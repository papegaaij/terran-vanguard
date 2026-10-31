---
title: Level 32 – Deep Core
design: approved
implementation: done
art: final
depends-on: [../../../enemies/ground/sentinel-tower, ../../../enemies/ground/strider, ../../../enemies/ground/sam-nest, ../../../enemies/air/talon, ../../../enemies/air/dart-drone, ../../../world/belt, ../../../player/specials, ../../../player/wingmen, ../../../player/generator, ../../act-3-red-dust/level-16-valles-canyon-run, ../../act-3-red-dust/level-19-foundry-of-tharsis]
updated: 2026-10-31
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
vents close the level. No Airstrike or Orbital Lance inside the rock. Density 4, front only, ≈ 122–125
enemies a minute at medium over 186 s (step E2).

**Status: approved (Act 5 design phase, step `m9-d-levels-2`, 2026-10-27), approved in round 60.** Written from the act roster, the gap analysis's recommended answers (Q5 a branching tunnels,
Q6 b the Dart Drone, Q1 a the enemy shields, Q11 a the Ascendancy look, Q12 c the arsenal), its decided
L32 items (Strider pairs on the floors, Sentinels in side galleries, the refinery last, no Airstrike or
Lance, blast doors as ground triggers) and the wave 1 documents; every number is a first draft for part E
of M9 to measure with `levelReport`.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image:

<!-- data: briefing -->
> **Commander Okafor:** "Kleopatra. The consortium's old mine, now the Ascendancy's refinery,
> and it feeds the Sovereign at Ceres. The Bellwether's crew gave us the shaft charts. Go in,
> burn through, come out the ore chute."
>
> **Dr. Varga:** "Rock overhead: no bombers, no Lance. The towers sweep all the way round; shoot
> the emitter or bomb the collar. A Strider falls when it loses a leg, on whatever stands beside
> it."
<!-- /data -->

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-32-deep-core`: the miners' shaft chart of Kleopatra (the dog-bone asteroid in
  section): the shaft, the galleries, the two forks with their channels (one marked "old drift", one
  "charge seam"), the pumping hall, the refinery and the ore chute.
- Varga's page `level-32-sentinel-sweep`: her diagram of a Sentinel from above: the emitter, the hardened
  collar, the 120° arc and the full turn's reach ring; beside it a Strider losing a leg, its fall line
  marked across a SAM Nest.

*Hangar teaser* (shop screen before L32; Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "Tunnels again. I'll tuck in behind. You pick the turns."
<!-- /data -->

## Threat profile

Directions are counted by each wave's `from` (the intel's rule): every wave enters from the front (the Dart
streams out of wall bays near the top edge count as front).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `ground` (the tunnel floors: Sentinel Towers, Strider pairs, SAM Nests), `air` (Talons, Dart streams, Skitters, Needlers, motes) |
| Attack directions | front 100% (the Dart streams out of wall bays near the top edge count as front) |
| Density | 4 |
| Recommended traits | `anti-ground`, `forward`, `piercing` |
| Hazards | Tunnel walls (terrain collision); Sentinel beams that sweep all round; furnace vents in the refinery |
| Boss / mid-boss | none |
| Sensor-suite detail | none: the belt, tunnels, `ground` + `air`, front, the objective `REACH THE CHUTE`, the `IN ROCK` limits · L1: + density 4, the hazards "walls", "beam sweeps" and "furnace vents", the secondary `TOWERS DOWN 4` · L2: + Sentinel Tower (new), Strider (new), SAM Nest, Talon, Dart Drone, Skitter, Needler, Mote Swarm portraits · L3: + the two forks and which channel is the richer on the threat map |
<!-- /data -->

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

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Shaft Mouth | 0–18 | 0–2,340 | 130 | clear | `ground`: Kleopatra's sunlit face, the shaft's lip and gantries, then the shaft walls closing in (t 4–14); a launch bay in the left wall (t≈14.5). | Popcorn and Talons; the first Darts out of a bay. |
| 2. The Galleries | 18–54 | 2,340–7,020 | 130 | clear | `ground`: rock walls with lamp strings, the first Sentinel's gallery notch (t≈24), a launch bay right (t≈41); the tunnel widening to the fork from t≈48. | The first Sentinel alone, the first Strider pair (t≈36). |
| 3. Fork One | 54–74 | 7,020–9,620 | 130 | clear | `ground`: island one; left the **old drift** (abandoned drill rigs, cold), right the **charge seam** (fresh cuts, mining charges, a Strider, SAM Nests, a lit blast door). | The first route choice (the ship's side at the island's tip, t≈56). |
| 4. The Pumping Hall | 74–114 | 9,620–14,820 | 130 | clear | `ground`: a wide hall, pumps and pipe runs on the floor, the Sentinel pair's shallow notches (t≈79, 81: offset, QE-1 a), the SAM pair and two charges (t≈92), the secret's sealed blast door in the right wall (t≈100). | The Sentinel pair; the Strider pair and the SAM pair (a topple to aim). |
| 5. Fork Two | 114–136 | 14,820–17,680 | 130 | clear | `ground`: island two; left the **cooling ducts** (richer: four SAM Nests, charges, a lit blast door), right the **ore chute** (safer: conveyor belts, empty); the refinery's gate (t≈134) with the repair crate. | The second route choice (t≈116). |
| 6. The Refinery | 136–186 | 17,680–23,690 | 120 | clear | `ground`: furnace floor, vent grates, the crucible gallery (Sentinel, t≈150), the gantry (SAM trio and four charges, t≈162); the ore chute opening to space at t≈178. | Vents, Striders, the last waves; out into space. |
<!-- /data -->

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

Wall table (ART-E's terrain, adopted in step E2: scroll = the row at the top edge, x = the wall's foot from the
field's left edge; the notches per QE-1 a, accepted in round 65, C.1 a):

<!-- data: terrain -->
| t at the top edge (scroll px) | Left x | Right x | Flyable width | Notes |
|---|---|---|---|---|
| 0–4 (0–520) | — | — | 480 | Open space before the shaft |
| 4 (520) | 0 | 480 | 480 | The shaft's lip: the walls begin at the field's edges; wall height 0 |
| 14 (1,820) | 70 | 410 | 340 | The galleries, 340 px; a launch bay left (t≈14.5), the Sentinel's notch right (t≈24), a bay right (t≈41) |
| 48 (6,240) | 70 | 410 | 340 | Widening to fork one |
| 52 (6,760) | 10 | 470 | 460 | |
| 52.6 (6,838) | 10 | 470 | 230 + 230 | Island one's tip (the choice at t≈56): the old drift left, the charge seam right |
| 52.7 (6,852) | 10 | 470 | 188 + 188 | |
| 53 (6,890) | 10 | 470 | 159 + 159 | |
| 53.4 (6,948) | 10 | 470 | 155 + 155 | |
| 70.5 (9,160) | 10 | 470 | 155 + 155 | |
| 70.8 (9,206) | 10 | 470 | 163 + 163 | |
| 71 (9,226) | 10 | 470 | 200 + 200 | |
| 71 (9,230) | 10 | 470 | 230 + 230 | Rejoined |
| 74 (9,620) | 30 | 450 | 420 | The pumping hall, 420 px; the Sentinel pair's notches (t≈79, 81), the secret's door (t≈100) |
| 110 (14,300) | 30 | 450 | 420 | Widening to fork two |
| 112.6 (14,638) | 10 | 470 | 230 + 230 | Island two's tip (the choice at t≈116): the cooling ducts left, the ore chute right |
| 112.7 (14,652) | 10 | 470 | 188 + 188 | |
| 113 (14,690) | 10 | 470 | 159 + 159 | |
| 113.4 (14,748) | 10 | 470 | 155 + 155 | |
| 132 (17,155) | 10 | 470 | 155 + 155 | |
| 132.3 (17,201) | 10 | 470 | 163 + 163 | |
| 132.5 (17,221) | 10 | 470 | 200 + 200 | |
| 132.5 (17,225) | 10 | 470 | 230 + 230 | Rejoined; the refinery's gate |
| 136 (17,680) | 40 | 440 | 400 | The refinery, 400 px; the crucible gallery's notch left (t≈150) |
| 175.9 (22,480) | 40 | 440 | 400 | The ore chute's mouth: out into space |
| 185.9 (23,680) | 0 | 480 | 480 | wall height 0 |

**Fork one** (`drift-rock`; x, scroll at the top edge): (240, 6,838) → (282, 6,852) → (311, 6,890) → (315, 6,948) → (315, 9,160) → (307, 9,206) → (270, 9,226) → (240, 9,230) → (210, 9,226) → (173, 9,206) → (165, 9,160) → (165, 6,948) → (169, 6,890) → (198, 6,852).

**Fork two** (`duct-rock`; x, scroll at the top edge): (240, 14,638) → (282, 14,652) → (311, 14,690) → (315, 14,748) → (315, 17,155) → (307, 17,201) → (270, 17,221) → (240, 17,225) → (210, 17,221) → (173, 17,201) → (165, 17,155) → (165, 14,748) → (169, 14,690) → (198, 14,652).

**Notches** (index, wall, t at the top edge of the middle (scroll px), length along the wall × depth): 0 left 14.5 (1,890), 140 × 56 px; 1 right 24.3 (3,160), 140 × 56 px; 2 right 41.3 (5,375), 140 × 56 px; 3 left 79.3 (10,310), 140 × 16 px; 4 right 81 (10,531), 140 × 16 px; 5 left 150.3 (19,410), 140 × 26 px.

**Contact**: 10 per touch (easy 6, hard 10), pushed 28 px along the wall's normal over 0.12 s, then a 0.5 s cooldown in which a touch only clamps.
<!-- /data -->

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
  fall can crush: the hall's pair past the SAM pair (the first enters at t=91.3 and passes the t=92 nests' row
  at y≈235; shoot its right leg and it falls onto them), the refinery's lone Strider (t=161.4) up between the
  gantry's SAM trio (t=162), fork one's Strider (t=61.3) up the charge seam between its nests (t=62) and over a
  charge. A walker rides the scroll (32 px/s up the ground against 130 px/s of scroll), so it meets a nest on the
  screen only when it enters about 0.7 s before it (step E2; the draft's t≈90/92 and t≈166/162 never met).

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). The numbers are the data's (step E2,
measured with `levelReport` per route): in **Act 1 terms** with a `bounty_scale` (see *Credit budget*).

- **HP** (the units' own pages, at the proposed L32 reference DPS **132**, judgement call J2): the Talon 32
  (shield 12 since round 62), the Sentinel's emitter 120 behind its hardened collar, the Strider 410 (hull 220, legs 80, pod
  30), the SAM Nest 20, the Dart 1, the Skitter, Needler and motes as built.
- **Density and pacing** (the Act 2+ rules, `PacingTest` and `ActFloorTest`): **373–380 enemies** by route
  at medium (waves plus 9–15 ground units) over 186 s, **≈ 124–126 a minute** (easy ≈ 99–100, hard ≈ 148–152 by
  the formation lever), a wave about every 1.25–2.5 s. The draft's 104–107 a minute left the screen empty: under
  the plan's Ion Beam L4 in the 155–420 px tunnels the fodder dies within a second (1.3–1.5 enemies on the screen,
  ten stretches over 2 s empty); step E2 added offbeat Talon pairs and lines (as Levels 29–31) and turned four
  Skitter streams and two Mote Swarms into Talon lines and wings; step E3 (its tower play flying the routes) added
  firing pairs after fork one's choice (t=58, both channels) and in the charge seam (t=65): **2.61–2.68 on the
  screen** (at least 2.5), 84–92 shots a minute, at most 1 stretch over 2 s on a route at medium and hard, the
  small units 0.92–0.98 × Level 10's time on the screen (`levelReport`, every route). The shots come from the Talon pairs, the Needlers, the Striders' fans
  and the SAM missiles.
- **Quiet windows**: no air wave for 3.5 s after the first Sentinel (t=24) and the first Strider pair
  (t=36).
- **Limits**: at most 2 Sentinels sweeping and 2 Striders walking at once; at most 3 SAM Nests firing; at
  most 16 Darts. No air wave's path crosses rock (the validator's rule); popcorn in the forks flies its
  channel's middle.

Wave table (step E2: the draft's common waves with offbeat Talon pairs and lines, four draft Skitter streams and
two Mote Swarms turned into Talon lines and wings, as every Act 5 level so far needed; the forks' branch waves, each
flown only on its chosen side; the Striders entering just before the nests they pass):

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (alternating edges) | |
| 5.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 8 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 9.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 10.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | The walls closing in |
| 12 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 13 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Leader at the tip |
| 15.5 | 1 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (left) | Out of the launch bay at the shaft's mouth (left wall) |
| 17 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step E2: a firing Talon line on the offbeat (the beam erases the fodder within a second) |
| 18 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step E2: a Talon line in place of the draft's Skitter stream (the beam erases the fodder within a second) |
| 20.5 | 2 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 22.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 28 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | After the first Sentinel's quiet window (t 24–27.5) |
| 30.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 33.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step E2: a firing Talon line on the offbeat (the beam erases the fodder within a second) |
| 36 | 2 | convoy | [Strider](../../../enemies/ground/strider/README.md) | 2 | front | **New.** The first Strider pair; nothing else stands here, so a topple can only crush its partner |
| 40 | 2 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | After the first Strider pair's quiet window (t 36–39.5) |
| 42.5 | 2 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (right) | From the bay in the right gallery wall |
| 43.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 45 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 47.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 49 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 50 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (alternating edges) | |
| 52.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The last wave across the full width before the fork |
| 57 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) (fork 1, left channel) | |
| 57 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) (fork 1, right channel) | |
| 58 | 3 | column | [Needler](../../../enemies/air/needler/README.md) | 2 | front (fork 1, left channel) | Step E3: a Needler pair after the choice (the pacing and floor tests per route: the Skitters die within a second) |
| 58 | 3 | column | [Talon](../../../enemies/air/talon/README.md) | 2 | front (fork 1, right channel) | Step E3: a firing Talon pair after the choice, as the drift's Needler pair |
| 59.5 | 3 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front (fork 1, left channel) | |
| 60 | 3 | column | [Talon](../../../enemies/air/talon/README.md) | 3 | front (fork 1, right channel) | |
| 61.3 | 3 | single | [Strider](../../../enemies/ground/strider/README.md) | 1 | front (fork 1, right channel) | Walks up the charge seam past its SAM pair |
| 62 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) (fork 1, left channel) | |
| 63 | 3 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (right) (fork 1, right channel) | |
| 64.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front (fork 1, left channel) | |
| 65 | 3 | column | [Talon](../../../enemies/air/talon/README.md) | 2 | front (fork 1, right channel) | Step E3: a firing Talon pair on the seam's offbeat (the pacing and floor tests per route: the seam's 66-69 s stretch emptied under the beam) |
| 66 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) (fork 1, right channel) | |
| 67 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) (fork 1, left channel) | |
| 69 | 3 | column | [Talon](../../../enemies/air/talon/README.md) | 3 | front (fork 1, right channel) | |
| 69.5 | 3 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front (fork 1, left channel) | |
| 72 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step E2: a firing Talon line on the offbeat (the beam erases the fodder within a second) |
| 74 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The tunnels rejoin |
| 76.5 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 78 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 79 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step E2: a Talon line in place of the draft's Skitter stream (the beam erases the fodder within a second) |
| 81.5 | 4 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Between the Sentinel pair's reaches |
| 83.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 84 | 4 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (left) | |
| 86.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 88 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 89 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (alternating edges) | |
| 91.3 | 4 | convoy | [Strider](../../../enemies/ground/strider/README.md) | 2 | front | The hall's pair, 1.5 s apart; a right-leg topple of the first falls onto the SAM pair |
| 91.5 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Over the Strider pair |
| 94 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 96.5 | 4 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (right) | |
| 99 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step E2: a firing Talon line on the offbeat (the beam erases the fodder within a second) |
| 101.5 | 4 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 103 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 104 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Step E2: a Talon V-wing in place of the draft's Mote Swarm (the beam erases the fodder within a second) |
| 106.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 108 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 109 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step E2: a Talon line in place of the draft's Skitter stream (the beam erases the fodder within a second) |
| 111.5 | 4 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (alternating edges) | The last wave across the full width before the second fork |
| 113 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 117 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) (fork 2, left channel) | |
| 117 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) (fork 2, right channel) | |
| 119.5 | 5 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front (fork 2, right channel) | |
| 120 | 5 | column | [Talon](../../../enemies/air/talon/README.md) | 3 | front (fork 2, left channel) | |
| 122 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front (fork 2, right channel) | |
| 123 | 5 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (left) (fork 2, left channel) | |
| 124.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) (fork 2, right channel) | |
| 126 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) (fork 2, left channel) | |
| 127 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front (fork 2, right channel) | |
| 129 | 5 | column | [Talon](../../../enemies/air/talon/README.md) | 3 | front (fork 2, left channel) | |
| 129.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) (fork 2, right channel) | |
| 132 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) (fork 2, left channel) | |
| 132 | 5 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front (fork 2, right channel) | |
| 133.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step E2: a firing Talon line on the offbeat (the beam erases the fodder within a second) |
| 135.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 137 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The refinery: the first vent row flares alone at t≈141 |
| 139.5 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 141.5 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 143 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (alternating edges) | |
| 145.5 | 6 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 146 | 6 | convoy | [Strider](../../../enemies/ground/strider/README.md) | 2 | front | The furnace floor's pair |
| 147 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 148 | 6 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (left) | |
| 150.5 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Step E2: a Talon V-wing in place of the draft's Mote Swarm (the beam erases the fodder within a second) |
| 152 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 153 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 155.5 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step E2: a Talon line in place of the draft's Skitter stream (the beam erases the fodder within a second) |
| 157 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 158 | 6 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (right) | |
| 160.5 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 161.4 | 6 | single | [Strider](../../../enemies/ground/strider/README.md) | 1 | front | The lone Strider through the gantry's SAM trio |
| 163 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 166 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step E2: a firing Talon line on the offbeat (the beam erases the fodder within a second) |
| 168 | 6 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 170.5 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 172 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 173 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (alternating edges) | |
| 175.5 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 176 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (alternating edges) | Out into open space: the last wave (step FIX6: t=176, not 181: the calm before the end) |
| 176.5 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The ore chute; a leader at the tip (step FIX6: t=176.5, not 178: the calm before the end) |
| 177 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step E2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |

Totals: Skitter 97 · Talon 168 · Mote Swarm 80 · Dart Drone 36 · Needler 32 · Strider 8 (every fork's branches together; a route flies one channel of each fork).
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 2 | [Sentinel Tower](../../../enemies/ground/sentinel-tower/README.md) in the right gallery (t≈24), alone | Bounty 40 (Act 1 terms); the first sweep with a quiet window |
| 4 | Sentinel Tower pair in the hall's notches (t≈79 left, t≈81 right), their rows offset so they never sweep the same rows at once | Bounty 40 each |
| 4 | [SAM Nest](../../../enemies/ground/sam-nest/README.md) pair on the hall floor (t≈92), by the Strider pair's path | Bounty 15 each; a topple aimed at them crushes them |
| 4 | Mining charges ×2 by the hall's SAM pair (t≈92) | One hit: a 64 px burst of 80 to every enemy in it (never the ship); pays nothing itself |
| 4 | The consortium store's sealed blast door, its lock dark (30 HP, t≈100) | Destroyed, the doors slide open and the hidden crate floats out (the secret) |
| 3 | SAM Nest pair in the charge seam (t≈62; fork one right) | Dormant unless the charge seam is chosen |
| 3 | Mining charges ×3 in the charge seam (fork one right), by the SAM pair and the Strider's path | As above; dormant unless chosen |
| 3 | The charge seam's lit blast door, its lock (20 HP, t≈66; fork one right) | Destroyed, the doors open on a cache: a crate worth 50 (Act 1 terms) |
| 5 | SAM Nest pairs in the cooling ducts (t≈119, 126; fork two left) | Dormant unless the cooling ducts are chosen |
| 5 | Mining charges ×3 in the cooling ducts (fork two left), by the SAM pairs | As above; dormant unless chosen |
| 5 | The cooling ducts' lit blast door, its lock (20 HP, t≈128; fork two left) | Destroyed, the doors open on a cache: a crate worth 50 (Act 1 terms) |
| 5 | A consortium repair crate on the refinery's gate, its lock (10 HP, t≈135) | Destroyed, it releases an armour patch |
| 6 | Sentinel Tower in the crucible gallery (t≈150) | Bounty 40 |
| 6 | SAM Nest trio on the gantry (t≈162), the lone Strider walking through them | Bounty 15 each; the last topple |
| 6 | Mining charges ×4 on the gantry by the SAM trio | As above |
<!-- /data -->

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
- **Armour patch**: a consortium repair crate on the refinery's gate (t≈134.6), a destructible of 10 HP that
  releases it.
- **Overdrive**: dropped by the last Needler of the t=145.5 column, before the crucible (step E2: a ground unit
  drops a pickup only as an objective group's, so not the crucible's Sentinel as drafted).

## Radio chatter

Spoken radio, draft text for the **1 s rule** (text at about 12 characters a second plus a 1.5 s lead for
the length check; 0.4 s of silence; no timed line more than 1 s late); event lines drop after 6 s. Rook's
lines carry `requires: escort`. The two **branch lines** are a new trigger (`branch: left|right` at a fork's
choice), spoken only for the channel taken.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Kleopatra. The shaft runs straight to the refinery." |
| t=9 | Rook | "Tunnels again. I'll tuck in behind. You pick the turns." |
| t=24.5 | Varga | "Sentinel tower. It sweeps all round. Hit the emitter." |
| t=36.5 | Varga | "Striders. Shoot a leg and it falls on its neighbours." |
| First topple crush (`topple-crush`) | Rook | "It fell on its own guns. I'll take that." |
| t=48 | Okafor | "Fork ahead. Left is quiet. Right has guns and a cache." |
| Fork one, the right channel taken (`branch`) | Okafor | "The seam it is. Watch the charges." |
| Fork one, the left channel taken (`branch`) | Okafor | "The old drift. Quiet suits me." |
| t=75 | Varga | "The pumping hall. Two towers, one each side." |
| t=108 | Okafor | "Second fork. The ducts left are guarded; the chute is clear." |
| t=133 | Okafor | "The refinery. It feeds the Sovereign. Burn through it." |
| t=140.5 | Varga | "Vents in the floor. Watch the grates glow, then go." |
| t=162 | Rook | "Hot in here. I mean that literally." |
| t=175 | Okafor | "The ore chute. Out the way the rock goes, Lancer." |
| Secondary met (the fourth tower down) | Okafor | "Every tower down. That rock is blind now." |
<!-- /data -->

Timeline check (step E2: every line trimmed to one radio page, three lines of 22 characters, 5 s on the panel;
voiced ≈ 2.5–5.7 s): Okafor's fork line (t=48) closes at t=53, before fork one's choice (t≈56–56.75), so the
branch line plays at the choice on both sides; the t=108 line closes at t=113, before fork two's choice
(t≈116; the second fork has no branch line, its channels are named in the t=108 line); the refinery line
(t=133) closes at t=138, before Varga's vent warning (t=140.5). The first topple crush comes at the earliest in
the charge seam (t≈63) or the hall (t≈94) — the t=36 pair walks in line, so neither falls on the other —, where
no timed line follows within 5 s; the secret's line (t≈100–104) and the secondary's (the fourth tower, t≈150–155)
fall in free stretches too. Without Rook his two lines and the topple line drop. Step E4's
`RadioTimelineLevel32Test` flies it per route.

## Music & ambience

[Track 12](../../../audio/music/README.md#track-list), the Act 5 A theme **"Hollow Rock"** (the chosen sketch
of round 03; full length with its base, full and motif stems in `assets/music` since round 62): the radio-static intro with
Vorne's motif in cold brass over the shaft mouth, the industrial groove (the **base**) through the galleries,
the sketch's conveyor breakdown with the lone human horn at the forks (a fourth stem `conveyor`, round 65's M.1 ok:
alone, Vorne's motif still on top, for 16 s from each fork's choice, t 56–72 and 116–132, switched in and out over
2 s), the **full mix** in the refinery (section 6), out into space at the chute; Vorne's motif stem on top from t=18 (on). Ambience: the
tunnel bed `belt-tunnel` (round 60 S.19 a: rock creak, pumps and machinery hum through the walls, a distant
drill), the refinery's furnace roar under section 6 (Level 19's `mars-foundry` bed as an ambience layer, t
136–180, faded out at the chute's mouth; the vents' tell and burst sounds reused). New sounds this level
needs: the Sentinel's charge, beam and servo, the Strider's steps, leg snap and fall, the blast doors'
grind, the mining charge's blast, the SAM Nest's hatch and launch; listed in [sfx](../../../audio/sfx/README.md)
by the act's audio step (Freesound for real-world sounds only, no bubbly or popping sounds).

## Credit budget

Budget(32) = 700 × 1.07³¹ ≈ **5,702** ([economy](../../../systems/economy/README.md#per-level-budget)).
Data in **Act 1 terms** (Skitter 5, Mote 2, Talon 12, its leader 25, Dart 2, Needler 12, Sentinel 40,
Strider 60 (hull 40, legs 6, pod 8), SAM Nest 15, the secret's crate 100, a branch cache 50, the secondary
100), × the act factor **6.5536** (1.6⁴), bounties × the scale. A route level's typical haul is the **mean of
its four routes** (a side per fork: L or R), each within ±8 % (step E2: the table has a row per route and the
mean as its total; `TypicalHaulTest` checks the mean and each route). Step E2 fitted a **`bounty_scale` of 0.34**
(the draft's 0.42 with the offbeat Talons added; `levelReport` reports each route): the safer route (LR: the old
drift, the ore chute) and the richer (RL: the charge seam, the cooling ducts) about 9 % apart.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Route LL | 9,578 | 5,743 |
| Route LR | 9,215 | 5,532 |
| Route RL | 10,088 | 6,056 |
| Route RR | 9,725 | 5,845 |
| **Total** (the routes' mean, bounty scale 0.34) | **9,652** | **5,794** |
| Budget(n) = the typical haul's target; the mean +2 % (by route: LL +1 % LR -3 % RL +6 % RR +3 %), perfect 1.69 × budget | | 5,702 |
<!-- /data -->

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
- **Medium and hard**: each Sentinel's first sweep is the **full turn** (QE-3 a, `full_turn_first`: a tower is
  about 4.7 s on the field and sweeps once); then every third sweep, hard every second.
- **Hard**: 160° arcs and every second sweep a full turn; the Striders' fan of 5 every 2.0 s, salvos of 3;
  the SAM Nests' salvos of 3; the vents every 3.0 s.

## Concept art

Part E's production art (step ART-E, concept round 65) is below; the level needs (round 60's proposals, then part E's
production art):

- the tunnel kit (rock walls with lamp strings and red wall-foot lights, gallery notches, launch bays, the
  two islands with their two dressings each: old drift / charge seam, cooling ducts / ore chute), the
  pumping hall, the refinery (furnace floor, vent grates, crucible, gantry), the shaft mouth on Kleopatra's
  sunlit face and the ore chute's mouth onto space;
- the blast doors (closed, lit lock, dark lock, open), the mining charges, the Sentinel Tower and the
  Strider (the unit documents), the SAM Nest;
- the two briefing images;
- the round's captures: a fork's choice from both sides, a Sentinel's full turn in the hall, a topple
  crushing the SAM pair, the vents with a Strider pair, readability crops of the walls at 1:1.

Production art, M9 part E (concept round 65, step ART-E): the tunnel kit backdrop (`tools/art/backdrop_l32.py`, its
places for step E2 in [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml): the terrain per QE-1 a, the
forks, the notches, the targets it assumes) and the blast doors, mining charge and repair crate
(`tools/art/belt_units_e.py`). The Sentinel Tower and the Strider are in the
[ground units' concept art](../../../enemies/ground/README.md#concept-art); the two briefing images in the
[briefing](../../../ui/briefing/README.md#concept-art). Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r65-a.png](concept/backdrop-final-r65-a.png) | Production backdrop (`tools/art/backdrop_l32.py`): fourteen screens at half size with stand-ins (the shaft's lip, the shaft closing, the gallery Sentinel's notch, a launch bay, fork one's head and channels, the hall's Sentinel pair, the consortium store, fork two's head and channels, the refinery's gate, the crucible gallery, the gantry, the ore chute's mouth); 1x crops of both forks' mouths (QE-2 a) and a wall | chosen (round 65, F.1 a and F.2 ok: accepted as final) |
| [concept/backdrop-final-r65-a.gif](concept/backdrop-final-r65-a.gif) | t 50-62 at 5 fps: the tunnel widening to fork one, the island's tip and the two channels' mouths, the charge seam's SAM pair and charges | chosen (round 65, F.1 a and F.2 ok: accepted as final) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block, the sections' tiles, the proposed `terrain:` block (walls, the two fork islands, the forks, the notches per QE-1 a) and the ground targets the art is laid out for (towers, doors, charges, nests) for step E2 (not data) | proposed |
| [concept/ground-objects-final-r65-a.png](concept/ground-objects-final-r65-a.png) | The lit blast doors (right, left; the lock's lamp, the opening), the consortium store's sealed door and its opening, the mining charge and its 64 px burst, the repair crate (`tools/art/belt_units_e.py`; sheet) | chosen (round 65, F.5 ok: accepted as final) |
| [concept/ground-objects-final-r65-a.gif](concept/ground-objects-final-r65-a.gif) | The doors opening as their locks go, a charge's burst taking a Talon and a SAM pair, the crate opening | chosen (round 65, F.5 ok: accepted as final) |
| [concept/fork-capture-r65-a.png](concept/fork-capture-r65-a.png) | Game capture (step E5; [recipe](concept/prompts.md#level-32-capture-r65-a)): fork one and fork two seen from the island's tip, both mouths on the screen (QE-2 a: the richer mouth's red posts and lit bulkhead, the safer one's work lamps and stripes) | chosen (round 65, accepted with the level, F.1–F.5) |
| [concept/walls-capture-r65-a.png](concept/walls-capture-r65-a.png) | Game capture: 1:1 wall strips (the launch bays, the gallery tower's notch, the seam, the hall's shallow notch, the refinery and the crucible's notch) | chosen (round 65, accepted with the level, F.1–F.5) |
| [concept/blast-door-capture-r65-a.png](concept/blast-door-capture-r65-a.png) | Game capture: the charge seam's lit door shut and opening on its cache (2x), the hall's sealed store opened, the secret's line | chosen (round 65, accepted with the level, F.1–F.5) |
| [concept/charges-capture-r65-a.png](concept/charges-capture-r65-a.png) | Game capture: the gantry's mining charges bursting by the SAM trio as the lone Strider walks through | chosen (round 65, accepted with the level, F.1–F.5) |
| [concept/vents-capture-r65-a.png](concept/vents-capture-r65-a.png) | Game capture: the furnace vents with the furnace floor's Strider pair and the crucible tower's full turn | chosen (round 65, accepted with the level, F.1–F.5) |
| [concept/towers-down-capture-r65-a.png](concept/towers-down-capture-r65-a.png) | Game capture: the crucible tower's emitter under the beam, `TOWERS DOWN` met (the HUD tag, Okafor's line) | chosen (round 65, accepted with the level, F.1–F.5) |
| [concept/level-32-capture-r65-a.mp4](concept/level-32-capture-r65-a.mp4) | Game capture with the game's sound: the gallery tower's full turn, fork one's approach and the seam taken | chosen (round 65, accepted with the level, F.1–F.5) |

## Implementation

- [x] Level data `data.yaml` (when built in part E): sections and speeds with eases, the terrain block (wall
      polylines, the gallery notches, the two islands), the branch waves per fork, waves, ground targets
      (with their channel), radio, budget, threat profile, briefing, backdrop, music, the `underground`
      special limits
- [x] Tunnel terrain: Level 16's contact, push and geometry rules validated (width at least 180 px, a
      channel at least 130 px, no wave path or spawn in rock, no Sentinel beside a passage under 300 px)
      (`DeepCoreChecks`, `TerrainClearanceTest`; steps E1–E3)
- [x] Route choice: the channel the ship's centre is in when an island's tip reaches its row; that branch's
      waves only; the other channel's ground units dormant; deterministic in replays; the `branch` radio
      trigger
- [x] Blast doors (locks as ground triggers, the doors' opening, the crates), mining charges (64 px burst,
      never the ship), topples placed to crush (steps E1–E4; captured in step E5: a door opening on its cache, the
      gantry's charges, the hall Strider's crush of the SAM pair)
- [x] Furnace vents in the refinery (Level 19's)
- [x] Wave script to the Act 2+ rules on all four routes (`ActFloorTest`, `PacingTest`), the quiet windows
- [x] `TOWERS DOWN` secondary, judged at the level end
- [x] Airstrike and Orbital Lance greyed `IN ROCK` (the hangar intel and the briefing's threat line show it)
- [x] Radio voiced, every timed line at most 1 s late, with Rook and without (`requires: escort`)
      (`RadioTimelineLevel32Test`; Rook's t=162, step E3)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel
- [x] Briefing: both pages, each one screen, with their images
- [x] Typical haul: the four routes' mean within ±5 % of budget(32) = 5,702, each route within ±8 %
- [x] Music: "Hollow Rock" full length and stems by section; the tunnel bed and the furnace roar
- [x] Easy/hard variations; the autopilot flies every route on every difficulty (with and without Rook)
- [x] Production art and captures for the round (ART-E; step E5's game captures, round 65)

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
- 2026-10-29 (step E2, accepted by the user in round 65): the data built on ART-E's proposal (its terrain, forks,
  six notches per QE-1 a and targets adopted unchanged). **Pacing**: the draft's waves left 1.3–1.5 enemies on the
  screen and ten stretches over 2 s empty under the plan's Ion Beam L4; 24 offbeat Talon pairs and lines added, four
  Skitter streams and two Mote Swarms (t=18, 79, 104, 109, 150.5, 155.5) turned into Talon lines and wings: 2.71–2.73
  on the screen on every route, ≈ 122–125 a minute (the draft's 104–107). **`bounty_scale` 0.34** (the draft's
  0.42): the routes' mean typical haul 5,751 (+0.9 %), each route −3 … +5 %. **Striders** enter 0.7 s before the
  nests they pass (fork one's at 61.3, the hall's pair at 91.3/92.8, the refinery's lone one at 161.4, not 166): a
  walker rides the scroll and meets a nest on the screen only so. **Overdrive** from the last Needler of the t=145.5
  column (a ground unit drops only as an objective group's). The radio lines trimmed to one page each (Okafor t=1,
  t=48, t=108, t=133; Varga t=24.5, t=36.5, t=75: "two towers, one each side", the hall pair offset by QE-1 a). The
  armour patch's crate a plain 10 HP destructible on the gate. `TrenchChecks`' ledge rule scoped to a trench under
  water (Level 25), so the tunnels' floor nests stand in the open.
- 2026-10-29 (step E2, found): the gallery tower (x 426, the 340 px tunnel's wall at 410) and the crucible tower
  (x 54, the refinery's wall at 40) stand in their notches beyond the tunnel's wall line: a front gun reaches the
  emitter only from inside the notch (the hall pair's 16 px notches leave theirs in reach). The autopilot met
  `TOWERS DOWN` on easy only (2 of 3); the typical haul counts it at 50 % as every secondary. For step E3's tower play
  or a round-65 call (shallower notches, the towers on the wall's foot).
- 2026-10-29 (step E3, accepted by the user in round 65): the **tower play** (the autopilot lines up under an
  emitter, low and holding its line while its shield is half full, waits under the next tower's x 1 s before it
  enters, steps into the gallery's notch under its tower and follows it down): `TOWERS DOWN` met on every route and
  difficulty, medium in every run, easy in 10 of 12, hard in 5 of 12 (`Level32Test`, 3 seeds a route, with Rook;
  without him hard 3 of 8): the hall pair's second tower and the crucible's are the misses on hard. The
  geometry needs no change: the crucible's emitter lies in the beam's reach from the tunnel (8 px), the gallery's
  from its notch. **Waves**: firing pairs after fork one's choice (t=58: Needlers in the old drift, Talons in the
  seam) and a Talon pair in the seam (t=65), for the pacing and floor tests on every route (the beam emptied those
  stretches); the routes' mean typical haul 5,800 (+1.7 %), each route −3 … +6 %. Rook's "Hot in here" moved from
  t=158 to **t=162**: the secondary's line after the crucible tower (t≈151–155.5) holds the radio about 6.8 s
  (`RadioTimelineLevel32Test`).
- 2026-10-29 (step E3, found; QE-3 for round 65): every Sentinel sweeps **one arc** in this level and no full turn:
  each tower is about 4.7 s on the field (5.1 s in the refinery), its first telegraph 1.0 s after it comes on and
  the next 4.5 s later (hard 3.6 s, the full turn's telegraph starting as it leaves). A full turn on the field needs
  e.g. the full turn as a tower's first sweep in this level, or a shorter first delay and cadence
  (`Level32Test.PENDING_FULL_TURN`).
- 2026-10-29 (step E3b, QE-3 **a**, decided by the user in round 65, C.2 a): each tower's **first sweep is the full
  turn** on medium and hard (`full_turn_first: true` on the three tower targets; easy keeps its arcs, the stat block
  unchanged for later levels). Left alone (`Level32Test`, route LL) each tower turns once, its 1.0 s ring telegraph
  with the start ray on the ship's bearing, the beam live at least 91 % of its 3 s before the tower leaves (the
  ship 211 px from a tower at the nearest telegraph); easy one arc each. Re-measured per route (`levelReport`,
  medium): floor 2.68 / 2.59 / 2.63 / 2.55 (LL/LR/RL/RR, unchanged), the typical haul +0.7 / −3.0 / +6.2 / +2.5 %
  (mean 5,794, +1.6 %), the autopilot wins every run with no retry at medium; `TOWERS DOWN` with Rook as before
  (medium 12 of 12, easy 10, hard 5). The credit table now rounds each of a Strider's parts as the game pays them
  (`tools/sync_tables.py`): the perfect run 9,652, as `TypicalHaulTest`.
- 2026-10-29 (step E1, accepted by the user in round 65): a branch's waves and targets come at or after its fork's
  **latest choice** (the island's tip at the bottom edge: t≈56.75 and 116.75), so the richer channel's lit cache door is
  never on the screen before the choice (QE-2's reading is the mouths'); a Sentinel's arcs alternate their direction;
  a **toppling Strider takes no more damage** (the stagger and fall play out, the crush pays it); a cache's crate pays at
  the secrets' rate but counts **no secret** (the hall's store is the level's one secret).
- 2026-10-29 (step E4, accepted by the user in round 65): the hangar intel's wave strip marks each fork (a white
  tick, a gold arm toward the **richer** side, richer = more credits in the branch's bounties and caches); a Strider
  that dies by its topple explodes as it lands; the intel names the Airstrike's and the Lance's `IN ROCK` limit from
  sensor L1.
- 2026-10-29 (step E5, captures for round 65): on medium each tower turns once on the field (QE-3 a) and **no arc is
  seen** (the arcs only on easy); with the plan's wing pods the Striders fell to their screen-left in 5 of 6 topples
  (the pods' missiles take that leg first), so the hall's crush of the SAM pair was caught only with the Ion Beam alone
  (no pods, no Rook) on the walker's screen-right side; the ship sits at x≥57 by the refinery's wall (pushed off at 49), where the beam still
  reaches the crucible's emitter; a cache door opens as its lock comes on the field under the beam.
- 2026-10-29 (user, [round 65](../../../concept-rounds/round-65/README.md), closed): the part's production art and calls accepted: **F.1 a** (the richer mouth's red running lights and lit bulkhead, the safer one's white work lamps), **F.2** the backdrop, **F.5** the doors, charges and crate ok (`art: final`); **C.1 a** (every tower emitter and door lock at least 40 px inside the field, the shallow notches), **C.2 a** (a tower's first sweep the full turn on medium and hard, easy keeps its arcs), **C.3** the offbeat waves and `bounty_scale` 0.34, **C.5** the route choice's rules, **C.6** the trimmed radio and Rook's t=162 accepted; **M.1** ok (track 12's conveyor stem at the forks, imported in step G4; until then the base stem plays there); the sounds all a but **S.3 b** (`sentinel-servo`), **S.7 b** (`strider-cannon`) and **S.11 b** (`charge-blast`), switched in step G4 ([sfx](../../../audio/sfx/README.md)). No notes from the user.
- 2026-10-30 (step FIX6, the user's playthrough 2026-10-30: "a hard line in the backdrop at the very end of the
  level"): the seam between `chute-end` and `chute-space` (the ore chute's mouth onto space, then belt space to the
  outro's end): the two pieces drew their space from two fields (seeds 3411 and 3415, the dust band's phase restarting
  in each) and posterized with two palettes (40 and 24 colours), so the dust band broke along a straight row.
  `backdrop_l32.py`'s `chute_pieces` now draws one space field over both pieces and posterizes them together (40
  colours), then splits them; re-run (only the two pieces and the review sheet changed) and `--check` 0 problems.
- 2026-10-30 (step FIX6, the user's playthrough 2026-10-30; the campaign's new end calm rule, at least 4 s from the
  last enemy gone to the end from Act 5 on, `PacingTest`): the level's end stays; the last waves earlier: the ore
  chute's V-wing 178 → 176.5 and the last Skitter stream 181 → 176; the end calm (easy/medium/hard, the autopilot
  with the plan's fit, route LL; the other routes alike, the least 4.8 s on hard LR) 1.9/1.3/0.9 s → 6.9/6.3/5.8 s.
  The counts, the haul and the autopilot's wins are unchanged.
- 2026-10-30, the user: dither audit, ≥ 25 % smoothed (step DITH8): the galleries' mouths, `crucible-glow`, `ice-wall-glow` and the other `-glow` pieces; `mining-charge-blast` in smooth steps, not the 4×4 Bayer checker ([audit](../../../art-direction/production/README.md#dither-audit-2026-10-30)).
