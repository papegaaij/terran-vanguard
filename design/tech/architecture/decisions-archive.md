# Architecture — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-10-01: Drafted after the tech stack was approved.
- 2026-10-01: User decisions: game numbers live in `data.yaml` files next to the documents and
  the README tables are rendered from them; the format is YAML (`balance-data.json` is converted).
- 2026-10-01: Approved by the user.
- 2026-10-01: M0 skeleton built. `FixedStepClock` moved to `sim` (it has no libGDX dependency);
  `InputRecording` lost its `replay` method with the spike's `World`, so `Command` stayed behind
  too. `SfxBank` is not carried over yet: M0 plays no sound effects, and its effect list named
  spike files; it returns with the SFX player in M1. palantir-java-format is pinned (2.80.0) in
  the version catalog.
- 2026-10-01: Placeholders: `./gradlew :pipeline:importPlaceholders` copies the chosen concept art
  and music into `assets/` (committed, LFS); the build only reads `assets/`, so CI pulls just
  `assets/**` from LFS.
- 2026-10-01: macOS first thread: the Construo launcher (roast) starts the JVM on the first thread
  by default (`runOnFirstThread`), so the bundles need nothing; `./gradlew :desktop:run` adds
  `-XstartOnFirstThread` on macOS. libGDX 1.14's `useGlfwAsync()` (LWJGL's `glfw_async`) was not
  chosen: it is newer, untested here, and would mean a second mechanism next to roast's. The
  `installDist` start script does not add the flag; it is a Linux smoke-test tool.
- 2026-10-01: Generational ZGC (`-XX:+UseZGC -XX:+ZGenerational`) in the bundles (roast) and in the
  `run` task and `installDist` scripts, so development runs match the release.
- 2026-10-01: M1 asset pipeline: `:pipeline:importPlaceholders` now also runs `PlaceholderSprites`, which cuts the placeholder frames out of the chosen concept sheets with documented crop rectangles (checkerboards keyed out, glow sprites background-subtracted for additive drawing, zoomed frames sampled back to 1×) into `assets/sprites` and `assets/backdrop` (committed, LFS). `:pipeline:packAtlases` packs them at build time into `sprites` (nearest) and `backdrop` (linear) atlases in the build output, which the desktop resources include instead of the single frames. `SfxBank` is carried over.
- 2026-10-01: M1 simulation: `Sortie` owns the ship, shots and Skitters in pools and emits `SimEvents`; the numbers sit in one record per part (`ShipSpec`, `PulseCannon`, `ShieldModel`, `Plating`, `SkitterSpec`) until the M2 data files. Tests: unit tests per rule, an allocation test over 3 600 steps and a replay test of a recorded minute of the test sortie. `--bench` now flies the test sortie.
- 2026-10-02: M2 data files (part A). One `data.yaml` per part next to its README, YAML with
  comments and compact flow-style rows; README wording a table needs goes under `notes` with
  `{field}` placeholders, so no number exists twice; the schemas are documented once, in *Data
  file schemas*. Generated tables are marked `<!-- data: NAME -->` … `<!-- /data -->` (one
  convention; the weapon tables' `balance:levels` markers were converted). After the migration
  every marked table renders byte-identical to its hand-written version, except the weapon
  tables' footnote, which now names `data.yaml` and `tools/sync_tables.py`.
- 2026-10-02: Tools: `tools/sync_tables.py` renders the tables (PyYAML), `tools/design_data.py`
  holds the loading, the placeholder filling and the weapon numbers shared with
  `tools/balance.py`; `balance.py` reads the data files and the plan
  (`design/player/balance-plan.yaml`) and prints the same report as before. Its enemy hook still
  looks for `design/enemies/balance-data.json`, so the report stays unchanged; pointing it at
  the enemy and level data files is left for the JUnit port. `balance-data.json` is removed:
  its `_doc` now lives in *Data file schemas*, its plan in `balance-plan.yaml`, two values took
  the README's meaning (Bomb Rack `range: drop` instead of 0, Proximity Mines `lifetime: 4`
  instead of no range), and the shield, armour, generator, engine and utility lists gained the rows
  that were only in the READMEs.
- 2026-10-02: Loader: Jackson 3 YAML into records; required unless `Optional` (a custom
  annotation introspector, so the records need no annotations), unknown keys rejected, `notes`
  ignored; float-to-int coercion off. Run-time delivery: the data files are copied into the
  `content` resources with an index rather than read from `design/` at run time, so the jar and
  the bundles carry them and the classpath is the only source.
- 2026-10-02: The simulation receives its specs from `content` (`SimSpecs` builds the `sim`
  records), so `sim` has no dependency on the data format; the M1 constants (`STORMHAWK`,
  `LEVEL_1`, `MK_I`, `STANDARD`, `SKITTER`) are gone. The `sim` rule tests build their own specs
  with the M1 numbers (`TestSpecs`); the replay test moved to `content` and flies the specs built
  from the data, with the same state hash (`6d6181b0b974e1bb`), because every migrated number is
  the same double as the M1 constant.
- 2026-10-02: M2 part B (Level 01). `sim`: `Sortie` runs a `LevelScript` with `Rules` (built by
  `content`'s `SimSpecs` at a `Difficulty`, levers already applied): `WaveSchedule` plans every
  unit once per level (`Formations`, `Spawn`), and an attempt walks it with a cursor; `Enemy`
  (enter, hold or orbit while firing, leave), `EnemyBullet`, `GroundObject`, `Pickup` and `Tally`
  (score, chain, credits by source) live in pools or fixed arrays; radio cues, edge warnings and
  pickups reach the presentation as `SimEvents` with an int value or as read-only state;
  `LevelResult` is built on demand for the debrief, outside the step. `M1`'s `TestSortie`,
  `Skitter`, `SkitterSpec` and `SnakeWave` are gone (`SnakePath` became the general `FlightPath`).
  Content: levels are keyed by their path under `design/campaign` (`act-1-…/level-01-…`), so the
  act and number come from the key. Data schema additions (documented above): enemy `drops`,
  wave `speed` and `interval`, ground target `size` and `at`, `pickup_drift_speed`,
  `notes.threat_profile`; removed: wave edge `both`, a placed pickup's `t`, easy
  `attack_interval`. Tests: an allocation test over a whole level (after a warm-up run, since the
  first run initialises `Trig`'s table and the enum switch maps), and the replay of the whole of
  Level 01 at medium (`level-01-medium-2185.rec`, hash `f3ae19deb416eead`, 90 kills, 916
  credits), re-recorded with `-Dvanguard.recordDir`. `game`: `LevelScreen`, `DebriefScreen`, the
  HUD split into `HudKit`, `MissionPanel` and `ShipPanel`, `RadioQueue`, `ControlPrompts`,
  `LevelMusic`. `desktop`: `--difficulty easy|medium|hard` and the debug option `--debug-speed <n>`
  (game time n times faster, to get through a level quickly when testing).
- 2026-10-02: M2 part C (level backdrop). The backdrop is data (*Level backdrop* above) and
  `vanguard.game.render.Backdrop` draws it: per layer the sections' tile sets (whole pixels, the
  layer's factor of the sim's ground scroll; drawn as texture strips so a seam can fall anywhere),
  the set pieces, the haze, the cloud banks and wisps blended across a ramp, without allocating per
  frame. Tile sets change spatially at a seam (structures would ghost in a dissolve), the
  atmosphere in time (the art direction's ramp). The low-air layer now draws above the ground
  objects. The backdrop atlas uses nearest filtering (everything at native size); the old
  single-crop cuts of `parallax-r03-a.png` and their `PlaceholderSprites` entries are gone. The
  game compiles against the data records, so `content` exports Jackson's annotations (`api`).
  The simulation is untouched (same replay hash).
- 2026-10-02: Level 01 after playing (user decisions): the level is 180 s; the replay was
  re-recorded (`level-01-medium-2185.rec` now 10,800 steps, hash `e602b2264976076f`, still 90
  kills and 916 credits; before: 11,400 steps, `f3ae19deb416eead`). The old recording ran 600
  steps past the new end and still matched, since nothing changed after the last enemy; re-record
  whenever a level's length changes. Loot targets (art direction, readability rule 7):
  `GroundObject` counts the steps since its last hit (hashed, like the ship's `ticksSinceShot`),
  which `LevelRenderer` turns into the white hit flash (`FlashShader`) and the damaged frame; the
  glint phase comes from the tick and the target's x, the beacon blinks with the tick. `Effects`
  comes as `glowing()` (additive) and `solid()`; a destroyed container's break-apart is a solid
  effect placed in ground coordinates (drawn at minus the ground scroll, so it rides the ground)
  above the ground objects and below the low-air layer.
- 2026-10-02: Backdrop and HUD fixes after playing Level 01. `LevelData.OUTRO_SECONDS` (5 s: the
  scroll runs on after the level end until the debrief) is shared by `LevelScreen` and the backdrop
  checks; `BackdropLayer.opaque()` marks `deep`, on which `BackdropCheck` wants a tile set in every
  section, and its density and motion checks now run through the outro; `BackdropAssetsTest`
  steps the level and its outro and fails when a set piece on an opaque layer shows a
  non-transparent edge on screen (the deep-layer coverage rule of the art direction). The left
  HUD panel's regions are `vanguard.game.render.MissionLayout` (y ranges as in the HUD's *Left
  panel layout*), `HudKit` cuts text off at a width, and `MissionLayoutTest` measures the
  content's prompts and radio lines with the built-in font's metrics, read without a GL context
  (`BitmapFontData` from the classpath). Debug launch option `--invulnerable` (with
  `--debug-speed`, for testing only): `Rules.withInvulnerableShip()` lets enemy bullets and rammers
  pass through the ship; it is off unless the option is given, so play and the replay are
  unchanged.
- 2026-10-02: M3 part A (screen flow, UI kit, options). `game`: `screen.ScreenFlow` is a stack
  (`Transition`: stay, open, back, replace, quit); `ui` holds the glass kit (`Glass`, `Fonts`,
  `TitleScene`, `Menu`, `Dialog`, `Words`); `settings` the Options records (`Settings` with
  `VideoSettings`, `AudioSettings`, `ControlSettings`, `GameplaySettings`) and the `SettingsStore`
  seam, which `desktop`'s `SettingsFile` implements next to the display keys; `audio.Mixer` scales
  every sound by its `Bus`. Input: fixed menu actions beside the remappable flight actions,
  `MenuInput` (key repeat), `KeyCapture` and `Bindings.withKey` / `withButton` (conflict swap);
  `ActionInput` reports focus loss and gamepad disconnects. `sim`: `Sortie.retry()` for the pause
  menu's restart (a restart now also clears `complete`); the replay hash is unchanged. Launch
  option `--start title|level` (default: title, level in a bench run), so a bench run can test the
  menus. Assets: `tools/concept/ui_assets.py` renders the bitmap fonts (BMFont text files, PNG
  pages) and the title scene and logo into `assets/`, like the backdrop placeholders; the menu
  sounds join `importPlaceholders`.
- 2026-10-02: M3 part B1. The campaign state is plain Java in `content`
  (`vanguard.content.campaign`, since it needs the difficulty and the level keys that `content`
  owns): `Campaign` (the state and its transitions: a won level banks, a failure loses the attempt,
  retries and the armour floor), `CampaignRules` (its numbers from the data), `CampaignRoute`
  (briefing / hangar / launch between levels), `Briefings` (the briefing before a level, with the
  act intro when the level opens its act), and the saves: `SaveGame` (the record),
  `SaveFormat` (versioned JSON through Jackson's JSON mapper) and `SaveSlots` (the files, written
  atomically). The desktop launcher puts the saves next to the settings file (`saves/`), so a
  `--settings` file in a temporary directory keeps test saves out of the real ones. `sim`: a
  destroyed ship no longer restarts by itself; `Sortie.retry(armour)` starts the next attempt with
  the campaign's armour and the constructor takes the first attempt's armour. New data files: the
  act's `data.yaml` and `systems/retry/data.yaml`; level data gained `briefing`; `sync_tables.py`
  renders the briefings (`briefing`, `teaser`, `act-title-card`, `act-briefing`).
- 2026-10-02: M3 part B2. `vanguard.content.campaign` gained the hangar's rules: `ItemKind` (what
  fits a slot), `Gear` (the immutable credits, loadout, inventory by kind, charges and armour the
  hangar changes; the visit's undo keeps one per transaction), `Catalogue` (every shop item from
  the data with price, upgrades, draw per level, unlock, traits and shown numbers; sell-back,
  repair cost and sensor bonus), `Hangar` (a visit: shop rows with their choices and refusals,
  buy / upgrade / fit / unfit / sell / charge, repair, undo, load and output, sensor level),
  `Intel` (the next level's threat profile and what the sensor level shows) and `Flight` (what a
  sortie flies of the loadout: `SimSpecs.loadout(engine, Pulse Cannon level, shield, plating)`).
  `game` gained `vanguard.game.hangar`: `HangarState` (the screen's focus and keys, headless and
  tested), `HangarView` with `ShopPanel`, `LoadoutPanel`, `IntelPanel` and the code-drawn
  `TacticalMap`, and `Names`. `sim`: `PulseCannon.pattern` fires a level's parallel bolts (the
  replay hash is unchanged at L1). Level data: `threat_profile` is structured (above); the save's
  inventory is a map by kind.
- 2026-10-02: Level 01 music and radio fixes. `LevelData.RadioCue` takes `easy` / `hard`
  `RadioChange`s (another line), applied by `SimSpecs` like the waves' changes, so the sim's cue list
  and the replay are unchanged; `LevelData.Music.startDb` sets the theme's level in its start
  section, which `LevelMusic` raises to full over 2 s after it. The outro after a won level is
  `vanguard.game.level.Outro`: `LevelScreen` hands over to the debrief once `RadioQueue.idle()`
  (nothing shown or queued), at the latest `LevelData.OUTRO_SECONDS` (now 15 s, the longest outro,
  which the backdrop checks step through) after the level end; the sim's level result is
  unchanged.
- 2026-10-02: The spike branch was replaced by the tag `spike-libgdx` (user decision).
- 2026-10-02: Production art, UI batch part U2. `Sprites.region(name)` and `Sprites.patch(name)`
  look up the named regions and nine-patches of the sprite pages; `Glass` takes `Sprites` and
  holds the glass kit's pieces (`ui/…`, tools/art/ui_kit.py) instead of a white pixel and two
  generated textures, so it is no longer `Disposable`. `vanguard.game.hangar.ItemIcons` maps every
  catalogue item (by identity) and the escort to its two icons (`icons/…`, tools/art/icons.py) when
  the hangar opens and fails there if one is missing; `TacticalMap` loads `ui/hangar-map.png`
  (tools/art/ui_scenes.py), which also renders the title scene and logo that
  `tools/concept/ui_assets.py` no longer writes. The replay hash is unchanged.
- 2026-10-02: M4 part A (the arsenal). `sim`: `Sortie` hands its work to `PlayerFire` (the ship's weapons:
  every `Armament.Mount` fires on its own clock, overdrive patterns, projectiles that fly, seek,
  fall and burst; what they reach by `WeaponSpec.Delivery` and the layer rules, hardened ground
  targets), `EnemyForce` (waves, enemies, bullets), `Objectives` and `Radio`; the split itself kept
  the replay's hash. `WeaponSpec` replaces `PulseCannon`: rate, damage, speed, hit box, range or
  lifetime, pierce, blast, turn rate, seek cone, fall or flight time, snap radius and one `Muzzle`
  (offset and angle from the ship's centre) per projectile; `Shot` carries its weapon, velocity,
  heading, range flown, pierce count and the serials it struck, a homing shot its locked target
  (`Enemy.serial`, unique per attempt), a bomb or shell its landing point scrolling with the ground.
  New events: `SHOT_GLANCED`, `BLAST`, `OVERDRIVE_ENDED`; shot and hit events carry the mount.
  `Trig.sin` no longer reads past its table for a tiny negative angle (a missile's heading found it).
  `content`: `SimSpecs.weapon` builds the muzzles from the ship's mount points (pods mirrored on the
  left and turned in by `converge`, side guns from the wing `roots`, mirrored to the left),
  `SimSpecs.loadout` takes the fitted weapons and the spare power (the shield's regen bonus),
  `Flight` maps the campaign loadout and computes the spare power like the hangar; a ground target
  may be `hardened`. Data: weapon `converge`, `fall`, `flight`, `snap`, `cone` (and a homing weapon's
  `range` as its seek radius beside `lifetime`), ship mount `roots`. `game`: `WeaponLooks` (shot
  sprites per angle, heading or level, muzzle flashes, impacts, pods from `pivots/pods.json` via
  `PodPivots`), `FlightSounds` per weapon family, the HUD's power, weapons and overdrive rows.
  Debug launch option `--loadout <slot>=<weapon>[:<level>],...` (`DebugFit`, implies `--start level`;
  testing only). The Level 01 replay keeps its kills (90) and credits (916); its hash is now
  `c58ff0e1fb68eae9` (the new state is hashed, and the starter fit's 4 MW spare power raises its
  shield regen by 40 %).
- 2026-10-02: M4 part B (Level 02). `sim`: ground units (`LevelScript.GroundUnit`: an enemy of the level's
  ground targets, entering at the top edge and scrolling with the ground; a turret's barrel turns
  at its turn rate inside its arc and fires along itself), dives (`EnemySpec.Dive`: the hold is the
  pause, the leave the dive at its own speed, one shot when it passes the ship's height or after
  its fire time), fans (`EnemyGun.fan`, `spreadRadians`), the `single` and `column` formations,
  group objectives (`Objectives`: a group's outcome when its last unit is gone; pay per cleared
  group, `GROUP_CLEARED` / `GROUP_LOST` events and cue triggers), `Crane` (the arm's angle a
  function of the level time; contact at most once a second; it stops every shot; a clamp counted
  while it swings), the overdrive and salvage M pickups. `content`: the level data's `prompts`,
  `cranes`, a section's `peak` (`LevelData.atmosphereStretches()` drives the backdrop's ramps and
  checks), ground targets with `enemy`, `group` and `easy` / `hard` placements, a secondary
  objective of `groups`, radio cues with `group` and `portrait`; the enemy data's `dive`,
  `terrain`, a fan's `count` and `spread`, a turret's `turn_rate` and `arc`, the `±30° tilt`
  orientation, optional weak-point multipliers and the `fan_count`, `dive_pause`, `burst` hooks.
  `game`: `EnemyLooks` finds a unit's frames by its slug (a tilt set, a pause flare, remains),
  `CraneLooks` (`pivots/crane-four.json`), the tracker's group pips, the level's theme by its
  track number. Debug option `--level <n>` (`DebugFit.startAt`, testing only). The Level 01 replay
  is unchanged (`c58ff0e1fb68eae9`).
- 2026-10-02: M4 part C (Level 03), the game. `LevelRenderer` draws the low-air flyers below the
  low-air layer's banks, the debris chunks on the play plane above the flyers (hit flash from
  `Debris.ticksSinceHit`), the spore mines additively just below the enemy bullets (growing and
  brightening while they rise), and the set pieces through `SetPieceLooks` (a part's sprite is
  `<slug>-<part>` with the screen side last, `fin-left`; a part without one is drawn as its
  `-glow`; per-frame pivots from `pivots/<slug>.json`): on the play plane below the ship, off it
  (arriving, descending, rising) above the ship and scaled between 1.25 and 1 by
  `SetPiece.altitude`. A level whose backdrop holds a `lifeboat-light` image draws its triggers as
  that light. `Sprites.frames` returns a sprite's frames in index order; `EnemyLooks` plays a
  radial spinner's frames at its spin; `Effects` can start an animation after a delay (the set
  piece's chained death). `LevelMusic` crossfades both ways per section (`Music.full`); a prompt's
  `skip` layer (`Prompt.skipLayer`) replaces Level 02's ground-prompt special case. The level's
  `backdrop` stays required.
- 2026-10-02: M4 part C, the death effects. `EnemyLooks` finds a unit's death animations by its
  slug like its flare and remains: `<slug>-death` additive, `<slug>-tatters` or `<slug>-husk`
  solid, as `DeathEffect`s (frames, steps per frame, delay) timed by tier (`tiny`: 2 steps a
  frame, the glow 4 steps after the pop; larger: 4 steps a frame, with the burst). `LevelScreen`
  keeps a second solid `Effects` at play-field positions (no ground scroll) for the pieces, which
  `LevelRenderer` draws after the set piece on the play plane, below the ship and the glows; a set
  piece's `<slug>-ichor` cloud (6 steps a frame) starts with each chained burst and the large one,
  `Effects` drawing it at the high-air scale and opacity (`LevelRenderer.highAirScale` and
  `highAirOpacity`) when the unit dies off the plane. `FlightSounds` plays a set piece's cry by
  slug (`Sfx.LEVIATHAN_CRY`) with its death.
- 2026-10-03 (user decision): two M4 part C points accepted as they are: the Level 01 replay's state hash changed with part C (`ReplayTest`, now `616ea9b687b6d5a9`), and the death pieces (`-tatters`, `-husk`) are drawn below the ship.
- 2026-10-03: Schema text for M4 part D (Level 04), marked "planned (part D)" until the loader
  reads it: the `drift`, `sine` and `walk` movement, the `spawn` attack and its hooks, a fan's
  `aim`, an aimed attack's `away`, `armour` as a mapping with `front_arc`, a walker wave's `paths`,
  the level's `road`, the `escort` primary, the threat profile's `objective`, `requires: special`
  on prompts and radio cues, the ally radio events, and the allies' data file. The Brood Pod,
  Scuttler and crawler data files exist ahead of the code, so the content loader rejects them
  until part D's loader work lands.
- 2026-10-03: M4 part D step 2, the convoy: `vanguard.sim.Convoy` (the `escort` primary's units,
  `Ally`, allocated up front and in the state hash only in levels with a convoy, so the Level 01–03
  replay hashes are unchanged), `Road` (straight between its points, as the backdrop art lays it
  out), the target-the-objective hook in mode `nearest` in `EnemyForce` (an aimed shot's target is
  chosen each step and as it fires; a turret's barrel turns toward it; `EnemyBullet` remembers a
  convoy target), the generic failed primary objective (`Sortie.primaryFailed()`, `PRIMARY_FAILED`),
  the escort's pay (`LevelResult.Escort`, part of the objectives' credits) and the radio's
  `FIRST_ALLY_HIT`, `FIRST_ALLY_LOST`, `MISSION_FAILED` cues, level-end ranges and special-only cues.
  The schema text above lost its "planned (part D)" marks for the road, the escort primary, the
  ally events, `requires: special`, the threat profile's `objective` and the allies' data file.
- 2026-10-03: M4 part D step 3, Level 04: `EnemySpec` gains `Sine`, `Brood` (a spawner) and
  `Walker` (walk, frontal arc, the spit as a second gun, the away angle); `Spawn` an `Escort` circle
  and a `WalkPath` (`vanguard.sim.WalkPath`, the authored ground path); `WaveSpec` the `paths` and
  the `CARRIER_ESCORTS` formation. `Enemy` has two new phases, `ESCORT` (circling its carrier, which
  `EnemyForce` hands it as the spawner that entered last, and released when it ends) and `WALK`;
  their fields and a spawner's burst timer enter the state hash only for such units, so the Level
  01–03 replay hashes are unchanged. `EnemyForce.hatch` releases a spawner's units (pooled ordinary
  units in the `LEAVE` phase), the released units count among the level's enemies, and a self-burst
  calls `Escapes.burst` (`Tally.unchained`: the burst bounty at the kill score, no chain, no kill).
  `PlayerFire` makes a direct shot glance off a walker's front (`Enemy.glances`, from `Shot.vx/vy`).
  New events `BROOD_HATCHED`, `BROOD_BURST` and `WALKER_DOWN` (the kind and facing, for the husk).
  Content: `LevelData.Wave.paths`, `EnemyChange.speedFactor`, `GroundTarget.bonusDrop`, a
  destructible's `reveals`, and `BackdropData.base()` (`BackdropLayer.opaque()` is gone). Game:
  `EnemyLooks` reads `-burst` as a death glow, a walker's `-glow` and `-husk` (its remains per
  heading), the walk frame by distance and a spawner's faster telegraph pulse; `LevelRenderer` draws
  walkers on the ground depth and their glow above the low-air layer; `Sfx.BROOD_BURST` and
  `AMBIENCE_LUNA`.
- 2026-10-03: M4 part E (main-agent choice): the enemy schema gains the planned boss and mortar
  fields (the `mortar`, `ring` and `spiral` patterns, aimed bursts and rotation, part
  multipliers, `chains`, per-attack difficulty changes, the `boss` block with phases and par),
  first used by the Polyp Mortar's and the Gorgon Frigate's data files; the loader accepts them
  once part E builds them.
- 2026-10-03: M4 part E step 2, the boss layer (main-agent brief): a boss is a `SetPiece` with a
  `BossSpec` (`vanguard.sim`) instead of passes, built by `SimSpecs.boss` from the stat block's
  `boss`, `chains` and attacks and the level's `boss` placement. It arrives on the level clock,
  descends invulnerable (`partShielded`), settles and sways; its necks are per-piece angles eased
  toward the ship with a first-order lag (`chainAngle`, armoured segments glance shots); its phases
  end on `until`, fire their attacks through `SetPiece.BossActions` (aimed bursts with rotation,
  rings, spirals, alternation after a 1 s crown opening) and release stream units planned once
  (`Formations.streamUnit`). The `Sortie` holds the arena clock (`Section.arena`: halt, early-death
  jump with the ground scroll, 1 s ramp), the boss checkpoint (`retryFromBoss()`: preallocated
  copies of the tally, objectives, radio, defences, charges and the schedule's place, recorded
  just before the arrival) and the Boss rush (`LevelResult.BossTime`). New events `BOSS_ARRIVED`,
  `BOSS_SETTLED`, `BOSS_PHASE`, `BOSS_DESTROYED` (the credit shower's credits), `BOSS_RETRY`; cue
  triggers `boss-phase` and `boss-destroyed`. The game draws the boss in plain shapes
  (`BossLooks`, no concept cut exists) with the bar at the top of the play field, a coin shower
  and the existing Sfx; the mission failed screen offers *Retry from boss* on easy and medium. The
  Level 01–04 replay hashes are unchanged (the new hash fields are added only with a boss or an
  arena). Tests: `BossTest` (sim) and `BossLevelTest` (Level 01's data cut at 150 s with the
  frigate's arena, loaded at Level 05's place: the specs, an autopilot kill on every difficulty,
  determinism).
- 2026-10-03: M4 part E, Level 05 playable: `Lob` (the mortar's blob in `EnemyForce`, marker fixed
  where the ship was, the ring starting 12 px outside the impact circle, the direct hit applied in
  `hitShip`), `Sled` (time-driven like `Crane`: lights, run, one strike per sled, blocking shots and
  bullets, shutting the stuck sled's trigger while lit), thrown rocks as `Debris.toss` (a life, a
  random drift, breaking on the hull), `LevelScript.targets` / `sled` / `rocks` / `groupDrops` and
  `Secondary.killAll` / `label`; `GroundUnit.group` indexes `LevelScript.groups()` (the primary's
  targets or the secondary's groups). The voice speaker table gains `uncast: true` (no `ref`; the
  speaker's lines have no voice file, VoiceFilesTest allows that only for such speakers). The
  Level 01–04 replay hashes are unchanged: the new fields join the state hash only when lobs are
  in flight or the level has sleds or targets. The debug option `--invulnerable` (captures) also
  keeps a lost destroy-targets group from failing the level. Tests: `CraterNestTest` (sim), `Level05Test`
  (budget, totals, difficulties, the balance plan's fit completing every difficulty), PacingTest
  and RadioTimelineTest over Level 05.
- 2026-10-04: Per-level unit atlases (user decision; design/art-direction/production, Budgets):
  `packAtlases` stages the sprite frames per atlas from `SpriteUse` (shared, or the one level that
  uses them) and packs `sprites` plus `level-NN`; `AtlasBudget` counts a level's unit atlas with
  its backdrop pages and prints every atlas's fill. `Sprites.enterLevel`/`leaveLevel` load and
  dispose a level's unit atlas around `LevelScreen`; `LunaLooks` looks up the mortar and sled
  sprites only in a level that has them.
- 2026-10-04: Economy rework: the level data's optional `bounty_scale` (`LevelData.bounties()`,
  default 1) reaches the sim as `ScoringRules.bountyScale`; `Tally.bounty` pays kills, parts,
  ground units, destructibles' bounties, bursts and shot mines × credit factor × scale with one
  rounding, half to even. The economy's `typical_player` shares (`EconomyData.TypicalPlayer`)
  drive the typical haul in the credit-budget tables and the content test helper `TypicalHaul`.
- 2026-10-04: M4 part F step 2, Level 06's mechanics (main-agent brief; code choices): a segment
  chain is a pooled `Chain` (fixed arrays: a 512-point path history with arc lengths) whose
  members are ordinary pooled `Enemy` units in the `CHAIN` phase with their own hit boxes
  (`Enemy.hitbox()`, which every hit test now reads); the chain's head point flies the wave's
  `FlightPath` and its loop-back (`Spawn.Loop`, warned on the bottom edge by `WaveSchedule`), the
  members sit on the history at their offsets. A cut of an uncut wave's chain moves the rear
  members to a new chain that holds 0.6 s while its head grows (the head is made only if a rear
  member is still alive then, so parts killed together do not regrow) and then lunges straight at
  where the ship was; any other cut and the head's death pop the members behind it one by one
  (`CHAIN_POP`, paying nothing). The Mantis's sweep is `EnemySpec.Sweep` on the hovering unit
  (`SWEEP_TELEGRAPH`, `SWEEP_FIRED`, `SWEEP_HIT`), the beam a thickened segment against the hull
  (`Hull.touchesSegment`). The darkness (`LevelScript.Darkness`) is deterministic from the clock
  and the ship: the sim uses only the headlight cone and the flare pools (a `dark` trigger is shut
  outside them); the game's `FarsideLooks` renders a light map (an FBO cleared to the ambient
  light, additive pools for the static lights, the headlight cone, the flares and the shots)
  multiplied over the ground layer and the ground units, then the turrets' and mortars' glows,
  the flare shells, the sweeps and the Smart Bomb. The Smart Bomb is a second kind of
  `SpecialSpec` (`SmartBombSpec`); `SpecialSlot.bomb` grows the ring, clears bullets and lobs and
  deals its damage once per target (the ledger), `Defences.guard` makes the ship invulnerable. A
  data core is a secret whose trigger drops `PickupType.DATA_CORE`; `LevelResult.dataCores`
  carries it to `Campaign.complete`, which records the core and its unlock. Every new piece of
  state is hashed only where it exists, so Levels 01–05 replay with their hashes.
- 2026-10-05: Round 23's close: a trigger may name a `sprite` of its own (Level 06's survey cache
  and terminal; `LevelRenderer` draws its hit and spent frames, its `-glow` after the light pass
  until spent and its `-glint` only while `Sortie.lit`, and gives a `dark` object no loot
  sparkle); `SpriteUse` claims such a trigger's sprite for its level; `LevelScreen` gives
  triggers no break-apart or wreck. The voice speaker table's `filter: pa` maps a speaker's radio
  lines to `VoiceLines.Filter.PA` (in the key; `tools/art/voice.py` posts them with `pa()`).
  `FlightSounds` plays the round's Mantis, flare and regrowth sounds (`Sfx`), the flare's burn
  loop as back-to-back plays over the level's `flareSeconds`. No simulation change: the replay
  hashes stay.
- 2026-10-05: Schema text for M4 part G (Level 07 and the act end), marked "planned (part G)" until
  the loader reads it: the boss keys (`engages_on_arrival`, `death_seconds`, `poses`, a phase's
  `until.seconds`, `delay`, `move` and `windows` with `spawns`; fire-only `armoured` parts; a
  boss `fan`; a spiral without `duration`; the hooks `arms` and `spawns`), the level keys (`tows`,
  a wave change's `hold`, a pickup's `skip`, `dropped_by.parts`, radio `requires: homing`,
  `requires_not` and a boss-phase cue's `timeout`, the `parts`/`before` secondary, music
  `boss_warning` and `boss_track`), the act's `outro` and the save's `stats.levels`. The level
  and act names follow the code the part's mechanics agents wrote alongside; the boss block's
  YAML names map onto `BossSpec`'s `Pose`, `Move`, `Windows` and `Spawn`. The `boss-destroyed`
  cue's empty subject (`SimSpecs` against the boss slug the `Sortie` cues) is the bug part G
  fixes.
- 2026-10-05: Master limiter (M4 part G): the game's mix went over full scale (peaks 1.2–1.59 in
  Levels 05 and 07). OpenAL Soft does the summing, so a limiter of our own would need the mix
  rendered on a loopback device and replayed through a second device (about 20 ms more latency on
  every sound, and libGDX's device handling replaced); OpenAL Soft's output limiter, off by default
  for float output, is turned on instead (`LimitedAudio`, `OutputLimiter`). Measured by replaying
  the recorded unlimited mixes through it on a loopback device: peaks 1.00, unchanged until the
  first over (1 ms late), afterwards on average 0.1 dB lower for a few seconds (see
  [audio](../../audio/README.md#master-limiter)).
- 2026-10-05: M4 part H docs reconciliation: the asset pipeline (placeholder import, angle sets,
  atlases with the per-level budget) is built; ticked.
- 2026-10-05: M4 part H balance tests: `BalanceTest` (content) buys the balance plan visit by visit
  through the shop's own rules (`Hangar`, the test helper `BalancePlan`) with each level's typical
  haul as income and checks the purchases are in the shop, affordable and within the generator's
  output, the plan's fit against the reference DPS (0.75–1.33 ×, Levels 01–03 up to 1.75 × as the
  accepted onboarding exception), and every enemy's time to kill at its first level and bounty
  against the balancing basis (bosses and `huge` set pieces at the effective 0.6 × DPS; a list of
  deviations pending a decision). `ActPlaythroughTest` flies Act 1 as one campaign per difficulty
  with the test autopilot (now public): the plan bought at each visit, repairs, retries as the
  mission failed screen offers them, a save round trip around every visit, and the act's outro
  after Level 07; about 2 s for all three. `tools/balance.py` lost its `balance-data.json` hook,
  reads the enemy and level data and exits 0.
- 2026-10-05: M4 part H: the armour data gained the low-armour `radio` line (`ArmourData.radio`, a
  `LevelData.RadioLine`); `Defences` sets off `SimEvents.Type.ARMOUR_CRITICAL` once per attempt
  at `Defences.CRITICAL_SHARE` (15 %, shared by the game's `LowArmour`) and hashes its flag only
  once set, so replays that never get that low keep their hashes; the level screen queues the line
  urgent and `VoiceLines` lists it (source `armour`).
- 2026-10-06: Schema text for M5 part A (Rook and the escort slot, the Act 2 weapons, the
  Targeting computer and the Salvage scanner), marked "planned (M5 part A)" until the loader reads
  it: the wingmen data file (written with its loader `WingmenData`, since the loader rejects a data
  file it does not know), the weapons' `accelerate`, `ports`, `drift`, `arm`, `trigger` and `slew`, the utility
  modules' `targeting` and `salvage` blocks, and save format version 3 with `escort`.
- 2026-10-06: M5 part A, the Act 2 weapons (built; the weapons' schema text above is no longer
  "planned"). `sim`: `WeaponSpec.Delivery` gains `TURRET` (aimed by `PlayerFire` per mount at its
  slew, the weapon's `turnRate`, hashed only for turret mounts) and `MINE` (a `Shot` that drifts,
  arms and holds its screen position; `PlayerFire.hitGround` sets mines off and bursts them;
  `WeaponSpec.Mines`), and accelerating shots (`endSpeed`, `accelSeconds`; `Shot.speed()`); the
  old 18-argument `WeaponSpec` constructor stays for weapons without them. `content`: `WeaponData`
  reads `accelerate`, `ports`, `slew`, `drift`, `arm`, `trigger` (`hits: area` = mines);
  `SimSpecs.weapon` builds the ports' muzzles and the one turn rate. `game`: `WeaponLooks` gives
  the mines their pulse and blast and the Hornet its smoke trail (`LevelScreen`, `TestFireView`).
- 2026-10-06: M5 part A, the Targeting computer and the Salvage scanner (built; their schema text
  above is no longer "planned"): `SystemsData.Utility` reads `targeting` (`turn_bonus`,
  `bar_seconds`, `bar_fade`) and `salvage` (`bonus` per level); `SimSpecs.fliesUtility` flies both.
  `sim`: `Loadout.salvageBonus` (0 without a scanner) multiplies a salvage pickup's and a hidden
  crate's payout before its one rounding (`Tally.earn` with a factor); `Enemy.ticksSinceHit` counts
  the steps since a unit's last damage for the HP bar, outside the state hash like its facing, so
  every replay hash stays. `content`: `SimSpecs.loadout` takes the turn bonus (the Targeting
  computer's share) and raises the Stormhawk's weapons' `turnRate`; `SimSpecs.wingman` builds Rook's
  guns at the base turn. `Flight` reports `targeting` and `salvage` for the renderer. The act HP
  factor: `SimSpecs.level` passes the level's act and number to everything it builds
  (`SimSpecs.actHpFactor`; `SimSpecs.enemy` and the public `setPiece`/`boss` without a level apply
  none). `game`: `TargetingOverlay` (bars and brackets, after the front scenery, under the spore
  mines and bullets) and `SecretGlints` (before the pickups), handed to `LevelRenderer.modules`;
  both draw from the simulation's state and keep no per-unit state. The `--loadout` debug option
  takes `utility` and `utility2` slots (a module's name in lower case, hyphenated).
- 2026-10-06: The voice speaker table gains `stage` (`VoiceData.Speaker.stage`): a radio line that
  is only a stage direction plays that sound in place of a voice; `VoiceLines.stageSound` finds it
  and `VoiceLines.radioVoice` (a line's rendered file, else its stage sound) serves `Voices` and
  RadioTimelineTest alike. The Choir's is concept round 29's option a, provisionally, copied by
  `:pipeline:copyPlaceholderStageSounds`; option b since the round closed (2026-10-06).
- 2026-10-06: M5 part B (user decisions D1 and D4, and the gap defaults): the schema text gains,
  as planned (M5 part B) until built, the **tower pieces** of the backdrop (a `ground` piece's
  `tower` block: `height`, `wall`, `shade`; drawn per frame round the projection centre, never
  under a unit; scenery only, D1 = a), the radio event **`escort-first-kill`** and **`{side}`** in
  a radio line (D4 = a), the ambience key `earth-megacity`, and for the Creeper a walker's aimed
  fan, the fan's **`stagger`** and an authored hook `interval` on an ordinary attack. The
  `stagger` key is not in the Creeper's data yet: the loader rejects unknown keys, so the
  Creeper's README holds it as a proposal until the walker code reads it. The tower block is a
  proposal for the renderer task to settle with this text.
- 2026-10-06: M5 part B built the Creeper's walker schema (a walker fan's `aim`, the fan's
  `stagger`, the authored hook `interval` on an ordinary attack; `stagger` is now in the Creeper's
  data), the radio event `escort-first-kill`, `{side}` in a radio line and the ambience key
  `earth-megacity`; their schema text is no longer marked planned (the tower pieces are the
  renderer task's).
- 2026-10-06: M5 part B built the **tower pieces** (D1 = a), settling the proposal: the roof's
  image is drawn at its scale (footprint × *k*, never resampled), not at the footprint's size; no
  `mirror` on a tower (symmetry rule: the roof and the walls' shading are lit from the upper
  left); wall rows follow the true perspective up the wall; the projection centre is (240, 297)
  from the top left, y 243 in the play field's y-up coordinates; a tower is on screen for the
  checks while its footprint or its roof is. The optional **`repeat`** helper of a placed piece
  (M3, traffic streams) came with it. `BackdropData.Tower`, `BackdropCheck`, `TowerProjection`;
  tests `BackdropDataTest`, `TowerProjectionTest`.
- 2026-10-07: M5 part B: the billboard's topple (Level 08) takes the default, a destructible's
  break. The hit that spends any ground trigger raises `TRIGGER_SPENT` (its script index; events
  are not hashed, so the replay hashes stay); `LevelScreen` and `FlightSounds` break only a
  trigger `TriggerBreak` names (a spent frame that is a wreck: the billboard): the small explosion
  on the ground, the small blast and its size's crumble (large at 72×40). Level 06's survey cache
  (shot open) and terminal (core released), the beacon, the lifeboat lights and the sled's clamp
  stay quiet. Tests `TriggerSpentTest`, `TriggerBreakTest`, `TriggerBreakSoundsTest`.
- 2026-10-07: Schema text for M5 part C (Level 09; user decisions D1–D11 of 2026-10-07 and the
  stated defaults), marked "planned (M5 part C)" until the loader reads it: for enemies,
  `armour: hardened` flown for a unit, the periodic `spawn` (`every`, `shut_within`; no `after` or
  `burst_bounty`), the attack pattern `pounce` (`interval`, `pounce`: `range`, `leap`, `air`,
  `scale`) with a unit's current layer, and the formation `pack` (in the basis's vocabulary now);
  for levels, `holds` and the level clock in script time (D2 = a, D3 = a), the `collapse` (D5 = a),
  a wave's `tag` and a tagged `escapes` secondary (D6 = a), the radio events `hold-start`,
  `first-pounce` and `collapse` and `requires: escort`, the threat profile's `required` (D7 = a),
  the music's `full_on`; for the backdrop, the real clock for paths and drift. The Hive Node's and
  the Ravager's data files hold only the keys the loader knows; the new ones wait in a comment there
  (the loader rejects unknown keys), as the Creeper's `stagger` waited in its README in part B.
- 2026-10-07: M5 part C, round 31's collapse look c (user): the `collapse` block's `sweep` gives way
  to `drop` and `blast` (`seconds`, `from`, `to`): the tower leans for `warning`, drops for `drop`,
  and from the impact the kills spread outward from its foot on a ring that rides the dust blast's
  front (Level 09: 1.5 s lean, 1.5 s drop, impact at 3.0 s, radius 100 → 390 px in 1 s), the same
  band and pay; new event `COLLAPSE_IMPACT`; `COLLAPSE_END` (and a hold through the collapse) at the
  blast's end. The old key is rejected by the strict loader.
- 2026-10-07: M5 part C (user): a hold through the collapse lasts until the collapse's dust has
  settled (the impact + `dust.seconds`; Level 09: 9.0 s after the warning starts), no longer until
  `COLLAPSE_END`, so with late kills the rubble heap is not scrolled away under the dust. The
  simulation's `LevelScript.Collapse` carries the dust's seconds (`settleSeconds`, at least the
  blast's; `content` checks it); the events are unchanged (no settle event: the music's `full_on:
  collapse` stays on to the level's end).
- 2026-10-08: Schema text for M5 part D (Level 10; user decisions D1–D12 of 2026-10-08 and the
  stated defaults), marked "planned (M5 part D)" until the loader reads it: for enemies, the
  `cloak` (`layer`, `flash`; the `DECLOAK` event) with the unit's current layer by phase, the
  `ambush` movement (`gap`, `lane`) and the hook `hover_seconds`, the `flock` movement and its
  `LOOP_BACK` event; for levels, the formations `swarm` (a leader route, the `loop_back` `count` and
  the wave change `loops`) and `rear ambush` (warned ahead of the re-entry), `paths` on a snake, the
  air escort (`stations` with a lane sway, `liftoff`, `climb`, untouchable windows,
  `scripted_loss` with `LOSS_GLOW` and `SCRIPTED_LOSS`, the fail on the saveable units), the radio
  events `ally-lost`, `scripted-loss`, `first-decloak` and `first-loop-back`, `required: [rear]`, the
  music's `duck` and `ambience_changes`, the ambience key `earth-ocean`; for allies, `follows:
  lanes` (read), `damaged_by` `bullets` and `contact`, `banks`, `glide`; for the balance plan, the
  `fit` action and the rear check. The Wraith's, the Mote Swarm's and the shuttle's data files
  hold only the keys the loader knows; the new ones wait in a comment there (the loader rejects
  unknown keys). The document is `in-progress` until part D builds them.
- 2026-10-08: M5 part D step D4, the air escort: `LevelScript.Air` (`Station`, `Liftoff`, `Pad`,
  `Climb`, `ScriptedLoss`) flown by `Convoy` (no road; the pads scroll with the ground at the
  sections' speeds until the liftoff), the new `Ally` states and accessors, `AllySpec`'s `bullets`,
  `contact`, `banks` and `glide`, hits in `Sortie.hitAirAllies` (a contact once per unit while it
  overlaps, `Enemy.allyContacts`, hashed only while set), the saveable units for the fail, the pay,
  the level-end cues and the debrief, the events `LOSS_GLOW` and `SCRIPTED_LOSS`, the cues
  `ALLY_LOST` (repeating), `SCRIPTED_LOSS`, `FIRST_DECLOAK` and `FIRST_LOOP_BACK`, a level without a
  secondary (`Secondary.NONE`), and in `content` the keys above (`PartDRules`). The lane sway uses
  `Trig` (StrictMath's sine allocates for |x| > π/4). Everything new is hashed only in a level with
  an air escort or once a decloak or loop-back happened, so the replay hashes of Levels 01–09 are
  unchanged; stepping an air escort allocates nothing (`ShuttleEscortTest`).
- 2026-10-08: Level 10's ferry crate: the level data's optional `crate_seconds`
  (`LevelData.crateSeconds`, checked by `ContentValidator`) reaches the sim as
  `PickupRules.crateSeconds` (default `seconds`), which `Sortie` gives every secret's drop (a
  ground object's, tow's or crane's hidden crate, a data core) instead of the other pickups' life. A
  shared rule (every crate lives until it leaves the bottom edge) was rejected: it would change the
  earlier levels' crates and their replay hashes; with the default the hashes of Levels 01–09 are
  unchanged.
- 2026-10-08: Schema text for M5 part E (Level 11; user decisions E1–E10 of 2026-10-08 and the
  stated defaults), planned until the loader reads it: for weapons, the torpedo delivery of `hits:
  anti-sub` (only with `water: true`); for enemies, the layer `sub` (appended, the ordinals kept),
  `submerge` (a seeded swap timer, the current layer flipping at the swap's middle), a ring's
  `within`, a raft's `terrain.drift`, and for an arena boss the `anchored` movement, a part's `layer`
  and `spots`, a chain's `motion: slam`, the boss's `lanes` and `slam`, a phase's `until.slams` and
  `until.below`, `slamming`, `surface`, `release` and `drop`, the events `TELEGRAPH`, `SLAM`,
  `SURFACE` and `DIVE`; for levels, `water`, an arena's `speed: 0`, the formation `field` (`size`,
  `spacing`, `current`), a nest's `current`, a destructible's `group`, a `sub` trigger with `easy` /
  `hard` `hits`, the `convoy` block outside the objectives (`name` per unit, `station`, `lane` or
  `leaves`, `glide`, `lane_y`), the secondary `afloat`, the radio events `ally-hit`,
  `first-telegraph` and `boss-part-destroyed`, ship names as `{ally}`; for allies, `follows:
  stations`, `damaged_by.slams` and `flak`. Rejected for the convoy: an `escort` primary (it would
  fail the mission; the level's primary is `reach-end`). Everything new is to be hashed only in a
  level that uses it, so the replay hashes of Levels 01–10 stay.
- 2026-10-08: M5 part E step E2a built the `sub` layer (`Layer.SUB`, last), the torpedo delivery
  (`WeaponSpec.Delivery.TORPEDO`, `reaches` `sub` and `ground`; `seeks()`, `passesUnder()`), the
  level's `water` (`LevelData.overWater()`, `LevelScript.water()` / `withWater`, read by
  `PlayerFire`) and the `sub` trigger (`GroundObjectSpec.submerged`, `GroundObject.spend()` for the
  Smart Bomb). Settled on the way: the torpedo's seek radius is its `range`; it seeks set-piece parts
  on the layers it reaches as homing missiles do; it is not `anti-ground`, so a hardened unit is no
  target and glances it, as for every other weapon; it runs in screen space like every other shot
  (the sea's scroll does not carry it) and passes under crane arms, sleds, tow cables and debris;
  Rook does not count a `sub` unit as a flank threat either; a stat block's or cloak's `layer: sub`
  is rejected (a unit only dives to it, E2b's `submerge`), as is any destructible's or trigger's
  layer but `ground` (and `sub` for a trigger over water). Nothing new is hashed: the state a level
  over land steps through is unchanged, so every replay hash stays.
- 2026-10-08: M5 part E step E2b built the naval units, the `field` formation, the naval convoy and
  the `afloat` secondary. `sim`: `EnemySpec.Submerge` (the current layer `sub` while submerged; the
  timer's own SplitMix64 state per unit, on the real steps), `EnemySpec.ProximityRing` (instead of a
  gun; `EnemyForce` fires it), a ground unit's `drift` along its `GroundUnit.currentRadians`,
  `WaveSpec.Field` / `Formation.FIELD` (`Formations.field`, `Spawn.Field`), `LevelScript.Naval` and
  `NavalUnit` (`withConvoy`), `AllySpec.slams` / `flakSeconds` / `damageable()`, `Ally.State`
  `GLIDING`, `LANE` and `AWAY`, `Convoy`'s naval mode (`sail`, `lane`, `slammable`, `afloat`,
  `copyFrom` for the boss checkpoint), `Secondary.afloat`, `Objectives.sunk()` / `bossDownAfloat()`,
  `CueTrigger.ALLY_HIT` (repeating per unit), `SimEvents.Type.ALLY_FLAK`, and in `Sortie` the
  package-private `slamAllies(lane)`, `lastAllyHit()`, `navalConvoy()`, `allyName(k)`, `allySpec(k)`,
  `allyLane(k)`, `damageableAllies()`, `alliesAfloat()` and `afloatFailed()`. `content`: the keys
  above (`EnemyData.Submerge`, `Attack.within`, `AttackChange.within`, `Terrain.drift`,
  `BossData.lanes` as `EnemyData.Lanes`, the wave's `x` / `size` / `spacing` / `current`, the nest's
  `current`, `LevelData.Convoy` / `ConvoyUnit`, `Secondary.afloat`, `CueEvent.ALLY_HIT`,
  `AlliesData` `stations`, `DamagedBy.slams`, `Flak`), mapped by `SimSpecs` and `PartERules`, checked
  by `ContentValidator.checkPartE`. Settled on the way: a field's leading (bottom) edge enters at the
  top edge at `t` (the schema's "top edge" read as the edge that enters first); a field unit's first
  swap after a seeded 0–`every.max` s, the later ones `every` apart, so a field swaps while it is on
  the screen; a ring due while the ship is closer than the bullet readability distance (72 px) waits;
  the convoy glides out in the first step the level clock is halted and back in the first step it
  runs again (a boss that dies before the halt leaves the convoy at its stations); a ship is in the
  lane its centre's x lies in, also while it glides; the frigate's flak bursts only while it holds
  its station, their places following from the burst count (no random draw); a level-end cue's
  `allies` counts the damageable units afloat. The new state is hashed only for drifting,
  submerging or pulsing units, a naval convoy and an afloat objective, so every replay hash stays.
- 2026-10-08: M5 part E step E2c built the arena boss (the Harbour Kraken; E5–E7 = a). `sim`:
  `BossSpec.Arena` (`Lanes`, `Arm`, `Slam`, `Spot`, the part layers), `BossSpec.PhaseArena`
  (`Slamming`, `Surface`, `Release`, the drop), `Chain.slam`, `BossSpec.anchored()`; `SlamArena`
  (per arm a slam cycle on the real steps, the alternate choice with the severed-half redirection,
  volleys with hard's second lane, the surfacing part's states and its attacks' timer, the current
  layer per part, the weak spots on the shot's line; a public read-only view for the game: arm
  states and shares, lanes, telegraphed lanes and the time to an impact, the surfacing state, the
  glow); `SetPiece` anchored (`anchorAt`, `ground`, `approaching()`, `arena()`, `partLayer`,
  `partTouches`, `partInBlast`, `partOnField`, `damagePartAt`), the slam chains laid along their
  lanes and not armour, the fans from the surfacing part's lower edge; `BossActions` `telegraph`,
  `slam`, `surface`, `release`, `drop`, `shipLanes`; `Sortie` easing into an arena of speed 0
  (`easeDistance`), anchoring the boss at the halt scroll, the impact on the ship, Rook and the
  ships in the lane (`slamLane` → `slamAllies`), the release planned once with its own generator,
  `first-telegraph` and `boss-part-destroyed` (`CueTrigger.FIRST_TELEGRAPH`, `BOSS_PART_DESTROYED`
  with its parts joined by `|`, `Radio.among`); `PlayerFire`, `SpecialSlot` (the Airstrike) and
  `Wingman` read each part's current layer (an `anti-ground` shot doubling on a surfaced part, a
  landing blast hitting a part on `ground`, a sunk part out of reach of all but the torpedo and the
  Smart Bomb); Rook's goal moves beside the telegraphed lanes; `SimEvents` `TELEGRAPH`, `SLAM`,
  `SURFACE`, `DIVE`. `content`: `EnemyData.Anchored`, `PartData.layer` / `spots` (`SpotData`),
  `ChainData.motion`, `BossData.slam` (`SlamData`, `Splash`), `PhaseData.slamming` / `surface` /
  `release` / `drop`, `Until.slams` / `below`, `Hook.telegraph` / `lanes`, a section's `speed: 0`,
  `CueEvent.FIRST_TELEGRAPH` / `BOSS_PART_DESTROYED` and the cue's `parts`; mapped by `SimSpecs`,
  checked by `ContentValidator.checkArenaBoss` / `checkArenaLevel`. Settled on the way (for review in
  round 33): the bar at the arena's start, the engagement at the halt; the arm on the ground until its
  sink starts; the splash's points and directions; the eye counted on the shot's line; the volley's
  first after the phase's delay and a busy arm sitting one out; the field's rows; the halt scroll
  carrying the ease's 68.8 px. Its state is hashed only for an arena boss, so every replay hash stays.
- 2026-10-09: M5 part E, Level 11's follow-ups (user, 2026-10-09): the destructible `group` and a
  trigger's `easy` / `hard` `hits` are marked built (step E3a built them). New and built: the
  `convoy` block's **`hold_clear`** (px of scroll after the halt) and a unit's **`hold`** point, so a
  naval convoy waits clear of the boss's platform before gliding back (`Convoy.HOLD`; the hold's
  scroll is in the state hash, only for a naval convoy); a **sunken trigger** in the torpedo's target
  pick (`PlayerFire.TRIGGER_SERIAL`, a ground object's serial above the parts'). Every replay hash
  stays (no recorded level has a naval convoy or a torpedo).
- 2026-10-09: Schema text for M5 part F (Level 12; user decisions F1–F9 of 2026-10-09 and the
  stated defaults), planned until the loader reads it: for enemies, the `chase` movement, the
  `latch` block (`slots`, `spots`, `damage_factor`, `shake`, `stun`, `throw`), the `drain` attack,
  the hooks `drain` and `latch_slots`, the events `LAMPREY_LATCHED` and `LAMPREY_SHAKEN`; for
  levels, the `weather` block (`gusts`, `drift`, `lightning` with the `LIGHTNING` event,
  `occlusion`, `rain`), a circle's `clockwise`, side release points for whirl clusters, chaser
  `swarm` and `stream` waves from the rear, `sondes` with `SONDE_COLLECTED` and the secondary
  `collect: sondes`, a drifting `low-air` trigger, `drop_factor`, a placed pickup's `at`, the event
  prompt, the radio event `first-latch`, the music's `section_db` and the ambience key
  `earth-storm`. The F5 switch needs no new stem key: the built `stems` (section: `base` or `full`)
  already switch by section; only the eye's level is new. Everything new is to be hashed only in a
  level that uses it (the wind exactly zero elsewhere), so the replay hashes of Levels 01–11 stay
  and `ReplayTest` needs no re-recording.
- 2026-10-11: M5 part F step 3, Level 12's simulation (pending the user's review in the combined
  round): `LevelScript.Weather` (gusts sharing one push, seconds, ramp and telegraph; drifts as time
  spans the loader takes from their section; the lightning times) read by `Wind`, a pure function of
  the level step plus the next flash; `EnemySpec.Chase` and `EnemySpec.Latch` (the `drain` rate inside
  the latch); `WeaponSpec.flanking` for the `side` and `rear` traits; `WaveSpec.clockwise`;
  `LevelScript.Sondes`, `Secondary.sondes`, `GroundObjectSpec.drifting`, `dropFactor`,
  `PlacedPickup`; events `LIGHTNING`, `LAMPREY_LATCHED`, `LAMPREY_SHAKEN`, `SONDE_COLLECTED` and the
  radio trigger `FIRST_LATCH`. The new state is hashed only in a level that uses it, so every replay
  hash stays and `ReplayTest` needs no re-recording; nothing new allocates per step.
- 2026-10-11: M5 part F step 4, Level 12's loader (pending the user's review in the combined round):
  the part F keys are read strictly and checked (`PartFRules`, `ContentValidator`): an enemy's
  `movement.chase`, the top-level `latch` block and the `drain` attack (only with a latch, its unit's
  only attack), the hooks `drain` and `latch_slots`; a level's `weather` (no simulation weather for
  a block with only rain and occlusion; a drift's push 0 leaves it out; hard's `extra` gusts merged
  by time; never more than 3 flashes a second), `sondes` (not in a level with a boss), the secondary
  `collect: sondes` (with its `label`, optionally a `name`), a `low-air` trigger's `drift` (y up), a
  pickup's `at` (exactly one of `at` and `dropped_by`), a wave's `clockwise` (circles only), a whirl
  cluster's `at` up to 40 px outside the side edge its `from: sides` wave names, a `stream` from the
  rear only for chasers and a swarm of chasers without `paths`, an event prompt (`event` instead of
  `t`, never due by the clock), the radio's `first-latch` and the prompt's only with a unit that
  latches, `drop_factor` and `music.section_db` (sections that exist, at most 0 dB). A weapon with
  the `side` or `rear` trait flies `WeaponSpec.flanking`.
- 2026-10-11: Schema text for M5 parts G and H (Levels 13 and 14; decisions G1–G9 and H1–H10 = a,
  the gap checks' recommendations, **pending the user's review** in the combined round), planned
  until the loader reads it: part G (sim in progress) the `defend` primary, the halted section
  without an arena and its `ramp`, `targeted_by` modes, `cross`, `lanes`, side-entry ground units
  with a `drift`, the `integrity` secondary, `drops`, `ally-below`, `end_jingle` / `jingle`,
  `water_splash`; part H `root_bursts`, the boss `lob` and `sweep`, `launch` with `ambush`, a phase
  `move` with `stump`, `part_layers` on an anchored boss and a daylight section after an arena. New
  state is hashed only in a level that uses it, so the replay hashes stay.
- 2026-10-12: M5 part G step G4, Level 13's loader (pending the user's review in the combined round):
  the part G keys are read strictly, checked (`PartGChecks`) and mapped (`PartGRules`) with their final
  names: `objectives.defend` (`ally`, `at` y up, `from`, `duration`, `targeted_by`, `easy` / `hard` `hp`
  and `targeted_by`), a section's `ramp` (only on a timed halt; the ease in from the halt's first section,
  out from its last), `lanes`, `from: all` for a `cross`, a change's `pairs`, a nest's `side` (`from`,
  `drift`; `at` `[t, y]` y up), the secondary `integrity`, the top-level `drops` (Java `pointDrops`), the
  radio's `ally-below` with `below`, the music's `jingle` (`t`, `fade`) instead of the planned `end_jingle:
  false`, `water_splash`; the allies' `follows: ground`, `dives` and `lobs`. An atmosphere split at a time
  is a `peak` to the section's end (`atmosphereStretches` drops the empty stretch after it, so the backdrop
  does not dip). Rejected: a level-wide `targeted_by` (it is the defend's ally's hook, as the escort's
  `hook`), a per-wave `pairs` (a difficulty's change, as hard's Stingers need), `end_jingle: false` (a YAML
  boolean in a file-name key).
- 2026-10-12: M5 part H step H4, Level 14's loader (pending the user's review in the combined round):
  the part H keys are read strictly, checked (`PartHChecks`) and mapped (`PartHRules`) with their final
  names: `root_bursts` (`telegraph`, `first_shot_delay`, `spacing`, `group`, `easy` / `hard`, `bursts`
  with `t`, `x`, `y`, `growths` and `easy` / `hard` growths), a boss's `mortar` (the lob) and
  `laser-sweep` (the sweep) attacks, `staggered`, a window spawn's `ambush: {most}`, the sway after a
  lift, a `groups` secondary's tracker `label`. Rejected: a new `lob` / `sweep` pattern name (the enemy
  patterns from a part say the same), `part_layers` `air` (the core reaches `air` by the lift's
  `layer`), a `stump` key (the stump is the boss's state) and `ambush` on a window (the spawn's). A phase
  with a `move` now keeps its arena keys (its `drop`); Level 07's carrier has none, so nothing else moves.
- 2026-10-12: M6 part D (Q14 = b, pending the user's review in round 35): Rook's guns and barks
  tables (`rook-guns`, `rook-barks`) and the act outro's page table (`act-outro-pages`) are rendered
  by `tools/sync_tables.py`; their wording sits under `notes` (`notes.scale`, `notes.trigger`,
  `notes.image`). The specials, allies and economy tables stay hand-written (`later: Act 3` tooling; round 44 I.4 dropped the tag, see below).
- 2026-10-12 (user, round 35): Q14 = b accepted: Rook's guns and barks tables and the act outro's
  page table stay rendered by `tools/sync_tables.py`; the specials, allies and economy tables stay
  hand-written until Act 3.
- 2026-10-19 (refactor, the user's request of 2026-10-18): the game module's biggest classes are split without
  behaviour changes: `LevelRenderer` keeps only the pass order (`GroundPasses`, `UnitPasses`, `SetPiecePasses`,
  `PlayerPasses`, `PropPasses`, the `KindLooks` registry, `PlayFrame`), `FlightSounds` routes events to six sound
  families sharing one `SoundMix` (`PlayerCues`, `BossCues`, `UnitCues`, `AllyCues`, `ArsenalCues`, `LevelCues`),
  `LevelScreen` hands events to `RadioEvents` and `EventEffects`; `PassOrderTest` pins the draw order.
- 2026-10-19 (build speed, the user's request of 2026-10-18): `spriteClaims` writes each sprite's atlases from
  `SpriteUse` to `build/atlas-claims.txt` (the only place the pipeline reads the content); `packAtlases` packs
  the atlases in parallel from that file and is cacheable (inputs hashed by relative path), so an unchanged tree
  restores it from the build cache; duplicate-frame entries are sorted by name and index and backdrop pages are
  numbered in folder order, so the output is byte-reproducible.
- 2026-10-19 (refactor step LS-3, refactor-levelscript.md; pending the user's review): `LevelScript` is split into
  four sub-records (`ObjectiveSpec`, `GroundSpec`, `HazardSpec`, `DropSpec`, each with a `NONE` and one-line
  withers) and 13 top-level types; it keeps 15 components and the cross-concern checks. Steps LS-4/LS-5 moved the
  callers to the sub-record accessors and withers (`level.ground().belts()`, `level.toBuilder().ground(...)`) and
  deleted the temporary delegate accessors, flat builder setters and level-wide withers; only `withGates` stays on
  `LevelScript` (it also adds the anchors to the ground units). The vents' and placed pickups' time sorts and every check's wording are kept (golden replays unchanged).
- 2026-10-20: round 42 closed: the refactors (X.a a-h: `LevelScript`, `LevelData`'s `*Block` interfaces, `SimSpecs`'s rules classes, the autopilot's plays, the simulation split, the level-metrics tool) and the Level 19 schema defaults (`turret`, `belts`, vents, `arc: up`; B.a-B.d) approved by the user, all yes.
- 2026-10-20: M7 part G step G2 (Level 20, pending the user's review in round 43): the reverse-scroll keys (a
  section's negative `speed`, entries by travel, a front wave's `warning` as the BOTTOM lesson rather than a new
  `warn` key), the `pursuit` formation (the unit's defaults, the wave's block with an explicit `break_at`), the
  trailing drone (`station: trail`), the strip backdrop (`strip`, pieces by `row`), the music's `ambience_layers`,
  the flak's `band: top` and the barks' `top_lines`; checked by `RetreatChecks` (and `BackdropCheck` for the strip).
- 2026-10-20 (step C1G, the user's request): `:content:allocationTest` runs the zero-allocation tests
  (`steppingAWholeLevelDoesNotAllocate`, `theFightAllocatesNothingPerStep`) once more in their own forks with
  `-XX:TieredStopAtLevel=1`: C1 has no escape analysis, so an allocation that only C2 removes (`HazardPlay`'s
  `double[2]`, which failed only on the 3-core macOS runner, CI 2026-10-20) fails on every machine. `check` depends on
  it and it runs next to `test`; measured +34 s on `:content:check` wall time (4 m 42 s → 5 m 16 s, machine under
  load), the task itself 62 s, alone 12 s of tests; with b239705 reverted it fails Level 16 (7.66 MB) while `test`
  passes.
- 2026-10-20 (user, round 44, I.4): the specials, allies and economy tables stay **hand-written**; the `later: Act 3` tag is dropped (they are stable and rarely edited), so no part I step renders them through `tools/sync_tables.py`.
