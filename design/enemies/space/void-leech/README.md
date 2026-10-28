---
title: Void Leech
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/generator, ../../air/lamprey]
updated: 2026-10-27
---

# Void Leech

## Summary

A small black-violet Vrell leech that drifts in from behind and the sides, chases, **latches** onto the
hull and drains the **generator**: each latched leech lowers the output by 2 MW, so a loadout without spare
power loses its shield regeneration first, then its fire rate. The Lamprey's latch and shake-off with a new
target. In Level 33 it feeds on Ascendancy craft too.

## Design

### Stat block

**Draft** (Act 5 design phase, step D-enemies-B; approved in round 60). A hand-written
table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2),
set at its first level from the proposed Act 5 reference DPS (judgement call J2: L33 136). As a `small` unit
it takes the **beam-era ×2** on its HP: 6 is flown as 12, and a latched leech takes ×2 damage (the
Lamprey's rule).

| Field | Value |
|---|---|
| Faction | Vrell (stunned 1.5 s by the EMP Burst) |
| Layer | `space` (the `air` layer in vacuum) |
| Size tier | `small` |
| Size | 32×32 px, hitbox 20×24 |
| Parts | single |
| Orientation | `16 angles` (16 headings × 4 swim frames; a latched pose per hull spot, a stunned pose) |
| HP | 6 (easy 4 / hard 8, from the global multipliers); ×2 in the beam era: 12 (easy 9 / hard 16) |
| Armour / shield | none |
| Speed | 50 px/s drifting, 190 px/s chasing (turning 160°/s) |
| Movement | `drift`s in from the rear or a side edge (edge-warned, as every side and rear entry) toward the ship's lane; within **220 px** of the ship it `chase`s for up to 4 s, then flies straight on and leaves; `latch` on contact at the left, right or tail spot (the [Lamprey](../../air/lamprey/README.md)'s spots); **3 latch slots** (easy 2) |
| Attack | `drain`: while latched, the generator's **effective output −2 MW** (stacking to −6 MW; the [generator's drain rules](../../../player/generator/README.md#enemy-drain-effects)); no damage to shield or armour; with every slot taken, a touch is a `small` contact (10) and the leech dies |
| Formations | stream (4–6, 0.5 s apart), rear ambush (3–5 from the bottom edge) |
| Weak points | the power-draw sac on its back (drawn only; it pulses brighter while it drains) |
| Effective traits | `homing`, `side`, `rear`, `area` |
| Credits | 12 (score 120 × chain) |
| Death | `small` dry chitin crack with a violet spark; on death or shake-off the output returns at once |
| First level / used in | L33 (Leech Field); later levels may use it (their docs decide) |
| Difficulty hooks | easy: 2 latch slots, chase 160 px/s; hard: chase 220 px/s, turning 200°/s |

### Behaviour

- **Latch, shake-off, stun and flee** follow the [Lamprey](../../air/lamprey/README.md#design) exactly:
  four reversals of the commanded horizontal direction within 1.2 s throw off **every** latched leech;
  a thrown-off leech is stunned 1 s, then flees and never latches again. **What hits a latched leech**
  is the Lamprey's rule too (the rear mount, `side`/`rear` weapons and blasts, at ×2; front shots pass it).
- **Drain** (Q2 a): the [generator](../../../player/generator/README.md#enemy-drain-effects) owns the
  effect (shield regeneration stops first, then fire rate falls by output ÷ load; armour, specials and the
  escort are never touched). The leech only lowers the output while it is latched; it never drains the
  shield's points.
- **Feedback.** Each latched leech's sac pulses violet once a second; the HUD's power gauge shows the
  drained MW (the HUD's part), the muzzles flicker and the shield ring dims when the load passes the
  output (the player step's part). Varga's hangar intel warns before L33: "bring spare power".
- **EMP.** The EMP Burst throws off every latched leech (as a shake-off) and stuns the free ones 1.5 s.
- **The uneasy alliance** (Level 33, presentation and a small rule): a wave may mark Ascendancy units
  ([Talons](../../air/talon/README.md)) as **prey**. A leech that touches a prey unit latches on it instead of the ship: the unit's
  [shield](../../README.md#enemy-shields) empties and does not refill, it stops firing and drifts down with the scroll at 60 px/s until it
  leaves. Both stay shootable and pay their credits; a leech on a prey unit never moves on to the ship.
  The level doc places these waves.

### Difficulty and level use

- L33 brings them from behind and the sides among the rock fields (with Rail Bunkers on the asteroid
  surfaces, judgement call J3); at most 6 leeches free on the screen at once.
- The level's power-cell drops (if any) are the level doc's; the leech itself drops nothing.

### Sounds

Brief for the round-60 sound proposals (Freesound CC0/CC-BY, real recordings, no bubbly or popping
sounds): the latch a dry clamp (a heavy staple gun or a ratchet click on metal); the drain a falling
electrical whine (a motor spinning down) under a low hum while latched; the shake-off a scrape of
claws on metal; the death a dry chitin crack.

## Concept art

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

Round 60 proposals (a/b), made in the gloss-black look of Q11 a and chosen below (production re-renders the chosen one in gunmetal/charcoal, Q11 b) (step ART-P-units, `tools/concept/enemies_r60.py`): a black-violet
leech with a pulsing violet power-draw sac (violet: the aimed-shot glow is taken; the leech differs by
shape), a latched pose on the Stormhawk's hull and one on a Talon.

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (gloss black; the user chose look b, gunmetal/charcoal with gold, in round 60, so the chosen variant is re-rendered in gunmetal in production), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 33 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/void-leech-r60-a.png](concept/void-leech-r60-a.png) | Void Leech A, ringed leech — five tapering black-violet chitin rings, an oral sucker disc ringed with bone hooks, a pulsing violet sac (sheet) | chosen (round 60) |
| [concept/void-leech-r60-a.gif](concept/void-leech-r60-a.gif) | Void Leech A, ringed leech — five tapering black-violet chitin rings, an oral sucker disc ringed with bone hooks, a pulsing violet sac (motion) | chosen (round 60) |

## Implementation

- [ ] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers and the beam-era factor applied
- [ ] Drift in, chase, latch on 3 slots (the Lamprey's `Latches`), shake-off, stun and flee
- [ ] The generator drain while latched (the generator's in-level output, built by the player step), restored at once on death or shake-off
- [ ] The EMP throw-off; prey latching on marked Ascendancy units (shield emptied, no fire, drifting out)
- [ ] Production sprites (swim frames, latched poses on the ship and on a Talon, the stunned pose), the intel portrait
- [ ] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds)

## Open questions

- Q2 (decided in round 60): **a** recommended (−2 MW a leech, to −6, the shortfall order, the gauge and the
  feedback); alternatives **b** (a flat −20 % fire rate a leech, whatever the loadout) and **c** (each
  leech blacks out the fitted weapon with the highest draw until shaken off).
- The latch slots: 3 at medium lets the drain reach the −6 MW cap; with 2 the cap is reached only at hard.
  Decided as 3 (easy 2), pending the round.

## Decisions

- 2026-10-27 (step D-enemies-B): drafted for the Act 5 design phase from the roster row and the gap
  analysis; every number is a first draft, approved in round 60.
- 2026-10-27 (the gap analysis's decision): the Lamprey's latch and shake-off with a new target (the
  generator) and look (black-violet, a pulsing power-draw glow); it also latches on Ascendancy units in
  L33 (a Talon with a leech loses its shield and drifts).
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`); art A.10 **a**, "Void Leech A, ringed leech", chosen (`art: chosen`), the other variant in `concept/rejected/`. Q11 **b** (gunmetal/charcoal with gold, not gloss black): the chosen variant is re-rendered in gunmetal/charcoal in this part's production art, keeping the ≥ 20 % gold and the readability check.
