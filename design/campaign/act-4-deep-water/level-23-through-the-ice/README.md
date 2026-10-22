---
title: Level 23 – Through the Ice
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/naval/eel-swarm, ../../../enemies/naval/glassfin, ../../../enemies/naval/driftjelly, ../../../enemies/naval/reef-spitter, ../../../enemies/air/lamprey, ../../../player/weapons/harpoon-torpedoes, ../../../player/weapons/depth-charges, ../../../player/systems, ../../../world/europa]
updated: 2026-10-22
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

**Status: approved in round 48 (2026-10-22); drafted (Act 4 design phase, 2026-10-21; step `m8-dl2224`); everything below is pending the
user's review in round 48.** Written from the act roster, the gap analysis's recommended answers (Q1 = b the
softened water rules, Q2 = a lamp-lit to black, Q3 = a the new swimmers) and the wave 1 documents. There is
**no `data.yaml`**: the build (M8 part C) turns the hand tables into one. The tables are marked draft; no number is measured.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft):

> **Commander Okafor:** "The Vrell drilled a shaft from Conamara to the ocean. We take it. Your
> Stormhawks have been fitted with hydro-kits overnight: they swim. Below is Thera Deep. Forty
> thousand people live there, and the Vrell are draining their farms."
>
> **Dr. Varga:** "Under water everything slows, theirs and yours. Torpedoes and depth charges hold
> their strength; the rest of your guns lose a third. Eels with a head that fires: cut the head. And a
> Sonar Pulse lights the whole water. The Airstrike and the Lance cannot reach under the ice."

*Images* (briefs only tonight; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-23-drill-shaft`: a cross-section of Europa's ice: the shelf, Conamara's dome, the drill
  shaft down through twenty kilometres of ice with the waterline marked, Thera Deep's lit pressure domes and farm
  racks at the bottom, the Vrell collars as red marks.
- Varga's page `level-23-eel-and-fry`: her scan of an Eel Swarm (the head's gills circled, the stripe) and a Glassfin
  school as a ribbon, with the rules strip (`BULLETS x0.7`, `DRAG`, `TORPEDO RANGE +50 %`).

*Hangar teaser* (shop screen before L23; Rook, not voiced):

> **Rook:** "A shaft, then water. Torpedoes, Lancer. Guns only tickle it down there."

The hangar before L23 keeps the L22 gear and reminds that the Airstrike and the Orbital Lance read `UNDER ICE`
from the waterline (a `SPECIAL LIMITED` warning on the intel panel). The Pressure hull and the Depth Charges come in
the shop from the visit before L23 ([generator hand plan](../../../player/generator/README.md#act-4-hand-plan-draft): Depth Charges 1 300, the Harpoon to L3).

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `air` (the play plane; Stinger and Needler above the waterline, then Glassfin schools, Eel Swarms, Driftjellies, Lampreys), `ground` (shaft ledges, farm racks: Spine Turrets, Reef Spitters, siphon pumps) |
| Attack directions | front 100 % |
| Density | 3 |
| Recommended traits | `anti-sub`, `forward` |
| Hazards | The shaft's walls (terrain collision); water: bullets × 0.7, drag |
| Boss / mid-boss | none |
| Sensor-suite detail | none: Europa, drill shaft, `air` + `ground`, front · L1: + directions, density 3, hazards "shaft walls" and "water", the objective `REACH THE PLAIN` and the secondary `PUMPS OUT` · L2: + Glassfin (new), Eel Swarm (new), Driftjelly, Reef Spitter, Lamprey portraits · L3: + `anti-sub` and `forward` highlighted, the wave strip marking the waterline |

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

| Section | t (s) | Scroll (px, approx.) | Speed (px/s) | Medium | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Shaft Mouth | 0–28 | 0–2,800 | 100 | dry, **waterline at t≈25.5** | `ground`: the shelf's lip, the shaft's mouth closing from 480 to 320 px (t=6–14); then the shaft walls with ledges (Spine Turret nests, t≈14 and t≈20). The ship crosses the waterline at t≈25.5; the factors ease in over 1 s. `deep`: the shaft's dark. | Surface popcorn; the walls teach the terrain rule; the waterline beat, with the hydro-kit tell (a short refit flash, the HUD's rules strip). |
| 2. The Wet Shaft | 28–72 | 2,800–6,760 | 90 | water | `ground`: shaft walls 320 px wide until t≈66, then opening to the full 480 by t≈74; Reef Spitter nests on the ledges (t≈36, t≈56), the secret's snagged probe (t≈52, x 40). `high-air`: the ice ceiling. | The first Glassfin school, the first Eel Swarm, the Driftjellies between the ledges, the walls' scrape; first torpedo targets. |
| 3. Thera Deep | 72–140 | 6,760–12,200 | 80 | water | `ground`: five farm racks (t=78, 90, 102, 114, 126) each with a siphon pump, lit kelp grids, pressure domes (scenery) with their lamps; a Reef Spitter nest on a rack (t≈108). | The pumps (the secondary), eels and schools over the racks, the Thera Deep voice. |
| 4. The Plain's Edge | 140–165 | 12,200–14,700 | 100 | water | `ground`: the farms thinning into dark plain; the last lamps at the top edge. | Release into the next level; the last eels and schools. |

## Terrain

The shaft is the level's **terrain** (the `Terrain` of [Level 16](../../act-3-red-dust/level-16-valles-canyon-run/README.md#terrain): two wall polylines with a damage and a push). Draft:

- **Walls**: ice walls at the field's edges, 480 px at the mouth closing to **320 px** from t=14 to 66 (the ship's hull is 48 px), then opening again to 480 by t≈74.
- **Contact**: a touch costs `small` **4** (not Level 16's 10: the descent is a teaching level) and pushes the ship 30 px off the
  wall; at most once a second; Rook's formation keeps clear. Enemy bullets stop at the walls.
- **Ledges** (the wall mounts): Spine Turret and Reef Spitter nests on the walls (ground targets), the Tendril
  Anchor's wall-mount rule; they fire across the shaft.
- **Waterline**: not terrain; at t≈25.5 the medium switches (the rules ease over 1 s), the render grade changes, the ambience
  bed and the special limits switch with it (`special_limits: [airstrike, orbital-lance]` from the waterline).

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with
a `bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

- **Act HP factor** ([balancing basis](../../../enemies/README.md#balancing-basis)) from the proposed
  reference DPS 100 at L23: **Driftjelly** (L11) 4 → 8 HP, **Reef Spitter** (L11) 7 → 14, **Lamprey** (L12) 3 → 6, **Spine Turret** 16 (the
  `small` units take the beam-era ×2 only); the **Needler** and **Stinger** at the cap ×3 (12 and 18 HP); the Skitter and the Mote are `tiny` (1 HP).
  New: the **Glassfin** (1 HP), the **Eel Swarm** (head 100 / segment 10 / tail 25, whole 245 at 12 segments; the head is 1.0 s of the
  reference) and the **siphon pump** (40 HP, 0.4 s). Under the beam's halved reach and the × 0.7 damage the beam-era factor is
  re-measured in part C (`BeamEraHpTest`).
- **Density and pacing** (§1.7: drafted to the Act 2+ floor): **352 enemies** (waves 334 + ground targets 18) over the 165 s,
  **≈ 128 a minute** at medium (≥ 60); a wave every 3–3.5 s from t=4, **front 100 %** (the shaft has no side entries, so
  no edge warnings are needed). The count is inflated by the Glassfin schools (about 70 % of it, 1 HP each): the **firing groups**
  carry the floor: an Eel Swarm's head (an aimed shot every 1.8 s, a 3-way fan every 5 s) is on the screen 8–10 s, the Driftjelly
  fields' rings, the Lamprey streams and the ledge nests of Spine Turrets and Reef Spitters; there is no Barbfin until L24. Expected
  to meet ≥ 2.5 enemies on the screen and ≥ 10 shots a minute, **to be confirmed with `levelReport`** in part C (bullets × 0.7 stay on the screen longer: fewer
  needed). The new units get **quiet windows**: no other wave enters t=28.5–32 after the first Glassfin school, nor t=42–45.5 after the first Eel.
  Never more than two eels on the screen.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | Down the shaft, three abreast |
| 7 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 10 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front |  |
| 13 | 1 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) |  |
| 16 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 19 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |
| 22 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | The last surface wave; still on the screen at the waterline |
| 28.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 12 | front | **New.** The first school; the first harpoon target; Varga's line at t=27.5. The quiet window is until t=32 |
| 32 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 3 | front | Drifting between the shaft's ledges |
| 35.5 | 2 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | Ribbons down the shaft |
| 38.5 | 2 | line abreast | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front | Homing; the shaft's width keeps them honest |
| 42 | 2 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | **New.** A solo 10-segment snake (12 by default, 10 on easy); alone until t=45.5; Varga's line at t=41 |
| 45.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front |  |
| 48.5 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 51.5 | 2 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front (alternating edges) |  |
| 54.5 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (left) |  |
| 57.5 | 2 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | Second solo eel; the head fires the aimed shot first |
| 60 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 12 | front |  |
| 63 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 66 | 2 | line abreast | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front |  |
| 69 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 16 | front | The shaft opens onto the farms |
| 72.5 | 3 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front | The first lamps of Thera Deep |
| 75.5 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | Over the first farm rack |
| 78.5 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (right) |  |
| 81.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front |  |
| 84.5 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | Between the farm lamps |
| 87.5 | 3 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front (alternating edges) |  |
| 90.5 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 2 | front | Two eels staggered 4 s apart |
| 93.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 12 | front |  |
| 96.5 | 3 | line abreast | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front |  |
| 99.5 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 102.5 | 3 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front |  |
| 105.5 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front |  |
| 108.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front |  |
| 111.5 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (left) |  |
| 114.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 12 | front |  |
| 117.5 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 120.5 | 3 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front (alternating edges) |  |
| 123.5 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 2 | front | Two eels staggered 4 s apart |
| 126.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 16 | front |  |
| 129.5 | 3 | line abreast | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front |  |
| 132.5 | 3 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front |  |
| 135.5 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 138.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front | Over the last farm racks |
| 141.5 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front |  |
| 144.5 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (right) |  |
| 147.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 16 | front |  |
| 150.5 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 153.5 | 4 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front |  |
| 156.5 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The last eel |
| 159.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front |  |
| 162 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) | The last wave |

Totals: Glassfin 238 · Driftjelly 31 · Lamprey 30 · Eel Swarm 10 · Stinger 9 · Needler 6 · Mote Swarm 6 · Skitter 4.

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 1 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nests ×3 on the shaft ledges (t≈14, 20; dry) | Bounty 12 each |
| 2 | [Reef Spitter](../../../enemies/naval/reef-spitter/README.md) nests ×4 on the wet ledges (t≈36, 56), sitting on the wall's foot instead of a raft; Spine Turret nest ×3 (t≈44) | Bounty 14 / 12 each |
| 2 | Survey probe snagged on a ledge (`ground` trigger, 20 HP, t≈52, x 40) | The secret: a crate worth 100 (410) |
| 3 | **Siphon pumps** ×5 on the farm racks (t=78, 90, 102, 114, 126; x 120, 360, 200, 330, 240): a teal pump tube with a glowing sump, 40 HP, no attack | The secondary counts them; bounty 20 each |
| 3 | Reef Spitter nest ×3 on the third rack (t≈108) | Bounty 14 each |

The **siphon pump** is a level-local `ground` target (a small structure, not hardened), specified here: promote it to its own part if
a later level reuses it.

## Hazards

- **Shaft walls** (terrain collision; see *Terrain*).
- **The water**: from the waterline the rules of [europa](../../../world/europa/README.md#under-water-rules) hold: bullets × 0.7 (a Glassfin school
  and the ship's own shots), acceleration × 0.6, non-`anti-sub` damage × 0.7, torpedo range +50 %. The Pressure hull (1 MW) removes the drag.
- No other hazards: no currents, no heat.

## Secrets and pickups

- **Survey probe** (t≈52, x 40, snagged on a wet ledge in section 2): a `ground` trigger of **20 HP**; destroyed, the probe frees and pops
  a hidden crate worth **100** in Act 1 terms (410 in Act 4). The Salvage scanner's glint marks it. Thera Deep Control's line: "*That's Conamara's
  probe. Keep it, you earned it.*" (an event line, dropped after 6 s if the radio is busy).
- **Overdrive**: dropped by the t=54.5 Lamprey stream's leader.
- **Armour patch**: a Thera Deep tender (a pressure-hull supply drone, the CDF supply drone's role) releases one at (240, 380 y-up) at t=100.
- **Shield cell**: dropped by the first Eel Swarm's head (t=42) when the head is shot first.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), retimed to the HUD's **1 s rule** (at least 6 s between lines; the build's
`RadioTimelineLevel23Test` checks them with and without Rook). Event lines drop after 6 s. Rook's lines carry `requires: escort`; his one dry joke is at t=9.

**One new voice**: **Thera Deep Control**, the city's harbour controller, a calm man's voice, cast with the audition step (pending).

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis flight, down the shaft. The hydro-kits are fitted; trust them. Thera Deep is at the bottom." |
| t=9 | Rook | "A shaft, a hydro-kit and a ship that was never meant to swim. Flying a submarine, Lancer." |
| t=17 | Varga | "Water in ten seconds. Everything slows, theirs and yours. Torpedoes keep their bite; the rest lose a third." |
| Waterline crossed (`waterline`, t≈25.5) | Varga | "Under the ice. Airstrike and Lance are dead weight now. A Sonar Pulse lights the water." |
| t=34 | Generic colonial (Thera Deep Control) | "Aegis, Thera Deep Control. We see you on the shaft. The farms are below you, and so is whatever is draining them." |
| t=41 | Varga | "Eel chain. Cut the head and the body dies; cut the body and the tail scatters." |
| t=70 | Generic colonial (Thera Deep Control) | "Those are our farms, Aegis. Forty thousand people eat from those racks." |
| t=77 | Varga | "Siphon pumps on the racks: they drain the farms into the Vrell. Shoot the pumps, not the racks." |
| Secondary met (`secondary-objective`) | Generic colonial (Thera Deep Control) | "That is the fourth pump off the racks. Thera Deep owes you a dinner, Aegis." |
| t=134 | Okafor | "The farms are clear. Beyond the lamps is the open plain. Hold your lights, Aegis." |
| Level end | Okafor | "Good work. Thera Deep is breathing again. The Vrell's road leads out across the plain." |

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
siphon pump 20, the crate 100, the secondary 100); the code pays each × the act factor **4.096**, bounties also × the scale. A **`bounty_scale` of
0.45** (the model of the earlier Act 3 levels: typical = 0.663 of the kills, 0.8 of the ground targets, half of the secret and secondary; a first
estimate for part C to confirm with `TypicalHaulTest` and `levelReport`) puts the typical haul at **3,087 (-0.5 %)**, the perfect run at 4,754. No boss or mid-boss.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Glassfin 238 × 2 + Driftjelly 31 × 10 + Lamprey 30 × 12 + Eel Swarm 10 × 48 + Stinger 9 × 15 + Needler 6 × 12 + Mote Swarm 6 × 2 + Skitter 4 × 5 (Eel Swarm whole, 48) | 3,438 | 2,279 |
| Ground targets: Spine Turret 6 × 12 + Reef Spitter 7 × 14 + Siphon Pump 5 × 20 | 498 | 398 |
| Secret: survey probe (hidden crate, 7 % of the typical haul) | 410 | 205 |
| Secondary: pumps out (4 of 5 siphon pumps) | 410 | 205 |
| **Total** (bounty scale 0.45) | **4,754** | **3,087** |
| Budget(n) = the typical haul's target; typical -0.5 %, perfect 1.53 × budget | | 3,101 |

**Balance plan**: the plan's L23 visit (3 418 cash plus 900 from selling the Tail Gun L3) buys Depth Charges 1 300 + L2 400 + L3 800 and the Harpoon L3 1 000
([generator](../../../player/generator/README.md#act-4-hand-plan-draft)); a loadout without `anti-sub` is flyable but does 70 % of its damage.

## Difficulty notes

- **Easy**: the eel has 10 segments and no fan, the schools are 12 (base 16 reduced by the formation lever), the Driftjelly rings are smaller; a pump needs the same hits; the secondary needs 3 of 5; the wall touch costs 2.
- **Hard**: the eel has 16 segments and its aimed shot every 1.4 s; schools of 22 where the table says 16, 40 % of a school dives; the secondary needs all 5; the other waves by the formation lever, trimmed by hand for at most ~3.5 enemies on the screen.

## Concept art

No art yet. Round 48 proposals: the descent and the Thera Deep mood stills, the under-water grade (a/b), the hydro-kit Stormhawk (step A-europa), the Glassfin and Eel Swarm (step A-units); the production
backdrop (the shaft, the farms), the shaft's wall art and the briefing images come in part C. See [europa](../../../world/europa/README.md#concept-art) and [naval](../../../enemies/naval/README.md#concept-art).

## Implementation

- [ ] Level data `data.yaml` (M8 part C): sections and speeds with 2 s eases, the medium switch at the waterline, waves, ground targets, shaft terrain, radio, budget, threat profile, briefing, backdrop, music
- [ ] The shaft's `Terrain` (walls 480 → 320 → 480, ledges with nests, contact 4 and a push) and the section `medium: underwater` with the waterline ease (part A's rules)
- [ ] The Glassfin schools and the Eel Swarm chain as specified; the Driftjelly, Reef Spitter and Lamprey under water (the returning-unit notes in [naval](../../../enemies/naval/README.md#returning-units-under-water))
- [ ] The siphon pumps (level-local ground target) and the secondary `destroy-targets` (4 of 5, `PUMPS` counter)
- [ ] The special limits from the waterline (`UNDER ICE` on the Airstrike and the Lance, the intel's `SPECIAL LIMITED` warning); the Sonar Pulse and the Harpoon usable from the waterline
- [ ] The secret (survey probe), the Thera Deep tender's armour patch, the overdrive and the shield-cell drops
- [ ] Wave script: ≥ 60 enemies a minute at medium, no empty screen over 2 s, the Act 2+ floor, the quiet windows (`DensityTest`, `PacingTest`, `ActFloorTest`)
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel23Test`, with Rook and without), Thera Deep Control cast
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`); the Glassfin's and Eel Swarm's portraits
- [ ] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(23) = 3 101 with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music and ambience: the stems by section, the under-water bed, the waterline and the school/eel sounds (`Level23SoundsTest`)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on every difficulty (`--escort none` too)
- [ ] Production art and captures for part C's round

## Open questions

- Should the waterline be a full set piece (a 3 s quiet with the hydro-kit refit shown) or just the switch? (Our reading: a short quiet window and a tell, as drafted.)
- Is the siphon pump enough of a secondary, or should the Thera Deep farms be an ally like Level 17's domes? (Our reading: pumps, to keep the new mechanics to the water.)

## Decisions

- 2026-10-21 (Act 4 design phase, step `m8-dl2224`; **all decided in round 48 (2026-10-22)**): promoted from the act roster to a
  draft level document, hand tables only, no `data.yaml`. The play plane stays `air` under water (`ground` = the sea floor, `sub` = below); the
  waterline is a medium switch inside the level (the shaft is a `Terrain`); the pumps (not an ally) are the secondary; Thera Deep Control is the
  new voice; numbers from the proposed reference DPS 100.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
