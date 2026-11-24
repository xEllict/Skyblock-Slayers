package com.skyblockslayers.slayer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.network.codec.StreamCodec;

public record SlayerData(Map<SlayerType, SlayerProgress> slayers) {
    public static final SlayerData EMPTY = new SlayerData(Map.of());

    public static final Codec<SlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(SlayerType.CODEC, SlayerProgress.CODEC)
                    .optionalFieldOf("slayers", Map.of())
                    .forGetter(SlayerData::slayers)
    ).apply(instance, SlayerData::new));

    public static final StreamCodec<ByteBuf, SlayerData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                for (SlayerType type : SlayerType.values()) {
                    SlayerProgress.STREAM_CODEC.encode(buf, data.get(type));
                }
            },
            buf -> {
                Map<SlayerType, SlayerProgress> map = new EnumMap<>(SlayerType.class);
                for (SlayerType type : SlayerType.values()) {
                    map.put(type, SlayerProgress.STREAM_CODEC.decode(buf));
                }
                return new SlayerData(map);
            });

    public SlayerProgress get(SlayerType type) {
        return slayers.getOrDefault(type, SlayerProgress.EMPTY);
    }

    public SlayerData with(SlayerType type, SlayerProgress progress) {
        Map<SlayerType, SlayerProgress> copy = new EnumMap<>(SlayerType.class);
        copy.putAll(slayers);
        copy.put(type, progress);
        return new SlayerData(copy);
    }
}