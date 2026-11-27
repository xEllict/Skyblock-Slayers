package com.skyblockslayers.network;

import com.skyblockslayers.slayer.SlayerType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Sent from the client to the server when the player confirms a slayer quest. */
public record StartQuestPayload(SlayerType slayer, int tier) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StartQuestPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath("skyblockslayers", "start_quest"));

    public static final StreamCodec<ByteBuf, StartQuestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(
                    i -> SlayerType.values()[Mth.clamp(i, 0, SlayerType.values().length - 1)],
                    SlayerType::ordinal),
            StartQuestPayload::slayer,
            ByteBufCodecs.VAR_INT, StartQuestPayload::tier,
            StartQuestPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}