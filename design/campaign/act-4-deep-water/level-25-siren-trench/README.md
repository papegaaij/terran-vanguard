---
title: Level 25 – Siren Trench
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/naval/siren, ../../../enemies/naval/spiral-nautilus, ../../../enemies/naval/barbfin, ../../../enemies/naval/glassfin, ../../../enemies/naval/reef-spitter, ../../../world/europa, ../../../player/wingmen, ../../../player/systems, ../../../story/twist]
updated: 2026-10-22
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

**Status: approved in round 48 (2026-10-22); drafted (Act 4 design phase, 2026-10-21), decided in round 48 (2026-10-22).** Written
from the act roster, the gap analysis's recommended answers (Q1 b, Q4 a, Q5 a, Q6 a, Q7 a, Q8 a) and
the wave 1 documents; every number is a first draft for part A to measure with `levelReport`.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

> **Commander Okafor:** "Beyond the plain the sea floor splits into a trench, and the Vrell have
> grown nests in its walls. Fly it end to end. Don't touch the rock, Lancer."
>
> **Dr. Varga:** "The nests sing. The ring does no damage, but it slows you, and a slowed ship
> can't dodge. Slip through the gap between rings, or shoot the sac. Side and anti-sub guns."

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-25-siren-trench`: a sonar chart of the trench: the route down its middle, the
  nests marked on both walls, the side caves as notches, the red-gold beacons as dots.
- Varga's page `level-25-sonar-ring`: her diagram of the ring: the sac, the expanding band, the
  gap between two rings, the ship slowed inside the band (60 %, 2 s) and the 3 s immunity after it.

*Hangar teaser* (shop screen before L25; Rook, not voiced):

> **Rook:** "A trench full of singing walls. I'll hum along if it helps, Lancer."

## Threat profile

Draft; directions are counted by each wave's `from` (the intel's rule), the wall nests counting as
sides: 76 % front, 12 % cave entries, 12 % walls.

| Field | Value |
|---|---|
| Dominant layers | `air` (the play plane under water: Glassfin schools, Barbfin groups, Nautilus pairs, the wall nests' sacs), `ground` (Reef Spitters on the ledges at the walls' foot) |
| Attack directions | front 76% · sides 24% · rear 0% |
| Density | 4 |
| Recommended traits | `anti-sub`, `spread`, `homing` |
| Hazards | Trench walls (terrain collision); Siren rings that slow the ship (60 % for 2 s) |
| Boss / mid-boss | none |
| Sensor-suite detail | none: Europa, the trench, `air` + `ground`, front and sides · L1: + directions (all), density 4, hazards "walls" and "sonar slow", the objective `CROSS THE TRENCH` and the secondary `KILL RATIO 65 %` · L2: + Siren (new), Spiral Nautilus (new), Barbfin, Glassfin, Reef Spitter portraits, the `ring` slow marked as no damage · L3: + the side caves' entry points on the threat arrows |

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
Motion budget: the rings and the Nautilus spirals are the strong elements; the drift of the particles
is the second; everything else stays quiet.

| Section | t (s) | Scroll (px) | Speed (px/s) | Flyable width (px) | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Trench Mouth | 0–22 | 0–3,080 | 140 | 400 → 320 | `ground`: the plain's edge dropping into the trench, the first walls rising from t=8, three beacons on the left wall. `sub`: the abyss below, a faint glow. | Glassfin and Barbfin popcorn; the first Siren alone (t=18). |
| 2. The Walls | 22–60 | 3,080–8,400 | 140 | 320 → 260 | `ground`: ice-rock walls with caves notched in them every 700 px, alternating sides (the first at t=29), Reef Spitters on the ledges (t=30), beacons every 900 px. | Siren pairs and cave entries; the nests taught as a rhythm. |
| 3. Cave Mouths | 60–95 | 8,400–13,300 | 140 | 260 | As section 2; the survey sub wreck in the left cave (t≈78, the secret). | The Nautilus's first pair (t=62.5) with Sirens on top. |
| 4. The Narrows | 95–130 | 13,300–17,500 | 120 | 330 (t=95–105, the fork) → 190 (t=108–118) → 260 | `ground`: a rock pillar mid-trench at t=98–106 splitting it into two 140 px channels, then the walls close to 190 px (t=108–118), a Conamara survey buoy (t=118, the armour patch). | The tightest stretch; Sirens flank the fork; no Nautilus inside the 190 px stretch. |
| 5. The Song | 130–160 | 17,500–21,400 | 130 | 260 | As section 2, nests on both walls at their densest (up to four on the screen). | The Sirens, Nautilus pairs and Barbfin together. |
| 6. Trench Floor | 160–173 | 21,400–23,220 | 140 | 260 → 400 | `ground`: the walls falling away, the sea floor opening to flat black water; no more nests. | Release; level end; the lights ahead go out (Level 26). |

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
- **Code fit**: a wave entering from a point inside the wall line (the notch) is new
  ([gap note §2.4](../README.md)): a `SideEntry` or `edge` plus a spawn x at the notch.

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). All numbers are **draft**:
data in **Act 1 terms** with a `bounty_scale` (see *Credit budget*).

- **HP** (the units' own pages): the Barbfin 24 (12 ×2 beam era), the Siren 90 and the Nautilus
  100 (set at L25's reference DPS **108**, round 48's proposal), the Glassfin 1 (`tiny`), the Reef
  Spitter by its spec with the beam-era factor.
- **Density and pacing** (the Act 2+ rules, `PacingTest` and `ActFloorTest`): **280 enemies** (waves
  271 + ground targets 9) over 173 s, **≈ 97 a minute** at medium (easy ≈ 224, hard ≈ 308 by the
  formation lever), a wave about every 2.5 s, no stretch over 2 s with nothing on the screen (the
  schools and Barbfin groups stay on the screen for seconds). The enemy-shot rate comes mostly from the
  Barbfin groups (a group of 4 fires about 100 shots in 60 s) and the Nautilus spirals (48 shots
  an uncoil); bullets ×0.7 stay longer on the screen, so part A sets the fire rates with `levelReport`.
- **Quiet windows**: no other wave for 3.5 s after the first Siren (t=18) and after the first
  Nautilus pair (t=62.5).
- **The combination** is the design: a Siren pair at the same time as a Nautilus pair (t=138.5 and 152)
  puts a slowed ship against a spiral's gap, the act's hardest moment; Varga warns of it (t=131.5).
- **Edge warnings** 3 s ahead for every side wave's first entry (the cave entries).

Draft wave table (`from`: `wall` = a Siren nest on that wall, `side (… cave)` = out of a cave
notch at that wall):

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front |  |
| 8.5 | 1 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front |  |
| 11 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front (right) |  |
| 13.5 | 1 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front (left) |  |
| 16 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front |  |
| 18 | 1 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 1 | wall (right) | The first Siren, alone: one ring, quiet window of 3.5 s after it |
| 21.5 | 1 | V-wing | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front |  |
| 24 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front |  |
| 26.5 | 2 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (both, staggered) |  |
| 29 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | side (left cave) | First cave entry, warned 3 s ahead |
| 31.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front |  |
| 34 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 36.5 | 2 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (both, staggered) |  |
| 39 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | side (right cave) | Warned 3 s ahead |
| 41.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front |  |
| 44 | 2 | V-wing | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 46.5 | 2 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (both, staggered) |  |
| 49 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | side (left cave) |  |
| 51.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front |  |
| 54 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 56.5 | 2 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 3 | wall (both, staggered) |  |
| 58.5 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | side (right cave) |  |
| 62.5 | 3 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | The first pair, orbiting in opposite directions: quiet window of 3.5 s after it |
| 67 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front |  |
| 69.5 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (both, staggered) |  |
| 72 | 3 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | side (left cave) |  |
| 74.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front |  |
| 77 | 3 | V-wing | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 79.5 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (both, staggered) |  |
| 82 | 3 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front |  |
| 86 | 3 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | side (right cave) |  |
| 88.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front |  |
| 91 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 3 | wall (both, staggered) |  |
| 93 | 3 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 96 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front | In the narrows |
| 98.5 | 4 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (left and right, opposite) | Two rings cross the pillar fork |
| 101 | 4 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | side (left cave) |  |
| 103.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front |  |
| 106 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front | Closing into the narrows (190 px) |
| 110 | 4 | V-wing | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front |  |
| 112.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front |  |
| 115 | 4 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (both, staggered) |  |
| 117.5 | 4 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | side (right cave) |  |
| 120 | 4 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front |  |
| 124 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front |  |
| 126.5 | 4 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 129 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front |  |
| 131 | 5 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 3 | wall (both, staggered) | The Song begins |
| 133.5 | 5 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | side (left cave) |  |
| 136 | 5 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 9 | front |  |
| 138.5 | 5 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | A slowed ship on a spiral's gap: the act's hardest moment |
| 142 | 5 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 3 | wall (both, staggered) |  |
| 144.5 | 5 | V-wing | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 147 | 5 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front |  |
| 149.5 | 5 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | side (right cave) |  |
| 152 | 5 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 4 | wall (both, staggered) | The most nests on the screen: four |
| 154.5 | 5 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front |  |
| 157.5 | 5 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 4 | front |  |
| 161 | 6 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 7 | front | The trench widening |
| 164 | 6 | V-wing | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front |  |
| 167 | 6 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front |  |

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 2 | [Reef Spitter](../../../enemies/naval/reef-spitter/README.md) pair on the left ledge (t≈30) | Bounty 14 each; their fans ×0.7 |
| 3 | Reef Spitter pair on the right ledge (t≈70) | Bounty 14 each |
| 4 | Reef Spitter pair on the fork's pillar (t≈100), out of the way of the 190 px stretch | Bounty 14 each |
| 5 | Reef Spitter trio on the right ledge (t≈140) | Bounty 14 each |
| 3 | Conamara survey sub wreck in the left cave (`ground` trigger, 20 HP, t≈78) | The secret: frees a crate worth 100 (410) |

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
- **Overdrive**: dropped by the last Siren of the t=91 trio.
- **Shield cell**: dropped by the second Nautilus of the t=62.5 pair at its death.
- **Armour patch**: a Conamara survey buoy releases one at (240, 380 y-up) at t=118, at the narrows'
  end.

## Radio chatter

Spoken radio, draft text for the **1 s rule** (text at about 12 characters a second plus a 1.5 s
lead for the length check; 0.4 s of silence; no timed line more than 1 s late); event lines drop after
6 s. Rook's lines carry `requires: escort`; his one dry joke is at t=11.

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "The Siren Trench, Lancer. Vrell nests sing in the walls. Get through it and out the other side." |
| t=11 | Rook | "Water outside, singing inside. I'd like a refund." |
| t=19 | Varga | "A Siren. Its ring does no damage, it slows you. Slip through the gap, or shoot the sac." |
| t=33 | Varga | "The beacons on the walls again. Same carrier, same Helix handshake." |
| t=41 | Okafor | "Log it, Doctor. Later." |
| t=63.5 | Varga | "Nautilus. The shell is armour, the crown is the weak point. Shoot it when it opens." |
| survey sub destroyed (event) | Varga | "Conamara's survey sub. Someone was down here before the Vrell." |
| t=94 | Okafor | "The narrows, Lancer. Stay off the walls." |
| t=131.5 | Varga | "Sirens on both walls and Nautilus between. A slowed ship can't thread a spiral. Keep moving." |
| t=158 | Okafor | "The trench opens ahead. Almost through, Aegis." |
| t=164 | Okafor | "Out of the trench. Dark water next. Keep your light on." |
| Secondary met (at the end) | Okafor | "Clean crossing. The song is quiet." |

Timeline check (text, by the rule above): the longest line (t=131.5) closes at ≈ 140.7 and nothing
overlaps; Okafor's cut at t=41 follows Varga's beacon line (closes ≈ 40.1). **The beacon beat**: the
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
crate 100, the secondary 100), × the act factor **4.096** (1.6³), bounties × the scale. With the
waves at ≈ 97 a minute a **`bounty_scale` of about 0.48** puts the typical haul at **3,527 (−0.7 %)**
(estimated by hand with the Act 3 shares: typical kills 61.4 %, ground 80 %, the secret and secondary
50 %); `TypicalHaulTest` confirms it in the build.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Glassfin 151 × 2 + Barbfin 79 × 12 + Siren 31 × 28 + Spiral Nautilus 10 × 30 | 4,754 | 2,919 |
| Ground targets: Reef Spitter 9 × 14 | 248 | 198 |
| Secret: survey sub (hidden crate) | 410 | 205 |
| Secondary: clean crossing | 410 | 205 |
| **Total** (bounty scale 0.48) | **5,821** | **3,527** |
| Budget(n) = the typical haul's target; typical −0.7 %, perfect 1.64 × budget | | 3,551 |

**Balance plan**: the plan's L25 visit (the other pod's Micro-missiles L3, see the
[Act 4 hand plan](../../../player/generator/README.md#act-4-hand-plan-draft)); the level expects
`anti-sub` (Harpoon Torpedoes, Depth Charges) with `spread` for the schools, and is flyable without
it at the ×0.7 damage.

## Difficulty notes

- **Easy**: the Siren's slow 40 % for 1.5 s and a ring every 6 s; the Nautilus with 2 arms and a 1.8 s
  window; the Barbfin shot every 3 s; contact 6; the formation lever thins the schools.
- **Hard**: the ring every 3.6 s and 2 s of immunity; the Nautilus with 4 arms and a 1.4 s window; the
  Barbfin's 2-needle burst every 2.1 s; the schools larger and 40 % of a school dives.

## Concept art

None yet. The level needs (Act 4's production-art round; the mood from the Europa mood stills):

- the trench backdrop (six sections: mouth, walls, cave mouths, the narrows with the pillar, the Song,
  the floor), the ice-rock wall faces with their feet, the cave notches, the beacons' red-gold pulse;
- the Siren's wall nest and its ring, the Nautilus (round 06's chosen concept goes to production), the
  Barbfin and Glassfin, from the [naval](../../../enemies/naval/README.md) round 48 proposals;
- the survey sub wreck and the survey buoy props; the two briefing images; the intel portraits;
- the round's captures: the level at medium with the plan's fit, a ring slowing the ship, the combination
  at t=138.5, readability crops of the walls at 1:1.

## Implementation

- [ ] Level data `data.yaml` (when built in part A): sections and speeds with eases, the terrain block
      (wall polylines, the notches, the pillar island), waves, ground targets, radio, budget, threat
      profile, briefing, backdrop, music
- [ ] Under-water medium for the whole level (the factors of [europa](../../../world/europa/README.md#under-water-rules))
- [ ] Trench terrain: contact 10 and the push, the geometry rules (width at least 180 px, a channel at
      least 130 px) validated, no wave path or spawn in rock
- [ ] Side-cave entries: a wave spawning at a notch's back, warned 3 s ahead at the edge
- [ ] Siren nests on the walls (the ring, the slow status on the ship and Rook, the 3 s immunity, the
      shimmer) and the Nautilus's orbit clamped to the flyable width
- [ ] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), the quiet windows, edge warnings
- [ ] Kill-ratio secondary at 65 %, judged at the level end
- [ ] Radio voiced, every timed line at most 1 s late, with Rook and without (`requires: escort`)
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel
- [ ] Briefing: both pages, each one screen, with their images
- [ ] Typical haul within ±5 % of budget(25) = 3,551 with the level's `bounty_scale`
- [ ] Music: the B theme's stems by section; the `europa-trench` ambience (no bubbles)
- [ ] Easy/hard variations; the autopilot flies the level on every difficulty (with and without Rook)
- [ ] Production art and captures for the round

## Open questions

- **The Siren ring's numbers**: the Siren page says 120 px/s to a radius of 230 px; the Europa rules say
  about 140 px/s fading at about 420 px. This level uses the Siren page's; the two documents need
  one value (part A's measure).
- **The Siren's layer**: `air` (a wall mount on the play plane; our reading) or `ground`; the level
  counts it as a wave.
- **Do Rook's guns have a Siren answer?** He is slowed like the ship; no change drafted.

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
