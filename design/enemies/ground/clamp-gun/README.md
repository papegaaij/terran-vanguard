---
title: Clamp gun
design: approved
implementation: done
art: proposed
depends-on: [../../../campaign/act-5-the-belt/level-29-betrayal-at-ceres, ../../../player/wingmen]
updated: 2026-10-28
---

# Clamp gun

## Summary

Helix's turret on each of the four docking clamps that hold the prison tender in Level 29's Dock 9. The four hold the
rescue halt: the level clock stands while one stands, and the last one's fall opens the tender's hold for Rook. Owned
by [Level 29](../../../campaign/act-5-the-belt/level-29-betrayal-at-ceres/README.md#ground-targets) (approved in
round 60); a unit data file since M9 part B (step B2), as the loader, the validator, the looks and the atlas key on
unit kinds (gap check `m9b-gaps.md` §4).

## Design

### Stat block

Rendered from [data.yaml](data.yaml) (numbers at **medium**, see the [balancing basis](../../README.md#balancing-basis)).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Helix Dynamics (machine: the EMP Burst stuns it longest) |
| Layer | `ground` |
| Size tier | `medium` |
| Size | 48×48 px, hitbox 36×36 |
| Parts | single (the turret turns on its clamp) |
| Orientation | `16 angles` (the turret; its clamp faces the tender) |
| HP | 250 (easy 188 / hard 325, from the global multipliers) |
| Armour / shield | none |
| Speed | 0 px/s (scrolls with the ground, halts with the rescue halt) |
| Movement | `terrain`: one on each of the prison tender's four docking clamps, asleep until the halt (Level 29's `dormant_until`); it holds the rescue halt (the level clock stands while one stands) |
| Attack | `aimed` single shot every 2 s (standard 160 px/s, `small` = 4) with a 0.3 s red muzzle glint; the turret turns 120°/s all round |
| Formations | turret nest (4–4) (Level 29's four, round the prison tender) |
| Weak points | none |
| Effective traits | `anti-ground`, `forward` |
| Credits | 10 (score 100 × chain) |
| Death | destroyed, the clamp springs open with a burst of gas and sparks |
| First level / used in | L29; L29 only (Dock 9's prison tender) |
| Difficulty hooks | easy: every 2.6 s; hard: `aimed` pairs |
<!-- /data -->

### Behaviour

- **The holders.** Level 29's held arena names their group (`holds: clamps`): the scroll eases to a halt over Dock 9 at
  t=120 and the level clock stands while a clamp gun stands; the halt's own cycle of waves runs meanwhile. They scroll
  in dark and asleep (not shootable, as Block C's nests) and wake with the 1 s tell as the scroll stands, so the halt
  always runs: the four 1,000 HP together, ≈ 14 s at the boss rule's 0.6 × the reference (step B3b). No boss bar:
  the objective line counts `CLAMPS 4` down during the halt (the HUD's).
- **The clamp.** Destroyed, its docking clamp springs open with a burst of gas and sparks (the look's); the fourth's
  fall opens the tender's hold, Rook's Ember launches and the escort swap begins
  ([wingmen](../../../player/wingmen/README.md#rooks-rescue-and-return-l29l30)).

## Concept art

Production frames (round 62, step ART-B, `tools/art/belt_units.py clamp`) live in the level's
[concept/](../../../campaign/act-5-the-belt/level-29-betrayal-at-ceres/concept/prompts.md) (`clamp-gun-final-r62-a`).

## Implementation

- [x] Stat block loaded from a `data.yaml`; global difficulty multipliers applied (M9 part B, step B2)
- [x] The holders of Level 29's held arena (its group `clamps`), the clock standing while one stands (steps B1, B2)
- [x] Production sprites, the clamps springing open and the gas, the shot's sound (step B4) (`tools/art/belt_units.py`: `Level29LooksTest.theArtIsWhatTheLooksDraw` and `theMuzzlesAndJawsComeFromThePivots`; the shot and the clamp's release as round 62's picks (both **b**), `Level29SoundsTest`; no dormant frame: the gun draws its turret while asleep; the art and the sounds accepted in round 62)

## Decisions

- 2026-10-28 (M9 part B, step B2): a unit data file instead of a level-owned block (gap check §4, pending round 62);
  `medium` tier, bounty 10 as the README's (below the medium class's 18–30: `BalanceTest` lists it pending the user's
  review in round 62); the turret turns 120°/s all round; hard's aimed pairs are the `burst: 2` hook.
- 2026-10-28 (M9 part B, step B3): the bounty 10 is Level 29's approved credit budget (round 60), so `BalanceTest`
  lists it as accepted, no longer pending: at 18 the typical haul would be 1.7 % higher, inside its ±5 %.
- 2026-10-28 (M9 part B, step B3b, pending the user's review in round 62): **250 HP** (was 80) and asleep until the
  halt (Level 29's `dormant_until: 119.98`): at 80 HP they fell to the beam and the pods as they scrolled in from
  t≈115.5, so the halt lasted 0–2 s against the README's ≈ 14 s; now 13.0 s on average at medium with the plan's fit
  (easy 7.6, hard 19.2). 2.1 s at the reference is above the medium class's 0.4–1.5 s: `BalanceTest` and
  `tools/balance.py` list it as accepted with the level's reason (the four hold the halt).
- 2026-10-28 (user, [round 62](../../../concept-rounds/round-62/README.md), closed; page answers): the art (F.6) accepted as final; the shot **b** (S.6) and the clamp's release **b** (S.7) picked (the a files in the sfx part's `concept/rejected/`); C.2 accepted: asleep until the halt, 250 HP.
