package com.skyblockslayers.items;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemLore;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("skyblockslayers");

    public static final DeferredItem<MaddoxBatphoneItem> MADDOX_BATPHONE = ITEMS.registerItem("maddox_batphone", MaddoxBatphoneItem::new, new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> REVENANT_FLESH = ITEMS.registerSimpleItem("revenant_flesh",
            itemInfo("revenant_flesh", ChatFormatting.GREEN,
                    lore("The flesh torn from the horror itself.", ChatFormatting.DARK_GRAY)));
    public static final DeferredItem<Item> FOUL_FLESH = ITEMS.registerSimpleItem("foul_flesh",
            itemInfo("foul_flesh", ChatFormatting.BLUE,
                    lore("This flesh has a more foul smell than the rest.", ChatFormatting.DARK_GRAY),
                    lore("Legend says this stuff can stoke a furnace's flame to increase it's heat", ChatFormatting.DARK_GRAY),
                    lore("can be used to speed up a furnace by 2x for a while", ChatFormatting.ITALIC)));
    public static final DeferredItem<Item> UNDEAD_CATALYST = ITEMS.registerSimpleItem("undead_catalyst",
            itemInfo("undead_catalyst", ChatFormatting.DARK_PURPLE,
                    lore("A catalyst used in mysterious undead crafting.", ChatFormatting.DARK_GRAY)));
    public static final DeferredItem<Item> REVENANT_CATALYST = ITEMS.registerSimpleItem("revenant_catalyst",
            itemInfo("revenant_catalyst", ChatFormatting.DARK_PURPLE,
                    lore("A catalyst used to craft items of the Revenant itself", ChatFormatting.DARK_GRAY)));
    public static final DeferredItem<Item> REVENANT_VISCERA = ITEMS.registerSimpleItem("revenant_viscera",
            itemInfo("revenant_viscera", ChatFormatting.BLUE,
                    lore("The bodily internals of the horror", ChatFormatting.DARK_GRAY),
                    lore("yuck", ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
    public static final DeferredItem<Item> BEHEADED_HORROR = ITEMS.registerSimpleItem("beheaded_horror",
            itemInfo("beheaded_horror", ChatFormatting.DARK_PURPLE,
                    lore("The head ripped straight from the neck of the Revenant Horror.", ChatFormatting.DARK_GRAY),
                    lore("is it still looking at me..?", ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));

    /** Item properties with a coloured name and optional lore lines. */
    private static Item.Properties itemInfo(String id, ChatFormatting nameColor, Component... loreLines) {
        return new Item.Properties()
                .component(DataComponents.ITEM_NAME,
                        Component.translatable("item.skyblockslayers." + id).withStyle(nameColor))
                .component(DataComponents.LORE, new ItemLore(List.of(loreLines)));
    }

    /** One lore line: gray and not italic by default. Add formatting after the text if you want it. */
    private static Component lore(String text, ChatFormatting... formatting) {
        return Component.literal(text)
                .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false))
                .withStyle(formatting);
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}