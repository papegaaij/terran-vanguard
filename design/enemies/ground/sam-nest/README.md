---
title: SAM Nest
design: draft
implementation: not-started
art: proposed
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../art-direction]
updated: 2026-10-27
---

# SAM Nest

## Summary

A small Ascendancy missile launcher on station hulls and asteroid surfaces: it swings its rack toward the
ship and fires salvos of two slow, shootable **homing** missiles, drawn as the art direction's red-gold
**homing diamond**. Level 29's station turrets that wake when Ceres Hub turns sides are mostly SAM Nests.

## Design

### Stat block

**Draft** (Act 5 design phase, step D-enemies-B; pending the user's review in round 60). A hand-written
table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2),
set at its first level from the proposed Act 5 reference DPS (judgement call J2: L29 120). As a `small`
unit it takes the **beam-era ×2** on its HP: 10 is flown as 20, 0.17 s at the reference. Fire rates are
tuned per level with `levelReport`.

| Field | Value |
|---|---|
| Faction | Ascendancy; a **machine** (stunned 3 s by the EMP Burst) |
| Layer | `ground` |
| Size tier | `small` |
| Size | 44×44 px, hitbox 32×32 |
| Parts | single |
| Orientation | `fixed` base; the launcher rack `16 angles`, turning 120°/s toward the ship |
| HP | 10 (easy 8 / hard 13, from the global multipliers); ×2 in the beam era: 20 (easy 15 / hard 26); missile 2 |
| Armour / shield | none (not hardened) |
| Speed | 0 px/s (scrolls with the hull or rock) |
| Movement | `terrain` |
| Attack | `homing`: a salvo of **2** missiles, 0.25 s apart, every **4.5 s**, launched along the rack's bearing; tell: the rack holds still and its hatch opens with a blinking red lock light for **0.4 s**. Missile (the Hornet's numbers, one homing diamond for the act): 110 px/s, turn rate 80°/s, life 4 s, 2 HP, `medium` 6 on a hit; shootable by every weapon, homing shots and Rook; the Decoy Flares retarget it. At most 8 alive per nest (a salvo past the cap is skipped) |
| Formations | turret nest (2–4 on a hull block or rock face) |
| Weak points | none (the open hatch's red light is drawn only) |
| Effective traits | `anti-ground`, `area`, `homing`, `spread` (the missiles) |
| Credits | 15 (score 150 × chain); missiles 0 |
| Death | `small` ground explosion, the rack thrown; a scorched launcher wreck stays |
| First level / used in | L29 (Ceres Hub); later Act 5 levels, the Strider's shoulder pod and the Iron Sovereign's SAM batteries share its missile (their docs) |
| Difficulty hooks | easy: every 6 s, turn rate 65°/s; hard: salvos of 3, turn rate 95°/s |

### Behaviour

- **The homing diamond** (art direction item, round 59 C.7, closed by Act 5): the Ascendancy's shootable
  homing missiles (SAM Nest, [Hornet](../../air/hornet/README.md), Strider pod, Sovereign batteries) are drawn as a **diamond** in red-gold
  with a short thin exhaust (the [art direction](../../../art-direction/README.md)'s item, the air units'
  step), so the player learns one shape for "shoot this or flare it".
- **Any direction.** The rack turns all round, so a nest the ship has passed keeps firing up the screen;
  it holds fire while the ship is within 96 px (no missile spawned on the ship).
- **Dormant until t** (Level 29's signature *station turns sides*): a nest on the turning station block is
  **dormant**: dark, white lights, not shootable and not picked by homing shots or Rook (a friendly station
  until then). At the block's moment its lights switch to red and it wakes with a 1 s tell (the rack lifts
  and lights red), then fires as above. The level doc sets the moment (~t=60) and which units wake.
- **EMP.** Stunned 3 s: the rack stops, no salvo; missiles already flying keep flying.
- **Vacuum.** The exhaust is a short thin flame with no smoke trail (no air); the round-60 look sets it.

### Difficulty and level use

- L29 introduces them on Ceres Hub's dock canyon hull and on the turning block; the level keeps at most 3
  nests firing at once (with a 4 s life, a nest has at most one salvo in the air: 2, hard 3).
- Their missiles are the Act 5 reason for `spread` and `homing` (L29's traits), and for the Decoy Flares.

### Sounds

Brief for the round-60 sound proposals (Freesound CC0/CC-BY, real recordings, no bubbly or popping
sounds): the hatch a short hydraulic clunk; the lock a two-tone electronic beep (a synthesised tone is
fine); the launch a sharp rocket whoosh (a firework or model-rocket launch, cut short); a missile shot
down a small dry crack.

## Concept art

None yet: proposals (a/b) in round 60 (step ART-P-units, `tools/concept/enemies_r60.py`): a gloss-black
octagonal pad with a gold four-cell rack and a red lock light (Q11 a); the homing diamond itself is the art
direction's sheet in `design/enemies/concept/` (the same step).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (pending the user's review), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 29 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/sam-nest-r60-a.png](concept/sam-nest-r60-a.png) | SAM Nest A, twin tubes on a round base — a round armoured base with a gold ring and four lights, two missile tubes on a yoke under a gold hatch, a red lock light; strong rim (sheet) | proposed |
| [concept/sam-nest-r60-a.gif](concept/sam-nest-r60-a.gif) | SAM Nest A, twin tubes on a round base — a round armoured base with a gold ring and four lights, two missile tubes on a yoke under a gold hatch, a red lock light; strong rim (motion) | proposed |
| [concept/sam-nest-r60-b.png](concept/sam-nest-r60-b.png) | SAM Nest B, four-cell box on a hex base — a hexagonal base with gold edges, a square box launcher with four cells in a gold frame (sheet) | proposed |
| [concept/sam-nest-r60-b.gif](concept/sam-nest-r60-b.gif) | SAM Nest B, four-cell box on a hex base — a hexagonal base with gold edges, a square box launcher with four cells in a gold frame (motion) | proposed |

## Implementation

- [ ] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers and the beam-era factor applied; tuned with `levelReport`
- [ ] Rack turning on a fixed base; the hatch tell; salvos of shootable homing missiles with the shared cap and the flares' retargeting
- [ ] The dormant state: dark, unhittable and unpicked until its wake time, then a 1 s wake tell (also for the other station turrets of L29)
- [ ] The homing diamond missile sprite (with the art direction item)
- [ ] Production sprites (the base, the rack at 16 angles, hatch frames, the wreck), the intel portrait
- [ ] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds)

## Open questions

- Q11 (pending round 60): the Ascendancy look on the belt, **a** recommended (gloss black with gold trim,
  strong rim light, red running lights); alternatives **b** gunmetal with gold, **c** black silhouettes with
  gold accents only.

## Decisions

- 2026-10-27 (step D-enemies-B): drafted for the Act 5 design phase from the roster row and the gap
  analysis; every number is a first draft, pending the user's review in round 60.
- 2026-10-27 (decided by me, trivia): a dormant nest cannot be hit or picked (the station is friendly until
  it turns); at most 8 missiles alive per nest, as the Hornet's and the Revenant Walker's pods; missiles pay nothing.
