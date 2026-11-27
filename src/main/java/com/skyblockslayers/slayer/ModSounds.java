package com.skyblockslayers.slayer;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, "skyblockslayers");

    public static final DeferredHolder<SoundEvent, SoundEvent> OCCASIONAL_DROP = sound("occasional_drop");
    public static final DeferredHolder<SoundEvent, SoundEvent> RARE_DROP = sound("rare_drop");
    public static final DeferredHolder<SoundEvent, SoundEvent> VERY_RARE_DROP = sound("very_rare_drop");
    public static final DeferredHolder<SoundEvent, SoundEvent> LEVEL_UP = sound("level_up");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_SPAWNING = sound("boss_spawning");

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath("skyblockslayers", name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}