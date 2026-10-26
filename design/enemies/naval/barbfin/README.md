---
title: Barbfin
design: approved
implementation: in-progress
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-24
---

# Barbfin

## Summary

A small Vrell swimmer with a barbed snout that darts in, holds and spits aimed needles: the Needler's role under water. It is also the escort of the Abyss Ray.

## Design

### Stat block

Approved in round 48 (2026-10-22). Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)), from its [data.yaml](data.yaml) (M8 part D, step D2; the table is rendered from it); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the Act 4 reference DPS (L24 104); the beam era's ×2 applies from the level number (24 at L24), not `small_act_hp` (gap check `m8d-gaps.md` §5 D5). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top. The play plane under water is the `air` layer. The data: the Needler's `swoop` and `hover` with the hold `above_ship` (90–200 px above the ship's y at the moment it stops, a seeded draw) instead of a fixed height; its `orbit` (the Needler's 90 px, 60°/s) in `circle` formations.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (the play plane under water) |
| Size tier | `small` |
| Size | 36×28 px, hitbox 26×20 |
| Parts | single |
| Orientation | `32 angles` (it turns toward the ship while it holds) |
| HP | 26 (easy 20 / hard 34, from the global multipliers) |
| Armour / shield | none |
| Speed | 120 px/s in, 0 while it holds |
| Movement | `swoop` in from a side or top edge along a short curve (in a rear ambush up from the bottom edge, stopping on the way up), stops 90–200 px above the ship's lane and holds 9 s with a 6 px/s drift, turning toward the ship, then leaves toward the nearest side; as an Abyss Ray's escort it peels off the Ray's shadow as it rises |
| Attack | `aimed` needle (a violet-hued shot) every 2.4 s, 150 px/s, `small` = 4; first shot 0.9 s after it stops |
| Formations | column (3–5), circle (4–5), carrier + escorts (4), line abreast (2), pincer (2), rear ambush (2) (carrier + escorts: the [Abyss Ray](../abyss-ray/README.md)'s escort; rear ambush: up from the bottom edge on the ambush lanes, Level 27); scattered pairs |
| Weak points | violet snout barb (drawn only) |
| Effective traits | `spread`, `homing` |
| Credits | 12 (score 120 × chain) |
| Death | `small` flash with a sinking shell and a short ink-grey cloud |
| First level / used in | L24; L25, L27 and the Maw's L28 escorts |
| Difficulty hooks | easy: a needle every 3 s; hard: a 2-needle `burst` 0.12 s apart every 2.6 s |
<!-- /data -->

### Behaviour

- **Needler pattern**: as the [Needler](../../air/needler/README.md): enters, stops 90–200 px above the ship's y at the moment it stops (a seeded draw), drifts 6 px/s and turns toward the ship while it holds, fires on its own seeded cadence, leaves; the holders are what lets Rook and the ship pick them off one by one.
- **Fire budget**: the Act 2+ floor needs 10 enemy shots a minute at medium; Barbfin groups are the floor's shot source in the swimming levels (a group of 4 fires about 100 shots in 60 s). Bullets x 0.7 live longer on the screen, so the per-level rates are set with `levelReport`.
- **Escort of the Abyss Ray**: a Ray arrives with four Barbfins in `circle` that peel off its shadow as it rises (held unspawned until the rise's midpoint, then fanned out from the Ray to their holds, gap check `m8d-gaps.md` §5 D3); they keep their own cadence after the Ray leaves or dies.
- **Under water**: only its needles are slowed; the unit's own movement is not.

### Difficulty and level use

- L24 introduces it (with the Abyss Ray); a school-plus-Barbfin wave is the L24-L27 workhorse.
- Beam-era HP applies by level number (`small`): 52 at L24 (M8 AW: 26 × 2; 24 before), not `small_act_hp` (gap check D5); the reduction of the beam's reach is re-measured in part D (`BeamEraHpTest`'s 0.9-1.5x rule, step D3).

## Concept art

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): sprite sheet and motion. Files `barbfin-r48-a/b` in [naval/concept](../concept/), listed in the [naval](../README.md#concept-art) Concept art table. Notes: the needles are yellow in the mockup (spec: violet).
The production frames (round 48's a, the bone lionfish; `tools/art/barbfin.py`, step ART-D) are `barbfin-final-r53-a` with the game capture `barbfin-capture-r53-a`, in the same table; its violet needle `needle-violet` is on the [enemies](../../README.md#concept-art) bullet sheet `enemy-bullets-final-r53-a`; proposed in [round 53](../../../concept-rounds/round-53/README.md).

## Implementation

- [x] Stat block loaded from a `data.yaml` (step D2: loaded and checked, the hover `above_ship`); global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen; step D3: Level 24's small units 1.42 × Level 10's time on the screen, within 0.9–1.5)
- [x] Behaviour, attack and tells per this spec, deterministic and allocation-free (steps D1 and D3: `BarbfinTest`, `Level24Test`)
- [x] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
      (step ART-D: `tools/art/barbfin.py` and the violet needle, round 53; step D4: `Level24LooksTest.theBarbfinTellsBeforeItsNeedle`;
      the portrait: `PortraitsTest.level24sNewUnitsHaveTheirSensorPortraits`)
- [x] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec (step D4: the under-water `small` burst,
      the sinking shell and the ink; the needle a/b in round 53, `Level24SoundsTest.thePlainsUnitsBeaconsAndWreckSound`)

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (our reading, Q3 a): the small firing swimmer, named Barbfin; first appears at L24 as the plan's part D asks.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-22 (M8 part D, step D2; gap check `m8d-gaps.md` §5 D3, D5, accepted in round 53 (2026-10-23)): the stat block as
  data, the table rendered from it. The open question closed by the default: the beam era's × 2 only (24 HP at L24),
  not `small_act_hp` (re-measured in step D3). The hold `above_ship` (90–200 px above the ship's y, a seeded draw, a 6
  px/s drift, the facing turned toward the ship); as the Ray's escort held until its rise and fanned out from it. Step
  D2's own: the Needler's `orbit` (90 px, 60°/s) for its `circle` formations, the attack named `needle` (its easy and
  hard intervals), hard's burst gap 0.12 s on the attack.
- 2026-10-22 (M8 part D, step D3, accepted in round 53 (2026-10-23)): mapped and flown in Level 24. The drift as the
  hover's `drift` key (6 px/s); the hold's depth below the top edge 60–460 px (never higher than 60, a path from the top
  ends at 460 at the latest). HP kept at 24 (the beam era's × 2): Level 24's small units live 2.27 s on the screen, 1.42
  × Level 10's (allowed 0.9–1.5) once the autopilot shoots the Barbfins before the mute fry. **Hard's interval 2.1 → 2.6
  s** (the 2-needle burst kept): with 2.1 the plan's fit without Rook (`--escort none`) lost every retry of Level 24 on
  hard (seed 1 at t≈161, seed 2 at t≈144; the needles the largest share, ~90 of ~320 damage); with 2.6 seeds 1 and 2
  complete in one attempt (seed 3 still not: measured, not asserted).
- 2026-10-22 (M8 part D, steps D1, ART-D and D4; accepted in round 53 (2026-10-23)): step D1's own: the drift's
  side is drawn per unit. Step ART-D: the final frames from round 48's a (32 headings × 2 swim frames and the shot's
  tell, the eye and barb glow, added as `barbfin-glow`, the snout sparkle, the sinking shell, the ink cloud) and the
  violet needle `needle-violet` (16 headings, as the yellow `needle`). Step D4 (`BarbfinLooks`, `BulletLooks`): the
  Barbfin fires its own bullet look (the needle turned to its velocity; the Ray's fans stay orbs), the glow ungraded,
  frame 2 held 0.25 s before a needle; its death the under-water `small` burst, then the shell (heading ÷ 4) and the ink.
  Weak spot: the first needle after the Barbfin arrives comes without the 0.25 s tell (the tell keys on the previous
  shot's clock).
- 2026-10-23 (user, round 53, closed): the final frames and the violet needle accepted (F.3; the part is `art: final`); the needle sound **b** picked; C.2 (hard's 2.6 s interval) accepted.
- 2026-10-24 (M8 AW, step AW2; accepted in round 56 (2026-10-24)): HP 12 → 26 (52 at L24–25 with the beam era's
  × 2): the reworked harpoon (2.8 torpedoes a second at L3, 31 damage, a 44 px splash) killed a Barbfin in about a second
  from its entry, before its first needle (Level 24's small units 0.68× Level 10's time on the screen, Level 25's floor
  3 shots a minute); at 52 it takes two L3 hits (a hit and its splash, 50, do not kill it). Measured with the levels'
  extra Barbfins: Level 24 0.94×, Level 25 1.25×. "Tougher, not more" (the user's rule of 2026-10-19) as far as it went:
  16 and 20 HP did not reach 0.9×. The circle's size 4–5 (Level 24's circles of 5).
- 2026-10-24 (M8 AW, step AW3; accepted in round 56 (2026-10-24)): in a level with rock walls (Levels 24–26's trench
  and scarp) a Barbfin's hold and its leaving after the hold stay inside the flyable span (`Canyon.inSpan`): it slides
  down along a wall and leaves inside the trench instead of crossing the rock sideways.
- 2026-10-24 (user, round 56 closed, C.4): the tougher Barbfin and the retuned waves of Levels 23–26 accepted.
- 2026-10-24 (M8 part G, step m8g1; accepted in round 57 (2026-10-25), gap check `m8g-gaps.md` §5 G9): formations `line abreast`,
  `pincer` and `rear ambush` (Level 27). In a rear ambush it has no ambush path: the pair rises from the bottom edge on
  the ambush lanes (x = (i + 1) × 480 ÷ (n + 1)), stops on the way up at its own height above the ship and holds as
  usual; the wave is warned at the bottom edge (`Formations.rearSwoops`).
