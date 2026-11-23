package com.skyblockslayers.items;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("skyblockslayers");

    public static final DeferredItem<MaddoxBatphoneItem> MADDOX_BATPHONE =
            ITEMS.registerItem("maddox_batphone", MaddoxBatphoneItem::new, new Item.Properties().stacksTo(1));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}