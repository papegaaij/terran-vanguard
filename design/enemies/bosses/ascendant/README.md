---
title: Ascendant
design: approved
implementation: done
art: chosen
depends-on: [../../air/mirror-interceptor, ../../air/chimera, ../../air/dart-drone, ../../ground/sam-nest, ../../../world/jovian, ../../../campaign/act-6-jovian-storm, ../../../player/wingmen, ../../../ui/hud, ../../../story/characters/vorne, ../../../systems/economy]
updated: 2026-11-03
---

# Ascendant

## Summary

The Act 6 boss (Level 42, Callisto orbit): Vorne's flagship, half Ascendancy warship and half Vrell organism. It
comes **bow-on** over a halted arena, its gold hull filling the top of the field. Phase 1 is the warship: shield
generators, mirror-panelled batteries and the bow cannon. In phase 2 the Vrell half erupts through the hull, with
regenerating tissue sacs, a heart and Chimera broods, and Vorne shouts for the only time. Phase 3 is the escape:
Vorne's Vrell **lifeboat** breaks from the wreck and climbs off the top edge in a scene (≈ 9 s: it tears free, hangs a moment, then
climbs; round 80: no target, no bar; round 81: longer), escaping toward the gate.

**Status: draft** (Act 6 design phase, step D-boss, 2026-10-31), **decided 2026-10-31 (round 72)**.
It follows the gap analysis's recommended answers: **Q6 a** (bow-on over a halted arena; the alternatives under
*Variants b and c*), **Q1 c** (the [`reflect`](../../README.md#reflect) rule on the phase-1 panels), **Q9 a** (the
elite livery), and judgement calls **J1** (Level 42's reference DPS 168), **J6** (Vorne's only shout, at the eruption)
and **J9** (track 19, then track 18 from phase 2, the escape sting). Every number is a hand-written **draft** at
medium; there is no `data.yaml` yet (M10 builds it with Level 42 once the design is approved).

## Design

### Why bow-on

The Act 5 boss turned its weapons through the ship's reach on rings; the Act 6 boss is one great body that
**changes nature** mid-fight. Bow-on, the whole ship is read at a glance: the gold warship first, then the Vrell
inside it breaking out, then the small thing that was hidden in it running away. Three phases, three ways to fight:
**flanking** (the mirrors open from the far side), **focus** (the tissue heals unless held) and **a chase that
cannot be won**, only paid. Level 42's traits are `piercing` (the stacked bow parts) and `homing` (the broods, the
lifeboat's weave).

### Stat block (draft)

Numbers at **medium**; HP in damage units (a Pulse Cannon L1 shot does 2); bullet classes (`small` = 4, `medium` = 6,
`heavy` = 10, `laser` = 8) and contact damage from the [balancing basis](../../README.md#balancing-basis). The table is
rendered from the boss's [data.yaml](data.yaml) (M10 part H, step H2), which also holds the fight's script (the
phases, the panels, the playbooks, the cues, the regeneration and the escape). The reference
DPS is the **proposed** Level 42 value of **168** (J1, the
[Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft); decided 2026-10-31 (round 72)). Borrowed attacks keep
their units' numbers (not repeated here).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Hybrid (a machine, but a boss: never stunned, its shields emptied by the EMP Burst) |
| Layer | `air` (`space` is `air` in vacuum, the [Jovian media](../../../world/jovian/README.md#media-per-level)); the hull is a body: contact `huge` 25 once a second and the ship pushed 24 px out |
| Size tier | `huge` |
| Size | 440×276 px, hitbox 400×274 (the visible bow ≈ 400 px wide at the top edge, tapering to the bow's tip at y 262; the hull runs on beyond the top edge); the lifeboat 56×80 px, hitbox 40×60 |
| Parts | phase 1: the **bow cannon**, 2 **shield generators**, 4 **mirror batteries**; phase 2: 3 **tissue sacs**, the **heart**; phase 3: the **lifeboat** (scenery, `armoured`: never hit). Every other part `destroyable`, none `vital` (the hull breaks on the heart); the hull itself is no hit area (shots spark on it) |
| Orientation | `fixed` (bow down); the batteries' panels turn (see *Arena*); the cannon aims at 64 headings |
| HP | phase 1: bow cannon 2800, generators 800 each, batteries 850 each (**7,800**); phase 2: sacs 1300 each, heart 2500 (**6,400**); total 14200 (easy 10650 / hard 18460, from the global multipliers) |
| Armour / shield | the bow cannon carries a gold hex **shield** under the [enemy shield rule](../../README.md#enemy-shields): **240** while both generators live, half with one, none with neither; each battery stands behind a **mirror panel** under the [`reflect`](../../README.md#reflect) rule (arc 120°, heat **260**, ≈ 2.0 s of the plan's Ion Beam L5; down 3 s); the sacs and the heart **regenerate** under the [regeneration](../../README.md#regeneration) rule (sacs 4 % a second after 1.5 s, the heart 2 % a second after 2.0 s); the heart is sealed under a tissue membrane (shots spark teal, no damage) until two sacs are dead; the lifeboat is scenery (no HP: see *The lifeboat*) |
| Speed | 44 px/s (the slide in, bow-first; then still over the halted arena); the lifeboat hangs 1.6 s where it tore free, then climbs to the top edge in 7.4 s, its speed rising 20 : 90 (the climb's ratio, ≈ 11 → 48 px/s) |
| Movement | slides in bow-first from the top edge over ≈ 6 s (invulnerable until in place), its centre at rest at (240, 125); fixed there (see *Arena*) |
| Attack | phase 1: the bow cannon's playbook (see *Phases*): its **spinal lance** (the muzzle's 0.8 s white-gold glow, then a red line aimed at the ship's spot for 1.2 s, locked 0.2 s before it fires, a 0.5 s beam 24 px wide down the field, `heavy` 10 once); the **lance sweep** (the spinal lance's line and lock, its beam then sweeping 50° over its 1.4 s toward the side the ship is not on, `heavy` 10 once); the **gold fan** (three 9-way fans over 80° 0.5 s apart, `small`, 150 px/s); the **port fan** (a 2-way aimed fan over 8° from the bow's gun ports as each lance's line is drawn and as its beam dies, `small`, 150 px/s: round 81, the field never quiet); the **missile salvo** (3 of the [SAM Nest](../../ground/sam-nest/README.md)'s homing diamonds from each shoulder rack whose generator lives, at most 8 alive); the Dart stream (8 Darts from two hull bays, at most every third draw); the batteries' aimed `burst` of **3** (`medium` 6, 160 px/s) every 6 s after a 0.5 s red rim, only while the panel faces within 30° of the ship, at most one bursting at a time, ≥ 0.6 s apart; phase 2, the Vrell playbook (from the heart, from the sacs while it is sealed): the Chimera brood (a sac splits into 4 Chimeras, at most 6 boss-launched alive, at most every third draw); the **spore spiral** (3 arms, `small` 4, 120 px/s, the arms turning 60°/s, 4 bullets/s an arm, for 4 s); the **bile lobs** (3 impact marks for 1 s, never within 60 px of the ship's x, each a ring of 10 `small` at 110 px/s); the **grafted guns** (twin aimed 5-way fans over 40° from both dead shoulders, three times 0.4 s apart, `small`, 150 px/s) |
| Formations | solo set piece |
| Weak points | none (the sacs' and the heart's lime cores, with the hybrids' teal seams round them; no weak-point multiplier) |
| Effective traits | `piercing`, `homing`, `shield-breaker`, `side`, `beam` |
| Credits | 407 (score 4070 × chain) in Act 1 terms, split 45 / 55: phase 1 **183** (the bow cannon 113, each generator 15, each battery 10), phase 2 **224** (the heart 176, each sac 16; the heart holds the lifeboat's former 15 %): ≈ **24 % of budget(42)** at Level 42's measured `bounty_scale` 0.63 (see *Bounty*) |
| Death | not a death: at phase 2's end the hull **breaks** (a 3 s vacuum chain along the keel, the act boss's screen flash at K+2.5 s, the credit shower over the wreck), then the lifeboat escapes in a short scene (see *The hull breaks*) |
| First level / used in | L42; only; the lifeboat's look is the seed of **Vorne's Chimera** (Level 48, Act 7's mid-boss, the [roster](../README.md#mid-bosses)) |
| Difficulty hooks | easy: rests ×1.3; the spinal lance's line 1.5 s; batteries' bursts of 2; the spiral 2 arms; regeneration 3 % / 1.5 % a second; the brood 3 Chimeras. Hard: rests ×0.8; mirror arc 140°; batteries' bursts of 4; the spiral 4 arms; regeneration 5 % / 2.5 %; no boss checkpoint; its HP ×1.05 (not the difficulty's ×1.3) |
<!-- /data -->

### Arena

The scroll **halts** in Callisto orbit ([the Jovian system](../../../world/jovian/README.md#sub-locations-per-level):
both fleets over the ice moon); Level 42's document places the arena and owns its timeline. Screen space, the field
480×540, y down:

| Element | Where | Notes |
|---|---|---|
| The bow | from the top edge (x 40–440) tapering to the bow tip at (240, 250) | a wedge of two slanted faces; the hull runs on off the top edge; clear of the boss bar's plate |
| Bow cannon | the bow tip, (240, 235); 70×50 px, hitbox 56×40 | the phase-1 playbook's source; its hex shield fed by the generators |
| Shield generators | the shoulders, (110, 60) and (370, 60); 52×52, hitbox 40×40 | exposed, high on the hull; a gold beam of light from each to the cannon while it lives |
| Mirror batteries | two on each face: (150, 130), (185, 190) left; (330, 130), (295, 190) right; 48×48, hitbox 36×36 | each behind its panel; a face's panels face **45° to their own side** of straight down and turn within ±20° of that toward the ship at 30°/s |
| Side pockets | beside the bow below y 150, ≈ 120–160 px wide each | the ship may fly up beside the bow (mind the hull's contact) |
| The ship's room | the field below the bow | the bottom 290 px open in every phase |
| Backdrop | deep layer | Callisto's curve, Jupiter's limb, the fleets' lights; the CDF ships marked friendly where they reach the play plane (round 70's marker) |

**The flank.** With the panels' arcs (120°, ±60° of their facing), a face's batteries are **open from the far side
of the field**: the left face's two batteries are hit by shots from the right third and the upper right, reflected
from the centre and the left; the right face's the other way round. So phase 1 asks the player to cross the field,
not to sit under the bow. Shots from above (the Airstrike, the Smart Bomb) pass the panels as the rule says.

**Fire rules.** At most **one** telegraphed line at a time (the spinal lance never overlaps another lance); a battery
fires its aimed `burst` of **3** (`medium` 6, 160 px/s) every **6 s** only while its panel faces within 30° of the
ship, at most **one** battery bursting at a time, ≥ 0.6 s apart (the barrage cues them whatever their clock); no enemy bullet spawns within 72 px of the ship (the
[bullet rules](../../README.md#bullet-readability-rules)); at most **8** of the cannon's missiles alive. A mirror's bounced
bullets are the player's own fire (the rule); Rook's shots are absorbed.

**Intro** (≈ 8 s): **the boss warning before the hull shows** (track 22, the Act 5 lesson) as the scroll slows; then the
hull slides in **bow-first** from the top edge over 6 s, its gold lights coming on from the bow back, the bar
`ASCENDANT` fills; invulnerable until in place. **Vorne** speaks first on the enemy channel (see *Vorne*). The boss
checkpoint is taken at the warning (none on hard); the level clock halts while it lives.

### Phases

The bar shows the hull's **total** HP (phase 1's parts, then phase 2's, 14,200 in all as built): it falls in every phase and
each part's kill takes its share off it. The bar goes away when the heart dies (round 80: the escape is a scene, no
bar).

| Phase | Ends at | Behaviour |
|---|---|---|
| 1 — The gold hull | every phase-1 part dead (any order) | The bow cannon draws from its **playbook** (below); the batteries burst on their clock behind their mirrors; the generators feed the cannon's shield. The natural order is the generators (the shield falls), the batteries from the far side, then the cannon; a `shield-breaker` or the EMP opens the cannon early for a few seconds. With the cannon dead first the playbook draws on from the **generators, then the batteries** (only the moves that do not fire from the cannon: the salvo, the Dart stream, the barrage; round 81), so a cannon killed first leaves no quiet hull. |
| The eruption | — (4 s, invulnerable) | The hull shudders, the keel splits along the centre line and the **Vrell half erupts** through the gold plates: teal-black tissue, the three sacs swelling out of the breaches, the heart pulsing under its membrane. The screen shakes; track 19 crossfades to **track 18** (J9); the Choir speaks, then **Vorne shouts** (J6, see *Vorne*). An **armour patch** drops. |
| 2 — The Vrell half | the heart at 0 | The sacs **regenerate** (a held beam or steady focus denies it); the heart opens to fire once **two** sacs are dead; the **Vrell playbook** (below) draws from the heart (from the sacs while the heart is sealed). |
| 3 — The escape | the lifeboat off the top edge (≈ 9 s from the breakout) | The hull breaks; the lifeboat tears free, hangs a moment and climbs away through the wreck: a scene, no target and no bar (round 80; round 81: ≈ 9 s, was 4). Scripted, no playbook: it always escapes (see *The lifeboat*). |

**The playbooks** (round 70's lesson, the [Iron Sovereign](../iron-sovereign/README.md#the-round-70-rework-decided-round-70)'s
core): after each attack and a **rest** the part draws its next attack with the simulation's seeded random (a replay
draws the same), **never the same twice running**, each with its own **0.8 s tell** (see *Tells*). In phase 1 a
**lance** (the spinal lance, its sweep or the twins) comes at least every **second** draw (round 80's B.1: the rail gun
more often).

**Phase 1 is never quiet** (round 81's B.1, the user: "still too many quiet periods; you don't even need to dodge the
bullets in the first part, because the shield regenerates before the next round"). Measured from phase 1's first shot
to its end, no stretch over **1.0 s** passes without a bullet still closing on the ship, a homing missile, a Dart or a
live beam (`AscendantFightTest`, five seeds at medium and hard; the stretch from the hull settling to the first shot,
≈ 1.8 s, is the arrival's), and a ship that stands still (its front gun firing) loses armour at medium on every seed
(it is shot down in ≈ 40–60 s). The levers: the rests **1.0 s** (0.7 s below half the cannon's HP; were 3.5 and 1.9);
the **port fan** (a 2-way aimed pair over 8° from the bow's gun ports, `small`, 150 px/s) covering every move: as each
move ends, and as a lance's line is drawn too, so its pair closes on the ship through the rest, the next tell and the
telegraph; the barrage's first battery telling under the barrage's own tell (it fires as the tell ends); the gold fan
two fans, not three (more draws, less spray round Rook); with the cannon dead the playbook draws on from the generators
and the batteries (the port pair then from that part's own gun). The pair is narrow on purpose: aimed at the ship, it
keeps off Rook's wing slot (round 81: Rook comes through phase 1 on 8 of 10 seeds at medium).

| Phase | Attack | What it does |
|---|---|---|
| 1 (rest 1.0 s; 0.7 s below half the cannon's HP; round 81) | **Spinal lance** | a red `laser-line` aimed at the ship's spot for **1.2 s**, locked 0.2 s before it fires, then a 0.5 s beam **24 px** wide down the whole field, `heavy` 10 once; the port pair as the line is drawn and as the beam dies |
| 1 | **Lance sweep** | the spinal lance's line and lock, then a **1.4 s** beam that fires at the ship's spot and sweeps **50°** toward the side the ship was not on at the draw (its tell shows the way), `heavy` 10 once |
| 1 | **Twin lances** | two lances from the cannon's flanks (24 px either side), the ship's side first, the other **0.6 s** later, re-aimed at the ship's spot then; one gold fan from between them as the first fires; the port pairs as the lines are drawn and as the last beam dies |
| 1 | **Gold fan** | two 9-way fans over 80° (`small`, 150 px/s), 0.5 s apart (three until round 81) |
| 1 | **Missile salvo** | 6 of the [SAM Nest](../../ground/sam-nest/README.md)'s homing diamonds from the shoulder racks (3 from each rack whose generator lives; a dead generator's rack is dead) |
| 1 | **Dart stream** | two hull bays launch a stream of 4 Dart Drones each (8, under the Darts' cap of 16); at most every **third** draw |
| 1 | **Battery barrage** | the living batteries side to side (left face, then right), **0.4 s** apart, each fires its own told burst (its 0.5 s red rim, then its aimed burst of 3), whatever its clock and its panel's facing; drawn at twice the others' weight (at 1 it was missing from a whole recorded fight) |
| 2 (rest 1.2 s; 0.7 s below half phase 2's HP) | **Chimera brood** | a sac swells and splits: a wing of **4** Chimeras (their own pattern and regeneration), at most **6** boss-launched alive; with no sac left the heart broods; at most every **third** draw |
| 2 | **Spore spiral** | a 3-arm `spiral` of Vrell spores (the larger, brighter Vrell bullets that read over teal; `small` 4, 120 px/s, the arms turning 60°/s, 4 bullets/s an arm) for 4 s |
| 2 | **Bile lobs** | the boss [`lob`](../../README.md#boss-attacks-and-root-bursts) from the sacs: 3 impact points marked on the play plane for **1.0 s** (never within 60 px of the ship's x at the mark), each bursting into a ring of 10 (`small`) |
| 2 | **Grafted guns** | the dead hull's shoulder mounts, now tissue-driven: twin aimed 5-way fans over 40° (`small`, 150 px/s) from both shoulders, three times 0.4 s apart: the warship firing for the Vrell |

The boss-launched Darts and Chimeras count into the level's enemies (not its density), pay their own bounty and get
no edge warning (a boss and what it launches never trigger one), as the Sovereign's Darts.

### The lifeboat

Vorne's escape craft, **grown**, not built: a Vrell seed-pod of teal-black tissue with the gold Ascendancy escape
capsule half swallowed in it, its gold canopy still showing. It is **the seed of Act 7's mid-boss** (Vorne's Chimera,
Level 48: him fused with this lifeboat), so its look is drawn to grow into that one.

| Rule | Value |
|---|---|
| Breakout | as the hull's flash fades (K + 3 s, see *The hull breaks*), the heart's cavity splits and the lifeboat tears free with a spray of tissue; the wreck's two halves, parted along the jagged fracture (A.11), drift apart to the sides and down (scenery: no hit, no contact) |
| Flight | it **hangs 1.6 s** where it tore free (bobbing 3 px), then climbs from (240, 175) to the top edge in **7.4 s** on every difficulty: ≈ 11 px/s at first, easing up to ≈ 48 px/s (the speeds 20 : 90 are the climb's ratio), weaving ±40 px on a seeded path; it never comes below y 200. Round 81 (B.1, the user: "the escape is way too quick now"): ≈ 9 s from the breakout to the top edge, was 4 |
| A scene | round 80 (B.1/A.4: the chase read unclear): it has **no hitbox** (shots and blasts pass through it, no sparks), pays nothing, drops no spores and has no contact damage; Rook and the specials ignore it. Its old 15 % share of the bounty is paid with the hull (the heart's, see *Bounty*) |
| No bar | the boss bar goes away when the heart dies; Vorne's escape line (≈ 6.4 s) starts with the climb and is heard in full over it (round 81) |
| The end | at the top edge it flashes once toward the gate and is gone (**`ESCAPED`**): the **escape sting** (track 33) cuts track 18. The level is won; its end and the act's end follow (Level 42's and the act's documents) |

### Tells

| Attack | Tell | Medium (easy / hard) |
|---|---|---|
| The hull's arrival | the boss warning (track 22) before it shows; the gold lights coming on | 5 s + 6 s |
| Battery burst | its panel's rim glows red as the battery charges | 0.5 s |
| Spinal lance | the bow's muzzle glows white-gold, then the red line brightening | 0.8 s + 1.2 s line (easy 1.5 s / hard 1.0 s) |
| Lance sweep | the muzzle's glow and a fading fan of thin red lines over the arc it will sweep, then its red line | 0.8 s + 1.2 s line |
| Twin lances | the muzzle's glow, then two red lines from the flanks, the second 0.6 s after the first | 0.8 s + 1.2 s each |
| Gold fan | the bow's gun ports flash gold | 0.8 s |
| Missile salvo | the shoulder racks' hatches open with a red lock light (the SAM Nest's) | 0.8 s |
| Dart stream | two hull bays' doors open with a gold light | 0.8 s |
| Battery barrage | every living panel's rim glows red together, then each its own rim as it fires (the first one's under the last 0.5 s of the barrage's tell, so it fires as the tell ends; round 81) | 0.8 s, then 0.5 s each |
| Mirror back up | the silver shimmer across the panel (the reflect rule) | 0.4 s |
| The eruption | the hull shudders, cracks of teal light run down the keel | 1.0 s, then 3 s erupting (invulnerable) |
| Chimera brood | a sac swells, its seams flaring teal-white | 0.8 s |
| Spore spiral | the heart's membrane (or the sacs) pulses lime and teal | 0.8 s |
| Bile lobs | the sacs contract; the impact markers on the play plane | 0.8 s + 1.0 s marks |
| Grafted guns | tissue tendrils snake into the shoulder mounts, their muzzles glow gold | 0.8 s |
| The lifeboat's breakout | the heart's cavity splits with a teal flash | 0.5 s |

### The escort (Rook)

Rook flies the act in the escort slot ([Rook in Act 6](../../../player/wingmen/README.md#rook-in-act-6-draft)). His boss
rules here: in phase 1 he shoots the **generators** first (his shots spark on a mirror and are absorbed), then a
battery from its open side if he is there, else the cannon; in phase 2 the **sac with the least HP** (his focus
denies its regeneration, as the rule rewards) and Chimeras within his reach; in phase 3 nothing (the lifeboat is
scenery). He steps out of
a told lance's line and a lob's marks like the ship. The test autopilot does the same, crossing the field to open a
face's batteries.

### Duration and HP

Target: the act-boss window **90–180 s** at medium; the **par** is the measured mean, **144 s** (round 81's re-measure
with phase 1 never quiet and the 9 s escape scene: five seeds 138.7–149.0 s, mean 144.2, from the bar to `ESCAPED`; round
80's 131 with the 4 s escape, step H3's 142 with the 14 s chase). The escape's 12 s from K (the 3 s break, the 1.6 s hold
and the 7.4 s climb) is in the par (it is the same on every run). Hard 160.5 s (154.7–175.2 over five seeds; round 81: the
HP lever down from ×1.15 to **×1.05**, at ×1.15 the never-quiet phase 1 ran 176–191 s, over the window). Easy 104.7 s.

**The rate.** The 0.6 × reference rule read the Iron Sovereign far too slow: its build needed **17,220 HP for 137 s of
fire** at Level 35's reference 144 (its measured fight 144 s less its spin-up and opening), **≈ 126 DPS ≈ 0.87 ×
reference**. This draft sizes the Ascendant on that **measured** rate: 0.87 × 168 ≈ **147**. Phase 1 at ×0.9 (beam time
lost on mirrors, the cannon's regrowing shield): ≈ 132. Phase 2 at ×0.8 (the regeneration, fire spent on Chimeras):
≈ 118. (The gap analysis's ≈ 11,500, the Sovereign's draft 8,960 scaled by the references, would measure ≈ 120 s.) **As
built** (step H3, measured with the autopilot and the plan's fit over five seeds): the panels cost the beam far more
than ×0.9 (≈ 2 s of heat for each 3.4 s down), so phase 1 ran ≈ 100 s at 8,800; the batteries came down to 850 each
(phase 1 7,800, the hull 14,200) and the table below gives the measured times.

| Phase | HP | Time |
|---|---|---|
| The warning, the arrival, Vorne | — | ≈ 8 s |
| 1 — The gold hull | 7,800 (batteries 850 each) | ≈ 82 s (round 81: the eruption 85–95 s after the bar) |
| The eruption | — | 4 s |
| 2 — The Vrell half | 6,400 | ≈ 39 s |
| The hull breaks | — | 3 s |
| 3 — The escape | — | 9 s (the 1.6 s hold, the 7.4 s climb) |
| Total | 14,200 | **≈ 144 s** (measured, round 81) |

Phase 2 is kept **shorter** than phase 1 (the Sovereign's round 70 lesson: a long last part bores), and both have a
playbook. Lessons the build must apply (M10, the Level 42 part):
- **Measure, then tune the HP, not the attacks**: the autopilot with the hand plan's Level 42 fit and Rook over five
  seeds, every seed inside 90–180 s, the par at the measured mean; tune phase 1's batteries and phase 2's heart first.
- **Phase 1 against the Kraken's** (the calibration band); the levers are the batteries' clock and the rests.
- **The regeneration must not stall**: on every seed phase 2 ends; a sac healed back to full more than twice by the
  autopilot means the rate comes down, not the HP up.

**Specials on a many-part boss.** The specials' boss rules cap their damage per part: a Smart Bomb on the full phase 1
is worth up to 7 × 150 = 1,050 (≈ 12 % of the phase; no part dies of it); an Orbital Lance up to 400 a part it
crosses; the EMP empties the cannon's shield and **shatters all four mirrors** at once (3 s down), stuns the
launched Chimeras and Darts, but never stops the sacs' regeneration (a boss is never stunned). The
[Plasma Arc](../../../player/weapons/plasma-arc/README.md)'s chain counts the hull as one target. The IFF spoofer fools
none of its batteries (a boss's parts).

### Bounty

An act boss aims at ≈ **24 %** of its level's budget (the economy's share under the perfect-run cap; Levels 21, 28 and
35). budget(42) = 700 × 1.07⁴¹ ≈ **11,216**; 24 % ≈ **2,692 paid**. Data is in Act 1 terms: **bounty = 2,692 ÷
(10.486 × `bounty_scale`) ≈ 257 ÷ `bounty_scale`** (10.486 = 1.6⁵, Act 6's factor). Level 42's scale is the level
document's; at its measured **0.63** ([Level 42](../../../campaign/act-6-jovian-storm/level-42-ascendant/README.md#credit-budget)) that is **407** (the hand 0.64 gave 401, the first draft's 0.59 435), split:

| Share | Parts | Act 1 terms | Paid |
|---|---|---|---|
| 45 % | phase 1: the bow cannon 113, each generator 15, each battery 10 | 183 | ≈ 1,209 |
| 55 % | phase 2: the heart 176, each sac 16 | 224 | ≈ 1,480 |
| | | **407** | **≈ 2,689 = 24.0 %** |

Round 80 (the escape a short scene): the lifeboat's old 15 % share (61, once paid pro rata to its damage) is the
heart's, so the whole bounty is paid when the hull breaks, the same on every run. When the level's scale is set, the
bounty is recomputed with the same formula; the shares stay. The boss-launched Darts and Chimeras pay their own bounty,
outside the share.

### The hull breaks

The heart at 0 (**K**): the heart bursts lime-white, the surviving sacs burst with it (paying their share), and a
**chain** runs down the keel for 3 s, gold plates blowing outward between the tissue
([vacuum rules](../../../world/belt/README.md#vacuum-rules): short flashes, debris thrown in rings, fire only where the hull
holds air: its breaches burn); the **act boss's screen flash** at K + 2.5 s and the **credit shower over the wreck**.
Then the lifeboat breaks out (K + 3 s) and the escape runs (≈ 9 s: the 1.6 s hold, the 7.4 s climb; round 81). The hull breaks along a **jagged fracture** (round 72
A.11): torn plating, a ragged edge, debris and venting along the break, **never a straight clean cut**. The wreck's halves
drift apart along it, burning at the breaches, and sink out of the field's sides by the end of the escape. There is no `huge` death explosion: the ship is
broken, not destroyed (Q6), and the level's last view is the empty orbit. Round 81 (B.1, the user: "the 2 halves of the
ship drift down, but there's no back of the ship"): each half carries the **rest of the hull** above the top edge, the
stern and its engine block (the same section and livery run on, gold frames across it where the ribs end, the keel's
tissue in the split, six dark nozzles with gold collars at the end), so whatever comes into view as they sink is ship;
the fracture runs on up through the stern.

### Vorne

[Vorne](../../../story/characters/vorne/README.md) is on the enemy channel for the fight (the HUD's amber plate, the
steady interference over his portrait). **This document owns the order and the wording of his Level 42 lines**; Level 42
places and times them (its radio's delays) and adds the friendly lines between them. Drafts, decided 2026-10-31 (round 72):

| # | When | Speaker | Line (draft) |
|---|---|---|---|
| 1 | `boss-arrival` (the hull in place) | Vorne | "The Ascendant, Lancer. Helix built her, the Vrell grew her, and I command her. You should have named your price." |
| 2 | the first generator destroyed (or 25 s into phase 1) | Vorne | "Okafor sends you in alone again. She spent Aurelia's people the same way, once." |
| 3 | the bow cannon below half its HP | Vorne | "You fight well for a hired gun. It changes nothing. The door is open, and I hold the key." |
| 4 | the eruption (its first second) | the Choir | "…the one who opened… is inside us now… he was always inside us…" (its own line, not Level 37's sample: [the Choir](../../../story/characters/the-choir/README.md#act-6-draft); its channel's dress) |
| 5 | the eruption, right after the Choir | Vorne, **shouted** | "No! We had an agreement!" (`shout: true`; the portrait's **rage**) |
| 6 | K + 4.6 s (the lifeboat's climb, round 81) | Vorne, cold again | "A misunderstanding. They still need me; someone must open the way. The gate, then. Goodbye, Lancer." |
| 7 | after `ESCAPED` and ≥ 4 s of calm | Okafor | "Then we follow him." (the act sketch's last line: the level's last voice) |

**The only shout** (J6): line 5 is the one shouted line Vorne has in the game before Act 7; the voice README's check
that rejects `shout: true` on his cues before Act 6's end takes **this cue alone** as its exception (the audio step
writes it). Phase 2 has no line of his after the shout: he says nothing while the Vrell half fights in his ship. Line 6
is his composure back, and his self-deception: he still believes the Choir will honour the deal (the story's Act 6
beat). Nothing of the Silence here: Level 41's files named it, and he does not speak of it.

### Echoes and relatives

- **No Level 49 echo**: the boss rush re-creates the Brood Carrier, the Dust Colossus and the Iron Sovereign (the
  [roster](../README.md#boss-rush-l49-echoes)).
- **Vorne's Chimera** (Level 48, Act 7's mid-boss): grown from this lifeboat with Vorne fused into it; it inherits the
  lifeboat's silhouette, the half-swallowed gold capsule and the mix of Ascendancy and Vrell tactics.
- **The Mirror Interceptor** ([its document](../../air/mirror-interceptor/README.md)) taught the panels in Level 37;
  the **Chimera** ([its document](../../air/chimera/README.md)) taught regeneration in Level 36; the boss tests both.

### Variants b and c (for the round's a/b/c)

- **b — Flank pass**: the long ship passes down one side of a scrolling field, its sections fought in turn (batteries,
  then the tissue, then the lifeboat's dock). A grand view of its length, but a multi-section scrolling boss (large)
  and harder to give phases that read.
- **c — Two bosses at once**: the ship splits into the gold hull and the Vrell mass, fought together, then the
  lifeboat. Striking, but two huge bodies on one field are hard to read and to keep apart; the same cost as b.

### Presentation

- **Look brief, phase 1** (Q9 a, the [elite livery](../../README.md#factions-and-visual-language)): a bow-on wedge of
  **gloss black and heavy gold** (gold ≈ 35–45 % of the silhouette: the bow's ribs, the panel frames, the generators'
  collars, the crest on the bow), the strong top-left rim light so the black never sits dark on black space, red
  running lights along the faces. The panels are polished silver-gold crescents with the Mirror Interceptor's slow
  specular sweep and heat colours. The generators' gold beams to the cannon.
- **Phase 2**: the keel split and the plates bent outward by **teal-black tissue** (the Vrell spawner base `1E5C5A`)
  with **teal `00FF9A` seams**; the sacs' and the heart's cores **lime** (the bosses' weak-point glow), pulsing teal
  as they heal. The gold half stays visible round the tissue: half ship, half organism.
- **The lifeboat**: see *The lifeboat*; its gold canopy is the one warm spot in the teal.
- **Drawing**: the hull is fixed (bow down), so its sprites are not rotated: a few large hull pieces, the parts in
  their states (whole, damaged, dead; mirror whole, heating, cracked, down), the eruption as a frame sequence, the
  wreck's two halves; the atlas within the level's page budget
  ([production budgets](../../../art-direction/production/README.md)), measured as the Sovereign's was. Every glow and
  haze layer smooth (≥ 32 steps, no ordered dither, drawn once: the 2026-10-30 dither audit).
- **Readability** (one check per sheet): the gold, then the teal, over Level 42's black space and Callisto's curve
  ([moods per level](../../../world/jovian/README.md#moods-per-level-q10)); the friendly fleet marked; the magenta
  bounced bullets distinct from the Vrell spores.
- **Music** (J9): the boss warning (track 22), then the Ascendancy boss theme **#19** from the warning's end; at the
  eruption **#18** (the Vrell boss theme) takes over; the **escape sting** (track 33, the audio step's) at `ESCAPED`
  ([music](../../../audio/music/README.md)).

### Sounds brief

Real recordings from Freesound (CC0 / CC-BY), heavy and mechanical, then organic; no bubbly, wet or popping sounds.
Sounds play in vacuum as everywhere (the game's convention). The launched units and the borrowed attacks keep their
own sounds.

| Sound | Where / when | Brief |
|---|---|---|
| Arrival | the hull sliding in | a huge ship's hull groan and a deep engine drone rising, metal stress ticks |
| Spinal lance | its charge and shot | a deep rising capacitor whine, then a heavy electric crack with a long rumble |
| Panel bounce | a shot bouncing off a panel | the Mirror Interceptor's glassy ping, lower and heavier |
| Generator down | a generator destroyed | a transformer failing: a hum collapsing into crackles and a thud |
| The eruption | phase 2's start | tearing steel plates, a long rending groan, a low organic creak under it (stretched leather or rope, slowed), never wet |
| Sac healing | a sac regenerating | the Chimera's soft rising creak, deeper |
| Brood | a sac splitting | a dry tearing rip, then the Chimeras' engines |
| The hull breaks | the chain along the keel | rapid secondary detonations, metal shearing, a deep boom at the flash |
| The lifeboat | breaking free, its climb | a tearing rip, then a straining engine whine fading up and away |

## Concept art

Concept round 72 (Act 6 design phase, step ART-R-B; **decided 2026-10-31 (round 72)**): the Ascendant per
phase with the lifeboat in the elite livery of Q9 a, a and b, each a PNG sheet (the phase-1 and phase-2 hulls at 1×,
the parts in their states, zooms, the eruption frames and the wreck's halves, readability over Level 42, the measured
gold share and the one-page atlas budget) and a phase storyboard (nine key frames: the warning before the hull
shows, the slide-in, phase 1, the generators down, the eruption, phase 2, K, the escape, `ESCAPED`). Generator
`tools/concept/act6_r72.py ascendant` (round 73's storyboard: `phases73 a`); prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/ascendant-r72-a.png](concept/ascendant-r72-a.png) | Ascendant A, the gilded wedge: one smooth wedge with gold ribs fanning down both faces; the Vrell half erupting along the keel (plates peeled outward) and two breaches; the lifeboat a round seed-pod with the gold capsule half swallowed; gold 34 %, atlas 11.9 % of a page (sheet) | chosen (round 72 A.10 a) |
| [concept/ascendant-phases-r73-a.png](concept/ascendant-phases-r73-a.png) | The phases in look A (storyboard), frame 8 redrawn: the hull torn in two along a jagged fracture down the keel, torn plate flaps bent outward on both halves, a charred, hot-glowing rim, shards in the gap, orange and teal gas venting from both edges | chosen (round 73 R.1 accept) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

Production art for concept round 80 (M10 part H, steps ART-H1 and ART-H1b; generator `tools/art/ascendant.py`, frames
in `assets/sprites/` since step H2's `data.yaml` claims the root `ascendant`; layouts, blending and pivots in the
script's header and `pivots/ascendant.json`). The parts sit at the places [data.yaml](data.yaml) gives them (the
generator reads them from it): round 72's A.10 a places, the side sacs in the hull's two breaches at (150, 125) and
(330, 125) where the upper batteries stood, the third sac high on the keel at (240, 60), the heart at (240, 152) over
the lifeboat's breakout (240, 175); the bow's tip at y 262 (step ART-H1b moved them from steps H1a/H1b's test places).
The eruption's keel frames widen to 252 px to take both breaches and are drawn over the dead batteries (the side sacs
burst through the upper two's mounts); the lifeboat breaks out through the heart's torn cavity; the jagged fracture
runs down the keel between the side sacs as before (decided 2026-11-03, round 80 A.3 ok). The whole boss is
**49 % of one 2048² page** (the one-page budget held: QH-1 does not arise from the Ascendant); gold 36 % of the phase-1
silhouette. Prompts: [concept/prompts.md](concept/prompts.md#ascendant-final-r80-a).

| File | What | Status |
|---|---|---|
| [concept/ascendant-final-r80-a.png](concept/ascendant-final-r80-a.png) | Final frames, phase 1: the hull (one 440×276 piece, ports, Dart bays and racks cut in) with its parts over round 72's Level 42 stand-in at 1× (the cannon's shield, the gold beams, the lamps, panels heating, a rim tell; a generator dead, a panel down, the muzzle glow, bays and racks open); the cannon base (whole, damaged, dead) and turret at 64 headings, the generators (lit, damaged, dead, grafted), the batteries, the mirror panels at their 18 headings with heat, cracks, the red rim, the shattered stub and the sweep's glint; the tells (muzzle, ports, bays, racks, lamps); gold 36 %, atlas 49 % of a page (sheet) | chosen (round 80 A.1-A.3 ok; the escape scene's frames follow round 81 B.1) |
| [concept/ascendant-final-r80-a.gif](concept/ascendant-final-r80-a.gif) | The arrival (the hull sliding in bow-first, its lamps coming on from the bow back) and phase 1: the turret tracking the ship, panels turning and heating, the lance's muzzle glow, a rim tell, the ports' flash, the bays opening | chosen (round 80 A.1-A.3 ok; the escape scene's frames follow round 81 B.1) |
| [concept/ascendant-fx-final-r80-a.png](concept/ascendant-fx-final-r80-a.png) | The eruption (the keel's teal cracks, six keel frames over the hull and both breaches), phase 2 at data.yaml's places (the side sacs in the breaches, the third on the keel, the heart over the breakout; sacs: rest, swelling, split, dead; the heart sealed to open; lime cores, the teal healing pulse, the brood flare), K (the heart and sac bursts, plates, shards, fire), the wreck's two halves on the authored jagged fracture with their hot rims and the orange/teal vents, K and the escape at 1.2, 4, 10 and 14 s, the lifeboat (five weave tilts, drive, wake, breakout tear, the gate flash) (sheet) | chosen (round 80 A.1-A.3 ok; the escape scene's frames follow round 81 B.1) |
| [concept/ascendant-fx-final-r80-a.gif](concept/ascendant-fx-final-r80-a.gif) | The shudder and the eruption, phase 2 (a sac healing, a brood swelling), K (bursts, the chain down the keel, the flash at 2.5 s), the escape (the halves drifting apart along the fracture, venting and burning, the lifeboat weaving up with its wake) and its flash at the top edge | chosen (round 80 A.1-A.3 ok; the escape scene's frames follow round 81 B.1) |
| [concept/ascendant-fight-r80-a.mp4](concept/ascendant-fight-r80-a.mp4) | The whole fight in the game at medium, real time with sound (Level 42's run, the content autopilot on the balance plan's fit with Rook): the warning, the arrival, phase 1, the eruption and Vorne's shout, the sacs and the heart, the break, the lifeboat's escape, the closing lines (2:58) | superseded (round 80 B.1 change, shown again in r81-a; V.1 ok: Vorne's lines accepted from it) |
| [concept/phase1-capture-r80-a.png](concept/phase1-capture-r80-a.png) | In the game, 1×: phase 1, the batteries' panels up (one heating under the beam), then down under the EMP Burst's ring | chosen (round 80 A.1 ok) |
| [concept/phase2-capture-r80-a.png](concept/phase2-capture-r80-a.png) | In the game, 1×: phase 2, the sacs on the Vrell half; the heart open | chosen (round 80 A.2 ok) |
| [concept/halves-capture-r80-a.png](concept/halves-capture-r80-a.png) | In the game, 1×: the wreck's two halves parted on the jagged fracture, the lifeboat climbing between them | chosen (round 80 A.3 ok) |
| [concept/rejected/escape-capture-r80-a.png](concept/rejected/escape-capture-r80-a.png) | In the game, 1×: the escape bar with 12 s and 1 s left (≈ 55 % of the cap), a stronger easy run's ≈ 97 % at 1 s, `ESCAPED` (no run reached `MAX`) | rejected (round 80 A.4 change: the user found the chase and its bar unclear; the escape is now a short scene with no bar, shown in round 81) |
| [concept/ascendant-fight-r81-a.mp4](concept/ascendant-fight-r81-a.mp4) | The whole fight after round 80's fixes, in the game at medium, real time with sound (r80's recipe, a new run): phase 1's lance sweeps (with their red tell fans), battery barrage and twin lances, the heart's death taking the bar, the break, the lifeboat's 4 s climb, Vorne's escape line and the sting; Rook survives (2:25) | proposed (round 81) |
| [concept/ascendant-fx-final-r82-a.png](concept/ascendant-fx-final-r82-a.png) | Final frames as r80's fx sheet, round 81 (B.1): the wreck's halves now carry the rest of the hull above the top edge (the stern with its frames and the keel's tissue, the engine block's six nozzles at its end; the fracture run on through it), K and the escape at 1.2 s, the pod hanging, climbing, `ESCAPED` (sheet) | proposed (round 82) |
| [concept/ascendant-fx-final-r82-a.gif](concept/ascendant-fx-final-r82-a.gif) | r80's fx animation with the 9.5 s drift from the flash: the halves sinking apart with their sterns, the pod hanging 1.6 s, then climbing 7.4 s | proposed (round 82) |
| [concept/ascendant-fight-r82-a.mp4](concept/ascendant-fight-r82-a.mp4) | The whole fight after round 81's fixes, in the game at medium, real time with sound (r80's recipe, a new run): phase 1 never quiet (rests 1.0 s, the port pairs, the barrage's first burst as its tell ends), the eruption at 1:36, the heart's death 2:18, the halves sinking with their sterns, the pod hanging 2:21.5–2:23, its climb under Vorne's whole escape line to `ESCAPED` and the sting at 2:30.5; Rook flies the whole fight (2:37) | proposed (round 82) |

## Implementation

Built in M10 part H with Level 42 (round 72 approved the design).

- [x] Stat block and boss script in a `data.yaml` (the parts per phase, the shields, the panels and their arcs, the
      regeneration, the playbooks, the broods, the eruption, the lifeboat's path, par); global difficulty
      multipliers applied (step H2; par the paper's 150 until step H3 measures)
- [x] The phase-1 panels on the shared [`reflect`](../../README.md#reflect) code (fixed mounts turning within their
      range), the generator-fed shield (240 / 120 / none) (step H1a: `PartMirrors`, `PartShields`)
- [x] The phase-2 regeneration on the shared [rule](../../README.md#regeneration), the heart's membrane, the broods and
      lobs; the playbooks per phase (seeded draws, never the same twice, the broods and Darts at most every third) (steps H1a, H1b: `PartRegen`, the shared `Playbook`)
- [x] **A boss that escapes** (the gap analysis §2.10: medium): the hull's break as phase 2's end (flash, credit
      shower), the lifeboat's hold and climb as scenery (no hitbox, pay, wake or contact; no bar from K; round 80; round
      81: the 1.6 s hold and the 7.4 s climb), `ESCAPED`
      winning the level; the boss checkpoint and a replay repeat it exactly (steps H1b, H4a, H4b, round 80: `EscapeRun`,
      `BOSS_ESCAPED`)
- [x] Rook and the autopilot: the generators first, the far-side flank, the weakest sac, out of the lance and the marks (steps H1b, H3, I1: `AscendantPlay`, `RookWeave`; in step H5's recording Rook went down 18 s into phase 1: the approach had taken his armour, see Level 42's Decisions)
- [x] Round 80's phase-1 moves (B.1): the lance sweep, the twin lances and the battery barrage, a lance at least every
      second draw; the autopilot keeps out of a sweep's turning beam and both twins (step R80a: `BossLines`' sweep and
      twin, `BatteryBursts.cue`, `AscendantLancesTest`; phase 1 measured inside its old time)
- [x] The fight measured over five seeds (the window, the HP tuned, the par set), phase 1 against the
      Kraken's band, the bounty at ≈ 24 % with Level 42's scale (steps H3, I2, round 80: `AscendantFightTest`; round 81:
      medium par 144 s, hard ×1.05)
- [x] Round 81 (B.1): phase 1 never quiet for more than 1.0 s at medium and hard (`AscendantFightTest`'s quiet probe,
      `AscendantPressure`), a ship standing still loses armour at medium on every seed, Rook through phase 1 on most
      seeds; the playbook's fallback (`PhasePlaybook.fallback`), the moves' port-fan cover (`PhasePlaybook.Move.cover`),
      the barrage's first battery under its tell; the escape's 1.6 s hold and 7.4 s climb (`EscapeSpec.holdSeconds`,
      Vorne's line at the climb); the wreck's halves with the stern (`tools/art/ascendant.py`, `STERN`)
- [x] Vorne's lines in this order in Level 42's radio timeline, line 5 `shout: true` (the voice check's one exception:
      content's `BowChecks` rejects `shout: true` on Vorne's cues but at an escaping boss's eruption; step H2)
- [x] Production art (the hull pieces, the parts' states, the eruption, the wreck halves, the lifeboat); readability
      checks over Level 42's backdrop (step ART-H1; seen in the game in step H5's recording)
- [x] Sounds (Freesound, CC0/CC-BY) per the brief; the music hand-off 19 → 18 and the escape sting (steps AUD-H, H4b; round 80: S.1 b, S.2-S.11 a, the choices in the game)

## Open questions

Both left to the build are settled by the measures (step H3, I2; *Duration and HP*): the HP **14,200** (phase 1 7,800,
phase 2 6,400; the medium fight 144 s with round 81's never-quiet phase 1 and 9 s escape, inside the window); the lifeboat's cap is gone (round
80). None open.

## Decisions

- 2026-09-30: Drafted in the bosses roster as the Act 6 boss (L42): Vorne's flagship, half warship and half Vrell
  organism; phase 1 the gold hull with shield generators and mirror panels, phase 2 the Vrell half regenerating and
  spawning Chimeras, phase 3 the lifeboat breaking free and fleeing (it escapes); Vorne's taunts.
- 2026-10-31 (Act 6 design phase, step m10-d-boss; **decided 2026-10-31 (round 72)**): promoted from the
  roster to a draft spec on the gap analysis's recommendations: Q6 a (bow-on over a halted arena; the lifeboat cannot
  die, its damage pays up to a cap; the scripted end the bosses README proposed), Q1 c (the panels), Q9 a (the elite
  livery), J1 (Level 42's reference 168), J6 (the shout at the eruption), J9 (19, then 18, the escape sting). HP 15,200
  (phase 1 8,800, phase 2 6,400) on the Sovereign's measured rate (0.87 × reference), paper ≈ 150 s with the escape,
  par 150 until measured; the bounty 435 in Act 1 terms (24 % of budget(42) ≈ 2,692 paid at a hand scale 0.59),
  split 45 / 40 / 15 with the lifeboat's share paid pro rata to its damage (the gap analysis's 45 / 40 / 15 HP split
  read as the bounty's: the lifeboat has no HP to share). Trivia decided here: the phases end on their parts (any
  order), the bar is the hull's total and the lifeboat has the escape bar; the panels face 45° to their side and turn
  ±20° (open from the far side); the generators feed the cannon's shield; the heart opens after two sacs; the
  broods count into the level's enemies, not its density; the hull breaking takes the screen flash and the credit
  shower, with no `huge` death explosion; the Choir speaks before Vorne's shout; the lifeboat's look seeds Vorne's
  Chimera (Act 7).
- 2026-10-31, design phase (step m10-d-act; decided 2026-10-31 (round 72)): the bounty follows Level 42's hand scale **0.64**:
  **401** in Act 1 terms (phase 1 180, phase 2 161, the lifeboat 60), 2,691 paid (24.0 % of budget(42)); the build
  recomputes it with the measured scale. Line 4 (the Choir at the eruption) is its own claim on Vorne, "*…the one who
  opened… is inside us now… he was always inside us…*", so the Choir never repeats Level 37's line (the story step's
  draft, accepted).
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): **Q6 a** (bow-on over a halted
  arena, the lifeboat escaping; b a flank pass and c two bosses rejected), Q1 c (the panels follow the `reflect`
  rule), J6 (Vorne's one shout at the eruption), J9 (track 19 in phase 1, track 18 from phase 2 by a crossfade, M.3 a;
  the sting M.4 a) and the document approved as drafted (D.13; `design: approved`); **A.10 a**, "the gilded wedge",
  chosen (`art: chosen`; b "armoured tiers" in `concept/rejected/`). **A.11 change**: the hull breaks along a **jagged
  fracture**, never a clean cut (see *The hull breaks*); the storyboard rows stay `proposed` ("change requested") and
  a follow-up art step re-renders frame 8 with a jagged break; the phase order, the warning and the escape are
  accepted.
- 2026-10-31, round 73: the storyboard's jagged break accepted (R.1): round 72's two storyboard rows are superseded and rejected.
- 2026-11-02 (M10 part H, step H2; trivia decided 2026-11-03 (round 80, no objection; the fight returns in round 81 B.1)): the stat block and the fight's script
  in [data.yaml](data.yaml), on steps H1a/H1b's simulation. The parts at the approved concept's places (A.10 a's sheet:
  the side sacs in the breaches where the upper batteries stood, (150, 125) and (330, 125), the third sac high on the
  keel at (240, 60), the heart at (240, 152)), not the test Ascendant's (200, 50), (280, 50), (240, 150) and (240, 100)
  that ART-H1 drew: ART-H1 reruns. The bow's tip at **y 262**, ART-H1's drawn tip (the prose's 250), the wedge's flat
  tip 80 px across (the cannon's base). **The lifeboat's climb** (the contradiction step H1b found: 20 → 90 px/s cannot
  fit its ≈ 205 px in 14 s): the 14 s (hard 12), the weave and the climb's shape are kept, the speeds read as the
  climb's start-to-end ratio (≈ 5 → 24 px/s), pending the user's look at the recording. The lifeboat's cap is its
  part's HP (the HP lever: easy 1,050, hard 1,820) and stays out of the boss's 15,200; the panels' heat takes the HP
  lever too (easy 195, hard 338). Every part `destroyable`, none vital: the hull breaks on the heart. The eruption's
  armour patch falls 40 px under the bow's tip. The bounty at Level 42's measured `bounty_scale` **0.63: 407** (phase 1
  183: the cannon 113, each generator 15, each battery 10; phase 2 163: the heart 115, each sac 16; the lifeboat 61).
- 2026-11-02 (M10 part I, step I2; decided 2026-11-03 (round 80, no objection; the fight returns in round 81 B.1)): hard's HP lever 1.15 (the difficulty hook `hp`, in place of
  the difficulty's `enemy_hp` 1.3; the lifeboat's cap 2,875): the hard fight 166.8 s (159.6-172.3, five seeds), inside
  the act boss's 90-180 (188.3 s before); hard's rests and regeneration as they were (at 1.0 each the fight was 186.8 s:
  the HP is the lever); medium unchanged.
- 2026-11-02 (M10 part H, step H5): built and recorded: the whole fight at medium in real time with sound
  (`ascendant-fight-r80-a.mp4`) and four stills, shown in round 80. The prose numbers follow the measures (steps H3, I2:
  14,200 HP, the cap 2,500, par 142 s). Seen in the recording: Rook goes down 18 s into phase 1 (the content tests'
  plan flights keep him to the escape: a debug start flies him at full armour on the same commands, so his loss here is
  the fight's, noted for part I); the autopilot's damage on the lifeboat stays under the cap (≈ 55 % at medium, ≈ 97 %
  on easy with a stronger fit), so no capture shows `MAX`.
- 2026-11-03 (user, round 80 answers): **ok** A.1, A.2, A.3 (the art in the game), V.1 (Vorne's lines), S.1 **b** and S.2-S.11 **a** (the sounds, in the game; the other variants rejected); **change** B.1 (more and different firing in phase 1, the rail gun more often; phase 2 "much more challenging already"; the final part unclear) and A.4 (the objective unclear): both built in R80a and shown again in round 81 B.1.
- 2026-11-03: round 80 (B.1/A.4): the user found the lifeboat chase unclear; the escape is a short scene (≈ 4 s climb, no hitbox, no bar), its 15 % share paid with the hull.
- 2026-11-03: round 80 (B.1, the user: phase 1 needs more and different firing sequences, the rail gun more often):
  phase 1's playbook gains the lance sweep, the twin lances and the battery barrage, and a lance (the lance, its sweep
  or the twins) at least every second draw; the rests stay (phase 1 measured 77.6–83.6 s after the bar at medium, the
  old playbook's 76.5–88.8). Phase 2 stays (the user: "much more challenging already").
- 2026-11-03: round 81 (B.1, the user: "still too many quiet periods ... the shield regenerates before the next round ...
  the escape is way too quick now ... there's no back of the ship"; step R81a): phase 1 is never quiet for more than
  1.0 s (measured from its first shot: bullets closing on the ship, missiles, Darts, live beams) and standing still
  costs armour, both tested at five seeds: the rests 1.0 s / 0.7 s (were 3.5 / 1.9), the port fan (a narrow 2-way aimed
  pair, so it keeps off Rook) covering every move as it ends and each lance as its line is drawn, the gold fan two fans
  (was three), the barrage's first battery telling under the barrage's tell, and with the cannon dead the playbook drawing
  on from the generators and batteries (the salvo, the stream, the barrage). Rook comes through phase 1 on 8 of 10 seeds
  at medium (R80a 9 of 10; he reaches the boss with 3–58 of 80). The par 131 → 144 s; hard's HP lever ×1.15 → ×1.05 (at
  ×1.15 hard ran 176–191 s); the random dodger's loss 3.93× the Kraken's (the pending bound 2.8 → 4.2); medium's Act 6
  credits end −15.5 % against the expert's bound (was −14.9 %; pending, part I's I3). The escape scene is ≈ 9 s from the
  breakout: the pod hangs 1.6 s, then climbs 7.4 s with Vorne's escape line (now at the climb's start) heard in full. The
  wreck's halves carry the stern and its engines above the top edge (production art, `ascendant.py`), so no cut edge shows
  as they sink.
