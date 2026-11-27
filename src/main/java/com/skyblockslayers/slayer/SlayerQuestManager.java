package com.skyblockslayers.slayer;

import com.skyblockslayers.entities.ModEntities;
import com.skyblockslayers.entities.RevenantHorror;
import com.skyblockslayers.entities.RevenantTier;
import com.skyblockslayers.network.StartQuestPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = "skyblockslayers")
public class SlayerQuestManager {
    // active quests by player. Not saved: they end when the player logs out or the server stops.
    private static final Map<UUID, SlayerQuest> QUESTS = new HashMap<>();

    // sound pitches
    private static final float START_PITCH = 1.5F;     // dragon growl when a quest starts
    private static final float COMPLETE_PITCH = 1.5F;  // ding when a quest is completed

    @Nullable
    public static SlayerQuest getQuest(ServerPlayer player) {
        return QUESTS.get(player.getUUID());
    }

    /** Is this entity the current boss of someone's quest? Used so leftover bosses can remove themselves. */
    public static boolean isBossActive(UUID bossUuid) {
        for (SlayerQuest quest : QUESTS.values()) {
            if (bossUuid.equals(quest.getBossUuid())) {
                return true;
            }
        }
        return false;
    }

    /** Which slayers and tiers actually have a boss to spawn. Expand this as you add bosses. */
    private static boolean isImplemented(SlayerType slayer, int tier) {
        return slayer == SlayerType.REVENANT_HORROR && tier >= 1 && tier <= 4;
    }

    // ---------- starting a quest ----------

    /** Called when a player's Confirm click arrives at the server. */
    public static void handleStartRequest(StartQuestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                tryStartQuest(player, payload.slayer(), payload.tier());
            }
        });
    }

    private static void tryStartQuest(ServerPlayer player, SlayerType slayer, int tier) {
        if (QUESTS.containsKey(player.getUUID())) {
            fail(player, "You already have an active slayer quest.");
            return;
        }
        if (!isImplemented(slayer, tier)) {
            fail(player, "That slayer quest isn't available yet.");
            return;
        }
        SlayerData data = player.getData(ModAttachments.SLAYER_DATA);
        if (!SlayerRequirements.isSlayerUnlocked(data, slayer)
                || !SlayerRequirements.isTierUnlocked(data, slayer, tier)) {
            fail(player, "You haven't unlocked that slayer quest.");
            return;
        }

        SlayerQuest quest = new SlayerQuest(slayer, tier);
        quest.getBossBar().addPlayer(player);
        QUESTS.put(player.getUUID(), quest);

        player.sendSystemMessage(Component.literal("SLAYER QUEST STARTED!")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("  Slay " + quest.getRequiredXp() + " XP worth of "
                + slayer.getMobName() + " mobs.").withStyle(ChatFormatting.GRAY));
        player.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, START_PITCH);
    }

    private static void fail(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.RED));
    }

    // ---------- ending a quest ----------

    /** Removes a player's quest and boss bar, with no message or sound. */
    public static void endQuest(ServerPlayer player) {
        SlayerQuest quest = QUESTS.remove(player.getUUID());
        if (quest != null) {
            quest.getBossBar().removeAllPlayers();
        }
    }

    /** The boss was killed. */
    public static void completeQuest(ServerPlayer player) {
        if (QUESTS.containsKey(player.getUUID())) {
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, COMPLETE_PITCH);
        }
        endQuest(player);
    }

    /** The quest failed. The boss (if it exists) notices it has no quest any more and removes itself. */
    public static void failQuest(ServerPlayer player, String reason) {
        if (!QUESTS.containsKey(player.getUUID())) {
            return;
        }
        endQuest(player);
        player.sendSystemMessage(Component.literal("SLAYER QUEST FAILED!")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.RED));
        player.sendSystemMessage(Component.literal("  " + reason).withStyle(ChatFormatting.GRAY));
    }

    // ---------- quest progress ----------

    /** When a player kills a mob that drops XP, count it towards their quest if it's the right family. */
    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (!(event.getAttackingPlayer() instanceof ServerPlayer player)) {
            return;
        }
        SlayerQuest quest = QUESTS.get(player.getUUID());
        if (quest == null || quest.getState() != SlayerQuest.State.COLLECTING) {
            return; // no quest, or the boss is already coming: XP no longer counts
        }
        LivingEntity killed = event.getEntity();
        if (!SlayerFamilies.isInFamily(quest.getSlayer(), killed)) {
            return;
        }
        int xp = event.getDroppedExperience();
        if (xp <= 0) {
            return;
        }

        quest.addCollectedXp(xp);
        if (quest.getCollectedXp() >= quest.getRequiredXp()) {
            beginSpawn(quest, killed);
        }
    }

    /** Starts the spawn animation and its sound where the last mob died. */
    private static void beginSpawn(SlayerQuest quest, LivingEntity killed) {
        if (!(killed.level() instanceof ServerLevel level)) {
            return;
        }
        quest.startSpawning(level, killed.position());
        level.playSound(null, killed.getX(), killed.getY(), killed.getZ(),
                ModSounds.BOSS_SPAWNING.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    /** Spawns the boss once the animation has finished. Only Revenant Horror exists so far. */
    private static void spawnBoss(ServerPlayer player, SlayerQuest quest) {
        ServerLevel level = quest.getSpawnLevel();
        RevenantHorror boss = ModEntities.REVENANT_HORROR.get().create(level);
        if (boss == null) {
            failQuest(player, "The boss could not spawn.");
            return;
        }
        boss.moveTo(quest.getSpawnPos().x, quest.getSpawnPos().y, quest.getSpawnPos().z,
                player.getYRot() + 180.0F, 0.0F); // roughly facing the player
        boss.setTier(RevenantTier.byTier(quest.getTier()));
        boss.attachToPlayer(player);
        boss.setPersistenceRequired(); // stops it despawning when the player is far away
        boss.setQuestBoss(true);
        level.addFreshEntity(boss);

        quest.startBossPhase(boss.getUUID());
        quest.updateBossBar(boss);
        player.sendSystemMessage(Component.literal("SLAYER BOSS SPAWNING!")
                .withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED));
    }

    // ---------- every tick ----------

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        for (UUID id : new ArrayList<>(QUESTS.keySet())) {
            SlayerQuest quest = QUESTS.get(id);
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (quest == null || player == null) {
                continue;
            }

            switch (quest.getState()) {
                case SPAWNING -> {
                    BossSpawnEffect.play(quest.getSpawnLevel(), quest.getSpawnPos(), quest.getSpawnTick());
                    quest.advanceSpawnTick();
                    if (quest.getSpawnTick() >= BossSpawnEffect.ANIMATION_TICKS) {
                        spawnBoss(player, quest);
                    }
                }
                case BOSS_ACTIVE -> {
                    RevenantHorror boss = findBoss(server, quest);
                    if (boss != null && boss.isAlive()) {
                        quest.updateBossBar(boss);
                    }
                    quest.tickBossTimer();
                    if (quest.getBossTicksLeft() <= 0) {
                        failQuest(player, "Time ran out!");
                    }
                }
                default -> { }
            }
        }
    }

    @Nullable
    private static RevenantHorror findBoss(MinecraftServer server, SlayerQuest quest) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(quest.getBossUuid()) instanceof RevenantHorror boss) {
                return boss;
            }
        }
        return null;
    }

    // ---------- failing: dying ----------

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SlayerQuest quest = QUESTS.get(player.getUUID());
            if (quest != null && quest.getState() != SlayerQuest.State.COLLECTING) {
                failQuest(player, "You died!");
            }
        }
    }

    // ---------- keep the quest and bar tidy ----------

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            endQuest(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        QUESTS.clear(); // so quests never leak between worlds
    }

    // respawning or changing dimension can make the client forget the bar, so send it again
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refreshBar(player);
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            refreshBar(player);
        }
    }

    private static void refreshBar(ServerPlayer player) {
        SlayerQuest quest = QUESTS.get(player.getUUID());
        if (quest != null) {
            quest.getBossBar().removeAllPlayers();
            quest.getBossBar().addPlayer(player);
        }
    }
}