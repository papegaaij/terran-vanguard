---
title: Hangar
design: approved
implementation: in-progress
art: final
depends-on: [../../player, ../../systems/economy, ../../campaign]
updated: 2026-10-24
---

# Hangar

## Summary

The ship configurator between levels. The player reads the intel on the next level, buys,
sells, swaps and upgrades equipment within the generator's power budget, repairs armour,
saves, and launches. The intel panel and the shop are linked: weapons whose traits match
the next level's threat profile are marked.

## Design

### Layout (960×540; columns ≈ 300 / 370 / 290 px)

```
┌──────────────────────────────────────────────────────────────────────────────┐
│ [INTEL] [SHOP] [LOADOUT] [REPAIR] [SAVE]              CR 12 450   ► LAUNCH   │
├────────────────────────┬──────────────────────────────┬──────────────────────┤
│  LOADOUT               │ SHOP · FRONT GUNS            │ INTEL · MISSION 15   │
│                        │                              │ ┌──────┐ DR. VARGA   │
│      [Scatter L3]      │ ► Scatter Vulcan  L3 owned   │ │ PORT │ "Burrowers  │
│  [Missile] ▲ [Missile] │   Lance Laser     2 500 ◆    │ │ RAIT │  under the  │
│   L2      ███      L2  │   Hornet Launcher 2 000      │ └──────┘  slopes..." │
│      [Tail Gun L1]     │   Hammer Mortar   1 500 ◆    │ Setting  Mars descent│
│                        │   Ion Beam   NEW  5 000      │ Layers   ground, air │
│  GEN  Mk III  16 MW    │ ───────────────────────────  │ From     front 100 % │
│  SHD  Mk II            │ Hammer Mortar L1             │ Density  ███░░ 3     │
│  ARM  Comp I  64/80    │ anti-ground, area            │ Hazards  burrowers   │
│  ENG  Mk I             │ DPS  25 (ground)   Draw 3    │ Boss     none        │
│  SPC  Airstrike ×2     │ vs fitted: DPS −10 air,      │ Recommend            │
│  UTL  Sensor L3, Magnet│      +25 ground              │ ◆ forward            │
│                        │ [BUY 1 500]  [TEST FIRE]     │ ◆ anti-ground        │
│ POWER ██████████░░░ 12.5/16 MW   (+35 % shield regen)                        │
└────────────────────────┴──────────────────────────────┴──────────────────────┘
```

(Example: the hangar before level 15 *Olympus Descent* with a level-3 sensor suite; numbers are
illustrative and come from [player](../../player/README.md).)

### Panels

- **Loadout** (left): the ship diagram with every slot. Select a slot to filter the shop to
  items that fit it. Core stats underneath. The **power bar** at the bottom shows the current
  load and, while browsing, the projected load in a lighter colour; it turns red when an item
  would not fit. The **module tiles** under the diagram (GEN, SHD, ARM, ENG, SPC, UTL, UTL; 48 px
  wide) name the fitted part in at most **6 characters** of the 8 px label font: the model without
  its nickname (`MK II` for Mk II "Arc"), `STD`, `CMP I` / `CMP II` for Composite, `STRIKE`,
  `S-BOMB`, `FLARES`, `SENSOR`, `MAGNET`, `TARGET` (Targeting computer), `SALVGE` (Salvage
  scanner); a longer name loses its spaces and is cut to 6 (`CMPIII`, `EVASIV`). The weapon
  callouts are wider and keep the longer short names (`MICRO-MSL`).
- **Shop** (centre): list for the selected slot (owned items first, then buyable, then locked
  with their unlock hint). ◆ markers show how many of the item's traits match the intel
  recommendation. A **NEW** tag marks items that entered the shop since the last visit; the
  unlocks follow the campaign's
  [trait pacing table](../../campaign/README.md#loadout-pressure-and-shop-unlock-pacing). Item details show traits, DPS, draw, price, and a **comparison with the
  fitted item** (green/red deltas).
- **Test fire**: a small looping preview box showing the weapon's pattern at the current level
  against dummy targets. Quick way to understand spread, rear and side weapons. The Torpedo Pod
  (M5 part E) is test-fired over a **water strip** with a **submerged dummy** (on `sub`, drawn
  through the water look) among the others, so its drop, its run under the surface and its turn
  show; nothing but the torpedo reaches that dummy.
- **Intel** (right): the next level's threat profile, presented by Dr. Varga (her 72×72 radio
  portrait with the hangar teaser beside it). How much is shown
  depends on the sensor suite level, see [ship systems](../../player/systems/README.md#sensor-levels-and-hangar-intel).
  Her line closes the panel: one per sensor level (none, L1, L2, L3), text only, from a vague
  no-sensor line to a precise tip that never tells more than the fields shown beside it (L1:
  directions, density, hazards, the objective; L2: the enemy types by name, a boss's weak points;
  L3: the recommended weapons and where the secrets hide). The lines are the level's
  `threat_profile.varga` data.
  Fields come from the level's threat profile (see the
  [level template](../../campaign/README.md#level-document-template)): setting, dominant layers,
  attack directions with the share of waves per direction (e.g. "front 60 %, rear 40 %"),
  density (1–5), hazards, special availability (e.g. "No air support under the ice"), boss,
  recommended weapon traits. A set piece (a `huge` unit outside the waves, such as Level 03's
  Leviathan) is not named: from sensor L2 it shows as an "unknown huge contact" (its size tier)
  with its 40×40 silhouette at the right of the boss row.
  The L3 **wave strip** shows each wave's start as an amber tick over the level's length, with the
  level's marks: a defend phase as a dim band under the ticks, a hazard span (a whiteout, a root
  field) as a pale band over them, and the boss's or mid-boss's arrival as a red tick the strip's
  full depth over everything (Levels 05, 07, 11 and 14).
- **Repair**: armour repair per point or "repair all", with cost; from Level 08 a second line
  for Rook's armour (see *Escort* below).
- **Save**: slot list, see [saves](../../systems/saves/README.md).
- **Launch**: confirmation if the loadout lacks a recommended trait ("No anti-sub weapon
  fitted. Launch anyway?"), armour is below 50 %, or (from Level 08) Rook's armour is below 50 %
  ("Rook's armour 34/80.") or he is grounded ("Rook is grounded and stays home."); one
  confirmation lists every warning that applies. The trait warning only uses what the intel
  shows: it needs the sensor level that reveals the recommended traits (L3, counting the easy
  bonus); below it there is no trait warning, so the launch never tells more than the intel.
  **Required traits** (user decision D7 = a of M5 part C; built): a level can mark a trait as
  `required` in its threat profile, a trait the primary objective cannot be met without (Level 09's
  `anti-ground`: the hive nodes are hardened). Its warning shows at **every** sensor level, since
  the briefing already says it ("No anti-ground source fitted: the targets cannot be destroyed."),
  and it counts every source that damages hardened targets: an `anti-ground` weapon in any slot,
  Rook's Mortar while he flies (hired, fitted and not grounded), and an Airstrike or Smart Bomb
  fitted with at least one charge. A recommended trait that is not required keeps the L3 rule.

### Escort (from Level 08)

The escort slot holds Rook from the hangar visit before Level 08 (see
[wingmen](../../player/wingmen/README.md)); before that its callout stays locked with his craft's
icon, as built.

- **Escort callout** on the schematic: Rook's craft icon, his fitted gun's name with five level
  pips, his armour `nn/80` and his side (`L` or `R`); `GROUNDED` in red at 0 armour. It is one of
  the slots Q/E cycle.
- **Selecting it** fills the shop with his four guns (fitted, owned, buyable by price; the NEW tag
  at the L08 visit). The detail shows the gun's traits (its base weapon's), his DPS at that level,
  the price at 60 %, draw `0` (his own power: the power bar does not move) and the green/red deltas
  against his fitted gun. Choices as for the front gun: buy, upgrade, fit, sell (not the fitted
  gun); every one of them joins the visit's undo.
- **Side**: a `SIDE LEFT / RIGHT` choice in the escort's detail, toggled with left/right; free,
  saved with the visit, not part of the undo.
- **Repair**: the Repair panel shows two lines, `SHIP` and `ROOK`, each with its missing points,
  its cost at the difficulty's repair cost per point, per point or all.
- **Test fire**: the box loops his selected gun from a single nose muzzle at his scale, like a
  front gun.

**The Bulwark and Rook's absence** (M8 part A, [wingmen](../../player/wingmen/README.md#heavy-drone-bulwark-escort-alternative-l22);
the layout below is the step's default, accepted in round 50 (2026-10-22)):

- The escort's shop lists a **Bulwark row** between Rook's guns and the side row: `BULWARK HEAVY
  DRONE` with its icon and its price, `OWNED`, `FITTED` (it flies the next level) or `LOCKED L22`.
  Its detail shows `ARMOUR 120   DPS 14   DRAW 0 MW`, `DRAWS 30 % OF AIMED FIRE NEAR YOU` and a
  note (its unlock, its repair cost, or who flies while Rook is missing), and its chips: `BUY 6 000`
  (bought, it flies at once), then `FIT` (it flies instead of Rook) or `UNFIT` (Rook flies again)
  and `SELL` (asking first). All of it joins the visit's undo; the replay hangar offers only `FIT` /
  `UNFIT` for an owned one.
- The **escort callout** shows whoever flies the next level: Rook as before, or the Bulwark's icon,
  `BULWARK`, `LOANER` for the free loaner, `CANNON` and its armour `nn/120` (amber below half).
- **Rook MIA** (Levels 27–29): his guns leave the shop (they stay in the inventory), the callout
  reads `ROOK MIA` in red under the Bulwark, his repair line is gone, and the owned Bulwark or the
  loaner flies whatever the choice (the row offers no `FIT` / `UNFIT` then, only `SELL`).
- The side row and its message name the Bulwark while it flies (`BULWARK'S SIDE`).
- **Repair**: an owned Bulwark has its own `BULWARK` line at 8 cr a point on every difficulty; Q/E
  cycle the lines that have points missing.

### Transactions

Buying fits the item immediately when the power budget allows, otherwise it goes to the
inventory. Undo within the visit refunds 100 %, and so does selling an item bought during the
visit (purchase plus this visit's upgrades); items owned before the visit sell at the normal
sell-back, see [economy](../../systems/economy/README.md#sell-back-and-undo).

Swapping the plating keeps the damage: the missing armour points stay missing (Standard 41/60 →
Composite I 61/80), with at least 1 point left. Repair is a separate choice.

**Power dial** (the user, 2026-10-21; rules in [player](../../player/README.md#power-budget)): a
fitted weapon's row offers **POWER −** and **POWER +** next to its other choices. They are free and part
of the visit's undo; POWER − is refused at L1, POWER + at the bought level and, like an upgrade, when the
load would exceed the output (`NEEDS x MORE POWER`), and the power bar previews both. A turned-down
weapon's row shows its DPS and draw at the setting, and its loadout callout shows the setting over the
bought level (`L3/5`). The replay hangar's refit-only mode offers the dial too (a free refit; stated
default, pending the user's review).

**Mouse** (M6 part A, see [ui](../README.md#mouse)): hovering a shop row, a choice chip or a
command of the top bar only gives it the hover look (a cyan outline) and never selects or
focuses it; a click selects a shop row or a slot, a click on a choice chip or an enabled command
makes it as Enter does (selling and launching still ask), and a click on a chip while the command
bar has the focus gives the focus back to the shop; a click on a weapon mount's callout, Rook's
callout or a module tile selects that slot as Q / E do (by click only, so moving across the
schematic does not switch the shop list); while the pointer rests on one of the selected row's
choices the test fire and the power bar preview it (pending the user's review); a key press takes
the hover look off; a click on Rook's `LEFT` / `RIGHT` chip sets his side; the wheel over the
shop list scrolls it. While the repair panel is open only it takes the mouse: a click on the
`SHIP` or `ROOK` line picks it, a click or a drag on the slider sets the points and a click on the
repair button repairs; the right button closes it, and elsewhere asks to quit as Esc does.

## Concept art

Concept [round 06](../../concept-rounds/round-06/README.md) — full 960×540 screens at 1× plus a 2× detail crop; generator `tools/concept/ui_r06.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/rejected/hangar-r06-a.png](concept/rejected/hangar-r06-a.png) | Hangar A — three columns: loadout diagram, shop list with ◆ / NEW / locked and item detail with test fire, intel panel with Varga (before L15) | rejected — B layout preferred |
| [concept/hangar-r06-b.png](concept/hangar-r06-b.png) | Hangar B — central ship schematic with callouts to the slots, shop drawer left, intel with the large Varga portrait right | superseded by r07-b (glass style) |

Concept [round 07](../../concept-rounds/round-07/README.md) — layout B in the menu's glass style; generator `tools/concept/ui_r07.py`.

| File | What | Status |
|---|---|---|
| [concept/rejected/hangar-r07-a.png](concept/rejected/hangar-r07-a.png) | Hangar r07 A — glass panels over a pre-rendered hangar bay; the parked Stormhawk is the schematic, with slot callouts, module tiles, power bar, shop drawer, intel with Varga (before L15) | rejected — B backdrop preferred |
| [concept/rejected/hangar-r07-b.png](concept/rejected/hangar-r07-b.png) | Hangar r07 B — same layout over a darkened tactical map of Mars with the descent route; holographic blueprint in the centre | rejected — round 44, A.a: superseded by the production art (a layout mockup: the screen is built by `tools/art/ui_kit.py` and `tools/art/ui_scenes.py`) |

Production art, UI batch part U2 (for concept round 13, opened by part U3): the tactical map rendered by [tools/art/ui_scenes.py](../../../tools/art/README.md) and the equipment icons by [tools/art/icons.py](../../../tools/art/README.md); the panels draw the glass kit. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/hangar-final-r13-a.png](concept/hangar-final-r13-a.png) | Review sheet: the 960×540 tactical map (Act 1, Earth from orbit) and all 47 icons, 16×16 at 1× and 3×, 24×24 at 1× and 2× | final (round 44, A.a: a record of production assets) |
| [concept/hangar-capture-final-r13-a.png](concept/hangar-capture-final-r13-a.png) | Game capture before Level 01: the right wing's shop rows with icons (locked ones dimmed), the Bomb Rack selected with its large icon, the holographic callouts with the fitted Pulse Cannon's icon, the locked escort with Rook's craft, the power bar in its trough | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r13-a.png](concept/intel-final-r13-a.png) | Review sheet (part U3, `tools/art/intel.py`): the intel's sensor-L2 pictures at 1× and 3×, the 30×30 portraits of the Skitter, Needler, Stinger and Spine Turret and the 40×40 silhouettes of the Gorgon Frigate and the Brood Carrier | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r16-a.png](concept/intel-final-r16-a.png) | Review sheet (M4 part C batch, `tools/art/intel.py`): the 30×30 intel portraits of the Spore Bomber and the six-bladed Whirl Seed and the Leviathan's 40×40 "unknown huge contact" silhouette (facing down, `boss-leviathan`), at 1× and 3× | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r17-a.png](concept/intel-final-r17-a.png) | Review sheet (M4 part D batch, `tools/art/intel.py`): the 30×30 intel portraits of Level 04's Brood Pod (between swells) and Scuttler (walking down), from the production models of `tools/art/vrell_l04.py`, at 1× and 3× | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r23-a.png](concept/intel-final-r23-a.png) | Review sheet (M4 part F batch, `tools/art/intel.py`, [round 23](../../concept-rounds/round-23/README.md)): the 30×30 intel portraits of Level 06's Mantis (nose down) and Coilwyrm (the head with its first segment), from the production models of `tools/art/mantis.py` and `tools/art/coilwyrm.py`, at 1× and 3× | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r30-a.png](concept/intel-final-r30-a.png) | Review sheet (M5 part B batch, `tools/art/intel.py`, [round 30](../../concept-rounds/README.md)): the 30×30 intel portrait of Level 08's Creeper (walking down), from the production model of `tools/art/creeper.py` with its readability lift, at 1× and 3× | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r31-a.png](concept/intel-final-r31-a.png) | Review sheet (M5 part C batch, `tools/art/intel.py`, [round 31](../../concept-rounds/README.md)): the 30×30 intel portraits of Level 09's Hive Node and Ravager, from the production models of `tools/art/hive_node.py` and `tools/art/ravager.py`, at 1× and 3× | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r32-a.png](concept/intel-final-r32-a.png) | Review sheet (M5 part D batch, `tools/art/intel.py`, [round 32](../../concept-rounds/README.md)): the 30×30 intel portraits of Level 10's Wraith and Mote Swarm, from the production models of `tools/art/wraith.py` and `tools/art/mote_swarm.py`, at 1× and 3× | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r33-a.png](concept/intel-final-r33-a.png) | Review sheet (M5 part E batch, `tools/art/intel.py`, [round 33](../../concept-rounds/README.md)): the 30×30 intel portraits of Level 11's Driftjelly and Reef Spitter, from the production models of `tools/art/driftjelly.py` and `tools/art/reef_spitter.py`, and the 40×40 silhouette of the Harbour Kraken (its head and mantle), at 1× and 3× | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r34-a.png](concept/intel-final-r34-a.png) | Review sheet (M5 part F batch, `tools/art/intel.py`, round 34): the 30×30 intel portrait of Level 12's Lamprey, from the production model of `tools/art/lamprey.py` nose down in swim frame 1, at 1× and 3× | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r35-a.png](concept/intel-final-r35-a.png) | Review sheet (M5 part G batch, `tools/art/intel.py`, round 35): the 30×30 intel portrait of Level 13's Skimmer, from the production model of `tools/art/skimmer.py`, the whole hull at heading 0 moving down the screen, at 1× and 3× | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r36-a.png](concept/intel-final-r36-a.png) | Review sheet (M5 part H batch, `tools/art/intel.py`, round 36): the 40×40 boss silhouette of Level 14's Siege Spire (`boss-siege-spire`), the rooted body as the arena first shows it (the production trunk, six roots and pods of `tools/art/siege_spire.py` placed by its pivots), at 1× and 3× | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r38-a.png](concept/intel-final-r38-a.png) | Review sheet (M7 part B batch, `tools/art/intel.py`, round 38): the 30×30 intel portraits of Level 15's Burrower and Dust Kite, from the production sprites | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r39-a.png](concept/intel-final-r39-a.png) | Review sheet (M7 part C batch, `tools/art/intel.py`, round 39): the 30×30 intel portraits of Level 16's Tendril Anchor and Threadcrawler, from the production models of `tools/art/canyon_units.py` | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r40-a.png](concept/intel-final-r40-a.png) | Intel portraits of Level 17's Choir Herald and Shellback (`tools/art/intel.py`) | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r41-a.png](concept/intel-final-r41-a.png) | Intel portraits of Level 18's Dust Devil and Ghost Drone and the `unclassified-ghost-drone` sensor picture (`tools/art/intel.py`) | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r42-a.png](concept/intel-final-r42-a.png) | Intel portraits of Level 19's Warden Tank (Act 3 grey) and Revenant Walker and the `boss-revenant-walker` "unclassified, huge" contact silhouette with sensor noise (`tools/art/intel.py`) | final (round 44, A.a: a record of production assets) |
| [concept/intel-final-r44-a.png](concept/intel-final-r44-a.png) | The `boss-dust-colossus` sensor silhouette (40×40, the open mouth as phase 3 shows it, outline only; `tools/art/intel.py`, M7 part H batch, step ART-H2) | final (round 44, F.d: approved as final) |
| [concept/intel-final-r51-a.png](concept/intel-final-r51-a.png) | The `drill-rig` sensor L2 portrait (30×30, the core and its four pumps; `tools/art/intel.py`, M8 part B batch) | chosen (round 51, F.8: accepted as final) |
| [concept/intel-final-r52-a.png](concept/intel-final-r52-a.png) | The `glassfin` and `eel-swarm` sensor L2 portraits (30×30; `tools/art/intel.py`, M8 part C batch) | chosen (round 52, F.10: accepted as final) |
| [concept/intel-final-r53-a.png](concept/intel-final-r53-a.png) | The `abyss-ray` and `barbfin` sensor L2 portraits (30×30; `tools/art/intel.py`, M8 part D batch) | chosen (round 53, F.7: accepted as final) |
| [concept/intel-final-r54-a.png](concept/intel-final-r54-a.png) | The `siren` and `spiral-nautilus` sensor L2 portraits (30×30; `tools/art/intel.py`, M8 part E batch) | chosen (round 54, F.7: accepted as final) |
| [concept/level-25-intel-capture-r54-a.png](concept/level-25-intel-capture-r54-a.png) | Game capture, M8 part E (step E6, a playthrough save at Level 25 on medium, the sensor suite L2): the hangar with Level 25's intel (Varga's L2 line, the threat strip, the four portraits incl. the Siren and the Spiral Nautilus), the panel at 1:1 | chosen (round 54, F.7: accepted as final) |
| [concept/intel-final-r55-a.png](concept/intel-final-r55-a.png) | The `glow-angler` sensor L2 portrait (30x30; `tools/art/intel.py`, M8 part F batch) | proposed (round 55) |
| [concept/level-26-intel-capture-r55-a.png](concept/level-26-intel-capture-r55-a.png) | Game capture, M8 part F (step F6, a playthrough save at Level 26 on medium, a sensor suite L2 fitted for the capture): the hangar with Level 26's intel (Rook's teaser, the threat strip, the five portraits incl. the Glow Angler, the lure note), the panel at 1:1 | proposed (round 55) |

M4 part H, test fire ([round 26](../../concept-rounds/README.md)): a game capture, see
[concept/prompts.md](concept/prompts.md#test-fire-capture-r26-a).

| File | What | Status |
|---|---|---|
| [concept/test-fire-capture-r26-a.png](concept/test-fire-capture-r26-a.png) | Game capture before Level 01: the hangar with the Side Splitter selected and its loop in the shop's test-fire box, and below it the box at 2× for the Pulse Cannon (L1>L2, upgrade highlighted), Scatter Vulcan, Autocannon Pod (left wing), Bomb Rack and Micro-missile Pod (right wing) and Side Splitter (rear) | final (round 44, A.a: a record of production assets) |

M7 part A, the Act 3 items' icons ([round 37](../../concept-rounds/README.md)): round 36 chose none,
so `tools/art/icons.py` proposes two emblems for each of the Ion Beam, Rear Lance, Deflector Pod,
Light Drone Bay, EMP Burst, Orbital Lance and Shield Overcharge. Round 37 chose **a** (P.3, user
2026-10-13), in `assets/` (16 and 24 px, the names `ItemIcons` derives); b is in `concept/rejected/`. Prompts: [concept/prompts.md](concept/prompts.md#act3-icons-r37-a).

| File | What | Status |
|---|---|---|
| [concept/act3-icons-r37-a.png](concept/act3-icons-r37-a.png) | A: the beam gun with a thick beam and contact star, the Lance Laser's emblem turned round, the pod with its field arc, the disc drone, an EMP ring with a lightning bolt, a column on a target ring, two nested shields (16/24 px at 1x and enlarged) | final (round 44, A.a: a record of production assets) |
| [concept/rejected/act3-icons-r37-b.png](concept/rejected/act3-icons-r37-b.png) | B: an emitter ring with the beam, one lance through two marks, the field arc catching a bolt, a small drone in an orbit arc (utility green), concentric arcs, a satellite above a column, a shield with an up arrow | rejected — round 37: a (each item as its effect) picked |
| [concept/act3-hangar-capture-r37-a.png](concept/act3-hangar-capture-r37-a.png) | Game capture (round 37, M7 part A), the hangar before Level 19 from a hand-written save: the Act 3 shop rows (Ion Beam, Deflector Pod, Light Drone Bay, Rear Lance, EMP Burst, Orbital Lance, Shield Overcharge bought, Evasive thrusters) and the `UTILITY BAY 3` row bought, the `UTL` tile appearing (sheet) | final (round 44, A.a: a record of production assets) |
| [concept/act3-hangar-capture-r37-a.mp4](concept/act3-hangar-capture-r37-a.mp4) | The same, 97 s | final (round 44, A.a: a record of production assets) |
| [concept/act4-icons-final-r50-a.png](concept/act4-icons-final-r50-a.png) | The four Act 4 equipment icons (`tools/art/icons.py --act4`): Harpoon Torpedoes, Depth Charges, Sonar Pulse, Bulwark (escort) at 16 and 24 px, beside the Torpedo Pod, Proximity Mines, EMP Burst and Rook icons for the style (sheet) | chosen (round 50, F.7: accepted as final) |
| [concept/act4-hangar-capture-r50-a.png](concept/act4-hangar-capture-r50-a.png) | Game capture (round 50, M8 part A, step A5), the hangar before Level 27 (Rook MIA) from a scratch save: the Harpoon Torpedoes row and its test fire, the escort callout with the loaner Bulwark and `ROOK MIA`, the escort shop's Bulwark row before and after `BUY`, the Depth Charges' test fire over water (sheet of 4 frames) | chosen (round 50, F.7: accepted) |

**After a load and in a replay** (user decision 2026-10-18): Continue and Load game reach the
hangar through the next level's briefing; that hangar does not autosave as it opens (a save was
just loaded), except after an act intro that was due, whose story flag the autosave keeps. A
mission select replay reaches the **replay hangar** through the level's briefing: the same hangar
on the real campaign, but **refit only** (user correction 2026-10-19): the owned items are fitted
and unfitted (and Rook's side set), which the campaign keeps; nothing is bought, sold, upgraded or
repaired. Its drawer reads `REFIT - <SLOT>` and lists only the owned items with their `FIT` /
`UNFIT` chips (no buyable, locked or bay rows; `NOTHING OWNED FOR THIS SLOT` when there are none),
the message line reads `REPLAY: REFIT ONLY`, and REPAIR is greyed and skipped by the keys and the
mouse; the launch does not warn about the ship's armour (a replay flies at full armour). It comes
with `REPLAY L05 - <NAME>` in its top bar instead of `BEFORE L05`, the replayed level's intel, and
`> REPLAY` on the launch chip. Its launch writes the campaign to the autosave and flies a replay of
it ([mission select](../mission-select/README.md)).

**After the release's end** (M6 part B, Q1 = a, accepted by the user in round 35): the hangar
before the first level that is not built (Level 15 after Act 2) reads `ACT III - RED DUST: COMING
SOON` in its top bar instead of the mission; it cannot launch. Continue reaches it after the
release's end, which writes that autosave.

## Implementation

- [x] Tabs/panels as above with keyboard and gamepad navigation
- [x] Replay hangar (user decision 2026-10-18): reached through the level's briefing from the mission select, on the real campaign, `REPLAY L05` header and `> REPLAY` launch, the campaign autosaved at launch (`HangarScreen.replay`, `replayFlight`; `MissionFlowTest`)
- [x] Replay hangar refit only (user correction 2026-10-19): owned rows with FIT / UNFIT alone, no bay row or repair, buys and sales refused (`HangarState` `refitOnly`, `REFIT_ONLY`; `MissionFlowTest`)
- [x] Mouse navigation — M6 part A (`HangarPointer`, `HangarPointerTest`; see *Design*), accepted by the user in concept round 35
- [x] Slot selection filters the shop; owned/buyable/locked sorting
- [x] Power bar with projected load and refusal
- [x] Power dial (the user, 2026-10-21): POWER − / POWER + on a fitted weapon's row (keys and mouse like
      the other choices, the refusal lines), the row's DPS and draw at the setting, `L3/5` on the
      loadout callout — content built (`Hangar` `POWER_DOWN` / `POWER_UP`, `Offer.power`; see
      [player](../../player/README.md#implementation)); the shop in step PD-G: chips `PWR -` / `PWR +` (the
      full words don't fit beside UPGRADE at the top prices), a second chip row when a row's choices don't fit
      in one (upgrade and dial above, UNFIT/SELL below), messages "… POWER L2 OF 3", "AT ITS LOWEST POWER",
      "AT FULL POWER", the detail and test fire at the setting ("TEST FIRE L3>L2"), the replay hangar's refit
      mode too (`PowerDialShopTest`)
- [x] Comparison deltas vs the fitted item
- [x] Test-fire preview box: the selected weapon (at the detail's level, or the next one while
      UPGRADE is highlighted) loops in the real simulation against three dummies in the shop's
      box, turned a quarter so the ship faces right (`TestFire`, `TestFirePanel`, `TestFireView`;
      `TestFireTest`, `TestFirePanelLayoutTest`)
- [x] Intel panel from the level threat profile, gated by sensor level
- [x] Varga's line per sensor level (none, L1, L2, L3) for every Act 1 level, the one for the
      fitted sensor level shown (`Intel.varga()`; `IntelPanelLayoutTest`)
- [x] Equipment icons: one per shop item and the escort, in the shop rows, the selected item and the schematic's callouts (`tools/art/icons.py`)
- [x] Sensor L2 enemy portraits and boss silhouette in the intel
- [x] Sensor L2 set pieces as an "unknown huge contact" with their silhouette in the intel
      (Level 03's Leviathan; `Intel.contacts()`, the silhouette `intel/boss-<enemy>`)
- [x] Trait-match markers in the shop
- [x] Launch warnings (missing recommended trait, low armour)
- [x] Escort callout unlocked from the L08 visit: Rook's icon, gun and level pips, armour, side, `GROUNDED` — M5 part A
- [x] Escort shop: his four guns at 60 %, buy, upgrade, fit, sell, undo, deltas against his fitted gun, draw 0 — M5 part A
- [x] Rook's side toggle (left/right), saved — M5 part A
- [x] Repair panel: the `ROOK` line at the difficulty's repair cost — M5 part A
- [x] Launch warnings: Rook's armour below 50 %, Rook grounded — M5 part A
- [x] Test fire of Rook's guns and of the Proximity Mines (their dummies fly low, `low-air`, which sets mines off) — M5 part A
- [x] Test fire of the Torpedo Pod over a water strip with a submerged dummy; the "NOT YET IN
      FLIGHT" label goes (`TestFireTest` flipped) — M5 part E (steps E2a and E3c: the range's
      water and the dummies under it in `TestFireView`, the torpedo with its bubbles, splash and
      under-water burst; `TestFirePanel.NOT_IN_FLIGHT` removed)
- [x] Launch warning for a `required` trait at every sensor level, counting Rook's gun (while he
      flies) and the specials' charges as sources (Level 09's `anti-ground`, D7 = a) — M5 part C
      (`Hangar.sourceTraits`/`missingRequired`, `Intel.requiredTraits`, `HangarState.launchWarnings`:
      `NO ANTI-GROUND SOURCE FITTED`; `HangarRequiredTraitTest`, `LaunchRequiredTraitTest`)
- [x] Level 10's `required: [rear]` (D9 = a): the launch warning `NO REAR WEAPON FITTED` at every
      sensor level until a rear-firing weapon (Tail Gun, Fan Blaster, Proximity Mines) is in the
      rear slot; Rook's gun never brings `rear` (`Hangar.REAR`, `sourceTraits`; `HangarState.
      requiredWarning`; `HangarRearRequiredTest`, `LaunchRearWarningTest`) — M5 part D
- [x] The intel panel's level name beside the sensor chip: a name too long for the body font
      (`EVACUATION CORRIDOR`) takes the label font (`IntelPanel.nameFits`, `IntelPanelLayoutTest`)
      — M5 part D
- [x] The hangar before Level 15 reads `ACT III - RED DUST: COMING SOON` (was `BEFORE L15 - NOT
      BUILT YET`) — M6 part B
- [x] Act 3 shop (M7 part A, step A5): the Ion Beam, Rear Lance, Deflector Pod, Light Drone Bay, EMP
      Burst, Orbital Lance and Shield Overcharge in the live shop with their icons
      (`Catalogue.AWAITING_ICONS` empty); the Shield Overcharge bought once (`BUY 5 000`) and sold
      like gear, its detail and tile showing its cooldown (`COOLDOWN 45 S`, `45S`); the tile names
      `EMP`, `LANCE`, `OVRCHG` and the callout `DRONE BAY` (`Names`)
- [x] Third utility bay (M7 part A, step A5): the utility slots' shops end with a bay row (`UTILITY
      BAY 3`, `BUY 5 000` from the L15 visit; in the bought bay's own shop `SELL`, asking first and
      refused while a module is fitted: `UNFIT ITS MODULE FIRST`), part of the visit's undo; once
      bought its slot joins the Q/E tabs after the second bay and its `UTL` tile shows in the
      schematic's lower right corner (clickable like the others) (`HangarState.bayRow`,
      `ShopPanel`, `LoadoutPanel`, `HangarPointer`; `ActThreeHangarTest`, `LoadoutTilesLayoutTest`)
- [x] Act 4 hangar (M8 part A, step A5): the Harpoon Torpedoes, Depth Charges, Sonar Pulse, Decoy
      Flares, Pressure hull and Auto-repair nanites in the live shop with their icons
      (`Catalogue.AWAITING_ICONS` empty; tiles `SONAR`, `FLARES`, `HULL`, `NANITE`, callout
      `HARPOON`); the escort's Bulwark row (buy, fit, unfit, sell, undo), the escort callout for the
      Bulwark and the loaner, Rook MIA (no gun rows, no repair line, `ROOK MIA`), the `BULWARK` repair
      line; the hydro-kit's one-off notice at the visit before Level 23; the intel's `SPECIAL LIMITED`
      on the specials row from sensor L1 when the next level forbids the fitted special (`AIRSTRIKE:
      UNDER ICE`); the Depth Charges test-fired over water among submerged dummies, like the
      torpedoes (`HangarState`, `ShopPanel`, `LoadoutPanel`, `HangarPointer`, `IntelPanel`,
      `TestFire`; `ActFourHangarTest`, `TestFireTest`)
- [x] Test fire of the Ion Beam (its column burns the dummy on its line), the Light Drone Bay (the
      drone orbits and fires at its convergence point) and the Deflector Pod (the dummies fire slow
      bullets at the ship and its field catches them on its side) (`TestFire`, `TestFireView`;
      `TestFireTest`) — loops once step A4 maps the three into the simulation's mounts

## Open questions

- None open.

## Decisions

- 2026-09-30: The intel panel sits in the hangar itself, so the player sees it while shopping.
- 2026-09-30: Converted to the 960×540 baseline (was 640×360).
- 2026-10-01: Concept round 06: layout **B** (central ship schematic with the panels around it) chosen, but in the glass style of the menus, not metal — restyle in round 07. Layout A rejected.
- 2026-10-01: Concept round 07: hangar layout B in the glass style over the tactical map of Mars (r07-b) chosen; the hangar-bay backdrop (r07-a) rejected.
- 2026-10-01: Test fire comes after the first build; the layout keeps its space.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-02: M3 part B1 placeholder (`vanguard.game.screen.HangarScreen`, marked as such on screen;
  the design here is part B2): one glass panel with the credits, the loadout, the armour, the next
  mission with its hangar teaser, difficulty, score, playtime and retries, and Launch (disabled
  while the next level is not built), Save game (the slot list) and Main menu (confirmation). It
  autosaves when it opens and plays the hangar theme. The real hangar replaces the class behind the
  same constructor; every way into it (intro briefing, debrief, Back to hangar, Abort, Load,
  Continue) already passes the campaign.
- 2026-10-02: M3 part B2 (`vanguard.game.screen.HangarScreen` with `vanguard.game.hangar`, the
  rules in `vanguard.content.campaign.Hangar`, `Catalogue`, `Intel`, `Flight`): layout B after
  hangar-r07-b at 960×540. Top bar: HANGAR, the next mission, act and difficulty, the UNDO, REPAIR
  and SAVE commands, the credits and LAUNCH. Left: the shop for the selected slot (fitted, owned,
  buyable by price with the NEW tag and ◆ markers, locked by unlock level with "LOCKED Lnn"; eight
  rows, scrolling), the selected item with its traits, numbers (DPS, output, capacity, regen,
  delay, armour, speed, charges; draw) and the green/red deltas against the fitted item, its
  choices (buy, upgrade, fit, unfit, sell, buy a charge; refused ones dim with the reason), the
  last transaction's message and the empty test-fire box. Centre: the holographic schematic with
  the parked Stormhawk sprite (3×), callouts for the front, wing and rear mounts with level pips,
  the locked escort slot (Act 2), the GEN/SHD/ARM/ENG/SPC/UTL/UTL tiles and the power bar. Right:
  the intel (below). Keys: Q/E (bumpers) cycle the eleven slots, up/down the rows (up from the
  first row reaches the command bar), left/right the choice or the command, Enter (A) confirms,
  Esc (B) asks to quit (and autosaves). Sell, a launch with a warning and quitting ask first. The
  repair panel opens on what the credits repair and changes by 1 (left/right) or 10 (up/down)
  points. Save opens the slot list; Launch is disabled while the next level is not built (after
  Level 01, until M4). The hangar theme plays; the autosave is written as the hangar opens. Intel
  from the level's `threat_profile` data (Level 01's now structured: setting, layers, density,
  traits, hazards, boss, special limits, Varga's line per sensor level) with the directions,
  enemies, wave times and secret count derived from the script; the sensor level gates the fields
  as in [ship systems](../../player/systems/README.md#sensor-levels-and-hangar-intel), and a
  hidden field shows the sensor level that reveals it. The speaker of the hangar teaser (Varga)
  is shown with her briefing portrait and the teaser. Placeholders: the tactical-map backdrop is
  drawn in code (grid, a planet limb, range rings, an approach route), the same for every level;
  test fire keeps its box. Not built: mouse input (so the first item stays open), the per-item
  Varga lines, the L2 portraits and the boss silhouette (so the intel item stays open), test fire,
  Rook's repairs (Act 2). Until M4 only the Pulse Cannon, shield, plating and engine fly; the
  shop says "NOT YET IN FLIGHT: FROM M4" on the other items and the HUD lists them.
- 2026-10-02: User decisions on the B2 open questions: a plating swap keeps the damage, the
  missing armour points carry over (as built; rule under *Transactions*); selling an item bought
  during the visit refunds its full price like the undo, items owned before keep the normal
  sell-back (`Hangar` tracks the visit's purchases wherever they are moved and restores them with
  the undo; the sell confirmation says the full price comes back; tests `HangarTest`:
  `anItemBoughtDuringTheVisitSellsForAllSpentOnIt`,
  `ofTwoEqualItemsOnlyTheOneBoughtDuringTheVisitSellsForAll`); the missing-trait launch warning
  only names traits the sensor level shows (`Intel.markedTraits()`, L3 with the easy bonus;
  `HangarStateTest.theMissingTraitWarningNeedsTheSensorLevelThatShowsTheTraits`). Varga's per-item
  lines (M4 writing pass), the Level 02 portraits and boss silhouette (art track) and the third
  utility bay's data (M4) are deferred; the open questions say where.
- 2026-10-02: M3 close-out (user decision): the navigation and intel items are split; keyboard/gamepad navigation and the sensor-gated intel panel are done. Mouse navigation moves to M6, the test-fire box and Varga's per-item lines to M4, the L2 portraits and boss silhouette to the art track. The document is done for M3.
- 2026-10-02: Production art, UI batch part U2: the tactical map is production art
  (`tools/art/ui_scenes.py`, `assets/ui/hangar-map.png`, loaded by `TacticalMap` instead of the
  code-drawn placeholder): a cyan grid over the dark Earth seen from orbit as a holographic relief,
  its limb across the lower part, orange range rings with bearing ticks round the operation's area
  and a dashed approach route with its arrowhead; one map for Act 1, the same for every level until
  the later acts' settings get theirs (M4/M5). Every shop item of the catalogue (all 46, locked
  ones included) and Rook's escort craft has an icon (`tools/art/icons.py`, `assets/sprites/icons/`,
  16×16 and 24×24): one emblem style, a bevelled dark steel badge with the item on it, the models
  where they exist (the five wing pods with the Stormhawk's production pod materials, the Hornet's
  missile, the mortar shell, the mine, Rook's craft), otherwise per kind a symbol in the kind's
  colour (guns: their barrel block and shots in the firing direction; generator, shield, plating,
  engine, eight utility modules, three specials), the grade (Mk, Composite) as amber studs. The
  shop rows show the 16 px icon (locked items darker) on a glass row band, the selected item and the
  callouts the 24 px one; `vanguard.game.hangar.ItemIcons` derives the names (weapon slug, or kind
  and name) and fails at start if one is missing. The callouts became 46 px tall to hold the icon
  (front 160, rear 154, wings 132 px wide; the escort box 86 px), names that would not fit are cut,
  and the tile names shorten MICRO-MISSILE to MICRO-MSL and drop POD. Review files proposed for
  round 13; `art` stays `chosen`.
- 2026-10-02: Production art, UI batch part U3: the intel's sensor-L2 pictures
  (`tools/art/intel.py`, `assets/sprites/intel/`): a 30×30 sensor portrait per enemy type of
  Levels 01–02 (Skitter, Needler, Stinger, Spine Turret: the chosen round-04 models on the intel's
  dark teal scope) and a 40×40 silhouette per Act 1 boss (Gorgon Frigate, Brood Carrier: the
  outline only, filled flat with the scope's grid). From sensor L2 the panel shows the enemy
  portraits in a row above their names and the boss's silhouette at the right of its row (no
  level with a boss has data yet); the attack directions share a line where two fit, which keeps
  the panel's fields above Varga's line at sensor L3. Level 02's intel lists only the enemies of
  its waves, so its Spine Turret (a ground target) shows when M4 builds that level's intel.
  Review sheet proposed for round 13; `art` stays `chosen`.
- 2026-10-02: Concept round 13 closed (user decision): the Act 1 tactical map (`tools/art/ui_scenes.py`), the equipment icons in both sizes (`tools/art/icons.py`) and the intel's sensor-L2 portraits and Act 1 boss silhouettes (`tools/art/intel.py`) approved as **final**; `art` stays `chosen`, since the later acts' tactical maps and the intel pictures of the later enemies do not exist yet.
- 2026-10-02: M4 part A: the shop's "not yet in flight" note now marks only what the simulation does not fly yet (the specials, the utility modules, the mines and the Torpedo Pod).
- 2026-10-02 (user decision): the intel shows a level's set pieces from sensor L2 as unknown
  contacts, as Level 03's threat profile promises: "UNKNOWN HUGE CONTACT" (the unit's size tier) in
  the boss row's value column with the unit's 40×40 silhouette at the row's right, as a boss's
  (`tools/art/intel.py`, `assets/sprites/intel/boss-leviathan.png`); without a boss the label sits
  below the row's `NONE`. The content derives them from the level's `set_pieces` and the stat
  block's tier (`vanguard.content.campaign.Intel.contacts()`, field `CONTACTS`, sensor L2), so the
  name never shows and nothing is keyed to the Leviathan; below L2 nothing tells of a contact.
  Tests: `IntelTest.aSetPieceIsAnUnknownContactOfItsSizeTierFromSensorL2`,
  `PortraitsTest.everyContactOfALevelHasItsIntelSilhouette`.
- 2026-10-02 (user decision): the intel shows the teaser's speaker with the 72×72 radio portrait
  (`portraits/radio-<speaker>-<expression>`, the existing asset at its own size, not scaled) instead
  of the 144×144 briefing one, the name, `CDF INTEL` and the teaser beside it (22 characters a line),
  and the level's name below whichever ends lower: the fields move up about 64 px. Captured before
  Level 03 at sensor L2 and L3, the panel ran out at the bottom (the wave strip, the secrets and
  traits rows over Varga's four-line line, which itself ended below the panel); now everything
  fits. Varga's line now ends at the panel's foot whatever its length (its last line 10 px above
  the bottom edge) instead of starting at a fixed height. `IntelPanel` lays out on a canvas, and
  `IntelPanelLayoutTest` checks every level at every sensor level: no two parts overlap, nothing
  leaves the panel, every field ends above Varga's line;
  `PortraitsTest.everyHangarTeaserHasItsSpeakersRadioPortrait`.
- 2026-10-03: Concept round 16 closed (user decision): the intel portraits of the Spore Bomber and the Whirl Seed and the Leviathan's "unknown huge contact" silhouette approved as **final**; this doc's `art` stays `chosen`.
- 2026-10-03: Concept round 17 (user decision): the intel portraits of the Brood Pod and the Scuttler approved as **final** ([round 17](../../concept-rounds/round-17/README.md)).
- 2026-10-05: Concept round 23 (user decision): the intel portraits of the Mantis and the Coilwyrm approved as **final** ([round 23](../../concept-rounds/round-23/README.md)).
- 2026-10-05 (user decision D4 of M4 part H): Varga's intel lines are **one line per sensor level**
  per level (none, L1, L2, L3), text only and not voiced, instead of one per intel item; the "one
  line per intel item" item is closed by it. Part H wrote the 21 L1–L3 lines of Act 1 beside the
  existing no-sensor lines (each level's `threat_profile.varga`): each tells what the better sensors
  reveal at that level and stays within the fields the panel shows there. The data now needs all
  four lines (`LevelData.ThreatProfile`), and `Intel.varga()` gives the fitted sensor level's line
  instead of falling back to a lower one. Tests: `IntelPanelLayoutTest`
  (`everyLevelHasVargasLineForEachSensorLevel`: four different lines, no word cut; every level at
  every sensor level fits the panel, with and without an objective row). Level 07's boss silhouette
  and two rows of enemy portraits leave its L2 and L3 lines one panel line (30 characters).
- 2026-10-05: M4 part H, test fire: the shop's box (264×64 below the message lines) loops the
  selected weapon in the real simulation (`vanguard.game.hangar.TestFire`): the ship, with only
  that weapon fitted in the slot being shopped (the starter engine, shield and plating, nothing
  hits it), climbs unseen to the middle of the field and holds fire while three dummies on the
  ground drift down past it at 90 px/s, one on the weapon's line (a wing weapon's 12 px out
  towards its pod) and one 36 px either side, so a forward gun meets them ahead, a side gun
  beside the ship, a rear gun behind it, a bomb under its pod, a mortar shell on its snap and a
  homing missile on its turn. A dummy takes two of the weapon's level-1 hits (one level-1 bomb or
  shell blast), so a higher level kills sooner; when all three are gone (at most about 8 s) the
  loop starts over with the sortie's own retry, the same every time. The box shows the play field
  turned a quarter clockwise at half size (the wide box holds the field's length: ahead is right,
  behind left, the ship's left up), drawn with the level's pieces (`vanguard.game.render.TestFireView`:
  the ship with its pod, the weapon's shots, muzzle flashes, impacts and explosions) over a grid
  moving with the scroll, the dummies as amber target boxes flashing white when hit. Level: the
  detail's level, or the next one while the UPGRADE choice is highlighted (the purchase's result,
  label "TEST FIRE L1>L2"); locked weapons loop at L1; the Act 2 mines and torpedo say "NOT YET IN
  FLIGHT", other items "WEAPONS ONLY". The loop restarts whenever the weapon, slot or level
  changes; stepping allocates nothing; silent until the shop sounds are wired. Tests:
  `TestFireTest` (every flown weapon in every slot it fits at every level hits a dummy where it
  should, the loop is deterministic, starts over and allocates nothing), `TestFirePanelLayoutTest`
  (the box fits the drawer below the message, the hull and every lane fit across it, nearly the
  whole field along it, the labels fit). Capture for round 26: test-fire-capture-r26-a.
- 2026-10-05: Concept round 26 closed (user: the round accepted as proposed): the test fire accepted as built, with the three proposals of its questions: (a) the box shows the play field **turned a quarter clockwise** at half size (ahead is right, the ship's left up); (b) **locked weapons** loop at L1; (c) with the UPGRADE choice highlighted the box shows **the next level** ("TEST FIRE L1>L2"), the purchase's result. The capture is accepted. Varga's 28 intel lines (one per sensor level per level, D4 = A, not voiced) accepted as written, Level 07's short L2 and L3 lines included. `art` stays `chosen` (the later acts' tactical maps and intel pictures do not exist yet).
- 2026-10-05: M4 close-out: the open question on the **third utility bay** is closed. Its data
  entry is made in [ship systems](../../player/systems/README.md#utility-bays-confirmed) (5 000 cr,
  `available: act 3`); the hangar sells it from the visit before Level 15 (`Hangar.available(Bay)`),
  so no Act 1–2 visit offers it and the save is unchanged. The shop keeps its two UTL slots; buying
  the bay and the third UTL slot come with Act 3 (tracked in ship systems).
- 2026-10-06: M5 part A (user decisions D3 = b and D4 = a, see
  [wingmen](../../player/wingmen/README.md#decisions)): the escort UI is designed under *Escort*:
  the callout with his gun, armour and side; his four guns in the shop like a front gun's (escort
  inventory, 60 % prices, the visit's undo); a side toggle on the escort's detail (the L08 HUD hint
  "Rook's side can be set in the hangar"); a `ROOK` repair line at the player's repair cost; launch
  warnings for his armour below 50 % and for a grounded Rook. Main-agent choices: the side toggle
  is a setting outside the undo; his guns are test-fired from one nose muzzle; the mines' test fire
  needs dummies on `air`, since mines trigger only on `air` and `low-air`. The document is
  `in-progress` again while M5 builds it.
- 2026-10-06: M5 part A, the escort UI as built (main-agent choices, for review with concept
  round 28): once Rook is hired the escort is a tab after the rear mount; his callout grows to
  92×92 px under the left wing's callout (the escort title with his side `L`/`R`, his icon and
  `ROOK`, his fitted gun's name with five pips, his armour, amber below the launch warning's 50 %, or
  `GROUNDED` in red), so his longest gun name fits. The side is a last row `ROOK'S SIDE` in the
  escort's shop: left/right pick the side, confirm toggles it, its detail shows `LEFT` / `RIGHT`. The
  repair panel lists `SHIP` and `ROOK` with their missing points; Q/E switch the line (each keeps the
  most points the credits pay for), left/right and up/down set the points as before; it opens on the
  ship's line while that has points missing it can pay for. The launch warnings read `ROOK'S ARMOUR
  34/80` and `ROOK IS GROUNDED AND STAYS HOME`. His guns are test-fired from his craft (his nose
  muzzle at his scale, the dummies as tough as the scale asks). The side change was part of the
  visit's undo as first built (M5 part A1), against *Side* above; closed in the next entry.
- 2026-10-06: user decision: Rook's side stays outside the visit's undo, as *Side* says. A side
  change pushes no undo step, and undoing an earlier transaction keeps the side he has now
  (`Hangar.escortSide`, `Hangar.restore`; test `EscortTest.hisSideIsAHangarSettingOutsideTheUndo`).
- 2026-10-06: The module tiles' names fit their 48 px (M5 part A: names such as `AIRSTRIKE`,
  `SMART BOMB` and `TARGETING COMPUTER` are wider than the tile): at most 6
  characters of the 8 px label font, with the abbreviations under *Panels* (`Names.tile`; the weapon
  callouts use `Names.callout`). For the Salvage scanner `SALVGE` over `SCANNR`: the tile says what
  the module is for (salvage) and `SCANNR` would read like the Sensor suite, the other scanner in
  the utility bays. Test `LoadoutTilesLayoutTest`: every generator, shield, plating, engine,
  special and utility module fits 6 × 8 px, and those of Acts 1–2 (unlocked by Level 14) fit by
  their model or an abbreviation, never by the fallback cut. Later parts still fall back (`CMPIII`,
  `EVASIV`, `AUTO-R`, `PRESSU`, `ASCEND`) until their acts give them abbreviations.
- 2026-10-06: The capture's "1 FREE SMART BOMB CHARGE" notice was an artefact of its hand-made save
  (placed at Level 08), not a bug: the visit gave the Airstrike's and the Smart Bomb's free charges
  at once, and `HangarState.notice` keeps only the last notice. In a played campaign the two gifts
  come at different visits (their unlocks, Levels 04 and 06). No change.
- 2026-10-06: Concept round 28 closed (user: accepted): the escort UI approved as built (the escort
  tab after the rear mount, the 92×92 callout with his side, icon, gun and pips, armour amber below
  50 % or `GROUNDED`, his gun shop at 60 % with the visit's undo, the `ROOK'S SIDE` row outside the
  undo, the `SHIP` / `ROOK` repair lines, the two launch warnings, his guns' test fire from his
  nose), and the module tiles' abbreviations (`CMP I`, `CMP II`, `STRIKE`, `S-BOMB`, `FLARES`,
  `SENSOR`, `MAGNET`, `TARGET`, `SALVGE`) accepted; the later acts' modules keep the cut fallback
  until their acts. Every item but mouse navigation (M6) is ticked, so the document is `done`
  again; `art` stays `chosen` (the later acts' tactical maps and intel pictures do not exist yet).
- 2026-10-07: The intel panel's line under the teaser speaker's name is the briefing speaker's role
  line, no longer a fixed `CDF INTEL` (Level 08's capture, round 30: Rook's teaser read
  `LT. K. TANAKA / CDF INTEL`): `AEGIS TWO` for Rook, `CDF INTELLIGENCE` for Varga, `CDF COMMAND` for
  Okafor (Level 04's teaser) (`Speaker.role`; `IntelPanelLayoutTest.theTeasersSpeakerShowsWithTheirNameAndRole`;
  every level's intel still fits at every sensor level).
- 2026-10-07: [Concept round 30](../../concept-rounds/round-30/README.md) closed (user,
  2026-10-07): the Creeper's intel portrait (re-rendered with its readability lift) approved as
  **final**. Asked separately in the round (question 17), the user **keeps the role labels**: the
  line under the teaser speaker's name comes from the speaker's role (`Speaker.role`): Varga
  `CDF INTELLIGENCE`, Okafor `CDF COMMAND`, Rook `AEGIS TWO`. This supersedes the fixed
  `CDF INTEL` of the 2026-10-02 decision; `IntelPanelLayoutTest` passes for every level at every
  sensor level with the longer label. `art` stays `chosen`.
- 2026-10-07: M5 part C (user decision D7 = a): a level's threat profile can mark a trait
  `required`; its launch warning shows at every sensor level (Level 09's briefing already says that
  the nodes need `anti-ground`) and counts every source that damages hardened targets: an
  `anti-ground` weapon, Rook's Mortar while he flies, an Airstrike or Smart Bomb charge. Rejected:
  b (keep the sensor-L3 rule and only add Rook's gun and the specials: most players would launch
  into a mission they cannot win without a word) and c (as built: Level 09's text would be wrong).
  The document is `in-progress` again for it.
- 2026-10-07: M5 part C, game side: the required trait's warning reads `NO ANTI-GROUND SOURCE
  FITTED` (as long as a weapon's warning, so the launch dialog keeps its width) and comes first; a
  required trait is not warned about a second time by the sensor-L3 rule. Sources: a weapon with the
  trait in any slot, Rook's fitted gun while he flies the level (hired, not grounded), and for
  `anti-ground` an Airstrike or Smart Bomb fitted with at least one charge.
- 2026-10-07: [Concept round 31](../../concept-rounds/round-31/README.md) closed for the intel
  portraits (user, 2026-10-07): Level 09's Hive Node (iris half open) and Ravager (running down)
  approved as **final**.
- 2026-10-08: M5 part D, game side (user decision D9 = a): Level 10's required `rear` is met only by
  the weapon in the rear slot, and only one that fires behind the ship: the Tail Gun, the Fan
  Blaster and the Proximity Mines carry the `rear` trait; the Side Splitter (rear slot, trait
  `side`) fires sideways and does not count; Rook's guns and homing never do. The warning reads `NO
  REAR WEAPON FITTED` (only a weapon can bring it; shorter than Level 09's line). Our reading, for
  review in round 32: the Side Splitter does not silence it. The intel panel's level name drops to
  the label font when it would run into the sensor chip (Level 10's `EVACUATION CORRIDOR`, 19
  characters).
- 2026-10-08: [Concept round 32](../../concept-rounds/round-32/README.md) closed for the intel
  portraits (user): Level 10's Wraith and Mote Swarm approved as **final**; the build choices on the
  hangar (the Side Splitter not silencing the rear warning, the intel's long level name in the label
  font) accepted as built.
- 2026-10-08: M5 part E (stated defaults with the user's decisions of 2026-10-08): the Torpedo Pod
  is test-fired over a water strip with a submerged dummy, the last weapon's "NOT YET IN FLIGHT"
  label going with it. Level 11's threat profile recommends `spread` and `anti-ground` and marks
  `anti-sub` optional, not `required`, so no new launch warning (the sensor-L3 rule as before).
- 2026-10-09: [Concept round 33](../../concept-rounds/round-33/README.md) closed for the intel
  pictures (user): the Driftjelly's and the Reef Spitter's portraits and the Harbour Kraken's
  silhouette approved as **final**, the weak spots as they are (the dark Reef Spitter, the Kraken's
  silhouette without arms); Rook's teaser and Varga's four Level 11 lines approved (L2 and L3 as
  shortened to fit the panel).
- 2026-10-12: M6 part A: mouse navigation as under *Design*, pending the user's review in concept
  round 35. Main-agent default: slots are chosen by a click, not by hover. The command bar's and
  the repair panel's layout moved into `HangarPointer`, which the drawing and the hit regions share.
- 2026-10-12: M6 part B (Q1 = a, pending the user's review in round 35): the top bar's mission line
  before a level that is not built reads `ACT III - RED DUST: COMING SOON`
  (`ReleaseEndScreen.COMING_SOON`); after Act 2 the hangar is reached through Continue, since the
  outro now leads to the release's end.
- 2026-10-12: Back to `design: review` until [concept round 35](../../concept-rounds/round-35/README.md) closes (C4's coming-soon line and the mouse, A.1).
- 2026-10-12 (user, [round 34](../../concept-rounds/round-34/README.md)): the Lamprey's and the Skimmer's
  intel portraits and the Siege Spire's silhouette (12.2e, 13.2e, 14.2g) approved as **final**; Rook's
  teasers and Varga's intel lines for Levels 12–14 accepted. `art` stays `chosen`; `design` stays in
  `review` for concept round 35.
- 2026-10-12 (user, round 35): accepted as built: C4's coming-soon line (`ACT III - RED DUST: COMING
  SOON` before Level 15), the mouse in the hangar and the build choices A.1 (a)–(g) (slots chosen by
  a click, shop rows, chips and commands by hover) and D.3 (a) (the intel's "FOLLOWS IN A LATER
  RELEASE."); `design: approved`, `art` stays `chosen`.
- 2026-10-12: Step K10: the wave strip marks the arrival of a level's boss or mid-boss (its
  placement's `t`) with a red tick the strip's depth, drawn over the waves; Level 14's and Level
  11's *Sensor-suite detail* promised it. Levels 05 and 07 get the same tick, which their documents
  do not spell out (pending the user's review).
- 2026-10-13 (user, round 36, L.2): accepted: the red full-height BOSS tick on the intel's wave strip at the boss's time (sensor L3) on every boss level (L05, L07, L11, L14).
- 2026-10-13: M7 part A, step A5 (defaults pending the user's review): the third bay's `UTL` tile
  sits in the schematic's lower right corner, opposite Rook's callout (the module row has no room
  for an eighth 48 px tile); the bay is bought from a bay row at the end of the utility slots'
  shops and sold from its own slot's shop, and is not listed before the L15 visit (no locked row);
  the Act 3 specials' tiles read `EMP`, `LANCE` and `OVRCHG`; the Deflector Pod's test fire has
  the dummies fire a slow (110 px/s) harmless bullet at the ship every 0.6 s; a dummy stands 1 s
  of the L1 Ion Beam.
- 2026-10-13: M7 part A, step A8 (round 37's capture findings R.4 e–g, **pending the user's
  review**): a loadout already over its generator's output (a hand-written save, or one from before a
  draw changed) is refused for power only by a change that widens the gap, so it can still fit a
  special (no draw) or a lighter item; the detail of an item bought this visit reads `BOUGHT THIS
  VISIT: FULL REFUND` where its `VS FITTED:` line would stand, and that line is left out when nothing
  differs; the visit's free-charge notice leaves the message line on the next move or slot change
  (a transaction's message still stays until the next one); after buying a bay the cursor goes to the
  bay's new slot, on its first row.
- 2026-10-13 (user, round 37): the Act 3 icons **a** chosen (P.3: each item drawn as its effect); the
  emblems (b) rejected and moved to `concept/rejected/` (`tools/art/icons.py` routes the unchosen sheet
  there). The hangar capture accepted (K.4); step A5's defaults kept (B.4 a–e: the `UTL` tile in the
  lower right corner, the bay row from the L15 visit with no locked row, the tiles `EMP`, `LANCE`,
  `OVRCHG` and `DRONE BAY`, the test fire's slow dummy bullets, a dummy standing 1 s of the L1 Ion
  Beam); R.4 fix all: step A8's fixes (e)–(g) above kept; save format 4 accepted (V.1); the balance
  plan's purchases re-planned for R.1's act factor (step Q1).
- 2026-10-14 (M7 part B, step ART-B): production art from the chosen round-36 looks, review sheet `intel-final-r38-a` proposed for round 38 (pending the user's review).
- 2026-10-14 (user, round 38): the Burrower's and Dust Kite's intel portraits `intel-final-r38-a` approved as
  final (F.5); B.2 (d), the intel strip marking the six Burrower fields (`BURROW_FIELD`), kept. `art` stays
  `chosen`.
- 2026-10-18 (user, round 39): the Tendril Anchor's and Threadcrawler's intel portraits `intel-final-r39-a`
  approved as final (F.5); B.4 (f), the intel strip's GATE and CRAWLER ticks, kept. Level 16's L3 intel line
  now "Kill an anchor to drop a tendril. A cache down the left fork." and its traits `forward`, `piercing`
  (R.4, in the level's data). `art` stays `chosen`.
- 2026-10-18 (user decision, Act 2 playtest): the hangar after Continue and Load game comes after
  the next level's briefing; the mission select replay gets the replay hangar (`REPLAY L05`
  header, `> REPLAY` launch; its changes kept in the campaign, written to the autosave at launch,
  the latter pending the user's review).
- 2026-10-19 (user correction of the 2026-10-18 choice): the replay hangar is refit only ("purchasing
  items seems odd at this point"): fitting and unfitting what the campaign owns stays and is kept;
  buying, selling, upgrading, bays and repairs are gone from it. The buyable and locked rows are
  hidden rather than greyed, with `REPLAY: REFIT ONLY` on the message line (pending the user's review).
- 2026-10-19 (user, round 40, F.f): the Choir Herald's and the Shellback's intel pictures approved as final.
- 2026-10-19 (step IP, pending the user's review): the intel's fields close up on a busy level instead of
  running into Varga's line. After the Act 2 wave rework Level 14 at sensor L2/L3 names eight enemy types
  (Stingers, Mantises and Spore Bombers joined), four lines instead of two, and with the boss silhouette its
  WAVES row ran 6 px into the quote. `IntelPanel` now measures the fields first: their rows stay 14 px apart
  where they fit (every other level, unchanged), else close up a pixel at a time to 12 px (Level 14: 13 px),
  and only if that is not enough the enemy names end on `+N MORE` in as many lines as fit. Tests:
  `IntelPanelLayoutTest.aBusyLevelsRowsCloseUpAndKeepEveryName`, `tooManyEnemyTypesEndOnMore`.
- 2026-10-19 (user, round 41): the intel pictures (F.e) approved as final; the intel panel's closing rows and "+N MORE" (P.f) accepted.
- 2026-10-19 (user's playtest): hover only gives a row, chip, slot or command the hover look and never selects; a click selects, a click on a choice or command makes it; a chip click returns the focus from the command bar to the shop; a key press takes the hover look off. Replaces round 35's A.1 choice "shop rows, chips and commands by hover". While the pointer rests on one of the selected row's choices the test fire and power bar preview it (pending the user's review).
- 2026-10-20: round 42 closed: the Level 19 intel pictures (F.e) approved as final by the user; the shop's mouse handling (T.6: the selection stays with the clicked item) accepted.
- 2026-10-20 (user, round 43, Q.5): the hangar's art moves to `final` for the approved items only (the rear-guard bay's icon, F.d); `art` stays `chosen` for the rest. The user asked "When do we move the remaining hangar and briefing art to final?": an audit step lists the `chosen` items left in `ui/hangar` and `ui/briefing` for the next round.
- 2026-10-20: the Dust Colossus's intel silhouette (`boss-dust-colossus`) made with the part H batch (`tools/art/intel.py`, step ART-H2) and its review sheet generated for [round 44](../../concept-rounds/round-44/README.md) (F.d); pending the user's review.
- 2026-10-20 (user, round 44, A.a): the audit approved: `hangar-r07-b` (a layout mockup) moved to `concept/rejected/`; the 22 review sheets and captures of production assets (the Act 1 map and icons sheet, the intel sheets r13-r42, the test-fire and Act 3 hangar captures, the Act 3 icons) marked `final`; the Dust Colossus's intel silhouette (F.d) approved as final; **`art: final`** for the hangar. A later addition needs its own row.
- 2026-10-21 (the user's Act 3 playthrough: "My big weapons take too much power and I can't lower them. We should
  add an option to reduce power on fitted weapons to allow rebalancing the weapons on the ship (like more power
  to the rear)."; decided: A, a free power dial): the shop's POWER − / POWER + choices on a fitted weapon (see
  *Transactions*); the layout mockup shows the Mk III's new 16 MW.
- 2026-10-22 (M8 part A, step A5; defaults accepted in round 50 (2026-10-22)): the escort's Bulwark
  row sits between Rook's guns and the side row and is listed while locked like the other items
  (`LOCKED L22`); `FIT` / `UNFIT` choose the slot between the Bulwark and Rook; the callout shows
  whoever flies; the intel's `SPECIAL LIMITED` names only the **fitted** special (an owned Sonar Pulse
  in the inventory would otherwise mark every dry level); the hydro-kit notice reads `HYDRO-KIT
  FITTED: THE STORMHAWK AND ROOK ARE SEALED FOR THE OCEAN`; the test fire of the torpedo-type weapons
  and the Depth Charges is over the water's surface, not under it (D9: the water's factors stay out
  of the hangar).
- 2026-10-22 (ART-A; accepted in round 50 (2026-10-22)): the Harpoon Torpedoes' icon drawn with a 1.7× stroke so the barb reads at 16 px.
- 2026-10-22 (user, [round 50](../../concept-rounds/round-50/README.md) closed): F.7: the Act 4 hangar icons (`tools/art/icons.py --act4`) and the hangar capture accepted, the concept rows `chosen`.
- 2026-10-22 (M8 part B, steps ART-B and B4; accepted in round 51 (2026-10-22)): the `drill-rig` sensor L2 portrait (`intel-final-r51-a`); Level 22's
  threat strip marks the four rig sites with a red "T" (`IntelPanelLayoutTest.level22sStripMarksTheFourRigSites`).
- 2026-10-22 (user, round 51, closed; F.8): the `drill-rig` intel portrait accepted as final.
- 2026-10-22 (user, round 52, closed): the `glassfin` and `eel-swarm` intel portraits and Level 23's threat strip accepted as final (F.10).
- 2026-10-24 (M8 AW, step AW3; pending the user's review in round 56): a visit's second notice. At the one-off visit
  before the first under-water level the hydro-kit's notice is followed, on the next move or slot change, by `ENERGY
  WEAPONS SCATTER IN WATER: FIT TORPEDOES AND DEPTH CHARGES` (`HangarState.notice` queues it behind the first).
