---
title: Depth Hunter
design: approved
implementation: in-progress
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-31
---

# Depth Hunter

## Summary

A grey human-built submarine drone, unmarked and unexplained, that rises in columns from below and fires slow, shootable `homing` torpedoes up the screen. **Hint 3**: its hull code is found under the paint on a wreck (Helix Dynamics).

## Design

### Stat block

Approved in round 48 (2026-10-22). Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)), from its [data.yaml](data.yaml) (M8 part G, step G2; the table is rendered from it); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the Act 4 reference DPS (L27 116: 1.0 s; no act factor at its first level). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top. The play plane under water is the `air` layer; `sub` is the deep water below it. The data: a `hover` at a `y` band with a `drift`, the `column` from the rear and the `homing` torpedo `aim: facing` (step G1; the [architecture](../../../tech/architecture/schemas-units/README.md)).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Unmarked (machine: the EMP Burst stuns it longest) |
| Layer | `air` (the play plane under water) |
| Size tier | `medium` |
| Size | 72×40 px, hitbox 56×28 |
| Parts | single |
| Orientation | `16 angles` (the torpedoes are launched along its facing, up) |
| HP | 120 (easy 90 / hard 156, from the global multipliers) |
| Armour / shield | none |
| Speed | 80 px/s rising and leaving, holds in the lower band |
| Movement | `column` from the bottom edge (`from: rear`, a 3 s edge warning): 3–4 hunters in a line, unit i of n holding at 380 + i ÷ (n − 1) × 80 px from the top (the band 380–460), drifting 12 px/s sideways, facing up; leaves downward after 10 s |
| Attack | a `homing` torpedo every 3.5 s along its facing (up), 90 px/s, turn rate 70°/s, 3 HP, `heavy` = 10 on impact, life 5 s, at most 8 in flight; the Decoy Flares retarget it; shot down with the ship's weapons |
| Formations | column (3–4) (entering from below) |
| Weak points | none (a plain grey hull; the thruster glow is drawn only) |
| Effective traits | `spread`, `homing`, `rear`, `anti-sub` |
| Credits | 30 (score 300 × chain); torpedoes 0 |
| Death | `medium` hull break, grey debris and an oil bloom; no wreck (the hull-code wreck is a placed prop) |
| First level / used in | L27; L26 as unseen torpedo trails at the end (Rook's shoot-down, no unit on the screen), first seen at L27; L28 as escort |
| Difficulty hooks | easy: a torpedo every 4.5 s, turn rate 55°/s; hard: every 2.8 s, 85°/s |
<!-- /data -->

### Behaviour

- **Grey, no marks** (Q11 a): the Depth Hunters are unmarked grey like Act 3's machines, no black and gold; the radio and the L27 briefing image of the hull code under the paint are the hint; the beacon colours stay red-gold until the reveal.
- **The torpedo**: slow and shootable like the Revenant Walker's missile and homing pods: the ship's weapons or Rook shoot it down in 1-3 hits; the Decoy Flares retarget it (an Act 4 special, part A).
- **Lower band**: the hunters hold below the ship's usual lane and fire upward, so the player must fly to the top and weave, a play-style change from Act 3's top-down fire; the column's entry is warned (the bottom edge).
- **Under water**: the torpedoes carry the x 0.7 and the Hunter's drag-free movement; a ship slowed by the Siren is the L27 danger the level avoids by not mixing the two.
- **Rook's loss** (L26 end): the unseen torpedo trails come from this unit but no Hunter is drawn (the level doc owns it).

### Difficulty and level use

- L27 first on the screen; L26's end has trails only, no unit.
- Never more than one column on the screen.

## Concept art

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): a grey hull with a thruster glow. Files `depth-hunter-r48-a/b` in [naval/concept](../concept/), listed in the [naval](../README.md#concept-art) Concept art table. ART-G's production frames (`tools/art/depth_hunter.py`, round 48 b: 16 headings, nozzle flicker, its own torpedo with
its wake and launch, the hull break and oil bloom; no longer the walker missile) are listed there as
`depth-hunter-final-r57-a`, accepted in round 57 (2026-10-25) (the game claims them in step G4).

## Implementation

- [x] Stat block loaded from a `data.yaml` (step G2, `ContentLoaderTest`); global difficulty multipliers applied; HP re-measured with `levelReport` (`BeamEraHpTest`) (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen)
- [x] Behaviour, attack and tells per this spec, deterministic and allocation-free (step G1, `DepthHunterTest`; step G4
  draws the rise as a fade in from 35 % over 1.2 s, `HunterLooks`)
- [x] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
  (step ART-G's `depth_hunter.py`, proposed in round 57; drawn by `HunterLooks` under the grade, the glow after it)
- [x] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec (the hull break over the oil bloom under
  the `medium` under-water burst, no wreck; the launch, run, impact and hull break: round 57's a until the pick,
  `Level27SoundsTest`)
- [x] The homing torpedo as a shootable projectile with retargeting (the Decoy Flares), launched from the bottom edge
  (`DepthHunterTest.aTorpedoGoesForABurningDecoyFlare`; the columns in Level 27, `Level27Test`; captured with the flares
  in [round 57](../../../concept-rounds/round-57/README.md))

## Open questions

- How a wreck's hull code is shown (briefing image and radio only): the L27 level doc.

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (recommended option, decided in round 48 (2026-10-22), Q11 a): grey until L28; the black and gold look is kept for the cruisers; rejected: b (black-and-gold trim spoils the reveal).
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-24 (M8 part G, step m8g1; defaults accepted in round 57 (2026-10-25), gap check `m8g-gaps.md` §1.1, §5 G3, G4): flown in the
  simulation: a `column` `from: rear` rises from below the bottom edge in the column's x (warned there), unit i of n
  holding at 380 + i ÷ (n − 1) × 80 px below the top edge (evenly over the band, 16 px between hit boxes on the way
  up), drifting 12 px/s (even places left, odd right) with its facing turned up; its torpedo (`homing` on an `aimed`
  attack with `aim: facing`) leaves along its facing, at most 8 in flight, and bursts on the ship, Rook or the
  Bulwark; never two columns on the screen (the validator). No wreck on death (G3: the hull-code wreck is a placed
  prop). Its `data.yaml` is step G2's.
- 2026-10-24 (M8 part G, step G2): the stat block as `data.yaml` (the approved numbers; the hover's band, drift and
  the torpedo's `aim: facing` and `homing` as step G1 flies them); `first_level: 27` (L26 shows trails only).
- 2026-10-25 (M8 part G, step G5; accepted in round 57 (2026-10-25)): built and flown in Level 27 (the game captures
  in [Level 27](../../../campaign/act-4-deep-water/level-27-hydrothermal-vents/README.md#concept-art)); the round asks the
  finals (F.2) and the columns' x and counts (C.4). A column's Hunters hold evenly over their band and drift apart
  (G1); no sound for the rise (not in the gap check's list).
- 2026-10-25 (user, [round 57](../../../concept-rounds/round-57/README.md) closed): the final frames (F.2) accepted as final; the part is `art: final`. Its sounds: the
  torpedo launch **a**, the run **b**, the impact **b** and the hull break **a** (the b picks go into the game in step H5);
  C.4 (the columns' places and counts) accepted, and so is the Hunter's rise without a sound of its own (C.7).
- 2026-10-30, the user: dither audit, ≥ 25 % smoothed (step DITH8): `depth-hunter-wake`, `-launch`, `-break`, `-oil` in smooth steps, not the 4×4 Bayer checker ([audit](../../../art-direction/production/README.md#dither-audit-2026-10-30)).
