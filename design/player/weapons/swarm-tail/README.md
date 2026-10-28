---
title: Swarm Tail
design: approved
implementation: done
art: final
depends-on: [.., ../micro-missile-pod, ../tail-gun, ../../generator, ../../../systems/economy]
updated: 2026-10-28
---

# Swarm Tail

## Summary

Act 5's rear gun: micro-missiles fired backwards out of the tail that curve round onto the nearest
enemy **behind or beside** the ship first. It is the homing rear answer to the belt's attacks from
behind (the Minelayers laying their lines, the Rail Serpents' trains entering from the sides and the
rear) and the first rear weapon that reaches `high-air`. Draft for [round 60](../../../concept-rounds/README.md),
pending the user's review (Q12 = c); its numbers are in [data.yaml](data.yaml) since M9 part A.

## Design

<!-- data: weapon-properties -->
| Property | Value |
|---|---|
| Slot | Rear gun |
| Traits | `rear`, `homing` |
| Layers hit | `air`, `low-air`, `ground` (hardened targets glance off) and **`high-air`** (homing), as the [Micro-missile Pod](../micro-missile-pod/README.md) ([layer rules](../../../enemies/README.md#layer-rules)); not the `sub` layer |
| Price | 3 500 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 1 750 / 3 500 / 7 000 / 14 000 cr (sell-back per [economy](../../../systems/economy/README.md)) |
| Unlock | L30 (in the shop from the hangar visit before it) |
| Projectile | 260 → 480 px/s, 4×10 px, range 1.6 s lifetime, seeking within 320 px |
| Sound family | `micromissile` ([weapon sound families](../../../audio/sfx/README.md#weapon-sound-families)) |
| Projectile family (VFX) | `micromissile` (the projectile sheet of concept round 08, the exhaust tinted warmer) |
<!-- /data -->

Each missile is launched backwards at 260 px/s and accelerates to 480 px/s over 0.3 s, seeking within
320 px all round (`pick: rear-first`, below). Its sound family is the `micromissile`, a little lower than
the pod's; its exhaust is tinted warmer so a player can tell his rear swarm from the wing pods'.

### Per level

<!-- data: weapon-levels -->
| Level | Shots | Pattern | Rate /s | Damage | Volley DPS | Single-target DPS | Extra | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 1 | single | 3 | 5 | 15.0 | 15.0 | turn 360°/s | 3 | — |
| 2 | 1 | single | 3.5 | 6 | 21.0 | 21.0 | turn 360°/s | 3.5 | 1 750 |
| 3 | 2 | fan ±25° | 2.5 | 6 | 30.0 | 30.0 | turn 360°/s | 4 | 3 500 |
| 4 | 2 | fan ±25° | 3 | 7 | 42.0 | 42.0 | turn 390°/s | 4.5 | 7 000 |
| 5 | 3 | fan ±30° | 2.5 | 7.33 | 55.0 | 55.0 | turn 390°/s | 5 | 14 000 |
| OD | 3 | fan ±30° | 3 | 7.5 | 67.5 | 67.5 | turn 420°/s | — (ignores cap) | — |

Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`; do not edit by hand; the exact offset and angle of every shot are in that file. Single-target DPS counts the shots that hit a 36 px target 100 px from the muzzle (seeking/lobbed: all shots). OD = the overdrive pattern (20 s pickup).
<!-- /data -->

DPS L1 and L5 are the roster's (15 / 55); L2–L4 ours.

### Behaviour

- **Rear first:** at launch each missile picks the nearest enemy **behind the ship's nose line**
  (below the row of its centre, in the normal scroll) within its seek radius; only with none there
  does it take the nearest enemy anywhere in reach, curving back up past the ship (the roster's
  "curve back up"). With no target it flies straight back and burns out at the end of its lifetime.
  It picks again when its target dies, with the same rule.
- In a reversed section (Level 20's way, where "behind" is the top of the screen) the rule follows
  the scroll, as the other `rear` weapons do.
- It strikes the first hit box it touches and is spent; it never hits the ship, Rook or an ally.
- The Targeting computer's +20 % turn reaches it, as it reaches every homing missile of the ship
  ([ship systems](../../systems/README.md#targeting-computer-user-decision-d6-of-m5-part-a)).
- Mines: a missile never picks a [Minelayer](../../../enemies/space/minelayer/README.md)'s mine as
  its target (homing shots skip mines, the Minelayer's rule: the player chooses where to open a line),
  but one that flies into a mine on its way sets it off like any shot.
- **Homing diamonds** (QA-1, decided 2026-10-27, confirmed in round 61 on 2026-10-28): a Swarm Tail missile may lock onto an enemy
  homing missile (a [homing diamond](../../../art-direction/README.md#implementation)) **only when no enemy unit is in
  reach**; a unit always comes first.
- **Power:** 3 → 5 MW for 15 → 55 DPS: less DPS a MW than the Tail Gun (5 → 11 against its
  10 → 12), and only from L2 more DPS than a Tail Gun L3; what it buys is the homing reach behind and
  to the sides, where every missile counts. The typical plan
  buys it at the L30 visit at L2 and raises it to L3 at the L32 visit
  ([Act 5 hand plan](../../generator/README.md#act-5-hand-plan-draft)).

## Concept art

Production art for concept round 61 (M9 part A, step ART-A), built from the frames by `tools/art/act5_gear.py swarm`
(`--review` rebuilds only the sheet). The Swarm Tail has no mount sprite: rear guns fire from the hull's rear mount.
Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/swarm-tail-final-r61-a.png](concept/swarm-tail-final-r61-a.png) | Swarm Tail final frames: the micro-missile at 32 headings with an ivory-white plume (the wing pods' is cyan; no enemy hue), beside the Micro-missile Pod's; L3 pairs launched backwards curving onto stand-ins behind the ship (sheet) | chosen (round 61, F.4: accepted) |
| [concept/swarm-tail-final-r61-a.gif](concept/swarm-tail-final-r61-a.gif) | L3 pairs fired backwards, accelerating and curving onto round 60's Talon stand-ins, 2x | chosen (round 61, F.4: accepted) |
| [concept/swarm-tail-capture-r61-a.png](concept/swarm-tail-capture-r61-a.png) | Game capture, M9 part A (step A4; the Tesla Coil Pod's Level 08 run, [its video](../tesla-coil-pod/concept/tesla-coil-pod-capture-r61-a.mp4)): three 2× crops round the ship with the Swarm Tail's missiles leaving backwards; at game size the ivory plume reads as a thin white streak | chosen (round 61, F.4: accepted) |

## Implementation

- [x] Weapon data in [data.yaml](data.yaml) (numbers as above; the homing delivery fired backwards with the
  rear-first pick and its `accelerate`) — M9 part A (step A2: `pick: rear-first`, `WeaponData.Pick`, `WeaponRules`;
  the turn rates ×1.5, see *Decisions*; `BeltKeysTest.theSwarmTailIsARearFirstHomingMissileLaunchedBackwards`,
  `theSwarmTailStrikesATargetAheadBesideAndBehind`)
- [x] The rear-first target pick and the re-pick on a kill; the reversed-scroll rule; the Targeting computer's turn — M9
  part A (step A1: `WeaponSpec.Seek.REAR_FIRST`, `SwarmTailTest`; step A2: the Targeting computer's +20 % from the data)
- [x] Shop entry from the L30 visit; the balance plan's purchases (see the hand plan) — M9 part A (step A2: `unlock: 30`,
  the plan's L30 and L32 visits, `BalanceTest`, `tools/balance.py`)
- [x] The warmer exhaust tint and the lower `micromissile` pitch — M9 part A (step A3: the ivory-white plume
  `swarm-tail-shot` at 32 headings drawn by `WeaponLooks`; the rear slot's `micromissile` at 0.9, `PlayerCues`;
  `ActFiveLooksTest`; seen on Level 01 with `--loadout`)

## Open questions

- Q12 of [round 60](../../../concept-rounds/README.md) (Act 5's arsenal): recommended (c), the
  Tesla Coil Pod and this weapon with the core tiers. Alternatives: (a) lean, without this weapon (the
  Tail Gun and the Fan Blaster stay the rear answers, the rear Minelayers fall to Rook and the beam's
  sweep); (b) the full roster (Plasma Arc, Time Dilation, the Hunter drone and the Bastion Shield in
  Act 5 too).

## Decisions

- 2026-10-27 (Act 5 design phase, step m9-d-systems; **approved in round 60**):
  promoted from the weapons roster with hand-written numbers (15 / 55 DPS, 3 → 5 MW and 3 500 cr
  kept, L2–L4 ours, the L3 pair and the L5 fan of three), the rear-first pick, the `micromissile`
  families with a warmer tint.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`, D.14); Q12 **c**. **QA-1** (the main agent's decision of 2026-10-27 from the part A gap check, confirmed in round 61 on 2026-10-28): a Swarm Tail missile may lock onto a homing diamond only when no enemy unit is in reach.
- 2026-10-27 (M9 part A, step A2; confirmed in round 61, 2026-10-28): the turn rates are the draft's ×1.5,
  **360 / 360 / 360 / 390 / 390°/s, overdrive 420** (were 240 / 240 / 240 / 260 / 260 / 280). Measured from the data on
  a test range (a 36 px target coming down the screen at the ship's x and 120 px beside it): at 240°/s and 480 px/s
  (a turn radius of 115 px) a missile launched backwards circles a target ahead of the ship, or beside it, and never
  strikes it (0 DPS at L1); at 360°/s it strikes at about its DPS ahead, beside and behind within its seek radius.
  Speed, lifetime, seek radius and DPS unchanged.
- 2026-10-28 (user, [round 61](../../../concept-rounds/round-61/README.md), closed): the final art accepted (**F.4**, the ivory-white plume); **C.2** the turn rates ×1.5 confirmed; **C.1** (QA-1) confirmed.
