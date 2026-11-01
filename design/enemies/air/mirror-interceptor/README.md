---
title: Mirror Interceptor
design: approved
implementation: done
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../player/weapons, ../../../player/shields, ../../../art-direction, ../../../campaign/act-6-jovian-storm]
updated: 2026-11-01
---

# Mirror Interceptor

## Summary

An Ascendancy heavy interceptor with a curved **mirror** across its nose (Level 37, Aurelia). Shots fired into the
mirror come back as slow enemy bullets; a beam held on it heats it until it breaks; a `shield-breaker` hit or the EMP
shatters it at once. Its flanks and rear are always open. It enters in pincers from the sides and in ambushes from
the rear, and turns its mirror to the ship.

## Design

### Stat block

**Draft** (Act 6 design phase, decided 2026-10-31 (round 72)). Rendered from its [data.yaml](data.yaml) (M10 part C, step C2). Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at
its first level from the proposed Act 6 reference DPS (judgement call J1, L37 = 148). A `medium` unit: no beam-era
factor; the act HP factor in its later levels. Fire rates are first values: the build tunes them per level with
`levelReport`.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy (machine: the EMP Burst stuns it longest) |
| Layer | `air` |
| Size tier | `medium` |
| Size | 72×68 px, hitbox 52×48 (the mirror a curved plate 76 px wide across the nose) |
| Parts | single |
| Orientation | `16 angles` (it turns its nose and the mirror toward the ship at **60°/s** while it holds its station) |
| HP | 120 (easy 90 / hard 156, from the global multipliers) |
| Armour / shield | the **mirror** ([reflect](../../README.md#reflect)): arc **120°** (±60° of its facing); heat **130** (about 1.0 s of the plan's Ion Beam L5 at L37, 130 DPS; the act HP factor raises it in later levels: ≈ 140 at L40, ≈ 148 at L42); down **3 s** after a break or a shatter, regrowing over 0.4 s; a bounced shot comes back as a `small` bullet at 110 px/s, at most one every 0.15 s. No hex shield |
| Speed | 180 px/s entering and leaving; 40 px/s strafing at its station |
| Movement | enters on a curve to a **station** and faces its flight until it gets there: from a side edge (a `pincer`) to 90–130 px in from that edge, 160–300 px below the top edge; from the bottom edge (a `rear ambush`) to 90–120 px above it, in its lane. It holds the station **7 s**, strafing up to 60 px along it to keep its mirror on the ship, then leaves through the edge it came in by (a rear one up through the nearest side edge) |
| Attack | `burst` of **3** aimed shots 0.15 s apart every **2.4 s**, from two gun pods under the mirror's rim, only while its facing is within 30° of the ship; standard 160 px/s, `medium` = 6; the tell is a **0.4 s red glow** in the pods under the mirror, and it holds its facing through the burst. Contact `medium` = 15. Its other "attack" is the player's own fire, bounced |
| Formations | pincer (2–4), rear ambush (1–3) (1 + 1 or 2 + 2 in a pincer); each first entry from a side or the rear edge-warned 3 s ahead; hulls at least one hull length apart (≥ 80 px between hull edges, the [formation rule](../../README.md#formation-vocabulary) of Act 6); at most 4 on the screen |
| Weak points | the open flanks and the engines (drawn: the mirror's silver edge shows where the arc ends) |
| Effective traits | `shield-breaker`, `side`, `rear`, `beam` |
| Credits | 30 (score 300 × chain); the mirror pays nothing, bounced bullets pay nothing |
| Death | `medium` explosion; the mirror bursts into falling silver shards (presentation, no damage) |
| First level / used in | L37; Aurelia, with `shield-breaker` "first strongly rewarded" there; its later Act 6 levels per the level drafts; the Ascendant's phase-1 panels share its rule (its own document) |
| Difficulty hooks | easy: arc 100°, turn 45°/s, burst of 2 every 2.8 s; hard: arc 140°, turn 80°/s, burst of 4 every 2.0 s; the heat takes the HP lever |
<!-- /data -->

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
| [concept/mirror-capture-r73-a.png](concept/mirror-capture-r73-a.png) | Game capture (the A4 rig; stand-in sprite, A3a's code-drawn plate): the Ion Beam L5 on the mirror over the cream bands, the plate heating to white, the break and its shards, the beam on the hull while it is down, the regrown plate bouncing the pods' bursts, 2×; the plasma arc earthed on its face over round 72's grade | chosen (round 73 F.6 accept) |
| [concept/mirror-capture-r73-a.gif](concept/mirror-capture-r73-a.gif) | Heat, break, down time and regrowth under the beam, 2×, 30 fps | chosen (round 73 F.6 accept) |
| [concept/mirror-interceptor-final-r75-a.png](concept/mirror-interceptor-final-r75-a.png) | Final frames (M10 part C, step ART-C1, `tools/art/aurelia_units.py mirror`; drawn by the game from step C4b): the 16 headings at 1× (hull and plate frames) over round 72's Level 37 deck stand-in; the states at 2× as the game will layer them (standing with the sweep, heat 40 %, 78 % with the cracks, 100 %, broken: the bare grey frame, regrowing 50 % with the shimmer, the pods' tell); the layers (hull, plate, the plate's glow mask, the cracks, the pods); 1× over the bright and the burnt deck beside the ship and a Talon; the plate in a polished steel-silver (sheet) | proposed (round 75) |
| [concept/mirror-interceptor-final-r75-a.gif](concept/mirror-interceptor-final-r75-a.gif) | A pincer from both side edges to their stations (the flanks on the way in), turning to the ship at 60°/s: the right one's pod tells and bursts of 3, the left one under the beam heating, cracking, breaking (shards), down 3 s and regrowing; both leave by their edges | proposed (round 75) |
| [concept/mirror-capture-r75-a.png](concept/mirror-capture-r75-a.png), [.gif](concept/mirror-capture-r75-a.gif) | In Level 37 (step C5, the [recipe](concept/prompts.md#mirror-capture-r75-a)): the t=38 pincer bouncing a Scatter Vulcan's fan back as magenta shots; the plan's Ion Beam L5 heating the left one's plate and the Tesla Coil Pod shattering it, the kill | proposed (round 75 F.1) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [x] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP and heat re-measured with `levelReport` at L37 (step C2, 2026-11-01: heat 130 is 1.0 s of the plan's Ion Beam L5 at 130 DPS, easy 0.75 s, hard 1.3 s; at most 4 on the screen and 2 a side on every difficulty)
- [x] The [reflect](../../README.md#reflect) rule: the arc test, the bounce (rate cap, mirror-image path, `small` 110 px/s, never twice), beam heat and cooling, the breaker and EMP shatter, the 3 s down time and regrowth, Rook's shots absorbed; deterministic and allocation-free (M10 part A, 2026-10-31: steps A1a, A1b, A3a, `ReflectTest`; the look on stand-in sprites)
- [x] Side and rear entries to a station, the turn toward the ship, the burst only while facing the ship; hull spacing in pincers and ambushes (steps A1a, C1: `MirrorStationTest`; step C2: the `movement.station` key)
- [x] Production sprites (16 headings, the mirror's sweep, heat, cracks, break and regrowth) via `tools/art/`; death effect, bounty and sounds (Freesound, CC0/CC-BY) (the frames and the intel portrait: step ART-C1, round 75, `tools/art/aurelia_units.py mirror`, `intel.py mirror-interceptor`; the game's drawing: step C4b; the turbine hum and the pods' burst: step AUD-C's a variants in the game from step C4b, round 75 S.6 and S.7; captured in Level 37: `mirror-capture-r75-a`)

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
- 2026-10-31, round 73: the mirror capture accepted (F.6); calls C.2-C.4 accepted (the plate turns only at its station, `fire_within: 30`, blasts pass a mirror); the heat, break and regrow sounds are chosen (S.1-S.3 a) and wait for the game.
- 2026-11-01 (step ART-C1, pending the user's review in round 75): the production frames from round 72's a, its models
  unchanged at the concept's scale (84 px frames, the plate 76 px across). The plate is its own frame per heading (the
  game draws the plate frame of the mirror's facing, the hull frame follows it) with a white glow mask the heat's colour
  is drawn through and a white crack layer the game tints; the sweep, the shimmer and the shards stay MirrorLooks' code.
  The plate's polish is a steel-silver instead of the concept's pale silver, which read cream on the cream decks; the
  bare frame keeps its grey (no gold rim); the burst's three shots alternate the two pods.
