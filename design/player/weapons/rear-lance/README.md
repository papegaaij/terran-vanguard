---
title: Rear Lance
design: approved
implementation: in-progress
art: none
depends-on: [.., ../lance-laser, ../../generator, ../../../systems/economy]
updated: 2026-10-13
---

# Rear Lance

## Summary

The Lance Laser turned round: slow, hard laser lances fired backwards that pierce several
enemies in a line. The rear answer for Act 3's lined-up pursuers and for the turrets that fire
from behind in the Tharsis foundry, at a higher price and draw than the Tail Gun.

## Design

The numbers the game reads are in [data.yaml](data.yaml) (M7 part A); the marked tables are rendered from it.

<!-- data: weapon-properties -->
| Property | Value |
|---|---|
| Slot | Rear gun |
| Traits | `rear`, `piercing` |
| Layers hit | `air`, `low-air` and `ground` (hardened ground targets glance off); not `high-air`; the `sub` layer only in under-water mode, at 50 % ([layer rules](../../../enemies/README.md#layer-rules)) |
| Price | 2 800 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 1 400 / 2 800 / 5 600 / 11 200 cr (sell-back per [economy](../../../systems/economy/README.md)) |
| Unlock | L17 (in the shop from the hangar visit before it) |
| Projectile | 1100 px/s, 4×24 px, range until off-screen |
| Sound family | `laser` ([weapon sound families](../../../audio/sfx/README.md#weapon-sound-families)) |
| Projectile family (VFX) | `laser` (projectile sheet of concept round 08) |
<!-- /data -->

Its `laser` shot sound is played about 10 % lower like every rear gun's, and its lance is the Lance
Laser's final lance turned round (180°, straight down the screen).

### Per level

<!-- data: weapon-levels -->
| Level | Shots | Pattern | Rate /s | Damage | Volley DPS | Single-target DPS | Extra | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 1 | single | 4 | 4.5 | 18.0 | 18.0 | pierce 2 | 3 | — |
| 2 | 1 | single | 4 | 6 | 24.0 | 24.0 | pierce 3 | 3.5 | 1 400 |
| 3 | 2 | parallel -6, +6 px | 4 | 4.5 | 36.0 | 36.0 | pierce 3 | 4 | 2 800 |
| 4 | 2 | parallel -6, +6 px | 4 | 6 | 48.0 | 48.0 | pierce 4 | 4.5 | 5 600 |
| 5 | 2 | parallel -6, +6 px | 4 | 7.5 | 60.0 | 60.0 | pierce 5 | 5 | 11 200 |
| OD | 3 | parallel -10, +0, +10 px | 4 | 7 | 84.0 | 84.0 | pierce 6 | — (ignores cap) | — |

Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`; do not edit by hand; the exact offset and angle of every shot are in that file. Single-target DPS counts the shots that hit a 36 px target 100 px from the muzzle. OD = the overdrive pattern (20 s pickup).
<!-- /data -->

For a rear gun the single target is 100 px behind the rear muzzle (both parallel lances hit it).
DPS 18 → 60 and draw 3 → 5 MW as in the roster.

### Against the rear guns and the reference

| Rear gun at Act 3 | DPS (single target) | Draw | Cost from nothing |
|---|---|---|---|
| Tail Gun L2 (the typical player's, from L14) | 14 | 1.5 | 900 |
| Tail Gun L3 / L5 | 20 / 35 | 2 / 3 | 1 500 / 5 100 |
| **Rear Lance L1 / L2 / L3** | **18 / 24 / 36** | **3 / 3.5 / 4** | **2 800 / 4 200 / 7 000** |

Rear DPS is not counted in the forward reference ([enemies](../../../enemies/README.md#balancing-basis));
the rear checks of the levels are (a unit's time on screen behind the ship against the plan's rear
DPS, as Level 10's Wraith check). The Rear Lance is a trait buy: per credit the Tail Gun's upgrades
give more single-target DPS, the Lance gives **pierce** (a line of pursuers behind the ship, a
Threadcrawler's segments, the Level 20 retreat's obstacles coming up from the bottom) and a lance
that kills a `medium` rear unit in fewer hits. Its draw (3 MW at L1, 1.5 more than the typical
Tail Gun L2) does not fit the typical Act 3 loadout on the Mk III generator with an Ion Beam (see
[Ion Beam](../ion-beam/README.md#price-and-power-m11)); with the Pulse Cannon it fits from the
Mk III on (Pulse L5 4 + pods L3 4 + shield Mk II 3 + Rear Lance L1 3 = 14 of 14 MW).

### Behaviour

- Each lance passes through up to *pierce* enemies, dealing full damage to each, then
  disappears. Canyon walls and rock islands stop it (M2 = a), as they
  stop the Lance Laser.
- Hardened ground targets stop a lance without damage (glance spark); it is not `anti-ground`.
- A tendril (Level 16's Tendril Anchor link) is not hurt by it: only `beam` damages a link
  (M9 = a); what a lance does at a link is the anchor document's rule.
- In the reverse scroll of Level 20 (M4 = a, M5 = a) "rear" stays the
  bottom of the screen: the lances fly down the screen, which is ahead in the direction of travel
  there, where the new obstacles and ambushes come from.
- Under water: range halved and 50 % damage like the Lance Laser (Act 4).

## Implementation

- [x] Weapon data (`data.yaml`) with the numbers above — M7 part A (step A2: `WeaponData`, validated by `ContentValidator` and its record checks)
- [x] Projectile pattern per level 1–5 and the overdrive pattern, fired backwards (M7 part A: the
  existing bolt delivery at angle 180, `ActThreeArsenalDataTest`)
- [x] Layer hit rules for its traits; pierce (the Lance Laser's rules, M7 part A)
- [ ] Walls stop a lance (with the terrain of Level 16)
- [x] The Lance Laser's final lance and impact turned round; the `laser` sound pitched for a rear gun
  (M7 part A step A6: `WeaponLooks` draws the Lance Laser's lance of the same level turned round,
  the overdrive the next level's; the impact is the laser's burst; rear guns play their family 10 %
  lower)
- [x] Power draw per level counted in the loadout; upgrades priced as listed (content: `Catalogue`/`Hangar` price and count it, M7 part A step A2; in the live shop since step A5 drew its hangar icon and emptied `Catalogue.AWAITING_ICONS`; tests `ActThreeShopTest`, `ActThreeHangarTest`)

## Decisions

- 2026-10-12 (overnight design phase): promoted from the weapons roster with full per-level
  numbers in Act 1 terms: the Lance Laser's pattern at rate 4 (18 → 60 DPS, the roster's), draw 3 →
  5, 2 800 cr, unlock L17; walls stop it; reverse scroll keeps "rear" as the screen's bottom. Accepted in round 36 (2026-10-13).
- 2026-10-13 (user, round 36): approved (D.21) with M2, M4 and M5 accepted.
- 2026-10-13 (M7 part A, step A2): data.yaml written; the property and per-level tables are
  rendered from it.
