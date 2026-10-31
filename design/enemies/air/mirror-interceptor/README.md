---
title: Mirror Interceptor
design: approved
implementation: in-progress
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../player/weapons, ../../../player/shields, ../../../art-direction, ../../../campaign/act-6-jovian-storm]
updated: 2026-10-31
---

# Mirror Interceptor

## Summary

An Ascendancy heavy interceptor with a curved **mirror** across its nose (Level 37, Aurelia). Shots fired into the
mirror come back as slow enemy bullets; a beam held on it heats it until it breaks; a `shield-breaker` hit or the EMP
shatters it at once. Its flanks and rear are always open. It enters in pincers from the sides and in ambushes from
the rear, and turns its mirror to the ship.

## Design

### Stat block

**Draft** (Act 6 design phase, decided 2026-10-31 (round 72)). A hand-written table: there is no
`data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at
its first level from the proposed Act 6 reference DPS (judgement call J1, L37 = 148). A `medium` unit: no beam-era
factor; the act HP factor in its later levels. Fire rates are first values: the build tunes them per level with
`levelReport`.

| Field | Value |
|---|---|
| Faction | Jovian Ascendancy (regular gunmetal livery); a **machine** (the [EMP Burst](../../../player/specials/README.md#emp-burst-l15) stuns it 3 s and shatters its mirror) |
| Layer | `air` |
| Size tier | `medium` |
| Size | 72×68 px, hitbox 52×48; the mirror a curved plate 76 px wide across the nose |
| Parts | single (the mirror is a state of the unit, not a part) |
| Orientation | `16 angles`; it turns its nose (and the mirror) toward the ship at **60°/s** while it holds its station |
| HP | 120 (easy 90 / hard 156, from the global multipliers): 0.81 s from the flank at the L37 reference of 148 |
| Armour / shield | the **mirror** ([reflect](../../README.md#reflect)): arc **120°** (±60° of its facing); heat **130** (about 1.0 s of the plan's Ion Beam L5 at L37, 130 DPS; the build re-measures it against the generator's Act 6 hand plan; the act HP factor raises it in later levels: ≈ 140 at L40, ≈ 148 at L42); down **3 s** after a break or a shatter. No hex shield |
| Speed | 180 px/s entering and leaving; 40 px/s strafing at its station |
| Movement | enters on a curve to a **station** and faces its flight until it gets there: from a side edge (a `pincer`) to 90–130 px in from that edge, 160–300 px below the top edge; from the bottom edge (a `rear ambush`) to 90–120 px above it, in its lane. It holds the station **7 s**, strafing up to 60 px along it to keep its mirror on the ship, then leaves through the edge it came in by (a rear one up through the nearest side edge) |
| Attack | `burst` of **3** aimed shots 0.15 s apart every **2.4 s**, from two gun pods under the mirror's rim, only while its facing is within 30° of the ship; standard 160 px/s, `medium` = 6; the tell is a **0.4 s red glow** in the pods under the mirror, and it holds its facing through the burst. Contact `medium` = 15. Its other "attack" is the player's own fire, bounced |
| Formations | pincer (1 + 1 or 2 + 2), rear ambush (2–3); each first entry from a side or the rear edge-warned 3 s ahead; hulls at least one hull length apart (≥ 80 px between hull edges, the [formation rule](../../README.md#formation-vocabulary) of Act 6); at most 4 on the screen |
| Weak points | the open flanks and the engines (drawn: the mirror's silver edge shows where the arc ends) |
| Effective traits | `shield-breaker` (shatters the mirror), `side`, `rear` (the open flanks), `beam` (burns the mirror) |
| Credits | 30 (score 300 × chain); the mirror pays nothing, bounced bullets pay nothing |
| Death | `medium` explosion; the mirror bursts into falling silver shards (presentation, no damage) |
| First level / used in | L37 (Aurelia, with `shield-breaker` "first strongly rewarded" there); its later Act 6 levels per the level drafts; the Ascendant's phase-1 panels share its rule (its own document) |
| Difficulty hooks | easy: arc 100°, turn 45°/s, burst of 2 every 2.8 s; hard: arc 140°, turn 80°/s, burst of 4 every 2.0 s; the heat takes the HP lever |

### Behaviour

- **The mirror** follows the shared [reflect](../../README.md#reflect) rule (Q1 c): non-beam shots inside the arc
  bounce back as slow magenta `small` bullets at 110 px/s, at most one every 0.15 s (the rest spark and vanish); a
  beam on the mirror heats it instead of the hull and breaks it after about 1.0 s; a Tesla Coil Pod hit or the EMP
  shatters it at once; down 3 s, then a 0.4 s silver shimmer as it regrows. A bounced bullet is never bounced again.
- **The play.** It is the act's loadout question: shoot the mirror with a spread weapon and it shoots back;
  **catch it on its way in** (it faces its flight until it reaches its station: a side entry shows its flank to the
  forward guns for about a second), slip round it faster than it turns (60°/s), use the wing and rear mounts on its
  flanks, burn the mirror with the beam, or carry the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md)
  and shatter it in one hit. The [Reflex Shield](../../../player/shields/README.md) of Act 6 answers mirrors with a
  mirror (the D-systems draft): a bounced bullet it absorbs is not bounced again.
- **Spacing** (the Act 5 lesson): two Mirror Interceptors never overlap, nor sit over a Gunship or Hornet; a
  pincer's two groups keep a hull length apart, and so do a rear ambush's lanes.
- **Look** (Q9 a, regular livery): a short, broad gunmetal hull with gold trim and red running lights; the mirror a
  polished silver-gold crescent with a slow specular sweep across it, so the arc reads at a glance. Heat: the
  plate glows dull red, orange, then white, cracks from 70 %; broken, a bare grey nose frame; regrowing, a silver
  shimmer sweeps across from one tip.
- **Sounds** (brief for the audio step): a heavy turbine hum; a bright glassy ping on each bounced shot (a struck
  glass or metal bowl from Freesound, CC0/CC-BY), a rising hot-metal hiss under a beam, a real glass shatter for
  the break, a low chime as it regrows; the pods' burst a dry triple crack.

### Difficulty and level use

- At most 4 on the screen, at most 2 holding a station on the same side.
- A level does not pair a Mirror Interceptor's station with a dense spread of popcorn in front of its mirror: the
  player must be able to stop firing into it without being overrun.

## Concept art

Round 72's proposals (step ART-R-A, picked 2026-10-31; generator `tools/concept/enemies_r72.py mirror-interceptor`). Each sheet holds the source render, the sprites at play size with their states, the readability check over Level 37's brightest and darkest band beside the player with the measured gold share, and key frames; each GIF is the 480×540 play field. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/mirror-interceptor-r72-a.png](concept/mirror-interceptor-r72-a.png) | Mirror Interceptor a, "crescent": the broad gunmetal hull with one curved silver mirror plate 76 px across the nose, gold tip caps; 16 headings; the states: standing, the specular sweep, heat 40 / 78 (cracks) / 100 %, broken (the bare frame), regrowing, the pod tell | chosen (round 72 A.2 a) |
| [concept/mirror-interceptor-r72-a.gif](concept/mirror-interceptor-r72-a.gif) | A side entry (edge-warned): its flank on the way in, the bounce at its 7 s station (slow magenta bullets, one per 0.15 s), two pod bursts, the flank again as it leaves, the kill and the falling silver shards | chosen (round 72 A.2 a) |
| [concept/mirror-capture-r73-a.png](concept/mirror-capture-r73-a.png) | Game capture (the A4 rig; stand-in sprite, A3a's code-drawn plate): the Ion Beam L5 on the mirror over the cream bands, the plate heating to white, the break and its shards, the beam on the hull while it is down, the regrown plate bouncing the pods' bursts, 2×; the plasma arc earthed on its face over round 72's grade | proposed (round 73) |
| [concept/mirror-capture-r73-a.gif](concept/mirror-capture-r73-a.gif) | Heat, break, down time and regrowth under the beam, 2×, 30 fps | proposed (round 73) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP and heat re-measured with `levelReport` at L37
- [x] The [reflect](../../README.md#reflect) rule: the arc test, the bounce (rate cap, mirror-image path, `small` 110 px/s, never twice), beam heat and cooling, the breaker and EMP shatter, the 3 s down time and regrowth, Rook's shots absorbed; deterministic and allocation-free (M10 part A, 2026-10-31: steps A1a, A1b, A3a, `ReflectTest`; the look on stand-in sprites)
- [ ] Side and rear entries to a station, the turn toward the ship, the burst only while facing the ship; hull spacing in pincers and ambushes
- [ ] Production sprites (16 headings, the mirror's sweep, heat, cracks, break and regrowth) via `tools/art/`; death effect, bounty and sounds (Freesound, CC0/CC-BY)

## Open questions

- None open.

## Decisions

- 2026-10-31 (step m10-d-enemies-a): drafted for the Act 6 design phase from the roster row and the M10 gap
  analysis (§2.2 and Q1 c: the front 120° arc, beam heat about 1.0 s, the breaker or EMP shatter, 3 s down, flanks
  open; pincers and rear ambushes a hull length apart); every number is a first draft decided 2026-10-31 (round 72). The roster's "vulnerable to `beam`/`shield-breaker`" became Q1 c's heat and shatter. The mirror
  replaces the hex shield (one defence a unit); the 60°/s turn and the entry flank window are this step's.
- 2026-10-31, design phase (step m10-d-act, the main agent's call; decided 2026-10-31 (round 72)): the mirror's heat **110 → 130**,
  about 1.0 s of the L37 plan's Ion Beam L5 (130 DPS); the act HP factor scales it like a hull in Levels 40 and 42. The
  **Plasma Arc** does not heat the mirror: an arc strike on a standing mirror's face is absorbed and ends the chain,
  and a jump from a neighbour may reach the open flank (the shared [reflect](../../README.md#reflect) rule's arc row,
  the [weapon's](../../../player/weapons/plasma-arc/README.md) proposal); the open question is closed.
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): **Q1 c** (the mirror's `reflect`:
  non-beam shots bounce, a beam held on it breaks it in about 1.0 s, a `shield-breaker` hit or the EMP shatters it, 3
  s down, the flanks open; a the roster's rule and b the beam-blocking mirror rejected), Q9 a (the regular livery) and
  the document approved as drafted (D.9; `design: approved`); **A.2 a**, "crescent", chosen (`art: chosen`; b "facets"
  in `concept/rejected/`).
