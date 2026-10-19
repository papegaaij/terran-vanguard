---
title: Warden Tank
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../campaign/act-3-red-dust]
updated: 2026-10-13
---

# Warden Tank

## Summary

A human-built tracked tank: the hull drives its own heading along roads and the Tharsis foundry's
conveyors while a separate turret tracks the ship and fires bursts, even back up the screen after
it has passed. In Act 3 it is **unmarked grey with no trim** (a hint of human involvement, Level
19); from Act 5 it appears in Ascendancy black and gold.

## Design

### Stat block

Hand-written draft numbers at **medium** (M19: no `data.yaml` until its level is built; see the
[balancing basis](../../README.md#balancing-basis)). HP is in damage units (a Pulse Cannon L1 shot
does 2). Set at its first level from the proposed Act 3 reference DPS (M18 = a, approved in round 36:
L19 = 92): the hull's 64 HP is 0.7 s, the turret 0.2 s. Where it returns (Act 6, L39 and L41) it
gets the act HP factor of a `medium` unit (every part).

| Field | Value |
|---|---|
| Faction | Unmarked (Ascendancy) in Act 3; Ascendancy from Act 5 |
| Layer | `ground` |
| Size tier | `medium` |
| Size | 64×64 px (the concept), hull hitbox 46×56; turret 32×32, hitbox 22×22 |
| Parts | hull (`vital`), turret (`destroyable`: silences its gun) |
| Orientation | hull `16 angles` (2 tread frames), turret `32 angles`, each with its own heading |
| HP | hull 64 · turret 20; 84 in all (easy ×0.75 / hard ×1.3, from the global multipliers) |
| Armour / shield | rear hull plate is the weak point: direct shots arriving within ±50° of its rear do ×1.5 to the hull (the Scuttler's arc rule reversed) |
| Speed | 40 px/s along its path (plus scroll; plus the conveyor's speed when on one) |
| Movement | `walk` (tracked) along an authored road or conveyor path, turning at 60°/s; the turret turns at 120°/s toward the ship independently |
| Attack | 3-shot `burst` every 2.5 s, the shots 0.15 s apart along the turret's facing once it is within 10° of the ship; standard 160 px/s, `medium` 6; it fires in any direction, up the screen too |
| Formations | convoy (2–4, one path, 1.5 s apart) |
| Weak points | the rear hull plate (×1.5 from behind); exhaust vents drawn glowing |
| Effective traits | `anti-ground`, `rear` (its plate faces the ship after it passes), EMP Burst (a machine: stunned 3 s, see [specials](../../../player/specials/README.md)) |
| Credits | whole (hull first) 26; parts: hull 20, turret 8 (28) |
| Death | `medium` mechanical explosion with sparks and smoke; the burnt hull stays as a wreck (grey, no markings) |
| First level | L19 (Foundry of Tharsis); returns L39, L41 (Act 6) in Ascendancy livery |
| Difficulty hooks | easy: 2-shot burst every 3.0 s; hard: 4-shot burst every 2.0 s, turret 160°/s |

### Behaviour

- **Two sprites.** Hull and turret are drawn and turned separately: the hull faces its path, the
  turret its target. The turret's barrel recoils with each shot and a muzzle flash marks the
  burst's start (the tell: the turret stops turning for 0.3 s before the first shot).
- **Firing behind.** Its turret tracks the ship all round, so a tank that has scrolled past keeps
  firing up the screen; this is part of Level 19's "turrets fire from behind as you pass" and
  why the rear plate is the weak point.
- **Conveyors.** On a conveyor path its ground speed adds to the belt's (a `Road` that moves
  relative to the ground, §2.8 of the gap analysis); a convoy rides the belt nose to tail.
- **EMP.** As a machine it is stunned for 3 s by the EMP Burst: tracks and turret stop, no fire.
  (Its unmarked grey makes the point: a Vrell unit would only be stunned 1.5 s.)
- **Hint.** In Act 3 nothing on it identifies a maker: grey hull, no trim, no rim light, no
  insignia. The wreck is the same. Radio may remark on it in Level 19, but the main hint there is
  the Revenant Walker.
- **Code fit.** Hull `walk` and the `EnemyGun` turret turn rate exist separately; the two-sprite
  unit with independent headings is new (§2.8 of the gap analysis).

### Difficulty and level use

- Burst and interval per the stat block; the global multipliers apply.
- Its burst fires along the turret's facing, so a level may give it the `in-arc` objective hook.

## Concept art

Chosen concept from [round 05](../../../concept-rounds/round-05/README.md) (listed in the
[ground](../README.md#concept-art) Concept art table): hull at 16 headings following a road,
turret at 32 headings tracking the player, tread marks. It is chosen **in Ascendancy livery only**
(black and gold, white accent, rim light), as used from Act 5. The **Act 3 unmarked-grey variant**
(the same model, grey, no trim) is proposed in concept round 36 (step A-units; prompts in
[../concept/prompts.md](../concept/prompts.md), generator `tools/concept/enemies_r36.py`), accepted in round 36 (U.6, 2026-10-13): the Act 3 livery is chosen too.

| File | What | Status |
|---|---|---|
| [../concept/warden-tank-r05-a.png](../concept/warden-tank-r05-a.png) | Warden Tank — Ascendancy tank (white accent): hull 16 headings following a road, turret 32 headings tracking the player, tread marks (sheet) | chosen — Ascendancy livery |
| [../concept/warden-tank-r05-a.gif](../concept/warden-tank-r05-a.gif) | Warden Tank — the same in motion | chosen — Ascendancy livery |
| [../concept/warden-tank-r36-a.png](../concept/warden-tank-r36-a.png) | Warden Tank, unmarked grey — the r05-a model with only its livery changed (no gold, no accent), side by side with r05-a (sheet) | chosen (round 36, U.6: the unmarked grey livery) |
| [../concept/warden-tank-r36-a.gif](../concept/warden-tank-r36-a.gif) | Warden Tank, unmarked grey — on a dirt road on Mars (motion) | chosen (round 36, U.6: the unmarked grey livery) |

## Implementation

- [ ] Two-part unit: hull (`16 angles`) and turret (`32 angles`) with independent headings and turn rates
- [ ] Tracked `walk` along roads and conveyors (belt speed added)
- [ ] Turret burst: 0.3 s still before it fires, 3 shots 0.15 s apart, every 2.5 s, any direction
- [ ] Rear plate ×1.5 within ±50° of the rear; the turret's loss silences it
- [ ] EMP stun 3 s (machine)
- [ ] Unmarked grey livery and wreck for Act 3; Ascendancy livery from Act 5
- [ ] Data file with the stat block (when Level 19 is built)
- [ ] Production art (round of part F)

## Open questions

- None open: round 36 answered it (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the [ground roster](../README.md) to a draft
  spec with the round-05 concept (Ascendancy livery) as its chosen model; the Act 3 grey variant
  is proposed by step A-units. M18 = a taken as the recommended option, pending the user's review
  in round 36. Our proposals: hull 64 and turret 20 as parts, the ±50° rear plate at ×1.5, the
  3-shot burst every 2.5 s in any direction, bounty 26 whole / 28 in parts, intel label
  "unidentified".
- 2026-10-12 (merge): the round-36 unmarked-grey livery proposal is listed under Concept art.
- 2026-10-13 (user, round 36): approved (D.14). Q.22f yes: the intel lists the Act 3 tanks as "unidentified", not Ascendancy. U.6 accepted: the unmarked grey livery (the r05-a model, no gold, no accent, the red sensors kept) is chosen for Act 3.
