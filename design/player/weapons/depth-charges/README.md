---
title: Depth Charges
design: approved
implementation: in-progress
art: final
depends-on: [.., ../torpedo-pod, ../../generator, ../../../world/europa, ../../../systems/economy]
updated: 2026-10-24
---

# Depth Charges

## Summary

The rear-slot `anti-sub` weapon of Act 4: barrels that the Stormhawk tosses forward from its stern
rack over the hull (M8 AW, the user's playtest decision of 2026-10-23: they were rolled out behind
it), that hang about 200 px ahead, sink with the water and burst after a short fuse or on touching a
swimmer, hitting everything near the burst. It clears the swarms ahead and a Lamprey on the hull,
and it keeps its full damage under the ice where the Tail Gun loses 30 %. Decided in round 48 (2026-10-22); its numbers are in [data.yaml](data.yaml) since M8
part A (`hits: depth-charge`, a delivery of its own).

## Design

<!-- data: weapon-properties -->
| Property | Value |
|---|---|
| Slot | Rear gun |
| Traits | `anti-sub`, `rear`, `area` |
| Layers hit | Under water: the burst hits everything inside its radius in the water, at full damage (the play plane `air`, `low-air`, the sea floor `ground`, and the `sub` layer below); never `high-air`. Above water: the same toss, bursting on its fuse only (nothing swims there) on the `sub` layer under the surface, hitting `sub` and naval `ground` in a level with `water: true`; idle with `NO WATER` in a dry level |
| Price | 1 300 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 400 / 800 / 1 600 / 3 200 cr (sell-back per [economy](../../../systems/economy/README.md)) |
| Unlock | L22 (in the shop from the hangar visit before it) |
| Projectile | 380 px/s tossed forward from the stern rack over the hull, slowing to a hover over 1 s, then sinking with the scroll, 12×12 px, a burst on its fuse (1.4 → 1.2 s) of 48 → 64 px radius |
| Sound family | `depth-charge` ([weapon sound families](../../../audio/sfx/README.md#weapon-sound-families)) |
| Projectile family (VFX) | `depth-charge` (the drum and its pressure-dome burst of concept round 48, G2 a) |
<!-- /data -->

It is usable from the waterline of L23. Its sound family `depth-charge` is a heavy, low thud under
water, no bubbles (round 48 proposals).

### Per level

<!-- data: weapon-levels -->
| Level | Charges | Fuse | Volleys /s | Damage | Volley DPS | Single-target DPS | Burst radius | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 1 | 1.4 s | 1 | 28 | 28.0 | 28.0 | 48 px | 2 | — |
| 2 | 1 | 1.4 s | 1.1 | 34 | 37.4 | 37.4 | 52 px | 2.5 | 400 |
| 3 | 1 | 1.3 s | 1.2 | 42 | 50.4 | 50.4 | 56 px | 2.5 | 800 |
| 4 | 1 | 1.3 s | 1.3 | 50 | 65.0 | 65.0 | 60 px | 3 | 1 600 |
| 5 | 2 | 1.2 s | 0.8 | 52 | 83.2 | 83.2 | 64 px | 3 | 3 200 |
| OD | 2 | 1.2 s | 0.9 | 56 | 100.8 | 100.8 | 68 px | — (ignores cap) | — |

Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`; do not edit by hand. A charge's damage is dealt once to every target inside its burst radius (a boss part at most one burst a charge), so against a group the volley counts more than the single-target DPS. OD = the overdrive (20 s pickup).
<!-- /data -->

Since M8 AW the levels are 28 → 52 damage on a 1.4 → 1.2 s fuse with a 48 → 64 px burst (OD 68), 28 →
83 DPS (it was 25 → 80 on 0.9 → 0.7 s and 40 → 64 px; L2–L4 ours then, as now). L5's two charges leave
8 px either side of the stern rack, fanned 8° apart either way. A charge's
damage is dealt once to every target inside the radius (so against a school the volley counts more
than the single-target DPS above); a boss part takes at most one burst per charge.

### Behaviour

- **Redefined from the roster** ("sink into the `sub` layer"): under water the play plane *is* the
  water, so the charges no longer change layer. **Tossed forward** (M8 AW): a charge leaves the stern
  rack at 380 px/s over the hull, its toss decaying over 1 s to a hover about 200 px ahead, then sinks
  with the scroll; the pressure wave is a circle on the play plane that also reaches the layers above
  and below it (the sea floor, the `sub` layer, never the ice ceiling). Swarms coming down the
  screen and units rising from below (the Abyss Ray's rise) run into them.
- It bursts on its fuse **or on touching a swimmer** (M8 AW): its 12×12 px barrel touching a unit or
  a boss's part (or body) on the play plane (`air`, `low-air`) in the water. A Lamprey latched on the
  hull counts while the barrel crosses the hull, so a charge tossed with one on the hull bursts on it
  at once (the rear gun's hit on a latched unit). Sea-floor and `sub` units are reached by the burst
  only. It never hurts the player or Rook (the burst is Stormhawk-safe, a pressure wave tuned to the
  hull). The barrels are drawn, hit by nothing and block nothing.
- Water range rule: torpedo-type range +50 % widens the toss; bullet speed × 0.7 applies to it too
  (380 × 1.5 × 0.7 = 399 px/s, about 200 px to the hover); the fuse and the burst radius are not scaled
  (they are the weapon's balance).
- **Above water** the same toss, bursting on its fuse only (nothing swims there) on the `sub` layer
  under the surface (hitting submerged units that a plain shot cannot reach); the pod rules apply
  (`NO WATER` in a dry level, still drawing power).

## Concept art

Proposals: concept round 48 (step A-gear, listed in the [weapons' Concept art
table](../README.md#concept-art); prompts in [../concept/prompts.md](../concept/prompts.md#depth-charges-r48-a),
generator `tools/concept/gear_r48.py`), pending the user's review: [a "drum"](../concept/depth-charges-r48-a.png) ([GIF](../concept/depth-charges-r48-a.gif)), a navy drum with yellow bands and a white fuse lamp blinking faster, a pressure-dome burst (flash disc, dark cavity, rim ring); and [b "finned canister"](../concept/rejected/depth-charges-r48-b.png) ([GIF](../concept/rejected/depth-charges-r48-b.gif)), a grey canister with cross fins and a closing cyan fuse ring, a double shock ring that refracts the floor. Both are shown under the
round-48 under-water grade (Level 24) with stand-in targets.

| File | What | Status |
|---|---|---|
| [concept/depth-charges-final-r50-a.png](concept/depth-charges-final-r50-a.png) | Depth Charges final frames (`tools/art/act4_gear.py charges`, round 48 G2 = a): the drum's 16 spins in three sinking stages, the fuse lamp, the pressure-dome bursts at 40 / 52 / 64 px and the dark cavity; roll-out, hover, sink and burst behind the ship (sheet) | chosen (round 50, F.2: accepted as final) |
| [concept/depth-charges-final-r50-a.gif](concept/depth-charges-final-r50-a.gif) | Two L3 charges rolling out of the stern, blinking faster and bursting at 52 px, 15 fps at 2x | chosen (round 50, F.2: accepted as final) |
| [concept/depth-charges-capture-r50-a.png](concept/depth-charges-capture-r50-a.png) | Game capture, Level 11 above water: the charges rolling off the stern and a burst on the `sub` layer by the Driftjelly field (a frame) | chosen (round 50, F.2: accepted) |
| [concept/depth-charges-capture-r50-a.mp4](concept/depth-charges-capture-r50-a.mp4) | Game capture with the game's sound: the roll-outs, the blinking lamps and the bursts on Level 11's submerged jellies | chosen (round 50, F.2: accepted) |

Prompts: [concept/prompts.md](concept/prompts.md).

## Implementation

- [x] Weapon data in [data.yaml](data.yaml) (numbers as above; M8 part A, step A2: `hits: depth-charge` with a `fuse` and a burst `blast` per level, loaded by `WeaponData`, the tables rendered by `tools/sync_tables.py`; the barrel's 12×12 px box is ours)
- [x] A rear delivery: barrel roll-out, fuse, a circular burst that hits the water's layers; safe for the ship and Rook — M8 part A (steps A1/A3/A4: `DepthChargeTest`, `Act4ArsenalFlightTest.theDepthChargesBurstOnWhatPassesBehindTheShip`; reworked in M8 AW: `theDepthChargesBurstOnWhatIsAheadOfTheShip`)
- [x] Idle with `NO WATER` in a dry level; the above-water drop on the `sub` layer — M8 part A (`DepthChargeTest.dryItReadsNoWaterAndTossesNothing`, `DepthChargeTest.aboveWaterItBurstsOnItsFuseOnlyOnTheSubLayerAndTheNavalGround`)
- [x] Power draw per level, upgrade prices, shop entry from the L22 visit; the balance plan's purchases — M8 part A (`ActFourDataTest.theDepthChargesBurstOnAFusePerLevel`, `BalanceTest`, `ActFourHangarTest.theActFourItemsAreInTheLiveShopWithTheirNames`; the hangar's test range: `ActFourHangarTest.theDepthChargesTestRangeIsWaterWithSubmergedDummiesThatOneBurstDestroys`)
- [x] M8 AW (the user, 2026-10-23): tossed forward about 200 px, a burst on the fuse or on touching a swimmer (a latched Lamprey included), never the ship; the new numbers — step AW1 (`DepthChargeTest.underWaterItIsTossedForwardHoversAboutTwoHundredPxAheadSinksAndBurstsOnItsFuse`, `underWaterItBurstsOnTouchingASwimmerAndItsBurstHitsEveryLayerInTheWaterButTheIceCeiling`, `aChargeTossedWithALampreyOnTheHullBurstsOnItAtOnceAndNeverHitsTheShip`, `ActFourDataTest.theDepthChargesBurstOnAFusePerLevel`, `Act4ArsenalFlightTest.theDepthChargesAreABarrelTossedForwardOnAFuse`)
- [ ] M8 AW: the toss's look from the stern rack over the hull and its toss thump (step AW3, round 56)
- [x] Sprites, burst VFX and sounds (round 48) — M8 part A (step A6: `DeepWaterLooksTest.theDrumSinksInThreeStagesAndSpins`, `theFuseLampBlinksFasterTowardsTheBurst`, `theBurstMatchesTheLevelsRadius`, `ActFourSoundsTest.aDepthChargeRollsOutAndBurstsOnItsFuse`; the finals accepted and the sounds picked in [round 50](../../../concept-rounds/round-50/README.md))

## Open questions

- None of its own: whether the roster's wording ("sink into the `sub` layer") is dropped is part of
  Q1 of [round 48](../../../concept-rounds/README.md) (recommended: yes, as above).

## Decisions

- 2026-10-21 (Act 4 design phase, step m8-dsystems; **decided in round 48 (2026-10-22)**):
  promoted from the weapons roster and **redefined** as a rear launcher of sinking charges that burst
  on the play plane (Q1 = b; the roster's "sink into the `sub` layer" is meaningless when the play
  plane is the water); numbers hand-written (25 / 80 DPS kept, L2–L4 ours).
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-22 (M8 part A, step A2): the numbers in [data.yaml](data.yaml) (`hits: depth-charge`, a delivery of its
  own: `speed` 140, `drift` 0.6, per level a `fuse` and the burst `blast`; L5's two charges 8 px either side; the
  barrel's 12×12 px box ours, accepted in round 50 (2026-10-22)); the tables are rendered from it (L4's 65.8 DPS
  where the hand-written table said 66).
- 2026-10-22 (M8 part A, steps A1–A6; accepted in round 50 (2026-10-22)): the roll-out under water is 140 × 1.5 (range) × 0.7 (speed) = 147 px/s, the fuse counts from the launch, L5's two charges ±8 px apart, hardened ground glances off as from the mines (D12); at most 8 charges live (`max_live`'s default); a charge sinking off the field is lost, so charges dropped with the ship at the bottom edge roll off the screen before they burst.
- 2026-10-22 (user, [round 50](../../../concept-rounds/round-50/README.md) closed): F.2: the final art (`tools/art/act4_gear.py charges`) and its game capture accepted; `art: final`, the concept rows `chosen`. B.7 accepted: a charge that leaves the field before its fuse is lost (no burst at the bottom edge). Its sounds (in [sfx](../../../audio/sfx/README.md)): roll-out b, burst a.
- 2026-10-24 (M8 AW, step AW1; decided by the user 2026-10-23 after playing L22–25: "thrown forward, sinking ahead
  and bursting on their fuse or on touching a swimmer, hitting swimmers, ground and `sub`"): the numbers of the
  rework's plan in [data.yaml](data.yaml): the pattern's angle 180 → 0 (L5/OD fanned ±8°), `speed` 140 → 380, `drift`
  0.6 → 1.0 (≈ 200 px ahead), rate 1 / 1.1 / 1.2 / 1.3 / 0.8 / OD 0.9, damage 28 / 34 / 42 / 50 / 52 / 56, blast 48 /
  52 / 56 / 60 / 64 / 68 px, fuse 1.4 / 1.4 / 1.3 / 1.3 / 1.2 / 1.2 s; slot, prices, draw and the 8 live charges
  unchanged. Defaults accepted in round 56 (2026-10-24): the contact counts a latched unit (while the barrel
  crosses the hull: its burst covers every latch spot) and a set piece's body or living part on `air`/`low-air`; the
  charges keep the rear slot.
- 2026-10-24 (user, round 56 closed): the rework's numbers (C.2), the latched Lamprey and the bosses (C.6) and the
  toss's look (A.1) accepted. After the playtest of Levels 22–26 with the rework ("The sound for the depth charges
  explosions is too weak. Those are supposed to be large explosions, but they hardly stand out") the **burst** plays the
  under-water family's large explosion at full gain, its own six instances so a volley's pair is heard whole; round 50's
  burst a stays chosen in the sfx part, unused ([sfx](../../../audio/sfx/README.md#decisions)).
