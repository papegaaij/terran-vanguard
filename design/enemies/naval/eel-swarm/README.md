---
title: Eel Swarm
design: approved
implementation: done
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-22
---

# Eel Swarm

## Summary

A 10-16 segment Vrell eel, each segment its own small eel with a glowing lateral stripe, swimming a sinuous path with its head firing aimed shots. The act's chain: the head is vital, cutting the body leaves a loose tail that scatters.

## Design

### Stat block

Approved in round 48 (2026-10-22). Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)), from its [data.yaml](data.yaml) (M8 part C, step C2; the table is rendered from it); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the Act 4 reference DPS (L23 100). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top, so the fire rates are tuned per level with `levelReport` in the build. The play plane under water is the `air` layer; `sub` is the deep water below it, `ground` the sea floor and the trench walls. The data: the head's part fires a list of two attacks (`attacks: [head-aimed, head-fan]`, the fan `only` on medium and hard), the head's path a `sway` (36 px, 0.8 Hz), the cut a `scatter` (70 px/s for 3 s) instead of the Coilwyrm's regrowth.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (the play plane under water) |
| Size tier | `large` |
| Size | 48×40 px, hitbox 36×28 (the head); segment 36×24 (hitbox 28×18); tail 32×24 (hitbox 24×16) |
| Parts | `vital` head, 10–16 `destroyable` segments (12 by default), `destroyable` tail |
| Orientation | `16 angles` per part, following the head's path history (`chain`) |
| HP | head 100 · segment 10 each · tail 25; whole 245 (easy 184 / hard 318, from the global multipliers) |
| Armour / shield | none |
| Speed | 110 px/s along its path (plus scroll) |
| Movement | `snake` along an authored water path, a sine of 36 px at 0.8 Hz about the route, the segments following by `chain` (path history at a fixed spacing); the stripe glows in a wave from head to tail |
| Attack | the head fires an `aimed` violet pulse every 1.8 s (130 px/s, `small` = 4); a 3-way `fan` every 5 s (spread 40°, 120 px/s); the stripe glows from head to tail 0.3 s before each shot (the tell); the segments fire nothing; contact damage `large` = 20 (head) / `small` = 10 (segments, tail) |
| Formations | snake (solo or two staggered 4 s apart); never more than two on screen |
| Weak points | the head's lime gill clusters (the vital head) |
| Effective traits | `spread`, `piercing`, `beam` |
| Credits | 56 (score 560 × chain); whole (head-first kill) 48: the head's 30 + its first bonus 18; parts: head 30, segment 2 each, tail 2 + 4 if destroyed first |
| Death | head killed: the body dies segment by segment from head to tail, 0.07 s apart (`small` flashes, a `medium` at the head), the husks sink; a cut's rear part scatters as drifting eels at 70 px/s for 3 s, firing nothing, and dies from the cut backwards |
| First level / used in | L23; L24 and L26 (a dark snake lit by its stripe), L27; the Maw sheds Eel Swarm trails in L28; returns later with the act HP factor |
| Difficulty hooks | easy: 10 segments, no `fan`; hard: 16 segments, `aimed` every 1.4 s |
<!-- /data -->

### Behaviour

- **Chain**: the [Coilwyrm](../../air/coilwyrm/README.md)'s and [Threadcrawler](../../ground/threadcrawler/README.md)'s rig in the water: the head swims the path, each part follows its path history. The head is on the screen at least 1.5 s before it can reach the player (snake readability rule).
- **Cut and scatter**: cutting a segment leaves the rear part alive as a headless trail: it keeps the old heading, 70 px/s, for 3 s, firing nothing, then dies (the Threadcrawler cut-and-die rule softened for the water; the roster's "cut-and-regrow" is not built, settled by round 48's approval).
- **The head fires** (replacing the pore wave of the land chains); the segments' glow is a tell: the stripe runs to the head 0.3 s before each shot.
- **Cutting is a score opportunity**: parts pay as the Threadcrawler's, so a head-first kill is the quick 48 and the full cut-up pays 56.
- **Under water**: the shots carry the level's x 0.7, the drag makes the weaving path a real dodge; the path's loops stay inside the flyable width.
- **Maw link**: the Abyssal Maw's eel trails reuse this chain's rig (the boss's own README owns the numbers).

### Difficulty and level use

- L23 introduces it as a solo snake after the descent; later levels stagger two. Never more than two on the screen.
- `anti-sub` weapons are not needed against it, but a loadout without one loses the 30 % that the level's rules cost (the whole point of the act).

## Concept art

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): a head and segment sheet with the stripe glow and a swimming GIF. Files `eel-swarm-r48-a/b` in [naval/concept](../concept/), listed in the [naval](../README.md#concept-art) Concept art table. Notes: b blends with the Level 23 floor.

## Implementation

- [x] Stat block loaded from a `data.yaml` (step C2: loaded and checked; the head's attack list, the sway and the scattering cut mapped in step C3; re-measured on Level 23 with `levelReport`, step C3); global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen)
- [x] Behaviour, attack and tells per this spec, deterministic and allocation-free (steps C1 and C3; the tells' look: step C4)
- [x] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
      (step ART-C's `tools/art/eel_swarm.py` from round 48's a, the looks step C4: `Level23LooksTest.theArtIsWhatTheLooksDraw`,
      `theEelsStripeRunsFromTheTailToTheHead`, `PortraitsTest`; the finals in round 52)
- [x] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec (the under-water bursts, `eel-pulse` and
      `eel-segment`: `Level23SoundsTest`; the bounty whole or by parts: `TypicalHaulTest`; round 52's a files until the picks)

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (our reading): no regrowth, the cut tail scatters for 3 s.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-22 (M8 part C, step C2; gap check m8c-gaps.md §5 D11): the open question (regrow or scatter) closed: round 48's approval settled the scatter, no regrowth. The stat block is now its [data.yaml](data.yaml) and the table rendered from it. Data readings, pending the user's review in round 52: `size` and the parts' hit boxes as [across, along] the path (a segment 36 px along, 24 across, hit box share 0.77; the tail's hit box written [16, 24]); the chain's spacing 0.8 × a segment's length (Threadcrawler's, ≈ 380 px with 12 segments); the stripe's tell as each attack's `tell` 0.3 s; easy's missing fan as the fan's `only: [medium, hard]`.
- 2026-10-22 (M8 part C, step C3): mapped for the simulation: the head's attack list as its head attacks (each on its own clock, the head's own gun empty), the sway as a sine of 36 px over 1.25 s about the path, the cut's scatter (70 px/s, 3 s, no regrown head). Data reading, pending the user's review in round 52: the first shots' delays after the head is on the screen, the aimed 0.6 s and the fan 2.5 s (half its interval), so the aimed shot comes first (Level 23's note) and each first volley has its stripe tell. The level's credit table and the code count an eel the same way: whole (48) for the typical player, by its parts with the tail first (60) in a perfect run.
- 2026-10-22 (M8 part C, steps C1, ART-C and C4; pending the user's review in round 52): the final frames from round 48's a
  (`tools/art/eel_swarm.py`: the head at 16 headings at rest, in its tell and firing, the body eel and the tail with the
  stripe lit, the stripe's additive bloom, the muzzle, the husks sinking); the stripe tell drawn brighter than the concept
  with a bloom so it reads at play size (step ART-C); a cut's scattered parts live their 3 s even off the screen (step C1);
  no glow-only frames yet (Level 26's darkness, part F). The sounds: step AUD-C's `eel-pulse` and `eel-segment` a/b, the
  a files in the game until round 52's picks. Captured in Level 23 (`eel-swarm-capture-r52-a`).
