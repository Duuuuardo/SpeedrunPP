package com.speedrunpp.network.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SpeedrunActionC2SPayload(int action) implements CustomPayload {
    public static final int ACTION_START = 0;
    public static final int ACTION_PAUSE = 1;
    public static final int ACTION_RESUME = 2;
    public static final int ACTION_RESET = 3;

    public static final CustomPayload.Id<SpeedrunActionC2SPayload> ID =
            new CustomPayload.Id<>(Identifier.of("speedrunpp", "speedrun_action"));

    public static final PacketCodec<RegistryByteBuf, SpeedrunActionC2SPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.VAR_INT, SpeedrunActionC2SPayload::action,
                    SpeedrunActionC2SPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
