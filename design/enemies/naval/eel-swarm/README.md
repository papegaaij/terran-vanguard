---
title: Eel Swarm
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-22
---

# Eel Swarm

## Summary

A 10-16 segment Vrell eel, each segment its own small eel with a glowing lateral stripe, swimming a sinuous path with its head firing aimed shots. The act's chain: the head is vital, cutting the body leaves a loose tail that scatters.

## Design

### Stat block

**Draft** (Act 4 design phase, round 48; decided in round 48 (2026-10-22)). A hand-written table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the proposed Act 4 reference DPS (L22 96, then +4 a level to 120 at L28; part A sets it on the extended balance plan). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top, so the fire rates are tuned per level with `levelReport` in the build. The play plane under water is the `air` layer; `sub` is the deep water below it, `ground` the sea floor and the trench walls.

| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (the play plane under water) |
| Size tier | `large` (the chain's total length) |
| Size | head 48x40 px, hitbox 36x28; segment 36x24 (hitbox 28x18); tail 32x24 (hitbox 24x16) |
| Parts | `vital` head, 10-16 `destroyable` segments (12 by default), `destroyable` tail |
| Orientation | `16 angles` per part, following the head's path history (`chain`) |
| HP | head 100 / segment 10 each / tail 25; whole 245 with 12 segments (easy 184 / hard 319, from the global multipliers): the head's 1.0 s at the L23 reference of 100, a segment 0.1 s |
| Armour / shield | none |
| Speed | 110 px/s along its path (plus scroll) |
| Movement | `snake` along an authored water path, a sine wave of 36 px at 0.8 Hz about the route, the segments following by `chain` (path history at a fixed spacing); the stripe glows in a wave from head to tail |
| Attack | the head fires an `aimed` shot (a violet pulse, `small` = 4, 130 px/s base) every 1.8 s and a 3-way `fan` (spread 40 degrees, 120 px/s) every 5 s; the segments fire nothing |
| Formations | `snake` (solo or two staggered 4 s apart) |
| Weak points | the head's lime gill clusters (the vital head) |
| Effective traits | `spread`, `piercing`, `beam` |
| Credits | 56 (score 560 x chain); whole (head-first kill) 48: the head's 30 plus its first bonus 18; parts: head 30, segment 2 each, tail 2 + 4 if destroyed first |
| Death | head killed: the body dies segment by segment from head to tail, 0.07 s apart (`small` flashes, a `medium` at the head), the husks sink; a cut's rear part scatters as drifting eels for 3 s and dies |
| First level / used in | L23; L24 and L26 (a dark snake lit by its stripe), L27; the Maw sheds Eel Swarm trails in L28; returns later with the act HP factor |
| Difficulty hooks | easy: 10 segments, no `fan`; hard: 16 segments, `aimed` every 1.4 s |

### Behaviour

- **Chain**: the [Coilwyrm](../../air/coilwyrm/README.md)'s and [Threadcrawler](../../ground/threadcrawler/README.md)'s rig in the water: the head swims the path, each part follows its path history. The head is on the screen at least 1.5 s before it can reach the player (snake readability rule).
- **Cut and scatter**: cutting a segment leaves the rear part alive as a headless trail: it keeps the old heading, 70 px/s, for 3 s, firing nothing, then dies (the Threadcrawler cut-and-die rule softened for the water; the roster's "cut-and-regrow" is not drafted: pending the user's review).
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

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen)
- [ ] Behaviour, attack and tells per this spec, deterministic and allocation-free
- [ ] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
- [ ] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec

## Open questions

- Should a cut tail regrow (the roster row) or scatter (our reading)? Our reading keeps the Threadcrawler rules and is cheaper.

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (our reading): no regrowth, the cut tail scatters for 3 s.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
