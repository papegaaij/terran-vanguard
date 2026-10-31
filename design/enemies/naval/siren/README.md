---
title: Siren
design: approved
implementation: in-progress
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-31
---

# Siren

## Summary

A Vrell throat-sac nest grown into the trench walls that sings: it sends out expanding sonar rings that do no damage but slow the ship for 2 s, which is deadly next to anything else on the screen.

## Design

### Stat block

Approved in round 48 (2026-10-22). Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)), from its [data.yaml](data.yaml) (M8 part E, step E3; the table is rendered from it); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the Act 4 reference DPS (L25 108): 0.83 s. The play plane under water is the `air` layer; `sub` is the deep water below it, `ground` the sea floor and the trench walls. The ring is sound, not bullets: its speed is not scaled by the water's × 0.7 (gap check `m8e-gaps.md` §5 E2: the Siren page's numbers, not the Europa page's 140 px/s to 420 px). The data: its `ring` attack with the `slow` block, the `terrain` movement of a nest mounted on a wall's foot by the level's `turret nest` wave (`wall: staggered` or `opposite`, Level 25).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (a wall mount on the play plane; its nest is part of the wall's terrain; gap check §5 E1) |
| Size tier | `medium` |
| Size | 64×56 px, hitbox 48×40 |
| Parts | single (the throat sac) |
| Orientation | `fixed` (it faces the open water) |
| HP | 90 (easy 68 / hard 117, from the global multipliers) |
| Armour / shield | none |
| Speed | 0 px/s (it scrolls with the wall) |
| Movement | `terrain`: a nest on a trench wall's foot (or a cave mouth's), scrolling with the ground; it sings once a pass at the trench's scroll (about 3.9 s on the screen at 140 px/s) |
| Attack | `ring` of sound, no damage: every 4.5 s the throat sac glows teal for 0.8 s (the tell), then an expanding ring (120 px/s to a radius of 230 px, a band 10 px wide) crosses the field once; the first 1.2 s after it enters; a ship (or Rook) it passes through with a clear line from the sac is slowed to 0.6 of its speed and acceleration for 2 s with a sonar shimmer on the hull, then immune to slows for 3 s (round 48 Q5 a; a Dash passes through it; the far wall, the pillar and a cave's rock stop it) |
| Formations | turret nest (1–4) (staggered on both walls, a nest every 1.5 s; or a pair opposite, the second ring 1.5 s later: never two rings within 1.5 s, at most one ring in flight per wall, at most four nests on the screen, never two of one wall within 90 px) |
| Weak points | the glowing throat sac (drawn only) |
| Effective traits | `spread`, `homing` |
| Credits | 28 (score 280 × chain) |
| Death | `medium` flash; the sac collapses in a pale plume and its cave mouth goes dark |
| First level / used in | L25; L26 (its glow is its tell in the dark), L27 |
| Difficulty hooks | easy: slow for 1.5 s, a ring every 6 s; hard: a ring every 3.6 s, 2 s of immunity |
<!-- /data -->

### Behaviour

- **The ring**: an expanding wave, not a bullet: it deals no damage and does not hurt Rook; one hit test per ring against the ship (and Rook, who is slowed the same) at the band, so a ring slows once. The ship's tell is the shimmer plus a lower hull hum; the HUD shows nothing else.
- **Counterplay**: the 0.8 s sac glow tells the time; the ring is slow enough to cross the gap between two rings; shooting the sac (it sits at the wall) ends the song; the slow is 60 % of the speed on top of the water's drag, so a slowed ship can still dodge slow bullets, but not a Nautilus's spiral and a Barbfin's needles at once: that is the combination the roster warns about.
- **Placement**: only on trench walls and cave mouths (the L25 trench); a nest does not scroll onto an open-water level.
- **Under water**: the ring's speed is not scaled by the bullets' x 0.7 (it is sound, not a projectile); this is a draft choice, to be checked with the first drag numbers.

### Difficulty and level use

- L25 is the Siren's level (nests in the trench's side caves, the Nautilus on top); at most one ring in flight per wall.
- Never more than 4 on the screen, and never two of the same cave side within 90 px.

## Concept art

The production frames (round 48's b, the frill bell; `tools/art/siren.py`, step ART-E) are `siren-final-r54-a` with the game capture `siren-capture-r54-a` (a ring slowing the ship, with a video), in the [naval](../README.md#concept-art) Concept art table, for [round 54](../../../concept-rounds/round-54/README.md).

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): wall nest and ring sheet. Files `siren-r48-a/b` in [naval/concept](../concept/), listed in the [naval](../README.md#concept-art) Concept art table. Notes: a looks metallic.

## Implementation

- [x] Stat block loaded from a `data.yaml` (step E3); global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen) (`Level25Test`, `ContentLoaderTest`)
- [x] Behaviour, attack and tells per this spec, deterministic and allocation-free (`SirenTest`, `Level25Test.theSirensSingSlowingRings`, `theNestsKeepTheirPhaseRulesInFlight`)
- [x] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait (`tools/art/siren.py`, `tools/art/intel.py`; `Level25LooksTest.theSirensStepsAndPlace`; round 54)
- [x] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec (`Level25SoundsTest.theSirenAndTheNautilusDieWithTheirOwnSounds`, `theNestsSingSlowTheShipAndTheShellsSpinAndOpen`; round 54's picks)
- [x] The slow as a timed ship status (60 % speed, 2 s, 3 s immunity) on the ship and on Rook, and the sonar shimmer tell (`SirenTest.theRingSlowsTheShipTo60PercentFor2SThen3SOfImmunityEasyAndHardByTheirNumbers`, `rookIsSlowedTooWithHisOwnImmunity`; the shimmer: `Level25LooksTest`)

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (recommended option, decided in round 48 (2026-10-22), Q5 a): a visible ring, 60 % for 2 s, 3 s immunity, no damage; rejected: b (as drafted, no immunity) and c (a weapon jam).
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-23 (step E3, gap check `m8e-gaps.md` §5, accepted in round 54 (2026-10-23)): the stat block as data; the open
  question closes: the nest is `air` (a wall mount on the play plane), counted as a wave (E1); the ring's numbers are this
  page's, 120 px/s to 230 px, not × 0.7 (E2); a ring slows a hull once, at its band, with a clear line from the sac, a Dash
  passing; easy's "40 % for 1.5 s" read as the medium slow (to 60 %) lasting 1.5 s, hard 2 s of immunity (E4).
- 2026-10-23 (steps E1, E5 and ART-E, accepted in round 54 (2026-10-23)): shooting a nest's sac ends its ring
  in flight (`SirenTest.shootingTheSacEndsItsSong`); a Dash passes a ring unslowed (`aDashPassesTheRing`); the debug
  `--invulnerable` still lets a ring slow the ship (it takes no damage, the slow is no hit); Sirens killed count for the
  kill ratio like any wave unit. Drawn: the nest centred 6 px off the wall's foot line into the water, the ring a wrapped
  strip round the sac (its lines stopped by rock, faded over the last 25 % of its 230 px), the slowed hulls' shimmer;
  the sounds are round 54's picks (ring a, slow b, death a).
- 2026-10-23 (user, round 54, closed): the final art accepted (F.3; `art: final`); sounds: the ring a, the slow **b**,
  the death a; calls C.4 (a Dash is not slowed) and C.5 (a kill ends its ring) accepted.
- 2026-10-30, the user: dither audit, ≥ 25 % smoothed (step DITH8): `siren-plume` in smooth steps, not the 4×4 Bayer checker ([audit](../../../art-direction/production/README.md#dither-audit-2026-10-30)).
