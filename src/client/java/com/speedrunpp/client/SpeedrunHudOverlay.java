package com.speedrunpp.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.network.chat.Component;

public class SpeedrunHudOverlay implements HudRenderCallback {

    private static final int TIMER_RUNNING_COLOR = 0xFF55FF55;
    private static final int TIMER_PAUSED_COLOR = 0xFFFFAA00;
    private static final int TIMER_WAITING_COLOR = 0xFFAAAAAA;
    private static final int DAY_COLOR = 0xFFFFFF55;
    private static final int BG_COLOR = 0x88000000;

    @Override
    public void onHudRender(GuiGraphics guiGraphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        Font font = client.font;
        int screenWidth = client.getWindow().getGuiScaledWidth();

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
        int timerWidth = font.width(fullTimerText);
        int timerX = (screenWidth - timerWidth) / 2;
        int timerY = 6;

        int bgPadding = 4;
        guiGraphics.fill(
                timerX - bgPadding,
                timerY - bgPadding,
                timerX + timerWidth + bgPadding,
                timerY + 9 + bgPadding,
                BG_COLOR
        );

        guiGraphics.drawString(font, fullTimerText, timerX, timerY, timerColor, true);

        if (started) {
            int day = SpeedrunClientState.getDays();
            String dayText = Component.translatable("speedrunpp.hud.day", day + 1).getString();
            int dayWidth = font.width(dayText);
            int dayX = (screenWidth - dayWidth) / 2;
            int dayY = timerY + 9 + bgPadding + 2;

            guiGraphics.fill(
                    dayX - bgPadding,
                    dayY - 2,
                    dayX + dayWidth + bgPadding,
                    dayY + 9 + 2,
                    BG_COLOR
            );

            guiGraphics.drawString(font, dayText, dayX, dayY, DAY_COLOR, true);
        }

        if (!started) {
            String waitingText = Component.translatable("speedrunpp.hud.waiting").getString();
            int waitWidth = font.width(waitingText);
            int waitX = (screenWidth - waitWidth) / 2;
            int waitY = timerY + 9 + bgPadding + 4;

            float pulse = (float) (Math.sin(System.currentTimeMillis() / 500.0) * 0.3 + 0.7);
            int alpha = (int) (255 * pulse);
            int pulseColor = (alpha << 24) | 0xFFAA00;

            guiGraphics.drawString(font, waitingText, waitX, waitY, pulseColor, true);
        } else if (paused) {
            String pausedText = Component.translatable("speedrunpp.hud.paused").getString();
            int pauseWidth = font.width(pausedText);
            int pauseX = (screenWidth - pauseWidth) / 2;
            int pauseY = timerY + 9 + bgPadding + 16;

            if ((System.currentTimeMillis() / 700) % 2 == 0) {
                guiGraphics.drawString(font, pausedText, pauseX, pauseY, TIMER_PAUSED_COLOR, true);
            }
        }
    }
}
