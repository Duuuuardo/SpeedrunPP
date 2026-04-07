package com.speedrunpp;

import com.speedrunpp.item.ModItems;
import com.speedrunpp.network.SpeedrunNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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
            ServerPlayerEntity player = handler.getPlayer();
            SpeedrunState state = SpeedrunState.get(server);
            SpeedrunNetworking.syncStateToPlayer(player);

            LOGGER.info("Player {} joined, synced speedrun state (started={}, paused={})",
                    player.getName().getString(), state.isStarted(), state.isPaused());
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (server.getTicks() == 1) {
                SpeedrunState state = SpeedrunState.get(server);
                if (!state.isRunning()) {
                    state.reset(server);
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
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.getVelocity().lengthSquared() > 0.001) {
                    player.setVelocity(0, 0, 0);
                    player.velocityModified = true;
                }
            }
        }

        if (syncTickCounter >= 1200) {
            syncTickCounter = 0;
        }
    }
}
