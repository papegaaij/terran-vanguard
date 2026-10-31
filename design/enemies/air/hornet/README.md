---
title: Hornet
design: approved
implementation: done
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../art-direction, ../../../campaign/act-5-the-belt]
updated: 2026-10-31
---

# Hornet

## Summary

An Ascendancy missile boat that hovers at the top of the screen and launches salvos of four slow,
shootable `homing` missiles, drawn as the art direction's red-gold **homing diamond**. Shoot the
missiles down, or kill the boat before its next salvo (Level 31).

## Design

### Stat block

**Draft** (Act 5 design phase, approved in round 60). Rendered from [data.yaml](data.yaml) since M9 part D (step D2, 2026-10-28; its header lists the
build's own readings). Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does
2), set at its first level from the proposed Act 5 reference DPS (L31 = 128). Fire rates are first
values: the build tunes them per level with `levelReport`.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy (machine: the EMP Burst stuns it longest) |
| Layer | `air` (`space` in the belt's vacuum levels: the same layer) |
| Size tier | `medium` |
| Size | 72×64 px, hitbox 52×44 |
| Parts | single |
| Orientation | `fixed` (faces down) |
| HP | 96 (easy 72 / hard 125, from the global multipliers) |
| Armour / shield | hex [shield](../../README.md#enemy-shields) **12**: spent first, refills 2 s after the last hit; `shield-breaker` and the EMP empty it; missile 2 |
| Speed | 140 px/s entering and leaving; holding, a 20 px/s sideways drift |
| Movement | `hover`: enters at the top, holds 90–150 px below the top edge for 9 s with the drift, then exits up |
| Attack | `homing` **salvo of 4** every 5 s, the pods alternating, 0.2 s apart (the first 1.5 s after it is on the screen): 110 px/s, turn rate 80°/s, 4 s life then they fizzle, **2 HP** each, `medium` = 6 on impact; at most 8 alive per Hornet; the pod hatches glow gold 0.5 s before a salvo (the tell); launched at its target (the ship, or the escorted hull's nearest section under the target-the-objective hook); the [Decoy Flares](../../../player/specials/README.md#decoy-flares-l27) retarget them. Contact `medium` = 15 |
| Formations | line abreast (2–3), single (at most three Hornets and 16 missiles on the screen) |
| Weak points | the pod hatches (drawn only) |
| Effective traits | `forward`, `piercing`, `spread`, `homing`, `shield-breaker` |
| Credits | 26 (score 260 × chain); missiles 0 |
| Death | `medium` explosion; any missile in a pod at that moment cooks off in a small flash (no bullets); missiles in flight fly on until their life ends |
| First level / used in | L31; the mining tug's main threat; its later Act 5 levels per the level drafts |
| Difficulty hooks | easy: salvo of 3 every 6 s, turn rate 65°/s; hard: salvo of 4 every 4 s, turn rate 100°/s |
<!-- /data -->

### Behaviour

- **The homing diamond.** Every Ascendancy homing missile of Act 5 (the Hornet's, the SAM Nest's, the
  Strider's pod and the Iron Sovereign's SAM batteries) is drawn as the bullet rules' **diamond**
  (shape = homing, shootable) in red-gold with a short thin exhaust; see the
  [art direction](../../../art-direction/README.md#implementation) item. Its hit box is the diamond's
  core, like a `medium` bullet's. **Who may lock onto it** (QA-1, decided 2026-10-27, confirmed in round 61 on 2026-10-28): the player's
  homing shots, Rook and the Tesla arc pick a diamond **only when no enemy unit is in reach**.
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

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

None yet: round 60's unit proposals (the gap analysis's wave 5) draw the Hornet and the homing
diamond, in the belt look of [Q11](../../README.md#open-questions).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (gloss black; the user chose look b, gunmetal/charcoal with gold, in round 60, so the chosen variant is re-rendered in gunmetal in production), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 31 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/hornet-r60-b.png](concept/hornet-r60-b.png) | Hornet B, delta hover-boat — a broad black delta with gold leading edges, four lift fans, the missile cells in the wing roots; a round hex bubble (sheet) | chosen (round 60) |
| [concept/hornet-r60-b.gif](concept/hornet-r60-b.gif) | Hornet B, delta hover-boat — a broad black delta with gold leading edges, four lift fans, the missile cells in the wing roots; a round hex bubble (motion) | chosen (round 60) |

## Implementation

- [x] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP and the salvo cadence re-measured with `levelReport` at L31
- [x] Salvos of the existing homing projectile (shootable, flares retarget), the hatch tell, the target-the-objective hook's modes on its missiles
- [x] The hex shield per [enemy shields](../../README.md#enemy-shields)
- [x] Production sprites and the homing diamond via `tools/art/`; death effect, bounty and sounds (Freesound, CC0/CC-BY)
      (ART-D `belt_units_d.py`, the homing diamond part A's; step D4: `MiningRigLooks`, the hover hum, pod hatch and
      launch from round 60/62 picks; accepted in round 64 with game captures)

## Open questions

- **Q1** (round 60): the shield of 8 follows **a**; **b** and **c** as in the
  [enemies](../../README.md#open-questions) document.

## Decisions

- 2026-10-27 (step m9-d-enemies-a): drafted for the Act 5 design phase from the roster row and the
  gap analysis; every number is a first draft approved in round 60. The missiles
  reuse the Revenant Walker's homing numbers (110 px/s, 4 s, 2 HP, `medium` 6) with a slower turn
  (80°/s), as four of them come at once.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`); art A.2 **b**, "Hornet B, delta hover-boat", chosen (`art: chosen`), the other variant in `concept/rejected/`. Q11 **b** (gunmetal/charcoal with gold, not gloss black): the chosen variant is re-rendered in gunmetal/charcoal in this part's production art, keeping the ≥ 20 % gold and the readability check.
- 2026-10-28 (M9 part D, step D2; accepted in round 64, below): the stat block as data. A burst's
  interval counts from its last shot: the salvo every 5 s is `interval: 4.4` (easy 5.6, hard 3.4); the hatch glow is
  the gun's tell (0.5 s). **Shield 8 → 12** (the Talon's round-62 check): under the plan's fit 8 pops in 0.06 s
  (invisible); 12 holds ≈ 0.09 s under the whole forward DPS, ≈ 0.13 s under the beam alone, a visible flash and no
  wall (the Gilded Gunship's 12 on a hull of about the same HP). `levelReport` at L31 (3 seeds): the Hornets live
  5–6.5 s of their 9 s hold at medium (3 s easy, 8–8.6 s hard, the autopilot busy with the missiles), the bow's hits
  within the seed spread at 8, 12, 16 and 24.
- 2026-10-28 (user, [round 64](../../../concept-rounds/round-64/README.md), closed; page answers): the final art accepted (**F.4**: round 60's A.2 b delta hover-boat in Q.11 b gunmetal with gold leading edges, the hatch tell and the lift fans; `art: final`); **C.2** accepted (the hex shield 8 to 12: a visible flash, no wall).
- 2026-10-30 (step FIX7, the user's re-test on f1dc0f05: "the large droneships fly on top of each other"; the new
  big-hull check, `HullStackingTest`): two Hornets holding side by side drifted through each other (their drift's
  side is drawn per unit; Level 31's trio at t=134 overlapped 2.6 s at medium). Its hover now has **`keep_apart:
  true`**: a holding Hornet turns its drift away from another kept-apart hold before their hit boxes come within 8 px
  (the simulation's `HoverHold.keepApart`; the Depth Hunter and the other drifting holds of Acts 1–4 drift as before).
- 2026-10-30, the user: dither audit, ≥ 25 % smoothed (step DITH8): `hornet-hatch-glow`, `hornet-launch` in smooth steps, not the 4×4 Bayer checker ([audit](../../../art-direction/production/README.md#dither-audit-2026-10-30)).
