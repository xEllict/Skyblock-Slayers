package com.skyblockslayers.slayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class SlayerUnlocks {

    /** Anything a slayer level can unlock. */
    public sealed interface Unlock permits StatBonus, RecipeUnlock { }

    /** A permanent stat increase, for example +2 max health. */
    public record StatBonus(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation)
            implements Unlock { }

    /** A crafting recipe that becomes available. */
    public record RecipeUnlock(ResourceLocation recipeId, String displayName) implements Unlock { }

    // slayer -> level -> list of unlocks
    private static final Map<SlayerType, Map<Integer, List<Unlock>>> UNLOCKS = new EnumMap<>(SlayerType.class);


    static {
        // PLACEHOLDER: one recipe for every level of every slayer. Replace these with your real ones.
        for (SlayerType type : SlayerType.values()) {
            for (int level = 1; level <= SlayerLevels.MAX_LEVEL; level++) {
                add(type, level, recipe(type.getSerializedName() + "_level_" + level + "_recipe", "Level " + level + " Placeholder"));
            }
        }

        // PLACEHOLDER: permanent stat buff, +2 max health (1 heart) at Revenant Horror level 5
        add(SlayerType.REVENANT_HORROR, 2, recipe("revenant_horror_wand_of_healing", "Wand of Healing"));
        add(SlayerType.REVENANT_HORROR, 5,
                new StatBonus(Attributes.MAX_HEALTH, 2.0, AttributeModifier.Operation.ADD_VALUE));
    }

    private static void add(SlayerType type, int level, Unlock unlock) {
        UNLOCKS.computeIfAbsent(type, t -> new HashMap<>())
                .computeIfAbsent(level, l -> new ArrayList<>())
                .add(unlock);
    }

    private static RecipeUnlock recipe(String id, String displayName) {
        return new RecipeUnlock(ResourceLocation.fromNamespaceAndPath("skyblockslayers", id), displayName);
    }

    /** Everything unlocked at exactly this level. */
    public static List<Unlock> getUnlocks(SlayerType type, int level) {
        return UNLOCKS.getOrDefault(type, Map.of()).getOrDefault(level, List.of());
    }

    /** The chat text for one unlock, e.g. "+2 Max Health" or "Recipe: ...". */
    public static Component describe(Unlock unlock) {
        return switch (unlock) {
            case StatBonus stat -> {
                boolean flat = stat.operation() == AttributeModifier.Operation.ADD_VALUE;
                double shown = flat ? stat.amount() : stat.amount() * 100; // percentages show as 10%, not 0.1
                String sign = shown >= 0 ? "+" : "-";
                String number = formatNumber(Math.abs(shown)) + (flat ? "" : "%");
                yield Component.literal(sign + number + " ")
                        .append(Component.translatable(stat.attribute().value().getDescriptionId()))
                        .withStyle(ChatFormatting.GREEN);
            }
            case RecipeUnlock recipe -> Component.literal(recipe.displayName() + " Recipe")
                    .withStyle(ChatFormatting.AQUA);
        };
    }

    private static String formatNumber(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    /** Everything unlocked from level 1 up to and including this level. */
    public static List<Unlock> getUnlocksUpToLevel(SlayerType type, int level) {
        List<Unlock> result = new ArrayList<>();
        for (int i = 1; i <= level; i++) {
            result.addAll(getUnlocks(type, i));
        }
        return result;
    }
}