---
title: Level 23 – Through the Ice
design: approved
implementation: in-progress
art: final
depends-on: [../../../enemies/naval/eel-swarm, ../../../enemies/naval/glassfin, ../../../enemies/naval/driftjelly, ../../../enemies/naval/reef-spitter, ../../../enemies/air/lamprey, ../../../player/weapons/harpoon-torpedoes, ../../../player/weapons/depth-charges, ../../../player/systems, ../../../world/europa]
updated: 2026-10-25
---

# Level 23 – Through the Ice

## Summary

The wing follows the Vrell's drill shaft down. The level is the **descent**, Europa's signature
transition: a narrow shaft through the ice (the walls scrape), the **waterline** where the hydro-kit
switches the ship to its [under-water rules](../../../world/europa/README.md#under-water-rules)
(slower bullets, drag, non-`anti-sub` weapons at × 0.7, no Airstrike or Orbital Lance), and below it the
lamp-lit kelp farms of **Thera Deep**, the city the Vrell are draining. It introduces the act's own
popcorn, the [Glassfin](../../../enemies/naval/glassfin/README.md) schools, and the
[Eel Swarm](../../../enemies/naval/eel-swarm/README.md) chain, with the returning Driftjelly, Reef Spitter and Lamprey.
165 s: a dry shaft mouth, the waterline, a wet shaft, the farms, the edge of the plain.

**Status: approved in round 48 (2026-10-22); in the build (M8 part C).** Drafted in the Act 4 design phase (2026-10-21,
step `m8-dl2224`) from the act roster, the gap analysis's recommended answers (Q1 = b the softened water rules, Q2 = a
lamp-lit to black, Q3 = a the new swimmers) and the wave 1 documents. The level's numbers live in its
[data.yaml](data.yaml) (step C2) and the briefing, threat profile, layout, terrain, waves, ground targets, radio and
credit tables below are rendered from it; the counts are measured in part C (`levelReport`, step C3).

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (approved in round 48):

<!-- data: briefing -->
> **Commander Okafor:** "The Vrell drilled a shaft from Conamara to the ocean. We take it. Your
> Stormhawks have been fitted with hydro-kits overnight: they swim. Below is Thera Deep. Forty
> thousand people live there, and the Vrell are draining their farms."
>
> **Dr. Varga:** "Under water everything slows. Torpedoes and depth charges keep their strength;
> guns lose a third, beams and lasers nearly all. Eels have a head that fires: cut it. A Sonar
> Pulse lights the water. The Airstrike and the Lance cannot reach under the ice."
<!-- /data -->

*Images* (briefs only tonight; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-23-drill-shaft`: a cross-section of Europa's ice: the shelf, Conamara's dome, the drill
  shaft down through twenty kilometres of ice with the waterline marked, Thera Deep's lit pressure domes and farm
  racks at the bottom, the Vrell collars as red marks.
- Varga's page `level-23-eel-and-fry`: her scan of an Eel Swarm (the head's gills circled, the stripe) and a Glassfin
  school as a ribbon, with the rules strip (`BULLETS x0.7`, `DRAG`, `TORPEDO RANGE +50 %`).

*Hangar teaser* (shop screen before L23; Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "A shaft, then water. Torpedoes, Lancer. Guns only tickle it down there."
<!-- /data -->

The hangar before L23 keeps the L22 gear and reminds that the Airstrike and the Orbital Lance read `UNDER ICE`
from the waterline (a `SPECIAL LIMITED` warning on the intel panel). The Pressure hull and the Depth Charges come in
the shop from the visit before L23 ([generator hand plan](../../../player/generator/README.md#act-4-hand-plan-draft): Depth Charges 1 300, the Harpoon to L3).

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (the play plane; Stinger and Needler above the waterline, then Glassfin schools, Eel Swarms, Driftjellies, Lampreys), `ground` (shaft ledges, farm racks: Spine Turrets, Reef Spitters, siphon pumps) |
| Attack directions | front 100% |
| Density | 3 |
| Recommended traits | `anti-sub`, `forward` |
| Hazards | The shaft's walls (terrain collision); water (bullets × 0.7, drag) |
| Boss / mid-boss | none |
| Sensor-suite detail | none: Europa, drill shaft, `air` + `ground`, front · L1: + directions, density 3, hazards "shaft walls" and "water", the objective `REACH THE PLAIN` and the secondary `PUMPS 4/5` · L2: + Glassfin (new), Eel Swarm (new), Driftjelly, Reef Spitter, Lamprey portraits · L3: + `anti-sub` and `forward` highlighted, the wave strip marking the waterline |
<!-- /data -->

*Varga's intel line* per sensor-suite level (they must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "A drill shaft, Lancer, then Thera Deep. Below the line it is all water." |
| L1 | "All from ahead. The walls hurt. Everything slows under water." |
| L2 | "New: glass-fry schools and a head-fired eel chain." |
| L3 | "Torpedoes and charges keep their bite. The rest lose a third." |

## Objective

- **Primary** `reach-end`: reach the edge of the plain, past Thera Deep's farms.
- **Secondary** `destroy-targets` (counted): destroy at least **4 of the 5 Vrell siphon pumps** fixed on the farm racks before
  they leave the screen (the data's `escapes: siphon-pump` with `allowed: 1`, the HUD counter `PUMPS`),
  met when the fourth pump falls, failed at once by the second rack away. Pays **100** credits in Act 1 terms (410 in Act 4); the typical
  haul counts 50 %. A pump is a `ground` target on the sea floor: the torpedo weapons hit it at full damage, every other weapon at
  × 0.7 (the `anti-ground` × 2 first for bolts), so the level pays the player who bought the harpoons.

## Layout

Scroll **100 px/s** in the shaft mouth and the plain's edge, **90 px/s** in the wet shaft, **80 px/s**
through the farms (a rack is on the screen about 8 s). Each change eased over 2 s (the sections' `ramp`). The look runs from the **ice
surface** (white-blue, Jupiter as a bright wedge at the top, the shaft's mouth a black disc) through the shaft (ice walls lit
by the ship's headlights and the collars' teal glow, drilled spiral grooves, from the waterline caustics and drifting
particles) to the **lamp-lit farms** of Thera Deep (round 48 Q2 a: grow-lamp arrays, warm city lights, kelp racks, the
chosen r07 look); the ice ceiling is the `high-air` layer (cracks of light). Motion budget: the schools' ribbons and the
farm lamps are the strong elements.

The data splits the README's first section at the waterline (gap check D1): **five data sections**, the waterline its
own (25.5–28 s, 100 px/s, the first `medium: underwater`), so the water's rules, the grade, the ambience bed, the base
stem and the special limits switch at its start; the shaft mouth is section 1, the wet shaft 3, Thera Deep 4 and the
plain's edge 5 in the tables below.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Shaft Mouth | 0–25.5 | 0–2,550 | 100 | clear | `ground`: the shelf's lip, the shaft's mouth closing from 480 to 320 px (t=6–14); then the dry shaft's walls with ledges (Spine Turret nests, t≈14 and t≈20); the shaft's dark below. | Surface popcorn; the walls teach the terrain rule; the Harpoon, the Depth Charges and the Sonar Pulse read NO WATER until the waterline. |
| 2. The Waterline | 25.5–28 | 2,550–2,800 | 100 | clear | `ground`: the waterline band across the shaft at the ship's row (t=25.5), the wet shaft above it. `low-air`: the waterline's foam lace. | The waterline beat, with the hydro-kit tell (a short refit flash, the HUD's rules strip); the last surface wave still on the screen. |
| 3. The Wet Shaft | 28–72 | 2,800–6,770 | 90 | clear | `ground`: shaft walls 320 px wide until t≈66, then opening to the full 480 by t≈74; Reef Spitter nests on the ledges (t≈36, t≈56), the secret's snagged probe (t≈52, the left wall's foot). `high-air`: the ice ceiling. | The first Glassfin school, the first Eel Swarm, the Driftjellies between the ledges, the walls' scrape; first torpedo targets. |
| 4. Thera Deep | 72–140 | 6,770–12,220 | 80 | clear | `ground`: five farm racks (t=78, 90, 102, 114, 126) each with a siphon pump, lit kelp grids, pressure domes (scenery) with their lamps; a Reef Spitter nest on a rack (t≈108). `high-air`: the ice ceiling. | The pumps (the secondary), eels and schools over the racks, the Thera Deep voice. |
| 5. The Plain's Edge | 140–165 | 12,220–14,700 | 100 | clear | `ground`: the farms thinning into dark plain; the last lamps at the top edge. `high-air`: the ice ceiling. | Release into the next level; the last eels and schools. |
<!-- /data -->

## Terrain

The shaft is the level's **terrain** (the `Terrain` of [Level 16](../../act-3-red-dust/level-16-valles-canyon-run/README.md#terrain): two wall polylines with a damage and a push). Draft:

- **Walls**: ice walls at the field's edges, 480 px at the mouth closing to **320 px** from t=14 to 66 (the ship's hull is 48 px), then opening again to 480 by t≈74.
- **Contact**: a touch costs `small` **4** (not Level 16's 10: the descent is a teaching level) and pushes the ship 30 px off the
  wall; at most once a second; Rook's formation keeps clear. Enemy bullets stop at the walls.
- **Ledges** (the wall mounts): Spine Turret and Reef Spitter nests on the walls (ground targets), the Tendril
  Anchor's wall-mount rule; they fire across the shaft.
- **Waterline**: not terrain; at t≈25.5 the medium switches (the rules ease over 1 s), the render grade changes, the ambience
  bed and the special limits switch with it. The data names no `special_limits` (gap check D9): the `underwater` medium
  itself forbids the Airstrike and the Orbital Lance (the specials' setting limits, `UNDER ICE`).

The walls' collision lines from the data (`terrain`), each point's row with the time it reaches the top edge:

<!-- data: terrain -->
| t at the top edge (scroll px) | Left x | Right x | Flyable width | Notes |
|---|---|---|---|---|
| 0–6 (0–600) | — | — | 480 | The shelf's ice, no walls |
| 6 (600) | 0 | 480 | 480 | The shaft's mouth: the walls begin at the field's edges; wall height 0 |
| 14 (1,400) | 80 | 400 | 320 | The shaft at 320 px; ledge nests at the walls' foot |
| 66 (6,230) | 80 | 400 | 320 | The shaft opens toward the farms |
| 74 (6,940) | 0 | 480 | 480 | The walls leave the field: Thera Deep's open water; wall height 0 |

**Contact**: 4 per touch (easy 2, hard 4), pushed 30 px along the wall's normal over 0.12 s, then a 1 s cooldown in which a touch only clamps.
<!-- /data -->

**The under-water look** (the data's `underwater_look`, presentation only, round 48's grade a): the wet shaft's ambient
0.5 with a cool tint (`bfe0ff`), the caustics on the walls at 0.45, 60 flakes of marine snow and the headlights at 0.8 (the
round-48 descent still); Thera Deep's section its own (ambient 0.58, the warm farm pools baked into the racks, caustics
0.12, snow 40, headlights 0.35: the Thera Deep still), the plain's edge darker (0.45, snow 80, headlights 0.6) toward
Level 24. A section's `underwater_look` changes the level's (part D's Level 24 darkens section by section); the game's
`UnderwaterGrade` reads it (step C4).

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with
a `bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

- **Act HP factor** ([balancing basis](../../../enemies/README.md#balancing-basis)) from the proposed
  reference DPS 100 at L23: **Driftjelly** (L11) 4 → 8 HP, **Reef Spitter** (L11) 7 → 14, **Lamprey** (L12) 3 → 6, **Spine Turret** 16 (the
  `small` units take the beam-era ×2 only); the **Needler** and **Stinger** at the cap ×3 (12 and 18 HP); the Skitter and the Mote are `tiny` (1 HP).
  New: the **Glassfin** (1 HP), the **Eel Swarm** (head 100 / segment 10 / tail 25, whole 245 at 12 segments; the head is 1.0 s of the
  reference) and the **siphon pump** (40 HP, 0.4 s). Under the beam's reach × 0.75 and the × 0.7 damage the beam-era factor is
  re-measured in part C (`BeamEraHpTest`).
- **Density and pacing** (§1.7: drafted to the Act 2+ floor): **352 enemies** (waves 334 + ground targets 18) over the 165 s,
  **≈ 128 a minute** at medium (≥ 60); a wave every 3–3.5 s from t=4, **front 100 %** (the shaft has no side entries, so
  no edge warnings are needed). The count is inflated by the Glassfin schools (about 70 % of it, 1 HP each): the **firing groups**
  carry the floor: an Eel Swarm's head (an aimed shot every 1.8 s, a 3-way fan every 5 s) is on the screen 8–10 s, the Driftjelly
  fields' rings, the Lamprey streams and the ledge nests of Spine Turrets and Reef Spitters; there is no Barbfin until L24. Expected
  to meet ≥ 2.5 enemies on the screen and ≥ 10 shots a minute, **to be confirmed with `levelReport`** in part C (bullets × 0.7 stay on the screen longer: fewer
  needed). The new units get **quiet windows**: no other wave enters t=28.5–32 after the first Glassfin school, nor t=42–45.5 after the first Eel.
  Never more than two eels on the screen.

The data's waves (step C2; measured in step C3). Each school (`swarm`) and ribbon (`column`) flies an S-shaped route
inside the shaft (x 130–350, clear of the walls with the flock's spread and sway); the schools of 16 are 12 on easy and 22
on hard (the stat block's sizes), the rest by the formation lever. The two eels of a pair are two rows 4 s apart (t=90.5
and 94.5, 123.5 and 127.5), each on the other's mirrored route.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | Down the shaft's mouth |
| 7 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 10 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 13 | 1 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | |
| 16 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 19 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 22 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | The last surface wave; still on the screen at the waterline |
| 28.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 12 | front | **New.** The first school; the first harpoon target. The quiet window is until t=32 |
| 32 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 3 | front | Drifting between the shaft's ledges |
| 34.5 | 3 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front (left) | Ribbons down the shaft |
| 38.5 | 3 | line abreast | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front | Homing; the shaft's width keeps them honest |
| 42 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | **New.** A solo 12-segment snake (10 on easy, 16 on hard); alone until t=45.5; Varga's line at t=41; its head shot first drops a shield cell |
| 45.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front | |
| 48.5 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 51.5 | 3 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front (right) | Down the right side (the next ribbon the left) |
| 54.5 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (left) | The leader drops an overdrive |
| 57.5 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | Second solo eel; the head fires the aimed shot first |
| 60 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 12 | front | |
| 63 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 66 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (alternating edges) | |
| 69 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 16 | front | The shaft opens onto the farms |
| 71.5 | 3 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (left) | The first lamps of Thera Deep |
| 75.5 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | Over the first farm rack |
| 78.5 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (right) | |
| 81.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front | |
| 84.5 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | Between the farm lamps |
| 87.5 | 4 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front (right) | Down the right side |
| 90.5 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The first of two eels staggered 4 s apart |
| 93.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 12 | front | |
| 94.5 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The second of the pair, on the mirrored route |
| 96.5 | 4 | line abreast | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front | |
| 99.5 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 102.5 | 4 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (left) | |
| 105.5 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | |
| 108.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front | |
| 111.5 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (left) | |
| 114.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 12 | front | |
| 117.5 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 120.5 | 4 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front (right) | Down the right side |
| 123.5 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The first of two eels staggered 4 s apart |
| 126.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 16 | front | |
| 127.5 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The second of the pair, on the mirrored route |
| 129.5 | 4 | line abreast | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front | |
| 132.5 | 4 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (left) | |
| 135.5 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 138.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front | Over the last farm racks |
| 141.5 | 5 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | |
| 144.5 | 5 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (right) | |
| 147.5 | 5 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 16 | front | |
| 150.5 | 5 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 153.5 | 5 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (right) | |
| 156.5 | 5 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The last eel |
| 159.5 | 5 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front | |
| 162 | 5 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) | The last wave |

Totals: Stinger 9 · Needler 6 · Skitter 4 · Mote Swarm 6 · Glassfin 238 · Driftjelly 31 · Lamprey 30 · Eel Swarm 10.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 1 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nests ×3 on the shaft's ledges (t≈14, 20; dry) | Bounty 12 each |
| 3 | [Reef Spitter](../../../enemies/naval/reef-spitter/README.md) nests ×4 on the wet ledges (t≈36, 56), on the wall's foot instead of a raft | Bounty 14 each |
| 3 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on the wet ledges (t≈44) | Bounty 12 each |
| 3 | Survey probe snagged on a ledge (`ground` trigger, 20 HP, t≈52, the left wall's foot) | The secret: 20 HP free the probe and its hidden crate (100, 410 in Act 4) |
| 4 | **[Siphon pumps](../../../enemies/ground/siphon-pump/README.md)** ×5 on the farm racks (t=78, 90, 102, 114, 126; x 120, 360, 200, 330, 240): a teal pump tube with a glowing sump, 40 HP, no attack | The secondary counts them; bounty 20 each |
| 4 | [Reef Spitter](../../../enemies/naval/reef-spitter/README.md) nest ×3 on the third rack (t≈108) | Bounty 14 each |
<!-- /data -->

The **siphon pump** is a `ground` target (a small structure, not hardened), promoted to its own part,
[siphon-pump](../../../enemies/ground/siphon-pump/README.md), in step C2 (gap check D3: the secondary's `escapes` counts a
unit by its slug). The shaft's nests stand at the walls' foot band (x 96 at the left wall, 384 at the right), the survey
probe too (gap check D4: the README's x 40 lies in the rock); Reef Spitters there sit on the wall's foot, under water
the unit's `underwater` block (no drift, its `floor` look).

## Hazards

- **Shaft walls** (terrain collision; see *Terrain*).
- **The water**: from the waterline the rules of [europa](../../../world/europa/README.md#under-water-rules) hold: bullets × 0.7 (a Glassfin school
  and the ship's own shots), acceleration × 0.6, non-`anti-sub` damage × 0.7, torpedo range +50 %. The Pressure hull (1 MW) removes the drag.
- No other hazards: no currents, no heat.

## Secrets and pickups

- **Survey probe** (t≈52, at the left wall's foot, snagged on a wet ledge in the wet shaft): a `ground` trigger of **20 HP**; destroyed, the probe frees and pops
  a hidden crate worth **100** in Act 1 terms (410 in Act 4). The Salvage scanner's glint marks it. Thera Deep Control's line: "*That's Conamara's
  probe. Keep it, you earned it.*" (an event line, dropped after 6 s if the radio is busy).
- **Overdrive**: dropped by the t=54.5 Lamprey stream's leader.
- **Armour patch**: a Thera Deep tender (a pressure-hull supply drone, the CDF supply drone's role) releases one at (240, 380 y-up) at t=100.
- **Shield cell**: dropped by the first Eel Swarm's head (t=42) when the head is shot first (the data's
  `dropped_by.head_first`; nothing drops if the chain is cut first).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), retimed to the HUD's **1 s rule** (at least 6 s between lines; the build's
`RadioTimelineLevel23Test` checks them with and without Rook). Event lines drop after 6 s. Rook's lines carry `requires: escort`; his one dry joke is at t=99.5, over the racks.

**One new voice**: **Thera Deep Control**, the city's harbour controller, a calm man's voice, cast with the audition step (pending).

The Thera Deep voice speaks as `Thera Deep Control` with the radio portrait `thera-deep` (step ART-C's
`radio-thera-deep`); the speaker is in the voice table, uncast until round 52 picks. The `waterline` trigger is resolved
by the data to the waterline section's start (gap check D5): a timed line at t=25.5.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis flight, down the shaft. The hydro-kits are fitted; trust them. Thera Deep is at the bottom." |
| t=13.5 | Varga | "Water in twelve seconds. Everything slows. Torpedoes and charges bite as ever; guns lose a third, beams nearly all." |
| Waterline crossed (`waterline`, t=25.5) | Varga | "Under the ice. Airstrike and Lance are dead weight now. A Sonar Pulse lights the water." |
| t=41 | Varga | "Eel chain. Cut the head and the body dies; cut the body and the tail scatters." |
| t=65 | Thera Deep (Thera Deep Control) | "Aegis, Thera Deep Control. We see you on the shaft. The farms are below you, and so is whatever is draining them." |
| t=77 | Varga | "Siphon pumps on the racks: they drain the farms into the Vrell. Shoot the pumps, not the racks." |
| t=88.5 | Thera Deep (Thera Deep Control) | "Those are our farms, Aegis. Forty thousand people eat from those racks." |
| t=99.5 | Rook | "A shaft, a hydro-kit and a ship that was never meant to swim. Flying a submarine, Lancer." |
| Secondary met (`secondary-objective`) | Thera Deep (Thera Deep Control) | "Enough pumps are off the racks. Thera Deep owes you a dinner, Aegis." |
| t=140 | Okafor | "The farms are clear. Beyond the lamps is the open plain. Hold your lights, Aegis." |
| Level end | Okafor | "Good work. Thera Deep is breathing again. The Vrell's road leads out across the plain." |
<!-- /data -->

The secret's line (the probe freed): Thera Deep Control, "That's Conamara's probe. Keep it, you earned it."

## Music & ambience

The Act 4 A theme **"Thera Deep"** ([track list](../../../audio/music/README.md#track-list), track 10): the ice-surface ambience in the shaft mouth,
the **under-water bed** switching at the waterline (a muffled ambience, whale-like calls far off; no bubble layer, Q12 a), the
**base stem from the waterline** and the **full mix from section 3** (the farms, with the "surfacing" filter-open not used). New sounds: the
waterline crossing (a heavy submersion swell, no bubbles), the hydro-kit refit tell, the Glassfin's glass shatter, the Eel head's pulse shot and
the eel's body dying, the siphon pump's hum and its break, the shaft wall scrape; listed for the act's sfx step
([sfx](../../../audio/sfx/README.md)); real-thing sounds from Freesound only, none bubbly or popping.

## Credit budget

Budget(23) = 700 × 1.07²² ≈ **3,101**, the typical haul's target
([economy](../../../systems/economy/README.md#per-level-budget)). The data keeps **Act 1 terms**
(Skitter 5, Mote 2, Needler 12, Stinger 15, Glassfin 2, Eel Swarm 48 whole (56 by parts), Driftjelly 10, Lamprey 12, Spine Turret 12, Reef Spitter 14,
siphon pump 20, the crate 100, the secondary 100); the code pays each × the act factor **4.096**, bounties also × the scale. The
first estimate, a **`bounty_scale` of 0.45** (the model of the earlier Act 3 levels: typical = 0.663 of the kills, 0.8 of the ground
targets, half of the secret and secondary), put the hand table's typical haul at 3,087 (−0.5 %). An eel is killed head first by the
typical player (48 whole) and taken apart in a perfect run (60 with the tail first), in this table and in the code alike (step C3).
Flown with the balance plan's fit (`levelReport`, step C3) the typical haul came to 2,906 at 0.45 (−6.3 %), so the scale is
**0.49** (the table below; the code's typical haul is in `TypicalHaulTest`'s ±5 %). No boss or mid-boss.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Stinger 9 × 15 + Needler 6 × 12 + Skitter 4 × 5 + Mote Swarm 6 × 2 + Glassfin 238 × 2 + Driftjelly 31 × 10 + Lamprey 30 × 12 + Eel Swarm 10 × 60 by parts (48 whole) | 3,970 | 2,362 |
| Ground targets: Spine Turret 6 × 12 + Reef Spitter 7 × 14 + Siphon Pump 5 × 20 | 540 | 432 |
| Secret: survey probe (hidden crate, 3% of budget) | 410 | 205 |
| Secondary: break 4 of the 5 siphon pumps | 410 | 205 |
| **Total** (bounty scale 0.49) | **5,330** | **3,204** |
| Budget(n) = the typical haul's target; typical +3 %, perfect 1.72 × budget | | 3,101 |
<!-- /data -->

**Balance plan** (M8 AW, the user's decision of 2026-10-23: nothing sold in Act 4): the plan's L23 visit buys the Depth
Charges L1 (1 300) and fits the Harpoon Torpedoes L1 bought at L22, the beam and the Tail Gun kept in the inventory; the
upgrades come at L24 from what L23 pays ([balance-plan.yaml](../../../player/balance-plan.yaml)). A loadout without
`anti-sub` is flyable but its guns do 70 % of their damage, a beam or a laser 20 % ([europa](../../../world/europa/README.md)).

## Difficulty notes

- **Easy**: the eel has 10 segments and no fan, the schools are 12 (base 16 reduced by the formation lever), the Driftjelly rings are smaller; a pump needs the same hits; the secondary needs 3 of 5; the wall touch costs 2.
- **Hard**: the eel has 16 segments and its aimed shot every 1.4 s; schools of 22 where the table says 16, 40 % of a school dives; the secondary needs all 5; the other waves by the formation lever, trimmed by hand for at most ~3.5 enemies on the screen.

## Concept art

Production art straight away (M8 part C, step ART-C), from the round 48 picks: the descent and Thera Deep mood
stills and the under-water grade are in [europa](../../../world/europa/README.md#concept-art) (with the hydro-kit's wake and
refit flash), the Eel Swarm's and the Glassfin's frames in [naval](../../../enemies/naval/README.md#concept-art), the siphon
pump's in [ground](../../../enemies/ground/README.md#concept-art), the under-water kill bursts in
[art direction](../../../art-direction/README.md#concept-art), Thera Deep Control's portrait in
[characters](../../../story/characters/README.md#concept-art) and the two briefing images on the
[briefing review sheet](../../../ui/briefing/README.md#concept-art). Prompts in [concept/prompts.md](concept/prompts.md);
[concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) is the generator's `backdrop:` block (`tools/art/backdrop_l23.py
--proposal`), merged into [data.yaml](data.yaml) in step C2, as Levels 15 and 22's.

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r52-a.png](concept/backdrop-final-r52-a.png) | Production backdrop (`tools/art/backdrop_l23.py`, from europa-descent-r48-a, europa-thera-deep-r48-a and round 07's farm floor): the tile sets (the dry and the wet shaft, Thera Deep's farms, the plain's edge, the ice ceiling), the pieces at exact rows (the shelf lip, the waterline band at the row the ship passes at 25.5 s, ledges with Vrell collars, the probe's ledge, two seams, five farm racks with grow-lamp pools, pressure domes, the plain's last lamps), the ice-shaft wall textures for WallProjection; ten screens with stand-ins (sheet) | chosen (round 52, F.1: accepted as final) |
| [concept/backdrop-final-r52-a.gif](concept/backdrop-final-r52-a.gif) | t 20–30 at half size: the waterline crossing, dry behind, water ahead | chosen (round 52, F.1: accepted as final) |
| [concept/l23-props-final-r52-a.png](concept/l23-props-final-r52-a.png) | The level's props (`tools/art/l23_props.py`): Conamara's survey probe (snagged, hit, freed; its strobe), the Thera Deep tender (up/down, 3 jet frames, lights), the Reef Spitter's floor base, the Driftjelly's under-water body (sheet) | chosen (round 52, F.5: accepted as final) |
| [concept/l23-props-final-r52-a.gif](concept/l23-props-final-r52-a.gif) | The probe shot free with its crate, the tender bringing the armour patch, a spitter on its base, jellies pulsing | chosen (round 52, F.5: accepted as final) |
| [concept/dry-shaft-capture-r52-a.png](concept/dry-shaft-capture-r52-a.png) | Game capture, M8 part C (step C5b; Level 23 on medium, `--invulnerable`, the balance plan's Level 23 fit: Harpoon Torpedoes L3, Depth Charges L3, two Micro-Missile Pods, the Sonar Pulse, Rook with his gun): the dry shaft at t=8 and t=20, the ice walls and ledges, the weapon rows reading `NO WATER` | chosen (round 52, F.1: accepted as final) |
| [concept/waterline-capture-r52-a.png](concept/waterline-capture-r52-a.png) | The waterline crossing at t=24.6, 25.6 and 26.6: the band reaching the ship's row, the wipe and the refit flash, the rules strip | chosen (round 52, F.1: accepted as final) |
| [concept/waterline-capture-r52-a.mp4](concept/waterline-capture-r52-a.mp4) | The same as a video with the game's sound (t=21.5–31): the last dry wave, the crossing, the wipe, the refit, the rules strip, the first school arriving | chosen (round 52, F.1 and F.6: accepted as final) |
| [concept/probe-capture-r52-a.png](concept/probe-capture-r52-a.png) | The secret at t=52.5, 54, 55 and 56: Conamara's survey probe snagged at the left wall's foot with its strobe, hit, shot free, its crate | chosen (round 52, F.5: accepted as final) |
| [concept/farms-capture-r52-a.png](concept/farms-capture-r52-a.png) | Thera Deep's farms at t=96, 99.5, 101 and 104: the racks with their siphon pumps, the pressure domes, the tender bringing the armour patch, a pump breaking | chosen (round 52, F.1, F.4 and F.5: accepted as final) |
| [concept/plains-edge-capture-r52-a.png](concept/plains-edge-capture-r52-a.png) | The plain's edge at t=141, 148 and 156: the farms thinning into the dark plain, the last lamps | chosen (round 52, F.1: accepted as final) |
| [concept/ice-ceiling-capture-r52-a.png](concept/ice-ceiling-capture-r52-a.png) | The ice ceiling (`high-air`, step C5a's thin cracks of light) over the wet shaft (t=39), the farms' entry (t=74) and the farms (t=135) | chosen (round 52, F.1: accepted as final) |
| [concept/radio-capture-r52-a.png](concept/radio-capture-r52-a.png) | Thera Deep Control on the radio at t=66 and t=89.5 (his portrait, candidate a's voice) | chosen (round 52, F.8: accepted as final) |
| [concept/waterline-capture-r57-a.png](concept/waterline-capture-r57-a.png) | The waterline strip after step FX (round 56 Q.1; step G5, medium): the play field at t≈26.5 and the strip at 1:1, `UNDER WATER · BULLETS ×0.7 · BEAMS ×0.2 · TORPEDOES +50 %` (`DRAG` dropped to keep one line) | chosen (round 57, L.2: accepted) |

## Implementation

- [x] Level data `data.yaml` (M8 part C, step C2): sections and speeds with 2 s eases, the medium switch at the waterline, waves, ground targets, shaft terrain, radio, budget, threat profile, briefing, backdrop (step ART-C's proposal, rerun on the data in step C3: unchanged), music, the under-water look; the `bounty_scale` fitted in step C3 (0.49)
- [x] The shaft's `Terrain` (walls 480 → 320 → 480, ledges with nests, contact 4 and a push) and the section `medium: underwater` with the waterline ease (part A's rules) (step C3, `Level23Test`; the ice walls' look: step C4)
- [x] The Glassfin schools and the Eel Swarm chain as specified; the Driftjelly, Reef Spitter and Lamprey under water (the returning-unit notes in [naval](../../../enemies/naval/README.md#returning-units-under-water)) (steps C1 and C3; their looks: step C4)
- [x] The siphon pumps (a ground unit, [siphon-pump](../../../enemies/ground/siphon-pump/README.md)) and the secondary (`escapes`: 4 of 5, `PUMPS` counter) (step C3)
- [x] The special limits from the waterline (`UNDER ICE` on the Airstrike and the Lance, the intel's `SPECIAL LIMITED` warning); the Sonar Pulse and the Harpoon usable from the waterline
      (`Level23Test.theSpecialLimitsSwitchAtTheWaterline`, `ActFourHangarTest.theIntelMarksAFittedSpecialTheLevelForbids`)
- [x] The secret (survey probe), the Thera Deep tender's armour patch, the overdrive and the shield-cell drops (the head-first drop: steps C1 and C3; the probe's and the tender's looks: step C4)
- [x] Wave script: ≥ 60 enemies a minute at medium, no empty screen over 2 s, the Act 2+ floor, the quiet windows (`DensityTest`, `PacingTest`, `ActFloorTest`, `Level23Test`; step C3)
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel23Test`, with Rook and without), Thera Deep Control cast
      (built and tested: `RadioTimelineLevel23Test`, `VoiceFilesTest`, `Level23Test.theWaterlineLinePlaysAtTheCrossing`, steps VOX-C
      and C5a; [round 52](../../../concept-rounds/round-52/README.md) cast him b, John Van Stan, V.1; his b takes in the game since step D4)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`, its
      `level23sStripMarksTheWaterlineAndThePumps`); the Glassfin's and Eel Swarm's portraits (`PortraitsTest`)
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images (`BriefingLayoutTest.everyPageImageIsInTheAssetsAtItsSize`, `BriefingsTest`)
- [x] Typical haul within ±5 % of budget(23) = 3 101 with the level's `bounty_scale` (`TypicalHaulTest`: 3,080 at 0.49, step C3)
- [x] Music and ambience: the stems by section, the under-water bed, the waterline and the school/eel sounds (`Level23SoundsTest`;
      round 52's picks in the game since step D4)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on every difficulty (`--escort none` too) (step C3)
- [x] Production art and captures for part C's round ([round 52](../../../concept-rounds/round-52/README.md), step C5b; the
      looks: `Level23LooksTest`)

## Decisions

- 2026-10-21 (Act 4 design phase, step `m8-dl2224`; **all decided in round 48 (2026-10-22)**): promoted from the act roster to a
  draft level document, hand tables only, no `data.yaml`. The play plane stays `air` under water (`ground` = the sea floor, `sub` = below); the
  waterline is a medium switch inside the level (the shaft is a `Terrain`); the pumps (not an ally) are the secondary; Thera Deep Control is the
  new voice; numbers from the proposed reference DPS 100.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-22 (user, [round 50](../../../concept-rounds/round-50/README.md) closed): Q.1 **a**: the dry shaft stays as approved: `NO WATER` on the Harpoon Torpedoes, the Depth Charges and the Sonar Pulse until the waterline (t = 25.5 s), the dry popcorn left to the wing weapons and Rook, the crossing flashing the rows back on. Part C measures it with `levelReport` and the autopilot; the waterline moves (b) only if those measures fail.
- 2026-10-22 (M8 part C, step C2; the gap check's defaults, `m8c-gaps.md` §5, accepted in round 52 (2026-10-22)):
  the data file from the approved tables, the hand tables replaced by tables rendered from it. The two open questions
  closed, settled by round 48's approval (D11): the waterline is a switch with a short quiet window and a tell (no set
  piece), the pumps (not an ally) are the secondary. **Five data sections** for the README's four, the waterline its own
  (D1); no `special_limits` key (D9); the `waterline` radio event resolved by content to the waterline's time, a timed
  line (D5); the survey probe and the ledge nests at the walls' foot band, x 96 / 384 (D4); the siphon pump its own part
  (D3); the shield cell only on a head-first kill of the first eel (`dropped_by.head_first`). Step C2's own readings: the
  eel pairs as two rows 4 s apart; the schools' and eels' routes inside the shaft (x 130–350); the schools of 16 as 12 on
  easy and 22 on hard (the Glassfin's stat block; the lever alone gives 13 and 19); the Lamprey `line abreast` rows kept
  as written; the under-water look per section from the round-48 stills (the wet shaft's descent, Thera Deep's farms, a
  darker plain's edge); the stray "(Eel Swarm whole, 48)" and "halved reach" (× 0.75) fixed.
- 2026-10-22 (M8 part C, step C3; accepted in round 52 (2026-10-22)): mapped and flown. The ribbons (`column`) are
  straight columns down the shaft's left or right side (x 144 / 336): the simulation's column flies no authored path.
  The README's Needler and Stinger at the cap ×3 need Level 23 in `small_act_hp`'s levels (added; 24 and 36 HP with the
  beam era). Measured with the balance plan's fit (`levelReport`, seeds 1-3): medium 352 enemies (132 a minute), 7.4 on
  the screen, 21 shots a minute, one stretch over 2 s (26.0-28.8 s, the waterline's quiet window), the small units 1.34×
  Level 10's time on the screen; the typical haul 2,906 at 0.45, so `bounty_scale` **0.49** (3,080, −0.7 %; perfect
  5,330); the autopilot wins every seed on every difficulty with and without Rook, PUMPS met 3/3 on easy and medium, 2/3 on
  hard. **The dry shaft** (round 50 Q.1 a, kept as approved): at medium on seed 1 with Rook the wing pods and Rook kill 23
  of the section's 28 units (4.4 on the screen, 38 shots a minute; 17 shield and 5 armour lost; 418 credits by the
  waterline), without Rook 15 (12 shield, no armour lost; 320 credits): every test passes, so no fallback goes to round 52.
- 2026-10-22 (M8 part C, step C5a; accepted in round 52 (2026-10-22)): VOX-C's radio retime applied (Varga t=17→13.5,
  Thera Deep Control t=34→65 and t=70→88.5, Rook's joke t=9→99.5 over the racks, Okafor t=134→140; every timed line at
  most 1 s late with and without Rook). Varga's t=13.5 line now says "Water in twelve seconds" (the waterline is at
  t=25.5); Thera Deep Control's secondary line is "Enough pumps are off the racks…", true on every difficulty (the third
  pump on easy, the fourth on medium, the fifth on hard), as Level 22's "Enough rigs are down…"; Varga's briefing page 2
  shortened to fit one screen (`BriefingLayoutTest`). Backdrop (`tools/art/backdrop_l23.py`): the ice ledges taper to
  nothing at their top and bottom rows and leave the wall-side column clear (`BackdropAssetsTest`); `thera-farms` is
  rendered over 1,008 rows, where r07's noise lattices wrap, and taken to 960 by nearest rows (`BackdropSeamsTest`: 4.84
  → 0); the ice ceiling's isolines (white scribbles over the field in C4's capture) replaced by seven dim fracture cracks
  with a fork or two each, about a pixel wide.
- 2026-10-22 (M8 part C, steps ART-C, C4 and C5b; accepted in round 52 (2026-10-22)): the production backdrop
  (`tools/art/backdrop_l23.py`), its ledge, nest and dome positions estimated by step ART-C from the data; the HUD's rules
  strip is a one-line prompt for 4 s at the crossing and the weapon rows' `NO WATER` clears with one flash (gap check
  D10, no sound beyond the crossing and refit pair). Captured on medium with `--invulnerable` (step C5b: the dry shaft, the
  waterline, a Glassfin school, an eel, the farms with the pumps and the tender, the plain's edge, the ice ceiling, Thera
  Deep Control on the radio). The frame rate of a full medium run on the software renderer (Xvfb, llvmpipe, no capture):
  **291 fps** on average, the longest frame **56.9 ms**, with the 7.4 units on the screen step C3 measured.
- 2026-10-22 (user, round 52, closed): the art accepted (F.1 the backdrop with the ice ceiling, F.5 the props, F.4 the
  siphon pump, F.6 the hydro-kit's wake and refit flash, F.8 Thera Deep Control's portrait, F.9 the briefing images, F.10
  the intel; `art: final`); the under-water kill bursts **a** (F.7, "pressure flash", already in the game). Thera Deep
  Control is cast **b** (V.1, John Van Stan; his takes in `assets/` since step D4). The sounds picked (waterline a, refit
  a, Glassfin shatter b, eel pulse b and segment b, pump hum b and break b, scrape a, the bed a; the b files in the game
  since step D4); the under-water explosion family (S.10) asked again in round 53. C.1–C.7 accepted (the density, the
  radio retime, the straight Glassfin columns, the eel's first shots, the Glassfin's dive, `small_act_hp`, the pumps'
  secondary), and with them the small rules the steps logged as pending this round (now "accepted in round 52").
- 2026-10-24 (M8 AW, step AW2; accepted in round 56 (2026-10-24)): the plan flies the Harpoon L1 and the Depth Charges
  L1 here (no sale, no upgrade; the user: Level 23 must be doable with the low-level torpedoes): `levelReport` won 3/3 on
  every difficulty, the secondary met 3/3; medium 3.7 on the screen, 10.9 shots a minute, the small units 1.64 s (1.03×
  Level 10's), the typical haul unchanged (−0.7 %). The harpoon's faster rate cleared the waves sooner (five stretches
  over 2 s at medium, three allowed): the t=35.5 ribbon at t=34.5, the t=66 Lamprey line a stream of 4 (1.5 s apart) and
  the t=72.5 ribbon at t=71.5 (three stretches left, two of them the waterline's quiet window). The Act 4 intro's page 3
  and Varga's briefing page warn that beams and lasers lose nearly all their bite under water (the intro: save credits for
  torpedoes and charges), the threat profile's L3 line too (Varga's page drops "theirs and yours" to keep its four
  lines); the two pages' voice takes are re-rendered by step VOX-AW.
  Varga's radio line at t=13.5 ("the rest lose a third") is voiced and left as it is (pending the user's review).
- 2026-10-24 (M8 AW, step VOX-AW; accepted in round 56 (2026-10-24)): Varga's t=13.5 radio line corrected for the
  rework: "Water in twelve seconds. Everything slows. Torpedoes and charges bite as ever; guns lose a third, beams nearly
  all." ("theirs and yours" dropped and "bite as ever" for "keep their bite", so it keeps its six radio lines, two pages:
  the seven-line wording held the radio until the waterline line started 3.4 s late on easy). Re-rendered with the two
  briefing pages; `RadioTimelineLevel23Test`, `BriefingLayoutTest` and `VoiceFilesTest` green.
- 2026-10-24 (user, round 56 closed, Q.1 yes): the waterline's rules strip names the energy weapons:
  `UNDER WATER · BULLETS ×0.7 · BEAMS ×0.2 · TORPEDOES +50 %` (`DRAG` dropped to keep it on one line; see
  [hangar](../../../ui/hangar/README.md#decisions)).
- 2026-10-25 (user, [round 57](../../../concept-rounds/round-57/README.md) closed): L.2 the waterline strip with `BEAMS ×0.2` (round 56's Q.1 as built) accepted.
