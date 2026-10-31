---
title: Level 35 – Iron Sovereign
design: approved
implementation: done
art: final
depends-on: [../../../enemies/bosses/iron-sovereign, ../../../enemies/air/talon, ../../../enemies/air/gilded-gunship, ../../../enemies/air/hornet, ../../../enemies/air/rail-serpent, ../../../enemies/air/dart-drone, ../../../enemies/space/shard-drone, ../../../enemies/ground/rail-bunker, ../../../enemies/ground/sam-nest, ../../../world/belt, ../../../player/wingmen, ../../../story/characters/vorne, ../../../systems/economy]
updated: 2026-10-31
---

# Level 35 – Iron Sovereign

## Summary

The Act 5 finale. Aegis Wing climbs to Ceres orbit, guided by the loyalist dock controller who never left
his channel, breaks the battle station's escort screen (Talon wings, Gilded Gunships launching Dart
streams, Hornets, a Rail Serpent pair), crosses the station's outer hull under SAM Nests and Rail Bunkers
and dodges one shot of the turned **mass relay**. Then the scroll halts over the
[Iron Sovereign](../../../enemies/bosses/iron-sovereign/README.md): three counter-turning gun rings round
a reactor core, Vorne on the enemy channel. About 92 s of approach, the fight on a halted arena (≈ 145 s at
medium), then the station breaks apart, **Vorne offers Lancer a job**, Varga reads Callisto out of its nav
core, and the act ends; the release's end names Act VI. Density 5.

**Status: approved** (Act 5 design phase, step `m9-d-levels-3`, 2026-10-27), **approved by the user in concept round 60 (2026-10-27)**. Written from the act roster, the gap analysis's recommended answers (**Q7 a** the top-down
rings, **Q1 a** the core's and the escort's shields, **Q6 b** the Dart Drones, **Q11 b** the belt look (gunmetal/charcoal with gold); judgement
call **J2** the reference DPS 144, all decided in round 60) and the wave 1 and 2 documents. The boss document owns
the fight (rings, batteries, phases, HP, tells, Vorne's lines and their wording); this document owns the approach,
the timeline, the radio's order and delays, the economy and the act end. M9 part H, step H2: the level's
[data.yaml](data.yaml) holds them (the tables below are rendered from it), measured with `levelReport` on the approach;
the fight's numbers are the boss's data, tuned in step H3.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

<!-- data: briefing -->
> **Commander Okafor:** "The Iron Sovereign holds Ceres orbit: Vorne's battle station. Ceres
> Control is still on our side and will guide you in. Break its escort, then break the station,
> Lancer."
>
> **Dr. Varga:** "Three gun rings turn round a reactor. The batteries only fire on the side that
> faces you. Destroy a ring's guns and it locks. Lock all three and the core opens."
<!-- /data -->

*Images* (production art in part H; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-35-ceres-orbit`: a tactical chart of Ceres orbit: the approach from the graveyard lane,
  the escort screen as gold contacts, the station's outer hull, the mass relay on Ceres's limb marked with a red
  bearing line, the Sovereign at the north pole.
- Varga's page `level-35-sovereign-rings`: her plan of the station from above: three rings with their battery
  types (SAM, rail, laser) and their turning directions as arrows, the fire arc facing the ship shaded red, the
  core's six petals shut.

*Hangar teaser* (shop screen before L35; Okafor, not voiced):

<!-- data: teaser -->
> **Commander Okafor:** "The Sovereign is next. Whatever you salvaged, fit it now."
<!-- /data -->

## Threat profile

Draft; directions are counted by each wave's `from` (the intel's rule): 87 % front, 10 % sides, 3 % rear; in the
fight the batteries fire from the whole station and are not in the numbers.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (written `space`: the Talons, Gunships and their Darts, Hornets, the Rail Serpents, the Shard Drones, the Vrell popcorn), `ground` (the outer hull's SAM Nests and Rail Bunkers; the Sovereign itself, seen from above) |
| Attack directions | front 86% · sides 10% · rear 4% (the approach by its waves; the station's batteries in the fight fire from all round the arena and are not in the numbers) |
| Density | 5 |
| Recommended traits | `piercing`, `anti-ground` (the boss's columns of batteries and its `ground` layer; `homing` helps on the batteries in the arc) |
| Hazards | The mass-relay flash (once, t=61); SAM homing diamonds and rail lines on the hull; the Sovereign's batteries, its core's fan, spiral, rail and Dart streams |
| Boss / mid-boss | Act boss [Iron Sovereign](../../../enemies/bosses/iron-sovereign/README.md) |
| Sensor-suite detail | **none**: the belt, Ceres orbit, `air` + `ground`, all directions. **L1**: + the directions, density 5, hazards "mass relay" and "rail lines", the objective `DESTROY THE IRON SOVEREIGN`. **L2**: + Talon, Gilded Gunship, Hornet, Rail Serpent, Shard Drone, Dart Drone, SAM Nest, Rail Bunker portraits; the boss's name and silhouette (`iron-sovereign`). **L3**: + `piercing` and `anti-ground` highlighted; the timeline strip marks the mass-relay flash (61 s) and the boss (92 s); 1 secret. |
<!-- /data -->

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Ceres orbit. One huge contact: Vorne's battle station." |
| L1 | "An escort screen, then the station. From every side." |
| L2 | "Talons, Gunships, Hornets, Serpents. The Iron Sovereign." |
| L3 | "Piercing for the rings, anti-ground for the batteries." |

## Objective

- **Primary** `reach-end` with the arena as the condition, as Levels 21 and 28: the level ends when the Iron
  Sovereign is destroyed and its closing lines have played. The briefing and the objective line read `DESTROY THE
  IRON SOVEREIGN` (the boss's name on the bar). It is not a `boss` primary.
- **Secondary** "Break the screen" (`kill_ratio`, the 65 % rule): destroy at least **65 %** of the approach's enemies
  (65 % of the approach's planned units, the waves' 136 and the 15 hull guns: **99 of 151** at medium, 80 of 123 on easy,
118 of 181 on hard; the Gunships' Darts count as kills as they come, not in the requirement: the simulation's rule,
measured in step H3; step H2 had read ≈ 114 of 175 with the Darts), judged at the level's end. The Sovereign and the Darts
  it launches are not counted (`boss_counted: false`). Pays **100** in Act 1 terms (655 in Act 5); the typical haul
  counts it at 50 %. No radio line.

## Layout

Scroll **160 px/s** through the escort screen, **150 px/s** over the outer hull, then slowing to a **halt** over
the station (eased over 6 s from t=85.5, so nothing pops in: the section *The Halt* eases to 1 px/s by t=91.5 and the
arena's own second eases it to rest, ground scroll 13,788 at t=92; the station has been scrolling into view with the
ground since the hull, [the boss's arena](../../../enemies/bosses/iron-sovereign/README.md#arena)). Setting:
[the belt](../../../world/belt/README.md)'s Ceres orbit, vacuum
([vacuum rules](../../../world/belt/README.md#vacuum-rules)). **Light**: Ceres's sunlit curve far below in the deep
layer, the dust band, the station's gunmetal/charcoal and gold under the top-left sun with its red running lights
(Q11 b, decided in round 60); a step lighter than Act 4 so the hulls read. Motion budget: the escort's waves and the Darts are the strong
elements in the approach; in the arena the rings' turning and the fire arc's red band; after the kill the drifting
ring segments.

**The arena and the clock.** The boss warning (track 22, the klaxon and the box) sounds at **t=84** (the boss's
`incoming: 8`, step FIX6), 2 s before the station's rim enters the top edge at t=86; the
arena section starts at **t=92** (a one-second section `speed: 0, arena: true`, Level 19's yard and Level 28's
vent), with the bar `IRON SOVEREIGN`, the boss theme and the **boss checkpoint** (none on hard). The boss's intro
(≈ 8 s from t=88: the lights ring by ring, the spin-up, Vorne's first line) runs on to t≈96 of real time,
invulnerable until the rings reach speed. The level clock halts at the arena section's end while the station lives;
the kill resumes it after `hold_after_boss: 6` (the 5 s death chain, the flash and the credit shower over the core,
collectable, and one second more). **K** below is the kill: the clock reads **t=87 + K** from the moment it resumes
(K+6 = t=93).

**Rook and the escort.** Rook flies his slot and follows the boss's
[escort rules](../../../enemies/bosses/iron-sovereign/README.md#the-escort-rook) (the armed batteries first, out of a rail
line and a sweep's wedge, the Darts in reach). With `--escort none` the level is the same without him and his lines.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Ceres Orbit | 0–20 | 0–3,200 | 160 | clear | `deep`: Ceres's curve, Ceres Hub's lights (red since Level 29), the dust band. `low-air`: loyalist wrecks and a dead Ceres patrol boat drifting. | Talons and popcorn; Ceres Hub Control comes on the radio. |
| 2. The Escort Screen | 20–50 | 3,200–8,000 | 160 | clear | `far`: the Sovereign's tenders and docking spars far below. `air`: the escort. | The Gunships' Dart streams (t=20), the Rail Serpent pincer (t=35), the Hornets; Talon pincers from the sides. |
| 3. The Outer Hull | 50–80 | 8,000–12,510 | 150 | clear | `ground`: the station's outer hull (black plating, gold trim, red lights): SAM Nest groups (t≈52, 64, 74), Rail Bunker pairs (t≈55.5, 70, 78), a Ceres garrison lifeboat clamped to the hull (t≈66, the secret). **The mass-relay flash** at t=61. | The hull's guns; the flash once; a rear V-wing (t=68.5). |
| 4. The Rings' Edge | 80–85.5 | 12,510–13,335 | 150 | clear | `ground`: the outer ring's rim rolls into view, then the middle and inner rings and the core. | The last wave (t=83.5); the pre-boss pause begins (t≈84). |
| 5. The Halt | 85.5–92 | 13,335–13,788 | 1 | clear | `ground`: the station's centre reaching its place (240, 160 on the screen) as the scroll comes to rest. | The pre-boss pause (t≈84-92); nothing on the ground is left on the field. |
| 6. The Sovereign | 92–93 | 13,788–13,788 | 0 | clear | `ground`: the station filling the field, its centre at (240, 160) ([arena](../../../enemies/bosses/iron-sovereign/README.md#arena)). `deep`: Ceres far below. | The boss fight (≈ 145 s at medium); the scroll halted and the clock stopped while the station lives. |
| 7. The Wreck | 93–131.5 | 13,788–16,038 | 60 | clear | `ground`: the broken rings drifting apart, slowly turning, the core's crater glowing. | The closing lines (K+5 to ≈ K+43): Okafor, Vorne's offer, the answer, Varga's data, Ceres Control; no enemies (the level's outro for the pacing checks). |
<!-- /data -->

The level ends at **t=131.5** (K+44.5): the closing queue's last line ends ≈ K+43.4 by the radio's text model, then one
second (step H2). The section after the kill is the level's **outro for the pacing checks** (no enemies by design: `PacingTest` and
`ActFloorTest` measure the approach up to the halt; the build must say so), as Level 28's finale.

**The mass-relay flash** ([belt hazard](../../../world/belt/README.md#hazards--set-pieces)): Ceres's mass relay (a
cargo launcher on the limb, turned by the Ascendancy) fires once. At **t=59.8** a thin red line is drawn across the
whole field, **horizontal through the ship's position** at that moment (the relay lies off the right edge), for
**1.2 s** (easy 1.5 s); at **t=61** it flashes along the line, 16 px wide, live 0.2 s: **20** damage (two `heavy`
hits; draft, pending), Rook and every enemy on the line untouched (enemy fire never hurts enemies; Rook steps off it
as from a rail line). No wave enters from t=58.5 to t=63.5, no bunker is on the screen at t=61, and Ceres Control
warns at t=57.

## Waves

Formation names from the [formation vocabulary](../../../enemies/README.md#formation-vocabulary). The **act HP
factor** for the returning `medium`+ units (the proposed reference **144** at Level 35 against the unit's first
level, J2): Gilded Gunship ×144/124 (88 → 102, its shield 12 → 14), Hornet ×144/128 (96 → 108, shield 8 → 9), Rail
Serpent ×144/124 (engine 150 → 174, cars 16 → 19, turret cars 24 → 28, tail 20 → 23), Rail Bunker ×144/136 (100 →
106); the `small` units (Talon, Shard Drone, SAM Nest) keep their pages' HP with the beam era's ×2, the `tiny` ones
(Darts, the Vrell popcorn) 1 HP. Every number is a hand estimate for part H's `levelReport`.

- **Density and pacing** (the Act 2+ floor): **175 enemies at medium** in the 92 s approach (the waves' 136, the
  Gunships' **24 Darts** they launch, Q6 b, counted as their doc says, and 15 hull guns), **≈ 114 a minute**, the
  act's highest: a wave every 2.5 s, Talon wings and the Gunships' Darts for the count, the Talons' pairs, the
  Gunships' rings, the Hornets' salvos and the hull guns for the shots. After t=83.5 **no wave enters**: the
  pre-boss pause (t≈84–92) is the level's one stretch over 2 s with nothing on the screen at the floor (the third
  allowed gap, as Level 28's); the pacing checks must name it.
- **The Sovereign's Darts** (phase 4, ≈ 13 streams of 4 at medium) count into the level's enemies, not its density
  (the boss rule); the typical haul budgets them at the expected count (52).
- **Quiet window**: no wave from t=58.5 to t=63.5 (the mass-relay flash).
- **Directions**: 27 of 31 waves from the front (87 %); the two Talon pincers and the Rail Serpent pincer have edge
  warnings, the rear wave a BOTTOM warning, 3 s ahead.
- **Totals**: Talon 54 (6 leaders) · Skitter 24 · Mote Swarm 38 · Gilded Gunship 4 (+ 24 Darts) · Hornet 5 · Rail
  Serpent 2 (10-car trains) · Shard Drone 4 · Dart Drone 5 (the rear stream).
- **Measured** (step H2, `levelReport -Plevel=35` with the plan's L35 fit and Rook, the approach to the halt): at
  medium 151 = 136 waves + 15 hull guns (the Gunships' Darts on top), 104 a minute; **3.60 on the screen**, 82 shots
  a minute; one gap over 2 s, the pre-boss pause (85.8–92.0, the third allowed); the small units' time on the screen
  1.66 s, **1.04×** Level 10's. Easy 2.15 on the screen (Act 1 rule: ok), hard 2.85 and 184 shots a minute. The draft's
  waves needed no offbeat firing groups: the Talons' pairs and V-wings, the Gunships, the Hornets and the hull guns
  carry the shots.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 5 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Ceres Hub's loyalist wrecks drift below; the leader at the tip |
| 7.5 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 10 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 12.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 15 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 17.5 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 20 | 2 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | Each Gunship launches Dart streams (3 every 5 s, at most 6 alive): ≈ 12 Darts from this pair |
| 22.5 | 2 | pincer | [Talon](../../../enemies/air/talon/README.md) | 6 | sides | Edge warnings at t=19.5 |
| 25 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 27.5 | 2 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 30 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 32.5 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 35 | 2 | snake | [Rail Serpent](../../../enemies/air/rail-serpent/README.md) | 1 | left side | The pincer's left train (10 cars), warned 3 s ahead at the left edge; its engine drops an overdrive |
| 35 | 2 | snake | [Rail Serpent](../../../enemies/air/rail-serpent/README.md) | 1 | right side | The pincer's right train (10 cars), warned 3 s ahead at the right edge |
| 38.5 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 41 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 43.5 | 2 | single | [Hornet](../../../enemies/air/hornet/README.md) | 1 | front | Escorted by the Talons of t=38.5 |
| 46 | 2 | line abreast | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 4 | front | Their links lit 0.8 s after the last drone enters |
| 48.5 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 51 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 53.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 56 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | ≈ 12 more Darts; the second Gunship drops a shield cell; hard a third |
| 58.5 | 3 | pincer | [Talon](../../../enemies/air/talon/README.md) | 6 | sides | Edge warnings at t=55.5; then **no wave until t=63.5**: the mass-relay flash (telegraph t=59.8, flash t=61) |
| 63.5 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 66 | 3 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 68.5 | 3 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | rear | From behind (BOTTOM warning at t=65.5): a Dart stream, the station's own chasers (the README's rear Talon V-wing: the simulation flies a Talon only on its swoop, from the front or the sides) |
| 71 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 73.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 76 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 78.5 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 81 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 83.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The last wave: the pre-boss pause t≈84-92 |
| 92 | 6 | boss | [Iron Sovereign](../../../enemies/bosses/iron-sovereign/README.md) | 1 | the ground (it scrolls in with the hull and halts at the centre (240, 160)) | At the arena's start: the warning, the bar and the checkpoint; the rings, the batteries, the core and its Darts as in its spec; no other waves in the arena |

Totals: Talon 54 · Skitter 24 · Mote Swarm 38 · Gilded Gunship 4 · Hornet 5 · Rail Serpent 2 · Shard Drone 4 · Dart Drone 5 · Iron Sovereign 1.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 3 | [SAM Nest](../../../enemies/ground/sam-nest/README.md) group ×3 on the hull (t≈52); hard ×4 | Bounty 15 each; homing diamonds, shootable, the Decoy Flares retarget them |
| 3 | [Rail Bunker](../../../enemies/ground/rail-bunker/README.md) pair (t≈55.5), off the screen before the flash | Bounty 45 each; hardened |
| 3 | SAM Nest group ×3 (t≈64); hard ×4 | Bounty 15 each |
| 3 | The Ceres garrison lifeboat clamped to the hull (`ground` trigger, 20 HP, hard 28, t≈66, x 400) | The secret: frees a crate worth 100 (655) |
| 3 | Rail Bunker pair (t≈70, staggered 1 s), their lines never both on the ship's row | Bounty 45 each |
| 3 | SAM Nest group ×3 (t≈74); hard ×4 | Bounty 15 each |
| 3 | Rail Bunker pair (t≈78), off the screen before the halt | Bounty 45 each |
<!-- /data -->

## Hazards

- **The approach**: the hull guns (the SAM Nests' salvos, at most 8 missiles a nest; the bunkers' rail lines, never
  more than 2 telegraphed at once), the mass-relay flash (*Layout*); drifting asteroids are few in orbit (two small
  ones in section 1); no terrain.
- **In the arena**: the station is `ground` (no collision, the ship flies over the rings); the batteries' salvos,
  rail lines and sweeps inside the fire arc, the core's fan, spiral, rail line and Dart streams are the boss's
  ([phases](../../../enemies/bosses/iron-sovereign/README.md#phases), [tells](../../../enemies/bosses/iron-sovereign/README.md#tells)).
- **Specials**: no setting limit (the Airstrike and the Orbital Lance work: Ceres orbit is open sky); their boss
  rules cap them per part ([the boss's note](../../../enemies/bosses/iron-sovereign/README.md#duration-and-hp)); the EMP
  Burst empties every shield on the screen (the core's included) and stuns the hull guns; the Sonar Pulse reads
  `NO WATER`.

## Secrets and pickups

- **The Ceres garrison lifeboat** (the secret, t≈66, x≈400, on the outer hull): a loyalist lifeboat the station
  caught in its docking clamps, empty, its beacon still on. A `ground` trigger of **20 HP** (28 on hard); destroyed,
  its stores float up as a crate worth **100** in Act 1 terms (655). The Salvage scanner's glint shows it at any
  range. Its event line, Ceres Hub Control: "*One of our lifeboats. Empty. Take the stores, Aegis.*" (one radio
  page, step H2)
- **Overdrive**: dropped by the first Rail Serpent's engine (t=35).
- **Shield cell**: dropped by the second Gunship of the t=56 column.
- **Armour patches** ×2: a CDF beacon on a loyalist wreck releases one at (240, 380 y-up) at t≈80, before the
  arena; and the boss's phase 4 drops one (its spec).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), draft text for the **1 s rule** (12 characters a second plus a
1.5 s lead; 0.4 s of silence); event lines drop after 6 s. Rook's lines carry `requires: escort`. **Speakers:
Okafor, Varga, Rook, Ceres Hub Control** ([Act 5 speakers](../../../story/characters/README.md#act-5-speakers-draft))
**and Vorne** on the enemy channel ([HUD](../../../ui/hud/README.md): the amber plate, the steady interference over
his portrait). **The Choir is silent** (the Ascendancy's boss; [the Choir](../../../story/characters/the-choir/README.md#act-5-draft)).
The arena's clock halts, so the boss lines are events (`boss-arrival`, `boss-phase`), queued as `PHASE` lines (they
wait for a gap, stale after 20 s, dropped at the kill). **Vorne's three fight lines and his recruitment line are
the boss document's** ([Vorne](../../../enemies/bosses/iron-sovereign/README.md#vorne), wording decided in round 60):
this table places them and sets their delays. **The closing lines are queued as `CLOSING` lines in order**
(Level 21's rule: they never go stale and the debrief waits for the last); `delay` counts from the kill K.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Break the Sovereign's screen, Lancer. Then break the station." |
| t=9.5 | Ceres Hub (Ceres Hub Control) | "Ceres Control, still on the dock channel. I'll guide you in." |
| t=23 | Rook | "Gunships with darts. I'll take the darts, you take the boats." |
| t=36 | Varga | "Rail Serpents on both flanks. Hit the engine first." |
| t=46 | Okafor | "SAMs and rail bunkers on the hull. Flares if you have them." |
| t=55.5 | Ceres Hub (Ceres Hub Control) | "The mass relay is turning on you! Red line: move!" |
| t=63 | Rook | "That rail gun was aimed at us? I'm flattered." |
| t=76.5 | Varga | "The rings are turning. Three of them, guns on every ring." |
| t=83.5 | Okafor | "This is Vorne's station. Everything you have, Lancer." |
| Boss arrival (`boss-arrival`, the spin-up), the enemy channel | Vorne | "Welcome to Ceres, Lancer. The Sovereign was built to hold the Belt. Do try to make this interesting." |
| Boss arrival, queued after Vorne's (`PHASE`) | Varga | "The guns fire only on the side facing you. Destroy a ring's guns." |
| First ring locked (`boss-phase` Two rings), the enemy channel | Vorne | "Such dedication. Such waste. You could have been on the winning side, pilot." |
| Second ring locked (`boss-phase` One ring) | Rook | "One ring left. It's running out of guns." |
| Third ring locked, the core opening (`boss-phase` The core), the enemy channel: cold and clipped, never raised | Vorne | "Enough. You are becoming expensive." |
| The core, queued after Vorne's (`PHASE`) | Varga | "The core is open! Its shield is thin. A Tesla hit empties it." |
| The core's first rail volley (`first-rail-volley`, its tell; `PHASE`), step BOSS70 (round 70) | Rook | "Red petals, three lines — move!" |
| The core's first mine ring (`first-mine-ring`, its tell; `PHASE`) | Okafor | "Mines off the rim, Lancer. Give the lights room." |
| The core's first wreck throw (`first-wreck-throw`, its tell; `PHASE`) | Rook | "It's throwing its own wreckage at us!" |
| The first wreck throw, queued after Rook's (`PHASE`) | Okafor | "Shoot the scrap down or get clear of it." |
| The core's first draw below half its HP, the rests shortening (`core-low`; `PHASE`), the enemy channel | Vorne | "You read the Sovereign well, pilot. Read faster." |
| Sovereign destroyed (`boss-destroyed`, `delay: 5`, `CLOSING`), at the flash | Okafor | "The Sovereign is down! Ceres orbit is ours, Aegis Wing." |
| Then (`CLOSING`): steady and warm again, the enemy channel | Vorne | "Lancer. You are wasted on Okafor. Come to Callisto. Name your price." |
| Then (`CLOSING`, with Rook) | Rook | "Funny. We were just heading your way." |
| Then (`CLOSING`, without Rook: replaces his line) | Okafor | "Our pilots are not for sale, Vorne." |
| Then (`CLOSING`) | Varga | "Its nav core took every order from one place. Callisto." |
| Then (`CLOSING`); the level ends 1 s after it | Ceres Hub (Ceres Hub Control) | "Ceres to Aegis. The hub lights are white again. Thank you." |
<!-- /data -->

Timeline check (step H2, the radio's text model: 22-character rows, 3-row pages typed at 30 characters a second, held
3 s and the last page 5 s, 0.4 s between lines; the voiced takes are shorter than the text): every approach line is one
page (the gap check's §4). Okafor t=1 ends ≈ 8.0, Ceres Control t=9.5 ≈ 16.4, Rook t=23 ≈ 30.0, Varga t=36 ≈ 42.6,
Okafor t=46 ≈ 52.9, Ceres Control t=55.5 ≈ 62.1 (the warning over the flash's telegraph at 59.8), **Rook t=63** right
after the flash (his "rail gun" is the relay; it was t=72) ≈ 69.4, the lifeboat's event line (the lifeboat enters at
t=66) waits behind him to ≈ 76.5, **Varga t=76.5** ≈ 83.7 (≤ 0.4 s late), **Okafor t=83.5** ≈ 90.8 (≤ 0.6 s late), over
the pre-boss pause. In the arena Vorne's arrival line (two pages, ≈ 11.2 s, the boss document's wording) and Varga's
one page fill ≈ 18.6 s, inside the spin-up and phase 1; Varga's two PHASE lines are one page each. The closing queue
from K: Okafor K+5.0 to ≈ K+11.8, Vorne ≈ K+12.2 to ≈ K+22.3, Rook (or Okafor) ≈ K+22.7 to ≈ K+28.9, Varga ≈ K+29.3 to
≈ K+36.1, Ceres Control (one page) ≈ K+36.5 to ≈ K+43.4; the level ends at **K+44.5 (t=131.5)** and the debrief waits
for the last line. Game step H4 flies it in `RadioTimelineLevel35Test` with Rook and without.

**Story notes.** The order after the kill is the boss document's: Okafor's kill line, Vorne's offer, a friendly answer
(Lancer never speaks), Varga's data line, so Vorne and Varga name Callisto together
([Vorne](../../../story/characters/vorne/README.md#act-5-draft)); Ceres Control's last line closes Level 29's turned
station (the hub's lights back to white). Varga's look at Vorne's face ("he looks forty; he was sixty when I left
Helix") is the **act outro's**, not the radio's ([Varga](../../../story/characters/varga/README.md#act-5-draft)). Vorne
never shouts: the core's line is cold and clipped under the *rage* portrait.

## Boss / mid-boss

Act boss: [Iron Sovereign](../../../enemies/bosses/iron-sovereign/README.md) (Q7 a, the top-down rings). Its rings,
batteries, fire arc, locks, the core's phase, HP, tells, the escort's rules, the death and Vorne's lines live in the
spec. Arena notes for this level:

- The arena starts at t=92 with the bar and the checkpoint; the warning sounds at t=84 (its `incoming: 8`, step FIX6),
  2 s before the station's rim is on the screen (the spec's ≈ 8 s intro from the arrival). Nothing slides under the station: it has scrolled in with the hull.
- **Traits**: `piercing` (the columns where the rings' batteries line up) and `anti-ground` (×2 on the whole station,
  a `ground` unit, not hardened); the roster's pair, kept. `homing` shots help on the batteries in the arc.
- The checkpoint is recorded at the halt on easy and medium; none on hard (the
  [retry](../../../systems/retry/README.md) rules).
- **Bounty**: the spec first assumed a `bounty_scale` of 0.75 and 341 in Act 1 terms, the level's hand scale 0.56 gave
  457; with the **measured scale 0.59** (step H2) the spec's formula (≈ 1,676 ÷ (6.5536 × the scale) for 24 % of
  budget(35)) gives **433 in Act 1 terms** (core 355, each of the 13 batteries 6), paid **1,674 = 24.0 %** of
  budget(35).
- **The Darts it launches** pay their own bounty (2 each, outside the 24 %) and count into the level's enemies, not
  its density; the kill-ratio secondary leaves them out (`boss_counted: false`).
- **Rook and the autopilot** must pick the armed batteries first, leave a rail line and a sweep's wedge and stay out of
  the core's 72 px when it opens, or the tests cannot finish the fight (the spec's lessons).

## Music & ambience

The [Act 5 music plan](../../../audio/music/README.md#act-5-music-draft-round-60)'s cue for this level:
[track 13](../../../audio/music/README.md#track-list), the Act 5 B theme: the **base stem** in section 1, the **full mix
from the escort screen** (t=20), Vorne's motif **on** (the Ascendancy's level), **out at the halt** over the station
(fading t=86–88); the boss warning (track 22) at t=84 (step FIX6) and [track 19](../../../audio/music/README.md#track-list) "Iron
Sovereign" through the fight (one full mix, no change at the core's opening: the klaxon and Vorne's line carry it,
under the radio's duck), ending with the kill. **After the kill track 13's motif stem plays alone** under the closing
lines (`motif_after_boss`, Level 28's hook), out with the level's end. The debrief plays the `mission-complete`
jingle (23), and the act-complete fanfare (24) plays under the first outro page. Ambience: the `belt-station` bed
([sfx](../../../audio/sfx/README.md#ambience-per-setting)) throughout, the rings' grind and the core's klaxon (the boss's
[sounds](../../../enemies/bosses/iron-sovereign/README.md#sounds-brief)) on top in the arena. New sounds this level
needs: the mass relay's charge and flash (a huge capacitor whine and a violent electric crack, the rail's family
scaled up), the hull guns' (their units'), the boss's (its brief). Freesound only for real-world sounds (CC0/CC-BY), no
bubbly or popping sounds.

## Act end

Level 35 ends Act 5 and follows Level 28's order:

1. The Sovereign's death: the chain (the batteries cooking off ring by ring, the rings cracking, the core's implosion
   and blast), the screen flash of an act boss, the credit shower over the core (collectable: the scroll is halted).
2. **The wreck** (section 6): no enemies, the closing lines in order (Okafor, Vorne's offer, the answer, Varga's
   Callisto, Ceres Control), the debrief waiting for the last.
3. The **debrief with the act summary** (`mission-complete` jingle, the tally, the summary `ACT V COMPLETE · THE
   BELT`).
4. The **act outro**, four voiced pages with their images (the texts and the data are the
   [Act 5 README](../README.md)'s: the Sovereign falls, Varga reads the captured data to Callisto, Varga on Vorne's
   face, Rook's line, Okafor's "Callisto"), with `act-complete` under page 1.
5. **The release's end**, moved from Level 28 to Level 35 (M9 part H, the `v0.3` release): the end card `ACTS I-V
   COMPLETE … TO BE CONTINUED / ACT VI - JOVIAN STORM. CALLISTO. COMING SOON.`, the name entry and the credits, then
   the main menu; Continue opens the hangar before Level 36, which reads `ACT VI - JOVIAN STORM: COMING SOON` and cannot
   launch (Level 28's `ACT V - THE BELT: COMING SOON` goes). The next act's name comes from the `next_act` key in Act
   5's data. `--act-end` works unchanged for this level.

## Credit budget

Budget(35) = 700 × 1.07³⁴ ≈ **6,985** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act 1
terms** (Talon 12, leader 25, Skitter 5, Mote Swarm 2 a mote, Gilded Gunship 28, Hornet 26, Rail Serpent 73 a
dismantled train, Shard Drone 14, Dart Drone 2, SAM Nest 15, Rail Bunker 45, the boss 433, the crate 100, the secondary
100), × the act factor **6.5536** (1.6⁴), bounties also × the level's scale. The draft's hand scale 0.56 (7,024 by
hand) is **0.59** measured (step H2, `levelReport`: the typical haul **6,948, −0.5 %** at medium, the perfect run 1.50 ×,
with the economy's shares: 60 % of the air kills and launched Darts, 80 % of the ground, half the secret and the
secondary, the boss and its 52 expected Darts at 100 % / 60 %); the boss's bounty follows the scale so it keeps the
act-boss share of **24 %** (the perfect-run ≥ 1.4 × rule; Levels 21 and 28). The table's rule gives the same.
Re-measured with the fight won (step H3: the core tuned to 14,000, the fight ≈ 149 s, its bays launching **84** Darts
at medium): the typical haul **7,101, +1.7 %** (0.58 would hit it exactly), the perfect run 10,717 (1.53 ×): the scale
**0.59** is kept, the boss's bounty 433 with it. Step BOSS70 (round 70 built: the HP moved from the core to the rings,
par 145, the core's playbook): its Darts come only in the playbook's surges, ≈ **19** a fight at medium (8 surges of
12 over five fights), the boss's `notes.spawns` 84 → 19: the typical haul **6,789, −2.8 %** (0.61 would hit it), the
perfect run 10,197 (1.46 ×): the scale **0.59** is kept, the bounty unchanged.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Talon 54 × 12 + Skitter 24 × 5 + Mote Swarm 38 × 2 + Gilded Gunship 4 × 28 + Hornet 5 × 26 + Rail Serpent 2 × 73 + Shard Drone 4 × 14 + Dart Drone 5 × 2 + launched Dart Drone 24 × 2 | 5,197 | 3,118 |
| Squad leaders: 6 × 13 extra (perfect run only) | 306 | 0 |
| Ground targets: SAM Nest 9 × 15 + Rail Bunker 6 × 45 | 1,566 | 1,253 |
| Boss: Iron Sovereign (core 355 + 13 batteries × 6) | 1,672 | 1,672 |
| Boss spawns: Dart Drone 19 × 2 (a typical fight) | 152 | 91 |
| Secret: Ceres lifeboat (hidden crate, 1% of budget) | 655 | 328 |
| Secondary: break the screen | 655 | 328 |
| **Total** (bounty scale 0.59) | **10,203** | **6,789** |
| Budget(n) = the typical haul's target; typical -3 %, perfect 1.46 × budget | | 6,985 |
<!-- /data -->

**Balance plan**: the plan's L35 visit buys the **Mk V "Helix"** (salvaged in Level 34) and raises both Micro-missile
Pods to L4 (the [Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)): forward DPS 147 against
the proposed reference **144** (J2), 21 of 22 MW. The level expects `piercing` and `anti-ground`; the plan's fit has
neither and flies it on its front guns and pods at the boss spec's paper time (≈ 145 s). The fight's measurement uses
the plan's Level 35 fit (part H; `PacingTest`'s stand-in from step H2, the Ion Beam L4, the Pods L4 ×2, the Swarm Tail
L3, the Shield Mk III, Composite IV, the EMP Burst and Rook, 21 of 22 MW).

## Difficulty notes

- **Easy**: the formation lever thins the wings and the popcorn; one Gunship in each column (fewer Darts); the
  Hornets' and the hull guns' easy hooks (their pages); the mass-relay flash's telegraph 1.5 s; the boss ×0.75 with its
  easy hooks (the fire arc 90°, the batteries' intervals ×1.3, the spiral's 2 arms, no core rail); the checkpoint.
- **Hard**: the wings larger; a third Gunship in the t=56 column; the hull's SAM groups of 4; the lifeboat's trigger 28
  HP; the boss ×1.3 with its hard hooks (the rings ×1.2 faster, the arc 150°, the intervals ×0.8, 3 spiral arms, Dart
  streams +1); no boss checkpoint.

## Concept art

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

The level needs (part H's production art; the boss's proposals are in the
[Iron Sovereign](../../../enemies/bosses/iron-sovereign/README.md)'s round-60 concept, the belt's mood stills for the
orbit): the orbit backdrop, the station's outer hull with its gun mounts, the rings' edge rolling in, the arena under the
halted station, the wreck's drifting plates; the mass relay's line and flash; the Ceres lifeboat and the CDF beacon; the
two briefing images; the intel silhouette of the Sovereign; the act's outro images and Vorne's production portraits;
the round's captures (the level at medium with the plan's fit and Rook: the escort screen, the flash, the hull, the
fight's phases, the death, the recruitment, the debrief and the act end; 1:1 crops of the rings and the fire arc's band).

Production art, M9 part H (concept round 68, step ART-H2): the backdrop (`tools/art/backdrop_l35.py`: Ceres orbit, the
outer hull as a tile set with its two ends cut from the same field, the neck to the station's centre under ART-H1's
rings, Ceres's pole under the arena; its places for step H2 in [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml):
the sections, the arena's rows, the hull guns on their pads, the lifeboat, the beacon's drop and a light debris block),
the mass relay's line and flash in both QH-2 variants and the Ceres lifeboat (`tools/art/l35_props.py`, held in
`build/art-held/` until the data and the game claim them). The CDF beacon is Level 33's nav buoy. The two briefing images
and the Act 5 outro's four are in the [briefing](../../../ui/briefing/README.md#concept-art); the station, its intel
silhouette and its effects are ART-H1's, in the [boss's document](../../../enemies/bosses/iron-sovereign/README.md#concept-art).
Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r68-a.png](concept/backdrop-final-r68-a.png) | Production backdrop (`tools/art/backdrop_l35.py`): eight screens at half size with stand-ins (Ceres's curve with the hub red and the loyalist wrecks, the tenders far below, the hull's south bulkhead, the nests and a bunker pair on their pads, the relay's telegraph, the lifeboat in its clamps, the north bulkhead and the neck as the rings roll in, the halted station over Ceres's pole), the small pieces, the tile and the long pieces (sheet) | chosen (round 68, H.3 ok: accepted as final) |
| [concept/backdrop-final-r68-a.gif](concept/backdrop-final-r68-a.gif) | t 82-93 at half size: the hull's end, the neck and the station rolling in to the halt | chosen (round 68, H.3 ok: accepted as final) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block, its reading of the sections (the halt eased down before the arena), the arena's rows and the station's centre, the hull guns' and the lifeboat's places on their pads, the beacon's drop and a light `debris:` for step H2 (not data) | proposed |
| [concept/relay-flash-final-r68-a.png](concept/relay-flash-final-r68-a.png) | QH-2 **a** (picked in round 68: the game's relay flash): the red line from the glowing point on the right edge brightening over the telegraph, the white-hot 16 px bar along the whole row with a short screen flash, the ion trail (eight moments at half size, the strips at 2x; sheet) | chosen (round 68, H.4 a picked: the built relay flash) |
| [concept/relay-flash-final-r68-a.gif](concept/relay-flash-final-r68-a.gif) | QH-2 a: t 59.6-62 at half size on the hull, the ship on the locked row | chosen (round 68, H.4 a picked: the built relay flash) |
| [concept/lifeboat-final-r68-a.png](concept/lifeboat-final-r68-a.png) | The Ceres lifeboat (`tools/art/l35_props.py`) in the station's clamps on the hull at 1x and 2x: the beacon's blink and the six frames of it shot open (sheet) | chosen (round 68, H.5 ok: accepted as final) |
| [concept/relay-capture-r68-a.mp4](concept/relay-capture-r68-a.mp4) | Game capture (step H5; [recipe](concept/prompts.md#level-35-capture-r68-a)), clip with sound: the mass relay's line, flash and trail on the ship's row (variant a, as built) | chosen (round 68, accepted with the level: C.2, C.7, H.4, H.5) |
| [concept/relay-capture-r68-a.png](concept/relay-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#level-35-capture-r68-a)), frames: the mass relay's line, flash and trail on the ship's row (variant a, as built) | chosen (round 68, accepted with the level: C.2, C.7, H.4, H.5) |
| [concept/lifeboat-capture-r68-a.mp4](concept/lifeboat-capture-r68-a.mp4) | Game capture (step H5; [recipe](concept/prompts.md#level-35-capture-r68-a)), clip with sound: the Ceres lifeboat under the beam, its crate drifting out (the capsule stays drawn whole) | chosen (round 68, accepted with the level: C.2, C.7, H.4, H.5; stale since FIX3 slowed the opening to 1 s: the record of the round; re-recorded as `lifeboat-capture-r69-a`) |
| [concept/lifeboat-capture-r68-a.png](concept/lifeboat-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#level-35-capture-r68-a)), frames: the Ceres lifeboat under the beam, its crate drifting out (the capsule stays drawn whole) | chosen (round 68, accepted with the level: C.2, C.7, H.4, H.5; stale since FIX3 slowed the opening to 1 s: the record of the round; re-recorded as `lifeboat-capture-r69-a`) |
| [concept/lifeboat-capture-r69-a.mp4](concept/lifeboat-capture-r69-a.mp4) | Game capture (step I3; [recipe](concept/prompts.md#level-35-capture-r69-a)), clip with sound: the Ceres lifeboat shot open mid-screen, its six open frames over 1 s and the crate drifting out (round 69 B.4) | chosen (round 69, B.4 ok) |
| [concept/lifeboat-capture-r69-a.png](concept/lifeboat-capture-r69-a.png) | Game capture (step I3; [recipe](concept/prompts.md#level-35-capture-r69-a)), frames at 1:1: the lifeboat opening, t=67.3–68.5 (round 69 B.4) | chosen (round 69, B.4 ok) |
| [concept/hull-guns-capture-r68-a.mp4](concept/hull-guns-capture-r68-a.mp4) | Game capture (step H5; [recipe](concept/prompts.md#level-35-capture-r68-a)), clip with sound: the hull's Rail Bunkers and SAM Nests firing on the approach | chosen (round 68, accepted with the level: C.2, C.7, H.4, H.5) |
| [concept/hull-guns-capture-r68-a.png](concept/hull-guns-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#level-35-capture-r68-a)), frames: the hull's Rail Bunkers and SAM Nests firing on the approach | chosen (round 68, accepted with the level: C.2, C.7, H.4, H.5) |
| [concept/halt-capture-r68-a.mp4](concept/halt-capture-r68-a.mp4) | Game capture (step H5; [recipe](concept/prompts.md#level-35-capture-r68-a)), clip with sound: the station rolling in to the halt with the arena warning | chosen (round 68, accepted with the level: C.2, C.7, H.4, H.5) |
| [concept/halt-capture-r68-a.png](concept/halt-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#level-35-capture-r68-a)), frames: the station rolling in to the halt with the arena warning | chosen (round 68, accepted with the level: C.2, C.7, H.4, H.5) |
| [concept/tenders-before-after-r69-a.png](concept/tenders-before-after-r69-a.png) | Before-and-after sheet (step I3; [recipe](concept/prompts.md#tenders-before-after-r69-a)): the Sovereign's tenders as built (pinned grey-gold) and rendered black like Level 34's Helix, 2x (round 69 B.6) | chosen (round 69, B.6 ok) |

## Implementation

- [x] Level data `data.yaml` (M9 part H) with the sections, waves, ground targets, radio, budget, threat profile,
      briefing, backdrop and music; the arena a one-second halted section with `hold_after_boss: 6` (step H2, on
      ART-H2's proposal; the halt a 6.5 s section eased to 1 px/s before the arena)
- [x] Reference DPS 144 at L35 and the Act 5 balance plan's Level 35 fit (part A) before the data; the act HP factor
      for the returning `medium`+ units listed in *Waves* (M9 part A, step A2; the fit flown by `Level35Test`, step H3)
- [x] The approach to the Act 2+ rules (`ActFloorTest`, `PacingTest`, the pre-boss pause named as the third gap), the
      Gunships' Darts counted, edge and BOTTOM warnings for side and rear first entries (step H2: `levelReport` ok at
      medium, the plan's fit in `PacingTest.PLAN_FITS`; content checks nothing on the ground still on the field at the
      halt; step H3: the tests pass with the fight won, 3.79 on the screen, 89 shots a minute, the wreck after the kill
      the level's outro)
- [x] The mass-relay flash: the 1.2 s horizontal telegraph through the ship's position, the flash, no wave or bunker
      around it (the simulation's `RelayFlash`, step H1b; the level's `relay` and content's checks, step H2: no wave
      entering t 58.5–63.5, no line-firing unit on the field at the flash on any difficulty; its looks are step H4's)
- [x] The [boss](../../../enemies/bosses/iron-sovereign/README.md) arena and fight per its spec: the warning at t=88,
      the halt and the checkpoint at t=92 (none on hard), the bar; Rook and the autopilot on armed batteries first
      (steps H1–H4: the warning plays at the arena's start, t=92, as Level 28's; the station at (240, 160) after H3's
      `ScrollPlan.haltScroll` fix; `Level35Test.theCheckpointAndTheSecondary`; recorded in round 68's captures, step H5)
- [x] Primary `reach-end` with the arena as the condition, `DESTROY THE IRON SOVEREIGN`; secondary `kill_ratio` 65 %
      of the approach (the boss and its Darts not counted) (step H2; step H3: 99 of 151 planned units at medium, the
      Gunships' Darts count as kills; no tracker label, the validator forbids one)
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`), with Rook and without; Vorne on the enemy
      channel (the amber plate, the interference, the portrait's mood per line); the boss lines as `PHASE` events, the
      closing lines as `CLOSING` with `delay` from the kill; Rook's line at t=63, before the lifeboat's event (step H2:
      the lines one page each and voiced, Vorne `smug`/`rage`; step H4: `RadioTimelineLevel35Test` with and without
      Rook, the closing queue honouring `requires_not: escort`; heard in round 68's captures, step H5)
- [x] Threat profile (the wreck not on the strip), Varga's four intel lines and Okafor's teaser fitting the intel panel;
      the Sovereign's intel silhouette (step H2's data; the silhouette ART-H1's, round 68 H.2; step H4: the strip's relay
      tick at 61 s)
- [x] Briefing: both pages, each one screen, with their images (step H2; the images ART-H2's, round 68 H.6)
- [x] Typical haul within ±5 % of budget(35) with the level's `bounty_scale`; the boss's share inside 20–40 %
      (`BalanceTest`), its bounty recomputed with the measured scale (step H2: scale 0.59, the boss 433, `levelReport`
      −0.5 %; step H3 with the fight won and 84 Darts: +1.7 %, 0.59 kept; `TypicalHaulTest` 10,717; step BOSS70 with the playbook's 19 Darts: −2.8 %, 0.59 kept, 10,197)
- [x] Ground targets, the lifeboat secret (20 HP trigger, hard 28, crate 100), the overdrive, the shield cell, the
      beacon's armour patch and the boss's phase-4 patch (step H2; the autopilot shoots the lifeboat open, step H3; step
      H5's capture read the capsule as drawn whole after it is shot open; step FIX3 found the open frames do play (the
      0.4 s burst at the top edge under the beam, then the last frame, the capsule with its hatch blown, close to the
      whole one) and slowed the lifeboat's opening to 1 s, `Level33LooksTest`; its last frame is the art's, accepted in
      round 68's H.5)
- [x] Music: track 13's stems by section, out at the halt, the warning and track 19, the motif stem alone after the
      kill (`motif_after_boss`), the act-complete fanfare under the outro; the `belt-station` ambience (step H2's data;
      step H4: track 19 at full length imported as `iron-sovereign`; heard in round 68's captures, step H5)
- [x] Act end: the debrief with `ACT V COMPLETE · THE BELT`, the four outro pages with `act-complete`, the release's end
      moved here from Level 28 (`ACTS I-V COMPLETE`, `next_act`: `ACT VI - JOVIAN STORM. CALLISTO.`), the hangar before
      L36 reading `ACT VI - JOVIAN STORM: COMING SOON` (`--level 35 --act-end`) (steps H2 and H4, `OutroOnwardTest`;
      recorded in round 68's captures, step H5: the debrief, `ACT V COMPLETE`, the four outro pages, `ACTS I-V COMPLETE`)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on every difficulty
      (`--escort none` too) (step H3, `Level35Test`: `RingPlay` in the arena, the relay's line stepped off, the lifeboat
      shot open)
- [x] Production art and the round's captures (accepted in round 68, closed 2026-10-29; ART-H2: the backdrop, the relay, the lifeboat and the images, in the
      game; round 68 H.3–H.7; the captures of the relay, the hull's guns, the lifeboat, the halt and the act end in round
      68, step H5)

## Open questions

Approved in round 60 (the gap analysis's questions this level follows):

- **Q7, the fight** (recommended **a**, drafted: top-down rings, the scroll halted). Alternatives (the spec's
  [variants](../../../enemies/bosses/iron-sovereign/README.md#variants-b-and-c-for-the-rounds-abc)): **b** a scrolling
  fortress (the level crawls over three ring sections, then halts over the core: the approach's section 4 would become
  the fight's first three thirds and the level ≈ 60 s longer); **c** a tilted wheel (the same timeline).
- **Q6, the Dart Drones** (recommended **b**, drafted: the Gunships' 24 Darts carry ≈ 15 % of the approach's count).
  Alternatives: **a** mixed waves without Darts (≈ 100 a minute: more Vrell popcorn); **c** Talons in great numbers.
- **Q1, enemy shields** (recommended **a**: the Talons, Gunships and Hornets of the escort and the core's 80).
  Alternatives **b** shields only `shield-breaker`, beams and the EMP get through (the plan's fit could not open the
  core); **c** no shields in Act 5 (the core's phase 4 without its regrowth).
- **Q11, the Ascendancy look** (decided in round 60, **b**, not the recommendation): gunmetal/charcoal hulls with gold; rejected: **a** gloss black, **c** black silhouettes.
- **J2, the reference DPS 144** (the boss's HP and par use it).
- **The boss's bounty**: 457 in Act 1 terms at the hand scale 0.56 (the boss document follows since step m9-d-act);
  the measured scale decides.
- **The mass-relay flash's damage** 20 (two `heavy`), a draft: the belt's hazard gives no number.
- **Without Rook**, Okafor answers Vorne ("Our pilots are not for sale, Vorne."), a draft.

## Decisions

- 2026-10-27 (Act 5 design phase, step `m9-d-levels-3`; approved in round 60): drafted from the roster
  row, the gap analysis (§0, §1.1, §2.4, §2.8, Q1 a, Q6 b, Q7 a, Q11 a, J2), the boss's README (the source of truth for
  the fight, Vorne's lines and the death) and Level 28's document as the model (every section, the act-end order, hand
  tables marked draft, no `data.yaml`).
- 2026-10-27: the approach is the act's densest stretch, ≈ 114 a minute (175 in 92 s, the Gunships' Darts counted, Q6 b),
  no wave after t=83.5, the warning at t=88, the halt at t=92; no mid-boss (Act 5's pacing rule).
- 2026-10-27: the mass-relay flash fires once at t=61, a horizontal line through the ship (the relay off the right edge),
  with a quiet window and Ceres Control's warning.
- 2026-10-27: Level 35's `bounty_scale` is **0.56** (hand estimate), so the boss's bounty is **457** in Act 1 terms (24 %
  of budget(35)), recomputed from the spec's 341 at 0.75.
- 2026-10-27: the closing order is the boss spec's (Okafor, Vorne's offer, Rook's answer or Okafor's without him, Varga's
  Callisto) plus Ceres Control's last line (the hub's lights white again); the Choir is silent; Varga's look at Vorne's face
  is the outro's.
- 2026-10-27, design phase: Rook's t=69 line moves to **t=72**, after the lifeboat's event line (the secret, in reach
  t≈63–69), so the two never collide (step m9-d-act; approved in round 60).
- 2026-10-27: the act end follows Level 28's order; the release's end moves to Level 35 and names Act VI (`next_act`).
- 2026-10-27: the music follows the [Act 5 music plan](../../../audio/music/README.md#act-5-music-draft-round-60) (step
  m9-d-audio): track 13 on the approach, track 19 for the fight, the motif stem alone after the kill, `belt-station`.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`, D.8): Q7 **a** (top-down rings), Q9 **a** (Vorne's broadcast is the act intro), J.8 kept (Vorne cold, never shouting); the station is **A.17 b**, armoured links, with the **A.23 top-lit** atlas (about 0.6 pages); the rings' bed is S.16 a, the rail shot S.8 b. Q11 **b**: gunmetal/charcoal with gold.
- 2026-10-29 (M9 part H, step H2; decided in round 68, 2026-10-29): the level's data on ART-H2's proposal. Decided
  trivia of the gap check applied: every approach line one radio page and retimed (Okafor 1, Ceres Control 9.5, Rook 23,
  Varga 36, Okafor 46, Ceres Control 55.5), the t≈56 bunker pair at t=55.5, the serpent pincer two `snake` trains, the
  kill ratio 65 % of what the simulation counts (the draft's 104 of 160 → ≈ 114 of 175), the level's end 1 s after the
  last closing line (t=131.5, K+44.5). Step H2's own calls: **Rook's relay line at t=63** (right after the flash: the
  "rail gun" is the relay; t=72 left Varga and Okafor 2–3 s late behind the lifeboat's line), Varga t=76.5, Okafor
  t=83.5, the long lines shortened to one page (Okafor t=1 "Break the Sovereign's screen, Lancer. Then break the
  station.", the hull line "SAMs and rail bunkers on the hull…", Ceres Control's warning "…Red line: move!", the
  lifeboat line "One of our lifeboats. Empty. Take the stores, Aegis.", Varga's arena lines, her Callisto line "Its nav
  core took every order from one place. Callisto.", Ceres Control's last "Ceres to Aegis. The hub lights are white
  again. Thank you."); Vorne's lines kept word for word (his arrival two pages); Okafor's answer without Rook on the
  existing `requires_not: escort` (the gap check's `requires: no-escort` is the same rule); the secondary has no
  tracker label (a kill ratio shows none). Varga's "Kill a ring's guns" (her briefing page and her arena line) read
  "Killer rings guns" in every voiced take: reworded "Destroy a ring's guns", the arena line "The guns fire only on the
  side facing you. Destroy a ring's guns."
- 2026-10-29 (step H2, deviations decided in round 68, 2026-10-29): **the halt** is a 6.5 s section easing from 150 to 1 px/s over
  [85.5, 91.5], then the arena's own 1 s ease to rest (ground scroll 13,788, ART-H2's 13,785 + 3 px; its generator
  re-run on the data): the simulation allows a timed halt (speed 0 outside an arena) only with a defend primary, so
  ART-H2's 0.5 s timed halt could not be flown. **The rear wave at t=68.5 is a Dart Drone stream of 5** (BOTTOM warning),
  not the draft's Talon V-wing: the simulation flies a Talon only on its swoop, from the front or the sides, and a plain
  rear entry exists for chasers only; the Talons are 54 (6 leaders). Hard's fourth SAM Nest in each hull group stands on
  open plating (no pad).
- 2026-10-29 (step H2): `bounty_scale` **0.59** measured (the hand 0.56): `levelReport` typical haul 6,948 (−0.5 %), the
  perfect run 1.50 ×; the boss **433** in Act 1 terms (core 355, 13 batteries × 6), 24 % of budget(35); its 52 expected
  Darts in the haul (`boss.notes.spawns`). The approach measured at medium 3.60 on the screen, 1.04× Level 10's small
  units, one gap over 2 s (the pre-boss pause): no offbeat firing groups needed.
- 2026-10-29 (step H3; decided in round 68, 2026-10-29): the fight measured and tuned in the boss's spec (the core 14,000, the par
  150); the boss's typical Darts **84** (`notes.spawns`, was 52), the scale **0.59** kept (+1.7 %); the secondary's 65 %
  is of the approach's planned units (151 at medium: 99 kills; the Gunships' Darts count in the total as they come, the
  sim's rule; the README's "≈ 114 of 175" is the share of the counted total); the station's halt place fixed in the
  simulation (the halt's 6 s ease was left out of the anchored boss's place: it sat 456 px low).
- 2026-10-29 (M9 part H, step H5; decided in round 68, 2026-10-29): the level captured at medium with the plan's
  fit and Rook (the relay's line and flash, the hull's guns, the lifeboat, the station arriving at the halt with the
  arena warning, the fight, the debrief and the act end). Found: the **Ceres lifeboat stays drawn whole** after it is shot
  open (its crate drifts out at t≈66.3 and the debrief counts the secret, but the open frames never show), a follow-up
  for the next game step; standing still at medium without `--invulnerable` the plan's fit is lost at t≈85 (not a
  finding about the autopilot, which wins every seed).
- 2026-10-29 (step FIX3): H5's lifeboat finding checked at 1:1 on Xvfb (the ship at the right edge, the fan on it):
  the simulation destroys the lifeboat and `LeechFieldLooks` plays its six open frames and leaves the last in the
  clamps, as Claim 114's; in H5's run the 0.4 s burst played at the top edge under the Ion Beam and the last open frame
  (the capsule, its hatch blown dark) reads close to the whole one. The lifeboat now opens over 1 s (10 steps a frame;
  Claim 114 keeps 4). Whether the last frame should read more clearly open is art (round 68's H.5, the user's call).
  Looks only, checked and left: the spin-up plays mostly under the `WARNING` banner (FIX3's stills t 93–96); the
  death's segment fade and the bay Darts on the dark rings were not re-captured (no kill run in FIX3).
- 2026-10-29 (user, [round 68](../../../concept-rounds/round-68/README.md), closed): the art accepted (**H.3** the backdrop, **H.5** the Ceres lifeboat), **H.4 a** (the built relay flash: the bar along the whole row with the short full-screen flash and the fading ion trail; b, the slug, rejected, `concept/rejected/`), the calls **C.2, C.3, C.4, C.7, C.8 accepted** (the halt, the rear wave, the radio, the bays and SAMs, the bounty scale 0.59); the sounds of the relay **S.11 a** (`relay-charge`), **S.12 a** (`relay-flash`). The part's `art:` is `final`.
- 2026-10-29 (user, [round 69](../../../concept-rounds/round-69/README.md), closed): **B.4 ok** (the lifeboat's 1 s
  opening) and **B.6 ok** (the tenders kept grey-gold, as accepted in round 68's H.3); the clips are `chosen`.
  **B.1 ok**, with the note that the rail lines must lead the ring's turn: built in step FIX5 (the Iron Sovereign's
  page).
- 2026-10-30 (step FIX6, the user's playthrough 2026-10-30: "the boss warning is too late, it should show before the
  boss"): the station's rim enters the top edge at t=86 (it scrolls in with the hull, 440 px across, its centre at
  y 758), while the warning box, the klaxon and the warning track opened at its arrival (t=92), 6 s after. The boss
  placement takes round 41's `incoming: 8` (Level 14's key): the warning opens at **t=84**, 2 s before the first
  pixel, on every difficulty (`Level35Test.theWarningOpensBeforeTheStationIsOnTheScreen`, from the sim); the box
  closes after its 5 s as the station comes in. A retry from the boss checkpoint warns at the arrival. The fight is
  unchanged (its rework is a later step).
- 2026-10-30 (step BOSS70): round 70's Sovereign rework built ([the boss](../../../enemies/bosses/iron-sovereign/README.md#the-round-70-rework-decided-round-70)):
  the HP shift (par 145; the fight 144.3 s at medium over five seeds) and the core's playbook; five boss lines added
  (Rook at the first rail volley and the first wreck throw, Okafor at the first mine ring and after the first throw,
  Vorne at the core's half: the events `first-rail-volley`, `first-mine-ring`, `first-wreck-throw`, `core-low`); the
  boss's typical Darts 84 → 19 (the surges only), the typical haul −2.8 %, the scale 0.59 kept.
- 2026-10-30 (step FIX7, the big-hull check `HullStackingTest` after the user's re-test of Levels 30–31): the Gunship
  columns (t=20, t=56) fly 2 s apart as written (they entered 0.4 s apart, stacked); the Hornets keep apart while they
  hold; the t=35 pincer's left train enters at the right train's height under the Hornets' hold row (its first points
  (−98, 200), (100, 200), was (−60, 110), (100, 140); as long a path to the crossing, so the two trains still pass each
  other clear). No big-hull overlap; the fight unchanged (its sweep bound in `Level35Test` 8 → 10: Rook's seed 11
  meets the arena in another state).
- 2026-10-30, the user: dither audit, ≥ 25 % smoothed (step DITH8): the deep `orbit-space` and the mass relay's `mass-relay-glow` in smooth steps, not the 4×4 Bayer checker ([audit](../../../art-direction/production/README.md#dither-audit-2026-10-30)).
