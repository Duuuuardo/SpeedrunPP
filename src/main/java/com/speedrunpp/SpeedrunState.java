package com.speedrunpp;

import com.speedrunpp.network.SpeedrunNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.saveddata.SavedData;

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

    public SpeedrunState() {
    }

    public static SpeedrunState get(MinecraftServer server) {
        ServerLevel level = server.overworld();
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(SpeedrunState::new, SpeedrunState::load),
                "speedrunpp_state"
        );
    }

    public static SpeedrunState load(CompoundTag tag, HolderLookup.Provider registries) {
        SpeedrunState state = new SpeedrunState();
        state.started = tag.getBoolean("started");
        state.paused = tag.getBoolean("paused");
        state.startTick = tag.getLong("startTick");
        state.totalPausedTicks = tag.getLong("totalPausedTicks");
        state.pauseStartTick = tag.getLong("pauseStartTick");
        return state;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("started", started);
        tag.putBoolean("paused", paused);
        tag.putLong("startTick", startTick);
        tag.putLong("totalPausedTicks", totalPausedTicks);
        tag.putLong("pauseStartTick", pauseStartTick);
        return tag;
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
        gameRules.getRule(GameRules.RULE_DAYLIGHT).set(!frozen, server);
        gameRules.getRule(GameRules.RULE_DOMOBSPAWNING).set(!frozen, server);
        gameRules.getRule(GameRules.RULE_DOFIRETICK).set(!frozen, server);
        gameRules.getRule(GameRules.RULE_WEATHER_CYCLE).set(!frozen, server);
        gameRules.getRule(GameRules.RULE_RANDOMTICKING).set(frozen ? 0 : 3, server);
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
