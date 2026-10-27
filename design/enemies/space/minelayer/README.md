---
title: Minelayer
design: draft
implementation: not-started
art: proposed
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../world/belt]
updated: 2026-10-27
---

# Minelayer

## Summary

An Ascendancy mine ship that comes up from behind, turns across the field ahead of the ship and lays
a line of proximity mines with **one gap**. Level 30's minefield is made of its lines: find the gap,
or shoot a mine to blow a stretch of the line open.

## Design

### Stat block

**Draft** (Act 5 design phase, step D-enemies-B; pending the user's review in round 60). A hand-written
table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2),
set at its first level from the proposed Act 5 reference DPS (judgement call J2: L30 124): 110 HP is
0.9 s. Fire rates and line counts are tuned per level with `levelReport` in the build.

| Field | Value |
|---|---|
| Faction | Ascendancy; a **machine** (stunned 3 s by the EMP Burst) |
| Layer | `space` (the `air` layer in vacuum) |
| Size tier | `medium` |
| Size | 88×64 px, hitbox 64×44 |
| Parts | single |
| Orientation | `16 angles` |
| HP | 110 (easy 82 / hard 143, from the global multipliers); mine 1 |
| Armour / shield | none (not on the [enemy shields](../../README.md#enemy-shields)' Act 5 list) |
| Speed | 120 px/s rising, 150 px/s across while laying |
| Movement | `rear-entry` (edge-warned 3 s) in a side lane (40–100 px from a side edge, beside the ship, not through it), rising to its **lay row** (set per unit in the wave; default 100 px and 180 px from the top), then `strafe`s across the field to the far edge and leaves; a column's layers enter **2.5 s apart** (see *Difficulty and level use*) |
| Attack | `mine`: while it strafes it lays a line of **three clusters of 4 mines** (32 px apart in a cluster, 56 px between clusters) with **one gap of 112 px** at the wave's gap position (one of the two cluster boundaries); a mine arms after **1 s**, then an armed mine within **48 px** of the ship's centre blinks **0.6 s** and bursts into a `ring` of **8** (slow 100 px/s, `small` 4); touched while armed it bursts at once (contact `medium` 6, plus its ring); mines stay where they are laid (they move with the scroll), so the line **slopes** (below) |
| Formations | column (2; hard 3) from the rear, each laying its own line |
| Weak points | none (the gold mine hopper at its tail is drawn only) |
| Effective traits | `rear`, `homing`, `spread`, `piercing` (the mines) |
| Credits | 26 (score 260 × chain); mines 0 |
| Death | `medium` mechanical explosion with sparks; its unlaid mines are lost with it (a line it has not finished keeps its gap and has a second one where it stopped) |
| First level / used in | L30 (the minefield); later Act 5 levels may use it (their docs decide) |
| Difficulty hooks | easy: a wider gap (144 px, the cluster beside it 3 mines), rings of 6; hard: a column of 3 (three lines), rings of 10, arming 0.8 s (the gap stays 112 px: a narrower one would sit inside the 48 px proximity of both edge mines) |

### The mine line (Q3 a)

The numbers of the shared [mine lines](../../README.md#mine-lines) rule:

- **Clusters and one gap** (C1 a, as Level 30 lays them). A full line across the 480 px field is **12
  mines in three clusters of 4**, 32 px apart within a cluster and 56 px between two clusters; the **one
  gap** of 112 px sits at one of the two cluster boundaries (left-centre at x≈160 or right-centre at
  x≈320), so its middle is 56 px from both edge mines, outside the 48 px proximity. The wave sets the
  gap's position (authored per wave, so a level reads) and the lay row, so lines read as a maze that
  scrolls down onto the ship.
- **The line slopes.** Mines hold their place in the field while the layer strafes, so a line laid in
  ≈ 3.2 s (480 px at 150 px/s) slopes down toward the side the layer started from: **≈ 28° at Level 30's
  80 px/s**. It stays one barrier with one gap; there are no level rows of mines.
- **Arming.** A laid mine shows a dim light for its first second: unarmed, touching it does nothing
  and it never bursts on its own. Armed, it blinks red-gold once a second.
- **Proximity burst.** An armed mine within 48 px of the ship blinks fast for 0.6 s, then bursts: a
  ring of 8 slow bullets from its spot. Flying through the gap at its centre stays outside 48 px of
  both edge mines (the 112 px gap).
- **Shot mine** (1 HP; C1 a): it bursts at once with its ring and **sets off the mines within 40 px**,
  hop by hop (≈ 0.08 s a hop) with a flash and debris but **no ring**, so **one shot clears its whole
  cluster of 4** and stops at the 56 px space to the next cluster: a 208 px hole between the
  neighbouring clusters' mines. Shooting a line open from far away is the reward for watching ahead, and
  which cluster to clear is a choice.
- **What picks a mine.** Every shot hits a mine in its path, but homing shots and Rook never pick a
  mine as their target (the player chooses where to open a line).
- **Limits.** At most 40 mines alive (three lines of 12 = 36); an unfinished line is not topped up.

### Behaviour

- **From behind.** The column's edge warning (3 s) marks the lane it rises in; the Minelayer flies up
  beside the ship (contact `medium` 15 if touched), so `rear` and rear-`homing` guns (the
  [Swarm Tail](../../../player/weapons/swarm-tail/README.md), Q12 c) get it before it turns, front guns once it lays ahead.
- **Fire.** It fires nothing but its mines.
- **EMP.** Stunned 3 s it stops laying (the line keeps the hole) and drifts with the scroll.
- **Readability.** The mines' red-gold blink and the ring's slow bullets follow the
  [bullet readability rules](../../README.md#bullet-readability-rules); mines are drawn on the play
  plane, above the drifting rocks.

### Difficulty and level use

- L30 is space only (no ground layer): its Minelayers and Rail Serpents enter from behind, at most 35 %
  of the waves non-front, each edge-warned 3 s (the level doc owns the waves).
- Never more than 3 unbroken lines on the screen at once; consecutive lines keep at least 120 px
  between them, so the ship can line up for the next gap. The default lay rows (100 and 180 px) are only
  80 px apart: the rule is met by **the staggered entry**, a column's layers entering **2.5 s apart**
  (Level 30's rule; a third layer on hard lays at the 260 px row), so the first line has scrolled on
  before the second is laid.

### Sounds

Brief for the round-60 sound proposals (Freesound CC0/CC-BY, real recordings, no bubbly or popping
sounds): a heavy low turbine drone for the ship; each mine drop a dull metal clunk; the arm a short
dry electronic tick (a synthesised beep is fine); the proximity blink a faster tick; the burst a sharp
metallic blast; the chain a quick ripple of smaller blasts.

## Concept art

None yet: proposals (a/b) in round 60 (step ART-P-units, `tools/concept/enemies_r60.py`): a gloss-black
hull with gold trim and red running lights (Q11 a), a gold mine hopper at the tail; the mine as a small
black-and-gold disc with a red-gold blink.

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (pending the user's review), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 30 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/minelayer-r60-a.png](concept/minelayer-r60-a.png) | Minelayer A, hopper barge — a wide flat black hull with a gold edge band, two engine sponsons, a gold mine drum at the stern; mine: a black ball with four gold spikes and a red lamp; strong rim (sheet) | proposed |
| [concept/minelayer-r60-a.gif](concept/minelayer-r60-a.gif) | Minelayer A, hopper barge — a wide flat black hull with a gold edge band, two engine sponsons, a gold mine drum at the stern; mine: a black ball with four gold spikes and a red lamp; strong rim (motion) | proposed |
| [concept/minelayer-r60-b.png](concept/minelayer-r60-b.png) | Minelayer B, catamaran — two slim black hulls with gold noses and tails joined by a gold bridge deck, the gold mine rack between them; mine: a flat black disc with a gold rim, three prongs and a red eye (sheet) | proposed |
| [concept/minelayer-r60-b.gif](concept/minelayer-r60-b.gif) | Minelayer B, catamaran — two slim black hulls with gold noses and tails joined by a gold bridge deck, the gold mine rack between them; mine: a flat black disc with a gold rim, three prongs and a red eye (motion) | proposed |

## Implementation

- [ ] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers applied; tuned with `levelReport`
- [ ] Rear entry, rise in a side lane, the strafe along the lay row (deterministic, allocation-free)
- [ ] Mine lines with one gap, arming, the proximity blink and ring, the shot mine's chain through its cluster (the `Mine` pool extended)
- [ ] Homing shots and Rook skip mines as targets
- [ ] Production sprites, the mine sprite and its blink, the intel portrait
- [ ] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds)

## Open questions

- Q3 (pending round 60): **a** recommended (lines with one gap, proximity burst, a shot mine sets off
  its neighbours); the alternatives are **b** (static contact mines as walls, no burst: a weaving test
  only) and **c** (seeker mines drifting toward the ship: one more homing threat beside the SAMs and
  Hornets).
- Q11 (pending round 60): the Ascendancy look on the belt, **a** recommended (gloss black with gold
  trim, strong rim light, red running lights); alternatives **b** gunmetal with gold, **c** black
  silhouettes with gold accents only.

## Decisions

- 2026-10-27 (step D-enemies-B): drafted for the Act 5 design phase from the roster row and the gap
  analysis; every number is a first draft, pending the user's review in round 60.
- 2026-10-27 (decided by me, trivia): a chained mine bursts without its ring, so a line can be shot open
  without filling the screen with rings; mines pay nothing (a line is not a credit farm); homing shots and
  Rook skip mines.
- 2026-10-27, design phase: reconciled with Level 30 (step m9-d-act; pending the user's review in round 60):
  the line is **three clusters of 4** with one 112 px gap at a cluster boundary, and a shot mine chains hop
  by hop through its cluster (C1 a; it replaces the even 36 px spacing, the 2-mine gap and the one-step
  chain that cleared 3); easy's wider gap is 144 px. Mines pay **0** (the L30 gap check's 1 credit
  dropped). Lines are laid **sloped** (≈ 28° at Level 30's 80 px/s), not in level rows (accepted). Two
  layers' lines keep ≥ 120 px apart by the column's **2.5 s staggered entry**, not by their lay rows
  (100/180 px).
