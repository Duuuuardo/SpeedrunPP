package com.speedrunpp;

import com.speedrunpp.network.SpeedrunNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpeedrunState extends PersistentState {
    private boolean started = false;
    private boolean paused = false;
    private long startTick = 0;
    private long totalPausedTicks = 0;
    private long pauseStartTick = 0;

    private final Map<UUID, UUID> trackerTargets = new HashMap<>();

    public SpeedrunState() {
    }

    public static SpeedrunState get(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        return world.getPersistentStateManager().getOrCreate(
                new Type<>(SpeedrunState::new, SpeedrunState::fromNbt, null),
                "speedrunpp_state"
        );
    }

    public static SpeedrunState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        SpeedrunState state = new SpeedrunState();
        state.started = nbt.getBoolean("started");
        state.paused = nbt.getBoolean("paused");
        state.startTick = nbt.getLong("startTick");
        state.totalPausedTicks = nbt.getLong("totalPausedTicks");
        state.pauseStartTick = nbt.getLong("pauseStartTick");
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putBoolean("started", started);
        nbt.putBoolean("paused", paused);
        nbt.putLong("startTick", startTick);
        nbt.putLong("totalPausedTicks", totalPausedTicks);
        nbt.putLong("pauseStartTick", pauseStartTick);
        return nbt;
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
        long currentTick = server.getOverworld().getTime();
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
        startTick = server.getOverworld().getTime();
        totalPausedTicks = 0;
        pauseStartTick = 0;
        setWorldFrozen(server, false);
        markDirty();
        SpeedrunNetworking.syncStateToAll(server);
    }

    public void pause(MinecraftServer server) {
        if (!started || paused) return;
        paused = true;
        pauseStartTick = server.getOverworld().getTime();
        setWorldFrozen(server, true);
        markDirty();
        SpeedrunNetworking.syncStateToAll(server);
    }

    public void resume(MinecraftServer server) {
        if (!started || !paused) return;
        long currentTick = server.getOverworld().getTime();
        totalPausedTicks += (currentTick - pauseStartTick);
        paused = false;
        pauseStartTick = 0;
        setWorldFrozen(server, false);
        markDirty();
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
        markDirty();
        SpeedrunNetworking.syncStateToAll(server);
    }

    private void setWorldFrozen(MinecraftServer server, boolean frozen) {
        GameRules gameRules = server.getGameRules();
        gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(!frozen, server);
        gameRules.get(GameRules.DO_MOB_SPAWNING).set(!frozen, server);
        gameRules.get(GameRules.DO_FIRE_TICK).set(!frozen, server);
        gameRules.get(GameRules.DO_WEATHER_CYCLE).set(!frozen, server);
        gameRules.get(GameRules.RANDOM_TICK_SPEED).set(frozen ? 0 : 3, server);
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
