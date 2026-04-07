package com.speedrunpp.client;

import com.speedrunpp.network.PlayerPositionData;

import java.util.ArrayList;
import java.util.List;

public class SpeedrunClientState {
    private static boolean started = false;
    private static boolean paused = false;
    private static long elapsedTicks = 0;
    private static int days = 0;
    private static List<PlayerPositionData> playerPositions = new ArrayList<>();

    private static long lastSyncTime = 0;
    private static long lastSyncTicks = 0;

    public static boolean isStarted() {
        return started;
    }

    public static boolean isPaused() {
        return paused;
    }

    public static boolean isRunning() {
        return started && !paused;
    }

    public static long getElapsedTicks() {
        if (isRunning()) {
            long now = System.currentTimeMillis();
            long ticksSinceSync = (now - lastSyncTime) / 50; // 50ms per tick
            return lastSyncTicks + ticksSinceSync;
        }
        return elapsedTicks;
    }

    public static int getDays() {
        return days;
    }

    public static List<PlayerPositionData> getPlayerPositions() {
        return playerPositions;
    }

    public static void updateState(boolean started, boolean paused, long elapsedTicks, int days) {
        SpeedrunClientState.started = started;
        SpeedrunClientState.paused = paused;
        SpeedrunClientState.elapsedTicks = elapsedTicks;
        SpeedrunClientState.days = days;
        SpeedrunClientState.lastSyncTime = System.currentTimeMillis();
        SpeedrunClientState.lastSyncTicks = elapsedTicks;
    }

    public static void updatePlayerPositions(List<PlayerPositionData> positions) {
        SpeedrunClientState.playerPositions = new ArrayList<>(positions);
    }

    public static void reset() {
        started = false;
        paused = false;
        elapsedTicks = 0;
        days = 0;
        playerPositions.clear();
        lastSyncTime = 0;
        lastSyncTicks = 0;
    }

    public static String formatTime(long ticks) {
        long totalSeconds = ticks / 20;
        long millis = (ticks % 20) * 50;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d.%01d", hours, minutes, seconds, millis / 100);
        }
        return String.format("%02d:%02d.%01d", minutes, seconds, millis / 100);
    }
}
