---
title: Dust Kite
design: approved
implementation: in-progress
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../skitter, ../../../world/mars, ../../../campaign/act-3-red-dust]
updated: 2026-10-18
---

# Dust Kite

## Summary

Act 3's Martian popcorn (M10 = b, approved in round 36): a tiny dust-camouflaged Vrell
glider, a rust membrane kite with a teal-glowing spine, that shimmers out of the haze in streams
and V-wings and glides down in swaying arcs to ram the ship. One hit kills it. It keeps the act's
55 enemies per minute Martian, next to the returning Skitters, Needlers and Stingers (from
Level 15).

## Design

### Stat block

Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)). The numbers live in [data.yaml](data.yaml) (M7 part B, step B2), with the swoop's `trigger` and the hard hook `swoop_speed` of the [schemas](../../../tech/architecture/README.md#data-file-schemas); the table is rendered from it. HP is in damage units (a Pulse Cannon L1 shot
does 2). A `tiny` unit dies in one hit at any level and keeps its HP (the act HP factor is only
for `medium` and larger).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` |
| Size tier | `tiny` |
| Size | 24×24 px, hitbox 16×14 |
| Parts | single |
| Orientation | `16 angles` (it faces its glide; 4 membrane-flex frames) |
| HP | 1 (easy 1 / hard 1, from the global multipliers) |
| Armour / shield | none |
| Speed | 130 px/s gliding; 200 px/s in the final swoop |
| Movement | `sine` glide down the screen (amplitude 50 px, period 1.6 s); within 180 px above the ship's row it `swoop`s across the ship's lane on a shallow curve (aimed at the ship's x when it starts, no homing) and glides on off the bottom or a side; a stream's kites alternate sides; in a `swarm` the kites `flock` (separation 26 px, alignment, cohesion) round a leader point that flies the wave's authored route |
| Attack | `none` (contact `tiny` 6; destroyed by the impact) |
| Formations | stream, V-wing (4–7), snake (4–6), swarm (8–12) (Level 17's swarm bends round the Helix compound; Level 18's dust storm flies it too) |
| Weak points | teal spine glow (drawn only) |
| Effective traits | `spread`, `forward` |
| Credits | 4 (score 40 × chain) |
| Death | `tiny` puff of rust dust and a teal spark |
| First level / used in | L15; popcorn in every Act 3 level |
| Difficulty hooks | none beyond the global ones (formation size); hard: the swoop at 230 px/s |
<!-- /data -->

### Behaviour

- **Camouflage** (a look only): for its first 0.3 s on the screen it is drawn blended into the
  dust and fades to full contrast, a shimmer in the haze; it is hittable and collides from the
  first frame. In the Level 18 storm (M6 = a) its teal spine is what shows.
- **Glide and swoop.** It sways down on a `sine` glide; close to the ship's row it banks into one
  shallow swoop across the ship's lane (aimed at the ship's x when it starts, no homing), then
  glides on. A stream's kites swoop one after another, alternating sides.
- **Different from the Skitter.** The [Skitter](../skitter/README.md) darts straight in on its
  path; the Kite drifts and then cuts across, so dodging it is a sideways read rather than a
  head-on one. Not insectoid (a membrane glider), so it keeps the act's
  [variety checklist](../../README.md#variety-checklist-per-act) share down.
- **Colours** (role colours): rust base (fast attackers, divers) with a teal glow (contact),
  dusted ochre; the same pairing as the Lamprey and Ravager, told apart by size and shape.
- **Code fit.** `sine` and `swoop` exist; a swoop triggered by the ship's distance and the entry
  fade are the only additions.

### Difficulty and level use

- Popcorn: the levels use it in numbers for density (≥ 55 per minute); a single kite is never a
  wave's "problem" enemy.

## Concept art

Concept round 36 (Act 3 "Red Dust", [review board](../../../concept-rounds/round-36/README.md),
choice U.7): two looks, each a PNG sheet plus a GIF in the 480×540 play field over Mars; prompts
in [concept/prompts.md](../concept/prompts.md#dust-kite-r36-a), generator
`tools/concept/enemies_r36.py dust-kite`.

| File | What | Status |
|---|---|---|
| [../concept/rejected/dust-kite-r36-a.png](../concept/rejected/dust-kite-r36-a.png) | Dust Kite A, diamond kite — rust membrane on a bone cross strut, teal spine (sheet) | rejected — round 36: b (crescent sail) picked |
| [../concept/rejected/dust-kite-r36-a.gif](../concept/rejected/dust-kite-r36-a.gif) | Dust Kite A — stream, V-wing and swarm over Mars (motion) | rejected — round 36: b (crescent sail) picked |
| [../concept/dust-kite-r36-b.png](../concept/dust-kite-r36-b.png) | Dust Kite B, crescent sail — swept rust sail on bone finger struts (sheet) | chosen (round 36, U.7) |
| [../concept/dust-kite-r36-b.gif](../concept/dust-kite-r36-b.gif) | Dust Kite B — the same scene as A (motion) | chosen (round 36, U.7) |
| [../concept/dust-kite-final-r38-a.png](../concept/dust-kite-final-r38-a.png) | Dust Kite final sprites (`tools/art/dust_kite.py`): 16 headings × 4 membrane-flex frames, death spark and dust puff (sheet) | chosen (round 38, F.4: approved as final) |
| [../concept/dust-kite-final-r38-a.gif](../concept/dust-kite-final-r38-a.gif) | Dust Kite final: stream, V-wing and swarm from the production frames (motion) | chosen (round 38, F.4: approved as final) |

## Implementation

- [x] `sine` glide with the distance-triggered swoop across the ship's lane — M7 part B (sim step B1, `KiteSwoopTest`; step B3 maps the stat block and its hard hook, content's `MarsRules`, the bend of 60 px a default pending round 38, and flies a V-wing of gliders down the screen)
- [x] Entry fade (0.3 s, look only) — M7 part B (step B4: the game's `KiteFade`, from the step it first shows, tracked per unit; its shadow fades with it)
- [x] Contact `tiny` 6, destroyed on impact; death puff, bounty and score — M7 part B (the simulation's tier
  contact and bounty from the data file, steps B2/B3; the `dust-kite-death` frames, steps ART-B/B4; seen in round 38's
  capture)
- [x] Data file with the stat block (M7 part B, step B2; the simulation's mapping of its new keys in step B3)
- [x] Production art (round of part B): `tools/art/dust_kite.py`, review `dust-kite-final-r38-a` (pending round 38)

## Open questions

- None open: round 36 answered it (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): new unit for M10 = b (one new Mars popcorn flyer, the
  "dust-camouflaged flyer" of the [Mars natives](../../../world/mars/README.md)), taken as the
  recommended option, pending the user's review in round 36. Our proposals: the name, a membrane
  glider rather than an insect, 1 HP, bounty 4, the sine glide with one swoop, the entry fade as a
  look only.
- 2026-10-12 (overnight design phase): concept a/b proposed in round 36 (U.7: diamond kite vs
  crescent sail), pending the user's choice.
- 2026-10-13 (user, round 36): approved (D.17). M10 = b kept: the Dust Kite, with its name (Q.22i). U.7 = b: the crescent sail chosen, the diamond kite (a) rejected and moved to `../concept/rejected/`.
- 2026-10-14 (M7 part B, step ART-B): production art from the chosen round-36 look (b, crescent sail), review sheet and GIF `dust-kite-final-r38-a` proposed for round 38 (pending the user's review).
- 2026-10-14 (user, round 38): closed: the production art `dust-kite-final-r38-a` approved as final (F.4), so
  `art: final`; B.1 (b), the swoop bending 60 px across the ship's lane, and B.2 (g), the V-wing placing gliding
  units in their wing slots, kept as built.
- 2026-10-18 (M7 part D, step D2; pending the user's review in round 40): the `flock` moved forward from part E,
  since Level 17's Helix swarm of 8 needs it first: the Mote Swarm's weights (alignment 0.5, cohesion 0.3, leader
  0.6), its separation and reach scaled to the kite's 24 px (26 and 64 px), the glide's 130 px/s cruise, at most 12;
  `swarm` among its formations. Part E's dust storm (Level 18) reuses it.
