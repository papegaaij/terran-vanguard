---
title: Level 30 – Minefield
design: approved
implementation: done
art: final
depends-on: [../../../enemies/space/minelayer, ../../../enemies/air/gilded-gunship, ../../../enemies/air/rail-serpent, ../../../enemies/air/dart-drone, ../../../enemies/README.md, ../../../player/weapons/swarm-tail, ../../../player/wingmen, ../../../world/belt]
updated: 2026-10-30
---

# Level 30 – Minefield

## Summary

The shipping lane out of Ceres, the loyalists' only road to the outer stations, and the act's one
**space-only** level: no ground layer (the Bomb Rack reads `NO GROUND`), open space between the lane's
beacon buoys, drifting rocks that block shots and break. Ascendancy
[Minelayers](../../../enemies/space/minelayer/README.md) come up **from behind** and seed lines of
proximity mines across the lane with one gap; [Gilded Gunships](../../../enemies/air/gilded-gunship/README.md),
plated in front, launch streams of [Dart Drones](../../../enemies/air/dart-drone/README.md); and
[Rail Serpent](../../../enemies/air/rail-serpent/README.md) drone trains cut in from the sides and the rear.
A convoy of three Gunships carrying the next load of mines is the secondary. **Rook is back in the escort
slot** for his first level since Level 26. 178 s, ≈ 101 enemies a minute at medium.

**Status: approved in round 60 (drafted Act 5 design phase, 2026-10-27, step `m9-d-levels-1`).** Written from the act roster, the gap analysis's recommended answers (Q3 a, Q6 b,
Q12 c; J2, J3), the L30 build gap check's questions (C1 a: mines in clusters of 4, C2 a: a 40 px blast with
the ring; J4: the Dart Drone first flown here) and the wave 1 documents (the units above,
[mine lines](../../../enemies/README.md#mine-lines), [rock riding](../../../enemies/README.md#rock-riding), the
[belt](../../../world/belt/README.md), the [Swarm Tail](../../../player/weapons/swarm-tail/README.md), the
[wingmen](../../../player/wingmen/README.md#rooks-rescue-and-return-l29l30)). Built in M9 part C (step C2): the
level script is [data.yaml](data.yaml); the marked tables below are rendered from it and measured with `levelReport`.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft):

<!-- data: briefing -->
> **Commander Okafor:** "Ceres is lost, but not everyone in it chose Vorne. The loyalists are
> leaving on a mining tug, and the only lane out is mined. Clear it before the tug sets out, and
> stop the Gunship convoy bringing more mines. Rook flies with you again. Welcome back, Aegis
> Two."
>
> **Dr. Varga:** "Minelayers come up from behind and lay a line across your path with one gap.
> Find the gap, or shoot a mine: its cluster goes with it. The Gunships are plated in front;
> their engines are not. And there is no ground out there, Lancer: the bombs stay idle."
<!-- /data -->

*Images* (production art in part C, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-30-shipping-lane`: a CDF chart of the lane from Ceres to the outer stations, the
  beacon buoys as a dotted line, the rock drift as grey arrows, the convoy's track in red, the tug's
  departure point marked.
- Varga's page `level-30-minelayer`: her scan of a Minelayer over a laid line seen from above, the clusters of
  four ringed, the one gap marked, an inset of a mine blinking.

*Hangar teaser* (shop screen before L30; Rook is back and speaks, not voiced):

<!-- data: teaser -->
> **Rook:** "Mines from behind? Then I want eyes in the back of the ship. That Swarm Tail,
> Lancer."
<!-- /data -->

The hangar before L30 sells the [Swarm Tail](../../../player/weapons/swarm-tail/README.md) (the plan buys it at
L2 for the rear: [Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)); Rook's MIA card
is gone and the escort slot defaults to him with his guns and upgrades, the loaner gone back to the CDF.

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule): the Minelayer
columns and a train from behind, the trains and a Mite rock group from the sides.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `space` (= `air`: Minelayers, Gilded Gunships and their Darts, Rail Serpents, Talon wings, Skitter and Mote streams, Asteroid Mites on rocks); **no ground** |
| Attack directions | front 92% · sides 3% · rear 5% |
| Density | 3 |
| Recommended traits | `rear`, `piercing` |
| Hazards | Mine lines (proximity, chained clusters); drifting rocks (small break, large are walls) |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: the belt, shipping lane, `space`, front and rear · **L1**: + directions (rear, sides), density 3, hazards "mine lines" and "drifting rocks", the objective `CLEAR THE LANE`, the secondary `CONVOY 0/3` and the row `NO GROUND` · **L2**: + Minelayer, Gilded Gunship, Rail Serpent, Dart Drone (new), Talon, Asteroid Mite, Skitter and Mote portraits · **L3**: + `rear` and `piercing` highlighted, the wave strip marking the rear entries and the convoy |
<!-- /data -->

*Varga's intel line* per sensor-suite level (must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "The lane out of Ceres, Lancer. Open space, and mined." |
| L1 | "Layers come from behind. No ground: the bombs stay idle." |
| L2 | "New: Minelayers, plated Gunships, drone trains, darts." |
| L3 | "A rear gun for the layers, piercing to cut the lines." |

## Objective

- **Primary** `reach-end`: clear the lane to its last beacon (the open belt of Level 31).
- **Secondary** `escapes` (the Gunship convoy): the three convoy Gunships of section 3 (t=96, 103, 110, 7 s
  apart so at most two are on the screen) must all be destroyed before they leave the bottom edge (`escapes:
  gilded-gunship`, `allowed: 0`, the HUD counter `CONVOY`, `DONE` when the third falls), failed at once by one
  escaping. Only the three convoy Gunships count (the others are ordinary waves). Pays **100** credits in Act 1
  terms (**655** in Act 5, the act factor 6.5536); the typical haul counts 50 %. Each Gunship is on the screen
  ≈ 13 s, so a convoy Gunship is a 13 s window under its own rings and Darts: from the side or behind it falls in
  well under a second ([its arcs](../../../enemies/air/gilded-gunship/README.md#stat-block)).
- **No ground** (`ground: none`, the act's only level without it, J3): the Bomb Rack and every other
  bomb delivery read `NO GROUND` and stay idle while drawing their power
  ([bomb rack](../../../player/weapons/bomb-rack/README.md)); the content validator rejects ground targets,
  terrain and a ground convoy here. The **Airstrike works** (it deals its damage to `air`), the EMP Burst too.

## Layout

Scroll **120 px/s** in the lane's first section, **80 px/s** in the minefield (sections 2–3: a mine line stays
on the screen ≈ 7 s and the gaps are readable), **100 px/s** in the last section; each change eased over 2 s.
The setting is [the belt](../../../world/belt/README.md#sub-locations-per-level)'s shipping lane: open space
over the dust band and the distant sun, a step lighter than Act 4 (Q11 b); the lane's **beacon buoys** (white
strobes on masts, every ≈ 600 px down the left and right thirds) on the `low-air` layer; large rocks tumbling
slowly on the `low-air` and `deep` layers; close rock fragments and dust on `high-air`. No ground layer at all:
the backdrop is space and its layers only. Motion budget: the mines' red-gold blink and the trains are the
strong elements; the buoys' strobes are slow (one flash in 2 s).

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Lane Marker | 0–30 | 0–3,600 | 120 | clear | `low-air`: the first beacon buoys. `air`: a few small rocks with riders. `deep`: Ceres falling behind, the dust band. | Popcorn and Talons; **the first Minelayer alone** (t=20) and its single line: learn the gap. |
| 2. The Seeded Lane | 30–88 | 3,600–8,280 | 80 | clear | `air`: the rock field: small breakable rocks and **one large** at a time. `low-air`: buoys, some dark (shot out). | Lines in pairs from behind; the Gunship alone (t=40, quiet window t=38–42); the first Rail Serpent from the right (t=62). |
| 3. Convoy Crossing | 88–134 | 8,280–11,960 | 80 | clear | `air`: rocks (up to two large); the dead beacon buoy drifting at t≈118 (the secret). `deep`: the convoy's running lights far ahead before it arrives. | The **convoy secondary** (t=96–110) between lines laid ahead of it; a train from behind (t=122). |
| 4. Lane End | 134–178 | 11,960–16,340 | 100 | clear | `air`: thinning rocks. `low-air`: the lane's last beacon, a tall mast with a green strobe. `deep`: the open belt ahead. | Lines, a Serpent pincer (t=145), a Gunship pair, the run to the open belt. Level end at t=178 as the last beacon passes the bottom edge. |
<!-- /data -->

### Mine lines in this level

The shared [mine lines](../../../enemies/README.md#mine-lines) rule and the
[Minelayer](../../../enemies/space/minelayer/README.md#the-mine-line-q3-a)'s numbers, laid out for L30 by the L30
gap check's **C1 a** and **C2 a** (approved in round 60; see *Open questions* for the
mismatch with the Minelayer draft):

- **Clusters** (C1 a): a line is **three clusters of 4 mines**, 32 px apart within a cluster, **56 px** between
  two clusters and **one gap of 112 px** (between two clusters' end mines: the gap's middle is 56 px from both,
  outside the 48 px proximity): 12 mines across the 480 px field. The gap sits at one of the two cluster
  boundaries, **authored per wave** (left-centre at x≈160 or right-centre at x≈320), so the level reads; the
  first line (t=20) has its gap at the centre-left, the column pairs alternate.
- **Chain** (C1 a): a shot mine bursts with its ring and sets off the mines within **44 px**, hop by hop
  (≈ 0.08 s a hop), so **one shot clears its whole cluster of 4** and stops at the 56 px space: one shot opens a
  208 px hole between the neighbouring clusters' mines, a maze of choices rather than one trivial shot per line. Chained mines burst without a ring.
- **Proximity blast** (C2 a): a proximity burst adds a **40 px blast** (`heavy` 10, once) at the mine to the ring
  of 8 (the ring stays outside the 72 px no-fire distance like every ring), so sitting beside a blinking mine is
  not safe.
- **Laying and scroll**: mines hold their place in the field and come down with the scroll. A Minelayer lays a
  line in ≈ 3.2 s of strafe (480 px at 150 px/s), so at 80 px/s the line **slopes ≈ 28°** down toward the side
  the layer started from; the gap is still one. A column's two layers enter 2.5 s apart and lay at the 100 and
  180 px rows: the two lines are ≥ 120 px apart. With a column every 17–19 s, at most **two lines** are on the
  screen at medium (three on hard: the Minelayer's column of 3), under the Minelayer's limit of three and the
  mine cap of 40 (3 × 12 = 36).
- **Who triggers**: only the ship and Rook set off a proximity; rocks and mines never touch; a passing enemy
  does not set one off (the L30 gap check's decided trivia). Mines pay nothing (the Minelayer's credits: 0).
- **Rook in the minefield**: he flies through the gaps and keeps out of the mines' proximity discs, as he keeps
  out of vents ([wingmen](../../../player/wingmen/README.md)); he never picks a mine as a target.

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). Measured with `levelReport` (the plan's fit with Rook; from step C3 the mine-line autopilot,
`MinePlay`, flying it).

- **HP** from the proposed Act 5 reference DPS (J2: **124** at L30,
  [balancing basis](../../../enemies/README.md#balancing-basis)): the Minelayer, Gunship, Rail Serpent and Dart
  Drone are new here; the Talon is `small` (no act factor); the rest are `tiny`. No act HP factor applies.
- **Density and pacing** (the Act 2+ floor from the start; step C2, measured): **322 enemies** in the waves
  (≈ 109 a minute at medium; with the Darts the flight launches, about 24, ≈ 117: the act's minimum 70, the target
  band 80–120; a Rail Serpent train counts as one): the README's 53 waves and, as in Level 29 (the beam erases the
  fodder within a second), offbeat firing Talon pairs, short Skitter snakes and two more Mite rock groups, the Mote
  Swarms 8 strong, every Mite wave a group of 4 rocks drifting in together (round 62). Waves by direction: front 68
  of 82, rear 9 (the Minelayer columns and one train), sides 5 (two trains, the pincer's two and one Mite rock
  group): **non-front 17 %**, under the 35 % cap; every rear and side entry **edge-warned 3 s** (the bottom edge's
  warning for the layers' lanes). The floor at medium: **3.35 enemies on the screen, 44 shots a minute, no pause
  over 2 s** (step C3: the plan's fit with Rook, the autopilot flying the lines; step C2 measured 2.75 and 40 with the
  ship invulnerable); easy 2.45 and 27, two stretches just over 2 s (none over 3); hard 5.40 and 94 (in Level 27's and
  Level 28's range on hard: 5.81 and 8.50). Quiet window t=38–42: the first Gunship alone.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 7 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 8.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 10 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 13 | 1 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | 4 rocks drifting in together among the lane's first rocks (2 with 2 riders, 2 with 1); round 62: Mites in numbers, arriving together |
| 13 | 1 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | front | |
| 16 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 18 | 1 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Step C2: a rock group on the offbeat (the beam erases the fodder within a second) |
| 18 | 1 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | front | |
| 20 | 1 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 1 | rear (left) | The first Minelayer, alone; warned 3 s ahead at the bottom-left edge; its line (gap at the centre-left) is laid at ≈ t=24 |
| 23 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 26 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Leader at the tip |
| 29 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 32 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The lane narrows into the rock field |
| 33.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 35 | 2 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (right) | Two lines (gaps right, then left); warned 3 s ahead |
| 40 | 2 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | The Gunship's first appearance, alone (quiet window t=38-42); about 6 Darts launched |
| 43 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 44.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 46 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | 4 rocks together (2 with 2 riders, 2 with 1) |
| 46 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | front | |
| 49 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 52 | 2 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (left) | Warned 3 s ahead |
| 55 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 57 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Step C2: a rock group on the offbeat |
| 57 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | front | |
| 58 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 62 | 2 | snake | [Rail Serpent](../../../enemies/air/rail-serpent/README.md) | 1 | right side | 10 cars; the train's first entry, warned 3 s ahead at the right edge |
| 65 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 68 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Leader at the tip |
| 71 | 2 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (right) | Warned 3 s ahead |
| 74 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | left side | 4 rocks drifting in together from the left edge (2 with 2 riders, 2 with 1); warned 3 s ahead |
| 74 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | left side | |
| 77 | 2 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | 2 s apart; about 12 Darts launched |
| 80 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 81.5 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Step C2: a rock group on the offbeat |
| 81.5 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | front | |
| 83 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 84.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 86 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 89 | 3 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (left) | Warned 3 s ahead; lines ahead of the convoy |
| 92 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 94 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 96 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | **Convoy 1/3** (the secondary); about 6 Darts |
| 96 | 3 | carrier + escorts | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Convoy 1's escorts, in `orbit` at 90 px |
| 99 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 101 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | Step C2: on the offbeat |
| 103 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | **Convoy 2/3**; about 6 Darts |
| 103 | 3 | carrier + escorts | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Convoy 2's escorts |
| 106 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 108 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 110 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | **Convoy 3/3**; about 6 Darts; it carries the shield cell |
| 110 | 3 | carrier + escorts | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Convoy 3's escorts |
| 113 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 114.5 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | Step C2: on the offbeat |
| 116 | 3 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (right) | Warned 3 s ahead |
| 119 | 3 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 6 | front | 4 rocks together (2 with 3 riders, 2 with 1) |
| 119 | 3 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | front | |
| 122 | 3 | snake | [Rail Serpent](../../../enemies/air/rail-serpent/README.md) | 1 | rear | 10 cars up from the bottom edge; warned 3 s ahead; Rook's six bark (t=123.5) |
| 125 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 128 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 130 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 131 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 135 | 4 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (left) | Warned 3 s ahead |
| 137 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Step C2: a rock group on the offbeat |
| 137 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | front | |
| 138 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 6 | front | Leader at the tip |
| 141 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 143 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 145 | 4 | snake | [Rail Serpent](../../../enemies/air/rail-serpent/README.md) | 1 | left side | The pincer's left train (10 cars), warned 3 s ahead at the left edge |
| 145 | 4 | snake | [Rail Serpent](../../../enemies/air/rail-serpent/README.md) | 1 | right side | The pincer's right train (10 cars), warned 3 s ahead at the right edge; easy: one train |
| 149 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 152 | 4 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (right) | Warned 3 s ahead |
| 155 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 158 | 4 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | 2 s apart; about 12 Darts |
| 161 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 164 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 6 | front | |
| 167 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | 4 rocks together (2 with 2 riders, 2 with 1) |
| 167 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | front | |
| 168.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step FIX6: t=168.5, not 174.5 (the calm before the end). Step C2: the last Talons over the lane's end |
| 170 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The last wave |
| 170.5 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Step FIX6: t=170.5, not 172 (the calm before the end). Step C2: on the offbeat |

Totals: Skitter 74 · Mote Swarm 96 · Talon 69 · Asteroid Mite 56 · Minelayer 15 · Gilded Gunship 8 · Rail Serpent 4.
<!-- /data -->

## Ground targets

None: the level has no ground layer (`ground: none`).

## Hazards

- **Mine lines**: above (*Mine lines in this level*); fifteen lines in all.
- **Drifting rocks** ([belt hazards](../../../world/belt/README.md#hazards--set-pieces), Level 03's `debris`
  rules with belt chunks): small rocks drift at 30–60 px/s plus the scroll, break when shot (their HP in the data)
  and pay nothing; **large** rocks are indestructible walls with contact damage 10 once a second (step FIX6; `max_large` 1 in section 2, 2 in
  sections 3–4, never inside a line's gap at the moment it is laid); bullets of both sides stop on both. The Mites
  ride small ones ([rock riding](../../../enemies/README.md#rock-riding)).

## Secrets and pickups

- **The dead beacon buoy** (t≈118, x 60, drifting down the left edge): a small breakable object on the play plane
  (as built: Level 07's tow rule, its housing takes 4 hits; shots pass the rest; a beam's touch counts one hit per
  0.25 s, `beam_touch`, round 64 C.1 a); broken, its housing frees a hidden crate worth **100** in Act 1 terms (655 in Act 5),
  the lane authority's emergency stores. The Salvage scanner's glint marks it. Rook's event line: "*Free stores from
  the lane authority. They won't miss them.*" (dropped after 6 s if the radio is busy).
- **Overdrive**: dropped by the t=26 squad leader.
- **Shield cell**: dropped by the third convoy Gunship.
- **Armour patch**: a CDF lane-service drone (a small white craft, presentation like Level 22's ice-crawler)
  releases one at (240, 380 y-up) at t=100.

## Radio chatter

Spoken radio, drafted for the **1 s rule** (≈ 12 characters a second, a 1.5 s lead, 0.4 s of silence; event lines
drop after 6 s); `RadioTimelineLevel30Test` checks them with Rook and without. Rook is back: his barks carry
`requires: escort` (the six bark at t=123.5); his scripted lines (t=9, t=47, t=160) do not, nor does his secret line
(a secret's line has no `requires`). The t=142 flank line is Varga's, as the table drafted it.
Speakers: Okafor, Varga, Rook. No enemy-channel line here.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, the Ceres loyalists need this lane open." |
| t=9 | Rook | "No water, no tunnels, no crate. I could kiss this lane." |
| t=17.5 | Varga | "A Minelayer, rising behind you. It lays a line. Find the gap." |
| t=27 | Varga | "Shoot a mine, its cluster goes. Fast blink? It bursts." |
| t=37 | Okafor | "Gunship ahead. Gold nose plate: hit its sides or engines." |
| t=47 | Rook | "Little darts coming out of that thing. Of course there are." |
| t=59 | Varga | "A drone train, right. Cut it near the engine: the rest dies." |
| t=88 | Okafor | "Convoy inbound: three Gunships with mines. None gets through." |
| Secondary met (the third convoy Gunship down) | Okafor | "Convoy down. That's a lot fewer mines in the lane, Aegis." |
| Secondary failed (the first Gunship leaves the screen alive: an escape cue is per enemy, not per tag) | Varga | "One got through. Its mines will be in the lane by morning." |
| t=123.5 | Rook | "Six o'clock! A train's coming up behind us." |
| t=131 | Okafor | "The loyalists board a mining tug. This lane is their way out." |
| t=142 | Varga | "Two trains, both sides. Pick one." |
| t=160 | Rook | "Hey, Lancer. Thanks. For Dock 9. I'm not saying it twice." |
| Level end (the closing line) | Okafor | "The lane is open. Well flown, both of you." |
<!-- /data -->

Timeline check (step C2, at the HUD's text model: pages of 3 × 22 characters, 30 characters a second, 3 s a page
and 5 s for the last, 0.4 s between lines): the drafted lines ran two pages each and up to 8 s late, so each timed
line is trimmed to one page (≈ 7 s; the voiced takes are 2.6–5.6 s). They close at ≈ 7.5, 15.8, 24.5, 33.6, 43.8,
53.9, 65.9, 95.0, 125.4, 135.0, 148.1 and 166.8: nothing is late. The secondary's lines fall at ≈ t=112–125 (the
convoy's last Gunship). Step C3 measured the convoy's line with the autopilot at t=104–107 (easy) and t=112.6–116.9
(medium, hard): a one-page event line holds the radio ≈ 7.3 s, so Rook's six bark moved from t=119 to **t=123.5**
(the rear train enters at t=122) and Okafor's tug line from t=128 to **t=131** behind it; no text changed. Rook's t=9 line is his one dry joke; t=160 is the warm one, the only word about his captivity (the
tone guide: a pause, a line, back to work).

## Music & ambience

The Act 5 A theme **"Hollow Rock"** ([track 12](../../../audio/music/README.md#track-list), full length from M9
part B): the **base stem** in section 1, the **full mix from section 2** (the first column), its conveyor breakdown
(the lone human horn) under the convoy if the stems allow, with **Vorne's motif stem on top** from the base stem's
start to the end (the [Act 5 cues](../../../audio/music/README.md#cues-per-level-act-5): "on"); the per-level music table and whether a later level takes
the B theme (track 13) are D-audio's. Ambience: the `belt-open` bed (hull creaks, distant impacts, radio hiss, heard
through the hull). New sounds this level needs, for the act's sfx step ([sfx](../../../audio/sfx/README.md)): the
mines' drop, arm, blink, blast and chain ripple, the Minelayer's turbine, the Gunship's bay doors and ring, the Dart
launch, the Rail Serpent's rumble, coupler snap and split (their documents' briefs), the beacon buoy's faint strobe
tick; Freesound only for real things (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(30) = 700 × 1.07²⁹ ≈ **4,980** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act
1 terms** (Mote 2, Skitter 5, Talon 12, Dart Drone 2, Asteroid Mite 3, Minelayer 30 (26 before step FIX6), Gilded Gunship 28, Rail Serpent
83 dismantled tail first, the crate 100, the secondary 100), × the act factor **6.5536**, bounties also × the scale.
A **`bounty_scale` of 0.43** (the earlier acts' model: typical = 0.663 of the kills, half of the secret and the
secondary; the draft's 0.45, fitted in step C2 with the offbeat waves; a Gunship counts two streams of its Darts, the
README's "two streams before a typical kill", `TypicalHaul` and `tools/sync_tables.py` alike) puts the typical haul on
budget (table below). No boss or mid-boss, no ground targets.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 74 × 5 + Mote Swarm 96 × 2 + Talon 69 × 12 + Asteroid Mite 56 × 3 + Minelayer 15 × 30 + Gilded Gunship 8 × 28 + Rail Serpent 4 × 73 + launched Dart Drone 48 × 2 | 7,413 | 4,448 |
| Squad leaders: 3 × 13 extra (perfect run only) | 108 | 0 |
| Secret: dead beacon buoy (hidden crate, 2% of budget) | 655 | 328 |
| Secondary: stop the Gunship convoy | 655 | 328 |
| **Total** (bounty scale 0.43) | **8,831** | **5,103** |
| Budget(n) = the typical haul's target; typical +2 %, perfect 1.77 × budget | | 4,980 |
<!-- /data -->

**Balance plan**: the plan's L30 visit buys the Swarm Tail at L2 for the rear and Rook returns
([Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)); the level expects `rear` (the Minelayers
before they turn, the trains from behind) and `piercing` (through a cluster), and is flyable with a front-only loadout
(the layers are hit once they strafe ahead, the lines have their gaps). The Bomb Rack is idle; the plan fits none.

## Difficulty notes

- **Easy**: the Minelayer's easy hooks (a wider gap: here 144 px, the cluster beside it 3 mines; rings of 6); the convoy
  secondary needs 2 of 3 (`allowed: 1`); the Serpent pincer becomes one train; the Gunships' easy hooks.
- **Hard**: columns of 3 (three lines; the third at the 260 px row, ≥ 120 px from the second), the mines arming in
  0.8 s; the convoy's escorts 3 a Gunship; the Rail Serpents' 14 cars; the other waves by the formation lever, trimmed
  by hand so no more than 16 Darts and two Gunships are on the screen.

## Concept art

Round 60's proposals for the level live in their parts: the belt mood still for the shipping lane (in the
[belt](../../../world/belt/README.md#concept-art)) and the unit sheets in their documents.

Production art, M9 part C (concept round 63, step ART-C): the backdrop straight to production in round 60's belt look
(A.18 a, the lighter grade), space only (`tools/art/backdrop_l30.py`, with its `backdrop:` block, sections and debris
places for step C2 in [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml)); the belt's large rocks and the
dead beacon buoy (`tools/art/belt_units_c.py rocks`). The units' frames are in their documents
([Minelayer](../../../enemies/space/minelayer/README.md), [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md),
[Dart Drone](../../../enemies/air/dart-drone/README.md), [Rail Serpent](../../../enemies/air/rail-serpent/README.md)), the
two briefing images in the [briefing](../../../ui/briefing/README.md#concept-art). Briefs:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r63-a.png](concept/backdrop-final-r63-a.png) | Production backdrop (`tools/art/backdrop_l30.py`): eight screens at half size with stand-ins (Ceres falling behind, the buoys down both sides, the seeded lane's large rocks and a mine line, shot-out buoys, the convoy's lights far ahead, the dead buoy, the lane's end, the last beacon and the open belt), the pieces at 1× and the tile sets | chosen (round 63, F.5-F.6: accepted as final) |
| [concept/backdrop-final-r63-a.gif](concept/backdrop-final-r63-a.gif) | t 14–24 at half size: the buoys' strobes (one flash in 2 s), Ceres sliding away, the rocks drifting | chosen (round 63, F.5-F.6: accepted as final) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block, its sections' tiles and the `debris:` block (the chunks, the large rocks' and the dead buoy's places) for step C2 (not data) | chosen (round 63, F.5-F.6: accepted as final) |
| [concept/belt-rocks-final-r63-a.png](concept/belt-rocks-final-r63-a.png) | The belt's large rocks (`tools/art/belt_units_c.py`): `belt-large-a` and `-b` at 16 spin frames (indestructible walls), the dead beacon buoy at 16 tumble frames and shot open; beside part B's small rocks at 1× (sheet) | chosen (round 63, F.5-F.6: accepted as final) |
| [concept/belt-rocks-final-r63-a.gif](concept/belt-rocks-final-r63-a.gif) | Two large rocks and two small ones drifting down with the scroll; the dead buoy shot open, its crate's light | chosen (round 63, F.5-F.6: accepted as final) |
| [concept/minefield-capture-r63-a.png](concept/minefield-capture-r63-a.png) | Game capture (step C5): a laid line and its gap, armed mines blinking by the ship, a blinking mine's blast and ring (the ship left mid-screen, not firing); a lit cluster set off hop by hop beside the firing ship | chosen (round 63, F.5-F.6: accepted as final) |
| [concept/minefield-capture-r63-a.mp4](concept/minefield-capture-r63-a.mp4) | Game capture with the game's sound, two cuts: t≈15–30 (the `BOTTOM` warning, the first Minelayer laying, the line coming down, the blink and the blast; the ship not firing) and t≈40–49 (a Gunship's ring and Darts, a cluster going off) | chosen (round 63, F.5-F.6: accepted as final) |
| [concept/level-30-hud-capture-r63-a.png](concept/level-30-hud-capture-r63-a.png) | Game capture, t≈103 (a debug loadout with the bomb rack): the weapons row `BOMB RACK  NO GROUND`, the secondary `CONVOY 1 / 3`, Rook on the radio | chosen (round 63, F.5-F.6: accepted as final) |
| [concept/dead-buoy-capture-r63-a.png](concept/dead-buoy-capture-r63-a.png) | Game capture, t≈119–124 (the ship moved to the left edge): the beam on the dead beacon buoy, shot open, its crate drifting down and collected (+655) | chosen (round 63, F.5-F.6: accepted as final) |
| [concept/dead-buoy-capture-r65-a.png](concept/dead-buoy-capture-r65-a.png) | Game capture (step E5; [recipe](concept/prompts.md#dead-buoy-capture-r65-a)), t=118.2–120.6 at 2x: the dead buoy freed by the beam's touch (round 64 C.1 a) | chosen (round 65, C.1 a accepted: the beam frees a dead buoy) |

## Implementation

- [x] Level data `data.yaml` (M9 part C): `ground: none`, sections and speeds with 2 s eases, waves with authored
      gaps, the `debris` block, radio, budget, threat profile, briefing, backdrop, music
- [x] `NO GROUND`: bomb deliveries idle and drawing power, the HUD's word; the validator rejects ground content here;
      the Airstrike and the EMP Burst work (steps C1, C4: `NoGroundTest`, `Level30Test.theBombsIdleAndTheAirstrikeWorks`,
      `PartEHudTest.aBombRackReadsNoGroundInSpace`; seen in round 63's captures)
- [x] Mine lines as above: clusters of 4 (32 px), 56 px between clusters, one authored 112 px gap; arming, proximity
      blink, the 40 px blast and the ring; the cluster chain; scroll-following lines; Rook passes the gaps (steps C1–C4:
      `MineLineTest`, `Level30Test` (the lines and gaps, a shot cluster, a blinking mine's blast), the chain reach 44 px;
      the line mine's look `MineLooks`; seen in round 63's captures)
- [x] The Gunship convoy secondary (`escapes: gilded-gunship`, `allowed: 0`, the three convoy Gunships only, `CONVOY`
      counter; easy `allowed: 1`) (steps C2–C4: `Level30Test.theConvoyIsTheSecondary`, `PartEHudTest`'s `CONVOY` fit;
      met on every autopilot flight)
- [x] Wave script: ≥ 70 enemies a minute at medium with the launched Darts (`DensityTest`), the Act 2+ floor
      (`PacingTest`, `ActFloorTest`), non-front ≤ 35 % of the waves with 3 s edge warnings (`Level30Test`), the quiet
      window t=38–42
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel30Test`, with Rook and without); Rook's barks
      `requires: escort` (steps C2–C4: 18 voiced files, `VoiceFilesTest`; C3's retimes t=123.5 and t=131)
- [x] Threat profile with the `NO GROUND` row, Varga's four intel lines and Rook's teaser fitting the intel panel
      (`IntelPanelLayoutTest`); the new units' portraits (step C4: `tools/art/intel.py`, the sheet
      [intel-final-r63-a.png](../../../ui/hangar/concept/intel-final-r63-a.png); the wave strip does not mark the
      convoy yet: round 63)
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images (step ART-C's
      two images; step C5: Okafor's page shortened to four lines and re-rendered)
- [x] Typical haul within ±5 % of budget(30) = 4 980 with the level's `bounty_scale` (`TypicalHaulTest`)
- [x] Music and ambience: "Hollow Rock" by section, the `belt-open` bed, the mine and unit sounds (`Level30SoundsTest`;
      step C4, `MinefieldCues`: round 63's **a** proposals play; the picks mine-arm b, serpent-split b, gunship-ring b and gunship-doors b wait for a path change in the game; heard in round 63's captures)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot (a mine-line play: the gap, shooting a cluster, the
      rear layers) flies the level with Rook on every difficulty and with `--escort none`
- [x] Production art and captures for part C's round (steps ART-C, C4, C5: [round
      63](../../../concept-rounds/round-63/README.md), the captures in this part's and the units' `concept/`)
- [x] The dead buoy counts a beam's touch, one hit per 0.25 s (round 64 C.1 a; `beam_touch` on its tow; step FIX1:
      `TowBeamTest`, `Level30Test.thePlansBeamFreesTheDeadBuoy`: the plan's Ion Beam frees it at t≈118.5 on medium)
- [x] Vorne's motif stem on top of the theme from section 1 (step FIX1: `music.motif`, `Level30SoundsTest`); the
      Rail Serpent's rumble is round 64's redo a (`serpent-rumble-r64-a`, step FIX1)

## Open questions

- **Q3** (round 60, minefields): drafted as **a** (lines with one gap, proximity, chained set-off). Alternatives: (b)
  static contact mines as walls, no burst; (c) seeker mines drifting toward the ship.
- **C2** (the proximity burst near the ship): drafted as **a**, a 40 px `heavy` 10 blast plus the ring. Alternatives:
  (b) ring only (hugging a blinking mine is safe); (c) the mine's ring exempt from the 72 px rule.
- **J4** (the Dart Drone built for this level, part C, not part D): drafted so; without it the level's density falls to
  ≈ 85 a minute (still inside the band).
- **Q6** (popcorn), **Q12** (the Swarm Tail in this hangar) as their documents draft them.

## Decisions

- 2026-10-27 (settled in the Act 5 design phase, step `m9-r60`; **C1 itself is asked in round 60**): the **C1 mismatch** is
  settled: the [Minelayer](../../../enemies/space/minelayer/README.md) follows this level (mine lines in clusters of 4, a
  shot clears its cluster, mines pay nothing). The **line slope** is kept: a line laid in a 3.2 s strafe while the field
  scrolls slopes (about 28 degrees at 80 px/s); it reads as a laid line and stays one barrier with one gap; the
  staggered 2.5 s entry of the lay rows (100/180 px apart against the 120 px minimum) is the rule.
- 2026-10-27 (Act 5 design phase, step `m9-d-levels-1`; **approved in round 60**): promoted from the act
  roster to a draft level document, hand tables only, no `data.yaml`. Space only (`ground: none`, J3), the Airstrike
  allowed; the mine lines by C1 a (clusters of 4, an authored 112 px gap) and C2 a (the 40 px blast); fifteen lines from
  eight Minelayer waves, all from behind; the convoy secondary = all three convoy Gunships (easy 2 of 3); ≈ 101 enemies a
  minute with ≈ 48 launched Darts (J4), non-front 23 % of the waves; `bounty_scale` 0.45; mines pay nothing (the
  Minelayer draft's 0, not the gap check's trivia of 1 credit); Rook back in the slot, his only word on the captivity at
  t=160.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`, D.3): Q3 **a** (minefields), J.3 (`NO GROUND` here), J.4 (the Dart Drone built in this part), J.5 **C1 a, C2 a** (clusters of 4, a 40 px blast with the ring); the mine sounds are S.14 a (blink) and S.15 a (burst). Q11 **b**: gunmetal/charcoal units.
- 2026-10-28 (step ART-C, production art; accepted in round 63, 2026-10-28): the backdrop is space only: deep
  (the lane's dust band; Ceres falling behind, far rocks, the convoy's lights, the open belt), low-air (the beacon buoys
  every 600 px down both sides, a third of section 2's dark; the last beacon's green strobe; dimmed rocks tumbling past,
  bluish and darker than the play plane's so they never read as walls), high-air (sparse close grit). The convoy's
  running lights are on the deep layer, not low-air as *Layout* says: "far ahead" only reads at the deep layer's slow
  parallax. At most two animated pieces on the screen (the buoys' strobe with a tumbling rock or the last beacon). The
  dead beacon buoy (the secret) is proposed as a debris chunk `lane-buoy` (20 HP, its sprite `debris-lane-buoy` with a
  break), the large rocks as chunks `belt-large-a` / `-b` (contact `large` 20) at x 60, 240 or 420, out of the gaps'
  middles: C2 decides both in the data. The CDF lane-service drone (the armour patch, t=100) has no art yet.
- 2026-10-28 (step C2, the data; accepted in round 63, 2026-10-28): the level script as
  [data.yaml](data.yaml), the marked tables rendered from it. Measured with `levelReport` and the plan's fit with Rook,
  the ship invulnerable (the plain autopilot does not fly mine lines: step C3's): the README's 53 waves gave 1.82
  enemies on the screen and 13 stretches over 2 s at medium (the Ion Beam L4 erases the fodder within a second, as in
  Level 29), so step C2 added Level 29's offbeat answer: 12 firing Talon pairs and short Skitter snakes, two more Mite
  rock groups (t=18, 57, 81.5 and 137 with the README's five: every group 4 rocks drifting in together, round 62's
  "Mites in numbers"), the Mote Swarms 8 strong: 82 waves, 322 units (≈ 109 a minute, ≈ 117 with the launched
  Darts), 2.75 on the screen, 40 shots a minute, no stretch over 2 s; non-front 17 % of the waves; `bounty_scale` 0.43
  (a Gunship's Darts counted as two streams). The convoy Gunships fly a `column` beside their escorts' `carrier +
  escorts` wave (a launcher is a carrier, but the formation has no carrier path for one yet); the Talon got an `orbit`
  for it. The dead beacon buoy is a **tow** (Level 07's rule: a debris chunk carries no secret), not ART-C's debris
  chunk `lane-buoy` (its sprite drawn by the tow's look in step C4); the large rocks at x 60 or 420 only (ART-C's x 240
  sat in both gap points), every rock drifting at the scroll plus its own (the proposal's drifts were slower than the
  scroll). The timed radio lines are trimmed to one page each (the drafts ran up to 8 s late), t=1 and t=27 reworded
  for the voice (Whisper read "The lane outages", "Fast blink at bursts"); the six bark at t=119 is written ("Six
  o'clock! A train's coming up behind us.", `requires: escort`); a secret's line takes no `requires`; the failed
  secondary's line is the first Gunship's escape (`enemy-escaped`: a cue per enemy, not per tag); the convoy's `CONVOY`
  counter on an escapes objective with no medium allowance (the content check now allows a tagged one with a
  difficulty's).
- 2026-10-28 (step C3, the level's tests and its autopilot; accepted in round 63, 2026-10-28): `Level30Test`
  (counts, `ground: none`, the lines and their gaps, a shot cluster, a blinking mine's blast, the layer's rear entry,
  the Gunship and the train as their stat blocks, the convoy, the direction share, the radio, whole runs with and
  without Rook) and the autopilot's mine-line play (`MinePlay`: every move planned among the lines, the rising layers'
  lanes, the rocks and the bullets; through a line where its beam burns a cluster). It wins every difficulty on every
  seed without a retry and meets the convoy every time. Three data changes: the Minelayer's **chain reach 44 px**, not
  40 (the t=20 line is laid at the first section's 120 px/s, its cluster neighbours 41 px apart on the slope, so a shot
  set off nothing; the [Minelayer](../../../enemies/space/minelayer/README.md)'s Decisions); Rook's six bark **t=123.5**
  (was 119) and Okafor's tug line **t=131** (was 128): the convoy's line, measured at t=112.6–116.9 at medium and hard,
  holds the radio ≈ 7.3 s and left the bark up to 5 s late. Hard measured with the autopilot: 5.40 on the screen, 94
  shots a minute, 380 units in the waves (C2's 423 counted the Darts), won 3/3 without a retry: left as it is, in Level
  27's and 28's range.
- 2026-10-28 (step C5, part C's close-out; accepted in round 63, 2026-10-28): Okafor's briefing page 1 ran to five
  lines where a page with an image holds four (`BriefingLayoutTest`), so it is shortened and re-rendered (pin 101, read
  back word for word): "Ceres is lost, but not everyone in it chose Vorne. The loyalists are leaving on a mining tug, and
  the only lane out runs through Ascendancy minelayers. Clear it before the tug sets out, and stop the Gunship convoy
  bringing the next load of mines. Rook flies with you again. Welcome back, Aegis Two." → "Ceres is lost, but not
  everyone in it chose Vorne. The loyalists are leaving on a mining tug, and the only lane out is mined. Clear it before
  the tug sets out, and stop the Gunship convoy bringing more mines. Rook flies with you again. Welcome back, Aegis Two."
  (Varga's page names the Minelayers). Weak spots from the build, asked in round 63 with the art: a laid line lands
  diagonal on the screen (the approved slope, about 28° at 80 px/s); the intel panel's wave strip marks the rear entries
  but not the convoy (the threat profile's L3 line asks for both); every beacon buoy's strobe flash ticks (a buoy each
  600 px down both sides); the Gunship faces down only; the convoy's lights are on the deep layer; the CDF lane-service
  drone has no art, so the t=100 armour patch appears without its carrier (as Level 29's).
- 2026-10-28 (user, [round 63](../../../concept-rounds/round-63/README.md), closed): the final art (**F.5** large rocks and dead buoy, **F.6** backdrop) accepted; **C.3** the offbeat waves, **C.4** the chain reach 44 px, **C.5** the convoy as a column with escorts, **C.6** the trimmed radio and **C.7** the dead buoy as a tow accepted; sounds: S.2 a, S.3 a, S.4 a, S.9 a, S.10 a, **S.1 b**, **S.5 redo**, **S.6 b**, **S.7 b**, **S.8 b** (see the sfx README).
- 2026-10-28 (user, [round 64](../../../concept-rounds/round-64/README.md), closed): **C.1 a**: a beam's touch counts as one hit per 0.25 s on a tow's housing from Act 5 on, opt-in per level, so the dead buoy (4 hits) can be freed by the Ion Beam build too; built in the next game step. The Rail Serpent's rumble is the redo's a (`serpent-rumble-r64-a`).
- 2026-10-28 (step FIX1): round 64 C.1 a built: the buoy's tow opts in (`beam_touch: true`); a beam whose column
  touches the housing below its end counts a hit at once and every 0.25 s while it lasts (several beams as one), and
  passes on: the plan's Ion Beam frees it at t≈118.5 on medium, the autopilot flying around it. Level 07's lifeboat and
  the recorded flights are unchanged. The music block gains the motif cue the Act 5 table marks "on" (from t=0, the
  base stem's start), and the game plays round 64's serpent rumble.
- 2026-10-29 (user, [round 65](../../../concept-rounds/round-65/README.md), closed): the dead buoy freed by the beam's touch (round 64's C.1 a), recorded in a game capture, accepted with the level.
- 2026-10-30 (step FIX6, the user's playthrough 2026-10-30: "the minelayers are too weak ... the large number of
  collisions with asteroids and other ships nearly killed me"; power is full for every Act 5 fit by design, the Mk V
  from L35, so no new utility): the Minelayer lays sooner and holds longer (its README: 600 px/s up its lane, 200
  px/s across, 280 HP; the first layer lays its line through its gap against a fit aimed at it). Collisions, measured
  with a naive pilot (holds the lower centre (240, 80), sidesteps only shots, the user's fit, three seeds;
  `Level30Test.aNaiveFlightTouchesLittle`): **218 contact damage a run at medium before** (the large rocks 20 a
  touch ≈ 70, the trains crossing the lower centre ≈ 90 with each car a hit, the Minelayers ≈ 25, the Mites ≈ 15),
  **90 after** (easy 129 → 80, hard 562 → 265): the large belt rocks touch for **10**, not the `large` 20; the rear
  train (t=122) rises up the right (x 390 to y 500, not 340 at 520) and the pincer's loops (t=145) turn at y 420/430
  instead of 520/560, off the lower centre. The autopilot still wins 3/3 on every difficulty without retries. Decided
  2026-10-30 (round 70: no objection).
- 2026-10-30 (step FIX6, the user's playthrough 2026-10-30; the campaign's new end calm rule, at least 4 s from the
  last enemy gone to the end from Act 5 on, `PacingTest`): the level's end stays; the last waves earlier: the
  Skitter snake 172 → 170.5 and the last Talons 174.5 → 168.5; the end calm (easy/medium/hard, the autopilot with
  the plan's fit) 0.8/0.0/0.0 s → 5.6/4.6/4.8 s. The counts, the haul and the autopilot's wins are unchanged.
