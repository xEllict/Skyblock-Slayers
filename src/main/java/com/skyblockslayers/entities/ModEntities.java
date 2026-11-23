package com.skyblockslayers.entities;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, "skyblockslayers");

    public static final DeferredHolder<EntityType<?>, EntityType<RevenantHorror>> REVENANT_HORROR =
            ENTITIES.register("revenant_horror", () ->
                    EntityType.Builder.of(RevenantHorror::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .fireImmune()
                            .build("revenant_horror"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}