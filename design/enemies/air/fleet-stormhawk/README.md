---
title: Fleet Stormhawk
design: approved
implementation: done
art: final
depends-on: [../../../campaign/act-6-jovian-storm/level-36-cloud-divers, ../../../ui/hud]
updated: 2026-10-31
---

# Fleet Stormhawk

## Summary

The CDF carrier group's drop craft in the fleet's grey and blue: Level 36's two pairs diving up past the ship at t=4
and t=10, the fleet following the wing down. A **neutral** craft: it never fires, never rams and never counts as an
enemy, and the HUD's friendly marker brackets it. Owned by
[Level 36](../../../campaign/act-6-jovian-storm/level-36-cloud-divers/README.md#the-fleets-drop-craft) (approved in
round 72); a unit data file since M10 part B (step B2), as Level 29's [wavering Stormhawk](../wavering-stormhawk/README.md).

## Design

### Stat block

Rendered from [data.yaml](data.yaml) (numbers at **medium**, see the [balancing basis](../../README.md#balancing-basis)).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | CDF (the Steadfast group) |
| Layer | `air` |
| Size tier | `small` |
| Size | 40×40 px, hitbox 28×28 |
| Parts | single |
| Orientation | `16 angles` |
| HP | 30 (easy 22 / hard 39, from the global multipliers) |
| Armour / shield | none; HP 30 base flies at 60 with the beam era's ×2 |
| Speed | 400 px/s up the screen from the bottom edge (≈ 1.5 s on the screen) |
| Movement | a pair dives past the ship up from the bottom edge and out at the top at 400 px/s, a presentation of the fleet following the wing down; the HUD's friendly marker opens its "FRIENDLIES INBOUND — HOLD FIRE" banner 2 s ahead and brackets them on the screen |
| Attack | `none`: a **neutral** craft: no fire, no contact; enemy fire and contact pass through them; homing shots and Rook never pick them, Rook's and the Bulwark's shots pass through them; the player's shots and specials hit them |
| Formations | line abreast (2–2) (Level 36's two pairs at t=4 and t=10) |
| Weak points | none |
| Effective traits | |
| Credits | 0 (score 0 × chain); never counted as an enemy (density, the floor, kill ratios) |
| Death | `small` explosion |
| First level / used in | L36; L36 only (the dive) |
| Difficulty hooks | none (its pairs pinned at 2 on every difficulty) |
<!-- /data -->

### Behaviour

- **Neutral** (the [friendly marker](../../../ui/hud/README.md#in-the-play-field)): the "FRIENDLIES INBOUND — HOLD
  FIRE" banner opens 2 s ahead of its wave (its warning: no edge warning), brackets while it is on the screen. Not
  counted by the density, the floor, the kill ratios or the haul.
- **The dive**: its wave enters `from: rear` at 400 px/s, straight up from the bottom edge and out at the top.

## Concept art

Production frames by `tools/art/jovian_units.py` (step ART-B) are the level's:
[fleet-stormhawk-final-r74-a](../../../campaign/act-6-jovian-storm/level-36-cloud-divers/README.md#concept-art).

## Implementation

- [x] Stat block loaded from a `data.yaml` (neutral; M10 part B, step B2)
- [x] The banner 2 s ahead and the brackets (step B4: the built friendly marker, seen in the Level 36 capture)
- [x] Production sprites at 16 headings with the exhaust streak (step B4: the streak under the hull while it dives up; the pass a pair, round 74 S.2 a)

## Decisions

- 2026-10-31 (M10 part B, step B2): a unit data file (gap check §4, decided 2026-10-31 (round 74)), HP 30 base as the wavering
  Stormhawk's (60 flown); its pair pinned at 2 on every difficulty by the formation's size.
- 2026-10-31, round 74: the production frames and the dive capture accepted (F.4); the pass-by **a** (two Eurofighter fly-bys, S.2) chosen; `art: final`.
