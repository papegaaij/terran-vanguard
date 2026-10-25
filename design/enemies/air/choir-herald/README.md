---
title: Choir Herald
design: approved
implementation: done
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../campaign/act-3-red-dust]
updated: 2026-10-19
---

# Choir Herald

## Summary

A Vrell support flyer that hovers behind its escorts and sings a shield ring onto every enemy
near it. The rings absorb damage and refill while it lives; kill the Herald (its glowing crest
marks it) and they collapse. The act's priority target (Level 17).

## Design

### Stat block

Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)). The numbers live
in [data.yaml](data.yaml) (M7 part D, step D2), with the new `aura` attack and its `aura` hooks of the
[schemas](../../../tech/architecture/schemas-units/README.md); the table is rendered from it. HP is in damage units (a Pulse Cannon L1 shot
does 2). Set at its first level from the proposed Act 3 reference DPS (M18 = a, approved in round 36:
L17 = 84): 60 HP is 0.7 s of focused fire. Where it returns it gets the act HP factor of a
`medium` unit.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` |
| Size tier | `medium` |
| Size | 72×72 px, hitbox 52×48 |
| Parts | single |
| Orientation | `fixed` (faces down; it hovers and drifts) |
| HP | 60 (easy 45 / hard 78, from the global multipliers) |
| Armour / shield | none of its own (its aura never covers itself or another Herald) |
| Speed | 100 px/s entry; hover drift 20 px/s |
| Movement | `hover`: enters behind its escorts, holds 150–200 px below the top edge for 12 s with a slow side-to-side drift, then exits up; its escorts `orbit` it at 0.8 × the aura's radius (72 px) |
| Attack | `aura`: every other enemy within **90 px** of its centre carries a shield ring absorbing up to **1× that unit's HP at the level** (M8 = a), at most 40; the ring refills at 25 % of its maximum per second after 2 s without a hit, while the Herald lives and the unit stays in range; its own song: a `ring` of 10 every 4 s (slow 100 px/s, `small` 4) |
| Formations | carrier + escorts, circle (the escorts in `orbit` round it); never more than one on the screen |
| Weak points | the glowing halo crest (drawn only) |
| Effective traits | `forward`, `piercing`, `homing` |
| Credits | 30 (score 300 × chain) |
| Death | `medium` organic burst with a falling chord; every ring it fed collapses over 0.3 s |
| First level / used in | L17; the act's priority target (Level 17) |
| Difficulty hooks | easy: radius 75 px, refill 15 %/s; hard: radius 105 px, ring of 12 every 3.2 s |
<!-- /data -->

### Behaviour

- **The aura.** Units inside its radius show a thin translucent ring round their sprite (the
  Choir's pale hue, outside the reserved bullet and player hues). Shots on a ringed unit spend the
  ring first: the ring flashes and the shot's damage is absorbed; overflow beyond the ring goes to
  the unit's HP. A `tiny` unit's ring absorbs 1 (one extra hit), a `medium` up to its HP, capped
  at 40 so a `large` part never doubles in toughness. Bosses and mid-bosses never take an aura.
- **Range and refill.** A unit entering the radius gets a full ring over 0.5 s; one leaving it
  keeps the ring 1.0 s, then it fades. While the Herald lives, a ring untouched for 2.0 s refills
  at 25 % of its maximum per second.
- **EMP.** The EMP Burst strips every ring on the screen and stops the refill for its stun time;
  the Herald itself (Vrell) is stunned 1.5 s, its song silent.
- **Readability.** A faint pulsing circle of the aura's radius is drawn on the Herald, and a thin
  thread of light links it to each unit it shields, so the player sees what to kill first.
- **Code fit.** A new `aura` attack: radius, a shield value per unit, the refill and the EMP strip
  (§2.7 of the gap analysis).

### Difficulty and level use

- Radius, refill and song per the stat block; the global multipliers apply to its HP.
- Never more than one Herald on the screen in Act 3; its escorts are mediums or popcorn, not other
  support units.

## Concept art

Proposals of concept round 36 (step A-units, listed in the [air](../README.md#concept-art)
Concept art table; prompts in [../concept/prompts.md](../concept/prompts.md), generator
`tools/concept/enemies_r36.py`): an a/b of the Herald with its crest and the aura ring on its escorts. Round 36 (2026-10-13): **b** "seraph" chosen (U.3), a "cantor bell" rejected.

| File | What | Status |
|---|---|---|
| [../concept/rejected/choir-herald-r36-a.png](../concept/rejected/choir-herald-r36-a.png) | Choir Herald A, cantor bell — teal-black bell with a sound-wave aura: source, wing beat, headings, escort ring states (sheet) | rejected — round 36: b (seraph) picked |
| [../concept/rejected/choir-herald-r36-a.gif](../concept/rejected/choir-herald-r36-a.gif) | Choir Herald A — with six Needler escorts in a circle over Mars; one escort shot through its ring, then the Herald dies and the rings fade (motion) | rejected — round 36: b (seraph) picked |
| [../concept/choir-herald-r36-b.png](../concept/choir-herald-r36-b.png) | Choir Herald B, seraph — bone seraph with a floating halo crest and segmented violet rings (sheet) | chosen (round 36, U.3) |
| [../concept/choir-herald-r36-b.gif](../concept/choir-herald-r36-b.gif) | Choir Herald B — the same scene as A (motion) | chosen (round 36, U.3) |

## Implementation

- [x] `aura`: 90 px radius, a ring of 1× unit HP (≤ 40) on every other enemy in range, refill 25 %/s after 2.0 s, collapse at the Herald's death, none on bosses or Heralds (`AuraTest`, M7 part D step D3)
- [x] Ring absorbs first, overflow to HP; ring flash; radius circle and links drawn (`AuraTest` over every damage path, `AuraLooksTest`; seen in the round 40 capture)
- [x] EMP strips the rings and pauses the refill (`AuraTest`)
- [x] `hover` with escorts (`carrier + escorts`); the song ring every 4.0 s (`AuraTest`, `Level17Test`)
- [x] Death, bounty and score
- [x] Data file with the stat block (when Level 17 is built; M7 part D, step D2)
- [x] Production art (round of part D): step ART-D, proposed in round 40 (`choir-herald-final-r40-a` in [air](../README.md))

## Open questions

- None open: round 36 answered it (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the [air roster](../README.md) to a draft
  spec. M8 = a (a ring absorbing ≈ 1× each unit's HP within ~90 px, refilled slowly while the
  Herald lives, stripped by the EMP; not full immunity) and M18 = a taken as the recommended
  options, accepted in round 36 (2026-10-13). Our proposals: 60 HP, the 40 cap, the 2.0 s /
  25 %/s refill, no aura on bosses or other Heralds, bounty 30, one Herald on screen at most.
- 2026-10-12 (merge): the round-36 a/b proposals are listed under Concept art (`art: proposed`).
- 2026-10-13 (user, round 36): approved (D.15). M8 = a accepted; Q.22g yes: a unit's ring thins as its absorb value drains. U.3 = b: the seraph (bone, floating halo crest, segmented violet rings) chosen, the cantor bell (a) rejected and moved to `../concept/rejected/`.
- 2026-10-18 (M7 part D, step D2; accepted in round 40 (2026-10-19): the stat block moved into
  [data.yaml](data.yaml) with the new `aura` attack (`radius` 90, `share` 1, `cap` 40, `refill` 0.25 after
  `refill_delay` 2.0 s, `fill` 0.5 s, `keep` 1.0 s, `collapse` 0.3 s) and its easy/hard `aura` hooks; the song a
  `ring` named `song` (hard 12 every 3.2 s through the attack hook). Defaults of the gap check: its escorts orbit
  it at 0.8 × the radius (`escort_radius`, 72 px) so they stay ringed; the aura rings units on every layer; the
  hover's drift a `sine` of 32 px over 10 s (20 px/s at its fastest); it exits up (`exit: back`).
- 2026-10-19 (user, round 40, F.b): the final art approved (`art: final`); the aura and Shellback build choices (round 40 B.c) accepted.
