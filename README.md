# Speedrun++

A Fabric mod for multiplayer speedrun timing in Minecraft. Handles timer display, world freezing, player tracking, and inventory-based controls — all synced across clients.

## Features

- **Speedrun Timer** — HUD timer at the top of the screen showing hours, minutes, seconds, and tenths.
- **Day Counter** — Tracks in-game days since the run started.
- **Inventory Controls** — Start, Pause, Resume, and Reset buttons injected into the inventory screen.
- **World Freeze** — Before a run starts (and while paused), players can't move and the world is fully frozen: no mob spawning, no daylight cycle, no weather, no fire tick, no random ticks.
- **Player Tracker Compass** — Custom item that points toward other players, showing name, distance, and direction in the action bar.
- **Multiplayer Sync** — Server-authoritative state management, synced to all clients in real time.
- **Persistent State** — Run state is saved to the world and survives server restarts.

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.16.0+
- Fabric API 0.102.0+1.21.1
- Java 21

## Building

### Generate the Gradle Wrapper

If you don't have `gradlew` yet:

```
gradle wrapper
```

Or grab the wrapper files from any Fabric template or [the Fabric template generator](https://fabricmc.net/develop/template/).

### Build

```
./gradlew build
```

Output JAR goes to `build/libs/`.

### Dev Client / Server

```
./gradlew runClient
./gradlew runServer
```

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for 1.21.1
2. Drop [Fabric API](https://modrinth.com/mod/fabric-api) into your `mods/` folder
3. Drop `speedrunpp-1.0.0.jar` into `mods/`
4. Launch

## Usage

### Starting a Run

Open your inventory and click **Start** on the right side. The timer begins, the world unfreezes, and all players get notified.

### Pause / Resume

Click **Pause** to freeze everything and stop the timer. Click **Resume** to continue. While paused, players can't move.

### Reset

Click **Reset** to zero out the timer, reset the day counter, and re-freeze the world.

### Player Tracker Compass

Get the item from the Tools creative tab or `/give @s speedrunpp:player_tracker`. Right-click to cycle through online players. Hold it to see the tracked player's name, distance, and direction — updates every second.

## Project Structure

```
src/
├── main/java/com/speedrunpp/
│   ├── SpeedrunMod.java              # Server entrypoint, tick events, player join sync
│   ├── SpeedrunState.java            # PersistentState: timer, pause, world freeze, tracker targets
│   ├── item/
│   │   ├── ModItems.java             # Item registration
│   │   └── PlayerTrackerCompassItem.java  # Compass with player tracking logic
│   ├── network/
│   │   ├── PlayerPositionData.java   # Record for player position data
│   │   ├── SpeedrunNetworking.java   # Payload registration, sync helpers, server receivers
│   │   └── payload/
│   │       ├── SpeedrunActionC2SPayload.java   # Client->Server: start/pause/resume/reset
│   │       ├── SpeedrunSyncS2CPayload.java     # Server->Client: timer state sync
│   │       └── PlayerPositionS2CPayload.java   # Server->Client: player positions for compass
│   └── mixin/
│       └── PlayerMovementMixin.java  # Cancels movement when frozen
├── client/java/com/speedrunpp/
│   ├── client/
│   │   ├── SpeedrunModClient.java    # Client entrypoint, HUD + networking init
│   │   ├── SpeedrunClientState.java  # Client-side state mirror with interpolation
│   │   └── SpeedrunHudOverlay.java   # Timer, day counter, status HUD rendering
│   └── mixin/
│       └── InventoryScreenMixin.java # Injects control buttons into inventory
└── main/resources/
    ├── fabric.mod.json
    ├── speedrunpp.mixins.json
    └── assets/speedrunpp/
        ├── lang/
        │   ├── en_us.json
        │   └── pt_br.json
        └── models/item/
            └── player_tracker.json
```

## How It Works

### Server Side

`SpeedrunState` extends `PersistentState` and lives per-world. It tracks whether the run has started, is paused, the start tick, total paused ticks, and compass tracker targets.

World freeze toggles game rules (`doDaylightCycle`, `doMobSpawning`, `doFireTick`, `doWeatherCycle`, `randomTickSpeed`) and cancels player movement via mixin.

Timer state syncs every 10 ticks (0.5s). Player positions sync every 20 ticks (1s). Players joining mid-run get an immediate sync.

### Client Side

`SpeedrunClientState` mirrors server state with client-side interpolation for smooth timer display between syncs.

`SpeedrunHudOverlay` renders the timer with color-coded states (green = running, orange = paused, gray = waiting) and visual effects like pulsing and blinking.

`InventoryScreenMixin` injects the control buttons and sends action packets to the server.

### Networking

All state is server-authoritative. Clients send action requests, the server validates them and broadcasts updates.

| Packet | Direction | Purpose |
|--------|-----------|---------|
| `speedrun_action` | Client -> Server | Request start/pause/resume/reset |
| `speedrun_sync` | Server -> Client | Broadcast timer state |
| `player_positions` | Server -> Client | Broadcast player positions for compass |

## Mod Icon

To add one, place an `icon.png` (128x128 or 256x256) at:

```
src/main/resources/assets/speedrunpp/icon.png
```

## License

MIT