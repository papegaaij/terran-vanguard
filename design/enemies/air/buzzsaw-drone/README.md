---
title: Buzzsaw Drone
design: approved
implementation: done
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../world/belt, ../../../campaign/act-5-the-belt]
updated: 2026-10-28
---

# Buzzsaw Drone

## Summary

A radially symmetric Ascendancy blade drone that chases the ship, spins up with a clear tell and
then dashes in a straight line, **ricocheting** off the play-field edges and the belt's drifting
asteroids. Contact damage only, but lethal in groups (Level 31).

## Design

### Stat block

**Draft** (Act 5 design phase, approved in round 60). Rendered from [data.yaml](data.yaml) since M9 part D (step D2, 2026-10-28; its header lists the
build's own readings). Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does
2), set at its first level from the proposed Act 5 reference DPS (L31 = 128). As a `small` unit it
takes the [beam-era](../../README.md#balancing-basis) ×2 and keeps it in every later level.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy (machine: the EMP Burst stuns it longest) |
| Layer | `air` |
| Size tier | `small` |
| Size | 36×36 px, hitbox 26×26 (a round hitbox) |
| Parts | single |
| Orientation | `radial` (six blades; `spin` 2 turns/s chasing, 6 turns/s spun up) |
| HP | 10 (easy 8 / hard 13, from the global multipliers) |
| Armour / shield | none (too small for a shield emitter). HP 10 base flies at 20 with the beam era's ×2 |
| Speed | 120 px/s chasing; **360 px/s** dashing |
| Movement | `chase` (turn rate 120°/s) for 2.5 s; then the **spin-up tell 0.6 s** (it slows to 40 px/s, the blades speed up with a rising whine, the gold rim flickers red); then a **dash** in a straight line along the bearing to the ship at the tell's start (locked, not re-aimed), `ricochet` off the play-field edges and the big drifting asteroids up to 3 times; it then slows to 120 px/s over 0.5 s and chases again; after its 2nd dash it leaves through the nearest edge |
| Attack | `none`: contact `small` = 10 (destroyed by the impact) |
| Formations | stream (3–6), circle (4–6) (a stream from alternating sides, edge-warned on a side entry; a circle orbiting a point 60 px round, breaking off one by one, 0.4 s apart, into their chase; at most 6 on the screen, two dashing) |
| Weak points | the hub (drawn only) |
| Effective traits | `spread`, `area`, `forward`, `piercing` |
| Credits | 12 (score 120 × chain) |
| Death | `small` explosion; the blades fly off as three spinning sparks (presentation, no damage) |
| First level / used in | L31; its later Act 5 levels per the level drafts |
| Difficulty hooks | easy: tell 0.8 s, dash 300 px/s, 2 bounces; hard: tell 0.5 s (the floor), dash 400 px/s, three dashes before it leaves |
<!-- /data -->

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

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

The chosen look of round 05, [buzzsaw-drone-r05-a.png](../concept/buzzsaw-drone-r05-a.png) and its
[motion GIF](../concept/buzzsaw-drone-r05-a.gif) (six blades, gunmetal and gold, rim light, ricochets),
is listed in the [air](../README.md#concept-art) Concept art table. Round 60's unit proposals add the
spin-up tell and the belt readability of [Q11](../../README.md#open-questions).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (gloss black; the user chose look b, gunmetal/charcoal with gold, in round 60, so the chosen variant is re-rendered in gunmetal in production), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 31 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/buzzsaw-drone-r60-b.png](concept/buzzsaw-drone-r60-b.png) | Buzzsaw Drone B, gold blades — six solid gold blades round a black hub; the tell's red flicker covers the disc (sheet) | chosen (round 60) |
| [concept/buzzsaw-drone-r60-b.gif](concept/buzzsaw-drone-r60-b.gif) | Buzzsaw Drone B, gold blades — six solid gold blades round a black hub; the tell's red flicker covers the disc (motion) | chosen (round 60) |

## Implementation

- [x] Stat block loaded from a `data.yaml`; global difficulty multipliers and the beam-era factor applied; re-measured with `levelReport` at L31
- [x] The chase → spin-up tell → locked dash cycle, ricochet off the edges and the big `Debris` rocks, small rocks smashed, deterministic and allocation-free (step D1: `Dashing`, `DashRocks`)
- [x] Production sprites (radial spin frames, the red tell flicker) via `tools/art/`; death effect, bounty and sounds (Freesound, CC0/CC-BY)
      (ART-D `belt_units_d.py`, the gold share a/b in round 64; step D4: `MiningRigLooks`, the whine pitched with the spin,
      the spin-up and the ricochet round 64's a proposals until the pick; its death the `small` rung)

## Open questions

- None open (no round-60 question touches it).

## Decisions

- 2026-10-27 (step m9-d-enemies-a): drafted for the Act 5 design phase from the roster row and the
  gap analysis; every number is a first draft approved in round 60. The roster's
  "4 blades" follows the chosen round 05 look: six blades.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`); art A.6 **b**, "Buzzsaw Drone B, gold blades", chosen (`art: chosen`), the other variant in `concept/rejected/`. Q11 **b** (gunmetal/charcoal with gold, not gloss black): the chosen variant is re-rendered in gunmetal/charcoal in this part's production art, keeping the ≥ 20 % gold and the readability check.
- 2026-10-28 (M9 part D, step D2; pending the user's review in round 64): the stat block as data (the `dash`
  movement and its hooks; a circle's `orbit` 60 px at 90°/s before the break, the fixture's). At L31 under the plan's
  fit a Buzzsaw lives ≈ 1.25–1.45 s on the screen (`levelReport`'s small units: within Level 10's band).
