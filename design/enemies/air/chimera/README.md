---
title: Chimera
design: approved
implementation: done
art: chosen
depends-on: [../talon, ../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../art-direction, ../../../campaign/act-6-jovian-storm]
updated: 2026-10-31
---

# Chimera

## Summary

The first hybrid (Level 36): a Talon airframe in gunmetal with Vrell tissue grown through its seams. It flies the
Talon's swoops and aimed pairs in V-wings and flocking swarms, and it **regenerates**: a second without a hit and
its HP climbs back at 20 % a second, the seams glowing brighter as it heals. Burst damage and a held beam kill it;
scattered hits do not.

## Design

### Stat block

Rendered from [data.yaml](data.yaml) (M10 part B, step B2, from the approved table of round 72; numbers at
**medium**, see the [balancing basis](../../README.md#balancing-basis)). HP is in damage units (a Pulse Cannon L1 shot
does 2), set at its first level from the Act 6 reference DPS (J1, L36 = 144). As a `small` unit it takes the
[beam-era](../../README.md#balancing-basis) ×2 on its HP and keeps it in every later level (no act HP factor). Fire
rates are first values: the build tunes them per level with `levelReport`.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Hybrid (machine: the EMP Burst stuns it longest) |
| Layer | `air` |
| Size tier | `small` |
| Size | 40×36 px, hitbox 28×24 (the [Talon](../talon/README.md)'s airframe) |
| Parts | single |
| Orientation | `16 angles` (it banks through its swoop) |
| HP | 20 (easy 15 / hard 26, from the global multipliers) |
| Armour / shield | none (no hex shield: its tissue is its defence); **[regeneration](../../README.md#regeneration) 20 % of its maximum a second** (8 HP/s flown) after **1 s** without a hit. HP 20 base flies at **40** with the beam era's ×2: 0.28 s at the L36 reference of 144 |
| Speed | 220 px/s through the swoop; 160 px/s in a swarm |
| Movement | `swoop` as the Talon's (enters at the top 280 px across its lane on a curve, bottoms out 200–260 px below the top edge, leaves up through the top); in a `swarm` it flocks round the wave's leader point along an authored route, as the [Mote Swarm](../mote-swarm/README.md) does. **No squad leader**: a Chimera wing never scatters |
| Attack | `aimed` **pair** (two shots 4° apart, both at the ship) every 1.6 s while within 300 px of the top edge, as the Talon's; standard 160 px/s, `small` = 4; the Talon's 0.25 s red glint on the twin muzzles is the tell. In a swarm at most 3 members fire at once, staggered 0.3 s. Contact `small` = 10 (destroyed by the impact) |
| Formations | V-wing (5–7), swarm (6–10), line abreast (2–2) (the intro pair, Level 36's t=34, in open sky); never more than two wings, or one swarm and one wing, on the screen |
| Weak points | the tissue seams (drawn only; they glow brighter as it heals) |
| Effective traits | `beam`, `homing`, `forward`, `spread` |
| Credits | 14 (score 140 × chain), paid once whatever it healed |
| Death | `small` explosion with gold sparks and a spray of teal tissue flecks (presentation, no bullets) |
| First level / used in | L36; its later Act 6 levels per the level drafts; the Ascendant's phase 2 spawns them (its own document) |
| Difficulty hooks | easy: regeneration 15 %/s after 1.3 s, pair every 2.0 s; hard: 25 %/s after 0.8 s, pair every 1.3 s |
<!-- /data -->

### Behaviour

- **Regeneration** follows the shared [rule](../../README.md#regeneration): any hit restarts the 1.0 s delay, a
  beam's continuous touch counts as a hit every step, healed HP never pays. A Pulse Cannon's steady stream keeps it
  from healing; the Micro-missile Pods' salvos, a passing spread or Rook's odd shot leave gaps it heals in. The
  lesson of Level 36: **finish what you start** (focus one Chimera, then the next), or hold the beam.
- **The seams** (the hybrid livery of [Q9](../../README.md#open-questions) a, the look **b "overgrown"** chosen in round 72
  A.1): the Talon's gunmetal plates split by seams twice as wide as a thin crack and veined, with tissue nodules bulging
  at the wing roots, the spine and the panels (a lumpier outline that tells it from the Talon at a glance; gold 28 %, teal
  6 % at full health, 11 % healing), the dark teal tissue glowing `00FF9A` in the seams. Healing, a teal pulse runs along them twice a second and
  they brighten the closer it is to full; a Chimera at full health glows faintly, one just hit shows dark seams.
  The [Targeting computer](../../../player/systems/README.md#targeting-computer-user-decision-d6-of-m5-part-a)'s HP
  bar grows back with a teal edge.
- **Under the cloud banks** (Level 36, Q2 a, the [Jovian world](../../../world/jovian/README.md)'s rule): a bank
  drawn over the `air` layer shows a Chimera beneath it at about 35 %, with its seams and engine glow; homing shots
  and the Targeting computer track it as usual. The banks do not stop hits, so they do not let it heal.
- **Mixed waves**: Chimera wings fly beside the Talons the player knows from Act 5, the same silhouette with a
  different rule; a Talon wing's leader scatters only Talons.
- **Sounds** (brief for the audio step): the Talon's turn-in whine with a lower, rougher edge; its dry twin crack
  for the pair; while it heals a soft rising creak (stretched leather or rope from Freesound, CC0/CC-BY), never a
  bubbly or wet sound; on death the Talon's crack with a short tearing sound under it.

### Difficulty and level use

- At most two Chimera groups on the screen; the regeneration is meant to be read one unit at a time.
- Its life on the screen under the plan's fit is measured with `levelReport` at L36 against Level 10's band
  (the beam-era rule, 0.9–1.5×); the regeneration rate is the lever if it lives too long.

## Concept art

Round 72's proposals (step ART-R-A, picked 2026-10-31; generator `tools/concept/enemies_r72.py chimera`); round 74's
production frames (step ART-B, pending the user's review: the seams as one additive layer the game draws at the state's
strength, the pulse as a second while it heals). Each sheet holds the source render, the sprites at play size with their states, the readability check over Level 36's brightest and darkest band beside the player with the measured gold and teal shares, and key frames; each GIF is the 480×540 play field. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/chimera-r72-b.png](concept/chimera-r72-b.png) | Chimera b, "overgrown": the seams twice as wide and veined, tissue nodules at the wing roots, the spine and the wing panels; the same states | chosen (round 72 A.1 b) |
| [concept/chimera-r72-b.gif](concept/chimera-r72-b.gif) | The same scene as a | chosen (round 72 A.1 b) |
| [concept/chimera-final-r74-a.png](concept/chimera-final-r74-a.png) | Final frames (M10 part B, `tools/art/jovian_units.py chimera`; held in `build/art-held/sprites/` until its data.yaml): the 16 headings at 2× with the seams at full health, the seams by state (just hit 0.13, full 0.45, healing 50 % and 95 %) and the healing pulse's six frames, beside the Act 5 Talon, the death's flecks over the small explosion; teal 6 % at full health, 13 % healing near full (sheet) | proposed (round 74) |
| [concept/chimera-final-r74-a.gif](concept/chimera-final-r74-a.gif) | A V of five swooping over Level 36's bands: one hit and healing with the pulse, one held under fire until it dies | proposed (round 74) |
| [concept/chimera-capture-r74-a.png](concept/chimera-capture-r74-a.png) | Game capture (Level 36, step B5): the t=120 swarm leaving its bank and flocking under the ship's fire (1×; the ship steered under it), members hit, healing (the seams lit) and dying | proposed (round 74) |
| [concept/chimera-capture-r74-a.gif](concept/chimera-capture-r74-a.gif) | t=120–123.4 at 20 frames a second, the play field at 1× | proposed (round 74) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [x] Stat block loaded from a `data.yaml`; global difficulty multipliers and the beam-era factor applied; re-measured with `levelReport` at L36 (M10 part B, step B2: 1.81 s on the screen at medium under the plan's fit, 1.13× Level 10's small units' 1.60 s)
- [x] Regeneration per the shared [rule](../../README.md#regeneration) (rate, delay, any hit and a beam's touch restart it, the EMP pauses it), deterministic and allocation-free (M10 part A, 2026-10-31: `RegenerationTest`, the seam pulse step A3a's)
- [x] The Talon's swoop and pair reused; the `swarm` flock reused from the Mote Swarm with a cap on simultaneous fire (steps B1 and B2: the flock's `fire_cap` 3 in a `stagger` of 0.3 s; a swarm wave casts the unit from its swoop path to its flock)
- [x] Production sprites (16 headings, the seam glow per HP share, the healing pulse) via `tools/art/`; death effect, bounty and sounds (Freesound, CC0/CC-BY) (step B4: `tools/art/jovian_units.py`'s sets drawn by the game's `CloudDiverLooks`: the seams at `RegenerationLooks.seamGlow`'s strength, the healing pulse, the death's flecks, the Talon's glint on its own muzzles; round 74's death a, its whine and twin crack on the Talon's files until QB-2)

## Open questions

- **Regeneration numbers**: the roster's 20 % a second after 1.0 s are kept; the build measures it and may slow the heal
  if Level 36 feels like a wall for non-beam fits.

## Decisions

- 2026-10-31 (step m10-d-enemies-a): drafted for the Act 6 design phase from the roster row and the M10 gap
  analysis (§2.2: HP ≈ the Talon's 32 × 1.25 = 40 with the beam era, regeneration 20 %/s after 1.0 s, no hex shield,
  the Talon's aimed pairs, pays once); every number is a first draft decided 2026-10-31 (round 72). It is a
  **machine** for the EMP like every hybrid (decided in this step, shown in round 72).
- 2026-10-31, design phase (step m10-d-act; decided 2026-10-31 (round 72)): the **line abreast** of 2 added to the formations, as
  Level 36 flies the Chimera's intro pair (t=34, a quiet window in open sky).
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): Q9 a (the hybrid livery: gunmetal
  with teal tissue seams; b and c rejected) and the document approved as drafted (D.9; `design: approved`); **A.1 b**,
  "overgrown", chosen (`art: chosen`; a "seams" in `concept/rejected/`), so the seams are twice as wide and veined
  with tissue nodules (the look above).
- 2026-10-31 (M10 part B, step B2): the stat block as data. The flock's numbers are first values for the 40 px
  airframe (pending the user's review in round 74): separation 30 px and radius 72 px (the Mote's 18 / 48 scaled to
  the hull), 160 px/s cruise (the table's), 220 px/s after a loop-back, 240 °/s, at most 12 members (hard's swarm of
  10); the fire cap 3 in a 0.3 s stagger in the swoop path's 300 px fire band.
