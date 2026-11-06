---
title: Level 44 – Crossing
design: approved
implementation: done
art: chosen
depends-on: [../../../enemies/space/rift-skater, ../../../enemies/air/coilwyrm, ../../../enemies/air/mantis, ../../../enemies/air/lamprey, ../../../enemies/air/harrow, ../../../enemies/README.md, ../../../world/vrell-space, ../../../player/generator, ../../../player/wingmen, ../../../ui/hud]
updated: 2026-11-06
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

<!-- data: briefing -->
> **Commander Okafor:** "We are inside the gate, and the crossing takes minutes, not seconds.
> Aegis Wing scouts ahead of the fleet to the far side. Kill what you can, Lancer: everything
> you leave meets the fleet behind you. Our signal will be bad in there. Keep Rook close."
>
> **Dr. Varga:** "Rift Skaters jump: a violet ring marks the landing half a second ahead, and
> they fire as they land. Space drifts in there, and twice it runs backwards: Rook calls it, the
> HUD shows a chevron. Your controls never turn. In your shop: the Choir Resonator."
<!-- /data -->

*Images* (production art in M11's build), briefs only now:

- Okafor's page `level-44-the-crossing`: the event plane as the *Steadfast*'s instruments draw it, a pale violet
  field with the drift as curving arrows, two reversal marks (doubled chevrons) on the wing's track, the fleet's
  column behind it.
- Varga's page `level-44-rift-skater`: her scan of a Skater in grey outline, its violet arrival ring drawn where it
  will land, "0.5 s" by the ring and three dots for its burst.

*Hangar teaser* (shop screen before L44; Rook speaks, not voiced):

<!-- data: teaser -->
> **Rook:** "In there, behind you is just another way in. Bring a tail gun."
<!-- /data -->

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

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `space` (Rift Skater streams, Coilwyrm chains, Lamprey streams, Mantis snipers at the upwind edge, Choir Heralds ringing their Needlers, Chimera and Needler wings, Stinger columns, Harrow streams, Vrell swarms); **no ground** |
| Attack directions | front 91% · sides 9% by the waves' data (the README's count by the edge each wave enters by: front 87 % · sides 11 % · rear 2 % at medium, a wave spawned in a reversal entering by travel at the bottom edge) |
| Density | 4 |
| Recommended traits | `spread`, `rear` |
| Hazards | **Distorted space**: the drift (up to ±25°) and two reversals, each told 2 s ahead; the Mantis's sweeps; the Heralds' shield rings and song; the Harrows' burst rings; the Lampreys' latch |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: inside the gate, `space`, all directions · **L1**: + density 4, the hazards "distorted space" and "2 reversals", the objective `CROSS THE GATE`, the secondary `KILLS 0 / 422`, the rows `NO GROUND` and `SPECIAL LIMITED` (`NO COVER`) · **L2**: + Rift Skater (new), Coilwyrm, Choir Herald, Mantis, Chimera, Lamprey, Harrow, Needler, Stinger, Skitter and Mote portraits · **L3**: + `spread` and `rear` highlighted, the wave strip marking the two reversals and the three chains |
<!-- /data -->

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
- **Secondary** `kill-ratio`: **65 %** of the level's enemies (the campaign's rule of thumb), **422 of 648** at
  medium (step B3b's count with the denser mixed waves; easy 324 of 497, hard 520 of 800), judged at the level's end (the campaign's rule: its credits and line come on completion, before the
  level-end line). The HUD counts to its target: `KILLS n / 422`, green `DONE` shown at the level's end. The story
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

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Far Mouth | 0–40 | 0 → 5,200 | +130 | clear | `deep`: the event plane's broad violet folds. `high-air`: thin dark streaks. | Drift: calm (0°). The warp unbends; popcorn; the Rift Skater's intro (t=20, quiet window t=18–26). |
| 2. The Current | 40–100 | 5,200 → 13,000 | +130 | clear | `deep`: the folds lean with the current. `low-air`: dark ribbons crossing the field. | Drift: 0° → **+25°** (t=40–50), held to t=78, swing to **−20°** (t=78–88), back to 0° (t=88–94); reversal 1 told at t=98. The drift's lesson: waves upwind (the left edge, then the right); the first chain (t=70). |
| 3. Reversal 1 | 100–105 | 13,000 → 12,480 | −130 | clear | `deep`: the layers ease to a stop and run back together. | Drift: **reversed** t=101–105 (4 s; easy 3 s). The first rear wave (t=102), up from the bottom edge. |
| 4. The Current, on | 105–106 | 12,480 → 12,480 | +130 | clear | `deep`: the layers ease forward again. | Drift: calm (0°). The scroll forward again. |
| 5. The Shoal | 106–170 | 12,480 → 20,800 | +130 | clear | `deep`: the plane's streaks run across the screen. `low-air`: the gold shard (the secret, t≈150). | Drift: 0° → **−25°** (t=106–116), swaying −25° ↔ −10° (≤ 5°/s) to t=150, back to 0° (t=150–160); reversal 2 told at t=168. Skater streams from the right edge; the second chain (t=140). |
| 6. Reversal 2 | 170–176 | 20,800 → 20,150 | −130 | clear | `deep`: the layers ease to a stop and run back together. | Drift: **reversed** t=171–176 (5 s; easy 3 s). The second rear wave (t=172), up from the bottom edge. |
| 7. The Shoal, on | 176–177 | 20,150 → 20,150 | +130 | clear | `deep`: the layers ease forward again. | Drift: calm (0°). The scroll forward again. |
| 8. The Narrows | 177–255 | 20,150 → 30,290 | +130 | clear | `deep`: the folds close in from both sides, the plane at its brightest in the middle. `high-air`: fast streaks. | Drift: 0° → **+25°** (t=180–185), held to t=200; **+25° → −25°** (t=200–205, 10°/s); held to t=220; **−25° → +25°** (t=220–225); held to t=245; eased to 0° by t=255. The hardest swings; the third chain (t=210); the last wave at t=250. |
| 9. Out of the Gate | 255–264 | 30,290 → 31,300 | +110 | clear | `deep`: the event plane parts like a curtain on the violet and teal nebula of Vrell space, a hive reef far ahead (Level 45). | Drift: calm (0°). ≥ 4 s of calm (the last wave gone by ≈ t=258); level end at t=264. |
<!-- /data -->

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

- **HP** from the proposed Act 7 reference DPS (J11: **176** at L44): the Rift Skater is new here (**40** with the
  beam era's ×2.5 from Level 43, step B3's draft, [its stat block](../../../enemies/space/rift-skater/README.md#stat-block)); the Coilwyrm takes its
  [Act 7 note](../../../enemies/air/coilwyrm/README.md#act-7-crossing-draft)'s act factor (whole **337**, head 135);
  the returning `medium` Mantis the act factor ×176/52 (26 → **88**); the `small` Lamprey, Harrow and Stinger fly the
  beam era as built; Skitters and Motes are `tiny`. No hex shields (J4). Step B3b's returning units: the `medium`
  [Choir Herald](../../../enemies/air/choir-herald/README.md) at the act factor (60 → **126**; its aura's ring on its
  four orbiting Needlers is its own, Act 3's, not a hex shield), the `small` Chimera (50) and Needler (30) in the
  beam era.
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
  inside it). **Built** (step B3b, the user's answer "Increase the density, but not just with small enemies. Add
  different types.", 2026-11-05): **648 enemies** at medium (≈ 150 a minute; easy 497, hard 800) in 124 waves: front
  108 (87 %), sides 14 (11 %), rear 2 (2 %); the floor met (see *Measured with the denser mixed waves*).
- **Spacing** (the Act 5 lesson): the Mantis and (step B3b) the Choir Herald are the `medium` hulls and the Coilwyrm
  the only `large` one; **one chain on the screen** at medium (its note's rule) and no Mantis while a chain is on the
  screen (the t=116 and t=190 Mantises leave before the t=140 and t=210 chains enter; step B3b's t=128.5 Mantis before
  the t=140 chain, its t=228 one after the t=210 chain); each Herald holds its 12 s between two chains (t=40.5,
  106.5, 178) in the field's middle, the Mantises at the edges (`HullStackingTest`: no overlap). The Rift Skaters' caps (8 on the screen, 4 rings at
  once, two streams jumping at most: its document's) hold: no two Skater streams enter within 8 s.
- **Calm at the end** (the Act 5 lesson): the last wave (a Skitter snake) enters at t=250 and is gone by ≈ t=258;
  **≥ 6 s** of calm before the level end at t=264.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | Out of the unbending warp; the drift calm |
| 4.5 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | top (ahead) | Step B3b: Chimeras through the gate behind the wing (the mixed density, the user 2026-11-05) |
| 6 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, left) | |
| 7.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 9 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | top (ahead) | |
| 10.5 | 1 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | top (ahead) | Step B3b: a Stinger column |
| 12 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 13.5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | top (ahead) | Step B3b: a Needler wing before the quiet window |
| 15 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, right) | The last wave before the quiet window |
| 16 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 20 | 1 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 4 | top (ahead) | **The Rift Skater's first appearance**: a stream of 4 in calm space, quiet window t=18–26 (Varga's line ahead of it); jumps alternate sides |
| 25 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 27 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 28.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 30 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, left) | |
| 30 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 33 | 1 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 5 | top (ahead) | |
| 36 | 1 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | top (ahead) | |
| 37.5 | 1 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | top (ahead) | Step B3b: a Stinger column |
| 39 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 40.5 | 2 | carrier + escorts | [Choir Herald](../../../enemies/air/choir-herald/README.md) + [Needler](../../../enemies/air/needler/README.md) | 1 + 4 | top (ahead) | Step B3b: a Choir Herald with four Needlers ringed, as the drift starts; gone before the first chain |
| 41 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 42 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, right) | The drift starts at t=40 (to +25° by t=50) |
| 45 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | left side | Upwind, on the current; edge-warned 3 s ahead |
| 48 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | top (ahead) | Jumps with the drift |
| 51 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 52.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | top (ahead) | Step B3b: a Needler wing |
| 54 | 2 | single | [Mantis](../../../enemies/air/mantis/README.md) | 1 | left side | Holds its station against the current (it banks into it); drops the Shield cell |
| 56 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 57 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, left) | |
| 57 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 58.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | top (ahead) | Step B3b: a Chimera wing |
| 60 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | top (ahead, alternating edges) | |
| 63 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | left side | Upwind |
| 64.5 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | top (ahead) | Step B3b: a Stinger column |
| 66 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 68.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 70 | 2 | snake | [Coilwyrm](../../../enemies/air/coilwyrm/README.md) | 1 | top (ahead) | The first chain, swirling with the drift; no other `medium` or larger hull on the screen while it is; its head drops the Overdrive |
| 72 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | top (ahead) | Step B3b: a Needler wing beside the chain |
| 74 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, right) | |
| 76 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | top (ahead) | Step B3b: a Chimera wing |
| 77 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 80 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 5 | top (ahead) | The drift swings (+25° → −20°, t=78–88) |
| 83 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | top (ahead) | |
| 84.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | top (ahead) | Step B3b: a Chimera wing in the swing |
| 86 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | right side | Upwind once the drift has turned |
| 87.5 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | top (ahead) | Step B3b: a Stinger column |
| 89 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 90.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 92 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, left) | |
| 92.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 95 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | top (ahead, alternating edges) | The last wave before reversal 1's tell (t=98) |
| 102 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | bottom (ahead) | Spawned in **reversal 1**: enters by travel at the bottom edge (a rear wave, edge-warned) |
| 106.5 | 5 | carrier + escorts | [Choir Herald](../../../enemies/air/choir-herald/README.md) + [Needler](../../../enemies/air/needler/README.md) | 1 + 4 | top (ahead) | Step B3b: a Choir Herald with four Needlers ringed, after reversal 1; gone before the second chain |
| 107 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, right) | |
| 110 | 5 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | top (ahead) | |
| 113 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 114.5 | 5 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | top (ahead) | Step B3b: a Stinger column |
| 116 | 5 | single | [Mantis](../../../enemies/air/mantis/README.md) | 1 | right side | Upwind; gone before the t=140 chain |
| 117.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 119 | 5 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | right side | |
| 122 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, left) | |
| 123.5 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | top (ahead) | Step B3b: a Needler wing |
| 125 | 5 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | right side | |
| 128 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 128.5 | 5 | single | [Mantis](../../../enemies/air/mantis/README.md) | 1 | right side | Step B3b: a second Mantis upwind; gone before the t=140 chain |
| 131 | 5 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | top (ahead) | |
| 134 | 5 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | top (ahead) | |
| 137 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | top (ahead) | Step B3b: a Chimera wing |
| 140 | 5 | snake | [Coilwyrm](../../../enemies/air/coilwyrm/README.md) | 1 | top (ahead) | The second chain; `loop` out of the bottom edge, back from the rear 6 s later; its tail drops the Armour patch |
| 142.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 144 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, right) | |
| 145.5 | 5 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | top (ahead) | Step B3b: a Stinger column |
| 147 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 150 | 5 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | top (ahead, alternating edges) | The gold shard drifts in from the right on the −25° current (the secret) |
| 153 | 5 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | top (ahead) | |
| 156 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, left) | |
| 157.5 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | top (ahead) | Step B3b: a Chimera wing |
| 159 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 161 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 162.5 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | top (ahead) | Step B3b: a Needler wing before the tell |
| 165 | 5 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | top (ahead) | The last wave before reversal 2's tell (t=168) |
| 166.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 172 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | bottom (ahead, left) | Spawned in **reversal 2**: enters by travel at the bottom edge (a rear wave, edge-warned) |
| 178 | 8 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 178 | 8 | carrier + escorts | [Choir Herald](../../../enemies/air/choir-herald/README.md) + [Needler](../../../enemies/air/needler/README.md) | 1 + 4 | top (ahead) | Step B3b: a Choir Herald with four Needlers ringed, into the narrows; gone before the third chain |
| 181 | 8 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | top (ahead) | |
| 182.5 | 8 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | top (ahead) | Step B3b: a Stinger column |
| 184 | 8 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | left side | The narrows: the drift swings hard (±25°) |
| 187 | 8 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, right) | |
| 190 | 8 | single | [Mantis](../../../enemies/air/mantis/README.md) | 1 | left side | Gone before the t=210 chain |
| 191.5 | 8 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 193 | 8 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 194.5 | 8 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 196 | 8 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | left side | |
| 197.5 | 8 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | top (ahead) | Step B3b: a Needler wing |
| 199 | 8 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, left) | |
| 199 | 8 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 200.5 | 8 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 202 | 8 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | top (ahead, alternating edges) | |
| 203.5 | 8 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | top (ahead) | Step B3b: a Stinger column |
| 205 | 8 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 207.5 | 8 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 210 | 8 | snake | [Coilwyrm](../../../enemies/air/coilwyrm/README.md) | 1 | top (ahead) | The third chain (hard: the crossing pair instead) |
| 210 | 8 | snake | [Coilwyrm](../../../enemies/air/coilwyrm/README.md) | 2 | top (ahead) | Hard only: *pairs crossing*, the second chain on the mirrored path (the heads never overlap) |
| 212 | 8 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | top (ahead) | Step B3b: a Chimera wing beside the chain |
| 214 | 8 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, right) | |
| 215.5 | 8 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 217 | 8 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | right side | |
| 220 | 8 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 222.5 | 8 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | top (ahead) | Step B3b: a Stinger column |
| 223 | 8 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | top (ahead) | |
| 226 | 8 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | top (ahead) | |
| 228 | 8 | single | [Mantis](../../../enemies/air/mantis/README.md) | 1 | left side | Step B3b: a Mantis upwind once the chain is gone |
| 229 | 8 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, left) | |
| 229.5 | 8 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 232 | 8 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | left side | |
| 235 | 8 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | top (ahead) | |
| 235.5 | 8 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | top (ahead) | Step B3b: a Chimera wing |
| 238 | 8 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | top (ahead, alternating edges) | |
| 241 | 8 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | top (ahead, right) | |
| 242.5 | 8 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | top (ahead) | Step B3b: a Needler wing |
| 244 | 8 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | top (ahead) | |
| 247 | 8 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | top (ahead) | Dives past and out |
| 250 | 8 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 10 | top (ahead, left) | The last wave; gone by ≈ t=258, calm to the exit |

Totals: Mote Swarm 144 · Chimera 36 · Skitter 212 · Stinger 64 · Needler 44 · Rift Skater 80 · Lamprey 33 · Choir Herald 3 · Mantis 5 · Harrow 24 · Coilwyrm 3.
<!-- /data -->

The README's hand table had 461 (Skitter 146); step B2 added the 22 offbeat Skitter trickles (66 Skitters, = 527).

**Measured** (`levelReport -Plevel=44`, step B2, the plan's fit: Level 43's with the Swarm Tail L4, the stand-in
until step B3's `Level44Test`): the README's 73 waves flew **1.25 on the screen**, 6 shots a minute and 22 pauses over
2 s at medium; step B2 added a **Skitter trickle of 3** on 22 offbeats (none in a tell or a reversal) and the
Stingers' `small_act_hp` (36 HP). Now **527 enemies, 122 a minute** at medium (easy 401, hard 648), **1.54 on the
screen**, 5 shots a minute, **4 pauses over 2 s**: all four the reversals' (the tell without a wave to the rear wave,
then the reversed run), 13 s of calm at the end. **The floor is not met** (accepted by the user in round 89): 1.54
on the screen (2.5) and 5 shots a minute (10); a killed small unit lives 0.63× Level 10's time; the drift carries
units out. The autopilot wins 3 of 3 on every difficulty and meets the kill ratio 3 of 3.

**Measured with the draft answer** (step B3, accepted by the user in round 89: the beam era's **×2.5 from Level
43**, a reversal from its tell to the end of its run a **scripted pause** (no gap, outside the floor's means), and the
drift autopilot, `Level44Test`'s `DriftPlay`: the target led by the drift, a landing ring lower than the target waited
under so the beam meets the Skater as it lands): **1.61 on the screen**, **4.5 shots a minute**, **1 pause over 2 s**
at medium (13.0–15.2 s), none at hard; a killed small unit **0.66×** Level 10's time (the Rift Skaters met as they
land, before their burst: 11.2 shots a minute and 0.71× with the generic play). The autopilot wins 3 of 3 on every
difficulty with Rook and with `--escort none`, the kill ratio met on every run (520 of 343 at medium), no chain on the
screen in a reversal, no Skater landing within 50 px of the ship. The haul is unchanged (13,095 at 0.29). **Still
missing the floor**: 1.61 against 2.5 on the screen, 4.5 against 10 shots a minute, the small units' 0.66× against 0.9
(measured, printed, not asserted: `PacingTest.PENDING_WAVES`, `BeamEraHpTest.PENDING_LEAST`).

**Measured with the denser mixed waves** (step B3b, 2026-11-05, the user's answer above; the draft answer kept: the
beam era's ×2.5, the reversal a scripted pause, `DriftPlay`): **29 waves** added, none in a tell or a reversal run,
the side ones upwind and edge-warned: **3 Choir Heralds** each ringing **4 Needlers** in orbit (t=40.5, 106.5, 178;
in at 50 px/s as Level 19's), **8 Chimera wings** (4 or 5), **7 Needler wings** (4 or 5), **9 Stinger columns** of 4
and **2 Mantises** upwind (t=128.5 right, t=228 left); B2's trickles kept: medium 527 → **648** (easy 403 → 497, hard
652 → 800). At medium **2.89 on the screen** (1.61), **19.3 shots a minute** (4.5), **no pause over 2 s** (1), 13.1 s
of calm at the end; easy 1.69 and 9.1, hard 4.52 and 50.3. No big-hull overlap, no chain on the screen in a reversal,
no Skater landing within 50 px of the ship. The autopilot wins 3 of 3 on every difficulty with Rook and with
`--escort none`, the kill ratio met on every run (636 of 422 at medium). The pacing and the floor are asserted again
(out of `PacingTest.PENDING_WAVES`). **Still short**: a killed small unit lives **0.80×** Level 10's time (0.66×; the
band 0.9–2.6): its 80 Rift Skaters (1.2 s, met as they land) and 33 Lampreys (0.6 s, killed in their latch) are the
approved waves' and pull the mean down whatever flies beside them (the Stingers 1.7 s, the ringed Needlers 1.45 s);
`BeamEraHpTest.PENDING_LEAST` keeps Level 44 at 0.75, pending the user's review.

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

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, Steadfast. We're in the ring behind you. Nothing in here is where it should be." |
| t=13 | Varga | "Everything drifts in here but you. Fly your ship, not the stars." |
| t=21 | Varga | "A violet ring is where a Skater lands. Shoot the ring." |
| t=28 | Rook | "Lancer, I think the stars are seasick. I know I am." |
| t=37 | Varga | "The current's turning. Waves ride in on it from upwind. Watch that edge." |
| The first Rift Skater killed within 0.5 s of landing (event, bark, `requires: escort`) | Rook | "Caught one landing. Shoot the ring, eh?" |
| t=66 | Okafor | "Coilwyrm in the current. Go for the head." |
| t=98 (reversal 1's tell) | Rook | "It's flipping! Eyes behind!" |
| t=112 (enemy channel) | The Choir | "…the door is open… every light that crosses… it sees…" |
| t=120 | Varga | "Did you hear that? I have never heard the Choir afraid." |
| t=148 | Okafor | "Aegis, your signal's breaking up. Stay on the current. We'll find you." |
| t=168 (reversal 2's tell) | Rook | "Flipping again! Behind us!" |
| t=180 | Varga | "The narrows. The current swings hard from here. Read the stars." |
| The first chain's head destroyed (event, bark, `requires: escort`) | Rook | "Wyrm's down. Just like old times." |
| t=252 | Okafor | "Light ahead, Aegis. The far side. Hold there for the fleet." |
| Secondary met (level end, before the level-end line) | Okafor | "Nothing followed us through. Clean work, Aegis." |
| Level end | Okafor | "Steadfast is through. The fleet is gathering behind you." |
<!-- /data -->

The gold shard broken (the secret's event line, in the data's `secrets`): Varga, "Gold plating. Ascendancy work. Vorne came through here."

Timeline check (text, step B5): the timed lines close at ≈ 11.9, 20.2, 27.8, 35.0, 47.4, 72.4, 103.9, 118.8, 126.9,
158.4, 173.9, 190.1 and 259.0; each opens at most 0.3 s late (the 1 s rule). Each reversal call starts at its tell and closes in the reversal's
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
**Act 1 terms** (Skitter 5, Mote 2, Rift Skater 14, Lamprey 12, Stinger 15, Harrow 12, Needler 12, Chimera 14,
Mantis 30, Choir Herald 30, Coilwyrm 86, the
crate 100, the secondary 100), × the act factor **16.777**, bounties also × the scale. A **`bounty_scale` of 0.31**
(air kills 60 %, the secret and the secondary 50 %; a first estimate for the build) puts the typical haul at
**12,880 (+0.3 %)**, the perfect run at 22,027 (1.72 × budget). No ground targets, no boss.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Mote Swarm 144 × 2 + Chimera 36 × 14 + Skitter 212 × 5 + Stinger 64 × 15 + Needler 44 × 12 + Rift Skater 80 × 14 + Lamprey 33 × 12 + Choir Herald 3 × 30 + Mantis 5 × 30 + Harrow 24 × 12 + Coilwyrm 3 × 86 | 18,976 | 11,386 |
| Secret: gold shard (hidden crate, 1% of budget) | 1,678 | 839 |
| Secondary: leave nothing to follow the fleet | 1,678 | 839 |
| **Total** (bounty scale 0.2) | **22,332** | **13,064** |
| Budget(n) = the typical haul's target; typical +2 %, perfect 1.74 × budget | | 12,841 |
<!-- /data -->

**Measured** (`levelReport`, step B2, after the offbeat trickles): the typical haul **13,095 (+2.0 %)** at the scale **0.29** (the hand estimate 0.31 before the 66 added Skitters), the perfect run 22,384 (1.74 × budget).
Step B3b (the denser mixed waves, 648 units): the typical haul **13,064 (+1.7 %)** at the scale **0.20**, the perfect
run 22,332 (1.74 × budget); the plan's forward / reference stays 1.07 (`balance.py`).

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

Round 84's picks (decided 2026-11-05): the Rift Skater's sheets with its warp-in ring in its document (ART-R-A, over this
level's brightest and darkest band); Q10 b's one-palette sheet (the light of this level) in
[Vrell space](../../../world/vrell-space/README.md) (ART-R-B).

Round 89 (M11 part B, step ART-B; production art, pending the user's review): the event plane by
`tools/art/backdrop_l44.py` (every tile set wraps in x for the drift's sideways slide; the parting curtain cut from the
Narrows' own rows, so it appears without a pop; the slow warp's UV map `plane-warp` for step B4), the gold shard by
`tools/art/gate_units.py props` (held until step B2's data claims it), the Rift Skater's frames in
[its document](../../../enemies/space/rift-skater/README.md#concept-art), the briefing images in the
[briefing](../../../ui/briefing/README.md#concept-art). Its brightest band measures 221 of 255 (luma), under a bullet core.
Prompts and briefs: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r89-a.png](concept/backdrop-final-r89-a.png) | Production backdrop: the five sections' screens at half size with stand-ins (Rift Skaters and a ring, the ship), the curtain parting on Vrell space with the far reef, the deep tiles twice side by side (the x wrap), the pieces and the warp map (sheet) | chosen (round 89 F.3/F.4 accept) |
| [concept/backdrop-final-r89-a.gif](concept/backdrop-final-r89-a.gif) | A drift swing in the Shoal: the layers sliding sideways at their factors, at half size | chosen (round 89 F.3/F.4 accept) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (the deep tile sets cut at the sections' rows, the low- and high-air streak tiles, the curtain, the nebula behind it and the reef with their paths) and the sections, for step B2 (not data) | chosen (round 89 F.4 accept) |
| [concept/l44-props-final-r89-a.png](concept/l44-props-final-r89-a.png) | The gold shard tumbling and shot apart, at 2× (sheet) | chosen (round 89 F.5 accept) |
| [concept/l44-props-final-r89-a.gif](concept/l44-props-final-r89-a.gif) | The shard drifting and breaking | chosen (round 89 F.5 accept) |
| [concept/chain-capture-r89-a.gif](concept/chain-capture-r89-a.gif) | Game capture (M11 part B, step B5): the t=70 Coilwyrm swirling in the drift, 10 s | chosen (round 89 L.2 ok) |
| [concept/reversal-capture-r89-a.gif](concept/reversal-capture-r89-a.gif) | Game capture, full screen: reversal 1 (told at t=98: Rook's call and the chevrons; run t=101–105, the rear wave) and the Choir Herald wave after it, 18 s | chosen (round 89 L.2 ok) |
| [concept/herald-capture-r89-a.png](concept/herald-capture-r89-a.png) | Game capture: the Choir Herald wave with its Needler escorts (t=106.5), 1 s apart | chosen (round 89 L.2 ok) |

## Implementation

- [x] Level data `data.yaml` (M11's build): `ground: none`, `medium: vacuum`, the `beyond-gate` limits, sections and
      speeds, the **drift profile** (θ over time, the two reversals and their lengths), waves with their entry edges,
      radio, budget, threat profile, briefing, backdrop, music (step B2, 2026-11-05)
- [x] Distorted space as the enemies' rules (the drift's lateral component on the units, the bullets and the
      pickups; Level 20's flipped frame for a reversal; the ship screen-fixed; aimed shots aimed on the screen;
      `laser-line`s locked to the screen; a unit carried out through a side edge has left); deterministic and
      allocation-free (M11 part A, step A1a, flown here by steps B2–B3b, 2026-11-05)
- [x] The reversal tells: Rook's scripted call and the HUD's double chevron 2 s ahead; the dim chevron while reversed;
      the rear wave's edge warning from its entry edge (steps A3a, B2, B4, 2026-11-05)
- [x] The backdrop's layers sliding along the vector at their parallax factors (`deep` least), the slow warp, the
      exit's parting curtain; the level opening in the transit's violet (the warp unbending over 1.0 s) (steps
      A3a, ART-B, B4, 2026-11-05)
- [x] The kill-ratio secondary (`KILLS n / 422`, 65 %, judged at the level end) (steps B2, B4, 2026-11-05)
- [x] Wave script: ≥ 80 enemies a minute at medium (`DensityTest`), the Act 2+ floor (`PacingTest`, `ActFloorTest`),
      side waves upwind and edge-warned 3 s, ≥ 60 % front by entry edge, the quiet window t=18–26, ≥ 4 s calm after
      the last enemy, one chain on the screen, no Mantis with a chain, the Skaters' caps (step B3b, 2026-11-05: the
      denser mixed waves, 2.89 on the screen, 19.3 shots a minute; the small units' life 0.80× still pending)
- [x] Radio voiced with the step-2 noise, every timed line at most 1 s late (`RadioTimelineLevel44Test`, with Rook and
      without); the reversal calls scripted, Rook's barks `requires: escort` (steps VOX-B, B4, B5,
      2026-11-05)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`); the
      Rift Skater's portrait; `TraitCoverageTest` covering Level 44 (steps B2, ART-B, 2026-11-05)
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images (steps B2, B4;
      Varga's page shortened and re-voiced: step B5, 2026-11-05)
- [x] Typical haul within ±5 % of budget(44) = 12 841 with the level's `bounty_scale` (`TypicalHaulTest`) (step B2:
      13,095 at 0.29; step B3b: 13,064 at 0.20)
- [x] Music and ambience: track 17, the `vrell-gate` bed with the `event-plane` layer, the level's sounds (`Level44SoundsTest`)
      (steps MUS-B, AUD-B, B4, 2026-11-05; the bed's a/b in round 89)
- [x] Easy/hard variations as in *Difficulty notes* (steps B2, B3b, 2026-11-05)
- [x] The autopilot (reading the drift, a landing ring, a rear wave) flies the level with Rook on every difficulty and
      with `--escort none` (`Level44Test`, `DriftPlay`; step B3, 2026-11-05: 3 of 3 everywhere, the kill ratio 9 of 9)
- [x] Production art and captures for the build's round (ART-B; captures by step B5, 2026-11-05;
      [round 89](../../../concept-rounds/round-89/README.md))

## Open questions

- **The small units' life** (step B3b): 0.80× Level 10's time against the band's 0.9, with the floor met. The
  approved Rift Skaters and Lampreys die fast by design (met as they land, killed in their latch); the choices: accept
  0.8 for Level 44 (`BeamEraHpTest.PENDING_LEAST`, drafted at 0.75), or slower Skaters (a longer first jump before the
  burst). Recommended: accept it.
- **The reversals' pauses**: "no wave in a tell" and "one rear wave per reversal" leave two pauses over 2 s per
  reversal (four), over the Act 2+ rule's three; either the rule counts a reversal's run as a scripted moment (as a
  quiet window) or the rear wave lasts the run (a stream instead of a swarm). Recommended: the scripted moment. Step
  B3 built it as the draft (the tell, the eases and the run: `LevelMetrics.REVERSAL_SCRIPTED`): 1 pause left at
  medium.

## Decisions

- 2026-11-05: denser with mixed types, the user's answer (step B3b, M11 part B; round 89's floor question closed): "This
  is the final act. It can be a bit harder. Increase the density, but not just with small enemies. Add different
  types." Three Choir Heralds ringing Needlers (the returning `medium`, between the chains), Chimera and Needler wings,
  Stinger columns and two more Mantises upwind (648 units at medium, the floor met; see *Measured with the denser
  mixed waves*); the kill-ratio target **422 of 648** (`KILLS n / 422`); the beam era's ×2.5 and the reversal rule
  kept (neither hurts: no pause over 2 s, the small units' life up from 0.66× to 0.80×); `bounty_scale` 0.20.
- 2026-11-05 (step B3, M11 part B, accepted by the user in round 89): the floor's draft answer built (the beam
  era's ×2.5 from Level 43; a reversal a scripted pause) and measured with the drift autopilot (`DriftPlay`); the
  floor still missed (see *Measured with the draft answer*). The kill-ratio target corrected to **343 of 527** (65 %
  of step B2's count; it read 300 of 461, the hand table's): the HUD's `KILLS n / 343`.
- 2026-11-05 (step B2, M11 part B): the round-84 open questions decided as drafted (round 84 closed): the traits
  `spread` and `rear` (the alternative `homing` and `rear` rejected); the length 264 s.
- 2026-11-05 (step B2, M11 part B; accepted by the user in round 89): the [data.yaml](data.yaml) from the hand
  tables: each reversal splits its section (a reversed section from the reversal's time to its run's end, then a 1 s
  forward one, the section's tile sets kept: `tools/art/backdrop_l44.py` cuts the planes by the first section of each);
  a wave spawned in a reversal is a front wave in the flipped frame (Level 20's rule); the gold shard enters at the
  top edge's right part (x 420) on the −25° current; the t=140 chain loops back once, 6 s after it leaves; the hard
  crossing pair its own wave; the far reef sinks in once the curtain has parted (t=260–263: two moving elements at
  most); the music ducks −3 dB on each reversal (`duck: reversal`); 22 offbeat Skitter trickles and the Stingers'
  `small_act_hp` (the floor); `bounty_scale` 0.29 (typical 13,095, +2.0 %).

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
- 2026-11-05 (step B5, the part's close-out; accepted by the user in round 89): Varga's t=11 and t=19 lines moved
  to t=13 and t=21 (the 1 s rule); her briefing page shortened to one screen (`BriefingLayoutTest`: the same facts,
  plainer; it names the Choir Resonator in the shop without "the wardens' eyes still ring") and re-voiced on the key's
  seed (Whisper read back word for word); Vrell space behind the curtain (`beyond`) held still from t=258.2 through
  the outro, so its top edge never reaches the screen (`BackdropAssetsTest`).
- 2026-11-06, round 89: F.4 (the event plane), F.5 (the gold shard) accepted and L.2 ok (the level in play); the art rows are chosen and the part's `art` is chosen. C.1 and C.3 accepted. C.2 accepted: the level's small units die a little faster than the rule wants (0.80× of Level 10's life against 0.9×: its Skaters and Lampreys).
