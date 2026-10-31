---
title: Data file schemas — levels and acts
design: approved
implementation: in-progress
art: n/a
updated: 2026-10-31
---

# Data file schemas — levels and acts

## Summary

The schema of a level's data file (sections, waves, triggers, objectives, radio and the per-level additions of M2 to M7) and of an act's file. Act 4's additions are in [schemas-act-4](../schemas-act-4/README.md), the backdrop in [schemas-backdrops](../schemas-backdrops/README.md). Moved unchanged from the [architecture](../README.md#data-file-schemas)'s Data file schemas, which keep the shared conventions (keys, units, sizes) and the index of every schema.

## Design

- **Level** (`campaign/<act>/<level>/data.yaml`): `scroll_speed`, `launch_seconds`, optional
  `bounty_scale` (default 1: a factor on every bounty paid in the level, after the act factor and
  the difficulty's income, before the one rounding per payout; see the economy), optional
  `crate_seconds` (default the player's `pickup_seconds`: how long an uncollected secret's crate or
  data core stays, at least `pickup_seconds` and only in a level with secrets; Level 10's ferry cache
  stays 12 s, enough to drift from the top edge to the ship's start line),
  `control_prompts`, timed `prompts` (`t`, `action`, `keys`, `seconds`, an optional `skip` layer:
  the prompt leaves once an enemy on it is destroyed; `requires: special`: shown only with a special fitted); `sections` back to back from t = 0 (`name`, `end`, `atmosphere`, optional
  `speed`, the backdrop's `tiles` (tile set ids, at most one per layer), an optional `peak`
  (`atmosphere`, `from`, `to`: another atmosphere inside the section, to its end at the latest; M5 part
  G: `easy` (s, at most the peak's length: easy's shorter peak, centred on it) and `ambience_db` (dB on
  the ambience in the peak, ramped with the backdrop's atmosphere ramp), Level 13's whiteout `peak:
  {atmosphere: heavy, from: 176, to: 188, easy: 6, ambience_db: 3}`), `arena: true` for the
  boss's arena (the level clock halts at its end while the boss lives and jumps to its end at an
  earlier death; at most one, only with a `boss`); scroll distances are derived); `boss` (`enemy`
  with a `boss` script, `t` it arrives, `x` of its centre, `section`, optional `incoming` (round 41: s before `t`
  the boss warning opens, `BossSpec.incomingSeconds`; Level 14's 7.5)); `waves` in time order, one row each (`t`,
  `formation`, `enemy` slug, `count`, `from` `front`/`sides`/`rear`, `edge`
  `left`/`right`/`alternating` (`sides` without an edge enters from both side edges), optional
  `hold`, `warning`, `break_group`, `speed` (px/s instead of the enemy's own), `interval` (s
  between the units of a stream; step FIX7: of a front `column` too, 0.4 s without one), `at` (a whirl cluster's release point `[x, y]`, y below the top
  edge), and `easy` / `hard` changes (`from`, `edge`, `count`, `break_group`, `warning`), an
optional `skip` list of the difficulties it is left out on (Level 06's hard-only Coilwyrm pair); a
  segment chain's `paths` (one list of `[x, y]` points per unit, y below the top edge, starting
  outside the play field; with one path every second unit flies it mirrored) and optional
  `loop_back` (`after`: s past the path's end, off the screen, until the head re-enters;
  `path`: its points, shifted sideways to start at the head's x; without one straight up from
  the bottom edge; the bottom edge is warned the wave's `warning` ahead, at least 3 s); a walker
  wave's `paths`, one list of
  `[x, y]` points per unit in screen coordinates at the wave's `t` (y below the top edge, points
  may lie outside the play field), which then scroll with the ground; a `pincer` with one path
  mirrors it for every second unit, other walker formations repeat it the wave's `interval` apart, 1.5 s
  without one), a mixed wave
  lists `groups` (a `carrier + escorts` wave lists the carrier's group first)
  instead; `set_pieces` (an `enemy` with a `part_list` and its `passes`, each a `name`, `section`,
  `layer`, fixed `heading` (° from straight down, positive to the right) and a `path` of
  `[t, x, y]` waypoints (y below the top edge); a pass on the player's layer adds `descend`
  (`at`, `seconds`), `hold` (s from the descent's start until it rises and leaves through the top
  edge), `leave_speed` and `easy` / `hard` holds; the README's *Waves* row comes from its
  `notes`); `debris` (the chunk kinds by name with their `size` and a large one's contact
  `damage` or a small one's `hp`, the `clearance` from the ship they enter at, `max_large` on
  screen, the `placed` chunks with `t`, `x`, `chunk` and `drift` `[x, y]` px/s, `easy`
  `leave_out_large` (every n-th large chunk) and `hard` `drift_factor`); `ground_targets` (`target`, `section`, `layer`, `size`, `at` (one `[t, x]` per object:
  when it enters at the top edge and its x), a destructible's `count`, `hp`, `bounty`, `drop`, an
  optional `bonus_drop` with it (a special charge drops only with a special fitted), `hardened`, an
  optional `reveals` (its hidden crate drops when it is destroyed), an `easy` / `hard` change giving its `hp`
  (Level 21's survey crawler, hard 28; round 44 Q.5), `dark: true` (a trigger that
  takes hits only while the headlight or a flare lights it), an optional `sprite` (its sprite
  set: `<sprite>_0..2` intact, damaged, wrecked and `<sprite>-break_<n>`; Level 01's
  `cargo-container`, without a wreck, if left out), or
  a trigger's `hits`, `reveals` (triggers revealing the same secret reveal it together, when the
  last of them is spent) and optional `sprite` (its own frames instead of the beacon or the level's
  trigger light: `<sprite>_0..2` intact, hit, spent, or `_0..1` intact, spent; an optional
  `<sprite>-glow` drawn after the darkness's light pass until it is spent (one frame: a still
  light; `<sprite>-glow_0..n`: a loop at 8 fps, `GroundGlow.FRAMES_PER_SECOND`, irregular flicker
  steps baked into the frames; with a hit frame an even loop splits in halves, the first until the
  first hit, the second after it: Level 08's billboard), and `<sprite>-glint_<n>`
  drawn after it only while it is lit; Level 06's survey cache and terminal; a look whose spent
  frame is a wreck, `TriggerBreak`'s billboard, breaks like a destructible on the hit that spends
  it, `SimEvents.Type.TRIGGER_SPENT`: the small explosion, blast and crumble)); `secrets` (`name`, hidden `crate` credits, `radio` line; or a
  `data_core` with the shop item it `unlocks` and `crate: 0`: it drops the data core pickup, its
  line plays when it is collected, and a won level records the core and its unlock); a dark
  level's `darkness` (`headlight`: `from` s, `length` px, an `easy` length, `angle` °; `flare`:
  `seconds` (`easy_seconds`) a flare burns, its pool's `radius` px and `drift` px/s down the
  screen; `flares`: `t`, `x`, `y` (px below the top edge) and an optional `skip` list of
  difficulties; `lights`: static ground pools with `t` (entering at the top edge), `x` and
  `radius`; `ambient`: the ground's brightness outside light, 0 to 1); placed
  `pickups` (`pickup`, `dropped_by` wave and unit `first`/`second`/`last`, or a ground-target
  `group` and unit `last`: dropped where the group's last unit dies when it is cleared); `radio` cues (trigger
  `t` or `event` `first-kill` or `enemy-escaped` (with `enemy`; a set piece escapes at the end of
  its last pass) / `group-cleared` / `group-lost` (with `group`) / `first-group-lost` /
  `secondary-objective` / `level-end` / `first-ally-hit` / `first-ally-lost` (a convoy's first hit
  and first loss; `{ally}` in the line becomes the unit's number word, "Three") / (M5 part B)
  `escort-first-kill` (no `enemy`: the first kill whose killing shot or blast is the
  escort's, `wingmanMount`, while he flies; never after he ejects; once per attempt, the flag in
  the state hash and the boss checkpoint; without an escort flying it never plays) / `boss-phase`
  (with `phase`, the phase's name) / `boss-destroyed` (the level's boss; planned (part G): the
  loader gave such a cue an empty subject while the simulation cues the boss's slug, so it never
  played, Level 05's "Frigate down" included; part G fixes it) / `mission-failed`
  (not played: the line and speaker on the mission failed screen after a failed primary objective;
  at most one; only with an `escort` or `destroy-targets` primary; `{group}` in it becomes the
  lost group's name); a `level-end` cue's `allies` `[min, max]` (the convoy units home, so each outcome
  has its line), and `requires: special` (only with a special fitted; on any cue); planned (part
  G): `requires: homing` (a weapon with homing delivery fitted) and `requires_not` (`special` or
  `homing`: plays only without it), and a `boss-phase` cue's `timeout: true` (it plays only when
  the phase before ended on its timeout with parts it waited for alive);
  `speaker`, `line` (M5 part B: `{side}` in it becomes `left` or `right`, the escort's side: the
  save's, or `--escort`'s `side=`, left when he does not fly; such a line is voiced once per side
  and the subtitle shows the side's text),
  `distorted`, the portrait's optional `expression` (`neutral`, `grim`, `fierce`; M9 part H: Vorne's own `smug` and
  `rage`, his only (checked); neutral if not given; a secret's `radio` line takes it too), `shout: true` (the voice shouts the line, with the
  speaker table's `shout` row; separate from how the line queues), an event cue's optional `delay` (round 45 B.a,
  step C45: seconds, at least 0; the radio holds the line that long after its event, the lines queued after it
  waiting behind it, the debrief too; Level 21's Choir at the Colossus's death, 4.5 s; `RadioCue.delaySeconds()`, or
  `LevelData.radioDelay(i)` by the script radio's index), an event cue's optional `not_before` (step FIX6: a level
  second; the radio holds the line, once its event came, until the level clock reaches it; Level 33's "Field's clean"
  held to the calm before the end; `RadioCue.notBeforeSeconds()`, `LevelData.radioNotBefore(i)`), and `easy` / `hard` changes
  giving another `line`,
  as when a wave enters elsewhere on that difficulty); `objectives` (`primary`, `secondary` `kill_ratio` with an
  optional `boss_counted: false` (the level's boss set pieces left out of the ratio's enemies and kills; default true:
  Level 21, round 44 K.h), `groups`, `escapes` (the enemy none of
  which may leave the screen alive; a spawner's self-burst counts as an escape) or `kill_all`
  (enemies every unit of which must die, met and failed as `escapes`, with the tracker's `label`)
  and `credits`;
  planned (part G): a fifth secondary kind, `parts` (names of the level boss's parts, every one
  of which must be shot off) with `before` (the boss phase whose end, by timeout, fails it) and
  the tracker's `label`;
  `primary` is `reach-end`, `destroy-targets` with its `targets` (the ground-target groups that
  must be cleared; it fails as soon as a unit of one leaves the screen alive, the groups pay no
  credits, and the secondary then cannot use `groups`) or `escort`, which adds an `escort` block: the `ally` slug, the
  column's centre heights `y` (px below the top edge, the leading unit first, at least the ally's
  length apart), `credits` per unit home (through the credit factor, with a debrief row), `enter`
  (`t` of the first unit, `interval` s between units, `speed` px/s up the screen: they roll in from
  the bottom edge to their stations), the target-the-objective `hook` (`mode`, only `nearest` so
  far, and the `enemies` whose aimed attacks go for the convoy) and `easy` / `hard` `hp`; it fails
  when every unit is lost); `road` (`width`, an optional `texture` id: the ribbon's image
  `assets/backdrop/level-NN/<texture>.png`, its rows by arc length from the first point, a flat
  placeholder colour without one; and `points`, one `[t, x]` per road point, `t` when it passes the
  middle of the screen, which may lie before the start; straight between points and straight up
  beyond them; the convoy follows it; the ribbon stays inside the play field and bends no further
  than the ally's headings, ±30° for the crawler); `music`
  (`track`, `start_section`, optional `start_db` (the theme's level through its start section,
  rising to full at the next), `full_section`, optional `stems` (section: `base` or `full`,
  overriding it), `ambience` (the setting's loop by its key in the
  [sfx](../../../audio/sfx/README.md#ambience-per-setting) *Ambience* table: `earth-orbit`, `luna`,
  `earth-megacity`; M5 part H: `earth-canopy`, the canopy's hum, which dies with the level's boss
  and hands over to `earth-megacity`), `end_jingle`, optional `boss_sting` (`miniboss-sting`: track 21 over
  a 0.5 s crossfade when the boss arrives, the theme returning after it), optional `ambience_from`
  (the level time from which only the ambience plays: the theme fades out as at a won level) and
  `voice_loop` (`speaker`, `section`, `db`: that speaker's first timed radio line loops at `db`
  on the voice bus while the section plays, silent without a voice file; Level 06's perimeter
  beacon); planned (part G): `boss_warning` (`boss-warning`: track 22 with the klaxon and the
  warning banner when an act boss arrives, the theme crossfading out) and `boss_track`
  (`choir-descends`: track 18, coming in on the downbeat after the warning and fading out at the
  kill, leaving the ambience)); `sleds` (Level 05's
  mass-driver sleds: rail `x`, `width`, the `first` launch, `period`, `until`, `lights` (s of the
  telegraph), `run` (s on screen), contact `damage`, the `clamp` secret whose trigger only takes
  hits while the rail is dark, and `easy` / `hard` `period`); `rocks` (low-gravity debris a
  destroyed ground unit throws: `count` and `speed` `[min, max]`, `life` s, `size`, `hp`, contact
  `damage`, `clearance` px from the ship, none on easy unless `on_easy`); `boss.notes.streams` (the
  boss streams the credit budget counts); `difficulty` (level-wide
  `easy` / `hard` enemy changes such as `burst` and a walker's `speed_factor`, and `extra_pickups` placed like `pickups`);
  planned (part G): a wave `easy` / `hard` change's `hold`; a placed pickup's `skip` list of
  difficulties; `dropped_by` `parts` (names of the level boss's parts) with `unit` `first`/
  `second`/`last`: the n-th of them shot off drops it (parts lost in the death drop nothing);
  `tows` (Level 07's lifeboat: a friendly craft on the air layer towing a secret's crate): `t` it
  enters at the top edge, `x` of its centre, `drift` `[x, y]` px/s (y up, so negative: down the
  screen), the `boat` and `pod` sizes, the pod's `tether` `[dx, dy]` from the boat's centre, the
  `cable` size (its hit box midway between them, the only part shots hit; boat and pod let shots
  and bullets through and never collide), `hits` to cut it and the secret it `reveals` (the pod
  falls free as that secret's crate), optional `beam_touch` (default false; the Act 5 tows, round 64
  C.1 a: a beam whose column touches the cable box counts one hit per 0.25 s of contact, the first
  at once, and passes on);
  `threat_profile` (the hangar intel: `setting`, `layers`, `density` 1–5, recommended `traits`,
  `hazards`, `boss`, optional `specials` limits, optional `objective` (the OBJECTIVE field
  shown from sensor L1, "ESCORT 5 CRAWLERS"), `varga`, Dr. Varga's line for each sensor level
  (`none`, `l1`, `l2`, `l3`, all four required), and `notes` with the *Threat profile* rows,
  where `{directions}` is derived and the other `{fields}` come from the profile); `briefing`
  (`pages` of `speaker` and `line` with the optional portrait `expression` and `image`, the name of
  a tactical map or mission image in `assets/ui/briefing/`, M9 part A the optional `channel` (`enemy`: an intercepted
  page, [briefing](../../../ui/briefing/README.md#intercepted-transmissions-draft); `friendly` by default), and the hangar
  `teaser`, rendered into *Briefing*); M9 part A: a radio cue's optional `channel` (`friendly`, `enemy`) overrides its
  speaker's (the voice table's `filter: enemy`: Vorne, Harker); the level's and a section's **`medium: vacuum`** (Act 5's
  space: presentation and the dry setting, flown as air) and **`underground: true`** (inside rock: the specials'
  `underground` limit, `IN ROCK`; a section's own, else the level's), both only from Act 5 (`BeltChecks`). M9 part B
  (Level 29; loaded and checked in step B2, flown from steps B1 and B1b): an arena section of speed 0's **`holds`**
  (a ground-target group: a held arena, its clock held while one of that group stands, not by a boss; the scroll eases
  down across the section from the speed before; no boss in the level), **`halt_waves`** (`every` s, `waves`: plain
  front waves on the halt's own clock, `t` s from its start, repeating while a holder stands, cut at the last one's
  fall) and **`escort_swap`** (`at` `[x, y]`, the prison tender's hold on the halted screen, y px below the top edge;
  `join` s: Rook's rescue in his last missing level), both only with a held arena; a wave's **`leader: tip`** (a
  V-wing's tip flies its unit's `leader` airframe) and **`on_rocks`** (`chunk`, a small chunk of the level's `debris`;
  `riders` 1–5 a rock; `rocks`, each `at` `[x, y]` (y px below the top edge) and `drift` `[x, y]` px/s on the screen,
  y up as the debris'; the wave's count is rocks × riders on every difficulty, of a unit that `ride`s); a ground
  target's **`dormant_until`** (a level time: the nest sleeps dark until then, then a 1 s wake tell); M9 part E (step
  E3b, Level 32): a ground target's **`full_turn_first: true`** (a unit whose sweep has a `full_turn`, the Sentinel
  Tower: each unit's first sweep is the full turn, then every `every`-th after it; none where a difficulty has no full
  turn); M9 part F (steps F1, F2, Level 33): a wave's **`prey`** (`hold` s and `depth` `[min, max]` px below the top
  edge: a V-wing or line abreast of a plain air unit flies a refuelling hold instead of its swoop and leaves straight
  up; latched by a leech it drifts down instead), a wave's **`hunts: prey`** (its unit's latch has a `prey_drift`; it
  follows a prey wave by at most 10 s and steers at the nearest free prey, then the ship; M10 part A, step A2a: or
  **`hunts: <tag>`**, the `tag` of another wave of the level, the [three-way war](../../../enemies/README.md#the-three-way-war):
  its units' chase, dive or ram, latch and aimed fire go at that wave's units while one is on the screen, their bullets
  hit them; the hunters are the Vrell's, the prey a wave of Jovian Ascendancy units that are no boss or neutral craft,
  checked in `JovianChecks`), a ground target's **`crown`** (M10 part A: a nest of a unit with a `dome`, the Shield
  Pylon, turns its crown and its dome's centre this many ° from straight up the screen, positive to the right;
  straight up if not given) and a ground target's
  **`staggered: true`** (a nest of a `laser-line` unit: the i-th of its n placements on that difficulty fires its first
  line i × the interval / n after the stat block's first delay; checked in `LeechChecks`); a secret's `radio` line may
  carry **`requires: escort`** as a timed line's (step F4: Rook's Claim 114 line plays only with him flying); the
  hunters' and the prey's unit keys (`chase.within`/`drift`, `latch.drains: power`, `emp: throw`, `prey_drift`, the
  rail's `most_telegraphing`) are in [units](../schemas-units/README.md); the hangar intel's wave strip marks a leech
  wave's rear and side entries (violet ticks at sensor L3); the secondary's
  **`spared`** (a neutral craft's slug the level flies, with its `label`: met when the last has left, failed at the
  first destroyed); the radio events **`first-homing`**, **`neutral-left`** and **`neutral-down`** (the latter two
  name their `enemy`), **`holders-down`** (in a level with an `escort_swap` it names the swap's outcome it plays for as
  **`swap`**: `bulwark` the Bulwark in the slot still flying, `joins` none flying, `leaves` no Rook to join, `empty`
  Rook flying from the start; step B4b) and **`escort-joined`**. The act data's `briefing` pages take `channel:
  enemy` (Act 5's Vorne). M9 part G (Level 34; loaded and checked in step G1 by content's `SalvageChecks`, flown from
  step G1, drawn and heard from step G4; the defaults accepted in round 67): the secondary's **`escapes`**
  may name an **object look** instead of a unit: the `sprite` of destructible ground targets (`cargo-container` if left
  out; a variant `<look>-<variant>` counts too: Level 34's `cargo-container-cdf` and `-helix`); a counted object
  destroyed by anything is opened, one leaving the screen is away; `allowed` per difficulty below the counted objects,
  no `tag`, the look on no trigger, no counted object on a `branch`; the HUD tracker shows `label n / total` of the
  objects opened (`Secondary.objectEscapes`). A placed pickup **`pickup: story`** with **`story`** (a kebab name: the
  game's `<story>*` frames, `helix-core`) and **`dropped_by: {group, unit: last}`** (only a group of destructible
  ground targets; at most one a level, none in a difficulty's extra pickups, never a target's `drop`/`bonus_drop`):
  the group's last one down frees it at the group's centre, it rises 1.0 s untouchable, then drifts as a pickup
  paying nothing, collected when the ship's collection radius touches its 60 × 40 box (no secret, no rating,
  `StoryPickups`); the radio events **`story-collected`** and **`story-missed`** (missed: it leaves the screen or
  its life runs out, or the group leaves unshot or the level ends first; only in a level with a story pickup). Step
  BOSS70: the radio events **`first-rail-volley`**, **`first-mine-ring`**, **`first-wreck-throw`** (the ringed boss's
  core draws that play for the first time, at its tell) and **`core-low`** (its first draw below its pool's `low`
  share), phase lines on the halted arena; only with a ringed boss whose core's `pool` has the play. A
  ground target's `sprite` (above) gives its own look: the containers' variants, the hold clamps
  `hold-clamp-left|right`, the secrets' `vigil-shutter`, `sable-wren-hatch` and `old-marta-airlock`. The music's
  `motif` may add **`to`** (a level time: the motif stem ends there, so its window may close inside a section; Level
  34's 106–124), and Level 34's `ambience_layers` (below) carry the `wreck-groans` loop over `belt-open`. A unit's
  `livery` is in [units](../schemas-units/README.md). M9 part H (Level 35; loaded and mapped in step H1c, flown from step H1b): the level's
  **`relay`** (the mass-relay flash, once: `t` s on the level clock, `telegraph` s, `live` s, `band` px, `damage`,
  `from: right|left`, `easy`/`hard` `{telegraph}`; `HazardSpec.Relay`); step H2's checks: no wave enters from 1.3 s
  before its telegraph to 2.5 s after its flash (medium's), no ground unit with a `laser-line` on the field at its flash
  on any difficulty, and in a level whose boss is ringed nothing on the ground still on the field when the arena
  starts (the halt boundary). Level 35's stop before the arena is no timed halt (a `speed: 0` section outside an arena
  needs a `defend` primary): its section "The Halt" eases to `speed: 1` (1 px/s, `ramp: 6`) and the arena's own 1 s ease
  follows; an anchored boss's halt place (`ScrollPlan.haltScroll`) integrates such eased sections step by step (step H3;
  only in a level with eases before its arena, so no Act 1–4 boss moves); the radio's `requires_not: escort` (above)
  gives Okafor's closing answer without Rook (step H2). Every part H key is decided in round 68, 2026-10-29. `backdrop`
  (presentation only, see below). The credit budget
  table is derived: kills × bounties (× `bounty_scale`, each payout rounded: a unit's parts each their own, the
  Strider's), ground targets, crates, the objectives,
  each as a perfect run and as the typical haul (weighted by the economy's `typical_player`),
  against budget(n) of the economy; the attack directions are each entry's share of the enemies.
  M5 part C, for
  [Level 09](../../../campaign/act-2-homefront/level-09-arcology-fall/README.md) (user decisions D2,
  D3, D5, D6 and D7 of M5 part C): **hold zones**, `holds` (each: `groups`, the ground-target
  groups of the objectives it waits for, each group in one hold at most; `y`, px below the top edge
  the first unit of those groups reaches to start it; `speed`, px/s it eases to, with `easy` /
  `hard` `speed`, below every section's speed; `ramp`, s of the ease each way, a smoothstep): the
  scroll eases to the hold's speed and back to the section's once every unit of its groups is gone
  (a hold whose groups include the `collapse`'s, once their clearing started it, only when the
  collapse's dust has settled, the impact + its `dust` `seconds` (user, 2026-10-07; Level 09:
  9.0 s after the warning starts), so the scroll stays at the hold's speed through the lean, the
  drop, the blast and the settling dust and what falls and the heap it leaves stay on the screen;
  no event marks the settling, the hold's `HOLD_END` comes in that step);
  there is no timeout (a unit that leaves the screen alive fails a `destroy-targets` primary at
  once, which also ends the hold); a hold whose groups are gone before it starts never starts; one
  hold runs at a time; the hold's state is in the state hash (only in a level with holds or a
  collapse, so the other levels hash as before). Holds are not built for a level with set pieces,
  cranes, sleds, tows or a boss (`content` rejects it). **The level clock** then advances at the
  current scroll speed ÷ the section's speed (D2 = a: 0.2 at 30 of 150), in fixed point (1/65 536
  of a step; outside a hold exactly one step per step, so the existing levels step and replay as
  before), so every time in a level file is **script time**, the scroll distance ÷ the section's
  speed: waves, ground targets, radio `t`, prompts, edge warnings, the sections and their
  atmosphere, the backdrop's placements, the progress bar and the level end wait with the scroll in
  a hold, while units, bullets, the spawners' cycles, pounces and Rook run on the real steps (the
  density and haul checks count script time, the pacing check real time); the simulation gives
  both clocks and the rate (`Sortie.levelSeconds()`/`scriptSeconds()`, `realSeconds()`,
  `scriptRate()`); planned (M5 part C, the game): the radio queue's own timing and the backdrop's
  animation on the real clock. A **`collapse`** block (D5 = a, round 31's look c: `groups`, the groups whose clearing
  starts it, every unit destroyed; `warning`, s the tower leans before it drops; `drop`, s it takes
  to drop straight down, the impact at `warning` + `drop`; `blast` (`seconds`, `from`, `to`): from
  the impact its kill ring grows from `from` to `to` px round the tower's footprint's centre over
  `seconds`, eased 1 − (1 − t)², the dust blast's front; `tower`, the backdrop's tower piece that
  falls, placed exactly once: the band is its footprint's extent along the scroll (the whole width),
  so it lies on the ground under the tower and scrolls with it however early or late the groups are
  cleared; `dust`, the `atmosphere` it raises and the `seconds` it ramps back out over after the
  impact, at least the blast's (a hold over its groups lasts until then); `rubble`, the backdrop piece it leaves, a piece id of the level's backdrop), timed on the
  real steps, with the events `COLLAPSE_WARNING` (the lean starts), `COLLAPSE_FALL` (the drop
  starts), `COLLAPSE_IMPACT` and `COLLAPSE_END` (the blast has rolled out): every ground unit (on the
  `ground` layer at that moment, so a pouncing Ravager in its air window is spared) in the band
  whose centre the ring reaches is destroyed and pays and scores as an Airstrike kill; air units
  and the player are untouched; built (user, 2026-10-07, round 31's look c), replacing the earlier
  sweep from left to right; the game draws the lean, the drop, the cast shadow, the dust and the
  rubble heap (`CollapseLooks`). A wave's optional **`tag`** (a name) and a secondary `escapes` with a
  `tag` (D6 = a): only the units of the waves with that tag count (their number per difficulty
  follows from the waves), met when all of them are destroyed, failed when one leaves the screen
  alive; `content` checks that a wave of the enemy carries the tag. Any secondary may give a
  **`name`** (Level 09's `Hold the bridge`), used where the generated wording would mislead: the
  briefing's bonus line (`BONUS: HOLD THE BRIDGE`) and the README's credit table (`Secondary: hold
  the bridge`) show it; the tracker's `label` stays apart. A **`pack`** walker wave lists
  one path per unit, as many as its largest difficulty's count (`content` checks it), the units
  entering 0.25 s apart. Radio events **`hold-start`** (the level's first hold starts its ease;
  once; only in a level with holds), **`first-pounce`** (the attempt's first pounce takes off; no
  `enemy`; only with an enemy that pounces) and **`collapse`** (the collapse's warning starts; only
  with a collapse), and **`requires: escort`** on any cue (it plays only while an escort flies:
  hired, fitted and not ejected, asked when the cue is due; Rook's scripted lines; also
  `requires_not: escort`). The threat profile's **`required`** (D7 = a: the traits among its
  `traits` the primary cannot be met without), loaded and checked; planned (M5 part C, the game): a
  launch warning at every sensor level, counting every source that damages hardened targets, see
  the [hangar](../../../ui/hangar/README.md). The `music` block's **`full_on`** (`hold`: the full mix
  while a hold runs, fading in over 1 s as it starts and out over 4 s after it ends; `collapse`:
  the full mix from the collapse to the level's end; each only in a level with it), loaded and
  checked, the simulation telling `holdActive()`, `collapseStarted()` and the `HOLD_START`,
  `HOLD_END` and `COLLAPSE_WARNING` events; planned (M5 part C, the game): the run-time stem hook.
  M5 part D, for [Level 10](../../../campaign/act-2-homefront/level-10-evacuation-corridor/README.md)
  (user decisions D1–D12 of 2026-10-08): the formation **`swarm`** (a `flock` enemy's wave: `count`
  members round the leader point flying the wave's `paths`, one route of `[x, y]` points, y below
  the top edge, starting outside the play field, below the bottom edge with `from: rear` (then
  warned as any rear wave); the members placed in a seeded cloud round the route's first point;
  the chains' **`loop_back`** (`after`, `path`) with a new `count` (the loop-backs, default 1) and
  a wave's `easy` / `hard` change **`loops`** (Level 10's hard: 2); each re-entry warned at the bottom
  edge the wave's `warning` ahead, at least 3 s, and counted as a rear entry for Rook's Trail and
  rear bark, as a chain's; `content` checks that a swarm's enemy has a flock and flies only swarms,
  one route, at most the flock's `max` on every difficulty, and that only a swarm loops back more
  than once). The formation **`rear ambush`** (1–4 units, each in its lane across the
  bottom edge, x = (i + 1) × 480 ÷ (n + 1) from the left, i from 0; a lone unit in the middle): a
  unit with the `ambush` movement enters at the top at the wave's `t` and flies it; its bottom-edge
  warning runs the wave's `warning` (at least 3 s) ahead of its **re-entry**, not ahead of `t`, and
  its wave is a rear wave for Trail and the rear bark from the re-entry on; `from: rear`, so the
  intel counts it as rear (an `ambush` enemy flies only this formation, `content` checks it).
  `paths` accepted on a **`snake`** wave (one route that every unit flies, from any edge; the
  Skitter's authored-paths item; without it the laid-out shape as before); 2026-10-08, for Level
  10's popcorn round the shuttle band: `paths` also on a **`stream`** wave (one route, flown by its
  units from the left edge, mirrored by those from the right; without it the stream's shape as
  before). Built (M5 part D, step D4) for Level 10: the **air escort** (D1, D2, D4, D5;
  `ObjectiveSpec.Air`, flown by `Convoy`): an `escort`
  block whose ally `follows: lanes` gives **`stations`** instead of `y`, an optional **`liftoff`**
  instead of `enter`, no `hook` and no road (`content` rejects each mix):
  `stations`, one per unit, the leading unit first, each `at` `[x, y]` (px from the play field's
  left edge, px below the top edge), `sway` `[ax, ay]` px, `period` s and `phase` (0–1): the unit
  flies at `at` + (ax sin θ, ay sin 2θ), θ = 2π (t ÷ `period` + `phase`), t the level clock, its
  hit box inside the band and never overlapping another's (checked by `content`, sampled), never
  reacting to threats; `liftoff` (`t`, `seconds`, `pads`: one `[x, y]` screen point per unit at
  `t`): from its pad the unit eases (smoothstep) to its station over `seconds`, drawn from the
  ground layer's scale to the air scale; `climb` (`t`, `seconds`): the climb-out, the units leaving
  the top edge, every unit alive at `t` home; **untouchable** during the liftoff, the climb-out and,
  for the scripted unit, until its loss: enemy fire and contact **pass through** it (nothing is
  spent, no rammer dies on it); **`scripted_loss`** (`unit`, 1-based, `t`, `glow` s): the events
  **`LOSS_GLOW`** at `t − glow` and **`SCRIPTED_LOSS`** at `t` (the lance; the radio event
  **`scripted-loss`**), then the unit is lost without the cues `ally-lost` and `first-ally-lost`,
  without counting toward the fail, and outside the escort's pay and the typical haul's stations
  (`TypicalHaul`); the primary **fails at once when every saveable unit is lost** (the units but the
  scripted one; without a scripted loss every unit, as before), through the failed primary flow; a
  level-end cue's `allies` counts the saveable units home. An air ally's hits follow its
  `damaged_by` (see *Allies*). `content` checks, sampled every step of the level, that each
  station's hit box stays on the play field and off the others', that the pads are on the screen,
  that the liftoff ends before the scripted loss's glow starts and the climb-out comes after the
  loss, inside the level, and that a level-end cue's `allies` names no more than the saveable units.
  The simulation (built) tells each unit's position, its `lift` (0 on the ground layer's scale to 1
  on the air layer's), `bankVelocity()`, armour (`hpShare()`), `ticksSinceHit()` (the hit flash;
  for a lost unit its glide's clock, `ticksSinceLost()`), `untouchable()` and its state (`PAD`,
  `LIFTING`, `FLYING`, `CLIMBING`, `HOME`, `WRECK` for lost: the simulation keeps a lost unit where
  it was lost and the game draws the glide), and `Sortie.airEscort()`, `saveableAllies()`,
  `saveableAlliesAlive()`, `scriptedAlly()` and `lastAllyLost()`; the debrief's `LevelResult.Escort`
  counts the saveable units (home of 4). With the debug option `--invulnerable` losing every saveable
  unit does not fail an air escort (so a capture sees the level to its end). A level may have **no
  secondary objective** (Level 10, D5 = a: `objectives` without `secondary`): the simulation then
  has `Secondary.NONE` (`none()`: never met, paid or cued). Radio event **`ally-lost`**
  (every loss the player could have
  prevented, `{ally}` its number word, `Sortie.lastAllyLost()`, whose `ALLY_LOST` event comes before
  the cue's `RADIO` event in the same step; also on the first, after `first-ally-lost`'s cue; unlike
  every other cue it starts again on each loss), built. The
  threat profile's `required` may name **`rear`** (loaded and checked as one of the `traits`; met by
  a fitted rear-slot weapon only, homing and Rook's guns do not reach the rear); planned (M5 part D,
  the hangar): the launch warning at every sensor level. The `music`
  block's **`duck`** (`on`: the event, `scripted-loss`; `db` below 0; `seconds`: the theme ducks by `db` for
  `seconds`, no sting; with the radio's duck the lower applies, they do not add up) and
  **`ambience_changes`** (`section` from 2, in order, `ambience` key, `crossfade` s: the ambience
  crossfades at that section's start; Level 10: `earth-ocean` at section 4 over 4 s), loaded and
  checked (the duck needs a scripted loss, a change's section exists); planned (M5 part D, the
  game): playing them. The ambience key **`earth-ocean`**
  (round 08's ocean loop, brought forward from part E). The radio events **`first-decloak`** (the
  attempt's first `DECLOAK`; once; only with a cloaked enemy) and **`first-loop-back`** (the first
  `LOOP_BACK`; once; only with a looping swarm; a Rook line on it counts as a scripted Rook line for
  the barks' spacing, as `escort-first-kill`'s), built (`content` checks the cloaked enemy, the
  looping swarm and, for `scripted-loss`, the scripted loss).
  M5 part E, for [Level 11](../../../campaign/act-2-homefront/level-11-atlantic-convoy/README.md)
  (user decisions E1–E10 of 2026-10-08 and the stated defaults): **`water: true`** (E1 = a; default
  false; built in step E2a, `GroundSpec.water()`): the whole play field is water (Levels 11–13): the
  torpedo runs only here, every kill on the play field is a water kill (splash, sinking, no crater or
  wreck) and landing blasts splash (presentation), and a fitted torpedo pod elsewhere is idle and
  reads `NO WATER`. Built in step E2c: a section's **`speed: 0`**, only on the `arena` of a level whose boss is
  **anchored** (see *Enemy*), which needs it: the scroll eases from the section before's speed to a halt over the section's first second (the
  arena's 1 s ramp, linear; `Sortie.easeDistance`: 68.8 px at 140 px/s) and the level clock halts at its `end` while the boss lives (at least 1 s
  after its start); such a boss's `t` is the arena's start (its bar, the `boss_sting`, the boss
  checkpoint), and it engages at the halt; a backdrop piece the boss lies on (Level 11's Platform
  Tiamat) is placed at the halt scroll. The formation **`field`** (a `drift` enemy's wave): `x` (the
  field's centre, px from the left edge), `size` `[w, h]` (the area; its top edge enters at the top
  edge at `t`), optional `spacing` (px, the least distance between two units; default 1.5 × the
  hit box's width) and `current` (° from straight down, positive to the right, default 0): `count`
  units placed in the area by the level's seeded scatter (built in step E2b: a jittered grid of
  cells of at least the spacing, the units in distinct seeded cells; the area's leading, bottom edge
  enters at the top edge at `t`, so every unit appears above the top edge then), scrolling with the ground and drifting at
  the enemy's `drift.speed` along `current`; with a `submerge` the share `start` of them begins
  submerged (seeded); `content` checks that a field's enemy drifts and that the units fit the area
  at that spacing on every difficulty, and lie across the play field. A ground target nest of an `enemy` may give **`current`** (°) for units whose
  `terrain` has a `drift` (built in step E2b, `GroundUnit.currentRadians`). A **destructible's `group`** (only for a placed pickup's `dropped_by`
  `group` and `last`: Level 11's containers; the objectives never count it; built in step E3a). A **trigger on `sub`**
  (`layer: sub`; built in step E2a, `GroundObjectSpec.submerged()`; `content` allows `sub` only on a
  trigger in a level with `water: true`, and `ground` for every other destructible or trigger):
  only `anti-sub` deliveries hit it, and the Smart Bomb's ring spends it at once; a torpedo seeks it
  while it is still to be freed (in its target pick beside units and parts, 2026-10-09); a
  trigger's **`easy` / `hard`** change may give its `hits` (Level 11's pod: 3, hard 5; built in step
  E3a). Built in
  step E2b (`ObjectiveSpec.Naval`, flown by `Convoy`; `Sortie.slamAllies(lane)` for step E2c's slams): a
  **`convoy`** block, a naval convoy outside the objectives (it never fails the mission and is
  rejected beside an `escort` primary): `units` in order, each an `ally` slug (one that `follows:
  stations`), a **`name`** (what `{ally}` becomes in a line: "Halvorsen"), its `station` `[x, y]`
  (px from the play field's left edge, px below the top edge, held at the scroll speed) and either
  the arena **`lane`** it glides to at the halt (1-based, of the boss's `lanes`) or **`leaves:
  true`** (it drops back off the bottom edge at the halt and returns after the boss), and an
  optional **`hold`** `[x, y]` (as the station's; below the bottom edge for off it) it glides to when
  the halt ends; `glide` (s of those moves, on the real steps), `lane_y` (px below the top edge: the
  centre of a unit in its lane), **`hold_clear`** (px the sea scrolls after the halt before the units
  glide back to their stations, each holding in its lane, away or at its `hold` meanwhile; 0 or
  absent: at once; Level 11: 552 (520 before round 33), Platform Tiamat's deck passing; built 2026-10-09, `content`
  checks a `hold` beside the play field's sides and a `hold` without `hold_clear`) and optional
  `easy` / `hard` `hp`; the units' states (afloat, hits taken, sunk) are in the
  state hash (only in a level with a convoy) and the boss checkpoint's objective tallies; `content`
  checks the stations on the play field and apart, the lanes against the boss's, and that only a
  level with an anchored boss with lanes gives lanes. The secondary kind **`afloat: true`** (E8 =
  a; with `credits` and the tracker's `label`): met at the level boss's death with every convoy unit
  that can be damaged afloat, failed at the first sinking; only with a `convoy` and a `boss`; the
  debrief's row counts the units afloat. Radio events **`ally-hit`** (each convoy unit's first hit,
  `{ally}` its name; it starts again for each unit, as `ally-lost`), **`first-telegraph`** (the
  attempt's first lane telegraph, `TELEGRAPH`; once; only with a boss with lanes; built in step E2c),
  **`first-ship-telegraph`** (round 40: the attempt's first telegraph on a lane holding a convoy ship a slam
  hurts; once; only with a boss with lanes and a convoy; `CueTrigger.FIRST_SHIP_TELEGRAPH`) and
  **`boss-part-destroyed`** (with `parts`, names of the level boss's parts other than its vital one:
  the first of them shot off; once; built in step E2c); `ally-lost` also for a naval convoy unit sinking; a `level-end` cue's `allies`
  counts a naval convoy's damageable units afloat. A naval convoy's `{ally}` is the unit's `name`
  (one voice take per name).
  M5 part F, Level 12 (user decisions F1–F9 of 2026-10-09 and the stated defaults; the simulation's
  side built in step 3: the wind, the lightning event, `clockwise`, the side whirl release points,
  the chaser waves, the sondes and their secondary, the drifting trigger, `drop_factor`, the placed
  pickups and `first-latch`; the loader's mapping and checks built in step 4 (`PartFRules`,
  `PartFLevelKeysTest`); the game's side of the event prompt, `occlusion`, `rain`, `section_db` and
  `earth-storm` in step 6): a
  **`weather`** block: `gusts` (`at`, a list of `{t, from}` with `from` `left` or `right`, the
  push's start, its telegraph `telegraph` s before; `seconds` including the `ramp` s up and down;
  `push` px/s; `telegraph` s; `easy` / `hard` changes of `push` and `telegraph` and hard's `extra`
  gust times), `drift` (`section`, `push` px/s, `from`; `easy` / `hard` `push`, 0 for none; the same
  ramps), `lightning` (`at`, the flash times; `reveal`, s the banks stay lit; the event
  **`LIGHTNING`**, value: the flash's index; nothing else in the simulation), **`occlusion: true`**
  (the banks on `low-air` hide that layer's bodies, drawn under the bank tiles with a glow; draw
  order only) and `rain` (`[t, density]` keys, 0–1, linear between them; presentation only). The
  wind adds to positions, not velocities, of the ship, the escort, units with a `latch` and Whirl
  Seeds (a whirl cluster's spiral centre too), clamped at the edges as now; never shots, mines,
  spores, pickups, sondes or drifting triggers; exactly zero, and not hashed, in a level without
  `weather`. A wave's **`clockwise`** (a `circle`'s sense of orbit on the screen) and a whirl
  cluster's `at` just outside a side edge with `from: sides` and an `edge`; a `stream` and a
  `swarm` **`from: rear`** for units with a `chase` (a `swarm` of chasers needs no `flock`: the
  cloud at its entry edge, each member's chase starting after a seeded `delay` `[0, 0.6]` s).
  **`sondes`** (`speed` px/s down the screen, `radius` px from the ship's centre that collects one,
  `at`: one `[t, x]` each, entering at the top edge; not shot, not pushed, not pulled by the magnet,
  not collected by the escort; the event **`SONDE_COLLECTED`**, value: the count) and the secondary
  kind **`collect: sondes`** (with `credits` and the tracker's `label`: met at the last sonde,
  failed when one leaves the bottom edge uncollected; the debrief's row counts the collected). A
  trigger on **`low-air`** with a **`drift`** `[x, y]` px/s in screen space instead of scrolling
  with the ground (Level 12's canister; y up as a tow's `drift`, negative down the screen; with `hits`, `easy` / `hard` `hits`, `reveals`), not pushed
  by the wind. A level **`drop_factor`** (default 1): each enemy's drop `every` is divided by it,
  rounded to the nearest, at least 1. A placed pickup's **`at`** `[t, x]` instead of `dropped_by`:
  it enters at the top edge at `t` and drifts like any pickup. An **event prompt**: a `prompts`
  entry with `event: first-latch` instead of `t`, shown (by the game, in the play field) on every
  latch of at least 0.3 s, for at most its `seconds` each time, until the attempt's first shake-off.
  The radio event **`first-latch`** (the attempt's first latch, once; it counts as a scripted
  escort line for the bark spacing). The music's **`section_db`** (section: dB, the theme's level
  from that section until the next entry, a 1 s ramp; Level 12's eye −6) and the ambience key
  **`earth-storm`** (the rain-only storm bed). The new state (the wind, latches, sondes, the
  drifting trigger) is to be hashed only in a level that uses it, so the replay hashes of Levels
  01–11 stay.
  M5 part G (Level 13 and the decisions G1–G9 = a, pending the user's review in the combined round;
  the simulation's side built in step G3, the loader's mapping and checks in step G4: `PartGRules`,
  `PartGChecks`, `PartGLevelKeysTest`, `PartGUnitsDataTest`; javadoc "M5 part G", M4's part G is
  `LevelRules`; the game's side in step G6): a level **`primary: defend`** with its **`defend`** block:
  `ally` (an ally that `follows: ground`, see *Allies*), `at` `[x, y]` (px from the left edge, px from the
  bottom edge, **y up**: where it stands while the ground halts; it scrolls in with the ground before),
  `from` (s: the synchronisation's start) and `duration` (s; its end judges an `integrity` secondary),
  **`targeted_by`** `{<enemy slug>: <mode>}` with the modes `nearest`, `alternate`, `in-arc`, `always` and
  `none` (the rules are in the [hook](../../../enemies/README.md#target-the-objective-hook); enemies not
  listed are not hooked) and `easy` / `hard` changes of `hp` (the ally's integrity) and `targeted_by`
  (merged over it); the ally has no regeneration, pays no credits and fails the mission at 0 (the
  escort's failed-primary flow). A **timed halt**: sections of **`speed: 0` without an `arena`**, allowed
  only with a `defend` primary, between sections that scroll; the level clock runs on (no boss, no
  script-time slowing), the scroll eases into the halt over its first section's **`ramp`** s and out of it
  over its last section's (default 2; smoothstep; a section inside the halt has none); `LevelData.scrollAt`
  (the backdrop's places) eases as the simulation does; `content` checks that the defend's
  synchronisation lies inside the halt and that nothing waits to scroll in at the top edge during it (a
  ground target, a placed pickup at `[t, x]` or a wave of ground units without lanes or paths timed inside
  it is rejected: use a nest from a side, walkers, lanes or `drops`). A wave's **`lanes`** (lists of
  `[x, y]` points, y below the top edge at the wave's `t`, outside the field for an entry from a side or
  the rear; ground space: they scroll with the ground and stand still in a halt) for a lane walker (a
  `ground` unit that does not walk: the Skimmer), unit i on lane i; the formation **`cross`** with
  **`from: all`** (and only it), a lane per unit on every difficulty (without lanes, as many units from
  each edge); a `circle`'s **`at`** is its centre; a `column` wave's `easy` / `hard` **`pairs`** (s
  between pairs entering side by side; hard's Stingers). A ground target nest's **`side`** `{from: left |
  right, drift}` (px/s across the screen; its `at` placements are then `[t, y]`, y up, where each unit
  crosses the edge; the mortar floe). A secondary **`integrity: <share>`** (with the tracker's `label`;
  failed at once below the share, met at the synchronisation's end; only with a `defend`). **`drops`**
  (`[{t, x, y, pickup, sprite}]`, y up: a pickup released at a point, the supply drone's patch; `sprite`
  names the drone's sprite set, presentation; the loader's name `pointDrops` beside `drop_factor`'s
  `drops()`). The radio event **`ally-below`** with **`below`** (the share, between 0 and 1; once each).
  The music's **`jingle`** `{t, fade}` (the `end_jingle` plays inside the level at `t`, the theme fading
  out over `fade` s, 1 if not given, and the debrief starts without it; the plan's `end_jingle: false` is
  this key). A level's **`water_splash`** (`all`, the default, or `naval-only`: only a burst on a naval
  unit and a naval unit's death play the water splash; only with `water: true`; presentation). An
  atmosphere split at a time is a section's `peak` running to the section's end (no new key). Allies:
  **`follows: ground`**, `damaged_by` **`dives`** (the damage of a hooked diver crossing it, once a dive)
  and **`lobs`** (`true`: a hooked mortar's direct hit landing on it). New state is hashed only in a
  level that uses it.
  Built (M5 part H, Level 14 and the act end, steps H3 and H4; decisions H1–H10 = a, pending the
  user's review; H1 changes an approved spec), final names: a level's **`root_bursts`** block
  (`telegraph` 1.0 s, `first_shot_delay` 1.5 s with `hard: {first_shot_delay: 1.0}`, `spacing` px
  between a burst's growths, `group`: the objectives' group the growths count in, and `bursts`, in time
  order: `t` the telegraph's start, `x`, `y` px below the top edge then (100–400), `growths` by enemy
  slug left to right with `easy` / `hard` `{growths}`): the growths are ground units (`terrain`) placed
  `spacing` apart centred on the point `telegraph` s after `t`, scrolling with the ground; they take no
  damage and fire nothing for the first-shot delay; the point moves sideways to keep 60 px from the ship's
  x at the telegraph; a burst is timed in a section that scrolls and grown before the arena
  (`PartHChecks`); a burst without growths (the Ravager crater) is the backdrop's, not data. Boss
  attacks from parts: the enemy **`mortar`** pattern is the boss lob (`BossSpec.Pattern.LOB`: its
  `mortar` block, the hook `ring`), **`laser-sweep`** the boss sweep (`Pattern.SWEEP`: its `sweep` block,
  the hook `sweep_arc`; its `interval` the turn in an alternation; fired by the part its phase ends on; round 41:
  an optional `lead` in the `sweep` block, ° from 0 to the arc, puts the arc's first edge, the telegraph's line,
  that far beside the ship's bearing on the side the beam comes from instead of centring the arc on it,
  `EnemySpec.Sweep.withLead`; the Siege Spire's 12),
  an attack's **`staggered: true`** (aimed or mortar, only a boss's fired by several parts: part k of n
  k × interval ÷ n after the first; `Attack.stagger`); a part's **`aim: down`** (round 34, P2: the part
  fires its `aimed` attack straight down the screen instead of at the ship, the Siege Spire's pods 1 and
  3; `Attack.down`, `BossActions.down`); an aimed boss attack's **`aim_turns: {every, glow}`** (round 40, the
  Siege Spire's pods: every `every` s the next living part in turn fires one shot at the ship after glowing
  `glow` s, the others firing as the attack says between; only an aimed attack that does not rotate, only a
  boss's; `BossSpec.AimTurns`, `SiegeSpireTest`); a window spawn's **`ambush: {most}`** (an ambush
  launch flying its enemy's rear-ambush path: only a unit with the `ambush` and `hover` movements; no
  `arc` or `glide`; `Spawn.ambushMost`); an anchored boss without `lanes` (`Lanes.NONE`), its parts'
  layers `sub`/`ground` as before (the core reaches `air` by the move), its phase **`move`** a lift
  (`to`, `layer`, `descend` = the lift's seconds, `pose`), its `movement.sine` the sway after the lift
  (only with a move, `PartHChecks`); a phase's `drop` with a move kept (the shockwave's patch). A `groups`
  secondary may name its tracker `label` (`ROOTS n/8`). The stump is presentation (the boss's state), not
  a key. A **daylight section after an arena** is an ordinary section (no new key). The act end needs no
  new key: the Act 2 `outro` is the Act 1 schema (`music`, `pages`). M5 part H (H3) in the simulation:
  `RootBurst` + `GroundUnit.fromBurst` (telegraph point in ground space, 60 px ship
  clearance, invulnerable silent growth, groups counted from the script); boss patterns `LOB` (mortar
  gun) and `SWEEP` (`EnemySpec.Sweep` on the attack), `Attack.stagger`; window `Spawn.ambushMost`
  (planned rear paths in `Launches`, a ring of 8 real-step bottom warnings, entry REAR from re-entry,
  cap alive); an anchored boss without lanes, its phase `move` = lift (smoothstep, layer at the end,
  stump = parts destroyed/armoured at the lift); events ROOT_BURST_TELEGRAPH/GREW, SPIRE_MAW_OPENED,
  SPIRE_LIFT/LIFTED, SPIRE_SWEEP_TELEGRAPH, SPIRE_SWEEP; new state hashed only when used.
  M7 part B, for [Level 15](../../../campaign/act-3-red-dust/level-15-olympus-descent/README.md) (loaded and
  checked in step B2, mapped for the simulation in step B3, content's `MarsRules`; the defaults pending the user's review in round
  38): a moving section's **`ramp`** (s: from the section's start the scroll eases from the speed before it
  to its own, smoothstep on the level clock; none, an instant change, if not given, so the levels before
  hash as they did; a timed halt's `ramp` keeps its meaning; not on an arena), integrated exactly by
  `LevelData.scrollAt` and `tools/sync_tables.py`; **`launch: drop`** (the ship, and the escort in his
  slot, drops from above the top edge to the start spot over `launch_seconds`, controls ignored as in
  any launch; the bottom launch if not given); **`no_surface`** (the ground areas where no Burrower
  surfaces, each `t` (its centre passing the middle of the screen, as a backdrop piece), `x` and `size`;
  they scroll with the ground; the spot rule picks the nearest allowed spot, else skips that surfacing,
  which still counts; only in a level with a burrowing enemy); a `field` wave of a burrowing unit (its
  area where the units enter buried); the radio event **`first-surfacing`** (the attempt's first Burrower
  breaking the surface; once; only with a burrowing enemy); a secret's `radio` line's optional
  **`portrait`** (a generic portrait, as a cue's: Ares Control's `generic-colonial`); the threat
  profile's `layers` are layer names (`buried` among them, checked). The per-section ambience is the
  existing `ambience_changes` (Level 15: `mars-entry`, then `mars-dust` from section 3). The intel's wave
  strip marks every wave of a burrowing unit (`Intel.Mark.Kind.BURROW_FIELD`).
  M7 part C, for [Level 16](../../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md) (loaded and
  checked in step C2 by content's `TerrainBlock` and `TerrainChecks`, mapped in step C4; the defaults accepted in
  round 39, 2026-10-18): **`terrain`** (`left` and `right`, each a wall's collision line, its foot,
  as `[scroll, x]` points or `[scroll, x, height]`: the scroll is the ground scroll at which the row reaches
  the play field's **top edge**, x px from the left edge, linear between points, no wall at a row without
  points; `height` 0 (flat) to 1 (the wall's full rise to the plateau at about 1.25), 1 if not given,
  presentation only (the canyon mouth's and the outflow's ramp); `islands`, each a `name`, a `look` (the
  art's `<look>-top` and `<look>-face`) and `points`, a polygon of `[x, scroll]`; `contact`: `damage` per
  difficulty, `push` px along the normal over `push_seconds`, then a `cooldown` in which a touch only
  clamps). The one coordinate helper is `TerrainBlock.row(groundScroll, y) = groundScroll + y − 540` (y up):
  a ground placement `[t, x]` (its bottom edge at the top edge at t) has its centre on row `scrollAt(t) + h /
  2`, a backdrop piece passing the middle at t on row `scrollAt(t) − 270`. The validator: the flyable width
  at least 180 px and a channel beside an island at least 130 px on every row where a line bends, a wall
  moving at most 0.5 px sideways per px of scroll, the islands between the walls; no ground target, secret
  trigger, released pickup or crawl point in rock, a gate's anchors within the 24 px foot band of a collision
  line, a burrowing field's area on the flyable floor; wave paths against rock are flown by a test over the
  real sortie (`TerrainClearanceTest`, step C4), not re-simulated. A ground target's **`link: pairs`** (an
  enemy nest of a unit with a `link` attack: its placements pair up in order, the same t, the left anchor
  first, on every difficulty's list; two pairs at least 1.5 s apart): Level 16's gates. A wave's
  **`crawl`** (a crawling unit's ground path, `[scroll, x]` points in the walls' rows: its head starts at the
  first point at the wave's t with the body straight behind it toward the lower rows, then crawls the path;
  only and always a crawling unit's wave, only with terrain). The secondary **`links: true`** (every
  tendril of the linked nests broken before it leaves the screen; its tracker `label`, Level 16's
  `BARRIERS`; only with linked nests). The radio event **`first-wall-contact`** (the attempt's first touch
  of rock; only with terrain). The backdrop layer **`plateau`** (optional in `scroll_factors`, above the
  ground, about 1.25: drawn only beyond the projected walls; only with terrain). The intel's wave strip
  marks each linked nest at its first pair (`Intel.Mark.Kind.GATE`) and each crawling wave flown at medium
  (`CRAWLER`), and a linked nest's unit joins the intel's enemies in order of appearance.
  M9 part E, for [Level 32](../../../campaign/act-5-the-belt/level-32-deep-core/README.md) (loaded and checked in steps
  E1/E2 by content's `TerrainBlock`, `LevelData` and `DeepCoreChecks`, mapped in step E2 by `DeepCoreRules`, flown from
  steps E1/E3; the defaults accepted by the user in round 65): the terrain's **`forks`** (a list of `{island: i}`,
  the `islands` index, fork 1 first: at the step an island's tip, its lowest row, reaches the ship's row the channel
  holding the ship's centre is chosen, left of the island's middle `left`, else `right`; deterministic, the input decides
  it; `BRANCH_CHOSEN`); a wave's and a ground target's **`branch`** (`fork` from 1, `side` `left` or `right`: a front wave
  only, flown in its own channel only when that side is chosen, the other side's never spawns; a ground target dormant,
  dark and not hittable, never homed on or paid, unless chosen, one that entered before the choice waking at it with the
  1 s tell; not with `side`, `burst`, `holds` or `dormant_until`); the validator: the fork exists, a branch item at or
  after its fork's latest choice (the tip at the bottom edge) and while its island is on the screen, a branch target in
  its side's channel. A ground target's **`crate`** (credits in Act 1 terms: a destructible lock, a blast door's, that
  frees a cache's crate paid at the secrets' rate but counting no secret; not with `reveals`) and **`blast`** (`radius`
  px, `damage`: a mining charge on `ground`, `hp: 1`, no `crate` or `reveals`: destroyed by anything, its burst hits
  every counted enemy, air or ground, hardened and parts included, within the radius and pays as the player's kills;
  never the ship, Rook or a ground object, so no chain; `CHARGE_BLAST`); a blast door's lock (`sprite: blast-door*`)
  and a ground-sweeping unit's emitter at least 40 px inside the field (QE-1 a), the sweeper never beside a passage
  narrower than its reach plus 80 px nor over a fork island within its reach's rows. The radio events **`branch`**
  (`fork`, `side`: the subject `"1:right"`) and **`topple-crush`** (the first topple that crushes a unit). The intel's wave
  strip marks each fork at its branches' first item, with the richer side (more credits in its bounties and caches).
  A forked level's credit budget table renders a row per route (`LL`, `LR`, `RL`, `RR`) and their mean as the total.
  M7 part D, for [Level 17](../../../campaign/act-3-red-dust/level-17-dome-siege/README.md) (loaded and checked
  in step D2 by content's `LevelData` and `SiegeChecks`, mapped in step D4, flown from steps D1/D3; the
  defaults pending the user's review in round 40): **`allies`** (ground allies outside the objectives, which
  scroll through with the ground and never fail the mission: `units`, each an `ally` slug that `follows:
  ground` and counts `hits`, its `name` (`{ally}` in the radio's `ally-hit` and `ally-lost`) and `at` `[t, x]`
  (it enters at the top edge at t, its bottom edge there, as a ground target), optional `size` (its footprint
  instead of the ally's, the hit box keeping the ally's margin: Eden's 144) and `sprite`; `targeted_by` `{<enemy
  slug>: <mode>}` as a defend's, per unit type (every wave of a listed enemy is hooked); `bomb_row` (px below
  the top edge: the row a hooked bomber holds while it steers to the next ally coming down, at its own speed,
  and drops one bomb run as the ally's footprint passes beneath it) and `bomb_slot` (px between a hooked
  wave's bombers' x slots round the ally's x); optional `bomb_run_in` (round 41: s a hooked bomber flies on the
  play field before it may drop, over an ally before then it keeps its load and steers on; `GroundAllies.
  withBombRunIn`; Level 17's 2.5); `easy` / `hard`: `hits` and `targeted_by` merged over the
  block's); a hooked mortar's objective lob lands where its ally will be (ground space, the marker on the
  ground). The secondary **`untouched: true`** (none of the level's ground allies hit; its tracker `label`,
  `DOMES` until round 41; failed at the first hit, met when the last ally leaves the bottom edge; only with
  `allies`) or (round 41) **`standing: true`** (none of them broken, hits allowed: Level 17's "No dome lost";
  failed when the first breaks, met when the last leaves the bottom edge; only with `allies`, never with
  `untouched`; `Secondary.standing`); a walker with a `roll` walks its paths between the allies, its body's hit box never over an
  ally's (`SiegeChecks`, round 40 R.c). The radio event **`first-ring`** (the attempt's first aura ring a
  Herald sings onto a unit; only with an aura enemy; round 40 K.a). A pickup's **`dropped_by.enemy`** (a wave's carrier counted among that enemy's units of the wave
  only: Level 17's Herald among its bombers). **`flak`** (presentation: `from` s, a burst on `low-air` every
  `every` s over the lower field, hitting nothing: Level 17's militia guns). The music's ambience
  `mars-colony`. The validator: the allies' footprints inside the play field and clear of one another on the
  ground, `targeted_by` naming known enemies, `bomb_row` on the play field, no ground target within 100 px of
  the `helix-compound` backdrop placement, a `dropped_by.enemy` flying in its wave. The intel's wave strip
  marks the sections the ground allies enter in as phases (`Intel.Mark.Kind.PHASE`).
  M7 part E, for [Level 18](../../../campaign/act-3-red-dust/level-18-dust-storm/README.md) (loaded and checked
  in step E2 by content's `LevelData` and `StormChecks`, mapped in step E4, drawn from step G1; the defaults
  pending the user's review in round 41): **`visibility`** (presentation only, as `flak`; the simulation never reads
  it: the dust storm's clear radius round the ship, M6 = a, MA.2 = b): `radii` (names: px, Level 18's `deep` 170
  and `wall` 150), `ramps` (each `{from, to, radius}`: from `from` to `to` s the radius eases (smoothstep) from the
  one before it, the full view before the first, to `radius`, a name of `radii` or `full`; in time order, not
  overlapping, inside the level), `falloff` (px, the soft ragged edge centred on the radius, broken by the look's
  drifting noise fields), `easy` / `hard` (the radii by name on those difficulties); `LevelData.Visibility.radiusAt(t)`
  (and `radiusAt(t, difficulty)`) gives the radius, `full` as the field's diagonal plus the falloff. The validator:
  every radius on every difficulty at least the pull on the ship (`ship_radius`) of every unit the level flies on it.
  The music's **`motif`** (`from`, level time, and `sections` `{n: false}`: track 9's motif stem over the theme
  from `from` on, in every section the theme plays except those turned off; `Music.motifOn(section, t)`; `from`
  inside a section the theme plays). The ambience `mars-storm`. The Dust Devils' pairs and trio fly as a `pack`,
  each wandering round its own path (the path its centre line).
  M7 part F, for [Level 19](../../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md) (loaded, checked
  and mapped in step F2 by content's `LevelData`, `FoundryChecks` and `FoundryRules`, flown from step F3, drawn
  from step G2; the defaults pending the user's review in round 42): **`belts`** (`width` px, `speed` px/s up the
  screen relative to the ground, `runs` each `{x, from, to}`: the run's bottom end enters at the top edge at level
  time `from`, its top end leaves at the bottom edge at `to`, where the backdrop's belt pieces lie; the simulation's
  `GroundSpec.Belt` gets the ground distances `scrollAt(from) + 270` and `scrollAt(to) − 270`; ground units on a
  belt ride it; no standing nest is placed on a belt's column while it passes). **`vents`** (`width` px, `tell`,
  `burn`, `period`, `ripple` s, `bullet` class, `easy` / `hard` changing `tell`, `period`, `ripple`, `rows` each
  `{t, xs}`: a row enters at the top edge at `t` and fires every period as a ripple from its first x; the period
  holds the whole ripple on every difficulty, the grates apart and on the field, every row before the arena).
  A ground target's **`arc: up`** (an enemy nest turned 180°: its guns' facing up the screen; only on a unit that
  fires within an arc). A section's **`hold_after_boss`** (s, an arena's: the clock stays halted after the boss's
  death chain); a moving section right after an arena with a `ramp` eases up from the halt over it (the wreck
  crawl). The secondary's **`allowed`** (an `escapes` objective's units that may get away; with it the objective
  may name its counter `label`). The music's `stems` value **`motif`** (the theme's stems silent, the motif layer
  alone; `Music.motifAlone(section)`; only with the music's `motif`). The ambience `mars-foundry`.
  M7 part G, for [Level 20](../../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md) (loaded and checked
  in step G2 by content's `LevelData` and `RetreatChecks`; flown from steps G1 and G3, mapped in step G4, drawn by
  steps GA/GB; the defaults pending the user's review in round 43): a section's **negative `speed`** (M5 = a: the
  ground moves up the screen and the direction of travel is down it, a reversed section, `Section.reversed()`;
  never on an arena; its `ramp` eases from the speed before it, through zero at a turnabout;
  `LevelData.travelAt(t)` the sign). A level that reverses never starts reversed and has no terrain, road, belts,
  vents, water, holds, boss or arena, timed halt, convoy, ground allies or set pieces (the travel frame's scope).
  **Entries by travel**: a wave's `from: front` is the edge ahead in the direction of travel (the bottom edge once
  reversed), `rear` the edge behind (the top); its `paths`, `at` and a field's area are written in the travel
  frame (y below the entry edge, as in a forward level) and the simulation flips the units that spawn while the
  travel is reversed; a ground target's `at` is when it enters at its edge by travel, and it carries no `arc`
  there (the frame turns a nest's arc). A front wave's **`warning`** without a `loop_back` only while the travel
  is reversed: the BOTTOM lesson (its edge warning `warning` s ahead). The formation **`pursuit`** (one group of a
  unit with a `pursuit` block, `from: rear` on every difficulty, entering while the travel is reversed, no
  `paths`) with its wave's **`pursuit`** block: `break_at` (level time of the break-off, after its entry, before
  the level's end, while still reversed; no two pursuits overlap), optional `refill`, `refill_every`,
  `lunge_every`, `tell`, `band`, `lag`, `dive`, `overshoot`, `climb` (instead of the unit's); its refills, one
  every `refill_every` s after its entry while before `break_at`, count in the density (a refill with no room in
  the flock is not spawned and counts nowhere). The music's **`ambience_layers`** (each `ambience`, `from`, `to`
  level time, `fade_in`, `fade_out` s: a second ambience loop over the level's own; Level 20's basin rumble;
  `AmbienceLayer.level(t)`). The flak's **`band: top`** (bursts in the top band instead of over the lower field;
  `Flak.bandOrDefault()`). The rear-wave bark's **`top_lines`** (the wingmen data's "Up top!" variants in a
  reversed section). The level's **`ridge`** (presentation, round 43 Q.4: the Dust Colossus's
  travelling ridge, Levels 20 and 21): `t` (level time the head surfaces), `seconds` (the head's crossing, > 0),
  `from` and `to` (`left` / `right`, opposite edges) and `depth` (px below the top edge the head surfaces, inside the
  field); `LevelData.Ridge`, read by the game's `RidgeLooks` (the track's S and run up the strip are its constants;
  the tail follows 7 s after the head) and the pipeline's `SpriteUse`.
  M7 part H (step PH2; design/campaign Level 21, design/enemies/bosses/dust-colossus): a **moving arena's `ramp`** (an
  arena of speed > 0 with `arena: true`; only in a level whose boss breaches, `ColossusChecks`): the arena eases in
  from the section before it over `ramp` s (Level 21's 140 → 20 over 2 s, `Section.easeSeconds()`), and it
  **crawls** (`ColossusRules`: a moving arena of a breaching boss → the simulation's `Section.crawl`: its ground
  scrolls on while the level clock is held at its end, a kill's jump moves the clock only). A breaching boss's stat
  block (`ColossusBlock`): its body's `layer: buried`, a part's `layer` may be `buried` (the mouth until its rise, the
  vents until they rise with it); **`movement.breach`** (`tell`, `rise`, `open`, `bite_tell`, `dive`, `buried` s,
  `buried` ≥ `tell`; `warn` s of a side or rear point's edge warning; `intro` s from the arrival to the first tell;
  optional `glow` s before each ring, 0.5; `points` `{front, left, right, rear}` each `[x, y]` px from the left and
  below the top edge; `follow` the same four, each `[min, max]` of the ship's x (front, rear) or y below the top edge
  (the sides) a following phase clamps to; `orders`, one per boss phase in order, each `{phase, points, follow}` with
  `points` from `front`, `left`, `right`, `rear` or `under` (alone); `bite {radius, bullet}`) — only on a boss without
  an anchor, stride, hover or descent; the boss's **`suction`** (`speed` px/s within `inner` px, falling off to 0 at
  `outer`), **`vents`** (`parts`, the destroyable vent parts; `distances` px from the mouth along the ridge; `attack`,
  the vents' burst; `phase`), **`eruption`** (`marker` s, `radius` px, `bullet`, `inset` px, `open`, `buried` s,
  `phase`) and **`debris`** (`every` {phase name: s}, `size` [min, max] px, `hp`, `speed` px/s, `acceleration`
  px/s², `layer`, `contact` size tier), all only with a breach; the hooks **`breach`** (`bite_tell`), **`suction`**
  (`speed`), **`eruption`** (`marker`). `ColossusRules` maps them to `BossSpec.Breach` (the points turned into the
  simulation's frame, y up from the bottom edge), `Suction`, `Vents` (the burst as the attack's index; the vents rise
  only in a phase listing that attack), `Eruption` and `DebrisFlow` (the time by phase index, 0 for none). An act's
  **`next_act`** (`act`, `name`, `setting`: the act after the release's last, not built yet, as the release's end and
  the hangar name it; `ActData.NextAct.comingSoon()`, `hangarLine()`).
  M9 part C (Level 30; loaded and checked from step C1, mapped in step C2): the level's **`ground: none`** (no
  ground layer, from Act 5: the bomb deliveries idle, no ground targets, terrain or ground units) and a mine layers'
  wave's **`mine_lines`** (`rows` px below the top edge and `gaps` px from the left edge, unit i taking `rows[i % n]`
  and the cluster boundary nearest `gaps[i % m]`; the units its `interval` apart); a tagged `escapes` secondary may
  name its counter (`label`) when a difficulty allows some away (Level 30's `CONVOY`).
  M10 part A (step A2b; loaded, checked and mapped by content's `JovianHazardRules` and `JovianHazardChecks`, flown by
  step A1b's passes): the weather's **`strikes`** (`t`, `x`, optional `track: true` and `skip`: a told lightning
  column, a tracking one locking on the ship's x at its tell, hard's only, so skipped on easy and medium; never two
  tells within 1.5 s on a difficulty, none within 1 s of a wave's entry; a level with strikes and no wind flies calm
  weather with them), the level's **`eruptions`** (vents `t`, `x` and an optional `period` `[min, max]` s, the world's
  5–7 if not given; the level's first eruption plays alone: no wave enters from its vent's earliest tell to the end of
  its latest), the level's **`turn`** (s on the level clock: the `hunts: <tag>` links are live from it; only in a level
  with such a wave) and **several held gates** (more than one `arena: true, holds: <group>` section, Level 41's pylon
  gates: never the first section or two in a row, each its own group, its holders entering after the gate before it
  ends, no `escort_swap` with more than one; no boss). The strikes' and eruptions' numbers are the world's, once:
  **Jovian** (`world/jovian/data.yaml`, `JovianData`): `strike` (`tell`, `live` s, `width` px, `ship` and `enemy`
  damage, `entry` and `spacing` s, `easy_clearance` px), `eruption` (`tell`, `rise`, `fly`, `fall` s, `fan` °, `speed`
  and `end_speed` px/s, `rock` box, `hp`, `damage`, `period` and `rocks` `[min, max]`, `easy` and `hard` changing
  `tell`, `rocks` and `speed`) and `banks` (the foreground banks' caps: `cover` and `easy_cover` shares of the play
  field at a time, the ship's `band` px and `band_seconds`, checked against the backdrop's `banks`,
  [schemas-backdrops](../schemas-backdrops/README.md)), rendered into the
  [Jovian system](../../../world/jovian/README.md#hazards--set-pieces)'s *Hazards & set pieces*.
  M10 part B (Level 36; loaded and checked from step B2, flown from step B1): a debris chunk's **`reveals`** (a small
  chunk that frees the named secret's hidden crate, the secret's `crate`, when the player's shot breaks it: the
  pathfinder pod) and the radio event **`first-strike-kill`** (the attempt's first enemy a lightning strike kills, once;
  only in a level with `weather.strikes`). An act's briefing page may name its **`music`**: `motif` (the act's A
  theme's motif stem alone in place of the briefing music, faded out over 1 s as the page opens and back over 1 s on
  the next: Act 6's page 3).
- **Act** (`campaign/<act>/data.yaml`): `levels` `[first, last]` (global numbers), `title_card`
  (`act`, `name`, `line`), `briefing` (pages as a level's); `outro`, the act-end
  outro after the last level's debrief: `music` (a music file's name, `act-complete`: track 24
  under the first page, played once) and `pages` (as the briefing's: `speaker`, `line`, optional
  `expression` and `image`, and `notes.image`, the README's description of the image); the loader checks that every level's act exists and includes it.
  Saves (not a data file; see [saves](../../../systems/saves/README.md)): format version 2 (part G)
  adds `stats.levels`, per won level number its banked `credits` and `kills`. Planned (M5 part A):
  format version 3 adds `escort` (`hired`, `side`, `fitted`, `guns` with `item` and `level`,
  `armour`), migrated from version 2; version 4 (M7 part A, Q1 = a) adds `bays` (the utility bays
  bought, 0 or 1), migrated from version 3 with 0; version 5 (the power dial, 2026-10-21) adds an item's
  optional `power` (its power setting, 1 … `level`, written only below full power; a missing one is full
  power), migrated from version 4 unchanged; version 6 (M8 part A) adds the escort's `flies` (`rook` or `bulwark`,
  the slot's choice) and `bulwark` (`owned`, `armour`), migrated from version 5 with `rook` and no Bulwark at its full
  120. M7 part B (L.6 of round 36; a default pending the
  user's review in round 38): the story flag **`act-intro:N`** records act N's intro as shown (set
  whenever a briefing opens an act); the version 3 migration adds `act-intro:1`, and `act-intro:2` from
  Level 08 on (the release had Acts 1–2), never `act-intro:3`; Continue on a save whose next level
  opens an act without its flag plays that intro once (`CampaignRoute.onContinue`), then the hangar.
