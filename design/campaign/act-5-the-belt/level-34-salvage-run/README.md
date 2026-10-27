---
title: Level 34 – Salvage Run
design: draft
implementation: not-started
art: none
depends-on: [../../../enemies/ground/warden-tank, ../../../enemies/air/talon, ../../../enemies/air/needler, ../../../enemies/space/asteroid-mite, ../../../player/generator, ../../../player/systems, ../../../world/belt, ../../../player/wingmen, ../../../systems/economy]
updated: 2026-10-27
---

# Level 34 – Salvage Run

## Summary

The breather before the Iron Sovereign. On the lane to Ceres lies a ship graveyard: derelict CDF and Helix
hulls drifted together after old battles, picked over by the Ascendancy. The
[Warden Tank](../../../enemies/ground/warden-tank/README.md) of Tharsis is back on the hulls in Ascendancy
black and gold (judgement call J1), a few Talon wings and the Vrell popcorn fill the sky, and every wreck hides
something: six cargo containers, three hidden crates (the Salvage scanner's level) and, in a Helix freighter's
hold, the **Mk V "Helix" generator** the wing salvages (Q12 c; in the shop from the L35 visit). Density 2 by
threat (≈ 86 a minute by count), no boss, 186 s: the graveyard's edge, the CDF picket, the Helix hulks, the
freighter, the tangle and clear space.

**Status: draft** (Act 5 design phase, step `m9-d-levels-3`, 2026-10-27), **pending the user's review in
concept round 60**. Written from the act roster, the gap analysis's recommended answers (**Q12 c** the Mk V
salvaged here, **Q11 a** the belt look, **Q1 a** the Talons' shields; judgement calls **J1** the Warden Tank for
the Crawler Tank and **J2** the reference DPS 140, all pending in round 60) and the wave 1 documents. Every number
is a hand-written first draft at medium for part G to measure with `levelReport`; there is no `data.yaml` yet.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

> **Commander Okafor:** "Before the Sovereign, we restock. A ship graveyard lies on the lane to Ceres, our
> hulls and Helix's. The Ascendancy has tanks on the wrecks. Take what you can carry, Lancer."
>
> **Dr. Varga:** "Helix freighters went down out there with their holds sealed. If one carried Helix
> tech, I want it. Their generators run hotter than ours."

*Images* (production art in part G; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-34-graveyard`: a chart of the graveyard: the lane through it, the CDF picket's broken
  frigates in grey, the Helix hulks in black and gold, the big freighter marked with a gold box, the tanks' roads
  along the hulls as dotted lines.
- Varga's page `level-34-helix-hold`: a salvage scan of the freighter's hold: a crate in four clamps, the Helix
  emblem on its seal, the power signature of a fusion core beside the Stormhawk's Mk IV for scale.

*Hangar teaser* (shop screen before L34; Rook, not voiced):

> **Rook:** "A salvage run. Finally, a mission where we get to keep things."

## Threat profile

Draft; directions are counted by each wave's `from` (the intel's rule): every wave from the front (the Stingers'
columns at a side of the front).

| Field | Value |
|---|---|
| Dominant layers | `ground` (the derelict hulls: Warden Tank convoys on their roads, the containers, the hidden crates, the freighter's hold), `air` (written `space`: Needler lines, three Talon wings and a Talon line, the Vrell popcorn and the Asteroid Mites on their rocks) |
| Attack directions | front 100% |
| Density | 2 |
| Recommended traits | `anti-ground`, `spread` |
| Hazards | The tanks' bursts, also back up the screen; drifting asteroids; mining charges on the wrecks (they never hurt the ship) |
| Boss / mid-boss | none |
| Sensor-suite detail | none: the belt, the ship graveyard, `ground` + `air`, front · L1: + direction, density 2, hazards "tank fire" and "mining charges", the objective `CROSS THE GRAVEYARD` and the secondary `CONTAINERS 5/6` · L2: + Warden Tank (Ascendancy livery), Needler, Talon, Stinger, Asteroid Mite, Skitter, Mote Swarm portraits; the Helix crate marked as an objective pickup · L3: + the three hidden crates' sections on the timeline strip, `anti-ground` highlighted |

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
- **The Helix core** (Q12 c, pending round 60) is a story pickup, not an objective: see *The freighter*.

## Layout

Scroll **130 px/s** throughout (a slow drift for looking; the breather's pace), **110 px/s** past the freighter's
hold (t=105–135; the data eases each change over 2 s). Setting: [the belt](../../../world/belt/README.md)'s ship
graveyard in vacuum ([vacuum rules](../../../world/belt/README.md#vacuum-rules)). **Light**: the belt's top-left sun
raking across the hulls (long hard shadows), the CDF wrecks' faded white-grey, the Helix hulks' black and gold
catching the sun, a few emergency lights still blinking red; the dust band in the deep layer. Motion budget: the
tanks and the popcorn are the strong elements, the hulls' slow turn the second.

| Section | t (s) | Scroll (px) | Speed (px/s) | Layers and content | Purpose |
|---|---|---|---|---|---|
| 1. Graveyard Edge | 0–25 | 0–3,250 | 130 | `ground`: scattered hull plates and a broken CDF corvette's spine. `air`: small drifting rocks with Mites. `deep`: the dust band, far wrecks. | Popcorn and Needlers; the first container (t≈18). |
| 2. The CDF Picket | 25–65 | 3,250–8,450 | 130 | `ground`: three broken CDF frigates side by side, their dorsal walkways the tanks' roads (convoys t≈32, t≈50), a mining charge on a frigate's flank (t≈50), two containers (t≈38, t≈58); the frigate *Vigil*'s bridge (t≈56, the first secret). | The tanks in livery taught; the first Talon wing (t=39). |
| 3. Helix Hulks | 65–105 | 8,450–13,650 | 130 | `ground`: gutted black-and-gold Helix ore freighters, tank convoys along their keels (t≈70, t≈88), a mining charge (t≈88), a container (t≈80); the Helix courier *Sable Wren* wedged between two hulls (t≈96, the second secret). | Tanks and Needlers; the sky quiet between waves. |
| 4. The Freighter | 105–135 | 13,650–16,950 | 110 | `ground`: one huge Helix freighter, its hold open to space; **the Helix core** in four clamps in the hold (t≈114–120); a tank pair on its far deck (t≈125). No air wave from t=113 to t=122. | The salvage set piece (Q12 c). |
| 5. The Tangle | 135–172 | 16,950–21,760 | 130 | `ground`: wrecks of both fleets tangled together, the tanks' last convoys (t≈140, t≈158), a mining charge (t≈140), a container (t≈150); the prospector's tender *Old Marta* under a Helix hull (t≈165, the third secret). | The level's busiest stretch (still a breather). |
| 6. Clear Space | 172–186 | 21,760–23,580 | 130 | `ground`: the last plates falling behind. `deep`: Ceres ahead, a hard white point, and beside it a ring of gold lights: the Sovereign. | Release; the level ends at t=186. |

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

**The freighter** (Q12 c, pending round 60): the hold scrolls in at t≈108; the crate (≈ 60 × 40 px, the Helix
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

Formation names from the [formation vocabulary](../../../enemies/README.md#formation-vocabulary). All numbers are
**draft**: data in **Act 1 terms** with a `bounty_scale` (see *Credit budget*). HP are the units' own pages': the
Warden Tank with the act HP factor of its Act 5 section, the `small` units with the beam era's ×2, the `tiny`
popcorn at 1 HP.

- **Density and pacing** (the Act 2+ rules, `PacingTest` and `ActFloorTest`): **266 enemies** (waves 248 + the
  18 tanks) over 186 s, **≈ 86 a minute** at medium: a **breather by threat, not by count** (Act 4's L22 and L26
  lesson), above the act's 70 (a breather may dip to 56). A wave every 3 s, lighter groups: the Vrell popcorn
  (Skitters, Mote Swarms, Mites) carries the count; the shots come from the Needlers (eight groups), three Talon
  wings and a Talon line, and the tanks. No unit new to the act.
- **Quiet window**: no air wave from t=113 to t=122, while the freighter's hold passes (the set piece).
- **Directions**: every wave from the front (the roster's "front"); the three Stinger columns enter at a side of
  the top edge.
- **Totals**: Skitter 56 · Asteroid Mite 48 · Mote Swarm 84 · Needler 32 · Talon 19 (3 leaders) · Stinger 9.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 5 | 1 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | Popcorn among the first hull plates |
| 8 | 1 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 11 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 7 | front | |
| 14 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | The level's shooters are the Needlers, the Talons and the tanks |
| 17 | 1 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 20 | 1 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 23 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 27 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 30 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 33 | 2 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front (right) | |
| 36 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 7 | front | |
| 39 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The first Talons: a scavenger wing |
| 42 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 45 | 2 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 48 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 51 | 2 | column | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | |
| 54 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 57 | 2 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 60 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 7 | front | |
| 63 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 67 | 3 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 70 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 73 | 3 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 76 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 79 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 82 | 3 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 85 | 3 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 88 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 7 | front | |
| 91 | 3 | column | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 94 | 3 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 97 | 3 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 100 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 103 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | |
| 107 | 4 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 110 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 113 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | The last wave before the freighter's hold: none from t=113 to t=122 |
| 122 | 4 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | After the Helix core (t≈116–120) |
| 125 | 4 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 128 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 131 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 7 | front | |
| 135 | 5 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 138 | 5 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 141 | 5 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 144 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 147 | 5 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 150 | 5 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 153 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 156 | 5 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 159 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 7 | front | |
| 162 | 5 | column | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | |
| 165 | 5 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | |
| 168 | 5 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 171 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 175 | 6 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front | The last wave; the level ends at t=186 |

## Ground targets

The tanks are the level's paying ground units; the containers, the freighter's clamps and the secrets' triggers
pay nothing themselves (their pickups and crates do).

| Section | Target | Effect |
|---|---|---|
| 2 | [Warden Tank](../../../enemies/ground/warden-tank/README.md) convoy ×2 on a frigate's walkway (t≈32) | Bounty 28 each (in parts; 26 whole); bursts back up the screen |
| 2 | Warden Tank convoy ×3 past a mining charge (t≈50) | Bounty 28 each |
| 3 | Warden Tank convoy ×2 along a Helix keel (t≈70) | Bounty 28 each |
| 3 | Warden Tank convoy ×3 past a mining charge (t≈88) | Bounty 28 each |
| 4 | Warden Tank pair on the freighter's far deck (t≈125) | Bounty 28 each |
| 5 | Warden Tank convoy ×3 past a mining charge (t≈140) | Bounty 28 each |
| 5 | Warden Tank convoy ×3 (t≈158) | Bounty 28 each |
| 1–6 | Cargo containers ×6 (t≈18, 38, 58, 80, 150, 170; 12 HP) | A salvage pickup of 50 each (328); the secondary |
| 4 | The freighter's four clamps (15 HP each, t≈108–120) | Free the Helix core (the Mk V unlock comes with the win) |
| 2, 3, 5 | The three hidden crates' triggers (see *Secrets*) | A crate worth 100 each (655) |

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
  hard). Its event line, Varga: "*A Helix courier. Sealed medical crates. I know what's grown in those.*"
- **The prospector's tender *Old Marta*** (t≈165, x≈240, under a Helix hull in the tangle): her airlock, **20 HP**
  (28 on hard). Its event line, Rook: "*Old Marta. Somebody's grandmother's boat. Sorry, ma'am.*"
- **Containers**: six salvage pickups of 50 (see *Layout*).
- **Shield cell**: dropped by the leader of the t=79 V-wing. **Overdrive**: dropped by the leader of the t=128
  V-wing. **Armour patch**: a CDF beacon on the *Vigil* releases one at (240, 380 y-up) at t≈60.

## Radio chatter

Spoken radio, draft text for the **1 s rule** (12 characters a second plus a 1.5 s lead; 0.4 s of silence; no timed
line more than 1 s late); event lines drop after 6 s. Rook's lines carry `requires: escort`. Speakers: **Okafor,
Varga, Rook**. A breather's radio: fewer lines, more silence.

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "The graveyard, Lancer. Old wrecks, Helix and ours. The Ascendancy picks them over. So do we." |
| t=12 | Rook | "Quiet out here. I don't trust quiet." |
| t=29 | Varga | "Tanks on the hulls. The grey tanks from Tharsis, in their own colours now." |
| t=40 | Okafor | "They've stopped hiding, Doctor. Every crate you find, Lancer, the wing keeps." |
| *Vigil* opened (event) | Okafor | "The Vigil. She held this lane in the first week. Take her stores, Lancer." |
| *Sable Wren* opened (event) | Varga | "A Helix courier. Sealed medical crates. I know what's grown in those." |
| t=108 | Varga | "That freighter's hold. Helix seals, a fusion core in the clamps. Shoot the clamps loose!" |
| The core collected (event) | Okafor | "Helix core aboard. The quartermaster will have it on the rack before Ceres." |
| The core missed (event: it leaves the screen) | Okafor | "Salvage tug has the core. Good enough, Lancer." |
| t=128 | Rook | "Finders keepers. Tell Helix to send us a bill." |
| t=150 | Varga | "Helix hulls and ours, side by side, all of them dead. Look at it, Commander." |
| t=159 | Okafor | "I'm looking, Doctor. Ceres is ahead, Lancer. Rest while you can." |
| *Old Marta* opened (event) | Rook | "Old Marta. Somebody's grandmother's boat. Sorry, ma'am." |
| Secondary met (at the end) | Okafor | "Every container opened. The quartermaster sends his thanks." |

Timeline check (text): t=1 closes ≈ 10.2, t=12 ≈ 16.5, t=29 ≈ 36.7, t=40 ≈ 47.9, t=108 ≈ 116.8, t=128 ≈ 133.3,
t=150 ≈ 157.8, t=159 ≈ 165.8. The secrets' and the core's event lines fall in the gaps: *Vigil* in reach at
t≈53–59 (free 47.9–108), *Sable Wren* t≈93–99 (free), the core at t≈116–122 (free from 117.2), *Old Marta* t≈162–168
(waits at most until 166.2).

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
level needs: the containers' burst (a cargo latch blowing, a short metal clang), the clamps' release (a heavy
hydraulic unlock), the core's pickup (a deep power hum swelling); the tanks keep their Level 19 sounds. Freesound
only for real-world sounds (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(34) = 700 × 1.07³³ ≈ **6,528** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act 1
terms** (Skitter 5, Asteroid Mite 3, Mote Swarm 2 a mote, Needler 12, Talon 12, leader 25, Stinger 15, Warden Tank
28 in parts, a salvage pickup 50, a crate 100, the secondary 100), × the act factor **6.5536** (1.6⁴), bounties ×
the scale. The salvage level earns more of its haul from the wrecks than from kills: a **`bounty_scale` of 0.45**
puts the typical haul at **6,511 (−0.3 %)** (the economy's shares: 60 % of the air kills, 80 % of the ground, 80 %
of the salvage pickups, half the secrets and the secondary); `TypicalHaulTest` confirms it in the build.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 56 × 5 + Asteroid Mite 48 × 3 + Mote Swarm 84 × 2 + Needler 32 × 12 + Talon 16 × 12 + leader 3 × 25 + Stinger 9 × 15 | 4,064 | 2,438 |
| Ground targets: Warden Tank 18 × 28 | 1,486 | 1,189 |
| Salvage: six containers' pickups 6 × 50 | 1,966 | 1,573 |
| Secrets: *Vigil*, *Sable Wren*, *Old Marta* (hidden crates) 3 × 100 | 1,966 | 983 |
| Secondary: strip the graveyard | 655 | 328 |
| **Total** (bounty scale 0.45) | **10,138** | **6,511** |
| Budget(n) = the typical haul's target; typical −0.3 %, perfect 1.55 × budget | | 6,528 |

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

## Implementation

- [ ] Level data `data.yaml` (when built in part G): sections and speeds with eases, waves, ground targets
      (tank convoys and their roads), containers, secrets, radio, budget, threat profile, briefing, backdrop, music
- [ ] Warden Tank in Ascendancy livery with the act HP factor (its page's Act 5 section)
- [ ] Containers (`ground` destructibles releasing salvage pickups) and the secondary `escapes: cargo-container`
      (5 of 6; easy 4, hard 6)
- [ ] The freighter's hold: four clamps, the core floating free and collected by flying through it, the tug's
      fallback; the Mk V "Helix" unlocked by the win and in the shop from the L35 visit (part A's unlock)
- [ ] Mining charges beside the tank roads (the belt's hazard, never the ship)
- [ ] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), the breather's density above 56, the quiet
      window at the hold
- [ ] Radio voiced, every timed line at most 1 s late, with Rook and without (`requires: escort`)
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel
- [ ] Briefing: both pages, each one screen, with their images
- [ ] Typical haul within ±5 % of budget(34) = 6,528 with the level's `bounty_scale`
- [ ] Music: track 12's base stem at −3 dB, the motif stem once at the hold; the `belt-open` ambience
- [ ] Easy/hard variations; the autopilot flies the level on every difficulty (with and without Rook)
- [ ] Production art and captures for the round

## Open questions

Pending the user's review in round 60 (the gap analysis's questions this level follows):

- **Q12, Act 5's arsenal** (recommended **c**, drafted: the Mk V "Helix" salvaged here, in the shop from the L35
  visit). Alternatives: **a** lean, the core tiers only, the Mk V in the shop with the act's first visit as its data's
  `act 5` reads (the freighter's crate then pays credits instead, ≈ a fourth secret); **b** the full roster, the same
  for the Mk V. Under a and b the Q12 set piece and Okafor's core lines go.
- **J1, the Warden Tank for the Crawler Tank** (drafted). Alternative: the roster's Crawler Tank (front and rear
  turrets), a new unit with its own art and sounds.
- **Q11, the Ascendancy look** (recommended **a**). The tank in black and gold on the black Helix hulls is the act's
  hardest readability case: the hulls are kept a step lighter (sunlit gold and grey faces) or the tanks run on the
  CDF hulls only.
- **The core's fallback** (this step's): the tug picks a missed core up, so the unlock can never be lost. The
  alternative is a missable unlock (the core on sale only if collected), against the linear campaign.

## Decisions

- 2026-10-27 (Act 5 design phase, step `m9-d-levels-3`; pending the user's review in round 60): drafted from the
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
