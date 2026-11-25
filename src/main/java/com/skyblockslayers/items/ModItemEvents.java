package com.skyblockslayers.items;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = "skyblockslayers")
public class ModItemEvents {
    @SubscribeEvent
    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.MADDOX_BATPHONE);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.REVENANT_FLESH);
            event.accept(ModItems.FOUL_FLESH);
            event.accept(ModItems.UNDEAD_CATALYST);
            event.accept(ModItems.REVENANT_CATALYST);
            event.accept(ModItems.REVENANT_VISCERA);
            event.accept(ModItems.BEHEADED_HORROR);
        }
    }
}