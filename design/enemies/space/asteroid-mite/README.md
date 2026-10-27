---
title: Asteroid Mite
design: draft
implementation: not-started
art: proposed
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../world/belt]
updated: 2026-10-27
---

# Asteroid Mite

## Summary

A tiny Vrell crawler that rides the belt's drifting rocks in clusters of 2–5 and leaps off at the
ship when it passes close. Act 5's Vrell popcorn: it carries the enemy count in the levels where the
Ascendancy brings the shots.

## Design

### Stat block

**Draft** (Act 5 design phase, step D-enemies-B; pending the user's review in round 60). A hand-written
table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2).
A `tiny` unit: one hit at every level, so the proposed Act 5 reference DPS (L29 120, judgement call J2)
does not set it, and the beam-era factor does not apply (`small` only).

| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `space` (the `air` layer in vacuum: a doc alias, see [space](../README.md#design)) |
| Size tier | `tiny` |
| Size | 20×20 px, hitbox 14×14 |
| Parts | single |
| Orientation | `16 angles` (a clinging pose per side of the rock, 4 crawl frames; a leap pose) |
| HP | 1 (easy 1 / hard 1, from the global multipliers) |
| Armour / shield | none |
| Speed | rides its rock (the rock's drift plus the scroll); 260 px/s in its dive |
| Movement | `ride` ([rock riding](../../README.md#rock-riding)): rides a small drifting rock (2–5 a rock); when the ship's centre comes within **160 px** of the rock's, each rider in turn (0.15 s apart, a seeded order) shows its tell for **0.4 s**, then `dive`s straight at the ship's position at the tell's end at 260 px/s, no steering, and flies on off the screen if it misses |
| Attack | `none`: contact only (`tiny` 6; destroyed by the impact, as every `tiny` rammer) |
| Formations | swarm (on rocks): 1–3 rocks a wave, 2–5 riders each, from the front or the sides |
| Weak points | none (the teal glow along its back is drawn only) |
| Effective traits | `spread`, `area`, `forward` |
| Credits | 3 (score 30 × chain) |
| Death | `tiny` chitin burst, a teal spark; nothing left |
| First level / used in | L29; the rock fields of L30, L31 and L33 as popcorn (the level docs place them) |
| Difficulty hooks | easy: leaps within 130 px, tell 0.5 s, dive 220 px/s; hard: leaps within 200 px, dive 300 px/s |

### Behaviour

- **Riding a rock** (the gap analysis's decision): the rock is a small breakable `Debris` chunk (Level
  03's small-chunk rules: it blocks shots and breaks when shot, its HP in the level data), drifting at
  30–60 px/s plus the scroll. The riders cling to its rim and crawl round it; they turn with it and are
  carried off the screen with it if the ship never comes close (no credits, no penalty).
- **Shot rock**: breaking the rock frees its riders early: each tumbles 0.3 s, then shows its tell and
  dives as above. Shooting the rock from far away is the safe answer; a spread clears the riders as they
  leap.
- **Shots at riders**: a clinging mite is hit by every weapon; a shot that hits the rock first is
  stopped by the rock (it blocks shots), so riders on the far side are safe until they leap.
- **Tell**: the mite crouches and its back glow flares teal (the glow of contact and ramming, see the
  [role colours](../../README.md#role-colours)); a short rock-scrape sound starts with it.
- **Edge warning**: rocks that bring riders in from a side edge are edge-warned like every side entry.
- **Q6** (popcorn in a human war, recommended **b**, pending round 60): the Vrell carry the count in
  Act 5 (Skitters, Mote Swarms, Asteroid Mites), the Ascendancy brings the shots and the
  [Dart Drone](../../air/dart-drone/README.md) fills the Ascendancy-only stretches.

### Difficulty and level use

- Never more than 3 rocks with riders on the screen at once, so a leap is always readable.
- L29 introduces them in Ceres Hub's approach debris; L30's rock field mixes them with the mine lines
  (a rider can leap through a gap the player is aiming for); L31 and L33 use them as fill.

### Sounds

Brief for the round-60 sound proposals (Freesound CC0/CC-BY, real recordings, no bubbly or popping
sounds): the crawl as a dry insect or crab clicking on stone; the tell a short gritty rock scrape; the
death a crisp chitin crack. Heard only close (a quiet tiny unit).

## Concept art

None yet: proposals (a/b) in round 60 (step ART-P-units, `tools/concept/enemies_r60.py`): a plum-chitin
mite (swarm fodder) with a teal back glow, clinging to a grey-brown rock.

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (pending the user's review), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 29 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/asteroid-mite-r60-a.png](concept/asteroid-mite-r60-a.png) | Asteroid Mite A, tick — a round plated plum abdomen with a teal seam, a small dark head with bone mandibles, six short splayed legs (sheet) | proposed |
| [concept/asteroid-mite-r60-a.gif](concept/asteroid-mite-r60-a.gif) | Asteroid Mite A, tick — a round plated plum abdomen with a teal seam, a small dark head with bone mandibles, six short splayed legs (motion) | proposed |
| [concept/asteroid-mite-r60-b.png](concept/asteroid-mite-r60-b.png) | Asteroid Mite B, flea — a long flat segmented body with three teal spots and two big folded bone-kneed hind legs (sheet) | proposed |
| [concept/asteroid-mite-r60-b.gif](concept/asteroid-mite-r60-b.gif) | Asteroid Mite B, flea — a long flat segmented body with three teal spots and two big folded bone-kneed hind legs (motion) | proposed |

## Implementation

- [ ] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers applied
- [ ] Units riding a `Debris` rock: carried with it, freed when it breaks, leaving with it
- [ ] The proximity leap: a tell of 0.4 s per rider in a seeded order, then a straight `dive`; deterministic and allocation-free
- [ ] Production sprites (16 headings, crawl frames, the leap pose) and the intel portrait
- [ ] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds)

## Open questions

- Q6 (pending round 60): **b** recommended (the Vrell carry the count, plus the Dart Drone); the
  alternatives are **a** (mixed waves only, the Ascendancy-only stretches fall short of the floor) and
  **c** (Talons in great numbers, against the doctrine).

## Decisions

- 2026-10-27 (step D-enemies-B): drafted for the Act 5 design phase from the roster row and the gap
  analysis; every number is a first draft, pending the user's review in round 60.
- 2026-10-27 (the gap analysis's decision): the mites ride `Debris` rocks (2–5 a rock), shooting the
  rock frees them early, they leap when the ship passes within 160 px (tell 0.4 s).
