---
title: Rear-guard Drone Bay
design: approved
implementation: in-progress
art: final
depends-on: [.., ../../wingmen, ../light-drone-bay, ../../generator, ../../../systems/economy, ../../../campaign/act-3-red-dust/level-20-retreat-from-hellas]
updated: 2026-10-26
---

# Rear-guard Drone Bay

## Summary

A wing pod that launches a small drone which trails the ship and fires straight back: rear fire
and a body that soaks up bullets from behind, from a wing mount. The second of the
[drones](../../wingmen/README.md#drones), after the [Light Drone Bay](../light-drone-bay/README.md);
it unlocks with Level 20's retreat.

## Design

The numbers the game reads are in [data.yaml](data.yaml) (M7 part G); the marked table is rendered
from it. The drone's behaviour is owned by [wingmen](../../wingmen/README.md#rear-guard-drone); this
document holds the shop item and its numbers. They are the wingmen document's table, approved with the
drones in concept round 36 (D.26), moved here unchanged; this document itself waits for the user's
review (round 43).

| Property | Value |
|---|---|
| Slot | Wing mount (per bay); the drone flies, the bay stays on the wing |
| Traits | `rear` (drone) |
| Layers hit | `air`, `low-air` and `ground` (hardened ground targets glance off); not `high-air`; under water the play plane at the water's damage factor and the deep `sub` layer only in the Sonar Pulse's window ([layer rules](../../../enemies/README.md#layer-rules), [Europa rules](../../../world/europa/README.md#under-water-rules)) |
| Price | 2 400 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 1 200 / 2 400 / 4 800 / 9 600 cr (the common factors on the price; sell-back per [economy](../../../systems/economy/README.md)) |
| Unlock | L20 (in the shop from the hangar visit before it) |
| Projectile | 900 px/s, 3×8 px, range until off-screen; straight back (180°) from the drone 40 px behind the ship |
| Sound family | `pulse`, pitched up and quieter, as the light drone's ([weapon sound families](../../../audio/sfx/README.md#weapon-sound-families)) |
| Projectile family (VFX) | `pulse`, smaller |

### Per level

<!-- data: weapon-levels -->
| Level | Rate /s | Damage | Volley DPS | Single-target DPS | Drone breaks after | Rebuild | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|---|---|
| 1 | 6 | 1.5 | 9.0 | 9.0 | 8 hits | 6 s | 2 | — |
| 2 | 6 | 2 | 12.0 | 12.0 | 9 hits | 5.5 s | 2.5 | 1 200 |
| 3 | 6 | 2.7 | 16.2 | 16.2 | 10 hits | 5 s | 2.5 | 2 400 |
| 4 | 6 | 3.5 | 21.0 | 21.0 | 11 hits | 4.5 s | 3 | 4 800 |
| 5 | 6 | 4.3 | 25.8 | 25.8 | 12 hits | 4 s | 3 | 9 600 |
| OD | 7 | 4.3 | 30.1 | 30.1 | 12 hits | 4 s | — (ignores cap) | — |

Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`; do not edit by hand. Values are per bay (one drone each). It flies 40 px behind the ship and fires straight back, its one shot a volley, so every shot counts as a single-target hit. OD = the overdrive (20 s pickup).
<!-- /data -->

Numbers per bay (one drone each). Rear DPS 9 → 26 and draw 2 → 3 MW as in the wingmen table; the
hits it takes before it breaks (8 → 12) and the rebuild time (6 → 4 s) are the light drone's. A
broken drone deals nothing until it is rebuilt.

### Against the rear guns and the light drone

| Mount | Rear DPS L1 / L2 / L3 | Draw L1 / L2 / L3 | Price |
|---|---|---|---|
| Tail Gun (rear slot, the plan's) | 10 / 14 / 20 | — (rear slot) | 600 |
| **Rear-guard Drone Bay** (wing, per bay) | **9 / 12 / 16.2** | **2 / 2.5 / 2.5** | **2 400** |
| Light Drone Bay (wing, per bay; forward) | — (10.2 / 13.8 / 19.2 forward) | 2 / 2.5 / 2.5 | 2 000 |

It gives the rear DPS of a Tail Gun L2–L3 from a wing mount, so a player can keep the rear slot for
another gun (the Rear Lance) or carry rear fire and a rear shield at once in Level 20; it gives up the
wing pod's forward DPS (10 at a Micro-missile Pod L2) and its homing. It is not in the
[balance plan](../../balance-plan.yaml): the plan's Level 20 visit buys the Tail Gun L3, and the
level is flown with the plan's fit; the bay is the survival choice for the retreat.

### Behaviour (summary)

- The drone flies 40 px behind the ship's centre and 12 px to its bay's side (two drones fly side by
  side), following the ship with a 0.15 s lag, so it swings out in a turn.
- It fires whenever the player fires, straight back (180°).
- Enemy bullets that touch it are absorbed (one hit each); after the level's number of hits it
  breaks and the bay rebuilds it in the level's time. It starts each attempt whole.
- In a reversed section (Level 20) "behind" stays the bottom of the screen, which is ahead in the
  direction of travel there: it shields the ship from the ambushes that come up from the bottom and
  fires into them.
- Full rules (contacts, walls, the HUD) in [wingmen](../../wingmen/README.md#rear-guard-drone).

## Concept art

Production art (M7 part G step ART-G2, for concept round 43; prompts in
[concept/prompts.md](concept/prompts.md)): `tools/art/light_drone.py --rear` draws the drone (the light
drone's disc with a forked twin gun and blue fins, 16 gun headings: the trail firing back draws frame
8) and the bay pod turned round (its dock ring at the back) on each wing per banking frame into
`assets/`; the shot is the pulse bolt, the break and rebuild are the light drone's. Its hangar icon
(`sprites/icons/rear-guard-drone-bay`, small and large) is drawn in the same step.

| File | What | Status |
|---|---|---|
| [concept/rear-guard-drone-final-r43-a.png](concept/rear-guard-drone-final-r43-a.png) | Production review: the rear-guard drone (16 gun headings), the rear-docked bay pod on every bank frame, the pair trailing, firing back, blocking, one breaking and rebuilt (sheet) | chosen (round 43: approved as final) |
| [concept/rear-guard-drone-final-r43-a.gif](concept/rear-guard-drone-final-r43-a.gif) | The same in motion: the pair trailing 40 px behind with the 0.15 s lag, firing back, blocking bullets from behind, one breaking and rebuilt at its dock (motion) | chosen (round 43: approved as final) |
| [concept/rear-guard-icon-final-r43-a.png](concept/rear-guard-icon-final-r43-a.png) | The bay's hangar icon, small and large (sheet) | chosen (round 43: approved as final) |
| [concept/rear-guard-drone-testfire-r43-a.mp4](concept/rear-guard-drone-testfire-r43-a.mp4) | The hangar shop before Level 20 (a test save, 9 000 CR), L WING, the list row "REAR-GUARD BAY" (NEW, 2 400) selected, the SELECTED name "REAR-GUARD DRONE BAY L1" in full: its test-fire box at L1, the drone trailing the ship and firing back at the dummies as they pass behind. Silent. 12 s | chosen (round 43: accepted) |
| [concept/rear-guard-drone-testfire-r43-a.png](concept/rear-guard-drone-testfire-r43-a.png) | Six test-fire frames at 2× + the shop screen at 1:1 | chosen (round 43: accepted) |

## Implementation

- [x] Weapon data (`data.yaml`) with the numbers above and a `drone` block with the trail station
  (`station: trail`, `behind`, `side`, `lag`, `fire: back`) — M7 part G step G2 (content's `WeaponData`,
  checked by `RetreatChecks`; test `RearGuardDroneBayDataTest`)
- [x] The trail station in the simulation (`DroneSpec.Station` TRAIL: the lag, the swing, firing back;
  block, break and rebuild as the light drone) — step G3; mapped in step G4 (content's `RetreatRules.trail`
  through `WeaponRules`: `SimSpecs.flies` is true, the mount's drone trails at every level and in
  overdrive; `RearGuardDroneBayDataTest`)
- [x] The drone and bay pod sprites and the hangar icon (drawn: step ART-G2, `tools/art/light_drone.py
  --rear`, pending round 43; the icon in the assets, so the live shop lists the bay from the L20 visit);
  in flight with its HUD pips as the light drone's (step GB)
- [ ] Power draw per level counted in the loadout; upgrades priced as listed (content's `Catalogue`/
  `Hangar` price and count it from the data; the shop at unlock 20, `ActThreeShopTest`)

## Decisions

- 2026-10-13 (user, round 36): the rear-guard drone approved with the drones (D.26) as wingmen content:
  2 400 cr, draw 2 → 3 MW, unlock L20, the per-level numbers.
- 2026-10-20 (M7 part G, step G2): the weapons document written from the wingmen table, which now
  links here; `design: review` (the document is new; approved in round 44, A.c). Not in
  the balance plan. Its drone flies `station: trail`; until the simulation flies the trail (steps G3,
  G4) the bay is not flown (`SimSpecs.flies` false), though the shop sells it from the L20 visit.
- 2026-10-20: production art made (step ART-G2): the light drone's disc with a forked twin gun and blue
  fins, the bay turned round (dock ring at the back); shot = pulse bolt, break/rebuild = the light
  drone's; accepted in round 43 (2026-10-20).
- 2026-10-20 (M7 part G, step G4): the trail station mapped; the simulation flies the bay
  (`SimSpecs.flies` true). Still not in the balance plan, so no level test fits it.
- 2026-10-20 (step R43): the art is in the game (the sprites, the pod, the icon, the looks of step GB); the document and the art are rows B.d and F.c-F.d of [round 43](../../../concept-rounds/round-43/README.md), approved in round 44 (A.c).
- 2026-10-20 (user, round 43, closed): the document approved with the build choices B.d (a)-(d) (the drone flies at
  every level, not in the balance plan, the numbers as the wingmen table, the shop sells it from the visit before
  Level 20); the drone, the bay pod and the hangar icon approved as final (F.c, F.d: `art: final`, their concept rows
  `chosen`); the test-fire capture (K.c, K.d) accepted.
- 2026-10-20 (user, round 44, A.c): the design **approved** (main had reverted it to `review` after part G because only the user approves; the user's decision: "approve"). The document is final for Acts 3 and later.
