package com.speedrunpp.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SpeedrunSyncS2CPayload(boolean started, boolean paused, long elapsedTicks, int days) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpeedrunSyncS2CPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("speedrunpp", "speedrun_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpeedrunSyncS2CPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SpeedrunSyncS2CPayload::started,
                    ByteBufCodecs.BOOL, SpeedrunSyncS2CPayload::paused,
                    ByteBufCodecs.VAR_LONG, SpeedrunSyncS2CPayload::elapsedTicks,
                    ByteBufCodecs.VAR_INT, SpeedrunSyncS2CPayload::days,
                    SpeedrunSyncS2CPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
