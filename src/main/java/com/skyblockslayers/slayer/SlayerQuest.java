package com.skyblockslayers.slayer;

import com.skyblockslayers.entities.RevenantHorror;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.phys.Vec3;

/** One player's active slayer quest. */
public class SlayerQuest {
    /** XP needed to spawn the boss for tiers 1 to 5 (starting values, balance as you like). */
    public static final int[] XP_REQUIRED = {25, 50, 100, 175, 250};

    /** How long the player has to kill the boss, in ticks (20 ticks = 1 second). */
    public static final int BOSS_TIME_LIMIT_TICKS = 20 * 60 * 5; // 5 minutes

    public enum State { COLLECTING, SPAWNING, BOSS_ACTIVE }

    private final SlayerType slayer;
    private final int tier;
    private final ServerBossEvent bossBar;
    private State state = State.COLLECTING;
    private int collectedXp = 0;

    // spawn animation
    private ServerLevel spawnLevel;
    private Vec3 spawnPos;
    private int spawnTick;

    // boss phase
    private UUID bossUuid;
    private int bossTicksLeft;

    public SlayerQuest(SlayerType slayer, int tier) {
        this.slayer = slayer;
        this.tier = tier;
        this.bossBar = new ServerBossEvent(Component.empty(), slayer.getBarColor(), BossEvent.BossBarOverlay.PROGRESS);
        updateCollectingBar();
    }

    public SlayerType getSlayer() { return slayer; }
    public int getTier() { return tier; }
    public ServerBossEvent getBossBar() { return bossBar; }
    public State getState() { return state; }
    public int getCollectedXp() { return collectedXp; }
    public int getRequiredXp() { return XP_REQUIRED[tier - 1]; }

    public ServerLevel getSpawnLevel() { return spawnLevel; }
    public Vec3 getSpawnPos() { return spawnPos; }
    public int getSpawnTick() { return spawnTick; }
    public void advanceSpawnTick() { spawnTick++; }

    public UUID getBossUuid() { return bossUuid; }
    public int getBossTicksLeft() { return bossTicksLeft; }
    public void tickBossTimer() { bossTicksLeft--; }

    public void addCollectedXp(int amount) {
        collectedXp = Math.min(collectedXp + amount, getRequiredXp());
        updateCollectingBar();
    }

    public void startSpawning(ServerLevel level, Vec3 pos) {
        this.state = State.SPAWNING;
        this.spawnLevel = level;
        this.spawnPos = pos;
        this.spawnTick = 0;
    }

    public void startBossPhase(UUID bossUuid) {
        this.bossUuid = bossUuid;
        this.state = State.BOSS_ACTIVE;
        this.bossTicksLeft = BOSS_TIME_LIMIT_TICKS;
    }

    private Component title() {
        return Component.literal(slayer.getDisplayName().getString() + " " + SlayerRequirements.toRoman(tier) + ": ")
                .withStyle(ChatFormatting.DARK_GREEN);
    }

    /** "☠ Revenant Horror IV: 12/25" */
    public void updateCollectingBar() {
        bossBar.setProgress((float) collectedXp / getRequiredXp());
        bossBar.setName(Component.literal("☠ ").withStyle(ChatFormatting.RED)
                .append(title())
                .append(Component.literal(String.valueOf(collectedXp)).withStyle(ChatFormatting.GREEN))
                .append(Component.literal("/").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf(getRequiredXp())).withStyle(ChatFormatting.GREEN))
                .append(Component.literal("XP").withStyle(ChatFormatting.GREEN)));
    }

    /** "☠ Revenant Horror IV: 800/1200❤" */
    public void updateBossBar(RevenantHorror boss) {
        double multiplier = boss.getDamageTakenMultiplier();
        int current = (int) Math.ceil(boss.getHealth() / multiplier);
        int max = (int) Math.ceil(boss.getMaxHealth() / multiplier);
        float fraction = max > 0 ? Mth.clamp((float) current / max, 0.0F, 1.0F) : 0.0F;
        ChatFormatting healthColor = fraction > 0.5F ? ChatFormatting.GREEN
                : fraction > 0.25F ? ChatFormatting.YELLOW : ChatFormatting.RED;

        bossBar.setProgress(fraction);
        bossBar.setName(Component.literal("☠ ").withStyle(ChatFormatting.RED)
                .append(title())
                .append(Component.literal(String.valueOf(current)).withStyle(healthColor))
                .append(Component.literal("/").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(String.valueOf(max)).withStyle(ChatFormatting.GREEN))
                .append(Component.literal("❤").withStyle(ChatFormatting.RED)));
    }
}