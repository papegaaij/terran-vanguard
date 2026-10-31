---
title: Helium-3 Harvester
design: approved
implementation: in-progress
art: proposed
depends-on: [../../../campaign/act-6-jovian-storm/level-36-cloud-divers, ../talon, ../../../systems/economy]
updated: 2026-10-31
---

# Helium-3 Harvester

## Summary

An uncrewed Ascendancy helium-3 scoop that skims a band below the play plane: Level 36's eight harvesters in four
pairs and its secondary (`HARVESTERS n / 6`). A hostile craft that never fires and never rams, picked, hit and paid
like an enemy but **not counted as one**. Owned by
[Level 36](../../../campaign/act-6-jovian-storm/level-36-cloud-divers/README.md#the-harvesters) (approved in round 72);
a unit data file since M10 part B (step B2), as the loader, the atlas and the looks key on unit kinds (gap check
`m10b-gaps.md` §4, as Level 29's [wavering Stormhawk](../wavering-stormhawk/README.md)).

## Design

### Stat block

Rendered from [data.yaml](data.yaml) (numbers at **medium**, see the [balancing basis](../../README.md#balancing-basis)).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy |
| Layer | `low-air` |
| Size tier | `medium` |
| Size | 96×56 px, hitbox 72×40 |
| Parts | single |
| Orientation | `fixed` (faces down; a pair's two hulls drawn mirrored) |
| HP | 160 (easy 120 / hard 208, from the global multipliers) |
| Armour / shield | none, no shield: a level object, its HP 160 flown with no act or beam-era factor (≈ 1.1 s under the reference of 144) |
| Speed | 50 px/s down the screen (it flies with the band's wind, slower than the scroll) |
| Movement | a pair enters at the top edge 160 px apart and skims **down the screen at 50 px/s** on a shallow diagonal (8 px/s sideways: the pair's two hulls mirrored, drifting apart), ≈ 12 s on the screen, and escapes at the bottom edge; never over the other's hull |
| Attack | `none`: never hurts anything (no fire, no contact) |
| Formations | carrier + escorts (2–2) (each pair's two [Talons](../talon/README.md) fly `orbit` at 90 px round the pair's middle and leave on their own swoop when both harvesters are gone) |
| Weak points | none |
| Effective traits | `homing` |
| Credits | 30 (score 300 × chain); not counted as an enemy (density, the floor, kill ratios), counted with the ground targets' share in the typical haul |
| Death | the hull breaks in two and falls into the band, the plume flaring once along its length (a smooth glow, no debris on the play plane); a `medium` explosion sound |
| First level / used in | L36; L36 only (the harvester fields, the secondary) |
| Difficulty hooks | none of its own: easy 5 of 8 and hard 7 of 8 are the level's secondary; HP by the global multipliers |
<!-- /data -->

### Behaviour

- **Not counted** (`counted: false`): homing shots, Rook and the Targeting computer pick it like an enemy and its kill
  pays, but the density, the Act 2+ floor and the kill ratios do not count it (the simulation's `countsAsEnemy()`).
  Its 30 credits count with the ground targets' share of the typical haul (`haul: ground-targets`).
- **A drifting carrier**: its wave's `carrier + escorts` pair enters at the top edge 160 px apart; its Talon escorts
  orbit the living harvesters' midpoint (one left: that one) and leave on their own swoop when both are gone.
- **The drift** (step B2, pending the user's review in round 74, QB-5 in `m10b-gaps.md` §7): the level's 30 px/s
  sideways toward each other would cross the hulls at about 2.7 s; the data flies the nearest variant that keeps "never
  over the other's hull" and "escapes at the bottom edge": 8 px/s apart.

## Concept art

Production frames by `tools/art/jovian_units.py` (step ART-B, QB-1 a pending round 74) are the level's:
[harvester-final-r74-a](../../../campaign/act-6-jovian-storm/level-36-cloud-divers/README.md#concept-art).

## Implementation

- [x] Stat block loaded from a `data.yaml` (`counted: false`, `haul: ground-targets`, the drift's `vx` and `mirrored`;
      M10 part B, steps B1 and B2)
- [ ] Never counted as an enemy, its credits with the ground targets (the level's tests, step B3)
- [ ] Production sprites with the plume (a smooth gradient) and the break in two (step B4)

## Decisions

- 2026-10-31 (M10 part B, step B2): a unit data file instead of a level-owned block (gap check §4, pending round 74);
  `medium` at its first level (no act or beam-era factor: the README's 160 HP flown as they are); its pair pinned at 2
  on every difficulty by the formation's size; the drift 8 px/s apart (QB-5, pending round 74).
