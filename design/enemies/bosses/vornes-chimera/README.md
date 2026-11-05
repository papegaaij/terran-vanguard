---
title: Vorne's Chimera
design: approved
implementation: not-started
art: chosen
depends-on: [../ascendant, ../../air/chimera, ../../air/dart-drone, ../../air/mirror-interceptor, ../../ground/sam-nest, ../../../world/vrell-space, ../../../campaign/act-7-beyond-the-gate, ../../../player/wingmen, ../../../ui/hud, ../../../story/characters/vorne, ../../../systems/economy]
updated: 2026-11-05
---

# Vorne's Chimera

## Summary

The Act 7 mid-boss (Level 48, the Vrell-grown citadel): Vorne fused by the Choir with the
[Ascendant](../ascendant/README.md#the-lifeboat)'s lifeboat, grown into a flying chimera of teal-black tissue round the
half-swallowed gold escape capsule. Phase 1 fights like the Ascendancy (mirror batteries, a missile rack, a Dart bay,
a lance), phase 2 like the Vrell (regenerating sacs, Chimera broods and a **rear sweep** up behind the ship), phase 3
both at once with the capsule torn open. Vorne talks through the whole fight in his fused voice, shouts once at the
fusion's pain, and dies mid-sentence.

**Status: approved** (Act 7 design phase, step D-boss, 2026-11-04; the user approved it on 2026-11-05, round 84). It follows
the gap analysis's recommended answers: **Q5 a** (an air craft grown from the lifeboat in three phases; the
alternatives under *Variants b and c*) and judgement calls **J7** (the fused voice, one shout at phase 2's pain),
**J8** (track 34 for his last stand) and **J11** (Level 48's reference DPS 192). Every number is a hand-written
**draft** at medium; there is no `data.yaml` (M11 part F builds it with Level 48 once the design is approved).

## Design

### Why an air craft

The lifeboat flew away from Level 42 as an air craft, so the Chimera is the same thing grown: it flies the level's
`rear` and `spread` (the rear sweep, the panels read from the flank), it fits the mid-boss window, and it re-uses
Act 6's built parts (the Ascendant's panels, playbooks and regeneration, the Chimera broods, the Darts). The two
rejected shapes are under *Variants b and c*.

### Stat block (draft)

Numbers at **medium**; HP in damage units (a Pulse Cannon L1 shot does 2); bullet classes (`small` = 4,
`medium` = 6, `heavy` = 10, `laser` = 8) and contact damage from the [balancing basis](../../README.md#balancing-basis).
The reference DPS is the **proposed** Level 48 value of **192** (J11, the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). Borrowed attacks keep their units'
numbers (not repeated here).

| Field | Value |
|---|---|
| Faction | Hybrid (Vorne and the Vrell; a boss: never stunned, its shields emptied by the EMP Burst) |
| Layer | `air` (`space` is `air` in vacuum: all of Act 7 is `vacuum`, J12) |
| Size tier | `large` |
| Size | 200×210 px (the lifeboat's 56×80 seed-pod grown ×3.5: the tissue body round the gold capsule, two grown shoulders, a long thorned tail, A.6 b); the hull is no hit area (shots spark teal on it), the parts are. Contact `large` 20 once a second, the ship pushed 24 px out |
| Parts | phase 1: 2 **mirror batteries** (the shoulders), the **missile rack** (the spine), the **Dart bay** (the belly); phase 2: 3 **tissue sacs** (bursting out of the dead gold parts' sockets); phase 3: the **capsule** (the gold escape capsule, torn open, Vorne inside). Every part `destroyable`; the capsule is `vital` |
| Orientation | `fixed` (nose down), banking ±12° as it slides; its sprite is never rotated further |
| HP | phase 1: batteries 700 each, rack 700, bay 800 (**2,900**); phase 2: sacs 550 each (**1,650**); phase 3: the capsule 1,900 with a gold hex shield of 120 on top; total **6,450** (easy 4,838 / hard 6,773: the hard lever ×1.05, not the difficulty's ×1.3, as the Ascendant's) |
| Armour / shield | each battery behind a **mirror panel** under the [`reflect`](../../README.md#reflect) rule (arc 120°, facing 40° outward of straight down on its own side, heat 220, down 3 s); the sacs **regenerate** under the [regeneration](../../README.md#regeneration) rule (4 % a second after 1.5 s); the capsule carries a gold hex **shield** of 120 under the [enemy shield rule](../../README.md#enemy-shields) (refills after 2 s; the Tesla arc's `shield-breaker` and the EMP empty it). One defence a part |
| Speed | 90 px/s on its station; 200 px/s in the rear sweep |
| Movement | rises out of the citadel's spire well into its station (centre y 140) over 4 s, invulnerable until in place; weaves on its station across x 120–360 (a seeded path, never faster than 90 px/s, never nearer the ship than 160 px); phase 2's and phase 3's **rear sweep** (see *Phases*) |
| Attack | the three playbooks (see *Phases*) |
| Formations | solo set piece; it launches Darts (phase 1) and Chimeras (phase 2) |
| Weak points | none (the sacs' lime cores and the capsule's gold glow show where to shoot; no weak-point multiplier) |
| Effective traits | `spread`, `rear`, `shield-breaker`, `homing` |
| Credits | **470** (score 4700 × chain) in Act 1 terms at Level 48's `bounty_scale` of 0.32: ≈ **15 % of budget(48)** (see *Bounty*) |
| Death | not a plain chain: Vorne's last line, then the capsule bursts (see *Death*) |
| First level / used in | L48 only (the Level 49 boss rush has no echo of it) |
| Difficulty hooks | easy: rests ×1.3; the lance's line 1.5 s; the batteries' bursts of 2; the spiral 2 arms; regeneration 3 %; broods of 3; no rear sweep in phase 3. Hard: rests ×0.8; mirror arc 140°; bursts of 4; the spiral 3 arms on both sides; regeneration 5 %; the HP ×1.05; no boss checkpoint |

### Arena

Level 48's document places the arena and owns its timeline; the citadel's grown decks over gold wreckage are
[Vrell space](../../../world/vrell-space/README.md)'s (Level 48's sub-location and mood). The scroll **halts** over the
citadel's heart, its spire well at the top centre. Screen space, the field 480×540, y down:

| Element | Where | Notes |
|---|---|---|
| The station | centre y 140, x 120–360 | it weaves here in phases 1 and 3; the boss bar's plate stays clear above it |
| Mirror batteries | the shoulders, (±70, −10) from its centre; 44×44, hitbox 32×32 | each panel faces 40° outward from straight down on its own side: a battery is open from the **far side** of the field (the Ascendant's flank lesson) |
| Missile rack | the spine, (0, −60); 40×40, hitbox 30×30 | high on the back: hit from below past the capsule's bulk |
| Dart bay | the belly, (0, 50); 48×36, hitbox 40×28 | |
| Tissue sacs | the three dead gold sockets (the shoulders and the spine) | |
| The capsule | the centre, (0, 10); 64×80, hitbox 44×60 | sealed in tissue until phase 3 (shots spark) |
| The ship's room | the field below y 260 | the rear sweep is the only time the boss enters it, along a side |
| Backdrop | the citadel's decks (`ground`, halted); towers of grown tissue and gold wreckage (`deep`) | wreckage a step paler than the boss's gold (the mood's gold-on-gold rule) |

**Fire rules.** At most **one** told line at a time; at most **8** of its homing diamonds and **6** boss-launched
Chimeras alive (the Darts under their cap of 16); no enemy bullet spawns within 72 px of the ship (the
[bullet rules](../../README.md#bullet-readability-rules)); the launched Chimeras keep ≥ 80 px off its hull (the act's
no-overlap rule for medium-or-larger air hulls). The boss-launched Darts and Chimeras count into the level's enemies
(not its density), pay their own bounty and get no edge warning.

**Intro** (≈ 7 s): **the warning before it shows**: the mini-boss sting (track 21) as the scroll halts and the spire
well's tissue splits with a gold light under it; Vorne's first line; then the Chimera rises out of the well over 4 s,
the bar `VORNE'S CHIMERA` fills (the mid-boss's short bar, in three segments by phase: the HUD's segmented bar), invulnerable
until in place; **track 34** (his last stand, J8) from the sting's end. The boss checkpoint is taken at the sting (none
on hard); the level clock halts while it lives.

### Phases

The bar shows the Chimera's **total** HP in three segments (each phase's share); every part's kill takes its share
off it, so it falls in every phase and never jumps.

| Phase | Ends at | Behaviour |
|---|---|---|
| 1 — The gold (Ascendancy) | every phase-1 part dead (any order) | It weaves on its station; the **gold playbook** draws from the living parts (below). The natural order: the Dart bay and the rack from below, the batteries from the far side. A part's death takes its moves out of the pool; the capsule's lance and the port pair stay to the end, so the last part alive never leaves a quiet hull. |
| The fusion's pain | — (3 s, invulnerable) | The dead gold parts' sockets split and **tissue bursts out** of them, swelling into the three sacs; the body convulses (the screen shakes lightly); the Choir speaks, then **Vorne shouts** (J7, see *Vorne*). An **armour patch** drops. |
| 2 — The tissue (Vrell) | the three sacs dead | The sacs **regenerate** (a held beam or focus denies it); the **Vrell playbook** (below), with the **rear sweep**. |
| The capsule opens | — (2 s, invulnerable) | The tissue round the capsule tears back; the gold capsule's canopy cracks open and Vorne shows inside, grown into the seat (a dark silhouette, teal veins in the gold) behind the hex shield's gold shimmer. |
| 3 — Both | the capsule at 0 | The **last-stand playbook** (below) draws from both pools; the capsule regrows its shield 2 s after the last hit. |

**The playbooks** (the Ascendant's rule, round 70 and 81's lessons): after each attack and a **rest** the boss draws its
next attack with the simulation's seeded random (a replay draws the same), **never the same twice running**, each
with its own **0.8 s tell** (see *Tells*). **No phase is quiet**: from a phase's first shot to its end no stretch over
**1.0 s** passes without a bullet still closing on the ship, a homing diamond, a Dart, a Chimera or a live line, and a
ship that stands still (its front gun firing) loses armour at medium on every seed. The lever in every phase is the
**port pair**: as each move ends, and as each told line is drawn, a 2-way aimed pair over 8° (`small`, 150 px/s) from
the capsule's gun ports (phase 1), the sacs (phase 2) or both (phase 3), so a pair is always closing through the rest
and the next tell. The pair is narrow: aimed at the ship, it keeps off Rook's wing slot.

| Phase | Attack | What it does |
|---|---|---|
| 1 (rest 1.0 s; 0.7 s below half the phase's HP) | **Lance** | from the capsule's nose: a red `laser-line` aimed at the ship's spot for **1.2 s**, locked 0.2 s before it fires, then a 0.4 s beam **16 px** wide down the field, `heavy` 10 once; the port pair as the line is drawn and as the beam dies. At least every **third** draw |
| 1 | **Gold fan** | two 7-way fans over 70° (`small`, 150 px/s), 0.5 s apart |
| 1 | **Missile salvo** | 4 of the [SAM Nest](../../ground/sam-nest/README.md)'s homing diamonds from the rack (while it lives) |
| 1 | **Dart stream** | the bay launches a stream of **6** [Dart Drones](../../air/dart-drone/README.md) (while it lives); at most every **third** draw |
| 1 | **Battery burst** | the living batteries, left then right 0.4 s apart, each its told aimed `burst` of **3** (`medium` 6, 160 px/s) after its panel's 0.5 s red rim, whatever their facing |
| 2 (rest 1.0 s; 0.7 s below half) | **Chimera brood** | a sac swells and splits into a wing of **4** [Chimeras](../../air/chimera/README.md) (their own pattern and regeneration), at most 6 boss-launched alive; at most every **third** draw |
| 2 | **Spore spiral** | a 2-arm `spiral` of Vrell spores (the larger, brighter Vrell bullets; `small` 4, 120 px/s, the arms turning 60°/s, 4 bullets/s an arm) for 3 s, from the body's centre |
| 2 | **Bile lobs** | the boss [`lob`](../../README.md#boss-attacks-and-root-bursts) from the sacs: 3 impact points marked for **1.0 s** (never within 60 px of the ship's x at the mark), each bursting into a ring of 8 (`small`) |
| 2 | **Rear sweep** | (below) at least every **third** draw, never the first of the phase |
| 3 (rest 0.8 s; 0.6 s below half the capsule's HP) | **both pools** | the lance, the gold fan and a 2-diamond salvo from the capsule's own collar; the spore spiral, the bile lobs and the rear sweep; never the same twice running; a lance or the rear sweep at least every **third** draw. No broods and no Darts (the bays are dead): the fight narrows to him |

**The rear sweep** (the level's `rear`): the tissue along its far flank flares teal (0.8 s), Rook calls it, and the
Chimera **drops out of the top edge** (1.0 s out of sight; a teal glow marks the bottom-edge point it will rise from: the
boss's own tell, never the hostile edge warning). It rises from the **bottom edge on the side the ship is not on**
(≥ 160 px off the ship's x, its hull ≥ 100 px off the ship all the way), climbs that side at **200 px/s** to its
station, and from the sacs fires an aimed spore pair (`small`, 150 px/s) at the ship every **0.4 s** of the climb.
While it climbs behind and beside the ship its sacs face the rear and side mounts (the Swarm Tail's moment). It never
crosses the ship's path; its contact is the body's.

### Tells

| Attack | Tell | Medium (easy / hard) |
|---|---|---|
| The arrival | the sting (track 21), the spire well splitting with gold light | 4 s + 4 s rise |
| Lance | the capsule's nose glows gold-white, then the red line brightening | 0.8 s + 1.2 s line (easy 1.5 s / hard 1.0 s) |
| Gold fan | the gun ports round the capsule flash gold | 0.8 s |
| Missile salvo | the rack's hatch opens with a red lock light (the SAM Nest's) | 0.8 s |
| Dart stream | the bay's doors open with a gold light | 0.8 s |
| Battery burst | both panels' rims glow red, then each its own as it fires | 0.8 s, then 0.5 s each |
| Mirror back up | the silver shimmer across the panel (the reflect rule) | 0.4 s |
| The fusion's pain | the gold plates buckle, teal light leaks from every seam | 1.0 s, then 2 s bursting (invulnerable) |
| Chimera brood | a sac swells, its seams flaring teal-white | 0.8 s |
| Spore spiral | the body's centre pulses lime and teal | 0.8 s |
| Bile lobs | the sacs contract; the impact markers | 0.8 s + 1.0 s marks |
| Rear sweep | the far flank's tissue flares teal; Rook's call; the bottom-edge glow during the 1.0 s out of sight | 0.8 s + 1.0 s |
| The capsule opens | the tissue tearing back, the canopy cracking | 2 s (invulnerable) |

### The escort (Rook)

Rook flies the level in the escort slot ([Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft)). His
boss rules here: in phase 1 the **Dart bay**, then the rack, then a battery from its open side if he is there; in
phase 2 the **sac with the least HP** and Chimeras within his reach, and he flies **Trail** while the rear sweep runs,
as for a `rear` wave; in phase 3 the capsule. He steps out of a told line and a lob's marks like the ship. The test
autopilot does the same, crossing the field to open a battery.

### Duration and HP

Target: the mid-boss window **45–75 s** at medium, Q5 a's **60–75 s**; the **par** is the measured mean, paper
**≈ 68 s** (from the bar to the capsule's death; the death's line and burst are the same on every run and stay
outside it).

**The rate.** The Ascendant (bow-on, still) measured ≈ 0.87 × reference; the Honour Guard (three dodging aces)
≈ 0.5 ×. The Chimera moves on a station and sweeps, so the paper rate is **0.65 × 192 ≈ 125**: phase 1 at ×0.9 (beam
time lost on the mirrors), phase 2 at ×0.8 (the regeneration, fire spent on Chimeras), phase 3 at ×1.0.

| Stretch | HP | Time (paper) |
|---|---|---|
| The sting, Vorne's first line, the rise | — | ≈ 7 s (≈ 4 s from the bar) |
| 1 — The gold | 2,900 | ≈ 26 s |
| The fusion's pain | — | 3 s |
| 2 — The tissue | 1,650 | ≈ 16 s |
| The capsule opens | — | 2 s |
| 3 — Both | 1,900 + the shield | ≈ 16 s |
| Total (the bar to the kill) | 6,450 | **≈ 67 s** |

Phase 3 is the shortest (the Sovereign's round 70 lesson: a long last part bores). Lessons the build must apply
(M11 part F):
- **Measure, then tune the HP, not the attacks**: the autopilot with the hand plan's Level 48 fit and Rook over five
  seeds, every seed inside 45–75 s, the par at the measured mean; tune phase 1's batteries and phase 3's capsule first.
- **Never quiet**: the 1.0 s rule and the standing-still check measured as the Ascendant's (`AscendantFightTest`'s
  form), five seeds at medium and hard.
- **The regeneration must not stall**: on every seed phase 2 ends; a sac healed back to full more than twice by the
  autopilot means the rate comes down, not the HP up.
- **The rear sweep reads**: on every seed the ship is never touched by the climbing hull at medium with the
  autopilot, and the sweep comes at least twice in phase 2.

**Specials on the Chimera** (the specials' boss rules): the plan's [Swarm Call](../../../player/specials/README.md)
drones hunt its open parts (at most 400 from a call); the EMP (if fitted) empties the capsule's shield and shatters
both mirrors (3 s), stuns the launched Darts and Chimeras, never stops the regeneration. The Airstrike and the Orbital
Lance are not available beyond the gate (J12).

### Bounty

A mid-boss pays ≈ **15 %** of its level's budget (the band 10–20 %, `BalanceTest`; the Honour Guard's 15 %).
budget(48) = 700 × 1.07⁴⁷ ≈ **16,832**; 15 % ≈ **2,525 paid**. Data is in Act 1 terms: **bounty = 2,525 ÷ (16.777 ×
`bounty_scale`) ≈ 150.5 ÷ `bounty_scale`** (16.777 = 1.6⁶, Act 7's factor). Level 48's scale is its document's,
**0.32**, so **470**, split 40 / 25 / 35:

| Share | Parts | Act 1 terms | Paid (at 0.32) |
|---|---|---|---|
| 40 % | phase 1: each battery 44, the rack 44, the bay 56 | 188 | ≈ 1,009 |
| 25 % | phase 2: each sac 39 | 117 | ≈ 628 |
| 35 % | phase 3: the capsule | 165 | ≈ 886 |
| | | **470** | **≈ 2,523 = 15.0 %** |

When the level's scale is set the bounty is recomputed with the same formula; the shares stay. The launched Darts
and Chimeras pay their own bounty, outside the share.

### Death

The capsule at 0 (**K**): the Chimera stops firing and its bullets on the screen fade out over 0.5 s (a scene, not a
trap); the bar goes. It hangs where it is, convulsing, and the tissue **closes over the open capsule** (the Choir
taking him back) while Vorne speaks his last line (≈ 4.5 s). At "*trying to—*" the line **breaks off**: the capsule
bursts in a gold-white flash, a 2 s `large` vacuum chain runs through the tissue
([vacuum rules](../../../world/belt/README.md#vacuum-rules): short flashes, debris in rings, fire only inside the
capsule), the credit shower where it died; no screen flash (a mid-boss). The level goes on after ≥ 4 s of calm (Level
48's document: the waves after him, its end).

### Vorne

[Vorne](../../../story/characters/vorne/README.md) is on the enemy channel for the fight (the HUD's amber plate) in his
**fused voice** (J7: his own voice with the Choir's chorus a breath behind it, the audio step's filter); his portrait
is the fused one (his portrait overgrown at one edge with the Choir's teal pattern; an art brief for the round). **This
document owns the order and the wording of his Level 48 lines**; Level 48 places and times them and adds the friendly
lines between them. Drafts, decided 2026-11-05, round 84:

| # | When | Speaker | Line (draft) |
|---|---|---|---|
| 1 | the sting (before it shows) | Vorne | "Lancer. You followed me all the way in. How loyal. How very Coalition." |
| 2 | `boss-arrival` (in place) | Vorne | "Look what they grew for me. I asked for a ship. They gave me a body." |
| 3 | the first phase-1 part destroyed (or 15 s into phase 1) | Vorne | "I kept my side of every bargain I ever made. They kept theirs. Forever, they said." |
| 4 | the fusion's pain (its first second) | the Choir | "…he is ours… he sings with us… he is afraid with us…" (its channel's dress, [the Choir](../../../story/characters/the-choir/README.md)) |
| 5 | right after the Choir | Vorne, **shouted** | "Get out of my head!" (`shout: true`; the portrait's rage) |
| 6 | the first sac dead (or 10 s into phase 2) | Vorne, strained | "You think this war was about Earth? Earth was the price, Lancer. I was buying time." |
| 7 | the capsule opens | Vorne, cold again | "One last deal, Lancer. Walk away, and I'll tell you what's out there." |
| 8 | the capsule below half its HP | Vorne | "I built an empire out of three moons and a grudge. I will not end as a song." |
| 9 | K (the death) | Vorne | "*Do you know what's coming after them, pilot? Do you have any idea what I was trying to—*" (fixed, the story's; breaks off at the burst) |

**The one shout** (J7): line 5 is Vorne's only shouted line in Act 7 (his Act 6 one was Level 42's eruption); the
voice README's check takes these two cues as its only exceptions (the audio step writes it). Lines 6–8 hint at what he
knows without naming it: the Silence is never named by him (the story's rule; Level 41's files named it once).
The last line is cut by the burst, not faded: the subtitle ends on the dash.

### Variants b and c (for the round's a/b/c)

- **b — Rooted in the citadel**: a ground boss, the citadel's grown turrets as his parts. Medium cost, but no rear play
  (the level's trait) and nothing of the lifeboat's flight.
- **c — Two stages**: the citadel first, then he tears loose and flies. Large, and over the mid-boss length.

### Presentation

- **Look brief**: the lifeboat's seed-pod (the [Ascendant](../ascendant/README.md#presentation)'s brief: teal-black
  tissue, the half-swallowed gold capsule, its canopy the one warm spot) grown into a craft three times its size.
  **A.6 b, "thorned graft"** (chosen 2026-11-05, round 84): a narrower, spikier body, the gold guns (the Ascendancy
  batteries and their silver-gold panels, the rack on the spine) held out on **bone-white thorned arms**, gold hull
  plates embedded in the tissue like armour, the belly bay's gold doors, a **long thorned tail**; the production
  target stays **35 % gold** (b measured 30 %). **Sickly gold and teal** (the Level 48
  mood): the gold dulled and veined with teal, the tissue's teal `00FF9A` seams; the sacs' and the capsule's cores
  lime. Strong top-left rim light so the teal-black never sits dark on the dark citadel.
- **Phases**: phase 1 gold-heavy (≈ 35 % of the silhouette gold); phase 2 the gold parts burst into tissue sacs (the
  sockets' torn gold rims kept); phase 3 the capsule open, Vorne's silhouette in the seat behind the gold shimmer.
- **Drawing**: fixed orientation with banking frames (±12°, 5 steps), the parts in their states (whole, damaged,
  dead; mirror whole, heating, cracked, down), the fusion's burst and the capsule's opening as frame sequences, the
  tissue closing over the capsule; every glow and haze smooth and fading to 0 inside its canvas (≥ 32 steps, no
  ordered dither, drawn once).
- **Readability**: the gold, then the teal, over Level 48's citadel (gold-on-gold: the wreckage paler); the magenta
  bounced bullets distinct from the Vrell spores.
- **Music**: the mini-boss sting (track 21), then **track 34** (Vorne's last stand, J8, the audio step's) from the
  sting's end to the death; at K the track cuts with his line (no sting over it); the level's track resumes after the
  calm ([music](../../../audio/music/README.md)).

### Sounds brief

Real recordings from Freesound (CC0 / CC-BY); mechanical, then organic, never wet, bubbly or popping. The launched
units and the borrowed attacks keep their own sounds.

| Sound | Where / when | Brief |
|---|---|---|
| The rise | the spire well splitting, the Chimera rising | a deep tearing groan of thick hide and a strained engine whine under it |
| Lance | its charge and shot | the Ascendant's lance, higher and thinner: a rising capacitor whine, a sharp electric crack |
| The fusion's pain | phase 2's start | metal plates buckling and tearing, a long organic creak (stretched leather, slowed) under it |
| Rear sweep | its drop and climb | a rushing engine pass fading out above, then rising from below, a dopplered whine |
| The capsule opens | phase 3's start | a canopy's seal cracking, a hiss of escaping air, metal hinges straining |
| The burst | the death | a sharp metallic bang cut short, debris rattling, then a low settling rumble |

## Concept art

Concept round 84 (Act 7 design phase, step ART-R-B, 2026-11-05; decided 2026-11-05, round 84): the Chimera grown from
round 72's chosen lifeboat (the round seed-pod, the gold capsule half swallowed) at three times its size, in Level 48's
sickly gold and teal, with the Ascendant's production parts as grafts. Generator `tools/concept/act7_r84.py chimera`; prompts:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/vornes-chimera-r84-b.png](concept/vornes-chimera-r84-b.png) | B "thorned graft": the gold parts held out on bone-thorn arms, plates of the Ascendant's gold hull embedded in the tissue, a long thorned tail (spikier, wider, more gold); as A (sheet) | chosen (round 84 A.6 b) |
| [concept/vornes-chimera-r84-b.gif](concept/vornes-chimera-r84-b.gif) | B in the field at 1×, as A | chosen (round 84 A.6 b) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

To be built in M11 part F with Level 48, once round 84 approves the design.

- [ ] Stat block and boss script in a `data.yaml` (the parts, the three playbooks, the rear sweep, the tells, the
      phases and their transitions, the par); the global difficulty multipliers with the ×1.05 hard lever
- [ ] The rear sweep: out of the top edge, the bottom-edge glow, the climb up the far side with its distance rules
- [ ] The never-quiet rule and the standing-still check measured (five seeds, medium and hard), the fight inside
      45–75 s on every seed, the HP tuned, the par set; the bounty at ≈ 15 % with Level 48's scale
- [ ] The segmented mid-boss bar `VORNE'S CHIMERA`; the lines in Level 48's radio timeline with the fused voice and the shout's
      exception; the line cut by the burst
- [ ] Rook's and the autopilot's rules (Trail in the rear sweep)
- [ ] Production art (grown from the chosen lifeboat; the phases, the fusion, the opening, the death) and the fused
      portrait; readability over Level 48's citadel
- [ ] Sounds (Freesound, CC0/CC-BY) per the brief; track 34

## Open questions

- **The bullets at the death**: faded at K so his line is heard in a calm field (this draft), or left to fly out.

## Decisions

- 2026-11-04 (Act 7 design phase, step m11-d-boss; decided 2026-11-05, round 84): promoted from the bosses
  roster to this draft spec (Q5 a, J7, J8, J11): 6,450 HP over three phases (paper 67 s), the rear sweep, the
  never-quiet port pair, 376 credits at a hand scale 0.40 (15 % of budget(48)), Vorne's nine lines with one shout and
  the fixed last line broken off by the burst. Decided here: phase 3 has no broods or Darts (the fight narrows to
  him); the bullets fade at the death; no echo of it at Level 49.
- 2026-11-04 (step m11-d-act, decided 2026-11-05, round 84): the bounty recomputed at Level 48's scale 0.32: **470** (the
  batteries, the rack and the bay 44/44/44/56, the sacs 39 each, the capsule 165; 15 % of budget(48)); line 6 reworded
  by the story step ("*Earth was the price, Lancer. I was buying time.*"), lines 6–8 kept as hints only (the open
  question closed); the fused portrait added to ART-R-B's subjects.
- 2026-11-05 (step m11-art-r-b, decided 2026-11-05, round 84): concept art proposed: a "swollen pod" and b "thorned graft", both grown from the chosen lifeboat; measured gold share in phase 1 24 % (a) and 30 % (b) against the brief's 35 % (decided 2026-11-05, round 84: b, the 35 % share stays the production target).
- 2026-11-05, round 84 (user, page answers): **Q5 a** as drafted (b and c rejected); **A.6 b** (not the recommendation):
  the **"thorned graft"** look (a narrower, spikier body, the gold guns held out on bone-white thorned arms, gold hull
  plates embedded like armour, a long thorned tail), the 35 % gold share kept as the production target; a "swollen
  pod" rejected; **A.12 a** (the fused portrait's "edge") and **V.2 b** (the stronger fusion, see the voice README) for
  his fused look and voice; **D.12 approved**. The open items were:
  - **Q5** (round 84): **a** the air craft in three phases (this draft), **b** rooted in the citadel, **c** two stages.
  - **The fused portrait**: proposed in round 84 ([characters](../../../story/characters/README.md#concept-art), `vorne-fused-r84-a/b`).
