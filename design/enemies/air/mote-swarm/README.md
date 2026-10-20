---
title: Mote Swarm
design: approved
implementation: in-progress
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-20
---

# Mote Swarm

## Summary

A flock of tiny ember-like motes that swirls in front of the player, sweeps off screen and loops back to dive from behind. Spread and area weapons shred it.

## Design

### Stat block

The numbers live in [data.yaml](data.yaml); the stat block is one mote's, a swarm is a wave of them. Values are first-draft balancing numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)). HP is in damage units (a Pulse Cannon L1 shot does 2); bullet damage classes and speeds are defined in the balancing basis. The flock is a planned key (the [schemas](../../../tech/architecture/README.md#data-file-schemas)): it waits in the data's comment until the simulation reads it.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` |
| Size tier | `tiny` |
| Size | 16×16 px, hitbox 10×10 |
| Parts | single |
| Orientation | `16 angles` |
| HP | 1 (easy 1 / hard 1, from the global multipliers) |
| Armour / shield | none |
| Speed | 200 px/s (flock cruise), 260 px/s diving after a loop-back |
| Movement | `flock` (separation 18 px, alignment, cohesion) round a leader point that flies the wave's authored route at 200 px/s (`path`): it swirls in front, exits at the bottom-left or bottom-right, re-enters from the bottom 1.5 s later (the edge warning 3 s ahead, starting while it exits) and dives up through the player's lane |
| Attack | `none` — contact (damage `tiny` = 6 per mote; destroyed by the impact) |
| Formations | swarm (6–24), pursuit (8–12) (a wave of motes; base 20, easy 16, hard 24 by the formation lever; Level 10 flies smaller ones too) |
| Weak points | none (dies in one hit) |
| Effective traits | `spread`, `area`, `rear` |
| Credits | 2 (score 20 × chain) per mote; a swarm of 20 pays 40 |
| Death | `tiny` ember puff |
| First level / used in | L10; returns in Act 3 (L20, as the pursuing swarm) |
| Difficulty hooks | flock size via the global formation lever; hard: two loop-backs (level data); the pursuit (Level 20): easy a lunge every 8 s with a 1.2 s tell, hard every 4.5 s |
<!-- /data -->

### Behaviour

User decision D8 = a of M5 part D: **real flocking** (boids).

- **The swarm** (formation `swarm`): a wave of 6–24 motes. A **leader point** flies the wave's
  authored route (its `paths`, with an optional `loop_back`, as a chain's head) at 200 px/s; the
  motes start in a seeded cloud round the route's first point and each steers by **separation**
  (18 px), **alignment** and **cohesion** among its neighbours and a pull toward the leader point.
  The leader point is not a unit: shooting the motes never stops the route.
- **Determinism and cost**: a fixed iteration order (the motes in entry order), `StrictMath`, fixed
  arrays sized for 24 members (at most 576 pair checks a step), no allocation per step; a killed
  mote drops out and the rest close up. The same seed flies the same flock.
- **Loop-back**: after its route leaves the bottom-left or bottom-right edge, the leader point
  re-enters from the bottom **1.5 s** later and the flock dives up through the player's lane at
  260 px/s. The bottom edge is warned **3 s** ahead of the re-entry, so the warning starts while
  the swarm still exits (the chains' rule); the flock counts as a `rear` wave for Rook's Trail from
  the re-entry, and the attempt's first loop-back raises the radio event `first-loop-back`. On hard
  a swarm loops back **twice** (the level data's wave change); on easy as on medium.
- A swarm may also enter **from the bottom edge** (`from: rear`, warned 3 s ahead as any rear
  wave): Level 10's crossing swarms.
- **Each mote is an ordinary unit**: a kill, a bounty of 2 and a chain step of its own, so chains
  are easy to build on a swarm, a score opportunity. Motes never fire; the swarm's danger is its
  loop-back path. A mote that touches the ship, Rook or a shuttle deals `tiny` contact (6) and is
  destroyed by the impact, as a rammer.
- **Seekers ignore it** (user decision 2026-10-20, "Homing ignores motes"; the stat block's
  `seekers_ignore: true`): no homing shot or torpedo locks onto a mote, no turret turns to one and Rook
  never makes one his target; it is fought with direct fire. A shot that flies into a mote still hits it
  (only the targeting changes). This holds in every level the swarm flies in (Levels 10, 11, 17, 18
  and 20).
- **Readability**: the leader routes keep the flock a readable cloud ("like starlings"), not a
  formation; the first values of the weights are tuned in the build for that.
- **Pursuit** (M7 part G, [Level 20](../../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#the-pursuit-swarm);
  numbers approved there in round 36, Q.20): the `pursuit` formation's flock presses from behind in a
  reversed section. Its leader point holds a band 50–150 px below the top edge, follows the ship's x at
  90 px/s, and every 6 s (easy 8, hard 4.5) lunges: a 0.8 s tell (easy 1.2; the flock contracts into a
  60 px ball), a dive down the ship's lane at 240 px/s to 140 px below where the ship was, no homing, and a
  climb back at 200 px/s. Refills of 3 (the formation lever: easy 2, hard 4) join from the top edge every
  10 s while the flock has room; at its wave's break-off the leader climbs off the top at 200 px/s and the
  members follow, alive. The defaults are the stat block's `pursuit` block (with its easy and hard hooks);
  a wave's `pursuit` block sets the break-off and may change the rest.

### Concept art

Chosen concept: [mote-swarm-r05-a.png](../concept/mote-swarm-r05-a.png), [mote-swarm-r05-a.gif](../concept/mote-swarm-r05-a.gif) (listed in the [air](../README.md#concept-art) Concept art table). M5 part D takes it straight to production (user decision D10 = a, concept round 32): 16×16, 16 headings, a flicker, the ember puff, an intel portrait and a flock GIF.

The production files (M5 part D, straight to production per D10 = a; approved as final in concept round 32), generator notes in [concept/prompts.md](concept/prompts.md):

| File | What | Status |
|---|---|---|
| [concept/mote-swarm-final-r32-a.png](concept/mote-swarm-final-r32-a.png) | Final sprites (`tools/art/mote_swarm.py`): 16 headings × 3 flicker frames (`mote-swarm_0..47`, 16×16), the ember puff and tatters (24×24) | chosen |
| [concept/mote-swarm-final-r32-a.gif](concept/mote-swarm-final-r32-a.gif) | A 20-mote flock looping back | chosen |

## Implementation

- [x] Stat block values loaded from [data.yaml](data.yaml); global difficulty multipliers applied
- [x] Boids flock round a leader point flying the wave's authored route (D8 = a): separation 18 px,
      alignment, cohesion; deterministic and allocation-free (at most 24 members)
- [x] The `swarm` formation: from the top or `from: rear`; the loop-back (one, hard two) with the
      bottom-edge warning 3 s ahead, Rook's Trail and the radio event `first-loop-back`
- [x] Per-mote kills count toward chains; contact `tiny`, destroyed by the impact
- [x] Death effect, bounty and score per this spec
- [x] Production sprites (headings, flicker, ember puff) and intel portrait; the swarm sound
      (concept round 32's b)
- [x] M7 part G: the `pursuit` formation (band, lag, lunge with its tell, refills, break-off) — its data
      (the `pursuit` block and hooks, the formation listed) written in step G2 (`RetreatKeysTest`);
      flown from step G3, mapped in step G4, its tell look and band hum in step GB

## Decisions

- 2026-10-01: Promoted from the air roster to a full spec for the Acts 1–2 wrap-up.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../../reviews/acts-1-2/README.md).
- 2026-10-08: M5 part D (user decisions of 2026-10-08): **D8 = a** boids: a `flock` group like the
  Coilwyrm's chain, its leader point flying an authored route from the level data (loop-backs
  included), the members steering by separation (18 px), alignment and cohesion toward it; fixed
  order, `StrictMath`, fixed arrays, at most 24 members; the route as data also ticks the
  Skitter's authored-paths item for snakes and swarms; rejected: b (authored paths with a fixed
  offset and a wobble: a moving formation, not a flock) and c (b with separation only). Stated
  defaults: the 1.5 s edge warning replaced by the 3 s rule (the 1.5 s off-screen gap kept, so the
  warning starts while the swarm exits); the numbers in [data.yaml](data.yaml); one mote's stat
  block, `swarm` its formation (6–24; `rear ambush` is the Wraith's), the draft's 12–30 narrowed to
  the 24 members a flock holds; easy as medium (one loop-back), hard two. Our readings, for review
  in round 32: the leader point as a route, not a unit; the weights' first values; a mote destroyed
  by contact with a shuttle. `design` goes to `review` for round 32.
- 2026-10-08: M5 part D simulation built (the movement, the cloak or the flock, the formations and
  their keys, read from the data file); its simulation items are ticked, the drawing, the sounds and
  the art follow.
- 2026-10-08: Concept round 32 closed (user): the production art (`mote-swarm_0..47`, the ember puff
  and tatters) and the intel portrait approved as **final**, the weak spots as they are (the muddy
  ember puff); the swarm sound **b** (an insect chitter over a whoosh, at the entry and each
  loop-back); the numbers and the build choices of round 32 (the flock tuning, the leader point a
  route, the motes removed 6 s after their route ends, `first-loop-back` at the re-entry) accepted.
  `design: approved`, `art: final`; every Implementation item is ticked, so the implementation is
  `done`.
- 2026-10-20 (M7 part G, step G2): the `pursuit` formation's defaults added to the stat block (the Level 20
  numbers approved in round 36) with its easy and hard hooks, and `pursuit` listed among its formations;
  `implementation: in-progress` until the pursuit flies.
- 2026-10-20 (user, after step G4 measured the pursuit melting under the homing pods and Rook):
  **"Homing ignores motes"**: the seekers never target a mote, and direct fire still hits it. Step SW
  added it as the stat block's `seekers_ignore: true` (the [schemas](../../../tech/architecture/README.md#data-file-schemas)),
  honoured by the player's homing shots, torpedoes and turrets and by Rook. The turrets count as seekers
  in our reading, pending the user's review in round 43. Levels 10, 11, 17 and 18 fly motes too. Their
  measures moved a little and every rule still holds: Level 11 unchanged; Level 10's pacing flight at
  medium 2.04 on the screen (was 1.89); Level 17's floor 2.59 (was 2.60, least 2.5); Level 18's floor
  2.69 (was 2.77). Level 20's numbers are in its
  [README](../../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#the-pursuit-swarm).
- 2026-10-20 (step R43): the pursuit flies (steps PG3, G4, GB, SW; its Implementation item ticked); whether the swarm's data levers (band lag 30 px/s, a lunge every 4 s) are applied and whether the turrets count as seekers are questions Q.1 and Q.3 of [round 43](../../../concept-rounds/round-43/README.md), pending the user's review.
