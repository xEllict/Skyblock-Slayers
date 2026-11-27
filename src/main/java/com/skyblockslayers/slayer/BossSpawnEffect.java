package com.skyblockslayers.slayer;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class BossSpawnEffect {
    // ---------- tweak these ----------
    public static final int ANIMATION_TICKS = 30;     // how long the animation plays before the boss appears (20 ticks = 1 second)
    private static final double HEIGHT = 2.0;         // height of the vortex in blocks
    private static final double BASE_RADIUS = 0.5;    // radius at the bottom in blocks
    private static final double SPREAD_DEGREES = 10.0;// how much the vortex widens going up
    private static final int PARTICLES_PER_TICK = 6;  // spawn rate, for each of the two particle types
    private static final double SWIRL_SPEED = 0.35;   // how far the vortex turns each tick (radians)
    private static final double TWIST = 1.5;          // extra turn from the bottom to the top (radians)
    private static final double JITTER = 0.08;        // random position offset in blocks
    private static final double DRIFT = 0.04;         // how fast witch particles drift around the circle

    /** Spawns one tick's worth of particles. Call once per tick while the animation plays. */
    public static void play(ServerLevel level, Vec3 center, int tick) {
        RandomSource random = level.getRandom();
        double widenPerBlock = Math.tan(Math.toRadians(SPREAD_DEGREES));

        for (int i = 0; i < PARTICLES_PER_TICK; i++) {
            double t = random.nextDouble();                       // 0 = bottom, 1 = top
            double y = center.y + t * HEIGHT;
            double radius = BASE_RADIUS + t * HEIGHT * widenPerBlock;
            double angle = tick * SWIRL_SPEED + t * TWIST + random.nextDouble() * 0.5;

            double x = center.x + Math.cos(angle) * radius + (random.nextDouble() - 0.5) * 2.0 * JITTER;
            double z = center.z + Math.sin(angle) * radius + (random.nextDouble() - 0.5) * 2.0 * JITTER;

            // direction of travel around the circle
            double tx = -Math.sin(angle);
            double tz = Math.cos(angle);

            // witch particles drift around the circle and slowly rise
            level.sendParticles(ParticleTypes.WITCH, x, y, z, 0, tx, 0.3, tz, DRIFT);

            // enchant particles fly in from behind and below, which makes the swirl look like it's turning
            level.sendParticles(ParticleTypes.ENCHANT, x, y, z, 0, -tx * 0.6, -0.4, -tz * 0.6, 1.0);
        }
    }
}