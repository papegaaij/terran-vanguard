---
title: Shields
design: approved
implementation: done
art: final
depends-on: [../generator, ../armor, ../../systems/retry, ../../enemies]
updated: 2026-10-31
---

# Shields

## Summary

The shield absorbs damage before armour and regenerates after a short delay without taking hits.
It is the player's renewable buffer; [armour](../armor/README.md) is the finite one.

## Design

<!-- data: shields -->
| Model | Capacity | Regen /s | Regen delay | Draw | Price (first draft) | Available |
|---|---|---|---|---|---|---|
| Mk I | 20 | 2 | 2.0 s | 2 MW | starter | start |
| Mk II | 30 | 3 | 1.8 s | 3 MW | 1 000 | act 1 |
| Mk III | 45 | 4 | 1.5 s | 4 MW | 3 000 | act 2 |
| Mk IV | 60 | 5 | 1.2 s | 5 MW | 7 000 | act 4 |
| Mk V | 80 | 6 | 1.0 s | 6 MW | 14 000 | act 5 |
| Reflex Shield | 40 | 6 | 1.0 s | 6 MW | 12 000 | L37 (Ascendancy mirror tech) |
<!-- /data -->

Variant shields (roster, alternative to the Mk line):

| Name | Summary | Design |
|---|---|---|
| Reflex Shield | Capacity 40, regen 6/s, 1.0 s delay; a bullet absorbed while the shield holds at least 75 % costs half its damage and flies back up the screen as a bolt of the ship's. 6 MW, 12 000, L37 (see *Reflex Shield (Act 6)* below) | draft |
| Bastion Shield | Capacity 120, regen only 2/s, 3.0 s delay. For tanky builds. 5 MW, 9 000, act 7 (was act 5, then act 6; Q7 = b of round 72, decided 2026-10-31) | idea |

Rules:
- Damage goes to the shield first. Overflow damage in the same hit goes to armour.
- **Shield break**: when the shield hits 0 it stays down for an extra 1.0 s before the regen
  delay starts. An alarm plays and the HUD bar flashes.
- Spare generator power boosts the regen rate (see [generator](../generator/README.md)).
- Collisions deal half damage to shields and half directly to armour, so ramming stays risky.
- **Under a generator drain** (Act 5, the Void Leech, L33): when the drained output no longer covers
  the load, the shield's **regeneration stops first**, before any weapon slows; the shield keeps its
  charge and the hex ring is drawn dimmed until the load fits again. The rule and its numbers are the
  [generator's](../generator/README.md#enemy-drain-effects) (draft, approved in round 60).

**Act 5** (draft, decided in round 60, Q12 = c): the Mk V (80, 6 MW, 14 000) is in the shop from the L29
visit as listed; the typical plan does not reach it (its 6 MW need the Mk V generator, see the
[Act 5 hand plan](../generator/README.md#act-5-hand-plan-draft)). The Bastion Shield moves to Act 6 with
the Reflex Shield: a variant line belongs with the heavier shields of Act 6, and Act 5's income does not
stretch to it.

### Reflex Shield (Act 6)

*Draft, step `m10-d-systems` (2026-10-31), **decided 2026-10-31 (round 72)** (Q7 = b: "mirrors answering
mirrors"). Promoted from the roster row; its row (capacity, regen, delay, draw, price, availability and the `reflect`
window, share, bolt speed and factor) is in [data.yaml](data.yaml) since M10 part A, step A2a. A variant shield: it
replaces the Mk shield in the shield slot.*

| Property | Value (draft) |
|---|---|
| Capacity | 40 |
| Regen | 6 /s after a **1.0 s** delay (the Mk V's), so the reflect window comes back quickly |
| Draw | 6 MW (2 more than the Mk III the typical plan flies) |
| Price | 12 000 |
| Available | the L37 visit (Ascendancy mirror tech from Aurelia's wrecks: the city's picket interceptors the carrier group downs as it breaks the air-defence grid, the act intro's plan) |
| Reflect window | while the shield holds **at least 75 %** of its capacity (30 of 40) when a bullet hits |
| Reflected | an enemy **bullet** (any size, a death-burst ring's) and an enemy missile (the homing diamonds); the shield takes **half** the bullet's damage and the bullet comes back as a **bolt of the ship's** |
| The bolt | from the hit point **straight up the screen** at 640 px/s; damage **twice** the bullet's class (`small` 8, `medium` 12, `heavy` 20; a missile as its class); hits `air`, `low-air` and `ground` (hardened ground targets glance off), not `high-air` or `sub`; it pays bounties and counts as the ship's kill; it is not in any DPS measure |
| Never reflected | a mirror's bounced bullet (the enemies' once-only rule), lasers (`laser-sweep`, `laser-line`), beams, lightning strikes, eruption rocks and debris, contact and rams, a boss's vents, cones and slams: these hit the shield as an ordinary shield (full damage) |
| Below 75 % | an ordinary shield: full damage, the break and its alarm as above |

- **Mirrors answering mirrors** (Q1, the Mirror Interceptor's [`reflect`](../../enemies/README.md#reflect)): the
  Ascendancy's mirrors turn the player's shots round, the Reflex turns the Ascendancy's bullets round. The enemies'
  **once-only** rule holds both ways, so nothing ping-pongs: a mirror's bounced bullet is never bounced again (the
  Reflex takes it as an ordinary hit, no bolt), and a **bolt** is itself a bounced bullet, so on a **standing mirror's
  face** it is absorbed with a spark (no damage, no bounce; the bolt side is proposed here for the enemies' rule). A
  bolt hits a mirror's open flank like any shot.
- **Spare power** speeds its regeneration as any shield's (the [generator](../generator/README.md)); the drain rule
  sheds its regeneration first as any shield's.
- **The Shield Overcharge** special doubles its capacity for 8 s (80): the window is 75 % of the capacity at the hit
  (60 of 80), so an overcharged Reflex reflects about eleven small bullets in a row where a full one reflects six.
- **Look:** while the window is open the hit's hex flash is **gold** instead of blue and the bolt leaves from it,
  drawn as the bullet's shape in the shield's white-gold with a short trail; the HUD shield bar's top quarter is gold
  while the window is open ([HUD](../../ui/hud/README.md#act-6-additions-draft)). **Sound:** a bright ricochet ping
  over the shield hit (a real recorded ricochet, an Act 6 sound for round 72; no bubbly or popping sound).
- **Where it sits:** half the Mk V's capacity for its 6 MW, paid back by the bolts in a dense bullet field (a full
  shield turns the first six small bullets of a burst into 8-damage shots and loses only 2 for each). It
  rewards a player who keeps the shield topped and punishes one who flies through bursts. The typical plan leaves it
  out (it competes with the IFF spoofer for the Mk V+'s room, see the
  [Act 6 hand plan](../generator/README.md#act-6-hand-plan-draft)).
- **The Bastion Shield moves to Act 7's design** (Q7 = b): one variant shield per act; a tanky shield fits Act 7's
  heavy endgame better than an act whose shop already carries Composite V.

## Concept art

Production art for concept round 26 (the M4 part H batch), review files built from the final frames in `assets/` by `tools/art/ship_fx.py` (`--review` rebuilds only them); prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/shield-ring-final-r26-a.png](concept/shield-ring-final-r26-a.png) | Final shield-hit ring (`ship-shield_0..3`, 60×60, additive): round 08's hex shimmer on a round bubble, the cells lighting up around a hit at the front and a ripple running back, fading over 4 frames; alone and over the hull | chosen |
| [concept/shield-ring-final-r26-a.gif](concept/shield-ring-final-r26-a.gif) | The ring over the hull at the game's 2 steps a frame | chosen |

Round 73 (M10 part A, step ART-A): the Reflex Shield's production frames by `tools/art/act6_gear.py reflex` and the
bolt's open look (QA-3), a/b proposals by `tools/concept/act6_r73.py bolt` (a written to the game's files until the
pick). Prompts: [concept/prompts.md](concept/prompts.md#reflex-shield-final-r73-a).

| File | What | Status |
|---|---|---|
| [concept/reflex-shield-final-r73-a.png](concept/reflex-shield-final-r73-a.png) | Final frames: the gold hex flash `ship-reflex_0..3` (60×60, the blue ring's shapes in gold) beside the blue one, the bolt `ship-reflex-bolt_0..3` (a) and its trail; over Level 36, the window open (gold flash, a bolt up per hit) beside below 75 % (sheet) | proposed (round 73) |
| [concept/reflex-shield-final-r73-a.gif](concept/reflex-shield-final-r73-a.gif) | The two cases side by side, 2× | proposed (round 73) |
| [concept/reflex-bolt-r73-a.png](concept/reflex-bolt-r73-a.png) | QA-3 a "white-gold bullet": the bolt in the enemy bullet's round shape in white-gold with a dark rim and a short gold trail, beside the enemy orb, the gold needle and the bounced orb on Level 36's grade, and in flight (sheet) | proposed (round 73 QA-3) |
| [concept/reflex-bolt-r73-a.gif](concept/reflex-bolt-r73-a.gif) | a in flight: orbs on the full Reflex Shield, gold flashes, bolts up, needles and a bounced orb drifting through, 2× | proposed (round 73 QA-3) |
| [concept/reflex-bolt-r73-b.png](concept/reflex-bolt-r73-b.png) | QA-3 b "gold needle": a thin gold needle with a white core, a dark rim and a faint trail, the same layout (sheet) | proposed (round 73 QA-3) |
| [concept/reflex-bolt-r73-b.gif](concept/reflex-bolt-r73-b.gif) | b in flight, 2× | proposed (round 73 QA-3) |

## Implementation

- [x] Shield capacity, regen, delay and break behaviour
- [x] Damage routing shield → armour, collision split
- [x] Shield hit/break feedback: the blue shimmer on the hull (`FlashShader`, `LevelRenderer`),
  the hit and break sounds (`Sfx.SHIELD_HIT`, `SHIELD_BREAK`), the HUD shield bar flickering while
  it is down after a break (`ShipPanel`)
- [x] The 60×60 4-frame hex-shimmer ring sprite on shield hits (the [ship](../ship/README.md)'s
  asset table; `ShipLooks`, frames by `tools/art/ship_fx.py`)
- [x] The shield-restore chime ([sfx](../../audio/sfx/README.md), part H's SFX pass: once the
  shield is full again after a break, `FlightSounds`)
- [ ] The Reflex Shield as in *Reflex Shield (Act 6)* (draft): its row in [data.yaml](data.yaml) (a variant with the
  reflect window, the half cost and the bolt; written and loaded in step A2a, `ShieldData.Reflect`, `JovianKeysTest`;
  the simulation's reflect is step A1b's, its mapping step A2b's), the bullet-to-bolt hand-off in the simulation (deterministic, hashed
  only when fitted), the once-only rule against the mirrors, the gold hex flash, the bolt's look, the
  ricochet sound, in the shop from the L37 visit — M10 part A

## Open questions

- The Reflex's numbers (40 capacity, the Mk V's regen, the half cost, the bolt at twice the bullet's damage) are a first
  draft for part A to measure; a softer variant reflects at full cost (the shield loses the bullet's whole damage).

## Decisions

- 2026-09-30: Shield regenerates, armour does not (user decision).
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-01: M1 implementation (`vanguard.sim.Defences`, Mk I numbers in `ShieldModel`): every hit that lands (shield or armour) restarts the 2.0 s delay; a break holds the shield at 0 for 1.0 s + the delay; in a collision the shield's half overflows to armour like a bullet's. Feedback so far: shield-hit and break sounds, a blue shimmer on the hull, the HUD shield bar flickering while down after a break; the hex-ring shimmer sprite is still to come.
- 2026-10-02: The shield models and the break time moved into [data.yaml](data.yaml) (M2 data files); the model table is rendered from it and `vanguard.sim.ShieldModel` is built from it.
- 2026-10-05: M4 part H docs reconciliation: the feedback item is split into the built part (hull
  shimmer, hit and break sounds, the HUD bar's flicker) and what is still missing: the hex-ring
  sprite of the ship's asset table and the shield-restore chime. The document stays
  `in-progress`.
- 2026-10-05: M4 part H: the hex-shimmer ring is drawn additively over the ship on every shield
  hit, its 4 frames over the 8-step shimmer (2 steps each), with the hull's blue shimmer under it.
  The hit point is fixed at the front of the bubble (most bullets come from ahead) rather than at
  the bullet's side. Frames by `tools/art/ship_fx.py`, proposed for round 26 (`art: final`).
- 2026-10-05: Concept round 26 closed (user: the round accepted as proposed): the hex-shimmer ring approved as **final** (`art: final`), with the hit point at the front of the bubble and the 4-frame fade as they are; the restore chime is played by part H's SFX pass, so every item is ticked and the document is `done`.
- 2026-10-27 (Act 5 design phase, step m9-d-systems; **approved in round 60**): under a generator
  drain the shield's regeneration stops first and its ring dims (Q2 = a, the rule in the generator); the Bastion
  Shield moves to Act 6 (Q12 = c; under (b) it stays in Act 5); the Mk V stays in Act 5.
- 2026-10-31 (Act 6 design phase, step m10-d-systems; **decided 2026-10-31 (round 72)**): the Reflex Shield
  promoted from the roster to a draft (Q7 = b: capacity 40, regen 6/s after 1.0 s, 6 MW, 12 000, from the L37 visit; a
  bullet absorbed at 75 % or more costs half its damage and returns straight up as a bolt at twice the bullet's
  damage; lasers, beams, hazards and a mirror's bounced bullets never reflected, a bolt absorbed by a standing mirror,
  the enemies' once-only rule); the Bastion
  Shield moves to Act 7.
- 2026-10-31, design phase (step m10-d-act, the main agent's call; decided 2026-10-31 (round 72)): the Reflex Shield's mirror tech
  comes **from Aurelia's wrecks** (the city's pickets), not the Cloud Divers' (Level 36 flies no mirror); the visit
  (L37) stays Q7 b's.
- 2026-10-31 (user, [round 72](../../concept-rounds/round-72/README.md) closed): Q7 b: the Reflex Shield at Level
  37 (mirrors answering mirrors; it never re-bounces a bullet), the Bastion Shield to Act 7; a both and c neither
  rejected. The Reflex's numbers stay for part A to measure.
