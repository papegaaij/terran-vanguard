---
title: Ship systems
design: approved
implementation: in-progress
art: final
depends-on: [../generator, ../../ui/hangar]
updated: 2026-10-27
---

# Ship systems

## Summary

The engine (a core component, always fitted) and the modules that go into the **utility bays**:
two bays at the start, and a third that can be bought from act 3 (price 5 000). Utility modules
are the "nice extra options": they make the ship better at a job without adding another gun.

## Design

### Engine (core)

<!-- data: engines -->
| Model | Speed | Draw | Price (first draft) | Available |
|---|---|---|---|---|
| Mk I | 270 px/s | 0 MW | starter | start |
| Mk II | 290 px/s | 1 MW | 1 200 | act 1 |
| Mk III | 315 px/s | 1 MW | 3 500 | act 3 |
| Mk IV | 345 px/s | 2 MW | 8 000 | act 5 |
<!-- /data -->

### Utility modules

<!-- data: utility -->
| Module | Effect | Levels | Draw | Price | Unlock | Design |
|---|---|---|---|---|---|---|
| Sensor suite | Improves hangar intel detail (see below) and shows off-screen threat arrows at L2+ | L1–L3 | 1 | 800 / 2 000 / 4 500 | start | idea |
| Pickup magnet | Pickups within 72 / 108 / 144 px of the ship fly to it at 240 / 300 / 360 px/s (see below) | L1–L3 | 1 | 600 / 1 500 / 3 000 | act 1 | approved |
| Salvage scanner | +10 / +20 % credits from salvage pickups and hidden crates; a glint marks the objects that hide a secret (see below) | L1–L2 | 1 | 2 500 / 6 000 | act 2 | approved |
| Targeting computer | Homing turn rate +20 % (Stormhawk weapons), fading HP bars under damaged enemies, brackets on open weak points (see below) | L1 | 1 | 3 000 | act 2 (from L07 with the L06 [data core](../../systems/economy/README.md#data-cores), otherwise L08) | approved |
| Evasive thrusters | Double-tap a direction (or the Dash button): 72 px dash in 0.12 s, 0.25 s invulnerable, 3 s cooldown (see below) | L1 | 2 | 4 000 | act 3 | approved |
| Auto-repair nanites | Repairs 1 armour per 4 s, up to 50% of max armour; a hit pauses it 1 s (see below) | L1–L2 (2 s at L2) | 3 | 6 000 / 12 000 | L24 | draft |
| Pressure hull | Removes the under-water drag (see [europa](../../world/europa/README.md#under-water-rules) and below) | L1 | 1 | 2 000 | L22 | draft |
| Ascendancy IFF spoofer | Ascendancy turrets hesitate 0.5 s before firing | L1 | 2 | 5 000 | act 6 (story) | idea |
<!-- /data -->

### Pickup magnet

A fitted Pickup magnet reaches out to the radius of its level around the ship's centre. Every
pickup inside that reach (salvage, overdrive, shield cell, armour patch, special charge, a secret's
hidden crate once it has been released, a data core) stops drifting and flies straight at the ship
at the magnet's pull speed, until the ship's normal collection radius (see
[ship](../ship/README.md)) takes it; a pickup outside the reach drifts down as usual. Only
pickups are pulled: the beacons, containers, cranes and tows that release them stay where they
are. A pulled pickup keeps its 6 s lifetime, and nothing is pulled while the ship is wrecked. With
a magnet in both bays the better level counts. The numbers are in the *Utility modules* table
above: at L1 a pickup at the edge of the reach is taken about 0.15 s later, at L3 about 0.3 s.

### Targeting computer (user decision D6 of M5 part A)

A fitted Targeting computer (one level) does three things in flight; a second one in the other bay
adds nothing.

- **HP bars.** A thin bar (2 px, the enemy's hitbox width, at least 12 and at most 48 px) 4 px
  under every damaged enemy that is not `tiny`: it appears with the first hit, shows the unit's
  remaining HP (a multi-part unit's living parts summed) in the HUD's phosphor green on a dark
  trough, amber below 30 %, stays 1.5 s after the last hit and then fades out over 0.3 s. Bosses,
  mid-bosses and set pieces get none (the boss bar and the brackets cover them), nor do ground
  objects without a stat block (containers, beacons, triggers). Drawn over the units, under the
  bullets.
- **Weak-point brackets.** Pulsing lime corner brackets (2 px, 2 pulses per second between 60 and
  100 % opacity) round every part with a damage `multiplier` of a boss, mid-boss, set piece or
  `large` or bigger unit, **while that part can take damage**: its phase has exposed it, its
  window is open, the boss is not in an invulnerable move. They go when the part is destroyed or
  closes.
- **Homing turn +20 %.** The Stormhawk's homing weapons turn 20 % faster: the Micro-missile Pod's
  and the Hornet Launcher's missiles (their `turn`, also the Micro-missile's doubled rate on a
  high-air boss) and the Swivel Gun's turret slew (360 → 432°/s). Not Rook's guns.

It enters the shop from the first hangar visit after M5 part A: from the L07 visit for a save
that holds the L06 [data core](../../systems/economy/README.md#data-cores)'s unlock, otherwise from
the L08 visit (`available: act 2`).

### Salvage scanner (user decision D7 of M5 part A)

- **Credits.** The best fitted scanner's level adds **+10 %** (L1) or **+20 %** (L2) to the credits
  of every **salvage pickup** (small, medium, large) and every **secret's hidden crate**; not to
  bounties, objectives or the grade bonus. The bonus multiplies the payout before its one rounding
  (Act 1 value × act factor × difficulty income × the bonus, half to even), shows in the floating
  number and counts in the debrief's salvage and secrets lines. It is **outside the budget**, like
  the grade bonus: the typical haul, the level budgets and `BalanceTest` leave it out.
- **Glint.** Every object that reveals a secret (a trigger, a destructible with a hidden crate,
  Level 07's tow cable) shows a short sparkle (4 frames in 0.3 s, every 1.5 s) at its centre while
  it is on the screen and not yet spent, with both levels, in the dark as well (a dark trigger still
  takes hits only while lit). A data core's secret glints too. The glint's own sprite is the star
  glint chosen in concept round 28: a 23×23 four-point star with a warm white core over a soft gold
  halo (`glint-secret`, `tools/art/secret_glint.py`).

### Evasive thrusters (Act 3)

Approved in concept round 36 (user, 2026-10-13, D.25). The
*Utility modules* row above is rendered from [data.yaml](data.yaml), whose `dash` block holds the
numbers below (M7 part A).

| Property | Value |
|---|---|
| Module | One level, 4 000 cr, draw 2 MW, one bay; on sale from the L15 visit (`available: act 3`). A second one adds nothing |
| Input | The **Dash** action of the [controls](../../ui/controls/README.md): a double-tap of a direction (the second press within **0.25 s** of the first press's release, nothing else pressed in between), or the bindable Dash button (V, the left bumper), which dashes in the direction held, or straight ahead with none. Only while the module is fitted; otherwise neither does anything |
| Dash | **72 px** in that direction (8 directions; a diagonal moves 72 px along it) over **0.12 s** (600 px/s), the ship's own speed set aside for that time; afterwards it keeps the speed it had before. Play field edges clamp it; a canyon wall or rock island stops it at the wall without wall contact |
| Invulnerable | **0.25 s** from the dash's start: enemy bullets pass through the ship (they are not absorbed), contacts and hazards do not hurt. The hull shows 3 fading afterimages along the dash |
| Cooldown | **3 s** from the dash's start; a dash pressed while it cools does nothing (no sound, so a missed double-tap in normal flying is silent) |
| HUD | A small dash pip beside the shield row: empty while cooling, filling over the 3 s, a short flash when ready (see [HUD](../../ui/hud/README.md)) |
| Sound | A short thruster burst (the engine's family, not bubbly) |
| Replays | Detected from the direction inputs per simulation step, so the same input gives the same dash in a replay |
| Rook and drones | Unaffected; Rook reacts to the ship's new place with his usual delay, the drones keep their orbit and station round it |
| Reverse scroll (L20) | Directions are the screen's |

Power: the thrusters' 2 MW fit the typical Act 3 loadout, which is at 13.5–14 of the Mk III
generator's 16 MW by L19 (14 MW until the user's power room of 2026-10-21; see the
[Ion Beam's power check](../weapons/ion-beam/README.md#price-and-power-m11)); next to a Shield Mk III or
another module a player turns a weapon down with the [power dial](../README.md#power-budget).

### Third utility bay (Act 3)

- On sale once from the L15 visit at 5 000 cr (*Utility bays* below); the hangar shows a third
  utility slot only after it is bought, and the save records the bought bay (format: the slot
  `UTILITY_3` the loader already knows; M7 part A, user decision Q1 = a: an empty bought bay has no
  loadout entry, so save format version 4 records the bays bought in a `bays` field of its own, see
  [saves](../../systems/saves/README.md)).
- Any utility module fits it, the same module as in another bay included (a second Targeting
  computer or thrusters still adds nothing); its module's draw counts like the others'.
- Selling it is not possible while a module is fitted in it; sold back at 60 % like other gear.
- The typical plan does not buy it in Act 3 (5 000 against the Mk III and the Act 3 guns); it is
  the reward for a thrifty player and the utility modules' answer to the power cap.

### Hydro-kit (automatic)

*Act 4 draft, decided in round 48 (2026-10-22) (the act's open question: keep it automatic; recommended yes).*

Not a module and not for sale: before L23 every Stormhawk gets a free field refit for underwater
play (a story beat: Thera Deep's yard fits it in the hangar visit before the descent). It takes no
slot and draws no power, and it needs no purchase, so a thrifty player is never locked out of
Act 4. **What it is in the game:** the ship's *look* under water (a sealed hull, ducted thrusters
and two floodlights, concept in round 48) and the story's reason for the drag; the factors
themselves come from the level's medium and are defined in
[europa](../../world/europa/README.md#under-water-rules). In a level without water (Act 5 on) the
kit is stowed and the ordinary Stormhawk sprite is drawn. Rook's variant-C Stormhawk gets the same
kit. The alternative (a purchasable module that must be fitted) was rejected: with the pressure hull
already optional it would put a mandatory purchase at the act's start.

### Pressure hull (L22)

*Act 4 draft, decided in round 48 (2026-10-22) (Q1 = b).* A utility module (1 MW, 2 000 cr, in
the shop from the visit before L22; the numbers it reads are in the *Utility modules* table above,
whose Effect text is regenerated from [data.yaml](data.yaml) when part A changes it).

- **Effect:** removes the **drag** of the under-water rules: acceleration is back to normal (the
  ship flies as on the surface). Under the recommended (b) rules there is no top-speed or
  shield-regeneration penalty left to remove; under the as-drafted rules (a) it removes those two
  and the drag stays.
- It does nothing above water, against the Siren's sonar slow, or for the bullet speed (× 0.7 for
  both sides), and it is not needed to beat any level: the recommended typical plan buys it at the L24
  visit (see the [hand plan](../generator/README.md#act-4-hand-plan-draft)).
- Two hulls do nothing more than one (the thrusters' rule).

### Auto-repair nanites (L24)

*Act 4 draft, decided in round 48 (2026-10-22); the unlock level 24 is ours (the roster said "act 4").*

- **Effect:** a fitted module repairs **1 armour point per 4 s** (L2: per 2 s) while the ship is in
  flight, but only up to **50 % of the armour's maximum**: it never lifts the armour above half, so
  it softens a long level without replacing the repair bill or the armour tiers. Nothing repairs
  while the ship is wrecked, and a retry starts at the level's start armour as always.
- **Price and power:** 6 000 cr (L2 12 000), **3 MW**, the heaviest module in the game: it is the
  reason the Mk IV generator matters (see the
  [hand plan](../generator/README.md#act-4-hand-plan-draft)). Not in the typical plan.
- **Economy:** the armour it restores in flight is not repaired at the hangar, so it saves the
  repair cost of those points (about 5 cr a point on medium, 10 on hard); at most
  about 5–10 points a level, worth 25–100 cr: it never pays for itself in credits, it is bought for
  the safety. Rook's and the Bulwark's armour is not repaired by it.
- HUD: a small `+` pip on the armour bar while it repairs; a hit pauses it for 1 s.

### Sensor levels and hangar intel

The [hangar intel panel](../../ui/hangar/README.md) shows the next level's threat profile. The
sensor suite decides how much of it is visible:

| Sensor | Intel shown |
|---|---|
| none | Setting, dominant layers, main attack direction |
| L1 | + all attack directions with the share of waves per direction, density (1–5), hazards, and an OBJECTIVE field in a level with one (e.g. `ESCORT 5 CRAWLERS`) |
| L2 | + enemy types with portraits, boss name and silhouette, a set piece as an "unknown huge contact" with its silhouette, special availability |
| L3 | + recommended weapon traits highlighted in the shop, wave timeline strip, secret count |

Dr. Varga adds one line per sensor level (text only): with no sensor suite it is vague ("Our
scans are patchy, Lancer…"), and each better level makes it more precise, never telling more than
the fields that level shows (see [hangar](../../ui/hangar/README.md)).

### Utility bays (confirmed)

The ship has **two utility bays**; a **third** can be bought (from Act 3, see
[player](../README.md)). Every system on this page occupies one bay. The third bay is bought once
and opens the loadout's third utility slot; it is sold from the hangar visit before Level 15.

<!-- data: bays -->
| Bay | Price | Available |
|---|---|---|
| 1–2 | starter | start |
| 3 | 5 000 | act 3 |
<!-- /data -->

### Act 5: vacuum and the core tiers (draft)

*Draft, step `m9-d-systems` (2026-10-27), **approved in round 60** (Q12 = c). Act 5
brings no new utility module; the belt's vacuum is defined in [the belt](../../world/belt/README.md#vacuum-rules).*

- **Engine Mk IV** (345 px/s, 2 MW, 8 000) is in the shop from the L29 visit as listed. The typical
  plan leaves it out (no power room on the Mk IV generator, see the
  [Act 5 hand plan](../generator/README.md#act-5-hand-plan-draft)); it is the speed answer to the
  Minefield's lines and the Rail Serpents for a player who keeps power spare.
- **In vacuum** the hydro-kit is stowed (the ordinary Stormhawk is drawn) and the **pressure hull does
  nothing** (no drag to remove); fitted, it still draws its 1 MW, so the hand plan puts it back in the
  inventory at the L29 visit.
- **Under a generator drain** (the Void Leech, L33) the engine and every utility module keep working:
  only the shield's regeneration and the weapons' fire rate are shed
  ([generator](../generator/README.md#enemy-drain-effects)).
- The **Salvage scanner** has its level in L34 *Salvage Run* (many caches and secrets on the
  derelict hulls); nothing in its numbers changes.
- The **Ascendancy IFF spoofer** stays an Act 6 story item (its roster row): Act 5 is where the
  Ascendancy's turrets are met openly, not yet where their codes are captured.

## Concept art

Prompts and capture notes: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/targeting-computer-capture-r28-a.png](concept/targeting-computer-capture-r28-a.png) | A capture of the game (Level 07): the Targeting computer's HP bar under a damaged Brood Pod, its lime brackets on the Brood Carrier's open bay sacs, and zooms of the bar and of the Salvage scanner's glint on the lifeboat's tow cable | chosen |
| [concept/secret-glint-r28-a.png](concept/secret-glint-r28-a.png) | Salvage scanner glint, option a "star glint" (production art, `tools/art/secret_glint.py`): 23×23, a four-point star with 10 px arms, short diagonals and a warm white core over a soft gold halo, 4 frames (rise, flare, twinkle, fade), additive, 24 colours; beside the old 9×9 loot glint, and on a cargo container on the station deck and in the dark (sheet) | chosen |
| [concept/secret-glint-r28-a.gif](concept/secret-glint-r28-a.gif) | Option a playing as the game does (4 frames in 0.3 s every 1.5 s) on a cargo container, deck and dark, beside the old glint (motion) | chosen |
| [concept/rejected/secret-glint-r28-b.png](concept/rejected/secret-glint-r28-b.png) | Salvage scanner glint, option b "scanner ping": 35×35, a thin gold ring expanding from the object (radius 3, 7, 11, 15 px) and fading, four lock marks on it at the diagonals, a white flash at the centre in the first two frames, additive, 24 colours (sheet) | rejected |
| [concept/rejected/secret-glint-r28-b.gif](concept/rejected/secret-glint-r28-b.gif) | Option b playing as the game would, beside the old glint (motion) | rejected |
| [concept/dash-capture-r37-a.png](concept/dash-capture-r37-a.png) | Game capture (round 37, M7 part A), L05 hard: the Evasive thrusters' dash by V and by a double-tap, its afterimages and burst, the HUD dash pip emptying and refilling in 3 s, a dash refused while it cools (sheet) | chosen (round 37, K.3: accepted) |
| [concept/dash-capture-r37-a.mp4](concept/dash-capture-r37-a.mp4) | The same, 19 s with the game's sound | chosen (round 37, K.3: accepted) |

## Implementation

- [x] Engine speed per model
- [x] Two utility bays; the same module may be fitted in both (M3 part B2)
- [x] Pickup magnet: pickups in its reach fly to the ship at its pull speed, per level from
  [data.yaml](data.yaml) (M4 part H; `vanguard.sim.Magnet`, tests in `PickupMagnetTest` and
  `UtilityModulesTest`)
- [x] Targeting computer kept out of the shop until M5; the L06 data core's unlock of it stays in
  the save (M4 part H, `for_sale: false` in [data.yaml](data.yaml))
- [x] Targeting computer: HP bars, weak-point brackets and the +20 % homing turn as under
  *Targeting computer*; in the shop from the next visit (L07 with the data core, otherwise L08:
  `for_sale: false` removed) — M5 part A (`targeting` in [data.yaml](data.yaml); the turn bonus in
  `SimSpecs.loadout`, the marks in `vanguard.game.render.TargetingOverlay` from
  `Enemy.ticksSinceHit`; tests `UtilityModulesTest`, `UtilityEffectsTest`)
- [x] Salvage scanner: +10 / +20 % on salvage and hidden crates, the glint on a secret's objects —
  M5 part A (`salvage` in [data.yaml](data.yaml); `Loadout.salvageBonus` paid in
  `Sortie.payPickup`, the glint in `vanguard.game.render.SecretGlints`; tests `UtilityEffectsTest`,
  `UtilityModulesTest`; the glint plays its own sprite `glint-secret`, concept round 28's chosen
  star glint, and the loot targets' glint frames when the sprite pages lack it)
- [x] Sensor suite: off-screen threat arrows at L2+ (M4 part H, `vanguard.game.render.ThreatArrows`;
  the sensor level in `Flight.sensor()`)
- [x] The third utility bay as data (5 000 cr, `available: act 3`): loaded and validated, for sale
  from the L15 visit and never in Acts 1–2, the save unchanged (M4 close-out; `Catalogue.bays()`,
  `Hangar.available(Bay)`, test `UtilityModulesTest`)
- [x] Buying the third bay in the hangar and fitting its slot (`UTILITY_3`) — **later: Act 3** (it
  is first for sale at the L15 visit; the save then records the bought bay) — rules under *Third utility bay (Act 3)*
  (M7 part A, step A2, the content side: `Hangar.bays`/`buyBay`/`sellBay` with the visit's undo,
  no shop rows for `UTILITY_3` until it is bought, never sold while a module is fitted, 60 % back;
  `Gear.bays` and save format 4; `--loadout utility3=` counts it bought; test `ActThreeShopTest`. The
  hangar's third tile and bay row: step A5, `ActThreeHangarTest`)
- [x] Evasive thrusters as under *Evasive thrusters (Act 3)*: the double-tap and Dash
  button, the 72 px dash, 0.25 s invulnerability, 3 s cooldown, the HUD pip — **later: Act 3** (M7 part A;
  step A2: the `dash` block in [data.yaml](data.yaml), `SystemsData.Dash`; step A3: the dash, its
  double-tap detection and invulnerability in the simulation; step A6: the Dash button sends
  `Command.DASH`, the HUD's dash pip right of the shield's label plate, three fading afterimages,
  the thruster burst and its sound, the hull see-through every other two steps while invulnerable)
- [x] Act 4's modules' effects: the Auto-repair nanites and the Pressure hull (M8 part A, the items below)
- [ ] The other modules' effects — **later: Act 6** (Ascendancy IFF spoofer)
- [x] Sensor level controls the intel panel detail
- [x] Underwater penalties and the pressure hull — **later: Act 4** (the pressure hull is an L22
  module; the penalties are Europa's under-water rules; the draft is *Pressure hull (L22)* above) (M8 part A, step
  A2: the hull's `hull: {drag: false}` and its Effect cell in [data.yaml](data.yaml), the water's numbers in
  [europa's data.yaml](../../world/europa/data.yaml); the drag in the simulation, steps A1 and A3: `UnderwaterTest`, `Act4SpecialsTest.thePressureHullTakesTheDragOffTheShipOnly`; fitted from the hangar, `Act4ArsenalFlightTest.theHullTheNanitesAndTheArsenalFlyFromTheHangar`)
- [x] Hydro-kit: the refit before L23 (a hangar notice, the ship's look under water, stowed in dry levels) — **later: Act 4** (M8 part A, look in round 48; step A2: the refit's level (`hydro_kit: 23` in [europa's data.yaml](../../world/europa/data.yaml)) and `Campaign.hydroKitNotice`, once per campaign by the story flag `hydro-kit`; the notice and the look in steps A5–A6: `ActFourHangarTest.theHydroKitsNoticeShowsOnceAndFitsTheMessageLines`, `ActFourEscortTest.theHydroKitNoticeShowsOnceAtTheVisitBeforeLevel23`; the `ship-hydro`/`rook-hydro` frames drawn while under water, `DeepWaterLooksTest.everyActFourSpriteIsInTheAssets`)
- [x] Auto-repair nanites as under *Auto-repair nanites (L24)*: the repair tick, the 50 % cap, the HUD pip — **later: Act 4** (M8 part A; step A2: the `repair` block (`interval` 4 / 2 s, `cap` 0.5, `pause` 1 s) and the unlock L24 in [data.yaml](data.yaml), `SystemsData.Repair`; the tick in step A3, `Act4SpecialsTest.theNanitesRepairAPointPerIntervalUpToHalfTheArmour`, `aHitPausesTheNanitesForASecondAndAWreckIsNotRepaired`, `theNanitesWorkInFlightAndHashOnlyWhenFitted`; the pip in step A6)

## Open questions

- The underwater penalties: recommended (b) keeps the drag and the × 0.7 damage and drops the
  top-speed and shield-regen penalties, so the pressure hull "removes the drag"; (a) is the
  first draft ([europa](../../world/europa/README.md#under-water-rules); round 48, Q1).
- Keep the hydro-kit automatic and free (recommended) or make it a module?
- Q12 of round 60 (Act 5's arsenal): recommended (c) adds no module in Act 5 (the Engine Mk IV is a
  core tier); under (b) the Hunter drone (an escort, [wingmen](../wingmen/README.md)) would be Act 5's.

## Decisions

- 2026-09-30: Extra options live in utility bays so they compete with each other, not with guns.
- 2026-09-30: The hydro-kit is an automatic free refit; the pressure hull is an optional L22 module
  (underwater rules owned by [europa](../../world/europa/README.md#under-water-rules)).
- 2026-09-30: Converted to the 960×540 baseline (was 640×360).
- 2026-10-01: Utility bays confirmed: two, a third buyable.
- 2026-10-01: Targeting computer: unlocked from L07 by the L06 data core (one act early), per the data-core rule in economy.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-02: The engines and utility modules moved into [data.yaml](data.yaml) (M2 data files); both tables are rendered from it.
- 2026-10-02: M3 part B2: engines and utility modules are sold in the hangar; the fitted engine's
  speed flies. The intel shows what the best fitted sensor suite's level plus the difficulty's
  sensor bonus (easy +1) allows, at most L3 (`vanguard.content.campaign.Intel`). Two utility bays;
  the same module may be fitted in both (the document does not forbid it). The modules' flight
  effects (magnet, scanner, threat arrows, …) and the third bay (Act 3) follow.
- 2026-10-02 (user decision): sensor L2 also shows a level's set pieces, unnamed, as an unknown
  contact of their size tier with their silhouette (Level 03's Leviathan: "unknown huge contact";
  see [hangar](../../ui/hangar/README.md#decisions)).
- 2026-10-03: Sensor L1 also shows the level's objective as an OBJECTIVE field (main-agent choice, M4 part D: Level 04's intel already promised "escort: 5 crawlers" at L1).
- 2026-10-04: The L06 data core's unlock of the Targeting computer is recorded in the save from
  M4 part F; the module itself comes with the utility modules in M5 and is in the shop from the
  first hangar visit after that for a save that holds the unlock (user decision D6 of M4 part F;
  see [economy](../../systems/economy/README.md#data-cores)).
- 2026-10-05: M4 part H (user decision D2 = A): the Pickup magnet and the sensor suite's L2 threat
  arrows are built in part H; the Targeting computer stays out of the shop until M5 (its data
  file's `for_sale: false`), its L06 data-core unlock still recorded. Rejected: building the
  Targeting computer in part H too (B, reverses part F's D6) and hiding every module without an
  effect (C, the Act 1 shop would lose its only cheap utility item).
- 2026-10-05: The Pickup magnet pulls (main-agent brief for D2 = A; the design only gave the radius):
  pickups within 72 / 108 / 144 px fly to the ship at 240 / 300 / 360 px/s, proposed numbers (the
  row is `draft` until the user reviews them); the ship still collects at its own radius. It pulls
  every pickup, the hidden crate and the data core included, but no ground object. Without a
  magnet nothing changes, so every replay hash stays.
- 2026-10-05: M4 part H: the sensor suite's threat arrows fly from the best fitted sensor suite's
  level 2 (the difficulty's sensor bonus is the hangar intel's only, so easy with an L1 suite shows
  none); the suite counts as flown, so the HUD no longer lists it as not available. The arrows
  point at every air enemy off the screen and coming in; the look, rules and limits are in the
  [HUD](../../ui/hud/README.md#decisions).
- 2026-10-05: Concept round 26 closed (user: the round accepted as proposed): the Pickup magnet's pull speeds 240 / 300 / 360 px/s at L1 / L2 / L3 approved with the radius 72 / 108 / 144 px, so its row leaves `draft` for `approved` (it was `idea` before part H, when it had no effect yet).
- 2026-10-05: M4 close-out: the third utility bay is data (`bays` in [data.yaml](data.yaml): two
  starting bays and one bought bay at 5 000 cr, `available: act 3`, rendered in the *Utility bays*
  table) and the loader and validator accept it; the hangar sells it from the L15 visit, so no Act
  1–2 visit offers it and the save is unchanged. Buying it and fitting the third slot are built
  with Act 3. This closes the [hangar](../../ui/hangar/README.md)'s open question on its data entry.
- 2026-10-06: M5 part A, user decisions: **D6 = a**, the Targeting computer shows a fading HP bar
  under damaged non-tiny enemies, lime brackets on open weak points, and turns the Stormhawk's
  homing weapons 20 % faster (not Rook's); in the shop from the first visit after part A (L07 with
  the data core, otherwise L08). Rejected: b (bars always on: noise in levels of 50 units a
  minute) and c (only `large` units and bosses: weak value for 3 000 cr). **D7 = a**, the Salvage
  scanner is built in part A: +10 / +20 % on salvage pickups and secrets' crates, outside the
  budget like a grade bonus, and a glint on a secret's trigger object while it is on screen.
  Rejected: b (out of the shop, retagged Act 3: the Act 2 shop loses a utility item) and c (the
  credits only: a half-built item). Until part A's code lands the scanner is on sale with no
  effect, as since the L08 visit was reachable. Both rows leave `idea` for `draft`: the user
  decided the effects, the details above (bar size and colours, the 0.3 s fade, the bracket pulse,
  which units and objects count, the glint's timing, the bonus before the rounding) are main-agent
  choices for review with concept round 28. "The other modules' effects" is split: the M5 pair
  above, the rest tagged with their acts.
- 2026-10-06: M5 part A built both modules as designed above; main-agent and builder choices for
  review with concept round 28: the HP bar sits under the unit's hit box (2 px, the HUD's readout
  green `40FF80`, amber `FFE04A` below 30 %, on the LCD dark trough), counts as "hit" any damage
  (shots, blasts, the Airstrike, ramming); a segment chain (the Coilwyrm) shows one bar under its
  foremost living member with its living members' HP summed; `tiny` is read from each unit's stat
  block. The brackets are lime `B4FF3C`, a sine pulse between 60 and 100 % at 2 Hz, each arm a
  quarter of the part's box, 2 px outside it, on a boss's, mid-boss's or set piece's parts with a
  damage multiplier while not shielded, armoured or wrecked; the Coilwyrm head's ×2 is in its data
  but not simulated, so it gets none. The +20 % raises the weapons' one turn rate where
  `SimSpecs.loadout` builds the Stormhawk's weapons (overdrive included), so Rook's guns, built
  from the base weapons, keep theirs. The scanner's factor (1 + its bonus) multiplies the payout
  before the one rounding; the score counts the plain value. The glint sparkles at the object's
  centre (a destructible or trigger hiding a secret, a crane clamp holding its crate, a tow cable
  until cut), each at its own phase, over every layer below the pickups; until its own sprite it
  plays the three loot-target glint frames forwards and back. Without either module every replay
  hash is unchanged (the enemy's hit counter is not hashed). `--loadout` also fits utility modules
  for testing (`utility=targeting-computer,utility2=salvage-scanner:2`).
- 2026-10-06: Concept round 28: the Salvage scanner's glint gets its own sprite, two production
  options by `tools/art/secret_glint.py` (the loot glint's warm white with the salvage gold, 2D
  light fields at 4×, additive, one 24-colour palette each): a, a 23×23 star glint with a soft halo,
  and b, a 35×35 scanner ring that expands from the object. Option a is written to the game's
  sprites (`glint-secret`, on the shared pages under the `glint` root) and played until the round
  closes (`SecretGlints`, the old loot glint as its fallback); `--variant b` swaps it.
- 2026-10-06: Bug fixed (M5 part A): the Coilwyrm head's ×2 weak-point multiplier was in its data
  but never simulated (since Level 06). The chain's head now takes every damage ×2 like a set
  piece's part (`EnemySpec.ChainSpec.headMultiplier`, from the head part's `multiplier`; a regrown
  head has none), so the Targeting computer's lime brackets show round a living Coilwyrm head (the
  rule's `large` unit with a multiplier) and not round a regrown one.
- 2026-10-06: Concept round 28 closed (user: everything accepted, glint **a**): the Targeting
  computer's details (the HP bars' size, colours, 1.5 s + 0.3 s fade and the units that get one, the
  lime brackets' pulse and the parts that get them, the +20 % turn for the Stormhawk's homing weapons
  only) and the Salvage scanner's (+10 / +20 % on salvage pickups and hidden crates before the one
  rounding, outside the budget and not in the score; the glint's timing and the objects that glint)
  are **approved**, so both rows leave `draft` for `approved`. The glint is option **a**, the star
  glint (23×23, warm white over a soft gold halo), approved as **final**: the game already played it
  (`glint-secret`); b, the scanner ping, rejected (its review pair in `concept/rejected/`, where
  `secret_glint.py --review` writes it). With the Targeting computer's code-drawn bars and brackets
  accepted as built, every asset of this part is approved: `art: final`. Every remaining item is
  tagged with its act, so the part is `done` again.
- 2026-10-12 (overnight design phase): the Evasive thrusters specified as a draft section (the
  Dash action by double-tap or its button, 72 px over 0.12 s, 0.25 s invulnerable, 3 s cooldown,
  walls stop it without contact, a HUD pip) and the third bay's rules (on sale once from L15,
  any module, not sold while fitted); the rendered rows keep their data until M7 part A. The
  power check found the thrusters' 2 MW does not fit the typical Act 3 loadout on the Mk III.
  Accepted in round 36 (2026-10-13).
- 2026-10-13 (user, round 36): the Act 3 sections approved (D.25): the Evasive thrusters (the dash,
  its double-tap and button, 0.25 s invulnerability, 3 s cooldown, 2 MW) and the third utility bay
  on sale once from the L15 visit. The HUD's dash pip and the controls' Dash binding are part A items
  ([roadmap](../../tech/roadmap/README.md), Q.25e).
- 2026-10-13: M7 part A, step A2: the Evasive thrusters' `dash` block in data.yaml (row `approved`);
  the third utility bay bought and sold in the hangar's rules with save format 4's `bays` (user
  decision Q1 = a).
- 2026-10-13 (user, round 37): the dash capture accepted (K.3); the dash's build choices kept (B.2 f:
  the direction held, a double-tap within 0.25 s, a dash asked for while it cools does nothing; B.2 g:
  `Command.DASH` the last command bit; B.5 b: the dash pip right of the shield's label plate, its white
  state 0.2 s of game time since R.4 d); its sound S.13 a "thruster puff"; save format 4 accepted
  (V.1); a second set of thrusters adds nothing (B.3 c).
- 2026-10-21 (the user's power room: the Mk III at 16 MW, the weapons' power dial): the Evasive thrusters fit
  the typical Act 3 loadout; the balance plan fits a Pickup magnet L1 from Level 20.
- 2026-10-21 (Act 4 design phase, step m8-dsystems; **decided in round 48 (2026-10-22)**): the hydro-kit stays automatic and free (its look, no slot, no power; the factors come from the level's medium), the pressure hull is specified as removing the drag (Q1 = b), the Auto-repair nanites (unlock L24, 3 MW, repairs up to 50 % of the armour) specified as a draft; the table rows stay `idea` until the user reviews them.
- 2026-10-22 (M8 part A, step A2): the pressure hull's and the nanites' numbers in [data.yaml](data.yaml) (`hull`,
  `repair`; the nanites' unlock L24 as this README has it, the hull's Effect cell rewritten: "Removes the under-water
  drag"); their table rows set from `idea` to `draft` (written and decided in round 48; raising a row to `approved` is
  the user's).
- 2026-10-22 (M8 part A, steps A3/A6; accepted in round 50 (2026-10-22)): the Auto-repair nanites are built in part A (D6, the Mk IV's reason); a hit pauses them for 1 s; the pip `+` beside the armour gauge while they repair.
- 2026-10-27 (Act 5 design phase, step m9-d-systems; **approved in round 60**): Act 5 adds no
  utility module (Q12 = c); the Engine Mk IV stays in Act 5; in vacuum the pressure hull does nothing and the plan
  stows it; the engine and the modules are never shed by a drain.
