---
title: Level 22 – Ice Shelf
design: approved
implementation: done
art: final
depends-on: [../../../enemies/ground/drill-rig, ../../../enemies/naval/skimmer, ../../../enemies/air/mantis, ../../../player/specials, ../../../player/wingmen, ../../../world/europa]
updated: 2026-10-27
---

# Level 22 – Ice Shelf

## Summary

Act 4 opens on Europa's ice. Conamara Station, a research town of forty scientists on the Conamara
Chaos ice field, has reported tremors for a week; the cause is four Vrell
[Drill Rigs](../../../enemies/ground/drill-rig/README.md) boring through the shelf toward the ocean
below. Aegis Wing flies the surface one last time before the dive: hardened rigs to bomb, a ring of
Spine Turrets round each, the Vrell flyers over the ice, and the new **ice geysers**, cracks that glow
and burst as the rigs shake the shelf. Jupiter shows only as a warm sheen on the ice. It is the act's opener and its
breather: no new fighter, one new ground structure, the same surface rules as Act 3 (the hydro-kit
and the water come in Level 23). 150 s: a shelf approach, a slow rig field (60 px/s) and the run-in to
Conamara.

**Status: approved in round 48 (2026-10-22); in the build (M8 part B).** Drafted in the Act 4 design phase
(2026-10-21, step `m8-dl2224`) from the act roster, the gap analysis's recommended answers and the wave 1 documents
([Drill Rig](../../../enemies/ground/drill-rig/README.md), the [specials](../../../player/specials/README.md), the
[wingmen](../../../player/wingmen/README.md)). The level's numbers live in its [data.yaml](data.yaml) (step B2) and
the briefing, threat profile, layout, waves, ground targets, radio and credit tables below are rendered from it; the
counts are measured in part B (`levelReport`, step B3).

## Briefing

Two pages, as Levels 08–14, each one screen (`BriefingLayoutTest`) with room for its image. Texts
(approved in round 48):

<!-- data: briefing -->
> **Commander Okafor:** "Conamara Station has felt the ice shaking for a week. Now we know why:
> the Vrell have set four drill rigs on the shelf and they are boring for the ocean. Forty
> scientists are in that dome. Break the rigs before they go through, then we follow the hole
> down."
>
> **Dr. Varga:** "A rig is a collar and four pumps in a hardened shell. Shots glance off; bombs,
> mortars and an Airstrike get through. Aim for the bore core. And mind the cracks: when the ice
> glows, the steam is next."
<!-- /data -->

*Images* (production art straight away, as Act 2; [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`), briefs only tonight:

- Okafor's page `level-22-conamara-map`: a top-down CDF map of the Conamara Chaos ice field: the
  station dome at the south, the shelf's fissure lines, four red Vrell rig symbols on the way, Jupiter
  as an inset on the corner.
- Varga's page `level-22-drill-rig`: her scan of a Drill Rig (the collar, the four pumps, the bore core
  marked) with an inset of a crack glowing before a geyser.

*Hangar teaser* (shop screen before L22; Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "Last open sky for a while, Lancer. Bombs for the rigs, I'd say."
<!-- /data -->

The hangar before L22 sells the act's first gear: the Harpoon Torpedoes, the Sonar Pulse (one free
charge) and the Bulwark heavy drone (see the [specials](../../../player/specials/README.md#sonar-pulse-l22),
[weapons](../../../player/weapons/harpoon-torpedoes/README.md) and
[wingmen](../../../player/wingmen/README.md#heavy-drone-bulwark-escort-alternative-l22)). All three are idle on
this dry level: the Harpoon and the Sonar Pulse read `NO WATER`; Level 22 is the one to fly with the old
loadout plus an `anti-ground` gun.

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (Stinger, Needler, Skitter and Mote streams, Mantis pincers), `ground` (Drill Rigs, Spine Turret nests, a Polyp Mortar floe, Skimmers in the open leads) |
| Attack directions | front 98% · sides 2% |
| Density | 3 |
| Recommended traits | `anti-ground`, `forward` |
| Hazards | Ice geysers (cracks that glow, then burst); the rigs' ice-shard rings |
| Boss / mid-boss | none |
| Sensor-suite detail | none: Europa, ice shelf, `ground` + `air`, front · L1: + directions (sides), density 3, hazard "ice geysers", the objective `REACH CONAMARA` and the secondary `RIGS 3/4` · L2: + Drill Rig (new, hardened), Skimmer, Stinger, Needler, Mantis, Skitter and Mote portraits · L3: + `anti-ground` and `forward` highlighted, the wave strip marking the four rig sites |
<!-- /data -->

*Varga's intel line* per sensor-suite level (they must fit `IntelPanelLayoutTest`, two lines beside the rows):

| Sensor suite | Line |
|---|---|
| none | "Europa's ice shelf, Lancer. Four rigs are boring for the ocean." |
| L1 | "Mostly ahead. Cracks glow before the steam bursts." |
| L2 | "New: a hardened drill rig. Aim for the glowing core." |
| L3 | "Bombs and mortars for the rigs, forward guns for the rest." |

## Objective

- **Primary** `reach-end`: reach Conamara Station. The rigs never fail the mission: a rig that is
  left standing keeps boring and the next level starts at its shaft (the story's consequence, not a
  fail state).
- **Secondary** `destroy-targets` (counted, as Level 19's grey tanks): destroy at least **3 of the 4
  rig cores** before the rig leaves the screen (the data's `escapes: drill-rig` with `allowed: 1`, the HUD
  counter `RIGS`), met as soon as the third core falls (the counter turns `DONE`) or at the level's
  end, failed at once by the second rig away. Pays **100** credits in Act 1 terms (410 in Act 4, the act
  factor 4.096, 1.6³); the typical haul counts 50 %. It rewards an `anti-ground` gun (the Bomb Rack, the Hammer
  Mortar) or the Airstrike: a loadout with neither can still win the level (the rigs only glance its shots) but
  cannot meet the secondary; the plan's fit has no `anti-ground` gun and breaks each rig with one of its four
  Airstrike charges (a core of 150 HP, hard 195, falls to two of a strike's blasts, under its cap of 300).
  The cores are the whole objective: the pumps are optional bounty and slow the collar 0.5 s each.
- *Pending the user's review*: the roster calls the rigs "the objective". We made them the secondary
  so the opener cannot be failed by a missing bomb rack, as Act 3's openers could not.

## Layout

Scroll **120 px/s** on the shelf approach and the Conamara run-in (110 px/s there), **60 px/s** in the rig field (a rig is on the screen about 11 s, a
hardened core needs time under an `anti-ground` gun). The setting is [Europa](../../../world/europa/README.md)'s
ice surface: a white-blue plain of ridges, fissures and open leads (cracks of black water, scenery;
the Sonar Pulse stays `NO WATER`; the ice is an opaque solid floor with **no deep layer** under it, decided in round 51 (F.3 keep): Jupiter shows as a warm sheen on the ice and huge only in the act's title still), hard sunlight from the top left,
Vrell teal growth round the rigs. Motion budget: the rigs' spinning collars and the geysers' steam are the strong elements; the ice stays still. The
data eases each speed change over 2 s (the sections' `ramp`).

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Shelf | 0–36 | 0–4,320 | 120 | clear | `ground`: fissured ice, ridges, open leads (plain dark water, scenery) along the Skimmers' lanes; a Spine Turret nest on a ridge (t≈20); the first geyser row (t≈12) flares alone. `high-air`: ice crystals. | Popcorn from t=5; the first geysers; no rig yet. |
| 2. The Rig Field | 36–104 | 4,320–8,460 | 60 | clear | `ground`: four **Drill Rigs** (t=40, 56, 72, 88; x 130, 340, 150, 330), each with its nest of Spine Turrets and, after the last, a Polyp Mortar floe (t≈100); steaming cracks where the collars bore; the secret's crevasse at t≈70. | The rigs' quiet window, the hardened target, the geysers between rigs. |
| 3. Conamara Run-in | 104–150 | 8,460–13,470 | 110 | clear | `ground`: the ice-breaker road to **Conamara Station** (the research dome, a landing pad, sodium lamps; scenery), a cordon of Spine Turrets (t≈125); the geysers tremble faster as the dome nears. | The run-in, the faster geysers; level end as the dome reaches the bottom edge. |
<!-- /data -->

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with
a `bounty_scale` (see *Credit budget*). Measured in step B3 (`levelReport`, the plan's fit): see *Density and pacing*.

- **Act HP factor** ([balancing basis](../../../enemies/README.md#balancing-basis)) from the proposed
  reference DPS 96 at L22 (the Act 4 hand plan, [generator](../../../player/generator/README.md#act-4-hand-plan-draft)):
  **Mantis** (L06, 52) ×1.85 → 48 HP; the **Needler** and **Stinger** take the factor capped at ×3
  (`small_act_hp`): 12 and 18 HP; the other `small` units take the beam-era ×2 only: **Skimmer** 10,
  **Spine Turret** 16, **Polyp Mortar** 20; the Skitter and the Mote are `tiny` and keep 1 HP. The
  **Drill Rig** is new here (core 150, pump 24: the whole 246 is 2.6 s of the reference; step B3b).
- **Density and pacing** (§1.7: drafted to the Act 2+ floor from the start): **214 enemies**
  (waves 184 + ground targets 30) over the 150 s from the launch, **≈ 86 a minute** at medium
  (the breather's band is ≥ 48; the Act 3 levels ran 69–119); a wave about every 3 s from t=5, in
  small waves so the rigs read against less noise; the Mantis pincers at t=61, 82 and 121, warned 3 s
  at their edge (`WarningEdge`); front 97 % by count. The floor comes from firing groups on the screen:
  Stinger streams, Needler lines and Skimmer streams plus the **rigs' rings** (a ring of 10 every 3.5 s
  a rig) and the turret nests; expected to meet ≤ 3 pauses over 2 s (`PacingTest`), ≥ 2.5 enemies on
  the screen and ≥ 10 shots a minute (`ActFloorTest`), **to be confirmed with `levelReport`** in part B.
  **Measured in step B3** (the plan's fit, medium): the approved draft's 214 units gave only **2.17 enemies on the
  screen** and **8 stretches over 2 s** (the Skitter and Mote waves of the shelf die at once in the beam; the run's
  last 6.4 s were empty). **Reworked in step B3b** (as Level 21's, accepted in round 51 (2026-10-22)): 19 firing
  groups added (Needler rings held over the tiny waves, Needler and Stinger pairs where the beam had emptied the
  screen, a last Stinger stream to the dome), the approved anchors kept (the sections, rigs, nests, geysers, the
  quiet window, the Skimmer lanes): **273 enemies** at medium (waves 243 + ground 30, 113 a minute), **2.54 on the
  screen**, 24 shots a minute, **no stretch over 2 s** (easy 214, 1.68, none over 3 s; hard 293, 3.54, none over
  2 s), measured with `levelReport` and asserted by `PacingTest` and `ActFloorTest`.
  The new rig gets a **quiet window**:
  no other wave enters t=39.5–43.5 (the rig and its nest only).
- **Rig sites** are ground targets (*Ground targets*); none of the waves hooks them.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 5 | 1 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 8 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 10.5 | 1 | circle | [Needler](../../../enemies/air/needler/README.md) | 5 | front | Step B3b: a firing ring held over the Skitter snake and the Motes (the beam takes them at once) |
| 11 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 14 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 14.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | Step B3b: a firing group behind the ring |
| 17 | 1 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) | Alternating corners, along the open leads |
| 20 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | The leader drops an overdrive |
| 23 | 1 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) | Out of the first open lead on the shelf |
| 26 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 28.5 | 1 | circle | [Needler](../../../enemies/air/needler/README.md) | 5 | front | Step B3b: a firing ring held over the Skitters and the Motes, gone before the first rig's quiet window |
| 29 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 32 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 32.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step B3b: a firing pair over the Motes |
| 35 | 1 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) | |
| 38 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 43.5 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | Over the first rig; the rig's quiet window (t=39.5-43.5) is over |
| 44 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 45.5 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step B3b: a firing pair past the first rig |
| 47 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 50 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 51.5 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step B3b: a firing pair over the second rig's approach |
| 53 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 54 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step B3b: a firing pair before the second rig |
| 56 | 2 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) | |
| 59 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 61 | 2 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | 1 per side, hover 6 s; edge warnings; act HP factor |
| 62.5 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step B3b: a firing pair over the second rig |
| 65 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 68 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 69 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step B3b: a firing pair past the third rig |
| 71 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 74 | 2 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) | |
| 75.5 | 2 | circle | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Step B3b: a firing ring held over the third rig's Skimmers |
| 77 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 80 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 82 | 2 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | 1 per side, hover 6 s; edge warnings; Rook's flank bark; the last drops a shield cell |
| 86 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 89 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 92 | 2 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) | |
| 95 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 96.5 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step B3b: a firing pair over the last rig |
| 98 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 99.5 | 2 | circle | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Step B3b: a firing ring held over the mortar floe |
| 101 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 104 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 105.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step B3b: a firing pair onto the run-in |
| 107 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 109 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The run-in to Conamara; the geysers at their fastest |
| 110 | 3 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) | |
| 111.5 | 3 | circle | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Step B3b: a firing ring held over the run-in's Motes |
| 113 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 116 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 119 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 121 | 3 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | 1 per side, hover 6 s; edge warnings |
| 123 | 3 | circle | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Step B3b: a firing ring held over the Conamara cordon |
| 125 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 128 | 3 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) | |
| 129.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step B3b: a firing pair past the cordon |
| 131 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 134 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 137 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 140 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 140.5 | 3 | circle | [Needler](../../../enemies/air/needler/README.md) | 5 | front | Step B3b: a firing ring held over the last Motes, up to the dome |
| 143 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 143.5 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Step B3b: a firing line over the last Motes |
| 146 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (alternating edges) | Step B3b: the last firing stream as the dome nears |

Totals: Stinger 66 · Needler 63 · Skitter 28 · Mote Swarm 56 · Skimmer 24 · Mantis 6.
<!-- /data -->

The Skimmer streams are lane walkers (step B2): each unit runs its own lane from alternating top corners, along an
open lead the backdrop cuts for it; on the shelf and the run-in a lane curls across the screen and back, in the rig
field the lanes pass under the rig that has just entered (or above the last one), clear of the nests and the cracks.

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 1 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on a ridge (t≈20) | Bounty 12 each |
| 2 | **[Drill Rig](../../../enemies/ground/drill-rig/README.md)** ×4 (t=40, 56, 72, 88; x 130, 340, 150, 330): core 150 HP, 4 pumps of 24, hardened | The secondary counts the cores; core 40, pumps 4 each |
| 2 | Spine Turret nest ×4 round the first rig | Bounty |
| 2 | Spine Turret nest ×4 round the second rig | Bounty |
| 2 | Spine Turret nest ×4 round the third rig | Bounty |
| 2 | Spine Turret nest ×5 round the last rig (one on the ridge beside it) | Bounty |
| 2 | Core-sample cache under a crevasse lip (`ground` trigger, 20 HP, t≈70, x 440) | 20 HP break the lip and free the hidden crate |
| 2 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) floe ×3 after the last rig (t≈100) | Bounty 15 each |
| 3 | Spine Turret cordon ×3 outside Conamara (t≈125) | Bounty |
<!-- /data -->

A destroyed rig leaves a steaming crack and a stilled collar (the Drill Rig's death). The mix has 23
Spine Turrets (3 + 4 + 4 + 4 + 5 + 3; the data sets the exact nests), 3 Polyp Mortars and the 4 rigs: 30 ground enemies in all.

## Hazards

- **Ice geysers** (the data's `vents`; Level 19's furnace vents are the model, `FurnaceVents` on the ground): a crack
  darkens and glows lime-white for a **1.2 s tell** (a hiss and a rising crack sound), then a steam
  column **40 px** across stands for **0.8 s**. It hits the ship and Rook whatever their layer for
  `heavy` **10**, once per burst; enemies are unharmed and bullets pass. Eight rows of 3 cracks at
  t≈12 (alone), 31, 50.5, 66, 84, 107, 118.5 and 136.5 (the data's, clear of the Skimmer leads); a row bursts as a
  ripple (0.4 s a crack, from alternate ends), every **4.0 s** in sections 1–2 and **3.2 s** in section 3 (Conamara's
  tremors: Varga and the station say so; the section-3 rows set their own `period`). A gap is always open. The data's
  `look: ice-geyser` draws and sounds the rows as geysers.
- **Rig rings**: a ring of 10 ice shards every 3.5 s (`medium` = 6) while the pumps live.
- No terrain collision; the rigs and the station are flown over.

## Secrets and pickups

- **Core-sample cache** (t≈70, x 440, under a crevasse lip beside the third rig): a `ground` trigger
  of **20 HP**; destroyed, the lip falls and the cache pops out as a hidden crate worth **100** in
  Act 1 terms (410 in Act 4). The Salvage scanner's glint marks it. Brandt's line: "*That's our deep
  core. Three years of ice. Keep it, you earned it.*" (an event line, dropped after 6 s if the radio is busy).
- **Overdrive**: dropped by the t=20 Stinger leader.
- **Armour patch**: a Conamara ice-crawler (a tracked survey vehicle, the CDF supply drone's role) releases one at (240, 380 y-up) at t=110.5, on the ice-breaker road: it drives up the road from the bottom edge, between open leads 5 and 6, and back down (step B5; t=100 put it on open lead 5).
- **Shield cell**: dropped by the second Mantis pair (t=82).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), retimed to the HUD's **1 s rule** (a timed line
holds the radio for the longer of its voice and its text; no timed line more than 1 s late): the times
below leave at least 6 s between lines and the build's `RadioTimelineLevel22Test` checks them with and
without Rook. Event lines drop after 6 s. Rook's lines carry `requires: escort`; his one dry joke is at t=18, after
the first geyser's warning. Step B3b retimed three lines to the autopilot's event times (the first core at t≈47, the
secondary met at t≈63 on easy, t≈79 on medium and t≈95.5 on hard, the cache at t≈71–73): Varga's personal line from
t=66 to t=58, Rook's Mantis call from t=82 to t=87 (the pincer hovers 6 s from t=82) and Brandt's shaft call from t=100
to t=104.

**The station's voice: Dr. Ilse Brandt**, Conamara Station's director, Varga's former colleague from
the Helix years (the story's L22 beat); dry, tired, precise, a woman's voice, new in Act 4 and kept as the
Conamara voice for L22–L23 ([Act 4 voice](../README.md)). Her radio portrait: the station's
sodium-lit dome control room. Voice cast pending the audition step.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Conamara Station (Dr. Brandt) | "Aegis flight, Conamara Station. Four rigs on the shelf, forty of us in the dome, and the ice will not hold still." |
| t=18 | Rook | "Copy, Conamara. Last open sky for a while, Lancer. Enjoy it." |
| First geyser row flares (`first-geyser`, t≈12) | Varga | "Cracks glow, then steam. Do not sit on them." |
| t=29 | Varga | "Drill rig ahead. Hardened shell: bombs, mortars and the Airstrike get through. Break the bore core." |
| First rig core destroyed (`target-destroyed`) | Conamara Station (Dr. Brandt) | "That's one collar stopped and the shaking eased. Three to go." |
| t=58 | Varga | "Ilse? It's Elena. Hold on, we are coming." |
| t=87 | Rook | "Mantis, both flanks!" |
| t=104 | Conamara Station (Dr. Brandt) | "Something went through under the north dome. There is water in the shaft at Kilo-Seven. It leads down." |
| Secondary met (`secondary-objective`) | Conamara Station (Dr. Brandt) | "Enough rigs are down. The shelf is quiet. Thank you, Aegis." |
| t=135 | Okafor | "Conamara is in sight. We refit for the water tonight and we take that shaft in the morning." |
| Level end | Okafor | "Conamara is safe, for now. Refit, Aegis. Tomorrow we go under the ice." |
<!-- /data -->

The secret's line (the cache freed, an event line): Brandt, "That's our deep core. Three years of ice. Keep it, you earned it." Varga's t=58 line is
her personal beat of the act (her former colleague; Level 23 continues it); Okafor does not reply.

## Music & ambience

The Act 4 A theme **"Thera Deep"** (track 10 in the [track list](../../../audio/music/README.md#track-list),
chosen in round 02; its production stems come with part A): the Europa ice-surface ambience alone in
section 1 (wind, a distant ice groan, the station's hum at the end), the **base stem from section 2** (the
first rig), the **full mix from section 3** (the run-in to Conamara). Round 48 Q12 a: no
bubble layer in the full-length theme. New sounds this level needs, listed for the act's sfx step
([sfx](../../../audio/sfx/README.md)): the geyser's crack and burst (heavy, no hiss-pops), the Drill
Rig's collar (a low drill whine, its ring), its core's death, the Conamara station hum and the ice
ambience bed; sounds that imitate real things come from Freesound only (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(22) = 700 × 1.07²¹ ≈ **2,898**, the typical haul's target
([economy](../../../systems/economy/README.md#per-level-budget)). The data keeps **Act 1 terms**
(Skitter 5, Mote 2, Needler 12, Stinger 15, Skimmer 14, Mantis 30, Drill Rig 56 whole, Spine Turret 12,
Polyp Mortar 15, the crate 100, the secondary 100); the code pays each × the act factor **4.096**
(1.6³), bounties also × the scale. A **`bounty_scale` of 0.30** (the model of the earlier Act 3
levels: typical = 0.663 of the kills, 0.8 of the ground targets, half of the secret and secondary; a first
estimate for part B to confirm with `TypicalHaulTest` and `levelReport`) puts the table's typical haul at
**2,857 (−1 %)**, the perfect run at 4,538 (step B3b, re-fitted after the waves' rework from B3's 0.38: `levelReport`
typical 2,857, −1.4 %, within `TypicalHaulTest`'s ±5 %). No boss or mid-boss.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Stinger 66 × 15 + Needler 63 × 12 + Skitter 28 × 5 + Mote Swarm 56 × 2 + Skimmer 24 × 14 + Mantis 6 × 30 | 3,043 | 1,907 |
| Ground targets: Spine Turret 23 × 12 + Drill Rig 4 × 56 + Polyp Mortar 3 × 15 | 675 | 540 |
| Secret: core-sample cache (hidden crate, 3% of budget) | 410 | 205 |
| Secondary: break 3 of the 4 drill rigs | 410 | 205 |
| **Total** (bounty scale 0.3) | **4,538** | **2,857** |
| Budget(n) = the typical haul's target; typical -1 %, perfect 1.57 × budget | | 2,898 |
<!-- /data -->

**Balance plan**: the plan's L22 visit (2 855 cash, Harpoon Torpedoes 2 000 and L2 500) buys gear that is idle here
([generator](../../../player/generator/README.md#act-4-hand-plan-draft)); the level expects `anti-ground`
and `forward` and is flyable without either (without `anti-ground` the secondary fails).

## Difficulty notes

- **Easy**: the rigs' shards every 4.5 s (the Drill Rig's easy hook), the Mantis pincers have 1 each;
  the secondary needs 2 of 4 cores; geysers cycle every 5 s (4 s in section 3).
- **Hard**: a second `aimed` shard with every ring; the secondary needs all 4 cores; geysers cycle every 3.2 s
  (2.6 s in section 3, its ripple 0.3 s so the row fits); the other waves by the formation lever, trimmed by hand on
  the Stinger streams for at most ~3.5 enemies on the screen (they keep 3); the t=35 Skimmers keep 3 (the first
  rig's quiet window).

## Concept art

Production art straight away (M8 part B, step ART-B), from the round 48 picks: the ice-surface mood still
and the under-water look are in [europa](../../../world/europa/README.md#concept-art), the Drill Rig's frames in
[ground](../../../enemies/ground/README.md#concept-art), Dr. Brandt's portrait in
[characters](../../../story/characters/README.md#concept-art) and the two briefing images on the
[briefing review sheet](../../../ui/briefing/README.md#concept-art). Prompts in [concept/prompts.md](concept/prompts.md);
[concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) is the generator's `backdrop:` block (`tools/art/backdrop_l22.py
--proposal`), merged into [data.yaml](data.yaml) (which adds the `far` factor the loader asks for), as Level 15's.

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r51-a.png](concept/backdrop-final-r51-a.png) | Production backdrop (`tools/art/backdrop_l22.py`, from europa-ice-shelf-r48-a): the tile sets (the shelf with Europa's double ridges, the Conamara Chaos rig field, the run-in with the ice-breaker road, the high-air crystals), the pieces at exact rows (two seams, the nest's pressure ridge, the secret's crevasse, seven open leads cut along every Skimmer run, Conamara Station) and eight screens with stand-ins (sheet) | chosen (round 51, F.2: accepted as final) |
| [concept/backdrop-final-r51-a.gif](concept/backdrop-final-r51-a.gif) | t 16–26 at half size: the ridge nest, the first leads with the Skimmer runs' water | chosen (round 51, F.2: accepted as final) |
| [concept/l22-props-final-r51-a.png](concept/l22-props-final-r51-a.png) | The level's props (`tools/art/l22_props.py`): the ice geyser (crack, tell glow, 0.8 s steam column, residue), the crevasse lip over the core-sample cache (intact, hit, fallen; its marker light), the Conamara ice-crawler (up/down, 3 track frames, lights), the station piece (sheet) | chosen (round 51, F.4: accepted as final) |
| [concept/l22-props-final-r51-a.gif](concept/l22-props-final-r51-a.gif) | A geyser row's ripple, the lip shot open, the ice-crawler driving up and back | chosen (round 51, F.4: accepted as final) |
| [concept/ice-shelf-capture-r51-a.png](concept/ice-shelf-capture-r51-a.png) | Game capture, M8 part B (step B5, medium, `--invulnerable`): the shelf at t=13.2 (the first geyser row's cracks glowing for the tell), t=14 (the steam), t=24 and t=33 (the first open leads under the Skimmer runs) | chosen (round 51, F.2: accepted as final) |
| [concept/geyser-capture-r51-a.mp4](concept/geyser-capture-r51-a.mp4) | The same run, t 9–19 with the game's sound: the first geyser row's tell, steam and ripple, Varga's warning | chosen (round 51, F.4: accepted as final) |
| [concept/crawler-capture-r51-a.png](concept/crawler-capture-r51-a.png) | The same run, t 107.5–112.5: Conamara's ice-crawler driving up the ice-breaker road, the armour patch dropped at t=110.5, the crawler driving back (the drop moved off lead 5 in step B5) | chosen (round 51, F.4: accepted as final) |
| [concept/station-capture-r51-a.png](concept/station-capture-r51-a.png) | The same run, t 128–150: the run-in on the road past the Spine Turret cordon, Okafor's t=135 line, Conamara Station coming in and its dome at the bottom edge | chosen (round 51, F.2: accepted as final) |
| [concept/radio-capture-r51-a.png](concept/radio-capture-r51-a.png) | The same run: Dr. Brandt on the radio (her portrait and text) at t=3 (t=1 line), t=48 (`target-destroyed`), t=94.5 (`secondary-objective`, the new "Enough rigs are down") and t=106 (t=104 line) | chosen (round 51, F.5: accepted as final) |

## Implementation

- [x] Level data `data.yaml` (M8 part B, step B2): sections and speeds with 2 s eases, waves (the Skimmer lanes),
      ground targets, geyser rows, radio, budget, threat profile, briefing, music; the backdrop block a placeholder
      until step ART-B's proposal, the `bounty_scale` fitted in step B3
- [x] The four Drill Rigs as hardened ground targets with a vital core and four pumps
      ([Drill Rig](../../../enemies/ground/drill-rig/README.md)); the secondary `destroy-targets` (3 of 4 cores, `RIGS` counter)
- [x] The ice geysers (`FurnaceVents` on the ground, 1.2 s tell, `heavy` 10 once per burst, cadence faster in section 3)
- [x] The secret (core-sample cache, 20 HP trigger, crate 100), the Conamara ice-crawler's armour patch, the overdrive and shield-cell drops
      (`Level22Test.theLevelHoldsTheReadmesCount`, `Level22LooksTest.theIceCrawlerCarriesThePatchUpTheRoadAndBack`; the drop
      moved to t=110.5 on the road in step B5)
- [x] Wave script: ≥ 48 enemies a minute at medium (`DensityTest`), no empty screen over 2 s (`PacingTest`), the Act 2+ floor (`ActFloorTest`), the rig's quiet window, edge warnings for the Mantis pincers
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel22Test`, with Rook and without), Dr. Brandt cast; Rook's lines `requires: escort`
      (built and tested: `RadioTimelineLevel22Test`, `VoiceFilesTest`, `Level22Test.theRadioEventsFireInTheRun`; Dr. Brandt cast with
      candidate a in round 51, V.1)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`, its
      `level22sStripMarksTheFourRigSites`); the Drill Rig's portrait (`PortraitsTest`)
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images (`BriefingLayoutTest.everyPageImageIsInTheAssetsAtItsSize`, `BriefingsTest`)
- [x] Typical haul within ±5 % of budget(22) = 2 898 with the level's `bounty_scale` (`TypicalHaulTest`)
- [x] Music and ambience: the Act 4 A theme's stems by section; the ice-surface bed; the geyser and rig sounds (`Level22SoundsTest`; round 51's picks: the burst, the beds a, the geyser tell, collar, ring and death b still to replace the a files in the game)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on every difficulty (`--escort none` too)
- [x] The hangar before L22 sells the Harpoon Torpedoes, the Sonar Pulse (1 free charge) and the Bulwark, each reading `NO WATER` / idle here
      (`ActFourHangarTest.theActFourItemsAreInTheLiveShopWithTheirNames`, `ArsenalHudTest`)
- [x] Production art and captures for part B's round ([round 51](../../../concept-rounds/round-51/README.md), step B5)

## Decisions

- 2026-10-21 (Act 4 design phase, step `m8-dl2224`; **all decided in round 48 (2026-10-22)**): promoted from the act roster to a
  draft level document, hand tables only, no `data.yaml`. The rigs are the secondary (3 of 4 cores), the geysers are
  `FurnaceVents` on the ground, the level is dry (the hydro-kit and the water start in L23), Dr. Ilse Brandt is the
  new Conamara voice (Varga's former colleague), numbers from the proposed reference DPS 96.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved. The two
  open questions settled by the approval: the rigs are the **secondary** (3 of 4 cores), so an `anti-ground`-less
  loadout can still finish the opener; the open leads are scenery, never water (the Sonar Pulse reads `NO WATER`
  until L23's waterline).
- 2026-10-22 (M8 part B, step B2; the gap check's defaults, accepted in round 51 (2026-10-22)): the data file
  from the approved tables. **8** geyser rows (the hazard text listed eight times under "seven rows"); hard's
  section-3 period 2.6 s with a 0.3 s ripple (tell 1.2 + burn 0.8 + 2 ripples must fit the period); easy's
  section-3 rows every 4 s (the "faster near Conamara" beat kept on easy); the secondary's `allowed` easy 2, medium 1,
  hard 0; the Skimmer lanes, the rig nests (round each rig, clear of its pumps and of the Skimmer leads) and the geyser
  rows' x placed by B2, which moved the t≈30 row to 31, the t≈52 row to 50.5, the t≈118 row to 118.5 and the t≈132
  row to 136.5 (clear of the leads); the Stinger streams and the t=35 Skimmers keep 3 on hard; the Mantis pincers
  fly 1 on easy; the Skimmers count with the ground layers in the threat profile (they are `ground` units).
- 2026-10-22 (M8 part B, step B3): the rigs, the geysers' rows, the per-difficulty secondary and the radio events
  flown (`Level22Test`); the autopilot calls the Airstrike on a rig low on the screen (the plan's fit has no
  `anti-ground` gun; with one it flies over the rig, pumps first): RIGS met easy 3/3, medium 2/3, hard 0/3 (a hard
  core's 312 HP is past one strike's cap of 300). The `bounty_scale` 0.38 confirmed (−2.3 %). The waves measure below
  the Act 2+ floor (2.17 on the screen, 8 stretches over 2 s at medium): measured, not asserted, pending the user's
  rework in round 51. The Needler and the Stinger take `small_act_hp` here as the waves' notes say (24 and 36 HP with
  the beam era's ×2). The backdrop block is step ART-B's (no deep layer, kept in round 51).
- 2026-10-22 (M8 part B, step B3b; accepted in round 51 (2026-10-22)): the waves reworked to the Act 2+ rules as
  Level 21's (19 firing groups added, the anchors kept; 2.17 → 2.54 on the screen and 8 → 0 stretches over 2 s at
  medium); L22 leaves `PacingTest.PENDING_WAVES`; the `bounty_scale` re-fitted 0.38 → 0.30 (typical 2,857, −1.4 %).
  The rig's core 240 → 150 HP (see the [Drill Rig](../../../enemies/ground/drill-rig/README.md)'s Decisions): the plan's
  fit meets RIGS on every difficulty (easy, medium and hard 3/3, the autopilot calling the Airstrike once a rig is in
  the lower half of the screen). The hardened-shell text corrected (Micro-missiles are not `anti-ground`): Varga's t=29
  line and briefing page 2 say "bombs, mortars and the Airstrike / an Airstrike", her L3 intel line "Bombs and mortars";
  both voice lines re-rendered (`tools/art/voice.py`, Whisper read back right with the name prompt). VOX-B's radio
  patch applied (Rook's dry joke t=8 → 18) and Varga's t=66 → 58, Rook's t=82 → 87, Brandt's t=100 → 104 retimed to
  the measured event times (`RadioTimelineLevel22Test`).
- 2026-10-22 (M8 part B, the gap check's §5, steps ART-B, AUD-B and B4; accepted in round 51 (2026-10-22)): the geysers reuse `vents` with a `look`
  (`ice-geyser`: the crack, the tell's glow, the steam column and residue, `tools/art/l22_props.py`), sounded by look; the
  station's hum is section 3's bed (`conamara-station`), no positional hum; the ice-crawler is presentation on the
  existing `drops:` (`IceCrawlerLooks`, up the road from the bottom edge and back); the Polyp floe stays at t≈100 as the
  tables count it; the leads are scenery (Sonar Pulse `NO WATER` all level). The backdrop (`tools/art/backdrop_l22.py`):
  the leads read as heavy black strips, the dome is simple, and there is **no deep layer** (the design had Jupiter's
  deep layer under the shelf): kept in [round 51](../../../concept-rounds/round-51/README.md) (F.3, 2026-10-22).
- 2026-10-22 (M8 part B, step B5; accepted in round 51 (2026-10-22)): the ice-crawler's armour patch moved from t=100 (on open lead 5) to **t=110.5**
  on the ice-breaker road (its drive up from the bottom edge and back stays off leads 5 and 6; haul unchanged, the
  autopilot's credits −0.4 %); Dr. Brandt's secondary line "Three rigs are down" (wrong on easy, 2, and hard, 4) is now
  "Enough rigs are down. The shelf is quiet. Thank you, Aegis." (one radio page, so her t=104 line keeps its time;
  both candidates re-rendered). Captures for round 51 in this table.
- 2026-10-22 (user, round 51, closed): the art accepted (F.2 the backdrop, F.4 the props, F.5 Dr. Brandt's portrait; `art: final`). **F.3 keep**: no deep layer under the shelf, the ice stays an opaque floor and the leads plain dark water; the design text no longer puts Jupiter in a deep layer (it shows as the warm sheen on the ice and in the act's title still). C.1–C.7 accepted (the waves, the rig's core HP, breaking the rigs with the Airstrike, the geyser rows off the leads, the bounty split, the secondary per difficulty, the crawler's drop). Dr. Brandt is cast with candidate a (V.1). The sounds: the geyser tell b, the burst a, the rig's collar, ring and death b, the beds a (S.1–S.7; the b files of S.1, S.3–S.5 replace the a files in the next code step).
