package com.skyblockslayers;

import com.skyblockslayers.entities.ModEntities;
import com.skyblockslayers.items.ModItems;
import com.skyblockslayers.slayer.ModAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(SkyblockSlayers.MODID)
public class SkyblockSlayers {
    public static final String MODID = "skyblockslayers";

    public SkyblockSlayers(IEventBus modEventBus, ModContainer modContainer) {
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModAttachments.register(modEventBus);
    }
}
