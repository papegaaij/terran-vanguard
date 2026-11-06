---
title: Rift Skater
design: approved
implementation: done
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../art-direction, ../../../world/vrell-space, ../../../campaign/act-7-beyond-the-gate]
updated: 2026-11-05
---

# Rift Skater

## Summary

A small Vrell skirmisher shaped like a flat manta of crystal chitin (Level 44, inside the gate). It rides the
distorted space's current in streams and **phase-jumps** (`teleport`) a short way along the drift: a ring of violet
light marks the arrival point half a second ahead, and it fires a `burst` of 3 the moment it lands. Never closer than
120 px to the ship.

## Design

### Stat block

**Draft** (Act 7 design phase, step m11-d-enemies-a, 2026-11-04; decided 2026-11-05, round 84, Q1 = a). A
hand-written table: there is no `data.yaml` yet. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP in damage units (a Pulse Cannon L1 shot does 2). A `small`
unit: the **beam era's factor** from its first level (×2.5 from Level 43: step B3's draft, pending the user's review
in round 89), no act HP factor (the balancing basis's rule).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (`space`); Vrell, biomechanical: the [EMP Burst](../../../player/specials/README.md#emp-burst-l15) stuns it 1.5 s (a Skater mid-jump is not on the screen and misses the burst) |
| Size tier | `small` |
| Size | 40×32 px, hitbox 26×20 |
| Parts | single |
| Orientation | `16 angles` (it faces its glide) |
| HP | 16 (easy 12 / hard 21, from the global multipliers) |
| Armour / shield | none; HP 16 base flies at **32** with the beam era's ×2 (≈ 0.18 s at the L44 reference of 176) |
| Speed | 140 px/s gliding (plus the [distorted space](../../README.md#distorted-space)'s sideways drift, as every unit) |
| Movement | `teleport` over a glide: enters in a stream, glides **1.2 s** along its stream's path, then jumps; after each landing it glides **1 s** and jumps again; **3 jumps**, then it glides out through the nearest edge downwind. A jump: **90–130 px** toward the drift's side and down its frame (30–60° off its frame's down); with no drift (|angle| < 5°) the jumps alternate sides. The shared [`teleport`](../../README.md#movement-pattern-vocabulary) rules: fade out 0.2 s (hittable), gone 0.5 s while the **violet ring** forms at the arrival point (shrinking onto it: round 84's pick, A.2 a) (not hittable, no contact damage), land; the arrival is never inside **120 px** of the ship, never in the 80 px band at the ship's edge of the field, never within 24 px of a side edge: a jump that would break a rule is shortened or turned to the other side |
| Attack | `burst` of **3** aimed shots 0.15 s apart, **0.25 s** after each landing (the muzzle glint its tell), standard 160 px/s, `small` = 4; aimed on the screen (the distorted-space rule: they cross the ship's position whatever the drift). Contact `small` = 10 |
| Formations | stream (4–6) (a stream of 4–6, 0.5 s apart, from the top or from the side the drift comes from, edge-warned 3 s ahead as any side wave); at most 8 on the screen, at most 4 arrival rings at once (the shared rule) |
| Weak points | the violet core under the shard's centre (drawn only) |
| Effective traits | `spread`, `homing` |
| Credits | 14 (score 140 × chain) |
| Death | `small` organic burst: the shard splinters into four violet flakes that fade (presentation) |
| First level / used in | L44; Crossing, distorted space; later Act 7 levels as the level drafts place it |
| Difficulty hooks | easy: ring 0.7 s, burst of 2, 2 jumps; hard: 4 jumps (the burst stays 0.25 s after landing: the shared floor, step A2a) |
<!-- /data -->

### Behaviour

- **The play.** The ring shows where it will be; the burst follows at once. A spread weapon covering the ring
  kills it as it lands; the beam must be moved onto the ring before the burst. Homing shots aimed at a vanished
  Skater pick a new target (the shared rule), so a stream asks for spread or for patience.
- **With the drift**: it glides with the current like everything else in the event plane and jumps with it, so a
  stream reads as a shoal sliding across the field. During a reversal it keeps its frame (spawned before the
  reversal: as before; after: flipped, entering by travel), L20's rule.
- **Look** (Q10 b, J5: a manta-like shard, not a water strider, so it does not count as insectoid): a flat
  rust-red chitin diamond with swept wing tips and a violet core (the gunners' aimed-shot glow), translucent crystal
  edges that catch the event plane's light; the arrival ring a thin violet-white circle over 0.5 s, shrinking onto the
  landing point (round 84's pick, A.2 a: variant a; the growing variant b rejected), never a flash.
- **Sounds** (brief for the audio step, Freesound CC0/CC-BY): a short dry air-rush for the jump out and a reversed
  one for the landing (a real whoosh, never a pop or a bubble), the burst a dry triple snap.

### Difficulty and level use

- Level 44 introduces it alone (a stream of 4 with the drift calm), then in the drift, then with Coilwyrms.
- No more than two streams jumping at once; the rings' 4-at-once cap holds per level.

## Concept art

Round 84's proposals (step ART-R-A, M11 design phase, picked 2026-11-05; generator `tools/concept/enemies_r84.py rift-skater`). Each sheet holds the source render, the sprites at play size with their states, the readability check over Level 44's brightest and darkest band beside the player (a stand-in of the level's light in the one Vrell palette, Q10 b), and key frames; each GIF is the 480×540 play field. Prompts: [concept/prompts.md](concept/prompts.md). Its Look row says the ring *shrinks* to the landing point, its stat block that it *grows*: a shows the first, b the second, for the user to pick.

| File | What | Status |
|---|---|---|
| [concept/rift-skater-r84-a.png](concept/rift-skater-r84-a.png) | Rift Skater a, "manta": a rounded rust chitin manta with a thin crystal rim and the violet core; 16 headings; the jump (glide, fading, the ring shrinking onto the landing point), the death's four flakes, its outlined shot | chosen (round 84 A.2 a) |
| [concept/rift-skater-r84-a.gif](concept/rift-skater-r84-a.gif) | A stream of five riding the event plane's drift (the layers sliding at their parallax): jumps toward the drift's side, the rings, the bursts of 3 on landing, the ship covering the rings for its kills | chosen (round 84 A.2 a) |
| [concept/rift-skater-final-r89-a.png](concept/rift-skater-final-r89-a.png) | Production frames (`tools/art/gate_units.py skater`, M11 part B, step ART-B): the 16 headings with the core glow, the burst's glint, the death's four flakes and their violet light, ART-A's shrinking ring and the landing (sheet) | proposed (round 89) |
| [concept/rift-skater-final-r89-a.gif](concept/rift-skater-final-r89-a.gif) | A Skater gliding, the glint before its burst, a death's flakes, over Level 44's light at 2× | proposed (round 89) |
| [concept/teleport-capture-r88-a.png](concept/teleport-capture-r88-a.png) | Game capture (A4 rig): stand-in Skaters (ART-B's held frames) fading out and the shrinking ring before each landing | proposed (round 88) |
| [concept/teleport-capture-r88-a.gif](concept/teleport-capture-r88-a.gif) | The same, 6 s | proposed (round 88) |
| [concept/skater-drift-capture-r89-a.gif](concept/skater-drift-capture-r89-a.gif) | Game capture, Level 44 (M11 part B, step B5): the first Skater stream with its landing rings (t=19–26) and a stream jumping with the drift as it swings to +25° (t=46–54), 15 s | proposed (round 89) |

Production (round 89, M11 part B, step ART-B): `tools/art/gate_units.py skater` renders round 84's a "manta" unchanged; the fade and the shrinking arrival ring are ART-A's `teleport-fade`/`teleport-ring` ([enemies](../../concept/prompts.md)). Held in `build/art-held/sprites/` until this unit's `data.yaml` exists (step B2). Its intel portrait is in the [hangar](../../../ui/hangar/README.md#concept-art) (`intel-final-r89-a`).

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [x] Stat block in a `data.yaml`; global multipliers and the beam era; checked with `levelReport` at L44 (M11 part B,
  step B2, 2026-11-05; the hard hook's burst 0.20 s after landing is not built: the shared 0.25 s floor, step A2a)
- [x] The shared `teleport` (fade, ring, arrival rules, retargeting) and the glide between jumps; deterministic and
  allocation-free (M11 part A, 2026-11-05, step A1a: `Teleporting`, `TeleportTest`, `Act7AAllocationsTest`; the look `TeleportLooks`, step A3a)
- [x] The drift's frame (its glide and its jumps follow the [distorted space](../../README.md#distorted-space) rules) (M11 part A, 2026-11-05, step A1a: the `Current` pass, `CurrentTest`)
- [x] Production sprites (16 headings, the fade, the ring, the death flakes) via `tools/art/`; sounds (Freesound,
  CC0/CC-BY) (M11 part B, 2026-11-05: `tools/art/gate_units.py` (ART-B), `SkaterLooks` and the cues (B4); in the
  game in round 89)

## Open questions

None open: round 84's items were decided 2026-11-05 (see *Decisions*).

## Decisions

- 2026-11-04 (step m11-d-enemies-a): drafted for the Act 7 design phase from the roster row and the M11 gap analysis
  (§2.2: small, streams, the 0.5 s violet ring, never inside 120 px, a burst of 3 after each jump, ×2 beam era; J5:
  drawn as a manta-like shard). The glide times, the 3 jumps, the jump length and angle, the 0.25 s burst delay and
  the 14 cr bounty are this step's. Decided 2026-11-05, round 84.
- 2026-11-04 (step m11-d-act, decided 2026-11-05, round 84): the stat block (ring grows) and the Look row (ring shrinks)
  disagreed; both now say "per round 84's pick" (ART-R-A's a shrinks, b grows).
- 2026-11-05, round 84 (the user's page answers; decided as drafted unless stated, the alternatives rejected). The open items were:
  - **The arrival ring** (round 84, with the art pick): shrinking onto the landing point (a, the Look row's first
    draft) or growing out of it (b, the stat block's first draft); the shared `teleport` rule takes the pick.
  - Q1 (round 84, the distorted space it rides) is in the [enemies' open questions](../../README.md#open-questions).
- 2026-11-05 (user, round 84 closed): **A.2 a "manta"** chosen (a rounded rust chitin manta with a thin lilac crystal rim); its arrival ring therefore **shrinks** onto the landing point (the Look row's draft; b "shard" with a growing ring rejected, moved to `concept/rejected/`); **D.10 approved**; the burst sound is S.8 a, the jump set S.1 b and S.2 b.
