---
title: Level 34 – Salvage Run
design: approved
implementation: in-progress
art: proposed
depends-on: [../../../enemies/ground/warden-tank, ../../../enemies/air/talon, ../../../enemies/air/needler, ../../../enemies/space/asteroid-mite, ../../../player/generator, ../../../player/systems, ../../../world/belt, ../../../player/wingmen, ../../../systems/economy]
updated: 2026-10-29
---

# Level 34 – Salvage Run

## Summary

The breather before the Iron Sovereign. On the lane to Ceres lies a ship graveyard: derelict CDF and Helix
hulls drifted together after old battles, picked over by the Ascendancy. The
[Warden Tank](../../../enemies/ground/warden-tank/README.md) of Tharsis is back on the hulls in Ascendancy
black and gold (judgement call J1), a few Talon wings and the Vrell popcorn fill the sky, and every wreck hides
something: six cargo containers, three hidden crates (the Salvage scanner's level) and, in a Helix freighter's
hold, the **Mk V "Helix" generator** the wing salvages (Q12 c; in the shop from the L35 visit). Density 2 by
threat (≈ 135 a minute by count with step G2's firing groups; the draft's 86), no boss, 186 s: the graveyard's edge, the CDF picket, the Helix hulks, the
freighter, the tangle and clear space.

**Status: approved** (Act 5 design phase, step `m9-d-levels-3`, 2026-10-27), **approved by the user in concept round 60 (2026-10-27)**. Written from the act roster, the gap analysis's recommended answers (**Q12 c** the Mk V
salvaged here, **Q11 b** the belt look (gunmetal/charcoal with gold), **Q1 a** the Talons' shields; judgement calls **J1** the Warden Tank for
the Crawler Tank and **J2** the reference DPS 140, all decided in round 60) and the wave 1 documents. Every number
is a hand-written first draft at medium for part G to measure with `levelReport`. **Built in M9 part G** (step G2,
2026-10-29): the level script is [data.yaml](data.yaml); the marked tables below are rendered from it and measured
with `levelReport`.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

<!-- data: briefing -->
> **Commander Okafor:** "Before the Sovereign, we restock. A ship graveyard lies on the lane to
> Ceres, our hulls and Helix's. The Ascendancy has tanks on the wrecks. Take what you can carry,
> Lancer."
>
> **Dr. Varga:** "Helix freighters went down out there with their holds sealed. If one carried
> Helix tech, I want it. Their generators run hotter than ours."
<!-- /data -->

*Images* (production art in part G; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-34-graveyard`: a chart of the graveyard: the lane through it, the CDF picket's broken
  frigates in grey, the Helix hulks in black and gold, the big freighter marked with a gold box, the tanks' roads
  along the hulls as dotted lines.
- Varga's page `level-34-helix-hold`: a salvage scan of the freighter's hold: a crate in four clamps, the Helix
  emblem on its seal, the power signature of a fusion core beside the Stormhawk's Mk IV for scale.

*Hangar teaser* (shop screen before L34; Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "A salvage run. Finally, a mission where we get to keep things."
<!-- /data -->

## Threat profile

Directions are counted by each wave's `from` (the intel's rule): every wave from the front (the Stingers'
columns and the Skitter snakes at a side of the front).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `ground` (the derelict hulls: Warden Tank convoys on their roads, the containers, the hidden crates, the freighter's hold), `air` (written `space`: Needler lines, three Talon wings and a Talon line, the Vrell popcorn and the Asteroid Mites on their rocks) |
| Attack directions | front 100% |
| Density | 2 |
| Recommended traits | `anti-ground`, `spread` |
| Hazards | The tanks' bursts, also back up the screen; drifting asteroids; mining charges on the wrecks (they never hurt the ship) |
| Boss / mid-boss | none |
| Sensor-suite detail | none: the belt, the ship graveyard, `ground` + `air`, front · L1: + direction, density 2, hazards "tank fire" and "mining charges", the objective `CROSS THE GRAVEYARD` and the secondary `CONTAINERS 5/6` · L2: + Warden Tank (Ascendancy livery), Needler, Talon, Stinger, Asteroid Mite, Skitter, Mote Swarm portraits; the Helix crate marked as an objective pickup · L3: + the three hidden crates' sections on the timeline strip, `anti-ground` highlighted |
<!-- /data -->

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "A ship graveyard. Quiet, for once. Look in every wreck." |
| L1 | "Tanks on the hulls, a few fighters. Crates everywhere." |
| L2 | "The grey tanks of Tharsis, in Ascendancy colours now." |
| L3 | "Three hidden crates, six containers. Bring a scanner." |

## Objective

- **Primary** `reach-end`: cross the graveyard (`CROSS THE GRAVEYARD`).
- **Secondary** "Strip the graveyard" (`escapes: cargo-container`, label `CONTAINERS`): open at least **5 of the
  6** cargo containers before they leave the screen (easy 4, hard all 6), failed by the second container away.
  Pays **100** in Act 1 terms (655 in Act 5); the typical haul counts 50 %.
- **The Helix core** (Q12 c, decided in round 60) is a story pickup, not an objective: see *The freighter*.

## Layout

Scroll **130 px/s** throughout (a slow drift for looking; the breather's pace), **110 px/s** past the freighter's
hold (t=105–135; the data eases each change over 2 s). Setting: [the belt](../../../world/belt/README.md)'s ship
graveyard in vacuum ([vacuum rules](../../../world/belt/README.md#vacuum-rules)). **Light**: the belt's top-left sun
raking across the hulls (long hard shadows), the CDF wrecks' faded white-grey, the Helix hulks' black and gold
catching the sun, a few emergency lights still blinking red; the dust band in the deep layer. Motion budget: the
tanks and the popcorn are the strong elements, the hulls' slow turn the second.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Graveyard Edge | 0–25 | 0–3,250 | 130 | clear | `ground`: scattered hull plates and a broken CDF corvette's spine. `air`: small drifting rocks with Mites. `deep`: the dust band, far wrecks. | Popcorn and Needlers; the first container (t≈18). |
| 2. The CDF Picket | 25–65 | 3,250–8,450 | 130 | clear | `ground`: three broken CDF frigates side by side, their dorsal walkways the tanks' roads (convoys t≈32, t≈50), a mining charge on a frigate's flank (t≈51), two containers (t≈38, t≈58); the frigate *Vigil*'s bridge (t≈56, the first secret). | The tanks in livery taught; the first Talon wing (t=39). |
| 3. Helix Hulks | 65–105 | 8,450–13,650 | 130 | clear | `ground`: gutted black-and-gold Helix ore freighters, tank convoys along their keels (t≈70, t≈88), a mining charge (t≈89), a container (t≈80); the Helix courier *Sable Wren* wedged between two hulls (t≈96, the second secret). | Tanks and Needlers; the sky quiet between waves. |
| 4. The Freighter | 105–135 | 13,650–16,970 | 110 | clear | `ground`: one huge Helix freighter, its hold open to space; **the Helix core** in four clamps in the hold (t≈108–120); a tank pair on its far deck (t≈123). No air wave from t=113 to t=122. | The salvage set piece (Q12 c). |
| 5. The Tangle | 135–172 | 16,970–21,760 | 130 | clear | `ground`: wrecks of both fleets tangled together, the tanks' last convoys (t≈140, t≈158), a mining charge (t≈141), two containers (t≈150, t≈170); the prospector's tender *Old Marta* under a Helix hull (t≈165, the third secret). | The level's busiest stretch (still a breather). |
| 6. Clear Space | 172–186 | 21,760–23,580 | 130 | clear | `ground`: the last plates falling behind. `deep`: Ceres ahead, a hard white point, and beside it a ring of gold lights, the Sovereign. | Release; the level ends at t=186. |
<!-- /data -->

**The tanks** ([Warden Tank](../../../enemies/ground/warden-tank/README.md#act-5-the-return-in-livery-draft)): convoys
of 2–3 on roads along the hulls' walkways and keels, turning at the plates' seams; their bursts reach back up
the screen after they pass, the level's main threat. HP with the act HP factor of their page (×140/92).

**Mining charges** (the belt's [hazard](../../../world/belt/README.md#hazards--set-pieces)): three on the wrecks, each
beside a tank road (t≈50, 88, 140), so a shot charge takes a passing convoy with it; never the ship.

**Containers** (the caches): six CDF and Helix cargo containers on the hulls (t≈18, 38, 58, 80, 150, 170), `ground`
destructibles of **12 HP** with no bounty; destroyed, each releases a **salvage pickup of 50** in Act 1 terms (328),
drifting with the ground. The Salvage scanner's bonus applies (outside the budget,
[ship systems](../../../player/systems/README.md#salvage-scanner-user-decision-d7-of-m5-part-a)). Homing shots and Rook
never pick a container.

**The freighter** (Q12 c, decided in round 60): the hold scrolls in at t≈108; the crate (≈ 60 × 40 px, the Helix
emblem on its seal) sits in four clamps (`ground` triggers, **15 HP** each, no bounty). With the four clamps
down the crate floats up from the hold (t≈116–120 for a player who shoots at once) and is **collected by flying
through it** (`HELIX CORE SALVAGED` on the HUD's event line). If the player misses it, the CDF salvage tug that
follows the wing picks it up (Okafor's event line), so the core is never lost: **the Mk V "Helix" generator is
unlocked by winning the level** and is in the shop from the L35 visit
([generator](../../../player/generator/README.md#act-5-hand-plan-draft), part A sets the unlock). The crate pays no
credits.

**Rook and the escort.** Rook flies his slot ([wingmen](../../../player/wingmen/README.md)). With `--escort none` the
level is the same without him and his lines.

## Waves

Formation names from the [formation vocabulary](../../../enemies/README.md#formation-vocabulary). Rendered from
the data (step G2): data in **Act 1 terms** with a `bounty_scale` (see *Credit budget*). HP are the units' own pages': the
Warden Tank with the act HP factor of its Act 5 section, the `small` units with the beam era's ×2, the `tiny`
popcorn at 1 HP.

- **Density and pacing** (the Act 2+ rules, `PacingTest` and `ActFloorTest`): **407 enemies** (waves 389 + the
  18 tanks) over 186 s, **≈ 135 a minute** at medium (the draft's 266 and ≈ 86 plus step G2's firing groups): a
  **breather by threat, not by count** (Act 4's L22 and L26 lesson). The draft's waves stand as written, a wave every
  3 s, the Vrell popcorn (Skitters, Mote Swarms, Mites) and the Needlers, three Talon wings and a Talon line; the
  popcorn dies within a second under the plan's Ion Beam L4, so **47 Talon trios on the offbeats** (at 110 px/s, hard
  kept at 3) carry the screen and the shots, as Levels 29, 32 and 33 needed them. Step G2's `levelReport` at medium
  with the plan's fit and Rook: **2.84 on the screen**, 83 shots a minute, one stretch over 2 s (the hold's quiet
  window), the small units 1.57 s on the screen against Level 10's 1.60 (0.98×). Without the trios: 0.92 on the
  screen, 23 stretches over 2 s, 0.52× Level 10.
- **Quiet window**: no air wave from t=113 to t=122, while the freighter's hold passes (the set piece); hard flies a
  Talon line at t=118 in it.
- **Directions**: every wave from the front; the Skitter snakes and the three Stinger columns enter at a side of the
  top edge.
- **The tanks**: seven convoys on ART-G's roads, one path per unit, 1.5 s apart (2 on easy, 3 on hard: 14 / 18 /
  21); the far deck's pair enters at t=123 (the freighter's bow road ends at its tip).
- **Totals** (medium): Skitter 56 · Talon 160 (3 leaders) · Asteroid Mite 48 · Mote Swarm 84 · Needler 32 · Warden
  Tank 18 · Stinger 9.

Wave table (`front (left)`: from the front at that side):

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 5 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | Popcorn among the first hull plates |
| 6.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 8 | 1 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 9.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 11 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 7 | front | |
| 12.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 14 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | The level's shooters are the Needlers, the Talons and the tanks |
| 17 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 18.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 20 | 1 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 21.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 23 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 25 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 27 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 28.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 30 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 32 | 2 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front | In Ascendancy livery, x2 on cdf-frigate-a's walkway |
| 33 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 34.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 36 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 7 | front | |
| 37.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 39 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The first Talons: a scavenger wing |
| 42 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 43.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 45 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 46.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 48 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 49.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 50 | 2 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 3 | front | In Ascendancy livery, x3 on cdf-frigate-b's walkway, past the mining charge (t 51.2) |
| 51 | 2 | column | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | |
| 52.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 54 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 55.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 57 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 58.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 60 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 7 | front | |
| 61.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 63 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 65 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 67 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 68.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 70 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 70 | 3 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front | In Ascendancy livery, x2 along helix-hulk-a's keel |
| 71.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 73 | 3 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 74.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 76 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 77.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 79 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The leader drops a shield cell |
| 82 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 83.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 85 | 3 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 86.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 88 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 7 | front | |
| 88 | 3 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 3 | front | In Ascendancy livery, x3 along helix-hulk-b's keel, past the mining charge (t 89.2) |
| 89.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 91 | 3 | column | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 94 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 95.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 97 | 3 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 98.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 100 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 101.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 103 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | |
| 105 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 107 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 108.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 110 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 111.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 113 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | The last wave before the freighter's hold: none from t=113 to t=122 (hard: the Talon line at t=118) |
| 118 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Hard only: the set piece is not quiet on hard |
| 122 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | After the Helix core (t≈116-120) |
| 123 | 4 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front | In Ascendancy livery, x2 on the freighter's far deck (ART-G: t 123, its bow road ends at its tip) |
| 123.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 125 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 126.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 128 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The leader drops an overdrive |
| 131 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 7 | front | |
| 133 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 135 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 136.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 138 | 5 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 140 | 5 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 3 | front | In Ascendancy livery, x3 through the tangle, past the mining charge (t 141.2) |
| 141 | 5 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 142.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 144 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 145.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 147 | 5 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 148.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 150 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 151.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 153 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 154.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 156 | 5 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 157.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 158 | 5 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 3 | front | In Ascendancy livery, x3, the last convoy |
| 159 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 7 | front | |
| 160.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 162 | 5 | column | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | |
| 165 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 166.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 168 | 5 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 169.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 171 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 173 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 175 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | The last wave; the level ends at t=186 |
| 177 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 180.5 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 183 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step G2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |

Totals: Skitter 56 · Talon 160 · Asteroid Mite 48 · Mote Swarm 84 · Needler 32 · Warden Tank 18 · Stinger 9.
<!-- /data -->

## Ground targets

The tanks are the level's paying ground units (the Warden Tank 28 in parts, 26 whole, hull 97 · turret 30 with the act
HP factor); they drive their roads as convoy waves (the wave table). The containers, the freighter's clamps and the
secrets' triggers pay nothing themselves (their pickups and crates do); the charges pay nothing.

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 1 | A CDF cargo container on a broken corvette's spine (`ground`, 12 HP, t≈18) | Opened, it drops a medium salvage pickup (50); the secondary counts it |
| 2 | CDF cargo containers ×2 on the picket's frigates (t≈38, 58) | Each drops a medium salvage pickup (50); the secondary |
| 3 | A Helix cargo container on a hulk's keel (t≈80) | A medium salvage pickup (50); the secondary |
| 5 | A Helix cargo container in the tangle (t≈150) | A medium salvage pickup (50); the secondary |
| 5 | A CDF cargo container on the last plate (t≈170) | A medium salvage pickup (50); the secondary's last |
| 4 | The freighter's hold clamps ×2 on the Helix crate's left (`ground`, 15 HP, t≈108) | The four clamps' last one down frees the crate: it rises out of the hold, the Helix core, collected by flying through it (no credits; the Mk V comes with the win) |
| 4 | The hold clamps ×2 on the crate's right | As the left pair (one group of four) |
| 2 | A mining charge on a frigate's flank beside the t≈50 convoy's road (t≈51) | One hit: a 64 px burst of 80 to every enemy in it, a passing tank too (never the ship); pays nothing itself |
| 3 | A mining charge beside the t≈88 convoy's road (t≈89) | As above |
| 5 | A mining charge beside the t≈140 convoy's road (t≈141) | As above |
| 2 | The frigate *Vigil*'s bridge, its blast shutter sealed (a `ground` trigger, 20 HP, t≈56, x 90) | A secret: frees a crate worth 100 (655) |
| 3 | The Helix courier *Sable Wren*'s cargo hatch (a `ground` trigger, 20 HP, t≈96, x 404) | A secret: frees a crate worth 100 (655) |
| 5 | The prospector's tender *Old Marta*'s airlock (a `ground` trigger, 20 HP, t≈165, x 240) | A secret: frees a crate worth 100 (655) |
<!-- /data -->

## Hazards

- **Tank fire**: the Warden Tank's 3-shot bursts, aimed in any direction, up the screen after it passes.
- **Mining charges** ([belt](../../../world/belt/README.md#hazards--set-pieces)): three, beside the tank roads; the
  64 px burst hurts enemies and ground units, **never the ship**: a tool, not a threat.
- **Drifting asteroids**: small ones with Mites in sections 1–3 and 5; no big rock in the freighter's section.
- **Specials**: no setting limit (the Airstrike and the Orbital Lance work); the Sonar Pulse reads `NO WATER`.

## Secrets and pickups

The Salvage scanner's level ([ship systems](../../../player/systems/README.md#salvage-scanner-user-decision-d7-of-m5-part-a)):
three hidden crates, each a `ground` trigger the scanner's glint shows at any range, each worth **100** in Act 1
terms (655); about 10 % of the budget each, inside the economy's 5–15 %.

- **The frigate *Vigil*'s bridge** (t≈56, x≈90, the CDF picket): the bridge's blast shutter, sealed; a trigger of
  **20 HP** (28 on hard). Its event line, Okafor: "*The Vigil. She held this lane in the first week. Take her
  stores, Lancer.*"
- **The Helix courier *Sable Wren*** (t≈96, x≈400, wedged between two hulks): her cargo hatch, **20 HP** (28 on
  hard). Its event line, Varga: "*Helix courier. Sealed medical crates. I know what's grown in those.*"
- **The prospector's tender *Old Marta*** (t≈165, x≈240, under a Helix hull in the tangle): her airlock, **20 HP**
  (28 on hard). Its event line, Rook: "*Old Marta. Somebody's grandmother's boat. Sorry, ma'am.*"
- **Containers**: six salvage pickups of 50 (see *Layout*).
- **Shield cell**: dropped by the leader of the t=79 V-wing. **Overdrive**: dropped by the leader of the t=128
  V-wing. **Armour patch**: a CDF beacon on the *Vigil* releases one at (240, 380 y-up) at t≈60.

## Radio chatter

Spoken radio, draft text for the **1 s rule** (12 characters a second plus a 1.5 s lead; 0.4 s of silence; no timed
line more than 1 s late); event lines drop after 6 s. Rook's lines carry `requires: escort`. Speakers: **Okafor,
Varga, Rook**. A breather's radio: fewer lines, more silence.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "The graveyard, Lancer. Old wrecks, Helix and ours. The Ascendancy picks them over. So do we." |
| t=13.5 | Rook | "Quiet out here. I don't trust quiet." |
| t=29 | Varga | "Tanks on the hulls. The grey tanks from Tharsis, in their own colours now." |
| t=40 | Okafor | "They've stopped hiding, Doctor. Every crate you find, Lancer, the wing keeps." |
| t=108 | Varga | "That freighter's hold. Helix seals, a fusion core in the clamps. Shoot the clamps loose!" |
| The core collected (event) | Okafor | "Helix core aboard. It'll be on the rack before Ceres." |
| The core missed (event: it leaves the screen, or the clamps leave unshot or the level ends first) | Okafor | "Salvage tug has the core. Good enough, Lancer." |
| t=130 | Rook | "Finders keepers. Tell Helix to send us a bill." |
| t=150 | Varga | "Helix and ours, side by side, all dead. Look, Commander." |
| t=157 | Okafor | "I'm looking, Doctor. Ceres ahead, Lancer. Rest while you can." |
| Secondary met (at the end) | Okafor | "Containers stripped. The quartermaster sends his thanks." |
<!-- /data -->

Step G2 trimmed Varga's t=150, Okafor's t=159 (now t=157) and the core's line to one radio page each (three
22-character lines, 5 s; the gap check's §4) and moved Rook's t=128 to t=130, so a core collected as late as t≈122.8
still finds its gap. Text time (30 characters a second, 3 s a page, 5 s the last), G2's: t=1 closes ≈ 12.0 (two pages),
t=29 ≈ 39.4, t=40 ≈ 50.5, t=108 ≈ 118.9, t=130 ≈ 136.5, t=150 ≈ 156.9. Step FIX2 (accepted in round 67, C.6) moved Rook's t=12 to
**t=13.5** (voiced, the two-page t=1 held it 1.3 s late) and Okafor's t=159 to **t=157** (with the secondary met at
t≈151, its line, Okafor's and *Old Marta*'s all play), and trimmed *Sable Wren* to one page (voiced 4.3 s, so it fits
before Varga's t=108 with the hatch shot as late as it passes the ship, t≈99.5). The event lines fall in the gaps:
*Vigil* (10.4 s) in reach at t≈53–59 (free 50.9–108), *Sable Wren* t≈96–100 (free), the core (6.8 s) at t≈110–122
(free from 119.3 until 130; missed also when the clamps leave unshot, t≈113.8, or the level ends first), *Old Marta*
(6.8 s) t≈162–168 (from 166.4), the secondary's line when the needed container opens or at the end. The secondary's line says "stripped", not "every container",
since medium needs five of six and easy four.

**Story notes.** Varga's *Sable Wren* line carries L31's thread (the Helix cures grown from Vrell biotech) without
a new finding; the Mk V is "captured Helix tech" (the generator's note), so Okafor's line puts it on the shop's
rack for the L35 visit. Varga's t=150 line and Okafor's answer are the breather's one quiet beat before the
Sovereign; Okafor's "I'm looking" keeps her Act 5 promise (what Varga sees, she says).

## Music & ambience

The [Act 5 music plan](../../../audio/music/README.md#act-5-music-draft-round-60)'s cue for this level, "the A theme
stripped": [track 12](../../../audio/music/README.md#track-list) "Hollow Rock", **its base stem only, 3 dB down**
(`section_db`) throughout, never the full mix; **Vorne's motif stem comes in once**, as the freighter's hold with
the Helix core scrolls in (t≈106), and goes out after it (t≈124). Ambience: the `belt-open` bed
([sfx](../../../audio/sfx/README.md#ambience-per-setting)) throughout, with the wrecks' slow metal groans. New sounds this
level needs: the containers' burst (a cargo latch blowing, a short metal clang), the clamps' release (Level 29's
Dock 9 [clamp release](../../../audio/sfx/README.md), round 67's S.2 "dock"), the core's pickup (a deep power hum swelling); the tanks keep their Level 19 sounds. Freesound
only for real-world sounds (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(34) = 700 × 1.07³³ ≈ **6,528** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act 1
terms** (Skitter 5, Asteroid Mite 3, Mote Swarm 2 a mote, Needler 12, Talon 12, leader 25, Stinger 15, Warden Tank
28 in parts, a salvage pickup 50, a crate 100, the secondary 100), × the act factor **6.5536** (1.6⁴), bounties ×
the scale. The salvage level earns more of its haul from the wrecks than from kills. The draft's **`bounty_scale` 0.45**
(by hand at ≈ 86 a minute: 6,511) is **0.27** with step G2's firing groups: `levelReport`'s typical haul **6,488
(−0.6 %)** at medium (the economy's shares: 60 % of the air kills, 80 % of the ground, 80 % of the salvage pickups,
half the secrets and the secondary); the table's rule gives 6,445 (−1 %). The Helix core pays nothing (a story pickup).

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 56 × 5 + Talon 160 × 12 + Asteroid Mite 48 × 3 + Mote Swarm 84 × 2 + Needler 32 × 12 + Warden Tank 18 × 28 by parts (26 whole) + Stinger 9 × 15 | 6,237 | 3,875 |
| Squad leaders: 3 × 13 extra (perfect run only) | 69 | 0 |
| Ground targets: cargo containers (CDF and Helix) 1 × medium salvage 50 + 2 × medium salvage 50 + 1 × medium salvage 50 + 1 × medium salvage 50 + 1 × medium salvage 50 | 1,968 | 1,260 |
| Secret: Vigil (hidden crate, 2% of budget) | 655 | 328 |
| Secret: Sable Wren (hidden crate, 2% of budget) | 655 | 328 |
| Secret: Old Marta (hidden crate, 2% of budget) | 655 | 328 |
| Secondary: strip the graveyard | 655 | 328 |
| **Total** (bounty scale 0.27) | **10,894** | **6,445** |
| Budget(n) = the typical haul's target; typical -1 %, perfect 1.67 × budget | | 6,528 |
<!-- /data -->

The Salvage scanner's +10 / +20 % on the salvage and the crates (up to ≈ 790 on a perfect run) comes on top,
outside the budget. The Helix core pays no credits: its value is the unlock.

**Balance plan**: the plan's L34 visit buys nothing (saving for the Mk V,
[Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)): forward DPS 135 against the proposed
reference **140** (J2), 0.96, the act's lowest ratio, on its easiest level. The level recommends `anti-ground` for
the tanks and `spread` for the popcorn; the plan's fit flies it without `anti-ground` (the tanks are not hardened).

## Difficulty notes

- **Easy**: the formation lever thins the popcorn; tank convoys of 2; the tanks' and Needlers' easy hooks (their
  pages); the clamps 10 HP; the secondary 4 of 6.
- **Hard**: tank convoys of 3 everywhere (21 tanks); the Needlers' hard hooks; one more Talon line abreast at t≈118
  (the set piece is not quiet on hard); the secrets' triggers 28 HP; the secondary all 6.

## Concept art

None yet. The level needs (part G's production art; the mood from the belt's round-60 mood stills):

- the graveyard backdrop (six sections: the edge, the CDF picket, the Helix hulks, the freighter with its open hold,
  the tangle, clear space with Ceres and the Sovereign's lights far ahead), the hull plates the tanks drive on;
- the Warden Tank in Ascendancy livery (its page: `tools/art/warden_tank.py` with the livery changed) and its wreck;
- the cargo containers, the freighter's clamps and the Helix core crate, the three secrets' props, the mining
  charge on a wreck;
- the two briefing images; the intel portrait of the tank in livery;
- the round's captures: the level at medium with the plan's fit and Rook; the core's salvage; a tank convoy taken
  by a mining charge; readability crops of the black tank on a black Helix hull at 1:1 (Q11).

Production art, M9 part G (concept round 67, step ART-G): the graveyard backdrop (`tools/art/backdrop_l34.py`, its
places for step G2 in [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml): the hull pieces, the convoys'
road paths, the containers, charges, clamps, the crate's place, the three secrets, the beacon's drop and a light debris
block), QG-1's 1:1 readability crops, and the salvage objects (`tools/art/belt_units_g.py`: the containers, the hold's
clamps, the Helix crate, the secrets' props; the CDF beacon is Level 33's nav buoy, the charges Level 32's). The Warden
Tank in livery is in the [ground units' concept art](../../../enemies/ground/README.md#concept-art) (its
[page](../../../enemies/ground/warden-tank/README.md#concept-art)); the two briefing images in the
[briefing](../../../ui/briefing/README.md#concept-art), the intel portrait in the
[hangar](../../../ui/hangar/README.md#concept-art). Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r67-a.png](concept/backdrop-final-r67-a.png) | Production backdrop (`tools/art/backdrop_l34.py`): eleven screens at half size with stand-ins (the corvette's spine and the first container, the CDF picket's convoy and charge, the *Vigil*'s bridge and beacon, a convoy on a Helix keel, the *Sable Wren*, the freighter's hold with the crate and clamps, the far deck's pair, the tangle's convoy and *Old Marta*, clear space with Ceres and the Sovereign), the pieces (sheet) | chosen (round 67, G.4 ok: accepted as final) |
| [concept/backdrop-final-r67-a.gif](concept/backdrop-final-r67-a.gif) | t 104-114 at half size: into the freighter, its engines and hold | chosen (round 67, G.4 ok: accepted as final) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block, its reading of the sections, the convoys' paths along their roads (one per unit), the ground objects' places, the beacon's drop and a light `debris:` for step G2 (not data) | chosen (round 67, G.4 ok: accepted as final) |
| [concept/tank-readability-final-r67-a.png](concept/tank-readability-final-r67-a.png) | QG-1: the Warden Tank in livery at 1:1 on the Helix keel road, trim, bay ribs, the freighter's deck, the CDF walkway and flank; the Act 3 grey tank below for comparison (sheet) | proposed |
| [concept/tank-readability-r67-b.png](concept/tank-readability-r67-b.png) | QG-1 **b** (concept mockup, re-shown in round 67 on 2026-10-29; `tools/concept/tank_readability_r67.py`): the tank roads on the CDF hulls only, the Helix hulls in round 60's black without roads; the same six 1:1 crops as a, then three screens at half size (sheet) | proposed |
| [concept/tank-readability-r67-c.png](concept/tank-readability-r67-c.png) | QG-1 **c** (concept mockup, same generator): round 60's black Helix hulls with their roads and a faint ground shadow under every tank; the same crops and screens (sheet) | proposed |
| [concept/tank-readability-r67-abc.png](concept/tank-readability-r67-abc.png) | QG-1 a, b and c side by side: the same six crops at 1:1, the same tank headings, one row per variant (sheet) | proposed |
| [concept/salvage-objects-final-r67-a.png](concept/salvage-objects-final-r67-a.png) | The salvage objects (`tools/art/belt_units_g.py`): CDF and Helix containers (intact, damaged, wrecked, the burst), the hold clamps (locked, hit, sprung, the hydraulics' burst), the Helix crate (in the hold, the rise, the drift, the seal's glow), the *Vigil*'s shutter, the *Sable Wren*'s hatch, *Old Marta*'s airlock (sheet) | chosen (round 67, G.5 ok: accepted as final) |
| [concept/salvage-objects-final-r67-a.gif](concept/salvage-objects-final-r67-a.gif) | The hold: the four clamps shot one by one, the crate rising on its glow and drifting, the ship flying through it; a CDF container shot open | chosen (round 67, G.5 ok: accepted as final) |
| [concept/salvage-tug-r67-b.png](concept/salvage-tug-r67-b.png) | QG-2 **b** (concept mockup, re-shown in round 67 on 2026-10-29; `tools/concept/salvage_tug_r67.py`): the CDF salvage tug, a concept sprite (khaki, white band, blue CDF mark, grapple arms open and shut on the crate) at 1x and 2x, and b's key frames in the level (sheet) | proposed |
| [concept/salvage-tug-r67-b.gif](concept/salvage-tug-r67-b.gif) | QG-2 a and b side by side at 1x, scrolling at the freighter section's 110 px/s: the clamps shot, the rise, the core missed; a: it drifts off and Okafor's tug line plays; b: the tug flies in, grips it and carries it off | proposed |
| [concept/salvage-objects-r67-ab.png](concept/salvage-objects-r67-ab.png) | QG-2 a and b: the same five moments of both at half size (sheet) | proposed |
| [concept/containers-capture-r67-a.png](concept/containers-capture-r67-a.png) | Game capture (step G5; [recipe](concept/prompts.md#level-34-capture-r67-a)): a CDF container shut, the corvette's container shot open with its salvage, the HUD's `CONTAINERS n / 6` tag | chosen (round 67, accepted with the level, G.5) |
| [concept/helix-core-capture-r67-a.png](concept/helix-core-capture-r67-a.png) | Game capture: the hold's clamps shot, the Helix core rising on its glow and flown through, `HELIX CORE SALVAGED` | proposed |
| [concept/secrets-capture-r67-a.png](concept/secrets-capture-r67-a.png) | Game capture: the *Vigil*'s shutter, the *Sable Wren*'s hatch and *Old Marta*'s airlock shut and opened with their crates; the CDF beacon releasing the armour patch | chosen (round 67, accepted with the level, G.4–G.5) |
| [concept/tank-readability-capture-r67-a.png](concept/tank-readability-capture-r67-a.png) | Game capture, QG-1: 1:1 crops of the tanks in livery on the Helix keel roads and the freighter's deck | proposed |
| [concept/level-34-capture-r67-a.mp4](concept/level-34-capture-r67-a.mp4) | Game capture with the game's sound: the groans under `belt-open`, a container burst, a charge taking a tank, the hold with the motif's window, the clamps and the core's hum | proposed |

## Implementation

- [x] Level data `data.yaml` (when built in part G): sections and speeds with eases, waves, ground targets
      (tank convoys and their roads), containers, secrets, radio, budget, threat profile, briefing, backdrop, music
      (step G2, on ART-G's proposal)
- [x] Warden Tank in Ascendancy livery with the act HP factor (its page's Act 5 section) (step G4: the hull, turret,
      stub and wreck in `warden-tank-asc` from Act 5, the grey tank's flash; its intel portrait in the livery)
- [x] Containers (`ground` destructibles releasing salvage pickups) and the secondary `escapes: cargo-container`
      (5 of 6; easy 4, hard 6) (step G4: the CDF and Helix looks with their bursts, the HUD's `CONTAINERS n / 6`)
- [x] The freighter's hold: four clamps, the core floating free and collected by flying through it, the tug's
      fallback; the Mk V "Helix" unlocked by the win and in the shop from the L35 visit (part A's unlock) (step G1: the
      story pickup; step G4: the shut crate under the clamps, its rise and drift with the glow, `HELIX CORE SALVAGED`,
      the core's hum; step FIX2: a core never freed counts as missed; seen in round 67's captures, step G5)
- [x] Mining charges beside the tank roads (the belt's hazard, never the ship) (Level 32's look and blast, round 65's b)
- [x] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), the breather's density above 56, the quiet
      window at the hold (step G2: `levelReport` 2.84 on the screen, one stretch over 2 s, 0.98× Level 10; Level 34
      in `PacingTest`'s plan fits)
- [x] Radio voiced, every timed line at most 1 s late, with Rook and without (`requires: escort`) (step G2: voiced,
      the text time modelled; step G4: `RadioTimelineLevel34Test`, the core's line collected or missed)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (step G2; `IntelPanelLayoutTest`
      over every level; step G4: `OBJECTIVE: HELIX CORE`, the crates' sections in gold at L3, the liveried portrait
      first; round 67's capture)
- [x] Briefing: both pages, each one screen, with their images (step G2, `BriefingLayoutTest`; the images ART-G's)
- [x] Typical haul within ±5 % of budget(34) = 6,528 with the level's `bounty_scale` (step G2: 0.27, `levelReport`
      6,488, −0.6 %; `TypicalHaulTest`'s row in step G3)
- [x] Music: track 12's base stem at −3 dB, the motif stem once at the hold; the `belt-open` ambience (step G4:
      `Level34SoundsTest`; the `wreck-groans` layer, round 67's a sound until the redo; the hold clamps play Level 29's Dock 9 clamp release, `Sfx.CLAMP_RELEASE`, since step FIX3: S.2 dock)
- [x] Easy/hard variations; the autopilot flies the level on every difficulty (with and without Rook) (step G2's data;
      step G3: `Level34Test`, the salvage play, wins and `CONTAINERS` 3/3 on every difficulty)
- [x] Production art and captures for the round (production art proposed in round 67, step ART-G; captures step G5)

## Open questions

Approved in round 60 (the gap analysis's questions this level follows):

- **Q12, Act 5's arsenal** (recommended **c**, drafted: the Mk V "Helix" salvaged here, in the shop from the L35
  visit). Alternatives: **a** lean, the core tiers only, the Mk V in the shop with the act's first visit as its data's
  `act 5` reads (the freighter's crate then pays credits instead, ≈ a fourth secret); **b** the full roster, the same
  for the Mk V. Under a and b the Q12 set piece and Okafor's core lines go.
- **J1, the Warden Tank for the Crawler Tank** (drafted). Alternative: the roster's Crawler Tank (front and rear
  turrets), a new unit with its own art and sounds.
- **Q11, the Ascendancy look** (decided in round 60, **b**, not the recommendation: gunmetal/charcoal with gold). The tank
  on the black Helix hulls is still the act's hardest readability case (gunmetal on a dark hull): the hulls are kept a step
  lighter (sunlit gold and grey faces) or the tanks run on the CDF hulls only; part G measures it.
- **The core's fallback** (this step's): the tug picks a missed core up, so the unlock can never be lost. The
  alternative is a missable unlock (the core on sale only if collected), against the linear campaign.

## Decisions

- 2026-10-27 (Act 5 design phase, step `m9-d-levels-3`; approved in round 60): drafted from the
  roster row, the gap analysis (§0, §1.1, §2.4, Q11 a, Q12 c, J1, J2) and the wave 1 documents (the Warden Tank's
  Act 5 section, the belt, the generator's Act 5 hand plan and Mk V note); Level 25 as the model.
- 2026-10-27: a breather by threat, not by count: ≈ 86 a minute (266 in 186 s, a wave every 3 s), the Vrell popcorn
  carrying the count, every wave from the front; no unit new to the act.
- 2026-10-27: the caches are six containers with salvage pickups (the secondary counts them) and three hidden crates
  (the scanner's level), ≈ 39 % of the typical haul from the wrecks; the bounty scale 0.45 carries the rest.
- 2026-10-27: the Mk V "Helix" is a story pickup in a freighter's hold whose unlock comes with the win (the tug's
  fallback), not a missable secret.
- 2026-10-27: the music follows the [Act 5 music plan](../../../audio/music/README.md#act-5-music-draft-round-60)
  (step m9-d-audio): track 12's base stem at −3 dB, the motif once at the core, `belt-open`.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`, D.7): J.1 kept (the Warden Tank in livery instead of a Crawler Tank; its chosen variant is A.15 b, heavier gold, in the Q11 b gunmetal/charcoal livery), Q12 **c** (the Mk V Helix salvaged). Q11 **b**: part G measures the readability of the gunmetal tanks on the dark Helix hulls.
- 2026-10-29 (step ART-G, pending round 67): production art proposed: the graveyard backdrop with QG-1 **a** (the Helix
  hulls a step lighter, grey-gold with gold trim; the roads plain steel), the salvage objects with QG-2 **a** (the crate
  rises on its seal's glow; no tug sprite), the tank in livery (A.15 b in Q.11 b), the briefing images and the intel
  portrait; the far deck's tank pair proposed at t 123 (the freighter's bow road ends at its tip: at 125 the second tank
  would drive off it).
- 2026-10-29 (M9 part G, step G2; decided in round 67, 2026-10-29): the level built as data on the approved
  tables and ART-G's proposal (its backdrop, the convoys' road paths one per unit at 1.5 s, the objects' places, the
  beacon's drop, the light debris), with these measured changes: **firing groups** (47 Talon trios on the offbeats at
  110 px/s, hard kept at 3, and the last three over clear space at t=177–183: 160 Talons, 407 enemies, ≈ 135 a minute;
  without them 0.92 on the screen, 23 stretches over 2 s and the small units 0.52× Level 10's; trios at 130 px/s
  reached 2.72 and 0.86×, pairs 1.99); **`bounty_scale` 0.27** (the draft's 0.45 with the trios' bounties; typical
  6,488, −0.6 %); the Skitter swarms fly as **snakes of 4** from alternating edges (a swarm needs a route, as Levels
  29 and 33), the Mites' rocks drift at the scroll plus 15 px/s, the Mote Swarms weave on Level 33's two paths at
  180 px/s; the far deck's tank pair at **t=123** (ART-G); the containers 12 HP with a medium salvage each, the CDF and
  Helix looks by `sprite` (`cargo-container-cdf`/`-helix`, both counted by the secondary); the four clamps one group
  (`helix-clamps`, 15 HP, easy 10) whose last one frees the story pickup `helix-core` at their centre (108.3, 240); the
  charges E's 80 in 64 px; the secrets' triggers 20 HP (hard 28) with their own looks; **radio**: Varga t=150, Okafor
  t=159 and the core's line one page each (the gap check's §4), Rook's t=128 moved to **t=130** (a core collected as late
  as t≈122.8 still gets its line), the secondary's line "Containers stripped" (true on every difficulty: medium needs
  five of six); the music's motif window by the new `motif.to` (t=106–124), the base stem only at −3 dB, the
  `wreck-groans` layer t=0–174 over `belt-open` (AUD-G). The autopilot wins 3/3 on every difficulty (hard 0 retries
  with the trios kept at 3; 2 retries at the formation lever's 4); the secondary met 3/3 at easy and medium, 1/3 on
  hard (all six: step G3's container play).
- 2026-10-29 (step G4, decided in round 67, 2026-10-29): the game's side. The tanks wear `warden-tank-asc`
  from Act 5 (hull, turret, stub, wreck; the flash stays the grey tank's), the atlases give the livery to Act 5's
  levels only and the CDF/Helix container variants only to Level 34; the shut crate lies at the clamps' centre while
  they stand, rises 1 s with its glow, drifts as a pickup in its glow's loop; `HELIX CORE SALVAGED` in the event
  prompt's box below the ship for 3 s; the HUD tag counts the containers opened of all six (`CONTAINERS n / 6`); the
  core's line, collected or missed, waits out Varga's t=108 call as a phase line (as an event line it went stale
  behind it whenever the core came before t≈113); the intel tags `OBJECTIVE: HELIX CORE` under the names from L2 and
  underlines the three crates' sections in gold on the strip at L3. Open: *Sable Wren*'s line fits before Varga's
  t=108 only when the hatch is shot by about t=97, and *Old Marta*'s (Rook's) goes stale behind the secondary's line
  when that was met at t≈151 (medium's usual): both want a retime; a core never freed (the clamps left standing) has
  no `story-missed` line.
- 2026-10-29 (step FIX2, decided in round 67, 2026-10-29): G4's four radio findings. A core never released is
  missed too: when the clamps' last one leaves the screen unshot (t≈113.8) or the level ends first, `story-missed`
  fires once and the tug's line plays, as for a released core that leaves (sim, opt-in: only a level with a story
  pickup). *Sable Wren*'s line trimmed to one page ("A Helix courier. …" → "Helix courier. …", same sense) and
  re-voiced, so it plays before Varga's t=108 when the hatch is shot as it passes the ship (t≈99.5). Okafor's t=159 →
  **t=157**, so with the secondary met at t≈151 its line, Okafor's and *Old Marta*'s (Rook's, t≈166.4) all play. Rook's
  t=12 → **t=13.5**, so it starts on time voiced behind the two-page t=1 (1.3 s late before). The radio runs G4 left
  out are back (`RadioTimelineLevel34Test`, ±Rook, text and voiced).
- 2026-10-29 (step G5, pending the user's review in round 67): the level recorded in the game (medium, the plan's fit,
  Rook): the convoys in livery on the CDF walkways and the Helix keel roads, a charge taking a hit tank (the picket
  charge at t≈53.7 after the beam stripped the turret; the keel charge at t≈91 blown by the wing pods), the containers
  and the tag (`DONE` at the fifth), the hold (the clamps down by t≈110.5, the core collected at t≈113 by a ship that
  waits under the crate), the three secrets and the beacon, the intel and the release's end. Seen while recording: the
  wrecked containers read as black holes in the deck (G.5's weak spot), the containers' salvage and the crates drift
  out of reach of a ship low on the screen (fly up for them), and with step H2's Level 35 data in the build the game
  goes on to Level 35's briefing (the release's end captured from a build without it).
- 2026-10-29 (user, [round 67](../../../concept-rounds/round-67/README.md), partly answered; the round stays open): **C.1 a** (the density as built: 407 enemies at medium, `bounty_scale` 0.27), **C.4 a** (a mining charge finishes a hit tank and strips a fresh one's turret), **C.2, C.3, C.5, C.6 accepted**; art **G.4** (the backdrop), **G.5** (the salvage objects) ok: final; sounds **S.1 a** (`container-burst`), **S.3 a** (`helix-core`) and **S.2 "dock"**: the hold clamps reuse Level 29's Dock 9 clamp release (`clamp-release-r62-b`), no new sound (FIX3 switches the key). **Open, re-shown after a redo:** G.1 and G.2 (QG-1, QG-2: b/c need renderings), G.3 (new clips) and S.4 `wreck-groans` (both variants too quiet); `art:` stays `proposed` until they are answered.
