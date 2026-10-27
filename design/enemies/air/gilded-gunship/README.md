---
title: Gilded Gunship
design: draft
implementation: not-started
art: proposed
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../story/factions/jovian-ascendancy, ../../../campaign/act-5-the-belt]
updated: 2026-10-27
---

# Gilded Gunship

## Summary

A slow, heavy Ascendancy gunship with gilded frontal armour: shots from the front do half damage,
shots into its two exposed engines at the rear do double. It fires `ring` bursts and launches streams
of [Dart Drones](../dart-drone/README.md) from its belly bays, so it carries a wave's count as well as
its shots (Level 30).

## Design

### Stat block

**Draft** (Act 5 design phase, pending the user's review in round 60). A hand-written table: there is
no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does
2), set at its first level from the proposed Act 5 reference DPS (L30 = 124). Fire rates are first
values: the build tunes them per level with `levelReport`.

| Field | Value |
|---|---|
| Faction | Jovian Ascendancy; a **machine** (stunned 3 s by the [EMP Burst](../../../player/specials/README.md#emp-burst-l15)) |
| Layer | `air` |
| Size tier | `medium` |
| Size | 88×100 px, hitbox 64×76 |
| Parts | single (the arcs below are directional damage on one hull, not parts) |
| Orientation | `fixed` (always faces down; the arcs are measured from its facing) |
| HP | 88 (easy 66 / hard 114, from the global multipliers): with the shield, 1.5 s from straight in front, 0.8 s from the sides, 0.45 s into the engines at the L30 reference of 124 |
| Armour / shield | hex [shield](../../README.md#enemy-shields) **12**, spent first at full damage; then **frontal armour**: a direct shot arriving within ±45° of its facing does ×0.5; **engines**: one arriving within ±60° of its rear does ×2; dropped bombs, lobbed shells and the Airstrike come from above and take neither factor |
| Speed | 45 px/s descent, a 20 px `sine` sway |
| Movement | `straight` down through the whole screen with the sway (≈ 13 s on the screen); leaves through the bottom edge; never stops |
| Attack | `ring` of 12 every 3.0 s (slow 110 px/s, `small` = 4), its gold ring lamps lighting 0.4 s before (the tell); `spawn`: a stream of 3 [Dart Drones](../dart-drone/README.md) out of the belly bays every 5 s, 0.25 s apart, launched forward and to the sides, at most 6 alive per Gunship; contact `medium` = 15 |
| Formations | `column` (2–3, 2 s apart), `carrier + escorts` (Talons in `orbit` at 90 px), the L30 convoy (a level secondary) |
| Weak points | the two engine nacelles at the rear, glowing white-gold (the ×2 arc) |
| Effective traits | `rear`, `homing`, `side`, `shield-breaker` (empties the 12) |
| Credits | 28 (score 280 × chain); its launched Darts pay their own |
| Death | `medium` explosion, the hull breaks in two behind the armour plate, gold plates spin off; the bays stop launching at once |
| First level / used in | L30 (the convoy secondary); its later Act 5 levels (e.g. the Sovereign's escort screen in L35) per the level drafts |
| Difficulty hooks | easy: ring of 10 every 3.6 s, streams of 2; hard: ring of 14 every 2.6 s, streams of 4 |

### Behaviour

- **Reading the arcs.** The armour plate is a bright gilded shield-shape across the nose: front shots
  flash grey-gold and spark (the multi-part rule's "armoured flashes grey" at half strength). The
  engines glow white-gold at the rear and flash white when hit. A ship that flies past it and fires
  back with the rear gun or the wing mounts kills it fastest; homing missiles count by the direction
  they arrive from, so they curl round and hit the engines.
- **Frontal armour as built.** The arc test reuses the Scuttler's frontal armour (the direction of
  arrival against the facing, ±45°); the Gunship's arc halves the damage instead of glancing, so a
  front-only loadout still kills it in about twice the time.
- **Darts.** The Dart streams make the Gunship the Ascendancy's popcorn source ([Q6](#open-questions)
  b): its launched units count toward the level's density. A Gunship killed early launches nothing
  more.
- **Sounds** (brief for the audio step): a deep turbine drone while on the screen, a heavy metallic
  clank of the bay doors per Dart stream, a hollow cannon thump for the ring; hits on the armour ring
  as a dull bell-like clang, hits on the engines as a sharp sputter.

### Difficulty and level use

- At most two Gunships on the screen; a Gunship is a wave's "problem" enemy among Talons and Darts.

## Concept art

The chosen faction-contrast look of round 04,
[gilded-gunship-r04-a.png](../concept/gilded-gunship-r04-a.png) (black and gold with a white accent
and the rim light), is listed in the [air](../README.md#concept-art) Concept art table. Round 60's unit
proposals add the armour plate, the engine glow, the belly bays and the belt readability of
[Q11](../../README.md#open-questions).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (pending the user's review), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 30 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/gilded-gunship-r60-a.png](concept/gilded-gunship-r60-a.png) | Gilded Gunship A, gilded chevron — the r04 gold chevron plate across the nose, white-gold engines, twelve ring lamps, flank bay doors; strong rim (sheet) | proposed |
| [concept/gilded-gunship-r60-a.gif](concept/gilded-gunship-r60-a.gif) | Gilded Gunship A, gilded chevron — the r04 gold chevron plate across the nose, white-gold engines, twelve ring lamps, flank bay doors; strong rim (motion) | proposed |
| [concept/gilded-gunship-r60-b.png](concept/gilded-gunship-r60-b.png) | Gilded Gunship B, gold scale plate — the whole front third in gold scale armour and gold pod tops; a round hex bubble (sheet) | proposed |
| [concept/gilded-gunship-r60-b.gif](concept/gilded-gunship-r60-b.gif) | Gilded Gunship B, gold scale plate — the whole front third in gold scale armour and gold pod tops; a round hex bubble (motion) | proposed |

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP and fire rates re-measured with `levelReport` at L30
- [ ] Directional damage on one hull: the frontal arc ×0.5 and the engine arc ×2 by the direction of arrival (the Scuttler's arc test), landing blasts ignoring both
- [ ] The hex shield per [enemy shields](../../README.md#enemy-shields)
- [ ] The Dart Drone streams (`spawn` from the bays, at most 6 alive), deterministic and allocation-free
- [ ] Production sprites (the plate, the engines, the bays opening) via `tools/art/`; death effect, bounty and sounds (Freesound, CC0/CC-BY)

## Open questions

- **Q1** (round 60): the shield of 12 follows **a**; under **b** only `shield-breaker`, beams and the
  EMP would get through; under **c** no shield in Act 5.
- **Q6** (round 60, popcorn in a human war): **b** recommended (the Gunship launches Dart streams);
  **a** no Darts, the Vrell carry the count (L30 falls short of the floor); **c** Talons in great
  numbers.

## Decisions

- 2026-10-27 (step m9-d-enemies-a): drafted for the Act 5 design phase from the roster row and the
  gap analysis; every number is a first draft pending the user's review in round 60. The roster's
  "armoured front (50 % damage reduction)" and "engines ×2" kept, as arcs by the direction of arrival
  on a single hull (the Scuttler's built test), not as parts.
