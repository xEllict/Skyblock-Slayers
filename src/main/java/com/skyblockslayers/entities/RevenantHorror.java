package com.skyblockslayers.entities;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.entity.ai.control.MoveControl;

public class RevenantHorror extends Monster {
    private static final EntityDataAccessor<Integer> DATA_TIER =
            SynchedEntityData.defineId(RevenantHorror.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SHAKING =
            SynchedEntityData.defineId(RevenantHorror.class, EntityDataSerializers.BOOLEAN);
    // Variable Fields
    private UUID attachedPlayer;
    private static final String[] ROMAN = {"I", "II", "III", "IV"};
    private int lastBarHealth = -1;
    private int lastBarTier = -1;
    private static final double PULL_HORIZONTAL = 1.1;
    private static final double PULL_VERTICAL = 0.35;
    private int ticksSinceMeleeHit = 0;
    private int ticksSinceYank = 0;
    private static final double EXTRA_REACH = 0.7; // blocks added on top of vanilla
    private static final double AURA_RADIUS = 2.5;
    // ---- Enrage settings (tiers 3 and 4) ----
    private static final int NORMAL_DURATION = 340;   // 17s normal before the mad state begins
    private static final int MAD_DURATION = 60;       // 3s: stands still, anvil sound, smoke
    private static final int ENRAGE_DURATION = 120;   // 6s enraged
    private static final double ENRAGE_DAMAGE_MULTIPLIER = 2.0;
    private static final int[] SMOKE_TICKS = {30, 45, 50}; // ticks after the mad state starts
    private static final int ANVIL_DELAY = 26; // ticks after the mad state starts

    private static final String HEAD_NORMAL = "http://textures.minecraft.net/texture/d8bee23b5c726ae8e3d021e8b4f7525619ab102a4e04be983b61414349aaac67";
    private static final String HEAD_MAD = "http://textures.minecraft.net/texture/ba7bb34471508b4b20cb35c37a1dd6e8afa7f2d5388822138837caf59c2195d8";

    private enum State { NORMAL, MAD, ENRAGED }
    private State state = State.NORMAL;
    private int stateTicks = 0;

    private static final ResourceKey<DamageType> TRUE_DAMAGE = ResourceKey.create(
            Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("skyblockslayers", "true_damage"));

    public RevenantHorror(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        equipGear();
        applyTierStats(RevenantTier.TIER_1);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.moveControl = new MoveControl(this) {
            @Override
            public void tick() {
                if (state == State.MAD) {
                    this.mob.setZza(0.0F);
                    this.mob.setXxa(0.0F);
                    return; // no movement while mad
                }
                super.tick();
            }
        };
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                living -> attachedPlayer == null || living.getUUID().equals(attachedPlayer)));
    }

    private void equipGear() {
        equip(EquipmentSlot.HEAD, createSkull(HEAD_NORMAL));
        equip(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        equip(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
        equip(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
    }

    private static ItemStack createSkull(String textureUrl) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}";
        String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes(textureUrl.getBytes(StandardCharsets.UTF_8)), "revenant_horror");
        profile.getProperties().put("textures", new Property("textures", encoded));
        ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
        skull.set(DataComponents.PROFILE, new ResolvableProfile(profile));
        return skull;
    }

    private static ItemStack createRedChestplate() {
        ItemStack chest = new ItemStack(Items.LEATHER_CHESTPLATE);
        chest.set(DataComponents.DYED_COLOR, new DyedItemColor(0xFF0000, false));
        return chest;
    }

    private void equip(EquipmentSlot slot, ItemStack stack) {
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY); // no armor stats
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(false));               // armor never breaks
        this.setItemSlot(slot, stack);
        this.setDropChance(slot, 0.0F);                                              // never drops
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ZOMBIE_HURT;
    }
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 0.8F);
    }
    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.7F; // lower = deeper
    }
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypeTags.IS_FIRE)
                || super.isInvulnerableTo(source);
    }
    @Override
    public int getArmorValue() {
        return 0;
    }
    @Override
    protected AABB getAttackBoundingBox() {
        return super.getAttackBoundingBox().inflate(EXTRA_REACH, 0.0, EXTRA_REACH);
    }
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TIER, 1);
        builder.define(DATA_SHAKING, false);
    }

    public RevenantTier getTier() {
        return RevenantTier.byTier(this.entityData.get(DATA_TIER));
    }

    public void setTier(RevenantTier tier) {
        this.entityData.set(DATA_TIER, tier.getTier());
        applyTierStats(tier);
    }

    private void applyTierStats(RevenantTier tier) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.min(tier.getHealth(), 1024.0));
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(tier.getDamage());
        this.setHealth(this.getMaxHealth());
        this.setCustomNameVisible(true);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Tier", this.entityData.get(DATA_TIER));
        if (attachedPlayer != null) {
            tag.putUUID("AttachedPlayer", attachedPlayer);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Tier")) {
            setTier(RevenantTier.byTier(tag.getInt("Tier")));
            if (tag.contains("Health")) {
                this.setHealth(tag.getFloat("Health"));
            }
        }
        if (tag.hasUUID("AttachedPlayer")) {
            attachedPlayer = tag.getUUID("AttachedPlayer");
        }
        setState(State.NORMAL);
    }

    private void updateHealthBar() {
        int tier = getTier().getTier();
        double multiplier = getDamageTakenMultiplier();
        int current = (int) Math.ceil(this.getHealth() / multiplier);
        if (current == lastBarHealth && tier == lastBarTier) {
            return; // nothing changed, skip rebuilding the name
        }
        lastBarHealth = current;
        lastBarTier = tier;

        int max = (int) Math.ceil(this.getMaxHealth() / multiplier);
        float fraction = (float) current / max;
        ChatFormatting healthColor = fraction > 0.5F ? ChatFormatting.GREEN
                : fraction > 0.25F ? ChatFormatting.YELLOW : ChatFormatting.RED;

        Component name = Component.literal("☠ ").withStyle(ChatFormatting.RED)
                .append(Component.literal("Revenant Horror " + ROMAN[tier - 1]).withStyle(ChatFormatting.DARK_GREEN))
                .append(Component.literal(" " + current).withStyle(healthColor))
                .append(Component.literal("/").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("" + max).withStyle(ChatFormatting.GREEN))
                .append(Component.literal("❤").withStyle(ChatFormatting.RED));
        this.setCustomName(name);
    }

    public double getDamageTakenMultiplier() {
        return Math.min(1.0, 1024.0 / getTier().getHealth());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, (float) (amount * getDamageTakenMultiplier()));
    }

    public void attachToPlayer(Player player) {
        this.attachedPlayer = player.getUUID();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            updateHealthBar();
            tickEnrageCycle();
            if (getTier().getTier() >= 2 && this.tickCount % 20 == 0) {
                auraAttack();
            }
            if (attachedPlayer != null) {
                Player target = this.level().getPlayerByUUID(attachedPlayer);
                if (target != null && target.isAlive()) {
                    if (this.tickCount % 60 == 0) {
                        target.hurt(this.damageSources().generic(), (float) getTier().getDamage());
                        target.invulnerableTime = 0;
                    }
                    if (state != State.MAD) {
                        ticksSinceMeleeHit++;
                        ticksSinceYank++;
                    }
                    if (ticksSinceMeleeHit >= 300) {          // 15 seconds with no melee hit
                        teleportToPlayer(target);
                        ticksSinceMeleeHit = 0;
                        ticksSinceYank = 0;
                    } else if (ticksSinceYank >= 100) {       // 5 seconds since the last yank
                        pullPlayer(target);
                        ticksSinceYank = 0;
                    }
                }
            }
        }
    }

    private void tickEnrageCycle() {
        if (getTier().getTier() < 3) {
            return; // tiers 1 and 2 never enrage
        }
        stateTicks++;
        switch (state) {
            case NORMAL -> {
                if (stateTicks >= NORMAL_DURATION) setState(State.MAD);
            }
            case MAD -> {
                this.getNavigation().stop();
                if (stateTicks == ANVIL_DELAY) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ANVIL_DESTROY, SoundSource.HOSTILE, 1.0F, 0.5F);
                }
                for (int t : SMOKE_TICKS) {
                    if (stateTicks == t) spawnSmoke();
                }
                if (stateTicks >= MAD_DURATION) setState(State.ENRAGED);
            }
            case ENRAGED -> {
                if (stateTicks >= ENRAGE_DURATION) setState(State.NORMAL);
            }
        }
    }

    private void setState(State newState) {
        this.state = newState;
        this.stateTicks = 0;
        this.entityData.set(DATA_SHAKING, newState != State.NORMAL);
        switch (newState) {
            case NORMAL -> {
                equipGear();
                this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(getTier().getDamage());
            }
            case MAD -> {
                equip(EquipmentSlot.HEAD, createSkull(HEAD_MAD));
                this.getNavigation().stop();
            }
            case ENRAGED -> {
                equip(EquipmentSlot.CHEST, createRedChestplate());
                this.getAttribute(Attributes.ATTACK_DAMAGE)
                        .setBaseValue(getTier().getDamage() * ENRAGE_DAMAGE_MULTIPLIER);
            }
        }
    }

    private void spawnSmoke() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    this.getX(), this.getY() + this.getBbHeight() + 0.5, this.getZ(),
                    15, 0.25, 0.1, 0.25, 0.02);
        }
    }

    public boolean isEnrageShaking() {
        return this.entityData.get(DATA_SHAKING);
    }

    private void teleportToPlayer(Player player) {
        this.getNavigation().stop();
        this.moveTo(player.getX(), player.getY(), player.getZ(), this.getYRot(), this.getXRot());
        player.hurt(trueDamage(), (float) getTier().getDamage());
        this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 1.0F, 0.6F);
    }

    private DamageSource trueDamage() {
        return new DamageSource(this.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TRUE_DAMAGE));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (attachedPlayer != null && target != null && !target.getUUID().equals(attachedPlayer)) {
            return;
        }
        super.setTarget(target);
    }

    private void pullPlayer(Player player) {
        Vec3 toBoss = this.position().subtract(player.position());
        Vec3 flat = new Vec3(toBoss.x, 0, toBoss.z);
        if (flat.lengthSqr() > 1.0E-4) {
            flat = flat.normalize().scale(PULL_HORIZONTAL);
        }
        player.hurtMarked = true; // sends the new velocity to the player's client
        player.hurt(trueDamage(), (float) getTier().getDamage());
        player.setDeltaMovement(player.getDeltaMovement().add(flat.x, PULL_VERTICAL, flat.z));
        this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.HOSTILE, 1.0F, 0.6F);


    }

    private void auraAttack() {
        double total = getTier().getDamage() / 2.0;
        float normalPart = (float) (total * 0.75);
        float truePart = (float) (total * 0.25);

        for (Player candidate : this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(AURA_RADIUS),
                p -> p.isAlive() && !p.isSpectator() && p.distanceToSqr(this) <= AURA_RADIUS * AURA_RADIUS)) {
            candidate.invulnerableTime = 0; // stops recent hits from blocking this damage
            candidate.hurt(this.damageSources().generic(), normalPart);
            candidate.hurt(trueDamage(), truePart);
        }
    }

    // attaches to the first player it hits (TEMPORARY, replace once spawn logic exists)
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof Player player) {
            if (attachedPlayer == null) {
                attachToPlayer(player);
            }
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 1, false, false));
            ticksSinceMeleeHit = 0;
            ticksSinceYank = 0;
            player.invulnerableTime = 0;
        }
        return hit;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ATTACK_DAMAGE, 1.5)
                .add(Attributes.MOVEMENT_SPEED, 0.38)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.75)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }
}