---
title: Enemies
design: approved
implementation: in-progress
art: final
updated: 2026-10-28
---

# Enemies

## Summary

All hostile units in the game: 66 regular enemies across the Vrell, the Jovian Ascendancy and
Ascendancy/Vrell hybrids, plus 7 act bosses and 5 mid-bosses. They range from tiny swarmers to
huge multi-part creatures, and include serpents, walkers, spinners and machines as well as
flyers. This document defines the shared vocabulary for enemy specifications: the stat block,
size tiers, multi-part and orientation rules, movement, attack and formation patterns, layer
rules, bullet readability, difficulty scaling and the per-act variety checklist. The category
directories hold the rosters.

## Contents

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [air](air/README.md) | Flying enemies on the `air`, `low-air` and `high-air` layers, incl. serpents, spinners and drone trains (24) | approved | in-progress | final |
| [ground](ground/README.md) | Turrets, walkers, tanks, crawlers, bunkers and spawners on the `ground` layer (23) | approved | in-progress | final |
| [naval](naval/README.md) | Surface vessels and submerged enemies (`ground` on water, `sub`) (11) | approved | in-progress | final |
| [space](space/README.md) | Vacuum-only enemies for space levels, incl. the Leviathan (8) | approved | in-progress | final |
| [bosses](bosses/README.md) | 7 act bosses and 5 mid-bosses | approved | done | final |

## Design

### Factions and visual language

| Faction | Look | Gameplay identity | Weak points |
|---|---|---|---|
| **Vrell** | Grown, not built: chitin plates, organic curves, bioluminescent glow (teal/violet). The hive grows many body plans: insectoid, serpentine, cetacean, crustacean, cephalopod, seed and plant forms. | Swarms, spawners, organic projectiles (thorns, spores, acid), living terrain; serpents and leviathans that move as many parts. | The glowing parts. Briefings teach "aim for the glow". |
| **Jovian Ascendancy** | Human-built: angular, black hulls with gold trim, visible hex-pattern shields. Aircraft, tanks, walking mechs, spinning drones, drone trains and rotating platforms. | Disciplined formations, shields, missiles, rail guns, mines. Fewer units, each tougher. | Exposed engines at the rear, and shield generators. |
| **Hybrids** (Act 6+) | Ascendancy black-and-gold plating with Vrell tissue growing through the seams. | Combine both: regeneration, reflecting shields, death bursts. | Tissue seams, visible as glowing cracks. |

Unmarked Ascendancy machines in Acts 3–4 (Ghost Drone, Revenant Walker, Depth Hunter) are grey
with no trim. They are recognisably human-made, but belong to no known faction.

**The Ascendancy in the open** (Act 5 on; decided in round 60, 2026-10-27, Q11 **b**, not the
recommendation): **gunmetal and charcoal hulls with gold trim**, not gloss black: a strong top-left rim light
and red running lights, gold on at least 20 % of the silhouette, so the units read over the belt's dark rock and
space; the belt backdrops stay a step lighter than Act 4's (a sunlit dust band, warm rock). This diverges on
purpose from the Level 28 cruisers (final, gloss black): the cruisers stay as they are, and the Act 5 units'
chosen variants are re-rendered in gunmetal/charcoal in their parts' production art. Crewed craft carry a
light hex [shield](#enemy-shields); expendable drones do not.

#### Role colours

Round 03 feedback: the Vrell read as uniformly magenta. From round 04 every unit gets its own
colour identity inside its faction, so players learn threats by colour (draft rule, awaiting the
user; colour values live in `tools/concept/render/enemy_models.py`):

- **Vrell chitin base = role family**

  | Base | Hex (mid / dark / spike) | Role family | Round 04 units |
  |---|---|---|---|
  | Plum | `A020A8 40004A C890D8` | Swarm fodder | Skitter |
  | Rust | `B04A2C 3A1008 E0B890` | Fast attackers, divers | Stinger |
  | Bone / ivory | `DCCFB4 5C4A5C F4ECD8` | Ranged gunners and snipers | Needler, Mantis |
  | Olive | `7C8C3A 263010 C8C890` | Bombers, area denial from the air | Spore Bomber |
  | Slate (mauve-grey) | `7C6878 241A24 C8B8C0` | Rooted ground units | Spine Turret, Polyp Mortar |
  | Teal-black | `1E5C5A 06201E 8AB8A8` | Spawners and carriers | Brood Pod, Brood Carrier |

- **Vrell glow hue = kind of threat** (seams, eyes, veins, weapon tips)

  | Glow | Hex | Threat | Round 04 units |
  |---|---|---|---|
  | Teal | `00FF9A` | Contact, ramming, spawning | Skitter, Brood Pod, Brood Carrier veins |
  | Violet | `9A4DFF` | Aimed shots | Needler, Spine Turret |
  | Crimson | `FF3038` | Lasers, sweeps and dives (lines of danger) | Stinger, Mantis |
  | Lime | `A8FF2A` | Area denial: mines, spores, acid | Spore Bomber, Polyp Mortar |

- **Weak points** glow in the unit's glow hue at full brightness (slightly whitened). **Bosses and
  mid-bosses always mark their weak points in lime** (`A8FF2A`), whatever their own colours, so
  players learn one rule for where to shoot (user decision, round 06).
- **Ascendancy** stay black & gold; each unit adds one secondary accent (Talon red, Gilded
  Gunship white, Rail Bunker gunmetal) and every Ascendancy sprite gets a 1 px **rim light**:
  gold on edges facing the key light (top-left), red on edges facing away.
- **Reserved hues are never used on bodies or glows**: enemy-bullet magenta `FF40FF`, needle
  yellow `FFFF40` and orange (see [art direction](../art-direction/README.md#readability-rules)),
  player blue / white / cyan. Vrell needles are drawn yellow, orbs magenta, as the bullet rules
  require (round 03 drew needles magenta).
- New units pick a base by role and a glow by threat; a unit whose combination is already
  taken differs by shape and size, never by a new hue outside the tables.

### Stat block template

When an enemy is promoted from a roster row to its own directory, its README gets this stat
block, followed by behaviour notes and the standard sections.

| Field | Meaning |
|---|---|
| Faction | Vrell / Ascendancy / Hybrid / Unmarked; a **machine** (M7 part A: `machine: true` in its data file, shown after the faction) is stunned 3 s by the [EMP Burst](../player/specials/README.md#emp-burst-l15), every other unit is biomechanical (Vrell) and stunned 1.5 s. No unit of Acts 1–2 is a machine; Act 3 marks the Ghost Drone, the Warden Tank and the Revenant Walker |
| Layer | From the layer vocabulary (`ground`, `low-air`, `air`, `high-air`, `sub`, `space`) |
| Size tier | `tiny` / `small` / `medium` / `large` / `huge` (see [size tiers](#size-tiers)) |
| Size | Sprite size in px at the 960×540 baseline (e.g. 36×36; see [art direction](../art-direction/README.md)) |
| Parts | `single`, or the part list for multi-part enemies: segments / articulated parts, with per-part HP and which parts are destroyable (see [multi-part enemies](#multi-part-enemies)) |
| Orientation | `fixed` (always faces down), `16 angles`, `32 angles` or `radial` (see [orientation](#orientation-and-rotation)) |
| HP | In **damage units**: 1 = one shot of the starting front gun at upgrade level 1 |
| Armour / shield | Damage reduction or a shield layer with its own HP; the traits that bypass it |
| Speed | px/s at 960×540 |
| Movement | Pattern name from the movement vocabulary + parameters |
| Attack | Pattern name(s) from the attack vocabulary + interval, bullet count and bullet speed |
| Formations | Formation names it appears in |
| Weak points | Drawn glowing; a damage multiplier only on a part of a multi-part unit (see [multi-part enemies](#multi-part-enemies)) — a single-part unit's weak point is drawn only |
| Effective traits | Weapon traits that do extra damage or are needed |
| Credits | Bounty at medium in Act 1 terms (see [economy](../systems/economy/README.md)); score is derived from it (see [scoring](../systems/scoring/README.md)) |
| Death | Explosion size, debris, drops, death behaviour (e.g. death burst) |
| First level | Level number where it is introduced |
| Difficulty hooks | Overrides of the global scaling (see below) |

### Size tiers

Waves mix sizes on purpose: tiny swarmers make a screen feel alive, huge units make it feel
dangerous. Sizes at the 960×540 baseline (the play field is 480×540); sprite sizes per class are
in [art direction](../art-direction/README.md#sprite-sizes-native-pixels).

| Tier | Size (px) | HP guideline | Typical roles | Examples |
|---|---|---|---|---|
| `tiny` | 16–24 | 1 | Flocks, seeds, clusters; die in one hit, threaten by numbers and paths | Skitter, Whirl Seed, Mote Swarm, Asteroid Mite |
| `small` | 28–48 | 2–8 | Fodder gunners, interceptors, drones | Needler, Talon, Buzzsaw Drone, Glow Angler |
| `medium` | 56–110 | 10–40 | Gunships, walkers, tanks, spawners | Gilded Gunship, Scuttler, Warden Tank, Brood Pod |
| `large` | 120–250 (chains: total length) | 40–150, often per part | Mechs, segment chains, rotating platforms, rays | Coilwyrm, Strider, Rail Serpent, Halo Platform, Abyss Ray |
| `huge` | ≥ 1/3 of the play field, up to multi-screen | per part; 150+ in total | Set-piece creatures and structures that are not bosses | Leviathan |

Mid-bosses are `large` to `huge`, act bosses `huge`. A `huge` regular enemy is a set piece: at
most one on screen, announced by radio, with a bounty close to a mid-boss.

### Multi-part enemies

- **Segment chains** (Coilwyrm, Threadcrawler, Rail Serpent, Eel Swarm): the head moves; every
  segment follows the head's **path history** at a fixed distance, so the body traces exactly
  where the head has been (loops and swirls stay intact). Chains are 6–16 segments.
- **Articulated parts** (Leviathan tail and fins, claws, turret rings): attached to a body at a
  pivot and animated relative to it (sweeps, lagged follow-through), not following a path.
- **Per-part HP.** Each part has its own HP and hitbox. Parts are `armoured` (bullets spark off,
  no damage), `destroyable` (break off, pay a part bounty, may change the enemy's behaviour) or
  `vital` (the weak point; destroying it kills the whole enemy).
- **Chains:** the head is vital. Destroying a body segment splits the chain: on the Coilwyrm
  the rear half grows a new head and becomes its own, faster chain — **once per chain**: a regrown
  chain that is cut again simply dies from the cut backwards; on machines (the [Rail Serpent](air/rail-serpent/README.md); Act 5 draft, decided in round 60)
  the rear half loses power, drifts with sparks for 1.5 s and explodes car by car from the cut
  backwards, harmless to the ship and paying its cars; the front half runs on with the engine. The tail pays a
  bonus when destroyed first.
- **Scoring:** part bounties add up to at least the whole-enemy bounty, so dismantling a big
  enemy part by part pays as well as killing its weak point; killing the vital part first
  destroys the rest with a chained explosion and a time bonus.
- **Hits and collision:** only parts on the player's layer collide. A part flashes when hit; an
  armoured part flashes grey so players learn where not to shoot.

### Boss attacks and root bursts

Planned (M5 part H, decisions H2 and H5 = a, accepted in round 34 (2026-10-12); the schema is in
[architecture](../tech/architecture/schemas-units/README.md)):

- **Boss `lob`.** The enemy `mortar` attack with a part as its origin (the Polyp Mortar's lob
  generalised): the impact marker shows on the ground layer for 1.0 s, then a ring of 10 bullets
  (hard 12), every 8 s per mortar root (4 s until round 34's P2). Used by the [Siege Spire](bosses/siege-spire/README.md).
- **Boss `sweep`.** The `laser-sweep` geometry of the Mantis with a part as its origin: a 120° arc
  (hard 180°), shown for 0.6 s, then a `laser` beam of 8 once per sweep, every 5 s, with `ring`s
  between. Used by the airborne Siege Spire.
- **Root burst entry.** A level's `root_bursts` place ground units at a point of the screen
  instead of the top edge: the paving cracks and dust rises for the 1.0 s telegraph, then the
  growths (a [Spine Turret](ground/spine-turret/README.md) and a
  [Polyp Mortar](ground/polyp-mortar/README.md) per burst) stand there and scroll with the
  ground from then on. A growth is invulnerable and silent while it grows and fires first
  1.5 s (hard 1.0 s) after it appeared; a burst lies 100–400 px below the top edge and never
  within 60 px of the ship's x at the telegraph; it has no hit and no collision.

### Orientation and rotation

The first enemies all have a clear nose and tail and only work flying down the screen. Enemies
that move in any direction must read from any angle:

- **`16 angles`** (22.5° steps) for enemies that turn to face their movement; **`32 angles`**
  for large and slow enemies and for turrets. Every angle is a separate pre-rendered frame from
  the 3D model with fixed lighting; the game shows the nearest one (see
  [animation rules](../art-direction/README.md#animation-rules)).
- **`radial`** for spinners: radially symmetric designs that spin in place or along their path
  and need no facing at all.
- **`fixed`** remains for enemies that only ever fly down the screen (most of the original
  roster).
- Independent parts (turrets, heads, arms) turn on their own, e.g. a tank hull drives one way
  while its turret tracks the player.

### Movement pattern vocabulary

| Name | Description |
|---|---|
| `straight` | Constant velocity along a vector. |
| `swoop` | Enter on a curve, cross the screen, exit on a curve. |
| `sine` | Straight path with a sinusoidal side-to-side offset. |
| `dive` | Enter, pause briefly, then accelerate toward the player's current position. |
| `hover` | Enter, hold a position for a set time, then exit. |
| `strafe` | Horizontal pass across the screen at a fixed height. |
| `orbit` | Circle a point (fixed, or relative to a leader or the player). |
| `path` | Follow a hand-authored spline (used for snakes and set pieces). |
| `chase` | Steer toward the player with a limited turn rate. |
| `terrain` | Fixed to the ground layer; moves only with the scroll. |
| `crawl` | Moves along the terrain (roads, rooftops, sea floor) at its own speed. |
| `burrow` | Alternates between submerged (invulnerable, shown as a shadow or ripple) and surfaced. |
| `drift` | Slow, inertial movement with no intent (mines, jellies, derelicts). |
| `latch` | Attaches to the player; the player shakes it off by moving hard back and forth. |
| `teleport` | Vanishes and reappears after a visible warp-in telegraph of at least 0.5 s. |
| `mirror` | Mirrors the player's horizontal movement around the screen centre. |
| `swirl` | A curving path that bends back on itself (S-bends, hooks), crossing its own track; the main pattern for chains and flocks. |
| `spiral-in` / `spiral-out` | Circles a point (often the player) while the radius shrinks or grows. |
| `loop` | A full loop-the-loop mid-path, often to turn around and come back from the rear. |
| `figure-8` | Traces a figure-eight around two points, typically across the whole play field. |
| `cross` | Crosses the screen diagonally or side to side, entering and leaving through different edges. |
| `rear-entry` | Enters from the bottom edge (or loops around to it) and attacks up the screen; a wave entering from the bottom edge is always edge-warned 3 s ahead; a loop-around re-entry is the unit's own flight and is not (2026-10-21). |
| `flock` | Boids-like group movement (separation, alignment, cohesion) steered by a leader or target; produces swirling clouds. Planned (M5 part D) for the [Mote Swarm](air/mote-swarm/README.md): a leader point flies the wave's authored route, the members steer round it; deterministic (fixed order, `StrictMath`) and allocation-free, at most 24 members. |
| `walk` | A walker moving along the terrain on its own heading (not just scrolling), turning to face where it goes; the gait follows the walk cycle. |
| `surface` | Emerges from the ground or water at a telegraphed spot (ripple, dust), acts, and dives again; like `burrow` but it travels while submerged. |
| `ricochet` | Bounces off the play field edges (and off terrain walls) at equal angles; in the belt also off the big, indestructible drifting rocks of the `Debris` hazard (their outline taken as a circle), while a dash smashes small breakable rocks (the [Buzzsaw Drone](air/buzzsaw-drone/README.md), Act 5 draft). |
| `ride` | Rides a small drifting rock until the ship comes near, then leaps off at it (a `dive`); see [rock riding](#rock-riding) (the Asteroid Mite, Act 5 draft). |
| `spin` | Rotates about its own axis while moving (a modifier on any other pattern); spin speed may change with the attack phase. |
| `chain` | Follows a leader's path history at a fixed distance (used by every segment of a chain). |

### Attack pattern vocabulary

| Name | Description |
|---|---|
| `none` | Harmless except on contact (rammers). |
| `aimed` | A single shot at the player's current position. |
| `burst` | n aimed shots in quick succession (0.15 s apart). |
| `fan` | An n-way spread, centred on the player, straight down or along the unit's facing. |
| `ring` | A circular burst of n bullets. |
| `spiral` | A rotating stream of bullets. |
| `laser-sweep` | A continuous beam rotating through an arc; the arc is shown for 0.6 s first. |
| `laser-line` | A charged straight beam; a thin line telegraphs it for ≥ 0.8 s. |
| `homing` | Slow projectiles that track the player; shootable (1–3 HP). The Ascendancy's missiles of Act 5 on are drawn as the red-gold **homing diamond** (the bullet rules' diamond; chosen variant a, round 60). The player's homing shots, Rook and the Tesla arc lock onto a diamond only when no enemy unit is in reach (QA-1, decided 2026-10-27, confirmed in round 61 on 2026-10-28). |
| `mine` | Drops stationary or drifting mines; shootable. From Act 5 also laid in **lines** with one gap, with a proximity burst and a chain blast: see [mine lines](#mine-lines) (the Minelayer, draft). |
| `mortar` | An arcing lob to a target point marked on the ground layer; bursts into a `ring` on impact. |
| `kamikaze` | Rams the player at speed; may combine with `death-burst`. |
| `spawn` | Releases other enemies (a carrier or node). |
| `link` | A damaging energy beam between two or more units; acts as a barrier. Between ground anchors (the [Tendril Anchor](ground/tendril-anchor/README.md), a tendril a beam burns) or, from Act 5, between moving air units (the Shard Drone's energy links, see [air links](#air-links); draft). |
| `aura` | Buffs nearby enemies (shield, speed or regeneration); shown as a visible ring. |
| `drain` | Drains the player's shield or generator energy while in range or latched. |
| `reflect` | A frontal shield bounces non-`beam` player shots back as enemy bullets. |
| `death-burst` | Releases a `ring` or `fan` when destroyed. |

### Formation vocabulary

<!-- data: formations -->
| Name | Description |
|---|---|
| `single` | One unit on its own, as when a new enemy is introduced. |
| `V-wing` | A V of 3–9 units led by the tip. |
| `line abreast` | A horizontal line moving down together. |
| `column` | A vertical line, one behind the other, on the same path. |
| `snake` | A column following a curving `path`, each unit delayed by a fixed interval. Readability: the head is on screen at least 1.5 s before it can reach the player, however long the snake. |
| `stream` | A continuous trickle of single units from alternating edges. |
| `pincer` | Two groups entering from the left and right edges at the same time. |
| `rear ambush` | A group of 2–4 attacking from the bottom edge, each in its own lane across it. A cloaked Wraith enters at the top first; its re-entry behind the ship is its flight pattern and gets no edge warning (M5 part D; 2026-10-21, see readability). |
| `circle` | Units orbiting a point, then breaking off. |
| `grid` | A block of units that holds and shifts sideways (a nod to classic arcades). |
| `wall` | A full-width line with one or two gaps to fly through. |
| `cross` | Groups from all four edges converging, n per edge, each unit on its own lane, entering together and warned on every edge (M5 part G, Level 13). |
| `carrier + escorts` | A large unit (often `spawn`) with escorts in `orbit`. |
| `turret nest` | 3–6 ground turrets in a cluster with overlapping fire. |
| `convoy` | Ground or naval units in a column along a road, river or lane. |
| `pack` | 3–5 walkers entering together, each on its own ground path, 0.25 s apart (the Ravager). |
| `submerged ambush` | `sub` units that surface together around the player (Act 4: no Act 2 level uses it). |
| `field` | Units scattered over an area that enters with the sea (scroll factor 1, surfaced or submerged) and drifts on the wave's current: a Driftjelly field of 6–12 (M5 part E). M7 part B: a Burrower field of 3–6 enters buried with the ground and burrows from there, its surfacings staggered. |
| `swarm` | A loose cloud of 6–24 units with flocking behaviour round a leader point that flies an authored route, from the top or the bottom edge, often looping back from below (the Mote Swarm, M5 part D). Planned (M5 part F): a swarm of chasers (the Lamprey, 3–6) is a cloud at its entry edge (front, sides or rear) whose members each start their own chase after a seeded 0–0.6 s delay, without flocking. |
| `pursuit` | A flock that presses from behind in a reversed section (M7 part G, Level 20): its leader point holds a band behind the ship (the top of the screen once the scroll is reversed), following its x with a lag, lunges down the ship's lane after a contracting tell, takes refills from the top edge while it has room and breaks off alive at a set time (the Mote Swarm, 8–12 on entry). |
| `solo set piece` | One huge unit on its own, announced by radio (at most one per level). |
| `whirl cluster` | A burst of 5–8 tiny spinners released from one point, spiralling outward and ricocheting. |
<!-- /data -->

**Formation layouts** (first values, to tune): a V-wing's ranks are 50 px apart sideways; a
pincer's groups enter 60 px from their edge; a circle's centre is 180 px below the top of the
play field and its units break off in groups 0.5 s apart, a Needler at its 120 px/s entry speed.

**Act 5 additions** (draft, approved in round 60; they join the rendered table
above when M9 part A writes them into [data.yaml](data.yaml)):

- `linked group`: 3–4 units flying together, joined by energy links in a line or a closed ring
  (the [Shard Drone](space/shard-drone/README.md); see [air links](#air-links)).
- **Squad leader** on a `V-wing` of Talons: the tip is a gold-striped leader; killing it scatters
  the wing (the [Talon](air/talon/README.md)). Other units' V-wings have no leader.
- A **mine line** is not a formation (mines are not units): it is the Minelayer's `mine` attack,
  see [mine lines](#mine-lines).

### Variety checklist per act

Every act must pass this checklist (counting the units that appear in the act, new or
returning; returning units are listed in the act's level notes):

1. At least **three size tiers**, including `tiny` or `huge`.
2. At least one **multi-part** enemy (segment chain or articulated).
3. At least one **walker or ground mover** (walking, crawling, tracked, burrowing).
4. At least one **spinner or radial** enemy.
5. At least one **non-front entry** pattern (sides, rear, loop-around, surfacing).
6. **No more than about half insectoid** units (insect, arachnid or myriapod bodies; crustaceans,
   serpents, cetaceans, cephalopods, seeds and machines don't count).

Current coverage (✓ = passes):

| Act | Size tiers | Multi-part | Walker / ground mover | Spinner / radial | Non-front entry | Insectoid share | Pass |
|---|---|---|---|---|---|---|---|
| 1 First Contact | tiny, small, medium, large, huge | Coilwyrm (L06), Leviathan (L03), Gorgon Frigate (L05), Brood Carrier (L07) | Scuttler (L04) | Whirl Seed (L03) | Mantis sides (L06, L07), Coilwyrm rear loops (L06), Brood Pod Skitter bursts | 3 of 14 (Skitter, Stinger, Mantis); the 14 used in L01–L07: Skitter, Needler, Stinger, Spine Turret, Spore Bomber, Whirl Seed, Leviathan, Brood Pod, Scuttler, Polyp Mortar, Gorgon Frigate, Coilwyrm, Mantis, Brood Carrier | ✓ |
| 2 Homefront | tiny, small, medium, huge (as built: no `large` unit) | Harbour Kraken (L11), Siege Spire (L14) | Creeper (L08), Ravager packs (L09, L14), Scuttler returns (L13) | Whirl Seed returns (L12) | Wraith and Mote Swarm rear (L10), Mantis sides (L12, L13), Skimmers from all edges (L13), maw Wraiths (L14) | 5 of 20 (Skitter, Stinger, Creeper, Wraith, Mantis); the 20 used in L08–L14: Needler, Skitter, Stinger, Spine Turret, Polyp Mortar, Creeper, Hive Node, Ravager, Mote Swarm, Wraith, Driftjelly, Reef Spitter, Harbour Kraken, Lamprey, Mantis, Spore Bomber, Whirl Seed, Scuttler, Skimmer, Siege Spire | ✓ |
| 3 Red Dust | tiny (the new Dust Kite and Mote Swarm, plus returning Skitters), small, medium, large, huge | Threadcrawler, Revenant Walker, Dust Colossus | Burrower, Threadcrawler, Shellback (L17), Warden Tank | Dust Devil (L18) | L19 rear, L20 reverse scroll, L21 breaches | 8 of 22 (Skitter, Stinger, Mantis, Creeper, Wraith, Burrower, Choir Herald, Threadcrawler); the 22 used in L15–L21: Dust Kite, Skitter, Mote Swarm, Needler, Stinger, Polyp Mortar, Spine Turret, Tendril Anchor, Ghost Drone, Burrower, Creeper, Ravager, Spore Bomber, Mantis, Choir Herald, Dust Devil, Wraith, Warden Tank, Threadcrawler, Shellback, Revenant Walker, Dust Colossus | ✓ |
| 4 Deep Water (as built) | tiny (Glassfin, returning Skitters and the Mote Swarm), small, medium, large, huge | Eel Swarm, Drill Rig (core and pumps), Abyssal Maw | Scuttler on the sea floor (L24) | Spiral Nautilus (L25–L28) | Mantis pincers (L22), Siren side caves (L25), the Abyss Ray rising from below (L24), Depth Hunter columns from the rear (L27, L28) | 3 of 25 (Skitter, Stinger, Mantis); the 25 used in L22–L28: Skitter, Mote Swarm, Glassfin, Needler, Stinger, Spine Turret, Polyp Mortar, Skimmer, Lamprey, Driftjelly, Reef Spitter, Barbfin, Glow Angler, Mantis, Scuttler, Siphon Pump, Siren, Spiral Nautilus, Depth Hunter, Relay Beacon, Drill Rig, Eel Swarm, Abyss Ray, Spawning Reef, Abyssal Maw | ✓ |
| 5 The Belt (draft, decided in round 60) | tiny (Asteroid Mite, Dart Drone), small, medium, large (Rail Serpent, Strider), huge (Iron Sovereign) | Rail Serpent, Shard Drone links, Strider legs, Iron Sovereign | Strider (L32), Warden Tank in Ascendancy livery (L34; the Crawler Tank dropped, J1) | Buzzsaw Drone (L31), Sovereign rings | Minelayer and Rail Serpent rear and sides (L30), Void Leech rear and sides (L33) | 1 of 15 new units (Asteroid Mite); the 15: Talon, SAM Nest, Asteroid Mite, Minelayer, Gilded Gunship, Rail Serpent, Dart Drone, Shard Drone, Hornet, Buzzsaw Drone, Sentinel Tower, Strider, Void Leech, Rail Bunker, Iron Sovereign; the returning units are counted from the level drafts | ✓ (on paper) |
| 6 Jovian Storm | small, medium, large, huge | Halo Platform, Ascendant | Warden Tank and Strider return (L39, L41) | Halo Platform (L41), Buzzsaw Drone returns (L38) | Honour Guard rear (L40), Harrow all edges | 0 of 12 | ✓ |
| 7 Beyond the Gate | tiny, small, medium, large, huge | Leviathan and Coilwyrm return, Choir Heart | Threadcrawler returns (L45) | Whirl Seed returns (L45) | all-direction levels (L44, L46–50) | 3 of 10 (Rift Skater, Choir Seraph, Threadcrawler) | ✓ |

When a level's waves are written, keep the act passing; the check is repeated when levels are
promoted to draft. Acts 1 and 2 were rechecked against their level documents on 2026-10-01; Act 2
was checked as built against Levels 08–14's data on 2026-10-12 (M5 part I): the 20 units of the
row, all three-plus tiers, both multi-part bosses, three walkers, the Whirl Seed, rear, side and
all-edge entries, 5 of 20 insectoid. Act 4 was checked as built against Levels 22–28's data on 2026-10-26 (M8
part I): the row above.

### Layer rules

| Enemy layer | Hit by player weapons | Collides with player | Notes |
|---|---|---|---|
| `air` / `space` | All weapons | Yes (contact damage) | The player's own plane. `space` = `air` in vacuum-only levels. |
| `low-air` | All weapons | No | Drawn smaller and lower; its bullets rise to the player plane. |
| `ground` | All weapons; `anti-ground` does ×2 | No; the [Ravager](ground/ravager/README.md)'s pounce is `air` for its middle 0.3 s | **Hardened** ground targets and units (bunkers, the [Hive Node](ground/hive-node/README.md)) can only be damaged by `anti-ground` weapons, the Airstrike and the Smart Bomb; other shots glance off with a spark, and homing shots and Rook do not pick them unless their weapon is `anti-ground` (M5 part C). |
| `high-air` | `homing` and `beam` only | No | Drawn larger and above the player; drops or deploys things. Descends to `air` to become fully hittable. |
| `sub` (player above water) | `anti-sub` (the Torpedo Pod) and the Smart Bomb only | No | Submerged, seen through the surface (the water rules: tinted, darker, blurred, wavy). Nothing else reaches it: no standard shot, homing shot, turret, landing blast (bombs, mortar shells, the Airstrike), mine or Rook (user decision E2 = a of M5 part E); homing shots and Rook never pick it. When it surfaces it becomes a `ground` (naval surface) target. |
| `sub` (underwater, Act 4) | The play plane stays `air` in water (a level or section sets `medium: underwater`); `sub` is the deep water *below* it. All weapons reach the play plane, ×0.7 damage without `anti-sub`; a deep `sub` unit is hit by `anti-sub` and the Smart Bomb only | Yes (the play plane); no for the deep `sub` | See the [under water rules](../world/europa/README.md#under-water-rules) (the rules table is the single owner of the numbers). |
| `deep` | Nothing | No | Background only. |

Enemy bullets always travel on the player's plane, whatever layer fired them.

**Water** (M5 part E, Level 11): a level whose data sets `water: true` is over water in its whole
play field (user decision E1 = a: Levels 11–13). There the `ground` layer is the sea surface:
naval surface units, surfaced parts and floating objects are `ground` targets (and the torpedo
reaches them, besides `sub`), kills sink with a splash and a ripple train instead of leaving a
crater or wreck, and landing blasts splash. A unit that surfaces or submerges (the Driftjelly, the
Harbour Kraken's head and arms) has a **current layer** that flips between `sub` and `ground` at the
middle of its swap, read at every hit, contact and targeting site (the part C pounce's and part D
cloak's mechanism). Submerged units scroll with the sea (factor 1); the `sub` layer's 0.8–0.9 in the
[layer model](../art-direction/README.md#parallax-layer-model) is for sea-floor scenery only, which
open ocean does not have.

### Bullet readability rules

- Enemy bullets are drawn **above every layer except the HUD**, including foreground clouds and
  debris. Foreground may hide enemy bodies but never enemy bullets.
- Enemy bullets are bright and high-contrast with a dark outline. Player shots are paler and
  less saturated, so the two never read alike.
- Bullet colour follows the faction colours in [factions](../story/factions/README.md): player
  pale blue/white, Vrell saturated teal/violet with a bright core, Ascendancy red/gold. Because
  player blue and Vrell teal are neighbours, saturation and the dark outline carry the
  difference, not hue alone. Where a background uses the same hues (Vrell space, Europa's
  bioluminescence), enemy bullets get larger and brighter (see
  [vrell-space](../world/vrell-space/README.md) and [europa](../world/europa/README.md)).
- Shape encodes threat: small round = standard, elongated = fast, large pulsing = slow and
  heavy, diamond = homing (shootable).
  The look follows the speed class, not the enemy's name for its bullet: a "thorn" at standard
  speed (the Needler's) is a standard orb; the yellow needle is only for the fast class.
- The hit box of a `small` enemy bullet is 6×6 px (the 9 px orb's core).
- Every laser and area attack is telegraphed: `laser-line` ≥ 0.8 s, `laser-sweep` ≥ 0.6 s,
  `mortar` impact point marked ≥ 1 s ahead.
- No enemy bullet spawns within 72 px of the player's ship.
- Waves entering from the sides or rear get an **edge warning**: an arrow at the edge of the
  play field ≥ 3 s ahead, often with a radio call. Only a wave's **first entry** warns: a unit's
  re-entry from a side or the rear as part of its own flight pattern (a Wraith's pop-up behind
  the ship, a chain's or a swarm's loop-back) never does, nor after its kill, and **bosses never
  trigger edge warnings** (their own tells announce them; 2026-10-21, user).
- A **bullet budget** caps the number of enemy bullets on screen (values per difficulty in
  [difficulty](../systems/difficulty/README.md)). Patterns degrade gracefully (fewer bullets per
  burst) when the budget is hit.

### Balancing basis

The Acts 1–2 stat blocks (round of 2026-10-01) use these first-draft assumptions; the economy
balancing sheet should use the same numbers.

**Reference player DPS** (single target, medium, a typical affordable loadout at that level;
damage units per second, Pulse Cannon L1 = 20, interpolated from the
[weapon roster](../player/weapons/README.md) and the [level budget](../systems/economy/README.md#per-level-budget)):

<!-- data: reference-dps -->
| Level | 01 | 02 | 03 | 04 | 05 | 06 | 07 | 08 | 09 | 10 | 11 | 12 | 13 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 | 24 | 25 | 26 | 27 | 28 | 29 | 30 | 31 | 32 | 33 | 34 | 35 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| DPS | 20 | 26 | 32 | 38 | 45 | 52 | 60 | 60 | 63 | 66 | 70 | 73 | 76 | 80 | 76 | 80 | 84 | 88 | 92 | 96 | 100 | 96 | 100 | 104 | 108 | 112 | 116 | 120 | 120 | 124 | 128 | 132 | 136 | 140 | 144 |
<!-- /data -->

Levels 08–14 were lowered on 2026-10-01 (user decision) to what a typical loadout reaches according to
`tools/balance.py` (≈ 60–80), instead of growing the economy. The Act 2 stat blocks have been rescaled
(2026-10-01): each Act 2 unit's HP was multiplied by new ÷ old reference DPS (70–130) at its first level
(e.g. L10 ×0.73, L14 ×0.62) and rounded, keeping the time-to-kill targets.

Levels 15–21 (Act 3, M18 = a of concept round 36) rise from 76 to 100 by 4 a level, what the typical
plan reaches with the Ion Beam (bought at L2 at the L18 visit and raised to L3 at L19, 77 · 77 · 77 · 80 ·
100 · 100 · 100 in the [balance plan](../player/balance-plan.yaml) on budget(n) income, the user's decision of
2026-10-14); the act HP factor of returning units uses them like Act 2's.

Levels 29–35 (Act 5; draft, judgement call **J2** of the gap analysis, approved in round 60) are proposed at **120 · 124 · 128 · 132 · 136 · 140 · 144**: Level 29 holds Level 28's 120, because in
vacuum the Harpoon Torpedoes idle and the Depth Charges go back in the inventory, leaving the plan's dry
loadout at ≈ 110 forward DPS (0.92 of the reference, too tight); the plan buys the **Ion Beam L4** at the L29 visit,
≈ 135 (the [generator hand plan](../player/generator/README.md): 1.13 of 120, then 1.09 to 0.96 as the reference climbs), and the curve then climbs by 4 a level to 144 at the Iron Sovereign.
The Act 5 unit drafts set their HP from these; M9 part A writes them into [data.yaml](data.yaml) with the
Act 5 balance plan and measures each level with `levelReport`. The beam-era ×2 keeps applying to `small`
units, the act HP factor to returning `medium` and larger ones (the Warden Tank 64 → ≈ 97 at L34, ×140/92),
and a hex [shield](#enemy-shields) comes on top of the HP.

**Early levels are deliberately easy** (user decision 2026-10-01): in L01–L03 a typical
loadout is about 1.5× stronger than the reference DPS above (`tools/balance.py`). This is
accepted as gentle onboarding; the L01–L03 stat blocks and the reference curve are not raised to
compensate.

Bosses assume an **effective DPS of 0.6 × reference** (accuracy, dodging, phase windows).

**Time-to-kill targets** at a unit's first level: `tiny` one hit · `small` ≤ 0.3 s · `medium`
0.4–1.5 s · `large` parts and heads 1–3 s · `huge` set pieces 20–40 s · mid-bosses 45–75 s ·
act bosses 90–180 s (matches [bosses](bosses/README.md)).

**Act HP factor** (user decision 2026-10-01, narrowed by D5 of M5 part A on 2026-10-06): in a
level from **Act 2 on** (Level 08 and later), a returning unit of tier **`medium` or larger**
(its `first_level` earlier than the level) gets
`HP × (reference DPS at that level ÷ reference DPS at its first level)`, so its time-to-kill stays
the same; no elite variants. **`tiny` and `small` units keep their HP**, so the dense levels'
popcorn stays easy (the campaign's "many small, easy units"), and Act 1's levels are unchanged.
**Exception, the Needler and the Stinger** (the user's Act 2 playtest, 2026-10-18: "the missiles take
out most enemies with a single shot"): the `small` units listed in the basis's `small_act_hp`
([data.yaml](data.yaml)) get the factor too, **capped at ×3**, in its listed levels, **Levels 08–09
and 11–22**: the Needler 4 → 12 HP (two Pulse Cannon bolts), the Stinger 6 → 14 at L08, 15 at L09, 17 at
L12 and 18 at L13 and L15–L18; the Skitters stay one-shot fodder, and the turrets, mortars and Act 2's own small
units keep their HP. Level 18 (M7 part E) joins with its waves built to the Act 2+ floor (the user's decision
of 2026-10-19), Levels 15, 16 and 17 with their reworks (steps PE15, PE16 and PE17). Level 10 and the rest of Act 3 keep 4 and 6: applied there, the autopilot's Level 10
escort fell to 2 of 4 shuttles home at medium (D2 = a asks for 3) and Level 11's hard runs stopped
completing (with the old waves). Levels 11–14 took it with their L10-style waves (steps WA and WB,
2026-10-19): without it the plan's fit kills the Needlers before their first shot (Level 12: 4 shots a
minute, Level 14: 3, the floor asks for 10). The
factor applies to every part's HP of a multi-part unit and every segment of a chain; it is applied
with the difficulty's HP lever, one rounding (half to even, at least 1). Bounties are unchanged (the
act factor already scales them). The time-to-kill check of `BalanceTest` covers these units at
every level they return in. Examples at Act 2 levels: Spore Bomber (L03) ×60/32 = ×1.88 at L08,
Mantis (L06) ×80/52 = ×1.54 at L14, Scuttler (L04) ×76/38 = ×2 at L13. The reference DPS curve is
extended act by act as levels are written.

**Beam-era HP** (the user's decision of 2026-10-19, "tougher, not more"; M7 part E, step E5): from the
level where the [balance plan](../player/balance-plan.yaml) first fits the Ion Beam, **Level 18**, every
**`small`** unit (bosses aside) takes **×2** on its HP on top of the act HP factor, its own first level included
(`beam_era_hp` in [data.yaml](data.yaml): `from_level`, `factor`, `tiers`); bounties are unchanged. The beam
burns the first target in its column every step from the moment it touches, so the plan's fit kills a small
unit far sooner than its single-target DPS against the reference says: on Level 18's first waves (≈ 90 units
a minute, Level 10's density) a killed small unit lived **0.9 s** on the screen, against **1.6 s** on Level 10
with its plan's fit (the user's reference, "as it should be"), and most died before they fired. Measured
there, ×1.5 gives 1.1 s, **×2 1.5 s** and ×2.5 1.9 s. The rule, checked by `BeamEraHpTest` on every mapped
level from Level 18 on: a killed small unit lives **0.9–1.5×** Level 10's time on the screen (medium, plan's
fit); Level 18 as built: 2.05 s (1.28×: the busier screen shares the beam). Examples at L18: the Needler 4 ×3
(capped) ×2 = 24 HP, the Stinger 36, the Ghost Drone 14 → 28, the Spine Turret 8 → 16. The `tiny` units stay
one-shot fodder (more HP would not slow a beam that kills a 1-HP unit in one step), and the `medium` and larger
ones keep the act factor alone: the Dust Devil's armoured vanes already gate its time to kill (doubled, the
autopilot killed none at hard), the Wraith's HP is set for the rear gun, which the beam does not replace, and
a bigger unit's time to kill depends less on the beam's instant reach (pending a measurement on the levels
that fly them). `BalanceTest` judges these units' time to kill at the reference DPS × the factor;
`tools/balance.py` prints them and fails when the plan's first Ion Beam moves off `from_level`. Levels 15–17 fly
the Pulse Cannon and take no beam-era factor (their small units lived 0.6–0.9 s: their P.e rework takes `small_act_hp`
like Act 2's: Level 15's in step PE15, Level 16's in step PE16 and Level 17's in step PE17); a later level gets the factor by its number, and a Needler or Stinger there keeps 24 / 36 HP only
when the level also joins `small_act_hp.levels` (else 8 / 12).

**Damage to the player** (shield first, overflow to armour; collisions split half/half — see
[shields](../player/shields/README.md)):

<!-- data: player-damage -->
| Class | Damage | Used for |
|---|---|---|
| `small` bullet | 4 | thorns, standard orbs, fan and ring bullets |
| `medium` bullet | 6 | large orbs, bursts from heavies, acid spit |
| `heavy` hit | 10 | mortar direct hits, slam arms, rail shots |
| `laser` | 8 per touch | `laser-sweep` / `laser-line` (once per sweep or line) |
| Contact | tiny 6 · small 10 · medium 15 · large 20 · huge 25 | rammers and bodies on the player's layer |
<!-- /data -->

**Ramming:** a `tiny` or `small` unit that touches the ship is destroyed by the impact, so its
contact damage lands once.

For scale: the starting ship (shield 20 + armour 60) survives about 20 small bullets.

**Bullet speed classes:** slow 90–120 px/s · standard 140–170 px/s · fast 190–260 px/s (the
play field is 540 px tall).

**Bounties** (Act 1 terms, medium; the act factor 1.6^(act−1) is applied automatically, see
[economy](../systems/economy/README.md)): `tiny` 2–5 · `small` 10–15 · `medium` 18–30 ·
hardened and `large` 40–60 · `huge` set pieces ≈ 15 % of their level's budget (sum of part
bounties). Mid-bosses and act bosses are planned as **absolute** medium credits in their level
(15 % / 30 % of that level's budget), but their **data holds Act 1 terms like every other
bounty**: the absolute amount ÷ the act factor, and the code multiplies every payout by the act
factor, bosses included; there is no exemption (M5 part A default, 2026-10-06). Since a level's
`bounty_scale` applies to every bounty, a boss's parts included, the amount written is the planned
paid amount ÷ (the act factor × the level's scale): the Harbour Kraken's ≈ 15 % of budget(11) ≈ 207
paid is written ≈ 207 ÷ (1.6 × Level 11's scale) (M5 part E default; the "300, written 188" of
2026-10-06 was planned before the scale existed and before budget(11) was 1 377). The conversion
is made when a boss's data is written (the Kraken in M5 part E, the Siege Spire in part H). Accepted exception (user
decision 2026-10-05): the [Coilwyrm](air/coilwyrm/README.md)'s parts total 86, above the `large`
class, as cutting a multi-part enemy up is extra work (a head-first kill pays 40). Check: level
01's worked budget (Skitter 5, Needler 12) is unchanged; a typical Act 1 level of 90–120 kills
earns 600–900 credits from kills plus ground targets and caches, inside the 1 000–1 500 budget.

### Difficulty scaling hooks

The global multipliers (enemy HP, fire rate, bullet speed, bullets per pattern, aiming,
formation size, bullet budget, credits) are defined once in
[difficulty](../systems/difficulty/README.md); every enemy gets them (scaled HP is rounded half to
even, at least 1). What an enemy's stat block
adds are **overrides**:

- Authored variants rather than multipliers: per-difficulty changes to the unit's patterns in its
  `difficulty:` block (a fan's bullets, a burst's shots, a boss attack's `count`, `burst`,
  `interval` or spiral `arms`); a level leaves a wave out on a difficulty with `skip` (see
  [difficulty](../systems/difficulty/README.md); per-bullet `medium+` / `hard-only` tags were
  dropped for these, user decision D3 of M4 part H).
- Hard-only elements: extra attack phases, `death-burst` on selected enemies.
- Opting out of a multiplier where it would break the enemy (e.g. a boss phase with a fixed
  bullet count).

### Target-the-objective hook

In `defend` and `escort` levels some units prefer the objective (a structure to defend or a
convoy unit, see [allies](../allies/README.md)) over the player. This is a per-level setting,
not part of a unit's stat block: the level document lists which units or waves get it and in
which mode. Units without it ignore the objective.

| Mode | Behaviour |
|---|---|
| `nearest` | Aimed attacks go at whichever is closer: the player or the nearest objective unit |
| `alternate` | Every second aimed shot goes at the objective while it is closer than the player |
| `in-arc` | Attacks the objective whenever it lies in the unit's facing arc, otherwise the player |
| `always` | The unit's attack goes at the objective: divers dive at it, mortars mark their impact on it, rammers ram it |

- **Choosing the target.** `nearest` is re-evaluated every simulation step (a turret's barrel
  turns toward the current choice), and an aimed shot picks its target as it is fired: the player
  or the nearest living objective unit, centre to centre. Only `aimed` and `burst` attacks follow
  the hook in `nearest` and `alternate`; a fan along a unit's facing is aimed at nobody.
- An objective-aimed attack uses the unit's normal pattern, look and damage class; whether and
  how much it hurts the objective is in the ally's spec. Shots aimed at the player never hurt an
  objective that only takes objective-aimed damage.
- Telegraphs still apply (mortar impact markers on the objective, edge warnings).
- **Homing missiles** (Act 5 draft, the Hornet in Level 31, decided in round 60): under `always` a
  missile homes on the objective picked at its launch (the nearest living objective unit); under the
  other modes it homes on the ship. The Decoy Flares still retarget it.
- Difficulty: a level may drop the hook on easy (e.g. divers go back to the player) or add
  units to it on hard.
- **Exact rules of the three modes (M5 part G, decision G1 = a, accepted in round 34 (2026-10-12); the
  bullets still hurt the ship, so the player can body-block).** `alternate`: each unit counts its
  attacks; every second one goes at the objective while it is closer than the player; a hard
  2-shot burst is one attack. `in-arc`: while the objective lies within ±45° of the unit's facing
  (its armour's arc), the unit's fan is centred on it and aimed at it, otherwise it fires as
  usual; a spit stays on the ship. `always`: a diver dives at the objective's centre, its fan is
  centred on it, and crossing the objective's footprint deals the ally's diver hit once (the
  diver then flies on and stays shootable); a mortar lobs at it (the marker on it, the direct hit
  `heavy` on the objective, the ring hurts only the ship). What each unit does to the relay is
  written once, in Level 13's
  [What hurts the relay](../campaign/act-2-homefront/level-13-polar-relay/README.md#what-hurts-the-relay-the-hook-modes-g1--a).
- **Who is hooked.** The level's `targeted_by` names kinds, never waves of popcorn: the small
  popcorn kinds (Skitter, Needler) and the Mantis are never hooked. In Level 13 the Stingers are
  hooked on medium and hard only (**easy: unhooked**, they go back to the ship).

### Enemy shields

**Draft** (Act 5 design phase; the gap analysis's **Q1 a**, approved in round 60). The
Ascendancy's doctrine puts light shields on its crewed craft. A shield is a **hex HP pool over the
hull**:

| Rule | Value |
|---|---|
| Hit | Every weapon hits it (no trait needed); a shot spends the shield first at its full damage and the **overflow** goes on to the hull (and the hull's own armour arcs), as the Choir Herald's aura ring does |
| Look | a thin gold hex outline round the hull at 40 % while it holds; a hit flashes the hexes where it lands; emptied, the hexes shatter in a 0.2 s ring of fragments; refilling, they shimmer in from the edge |
| Refill | after **2.0 s** without a hit it refills at **50 %** of its maximum per second (the shared numbers are `shields` in [data.yaml](data.yaml), a unit's HP its `shield`) |
| `shield-breaker` | a hit by a `shield-breaker` weapon (the Tesla Coil Pod, see the [weapons](../player/weapons/README.md)) **empties it at once** (the hit's whole damage goes to the hull) and stops the refill for **3 s** |
| EMP Burst | the [EMP Burst](../player/specials/README.md#emp-burst-l15) empties every shield on the screen and stops the refill for the stun (a machine's 3 s), as it strips the aura rings |
| Scaling | the shield's HP takes the difficulty's HP lever and, on a returning `medium` or larger unit, the act HP factor; never the beam-era factor |
| Who has one | Act 5 (light): the [Talon](air/talon/README.md) 12 (its leader 24; 2 and 4 until round 62), the [Hornet](air/hornet/README.md) 8, the [Gilded Gunship](air/gilded-gunship/README.md) 12, the Iron Sovereign's core (its own document); no other Act 5 unit. Heavier from Act 6, where `shield-breaker` is strongly rewarded (L37) |

Bosses follow the same rule unless their document says otherwise (a boss is never stunned, but its
shields are emptied by the EMP). Different from **hardened** ground units (only `anti-ground` and the
landing blasts hurt them) and from the Act 6 **`reflect`** shield (a frontal arc that bounces shots).

### Air links

**Draft** (Act 5 design phase, decided in the gap analysis, approved in round 60). The
`link` attack between **moving air units**, generalised from the Tendril Anchor's ground pairs; the
first unit is the [Shard Drone](space/shard-drone/README.md) (Level 31), whose document holds the
numbers (band, damage, tell, length):

- A `linked group` of 3–4 units is joined by **energy links** between neighbours: a line or a closed
  ring. A link is harmless during its flicker tell after the group enters, then hurts the ship on
  touch (with a cooldown, the ship not pushed).
- Links are **energy**: they have no HP and every shot passes through them, a `beam` included (unlike
  the tendril, which a beam burns). **Killing a unit breaks its links**; the **EMP Burst** drops every
  link for its stun, and they relight with the tell.
- Enemy bullets pass through links; links never hurt Rook or enemies, and no ally except an escort objective that the links are sent against (the L31 mining tug, Q4 a; see the [Shard Drone](space/shard-drone/README.md)), approved in round 60.

### Mine lines

**Draft** (Act 5 design phase; the gap analysis's **Q3 a**, approved in round 60). The
`mine` attack laid as a barrier; the [Minelayer](space/minelayer/README.md) (Level 30) holds the numbers
(spacing, gap, arming, proximity, ring, limits):

- A layer passes from behind (`rear-entry`, edge-warned) to a lane near the top and lays a **line**
  across the play field with **one gap** wide enough that flying its middle stays outside every mine's
  proximity. The line holds its place in the field and scrolls down onto the ship.
- A mine **arms** a moment after it is laid; an armed mine near the ship **blinks**, then bursts into a
  slow `ring`; touching one bursts it at once.
- A **shot** mine (1 HP) bursts at once and **sets off the mines within 44 px** (hop by hop, no
  further rings; mine lines come in clusters of 4, so a shot clears its cluster: C1 a, decided in round 60): one shot opens a hole a ship can use, so spread and piercing weapons cut lanes from
  far away. Homing shots and Rook never pick a mine.

### Topple

**Draft** (Act 5 design phase, decided in the gap analysis, approved in round 60). A
walker with **destroyable legs** falls when it loses one; the [Strider](ground/strider/README.md)
(Level 32) holds the numbers (tell, fall, line size):

- A **stagger** tell marks the **fall line** on the ground (a dark strip on the lost leg's side), then the
  walker **falls** along it, is destroyed (its whole bounty paid) and leaves a wreck.
- **Crush**: every **ground** unit under the fall line is destroyed (hardened ones included), its bounty
  paid to the player. It **never hurts the ship** or Rook (ground never collides) nor any `air` unit.

### Rock riding

**Draft** (Act 5 design phase, decided in the gap analysis, approved in round 60). Units
that ride the belt's drifting rocks (`ride` in the movement vocabulary); the
[Asteroid Mite](space/asteroid-mite/README.md) (Level 29) holds the numbers:

- A small, breakable `Debris` rock carries **1–5** riders (2–5 until round 62), the rocks drifting in in groups
  of 3–5; they move and turn with it and are hittable
  like the rock, which blocks shots as the debris field does.
- When the ship comes within **160 px** the riders **leap off** one after another, each after a
  **0.4 s** tell (a `dive` at the ship).
- **Shooting the rock** frees its riders early: they tumble off and leap after the same tell. Riders
  carried off the screen on an untouched rock pay nothing.

## Concept art

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

Concept [round 03](../concept-rounds/round-03/README.md) gives the enemies their first visuals: the Act 1 Vrell set in [air](air/README.md) and [ground](ground/README.md), three Ascendancy units for faction contrast and the Act 1 boss in [bosses](bosses/README.md). Two Vrell design languages are proposed: **A "Sleek chitin"** (smooth, elongated, glossy violet chitin with thin glowing teal seams, pink eye as weak point) and **B "Armoured brood"** (bulky segmented carapace plates, claws and spikes, glow only between plates and in eye clusters). The key Act 1 enemies are shown in both; the others in A. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/lineup-r03-a.png](concept/lineup-r03-a.png) | All round-03 enemies at native scale next to the player on four backgrounds, then at 2× with names, plus the Brood Carrier at 1/4 scale — size and readability check | superseded by the r04 lineup |

Concept [round 04](../concept-rounds/round-04/README.md) — colour pass on the chosen enemies with the [role colours](#role-colours) (chitin base = role family, glow = kind of threat; Ascendancy black & gold with a per-unit accent and a thin gold/red rim light). Same models and sheet layout; generator `tools/concept/enemies_r04.py`.

| File | What | Status |
|---|---|---|
| [concept/lineup-r04-a.png](concept/lineup-r04-a.png) | Round-04 lineup: all chosen enemies in their role colours at native scale on four backgrounds, 2× with names, the Brood Carrier at 1/4 scale, and the role-colour legend | chosen |

Concept [round 05](../concept-rounds/round-05/README.md) — size lineup from the tiniest Mote to the Leviathan; generator `tools/concept/enemies_r05.py`.

| File | What | Status |
|---|---|---|
| [concept/size-lineup-r05-a.png](concept/size-lineup-r05-a.png) | Every round-04 and round-05 unit plus the player at 1×, sorted by area; Coilwyrm, Leviathan and Brood Carrier below | superseded by r05-b |
| [concept/size-lineup-r05-b.png](concept/size-lineup-r05-b.png) | Size lineup with the Ravager, Shellback and its curled ball added | chosen |

Concept [round 06](../concept-rounds/round-06/README.md) — lineup of the round-06 units; generator `tools/concept/enemies_r06.py`.

| File | What | Status |
|---|---|---|
| [concept/lineup-r06-a.png](concept/lineup-r06-a.png) | The 11 round-06 units at 1× next to the player, Skitter, Scuttler and Ravager; Threadcrawler chain and assembled Halo Platform below | chosen |

Concept [round 09](../concept-rounds/round-09/README.md) — generator `tools/concept/vfx_r09.py`.

| File | What | Status |
|---|---|---|
| [concept/enemy-bullets-r09-a.png](concept/enemy-bullets-r09-a.png) | Enemy bullet set: 9 types in Vrell and Ascendancy colours with a readability test over the chosen scenes | chosen |

Production art for concept round 12 (the Level 01 batch; part P2 opens the round), review files built from the final frames in `assets/` by `tools/art/enemy_bullets.py` (`--review` rebuilds only them); prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/enemy-bullets-final-r12-a.png](concept/enemy-bullets-final-r12-a.png) | Final Vrell `small` bullets: the orb (15×15, 4-frame core pulse) and the needle (23×23, 16 headings clockwise from straight down; a gold deep band and a tight 2/3 halo instead of the faint olive one) | chosen |
| [concept/enemy-bullets-final-r12-a.gif](concept/enemy-bullets-final-r12-a.gif) | Orbs and needles drifting over a dark blue field | chosen |
| [concept/enemy-bullets-final-r53-a.png](concept/enemy-bullets-final-r53-a.png) | The Barbfin's violet needle `needle-violet` (16 headings; `tools/art/enemy_bullets.py violet`) next to the yellow needle and the orb (sheet) | chosen (round 53, F.3: accepted as final) |
| [concept/enemy-bullets-final-r53-a.gif](concept/enemy-bullets-final-r53-a.gif) | Violet needles and orbs drifting over Level 24's graded plain, 3x | chosen (round 53, F.3: accepted as final) |

Production art for concept round 26 (the M4 part H batch), review files built from the final frames in `assets/` by `tools/art/bullet_medium.py` (`--review` rebuilds only them); prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/enemy-bullet-medium-final-r26-a.png](concept/enemy-bullet-medium-final-r26-a.png) | Final Vrell `medium` bullet: the large pulsing orb (`orb-medium_0..5`, 23×23, 13 px body, 6-frame pulse; round 09's large orb in the small orb's colours) next to the small orb | chosen |
| [concept/enemy-bullet-medium-final-r26-a.gif](concept/enemy-bullet-medium-final-r26-a.gif) | Large and small orbs drifting over a dark blue field, the large ones slower | chosen |

Concept round 60 (Act 5 design phase, step ART-P-units) — the [art direction](../art-direction/README.md)'s **homing diamond**, a/b, the look of every Ascendancy homing missile of Act 5 (pending the user's review); generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md#round-60--the-homing-diamond-act-5). The units' own round-60 proposals are listed in their documents.

| File | What | Status |
|---|---|---|
| [concept/homing-diamond-r60-a.png](concept/homing-diamond-r60-a.png) | Homing diamond A, red diamond, gold heart — a broad crimson-red diamond with a dark rim, a gold inner diamond, a white-hot core and a short thin pale-gold exhaust (sheet) | chosen (round 60) |
| [concept/homing-diamond-r60-a.gif](concept/homing-diamond-r60-a.gif) | Homing diamond A, red diamond, gold heart — a broad crimson-red diamond with a dark rim, a gold inner diamond, a white-hot core and a short thin pale-gold exhaust (motion) | chosen (round 60) |
| [concept/enemy-shield-final-r61-a.png](concept/enemy-shield-final-r61-a.png) | Enemy hex shield, proposal **a** "hex lattice" (`tools/art/act5_gear.py shield`, M9 part A): the round bubble per size tier (52, 92, 116 px) with a thin gold hex lattice at 40 % and a brighter rim, the shimmer loop, the refill in from the edge, the 0.2 s shatter ring, the hit's lit hexes; over round 60's Talon, Hornet and Gunship (sheet) | chosen (round 61, F.2 a picked: the hex lattice, not the recommendation; in production from round 64: concept/enemy-shield-final-r64-a.png) |
| [concept/enemy-shield-final-r61-a.gif](concept/enemy-shield-final-r61-a.gif) | a over the three stand-ins: hits, emptied, 2 s quiet, the refill, 2x | chosen (round 61, F.2 a picked: the hex lattice, not the recommendation; in production from round 64: concept/enemy-shield-final-r64-a.png) |
| [concept/homing-diamond-final-r61-a.png](concept/homing-diamond-final-r61-a.png) | Homing diamond final frames (`tools/art/act5_gear.py diamond`, round 60 A.16 a): the diamond at 16 headings, its pale-gold exhaust (2 flicker frames), the shot-down pop and the end-of-life fizzle (sheet) | chosen (round 61, F.6: accepted) |
| [concept/homing-diamond-final-r61-a.gif](concept/homing-diamond-final-r61-a.gif) | Four diamonds homing on the ship over belt space, one shot down, one fizzling, 2x | chosen (round 61, F.6: accepted) |
| [concept/enemy-shield-capture-r61-a.png](concept/enemy-shield-capture-r61-a.png) | Game capture on the **capture rig** (step A4: Level 08 in a scratch copy with Act 5 keys, see the prompts; nothing of it is in the repository): the Spore Bombers' shields (b, the gold film, in the game), a Stinger column shielded, a Tesla arc on a Spore Bomber whose shield is up (its two neighbours' already broken), an arc on a shielded Stinger | chosen (round 61, F.2 a picked; the capture shows b, the game's film until round 64) |
| [concept/enemy-shield-capture-r61-a.mp4](concept/enemy-shield-capture-r61-a.mp4) | The rig with the Tesla pods and the game's sound (rig seconds ≈ 28.5–48.5): shields hit and broken by the arcs, the shatter and refill sounds, the vacuum bursts and wrecks | chosen (round 61, F.2 a picked; the capture shows b, the game's film until round 64) |
| [concept/homing-diamond-capture-r61-a.png](concept/homing-diamond-capture-r61-a.png) | Game capture on the rig: the Needlers (stand-ins) launching diamonds down that turn onto the ship, two frames and two 2× zooms of the diamonds and their exhaust | chosen (round 61, F.6: accepted) |
| [concept/belt-rig-capture-r61-a.mp4](concept/belt-rig-capture-r61-a.mp4) | The rig with the default fit and the game's sound (rig seconds ≈ 23.5–65.5): the shields, the diamonds, Vorne's intercepted line (t = 26), the drain from t = 30 (`POWER DRAIN 6/8`, `4/8`, `REGEN OFF`), the Spine Turrets' laser lines (t ≈ 49–53), the vacuum kills, `AIRSTRIKE IN ROCK` | chosen (round 61, F.2-F.6, K.1, C.1-C.8: accepted) |

Production art, round 64 (step ART-F): round 61's pick F.2 a, the hex lattice, in `assets/sprites/enemy-shield-*` (the
same keys and frame layout; `tools/art/act5_gear.py shield`), the lattice fading toward the middle so the unit stays
clear. Prompts: [concept/prompts.md](concept/prompts.md#enemy-shield-final-r64).

| File | What | Status |
|---|---|---|
| [concept/enemy-shield-final-r64-a.png](concept/enemy-shield-final-r64-a.png) | Enemy hex shield final frames (a, the hex lattice): the three tiers' holding loop, refill and break, the hit's lit hexes; the lattice fades from 38 % at the rim to 12 % over the unit; over the production Talon (shield 12), Hornet and Gilded Gunship at 1× and 2× (sheet) | proposed (round 64) |
| [concept/enemy-shield-final-r64-a.gif](concept/enemy-shield-final-r64-a.gif) | The three units shielded: hits, emptied, 2 s quiet, the refill, 2× | proposed (round 64) |
| [concept/enemy-shield-capture-r64-a.png](concept/enemy-shield-capture-r64-a.png) | Game captures of the production lattice (step D5; [recipe](concept/prompts.md#enemy-shield-capture-r64-a)): Level 29's first Talons under the beam at 2× (the bubble, lit hexes on a hit, the shatter ring) and a Level 30 convoy Gunship with its escort Talons at 2× | proposed (round 64) |

## Implementation

- [x] Rescale the Act 2 unit and boss HP to the lowered reference DPS for L08–L14 (balancing basis)
- [x] Data-driven enemy definitions using the stat block fields.
- [x] Movement patterns from the vocabulary implemented as reusable behaviours: those the Act 1
      units fly (their stat blocks and Levels 01–07; `EnemySpec`, `FlightPath`, `Formations`).
- [x] The Act 2 units' movement patterns with the first unit that flies each (M5 parts B–H): the
      Creeper's crawl, the Ravager's pounce, the Wraith's cloak, loop and rear entry, the Mote
      Swarm's flock, the Driftjelly's and the rafts' drift, the Kraken's surfacing, the Lamprey's
      chase and latch, the Skimmer's lanes, the Spire's lift (`CreeperTest`, `RavagerPounceTest`,
      `WraithTest`, `FlockTest`, `DriftjellyTest`, `ReefSpitterTest`, `SlamArenaTest`,
      `LampreyTest`, `SkimmerTest`, `SpireLiftTest`).
- [ ] The other movement patterns with the first unit that flies each — **later: Act 5** to Act 7
      (e.g. `burrow`, `teleport`, `mirror`).
- [x] Attack patterns from the vocabulary implemented as reusable emitters: those the Act 1 units
      and bosses use (`aimed`, `burst`, `fan`, `ring`, `spiral`, `laser-sweep`, `mine`, `mortar`,
      `spawn`, `death-burst`, `none`; `EnemyGun`, `Lob`, `Mine`).
- [x] The Act 2 units' attack patterns with the first unit that uses each (M5 parts C–H): the Hive
      Node's periodic `spawn`, the Driftjelly's proximity `ring`, the Kraken's lane slams, the
      Lamprey's `drain`, the Siege Spire's `lob`, `sweep` and maw launches (`HiveNodeTest`,
      `DriftjellyTest`, `SlamArenaTest`, `LampreyTest`, `BossLobTest`, `BossSweepTest`,
      `MawLaunchTest`).
- [ ] The other attack patterns with the first unit that uses each — **later: Act 5** to Act 7
      (e.g. `laser-line`, `link`, `aura`, `reflect`).
- [x] Formation spawner that places enemies by formation name and entry edge (`Formations`): the
      Act 1 formations (single, V-wing, line abreast, column, snake, stream, pincer, circle,
      whirl cluster, carrier + escorts, convoy, the walkers' paths and the chains' paths).
- [x] The Act 2 formations with the first level that uses each (M5 parts C–G): `pack` (L09),
      `swarm` and `rear ambush` (L10), `field` (L11), `cross` (L13) (`PackFormationTest`,
      `PartDFormationsTest`, `DriftjellyTest`, `CrossTest`).
- [ ] The other formations with the first level that uses each — **later: Act 5** to Act 7
      (`submerged ambush` — **later: Act 5**: no Act 4 level uses it as built; the Abyss Ray rises from `sub` with its
      own escorts).
- [x] M5 part E (Level 11): the `sub` layer and its hit rules (E2 = a: only `anti-sub` and the
      Smart Bomb; no contact; homing shots, turrets, landing blasts, mines, the Airstrike and Rook
      skipping it) — step E2a (`SubLayerTest`).
- [x] M5 part E (Level 11): a unit's current layer flipping between `sub` and `ground` as it
      surfaces and submerges (a seeded per-unit timer on the real steps), the `field` formation
      (scattered units with the sea and a current), a ground unit's drift on the water (the Reef
      Spitter's raft), the `ring` fired by proximity (the Driftjelly), the anchored arena boss with
      its lanes and slams (the Harbour Kraken) — steps E2b–E2c (`DriftjellyTest`, `KrakenTest`);
      built in step E2b: the swap, `field`, the raft's drift and the proximity ring
      (`DriftjellyTest`, `ReefSpitterTest`); in step E2c the arena boss: anchored, its parts' own
      layers, its slam lanes, surfacing and weak spots (`SlamArenaTest`, `KrakenTest`).
- [x] M5 part D (Level 10): the `swarm` formation with the `flock` movement (a leader point on an
      authored route, boids round it, loop-backs (not edge-warned since 2026-10-21); the Mote Swarm),
      the `rear ambush` formation (2–4 across the bottom edge; a cloaked unit enters at the top and
      re-enters behind the ship, not edge-warned since 2026-10-21; the Wraith), a cloak (`high-air` until a decloak, then `air`,
      the per-unit current layer of part C) and authored `paths` on a snake (`FlockTest`,
      `PartDFormationsTest`, `WraithTest`).
- [x] M5 part C (Level 09): the `pack` formation (one path per walker, 0.25 s apart), hardened
      enemies (`armour: hardened` flown: the glance, homing shots and Rook skipping them), a
      periodic `spawn` on a ground unit (the Hive Node), a per-unit current layer (the Ravager's
      pounce, `air` for its middle 0.3 s) read at every hit, contact and targeting site
      (`PackFormationTest`, `HardenedEnemyTest`, `HiveNodeTest`, `RavagerPounceTest`).
- [x] Layer rules for hit detection and collision on `ground`, `low-air`, `air` and `high-air`: what each weapon delivery reaches, hardened ground targets, contact on the player's layer.
- [x] The `sub` layer — M5 part E (Level 11, step E2a; appended to the layers so the existing
      ordinals and replay hashes stay; its hit rules in `SubLayerTest`).
- [x] The `space` and `deep` layers: `space` is a document alias of `air` (a level's vacuum presentation, no clouds or
      weather; no new layer value, so the ordinals and replay hashes stay) and `deep` is the background, built (the gap
      analysis's trivia, 2026-10-27, shown in round 60).
- [x] Bullet rendering order, telegraphs, edge warnings and the bullet budget (bullets above every
      layer, the `laser-sweep` wedge and the `mortar` marker, `EdgeWarnings`, `Rules.bulletBudget`).
- [x] Global difficulty multipliers with per-enemy overrides (the stat blocks' `difficulty:` hooks,
      `EnemyData.Hook`).
- [x] The act HP factor for returning `medium` and larger units from Act 2 on, with its time-to-kill
      check in `BalanceTest` — M5 part A (`SimSpecs.actHpFactor`, applied by `SimSpecs.level` to
      waves, ground units, broods, set pieces and a boss's parts and launched units; tests
      `ActHpFactorTest`, `BalanceTest.everyReturningUnitKeepsItsTimeToKillInActTwo`;
      `tools/balance.py` prints the returning units' HP and time to kill at Levels 08–14)
- [x] The beam-era HP factor (M7 part E, step E5: `beam_era_hp`, ×2 on `small` units from Level 18, where the
      plan fits the Ion Beam; `SimSpecs.actHpFactor`, the validator's check, `ActHpFactorTest`,
      `BeamEraHpTest` (a killed small unit's time on the screen 0.9–1.5× Level 10's), `BalanceTest`'s time to
      kill at the reference × the factor, `tools/balance.py`'s beam-era table and plan check)
- [x] Target-the-objective hook: per-level unit/wave configuration, mode `nearest` (Level 04's
      convoy; `LevelScript`, `EnemyBullet`).
- [x] The modes `alternate`, `in-arc` and `always` (M5 part G, Level 13, the first level that uses
      them): the exact rules above, the per-kind and per-difficulty `targeted_by` and the `cross`
      formation (`HookModesTest`, `CrossTest`, `Level13Test`).
- [x] Root bursts, the boss `lob` and `sweep` and the Wraith's maw launches (M5 part H; `RootBurstTest`,
      `BossLobTest`, `BossSweepTest`, `MawLaunchTest`, `Level14Test`).
- [x] Multi-part enemies: segment chains following the head's path history, articulated parts,
      per-part HP and destroyable/armoured/vital parts, chain splitting (the Coilwyrm's `Chain`, the
      Gorgon Frigate's necks, the Leviathan's and the Brood Carrier's parts).
- [ ] A machine chain's split (the rear half stops, drifts 1.5 s harmless and explodes paying its cars) — **later: Act 5**
      (the [Rail Serpent](air/rail-serpent/README.md), M9 part C).
- [ ] Enemy shields (the hex HP pool, overflow, refill, `shield-breaker`, the EMP) — **later: Act 5** (M9 part A, flown by
      the Talon in part B). (Step A1: the simulation's `EnemyShield`, `EnemyShieldTest`; step A2: a unit's `shield` and
      the shared `shields` in [data.yaml](data.yaml), scaled by the difficulty's HP lever and the act HP factor, never
      the beam era's, `BeltRules`, `BeltChecks`, `BeltKeysTest`; step A3: the hex look `EnemyShieldLooks`, the bubble by
      size tier fading with the pool, the hit on the rim toward the ship, the break and the refill's shimmer, and
      their cues (`BeltCues`; the art in the Act 5 levels' unit atlases, `SpriteUse`), `ActFiveLooksTest`,
      `ActFiveSoundsTest`.)
- [ ] Air links between moving units, mine lines (proximity, chain blasts), topple and crush, rock riding, ricochet off
      rocks — **later: Act 5**, each with the first unit that flies it (the Shard Drone, the Minelayer, the Strider, the
      Asteroid Mite, the Buzzsaw Drone; M9 parts B–E).
- [x] Angle-set sprites (16/32 angles, nearest frame) and radial spinners (16 for the Skitter and
      the needle, 32 for turrets, 48 for the Coilwyrm; the Whirl Seed spinning; `EnemyLooks`).
- [x] Act 1 passes the variety checklist (as built: [act 1](../campaign/act-1-first-contact/README.md#enemy-use-per-level)).
- [x] Act 2 passes the variety checklist as built (M5 part I, 2026-10-12, from Levels 08–14's
      data: the table's row above; tiny, small, medium and huge, no `large` unit).
- [x] Act 3 passes the variety checklist as built (M7 part I, 2026-10-20, from Levels 15–21's data: the table's row above;
      8 of 22 units insectoid).
- [x] Act 4 passes the variety checklist as built (M8 part I, 2026-10-26, from Levels 22–28's data: the table's row above;
      3 of 25 units insectoid).
- [ ] Acts 5–7 pass it as built — **later: Act 5** to Act 7.

## Open questions

Act 5's design questions for round 60 that touch the shared rules (the drafts follow the recommended
option; pending the user's review):

- **Q1 enemy shields**: **a** (drafted) a light hex HP pool every weapon hits, `shield-breaker` empties
  it; **b** shields block everything but `shield-breaker`, beams and the EMP (forces the Tesla Coil Pod
  in Act 5, against the L37 pacing); **c** no shield mechanic in Act 5 (sparks only), shields from Act 6.
- **Q3 minefields**: **a** (drafted) lines with one gap, proximity ring and chain blasts; **b** static
  contact mines as walls, no burst; **c** seeker mines that drift toward the ship.
- **Q4 the mining tug escort** (Level 31; the hook's `always` on homing missiles above): **a** (drafted
  by the level step) a huge tug with three sections that units target; **b** three ore haulers as a
  convoy; **c** a tug on the play plane that blocks fire.
- **Q6 popcorn in a human war**: **b** (drafted) the new [Dart Drone](air/dart-drone/README.md) launched
  in streams; **a** the Vrell alone carry the count; **c** Talons in great numbers.
- **Q11 the Ascendancy look on the belt**: decided in round 60, **b** (not the recommendation): gunmetal/charcoal
  hulls with gold (see *Factions*); rejected: a gloss black (the draft), c black silhouettes with gold accents only.

## Decisions

Entries dated before 2026-10-21 (M8's start): [decisions-archive.md](decisions-archive.md).

- 2026-10-21 (user, after playthroughs): "It looks like for the wraiths this warning is shown. The
  wraith popping up from behind is part of its normal flight pattern, and as such, the warning is
  confusing. Also, when the wraith is shot down while going down, you get a warning for nothing.
  When a re-entry from left, rear or right is part of the normal flight pattern, do not show the
  warning. The walker also seem to show warnings. This takes away the surprise effect that should
  go with a boss fight. Bosses must never trigger these warnings." The readability rule now warns
  only a wave's first entry from a side or the rear; re-entries (the Wraith, the Coilwyrm's and
  the Mote Swarm's loop-backs) and bosses feed no edge warning (see [HUD](../ui/hud/README.md)).
- 2026-10-22 (M8 part A, step A2): `reference_dps` for Levels 22–28 (96 → 120, the generator's hand plan of round 48)
  in [data.yaml](data.yaml), so the act HP factor reaches Act 4; parts B–H re-measure each level with `levelReport`.
- 2026-10-22 (M8 part B, step B3): `small_act_hp.levels` extended to Level 22, as its approved README has it (the
  Needler and the Stinger take the factor capped at ×3, 12 and 18 HP, doubled by the beam era to 24 and 36).
- 2026-10-27 (step m9-d-enemies-a, Act 5 design phase; approved in round 60): the shared Act 5 rules
  drafted from the gap analysis's recommended answers: [enemy shields](#enemy-shields) (Q1 a), [air links](#air-links),
  [mine lines](#mine-lines) (Q3 a), [topple](#topple), [rock riding](#rock-riding), the machine split's 1.5 s drift, the
  `ride` movement and the homing diamond; the Act 5 variety row with the Crawler Tank dropped for the Warden Tank in
  livery (judgement call **J1**) and the Dart Drone added (Q6 b); the balancing-basis note for `reference_dps` 29–35
  (judgement call **J2**: 120 → 144, the Ion Beam L4 at the L29 visit); `space` closed as a document alias of `air`.
- 2026-10-27 (user, [round 60](../concept-rounds/round-60/README.md) closed): the Act 5 enemy design approved as drafted (D.10–D.12 with the units): Q1 **a** (enemy shields: a hex HP pool that regrows 2 s after the last hit, emptied at once and held back 3 s by a `shield-breaker`), Q3 **a** (mine lines, J.5 C1 a and C2 a), Q6 **b** (the Dart Drone), J.1 (the Warden Tank in livery), J.2 (reference DPS 120 to 144); **Q11 b**, the Ascendancy's Act 5 look is gunmetal/charcoal with gold, not gloss black (see *Factions*); **A.16 a** the homing diamond (b rejected); the shield sounds are S.1 a, S.2 b, S.3 a, the homing sounds S.4 b, S.5 a, S.6 b. **QA-1** (decided 2026-10-27 by the main agent, confirmed in round 61 on 2026-10-28): homing shots, Rook and the Tesla arc lock onto homing diamonds only when no enemy unit is in reach.
- 2026-10-28 (user, [round 61](../concept-rounds/round-61/README.md), closed): **F.2 a**, the hex lattice, not the recommendation (the game's gold film **b** is rejected; the swap of the shield frames to a is a later art step, **a to production in ART-F**, the game draws b until then); F.6 the homing diamond accepted; **C.1** (QA-1: homing shots, Rook and the Tesla arc lock onto a diamond only when no unit is in reach) confirmed; **C.3** the breaker's 3 s hold and **C.8** the refill sound when the shield is full again accepted.
- 2026-10-28 (M9, step ART-F; review in round 64): the enemy hex shield's production frames are now round 61's **a**,
  the hex lattice (`act5_gear.py shield`, same keys, sizes and frame counts: no game change). Round 60's worry that the
  lattice reads heavy over the Gilded Gunship: the lattice now fades toward the bubble's middle (12 % over the unit, 38 %
  at the rim, round 61's sheet drew it evenly at 40 %; `shield --r61` rebuilds that sheet).
