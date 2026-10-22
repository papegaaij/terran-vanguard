---
title: Siren
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-22
---

# Siren

## Summary

A Vrell throat-sac nest grown into the trench walls that sings: it sends out expanding sonar rings that do no damage but slow the ship for 2 s, which is deadly next to anything else on the screen.

## Design

### Stat block

**Draft** (Act 4 design phase, round 48; decided in round 48 (2026-10-22)). A hand-written table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the proposed Act 4 reference DPS (L22 96, then +4 a level to 120 at L28; part A sets it on the extended balance plan). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top, so the fire rates are tuned per level with `levelReport` in the build. The play plane under water is the `air` layer; `sub` is the deep water below it, `ground` the sea floor and the trench walls.

| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (a wall mount on the play plane; its nest is part of the wall's terrain) |
| Size tier | `medium` |
| Size | 64x56 px, hitbox 48x40 |
| Parts | single (the throat sac) |
| Orientation | `fixed` (it faces the open water) |
| HP | 90 (easy 68 / hard 117, from the global multipliers): 0.83 s at the L25 reference of 108 |
| Armour / shield | none |
| Speed | 0 px/s (scrolls with the wall) |
| Movement | `terrain`: a nest on a trench wall or a cave mouth |
| Attack | `ring` of sound, no damage: every 4.5 s the throat sac glows teal for 0.8 s (the tell), then an expanding ring (120 px/s to a radius of 230 px, a band 10 px wide) crosses the field once; a ship it passes through is slowed to 60 % speed for 2 s with a sonar shimmer on the hull and is then immune to slows for 3 s (round 48 Q5 a; the ring is dodged like a bullet ring: slipping through its band is the hit, so the safe place is outside the radius or behind the wall) |
| Formations | `turret nest` (2-4, staggered on both walls; never two rings arriving together: their phases are offset by at least 1.5 s) |
| Weak points | the glowing throat sac (drawn only) |
| Effective traits | `spread`, `homing` (not `beam`: the sac sits in a cave mouth) |
| Credits | 28 (score 280 x chain) |
| Death | `medium` flash; the sac collapses in a pale plume and its cave mouth goes dark |
| First level / used in | L25; L26 (its glow is its tell in the dark), L27 |
| Difficulty hooks | easy: slow 40 % for 1.5 s, ring every 6 s; hard: ring every 3.6 s, 2 s of immunity |

### Behaviour

- **The ring**: an expanding wave, not a bullet: it deals no damage and does not hurt Rook; one hit test per ring against the ship (and Rook, who is slowed the same) at the band, so a ring slows once. The ship's tell is the shimmer plus a lower hull hum; the HUD shows nothing else.
- **Counterplay**: the 0.8 s sac glow tells the time; the ring is slow enough to cross the gap between two rings; shooting the sac (it sits at the wall) ends the song; the slow is 60 % of the speed on top of the water's drag, so a slowed ship can still dodge slow bullets, but not a Nautilus's spiral and a Barbfin's needles at once: that is the combination the roster warns about.
- **Placement**: only on trench walls and cave mouths (the L25 trench); a nest does not scroll onto an open-water level.
- **Under water**: the ring's speed is not scaled by the bullets' x 0.7 (it is sound, not a projectile); this is a draft choice, to be checked with the first drag numbers.

### Difficulty and level use

- L25 is the Siren's level (nests in the trench's side caves, the Nautilus on top); at most one ring in flight per wall.
- Never more than 4 on the screen, and never two of the same cave side within 90 px.

## Concept art

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): wall nest and ring sheet. Files `siren-r48-a/b` in [naval/concept](../concept/), listed in the [naval](../README.md#concept-art) Concept art table. Notes: a looks metallic.

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen)
- [ ] Behaviour, attack and tells per this spec, deterministic and allocation-free
- [ ] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
- [ ] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec
- [ ] The slow as a timed ship status (60 % speed, 2 s, 3 s immunity) on the ship and on Rook, and the sonar shimmer tell

## Open questions

- Layer of a wall nest: `air` (our reading) or a `ground` target with the `anti-ground` bonus: decided with the under-water rules in the system docs.

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (recommended option, decided in round 48 (2026-10-22), Q5 a): a visible ring, 60 % for 2 s, 3 s immunity, no damage; rejected: b (as drafted, no immunity) and c (a weapon jam).
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
