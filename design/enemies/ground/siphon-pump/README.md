---
title: Siphon Pump
design: review
implementation: done
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../campaign/act-4-deep-water/level-23-through-the-ice]
updated: 2026-10-22
---

# Siphon Pump

## Summary

A Vrell pump fixed on Thera Deep's kelp farm racks, draining the farms into the Vrell: a teal pump tube with a glowing
sump on the sea floor. Level 23's secondary target: no attack, 40 HP, five of them on the racks, the player who bought
the torpedoes breaks them fastest.

## Design

### Stat block

The approved text of [Level 23](../../../campaign/act-4-deep-water/level-23-through-the-ice/README.md#ground-targets)
(round 48: "a teal pump tube with a glowing sump, 40 HP, no attack", bounty 20) promoted to a part of its own in M8 part
C (step C2), because the secondary's `escapes` counts a unit by its slug and the loader finds units only here (gap check
`m8c-gaps.md` §5 D3). Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)), from its
[data.yaml](data.yaml) (the table is rendered from it); 40 HP is 0.4 s at the Level 23 reference DPS of 100.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `ground` (the sea floor: a farm rack) |
| Size tier | `medium` |
| Size | 40×40 px, hitbox 32×32 |
| Parts | single |
| Orientation | `fixed` (seen from above; the sump glows, the tube pulses) |
| HP | 40 (easy 30 / hard 52, from the global multipliers) |
| Armour / shield | none |
| Speed | 0 px/s (scrolls with the ground) |
| Movement | `terrain` (fixed on its farm rack) |
| Attack | `none`: it drains the farm into the Vrell; a hum while it lives |
| Formations | turret nest (one on each farm rack) |
| Weak points | none (dies in 0.4 s at the Level 23 reference) |
| Effective traits | `anti-sub`, `anti-ground` |
| Credits | 20 (score 200 × chain) |
| Death | `small` burst: the tube breaks, the sump's glow gutters out, the kelp round it slackens |
| First level / used in | L23; Level 23 (the secondary `PUMPS`); a later Europa level may reuse it |
| Difficulty hooks | none (the secondary's allowed escapes change: easy 2, medium 1, hard 0) |
<!-- /data -->

### Behaviour

- **A ground target on the sea floor**: it scrolls with the farm rack it is fixed on and never moves or fires. Every
  weapon that reaches `ground` hits it; under water the torpedo weapons hit it at full damage, every other weapon at
  × 0.7 (after `anti-ground`'s × 2 for bolts): Europa's [under water rules](../../../world/europa/README.md#under-water-rules),
  no rule of its own.
- **The secondary** (Level 23, `PUMPS`): the level's `escapes: siphon-pump` counts each pump that leaves the screen
  alive; the secondary is met when the fourth falls and failed by the second away (easy: the third, hard: the first).
- **Readability**: the sump glows teal and pulses with the hum while it lives (presentation, the sound list's hum
  loop); its break is the tube snapping and the glow guttering out.

### Difficulty and level use

- No difficulty hook: the same hits on every difficulty; the secondary's allowed escapes change instead (easy 2,
  medium 1, hard 0).
- Level 23 only for now; a later Europa level may reuse it.

## Implementation

- [x] Stat block in a `data.yaml`, loaded and checked (M8 part C, step C2)
- [x] The five pumps on Level 23's farm racks counted by the secondary (`escapes: siphon-pump`, step C3)
- [x] Production sprite, its hum loop and its break (steps ART-C, AUD-C and C4; round 52: `Level23LooksTest.theArtIsWhatTheLooksDraw`,
      `Level23SoundsTest.theWaterlineTheKillsTheEelsAndThePumpsSound`)

## Decisions

- 2026-10-22 (M8 part C, step C2; gap check `m8c-gaps.md` §5 D3, accepted in round 52 (2026-10-22)): promoted
  from Level 23's ground targets to its own part (the loader and the secondary need a unit), its text Level 23's
  approved one; the 40 × 40 sprite with a 32 × 32 hit box, the fixed orientation and the formation `turret nest` (one
  on each rack) decided here. Its tier is `medium` (the gap check said `small`): the approved 40 HP (0.4 s at the
  Level 23 reference) and bounty 20 lie in the medium class of the balancing basis, outside the small one (≤ 0.3 s,
  bounty 10–15), so `BalanceTest` holds without a new accepted deviation; its effective traits `anti-sub` (the torpedo
  weapons at full damage under water) and `anti-ground` (× 2).
- 2026-10-22 (M8 part C, step C3): mapped and flown: the five pumps on Level 23's racks counted by the `escapes` secondary (`PUMPS`, 1 away allowed at medium, 2 on easy, none on hard); the autopilot breaks a pump low on the screen first (`WaterPlay`).
- 2026-10-22 (M8 part C, steps ART-C, AUD-C and C4; accepted in round 52 (2026-10-22)): the final frames
  (`tools/art/siphon_pump.py`, from the Thera Deep mood still: the heave loop, the sump's glow, the dead pump, the break,
  the root decal live and withered); one quiet `pump-hum` loop for all the pumps on the screen (as the Drill Rig's
  collar), `pump-break` a few dB under its under-water `medium` burst; the a files in the game until round 52's picks.
  Captured in Level 23 (`farms-capture-r52-a`).
- 2026-10-22 (user, round 52, closed): the final frames accepted (F.4, `art: final`); the hum **b** and the break **b** picked (in the game since step D4); C.7 (the secondary 3 / 4 / 5, the part's stats) accepted.
