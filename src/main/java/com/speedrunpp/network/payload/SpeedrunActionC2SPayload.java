package com.speedrunpp.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SpeedrunActionC2SPayload(int action) implements CustomPacketPayload {
    public static final int ACTION_START = 0;
    public static final int ACTION_PAUSE = 1;
    public static final int ACTION_RESUME = 2;
    public static final int ACTION_RESET = 3;

    public static final CustomPacketPayload.Type<SpeedrunActionC2SPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.of("speedrunpp", "speedrun_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpeedrunActionC2SPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SpeedrunActionC2SPayload::action,
                    SpeedrunActionC2SPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
