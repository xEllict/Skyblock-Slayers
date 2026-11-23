package com.skyblockslayers.slayer;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public enum SlayerType {
    REVENANT_HORROR("Revenant Horror", "revenant_horror"),
    TARANTULA_BROODFATHER("Tarantula Broodfather", "tarantula_broodfather"),
    SKELETON_SLAYER("Skeleton Slayer", "skeleton_slayer"),
    INFERNO_DEMONLORD("Inferno Demonlord", "inferno_demonlord"),
    VOIDGLOOM_SERAPH("Voidgloom Seraph", "voidgloom_seraph");

    private final String displayName;
    private final ResourceLocation icon;

    SlayerType(String displayName, String id) {
        this.displayName = displayName;
        this.icon = ResourceLocation.fromNamespaceAndPath("skyblockslayers", "textures/gui/slayer/" + id + ".png");
    }

    public Component getDisplayName() {
        return Component.literal(displayName);
    }

    public ResourceLocation getIcon() {
        return icon;
    }
}