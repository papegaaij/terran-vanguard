---
title: Burrower
design: approved
implementation: in-progress
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../campaign/act-3-red-dust]
updated: 2026-10-14
---

# Burrower

## Summary

A Vrell sand-swimmer that travels under the Martian surface, surfaces a short way ahead of the
ship after a bulge in the ground gives it away, fires a `ring`, and dives again after 2 s. While
it is under the sand nothing can touch it: the act's first unit that is hittable only in a window
(Level 15).

## Design

### Stat block

Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)). The numbers live in [data.yaml](data.yaml) (M7 part B, step B2), with the `burrow` movement (the spot rule, the windows, the rings, the stagger) and its difficulty hooks of the [schemas](../../../tech/architecture/README.md#data-file-schemas); the table is rendered from it. User decision Q1 = a of 2026-10-14 (M7 part B): the Burrower is **screen-fixed** while it travels, bulges and is surfaced (a sand bow wave on its top side while up), so the spot rule and the 90 px/s swim hold on a scrolling ground as written; the crater or filling hole it leaves scrolls with the ground. HP is in damage units (a Pulse Cannon L1 shot
does 2). Its HP is set at its first level from the proposed Act 3 reference DPS (M18 = a, approved in round 36: L15 76 · L16 80 · L17 84 · L18 88 · L19 92 · L20 96 · L21 100):
44 HP at L15 is a time-to-kill of 0.58 s, well inside one 2 s surfacing (`anti-ground` halves it).
Where it returns later in the act it gets the act HP factor of a `medium` unit (e.g. ×100/76 =
58 HP at L21).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `ground` while surfaced; `buried` while submerged (new layer, M7 = a: nothing hits it; its **current layer**, flipping at the middle of the dive) |
| Size tier | `medium` |
| Size | 64×64 px, hitbox 44×44 (surfaced); submerged a 40 px sand ripple (no hitbox) |
| Parts | single |
| Orientation | `radial` (a round maw ringed by digging plates) with a 16-angle ripple trail while submerged |
| HP | 44 (easy 33 / hard 57, from the global multipliers) |
| Armour / shield | invulnerable while `buried`; none while surfaced |
| Speed | 90 px/s under the sand, screen-fixed (it works against the scroll, user Q1 = a); still on the screen while it bulges and is surfaced, a sand bow wave on its top side |
| Movement | `burrow`: submerged travel (1–1.8 s) to a spot 140–240 px above the ship and within ±110 px of its x (at least 110 px from it, 40 px inside the side edges), 1 s bulge tell, surfaced 2 s, dive 0.4 s; up to 3 surfacings, then it travels off the bottom of the screen under the sand |
| Attack | 10-bullet `ring` (150 px/s, `small` = 4) 0.4 s after it surfaces, once per surfacing |
| Formations | field (3–6) (scattered), surfacings staggered 0.8 s apart, at most 2 up at once |
| Weak points | glowing lime maw (drawn only) |
| Effective traits | `anti-ground`, `area`, `forward` |
| Credits | 24 (score 240 × chain) |
| Death | `medium` organic burst with a sand spout; leaves a collapsed crater for the rest of the level |
| First level / used in | L15; may return in L20–L21 (the level documents decide) |
| Difficulty hooks | easy: ring of 8, surfaced 2.4 s; hard: ring of 12 plus a second ring of 8 at 1.4 s, surfaced 1.8 s |
<!-- /data -->

### Behaviour

- **Submerged.** It moves on the new **`buried` layer** (M7 = a, accepted in round 36): appended after `sub` so the layer ordinals and replay hashes stay, hit by nothing,
  not even the Smart Bomb or the Airstrike, no contact, never picked by homing shots, turrets or
  Rook. It is drawn as a ripple in the sand moving at 90 px/s (a darker furrow with grit kicked up
  at its head), so a watchful player can follow it.
- **Choosing a spot.** It surfaces at a point picked when the bulge starts: 140–240 px above the
  ship's y and within ±110 px of the ship's x (seeded), kept 40 px inside the play field's side
  edges and never closer than 110 px to the ship's centre (so the ring respects the 72 px spawn
  rule). It travels to that spot submerged; the spot stays where it is on the screen (Q1 = a of 2026-10-14:
  the Burrower works against the scroll while it travels, bulges and is surfaced, a sand bow wave on its top
  side while up).
- **Tell.** For **1.0 s** before surfacing the ground **bulges** at the spot: a growing mound of
  sand with radial cracks and a lime glow leaking through them, plus a low rumble. The bulge is
  the tell for both the position and the moment.
- **Surfaced.** It breaks out (sand spray, the crater rim), is a plain `ground` target for
  **2.0 s** (all weapons, `anti-ground` ×2), fires its ring after 0.4 s, then dives over 0.4 s
  (`buried` from the dive's middle, the Driftjelly's layer swap at its middle). A Burrower killed
  while surfaced leaves its crater; one that dives leaves a filling hole.
- **Cycle.** Submerged travel to the next spot takes 1.0–1.8 s (seeded), so a cycle is about
  4.5 s. After its third surfacing (or when the wave's time runs out) it heads off the bottom of
  the screen under the sand and is gone (an escape, no bounty).
- **Swarm stagger.** A wave of 3–6 staggers its surfacings 0.8 s apart and lets at most two be up
  at the same time, so the rings come as a rhythm, not a wall.
- **Code fit.** The Driftjelly's `EnemySpec.Submerge` timer (seeded waits, the layer flip at the
  swap's middle) plus a spot picked relative to the ship and the ring on surfacing (§2.2 of the
  gap analysis).

### Difficulty and level use

- Rings, window and the hard second ring per the stat block; the global multipliers apply.
- A level should not surface Burrowers under a dense air wave in its first appearance (L15 teaches
  it alone first: the act's first new unit).
- Over water or rock it cannot appear; a level only places it over sand, dust or regolith.

## Concept art

Proposals of concept round 36 (step A-units, listed in the [ground](../README.md#concept-art)
Concept art table; prompts in [../concept/prompts.md](../concept/prompts.md), generator
`tools/concept/enemies_r36.py`): an a/b of the body, the surfacing sequence and the bulge tell. Round 36 (2026-10-13): **a** "sand maw" chosen (U.1), b "sand clam" rejected.

| File | What | Status |
|---|---|---|
| [../concept/burrower-r36-a.png](../concept/burrower-r36-a.png) | Burrower A, sand maw — slate/lime radial worm head: furrow, bulge tell, breach, ring of orbs, submerge (sheet) | chosen (round 36, U.1) |
| [../concept/burrower-r36-a.gif](../concept/burrower-r36-a.gif) | Burrower A — three Burrowers cycling in the play field (motion) | chosen (round 36, U.1) |
| [../concept/rejected/burrower-r36-b.png](../concept/rejected/burrower-r36-b.png) | Burrower B, sand clam — bone/violet armoured clam-shell that faces the ship (sheet) | rejected — round 36: a (sand maw) picked |
| [../concept/rejected/burrower-r36-b.gif](../concept/rejected/burrower-r36-b.gif) | Burrower B — the same scene as A (motion) | rejected — round 36: a (sand maw) picked |
| [../concept/burrower-final-r38-a.png](../concept/burrower-final-r38-a.png) | Burrower final sprites (`tools/art/burrower.py`): body and maw bloom, the sand bow wave (screen-fixed while up, Q1 = a), 16-heading ripple, bulge, breach, dive, hole, crater, death (sheet) | chosen (round 38, F.3: approved as final) |
| [../concept/burrower-final-r38-a.gif](../concept/burrower-final-r38-a.gif) | Burrower final: three Burrowers cycling over a scrolling Level 15 ground stand-in, 10 fps (motion) | chosen (round 38, F.3: approved as final) |

## Implementation

- [x] The `buried` layer: hit by nothing, no contact, never targeted (M7 = a) — M7 part B (sim step B1: `Layer.BURIED`, `BuriedLayerTest`; mapped in step B3)
- [x] `burrow` movement: submerged travel, a spot ahead of the ship (140–240 px, ±110 px, ≥ 110 px away), 1.0 s bulge, 2.0 s surfaced, 0.4 s dive with the layer flip at its middle, 3 surfacings then away — M7 part B (sim step B1, `BurrowTest`; step B3 maps the stat block, content's `MarsRules`, `Level15Test`; its longest life of 30 s a default pending round 38)
- [x] Ring of 10 at 0.4 s after surfacing; the hard second ring — M7 part B (steps B1, B3: the hooks per difficulty, `Level15Test`)
- [x] Swarm stagger: 0.8 s apart, at most 2 surfaced — M7 part B (steps B1, B3)
- [x] Ripple, bulge tell, crater and death per this spec; bounty and score — M7 part B (step B4: the game's `BurrowerLooks` on the ground layer as tools/art/burrower.py's timeline, the ripple on its ground-relative heading, the hole and the kept crater scrolling with the ground, the eruption from the bulge's start and the dive's hiss; `BurrowerLooksTest`)
- [x] Data file with the stat block (M7 part B, step B2; the simulation's mapping of its new keys in step B3)
- [x] Production art (round of part B): `tools/art/burrower.py`, review `burrower-final-r38-a` (pending round 38)

## Open questions

- None open: round 36 answered it (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the [ground roster](../README.md) to a draft
  spec. M7 = a (a new `buried` layer, nothing hits it) and M18 = a (L15 reference DPS 76) taken
  as the recommended options, accepted in round 36 (2026-10-13). Our proposals: 44 HP,
  bounty 24, the 1.0 s bulge tell, the spot rule (140–240 px ahead, ≥ 110 px from the ship),
  3 surfacings, at most 2 up at once.
- 2026-10-12 (merge): the round-36 a/b proposals are listed under Concept art (`art: proposed`).
- 2026-10-13 (user, round 36): approved (D.9). M7 = a (the `buried` layer) accepted; the open question answered as proposed (Q.22a): in Level 18's storm only the bulge shows, not the ripple. U.1 = a: the sand maw (slate/lime) chosen, the sand clam (b) rejected and moved to `../concept/rejected/`.
- 2026-10-14 (user, M7 part B Q1 = a): screen-fixed while it travels, bulges and is surfaced (a sand bow wave
  on its top side while up), so the spot rule's numbers, the 90 px/s swim, the 4.5 s cycle and the three
  surfacings hold on Level 15's 150–160 px/s ground; the crater or filling hole it leaves scrolls with the
  ground. The stat block moved into `data.yaml` (step B2).
- 2026-10-14 (M7 part B, step ART-B): production art from the chosen round-36 look (a, sand maw), review sheet and GIF `burrower-final-r38-a` proposed for round 38 (accepted in round 38 (2026-10-14)).
- 2026-10-14 (user, round 38): closed: the production art `burrower-final-r38-a` approved as final (F.3), so
  `art: final`; the simulation's build choices B.1 (a)–(f) all yes (the 30 s life and the escape off the bottom,
  the spot picked at the swim's start and checked again at the bulge, the mirrored spot, the stagger, the
  no-surface zones' nearest allowed point) and the game's B.3 (b)–(c) and (e) yes (32 craters, no hole after an
  early dive kill, the eruption from the bulge's start); R.2 accepted (the surfaced windows not retuned). Its
  sounds: the bulge's crack layer b over the eruption, the dive's grain slide b (S.1, S.2).
