---
title: Strider
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../bosses/revenant-walker]
updated: 2026-10-27
---

# Strider

## Summary

A two-legged Ascendancy walker mech that stalks across tunnel floors and station hulls in pairs, firing fans
from twin cannons and homing diamonds from a shoulder pod. Its legs are its weakness: destroy one and it
staggers, then **topples** along a marked line, crushing any ground unit beneath it.

## Design

### Stat block

**Draft** (Act 5 design phase, step D-enemies-B; approved in round 60). A hand-written
table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2),
set at its first level from the proposed Act 5 reference DPS (judgement call J2: L32 132): the hull's 220 is
1.7 s (a `large` part, 1–3 s), a leg's 80 is 0.6 s, so the legs are the quick kill for a player who aims.
Fire rates are tuned per level with `levelReport`.

| Field | Value |
|---|---|
| Faction | Ascendancy; a **machine** (stunned 3 s by the EMP Burst) |
| Layer | `ground` |
| Size tier | `large` |
| Size | 120×120 px (the chosen r05 concept); hitboxes: hull 64×64 on the centre, each leg 22×40 at ±42 px across its heading, the pod 24×24 on the left shoulder |
| Parts | hull (`vital`), left and right leg (`destroyable`: either one topples it), shoulder pod (`destroyable`: silences the missiles) |
| Orientation | legs and hull `16 angles` × an 8-frame walk cycle; the torso `32 angles`, twisting toward the ship to fire |
| HP | hull 220 · leg 80 each · pod 30; whole 410 (easy 308 / hard 533, from the global multipliers) |
| Armour / shield | none (not on the [enemy shields](../../README.md#enemy-shields)' Act 5 list) |
| Speed | 32 px/s along its path (plus the scroll), turning 45°/s |
| Movement | `walk` along an authored path on a tunnel floor or a hull road; a pair walks 1.5 s apart or on mirrored paths |
| Attack | twin cannons: a `fan` of 3 (12° apart) along the torso's aim at the ship, every 2.4 s, standard 160 px/s, `medium` 6; tell: the torso holds still 0.3 s and the muzzles glow. Shoulder pod: a `homing` salvo of 2 (the homing diamond, as the [SAM Nest](../sam-nest/README.md)'s and the Hornet's missile: 110 px/s, 80°/s, 2 HP, `medium` 6, life 4 s) every 5 s; tell: the hatch opens 0.4 s |
| Formations | solo, pairs |
| Weak points | the legs (destroyable parts; the knee joints glow red when hit) and the pod |
| Effective traits | `anti-ground`, `piercing`, `homing` |
| Credits | 60 (score 600 × chain); parts: hull 40, legs 6 each, pod 8; a topple pays every part not yet paid (the whole 60) |
| Death | hull destroyed: a `large` mechanical explosion in place, the burnt hulk stays as a wreck; toppled: see *The topple* |
| First level / used in | L32 (Deep Core, the tunnel floors); later Act 5 levels may use it (their docs decide), with the act HP factor of a `large` unit |
| Difficulty hooks | easy: a fan of 3 every 3.0 s, salvos of 1 every 6 s; hard: a fan of 5 every 2.0 s, salvos of 3 |

### The topple

The gap analysis's decision (the shared [topple](../../README.md#topple) rule): the topple is presentation plus a
one-time **crush**.

1. **Stagger** (0.6 s, the tell): a destroyed leg sparks at the knee, the hull lurches toward it, and the
   **fall line** is marked on the ground: a dark strip 48 px wide and 140 px long from its centre on the lost
   leg's side (turned with the heading). The guns stop.
2. **Fall** (0.8 s): it falls along the line and lands with a ground shock and a dust ring.
3. **Crush**: on landing it is destroyed (all its parts paid) and every **ground** unit whose hitbox overlaps
   the strip is destroyed too, hardened ones included (a Rail Bunker, a Sentinel's collar), their credits paid
   to the player and counted as kills.
4. The toppled hulk stays as a wreck along the line.

It never hurts the ship (ground never collides with the play plane) and never touches `air` units. Total:
1.4 s from the leg's loss to the crush.

### Behaviour

- **Target choice.** The legs are smaller and move with the gait; a leg falls in under half the hull's time,
  the reward for aim, and its fall line can be aimed too (shoot the leg on the side of the bunker it should
  fall on). `anti-ground` weapons do ×2 to every part.
- **The torso** twists to fire across its heading; a Strider walking past keeps firing up the screen.
- **EMP.** Stunned 3 s: it freezes mid-stride, no fire; a stagger under way still falls.
- **Code fit.** The `Walking`/`WalkPath` walkers and the [Revenant Walker](../../bosses/revenant-walker/README.md)'s
  "Strider heavy" biped kit (`revenant_walker.py`) for the art; destroyable legs with a topple are new (gap
  analysis §2.8, size S).

### Difficulty and level use

- L32's tunnel floors, in pairs; the level places a few ground units (turrets, a bunker) where a topple can
  crush them. At most two Striders walking at once.
- In the branch the player picks (Q5 a), the richer branch's Striders guard the cache.

### Sounds

Brief for the round-60 sound proposals (Freesound CC0/CC-BY, real recordings, no bubbly or popping
sounds): each step a heavy metal stomp with a short hydraulic hiss (a hydraulic press or a car lift); the
torso a servo whine; the cannons a deep double gunshot; the leg's loss a metal snap with sparks; the fall a
long metal groan into a heavy crash and rumble.

## Concept art

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

Chosen concept from [round 05](../../../concept-rounds/round-05/README.md), listed in the
[ground](../README.md#concept-art) Concept art table: a 120 px Ascendancy biped (red accent), an 8-frame walk
cycle, the torso at 32 headings twisting to fire its shoulder cannons
([strider-r05-a.png](../concept/strider-r05-a.png), [GIF](../concept/strider-r05-a.gif); chosen, Ascendancy
only). Prompts: [../concept/prompts.md](../concept/prompts.md). The round-60 proposals add what the concept
lacks (step ART-P-units): the shoulder pod, the knee glow, the stagger and the topple with its fall line, and
the readability check against the tunnel floor (Q11).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (gloss black; the user chose look b, gunmetal/charcoal with gold, in round 60, so the chosen variant is re-rendered in gunmetal in production), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 32 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/strider-r60-b.png](concept/strider-r60-b.png) | Strider B, buckle and crumple — the lost leg's knee buckles, it pitches over and the torso breaks off the legs: two pieces (sheet) | chosen (round 60) |
| [concept/strider-r60-b.gif](concept/strider-r60-b.gif) | Strider B, buckle and crumple — the lost leg's knee buckles, it pitches over and the torso breaks off the legs: two pieces (motion) | chosen (round 60) |

## Implementation

- [ ] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers applied; tuned with `levelReport`
- [ ] `walk` on a path with the twisting torso (`32 angles`); the fan and the pod's homing salvos with their tells
- [ ] Destroyable legs and pod; the stagger tell, the marked fall line, the fall and the one-time crush of ground units (credits paid)
- [ ] Production sprites from the chosen r05 concept with the pod, the stagger and topple frames and the wreck; the intel portrait
- [ ] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds)

## Open questions

- Q5 (decided in round 60): L32's branching tunnels, **a** recommended; alternatives **b** (cosmetic forks) and
  **c** (the same waves in both).
- Q11 (decided in round 60, **b**, not the recommendation): the Ascendancy look on the belt is gunmetal/charcoal hulls
  with gold (≥ 20 % gold, rim light, red running lights); a (gloss black) and c (black silhouettes) rejected
  ([enemies](../../README.md#factions-and-visual-language)).

## Decisions

- 2026-09-30: added in the enemy variety pass; concept round 05 chosen (Ascendancy only: too mechanical for the Vrell).
- 2026-10-27 (step D-enemies-B): promoted from the [ground roster](../README.md) to a draft spec for the Act 5
  design phase; every number is a first draft, approved in round 60.
- 2026-10-27 (the gap analysis's decision): the topple is presentation plus a one-time crush of ground units
  under its fall line (credits paid to the player); it never hurts the ship.
- 2026-10-27 (decided by me, trivia): either leg topples it; the fall goes to the lost leg's side; hardened
  ground units are crushed too (a way to break a Rail Bunker without an `anti-ground` gun).
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`); art A.14 **b**, "Strider B, buckle and crumple", chosen (`art: chosen`), the other variant in `concept/rejected/`. Q11 **b** (gunmetal/charcoal with gold, not gloss black): the chosen variant is re-rendered in gunmetal/charcoal in this part's production art, keeping the ≥ 20 % gold and the readability check.
