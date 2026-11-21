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

import net.minecraft.world.entity.Entity;

public class RevenantHorror extends Monster {
    private static final EntityDataAccessor<Integer> DATA_TIER =
            SynchedEntityData.defineId(RevenantHorror.class, EntityDataSerializers.INT);
    private UUID attachedPlayer;
    private static final String[] ROMAN = {"I", "II", "III", "IV"};
    private int lastBarHealth = -1;
    private int lastBarTier = -1;

    public RevenantHorror(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        equipGear();
        applyTierStats(RevenantTier.TIER_1);
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
        equip(EquipmentSlot.HEAD, createSkull());
        equip(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
        equip(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
        equip(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
    }

    private void equip(EquipmentSlot slot, ItemStack stack) {
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY); // no armor stats
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(false));               // armor never breaks
        this.setItemSlot(slot, stack);
        this.setDropChance(slot, 0.0F);                                              // never drops
    }

    private static ItemStack createSkull() {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/d8bee23b5c726ae8e3d021e8b4f7525619ab102a4e04be983b61414349aaac67\"}}}";
        String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        GameProfile profile = new GameProfile(UUID.nameUUIDFromBytes("revenant_horror".getBytes(StandardCharsets.UTF_8)), "revenant_horror");
        profile.getProperties().put("textures", new Property("textures", encoded));
        ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
        skull.set(DataComponents.PROFILE, new ResolvableProfile(profile));
        return skull;
    }

    @Override
    public int getArmorValue() {
        return 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TIER, 1);
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
            if (attachedPlayer != null && this.tickCount % 60 == 0) {
                Player target = this.level().getPlayerByUUID(attachedPlayer);
                if (target != null && target.isAlive()) {
                    target.hurt(this.damageSources().magic(), (float) getTier().getDamage());
                }
            }
        }
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

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (attachedPlayer != null && target != null && !target.getUUID().equals(attachedPlayer)) {
            return;
        }
        super.setTarget(target);
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
        }
        return hit;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ATTACK_DAMAGE, 1.5)
                .add(Attributes.MOVEMENT_SPEED, 0.38)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }
}