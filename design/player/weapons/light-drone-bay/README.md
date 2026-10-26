---
title: Light Drone Bay
design: approved
implementation: in-progress
art: final
depends-on: [.., ../../wingmen, ../../generator, ../../../systems/economy]
updated: 2026-10-26
---

# Light Drone Bay

## Summary

A wing pod that launches a small light drone: it circles the ship, fires a light gun at the
point ahead of the nose and soaks up enemy bullets with its own body until it breaks, then the
bay builds a new one. Damage and a moving shield in one mount, the first of the
[drones](../../wingmen/README.md#drones).

## Design

The numbers the game reads are in [data.yaml](data.yaml) (M7 part A); the marked tables are rendered from it. The drone's behaviour is
owned by [wingmen](../../wingmen/README.md#light-drone); this document holds the shop item and its
numbers.

| Property | Value |
|---|---|
| Slot | Wing mount (per bay); the drone flies, the bay stays on the wing |
| Traits | `forward` (drone) |
| Layers hit | `air`, `low-air` and `ground` (hardened ground targets glance off); not `high-air`; under water the play plane at the water's damage factor and the deep `sub` layer only in the Sonar Pulse's window ([layer rules](../../../enemies/README.md#layer-rules), [Europa rules](../../../world/europa/README.md#under-water-rules)) |
| Price | 2 000 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 1 000 / 2 000 / 4 000 / 8 000 cr (the common factors on the price; sell-back per [economy](../../../systems/economy/README.md)) |
| Unlock | L15 (in the shop from the hangar visit before it) |
| Projectile | 900 px/s, 3×8 px, range until off-screen; aimed at the convergence point 140 px ahead of the ship's centre |
| Sound family | `pulse`, pitched up and quieter ([weapon sound families](../../../audio/sfx/README.md#weapon-sound-families)) |
| Projectile family (VFX) | `pulse`, smaller |

### Per level

<!-- data: weapon-levels -->
| Level | Rate /s | Damage | Volley DPS | Single-target DPS | Drone breaks after | Rebuild | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|---|---|
| 1 | 6 | 1.7 | 10.2 | 10.2 | 8 hits | 6 s | 2 | — |
| 2 | 6 | 2.3 | 13.8 | 13.8 | 9 hits | 5.5 s | 2.5 | 1 000 |
| 3 | 6 | 3.2 | 19.2 | 19.2 | 10 hits | 5 s | 2.5 | 2 000 |
| 4 | 6 | 4 | 24.0 | 24.0 | 11 hits | 4.5 s | 3 | 4 000 |
| 5 | 6 | 5 | 30.0 | 30.0 | 12 hits | 4 s | 3 | 8 000 |
| OD | 7 | 5 | 35.0 | 35.0 | 12 hits | 4 s | — (ignores cap) | — |

Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`; do not edit by hand. Values are per bay (one drone each). Its shots aim at the convergence point 140 px ahead of the ship's centre, so every shot counts as a single-target hit. OD = the overdrive (20 s pickup).
<!-- /data -->

Numbers per bay (one drone each). DPS 10 → 30 and draw 2 → 3 MW as in the roster. Because the
drone aims at the convergence point, its shots hit a 36 px target 100 px ahead wherever it is on
its orbit, so single-target DPS equals volley DPS; a broken drone deals nothing until it is
rebuilt.

### Against the wing pods and the reference

| Wing mount (per pod) | DPS L1 / L2 / L3 | Draw L1 / L2 / L3 | Price |
|---|---|---|---|
| Autocannon Pod | 8 / 10.5 / 15 | 1 / 1.5 / 1.5 | 500 |
| Micro-missile Pod (the typical player's) | 8 / 10 / 15 | 1 / 1.5 / 2 | 800 |
| **Light Drone Bay** | **10.2 / 13.8 / 19.2** | **2 / 2.5 / 2.5** | **2 000** |

A drone bay gives about a third more DPS than a pod at the same level, plus a body that blocks
8–12 bullets per life, for a higher price and 0.5–1 MW more draw. Two bays at L2 in place of the
typical two Micro-missile Pods L2 add 7.6 forward DPS (77 → 84.6) and 2 MW, so on the Mk II
generator (11 of 11 MW at the end of Act 2) they need the Mk III first; they also give up homing
and the pods' reach on `high-air`. The typical plan keeps its pods; the drone bay is the
survival-and-damage choice for side and all-direction levels (L17, L18, L21).

### Behaviour (summary)

- The drone orbits the ship at 36 px, one turn per 2.4 s; two drones fly opposite each other.
- It fires whenever the player fires, at the convergence point 140 px ahead of the ship's centre.
- Enemy bullets that touch it are absorbed (one hit each); after the level's number of hits it
  breaks (a small spark burst) and the bay rebuilds it in the level's time. It starts each attempt
  whole.
- Full rules (contacts, walls, reverse scroll, the HUD) in [wingmen](../../wingmen/README.md#light-drone).

## Concept art

Proposals: concept round 36 (step A-gear, listed in the [weapons' Concept art
table](../README.md#concept-art); prompts in [../concept/prompts.md](../concept/prompts.md#light-drone-r36-a),
generator `tools/concept/gear_r36.py`); round 36 (2026-10-13) chose **a** (G.2), b is rejected:
[a](../concept/light-drone-r36-a.png) ([GIF](../concept/light-drone-r36-a.gif)), a disc drone and a
dock-ring bay pod, with the orbit, convergence, block / break / rebuild and the HUD hits-left pips;
and [b](../concept/rejected/light-drone-r36-b.png) ([GIF](../concept/rejected/light-drone-r36-b.gif)), a dart drone
like a miniature Stormhawk and a slim launch-rail bay.

Production art (M7 part A step ART-A, for concept round 37; prompts in
[concept/prompts.md](concept/prompts.md)): `tools/art/light_drone.py` draws look a into `assets/`: the
disc drone at 16 headings (`light-drone-bay-drone`), its shot at 32 headings, the break and the
rebuild at the dock, and the dock-ring bay on each wing per banking frame (`pod-drone-bay-left/right`,
offsets in `pivots/pods.json`).

| File | What | Status |
|---|---|---|
| [concept/light-drone-final-r37-a.png](concept/light-drone-final-r37-a.png) | Final sprites: the drone's 16 headings, its shot, break and rebuild, the bay on the five banking frames, frames in flight (sheet) | chosen (round 37, F.2: approved as final) |
| [concept/light-drone-final-r37-a.gif](concept/light-drone-final-r37-a.gif) | Two drones orbiting and firing at the convergence point, one breaking and rebuilt at its bay (the rebuild shortened for the loop) (motion) | chosen (round 37, F.2: approved as final) |

## Implementation

- [x] Weapon data (`data.yaml`) with the numbers above and a `drone` block (orbit, hits, rebuild) — M7 part A (step A2: `WeaponData`, validated by `ContentValidator` and its record checks)
- [x] The drone behaviour of [wingmen](../../wingmen/README.md#light-drone): orbit, aimed fire,
  block, break, rebuild (M7 part A step A1: `vanguard.sim.Drones`, the bay an `Armament.Mount` of
  its own kind; tests `DronesTest`, `ActThreeArsenalFlightTest`)
- [x] The bay pod sprite on the wing (12×18), the drone sprite and its break and rebuild effects
  (drawn: `tools/art/light_drone.py`, M7 part A step ART-A, approved as final in round 37 (F.2);
  in flight step A6: `ArsenalLooks` draws the drone facing its convergence point, white for two
  steps as it absorbs a hit, fading in as it is rebuilt, a small muzzle flash ahead of it as it
  fires, its break and the bay's rebuild ring; the HUD's hits strip under its row; the break and
  rebuild sounds, an absorbed hit a soft metal ping)
- [x] Power draw per level counted in the loadout; upgrades priced as listed (content: `Catalogue`/`Hangar` price and count it, M7 part A step A2; in the live shop since step A5 drew its hangar icon and emptied `Catalogue.AWAITING_ICONS`; tests `ActThreeShopTest`, `ActThreeHangarTest`)

## Decisions

- 2026-10-12 (overnight design phase): promoted from the weapons roster with full per-level
  numbers in Act 1 terms (10 → 30 DPS, 8 → 12 hits, rebuild 6 → 4 s, draw 2 → 3, 2 000 cr, L15);
  the drone aims at a convergence point so its orbit does not waste its shots. Accepted in round 36 (2026-10-13).
- 2026-10-12 (overnight design phase): concept round 36 proposes an a/b of the drone and its
  bay; `art: proposed`, pending the user's choice.
- 2026-10-13 (user, round 36): approved (D.23). G.2 = a: the disc drone with the dock-ring bay
  pod chosen (its HUD hits-left pips in place of the level pips with it); the dart drone (b)
  rejected and moved to `../concept/rejected/`.
- 2026-10-13 (M7 part A, step A2): data.yaml written with a `drone` block (size, hit box, orbit,
  period, convergence, fade-in, contact) and each level's `drone_hits` and `rebuild`; a `laser` touch
  costs 2 hits (`laser_hits`). The per-level table is rendered from it.
- 2026-10-13 (user, round 37): the production art approved as final (F.2; `art: final`); the HUD's
  hits strip under the drone's row accepted as a change to the approved pips (C1); the capture (K.1)
  and the build choices kept (B.1 c: a rebuilt drone fades in over 0.3 s, neither firing nor blocking;
  B.6 d: an absorbed hit plays the metal hit 12 dB under the hits); its sounds S.14 break a and S.15
  rebuilt a "materialise".
