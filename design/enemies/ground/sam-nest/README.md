---
title: SAM Nest
design: approved
implementation: done
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../art-direction]
updated: 2026-10-28
---

# SAM Nest

## Summary

A small Ascendancy missile launcher on station hulls and asteroid surfaces: it swings its rack toward the
ship and fires salvos of two slow, shootable **homing** missiles, drawn as the art direction's red-gold
**homing diamond**. Level 29's station turrets that wake when Ceres Hub turns sides are mostly SAM Nests.

## Design

### Stat block

**Draft** (Act 5 design phase, step D-enemies-B; approved in round 60). Rendered from [data.yaml](data.yaml) since M9 part B (step B2, 2026-10-28; its header lists the
build's own readings). Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2),
set at its first level from the proposed Act 5 reference DPS (judgement call J2: L29 120). As a `small`
unit it takes the **beam-era ×2** on its HP: 10 is flown as 20, 0.17 s at the reference. Fire rates are
tuned per level with `levelReport`.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy (machine: the EMP Burst stuns it longest) |
| Layer | `ground` |
| Size tier | `small` |
| Size | 44×44 px, hitbox 32×32 |
| Parts | single (the launcher rack turns) |
| Orientation | `16 angles` (the launcher rack; the base is fixed) |
| HP | 10 (easy 8 / hard 13, from the global multipliers) |
| Armour / shield | none (not hardened). HP 10 base flies at 20 with the beam era's ×2; missile 2 |
| Speed | 0 px/s (scrolls with the hull or rock) |
| Movement | `terrain`; **dormant until t** where its level says (dark, white-lit, not shootable, not picked; then a 1 s wake tell) |
| Attack | `homing`: a salvo of **2** missiles, 0.25 s apart, every **4.5 s**, launched along the rack's bearing (the rack turns 120°/s all round); tell: the rack holds still and its hatch opens with a blinking red lock light for **0.4 s**. Missile (the homing diamond): 110 px/s, turn rate 80°/s, life 4 s, 2 HP, `medium` 6 on a hit; shootable by every weapon, homing shots and Rook; the Decoy Flares retarget it. At most 8 alive per nest; it holds fire while the ship is within 96 px |
| Formations | turret nest (2–4) (on a hull block or rock face; at most 3 nests firing at once, a level rule) |
| Weak points | none (the open hatch's red light is drawn only) |
| Effective traits | `anti-ground`, `area`, `homing`, `spread` |
| Credits | 15 (score 150 × chain); missiles 0 |
| Death | `small` ground explosion, the rack thrown; a scorched launcher wreck stays |
| First level / used in | L29; Ceres Hub; later Act 5 levels, the Strider's shoulder pod and the Iron Sovereign's SAM batteries share its missile (their docs) |
| Difficulty hooks | easy: every 6 s, turn rate 65°/s; hard: salvos of 3, turn rate 95°/s |
<!-- /data -->

### Behaviour

- **The homing diamond** (art direction item, round 59 C.7, closed by Act 5): the Ascendancy's shootable
  homing missiles (SAM Nest, [Hornet](../../air/hornet/README.md), Strider pod, Sovereign batteries) are drawn as a **diamond** in red-gold
  with a short thin exhaust (the [art direction](../../../art-direction/README.md)'s item, the air units'
  step), so the player learns one shape for "shoot this or flare it". Player homing shots, Rook and the Tesla arc lock onto a diamond only when no enemy unit is in reach (QA-1, decided 2026-10-27, confirmed in round 61 on 2026-10-28).
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

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

Round 60 proposals (a/b), made in the gloss-black look of Q11 a and chosen below (production re-renders the chosen one in gunmetal/charcoal, Q11 b) (step ART-P-units, `tools/concept/enemies_r60.py`): a gloss-black
octagonal pad with a gold four-cell rack and a red lock light (Q11 a); the homing diamond itself is the art
direction's sheet in `design/enemies/concept/` (the same step).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (gloss black; the user chose look b, gunmetal/charcoal with gold, in round 60, so the chosen variant is re-rendered in gunmetal in production), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 29 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/sam-nest-r60-a.png](concept/sam-nest-r60-a.png) | SAM Nest A, twin tubes on a round base — a round armoured base with a gold ring and four lights, two missile tubes on a yoke under a gold hatch, a red lock light; strong rim (sheet) | chosen (round 60) |
| [concept/sam-nest-r60-a.gif](concept/sam-nest-r60-a.gif) | SAM Nest A, twin tubes on a round base — a round armoured base with a gold ring and four lights, two missile tubes on a yoke under a gold hatch, a red lock light; strong rim (motion) | chosen (round 60) |

## Implementation

- [x] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers and the beam-era factor applied; tuned with `levelReport`
- [x] Rack turning on a fixed base; the hatch tell; salvos of shootable homing missiles with the shared cap and the flares' retargeting
- [x] The dormant state: dark, unhittable and unpicked until its wake time, then a 1 s wake tell (also for the other station turrets of L29)
- [x] The homing diamond missile sprite (with the art direction item) (part A's `DiamondLooks`, round 61 F.6)
- [x] Production sprites (the base, the rack at 16 angles, hatch frames, the wreck), the intel portrait (`tools/art/belt_units.py`, steps ART-B and B4: `Level29LooksTest.theArtIsWhatTheLooksDraw`, the intel portrait in `PortraitsTest`; the art accepted in round 62)
- [x] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds) (step B4: the hatch **b**, the lock beep **b** and the wake `ab` (a's servo, b's lock beep) as round 62's picks, `Level29SoundsTest.theTalonsTheNestsAndTheTurningStationSound`)

## Open questions

- Q11 (decided in round 60, **b**, not the recommendation): the Ascendancy look on the belt is gunmetal/charcoal hulls
  with gold (≥ 20 % gold, rim light, red running lights); a (gloss black) and c (black silhouettes) rejected
  ([enemies](../../README.md#factions-and-visual-language)).

## Decisions

- 2026-10-27 (step D-enemies-B): drafted for the Act 5 design phase from the roster row and the gap
  analysis; every number is a first draft, approved in round 60.
- 2026-10-27 (decided by me, trivia): a dormant nest cannot be hit or picked (the station is friendly until
  it turns); at most 8 missiles alive per nest, as the Hornet's and the Revenant Walker's pods; missiles pay nothing.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`); art A.11 **a**, "SAM Nest A, twin tubes on a round base", chosen (`art: chosen`), the other variant in `concept/rejected/`. Q11 **b** (gunmetal/charcoal with gold, not gloss black): the chosen variant is re-rendered in gunmetal/charcoal in this part's production art, keeping the ≥ 20 % gold and the readability check.
- 2026-10-28 (user, [round 62](../../../concept-rounds/round-62/README.md), closed; page answers): the art (F.2) accepted as final; the hatch **b** (S.3), the lock beep **b** (S.4) and the wake (S.5 a with S.4 b: `nest-wake-r62-ab`) picked.
