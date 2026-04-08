package com.speedrunpp.mixin;

import com.speedrunpp.client.SpeedrunClientState;
import com.speedrunpp.network.payload.SpeedrunActionC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {

    @Unique
    private Button startButton;
    @Unique
    private Button pauseButton;
    @Unique
    private Button resumeButton;
    @Unique
    private Button resetButton;

    private InventoryScreenMixin() {
        super(null, null, null);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void speedrunpp$addButtons(CallbackInfo ci) {
        int buttonWidth = 70;
        int buttonHeight = 20;
        int startX = this.leftPos + this.imageWidth + 4;
        int startY = this.topPos + 4;
        int gap = 24;

        startButton = Button.builder(
                        Component.translatable("speedrunpp.button.start").withStyle(ChatFormatting.GREEN),
                        button -> {
                            ClientPlayNetworking.send(new SpeedrunActionC2SPayload(SpeedrunActionC2SPayload.ACTION_START));
                        })
                .bounds(startX, startY, buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(startButton);

        pauseButton = Button.builder(
                        Component.translatable("speedrunpp.button.pause").withStyle(ChatFormatting.YELLOW),
                        button -> {
                            ClientPlayNetworking.send(new SpeedrunActionC2SPayload(SpeedrunActionC2SPayload.ACTION_PAUSE));
                        })
                .bounds(startX, startY, buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(pauseButton);

        resumeButton = Button.builder(
                        Component.translatable("speedrunpp.button.resume").withStyle(ChatFormatting.GREEN),
                        button -> {
                            ClientPlayNetworking.send(new SpeedrunActionC2SPayload(SpeedrunActionC2SPayload.ACTION_RESUME));
                        })
                .bounds(startX, startY, buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(resumeButton);

        resetButton = Button.builder(
                        Component.translatable("speedrunpp.button.reset").withStyle(ChatFormatting.RED),
                        button -> {
                            ClientPlayNetworking.send(new SpeedrunActionC2SPayload(SpeedrunActionC2SPayload.ACTION_RESET));
                        })
                .bounds(startX, startY + gap, buttonWidth, buttonHeight)
                .build();
        this.addRenderableWidget(resetButton);

        speedrunpp$updateButtonVisibility();
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void speedrunpp$updateButtons(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
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
