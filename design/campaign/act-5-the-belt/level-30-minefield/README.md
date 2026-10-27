---
title: Level 30 – Minefield
design: draft
implementation: not-started
art: none
depends-on: [../../../enemies/space/minelayer, ../../../enemies/air/gilded-gunship, ../../../enemies/air/rail-serpent, ../../../enemies/air/dart-drone, ../../../enemies/README.md, ../../../player/weapons/swarm-tail, ../../../player/wingmen, ../../../world/belt]
updated: 2026-10-27
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

**Status: drafted (Act 5 design phase, 2026-10-27, step `m9-d-levels-1`); everything below is pending the
user's review in round 60.** Written from the act roster, the gap analysis's recommended answers (Q3 a, Q6 b,
Q12 c; J2, J3), the L30 build gap check's questions (C1 a: mines in clusters of 4, C2 a: a 40 px blast with
the ring; J4: the Dart Drone first flown here) and the wave 1 documents (the units above,
[mine lines](../../../enemies/README.md#mine-lines), [rock riding](../../../enemies/README.md#rock-riding), the
[belt](../../../world/belt/README.md), the [Swarm Tail](../../../player/weapons/swarm-tail/README.md), the
[wingmen](../../../player/wingmen/README.md#rooks-rescue-and-return-l29l30)). There is **no `data.yaml`**: the
build (M9 part C) turns the hand tables into one; no number is measured.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft):

> **Commander Okafor:** "Ceres is lost, but not everyone in it chose Vorne. The loyalists are leaving on a
> mining tug, and the only lane out runs through Ascendancy minelayers. Clear it before the tug sets out,
> and stop the Gunship convoy bringing the next load of mines. Rook flies with you again. Welcome back,
> Aegis Two."
>
> **Dr. Varga:** "Minelayers come up from behind and lay a line across your path with one gap. Find the gap,
> or shoot a mine: its cluster goes with it. The Gunships are plated in front; their engines are not. And
> there is no ground out there, Lancer: the bombs stay idle."

*Images* (production art in part C, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-30-shipping-lane`: a CDF chart of the lane from Ceres to the outer stations, the
  beacon buoys as a dotted line, the rock drift as grey arrows, the convoy's track in red, the tug's
  departure point marked.
- Varga's page `level-30-minelayer`: her scan of a Minelayer over a laid line seen from above, the clusters of
  four ringed, the one gap marked, an inset of a mine blinking.

*Hangar teaser* (shop screen before L30; Rook is back and speaks, not voiced):

> **Rook:** "Mines from behind? Then I want eyes in the back of the ship. That Swarm Tail, Lancer."

The hangar before L30 sells the [Swarm Tail](../../../player/weapons/swarm-tail/README.md) (the plan buys it at
L2 for the rear: [Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)); Rook's MIA card
is gone and the escort slot defaults to him with his guns and upgrades, the loaner gone back to the CDF.

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `space` (= `air`: Minelayers, Gilded Gunships and their Darts, Rail Serpents, Talon wings, Skitter and Mote streams, Asteroid Mites on rocks); **no ground** |
| Attack directions | front 77 % · rear 17 % · sides 6 % |
| Density | 3 |
| Recommended traits | `rear`, `piercing` |
| Hazards | Mine lines (proximity, chained clusters); drifting rocks (small break, large are walls) |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: the belt, the shipping lane, `space`, front and rear · **L1**: + directions (rear, sides), density 3, hazards "mine lines" and "drifting rocks", the objective `CLEAR THE LANE`, the secondary `CONVOY 0/3` and the row `NO GROUND` · **L2**: + Minelayer, Gilded Gunship, Rail Serpent, Dart Drone (new), Talon, Asteroid Mite, Skitter and Mote portraits · **L3**: + `rear` and `piercing` highlighted, the wave strip marking the rear entries and the convoy |

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
over the dust band and the distant sun, a step lighter than Act 4 (Q11 a); the lane's **beacon buoys** (white
strobes on masts, every ≈ 600 px down the left and right thirds) on the `low-air` layer; large rocks tumbling
slowly on the `low-air` and `deep` layers; close rock fragments and dust on `high-air`. No ground layer at all:
the backdrop is space and its layers only. Motion budget: the mines' red-gold blink and the trains are the
strong elements; the buoys' strobes are slow (one flash in 2 s).

| Section | t (s) | Scroll (px, approx.) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Lane Marker | 0–30 | 0–3,600 | 120 | clear | `low-air`: the first beacon buoys; `air`: a few small rocks with riders; `deep`: Ceres falling behind, the dust band. | Popcorn and Talons; **the first Minelayer alone** (t=20) and its single line: learn the gap. |
| 2. The Seeded Lane | 30–88 | 3,600–8,240 | 80 | clear | `air`: the rock field: small breakable rocks and **one large** at a time (`max_large` 1); `low-air`: buoys, some dark (shot out). | Lines in pairs from behind; the Gunship alone (t=40, quiet window t=38–42); the first Rail Serpent from the right (t=62). |
| 3. Convoy Crossing | 88–134 | 8,240–11,920 | 80 | clear | `air`: rocks (`max_large` 2); a dead beacon buoy drifting at t≈118 (the secret); `low-air`: the convoy's running lights far ahead before it arrives. | The **convoy secondary** (t=96–110) between lines laid ahead of it; a train from behind (t=122). |
| 4. Lane End | 134–178 | 11,920–16,320 | 100 | clear | `air`: thinning rocks; `low-air`: the lane's last beacon, a tall mast with a green strobe; `deep`: the open belt ahead. Level end at t=178 as the last beacon passes the bottom edge. | Lines, a Serpent pincer (t=145), a Gunship pair, the run to the open belt. |

### Mine lines in this level

The shared [mine lines](../../../enemies/README.md#mine-lines) rule and the
[Minelayer](../../../enemies/space/minelayer/README.md#the-mine-line-q3-a)'s numbers, laid out for L30 by the L30
gap check's **C1 a** and **C2 a** (pending the user's review in round 60; see *Open questions* for the
mismatch with the Minelayer draft):

- **Clusters** (C1 a): a line is **three clusters of 4 mines**, 32 px apart within a cluster, **56 px** between
  two clusters and **one gap of 112 px** (between two clusters' end mines: the gap's middle is 56 px from both,
  outside the 48 px proximity): 12 mines across the 480 px field. The gap sits at one of the two cluster
  boundaries, **authored per wave** (left-centre at x≈160 or right-centre at x≈320), so the level reads; the
  first line (t=20) has its gap at the centre-left, the column pairs alternate.
- **Chain** (C1 a): a shot mine bursts with its ring and sets off the mines within **40 px**, hop by hop
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
`bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

- **HP** from the proposed Act 5 reference DPS (J2: **124** at L30,
  [balancing basis](../../../enemies/README.md#balancing-basis)): the Minelayer, Gunship, Rail Serpent and Dart
  Drone are new here; the Talon is `small` (no act factor); the rest are `tiny`. No act HP factor applies.
- **Density and pacing** (the Act 2+ floor from the start): **300 enemies** (waves 252 + an estimated 48
  launched Darts, 6 a Gunship: two streams of 3 before a typical kill; launched units count) over 178 s, **≈ 101
  a minute** at medium (the act's minimum 70, the target band 80–120; a Rail Serpent train counts as one); a wave
  every 3 s from t=4. Waves by direction: front 41 of 53 (77 %), rear 9 (the Minelayer columns and one train, 17
  %), sides 3 (a train, the Serpent pincer and one Mite pair, 6 %): **non-front 23 %**, under the 35 % cap; every
  rear and side entry **edge-warned 3 s** (`WarningEdge`, the bottom edge's warning for the layers' lanes). The
  floor comes from the Gunships' rings, the Talon pairs, the turret cars and the mines' rings: expected to meet ≤ 3
  pauses over 2 s, ≥ 2.5 enemies on the screen and ≥ 10 shots a minute (`PacingTest`, `ActFloorTest`), **to be
  confirmed with `levelReport`** in part C (the mines' rings count as shots only if the measure counts them).
  Quiet window t=38–42: the first Gunship alone.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) |  |
| 7 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |
| 10 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 13 | 1 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 6 | front | 2 rocks × 3 riders among the lane's first rocks |
| 16 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) |  |
| 20 | 1 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 1 | rear (left lane) | The first Minelayer, alone; warned 3 s ahead at the bottom-left edge; its line (gap at the centre) is laid at ≈ t=24 |
| 23 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |
| 26 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Leader at the tip |
| 29 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) |  |
| 32 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | The lane narrows into the rock field |
| 35 | 2 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (right lane) | Two lines (gaps right, then left); warned 3 s ahead |
| 40 | 2 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | The Gunship's first appearance, alone (quiet window t=38-42); about 6 Darts launched |
| 43 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) |  |
| 46 | 2 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 6 | front | 2 rocks × 3 riders |
| 49 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 52 | 2 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (left lane) | Warned 3 s ahead |
| 55 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |
| 58 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 62 | 2 | snake | [Rail Serpent](../../../enemies/air/rail-serpent/README.md) | 1 | side (right) | 10 cars; the train's first entry, warned 3 s ahead |
| 65 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |
| 68 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Leader at the tip |
| 71 | 2 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (right lane) | Warned 3 s ahead |
| 74 | 2 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 6 | side (left) | 2 rocks × 3 riders; warned 3 s ahead |
| 77 | 2 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | 2 s apart; about 12 Darts launched |
| 80 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) |  |
| 83 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |
| 86 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 89 | 3 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (left lane) | Warned 3 s ahead; lines ahead of the convoy |
| 92 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) |  |
| 96 | 3 | carrier + escorts | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | **Convoy 1/3** (the secondary): 2 Talons in `orbit` at 90 px; about 6 Darts |
| 96 | 3 | carrier + escorts | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Convoy 1's escorts |
| 99 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 103 | 3 | carrier + escorts | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | **Convoy 2/3**; about 6 Darts |
| 103 | 3 | carrier + escorts | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Convoy 2's escorts |
| 106 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) |  |
| 110 | 3 | carrier + escorts | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | **Convoy 3/3**; about 6 Darts |
| 110 | 3 | carrier + escorts | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Convoy 3's escorts |
| 113 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 116 | 3 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (right lane) | Warned 3 s ahead |
| 119 | 3 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 8 | front | 2 rocks × 4 riders |
| 122 | 3 | snake | [Rail Serpent](../../../enemies/air/rail-serpent/README.md) | 1 | rear | 10 cars up from the bottom edge; warned 3 s ahead; Rook's six bark |
| 125 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 5 | front |  |
| 128 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 131 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) |  |
| 135 | 4 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (left lane) | Warned 3 s ahead |
| 138 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 6 | front | Leader at the tip |
| 141 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 145 | 4 | pincer | [Rail Serpent](../../../enemies/air/rail-serpent/README.md) | 2 | sides | Two 10-car trains, one from each side, warned 3 s ahead at both edges |
| 149 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) |  |
| 152 | 4 | column | [Minelayer](../../../enemies/space/minelayer/README.md) | 2 | rear (right lane) | Warned 3 s ahead |
| 155 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 158 | 4 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | 2 s apart; about 12 Darts |
| 161 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) |  |
| 164 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 6 | front |  |
| 167 | 4 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 6 | front | 2 rocks × 3 riders |
| 170 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The last wave |

Totals: Mote Swarm 84 · Skitter 60 · Talon 49 · Dart Drone ≈ 48 (launched) · Asteroid Mite 32 · Minelayer 15
(15 lines) · Gilded Gunship 8 · Rail Serpent 4 (10 cars each).

## Ground targets

None: the level has no ground layer (`ground: none`).

## Hazards

- **Mine lines**: above (*Mine lines in this level*); fifteen lines in all.
- **Drifting rocks** ([belt hazards](../../../world/belt/README.md#hazards--set-pieces), Level 03's `debris`
  rules with belt chunks): small rocks drift at 30–60 px/s plus the scroll, break when shot (their HP in the data)
  and pay nothing; **large** rocks are indestructible walls with contact damage (`max_large` 1 in section 2, 2 in
  sections 3–4, never inside a line's gap at the moment it is laid); bullets of both sides stop on both. The Mites
  ride small ones ([rock riding](../../../enemies/README.md#rock-riding)).

## Secrets and pickups

- **The dead beacon buoy** (t≈118, x 60, drifting down the left edge): a small breakable object on the play plane
  (20 HP, like a small rock); broken, its housing frees a hidden crate worth **100** in Act 1 terms (655 in Act 5),
  the lane authority's emergency stores. The Salvage scanner's glint marks it. Rook's event line: "*Free stores from
  the lane authority. They won't miss them.*" (dropped after 6 s if the radio is busy).
- **Overdrive**: dropped by the t=26 squad leader.
- **Shield cell**: dropped by the third convoy Gunship.
- **Armour patch**: a CDF lane-service drone (a small white craft, presentation like Level 22's ice-crawler)
  releases one at (240, 380 y-up) at t=100.

## Radio chatter

Spoken radio, drafted for the **1 s rule** (≈ 12 characters a second, a 1.5 s lead, 0.4 s of silence; event lines
drop after 6 s); `RadioTimelineLevel30Test` checks them with Rook and without. Rook is back: his barks carry
`requires: escort` (the six bark at t=119, the flank bark at t=142); his scripted lines (t=9, t=47, t=160) do not.
Speakers: Okafor, Varga, Rook. No enemy-channel line here.

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "The lane to the outer stations, Aegis. The Ceres loyalists need it open." |
| t=9 | Rook | "Open space. No water, no tunnels, no crate. Lancer, I could kiss this lane." |
| t=17.5 | Varga | "Something rising behind you: a Minelayer. It seeds a line ahead. Find the gap." |
| t=27 | Varga | "Shoot one mine and its whole cluster goes. A fast blink means it's about to burst." |
| t=37 | Okafor | "Gunship ahead. Gilded nose plate: hit it from the side, or in the engines." |
| t=47 | Rook | "Little darts coming out of that thing. Of course there are." |
| t=59 | Varga | "A drone train on the right. Cut it near the engine and the rest goes dead." |
| t=88 | Okafor | "Convoy inbound: three Gunships carrying mines for the lane. None of them gets through." |
| Secondary met (`secondary-objective`, the third convoy Gunship down) | Okafor | "Convoy down. That's a lot fewer mines in the lane, Aegis." |
| Secondary failed (a convoy Gunship escapes) | Varga | "One got through. Its mines will be in the lane by morning." |
| The buoy's crate freed (event) | Rook | "Free stores from the lane authority. They won't miss them." |
| t=128 | Okafor | "The Ceres loyalists are boarding a mining tug. This lane is their only road out." |
| t=142 | Varga | "Two trains, both sides. Pick one." |
| t=160 | Rook | "Hey, Lancer. Thanks. For Dock 9. I'm not saying it twice." |
| Level end | Okafor | "The lane is open. Well flown, both of you." |

Timeline check (text): the timed lines close at ≈ 8.0, 17.1, 25.9, 35.7, 45.1, 53.8, 67.1, 97.1, 136.6, 146.7 and
166.7; nothing overlaps. The secondary's lines fall at ≈ t=112–125 (the convoy's last Gunship), in the gap between
t=97 and t=128. Rook's t=9 line is his one dry joke; t=160 is the warm one, the only word about his captivity (the
tone guide: a pause, a line, back to work).

## Music & ambience

The Act 5 A theme **"Hollow Rock"** ([track 12](../../../audio/music/README.md#track-list), full length from M9
part B): the **base stem** in section 1, the **full mix from section 2** (the first column), its conveyor breakdown
(the lone human horn) under the convoy if the stems allow; the per-level music table and whether a later level takes
the B theme (track 13) are D-audio's. Ambience: the `belt-open` bed (hull creaks, distant impacts, radio hiss, heard
through the hull). New sounds this level needs, for the act's sfx step ([sfx](../../../audio/sfx/README.md)): the
mines' drop, arm, blink, blast and chain ripple, the Minelayer's turbine, the Gunship's bay doors and ring, the Dart
launch, the Rail Serpent's rumble, coupler snap and split (their documents' briefs), the beacon buoy's faint strobe
tick; Freesound only for real things (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(30) = 700 × 1.07²⁹ ≈ **4,980** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act
1 terms** (Mote 2, Skitter 5, Talon 12, Dart Drone 2, Asteroid Mite 3, Minelayer 26, Gilded Gunship 28, Rail Serpent
83 dismantled tail first, the crate 100, the secondary 100), × the act factor **6.5536**, bounties also × the scale.
A **`bounty_scale` of 0.45** (the earlier acts' model: typical = 0.663 of the kills, half of the secret and the
secondary; a first estimate for part C to confirm with `TypicalHaulTest` and `levelReport`, the Dart count with it)
puts the typical haul at **4,945 (−0.7 %)**, the perfect run at 7,781. No boss or mid-boss, no ground targets.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Mote Swarm 84 × 2 + Skitter 60 × 5 + Talon 49 × 12 + Dart Drone 48 × 2 + Asteroid Mite 32 × 3 + Minelayer 15 × 26 + Gilded Gunship 8 × 28 + Rail Serpent 4 × 83 | 6,470 | 4,290 |
| Ground targets | 0 | 0 |
| Secret: the dead beacon buoy (hidden crate) | 655 | 328 |
| Secondary: the Gunship convoy | 655 | 328 |
| **Total** (bounty scale 0.45) | **7,781** | **4,945** |
| Budget(n) = the typical haul's target; typical −0.7 %, perfect 1.56 × budget | | 4,980 |

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

No art yet. Round 60 proposals (wave 5): the belt mood still for the shipping lane (ART-P-world, in the
[belt](../../../world/belt/README.md#concept-art)) and the unit sheets in their documents (the Minelayer new; the
Gunship r04 and the Rail Serpent r05 chosen earlier). Production backdrop (the lane, its buoys, the belt rock chunks),
the mine frames and the briefing images come in part C (its round 63).

## Implementation

- [ ] Level data `data.yaml` (M9 part C): `ground: none`, sections and speeds with 2 s eases, waves with authored
      gaps, the `debris` block, radio, budget, threat profile, briefing, backdrop, music
- [ ] `NO GROUND`: bomb deliveries idle and drawing power, the HUD's word; the validator rejects ground content here;
      the Airstrike and the EMP Burst work
- [ ] Mine lines as above: clusters of 4 (32 px), 56 px between clusters, one authored 112 px gap; arming, proximity
      blink, the 40 px blast and the ring; the cluster chain; scroll-following lines; Rook passes the gaps
- [ ] The Gunship convoy secondary (`escapes: gilded-gunship`, `allowed: 0`, the three convoy Gunships only, `CONVOY`
      counter; easy `allowed: 1`)
- [ ] Wave script: ≥ 70 enemies a minute at medium with the launched Darts (`DensityTest`), the Act 2+ floor
      (`PacingTest`, `ActFloorTest`), non-front ≤ 35 % of the waves with 3 s edge warnings (`Level30Test`), the quiet
      window t=38–42
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel30Test`, with Rook and without); Rook's barks
      `requires: escort`
- [ ] Threat profile with the `NO GROUND` row, Varga's four intel lines and Rook's teaser fitting the intel panel
      (`IntelPanelLayoutTest`); the new units' portraits
- [ ] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(30) = 4 980 with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music and ambience: "Hollow Rock" by section, the `belt-open` bed, the mine and unit sounds (`Level30SoundsTest`)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot (a mine-line play: the gap, shooting a cluster, the
      rear layers) flies the level with Rook on every difficulty and with `--escort none`
- [ ] Production art and captures for part C's round

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
- 2026-10-27 (Act 5 design phase, step `m9-d-levels-1`; **pending the user's review in round 60**): promoted from the act
  roster to a draft level document, hand tables only, no `data.yaml`. Space only (`ground: none`, J3), the Airstrike
  allowed; the mine lines by C1 a (clusters of 4, an authored 112 px gap) and C2 a (the 40 px blast); fifteen lines from
  eight Minelayer waves, all from behind; the convoy secondary = all three convoy Gunships (easy 2 of 3); ≈ 101 enemies a
  minute with ≈ 48 launched Darts (J4), non-front 23 % of the waves; `bounty_scale` 0.45; mines pay nothing (the
  Minelayer draft's 0, not the gap check's trivia of 1 credit); Rook back in the slot, his only word on the captivity at
  t=160.
