---
title: Data file schemas — Act 4
design: approved
implementation: in-progress
art: n/a
updated: 2026-10-26
---

# Data file schemas — Act 4

## Summary

M8's additions: Europa's underwater rules and the level and unit keys of Act 4's parts A to H. Moved unchanged from the [architecture](../README.md#data-file-schemas)'s Data file schemas, which keep the shared conventions (keys, units, sizes) and the index of every schema.

## Design

- **Europa** (`world/europa/data.yaml`, M8 part A, `EuropaData`): the **`underwater`** rules a level or section with
  `medium: underwater` flies by: `bullet_speed` (every bullet's speed at its spawn), `acceleration` (the drag: the
  ship's, Rook's and the Bulwark's acceleration and braking), `damage` (a weapon without `anti-sub`, after
  `anti-ground`'s ×2; not specials, not the enemy's damage, not an energy weapon's), `energy_damage` (M8 AW: the
  `energy_weapons`' damage, in place of `damage`), `reach` (the share of the play field's height the
  `energy_weapons` reach) with `energy_weapons` (beams or lasers that fly to the screen's edge), `torpedo_range` with
  `torpedo_weapons` (each `anti-sub`), `ease` (s the factors ease over at a waterline, for new projectiles) and
  `hydro_kit` (the level whose hangar visit fits the hydro-kit, its one-off notice; Act 4). The README's *Under water
  rules* numbers table is rendered from it (`underwater`).
- **Level, M8 part A** (Act 4's medium and special limits; loaded and checked in step A2, mapped in step A4): an
  optional **`medium`** (`air`, the default, or `underwater`: the play plane is under water and Europa's rules apply)
  on the level and on a section (a section's own, else the level's: `LevelData.mediumOf`; a section whose medium
  differs from the one before it is a waterline at its start), only from Act 4's first level on; an optional
  **`special_limits`** (the specials forbidden, by their slugs, `orbital-lance`) on the level and on a section,
  replacing its setting's list from the specials' `limits` (a section's own, else the level's:
  `LevelData.forbiddenSpecials`); every special named there must be in a setting's list (its reason word).
- **Level and unit, M8 part B** ([Level 22](../../../campaign/act-4-deep-water/level-22-ice-shelf/README.md), the
  [Drill Rig](../../../enemies/ground/drill-rig/README.md); loaded and checked in step B2, mapped in step B3; the
  defaults accepted in round 51 (2026-10-22)): the **`vents`**' **`look`** (`furnace-vent`, the default, or
  `ice-geyser`: how the game draws and sounds the rows; `Vents.lookOrDefault()`, the simulation never reads it) and a
  row's own **`tell`**, **`period`**, **`ripple`** and **`easy`** / **`hard`** changes of them (a row's value on a
  difficulty: its own change, else its own value, else the rows' on that difficulty, `Vents.periodOn(row,
  difficulty)`; each row's period holds its whole ripple on every difficulty). An `escapes` secondary's **`easy`** /
  **`hard`** `{allowed}` (0 or more: how many may get away on that difficulty, `Secondary.escapesAllowed(difficulty)`;
  fewer than the units it counts on every difficulty); an `escapes` objective counts the **ground targets** of its
  enemy too (a wave or a ground target of it must be in the level). The radio events **`first-geyser`** (the
  attempt's first vent tell, once; only with `vents`) and **`target-destroyed`** (the attempt's first unit of the
  escapes secondary's enemy destroyed, once; only with an `escapes` secondary). The intel wave strip's **`TARGET`**
  marks (each ground target of the escapes secondary's enemy at its time). A unit's `part_list` may hold **several
  `destroyable` parts** carrying its `attack` (the Drill Rig's four pumps with `attack: ring`): the attack stops when
  the last of them is lost, and a part's **`slows`** (s, only on a unit's part that carries an attack) adds that much
  to the attack's interval when it is lost; the hook **`attacks.<name>.aimed`** (only on a `ring`: that many bullets
  aimed at the ship besides the ring with each volley, the hard rig's shard).
- **Level and unit, M8 part C** ([Level 23](../../../campaign/act-4-deep-water/level-23-through-the-ice/README.md), the
  [Eel Swarm](../../../enemies/naval/eel-swarm/README.md), the [Glassfin](../../../enemies/naval/glassfin/README.md), the
  [siphon pump](../../../enemies/ground/siphon-pump/README.md) and the naval *Returning units under water*; loaded and checked
  in step C2 by content's `DeepWaterChecks`, flown from step C1, mapped in step C3; the gap check's defaults accepted in round 52 (2026-10-22)): a part's **`attacks`** (two or more of the unit's attacks by name instead of one `attack`:
  the eel head's aimed shot and fan; only on a segment chain's vital part; `PartData.attackNames()`); a segment chain's
  **`cut: {scatter, seconds}`** (instead of `delay`: the rear part keeps its heading at `scatter` px/s for `seconds`,
  firing nothing, then dies from the cut backwards; only on an air chain; `Cut.scatters()`); a `path` movement's
  **`sway`** (`amplitude` px, `frequency` Hz: a sine across the authored path, only on a segment chain's head); a
  flock's **`sway`** (the same, across the leader's route: the Glassfin's ribbon) and **`ram`** (`after` s on the screen,
  the `share` of a school that breaks off by each member's own seeded draw, a `tell` s glint, the dive `delay` s after
  the glint ends (step C3), straight down the ship's lane at `speed` px/s, no homing, leaving at the bottom) with the hook
  **`ram.share`** on easy and hard; an attack's existing **`tell`** for the eel's stripe. A unit's **`underwater`** block
  (its changes in a wave or a nest whose entry time lies in a section with `medium: underwater`, applied when the script
  is built: `layer` instead, `submerge: false` drops the surface/submerge swap, `drift` its terrain drift instead (0:
  none), `look: floor` the game's base for a ground unit; `EnemyData.inWater()`): the Driftjelly's `{layer: air,
  submerge: false}`, the Reef Spitter's `{drift: 0, look: floor}`. A level's **`underwater_look`** (presentation only, the
  game's under-water grade: `ambient` 0–1, `tint` `rrggbb`, `caustics` 0–1, `snow` particles on the screen, `headlights`
  a strength 0–1 (the default beam, 200 px over a 44° sector) or `{strength, reach, angle}` (reach px, the lit sector's
  full angle in degrees; step LX: light that brings the world in the cones up toward its full colour, `SectionsBlock.Headlights`),
  `rim` 0–1 (optional, step LX: a faint light along the terrain's rock edges, the notches' and the islands'); only on a level that is under water somewhere; the game's defaults where it is left out) and a section's own **`underwater_look`** (any of
  those values instead of the level's, only on an under-water section of a level that gives the block: part D's Level 24 darkens section by section;
  `LevelData.underwaterLookOf(section)`). The radio event **`waterline`** (the level's first waterline, the start of the
  first section whose medium differs from the one before it, `LevelData.firstWaterline()`; content resolves it to that
  time, so it plays as a timed line with no simulation event; only in a level with a waterline). A pickup's
  **`dropped_by.head_first: true`** (only the wave's first unit of a segment chain's wave: it drops only when the
  chain's head is destroyed before any other part; `Carrier.dropsHeadFirst()`). The intel wave strip's **`WATERLINE`**
  marks (each waterline at its time, `LevelData.waterlines()`).
- **Level and unit, M8 part D** ([Level 24](../../../campaign/act-4-deep-water/level-24-abyssal-plain/README.md), the
  [Abyss Ray](../../../enemies/naval/abyss-ray/README.md), the [Barbfin](../../../enemies/naval/barbfin/README.md) and the
  [spawning reef](../../../enemies/ground/spawning-reef/README.md); loaded and checked in step D2 by content's `AbyssChecks`,
  flown from step D1, mapped in step D3; the gap check's defaults accepted in round 53 (2026-10-23)): a unit's
  **`rise`** movement (`shadow` s on `sub`, growing in the ship's lane `ahead` px above the ship, reached only by
  `anti-sub` fire and the Smart Bomb, no contact; the `rise` s, the layer flipping to `air` at its middle, no contact
  until it ends; the `glide` s at `speed` px/s toward the far side, its attack firing from the rise's end; the `sink` s,
  back to `sub` at its middle, then gone as escaped; `Rise.visit()` the whole of it): its stat block's `layer` is `sub`
  (the only unit that may give it) and the rise is its whole movement; a `carrier + escorts` wave's escorts are held until
  its rise's middle and fan out from it; a rising unit's `fan` may come in a `burst` (as a boss's). A level flies one
  rising unit to a wave and never two on the screen at once (its waves at least a visit apart, every difficulty). A
  `hover`'s **`above_ship`** (`[min, max]` px instead of `y`: it stops that far above the ship's y at the moment it stops,
  a seeded draw, drifting its **`drift`** px/s sideways while it holds (0 if not given; only with `above_ship`) and
  turning toward the ship; only on an `air` unit with the hover's `seconds`; `Hover.heightFromTop()` throws for it; the
  simulation's hover depth is then 60–460 px below the top edge: never higher than 60, a path from the top ends at 460). A periodic `spawn` may release a **flock** unit (the spawning reef's Glassfin
  fry: each flies out and rams on its own, without a flock). The radio event **`first-ray`** (the attempt's first rising
  unit's shadow, the simulation's `RISE_SHADOW` sighting; only in a level with a rising unit; the cue's time is the first
  rising wave's, `AbyssChecks.firstRise`, for the radio timeline). A backdrop piece's
  **`keep_clear`** (px, only on a ground piece: no ground target's placement and no walker's walk start, its path's first
  point, lies within that far of the piece's centre wherever it is placed; Level 24's relay beacons, 100). The intel wave
  strip's **`RAY`** marks (each wave with a rising unit at its time, flown at medium). The stems' existing `stems`
  override plays the base stem again in a later section (Level 24's `{4: base}`).
- **Level and unit, M8 part E** ([Level 25](../../../campaign/act-4-deep-water/level-25-siren-trench/README.md), the
  [Siren](../../../enemies/naval/siren/README.md) and the [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md);
  loaded, checked (`TrenchChecks`) and mapped (`TrenchRules`) in step E3, flown from steps E1/E2; the gap check's
  defaults accepted in round 54 (2026-10-23)): the terrain's **`notches`** (the side caves: each a `side`, `left` or
  `right`, a `scroll`, the row of its middle, a `width` along the wall (rows) and a `depth` behind the wall's line, px;
  on its wall's rows, the notches of one wall not overlapping; open water with rock on three sides: `TerrainBlock.rock`
  opens them, the geometry rules read the walls' lines without them; an index into the list names a notch). A wave's
  **`cave`** (its units swim out of that notch, spawning at its back: `from: sides` with the notch's wall as its
  `edge`, the notch on the screen at the wave's t, one group, the entry the same on every difficulty) and **`wall`**
  (`staggered` or `opposite`: its units are nests on the walls' feet, 12 px out from the collision line at their row,
  entering with the ground; `from: sides` (counted as side entries by the intel) with an `edge` `left` or `right` for one
  wall or none for both, staggered a nest every 1.5 s, the walls taking turns from the right; `opposite`: pairs at one
  row, no edge, the left one's ring 1.5 s later; the formation `turret nest`, and a `turret nest` wave is a wall wave; the
  unit a fixed `air` nest with `terrain` movement). The validator plans the nests on every difficulty as the simulation
  does: at most four on the screen, never two of one wall within 90 px, every two rings at least 1.5 s apart and at most
  one ring in flight per wall, no nest on a notch's mouth (its rows ± 20 px) or within a backdrop piece's `keep_clear`;
  no coiling unit holds while the flyable width at the top edge is under twice its `inner` radius plus its hit box (no
  Nautilus in the 190 px narrows); in a terrain with notches every enemy ground target stands in the 24 px foot band of
  a wall's or an island's line (a ledge). A unit's **`ring`** attack with a **`slow`** block (`radius`, `band` px,
  `factor` of the speed and acceleration, `seconds`, `immunity` s; the attack's `speed` px/s (sound: not the water's
  × 0.7, not the bullet-speed lever), `interval`, `tell` and `first_shot_delay`; no bullet, count or reach; one to a
  unit, a fixed `air` nest's with `terrain` movement); its difficulty hook **`slow`** (`factor`, `seconds`,
  `immunity` instead). The movement **`coil`** (`outer` → `inner` px over `closing` s spiralling in round the ship,
  never closer than `inner`; `opening` s (its last `tell` s the crown's tell), `open` s open at `open_speed` px/s;
  `offset` s between a wave's units' cycles, the units orbiting in turn the other way round; the whole movement of an
  `air` unit, which leaves after its wave's `hold`), with the armour **`shell`** (closed: every shot glances off but the
  specials' blasts and `anti-sub` fire; only and always with a coil) and its `spiral` fired `while: open`; the hook
  **`coil`** (`open` instead). The intel's **`CAVE`** marks (each cave wave flown at medium, at its time: the panel's
  side arrows at sensor L3).
- **Level and unit, M8 part F** ([Level 26](../../../campaign/act-4-deep-water/level-26-blackwater/README.md) and the
  [Glow Angler](../../../enemies/naval/glow-angler/README.md); loaded, checked (`DarkChecks`) and mapped (`DarkRules`) in
  step F2, flown from step F1; the gap check's defaults accepted in round 55 (2026-10-24)): the movement
  **`lunge`** (`trigger` px from the unit's centre to the ship's, `speed` px/s for at most `seconds`, `stop_short` px
  short of where the ship was at the trigger, `retreat_speed` px/s for `retreat_seconds`; an `air` unit that drifts
  (`drift`, a field's current) between lunges, with exactly one attack, its `ring` (`count`, `bullet`, `speed`, the
  re-arm `interval` after the ring, no reach): the lunge's, not a gun; `EnemySpec.Lunge`) and the hook **`lunge`**
  (`trigger` instead). A level's **`rook_loss`** (`trails`, `t`, `spin` s: the torpedo trails and the end of Rook's
  barks at `trails`, the hit at `t`, the spin-down off the bottom edge in `spin`; `trails` after the launch, `t` after
  `trails` and inside the level, no boss; `LevelScript.rookLoss`). The look **`underwater_look`**'s dark keys
  (presentation only): a section's **`ramp`** (`to`, `from`, `until`: its ambient eased to `to` over that level time,
  inside the section; only a section's own look), the level's **`lights`** (static pools on the ground: `scroll`, the
  terrain row of the centre (below 0: on the screen at the start), `x`, `radius` px, `colour` `rrggbb`; on the level's
  rows) and **`escort_lamp`** (the radius of the escort's light pool, px); M8 part G: a level with them is *black water*
  (the units' glow-only frames, the crown hum, the headlights' clicks) only where a section's ambient or its ramp's
  `to` is below 0.3 (`DarkLight.blackWater`): Level 27's lit vent field draws its pools and keeps its plain looks. A radio row's **`static_lead`** (s the HUD
  portrait's interference shows before a timed line, from the level's start on) and **`duck_db`** (dB the music ducks
  under the line, above 0). A wave carrier's **`unit: centre`** (a `field` wave of one group: the unit nearest the
  field's centre where the scatter puts it; `WaveSpec.Carried.CENTRE`). The music's **`stem_times`** (`t`, `fade` s and
  `stems` and/or `db`: from `t` the theme plays `full`, `base`, an extra stem of the theme alone by its name (`dark`,
  `warm`: `<theme>-<name>.ogg` on the same grid) or `none`, at `db` (at most 0); in time order, each after the fade
  before it, inside the level).
- **Level and unit, M8 part G** ([Level 27](../../../campaign/act-4-deep-water/level-27-hydrothermal-vents/README.md)
  and the [Depth Hunter](../../../enemies/naval/depth-hunter/README.md); loaded, checked (`VentChecks`, `FoundryChecks`)
  and mapped (`FoundryRules`, `AbyssRules`, `TrenchRules`) and flown in step G1; defaults accepted in round 57 (2026-10-25)): the
  `vents`' **`look: heat-plume`** with **`shape: disc`** (`square` the default: each grate's flame a disc `width` px
  across; `look: heat-plume` and `shape: disc` go together), **`enemy_damage`** (each `air` or `low-air` unit over a
  burning disc takes it once per burst; the kill is the player's) and **`bend`** (° below 90: a player's shot, an enemy
  bullet or a homing torpedo entering a burning disc turns that far away from its centre, once; not beams, the Lance,
  bombs, shells, mines, depth charges or lobs); a disc row's neighbouring xs at least 100 px apart; the discs burn the
  ship, Rook and the Bulwark (`HeatPlumes`, events `PLUME_BEND` and `PLUME_BURN`, `VENT_HIT` value 2 for the Bulwark).
  A hover's **`drift`** at its `y` too (px/s sideways while it holds at its depth, its facing turned up the screen: the
  Depth Hunter's). An attack's **`homing`** on a unit that is not a boss: its one `aimed` attack with **`aim: facing`**
  (each volley one torpedo launched along the unit's facing, `Missiles.launchAlong`; the unit hovers). A **`column`**
  wave **`from: rear`** of a unit hovering at its `y` (the Depth Hunters: up from the bottom edge in the column's x,
  unit i of n holding at the depth's min + i ÷ (n − 1) of its span, 16 px between hit boxes; warned at the bottom
  edge; never two such columns on the screen at once); a **`rear ambush`** of a unit that holds `above_ship` (the Barbfin: up the ambush lanes from the bottom edge,
  stopping on the way up; warned). A `turret nest` wave's **`on_ground`** (`[x, …]` px, one per unit on every
  difficulty, `from: front`: nests of a fixed `air` unit standing on the ground at one row, entering with it at the
  top edge, facing down, their songs 1.5 s apart in turn; no terrain needed; `WaveSpec.withGroundNests`). Drawn and
  played in step G4: the vents' look names their sprite root (`SpriteUse`: `heat-plume*`; `PlumeLooks`, the mouth's
  pool by phase in `DarkLight`), the Hunters, their torpedoes and the relay beacons (`HunterLooks`, `BeaconLooks`), the
  sounds (`VentFieldCues`: in a level with Depth Hunters the homing missiles are their torpedoes); `stem_times`'
  `stems: none` with the music's `motif` leaves the motif layer alone over the bed (Level 27's wreck beat); the
  intel's strip marks a `column` `from: rear` (`Intel.Mark.Kind.COLUMN`).
- **Level and unit, M8 part H** ([Level 28](../../../campaign/act-4-deep-water/level-28-abyssal-maw/README.md), the
  [Abyssal Maw](../../../enemies/bosses/abyssal-maw/README.md) and the [Act 4](../../../campaign/act-4-deep-water/README.md)
  end; flown by sim steps H1 (`CoilArena`) and H2 (`CruiserRun`, the floor, `MawHazards`), loaded, checked (`MawChecks`)
  and mapped (`MawRules`, `MawBlock`) in step H3; accepted in round 58 (2026-10-26)). A boss script's **`coil`** (an
  `anchored` boss without lanes, a stride or a breach, every part starting `sub`, its head the first vital part with a
  `while: open` spot, the throat; screen px, y below the top edge, turned once into the simulation's frame):
  **`intro`** (s from the arrival until the coil and the head are up: invulnerable, nothing fires); **`wind`** (s,
  at most `intro`: its last part, the head swimming in from off the screen past the path's last point and the body
  winding in behind it along the path; the dive's way back winds in the same way over `return`; round 58 F.2);
  **`body`** (`segments`, `spacing` px, `hitbox`, `path` `[[x, y], …]` the centre line from the head's end, run on
  straight past its last point; `bend` px the radius its corners turn on (the neck rises from the head and bends into
  the first leg at its first point at the latest), every segment facing along the line as it lies; `inner` `{left, right, bottom, top?}` the inner flanks; `clearance` px the ship and the escort keep
  inside them: the wall, no damage); **`sacs`** (`parts`, destroyable, outside the bar; `swell` s before each ring: the
  phase's `ring` attack breathes from them in turn); **`jaw`** (`glow`, `open` s: a phase's `fan` other than the wall's
  fires from the throat, the throat ×2 while open); **`wall`** (`attack`, one of its `fan` attacks fired straight down
  after a `glow`; `gaps` 1 or 2 orbs left out `near`–`far` px beside the ship's x at its height and within `chain` px of
  the last wall's gap); **`sweeps`** (`segments` of the bight, `tell`, `swing`, `hold`, `back` s, `reach` and
  `bottom_reach` px, `bullet` once a sweep with the push, `along` the side sweeps' range of the ship's y, `across` the
  bottom one's of its x, `phases` `[{phase, every, sides: [left|right|bottom, …]}]`); **`dive`** (once in `phase` when
  the head falls below `below`: `slide`, `quiet`, `shadow`, `lunge`, `body` (the body streaming up the column and out
  of the top), `return` (winding back in) s, the column `width` px at the
  ship's x within `column`, `bullet` once to what the lunge passes; `sub` in the dark); **`implants`** (`parts` that are
  not the head, from `phase` in turn: `tell`, `open` s, a hit counting on the head's HP at the part's multiplier,
  armoured while shut; its `aimed` `attack` after each `arc`); **`shed`** (`enemy` a segment chain built with
  `segments` (`LevelData.EnemyChange.segments`, which a level's `difficulty.<easy|hard>.enemies` may give too), `tell` s
  of plate flare, `points` `[{side, at}]` (`at` px below the top edge on a side, from the left on the bottom), `phases`
  `[{phase, every, most}]`; no edge warning; out of a kill ratio with `boss_counted: false`, still paying). The
  difficulty hook **`coil_arena`** (`sweep_tell`, `shadow`, `implant_open`, `shed_segments`, `wall_gaps`; the attacks'
  counts and bursts by the usual `attacks` hook). A level's **`cruisers`** (the finale's run, on the level clock: `rise`,
  `run`, `end`, `fall` s, `hull` `[w, h]`, `inner` `[left, right]` the hulls' inner edges; `broadside` `{band, tell,
  shells, speed, bullet, every, double_after?, free}` (no `double_after`: never two bands), `flak` `{markers, every,
  fuse, radius, scatter, bullet, ring, speed, ring_bullet}`, `point_defence` `{burst, speed, bullet, every, turrets}`
  (turret heights px below the top edge on the hull's inner edge); `easy` / `hard` `{tell, double_bands, markers,
  flak_every, burst}`; invulnerable `low-air` set pieces, no bounty, above water: no difficulty lever on their numbers;
  only after a boss's arrival, ending with its fall by the level's end; `CruiserRunSpec`) and **`armour_floor`** (points
  the armour stops at from the boss's kill to the level's end, every difficulty, cleared on a retry; only with a boss;
  `LevelScript.armourFloor`). A section's `medium: air` after a water section is the first water → air crossing (Level
  28's breakout). A terrain's **`contact.damage`** may be 0 on a difficulty (a push without damage: Level 28's shaft).
  The radio events **`boss-arrival`** (the boss's bar; the line plays though the arena's clock is halted) and
  **`boss-dive`** (a coiled boss's dive, queued as a `PHASE` line); both only in a level with a boss. An act's
  **`next_act`** moves to Act 4 (`{act: ACT V, name: THE BELT, setting: CERES}`: the release ends after Level 28).
  Step H6: the music's **`motif_after_boss`** (`{from, to, fade_in, fade_out}` s of level time: after an act boss's
  kill the theme, out since its boss music, comes back as its motif stem alone, the timed stems and section levels no
  longer applied, faded in and out at its ends; only with a `boss_track`; `LevelData.Music.MotifAfterBoss`, Level 28's
  run); the `stems` value `motif` now plays the motif as the music's `motif` cue says (silent before its `from`, the
  stems silent from the theme's first sample). The threat profile's **`strip`** (`[choir-song]`: the L3 strip marks
  each timed Choir song, `Intel.Mark.Kind.CHOIR`; only where a level asks). A section's `hold_after_boss` counts after
  the boss's `death_seconds` (Level 28's 1 s: K+5). The ambiences `europa-hatchery` and `caldera-rumble` (a layer; both left the game in step I2: one bed per setting).
  The game queues `boss-arrival` and `boss-dive` lines as `PHASE` lines.
- 2026-10-26 (user, round 58 closed): the boss script's `coil` block and `MawRules` as built in step H3 accepted with the rest of that step's rules.