package com.skyblockslayers.slayer;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.BossEvent;

public enum SlayerType implements StringRepresentable {
    REVENANT_HORROR("Revenant Horror", "revenant_horror", "Zombie"),
    TARANTULA_BROODFATHER("Tarantula Broodfather", "tarantula_broodfather", "Spider"),
    SKELETON_SLAYER("Skeleton Slayer", "skeleton_slayer", "Skeleton"),
    INFERNO_DEMONLORD("Inferno Demonlord", "inferno_demonlord", "Blaze"),
    VOIDGLOOM_SERAPH("Voidgloom Seraph", "voidgloom_seraph", "Enderman");

    public static final Codec<SlayerType> CODEC = StringRepresentable.fromEnum(SlayerType::values);

    private final String displayName;
    private final String id;
    private final String mobName;
    private final ResourceLocation icon;

    SlayerType(String displayName, String id, String mobName) {
        this.displayName = displayName;
        this.id = id;
        this.mobName = mobName;
        this.icon = ResourceLocation.fromNamespaceAndPath("skyblockslayers", "textures/gui/slayer/" + id + ".png");
    }

    public Component getDisplayName() {
        return Component.literal(displayName);
    }

    public String getMobName() {
        return mobName;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public BossEvent.BossBarColor getBarColor() {
        return switch (this) {
            case REVENANT_HORROR -> BossEvent.BossBarColor.GREEN;
            case TARANTULA_BROODFATHER -> BossEvent.BossBarColor.RED;
            case SKELETON_SLAYER -> BossEvent.BossBarColor.WHITE;
            case INFERNO_DEMONLORD -> BossEvent.BossBarColor.YELLOW; // vanilla boss bars have no orange
            case VOIDGLOOM_SERAPH -> BossEvent.BossBarColor.PURPLE;
        };
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}