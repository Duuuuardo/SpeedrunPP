package com.speedrunpp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.speedrunpp.network.SpeedrunNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpeedrunState extends SavedData {
    private boolean started = false;
    private boolean paused = false;
    private long startTick = 0;
    private long totalPausedTicks = 0;
    private long pauseStartTick = 0;
    private final Map<UUID, UUID> trackerTargets = new HashMap<>();

    public static final Codec<SpeedrunState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("started").forGetter(s -> s.started),
            Codec.BOOL.fieldOf("paused").forGetter(s -> s.paused),
            Codec.LONG.fieldOf("startTick").forGetter(s -> s.startTick),
            Codec.LONG.fieldOf("totalPausedTicks").forGetter(s -> s.totalPausedTicks),
            Codec.LONG.fieldOf("pauseStartTick").forGetter(s -> s.pauseStartTick)
    ).apply(instance, (started, paused, startTick, totalPausedTicks, pauseStartTick) -> {
        SpeedrunState state = new SpeedrunState();
        state.started = started;
        state.paused = paused;
        state.startTick = startTick;
        state.totalPausedTicks = totalPausedTicks;
        state.pauseStartTick = pauseStartTick;
        return state;
    }));

    public static final SavedDataType<SpeedrunState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("speedrunpp", "state"),
            SpeedrunState::new,
            CODEC,
            null
    );

    public SpeedrunState() {
    }

    public static SpeedrunState get(MinecraftServer server) {
        ServerLevel level = server.overworld();
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isRunning() {
        return started && !paused;
    }

    public long getElapsedTicks(MinecraftServer server) {
        if (!started) return 0;
        long currentTick = server.overworld().getGameTime();
        if (paused) {
            return pauseStartTick - startTick - totalPausedTicks;
        }
        return currentTick - startTick - totalPausedTicks;
    }

    public int getDays(MinecraftServer server) {
        long elapsed = getElapsedTicks(server);
        return (int) (elapsed / 24000);
    }

    public void start(MinecraftServer server) {
        if (started) return;
        started = true;
        paused = false;
        startTick = server.overworld().getGameTime();
        totalPausedTicks = 0;
        pauseStartTick = 0;
        setWorldFrozen(server, false);
        setDirty();
        SpeedrunNetworking.syncStateToAll(server);
    }

    public void pause(MinecraftServer server) {
        if (!started || paused) return;
        paused = true;
        pauseStartTick = server.overworld().getGameTime();
        setWorldFrozen(server, true);
        setDirty();
        SpeedrunNetworking.syncStateToAll(server);
    }

    public void resume(MinecraftServer server) {
        if (!started || !paused) return;
        long currentTick = server.overworld().getGameTime();
        totalPausedTicks += (currentTick - pauseStartTick);
        paused = false;
        pauseStartTick = 0;
        setWorldFrozen(server, false);
        setDirty();
        SpeedrunNetworking.syncStateToAll(server);
    }

    public void reset(MinecraftServer server) {
        started = false;
        paused = false;
        startTick = 0;
        totalPausedTicks = 0;
        pauseStartTick = 0;
        trackerTargets.clear();
        setWorldFrozen(server, true);
        setDirty();
        SpeedrunNetworking.syncStateToAll(server);
    }

    private void setWorldFrozen(MinecraftServer server, boolean frozen) {
        GameRules gameRules = server.getGameRules();
        gameRules.set(GameRules.ADVANCE_TIME, !frozen, server);
        gameRules.set(GameRules.SPAWN_MOBS, !frozen, server);
        gameRules.set(GameRules.ADVANCE_WEATHER, !frozen, server);
        gameRules.set(GameRules.RANDOM_TICK_SPEED, frozen ? 0 : 3, server);
    }

    public UUID getTrackerTarget(UUID playerUuid) {
        return trackerTargets.get(playerUuid);
    }

    public void setTrackerTarget(UUID playerUuid, UUID targetUuid) {
        trackerTargets.put(playerUuid, targetUuid);
    }

    public void removeTrackerTarget(UUID playerUuid) {
        trackerTargets.remove(playerUuid);
    }
}
