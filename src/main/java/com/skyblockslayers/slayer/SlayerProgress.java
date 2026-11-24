package com.skyblockslayers.slayer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SlayerProgress(int xp, int rngMeterXp, List<Integer> bossCompletions) {
    public static final int TIER_COUNT = 5;
    public static final SlayerProgress EMPTY = new SlayerProgress(0, 0, List.of());

    // always keeps exactly 5 entries in the list (one per tier)
    public SlayerProgress {
        List<Integer> fixed = new ArrayList<>(bossCompletions);
        while (fixed.size() < TIER_COUNT) {
            fixed.add(0);
        }
        bossCompletions = List.copyOf(fixed.subList(0, TIER_COUNT));
    }

    public static final Codec<SlayerProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("xp", 0).forGetter(SlayerProgress::xp),
            Codec.INT.optionalFieldOf("rng_meter_xp", 0).forGetter(SlayerProgress::rngMeterXp),
            Codec.INT.listOf().optionalFieldOf("boss_completions", List.of()).forGetter(SlayerProgress::bossCompletions)
    ).apply(instance, SlayerProgress::new));

    public static final StreamCodec<ByteBuf, SlayerProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SlayerProgress::xp,
            ByteBufCodecs.VAR_INT, SlayerProgress::rngMeterXp,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), SlayerProgress::bossCompletions,
            SlayerProgress::new);

    public int getCompletions(int tier) {
        return bossCompletions.get(tier - 1);
    }

    public SlayerProgress addXp(int amount) {
        return new SlayerProgress(xp + amount, rngMeterXp, bossCompletions);
    }

    public SlayerProgress addRngMeterXp(int amount) {
        return new SlayerProgress(xp, rngMeterXp + amount, bossCompletions);
    }

    public SlayerProgress withRngMeterXp(int value) {
        return new SlayerProgress(xp, value, bossCompletions);
    }

    public SlayerProgress addCompletion(int tier) {
        List<Integer> updated = new ArrayList<>(bossCompletions);
        updated.set(tier - 1, updated.get(tier - 1) + 1);
        return new SlayerProgress(xp, rngMeterXp, updated);
    }
}