package com.speedrunpp.client;

import com.speedrunpp.network.payload.PlayerPositionS2CPayload;
import com.speedrunpp.network.payload.SpeedrunSyncS2CPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class SpeedrunModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(new SpeedrunHudOverlay());

        ClientPlayNetworking.registerGlobalReceiver(SpeedrunSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                SpeedrunClientState.updateState(
                        payload.started(),
                        payload.paused(),
                        payload.elapsedTicks(),
                        payload.days()
                );
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PlayerPositionS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                SpeedrunClientState.updatePlayerPositions(payload.positions());
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            SpeedrunClientState.reset();
        });
    }
}
