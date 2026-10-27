---
title: Iron Sovereign
design: draft
implementation: not-started
art: proposed
depends-on: [../../../world/belt, ../../../campaign/act-5-the-belt, ../../ground/sam-nest, ../../ground/rail-bunker, ../../ground/sentinel-tower, ../../air/dart-drone, ../../../player/wingmen, ../../../story/characters/vorne, ../../../systems/economy]
updated: 2026-10-27
---

# Iron Sovereign

## Summary

The Act 5 boss (Level 35, Ceres orbit): the Ascendancy battle station that holds the Belt. Seen from
above, three concentric rings turn round an armoured reactor core at different speeds and in different
directions; their batteries (SAM launchers, rail guns, laser emitters) fire only while rotation carries
them into the arc facing the ship. Killing all of a ring's batteries **locks** that ring; with all three
locked the core opens and fights on with spirals, Dart Drone streams and Vorne's composure cracking.
After its death Vorne offers Lancer a job.

**Status: draft** (Act 5 design phase, step D-boss, 2026-10-27), **pending the user's review in concept
round 60**. It follows the gap analysis's recommended answers, all pending in round 60: **Q7 a** (the
top-down rings; the alternatives under *Variants b and c*), **Q1 a** (the core's hex shield), **Q6 b**
(the core's Dart streams), **Q11 a** (the belt look) and judgement call **J2** (Level 35's reference DPS
144). Every number is a hand-written **draft** at medium; there is no `data.yaml` yet (M9 part H writes
it once the design is approved).

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
The reference DPS is the **proposed** Level 35 value of **144** (judgement call J2, pending round 60).
The batteries reuse their ground units' attacks: their numbers live in those units' documents and are
not repeated here; the boss sets only where and how often they fire.

| Field | Value |
|---|---|
| Faction | Jovian Ascendancy; a **machine** (but a boss: never stunned, its shield emptied by the EMP Burst) |
| Layer | `ground` (the station seen from above, in orbit under the play plane): every weapon hits its parts, `anti-ground` ×2; **not hardened** (unlike the Rail Bunkers on its outer hull in the approach); no collision with the ship or the escort, who fly over it freely |
| Size tier | `huge` |
| Size | ≈ 440 px across (the outer ring's rim at radius 220 from the centre); the core 80 px across, hitbox 70×70; batteries: SAM 40×40 (hitbox 30×30), rail 48×48 (36×36), laser 44×44 (32×32) |
| Parts | the **core** (`vital`; armoured while shut, phases 1–3); the **outer ring** (6 SAM batteries), the **middle ring** (4 rail batteries), the **inner ring** (3 laser batteries): every battery `destroyable`; the rings' hulls are **not hit areas** (shots pass over them as over terrain) |
| Orientation | the rings `radial` (they turn about the centre; see *Presentation* for how they are drawn); every battery's turret `64 angles`, aiming on its own while its mount turns with the ring |
| HP | SAM battery 220 each (1,320), rail battery 330 each (1,320), laser battery 440 each (1,320), core 5,000; **total 8,960** (easy 6,720 / hard 11,648, from the global multipliers); the core's hex shield 80 on top (phase 4) |
| Armour / shield | the core is armoured (shots spark grey, no damage) until it opens; open, it carries a **hex shield** of 80 under the [enemy shield rule](../../README.md#enemy-shields) (Q1 a: every weapon wears it down, the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md)'s `shield-breaker` empties it, the EMP Burst strips it) |
| Speed | 0 px/s (the scroll halts over it); the rings turn: outer 6°/s clockwise, middle 10°/s counter-clockwise, inner in steps (120° in 4 s, then a 4 s hold), each lock speeding the survivors up |
| Movement | anchored, its centre at (240, 160) (see *Arena*) |
| Attack | per battery, only while its mount is inside the **fire arc** (see below): SAM batteries the [SAM Nest](../../ground/sam-nest/README.md)'s salvo (its homing diamonds, its tell), one salvo per battery every 6 s; rail batteries the [Rail Bunker](../../ground/rail-bunker/README.md)'s rail line (its lock, telegraph, band and `heavy` hit), one per battery every 5 s; laser batteries the [Sentinel Tower](../../ground/sentinel-tower/README.md)'s arc sweep (its telegraph, beam width and `laser` hit) with a 300 px beam, once per inner-ring hold; the shut core (phases 1–3) an aimed `fan` of 5 over 40° (`small`, 150 px/s) through its shutter slits every 3.0 s (2.5 s in phase 2, 2.0 s in phase 3), after a 0.5 s glow; the open core (phase 4) a 2-arm `spiral` (`small`, 130 px/s), [Dart Drone](../../air/dart-drone/README.md) streams and, below half its HP, its own rail line |
| Formations | solo set piece (the roster's turret nest, three rings deep) |
| Weak points | none beyond the parts: the core is the vital part once open |
| Effective traits | `anti-ground`, `piercing`, `homing` (the batteries in the arc), `shield-breaker` (the core's shield, a bonus only) |
| Credits | 457 in Act 1 terms (core 379, every battery 6); × 6.5536 (Act 5's factor 1.6⁴) × Level 35's `bounty_scale` 0.56 it pays **24 % of budget(35) ≈ 1,677** (see *Bounty*) |
| Death | `huge` vacuum variant: the wrecked batteries cook off ring by ring, the core collapses and blows, the rings break into drifting segments; a 5 s chain, screen flash, credit shower (see *Death*) |
| First level / used in | L35; the L49 boss rush's echo (see *Echoes and relatives*) |
| Difficulty hooks | hard: rings ×1.2 faster, fire arc 150°, battery intervals ×0.8, the core's spiral 3 arms, Dart streams +1, no boss checkpoint; easy: fire arc 90°, battery intervals ×1.3, the core's spiral 2 arms at 3 bullets/s an arm, no core rail line |

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
apart); a rail battery holds fire while the ship is within 100 px (the bunker's); no
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
| 4 — The core | the core at 0 | The station shudders and every ring goes dark; the core's six shutter petals **open over 3 s** (invulnerable while they open; a klaxon), and its reactor glows white-gold under a gold **hex shield** of 80 (Q1 a). The core fires a 2-arm `spiral` (`small`, 130 px/s, the arms turning 72°/s, 5 bullets/s an arm) for 4 s, then rests 2 s; its four rim bays launch **Dart Drone streams** of 4, one bay every 5 s, the bays nearest the ship first (a bay within 72 px of the ship skips its turn); below **50 %** of the core's HP it adds its own **rail line** every 6 s (the Rail Bunker's telegraph and hit, from the core's muzzle). An **armour patch** drops as the phase begins. |

A lock needs the ring's **every** battery dead; its last battery's kill starts the halt at once. A
locked ring blocks nothing (its hull is no hit area).

### Dart Drones

Q6 b, pending round 60: the [Dart Drone](../../air/dart-drone/README.md), launched only in phase 4. A rim
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
| Core `spiral` | the reactor brightens, the hex shield ripples | 0.6 s |
| Dart stream | a rim bay's doors open with a gold light | 0.5 s |
| Core rail | the Rail Bunker's line, from the core | its own |

### The escort (Rook)

Rook is back in the escort slot from Level 30 ([wingmen](../../../player/wingmen/README.md)). He follows
his boss rules: he shoots the nearest battery **inside the fire arc** first (then the nearest of the
others; the core once open), steps out of a telegraphed rail line and a sweep's wedge like the ship,
and chases Darts that come within his reach. His state is in the boss checkpoint as usual. With
`--escort none` the fight is the same without him. The test autopilot does the same, so the balance and
playthrough tests can finish the fight.

### Duration and HP

Target: the act-boss window **90–180 s** at medium, aimed at ≈ **150 s**; the **par** is 150 until part H
measures the mean.

Paper estimate: the effective boss DPS is 0.6 × 144 = **86.4**. Phases 1–3 at ×0.7 (moving targets, the
choice of battery, shots that pass between batteries over the hulls): ≈ 60.5 on the batteries. Phase 4
at ×0.85 (the shield's regrowths, fire spent on Darts): ≈ 73.4 on the core. `anti-ground` weapons (×2)
and `piercing` columns make it shorter for the loadouts the level asks for: the measurement counts
them, the paper does not.

| Phase | HP | Time |
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
measures it (see *Open questions*).

Lessons from Acts 2–4 the build must apply (M9 part H):
- **Measure, then tune the HP, not the attacks** (Kraken, Spire, Colossus, walker, Maw): the autopilot
  with the Act 5 balance plan's Level 35 fit over five seeds, every seed inside 90–180 s, the par at the
  measured mean; tune the core's HP first, the batteries' only if a ring phase runs long.
- **Phase 1 against the Kraken's** (the calibration band 0.8–1.2×): the levers are the batteries'
  intervals and the core's `fan`.
- The autopilot and Rook must leave a rail line and a sweep's wedge, pick armed batteries first and
  stay out of the core's 72 px when it opens, or the tests cannot finish the fight.

### Bounty

An act boss aims at ≈ **24 %** of its level's budget (the economy's share under the perfect-run cap;
Level 21's and Level 28's 24 %). budget(35) = 700 × 1.07³⁴ ≈ **6,985**; 24 % ≈ **1,676 paid**. Data is in
Act 1 terms: **bounty = 1,676 ÷ (6.5536 × `bounty_scale`) ≈ 256 ÷ `bounty_scale`**. Level 35's scale is
the level document's: at its hand estimate of **0.56** ([Level 35](../../../campaign/act-5-the-belt/level-35-iron-sovereign/README.md#credit-budget))
that is **457** (core 379, each of the 13 batteries 6), paid **1,677 = 24.0 %** (this draft first assumed 0.75,
Act 4's Level 28 scale, and 341). When the level's scale is measured, the bounty is recomputed with the same
formula; the share stays at 24 %. The Darts pay their own bounty, outside the share.

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
and times them (its radio's order and delays). Drafts, pending round 60:

| When | Line (draft) |
|---|---|
| `boss-arrival` (the intro) | "Welcome to Ceres, Lancer. The Sovereign was built to hold the Belt. Do try to make this interesting." |
| `boss-phase` (the first lock) | "Such dedication. Such waste. You could have been on the winning side, pilot." |
| `boss-phase` (the core opening) | "Enough. You are becoming expensive." (the portrait's *rage* alternate; see below) |

**His composure cracks, he does not shout**: the gap analysis's "Vorne's rage" for phase 4 meets his
character rule (he never shouts until Act 6's end), so the third line is short, cold and clipped, under
the bared-teeth portrait with heavier static, not raised. (Judgement call of this step, pending round 60.)

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

- **Look brief (a)** (Q11 a, the belt look): a vast disc of **gloss black** hull with **gold trim on at
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

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-world; pending the user's review) — two looks of the
top-down rings (Q7 a) in the belt look of [Q11](../../../world/belt/README.md#open-questions) a, each a PNG sheet
(the station at rest, the segments and a seam, the batteries out of and in the arc and wrecked, the core shut,
opening, open under its hex shield, readability crops, key frames, the measured atlas estimate) and a 26 s GIF in
the 480×540 play field (intro, phase 1, a cut to the outer ring's lock, a cut to the core, the death chain); the
rings are drawn from identical segment sprites at 1-degree headings under one fixed sun. Q7's b and c are described
under *Variants b and c* only (no art). Generator `tools/concept/belt_r60.py`; prompts:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/iron-sovereign-r60-a.png](concept/iron-sovereign-r60-a.png) | Iron Sovereign A, continuous wheel — the segments butt into smooth rings, gold lips and teeth on every rim, red lights on the ribs (sheet) | proposed |
| [concept/iron-sovereign-r60-a.gif](concept/iron-sovereign-r60-a.gif) | Iron Sovereign A, continuous wheel — the fight and the death in the play field (motion) | proposed |
| [concept/iron-sovereign-r60-b.png](concept/iron-sovereign-r60-b.png) | Iron Sovereign B, armoured links — every segment a separate block with a dark gap and a gold-edged cap plate, the gaps reading the turning (sheet) | proposed |
| [concept/iron-sovereign-r60-b.gif](concept/iron-sovereign-r60-b.gif) | Iron Sovereign B, armoured links — the fight and the death in the play field (motion) | proposed |

## Implementation

To be built in M9 part H with Level 35, once round 60 approves the design.

- [ ] Stat block and boss script in a `data.yaml` (the rings, their radii, speeds, directions and steps,
      the batteries and their units' attacks, the fire arc, the core's phases, the Darts, par), loaded;
      global difficulty multipliers applied
- [ ] The **rotating-ring mode** (new; gap analysis §2.8, size L): parts whose hitboxes orbit the centre,
      mounts turning with their ring, turrets aiming independently, firing gated by the arc that follows
      the ship; the inner ring's steps and holds; reusable by the Halo Platform (Act 6) and the L49 echo
- [ ] Ring locks as phase triggers (every battery of a ring dead, any order), the survivors' speed-ups,
      the bar of the station's total (batteries and core); the boss checkpoint keeps the ring angles,
      speeds, locks and the batteries' HP, and a retry from it and a replay repeat the fight exactly
- [ ] The core: armoured while shut and its `fan`; the opening; the hex shield (the
      [enemy shield rule](../../README.md#enemy-shields)); the `spiral`, the Dart streams from the rim bays,
      the core rail below half
- [ ] Rook and the autopilot: armed batteries first, out of rail lines and sweep wedges, the core's
      72 px kept
- [ ] The fight measured over five seeds (the window, the HP tuned, the par set), phase 1 checked
      against the Kraken's band, the specials' per-part damage checked, the bounty at ≈ 24 % with Level
      35's scale
- [ ] Intro (the halt, the spin-up, Vorne's line), death (the cook-offs, the implosion, the segments
      drifting, flash, credit shower), Vorne's taunts and the recruitment in Level 35's radio timeline
- [ ] Production art (≤ one atlas page): the ring segments at their heading sets, the batteries' mounts
      and turrets, the core shut / opening / open, the locked look, the death, the intel silhouette;
      readability checks over Level 35's backdrop
- [ ] Sounds (Freesound, CC0/CC-BY) per the brief; the music #19 from the warning

## Open questions

All pending the user's review in concept round 60:

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
- **Q11** (the look): **a** gloss black with gold trim and red lights, the backdrops a step lighter (this
  document); **b** gunmetal hulls with gold (reads better, diverges from the final L28 cruisers);
  **c** black silhouettes with gold accents (cheapest, poor at speed).
- **J2**: Level 35's reference DPS 144 (the HP above follow it).
- Vorne in phase 4: cold and clipped, never a shout (this document), or the gap analysis's "rage" as a
  raised voice (against his character rule, which saves the shout for Act 6's end).
- The specials on a many-part boss: keep the per-part caps (a Smart Bomb worth up to ≈ 1,950 on a full
  station; this document, part H measures), or cap a special's total on one boss.

## Decisions

- 2026-09-30: Drafted in the bosses roster as the Act 5 boss (L35): a battle station with three
  concentric rotating rings whose batteries (rail, SAM, laser) rotate into view; destroy a ring's
  batteries to stop it, stop all rings to expose the core.
- 2026-10-27 (Act 5 design phase, step m9-d-boss; **pending the user's review in round 60**): promoted from
  the roster to a draft spec on the gap analysis's recommendations: Q7 a (the top-down rings over Ceres
  orbit, the scroll halted; phases Three rings, Two rings, One ring, the Core), Q1 a (the core's hex shield
  80), Q6 b (Dart streams in phase 4), Q11 a (the look). HP 8,960 (13 batteries 3,960, core 5,000) from the
  proposed L35 reference DPS 144 (J2); paper ≈ 145 s, par 150 until measured; bounty 341 in Act 1 terms
  (24 % of budget(35) ≈ 1,676 paid at a hand `bounty_scale` of 0.75, recomputed with the level's).
- 2026-10-27, design phase: reconciled with Level 35 (step m9-d-act; pending the user's review in round 60): the
  bounty is **457** in Act 1 terms (core 379, 13 batteries × 6) at Level 35's hand scale **0.56**, paid 1,677 (24 %
  of budget(35)), not 341 at the assumed 0.75; **this document owns Vorne's Level 35 wording**, the level places
  and times the lines (the sentence that gave the wording to the level corrected).
  Trivia decided here: the rings' hulls are no hit areas; rings lock in any order and each lock is a
  phase; the bar is the station's total; the fire arc follows the ship (clamped to the lower half); the
  inner ring steps and holds so its sweeps never move under their telegraph; the batteries reuse the SAM
  Nest's, the Rail Bunker's and the Sentinel Tower's attacks; the station is `ground` but not hardened;
  Vorne announces the boss and cracks without shouting; the rings drawn from segment sprites for the
  one-page budget; the L49 echo proposed as the outer ring round the open core.
