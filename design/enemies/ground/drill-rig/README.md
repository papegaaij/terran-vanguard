---
title: Drill Rig
design: approved
implementation: done
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-22
---

# Drill Rig

## Summary

A Vrell ground structure on Europa's ice shelf: a drill collar boring toward the ocean with four vents (pumps) round it. The act opener's hardened target: the wing bombs it, or calls an Airstrike on it, before it drills through.

## Design

### Stat block

Approved in round 48 (2026-10-22). Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)),
from its [data.yaml](data.yaml) (M8 part B, step B2; the table is rendered from it); HP is in damage units (a Pulse
Cannon L1 shot does 2), set at the unit's first level from the Act 4 reference DPS (L22 96). Bullet speeds are base
values (the rig sits on the surface: no water factor). The pumps carry the collar's ring (`attack: ring`): each pump
lost adds its `slows` 0.5 s to the ring's interval, and the ring stops with the last pump; the hard hook adds one
aimed shard to each ring (`aimed: 1`). The whole 246 HP is 2.6 s at the reference, inside the large class's 1–3 s
(step B3b: the core 150, down from 240, so one Airstrike's blasts break it on every difficulty, hard's 195 included).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `ground` (hardened: only `anti-ground` weapons, the Airstrike and the Smart Bomb damage it) |
| Size tier | `large` |
| Size | 120×120 px, hitbox 96×96 (the core); pump 32×32 (hitbox 24×24) at the collar's corners |
| Parts | `vital` core, 4 `destroyable` pumps (they carry the ring) |
| Orientation | `fixed` (the drill collar spins decoratively) |
| HP | core 150 · pump 24 each (2.6 s at the L22 reference of 96; one Airstrike breaks the core on every difficulty); whole 246 (easy 184 / hard 320, from the global multipliers) |
| Armour / shield | hardened: other shots glance off with a spark; homing shots and Rook do not pick it unless their weapon is `anti-ground` |
| Speed | 0 px/s (scrolls with the ice) |
| Movement | `terrain` |
| Attack | the collar fires a `ring` of 10 lime ice shards (110 px/s, `medium` = 6) every 3.5 s while the pumps live; each pump lost slows it by 0.5 s (3.5 / 4.0 / 4.5 / 5.0 s), none with all four down |
| Formations | single (a set of 2-4 rigs along the shelf, with 3-6 Spine Turrets round them) |
| Weak points | the lime bore core (the vital part; drawn only) |
| Effective traits | `anti-ground`, `area` |
| Credits | 56 (score 560 × chain); core 40, pumps 4 each; whole (core first) 56; the level's objective counts the cores |
| Death | `large` ground explosion; the collar stops, the ice cracks and a steam plume rises (a geyser scenery, not a hazard) |
| First level / used in | L22; the objective; not under water (it sits on the surface) |
| Difficulty hooks | easy: shards every 4.5 s; hard: a second `aimed` shard each time |
<!-- /data -->

### Behaviour

- **L22's objective**: Aegis Wing destroys the Vrell drill rigs on the surface before diving (the level doc owns the count and the primary/secondary structure). The cores are the target; the pumps are optional bounty and slow the collar.
- **Hardened**: like the [Hive Node](../../ground/hive-node/README.md): a standard loadout glances off, the player needs an `anti-ground` weapon (the Bomb Rack, the Hammer Mortar) or a special (the Airstrike, the Smart Bomb), the layer rules' hardened case. Micro-missiles are not `anti-ground` and glance off too.
- **Ice and geysers**: a destroyed rig leaves a steaming crack; the cracking-ice geysers of the level are the hazard (tell: a crack, then a burst), separate from the rig; the level doc owns them.
- **Surface**: above water, no water factor and no drag (L22 is on the ice).

### Difficulty and level use

- L22 only (the act opener and a breather): 2-4 rigs along the shelf, the nests of Spine Turrets are the level's fire; the objective counts the cores.
- Not used below the ice: the descent shaft's rig is scenery.

## Concept art

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): a bone bore tower, b rust auger flower, files `drill-rig-r48-a/b` in [ground/concept](../concept/), listed in the [ground](../README.md#concept-art) Concept art table. Notes: a looks tyre-like, b's augers are faint.

## Implementation

- [x] Stat block loaded from a `data.yaml` (done in step B2); global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen)
- [x] Behaviour, attack and tells per this spec, deterministic and allocation-free (`DrillRigTest`, `Level22Test`'s
      `theRigsCarryTheirPumpsAndTheirRing`, `steppingAWholeLevelDoesNotAllocate` and `theSameSeedAndCommandsGiveTheSameState`)
- [x] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
      (`tools/art/drill_rig.py`, one look from round 48's pick b, accept/redo in [round 51](../../../concept-rounds/round-51/README.md);
      no in-water grade: the rig stands on the surface; `Level22LooksTest.theRigsArtIsWhatItsLooksDraw`, `PortraitsTest`)
- [x] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec (`DrillRigTest`, `Level22SoundsTest.theGeysersAndTheRigsSound`;
      the sounds' a/b in round 51)
- [x] A hardened large ground structure with a vital core and four pumps (the Hive Node's rules; steps B1 and B3)

## Open questions

- Does the rig need its own proposal in round 48 (a/b), or is it drawn in the ice backdrop's round (part B)? Decided by the main agent.

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (decided by me, gap analysis section 2.6): drill rigs are hardened ground targets with a vital core, as the Hive Node.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-22 (M8 part B, step B2; defaults of the gap check, pending the user's review in round 51): the stat block in
  a `data.yaml`; the pumps (at step ART-B's pivot offsets ±62, ±58) carry the ring and slow it 0.5 s each; the core's
  `first_bonus` 16 (a core-first kill pays the whole 56, as Level 22's credit table counts); the shards use the existing
  lime `medium` bullet look; the pumps fire nothing of their own.
- 2026-10-22 (M8 part B, step B3): mapped for the simulation: the four pumps as unit parts that carry the ring and slow
  it 0.5 s each once lost, the core's `first_bonus` 16 shared as 4 a pump (a core-first kill pays the whole 56), hard's
  aimed shard (`Level22Test`). At hard the core's 312 HP is past one Airstrike's cap of 300, so without an
  `anti-ground` gun a hard run cannot break all four rigs (pending the user's review in round 51).
- 2026-10-22 (M8 part B, step B3b; pending the user's review in round 51): the core **240 → 150 HP** (pumps 24 kept;
  whole 336 → 246, **3.5 → 2.6 s** at the L22 reference of 96, inside the large class's 1–3 s, so `BalanceTest`'s
  accepted deviation is gone). The balance plan's fit has no `anti-ground` gun: its four Airstrike charges must break
  the rigs, and a strike deals at most 300 to one target (blasts of 100). At 240 the hard core's 312 was past the cap
  and a strike that landed only two blasts left a medium core standing; at 150 (easy 113, hard 195) two blasts break
  it. Measured with `levelReport` (Level 22, the autopilot calling the strike once a rig is in the lower half of the
  screen): RIGS met easy 3/3, medium 3/3, hard 3/3 (before: 3/3, 2/3, 0/3), every rig struck about 7 s after it
  enters (cores at t≈47, 63, 79, 95). The pass time and the rig field's scroll (60 px/s, about 11 s on the screen) are
  unchanged (the backdrop's leads are cut along it). A Bomb Rack L3 alone (about 27 DPS, no Airstrike) still breaks
  four pumps and no core in the autopilot's run (`Level22Test`). The hardened text corrected: missiles do not get through.
- 2026-10-22 (M8 part B, steps B1, ART-B and B4; pending the user's review in round 51): the pumps are unit parts carrying the collar's ring, each lost
  pump slowing the gun (`UnitPart.slowsGunSeconds`), hard's aimed shard added to the ring (`EnemyGun.withAimedShards`); the
  core's first bonus is split per pump (`first_bonus` 16 = 4 a standing pump, so a core-first kill pays the whole 56);
  a blast reaches a pump only through the core's hitbox; the shards use the lime `medium` bullet look (no new bullet
  art), the pumps fire nothing of their own. The production frames (`tools/art/drill_rig.py`, round 48's pick b with
  stronger augers) drawn by `DrillRigLooks`: the muzzle gathers over the tell, each ring foreseen for it
  (`Level22LooksTest`); the game capture `drill-rig-capture-r51-a` in the [ground](../README.md#concept-art) table.
