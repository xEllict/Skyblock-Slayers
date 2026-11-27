package com.skyblockslayers.slayer;

import org.jetbrains.annotations.Nullable;

public class SlayerRequirements {
    /** TESTING ONLY: set to true to unlock everything. */
    private static final boolean BYPASS_LOCKS_FOR_TESTING = false;

    /** The tier of the previous slayer that must be killed to unlock the next slayer. */
    public static final int REQUIRED_PREVIOUS_SLAYER_TIER = 2;

    private static final String[] ROMAN = {"I", "II", "III", "IV", "V"};

    /** The slayer that must be beaten first, or null for the first slayer (Revenant Horror). */
    @Nullable
    public static SlayerType getPrerequisite(SlayerType type) {
        return type.ordinal() == 0 ? null : SlayerType.values()[type.ordinal() - 1];
    }

    public static boolean isSlayerUnlocked(SlayerData data, SlayerType type) {
        if (BYPASS_LOCKS_FOR_TESTING) {
            return true;
        }
        SlayerType prerequisite = getPrerequisite(type);
        return prerequisite == null
                || data.get(prerequisite).getCompletions(REQUIRED_PREVIOUS_SLAYER_TIER) >= 1;
    }

    public static boolean isTierUnlocked(SlayerData data, SlayerType type, int tier) {
        if (BYPASS_LOCKS_FOR_TESTING) {
            return true;
        }
        return tier <= 1 || data.get(type).getCompletions(tier - 1) >= 1;
    }

    public static String toRoman(int number) {
        return ROMAN[Math.max(1, Math.min(number, ROMAN.length)) - 1];
    }
}