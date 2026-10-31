---
title: Plasma Arc
design: approved
implementation: done
art: final
depends-on: [.., ../../generator, ../../../enemies, ../../../systems/economy]
updated: 2026-10-31
---

# Plasma Arc

## Summary

Act 6's front gun: a short-range plasma discharge from the nose that strikes the nearest enemy ahead and
**chains** on to the next ones nearby, three targets at L1 and six at L5 (`area`, `spread`). It is the
area alternative to the Ion Beam for the act's packed waves (Jupiter's Chimera flocks, Aurelia's decks, the
fortress's turret rows), weaker on a single target and strong on a formation. Draft for
[round 72](../../../concept-rounds/README.md), decided 2026-10-31 (Q7 = b); its numbers are in
[data.yaml](data.yaml) since M10 part A.

## Design

<!-- data: weapon-properties -->
| Property | Value |
|---|---|
| Slot | Front gun |
| Traits | `area`, `spread` |
| Layers hit | `air`, `low-air` and `ground` (hardened ground targets take it only with `anti-ground`, which it has not: the arc glances off them and the chain ends there); not `high-air` (an arc needs a body in reach, not a lock, as the [Tesla Coil Pod](../tesla-coil-pod/README.md)'s), not the `sub` layer ([layer rules](../../../enemies/README.md#layer-rules)) |
| Price | 6 000 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 1 500 / 3 000 / 6 000 / 12 000 cr (sell-back per [economy](../../../systems/economy/README.md)) |
| Unlock | L36 (in the shop from the hangar visit before it) |
| Projectile | None: an instant discharge from the nose to the nearest enemy within 200 → 240 px in a 90° cone that opens forward, then on along the chain, each jump to the nearest unit not yet struck within 90 → 120 px (it hits on the frame it fires) |
| Sound family | `tesla` ([weapon sound families](../../../audio/sfx/README.md#weapon-sound-families)) |
| Projectile family (VFX) | `arc` (the Tesla Coil Pod's family drawn wider and hotter (round 72 A.7 a, "the wide Tesla"): a white core with a violet fringe on the first strike 1.7× the pod's width, each jump a thinner fork (0.72×), a spark burst on every struck unit) |
<!-- /data -->

It is in the shop from the L36 visit, the act's first (the fleet's own stock, brought to Jupiter). The arc is drawn
for 0.1 s; its sound family `tesla` replays the zap per jump at a rising pitch. Its look is chosen (round 72 A.7 a,
"the wide Tesla": one family with the Tesla Coil Pod, the jagged bolt 1.7× wide with two forks, each jump 0.72×
thinner).

### Per level

<!-- data: weapon-levels -->
| Level | Chain | Pattern | Rate /s | Damage | Volley DPS | Single-target DPS | Extra | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 3 | arc to the nearest ahead, jumps on | 4 | 7.5 | 69.4 | 30.0 | reach 200 px, jump 90 px | 5 | — |
| 2 | 4 | arc to the nearest ahead, jumps on | 4 | 11.25 | 123.0 | 45.0 | reach 210 px, jump 95 px | 6 | 1 500 |
| 3 | 4 | arc to the nearest ahead, jumps on | 5 | 13 | 177.7 | 65.0 | reach 220 px, jump 100 px | 6.5 | 3 000 |
| 4 | 5 | arc to the nearest ahead, jumps on | 5 | 18 | 274.6 | 90.0 | reach 230 px, jump 110 px | 7.5 | 6 000 |
| 5 | 6 | arc to the nearest ahead, jumps on | 5 | 24 | 394.6 | 120.0 | reach 240 px, jump 120 px | 8 | 12 000 |
| OD | 6 | arc to the nearest ahead, jumps on | 6 | 24 | 473.5 | 144.0 | reach 250 px, jump 130 px | — (ignores cap) | — |

Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`; do not edit by hand. Single-target DPS counts the first strike (it always strikes a target inside its reach). Volley DPS counts a full chain, each jump at 0.75 of the strike before it: the most a discharge does when every jump finds a target; in a thin wave it is the single-target figure. OD = the overdrive pattern (20 s pickup).
<!-- /data -->

DPS L1 and L5 are the roster's (30 / 120); L2–L4 ours; the draw is the roster's 5 → 8 at the common rounding. The
36 px target 100 px away is inside the reach from L1. Its numbers are in [data.yaml](data.yaml) since M10 part A.

### Behaviour

- **First strike:** the nearest valid enemy inside the reach in a **90° cone** that opens forward from the nose
  (45° either side of straight up). With no target there the gun does not fire (no arc into empty space, no draw
  saved), as the Tesla Coil Pod.
- **The chain (`spread`):** from each struck unit the arc jumps to the **nearest unit not yet struck** by this
  discharge within the jump radius of it, in any direction, until the chain's count is reached or no unit is in
  reach. Each jump does **0.75** of the damage of the strike before it. A unit is struck at most once a discharge.
- **`area`:** the chain is the area; there is no splash beside it.
- **Bosses and multi-part units:** the parts of one body count as **one** target: the chain strikes the nearest
  part and jumps on to other units, never to a second part of the same body (else a boss with many parts would
  take the whole chain). A mid-boss of several bodies (the Honour Guard, if Q5 = a) is several targets.
- **Shields:** the arc is not a `shield-breaker`; each strike wears an [enemy shield](../../../enemies/README.md#enemy-shields)
  down at its damage like any shot, the overflow to the hull.
- **Mirrors** (the Mirror Interceptor's [`reflect`](../../../enemies/README.md#reflect), Q1): proposed for that rule,
  whose owner is the enemies' document: there is no projectile to bounce, so a strike on a **standing mirror's
  face** is earthed: it does nothing and **ends the chain** there. A chain that jumps in from a struck neighbour
  can reach the interceptor's open **flank** (the jump comes from the side), which is the arc's answer to a mirror
  line: start the chain beside it.
- **Regeneration** (the Chimera, Act 6's [regeneration rule](../../../enemies/README.md#regeneration)): each strike is a
  hit for the rule's clock, as any shot.
- **Mines and debris:** a [Minelayer](../../../enemies/space/minelayer/README.md)'s mine is never struck by the first
  strike or a jump (as homing shots skip mines); shootable debris (Io's burning rocks) is struck only when nothing
  else is in reach, and a jump never lands on it.
- Its arc passes over walls and rocks to the unit it picked, as the pod's.
- **Under water** (none in Act 6) it would take the energy weapons' × 0.2 of the
  [Europa rules](../../../world/europa/README.md#under-water-rules), as the beam.

### Where it sits

- Against the Ion Beam L5 (130 single-target, 8 MW, the [Act 6 hand plan](../../generator/README.md#act-6-hand-plan-draft)'s
  gun): the arc L5 does 120 on one target for the same 8 MW, and up to 395 on a packed wave; it has a third of
  the beam's reach (240 px against the field's height), does not hit `high-air` (the act's `high-air` units, as Level 36's layers have them, are the beam's
  and the missiles'), and cannot break a mirror.
- The hand plan keeps the beam; the arc is the player's choice for the act's `spread` and `area` levels (L37,
  L40, L41 in the [campaign's pressure table](../../../campaign/README.md#loadout-pressure-and-shop-unlock-pacing)).
  A player who moves to it from the beam L4 pays 6 000 + 4 500 for L3 (65 / 178) or 22 500 more for L5.

## Concept art

Round 72's proposals (step ART-R-A, picked 2026-10-31; generator `tools/concept/enemies_r72.py plasma-arc`): the chain over Level 36's bands against a Chimera swarm, L1 (3 targets) then L5 (6). Each sheet holds a chain of five jumps, the family beside the Tesla Coil Pod's arc, the spark burst, the readability check over the cream and brown bands and key frames. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/plasma-arc-r72-a.png](concept/plasma-arc-r72-a.png) | Plasma Arc a, "the wide Tesla": the arc family's jagged bolt (white core, blue-white glow, violet fringe, two forks) at 1.7× the pod's width, each jump 0.72× thinner, a flash and six sparks per struck unit | chosen (round 72 A.7 a) |
| [concept/plasma-arc-r72-a.gif](concept/plasma-arc-r72-a.gif) | The chain in the play field: L1 on a swarm of eight (they heal between discharges), then L5 on a second swarm | chosen (round 72 A.7 a) |

Production art for concept round 73 (M10 part A, step ART-A), built from the frames by `tools/art/act6_gear.py arc`
(`--review` rebuilds only the sheet; the frames wait in `build/art-held/sprites/` until the weapon's `data.yaml`
exists, then a rerun writes them to `assets/`). Prompts: [concept/prompts.md](concept/prompts.md#plasma-arc-final-r73-a).

| File | What | Status |
|---|---|---|
| [concept/plasma-arc-final-r73-a.png](concept/plasma-arc-final-r73-a.png) | Final frames of A.7 a: the 160×48 strips per place in the chain (the first strike 1.7× the pod's width, each jump 0.72× thinner), three shapes each, the 64 px spark burst, the nose glow, beside the Tesla Coil Pod's arc; in flight at L5 over Level 36's bands with Talon stand-ins, bounced magenta orbs drifting through and a kill on the cream (sheet) | chosen (round 73 F.1 accept) |
| [concept/plasma-arc-final-r73-a.gif](concept/plasma-arc-final-r73-a.gif) | The L5 chain at 5 discharges a second over Level 36's bands, 1× | chosen (round 73 F.1 accept) |
| [concept/plasma-arc-capture-r73-a.png](concept/plasma-arc-capture-r73-a.png) | Game capture (the A4 rig, M10 part A): the chain at L5 with the pods' missiles bounced back as magenta orbs by mirrors, over round 72's Level 36 grade (the mood still as the deep layer), ART-B's cream bands and the rust band; 2× crops: the white core and violet-pink fringe beside the dark-rimmed magenta orbs (they read apart by shape and brightness; the hues are close) | chosen (round 73 F.1 accept) |
| [concept/plasma-arc-capture-r73-a.gif](concept/plasma-arc-capture-r73-a.gif) | The same over the cream bands, 2 s at 15 fps, 1× | chosen (round 73 F.1 accept) |
| [concept/plasma-arc-l35-capture-r73-a.png](concept/plasma-arc-l35-capture-r73-a.png) | Game capture: Level 35 with `--loadout front=plasma-arc:5,shield=reflex-shield`, the chain through the escort screen | chosen (round 73 F.1 accept) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [x] Weapon data in [data.yaml](data.yaml) (written and loaded in step A2a: `hits: chain`, the per-level `chain` and `jump`, the `falloff`, checked by content's `WeaponData`; the simulation's `CHAIN` delivery is step A1b's, its mapping step A2b's) (numbers as above; a `CHAIN` delivery: the instant first strike in
  the forward cone, the jumps with their radius, count and falloff, one target per body) — M10 part A
- [x] The chain against shields, mirrors (earthed on the face, the flank open to a jump), regeneration, mines and debris
  as under *Behaviour* — M10 part A (with the enemies' rules; `PlasmaArcTest`, 2026-10-31)
- [x] Shop entry from the L36 visit; the hangar's trait markers for `area` and `spread` — M10 part A
- [x] The look (the `arc` family wider, the forks thinning per jump, the spark burst per strike) and the `tesla` zap per
  jump at a rising pitch — M10 part A (the look is round 72's A.7 a; part A checks in a capture that its violet fringe and the bounced bullets' magenta read apart, and how explosions read on Level 36's cream band: step A4's captures, round 73; the explosions' fix is a round-73 question)
- [x] `BalanceTest`/`tools/balance.py` count its single-target DPS (the first strike) in the forward DPS

## Open questions

- None open.

## Decisions

- 2026-10-31 (Act 6 design phase, step m10-d-systems; **decided 2026-10-31 (round 72)**): promoted from the
  weapons roster with hand-written numbers (30 / 120 DPS, 5 → 8 MW and 6 000 cr kept; chain 3 → 6, a 90° forward cone,
  reach 200 → 240 px, jumps 90 → 120 px at × 0.75 each, one target per body; an upgrade base of 3 000; in the shop
  from the L36 visit, Q7 = b); the `tesla` sound family the sound table already gave it; the `arc` look drawn wider.
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): **Q7 b** (the middle arsenal:
  this gun at Level 36 with the Reflex Shield, Composite V, the IFF spoofer and the Crucible; a everything and c
  minimal rejected), Q1 c (the arc earths on a mirror's face and the chain ends; the Plasma Arc's chain heats a
  mirror) and the document approved as drafted (D.14; `design: approved`); **A.7 a** (not the recommendation), "the
  wide Tesla", chosen (`art: chosen`; b "the braid" in `concept/rejected/`): one family with the Tesla Coil Pod. The
  violet fringe near the bounced bullets' magenta is accepted, and part A checks in a capture that the two read apart.
  The user's note on the explosions' sharp dark edge is answered: a mockup artifact (see the
  [act](../../../campaign/act-6-jovian-storm/README.md#implementation)).
- 2026-10-31, round 73: the final frames and the game captures accepted (F.1): the arc's violet fringe and the bounced bullets' magenta read apart; `art: final`.
