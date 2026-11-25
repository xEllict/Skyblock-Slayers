package com.skyblockslayers.items;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("skyblockslayers");

    public static final DeferredItem<MaddoxBatphoneItem> MADDOX_BATPHONE = ITEMS.registerItem("maddox_batphone", MaddoxBatphoneItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> REVENANT_FLESH = ITEMS.registerSimpleItem("revenant_flesh");
    public static final DeferredItem<Item> REVENANT_VISCERA = ITEMS.registerSimpleItem("revenant_viscera");
    public static final DeferredItem<Item> FOUL_FLESH = ITEMS.registerSimpleItem("foul_flesh");
    public static final DeferredItem<Item> UNDEAD_CATALYST = ITEMS.registerSimpleItem("undead_catalyst");
    public static final DeferredItem<Item> REVENANT_CATALYST = ITEMS.registerSimpleItem("revenant_catalyst");
    public static final DeferredItem<Item> BEHEADED_HORROR = ITEMS.registerSimpleItem("beheaded_horror");

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}