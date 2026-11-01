---
title: Pump Station
design: review
implementation: done
art: proposed
depends-on: [../../../campaign/act-6-jovian-storm/level-39-io-flyby]
updated: 2026-11-01
---

# Pump Station

## Summary

A pump housing on Io's pipeline racks: Level 39's secondary target, eight of them, six to cut (`PUMPS n / 6`). No
attack, 20 HP. The design is
[Level 39's](../../../campaign/act-6-jovian-storm/level-39-io-flyby/README.md#the-cracking-tower-and-the-pump-station);
this part only holds its numbers, because the secondary's `escapes` counts a unit by its slug and the loader finds
units only in a directory of their own.

## Design

### Stat block

Numbers at **medium** from its [data.yaml](data.yaml) (the table is rendered from it), Level 39's approved table
(round 72).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy |
| Layer | `ground` |
| Size tier | `small` |
| Size | 40×40 px, hitbox 32×32 |
| Parts | single |
| Orientation | `fixed` (seen from above; its lamp lit) |
| HP | 10 (easy 8 / hard 13, from the global multipliers) |
| Armour / shield | none (not hardened). HP 10 base flies at 20 with the beam era's × 2 |
| Speed | 0 px/s (scrolls with the ground) |
| Movement | `terrain` (fixed on its pipeline rack) |
| Attack | `none` |
| Formations | single (one on a rack segment, P1-P8) |
| Weak points | none |
| Effective traits | `anti-ground` |
| Credits | 8 (score 80 × chain) |
| Death | a short flash and a debris ring; the rack's pipe beside it goes dark |
| First level / used in | L39; Level 39 (the secondary `PUMPS`: 6 of the 8 on the racks) |
| Difficulty hooks | none (the secondary's allowed escapes: 2) |
<!-- /data -->

### Behaviour

Level 39's [secondary](../../../campaign/act-6-jovian-storm/level-39-io-flyby/README.md#objective): `escapes:
pump-station`, two allowed away; met at the sixth down, failed by the third away. No vent lies within 100 px of a pump
station.

## Concept art

Production art in Level 39's batch (step ART-E, round 77, QE-3):
[refinery-final-r77-a.png](../../../campaign/act-6-jovian-storm/level-39-io-flyby/concept/refinery-final-r77-a.png)
(`tools/art/refinery_units.py`: the pump intact and wrecked); see Level 39's *Concept art*.

## Implementation

- [x] Stat block in a `data.yaml`, loaded and checked (M10 part E, step E2)
- [x] The eight pumps on Level 39's racks counted by the secondary (`escapes: pump-station`, step E2)
- [x] Production sprite in the game, its wreck and the rack's pipe going dark (step E4; `Level39LooksTest`)

## Decisions

- 2026-11-01 (M10 part E, step E2; gap check `m10e-gaps.md` §4, pending the user's review in round 77): promoted from
  Level 39's ground objects to a minimal part (the secondary counts a unit by its slug), a counted enemy; HP 10 base
  (the beam era's × 2 flies it at 20). Its bounty 8 lies under the small class's 10–15 (Level 39's approved credit
  budget gives it; `BalanceTest`'s accepted exception).
