---
title: Halo Platform
design: approved
implementation: done
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../bosses/iron-sovereign, ../rail-bunker, ../../../world/jovian]
updated: 2026-11-02
---

# Halo Platform

## Summary

The Callisto fortress's centrepiece gun mount: a ring of six turret segments turning slowly round a shielded core.
Its gun and rail turrets fire only while the ring carries them through the arc that faces the ship, every turret lost
speeds the ring up, and the core's barrier drops when half the ring is gone. One per Level 41, as its held arena.

## Design

### Stat block

Rendered from the [data.yaml](data.yaml) (M10 part G, step G2: the approved stat block of round 72 as data; the ring's
frames by the Iron Sovereign's segment rule, QG-1 pending round 79). Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at its
first level from the proposed Act 6 reference DPS (judgement call J1, decided 2026-10-31 (round 72): L41 = 164): a turret's 180 is
1.1 s (a `large` unit's part, 1–3 s), the core's 480 is 2.9 s; the whole 1,560 is ≈ 16 s at the boss rule's
0.6 × the reference on paper; **measured**, the halt lasts **≈ 13 s** at medium (12.9–13.5 s, `levelReport` with the
plan's fit, three seeds; easy ≈ 8 s, hard ≈ 15 s): the HP stay the approved ones, the doc follows the measure (step
G5). Fire rates are tuned in Level 41 with `levelReport`.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy (a machine; a held set piece on the boss engine: the EMP Burst stuns it 3 s, a boss never) |
| Layer | `ground` (a fortress mount seen from above, under the play plane: every weapon hits its parts, `anti-ground` ×2; not hardened; the ship flies over it) |
| Size tier | `large` |
| Size | 240×240 px, hitbox 60×60 (the `large` tier's top); the core an octagon 76 px across (hitbox 60×60 on the centre); the ring's band at radius 75–118, a turret's hitbox 34×34 at radius 96 |
| Parts | core (`vital`), 6 turrets (`destroyable`), alternating round the ring: 3 **gun turrets** and 3 **rail turrets**; the ring's segments themselves take no hits |
| Orientation | `fixed` (the ring turns as one 6-fold model, each segment placed at its exact angle from a top-lit set: the Iron Sovereign's segment rule, QG-1 pending round 79); each turret `64 angles`, tracking the ship |
| HP | turret 180 each · core 480; whole 1560 (easy 1170 / hard 2028, from the global multipliers) |
| Armour / shield | the core's **barrier** (a red hex dome over the core): the core takes no damage while 4 or more turrets stand, shots spark on it; no hex [enemy shield](../../README.md#enemy-shields) anywhere, not hardened |
| Speed | 0 px/s (scrolls with the fortress; the level halts the scroll over it) |
| Movement | `terrain` (anchored on its mount, its core at (240, 190) on the halted screen); the ring turns **20°/s** clockwise, **+8°/s per turret lost**, each speed-up eased over 0.8 s (60°/s with five lost) |
| Attack | a turret fires only while its mount lies inside the **120° arc** of the ring centred on the bearing from the core to the ship (the [Iron Sovereign](../../bosses/iron-sovereign/README.md#arena)'s fire arc, all round, no clamp). **gun turrets** an `aimed` 3-shot burst every 2.4 s, the shots 0.12 s apart, standard 160 px/s, `medium` 6; tell: the barrels stop tracking and the muzzles glow 0.3 s; **rail turrets** the [Rail Bunker](../rail-bunker/README.md)'s `laser-line` (its 1 s red line, `heavy` hit, 8 px band live 0.2 s, held while the ship is within 100 px) every 4.5 s; at most one line from the platform telegraphed at a time, ≥ 1.5 s apart; the line locks at its telegraph's start and leads the ring's turn (the Sovereign's rail rule); **open core** (the barrier down) a `ring` of 10 slow bullets (100 px/s, `small` 4) every 3 s; tell: the reactor eye flares white 0.5 s |
| Formations | solo set piece (one per level, never with a second large mount on the screen) |
| Weak points | none (the turrets, destroyable parts; the core once its barrier is down) |
| Effective traits | `anti-ground`, `piercing`, `area` |
| Credits | 120 (score 1200 × chain) whole; parts: turrets 10 each, core 60; the core's death pays every turret not yet paid |
| Death | the core's death: the reactor eye blows out, the remaining turrets explode in turn round the ring (0.15 s apart), then a `large` mechanical explosion with a gold-white flash and a debris ring; the ring stops and the burnt mount stays as a wreck |
| First level / used in | L41; Callisto Fortress (the citadel's centre, its held gate) |
| Difficulty hooks | easy: ring 15°/s (+6°/s per loss), gun bursts of 2 every 3.0 s, rail every 5.5 s, no open-core ring; hard: ring 24°/s (+10°/s), bursts of 4 every 2.0 s, rail every 3.6 s, open-core ring of 12 every 2.4 s |
<!-- /data -->

### Behaviour

- **Rotation is the threat** (the roster's idea, kept from round 07): the platform sits still, the ring carries its
  guns in and out of the arc that faces the ship, so the test is where the ship stands. The arc is drawn: a faint red
  band on the ring's hull over its live sector, and a turret's running lights switch gold → red as it enters (the
  Sovereign's look, so the player who beat Level 35 reads it at once). A turret outside the arc holds fire but can
  still be shot: the arc gates its weapons, not its hitbox.
- **Speed-ups.** Every turret lost speeds the ring up by 8°/s, eased over 0.8 s (no jump: the r07 rule). With
  five lost the last turret sweeps the arc quickly; by then the core is open and the fight is the core.
- **The barrier.** The core's red hex dome holds while four or more turrets stand; when the **third** turret falls
  it fades out over 0.5 s with a falling hum and a klaxon, and the core's reactor eye shows. It is a structural gate,
  not a hex HP pool: the EMP Burst and `shield-breaker` do not open it (decided, trivia: otherwise the special skips
  half the fight).
- **Fire rules.** The Sovereign's: no enemy bullet spawns within 72 px of the ship
  ([bullet rules](../../README.md#bullet-readability-rules)); enemy fire never hurts the platform's parts; a rail turret
  over which the ship hovers skips its shot.
- **Held arena (draft for Level 41).** The level halts the scroll over it (Level 29's rescue halt: the level clock
  stands while the core lives; L41's document owns the section, its waves and timings). The platform stops with its
  core at about (240, 190) in the 480×540 field, so the ship band below the ring (y > 310) stays open; its
  checkpoint is the section's, none on hard.
- **EMP.** Stunned 3 s: the turrets and the open core hold fire; the ring keeps turning (its mass); the barrier stays.
  The only set piece the EMP Burst stuns (a held piece, a machine; a boss is never stunned): the ring stuns it once as it
  reaches its centre, a second burst restarts the 3 s; a rail line told or live is cancelled without a shot and the IFF
  spoofer's held volleys are lost (step I1).
- **Not under a dome.** A [Shield Pylon](../shield-pylon/README.md)'s dome never covers the platform (its barrier is
  the gate there); the level may place pylons on the approach.
- **Code fit.** The Iron Sovereign's ring code (a rotating ring of mounts, the fire arc, the rail lead) and the Rail
  Bunker's `laser-line`; a single ring with destroyable mounts round a gated core is the new part (gap analysis
  §1.3: "rotating ring of batteries, arc facing the ship, built (L35)").

### Livery

The heartland's elite look (Q9 **a**, recommended, decided 2026-10-31 (round 72); the [enemies](../../README.md#factions-and-visual-language)
README owns the rule): **gloss black and heavy gold**, red running lights, a strong top-left rim light (Callisto's
low Sun and Jupiter's light); the chosen r07 model is kept (judgement call **J8**, decided 2026-10-31 (round 72)) and only its
production frames move to the heartland livery. The barrier is a red hex dome; its glow and the arc's band are smooth
(≥ 32 steps, no ordered dither, drawn once: the 2026-10-30 dither audit). A readability check over Level 41's darkest
ice and the black citadel deck comes with the frames.

### Sounds

Brief for the round-72 sound proposals (Freesound CC0/CC-BY, real recordings, no bubbly or popping sounds): the
ring's turn reuses the Iron Sovereign's ring grind (a loop, pitched up a little with each speed-up); the gun turrets
the Ascendancy's medium autocannon; the rail turrets the [rail line](../../../audio/sfx/README.md)'s charge and
crack (reused); the barrier's collapse a descending electrical hum cut off by a heavy relay clunk; the open core's
ring a short rising whine; the death the turrets' chain of explosions into a large metal blast.

## Concept art

The design's look was chosen in round 07 (smooth rotation; the r06 variant rejected for its jagged rotation). The
files moved here from the [ground roster](../README.md#concept-art) on 2026-10-31. Rejected variants:
[concept/rejected/README.md](concept/rejected/README.md). Prompts: [concept/prompts.md](concept/prompts.md). Round 72
re-renders it in the heartland livery (step ART-R-A, J8).

| File | What | Status |
|---|---|---|
| [concept/halo-platform-r07-a.png](concept/halo-platform-r07-a.png) | Halo Platform r07 — ring as one 6-fold model at 128 frames per 60° step (768 per turn, ~0.9 px edge step), turrets at 64 headings; comparison with r06 and a rotation-over-time chart (sheet) | chosen |
| [concept/halo-platform-r07-a.gif](concept/halo-platform-r07-a.gif) | Halo Platform r07 — smooth rotation with eased speed-ups as turrets die, shield drops | chosen |

Round 72 (step ART-R-A, J8, picked 2026-10-31; generator `tools/concept/enemies_r72.py halo-platform`): the chosen r07 model re-rendered in the elite livery, only what changes (the livery, the rail turrets of the draft, the arc band and the turrets' gold → red lights, the smooth red barrier), a held arena on Level 41's citadel deck.

| File | What | Status |
|---|---|---|
| [concept/halo-platform-r72-a.png](concept/halo-platform-r72-a.png) | Halo Platform a, "gold trim": gloss black, the r07 gold rails, the mount rings and end caps gold, the strong rim; gun and rail turrets, the core held and flaring, the readability check on the citadel deck, the measured gold share | chosen (round 72 A.6 a) |
| [concept/halo-platform-r72-a.gif](concept/halo-platform-r72-a.gif) | The held arena: the live arc following the ship, gun bursts and rail lines from mounts in the arc, three mounts lost (eased speed-ups), the barrier dropping, the open core's rings | chosen (round 72 A.6 a) |

Production art for concept round 79 (M10 part G, step ART-G1; generator `tools/art/halo_platform.py`, frames held in
`build/art-held/sprites/` until the platform's `data.yaml` claims the root; layouts, blending and pivots in the script's
header). **QG-1** (round 79, pending the user's review): the ring is drawn by the Iron Sovereign's **segment rule** (one
segment rendered top-lit at 0–45° in 87 steps, the outer edge moving ≤ 1 px a step, placed at its exact angle by quarter
turns and a mirror; the Sun's light and the elite rim as static overlays) instead of r07's 128 ring frames per 60° (about
two atlas pages): the whole platform is 35 % of one 2048² page. Prompts: [concept/prompts.md](concept/prompts.md#halo-platform-final-r79-a).

| File | What | Status |
|---|---|---|
| [concept/halo-platform-final-r79-a.png](concept/halo-platform-final-r79-a.png) | Final frames: the platform at rest, with three mounts lost and the core open, and wrecked over Level 41's citadel deck (1×); the segment set, the gun and rail turrets at 64 headings and their wrecks, the core (shut, waking, open, the white flare) and its wreck, the red hex barrier and its hit, the eye's glow and flare, the muzzle and coil tells, the lamps, the cook-off, debris and the eye's blowout, the overlays (rim, shade, scorch) and the arc strip; key frames of the fight; gold 37 % of the ring (sheet) | proposed (round 79) |
| [concept/halo-platform-final-r79-a.gif](concept/halo-platform-final-r79-a.gif) | The held arena at 1×: the live arc following the ship (lamps gold → red), gun bursts with their muzzle tell, a rail line's charge and lock, three mounts lost with their eased speed-ups, the barrier fading at the third, the eye's flares and rings of 10, the death (the blowout, the cook-off chain round the ring, the large blast) and the wreck | proposed (round 79) |
| [concept/halo-rotation-final-r79-a.png](concept/halo-rotation-final-r79-a.png) | QG-1's proof: the ring turning at 20°/s and 60°/s, ten consecutive 60 fps frames of a quarter at 1:1 and five at 3×; the outer edge steps 0.64 / 1.92 px a frame (the true motion), at most 1.0 / 2.0 px, never more than 0.5 px off the true heading; the rule's turned and mirrored frames against direct renders off the set (≤ 0.5 % of the pixels differ in shape, mean colour difference ≤ 4.3 of 255) (sheet) | proposed (round 79) |
| [concept/halo-rotation-final-r79-a.gif](concept/halo-rotation-final-r79-a.gif) | The ring at 20°/s and 60°/s side by side, 50 fps, 1× | proposed (round 79) |
| [concept/halo-capture-r79-a.png](concept/halo-capture-r79-a.png) | Game capture (step G5, Level 41 at medium, the plan's fit, real time; 2× of the play field's top): the Halo's halt with its red barrier up over the core, and the barrier dropped after the third turret fell (the open core's eye) | proposed (round 79) |

## Implementation

- [x] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers applied; tuned with `levelReport` in Level 41
- [x] The ring of six destroyable mounts round a gated core: the ring's speed-ups eased, the barrier dropping at the third loss, the core's death chain
- [x] The fire arc (all round), the gun turrets' bursts, the rail turrets' `laser-line` with the lead, one line at a time
- [x] The held arena in Level 41 (the scroll halts, the clock stands while the core lives)
- [x] Production frames in the heartland livery (the r07 model, the ring's segments placed at their angle by the Iron Sovereign's segment rule (QG-1, pending round 79), turrets at 64 headings, the wreck), the arc band, the barrier, the intel portrait
- [x] The EMP Burst's 3 s stun (*Behaviour*): the turrets and the open core hold fire, the ring turns on, the barrier stays (M10 part I, step I1: a held set piece only, `PieceStun`; the running rail line cancelled, the spoofer's held volleys lost; `HaloPlatformTest`)
- [x] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds)

## Open questions

- None open.

## Decisions

- 2026-09-30: added in the enemy variety pass (ground roster).
- 2026-10-01: round 06's rotation rejected as jagged; round 07's dense angle set chosen ("rotation now smooth").
- 2026-10-31 (Act 6 design phase, step D-enemies-B; decided 2026-10-31 (round 72)): promoted from the
  [ground roster](../README.md) to a draft spec; its concept files moved here from the ground part's `concept/`
  (r07 chosen, r06 in `rejected/`).
- 2026-10-31 (decided by me, trivia): three gun and three rail turrets, alternating, so the arc always holds both
  kinds; the roster's "speeds up as turrets die" is +8°/s per loss (r07's mockup used +14°/s, too fast for a
  readable rail lead at five losses); the barrier is a gate (no EMP or `shield-breaker` effect); the core's death
  pays the turrets still standing; a held arena rather than a scrolling target, as Level 29's clamps; 240 px across
  (round 06's mockup was ≈ 300 px, which the size tiers would call `huge`; the roster's tier `large` kept).
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): Q9 a (the heartland livery) and J8
  (the r07 design kept and re-rendered in the heartland livery, with the checkpoint at Level 41's halt) as drafted;
  the document approved (D.10; `design: approved`); **A.6 a**, "gold trim", chosen (`art: chosen`; b "gold decks" in
  `concept/rejected/`).
- 2026-11-02 (M10 part G, step G2): the stat block as [data.yaml](data.yaml), rendered above: a held set piece
  (`boss.kind: held`) on the Iron Sovereign's ring engine with the turrets' own attacks, the all-round bearing, the
  losses' eased speed-up and the tells (steps G1a/G1b's keys); the orientation row now names the segment rule (QG-1)
  instead of r07's 128 frames per 60°. Trivia (pending the user's review in round 79): the spin-up 1 s from the halt,
  the par 16 s (the typical halt; a held piece pays no Boss rush bonus), the death chain 2 s, the ring's first turret
  straight down. Open: the doc's EMP stun is not built (bosses are never stunned in the simulation; the gap check
  missed it): a later sim step.
- 2026-11-02 (M10 part G, step G5): built (steps G1b, G2, G3, ART-G1, G4) and captured in the game for round 79
  (`concept/halo-capture-r79-a.png`, the barrier up and dropped; the whole halt in Level 41's real-time recording). The
  measured halt (≈ 13 s at medium) replaces the paper's ≈ 16 s in the prose; the data's typical halt and par (16 s) stay.
  Decided here, no round row (trivia; steps G1b, G2 and ART-G1's "pending round 79" items): the turrets hold fire in the
  barrier's 0.5 s fade; a volley once begun fires even if its turret has turned out of the arc; a spoofed rail line
  locks on the ship's place at its telegraph's start; the spin-up, the par 16 s, the 2 s death chain and the first
  turret straight down as G2 set them; the intel portrait's hull lightened 3×. Left: the EMP's 3 s stun (a later
  simulation step).
- 2026-11-02 (M10 part I, step I1; decided by me, trivia): the EMP's 3 s stun built for the held piece only (no boss
  is stunned): a second burst restarts it (as a unit's), a rail line told or live when it hits is cancelled without a
  shot, and the volleys the IFF spoofer holds are lost (they would all fire at once at the stun's end). The game shows
  no crackle on it yet and plays its power-down at a Vrell unit's level (the event's value is -1, no unit kind): a game
  step's follow-up. The `data.yaml` header's "Open" note is stale.
