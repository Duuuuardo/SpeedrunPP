package com.speedrunpp;

import com.speedrunpp.item.ModItems;
import com.speedrunpp.network.SpeedrunNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpeedrunMod implements ModInitializer {
    public static final String MOD_ID = "speedrunpp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private int syncTickCounter = 0;

    @Override
    public void onInitialize() {
        ModItems.register();
        SpeedrunNetworking.registerPayloads();
        SpeedrunNetworking.registerServerReceivers();

        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            SpeedrunState state = SpeedrunState.get(server);
            SpeedrunNetworking.syncStateToPlayer(player);

            LOGGER.info("Player {} joined, synced speedrun state (started={}, completed={})",
                    player.getName().getString(), state.isStarted(), state.isCompleted());
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (server.getTickCount() == 1) {
                SpeedrunState state = SpeedrunState.get(server);
                if (!state.isRunning()) {
                    state.reset(server);
                }
            }
        });

        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
            if (origin.dimension() == Level.END && destination.dimension() == Level.OVERWORLD) {
                var server = destination.getServer();
                if (server != null) {
                    SpeedrunState state = SpeedrunState.get(server);
                    if (state.isRunning()) {
                        state.complete(server);
                        for (var p : server.getPlayerList().getPlayers()) {
                            p.sendSystemMessage(Component.translatable("speedrunpp.toast.completed"), true);
                        }
                    }
                }
            }
        });
    }

    private void onServerTick(MinecraftServer server) {
        syncTickCounter++;

        SpeedrunState state = SpeedrunState.get(server);

        if (syncTickCounter % 10 == 0) {
            SpeedrunNetworking.syncStateToAll(server);
        }

        if (syncTickCounter % 20 == 0) {
            SpeedrunNetworking.syncPlayerPositions(server);
        }

        if (!state.isRunning()) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.getDeltaMovement().lengthSqr() > 0.001) {
                    player.setDeltaMovement(0, 0, 0);
                    player.hurtMarked = true;
                }
            }
        }

        if (syncTickCounter >= 1200) {
            syncTickCounter = 0;
        }
    }
}
