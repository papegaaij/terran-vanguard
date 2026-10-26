---
title: Relay Beacon
design: review
implementation: in-progress
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../campaign/act-4-deep-water/level-27-hydrothermal-vents]
updated: 2026-10-25
---

# Relay Beacon

## Summary

A human-built relay mast on Europa's sea floor, the same red-gold pulsing beacons that stand as scenery along the
Vrell route in Levels 24–26 and carry the Helix handshake. Level 27's secondary target: no attack, 60 HP, four of them
on the Beacon Ridge; three must fall.

## Design

### Stat block

The approved text of [Level 27](../../../campaign/act-4-deep-water/level-27-hydrothermal-vents/README.md#ground-targets)
(round 48: `ground`, not hardened, draft 60 HP, a red-gold pulse every 4 s, bounty 15, a destroyed beacon goes dark)
promoted to a part of its own in M8 part G (step G2), because the secondary's `escapes` counts a unit by its slug and the
loader finds units only here (as the [siphon pump](../siphon-pump/README.md); gap check `m8g-gaps.md` §1.4). Numbers at
**medium** (see the [balancing basis](../../README.md#balancing-basis)), from its [data.yaml](data.yaml) (the table is
rendered from it); 60 HP is 0.5 s at the Level 27 reference DPS of 116.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Unmarked |
| Layer | `ground` (the sea floor: a mast on the beacon ridge) |
| Size tier | `medium` |
| Size | 72×104 px, hitbox 36×80 |
| Parts | single |
| Orientation | `fixed` (seen from above; its lamp pulses red-gold every 4 s) |
| HP | 60 (easy 45 / hard 78, from the global multipliers) |
| Armour / shield | none |
| Speed | 0 px/s (scrolls with the ground) |
| Movement | `terrain` (a mast fixed on a ridge crest) |
| Attack | `none`: a human-built relay with the same Helix handshake as the Depth Hunters' hulls; a low hum and a red-gold pulse every 4 s while it lives |
| Formations | turret nest (one on each ridge crest) |
| Weak points | none (dies in 0.5 s at the Level 27 reference) |
| Effective traits | `anti-sub`, `anti-ground` |
| Credits | 15 (score 150 × chain) |
| Death | `small` burst: the lamp breaks and the mast goes dark (a dark frame that stays) |
| First level / used in | L27; Level 27 (the secondary `BEACONS`); scenery in Levels 24–26 |
| Difficulty hooks | none (the README's hard 90 HP has no key: the global multiplier gives 78, accepted in round 57 (2026-10-25)) |
<!-- /data -->

### Behaviour

- **A ground target on the sea floor**: it scrolls with the ridge crest it stands on and never moves or fires. Every
  weapon that reaches `ground` hits it; under water the torpedo weapons hit it at full damage, every other weapon at
  × 0.7 (after `anti-ground`'s × 2 for bolts): Europa's [under water rules](../../../world/europa/README.md#under-water-rules).
- **The secondary** (Level 27, `BEACONS`): the level's `escapes: relay-beacon` counts each beacon that leaves the screen
  alive; met when the third falls, failed by the second away (the same on every difficulty).
- **Readability**: its lamp pulses red-gold every 4 s while it lives (presentation; a low hum); destroyed, it breaks and
  its dark frame stays. Destroying the beacons has no effect on the waves (gap check §5 G13: no dimming of the Vrell's
  wave lights).

### Difficulty and level use

- No difficulty hook: the global HP multipliers apply (easy 45, hard 78). The Level 27 draft's hard 90 HP has no
  stat-block key (accepted in round 57 (2026-10-25)).
- Level 27 only; in Levels 24–26 the beacons are scenery pieces (`relay-beacon` in their backdrops).

## Concept art

ART-G's production frames (`tools/art/relay_beacon.py`, Levels 24–26's mast as Level 27's target) are listed in the
[ground](../README.md#concept-art) Concept art table (`relay-beacon-final-r57-a`), accepted in round 57 (2026-10-25).

## Implementation

- [x] Stat block in a `data.yaml`, loaded and checked (M8 part G, step G2; `ContentLoaderTest`, `BalanceTest`)
- [x] The four beacons on Level 27's ridge counted by the secondary (`escapes: relay-beacon`, step G2's data;
  `Level27Test.theFourBeaconsAndTheirSecondary`)
- [x] Production sprite (pulse, dark frame, break), its hum and its break sound (steps ART-G, AUD-G and G4; proposed in
  round 57: `BeaconLooks`, `VentFieldCues`; its death the `small` under-water burst)

## Decisions

- 2026-10-24 (M8 part G, step G2; gap check `m8g-gaps.md` §1.4, accepted in round 57 (2026-10-25)): promoted from Level 27's ground
  targets to its own part (the loader and the secondary need a unit), its text Level 27's approved one; ART-G's 72 × 104
  sprite with a 36 × 80 hit box, the fixed orientation and the formation `turret nest` (one on each ridge crest); tier
  `medium` as the siphon pump; effective traits `anti-sub` and `anti-ground` as the siphon pump; hard's HP from the
  global multiplier (78) instead of the draft's 90 (no stat-block key for it).
- 2026-10-25 (M8 part G, step G5; accepted in round 57 (2026-10-25)): captured shot down on Level 27's ridge (the
  red lamp, the gold pulse, the break, the dark mast; `BEACONS DONE` at t≈110 in a steered run); the round asks the art
  (F.3) and its bounty 15 and hard HP 78 (C.3).
- 2026-10-25 (user, [round 57](../../../concept-rounds/round-57/README.md) closed): the final frames accepted as final; the part is `art: final`; its 60 HP (78 on hard) and
  bounty 15 accepted (C.3); sounds: the hum **a**, the break **b** (the b pick goes into the game in step H5).
