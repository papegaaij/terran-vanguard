---
title: Minelayer
design: approved
implementation: done
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../world/belt]
updated: 2026-10-30
---

# Minelayer

## Summary

An Ascendancy mine ship that comes up from behind, turns across the field ahead of the ship and lays
a line of proximity mines with **one gap**. Level 30's minefield is made of its lines: find the gap,
or shoot a mine to blow a stretch of the line open.

## Design

### Stat block

Approved in round 60 (drafted in the Act 5 design phase, step D-enemies-B); rendered from
[data.yaml](data.yaml) since M9 part C (step C2). Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2),
set at its first level from the proposed Act 5 reference DPS (judgement call J2: L30 124): 110 HP was
0.9 s; step FIX6 (the user's playthrough 2026-10-30) made it **280 HP (2.3 s)** with a fast rise, so it lays its
line before a typical fit kills it (an accepted exception to the medium class's 0.4–1.5 s, decided 2026-10-30 in round 70). Fire rates and line counts are tuned per level with `levelReport` in the build.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy (machine: the EMP Burst stuns it longest) |
| Layer | `air` (`space` in vacuum: the same layer) |
| Size tier | `medium` |
| Size | 88×64 px, hitbox 64×44 |
| Parts | single |
| Orientation | `16 angles` |
| HP | 280 (easy 210 / hard 364, from the global multipliers) |
| Armour / shield | none (not on the [enemy shields](../../README.md#enemy-shields)' Act 5 list) |
| Speed | 600 px/s rising, 200 px/s across while laying |
| Movement | `rear-entry` (edge-warned 3 s) in a side lane (70 px from a side edge, beside the ship, not through it), rising to its **lay row** (set per unit in the wave; default 100 px and 180 px from the top), then `strafe`s across the field to the far edge and leaves; a column's layers enter **2.5 s apart** (see *Difficulty and level use*) |
| Attack | `mine`: while it strafes it lays a line of **3 clusters of 4 mines** (32 px apart in a cluster, 56 px between clusters) with **one gap of 112 px** at the wave's gap position (one of the two cluster boundaries); a mine (1 HP) arms after **1 s**, then an armed mine within **48 px** of the ship's centre blinks **0.6 s** and bursts into a `ring` of **8** (slow 100 px/s, `small` 4) with a **40 px** `heavy` blast; touched while armed it bursts at once (contact `medium` 6, plus its ring); shot, it sets off the mines within 44 px (0.08 s a hop: its cluster); mines stay where they are laid (they move with the scroll), so the line **slopes** (below) |
| Formations | column (2–3) from the rear, each laying its own line |
| Weak points | none (the gold mine hopper at its tail is drawn only) |
| Effective traits | `rear`, `homing`, `spread`, `piercing` |
| Credits | 30 (score 300 × chain); mines 0 |
| Death | `medium` mechanical explosion with sparks; its unlaid mines are lost with it (a line it has not finished keeps its gap and has a second one where it stopped) |
| First level / used in | L30; the minefield; later Act 5 levels may use it (their docs decide) |
| Difficulty hooks | easy: a wider gap (144 px, the cluster beside it 3 mines), rings of 6; hard: a column of 3 (three lines), rings of 10, arming 0.8 s (the gap stays 112 px: a narrower one would sit inside the 48 px proximity of both edge mines) |
<!-- /data -->

### The mine line (Q3 a)

The numbers of the shared [mine lines](../../README.md#mine-lines) rule:

- **Clusters and one gap** (C1 a, as Level 30 lays them). A full line across the 480 px field is **12
  mines in three clusters of 4**, 32 px apart within a cluster and 56 px between two clusters; the **one
  gap** of 112 px sits at one of the two cluster boundaries (left-centre at x≈160 or right-centre at
  x≈320), so its middle is 56 px from both edge mines, outside the 48 px proximity. The wave sets the
  gap's position (authored per wave, so a level reads) and the lay row, so lines read as a maze that
  scrolls down onto the ship.
- **The line slopes.** Mines hold their place in the field while the layer strafes, so a line laid in
  ≈ 2.4 s (480 px at 200 px/s) slopes down toward the side the layer started from: **≈ 22° at Level 30's
  80 px/s** (≈ 28° at 150 px/s before step FIX6). It stays one barrier with one gap; there are no level rows of mines.
- **Arming.** A laid mine shows a dim light for its first second: unarmed, touching it does nothing
  and it never bursts on its own. Armed, it blinks red-gold once a second.
- **Proximity burst.** An armed mine within 48 px of the ship blinks fast for 0.6 s, then bursts: a
  ring of 8 slow bullets from its spot. Flying through the gap at its centre stays outside 48 px of
  both edge mines (the 112 px gap).
- **Shot mine** (1 HP; C1 a): it bursts at once with its ring and **sets off the mines within 44 px**,
  hop by hop (≈ 0.08 s a hop) with a flash and debris but **no ring**, so **one shot clears its whole
  cluster of 4** and stops at the 56 px space to the next cluster: a 208 px hole between the
  neighbouring clusters' mines. Shooting a line open from far away is the reward for watching ahead, and
  which cluster to clear is a choice.
- **What picks a mine.** Every shot hits a mine in its path, but homing shots and Rook never pick a
  mine as their target (the player chooses where to open a line).
- **Limits.** At most 40 mines alive (three lines of 12 = 36); an unfinished line is not topped up.

### Behaviour

- **From behind.** The column's edge warning (3 s) marks the lane it rises in; the Minelayer streaks up
  beside the ship at 600 px/s (contact `medium` 15 if touched) and lays about 0.8 s after it enters, so the line
  starts before a gun can follow it; `rear` and rear-`homing` guns (the
  [Swarm Tail](../../../player/weapons/swarm-tail/README.md), Q12 c) and the front guns get it while it lays.
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

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

Round 60 proposals (a/b), made in the gloss-black look of Q11 a and chosen below (production re-renders the chosen one in gunmetal/charcoal, Q11 b) (step ART-P-units, `tools/concept/enemies_r60.py`): a gloss-black
hull with gold trim and red running lights (Q11 a), a gold mine hopper at the tail; the mine as a small
black-and-gold disc with a red-gold blink.

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (gloss black; the user chose look b, gunmetal/charcoal with gold, in round 60, so the chosen variant is re-rendered in gunmetal in production), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 30 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/minelayer-r60-a.png](concept/minelayer-r60-a.png) | Minelayer A, hopper barge — a wide flat black hull with a gold edge band, two engine sponsons, a gold mine drum at the stern; mine: a black ball with four gold spikes and a red lamp; strong rim (sheet) | chosen (round 60) |
| [concept/minelayer-r60-a.gif](concept/minelayer-r60-a.gif) | Minelayer A, hopper barge — a wide flat black hull with a gold edge band, two engine sponsons, a gold mine drum at the stern; mine: a black ball with four gold spikes and a red lamp; strong rim (motion) | chosen (round 60) |

## Implementation

- [x] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers applied; tuned with `levelReport`
- [x] Rear entry, rise in a side lane, the strafe along the lay row (deterministic, allocation-free) (step C1: `MineLaying`, `MineLineTest.aLayerRisesFromBehindInItsLaneWarnedAtTheBottomThenLaysItsLineAcrossItsRow`; the staggered 2.5 s column entry)
- [x] Mine lines with one gap, arming, the proximity blink and ring, the shot mine's chain through its cluster (the `Mine` pool extended) (steps C1–C3: `MineLines`, `MineLineTest`, `Level30Test`; the chain reach 44 px, step C3)
- [x] Homing shots and Rook skip mines as targets (step C1: `Homing` never picks a mine; `RookHazards.clearOfMines`, `MineLineTest.rookFliesTheGapWithTheShipAndSetsNoMineOff`)
- [x] Production sprites, the mine sprite and its blink, the intel portrait (frames: `tools/art/belt_units_c.py layer`, step ART-C: `minelayer`, `-laying`, `minelayer-mine`, `-lit`, `-glow`, `-blast`, `-pop`, `pivots/minelayer.json`; held in `build/art-held/` until this unit's `data.yaml` exists; drawn by C4; accepted in round 63, 2026-10-28; the intel portrait is C4's) (step C4: `MineLooks` (dim, armed, the blink and glow, the blast and the chained pop) and the layer's look; the intel portrait by `tools/art/intel.py`; seen in round 63's captures)
- [x] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds) (step C4: the turbine's drone lower (round 60's pick), the drop as round 63's **a** (picked) and the arm as its **a** proposal (b picked; the game copies it into `assets/sfx/` in its next step), the blink and burst round 60's picks, the chain ripple 0.08 s after a burst; `Level30SoundsTest`; the picks of round 63 closed 2026-10-28)

## Open questions

- Q3 (decided in round 60): **a** recommended (lines with one gap, proximity burst, a shot mine sets off
  its neighbours); the alternatives are **b** (static contact mines as walls, no burst: a weaving test
  only) and **c** (seeker mines drifting toward the ship: one more homing threat beside the SAMs and
  Hornets).
- Q11 (decided in round 60, **b**, not the recommendation): the Ascendancy look on the belt is gunmetal/charcoal hulls
  with gold (≥ 20 % gold, rim light, red running lights); a (gloss black) and c (black silhouettes) rejected
  ([enemies](../../README.md#factions-and-visual-language)).

## Decisions

- 2026-10-27 (step D-enemies-B): drafted for the Act 5 design phase from the roster row and the gap
  analysis; every number is a first draft, approved in round 60.
- 2026-10-27 (decided by me, trivia): a chained mine bursts without its ring, so a line can be shot open
  without filling the screen with rings; mines pay nothing (a line is not a credit farm); homing shots and
  Rook skip mines.
- 2026-10-27, design phase: reconciled with Level 30 (step m9-d-act; approved in round 60):
  the line is **three clusters of 4** with one 112 px gap at a cluster boundary, and a shot mine chains hop
  by hop through its cluster (C1 a; it replaces the even 36 px spacing, the 2-mine gap and the one-step
  chain that cleared 3); easy's wider gap is 144 px. Mines pay **0** (the L30 gap check's 1 credit
  dropped). Lines are laid **sloped** (≈ 28° at Level 30's 80 px/s), not in level rows (accepted). Two
  layers' lines keep ≥ 120 px apart by the column's **2.5 s staggered entry**, not by their lay rows
  (100/180 px).
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`); art A.8 **a**, "Minelayer A, hopper barge", chosen (`art: chosen`), the other variant in `concept/rejected/`. Q11 **b** (gunmetal/charcoal with gold, not gloss black): the chosen variant is re-rendered in gunmetal/charcoal in this part's production art, keeping the ≥ 20 % gold and the readability check.
- 2026-10-28 (step ART-C, production art; accepted in round 63, 2026-10-28): A.8 a re-rendered in gunmetal/charcoal with gold (Q.11 b, gold about 20–23 %), 88×64 on a 96 px canvas at 16 headings plus a laying set (the drum's hatch lit). The mine (24×24, about 20 px across) has its lamp enlarged from the concept so the dim/lit blink reads at 1×, lit in the homing diamond's crimson with a white-gold core; its sprites are named `minelayer-mine-*` so this unit's data claims them (SpriteUse maps `pattern: mine` to `spore-mine` today: C4 draws a line mine with these). The proximity blast's ring reaches the 40 px blast radius in its 4th frame; a chained mine's pop has no ring.
- 2026-10-28 (step C2, the data; accepted in round 63, 2026-10-28): the stat block as data: the `lay` movement (lane 70 px in), the line mine (`mine` with `proximity`, `blink`, `blast`, `chain`, `hop` and `line`, life 30 s: longer than a line stays on the screen, a line mine never bursts on its own), easy's and hard's line changes as the new hook `mine` (`gap`, `middle_short`, `arm`). The mine's `credits` 0. The README's "mine 1" HP row moved into the attack's text (the HP cell renders the unit's).
- 2026-10-28 (step C3, Level 30's tests; accepted in round 63, 2026-10-28): the chain reach is **44 px**, not
  40: a layer strafes at 150 px/s, so at Level 30's first section (120 px/s, the t=20 line) two neighbours of a
  cluster lie 32 px across and 26 px down the slope, 41 px apart, and a shot mine set off nothing (`Level30Test`).
  44 px chains a cluster at every scroll the level uses (41 px at 120, 38 at 100, 36 at 80) and still stops at the
  56 px space to the next cluster, so one shot clears its cluster of 4, as C1 a decided.
- 2026-10-28 (user, [round 63](../../../concept-rounds/round-63/README.md), closed): the Minelayer and its mine accepted (**F.1**); **C.4** the chain reach 44 px accepted; sounds: **S.1 b** (`mine-arm` the two-note chirp), S.2 a (`mine-drop`), S.3 a (`mine-chain`).
- 2026-10-30 (step FIX6, the user's playthrough 2026-10-30: "the minelayers are too weak, you can easily take them
  out before they lay any mines at all"): measured on Level 30 with a pilot that flies under each layer from its
  entry with the beam on it (the plan's fit and the user's: Ion Beam L4, Micro-missile L5, Autocannon L5, Tail Gun
  L3), every layer died in 1.2–1.9 s, before it laid a mine (its 3.9 s rise at 120 px/s). Now it **rises at 600
  px/s** (lays ≈ 0.8 s after entering), **strafes at 200 px/s** (the line in ≈ 2.4 s) and has **280 HP** (easy 210,
  hard 364): focused, every layer lays at least its first cluster and the first (t=20) its line through the gap
  (easy 7 of 11, medium 10 of 12, hard all), most are still killed before they leave on easy and medium, and under
  the autopilot all 15 lines of medium are finished (`Level30Test.theMinelayersLayTheirLinesBeforeATypicalFitKillsThem`).
  Tried: 300 HP at 300/240 px/s (the first laid 4 on easy), 260 HP at 400/360 (unkillable: the strafe outran the
  ship), 240 HP at 400/200 (4 on easy). The **bounty 30** (26 before): the medium class's top for its 2.3 s time
  to kill (`BalanceTest.ACCEPTED`, decided 2026-10-30 in round 70). A shot mine still clears its cluster: at 200 px/s two
  neighbours lie 32 px across and 19 px down at Level 30's 120 px/s, 37 px apart.
