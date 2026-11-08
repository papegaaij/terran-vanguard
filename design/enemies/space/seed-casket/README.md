---
title: Seed casket
design: approved
implementation: done
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../husk, ../../../world/vrell-space, ../../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir]
updated: 2026-11-08
---

# Seed casket

## Summary

A Vrell seed casket drifting out of a breached hull in the graveyard of the Choir: a chitin capsule, dull grey-brown
like the dead hulls but with a faint teal seam, the one thing in the wreckage worth taking. Level 46's secondary
objects (`CASKETS n / 5`): 20 HP, no contact damage, never picked by homing shots or Rook; broken, it frees a medium
salvage.

## Design

### Stat block

The approved text of [Level 46](../../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md#the-seed-casket)
(round 84, D.10) promoted to a part of its own in M11 part D (step D2), because the secondary's `escapes` and the loader
find units only here (the [spawning organ](../../ground/spawning-organ/README.md)'s precedent; gap check `m11d-gaps.md`
§4). Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)), from its [data.yaml](data.yaml)
(the table is rendered from it).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (`space`, the play plane) |
| Size tier | `tiny` |
| Size | 40×28 px, hitbox 40×28 |
| Parts | single |
| Orientation | `fixed` (it drifts without turning) |
| HP | 20 (easy 15 / hard 26, from the global multipliers) |
| Armour / shield | none |
| Speed | 20 px/s (each casket drifts at 15–25 px/s across the play plane, out of a breached hull) |
| Movement | `line_drift`: a slow straight drift along its wave's line, entering unwarned from an edge and leaving through the far one (≥ 8 s on the screen); it does not scroll with the hulks |
| Attack | none; no contact damage (the ship flies through it) |
| Formations | single |
| Weak points | none |
| Effective traits | |
| Credits | 0 (score 0 × chain); no bounty: broken, it frees a **medium salvage** (50 in Act 1 terms) and counts for the secondary (`escapes`); not counted as an enemy; homing shots and Rook never pick it |
| Death | the capsule splits along its seam, the halves drifting apart with a faint teal breath |
| First level / used in | L46; Level 46 (the secondary `CASKETS`, C1–C6) |
| Difficulty hooks | none (the global HP multipliers only) |
<!-- /data -->

### Behaviour

- **A drifting object on the play plane**: it drifts slowly (15–25 px/s, drawn per casket) straight along its wave's
  line ([Husk](../husk/README.md)'s `line_drift`, without the spin), in unwarned from an edge and out through the far
  one; it does not scroll with the hulks, so each is on the screen at least 8 s (gap check §4: at the scroll's 90 px/s a
  casket would cross in 6 s).
- **Harmless**: no attack and no contact damage (the ship flies through it).
- **Not a target for seekers**: homing shots, turrets and Rook never pick it (Level 34's containers' rule); direct fire
  breaks it.
- **The secondary's object**: not counted as an enemy (density, the floor, kill ratios) and no bounty; broken, it frees
  a **medium salvage** (50 in Act 1 terms) and counts for `escapes: seed-casket`; one that drifts off the screen
  unbroken is away.
- **Death**: the capsule splits along its seam, the halves drifting apart with a faint teal breath.

### Difficulty and level use

- Its HP by the global multipliers (easy 15 / hard 26); the secondary's target per difficulty is the level's (5 of 6,
  easy 4, hard 6).
- Level 46 only (C1–C6).

## Concept art

Its production art (round 91, step ART-D, `tools/art/act7_deadfleet.py casket`: whole, broken, the tatters and the
teal breath) lives with the level that places it:
[Level 46's Concept art](../../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md#concept-art)
(`l46-props-final-r91-a`; in `assets/` since step D2 claimed the frames).

## Implementation

- [x] Stat block in a `data.yaml`, loaded and checked (M11 part D, step D2): `line_drift` (CD1's drift), `contact:
      false`, `seekers_ignore`, `counted: false`, a medium salvage drop
- [x] Placed as Level 46's six caskets C1–C6 on their drift lines, the secondary `escapes: seed-casket`, flown by
      `Level46Test`
- [x] Looks and sounds in the game (step D4): the casket's dry crack and its teal breath

## Decisions

- 2026-11-06 (M11 part D, step D2; gap check `m11d-gaps.md` §4, pending the user's review in round 91): promoted from
  Level 46's "row in the enemies' data" to its own part (the loader and the secondary need a unit); `tier: tiny` (the
  beam era's HP factor for `small` units never applies: the approved 20 HP), `orientation: fixed`, no spin; it drifts on
  the play plane at its own 15–25 px/s, not with the scroll (the ≥ 8 s on the screen); `contact: false` is new (a unit
  without contact damage).
- 2026-11-06, round 91: F.3 accepted (the warm hull with its core and sinews, the casket and the wrecks).
- 2026-11-08 (user, [round 96](../../../concept-rounds/round-96/README.md) closed): **D.3 approve**: Level 46's casket, as built (round 91); `design: approved`, the user's approval in this round.
