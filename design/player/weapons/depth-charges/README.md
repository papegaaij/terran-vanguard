---
title: Depth Charges
design: approved
implementation: not-started
art: chosen
depends-on: [.., ../torpedo-pod, ../../generator, ../../../world/europa, ../../../systems/economy]
updated: 2026-10-22
---

# Depth Charges

## Summary

The rear-slot `anti-sub` weapon of Act 4: barrels that the Stormhawk rolls out behind it, that sink
back through the water and burst after a short fuse, hitting everything near the burst. It guards
the tail against swimmers that come up behind, and it keeps its full damage under the ice where the
Tail Gun loses 30 %. Draft, decided in round 48 (2026-10-22); numbers are hand-written.

## Design

| Property | Value (draft) |
|---|---|
| Slot | Rear |
| Traits | `anti-sub`, `rear`, `area` |
| Layers hit | Under water: the burst hits everything inside its radius in the water, at full damage (the play plane `air`, `low-air`, the sea floor `ground`, and the `sub` layer below); never `high-air`. Above water: the barrel drops through the surface and bursts on the `sub` layer, hitting `sub` and naval `ground` in a level with `water: true`; idle with `NO WATER` in a dry level |
| Price | 1 300 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 400 / 800 / 1 600 / 3 200 cr (an upgrade base of 800 on the common factors) |
| Unlock | L22 (in the shop from the hangar visit before L22; usable from the waterline of L23) |
| Projectile | A barrel leaves the stern at 140 px/s backwards and slows to a hover over 0.6 s (the water's drag), then sinks along the scroll; fuse 0.9 s; burst radius 40 px (L1) → 64 px (L5) |
| Sound family | a heavy, low underwater thud, no bubbles (round 48 proposals) |

### Per level (draft, hand-written)

| Level | Charges | Fuse | Volleys /s | Damage | Volley DPS | Single-target DPS | Burst radius | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 1 | 0.9 s | 1 | 25 | 25.0 | 25.0 | 40 px | 2 | — |
| 2 | 1 | 0.9 s | 1.2 | 31.7 | 38.0 | 38.0 | 46 px | 2.5 | 400 |
| 3 | 1 | 0.8 s | 1.3 | 40 | 52.0 | 52.0 | 52 px | 2.5 | 800 |
| 4 | 1 | 0.8 s | 1.4 | 47 | 66.0 | 66.0 | 58 px | 3 | 1 600 |
| 5 | 2 | 0.7 s | 0.8 | 50 | 80.0 | 80.0 | 64 px | 3 | 3 200 |
| OD | 2 | 0.7 s | 0.9 | 54 | 97.0 | 97.0 | 64 px | — (ignores cap) | — |

DPS L1 and L5 are the roster's (25 / 80); L2–L4 are ours. A charge's damage is dealt once to every
target inside the radius (so against a school the volley counts more than the single-target
DPS above); a boss part takes at most one burst per charge.

### Behaviour

- **Redefined from the roster** ("sink into the `sub` layer"): under water the play plane *is* the
  water, so the charges no longer change layer. They roll out of the stern, hang and sink, burst on
  their fuse, and the pressure wave is a circle on the play plane that also reaches the layers
  above and below it (the sea floor, the `sub` layer, never the ice ceiling). Enemies that rise from
  below or pursue from behind (Eels, the Abyss Ray's rise) run into them.
- A charge is not a mine: it bursts on its fuse, never on contact, and never hurts the player or
  Rook (the burst is Stormhawk-safe, a pressure wave tuned to the hull). The barrels are drawn,
  hit by nothing and block nothing.
- Water range rule: torpedo-type range +50 % widens the first roll-out (140 → 210 px/s); the burst
  radius is not scaled (it is the weapon's balance).
- Bullet speed × 0.7 applies to the roll-out; the fuse is not scaled.
- **Above water** the barrels drop with a splash and burst on the `sub` layer (hitting submerged units
  that a plain shot cannot reach); the pod rules apply (`NO WATER` in a dry level, still drawing power).

## Concept art

Proposals: concept round 48 (step A-gear, listed in the [weapons' Concept art
table](../README.md#concept-art); prompts in [../concept/prompts.md](../concept/prompts.md#depth-charges-r48-a),
generator `tools/concept/gear_r48.py`), pending the user's review: [a "drum"](../concept/depth-charges-r48-a.png) ([GIF](../concept/depth-charges-r48-a.gif)), a navy drum with yellow bands and a white fuse lamp blinking faster, a pressure-dome burst (flash disc, dark cavity, rim ring); and [b "finned canister"](../concept/rejected/depth-charges-r48-b.png) ([GIF](../concept/rejected/depth-charges-r48-b.gif)), a grey canister with cross fins and a closing cyan fuse ring, a double shock ring that refracts the floor. Both are shown under the
round-48 under-water grade (Level 24) with stand-in targets.

## Implementation

- [ ] Weapon data in `data.yaml` (not yet written) (numbers as above) — M8 part A
- [ ] A rear delivery: barrel roll-out, fuse, a circular burst that hits the water's layers; safe for the ship and Rook — M8 part A
- [ ] Idle with `NO WATER` in a dry level; the above-water drop on the `sub` layer — M8 part A
- [ ] Power draw per level, upgrade prices, shop entry from the L22 visit; the balance plan's purchases — M8 part A
- [ ] Sprites, burst VFX and sounds (round 48) — M8 part A

## Open questions

- None of its own: whether the roster's wording ("sink into the `sub` layer") is dropped is part of
  Q1 of [round 48](../../../concept-rounds/README.md) (recommended: yes, as above).

## Decisions

- 2026-10-21 (Act 4 design phase, step m8-dsystems; **decided in round 48 (2026-10-22)**):
  promoted from the weapons roster and **redefined** as a rear launcher of sinking charges that burst
  on the play plane (Q1 = b; the roster's "sink into the `sub` layer" is meaningless when the play
  plane is the water); numbers hand-written (25 / 80 DPS kept, L2–L4 ours).
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
