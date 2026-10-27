---
title: Talon
design: draft
implementation: not-started
art: proposed
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../story/factions/jovian-ascendancy, ../../../campaign/act-5-the-belt]
updated: 2026-10-27
---

# Talon

## Summary

The Ascendancy's black-and-gold interceptor and the first enemy of the open war (Level 29): fast
`swoop`s in V-wings and lines abreast, paired `aimed` shots, and a light gold hex **shield** that
pops before the hull takes damage. A V-wing flies behind a gold-striped **squad leader**: kill it
and the wing scatters.

## Design

### Stat block

**Draft** (Act 5 design phase, pending the user's review in round 60). A hand-written table: there is
no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does
2), set at its first level from the proposed Act 5 reference DPS (L29 = 120). As a `small` unit it
takes the [beam-era](../../README.md#balancing-basis) ×2 on its HP and keeps it in every later level
(no act HP factor). Fire rates are first values: the build tunes them per level with `levelReport`.

| Field | Value |
|---|---|
| Faction | Jovian Ascendancy; a **machine** (stunned 3 s by the [EMP Burst](../../../player/specials/README.md#emp-burst-l15)) |
| Layer | `air` (`space` in the belt's vacuum levels: the same layer) |
| Size tier | `small` |
| Size | 40×36 px, hitbox 28×24 (leader the same airframe, a gold stripe down the spine) |
| Parts | single |
| Orientation | `16 angles` (it banks through its swoop) |
| HP | 16 base, **32** with the beam-era ×2 (easy 24 / hard 42, from the global multipliers): 0.27 s at the L29 reference of 120. Leader 24 base, 48 |
| Armour / shield | hex [shield](../../README.md#enemy-shields) **2** (leader 4): spent first, refills 2 s after the last hit; `shield-breaker` and the EMP empty it |
| Speed | 220 px/s through the swoop; 260 px/s scattering |
| Movement | `swoop`: enters at the top on a curve, crosses the upper half (lowest point 200–260 px below the top edge), exits through a side or the top on a curve; a wing keeps its shape round the leader's path |
| Attack | `aimed` **pair** (two shots side by side, 12 px apart, both aimed at the ship) every 1.6 s while in the upper 300 px; standard 160 px/s, `small` = 4; a 0.25 s red glint on the twin muzzles is the tell. Contact `small` = 10 (destroyed by the impact) |
| Formations | `V-wing` (5–7, a leader at the tip), `line abreast` (4–6, no leader), `pincer` (two wings, edge-warned) |
| Weak points | the twin engine glow (drawn only) |
| Effective traits | `spread`, `homing`, `forward`; `shield-breaker` pops the shield (not needed: 2 HP) |
| Credits | 12 (score 120 × chain); leader 25 |
| Death | `small` explosion with gold sparks and a spinning wing; a scattering Talon killed pays the same |
| First level / used in | L29 (Ceres Hub's mutineers); the act's main fighter in L29–L35; its airframe returns in Act 6 (the Chimera, the Honour Guard) |
| Difficulty hooks | easy: pair every 2.0 s, the leader's wing scatters without firing; hard: pair every 1.3 s, each scattering Talon fires one parting pair as it breaks off |

### Behaviour

- **The shield.** A thin gold hex outline round the hull (40 % opacity) while it holds; a hit flashes
  the hexes where it lands; the shot's damage beyond 2 goes on to the hull, so the shield costs a
  Pulse Cannon bolt at most. It shows the doctrine and teaches the hex flash before the heavier
  shields of the Hornet and the [Gilded Gunship](../gilded-gunship/README.md); the shared rule is in
  [enemy shields](../../README.md#enemy-shields).
- **Squad leader** (the Ascendancy's *Command* doctrine, decided in the gap analysis, shown in round
  60). A V-wing's tip is a leader: a gold stripe down the spine and a brighter red running light.
  Killing it **scatters** the wing: each Talon breaks off sideways, away from the wing's centre, at
  260 px/s, stops firing and leaves through the nearer side edge within 1.5 s. The wing is then lost
  as credits but no longer shoots: the player trades bounty for safety, or takes the wing first and
  the leader last. A wing whose leader leaves the screen alive simply finishes its swoop.
- **Mutineers** (L29): Ceres Hub's garrison flies Talons once the station declares for the
  Ascendancy; the radio names them. Whether some Ceres pilots in repainted CDF craft waver instead is
  the [Q10](#open-questions) choice, owned by the Level 29 draft, not by this unit.
- **Leeched** (L33): a Talon with a Void Leech on it loses its shield and drifts out of its swoop
  (presentation of the uneasy alliance; the leech's document owns it).
- **Sounds** (brief for the audio step): a short jet whine on the swoop's turn-in, a dry twin crack
  for the pair (a real cannon from Freesound, CC0/CC-BY), a glassy hex tick when the shield is hit and
  a brittle shatter when it breaks; the leader's death adds a short radio squelch.

### Difficulty and level use

- Never more than two wings on the screen; a line abreast never more than 6.
- The wing's first entry from a side (a `pincer`) is edge-warned 3 s ahead.

## Concept art

The chosen faction-contrast look of round 04, [talon-r04-a.png](../concept/talon-r04-a.png) (black and
gold with a red accent and the gold/red rim light), is listed in the [air](../README.md#concept-art)
Concept art table. Round 60's unit proposals (the gap analysis's wave 5) add the leader stripe, the
hex shield and the belt readability of [Q11](../../README.md#open-questions).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (pending the user's review), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 29 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/talon-r60-a.png](concept/talon-r60-a.png) | Talon A, gold trim — the r04 gold chines and spine plus gold wing-tip caps, a strong 2 px gold rim; the hex shield hugs the hull (sheet) | proposed |
| [concept/talon-r60-a.gif](concept/talon-r60-a.gif) | Talon A, gold trim — the r04 gold chines and spine plus gold wing-tip caps, a strong 2 px gold rim; the hex shield hugs the hull (motion) | proposed |
| [concept/talon-r60-b.png](concept/talon-r60-b.png) | Talon B, gold panels — the outer wing panels and tail fins in gold, a 1 px rim; the hex shield as a round bubble (sheet) | proposed |
| [concept/talon-r60-b.gif](concept/talon-r60-b.gif) | Talon B, gold panels — the outer wing panels and tail fins in gold, a 1 px rim; the hex shield as a round bubble (motion) | proposed |

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers and the beam-era factor applied; HP and fire rate re-measured with `levelReport` at L29
- [ ] The hex shield per [enemy shields](../../README.md#enemy-shields) (spent first, overflow to the hull, refill, `shield-breaker`, EMP)
- [ ] The squad leader and the scatter (leader killed → the wing breaks off sideways and leaves), deterministic and allocation-free
- [ ] Production sprites (16 angles, the leader's stripe, the shield's hex flash and break) via `tools/art/`
- [ ] Death effect, bounty and sounds (Freesound, CC0/CC-BY) per this spec

## Open questions

- **Q1** (round 60, the shield's model): the draft follows **a** (a light shield every weapon hits);
  alternatives: **b** shields only `shield-breaker`, beams and the EMP get through (forces the Tesla
  Coil Pod in Act 5), **c** no shield mechanic in Act 5 (sparks only).
- **Q10** (round 60, the mutineers): **b** recommended (some Ceres pilots waver, a "Hold fire"
  secondary; the Level 29 draft); **a** all mutineers fly Talons and fight; **c** a remembered story
  choice.

## Decisions

- 2026-10-27 (step m9-d-enemies-a): drafted for the Act 5 design phase from the roster row and the
  gap analysis; every number is a first draft pending the user's review in round 60. HP 16 base (32
  with the beam era) for 0.27 s at the proposed L29 reference of 120; the shield 2 per Q1 a.
- 2026-10-27 (gap analysis, trivia decided, shown in round 60): the squad leader's scatter (the
  *Command* doctrine) rewards target choice and costs one flag per wave.
