---
title: Level 24 – Abyssal Plain
design: approved
implementation: in-progress
art: final
depends-on: [../../../enemies/naval/abyss-ray, ../../../enemies/naval/barbfin, ../../../enemies/ground/spawning-reef, ../../../enemies/naval/glassfin, ../../../enemies/naval/eel-swarm, ../../../enemies/ground/scuttler, ../../../player/weapons/harpoon-torpedoes, ../../../world/europa]
updated: 2026-10-24
---

# Level 24 – Abyssal Plain

## Summary

Beyond Thera Deep's last lamps lies the ocean floor: a wide, open plain with Vrell **spawning reefs** growing out of it, breeding
fry from irises. The level introduces the [Abyss Ray](../../../enemies/naval/abyss-ray/README.md), a huge manta that rises out of the
black water *below* the ship (a shadow first, then the ray, then three wide fans), and its escort, the
[Barbfin](../../../enemies/naval/barbfin/README.md), the act's small firing swimmer; [Scuttlers](../../../enemies/ground/scuttler/README.md) walk
the sea floor. Along the way the wing passes a line of **human-built relay beacons** pulsing red-gold: the first thing the act asks the
player to wonder about. 172 s: the plain's edge, the reef field, the beacon line, the Ray crossing.

**Status: approved in round 48 (2026-10-22); in the build (M8 part D).** Drafted in the Act 4 design phase (2026-10-21,
step `m8-dl2224`) from the act roster, the gap analysis's recommended answers (Q1 = b, Q2 = a: the lamps thin out so the
water darkens, Q3 = a, Q11 = a: beacons pulse red-gold) and the wave 1 documents. The level's numbers live in its
[data.yaml](data.yaml) (step D2) and the briefing, threat profile, layout, waves, ground targets, radio and credit tables
below are rendered from it; the counts are measured in part D (`levelReport`, step D3).

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (approved in round 48; Varga's page
trimmed by a few words in step D2 to fit one screen, accepted in round 53 (2026-10-23)):

<!-- data: briefing -->
> **Commander Okafor:** "Past the farms is the abyssal plain, and the Vrell are growing reefs on
> it: living nurseries that breed fry. Burn them down. Thera Deep's lamps end ahead, so you fly
> on your own lights."
>
> **Dr. Varga:** "Two new forms: a small swimmer with a barbed snout that holds and spits
> needles, and a manta the size of a cruiser that rises from the dark below you. Torpedoes reach
> it while it is a shadow. And there are human relay beacons out there. I want a closer look."
<!-- /data -->

*Images* (briefs only tonight; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-24-abyssal-plain`: a sonar-style map of the plain: Thera Deep's lamps at the north, four red reef
  clusters, three dashed beacon marks in a line, the route in teal.
- Varga's page `level-24-abyss-ray`: her scan of an Abyss Ray in profile with its shadow below the ship's plane, a Barbfin inset, the
  gill slits marked.

*Hangar teaser* (shop screen before L24; Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "Open water, Lancer. Spread for the fry, torpedoes for the big one."
<!-- /data -->

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule). At sensor L3 the wave
strip marks the three Rays (`Intel` `RAY`, each Ray's wave) and the four reefs (the secondary's targets).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (Glassfin schools, Barbfins, Eel Swarms, Driftjellies, Lampreys), `sub` (the Abyss Ray's shadow), `ground` (Scuttlers, Reef Spitters, spawning reefs) |
| Attack directions | front 100% (the Ray rises from below the ship's lane) |
| Density | 4 |
| Recommended traits | `anti-sub`, `spread` |
| Hazards | The Ray's fans; the reefs' fry; water (bullets × 0.7, drag) |
| Boss / mid-boss | none (the Abyss Ray is a large set-piece unit, not a mid-boss) |
| Sensor-suite detail | none: Europa, ocean floor, `air` + `ground` + `sub`, front · L1: + directions, density 4, hazard "Ray fans", the objective `REACH THE BEACONS` and the secondary `REEFS RAZED` · L2: + Abyss Ray (new), Barbfin (new), Scuttler, Glassfin, Eel Swarm, Driftjelly, Lamprey portraits · L3: + `anti-sub` and `spread` highlighted, the wave strip marking the three Rays |
<!-- /data -->

*Varga's intel line* per sensor-suite level (they must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "The abyssal plain, Lancer. Beyond Thera Deep's lamps." |
| L1 | "All from ahead. A shadow on the floor means a Ray is rising." |
| L2 | "New: a needle swimmer and a manta that rises from below." |
| L3 | "Spread for the fry, torpedoes for the Ray's shadow." |

## Objective

- **Primary** `reach-end`: reach the end of the plain, where the trench's mouth begins (the beacons point to it).
- **Secondary** `destroy-targets` (counted): destroy at least **3 of the 4 spawning reefs** before they leave the screen (the data's
  `escapes: spawning-reef` with `allowed: 1`, the HUD counter `REEFS`), met when the third falls (easy: 2 of 4, `allowed: 2`; hard: 3 of 4 as
  medium, round 53 C.1). Pays **100** credits in Act 1 terms
  (410 in Act 4); the typical haul counts 50 %. A reef is a soft (non-hardened) `ground` target: the torpedo weapons hit it at full
  damage, other weapons at × 0.7; razing it also stops its fry (no new releases; the fry already out live on, gap check D4). The
  reefs are not hardened, so the harpoon loadout of the plan can raze them (approved in round 48); the reef is its own part,
  [spawning-reef](../../../enemies/ground/spawning-reef/README.md) (gap check D1).

## Layout

Scroll **100 px/s** at the plain's edge and along the beacon line, **80 px/s** through the reef field (a reef is on the screen about 9 s),
**90 px/s** in the Ray crossing. Each change eased over 2 s. The look follows round 48 Q2 a: the lamp-lit r07 water of Thera Deep at the
top of the level, **darkening** as the farms' lamps fall behind (only the ship's headlights, the Vrell reefs' teal glow and the beacons' red-gold
pulse by the middle), the plain a wide silt floor with ridges, kelp stands, reef towers and the odd wreck. The ice ceiling is far above
(`high-air`, a faint glow of light cracks). Motion budget: the reefs' iris glow, the Rays' shadows and the beacons' pulse are the strong elements.

The whole level is under water (the level's `medium: underwater`): no waterline, the special limits (`UNDER ICE`) from
the start. The grade darkens by section (the per-section `underwater_look`: ambient 0.50, 0.32, 0.18, 0.14, the caustics
thinning, the headlights up; gap check D10, ART-D's proposal for round 53), eased over each change's 2 s.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Plain's Edge | 0–30 | 0–3,000 | 100 | clear | `ground`: silt floor at Thera Deep's edge, the last grow-lamp pylons (t≈4, 12, 22), kelp stands, a Reef Spitter nest on a ridge (t≈18). `high-air`: the ice ceiling far above (a faint glow of light cracks). | The first Barbfins (t=6) alone; popcorn; the first Eel; two Scuttlers walking in (t=30). |
| 2. The Reef Field | 30–95 | 3,000–8,220 | 80 | clear | `ground`: four **spawning reefs** (t=36, 48, 64, 80; x 120, 350, 130, 340), coral towers with a teal iris, Reef Spitter nests round the middle two; ridges; the survey sub's wreck lies ahead (the secret). `high-air`: the ice ceiling. | The reefs' fry, the first Abyss Ray (t=58, over the second and third reef), Scuttlers on the floor. |
| 3. The Beacon Line | 95–135 | 8,220–12,200 | 100 | clear | `ground`: three **relay beacons** (t=100 x 60, t=112 x 420, t=126 x 100): tall grey masts with an amber-red lamp pulsing every 2 s, human cable-laid, no Vrell growth within 100 px (scenery, not hit by shots); the survey sub's wreck at t≈105. `high-air`: the ice ceiling. | The beacon beat (radio), the second Ray (t=112), a Scuttler convoy along the beacon line. |
| 4. The Ray Crossing | 135–172 | 12,200–15,540 | 90 | clear | `ground`: a wide, empty silt floor with the trench's mouth at the bottom edge; the last lamp-free stretch. `high-air`: the ice ceiling. | The third Ray (t=150), the last eel and walkers; level end as the trench's mouth reaches the bottom edge. |
<!-- /data -->

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with
a `bounty_scale` (see *Credit budget*). Measured (step D3, `levelReport -Plevel=24`, the plan's fit): 281 enemies at
medium with the reefs' first releases (101 a minute), 6.0 on the screen and 31 shots a minute, one stretch over 2 s
(the first Barbfins' quiet window); easy 3.4 on the screen, hard 9.6. **M8 AW** (step AW2, pending the user's review in
round 56): the faster harpoon (2.8 a second at L3, its splash 44 px) cleared the Barbfins in about a second, before they
fired (small units 0.68× Level 10's time on the screen, four stretches over 2 s): the [Barbfin](../../../enemies/naval/barbfin/README.md)
26 HP (52 here, two L3 hits), one more Barbfin in every group but the first and the Rays' escorts, the t=12 column at
t=10 and, at the ends of the Rays' quiet windows, the t=63 ribbon at t=62 and the t=156 Scuttlers at t=154. Measured:
276 enemies, 3.4 on the screen, 16.5 shots a minute, the small units 1.51 s (0.94×), three stretches over 2 s (easy two
over 3 s, hard none), won 3/3 and REEFS 3/3 on every difficulty.

- **Act HP factor** ([balancing basis](../../../enemies/README.md#balancing-basis)) from the proposed
  reference DPS 104 at L24: **Scuttler** (L04, 38) ×2.74 → 77 HP; **Driftjelly** 8, **Reef Spitter** 14, **Lamprey** 6 (the `small` units take the beam-era ×2 only); new units at their
  specs: **Barbfin** 26 → 52 (beam-era ×2 from the level number; M8 AW: 12 → 24 before), **Abyss Ray** 210 (2.0 s of the reference), **Eel Swarm** (the L23 spec ×1.04: head 104,
  whole about 255), **Glassfin** 1; the **spawning reef** is 200 HP (about 1.9 s). Re-measured in part D under the beam's halved reach (`BeamEraHpTest`).
- **Density and pacing** (§1.7: drafted to the Act 2+ floor): **276 enemies** (waves 266 + ground targets 10; 265 before M8 AW) over the 172 s,
  **≈ 92 a minute** at medium (≥ 60; the reefs' fry add more, not counted); a wave about every 3 s from t=3, **front 100 %**. The **firing groups** carry the floor: Barbfin
  columns and circles (the act's shot source: a group of 4 fires about 100 shots in 60 s), Eel Swarm heads, Driftjelly rings, Lamprey streams, the Reef Spitter
  nests and the Rays' fans (9-way, three a visit). Expected to meet ≥ 2.5 enemies on the screen and ≥ 10 shots a minute, **to be confirmed with `levelReport`** in part D
  (bullets × 0.7 stay longer on the screen, so fewer groups may do). The new units get **quiet windows**: no other wave enters t=6–9.5 (the first Barbfins), and t=55–62, 109–116, 147–154 around each Ray (3 s before the rise to 4 s after it
  starts; the Barbfin escort is the Ray's own wave). **Never more than one Ray on the screen** (a check of the data: a Ray's visit is 8.3 s); the three Rays are 54 s and 38 s apart (the unit spec's "once a minute", tightened by the act's 172 s).

The data's waves (step D2; measured in step D3). Each school (`swarm`) flies an S-shaped route over the open plain
(x 140–340); a ribbon (`column`) is a straight column down the left (x 144) or the right side (x 336); the Barbfins'
columns stop and hold above the ship's lane, their circles orbit for their 9 s hold; each Ray's wave is the Ray and its
four Barbfins (held until its rise, then fanned out). The two eels of a pair are two rows 4 s apart (t=78 and 82, 128 and
132), each on the other's mirrored route; the README's `pair` of walkers (t=90, 156) is a `single` of two on their own
paths (the formation vocabulary has no pair). The walkers keep 100 px from the beacons (a check of the data).

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front | |
| 6 | 1 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | **New.** Three Barbfins hold and spit needles; Varga's line at t=13, after Okafor's opening; alone until t=9.5 |
| 10 | 1 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 15 | 1 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 18 | 1 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) | |
| 20 | 1 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | Solo; the Barbfins' first holders are gone |
| 24 | 1 | circle | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 27 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front | |
| 30 | 2 | single | [Scuttler](../../../enemies/ground/scuttler/README.md) | 2 | front | Two walkers on the sea floor (`ground`) |
| 33 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 36 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) | |
| 39 | 2 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (left) | |
| 42 | 2 | circle | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 44 | 2 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | Over the first spawning reef |
| 48 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 51 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 54 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) | |
| 58 | 2 | carrier + escorts | [Abyss Ray](../../../enemies/naval/abyss-ray/README.md) + [Barbfin](../../../enemies/naval/barbfin/README.md) | 1 + 4 | front | **New.** The first Ray: a shadow grows in the ship's lane ahead of it for 1.5 s, the Ray rises, fans three times and glides off; the four Barbfins peel off its shadow; Varga's line at its first shadow (`first-ray`); no wave enters until t=62; it drops an overdrive when it dies |
| 62 | 2 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (right) | |
| 66 | 2 | convoy | [Scuttler](../../../enemies/ground/scuttler/README.md) | 3 | front | Three walkers 3 s apart down the reefs' lane |
| 69 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front | |
| 72 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 75 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 78 | 2 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The first of two eels staggered 4 s apart |
| 81 | 2 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (left) | |
| 82 | 2 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The second of the pair, on the mirrored route |
| 84 | 2 | circle | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 87 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front | |
| 90 | 2 | single | [Scuttler](../../../enemies/ground/scuttler/README.md) | 2 | front | Past the last reef |
| 93 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 96 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) | |
| 100 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | Under the first beacon |
| 102 | 3 | circle | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 105 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front | |
| 108 | 3 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 112 | 3 | carrier + escorts | [Abyss Ray](../../../enemies/naval/abyss-ray/README.md) + [Barbfin](../../../enemies/naval/barbfin/README.md) | 1 + 4 | front | The second Ray, over the beacon line: its fans cross the beacons' pulse; no wave enters until t=116; it drops a shield cell when it dies |
| 117 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 120 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) | |
| 124 | 3 | convoy | [Scuttler](../../../enemies/ground/scuttler/README.md) | 3 | front | Along the beacon line's floor |
| 126 | 3 | circle | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 128 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The first of two eels staggered 4 s apart |
| 132 | 3 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 132 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The second of the pair, on the mirrored route |
| 135 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 138 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) | |
| 141 | 4 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (right) | |
| 144 | 4 | circle | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 150 | 4 | carrier + escorts | [Abyss Ray](../../../enemies/naval/abyss-ray/README.md) + [Barbfin](../../../enemies/naval/barbfin/README.md) | 1 + 4 | front | The last Ray; the Barbfins stay when it sinks; no wave enters until t=154 |
| 154 | 4 | single | [Scuttler](../../../enemies/ground/scuttler/README.md) | 2 | front | The last walkers |
| 160 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The last eel |
| 162 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | |
| 165 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) | |
| 168 | 4 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (left) | The last wave |

Totals: Glassfin 120 · Barbfin 70 · Driftjelly 32 · Lamprey 21 · Eel Swarm 8 · Scuttler 12 · Abyss Ray 3.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 1 | [Reef Spitter](../../../enemies/naval/reef-spitter/README.md) nest ×2 on a ridge (t≈18) | Bounty 14 each |
| 2 | **[Spawning reefs](../../../enemies/ground/spawning-reef/README.md)** ×4 (t=36, 48, 64, 80; x 120, 350, 130, 340): a coral tower with an iris that releases 4 Glassfin every 3.5 s while it is on the screen, 200 HP, **not hardened** | The secondary counts them; bounty 40 each; razed: the iris closes, a coral burst |
| 2 | [Reef Spitter](../../../enemies/naval/reef-spitter/README.md) nests ×2 round the second and third reef (t≈50, 66) | Bounty 14 each |
| 3 | Wrecked survey sub *Meridian* (`ground` trigger, 20 HP, t≈105, x 70) | The secret: a crate worth 100 (410) |
| 3 | [Reef Spitter](../../../enemies/naval/reef-spitter/README.md) nest ×2 at the foot of the second beacon (t≈114), 200 px below it | Bounty 14 each |
<!-- /data -->

The **relay beacons** are scenery: shots pass over them, nothing is hooked on them, no ground target lies within 100 px of one (the Helix compound's rules of
[Level 17](../../act-3-red-dust/level-17-dome-siege/README.md)). The beacons are animated ground pieces of the backdrop (a 2 s pulse loop), each with `keep_clear: 100`: the content
check rejects a ground target or a walker's walk start within 100 px of one (gap check D6, D7); the nests "at the foot of
the second beacon" lie about 200 px below it on the screen (t≈114). The **spawning reef** is its own part,
[spawning-reef](../../../enemies/ground/spawning-reef/README.md) (non-hardened, 200 HP, the Hive Node's iris rule
softened for the water; gap check D1).

## Hazards

- **The Abyss Ray's fans**: three 9-way fans (110°, bullets × 0.7), centred on the ship at the moment of firing; the rise's shadow is a tell, not a hazard.
- **The reefs' fry**: Glassfin released while a reef lives, each ramming on its own; a razed reef releases no more (the fry already out live on, gap check D4).
- **The water**: bullets × 0.7, acceleration × 0.6, non-`anti-sub` damage × 0.7, torpedo range +50 %; the Pressure hull removes the drag.
- No terrain collision; no currents.

## Secrets and pickups

- **Wrecked survey sub *Meridian*** (t≈105, x 70, a Thera Deep survey vessel lost with its crew, lying at the left edge of the beacon line): a `ground` trigger of **20 HP**;
  destroyed, its hatch lets go and a hidden crate floats up worth **100** in Act 1 terms (410 in Act 4). The Salvage scanner's glint marks it. Varga's line: "*That's the
  Meridian. Thera Deep lost her to the dark, not to the Vrell. Take what she kept.*" (an event line, dropped after 6 s if the radio is busy).
- **Overdrive**: dropped by the first Ray (t=58) when it dies (`dropped_by: {wave: 58, enemy: abyss-ray}`).
- **Armour patch**: a Thera Deep tender releases one at (240, 380 y-up) at t=95.
- **Shield cell**: dropped by the second Ray (t=112) when it dies.
- An escaping Ray drops nothing: the plan's harpoons can reach its shadow (the autopilot's shadow targeting, step D3).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), retimed to the HUD's **1 s rule** (at least 6 s between lines; the build's
`RadioTimelineLevel24Test` checks them with and without Rook). Event lines drop after 6 s. Rook's lines carry `requires: escort`; his one dry joke is at t=14. Okafor's t=106 line is the
first sign that she listens to Varga after Act 3 (L16–17: "Not on this channel, Doctor."): she asks for a **private record**, which is not yet agreement.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Past the farms is the abyssal plain, Aegis. Thera Deep's lamps end ahead; from here you fly on your own lights." |
| t=13 | Varga | "Small swimmers, needles aimed at you. They hold and shoot: take them one by one." |
| t=24 | Rook | "Dark's coming, Lancer. Rule one in a submarine: the ceiling is lower than it looks." |
| t=35 | Varga | "Spawning reefs. They breed fry from the iris, and they are soft: torpedoes will do." |
| A Ray's first shadow (`first-ray`, t=58) | Varga | "Something huge under you. A shadow first, then a manta. Torpedoes reach it now; the rest must wait." |
| t=98 | Varga | "A beacon. Human-made, relay hardware, an amber pulse. And the carrier on it is Helix's." |
| t=109.5 | Okafor | "Log it, Doctor. Full record, for my eyes only." |
| t=120 | Rook | "Ray at six, Lancer: it is rising!" |
| Secondary met (`secondary-objective`) | Okafor | "Third reef down. The Vrell will have to breed somewhere else."; easy: "Second reef down. The Vrell will have to breed somewhere else." |
| t=131 | Varga | "A third beacon. The same pulse every two kilometres. Somebody laid a road down here." |
| Level end | Okafor | "The plain is behind us. The beacons lead into the trench, and so do we. Refit, Aegis." |
<!-- /data -->

The secret's line (the wreck's hatch opened): Varga, "That's the Meridian. Thera Deep lost her to the dark, not to the Vrell. Take what she kept." Retimed in step D2 to the HUD's real hold (a two-page line holds the radio about 11 s, not 6): Okafor's opening holds it
until t≈12.7, so Varga's Barbfin line moves from t=5 to t=13 (gap check D8 had t=7), Rook's t=14 to t=24, Varga's reef
line from t=34 to t=35 (before the first reef at t=36) and Okafor's t=106 to t=109.5 (after Varga's t=98 line); each
starts at most 1 s late with and without Rook (`RadioTimelineLevel24Test`, accepted in round 53 (2026-10-23)). The
`first-ray` line plays at the first Ray's shadow, its wave's time (t=58; the approved "t≈57.6" is the same beat, gap
check D2). The secondary's line counts true on every difficulty: "Third reef down" at medium, "Second reef down" on easy,
"Third reef down" on hard too (round 53 C.1: hard allows one reef away, as medium; its `easy` line). Varga's t=98 and t=131 lines are the act's beacon beats
(L24–L26: Varga decodes Helix frequencies), at no more than one a level so the reveal keeps its weight; Rook's t=120 bark may be dropped by the 1 s rule (pending the build).

## Music & ambience

The Act 4 A theme **"Thera Deep"** ([track list](../../../audio/music/README.md#track-list), track 10): the under-water bed alone at the plain's edge, the **base stem from section 2**
(the reef field), the **full mix from section 3** (the beacon line, with the beacons' pulse as a faint sonar ping layered on the beat), the base stem again in section 4 (the data's `stems: {4: base}`; the beacons' ping is the beacon-pulse sound at low gain while a beacon is on the screen, no stem, gap check D6). Round 48 Q12 a:
no bubble layer. New sounds: the Ray's rising horn (the shadow's tell: a low, heavy swell), its fans, the Barbfin's needle, the reef's iris and its collapse, the Scuttler's walk on silt, the beacon's pulse; listed for
the act's sfx step ([sfx](../../../audio/sfx/README.md)); real-thing sounds from Freesound only, none bubbly or popping.

## Credit budget

Budget(24) = 700 × 1.07²³ ≈ **3,318**, the typical haul's target
([economy](../../../systems/economy/README.md#per-level-budget)). The data keeps **Act 1 terms**
(Glassfin 2, Barbfin 12, Eel Swarm 48 whole, Abyss Ray 60, Driftjelly 10, Lamprey 12, Scuttler 25, Reef Spitter 14, spawning reef 40, the crate 100, the secondary 100); the code pays each × the act factor
**4.096**, bounties also × the scale. A **`bounty_scale` of 0.40** (M8 AW, step AW2: refitted for the extra Barbfins; 0.42 before; the model of the earlier Act 3 levels: typical = 0.663 of the kills, 0.8 of the ground targets, half of the secret and secondary;
fitted in step D3 with `TypicalHaulTest` and `levelReport`, the first estimate 0.40 being 3.9 % under) puts the typical haul on budget(24). No boss or mid-boss; the reefs' fry pay as Glassfin (not in the table).

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Glassfin 120 × 2 + Barbfin 70 × 12 + Driftjelly 32 × 10 + Lamprey 21 × 12 + Eel Swarm 8 × 60 by parts (48 whole) + Scuttler 12 × 25 + Abyss Ray 3 × 60 + released Glassfin 16 × 2 | 4,286 | 2,691 |
| Ground targets: Reef Spitter 6 × 14 + Spawning Reef 4 × 40 | 402 | 322 |
| Secret: Meridian (hidden crate, 3% of budget) | 410 | 205 |
| Secondary: raze 3 of the 4 spawning reefs | 410 | 205 |
| **Total** (bounty scale 0.4) | **5,508** | **3,422** |
| Budget(n) = the typical haul's target; typical +3 %, perfect 1.66 × budget | | 3,318 |
<!-- /data -->

**Balance plan**: the plan's L24 visit (4 104 cash) buys the Pressure hull 2 000 and one wing pod's Micro-missiles L3 800
([generator](../../../player/generator/README.md#act-4-hand-plan-draft)); the level expects `anti-sub` and `spread` and is flyable without either.

## Difficulty notes

- **Easy**: the Ray has a 7-way fan and two fans; the Barbfin shoots every 3 s; the eel has 10 segments and no fan; the secondary needs 2 of 4 reefs; the reefs release 3 fry.
- **Hard**: the Ray has an 11-way fan and four fans; the Barbfin fires a 2-needle burst every 2.6 s; the eel has 16 segments; the secondary needs 3 of 4 reefs, as medium (round 53 C.1); the other waves by the formation lever, trimmed by hand for at most ~3.5 enemies on the screen.

## Concept art

Production art straight away (M8 part D, step ART-D), from the round 48 picks: the Abyssal Plain mood still and the
under-water grade are in [europa](../../../world/europa/README.md#concept-art), the Abyss Ray's and the Barbfin's frames in
[naval](../../../enemies/naval/README.md#concept-art), the spawning reef's two looks (a/b) in
[ground](../../../enemies/ground/README.md#concept-art), the Barbfin's violet needle in
[enemies](../../../enemies/README.md#concept-art), the two briefing images on the
[briefing review sheet](../../../ui/briefing/README.md#concept-art) and the intel portraits in
[hangar](../../../ui/hangar/README.md#concept-art). Prompts in [concept/prompts.md](concept/prompts.md);
[concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) is the generator's `backdrop:` block (`tools/art/backdrop_l24.py
--proposal`), merged into [data.yaml](data.yaml) in step D2, as Levels 22 and 23's. All pending the user's review in
[round 53](../../../concept-rounds/round-53/README.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r53-a.png](concept/backdrop-final-r53-a.png) | Production backdrop (`tools/art/backdrop_l24.py`, from europa-abyssal-plain-r48-a): four plain tile sets darkening by section, the ice ceiling, Thera Deep's last pylons with baked pools, the ridge, dead coral stacks, the Meridian's scour, three relay beacons (16-frame pulse plus an additive glow) on cable-trenched footings, the trench's mouth; ten screens under the proposed grade ladder 0.50/0.32/0.18/0.14 (sheet) | chosen (round 53, F.1: accepted as final) |
| [concept/backdrop-final-r53-a.gif](concept/backdrop-final-r53-a.gif) | t 97–107 at half size: the first beacon's pulse in the dark | chosen (round 53, F.1: accepted as final) |
| [concept/l24-props-final-r53-a.png](concept/l24-props-final-r53-a.png) | The level's props (`tools/art/l24_props.py`): the relay beacon and its glow, a grow-lamp pylon, the Meridian wreck (lying, hit, opened) with its lamp and spark glow, the Scuttler's silt puff (sheet) | chosen (round 53, F.5: accepted as final) |
| [concept/l24-props-final-r53-a.gif](concept/l24-props-final-r53-a.gif) | A beacon pulsing, the wreck shot open and its crate rising, a Scuttler striding with silt puffs | chosen (round 53, F.5: accepted as final) |
| [concept/plains-edge-capture-r53-a.png](concept/plains-edge-capture-r53-a.png) | Game capture, M8 part D (step D5; Level 24 on medium, `--invulnerable`, the balance plan's Level 24 fit: Harpoon Torpedoes L3, Depth Charges L3, Micro-Missile Pods L3/L2, the Pressure hull, the Sonar Pulse, Rook along): the plain's edge at t=6, 13.5 and 23.5, the three grow-lamp pylons' pools of light on the silt, an Eel Swarm in the last frame | chosen (round 53, F.1: accepted as final) |
| [concept/beacon-line-capture-r53-a.png](concept/beacon-line-capture-r53-a.png) | The beacon line at its pulses, t=102, 113.95 and 128: the first beacon (x 60), the second (x 420) pulsing beside the second Ray's rise, the third (x 100) | chosen (round 53, F.5: accepted as final) |
| [concept/wreck-capture-r53-a.png](concept/wreck-capture-r53-a.png) | The *Meridian* (2× detail of the left lane; the ship steered under it) at t=106, 107, 107.5 and 109: the wreck lying, hit, the crate let go and rising off it | chosen (round 53, F.5: accepted as final) |
| [concept/dark-crossing-capture-r53-a.png](concept/dark-crossing-capture-r53-a.png) | The Ray crossing at t=146, 151.5, 154 and 170: the dark plain lit only by the headlights, the third Ray's shadow taking a harpoon, its fan, the darker run-in to the trench's mouth | chosen (round 53, F.1: accepted as final) |

## Implementation

- [x] Level data `data.yaml` (M8 part D, step D2): sections and speeds with 2 s eases, the whole level under water with the grade by section, waves, ground targets, the wreck's trigger, pickups, radio, budget, threat profile, briefing, backdrop (ART-D's `--proposal` merged, the beacons kept clear), music; the `bounty_scale` fitted in step D3
- [x] The Abyss Ray as specified (the shadow tell, the `sub` → `air` flip at the rise's midpoint, three fans, the glide off) and its Barbfin escort ([Abyss Ray](../../../enemies/naval/abyss-ray/README.md), [Barbfin](../../../enemies/naval/barbfin/README.md)); never more than one Ray on the screen
- [x] The spawning reefs (non-hardened ground spawners, their own part) and the secondary (3 of 4, `REEFS` counter); a razed reef releases no more fry
- [x] The Scuttler walking the sea floor under water; the Driftjelly, Reef Spitter and Lamprey under water
- [x] The relay beacons as set pieces (scenery, red-gold pulse every 2 s, clear of every target and wave route), the beacon radio beat
      (`keep_clear` in the content check, steps D2 and D3; the pulse and its ungraded glow: `Level24LooksTest.theBeaconsLightGoesWithThem`,
      the ping: `Level24SoundsTest.theBeaconsAreHeardWhileOnTheScreen`, step D4)
- [x] The secret (wrecked survey sub), the tender's armour patch, the Ray drops (the carrier check: `unit: first` of a Ray's wave is the Ray)
- [x] Wave script: ≥ 60 enemies a minute at medium, no empty screen over 2 s, the Act 2+ floor, the quiet windows (`DensityTest`, `PacingTest`, `ActFloorTest`)
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel24Test`, with Rook and without); Rook's lines `requires: escort`
      (steps VOX-D and D4: `VoiceFilesTest`, `RadioTimelineLevel24Test`, `Level24Test.theFirstRayLinePlaysAtItsShadow`)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`, its
      `level24sStripMarksTheThreeRaysAndTheReefs`); the Abyss Ray's and Barbfin's portraits (`PortraitsTest.level24sNewUnitsHaveTheirSensorPortraits`)
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images (`BriefingLayoutTest.everyPageImageIsInTheAssetsAtItsSize`, `BriefingsTest`)
- [x] Typical haul within ±5 % of budget(24) = 3 318 with the level's `bounty_scale` (`TypicalHaulTest`)
- [x] Music and ambience: the stems by section, the beacon ping layer, the Ray, reef and Barbfin sounds (`Level24SoundsTest`;
      round 53's picks in the game since step E5; step D4)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on every difficulty (`--escort none` too)
- [x] Production art and captures for part D's round ([round 53](../../../concept-rounds/round-53/README.md), step D5; the
      looks: `Level24LooksTest`)

## Decisions

- 2026-10-21 (Act 4 design phase, step `m8-dl2224`; **all decided in round 48 (2026-10-22)**): promoted from the act roster to a
  draft level document, hand tables only, no `data.yaml`. The Ray rises from `sub` and flips to `air` (the play plane stays `air`); the spawning reefs are
  soft level-local spawners; the beacons are scenery that Varga decodes once or twice a level (Q11 a: grey machines, red-gold beacons); Okafor first asks for a private record;
  numbers from the proposed reference DPS 104.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-22 (M8 part D, step D2; the gap check's defaults, `m8d-gaps.md` §5, accepted in round 53 (2026-10-23)):
  the data file from the approved tables, the hand tables replaced by tables rendered from it, the stray "(Eel Swarm
  whole, 48)" removed. The two open questions closed, settled by round 48's approval (D11): three Rays (J4: all three
  with four Barbfins, D9), the reef not hardened. The spawning reef its own part (D1); the Ray's shadow ahead of the ship
  in its lane, no contact until the rise ends (D2); the escorts held until the rise (D3); the fry as single rammers that
  outlive their reef (D4); the beacons animated scenery kept clear by 100 px (`keep_clear`, D6/D7); the plain darkening
  by section (D10). Step D2's own: the radio retimed to the HUD's real hold (Varga t=13, Rook t=24, Varga t=35, Okafor
  t=109.5; D8's t=7 was not enough); the secondary's line per difficulty; Varga's briefing page trimmed to one screen
  ("Two new forms: …", "a shadow", "human relay beacons"); the schools' and eels' routes, the walkers' paths, the nests'
  x (t≈18 x 150/200, t≈50 x 250, t≈66 x 200, t≈114 x 380/440), the wreck's 80 × 48 size and the beacons' rows; the
  `pair` of walkers as a `single` of two; the eel pairs as two rows 4 s apart; the Barbfin circles' 9 s hold.
- 2026-10-22 (M8 part D, step D3, accepted in round 53 (2026-10-23)): mapped and flown (`AbyssRules`, `Level24Test`).
  `bounty_scale` **0.40 → 0.42** (typical 3,322 vs budget 3,318; perfect 5,481). The `first-ray` line on the
  simulation's first shadow (t=58). The reefs' fry draw their ram as they leave the iris (`ram_after: 0` on the reef's
  spawn: with the school's 3 s none rammed). The Barbfin's hard interval **2.1 → 2.6 s** (the plan's fit without Rook
  lost every retry on hard with 2.1). Measured with the autopilot (`levelReport -Plevel=24`, three seeds, Rook): won
  3/3 on every difficulty without a retry; REEFS met 3/3 on easy and medium, **0/3 on hard** (all four needed: the fry
  absorb the torpedoes, an eel covers the second reef), kept as approved and printed, not asserted. The beam era's small
  units live 1.42 × Level 10's time on the screen (0.9–1.5). The autopilot's play (`AbyssPlay`): the torpedoes into the
  Ray's shadow, the Barbfins before the fry.
- 2026-10-22 (M8 part D, steps ART-D and D4; accepted in round 53 (2026-10-23)): the production backdrop
  (`tools/art/backdrop_l24.py`: the tile sets `lamp-edge`, `plain-reefs`, `beacon-line`, `deep-silt`, the `ice-ceiling`
  high-air, Thera Deep's last pylons with baked lamp pools, the ridge, the coral stacks, the *Meridian*'s scour, three
  relay beacons on cable-trenched footings, the trench's mouth) under the proposed grade ladder (ambient 0.50 / 0.32 /
  0.18 / 0.14 by section, the caustics 0.1 → 0, the headlights 0.6 → 0.9); the props (`tools/art/l24_props.py`: the
  beacon with its 16-frame pulse and an additive glow, a grow-lamp pylon, the wreck lying / hit / opened, the Scuttler's
  silt puff). Step D4 (`PlainLooks`): the beacons' glow drawn ungraded after the grade at their positions on the same
  clock, the wreck's three frames, a walker's silt puff once a stride. Weak spots for the round: the pylons' lamp pools
  read pale, the beacon masts lean a little and are faint at the 0.18 grade, the Ray's body dark at 0.32.
- 2026-10-22 (M8 part D, step D5): captured on medium with `--invulnerable` and the plan's fit (the plain's edge, the
  first Barbfins, a reef's release and razing, the first Ray's shadow, rise, fans and glide with the game's sound, a
  harpoon on its shadow, the beacon line, the *Meridian*, the dark crossing). The frame rate of a full medium run on the
  software renderer (Xvfb, llvmpipe, no capture): **419 fps** on average, the longest frame **46.6 ms**. Seen in the
  captures and shown as weak spots in round 53: the fry hardly read, the reef's razing burst looks like the fry's, a
  reef is on the screen about 6 s (razed only from under it), the Ray's escorts and the first Barbfins die almost as
  they come under auto-fire, the shadow shows no hit of its own, the run-in to the trench's mouth is almost black.
- 2026-10-23 (user, round 53 C.1: "allow 1"): hard's REEFS allows one reef away, as medium (3 of 4; the approved table had
  all 4): with the plan's fit the autopilot never razed all four on hard (the fry soak up the harpoons, an Eel Swarm covers
  the second reef); hard stays harder by HP, fry and fire. Okafor's line is "Third reef down" on hard too (step E3).
  Measured in step E3 (`levelReport -Plevel=24`, the plan's fit, seeds 1–3): the autopilot still razes only two of the
  four on hard (the second reef away by t≈72, the fourth by t≈88), so hard's REEFS stays unmet for it (0/3; easy and
  medium 3/3): open for the user (a hard reef's 260 HP, or the autopilot's reef play).
- 2026-10-23 (step E4, accepted in round 54 (2026-10-23)): the autopilot's reef play closes it without a number
  changed: a reef of more than 230 HP (hard's 260; `WaterPlay.TOUGH_HP`) is shot from its entry, the ship closes in to
  60 px under it (at most 300 px up the screen), where the reef is shut and the harpoons reach it past no fry, and once it
  passes under the ship it holds 50 px above it so the Depth Charges burst on it. Measured (`levelReport -Plevel=24`, the
  plan's fit with Rook): hard won 3/3 with REEFS met 3/3 (8 seeds: 7/8, two retries), so `Level24Test` now asserts
  REEFS on hard too. Easy and medium keep the plain play and their numbers (medium 6.0 on the screen, the small units
  1.42 × Level 10's time): played hard there, the fry stayed on the screen longer (1.55–1.63 ×, over the beam era's
  1.5).
- 2026-10-23 (user, round 53, closed): Level 24's backdrop, props and captures accepted as final (F.1, F.5; the level is
  `art: final`); **C.1 allow 1** (hard's REEFS asks for 3 of 4, as medium, built in step E3), C.2–C.7 accepted (the
  Barbfin's hard interval, the fry's ram, the Ray's glide off the edge and firing only on the field, the radio retimed,
  the density); the steps' logged small rules accepted.
- 2026-10-24 (M8 AW, step AW2; accepted in round 56 (2026-10-24)): the harpoon's rework cleared the level too fast
  (`BeamEraHpTest` 0.68×, `PacingTest` red on every difficulty): the Barbfin's HP 12 → 26 (its stat block: two L3
  harpoon hits; "tougher, not more", the user's rule of 2026-10-19), one Barbfin more in each column and circle (not the
  first three nor the Rays' escorts; its stat block's circle now 4–5), the t=12 column at t=10, the t=63 ribbon at t=62,
  the t=156 Scuttlers at t=154, `bounty_scale` 0.42 → 0.40 (typical +0.0 %). Tried and rejected: the columns as lines
  abreast (0.87×), five per column and circle without the HP (0.69×; six 0.60×: a column dies in one splash), the HP alone
  (0.83×), the Depth Charges kept at L1 (no change).
