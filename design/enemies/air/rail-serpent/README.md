---
title: Rail Serpent
design: draft
implementation: not-started
art: proposed
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../coilwyrm, ../../../campaign/act-5-the-belt]
updated: 2026-10-27
---

# Rail Serpent

## Summary

An armoured Ascendancy drone train of 8–14 cars that snakes across the screen and enters from the
sides or the rear. Every third car carries a turret; the engine car at the front is `vital`.
Destroying a car **splits** the train the machine way: the cars behind the cut lose power, drift with
sparks and explode, while the front half runs on (Level 30).

## Design

### Stat block

**Draft** (Act 5 design phase, pending the user's review in round 60). A hand-written table: there is
no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does
2), set at its first level from the proposed Act 5 reference DPS (L30 = 124). A `large` unit: in a later
level its parts take the act HP factor (e.g. ×136/124 at L33).

| Field | Value |
|---|---|
| Faction | Jovian Ascendancy; a **machine** (stunned 3 s by the [EMP Burst](../../../player/specials/README.md#emp-burst-l15): the train halts in place and its turrets go dark, then runs on along its path) |
| Layer | `air` |
| Size tier | `large` (a 10-car train ≈ 420 px long) |
| Size | engine car 64×64 px, cars 48×48 px; hitboxes 70 % of the sprite |
| Parts | engine car (`vital`), 8–14 cars (`destroyable`): every third car (3rd, 6th, 9th, 12th) a **turret car**, the last car the **tail** (`destroyable`, a bonus when destroyed first) |
| Orientation | `32 angles` per car (each car faces along the path) |
| HP | engine 150 (1.2 s at the L30 reference of 124), car 16, turret car 24, tail 20; a 10-car train 150 + 6 × 16 + 3 × 24 = 318 (easy 0.75 / hard 1.3 per part, from the global multipliers) |
| Armour / shield | none: the "armour" is its HP per car; no hex shield (an unmanned train) |
| Speed | 170 px/s (the cars `chain` on the engine's path history at 0.85 × the car length) |
| Movement | the engine flies an authored `swirl` or `cross` path from the wave, **entering from a side or the bottom edge** (`from: sides` / `rear`; a wave's first entry edge-warned 3 s ahead), crossing the screen once with at most one `loop`, and leaves through another edge |
| Attack | each turret car: `aimed` single shot every 2.4 s, the turret cars staggered 0.6 s apart (standard 160 px/s, `small` = 4), a 0.2 s red lamp on the turret as the tell; contact `large` = 20 (engine) / `small` = 10 (cars) |
| Formations | `snake` (one train), `pincer` (two trains from both sides, edge-warned) |
| Weak points | the engine's reactor glow (the vital part) |
| Effective traits | `homing`, `piercing`, `beam` (along the train), `rear` (rear and side entries) |
| Credits | engine 40, car 3, turret car 5, tail +10 when destroyed first; a 10-car train dismantled pays 40 + 6 × 3 + 3 × 5 = 73 (83 tail first); an engine-first kill pays the engine's 40 and the chained explosion's time bonus (the [multi-part](../../README.md#multi-part-enemies) scoring rule) |
| Death | engine killed: the whole train blows car by car from the front (0.1 s apart), each a `medium` explosion with gold debris; a split's rear half: see below |
| First level / used in | L30 (behind the ship, beside the Minelayers); its later Act 5 levels per the level drafts |
| Difficulty hooks | easy: 8 cars, turrets every 3.0 s; hard: 14 cars, each turret a `burst` of 2 (0.15 s apart) |

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

The chosen look of round 05, [rail-serpent-r05-a.png](../concept/rail-serpent-r05-a.png) and its
[motion GIF](../concept/rail-serpent-r05-a.gif) (head car and 8 cars at 16 headings, red accent), is
listed in the [air](../README.md#concept-art) Concept art table. Round 60's unit proposals add the
turret cars, the 32 angles, the split's sparks and the belt readability of
[Q11](../../README.md#open-questions).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (pending the user's review), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 30 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/rail-serpent-r60-a.png](concept/rail-serpent-r60-a.png) | Rail Serpent A, gold spine and skirts — the r05 gold spine bar plus gold side skirts, a white-gold reactor on the engine, domed turret cars with a red lamp, gold tail fins; strong rim (sheet) | proposed |
| [concept/rail-serpent-r60-a.gif](concept/rail-serpent-r60-a.gif) | Rail Serpent A, gold spine and skirts — the r05 gold spine bar plus gold side skirts, a white-gold reactor on the engine, domed turret cars with a red lamp, gold tail fins; strong rim (motion) | proposed |
| [concept/rail-serpent-r60-b.png](concept/rail-serpent-r60-b.png) | Rail Serpent B, gold roofs and links — gold roof armour with a black centre stripe on every car, gold coupler links between the cars (sheet) | proposed |
| [concept/rail-serpent-r60-b.gif](concept/rail-serpent-r60-b.gif) | Rail Serpent B, gold roofs and links — gold roof armour with a black centre stripe on every car, gold coupler links between the cars (motion) | proposed |

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; re-measured with `levelReport` at L30
- [ ] The train on the existing `Chain` (path history), the turret cars' staggered aimed shots, side and rear entries with the edge warning on the first entry only
- [ ] The machine split: the rear half drifts 1.5 s without damage, then explodes car by car paying its bounties; the front half runs on (the enemies item "a machine chain's split")
- [ ] Production sprites (32 angles per car, the turret cars, the tail) via `tools/art/`; death effects, bounty and sounds (Freesound, CC0/CC-BY)

## Open questions

- None open (no round-60 question touches it).

## Decisions

- 2026-10-27 (step m9-d-enemies-a): drafted for the Act 5 design phase from the roster row and the
  gap analysis; every number is a first draft pending the user's review in round 60. The split's
  drift is 1.5 s (the enemies document's earlier "after 1 s" raised to show the sparks), harmless to
  the ship, paying its cars.
