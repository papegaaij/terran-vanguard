---
title: Dart Drone
design: draft
implementation: not-started
art: proposed
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../gilded-gunship, ../../../campaign/act-5-the-belt]
updated: 2026-10-27
---

# Dart Drone

## Summary

A `tiny` black-and-gold Ascendancy drone launched in streams from Gilded Gunships, stations and the
Iron Sovereign: it darts out of its bay, turns toward the ship and rams. One hit kills it. It is the
Ascendancy's popcorn ([Q6](#open-questions) b), so Act 5's mostly human levels still reach the
density floor without breaking the "fewer, tougher units" doctrine: the crewed craft stay few, the
expendable drones carry the count.

## Design

### Stat block

**Draft** (Act 5 design phase, new unit of the gap analysis's Q6 b, pending the user's review in round
60). A hand-written table: there is no `data.yaml` yet, the build turns it into one. Numbers at
**medium** (see the [balancing basis](../../README.md#balancing-basis)). As a `tiny` unit it stays
one-shot fodder in every level (no act or beam-era factor).

| Field | Value |
|---|---|
| Faction | Jovian Ascendancy; a **machine** (stunned 3 s by the [EMP Burst](../../../player/specials/README.md#emp-burst-l15): it coasts on, lights out) |
| Layer | `air` |
| Size tier | `tiny` |
| Size | 20×20 px, hitbox 12×12 |
| Parts | single |
| Orientation | `16 angles` |
| HP | 1 (easy 1 / hard 1) |
| Armour / shield | none (too small for a shield emitter) |
| Speed | 240 px/s |
| Movement | **launched** (`spawn` by its carrier): 0.4 s `straight` along the launch direction out of the bay, then `chase` with a low turn rate (60°/s) for 3 s, then `straight` on along its heading and out through the nearest edge; as a wave of its own: a `stream` from the top or the sides |
| Attack | `none`: contact `tiny` = 6 (destroyed by the impact) |
| Formations | `stream` (launched in streams of 2–5, 0.25 s apart), `swarm`-like clouds from the Sovereign's bays (its document) |
| Weak points | none |
| Effective traits | `spread`, `area`, `forward` |
| Credits | 2 (score 20 × chain), launched or not |
| Death | `tiny` spark puff with gold flecks |
| First level / used in | L30 (the Gilded Gunships' bays); L29's turned station and L35's Sovereign may launch them (the level and boss drafts decide) |
| Difficulty hooks | streams sized by the global formation lever (easy −1, hard +1 per stream); hard: turn rate 80°/s |

### Behaviour

- **Counts as a unit.** A launched Dart counts toward the level's enemies per minute and the Act 2+
  floor like any wave unit (launched units count), so a Gunship's streams carry a wave's density.
- **Readable at speed.** A slim black dart with a gold nose and one red running light (the Ascendancy
  accent of readability rule 4); the low turn rate makes a stream sweep past rather than track: the
  player sidesteps a stream as with Skitters.
- **No shield, no leader.** Darts are expendable machines: no hex shield, no squad leader, no scatter.
- **Sounds** (brief for the audio step): a short pneumatic launch thump per Dart from the bay (real
  pneumatics from Freesound, CC0/CC-BY), a thin high whine in flight, a tiny metallic crack on death;
  quiet enough that a stream does not drown the music.

### Difficulty and level use

- At most 16 Darts on the screen (all carriers together).

## Concept art

None yet: round 60's unit proposals (the gap analysis's wave 5) draw the Dart Drone, in the belt look
of [Q11](../../README.md#open-questions).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (pending the user's review), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 30 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/dart-drone-r60-a.png](concept/dart-drone-r60-a.png) | Dart Drone A, needle dart — a slim black needle, a gold nose cone, small swept gold fins, one red light; strong rim (sheet) | proposed |
| [concept/dart-drone-r60-a.gif](concept/dart-drone-r60-a.gif) | Dart Drone A, needle dart — a slim black needle, a gold nose cone, small swept gold fins, one red light; strong rim (motion) | proposed |
| [concept/dart-drone-r60-b.png](concept/dart-drone-r60-b.png) | Dart Drone B, chevron dart — a black arrowhead with gold leading edges and a red light in the notch (sheet) | proposed |
| [concept/dart-drone-r60-b.gif](concept/dart-drone-r60-b.gif) | Dart Drone B, chevron dart — a black arrowhead with gold leading edges and a red light in the notch (motion) | proposed |

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied
- [ ] Launch from a carrier's bays (`spawn`), the low-turn chase and the run-out, counted as units by the density measures (`levelReport`)
- [ ] Production sprites (16 angles) via `tools/art/`; death effect, bounty and sounds (Freesound, CC0/CC-BY)

## Open questions

- **Q6** (round 60, popcorn in a human war): **b** recommended, this unit; **a** no Dart Drone, the
  Vrell units carry the count (L30, L32 and L35 fall short of the floor); **c** Talons in great
  numbers (against the doctrine, and a shielded popcorn is tedious). Under a or c the unit is
  dropped.
- Its name ("Dart Drone") is a working name, pending round 60.

## Decisions

- 2026-10-27 (step m9-d-enemies-a): drafted for the Act 5 design phase as the gap analysis's Q6 b;
  every number is a first draft pending the user's review in round 60.
