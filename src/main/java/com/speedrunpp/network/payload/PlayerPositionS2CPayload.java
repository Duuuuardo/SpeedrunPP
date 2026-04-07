package com.speedrunpp.network.payload;

import com.speedrunpp.network.PlayerPositionData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record PlayerPositionS2CPayload(List<PlayerPositionData> positions) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PlayerPositionS2CPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.of("speedrunpp", "player_positions"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerPositionS2CPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public PlayerPositionS2CPayload decode(RegistryFriendlyByteBuf buf) {
                    int size = buf.readVarInt();
                    List<PlayerPositionData> positions = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        positions.add(new PlayerPositionData(
                                buf.readUtf(),
                                buf.readDouble(),
                                buf.readDouble(),
                                buf.readDouble()
                        ));
                    }
                    return new PlayerPositionS2CPayload(positions);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, PlayerPositionS2CPayload payload) {
                    buf.writeVarInt(payload.positions.size());
                    for (PlayerPositionData data : payload.positions) {
                        buf.writeUtf(data.name());
                        buf.writeDouble(data.x());
                        buf.writeDouble(data.y());
                        buf.writeDouble(data.z());
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
