---
title: Glassfin
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-22
---

# Glassfin

## Summary

A school of transparent Vrell fry that streams up the screen in sinuous ribbons and rams. The act's `tiny` popcorn under water: one hit each, dangerous only in numbers, shredded by spread and area weapons.

## Design

### Stat block

**Draft** (Act 4 design phase, round 48; decided in round 48 (2026-10-22)). A hand-written table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the proposed Act 4 reference DPS (L22 96, then +4 a level to 120 at L28; part A sets it on the extended balance plan). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top, so the fire rates are tuned per level with `levelReport` in the build. The play plane under water is the `air` layer; `sub` is the deep water below it, `ground` the sea floor and the trench walls.

| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (the play plane under water) |
| Size tier | `tiny` |
| Size | 16x16 px, hitbox 10x10 |
| Parts | single |
| Orientation | `16 angles` |
| HP | 1 (easy 1 / hard 1) |
| Armour / shield | none |
| Speed | 170 px/s cruise, 230 px/s in a ram dive |
| Movement | `flock` round a leader point flying the wave's authored route, as the [Mote Swarm](../../air/mote-swarm/README.md) (separation 14 px, alignment, cohesion), with a sway of 14 px at 1.4 Hz across the route; after 3 s on the screen each fry may break off and `dive` at the ship's lane |
| Attack | `none`: contact (`tiny` = 6, the fry is destroyed by the impact) |
| Formations | `swarm` (8-24), `column` (6-10 ribbons) |
| Weak points | none (dies in one hit) |
| Effective traits | `spread`, `area`, `rear` |
| Credits | 2 (score 20 x chain) per fry; a school of 16 pays 32 |
| Death | `tiny` glass shatter: a grey-teal flash and four slow shards, no bubbles |
| First level / used in | L23; L24, L25, L27 as popcorn (not L26: the dark's breather has the Glow Anglers) |
| Difficulty hooks | school size via the global formation lever (base 16, easy 12, hard 22); hard: 40 % of a school dives (easy 15 %) |

### Behaviour

- **A school** (`swarm`): a wave of 8-24 fry on a leader point, like the Mote Swarm's flock but a ribbon, not a cloud: the route is long and S-shaped, the fry string out along it. The same boids rules and caps apply (at most 24 members, fixed iteration order, `StrictMath`).
- **The ram**: a fry that breaks off dives at the ship's lane at 230 px/s without homing, 0.5 s after a 0.3 s glint tell (its glass body flashes teal, the contact hue). Fry that reach the bottom leave; none loops back.
- **Under water**: its speeds are the unit's own and carry the level's bullet factor only for shots, so a school is as fast as it looks while the ship's drag makes it harder to slip: the floor's popcorn that Act 2-3's Skitters were. Seekers (homing shots, torpedoes) ignore it, as the Mote Swarm (`seekers_ignore`, pending the user's review): its job is density, not a lock-on target.
- **Readability**: transparent glass with a teal spine; against the lamp-lit water it shows as a bright thread, in the dark levels the spine glow is the only part drawn, so it reads like a bioluminescent trail.

### Difficulty and level use

- Used by every swimming level to meet the Act 2+ floor and density together with the Barbfin; the level drafts count its fry as enemies (60+ a minute at medium needs about 20 fry a minute plus the others).
- Never fires, so it adds no shots to the floor's 10 a minute: the Barbfins carry those.

## Concept art

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): a tiny school sprite and ribbon motion. Files `glassfin-r48-a/b` in [naval/concept](../concept/), listed in the [naval](../README.md#concept-art) Concept art table. Notes: enters from the top in the motion (spec: streams up).

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen)
- [ ] Behaviour, attack and tells per this spec, deterministic and allocation-free
- [ ] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
- [ ] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec

## Open questions

- Is a `tiny` school enough popcorn, or does Act 4 also want a tiny contact unit that is hit-resistant (kept out: Act 2-3 had none)? Decided by the level drafts.

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (our reading, Q3 a): a new Vrell swimmer, not a re-tinted Skitter; named Glassfin.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
