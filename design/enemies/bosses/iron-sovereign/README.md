---
title: Iron Sovereign
design: approved
implementation: done
art: final
depends-on: [../../../world/belt, ../../../campaign/act-5-the-belt, ../../ground/sam-nest, ../../ground/rail-bunker, ../../ground/sentinel-tower, ../../air/dart-drone, ../../../player/wingmen, ../../../story/characters/vorne, ../../../systems/economy]
updated: 2026-10-30
---

# Iron Sovereign

## Summary

The Act 5 boss (Level 35, Ceres orbit): the Ascendancy battle station that holds the Belt. Seen from
above, three concentric rings turn round an armoured reactor core at different speeds and in different
directions; their batteries (SAM launchers, rail guns, laser emitters) fire only while rotation carries
them into the arc facing the ship. Killing all of a ring's batteries **locks** that ring; with all three
locked the core opens and fights on with spirals, Dart Drone streams and Vorne's composure cracking.
After its death Vorne offers Lancer a job.

**Status: approved** by the user in concept round 60 (2026-10-27; drafted in the Act 5 design phase, step D-boss). It follows the gap analysis's recommended answers, all decided in round 60: **Q7 a** (the
top-down rings; the alternatives under *Variants b and c*), **Q1 a** (the core's hex shield), **Q6 b**
(the core's Dart streams), **Q11 b** (the belt look: gunmetal/charcoal with gold) and judgement call **J2** (Level 35's reference DPS
144). The numbers are in its [data.yaml](data.yaml) (M9 part H, step H2: the stat block below is rendered from it);
the fight is measured and its HP tuned in step H3.

## Design

### Why rotating rings

The roster's idea, kept: a machine whose danger is **geometry turning**, so the test is positioning and
timing rather than dodging a pattern. It is the opposite of the Act 4 boss: the
[Abyssal Maw](../abyssal-maw/README.md) frames the arena and moves its frame at the ship; the Sovereign
sits still above the ship and **rotation** brings its weapons in and out of reach. Three rings turning
against each other line their batteries up in columns now and then (the moment `piercing` pays), and
the station lies on the `ground` layer, so `anti-ground` weapons do double damage (Level 35's traits are
`piercing` and `anti-ground`).

### Stat block (draft)

Numbers at **medium**; HP in damage units (a Pulse Cannon L1 shot does 2); bullet classes (`small` = 4,
`heavy` = 10, `laser` = 8) and contact damage from the [balancing basis](../../README.md#balancing-basis).
The reference DPS is the **proposed** Level 35 value of **144** (judgement call J2, decided in round 60).
The batteries reuse their ground units' attacks: their numbers live in those units' documents and are
not repeated here; the boss sets only where and how often they fire.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy (a machine, but a boss: never stunned, its shields emptied by the EMP Burst) |
| Layer | `ground` (the station seen from above, under the play plane: every weapon hits its parts, `anti-ground` ×2; **not hardened**, unlike the Rail Bunkers on its outer hull; no collision with the ship or the escort, who fly over it freely) |
| Size tier | `huge` |
| Size | 440×440 px, hitbox 70×70 (the outer ring's rim at radius 220 from the centre; the core 80 px across; batteries: SAM 40×40 (hitbox 30×30), rail 48×48 (36×36), laser 44×44 (32×32)) |
| Parts | the **core** (`vital`; armoured while shut, phases 1–3); the **outer ring** (6 SAM batteries), the **middle ring** (4 rail batteries), the **inner ring** (3 laser batteries): every battery `destroyable`; the rings' hulls are **not hit areas** (shots pass over them as over terrain) |
| Orientation | `fixed` (the rings turn about the centre, their segments placed by angle; every battery's turret in `64 angles`, aiming on its own while its mount turns with the ring) |
| HP | SAM battery 220 each, rail battery 1200 each, laser battery 1500 each, core 6,600; the core's hex shield 80 on top (phase 4); total 17220 (easy 12915 / hard 22386, from the global multipliers) |
| Armour / shield | the core is armoured (shots spark grey, no damage) until it opens; open, it carries a **hex shield** of 80 under the [enemy shield rule](../../README.md#enemy-shields) (Q1 a: every weapon wears it down, the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md)'s `shield-breaker` empties it, the EMP Burst strips it) |
| Speed | 0 px/s (the scroll halts over it); the rings turn: outer 6°/s clockwise, middle 10°/s counter-clockwise, inner in steps (120° in 4 s, then a 4 s hold), each lock speeding the survivors up |
| Movement | anchored, its centre at (240, 160) (see *Arena*) |
| Attack | per battery, only while its mount is inside the **fire arc** (120°, see *Arena*): SAM batteries the [SAM Nest](../../ground/sam-nest/README.md)'s salvo (its homing diamonds, its tell), one salvo per battery every 6 s in turn (at most 8 station missiles alive); rail batteries the [Rail Bunker](../../ground/rail-bunker/README.md)'s rail line (its lock, telegraph, band and `heavy` hit), one per battery every 5 s in turn (one telegraphed at a time, ≥ 1.5 s apart); laser batteries the [Sentinel Tower](../../ground/sentinel-tower/README.md)'s arc sweep (its telegraph, beam width and `laser` hit) with a 300 px beam, once per inner-ring hold; the shut core (phases 1–3) an aimed `fan` of 5 over 40° (`small` = 4, 150 px/s) through its shutter slits every 3 s; 2.5 s in phase 2; 2 s in phase 3, each after a 0.5 s glow; the open core (phase 4) draws its attacks from its playbook (see *Phases*): its 2-arm `spiral` (`small`, 130 px/s, the arms turning 72°/s, 5 bullets/s an arm) for 4 s; its rail volley: three of the [Rail Bunker](../../ground/rail-bunker/README.md)'s lines fanned 22° through the ship's spot (its telegraph, band and `heavy` hit), a Dart surge from all four bays, a ring of 8 [Minelayer](../../space/minelayer/README.md) mines and the wreck throw |
| Formations | solo set piece |
| Weak points | none |
| Effective traits | `anti-ground`, `piercing`, `homing` |
| Credits | 433 (score 4330 × chain) in Act 1 terms (core 355, every battery 6); × 6.5536 (Act 5's factor 1.6⁴) × Level 35's `bounty_scale` 0.59 it pays **24 % of budget(35) ≈ 1,674** (see *Bounty*) |
| Death | `huge` vacuum variant: the wrecked batteries cook off ring by ring (0.15 s apart), the core collapses and blows, the rings break into drifting segments; a 5 s chain, screen flash, credit shower (see *Death*) |
| First level / used in | L35; the L49 boss rush's echo (see *Echoes and relatives*) |
| Difficulty hooks | hard: rings ×1.2 faster, fire arc 150°, battery intervals ×0.8, the core's spiral 3 arms, the volley's telegraph 0.8 s (the Rail Bunker's), the Minelayer's hard mines (arming 0.8 s, rings of 10), no boss checkpoint; easy: fire arc 90°, battery intervals ×1.3, the core's spiral 2 arms at 3 bullets/s an arm, the volley's telegraph 1.2 s, the mines' rings of 6 |
<!-- /data -->

### Arena

The scroll **halts** over the station in Ceres orbit ([the belt](../../../world/belt/README.md), Level 35 of
[Act 5](../../../campaign/act-5-the-belt/README.md); the level document places the arena and owns its
timeline). The approach crosses the station's outer hull (its escort screen, the Rail Bunkers, the
mass-relay flash: the level's), and the scroll slows to a stop as the station's centre reaches its
place, so nothing pops in: the station has been scrolling into view with the ground since the hull.
Screen space, the field 480×540, y down:

| Element | Where | Notes |
|---|---|---|
| Core | centre (240, 160), 80 px across | clear of the boss bar's plate (35 px); six armoured shutter petals |
| Inner ring | radius 85 (band 70–100) | 3 laser batteries 120° apart; turns 120° clockwise in 4 s, then holds 4 s |
| Middle ring | radius 140 (band 123–157) | 4 rail batteries 90° apart; 10°/s counter-clockwise |
| Outer ring | radius 200 (band 180–220) | 6 SAM batteries 60° apart; 6°/s clockwise; its top passes off the top edge (y < 0), its bottom reaches y 380 |
| The ship's room | the whole field | the station is `ground`: the ship flies over the rings and the core; below the outer ring (y > 380) the ship band is open |
| Backdrop | deep layer | Ceres's curve far below, the sunlit dust band ([belt parallax](../../../world/belt/README.md#parallax-layers)) |

**The fire arc.** A battery fires only while its mount lies inside a **120° arc** of its ring centred on
the bearing from the station's centre to the ship, that bearing clamped to the lower half (30°–150°, 90°
straight down): the arc faces the ship and follows it. So about two SAM batteries, one or two rail
batteries and at most one laser battery are live at a time, rotation carries them in and out, and the
player steers which part of each ring is armed by moving. A battery outside the arc holds fire but can
still be shot: the arc gates its weapons, not its hitbox. The arc is drawn: a faint red band on each
ring's hull over its live sector, and a battery's running lights switch gold → red as it enters.

**Fire rules.** At most one rail line is telegraphed at a time (the boss staggers them ≥ 1.5 s
apart); a rail line's direction locks at its telegraph's start and its origin rides with its battery as the ring turns
(the Rail Bunker's rule since round 66's C.1 b; round 68's C.9 a, built in step FIX3, accepted in round 68); the line **leads** the ring's turn (round 69's B.1, the user; built in step FIX5): at the lock the
battery's position when the rail fires is predicted from its ring's turning speed, and the line runs from there through
the ship, so the riding line fires through the ship's locked point (the bunker's [lead](../../ground/rail-bunker/README.md#behaviour);
the core's rail, from the still centre, aims straight at the ship); a rail battery holds fire while the ship is within 100 px (the bunker's); no
enemy bullet or missile spawns within 72 px of the ship ([bullet rules](../../README.md#bullet-readability-rules)),
so a battery the ship hovers over skips its shot. At most **8** SAM missiles from the station alive
(a salvo past the cap is skipped). A laser battery fires only during the inner ring's hold, so its
telegraphed wedge never moves under it. Enemy fire never hurts the station's parts.

**Intro** (≈ 8 s): the boss warning with the scroll's last slowing; the station's lights come on ring by
ring, outer → inner, and each ring **spins up** from rest with a grinding rumble (a klaxon from the core);
the bar `IRON SOVEREIGN` fills; **Vorne** speaks first on the enemy channel (an Ascendancy boss is
announced by Vorne as a Vrell boss is by the Choir; see *Vorne*). Invulnerable until the rings reach
speed. The boss checkpoint is taken at the halt (none on hard); the level clock halts while it lives.

### Phases

The phases follow **locks**, not the bar: rings may be locked in any order, and each lock is a
`boss-phase`. The bar shows the station's **total** HP (the batteries' and the core's), so it falls in
every phase and a battery's kill takes its share off it.

| Phase | Ends at | Behaviour |
|---|---|---|
| 1 — Three rings | the first ring locked | All three rings turn; every battery in the arc fires; the shut core's `fan` every 3.0 s. The outer ring's SAMs are nearest the ship band, the inner lasers deepest: the front guns meet the outer batteries first, and a column where the rings' batteries line up lets `piercing` shots take two or three at once. |
| 2 — Two rings | the second ring locked | The locked ring grinds to a halt (2 s, sparks, its lights out, its wrecked mounts smoking) and stays still; the two survivors turn **×1.25** faster (the inner ring's 120° step in 3.2 s, its hold unchanged); the core's `fan` every 2.5 s. |
| 3 — One ring | the third ring locked | The last ring turns **×1.5**; the core's `fan` every 2.0 s. |
| 4 — The core | the core at 0 | The station shudders and every ring goes dark; the core's six shutter petals **open over 3 s** (invulnerable while they open; a klaxon), and its reactor glows white-gold under a gold **hex shield** of 80 (Q1 a). An **armour patch** drops as the phase begins. Open, the core plays its **playbook** (round 70, built by step BOSS70): after a **1.5 s rest** (**0.8 s** below half its HP) it draws its next attack from five with the simulation's seeded random (a replay draws the same), **never the same twice running** and the Dart surge **at most every third draw**; each starts with its own **0.8 s tell** (see *Tells*): the **spiral** (2-arm, `small`, 130 px/s, the arms turning 72°/s, 5 bullets/s an arm, for 4 s), the **rail volley** (three of the Rail Bunker's lines from the petals' rim, 30 px out, fanned **22°** with the middle one through the ship's spot: its telegraph, 8 px band and `heavy` hit, each line once), the **Dart surge** (all four rim bays a stream of **3** at once, 12 Darts, under their cap of 16), the **mine ring** (**8** of the [Minelayer](../../space/minelayer/README.md)'s line mines thrown out from the core in 0.5 s to seeded distances of **120–220 px** at even bearings, each arming, blinking and bursting by the Minelayer's own rule, gone after **8 s** untouched) and the **wreck throw** (**2–3** dead batteries' wrecks, seeded, any ring and side, each glowing 0.8 s, then torn off its socket and hurled at the ship's spot **0.8 s** apart, tumbling at **200 px/s**, **60 HP**, `heavy` on contact; at most **3** in the air; an empty socket is left, and with no wreck left the throw drops out of the pool). |

A lock needs the ring's **every** battery dead; its last battery's kill starts the halt at once. A
locked ring blocks nothing (its hull is no hit area).

**Round 70 (decided 2026-10-30, built by step BOSS70):** the user's playtest asked for longer ring phases and a
shorter, less predictable core phase; the HP shift and the core's five-attack playbook are under
[The round 70 rework](#the-round-70-rework-decided-round-70). What the playbook replaced: the spiral's fixed cycle (its
0.6 s tell and 2 s rest), the bays' timed stream (one bay every 5 s, its 0.5 s tell, the 72 px skip; hard's streams
of 5) and the core's rail every 6 s below half (medium and hard only; the volley fires on every difficulty, its
telegraph the Rail Bunker's: easy 1.2 s, hard 0.8 s). What stays: the opening, the hex shield, the armour patch, the
spiral's bullets, the bays' places and their Darts, and the Rail Bunker's line (its band, live time and hit) under
the volley.

### Dart Drones

Q6 b, decided in round 60: the [Dart Drone](../../air/dart-drone/README.md), launched only in phase 4. A rim
bay's doors open with a gold light for **0.5 s** (the tell), then the stream rises out of the bay onto
the play plane on the Dart's own launch and chase; the Dart doc holds its numbers and its screen cap
(16). As the Maw's shed Eel Swarms, they count into the level's enemies (not its density), pay their own
bounty and get no edge warning (a boss and what it launches never trigger them). They give `spread`
and `area` loadouts their targets while the core's shield regrows.

### Tells

| Attack | Tell | Medium (easy / hard) |
|---|---|---|
| Battery armed | its running lights turn red as it enters the fire arc; the arc's band on the hull | — |
| SAM salvo | the [SAM Nest](../../ground/sam-nest/README.md)'s (the hatch and the red lock light) | its own |
| Rail line | the [Rail Bunker](../../ground/rail-bunker/README.md)'s red line, brightening | its own |
| Laser sweep | the [Sentinel Tower](../../ground/sentinel-tower/README.md)'s crimson wedge (the arc only, no full turn) | its own |
| Inner ring step | the ring's gold teeth flash as it unlocks, a short clank | 0.4 s |
| Core `fan` (phases 1–3) | the shutter slits glow gold | 0.5 s |
| Core opening | the station shudders, the rings go dark, the petals part | 3.0 s (invulnerable) |
| Core `spiral` (playbook) | the reactor flares white (its glow brightening) | 0.8 s |
| Rail volley (playbook) | three petals glow red toward the ship, then the Rail Bunker's three red lines | 0.8 s, then the line's telegraph |
| Dart surge (playbook) | all four rim bays open, lit gold | 0.8 s |
| Mine ring (playbook) | the core's rim lights blink amber | 0.8 s |
| Wreck throw (playbook) | a dead battery's wreck glows gold with sparks (each wreck before its throw) | 0.8 s |

### The escort (Rook)

Rook is back in the escort slot from Level 30 ([wingmen](../../../player/wingmen/README.md)). He follows
his boss rules: he shoots the nearest battery **inside the fire arc** first (then the nearest of the
others; the core once open), steps out of a telegraphed rail line and a sweep's wedge like the ship,
and chases Darts that come within his reach. His state is in the boss checkpoint as usual. With
`--escort none` the fight is the same without him. The test autopilot does the same, so the balance and
playthrough tests can finish the fight.

### Duration and HP

Target: the act-boss window **90–180 s** at medium; the **par** is the measured mean, **145** (round 70, the HP
moved from the core to the rings: [The round 70 rework](#the-round-70-rework-decided-round-70)).

**Measured (step BOSS70, round 70 built):** rail batteries 1,200, laser batteries 1,500, the core 6,600 (the station
17,220), the playbook in the core phase; the autopilot with the plan's Level 35 fit and Rook over five seeds
(`levelReport -Plevel=35 -Pparts=boss -Pseeds=5`; phases: the spin-up and phase 1, phases 2 and 3, the core with its
3 s opening; the shield and armour lost per phase; the playbook's draws over the five fights):

| Difficulty | Fight, s | Phases 1 / 2 / 3 / core, s | Lost | Sweeps struck | Draws: spiral / volley / surge / mines / wrecks |
|---|---|---|---|---|---|
| easy | 108.2 (killed 5/5) | 46.7 / 12.7 / 6.8 / 40.9 | 3 / 0 / 0 / 0 | 2 (at most 1) | 7 / 16 / 12 / 12 / 14 |
| medium | **144.3** (140.1–149.2, 5/5 in the window) | 45.0 / 22.9 / 22.7 / 52.7 | 21 / 18 / 14 / 0 | 33 (at most 8) | 15 / 17 / 8 / 13 / 19 |
| hard | 210.8 (no window) | 50.2 / 48.0 / 33.3 / 78.4 | 32 / 32 / 24 / 2 | 55 (at most 14) | 24 / 26 / 16 / 18 / 24 |

R70's measure on a copy of the data (no playbook) read medium 145.2 s, the rings ≈ 91 s and the core 53.5 s; the
playbook leaves the core's length to its HP (52.7 s). No rail line and no volley line struck the autopilot; the core
phase cost it nothing at medium (it reads the tells: off the volley's lines, out of a mine's 48 px, off a wreck's
path). The longer inner ring's sweeps strike it 6–8 times a fight at medium (0–3 before; `Level35Test`'s bound 3 → 8,
pending the user's review: a sweep centred on the ship at ≈ 195 px leaves no way out at its height, and a planner cost
by the depth in the wedge struck it more, not less). Phase 1 for the calibration's random dodger: **1.50 ×** the
Kraken's (1.18 × before; H.4's band 0.8–1.2, the test's pending band to 1.6; pending the user's review). Without Rook
at medium (two seeds) 154.3 and 147.0 s.

The earlier measures (step H3, the core 14,000, par 150) follow.

**Measured (M9 part H, step H3; decided in round 68, 2026-10-29):** the autopilot with the plan's Level 35 fit
and Rook (`Level35Test`, five seeds) took the draft's station in **77 s** at medium: the rings ≈ 35 s (the spin-up
4 s, phase 1 ≈ 30 s, phases 2 and 3 ≈ 3 s each: the beam's column burns the lowest battery of every ring at once, so
the second and third locks follow the first within seconds) and the core ≈ 42 s. The core was tuned first and alone
(no ring phase runs long): **core 5,000 → 14,000**, the station's total **17,960**; the fight is now **145–155 s on
every seed, the mean 148.7 s**, so the par stays **150**. Easy ≈ 108 s, hard ≈ 248 s (the core ≈ 200 s under three
spiral arms; hard has no window). The ship loses about 10 a fight at medium, no rail line strikes it, a sweep at most
twice. Phase 1 for the calibration's random dodger: **1.18 ×** the Kraken's (inside H.4's 0.8–1.2). The paper
estimate below (0.6 × 144) now reads 209 s; `BalanceTest` lists it as pending.

Paper estimate: the effective boss DPS is 0.6 × 144 = **86.4**. Phases 1–3 at ×0.7 (moving targets, the
choice of battery, shots that pass between batteries over the hulls): ≈ 60.5 on the batteries. Phase 4
at ×0.85 (the shield's regrowths, fire spent on Darts): ≈ 73.4 on the core. `anti-ground` weapons (×2)
and `piercing` columns make it shorter for the loadouts the level asks for: the measurement counts
them, the paper does not.

| Phase (the draft's paper) | HP | Time |
|---|---|---|
| Intro, the spin-up, Vorne | — | ≈ 8 s |
| 1 — Three rings (the first ring's batteries) | 1,320 | ≈ 22 s |
| 2 — Two rings | 1,320 | ≈ 22 s |
| 3 — One ring | 1,320 | ≈ 22 s |
| The core opening | — | 3 s |
| 4 — The core (+ the shield of 80, regrowing) | 5,000 | ≈ 68 s |
| Total | 8,960 | **≈ 145 s** |

**Specials on a many-part boss.** The specials' boss rules cap their damage **per boss part**, and the
Sovereign has 13 batteries: a Smart Bomb on a full station is worth up to 13 × 150 = 1,950 (≈ 22 % of the
bar; no battery dies of it, every battery's HP is above 150), an Orbital Lance up to 400 a part it crosses
(it kills a SAM or a rail battery, never a laser battery). The draft keeps the specials' rule; part H
measures it (see *Open questions*). **Measured (step H3, QH-1):** on the full station as the rings come up, a Smart
Bomb takes **4.0 %** of the bar (60 a part it reaches) and an Orbital Lance held in one column **2.6–8.0 %** (five
columns), both far under QH-1's 25 %: the per-part caps stay, no total cap.

Lessons from Acts 2–4 the build must apply (M9 part H):
- **Measure, then tune the HP, not the attacks** (Kraken, Spire, Colossus, walker, Maw): the autopilot
  with the Act 5 balance plan's Level 35 fit over five seeds, every seed inside 90–180 s, the par at the
  measured mean; tune the core's HP first, the batteries' only if a ring phase runs long.
- **Phase 1 against the Kraken's** (the calibration band 0.8–1.2×): the levers are the batteries'
  intervals and the core's `fan`.
- The autopilot and Rook must leave a rail line and a sweep's wedge, pick armed batteries first and
  stay out of the core's 72 px when it opens, or the tests cannot finish the fight.

### The round 70 rework (decided, round 70)

The user's playtest (2026-10-30, medium, `m9-pt-notes.md`): *"The last part of the boss is a bit boring. We should add
something extra to it and make it shorter. We can extend the first part (shooting the guns on the rings). These guns can
have some more hitpoints and the core less. We need to add something extra to the core part as well and make it less
predictable."* Drafted in step R70 for [round 70](../../../concept-rounds/round-70/README.md) (A.1, A.2) and **decided there on 2026-10-30** (A.1 ok as
proposed; A.2 "I like a combination of a and b", read as below); **built by step BOSS70** (the measures under *Duration and HP*, the rules under *Phases* and *Tells*).

**1. The HP shift (decided: ok; measured on a temporary copy of the data, the data restored).** The direction is the
user's; the numbers: **SAM battery 220 (kept), rail battery 330 → 1,200, laser battery 440 → 1,500, the core 14,000 → 6,600**; the
station 17,960 → 17,220 (easy and hard by the global multipliers), the bounty unchanged (it does not follow HP). The
autopilot with the plan's fit and Rook (`Level35Test`, five seeds; phases: the spin-up and phase 1, phase 2, phase 3, the
core with its opening; `levelReport`'s test fit in brackets):

| Fight | Difficulty | Fight, s | Spin-up + phase 1 | Phase 2 | Phase 3 | The core |
|---|---|---|---|---|---|---|
| now (as built) | easy | 108.4 [—] | 24.5 | 1.7 | 3.1 | 78.1 |
| now (as built) | medium | 147.1 (144.1–151.9) [—] | 30.6 | 3.7 (2.3–6.9) | 1.5 (0.6–2.2) | 110.4 |
| now (as built) | hard | 252.2 [—] | 44.6 | 2.6 | 2.0 | 202.0 |
| proposed | easy | 106.1 (102.9–108.6) [105.4] | 46.7 | 12.8 | 6.9 | 38.8 |
| proposed | medium | 145.2 (143.6–150.3) [145.4] | 45.0 (44.8–45.0) | 23.0 (20.0–27.9) | 22.7 (20.9–24.0) | 53.5 (52.9–54.3) |
| proposed | hard | 230.8 (228.1–236.1) [229.2] | 50.1 | 48.0 | 33.3 | 98.4 |

At medium the rings last ≈ 91 s (each of phases 1–3 ≥ 20 s on every seed) and the core ≈ 54 s; without Rook (two seeds)
150.1 s (48.5 / 18.6 / 28.1 / 53.9). Every seed inside the window 90–180 s; the **par moves to the measured mean,
145**. Costs: the inner ring's sweeps live ≈ 40 s longer, so they strike the autopilot 6–8 times a fight at medium (now
0–3; `Level35Test`'s bound is 3) and it loses ≈ 50–70 shield and armour instead of ≈ 10: the bound or the autopilot's
wedge dodge (`RingPlay`) moves in the build; easy's phase 3 stays short (4–10 s).

**The column.** The Ion Beam's column does not need the rings to line up: a fixed vertical beam crosses all three ring
bands at once and each ring turns its batteries through it, so all three take damage together whatever their speeds
(the speeds change how often a battery crosses, not how long it spends in the beam); with three rings of equal HP
(1,320 each) the second and third locks followed the first within seconds. It is answered **with the data alone**:
the rings' HP graded outward-in (outer 1,320, middle 4,800, inner 4,500 in all) so the column chips at the inner rings
but cannot lock them early: **outer, then middle, then inner on every seed** (two probes: with the outer ring
unkillable nothing locked before 209 s; with the middle unkillable the inner needed 196 s after the outer). An
alternative with code, not needed by the measure: **shuttered rings** (S): a ring's batteries armoured (grey sparks, as
the shut core) until the ring outside it locks, a strict order with less spread by seed; `RingSpec`/`RingBlock` (a key
such as `armoured_until: {locks: n}`), `SetPieceParts.partShielded`, `SovereignLooks` (the shut batteries' lamps dim)
and `RingPlay`'s targets; today a phase's `exposes` cannot name a battery (the ring batteries are not in `part_list`).

**2. The core phase: the playbook with the wreck throw as its fifth attack (decided: "a combination of a and b").** The
user's answer to the three variants was *"I like a combination of a and b"*; read as **a (the playbook) with b's wreck
throw as a fifth attack in its pool**; c (the core breaks loose) is rejected. Everything is seeded by the sim's random
(the level's seed: a replay plays the same draws); the opening, the hex shield and the armour patch stay as built.
After each attack and a 1.5 s rest (0.8 s below half its HP) the core draws its next from **five**, **never the same
twice running** (a's draft kept the Dart surge to at most every third draw; BOSS70 keeps that unless it measures badly),
each with its own 0.8 s tell, so the player reads what comes instead of counting a loop:

| Attack | Tell (0.8 s) | What it does |
|---|---|---|
| **Spiral** | the reactor flares white, the shield ripples | the built 2-arm spiral, 4 s |
| **Rail volley** | three petals glow red | three of the Rail Bunker's lines fanned 22° through the ship's spot, 1.0 s telegraph, `heavy` |
| **Dart surge** | all four bays light gold | four streams of 3 at once (12, under the Darts' cap of 16) |
| **Mine ring** | the rim's amber lights | 8 of the [Minelayer](../../space/minelayer/README.md)'s mines thrown out to 120–220 px (seeded radii), each arming and bursting by the Minelayer's own rule, gone after 8 s if untouched |
| **Wreck throw** | a dead battery's wreck glows gold with sparks | **2–3 dead batteries' wrecks** (seeded order, any ring and side) torn off their sockets and hurled one after another at the ship's spot, tumbling at **200 px/s**, **60 HP** each (dodge or shoot), `heavy` on contact, at most 3 in the air; an empty socket is left |

The bays' every-5 s stream and the core rail below 50 % fold into the pool (a's draft). The spacing of one throw's
wrecks is BOSS70's to set and measure (b's old 2.5–4 s clock is gone). Code **M**: `RingCore` (the draw, the rest, the
tells), a `core.pool` key (`RingBlock`/`RingRules`/`RingChecks`), the mine ring on the Minelayer's `Mine`, a thrown wreck
as a shootable projectile (as `Missiles`' diamonds), `RingArena` (a wreck gone from its mount), `SovereignLooks` (the
five tells, the socket, the tumbling wreck from the existing wreck frames) and `SovereignCues`, `RingPlay` (reads the
tell, dodges or shoots a wreck). Storyboards (the combination's look is a and b): [concept/sovereign-core-r70-a.png](concept/sovereign-core-r70-a.png),
[concept/sovereign-core-r70-b.png](concept/sovereign-core-r70-b.png), all three side by side in
[concept/sovereign-core-r70-abc.png](concept/sovereign-core-r70-abc.png); c is in [concept/rejected/](concept/rejected/README.md).

**3. The lines the combination needs** (Vorne's own wording stays his document's; to write and record in BOSS70): Rook
at the first rail volley ("Red petals, three lines — move!"), Okafor at the first mine ring ("Mines off the rim, Lancer.
Give the lights room."), Rook at the first wreck throw ("It's throwing its own wreckage at us!"), Vorne at the half
("You read the Sovereign well, pilot. Read faster." or "Nothing on the Sovereign is wasted, Lancer. Not even its dead."),
Okafor after a throw at the half ("Shoot the scrap down or get clear of it.").

### Bounty

An act boss aims at ≈ **24 %** of its level's budget (the economy's share under the perfect-run cap;
Level 21's and Level 28's 24 %). budget(35) = 700 × 1.07³⁴ ≈ **6,985**; 24 % ≈ **1,676 paid**. Data is in
Act 1 terms: **bounty = 1,676 ÷ (6.5536 × `bounty_scale`) ≈ 256 ÷ `bounty_scale`**. Level 35's scale is
the level document's: measured in step H2 at **0.59** ([Level 35](../../../campaign/act-5-the-belt/level-35-iron-sovereign/README.md#credit-budget))
that is **433** (core 355, each of the 13 batteries 6), paid **1,674 = 24.0 %** (this draft first assumed 0.75,
Act 4's Level 28 scale, and 341; the level's hand estimate 0.56 gave 457). The share stays at 24 %. The Darts pay their own bounty, outside the share.

### Death

The core at 0: the reactor flares, and the **wrecked batteries cook off** ring by ring, outer → middle →
inner, 0.15 s apart, each a short vacuum flash with a ring of debris thrown outward (the
[vacuum rules](../../../world/belt/README.md#vacuum-rules): no rising smoke, fire only where a hull holds
air); the rings **crack** at their segment joints and the segments drift apart, slowly turning; the core
**collapses inward** (the petals folding in, a white-gold implosion) and then **blows** in the `huge`
explosion family (the [sound list](../../../audio/sfx/README.md) names the Sovereign's core). 5 s of chain,
then the screen flash (an act boss) and the credit shower over the core.

### Vorne

[Vorne](../../../story/characters/vorne/README.md) is on the enemy channel for the fight (the HUD's
[enemy channel](../../../ui/hud/README.md): amber plate, the steady interference over his portrait; never
urgent). Two or three taunts, then the recruitment; **this document owns their wording**, Level 35 places
and times them (its radio's order and delays). Drafts, decided in round 60:

| When | Line (draft) |
|---|---|
| `boss-arrival` (the intro) | "Welcome to Ceres, Lancer. The Sovereign was built to hold the Belt. Do try to make this interesting." |
| `boss-phase` (the first lock) | "Such dedication. Such waste. You could have been on the winning side, pilot." |
| `boss-phase` (the core opening) | "Enough. You are becoming expensive." (the portrait's *rage* alternate; see below) |

**His composure cracks, he does not shout**: the gap analysis's "Vorne's rage" for phase 4 meets his
character rule (he never shouts until Act 6's end), so the third line is short, cold and clipped, under
the bared-teeth portrait with heavier static, not raised. (Judgement call of this step, decided in round 60.)

**The recruitment** (Vorne's Act 5 beat): after the death chain and Okafor's kill line, Vorne's last
transmission of the act on the enemy channel, steady and warm again: "Lancer. You are wasted on Okafor.
Come to Callisto. Name your price." (his document's sample line); a friendly answer follows (Rook or
Okafor; Lancer never speaks). The station's captured data pointing to Callisto (Varga) comes after it,
so the two name Callisto together. Order from the kill (K), for the level to time: the chain K to K+5,
the flash and the credit shower; Okafor's kill line; Vorne's offer; the answer; Varga's data line; then
the level's end and the act end in Level 28's order.

### Echoes and relatives

- **Level 49's echo** ([Act 7](../../../campaign/act-7-beyond-the-gate/README.md), the boss rush): a
  shortened, single-phase re-creation by the Choir from this boss's assets with a spectral tint. Proposal
  for that level's document: the **outer ring alone** turning round the **open core** (shield, spiral, no
  Darts), the fire arc kept; its HP and par are Level 49's.
- **The Halo Platform** ([ground roster](../../ground/README.md), Act 6, Level 41) reuses the rotating-ring
  mode but stays a different thing: a `large` fortress mount that **scrolls** with the ground in its
  level (no halt, no bar, not a boss), **one** ring of six turret segments round a shielded core whose
  shield drops when half the ring is gone, the ring speeding up as turrets die, and no fire arc. The
  Sovereign is `huge`, halts the scroll, has three counter-turning rings, the fire arc, the locks and the
  core's own phase.

### Variants b and c (for the round's a/b/c)

- **b — Scrolling fortress**: no halt; the level crawls over the outer, middle and inner ring as long
  hull sections (three mini-arenas of batteries in fixed mounts), then halts over the core. Cheaper
  (tileable hull, no rotation mode); loses the rotation and the fire arc, and reads as Level 32's
  galleries again.
- **c — Tilted wheel**: the rings seen as ellipses at an angle; batteries swing from behind the core
  (out of reach, drawn darker) to the front (hittable) as the rings turn. A striking silhouette;
  pseudo-3D art with depth sorting, and a readability risk for what can be hit.

### Presentation

- **Look brief** (Q11 b, the belt look, decided in round 60: gunmetal/charcoal, not the gloss black first drafted; the
  chosen variant b, armoured links, is re-rendered in it in production): a vast disc of **gunmetal/charcoal** hull with **gold trim on at
  least 20 %** of its silhouette (the rings' rims and teeth, the battery collars, the core's petals), a
  strong **top-left rim light** (the belt's sun) and **red running lights** round each ring; the
  Ascendancy crest (a rising sun over a ringed planet) in gold on the core's petals. The rings are
  separated by dark gaps with light-strip catwalks, so each reads as its own wheel; the turning reads by
  the gold teeth on each rim and the red lights moving against each other. Batteries: the SAM Nest's
  rack, the Rail Bunker's coil gun and the Sentinel Tower's emitter in the station's black and gold, so a
  battery reads as the unit the player already knows. A locked ring goes dark (lights out, smoking
  mounts). The open core: a white-gold reactor under the gold hex shield.
- **Drawing the rings**: never a whole ring rotated at runtime (sprites are pre-rendered at every angle,
  [art direction](../../../art-direction/README.md)), and a whole ring at the slow-rotation rule's
  density would far exceed the **one-page** budget of a boss
  ([production budgets](../../../art-direction/production/README.md)). So each ring is built from
  **identical hull segments** (outer 12, middle 8, inner 6) placed at their exact angle each frame, each
  segment's sprite rendered at a dense heading set (its outer edge moving ≤ 2 px a step), the batteries'
  mounts on segments and their turrets at 64 headings. The art step proves the seams and the page count;
  Level 35's whole atlas is measured as Level 28's was (≤ 6 pages, L28 had 7 accepted).
  **Decided in round 60 (A.23, top-lit):** the segments are rendered **once with the light fixed from the top** (45° segments),
  rotated in 90° turns and mirrored, plus a static rim-light overlay: about **0.6 atlas pages** (the full-sun render, 3.1–3.6
  pages, is rejected); the sun gradient no longer travels round the wheel. The chosen look is **b, armoured links** (A.17 b).
- **Readability** (one check per sheet): every battery and the arc's red band over the darkest part of
  the station and over Ceres's backdrop; the station's black must stay distinct from space behind the
  outer ring (the belt's backdrops a step lighter than Act 4's).
- **Music**: the Ascendancy boss theme, [#19 "Iron Sovereign"](../../../audio/music/README.md) (chosen in
  round 03), from the warning; Vorne's motif carries it.

### Sounds brief

Real recordings from Freesound (CC0 / CC-BY), heavy and mechanical; no bubbly or popping sounds. Sounds
play in vacuum as everywhere (the game's convention). The batteries keep their units' sounds.

| Sound | Where / when | Brief |
|---|---|---|
| Spin-up | the intro, each ring starting to turn | a huge bearing grinding up to speed: a turntable or crane slewing ring, pitched down, a rising rumble |
| Ring turning | a low bed while the rings turn | a steady deep mechanical grind with slow metallic ticks (gear teeth) |
| Inner ring step | each 120° step and hold | a heavy ratchet clank, then the grind, then a clunk as it holds |
| Klaxon | the intro, the core opening | an industrial or ship's alarm horn, reverberant |
| Ring lock | a ring's last battery dies | a long metal brake screech and a groan as the ring grinds to a halt, sparks crackling |
| Core shutters | the core opening | massive hydraulic doors: a hiss and a deep sliding thud per petal |
| Core `fan` / `spiral` | the core's guns | a hard mechanical gun burst (autocannon), lighter for each spiral bullet |
| Dart bays | a stream's launch | the Dart doc's pneumatic launch thump, four in a row |
| Shield | the core's hex shield hit, emptied | the enemy shield's sounds (the shared hex-shield set, not a boss sound of its own) |
| Death | the cook-offs, the implosion, the blast | rapid secondary detonations, tearing metal, a deep implosion whoomph, then the `huge` explosion |

## Concept art

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-world; chosen in round 60) — two looks of the
top-down rings (Q7 a) in the belt look of [Q11](../../../world/belt/README.md#open-questions) a (gloss black; production re-renders in gunmetal, Q11 b), each a PNG sheet
(the station at rest, the segments and a seam, the batteries out of and in the arc and wrecked, the core shut,
opening, open under its hex shield, readability crops, key frames, the measured atlas estimate) and a 26 s GIF in
the 480×540 play field (intro, phase 1, a cut to the outer ring's lock, a cut to the core, the death chain); the
rings are drawn from identical segment sprites at 1-degree headings under one fixed sun. Q7's b and c are described
under *Variants b and c* only (no art). Generator `tools/concept/belt_r60.py`; prompts:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/iron-sovereign-r60-b.png](concept/iron-sovereign-r60-b.png) | Iron Sovereign B, armoured links — every segment a separate block with a dark gap and a gold-edged cap plate, the gaps reading the turning (sheet) | chosen (round 60) |
| [concept/iron-sovereign-r60-b.gif](concept/iron-sovereign-r60-b.gif) | Iron Sovereign B, armoured links — the fight and the death in the play field (motion) | chosen (round 60) |
| [concept/iron-sovereign-final-r68-a.png](concept/iron-sovereign-final-r68-a.png) | Production art (step ART-H1, `tools/art/iron_sovereign.py`, A.23 top-lit): the station at rest and in phase 1, the segment sets, the seams at 1:1 at several angles and the turn-and-mirror rule against direct renders, the battery mounts and turrets (64 headings), the core shut / opening / open under the hex shield, the locked look, the death's frames, the bays, readability crops, the measured atlas share (0.65 of a page) (sheet) | chosen (round 68, H.1 ok: accepted as final) |
| [concept/iron-sovereign-final-r68-a.gif](concept/iron-sovereign-final-r68-a.gif) | Production art: the intro (lights ring by ring, spin-up), phase 1 with the arc, the outer ring's lock, the core opening, the death (motion) | chosen (round 68, H.1 ok: accepted as final) |
| [concept/sovereign-spinup-capture-r68-a.mp4](concept/sovereign-spinup-capture-r68-a.mp4) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), clip with sound: the rings lighting and spinning up (under the `WARNING` banner) | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-spinup-capture-r68-a.png](concept/sovereign-spinup-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), frames: the rings lighting and spinning up (under the `WARNING` banner) | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-sweep-capture-r68-a.mp4](concept/sovereign-sweep-capture-r68-a.mp4) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), clip with sound: a laser battery's wedge in the inner ring's hold | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-sweep-capture-r68-a.png](concept/sovereign-sweep-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), frames: a laser battery's wedge in the inner ring's hold | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-rail-capture-r68-a.mp4](concept/sovereign-rail-capture-r68-a.mp4) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), clip with sound: rail batteries' lines from the turning middle ring | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a; stale since FIX3 built C.9 a: shows the lines fixed where they started, the record of the round; re-recorded as `sovereign-rail-capture-r69-a`) |
| [concept/sovereign-rail-capture-r68-a.png](concept/sovereign-rail-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), frames: rail batteries' lines from the turning middle ring | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a; stale since FIX3 built C.9 a: shows the lines fixed where they started, the record of the round; re-recorded as `sovereign-rail-capture-r69-a`) |
| [concept/sovereign-rail-capture-r69-a.mp4](concept/sovereign-rail-capture-r69-a.mp4) | Game capture (step I3; [recipe](concept/prompts.md#iron-sovereign-capture-r69-a)), clip with sound: rail batteries' lines riding with the turning ring, the direction locked (C.9 a as built; round 69 B.1) | chosen (round 69, B.1 ok (the clip shows the pre-FIX5 aim; the lead is built in FIX5)) |
| [concept/sovereign-rail-capture-r69-a.png](concept/sovereign-rail-capture-r69-a.png) | Game capture (step I3; [recipe](concept/prompts.md#iron-sovereign-capture-r69-a)), frames at 1:1: a middle-ring rail battery's telegraph and shot, halt +17.2 to +17.8 s (round 69 B.1) | chosen (round 69, B.1 ok (the clip shows the pre-FIX5 aim; the lead is built in FIX5)) |
| [concept/sovereign-core-capture-r70-a.png](concept/sovereign-core-capture-r70-a.png) | Game capture (step R70; [recipe](concept/prompts.md#sovereign-core-r70)): the open core in phase 4 (the dark rings, Rook, the scatter fit), the base of round 70's storyboards | chosen (round 70: the base of the storyboards) |
| [concept/sovereign-core-r70-abc.png](concept/sovereign-core-r70-abc.png) | The core phase's three variants side by side, five panels each at half size ([brief](concept/prompts.md#sovereign-core-r70)) | chosen (round 70: the three side by side; a and b picked as the combination) |
| [concept/sovereign-core-r70-a.png](concept/sovereign-core-r70-a.png) | Storyboard a, the playbook: spiral, rail volley, Dart surge and mine ring with their tells, two seeds' draws (1:1) | chosen (round 70: the playbook, the combination's look) |
| [concept/sovereign-core-r70-b.png](concept/sovereign-core-r70-b.png) | Storyboard b, the wreck throw: a wreck's tell, the lift, the throw, a hit, two seeds' orders (1:1) | chosen (round 70: the wreck throw, the playbook's fifth attack) |
| [concept/sovereign-playbook-spiral-capture-r71-a.mp4](concept/sovereign-playbook-spiral-capture-r71-a.mp4) | Game capture (step BOSS70; [recipe](concept/prompts.md#sovereign-playbook-capture-r71-a)), clip with sound: the core's playbook, the white flare (the tell), then the 2-arm spiral | proposed |
| [concept/sovereign-playbook-spiral-capture-r71-a.png](concept/sovereign-playbook-spiral-capture-r71-a.png) | Game capture (step BOSS70; [recipe](concept/prompts.md#sovereign-playbook-capture-r71-a)), four frames of the same: the white flare (the tell), then the 2-arm spiral | proposed |
| [concept/sovereign-playbook-rail-volley-capture-r71-a.mp4](concept/sovereign-playbook-rail-volley-capture-r71-a.mp4) | Game capture (step BOSS70; [recipe](concept/prompts.md#sovereign-playbook-capture-r71-a)), clip with sound: the core's playbook, the red petals toward the ship (the tell), the three lines telegraphed 22° apart through its spot, the rails live | proposed |
| [concept/sovereign-playbook-rail-volley-capture-r71-a.png](concept/sovereign-playbook-rail-volley-capture-r71-a.png) | Game capture (step BOSS70; [recipe](concept/prompts.md#sovereign-playbook-capture-r71-a)), four frames of the same: the red petals toward the ship (the tell), the three lines telegraphed 22° apart through its spot, the rails live | proposed |
| [concept/sovereign-playbook-dart-surge-capture-r71-a.mp4](concept/sovereign-playbook-dart-surge-capture-r71-a.mp4) | Game capture (step BOSS70; [recipe](concept/prompts.md#sovereign-playbook-capture-r71-a)), clip with sound: the core's playbook, all four bays lit gold (the tell), the four streams of three Darts | proposed |
| [concept/sovereign-playbook-dart-surge-capture-r71-a.png](concept/sovereign-playbook-dart-surge-capture-r71-a.png) | Game capture (step BOSS70; [recipe](concept/prompts.md#sovereign-playbook-capture-r71-a)), four frames of the same: all four bays lit gold (the tell), the four streams of three Darts | proposed |
| [concept/sovereign-playbook-mine-ring-capture-r71-a.mp4](concept/sovereign-playbook-mine-ring-capture-r71-a.mp4) | Game capture (step BOSS70; [recipe](concept/prompts.md#sovereign-playbook-capture-r71-a)), clip with sound: the core's playbook, the amber rim lights (the tell), eight line mines thrown out to 120–220 px, shot mines bursting into their rings | proposed |
| [concept/sovereign-playbook-mine-ring-capture-r71-a.png](concept/sovereign-playbook-mine-ring-capture-r71-a.png) | Game capture (step BOSS70; [recipe](concept/prompts.md#sovereign-playbook-capture-r71-a)), four frames of the same: the amber rim lights (the tell), eight line mines thrown out to 120–220 px, shot mines bursting into their rings | proposed |
| [concept/sovereign-playbook-wreck-throw-capture-r71-a.mp4](concept/sovereign-playbook-wreck-throw-capture-r71-a.mp4) | Game capture (step BOSS70; [recipe](concept/prompts.md#sovereign-playbook-capture-r71-a)), clip with sound: the core's playbook, a dead battery's wreck glowing gold with sparks (the tell), torn off and hurled tumbling at the ship, the next glowing, the empty sockets | proposed |
| [concept/sovereign-playbook-wreck-throw-capture-r71-a.png](concept/sovereign-playbook-wreck-throw-capture-r71-a.png) | Game capture (step BOSS70; [recipe](concept/prompts.md#sovereign-playbook-capture-r71-a)), four frames of the same: a dead battery's wreck glowing gold with sparks (the tell), torn off and hurled tumbling at the ship, the next glowing, the empty sockets | proposed |
| [concept/sovereign-look-capture-r71-a.png](concept/sovereign-look-capture-r71-a.png) | Game capture (step PIX7; [recipe](concept/prompts.md#sovereign-look-capture-r71-a)): the station with the rim and shade overlays at full size and undithered, phase 1 and the open core at 1:1, and the sheen before / after at 2x | proposed |
| [concept/sovereign-lock-capture-r68-a.mp4](concept/sovereign-lock-capture-r68-a.mp4) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), clip with sound: the inner ring locking: the grind, the lights out, the vented wrecks | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-lock-capture-r68-a.png](concept/sovereign-lock-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), frames: the inner ring locking: the grind, the lights out, the vented wrecks | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-opening-capture-r68-a.mp4](concept/sovereign-opening-capture-r68-a.mp4) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), clip with sound: the core's shutters opening to the reactor | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-opening-capture-r68-a.png](concept/sovereign-opening-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), frames: the core's shutters opening to the reactor | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-spiral-capture-r68-a.mp4](concept/sovereign-spiral-capture-r68-a.mp4) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), clip with sound: the open core's spiral | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-spiral-capture-r68-a.png](concept/sovereign-spiral-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), frames: the open core's spiral | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-bays-capture-r68-a.mp4](concept/sovereign-bays-capture-r68-a.mp4) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), clip with sound: a rim bay's Dart stream (the Ion Beam alone, no Rook) | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-bays-capture-r68-a.png](concept/sovereign-bays-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), frames: a rim bay's Dart stream (the Ion Beam alone, no Rook) | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-death-capture-r68-a.mp4](concept/sovereign-death-capture-r68-a.mp4) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), clip with sound: the cook-offs, the implosion, the white flash and the drift | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-death-capture-r68-a.png](concept/sovereign-death-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), frames: the cook-offs, the implosion, the white flash and the drift | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |
| [concept/sovereign-rings-capture-r68-a.png](concept/sovereign-rings-capture-r68-a.png) | Game capture (step H5; [recipe](concept/prompts.md#iron-sovereign-capture-r68-a)), frames: 1:1 crops of the rings, their batteries and the fire arc's band | chosen (round 68, accepted with the boss fight: C.1 a, C.5 a, C.6, C.9 a) |

## Implementation

To be built in M9 part H with Level 35; round 60 approved the design (2026-10-27).

- [x] Stat block and boss script in a `data.yaml` (the rings, their radii, speeds, directions and steps,
      the batteries and their units' attacks, the fire arc, the core's phases, the Darts, par), loaded;
      global difficulty multipliers applied (step H2 on step H1c's keys; par 150 until step H3 measures it)
- [x] The **rotating-ring mode** (new; gap analysis §2.8, size L): parts whose hitboxes orbit the centre,
      mounts turning with their ring, turrets aiming independently, firing gated by the arc that follows
      the ship; the inner ring's steps and holds; reusable by the Halo Platform (Act 6) and the L49 echo
      (steps H1a–H1c: the simulation's `RingArena`, `RingArenaTest`; step H4: `SovereignLooks`; recorded in round
      68's captures, step H5)
- [x] Ring locks as phase triggers (every battery of a ring dead, any order), the survivors' speed-ups,
      the bar of the station's total (batteries and core); the boss checkpoint keeps the ring angles,
      speeds, locks and the batteries' HP, and a retry from it and a replay repeat the fight exactly (steps H1a–H1b,
      `RingLocksTest`; `Level35Test`'s checkpoint and determinism, step H3)
- [x] The core: armoured while shut and its `fan`; the opening; the hex shield (the
      [enemy shield rule](../../README.md#enemy-shields)); the `spiral`, the Dart streams from the rim bays,
      the core rail below half (step H1b: `PartShields`, `BossLines`, the bays; step H4: the shutters, the reactor
      glow, the shield's look; round 68 C.6 and C.7 for the EMP and the bays' rules)
- [x] Rook and the autopilot: armed batteries first, out of rail lines and sweep wedges, the core's
      72 px kept (Rook step H1b; the autopilot's `RingPlay` step H3)
- [x] The fight measured over five seeds (the window, the HP tuned, the par set), phase 1 checked
      against the Kraken's band, the specials' per-part damage checked, the bounty at ≈ 24 % with Level
      35's scale (step H3, `Level35Test`: the core 14,000, the mean 148.7 s, par 150; phase 1 1.18 ×; the Smart
      Bomb 4 %, the Lance ≤ 8 %; the scale 0.59 kept)
- [x] Intro (the halt, the spin-up, Vorne's line), death (the cook-offs, the implosion, the segments
      drifting, flash, credit shower), Vorne's taunts and the recruitment in Level 35's radio timeline (steps H2 and
      H4, `RadioTimelineLevel35Test`; recorded in round 68's captures, step H5: the spin-up runs mostly under the
      `WARNING` banner, the drifting segments fade within ≈ 1 s of the white flash and the base disc stays ≈ 15 s)
- [x] Production art (accepted in round 68, closed 2026-10-29; built by FIX4; ≤ one atlas page, about 0.6 with the top-lit segments of A.23; the chosen look b in gunmetal/charcoal, Q11 b): the ring segments at their heading sets, the batteries' mounts
      and turrets, the core shut / opening / open, the locked look, the death, the intel silhouette;
      readability checks over Level 35's backdrop (step ART-H1, in the game from step H4; round 68 H.1–H.2; the
      readability over the backdrop in round 68's captures, step H5)
- [x] Round 70 A.1, the HP shift (step BOSS70): rail batteries 1,200, laser batteries 1,500, the core 6,600, the
      station 17,220, par 145 (`data.yaml`, `Level35Test`, `BalanceTest`/`balance.py`'s paper note); the fight
      re-measured (*Duration and HP*: medium 144.3 s, 5/5 in the window)
- [x] Round 70 A.2, the core's playbook (step BOSS70): the simulation's `RingCore` draws (seeded, never twice
      running, the surge at most every third, the rest 1.5 / 0.8 s), `CoreVolley`, the surge from all four bays, the
      mine ring on the Minelayer's `Mine` (thrown), the thrown wreck as a shootable missile (`Missiles.launchWreck`,
      200 px/s, 60 HP, at most 3; the socket empty); the data's `boss.core.pool` (`RingBlock`, `RingRules`,
      `RingChecks`; the schema in [schemas-units](../../../tech/architecture/schemas-units/README.md)); `CorePlaybookTest`
- [x] The playbook's tells and sounds (step BOSS70, `SovereignLooks`, `SovereignCues`; existing sounds, a sounds row
      for a later round): the white flare, the red petals and the volley's three lines, the gold bays, the amber
      rim, the gold wreck with sparks, the empty socket, the tumbling wreck (its battery's wreck frame turning) and
      its break (the cook-off flash and debris); captures `sovereign-playbook-*-capture-r71-a` (proposed)
- [x] The playbook's radio (step BOSS70): Rook at the first volley and the first wreck throw, Okafor at the first mine
      ring and after the first throw, Vorne at the core's half (`first-rail-volley`, `first-mine-ring`,
      `first-wreck-throw`, `core-low`; voiced with `tools/art/voice.py`)
- [x] The autopilot (`RingPlay`) off the volley's lines, out of the thrown mines' proximity and off the wrecks; 3/3 on
      every difficulty (`Level35Test`, five seeds with Rook, two without)
- [x] Sounds (Freesound, CC0/CC-BY) per the brief (round 68's picks, built by FIX3; the cuts are previews until `briefs/freesound-originals.md` is fetched); the music #19 from the warning (steps AUD-H1 and AUD-H2; step H4
      plays the a variants and track 19; round 68 M.1 and S.1–S.12)

## Open questions

All approved by the user in concept round 60:

- **Q7** (the fight): **a** top-down rings (recommended, this document); **b** a scrolling fortress
  crossing the three rings as hull sections, then the core (cheaper, loses the rotation identity);
  **c** a tilted wheel whose batteries swing from behind to the front (pseudo-3D art and depth sorting,
  a readability risk). See *Variants b and c*.
- **Q1** (the core's shield): **a** a regrowing hex shield every weapon wears down, emptied by
  `shield-breaker` and the EMP (this document); **b** a shield only `shield-breaker`, beams and the EMP get
  through (phase 4 would demand the Tesla Coil Pod or a beam); **c** no enemy shields in Act 5 (the core
  just opens, armoured until then).
- **Q6** (popcorn): **b** the core's Dart streams (this document); **a** no Dart Drone (phase 4 is the core
  alone); **c** Talons launched instead (shielded popcorn, against the doctrine).
- **Q11** (the look): decided in round 60, **b** gunmetal/charcoal hulls with gold, red lights, the backdrops a step
  lighter (not the recommendation; it diverges on purpose from the final L28 cruisers); rejected: **a** gloss black,
  **c** black silhouettes with gold accents (cheapest, poor at speed).
- **J2**: Level 35's reference DPS 144 (the HP above follow it).
- Vorne in phase 4: cold and clipped, never a shout (this document), or the gap analysis's "rage" as a
  raised voice (against his character rule, which saves the shout for Act 6's end).
- The specials on a many-part boss: keep the per-part caps (a Smart Bomb worth up to ≈ 1,950 on a full
  station; this document, part H measures), or cap a special's total on one boss. Measured in step H3: 4 % and at most
  8 % of the bar, so the caps stay (QH-1 a; trivia in round 68).
- **Round 70** (decided 2026-10-30): A.1 ok (the HP shift, the par 145), A.2 the playbook with the wreck throw as its fifth attack. Nothing open; step BOSS70 builds it. See *The round 70 rework*.

## Decisions

- 2026-09-30: Drafted in the bosses roster as the Act 5 boss (L35): a battle station with three
  concentric rotating rings whose batteries (rail, SAM, laser) rotate into view; destroy a ring's
  batteries to stop it, stop all rings to expose the core.
- 2026-10-27 (Act 5 design phase, step m9-d-boss; **approved in round 60**): promoted from
  the roster to a draft spec on the gap analysis's recommendations: Q7 a (the top-down rings over Ceres
  orbit, the scroll halted; phases Three rings, Two rings, One ring, the Core), Q1 a (the core's hex shield
  80), Q6 b (Dart streams in phase 4), Q11 b (the look). HP 8,960 (13 batteries 3,960, core 5,000) from the
  proposed L35 reference DPS 144 (J2); paper ≈ 145 s, par 150 until measured; bounty 341 in Act 1 terms
  (24 % of budget(35) ≈ 1,676 paid at a hand `bounty_scale` of 0.75, recomputed with the level's).
- 2026-10-27, design phase: reconciled with Level 35 (step m9-d-act; approved in round 60): the
  bounty is **457** in Act 1 terms (core 379, 13 batteries × 6) at Level 35's hand scale **0.56**, paid 1,677 (24 %
  of budget(35)), not 341 at the assumed 0.75; **this document owns Vorne's Level 35 wording**, the level places
  and times the lines (the sentence that gave the wording to the level corrected).
  Trivia decided here: the rings' hulls are no hit areas; rings lock in any order and each lock is a
  phase; the bar is the station's total; the fire arc follows the ship (clamped to the lower half); the
  inner ring steps and holds so its sweeps never move under their telegraph; the batteries reuse the SAM
  Nest's, the Rail Bunker's and the Sentinel Tower's attacks; the station is `ground` but not hardened;
  Vorne announces the boss and cracks without shouting; the rings drawn from segment sprites for the
  one-page budget; the L49 echo proposed as the outer ring round the open core.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`, D.9): Q7 **a** (the three top-down rings), J.8 (Vorne cold and clipped, never shouting), J.2 (reference DPS 144), Q6 **b**. Art: **A.17 b**, armoured links (every segment a separate block with a dark gap and a gold-edged cap plate; the continuous wheel, a, rejected); **A.23 top-lit** (segments rendered once with the light fixed from the top, 90° turns, mirrored, a static rim overlay: about 0.6 pages, not the 3.1–3.6 of full sun). Q11 **b**: the production art is re-rendered in gunmetal/charcoal with gold. The rings' bed is S.16 a.
- 2026-10-29 (M9 part H, step H2; decided in round 68, 2026-10-29): the boss's `data.yaml` on step H1c's keys (decided as built:
  `laser-line`, the spiral's `run`, `until: {locks: n}`, the battery attacks keep their units' difficulty hooks): the
  three rings with the batteries `from:` the SAM Nest, the Rail Bunker and the Sentinel Tower (300 px), the core's
  fans, spiral and rail (none on easy), the four rim bays at 60 px on the diagonals (ART-H1's), the phases by locks
  and "The core" exposing the core with its armour patch; par **150** (the draft) until step H3; the bounty **433**
  (core 355, 13 batteries × 6) at Level 35's measured scale 0.59, 24 % of budget(35). The stat block is rendered
  from the data (a machine boss's faction cell reads "never stunned").
- 2026-10-29 (M9 part H, step H3; decided in round 68, 2026-10-29): the fight measured with the autopilot (`RingPlay`) and the plan's
  fit and Rook over five seeds: the draft's 77 s at medium fell under the window, so the **core's HP 5,000 → 14,000**
  (the brief's rule: the core first; the batteries kept, no ring phase runs long), the total 17,960, the mean 148.7 s,
  the **par 150** kept; the alternative for the round (estimated, not flown): the core ≈ 9,000 and the batteries ×2 (a longer ring fight, a
  shorter core). Phase 1 1.18 × the Kraken's; QH-1 measured (Smart Bomb 4 %, Lance ≤ 8 %): the per-part caps stay.
  Found and fixed in the simulation: an anchored boss's halt place ignored the sections' eases (`ScrollPlan.haltScroll`),
  so the station sat 456 px low (centre at y −76); it now sits at (240, 160) on the screen.
- 2026-10-29 (M9 part H, step H5; decided in round 68, 2026-10-29, C.1): the round's alternative **flown** with
  the autopilot over five seeds (the plan's fit and Rook, `Level35Test`'s flights; the data edited for the measure only
  and restored): **b** (the core 9,000, every battery's HP ×2: SAM 440, rail 660, laser 880; total 16,920) lasts 141.3 s at
  medium (134.8–147.8; `levelReport`'s test fit 143.1 s), easy 103.7 s, hard 229.4 s; its phases at medium 48.4 + 12.2
  + 8.0 + 71.8 s (spin-up and phase 1, phase 2, phase 3, the core) against **a**'s 30.8 + 3.1 + 2.4 + 111.1 s (148.4 s).
  b's two ring phases stay under the ≈ 15 s each the round asked for, so **a (as built) is recommended** and kept;
  b also lets the sweeps strike the autopilot more (one seed 4 sweeps, over `Level35Test`'s 3). Recorded in round 68's
  captures (step H5, the ship still with the plan's fit, `--invulnerable`): halt → rings lit ≈ +1.5–4 s, the inner
  ring locked +31 s, the middle +40 s, the outer +49 s, the core open +52 s, the kill ≈ +148 s; the spin-up runs mostly
  under the `WARNING` banner; the bay Darts die as they leave the bays with the plan's pods and Rook (captured with the
  Ion Beam alone); the drifting segments fade within ≈ 1 s of the white flash while the plain base disc stays ≈ 15 s.
- 2026-10-29 (step FIX3, on round 68's C.9 recommendation a; accepted in round 68): the station's rail lines ride with
  their battery on the turning ring, the direction locked at the telegraph's start (`BossLines`: the origin onto the
  part's centre each step while it lives, a dead part's line keeps its last; `BossLinesTest`); the looks draw the
  telegraph, rail and flash from the moving battery and RingPlay looks ahead along the moving band. `levelReport
  -Plevel=35`: won 3/3, 0 retries a difficulty; the fight 108.5 s easy, 149.0 s medium (par 150), 249.3 s hard (as
  before, ≈ 248 s). Step H5's `sovereign-rail-capture-r68-a` shows the lines fixed where they started (b); FIX3 did not
  re-record it.
- 2026-10-29 (user, round 69 B.1: "The aim of the gun should compensate for the movement so that it hits after it has
  moved."): the rail lines lead the ring's turn (Fire rules), built in step FIX5 (`LineAim`, shared with the bunker's
  `LineBeam`; the battery's position at the live moment from `RingArena`'s ring speed, exact on the evenly turning
  middle ring; `BossLinesTest`: the live line within 1 px of the locked ship point, a sidestep dodges, the still core
  aims straight). `levelReport -Plevel=35`: won 3/3, 0 retries a difficulty; the fight 108.5 s easy, 148.4 s medium
  (par 150), 253.8 s hard (before: 108.5 / 149.0 / 249.3 s); `Level35Test`'s autopilot still struck by 0 lines.
- 2026-10-29 (user, [round 68](../../../concept-rounds/round-68/README.md), closed): **C.1 a** (the fight as built: the core 14,000, the batteries 220 / 330 / 440), **C.5 a** (Rook ignores the rail and relay lines, as the lines pass through allies), **C.6 accepted** (the EMP leaves the shut core's shield), **C.9 a** (the station's rail lines ride with their batteries, the direction locked; built in FIX3); art **H.1 ok** (the production frames) and **H.2 ok** (the intel silhouette): `art: final`; sounds **S.1, S.3, S.4, S.6–S.10 a** (`sovereign-spinup`, `-hold`, `-lock`, `-shutters`, `-fan`, `-spiral`, `-cookoff`, `-implosion`) and **S.2 b**, **S.5 b** (`sovereign-step`, `sovereign-klaxon`: not the recommendation; FIX4 switches the two keys, `m9-fix4-notes.md`, the a files play until then).
- 2026-10-30 (step R70, drafted for [round 70](../../../concept-rounds/round-70/README.md); decided in the next entry): the
  user's playtest (the core phase boring and long, the rings to last longer, the core less predictable): *The round 70
  rework*: the HP shift measured on a temporary copy of the data (SAM 220, rail 1,200, laser 1,500, the core 6,600:
  medium 145.2 s, the rings ≈ 91 s with every phase ≥ 20 s, the core ≈ 54 s; the data restored unchanged), the column
  answered by the graded HP (shuttered rings as a code alternative, S), and three core variants with storyboards (a the
  playbook, recommended; b the wreck throw; c the core breaks loose). Nothing built.
- 2026-10-30 (user, [round 70](../../../concept-rounds/round-70/README.md), closed): **A.1 ok**: the HP shift as proposed
  (SAM battery 220 kept, rail 330 → 1,200, laser 440 → 1,500, the core 14,000 → 6,600; the par 150 → 145). **A.2: "I like a
  combination of a and b"**, read (main agent) as **the playbook (a) with the wreck throw (b) as its fifth attack**: after
  each attack the core draws one of five at random (seeded), never the same twice running: spiral, 3-line rail volley,
  12-Dart surge, ring of 8 mines, wreck throw (2–3 dead batteries' wrecks hurled at the ship), each with its own tell; c
  (the core breaks loose) rejected, its storyboard in `concept/rejected/`; a and b `chosen` as the combination's look. Both
  built by step BOSS70 (`m9-pt-notes.md` "From CL70").
- 2026-10-30 (step BOSS70, round 70 built): A.1 and A.2 as decided. Built choices, pending the user's review: the
  wreck throw's wrecks **0.8 s apart**, each glowing its 0.8 s before its throw (b's 2.5–4 s clock gone); a wreck due
  while 3 fly stays on its socket; the mine ring's mines thrown in **0.5 s**, at even bearings with a seeded turn; the
  volley's lines from **30 px** out (the petals' rim) and on **every difficulty** (the old core rail was medium and
  hard only); the surge keeps its streams of **3** on hard (the timed streams of 5 are gone with the playbook); Vorne's
  half line is "You read the Sovereign well, pilot. Read faster." and Okafor's scrap line follows Rook's at the first
  throw; the tells' sounds reuse the Sentinel's charge, the nest's lock, the Gunship's doors, the line mine's blink
  and the walker wreck's groan (a sounds row in a later round). Measured: medium 144.3 s (par 145), sweeps 6–8 a fight
  (the bound 3 → 8), phase 1 for a random dodger 1.50 × the Kraken's (the pending band to 1.6).
- 2026-10-30 (step PIX7; the user on f1dc0f05: "the sovereign looks very pixelated, as if captured with a very small
  colour palette"): **no regression**: the station's frames, its Level 35 atlas region (RGBA8888, Nearest, 1:1) and
  its draw are unchanged since part H, and a still at level tick 6200 is pixel-identical on 5f510b41 and f1dc0f05
  (only the boss bar differs). The look came from the two static overlays over the rings: the rim sheen and the shade
  were 224×224 drawn 2x with a 4×4 Bayer dither over 10 and 5 steps, a coarse 2×2-pixel stipple over the whole
  station (a quarter of their pixels a checker cell); round 70's longer live-ring phase (the core opens ≈ 102 s after
  the halt, not ≈ 52 s) keeps them on screen twice as long. Now 448×448 drawn 1x with 32 undithered steps (the
  pivots' `overlays` scale 1, read by the looks): a smooth sheen and shade, the rest of the art unchanged; Level 35
  stays 3/6 pages. Pending the user's review ([still](concept/sovereign-look-capture-r71-a.png)).
