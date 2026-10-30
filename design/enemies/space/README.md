---
title: Space enemies
design: approved
implementation: in-progress
art: final
updated: 2026-10-28
---

# Space enemies

## Summary

Vacuum-only enemies for space levels: the belt, the gate and Vrell space. On the `space` layer
they behave like `air` units (the player's plane) but never appear in atmosphere or water. Many
use the space environment: clinging to asteroids, laying mines, draining energy or bending space.

## Contents

Promoted to their own documents for the Acts 1–2 wrap-up, plus the Act 5 drafts of its design phase (2026-10-27,
approved in round 60); the remaining units stay in the roster below.

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [leviathan](leviathan/README.md) | Huge whale set piece: high-air pass, then shootable parts (L03) | approved | done | final |
| [asteroid-mite](asteroid-mite/README.md) | Tiny Vrell crawler riding drifting rocks, leaps at the ship within 160 px (L29) | approved | done | final |
| [minelayer](minelayer/README.md) | Ascendancy mine ship from behind, lays proximity-mine lines with one gap (L30) | approved | done | final |
| [shard-drone](shard-drone/README.md) | Spinning drones in groups of 3–4 joined by energy links, a moving barrier (L31) | approved | done | final |
| [void-leech](void-leech/README.md) | Vrell leech that latches and drains 2 MW of generator output (L33) | approved | done | final |

## Roster

| Name | Faction | Layer | Tier | Role/ability | Formations | First level | Design |
|---|---|---|---|---|---|---|---|
| Gate Warden | Vrell | space | medium | Shielded sentinel. Its shield opens only while it fires a `laser-line`; `shield-breaker` strips it at any time. | line abreast | 43 | idea |
| Rift Skater | Vrell | space | small | Phase-jumps (`teleport`) short distances along the distorted scroll; fires a `burst` after each jump. | stream | 44 | idea |
| Husk | Vrell (Silence-touched) | space | medium | A drifting derelict hollowed from inside. When damaged it cracks and sprays crystalline shards (`death-burst` + `fan`). | swarm (scattered) | 46 | idea |

## Design

- **`space` is a doc alias of the `air` layer** (Act 5 design phase, 2026-10-27): the game has no separate
  space layer; a space level is the vacuum presentation of `air` (no clouds or weather). Rosters and stat
  blocks keep writing `space`.
- Space levels have no `ground` layer in the planetary sense. Station hulls, asteroid surfaces
  and reefs act as the `ground` layer (see [layer rules](../README.md)).
- The Void Leech ties enemies to the player's generator budget; the drain rules are in
  [generator](../../player/generator/README.md#enemy-drain-effects).

## Concept art

Concept [round 05](../../concept-rounds/round-05/README.md) — new archetypes, each a PNG sheet plus a GIF in the 480×540 play field; generator `tools/concept/enemies_r05.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/leviathan-r05-a.png](concept/leviathan-r05-a.png) | Leviathan — huge Vrell whale (bone/violet), ~480 px: body + 3 tail segments + fluke with a travelling wave + flapping fins, each part at 32 headings; dorsal turrets fire aimed orbs (sheet) | superseded by r08 (model-space re-render) |
| [concept/leviathan-r05-a.gif](concept/leviathan-r05-a.gif) | Leviathan — huge Vrell whale (bone/violet), ~480 px: body + 3 tail segments + fluke with a travelling wave + flapping fins, each part at 32 headings; dorsal turrets fire aimed orbs (motion) | superseded by r08 (model-space re-render) |

Concept [round 08](../../concept-rounds/round-08/README.md) — model-space re-render (seams and patterns now turn with the body), design unchanged; generator `tools/concept/rerender_r08.py`.

| File | What | Status |
|---|---|---|
| [concept/leviathan-r08-a.png](concept/leviathan-r08-a.png) | Leviathan re-render (sheet) | chosen |
| [concept/leviathan-r08-a.gif](concept/leviathan-r08-a.gif) | Leviathan re-render (motion) | chosen |

Production art, M9 part B (concept round 62, step ART-B): the Asteroid Mite from round 60's chosen A.7 a (Vrell: its plum chitin and teal glow kept) and the belt's small rocks it rides (first used in Level 29), generator `tools/art/belt_units.py mite rocks`, held in `build/art-held/` until the data claims them (the rocks as debris chunks `belt-a`, `belt-b`, `belt-c`). Prompts: [concept/prompts.md](concept/prompts.md#round-62--production-the-asteroid-mite-and-the-belt-rocks-m9-part-b).

| File | What | Status |
|---|---|---|
| [concept/asteroid-mite-final-r62-a.png](concept/asteroid-mite-final-r62-a.png) | Asteroid Mite final frames (`tools/art/belt_units.py`): 16 headings × the 4-frame crawl, the crouch (tell) and the leap; the three belt rocks at 16 spin frames and a rock shot apart in vacuum (sheet) | chosen (round 62, F.3/F.4: accepted as final); F.3 redo is Level 29's rock waves, not the art |
| [concept/asteroid-mite-final-r62-a.gif](concept/asteroid-mite-final-r62-a.gif) | Two rocks drifting in with riders crawling their rims; within 160 px each rider crouches and leaps at the ship; a shot rock frees its riders | chosen (round 62, F.3/F.4: accepted as final); F.3 redo is Level 29's rock waves, not the art |
| [concept/asteroid-mite-capture-r62-a.png](concept/asteroid-mite-capture-r62-a.png) | Game capture, Level 29 (step B5): rocks drifting in from the left edge, their riders leaping | chosen (round 62, F.3/F.4: accepted as final); F.3 redo is Level 29's rock waves, not the art |

Production art, M9 part C (concept round 63, step ART-C): the Minelayer from round 60's chosen A.8 a "hopper barge" re-rendered in the Q.11 b look (gunmetal hull and charcoal sponsons, the gold edge band and mine drum, a 1 px gold/red rim; gold about 20–23 %), and its line mine in the homing diamond's red-gold ordnance language. Generator `tools/art/belt_units_c.py layer`; brief in [concept/prompts.md](concept/prompts.md#round-63--production-the-minelayer-and-its-line-mine-m9-part-c).

| File | What | Status |
|---|---|---|
| [concept/minelayer-final-r63-a.png](concept/minelayer-final-r63-a.png) | Minelayer final frames (`tools/art/belt_units_c.py`): 16 headings and the laying set (the drum's hatch lit red-gold); the mine dim and lit at 8 spin steps, its glow, the chained pop and the proximity blast with the 40 px ring; the units on Level 30's space (sheet) | chosen (round 63, F.1: accepted as final) |
| [concept/minelayer-final-r63-a.gif](concept/minelayer-final-r63-a.gif) | A Minelayer rising up the left lane from behind, turning and laying a sloped line of three clusters of four with one 112 px gap; the mines dim, then blinking armed; a shot clears the right cluster hop by hop; the ship drifts too close to a mine: the fast blink, the blast and its ring | chosen (round 63, F.1: accepted as final) |
| [concept/minelayer-capture-r63-a.png](concept/minelayer-capture-r63-a.png) | Game capture, Level 30 (step C5): the `BOTTOM` warning at t≈17.5, the first Minelayer rising up the left lane, laying its line, the sloped line with its gap; the minefield in the [Level 30 captures](../../campaign/act-5-the-belt/level-30-minefield/README.md#concept-art) | chosen (round 63, F.1: accepted as final) |
| [concept/shard-drone-final-r64-a.png](concept/shard-drone-final-r64-a.png) | Shard Drone final frames (`tools/art/belt_units_d.py`): round 60's A.9 b tri-prism in gunmetal (the frame struts and hub thickened so the rim no longer turns them gold: about 27-30 % gold), 16 spin frames over the 120° loop; the link strip (crimson lightning with a gold core, tiling along the link, 8 crackle steps), the tell's broken thread, the snap, a flung gold prism (sheet) | chosen (round 64, F.5: accepted as final) |
| [concept/shard-drone-final-r64-a.gif](concept/shard-drone-final-r64-a.gif) | A line abreast of four descending with a sway: the links flicker for 0.8 s, light, then one drone is killed and its two links snap | chosen (round 64, F.5: accepted as final) |
| [concept/shard-drone-capture-r64-a.png](concept/shard-drone-capture-r64-a.png) | Game capture, Level 31 (step D5, not firing; [recipe](concept/prompts.md#shard-drone-capture-r64-a)): the t=58 group's links lit, the EMP's arc at t≈61, the links dropped for the stun, relit and dragged across the bow; the t=82 ring closing round the hull | chosen (round 64, F.5: accepted as final) |
| [concept/void-leech-final-r66-a.png](concept/void-leech-final-r66-a.png) | Void Leech final frames (`tools/art/belt_units_f.py`, round 60's A.10 a, the Vrell's black-violet with a lavender light rim, no gold): 16 headings and the swim, latched on the Stormhawk's three spots with the sac's pulse and the drain glow, on a prey Talon (the Talon darkened by the game's tint, QF-2 a), stunned, the death (sheet) | chosen (round 66, F.1 a and F.2 ok, black-violet: accepted as final) |
| [concept/void-leech-final-r66-a.gif](concept/void-leech-final-r66-a.gif) | A prey Talon drifting down dark with a leech on its back; two leeches coming in from behind and the side, latching left and tail (`GEN -4 MW`), the shake, thrown off stunned, one shot | chosen (round 66, F.1 a and F.2 ok, black-violet: accepted as final) |
| [concept/void-leech-capture-r66-a.png](concept/void-leech-capture-r66-a.png) | Game capture, Level 33 (step F5; [recipe](concept/prompts.md#void-leech-capture-r66-a)): three leeches latched on the ship with the power row (hard), one at medium | chosen (round 66, accepted with the level, F.2) |

## Implementation

- [x] Each enemy promoted to its own directory with a stat block before it is implemented (every unit of Acts 1–2; the rule stands for the units of Acts 3–7, M6 part D)

## Open questions

- Should Husks (L46) show anything of the Silence visually, e.g. an unnatural black-glass
  interior? Proposal: yes, as the only visual hint of the Silence in the game.

## Decisions

- 2026-09-30: Roster of 7 space enemies drafted.
- 2026-09-30: Enemy variety pass: size tier column added to every row; new units Leviathan.
- 2026-10-01: Acts 1–2 units promoted to full specs: Leviathan.
- 2026-10-01: Concept round 08: model-space re-renders accepted; they supersede the r05 sheets.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-27 (Act 5 design phase, step D-enemies-B; approved in round 60): the Asteroid Mite, the
  Minelayer, the Shard Drone and the Void Leech promoted from the roster to draft specs.
- 2026-10-28 (user, [round 62](../../concept-rounds/round-62/README.md), closed; page answers): the **Asteroid Mite** (F.3) and the **belt rocks** (F.4) production art accepted; `art: final`. The user's note on F.3: the Mites "should be hard to spot, but we need more of them, not just a hand full": more rocks carrying Mites arriving at the same time. That is a change to Level 29's rock waves (part B, in the level data), not to the art. Sounds: the leap tell a (S.13), the leap a (S.14).
