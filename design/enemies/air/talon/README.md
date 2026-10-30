---
title: Talon
design: approved
implementation: done
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../story/factions/jovian-ascendancy, ../../../campaign/act-5-the-belt]
updated: 2026-10-29
---

# Talon

## Summary

The Ascendancy's black-and-gold interceptor and the first enemy of the open war (Level 29): fast
`swoop`s in V-wings and lines abreast, paired `aimed` shots, and a light gold hex **shield** that
pops before the hull takes damage. A V-wing flies behind a gold-striped **squad leader**: kill it
and the wing scatters.

## Design

### Stat block

**Draft** (Act 5 design phase, approved in round 60). Rendered from [data.yaml](data.yaml) since M9 part B (step B2, 2026-10-28; its header lists the
build's own readings). Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does
2), set at its first level from the proposed Act 5 reference DPS (L29 = 120). As a `small` unit it
takes the [beam-era](../../README.md#balancing-basis) ×2 on its HP and keeps it in every later level
(no act HP factor). Fire rates are first values: the build tunes them per level with `levelReport`.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy (machine: the EMP Burst stuns it longest) |
| Layer | `air` (`space` in the belt's vacuum levels: the same layer) |
| Size tier | `small` |
| Size | 40×36 px, hitbox 28×24 (leader the same airframe, a gold stripe down the spine) |
| Parts | single |
| Orientation | `16 angles` (it banks through its swoop) |
| HP | 16 (easy 12 / hard 21, from the global multipliers) |
| Armour / shield | hex [shield](../../README.md#enemy-shields) **12** (leader 24): spent first, refills 2 s after the last hit; `shield-breaker` and the EMP empty it. HP 16 base flies at 32 with the beam era's ×2 (leader 24, 48) |
| Speed | 220 px/s through the swoop; 260 px/s scattering |
| Movement | `swoop`: enters at the top 280 px across its lane on a curve, bottoms out 200–260 px below the top edge and leaves up through the top; a `pincer`'s wings enter at their side edges and leave up through the top; a V-wing flies behind its leader. Its leader killed: the wing **scatters** sideways at 260 px/s, holding fire, out by the nearer side edge within 1.5 s; as a carrier's escort it orbits it at 90 px |
| Attack | `aimed` **pair** (two shots 4° apart, both at the ship) every 1.6 s while within 300 px of the top edge; standard 160 px/s, `small` = 4; a 0.25 s red glint on the twin muzzles is the tell. Contact `small` = 10 (destroyed by the impact) |
| Formations | V-wing (5–7), line abreast (4–6), pincer, carrier + escorts (a V-wing's tip is its leader; never more than two wings on the screen; as escorts, the Gilded Gunship's in Level 30) |
| Weak points | the twin engine glow (drawn only) |
| Effective traits | `spread`, `homing`, `forward` |
| Credits | 12 (score 120 × chain); leader 25 |
| Death | `small` explosion with gold sparks and a spinning wing; a scattering Talon killed pays the same |
| First level / used in | L29; Ceres Hub's mutineers; the act's main fighter in L29–L35; its airframe returns in Act 6 (the Chimera, the Honour Guard) |
| Difficulty hooks | easy: pair every 2.0 s, the leader's wing scatters without firing; hard: pair every 1.3 s, each scattering Talon fires one parting pair as it breaks off |
<!-- /data -->

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

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

The chosen faction-contrast look of round 04, [talon-r04-a.png](../concept/talon-r04-a.png) (black and
gold with a red accent and the gold/red rim light), is listed in the [air](../README.md#concept-art)
Concept art table. Round 60's unit proposals (the gap analysis's wave 5) add the leader stripe, the
hex shield and the belt readability of [Q11](../../README.md#open-questions).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (gloss black; the user chose look b, gunmetal/charcoal with gold, in round 60, so the chosen variant is re-rendered in gunmetal in production), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 29 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/talon-r60-b.png](concept/talon-r60-b.png) | Talon B, gold panels — the outer wing panels and tail fins in gold, a 1 px rim; the hex shield as a round bubble (sheet) | chosen (round 60); production frames final (round 62, F.1) |
| [concept/talon-r60-b.gif](concept/talon-r60-b.gif) | Talon B, gold panels — the outer wing panels and tail fins in gold, a 1 px rim; the hex shield as a round bubble (motion) | chosen (round 60); production frames final (round 62, F.1) |

## Implementation

- [x] Stat block loaded from a `data.yaml`; global difficulty multipliers and the beam-era factor applied; HP and fire rate re-measured with `levelReport` at L29 (M9 part B, step B2: the swoop's `across` 280 px, ≈ 3 s on the screen; the aimed pair a 2-shot fan 4° wide)
- [x] The hex shield per [enemy shields](../../README.md#enemy-shields) (spent first, overflow to the hull, refill, `shield-breaker`, EMP) (part A's `EnemyShield`, step B2's `shield` 2 and the leader's 4; seen in round 62's capture)
- [x] The squad leader and the scatter (leader killed → the wing breaks off sideways and leaves), deterministic and allocation-free (steps B1, B2: `leader: tip` on a V-wing flies the `leader` airframe)
- [x] Production sprites (16 angles, the leader's stripe, the shield's hex flash and break) via `tools/art/` (`tools/art/belt_units.py`, steps ART-B and B4: `Level29LooksTest.theArtIsWhatTheLooksDraw` and `theMuzzlesAndJawsComeFromThePivots`; the shield is part A's look; the art pending the user's review in round 62)
- [x] Death effect, bounty and sounds (Freesound, CC0/CC-BY) per this spec (step B4: the turn-in whine, the twin shot and the leader's squelch as round 62's **a** proposals, `Level29SoundsTest`; the picks pending round 62)

## Open questions

- **Q1** (round 60, the shield's model): the draft follows **a** (a light shield every weapon hits);
  alternatives: **b** shields only `shield-breaker`, beams and the EMP get through (forces the Tesla
  Coil Pod in Act 5), **c** no shield mechanic in Act 5 (sparks only).
- **Q10** (round 60, the mutineers): **b** recommended (some Ceres pilots waver, a "Hold fire"
  secondary; the Level 29 draft); **a** all mutineers fly Talons and fight; **c** a remembered story
  choice.
- **Shield scale** (step B6, pending the user's review): the Talon's shield is now 12, the drafts' Gilded Gunship's
  and above the Hornet's 8 (neither built yet): their shields want the same measure when their levels are built.

## Decisions

- 2026-10-27 (step m9-d-enemies-a): drafted for the Act 5 design phase from the roster row and the
  gap analysis; every number is a first draft approved in round 60. HP 16 base (32
  with the beam era) for 0.27 s at the proposed L29 reference of 120; the shield 2 per Q1 a.
- 2026-10-27 (gap analysis, trivia decided, shown in round 60): the squad leader's scatter (the
  *Command* doctrine) rewards target choice and costs one flag per wave.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`); art A.1 **b**, "Talon B, gold panels", chosen (`art: chosen`), the other variant in `concept/rejected/`. Q11 **b** (gunmetal/charcoal with gold, not gloss black): the chosen variant is re-rendered in gunmetal/charcoal in this part's production art, keeping the ≥ 20 % gold and the readability check.
- 2026-10-28 (user, round 62 F.1 and C.1: "the shield need a bit more strength"; step B6, the value pending the
  user's review): the hex shield **2 → 12** (the leader 4 → 24), its refill rules unchanged; the rest of C.1's rework
  (130 px/s, the small groups) stays. Measured with `levelReport` in Level 29 (the plan's fit with the Ion Beam L4,
  medium, the waves before round 62's rock groups), the Talon's mean life on the screen from its entry to its kill:
  shield 2 **1.49 s**, 4 1.54 s, 8 1.53 s, **12 1.77 s**, 16 1.77 s, 20 1.90 s, 24 1.97 s, 40 2.13 s (with the rock
  groups: 12 → 1.77 s). The recommended 4 (leader 8) adds 2 HP to the 32 the beam era flies, 0.02 s at the L29
  reference of 120: no visible hold. 12 adds ≈ 0.3 s on the screen and holds the beam ≈ 0.1 s before the hull takes
  damage ("a bit stronger", not a wall). 20 (+0.4 s) was measured too, but with the rock groups the autopilot lost
  hard without an escort (`--escort none`, `Level29Test.theDebugEscortsFlyTheLevel`) on 3 of 6 seeds (12: 1 of 6;
  shield 2 with the rock groups: 1 of 6; 20 without them: 0 of 6). The shield takes the difficulty's HP lever, never
  the beam-era ×2 (the [shield rules](../../README.md#enemy-shields)).
- 2026-10-28 (step C2, Level 30's convoy; accepted in round 63, 2026-10-28): an `orbit` (90 px, 60°/s) and the `carrier + escorts` formation, so Talons escort the Gilded Gunship (the Gunship's own document: Talons in `orbit` at 90 px); Level 29 flies no escorts, its waves unchanged.
- 2026-10-28 (user, [round 63](../../../concept-rounds/round-63/README.md), closed): **C.1** accepted: the Talon's hex shield 12, the squad leader's 24.
- 2026-10-29 (M9 part I, step I2): `art: final`: the production frames (`tools/art/belt_units*.py`, every PNG with its `Source` chunk, so `importPlaceholders` skips them) are in `assets/sprites/` and the user accepted them as final in round 62 (F.1); the round 60 mockup rows are history.
