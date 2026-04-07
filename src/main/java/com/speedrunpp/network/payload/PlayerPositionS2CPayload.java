package com.speedrunpp.network.payload;

import com.speedrunpp.network.PlayerPositionData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record PlayerPositionS2CPayload(List<PlayerPositionData> positions) implements CustomPayload {
    public static final CustomPayload.Id<PlayerPositionS2CPayload> ID =
            new CustomPayload.Id<>(Identifier.of("speedrunpp", "player_positions"));

    public static final PacketCodec<RegistryByteBuf, PlayerPositionS2CPayload> CODEC =
            new PacketCodec<>() {
                @Override
                public PlayerPositionS2CPayload decode(RegistryByteBuf buf) {
                    int size = buf.readVarInt();
                    List<PlayerPositionData> positions = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        positions.add(new PlayerPositionData(
                                buf.readString(),
                                buf.readDouble(),
                                buf.readDouble(),
                                buf.readDouble()
                        ));
                    }
                    return new PlayerPositionS2CPayload(positions);
                }

                @Override
                public void encode(RegistryByteBuf buf, PlayerPositionS2CPayload payload) {
                    buf.writeVarInt(payload.positions.size());
                    for (PlayerPositionData data : payload.positions) {
                        buf.writeString(data.name());
                        buf.writeDouble(data.x());
                        buf.writeDouble(data.y());
                        buf.writeDouble(data.z());
                    }
                }
            };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
