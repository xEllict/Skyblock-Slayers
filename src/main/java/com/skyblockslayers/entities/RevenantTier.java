package com.skyblockslayers.entities;

public enum RevenantTier {
    TIER_1(1, 75.0, 1.5 , 5),
    TIER_2(2, 200.0, 2.5, 25),
    TIER_3(3, 500.0, 4.0, 100),
    TIER_4(4, 1000.0, 7.0, 500);

    private final int tier;
    private final double health;
    private final double damage;
    private final int xp;

    RevenantTier(int tier, double health, double damage, int xp) {
        this.tier = tier;
        this.health = health;
        this.damage = damage;
        this.xp = xp;
    }

    public int getTier() { return tier; }
    public double getHealth() { return health; }
    public double getDamage() { return damage; }
    public int getXp() { return xp; }

    public static RevenantTier byTier(int tier) {
        for (RevenantTier t : values()) {
            if (t.tier == tier) return t;
        }
        return TIER_1;
    }
}