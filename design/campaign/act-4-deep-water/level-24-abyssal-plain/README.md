---
title: Level 24 – Abyssal Plain
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/naval/abyss-ray, ../../../enemies/naval/barbfin, ../../../enemies/naval/glassfin, ../../../enemies/naval/eel-swarm, ../../../enemies/ground/scuttler, ../../../player/weapons/harpoon-torpedoes, ../../../world/europa]
updated: 2026-10-22
---

# Level 24 – Abyssal Plain

## Summary

Beyond Thera Deep's last lamps lies the ocean floor: a wide, open plain with Vrell **spawning reefs** growing out of it, breeding
fry from irises. The level introduces the [Abyss Ray](../../../enemies/naval/abyss-ray/README.md), a huge manta that rises out of the
black water *below* the ship (a shadow first, then the ray, then three wide fans), and its escort, the
[Barbfin](../../../enemies/naval/barbfin/README.md), the act's small firing swimmer; [Scuttlers](../../../enemies/ground/scuttler/README.md) walk
the sea floor. Along the way the wing passes a line of **human-built relay beacons** pulsing red-gold: the first thing the act asks the
player to wonder about. 172 s: the plain's edge, the reef field, the beacon line, the Ray crossing.

**Status: approved in round 48 (2026-10-22); drafted (Act 4 design phase, 2026-10-21; step `m8-dl2224`); everything below is pending the
user's review in round 48.** Written from the act roster, the gap analysis's recommended answers (Q1 = b, Q2 = a: the lamps thin out so the water darkens, Q3 = a, Q11 = a: beacons pulse red-gold)
and the wave 1 documents. There is **no `data.yaml`**: the build (M8 part D) turns the hand tables into one. The tables are marked draft; no number is measured.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft):

> **Commander Okafor:** "Past the farms is the abyssal plain, and the Vrell are growing reefs on it: living
> nurseries that breed fry. Burn them down. Thera Deep's lamps end ahead, so you fly on your own lights."
>
> **Dr. Varga:** "Two new forms. A small swimmer with a barbed snout that holds and spits needles, and a manta the size of a
> cruiser that rises from the dark below you. Torpedoes reach it while it is only a shadow. Also: there are relay beacons out
> there. Human ones. I want a closer look."

*Images* (briefs only tonight; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-24-abyssal-plain`: a sonar-style map of the plain: Thera Deep's lamps at the north, four red reef
  clusters, three dashed beacon marks in a line, the route in teal.
- Varga's page `level-24-abyss-ray`: her scan of an Abyss Ray in profile with its shadow below the ship's plane, a Barbfin inset, the
  gill slits marked.

*Hangar teaser* (shop screen before L24; Rook, not voiced):

> **Rook:** "Open water, Lancer. Spread for the fry, torpedoes for the big one."

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `air` (Glassfin schools, Barbfins, Eel Swarms, Driftjellies, Lampreys), `sub` (the Abyss Ray's shadow), `ground` (Scuttlers, Reef Spitters, spawning reefs) |
| Attack directions | front 100 % (the Ray rises from below the ship's lane) |
| Density | 4 |
| Recommended traits | `anti-sub`, `spread` |
| Hazards | The Ray's fans; the reefs' fry; water (bullets × 0.7, drag) |
| Boss / mid-boss | none (the Abyss Ray is a large set-piece unit, not a mid-boss) |
| Sensor-suite detail | none: Europa, ocean floor, `air` + `ground` + `sub`, front · L1: + directions, density 4, hazard "Ray fans", the objective `REACH THE BEACONS` and the secondary `REEFS RAZED` · L2: + Abyss Ray (new), Barbfin (new), Scuttler, Glassfin, Eel Swarm, Driftjelly, Lamprey portraits · L3: + `anti-sub` and `spread` highlighted, the wave strip marking the three Rays |

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
  `escapes: spawning-reef` with `allowed: 1`, the HUD counter `REEFS`), met when the third falls. Pays **100** credits in Act 1 terms
  (410 in Act 4); the typical haul counts 50 %. A reef is a soft (non-hardened) `ground` target: the torpedo weapons hit it at full
  damage, other weapons at × 0.7; razing it also stops its fry. *Pending the user's review*: the reefs are drafted not hardened so the
  harpoon loadout of the plan can raze them (a hardened Hive-Node-like reef would need an `anti-ground` gun).

## Layout

Scroll **100 px/s** at the plain's edge and along the beacon line, **80 px/s** through the reef field (a reef is on the screen about 9 s),
**90 px/s** in the Ray crossing. Each change eased over 2 s. The look follows round 48 Q2 a: the lamp-lit r07 water of Thera Deep at the
top of the level, **darkening** as the farms' lamps fall behind (only the ship's headlights, the Vrell reefs' teal glow and the beacons' red-gold
pulse by the middle), the plain a wide silt floor with ridges, kelp stands, reef towers and the odd wreck. The ice ceiling is far above
(`high-air`, a faint glow of light cracks). Motion budget: the reefs' iris glow, the Rays' shadows and the beacons' pulse are the strong elements.

| Section | t (s) | Scroll (px, approx.) | Speed (px/s) | Medium | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Plain's Edge | 0–30 | 0–3,000 | 100 | water, lamp-lit | `ground`: silt floor at Thera Deep's edge, the last grow-lamp pylons (t≈4, 12, 22), kelp stands, a Reef Spitter nest on a ridge (t≈18). | The first Barbfins (t=6) alone; popcorn; the first Eel; two Scuttlers walking in (t=30). |
| 2. The Reef Field | 30–95 | 3,000–8,200 | 80 | water, dimming | `ground`: four **spawning reefs** (t=36, 48, 64, 80; x 120, 350, 130, 340), coral towers with a teal iris, Reef Spitter nests round the middle two; ridges; the survey sub's wreck lies ahead (the secret). | The reefs' fry, the first Abyss Ray (t=58, over the second and third reef), Scuttlers on the floor. |
| 3. The Beacon Line | 95–135 | 8,200–12,200 | 100 | water, dark | `ground`: three **relay beacons** (t=100 x 60, t=112 x 420, t=126 x 100): tall grey masts with an amber-red lamp pulsing every 2 s, human cable-laid, no Vrell growth within 100 px (scenery, not hit by shots); the survey sub's wreck at t≈105. | The beacon beat (radio), the second Ray (t=112), a Scuttler convoy along the beacon line. |
| 4. The Ray Crossing | 135–172 | 12,200–15,530 | 90 | water, dark | `ground`: a wide, empty silt floor with the trench's mouth at the bottom edge; the last lamp-free stretch. | The third Ray (t=150), the last eel and walkers; level end as the trench's mouth reaches the bottom edge. |

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with
a `bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

- **Act HP factor** ([balancing basis](../../../enemies/README.md#balancing-basis)) from the proposed
  reference DPS 104 at L24: **Scuttler** (L04, 38) ×2.74 → 77 HP; **Driftjelly** 8, **Reef Spitter** 14, **Lamprey** 6 (the `small` units take the beam-era ×2 only); new units at their
  specs: **Barbfin** 12 → 24 (beam-era ×2 from the level number), **Abyss Ray** 210 (2.0 s of the reference), **Eel Swarm** (the L23 spec ×1.04: head 104,
  whole about 255), **Glassfin** 1; the **spawning reef** is 200 HP (about 1.9 s). Re-measured in part D under the beam's halved reach (`BeamEraHpTest`).
- **Density and pacing** (§1.7: drafted to the Act 2+ floor): **265 enemies** (waves 255 + ground targets 10) over the 172 s,
  **≈ 92 a minute** at medium (≥ 60; the reefs' fry add more, not counted); a wave about every 3 s from t=3, **front 100 %**. The **firing groups** carry the floor: Barbfin
  columns and circles (the act's shot source: a group of 4 fires about 100 shots in 60 s), Eel Swarm heads, Driftjelly rings, Lamprey streams, the Reef Spitter
  nests and the Rays' fans (9-way, three a visit). Expected to meet ≥ 2.5 enemies on the screen and ≥ 10 shots a minute, **to be confirmed with `levelReport`** in part D
  (bullets × 0.7 stay longer on the screen, so fewer groups may do). The new units get **quiet windows**: no other wave enters t=6–9.5 (the first Barbfins), and t=55–62, 109–116, 147–154 around each Ray (3 s before the rise to 4 s after it
  starts; the Barbfin escort is the Ray's own wave). **Never more than one Ray on the screen**; the three Rays are 54 s and 38 s apart (the unit spec's "once a minute", tightened by the act's 172 s).

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front |  |
| 6 | 1 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | **New.** Three Barbfins hold and spit needles; Varga's line at t=5; alone until t=9.5 |
| 12 | 1 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 15 | 1 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 18 | 1 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) |  |
| 20 | 1 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | Solo; the Barbfins' first holders are gone |
| 24 | 1 | circle | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 27 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front |  |
| 30 | 2 | single | [Scuttler](../../../enemies/ground/scuttler/README.md) | 2 | front | Two walkers on the sea floor (`ground`) |
| 33 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 36 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) |  |
| 39 | 2 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (alternating edges) |  |
| 42 | 2 | circle | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 44 | 2 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | Over the first spawning reef |
| 48 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 51 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 54 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) |  |
| 58 | 2 | carrier + escorts | [Abyss Ray](../../../enemies/naval/abyss-ray/README.md) + [Barbfin](../../../enemies/naval/barbfin/README.md) | 1 + 4 | front (from below) | **New.** The first Ray: a shadow grows under the ship's lane for 1.5 s, the Ray rises, fans three times and glides off; the four Barbfins peel off its shadow; Varga's line at its first shadow (t=57.6); no wave enters until t=62 |
| 63 | 2 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (alternating edges) |  |
| 66 | 2 | convoy | [Scuttler](../../../enemies/ground/scuttler/README.md) | 3 | front | Three walkers 3 s apart down the reefs' lane |
| 69 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front |  |
| 72 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 75 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 78 | 2 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 2 | front | Two staggered 4 s apart |
| 81 | 2 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (alternating edges) |  |
| 84 | 2 | circle | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 87 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front |  |
| 90 | 2 | pair | [Scuttler](../../../enemies/ground/scuttler/README.md) | 2 | front | Past the last reef |
| 93 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 96 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) |  |
| 100 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | Under the first beacon |
| 102 | 3 | circle | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 105 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 14 | front |  |
| 108 | 3 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 112 | 3 | carrier + escorts | [Abyss Ray](../../../enemies/naval/abyss-ray/README.md) + [Barbfin](../../../enemies/naval/barbfin/README.md) | 1 + 4 | front (from below) | The second Ray, over the beacon line: its fans cross the beacons' pulse; no wave enters until t=116 |
| 117 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 120 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) |  |
| 124 | 3 | convoy | [Scuttler](../../../enemies/ground/scuttler/README.md) | 3 | front | Along the beacon line's floor |
| 126 | 3 | circle | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 128 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 2 | front | Two staggered 4 s apart |
| 132 | 3 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 135 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 138 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) |  |
| 141 | 4 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (alternating edges) |  |
| 144 | 4 | circle | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 150 | 4 | carrier + escorts | [Abyss Ray](../../../enemies/naval/abyss-ray/README.md) + [Barbfin](../../../enemies/naval/barbfin/README.md) | 1 + 4 | front (from below) | The last Ray; the Barbfins stay when it sinks; no wave enters until t=154 |
| 156 | 4 | pair | [Scuttler](../../../enemies/ground/scuttler/README.md) | 2 | front | The last walkers |
| 160 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The last eel |
| 162 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |
| 165 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | front (alternating edges) |  |
| 168 | 4 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 10 | front (alternating edges) |  |

Totals: Glassfin 120 · Barbfin 59 · Driftjelly 32 · Lamprey 21 · Scuttler 12 · Eel Swarm 8 · Abyss Ray 3.

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 1 | [Reef Spitter](../../../enemies/naval/reef-spitter/README.md) nest ×2 on a ridge (t≈18) | Bounty 14 each |
| 2 | **Spawning reefs** ×4 (t=36, 48, 64, 80; x 120, 350, 130, 340): a coral tower with an iris that releases 4 Glassfin every 3.5 s while it is on the screen, 200 HP, **not hardened** | The secondary counts them; bounty 40 each; razed: the iris closes, a coral burst |
| 2 | Reef Spitter nests ×2 round the second and third reef (t≈50, 66) | Bounty 14 each |
| 2–3 | Wrecked survey sub *Meridian* (`ground` trigger, 20 HP, t≈105, x 70) | The secret: a crate worth 100 (410) |
| 3 | Reef Spitter nest ×2 at the foot of the second beacon (t≈114) | Bounty 14 each |

The **relay beacons** are scenery: shots pass over them, nothing is hooked on them, no ground target lies within 100 px of one (the Helix compound's rules of
[Level 17](../../act-3-red-dust/level-17-dome-siege/README.md)). The **spawning reef** is a level-local `ground` spawner, specified here (non-hardened, 200 HP, the Hive Node's iris
rule softened for the water): promote it to its own part if a later level reuses it.

## Hazards

- **The Abyss Ray's fans**: three 9-way fans (110°, bullets × 0.7), centred on the ship at the moment of firing; the rise's shadow is a tell, not a hazard.
- **The reefs' fry**: Glassfin released while a reef lives (dies with it).
- **The water**: bullets × 0.7, acceleration × 0.6, non-`anti-sub` damage × 0.7, torpedo range +50 %; the Pressure hull removes the drag.
- No terrain collision; no currents.

## Secrets and pickups

- **Wrecked survey sub *Meridian*** (t≈105, x 70, a Thera Deep survey vessel lost with its crew, lying at the left edge of the beacon line): a `ground` trigger of **20 HP**;
  destroyed, its hatch lets go and a hidden crate floats up worth **100** in Act 1 terms (410 in Act 4). The Salvage scanner's glint marks it. Varga's line: "*That's the
  Meridian. Thera Deep lost her to the dark, not to the Vrell. Take what she kept.*" (an event line, dropped after 6 s if the radio is busy).
- **Overdrive**: dropped by the first Ray (t=58) when it dies.
- **Armour patch**: a Thera Deep tender releases one at (240, 380 y-up) at t=95.
- **Shield cell**: dropped by the second Ray (t=112) when it dies.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), retimed to the HUD's **1 s rule** (at least 6 s between lines; the build's
`RadioTimelineLevel24Test` checks them with and without Rook). Event lines drop after 6 s. Rook's lines carry `requires: escort`; his one dry joke is at t=14. Okafor's t=106 line is the
first sign that she listens to Varga after Act 3 (L16–17: "Not on this channel, Doctor."): she asks for a **private record**, which is not yet agreement.

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Past the farms is the abyssal plain, Aegis. Thera Deep's lamps end ahead; from here you fly on your own lights." |
| t=5 | Varga | "Small swimmers, needles aimed at you. They hold and shoot: take them one by one." |
| t=14 | Rook | "Dark's coming, Lancer. Rule one in a submarine: the ceiling is lower than it looks." |
| t=34 | Varga | "Spawning reefs. They breed fry from the iris, and they are soft: torpedoes will do." |
| A Ray's first shadow (`first-ray`, t≈57.6) | Varga | "Something huge under you. A shadow first, then a manta. Torpedoes reach it now; the rest must wait." |
| t=98 | Varga | "A beacon. Human-made, relay hardware, an amber pulse. And the carrier on it is Helix's." |
| t=106 | Okafor | "Log it, Doctor. Full record, for my eyes only." |
| t=120 | Rook | (the Ray's flank bark) "Ray at six, Lancer: it is rising!" |
| Secondary met (`secondary-objective`) | Okafor | "Third reef down. The Vrell will have to breed somewhere else." |
| t=131 | Varga | "A third beacon. The same pulse every two kilometres. Somebody laid a road down here." |
| Level end | Okafor | "The plain is behind us. The beacons lead into the trench, and so do we. Refit, Aegis." |

The secret's line (the wreck's hatch opened): Varga, "That's the Meridian. Thera Deep lost her to the dark, not to the Vrell. Take what she kept." Varga's t=98 and t=131 lines are the act's beacon beats
(L24–L26: Varga decodes Helix frequencies), at no more than one a level so the reveal keeps its weight; Rook's t=120 bark may be dropped by the 1 s rule (pending the build).

## Music & ambience

The Act 4 A theme **"Thera Deep"** ([track list](../../../audio/music/README.md#track-list), track 10): the under-water bed alone at the plain's edge, the **base stem from section 2**
(the reef field), the **full mix from section 3** (the beacon line, with the beacons' pulse as a faint sonar ping layered on the beat), the base stem again in section 4. Round 48 Q12 a:
no bubble layer. New sounds: the Ray's rising horn (the shadow's tell: a low, heavy swell), its fans, the Barbfin's needle, the reef's iris and its collapse, the Scuttler's walk on silt, the beacon's pulse; listed for
the act's sfx step ([sfx](../../../audio/sfx/README.md)); real-thing sounds from Freesound only, none bubbly or popping.

## Credit budget

Budget(24) = 700 × 1.07²³ ≈ **3,318**, the typical haul's target
([economy](../../../systems/economy/README.md#per-level-budget)). The data keeps **Act 1 terms**
(Glassfin 2, Barbfin 12, Eel Swarm 48 whole, Abyss Ray 60, Driftjelly 10, Lamprey 12, Scuttler 25, Reef Spitter 14, spawning reef 40, the crate 100, the secondary 100); the code pays each × the act factor
**4.096**, bounties also × the scale. A **`bounty_scale` of 0.40** (the model of the earlier Act 3 levels: typical = 0.663 of the kills, 0.8 of the ground targets, half of the secret and secondary; a first
estimate for part D to confirm with `TypicalHaulTest` and `levelReport`) puts the typical haul at **3,319 (+0.0 %)**, the perfect run at 5,125. No boss or mid-boss; the reefs' fry pay as Glassfin.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Glassfin 120 × 2 + Barbfin 59 × 12 + Driftjelly 32 × 10 + Lamprey 21 × 12 + Scuttler 12 × 25 + Eel Swarm 8 × 48 + Abyss Ray 3 × 60 (Eel Swarm whole, 48) | 3,906 | 2,590 |
| Ground targets: Spawning Reef 4 × 40 + Reef Spitter 6 × 14 | 400 | 320 |
| Secret: wrecked survey sub (hidden crate, 6 % of the typical haul) | 410 | 205 |
| Secondary: reefs razed (3 of 4 spawning reefs) | 410 | 205 |
| **Total** (bounty scale 0.4) | **5,125** | **3,319** |
| Budget(n) = the typical haul's target; typical +0.0 %, perfect 1.54 × budget | | 3,318 |

**Balance plan**: the plan's L24 visit (4 104 cash) buys the Pressure hull 2 000 and one wing pod's Micro-missiles L3 800
([generator](../../../player/generator/README.md#act-4-hand-plan-draft)); the level expects `anti-sub` and `spread` and is flyable without either.

## Difficulty notes

- **Easy**: the Ray has a 7-way fan and two fans; the Barbfin shoots every 3 s; the eel has 10 segments and no fan; the secondary needs 2 of 4 reefs; the reefs release 3 fry.
- **Hard**: the Ray has an 11-way fan and four fans; the Barbfin fires a 2-needle burst every 2.1 s; the eel has 16 segments; the secondary needs all 4 reefs; the other waves by the formation lever, trimmed by hand for at most ~3.5 enemies on the screen.

## Concept art

No art yet. Round 48 proposals: the Abyss Ray with its shadow, the Barbfin, the Glassfin and the Eel Swarm (step A-units), the Abyssal Plain mood still and the under-water grade (step A-europa); the production backdrop (plain, reefs, beacons),
the spawning reef sprite, the beacon set piece and the briefing images come in part D. See [naval](../../../enemies/naval/README.md#concept-art) and [europa](../../../world/europa/README.md#concept-art).

## Implementation

- [ ] Level data `data.yaml` (M8 part D): sections and speeds with 2 s eases, waves, ground targets, radio, budget, threat profile, briefing, backdrop, music
- [ ] The Abyss Ray as specified (the shadow tell, the `sub` → `air` flip at the rise's midpoint, three fans, the glide off) and its Barbfin escort ([Abyss Ray](../../../enemies/naval/abyss-ray/README.md), [Barbfin](../../../enemies/naval/barbfin/README.md)); never more than one Ray on the screen
- [ ] The spawning reefs (level-local non-hardened ground spawners) and the secondary `destroy-targets` (3 of 4, `REEFS` counter); the fry stop when a reef dies
- [ ] The Scuttler walking the sea floor under water; the Driftjelly, Reef Spitter and Lamprey under water
- [ ] The relay beacons as set pieces (scenery, red-gold pulse every 2 s, clear of every target and wave route), the beacon radio beat
- [ ] The secret (wrecked survey sub), the tender's armour patch, the Ray drops
- [ ] Wave script: ≥ 60 enemies a minute at medium, no empty screen over 2 s, the Act 2+ floor, the quiet windows (`DensityTest`, `PacingTest`, `ActFloorTest`)
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel24Test`, with Rook and without); Rook's lines `requires: escort`
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`); the Abyss Ray's and Barbfin's portraits
- [ ] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(24) = 3 318 with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music and ambience: the stems by section, the beacon ping layer, the Ray, reef and Barbfin sounds (`Level24SoundsTest`)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on every difficulty (`--escort none` too)
- [ ] Production art and captures for part D's round

## Open questions

- Are three Abyss Rays in one level too many for a "large set piece, once a minute"? (Our reading: three at 54 s and 38 s apart is the act's only set piece in a 172 s level; part D may drop the third.)
- Should the spawning reef be hardened like the Hive Node? (Our reading: no, so the harpoon loadout can raze it.)

## Decisions

- 2026-10-21 (Act 4 design phase, step `m8-dl2224`; **all decided in round 48 (2026-10-22)**): promoted from the act roster to a
  draft level document, hand tables only, no `data.yaml`. The Ray rises from `sub` and flips to `air` (the play plane stays `air`); the spawning reefs are
  soft level-local spawners; the beacons are scenery that Varga decodes once or twice a level (Q11 a: grey machines, red-gold beacons); Okafor first asks for a private record;
  numbers from the proposed reference DPS 104.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
