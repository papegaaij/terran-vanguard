---
title: Sentinel Tower
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials]
updated: 2026-10-27
---

# Sentinel Tower

## Summary

A tall Ascendancy tower in the side galleries of the hollowed asteroid: its emitter sweeps a laser through
an arc in **any direction**, and every third sweep turns a full circle, so the ship has to leave its marked
reach. A hardened collar shields the base; the emitter on top takes every weapon.

## Design

### Stat block

**Draft** (Act 5 design phase, step D-enemies-B; approved in round 60). A hand-written
table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2),
set at its first level from the proposed Act 5 reference DPS (judgement call J2: L32 132): the emitter's
120 HP is 0.9 s. Sweep rates are tuned per level with `levelReport`.

| Field | Value |
|---|---|
| Faction | Ascendancy; a **machine** (stunned 3 s by the EMP Burst) |
| Layer | `ground` |
| Size tier | `medium` |
| Size | 80×80 px (the tower seen from above, its head at the centre); hitboxes: emitter 28×28 on the centre, collar 72×72 |
| Parts | emitter (`vital`), hardened collar (no HP: a hit area) |
| Orientation | `fixed` tower; the emitter head `32 angles`, turning with the beam |
| HP | emitter 120 (easy 90 / hard 156, from the global multipliers) |
| Armour / shield | **hardened collar**: a non-`anti-ground` shot that lands on the collar outside the emitter glances off with a spark; an `anti-ground` shot or blast anywhere on the collar damages the emitter; a beam whose line crosses the emitter reaches it (the collar does not stop a beam) |
| Speed | 0 px/s (scrolls with the tunnel) |
| Movement | `terrain` |
| Attack | `laser-sweep`: a beam **220 px** long, 6 px wide, from the emitter; an **arc sweep** turns 120° over 1.6 s, centred on the ship's bearing at the telegraph's start, in any direction; every third sweep is a **full turn**, 360° over 3.0 s (the direction alternates); telegraph: the arc (or, for a full turn, the whole reach ring and the start ray) drawn crimson for **0.8 s** (full turn **1.0 s**); a sweep every 4.5 s, telegraph to telegraph (the full turn's slot 6 s); `laser` 8 per touch, once per sweep; the beam passes over walls |
| Formations | turret nest (single, or a pair across a gallery whose reaches leave a lane) |
| Weak points | the emitter (the vital part; it glows crimson while the beam is live) |
| Effective traits | `anti-ground`, `beam`, `piercing` |
| Credits | 40 (score 400 × chain) |
| Death | `medium` ground explosion, the head topples off; the scorched tower stump stays |
| First level / used in | L32 (Deep Core, the side galleries); later Act 5 levels may use it (their docs decide) |
| Difficulty hooks | easy: 100° arcs, no full turn, a sweep every 5.5 s; hard: 160° arcs, every second sweep a full turn, a sweep every 3.6 s |

### Behaviour

- **A 360° sweep from the ground.** Unlike the [Mantis](../../air/mantis/README.md) (clamped so the far
  side stays safe), the Sentinel aims all round: a tower that has scrolled past sweeps up the screen. The arc
  telegraph is ≥ 0.6 s as the vocabulary asks (0.8 s), the full turn's 1.0 s.
- **The full turn.** A 220 px beam turning 120°/s cannot be outrun near the tower; the 1.0 s telegraph draws
  the reach ring so the ship leaves it (down the field, or to the far side of the tunnel). The level places
  each tower so its reach always leaves a way out: no tower in a passage narrower than its reach plus 80 px,
  and never two full-turn towers overlapping.
- **Hardened collar.** The roster's "hardened base": front guns must hit the 28 px emitter; `anti-ground`
  weapons (the Bomb Rack, the Hammer Mortar) and blasts (specials, mining charges) damage it anywhere on the
  tower; the Ion Beam reaches it through the collar when its line crosses the emitter. Homing shots aim at the
  emitter.
- **EMP.** Stunned 3 s: the beam is cut mid-sweep and the next telegraph starts after the stun.
- **Airstrike and Orbital Lance** are forbidden inside the hollowed asteroid (L32, rock overhead; the specials'
  setting restriction, the player step's part), so the tower is a gun target there.

### Difficulty and level use

- L32's side galleries: a tower per gallery, the Strider pairs on the floors below; in the branch the player
  picks (Q5 a) the richer branch has more towers.
- At most 2 towers sweeping at once.

### Sounds

Brief for the round-60 sound proposals (Freesound CC0/CC-BY, real recordings, no bubbly or popping
sounds): the charge a rising electrical hum (a transformer or a capacitor bank charging); the live beam a
harsh sizzle (an arc welder or an electric arc); the turn a low servo grind; the death a heavy metal crash
with the head falling.

## Concept art

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

Round 60 proposals (a/b), made in the gloss-black look of Q11 a and chosen below (production re-renders the chosen one in gunmetal/charcoal, Q11 b) (step ART-P-units, `tools/concept/enemies_r60.py`): a gloss-black
tower with gold collar plates seen from above, a crimson emitter eye and red running lights (Q11 a); the
sweep telegraph in the crimson of lasers and sweeps.

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (gloss black; the user chose look b, gunmetal/charcoal with gold, in round 60, so the chosen variant is re-rendered in gunmetal in production), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 32 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/sentinel-tower-r60-a.png](concept/sentinel-tower-r60-a.png) | Sentinel Tower A, lighthouse — a gold-banded octagonal hardened collar, a round shaft, a black sphere head with a gold equator ring and a lens snout; strong rim (sheet) | chosen (round 60) |
| [concept/sentinel-tower-r60-a.gif](concept/sentinel-tower-r60-a.gif) | Sentinel Tower A, lighthouse — a gold-banded octagonal hardened collar, a round shaft, a black sphere head with a gold equator ring and a lens snout; strong rim (motion) | chosen (round 60) |

## Implementation

- [ ] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers applied; tuned with `levelReport`
- [ ] The emitter and the hardened collar (glance outside the emitter, `anti-ground` and blasts through, the beam's line rule)
- [ ] `laser-sweep` in any direction (no clamp), the arc and full-turn telegraphs, the full turn every third sweep (hard: every second)
- [ ] Production sprites (the tower, the head at 32 angles, the stump), the beam and the reach ring, the intel portrait
- [ ] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds)

## Open questions

- Q5 (decided in round 60): L32's branching tunnels, **a** recommended (two real forks, one safer, one richer);
  alternatives **b** (cosmetic forks) and **c** (the same waves in both). The towers' placement follows it.
- Q11 (decided in round 60, **b**, not the recommendation): the Ascendancy look on the belt is gunmetal/charcoal hulls
  with gold (≥ 20 % gold, rim light, red running lights); a (gloss black) and c (black silhouettes) rejected
  ([enemies](../../README.md#factions-and-visual-language)).

## Decisions

- 2026-10-27 (step D-enemies-B): drafted for the Act 5 design phase from the roster row and the gap
  analysis; every number is a first draft, approved in round 60.
- 2026-10-27 (decided by me, trivia): the "hardened base" is a collar around a hittable emitter (so a front
  gun can still kill it with aim, `anti-ground` with ease); the "360°" is the full turn every third sweep, with
  a reach ring telegraph of 1.0 s; the beam is 220 px so a tunnel always leaves a way out.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`); art A.12 **a**, "Sentinel Tower A, lighthouse", chosen (`art: chosen`), the other variant in `concept/rejected/`. Q11 **b** (gunmetal/charcoal with gold, not gloss black): the chosen variant is re-rendered in gunmetal/charcoal in this part's production art, keeping the ≥ 20 % gold and the readability check.
