---
title: Ascendant
design: approved
implementation: not-started
art: chosen
depends-on: [../../air/mirror-interceptor, ../../air/chimera, ../../air/dart-drone, ../../ground/sam-nest, ../../../world/jovian, ../../../campaign/act-6-jovian-storm, ../../../player/wingmen, ../../../ui/hud, ../../../story/characters/vorne, ../../../systems/economy]
updated: 2026-10-31
---

# Ascendant

## Summary

The Act 6 boss (Level 42, Callisto orbit): Vorne's flagship, half Ascendancy warship and half Vrell organism. It
comes **bow-on** over a halted arena, its gold hull filling the top of the field. Phase 1 is the warship: shield
generators, mirror-panelled batteries and the bow cannon. In phase 2 the Vrell half erupts through the hull, with
regenerating tissue sacs, a heart and Chimera broods, and Vorne shouts for the only time. Phase 3 is the escape:
Vorne's Vrell **lifeboat** breaks from the wreck and flees up the screen. It cannot die; damage on it pays up to a
cap, and it always escapes toward the gate.

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
`heavy` = 10, `laser` = 8) and contact damage from the [balancing basis](../../README.md#balancing-basis). The reference
DPS is the **proposed** Level 42 value of **168** (J1, the
[Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft); decided 2026-10-31 (round 72)). Borrowed attacks keep
their units' numbers (not repeated here).

| Field | Value |
|---|---|
| Faction | Hybrid: Vorne's flagship (the elite livery) with the Vrell half inside it; a **machine** (a boss: never stunned; the EMP Burst empties its shield and shatters its mirrors) |
| Layer | `air` (`space` is `air` in vacuum, the [Jovian media](../../../world/jovian/README.md#media-per-level)); the hull is a body: contact `huge` 25 once and the ship pushed out |
| Size tier | `huge` |
| Size | the visible bow ≈ 400 px wide at the top edge, tapering to a 100 px bow tip at y 250 (the hull runs on beyond the top edge); the lifeboat 56×80 px, hitbox 40×60 |
| Parts | phase 1: the **bow cannon** (`vital` for the phase), 2 **shield generators**, 4 **mirror batteries**; phase 2: 3 **tissue sacs**, the **heart** (`vital`); phase 3: the **lifeboat** (cannot die). Every part `destroyable` except the lifeboat; the hull itself is no hit area (shots spark on it) |
| Orientation | fixed (bow down); the batteries' panels turn (see *Arena*); the cannon aims at 64 headings |
| HP | phase 1: bow cannon 2,800, generators 800 each, batteries 1,100 each (**8,800**); phase 2: sacs 1,300 each, heart 2,500 (**6,400**); **total 15,200** (easy 11,400 / hard 19,760, the global multipliers); the lifeboat's **damage cap 1,400** (no HP: see *The lifeboat*) |
| Armour / shield | the bow cannon carries a gold hex **shield** under the [enemy shield rule](../../README.md#enemy-shields): **240** while both generators live, 120 with one, none with neither; each battery stands behind a **mirror panel** under the [`reflect`](../../README.md#reflect) rule (Q1 c; heat **260**, ≈ 2.0 s of the plan's Ion Beam L5; down 3 s); the sacs and the heart **regenerate** under the [regeneration](../../README.md#regeneration) rule (sacs 4 % a second after 1.5 s, the heart 2 % a second after 2.0 s); the heart is sealed under a tissue membrane (shots spark teal, no damage) until two sacs are dead |
| Speed | 0 px/s (anchored; the scroll halts); the lifeboat 20 → 90 px/s up the screen |
| Attack | phase 1: the bow cannon's playbook (spinal lance, gold fan, missile salvo, Dart stream) and the batteries' aimed `burst`s; phase 2: the Vrell playbook (Chimera brood, spore spiral, bile lobs, grafted guns); phase 3: the lifeboat's spore wake (see *Phases*) |
| Formations | solo set piece; it launches [Dart Drones](../../air/dart-drone/README.md) (phase 1) and [Chimeras](../../air/chimera/README.md) (phase 2) |
| Weak points | the sacs and the heart glow **lime** at their cores (the bosses' weak-point rule), with the hybrids' teal seams round them |
| Effective traits | `piercing` (the cannon and the batteries stacked on the bow), `homing` (the broods, the lifeboat), `shield-breaker` (the cannon's shield, the mirrors), `side` (the far-side flank of the panels), `beam` (holds the sacs' regeneration, burns a mirror) |
| Credits | **24 % of budget(42)** in all, split 45 / 40 / 15 between phase 1, phase 2 and the lifeboat's damage (see *Bounty*): **401** in Act 1 terms at Level 42's hand `bounty_scale` of 0.64 |
| Death | not a death: at phase 2's end the hull **breaks** (a 3 s vacuum chain along the keel, the act boss's screen flash, the credit shower over the wreck), then the lifeboat escapes (see *The hull breaks*) |
| First level / used in | L42; the lifeboat's look is the seed of **Vorne's Chimera** (Level 48, Act 7's mid-boss, the [roster](../README.md#mid-bosses)) |
| Difficulty hooks | easy: rests ×1.3; the spinal lance's line 1.5 s; batteries' bursts of 2; the spiral 2 arms; regeneration 3 % / 1.5 % a second; the brood 3 Chimeras; the lifeboat's cap 1,050 (its share pays the same); hard: rests ×0.8; mirror arc 140°; batteries' bursts of 4; the spiral 4 arms; regeneration 5 % / 2.5 %; the escape 12 s; no boss checkpoint |

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
fires its aimed `burst` of **3** (`medium` 6, 160 px/s) every **3.0 s** only while its panel faces within 30° of the
ship, at most **two** batteries bursting at once, ≥ 0.6 s apart; no enemy bullet spawns within 72 px of the ship (the
[bullet rules](../../README.md#bullet-readability-rules)); at most **8** of the cannon's missiles alive. A mirror's bounced
bullets are the player's own fire (the rule); Rook's shots are absorbed.

**Intro** (≈ 8 s): **the boss warning before the hull shows** (track 22, the Act 5 lesson) as the scroll slows; then the
hull slides in **bow-first** from the top edge over 6 s, its gold lights coming on from the bow back, the bar
`ASCENDANT` fills; invulnerable until in place. **Vorne** speaks first on the enemy channel (see *Vorne*). The boss
checkpoint is taken at the warning (none on hard); the level clock halts while it lives.

### Phases

The bar shows the hull's **total** HP (phase 1's parts, then phase 2's, 15,200 in all): it falls in every phase and
each part's kill takes its share off it. The lifeboat has its own bar (the escape bar, see *The lifeboat*).

| Phase | Ends at | Behaviour |
|---|---|---|
| 1 — The gold hull | every phase-1 part dead (any order) | The bow cannon draws from its **playbook** (below); the batteries burst on their clock behind their mirrors; the generators feed the cannon's shield. The natural order is the generators (the shield falls), the batteries from the far side, then the cannon; a `shield-breaker` or the EMP opens the cannon early for a few seconds. With the cannon dead first, the batteries fire on and no playbook runs. |
| The eruption | — (4 s, invulnerable) | The hull shudders, the keel splits along the centre line and the **Vrell half erupts** through the gold plates: teal-black tissue, the three sacs swelling out of the breaches, the heart pulsing under its membrane. The screen shakes; track 19 crossfades to **track 18** (J9); the Choir speaks, then **Vorne shouts** (J6, see *Vorne*). An **armour patch** drops. |
| 2 — The Vrell half | the heart at 0 | The sacs **regenerate** (a held beam or steady focus denies it); the heart opens to fire once **two** sacs are dead; the **Vrell playbook** (below) draws from the heart (from the sacs while the heart is sealed). |
| 3 — The escape | the lifeboat off the top edge (≈ 14 s) | The hull breaks; the lifeboat climbs away through the wreck. Scripted, no playbook: it always escapes (see *The lifeboat*). |

**The playbooks** (round 70's lesson, the [Iron Sovereign](../iron-sovereign/README.md#the-round-70-rework-decided-round-70)'s
core): after each attack and a **rest** the part draws its next attack with the simulation's seeded random (a replay
draws the same), **never the same twice running**, each with its own **0.8 s tell** (see *Tells*).

| Phase | Attack | What it does |
|---|---|---|
| 1 (rest 1.5 s; 0.8 s below half the cannon's HP) | **Spinal lance** | a red `laser-line` aimed at the ship's spot for **1.2 s**, locked 0.2 s before it fires, then a 0.5 s beam **24 px** wide down the whole field, `heavy` 10 once |
| 1 | **Gold fan** | three 9-way fans over 80° (`small`, 150 px/s), 0.5 s apart |
| 1 | **Missile salvo** | 6 of the [SAM Nest](../../ground/sam-nest/README.md)'s homing diamonds from the shoulder racks (3 from each rack whose generator lives; a dead generator's rack is dead) |
| 1 | **Dart stream** | two hull bays launch a stream of 4 Dart Drones each (8, under the Darts' cap of 16); at most every **third** draw |
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
| Flight | it climbs from (240, 175) to the top edge in **14 s** (hard 12 s): 20 px/s at first, easing up to 90 px/s, weaving ±60 px on a seeded path; it never comes below y 200 |
| It cannot die | every hit counts its damage toward the **cap of 1,400** (≈ 10 s of the plan's Level 42 fit at the measured boss rate: a focused player reaches it, a busy one does not); past the cap hits spark and pay nothing. Rook's hits and the specials count like the player's (the Lance up to 400) |
| Pay | the lifeboat's 15 % of the bounty is paid **pro rata** as the damage comes in (the floating numbers), the whole share at the cap |
| Its threat | a **spore wake**: every 0.8 s a pair of slow spores (`small` 4) drop behind it, drifting down at 60 px/s; contact `large` 20 once |
| The escape bar | the boss bar becomes the HUD's [escape bar](../../../ui/hud/README.md#act-6-additions-draft) under the lifeboat's name, `VORNE`; the time drains in amber, the gold fill shows the share of the cap, `MAX` at the cap |
| The end | at the top edge it flashes once toward the gate and is gone: the bar reads **`ESCAPED`**, the **escape sting** (track 33) cuts track 18. The level is won; its end and the act's end follow (Level 42's and the act's documents) |

### Tells

| Attack | Tell | Medium (easy / hard) |
|---|---|---|
| The hull's arrival | the boss warning (track 22) before it shows; the gold lights coming on | 5 s + 6 s |
| Battery burst | its panel's rim glows red as the battery charges | 0.5 s |
| Spinal lance | the bow's muzzle glows white-gold, then the red line brightening | 0.8 s + 1.2 s line (easy 1.5 s / hard 1.0 s) |
| Gold fan | the bow's gun ports flash gold | 0.8 s |
| Missile salvo | the shoulder racks' hatches open with a red lock light (the SAM Nest's) | 0.8 s |
| Dart stream | two hull bays' doors open with a gold light | 0.8 s |
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
denies its regeneration, as the rule rewards) and Chimeras within his reach; in phase 3 the lifeboat. He steps out of
a told lance's line and a lob's marks like the ship. The test autopilot does the same, crossing the field to open a
face's batteries.

### Duration and HP

Target: the act-boss window **90–180 s** at medium; the **par** is the paper's **150 s** until the build measures the
mean (then the mean). The escape's 14 s is in the par (it is the same on every run).

**The rate.** The 0.6 × reference rule read the Iron Sovereign far too slow: its build needed **17,220 HP for 137 s of
fire** at Level 35's reference 144 (its measured fight 144 s less its spin-up and opening), **≈ 126 DPS ≈ 0.87 ×
reference**. This draft sizes the Ascendant on that **measured** rate: 0.87 × 168 ≈ **147**. Phase 1 at ×0.9 (beam time
lost on mirrors, the cannon's regrowing shield): ≈ 132. Phase 2 at ×0.8 (the regeneration, fire spent on Chimeras):
≈ 118. (The gap analysis's ≈ 11,500, the Sovereign's draft 8,960 scaled by the references, would measure ≈ 120 s.)

| Phase | HP | Time |
|---|---|---|
| The warning, the arrival, Vorne | — | ≈ 8 s |
| 1 — The gold hull | 8,800 | ≈ 67 s |
| The eruption | — | 4 s |
| 2 — The Vrell half | 6,400 | ≈ 54 s |
| The hull breaks | — | 3 s |
| 3 — The escape | (cap 1,400) | 14 s |
| Total | 15,200 | **≈ 150 s** |

Phase 2 is kept **shorter** than phase 1 (the Sovereign's round 70 lesson: a long last part bores), and both have a
playbook. Lessons the build must apply (M10, the Level 42 part):
- **Measure, then tune the HP, not the attacks**: the autopilot with the hand plan's Level 42 fit and Rook over five
  seeds, every seed inside 90–180 s, the par at the measured mean; tune phase 1's batteries and phase 2's heart first.
- **Phase 1 against the Kraken's** (the calibration band); the levers are the batteries' clock and the rests.
- **The regeneration must not stall**: on every seed phase 2 ends; a sac healed back to full more than twice by the
  autopilot means the rate comes down, not the HP up.
- **The lifeboat's cap**: the autopilot reaches 40–80 % of it at medium; outside that band the cap moves.

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
document's; at its hand **0.64** ([Level 42](../../../campaign/act-6-jovian-storm/level-42-ascendant/README.md#credit-budget)) that is **401** (the first draft's 0.59, Level 35's measured scale, gave 435), split:

| Share | Parts | Act 1 terms | Paid |
|---|---|---|---|
| 45 % | phase 1: the bow cannon 110, each generator 15, each battery 10 | 180 | ≈ 1,208 |
| 40 % | phase 2: the heart 113, each sac 16 | 161 | ≈ 1,080 |
| 15 % | the lifeboat, pro rata to its damage up to the cap | 60 | ≈ 403 at the cap |
| | | **401** | **≈ 2,691 = 24.0 %** |

So the hull pays 85 % of the bounty however the escape goes, and a full share is a reward for the chase. When the
level's scale is set, the bounty is recomputed with the same formula; the shares stay. The boss-launched Darts and
Chimeras pay their own bounty, outside the share.

### The hull breaks

The heart at 0 (**K**): the heart bursts lime-white, the surviving sacs burst with it (paying their share), and a
**chain** runs down the keel for 3 s, gold plates blowing outward between the tissue
([vacuum rules](../../../world/belt/README.md#vacuum-rules): short flashes, debris thrown in rings, fire only where the hull
holds air: its breaches burn); the **act boss's screen flash** at K + 2.5 s and the **credit shower over the wreck**.
Then the lifeboat breaks out (K + 3 s) and the escape runs (≈ 14 s). The hull breaks along a **jagged fracture** (round 72
A.11): torn plating, a ragged edge, debris and venting along the break, **never a straight clean cut**. The wreck's halves
drift apart along it, burning at the breaches, and sink out of the field's sides by the end of the escape. There is no `huge` death explosion: the ship is
broken, not destroyed (Q6), and the level's last view is the empty orbit.

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
| 6 | K + 4 s (the lifeboat away) | Vorne, cold again | "A misunderstanding. They still need me; someone must open the way. The gate, then. Goodbye, Lancer." |
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

## Implementation

To be built in M10 with Level 42, once round 72 approves the design.

- [ ] Stat block and boss script in a `data.yaml` (the parts per phase, the shields, the panels and their arcs, the
      regeneration, the playbooks, the broods, the eruption, the lifeboat's path and cap, par); global difficulty
      multipliers applied
- [ ] The phase-1 panels on the shared [`reflect`](../../README.md#reflect) code (fixed mounts turning within their
      range), the generator-fed shield (240 / 120 / none)
- [ ] The phase-2 regeneration on the shared [rule](../../README.md#regeneration), the heart's membrane, the broods and
      lobs; the playbooks per phase (seeded draws, never the same twice, the broods and Darts at most every third)
- [ ] **A boss that escapes** (the gap analysis §2.10: medium): the hull's break as phase 2's end (flash, credit
      shower), the lifeboat that cannot die, its damage cap paying pro rata, the escape bar, `ESCAPED` winning the level;
      the boss checkpoint and a replay repeat it exactly
- [ ] Rook and the autopilot: the generators first, the far-side flank, the weakest sac, out of the lance and the marks
- [ ] The fight measured over five seeds (the window, the HP tuned, the par set, the cap's band), phase 1 against the
      Kraken's band, the bounty at ≈ 24 % with Level 42's scale
- [ ] Vorne's lines in this order in Level 42's radio timeline, line 5 `shout: true` (the voice check's one exception)
- [ ] Production art (the hull pieces, the parts' states, the eruption, the wreck halves, the lifeboat); readability
      checks over Level 42's backdrop
- [ ] Sounds (Freesound, CC0/CC-BY) per the brief; the music hand-off 19 → 18 and the escape sting

## Open questions

Left to the build (the document is approved as drafted, with the jagged break below):

- **The HP**: sized on the Sovereign's measured rate (15,200, this document) or the gap analysis's paper ≈ 11,500 (a
  shorter fight, ≈ 120 s); the build measures either way.
- **The lifeboat's cap**: 1,400 (reachable with focus, this document); a lower cap makes the full share common.

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
