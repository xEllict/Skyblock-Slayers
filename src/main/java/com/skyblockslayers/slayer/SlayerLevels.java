package com.skyblockslayers.slayer;

import java.util.EnumMap;
import java.util.Map;

public class SlayerLevels {
    public static final int MAX_LEVEL = 9;

    // total XP needed to reach level 1, 2, 3 ... 9 (0 = not set yet)
    private static final Map<SlayerType, int[]> TOTAL_XP = new EnumMap<>(SlayerType.class);

    static {
        TOTAL_XP.put(SlayerType.REVENANT_HORROR,       new int[]{5, 15, 100, 250, 1000, 5000, 15000, 40000, 100000});
        TOTAL_XP.put(SlayerType.TARANTULA_BROODFATHER, new int[]{5, 15, 100, 250, 1000, 5000, 15000, 40000, 100000});
        TOTAL_XP.put(SlayerType.SKELETON_SLAYER,       new int[]{5, 15, 100, 250, 1000, 5000, 15000, 40000, 100000});
        TOTAL_XP.put(SlayerType.INFERNO_DEMONLORD,     new int[]{5, 15, 100, 250, 1000, 5000, 15000, 40000, 100000});
        TOTAL_XP.put(SlayerType.VOIDGLOOM_SERAPH,      new int[]{5, 15, 100, 250, 1000, 5000, 15000, 40000, 100000});
    }

    /** Total XP needed to reach the given level (1 to 9). Returns 0 if not set. */
    public static int getTotalXpForLevel(SlayerType type, int level) {
        if (level < 1 || level > MAX_LEVEL) {
            return 0;
        }
        return TOTAL_XP.get(type)[level - 1];
    }

    /** The level a player with this much XP is at (0 to 9). */
    public static int getLevel(SlayerType type, int xp) {
        int level = 0;
        for (int i = 1; i <= MAX_LEVEL; i++) {
            int required = getTotalXpForLevel(type, i);
            if (required <= 0 || xp < required) {
                break;
            }
            level = i;
        }
        return level;
    }

    /** XP still needed for the next level. Returns 0 at max level or if the next level isn't set. */
    public static int getXpToNextLevel(SlayerType type, int xp) {
        int level = getLevel(type, xp);
        if (level >= MAX_LEVEL) {
            return 0;
        }
        int next = getTotalXpForLevel(type, level + 1);
        return next <= 0 ? 0 : next - xp;
    }
}