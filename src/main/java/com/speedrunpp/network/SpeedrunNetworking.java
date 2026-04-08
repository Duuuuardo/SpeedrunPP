package com.speedrunpp.network;

import com.speedrunpp.SpeedrunState;
import com.speedrunpp.network.payload.PlayerPositionS2CPayload;
import com.speedrunpp.network.payload.SpeedrunActionC2SPayload;
import com.speedrunpp.network.payload.SpeedrunSyncS2CPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class SpeedrunNetworking {

    public static void registerPayloads() {
        PayloadTypeRegistry.serverboundPlay().register(SpeedrunActionC2SPayload.ID, SpeedrunActionC2SPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SpeedrunSyncS2CPayload.ID, SpeedrunSyncS2CPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlayerPositionS2CPayload.ID, PlayerPositionS2CPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SpeedrunActionC2SPayload.ID, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = context.server();

            SpeedrunState state = SpeedrunState.get(server);

            switch (payload.action()) {
                case SpeedrunActionC2SPayload.ACTION_START -> {
                    if (!state.isStarted()) {
                        state.start(server);
                        broadcastMessage(server, Component.translatable("speedrunpp.toast.started"));
                    }
                }
                case SpeedrunActionC2SPayload.ACTION_RESET -> {
                    state.reset(server);
                    broadcastMessage(server, Component.translatable("speedrunpp.toast.reset"));
                }
            }
        });
    }

    public static void syncStateToAll(MinecraftServer server) {
        SpeedrunState state = SpeedrunState.get(server);
        SpeedrunSyncS2CPayload payload = new SpeedrunSyncS2CPayload(
                state.isStarted(),
                state.isPaused(),
                state.isCompleted(),
                state.getElapsedTicks(server),
                state.getDays(server)
        );

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void syncStateToPlayer(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server == null) return;

        SpeedrunState state = SpeedrunState.get(server);
        SpeedrunSyncS2CPayload payload = new SpeedrunSyncS2CPayload(
                state.isStarted(),
                state.isPaused(),
                state.isCompleted(),
                state.getElapsedTicks(server),
                state.getDays(server)
        );
        ServerPlayNetworking.send(player, payload);
    }

    public static void syncPlayerPositions(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        for (ServerPlayer receiver : players) {
            List<PlayerPositionData> positions = new ArrayList<>();
            for (ServerPlayer other : players) {
                if (other == receiver) continue;
                positions.add(new PlayerPositionData(
                        other.getName().getString(),
                        other.getX(),
                        other.getY(),
                        other.getZ()
                ));
            }

            if (!positions.isEmpty()) {
                ServerPlayNetworking.send(receiver, new PlayerPositionS2CPayload(positions));
            }
        }
    }

    private static void broadcastMessage(MinecraftServer server, Component message) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(message, true);
        }
    }
}
