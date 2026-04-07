package com.speedrunpp.network;

import com.speedrunpp.SpeedrunState;
import com.speedrunpp.network.payload.PlayerPositionS2CPayload;
import com.speedrunpp.network.payload.SpeedrunActionC2SPayload;
import com.speedrunpp.network.payload.SpeedrunSyncS2CPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SpeedrunNetworking {

    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(SpeedrunActionC2SPayload.ID, SpeedrunActionC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SpeedrunSyncS2CPayload.ID, SpeedrunSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerPositionS2CPayload.ID, PlayerPositionS2CPayload.CODEC);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(SpeedrunActionC2SPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            MinecraftServer server = player.getServer();
            if (server == null) return;

            server.execute(() -> {
                SpeedrunState state = SpeedrunState.get(server);

                switch (payload.action()) {
                    case SpeedrunActionC2SPayload.ACTION_START -> {
                        if (!state.isStarted()) {
                            state.start(server);
                            broadcastMessage(server, Text.translatable("speedrunpp.toast.started"));
                        }
                    }
                    case SpeedrunActionC2SPayload.ACTION_PAUSE -> {
                        if (state.isStarted() && !state.isPaused()) {
                            state.pause(server);
                            broadcastMessage(server, Text.translatable("speedrunpp.toast.paused"));
                        }
                    }
                    case SpeedrunActionC2SPayload.ACTION_RESUME -> {
                        if (state.isStarted() && state.isPaused()) {
                            state.resume(server);
                            broadcastMessage(server, Text.translatable("speedrunpp.toast.resumed"));
                        }
                    }
                    case SpeedrunActionC2SPayload.ACTION_RESET -> {
                        state.reset(server);
                        broadcastMessage(server, Text.translatable("speedrunpp.toast.reset"));
                    }
                }
            });
        });
    }

    public static void syncStateToAll(MinecraftServer server) {
        SpeedrunState state = SpeedrunState.get(server);
        SpeedrunSyncS2CPayload payload = new SpeedrunSyncS2CPayload(
                state.isStarted(),
                state.isPaused(),
                state.getElapsedTicks(server),
                state.getDays(server)
        );

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void syncStateToPlayer(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        SpeedrunState state = SpeedrunState.get(server);
        SpeedrunSyncS2CPayload payload = new SpeedrunSyncS2CPayload(
                state.isStarted(),
                state.isPaused(),
                state.getElapsedTicks(server),
                state.getDays(server)
        );
        ServerPlayNetworking.send(player, payload);
    }

    public static void syncPlayerPositions(MinecraftServer server) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        for (ServerPlayerEntity receiver : players) {
            List<PlayerPositionData> positions = new ArrayList<>();
            for (ServerPlayerEntity other : players) {
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

    private static void broadcastMessage(MinecraftServer server, Text message) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(message, true);
        }
    }
}
