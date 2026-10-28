---
title: Level 32 – Deep Core
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/ground/sentinel-tower, ../../../enemies/ground/strider, ../../../enemies/ground/sam-nest, ../../../enemies/air/talon, ../../../enemies/air/dart-drone, ../../../world/belt, ../../../player/specials, ../../../player/wingmen, ../../../player/generator, ../../act-3-red-dust/level-16-valles-canyon-run, ../../act-3-red-dust/level-19-foundry-of-tharsis]
updated: 2026-10-28
---

# Level 32 – Deep Core

## Summary

A raid through Kleopatra, an old consortium mine the Ascendancy has hollowed into its Deep Core refinery.
The act's terrain level: [Level 16](../../act-3-red-dust/level-16-valles-canyon-run/README.md#terrain)'s
walls and islands inside rock, with **two forks where the player picks the route** (Q5 a): the side the
ship is on at a fork's head chooses a channel with its own waves, one safer, one richer (more guns, a
cache behind a blast door, mining charges). [Sentinel Tower](../../../enemies/ground/sentinel-tower/README.md)s
sweep their beams all round from the side galleries, [Strider](../../../enemies/ground/strider/README.md)
pairs walk the tunnel floors and topple onto their own guns when a leg goes, and the refinery's furnace
vents close the level. No Airstrike or Orbital Lance inside the rock. Density 4, front only, ≈ 104–107
enemies a minute at medium over 186 s.

**Status: approved (Act 5 design phase, step `m9-d-levels-2`, 2026-10-27), approved in round 60.** Written from the act roster, the gap analysis's recommended answers (Q5 a branching tunnels,
Q6 b the Dart Drone, Q1 a the enemy shields, Q11 a the Ascendancy look, Q12 c the arsenal), its decided
L32 items (Strider pairs on the floors, Sentinels in side galleries, the refinery last, no Airstrike or
Lance, blast doors as ground triggers) and the wave 1 documents; every number is a first draft for part E
of M9 to measure with `levelReport`.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

> **Commander Okafor:** "Kleopatra. The consortium's old mine, now the Ascendancy's refinery, and it
> feeds the Sovereign at Ceres. The Bellwether's crew gave us the shaft charts. Go in, burn through, come
> out the ore chute."
>
> **Dr. Varga:** "Rock overhead: no bombers, no Lance. The towers sweep all the way round; shoot the
> emitter or bomb the collar. A Strider falls when it loses a leg, on whatever stands beside it."

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-32-deep-core`: the miners' shaft chart of Kleopatra (the dog-bone asteroid in
  section): the shaft, the galleries, the two forks with their channels (one marked "old drift", one
  "charge seam"), the pumping hall, the refinery and the ore chute.
- Varga's page `level-32-sentinel-sweep`: her diagram of a Sentinel from above: the emitter, the hardened
  collar, the 120° arc and the full turn's reach ring; beside it a Strider losing a leg, its fall line
  marked across a SAM Nest.

*Hangar teaser* (shop screen before L32; Rook, not voiced):

> **Rook:** "Tunnels again. I'll tuck in behind. You pick the turns."

## Threat profile

Draft; directions are counted by each wave's `from` (the intel's rule): every wave enters from the front
(the Dart streams out of wall bays near the top edge count as front).

| Field | Value |
|---|---|
| Dominant layers | `ground` (the tunnel floors: Sentinel Towers, Strider pairs, SAM Nests), `air` (Talons, Dart streams, Skitters, Needlers, motes) |
| Attack directions | front 100% · sides 0% · rear 0% |
| Density | 4 |
| Recommended traits | `anti-ground`, `forward` (the roster's), `piercing` |
| Hazards | Tunnel walls (terrain collision); Sentinel beams that sweep all round; furnace vents in the refinery |
| Boss / mid-boss | none |
| Special limits | Airstrike and Orbital Lance `IN ROCK` ([Act 5 specials](../../../player/specials/README.md#act-5-specials-draft)); Sonar Pulse `NO WATER` |
| Sensor-suite detail | none: the belt, tunnels, `ground` + `air`, front, the objective `REACH THE CHUTE`, the `IN ROCK` limits · L1: + density 4, the hazards "walls", "beam sweeps" and "furnace vents", the secondary `TOWERS DOWN 4` · L2: + Sentinel Tower (new), Strider (new), SAM Nest, Talon, Dart Drone, Skitter, Needler, Mote Swarm portraits · L3: + the two forks and which channel is the richer on the threat map |

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Tunnels inside a hollowed rock. No room to dodge wide." |
| L1 | "Two forks: one quiet, one rich. No bombers in the rock." |
| L2 | "New: towers that sweep all round, and walkers that topple." |
| L3 | "Fork routes marked. Anti-ground for the towers' collars." |

## Objective

- **Primary** `reach-end`: fly through the refinery and out of the ore chute.
- **Secondary** `TOWERS DOWN`: destroy all **four** Sentinel Towers of the common route (the galleries,
  the pumping hall's pair, the crucible), judged at the level's end. Pays **100** in Act 1 terms (655 in
  Act 5); the typical haul counts 50 %.
- The forks are a choice, not an objective: neither channel is required, the level is won on both.

## Layout

Scroll **130 px/s** in the tunnels, **120 px/s** in the refinery (eased over 2 s). Setting:
[the belt](../../../world/belt/README.md#sub-locations-per-level)'s mining station interiors and refinery,
inside the hollowed asteroid: **rock overhead** (the `IN ROCK` limits), air inside the base (the
[vacuum rules](../../../world/belt/README.md#vacuum-rules)' exception: fire burns briefly, smoke rises in the
refinery). **Light**: the tunnels lit by strings of white work lamps and the Ascendancy's red running lights
along the wall feet; the forks' channels darker; the refinery orange from the furnaces (the belt palette's
refinery light). Motion budget: the Sentinel beams and the vents are the strong elements; the pumps,
conveyors and lamp strings stay quiet.

| Section | t (s) | Scroll (px) | Speed (px/s) | Flyable width (px) | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Shaft Mouth | 0–18 | 0–2,340 | 130 | 480 → 340 | `ground`: Kleopatra's sunlit face, the shaft's lip and gantries, then the shaft walls closing in (t 4–14). | Popcorn and Talons; the first Darts out of a bay. |
| 2. The Galleries | 18–54 | 2,340–7,020 | 130 | 340 → 460 | `ground`: rock walls with lamp strings, gallery notches (the first Sentinel's at t≈24), launch bays; the tunnel widening to the fork from t≈48. | The first Sentinel alone, the first Strider pair (t≈36). |
| 3. Fork One | 54–74 | 7,020–9,620 | 130 | 2 channels of ≥ 134 | `ground`: island one; left the **old drift** (abandoned drill rigs, cold), right the **charge seam** (fresh cuts, mining charges, a Strider, SAM Nests, a blast door). | The first route choice. |
| 4. The Pumping Hall | 74–114 | 9,620–14,820 | 130 | 420 | `ground`: a wide hall, pumps and pipe runs on the floor, the Sentinel pair's notches (t≈80), the secret's blast door in the right wall (t≈100). | The Sentinel pair's lane; the Strider pair and the SAM pair (a topple to aim). |
| 5. Fork Two | 114–136 | 14,820–17,680 | 130 | 2 channels of ≥ 134 | `ground`: island two; left the **cooling ducts** (richer: four SAM Nests, charges, a blast door), right the **ore chute** (safer: conveyor belts, empty). | The second route choice. |
| 6. The Refinery | 136–186 | 17,680–23,680 | 120 | 400 → 480 | `ground`: furnace floor, vent grates, the crucible gallery (Sentinel, t≈150), the gantry (SAM trio, t≈162); the ore chute opening to space at t≈178. | Vents, Striders, the last waves; out into space. |

### The tunnels (terrain inside rock)

The terrain rules of [Level 16](../../act-3-red-dust/level-16-valles-canyon-run/README.md#terrain) carry
over unchanged (wall polylines and islands as data, the wall's foot as the collision line, contact 10 and a
28 px push with a 0.5 s cooldown, easy 6, what walls stop, the geometry rules: flyable width at least
180 px, a channel at least 130 px, a wall moving at most 0.5 px sideways per px of scroll), with these
differences:

- **Rock overhead.** The walls are cut rock with lamp strings; the tunnel's ceiling is not drawn (the
  camera looks down the shaft), but it is why the Airstrike and the Orbital Lance are greyed `IN ROCK`.
- **Galleries** are notches in the wall polyline, **100 px deep and 140 px long**, as Level 25's caves:
  a Sentinel stands in a notch's middle, a launch bay at a notch's back (the Darts leave it inward; they
  count as front waves). A tower is never placed beside a passage narrower than its reach plus 80 px
  (300 px; the Sentinel's rule): the galleries' tunnel is 340 px, the hall 420 px; **no tower in a fork's
  channel**.
- **Rook** keeps 24 px off the walls and takes Level 16's trail position below 240 px or in a split
  ([Rook in the canyon](../../act-3-red-dust/level-16-valles-canyon-run/README.md#rook-in-the-canyon)); at
  a fork he follows the player's channel.

Wall table (draft; scroll = the row at the top edge, x = the wall's foot from the field's left edge):

| t at the top edge (s) | Scroll (px) | Left x | Right x | Width (px) | Note |
|---|---|---|---|---|---|
| 0–4 | 0–520 | — | — | 480 | Open space before the shaft |
| 4–14 | 520–1,820 | 0 → 70 | 480 → 410 | 480 → 340 | The shaft closing (0.05 px/px) |
| 14–48 | 1,820–6,240 | 70 | 410 | 340 | The galleries; notches right at t≈24 (Sentinel) and t≈42 (bay), left at t≈15 (bay) |
| 48–52 | 6,240–6,760 | 70 → 10 | 410 → 470 | 340 → 460 | Widening to fork one (0.12 px/px) |
| 52.6–71 | 6,838–9,230 | 10 | 470 | 155 + 155 | **Island one** at x 165–315 (150 px), its tip blunt; it reaches the ship's row at t≈56 |
| 71–74 | 9,230–9,620 | 10 → 30 | 470 → 450 | 420 | Rejoin |
| 74–110 | 9,620–14,300 | 30 | 450 | 420 | The pumping hall; notches both walls at t≈80 (the Sentinel pair), the blast door's notch right at t≈100 |
| 110–112.6 | 14,300–14,638 | 30 → 10 | 450 → 470 | 460 | Widening to fork two |
| 112.6–132.5 | 14,638–17,225 | 10 | 470 | 155 + 155 | **Island two** at x 165–315; it reaches the ship's row at t≈116 |
| 132.5–136 | 17,225–17,680 | 10 → 40 | 470 → 440 | 400 | Rejoin; the refinery's gate |
| 136–176 | 17,680–22,480 | 40 | 440 | 400 | The refinery; the crucible gallery's notch left at t≈150 |
| 176–186 | 22,480–23,680 | 40 → 0 | 440 → 480 | 480 | The ore chute's mouth: out into space |

### The forks (route choice, Q5 a)

- **The choice.** The branch is the channel the **ship's centre** is in when the island's tip reaches the
  ship's row (t≈56 and t≈116; the same moment Rook picks his channel by). The choice is made by the ship's
  position, so a replay reproduces it (the input decides it).
- **Each branch has its own waves.** The chosen channel's air waves start 1 s after the choice (the
  branch tables below); the other channel gets none. **Ground units** in both channels scroll through as
  terrain: those of the channel not taken stay **dormant** (dark, not shootable, never picked by homing
  shots or Rook, no bounty: the SAM Nest's *dormant until* state, here *dormant unless chosen*), so the
  unchosen channel's guns never fire across the island (enemy bullets stop at rock anyway).
- **One safer, one richer.** The safer channel (fork one left, fork two right) has popcorn and Needlers
  only; the richer has Talons, Darts, SAM Nests (and a Strider in fork one), mining charges and a **blast
  door cache** worth 50 in Act 1 terms (328). The richer route pays about 10 % more over the level (see
  *Credit budget*) and costs more fire on the ship.
- **Both channels meet the floor.** Each branch's waves keep the Act 2+ floor on their own (a wave every
  2.5–3 s, no stretch over 2 s empty); the autopilot flies all four routes in the tests.
- **Different from Level 16.** Level 16's fork was visual only (its M3 b, real route choice, was rejected
  then for doubling the waves, tests and art); here the branches are short (≈ 16 s each), so the doubled
  authoring is about 13 waves, and the art is one island per fork with two dressings.

### Blast doors, mining charges, topples

- **Blast doors** (the gap analysis's decision: a ground-object trigger, no terrain change): a door pair in
  a wall notch with a **lock** (a ground target: 20 HP, the secret's 30 HP); destroyed, the doors slide
  open (presentation) and the cache's crate floats out into the tunnel. Three in the level: the two richer
  channels' caches and the hall's secret.
- **Mining charges** ([the belt's hazard](../../../world/belt/README.md#hazards--set-pieces)): shootable
  charges on the rock (1 hit) with a **64 px burst** that hurts enemies and ground units, never the ship.
  Placed next to what they should hit: 3 in each richer channel (beside the SAM Nests and the Strider's
  path), 2 by the hall's SAM pair, 4 on the refinery's gantry (by the SAM trio). They pay nothing
  themselves.
- **Topples** ([topple](../../../enemies/README.md#topple)): every Strider pair walks past something its
  fall can crush: the hall's pair past the SAM pair (t≈92; shoot the right leg and it falls onto them), the
  refinery's lone Strider (t≈166) toward the gantry's SAM trio, fork one's Strider among the charge seam's
  nests.

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). All numbers are **draft**: data
in **Act 1 terms** with a `bounty_scale` (see *Credit budget*).

- **HP** (the units' own pages, at the proposed L32 reference DPS **132**, judgement call J2): the Talon 32
  (shield 12 since round 62), the Sentinel's emitter 120 behind its hardened collar, the Strider 410 (hull 220, legs 80, pod
  30), the SAM Nest 20, the Dart 1, the Skitter, Needler and motes as built.
- **Density and pacing** (the Act 2+ rules, `PacingTest` and `ActFloorTest`): **323–332 enemies** by route
  (the safer route 332, the richer 323: waves plus 16–23 ground units) over 186 s, **≈ 104–107 a minute**
  at medium (easy ≈ 270, hard ≈ 370 by the formation lever), a wave about every 2.5–3 s, no stretch over 2 s
  with nothing on the screen (the ground units hold the quiet windows). The shots come from the Talon pairs,
  the Needlers, the Striders' fans and the SAM missiles; part E sets the fire rates with `levelReport`.
- **Quiet windows**: no air wave for 3.5 s after the first Sentinel (t=24) and the first Strider pair
  (t=36).
- **Limits**: at most 2 Sentinels sweeping and 2 Striders walking at once; at most 3 SAM Nests firing; at
  most 16 Darts. No air wave's path crosses rock (the validator's rule); popcorn in the forks flies its
  channel's middle.

Draft wave table, common route (every wave `front`; "left"/"right" is the side of the field it enters on):

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 5.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 8 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 10.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | The walls closing in |
| 13 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Leader at the tip |
| 15.5 | 1 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front | Out of a launch bay at the shaft's mouth |
| 18 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 20.5 | 2 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 28 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | After the first Sentinel's quiet window (t 24–27.5) |
| 30.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 40 | 2 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | After the first Strider pair's quiet window (t 36–39.5) |
| 42.5 | 2 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (right) | From a bay in the right gallery wall |
| 45 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 47.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 50 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 52.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The last wave across the full width before the fork |
| 74 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The tunnels rejoin |
| 76.5 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 79 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 81.5 | 4 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Down the lane between the Sentinel pair |
| 84 | 4 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (left) |  |
| 86.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 89 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 91.5 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Over the Strider pair |
| 94 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 96.5 | 4 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (right) |  |
| 101.5 | 4 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 104 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 106.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 109 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 111.5 | 4 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front | The last wave across the full width before the second fork |
| 137 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The refinery: the first vent row flares alone at t≈141 |
| 139.5 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 143 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 145.5 | 6 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 148 | 6 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (left) |  |
| 150.5 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 153 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 155.5 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 158 | 6 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (right) |  |
| 160.5 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 163 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 168 | 6 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 170.5 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 173 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 175.5 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 178 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The ore chute; a leader at the tip |
| 181 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | Out into open space: the last wave |

**Fork one** (the choice at t≈56):

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 57 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left channel) |  |
| 59.5 | 3 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front (left channel) |  |
| 62 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left channel) |  |
| 64.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front (left channel) |  |
| 67 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left channel) |  |
| 69.5 | 3 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front (left channel) |  |
| 57 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right channel) |  |
| 60 | 3 | column | [Talon](../../../enemies/air/talon/README.md) | 3 | front (right channel) |  |
| 63 | 3 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (right channel) |  |
| 66 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right channel) |  |
| 69 | 3 | column | [Talon](../../../enemies/air/talon/README.md) | 3 | front (right channel) |  |

Left channel (the old drift, safer): 6 waves, 29 enemies. Right channel (the charge seam, richer): 5 waves,
20 enemies plus a Strider and 2 SAM Nests.

**Fork two** (the choice at t≈116):

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 117 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left channel) |  |
| 120 | 5 | column | [Talon](../../../enemies/air/talon/README.md) | 3 | front (left channel) |  |
| 123 | 5 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (left channel) |  |
| 126 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left channel) |  |
| 129 | 5 | column | [Talon](../../../enemies/air/talon/README.md) | 3 | front (left channel) |  |
| 132 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left channel) |  |
| 117 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right channel) |  |
| 119.5 | 5 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front (right channel) |  |
| 122 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front (right channel) |  |
| 124.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right channel) |  |
| 127 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front (right channel) |  |
| 129.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right channel) |  |
| 132 | 5 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front (right channel) |  |

Left channel (the cooling ducts, richer): 6 waves, 25 enemies plus 4 SAM Nests. Right channel (the ore
chute, safer): 7 waves, 32 enemies.

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 2 | [Sentinel Tower](../../../enemies/ground/sentinel-tower/README.md) in the right gallery (t≈24), alone | Bounty 40 each (Act 1 terms); the first sweep with a quiet window |
| 2 | [Strider](../../../enemies/ground/strider/README.md) pair down the tunnel's floor (t≈36), 1.5 s apart | Bounty 60 each; nothing else stands here, so the first topple can only crush its partner (when it walks in the fall line) |
| 3 (right) | Strider (t≈58) walking up the charge seam, [SAM Nest](../../../enemies/ground/sam-nest/README.md) pair (t≈62), 3 mining charges | Richer channel only (dormant if not chosen) |
| 3 (right) | Blast door lock (20 HP, t≈66) | The cache: a crate worth 50 (328) |
| 4 | Sentinel pair in notches on both walls (t≈80), an 80 px lane between their reaches | Bounty 40 each |
| 4 | Strider pair across the hall (t≈90) past the SAM pair (t≈92) and 2 charges | A topple aimed at the nests crushes them |
| 4 | The secret's blast door lock (30 HP, t≈100) | See *Secrets and pickups* |
| 5 (left) | SAM Nest pairs (t≈119, t≈126), 3 mining charges | Richer channel only |
| 5 (left) | Blast door lock (20 HP, t≈128) | The cache: a crate worth 50 (328) |
| 6 | Strider pair on the furnace floor (t≈146); Sentinel in the crucible gallery (t≈150) | |
| 6 | SAM Nest trio on the gantry (t≈162) with 4 charges; a lone Strider (t≈166) walking toward it | The last topple |

## Hazards

- **Walls**: contact 10 and a 28 px push (easy 6); the forks' channels are at least 134 px wide.
- **Sentinel beams**: 220 px, 120° arcs and the full turn's ring (the tower's page); the beam passes over
  walls, so a tower in a gallery reaches the tunnel's far side.
- **Furnace vents** (the refinery, [Level 19](../../act-3-red-dust/level-19-foundry-of-tharsis/README.md#hazards)'s
  rules unchanged: a 1.2 s glow tell, a 44 px flame column for 1.0 s, `heavy` 10 to the ship and Rook,
  rows rippling 0.4 s a grate, every 3.5 s): five rows at t≈141 (alone, before any wave reaches it), 149,
  157, 165 and 173, 3–4 grates a row; cold outside the refinery.
- **Special limits** (Q12 c, [Act 5 specials](../../../player/specials/README.md#act-5-specials-draft)):
  no Airstrike or Orbital Lance (`IN ROCK`, greyed from the start); the EMP Burst, the Smart Bomb, the Decoy
  Flares and the Shield Overcharge work.

## Secrets and pickups

- **The consortium store** (the secret, the pumping hall's right wall, t≈100): a sealed blast door with
  faded consortium markings, its lock dark (no lights, unlike the richer channels' doors); a **30 HP** lock,
  destroyed, it frees a crate worth **100** in Act 1 terms (655). The Salvage scanner's glint shows it at
  any range. Its event line, Varga: "*A sealed store room. Consortium stamps. They never found it.*"
- **The branch caches** (the richer channels, t≈66 and t≈128): two lit blast doors, crates of 50 (328)
  each; not secrets (in plain sight on their route).
- **Shield cell**: dropped by the second Strider of the t≈36 pair.
- **Armour patch**: a consortium repair crate on the refinery's gate (t≈135), freed by its lock (a
  ground trigger of 10 HP).
- **Overdrive**: dropped by the crucible's Sentinel (t≈150).

## Radio chatter

Spoken radio, draft text for the **1 s rule** (text at about 12 characters a second plus a 1.5 s lead for
the length check; 0.4 s of silence; no timed line more than 1 s late); event lines drop after 6 s. Rook's
lines carry `requires: escort`. The two **branch lines** are a new trigger (`branch: left|right` at a fork's
choice), spoken only for the channel taken.

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Kleopatra. The Bellwether's charts say the shaft runs straight to the refinery." |
| t=9 | Rook | "Tunnels again. I'll tuck in behind. You pick the turns." |
| t=24.5 | Varga | "Sentinel tower. It sweeps all the way round. Shoot the emitter, or bomb the collar." |
| t=36.5 | Varga | "Striders. Take a leg and it falls, on whatever stands beside it." |
| first topple crush (event) | Rook | "It fell on its own guns. I'll take that." |
| t=48 | Okafor | "The shaft forks. Left is the old drift, quiet. Right is the charge seam: guns, and a cache." |
| branch right, fork one (event) | Okafor | "The seam it is. Watch the charges." |
| branch left, fork one (event) | Okafor | "The old drift. Quiet suits me." |
| t=75 | Varga | "The pumping hall. Two towers, and one lane between them." |
| secret found (event) | Varga | "A sealed store room. Consortium stamps. They never found it." |
| t=108 | Okafor | "Second fork. The cooling ducts on the left are guarded. The ore chute is clear." |
| t=133 | Okafor | "The refinery ahead. This is what feeds the Sovereign. Burn through it." |
| t=140.5 | Varga | "Vents in the floor. Watch the grates glow, then go." |
| t=158 | Rook | "Hot in here. I mean that literally." |
| t=175 | Okafor | "The ore chute. Out the way the rock goes, Lancer." |
| Secondary met (at the end) | Okafor | "Every tower down. That rock is blind now." |

Timeline check (text, by the rule above): Okafor's fork line (t=48, the longest) closes at about t=57.1,
before the branch line (an event after t≈56 waits its turn: it plays when the fork line ends, within its
6 s); the t=108 line closes at about t=116.2, as fork two's choice is made (the second fork has no branch
line, its channels are named in the t=108 line); the refinery line (t=133) closes at about t=140.3, before
Varga's vent warning (t=140.5), just before the first row flares. Without Rook his two lines and the topple line drop.

## Music & ambience

[Track 12](../../../audio/music/README.md#track-list), the Act 5 A theme **"Hollow Rock"** (the chosen sketch
of round 03; full length and its base/full/motif stems still to make): the radio-static intro with
Vorne's motif in cold brass over the shaft mouth, the industrial groove (the **base**) through the galleries,
the sketch's conveyor breakdown with the lone human horn at the forks (sections 3 and 5), the **full mix** in
the refinery (section 6), out into space at the chute. Ambience: a tunnel bed (proposed `belt-tunnel`: rock
creak, pumps and machinery hum through the walls, a distant drill; the act's audio step decides), the
refinery's furnace roar under section 6 (Level 19's vent and furnace sounds reused). New sounds this level
needs: the Sentinel's charge, beam and servo, the Strider's steps, leg snap and fall, the blast doors'
grind, the mining charge's blast, the SAM Nest's hatch and launch; listed in [sfx](../../../audio/sfx/README.md)
by the act's audio step (Freesound for real-world sounds only, no bubbly or popping sounds).

## Credit budget

Budget(32) = 700 × 1.07³¹ ≈ **5,702** ([economy](../../../systems/economy/README.md#per-level-budget)).
Data in **Act 1 terms** (Skitter 5, Mote 2, Talon 12, its leader 25, Dart 2, Needler 12, Sentinel 40,
Strider 60, SAM Nest 15, the secret's crate 100, a branch cache 50, the secondary 100), × the act factor
**6.5536** (1.6⁴), bounties × the scale. A **`bounty_scale` of 0.42** (Skitter 14, Mote 6, Talon 33, leader
69, Dart 6, Needler 33, Sentinel 110, Strider 165, SAM Nest 41 a kill) puts the **typical haul, the mean of
the four routes, at 5,663 (−0.7 %)**: the safer route 5,400 (−5.3 %), the richer 5,926 (+3.9 %), the mixed
ones 5,621 and 5,705 (by hand with the economy's shares: air kills 60 %, ground 80 %, the secret, the caches
and the secondary 50 %). `TypicalHaulTest` checks the mean within ±5 % and each route within ±8 %.

| Source | Safer route: perfect | Safer: typical | Richer route: perfect | Richer: typical |
|---|---|---|---|---|
| Kills, safer: Skitter 96 × 5 + Mote Swarm 96 × 2 + Talon 59 × 12 + leader 7 × 25 + Dart Drone 28 × 2 + Needler 30 × 12; richer: Skitter 91, Mote 80, Talon 68, leader 7, Dart 36, Needler 18 | 5,508 | 3,305 | 5,291 | 3,175 |
| Ground targets, safer: Sentinel Tower 4 × 40 + Strider 7 × 60 + SAM Nest 5 × 15; richer: Sentinel 4, Strider 8, SAM Nest 11 | 1,800 | 1,440 | 2,211 | 1,769 |
| Secret: the consortium store (hidden crate) | 655 | 328 | 655 | 328 |
| Branch caches (richer channels, 50 each) | — | — | 655 | 328 |
| Secondary: towers down | 655 | 328 | 655 | 328 |
| **Total** (bounty scale 0.42) | **8,618** | **5,401** | **9,467** | **5,928** |
| Budget(n) = the typical haul's target (the routes' mean 5,663, −0.7 %); perfect 1.51–1.66 × budget | | | | 5,702 |

**Balance plan** (the [Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)): the L32
visit raises the **Swarm Tail to L3** (the Airstrike is forbidden in the rock, the EMP stays fitted); the
loadout is the Ion Beam L4, the Micro-missile Pods L3 ×2 and the Swarm Tail L3: forward DPS 135 against the
reference 132 (1.02). The plan has no anti-ground weapon: the Ion Beam reaches a Sentinel's emitter through
the collar when its line crosses it, the pods' missiles aim at the emitter, the charges and the topples do
the rest; `anti-ground` (the Bomb Rack, the Hammer Mortar) is the recommended trait and pays ×2 on the
Striders.

## Difficulty notes

- **Easy**: the Sentinels' 100° arcs without the full turn, a sweep every 5.5 s; the Striders' fan every
  3.0 s, single missiles; wall contact 6; the formation lever thins the popcorn.
- **Hard**: 160° arcs and every second sweep a full turn; the Striders' fan of 5 every 2.0 s, salvos of 3;
  the SAM Nests' salvos of 3; the vents every 3.0 s.

## Concept art

None yet. The level needs (round 60's proposals, then part E's production art):

- the tunnel kit (rock walls with lamp strings and red wall-foot lights, gallery notches, launch bays, the
  two islands with their two dressings each: old drift / charge seam, cooling ducts / ore chute), the
  pumping hall, the refinery (furnace floor, vent grates, crucible, gantry), the shaft mouth on Kleopatra's
  sunlit face and the ore chute's mouth onto space;
- the blast doors (closed, lit lock, dark lock, open), the mining charges, the Sentinel Tower and the
  Strider (the unit documents), the SAM Nest;
- the two briefing images;
- the round's captures: a fork's choice from both sides, a Sentinel's full turn in the hall, a topple
  crushing the SAM pair, the vents with a Strider pair, readability crops of the walls at 1:1.

## Implementation

- [ ] Level data `data.yaml` (when built in part E): sections and speeds with eases, the terrain block (wall
      polylines, the gallery notches, the two islands), the branch waves per fork, waves, ground targets
      (with their channel), radio, budget, threat profile, briefing, backdrop, music, the `underground`
      special limits
- [ ] Tunnel terrain: Level 16's contact, push and geometry rules validated (width at least 180 px, a
      channel at least 130 px, no wave path or spawn in rock, no Sentinel beside a passage under 300 px)
- [ ] Route choice: the channel the ship's centre is in when an island's tip reaches its row; that branch's
      waves only; the other channel's ground units dormant; deterministic in replays; the `branch` radio
      trigger
- [ ] Blast doors (locks as ground triggers, the doors' opening, the crates), mining charges (64 px burst,
      never the ship), topples placed to crush
- [ ] Furnace vents in the refinery (Level 19's)
- [ ] Wave script to the Act 2+ rules on all four routes (`ActFloorTest`, `PacingTest`), the quiet windows
- [ ] `TOWERS DOWN` secondary, judged at the level end
- [ ] Airstrike and Orbital Lance greyed `IN ROCK` (the hangar intel and the briefing's threat line show it)
- [ ] Radio voiced, every timed line at most 1 s late, with Rook and without (`requires: escort`)
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel
- [ ] Briefing: both pages, each one screen, with their images
- [ ] Typical haul: the four routes' mean within ±5 % of budget(32) = 5,702, each route within ±8 %
- [ ] Music: "Hollow Rock" full length and stems by section; the tunnel bed and the furnace roar
- [ ] Easy/hard variations; the autopilot flies every route on every difficulty (with and without Rook)
- [ ] Production art and captures for the round

## Open questions

The design choices below are drafted on the recommended option, **approved in round 60**;
the alternatives:

- **Q5, branching tunnels** (gap analysis): **a** two forks, the ship's side at the fork's head picks the
  branch, each with its own waves, one safer and one richer (drafted); **b** cosmetic forks, side passages
  closed by blast doors, one route (cheap, "branching" in name only); **c** forks with the same waves in both
  (cheap, the choice is meaningless).
- **The typical haul of a route level**: drafted as the mean of the four routes, each within ±8 %; the
  alternative is the safer route's haul on the budget (the richer then pays about +10 %).
- **The unchosen channel's ground units**: drafted as dormant (no fire across the island, no bounty); the
  alternative is live but unreachable, which needs no new state but lets SAM missiles cross the island's
  top.
- **Kleopatra and Deep Core** (the names): placeholders for the story step; the act roster calls the place
  "inside a hollowed asteroid".

## Decisions

- 2026-10-27 (Act 5 design phase, step `m9-d-levels-2`): drafted from the roster row, the gap analysis
  (Q5 a, Q6 b, Q1 a, Q11 a, Q12 c and its decided L32 items) and the wave 1 documents; approved in round 60.
- 2026-10-27: Level 16's terrain rules carry over unchanged; the galleries are Level 25's notches; no
  Sentinel in a fork's channel (its 300 px passage rule); front only, as the roster says (the Darts' wall
  bays near the top count as front).
- 2026-10-27: the route is chosen by the ship's centre at the island's tip (the moment Rook picks his
  channel by); the unchosen channel's ground units are dormant; the richer route pays about 10 % more.
- 2026-10-27: the level meets the Act 2+ floor on every route (≈ 104–107 a minute); the refinery reuses
  Level 19's vents unchanged; the `TOWERS DOWN` secondary counts the four common-route Sentinels.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`, D.5): Q5 **a** (two real forks), J.11 kept (the route choice has consequences, reversing Level 16's rejected M3 b); the tunnel bed is S.19 a. Q11 **b**: gunmetal/charcoal units.
