---
title: Choir Heart
design: approved
implementation: done
art: chosen
depends-on: [../../air/choir-herald, ../../air/needler, ../../air/spore-bomber, ../../air/whirl-seed, ../../air/mote-swarm, ../../air/skitter, ../../space/gate-warden, ../../../world/vrell-space, ../../../world/europa, ../../../campaign/act-7-beyond-the-gate, ../../../player/wingmen, ../../../ui/hud, ../../../story/characters/the-choir, ../../../systems/economy]
updated: 2026-11-08
---

# Choir Heart

## Summary

The final boss (Level 50, the hive core): the Vrell hive mind's core, a vast luminous organ filling the top of the
field. Phase 1, **the membranes**: six singing organs round the sealed Heart, each open only while it sings one of
the Vrell's attack styles. Phase 2, **the arena**: tissue walls close in from the sides, the light goes out but for
the Heart's glow and the scroll drifts, until the four nodes that hold the walls are cut. Phase 3, **the core**: the
Heart's dome opens along its spokes and a single weak point fights on in a storm of bullets while the Choir's fearful last lines play.
Its death hands over to the closing gate.

**Status: approved** (Act 7 design phase, step D-boss, 2026-11-04; the user approved it on 2026-11-05, round 84). It follows
the gap analysis's recommended answers: **Q7 a** (three phases, 150–180 s; the alternatives under *Variants b and c*),
**Q8 a** for what follows the kill (the escape through the closing gate is a scene after it, not a phase: Level 50's
and the story's documents), and judgement calls **J8** (track 20 in full) and **J11** (Level 50's reference DPS 200).
**Built** (M11 part H, step H2): the stat block and the fight's script are in [data.yaml](data.yaml), rendered into the
*Stat block* below; the fight follows the user's boss recipe (round 93 L.3: every phase fires from its parts, each its
own gun, silent at its death; it enters firing) and its HP was **measured, then tuned** (*Duration and HP*).

## Design

### Why three phases

The roster asks for a boss that "switches between the attack styles of every Vrell enemy, reshapes the arena (walls
of tissue, darkness, distorted scroll), final phase a single weak point in a storm of bullets". Each phase takes one
of those and makes it the objective: the membranes sing the Vrell's styles (and teach the act's window shield again),
the arena phase turns the field itself against the player with tools that all exist or come with Level 44, and the
core is the storm. Every phase has one thing to shoot that glows and one rule to read.

### Stat block

Numbers at **medium**; HP in damage units (a Pulse Cannon L1 shot does 2); bullet classes (`small` = 4,
`medium` = 6, `heavy` = 10, `laser` = 8) and contact damage from the [balancing basis](../../README.md#balancing-basis).
The reference DPS is the **proposed** Level 50 value of **200** (J11, the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). Borrowed attacks keep their units'
numbers unless the table says otherwise.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (`space` is `air` in vacuum, J12); the walls of phase 2 are **terrain** (Act 3's canyon walls) |
| Size tier | `huge` |
| Size | 440×300 px, hitbox 400×260 (the organ, its upper third beyond the top edge: the visible Heart ≈ 440 px wide, its lowest point at y 200); the body is no hit area (shots spark violet on it), the parts are. Contact `huge` 25 once a second |
| Parts | phase 1: 6 **membranes** (the singing organs on its rim); phase 2: 4 **wall nodes** (two in each wall); phase 3: the **core** (`vital`). Every part `destroyable` |
| Orientation | `fixed` |
| HP | phase 1: membranes 1900 each (**11,400**); phase 2: nodes 300 each (**1,200**); phase 3: the core **1800**; total 14400 (easy 10800 / hard 18720, from the global multipliers) |
| Armour / shield | the membranes are **window shields** (the [window shield](../../README.md#window-shields) rule, the [Gate Warden](../../space/gate-warden/README.md)'s): closed and invulnerable except from the first frame of their song's tell to its last bullet; the Tesla arc's `shield-breaker` opens one for 3 s, the EMP all for 3 s; the nodes and the core have no defence (the walls and the dark are phase 2's defence, the storm phase 3's) |
| Speed | 40 px/s into view; it holds its place (the walls move, see *Arena*) |
| Movement | comes into view from above the top edge with the last of the scroll over 6 s (invulnerable until in place), its centre at (240, 70); fixed there |
| Attack | every phase fires from its parts, each its own gun, silent at its death, and its playbook's told moves and the heartbeat on top (see *Phases*); as it comes into view, a **descent volley** from the membranes in turn every 0.35 s (a 3-way aimed fan over 20°, `medium` 6, 170 px/s; never at a ship within 110 px); the **heartbeat** (a `ring` of 12, `small` 4, 100 px/s, from the Heart's centre every 3 s in phases 1 and 2, every 2.0 s in phase 3, each on the pulse's peak: the never-quiet floor); phase 1, its parts' own guns: each membrane's **mouth pair** (an aimed burst of 2, `medium`, 160 px/s, every 2.2 s each, the six in turn, closed or open); phase 1's playbook (each membrane its song, two at once from the phase's half): the **Herald's song** ([Choir Herald](../../air/choir-herald/README.md)'s ring, larger: two rings of 16, `small`, 100 px/s, 0.6 s apart); the **Needler thorns** ([Needler](../../air/needler/README.md)'s thorn: three aimed 3-way bursts over 10°, `small`, 160 px/s, 0.4 s apart); the **spore drop** (5 of the [Spore Bomber](../../air/spore-bomber/README.md)'s spores thrown to seeded points ≥ 80 px from the ship, each bursting on contact, `medium` 6, or after 4 s into its ring of 6); the **seed scatter** (6 [Whirl Seeds](../../air/whirl-seed/README.md) flung in a fan toward the ship's half); the **mote cloud** (a [Mote Swarm](../../air/mote-swarm/README.md) of 8 released toward the ship, at most every third draw); the **Choir spiral** (3 arms, `small`, 120 px/s, the arms turning 60°/s, 4 bullets/s an arm, for 3 s; phases 2 and 3: from the Heart's centre); phase 2, its parts' own guns: each wall node's **node burst** (an aimed burst of 2, `medium` 6, 170 px/s, every 2.4 s each, the four in turn); phase 2's playbook: the **Heart's line** (the centre whitens 0.8 s, then a violet-white line aimed at the ship's spot for 1.2 s, locked 0.2 s before it fires, a 0.5 s beam 20 px wide, `heavy` 10 once; at least every third draw), the **line sweep** (the beam turning 40° away from the ship's side), the **wall spit** (the veins' aimed pairs from both walls four times 0.4 s apart, from points ≥ 120 px from the ship's height), the **pit brood** (two wall pits × 3 [Skitters](../../air/skitter/README.md), at most every third draw) and the Choir spiral; phase 3, its part's own gun: the core's **core burst** (an aimed burst of 3, `medium` 6, 190 px/s, every 0.7 s); the storm's playbook, two attacks at once from the Herald's song, the Needler thorns, the Choir spiral, the Heart's line, the line sweep and the spore drop (never two lines together, a line at least every third draw, no releases); every song, wall spit and core attack ends with the **aimed pair** (a 2-way pair over 8°, `small`, 150 px/s, from the part that fired) |
| Formations | solo set piece |
| Weak points | none (the membranes' singers, the nodes and the core glow lime; no weak-point multiplier) |
| Effective traits | `beam`, `homing`, `area`, `shield-breaker` |
| Credits | 1377 (score 13770 × chain) in Act 1 terms at Level 50's `bounty_scale`: ≈ **24 % of budget(50)** (see *Bounty*), split 30 / 30 / 40: each membrane 69, each node 103, the core 551 |
| Death | the act boss's chain and flash: at K the heartbeat stops and its bullets fade over 0.5 s, the chain runs 4 s, the flash at K + 3.5 s, the Choir's last line, then the closing gate (see *Death*) |
| First level / used in | L50; only |
| Difficulty hooks | easy: rests ×1.3; one membrane sings at a time all phase 1; the walls close to 360 px (contact 6), no reversal; the darkness's ambient 20 %; phase 3 draws one attack at a time; the line 1.5 s. Hard: rests ×0.8; the heartbeat ring of 16; the walls close to 300 px; the line 1.0 s; the HP ×1.05; no boss checkpoint |
<!-- /data -->

### Arena

Level 50's document places the arena and owns its timeline; the hive core's chambers and the Heart's pulse are
[Vrell space](../../../world/vrell-space/README.md)'s (Level 50's sub-location and mood: near-black violet tissue lit
by the Heart's slow red-violet pulse, ≤ 1 Hz, smooth, never a flash). The scroll **halts** with the Heart at the top
of the field. Screen space, the field 480×540, y down:

| Element | Where | Notes |
|---|---|---|
| The Heart | centre (240, 70); visible to y 200 | the boss bar's plate stays clear over it |
| Membranes | on the Heart's rim at (90, 150), (150, 185), (210, 200), (270, 200), (330, 185), (390, 150); 56×56, hitbox 40×40 | translucent tissue mouths on the dome's rim (A.7 b) that iris open while their singer sings |
| The walls (phase 2) | the field's sides, closing from x 0 and x 480 to leave a **corridor of 320 px** (x 80–400) | terrain under [Act 3's canyon-wall rule](../../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md) (contact 10, easy 6, the push-back and 0.5 s cooldown; walls stop shots, beams and missiles); they **breathe** ±12 px at 0.5 Hz and never move faster than 20 px/s; every move told by the veins' 1.0 s glow; a lit edge so the field's shape reads in the dark |
| Wall nodes | two in each wall's inner face, at y 220 and 400; 48×48, hitbox 36×36 | lime, pulsing; each one cut pushes its wall's half back to the edge |
| The core (phase 3) | the Heart's centre, (240, 150); 72×72, hitbox 56×56 | lime-white under the opened dome |
| The ship's room | below y 260 | the walls' corridor in phase 2 |

**Fire rules.** At most **one** told line at a time; at most **two** patterns firing at once (the heartbeat aside); at
most 8 Motes, 8 Whirl Seeds and 6 Skitters boss-released alive; no enemy bullet spawns within 72 px of the ship (the
[bullet rules](../../README.md#bullet-readability-rules)); **bullets are never hidden** (the dark, the walls and the
drift change what the player sees and where things go, never a bullet's brightness). The boss-released units count
into the level's enemies (not its density), pay their own bounty and get no edge warning.

**Intro** (≈ 11 s): **the boss warning before the Heart shows** (track 22) as the scroll slows through the last
chamber; the Choir's first line; then the Heart comes into view with the last of the scroll over 6 s, its pulse
lighting the chamber from the bottom up, the bar `CHOIR HEART` fills (the HUD's three-segment bar,
[Act 7 additions](../../../ui/hud/README.md#act-7-additions-draft)); invulnerable until in place. **Track 20** (the
final boss theme, J8) from the warning's end. The boss checkpoint is taken at the warning (none on hard), and again at
each phase's start (the final fight is long; Level 50's document confirms); the level clock halts while it lives.

### Phases

The bar shows the Heart's **total** HP in three segments; every part's kill takes its share off it.

| Phase | Ends at | Behaviour |
|---|---|---|
| 1 — The membranes | all six membranes dead | The Heart is sealed; its rim's six membranes **sing** in turn from the **membranes' playbook** (below). A membrane is closed (invulnerable) except while it sings: its mouth irises open on the first frame of its tell and close on its song's last bullet. The heartbeat every 3.0 s. From the phase's half (three membranes dead) **two** sing at a time, 0.4 s apart, at most one of them releasing units. |
| The closing | — (4 s, invulnerable) | The Heart contracts; the **walls** grow out of the chamber's sides to the 320 px corridor (told by their veins), the **light goes out** but for the Heart's glow and the nodes', and the **drift** begins (Rook calls it). The Choir's second line. An **armour patch** drops. |
| 2 — The arena | all four nodes dead | The walls breathe; the field is **dark** ([Act 4's darkness](../../../world/europa/README.md#darkness-level-26), a look only: outside the ship's headlight cone and the light pools everything shows only its glow, ambient ≈ 8 %; bullets always bright; Rook's lamp); the scroll **drifts** under the [distorted-space](../../README.md#distorted-space) rules on a gentler profile (\|θ\| ≤ 15°) with **one reversal** (3 s), told 2 s ahead, at the second node's death or 25 s into the phase. Each node cut pushes its half-wall back to the edge and brings back a quarter of the light: the objective reads on the screen. The **arena's playbook** (below); the heartbeat every 3.0 s. |
| The opening | — (4 s, invulnerable) | The walls fall back, the light returns, the drift eases out (1 s). The Heart's sealed dome **opens along its spokes** (A.7 b) and peels back from the **core**. The Choir's third line. An **armour patch** drops. |
| 3 — The core | the core at 0 | The **storm's playbook** (below): two attacks at a time from every phase's pool; the heartbeat every 2.0 s. The Choir's fourth line at the core's half. |

**The playbooks** (the Ascendant's rule, rounds 70 and 81): after each attack and a **rest** the Heart draws its next
attack with the simulation's seeded random (a replay draws the same), **never the same twice running**, each with its
own **0.8 s tell** (see *Tells*). **No phase is quiet**: from a phase's first shot to its end no stretch over **1.0 s**
passes without a bullet still closing on the ship, a unit it released or a live line; the heartbeat ring is the floor
and every song, wall spit and core attack ends with an **aimed pair** (a 2-way pair over 8°, `small`, 150 px/s, from the
part that fired), so a ship that stands still (its front gun firing) loses armour at medium on every seed.

**Phase 1, the membranes' songs** (rest **0.9 s**; 0.6 s below the phase's half). Each membrane sings one Vrell style
(its singer's look and tell borrowed from that unit), never the same membrane twice running, each living membrane at
least once in any eight draws (no membrane left closed for long):

| Membrane | Song | What it does |
|---|---|---|
| 1 | **Herald's song** | the [Choir Herald](../../air/choir-herald/README.md)'s `ring`, larger: two rings of 16 (`small`, 100 px/s) 0.6 s apart |
| 2 | **Needler thorns** | the [Needler](../../air/needler/README.md)'s aimed thorn as a `burst`: three bursts of 3 (0.15 s apart, `small`, 160 px/s), 0.4 s apart |
| 3 | **Spore drop** | 5 of the [Spore Bomber](../../air/spore-bomber/README.md)'s spores thrown to seeded points ≥ 80 px from the ship, each bursting on contact (`medium` 6) or after **4 s** into its ring of 6 |
| 4 | **Seed scatter** | 6 [Whirl Seeds](../../air/whirl-seed/README.md) flung out in a fan toward the ship's half of the field (contact only, their own rules) |
| 5 | **Choir spiral** | a 3-arm `spiral` (`small`, 120 px/s, the arms turning 60°/s, 4 bullets/s an arm) for 3 s |
| 6 | **Mote cloud** | a [Mote Swarm](../../air/mote-swarm/README.md) of 8 motes released toward the ship (contact only); at most every **third** draw |

A dead membrane's song leaves the pool; the last membrane alive sings its own song and the Herald's ring by turns
(the pool never empties).

**Phase 2, the arena** (rest **0.9 s**; 0.6 s below the phase's half):

| Attack | What it does |
|---|---|
| **The Heart's line** | a violet-white `laser-line` from the Heart's centre aimed at the ship's spot, its line drawn **1.2 s**, locked 0.2 s before it fires, a 0.5 s beam 20 px wide, `heavy` 10 once; at least every **third** draw (the level's `beam`: the Heart answers in kind) |
| **Line sweep** | the line and lock, then a 1.2 s beam sweeping 40° toward the side the ship was not on (its tell shows the way) |
| **Wall spit** | the walls' veins fire aimed pairs (`small`, 150 px/s) inward from both sides, four times 0.4 s apart, from points ≥ 120 px from the ship's y |
| **Pit brood** | two wall pits each release 3 [Skitters](../../air/skitter/README.md) into the corridor; at most every **third** draw |
| **Choir spiral** | the phase-1 spiral from the Heart's centre for 3 s (it slides with the drift as a whole: the distorted-space rule) |

**Phase 3, the storm** (rest **0.6 s**; 0.5 s below the core's half): the core draws **two** attacks at once (0.4 s
apart) from the Herald's song, the Needler thorns, the Choir spiral, the Heart's line, the line sweep and the spore
drop, never the same pair twice running, never two told lines together, a line at least every **third** draw. No
releases (Motes, Seeds, Skitters): the storm is bullets round one target.

### Tells

| Attack | Tell | Medium (easy / hard) |
|---|---|---|
| The arrival | the boss warning (track 22), the pulse lighting the chamber | 5 s + 6 s |
| A membrane's song | its mouth irises open and its singer glows in the style of its unit (the Herald's ring light, the Needler's thorn glint, the Bomber's sac swelling, the seeds' rattle, the spiral's lime pulse, the motes' shimmer); open from this first frame (the window rule) | 0.8 s |
| Heartbeat | the pulse's slow swell to its peak (≤ 1 Hz, smooth) | the pulse itself |
| The closing | the veins along the chamber's sides glowing, the light dimming | 1.0 s, then 3 s closing (invulnerable) |
| A wall's move | its veins glow | 1.0 s |
| Heart's line, line sweep | the Heart's centre whitens, then the violet-white line (the sweep: a fading fan over its arc) | 0.8 s + 1.2 s line (easy 1.5 s / hard 1.0 s) |
| Wall spit | the veins at the firing points flash lime | 0.8 s |
| Pit brood | the pits' lips swell and part | 0.8 s |
| A reversal | the HUD's double chevron and Rook's call | 2 s |
| The opening | the dome's spokes opening with light | 1.0 s, then 3 s (invulnerable) |

### The escort (Rook)

Rook flies the level in the escort slot ([Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft)). His
boss rules here: in phase 1 the **open** membrane (a closed one only when none is open: the window rule's targeting),
the released units within his reach; in phase 2 the **nearest node** on his side of the corridor (his weave keeps
inside the walls; the walls' contact hurts him as the ship); in phase 3 the core. He steps out of a told line and a
spore's spot like the ship, and calls the reversal. The test autopilot does the same.

### Duration and HP

Target: the act-boss window **90–180 s** at medium, Q7 a's **150–180 s** for the last fight; the **par** is the
measured mean, paper **≈ 165 s** (from the bar to the kill; the death and the scene after it stay outside).

**The rate.** The Ascendant measured ≈ 0.87 × reference on a still boss; the Heart is still too: **0.87 × 200 ≈ 174**.
Phase 1 at ×0.6 (a membrane open only while it sings, ≈ 70 % of the time one is open, and the beam's travel between
them; the Tesla arc buys more), phase 2 at ×0.8 (nodes on the walls' faces, shots lost to the walls, the drift),
phase 3 at ×1.0.

| Stretch | HP | Time (paper) |
|---|---|---|
| The warning, the arrival | — | ≈ 11 s (≈ 6 s from the bar) |
| 1 — The membranes | 6,300 | ≈ 60 s |
| The closing | — | 4 s |
| 2 — The arena | 7,000 | ≈ 50 s |
| The opening | — | 4 s |
| 3 — The core | 6,600 | ≈ 38 s |
| Total (the bar to the kill) | 19,900 | **≈ 162 s** |

**Measured** (step H2, `HeartFightTest`: the autopilot with the plan's Level 50 fit and Rook, five seeds; pending the
user's review in round 95). At the spec's 19,900 HP the fight ran ≈ 310 s at medium: the membranes' windows held phase 1
near its paper 60 s, but the arena's nodes (on the walls' faces, under their own fire) and the core in the storm took
2–3 times their paper. The HP is tuned to **14,400** (membranes 1,900 each, nodes 300 each, the core 1,800: phase 1
carries the length, where a moving ship loses least) so that every seed lands inside 150–180 s:

| Difficulty | The bar to K (five seeds) | Phases 1 / 2 / 3 | Shots a minute (phases) | Lost a second (autopilot) |
|---|---|---|---|---|
| medium | 155.7–173.4 s, mean 164.2 (**par 165**) | 86–94 / 27–33 / 29–42 s | 254–289 (≈ 290 / 180 / 335) | 0.4–0.7 / 2–4.7 / 1.0–1.9 |
| easy | 115.9–130.9 s | 62–72 / 22–29 / 19–26 s | 169–191 | under 3.1 in every phase |
| hard (×1.05, no checkpoint) | 177.8–216.3 s, won on every seed | 90–132 / 31–39 / 35–53 s | 340–366 | 0.9–1.5 / 3.8–5.3 / 2.1–2.4 |

A ship standing still under it (its front gun firing) loses its armour in ≈ 16 s in phase 1, in 22–30 s in phase 2 and
loses ≈ 4.5 a second in phase 3 (the autopilot dodging: a tenth to a third of that). Never quiet over 1.0 s in any
phase; the autopilot cuts all four nodes and the reversal comes once on every seed; no shot comes from a dead part;
`Level50Test` wins every difficulty with Rook and without (hard: no checkpoint, the autopilot only dodging below 45 %
of its armour, as a careful player).

Phase 3 is the shortest (the Sovereign's round 70 lesson). Lessons the build must apply (M11 part H):
- **Measure, then tune the HP, not the attacks**: the autopilot with the hand plan's Level 50 fit and Rook over five
  seeds, every seed inside 150–180 s, the par at the measured mean; phase 1's membranes first (their windows decide
  its length).
- **Never quiet**: the 1.0 s rule and the standing-still check, five seeds at medium and hard, in every phase.
- **The arena reads**: on every seed the autopilot cuts all four nodes; no wall ever pins the ship against a bullet (a
  wall never moves toward a ship within 40 px of it); the reversal comes once.
- **The storm stays readable**: two patterns at most, one line at most, the bullets bright in every phase.

**Specials on the Heart** (the specials' boss rules): the plan's [Swarm Call](../../../player/specials/README.md)
drones hunt an open membrane, a node or the core (at most 400 from a call; a closed membrane is no target); the EMP
(if fitted) opens every membrane for 3 s (the window rule) and stuns the released units. The Airstrike and the Orbital
Lance are not available beyond the gate (J12).

### Bounty

An act boss aims at ≈ **24 %** of its level's budget (Levels 21, 28, 35 and 42). budget(50) = 700 × 1.07⁴⁹ ≈
**19,271**; 24 % ≈ **4,625 paid**. Data is in Act 1 terms: **bounty = 4,625 ÷ (16.777 × `bounty_scale`) ≈ 275.7 ÷
`bounty_scale`** (16.777 = 1.6⁶, Act 7's factor). Level 50's scale is its measured **0.20** (step H2's density waves;
the hand 0.34 gave 811), so **1,377**, split 30 / 30 / 40:

| Share | Parts | Act 1 terms | Paid (at 0.20) |
|---|---|---|---|
| 30 % | phase 1: each membrane 69 | 414 | ≈ 1,389 |
| 30 % | phase 2: each node 103 | 412 | ≈ 1,382 |
| 40 % | phase 3: the core | 551 | ≈ 1,849 |
| | | **1,377** | **≈ 4,620 = 24.0 %** |

When the level's scale is set the bounty is recomputed with the same formula; the shares stay. The released units
pay their own bounty, outside the share.

### Death

The core at 0 (**K**): the heartbeat **stops** (the pulse holds dark for a beat), the Heart's bullets on the screen
fade out over 0.5 s, the bar goes. A **chain** runs out from the core through the Heart's tissue for 4 s (lime-white
bursts, the tissue collapsing inward, debris in rings: the [vacuum rules](../../../world/belt/README.md#vacuum-rules));
the **act boss's screen flash** at K + 3.5 s (under the flash-reduction option) and the **credit shower** over the
dead Heart. At K + 5 s the Choir speaks its **last line** (see *The Choir*), its portrait's pattern forming something
face-like, then scattering. With the line's end the **closing gate** begins: the Choir held the gate open and it now
closes; the wing's race back through the event plane is a **scene** (no fail, no bar, no target; Level 50's document
and the [story](../../../story/README.md) own it, Q8 a), then the campaign's end
([debrief](../../../ui/debrief/README.md#the-campaign-end-act-7-draft)).

### The Choir

[The Choir](../../../story/characters/the-choir/README.md) speaks on the enemy channel (its own dress: no static, the
teal-violet pattern in the portrait frame; in Act 7 it is close, so clean). **This document owns the order and the
wording of the Heart's fight lines**; Level 50 places and times them and adds the friendly lines between them (Varga,
Rook, Okafor). Drafts, decided 2026-11-05, round 84; only line 5 is the story's fixed line (the last words);
lines 1 and 3 are the story step's wording, since Levels 43 and 47 hold the fixed address to Lancer and the fearful turn:

| # | When | Line (draft) |
|---|---|---|
| 1 | the warning (before it shows) | "…small bright one… you have come into the heart of us… turn back… turn back…" |
| 2 | the closing (phase 2's start) | "…we sang so it would not hear us… you are so loud… so loud…" |
| 3 | the opening (phase 3's start) | "…you opened us… now it can hear… it is listening…" |
| 4 | the core below half its HP | "…let us through… we only wanted to live… let us through…" |
| 5 | K + 5 s (the death) | "…it is behind us… it has always been behind us…" (fixed; the portrait's face-like frame, then it scatters) |

A line never starts during a told line's tell (it never talks over the tell's sound); the Silence is never shown or
described, only feared.

### Variants b and c (for the round's a/b/c)

- **b — A five-part medley** of every Vrell attack style, one part each. Larger, over 180 s, and the arena never
  changes.
- **c — Two phases, then a scripted escape** from the collapsing hive as the finale. Medium; the final fight is shorter
  and the escape becomes a fight (round 80's lesson: an unclear chase reads worse than a scene).

### Presentation

- **Look brief**: a vast luminous organ hung in the hive core's dark tissue, **A.7 b "the crown"** (chosen 2026-11-05,
  round 84): a vast **dome banded with glowing rings and spokes** (the Choir Herald's ring markings) in near-black
  violet tissue, its red-violet light swelling with the slow pulse (≤ 1 Hz, smooth, never a flash; the pulse's
  brightest below the bullets' cores); the six membranes **mouths on its rim** of pale violet tissue, each hiding a
  singer organ glowing in its unit's colour when open; the nodes lime knots of nerve in the walls; the core
  lime-white under the opened dome. Echoes of every Vrell unit in its surface (the Herald's ring markings, the Spore
  Bomber's sacs, the Whirl Seeds' spirals) so the "every Vrell style" reads at a glance.
- **Phase 2**: the walls are ribbed tissue with a lit inner edge (the darkness lesson: the field's shape always
  reads); the darkness is the play field's only, never the HUD's (the HUD's rule).
- **Drawing**: fixed; the Heart's pulse as a smooth light layer, the membranes in their states (closed, opening, open,
  dead), the walls as terrain strips with breathing frames, the dome's opening along its spokes and the collapse as sequences; every glow
  and haze smooth and fading to 0 inside its canvas (≥ 32 steps, no ordered dither, drawn once).
- **Readability**: the violet over the violet chamber (the Heart a step brighter and warmer than its walls); the
  borrowed bullets as built; the drift's slide visible in the chamber's parallax.
- **Music**: the boss warning (track 22), then **track 20** (the final boss theme, chosen in round 03, full length for
  M11, J8) from the warning's end; at K it falls away to the Choir motif alone under the last line; the closing gate's
  music and the ending's track 27 are Level 50's and the [music](../../../audio/music/README.md) document's.

### Sounds brief

Real recordings from Freesound (CC0 / CC-BY); deep and organic but dry, never wet, bubbly or popping. Borrowed attacks
and released units keep their own sounds.

| Sound | Where / when | Brief |
|---|---|---|
| Heartbeat | every pulse | a huge, slow, muffled drum-like thud with a low sub swell (a bass drum through a wall, slowed) |
| A membrane opening | each song's tell | a dry stretching creak (leather or a sail under strain), short |
| The closing | phase 2's start | a vast groaning creak, the walls grinding in: stone and rope under strain, slowed |
| Heart's line | its charge and shot | a rising choral hum (a choir sample pitched up), then a hard electric crack |
| A node cut | each node's death | a taut cable snapping, then the wall's groan easing back |
| The opening | phase 3's start | a long tearing rip, slowed, and a rising tone as the light returns |
| The death | the chain | the heartbeat stopping mid-beat, then deep collapsing rumbles and a long fading choral tone |

## Concept art

Concept round 84 (Act 7 design phase, step ART-R-B, 2026-11-05; decided 2026-11-05, round 84): the Heart at the top of
the field over Level 50's chamber, its pulse a smooth light layer (two renders blended), the membranes in their states,
the walls and the dark of phase 2, the split and the core. Generator `tools/concept/act7_r84.py heart`; prompts:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/choir-heart-r84-b.png](concept/choir-heart-r84-b.png) | B "the crown": a vast domed organ banded with concentric rings and spokes (the Herald's ring markings), the membranes as mouths on its rim, a sealed boss over the core that splits; as A (sheet) | chosen (round 84 A.7 b) |
| [concept/choir-heart-r84-b.gif](concept/choir-heart-r84-b.gif) | B at 1×, as A | chosen (round 84 A.7 b) |

Round 95 (M11 part H, step ART-H, 2026-11-07; production art, pending the user's review): the Heart in production by
`tools/art/choir_heart.py`: round 84 b's crown at the spec's places and size (440×300, its centre at (240, 70), the core
at (240, 150)), rendered 1:1 at 3× under the fixed key light: the sealed dome (five ring ridges, curved spokes, the
Spore Bomber's sacs on its shoulders, the sealed boss's eight-armed whorl), its pulse as a smooth additive layer over
the body (drawn with the pulse's alpha) and a halo on the chamber; the closing's contraction (4 frames, the last
phase 2's body); the dome opening along the whorl's spokes (8 frames, light leaking from the splits; the last phase 3's
body with the core); the six membranes (closed, the iris in 4 frames, open, the same damaged, dead), each singer's
glow in its unit's style; the wall nodes (left and right: whole, damaged, severed; their glow); the core (whole,
cracked; its beat; a storm attack's flare); the walls as wrapping 128×256 tissue strips in 8 breathing frames with a lit
inner edge and the veins' tell; the pits (shut to open); the wall spit's flash, the centre whitening for the line, the
sparks, the deaths (membrane, node), K's collapse (12 frames) with the chain's bursts and debris. Held in
`build/art-held/` until step H2's data claims `choir-heart`; the pivots (`pivots/choir-heart.json`) give every part's
offset, the frame meanings and the chain's points and times. Prompts:
[concept/prompts.md](concept/prompts.md#choir-heart-final-r95-a).

| File | What | Status |
|---|---|---|
| [concept/choir-heart-final-r95-a.png](concept/choir-heart-final-r95-a.png) | The three phases at 1× over the hive-core arena (ART-H2's kit) (the pulse low and high, a membrane singing, two at once, the arena in the dark, a wall move's tell, the storm), the closing, the opening, the collapse, every part in its states, the singers' loops, the walls, pits and effects (sheet) | proposed |
| [concept/choir-heart-final-r95-a.gif](concept/choir-heart-final-r95-a.gif) | A scripted pass at 1×: the arrival, the songs (one, then two at once), the closing, the arena (spit, a wall's tell, the line's tell, the nodes cut and the light coming back), the opening, the storm, K's collapse | proposed |
| [concept/heart-fight-r95-a.mp4](concept/heart-fight-r95-a.mp4) | Game capture (step GH5), the whole fight in real time with sound, the whole screen, medium, Rook: the warning, the descent volley, phase 1's membranes singing and dying silent, the closing (the walls, the dark), phase 2 with the reversal, the opening, phase 3's storm, K and the flash (t=180–368.5, 188.5 s) | proposed |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

Built in M11 part H with Level 50 (step H2: the data, the content keys, the measures; the game's looks and sounds are
step H4's).

- [x] Stat block and boss script in a `data.yaml` (the parts, the three playbooks, the songs, the heartbeat, the walls'
      positions and moves, the darkness and drift cues, the phases and their transitions, the par); the ×1.05 hard
      lever
- [x] The membranes as window shields (the Gate Warden's rule, part A's), the two-at-once singing from the phase's half
- [x] **A boss that reshapes its arena** (the gap analysis's large risk): moving terrain walls (the canyon walls'
      collision on polylines that move, told, never toward a ship within 40 px), the darkness switched on and off
      inside a level (Act 4's light map), the distorted-space drift and one reversal inside a boss phase (part A's)
- [x] The never-quiet rule and the standing-still check (five seeds, medium and hard, every phase), the fight inside
      150–180 s on every seed, the HP tuned, the par set; the bounty at ≈ 24 % with Level 50's scale
- [x] The three-segment bar `CHOIR HEART`; the Choir's lines in Level 50's radio timeline; the face-like frame at the
      last line; the hand-off to the closing gate scene
- [x] Rook's and the autopilot's rules (`HeartPlay`)
- [x] Production art (the Heart, the membranes, the walls, the split, the collapse); readability over Level 50's
      chamber in the light and the dark (built by H4, G6 and GH5; the dark's unit rims and the walls in the dark judged in round 95)
- [x] Sounds (Freesound, CC0/CC-BY) per the brief; track 20 in full

## Open questions

None.

## Decisions

- 2026-11-08 (M11 part H, step H2; pending the user's review in round 95): built to the user's boss recipe and
  measured: every phase fires from its parts (the membranes' mouth pairs, the nodes' bursts, the core's burst; each
  silent at its death), it comes into view firing a descent volley; the HP tuned to 14,400 (phase 1 11,400, phase 2
  1,200, phase 3 1,800) for 150–180 s on every seed at medium (par 165), hard won without a checkpoint; phase 2's spiral 2
  arms, its wall spit 3 volleys, the node bursts of 2; a node's guns fire from inside the corridor; the patches drop
  250 px under the centre; the phase 3 heartbeat its own attack. The open questions closed (round 84's answers, §6 of
  the gap check): **the checkpoints** at the warning and each phase's start on easy and medium (this document's
  decision of 2026-11-04), **phase 2's three tools at once** (E.4 a).

- 2026-11-04 (Act 7 design phase, step m11-d-boss; decided 2026-11-05, round 84): promoted from the bosses
  roster to this draft spec (Q7 a, Q8 a's hand-off, J8, J9, J11): 19,900 HP over three phases (paper 162 s), the six
  membranes singing the Vrell's styles as window shields, the arena phase (walls, darkness, drift with one reversal,
  four nodes), the core's storm (two at once), the heartbeat as the never-quiet floor, 460 credits at a hand scale 0.60
  (24 % of budget(50)), the Choir's five lines (three fixed). Decided here: a checkpoint at each phase's start; the
  bullets fade at the death; no releases in phase 3.
- 2026-11-04 (step m11-d-act, decided 2026-11-05, round 84): the bounty recomputed at Level 50's scale 0.34: **811** (41 a
  membrane, 61 a node, 321 the core; ≈ 24 % of budget(50)); the Choir's lines 1 and 3 reworded by the story step
  (Levels 43 and 47 hold the fixed lines), lines 2 and 4 kept as drafted (the open question closed).
- 2026-11-05 (step m11-art-r-b, decided 2026-11-05, round 84): concept art proposed: a "the heart" and b "the crown".
- 2026-11-05, round 84 (user, page answers): **Q7 a** three phases as drafted (b and c rejected), **E.4 a** the second
  phase all at once; **A.7 b** (not the recommendation): the **"crown"** look, a vast dome banded with glowing rings and
  spokes (the Choir Herald's ring markings), the six membranes as mouths on its rim, and where the draft said the seam
  splits for the core, the dome **opens along its spokes**; the "heart" (a) rejected; **S.25 b**, **S.26–S.31 a**: the Heart's
  sounds as picked in the audio README; **D.13 approved**. The open items were:
  - **Q7** (round 84): **a** three phases (this draft), **b** a five-part medley, **c** two phases and a scripted escape.
