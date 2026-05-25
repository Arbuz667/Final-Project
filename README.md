# 🌀 Soft Chaos

> A fast-paced roguelike survivor built with **Java 21 + libGDX 1.14**.  
> Fight through three locations, level up, collect weapons, and take down the boss of each world.

---

## 📋 Table of Contents

- [Gameplay Overview](#gameplay-overview)
- [Controls](#controls)
- [Locations & Bosses](#locations--bosses)
- [Characters](#characters)
- [Weapons](#weapons)
- [Progression Systems](#progression-systems)
- [Enemy AI](#enemy-ai)
- [Design Patterns](#design-patterns)
- [Project Structure](#project-structure)
- [Building & Running](#building--running)

---

## Gameplay Overview

Soft Chaos is a **top-down wave survival** game. Enemies spawn continuously and swarm toward the player. Survive the wave timer, defeat the location boss, and advance to the next world. Survive all three locations to win.

**Core loop:**
1. Pick a character and starting weapon
2. Survive the 2-minute wave — enemies grow stronger over time
3. Level up → choose weapon upgrades
4. Open chests dropped by enemies → gain new weapons
5. Wave ends → boss spawns → defeat it to advance
6. Clear all 3 locations → **Victory**

**Key survival mechanics:**
- **HP regeneration** — 1 HP every 2 seconds passively
- **Invincibility frames** — 1 second of immunity after taking a hit
- **Knockback** — explosive weapons push enemies away
- **Damage scaling** — enemies deal +10% damage every 30 seconds

---

## Controls

| Key / Input | Action |
|---|---|
| `W` `A` `S` `D` | Move |
| `Mouse` | Aim (weapons auto-fire toward cursor) |
| `ESC` | Pause / Unpause |
| `TAB` | Instant return to Main Menu |
| `Enter` | Confirm (pause menu) |
| `M` | Quit to Main Menu (pause menu) |

---

## Locations & Bosses

| # | Location | Enemies | Boss |
|---|---|---|---|
| 1 | 🌲 **Forest** | Aliens, Rabbids, Snipers | **Slenderman** *(2-frame animation, no minions)* |
| 2 | 🌊 **Ocean** | Sharks, Jellyfish, Snipers | **Megalodon** *(spawns 8 sharks on ability)* |
| 3 | 🚀 **Space** | Robots, Zombies, Snipers | **Pickle Rick** *(spawns robots + zombies; surrounded by 6 snipers on arrival)* |

Each location has its own background music and escalating enemy difficulty. Bosses have wider HP bars and significantly more health than regular enemies.

---

## Characters

Three playable characters, each with a unique sprite and walk animation:

| # | Character | Animation Frames |
|---|---|---|
| 0 | **Warrior** | 3 frames |
| 1 | **Elf** | 3 frames |
| 2 | **Witch** | 2 frames |

Character selection persists through the run and is stored in `GameStateManager`.

---

## Weapons

All weapon stats are loaded from `data/weapons.json`. The player can hold up to **4 weapons** simultaneously.

| Weapon | Type | Notes |
|---|---|---|
| **Sword** | Melee swing arc | Close-range, high DPS, piercing arc |
| **Iron Bow** | Ranged bullet | Arrow projectile, rotates to face direction |
| **Diary** | Magic | Fireball with +180° texture correction |
| **Desert Eagles** | Ranged bullet | Dual pistols |
| **Shurikens** | Ranged spinning | Rotates to velocity angle |
| **Minigun** | Rapid fire | High fire rate, small bullets |
| **Potato Thrower** | Explosive | AOE knockback on impact |
| **Nuclear Bazooka** | Explosive rocket | Large AOE explosion radius |
| **Six Seven** | Cringe aura | Passive — builds **Cringe Meter** on nearby enemies; one-shots at 100% |

### Weapon Rarities

`COMMON` → `RARE` → `EPIC` → `LEGENDARY`

Rarity determines the accent color on the inventory slot and the upgrade pool weight.

### Upgrades

On level-up, 3 random upgrade cards are shown for a randomly selected weapon. Each upgrade can be picked multiple times (max depends on rarity: Legendary = 1, Common = 5). Upgrades modify `damage`, `cooldown`, `piercing`, `projectileCount`, `projectileSpeed`, or `explosionRadius`.

---

## Progression Systems

### XP & Leveling
- Every enemy kill grants XP proportional to enemy type
- Level threshold: `level × 100 XP`
- On level-up → upgrade screen appears for a random weapon

### Chests
- Enemies have a configurable **chest drop chance**
- Walking over a chest opens it
- If inventory is full → converts to a weapon upgrade instead of a new weapon

### Wave Timer
- Each location runs a **120-second countdown**
- Timer hits zero → all regular enemies cleared → **boss spawns**
- Boss music plays during boss phase

### Damage Scaling
- Enemy damage multiplier increases by `+0.1×` every 30 seconds
- Spawn interval decreases over time (floor: 0.15s)
- Every 25 regular enemies, a ranged **Sniper** is forced to spawn

---

## Enemy AI

Each enemy has a swappable `EnemyAI` strategy component:

| Strategy | Behavior |
|---|---|
| `ChaseAI` | Moves directly toward player at own speed |
| `SwarmAI` | Moves toward player while maintaining swarm cohesion |
| `RangedAI` | Keeps distance, fires projectiles at player |
| `TankAI` | Slow but high HP; direct charge |
| `SlendermanAI` | Forest boss — teleport-like movement |
| `MegalodonAI` | Ocean boss — charges + spawns shark minions |
| `PickleRickAI` | Space boss — erratic movement + spawns robot/zombie minions |

Bosses override the base AI update loop to implement multi-phase behavior.

---

## Design Patterns

| Pattern | Where | Why |
|---|---|---|
| **Singleton** | `GameStateManager`, `AudioManager` | Global state (location, kills, HP, volume) accessible from any screen without passing references |
| **Observer** | `XPSystem.XPListener`, `WaveManager.WaveListener`, `ChestSystem.ChestListener` | Systems notify `GameScreen` of events (level-up, wave end, chest opened) without direct coupling |
| **Strategy** | `EnemyAI` interface + 7 implementations | Each enemy type has a different movement/attack algorithm swappable at runtime |
| **Template Method** | `Weapon` abstract class | Common cooldown logic in base class; `fire()` is abstract — each weapon defines its own projectile behavior |
| **Object Pool** | `EnemySpawner` — `Pool<Enemy>` | Hundreds of enemies spawn and die per minute; pool reuses objects to eliminate GC pauses |
| **Command** | `pendingTransition` Runnable in `GameScreen` | Screen transitions are deferred until the fade-out animation completes, preventing mid-render state changes |
| **State** | libGDX `Screen` interface + `isPaused` / `fadingOut` flags | Each screen is an isolated state; `update()` behavior changes completely based on current flags |

---

## Project Structure

```
core/src/main/java/com/softchaos/
├── SoftChaosGame.java          # Application entry point
├── ai/                         # Strategy pattern — enemy AI implementations
│   ├── EnemyAI.java            #   Strategy interface
│   ├── ChaseAI.java
│   ├── SwarmAI.java
│   ├── RangedAI.java
│   ├── TankAI.java
│   ├── SlendermanAI.java
│   ├── MegalodonAI.java
│   └── PickleRickAI.java
├── config/                     # JSON data loaders (weapons, enemies, upgrades)
├── entities/                   # Player, Enemy, Boss, Projectile, Chest
├── managers/
│   ├── GameStateManager.java   # Singleton — global run state
│   ├── AudioManager.java       # Singleton — music/SFX with 1s fade
│   └── UIManager.java
├── screens/                    # State pattern — one class per game screen
│   ├── MainMenuScreen.java
│   ├── CharacterSelectScreen.java
│   ├── WeaponSelectScreen.java
│   ├── GameScreen.java         # Core gameplay loop
│   ├── UpgradeScreen.java
│   ├── ChestScreen.java
│   ├── WinScreen.java
│   ├── GameOverScreen.java
│   └── SettingsScreen.java
├── systems/
│   ├── XPSystem.java           # Observer — fires onLevelUp
│   ├── WaveManager.java        # Observer — fires onWaveEnd / onBossKilled
│   ├── ChestSystem.java        # Observer — fires onChestOpened + Object Pool
│   ├── EnemySpawner.java       # Object Pool — Pool<Enemy>
│   └── UpgradeSystem.java
├── weapons/                    # Template Method — Weapon base + subclasses
│   ├── Weapon.java             #   Abstract base
│   ├── Sword.java
│   ├── Bow.java
│   ├── Minigun.java
│   ├── NuclearBazooka.java
│   ├── PotatoThrower.java
│   ├── Shurikens.java
│   ├── DesertEagles.java
│   ├── Diary.java
│   ├── WeaponSystem.java
│   └── cringe/
│       └── SixSeven.java       # Passive aura weapon with CringeMeter
└── utils/                      # Enums and Constants
```

---

## Building & Running

**Requirements:** JDK 17+, no other installation needed (Gradle wrapper included).

```bash
# Run the game
.\gradlew.bat lwjgl3:run

# Build a runnable JAR
.\gradlew.bat lwjgl3:jar
# Output: lwjgl3/build/libs/

# Clean build outputs
.\gradlew.bat clean
```
