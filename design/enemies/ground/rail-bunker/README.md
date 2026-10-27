---
title: Rail Bunker
design: draft
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials]
updated: 2026-10-27
---

# Rail Bunker

## Summary

A **hardened** Ascendancy gun bunker on asteroid surfaces and station hulls. Its rail turret locks a thin red
line through the ship's position, charges for a second and fires a screen-long rail shot along it: a `heavy`
hit, dodged by leaving the line. Only `anti-ground` weapons and specials break it.

## Design

### Stat block

**Draft** (Act 5 design phase, step D-enemies-B; pending the user's review in round 60). A hand-written
table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2),
set at its first level from the proposed Act 5 reference DPS (judgement call J2: L33 136): 100 HP is 0.7 s
at the reference, but only an `anti-ground` weapon or a special reaches it. Fire rates are tuned per level
with `levelReport`.

| Field | Value |
|---|---|
| Faction | Ascendancy; a **machine** (stunned 3 s by the EMP Burst) |
| Layer | `ground` (hardened: only `anti-ground` weapons, the Airstrike, the Orbital Lance and the Smart Bomb damage it) |
| Size tier | `medium` |
| Size | 72×72 px (the chosen r04 concept's octagon), hitbox 56×56 |
| Parts | single |
| Orientation | `fixed` bunker; the rail turret `32 angles`, turning 90°/s toward the ship |
| HP | 100 (easy 75 / hard 130, from the global multipliers) |
| Armour / shield | hardened: other shots glance off with a spark; homing shots and Rook do not pick it unless their weapon is `anti-ground` |
| Speed | 0 px/s (scrolls with the rock or hull) |
| Movement | `terrain` |
| Attack | `laser-line` (the rail): at the telegraph's start the turret locks a line from its muzzle through the ship's position to the screen edge and draws it as a thin red line, brightening, for **1.0 s** (the coils glow gold); then the rail fires along it, 10 px drawn, an 8 px hit band, live 0.2 s, **`heavy` 10** once; every **4.5 s**, telegraph to telegraph; it holds fire while the ship is within 100 px; any direction, up the screen too |
| Formations | turret nest (single, or 2–3 along a rock face whose lines cross) |
| Weak points | none (the gold coils glow during the charge, drawn only) |
| Effective traits | `anti-ground` (and the specials) |
| Credits | 45 (score 450 × chain) |
| Death | `medium` ground explosion, the rail thrown; the cracked bunker shell stays as a wreck |
| First level / used in | L33 (the asteroid surfaces of the Leech Field, judgement call J3); L35's approach on the Sovereign's outer hull; the Iron Sovereign's rail batteries share its line (the boss doc) |
| Difficulty hooks | easy: telegraph 1.2 s, every 6 s; hard: telegraph 0.8 s (the vocabulary's minimum), every 3.6 s |

### Behaviour

- **Lock, not track.** The line is fixed when the telegraph starts: a sidestep out of the 8 px band during
  the second is always enough. Its warning is the bright red line (crimson is the lasers' hue; the line
  stays thin until it fires), so it reads at speed.
- **Hardened.** As the [Hive Node](../hive-node/README.md) and the Drill Rig: a standard loadout glances off,
  the player needs an `anti-ground` weapon (the Bomb Rack, the Hammer Mortar) or a special. A bunker is a
  threat to dodge for a loadout without one; the Strider's topple crushes one under its fall line (see
  [Strider](../strider/README.md)) and so do the belt's mining charges.
- **Enemy fire never hurts enemies**: the rail passes through other units.
- **EMP.** Stunned 3 s: a running telegraph is cancelled, no shot.
- **The mass-relay flash** of L35 is a separate screen-scale `laser-line` hazard (the level doc), not a bunker.
- **Code fit.** The telegraphed full-screen line of the `Sled` and the `laser-sweep`'s telegraph code
  (gap analysis §2.8: `laser-line`, size S).

### Difficulty and level use

- L33: on the large asteroid surfaces (the level's ground layer, so the first level without ground is L30 and
  the Bomb Rack's `NO GROUND` moves there, J3); never more than 2 lines telegraphed at once, never two lines
  that leave no safe column between them.
- L35's approach: on the outer hull before the arena.

### Sounds

Brief for the round-60 sound proposals (Freesound CC0/CC-BY, real recordings, no bubbly or popping
sounds): the charge a rising capacitor whine (a camera flash charging, pitched down) over a low hum; the
shot a violent electric crack with a short metallic ring (a large spark gap or a whip crack layered with a
heavy gunshot); the death a heavy metal explosion.

## Concept art

Chosen concept from [round 03](../../../concept-rounds/round-03/README.md) and the round-04 colour pass,
listed in the [ground](../README.md#concept-art) Concept art table: an octagonal hardened bunker with a
twin-rail cannon and gold coils, black and gold with a gunmetal accent and rim light
([rail-bunker-r04-a.png](../concept/rail-bunker-r04-a.png), chosen; r03 superseded). Prompts:
[../concept/prompts.md](../concept/prompts.md). A round-60 readability check against the belt's rock (Q11)
and the production frames (the turret at 32 angles, the charge glow, the wreck) follow in the build.

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (pending the user's review), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 33 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/rail-bunker-r60-a.png](concept/rail-bunker-r60-a.png) | Rail Bunker A, solid lock line — a thin red line from the muzzle through the ship to the edge, brightening 1 → 2 px over 1.0 s while the coils glow gold; the rail shot (sheet) | proposed |
| [concept/rail-bunker-r60-a.gif](concept/rail-bunker-r60-a.gif) | Rail Bunker A, solid lock line — a thin red line from the muzzle through the ship to the edge, brightening 1 → 2 px over 1.0 s while the coils glow gold; the rail shot (motion) | proposed |
| [concept/rail-bunker-r60-b.png](concept/rail-bunker-r60-b.png) | Rail Bunker B, dashed line with running chevrons — a dashed red line with gold chevrons running out along it, faster as the charge builds, solid for the last 0.2 s (sheet) | proposed |
| [concept/rail-bunker-r60-b.gif](concept/rail-bunker-r60-b.gif) | Rail Bunker B, dashed line with running chevrons — a dashed red line with gold chevrons running out along it, faster as the charge builds, solid for the last 0.2 s (motion) | proposed |

## Implementation

- [ ] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers applied; tuned with `levelReport`
- [ ] `laser-line`: the locked line, the brightening telegraph (≥ 0.8 s), the rail's band, `heavy` 10 once, any direction
- [ ] Hardened: glance, the specials' damage, homing and Rook skip it without `anti-ground`
- [ ] Production sprites from the chosen r04 concept (the turret at 32 angles, the coil glow, the wreck), the intel portrait
- [ ] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds)

## Open questions

- J3 (shown in round 60): L33 gets ground (the bunkers need a surface), so `NO GROUND` moves to L30.
- Q11 (pending round 60): the Ascendancy look on the belt, **a** recommended (the r04 concept already is it);
  alternatives **b** gunmetal with gold, **c** black silhouettes with gold accents only.

## Decisions

- 2026-09-30: concept round 03 drew the Rail Bunker; the round-04 colour pass (gunmetal accent) chosen.
- 2026-10-27 (step D-enemies-B): promoted from the [ground roster](../README.md) to a draft spec for the Act 5
  design phase; every number is a first draft, pending the user's review in round 60.
- 2026-10-27 (decided by me, trivia): the rail aims at the ship (the roster's "along the player's column" read
  as through the ship's position, so it works up the screen too), damage `heavy` 10 (the basis's rail shots);
  the specials' own rules already let the Airstrike, the Smart Bomb and the Orbital Lance damage hardened targets.
