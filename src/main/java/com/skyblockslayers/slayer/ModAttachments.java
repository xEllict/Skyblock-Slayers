package com.skyblockslayers.slayer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "skyblockslayers");

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SlayerData>> SLAYER_DATA =
            ATTACHMENT_TYPES.register("slayer_data", () -> AttachmentType.builder(() -> SlayerData.EMPTY)
                    .serialize(SlayerData.CODEC)           // saved with the player
                    .sync(SlayerData.STREAM_CODEC)         // sent to the client (for the GUI)
                    .copyOnDeath()                         // kept after dying
                    .build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}