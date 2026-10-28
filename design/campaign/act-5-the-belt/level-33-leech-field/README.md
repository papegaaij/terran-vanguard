---
title: Level 33 – Leech Field
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/space/void-leech, ../../../enemies/ground/rail-bunker, ../../../enemies/air/talon, ../../../enemies/space/asteroid-mite, ../../../player/generator, ../../../world/belt, ../../../player/wingmen, ../../../story/characters/the-choir, ../../../systems/economy]
updated: 2026-10-28
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
hand-written first draft at medium for part F to measure with `levelReport`; there is no `data.yaml` yet.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

> **Commander Okafor:** "Between the deep core and Ceres lies a rock field the Vrell have grown over.
> The Ascendancy keeps rail bunkers on the big rocks. Cross it, Lancer. Rook flies your wing."
>
> **Dr. Varga:** "The growth is full of leeches. They latch on and feed on your generator: your shield
> stops regrowing, then your guns slow. Bring spare power, and shake them off."

*Images* (production art in part F; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-33-leech-field`: a chart of the field: the route through the big rocks, the
  bunkers' positions as red ticks, the growth shaded violet, Ceres's light at the top edge.
- Varga's page `level-33-power-drain`: her diagram of the drain: a leech latched on the Stormhawk's
  hull, the power gauge with 4 MW drained in the warning colour, the shield ring dimmed, and the
  shake-off (left-right-left-right) drawn as arrows.

*Hangar teaser* (shop screen before L33; Rook, not voiced):

> **Rook:** "Varga says bring spare power. I'm bringing spare jokes."

## Threat profile

Draft; directions are counted by each wave's `from` (the intel's rule): 78 % front, 11 % rear, 11 % sides
(the leeches' streams and ambushes are every non-front wave).

| Field | Value |
|---|---|
| Dominant layers | `air` (written `space` in the belt: the Talons, the Void Leeches, the Asteroid Mites on their rocks, the Vrell popcorn), `ground` (the Rail Bunkers on the large asteroid surfaces, judgement call J3) |
| Attack directions | front 78% · rear 11% · sides 11% |
| Density | 4 |
| Recommended traits | `rear`, `side` (the roster's `beam` replaced: a latched leech is hit only by rear and side fire, see *Decisions*) |
| Hazards | Generator drain (the leeches); the Rail Bunkers' rail lines; drifting asteroids (small ones break, big ones block) |
| Boss / mid-boss | none |
| Sensor-suite detail | none: the belt, the Leech Field, `air` + `ground`, front, rear and sides · L1: + directions, density 4, hazards "power drain" and "rail lines", the objective `CROSS THE LEECH FIELD` and the secondary `LEECHES 48/63` · L2: + Void Leech (new), Rail Bunker (new), Talon, Asteroid Mite, Stinger, Skitter, Mote Swarm portraits; the drain marked "power, not shield" · L3: + the rear and side entries on the threat arrows, `rear` and `side` highlighted |

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

| Section | t (s) | Scroll (px) | Speed (px/s) | Layers and content | Purpose |
|---|---|---|---|---|---|
| 1. Field Edge | 0–24 | 0–3,600 | 150 | `ground`: the first small asteroid surfaces, bare sunlit rock. `air`: drifting rocks, two carrying Asteroid Mites. `deep`: the dust band, the Sun as a bright point. | Popcorn and Talons; the first leech stream from the rear (t=22.5), alone. |
| 2. The Grown Rocks | 24–60 | 3,600–9,000 | 150 | `ground`: large asteroid surfaces overgrown with black-violet Vrell growth (leech sacs pulsing), the first Rail Bunker dug into a rock face (t=33.5). `low-air`: smaller rocks. | The bunker taught alone; leeches from the sides and the rear. |
| 3. Bunker Ridge | 60–95 | 9,000–13,550 | 130 | `ground`: a long rock ridge with bunker pairs (t≈68, t≈86) whose lines cross with a safe column between; the growth on its flanks. | The first **prey** wing (t=62.5): the leeches feed on the Talons. |
| 4. The Feeding Ground | 95–118 | 13,550–17,000 | 150 | `ground`: the growth at its thickest, the drifting wrecks of latched Talons (scenery), the prospector's claim shack *Claim 114* in the growth (t≈104, the secret). | The second prey wing; then the Choir (t=118). |
| 5. The Swarm | 118–163 | 17,000–23,750 | 150 | `ground`: leech nests in the growth, bunker pairs (t≈132) and a single bunker (t≈150). `air`: rocks thick with riders. | The leeches at their densest (at most 6 free on the screen), every 7.5 s from the rear or a side. |
| 6. Clear Rock | 163–178 | 23,750–26,000 | 150 | `ground`: the rocks thinning, the growth gone, bare rock lit gold; Ceres's light ahead in the deep layer. | Release; the level ends at t=178. |

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

Formation names from the [formation vocabulary](../../../enemies/README.md#formation-vocabulary). All numbers
are **draft**: data in **Act 1 terms** with a `bounty_scale` (see *Credit budget*). HP are the units' own
pages': the new Act 5 units at their first levels, the Vrell popcorn (`tiny`) at 1 HP, the Stinger with the
beam era's ×2; no returning `medium` or larger unit flies here, so no act HP factor applies.

- **Density and pacing** (the Act 2+ rules, `PacingTest` and `ActFloorTest`): **326 enemies** (waves 318 +
  ground targets 8) over 178 s, **≈ 110 a minute** at medium (inside the act's 80–120 band, a density-4 level),
  a wave every 2.5 s with the quiet windows below, no stretch over 2 s with nothing on the screen (the rocks'
  riders and the Mote Swarms stay for seconds). The shots come from the Talons' aimed pairs (fifteen groups) and
  the bunkers' lines; part F sets the fire rates with `levelReport`.
- **Quiet windows**: 3.5 s after the first leeches (t=22.5); no air wave between t=31 and t=37 while the first
  bunker teaches its line (t=33.5); 6 s before the Swarm while the Choir speaks (t=115 to t=121).
- **Directions**: 49 of 63 waves from the front (78 %); every rear wave has a BOTTOM warning and every side wave
  an edge warning 3 s ahead.
- **Totals**: Asteroid Mite 53 · Skitter 50 · Mote Swarm 61 · Stinger 20 · Talon 71 (6 leaders) · Void Leech 63.

Draft wave table (`front (left)`: from the front at that side; `side`: from that edge; `rear`: from the
bottom edge):

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 5 | 1 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Two rocks carrying the riders (rock riding) |
| 7.5 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Shielded Talons (12 HP hex shield; the leader 24: round 62) |
| 10 | 1 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 12.5 | 1 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 5 | front (left) | |
| 15 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 17.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 9 | front | |
| 20 | 1 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (right) | |
| 22.5 | 1 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | rear | **The first leeches**, alone: BOTTOM warning at t=19.5; quiet window of 3.5 s after it |
| 26 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 28.5 | 2 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 31 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The leader drops a shield cell |
| 37 | 2 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | side (left) | Edge warning at t=34; the first Rail Bunker is on the screen (t=33.5) |
| 39.5 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 42 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 44.5 | 2 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 5 | front (right) | |
| 47 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 49.5 | 2 | rear ambush | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | rear | BOTTOM warning at t=46.5 |
| 52 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 9 | front | |
| 54.5 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 57 | 2 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 60 | 3 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 62.5 | 3 | V-wing (prey) | [Talon](../../../enemies/air/talon/README.md) | 7 | front | **Prey**: the leeches of t=65 are sent at this wing (the uneasy alliance) |
| 65 | 3 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | side (right) | Edge warning at t=62; aimed at the prey wing |
| 67.5 | 3 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 5 | front | |
| 70 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 72.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 9 | front | |
| 75 | 3 | rear ambush | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | rear | BOTTOM warning at t=72 |
| 77.5 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | |
| 80 | 3 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 82.5 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 85 | 3 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | side (left) | Edge warning at t=82 |
| 87.5 | 3 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 90 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 92.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 95 | 4 | V-wing (prey) | [Talon](../../../enemies/air/talon/README.md) | 6 | front | **Prey** again: the t=97.5 stream feeds on them |
| 97.5 | 4 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | side (right) | Edge warning at t=94.5; aimed at the prey wing |
| 100 | 4 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 102.5 | 4 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 5 | front | |
| 105 | 4 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 107.5 | 4 | rear ambush | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | rear | BOTTOM warning at t=104.5 |
| 110 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 9 | front | |
| 112.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 115 | 4 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | The last wave before the Choir (t=118): no wave for 6 s |
| 121 | 5 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | rear | **The Swarm** begins: BOTTOM warning at t=118, under the Choir's line |
| 123.5 | 5 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 126 | 5 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The leader drops an overdrive |
| 128.5 | 5 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | side (left) | Edge warning at t=125.5 |
| 131 | 5 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 5 | front | |
| 133.5 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 9 | front | |
| 136 | 5 | rear ambush | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | rear | BOTTOM warning at t=133 |
| 138.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 141 | 5 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 143.5 | 5 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | side (right) | Edge warning at t=140.5 |
| 146 | 5 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 148.5 | 5 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 151 | 5 | rear ambush | [Void Leech](../../../enemies/space/void-leech/README.md) | 5 | rear | BOTTOM warning at t=148 |
| 153.5 | 5 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 156 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 158.5 | 5 | stream | [Void Leech](../../../enemies/space/void-leech/README.md) | 4 | side (left) | Edge warning at t=155.5; the last leeches |
| 161 | 5 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 165 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 168 | 6 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 171 | 6 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | The last wave; the level ends at t=178 |

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 2 | [Rail Bunker](../../../enemies/ground/rail-bunker/README.md) on a rock face (t≈33.5), alone | Bounty 45; hardened (only `anti-ground` weapons and the specials hurt it); its first line taught with no air wave until t=37 |
| 3 | Rail Bunker pair on the ridge (t≈68), their lines crossing with a safe column between | Bounty 45 each; never more than 2 lines telegraphed at once (the bunker's rule) |
| 3 | Rail Bunker pair on the ridge (t≈86) | Bounty 45 each |
| 5 | Rail Bunker pair in the growth (t≈132) | Bounty 45 each |
| 5 | Rail Bunker, single (t≈150) | Bounty 45 |
| 4 | The prospector's shack *Claim 114* in the growth (`ground` trigger, 20 HP, t≈104, x≈380) | The secret: frees a crate worth 100 (655) |

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

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "The Leech Field, Lancer. Rail Bunkers on the rocks, Vrell in the growth. Cross it and keep your power up." |
| t=13 | Rook | "Rocks with teeth. Lovely. Keep your tail clear, Lancer." |
| t=23 | Varga | "Leeches, behind you! They drain the generator, not the shield. Jink left and right to throw them off." |
| t=34.5 | Okafor | "Rail Bunker on that rock. A red line means it has locked on. Step off the line." |
| t=46 | Varga | "Rear guns and side mounts shoot a latched leech off. The front guns can't reach it." |
| t=63.5 | Varga | "They're on the Talons too. The Vrell are feeding on their own allies." |
| t=71.5 | Okafor | "Some alliance. Don't count on it, Doctor. Keep firing, Lancer." |
| t=86.5 | Rook | "Two red lines. I'll take the space between them, thanks." |
| t=96 | Varga | "More Talons with leeches on them. They don't fight back. They just drift." |
| *Claim 114* destroyed (event) | Rook | "Claim 114. Someone staked this rock. The Vrell got here first." |
| t=118 | The Choir (distorted) | "…the one who opened… calls to us… we do not answer… the door is open… we are going…" |
| t=128 | Varga | "'The one who opened.' That's Vorne. And they don't care about him at all." |
| t=137 | Okafor | "Then he's alone in it. Good. Leeches from every side now, Lancer." |
| t=150 | Rook | "Lancer, you've got passengers. Shake 'em loose!" |
| t=164 | Okafor | "Clear rock ahead. The graveyard next, then the Sovereign." |
| Secondary met (at the end) | Okafor | "Field's clean. Not one leech left to follow us home." |

Timeline check (text): t=1 closes ≈ 11.2, t=13 ≈ 19.1, t=23 ≈ 32.9, t=34.5 ≈ 42.6, t=46 ≈ 54.4, t=63.5 ≈ 70.8,
t=71.5 ≈ 78.2, t=86.5 ≈ 92.7, t=96 ≈ 103.6, the Choir (subtitle ≈ 9.4 s, its layering on speech) ≈ 127.4, t=128
≈ 135.6, t=137 ≈ 143.9, t=150 ≈ 155.4, t=164 ≈ 170.2; nothing overlaps, and *Claim 114* is in reach at t≈101–107,
when the radio is free (103.6 to 118).

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
× the scale. At ≈ 110 a minute a **`bounty_scale` of 0.55** puts the typical haul at **6,099 (−0.0 %)**
(estimated by hand with the economy's shares: 60 % of the air kills, half the secret and the secondary, and the
hardened Rail Bunkers at **0 %**, below); `TypicalHaulTest` confirms it in the build.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Asteroid Mite 53 × 3 + Skitter 50 × 5 + Mote Swarm 61 × 2 + Stinger 20 × 15 + Talon 65 × 12 + leader 6 × 25 + Void Leech 63 × 12 | 9,072 | 5,443 |
| Ground targets: Rail Bunker 8 × 45 (hardened: 0 % in the typical haul) | 1,298 | 0 |
| Secret: *Claim 114* (hidden crate) | 655 | 328 |
| Secondary: purge the field | 655 | 328 |
| **Total** (bounty scale 0.55) | **11,680** | **6,099** |
| Budget(n) = the typical haul's target; typical −0.0 %, perfect 1.91 × budget | | 6,101 |

**The bunkers and the plan.** The Rail Bunkers are hardened and the hand plan's L33 fit (the Ion Beam L4, two
Micro-missile Pods, the Swarm Tail, the EMP Burst) has no `anti-ground` weapon, so the plan kills them only
with a special, so the typical haul counts them at **0 %** (decided) and the scale is set without them: 0.55
(it was 0.46 with the economy's 80 % ground share, 868 of the haul that the plan could not earn). The perfect run
rises to 1.91 × the budget (the act's others 1.48–1.66): a player who brings `anti-ground` takes the bunkers'
1,298 on top; part F checks it against the expert's bound with `levelReport`.

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

None yet. The level needs (part F's production art; the mood from the belt's round-60 mood stills):

- the field backdrop (six sections: the bare edge, the grown rocks, the ridge, the feeding ground, the nests,
  clear rock) and the big and small rocks with the growth; the shack *Claim 114*, the nav buoy;
- the Void Leech's latched poses on the Stormhawk and on a Talon (its own page), the prey Talon's drifting pose;
- the two briefing images; the intel portraits of the Void Leech and the Rail Bunker;
- the round's captures: the level at medium with the plan's fit and Rook; a ship with three leeches latched
  (the gauge, the dimmed ring, the flicker); a prey wing; the bunker pair's crossed lines; readability crops of a
  Talon over the growth at 1:1 (Q11).

## Implementation

- [ ] Level data `data.yaml` (when built in part F): sections and speeds with eases, waves, ground targets,
      radio, budget, threat profile, briefing, backdrop, music; vacuum (no weather)
- [ ] The generator drain in the level (part A's rules on a fixture first; flown and hashed here with the leeches)
- [ ] Void Leech waves: rear and side entries warned 3 s ahead, at most 6 free on the screen, the prey rule on
      the marked Talon wings
- [ ] Rail Bunkers on the asteroid surfaces (`ground`): never more than 2 lines telegraphed, a safe column
      between crossed lines
- [ ] Drifting rocks with the Asteroid Mites' riders
- [ ] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), the quiet windows, edge and BOTTOM warnings
- [ ] Secondary `escapes: void-leech` (48 of 63 at medium; the allowance per difficulty), judged as leeches leave
- [ ] Radio voiced, every timed line at most 1 s late, with Rook and without (`requires: escort`); the Choir's
      line with its layering
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel
- [ ] Briefing: both pages, each one screen, with their images
- [ ] Typical haul within ±5 % of budget(33) = 6,101 with the level's `bounty_scale`
- [ ] Music: track 13's base, then full from the Swarm, the −6 dB duck under the Choir, the motif muted; the `belt-open` ambience
- [ ] Easy/hard variations; the autopilot flies the level on every difficulty (with and without Rook) and shakes
      off leeches
- [ ] Production art and captures for the round

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
