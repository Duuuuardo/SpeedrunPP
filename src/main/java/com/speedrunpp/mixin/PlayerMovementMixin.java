package com.speedrunpp.mixin;

import com.speedrunpp.SpeedrunState;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class PlayerMovementMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void speedrunpp$preventMovement(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (player.level().getServer() == null) return;

        SpeedrunState state = SpeedrunState.get(player.level().getServer());
        if (!state.isRunning()) {
            // Reset player position to prevent desync
            player.connection.teleport(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getYRot(),
                    player.getXRot()
            );
            ci.cancel();
        }
    }
}
