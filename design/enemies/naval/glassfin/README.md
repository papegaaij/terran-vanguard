---
title: Glassfin
design: approved
implementation: done
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-31
---

# Glassfin

## Summary

A school of transparent Vrell fry that streams up the screen in sinuous ribbons and rams. The act's `tiny` popcorn under water: one hit each, dangerous only in numbers, shredded by spread and area weapons.

## Design

### Stat block

Approved in round 48 (2026-10-22). Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)), from its [data.yaml](data.yaml) (M8 part C, step C2; the table is rendered from it); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the Act 4 reference DPS (L23 100). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top. The play plane under water is the `air` layer; `sub` is the deep water below it, `ground` the sea floor and the trench walls. The data: the Mote Swarm's flock with a `sway` (14 px, 1.4 Hz: the ribbon) and a `ram` (after 3 s on the screen, a 0.3 s glint, the dive 0.5 s after it at 230 px/s; a school's share 0.15 easy, 0.25 medium, 0.40 hard by its seeded draws).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (the play plane under water) |
| Size tier | `tiny` |
| Size | 16×16 px, hitbox 10×10 |
| Parts | single |
| Orientation | `16 angles` |
| HP | 1 (easy 1 / hard 1, from the global multipliers) |
| Armour / shield | none |
| Speed | 170 px/s (cruise), 230 px/s in a ram dive |
| Movement | `flock` round a leader point flying the wave's authored route, as the [Mote Swarm](../../air/mote-swarm/README.md) (separation 14 px, alignment, cohesion), with a sway of 14 px at 1.4 Hz across the route; after 3 s on the screen each fry may break off and `dive` at the ship's lane: a 0.3 s glint, the dive 0.5 s after it, no homing; fry that reach the bottom leave |
| Attack | `none`: contact (`tiny` = 6, the fry is destroyed by the impact) |
| Formations | swarm (8–24), column (6–10) (school size via the global formation lever: base 16, easy 12, hard 22) |
| Weak points | none (dies in one hit) |
| Effective traits | `spread`, `area`, `rear` |
| Credits | 2 (score 20 × chain) per fry; a school of 16 pays 32 |
| Death | `tiny` glass shatter: a grey-teal flash and four slow shards, no bubbles |
| First level / used in | L23; L24, L25, L27 as popcorn (not L26) |
| Difficulty hooks | school size via the global formation lever (base 16, easy 12, hard 22); the share of a school that rams: easy 15 %, medium 25 %, hard 40 % |
<!-- /data -->

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

- [x] Stat block loaded from a `data.yaml` (step C2: loaded and checked; the flock's sway and ram mapped in step C3; re-measured on Level 23 with `levelReport`, step C3); global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen)
- [x] Behaviour, attack and tells per this spec, deterministic and allocation-free (steps C1 and C3; the tells' look: step C4)
- [x] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
      (step ART-C's `tools/art/glassfin.py` from round 48's a, the grade and the looks step C4: `Level23LooksTest.theArtIsWhatTheLooksDraw`,
      `theGlassfinsSparkleRunsThroughItsFramesAndHolds`, `PortraitsTest`; the finals in round 52)
- [x] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec (the under-water `tiny` burst and
      `glassfin-shatter`: `Level23SoundsTest`; the bounty: `TypicalHaulTest`; round 52's a files until the picks)

## Open questions

- Is a `tiny` school enough popcorn, or does Act 4 also want a tiny contact unit that is hit-resistant (kept out: Act 2-3 had none)? Decided by the level drafts.

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (our reading, Q3 a): a new Vrell swimmer, not a re-tinted Skitter; named Glassfin.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-22 (M8 part C, step C2): the stat block is now its [data.yaml](data.yaml) and the table rendered from it (the Mote Swarm's flock as the template: separation 14, radius 40, cruise 170, at most 24). Data readings, accepted in round 52 (2026-10-22): the medium ram share 0.25 (gap check §5 D6; the stat block gives easy 0.15 and hard 0.40), "the dive 0.5 s after a 0.3 s glint" read as the dive 0.5 s after the glint starts (step C3 reads it as after the glint's end, see below), "streams up the screen" as entering from the top (the Level 23 table's `front`); `seekers_ignore` stands (D7).
- 2026-10-22 (M8 part C, step C3): the ram's timing follows the approved text "0.5 s after a 0.3 s glint tell": the dive 0.5 s after the glint ends (0.8 s from its start), as the simulation flies it (step C1); the data's `delay` now counts from the glint's end. A `column` flies no authored path in the simulation, so Level 23's ribbons are straight columns down the shaft's left or right side (x 144 / 336), each fry with the same ram. Pending the user's review in round 52.
- 2026-10-22 (M8 part C, steps C1, ART-C and C4; accepted in round 52 (2026-10-22)): the final frames from round 48's a
  (`tools/art/glassfin.py`: 16 headings swimming and glinting, the glint's sparkle, the shatter's flash and shards) and the
  intel portrait; a fry's swim frame is offset by a hash of its serial, a ribbon's sway lags π/4 a fish along the column
  (step C1); the shatter's shards draw under the `tiny` death flash (step C4); no glow-only frames yet (Level 26's
  darkness, part F). The sounds: step AUD-C's `glassfin-shatter` a/b over the under-water `tiny` rung, the a files in the
  game until round 52's picks. Captured in Level 23 (`glassfin-capture-r52-a`).
- 2026-10-22 (user, round 52, closed): the final frames accepted (F.3, `art: final`); the shatter **b** picked (in the game since step D4); C.3 (straight columns) and C.5 (the dive 0.5 s after the glint ends, the ram shares) accepted.
- 2026-10-30, the user: dither audit, ≥ 25 % smoothed (step DITH8): `glassfin-glint`, `glassfin-death` in smooth steps, not the 4×4 Bayer checker ([audit](../../../art-direction/production/README.md#dither-audit-2026-10-30)).
