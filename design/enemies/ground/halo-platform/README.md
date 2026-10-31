---
title: Halo Platform
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../bosses/iron-sovereign, ../rail-bunker, ../../../world/jovian]
updated: 2026-10-31
---

# Halo Platform

## Summary

The Callisto fortress's centrepiece gun mount: a ring of six turret segments turning slowly round a shielded core.
Its gun and rail turrets fire only while the ring carries them through the arc that faces the ship, every turret lost
speeds the ring up, and the core's barrier drops when half the ring is gone. One per Level 41, as its held arena.

## Design

### Stat block

**Draft** (Act 6 design phase, step D-enemies-B; decided 2026-10-31 (round 72)). A hand-written table: there
is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at its
first level from the proposed Act 6 reference DPS (judgement call J1, decided 2026-10-31 (round 72): L41 = 164): a turret's 180 is
1.1 s (a `large` unit's part, 1–3 s), the core's 480 is 2.9 s; the whole 1,560 is ≈ 16 s at the boss rule's
0.6 × the reference, the length of a held arena. Fire rates are tuned in Level 41 with `levelReport`.

| Field | Value |
|---|---|
| Faction | Jovian Ascendancy; a **machine** (stunned 3 s by the EMP Burst) |
| Layer | `ground` (a fortress mount; the ship flies over it) |
| Size tier | `large` |
| Size | 240×240 px (the `large` tier's top); the core an octagon 76 px across (hitbox 60×60 on the centre); the ring's band at radius 75–118, a turret's hitbox 34×34 at radius 96 |
| Parts | core (`vital`), 6 turrets (`destroyable`), alternating round the ring: 3 **gun turrets** and 3 **rail turrets**; the ring's segments themselves take no hits |
| Orientation | the ring turns as one 6-fold model (the [smooth slow rotation](../../../art-direction/README.md) rule: r07's 128 frames per 60° step); each turret `64 angles`, tracking the ship |
| HP | turret 180 each · core 480; whole 1,560 (easy 1,170 / hard 2,028, from the global multipliers) |
| Armour / shield | the core's **barrier** (a red hex dome over the core): the core takes no damage while 4 or more turrets stand, shots spark on it; no hex [enemy shield](../../README.md#enemy-shields) anywhere, not hardened |
| Speed | 0 px/s (scrolls with the fortress; the level halts the scroll over it) |
| Movement | `terrain`; the ring turns **20°/s** clockwise, **+8°/s per turret lost**, each speed-up eased over 0.8 s (60°/s with five lost) |
| Attack | a turret fires only while its mount lies inside the **120° arc** of the ring centred on the bearing from the core to the ship (the [Iron Sovereign](../../bosses/iron-sovereign/README.md#arena)'s fire arc, all round, no clamp). **Gun turret**: an `aimed` 3-shot burst every 2.4 s, the shots 0.12 s apart, standard 160 px/s, `medium` 6; tell: the barrels stop tracking and their muzzles glow 0.3 s. **Rail turret**: the [Rail Bunker](../rail-bunker/README.md)'s `laser-line` (the 1 s red line, `heavy` 10, an 8 px band live 0.2 s), every 4.5 s; at most **one** line from the platform telegraphed at a time, lines ≥ 1.5 s apart; the line locks at its telegraph's start and **leads** the ring's turn (the Sovereign's rail rule); it holds fire while the ship is within 100 px. **Open core** (the barrier down): a `ring` of 10 slow bullets (100 px/s, `small` 4) every 3.0 s; tell: the reactor eye flares white 0.5 s |
| Formations | turret nest (solo centrepiece): one per level, never with a second large mount on the screen |
| Weak points | the turrets (destroyable parts); the core once its barrier is down |
| Effective traits | `anti-ground`, `piercing` (a line through two turrets at the ring's edge), `area` |
| Credits | 120 whole (score 1,200 × chain); parts: turrets 10 each, core 60; the core's death pays every turret not yet paid |
| Death | the core's death: the reactor eye blows out, the remaining turrets explode in turn round the ring (0.15 s apart), then a `large` mechanical explosion with a gold-white flash and a debris ring; the ring stops and the burnt mount stays as a wreck |
| First level / used in | L41 (Callisto Fortress, the citadel's centre) |
| Difficulty hooks | easy: ring 15°/s (+6°/s per loss), gun bursts of 2 every 3.0 s, rail every 5.5 s, no open-core ring; hard: ring 24°/s (+10°/s), bursts of 4 every 2.0 s, rail every 3.6 s, open-core ring of 12 every 2.4 s |

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

## Implementation

- [ ] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers applied; tuned with `levelReport` in Level 41
- [ ] The ring of six destroyable mounts round a gated core: the ring's speed-ups eased, the barrier dropping at the third loss, the core's death chain
- [ ] The fire arc (all round), the gun turrets' bursts, the rail turrets' `laser-line` with the lead, one line at a time
- [ ] The held arena in Level 41 (the scroll halts, the clock stands while the core lives)
- [ ] Production frames in the heartland livery (the r07 model, 128 frames per 60°, turrets at 64 headings, the wreck), the arc band, the barrier, the intel portrait
- [ ] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds)

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
