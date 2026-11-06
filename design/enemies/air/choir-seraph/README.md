---
title: Choir Seraph
design: approved
implementation: not-started
art: chosen
depends-on: [../choir-herald, ../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../art-direction, ../../../world/vrell-space, ../../../campaign/act-7-beyond-the-gate]
updated: 2026-11-05
---

# Choir Seraph

## Summary

The Choir's elite, from its inner sphere (Level 47, the Seraph Gauntlet): a pale bone seraph of the
[Choir Herald](../choir-herald/README.md)'s family, lit gold-white. Seraphs come in pairs. Each one `teleport`s to a
point marked by a growing ring and then `mirror`s the ship across the field's centre for a few seconds, firing a
`spiral` as it goes. The player lines it up only by crossing the middle.

## Design

### Stat block

**Draft** (Act 7 design phase, step m11-d-enemies-a, 2026-11-04; decided 2026-11-05, round 84, Q3 = a). A
hand-written table: there is no `data.yaml` yet. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP in damage units (a Pulse Cannon L1 shot does 2). The roster's
**40 HP** is read on its family's scale (the Choir Herald's first level, L17, reference 84) and takes the act HP
factor to its first level (J11, L47 = 188): 40 × 188/84 ≈ **90**. A `medium` unit: no beam-era factor.

| Field | Value |
|---|---|
| Faction | Vrell (elite: the *Vrell elite* look in [factions](../../README.md#factions-and-visual-language)); biomechanical (the [EMP Burst](../../../player/specials/README.md#emp-burst-l15) stuns it 1.5 s; one mid-jump misses the burst) |
| Layer | `air` |
| Size tier | `medium` |
| Size | 72×76 px, hitbox 48×52 |
| Parts | single |
| Orientation | `fixed` (faces down; its wings fold for a jump) |
| HP | 90 (easy 68 / hard 117, from the global multipliers): ≈ 0.48 s at the L47 reference (188); lined up only near the centre, so its time on the screen is set by the mirror, not the HP |
| Armour / shield | none of its own (J4); a [Choir Herald](../choir-herald/README.md)'s aura ring covers it while it stays in the aura's radius, and a jump out of the radius ends the ring (the Herald's rule) |
| Speed | mirroring: up to 260 px/s sideways (it follows the mirrored point with a 0.15 s lag, never faster) |
| Movement | `teleport` + `mirror`, a cycle: **arrive** (the ring: **1.0 s** for its first arrival on the screen, **0.5 s** later), **mirror** the ship's x around the field's centre for **2.5 s** (2–3 s, the wave's) at its arrival row, **fold** 0.3 s (stops firing, wings close: the jump's tell), fade out 0.2 s, jump. **4 cycles**, then it jumps out for good. The shared [`teleport` and `mirror`](../../README.md#movement-pattern-vocabulary) rules, with this unit's numbers: arrival never inside **140 px** of the ship, never in the **80 px** band at the ship's edge of the field; its row stays ≥ 140 px from the ship's y |
| Attack | `spiral` while mirroring: two arms, **6 shots/s** in all, turning 120°/s, slow 120 px/s, `small` = 4, the arms' start angle set by its arrival (so two Seraphs' spirals interleave, never stack); it fires only while mirroring. Contact `medium` = 15 |
| Formations | **pairs**: both arrive together at two rows ≥ 90 px apart (more than a hull's height: never overlapping), mirroring the same point; at most one pair on the screen, plus a Choir Herald and the act's regulars |
| Weak points | the gold-white heart in its chest (drawn only) |
| Effective traits | `homing` (it jumps; homing shots pick the new arrival), `beam` (held on the centre line it burns both of a pair stacked above the ship) |
| Credits | 35 (score 350 × chain) |
| Death | `medium` organic burst with a falling chord (the Herald's family), its wings scattering into gold-white feathers of light that fade |
| First level / used in | L47 (the Seraph Gauntlet, "the hardest regular waves in the game"); later Act 7 levels as the level drafts place it |
| Difficulty hooks | easy: mirror 2.0 s, 3 cycles, 4 shots/s, single Seraphs before pairs; hard: mirror 3.0 s, 5 cycles, 8 shots/s |

### Behaviour

- **The mirror.** A mirrored Seraph sits at the field's width minus the ship's x: when the ship is at the centre it
  is straight above it, anywhere else it is on the far side. The forward guns line it up only by crossing the middle,
  and the spiral is densest there: the elite's puzzle. Wing weapons, homing shots and the Tesla arc (if the ship
  dares the 180 px reach) work from anywhere.
- **The jump.** The fold is the tell that it is about to vanish: the player has 0.5 s to finish it. Homing shots in
  flight pick a new target when it vanishes (the shared rule); the arrival ring is the next place to aim.
- **The leaving tell** (Level 47's secondary, the round 80 lesson): a Seraph's **last** fold is 0.8 s instead of
  0.3 s, its wings flaring white as it rises 40 px, with a rising glass chime; the player can still finish it in the
  tell, and the HUD's tracker blinks amber while one shows it (the [HUD](../../../ui/hud/README.md#act-7-additions-draft)'s
  shared leaving blink).
- **Fairness** (Q3 a): never on top of the ship (the 140 px arrival rule, the 80 px band, the row's 140 px from the
  ship's y); a spiral, never an aimed shot, while it mirrors, so following the ship never becomes perfect aim; no
  bullet within 72 px of the ship (the global rule).
- **Look** (Q10 b, the *Vrell elite* look; the Choir Herald's chosen "seraph" b family): pale bone and ivory chitin,
  two pairs of long swept wings with gold-white light along their edges and a gold-white heart; its spiral's tell is
  a violet flare at the wing roots (the aimed/threat hue keeps its meaning), the arrival ring violet with a violet-white core,
  never gold (the world document's Level 47 readability rule), over its time, never a flash.
- **Sounds** (brief for the audio step, Freesound CC0/CC-BY): a bowed glass or singing-bowl tone that bends with its
  mirroring, the jump a soft air rush, the arrival a rising glass shimmer, the spiral a fast soft tick; the Choir's
  voice is the audio step's, not this unit's.

### Difficulty and level use

- At most one pair on the screen; a level never puts a second elite cycle's arrival on the same 0.5 s as another
  unit's edge warning on the ship's side.
- Level 47 shows one Seraph alone first (its ring 1.0 s), then pairs, then pairs with a Herald.

## Concept art

Round 84's proposals (step ART-R-A, M11 design phase, picked 2026-11-05; generator `tools/concept/enemies_r84.py choir-seraph`). Each sheet holds the source render, the sprites at play size with their states, the readability check over Level 47's brightest and darkest band beside the player (a stand-in of the level's light in the one Vrell palette, Q10 b), and key frames; each GIF is the 480×540 play field. Prompts: [concept/prompts.md](concept/prompts.md). The arrival ring is drawn violet with a violet-white core, never gold: the world document's L47 readability rule; this README's Look row now says the same (it said gold-white over violet; the round confirms).

| File | What | Status |
|---|---|---|
| [concept/choir-seraph-r84-a.png](concept/choir-seraph-r84-a.png) | Choir Seraph a, "blades": a slim ivory seraph with two pairs of long swept blade wings, gold-white leading edges and heart; mirroring, the violet flare, folding, folded, fading, the arrival ring, its shot; over the inner sphere's bone vault | chosen (round 84 A.4 a) |
| [concept/choir-seraph-r84-a.gif](concept/choir-seraph-r84-a.gif) | A pair arrives (violet rings), mirrors the ship across the centre firing interleaved two-arm spirals, folds and jumps to new rows; the ship crosses the middle and kills the lower one (feathers of light), the other jumps on | chosen (round 84 A.4 a) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [ ] Stat block in a `data.yaml`; global multipliers; checked with `levelReport` at L47 (the pairs' spiral density
  and time on the screen)
- [ ] The shared `teleport` and `mirror` (the arrival rules, the mirror's lag and speed cap, the retargeting); the
  cycle and the pair's rows; deterministic and allocation-free (the shared `teleport` and `mirror` built in M11 part A, step A1a: `TeleportTest`, `ShadowingTest`; the cycle and the pair's rows are part E's)
- [ ] `spiral` while mirroring, the pair's interleaved arms
- [ ] Production sprites (the wings' fold, the arrival ring, the heart, the death's feathers) via `tools/art/`;
  sounds (Freesound, CC0/CC-BY)

## Open questions

- **The arrival ring's colour**: violet only (drafted now, the world document's rule and both proposals) or
  gold-white over violet (the first draft's Look row; gold risks reading as a pickup over the bone).
- The HP of 90 (the roster's 40 on the Herald's scale × 188/84); a heavier elite (≈ 180, 1 s at the reference) is
  the alternative if the round wants Seraphs that outlast a crossing of the centre.

## Decisions

- 2026-11-04 (step m11-d-enemies-a): promoted from the air roster for the Act 7 design phase from the M11 gap
  analysis (§2.2, Q3 a, J4): `teleport` with a told arrival never inside 140 px nor in the ship's bottom 80 px, then
  `mirror` the ship's x for 2–3 s firing a `spiral`, in pairs; the roster's 40 HP with the act HP factor. The 4
  cycles, the 0.3 s fold, the 1.0 s first ring, the 260 px/s mirror cap, the pair's rows and the interleaved spirals
  are this step's. Decided 2026-11-05, round 84.
- 2026-11-04 (step m11-d-act, decided 2026-11-05, round 84): the leaving tell (last fold 0.8 s, the wings flaring white, a 40 px
  rise, a rising chime; Level 47's proposal) and the HUD's amber leaving blink; the arrival ring violet, never gold,
  as the world document's Level 47 rule (an open question for the round).
- 2026-11-05, round 84 (the user's page answers; decided as drafted unless stated, the alternatives rejected). The open items were:
  - **Q3** (round 84): how far do the elites go? **a** (drafted) teleport to a told point never inside 140 px, then
    mirror the ship's x for 2–3 s firing a `spiral`, in pairs; **b** teleport only, no mirror (less of an elite);
    **c** mirror only, no teleport (a curiosity, not the act's hardest waves).
- 2026-11-05 (user, round 84 closed): **A.4 a "blades"** chosen (a slim ivory seraph with two pairs of long blade wings and gold-white edges); b "halo" rejected (moved to `concept/rejected/`); **E.5 a**: the arrival ring is violet only; **Q3 a** as drafted; **D.11 approved**; the sounds are S.13 a, S.14 b, S.15 a, S.16 b.
