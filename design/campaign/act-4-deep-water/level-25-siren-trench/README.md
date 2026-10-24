---
title: Level 25 – Siren Trench
design: approved
implementation: in-progress
art: proposed
depends-on: [../../../enemies/naval/siren, ../../../enemies/naval/spiral-nautilus, ../../../enemies/naval/barbfin, ../../../enemies/naval/glassfin, ../../../enemies/naval/reef-spitter, ../../../world/europa, ../../../player/wingmen, ../../../player/systems, ../../../story/twist]
updated: 2026-10-24
---

# Level 25 – Siren Trench

## Summary

A narrow trench cut into Europa's sea floor, its walls grown over with Vrell nests that sing. The
[Siren](../../../enemies/naval/siren/README.md)'s ring does no damage but slows the ship, and next to
the [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md)'s spirals and the
[Barbfin](../../../enemies/naval/barbfin/README.md)'s needles that is deadly: the level is built
around that combination. The act's terrain level (the Level 16 terrain rules under water, with
**side caves** that enemies swim out of) and its hardest stretch before the Maw: no boss, density 4,
at the Act 2+ floor (≈ 97 enemies a minute at medium). Red-gold human beacons pulse on the walls.
173 s: the trench mouth, the walls, the cave mouths, the narrows round a rock pillar, the Song and
the opening floor.

**Status: approved in round 48 (2026-10-22); in the build (M8 part E).** Drafted in the Act 4 design phase
(2026-10-21) from the act roster, the gap analysis's recommended answers (Q1 b, Q4 a, Q5 a, Q6 a, Q7 a, Q8 a) and the
wave 1 documents. The level's numbers live in its [data.yaml](data.yaml) (step E3) and the briefing, threat profile,
layout, terrain, waves, ground targets, radio and credit tables below are rendered from it; the counts are measured with
`levelReport` (step E3, tests in step E4).

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (approved in round 48):

<!-- data: briefing -->
> **Commander Okafor:** "Beyond the plain the sea floor splits into a trench, and the Vrell have
> grown nests in its walls. Fly it end to end. Don't touch the rock, Lancer."
>
> **Dr. Varga:** "The nests sing. The ring does no damage, but it slows you, and a slowed ship
> can't dodge. Slip through the gap between rings, or shoot the sac. Side and anti-sub guns."
<!-- /data -->

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-25-siren-trench`: a sonar chart of the trench: the route down its middle, the
  nests marked on both walls, the side caves as notches, the red-gold beacons as dots.
- Varga's page `level-25-sonar-ring`: her diagram of the ring: the sac, the expanding band, the
  gap between two rings, the ship slowed inside the band (60 %, 2 s) and the 3 s immunity after it.

*Hangar teaser* (shop screen before L25; Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "A trench full of singing walls. I'll hum along if it helps, Lancer."
<!-- /data -->

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule): the cave waves and the
walls' nests enter `from: sides` (the simulation brings the nests in with the ground at the front). At sensor L3 the
intel panel marks the cave entries on the side arrows (`Intel` `CAVE`, each cave wave's time; drawn in step E5).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (the play plane under water: Glassfin schools, Barbfin groups, Nautilus pairs, the wall nests' sacs), `ground` (Reef Spitters on the ledges at the walls' foot) |
| Attack directions | front 73% · sides 27% (the cave entries and the walls' nests count as sides) |
| Density | 4 |
| Recommended traits | `anti-sub`, `spread` |
| Hazards | Trench walls (terrain collision); Siren rings that slow the ship (60 % for 2 s) |
| Boss / mid-boss | none |
| Sensor-suite detail | none: Europa, the trench, `air` + `ground`, front and sides · L1: + directions (all), density 4, hazards "walls" and "sonar slow", the objective `CROSS THE TRENCH` and the secondary `KILL RATIO 65 %` · L2: + Siren (new), Spiral Nautilus (new), Barbfin, Glassfin, Reef Spitter portraits, the `ring` slow marked as no damage · L3: + the side caves' entry points on the threat arrows |
<!-- /data -->

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "A trench with singing walls. Narrow, and no way round." |
| L1 | "Nests on both walls and enemies out of the caves. Keep off the rock." |
| L2 | "New: Sirens slow you with sound. Nautilus spirals round you." |
| L3 | "Cave entries marked. Torpedoes and spread; keep moving." |

## Objective

- **Primary** `reach-end`: cross the trench to its open floor.
- **Secondary**: destroy at least **65 %** of all enemies (§1.7), judged at the level's end. Pays
  **100** credits in Act 1 terms (410 in Act 4); the typical haul counts 50 %.
- The trench is a lane: there is nothing to hold, only a gauntlet to cross; the secondary rewards
  shooting the nests before they sing.

## Layout

Scroll **140 px/s** in the open stretches, **120 px/s** in the narrows, **130 px/s** in the Song
(the water's drag makes the ship sluggish, so the scroll is slow for its width; the data eases
each change over 2 s). Setting: [Europa](../../../world/europa/README.md)'s trench, medium
`underwater` for the whole level (the descent's waterline was Level 23). **Light** (Q2 a): the trench
is dark but not black: the ship's headlight cone, the nests' teal sacs and the beacons' red-gold
pulse every ~4 s light it, with a dim blue ambient of about 14 %; Level 26 is the black level.
Since round 54's redo (F.1/F.2) the **headlights light the world**: inside the ship's and Rook's twin cones (240 px
ahead over a 50° sector, Rook's 80 % as long) the walls, cave mouths, floor and pillar come up nearly to their full
colour, with a soft falloff along and across the beams; outside them the dark ambient stays. A faint cool **rim light**
runs along the rock edges (the walls' foot, the cave notches' back and sides, the pillar's foot) and the walls' frosted
lip glows faintly, so the trench's shape reads outside the beams. The pillar's two ends are rounded (25 × 40 px
half-ellipses), not pointed.
Motion budget: the rings and the Nautilus spirals are the strong elements; the drift of the particles
is the second; everything else stays quiet.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Trench Mouth | 0–22 | 0–3,080 | 140 | clear | `ground`: the plain's edge dropping into the trench, the first walls rising from t=8, three beacons on the left wall. `sub`: the abyss below, a faint glow. | Glassfin and Barbfin popcorn; the first Siren alone (t=18). |
| 2. The Walls | 22–60 | 3,080–8,400 | 140 | clear | `ground`: ice-rock walls with caves notched in them every 700 px, alternating sides (the first at t=29), Reef Spitters on the ledges (t=30), beacons every 900 px. | Siren pairs and cave entries; the nests taught as a rhythm. |
| 3. Cave Mouths | 60–95 | 8,400–13,300 | 140 | clear | `ground`: As section 2; the survey sub wreck in the left cave (t≈78, the secret). | The Nautilus's first pair (t=62.5) with Sirens on top. |
| 4. The Narrows | 95–130 | 13,300–17,520 | 120 | clear | `ground`: a rock pillar mid-trench at t=98–106 splitting it into two 140 px channels, then the walls close to 190 px (t=108–118), a Conamara survey buoy (t=118, the armour patch). | The tightest stretch; Sirens flank the fork; no Nautilus inside the 190 px stretch. |
| 5. The Song | 130–160 | 17,520–21,410 | 130 | clear | `ground`: As section 2, nests on both walls at their densest (up to four on the screen). | The Sirens, Nautilus pairs and Barbfin together. |
| 6. Trench Floor | 160–173 | 21,410–23,220 | 140 | clear | `ground`: the walls falling away, the sea floor opening to flat black water; no more nests. | Release; level end; the lights ahead go out (Level 26). |
<!-- /data -->

### The trench (terrain under water)

The terrain rules of [Level 16](../../act-3-red-dust/level-16-valles-canyon-run/README.md#terrain)
carry over (wall polylines and islands as data, contact 10 and a 28 px push, a 0.5 s cooldown,
easy 6, the geometry rules: flyable width at least 180 px, a channel at least 130 px, a wall moving
at most 0.5 px sideways per px of scroll), with these Act 4 differences:

- **The walls are ice-rock and Vrell growth**: the collision line is the wall's foot; the nests, the
  ledges and the beacons stand on it (the Tendril Anchor's wall mounts are the model). Everything
  on the wall is drawn over its face; bullets are never hidden.
- **The caves** are notches in the wall polyline, 70 px deep and 90 px wide, with a floor and a back
  wall; a cave wave spawns **at the notch's back** (inside the opening, never in rock) and swims out;
  the entry is warned 3 s ahead at that side's edge (`WarningEdge`). The notch is flyable: the ship
  may enter it, but it is a dead end with the wall's contact on three sides.
- **Siren nests** are mounted on the wall's foot (a `turret nest` on the wall): at most four on the
  screen, at most one ring in flight per wall, the phases at least 1.5 s apart, never two nests of
  the same wall within 90 px (the Siren's rules). A nest scrolls through the screen in about 3.9 s
  at 140 px/s, so it sings once: first ring 1.2 s after it enters (the sac's tell begins 0.4 s after it enters),
  and shooting the sac first ends the song.
- **Enemies and the walls**: swimming enemies fly through the flyable width only; the Nautilus's orbit
  radius is clamped to the width (the 90 px floor stands); in the 190 px narrows no Nautilus is sent.
- **Rook** keeps 24 px off the walls and is slowed by the rings like the ship (his own 3 s immunity).
- **Code fit** (step E3, gap check `m8e-gaps.md` §5 E5): the caves are the terrain's `notches` (`{side, scroll, width,
  depth}`, not wall points: the 0.5 slope rule forbids the step), flyable dead ends with rock on three sides; the
  geometry rules (width, channels, slope) read the walls' lines without them, and Rook and the autopilot treat a notch as
  rock. A cave wave names its notch (`cave: <index>`, `from: sides` with the notch's wall as its `edge`); the content
  check asks the notch to be on the screen at the wave's time. The walls' nests are `wall` waves (`staggered`: a nest
  every 1.5 s, the walls taking turns from the right; `opposite`: a pair at one row, the left one's ring 1.5 s later);
  the check asks at most four nests on the screen, never two of one wall within 90 px, every two rings at least 1.5 s
  apart and at most one ring in flight per wall on every difficulty, and no nest on a notch's mouth or within a
  beacon's 100 px (`keep_clear`). The README's nest waves at t=26.5, 46.5, 56.5, 69.5, 91, 131 and 152 moved by
  0.3–0.5 s for that (26.8, 47, 56.2, 70, 91.3, 131.3, 152.3).

The walls' collision lines from the data (`terrain`), each point's row with the time it reaches the top edge (ART-E's
proposal for the backdrop, adopted unchanged in step E3):

<!-- data: terrain -->
| t at the top edge (scroll px) | Left x | Right x | Flyable width | Notes |
|---|---|---|---|---|
| 0–5 (0–700) | — | — | 480 | Level 24's plain at its edge, no walls |
| 5 (700) | 0 | 480 | 480 | The trench's mouth: the walls begin at the field's edges; wall height 0 |
| 9 (1,260) | 40 | 440 | 400 | wall height 0.7 |
| 12 (1,680) | 40 | 440 | 400 | The trench 400 px wide, the walls at full height; three beacons on the left wall |
| 20 (2,800) | 80 | 400 | 320 | The walls close to 320 px |
| 26 (3,640) | 80 | 400 | 320 | |
| 34 (4,760) | 110 | 370 | 260 | The walls at 260 px: the caves and the nests |
| 95 (13,300) | 110 | 370 | 260 | The fork opens round the rock pillar |
| 96.6 (13,510) | 75 | 405 | 330 | |
| 98 (13,680) | 75 | 405 | 165 + 165 | |
| 98 (13,684) | 75 | 405 | 154 + 154 | |
| 98.1 (13,694) | 75 | 405 | 146 + 146 | |
| 98.2 (13,706) | 75 | 405 | 142 + 142 | |
| 98.3 (13,720) | 75 | 405 | 140 + 140 | |
| 105.7 (14,600) | 75 | 405 | 140 + 140 | |
| 105.8 (14,614) | 75 | 405 | 142 + 142 | |
| 105.9 (14,626) | 75 | 405 | 146 + 146 | |
| 106 (14,636) | 75 | 405 | 154 + 154 | |
| 106 (14,640) | 75 | 405 | 165 + 165 | |
| 106.4 (14,690) | 75 | 405 | 330 | |
| 108.2 (14,900) | 145 | 335 | 190 | The narrows: 190 px |
| 117.8 (16,060) | 145 | 335 | 190 | |
| 119.6 (16,270) | 110 | 370 | 260 | 260 px again; the Song |
| 160 (21,410) | 110 | 370 | 260 | The floor opens |
| 164 (21,960) | 40 | 440 | 400 | wall height 0.6 |
| 169 (22,660) | 0 | 480 | 480 | The walls leave the field: flat black water; wall height 0 |

**Rock pillar** (`pillar`; x, scroll at the top edge): (240, 13,680) → (250.6, 13,684) → (259.2, 13,694) → (263.5, 13,706) → (265, 13,720) → (265, 14,600) → (263.5, 14,614) → (259.2, 14,626) → (250.6, 14,636) → (240, 14,640) → (229.4, 14,636) → (220.8, 14,626) → (216.5, 14,614) → (215, 14,600) → (215, 13,720) → (216.5, 13,706) → (220.8, 13,694) → (229.4, 13,684).

**Notches** (the side caves, 90 px along the wall, 70 deep; index, wall, t at the top edge of the middle (scroll px)): 0 right 25.1 (3,520); 1 left 27.9 (3,910); 2 right 37.9 (5,310); 3 left 40.2 (5,630); 4 right 45.4 (6,360); 5 left 47.9 (6,710); 6 right 57.4 (8,040); 7 left 60 (8,400); 8 right 66.6 (9,320); 9 left 70.9 (9,930); 10 right 75.8 (10,610); 11 left 78.1 (10,940); 12 right 84.9 (11,890); 13 left 88.7 (12,420); 14 right 93.9 (13,140); 15 left 99.8 (13,890); 16 left 110.7 (15,200); 17 right 116.2 (15,870); 18 left 121.6 (16,510); 19 right 127.4 (17,210); 20 left 132.4 (17,820); 21 right 138.1 (18,560); 22 left 142.7 (19,160); 23 right 148.4 (19,900); 24 left 153.1 (20,510).

**Contact**: 10 per touch (easy 6, hard 10), pushed 28 px along the wall's normal over 0.12 s, then a 0.5 s cooldown in which a touch only clamps.
<!-- /data -->

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). Measured (step E3, `levelReport -Plevel=25`, the plan's fit): 280 enemies at medium
(100 a minute), 3.6 on the screen and 21 shots a minute (the Act 2+ floor: 2.5 and 10), one stretch over 2 s
(t≈107.7–110.3, into the narrows; three allowed); easy 225 (2.3 on the screen), hard 335 (4.9). With step E4's trench
play: 3.5 on the screen, 20 shots a minute, two stretches over 2 s (t≈65–67 and the floor's last 4 s); hard 5.5.
**M8 AW** (step AW2, pending the user's review in round 56): the faster harpoon emptied the trench (1.7 on the screen,
3 shots a minute, the small units 0.68× Level 10's): the [Barbfin](../../../enemies/naval/barbfin/README.md) 26 HP (52
here), one more in every group from the front (a group of 5 held at 5 on hard: the trench's span), two more from every
cave (at most the column's 5) and a column of 3 down the narrows at t=107.5; `bounty_scale` 0.42. Measured: 314
enemies, 2.6 on the screen, 15 shots a minute, the small units 2.0 s (1.25×), three stretches over 2 s; won 3/3 on every
difficulty.

- **HP** (the units' own pages): the Barbfin 52 (26 ×2 beam era; M8 AW, 24 before), the Siren 90 and the Nautilus
  100 (set at L25's reference DPS **108**, round 48's proposal), the Glassfin 1 (`tiny`), the Reef
  Spitter by its spec with the beam-era factor.
- **Density and pacing** (the Act 2+ rules, `PacingTest` and `ActFloorTest`): **314 enemies** (waves
  305 + ground targets 9; M8 AW, 280 before) over 173 s, **109 a minute** at medium (easy 258, hard 362 by the
  formation lever), a wave about every 2.5 s, no stretch over 2 s with nothing on the screen (the
  schools and Barbfin groups stay on the screen for seconds). The enemy-shot rate comes mostly from the
  Barbfin groups (a group of 4 fires about 100 shots in 60 s) and the Nautilus spirals (48 shots
  an uncoil); bullets ×0.7 stay longer on the screen; measured at 21 a minute with the stat blocks' rates (step E3).
- **Quiet windows**: no other wave for 3.5 s after the first Siren (t=18) and after the first
  Nautilus pair (t=62.5).
- **The combination** is the design: a Siren pair at the same time as a Nautilus pair (t=138.5 and 152)
  puts a slowed ship against a spiral's gap, the act's hardest moment; Varga warns of it (t=131.5).
- **Edge warnings** 3 s ahead for every side wave's first entry (the cave entries).

The data's waves (step E3). Each school (`swarm`) flies an S-shaped route down the trench (x 190–290 in the 260 px
trench, x 210–270 in the narrows, down the fork's left channel at x 145–150 while the pillar is on the screen); the
Barbfins' groups from the front are laid out on the trench's span and hold above the ship; a cave wave (`side (… cave)`)
swims out of its notch at that wall, warned 3 s ahead at that edge; a Siren nest (`wall`) stands on a wall's foot,
staggered a nest every 1.5 s (both walls taking turns from the right) or a pair opposite; a Nautilus pair (`circle`)
orbits the ship for its 15 s hold, then breaks off the nearer side (gap check E7).

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front | |
| 8.5 | 1 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front | |
| 11 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front | In from the right |
| 13.5 | 1 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front (left) | |
| 16 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front | |
| 18 | 1 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 1 | wall (right) | **New.** The first Siren, alone: one ring, quiet window of 3.5 s after it |
| 21.5 | 1 | V-wing | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front | |
| 24 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front | |
| 26.8 | 2 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (both, staggered) | |
| 29 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | side (left cave) | First cave entry, warned 3 s ahead |
| 31.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front | |
| 34 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 36.5 | 2 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (both, staggered) | |
| 39 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | side (right cave) | Warned 3 s ahead |
| 41.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front | |
| 44 | 2 | V-wing | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 47 | 2 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (both, staggered) | |
| 49 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | side (left cave) | |
| 51.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front | |
| 54 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 56.2 | 2 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 3 | wall (both, staggered) | |
| 58.5 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | side (right cave) | |
| 62.5 | 3 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | **New.** The first pair, orbiting in opposite directions: quiet window of 3.5 s after it |
| 67 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front | |
| 70 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (both, staggered) | |
| 72 | 3 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | side (left cave) | |
| 74.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front | |
| 77 | 3 | V-wing | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 79.5 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (both, staggered) | |
| 82 | 3 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | |
| 86 | 3 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | side (right cave) | |
| 88.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front | |
| 91.3 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 3 | wall (both, staggered) | The last one drops an overdrive |
| 93 | 3 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 96 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front | Into the fork, ahead of the pillar |
| 98.5 | 4 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (left and right, opposite) | Two rings cross the pillar fork |
| 101 | 4 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | side (left cave) | |
| 103.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front | Down the fork's left channel |
| 106 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front | Closing into the narrows (190 px), down the left channel past the pillar's end |
| 107.5 | 4 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | M8 AW: down the narrows ahead of the V (step AW2) |
| 110 | 4 | V-wing | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front | |
| 112.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front | In the narrows |
| 115 | 4 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (both, staggered) | |
| 117.5 | 4 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | side (right cave) | |
| 120 | 4 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | |
| 124 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front | |
| 126.5 | 4 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 129 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front | |
| 131.3 | 5 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 3 | wall (both, staggered) | The Song begins |
| 133.5 | 5 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | side (left cave) | |
| 136 | 5 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front | |
| 138.5 | 5 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | A slowed ship on a spiral's gap: the act's hardest moment |
| 142 | 5 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 3 | wall (both, staggered) | |
| 144.5 | 5 | V-wing | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 147 | 5 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front | |
| 149.5 | 5 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | side (right cave) | |
| 152.3 | 5 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 4 | wall (both, staggered) | The most nests on the screen: four |
| 154.5 | 5 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | |
| 157.5 | 5 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 5 | front | |
| 161 | 6 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front | The trench widening |
| 164 | 6 | V-wing | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front | |
| 167 | 6 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front | The last wave |

Totals: Glassfin 151 · Barbfin 113 · Siren 31 · Spiral Nautilus 10.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 2 | [Reef Spitter](../../../enemies/naval/reef-spitter/README.md) pair on the left ledge (t≈30) | Bounty 14 each; their fans × 0.7 |
| 3 | [Reef Spitter](../../../enemies/naval/reef-spitter/README.md) pair on the right ledge (t≈73) | Bounty 14 each |
| 3 | Conamara survey sub wreck in the left cave (`ground` trigger, 20 HP, t≈78) | The secret: frees a crate worth 100 (410) |
| 4 | [Reef Spitter](../../../enemies/naval/reef-spitter/README.md) pair on the fork's pillar (t≈100) | Bounty 14 each |
| 5 | [Reef Spitter](../../../enemies/naval/reef-spitter/README.md) trio on the right ledge (t≈140) | Bounty 14 each |
<!-- /data -->

The Reef Spitters stand on ledges in the 24 px foot band of a wall or the pillar (gap check E6; the content check asks it
in a trench), at ART-E's spots (the right pair at t≈73, not the draft's 70); the survey sub is a trigger inside the left
wall's notch 11 (its back at x 40), where the ship may follow it at the walls' contact on three sides.

## Hazards

- **Walls**: contact 10 and a 28 px push (easy 6); the narrows are 190 px wide at their tightest.
- **The Siren ring**: no damage, 60 % speed and acceleration for 2 s, 3 s immunity
  (easy 40 % for 1.5 s; hard 2 s immunity); a Dash passes through it ([europa](../../../world/europa/README.md#under-water-rules)).
- **Water** (Q1 b, [under water rules](../../../world/europa/README.md#under-water-rules)): bullets ×0.7, acceleration ×0.6, torpedoes +50 %, non-`anti-sub` damage ×0.7; the pressure hull removes the drag.
- **Special limits**: no Airstrike or Orbital Lance; the Sonar Pulse works (full damage for 6 s).

## Secrets and pickups

- **Conamara survey sub wreck** (the secret, left cave, t≈78): a small research sub that went missing
  before the Vrell, nose-down in the cave mouth, its lamp dead. A `ground` trigger of **20 HP** inside
  the notch (the ship may enter the notch to reach it; the walls' contact on three sides is the cost);
  destroyed, its cargo pod floats up as a crate worth **100** in Act 1 terms (410). The Salvage
  scanner's glint shows it at any range. Its event line, Varga: "*Conamara's survey sub. Someone was
  down here before the Vrell.*"
- **Overdrive**: dropped by the last Siren of the t=91.3 trio (`dropped_by: {wave: 91.3, enemy: siren, unit: last}`).
- **Shield cell**: dropped by the second Nautilus of the t=62.5 pair at its death.
- **Armour patch**: a Conamara survey buoy releases one at (240, 380 y-up) at t=118, at the narrows'
  end (a `drops:` row with the `survey-buoy` look, gap check E12).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)) at the README's times, checked against the HUD's **1 s rule**
(no timed line more than 1 s late, with Rook and without) by the game's radio timeline test once the lines are voiced
(step VOX-E; the runs for `RadioTimelineLevel25Test` are written in step E3 for step E5). Event lines drop after 6 s.
Rook's lines carry `requires: escort`; his one dry joke is at t=13. The secret's line (the survey sub destroyed): Varga,
"Conamara's survey sub. Someone was down here before the Vrell."

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "The Siren Trench, Lancer. Vrell nests sing in the walls. Get through it and out the other side." |
| t=13 | Rook | "Water outside, singing inside. I'd like a refund." |
| t=20 | Varga | "A Siren. Its ring does no damage, it slows you. Slip through the gap, or shoot the sac." |
| t=33 | Varga | "The beacons on the walls again. Same carrier, same Helix handshake." |
| t=44 | Okafor | "Log it, Doctor. Later." |
| t=63.5 | Varga | "Nautilus. The shell is armour, the crown is the weak point. Shoot it when it opens." |
| t=94 | Okafor | "The narrows, Lancer. Stay off the walls." |
| t=131.5 | Varga | "Sirens on both walls and Nautilus between. A slowed ship can't thread a spiral. Keep moving." |
| t=158 | Okafor | "The trench opens ahead. Almost through, Aegis." |
| t=164 | Okafor | "Out of the trench. Dark water next. Keep your light on." |
| Secondary met (at the end) | Okafor | "Clean crossing. The song is quiet." |
<!-- /data -->

Retimed in step E3 to the HUD's real hold (the radio timeline test with the lines as text, with and without Rook):
Okafor's opening holds the radio until t≈12.6, so Rook's joke moves from t=11 to t=13 and, with him on the wing,
Varga's Siren line from t=19 to t=20 (the first ring at t≈19.2); Varga's beacon line holds it until t≈43.7, so
Okafor's reply moves from t=41 to t=44 (accepted in round 54 (2026-10-23); voiced since step VOX-E, every timed line at most
1 s late: `RadioTimelineLevel25Test`). **The beacon beat**: the
red-gold relays on the walls are Act 4's visual hint (human-built, Helix frequencies); this level's
line is Varga's reaction, not a new finding (hint 3 is Level 27). Okafor's reply keeps the private-
channel refrain of Level 17/19 in a lighter form ("Log it, Doctor. Later.").

## Music & ambience

[Track 11](../../../audio/music/README.md#track-list), the Act 4 B theme (muffled ambient-dnb, in the
track list without a chosen file): the A theme's sonar-and-pad intro in section 1, the **B base from
section 2**, the Sirens' song as a long harmonic drone layer that swells with each ring, the **full mix
in section 5** (the Song), out over the floor into silence (the lights go out). No bubble layer
(round 48 Q12 a). Ambience: the `europa-trench` bed (a low rumble of a deep cut, distant whale calls,
no bubbles) throughout. New sounds this level needs: the Siren's sac tell and its ring (a soft heavy
low sweep, no bubbles), the slow's falling tone and the hull shimmer, the Nautilus's shell spin, its
uncoil and its spiral shots, the Barbfin's needle, the Glassfin shatter, the wall scrape; listed in
[sfx](../../../audio/sfx/README.md) by the act's audio step (Freesound for real-world sounds only).

## Credit budget

Budget(25) = 700 × 1.07²⁴ ≈ **3,551** ([economy](../../../systems/economy/README.md#per-level-budget)).
Data in **Act 1 terms** (Glassfin 2, Barbfin 12, Siren 28, Spiral Nautilus 30, Reef Spitter 14, the
crate 100, the secondary 100), × the act factor **4.096** (1.6³), bounties × the scale. A **`bounty_scale` of 0.42**
(M8 AW, step AW2: refitted for the extra Barbfins; the README's estimate 0.48 before) puts the typical haul at 3,483,
**−1.9 %** of budget(25) (`levelReport -Plevel=25`; 0.43 would hit it; within the ±5 % of `TypicalHaulTest`).

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Glassfin 151 × 2 + Barbfin 113 × 12 + Siren 31 × 28 + Spiral Nautilus 10 × 30 | 4,834 | 2,900 |
| Ground targets: Reef Spitter 9 × 14 | 216 | 173 |
| Secret: survey sub (hidden crate, 3% of budget) | 410 | 205 |
| Secondary: clean crossing | 410 | 205 |
| **Total** (bounty scale 0.42) | **5,870** | **3,483** |
| Budget(n) = the typical haul's target; typical -2 %, perfect 1.65 × budget | | 3,551 |
<!-- /data -->

**Balance plan**: the plan's L25 visit (the other pod's Micro-missiles L3, see the
[Act 4 hand plan](../../../player/generator/README.md#act-4-hand-plan-draft)); the level expects
`anti-sub` (Harpoon Torpedoes, Depth Charges) with `spread` for the schools, and is flyable without
it at the ×0.7 damage.

## Difficulty notes

- **Easy**: the Siren's slow 40 % for 1.5 s and a ring every 6 s; the Nautilus with 2 arms and a 1.8 s
  window; the Barbfin shot every 3 s; contact 6; the formation lever thins the schools.
- **Hard**: the ring every 3.6 s and 2 s of immunity; the Nautilus with 4 arms and a 1.4 s window; the
  Barbfin's 2-needle burst every 2.6 s (its stat block); the schools larger and 40 % of a school dives.

## Concept art

The production art of M8 part E (step ART-E) and the game captures (step E6) for [round
54](../../../concept-rounds/round-54/README.md); briefs in [concept/prompts.md](concept/prompts.md). The captures are game
recordings (Level 25 on medium, `--invulnerable`, the balance plan's Level 25 fit: Harpoon Torpedoes L3, Depth Charges L3, two Micro-Missile Pods L3, the Pressure hull, the Sonar Pulse, Rook along; Xvfb, the software renderer); each PNG is frames of one run side by side, the level time in each
row's text.

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r54-a.png](concept/backdrop-final-r54-a.png) | Production backdrop (`tools/art/backdrop_l25.py`, from europa-siren-trench-r48-a): the trench's ice-rock walls with Vrell growth and glowing nodules, the side caves, the rock pillar at the fork, the narrows; the plain's lip at the start, four floor tile sets (rubble, scour, root mats, the open floor), ledges under the Reef Spitters, eighteen relay beacons at the walls' foot (4 s pulse); ten screens under the proposed ambient 0.14, the walls projected as the game does (sheet) | redo (round 54, F.1/F.2: the user asked for a redo, shown again in round 55; the file stays until then) |
| [concept/backdrop-final-r54-a.gif](concept/backdrop-final-r54-a.gif) | t 96–104 at half size: the fork round the pillar | redo (round 54, F.1/F.2: the user asked for a redo, shown again in round 55; the file stays until then) |
| [concept/backdrop-final-r55-a.png](concept/backdrop-final-r55-a.png) | The backdrop again after round 54's redo (step LX, the same sheet as r54's): the pillar's ends rounded, the walls' frosted lip with a faint rim glow (`ice-wall-glow`); the headlights and the rim light are the game's (see the r55 captures), the review grade shows the ambient only | proposed (round 55) |
| [concept/backdrop-final-r55-a.gif](concept/backdrop-final-r55-a.gif) | t 96–104 at half size: the fork round the rounded pillar | proposed (round 55) |
| [concept/walls-capture-r55-a.png](concept/walls-capture-r55-a.png) | Game capture (step LX; Level 25 on medium, `--invulnerable`, the default loadout, Rook along with an autocannon; Xvfb, the software renderer; the field at 1:1, t≈17, 37, 57, 77): the dark trench, the ship's and Rook's headlights lighting the floor and the walls ahead, the rim light on the walls' foot and the lip's glow | proposed (round 55) |
| [concept/cave-entry-capture-r55-a.png](concept/cave-entry-capture-r55-a.png) | The same run (t≈25, 27, 28, 29): cave mouths in both walls with their rock edges in the rim light, the t=29 cave wave warned on the left | proposed (round 55) |
| [concept/fork-capture-r55-a.png](concept/fork-capture-r55-a.png) | The same run (t≈98–101): the pillar's rounded head coming down the trench and into the headlights | proposed (round 55) |
| [concept/narrows-capture-r55-a.png](concept/narrows-capture-r55-a.png) | The same run (t≈107, 110, 113, 116): the pillar's rounded tail, the narrows' walls (190 px) in the cones and the rim | proposed (round 55) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block and the `terrain:` block (walls, pillar, 25 notches) the art is drawn for | redo (round 54, F.1/F.2: the user asked for a redo, shown again in round 55; the file stays until then) |
| [concept/l25-props-final-r54-a.png](concept/l25-props-final-r54-a.png) | The level's props (`tools/art/l25_props.py`): Conamara's survey sub nose-down in its cave (lying, hit, opened; its dead lamp's pinger, sparks), the survey buoy (shut, opening, open; its strobe) (sheet) | chosen (round 54, F.5: accepted as final) |
| [concept/l25-props-final-r54-a.gif](concept/l25-props-final-r54-a.gif) | The sub shot open in its cave and its crate rising; the buoy releasing the armour patch at the narrows' end | chosen (round 54, F.5: accepted as final) |
| [concept/cave-entry-capture-r54-a.png](concept/cave-entry-capture-r54-a.png) | Game capture, M8 part E (step E6; Level 25 on medium, `--invulnerable`, the balance plan's Level 25 fit: Harpoon Torpedoes L3, Depth Charges L3, two Micro-Missile Pods L3, the Pressure hull, the Sonar Pulse, Rook along): the first cave wave (t=27 the edge warning on the left, t=29.4–30.8 three Barbfins swimming out of the left wall's cave mouth while a Siren's ring crosses) | redo (round 54, F.1/F.2: the user asked for a redo, shown again in round 55; the file stays until then) |
| [concept/fork-capture-r54-a.png](concept/fork-capture-r54-a.png) | The fork (t=99, 102, 105, 108): the rock pillar splitting the trench into two 140 px channels, its Reef Spitters, the ship in the right channel | redo (round 54, F.1/F.2: the user asked for a redo, shown again in round 55; the file stays until then) |
| [concept/narrows-capture-r54-a.png](concept/narrows-capture-r54-a.png) | The narrows (t=109.5, 111, 114, 117): the walls at 190 px, no Nautilus, a cave wave warned on the right | redo (round 54, F.1/F.2: the user asked for a redo, shown again in round 55; the file stays until then) |
| [concept/siren-nautilus-capture-r54-a.png](concept/siren-nautilus-capture-r54-a.png) | The t=138.5 combination (t=141.5, 144, 146, 147): a Nautilus pair's spirals with the t=142 nests' rings, the ship and Rook slowed (their shimmer) | chosen (round 54, F.3/F.4: accepted as final; the trench round it is redrawn in round 55) |
| [concept/walls-capture-r54-a.png](concept/walls-capture-r54-a.png) | Wall crops at 1:1 (top) and the same lifted (gamma 0.5, bottom, to show what the grade hides): the left wall with a cave mouth (t=40.5), a Siren singing on the right wall (t=95.6), the pillar's tip (t=103), the narrows' wall (t=112) | redo (round 54, F.1/F.2: the user asked for a redo, shown again in round 55; the file stays until then) |
| [concept/survey-sub-capture-r54-a.png](concept/survey-sub-capture-r54-a.png) | The secret at 1:1 (a second run, the ship steered to the left wall at t=76.3): the survey sub in its cave (t=79.5, 80.5), hit (t=81), its crate taken (+410, t=82) | chosen (round 54, F.5: accepted as final) |
| [concept/survey-buoy-capture-r54-a.png](concept/survey-buoy-capture-r54-a.png) | The survey buoy at 1:1 (t=117.5–119.5): moored and shut, opening, the armour patch released and drifting | chosen (round 54, F.5: accepted as final) |

## Implementation

- [x] Level data `data.yaml` (step E3): sections and speeds with eases, the terrain block
      (wall polylines, the notches, the pillar island), waves, ground targets, radio, budget, threat
      profile, briefing, backdrop, music
- [x] Under-water medium for the whole level (the factors of [europa](../../../world/europa/README.md#under-water-rules))
      (`Level25Test`, step E4)
- [x] Trench terrain: contact 10 and the push, the geometry rules (width at least 180 px, a channel at
      least 130 px) validated, no wave path or spawn in rock (`TrenchChecks`; `Level25Test.theTrenchsWallsPillarAndNotches`,
      `noAirUnitFliesInRock` on every difficulty since step E5 clamped the hover drift to the flyable span)
- [x] Side-cave entries: a wave spawning at a notch's back, warned 3 s ahead at the edge (`CaveEntryTest`,
      `Level25Test`)
- [x] Siren nests on the walls (the ring, the slow status on the ship and Rook, the 3 s immunity, the
      shimmer) and the Nautilus's orbit clamped to the flyable width (`SirenTest`, `SpiralNautilusTest`,
      `Level25Test.theRingsSlowTheShipAndRook`, `theNautilusPairsCoilOutsideTheNarrows`; drawn: `Level25LooksTest`)
- [x] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`, hard included since step E4), the quiet windows,
      edge warnings (`Level25Test`)
- [x] Kill-ratio secondary at 65 %, judged at the level end (`Level25Test`: met on every seed by the autopilot)
- [x] Radio voiced, every timed line at most 1 s late, with Rook and without (`requires: escort`)
      (`RadioTimelineLevel25Test`, `VoiceFilesTest`; step VOX-E)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`,
      the cave entries on the L3 strip: `level25sStripMarksTheCaveEntries`)
- [x] Briefing: both pages, each one screen, with their images (`BriefingsTest`, `BriefingLayoutTest`)
- [x] Typical haul within ±5 % of budget(25) = 3,551 with the level's `bounty_scale` (`TypicalHaulTest`; the
      sources by hand in `Level25Test`)
- [x] Music: the B theme's stems by section; the `europa-trench` ambience (no bubbles)
      (`Level25SoundsTest.theMusicIsPressureLineOverTheTrenchsBed`; the bed: round 54's a)
- [x] Easy/hard variations; the autopilot flies the level on every difficulty (with and without Rook; `Level25Test`,
      `SirenPlay`, step E4)
- [x] Production art and captures for the round (steps ART-E and E6; [round 54](../../../concept-rounds/round-54/README.md))
- [x] Round 54's F.1/F.2 redo (step LX): the headlights light the world (`underwater_look.headlights: {strength, reach,
      angle}`, `UnderwaterGrade`), the rim light along the rock edges (`underwater_look.rim`) and the lip's glow, the
      pillar's ends rounded (`Level25Test` green, the flyable span unchanged); captures for round 55

## Decisions

- 2026-10-21 (Act 4 design phase, step m8-dl2526): drafted from the roster row, the gap analysis and the
  wave 1 documents; **decided in round 48 (2026-10-22)**: the recommended options Q1 b, Q4 a (not
  used here), Q5 a (a visible ring, 60 % for 2 s, 3 s immunity), Q7 a (Rook is still with the wing).
- 2026-10-21: the level meets the Act 2+ floor from the start (≈ 97 a minute, a wave every 2.5 s) with
  the beam era's HP on the small units; the Siren sings once a pass at the wall's scroll speed.
- 2026-10-21: caves are notches in the wall polyline (spawn at the notch's back); no Nautilus in the
  190 px narrows; no mid-boss (the campaign's pacing rule).
- 2026-10-21: the secret is the Conamara survey sub (ties L22's Conamara); the beacon beat in this
  level is Varga's reaction only, hint 3 stays Level 27.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-23 (step E3, gap check `m8e-gaps.md` §5, accepted in round 54 (2026-10-23)): the level as data; the open
  questions close: the ring's numbers are the Siren page's, 120 px/s to 230 px, not × 0.7 (E2; the Europa page defers to
  it); the nest is `air`, mounted on its wall's foot and counted as a wave, 271 + 9 (E1); Rook has no Siren answer, as
  drafted (E13). The caves are the terrain's `notches` (E5), ART-E's terrain proposal adopted unchanged; the pillar's
  Reef Spitters stand in the island's foot band (E6); the recommended traits are `anti-sub` and `spread` (the draft's
  `homing` dropped: three do not fit the intel panel at L3, and Varga's L3 line names torpedoes and spread); a Nautilus pair holds 15 s (E7) and none holds while the narrows
  are at the top edge (E9); the survey sub is a trigger inside the left notch, the armour patch a drop with the
  `survey-buoy` look (E12). The nest waves at t=26.5, 46.5, 56.5, 69.5, 91, 131 and 152 moved to 26.8, 47, 56.2, 70,
  91.3, 131.3 and 152.3 so no nest stands on a notch's mouth or within a beacon's 100 px on any difficulty (the content
  check). Measured: 280 enemies, 100 a minute, the floor met (3.6 on the screen, 21 shots a minute), the typical haul
  −1.7 % at `bounty_scale` 0.48.
- 2026-10-23 (step E4, accepted in round 54 (2026-10-23)): the tests (`Level25Test`) and the autopilot's trench play
  (`SirenPlay`); no number of the level changed. On hard the autopilot died at t≈100–136 (0/3): most of its armour went
  on the nests' bodies (it lined up under a nest at the wall and stayed there as the nest scrolled onto it), the rest on
  needles while slowed and on the pillar's tip. Now a nest is rock to its hull while it scrolls past (0.9 s ahead, longer
  while slowed), the trench's rock is looked at 1.2 s ahead (the drag; the slow's share longer while slowed), and it
  sidesteps bullets from further up while slowed. Shooting the sacs first was tried and dropped: the small units lived
  longer (1.73 × Level 10's time at medium, over the 1.5) and hard lost more. Measured (`levelReport -Plevel=25`, the
  plan's fit with Rook, seeds 1–3): won 3/3 on every difficulty without a retry, the kill ratio met 3/3 (8 seeds: 8/8;
  hard three retries); medium 3.5 on the screen, 20 shots a minute, two stretches over 2 s (t≈65–67 after the first
  pair's quiet window, and the floor's last 4 s; the t≈108–110 stretch into the narrows is gone), the small units 1.27 ×
  Level 10's time. `PacingTest` flies hard again. Found, for a simulation step: a hovering unit's drift is not clamped
  to the trench: on hard the outer Barbfin of a five-wide group (the t=44 and t=77 V-wings, the t=54 line; laid out to
  the span's edge) drifts up to 6 px past a wall's line, its centre at the wall's foot (`Level25Test` prints it and
  fails anything deeper or any other unit in rock).
- 2026-10-23 (steps E5, ART-E and E6, accepted in round 54 (2026-10-23)): Level 25 on the screen and in the
  speakers: the cave mouths drawn at every notch after the walls, the Siren's steps, ring (a wrapped strip, its lines
  stopped by rock) and the slowed hulls' shimmer, the Nautilus rolling, uncoiling and splitting, the survey sub in its
  cave and the buoy moored at its drop (`Level25LooksTest`, `Level25SoundsTest`); a hovering unit's drift is clamped to
  the flyable span (`noAirUnitFliesInRock` asserts every difficulty); ART-E's judgement calls: the notch layout (25,
  14 fillers), 18 beacons (none in the fork and the narrows), the pillar's shape, ambient 0.14 (0.10 in the last
  section), the ring as a wrapped strip, the survey sub's pose and the buoy's look. The frame rate of a full medium run
  on the software renderer (Xvfb, llvmpipe, no capture): **190 fps** on average (Level 24: 419), the longest frame
  73–90 ms in three runs, always the level's first frame (0.1 s in: the first-time loads), no frame of 50 ms or more
  after it (the bench report now names the moment of its longest frame and lists the frames of 50 ms or more).
- 2026-10-23 (user, round 54, closed): the art F.3–F.7 accepted as final (the Siren, the Spiral Nautilus, the props, the
  briefing images, the intel); **F.1/F.2 redo**: the backdrop is too dark ("on the in-game screenshots you can hardly see
  anything, let alone make out the edges of the cave"), the fork's start too sharp (it needs a rounded head); the trench
  is meant to be dark, but the brighter variant is too bright and the headlights' cones are not visible: the dark ambient
  stays, the ship's headlights light the world, the walls' edges and cave mouths get a faint rim light, the pillar's head
  is rounded (built in step LX, shown again in round 55; `backdrop-final-r54-a` and the r54 captures stay until then).
  Sounds: the Siren's ring a, slow **b**, death a; the shell spin a, uncoil a, the Nautilus's death **b**; the bed a.
  C.1–C.8 accepted (the 90 px floor, armoured while it opens, every anti-sub weapon cracks the shell, a Dash is not slowed,
  a kill ends its ring, the nests nudged, the radio retimed, the intel without `homing`).
- 2026-10-23 (step LX, round 54's F.1/F.2 redo, pending the user's review in round 55): the dark ambient (0.14, 0.10 over
  the floor) kept; the headlights are real light now: each lamp's cone adds the light still missing to full (slightly
  warm) times the look's strength, so the world inside comes up nearly to its own colour, full to 30 % of the reach,
  then fading to its end, and soft over the outer 45 % of its half-angle; each lamp's cone spans 84 % of the sector, toed
  out by 8 %, so the two overlap ahead. Level 25: strength 0.95, reach 240 px, a 50° sector; Rook flies the same beams at
  80 % of the reach. The data's `headlights` takes a strength alone (the old twin cones' 200 px and 44°: Levels 23 and
  24 keep their numbers but now light the world too, their cones a little brighter) or `{strength, reach, angle}`, so
  Level 26's hard cone (70°, 260 px) can reuse it. The rim (`rim: 0.5`): a cool band 32 px into the rock and 6 px into the
  water along the walls' foot, the notches' back and both their edges across the wall and the pillar's foot; the walls'
  frosted lip glows faintly in `ice-wall-glow` (22 %, additive, ungraded). The pillar's ends are 25 × 40 px
  half-ellipses (both ends, the head and the tail; the channels stay 140 px, `Level25Test` unchanged). Frame rate of a
  full medium run with Rook on the software renderer (Xvfb, no capture, the machine under other load): 142 fps against
  144 for the round-54 build in the same conditions back to back; one frame of 50 ms or more after the first (78 ms).
- 2026-10-24 (M8 AW, step AW2; pending the user's review in round 56): the harpoon's rework emptied the trench
  (`ActFloorTest` 1.7 on the screen and 3 shots a minute, `BeamEraHpTest` 0.68×, `PacingTest` red on easy and medium): the
  Barbfin's HP 12 → 26 (its stat block, with Level 24), one more Barbfin in every group from the front (`hard: {count}`
  holds a group of 5 at 5: a 7-wide group stood in the rock), two more from every cave (at most the column's 5), a column
  of 3 at t=107.5 (the narrows' gap), `bounty_scale` 0.48 → 0.42. **Open**: `Level25Test.noAirUnitFliesInRock` is red: a
  Barbfin that outlives its 9 s hold (a cave's, out of the harpoon's ±15° cone at the wall) and a Spiral Nautilus that
  outlives its orbit leave down their path through the trench's lower walls (the simulation keeps only the hold's drift
  off the rock); with the original waves and the 26 HP a Nautilus and two Barbfins already do. A simulation fix
  (the leave kept to the flyable span) is a step of its own.
