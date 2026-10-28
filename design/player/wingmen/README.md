---
title: Wingmen and drones
design: approved
implementation: in-progress
art: final
depends-on: [../weapons, ../../story, ../../systems/saves, ../../ui/hangar, ../../ui/hud]
updated: 2026-10-28
---

# Wingmen and drones

## Summary

Extra guns that fly themselves. The AI wingman Lt. Kenji "Rook" Tanaka flies his own ship in
the **escort slot**. Drones are cheaper helpers: light drones use a wing mount, and a heavy
drone can take the escort slot instead of Rook.

## Design

### Escort slot (recommended answer to "wing slot or separate slot?")

A separate **escort slot**, unlocked at L08 when Rook is assigned as Lancer's wingman (a story
beat in [act 2](../../campaign/act-2-homefront/README.md); Rook has been in Aegis Wing for years
and flies in the wider formation before that). It holds either Rook or one heavy drone. It draws no power
from the Stormhawk's generator: the escort brings its own. Its costs are hiring/upgrade prices
and repairs.

Why a separate slot: if Rook took a wing mount, players would compare a whole pilot to a
missile pod, and he would rarely be worth it. A separate slot keeps both choices interesting and
makes his availability a story lever: Rook is shot down at the end of L26 and is missing for
L27–L29 (see [Rook](../../story/characters/rook/README.md)). During those levels the escort slot
can only hold a heavy drone; Rook is freed at Ceres Hub in the middle of L29, flies its last section and
is back in the slot from L30 (draft, *Rook's rescue and return* below). His upgrades are kept.

In Act 2 the slot always holds Rook: there is no empty escort slot and no choice of escort until
the Bulwark (L22).

### Rook (AI wingman)

| Property | Value (first draft) |
|---|---|
| Hire | Free when he is assigned (L08); his guns and their upgrades are paid |
| Ship | An AF-12 Stormhawk built on the variant-C airframe (the blended manta from the [ship concepts](../ship/README.md)) in Rook's own colours; 42×42 sprite, slightly smaller than the player's 48×48 so the player's ship always reads first; armour 80, no shield |
| Position | Formation slot beside and slightly behind the player (left or right, hangar setting) |
| Behaviour | Keeps formation, fires whenever the player fires, dodges bullets with a delay, prefers the player's target, then an enemy near him (a flank threat, within 160 px) |
| Weapon | One gun fitted from his own inventory (see *Rook's guns* and *Escort inventory* below), each gun upgradable L1–L5 at 60 % of the player's prices |
| Downed | At 0 armour he ejects and is out for the rest of the level; his armour is repaired in the hangar, like the player's (see *Armour and repairs*) |
| Radio | Banters on the radio; warns about threats from the rear ("Six o'clock, Lancer!") |

### Where Rook flies

- **Hired** when the hangar opens with Level 08 or later as the next level: the visit after Level
  07's act end, or a loaded save that is past it. From then on the save's `escort` holds him
  ([saves](../../systems/saves/README.md)).
- He flies in **every campaign level from 08 on** while he is hired and not grounded (below). He
  does **not** fly in Act 1 levels replayed from mission select, nor in a `--level` run below 8
  (without `--escort`); his armour and guns are then left as they are. Which escort a replay of a later level flies is
  set in [mission select](../../ui/mission-select/README.md) (*The replay's escort*): a replay while Rook is lost flies
  him at full armour with no repair warning (the user's decision of 2026-10-27).
- The L08 HUD hint "Rook's side can be set in the hangar" points at his side setting.

### Rook's AI (first-draft parameters)

Coordinates are relative to the player's centre, +y pointing down the screen; the side (left or
right) is the hangar setting and mirrors the x values.

**Ship**

| Property | Value |
|---|---|
| Armour | 80, no shield, no hit invulnerability; takes enemy bullets and contact damage like the player |
| Hitbox | 11×11 px, on the `air` layer |
| Movement | Top speed 250 px/s, full speed in 0.2 s; never closer than 40 px to the player, also where that meets the play-field margin (the player near an edge or in a corner, a side swap gliding past him: he is pushed out along the margin); keeps the 12 px play-field margin. When his straight way to his slot passes within the 40 px (a slot on the player's other side) on a side where the margins leave no room at that distance, he steers round the player the other way, 8 px outside them: with the player low on the screen the way beneath is closed and he passes over him |
| Bodies | His slot keeps his 14 px dodge clearance from every `air` enemy's hit box, over where the enemy flies in the next 0.4 s: a slot inside that box moves to its nearest edge he can reach without crossing the box (he waits on his side while a body passes), and once inside one he leaves it the shortest way that does not cross the body; such a move jinks like a sidestep and keeps the 40 px to the player |
| Collision with an `air` enemy | Rook takes the enemy's full contact damage and deals 20 to it; a `tiny` or `small` enemy is destroyed by the impact, as when it rams the player |

**Formations** — he switches automatically and glides to the new slot over 0.6 s.

| Formation | Offset | When |
|---|---|---|
| Wing (default) | (64, +28) | No other condition |
| Wide | (120, +10) | A `sides` wave is active, or an enemy is within 160 px of him (plain distance from his centre, so an enemy just ahead counts too) |
| Trail | (40, +90) | A `rear` wave is active: pursuers overtake him first and enter his firing cone sooner |

A wave is active from its first unit's entry until its last unit has died or left the screen. A
segment chain counts by the edge it entered from last: a Coilwyrm wave from the rear is a rear
wave, and a chain that loops back is one from its re-entry at the bottom edge. Planned (M5 part D,
Level 10): a Wraith `rear ambush` counts as a rear wave from its re-entry at the bottom edge (its
cloaked pass from the top before it does not), and a Mote Swarm that loops back from its leader's
re-entry, as a chain's loop-back. Those re-entries are the units' own flight pattern: since
2026-10-21 (user) they get no edge warning and so no rear bark (trigger 2), which keys to the edge
warnings of rear waves ([HUD](../../ui/hud/README.md)).
When both Trail and Wide apply, **Trail** wins. When the slot lies outside the play field (the
player flies close to the edge on Rook's side, or near the bottom edge in Trail), he takes the
**mirrored slot** on the other side, and returns to his own side once it has been inside the
field for 1 s. Formation changes and side swaps start after his reaction delay.

**Reactions**

| Property | Value |
|---|---|
| Reaction delay | 0.25 s for target switches, formation changes and dodges |
| Dodging | Every 0.1 s he predicts enemy bullets 0.6 s ahead (user decision 2026-10-06; 0.4 s left him too little time against aimed fire); the soonest one that would pass within 14 px he decides on **once**: he **reacts to about 70 %** of them (user decision 2026-10-06), sidestepping up to 48 px at right angles to it after his reaction delay and keeping at it while it is the soonest, then back to his slot; the rest he misses and ignores. The 0.25 s delay inside the 0.6 s look-ahead leaves him 0.25–0.35 s to clear a bullet |
| Firing cone | 30° ahead of his ship (15° either side of straight up), range 360 px: where he picks his target. His craft never turns (banking frames only), so he fires up the screen. With the Mortar (a lobbed gun) he picks among the ground targets anywhere ahead of him within the lob's 200 px instead (user decision 2026-10-07), so a target beside the player is his while he flies in formation |
| Fire | Fires whenever the player is firing, at his gun's rate, with a target in his cone or without one; he never fires while the player does not. The target only decides what he aims at: a homing gun's shots start locked onto it (without one they find their own); the Mortar's shells land **on it** at any distance up to the lob's 200 px, their flight time and arc shortened to the distance (user decision 2026-10-07; without a target they land 200 px ahead, snapping to a ground target within 48 px, as the player's) |
| Target priority | (QA-1, decided 2026-10-27, confirmed in round 61 on 2026-10-28: an enemy homing missile, a homing diamond, is picked only when no enemy unit is in his cone) (1) an enemy the player damaged in the last 1.0 s; (2) an enemy within 160 px of him (flank threat); (3) the nearest enemy — all within his cone (the Mortar's: ahead within 200 px). Layers follow his gun's traits (Missiles can hit `high-air`; Mortar only `ground`); never `sub` (none of his guns is `anti-sub`) |

Enemies never aim at Rook: aimed attacks and the target-the-objective hook pick the player or the
objective as before, and Rook is hit by what crosses his path. He collects no pickups and is not
pushed by the scroll. The AI's only randomness is his own generator for the dodge share, seeded
from the sortie's seed and kept in the boss checkpoint, so a sortie with him stays deterministic
(state hash, replays); runs without him keep their hashes.

**Rook's guns** — each uses a player weapon's per-level table, scaled; prices are 60 % of the
player weapon's base price, upgrades follow the [weapons](../weapons/README.md#common-rules)
formula on 60 % of the player weapon's upgrade base.

<!-- data: rook-guns -->
| Gun | Id | Based on | Scale | DPS L1 → L5 | Price | Upgrades L2 / L3 / L4 / L5 | Available |
|---|---|---|---|---|---|---|---|
| Autocannon | `autocannon` | [Autocannon Pod](../weapons/autocannon-pod/README.md) | × 1.5 | 12 → 37 | free (fitted when he joins) | 150 / 300 / 600 / 1 200 | act 2 |
| Scatter | `scatter` | [Scatter Vulcan](../weapons/scatter-vulcan/README.md) | × 0.6 (volley) | 11 → 39 | 720 | 360 / 720 / 1 440 / 2 880 | act 2 |
| Missiles | `missiles` | [Micro-missile Pod](../weapons/micro-missile-pod/README.md) | × 1.5 | 12 → 42 | 480 | 240 / 480 / 960 / 1 920 | act 2 |
| Mortar | `mortar` | [Hammer Mortar](../weapons/hammer-mortar/README.md) | × 0.6 (aimed: lands on his ground target) | 15 → 54 | 900 | 450 / 900 / 1 800 / 3 600 | act 2 |
<!-- /data -->

- The **scale multiplies each projectile's damage**; the pattern, rate, speed, blast, turn rate
  and traits are the base weapon's at the same level. He fires the pattern from one muzzle at his
  nose; a pod weapon's pattern is that of the pod on his side (the Missiles launch outward, away
  from the player). The DPS column is the base weapon's volley DPS × the scale.
- He has **no overdrive**: an overdrive pickup is the player's alone. The Targeting computer's
  homing bonus does not reach his Missiles.
- His shots hit what the player's shots hit and pass through allies, as the player's do. His kills
  pay like the player's (see *Kills*).

### Escort inventory (user decision D4 of M5 part A)

Rook's guns are owned like the player's weapons, in their own inventory:

- Buy any of the four at its price (60 %); each owned gun keeps its own level and is upgraded
  L1–L5 separately. Exactly one is **fitted**; fitting another is free, the others stay in his
  inventory at their levels.
- Sell an unfitted gun at the [sell-back](../../systems/economy/README.md#sell-back-and-undo)
  share (60 % of all spent on it, 100 % for one bought during this visit). The fitted gun cannot
  be sold or unfitted (like the front gun), and the free Autocannon sells for its upgrades only.
- Every escort purchase, upgrade, fit and sale is part of the visit's undo log.
- His guns and the player's weapons are separate: none of his can be fitted on the Stormhawk, and
  no Stormhawk weapon on him. The escort draws no power.

### Armour and repairs (user decision D3 of M5 part A)

- His armour is kept between levels: after a won level it is saved as it ended; a failed or
  aborted attempt leaves it as he launched.
- The hangar's **Repair** has a **Rook line** beside the ship's: per point or all, at the
  difficulty's repair cost per point ([difficulty](../../systems/difficulty/README.md): free on
  easy, 5 on medium, 10 on hard), the same as the player's. **Unrepaired armour stays missing.**
- **Launch warning** when his armour is below 50 % (under 40 of 80), in the same confirmation as
  the player's warnings.
- **Grounded:** after an ejection his armour is 0. At 0 he stays home: the escort tile reads
  `GROUNDED`, the launch confirmation says he will not fly, and any repair (one point is enough)
  puts him back in the air for the next launch.

### Kills (user decision D2 of M5 part A)

Rook's kills pay **like the player's**: the enemy's bounty (with the act factor, the difficulty's
income and the level's bounty scale), its score, the chain, the objectives (a kill ratio, a
group, a named target) and the statistics. The debrief does not list them apart. This follows the
Airstrike's precedent ([specials](../specials/README.md)); the typical haul is unchanged, since
its shares are about what dies.

### Shot down, retry and checkpoint

- At 0 armour he **ejects**: the pod pops out and drifts off-screen sideways (120 px/s, level
  with where he ejected) while the ship explodes (`medium` explosion). It heads for the **nearer
  side edge unless the player's ship is in the way** (on that side of the pod and within **36 px**
  above or below its line); then it takes the other edge, so the pod never passes under the ship.
  It pops out under the explosion and is drawn **over it after 0.1 s**, with a red **glow** around
  it while its beacon blinks and a thin **trail** of light smoke behind it, so it reads on any
  ground. He is out for the rest of the level. This never fails the mission and costs no score.
- His scripted radio lines still play after he ejects (he is on the radio from his pod). A line
  that needs him flying (a level's "Rook's first kill", the `escort-first-kill` event below) does
  not fire.
- On a **retry** he starts again with his level-start state, like the player: his level-start
  armour, at least the retry's armour floor share (as the player's, see
  [retry](../../systems/retry/README.md)).
- The **boss checkpoint** includes him: his position, armour and whether he has ejected. *Retry
  from boss* puts him back in formation in that state (an ejected Rook stays out).

### Act 2 hazards

- [Lampreys](../../enemies/air/lamprey/README.md) latch only on the player. Level 12 (M5 part F,
  user decision F3 = a): his shots pass a **latched** Lamprey and his target pick skips it (it sits
  on the player's hull); a chasing, stunned or fleeing one is an ordinary `air` target and body.
- The gusts and the storm wall's drift of [Level 12](../../campaign/act-2-homefront/level-12-storm-front/README.md#weather)
  push him like the player (M5 part F, stated defaults): the push adds to his position, not his
  steering, and the play field's edges clamp him as now; no change to his AI. He does not collect
  the weather sondes (only the ship does).
- Ravager pounces and `air` contacts hit him as designed (his collision rule above).
- Level 10 (M5 part D): in Trail he sits 90 px behind the ship, in the band where the
  [Wraiths](../../enemies/air/wraith/README.md) hold 20–70 px above the bottom edge after
  decloaking. No change to his AI (a stated default of part D): a cloaked Wraith is on `high-air`
  and touches nobody; a decloaked one is an `air` body his slot keeps its clearance from, and its
  bursts are bullets he dodges like any other. He fires only up the screen, so he gets a Wraith on
  its exit up a side lane, not in its hold. A `WingmanTest` case covers Trail near the hold band.
  The [evacuation shuttles](../../allies/README.md#evacuation-shuttle) are no obstacle to him (he
  flies through them, as the ship does).
- Level 11 (M5 part E, stated defaults): he **skips `sub` targets** (his pick never lands on a
  submerged jelly or the Kraken's submerged parts, which none of his guns reach; with the Mortar he
  lobs at surfaced ones only), flies over the convoy's ships like the ship, and in the Kraken's arena
  he **keeps out of a telegraphed or slamming lane**: from a lane's telegraph until its arm lies awash,
  his slot and his dodges stay outside the lane (his slot moves to the nearer point outside it,
  keeping the 40 px from the player; when the player is in that lane he waits beside it); nothing
  else about his AI changes. A **slam** hits him like the ship (`heavy` 10 if his hit box overlaps the
  lane at the impact). The balance plan's L11 visit refits his Mortar for the rafts and the surfaced
  Kraken.

### Scripted lines about him (user decision D4 of M5 part B)

Two kinds of a level's radio cues are about Rook himself (schema in
[architecture](../../tech/architecture/schemas-player/README.md), built in M5 part B):

- **His first kill**: the radio event **`escort-first-kill`** (no `enemy`) fires on the first
  kill in an attempt whose killing shot or blast is his (his gun's mount), while he flies. It
  never fires after he has ejected, nor in a level he does not fly in (`--escort none`, an Act 1
  replay); it fires at most once per attempt (the flag is in the state hash and the boss
  checkpoint, so a retry from the boss keeps it). A cue on it is an event line like the barks'
  triggers; its line counts as a scripted Rook line for the barks' 8 s spacing (from the moment it
  fires: a bark queued with the same kill, a streak, is withdrawn). Level 08's "Splash one" is the
  first.
- **His side**: `{side}` in a radio line becomes `left` or `right`, his side setting (the save's,
  or `--escort`'s `side=`): the line is voiced once per side and plays the take for his side, the
  subtitle showing that text. Level 08's opening "Lancer, I'm on your {side}" is the first.

### Radio barks

Rook's barks are **event lines** under the [radio queue](../../ui/hud/README.md#left-panel-mission)
rules: they wait for a gap, are dropped as stale after 6 s and never push a timed line (all but the
eject bark, an urgent line: see below), and are voiced like every Act 1 line
([voice](../../audio/voice/README.md)).

- **Spacing:** at least **8 s** from the start of any Rook line (a bark or a scripted line) to the
  next bark. A bark that fires sooner is dropped, and so is one that fires within 8 s before a
  timed Rook line is due: the scripted line says it. The **eject bark** (trigger 6) is exempt: it
  is never dropped by the spacing, a scripted line or as stale.
- **Priority:** only one bark waits at a time; a bark of a higher priority (lower number) replaces
  a waiting one, a lower one is dropped.
- **Variants:** the *n*-th bark of a trigger in an attempt (from 0) plays variant
  (level number + *n*) mod the trigger's count, so levels open on different lines and a retry
  replays the same ones.
- The **eject bark is urgent**: it cuts a bark of his that waits or is on the radio and plays at
  once like the Airstrike's call (an urgent line already on the radio finishes first), and no other
  bark replaces it.
- No bark once the level is won.
- **Reverse scroll (Level 20, M7 part G):** in a reversed section trigger 2 fires for the waves that
  enter from the **top** (`rear` by travel: the pursuit swarms, the Wraiths) with its "Up top!"
  variants (`top_lines`), and stays silent for the bottom entries, which are ahead by travel there
  (accepted in round 43 (2026-10-20).
- After he ejects, trigger 5 can no longer fire; the others still do (he watches the scope from
  his pod).
- The level documents' scripted "Six o'clock" and "flank" lines (L08 t=92, L13 t=82 and t=122,
  L14 t=80) are reviewed by each level's part: they suppress the bark by the spacing rule. Level
  10's t=50 "Six o'clock, Lancer!" is dropped (M5 part D): his rear bark says it, and his event
  line on the first loop-back (`first-loop-back`) counts as a scripted Rook line for the spacing,
  as `escort-first-kill`'s does. Level 12's t=102 is dropped the same way (M5 part F), its t=44
  "Seeds on the wind" kept (it suppresses the side bark of the t=46 cluster), and his line on the
  first latch (`first-latch`) counts as a scripted Rook line.
- **Level 13 (planned, M5 part G, accepted in round 34 (2026-10-12)):** he does not guard the Nansen Relay
  (no change to his AI) and keeps his Autocannon (the plan's L13 visit changes nothing for him); the
  level document's t=82 "Six o'clock" and t=122 "Contacts on your flank" are dropped for his rear
  and side barks, and his t=3, t=54 and t=230 lines carry `requires: escort`.
- **Level 14 (planned, M5 part H, accepted in round 34 (2026-10-12)):** in the Siege Spire's fight he flies
  Trail for each of the maw's Wraith re-entries (the bottom edge counts as a rear wave; no edge warning or rear bark since 2026-10-21); his boss-warning bark plays at t=108 (not scripted);
  the level document's t=80 "Six o'clock" is dropped, his t=3 line carries `requires: escort`, and
  his phase-3 line "Roots and all. Somebody call a gardener." is an event line 5 s after the phase
  starts. No change to his AI.

<!-- data: rook-barks -->
| Priority | Trigger | Variants (speaker Rook) |
|---|---|---|
| 1 | Boss warning starts (an act boss's warning, a mid-boss's sting) | "Here comes the big one." · "Heads up. Something nasty's coming." · "Oh, I don't like the look of that." · "Big contact inbound. Stay loose, Lancer." |
| 2 | A `rear` wave enters within 1.5 s (shouted) | "Six o'clock, Lancer!" · "Contacts on six! Why is it always six?" · "Check six, check six!" · "Behind us! They're on our tails!"; in a reversed section, from the top: "Up top, Lancer! They're on our tails!" · "Up top! Coming down on us!" · "Contacts up top! Keep moving!" |
| 3 | A `sides` wave enters (shouted) | "Contacts on your flank!" · "Bandits coming in from the side!" · "Flank! Watch the edges, Lancer!" |
| 4 | The player's armour falls below 30 % (again only after it was back at 30 % or more) | "You're smoking, Lancer. Ease off!" · "That hull won't take much more, Lancer!" · "Lancer, you're coming apart. Get clear!" |
| 5 | Rook's armour falls below 30 % (once per attempt) | "Taking a beating over here!" · "I'm hit. I'm okay. I'm mostly okay!" · "Rook's hurting, Lancer. Hurting bad." |
| 6 | Rook ejects (shouted) | "Punching out! Give 'em hell!" · "I'm out, I'm out! She's all yours, Lancer!" · "Ejecting! Somebody owes me a ship." |
| 7 | 10 kills (the player's and his) within 5 s; the count starts over | "Nice shooting!" · "Ha! Save some for me!" · "Now that's how it's done." · "Look at you go, Lancer." |
| 8 | An overdrive pickup appears | "Power-up! Grab it!" · "Ooh, shiny. Grab that." · "Overdrive's up for grabs, Lancer!" |
<!-- /data -->

Expressions: 1, 4 and 5 `grim`; 2, 3 and 6 `fierce` and shouted; 7 and 8 `fierce` (the voice's
joking row).

### Balance

- Rook adds 12 → 42 DPS. `BalanceTest` prints his DPS beside the player's but does not count it
  against the [reference DPS](../../enemies/README.md#balancing-basis), which was set without him.
- The [balance plan](../balance-plan.yaml) buys his guns and pays his repairs from the L08 visit;
  `ActPlaythroughTest` and the test autopilot fly with him.

### Debug option

`--escort rook:<gun>:<level>[,side=left|right]` flies Rook with that gun (an id from the table
above) at that level and full armour on any level, Act 1's included, for testing; the side is the
save's (left for a new campaign) if left out. `--escort none` flies a level from 08 on without
him. For testing only, like `--loadout`. The option applies to the flights only, never to the
campaign's gear: it does not hire him, his fitted gun, side and armour stay as they were, and no
save (the autosave of a failure, a retry or a won level included) holds it. M8 part A: `--escort
bulwark[,side=left|right]` flies the Bulwark at full armour instead (`DebugFit.EscortFit.bulwark`,
`Campaign.debugBulwark`), on any level, never the gear's.

### Data

Rook's numbers, his guns and his barks live in [data.yaml](data.yaml) (`rook`, `guns`, `barks`;
schema in [architecture](../../tech/architecture/schemas-player/README.md)), loaded by the
content's `WingmenData` (M5 part A). The guns and barks tables above are rendered from it by
`tools/sync_tables.py` (`rook-guns`, `rook-barks`; M6 part D): the DPS, prices and upgrades from
each gun's base weapon and the price factor, the Scale and Trigger cells' wording from `notes`.
Rook's repair cost per point is the difficulty's `repair_cost`, so it is not in the file. M8 part A: his
absence (`rook.missing`: lost after Level 26, back from Level 30) and the Bulwark's numbers (`bulwark`) are there
too (`WingmenData.Missing`, `WingmenData.Bulwark`).

### In the game (M5 part A)

- **His craft** is drawn like the player's: the banking frame of his bank, his two warm engine
  flames, a runtime drop shadow on the ground layer, the muzzle flash of his gun at his nose, a
  white flash on a hit, a smoke trail below 30 % armour (denser below 15 %) and, after the eject,
  the `medium` explosion and the pod drifting off to a side edge (the path rule above), over the
  explosion after 0.1 s with its beacon glow and smoke trail.
- **Production sprites** (art track, final since concept round 28; `tools/art/rook.py`), in `assets/sprites/`:
  `rook_0` … `rook_4` (42×42, hard left … hard right at −30, −15, 0, +15 and +30° of roll through
  the Stormhawk's perspective camera, as the player's `ship_0` … `ship_4`, so the pair banks as
  one; the round-09 Ember model); `rook-flame_0` … `rook-flame_2` (8×14, his warm engine flame:
  a pale yellow core, orange body and red-brown tail flickering in length, drawn additively with
  its top centre on each engine mount); `rook-pod_0` … `rook-pod_3` (16×16, the eject pod: a
  slate capsule with a yellow nose band and a canopy, tumbling slowly clockwise in 45° steps with
  its red beacon lit in the first frame only, so the 4-frame loop blinks once; normal blending).
  His engine mounts per banking frame are in `assets/pivots/rook.json` (`points.engine-left` and
  `points.engine-right`, five `[x, y]` each, px from the sprite's top left, y down, as
  `pivots/ship.json`): the nozzles' rear ends, rolled with the hull.
- **Placeholders** (the game's fallbacks while a sprite is missing): the chosen round-09 Ember
  frames (40×40) cut from [rook-craft-r09-a.png](concept/rook-craft-r09-a.png) by the pipeline's
  placeholder import as `rook_0` … `rook_4` (skipped now that the production frames carry their
  `Source` chunk); a small warm glow per engine at the concept's mounts; a drawn capsule with a
  blinking beacon for the pod.
- **Barks** queue on the radio as described above, with their voice files (27 lines, source
  `wingmen barks <trigger>` in the voice list, rendered by `tools/art/voice.py`).
- The HUD's escort box and the hangar's escort UI are in [HUD](../../ui/hud/README.md) and
  [hangar](../../ui/hangar/README.md#escort-from-level-08).

### Heavy drone "Bulwark" (escort alternative, L22)

*Act 4 draft, decided in round 48 (2026-10-22). Renamed from "Warden", which clashes with
Act 3's Warden Tank (the table's numbers are the 2026-10-01 first draft, kept; the under-water
changes and the loaner are new).*

| Property | Value |
|---|---|
| Ship | Armour 120, no shield, top speed 200 px/s, hitbox 14×14 px; the hydro-kit's drag applies under water like Rook's |
| Formation | Wing slot (56, +20), no automatic formation changes |
| Weapon | Slow cannon straight ahead: 1 shell/s, 14 damage (14 DPS), not upgradable. Under water the shell is `anti-sub` (a deep gun), so it keeps its 14 DPS instead of losing 30 % |
| Draws fire | While within 150 px of the player, 30 % of enemy aimed shots that would target the player target the Bulwark instead |
| Destroyed | Out for the rest of the level; rebuilt for the next level, repairs 8 cr per point |
| Radio | None (a drone) |
| Price and unlock | 6 000 cr, in the shop from the visit before L22, **and one Bulwark is loaned free** while Rook is missing (below) |

### Rook's loss and absence (L26–L29)

*Act 4 draft, decided in round 48 (2026-10-22) (Q7 = a: the shoot-down on the screen). The story
beat is in [Rook](../../story/characters/rook/README.md) and the L26 level draft.*

- **L26's end:** a scripted loss. In the black, torpedo trails from unseen Depth Hunters, Rook's
  light spins down into the dark, his last radio line is cut mid-word by the hits (since 2026-10-24 the line comes
  first and the torpedoes strike while he speaks; his 2.5 s spin plays on the screen: tossed up by the hits, he sinks
  back faster and faster and leaves the bottom edge as it ends). It is not a normal
  "Downed" (the eject, the repair at the next visit): the save records `rook_missing: true` from the
  win of L26.
- **L27, L28 and L29 (Act 5's first level):** the escort slot shows Rook as **MIA** (a greyed card,
  no hire and no repair row), his guns and upgrades kept in the inventory; the slot can hold only a
  heavy drone. A visit before L27 puts a **Bulwark (loaner)** in the slot at no cost, so the slot is
  never empty, and the shop sells the Bulwark for 6 000 as a second one for the later levels. Rook
  has no radio lines in L27 and L28: his `requires: escort` barks are dropped, not just silenced by
  the missing escort (the debrief and briefing lines name him). He is freed at Ceres Hub in L29 and
  flies again from L30, with all his guns and upgrades.
- **`--escort none`** and a run without Rook: nothing changes for the shoot-down (there is no ship
  to lose), only his radio is heard in L26; the MIA state is a no-op. A `--level 27` debug start
  treats him as MIA unless `--escort rook:` is given.
- The new rules need `Campaign.escortFlight()` to take the level number (an availability per
  level) and the hangar's escort UI a "missing" card ([hangar](../../ui/hangar/README.md)).

### Rook's rescue and return (L29–L30)

*Act 5 draft, step `m9-d-systems` (2026-10-27), **approved in round 60** (Q8 = a: the
rescue on the screen). The story beat is in [Rook](../../story/characters/rook/README.md); the set
piece's place, timing and lines belong to L29's level document in [Act 5](../../campaign/act-5-the-belt/README.md).*

- **Before the rescue** L29 starts as L27 and L28 did: Rook is MIA, the slot holds the loaner Bulwark
  (or an owned Bulwark), and Rook has **no lines** (his `requires: escort` barks stay dropped).
- **The rescue** (≈ t=120): the scroll halts over Dock 9, where a prison tender sits in four clamps;
  each clamp has a gun. The halt lasts until the four clamp guns are destroyed (there is nothing to fail
  and no timer; the level's waves keep the field busy). As the last clamp falls the tender opens and
  **Rook's Ember** launches from it (recovered with him; the how is [Rook](../../story/characters/rook/README.md)'s story), and the drone in
  the slot peels away to the edge it flies on and leaves ("Bulwark, RTB", a line of the level's). Rook
  flies into the slot over 1 s and fights the level's last section; his first line is
  *"Miss me? Don't answer that. Just shoot."*, then his barks are back.
- **What he flies with:** the gun fitted when he was lost, at its level, and **full armour** (the
  hangar's last repair state before L26 is not used: a rescued craft is a fresh start). His kills
  pay as always from the moment he joins.
- **The drone that leaves:** the loaner flies away whole and goes back to the CDF: from the L30 visit
  there is **no loaner** any more. An owned Bulwark peels away the same and returns to the hangar with
  its armour as it left; it is still owned and can be chosen for the slot in a later visit. A drone
  destroyed before the rescue changes nothing: Rook launches all the same.
- **Retry:** a retry restarts L29 as it started (Rook MIA, the drone in the slot), and the rescue plays
  again; there is no checkpoint in L29. The save clears `rook_missing` at L29's win (as built: the
  story flag runs from Level 26's win to Level 29's).
- **From L30** the escort slot defaults to Rook with all his guns and upgrades (his inventory was
  kept), the hangar's MIA card is gone and his repair line is back; an owned Bulwark stays a choice.
- **Debug starts:** `--level 29` treats him as MIA and plays the rescue with the loaner; with
  `--escort rook:<gun>` he flies from the start and the tender opens empty (no swap); with `--escort
  none` the set piece plays and the Ember launches and leaves upward (nobody joins). A
  [mission-select](../../ui/mission-select/README.md) replay of L29 flies it as the campaign does
  (the loaner, then the rescue).
- The new rules need a **mid-level escort swap** (the slot's flyer replaced during a level, scripted
  by the level: `RookLoss` in reverse) and the tender's clamp guns as ground targets of the level.

### Drones

| Name | Slot | Summary | Draw | Price | Unlock | Design |
|---|---|---|---|---|---|---|
| Light drone | wing mount | Orbits the ship at 36 px, fires a small gun at the point ahead of the nose, blocks bullets (breaks after 8 hits, rebuilds in 6 s at L1). Sold as the [Light Drone Bay](../weapons/light-drone-bay/README.md); see *Light drone* below | 2 → 3 | 2 000 | L15 | draft |
| Rear-guard drone | wing mount | Trails 40 px behind the ship, fires backwards (`rear`), blocks bullets from behind. Sold as the [Rear-guard Drone Bay](../weapons/rear-guard-drone-bay/README.md); see *Rear-guard drone* below | 2 → 3 | 2 400 | L20 | draft |
| Heavy drone "Bulwark" | escort | Tough (armour 120), slow-firing cannon (`anti-sub` under water), draws enemy fire; a free loaner while Rook is missing | 0 | 6 000 | L22 | draft |
| Hunter drone | escort | Leaves formation to chase homing targets, fragile | 0 | 7 500 | Act 6 (was L29; Q12 = c of round 60, decided: covering Rook's absence is the Bulwark's job) | idea |

Unlock `Lnn` = in the shop from the hangar visit before level *nn*. The Bulwark arrives before
Rook's absence (L27–L29) so the escort slot never has to stay empty.

#### Light drone

Act 3 (L15), approved in concept round 36 (user, 2026-10-13, D.26; its look is the disc drone, G.2 = a). The shop item, its per-level
numbers (DPS 10 → 30, hits 8 → 12, rebuild 6 → 4 s) and its price are in the
[Light Drone Bay](../weapons/light-drone-bay/README.md); this is its behaviour.

| Property | Value |
|---|---|
| Craft | A 14×14 drone (sprite), hit box 8×8, on the `air` layer; drawn over the ship's shadow, under the ship |
| Orbit | Round the ship's centre at **36 px**, one turn per **2.4 s** (150°/s), clockwise; a left bay's drone starts at 9 o'clock, a right bay's at 3 o'clock, so a pair flies opposite each other. The orbit centre follows the ship at once (the drone keeps its angle) |
| Fire | Whenever the player fires: one shot at a time at the bay's rate, aimed from the drone at the **convergence point 140 px ahead of the ship's centre**, so every shot crosses the ship's line of fire |
| Block | An enemy bullet that touches the drone is absorbed and costs it one hit; a `laser` touch costs 2 hits and the laser goes on. Enemy aimed shots never target the drone |
| Contact | An `air` enemy touching the drone costs it 4 hits (a `tiny` or `small` rammer 2) and takes 10 damage; a `tiny` or `small` rammer is destroyed, as against the ship |
| Break | After the level's number of hits it breaks: a small spark burst (`explosion-small`), its shots stop |
| Rebuild | The bay builds a new drone in the level's rebuild time; it fades in at its bay over 0.3 s and returns to its orbit. A new attempt or a retry starts with whole drones |
| Kills | Pay like the player's: bounty, score, chain, objectives (its shots are the player's) |
| Walls (Act 3, M2 = a) | Terrain never hurts it: where a canyon wall or rock island would cut its orbit the radius shrinks to keep it in the open (down to 20 px), and grows back afterwards; its shots stop at walls like bolts |
| Reverse scroll (L20) | No change: the orbit and the convergence point are relative to the screen and the ship's nose |
| Ship lost | When the ship is wrecked the drones break with it |
| HUD | The bay's row in the weapons box shows the drone's hits left as five pips draining; while it rebuilds, the pips refill over the rebuild time in a dim colour |

#### Rear-guard drone

Act 3 (L20), approved in concept round 36 (user, 2026-10-13, D.26). The shop item, its per-level
numbers (rear DPS 9 → 26, hits 8 → 12, rebuild 6 → 4 s, draw 2 → 3 MW) and its price (2 400 cr,
unlock L20) are in the [Rear-guard Drone Bay](../weapons/rear-guard-drone-bay/README.md) (M7 part G,
step G2; that document waits for the user's review in round 43); this is its behaviour.

| Property | Value |
|---|---|
| Craft | The light drone's 14×14 body with a rear-facing gun; hit box 8×8, `air` layer |
| Station | **40 px behind** the ship's centre and 12 px to its bay's side (a pair flies side by side, 24 px apart), following the ship with a 0.15 s lag, so it swings out in a turn |
| Fire | Whenever the player fires: straight back (180°), the `pulse` family pitched up; its shots stop at walls |
| Block, contact, break, rebuild, kills, walls, HUD | As the light drone's |
| Reverse scroll (L20, M4 = a, M5 = a) | "Behind" stays the bottom of the screen: the drone trails below the ship, which is ahead in the direction of travel there, so it shields the ship from the obstacles and ambushes that come up from the bottom and fires into them |

It gives the rear DPS of a Tail Gun L2–L3 from a wing mount, so a player can keep the rear slot
for another gun (the Rear Lance) or carry rear fire and a rear shield at once in Level 20; it gives
up the wing pod's forward DPS (10 at a Micro-missile Pod L2).

## Concept art

Rejected proposals (3 rows) are listed in [concept/rejected/README.md](concept/rejected/README.md).

Round 02 — see [round 02](../../concept-rounds/round-02/README.md). AI-generator prompts: [concept/prompts.md](concept/prompts.md). Generated by
`tools/concept/ships_r02.py`. Rook flies the variant-C airframe from
[ship](../ship/README.md) concept round 01. The mockups were drawn at 40×40; the production
sprite is 42×42, the size in *Rook* above.

| File | What | Status |
|---|---|---|
| [concept/rook-craft-r02-a.png](concept/rook-craft-r02-a.png) | Rook scheme A "Ember": dark slate hull, orange/yellow accents, warm engines — darker and warmer than the player | chosen |

Concept [round 09](../../concept-rounds/round-09/README.md) — generator `tools/concept/vfx_r09.py`.

| File | What | Status |
|---|---|---|
| [concept/rook-craft-r09-a.png](concept/rook-craft-r09-a.png) | Rook (Ember) 5 banking frames at 4×, 1× strip beside the Stormhawk with greyscale check, in-game view | chosen |

Concept round 28 (M5 part A) — game capture, see [prompts](concept/prompts.md#rook-ingame-capture-r28-a).

| File | What | Status |
|---|---|---|
| [concept/rook-ingame-capture-r28-a.png](concept/rook-ingame-capture-r28-a.png) | Game capture, Level 02 with `--escort rook:missiles:3,side=right`: Rook (placeholder Ember frames) in the Wide formation with his warm engine glow and shadow, his "Flank! Watch the edges, Lancer!" bark on the radio, the HUD's escort box | chosen |
| [concept/rook-final-r28-a.png](concept/rook-final-r28-a.png) | Production sprites (`tools/art/rook.py`): the five banking frames, with both flames at their pivots, the flame loop, the eject pod's tumble, 1× beside the Stormhawk with a greyscale check | chosen |
| [concept/rook-final-r28-a.gif](concept/rook-final-r28-a.gif) | Loop (`tools/art/rook.py`): Rook in the Wing formation banking with the Stormhawk, flames flickering, then his eject pod tumbling off to the side | chosen |
| [concept/part-a-capture-final-r28-a.png](concept/part-a-capture-final-r28-a.png) | Game capture sheet (M5 part A): Rook in production sprites in Wing and Wide, his muzzle flash and smoke, the eject (Level 06, scratch low armour) with the pod and `EJECTED`, his rear-wave bark, the Hornet, Swivel, Fan Blaster, Tail Gun and Proximity Mines, the Targeting computer's brackets and HP bar, the Salvage scanner's glint, the hangar's escort tab | chosen |
| [concept/part-a-capture-final-r28-a.mp4](concept/part-a-capture-final-r28-a.mp4) | The same capture as video with the game's sound (101 s): formations, the barks' voices, the eject, the Act 2 weapons, the utility modules, the hangar | chosen |
| [concept/rook-eject-capture-r28-b.png](concept/rook-eject-capture-r28-b.png) | Game capture sheet of the eject after the pod fixes (Level 07 medium, scratch data: armour 2, hitbox 40, no dodging): the pod over the explosion, its red beacon glow and smoke trail at 3× zoom, 0.1 s apart, and the full frame with the pod heading for the edge away from the ship, `EJECTED` in the HUD and the eject bark on the radio | chosen |

Concept [round 48](../../concept-rounds/README.md) (Act 4 "Deep Water", design phase 2026-10-21) — the
heavy drone [Bulwark](#heavy-drone-bulwark-escort-alternative-l22), a/b, under the round-48 under-water
grade; generator `tools/concept/gear_r48.py`; prompts in [concept/prompts.md](concept/prompts.md#bulwark-r48-a).

| File | What | Status |
|---|---|---|
| [concept/bulwark-r48-a.png](concept/bulwark-r48-a.png) | Bulwark A "turtle" — a domed blue-grey armour shell with a cobalt rim band, skirts, a heavy barrel and twin pump-jets, 36×36 (hit box 14×14); intact, hit, under half; beside the Stormhawk and Rook; the wing slot (56, +20) and the 150 px draw-fire radius; the loss; draws fire with a slow cyan lure halo (sheet) | chosen — round 48 |
| [concept/bulwark-r48-a.gif](concept/bulwark-r48-a.gif) | Bulwark A — lagging in its wing slot with the water's drag, its cannon firing once a second, 30 % of the stand-in Needlers' aimed shots going to it (motion) | chosen — round 48 |
| [concept/bulwark-final-r50-a.png](concept/bulwark-final-r50-a.png) | Bulwark final frames (`tools/art/act4_gear.py bulwark`, round 48 G4 = a): 36×36 intact / hit / under half, pump-jets, the lure halo loop, the shot, muzzle light and hit sparks, the loss (light, ring, debris); in the wing slot, firing, hit, lost (sheet) | chosen (round 50, F.5: accepted as final) |
| [concept/bulwark-final-r50-a.gif](concept/bulwark-final-r50-a.gif) | The Bulwark in the wing slot with its lure halo, firing twice, hit, scorched and lost, 20 fps at 2x | chosen (round 50, F.5: accepted as final) |
| [concept/bulwark-capture-r50-a.png](concept/bulwark-capture-r50-a.png) | Game capture, Level 19 under water with `--escort bulwark`: the Bulwark in the wing slot with its lure halo (a frame) | chosen (round 50, F.5: accepted) |
| [concept/bulwark-capture-r50-a.mp4](concept/bulwark-capture-r50-a.mp4) | Game capture with the game's sound: the Bulwark crossing the waterline at 10 s, holding its slot, firing and drawing the aimed volleys | chosen (round 50, F.5: accepted) |

## Implementation

M5 part A builds Rook and the escort slot; its production art, the barks' voices and the AI as
built were accepted in concept round 28.

- [x] Escort slot unlocked by a story flag: Rook hired when the hangar opens with Level 08 or later next; the save's `escort` (format version 3, see [saves](../../systems/saves/README.md)) (`Campaign.hireWhenDue`, `SaveFormat.migrateFrom2`; tests in `EscortTest`)
- [x] Rook flies only in levels from 08 on (not in Act 1 replays from mission select, not on `--level` below 8) (`Campaign.escortFlies`; `EscortTest.heFliesFromLevel08ButNotInAnAct1Replay`)
- [x] Rook AI as in *Rook's AI*: formations Wing / Wide / Trail with the 0.6 s glide and the mirrored slot at the edges, reaction delay, dodging, targeting priority, firing cone, fire whenever the player fires; deterministic (state hash, replay test) (`vanguard.sim.Wingman`; tests in `WingmanTest`: the formations, the mirrored slot, Trail on rear chains, the 70 % dodge, the body clearance and the 40 px, his fire; the same commands give the same state hash and a run without him keeps its own, the recorded `ReplayTest` run flies without him)
- [x] Collisions and damage: enemy bullets and `air` contacts on his 11×11 hitbox, 20 to the enemy, `tiny`/`small` rammers destroyed (`Sortie`'s wingman hits: bullets, mines and air contacts, a lasting contact hurting once)
- [x] Rook's four guns derived from the player weapon tables (scale on damage, one muzzle, no overdrive), with their 60 % prices and upgrade costs (`SimSpecs.wingman`; `WingmenDataTest`, `EscortTest.hisGunsCostSixtyPercentOfTheirBaseWeaponsAndDrawNoPower`)
- [x] Escort inventory in the hangar: buy, upgrade each gun L1–L5, fit one, sell at the sell-back share, undo in the visit (see [hangar](../../ui/hangar/README.md))
- [x] His side setting (left/right) in the hangar, saved
- [x] Armour kept between levels; the hangar's Rook repair line at the difficulty's repair cost; launch warning below 50 %; grounded at 0
- [x] His kills pay like the player's: bounty, score, chain, objectives, statistics (his shots are the player's shot pool with his mount, so they share the kill path; `WingmanTest.hisKillsPayLikeThePlayersAndChain`)
- [x] Eject: the pod drifting off to the nearer edge unless the ship is in the way (36 px), over the explosion after 0.1 s with its beacon glow and smoke trail, the `medium` explosion, out for the level, never a failure; the eject bark urgent and exempt from the spacing
- [x] Retry with his level-start state (and the armour floor); the boss checkpoint includes him (`WingmanTest.atZeroArmourHeEjectsWithoutFailingTheLevelAndARetryBringsHimBack`, `theBossCheckpointKeepsHisArmourAndARetryFromBossBringsHimBackInIt`, `EscortTest.aRetryRaisesHisArmourToTheFloorButLeavesAGroundedRookHome`)
- [x] Wingman radio barks: the eight triggers with their variants, priority, 8 s spacing and the event-line queue rules; voiced (27 lines, accepted as rendered in concept round 28)
- [x] HUD escort box (see [HUD](../../ui/hud/README.md))
- [x] His craft in the level: banking frames, engine flames, runtime shadow, muzzle flash, hit flash, smoke when low, the eject (see *In the game*); the placeholder Ember frames, glow and drawn pod remain as fallbacks
- [x] Debug option `--escort rook:<gun>:<level>[,side=left|right]` and `--escort none` (`DebugFit.withEscort`, never saved; `LaunchOptionsTest.aDebugEscortFliesRookWithHisGun`, `EscortTest.theEscortDebugOptionFliesHimOnAnyLevelOrKeepsHimHome`, `theEscortDebugOptionIsNeverSavedNorKept`)
- [x] His data in [data.yaml](data.yaml) with its loader and validator (`WingmenData`, `CampaignChecks.checkWingmen`; `WingmenDataTest`)
- [x] The guns and barks tables above rendered from [data.yaml](data.yaml) by `tools/sync_tables.py` (`rook-guns`, `rook-barks`; M6 part D: the Available cell now reads the data's `act 2` where the hand-written table said L08)
- [x] `BalanceTest` prints his DPS (the sheet's `Rook` column, his gun's single-target DPS, not in the forward DPS; `tools/balance.py` prints the same `Rook DPS`); the balance plan buys his guns from L08 and `ActPlaythroughTest` pays his repairs at every visit from L08 after the ship's; the autopilot and `ActPlaythroughTest` fly with him (Acts 1–2 as one campaign on every difficulty, his armour kept between levels, his retries with the armour floor) — M5 part I
- [x] Production sprites of Rook's craft (5 banking frames, Ember, 42×42), his engine flame, the eject pod and the engine mounts (`tools/art/rook.py`, approved as final in concept round 28)
- [x] His shots in their base weapons' families: drawn with the base weapons' final shot sprites and muzzle flash at his nose (`WeaponLooks` builds his gun's look by its base weapon's slug; `WingmanLooks.drawMuzzle`)
- [x] The radio event `escort-first-kill`: his first kill per attempt, only while he flies, never after an eject, the flag in the state hash and the boss checkpoint; loader, `SimSpecs` and schema text (M5 part B, D4)
- [x] `{side}` in a radio line: one take per side in the voice line list, the take and subtitle of his side played (M5 part B, D4)
- [x] Trail on a Wraith `rear ambush` and a Mote Swarm loop-back from their re-entry, the rear bark
      1.5 s ahead of it; a Rook event line on `first-loop-back` counting for the bark spacing; a
      `WingmanTest` case for Trail near the Wraiths' hold band (M5 part D, Level 10)
- [x] Level 11 (M5 part E, step E2a): his target pick skipping `sub` units, which are no flank
      threat either (`SubLayerTest`)
- [x] Level 11 (M5 part E, step E2c): his slot and dodges keeping out of a telegraphed lane until
      its impact (his goal moved beside the run of telegraphed lanes, on the side nearer him; only the
      impact hurts, so a slammed lane is safe again); a slam's impact hitting him; the cases in
      `SlamArenaTest` (`rookKeepsOutOfATelegraphedLane`, `aSlamHitsRookInItsLane`)
- [x] Bulwark heavy drone (was "Warden"): formation, cannon (`anti-sub` under water), draw-fire rule, the free loaner — **later: Act 4** (unlocks at L22) (M8 part A, step A2: its data (`bulwark`), the campaign's side of it: bought once for 6 000 and sold back, chosen for the slot (`Hangar.buyBulwark`/`sellBulwark`/`chooseEscort`), its repair at 8 cr a point (`repairBulwark`), its armour kept between levels and a destroyed one rebuilt, the loaner, save format 6; `ActFourEscortTest`; the simulation in step A3, `BulwarkTest`; the flight in step A4, `Act4ArsenalFlightTest.theBulwarkFliesWithItsCannonAndALoanerWhileRookIsMissing`; the hangar and the look in steps A5–A6, `ActFourHangarTest.theBulwarkIsBoughtFittedUnfittedSoldAndUndoneOnItsRow`, `DeepWaterLooksTest.theBulwarkShowsItsHitThenScorchesUnderHalf`; the finals accepted in [round 50](../../concept-rounds/round-50/README.md))
- [x] Drone behaviours: orbit, trail, block, rebuild for the Act 3 pair (light drone L15, rear-guard drone L20) — specified under *Light drone* and *Rear-guard drone* (draft); the light drone's
  orbit, block, break and rebuild built in M7 part A (`vanguard.sim.Drones`, `DronesTest`), the
  rear-guard drone's trail waits for part G (its bay's data and the `station: trail` keys written in
  step G2; flown from step G3, mapped in step G4)
- [ ] The hunter drone's behaviours — **later: Act 6** (was L29; moved with Act 5's arsenal, Q12 = c of round 60, decided)
- [ ] Rook's rescue in L29 as in *Rook's rescue and return* (draft): the mid-level swap (the drone peels away, Rook
  joins with his gun at full armour, his first line, his barks back), the loaner returned after L29, the debug
  starts — M9 (L29's part) (M9 part A, step A2: the campaign's side: at Level 29's win Rook is back at full armour
  and the slot defaults to him, the loaner gone from the L30 visit, an owned Bulwark kept with its armour,
  `BeltCampaignTest`; the swap in the level is part B's) (M9 part B: the swap flown by the simulation, step B1; step
  B2 the content: Level 29's `escort_swap`, `Campaign.EscortFlight.rescue` (the loaner or an owned Bulwark with Rook's
  gun fitted at his loss, full armour) into `Loadout.withRescue`; `--escort` starts never carry a rescue;
  `BeltCampaignTest.levelTwentyNinesFlightCarriesRooksRescue`; the looks, radio and debug runs are step B4's)
- [ ] Rook missing for L27–L29 (only a heavy drone in the slot), back from L30 — Act 4's part built (L26's loss, L27–L28 with the loaner; M8 part I: `ActPlaythroughTest` flies them to the release's end), **later: Act 5** for L29 and his return at L30 (draft: *Rook's loss and absence*; the scripted loss at L26's end, the MIA card, the loaner, per-level availability in `Campaign.escortFlight()`) (M8 part A, step A2: `Campaign.escortFlight(level)` and `rookMissing(level)` from `rook.missing`, the story flag `rook-missing` from Level 26's win to Level 29's, the hangar's MIA rules (`Hangar.rookMissing`: no gun rows, no repair line), `Campaign.rookFlies` for his `requires: escort` lines; a `--level 27` start treats him as missing; the scripted loss is part F, the MIA card part A5) (M8 part F: the scripted loss at Level 26's end, `RookLossTest`, `RookLossLooksTest`, `Level26Test.rooksLossAndItsReplayOnARetry`; the debrief's `ROOK  MIA` row; the campaign playthrough's Level 26 win records him missing and the Level 27 visit has the loaner, `ActPlaythroughTest`; his absence through L29 and his return at L30 wait for Act 5's first levels)
- [x] Rook under water: the hydro-kit's drag, his guns × 0.7, his formation in the trench walls and the dark (his lamp) — **later: Act 4** (M8 parts A–F: `UnderwaterTest.rooksGunsDoSevenTenthsAndHisCraftTakesTheDrag`; the walls' 24 px clearance of the canyon play, `RookCanyonTest`, in Level 25's trench; the Siren's slow on him, `Level25Test.theRingsSlowTheShipAndRook`; his lamp in Level 26's dark, `Level26LooksTest.onlyLevel26IsDark`)

- [x] Level 12 (M5 part F, step 3): the wind pushing him (positions, edges clamped); his shots
      passing a latched Lamprey and his pick skipping it; no sonde collection; a test case in a gust
      (`WindTest`)
- [x] Level 12: the plan's L12 visit refitting his Autocannon; the autopilot flies the level with him
      on every difficulty (`Level12Test`) (M5 part F, step 4)

- [x] Level 13 and Level 14 with him: the lines above, Trail for the maw's Wraiths, the autopilot
      flying both levels with him on every difficulty (`Level13Test`, `Level14Test`, `MawLaunchTest`)
      (M5 parts G and H)

## Open questions

- Should Rook ever be killed for good in the story? The current story keeps him alive (missing
  L27–L29 only); see [Rook](../../story/characters/rook/README.md).
- While Rook is missing, the draft allows a heavy drone in the escort slot. The alternative is
  to lock the slot completely for those levels, which makes his absence hit harder.
- Q8 of [round 60](../../concept-rounds/README.md) (Rook's rescue): recommended (a), on the screen in
  L29 (above). Alternatives: (b) radio only: loyalists free him off-screen, his line at L29's end, he
  flies from L30 (cheap, but the on-screen loss gets an off-screen return); (c) told in the L29 debrief
  or the L30 briefing only (the flattest).
- Q12 of round 60: the Hunter drone to Act 6 (recommended c); under (b) it stays at L29.

## Decisions

Entries dated before 2026-10-21 (M8's start): [decisions-archive.md](decisions-archive.md).

- 2026-10-21 (Act 4 design phase, step m8-dsystems; **decided in round 48 (2026-10-22)**): the heavy drone "Warden" is renamed **"Bulwark"** (it clashed with Act 3's Warden Tank); its cannon is `anti-sub` under water; one Bulwark is loaned free while Rook is missing (L27–L29) so the slot is never empty; Rook's loss is a scripted on-screen shoot-down at L26's end (Q7 = a; alternatives: radio only, or told in L27's briefing); Rook under water flies with the same drag and the × 0.7 damage rule.
- 2026-10-22 (M8 part A, step A2; small rules decided by the gap check, accepted in round 50 (2026-10-22)): the
  Bulwark's economy (D10): an owned Bulwark's armour is kept between levels and repaired at 8 cr a point on every
  difficulty, a destroyed one is rebuilt whole and free for the next level, the loaner flies whole and keeps nothing;
  buying it chooses it for the slot, selling it gives the slot back to Rook; with an owned Bulwark chosen and Rook
  grounded nobody flies (the choice is the player's); an owned Bulwark takes the slot while Rook is missing, the
  loaner only when none is owned. Draw-fire (D11) is the simulation's (step A3).
- 2026-10-22 (M8 part A, steps A3–A6; accepted in round 50 (2026-10-22)): the Bulwark reaches its slot in 0.4 s and settles in 0.25 s; it fires only while the player fires, its shell at 500 px/s (not in the data); boss beams, cones and vents pass through it; rammers die on it without ram damage; the draw-fire rolls 30 % per aimed **volley** (not per bullet as D11 had it), and turret barrels, aim-along fans and the convoy's shots are never drawn; its cannon plays the ballistic shot at pitch 0.8, its hit the metal hit at 0.85, its loss the drone break at 0.75 with the medium explosion.
- 2026-10-22 (user, [round 50](../../concept-rounds/round-50/README.md) closed): F.5: the Bulwark's final art and game capture accepted, the concept rows `chosen`. B.1 (the draw-fire per aimed volley, 30 % within 150 px), B.3 (Rook and the Bulwark in one slot, the free rebuild) and B.6 (how the Bulwark fights) accepted.
- 2026-10-24 (the user's playtest of Level 26, round 55 C.2 changed): Rook's spin after his loss plays on the screen.
  Flying low beside the ship he used to fall off the bottom edge in about 0.7 s; now the hits toss him up a little and
  he sinks back faster and faster, crossing the bottom edge as the 2.5 s spin ends at 1.25× the scroll (from high on the
  screen, where that speed alone takes him off in time, a steady fall); his cut line comes before the hits, which cut it
  ([Level 26](../../campaign/act-4-deep-water/level-26-blackwater/README.md#decisions); `RookLoss`, `RookLossTest`).
- 2026-10-25 (M8 part G, steps G1 and G5; pending the user's review in [round 57](../../concept-rounds/round-57/README.md) C.1):
  in Level 27 the Bulwark keeps out of a plume's disc while it glows or burns and off every homing torpedo's line over
  the next second (`BulwarkHazards`, `BulwarkHazardsTest`), aims its shell at the nearest Depth Hunter while one is on
  the screen, is burnt by a plume like the ship and hit by the torpedoes, and never draws them (aimed bullets only).
- 2026-10-27 (user, the Act 4 playthrough done): a mission-select replay flies the replayed mission's escort, and a
  replay while Rook is lost (after Level 26) flies him at full armour with no repair warning (step K2, `8de88791`;
  [mission select](../../ui/mission-select/README.md#decisions)): decided, no longer pending.
- 2026-10-27 (Act 5 design phase, step m9-d-systems; **approved in round 60**): Rook's rescue in
  L29 on the screen (Q8 = a): the clamp guns of Dock 9's prison tender, his Ember launching at full armour with the
  gun he had, the drone in the slot peeling away, his first line and barks back; no lines before it; the loaner goes
  back to the CDF after L29 (an owned Bulwark stays); Rook in the slot by default from L30. The Hunter drone moves
  to Act 6 (Q12 = c).
- 2026-10-27 (user, [round 60](../../concept-rounds/round-60/README.md) closed): the Act 5 section approved as drafted (D.16: Rook's rescue at Dock 9 and his return to the slot, Q8 **a**; the loaner Bulwark leaves). **QA-1** (decided 2026-10-27 by the main agent, confirmed in round 61 on 2026-10-28): Rook locks onto a homing diamond only when no enemy unit is in his cone.
