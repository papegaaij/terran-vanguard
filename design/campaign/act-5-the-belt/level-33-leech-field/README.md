---
title: Level 33 – Leech Field
design: approved
implementation: done
art: final
depends-on: [../../../enemies/space/void-leech, ../../../enemies/ground/rail-bunker, ../../../enemies/air/talon, ../../../enemies/space/asteroid-mite, ../../../player/generator, ../../../world/belt, ../../../player/wingmen, ../../../story/characters/the-choir, ../../../systems/economy]
updated: 2026-10-31
---

# Level 33 – Leech Field

## Summary

The act's energy level. Aegis Wing crosses a rock field the Vrell have grown over, where
[Void Leeches](../../../enemies/space/void-leech/README.md) nest in the growth and come in from behind
and the sides to latch on and **drain the generator**
([drain rules](../../../player/generator/README.md#enemy-drain-effects)): a loadout without spare power
loses its shield's regrowth, then its rate of fire. [Rail Bunkers](../../../enemies/ground/rail-bunker/README.md)
dug into the big rocks lay their red lines across the field, and the leeches feed on the Ascendancy's
own [Talons](../../../enemies/air/talon/README.md) too: the uneasy alliance shown. The Choir speaks for the
one time in Act 5. Density 4, no boss, 178 s: the field's edge, the grown rocks, the bunker ridge, the
feeding ground, the swarm and clear rock.

**Status: approved** (Act 5 design phase, step `m9-d-levels-3`, 2026-10-27), **approved by the user in concept round 60 (2026-10-27)**. Written from the act roster, the gap analysis's recommended answers (**Q2 a** the drain,
**Q1 a** the Talons' shields, **Q11 b** the belt look (gunmetal/charcoal with gold); judgement calls **J2** the reference DPS 136 and
**J3** the ground layer for the bunkers, all decided in round 60) and the wave 1 documents. Every number is a
hand-written first draft at medium for part F to measure with `levelReport`. **Built in M9 part F** (step F2,
2026-10-29): the level script is [data.yaml](data.yaml); the marked tables below are rendered from it and measured
with `levelReport`.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

<!-- data: briefing -->
> **Commander Okafor:** "Between the deep core and Ceres lies a rock field the Vrell have grown
> over. The Ascendancy keeps rail bunkers on the big rocks. Cross it, Lancer. Rook flies your
> wing."
>
> **Dr. Varga:** "The growth is full of leeches. They latch on and feed on your generator: your
> shield stops regrowing, then your guns slow. Bring spare power, and shake them off."
<!-- /data -->

*Images* (production art in part F; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-33-leech-field`: a chart of the field: the route through the big rocks, the
  bunkers' positions as red ticks, the growth shaded violet, Ceres's light at the top edge.
- Varga's page `level-33-power-drain`: her diagram of the drain: a leech latched on the Stormhawk's
  hull, the power gauge with 4 MW drained in the warning colour, the shield ring dimmed, and the
  shake-off (left-right-left-right) drawn as arrows.

*Hangar teaser* (shop screen before L33; Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "Varga says bring spare power. I'm bringing spare jokes."
<!-- /data -->

## Threat profile

Draft; directions are counted by each wave's `from` (the intel's rule): 78 % front, 11 % rear, 11 % sides
(the leeches' streams and ambushes are every non-front wave).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (written `space` in the belt: the Talons, the Void Leeches, the Asteroid Mites on their rocks, the Vrell popcorn), `ground` (the Rail Bunkers on the large asteroid surfaces, judgement call J3) |
| Attack directions | front 84% · sides 8% · rear 8% |
| Density | 4 |
| Recommended traits | `rear`, `side` (the roster's `beam` replaced: a latched leech is hit only by rear and side fire, see *Decisions*) |
| Hazards | Generator drain (the leeches); the Rail Bunkers' rail lines; drifting asteroids (small ones break, big ones block) |
| Boss / mid-boss | none |
| Sensor-suite detail | none: the belt, Leech Field, `air` + `ground`, front, rear and sides · L1: + directions, density 4, hazards "power drain" and "rail lines", the objective `CROSS THE LEECH FIELD` and the secondary `LEECHES` · L2: + Void Leech (new), Rail Bunker (new), Talon, Asteroid Mite, Stinger, Skitter, Mote Swarm portraits; the drain marked "power, not shield" · L3: + the rear and side entries on the threat arrows, `rear` and `side` highlighted |
<!-- /data -->

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`); the drain warning is in the
first line, so every player reads it ([generator](../../../player/generator/README.md#enemy-drain-effects)):

| Sensor suite | Line |
|---|---|
| none | "A rock field full of leeches. Bring spare power, Lancer." |
| L1 | "Leeches from behind and the sides. Bunkers on the rocks." |
| L2 | "New: Void Leeches drain your generator. Bunkers fire lines." |
| L3 | "Rear and side guns throw a leech off. Keep 4 MW spare." |

## Objective

- **Primary** `reach-end`: cross the field (`CROSS THE LEECH FIELD`).
- **Secondary** "Purge the field" (`escapes: void-leech`, label `LEECHES`): destroy at least **48 of the 63**
  Void Leeches at medium before they leave the screen, failed by the sixteenth away (a shaken-off leech that
  flees, or one carried off on a drifting prey Talon, counts as away). Easy and hard keep the allowance at a
  quarter of their leech count (the formation lever). Pays **100** in Act 1 terms (655 in Act 5); the typical
  haul counts 50 %. It rewards killing a latched leech (rear and side fire) over only shaking it off.

## Layout

Scroll **150 px/s**, **130 px/s** on the bunker ridge (the rail lines need room to read; the data eases each
change over 2 s). Setting: [the belt](../../../world/belt/README.md)'s Vrell-infested field, vacuum
([vacuum rules](../../../world/belt/README.md#vacuum-rules)): no weather, explosions flash and throw debris
in a ring. **Light**: the belt's top-left sun on warm brown-grey rock, the sunlit dust band in the deep layer,
the growth's black-violet sheen; a step lighter than Act 4 (Q11 b) so the Talons' gunmetal reads. Motion budget:
the leeches and the rail lines are the strong elements, the drifting rocks the second; the growth is still.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Field Edge | 0–24 | 0–3,600 | 150 | clear | `ground`: the first small asteroid surfaces, bare sunlit rock. `air`: drifting rocks, two carrying Asteroid Mites. `deep`: the dust band, the Sun as a bright point. | Popcorn and Talons; the first leech stream from the rear (t=22.5), alone. |
| 2. The Grown Rocks | 24–60 | 3,600–9,000 | 150 | clear | `ground`: large asteroid surfaces overgrown with black-violet Vrell growth (leech sacs pulsing), the first Rail Bunker dug into a rock face (t=33.5). `low-air`: smaller rocks. | The bunker taught alone; leeches from the sides and the rear. |
| 3. Bunker Ridge | 60–95 | 9,000–13,570 | 130 | clear | `ground`: a long rock ridge with bunker pairs (t≈68, t≈86) whose lines cross with a safe column between; the growth on its flanks. | The first **prey** wing (t=62.5): the leeches feed on the Talons. |
| 4. The Feeding Ground | 95–118 | 13,570–17,000 | 150 | clear | `ground`: the growth at its thickest, the drifting wrecks of latched Talons (scenery), the prospector's claim shack *Claim 114* in the growth (t≈104, the secret). | The second prey wing; then the Choir (t=118). |
| 5. The Swarm | 118–163 | 17,000–23,750 | 150 | clear | `ground`: leech nests in the growth, a bunker pair (t≈132) and a single bunker (t≈150). `air`: rocks thick with riders. | The leeches at their densest (at most 6 free on the screen), every 7.5 s from the rear or a side. |
| 6. Clear Rock | 163–178 | 23,750–26,000 | 150 | clear | `ground`: the rocks thinning, the growth gone, bare rock lit gold; Ceres's light ahead in the deep layer. | Release; the level ends at t=178. |
<!-- /data -->

**Rocks.** The belt's drifting asteroids ([hazards](../../../world/belt/README.md#hazards--set-pieces)) cross
every section: small ones (a few hits) carry the Asteroid Mites ([rock riding](../../../enemies/README.md#rock-riding)),
big ones block shots of both sides and give the leeches cover to come round; at most two big rocks on the
screen at once.

**The drain on the screen.** The [generator](../../../player/generator/README.md#enemy-drain-effects) owns the
rules and the [HUD](../../../ui/hud/README.md) the gauge; the level's part is the count: at most **6 free leeches**
on the screen ([Void Leech](../../../enemies/space/void-leech/README.md)), a leech wave never within 7 s of the
last one in sections 1–4, and the Swarm's streams 7.5 s apart, so a player who shakes them off as they come
never carries more than two, and one who ignores them reaches the cap of three. The typical plan at L33
(20 of 20 MW, the [Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)) loses its
shield's regrowth to two and an eighth of its fire to three.

**The uneasy alliance.** Two Talon wings (t=62.5, t=95) are marked **prey**: the leech stream that follows each
(t=65, t=97.5) enters from the side the wing flies on and latches on its Talons first (the
[Void Leech](../../../enemies/space/void-leech/README.md)'s prey rule: the Talon's shield empties, it stops firing
and drifts down with the scroll). The other Talon wings are not prey; a leech never leaves a prey for the ship.

**Rook and the escort.** Rook flies his slot ([wingmen](../../../player/wingmen/README.md)); the drain never
touches him (the escort has its own power). With `--escort none` the level is the same without him and his
lines.

## Waves

Formation names from the [formation vocabulary](../../../enemies/README.md#formation-vocabulary). Rendered from
the data (step F2): data in **Act 1 terms** with a `bounty_scale` (see *Credit budget*). HP are the units' own
pages': the new Act 5 units at their first levels, the Vrell popcorn (`tiny`) at 1 HP, the Stinger with the
beam era's ×2; no returning `medium` or larger unit flies here, so no act HP factor applies.

- **Density and pacing** (the Act 2+ rules, `PacingTest` and `ActFloorTest`): **393 enemies** (waves 385 +
  ground targets 8) over 178 s, **≈ 136 a minute** at medium (the draft's 326 and ≈ 110 plus step F2's firing
  groups, as Levels 29 and 32 needed them: 124 and 125 a minute), a wave every 2.5 s with the quiet windows
  below and a firing Talon group on most offbeats. Step F2's `levelReport` at medium with the plan's fit and Rook:
  **3.02 on the screen**, 62.8 shots a minute, two stretches over 2 s (the Choir's window and t≈169–171), the small
  units 1.51 s on the screen against Level 10's 1.60 (0.94×). The Vrell popcorn dies within a second under the
  Ion Beam L4, so the shots and the screen come from the Talons: the draft's fifteen groups plus seventeen trios on
  the offbeats, a pair over each Stinger column (alone, a column died in 0.45 s) and the last Talons over clear rock.
- **Quiet windows**: 3.5 s after the first leeches (t=22.5); no air wave between t=31 and t=37 while the first
  bunker teaches its line (t=33.5); 6 s before the Swarm while the Choir speaks (t=115 to t=121).
- **Directions**: 78 of 92 waves from the front; every rear wave has a BOTTOM warning and every side wave
  an edge warning 3 s ahead.
- **Totals**: Asteroid Mite 53 · Skitter 50 · Mote Swarm 61 · Stinger 20 · Talon 138 (6 leaders) · Void Leech 63.
- **The leeches** (step F2's flights, the plan's fit ±Rook, three seeds): at most **5 free** on the screen at medium
  (easy 3, hard 6: the cap), at most 3 latched; the prey wings' streams enter as the wing settles into its hold
  (t=63.5 and 96, at 190 px/s along the wing's height), and 1–5 Talons are preyed on in every flight (the plan's
  beam kills the rest of the wing first).

Wave table (`front (left)`: from the front at that side; `side`: from that edge; `rear`: from the
bottom edge; a Mite group of 5 is two rows, 2 rocks of 2 and 1 of 1):

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 5 | 1 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Two rocks carrying the riders |
| 7.5 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Shielded Talons; the leader at the tip |
| 8.75 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 10 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | |
| 12.5 | 1 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 12.5 | 1 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 1 | front | |
| 15 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 17.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 9 | front | |
| 18.75 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 20 | 1 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (right) | |
| 20 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step F2: a Talon pair over the Stinger column (both fire at once) |
| 22.5 | 1 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | rear | The first leeches, alone, from the rear (BOTTOM warning 3 s ahead); a quiet window of 3.5 s after it |
| 26 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) | |
| 27.25 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 28.5 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 31 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The leader drops a shield cell |
| 37 | 2 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | left side | Edge warning at t=34; the first Rail Bunker on the screen (t=33.5) |
| 39.5 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | |
| 40.75 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 42 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 44.5 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 44.5 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 1 | front | |
| 47 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 47 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step F2: a Talon pair over the Stinger column (both fire at once) |
| 49.5 | 2 | swarm | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | rear | Rear ambush (BOTTOM warning at t=46.5) |
| 50.75 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 52 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 9 | front | |
| 54.5 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 55.75 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 57 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 60 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) | |
| 62.5 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 7 | front | Prey: a refuelling hold (5 s at 140-200 px), the t=63.5 leeches feed on them |
| 63.5 | 3 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | right side | Aimed at the prey wing, at its height from its side as it settles (edge warning 3 s ahead) |
| 67.5 | 3 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 67.5 | 3 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 1 | front | |
| 68.75 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 70 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 72.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 9 | front | |
| 75 | 3 | swarm | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | rear | Rear ambush (BOTTOM warning at t=72) |
| 77.5 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | |
| 77.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step F2: a Talon pair over the Stinger column (both fire at once) |
| 78.75 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 80 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | |
| 82.5 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 85 | 3 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | left side | Edge warning at t=82 |
| 87.5 | 3 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 88.75 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 90 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 92.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 95 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 6 | front | Prey again: the t=96 stream feeds on them |
| 96 | 4 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | right side | Aimed at the prey wing as it settles (edge warning 3 s ahead) |
| 98.75 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 100 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) | |
| 102.5 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 102.5 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 1 | front | |
| 105 | 4 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 105 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step F2: a Talon pair over the Stinger column (both fire at once) |
| 106.25 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 107.5 | 4 | swarm | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | rear | Rear ambush (BOTTOM warning at t=104.5) |
| 110 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 9 | front | |
| 111.25 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 112.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 115 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | The last wave before the Choir (t=118): no wave for 6 s |
| 121 | 5 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | rear | The Swarm begins: BOTTOM warning at t=118, under the Choir's line |
| 123.5 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | |
| 124.75 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 126 | 5 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The leader drops an overdrive |
| 128.5 | 5 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | left side | Edge warning at t=125.5 |
| 131 | 5 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 131 | 5 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 1 | front | |
| 133.5 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 9 | front | |
| 136 | 5 | swarm | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | rear | Rear ambush (BOTTOM warning at t=133) |
| 138.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 139.75 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 141 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) | |
| 143.5 | 5 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | right side | Edge warning at t=140.5 |
| 146 | 5 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 146 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step F2: a Talon pair over the Stinger column (both fire at once) |
| 148.5 | 5 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 149.75 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 151 | 5 | swarm | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | rear | Rear ambush (BOTTOM warning at t=148) |
| 153.5 | 5 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 156 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 157.25 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 158.5 | 5 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | left side | Edge warning at t=155.5; the last leeches |
| 161 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | |
| 163.25 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 3 | front | Step F2: a firing Talon group on the offbeat (the beam erases the popcorn within a second) |
| 165 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 167 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step FIX6: t=167, not 172.5 (the calm before the end). Step F2: the last Talons over clear rock |
| 168 | 6 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 169.5 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step FIX6: t=169.5, not 175.5 (the calm before the end). Step F2: a firing Talon pair on the offbeat (the beam erases the popcorn within a second) |
| 171 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) | The last wave; the level ends at t=178 |

Totals: Asteroid Mite 53 · Talon 138 · Skitter 50 · Mote Swarm 61 · Stinger 20 · Void Leech 63.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 2 | [Rail Bunker](../../../enemies/ground/rail-bunker/README.md) on a rock face (t≈33.5), alone | Bounty 45; hardened (only `anti-ground` weapons and the specials hurt it); its first line taught with no air wave until t=37 |
| 3 | Rail Bunker pair on the ridge (t≈68), their lines crossing with a safe column between | Bounty 45 each; their first lines half the interval apart; never more than 2 lines telegraphed at once (the bunker's rule) |
| 3 | Rail Bunker pair on the ridge (t≈86; hard a trio); hard ×3 | Bounty 45 each; staggered as the first pair (hard's trio a third of the interval apart) |
| 5 | Rail Bunker pair in the growth (t≈132; hard a trio); hard ×3 | Bounty 45 each; staggered (hard's trio a third of the interval apart) |
| 5 | Rail Bunker, single (t≈150) | Bounty 45 |
| 4 | The prospector's shack *Claim 114* in the growth (a `ground` trigger, 20 HP, t≈104, x 380) | The secret: frees a crate worth 100 (655) |
<!-- /data -->

## Hazards

- **Generator drain** (Q2 a, decided in round 60): the [drain rules](../../../player/generator/README.md#enemy-drain-effects);
  the leech's latch, shake-off and the EMP's answer are the [Void Leech](../../../enemies/space/void-leech/README.md)'s.
- **Rail lines**: the [Rail Bunker](../../../enemies/ground/rail-bunker/README.md)'s locked line and its `heavy` hit;
  they fire up the screen too, so a bunker the ship has passed still matters.
- **Drifting asteroids**: the [belt's](../../../world/belt/README.md#hazards--set-pieces) small and big rocks.
- **Specials**: no setting limit (the Airstrike and the Orbital Lance work; they hurt the hardened bunkers);
  the Sonar Pulse reads `NO WATER` ([vacuum rules](../../../world/belt/README.md#vacuum-rules)). The EMP Burst throws
  off every latched leech and empties every Talon's shield.

## Secrets and pickups

- ***Claim 114*** (the secret, t≈104, x≈380, in the feeding ground): a belt prospector's claim shack on a
  grown-over rock, its beacon still blinking under the growth. A `ground` trigger of **20 HP** (28 on hard);
  destroyed, its strongbox floats up as a crate worth **100** in Act 1 terms (655). The Salvage scanner's glint
  shows it at any range. Its event line, Rook: "*Claim 114. Someone staked this rock. The Vrell got here first.*"
- **Shield cell**: dropped by the leader of the t=31 V-wing (the shield's regrowth is what the drain takes first).
- **Overdrive**: dropped by the leader of the t=126 V-wing, at the Swarm's start.
- **Armour patch**: a CDF nav buoy releases one at (240, 380 y-up) at t=160, at the Swarm's end.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), draft text for the **1 s rule** (text at about 12 characters a
second plus a 1.5 s lead for the length check; 0.4 s of silence; no timed line more than 1 s late); event lines
drop after 6 s. Rook's lines carry `requires: escort`. Speakers: **Okafor, Varga, Rook, the Choir**.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Leech Field, Lancer. Bunkers on the rocks. Keep your power up." |
| t=13 | Rook | "Rocks with teeth. Lovely. Keep your tail clear, Lancer." |
| t=23 | Varga | "Leeches behind you! They drain power. Jink to throw them." |
| t=34.5 | Okafor | "Rail Bunker. A red line: it's locked on. Step off the line." |
| t=46 | Varga | "Rear and side guns hit a latched leech. Front guns can't." |
| t=65 | Varga | "They're on the Talons too. Feeding on their own allies." |
| t=71.5 | Okafor | "Some alliance. Don't count on it, Doctor. Keep firing." |
| t=86.5 | Rook | "Two red lines. I'll take the space between, thanks." |
| t=97.5 | Varga | "More Talons with leeches. They just drift." |
| t=118 | The Choir (distorted) | "…the one who opened… calls to us… we do not answer… the door is open… we are going…" |
| t=130 | Varga | "'The one who opened.' That's Vorne. They don't care about him." |
| t=137 | Okafor | "Then he's alone in it. Good. Leeches from every side now." |
| t=150 | Rook | "Lancer, you've got passengers. Shake 'em loose!" |
| t=164 | Okafor | "Clear rock ahead. The graveyard next, then the Sovereign." |
| Secondary met, held to t=172.5: after the last leech, in the calm before the end (step FIX6, the user's choice) | Okafor | "Field's clean. Not one leech left to follow us home." |
<!-- /data -->

Step F2 trimmed every timed line to one radio page (three 22-character lines, 5 s; the drafts ran to two) and
kept the Choir's two pages; Varga's lines follow the prey latches (t=63.5 → 65, t=96 → 97.5) and her answer to the
Choir comes after its voiced line and its 2 s static lead (t=128 → 130, the gap check's §4). Text time: t=1 closes
≈ 6, t=13 ≈ 18, t=23 ≈ 28, t=34.5 ≈ 39.5, t=46 ≈ 51, t=65 ≈ 70, t=71.5 ≈ 76.5, t=86.5 ≈ 91.5, t=97.5 ≈ 102.5,
the Choir (two pages) ≈ 128, t=130 ≈ 135, t=137 ≈ 142, t=150 ≈ 155, t=164 ≈ 169; nothing overlaps, and *Claim
114* is in reach at t≈101–107, when the radio is free (102.5 to 118).

**Story notes.** The Choir's line is its one Act 5 transmission ([the Choir](../../../story/characters/the-choir/README.md#act-5-draft)),
placed before the Swarm in its usual warning role; the Swarm begins under it (t=121). Varga's reaction names
Vorne, as her Act 5 rule asks: she says what she sees, on every channel
([Varga](../../../story/characters/varga/README.md#act-5-draft)); Okafor no longer files it ("Log it, Doctor"
was Acts 3–4's refrain) but answers it. The prey wings carry the act's beat "the alliance is uneasy: the
leeches drain Ascendancy ships too" ([twist](../../../story/twist/README.md)). Varga's power warning is in the
briefing and the intel line, not repeated on the radio before the first leech.

## Music & ambience

The [Act 5 music plan](../../../audio/music/README.md#act-5-music-draft-round-60)'s cue for this level:
[track 13](../../../audio/music/README.md#track-list), the Act 5 B theme (two sketches in round 60): the **base stem
from the start**, the **full mix from the Swarm** (t=121), and the Choir's line (t=118) **ducks the theme −6 dB**
(Level 26's duck) while it speaks. **Vorne's motif is muted** all level: the Vrell's field, the Choir's level.
Ambience: the `belt-open` bed ([sfx](../../../audio/sfx/README.md#ambience-per-setting): hull creaks, distant impacts,
radio hiss, heard through the hull) throughout. New sounds this level needs (in the units' briefs and the act's
[sfx](../../../audio/sfx/README.md) list): the leech's latch and drain whine, the brown-out hum while the load is over
the output ([generator](../../../player/generator/README.md#enemy-drain-effects)), the rail's charge and crack, the hex
shield's hit and shatter. Freesound only for real-world sounds (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(33) = 700 × 1.07³² ≈ **6,101** ([economy](../../../systems/economy/README.md#per-level-budget)).
Data in **Act 1 terms** (Asteroid Mite 3, Skitter 5, Mote Swarm 2 a mote, Stinger 15, Talon 12, leader 25,
Void Leech 12, Rail Bunker 45, the crate 100, the secondary 100), × the act factor **6.5536** (1.6⁴), bounties
× the scale. The draft's **`bounty_scale` 0.55** (by hand at ≈ 110 a minute) is **0.43** with step F2's
firing groups: the typical haul with the hardened Rail Bunkers at **0 %** (below) is **≈ 6,155 (+0.9 %)**. The
table below counts the bunkers at 0 % as the typical haul `TypicalHaulTest` checks does (step F3: a test-side rule,
`TypicalHaul`'s hardened ground at 0 % while the plan's fit has no `anti-ground` gun; `tools/sync_tables.py` renders
the same since M9 part I, before it the economy's 80 %: +14 %).

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Asteroid Mite 53 × 3 + Talon 138 × 12 + Skitter 50 × 5 + Mote Swarm 61 × 2 + Stinger 20 × 15 + Void Leech 63 × 12 | 9,164 | 5,498 |
| Squad leaders: 6 × 13 extra (perfect run only) | 216 | 0 |
| Ground targets: Rail Bunker 8 × 45 (hardened: typical 0 %, the plan's fit has no anti-ground gun) | 1,016 | 0 |
| Secret: Claim 114 (hidden crate, 2% of budget) | 655 | 328 |
| Secondary: purge the field | 655 | 328 |
| **Total** (bounty scale 0.43) | **11,706** | **6,153** |
| Budget(n) = the typical haul's target; typical +1 %, perfect 1.92 × budget | | 6,101 |
<!-- /data -->

**The bunkers and the plan.** The Rail Bunkers are hardened and the hand plan's L33 fit (the Ion Beam L4, two
Micro-missile Pods, the Swarm Tail, the EMP Burst) has no `anti-ground` weapon, so the plan kills them only
with a special, so the typical haul counts them at **0 %** (decided) and the scale is set without them: 0.43
(the draft's 0.55; 0.37 with the economy's 80 % ground share, 811 of the haul that the plan could not earn). The
perfect run is 1.92 × the budget (the act's others 1.48–1.70): a player who brings `anti-ground` takes the bunkers'
on top.

**Balance plan**: the plan's L33 visit buys nothing (20 of 20 MW, saving for the Mk V; the
[Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)), single-target forward DPS 135
against the proposed reference **136** (J2), 118 with three leeches latched. The level asks for spare power
(turn a weapon down a level in the hangar) and rear or side fire for the latched leeches; it is flyable without
either, at a slower rate of fire.

## Difficulty notes

- **Easy**: the formation lever thins the streams (leech streams of 3, ambushes of 2–3; at most 4 free); the
  Talons' pair every 2.1 s; the bunkers' and the leeches' easy hooks (their pages); *Claim 114* 15 HP; the
  secondary's allowance a quarter of the easy count.
- **Hard**: leech streams of 5–6 and ambushes of 5 (at most 6 free, the cap); the Talons' pair every 1.3 s; the
  bunker pairs at t≈86 and t≈132 become trios on the ridge's length (still at most 2 lines telegraphed); prey
  wings keep their leeches (a prey wing is never cut); *Claim 114* 28 HP.

## Concept art

The level needs (part F's production art; the mood from the belt's round-60 mood stills):

- the field backdrop (six sections: the bare edge, the grown rocks, the ridge, the feeding ground, the nests,
  clear rock) and the big and small rocks with the growth; the shack *Claim 114*, the nav buoy;
- the Void Leech's latched poses on the Stormhawk and on a Talon (its own page), the prey Talon's drifting pose;
- the two briefing images; the intel portraits of the Void Leech and the Rail Bunker;
- the round's captures: the level at medium with the plan's fit and Rook; a ship with three leeches latched
  (the gauge, the dimmed ring, the flicker); a prey wing; the bunker pair's crossed lines; readability crops of a
  Talon over the growth at 1:1 (Q11).

Production art, M9 part F (concept round 66, step ART-F2): the field backdrop (`tools/art/backdrop_l33.py`, its places
for step F2 in [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml): the faces with the bunkers' pads (step
FIX3, round 66's C.7 b: a pad of its own at x 240 on `ridge-2` and `nest-face-1` for hard's third bunker), Claim
114's ledge, the grown rocks' `debris:`, the nav buoy's drop), the grown rocks, Claim 114 and the CDF nav buoy
(`tools/art/belt_units_f.py`). The Void Leech is in the [space units' concept art](../../../enemies/space/README.md#concept-art)
(its latched poses on the Stormhawk and on a prey Talon on its sheet), the Rail Bunker in the
[ground units'](../../../enemies/ground/README.md#concept-art); the two briefing images in the
[briefing](../../../ui/briefing/README.md#concept-art), the intel portraits in the [hangar](../../../ui/hangar/README.md#concept-art).
Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r66-a.png](concept/backdrop-final-r66-a.png) | Production backdrop (`tools/art/backdrop_l33.py`): eight screens at half size with stand-ins (the field's edge and the Sun, the first bunker on its pad, the grown rocks, the ridge's two pairs, Claim 114 in the feeding ground, a pair in the Swarm, clear rock with Ceres ahead), the pieces (sheet) | chosen (round 66, F.4 ok: accepted as final) |
| [concept/backdrop-final-r66-a.gif](concept/backdrop-final-r66-a.gif) | t 60-70 at 8 fps: into the bunker ridge, the first pair on its pads, the grown rocks drifting | chosen (round 66, F.4 ok: accepted as final) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block, its reading of the sections, the ground targets' places (the bunkers on the faces' pads, Claim 114 on its ledge), the nav buoy's drop and the grown rocks' `debris:` for step F2 (not data) | chosen (round 66, F.4 ok; the pad at x 240 is C.7 b, built in FIX3: accepted as final) |
| [concept/grown-rocks-final-r66-a.png](concept/grown-rocks-final-r66-a.png) | The grown rocks (`tools/art/belt_units_f.py`): part C's large rocks and part B's small ones with the growth's black-violet lumps and violet sacs, the small one shot apart, beside the plain ones (sheet) | chosen (round 66, F.5 ok: accepted as final) |
| [concept/ground-objects-final-r66-a.png](concept/ground-objects-final-r66-a.png) | Claim 114 (the prospector's shack on its cut pad, the amber beacon, the growth over one end; shot open) and the CDF nav buoy (shut, opening, empty, its strobe) (sheet) | chosen (round 66, F.6 ok: accepted as final) |
| [concept/ground-objects-final-r66-a.gif](concept/ground-objects-final-r66-a.gif) | Claim 114 blinking, shot open, its strongbox rising; the nav buoy opening | chosen (round 66, F.6 ok: accepted as final) |
| [concept/prey-wing-capture-r66-a.png](concept/prey-wing-capture-r66-a.png) | Game capture (step F5; [recipe](concept/prompts.md#level-33-capture-r66-a)): the first prey wing on its hold, the preyed Talons dark and tumbling with their riders | chosen (round 66, F.1 a: the prey Talon by tint, accepted with the level) |
| [concept/talons-growth-capture-r66-a.png](concept/talons-growth-capture-r66-a.png) | Game capture: 1:1 crops of Talons over the ridge's growth (Q11's readability) | chosen (round 66, accepted with the level, F.4) |
| [concept/claim-114-capture-r66-a.png](concept/claim-114-capture-r66-a.png) | Game capture: Claim 114 coming down shut, shot open, its crate | chosen (round 66, accepted with the level, F.6) |
| [concept/choir-capture-r66-a.png](concept/choir-capture-r66-a.png) | Game capture: the Choir's line (t=120) | chosen (round 66, accepted with the level, C.6) |
| [concept/nav-buoy-capture-r66-a.png](concept/nav-buoy-capture-r66-a.png) | Game capture: the CDF nav buoy and its armour patch | chosen (round 66, accepted with the level, F.6) |
| [concept/level-33-capture-r66-a.mp4](concept/level-33-capture-r66-a.mp4) | Game capture with the game's sound: three leeches latching (the latch, drain whine, hum, brown-out), the prey wing, a rail line and shot | chosen (round 66, accepted with the level, F.1–F.6; its rail line is C.1 a's, stale since FIX3: the ridge pair's line riding with the bunker is the Rail Bunker's `rail-bunker-capture-r69-a`) |
| [concept/nest-face-before-after-r69-a.png](concept/nest-face-before-after-r69-a.png) | Before-and-after sheet (step I3; [recipe](concept/prompts.md#nest-face-before-after-r69-a)): `nest-face-1` with step F's middle nest and with FIX3's pad at x 240 and the nest moved, 1:1 (round 69 B.7) | chosen (round 69, B.7 ok) |

## Implementation

- [x] Level data `data.yaml` (when built in part F): sections and speeds with eases, waves, ground targets,
      radio, budget, threat profile, briefing, backdrop, music; vacuum (no weather)
- [x] The generator drain in the level (part A's rules on a fixture first; flown and hashed here with the leeches)
- [x] Void Leech waves: rear and side entries warned 3 s ahead, at most 6 free on the screen, the prey rule on
      the marked Talon wings (steps F1–F3: `PreyLatches`, `LeechChecks`; `Level33Test` counts at most 6 free on every
      flight, 3 at most latched; 1–8 Talons preyed a flight; QF-1 a as built, retimed, accepted in round 66 C.2)
- [x] Rail Bunkers on the asteroid surfaces (`ground`): never more than 2 lines telegraphed, a safe column
      between crossed lines (step FIX3, round 66's C.1 b: the line rides with the bunker, its direction locked; the
      autopilot looks ahead along the moving band: `Level33Test` and `levelReport` won 3/3 with 0 retries a difficulty)
- [x] Drifting rocks with the Asteroid Mites' riders
- [x] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), the quiet windows, edge and BOTTOM warnings
      (step F2: 17 offbeat Talon trios, `bounty_scale` 0.43; step F3: the plan's fit in `Level33Test`)
- [x] Secondary `escapes: void-leech` (48 of 63 at medium; the allowance per difficulty), judged as leeches leave
- [x] Radio voiced, every timed line at most 1 s late, with Rook and without (`requires: escort`); the Choir's
      line with its layering (step F4: `RadioTimelineLevel33Test`, the secret's line Rook's only)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (step F4:
      `IntelPanelLayoutTest`; the leeches' rear and side entries marked on the L3 wave strip, `ActFiveHangarTest`)
- [x] Briefing: both pages, each one screen, with their images (step F4; ART-F2's two images)
- [x] Typical haul within ±5 % of budget(33) = 6,101 with the level's `bounty_scale`
- [x] Music: track 13's base, then full from the Swarm, the −6 dB duck under the Choir, the motif muted; the `belt-open` ambience
      (step F4: `Level33SoundsTest`; the level's sounds in `LeechFieldCues`, round 66's picks: a, S.6 b `brownout-warn-r66-b` since step FIX3)
- [x] Easy/hard variations; the autopilot flies the level on every difficulty (with and without Rook) and shakes
      off leeches
- [x] Production art and captures for the round (ART-F2; step F5's game captures, round 66)

## Open questions

Approved in round 60 (the gap analysis's questions this level follows):

- **Q2, the drain** (recommended **a**, drafted: −2 MW a leech to −6, shield regrowth first, then the fire rate).
  Alternatives: **b** a flat −20 % fire rate a leech whatever the loadout (the briefing's and intel's "bring spare
  power" would go; the secondary stays); **c** each leech blacks out the fitted weapon with the highest draw until
  it is shaken off (the level would cut the leech count by a third).
- **J3, ground in L33** (drafted: the Rail Bunkers on asteroid surfaces, so L30 is the act's only space-only
  level). Alternative: L33 space only, the bunkers moved to L34's hulls or dropped, `NO GROUND` back on L33.
- **Q11, the Ascendancy look** (decided in round 60, **b**, not the recommendation: gunmetal/charcoal hulls, gold ≥ 20 %, rim
  light, red lights; the field a step lighter than Act 4). Rejected: **a** gloss black, **c** black silhouettes with gold
  accents only.
- **Q1, enemy shields** (recommended **a**: the Talons' 2 HP hex shield, emptied by the EMP and by a leech on a prey
  Talon). Alternatives **b** shields only `shield-breaker`, beams and the EMP get through; **c** no shields in Act 5.

## Decisions

- 2026-10-27 (Act 5 design phase, step `m9-d-levels-3`; approved in round 60): drafted from the
  roster row, the gap analysis (§0, §1.1, §2.4, §2.8, Q1 a, Q2 a, Q11 a, J2, J3) and the wave 1 documents
  (the Void Leech, the Rail Bunker, the Talon, the Asteroid Mite, the generator's drain rules, the belt);
  Level 25 as the model (every section, hand tables marked draft, no `data.yaml`).
- 2026-10-27: ≈ 110 a minute at medium (326 in 178 s, a wave every 2.5 s) to the Act 2+ floor; the Vrell popcorn
  carries the count, the Talons the shots (Q6's mixed waves; no Darts here: no Gunship or station launches).
- 2026-10-27: the recommended traits are **`rear`, `side`**, not the roster's `rear`, `beam`: a latched leech is hit
  only by the rear mount, side and rear weapons and blasts (the Lamprey's rule the leech follows), and a beam's
  reach is no answer to it.
- 2026-10-27: the secondary is a leech purge (`escapes: void-leech`, 48 of 63), not the 65 % kill ratio: it
  rewards killing latched leeches, the level's lesson.
- 2026-10-27: the Choir speaks at t=118, before the Swarm (its Act 5 line, the-choir's document); Varga answers it
  and names Vorne; the prey wings show the uneasy alliance (the leech's prey rule).
- 2026-10-27: the secret is the prospector's shack *Claim 114* (belt settlers, before the Vrell).
- 2026-10-27, design phase: the hardened Rail Bunkers count at **0 %** in the typical haul (the plan's fit has no
  `anti-ground`), so the `bounty_scale` is recomputed **0.46 → 0.55** (typical 6,099, −0.0 %; perfect 11,680, 1.91 ×
  the budget); the eight bunkers stay (step m9-d-act; approved in round 60).
- 2026-10-27, design phase: rail lines pass the big rocks (accepted: the line is the bunker's `laser-line`, not a
  bullet; the rocks stop bullets only).
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`, D.6): Q2 **a** (a latched Void Leech drains the generator), J.12 kept (2 MW per leech), Q12 **c**; the brown-out hum is S.10 a, the rail line S.7 b and S.8 b. Q11 **b**: gunmetal/charcoal units.
- 2026-10-29 (M9 part F, step F2; decided in round 66, 2026-10-29): the level built as data on the approved
  tables and ART-F2's proposal, with these measured changes: **firing groups** (seventeen Talon trios on the
  offbeats, a pair over each Stinger column, the last Talons at t=172.5 and 175.5: 138 Talons, ≈ 136 a minute;
  without them 1.98 on the screen and the small units 0.82× Level 10's); **`bounty_scale` 0.43** (the draft's 0.55
  with the firing groups' bounties; the hardened bunkers at 0 % in the typical haul, ≈ 6,155); **the prey streams**
  enter at the wing's t + 1 s (t=63.5, 96: as the wing settles into its hold, at 64.2 and 96.8), at 190 px/s along the
  wing's height, 0.35 s apart (QF-1 a's "settle + 1.5 s" let the plan's beam kill the wing first: no Talon preyed at
  medium; now 1–5 in every flight); Varga's prey lines follow the first latches (t=63.5 → 65, t=96 → 97.5) and her
  answer to the Choir comes at t=130 (the gap check); every timed line trimmed to one radio page; the Skitter swarms
  fly as snakes and the leech ambushes as swarms from the rear (Level 29's and the Lamprey's forms); four large rocks
  1.5–2.5 s earlier so none is in the ship's lowest 120 px while rear leeches enter; the bunker pairs and hard's trios
  `staggered` (their first lines half / a third of the interval apart; hard's third bunker at x 240 between the pads);
  the full mix by a stem time at t=121; the Talon's own easy pair interval (2.0 s, the README's 2.1).
- 2026-10-29, step F3 (tests): `Level33Test` flies the plan's fit (20 of 20 MW) 3 seeds a difficulty with and without
  Rook: every flight won without a retry, `LEECHES` met, at most 5 free leeches (the cap 6), at most 3 latched, the
  power budget following the latched leeches in every step, at most one line telegraphed in practice (the gate 2), no
  rail hit; the typical haul counts the hardened bunkers at 0 % (`TypicalHaul`, a test-side rule). The autopilot's
  `LeechPlay` (judgement calls, accepted in round 66 C.5): it lets the leeches latch (they are no body to
  dodge), rides one while the rear or side guns kill it (the shield at least half full), shakes two off, calls the EMP
  Burst on a third, and plans every other move among the bullets, bodies, large rocks and the locked rail lines.
  Found: the secret's Rook line ("Claim 114 …") has no `requires: escort` (a secret's radio line has no such key), so
  Rook speaks it on a flight without him; for the user.
- 2026-10-29 (step F4, the coordinator's data fixes): the setting shortened to "The belt, Leech Field" (the hangar
  intel's setting line "THE BELT, THE LEECH FIELD" ran past the panel at sensor L0; `IntelPanelLayoutTest`), and the
  secret's Rook line gets `requires: escort` (a secret's radio line now takes the key, as a radio cue's: content's
  `RadioLine.requires`), so it plays only with Rook flying; on a flight without him the secret has no line.
- 2026-10-29 (step F4, presentation; decided in round 66, 2026-10-29: the prey Talon F.1 a; the fixed line replaced by C.1 b): the prey Talon is its own frame darkened
  (0.50/0.46/0.56) and tumbling two headings a second, the leech on its back (QF-2 a, no lights-out frames); the
  simulation locks a rail line where the bunker stood at the telegraph's start and keeps it there while the bunker
  scrolls on (≈130 px over the 1.0 s telegraph), so the drawn line, rail and flash (the hit band) part from the
  turret, which turns onto the line's direction; hard's third bunker of the t≈86 and t≈132 nests stands between the
  backdrop's pads (no pad drawn under it); the leeches' rear and side entries on the L3 wave strip as violet ticks
  below it (a bar across a side entry's foot).
- 2026-10-29 (step F5): recorded in [round 66](../../../concept-rounds/round-66/README.md)'s captures (the prey wing, the
  ridge pair's lines, Claim 114, the Choir, the nav buoy, the release end, three leeches latched on hard); the round asked
  the open calls, decided in round 66 (2026-10-29): C.1 the rail line fixed on the screen while its bunker scrolls,
  C.2 QF-1 a retimed, C.3 the offbeat waves and `bounty_scale` 0.43, C.4 the leech rules, C.5 `LeechPlay`, C.6 the radio,
  C.7 hard's third bunker without a pad, C.8 the intel's leech marks. Found while recording: the ridge pair's lines never
  show together (staggered half the 4.5 s interval, 1.0 s telegraphs), so the "crossed lines with a safe column" is two
  lines in turn; a ship held still at medium is lost by t≈60; the beam opens Claim 114 at the top edge from below it.
- 2026-10-29 (user, [round 66](../../../concept-rounds/round-66/README.md), closed): all as recommended except **C.1 b**, **S.6 b** and **C.7 b**. The art accepted (`art: final`): **F.1 a** (the prey Talon by tint), **F.2 ok** (the leech stays black-violet), F.3-F.8 ok (the Rail Bunker, this backdrop, the grown rocks, Claim 114 and the nav buoy, the briefing images, the intel portraits). **C.1 b** (not the recommendation, built in FIX3): the rail line rides with the bunker: its direction is locked at the telegraph's start and its origin stays on the turret, moving with the bunker, so the band slides down with the ground; this replaces the line fixed on the screen (step F4), and the pairs' "crossed lines" stay two lines in turn. **C.7 b**: the art step adds a pad at x 240 on both faces for hard's third bunker (the backdrop's small edit, FIX3). Sounds a except **S.6 b** (the brown-out warning, switched in FIX3); **C.2-C.6 and C.8 accepted** (QF-1 retimed, the offbeat waves with `bounty_scale` 0.43, the leech rules, LeechPlay, the radio, the intel's leech marks).
- 2026-10-29 (step FIX3, round 66's C.1 b, S.6 b, C.7 b built): the rail line's origin rides with its bunker as the
  ground scrolls (the telegraph, the live band and the hit test; the direction locked), drawn from the turret as it
  scrolls (the step F4 entry's parting line, rail and flash are gone); `BROWNOUT_WARN` plays `brownout-warn-r66-b`;
  hard's third bunker of the t≈86 and t≈132 nests stands on its own pad at x 240 (`tools/art/backdrop_l33.py`'s
  `HARD_PADS`; the middle nest of `nest-face-1` moved off it to (320, 160)), no longer between the pads with none
  under it. `levelReport -Plevel=33`: won 3/3, 0 retries on every difficulty.
- 2026-10-29 (user, [round 69](../../../concept-rounds/round-69/README.md), closed): **B.7 ok** (the middle nest of
  `nest-face-1` moved to (320, 160) for hard's pad; the before-and-after sheet is `chosen`). **B.2 ok**, with the note
  that the line must lead the scroll: built in step FIX5 (the Rail Bunker's page).
- 2026-10-30 (step FIX6, the user's playthrough 2026-10-30: "the line says none are left, but then some more enter
  the screen ... moving the line to the end is best"): Okafor's "Field's clean. Not one leech left to follow us home."
  keeps its words and its trigger (the secondary met: it turns safe as the 48th leech dies, mid-level) but the radio
  holds it until **t=172.5** (the cue's new `not_before`, design/tech/architecture/schemas-levels), after the last
  leech (the last stream enters at t=158.5; `Level33Test` checks the last leech flies before t=172.5 in every
  autopilot flight) and in the calm before the end (t=178, the 5.0 s line ends ≈ 177.5). Met only at the end (a
  leech short), it plays with the closing lines as before.
- 2026-10-30 (step FIX6, the user's playthrough 2026-10-30; the campaign's new end calm rule, at least 4 s from the
  last enemy gone to the end from Act 5 on, `PacingTest`): the level's end stays; the last waves earlier: the last
  Talons 172.5 → 167 and the Talon pair 175.5 → 169.5 (the user: "the level ends a bit abruptly"); the end calm
  (easy/medium/hard, the autopilot with the plan's fit) 0.7/0.2/0.0 s → 6.7/5.2/5.0 s. The counts, the haul and the
  autopilot's wins are unchanged.
- 2026-10-30, the user: dither audit, ≥ 25 % smoothed (step DITH8): the deep `field-space` and `nav-buoy-glow` in smooth steps, not the 4×4 Bayer checker ([audit](../../../art-direction/production/README.md#dither-audit-2026-10-30)).
