---
title: Tesla Coil Pod
design: approved
implementation: done
art: final
depends-on: [.., ../../generator, ../../../enemies, ../../../systems/economy]
updated: 2026-10-28
---

# Tesla Coil Pod

## Summary

The wing pod that brings the `shield-breaker` trait in Act 5: a short-range electric arc that jumps
from the pod to the nearest enemy, scatters sparks round it (`area`) and empties an Ascendancy hex
shield at once. It is the shop's answer to "shields on enemies" at Ceres (L29), strongly rewarded
only from Act 6 (L37), where the shields grow heavy. Draft for [round 60](../../../concept-rounds/README.md),
pending the user's review (Q1 = a, Q12 = c); its numbers are in [data.yaml](data.yaml) since M9 part A.

## Design

<!-- data: weapon-properties -->
| Property | Value |
|---|---|
| Slot | Wing mount (per pod) |
| Traits | `area`, `shield-breaker` |
| Layers hit | `air`, `low-air` and `ground` (hardened ground targets take it only with `anti-ground`, which it has not: the arc glances off them); not `high-air` (an arc needs a body in reach, not a lock), not the `sub` layer ([layer rules](../../../enemies/README.md#layer-rules)) |
| Price | 3 000 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 1 500 / 3 000 / 6 000 / 12 000 cr (sell-back per [economy](../../../systems/economy/README.md)) |
| Unlock | L29 (in the shop from the hangar visit before it) |
| Projectile | None: an instant arc from the pod to its target (it hits on the frame it fires) within 140 → 180 px in a 240° arc that opens forward |
| Sound family | `tesla` ([weapon sound families](../../../audio/sfx/README.md#weapon-sound-families)) |
| Projectile family (VFX) | `arc` (new in round 60, a jagged blue-white arc with a violet fringe, three shapes in turn, and a spark burst at the target) |
<!-- /data -->

It is in the shop from the L29 visit: captured CDF stock at Ceres. The arc is drawn for 0.08 s; its
sound family `tesla` is the round-03 zap, already final.

### Per level

<!-- data: weapon-levels -->
| Level | Arcs | Pattern | Rate /s | Damage | Volley DPS | Single-target DPS | Extra | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 1 | arc to the nearest | 4 | 3 | 12.0 | 12.0 | reach 140 px, sparks 24 px | 2 | — |
| 2 | 1 | arc to the nearest | 4 | 4.25 | 17.0 | 17.0 | reach 150 px, sparks 24 px | 2.5 | 1 500 |
| 3 | 1 | arc to the nearest | 5 | 4.6 | 23.0 | 23.0 | reach 160 px, sparks 28 px | 3 | 3 000 |
| 4 | 1 | arc to the nearest | 5 | 6.2 | 31.0 | 31.0 | reach 170 px, sparks 32 px | 3.5 | 6 000 |
| 5 | 1 | arc to the nearest | 5 | 8 | 40.0 | 40.0 | reach 180 px, sparks 36 px | 4 | 12 000 |
| OD | 1 | arc to the nearest | 6 | 8 | 48.0 | 48.0 | reach 190 px, sparks 40 px | — (ignores cap) | — |

Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`; do not edit by hand. Values are per pod. Single-target DPS counts every arc (an arc always strikes a target inside its reach). The sparks are the `area`: 0.5 of the arc's damage on every other unit within the sparks' radius of the struck one (not on the struck target), counted in neither DPS column. OD = the overdrive pattern (20 s pickup).
<!-- /data -->

DPS L1 and L5 are the roster's (12 / 40); L2–L4 ours. The 36 px target 100 px away is inside the
reach from L1.

### Behaviour

- **Target pick:** each pod fires at the nearest valid enemy within its reach in a 240° arc that
  opens forward (everything but a 120° wedge straight behind the ship). A **shielded** enemy in
  reach is picked first (the nearest of them), so a pair of pods finds the shields in a mixed wave
  by itself. With no target in reach the pod does not fire (no arc into empty space, no draw saved).
  The two pods pick independently and may strike the same target. A
  [Minelayer](../../../enemies/space/minelayer/README.md)'s mine is never picked (as homing shots skip
  mines), but the sparks set off a mine inside their radius.
- **Homing diamonds** (QA-1, decided 2026-10-27, confirmed in round 61 on 2026-10-28): the arc may strike an enemy homing
  missile (a [homing diamond](../../../art-direction/README.md#implementation)) **only when no enemy unit is in reach**;
  a unit, shielded or not, is always picked first.
- **`shield-breaker`:** an arc that strikes a unit with a shield empties the shield at once and
  holds its regrowth back; the shield model, its numbers and the same effect of the EMP Burst are
  defined once in the enemies' [enemy shield rule](../../../enemies/README.md#enemy-shields). Every
  other weapon still wears a shield down hit by hit; the pod only makes it quick.
- **Damage on a shield:** the arc's own damage then goes to the hull (the shield is gone in the same
  hit), so against a shielded unit the pod is worth far more than its DPS column.
- **Short reach** is its price: 140 → 180 px is about a third of the play field's height, so the
  pod rewards flying close (the Talons' V-wings, the Gunships' flanks) and does nothing at the long
  range a beam or a missile covers.
- Its arc passes over walls and rocks (it strikes what it picked; a drifting rock in between is
  no cover). It never chains to a second target: chaining is the Plasma Arc's (Act 6).
- **Power:** 2 → 4 MW for 12 → 40 DPS on one pod, about the Micro-missile Pod's DPS a MW at L1 and
  above it at L5; see the [Act 5 hand plan](../../generator/README.md#act-5-hand-plan-draft), whose
  typical player keeps the Micro-missile Pods in Act 5 (the shields are light) and fits the pods only
  when Act 6's heavy shields arrive.

## Concept art

Production art for concept round 61 (M9 part A, step ART-A), built from the frames by `tools/art/act5_gear.py`
(`arc`, `pod`; `--review` rebuilds only the sheets). Round 60 showed no arc, so its look is an a/b proposal; a is
written to the game's files until the pick. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/tesla-arc-final-r61-a.png](concept/tesla-arc-final-r61-a.png) | The arc, proposal **a** "blue-white" (recommended: the player's fire stays cool): three jagged 128×24 strips (white core, blue-white glow, violet fringe, short forks) stretched pod → target, the 72 px spark burst, the terminal glow; in flight over belt space, shielded stand-ins picked first (sheet) | chosen (round 61, F.1 a picked: the blue-white arc, in the game) |
| [concept/tesla-arc-final-r61-a.gif](concept/tesla-arc-final-r61-a.gif) | a: two L1 pods arcing at round 60's Talon and Hornet stand-ins, shields shattering, 2x | chosen (round 61, F.1 a picked: the blue-white arc, in the game) |
| [concept/tesla-coil-pod-final-r61-a.png](concept/tesla-coil-pod-final-r61-a.png) | The wing pod on every banking frame (two copper coil rings, a white ceramic insulator, the glowing terminal), beside the Micro-missile and Deflector pods (sheet) | chosen (round 61, F.1: accepted) |
| [concept/tesla-coil-pod-final-r61-a.gif](concept/tesla-coil-pod-final-r61-a.gif) | The ship banking with the pods, the terminals lighting as they arc, 3x | chosen (round 61, F.1: accepted) |
| [concept/tesla-coil-pod-capture-r61-a.png](concept/tesla-coil-pod-capture-r61-a.png) | Game capture, M9 part A (step A4; Level 08 on medium, `--invulnerable`, `--loadout left=tesla-coil-pod:3,right=tesla-coil-pod:3,rear=swarm-tail:2`, the ship steered up among the waves): four play-field frames with an arc striking (a Mantis, a Spore Bomber, a Stinger; at L3 the 160 px reach means flying close), the Swarm Tail's missiles behind the ship | chosen (round 61, F.1: accepted) |
| [concept/tesla-coil-pod-capture-r61-a.mp4](concept/tesla-coil-pod-capture-r61-a.mp4) | The same run with the game's sound (level seconds ≈ 3.5–43.5): the zap and the arcs, the sparks, the Swarm Tail launching backwards | chosen (round 61, F.1: accepted) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [x] Weapon data in [data.yaml](data.yaml) (numbers as above; a new `ARC` delivery: instant, a reach, the
  forward arc, the shielded-first pick) — M9 part A (step A1: the simulation's `Delivery.ARC`, `TeslaArcTest`; step A2:
  `hits: arc`, the levels' `reach`, `WeaponData`/`WeaponRules`, the tables rendered by `tools/sync_tables.py`,
  `BeltKeysTest.theTeslaCoilPodArcsFromItsPodWithinItsReachAndBreaksShields`)
- [x] The `shield-breaker` hit on an enemy shield (the shield model of [enemies](../../../enemies/README.md#enemy-shields))
  and the sparks' `area` share on the others — M9 part A (step A1: `EnemyShieldTest`, `TeslaArcTest`; step A2: the trait
  mapped, `WeaponData.shieldBreaker`)
- [x] Shop entry from the L29 visit — M9 part A (step A2: `unlock: 29`, `BeltCampaignTest.theShopSellsAct5sItemsFromTheirVisits`)
- [x] The hangar's ◆ trait marker for `shield-breaker` — M9 part A (step A3: the shop row's ◆ where the next level's
  intel recommends the trait, the row's name and the detail's trait line fitting, `ActFiveHangarTest`)
- [x] The arc's look (`arc` family) and the spark burst; the `tesla` sound per arc at the level's pitch — M9 part A
  (step A3: `ArcLooks`, the arc stretched from the pod's terminal to the struck point for 5 steps, the three shapes in
  turn and the terminal's light; the spark burst as the `arc` family's impact; the coil pod on the wing; the zap with
  each arc's report, played as the other families at pitch 1 (the rear slot's lower pitch does not apply to a wing);
  `ActFiveLooksTest`, `ActFiveSoundsTest`, `TestFireTest`; seen on Level 08 with `--loadout`; the arc's colour a was picked in
  round 61)

## Open questions

- Q1 of [round 60](../../../concept-rounds/README.md) (enemy shields): recommended (a), a regrowing
  shield every weapon wears down and this pod empties at once (this document). Alternatives: (b)
  shields that only `shield-breaker`, beams and the EMP get through (the pod then becomes a must-buy
  at L29, against the campaign's L37 pacing), or (c) no shield mechanic in Act 5 (the pod then sells
  only its sparks until Act 6).
- Q12: the pod is in every variant of Act 5's arsenal; only the number of other new items changes.

## Decisions

- 2026-10-27 (Act 5 design phase, step m9-d-systems; **approved in round 60**):
  promoted from the weapons roster with hand-written numbers (12 / 40 DPS and 2 → 4 MW kept, 3 000 cr,
  L2–L4 ours, an instant arc of 140 → 180 px instead of a projectile, sparks at half damage, the
  shielded-first pick); the `tesla` sound family it already had.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`, D.14); Q12 **c**. **QA-1** (the main agent's decision of 2026-10-27 from the part A gap check, confirmed in round 61 on 2026-10-28): the arc may lock onto a homing diamond only when no enemy unit is in reach.
- 2026-10-28 (user, [round 61](../../../concept-rounds/round-61/README.md), closed): **F.1 a**, the blue-white arc (b, the white-gold, rejected: `concept/rejected/`); the pod accepted; **C.1** (QA-1: the arc strikes a homing diamond only when no enemy unit is in reach) and **C.4** (never a hardened unit, boss parts yes, no chaining, sparks ignore mines) confirmed; the shield-breaker's 3 s hold (C.3) accepted.
