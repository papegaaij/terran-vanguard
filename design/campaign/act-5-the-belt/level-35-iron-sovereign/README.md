---
title: Level 35 – Iron Sovereign
design: draft
implementation: not-started
art: none
depends-on: [../../../enemies/bosses/iron-sovereign, ../../../enemies/air/talon, ../../../enemies/air/gilded-gunship, ../../../enemies/air/hornet, ../../../enemies/air/rail-serpent, ../../../enemies/air/dart-drone, ../../../enemies/space/shard-drone, ../../../enemies/ground/rail-bunker, ../../../enemies/ground/sam-nest, ../../../world/belt, ../../../player/wingmen, ../../../story/characters/vorne, ../../../systems/economy]
updated: 2026-10-27
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

**Status: draft** (Act 5 design phase, step `m9-d-levels-3`, 2026-10-27), **pending the user's review in
concept round 60**. Written from the act roster, the gap analysis's recommended answers (**Q7 a** the top-down
rings, **Q1 a** the core's and the escort's shields, **Q6 b** the Dart Drones, **Q11 a** the belt look; judgement
call **J2** the reference DPS 144, all pending in round 60) and the wave 1 and 2 documents. The boss document owns
the fight (rings, batteries, phases, HP, tells, Vorne's lines and their wording); this document owns the approach,
the timeline, the radio's order and delays, the economy and the act end. Every number is a hand-written first
draft at medium for part H to measure with `levelReport`; there is no `data.yaml` yet.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

> **Commander Okafor:** "The Iron Sovereign holds Ceres orbit: Vorne's battle station. Ceres Control is still
> on our side and will guide you in. Break its escort, then break the station, Lancer."
>
> **Dr. Varga:** "Three gun rings turn round a reactor. The batteries only fire on the side that faces you.
> Kill a ring's guns and it locks. Lock all three and the core opens."

*Images* (production art in part H; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-35-ceres-orbit`: a tactical chart of Ceres orbit: the approach from the graveyard lane,
  the escort screen as gold contacts, the station's outer hull, the mass relay on Ceres's limb marked with a red
  bearing line, the Sovereign at the north pole.
- Varga's page `level-35-sovereign-rings`: her plan of the station from above: three rings with their battery
  types (SAM, rail, laser) and their turning directions as arrows, the fire arc facing the ship shaded red, the
  core's six petals shut.

*Hangar teaser* (shop screen before L35; Okafor, not voiced):

> **Commander Okafor:** "The Sovereign is next. Whatever you salvaged, fit it now."

## Threat profile

Draft; directions are counted by each wave's `from` (the intel's rule): 87 % front, 10 % sides, 3 % rear; in the
fight the batteries fire from the whole station and are not in the numbers.

| Field | Value |
|---|---|
| Dominant layers | `air` (written `space`: the Talons, Gunships and their Darts, Hornets, the Rail Serpents, the Shard Drones, the Vrell popcorn), `ground` (the outer hull's SAM Nests and Rail Bunkers; the Sovereign itself, seen from above) |
| Attack directions | front 87% · sides 10% · rear 3% (the approach by its waves; the station's batteries in the fight fire from all round the arena) |
| Density | 5 |
| Recommended traits | `piercing`, `anti-ground` (the boss's columns of batteries and its `ground` layer; `homing` helps on the batteries in the arc) |
| Hazards | The mass-relay flash (once, t=61); SAM homing diamonds and rail lines on the hull; the Sovereign's batteries, its core's fan, spiral, rail and Dart streams |
| Boss / mid-boss | Act boss [Iron Sovereign](../../../enemies/bosses/iron-sovereign/README.md) |
| Sensor-suite detail | **none**: the belt, Ceres orbit, `air` + `ground`, all directions. **L1**: + the directions, density 5, hazards "mass relay" and "rail lines", the objective `DESTROY THE IRON SOVEREIGN`. **L2**: + Talon, Gilded Gunship, Hornet, Rail Serpent, Shard Drone, Dart Drone, SAM Nest, Rail Bunker portraits; the boss's name and silhouette (`iron-sovereign`). **L3**: + `piercing` and `anti-ground` highlighted; the timeline strip marks the mass-relay flash (61 s) and the boss (92 s); 1 secret. |

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
  (**104 of 160** at medium, the Gunships' Darts included), judged at the level's end. The Sovereign and the Darts
  it launches are not counted (`boss_counted: false`). Pays **100** in Act 1 terms (655 in Act 5); the typical haul
  counts it at 50 %. No radio line.

## Layout

Scroll **160 px/s** through the escort screen, **150 px/s** over the outer hull, then slowing to a **halt** over
the station (eased over the last 6 s, so nothing pops in: the station has been scrolling into view with the
ground since the hull, [the boss's arena](../../../enemies/bosses/iron-sovereign/README.md#arena)). Setting:
[the belt](../../../world/belt/README.md)'s Ceres orbit, vacuum
([vacuum rules](../../../world/belt/README.md#vacuum-rules)). **Light**: Ceres's sunlit curve far below in the deep
layer, the dust band, the station's gloss black and gold under the top-left sun with its red running lights
(Q11 a); a step lighter than Act 4 so the black reads. Motion budget: the escort's waves and the Darts are the strong
elements in the approach; in the arena the rings' turning and the fire arc's red band; after the kill the drifting
ring segments.

**The arena and the clock.** The boss warning (track 22) sounds at **t=88**, with the scroll's last slowing; the
arena section starts at **t=92** (a one-second section `speed: 0, arena: true`, Level 19's yard and Level 28's
vent), with the bar `IRON SOVEREIGN`, the boss theme and the **boss checkpoint** (none on hard). The boss's intro
(≈ 8 s from the warning: the lights ring by ring, the spin-up, Vorne's first line) runs on to t≈96 of real time,
invulnerable until the rings reach speed. The level clock halts at the arena section's end while the station lives;
the kill resumes it after `hold_after_boss: 6` (the 5 s death chain, the flash and the credit shower over the core,
collectable, and one second more). **K** below is the kill: the clock reads **t=87 + K** from the moment it resumes
(K+6 = t=93).

**Rook and the escort.** Rook flies his slot and follows the boss's
[escort rules](../../../enemies/bosses/iron-sovereign/README.md#the-escort-rook) (the armed batteries first, out of a rail
line and a sweep's wedge, the Darts in reach). With `--escort none` the level is the same without him and his lines.

| Section | t (s) | Scroll (px) | Speed (px/s) | Layers and content | Purpose |
|---|---|---|---|---|---|
| 1. Ceres Orbit | 0–20 | 0–3,200 | 160 | `deep`: Ceres's curve, Ceres Hub's lights (red since Level 29), the dust band. `low-air`: loyalist wrecks and a dead Ceres patrol boat drifting. | Talons and popcorn; Ceres Hub Control comes on the radio. |
| 2. The Escort Screen | 20–50 | 3,200–8,000 | 160 | `low-air`: the Sovereign's tenders and docking spars far below. `air`: the escort. | The Gunships' Dart streams (t=20), the Rail Serpent pincer (t=35), the Hornets; Talon pincers from the sides. |
| 3. The Outer Hull | 50–80 | 8,000–12,500 | 150 | `ground`: the station's outer hull (black plating, gold trim, red lights): SAM Nest groups (t≈52, 64, 74), Rail Bunker pairs (t≈56, 70, 78), a Ceres garrison lifeboat clamped to the hull (t≈66, the secret). **The mass-relay flash** at t=61. | The hull's guns; the flash once; a rear V-wing (t=68.5). |
| 4. The Rings' Edge | 80–92 | 12,500–13,850 | 150 → 0 | `ground`: the outer ring's rim rolls into view, then the middle and inner rings and the core; the scroll at 150 until t=86, eased to 0 by t=92. | The last wave (t=83.5); the pre-boss pause (t≈84–92); the warning at t=88. |
| 5. The Sovereign (arena) | 92–93 | 13,850–13,850 | 0 | `ground`: the station filling the field, its centre at (240, 160) ([arena](../../../enemies/bosses/iron-sovereign/README.md#arena)). `deep`: Ceres far below. | The boss fight (≈ 145 s at medium); the scroll halted and the clock stopped while the station lives. |
| 6. The Wreck | 93–130 | 13,850–15,950 | 0 → 60 | `ground`: the broken rings drifting apart, slowly turning, the core's crater glowing; the scroll eases to 60 px/s over 2 s. No enemies. | The closing lines (K+6 to K+43): Okafor, Vorne's offer, the answer, Varga's data, Ceres Control; the level ends at t=130 (K+43). |

The section after the kill is the level's **outro for the pacing checks** (no enemies by design: `PacingTest` and
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
  warnings, the rear V-wing a BOTTOM warning, 3 s ahead.
- **Totals**: Talon 59 (7 leaders) · Skitter 24 · Mote Swarm 38 · Gilded Gunship 4 (+ 24 Darts) · Hornet 5 · Rail
  Serpent 2 (10-car trains) · Shard Drone 4.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 5 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Ceres Hub's loyalist wrecks drift below |
| 7.5 | 1 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 10 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front (left) | |
| 12.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 15 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 17.5 | 1 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 20 | 2 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | Each Gunship launches Dart streams (3 every 5 s, at most 6 alive): ≈ 12 Darts from this pair |
| 22.5 | 2 | pincer | [Talon](../../../enemies/air/talon/README.md) | 6 | sides | Edge warnings at t=19.5 |
| 25 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 27.5 | 2 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 30 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 32.5 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 35 | 2 | pincer | [Rail Serpent](../../../enemies/air/rail-serpent/README.md) | 2 | sides | Edge warnings at t=32; one train a side |
| 38.5 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 41 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 43.5 | 2 | single | [Hornet](../../../enemies/air/hornet/README.md) | 1 | front | Escorted by the Talons of t=38.5 |
| 46 | 2 | line abreast | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 4 | front | Their links lit 0.8 s after the last drone enters |
| 48.5 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 51 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 53.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 56 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | ≈ 12 more Darts; the second Gunship drops a shield cell |
| 58.5 | 3 | pincer | [Talon](../../../enemies/air/talon/README.md) | 6 | sides | Edge warnings at t=55.5; then **no wave until t=63.5**: the mass-relay flash (telegraph t=59.8, flash t=61) |
| 63.5 | 3 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 66 | 3 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 68.5 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | rear | From behind: BOTTOM warning at t=65.5 |
| 71 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 73.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 76 | 3 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 78.5 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 81 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 83.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The last wave: the pre-boss pause t≈84–92 |

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 3 | [SAM Nest](../../../enemies/ground/sam-nest/README.md) group ×3 on the hull (t≈52) | Bounty 15 each; homing diamonds, shootable, the Decoy Flares retarget them |
| 3 | [Rail Bunker](../../../enemies/ground/rail-bunker/README.md) pair (t≈56), off the screen by t=60 | Bounty 45 each; hardened |
| 3 | SAM Nest group ×3 (t≈64) | Bounty 15 each |
| 3 | The Ceres garrison lifeboat clamped to the hull (`ground` trigger, 20 HP, t≈66, x≈400) | The secret: frees a crate worth 100 (655) |
| 3 | Rail Bunker pair (t≈70), their lines never both on the ship's row | Bounty 45 each |
| 3 | SAM Nest group ×3 (t≈74) | Bounty 15 each |
| 3 | Rail Bunker pair (t≈78) | Bounty 45 each |

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
  range. Its event line, Ceres Hub Control: "*That's one of our lifeboats. Empty. Take the stores, Aegis. They'd
  want you to.*"
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
the boss document's** ([Vorne](../../../enemies/bosses/iron-sovereign/README.md#vorne), wording pending round 60):
this table places them and sets their delays. **The closing lines are queued as `CLOSING` lines in order**
(Level 21's rule: they never go stale and the debrief waits for the last); `delay` counts from the kill K.

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "The Iron Sovereign, Lancer. It holds Ceres for Vorne. Break its screen, then break the station." |
| t=11 | Ceres Hub Control | "Ceres Control, still on the dock channel. The Sovereign holds the north pole. I'll guide you in." |
| t=23 | Rook | "Gunships with darts. I'll take the darts, you take the boats." |
| t=36 | Varga | "Rail Serpents on both flanks. Hit the engine and the rest falls apart." |
| t=46 | Okafor | "The station's hull ahead. SAMs and rail bunkers. Flares if you have them." |
| t=57 | Ceres Hub Control | "The mass relay is turning on you! When the red line shows, move!" |
| Lifeboat destroyed (event) | Ceres Hub Control | "That's one of our lifeboats. Empty. Take the stores, Aegis. They'd want you to." |
| t=72 | Rook | "That rail cannon was aimed at us? I'm flattered." |
| t=75 | Varga | "The rings are turning. Three of them, guns on every ring." |
| t=82 | Okafor | "This is Vorne's station. Everything you have, Lancer." |
| Boss arrival (`boss-arrival`, the spin-up) | Vorne (enemy channel, *smug*) | the boss document's `boss-arrival` line |
| After it (queued as `PHASE`) | Varga | "Batteries fire only on the side that faces you. Kill a ring's guns and it locks." |
| First ring locked (`boss-phase`) | Vorne (enemy channel, *neutral*) | the boss document's first-lock line |
| Second ring locked (`boss-phase`) | Rook | "One ring left. It's running out of guns." |
| Third ring locked, the core opening (`boss-phase`) | Vorne (enemy channel, *rage*, heavier static, never raised) | the boss document's core-opening line |
| After it (queued as `PHASE`) | Varga | "The core's open! Its shield is thin; anything wears it down. A Tesla hit empties it." |
| Sovereign destroyed (`boss-destroyed`, `delay: 5`, `CLOSING`), at the flash | Okafor | "The Sovereign is down! Ceres orbit is ours, Aegis Wing." |
| Then (`CLOSING`) | Vorne (enemy channel, *neutral*, steady and warm again) | the boss document's recruitment line ("…Come to Callisto. Name your price.") |
| Then (`CLOSING`, `requires: escort`) | Rook | "Funny. We were just heading your way." |
| Then (`CLOSING`, without Rook: replaces his line) | Okafor | "Our pilots are not for sale, Vorne." |
| Then (`CLOSING`) | Varga | "The station's nav core is ours. Every order it took came from one place. Callisto." |
| Then (`CLOSING`) | Ceres Hub Control | "Ceres Control to Aegis. The hub's lights are coming back white. Thank you." |

Timeline check (text model): t=1 closes ≈ 10.4, t=11 ≈ 20.5, t=23 ≈ 29.6, t=36 ≈ 43.3, t=46 ≈ 53.6, t=57 ≈ 63.8 (the
flash's telegraph at 59.8 falls under the warning, as meant), t=72 ≈ 77.5, t=75 ≈ 81.3, t=82 ≈ 87.9, before the
warning at t=88; the lifeboat's event line (in reach at t≈63–69) waits behind Ceres Control's warning and is spoken
by ≈ 64.2–71.1, so **Rook's line is timed at t=72** (it was t=69, which started ≈ 2.5 s late when the secret was
taken); it closes ≈ 77.5. **Open for part H:** Varga's t=75 line then queues to ≈ 77.9 (≈ 2.9 s late, over the
1 s rule) and pushes Okafor's t=82 line to ≈ 84.6; part H retimes the two with `RadioTimelineTest` (e.g. Varga t=78,
Okafor t=84.5, closing ≈ 90.4 under the warning) or shortens a line. In the arena: Vorne's arrival line
(≈ 9.8 s of subtitle) then Varga's (≈ 8.2 s) fill the intro and the first seconds of phase 1. The closing queue from
K, in order: Okafor K+5.0 to ≈ K+11.1, Vorne ≈ K+11.5 to ≈ K+18.6, Rook (or Okafor) ≈ K+19.0 to ≈ K+23.6, Varga
≈ K+24.0 to ≈ K+32.4, Ceres Control ≈ K+32.8 to ≈ K+40.4; the level ends at **K+43** (t=130) and the debrief waits for
the last line.

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

- The arena starts at t=92 with the bar and the checkpoint; the warning sounds at t=88 with the scroll's last slowing
  (the spec's ≈ 8 s intro begins there). Nothing slides under the station: it has scrolled in with the hull.
- **Traits**: `piercing` (the columns where the rings' batteries line up) and `anti-ground` (×2 on the whole station,
  a `ground` unit, not hardened); the roster's pair, kept. `homing` shots help on the batteries in the arc.
- The checkpoint is recorded at the halt on easy and medium; none on hard (the
  [retry](../../../systems/retry/README.md) rules).
- **Bounty**: the spec first assumed a `bounty_scale` of 0.75 and 341 in Act 1 terms; with this level's hand scale
  **0.56** the spec's formula (≈ 256 ÷ the scale for 24 % of budget(35)) gives **457 in Act 1 terms** (core 379, each
  of the 13 batteries 6, as the spec's split keeps the batteries at 6), paid **1,677 = 24.0 %** of budget(35). The
  spec's number is recomputed with the measured scale in part H.
- **The Darts it launches** pay their own bounty (2 each, outside the 24 %) and count into the level's enemies, not
  its density; the kill-ratio secondary leaves them out (`boss_counted: false`).
- **Rook and the autopilot** must pick the armed batteries first, leave a rail line and a sweep's wedge and stay out of
  the core's 72 px when it opens, or the tests cannot finish the fight (the spec's lessons).

## Music & ambience

The [Act 5 music plan](../../../audio/music/README.md#act-5-music-draft-round-60)'s cue for this level:
[track 13](../../../audio/music/README.md#track-list), the Act 5 B theme: the **base stem** in section 1, the **full mix
from the escort screen** (t=20), Vorne's motif **on** (the Ascendancy's level), **out at the halt** over the station
(fading t=86–88); the boss warning (track 22) at t=88 and [track 19](../../../audio/music/README.md#track-list) "Iron
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
dismantled train, Shard Drone 14, Dart Drone 2, SAM Nest 15, Rail Bunker 45, the boss 457, the crate 100, the secondary
100), × the act factor **6.5536** (1.6⁴), bounties also × the level's scale. At ≈ 114 a minute a **`bounty_scale` of
0.56** puts the typical haul on the budget (**7,024, +0.6 %**), the perfect run at 1.48 × (estimated by hand with the
economy's shares: 60 % of the air kills and launched Darts, 80 % of the ground, half the secret and the secondary, the
boss at 100 %); `TypicalHaulTest` confirms it in the build. The boss keeps the act-boss share of **24 %** (the perfect-run
≥ 1.4 × rule; Levels 21 and 28).

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Talon 52 × 12 + leader 7 × 25 + Skitter 24 × 5 + Mote Swarm 38 × 2 + Gilded Gunship 4 × 28 + Hornet 5 × 26 + Rail Serpent 2 × 73 + Shard Drone 4 × 14 + the Gunships' Darts 24 × 2 | 5,457 | 3,274 |
| Ground targets: Rail Bunker 6 × 45 + SAM Nest 9 × 15 | 1,486 | 1,189 |
| Boss: Iron Sovereign (core 379 + 13 batteries × 6) | 1,677 | 1,677 |
| The boss's Darts (expected 52 × 2) | 382 | 229 |
| Secret: the Ceres lifeboat (hidden crate) | 655 | 328 |
| Secondary: break the screen | 655 | 328 |
| **Total** (bounty scale 0.56) | **10,312** | **7,025** |
| Budget(n) = the typical haul's target; typical +0.6 %, perfect 1.48 × budget | | 6,985 |

**Balance plan**: the plan's L35 visit buys the **Mk V "Helix"** (salvaged in Level 34) and raises both Micro-missile
Pods to L4 (the [Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)): forward DPS 147 against
the proposed reference **144** (J2), 21 of 22 MW. The level expects `piercing` and `anti-ground`; the plan's fit has
neither and flies it on its front guns and pods at the boss spec's paper time (≈ 145 s). The fight's measurement uses
the plan's Level 35 fit (part H).

## Difficulty notes

- **Easy**: the formation lever thins the wings and the popcorn; one Gunship in each column (fewer Darts); the
  Hornets' and the hull guns' easy hooks (their pages); the mass-relay flash's telegraph 1.5 s; the boss ×0.75 with its
  easy hooks (the fire arc 90°, the batteries' intervals ×1.3, the spiral's 2 arms, no core rail); the checkpoint.
- **Hard**: the wings larger; a third Gunship in the t=56 column; the hull's SAM groups of 4; the lifeboat's trigger 28
  HP; the boss ×1.3 with its hard hooks (the rings ×1.2 faster, the arc 150°, the intervals ×0.8, 3 spiral arms, Dart
  streams +1); no boss checkpoint.

## Concept art

None yet. The level needs (part H's production art; the boss's proposals are in the
[Iron Sovereign](../../../enemies/bosses/iron-sovereign/README.md)'s round-60 concept, the belt's mood stills for the
orbit):

- the orbit backdrop (Ceres's curve, the hub's lights, the loyalist wrecks), the station's outer hull as a scrolling
  ground tile with its gun mounts, the rings' edge rolling in, the arena under the halted station, the wreck's drifting
  ring segments; the mass relay's line and flash;
- the Ceres lifeboat and the CDF beacon props; the two briefing images; the intel silhouette of the Sovereign;
- the act's outro images (the Act 5 README's) and Vorne's production portraits (neutral, smug, rage; his page);
- the round's captures: the level at medium with the plan's fit and Rook: the escort screen, the flash, the hull, the
  fight's phases (a lock, the core opening, the Darts), the death, the recruitment, the debrief and the act end; 1:1
  crops of the rings and the fire arc's band.

## Implementation

- [ ] Level data `data.yaml` (M9 part H) with the sections, waves, ground targets, radio, budget, threat profile,
      briefing, backdrop and music; the arena a one-second halted section with `hold_after_boss: 6`
- [ ] Reference DPS 144 at L35 and the Act 5 balance plan's Level 35 fit (part A) before the data; the act HP factor
      for the returning `medium`+ units listed in *Waves*
- [ ] The approach to the Act 2+ rules (`ActFloorTest`, `PacingTest`, the pre-boss pause named as the third gap), the
      Gunships' Darts counted, edge and BOTTOM warnings for side and rear first entries
- [ ] The mass-relay flash: the 1.2 s horizontal telegraph through the ship's position, the flash, no wave or bunker
      around it
- [ ] The [boss](../../../enemies/bosses/iron-sovereign/README.md) arena and fight per its spec: the warning at t=88,
      the halt and the checkpoint at t=92 (none on hard), the bar; Rook and the autopilot on armed batteries first
- [ ] Primary `reach-end` with the arena as the condition, `DESTROY THE IRON SOVEREIGN`; secondary `kill_ratio` 65 %
      of the approach (the boss and its Darts not counted)
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`), with Rook and without; Vorne on the enemy
      channel (the amber plate, the interference, the portrait's mood per line); the boss lines as `PHASE` events, the
      closing lines as `CLOSING` with `delay` from the kill; Rook's line at t=72, after the lifeboat's event
- [ ] Threat profile (the wreck not on the strip), Varga's four intel lines and Okafor's teaser fitting the intel panel;
      the Sovereign's intel silhouette
- [ ] Briefing: both pages, each one screen, with their images
- [ ] Typical haul within ±5 % of budget(35) with the level's `bounty_scale`; the boss's share inside 20–40 %
      (`BalanceTest`), its bounty recomputed with the measured scale
- [ ] Ground targets, the lifeboat secret (20 HP trigger, hard 28, crate 100), the overdrive, the shield cell, the
      beacon's armour patch and the boss's phase-4 patch
- [ ] Music: track 13's stems by section, out at the halt, the warning and track 19, the motif stem alone after the
      kill (`motif_after_boss`), the act-complete fanfare under the outro; the `belt-station` ambience
- [ ] Act end: the debrief with `ACT V COMPLETE · THE BELT`, the four outro pages with `act-complete`, the release's end
      moved here from Level 28 (`ACTS I-V COMPLETE`, `next_act`: `ACT VI - JOVIAN STORM. CALLISTO.`), the hangar before
      L36 reading `ACT VI - JOVIAN STORM: COMING SOON` (`--level 35 --act-end`)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on every difficulty
      (`--escort none` too)
- [ ] Production art and the round's captures

## Open questions

Pending the user's review in round 60 (the gap analysis's questions this level follows):

- **Q7, the fight** (recommended **a**, drafted: top-down rings, the scroll halted). Alternatives (the spec's
  [variants](../../../enemies/bosses/iron-sovereign/README.md#variants-b-and-c-for-the-rounds-abc)): **b** a scrolling
  fortress (the level crawls over three ring sections, then halts over the core: the approach's section 4 would become
  the fight's first three thirds and the level ≈ 60 s longer); **c** a tilted wheel (the same timeline).
- **Q6, the Dart Drones** (recommended **b**, drafted: the Gunships' 24 Darts carry ≈ 15 % of the approach's count).
  Alternatives: **a** mixed waves without Darts (≈ 100 a minute: more Vrell popcorn); **c** Talons in great numbers.
- **Q1, enemy shields** (recommended **a**: the Talons, Gunships and Hornets of the escort and the core's 80).
  Alternatives **b** shields only `shield-breaker`, beams and the EMP get through (the plan's fit could not open the
  core); **c** no shields in Act 5 (the core's phase 4 without its regrowth).
- **Q11, the Ascendancy look** (recommended **a**). Alternatives **b** gunmetal with gold, **c** black silhouettes.
- **J2, the reference DPS 144** (the boss's HP and par use it).
- **The boss's bounty**: 457 in Act 1 terms at the hand scale 0.56 (the boss document follows since step m9-d-act);
  the measured scale decides.
- **The mass-relay flash's damage** 20 (two `heavy`), a draft: the belt's hazard gives no number.
- **Without Rook**, Okafor answers Vorne ("Our pilots are not for sale, Vorne."), a draft.

## Decisions

- 2026-10-27 (Act 5 design phase, step `m9-d-levels-3`; pending the user's review in round 60): drafted from the roster
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
  t≈63–69), so the two never collide (step m9-d-act; pending the user's review in round 60).
- 2026-10-27: the act end follows Level 28's order; the release's end moves to Level 35 and names Act VI (`next_act`).
- 2026-10-27: the music follows the [Act 5 music plan](../../../audio/music/README.md#act-5-music-draft-round-60) (step
  m9-d-audio): track 13 on the approach, track 19 for the fight, the motif stem alone after the kill, `belt-station`.
