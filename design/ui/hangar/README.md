---
title: Hangar
design: approved
implementation: in-progress
art: final
depends-on: [../../player, ../../systems/economy, ../../campaign]
updated: 2026-10-29
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

Rejected proposals (4 rows) are listed in [concept/rejected/README.md](concept/rejected/README.md).

Concept [round 06](../../concept-rounds/round-06/README.md) — full 960×540 screens at 1× plus a 2× detail crop; generator `tools/concept/ui_r06.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/hangar-r06-b.png](concept/hangar-r06-b.png) | Hangar B — central ship schematic with callouts to the slots, shop drawer left, intel with the large Varga portrait right | superseded by r07-b (glass style) |

Concept [round 07](../../concept-rounds/round-07/README.md) — layout B in the menu's glass style; generator `tools/concept/ui_r07.py`.

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
| [concept/intel-final-r55-a.png](concept/intel-final-r55-a.png) | The `glow-angler` sensor L2 portrait (30x30; `tools/art/intel.py`, M8 part F batch) | chosen (round 55, F.8: accepted as final) |
| [concept/intel-final-r57-a.png](concept/intel-final-r57-a.png) | The `depth-hunter` and `relay-beacon` sensor L2 portraits (30x30; `tools/art/intel.py`, M8 part G batch) | chosen (round 57: accepted as final) |
| [concept/intel-final-r58-a.png](concept/intel-final-r58-a.png) | The `boss-abyssal-maw` sensor silhouette (40x40; `tools/art/intel.py` from `tools/art/abyssal_maw.py`'s sprites, M8 part H batch): the coil round the arena's edges with the head at the top | chosen (round 58: accepted as final) |
| [concept/intel-final-r62-a.png](concept/intel-final-r62-a.png) | The `talon`, `sam-nest`, `asteroid-mite`, `clamp-gun` and `wavering-stormhawk` sensor L2 portraits (30x30; `tools/art/intel.py` from `tools/art/belt_units.py`'s production models in Q.11 b's gunmetal, M9 part B batch, step B4): Level 29's new units nose down (the SAM Nest's rack over its base, the Stormhawk's IFF light lit) | chosen (round 62, F.11: accepted as final) |
| [concept/intel-final-r63-a.png](concept/intel-final-r63-a.png) | The `minelayer`, `gilded-gunship`, `dart-drone` and `rail-serpent` sensor L2 portraits (30x30; `tools/art/intel.py` from `tools/art/belt_units_c.py`'s production models, M9 part C batch, step C4): Level 30's new units nose down (the Minelayer's drum dark, the Gunship's doors shut and lamps dark, the Rail Serpent's engine car) | chosen (round 63, F.8: accepted as final) |
| [concept/intel-final-r64-a.png](concept/intel-final-r64-a.png) | The `hornet`, `shard-drone` and `buzzsaw-drone` sensor L2 portraits (30x30; `tools/art/intel.py` from `tools/art/belt_units_d.py`'s production models in Q.11 b's gunmetal, M9 part D batch, step ART-D) | chosen (round 64, F.11: accepted as final) |
| [concept/level-28-intel-capture-r58-a.png](concept/level-28-intel-capture-r58-a.png) | Game capture, M8 part H (step H7): the Level 28 hangar from a playthrough save (a sensor suite L3 fitted for the capture): Okafor's teaser, the threat strip with the Choir's lime mark at the pre-boss pause, the portraits, the Bulwark loaner with `ROOK MIA` | chosen (round 58: accepted as final) |
| [concept/level-26-intel-capture-r55-a.png](concept/level-26-intel-capture-r55-a.png) | Game capture, M8 part F (step F6, a playthrough save at Level 26 on medium, a sensor suite L2 fitted for the capture): the hangar with Level 26's intel (Rook's teaser, the threat strip, the five portraits incl. the Glow Angler, the lure note), the panel at 1:1 | chosen (round 55, F.8: accepted as final) |
| [concept/level-27-intel-capture-r57-a.png](concept/level-27-intel-capture-r57-a.png) | Game capture, M8 part G (step G5, a playthrough save at Level 27 with a sensor suite L3 fitted for the capture): the hangar before Level 27, Okafor's teaser, the threat strip, the five portraits with the Depth Hunter's `UNMARKED GREY`, the Bulwark loaner in the escort slot (`ROOK MIA`) | chosen (round 57: accepted as final) |
| [concept/intel-final-r65-a.png](concept/intel-final-r65-a.png) | The `sentinel-tower` and `strider` sensor L2 portraits (30x30; `tools/art/intel.py` from `tools/art/belt_units_e.py`'s production models in Q.11 b's gunmetal, M9 part E batch, step ART-E) | chosen (round 65, F.7 ok: accepted as final) |
| [concept/intel-final-r66-a.png](concept/intel-final-r66-a.png) | The `void-leech` and `rail-bunker` sensor L2 portraits (30x30; `tools/art/intel.py` from `tools/art/belt_units_f.py`'s production models: the leech's Vrell black-violet, the bunker in Q.11 b's gunmetal, M9 part F batch, step ART-F2) | chosen (round 66, F.8 ok: accepted as final) |
| [concept/intel-final-r67-a.png](concept/intel-final-r67-a.png) | The `warden-tank-asc` sensor L2 portrait (30x30; `tools/art/intel.py` from `tools/art/warden_tank.py`'s model in its Act 5 livery: A.15 b's gold on Q.11 b's gunmetal, driving down, the turret aimed along it; M9 part G batch, step ART-G) | chosen (round 67, G.7 ok: accepted as final) |
| [concept/intel-capture-r67-a.png](concept/intel-capture-r67-a.png) | Game capture (step G5; [recipe](concept/prompts.md#intel-capture-r67-a)): the hangar before Level 34 at sensor L3: the liveried tank's portrait first, `OBJECTIVE: HELIX CORE`, the crates' sections underlined in gold | chosen (round 67, G.7 ok: accepted as final) |
| [concept/intel-final-r68-a.png](concept/intel-final-r68-a.png) | The `boss-iron-sovereign` sensor L2 silhouette (40x40; `tools/art/intel.py` from `tools/art/iron_sovereign.py`'s frames: the three rings at rest with their batteries and the core, the base disc left out so the rings read; M9 part H batch, step ART-H1) | chosen (round 68, H.2 ok: accepted as final) |

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
| [concept/act3-hangar-capture-r37-a.png](concept/act3-hangar-capture-r37-a.png) | Game capture (round 37, M7 part A), the hangar before Level 19 from a hand-written save: the Act 3 shop rows (Ion Beam, Deflector Pod, Light Drone Bay, Rear Lance, EMP Burst, Orbital Lance, Shield Overcharge bought, Evasive thrusters) and the `UTILITY BAY 3` row bought, the `UTL` tile appearing (sheet) | final (round 44, A.a: a record of production assets) |
| [concept/act3-hangar-capture-r37-a.mp4](concept/act3-hangar-capture-r37-a.mp4) | The same, 97 s | final (round 44, A.a: a record of production assets) |
| [concept/act4-icons-final-r50-a.png](concept/act4-icons-final-r50-a.png) | The four Act 4 equipment icons (`tools/art/icons.py --act4`): Harpoon Torpedoes, Depth Charges, Sonar Pulse, Bulwark (escort) at 16 and 24 px, beside the Torpedo Pod, Proximity Mines, EMP Burst and Rook icons for the style (sheet) | chosen (round 50, F.7: accepted as final) |
| [concept/act4-hangar-capture-r50-a.png](concept/act4-hangar-capture-r50-a.png) | Game capture (round 50, M8 part A, step A5), the hangar before Level 27 (Rook MIA) from a scratch save: the Harpoon Torpedoes row and its test fire, the escort callout with the loaner Bulwark and `ROOK MIA`, the escort shop's Bulwark row before and after `BUY`, the Depth Charges' test fire over water (sheet of 4 frames) | chosen (round 50, F.7: accepted) |
| [concept/act5-icons-final-r61-a.png](concept/act5-icons-final-r61-a.png) | The two Act 5 weapon icons (`tools/art/icons.py --act5`, M9 part A): Tesla Coil Pod (the pod with an arc from its terminal) and Swarm Tail (three micro-missiles fanned out backwards) at 16 and 24 px, beside the Micro-missile Pod and Tail Gun icons for the style (sheet) | chosen (round 61, F.5: accepted) |
| [concept/act5-hangar-capture-r61-a.png](concept/act5-hangar-capture-r61-a.png) | Game capture of the hangar before the [capture rig](../../enemies/concept/prompts.md#belt-rig-capture-r61-a)'s Level 08 (aborted to the hangar, `--loadout utility=sensor-suite:3 --special airstrike:2`): the Tesla Coil Pod in the L WING shop with `NEW` and the ◆ (the rig's intel recommends `shield-breaker`), its detail `<AREA> <SHIELD-BREAKER>` and the test fire's arc, the intel's `SPECIAL LIMITED · AIRSTRIKE: IN ROCK`; right: the REAR shop with the Swarm Tail. The intel's RECOMMEND row cuts `SHIELD-BREAKER` at the panel's edge | chosen (round 61, F.5: accepted) |
| [concept/act6-icons-final-r73-a.png](concept/act6-icons-final-r73-a.png) | The four Act 6 equipment icons (`tools/art/icons.py --act6`, M10 part A, step ART-A): Plasma Arc (a nose emitter, a jagged arc to a node forking on to two more), Reflex Shield (the shield badge with its top quarter gold), Mk V+ "Crucible" (the generators' ring round a white-hot core in a molten orange ring, Mk V's studs), the IFF spoofer redrawn (a green reticle and a steel ghost reticle offset from it; it was a Vrell eye) at 16 and 24 px, beside Composite V (as is), the Mk V "Helix", the Ion Beam and the Shield Mk V for the style (sheet) | proposed (round 73) |

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

Entries dated before 2026-10-21 (M8's start): [decisions-archive.md](decisions-archive.md).

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
- 2026-10-24 (M8 AW, step AW3; accepted in round 56 (2026-10-24)): a visit's second notice. At the one-off visit
  before the first under-water level the hydro-kit's notice is followed, on the next move or slot change, by `ENERGY
  WEAPONS SCATTER IN WATER: FIT TORPEDOES AND DEPTH CHARGES` (`HangarState.notice` queues it behind the first).
- 2026-10-24 (user, round 56 closed): the rework's hangar notices accepted; **Q.1 yes**: the waterline's rules strip
  also names the energy weapons, `UNDER WATER · BULLETS ×0.7 · BEAMS ×0.2 · TORPEDOES +50 %` (step FX, from the water's
  factors, `WaterlineTell.rules`). To keep it on one line inside the 480 px play field (8 px clear of each edge,
  `Level23LooksTest.theRulesStripReadsTheWatersFactors`) its `DRAG` gave way: the drag is felt at once, the weapons'
  factors are what the strip must name (pending the user's review). The replay hangar now shows the replayed mission's
  escort ([mission select](../mission-select/README.md#decisions)).
- 2026-10-25 (user, [round 57](../../concept-rounds/round-57/README.md) closed): the `depth-hunter` and `relay-beacon` intel portraits and Level 27's intel accepted as final (F.8).
- 2026-10-26 (user, [round 58](../../concept-rounds/round-58/README.md) closed): the `boss-abyssal-maw` sensor silhouette and Level 28's intel accepted as final (F.6).
- 2026-10-28 (user, [round 62](../../concept-rounds/round-62/README.md), closed; page answers): the five Level 29 intel portraits (F.11: Talon, SAM Nest, Asteroid Mite, clamp gun, wavering Stormhawk) accepted as final.
- 2026-10-28 (user, [round 61](../../concept-rounds/round-61/README.md), closed): the Act 5 icons and the hangar's ◆ marker accepted (**F.5**). Round 63 closed the same day: the four intel portraits of Level 30 accepted (**F.8**).
- 2026-10-28 (user, [round 64](../../concept-rounds/round-64/README.md), closed): **F.11** accepted: the Hornet's, Shard Drone's and Buzzsaw Drone's sensor L2 portraits are final.
- 2026-10-29 (user, [round 65](../../concept-rounds/round-65/README.md), closed): **F.7** ok: the Sentinel Tower's and Strider's intel portraits accepted as final; **C.5** accepted (the intel marks each fork on the wave strip).
- 2026-10-29 (user, rounds [round 67](../../concept-rounds/round-67/README.md) and [round 68](../../concept-rounds/round-68/README.md), closed): **G.7** ok: the Warden Tank's livery portrait (`warden-tank-asc`) and **H.2** ok: the Iron Sovereign's sensor L2 silhouette (`boss-iron-sovereign`) accepted as final.
