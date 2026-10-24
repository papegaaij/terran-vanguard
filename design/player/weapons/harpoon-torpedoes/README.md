---
title: Harpoon Torpedoes
design: approved
implementation: in-progress
art: final
depends-on: [.., ../torpedo-pod, ../../generator, ../../../world/europa, ../../../systems/economy]
updated: 2026-10-24
---

# Harpoon Torpedoes

## Summary

The front-slot `anti-sub` weapon of Act 4: heavy torpedoes that run ahead of the ship on the play
plane under the Europa ice and keep their full damage where the missiles lose 30 % and the Ion Beam
80 %. It is the answer to the water's damage rule and the only front weapon that reaches the `sub`
layer: since the user's playtest (M8 AW) a fast, light swarm-killer with a wide splash. Decided in
round 48 (2026-10-22); its numbers are in [data.yaml](data.yaml) since M8 part A.

## Design

<!-- data: weapon-properties -->
| Property | Value |
|---|---|
| Slot | Front gun |
| Traits | `anti-sub`, `forward` |
| Layers hit | Under water: every target in the water (the play plane `air`, `low-air`, the sea floor `ground`, and the `sub` layer below), at full damage, no ×2 and no ×0.7 ([water rules](../../../world/europa/README.md#under-water-rules)); never `high-air`. Above water: like the [Torpedo Pod](../torpedo-pod/README.md) (`sub` and naval `ground`) in a level with `water: true`. In a dry level it is idle with `NO WATER` and still draws power |
| Price | 2 000 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 500 / 1 000 / 2 000 / 4 000 cr (sell-back per [economy](../../../systems/economy/README.md)) |
| Unlock | L22 (in the shop from the hangar visit before it) |
| Projectile | 540 → 780 px/s, 6×22 px, range 600 px |
| Sound family | `harpoon` ([weapon sound families](../../../audio/sfx/README.md#weapon-sound-families)) |
| Projectile family (VFX) | `harpoon` (the barbed harpoon of concept round 48, G1 a) |
<!-- /data -->

Under water the range is 900 px (torpedo-type +50 %) and the speed is the level's bullet speed × 0.7
([water rules](../../../world/europa/README.md#under-water-rules)); it is usable from the waterline of
L23. Its sound family `harpoon` is the torpedo's heavier launch, run and thud (round 48 proposals).

### Per level

<!-- data: weapon-levels -->
| Level | Shots | Pattern | Rate /s | Damage | Volley DPS | Single-target DPS | Extra | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 1 | single | 2.4 | 27 | 64.8 | 64.8 | blast 40 px, turn 90°/s | 3 | — |
| 2 | 1 | single | 2.6 | 29 | 75.4 | 75.4 | blast 40 px, turn 90°/s | 3.5 | 500 |
| 3 | 1 | single | 2.8 | 31 | 86.8 | 86.8 | blast 44 px, turn 90°/s | 4 | 1 000 |
| 4 | 1 | single | 3 | 33 | 99.0 | 99.0 | blast 48 px, turn 90°/s | 4.5 | 2 000 |
| 5 | 2 | parallel -6, +6 px | 1.8 | 33 | 118.8 | 118.8 | blast 48 px, turn 90°/s | 5 | 4 000 |
| OD | 2 | parallel -6, +6 px | 2 | 35 | 140.0 | 140.0 | blast 52 px, turn 90°/s | — (ignores cap) | — |

Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`; do not edit by hand; the exact offset and angle of every shot are in that file. Single-target DPS counts the shots that hit a 36 px target 100 px from the muzzle (seeking/lobbed: all shots). OD = the overdrive pattern (20 s pickup).
<!-- /data -->

Since M8 AW (the user's playtest decision of 2026-10-23) it fires 2.4 → 3 harpoons a second for
27 → 33 damage (65 → 119 DPS; it was 0.8 → 1 a second for 37.5 → 80, 30 → 100 DPS): a hit kills any
swarm unit. A harpoon that hits blasts its level's radius (the *blast* above, 40 → 52 px; it was 20)
for 0.6 of its damage (`splash: 0.6` in [data.yaml](data.yaml), was 0.5) on the others there, not on
the struck target (the DPS above counts the direct hit only): 16 at L1, enough for a Barbfin (12;
hard 15.6), a Glassfin or a medium Glow Angler (14) within 40 px.

### Behaviour

- Fired straight ahead like a missile, it **homes weakly**: it steers up to 90°/s toward the
  nearest valid target in a 30° cone (±15°, M8 AW: was 70°) as it is fired, so it locks only on
  what is ahead and runs nearly straight (the Torpedo Pod's pick rule: again when its
  target dies; the Targeting computer does not scale it), without one it runs straight.
- A trail of cable-thin light behind it (the harpoon's line) and a heavy thud on impact. No
  bubbles: the wake is a straight white-teal streak (the user's rule: no bubbly sounds).
- It strikes the first hit box it touches and is spent. **Hardened** ground targets take it only
  if it is also `anti-ground` (it is not): it glances off them, like the Torpedo Pod's.
- **Under water** the rules of the [water](../../../world/europa/README.md#under-water-rules) apply:
  the bullet speed × 0.7 (the 540 → 780 px/s above, M8 AW: +50 %, accelerating over 0.4 s, are the
  speeds before the factor: 378 → 546 px/s under water), range +50 %. It reaches the `sub` layer below at once: a
  harpoon fired at a rising unit hits it.
- **Power:** it draws 3 → 5 MW for 65 → 119 DPS (M8 AW; 22 → 24 DPS a MW, the Ion Beam 11 → 16), and
  under water its 87 DPS at L3 for 4 MW beat the beam's 16 for 6 (the beam's 80 × 0.2 since M8 AW); see the
  [hand plan](../../generator/README.md#act-4-hand-plan-draft).
- Replacing the Ion Beam: the beam stays owned and can be refitted in dry levels for free (Act 5
  returns to dry fights).

## Concept art

Proposals: concept round 48 (step A-gear, listed in the [weapons' Concept art
table](../README.md#concept-art); prompts in [../concept/prompts.md](../concept/prompts.md#harpoon-torpedoes-r48-a),
generator `tools/concept/gear_r48.py`), pending the user's review: [a "barbed harpoon"](../concept/harpoon-torpedoes-r48-a.png) ([GIF](../concept/harpoon-torpedoes-r48-a.gif)), a gunmetal shaft with a steel spear head and swept barbs, a teal motor ring and the cable-thin line of light behind it, an impact of a dark cavity flash and a teal ring; and [b "heavy torpedo"](../concept/rejected/harpoon-torpedoes-r48-b.png) ([GIF](../concept/rejected/harpoon-torpedoes-r48-b.gif)), a white UTC torpedo with a cobalt nose band and a pump-jet shroud, a straight white-teal wake streak, an impact of a white flash and a double ring. Both are shown under the
round-48 under-water grade (Level 24) with stand-in targets.

| File | What | Status |
|---|---|---|
| [concept/harpoon-torpedoes-final-r50-a.png](concept/harpoon-torpedoes-final-r50-a.png) | Harpoon Torpedoes final frames (`tools/art/act4_gear.py harpoon`, round 48 G1 = a): the barbed harpoon at 32 headings, the impact (dark cavity, teal ring, barb sparks), motor glow, launch light, cable tile; an L5 pair in the water (sheet) | chosen (round 50, F.1: accepted as final) |
| [concept/harpoon-torpedoes-final-r50-a.gif](concept/harpoon-torpedoes-final-r50-a.gif) | Two L5 volleys running up from the nose with their lines of light, curving onto a stand-in Needler and hitting it, 20 fps at 2x | chosen (round 50, F.1: accepted as final) |
| [concept/harpoon-torpedoes-capture-r50-a.png](concept/harpoon-torpedoes-capture-r50-a.png) | Game capture, Level 18 with `--medium underwater@10`: harpoons at L3 with their lines of light running from the nose under water, the Depth Charges behind (a frame) | chosen (round 50, F.1: accepted) |
| [concept/harpoon-torpedoes-capture-r50-a.mp4](concept/harpoon-torpedoes-capture-r50-a.mp4) | Game capture with the game's sound: the `HARPOONS … NO WATER` row while dry, then the harpoons' launches, run loop and hits under water, the ship swept left and right (the drag) | chosen (round 50, F.1: accepted) |
| [concept/aw-capture-r56-a-toss.png](concept/aw-capture-r56-a-toss.png) | Game capture, M8 AW (step AW3; Level 23 on medium, `--invulnerable`, the plan's Harpoon L1 + Depth Charges L1): the depth charge tossed forward from the stern rack over the hull, swelling on its 0.15 s arc and dropping in ahead (8 frames, 1/30 s apart, 1.7×; Q1 a, the built default) | chosen (round 56, A.1: accepted) |
| [concept/aw-capture-r56-a-charges.png](concept/aw-capture-r56-a-charges.png) | Level 23 under the ice: the tossed charges bursting about 150–200 px ahead of the ship (the pressure dome, then its ring) among the swarm, harpoon cables above (4 frames, 0.2 s apart) | chosen (round 56, A.1: accepted) |
| [concept/aw-capture-r56-a-harpoon.png](concept/aw-capture-r56-a-harpoon.png) | Level 23: Glassfin swarms falling to the reworked harpoon (L1, 2.4 shots/s, ±15° cone) and its splash as the ship weaves, a charge's burst ring ahead (3 frames) | chosen (round 56, A.1: accepted) |
| [concept/aw-capture-r56-a-beam.png](concept/aw-capture-r56-a-beam.png) | Level 24: the Ion Beam L3 under water (×0.4 reach, ×0.2 damage): short and faint (40 % strength), scattering out over its last 72 px where nothing stops it; burning a unit close ahead in the middle frame (Q3: the one look built) | chosen (round 56, A.1: accepted) |

Prompts: [concept/prompts.md](concept/prompts.md).

## Implementation

- [x] Weapon data in [data.yaml](data.yaml) (numbers as above; M8 part A, step A2: loaded by `WeaponData` with the `splash` share, the tables rendered)
- [x] A front torpedo delivery that runs on the play plane under water and on the `sub` layer above it; reaches `sub`, `air`, `low-air` and `ground` in the water; idle with `NO WATER` in a dry level — M8 part A (steps A1/A3/A4: `HarpoonTorpedoTest`, `Act4ArsenalFlightTest.theHarpoonHitsUnderWaterAtFullDamageAndIsIdleInADryLevel`; the HUD's `HARPOONS … NO WATER` row in step A6)
- [x] Weak homing, impact blast, power draw per level, upgrade prices — M8 part A (the 20 px splash at half damage to others (D13); `ActFourDataTest.theHarpoonIsAFrontTorpedoWithASplash`, `HarpoonTorpedoTest.underWaterItStrikesAFlyerAndSplashesHalfItsDamageWithinTwentyPixels`)
- [x] M8 AW (the user, 2026-10-23): the swarm-killer numbers (rate 2.4 → 3, damage 27 → 33, speed 540 → 780 px/s, blast 40 → 52 px, splash 0.6, the ±15° cone) — step AW1 (`ActFourDataTest.theHarpoonIsAFrontTorpedoWithASplash`, `Act4ArsenalFlightTest.theHarpoonTorpedoesAreATorpedoFromTheNoseWithASplash`; the levels' tuning in AW2)
- [x] Shop entry from the L22 visit; the balance plan's purchases (see the generator's hand plan) — M8 part A (step A2: the plan's L2 at L23, L4 at L26, `BalanceTest`; step A5: `ActFourHangarTest.theActFourItemsAreInTheLiveShopWithTheirNames`)
- [x] Sprites, the cable trail and sounds of the weapon (round 48) — M8 part A (ART-A's finals and AUD-A's a variants in the game, step A6: `DeepWaterLooksTest.theCableFadesAfterAHitAndItsSlackGrowsAlongIt`, `ActFourSoundsTest.harpoonsLaunchAndRunOnOneLoopWhileAnyIsInFlight`; the finals accepted and the sounds picked in [round 50](../../../concept-rounds/round-50/README.md))

## Open questions

- None of its own: its place is Q1 of [round 48](../../../concept-rounds/README.md) (the water's
  damage rule); the weapon is the same under (a) and (b), only the pressure to buy it differs.

## Decisions

- 2026-10-21 (Act 4 design phase, step m8-dsystems; **decided in round 48 (2026-10-22)**):
  promoted from the weapons roster with hand-written numbers (30 / 100 DPS kept, L2–L4 ours, upgrade
  base 1 000, the L5 pattern two parallel torpedoes); `anti-sub` keeps its full damage under the
  recommended water rules (Q1 = b).
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-22 (M8 part A, step A2): the numbers in [data.yaml](data.yaml) (`hits: anti-sub`, the Torpedo Pod's
  delivery from the nose; the splash as `blast: 20` per level with `splash: 0.5`; sound and VFX family `harpoon`);
  the tables are rendered from it.
- 2026-10-22 (M8 part A, steps A1–A6; accepted in round 50 (2026-10-22)): the splash is half the damage to the **other** units within 20 px, not to the struck target (D13), and hits units only (not projectiles); all harpoons in flight share one run loop.
- 2026-10-22 (user, [round 50](../../../concept-rounds/round-50/README.md) closed): F.1: the final art (`tools/art/act4_gear.py harpoon`) and its game capture accepted; `art: final`, the concept rows `chosen`. Its sounds (in [sfx](../../../audio/sfx/README.md)): launch b, run a, hit a.
- 2026-10-24 (M8 AW, step AW1; decided by the user 2026-10-23 after playing L22–25: "a faster rate, faster travel,
  a larger blast, less damage a torpedo, homing only in a narrow ±15° cone ahead"): the numbers of the rework's plan
  in [data.yaml](data.yaml): speed 360 → 520 became 540 → 780 px/s (+50 %, `accelerate` 0.4), `cone` 70 → 30, rate
  2.4 / 2.6 / 2.8 / 3 / 1.8 (two tubes) / OD 2, damage 27 / 29 / 31 / 33 / 33 / 35, blast 40 / 40 / 44 / 48 / 48 / 52 px,
  `splash` 0.5 → 0.6; prices, draw, range and turn unchanged. Defaults accepted in round 56 (2026-10-24): the
  splash share 0.6, the turn 90°/s kept.
- 2026-10-24 (user, round 56 closed): the new numbers (C.1) and the four stills (A.1) accepted.
