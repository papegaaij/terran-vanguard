---
title: Asteroid Mite
design: approved
implementation: done
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../world/belt]
updated: 2026-10-28
---

# Asteroid Mite

## Summary

A tiny Vrell crawler that rides the belt's drifting rocks in clusters of 2–5 and leaps off at the
ship when it passes close. Act 5's Vrell popcorn: it carries the enemy count in the levels where the
Ascendancy brings the shots.

## Design

### Stat block

**Draft** (Act 5 design phase, step D-enemies-B; approved in round 60). Rendered from [data.yaml](data.yaml) since M9 part B (step B2, 2026-10-28; its header lists the
build's own readings). Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2).
A `tiny` unit: one hit at every level, so the proposed Act 5 reference DPS (L29 120, judgement call J2)
does not set it, and the beam-era factor does not apply (`small` only).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (`space` in vacuum: a doc alias, see [space](../README.md#design)) |
| Size tier | `tiny` |
| Size | 20×20 px, hitbox 14×14 |
| Parts | single |
| Orientation | `16 angles` (a clinging pose per side of the rock, 4 crawl frames; a leap pose) |
| HP | 1 (easy 1 / hard 1, from the global multipliers) |
| Armour / shield | none |
| Speed | 260 px/s in its dive; it rides its rock (the rock's drift plus the scroll) until then |
| Movement | `ride` ([rock riding](../../README.md#rock-riding)): rides a small drifting rock (1–5 a rock); when the ship's centre comes within **160 px** of the rock's, each rider in turn (0.15 s apart, a seeded order) shows its tell for **0.4 s**, then `dive`s straight at the ship's position at the tell's end at 260 px/s, no steering, and flies on off the screen if it misses; a broken rock's riders tumble 0.3 s first |
| Attack | `none`: contact only (`tiny` 6; destroyed by the impact, as every `tiny` rammer) |
| Formations | swarm (1–5) (on rocks: a group of 3–5 rocks drifting in together, from the front or the sides; never more than 5 rocks with riders on the screen) |
| Weak points | teal glow along its back (drawn only) |
| Effective traits | `spread`, `area`, `forward` |
| Credits | 3 (score 30 × chain) |
| Death | `tiny` chitin burst, a teal spark; nothing left |
| First level / used in | L29; the rock fields of L30, L31 and L33 as popcorn (the level docs place them) |
| Difficulty hooks | easy: leaps within 130 px, tell 0.5 s, dive 220 px/s; hard: leaps within 200 px, dive 300 px/s |
<!-- /data -->

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
- **Q6** (popcorn in a human war, recommended **b**, decided in round 60): the Vrell carry the count in
  Act 5 (Skitters, Mote Swarms, Asteroid Mites), the Ascendancy brings the shots and the
  [Dart Drone](../../air/dart-drone/README.md) fills the Ascendancy-only stretches.

### Difficulty and level use

- Rocks come in **groups**: 3–5 rocks drifting in together, 1–5 riders a rock (round 62: hard to spot, but many;
  Level 29 flies 1–2 a rock). Never more than 5 rocks with riders on the screen at once.
- L29 introduces them in Ceres Hub's approach debris; L30's rock field mixes them with the mine lines
  (a rider can leap through a gap the player is aiming for); L31 and L33 use them as fill.

### Sounds

Brief for the round-60 sound proposals (Freesound CC0/CC-BY, real recordings, no bubbly or popping
sounds): the crawl as a dry insect or crab clicking on stone; the tell a short gritty rock scrape; the
death a crisp chitin crack. Heard only close (a quiet tiny unit).

## Concept art

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

Round 60 proposals (a/b), made in the gloss-black look of Q11 a and chosen below (production re-renders the chosen one in gunmetal/charcoal, Q11 b) (step ART-P-units, `tools/concept/enemies_r60.py`): a plum-chitin
mite (swarm fodder) with a teal back glow, clinging to a grey-brown rock.

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (gloss black; the user chose look b, gunmetal/charcoal with gold, in round 60, so the chosen variant is re-rendered in gunmetal in production), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 29 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/asteroid-mite-r60-a.png](concept/asteroid-mite-r60-a.png) | Asteroid Mite A, tick — a round plated plum abdomen with a teal seam, a small dark head with bone mandibles, six short splayed legs (sheet) | chosen (round 60) |
| [concept/asteroid-mite-r60-a.gif](concept/asteroid-mite-r60-a.gif) | Asteroid Mite A, tick — a round plated plum abdomen with a teal seam, a small dark head with bone mandibles, six short splayed legs (motion) | chosen (round 60) |

## Implementation

- [x] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers applied
- [x] Units riding a `Debris` rock: carried with it, freed when it breaks, leaving with it
- [x] The proximity leap: a tell of 0.4 s per rider in a seeded order, then a straight `dive`; deterministic and allocation-free
- [x] Production sprites (16 headings, crawl frames, the leap pose) and the intel portrait (`tools/art/belt_units.py`, steps ART-B and B4: `Level29LooksTest.theArtIsWhatTheLooksDraw` and `aBeltRockTurnsThroughItsSpinFrames`, the intel portrait in `PortraitsTest`; the art pending the user's review in round 62)
- [x] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds) (step B4: the tell's scrape and the leap as round 62's **a** proposals, `Level29SoundsTest`; the picks pending round 62)

## Open questions

- Q6 (decided in round 60): **b** recommended (the Vrell carry the count, plus the Dart Drone); the
  alternatives are **a** (mixed waves only, the Ascendancy-only stretches fall short of the floor) and
  **c** (Talons in great numbers, against the doctrine).

## Decisions

- 2026-10-27 (step D-enemies-B): drafted for the Act 5 design phase from the roster row and the gap
  analysis; every number is a first draft, approved in round 60.
- 2026-10-27 (the gap analysis's decision): the mites ride `Debris` rocks (2–5 a rock), shooting the
  rock frees them early, they leap when the ship passes within 160 px (tell 0.4 s).
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`); art A.7 **a**, "Asteroid Mite A, tick", chosen (`art: chosen`), the other variant in `concept/rejected/`. Q11 **b** (gunmetal/charcoal with gold, not gloss black): the chosen variant is re-rendered in gunmetal/charcoal in this part's production art, keeping the ≥ 20 % gold and the readability check.
- 2026-10-28 (user, round 62 F.3: "They should be hard to spot, but we need more of them, not just a hand full. Add
  more asteroids with mites that come in at the same time."; step B6): the rocks come in **groups of 3–5** drifting
  in together with **1–5 riders a rock** (was 2–5; Level 29 flies 1–2), at most **5** rocks with riders on the
  screen (was 3); the art stays. Level 29: 34 Mites on 10 rocks in 5 waves → 48 Mites on 29 rocks in 7 groups.
- 2026-10-28 (user, [round 63](../../../concept-rounds/round-63/README.md), closed): **C.2** accepted: Level 29's Mites in seven rock groups (48 on 29 rocks, the halt cycle's Mote Swarm 6).
