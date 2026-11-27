package com.skyblockslayers.slayer;

import com.skyblockslayers.entities.RevenantHorror;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;

public class SlayerFamilies {
    private static final Map<SlayerType, TagKey<EntityType<?>>> TAGS = new EnumMap<>(SlayerType.class);

    static {
        TAGS.put(SlayerType.REVENANT_HORROR, tag("family/zombie"));
        TAGS.put(SlayerType.TARANTULA_BROODFATHER, tag("family/spider"));
        TAGS.put(SlayerType.SKELETON_SLAYER, tag("family/skeleton"));
        TAGS.put(SlayerType.INFERNO_DEMONLORD, tag("family/blaze"));
        TAGS.put(SlayerType.VOIDGLOOM_SERAPH, tag("family/enderman"));
    }

    private static TagKey<EntityType<?>> tag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("skyblockslayers", path));
    }

    /** Does killing this mob count towards the given slayer's quest? */
    public static boolean isInFamily(SlayerType slayer, LivingEntity entity) {
        if (entity instanceof RevenantHorror) {
            return false; // slayer bosses never count
        }
        if (entity.getType().is(TAGS.get(slayer))) {
            return true;
        }
        // fallback: modded mobs that are built on the vanilla classes
        return switch (slayer) {
            case REVENANT_HORROR -> entity instanceof Zombie;
            case TARANTULA_BROODFATHER -> entity instanceof Spider;
            case SKELETON_SLAYER -> entity instanceof AbstractSkeleton;
            case INFERNO_DEMONLORD -> entity instanceof Blaze;
            case VOIDGLOOM_SERAPH -> entity instanceof EnderMan;
        };
    }
}