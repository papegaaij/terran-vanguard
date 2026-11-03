---
title: Databank
design: approved
implementation: done
art: chosen
depends-on: [../hangar, ../main-menu, ../../player/systems, ../../systems/saves, ../../enemies, ../../player/weapons]
updated: 2026-11-03
---

# Databank

## Summary

An encyclopaedia of the campaign: every enemy, boss, weapon, ship part and key game concept the player has met or been
offered, with pictures, numbers and weapon advice. Opened from the [hangar](../hangar/README.md) and the
[main menu](../main-menu/README.md). An enemy's entry shows only what the **sensor suite** the campaign owns can tell
(the [intel ladder](../../player/systems/README.md#sensor-levels-and-hangar-intel)), so the databank is also where the
player learns that the suite exists and what it buys. The user's feature of 2026-11-01; its design confirmed by the user in round 82 (D.1) and built
the same day (steps DB1–DB3).

## Design

### What it holds (the user's decisions of 2026-11-01)

- **Only what the campaign knows.** An enemy or boss appears once it has been **met** in a level (on the screen in any
  attempt); a weapon, ship part, utility, special or escort once it has been **in the shop or the hangar** (offered or
  owned); a key concept once it has **come up** (its level flown). Nothing of a later act is named, counted or hinted.
- **Enemy and boss detail follows the sensor suite the campaign owns now**, never the one it had when it met the unit:
  buying a better suite fills every met entry at once. The player's own gear always shows in full.
- **It points to the sensor suite**: every locked field names the sensor level that would show it, and the suite has an
  entry of its own that explains the ladder (the user did not know the suite existed).

### Screen (960×540, the glass kit)

The out-of-game glass style of every menu screen ([ui](../README.md#shared-ui-rules)): a top bar, a row of tabs, the
list on the left (300 px) and the entry on the right (620 px), the key hints at the bottom. Drawn over the screen it was
opened from (the hangar's tactical map, or the title scene dimmed), with that screen's music playing on.

```
┌──────────────────────────────────────────────────────────────────────────────┐
│ DATABANK  CAMPAIGN - BEFORE L40 - HONOUR GUARD    [SENSOR SUITE  L3]  < BACK │
│           ACT VI - JOVIAN STORM - MEDIUM          [SHOWS EVERYTHING]         │
│ [ENEMIES 63] [BOSSES 9] [WEAPONS 24] [SHIP 31] [CONCEPTS 14]   Q/E           │
├────────────────────────┬─────────────────────────────────────────────────────┤
│ 63 KNOWN - 3 UNKNOWN ▲ │ ENEMY - FIRST MET L37 FALL OF AURELIA               │
│ ACT VI - JOVIAN STORM ─│ ┌────────┐ MIRROR INTERCEPTOR                       │
│  CHIMERA           L36 │ │ sprite │ CLASS   JOVIAN ASCENDANCY - MEDIUM - …   │
│  FLEET STORMHAWK ALLY  │ │ 1:1,   │ LAYER   AIR …     FROM  SIDES 70 % …     │
│► MIRROR INTERCEPTOR L37│ │animated│ KILLS 23   HP 120 (L37)   BOUNTY 101 CR   │
│  CRACKING TOWER NEW L39│ └────────┘ ATTACKS …              WEAK …            │
│  ???            ACT VI │ WEAPON ADVICE ─────────────────────────────────────  │
│                        │ [SHIELD-BREAKER] SHATTERS THE MIRROR  TESLA COIL POD │
│                        │ [FORWARD] BOUNCES BACK …                             │
│                        │ ┌Varga┐ "ITS MIRROR ONLY GUARDS THE NOSE …"          │
└────────────────────────┴─────────────────────────────────────────────────────┘
          Q/E TAB    UP/DOWN ENTRY    ESC BACK   (WEAPONS: LEFT/RIGHT LEVEL)
```

- **Top bar**: `DATABANK`, the campaign's next level and act and its difficulty, the **sensor badge** (the suite level the
  entries use: `L1`–`L3` in green, `NONE` in red with `SEE SHIP TAB`) and a `< BACK` chip.
- **Tabs** (the five things the user named): `ENEMIES`, `BOSSES`, `WEAPONS`, `SHIP`, `CONCEPTS`, each with its count of
  known entries.
- **The list** (layout **a**, the user's choice in round 82 D.2; layout **b**, a portrait grid, rejected):
  - its header counts the tab's entries: `63 KNOWN - 3 UNKNOWN`;
  - grouped under rule headers: enemies and bosses **by act** in the order met (first level, then name), weapons by
    slot (`FRONT`, `WINGS`, `REAR`, `SPECIALS`), ship parts by kind (`CORE`, `UTILITY`, `ESCORT`), concepts by act;
  - a row is the name and, on the right, the first-met level (`L37`) for an enemy or boss, the owned level, `FITTED`,
    `OWNED` or `SHOP` for gear; tags as the shop's: `ALLY` (green) on a friendly or neutral craft, `NEW` (amber) on an
    entry not opened yet, `SET PIECE` / `MID-BOSS` on bosses;
  - **unknown entries** are `???` rows, dimmed, with their act on the right (`ACT VI`), skipped by the cursor like
    mission select's locked levels;
  - 24 rows show; the list scrolls with the kit's markers and the wheel.
- **The entry page** (one page, no scrolling): a 136×136 **picture** inset on the left (196×196 on a page whose picture
  is a 192×192 databank plate: a boss, the Leviathan, the Abyss Ray, the Ascendancy concept; step DB2), the **name** in
  the heading font beside it, the **fields** below the name (label 8×12, values cyan, numbers white, credits amber), then the
  **advice** or **level table** block, then **Dr. Varga's line** with her 72×72 radio portrait (enemies, bosses and
  concepts) or a notes inset (gear).

### Enemy entries (by sensor level)

The suite level is the best **sensor suite the campaign owns** (fitted or stored) plus the difficulty's sensor bonus
(easy +1, at most L3), the hangar intel's rule but counting a stored suite (a databank is read at home, not in flight;
round 82 Q.4).

| Sensor | The entry shows | Locked fields read |
|---|---|---|
| none | the **name**, the **silhouette** (the sprite's shape in dark navy with a lit rim and scan lines), `FIRST MET Lnn <level name>`, Varga's **vague line**, and a callout that explains what a suite would add with a `SENSOR SUITE >` chip to its entry | `- - - -` and a tag `SENSOR L1` / `L2` / `L3` |
| L1 | + `CLASS` (faction, size tier, `MACHINE` if one), `LAYER` (with what reaches it: `AIR - EVERY WEAPON REACHES IT`, `HIGH-AIR - HOMING AND BEAM ONLY`, `GROUND - HARDENED: ANTI-GROUND ONLY`, `SUB - ANTI-SUB ONLY`), `FROM` (the edges it came in by, as shares: `SIDES 70 % REAR 30 %`), `KILLS` (the campaign's kills of it) | `SENSOR L2` / `L3` |
| L2 | + the **picture**: the unit's animated sprite at 1:1 (2:1 for a unit under 40 px, a databank plate for one over 128 px), `HP`, a hex `SHIELD` or a `MIRROR` (its heat), `BOUNTY` in credits, `ATTACKS` by name (one line each: name, pattern, count, bullet class and its damage, interval) | `SENSOR L3` |
| L3 | + `WEAK` (its weak points), the **weapon advice** block (below) and Varga's **precise tip**; a boss adds its **phase notes** | — |

- **HP, shield and bounty** are the numbers of the **last level the campaign met it in**, at the campaign's difficulty,
  computed by the code that spawns it (the act HP factor, the beam era's ×2 on `small` units, the Needler's and Stinger's
  capped factor, the difficulty's HP lever, one rounding), with that level named: `120  AT L37, MEDIUM`. The bounty is
  the credits one kill paid there (Act 1 bounty × act credit factor × difficulty income × the level's `bounty_scale`, one
  rounding): the Mirror Interceptor's 30 at Level 37 is **101 CR** (30 × 1.6⁵ × 0.32).
- **Allies and neutrals** (the wavering and the fleet Stormhawks, Levels 29 and 36) are entries too, tagged `ALLY` (a
  neutral craft `NEUTRAL`), with no weapon advice (a `DO NOT FIRE` line where hitting them costs a secondary).
- **Faction spoilers**: the `CLASS` field shows the faction as the data has it, and a unit whose data says `Unmarked`
  stays unmarked; a unit later unmasked (the Warden Tank's Act 5 livery) shows its new faction only from the level that
  unmasks it (a `faction_from: {level: name}` in the databank's text data). The Ascendancy concept entry opens at the twist
  (Level 28's end), never before.

### Weapon advice (generated)

The advice block lists one row per **effective trait** the unit's data names (its `traits`, the stat block's
"Effective traits") plus the rules that apply to it, each with a **reason** generated from the rules and, on the right,
**the player's own matching gear** in green (the fitted item first, then owned; nothing when none). A row for a weapon
that **hurts the player** is red. The rules, each owned by its own document:

| The unit has | Row | From |
|---|---|---|
| `ground` layer | `ANTI-GROUND  X2 ON EVERY PART` (hardened: `ANTI-GROUND  THE ONLY WAY IN; OTHER SHOTS GLANCE OFF`, with the Airstrike and Smart Bomb) | [layer rules](../../enemies/README.md#layer-rules) |
| `high-air` layer | `HOMING` / `BEAM  THE ONLY WEAPONS THAT REACH IT` | layer rules |
| `sub` layer | `ANTI-SUB  THE ONLY WEAPON THAT REACHES IT` (with the Smart Bomb); under water `X0.7 WITHOUT ANTI-SUB` | layer rules, [Europa](../../world/europa/README.md#under-water-rules) |
| a hex `shield` | `SHIELD-BREAKER  EMPTIES IT, NO REFILL FOR 3 S`, `EMP BURST  EMPTIES EVERY SHIELD`, `ANY  WEARS IT DOWN; IT REFILLS AFTER 2 S` | [enemy shields](../../enemies/README.md#enemy-shields) |
| a `mirror` | `SHIELD-BREAKER` / `EMP BURST  SHATTERS IT`, `BEAM  HEATS IT: BREAKS IN n S` (n from its heat and the fitted beam's damage), `SIDE` / `REAR  THE FLANKS ARE OPEN`, `BOMBS` / `SHELLS  FALL PAST IT`, red `FORWARD  BOUNCES BACK AS A SMALL BULLET (4)` | [reflect](../../enemies/README.md#reflect) |
| `machine: true` | `EMP BURST  STUNS IT 3 S` (a Vrell unit: `1.5 S`) | [EMP Burst](../../player/specials/README.md#emp-burst-l15) |
| frontal armour (the Gilded Gunship's) | `SIDE` / `REAR  ITS ARMOUR FACES FORWARD` | its stat block |
| an effective trait with no rule above | the trait and `EFFECTIVE`, plus the stat block's `notes.weak_points` text when it has one | its data |

The `WEAK` field is its `weak_points` names (and a multi-part unit's vital parts). Nothing in the block is hand-written:
a new unit gets its advice from its data alone. A unit whose data names no effective trait shows `ANY WEAPON` with the
fitted front gun.

### Boss entries

As an enemy entry, with a boss's own fields: `CLASS` with its role (`ACT BOSS`, `MID-BOSS`, `SET PIECE`), `BEATEN`
(the level, and its par from its data: `PAR 2:25`; a boss is beaten once killed or once its level is behind the
campaign, so a migrated save's bosses read beaten with their kills at 0), `HP` as the sum of its parts with the split (`17 220  CORE 6 600 +
13 BATTERIES`), and at L3 the **phases** block: three to five authored lines from the boss document's *Phases* section
(what changes, the tells that matter, the parts to kill first), then the advice block and Varga's tip. A boss met but not
beaten still has its entry. The picture is a still **databank plate** (192×192, rendered from the production models;
its silhouette at sensor none from the same plate, derived from the plate's alpha at load: no file of its own). The
plates: `databank/<slug>` ([tools/art/databank_plates.py](../../../tools/art/databank_plates.py)), one per boss of the
texts' `bosses`, the Leviathan and the Abyss Ray (the units whose sprite is over 128 px) and the Ascendancy concept's
cruiser.

### Gear entries (always in full)

- **Weapons**: the large icon at 4× in the picture inset, `TRAITS`, `YOURS` (`L2  FITTED ON THE LEFT WING`, `OWNED L3`, or
  `NOT OWNED` with the price), `HITS` and `NOT` (the layers it reaches and the ones it cannot, from its `hits`, traits and
  the layer rules), its targeting note where it has one; the **level table** L1–L5 (DPS by the shop's single-target rule,
  draw MW, cost, and the weapon's own key number: reach, width, count), the player's level lit amber; the hangar's **test
  fire** box looping the selected level (LEFT / RIGHT steps L1–L5); `STRONG AGAINST`: up to four **met** units whose
  effective traits it carries, with the reason from the advice rules; a one-to-two-line authored note.
- **Specials** as weapons: charges owned and most carried, price per charge or cooldown, the special's own numbers, the
  units it is the answer to (a hardened target, a shield, a mirror).
- **Ship parts** (generator, shield, plating, engine) and **utilities**: icon, `YOURS`, the per-level numbers of their
  data (output, regeneration, armour, speed, the utility's effect), price and draw, and a note.
- **Escort**: Rook (his four guns with their levels and DPS, his armour, his side) from Level 08, the Bulwark from
  Level 22.
- **The sensor suite** has the fullest entry: `YOURS`, its costs, its draw, its in-flight arrows, easy's bonus, and a
  table **what each level shows** in the hangar intel and in the databank, side by side, with Varga's line asking for
  one. Every `SENSOR Ln` tag and the `NONE` badge lead here (a click, or Enter on the callout's chip).

### Concept entries

Short authored pages (four to eight lines, a picture where the game has one: a capture or an existing sprite), each
opened by the level where it first matters. A first draft of the list (14 by the end of Act 6):

| Concept | Opens after | Picture |
|---|---|---|
| Shield and armour (regeneration, armour, a retry at zero) | L01 | the HUD's ship panel |
| Power and the generator (the budget, the power dial) | L01 | the power bar |
| Intel and sensors (the ladder, pointing to the suite's entry) | L01 | the intel panel |
| Weapon traits (the vocabulary; which trait answers what) | L01 | the trait chips |
| Layers: ground, air, high-air | L02 | a Spine Turret |
| Specials | L04 | the Airstrike icon |
| Rook and the escort | L08 | Rook's craft |
| Hardened targets | L09 | the Hive Node |
| Under water and the `sub` layer | L11 | the Driftjelly |
| The Ascendancy (the twist) | L28 | the cruisers (the plate `databank/ascendancy-cruiser`) |
| Vacuum | L29 | the belt |
| Enemy shields | L29 | the hex shield |
| Mirrors | L37 | the Mirror Interceptor |
| The three-way war | L37 | the hybrids |

### Where the data comes from

Generated from the data wherever it can be; the authored texts live in **one file**, `design/ui/databank/data.yaml`
(keyed by the unit, item or concept slug, so no Act 6 step's unit file is touched; written, 2026-11-01: 66 enemies, 9
bosses, 63 gear notes and 14 concept pages; the schema is in [schemas-player](../../tech/architecture/schemas-player/README.md)).

| Field | Source |
|---|---|
| Names, faction, tier, layer, machine, size, attacks, weak points, effective traits, shield, mirror | the unit's `data.yaml` (`design/enemies/*/*/data.yaml`) |
| HP, shield, heat at a level and difficulty | content's unit rules (the code that builds the sim's enemy spec) |
| Bounty in credits | the unit's bounty, the act credit factor, the difficulty's income, the level's `bounty_scale` |
| `FROM` shares | the waves of the levels flown that hold the unit (their entry edges), at the campaign's difficulty |
| First met, kills | the save's databank record (below) |
| Picture, silhouette | the unit's sprite frames in `assets/` (frame 0 faces down); the intel portraits (`assets/sprites/intel/`) for layout b and the intel's links; a databank plate for bosses and units over 128 px |
| Weapon numbers, test fire | the weapons', specials' and parts' `data.yaml` (`design/player/`), the shop's DPS rule, the hangar's test fire |
| Gear status | the save's loadout, inventory and unlocks; the shop's unlock levels |
| Weapon advice, `STRONG AGAINST` | generated by the rules above from the unit and weapon data |
| **Authored**: Varga's vague line and precise tip per enemy and boss (≈ 2 × 80), the boss phase notes (≈ 12 × 4 lines), a note per gear item (≈ 70), the concept pages (≈ 14 × 6 lines) | `design/ui/databank/data.yaml` |

Varga's lines follow the intel lines' rule: the vague one tells the shape and nothing the sensor would; the precise one
never says more than the L3 fields beside it (a weapon or a trait to bring, where to hit it). Text only, not voiced (as
the hangar intel's lines).

### The record in the save (format version 7)

A `databank` field, the only new state; everything else is derived from what the save already holds:

```json
"databank": {
  "met":  {"mirror-interceptor": {"first": 37, "kills": 23}, "iron-sovereign": {"first": 35, "kills": 1}},
  "read": ["mirror-interceptor", "tesla-coil-pod", "concept:shields"]
}
```

- `met`: per unit or boss slug, the first level it was met in and the campaign's kills of it (a boss's kill is its
  defeat). An attempt's units are those that were **on the screen** (a part of a boss counts its boss); its kills the
  enemies destroyed (every destroyed enemy of that kind, Rook's kills included). Both are kept from **every attempt**
  (won, failed, restarted, aborted) and written with the autosave that follows it; quitting from the pause menu to the
  main menu keeps nothing of that attempt (no save is written there). A **replay** from mission select counts too,
  written to the autosave at its debrief (round 82 Q.5). Debug runs write nothing, as always.
- `read`: the entries opened at least once (the `NEW` tag is a known entry not in it).
- **Gear and concepts need no record**: an item is known when its unlock level is at or below the save's next level, or
  it is owned or unlocked (`loadout`, `inventory`, `unlocks`, the escort); a concept when its level has been flown.
- **Migration** of a version 6 save: `met` holds every unit, set piece and boss of the waves of every level below its
  `nextLevel`, at the save's difficulty (waves the difficulty skips left out, waves gated on the escort counted), each
  with that first level and `kills` 0 (the kills count from the update on); `read` holds all of them, so an updated
  campaign opens with no flood of `NEW` tags. A version 6 save at the Level 40 hangar opens the databank with Acts 1–6's
  units through Level 39 known.

### The ways in, and the controls

- **Hangar**: a fourth command chip `DATA` in the top bar beside `UNDO`, `REPAIR`, `SAVE` (the four chips narrowed from
  64 to 54 px and moved to x 424, so the longest mission line, `BEFORE L10 - EVACUATION CORRIDOR`, still fits), a key
  (**Tab**, gamepad **Y / Triangle**), and a click or Enter on an **enemy portrait in the intel panel** opens that
  enemy's entry (its boss row's silhouette: the boss's). Back returns to the hangar as it was (the selection, the shop
  list, the visit's undo kept).
- **Main menu**: a `DATABANK` item between `MISSIONS` and `SCORES`, for the **current campaign** (the most recent save,
  the one Continue loads, as Missions does); disabled while no save exists. The panel moves up from y 199 to y 172 so its
  nine items keep their 33 px step above the key hints. Back returns to the menu.
- **Keys** (the shared menu rules): Q / E and the gamepad's bumpers (or a click on a tab) change the tab; up / down
  move through the known entries; LEFT / RIGHT step a weapon's test-fire level; Enter on a `SENSOR SUITE >` chip or a
  `SENSOR Ln` tag opens the suite's entry, Back from there returns to the entry it came from; Esc / B / the right mouse
  button go back. The cursor starts on the last entry opened (in that tab), or on the first `NEW` one.
- **Mouse** (the [ui](../README.md#mouse) rules): hover selects only while the pointer moves, a click selects a row or a
  tab, the wheel scrolls the list, a click on the chip or a tag follows it.

### Discoverability of the sensor suite

Besides the locked tags, the badge and the suite's own entry: the hangar intel's `SENSOR NONE` box gets the line
`DATABANK: SHIP > SENSOR SUITE` while the campaign owns no suite; the concept entry *Intel and sensors* opens after
Level 01; the first time the databank opens with no suite, the cursor starts on the sensor callout of the selected
enemy.

## Concept art

Concept round 82 (closed 2026-11-01, every row as recommended): mockups at 1280×720 (the 960×540 screen as a 1280×720 window shows it),
drawn from the real assets by [tools/concept/databank_r82.py](../../../tools/concept/databank_r82.py); the plates' final
art by [tools/art/databank_plates.py](../../../tools/art/databank_plates.py). Prompts and briefs:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/databank-r82-a.png](concept/databank-r82-a.png) | Layout a: the list left (names by act, the first-met level, `ALLY` / `NEW` tags, `???` rows), the Mirror Interceptor at sensor L3 | chosen (round 82 D.2) |
| [concept/databank-enemy-none-r82-a.png](concept/databank-enemy-none-r82-a.png) | The Mirror Interceptor with no sensor suite: name, silhouette, first met, locked fields with their sensor level, the callout to the suite, Varga's vague line | chosen (round 82 D.1) |
| [concept/databank-boss-r82-a.png](concept/databank-boss-r82-a.png) | The Iron Sovereign at L3 (BOSSES tab): stats, phase notes, weapon advice, Varga's tip | chosen (round 82 D.1) |
| [concept/databank-weapon-r82-a.png](concept/databank-weapon-r82-a.png) | The Tesla Coil Pod (WEAPONS tab): status, layers, the L1–L5 table with the player's level lit, test fire, strong against | chosen (round 82 D.1) |
| [concept/databank-sensor-r82-a.png](concept/databank-sensor-r82-a.png) | The sensor suite's own entry (SHIP tab), owned none: what each level shows in the intel and in the databank | chosen (round 82 D.1) |
| [concept/databank-access-r82-a.png](concept/databank-access-r82-a.png) | The ways in (960×600 at 1:1): the hangar's top bar with the `DATA` chip, the main menu with `DATABANK` | chosen (round 82 D.1) |
| [concept/databank-plates-final-r82-a.png](concept/databank-plates-final-r82-a.png) | The databank plates, final art (step DB-ART): the nine bosses, the Leviathan, the Abyss Ray and the Ascendancy concept's cruiser at 1:1 on 192×192, then the sensor-none silhouette each gives (a preview of the look the game derives at load, ½) (sheet) | chosen (round 79 H.4 ok: the plates as the screens show them) |
| [concept/databank-capture-r82-none.png](concept/databank-capture-r82-none.png) | Capture (step DB3, a `playthroughSave` Level 40 save): the Mirror Interceptor at sensor none, opened from the main menu | chosen (round 79 H.4 ok: the same screens) |
| [concept/databank-capture-r82-l1.png](concept/databank-capture-r82-l1.png) | Capture: the same entry with a stored suite L1 (class, layer, entry edges, kills) | chosen (round 79 H.4 ok: the same screens) |
| [concept/databank-capture-r82-l2.png](concept/databank-capture-r82-l2.png) | Capture: suite L2 (the picture, HP and mirror heat at L37, bounty, attacks) | chosen (round 79 H.4 ok: the same screens) |
| [concept/databank-capture-r82-l3.png](concept/databank-capture-r82-l3.png) | Capture: suite L3 (weak points, the weapon advice with the fit's gear, Varga's tip) | chosen (round 79 H.4 ok) |
| [concept/databank-capture-r82-boss.png](concept/databank-capture-r82-boss.png) | Capture: the Iron Sovereign at L3 (plate, `BEATEN L35` with kills 0 after DB3's fix, the HP split, phases, advice) | chosen (round 79 H.4 ok: the same screens) |
| [concept/databank-capture-r82-weapon.png](concept/databank-capture-r82-weapon.png) | Capture: the Tesla Coil Pod fitted at L2 (levels table, test fire, strong against, note) | chosen (round 79 H.4 ok) |
| [concept/databank-capture-r82-hangar.png](concept/databank-capture-r82-hangar.png) | Capture: the Level 40 hangar without a suite: the `DATA` chip, `TAB DATA`, the intel's `DATABANK: SHIP > SENSOR SUITE` line | chosen (round 79 H.4 ok: the same screens) |
| [concept/databank-capture-r82-menu.png](concept/databank-capture-r82-menu.png) | Capture: the main menu with `DATABANK` selected (the panel at y 172) | chosen (round 79 H.4 ok) |

Rejected proposals (1 row) are listed in [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [x] `content`: a databank index built from the data (units, bosses, weapons, specials, parts, utilities, escort,
      concepts) with each entry's act, group, unlock rule and generated fields; the authored texts' loader
      (`design/ui/databank/data.yaml`, checked: every unit and item has its lines, Varga's lines within the label width)
- [x] `content`: an entry's fields at a sensor level (the ladder above), HP / shield / heat / bounty at the last met
      level and the campaign's difficulty by the spawn code, the `FROM` shares from the levels' waves, the advice rows
      from the rules table, `STRONG AGAINST`; tests per rule (a hardened, a high-air, a sub, a shielded, a mirrored, a
      machine unit; a boss)
- [x] `content`: save format version 7 with `databank` (`met`, `read`), the version 6 migration (the waves of the levels
      below `nextLevel` at the save's difficulty), a version 7 save without it unreadable; tests
- [x] `game`: the databank screen (tabs, list, entry page, picture with the animated sprite or the silhouette, test fire,
      Varga's inset), keyboard, gamepad and mouse; opened from the hangar (`DATA` chip, Tab / Y, an intel portrait) and the
      main menu (`DATABANK`), Back to where it came from (step DB2: `vanguard.game.databank`; `DatabankScreenTest`)
- [x] `game`: the attempt's met units (on the screen) and kills per kind (`ENEMY_DESTROYED`'s kind) merged into the
      campaign at every attempt's end, a replay's at its debrief; written by the next autosave (step DB2: `DatabankLog`,
      `DatabankLogTest`)
- [x] `pipeline` / `tools/art`: a databank plate (192×192) for each boss and each unit over 128 px; the intel's 30×30
      portrait for the units that have none (Level 39's Cracking Tower and Pump Station), if layout b is chosen (step
      DB-ART: 12 plates in `assets/sprites/databank/`; no portraits, layout a being the recommendation)
- [x] The hangar intel's `SENSOR NONE` hint line; the main menu's panel at y 172 (step DB2)
- [x] `content`: a boss counts as `BEATEN` once killed or once its level is behind the campaign, so a migrated or
      `playthroughSave` campaign (kills from 0) reads its earlier bosses beaten (step DB3;
      `DatabankTest.aMigratedCampaignHasBeatenEveryBossBehindIt`)
- [x] Captures of the screen at each sensor level, a boss, a weapon, the hangar and the main menu for the round after
      the build (step DB3: `concept/databank-capture-r82-*`)

## Open questions

None: round 82 answered every question of the draft on 2026-11-01 (see *Decisions*). The build's own calls (steps
DB-ART, DB1, DB2) wait for the user's review with the captures in the next round.

## Decisions

- 2026-11-01: The user's feature: a databank of enemies, bosses, ship upgrades, weapons and key concepts with details
  (HP, effective weapons) and graphics, reachable from the hangar and the main menu. Decided with the user: entries only
  once seen or owned, a counter of the unknown ones, no spoilers of later acts; enemy and boss detail by the sensor suite
  owned now (the intel's ladder), filling every met entry at once; own gear in full; the databank points to the sensor
  suite; designed and built now, beside Act 6.
- 2026-11-01: Draft (step DB0), decided as trivia; confirmed by the user in round 82 (D.1, 2026-11-01): five tabs (the user's five
  words); `ALLY` / `NEUTRAL` entries without advice; HP and bounty at the last met level; the advice generated, never
  hand-written; one authored-text file; the save's `databank` field (version 7) with `met` and `read`, gear and concepts
  derived; the migration from the levels flown with kills from 0; Tab / Y and a `DATA` chip in the hangar, a
  `DATABANK` item in the main menu (panel at y 172); text-only Varga lines.
- 2026-11-01: The plates (step DB-ART), decided 2026-11-02 (round 79 H.4 ok, shown in the captures): each boss or unit as the game assembles it
  from its production frames (the generators' own compose functions, pivots and offsets), at rest and nose down; 1:1
  when it fits 184 px (the Honour Guard's three aces), else reduced as the render path reduces a render (box filter,
  1-bit alpha, unsharp mask); glows only where they fall on the body, so no translucent pixel; 48 colours a plate. The
  Abyssal Maw shows its head and the coil's last six segments (the whole arena ring left the head a speck); the Harbour
  Kraken its head surfaced over the arms that grip the platform from below, the platform left out; the Dust Colossus its
  open mouth (the intel's view). The units over 128 px are the Leviathan and the Abyss Ray only; the Ascendancy concept
  gets a cruiser plate; the other concept pictures are existing sprites or captures. The silhouette is derived at load.
- 2026-11-01: Built in `content` (step DB1; `vanguard.content.databank`: `Databank`, `DatabankView`, `Pages`, `Advice`,
  `LevelUnits`; the texts' `DatabankData` and `DatabankChecks`; save format 7's `DatabankRecord`), decided as trivia
  pending the user's review with the captures in the next round: a gear entry's id is `<kind>:<slug>` (`generator:mk-ii`, `special:emp-burst`,
  `utility:sensor-suite`, `escort:rook`) except a weapon's, its slug (the core parts share their names across kinds);
  a gear item's slug is its name without the nickname, a `+` read "plus" (`Mk V+ "Crucible"` is `mk-v-plus`); a unit's
  numbers are those of the last level flown that holds it (the save keeps only its first level), else of its first
  met; `FROM` counts the waves' units by edge (a cross split one front, two sides, one rear) and the ground units
  (front, or a side); a weapon's `HITS` / `NOT` come from its `hits`, its traits and the layer rules (its README's
  hand-written *Layers hit* cell is not data); a neutral craft whose level spares it (Level 29's) is `NEUTRAL` with
  `DO NOT FIRE` at every sensor level, the fleet's `ALLY` without advice; the Leviathan is a `SET PIECE` entry under
  `ENEMIES`; a boss's HP split names its vital parts with their HP and counts the rest by kind (`CORE 6 600 + 6 X SAM +
  4 X RAIL + 3 X LASER`); generated lines wrap at 63 characters and read the multiplication sign as `X`; the version 6
  migration also marks read the gear and concepts its next level knows, so an updated campaign shows no `NEW` tags; it
  reads the design data on the classpath unless the game passes its own; `playthroughSave` fills the record the same
  way; a replay's record reaches a save through `SaveGame.withDatabank`.
- 2026-11-01: Built in `game` (step DB2), decided as trivia pending the user's review with the captures in the next round: a page with a
  192×192 plate has a 196×196 picture box (the 136×136 one is too small for a plate at 1:1); a unit's picture is its
  sprite from the unit atlas of the level its numbers are of (loaded alone a moment after the cursor rests, the last two
  kept), frame 0 (a tilt set's middle heading) of a unit that turns, its frames looped at 8 fps otherwise, its intel
  portrait at 4× when the frames are missing; the silhouette is the sprite's or plate's alpha in navy with a lit
  one-pixel rim and every third row a lighter scan line, drawn with the flash shader; an entry counts as opened once
  shown for 0.75 s (passing it with the keys keeps its `NEW` tag); from the main menu the save's record goes back into
  its slot on the way out (only `databank` changes; a debug run writes nothing); a boss's page lists its `ATTACKS` by
  name (the phases tell the patterns) and shows Varga's portrait at 36×36 beside her line, so the page keeps its phases
  and advice; advice rows with the same reason share a row (`HOMING` `BEAM` on a high-air unit) and the plain effective
  traits share one `EFFECTIVE` line; the ship tab's list names a core part with its kind (`GEN MK I "SPARK"`, `SHIELD
  MK I`); the concepts' pictures are existing sprites (an intel portrait or an icon at 4×, the cruiser's plate) until
  captures replace them; a click on an intel picture opens its unit's entry (a unit not met yet opens its tab), Enter on
  a portrait is not built (the intel panel has no keyboard focus); the hangar hint reads `DATABANK: SHIP > SENSOR
  SUITE` under the `NO SENSOR` chip.
- 2026-11-01 (user, round 82 closed; page answers, no notes): **every row as recommended**. D.1 **ok**: the design as
  drafted is confirmed, so the part is `design: approved`; D.2 **a**, the list (layout b's mockup rejected, moved to
  `concept/rejected/`); Q.1 the unknown counter counts **every act reached**; Q.2 the numbers at the **last level met**;
  Q.3 **per campaign** (in the save); Q.4 a **stored** suite counts; Q.5 **replays count**; Q.6 **all three** ways in
  from the hangar (the chip, Tab / Y, a portrait); Q.7 the Cracking Tower and the Pump Station are **entries**. Steps
  DB1 and DB2 had built on these recommendations already; nothing changes.
- 2026-11-01: Closed (step DB3): a boss page read `BEATEN  NOT YET` for every boss of a migrated or `playthroughSave`
  campaign (kills from 0, found by DB2); a boss now counts as beaten once killed or once the last level that holds it
  is behind the campaign's next level (a boss seen ahead of its own fight stays `NOT YET`). The full `check` green; the
  captures `concept/databank-capture-r82-*` from the game on a Level 40 `playthroughSave` (stored suites L1–L3 added),
  for the user's review with the plates and the build's calls in the next round.
- 2026-11-02 (user, round 79 H.4 closed, ok, no note): the databank's screens in the game (the main menu, the list and an
  enemy, a weapon, and with them the other `databank-capture-r82-*` captures and the plates) accepted as built; art
  `chosen`.
- 2026-11-03 (user, Act 6 playthrough; step PT2): a weapon's `TRAITS` show a line each with the trait's meaning
  (`AREA: SPLASHES ONTO TARGETS CLOSE BY`), and the *Weapon traits* entry lists all eleven traits with their meanings
  under its text; the words are the hangar's (design/ui/hangar, *Trait words*). The hangar intel's `RECOMMEND` tags
  open that entry on a click.
