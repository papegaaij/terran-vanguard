---
title: Hornet
design: draft
implementation: not-started
art: proposed
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../art-direction, ../../../campaign/act-5-the-belt]
updated: 2026-10-27
---

# Hornet

## Summary

An Ascendancy missile boat that hovers at the top of the screen and launches salvos of four slow,
shootable `homing` missiles, drawn as the art direction's red-gold **homing diamond**. Shoot the
missiles down, or kill the boat before its next salvo (Level 31).

## Design

### Stat block

**Draft** (Act 5 design phase, pending the user's review in round 60). A hand-written table: there is
no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does
2), set at its first level from the proposed Act 5 reference DPS (L31 = 128). Fire rates are first
values: the build tunes them per level with `levelReport`.

| Field | Value |
|---|---|
| Faction | Jovian Ascendancy; a **machine** (stunned 3 s by the [EMP Burst](../../../player/specials/README.md#emp-burst-l15)) |
| Layer | `air` |
| Size tier | `medium` |
| Size | 72×64 px, hitbox 52×44 |
| Parts | single (two missile pods drawn on the flanks, not separate parts) |
| Orientation | `fixed` (faces down) |
| HP | 96 (easy 72 / hard 125, from the global multipliers): with the shield, 0.81 s at the L31 reference of 128; missile 2 |
| Armour / shield | hex [shield](../../README.md#enemy-shields) **8** |
| Speed | 140 px/s entering and leaving; holding, a 20 px/s sideways drift |
| Movement | `hover`: enters at the top, holds 90–150 px below the top edge for 9 s with the drift, then exits up |
| Attack | `homing` **salvo of 4** every 5 s, the pods alternating, 0.2 s apart (the first 1.5 s after it reaches its hold): 110 px/s, turn rate 80°/s, 4 s life then they fizzle, **2 HP** each, `medium` = 6 on impact; at most 8 alive per Hornet; the pod hatches glow gold 0.5 s before a salvo (the tell); the [Decoy Flares](../../../player/specials/README.md#decoy-flares-l27) retarget them. Contact `medium` = 15 |
| Formations | `line abreast` (2–3), `single` with Talon escorts |
| Weak points | the pod hatches (drawn only) |
| Effective traits | `forward`, `piercing`, `spread` (for the missiles), `homing`, `shield-breaker` (empties the 8) |
| Credits | 26 (score 260 × chain); missiles 0 |
| Death | `medium` explosion; any missile in a pod at that moment cooks off in a small flash (no bullets); missiles in flight fly on until their life ends |
| First level / used in | L31 (the mining tug's main threat); its later Act 5 levels per the level drafts |
| Difficulty hooks | easy: salvo of 3 every 6 s, turn rate 65°/s; hard: salvo of 4 every 4 s, turn rate 100°/s |

### Behaviour

- **The homing diamond.** Every Ascendancy homing missile of Act 5 (the Hornet's, the SAM Nest's, the
  Strider's pod and the Iron Sovereign's SAM batteries) is drawn as the bullet rules' **diamond**
  (shape = homing, shootable) in red-gold with a short thin exhaust; see the
  [art direction](../../../art-direction/README.md#implementation) item. Its hit box is the diamond's
  core, like a `medium` bullet's.
- **Shootable.** A missile takes 2 HP: one Pulse Cannon L1 bolt does not stop it, two do; spread and
  piercing weapons clear a salvo. The missiles share the game's existing homing projectile (the
  Revenant Walker's pods, the Depth Hunter's torpedo): the speed, turn rate and life above.
- **The tug** (L31): with the [target-the-objective hook](../../README.md#target-the-objective-hook) a
  level may aim a Hornet's salvos at the escorted mining tug instead of the ship; the Level 31 draft
  sets the mode ([Q4](../../README.md#open-questions) a).
- **Sounds** (brief for the audio step): a hovering turbine hum, a hatch clunk for the tell, a short
  hissing rocket launch per missile (real launches from Freesound, CC0/CC-BY, not synthesised), a dry
  pop-crack when a missile is shot down.

### Difficulty and level use

- At most three Hornets and 16 missiles on the screen; a level never pairs Hornet salvos with the
  SAM Nest's in the same seconds unless it says so.

## Concept art

None yet: round 60's unit proposals (the gap analysis's wave 5) draw the Hornet and the homing
diamond, in the belt look of [Q11](../../README.md#open-questions).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (pending the user's review), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 31 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/hornet-r60-a.png](concept/hornet-r60-a.png) | Hornet A, twin-pod boat — a slim fuselage with gold spine and canards, two fat missile pods on gold pylons, gold-capped, two gold-glowing hatches each; strong rim (sheet) | proposed |
| [concept/hornet-r60-a.gif](concept/hornet-r60-a.gif) | Hornet A, twin-pod boat — a slim fuselage with gold spine and canards, two fat missile pods on gold pylons, gold-capped, two gold-glowing hatches each; strong rim (motion) | proposed |
| [concept/hornet-r60-b.png](concept/hornet-r60-b.png) | Hornet B, delta hover-boat — a broad black delta with gold leading edges, four lift fans, the missile cells in the wing roots; a round hex bubble (sheet) | proposed |
| [concept/hornet-r60-b.gif](concept/hornet-r60-b.gif) | Hornet B, delta hover-boat — a broad black delta with gold leading edges, four lift fans, the missile cells in the wing roots; a round hex bubble (motion) | proposed |

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP and the salvo cadence re-measured with `levelReport` at L31
- [ ] Salvos of the existing homing projectile (shootable, flares retarget), the hatch tell, the target-the-objective hook's modes on its missiles
- [ ] The hex shield per [enemy shields](../../README.md#enemy-shields)
- [ ] Production sprites and the homing diamond via `tools/art/`; death effect, bounty and sounds (Freesound, CC0/CC-BY)

## Open questions

- **Q1** (round 60): the shield of 8 follows **a**; **b** and **c** as in the
  [enemies](../../README.md#open-questions) document.

## Decisions

- 2026-10-27 (step m9-d-enemies-a): drafted for the Act 5 design phase from the roster row and the
  gap analysis; every number is a first draft pending the user's review in round 60. The missiles
  reuse the Revenant Walker's homing numbers (110 px/s, 4 s, 2 HP, `medium` 6) with a slower turn
  (80°/s), as four of them come at once.
