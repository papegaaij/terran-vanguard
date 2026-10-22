---
title: Harpoon Torpedoes
design: approved
implementation: not-started
art: chosen
depends-on: [.., ../torpedo-pod, ../../generator, ../../../world/europa, ../../../systems/economy]
updated: 2026-10-22
---

# Harpoon Torpedoes

## Summary

The front-slot `anti-sub` weapon of Act 4: heavy torpedoes that run ahead of the ship on the play
plane under the Europa ice and keep their full damage where the Ion Beam and the missiles lose 30 %.
It is the answer to the water's damage rule and the only front weapon that reaches the `sub`
layer. Draft, decided in round 48 (2026-10-22); numbers are hand-written, nothing is in
`data.yaml` yet (M8 part A).

## Design

| Property | Value (draft) |
|---|---|
| Slot | Front |
| Traits | `anti-sub`, `forward` |
| Layers hit | Under water: every target in the water (the play plane `air`, `low-air`, the sea floor `ground`, and the `sub` layer below), at full damage, no ×2 and no ×0.7 ([water rules](../../../world/europa/README.md#under-water-rules)); never `high-air`. Above water: like the [Torpedo Pod](../torpedo-pod/README.md) (`sub` and naval `ground`) in a level with `water: true`. In a dry level it is idle with `NO WATER` and still draws power |
| Price | 2 000 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 500 / 1 000 / 2 000 / 4 000 cr (an upgrade base of 1 000 on the common factors 0.5 / 1 / 2 / 4, the Ion Beam's way); sell-back per [economy](../../../systems/economy/README.md) |
| Unlock | L22 (in the shop from the hangar visit before L22; usable from the waterline of L23) |
| Projectile | 360 → 520 px/s over 0.6 s, 6×22 px, range 600 px (900 px under water: torpedo-type +50 %), bullet speed factor of the level applies |
| Sound family | `torpedo`, heavier launch (round 48 proposals) |
| Projectile family (VFX) | `torpedo` sheet of concept round 08 re-used; a harpoon variant proposed in round 48 |

### Per level (draft, hand-written)

| Level | Shots | Pattern | Rate /s | Damage | Volley DPS | Single-target DPS | Extra | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 1 | single | 0.8 | 37.5 | 30.0 | 30.0 | turn 90°/s | 3 | — |
| 2 | 1 | single | 0.9 | 50 | 45.0 | 45.0 | turn 90°/s | 3.5 | 500 |
| 3 | 1 | single | 1 | 62 | 62.0 | 62.0 | turn 90°/s | 4 | 1 000 |
| 4 | 1 | single | 1 | 80 | 80.0 | 80.0 | turn 90°/s | 4.5 | 2 000 |
| 5 | 2 | parallel -6, +6 px | 0.625 | 80 | 100.0 | 100.0 | turn 90°/s | 5 | 4 000 |
| OD | 2 | parallel -6, +6 px | 0.75 | 85 | 127.5 | 127.5 | turn 90°/s | — (ignores cap) | — |

DPS L1 and L5 are the roster's (30 / 100); L2–L4 are ours, on the ratio the other front weapons
grow by. A harpoon that hits blasts a 20 px radius for half its damage on neighbours
(the DPS above counts the direct hit only).

### Behaviour

- Fired straight ahead like a missile, it **homes weakly**: it steers up to 90°/s toward the
  nearest valid target in a 70° cone as it is fired (the Torpedo Pod's pick rule: again when its
  target dies; the Targeting computer does not scale it), without one it runs straight.
- A trail of cable-thin light behind it (the harpoon's line) and a heavy thud on impact. No
  bubbles: the wake is a straight white-teal streak (the user's rule: no bubbly sounds).
- It strikes the first hit box it touches and is spent. **Hardened** ground targets take it only
  if it is also `anti-ground` (it is not): it glances off them, like the Torpedo Pod's.
- **Under water** the rules of the [water](../../../world/europa/README.md#under-water-rules) apply:
  the bullet speed × 0.7 (the 360 → 520 px/s above are the level's multiplied speeds before the
  factor: ≈ 250 → 365 px/s on the screen), range +50 %. It reaches the `sub` layer below at once: a
  harpoon fired at a rising unit hits it.
- **Power:** it draws 3 → 5 MW for 30 → 100 DPS (10 → 20 DPS a MW, the Ion Beam 11 → 16), and under
  water its 62 DPS at L3 for 4 MW beat the beam's 56 for 6 (the beam's 80 × 0.7); see the
  [hand plan](../../generator/README.md#act-4-hand-plan-draft).
- Replacing the Ion Beam: the beam stays owned and can be refitted in dry levels for free (Act 5
  returns to dry fights).

## Concept art

Proposals: concept round 48 (step A-gear, listed in the [weapons' Concept art
table](../README.md#concept-art); prompts in [../concept/prompts.md](../concept/prompts.md#harpoon-torpedoes-r48-a),
generator `tools/concept/gear_r48.py`), pending the user's review: [a "barbed harpoon"](../concept/harpoon-torpedoes-r48-a.png) ([GIF](../concept/harpoon-torpedoes-r48-a.gif)), a gunmetal shaft with a steel spear head and swept barbs, a teal motor ring and the cable-thin line of light behind it, an impact of a dark cavity flash and a teal ring; and [b "heavy torpedo"](../concept/rejected/harpoon-torpedoes-r48-b.png) ([GIF](../concept/rejected/harpoon-torpedoes-r48-b.gif)), a white UTC torpedo with a cobalt nose band and a pump-jet shroud, a straight white-teal wake streak, an impact of a white flash and a double ring. Both are shown under the
round-48 under-water grade (Level 24) with stand-in targets.

## Implementation

- [ ] Weapon data in `data.yaml` (not yet written) (numbers as above) — M8 part A
- [ ] A front torpedo delivery that runs on the play plane under water and on the `sub` layer above it; reaches `sub`, `air`, `low-air` and `ground` in the water; idle with `NO WATER` in a dry level — M8 part A
- [ ] Weak homing, impact blast, power draw per level, upgrade prices — M8 part A
- [ ] Shop entry from the L22 visit; the balance plan's purchases (see the generator's hand plan) — M8 part A
- [ ] Sprites, the cable trail and sounds of the weapon (round 48) — M8 part A

## Open questions

- None of its own: its place is Q1 of [round 48](../../../concept-rounds/README.md) (the water's
  damage rule); the weapon is the same under (a) and (b), only the pressure to buy it differs.

## Decisions

- 2026-10-21 (Act 4 design phase, step m8-dsystems; **decided in round 48 (2026-10-22)**):
  promoted from the weapons roster with hand-written numbers (30 / 100 DPS kept, L2–L4 ours, upgrade
  base 1 000, the L5 pattern two parallel torpedoes); `anti-sub` keeps its full damage under the
  recommended water rules (Q1 = b).
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
