---
title: Rail Serpent
design: approved
implementation: done
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../coilwyrm, ../../../campaign/act-5-the-belt]
updated: 2026-10-31
---

# Rail Serpent

## Summary

An armoured Ascendancy drone train of 8–14 cars that snakes across the screen and enters from the
sides or the rear. Every third car carries a turret; the engine car at the front is `vital`.
Destroying a car **splits** the train the machine way: the cars behind the cut lose power, drift with
sparks and explode, while the front half runs on (Level 30).

## Design

### Stat block

Approved in round 60 (drafted in the Act 5 design phase); rendered from [data.yaml](data.yaml) since M9
part C (step C2). Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does
2), set at its first level from the proposed Act 5 reference DPS (L30 = 124). A `large` unit: in a later
level its parts take the act HP factor (e.g. ×136/124 at L33).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy (machine: the EMP Burst stuns it longest) |
| Layer | `air` |
| Size tier | `large` |
| Size | 64×64 px, hitbox 45×45 (the engine car); cars 48×48 px, a 10-car train ≈ 420 px long; hitboxes 70 % of the sprite |
| Parts | engine car (`vital`), 8–14 cars (`destroyable`): every third car (3rd, 6th, 9th, 12th) a **turret car**, the last car the **tail** (`destroyable`, a bonus when destroyed first) |
| Orientation | `32 angles` per car (each car faces along the path) |
| HP | engine 150 (1.2 s at the L30 reference of 124), car 16, turret car 24, tail 20; a 10-car train 150 + 6 × 16 + 3 × 24 + 20 = 338 (easy 254 / hard 439, from the global multipliers) |
| Armour / shield | none: the "armour" is its HP per car; no hex shield (an unmanned train) |
| Speed | 170 px/s (the cars `chain` on the engine's path history at 0.85 × the car length) |
| Movement | the engine flies an authored `swirl` or `cross` path from the wave, **entering from a side or the bottom edge** (`from: sides` / `rear`; a wave's first entry edge-warned 3 s ahead), crossing the screen once with at most one `loop`, and leaves through another edge |
| Attack | each turret car: `aimed` single shot every 2.4 s, the turret cars staggered 0.6 s apart (standard 160 px/s, `small` = 4), a 0.2 s red lamp on the turret as the tell; contact `large` = 20 (engine) / `small` = 10 (cars) |
| Formations | snake, pincer (one train, or two from both sides, edge-warned) |
| Weak points | the engine's reactor glow (the vital part) |
| Effective traits | `homing`, `piercing`, `beam`, `rear` |
| Credits | 73 (score 730 × chain): a 10-car train dismantled (engine 40, car 3, turret car 5), 83 with the tail first (+10); an engine-first kill pays the engine's 40 and the chained explosion's time bonus (the [multi-part](../../README.md#multi-part-enemies) scoring rule) |
| Death | engine killed: the whole train blows car by car from the front (0.1 s apart), each a `medium` explosion with gold debris; a split's rear half: see below |
| First level / used in | L30; behind the ship, beside the Minelayers; its later Act 5 levels per the level drafts |
| Difficulty hooks | easy: 8 cars, turrets every 3.0 s; hard: 14 cars, each turret a `burst` of 2 (0.15 s apart) |
<!-- /data -->

### Behaviour

- **The machine split** (decided in the gap analysis, shown in round 60; the multi-part rule in
  [enemies](../../README.md#multi-part-enemies)). Destroying any car but the engine cuts the train:
  - the cars **behind** the cut lose power: they stop following the path, drift on their last
    heading at 40 % of the train's speed (slowing to a stop), spark and smoke for **1.5 s**, their
    turrets dark, then explode one after another from the cut backwards (0.1 s apart);
  - the drifting wreckage does **no damage** to the ship (no contact, no blast) and pays every car's
    bounty to the player as it explodes;
  - the front half, still pulled by the engine, runs on along the path with its remaining turret
    cars.
  Unlike the Vrell [Coilwyrm](../coilwyrm/README.md), nothing regrows: cutting near the engine
  disarms most of the train at once, cutting near the tail pays the tail bonus.
- **Rear entry.** The Serpent is Act 5's rear and side threat with the Minelayer: its first entry is
  edge-warned; a `loop` back through an edge is its own flight and is not (the readability rule).
- **Sounds** (brief for the audio step): a heavy rail-car rumble and rhythmic coupling clatter while
  on the screen (real trains or trams from Freesound, CC0/CC-BY), a shrill electric whine for the
  engine, a coupler snap and a falling power-down whine at a split, a rolling chain of thuds as the
  wreckage blows.

### Difficulty and level use

- At most one train on the screen, two in a `pincer`.

## Concept art

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

The chosen look of round 05, [rail-serpent-r05-a.png](../concept/rail-serpent-r05-a.png) and its
[motion GIF](../concept/rail-serpent-r05-a.gif) (head car and 8 cars at 16 headings, red accent), is
listed in the [air](../README.md#concept-art) Concept art table. Round 60's unit proposals add the
turret cars, the 32 angles, the split's sparks and the belt readability of
[Q11](../../README.md#open-questions).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (gloss black; the user chose look b, gunmetal/charcoal with gold, in round 60, so the chosen variant is re-rendered in gunmetal in production), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 30 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/rail-serpent-r60-b.png](concept/rail-serpent-r60-b.png) | Rail Serpent B, gold roofs and links — gold roof armour with a black centre stripe on every car, gold coupler links between the cars (sheet) | chosen (round 60) |
| [concept/rail-serpent-r60-b.gif](concept/rail-serpent-r60-b.gif) | Rail Serpent B, gold roofs and links — gold roof armour with a black centre stripe on every car, gold coupler links between the cars (motion) | chosen (round 60) |

## Implementation

- [x] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; re-measured with `levelReport` at L30
- [x] The train on the existing `Chain` (path history), the turret cars' staggered aimed shots, side and rear entries with the edge warning on the first entry only (step C1: `ChainSpec.withCars`; `RailSerpentTest.everyThirdCarIsATurretCarAndTheLastTheTail`, `theTurretCarsFireStaggeredEachAfterItsLamp`)
- [x] The machine split: the rear half drifts 1.5 s without damage, then explodes car by car paying its bounties; the front half runs on (the enemies item "a machine chain's split") (step C1: `Scatter.wreck`; `RailSerpentTest.aCutStopsTheCarsBehindItWhichBlowFromTheCutBackwardsPayingTheirBounty`, `Level30Test.aWreckedTrainPays`)
- [x] Production sprites (32 angles per car, the turret cars, the tail) via `tools/art/`; death effects, bounty and sounds (Freesound, CC0/CC-BY) (frames: `tools/art/belt_units_c.py serpent`, step ART-C: `rail-serpent` (engine), `-segment`, `-turret`, `-turret-tell`, `-tail` and the wrecks `-segment-wreck`, `-turret-wreck`, `-tail-wreck` at 32 headings, `-sparks`, `-snap`, `pivots/rail-serpent.json`; held until this unit's `data.yaml` exists; drawn by C4; accepted in round 63, 2026-10-28) (step C4: the engine, segments, turret cars and their tell, the tail, the wreck sets with their sparks and the snap at the cut; the rumble, the turret's shot and the split as round 63's **a** proposals, `Level30SoundsTest`; not made: the engine's whine; seen in round 63's captures)

## Open questions

- None open (no round-60 question touches it).

## Decisions

- 2026-10-27 (step m9-d-enemies-a): drafted for the Act 5 design phase from the roster row and the
  gap analysis; every number is a first draft approved in round 60. The split's
  drift is 1.5 s (the enemies document's earlier "after 1 s" raised to show the sparks), harmless to
  the ship, paying its cars.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`); art A.5 **b**, "Rail Serpent B, gold roofs and links", chosen (`art: chosen`), the other variant in `concept/rejected/`. Q11 **b** (gunmetal/charcoal with gold, not gloss black): the chosen variant is re-rendered in gunmetal/charcoal in this part's production art, keeping the ≥ 20 % gold and the readability check.
- 2026-10-28 (step ART-C, production art; accepted in round 63, 2026-10-28): A.5 b re-rendered in gunmetal (Q.11 b) with the gold roof only on its inner bands beside the black centre stripe (gold 23–32 %; b's full roofs gave 40–47 %) and gold coupler stubs front and rear that meet the next car's at about 40 px spacing (the "gold links" drawn in each car's sprite). Cars about 37×35 px on 48 px frames (the stat block's 48×48 sprites), the engine on 64. The split's rear part is drawn with wreck sets (lights dead, scorched, the gold dulled) and an additive spark loop: vacuum, so no smoke (the stat block's "spark and smoke").
- 2026-10-28 (step C2, the data; accepted in round 63, 2026-10-28): the stat block as data: a 10-car train is the engine, 9 cars (`segments`, turret cars 3, 6 and 9) and the tail; its HP 338 counts the tail (the draft's 318 left it out; the content check now counts the turret cars' HP and bounty), the cut `{scatter: 68, seconds: 1.5, wreck: true}`, the turret cars' `car-gun` (`tell` 0.2 s, the first shot 0.5 s in, hard's `burst: 2` 0.15 s apart, easy 3.0 s), easy 7 / hard 13 `segments`.
- 2026-10-28 (user, [round 63](../../../concept-rounds/round-63/README.md), closed): the final art accepted (**F.4**); sounds **S.4 a** (`serpent-shot`, the .50 BMG), **S.5 redo** (`serpent-rumble`, no note: new proposals in round 64's audio step; a keeps playing), **S.6 b** (`serpent-split`, the chain snap and arc welder).
- 2026-10-28 (user, [round 64](../../../concept-rounds/round-64/README.md), closed; page answers): **S.11 a** (`serpent-rumble`, the redo of round 63's S.5): an electric locomotive's traction-motor whine over a subway train's rail-joint clatter, `serpent-rumble-r64-a`; it replaces round 63's a (`serpent-rumble-r63-a`), which the game plays until the next game step switches the path (the new file is in `assets/sfx/` already); b rejected.
- 2026-10-30, the user: dither audit, ≥ 25 % smoothed (step DITH8): `rail-serpent-snap` in smooth steps, not the 4×4 Bayer checker ([audit](../../../art-direction/production/README.md#dither-audit-2026-10-30)).
