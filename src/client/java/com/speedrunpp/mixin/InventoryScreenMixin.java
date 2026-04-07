package com.speedrunpp.mixin;

import com.speedrunpp.client.SpeedrunClientState;
import com.speedrunpp.network.payload.SpeedrunActionC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends net.minecraft.client.gui.screen.ingame.HandledScreen<net.minecraft.screen.PlayerScreenHandler> {

    @Unique
    private ButtonWidget startButton;
    @Unique
    private ButtonWidget pauseButton;
    @Unique
    private ButtonWidget resumeButton;
    @Unique
    private ButtonWidget resetButton;

    private InventoryScreenMixin() {
        super(null, null, null);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void speedrunpp$addButtons(CallbackInfo ci) {
        int buttonWidth = 70;
        int buttonHeight = 20;
        int startX = this.x + this.backgroundWidth + 4;
        int startY = this.y + 4;
        int gap = 24;

        startButton = ButtonWidget.builder(
                        Text.translatable("speedrunpp.button.start").formatted(Formatting.GREEN),
                        button -> {
                            ClientPlayNetworking.send(new SpeedrunActionC2SPayload(SpeedrunActionC2SPayload.ACTION_START));
                        })
                .dimensions(startX, startY, buttonWidth, buttonHeight)
                .build();
        this.addDrawableChild(startButton);

        pauseButton = ButtonWidget.builder(
                        Text.translatable("speedrunpp.button.pause").formatted(Formatting.YELLOW),
                        button -> {
                            ClientPlayNetworking.send(new SpeedrunActionC2SPayload(SpeedrunActionC2SPayload.ACTION_PAUSE));
                        })
                .dimensions(startX, startY, buttonWidth, buttonHeight)
                .build();
        this.addDrawableChild(pauseButton);

        resumeButton = ButtonWidget.builder(
                        Text.translatable("speedrunpp.button.resume").formatted(Formatting.GREEN),
                        button -> {
                            ClientPlayNetworking.send(new SpeedrunActionC2SPayload(SpeedrunActionC2SPayload.ACTION_RESUME));
                        })
                .dimensions(startX, startY, buttonWidth, buttonHeight)
                .build();
        this.addDrawableChild(resumeButton);

        resetButton = ButtonWidget.builder(
                        Text.translatable("speedrunpp.button.reset").formatted(Formatting.RED),
                        button -> {
                            ClientPlayNetworking.send(new SpeedrunActionC2SPayload(SpeedrunActionC2SPayload.ACTION_RESET));
                        })
                .dimensions(startX, startY + gap, buttonWidth, buttonHeight)
                .build();
        this.addDrawableChild(resetButton);

        speedrunpp$updateButtonVisibility();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void speedrunpp$updateButtons(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        speedrunpp$updateButtonVisibility();
    }

    @Unique
    private void speedrunpp$updateButtonVisibility() {
        boolean started = SpeedrunClientState.isStarted();
        boolean paused = SpeedrunClientState.isPaused();

        if (startButton != null) {
            startButton.visible = !started;
        }
        if (pauseButton != null) {
            pauseButton.visible = started && !paused;
        }
        if (resumeButton != null) {
            resumeButton.visible = started && paused;
        }
        if (resetButton != null) {
            resetButton.visible = started;
        }
    }
}
