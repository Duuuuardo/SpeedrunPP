package com.speedrunpp.mixin;

import com.speedrunpp.SpeedrunState;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class PlayerMovementMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onPlayerMove", at = @At("HEAD"), cancellable = true)
    private void speedrunpp$preventMovement(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (player.getServer() == null) return;

        SpeedrunState state = SpeedrunState.get(player.getServer());
        if (!state.isRunning()) {
            // Reset player position to prevent desync
            player.networkHandler.requestTeleport(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getYaw(),
                    player.getPitch()
            );
            ci.cancel();
        }
    }
}
