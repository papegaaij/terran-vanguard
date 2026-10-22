---
title: Drill Rig
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-22
---

# Drill Rig

## Summary

A Vrell ground structure on Europa's ice shelf: a drill collar boring toward the ocean with four vents (pumps) round it. The act opener's hardened target: the wing bombs and missiles it before it drills through.

## Design

### Stat block

**Draft** (Act 4 design phase, round 48; decided in round 48 (2026-10-22)). A hand-written table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the proposed Act 4 reference DPS (L22 96, then +4 a level to 120 at L28; part A sets it on the extended balance plan). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top, so the fire rates are tuned per level with `levelReport` in the build. The play plane under water is the `air` layer; `sub` is the deep water below it, `ground` the sea floor and the trench walls.

| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `ground` (hardened: only `anti-ground` weapons, the Airstrike and the Smart Bomb damage it) |
| Size tier | `large` (a ground structure; not a set piece) |
| Size | core 120x120 px, hitbox 96x96; pump 32x32 (hitbox 24x24) |
| Parts | `vital` core, 4 `destroyable` pumps |
| Orientation | `fixed` (the drill collar spins decoratively) |
| HP | core 240 / pump 24 each; whole 336 (easy 252 / hard 437, from the global multipliers): the core's 2.5 s at the L22 reference of 96 |
| Armour / shield | hardened: other shots glance off with a spark; homing shots and Rook do not pick it unless their weapon is `anti-ground` |
| Speed | 0 px/s (scrolls with the ice) |
| Movement | `terrain` |
| Attack | the collar fires a `ring` of 10 lime ice shards (110 px/s base, `medium` = 6) every 3.5 s while the pumps live; killing the pumps one by one slows it by 0.5 s each; hard: a second `aimed` shard each time |
| Formations | a `set` of 2-4 rigs along the shelf, with 3-6 Spine Turrets round them |
| Weak points | the lime bore core (the vital part; drawn only) |
| Effective traits | `anti-ground`, `area` |
| Credits | 56 (score 560 x chain); core 40, pumps 4 each; the level's objective counts the cores |
| Death | `large` ground explosion; the collar stops, the ice cracks and a steam plume rises (a geyser scenery, not a hazard) |
| First level / used in | L22 (the objective); not under water (it sits on the surface) |
| Difficulty hooks | easy: shards every 4.5 s; hard: the extra aimed shard |

### Behaviour

- **L22's objective**: Aegis Wing destroys the Vrell drill rigs on the surface before diving (the level doc owns the count and the primary/secondary structure). The cores are the target; the pumps are optional bounty and slow the collar.
- **Hardened**: like the [Hive Node](../../ground/hive-node/README.md): a standard loadout glances off, the player needs an `anti-ground` weapon (bombs, mortar, missiles), the layer rules' hardened case.
- **Ice and geysers**: a destroyed rig leaves a steaming crack; the cracking-ice geysers of the level are the hazard (tell: a crack, then a burst), separate from the rig; the level doc owns them.
- **Surface**: above water, no water factor and no drag (L22 is on the ice).

### Difficulty and level use

- L22 only (the act opener and a breather): 2-4 rigs along the shelf, the nests of Spine Turrets are the level's fire; the objective counts the cores.
- Not used below the ice: the descent shaft's rig is scenery.

## Concept art

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): a bone bore tower, b rust auger flower, files `drill-rig-r48-a/b` in [ground/concept](../concept/), listed in the [ground](../README.md#concept-art) Concept art table. Notes: a looks tyre-like, b's augers are faint.

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen)
- [ ] Behaviour, attack and tells per this spec, deterministic and allocation-free
- [ ] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
- [ ] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec
- [ ] A hardened large ground structure with a vital core and four pumps (the Hive Node's rules)

## Open questions

- Does the rig need its own proposal in round 48 (a/b), or is it drawn in the ice backdrop's round (part B)? Decided by the main agent.

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (decided by me, gap analysis section 2.6): drill rigs are hardened ground targets with a vital core, as the Hive Node.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
