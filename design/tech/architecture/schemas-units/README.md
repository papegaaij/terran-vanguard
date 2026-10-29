---
title: Data file schemas — enemies and allies
design: approved
implementation: in-progress
art: n/a
updated: 2026-10-29
---

# Data file schemas — enemies and allies

## Summary

The schemas of the enemies' stat blocks and boss scripts and of the allies' file. Moved unchanged from the [architecture](../README.md#data-file-schemas)'s Data file schemas, which keep the shared conventions (keys, units, sizes) and the index of every schema.

## Design

- **Enemy** (`enemies/<category>/<slug>/data.yaml`): the stat block of the
  [enemies](../../../enemies/README.md#stat-block-template) template: `name`, `faction`, `layer`,
  `tier`, `size`, `hitbox`, `parts` (`single` or `multi`: a multi-part unit lists its
  `part_list`, each part a `name`, an `offset` `[dx, dy]` from the centre in its own frame facing
  down (dx right, dy up towards the tail), a `hitbox`, `hp`, `kind` `destroyable`/`vital`/
  `armoured`, `bounty` and the `attack` it fires by name; the parts' HP and bounties add up to the
  unit's), `orientation`, `hp`, `armour`, `speed`, the optional `machine` (M7 part A: `true` for a
  machine, which the EMP Burst stuns longest; every other unit is biomechanical), the optional
  `seekers_ignore` (M7 part G, user decision 2026-10-20: `true` for a unit no seeker picks as a target, the
  Mote Swarm's motes: the player's homing shots and turrets and Rook pass it over; a shot that flies into it
  still hits; mapped to `EnemySpec.seekersIgnore`), `movement` (one
  entry per pattern: `snake` `spacing`; `swoop` `radius`, `top_speed`; `straight` `speed`;
  `hover` `seconds`, `y`; `orbit` `radius`, `turn_rate`; `strafe` `y`, the height a convoy turns
  across at; `spiral_out` `seconds`, `turns`, `growth`, `drift`, `ricochets`;
  `drift` `speed` (straight down, no intent); `sine` `amplitude`, `period` (a side-to-side offset
  on another pattern); `walk` `speed`, `turn_rate`, `stride` (px of ground per walk cycle), the
  path coming from the wave), `attacks`
  (`pattern` `aimed`/`fan`/`mine`, an optional `name`, `bullet` class, `interval`, `speed`,
  `first_shot_delay` (for parts sharing an attack: the stagger between them), a fan's `count` and
  `spread`, a mine's `mine` with `arm`, `life`, `drift`, `hp`, `ring`, `ring_bullet`, `credits`;
  a fan's `aim` (`target`, the default, `down` or `facing`; an aimed burst's `up`, see M5 part D), an aimed attack's
  `away` (fires only while the player is more than this many ° off its facing), and the pattern
  `spawn` with no `bullet`, `interval` or `speed` but a `spawn` block: `enemy`, `count`, `after`
  (s from entering to the self-burst), `telegraph` (s), `arc` (°), `speed` (the released units')
  and `burst_bounty` (credits for a self-burst, which is not a kill)),
  `formations` (`name`, `size` `[n]` or `[min, max]`),
  `weak_points` (`name`, `multiplier`), `traits`, `bounty`, `first_level`, `difficulty` hooks
  (`easy`/`hard`: `fan_count`, `dive_pause`, `burst` (aimed attacks), `leads_target_in`, a mine's
  `ring` and `mine_bursts`, a `death_burst` with `count`, `speed`, `bullet`;
  a spawner's `spawn_count` and `spawn_after`). `armour` is a text, or a mapping
  with `front_arc` (° each side of the facing from which direct shots glance; dropped bombs,
  lobbed shells and specials ignore it).
  `drops` (`pickup`, `every`: every n-th kill of the enemy in a level drops it).
  Easy/hard HP in the table are derived from the difficulty levers (rounded half to even, at
  least 1), the
  contact and bullet damage from the basis, the score from the bounty.
  For the Polyp Mortar and the Gorgon Frigate (M4 part E; both flown in Level 05): the attack
  patterns `mortar`
  (its `bullet` is the direct hit, `speed` the ring's; a `mortar` block with `marker` (s the
  impact marker shows ahead, the blob's flight), `impact` (px, the direct-hit circle's diameter),
  `ring` and `ring_bullet`; the difficulty hook `ring` applies to it as to a mine), `ring`
  (`count` bullets) and `spiral` (`arms`, `interval` between two bullets of an arm, `turn_rate`,
  `duration` s); on an aimed attack `burst` (shots per volley), `burst_gap` (s between them) and
  `rotate: true` (the parts sharing it take turns, one volley every `interval` among the living
  ones); a part's `multiplier` (its damage multiplier as a weak point); `hover` without `seconds`
  (holds until killed); `chains` (articulated necks: `name`, `from` (the anchor offset on the
  body), `to` (the part at its end, whose `offset` is the chain's rest end), `segments`,
  `hitbox` per `armoured` segment, `lag` (s of follow-through per segment), `bend` (° it may turn
  towards the player)); the difficulty hook `attacks` (changes per attack name, such as
  `burst` or `count`); and a `boss` block: `kind` (`boss`/`mid-boss`; a mid-boss has the short
  bar), `bar_name`, `par` (s from the bar appearing to the kill, the Boss rush bonus), `phases`
  in order, each a `name`, `until` (`parts` and how many of them are `left` alive when it ends),
  the `attacks` it fires by name or an `alternate` list (each attack runs its `interval` or
  `duration`, then hands over), optional `streams` (`enemy`, `count`, `every` s, `interval` s
  between units, `edge`; the first at the settle), `exposes` (parts that take no damage before
  this phase) and `bend` (the chains' bend in this phase). During its entrance movement a boss
  takes no damage. `hover: {y}` may be one height instead of `[min, max]`.
  For the Mantis and the Coilwyrm (M4 part F; flown in Level 06): the attack pattern
  `laser-sweep` (its `bullet` is the beam's class, damage once per sweep; no `speed`; `aim`
  `target` centres the sweep on the ship's bearing when the telegraph starts; a `sweep` block
  with `arc` (°), `duration` (s), `telegraph` (s, the arc shown ahead), `length` and `width` (px), and an optional `origin: [in, down]`
  (px from the unit's centre to where the beam starts, toward the field and down, mirrored on the
  right edge; default the centre));
  `hover` `edge_x` (px from the side edge it entered by) and `exit: back` (it leaves through that
  edge); the movement `path` with `speed` (the head flies the wave's authored path); a
  `segment_chain` block for a chain of segments following the head's path history: `segments`,
  `size` (the first and last segment's sprite, tapering), `hitbox_share`, `spacing` (× the
  segment length), `hp` and `bounty` per segment, `contact` (the segments' and tail's contact
  class), `regrow` (`seconds`, `speed`, `hp`, `bounty`: the rear part's new head after a cut,
  once per chain) and `pop_interval` (s per segment of the chained death); the unit's `hp` and
  `bounty` include the segments; a part's `first_bonus` (credits when it is destroyed before the
  vital part; the tail) and the difficulty hooks `sweep_arc`, `segments` and
  `regrown_fan_count`. A segment chain counts as one enemy (density, kill ratio): the simulation's
  unit is its head (the vital part's HP and bounty), its segments, tail and regrown head are kinds
  of their own (`<slug>-segment`, `-tail`, `-regrown`) that pay their bounty but are not kills; a
  hard `attacks` change may set an attack's `interval` (authored, so the fire-rate lever does not
  apply on top); `hover` `seconds` may be one time instead of `[min, max]`.
  Planned (part G), for the Brood Carrier (flown in Level 07): the stat block's `layer` is the
  layer a boss arrives on (`high-air`: only `homing` shots reach it, drawn at the 1.25 high-air
  scale, no contact); a boss part of `kind: armoured` fires its `attack` but is never damaged, has
  no `hp` or `bounty` and is not in the bar (the head turrets); the pattern `fan` for a boss
  attack; a `spiral` without `duration` runs for its whole phase (beside a `ring` in a
  non-`alternate` phase, each with its own spiral state); the difficulty hook `attacks` gains
  `arms`, and a hook `spawns` sets a window spawn's `count` by its `name`. In the `boss` block:
  `engages_on_arrival: true` (the first phase starts when the boss arrives, its entrance part of
  the fight, rather than when it settles); `death_seconds` (s the chained death runs, the parts
  bursting in `part_list` order, tail to head; parts still alive then burst and pay); `poses`, the
  further part layouts (the arrival pose is the `part_list` offsets with the stat block's
  `hitbox`), each a `name`, the body's `hitbox` and `offsets` (part
  name → `[dx, dy]` in this pose; parts left out keep their `part_list` offset), drawn from their
  own pre-rendered sprites, never rotated. Per phase: `until` may give `seconds` (from when the
  phase engages, after its move) beside or instead of `parts`/`left`: a phase with only `seconds` is
  timed, one with both times out (a **timeout**) when the seconds run out first; `delay` (s after
  the move before it fires and opens its windows; default 1 s in a later phase that
  alternates, 0 otherwise: the frigate's crown opening, generalised); `move`, the invulnerable opening move: `to` `[x, y]`
  (the centre's station, px from the left edge and px below the top edge, like `hover.y`), `layer` (the layer it descends or rises to), `descend` (s), `pose` (the
  pose's name) and `turn` (s through the turn's pre-rendered frames); `windows`: `groups` (lists
  of part names, in the order the windows cycle), `every` (s), `open` (s a group stays open),
  `offset` (s before the first, after the delay), `all: true` (every group with a living part
  opens at once), and `spawns` in turn, one per opening, each a `name`, `enemy`, `count` per
  group (a group with parts destroyed releases its share rounded up, an empty group nothing),
  `speed`, `arc` (°) and `glide` (s); a part in a window group takes damage only while its group
  is open.
  M5 part B, for the [Creeper](../../../enemies/ground/creeper/README.md) (flown in Level 08): a
  walker's `fan` with `aim: target` (the default) is aimed at the player, as a flyer's, and with
  `aim: facing` along its facing (the Scuttler; `down` is rejected for a walker); a walker's fan's
  `stagger` (s, positive; only a walker's fan has one): the units of one wave share a volley clock
  that starts as the first of them comes onto the screen, the first volley half an interval later
  and then one every interval, unit *i* (from 0, in entry order) firing *i* × `stagger` after the
  volley's start, a unit off the screen skipping its turn (a wave whose last unit's turn would
  reach the next volley is rejected when the level is built); and the difficulty hook `attacks`
  with an `interval` on any named attack, an ordinary unit's included: authored, so the fire-rate
  lever does not apply on top.
  M5 part C, for the [Hive Node](../../../enemies/ground/hive-node/README.md) and the
  [Ravager](../../../enemies/ground/ravager/README.md) (flown in Level 09): `armour: hardened` flies for an
  enemy as for a hardened ground object (only `anti-ground` deliveries, the Airstrike and the
  Smart Bomb damage it; other shots and blasts glance, `SHOT_GLANCED`; a homing shot's lock-on and
  Rook's target pick skip it unless the weapon is `anti-ground`); a **periodic `spawn`**: the
  `spawn` block with `every` (s between releases, the first `every` s after the unit's centre
  crosses the top edge) instead of `after` and `burst_bounty` (a periodic spawner never bursts on
  its own), its `telegraph` (s the iris opens ahead of a release), `enemy`, `count`, `arc`,
  `speed` and `shut_within` (px: an opening due while the ship's centre is this close is skipped,
  and so is a release the ship came that close to during the telegraph; the next one waits a whole
  `every`; M8 part D: `ram_after`, s on the field before each released unit of a kind with a flock's
  `ram` draws it, instead of the kind's own: the spawning reef's fry, 0); a unit has at most one `spawn`; the released units count into the level's enemies as they
  are released (as a boss's streams), the density and haul checks count one release per spawner,
  and the hook `spawn_count` sets the count; the attack pattern **`pounce`** (no `bullet` or
  `speed`; its `interval` is the s from landing until it may leap again, by the fire-rate lever
  unless the hook `attacks: {pounce: {interval}}` authors it, as hard's; a `pounce` block: `range` (px, centre to centre,
  that starts a leap while the unit is on the screen), `leap` (s, aimed at the ship's position at
  take-off, no homing), `air` (s in the middle of the leap on the `air` layer: the unit's
  **current layer**, read at every hit, contact and targeting site instead of its stat block's;
  contact of its tier's class with the ship or the escort once per leap; ×1 from air-reaching
  weapons, ground-only blasts miss, mines trigger) and `scale` (the drawn scale at the apex));
  only a walker pounces; and the formation **`pack`** (see *Level*; the planner flies it).
  M5 part D, for the [Wraith](../../../enemies/air/wraith/README.md) and the
  [Mote Swarm](../../../enemies/air/mote-swarm/README.md) (flown in Level 10; user decisions D6–D8 of
  2026-10-08): a **`cloak`** block (`layer`, the layer from its decloak on, the stat block's `layer`
  being the cloaked one (`high-air`: only homing hits it, no contact, specials skip it); `flash`, s
  of the decloak flash at its hold point, on `layer` from the flash's start and its gun silent
  until the flash ends; only with an `ambush`, to another layer): the unit's **current layer**, as
  the pounce's, read at every hit, contact and targeting site; the event **`DECLOAK`** at the
  flash's start (the flash, its sound, the radio event `first-decloak`); the movement **`ambush`**
  (`gap`, s below the bottom edge; `lane`, px from the nearer side edge; with a `hover` with its
  `seconds`, a `straight` and `rear ambush` among its formations): the path of a unit of a `rear
  ambush` wave, cloaked straight down its lane at the stat block's `speed` past the ship and off
  the bottom edge, `gap` s below it, up to its `hover.y` at that speed (a height in the range per
  unit, seeded), the flash and its `hover.seconds` hold (its aimed attack's `first_shot_delay`
  counted from the stop; two volleys in the hold, the `interval` counted from a burst's start, the
  burst's shots `burst_gap` apart: the attack's own `burst` and `burst_gap`, read for any aimed
  attack, 0.15 s without one; an aimed attack's **`aim: up`** with a **`spread`** (°; 2026-10-08,
  the user's choice for the Wraith) sends each burst straight up the screen as a fixed fan, its
  shots in turn from the fan's left edge to its right, aimed at nobody, without the difficulty's aim
  spread and without the 72 px no-fire distance), then out to the nearer side lane (`lane` px from that edge, a tie to
  the left) while it climbs 60 px and straight up it from 120 px above its hold point, at its
  `straight.speed`, off the top edge; the difficulty hook **`hover_seconds`** (hard's 3.0 s hold);
  the movement **`flock`** (`separation`, px; `radius`, px of the neighbourhood; the weights
  `alignment`, `cohesion` and `leader` (the pull toward the leader point); `speed` and `dive_speed`
  (after a loop-back), px/s; `turn_rate`, °/s; `max` members, at most 24; with `swarm` among its
  formations): the members of a `swarm` wave steer by the three rules among the members within
  `radius` and toward a **leader point** that flies the wave's route at the unit's `path.speed` and
  its loop-backs at `dive_speed` (the point is not a unit: killing members never stops it); each
  member turns at most `turn_rate` and flies at 0.8–1.3 × the cruise (`speed`, `dive_speed` after a
  loop-back) as it is near or far from the point, and pairs closer than `separation` also move
  apart by 0.3 of the shortfall a step (the separation as a soft constraint, so the cloud reads as
  motes); a fixed iteration order (the members in entry order, every heading from the positions at
  the step's start), `StrictMath.atan2` and the `Trig` table, fixed arrays sized for 24, no
  allocation per step (`FlockTest`), a killed member dropping out; members off the play field at a
  loop-back re-enter with the point as the cloud they flew in; after the route's last path the
  members off the play field are gone (escaped), those still on it 6 s later too; every member an
  ordinary unit (a kill, a bounty, a chain step, a unit of the level's density); the event
  **`LOOP_BACK`** (value: the loop-back's number) when the leader point re-enters below the bottom
  edge, as its warning ends (the swarm's sound, the radio event `first-loop-back`).
  M5 part E, for the [Driftjelly](../../../enemies/naval/driftjelly/README.md), the
  [Reef Spitter](../../../enemies/naval/reef-spitter/README.md) and the
  [Harbour Kraken](../../../enemies/bosses/harbour-kraken/README.md) (flown in Level 11; user decisions
  E2–E7 of 2026-10-08): the layer **`sub`** (built in step E2a: appended to the layers, so the
  existing ordinals and replay hashes stay; hit only by `anti-sub` deliveries and the Smart Bomb, no
  contact, never picked by homing shots, turrets or Rook; `content` rejects it as a stat block's or
  a cloak's `layer`, a unit only dives to it); built in step E2b (`EnemySpec.Submerge`, `ProximityRing`, `drift`): a **`submerge`** block on a unit
  whose stat block's `layer` is `ground` (`every` `[min, max]` s between swaps, drawn per unit from
  its own seed; `swap` s, the layer flipping between `ground` and `sub` at the swap's middle; `start`,
  the share of a wave's units that begin submerged): the unit's **current layer**, as the pounce's
  and the cloak's, its timer on the real steps (it keeps going in a halted arena), the simulation
  telling each unit's swap progress for the looks; a `ring`'s **`within`** (px: it fires only while
  the ship's centre is this close, on either layer, its `interval` the cooldown from the last ring;
  the hook `attacks` may set `count` and `within`, hard's 12 and 110); the movement `drift` flying a
  `field` wave (see *Level*); `terrain`'s **`drift`** (px/s along its nest's `current`: a raft on
  the water). Built in step E2c (`BossSpec.Arena`, `SlamArena`; `KrakenTest`, `SlamArenaTest`), the
  `lanes` loaded and checked since step E2b for a level's convoy (`EnemyData.Lanes`): for an arena boss the movement **`anchored`** (`y`, px below the top edge where its
  centre stops when the scroll halts, negative above it (round 40: the Kraken's −46); no descent, no `hover` or `straight`): it is placed on the ground layer
  that far up the scroll from the halt (the halt scroll: the scroll at the arena's start plus the
  ease, `Sortie.easeDistance`), scrolls in with the ground invulnerable and without its bar
  (`SetPiece.approaching()`), arrives at its `t` (the arena's start: the bar, the sting, the boss
  checkpoint) and engages at the halt (`BOSS_SETTLED`); its stat block's `layer` is the sea's surface
  (`ground`), its `hitbox` the platform's deck (no armour: shots fly over it), the lanes running from
  its lower edge at the halt down to the bottom edge; a part's **`layer`** (`sub` or `ground`, where it starts; the script
  moves it) and **`spots`** (`name`, `offset` `[dx, dy]` px from the part's centre, `hitbox`, `multiplier`, `while: open`: a sub-box of
  the part that routes damage to the part's HP at its multiplier, only while the part's surfacing
  window is open, a shot counting the spot on its line ahead of it; the part's own `multiplier` elsewhere: the Kraken's eyes ×2 on a mantle of ×0.5);
  a chain's **`motion: slam`** (its segments follow the slam cycle instead of bending toward the
  player: at rest a line from its anchor to its part under the water, from the rise until the sink
  ends laid along its lane from the lanes' top to 24 px above the bottom edge, its segments evenly
  spaced; they are the part's own hit boxes, not armour; only a part an arm of the `lanes` names). In the `boss` block: **`lanes`**
  (`count`, `width` px from the play field's left edge, lane 1 first; `arms`, each slam arm's part
  name → the lanes it owns, E5 = a; only for an anchored boss, with a `slam`; round 40: **`rooted: true`**, every arm a
  rooted one-lane arm whose part is its root, a `ground` part that stays at its offset, always hittable, the lanes
  running from the roots' lower edge, a cut root ending its lane's slams, and **`raised`**, the root's damage
  multiplier while its arm is raised, only with `rooted`; `BossSpec.Lanes.rooted`, `KrakenTest`) and **`slam`** (`telegraph`, `rise`, `awash`, `sink` s;
  round 40's optional **`volley`**, the lanes a volley slams at medium, one per arm when not given;
  `damage`, the impact's class on the ship and the escort in the lane, once per slam; `splash`
  `count`, `speed`, `bullet`: from evenly spaced points along the arm, out to either side in turn,
  15° down; `choose: alternate`, the player's lane and the lane of the convoy unit
  nearest the player's x in turn, a tie to the lower lane, a choice in a severed arm's half going to
  the other choice or the living half's lane nearest the player; the hooks `telegraph` and `lanes`,
  hard's 1.1 s (0.8 until round 40) and three lanes a volley, a second lane of one arm 0.5 s after its first; the arm on
  `ground` from its rise until its sink starts); per phase
  `until` may give **`slams`** (how many impacts end it) and, with its `parts` instead of a `left`, **`below`** (the share of their
  HP under which it ends), beside `seconds` and `parts`/`left`, the first met ending it (a phase that
  ends on slams also ends when every arm is destroyed); a phase's **`slamming`** (`chain`: one at a time,
  the next telegraph when the arm has sunk, the first after the phase's `delay`; `after_dive`: one after each dive; `{mode: volley,
  every: s}`: one per living arm at once, the first after the phase's `delay`, an arm still in its
  cycle sitting one out); **`surface`** (`part`, `rise` s crown first, the layer
  flipping at its middle; `open` s, the eyes' window; `dive` s; `glow` s, the tell before each of
  the phase's attacks, which fire only while it is up, from its hit box's lower edge, the first
  `glow` s after the window opens, then one every interval; `stay: true` keeps it up, without
  `open` or `dive`, turning a dive round); **`release`** (`enemy`, `count`, `formation: field`,
  `lanes`, `on: first-surface`: units released once as the part first starts to rise, placed in
  rows down the lanes, counted as a boss's streams); and **`drop`**
  (a pickup dropped under the surfacing part as the phase begins: the shield cell). Events **`TELEGRAPH`** (value: the lane;
  the radio event `first-telegraph`), **`SLAM`** (the impact; value: the lane), **`SURFACE`** and
  **`DIVE`** (a surfacing part's; value: the part), and the part events the radio's `boss-part-destroyed` reads.
  M5 part F, for the [Lamprey](../../../enemies/air/lamprey/README.md) (flown in Level 12;
  user decisions F3, F4 and F9 = a of 2026-10-09; the simulation built in step 3, the loader's
  mapping built in step 4): the movement **`chase`** (`speed` px/s,
  `turn_rate` °/s, `seconds`: it steers toward the ship's centre from its entry for that long, then
  flies straight on at `speed` along its heading); a **`latch`** block (`slots`, how many may be
  latched at once; `spots`, the hull offsets `left`, `right` and `tail`, each `[dx, dy]` px from the
  ship's centre, y up as the muzzles' (Level 12: left `[-22, -13]`, right `[21, -13]`, tail `[0, -35]`), the tail for a touch within 45° of straight down, else the side by x;
  `damage_factor`, the damage it takes while latched (2); `shake`, `reversals` and `within` s: that
  many reversals of the commanded horizontal direction, neutral steps ignored, throw off every
  latched one; `stun` s and `throw` px/s, its outward start speed slowing to 0 over the stun; it
  then flees at its `speed` to the nearer side edge, never latching again, and neither stunned nor
  fleeing collides with the ship); and the attack pattern **`drain`** (`rate`, shield points a
  second while latched, on the shield only, pausing its regeneration, stopping at the break; no
  `bullet`); the hooks `drain` (a rate) and `latch_slots`. While latched it is hit only by rear-mount
  shots, shots of a `side` or `rear` weapon and blasts that reach `air`; homing shots and the escort's
  pick skip it. With every slot taken a touch is its tier's contact. A unit with a `latch` takes the
  wind (a level's `weather`) in every state but latched. Events **`LAMPREY_LATCHED`** (value: the
  spot) and **`LAMPREY_SHAKEN`** (the count thrown off), and the ship's draining flag for the HUD.
  M7 part B, for the [Burrower](../../../enemies/ground/burrower/README.md) and the
  [Dust Kite](../../../enemies/air/dust-kite/README.md) (Level 15; M7 = a and the user's Q1 = a of
  2026-10-14; loaded and checked in step B2, mapped for the simulation in step B3): the layer name
  **`buried`** (`Layer.BURIED`, under the sand: hit by nothing, no contact, never targeted), never a
  stat block's `layer` (a burrowing unit's stat block says `ground`, the layer it surfaces on; `buried` is
  its current layer under the sand); the movement **`burrow`**: `swim` (px/s under the sand, on the
  screen: screen-fixed while it travels, bulges and is surfaced, Q1 = a), `travel` `[min, max]` (s to
  the next spot, seeded), `spot` (picked when the bulge starts: `ahead` `[min, max]` px above the ship's
  centre, `side` px either side of its x, `edge` px inside the side edges, `clear` px at least from the
  ship's centre), `bulge`, `surfaced` and `dive` (s; `buried` from the dive's middle), `surfacings` (then
  it travels off the bottom edge under the sand: an escape, no bounty), `rings` (each `at` s after it
  breaks out, with an optional `count` instead of the attack's) and per wave `stagger` (s at least
  between two surfacings) and `max_up` (at most that many surfaced or bulging at once; one that would
  break it waits buried); a burrowing unit's only attack is its `ring` (no `within`, no `interval`: it
  fires at the burrow's `rings`); its formation is `field` (the units enter buried with the ground in the
  wave's area); the hook **`burrow`** (`surfaced`, `rings`: the easy and hard windows and rings); its
  longest life before it escapes with surfacings left, 30 s (not while up), has no key (content's
  `MarsRules`, a default pending the user's review in round 38). The
  movement **`swoop`** gains a **`trigger`** (px above the ship's row: once the unit is that close it
  swoops across the ship's lane on one curve of its `radius`, aimed at the ship's x when it starts, no
  homing, at its `top_speed`, then glides on; a `sine` glide before it), and the hook **`swoop_speed`**
  (hard's 230); the simulation's curve bends 60 px to the side halfway down, the wave's units to
  alternate sides (content's `MarsRules`, a default pending the user's review in round 38; the
  triggered swoop's `radius` is not read). A plain `swoop` without a trigger stays the entry curve it
  was (not flown). A `V-wing` of units that neither hover nor dive (the Dust Kite) flies the V down the
  screen, the leader first (M7 part B, step B3).
  M7 part C, for the [Tendril Anchor](../../../enemies/ground/tendril-anchor/README.md) and the
  [Threadcrawler](../../../enemies/ground/threadcrawler/README.md) (Level 16; loaded and checked in step C2,
  mapped for the simulation in step C4): the attack pattern **`link`** (the tendril between two anchors of
  a linked nest: its `bullet` the touch's class, no speed, interval or first shot; its block **`link`**:
  `hp` (damaged only by a `beam`, which goes on past it), `dormant` (s of the harmless tell once the pair
  scrolls onto the screen), `band` (px, the contact band), `width` (px, drawn), `cooldown` (s, the
  touch's own; the ship not pushed), `burnt_bounty` (credits for burning it)), only on a ground unit with
  `terrain` movement, at most one; an attack's **`only`** (the difficulties it fires on: the anchors'
  hard orb; every one if not given); the movement **`crawl`** (`speed`, px/s along the ground on top of
  the scroll: the head crawls the wave's `crawl` path; only a ground segment chain, with a `pores` attack);
  the attack pattern **`pores`** (a chain's pore wave: each segment's pore an aimed `bullet` at `speed` in
  turn head to tail, `interval` s apart; its block **`pores`**: `rest`, `glow` (the tell) and optional
  `every_other`); the hook **`pores`** (`rest`, `every_other`: the easy and hard waves); a
  `segment_chain`'s optional **`width`** (the segments' sprite across the path: hit box `width × share`
  by `length × share`) and **`cut`** (`delay`: a cut's rear part dies, from the cut backwards) instead of
  `regrow` (one of them). A chain head's `first_bonus` pays a head-first kill its whole (the
  Threadcrawler's 30 + 18 = 48; taken apart 60), a default accepted in round 39 (2026-10-18).
  M7 part D, for the [Choir Herald](../../../enemies/air/choir-herald/README.md) and the
  [Shellback](../../../enemies/ground/shellback/README.md) (Level 17; loaded and checked in step D2 by content's
  `EnemyData` and `SiegeChecks`, mapped in step D4, flown from step D3; the defaults pending the user's review in
  round 40): the attack pattern **`aura`** (no bullet, speed, interval, count or first shot; its block
  **`aura`**: `radius` (px from its centre), `share` and `cap` (the ring a unit in range carries: `share` × that
  unit's HP at the level, at most `cap`), `refill` (the share of the ring a second, after `refill_delay` s
  without a hit, while the aura's unit lives and the unit stays in range), `fill` (s to the full ring on
  entering the radius), `keep` (s a unit leaving it keeps its ring), `collapse` (s the rings it fed take to
  collapse at its death) and `escort_radius` (its `carrier + escorts` escorts orbit it at this share of the
  radius, so they stay ringed)); never on a boss, at most one, only on an `air` unit; rings on every other
  enemy on every layer, never on a boss or another aura unit; the EMP Burst strips them; the hook **`aura`**
  (`radius`, `refill`: the Herald's easy 75 px and 15 %/s, hard 105 px). A unit's **`roll`** block (`below`,
  the share of its body's HP, its vital part's, at which it curls once; `curl`, s; `seconds` and `speed`, the
  roll along its walk path; `uncurl`, s; `belly_seconds` and `belly_factor`, the damage factor on its body
  after the uncurl; `ball`, the rolled ball's size and hit box; optional `belly_row`, px above the bottom
  edge: a roll drifting down the screen ends early where the scroll would carry it below that row before its
  uncurl ends, round 40 R.e): fully armoured while curled, no attack, the roll crushes the ground objects in its
  path (an ally's `rolls` where the ball touches its footprint, destructible scenery, crates; never enemies or
  the ship); a shot whose line of flight crosses a standing unit part's box hits the part first, from any side
  (round 40 R.a); only on a `ground` walker, the ball inside its sprite; the hook **`roll`** (`seconds`: hard's
  3.0). A `part_list` on a unit that is not a boss (the Shellback's `vital` body and `destroyable` vent, the
  vent's `attack: mortar`: its loss silences the mortar): the parts' HP and bounties add up to the unit's, a
  vital part's `first_bonus` pays a body-first kill its whole (the Shellback's 40 + 8 = 48; the vent first
  52). The Dust Kite's **`flock`** (moved forward from part E: Level 17's swarm) makes it fly `swarm` waves
  too: a unit with a flock flies only swarms unless its stat block lists other formations.
  M7 part E, for the [Dust Devil](../../../enemies/ground/dust-devil/README.md) and the
  [Ghost Drone](../../../enemies/air/ghost-drone/README.md) (Level 18; loaded and checked in step E2 by content's
  `EnemyData` and `StormChecks`, mapped in step E4, flown from steps E1/E3; the defaults pending the user's review
  in round 41): a unit's **`vortex`** (the spin cycle: `closed`, `rise` and `open`, s: closed on `ground` with
  the armour **`vanes`**, every shot glancing off, a blast (Smart Bomb, Airstrike) excepted; rising, the tell;
  open on `low-air`, the core hittable; it sinks back at the next cycle's start; `offset`, s between two units'
  cycles in a wave; `wander_speed`, px/s, and `wander_amplitude`, px either side of its wave's path, seeded;
  `keep_away`, px from the ship's centre; only on a `ground` unit, with `vanes` and only with them; the hook
  **`vortex`** (`open`: easy's 1.0 s window); a **`pull`** (`ship_radius` and `ship_speed`: the ship within the
  radius drifts toward the centre at the speed at the centre, fading to 0 at the edge; `pickup_radius` and
  `pickup_speed`: loose pickups drawn in and held until the unit dies, released then; only with a vortex; the
  hook **`pull`** (`ship_speed`: easy 25, hard 45)); an attack's **`while: open`** (it fires only in the open
  window; a `spiral` on a unit that is not a boss has it: the grit spiral, `arms`, `turn_rate` °/s, `interval`
  between two bullets of an arm). The movement **`lane_strafe`** (`descent_speed` and `strafe_speed`, px/s;
  `lane_depth` `[min, max]`, px below the top edge; `lane_spacing`, px between a wave's lanes: a front entry drops
  to its lane, banks and strafes across, a line abreast splitting half left, half right, a pincer entering at its
  lane from the sides; only on an `air` unit; the hook **`second_pass`**: hard's line-abreast survivors strafe back
  once); an aimed attack's pass **`trigger`** (`within_x`, px: its burst fires once per pass when the ship's x
  comes that close, no `interval`; only with a lane strafe), **`tell`** (s of the eye glint before the first shot;
  only with a trigger) and **`reaim`** (every shot of the burst aimed anew; only with a `burst`). A unit's
  **`intel`** (`unclassified_until_flown`: the hangar intel shows an "unclassified" silhouette of its layer and
  size until its first level has been flown; it first flies in its `first_level`, in no level before it).
  M7 part F, for the [Warden Tank](../../../enemies/ground/warden-tank/README.md) and the
  [Revenant Walker](../../../enemies/bosses/revenant-walker/README.md) (Level 19; loaded, checked and mapped in step F2
  by content's `EnemyData`, `FoundryChecks` and `FoundryRules`, flown from steps F1/F3; the defaults pending the
  user's review in round 42): a unit's **`turret`** (`turn_rate` °/s, `lock` s, `within` °: the turret turns all
  round toward the ship independent of the hull, stops `lock` s once ready and within `within`° of the ship and
  fires its burst along its aim; only on a `walk` unit that is not a boss, with one aimed attack; the hook
  **`turret`** (`turn_rate`: hard's 160)) and **`rear_plate`** (`arc` °, `multiplier`: direct shots arriving within
  `arc`° of its rear do ×`multiplier` to the body; only on a `walk` unit); the turret part is a `part_list` part
  as the Shellback's vent. The orientation **`8 angles`** (a walking boss's body). A walking boss's movement
  **`stride`** (`lanes_x`, px from the left; `lanes_y`, px below the top edge; `edges`, the entry edges in turn
  from `top`, `bottom`, `left`, `right`, the first `top`; `off_screen` s between passes under the next edge's
  warning; `turn` s for a turn round; `speeds` px/s by phase; `beside_lane`, `turn_in_place`: the first phase's
  lane beside the ship's, the last phase turning on the spot; only on a boss without an anchor, hover or
  descent: it is an arena boss as an anchored one is). A boss's **`plate`** (`part`, `hitbox` across × along the
  heading, `ahead` px: the front plate every shot glances off, the Ion Beam stopped) and **`cone`** (`length` px,
  `half_angle` °, `reach_behind` px, `cooldown`, `tell`, `burn`, `vents` s, `swivel` °, `bullet` class, `behind`
  px, optional `track` °/s (round 43: from its second phase the burn keeps turning toward the ship within the
  swivel): the rear flame cone; both only with a stride; the hook **`cone`** (`length`, `swivel`, `tell`)). Round 44
  (the walker, round 43 W.a/W.e): a boss's **`hatch_armour`** (`true`: the parts firing its homing attacks take damage
  only while their hatch is open, until an overheating last phase), **`lance`** (`after`, the parts whose loss wakes
  it; `from_phase`, a phase's name from which it is awake anyway; `charge`, `fire`, `interval`, `last_interval` s;
  `width` px; `bullet` class; `behind` px: a rail lance charging a line from its back through where the ship was,
  then a shot along it) and **`stomp`** (`reach`, `radius` px, `tell`, `expand`, `cooldown` s, `bullet`: a shockwave
  ring when the ship or Rook comes within its reach), all three only with a stride; the hooks **`lance`**
  (`charge`, `interval`, `last_interval`) and **`stomp`** (`tell`, `cooldown`). An aimed
  attack's **`homing`** (`turn_rate` °/s, `life` s, `hp`, `most` alive, `hatch` s of the hatch tell: its shots are
  homing missiles; a boss's; the hook **`homing`** (`turn_rate`); M9 part A: `look: diamond`, the Ascendancy's homing
  diamond, and `seekable: true`, picked by the player's homing shots, Rook and the Tesla arc when no unit is in their
  reach, QA-1; both only on a unit of Act 5 on, `BeltChecks`); a boss attack's **`parts`** (the parts that fire
  it besides those naming it as their `attack`: the walker's pods fire one salvo pattern in phase 1, another
  later) and **`aim: facing`** (a walking boss's aimed or `fan` attack fired along its heading; round 43: a fan of
  2 over 180° leaves its flanks). Round 43: a boss's **`fan`** may take a `burst` and `burst_gap` (the walker's chin
  and flank guns; no other unit's fan). A phase's **`until.share`**
  (`parts`, `below`: a second end beside its parts' `left`, once those parts hold less than `below` of their HP).
  M7 part G, for the [Mote Swarm](../../../enemies/air/mote-swarm/README.md)'s pursuit (Level 20; loaded and checked
  in step G2 by content's `EnemyData` and `RetreatChecks`, flown from step G3, mapped in step G4): a unit's
  **`pursuit`** block, the `pursuit` formation's defaults (`band` `[top, bottom]` px below the top edge, the
  leader point's band; `lag` px/s sideways after the ship; `lunge_every` and `tell` s; `ball` px, the flock
  contracted in the tell; `dive` px/s down the ship's lane to `overshoot` px below where the ship was at the
  tell's start, no homing; `climb` px/s back to the band; `refill` motes from the top edge every `refill_every`
  s while the flock has room; `break_speed` px/s off the top edge at the break-off); only with a `flock` and
  `pursuit` among its formations; the hook **`pursuit`** (`lunge_every`, `tell`: the Mote Swarm's easy 8 s and
  1.2 s, hard 4.5 s). Mapped (step G4, `RetreatRules.pursuit`): the hook over the wave's `pursuit` block over the
  unit's defaults; the entry and the `refill` sized by the formation lever.
  Basis (`enemies/data.yaml`): `reference_dps` per level (Levels 01–21 since M7 part A), `bullet_damage`, `contact_damage`
  per tier, `small_act_hp` (`cap`, `units`, `levels`: the `small` units that get the act HP factor, capped, in those levels; the user's Act 2
  playtest, 2026-10-18), `beam_era_hp` (`from_level`, `factor`, `tiers`: from that level on, the units of those
  tiers (bosses aside, below `huge`) take the factor on their HP on top of the act HP factor; M7 part E, step E5),
  `formations` (name: description); M9 part A: `reference_dps` 29–35 (the generator's Act 5 hand plan) and **`shields`**
  (`delay` s without a hit before a shield refills, `refill` of its maximum a second, `breaker_hold` s a `shield-breaker`
  hit holds the refill back; [enemy shields](../../../enemies/README.md#enemy-shields)).
- **M9 part A, Act 5's unit keys** (loaded and checked in step A2 by content's `EnemyData`, `BeltRules`, `BeltChecks`;
  the simulation's in step A1): a stat block's **`shield`** (the hex shield's HP at medium; the difficulty's HP lever
  and, on a returning `medium` or larger unit, the act HP factor on it, never the beam era's; only from Act 5) and the
  attack pattern **`laser-line`** with its **`line`** (`telegraph` s, at least 0.8, `live` s, `band` px, `hold_within`
  px: the unit holds fire while the ship is that close), its `bullet` (the damage once), `interval` (holding the
  telegraph and the live time) and `first_shot_delay`, no `speed`; the hooks `telegraph` and `attacks.<name>.interval`
  change it by difficulty. A laser line is the unit's `EnemySpec.laserLine`, not a gun.
- **M9 part B, Level 29's unit keys** (loaded and checked in step B2 by content's `EnemyData`, `BeltRules`,
  `BeltChecks`; flown from steps B1 and B1b): a stat block's **`leader`** (`hp`, `shield`, `bounty`: the airframe a
  V-wing's tip flies with the wave's `leader: tip`, the Talon's gold stripe; only with a `V-wing` among its formations)
  and **`neutral: true`** (a neutral craft: no attacks, no bounty, no contact; never counted as an enemy, never picked
  by the seekers, Rook's and the Bulwark's shots pass through; Level 29's wavering Stormhawks); the movements
  **`swoop_path`** (`lowest` `[min, max]` px below the top edge it bottoms out, drawn once per wave; `across` px it
  travels across its lane; `fire_band` px below the top edge it fires within: the Talon's swoop of a V-wing, line
  abreast, column or pincer), **`scatter`** (`speed` px/s, `seconds`: its wave's leader killed, it breaks off sideways
  and leaves holding fire) and **`ride`** (`within` px, `tell` s, `gap` s between riders, `dive_speed` px/s, `tumble`
  s a broken rock's riders tumble, `crawl` °/s round the rim: it rides a rock of its wave's `on_rocks`, only there); a
  hover's **`peel`** (px/s: at its end it peels off to the nearer side edge); a homing attack's **`hold_within`** (px:
  the unit holds fire while the ship is that close) and **`per_shooter: true`** (`most` counts only this unit's
  missiles in flight); a ground turret's aimed `homing` attack with a `turn_rate` (its rack aims, no `aim: facing`:
  the SAM Nest); the hooks **`parting_pair`** (`true`: a scattering unit fires one parting shot), **`ride`** (`within`,
  `tell`, `dive_speed`) and **`hover_y`** (px: the hover's height instead). M9 part C (Level 30; loaded and checked from step C1, mapped
  in step C2): the movement **`lay`** (`rise` px/s up its side lane from behind, `strafe` px/s across while laying,
  `lane` px in from its side edge: a mine layer, with a line mine), a `mine` attack's line-mine keys **`proximity`** px,
  **`blink`** s, **`blast`** (`radius` px, `bullet`: its damage), **`chain`** px, **`hop`** s and **`line`**
  (`clusters`, `per_cluster`, `spacing`, `cluster_gap`, `gap`, optional `middle_short`, `most`; its `drift` 0); a
  flyer's facing armour **`armour: {front_arc, factor}`** with a **`rear_plate`** (`arc`, `multiplier`: the engines);
  a `spawn` with a **`stream`** (`gap`, `first`, `straight` s, `most` alive: a launcher beside the unit's other
  attacks, its ring's `tell` its lamps); a segment chain's **`cars`** (`every`, `stagger`, `hp`, `bounty`, `attack`:
  its turret cars, counted in the parts' HP and bounty) and a cut's **`wreck: true`**; the hooks **`mine`** (`gap`,
  `middle_short`, `arm`) and **`chase`** (`turn_rate`).
  M9 part E (Level 32; loaded and checked in steps E1/E2 by content's `EnemyData`, `FoundryChecks` and `DeepCoreChecks`,
  mapped in step E2 by `UnitRules`/`DeepCoreRules`, flown from step E1; the defaults accepted by the user in round 65):
  the armour **`armour: {collar: [w, h]}`** (a hardened collar, `hardened` true: the hit box is the collar, the emitter's
  box on the centre inside it; a non-`anti-ground` shot on the collar outside the emitter glances, `SHOT_GLANCED`, on the
  emitter it damages; `anti-ground` shots, specials and charges anywhere; a beam where its column crosses the emitter;
  homing shots and Rook pick it); a `laser-sweep` on a `movement: terrain` unit is a **ground sweep** (no `origin`, no
  hover: due while on the field, centred on the ship's bearing any way round, arcs alternating their direction) whose
  `sweep` may add **`full_turn`** (`every`: every n-th sweep turns 360° from the ship's bearing, full turns alternating;
  `duration` s, `telegraph` s, the reach ring drawn first; `slot` s to the next telegraph; only on a terrain unit), with
  the hooks `sweep_arc`, **`full_turn_every`** (0 none, e.g. easy; 2 hard) and `attacks: {sweep: {interval}}`; a level's
  ground target may make the first sweep the full turn (`full_turn_first`, [levels](../schemas-levels/README.md)). A
  walker's **`walk.topple`** (`stagger` s, `fall` s, `line` `[width, length]` px) with parts **`topples: true`** (a
  `destroyable` part, e.g. a leg, whose loss starts the topple: the stagger, guns quiet and no more damage taken, then the
  fall toward the lost part, and 1.4 s after the loss the crush of every counted, non-dormant ground unit under the line,
  paid; `TOPPLE_TELL`, `TOPPLE_FALL`, `TOPPLE_CRUSH`; topple and a `topples` part come together) and a part's
  **`attack: salvo`** (the pod: it fires the walker's named second attack, an `aimed` pattern with `homing: {…, aimed:
  true}`, as the walk's spit, its `burst` missiles at once along the bearing to the ship; the pod's loss silences it).
  M9 part F (Level 33; loaded and checked in step F1 by content's `EnemyData` and `LeechChecks`, mapped in step F2 by
  `PartFRules`/`BeltRules`/`MinefieldRules`, flown from step F1; the defaults accepted in round 66):
  the movement `chase` may add **`within`** and **`drift`** (both or neither: from its entry it drifts at `drift` px/s,
  steering at its target's centre (the ship, or the prey it hunts) at the chase's `turn_rate`, with no time limit; once
  its centre is within `within` px it chases for `seconds`, then flies straight on and out; it latches while drifting
  too; the unit's `speed` is its way in, the drift's); a `latch`'s **`drains: power`** (default `shield`: while latched
  it lowers the generator's effective output by 2 MW, stacking with every latched drainer, the output back in the same
  step it is shaken off, thrown off or killed; no shield drain, so its `drain` attack has no `rate`; a loadout without a
  power budget is never drained), **`emp: throw`** (default `hold`: the EMP Burst throws a latched unit off, from the ship
  or from its prey, as a shake-off: stunned `stun` s, then it flees; a free one gets the EMP's Vrell stun) and
  **`prey_drift`** (px/s: it latches on a touched free prey unit, a wave's `prey` in
  [levels](../schemas-levels/README.md), which loses its hex shield with no refill, stops firing and drifts down at that
  speed until it leaves; a rider is hit by any shot at ×1, never moves on to the ship, drops off stunned when its prey
  dies and leaves as escaped when its prey leaves the screen); the hooks **`chase: {speed, turn_rate}`** (the speed new
  beside the turn rate; a hook keeps `within` and `drift`) and `latch_slots`. A `laser-line`'s `line` may add
  **`most_telegraphing`** (n: a due line waits while n lines of any unit telegraph, in pool order, its interval clock
  waiting too; the Rail Bunker's 2).
  M9 part G (Level 34; loaded and checked in step G1 by content's `EnemyData`, drawn from step G4; pending the user's
  review in round 67): a unit's **`livery`** `{from_act, sprite, faction}` (presentation only, the simulation never
  sees it): from act `from_act` on (2–7, after the act of the unit's first level) the game draws the unit from the
  `sprite` set instead of its own (hull, turret, stub and wreck; the muzzle flash stays the unit's), the atlases give
  the livery's frames only to that act's levels and the plain ones only to the levels before it, and the intel names
  the unit's `faction` (`EnemyData.liverySprite(act)`, `factionIn(act)`; the Warden Tank's `warden-tank-asc`,
  Jovian Ascendancy, from Act 5).
- **M9 part H, a ringed boss's keys** (the [Iron Sovereign](../../../enemies/bosses/iron-sovereign/README.md), Level 35;
  loaded and checked in step H1c by content's `EnemyData` (`RingBlock`), `RingRules` and `RingChecks`, flown from steps
  H1a/H1b; the defaults pending the user's review in round 68). Angles: a ring's `start`, a step's `angle` and the bays'
  `start` are ° clockwise on the screen from straight down; the fire arc's `clamp` is the boss doc's bearings (90°
  straight down, clockwise from the right), mapped −90°. In the `boss` block: **`rings`** (outer first, at most 3: `name`,
  `radius` px, `segments` (the game's hull pieces only), either `speed` °/s with `direction: cw|ccw` or **`step`**
  (`angle` °, `seconds`, `hold` s, `tell` s inside the hold) with its `direction`, `start` ° (0 if not given: the first
  battery straight down), **`batteries`** (`count`, `name` (the attack's if not given), `part` {`hp`, `box` `[w, h]`,
  `bounty`}, `attack`: the boss attack they fire); the batteries are appended to the `part_list` as `destroyable` parts
  named `<name>-1` … in ring order, at their mounts' rest places, so the bar, the phases' `exposes`, Rook and the looks
  see them as parts), **`fire_arc`** (`degrees`, `clamp` `[min, max]` bearings), **`spin_up`** (s from the halt; 4),
  **`grind`** (s; 2), **`speed_ups`** (the survivors' factors by the locked rings; `[1, 1.25, 1.5]`), **`cookoff`** (s
  between the batteries' cook-offs at the death; 0.15) and **`core`** (`part`, `glow` s before each shut-core fan,
  `opening` s untouchable as the phase exposing it starts, `shield` (its hex shield's HP under the enemy shield rule,
  by the HP lever), `spiral` {`attack`, `run`, `rest`, `tell` s}, `rail` {`attack` (a `laser-line`), `below` (the core's
  HP share under which it fires)}, `bays` {`count`, `radius` px, `start` °, `from` (the launched plain air unit),
  `stream`, `every`, `gap`, `first` (s after the opening), `speed` (the unit's if not given), `spread` °, `straight` s,
  `most_alive`, `tell` s, `skip_within` px}); a phase's **`until: {locks: n}`** (alone: the phase ends once n rings are
  locked, in any order; rising, at most the rings, the last phase on its parts). An attack's **`from`** (a boss's only:
  the unit's slug, or `{unit, attack, length}`): written with its `name`, `pattern` (the unit's: `aimed`, `laser-line`,
  `laser-sweep`), `interval`, `staggered`, `rotate`, `parts`, `only` and nothing of the unit's shape, the loader takes
  the bullet, speed, burst, homing, line or sweep from the unit's attack (a sweep at `length` px, without the unit's full
  turn), and the unit's hooks change that shape per difficulty (the SAM Nest's burst and turn rate, the Rail Bunker's
  telegraph, the Sentinel Tower's arc); a boss's `laser-line` is a rail line from its parts (`BossSpec.Pattern.LINE`,
  `staggered` allowed). A boss attack's `only` leaves its phases on the other difficulties (the core rail's `[medium,
  hard]`). The hook **`ring_arena`** (`speed` factor on the rings, a step's `seconds` divided by it; `arc` ° instead;
  `interval` factor on the battery attacks' intervals instead of the fire-rate lever; an authored `attacks.<name>.interval`
  stays final, as for a unit); the boss's `spawn_count` hook sizes the bays' streams. Checked: the arc 60–180° (each
  difficulty's), each ring across the field (2 × radius + a battery ≤ 480; at the level's boss `x` both sides), its
  battery attack a known one, a `laser-sweep` on a stepping ring only and its telegraph + sweep within the hold, each
  battery a part of its own, the SAM hatch rule (a homing battery's `hatch` shorter than its staggered turn, interval ÷
  batteries, on every difficulty), the core a part and no battery that a phase exposes, its spiral a `spiral`, its rail a
  `laser-line`, its bays' unit a plain air unit and their tell between two streams; `locks`, `ring_arena` and `from`
  only on a ringed boss (`from` on a boss).
- **Allies** (`allies/data.yaml`): one entry per ally
  slug with `name`, `layer`, `size`, `hitbox`, `hp` (medium; the level sets difficulty variants),
  `damaged_by` (`objective_aimed`: only shots the target-the-objective hook aims at it;
  `claws`: damage per second while a walker overlaps it), `follows` (`road`; `lanes`, M5 part D:
  its station in the level's band, see *Level*), `headings` (`count`, `step` in °, centred on
  straight up; rendered, not rotated), `smoke_below` (share of its HP) and optional `first_level`
  (the level that introduces it: an ally no level's data escorts yet goes in that level's sprite
  atlas, as an enemy does). M5 part D (the
  [evacuation shuttle](../../../allies/README.md#evacuation-shuttle), D2 = a): `damaged_by`
  **`bullets`** (every enemy bullet touching its hit box hurts it by its class and is spent) and
  **`contact`** (an `air` enemy's body hurts it by the enemy's tier once per contact; a `tiny` or
  `small` enemy is destroyed by the impact and paid as when it rams the ship); **`banks`**
  (`frames`, the rendered banking frames, and `full`, the sideways px/s of the full bank) and
  **`glide`** (s of a lost unit's glide into `far`, a presentation effect); built (M5 part D, step
  D4): read, checked (`lanes` with the `air` layer and only `bullets`/`contact`, `road` with the
  ground and neither) and in `AllySpec`.
  Built in step E2b (M5 part E, Level 11's [naval convoy](../../../allies/README.md#convoy-cargo-ship); user
  decisions E8 and E9 and the stated defaults of 2026-10-08): **`follows: stations`** (a `ground` ally
  of a level's `convoy` block: it holds its screen-space station at the scroll speed, glides to its
  arena lane at the halt and back after the boss, see *Level*; no road, no lanes, no headings beyond
  one); `damaged_by` **`slams`** (the hits one boss slam in its lane deals; its `hp` then counts slams:
  the cargo ship's `hp: 4`, `slams: 1`, retuned from 2 on 2026-10-09; nothing else hurts it); an ally whose `damaged_by` names
  nothing cannot be damaged and is never lost (the frigate); **`flak`** (presentation: `every` s
  between bursts, drawn on `low-air` over the convoy with a quiet sound, hitting nothing); a lost
  naval ally **sinks** (a presentation effect; the simulation marks it sunk at once). `content`
  checks `stations` with the `ground` layer and only `slams`.
  M7 part D (the [Ares Landing dome](../../../allies/ares-landing-dome/README.md), Level 17; loaded and checked
  in step D2, mapped in step D4; the defaults pending the user's review in round 40): **`hits`** instead of
  `hp` (one of them: the ally counts hits, its hp is that count; every attack its `damaged_by` names deals
  the hits given there, a hooked lob's direct hit one); `damaged_by` **`bombs`** (the hits a hooked
  bomber's drop deals as the ally's footprint passes beneath it, once per bomber) and **`rolls`** (the
  hits a roll deals crossing its footprint, once per roll); an ally hurt by bomb runs or rolls counts
  hits and one that counts hits is hurt by them, both only with `follows: ground`; **`sprite`**
  (presentation: its sprites' base name, the slug if not given: the dome's `ares-dome`).
