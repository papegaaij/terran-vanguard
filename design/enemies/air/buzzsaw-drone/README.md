---
title: Buzzsaw Drone
design: draft
implementation: not-started
art: proposed
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../world/belt, ../../../campaign/act-5-the-belt]
updated: 2026-10-27
---

# Buzzsaw Drone

## Summary

A radially symmetric Ascendancy blade drone that chases the ship, spins up with a clear tell and
then dashes in a straight line, **ricocheting** off the play-field edges and the belt's drifting
asteroids. Contact damage only, but lethal in groups (Level 31).

## Design

### Stat block

**Draft** (Act 5 design phase, pending the user's review in round 60). A hand-written table: there is
no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does
2), set at its first level from the proposed Act 5 reference DPS (L31 = 128). As a `small` unit it
takes the [beam-era](../../README.md#balancing-basis) ×2 and keeps it in every later level.

| Field | Value |
|---|---|
| Faction | Jovian Ascendancy; a **machine** (stunned 3 s by the [EMP Burst](../../../player/specials/README.md#emp-burst-l15): it stops spinning and coasts) |
| Layer | `air` |
| Size tier | `small` |
| Size | 36×36 px, a round hitbox of 26 px |
| Parts | single |
| Orientation | `radial` (six blades; `spin` 2 turns/s chasing, 6 turns/s spun up) |
| HP | 10 base, **20** with the beam-era ×2 (easy 15 / hard 26, from the global multipliers): 0.16 s at the L31 reference of 128 |
| Armour / shield | none (too small for a shield emitter) |
| Speed | 120 px/s chasing; **360 px/s** dashing |
| Movement | `chase` (turn rate 120°/s) for 2.5 s; then the **spin-up tell 0.6 s** (it slows to 40 px/s, the blades speed up with a rising whine, the gold rim flickers red); then a **dash** in a straight line along the bearing to the ship at the tell's start (locked, not re-aimed), `ricochet` off the play-field edges and the big drifting asteroids up to 3 times; it then slows to 120 px/s over 0.5 s and chases again; after its second dash it leaves through the nearest edge |
| Attack | `none`: contact `small` = 10 (destroyed by the impact) |
| Formations | `stream` (3–6 from alternating sides, edge-warned on a side entry), `circle` (4–6 orbiting a point 160 px below the top, breaking off one by one into their chase) |
| Weak points | the hub (drawn only) |
| Effective traits | `spread`, `area`, `forward`, `piercing` |
| Credits | 12 (score 120 × chain) |
| Death | `small` explosion; the blades fly off as three spinning sparks (presentation, no damage) |
| First level / used in | L31; its later Act 5 levels per the level drafts |
| Difficulty hooks | easy: tell 0.8 s, dash 300 px/s, 2 bounces; hard: tell 0.5 s (the floor), dash 400 px/s, three dashes before it leaves |

### Behaviour

- **The tell is the rule.** A dash never starts without the full spin-up (≥ 0.5 s at every
  difficulty), and its line is the bearing at the tell's start: a ship that moves during the tell
  is missed. Several drones in a `circle` stagger their tells (0.4 s apart), so the player reads one
  dash at a time.
- **Ricochet off rocks.** In the belt the dash bounces off the big, indestructible drifting rocks of
  the `Debris` hazard as off an edge (equal angles, the rock's outline as a circle); small breakable
  rocks are smashed by the dash and do not deflect it (see `ricochet` in the
  [movement vocabulary](../../README.md#movement-pattern-vocabulary)). A bounce counts toward the 3.
- **Sounds** (brief for the audio step): a circular-saw whine whose pitch follows the spin (a real
  saw or grinder from Freesound, CC0/CC-BY), rising sharply through the tell; a metal scrape-clang on
  each ricochet; a short shatter on death.

### Difficulty and level use

- At most 6 drones on the screen; at most two in their dash at once.

## Concept art

The chosen look of round 05, [buzzsaw-drone-r05-a.png](../concept/buzzsaw-drone-r05-a.png) and its
[motion GIF](../concept/buzzsaw-drone-r05-a.gif) (six blades, gunmetal and gold, rim light, ricochets),
is listed in the [air](../README.md#concept-art) Concept art table. Round 60's unit proposals add the
spin-up tell and the belt readability of [Q11](../../README.md#open-questions).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (pending the user's review), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 31 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/buzzsaw-drone-r60-a.png](concept/buzzsaw-drone-r60-a.png) | Buzzsaw Drone A, gunmetal blades, gold tips — the r05 gunmetal blades with gold tips round a gold hub ring and a red eye; strong rim (sheet) | proposed |
| [concept/buzzsaw-drone-r60-a.gif](concept/buzzsaw-drone-r60-a.gif) | Buzzsaw Drone A, gunmetal blades, gold tips — the r05 gunmetal blades with gold tips round a gold hub ring and a red eye; strong rim (motion) | proposed |
| [concept/buzzsaw-drone-r60-b.png](concept/buzzsaw-drone-r60-b.png) | Buzzsaw Drone B, gold blades — six solid gold blades round a black hub; the tell's red flicker covers the disc (sheet) | proposed |
| [concept/buzzsaw-drone-r60-b.gif](concept/buzzsaw-drone-r60-b.gif) | Buzzsaw Drone B, gold blades — six solid gold blades round a black hub; the tell's red flicker covers the disc (motion) | proposed |

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers and the beam-era factor applied; re-measured with `levelReport` at L31
- [ ] The chase → spin-up tell → locked dash cycle, ricochet off the edges and the big `Debris` rocks, small rocks smashed, deterministic and allocation-free
- [ ] Production sprites (radial spin frames, the red tell flicker) via `tools/art/`; death effect, bounty and sounds (Freesound, CC0/CC-BY)

## Open questions

- None open (no round-60 question touches it).

## Decisions

- 2026-10-27 (step m9-d-enemies-a): drafted for the Act 5 design phase from the roster row and the
  gap analysis; every number is a first draft pending the user's review in round 60. The roster's
  "4 blades" follows the chosen round 05 look: six blades.
