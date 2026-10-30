---
title: Wavering Stormhawk
design: approved
implementation: done
art: final
depends-on: [../../../campaign/act-5-the-belt/level-29-betrayal-at-ceres, ../../../systems/economy]
updated: 2026-10-29
---

# Wavering Stormhawk

## Summary

A Ceres Wing pilot in the CDF Stormhawk airframe, the Ascendancy's crest freshly painted over the CDF roundel, who will
not fire on Aegis Wing: Level 29's three wavering pairs and its inverted "Hold fire" secondary. A **neutral** craft:
it never fires, never rams and never counts as an enemy. Owned by
[Level 29](../../../campaign/act-5-the-belt/level-29-betrayal-at-ceres/README.md#objective) (approved in round 60, Q10
b); a unit data file since M9 part B (step B2), as the loader and the looks key on unit kinds (gap check
`m9b-gaps.md` §4).

## Design

### Stat block

Rendered from [data.yaml](data.yaml) (numbers at **medium**, see the [balancing basis](../../README.md#balancing-basis)).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | CDF (Ceres Wing) |
| Layer | `air` |
| Size tier | `small` |
| Size | 40×40 px, hitbox 28×28 |
| Parts | single |
| Orientation | `16 angles` |
| HP | 20 (easy 15 / hard 26, from the global multipliers) |
| Armour / shield | none; HP 20 base flies at 40 with the beam era's ×2 |
| Speed | 90 px/s descending; 260 px/s peeling off |
| Movement | a pair enters at the top, descends to 150 px below the top edge, holds there 6 s (a slow sway, a wing-waggle), then peels off to the nearer side edge at 260 px/s; on the screen ≈ 9 s |
| Attack | `none`: a **neutral** craft: no fire, no contact (the ship passes under it); a white IFF light blinking 1 Hz, a red flash on a hit; homing shots and Rook never pick it, Rook's and the Bulwark's shots pass through it; the player's shots and specials hit it |
| Formations | line abreast (2–2) (Level 29's three pairs) |
| Weak points | none |
| Effective traits | |
| Credits | 0 (score 0 × chain); never counted as an enemy (density, the floor, kill ratios) |
| Death | `small` explosion; it fails the level's Hold fire secondary |
| First level / used in | L29; L29 only (Ceres Hub's mutiny) |
| Difficulty hooks | easy: holds 8 s; hard: 40 px lower (190 px), among the waves |
<!-- /data -->

### Behaviour

- **Neutral.** Homing shots and Rook never pick it, Rook's and the Bulwark's shots pass through it; the player's shots
  and specials hit it (the Airstrike downs it, the EMP leaves it alone). Not counted by the density, the floor, the
  kill ratios or the haul.
- **Hold fire.** The level's secondary `spared: wavering-stormhawk` is met when the third pair has left the screen with
  none destroyed and failed at the first destroyed; a hit only flashes its IFF light red.

## Concept art

Round 60's look is **A.22 a** (Ceres Wing ochre), the act's
[concept/](../../../campaign/act-5-the-belt/README.md#concept-art) (`wavering-stormhawk-r60-a`, production frames
`wavering-stormhawk-final-r62-a`).

## Implementation

- [x] Stat block loaded from a `data.yaml` (neutral, the hover's peel-off; M9 part B, step B2)
- [x] Never counted as an enemy (`countsAsEnemy`: density, floor, haul; steps B1, B2)
- [x] Production sprites with the IFF blink and the red hit flash (step B4) (`tools/art/belt_units.py`: `Level29LooksTest.theArtIsWhatTheLooksDraw` and `theIffLightBlinksAtOneHertz`; the art accepted in round 62)

## Decisions

- 2026-10-28 (M9 part B, step B2): a unit data file instead of a level-owned block (gap check §4, pending round 62);
  HP 20 base so the beam era's ×2 flies the README's 40; hard's "40 px lower" is the hook `hover_y: 190`, easy's 8 s
  hold the hook `hover_seconds`; its line abreast of 2 is pinned on every difficulty.
- 2026-10-28 (user, [round 62](../../../concept-rounds/round-62/README.md), closed; page answers): the art (F.5: 16 headings x 5 banks, the IFF light dark, white and red) accepted as final.
- 2026-10-29 (M9 part I, step I2): `art: final`: the production frames (`tools/art/belt_units*.py`, every PNG with its `Source` chunk, so `importPlaceholders` skips them) are in `assets/sprites/` and the user accepted them as final in round 62 (F.5); the round 60 mockup rows are history.
