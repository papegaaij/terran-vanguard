---
title: Level 44 – Crossing
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/space/rift-skater, ../../../enemies/air/coilwyrm, ../../../enemies/air/mantis, ../../../enemies/air/lamprey, ../../../enemies/air/harrow, ../../../enemies/README.md, ../../../world/vrell-space, ../../../player/generator, ../../../player/wingmen, ../../../ui/hud]
updated: 2026-11-05
---

# Level 44 – Crossing

## Summary

**Inside the gate.** Aegis Wing scouts ahead of the fleet through the event plane, where space itself moves:
**distorted space** turns the scroll up to 25° sideways and twice runs it backwards, each reversal told 2 s ahead by
Rook and the HUD's chevron. The new [Rift Skater](../../../enemies/space/rift-skater/README.md)s ride the current
and jump along it, and three returning [Coilwyrm](../../../enemies/air/coilwyrm/README.md) chains swirl with it. Waves
come from wherever the current comes from. The secondary is a 65 % kill ratio: every enemy left meets the fleet
behind. Space only (`NO GROUND`), all directions. 264 s, ≈ 105 enemies a minute at medium.

**Status: approved** (Act 7 design phase, step `m11-d-levels-1`, 2026-11-04; the user approved it on 2026-11-05, round 84).
Written from the act roster, the gap analysis's recommended answers (**Q1 a**, Q11 a's far side; J3, J12) and the
earlier steps' drafts, the source of truth for their numbers: the enemies'
[distorted space](../../../enemies/README.md#distorted-space) and
[`teleport`](../../../enemies/README.md#movement-pattern-vocabulary) rules, the
[Rift Skater](../../../enemies/space/rift-skater/README.md), the Coilwyrm's
[Act 7 note](../../../enemies/air/coilwyrm/README.md#act-7-crossing-draft), [Vrell space](../../../world/vrell-space/README.md)'s
event plane and its distorted-space backdrop, the [Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft),
[Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft) and the HUD's
[reversal chevron](../../../ui/hud/README.md#act-7-additions-draft). There is **no `data.yaml`**: the build turns the
hand tables into one; no number is measured.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft):

> **Commander Okafor:** "We are inside the gate, and the crossing takes minutes, not seconds. Aegis Wing scouts
> ahead of the fleet to the far side. Kill what you can, Lancer: everything you leave meets the fleet behind you. Our
> signal will be bad in there. Keep Rook close."
>
> **Dr. Varga:** "Rift Skaters: small, fast, and they jump. A violet ring shows where one lands, half a second
> ahead, and it fires the moment it does. In there the space drifts sideways, and twice it will run backwards. Rook
> will call it, and your HUD shows a chevron. Your controls never turn. Only the world does. And the wardens' eyes
> still ring: we set one in a gun. The Choir Resonator, in your shop."

*Images* (production art in M11's build), briefs only now:

- Okafor's page `level-44-the-crossing`: the event plane as the *Steadfast*'s instruments draw it, a pale violet
  field with the drift as curving arrows, two reversal marks (doubled chevrons) on the wing's track, the fleet's
  column behind it.
- Varga's page `level-44-rift-skater`: her scan of a Skater in grey outline, its violet arrival ring drawn where it
  will land, "0.5 s" by the ring and three dots for its burst.

*Hangar teaser* (shop screen before L44; Rook speaks, not voiced):

> **Rook:** "In there, behind you is just another way in. Bring a tail gun."

The hangar before L44 adds the [Choir Resonator](../../../player/weapons/choir-resonator/README.md) to the shop
(Act 7's one new weapon, round 84 E.2 b; captured from the Gate Wardens' eyes after L43, approved in round 85): a front
gun whose widening wave covers this level's `spread`. The plan keeps the Ion Beam (round 85 Q.1 a),
buys the [Swarm Tail](../../../player/weapons/swarm-tail/README.md) **L4** (the reversals' rear waves) and
flies 24 of 25 MW ([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The intel panel
warns `NO GROUND` for the Bomb Rack and the Mortar (Rook flies his Autocannon here) and `SPECIAL LIMITED` (`NO COVER`)
as from L43.

## Threat profile

Hand-written (draft); directions are counted by the edge each wave enters by (the HUD's rule: a wave spawned in a
reversal enters by travel at the bottom edge and counts as rear).

| Field | Value |
|---|---|
| Dominant layers | `space` (Rift Skater streams, Coilwyrm chains, Lamprey streams, Mantis snipers at the upwind edge, Harrow streams, Vrell swarms); **no ground** |
| Attack directions | front 81 % · sides 16 % (upwind edge) · rear 3 % (the two reversals) |
| Density | 4 |
| Recommended traits | `spread`, `rear` |
| Hazards | **Distorted space**: the drift (up to ±25°) and two reversals, each told 2 s ahead; the Mantis's sweeps; the Harrows' burst rings; the Lampreys' latch |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: inside the gate, `space`, all directions · **L1**: + density 4, the hazards "distorted space" and "2 reversals", the objective `CROSS THE GATE`, the secondary `KILLS 0 / 300`, the rows `NO GROUND` and `SPECIAL LIMITED` (`NO COVER`) · **L2**: + Rift Skater (new), Coilwyrm, Mantis, Lamprey, Harrow, Stinger, Skitter and Mote portraits · **L3**: + `spread` and `rear` highlighted, the wave strip marking the two reversals and the three chains |

*Varga's intel line* per sensor-suite level (must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Inside the gate, Lancer. Nothing holds still in there." |
| L1 | "Every way at once. Space drifts, and twice it runs back." |
| L2 | "New: Rift Skaters, they jump. Coilwyrms ride the current." |
| L3 | "Spread covers a landing ring. Keep a gun facing back." |

**Trait coverage** (`TraitCoverageTest`, the lesson): `spread` and `rear` are covered by one loadout from the shop
(the Plasma Arc, the Scatter Vulcan or, from this visit, the
[Choir Resonator](../../../player/weapons/choir-resonator/README.md) on the front with any rear weapon, or the Fan
Blaster alone, `rear` and `spread`), within the 20 MW the plan's Mk V+ leaves the weapons. The plan's own fit covers `rear` (the Swarm Tail L4)
and not `spread` (the beam): the hangar warns it, as Level 40 (`spread`, `rear`) warned the Act 6 plan; the beam held
on a landing ring and the homing pod answer the Skaters too.

## Objective

- **Primary** `reach-end`: cross the gate (`CROSS THE GATE`); the level ends where the event plane parts on Vrell
  space (Level 45's start).
- **Secondary** `kill-ratio`: **65 %** of the level's enemies (the campaign's rule of thumb), **300 of 461** at
  medium, judged at the level's end (the campaign's rule: its credits and line come on completion, before the
  level-end line). The HUD counts to its target: `KILLS n / 300`, green `DONE` shown at the level's end. The story
  reason is the briefing's (what the wing leaves meets the fleet). Pays **100** in Act 1 terms (**1,678**); the
  typical haul counts 50 %. Easy and hard: 65 % of their own counts.
- **No ground** (`ground: none`, the world's): the Bomb Rack and every bomb delivery read `NO GROUND` and stay idle
  while drawing their power (Level 30's rule, built). `NO COVER` as in all of Act 7.

## Layout

Scroll **130 px/s** along the scroll's vector (the [distorted space](../../../enemies/README.md#distorted-space)'s
speed s), **110 px/s** in the exit (section 5), eased over 2 s. The setting is
[Vrell space](../../../world/vrell-space/README.md#sub-locations-per-level)'s inside of the gate, the **event plane**,
in Q10 b's **L44 light** ([moods](../../../world/vrell-space/README.md#light-per-level-q10-b)): the palette at its lightest, pale
violet-white to mint with dark violet streaks, the act's one light field; the brightest band below a bullet core's
brightness, the bullets' dark outline and the ship's dark hull carrying the read. No surface and no stars; every
layer slides along the scroll's vector at its parallax factor (`deep` least) and the plane warps slowly and smoothly.
Medium: **vacuum**. The level opens in the transit's violet, the warp unbending over 1.0 s (t=0–1).

| Section | t (s) | Scroll (px, approx., along the vector) | Drift | Layers and content | Purpose |
|---|---|---|---|---|---|
| 1. The Far Mouth | 0–40 | 0–5,200 | calm (0°) | `deep`: the event plane's broad violet folds; `high-air`: thin dark streaks | The warp unbends; popcorn; the Rift Skater's intro (t=20, quiet window t=18–26) |
| 2. The Current | 40–106 | 5,200–12,500 | 0° → **+25°** (t=40–50), held to t=78, swing to **−20°** (t=78–88), back to 0° (t=88–94); **reversal 1** (tell t=98, reversed t=101–105) | `deep`: the folds lean with the current; `low-air`: dark ribbons crossing the field | The drift's lesson: waves upwind (the left edge, then the right); the first chain (t=70); the first rear wave (t=102) |
| 3. The Shoal | 106–177 | 12,500–20,300 | 0° → **−25°** (t=106–116), swaying −25° ↔ −10° (≤ 5°/s) to t=150, back to 0° (t=150–160); **reversal 2** (tell t=168, reversed t=171–176) | `deep`: the plane's streaks run across the screen; `low-air`: the gold shard (the secret, t≈150) | Skater streams from the right edge; the second chain (t=140); the second rear wave (t=172) |
| 4. The Narrows | 177–255 | 20,300–30,400 | 0° → **+25°** (t=180–185), held to t=200; **+25° → −25°** (t=200–205, 10°/s); held to t=220; **−25° → +25°** (t=220–225); held to t=245; eased to 0° by t=255 | `deep`: the folds close in from both sides, the plane at its brightest in the middle; `high-air`: fast streaks | The hardest swings; the third chain (t=210); the last wave at t=250 |
| 5. Out of the Gate | 255–264 | 30,400–31,400 | calm (0°) | `deep`: the event plane parts like a curtain on the violet and teal nebula of Vrell space, a hive reef far ahead (Level 45) | ≥ 4 s of calm (the last wave gone by ≈ t=258); level end at t=264 |

**The drift's profile** (Q1 a; the vector's limits and the rules for the ship, the units, the bullets and the
pickups are the enemies' [distorted space](../../../enemies/README.md#distorted-space)): |θ| ≤ 25°, turning at
≤ 10°/s (only t=200–205 and t=220–225 reach it); the sideways drift at most 0.42 s ≈ 55 px/s. θ > 0 slides the space
to the right (the current comes from the left). **Reversals**: two, at t=100 and t=170 (70 s apart; at most 2 a
minute, ≥ 20 s apart), each with the drift eased to 0° first, the vector flipping through zero over 1 s, running
reversed **4 s** (reversal 1) and **5 s** (reversal 2), and easing back over 1 s; each told **2 s ahead** (t=98, t=168)
by Rook's call and the HUD's double chevron. The ship's controls never turn and the drift never pushes the ship; the
screen never rotates.

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

- **HP** from the proposed Act 7 reference DPS (J11: **176** at L44): the Rift Skater is new here (**32** with the
  beam era, [its stat block](../../../enemies/space/rift-skater/README.md#stat-block)); the Coilwyrm takes its
  [Act 7 note](../../../enemies/air/coilwyrm/README.md#act-7-crossing-draft)'s act factor (whole **337**, head 135);
  the returning `medium` Mantis the act factor ×176/52 (26 → **88**); the `small` Lamprey, Harrow and Stinger fly the
  beam era as built; Skitters and Motes are `tiny`. No hex shields (J4).
- **Upwind** (the enemies' rule): a wave enters from the top or from the side the current comes from; every side
  wave is edge-warned 3 s ahead and enters on the edge the drift's sign gives at its time (θ > 0: left, θ < 0:
  right). A wave **spawned in a reversal** keeps Level 20's flipped frame and enters by travel at the **bottom** edge
  (a rear wave, warned with its arrow and banner); one per reversal, a swarm or a snake, nothing that fires aimed in
  its first second.
- **Around the reversals**: no wave enters during a tell except the reversal's own rear wave; no chain enters within
  10 s before a tell, and a chain's loop-back that falls due in a tell or a reversal waits (its note's rule).
- **Density and pacing**: **461 enemies** (a Coilwyrm chain counts as one) over 264 s, **≈ 105 a minute** at medium
  (the act's minimum 80). A wave every 3 s from t=3 to the last at t=250, gaps of 4–7 s only round the reversals and
  the chains. Waves by direction: front 59 of 73 (81 %), sides 12 (16 %), rear 2 (3 %): the ≥ 60 % front rule holds
  in the drifting frame. The floor comes from the Skaters' bursts, the Stingers' fans, the Mantis's sweeps and the
  Harrows' rings: expected to meet ≤ 3 pauses over 2 s, ≥ 2.5 enemies on the screen and ≥ 10 shots a minute
  (`PacingTest`, `ActFloorTest`), **to be confirmed with `levelReport`** in the build (the reversals' few seconds
  inside it).
- **Spacing** (the Act 5 lesson): the Mantis is the only `medium` hull and the Coilwyrm the only `large` one; **one
  chain on the screen** at medium (its note's rule) and no Mantis while a chain is on the screen (the t=116 and
  t=190 Mantises leave before the t=140 and t=210 chains enter). The Rift Skaters' caps (8 on the screen, 4 rings at
  once, two streams jumping at most: its document's) hold: no two Skater streams enter within 8 s.
- **Calm at the end** (the Act 5 lesson): the last wave (a Skitter snake) enters at t=250 and is gone by ≈ t=258;
  **≥ 6 s** of calm before the level end at t=264.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Out of the unbending warp; the drift calm |
| 6 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 9 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 12 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 15 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | The last wave before the quiet window |
| 20 | 1 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 4 | front | **The Rift Skater's first appearance**: a stream of 4 in calm space, quiet window t=18–26 (Varga's line ahead of it); jumps alternate sides |
| 27 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 30 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 33 | 1 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 5 | front |  |
| 36 | 1 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | front |  |
| 39 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 42 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | The drift starts at t=40 (to +25° by t=50) |
| 45 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | side (left) | Upwind, on the current; edge-warned 3 s ahead |
| 48 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | front | Jumps with the drift |
| 51 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 54 | 2 | single | [Mantis](../../../enemies/air/mantis/README.md) | 1 | side (left) | Holds its station against the current (it banks into it); drops the Shield cell |
| 57 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 60 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front |  |
| 63 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | side (left) | Upwind |
| 66 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 70 | 2 | chain | [Coilwyrm](../../../enemies/air/coilwyrm/README.md) | 1 | front | The first chain, swirling with the drift; no other `medium` or larger hull on the screen while it is; its head drops the Overdrive |
| 74 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 77 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 80 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 5 | front | The drift swings (+25° → −20°, t=78–88) |
| 83 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front |  |
| 86 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | side (right) | Upwind once the drift has turned |
| 89 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 92 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 95 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front | The last wave before reversal 1's tell (t=98) |
| 102 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | rear | Spawned in **reversal 1**: enters by travel at the bottom edge (a rear wave, edge-warned) |
| 107 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 110 | 3 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | front |  |
| 113 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 116 | 3 | single | [Mantis](../../../enemies/air/mantis/README.md) | 1 | side (right) | Upwind; gone before the t=140 chain |
| 119 | 3 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | side (right) |  |
| 122 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 125 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | side (right) |  |
| 128 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 131 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front |  |
| 134 | 3 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | front |  |
| 140 | 3 | chain | [Coilwyrm](../../../enemies/air/coilwyrm/README.md) | 1 | front | The second chain; its tail drops the Armour patch |
| 144 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 147 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 150 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front |  |
| 153 | 3 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | front |  |
| 156 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 159 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 165 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | The last wave before reversal 2's tell (t=168) |
| 172 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | rear | Spawned in **reversal 2**: enters by travel at the bottom edge (a rear wave, edge-warned) |
| 178 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 181 | 4 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | front |  |
| 184 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | side (left) | The narrows: the drift swings hard (±25°) |
| 187 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 190 | 4 | single | [Mantis](../../../enemies/air/mantis/README.md) | 1 | side (left) | Gone before the t=210 chain |
| 193 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 196 | 4 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | side (left) |  |
| 199 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 202 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front |  |
| 205 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 210 | 4 | chain | [Coilwyrm](../../../enemies/air/coilwyrm/README.md) | 1 | front | The third chain (hard: a crossing pair) |
| 214 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 217 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | side (right) |  |
| 220 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 223 | 4 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | front |  |
| 226 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front |  |
| 229 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 232 | 4 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | side (left) |  |
| 235 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 238 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | front |  |
| 241 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 244 | 4 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | front |  |
| 247 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front | Dives past and out |
| 250 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 10 | front | The last wave; gone by ≈ t=258, calm to the exit |

Totals: Skitter 146 · Mote Swarm 144 · Rift Skater 80 · Lamprey 33 · Stinger 28 · Harrow 24 · Mantis 3 · Coilwyrm 3
(= 461).

## Ground targets

None: the level has no ground layer (`ground: none`, the world's).

## Hazards

- **Distorted space** (Q1 a): the profile in *Layout*; the rules are the enemies'. What it does to play: streams bend
  with the current, rings and spirals slide across the field as a whole, aimed shots still cross the ship's position,
  a `laser-line` (the Mantis's sweep) is locked on the screen; a unit the drift carries out through a side edge has
  left (no bounty, not a kill for the ratio's count of kills, still counted in the level's enemies).
- **The reversals**: told 2 s ahead (Rook, the chevron pulsing at 2 Hz at the top and bottom edges); during the run
  the backdrop's layers ease to a stop and run back together, the pickups follow the scroll's sign (Level 20's rule),
  and the one rear wave comes up from the bottom edge.
- **The Mantis's sweeps** (the unit's, as built): it holds its station at the upwind edge against the current and
  sweeps its short laser across the lower screen.
- **The Harrows' rings** and **the Lampreys' latch** (the units', as built).

## Secrets and pickups

- **The gold shard** (t≈150, entering at the right edge's top third on the −25° current and drifting across): a
  plate of gold Ascendancy hull, torn from Vorne's lifeboat as it was burned for the gate (Act 6's outro), a small
  breakable object on the play plane (20 HP); broken, it frees a hidden crate worth **100** in Act 1 terms (1,678).
  The Salvage scanner's glint marks it. Varga's event line.
- **Shield cell**: dropped by the t=54 Mantis.
- **Overdrive**: dropped by the t=70 chain's head.
- **Armour patch**: dropped by the t=140 chain's tail (no supply drone reaches into the gate).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), drafted for the **1 s rule** (≈ 12 characters a second, a
1.5 s lead, 0.4 s of silence; event lines drop after 6 s); `RadioTimelineLevel44Test` checks them with Rook and
without. **The radio's noise** (J3): step **2**, the subtitles clean, the Choir's channel clean (it is close).
**Speakers**: Okafor (from the *Steadfast*, behind in the gate), Varga, Rook, the Choir once. Rook's **reversal calls**
are scripted (t=98, t=168; they play without Rook too, as the call that tells the reversal: the
[HUD](../../../ui/hud/README.md#act-7-additions-draft)'s chevron and Rook's call are the two tells), as is his t=28 line;
his barks carry `requires: escort`. **Vorne is silent.**

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, Steadfast. We're in the ring behind you. Nothing in here is where it should be." |
| t=11 | Varga | "Everything drifts in here but you. Fly your ship, not the stars." |
| t=19 | Varga | "A violet ring is where a Skater lands. Shoot the ring." |
| t=28 | Rook | "Lancer, I think the stars are seasick. I know I am." |
| t=37 | Varga | "The current's turning. Waves ride in on it from upwind. Watch that edge." |
| The first Rift Skater killed within 0.5 s of landing (event, bark, `requires: escort`) | Rook | "Caught one landing. Shoot the ring, eh?" |
| t=66 | Okafor | "Coilwyrm in the current. Go for the head." |
| t=98 (reversal 1's tell) | Rook | "It's flipping! Eyes behind!" |
| t=112 (enemy channel) | the Choir | "*…the door is open… every light that crosses… it sees…*" |
| t=120 | Varga | "Did you hear that? I have never heard the Choir afraid." |
| t=148 | Okafor | "Aegis, your signal's breaking up. Stay on the current. We'll find you." |
| The gold shard broken (event) | Varga | "Gold plating. Ascendancy work. Vorne came through here." |
| t=168 (reversal 2's tell) | Rook | "Flipping again! Behind us!" |
| t=180 | Varga | "The narrows. The current swings hard from here. Read the stars." |
| The first chain's head destroyed (event, bark, `requires: escort`) | Rook | "Wyrm's down. Just like old times." |
| t=252 | Okafor | "Light ahead, Aegis. The far side. Hold there for the fleet." |
| Secondary met (level end, before the level-end line) | Okafor | "Nothing followed us through. Clean work, Aegis." |
| Level end | Okafor | "Steadfast is through. The fleet is gathering behind you." |

Timeline check (text): the timed lines close at ≈ 9.7, 17.8, 25.0, 33.8, 44.5, 70.9, 101.8, 117.8, 126.1, 155.3,
171.7, 186.8 and 258.4; nothing overlaps. Each reversal call starts at its tell and closes in the reversal's
first second of running back. The Choir's line is fearful and names nothing (the [Silence's rule](../../../story/README.md)); Varga hears the
fear (her line that the Choir sings *away* from something stays for a later level, D-story's). "The door" ties back to
Level 43's line.

## Music & ambience

The Act 7 B theme (**track 17**, new, J8; the cues are D-audio's
[music](../../../audio/music/README.md#cues-per-level-act-7)): the intro and the base over the far mouth, the full mix
from the current to the end, the base again out of the gate. A reversal ducks the music 3 dB for its run under the
reversal groan. Ambience: the **`vrell-gate`** bed with the **`event-plane` layer** over it for the whole level
([sfx](../../../audio/sfx/README.md#ambience-per-setting)): a slow, deep groan of stressed space under a faint high
shimmer. New sounds this level needs: the Rift Skater's (its document's brief), the reversal's tell (a soft rising
two-tone with the chevron, a UI sound, synth allowed), the reversal's run (the layer's groan bending down and back),
the Coilwyrm's as built; no bubbly or popping sounds.

## Credit budget

Budget(44) = 700 × 1.07⁴³ ≈ **12,841** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in
**Act 1 terms** (Skitter 5, Mote 2, Rift Skater 14, Lamprey 12, Stinger 15, Harrow 12, Mantis 30, Coilwyrm 86, the
crate 100, the secondary 100), × the act factor **16.777**, bounties also × the scale. A **`bounty_scale` of 0.31**
(air kills 60 %, the secret and the secondary 50 %; a first estimate for the build) puts the typical haul at
**12,880 (+0.3 %)**, the perfect run at 22,027 (1.72 × budget). No ground targets, no boss.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 146 × 5 + Mote Swarm 144 × 2 + Rift Skater 80 × 14 + Lamprey 33 × 12 + Stinger 28 × 15 + Harrow 24 × 12 + Mantis 3 × 30 + Coilwyrm 3 × 86 (3,590) | 18,671 | 11,203 |
| Secret: the gold shard (hidden crate) | 1,678 | 839 |
| Secondary: the kill ratio | 1,678 | 839 |
| **Total** (bounty scale 0.31) | **22,027** | **12,880** |
| Budget(n) = the typical haul's target; typical +0.3 %, perfect 1.72 × budget | | 12,841 |

**Balance plan**: the L44 visit buys the Swarm Tail L4 (rear 42; forward / reference **1.07**, the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)); 24 of 25 MW. A unit the drift carries
out pays nothing, so the haul leans on the 60 % share; the build's measure with the autopilot in the drift sets the
scale.

## Difficulty notes

- **Easy**: the gentler profile (the world's hazard row: |θ| ≤ **15°**, the swings at half rate; at most one reversal
  a minute, both reversals kept, each **3 s**); the Rift Skater's easy hooks (ring 0.7 s, burst of 2, 2 jumps); the
  formation lever's smaller waves; the Coilwyrm one chain.
- **Hard**: the drift as medium (the world's rule); the Rift Skater's hard hooks (burst 0.20 s after landing, 4
  jumps); the t=210 chain flies as the Coilwyrm's **crossing pair** (its hard hook, the two paths authored so the
  heads never overlap, no Mantis on the screen); the other waves by the formation lever, trimmed by hand to the
  Skaters' caps.

## Concept art

No art yet. Round 84's picks (decided 2026-11-05): the Rift Skater's sheets with its warp-in ring in its document (ART-R-A, over this
level's brightest and darkest band); Q10 b's one-palette sheet (the light of this level) in
[Vrell space](../../../world/vrell-space/README.md) (ART-R-B). Production backdrop (the event plane's sliding layers
and slow warp, the exit's parting curtain), the briefing images and the gold shard come in M11's build.

## Implementation

- [ ] Level data `data.yaml` (M11's build): `ground: none`, `medium: vacuum`, the `beyond-gate` limits, sections and
      speeds, the **drift profile** (θ over time, the two reversals and their lengths), waves with their entry edges,
      radio, budget, threat profile, briefing, backdrop, music
- [ ] Distorted space as the enemies' rules (the drift's lateral component on the units, the bullets and the
      pickups; Level 20's flipped frame for a reversal; the ship screen-fixed; aimed shots aimed on the screen;
      `laser-line`s locked to the screen; a unit carried out through a side edge has left); deterministic and
      allocation-free
- [ ] The reversal tells: Rook's scripted call and the HUD's double chevron 2 s ahead; the dim chevron while reversed;
      the rear wave's edge warning from its entry edge
- [ ] The backdrop's layers sliding along the vector at their parallax factors (`deep` least), the slow warp, the
      exit's parting curtain; the level opening in the transit's violet (the warp unbending over 1.0 s)
- [ ] The kill-ratio secondary (`KILLS n / 300`, 65 %, judged at the level end)
- [ ] Wave script: ≥ 80 enemies a minute at medium (`DensityTest`), the Act 2+ floor (`PacingTest`, `ActFloorTest`),
      side waves upwind and edge-warned 3 s, ≥ 60 % front by entry edge, the quiet window t=18–26, ≥ 4 s calm after
      the last enemy, one chain on the screen, no Mantis with a chain, the Skaters' caps
- [ ] Radio voiced with the step-2 noise, every timed line at most 1 s late (`RadioTimelineLevel44Test`, with Rook and
      without); the reversal calls scripted, Rook's barks `requires: escort`
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`); the
      Rift Skater's portrait; `TraitCoverageTest` covering Level 44
- [ ] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(44) = 12 841 with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music and ambience: track 17, the `vrell-gate` bed with the `event-plane` layer, the level's sounds (`Level44SoundsTest`)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot (reading the drift, a landing ring, a rear wave)
      flies the level with Rook on every difficulty and with `--escort none`
- [ ] Production art and captures for the build's round

## Open questions

- **The traits**: drafted `spread` (the sketch's, the Skater's landing rings) and `rear` (the reversals, the plan's
  Swarm Tail L4); the plan's beam does not cover `spread`, so the hangar warns the typical player (as at Level 40).
  The alternative is `homing` and `rear`, which the plan's fit covers.
- **Length**: 264 s (the act's "near 5 minutes"; the reversals and the drift make every second count double); the
  build may stretch section 4 if the pacing allows.

## Decisions

- 2026-11-04 (Act 7 design phase, step `m11-d-levels-1`; decided 2026-11-05, round 84): promoted from the
  act roster to a draft level document, hand tables only, no `data.yaml`. Space only (`NO GROUND`), all directions by
  entry edge (front 81 %); the drift profile by Q1 a (|θ| ≤ 25°, ≤ 10°/s, two reversals of 4 s and 5 s, 70 s apart,
  each told 2 s ahead by Rook and the chevron, the drift eased to 0° before each); five sections, 264 s, 461 enemies
  (≈ 105 a minute); the Rift Skater's intro stream in calm space at t=20; three Coilwyrm chains (t=70, 140, 210), one
  on the screen; side waves upwind; one rear wave per reversal; the kill-ratio secondary 65 % (`KILLS n / 300`) with
  the briefing's reason; the gold shard of Vorne's lifeboat as the secret; the Choir's fearful line and Varga hearing
  the fear; the radio's noise step 2; the level opening in Level 43's transit violet and ending as the event plane
  parts on Vrell space; traits `spread`, `rear`; `bounty_scale` 0.31; Vorne silent.
- 2026-11-04 (step `m11-d-act`, decided 2026-11-05, round 84): the music and bed as D-audio's cues: track 17, the `vrell-gate`
  bed with the `event-plane` layer (the proposed `vrell-event-plane` key folded into it).
- 2026-11-05, round 84 (the user's page answers; decided as drafted unless stated, the alternatives rejected). The open items were:
  - **Q1** (round 84, what distorted space does to play): drafted **a** (the scroll's direction turns up to ±25° and
    reverses twice, units and bullets in the drifting frame, the controls never rotating); **b** visual only (the layers
    slide, the play stays a straight scroll); **c** reversals only (Level 20's rule twice). The
    [enemies' open questions](../../../enemies/README.md#open-questions) hold it.
- 2026-11-05 (step RES, round 85; approved by the user, round 85 Q.2): the hangar before L44 adds the
  [Choir Resonator](../../../player/weapons/choir-resonator/README.md) (round 84 E.2 b) and Varga's page names it in
  one line ("the wardens' eyes still ring"; the fit is `BriefingLayoutTest`'s in the build); it can cover the level's
  `spread`; the plan keeps the beam (round 85 Q.1 a, decided 2026-11-05).
