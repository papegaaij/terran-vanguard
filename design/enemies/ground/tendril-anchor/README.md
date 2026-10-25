---
title: Tendril Anchor
design: approved
implementation: done
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/weapons, ../../../campaign/act-3-red-dust]
updated: 2026-10-18
---

# Tendril Anchor

## Summary

Small Vrell growths rooted in pairs at the foot of the canyon walls of Valles Marineris (and on rocks and
pylons elsewhere) that stretch a glowing, damaging tendril across the flight path. Kill either
anchor to drop the barrier, or burn through the tendril itself with a `beam` (Level 16).

## Design

### Stat block

Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)). The numbers live in
[data.yaml](data.yaml) (M7 part C, step C2), with the `link` attack (the tendril's HP, tell, band, cooldown and
burnt bounty) and the hard-only orb (`only: [hard]`) of the [schemas](../../../tech/architecture/schemas-units/README.md);
the table is rendered from it. HP is in damage units (a Pulse Cannon L1 shot
does 2). Set at its first level from the proposed Act 3 reference DPS (M18 = a, approved in round 36:
L16 = 80): 12 HP per anchor is a time-to-kill of 0.15 s. As a `small` unit it keeps its HP in
every later level.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `ground` (at a wall's foot, a rock's or on a pylon); the tendril on the player's plane (`link`) |
| Size tier | `small` |
| Size | 40×40 px, hitbox 30×30 (each anchor); tendril 8 px wide, its contact band 6 px |
| Parts | anchor pair: two `destroyable` anchors and the tendril between them (M9 = a: its own HP, hit only by `beam`) |
| Orientation | `32 angles` (each faces its partner); the tendril a stretched strip |
| HP | anchor 12 (easy 9 / hard 16, from the global multipliers) |
| Armour / shield | none on the anchors; the tendril ignores every non-`beam` shot (shots pass through it) |
| Speed | 0 px/s (still; scrolls with the ground, walls included) |
| Movement | `terrain` |
| Attack | `link`: the tendril between the two anchors (40 HP, hit only by `beam`; easy 30 / hard 52), dormant 1 s after the pair scrolls onto the screen, then contact `medium` 6 per touch with a 0.5 s cooldown (M9 = a; its 6 px band, the ship not pushed); the anchors fire nothing (hard: each anchor an `aimed` orb every 3 s, standard 150 px/s, `small` 4) |
| Formations | turret nest (2–6) (pairs); 1–3 pairs per gate, a gate's tendrils at different heights |
| Weak points | the glowing root bulb at the tendril's base (drawn only) |
| Effective traits | `beam`, `piercing`, `forward` |
| Credits | 12 (score 120 × chain); tendril burnt 8 (score 80 × chain) |
| Death | anchor: `small` organic burst, the tendril snaps back into the surviving anchor and withers over 0.4 s; tendril burnt: it parts in the middle, both halves recoil |
| First level / used in | L16; on rocks and pylons elsewhere in later acts (the level documents decide) |
| Difficulty hooks | hard: the anchors' aimed orbs; easy: tendril 30 HP (global multiplier) |
<!-- /data -->

### Behaviour

- **The barrier.** Two anchors sit at the foot of opposite walls (or of a rock island and a wall) and the
  tendril spans the line between their centres. It scrolls with the ground, so the barrier comes
  down the screen like a gate the ship cannot fly through while it stands.
- **Tell.** When the pair scrolls onto the screen the tendril is **dormant for 1.0 s**: a thin
  flickering thread that deals no damage. Then it flares to its full width with a hum; the hum
  rises as it nears the ship's row. A gate enters at the top edge, so the player always has its
  full approach (~2.5–3.5 s at L16's fast scroll) to deal with it.
- **Touch.** The ship (and Rook) touching the tendril's 6 px band take **`medium` 6** (shield
  first, overflow to armour) and the ship is not pushed; 0.5 s cooldown per touch. A ship that
  holds still in the band takes 12 per second: unpleasant, not lethal. Enemy bullets and enemies
  pass through it.
- **Breaking it.** Either anchor killed (by any weapon) breaks the tendril. A `beam` touching the
  tendril damages it (M9 = a): 40 HP is 0.9 s of an Ion Beam at L1 (45 DPS, raised from the
  roster's 35 by the [weapon document](../../../player/weapons/ion-beam/README.md)), 0.3 s at L5
  (130 DPS); the rest of the beam goes on past it. Other shots pass through the tendril, as do homing missiles,
  bombs and mines. A broken tendril stays broken.
- **Walls.** Anchors stand at a wall's foot on the canyon floor (MA.1 = b of round 36: the faces
  are scenery) and are hittable from the play field; shots that hit the wall rock around them stop (M2 = a). The Ion Beam stops at rock but not
  at the tendril; the Orbital Lance's column is not stopped by rock either (round 39 C1).
- **Rook.** His AI treats a standing tendril as a wall: he holds below it, shooting the nearer
  anchor, and crosses only when it is down.
- **Code fit.** A new `link` attack (two units and a line between them; the Mantis
  `laser-sweep`'s line-vs-ship test for the contact) and a tendril part hit only by the `beam`
  delivery (§2.7 of the gap analysis).

### Difficulty and level use

- Hard adds the anchors' aimed orbs; the global multipliers apply to the HP.
- A level teaches it alone first (one pair, nothing else on the screen), then pairs at
  different heights, then gates combined with a narrowing canyon.
- Never put two gates closer than 1.5 s of scroll apart: the second must be readable after the
  first breaks.

## Concept art

Proposals of concept round 36 (step A-units, listed in the [ground](../README.md#concept-art)
Concept art table; prompts in [../concept/prompts.md](../concept/prompts.md), generator
`tools/concept/enemies_r36.py`): an a/b of the anchor pair on the canyon walls with the tendril between them; the beam in the GIFs is a mock-up. Round 36 (2026-10-13): **a** (polyp clamps, fleshy rope) chosen (U.2), b rejected; with the canyon walls' b (MA.1) the anchors sit at the walls' foot.

| File | What | Status |
|---|---|---|
| [../concept/tendril-anchor-r36-a.png](../concept/tendril-anchor-r36-a.png) | Tendril Anchor A — polyp clamps with a fleshy rope link; pulse, link and burning link (sheet) | chosen (round 36, U.2) |
| [../concept/tendril-anchor-r36-a.gif](../concept/tendril-anchor-r36-a.gif) | Tendril Anchor A — scrolling canyon: one pair broken by killing an anchor, one cut by a beam (motion) | chosen (round 36, U.2) |
| [../concept/rejected/tendril-anchor-r36-b.png](../concept/rejected/tendril-anchor-r36-b.png) | Tendril Anchor B — spine pylons with a crackling three-strand energy filament (sheet) | rejected — round 36: a (polyp clamps) picked |
| [../concept/rejected/tendril-anchor-r36-b.gif](../concept/rejected/tendril-anchor-r36-b.gif) | Tendril Anchor B — the same scene as A (motion) | rejected — round 36: a (polyp clamps) picked |

## Implementation

- [x] Anchor pair as one wave entry: two anchors, positions on walls/rocks, the tendril between their centres
- [x] `link` attack: 1.0 s dormant tell, then `medium` 6 per touch with a 0.5 s cooldown (ship and Rook)
- [x] Tendril part hit only by `beam` (40 HP); breaking it or killing an anchor drops the barrier for good
- [x] Hard: anchors fire aimed orbs every 3 s
- [x] Rook holds below a standing tendril and shoots the nearer anchor
- [x] Death, snap-back, bounty (12 per anchor, 8 for a burnt tendril) and score
- [x] Data file with the stat block (when Level 16 is built; M7 part C, step C2)
- [x] Production art (round of part C): step ART-C, approved as final in [round 39](../../../concept-rounds/round-39/README.md) (F.3)

## Open questions

- None open: round 36 answered it (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the [ground roster](../README.md) to a draft
  spec. M9 = a (the tendril has its own HP, hit only by `beam`; anchors hit by everything; touch
  `medium` 6 with a cooldown) and M18 = a taken as the recommended options, accepted in round 36 (2026-10-13). Our proposals: anchors 12 HP, tendril 40 HP, a 1.0 s dormant tell, a
  0.5 s touch cooldown, no regrowth, the anchors silent except on hard.
- 2026-10-12 (merge): the beam figure follows the Ion Beam document (L1 45 DPS, not 35); the
  round-36 a/b proposals are listed under Concept art (`art: proposed`).
- 2026-10-13 (user, round 36): approved (D.10). M9 = a accepted; Q.22b: a broken tendril does not regrow. U.2 = a: the polyp clamps with the fleshy rope chosen, the spine pylons (b) rejected and moved to `../concept/rejected/`. MA.1 = b (the canyon walls rise toward the camera, nothing gameplay touches on a face): the anchors stand at the walls' foot (Level 16).
- 2026-10-14 (M7 part C, step C2): the stat block moved into [data.yaml](data.yaml): the `link` attack
  (the tendril's 40 HP, 1.0 s tell, 6 px band, 0.5 s cooldown, burnt 8) and the hard-only aimed orb
  (`only: [hard]`); a level places the pairs as a linked enemy nest (`link: pairs`, a default of the
  gap check, accepted in round 39 (2026-10-18).
- 2026-10-14 (M7 part C, step C5): every item built (steps C3/C4 (simulation, data), G2 (looks, sounds)), flown in Level 16's capture (round 39); the art and the defaults accepted in round 39 (2026-10-18).
- 2026-10-18 (user, round 39): closed. The production art `tendril-anchor-final-r39-a` approved as final (F.3),
  so `art: final`; the gates as linked nests (`link: pairs`, the anchors 20 px off the wall line) and B.2's
  choices (a beam burns a dormant tendril too, the shared touch cooldown, Rook's clearance, the burnt
  tendril as a part kill) kept; C1: the Orbital Lance is not stopped by rock (the specials spec), the Ion Beam
  is. The tendril hum, burn and break accepted (S.1–S.3).
