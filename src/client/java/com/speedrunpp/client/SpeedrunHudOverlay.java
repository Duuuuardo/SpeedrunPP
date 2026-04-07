package com.speedrunpp.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class SpeedrunHudOverlay implements HudRenderCallback {

    private static final int TIMER_RUNNING_COLOR = 0xFF55FF55;
    private static final int TIMER_PAUSED_COLOR = 0xFFFFAA00;
    private static final int TIMER_WAITING_COLOR = 0xFFAAAAAA;
    private static final int DAY_COLOR = 0xFFFFFF55;
    private static final int BG_COLOR = 0x88000000;

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();

        boolean started = SpeedrunClientState.isStarted();
        boolean paused = SpeedrunClientState.isPaused();

        String timeStr = SpeedrunClientState.formatTime(SpeedrunClientState.getElapsedTicks());
        int timerColor;
        String statusPrefix = "";

        if (!started) {
            timerColor = TIMER_WAITING_COLOR;
            timeStr = "00:00.0";
        } else if (paused) {
            timerColor = TIMER_PAUSED_COLOR;
            if ((System.currentTimeMillis() / 500) % 2 == 0) {
                statusPrefix = "|| ";
            }
        } else {
            timerColor = TIMER_RUNNING_COLOR;
            statusPrefix = "> ";
        }

        String fullTimerText = statusPrefix + timeStr;
        int timerWidth = textRenderer.getWidth(fullTimerText);
        int timerX = (screenWidth - timerWidth) / 2;
        int timerY = 6;

        int bgPadding = 4;
        drawContext.fill(
                timerX - bgPadding,
                timerY - bgPadding,
                timerX + timerWidth + bgPadding,
                timerY + 9 + bgPadding,
                BG_COLOR
        );

        drawContext.drawText(textRenderer, fullTimerText, timerX, timerY, timerColor, true);

        if (started) {
            int day = SpeedrunClientState.getDays();
            String dayText = Text.translatable("speedrunpp.hud.day", day + 1).getString();
            int dayWidth = textRenderer.getWidth(dayText);
            int dayX = (screenWidth - dayWidth) / 2;
            int dayY = timerY + 9 + bgPadding + 2;

            drawContext.fill(
                    dayX - bgPadding,
                    dayY - 2,
                    dayX + dayWidth + bgPadding,
                    dayY + 9 + 2,
                    BG_COLOR
            );

            drawContext.drawText(textRenderer, dayText, dayX, dayY, DAY_COLOR, true);
        }

        if (!started) {
            String waitingText = Text.translatable("speedrunpp.hud.waiting").getString();
            int waitWidth = textRenderer.getWidth(waitingText);
            int waitX = (screenWidth - waitWidth) / 2;
            int waitY = timerY + 9 + bgPadding + 4;

            float pulse = (float) (Math.sin(System.currentTimeMillis() / 500.0) * 0.3 + 0.7);
            int alpha = (int) (255 * pulse);
            int pulseColor = (alpha << 24) | 0xFFAA00;

            drawContext.drawText(textRenderer, waitingText, waitX, waitY, pulseColor, true);
        } else if (paused) {
            String pausedText = Text.translatable("speedrunpp.hud.paused").getString();
            int pauseWidth = textRenderer.getWidth(pausedText);
            int pauseX = (screenWidth - pauseWidth) / 2;
            int pauseY = timerY + 9 + bgPadding + 16;

            if ((System.currentTimeMillis() / 700) % 2 == 0) {
                drawContext.drawText(textRenderer, pausedText, pauseX, pauseY, TIMER_PAUSED_COLOR, true);
            }
        }
    }
}
