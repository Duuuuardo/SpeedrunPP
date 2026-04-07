package com.speedrunpp.network.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SpeedrunSyncS2CPayload(boolean started, boolean paused, long elapsedTicks, int days) implements CustomPayload {
    public static final CustomPayload.Id<SpeedrunSyncS2CPayload> ID =
            new CustomPayload.Id<>(Identifier.of("speedrunpp", "speedrun_sync"));

    public static final PacketCodec<RegistryByteBuf, SpeedrunSyncS2CPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.BOOL, SpeedrunSyncS2CPayload::started,
                    PacketCodecs.BOOL, SpeedrunSyncS2CPayload::paused,
                    PacketCodecs.VAR_LONG, SpeedrunSyncS2CPayload::elapsedTicks,
                    PacketCodecs.VAR_INT, SpeedrunSyncS2CPayload::days,
                    SpeedrunSyncS2CPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
