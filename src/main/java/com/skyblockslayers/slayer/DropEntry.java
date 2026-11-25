package com.skyblockslayers.slayer;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public record DropEntry(ItemFactory factory, int minCount, int maxCount, double chancePercent, int minLevel) {

    @FunctionalInterface
    public interface ItemFactory {
        ItemStack create(ServerLevel level, int count);
    }

    /**
     * A normal item with a count range. "revenant_flesh" means skyblockslayers:revenant_flesh.
     * For other mods or vanilla, write the full id, e.g. "minecraft:diamond".
     * If the item doesn't exist yet, this entry is skipped instead of crashing.
     */
    public static DropEntry item(String id, int min, int max, double chancePercent) {
        ResourceLocation location = id.contains(":")
                ? ResourceLocation.parse(id)
                : ResourceLocation.fromNamespaceAndPath("skyblockslayers", id);
        return new DropEntry(
                (level, count) -> BuiltInRegistries.ITEM.getOptional(location)
                        .map(item -> new ItemStack(item, count))
                        .orElse(ItemStack.EMPTY),
                min, max, chancePercent, 0);
    }

    /** A normal item with a fixed count. */
    public static DropEntry item(String id, int count, double chancePercent) {
        return item(id, count, count, chancePercent);
    }

    /** An enchanted book, e.g. enchantedBook(Enchantments.SMITE, 6, 4.0) for Smite VI at 4%. */
    public static DropEntry enchantedBook(ResourceKey<Enchantment> enchantment, int enchantLevel, double chancePercent) {
        return new DropEntry((level, count) -> {
            Holder<Enchantment> holder = level.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
            return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, enchantLevel));
        }, 1, 1, chancePercent, 0);
    }

    /** Returns a copy of this entry that only drops if the player's slayer level is at least this. */
    public DropEntry withMinLevel(int level) {
        return new DropEntry(factory, minCount, maxCount, chancePercent, level);
    }

    /** Rolls this entry once. Returns an empty stack if it didn't drop. */
    public ItemStack roll(ServerLevel level, RandomSource random, int slayerLevel) {
        if (slayerLevel < minLevel) {
            return ItemStack.EMPTY;
        }
        if (random.nextDouble() * 100.0 >= chancePercent) {
            return ItemStack.EMPTY;
        }
        int count = minCount == maxCount ? minCount : random.nextIntBetweenInclusive(minCount, maxCount);
        return factory.create(level, count);
    }
}