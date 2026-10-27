---
title: Tesla Coil Pod
design: draft
implementation: not-started
art: none
depends-on: [.., ../../generator, ../../../enemies, ../../../systems/economy]
updated: 2026-10-27
---

# Tesla Coil Pod

## Summary

The wing pod that brings the `shield-breaker` trait in Act 5: a short-range electric arc that jumps
from the pod to the nearest enemy, scatters sparks round it (`area`) and empties an Ascendancy hex
shield at once. It is the shop's answer to "shields on enemies" at Ceres (L29), strongly rewarded
only from Act 6 (L37), where the shields grow heavy. Draft for [round 60](../../../concept-rounds/README.md),
pending the user's review (Q1 = a, Q12 = c); numbers are hand-written, nothing is in `data.yaml`
yet (M9 part A).

## Design

| Property | Value (draft) |
|---|---|
| Slot | Wing mount (per pod) |
| Traits | `area`, `shield-breaker` |
| Layers hit | `air`, `low-air` and `ground` (hardened ground targets take it only with `anti-ground`, which it has not: the arc glances off them); not `high-air` (an arc needs a body in reach, not a lock), not the `sub` layer ([layer rules](../../../enemies/README.md#layer-rules)) |
| Price | 3 000 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 1 500 / 3 000 / 6 000 / 12 000 cr (the common factors on the price); sell-back per [economy](../../../systems/economy/README.md) |
| Unlock | L29 (in the shop from the hangar visit before it: captured CDF stock at Ceres) |
| Projectile | None: an instant arc from the pod to its target (hits on the frame it fires), drawn for 0.08 s |
| Sound family | `tesla` ([weapon sound families](../../../audio/sfx/README.md#weapon-sound-families); the round-03 zap, already final) |
| Projectile family (VFX) | `arc`, new: a jagged blue-white arc with a violet fringe, three shapes in turn, and a spark burst at the target (round 60 proposals) |

### Per level (draft, hand-written)

| Level | Arcs | Pattern | Rate /s | Damage | Volley DPS | Single-target DPS | Extra | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 1 | arc to the nearest | 4 | 3 | 12.0 | 12.0 | reach 140 px, sparks 24 px | 2 | — |
| 2 | 1 | arc to the nearest | 4 | 4.25 | 17.0 | 17.0 | reach 150 px, sparks 24 px | 2.5 | 1 500 |
| 3 | 1 | arc to the nearest | 5 | 4.6 | 23.0 | 23.0 | reach 160 px, sparks 28 px | 3 | 3 000 |
| 4 | 1 | arc to the nearest | 5 | 6.2 | 31.0 | 31.0 | reach 170 px, sparks 32 px | 3.5 | 6 000 |
| 5 | 1 | arc to the nearest | 5 | 8 | 40.0 | 40.0 | reach 180 px, sparks 36 px | 4 | 12 000 |
| OD | 1 | arc to the nearest | 6 | 8 | 48.0 | 48.0 | reach 190 px, sparks 40 px | — (ignores cap) | — |

DPS L1 and L5 are the roster's (12 / 40); L2–L4 ours. Single-target DPS counts every arc (an arc
always strikes a target inside its reach, and the 36 px target 100 px away is inside it from L1).
The sparks are the `area`: 0.5 of the arc's damage on every other unit within the radius of the
struck one (not on the struck target, as the Harpoon's splash), counted in neither DPS column.

### Behaviour

- **Target pick:** each pod fires at the nearest valid enemy within its reach in a 240° arc that
  opens forward (everything but a 120° wedge straight behind the ship). A **shielded** enemy in
  reach is picked first (the nearest of them), so a pair of pods finds the shields in a mixed wave
  by itself. With no target in reach the pod does not fire (no arc into empty space, no draw saved).
  The two pods pick independently and may strike the same target. A
  [Minelayer](../../../enemies/space/minelayer/README.md)'s mine is never picked (as homing shots skip
  mines), but the sparks set off a mine inside their radius.
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

## Implementation

- [ ] Weapon data in `data.yaml` (not yet written) (numbers as above; a new `ARC` delivery: instant, a reach, the
  forward arc, the shielded-first pick) — M9 part A
- [ ] The `shield-breaker` hit on an enemy shield (the shield model of [enemies](../../../enemies/README.md#enemy-shields))
  and the sparks' `area` share on the others — M9 part A
- [ ] Shop entry from the L29 visit; the hangar's ◆ trait marker for `shield-breaker` — M9 part A
- [ ] The arc's look (`arc` family) and the spark burst; the `tesla` sound per arc at the level's pitch — M9 part A
  (round 60 proposals)

## Open questions

- Q1 of [round 60](../../../concept-rounds/README.md) (enemy shields): recommended (a), a regrowing
  shield every weapon wears down and this pod empties at once (this document). Alternatives: (b)
  shields that only `shield-breaker`, beams and the EMP get through (the pod then becomes a must-buy
  at L29, against the campaign's L37 pacing), or (c) no shield mechanic in Act 5 (the pod then sells
  only its sparks until Act 6).
- Q12: the pod is in every variant of Act 5's arsenal; only the number of other new items changes.

## Decisions

- 2026-10-27 (Act 5 design phase, step m9-d-systems; **pending the user's review in round 60**):
  promoted from the weapons roster with hand-written numbers (12 / 40 DPS and 2 → 4 MW kept, 3 000 cr,
  L2–L4 ours, an instant arc of 140 → 180 px instead of a projectile, sparks at half damage, the
  shielded-first pick); the `tesla` sound family it already had.
