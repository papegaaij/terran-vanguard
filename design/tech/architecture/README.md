---
title: Architecture
design: approved
implementation: in-progress
art: n/a
depends-on: [.., ../../player, ../../enemies, ../../campaign]
updated: 2026-10-30
---

# Architecture

## Summary

How the game code is organised: a Gradle multi-project build at the repository root with a
pure-Java simulation, a content loader, the libGDX presentation layer, the desktop launcher and
an asset pipeline. Game numbers live in data files next to the design documents that describe
them, and the documents' tables are rendered from those files, so the design tree stays the
single source of truth. Everything is tested headless; CI builds and tests on Linux, Windows
and macOS.

## Contents

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [schemas-player](schemas-player/README.md) | Data file schemas: player and economy | approved | in-progress | n/a |
| [schemas-units](schemas-units/README.md) | Data file schemas: enemies and allies | approved | in-progress | n/a |
| [schemas-levels](schemas-levels/README.md) | Data file schemas: levels and acts | approved | in-progress | n/a |
| [schemas-act-4](schemas-act-4/README.md) | Data file schemas: Act 4 | approved | in-progress | n/a |
| [schemas-backdrops](schemas-backdrops/README.md) | Data file schemas: level backdrops | approved | in-progress | n/a |
| [schemas-voice](schemas-voice/README.md) | Data file schemas: voice | approved | in-progress | n/a |

## Design

### Repository layout

```
build.gradle.kts, settings.gradle.kts, gradlew   Gradle build at the root (wrapper, version catalog)
buildSrc/        shared Java conventions (Java 21, compiler flags, tests, formatting)
sim/             pure-Java simulation — no libGDX
content/         data model (records) and the loader for the data files — no libGDX
game/            libGDX presentation: screens, rendering, audio, input, UI kit
desktop/         LWJGL3 launcher, settings file, Construo packaging
pipeline/        build-time asset tools: angle sets, atlas packing, audio conversion
assets/          production source art and audio (Git LFS); packed into build output
design/          the design tree, including the data files (see below)
tools/           Python concept-art generators and doc tools (unchanged)
```

Dependencies only point downwards: `desktop → game → content → sim`; `pipeline` is used by the
build only. Gradle enforces it: `sim` and `content` cannot see libGDX. Root package `vanguard`
(`vanguard.sim`, `vanguard.content`, `vanguard.game`, `vanguard.desktop`, `vanguard.pipeline`).

### What carries over from the spike

| Spike part | In the game |
|---|---|
| `buildSrc` conventions, version catalog, wrapper | as is, moved to the root |
| `FixedStepClock`, `SplitMix64`, `Trig`, `Pool`, `StateHash`, `InputRecording` | `sim`, as the foundation |
| `PixelScreen`, `DisplayModes` | `game`, extended with sharp-bilinear and settings |
| `MusicStreamer`, `LoopingStream`, `VorbisFile`, `SfxBank` | `game/audio` |
| `PlayerInput` | rewritten as input actions with remapping ([controls](../../ui/controls/README.md)) |
| `AngleSetGenerator` | `pipeline`, extended with symmetry steps ([art direction](../../art-direction/README.md)) |
| Benchmark scenes, `World`, `Autopilot`, procedural art | not carried over; the benchmark mode returns as a `--bench` option |
| CI workflow, Construo setup | as is, extended with release builds |

The spike is never merged; it is kept for reference as the tag `spike-libgdx`.

### Simulation (`sim`)

- **Fixed 60 Hz step**; the renderer interpolates between the last two states.
- **Deterministic**: seeded `SplitMix64` per level and difficulty, table-based trigonometry
  filled from `StrictMath`, no wall-clock time, no `HashMap` iteration order in logic.
- **No allocation per step**: entities, bullets and effects live in pools; a unit test measures
  allocation per step, as in the spike.
- Owns the rules: movement and formations, layers and layer hit rules, weapons and projectiles,
  collisions, shield/armour, pickups, scoring, credits, objectives, the level script runner.
- Emits **events** (shot fired, hit, explosion, radio cue, objective changed) that the
  presentation turns into sound, effects and HUD changes; `sim` never calls the presentation.
- The campaign state (credits, loadout, unlocks, difficulty) is plain Java and serialisable for
  save slots ([saves](../../systems/saves/README.md)).

### Content: data files next to the documents

The documents describe every number in tables; the code needs them as data. To keep one source,
the numbers live in **YAML data files inside the part's directory** (`data.yaml` next to the
README), and the tables in the README are **rendered from the data**:

| Part | Data file | Rendered into |
|---|---|---|
| Enemy | `design/enemies/<category>/<slug>/data.yaml` (stat block) | its *Stat block* table |
| Enemy basis | `design/enemies/data.yaml` (reference DPS, damage to the player, formation vocabulary) | the *Balancing basis* and *Formation vocabulary* tables |
| Weapon | `design/player/weapons/<slug>/data.yaml`; shared rules in `design/player/weapons/data.yaml` | its property and *Per level* tables |
| Ship and core parts | `design/player/{ship,shields,armor,generator,systems,specials}/data.yaml` | their tables (the specials table is still hand-written) |
| Player-wide | `design/player/data.yaml` (shop availability, pickups) | the *In-level pickups* table |
| Level | `design/campaign/<act>/<level>/data.yaml` (sections, scroll, waves, ground targets, secrets, cues, objectives, music, difficulty changes, backdrop) | the *Threat profile*, *Layout*, *Backdrop*, *Waves*, *Ground targets*, *Radio chatter* and *Credit budget* tables |
| Act | `design/campaign/<act>/data.yaml` (the act's levels, title card, act briefing and act-end outro) | the act's *Act intro and outro* quotes and the outro's page table (`act-outro-pages`, M6 part D) |
| Allies | `design/allies/data.yaml` (one entry per ally) | the allies' spec tables (still hand-written) |
| Wingmen | `design/player/wingmen/data.yaml` (Rook, his guns, his barks) | the guns and barks tables (`rook-guns`, `rook-barks`, M6 part D) |
| Economy, scoring, difficulty, retry | `design/systems/<part>/data.yaml` | the scoring bonus and grade tables and the difficulty levers (the economy's tables are still hand-written; retry has no table) |

- **Marked tables.** A generated table sits between `<!-- data: NAME -->` and `<!-- /data -->`;
  NAME picks a renderer in `tools/sync_tables.py`, which reads the `data.yaml` in the README's
  own directory (and values it derives from other files, such as a level's bounties).
  `python3 tools/sync_tables.py` rewrites the marked tables, `check_docs.py` fails when one
  differs from its data and `--fix` re-renders it. Everything outside the markers is hand-written.
- **README wording** that a table needs but the game does not (descriptions, purposes, notes)
  sits under a `notes` key, in the file or in a row. A note may contain `{field}` placeholders
  (Python format fields; `{a.b}` reaches into a nested value, a `[min, max]` pair prints as
  `min–max`), so a number inside a sentence still has one source. The game ignores `notes`.
- **The expected purchases** of a typical player per hangar visit are not a rule: they live in
  `design/player/balance-plan.yaml`, read by the content tests `BalanceTest` and
  `ActPlaythroughTest` and printed by `tools/balance.py`. Planned (M5 part D, D7 = a): a
  visit's **`fit`** action (`[slot, item]`: fits an item already owned, without buying it; Level
  10's visit refits Rook's Autocannon, `[escort, autocannon]`) and `BalanceTest`'s **rear check**
  (the Wraith's HP ÷ the plan's rear DPS at most 0.6 × its decloak flash + hold at medium).
- **Loading.** `content` reads the files with Jackson 3 (YAML) into Java records, one per file
  (`ShipData`, `EnemyData`, `LevelData`, …), gathered in `Content` by `ContentLoader`. A level's
  nested records are declared per data block in sealed `*Block` interfaces that `LevelData`
  implements (`WavesBlock`, `RadioBlock`, `ObjectivesBlock`, …), so they are still named
  `LevelData.Wave` and so on; a new block's records go in its block file. Every
  record component is required unless its type is `Optional`; unknown keys are rejected; ranges
  are checked in the records' compact constructors; `ContentValidator` checks across files
  (a wave's enemy and formation exist, an attack's bullet class exists, availability keys
  resolve, starters come first, times and sections fit the level). All problems are collected
  and reported together as `design/<file>:<line>: <field path>: <problem>`.
- **Run time.** The build copies `design/**/data.yaml` with their paths into the `content`
  resources under `design/`, plus an index `design/data-files.txt` (task `:content:designData`);
  the game calls `ContentLoader.fromClasspath()` once at start. The data files are therefore
  part of the jar and the bundles; editing one needs a rebuild, no asset step.
- **Simulation specs.** `sim` defines the records it runs on (`ShipSpec`, `PulseCannon`,
  `ShieldModel`, `Plating`, `Loadout`, `EnemySpec`, `LevelScript`, `Rules`) and knows nothing
  about data files. `LevelScript` holds the level-wide parts (scroll sections, waves, ground objects and units, set
  pieces, secrets, radio cues) and four sub-records, each validating its own parts: `ObjectiveSpec` (secondary,
  targets, escort, defend, naval convoy, ground allies, tows, holds, collapse), `GroundSpec` (road, water,
  terrain, gates, no-surface zones, belts), `HazardSpec` (cranes, debris, sleds, rocks, vents, darkness, weather,
  sondes) and `DropSpec` (group and part drops, placed pickups, drop factor); their feature types are nested in
  them, the types shared across concerns (`Section`, `Secondary`, `RadioCue`, `CueTrigger`, `HookMode`,
  `GroundUnit`, `RootBurst`, `SideEntry`, `GroundObjectSpec`, `SetPieceSpec`, `PartSpec`, `Pass`, `Waypoint`)
  are top-level `vanguard.sim` types. `LevelScript` keeps the checks across the sub-records and builds with
  `LevelScript.builder(number, act)` / `toBuilder()`, whose setters take the level-wide parts and each sub-record
  whole (a caller reads `level.hazards().vents()` and changes one part with the sub-record's wither); `content`'s `SimSpecs` builds them from the loaded records at a difficulty,
  with the difficulty levers applied. The dependency stays
  `content → sim`, so the simulation keeps no YAML, Jackson or file access.
- Balance checks (budget per level, DPS against the reference, time to kill, bounty) are
  JUnit tests over the loaded content (`BalanceTest`, with the chained Act 1 playthrough
  `ActPlaythroughTest`); `tools/balance.py` prints the same plan and checks as the balancing sheet.

### Data file schemas

Keys are snake_case; times in seconds, distances and sizes in px, speeds in px/s, power in MW,
prices in credits. Every file starts with a comment that names its README and links here. Sizes
are `[width, height]`, ranges `[min, max]`, points `[x, y]`. `available` is a key of
`design/player/data.yaml`'s `availability` (`start`, `act N`) or an exact level (`L22`); the
first entry of a part's model list is the starter (price 0, `start`).

One part per family; this table names the part that holds each data file's schema.

| Schema | Part |
|---|---|
| Weapon (`player/weapons/<slug>/data.yaml`) | [schemas-player](schemas-player/README.md) |
| Europa (`world/europa/data.yaml`) | [schemas-act-4](schemas-act-4/README.md) |
| Enemy (`enemies/<category>/<slug>/data.yaml`) | [schemas-units](schemas-units/README.md) |
| Level (`campaign/<act>/<level>/data.yaml`) | [schemas-levels](schemas-levels/README.md) |
| Level, M8 part A | [schemas-act-4](schemas-act-4/README.md) |
| Level and unit, M8 part B | [schemas-act-4](schemas-act-4/README.md) |
| Level and unit, M8 part C | [schemas-act-4](schemas-act-4/README.md) |
| Level and unit, M8 part D | [schemas-act-4](schemas-act-4/README.md) |
| Level and unit, M8 part E | [schemas-act-4](schemas-act-4/README.md) |
| Level and unit, M8 part F | [schemas-act-4](schemas-act-4/README.md) |
| Level and unit, M8 part G | [schemas-act-4](schemas-act-4/README.md) |
| Level and unit, M8 part H | [schemas-act-4](schemas-act-4/README.md) |
| Level backdrop (`backdrop`) | [schemas-backdrops](schemas-backdrops/README.md) |
| Ship and core parts | [schemas-player](schemas-player/README.md) |
| Player (`player/data.yaml`) | [schemas-player](schemas-player/README.md) |
| Systems | [schemas-player](schemas-player/README.md) |
| Voice (`audio/voice/data.yaml`) | [schemas-voice](schemas-voice/README.md) |
| Allies (`allies/data.yaml`) | [schemas-units](schemas-units/README.md) |
| Act (`campaign/<act>/data.yaml`) | [schemas-levels](schemas-levels/README.md) |
| Wingmen (`player/wingmen/data.yaml`) | [schemas-player](schemas-player/README.md) |

### Presentation (`game`)

- A **screen state machine** for the flow in [ui](../../ui/README.md): title, menu, difficulty,
  briefing, hangar, level, pause, debrief, options, credits.
- One **UI kit** (glass panels, metal HUD, bitmap fonts 8×12 / 10×20 / 20×30) shared by all
  screens; keyboard and gamepad navigation everywhere.
- Rendering into the 960×540 `PixelScreen`, integer-scaled with letterboxing or sharp-bilinear;
  parallax layers per the [art direction](../../art-direction/README.md); shaders stay
  GLES 2 compatible (the ANGLE fallback on macOS).
- M5 part E, the `sub` pass (`WaterLooks`, user decision E4 = c): on a level with `water: true`
  everything on the `sub` layer is drawn into a play-field-sized `FrameBuffer` (RGBA8888, no depth)
  between the deep layer and the ground tiles, with `setBlendFunctionSeparate` so the buffer holds
  premultiplied colour, then drawn over the field once through a composite shader (row-wise sway,
  3×3 blur, the water tint) with `GL_ONE, GL_ONE_MINUS_SRC_ALPHA`; both shaders are GLSL 1.10 /
  GLSL ES 1.00 without `#version`, like the others. The pass saves and restores the bound frame
  buffer and viewport (the `PixelScreen`'s), as `FarsideLooks`' light map does. When the driver
  refuses the frame buffer it falls back to a per-sprite tint shader (no blur, no sway).
- Audio: the streaming music player with intro and loop points, and the SFX bank with 64
  sources. The bank decodes the effects (about 17 minutes of OGG) on a background thread to 16-bit
  PCM WAV files in memory (`OggPcm`, the stb_vorbis decoder libGDX uses), the menu sounds first; the
  render thread makes each OpenAL sound from its PCM a few a frame (`SfxBank.update`), or at once when
  it is played first (decoding it itself if the thread has not got to it, so nothing is dropped), and
  `LevelScreen` calls `finish` so every effect is ready before a level's first tick. Every OpenAL call
  stays on the render thread.
- Startup (step startup, 2026-10-30): the launcher's `TerranVanguard` starts the content loading on a
  thread of its own before the window opens (`GameServices.Data`: `ContentLoader.parseClasspath`, the
  files parsed side by side, then the campaign rules and catalogue); `create()` loads the textures and
  sounds meanwhile and waits for what is left. The validator's checks (`Parsed.check`, longer than the
  parsing) run on that thread after it while the title shows; their problems are thrown on the render
  thread once done, as the start did before. One `startup` log line gives the time to the first title
  frame per part of `create()`, a second when the sound effects are ready, and a `level` line each
  level start's load time. Measured on Xvfb/llvmpipe, three runs: 7.4–8.0 s to the first title frame
  before (content 2.3–3.0 s, backdrop atlas 1.9–2.1 s, sound effects 1.6–1.8 s, the JVM and window
  1.2 s), 1.7–1.9 s after (the JVM and window 1.2 s, the rest 0.5–0.6 s); the effects are all ready 3.4–3.8 s
  after the launch. OpenAL Soft sums every source (the effects and the music stream's `AudioDevice`) into
  the device's float mix; it has no limiter on float output by default, so the
  [master limiter](../../audio/README.md#master-limiter) is OpenAL Soft's output limiter
  (`ALC_SOFT_output_limiter`): `desktop`'s `LimitedAudio` (libGDX's `OpenALLwjgl3Audio`, returned by
  the launcher's `createAudio`) resets the device with `ALC_OUTPUT_LIMITER_SOFT` on through
  `alcResetDeviceSOFT` before anything plays, and `OutputLimiter.watch` turns it back on from the
  render loop's `update()` once a second when libGDX's device observer turned it off: the observer
  reopens the device without attributes (`alcReopenDeviceSOFT`) once just after the start and on
  every change of the device list, so the limiter is off for up to a second then (it gives up after
  three resets in a row that do not take). The limiter is fixed by OpenAL Soft: 0 dBFS, 1 ms look-ahead, 2 ms
  hold, automatic attack and release, and an adaptive make-up gain that is the slow release after
  an over. `OutputLimiterTest` checks it on a loopback device (`ALC_SOFT_loopback`), which renders
  the mix into memory without a sound card.

### Assets

- Production sprites and audio are source files in `assets/` (LFS). Until a part's art is
  `final`, its **chosen concept art** is copied in as a placeholder by a script, so the game is
  playable early.
- **Exception: level backdrops.** A chosen setting scene is one composed concept sheet, not the
  tile sets and set pieces a level is built from, so cutting it up gave one repeating crop per
  layer. The backdrop placeholders are therefore rendered by `tools/concept/backdrop_l01.py`
  straight into `assets/backdrop/level-01/` from the chosen Earth orbit scene's models, palette
  and rules, at the sizes the level's data file gives; each PNG carries a `Placeholder` text chunk
  naming the script. `importPlaceholders` does not touch them. The same holds for ground targets
  without concept art: `tools/concept/ground_targets.py` renders Level 01's cargo containers,
  beacon, their break-apart and glint frames into `assets/sprites/` (superseded by
  `tools/art/loot_targets.py`).
- **Final art** is rendered by the production generators in `tools/art/` (see its README)
  straight into `assets/`; every PNG carries a `Source` text chunk naming its generator, which is
  how `importPlaceholders` knows to leave a part alone (`FinalArt`). Attachment points per frame
  (mount points, pod offsets) are JSON pivot files in `assets/pivots/`. Once a part is final, its
  placeholder generator (e.g. `ground_targets.py`) is superseded.
- The `pipeline` module turns them into build output at build time: angle sets (using the
  symmetry rule), texture atlases, audio in OGG. Generated atlases are never committed.
  `packAtlases` checks the packed pages against the budgets of the production plan and fails the
  build when one is exceeded (`AtlasBudget`). It packs a shared `sprites` atlas and one unit atlas
  `level-NN` per level for the sprites only that level uses (`SpriteUse` derives the split from the
  levels' data and fails the build on a sprite nothing uses). `Sprites` keeps the shared atlas
  loaded; `LevelScreen` loads its level's unit atlas (`enterLevel`) and disposes it when it closes
  (`leaveLevel`, counted so a retry's new screen keeps it); lookups by name search both.
  The backdrops are packed the same way, one atlas `backdrop-level-NN` per folder of `assets/backdrop/`
  (step startup, 2026-10-30: the single `backdrop` atlas of every level, 66 pages and 834 MiB of
  textures, was loaded before the title screen). `enterLevel` loads the level's own backdrop atlas
  with its unit atlas and `leaveLevel` frees it (`BackdropAtlases`); a lookup of another folder's image
  (`images: level-NN`, a level borrowing another level's backdrop) loads that folder's atlas for the
  entered level and frees it with it. A level's backdrop is 16–40 MiB; loading it adds 0.07–0.25 s
  to the level start (Levels 01, 19, 35), before its first frame.
  Sprites are drawn at 1x (a unit's frames are rendered at their game size): a static overlay over a large body
  drawn scaled up with Nearest turns its dither into coarse blocks (step PIX7, the Iron Sovereign's rim and shade;
  `SovereignLooksTest` guards their size, scale and grain).

### Testing

| Level | What | Where |
|---|---|---|
| Unit | rules in `sim` and `content`: damage, shields, pickups, economy, formations, loader validation | JUnit 5, every module |
| Content | every data file loads and validates; balance checks per level | JUnit over `design/**/data.yaml` |
| Replay | per level a recorded run (seed + input) must give the same state hash, score and credits | JUnit, headless; re-recorded deliberately when rules change |
| Allocation | no allocation per simulation step | JUnit |
| Smoke | the desktop build starts, runs a level for a few seconds in `--bench` mode and exits | CI on Linux (xvfb), locally on all |

Screenshot tests are left out until there is a need.

Every module's test classes spread over forked JVMs, half the machine's cores (`maxParallelForks` in the
`buildSrc` conventions, 1 GB heap each; 2026-10-18: the full `check` from 50 to 12 minutes on 20 cores). Forks
rather than JUnit's in-JVM parallel threads: flights running side by side in one JVM change what the JIT
compiles, and the allocation tests then see allocations that escape analysis otherwise removes.

### Conventions

- Java 21; records, sealed interfaces and switch expressions where they fit; no reflection-based
  frameworks beyond Jackson in `content`.
- Formatting enforced by the build (Spotless with palantir-java-format, 120 columns, as the
  spike code); compiler warnings are errors.
- Small classes, clear names, light Javadoc on public types; tests named as behaviour.
- The README of the part being implemented is the ticket: its *Implementation* checklist is
  ticked in the same change as the code.

### CI and releases

- Every push: build, format check and tests on Linux, Windows and macOS (as in the spike).
- Tags `v*`: Construo bundles for linux-x64, windows-x64, macos-x64 and macos-arm64 are attached
  to a GitHub release, after the Linux bundle has been started under Xvfb; the bundles are
  unsigned. The version, the icon, the bundle contents and the notes:
  [release](../release/README.md).

## Implementation

- [x] Gradle root build with `sim`, `content`, `game`, `desktop`, `pipeline` and `buildSrc`
- [x] Spike foundations carried over as listed, with their tests (`SfxBank` follows with the first sound effects in M1)
- [x] Data-file loader with validation; `sync_tables.py`; `check_docs.py` compares rendered tables
- [x] `balance-data.json` migrated to per-part `data.yaml`
- [x] Balance checks ported to JUnit (`BalanceTest`, `ActPlaythroughTest` in `content`; `tools/balance.py` prints the balancing sheet)
- [x] Asset pipeline: placeholder import from chosen concept art, angle sets, atlases (`:pipeline:importPlaceholders` with `PlaceholderSprites`, `AngleSetGenerator`, `FinalArt`; `packAtlases` with `AtlasPacker` and `AtlasBudget`; see [production](../../art-direction/production/README.md#implementation))
- [x] CI: build, format check, tests on three OSes; release bundles on tags
- [x] Smoke test of the desktop build in CI

## Open questions

- None open.

## Decisions

Entries dated before 2026-10-21 (M8's start): [decisions-archive.md](decisions-archive.md).

- 2026-10-30 (step PIX7): the Iron Sovereign's "pixelated, few colours" look was not a pipeline regression: its
  frames, its Level 35 atlas (RGBA8888, Nearest, the same pages) and a 1:1 still at the same tick are identical on
  5f510b41 and f1dc0f05; its rim and shade overlays, half size drawn 2x with a Bayer dither, laid a 2×2-pixel stipple
  over the station. They are full size now, drawn 1x, undithered (no other unit draws a scaled static overlay).
- 2026-10-30 (step startup): the time to the title screen cut from about 8 s to 1.7–1.9 s: backdrop atlases per
  level (rather than folded into the unit atlases `level-NN`, which would change their packing settings and the
  per-level page budget's split), sound effects decoded in the background with the render thread as the fallback
  (rather than decoded lazily on first play, which would stall a level's first seconds), the content parsed in
  parallel on a thread of its own from the launch on and its validator checks run after the title shows.
- 2026-10-25 (step m8split, user-approved token savings): the Data file schemas split into six child parts, one per
  family (player and economy, enemies and allies, levels and acts, Act 4, backdrops, voice), moved unchanged; this
  section keeps the shared conventions and an index of every schema. The data files' schema comments and the parts'
  links now point at their family's part.
