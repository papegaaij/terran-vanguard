---
title: Barbfin
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-22
---

# Barbfin

## Summary

A small Vrell swimmer with a barbed snout that darts in, holds and spits aimed needles: the Needler's role under water. It is also the escort of the Abyss Ray.

## Design

### Stat block

**Draft** (Act 4 design phase, round 48; decided in round 48 (2026-10-22)). A hand-written table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the proposed Act 4 reference DPS (L22 96, then +4 a level to 120 at L28; part A sets it on the extended balance plan). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top, so the fire rates are tuned per level with `levelReport` in the build. The play plane under water is the `air` layer; `sub` is the deep water below it, `ground` the sea floor and the trench walls.

| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (the play plane under water) |
| Size tier | `small` |
| Size | 36x28 px, hitbox 26x20 |
| Parts | single |
| Orientation | `32 angles` (it turns toward the ship while it holds) |
| HP | 12 (easy 9 / hard 16, from the global multipliers); the beam-era factor x 2 applies from the level number, so 24 at L24 (pending the re-measure under the beam's halved reach) |
| Armour / shield | none |
| Speed | 120 px/s in, stops at its hold point |
| Movement | `dive` in from a side or top edge along a short curve, stops 90-200 px above the ship's lane and holds with a 6 px/s drift, then leaves toward the nearest side after 9 s |
| Attack | `aimed` needle (a violet-hued shot) every 2.4 s, 150 px/s base, `small` = 4; first shot 0.9 s after it stops; hard: a 2-needle `burst` 0.12 s apart |
| Formations | `column` (3-5), `circle` (4, as the Abyss Ray's escort), scattered pairs |
| Weak points | violet snout barb (drawn only) |
| Effective traits | `spread`, `homing` |
| Credits | 12 (score 120 x chain) |
| Death | `small` flash with a sinking shell and a short ink-grey cloud |
| First level / used in | L24; L25, L27 and the Maw's L28 escorts |
| Difficulty hooks | easy: shot every 3 s; hard: 2-needle burst, 2.1 s |

### Behaviour

- **Needler pattern**: as the [Needler](../../air/needler/README.md): enters, stops, fires on its own seeded cadence, leaves; the holders are what lets Rook and the ship pick them off one by one.
- **Fire budget**: the Act 2+ floor needs 10 enemy shots a minute at medium; Barbfin groups are the floor's shot source in the swimming levels (a group of 4 fires about 100 shots in 60 s). Bullets x 0.7 live longer on the screen, so the per-level rates are set with `levelReport`.
- **Escort of the Abyss Ray**: a Ray arrives with four Barbfins in `circle` that peel off its shadow as it rises; they keep their own cadence after the Ray leaves.
- **Under water**: only its needles are slowed; the unit's own movement is not.

### Difficulty and level use

- L24 introduces it (with the Abyss Ray); a school-plus-Barbfin wave is the L24-L27 workhorse.
- Beam-era HP applies by level number (`small`); the reduction of the beam's reach is re-measured in part A (`BeamEraHpTest`'s 0.9-1.5x rule).

## Concept art

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): sprite sheet and motion. Files `barbfin-r48-a/b` in [naval/concept](../concept/), listed in the [naval](../README.md#concept-art) Concept art table. Notes: the needles are yellow in the mockup (spec: violet).

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen)
- [ ] Behaviour, attack and tells per this spec, deterministic and allocation-free
- [ ] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
- [ ] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec

## Open questions

- Does the Barbfin's HP take `small_act_hp` (capped x3 as the Needler) on top of the beam era? Decided by the build's measure (default: no, it is a new unit already set at L24).

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (our reading, Q3 a): the small firing swimmer, named Barbfin; first appears at L24 as the plan's part D asks.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
