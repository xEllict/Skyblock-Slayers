package com.skyblockslayers.entities;

public enum RevenantTier {
    TIER_1(1, 50.0, 1.5),
    TIER_2(2, 200.0, 2.5),
    TIER_3(3, 400.0, 8.0),
    TIER_4(4, 1200.0, 15.0);

    private final int tier;
    private final double health;
    private final double damage;

    RevenantTier(int tier, double health, double damage) {
        this.tier = tier;
        this.health = health;
        this.damage = damage;
    }

    public int getTier() { return tier; }
    public double getHealth() { return health; }
    public double getDamage() { return damage; }

    public static RevenantTier byTier(int tier) {
        for (RevenantTier t : values()) {
            if (t.tier == tier) return t;
        }
        return TIER_1;
    }
}