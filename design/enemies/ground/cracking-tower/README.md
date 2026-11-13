---
title: Cracking Tower
design: approved
implementation: done
art: chosen
depends-on: [../../../campaign/act-6-jovian-storm/level-39-io-flyby, ../strider]
updated: 2026-11-13
---

# Cracking Tower

## Summary

A refinery column on Io's sulphur plains, its gold flare cap burning: Level 39's primary target, six of them in three
works. No attack, not hardened, 260 HP; one that scrolls past alive fails the mission. The design is
[Level 39's](../../../campaign/act-6-jovian-storm/level-39-io-flyby/README.md#the-cracking-tower-and-the-pump-station);
this part only holds its numbers, because the loader finds units only in a directory of their own.

## Design

### Stat block

Numbers at **medium** from its [data.yaml](data.yaml) (the table is rendered from it), Level 39's approved table
(round 72). Counted as an enemy (Level 09's Hive Nodes' model): a destroy-targets group, a
[Strider](../strider/README.md)'s topple and the kill tally count units.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy |
| Layer | `ground` |
| Size tier | `large` |
| Size | 96×96 px, hitbox 72×72 |
| Parts | single |
| Orientation | `fixed` (seen from above; the flare cap burns) |
| HP | 260 (easy 195 / hard 338, from the global multipliers) |
| Armour / shield | none |
| Speed | 0 px/s (scrolls with the ground) |
| Movement | `terrain` (fixed on its works' pad) |
| Attack | `none`: the works' guards fire; nothing to spoof |
| Formations | single (two to a works, each its own named target) |
| Weak points | none (dies in ≈ 1.7 s at the Level 39 reference) |
| Effective traits | `anti-ground` |
| Credits | 30 (score 300 × chain); a Strider toppled onto it destroys it and pays it |
| Death | Io's near-vacuum: a short white flash, the flare cap tumbling off, a ring of burning debris that does not rise; the dark wreck stays and the pipes from it stop glowing |
| First level / used in | L39; Level 39 (the primary `destroy-targets`: the six named towers of the Loki, Pele and Prometheus works) |
| Difficulty hooks | none (the global HP multipliers; the holds' speeds change instead) |
<!-- /data -->

### Behaviour

Level 39's [objective](../../../campaign/act-6-jovian-storm/level-39-io-flyby/README.md#objective) and
[holds](../../../campaign/act-6-jovian-storm/level-39-io-flyby/README.md#layout): each tower is its own named group
(`TOWERS n / 6`); the scroll holds over a works while its towers stand; a Strider toppled onto one destroys it, pays it
and counts. No vent lies within 100 px of a tower.

## Concept art

Production art in Level 39's batch (step ART-E, round 77, QE-3):
[refinery-final-r77-a.png](../../../campaign/act-6-jovian-storm/level-39-io-flyby/concept/refinery-final-r77-a.png)
(`tools/art/refinery_units.py`: the tower, its flare, fall and wreck); see Level 39's *Concept art*.

## Implementation

- [x] Stat block in a `data.yaml`, loaded and checked (M10 part E, step E2)
- [x] The six towers as Level 39's `destroy-targets` groups and the holds (step E2)
- [x] Production sprite in the game, its flare, fall and wreck, the pipes going dark (step E4; `Level39LooksTest`; in
      round 77's captures)

## Decisions

- 2026-11-01 (M10 part E, step E2; gap check `m10e-gaps.md` §4, decided 2026-11-01, round 77 C.2): promoted from
  Level 39's ground objects to a minimal part (the loader needs a unit directory; the design stays in Level 39), a
  counted enemy (the topple's crush and the kill tally need it; the density gains 6 units). Its bounty 30 lies under
  the large class's 40–60 (Level 39's approved credit budget gives it; `BalanceTest`'s accepted exception).
- 2026-11-01 (user, round 77 closed): the cracking tower's production art accepted (F.2, `art: chosen`), its counted-enemy
  promotion accepted (C.2), the pump's vacuum burst (C.11) and the pipes' glow over flyers (C.10) accepted; the topple onto
  a tower (F.5) and the failed primary (F.6) accepted. The document stays `design: review` (the user did not approve the part's page).
- 2026-11-13 (user): approved for v1.0.
