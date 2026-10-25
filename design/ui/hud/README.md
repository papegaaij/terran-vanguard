---
title: HUD
design: approved
implementation: in-progress
art: final
depends-on: [../../player, ../../systems/scoring, ../../art-direction]
updated: 2026-10-22
---

# HUD

## Summary

The play field (480×540) is kept clear. All status information lives in the two 240×540 side
panels. Only boss health, warnings and pickup pop-ups appear in the play field itself. The
left panel is about the mission (score, radio), the right panel about the ship.

## Design

### Layout (960×540; each character cell ≈ 10×20 px)

```
┌────────────────────────┬────────────────────────────────────────────────┬────────────────────────┐
│ MISSION 21             │ ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░  DUST COLOSSUS   │ ARMOUR              64 │
│ DUST COLOSSUS          │   (boss bar, only during boss fights)          │ █████████████████░░░░░ │
│                        │                                                │ SHIELD              18 │
│ SCORE                  │                                                │ ███████░░░░░░░░░░░░░░░ │
│             1 204 350  │                                                │ POWER ▮▮▮▮▮▮▮▮▯▯  +15% │
│ CREDITS                │                                                │                        │
│                12 450  │                                                │ WEAPONS                │
│ CHAIN 34          ×2.5 │                                                │ F Scatter Vulcan ■■■□□ │
│ ▰▰▰▰▰▰▰▰▰▰▰▱▱▱▱▱▱▱▱▱▱▱ │                                                │ R Tail Gun       ■□□□□ │
│                        │                                                │ L Micro-missile  ■■□□□ │
│ ┌────────┐ ROOK        │                   PLAY FIELD                   │ R Micro-missile  ■■□□□ │
│ │PORTRAIT│ Aegis Wing  │                    480×540                     │ OVERDRIVE ▰▰▰▰▰▱▱  12s │
│ │ 72×72  │ ▂▃▅▇▅▃▂     │                                                │                        │
│ └────────┘             │                                                │ SPECIAL     AIRSTRIKE  │
│ "Six o'clock, Lancer!  │                                                │ ◉◉○○                   │
│  Bandits closing on    │                                                │                        │
│  your tail."           │                                                │ ESCORT  ROOK           │
│                        │                                                │ ███████████████░░░░░░░ │
│                        │                                                │                        │
│                        │                                                │                        │
│                        │                       ▲                        │                        │
│                        │                      ███                       │                        │
│                        │                                                │                        │
│ PROGRESS               │                                                │                        │
│ ▕██████████░░░░░░░░░◆▏ │                                                │                        │
└────────────────────────┴────────────────────────────────────────────────┴────────────────────────┘
```

### Left panel (mission)

| Element | Notes |
|---|---|
| Mission number and name | Top |
| Score | 8 digits, counts up quickly |
| Credits | Earned so far, including level-start balance |
| Chain | Chain count, multiplier and draining window bar (see [scoring](../../systems/scoring/README.md)) |
| Radio | 72×72 portrait with static on open/close, name, subtitle below the portrait up to 3 lines × 22 chars per page; a longer line is paged: each page typed out, then held 3 s (the last page 5 s), and a spoken line's page at least until its [voice](../../audio/voice/README.md) has reached the page's end (the last page until the voice ends); a radio line is at most **two pages** of word-wrapped lines (writing rule); queued messages: **timed lines go first** (a line at its time in the level script plays before any waiting event line), an **event line** (a reaction to an escaped enemy, a secret, a kill) waits for a gap, a free radio long enough to play it before the next timed line is due, and is dropped as stale once it has waited more than **6 s**; a boss phase's lines wait the same way but go stale only after **20 s**, so a reply queued behind the phase's first line still plays (dropped at the boss's kill); the lines that close a level (its end, a met secondary objective, a boss's death) wait for a gap too but are never dropped, and one queued while the radio is free (nothing on it, nothing waiting) **goes ahead at once**, the timed lines that fall due waiting for it, except a secondary's line still waiting (or only just opened) when a boss's warning starts, which is dropped (the HUD shows the objective done); urgent warnings interrupt (and cut the playing voice; the interrupted line replays with its voice): the Airstrike's call and Rook's **eject bark** ([wingmen](../../player/wingmen/README.md#radio-barks): exempt from the barks' spacing, never stale, it cuts his own waiting or playing bark). After a won level the scroll runs on and the debrief waits until the radio has shown its last message (the level-end line and whatever is still queued), at most 15 s after the level end |
| Progress | Level progress bar with a boss marker at the end |
| Control prompts | The contextual prompts of the first levels (see [Level 01](../../campaign/act-1-first-contact/level-01-break-at-dawn/README.md#launch-and-control-prompts)): one line each, the action on the left and its keys on the right (the four arrow keys read `ARROW KEYS`), at most three at once; the box is shown only while a prompt is pending. An **event prompt** (M5 part F, shown on a level event instead of a time: Level 12's `SHAKE OFF` · `LEFT-RIGHT`) is a warning and stands in the play field instead (see *In the play field*) |
| Objective tracker | Shown for every primary or secondary objective; hidden in levels without one. Compact box above the progress bar: objective icon and short label (e.g. "DOCKS", "CRAWLERS", "BATTERIES", "NODES", "SHUTTLES", "CONVOY", "RELAY"), then progress as pips or counters (docks, crawler pips, batteries A–D, hive nodes, shuttles, ship pips, relay integrity bar). A pip flashes green on success and red on a loss or failure; the whole box flashes when the objective is won or lost. Used by [L02](../../campaign/act-1-first-contact/level-02-shipyard-burning/README.md), [L04](../../campaign/act-1-first-contact/level-04-tranquility-run/README.md), [L05](../../campaign/act-1-first-contact/level-05-crater-nest/README.md), [L09](../../campaign/act-2-homefront/level-09-arcology-fall/README.md), [L10](../../campaign/act-2-homefront/level-10-evacuation-corridor/README.md), [L11](../../campaign/act-2-homefront/level-11-atlantic-convoy/README.md) and [L13](../../campaign/act-2-homefront/level-13-polar-relay/README.md); the secondary objective of [L01](../../campaign/act-1-first-contact/level-01-break-at-dawn/README.md) shows a `KILLS n / 76` counter that turns `DONE` and flashes green when met; [L03](../../campaign/act-1-first-contact/level-03-spore-drift/README.md)'s "nothing gets through" shows `BOMBERS n / 10` (destroyed of all), `DONE` in green when met or `FAILED` in red once one gets through, the box flashing green or red. **Two objectives** (a primary and a secondary at once: [L04](../../campaign/act-1-first-contact/level-04-tranquility-run/README.md), [L05](../../campaign/act-1-first-contact/level-05-crater-nest/README.md), [L09](../../campaign/act-2-homefront/level-09-arcology-fall/README.md), [L13](../../campaign/act-2-homefront/level-13-polar-relay/README.md)): a two-line tracker, the primary on line one and the secondary on line two. Level 09 (M5 part C): line one `NODES A1 A2 B1 B2 C1 C2`, one **two-character mark** per named node (the last word of its group's name; dim while it lives, struck through in green when destroyed, red and the line flashing red when one gets past and the mission fails), line two `RAVAGERS n / 8` (the bridge waves' Ravagers destroyed of all; 6 on easy, 10 on hard), then `DONE` or `FAILED`. Level 04: line one `CRAWLERS` with five pips (green; amber below 50 % HP; a white flash on a hit; a red flash, then dark, when lost), line two `PODS n / 6` (pods killed before they burst, of all), then `DONE` or `FAILED`. Level 10 (planned, M5 part D, user decision D3 = a): the two-line tracker with **one** objective, line one `SHUTTLES n / 4` (the saveable shuttles still flying; the count turns red and the box flashes red when the last is lost and the mission fails), line two **five small armour bars** in order, Lifeline One to Five (each about 32 px wide, filled by its armour share: green, a white flash on a hit, amber below 50 %, a red flash then dark when lost; Lifeline Three's bar dark from its scripted loss at t=118, the count unchanged; a shuttle **home** after the climb-out shows a full bar in pale mint under a green glow, unlike both a flying and a lost one's). Level 11 (planned, M5 part E, a stated default): the one-line tracker for its secondary objective, `CONVOY` with **three ship pips** right, one per cargo ship in order (Halvorsen, Mbeki, Saint-Laurent: green; amber after the ship's first slam; a red flash, then dark, when it sinks, the box flashing red as the objective fails), turning `DONE` (green) or `FAILED` (red) at the Kraken's death; the debrief's secondary row `HULLS AFLOAT n / 3`. Level 12 (planned, M5 part F, a stated default): the one-line tracker for its `collect` secondary, `SONDES n / 3` (the weather sondes collected of all), the count flashing green at each collection, turning `DONE` (green) at the third or `FAILED` (red, the box flashing red) when a sonde leaves the screen uncollected; the debrief's secondary row `SONDES n / 3`. Level 13 (M5 part G, stated defaults of the gap check, pending the user's review; built in step G6): the two-line tracker with the **primary** `defend`: line one `RELAY` with the Nansen Relay's **integrity bar** (green; a white flash per hit; amber below 50 %, a tick marking 50 %; a red flash, then dark, at 0, when the mission fails) and, on the right from the halt's start, the **sync countdown** `2:58 … 0:00` (the synchronisation's 178 s, rounded up); line two the secondary `INTEGRITY ≥ 50 %`, turning `DONE` (green) at the sync's end or `FAILED` (red) the moment the relay drops below half; the debrief's secondary row `RELAY n %` (the relay's integrity at the end; pays when met). Level 14 (M5 part H, a stated default, pending the user's review; built in step H6): the one-line tracker for its secondary, `ROOTS n / 8` (the root-burst growths destroyed of all; `n / 4` on easy), then `DONE` or `FAILED`; the debrief's secondary row `ROOTS n / 8` and its **BOSS TIME** row with the Siege Spire's par (the [debrief](../debrief/README.md) rule) |

### Left panel layout

The left panel (240×540) is a fixed vertical stack: each region is as tall as the most it can
show and keeps its place whether it is filled or empty, so nothing moves or overlaps. Regions
are 6 px apart, 16 px from the panel's top and bottom edges, and 208 px wide (16 px side
margins). Label plates are 22 px tall; an LCD well has a 2 px dark frame and is 24 px tall for
one line or 60 px for three (lines 18 px apart, text 6 px from the well's sides, so 196 px wide).
Text never runs over its well: a line that is too long is cut off at the well's edge, and the
content rules (radio pages of 3 × 22 characters, one-line prompts) keep that from happening.

| Region | y (px from the top) | Contents |
|---|---|---|
| Mission | 16–60 | plate `MISSION nn`, the level's name below it |
| Score | 66–116 | plate, one-line well, digits right-aligned |
| Credits | 122–172 | plate, one-line well, digits right-aligned |
| Chain | 178–206 | `CHAIN n` and the multiplier on one line, the window bar (6 px) below |
| Radio | 212–378 | plate; the 72×72 portrait with the speaker's name beside it (one word per line, 126 px wide); 4 px below, the three-line subtitle well |
| Control prompts | 384–448 | three-line well: action left (88 px column), keys right |
| Objective tracker | 454–482 | one-line well: label left, count right |
| Progress | 488–524 | plate, progress bar (10 px) |

In a level with two objectives, or an objective with a second line (Level 10's armour bars), the
tracker takes one line from the control prompts' well:
control prompts 384–430 (a two-line well, 42 px), objective tracker 436–482 (a two-line well,
42 px). The layout is fixed per level, so nothing moves during it.

### Right panel (ship)

| Element | Notes |
|---|---|
| Armour | Bar + number; flashes red at 30 % and 15 % |
| Shield | Bar + number; flickers while down after a break. Planned (M5 part F, Level 12): while a [Lamprey](../../enemies/air/lamprey/README.md) drains it, the label reads `SHIELD DRAIN` in the warning colour and the bar flickers the same way (if the label does not fit the row, `DRAIN` replaces `SHIELD`; a layout test) |
| Power | Spare power as pips and the resulting regen bonus; glows during overdrive. Output drained by enemies (Void Leech) shows in a warning colour and the gauge flashes when load exceeds output (see [generator](../../player/generator/README.md#enemy-drain-effects)) |
| Weapons | Front, rear, left, right with level pips (5); overdrive timer bar. A weapon that cannot fire in the level reads so in its row instead of its pips: a Torpedo Pod in a level without water reads `NO WATER` (M5 part E; it still draws its power), as the Bomb Rack's `NO GROUND` will in a level without ground |
| Special | Icon, charges or cooldown ring; greyed out if unavailable in this setting |
| Escort | Rook's (or the heavy drone's) armour; "EJECTED" when down. A box under the special row in the weapons' style: the `ESCORT` plate, Rook's 16 px hangar icon and `ROOK`, his armour as a bar with the number (flashing red at 30 %, as the player's); after the eject the bar is empty and the well reads `EJECTED` in red. Drawn only while an escort flies in the level (from Level 08, or with `--escort`); otherwise its region stays empty, so nothing else moves |

(The mock shows the level 21 *Dust Colossus* boss fight; numbers are illustrative.)

### In the play field

- Boss health bar and name at the top during boss fights: the sum of the boss's remaining
  part HP; a mid-boss's bar is shorter (Level 05's Gorgon Frigate is the first, M4 part E).
- **Warning** banners ("WARNING — HOSTILES FROM THE REAR") and **edge warnings**: every wave
  that enters from the sides or the rear gets a flashing arrow at that edge of the play field
  at least 3 s ahead, always, often with a radio call (readability rule in
  [enemies](../../enemies/README.md#bullet-readability-rules)). Only a wave's **first entry**
  warns: a unit's own re-entry from a side or the rear as part of its flight pattern (a Wraith's
  pop-up behind the ship, a chain's or a swarm's loop-back) gets no warning, banner or rear bark,
  and a **boss never triggers** them (the walker's entries, the Dust Colossus's breaches, the
  Siege Spire's launched Wraiths): its own tells and the boss warning banner announce it
  (2026-10-21, user). The banner's band sits across the
  upper play field (its middle 164 px below the top edge); in a level with an air escort
  (Level 10) it sits higher, 97–127 px below the top edge, so it never covers the shuttle band
  (stations from 140 px down). The boss warning banner opens at the boss's lead warning when it has
  one, and closes early rather than hide a boss that fights under it.
- Level 12's **gust telegraph** (planned, M5 part F) is a weather look, not an edge warning: the
  wind streaks brighten and an arrow shows at the upwind edge 1.0 s ahead (easy 1.5 s), drawn
  unlike the flashing edge-warning arrows so the two are never confused
  ([Level 12](../../campaign/act-2-homefront/level-12-storm-front/README.md#weather)).
- Level 12's **shake-off prompt** (an event prompt, M5 part F): a warning where the player looks
  while a Lamprey drains the shield. `SHAKE OFF` in amber and `LEFT-RIGHT` in the label colour on
  a one-line box in the wave banner's look (dark band, amber frame pulsing with the edge
  warnings), sized to its line (19 px tall), centred across the play field (so the shake's jolts
  do not move it) with its middle 80 px below the ship's centre, under the hull and a Lamprey at
  its tail, at least 16 px above the bottom edge. It shows on every latch that lasts at least
  0.3 s, at most 6 s each time, until the attempt's first shake-off; then not again that attempt.
- With a sensor suite at L2+, extra arrows also track individual off-screen threats (single
  enemies, homing missiles) between waves.
- Small floating numbers for credits picked up. Can be disabled in options (Gameplay tab,
  `CREDIT NUMBERS`).

## Concept art

Rejected proposals (5 rows) are listed in [concept/rejected/README.md](concept/rejected/README.md).

Concept round 01 — see [round 01](../../concept-rounds/round-01/README.md). AI-generator prompts: [concept/prompts.md](concept/prompts.md). Generated by `tools/concept/hud.py`. Both show the full 640×360 screen at 2×, level 07 *Brood Carrier* state (rear gun and escort slot still empty).

| File | What | Status |
|---|---|---|
| [concept/hud-r01-a.png](concept/hud-r01-a.png) | HUD A — classic metallic bevelled panels with LCD readouts | chosen |
| [concept/hud-r02-a.png](concept/hud-r02-a.png) | Round 02: HUD A at 960×540 with 240 px panels in palette B, full element list from this document | chosen |

Concept [round 08](../../concept-rounds/round-08/README.md) — HUD A refresh (metal, unchanged style) with the current element set; generator `tools/concept/ui_r08.py`.

| File | What | Status |
|---|---|---|
| [concept/hud-r08-a.png](concept/hud-r08-a.png) | HUD A refresh — L11 Kraken fight: Rook's radio portrait and subtitle queue, overdrive timer, escort box, boss bar with weak point, edge warning | chosen |

Concept [round 09](../../concept-rounds/round-09/README.md) — generator `tools/concept/vfx_r09.py`.

| File | What | Status |
|---|---|---|
| [concept/edge-warnings-r09-a.png](concept/edge-warnings-r09-a.png) | Edge warnings (side and rear) with flash cycle, sensor threat arrows, wave and boss banners, close-ups and 1× play-field panels | chosen (the edge-warning look itself is superseded by edge-warnings-r11-a) |

Concept [round 11](../../concept-rounds/round-11/README.md) — edge warnings that are harder to
miss (after playing Level 01), over a still frame of Level 01's backdrop placeholders; generator
`tools/concept/ui_r11.py`. Each PNG shows the side and rear warning at 1× and one flash cycle at
2×; each GIF 1.6 s of a left warning at 30 fps. The tones are in [sfx](../../audio/sfx/README.md).

| File | What | Status |
|---|---|---|
| [concept/edge-warnings-r11-a.png](concept/edge-warnings-r11-a.png) | A "big pulse": the round-09 bar and chevrons at about 3× (300 px bar, 20 px chevrons, 2× text) with a soft glow; grows in over 0.2 s, then pulses smoothly between 45 % and 100 % (sheet) | chosen |
| [concept/edge-warnings-r11-a.gif](concept/edge-warnings-r11-a.gif) | A "big pulse" (motion) | chosen |

Production art, UI batch part U1 (for concept round 13, not yet opened): the HUD's metal parts
rendered by [tools/art/hud.py](../../../tools/art/README.md) into `assets/sprites/hud/`, reviewed
from the files the game loads. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/hud-final-r13-a.png](concept/hud-final-r13-a.png) | Review sheet: the two 240×540 side-panel plates (bevel, four domed corner rivets, brushed steel) with 4× corners, the label plate, the LCD well, phosphor fill and glow nine-patches with their splits, the portrait well, and the pieces as the game draws them | chosen |
| [concept/hud-capture-final-r13-a.png](concept/hud-capture-final-r13-a.png) | Game capture of Level 01 at 960×540 (radio, control prompts, kill tracker, gauges, weapon box) | chosen |

Concept [round 14](../../concept-rounds/round-14/README.md) — the right panel with the arsenal (no new art: the round-13 kit).

| File | What | Status |
|---|---|---|
| [concept/hud-capture-final-r14-a.png](concept/hud-capture-final-r14-a.png) | Game capture of Level 01 with `--loadout` (Hammer Mortar, Bomb Rack, Autocannon Pod, Side Splitter): the power row and the four weapon rows with pips and the overdrive timer | chosen |

Concept round 26 (M4 part H): the wave banners, the sensor suite's threat arrows, the low-armour
flash and the credit-numbers option in the game (no new art: drawn in code in the round-09 look).
Prompts: [concept/prompts.md](concept/prompts.md#hud-warnings-capture-r26-a).

| File | What | Status |
|---|---|---|
| [concept/hud-warnings-capture-r26-a.png](concept/hud-warnings-capture-r26-a.png) | Game captures: Level 02's side wave with its banner, Level 06's rear loop-back banner (the left-and-right one waiting for it), the sensor L2 arrows at a Coilwyrm's body below the screen and at a Skitter above it; the armour gauge's low-armour flash off and on; the Gameplay tab's `CREDIT NUMBERS` row | chosen |

Concept [round 16](../../concept-rounds/round-16/README.md): the escapes tracker and the layer-skip prompt in the game (no new art: the round-13 kit). Prompts: [concept/prompts.md](concept/prompts.md#escapes-tracker-capture-r16-a).

| File | What | Status |
|---|---|---|
| [concept/escapes-tracker-capture-r16-a.gif](concept/escapes-tracker-capture-r16-a.gif) | Game capture of Level 03 (left panel and play field at 1×, 12 fps, two cuts, 6.5 s): the `LOW-AIR` prompt leaving as the first Spore Bomber dies and `BOMBERS 0 / 10` turning `1 / 10`; then `2 / 10` turning `FAILED` in red with the box's red flash as a bomber of the 42 s line escapes | chosen |

Production art for concept round 26 (M4 part H batch): the boss bar's plate, rendered by
`tools/art/boss_bar.py` in the HUD's metal look; prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/boss-bar-plate-final-r26-a.png](concept/boss-bar-plate-final-r26-a.png) | `hud/boss-bar-plate.9.png`: a 64×20 nine-patch steel strip with riveted end caps and a recessed trough (splits L14 R14 T7 B7, content box L11 R11 T7 B7 = the 6 px fill), at 8× with its splits, and round the 400 px and 240 px bars as drawn | chosen |
| [concept/special-limits-capture-r50-a.png](concept/special-limits-capture-r50-a.png) | Game captures of the special row's limits (M8 part A, 2× crops of the right panel): `NO WATER` on the Sonar Pulse (and the Harpoons and Depth Charges) on Level 18 before the water, `UNDER ICE` on the Airstrike on Level 20 under water | chosen (round 50, K.1: accepted) |

## Implementation

- [x] Side panel frames: bevelled metal plates with corner rivets, label plates, LCD wells, bar troughs and phosphor fills from the production art (`tools/art/hud.py`)
- [x] Left panel: mission, score, credits, chain, radio, progress
- [x] Left panel layout: fixed regions without overlap; texts cut off at their well; a test checks the regions and that every prompt and radio line of the content fits, measured with the font's metrics
- [x] Two-objective levels (L04, L05, L13): the two-line tracker and the two-line control prompts' well (shown with a convoy: Level 04's `CRAWLERS` line; Level 05's and Level 13's first lines come with them)
- [x] Level 10's tracker (D3 = a, M5 part D): `SHUTTLES n / 4` over five armour bars (a bar drawer
      in `MissionPanel`: the hit flash, amber below 50 %, the red flash and dark when lost, Lifeline
      Three dark after its scripted loss), fitting the 208 px well (`MissionLayoutTest`) — built:
      `MissionPanel.drawShuttleTracker` (n the saveable shuttles alive of `Sortie.saveableAllies()`,
      the line red and flashing as Level 04's when the mission fails), five 32×8 bars 41 px apart
      (`ALLY_BAR_*`, filling the 196 px text width) centred in line two, each in its trough and
      filled by its armour share; a level without a secondary objective (`Secondary.none()`) shows
      no secondary line or one-line tracker, and its debrief no secondary row; a shuttle home after
      the climb-out full in pale mint under a green glow (`MissionPanel.ALLY_HOME`, 2026-10-08)
- [x] Level 11's tracker (M5 part E, step E3c): `CONVOY` with three ship pips (green, amber after
      the first slam, the red flash then dark when sunk), `DONE` or `FAILED` at the Kraken's death,
      fitting the one-line well (`MissionLayoutTest`); the debrief's row `HULLS AFLOAT n / 3` —
      built: `MissionPanel.drawConvoyTracker` (the `cargo-ship-pip` tinted, a pip per damageable
      convoy unit, white for a moment on a hit), `DebriefScreen.hullsRow`; `PartEHudTest`,
      `LevelScreenPartETest`
- [x] The weapon row's `NO WATER` for a Torpedo Pod in a level without `water: true` (M5 part E,
      step E3c: `ShipPanel.NO_WATER` right-aligned in place of the pips, `Hud.idle` from
      `Sortie.mountIdle`; `PartEHudTest`)
- [x] Level 12's tracker (M5 part F, step 6): `SONDES n / 3`, the green flash per collection,
      `DONE` or `FAILED`, fitting the one-line well (`MissionLayoutTest`); the debrief's row
      `SONDES n / 3`; `PartFHudTest` — built: the escapes tracker's line (the `collect: sondes`
      objective counts as one), `MissionPanel.collectFlash` (the well green for a blink, 32 frames, per
      sonde), `DebriefScreen.hullsRow` with the `SONDES` label (`LevelScreen.debriefCount`;
      `LevelScreenPartFTest`)
- [x] `SHIELD DRAIN` and the shield bar's flicker while a Lamprey drains (M5 part F, step 6;
      `PartFHudTest`) — built: `ShipPanel.SHIELD_DRAIN` in amber on the label plate (it fits, so
      `DRAIN` alone is not needed), the bar flickering as after a break while `Sortie.draining()`
- [x] The event prompt (`SHAKE OFF` · `LEFT-RIGHT`) in the play field below the ship, its box
      sized to its line, on every latch of at least 0.3 s for at most 6 s until the attempt's
      first shake-off (M5 part F, steps 4 and 6, moved out of the control prompts' well after the
      round 34 capture) — built: `EventPrompt`, driven by `LevelScreen` from `LAMPREY_LATCHED`,
      `LAMPREY_SHAKEN` and `Sortie.latched()` (`PartFHudTest`)
- [x] Level 09's tracker: `NODES` with six two-character marks (`A1` … `C2`; the group marks widened from one letter, the line fitting the 208 px well, a layout test) over `RAVAGERS n / 8` — M5 part C (`MissionPanel.markStep`/`markLeft`: a mark's glyph advance + 4 px, 24 px for a pair, right-aligned; Level 05's letters keep their 16 px; `MissionLayoutTest`)
- [x] Objective tracker: icon, label, pips/counters or integrity bar per level, success/fail flash; hidden in levels without an objective (Act 1's done: Level 01's kill counter, Level 02's dock pips, Level 03's bomber counter, Level 04's crawler pips, Level 05's `BATTERIES A B C D` over `NEST n / 30`, Level 06's `MANTISES` escapes and Level 07's bay count; the later levels' trackers are part of those levels' own tickets)
- [x] Right panel: armour, shield, power (spare-power bar and the regen bonus, glowing in an overdrive), weapons (front, rear, left, right with level pips), overdrive timer
- [x] Right panel: special (M4 part D): a row under the weapons box in their style, `SPECIAL`, the special's 16 px hangar icon, its name and `×` charges; greyed while its strike flies or with no charge left, flashing red when the button is denied
- [x] Right panel: escort box (plate, icon, `ROOK`, armour bar and number, the 30 % flash, `EJECTED`; hidden without an escort) — M5 part A
- [x] Radio message queue with portraits, priority interrupts
- [x] Boss bar and name at the top of the play field, shorter for a mid-boss (M4 part E); drawn on
      its production plate `hud/boss-bar-plate` (a nine-patch) once that exists, plainly until then
      (M4 part H)
- [x] Edge-warning arrows and floating credit numbers (M3) and the boss warning banner (M4 part G,
      with track 22 and the klaxon)
- [x] Wave warning banners ("WARNING — HOSTILES FROM THE REAR"), the sensor suite's threat
      arrows, the option to hide the credit numbers (M4 part H); with an air escort the banner
      above the shuttle band (`WaveBanners.ESCORT_CENTRE_Y`, `WaveBannersTest`, 2026-10-08)
- [x] Low-armour flash: the armour readout and gauge flash red at 30 % and faster at 15 % (M4 part H)

- [x] Level 13's `RELAY` line with the integrity bar and its 50 % tick, the sync countdown, the
      `INTEGRITY ≥ 50 %` line and the debrief's `RELAY n %` row (M5 part G, step G6; `PartGHudTest`,
      `LevelScreenPartGTest`)
- [x] Level 14's `ROOTS n / 8` tracker and debrief row, the BOSS TIME row with the Spire's par and
      the Act 2 summary page `ACT II COMPLETE · HOMEFRONT` (seven rows, Levels 08–14; the act's data
      cores are checked against L08–L14: none of them hides one, the page reads `NONE FOUND`), as the
      Act 1 summary of the [debrief](../debrief/README.md) (M5 part H, step H6; `PartHHudTest`,
      `LevelScreenPartHTest`, `DebriefActEndTest`)
- [x] The hangar intel for Levels 13 and 14 (M5 parts G and H): Varga's lines (Level 13's four,
      Level 14's five), the Siege Spire's boss silhouette (`intel/boss-siege-spire`), and the L3
      wave strip's marks: Level 13's three defend phases and the whiteout (new marks, done in M5 part
      G, step G6: `Intel.marks`, `PartGHudTest`), Level 14's root field (t=60–95) and the boss at 108
      ([hangar](../hangar/README.md))
- [x] The special row's **cooldown ring** (the [specials](../../player/specials/README.md)' *Cooldown
      specials and the HUD ring*: the clockwise fill, the seconds left, `READY` with its chime, the
      white arc while the Shield Overcharge lasts) and the **dash pip** beside the shield row (the
      [Evasive thrusters](../../player/systems/README.md)) — **later: Act 3** (M7 part A; round 36,
      Q.25e). Built in step A6 (`ShipPanel`): the ring of 28 dots round the 16 px icon, the row
      compact (the icon where the word `SPECIAL` stands) for a cooldown special or a name over 10
      characters (`ORBITAL LANCE`), `OVERCHARGE` for the Shield Overcharge; the dash pip a 16×6 cell
      right of the shield's label plate; with it the overcharge's white cores in the shield bar and
      a drone bay's hits strip under its weapons row

## Open questions

- Should the side panels show a subtle live element, such as a mini-radar or the pilot's
  heartbeat? Nice 90s flavour, but it competes for attention.

## Decisions

Entries dated before 2026-10-21 (M8's start): [decisions-archive.md](decisions-archive.md).

- 2026-10-21 (user, after playthroughs): "When a re-entry from left, rear or right is part of the
  normal flight pattern, do not show the warning. [...] Bosses must never trigger these warnings."
  The edge warnings and wave banners now come only from a wave's first entry from the left, right
  or rear (in Level 20's reversed scroll, the top) and a side ground unit's entry: no warning for
  a Wraith's re-entry (nor one after its kill), a chain's or a swarm's loop-back, the Revenant
  Walker's or the Dust Colossus's entries or the Siege Spire's launches (`Sortie.edgeWarnings`).
  Rook's rear bark went with the re-entry warnings; Rook's Trail on those units stays.
- 2026-10-22 (M8 part A, step A6; accepted in round 50 (2026-10-22)): a forbidden special greys and switches to the compact row with its reason word (`UNDER ICE` / `NO WATER`) in amber, `ORBITAL LANCE` cut to `ORBITAL` to fit; the row flashes once on the state change at a waterline and plays the limit chime (a press on a greyed special keeps the denied buzz r08-a); the Harpoon Torpedoes read `HARPOONS` so `NO WATER` fits beside the name; the escort box reads `BULWARK` (`LOST` when destroyed); the nanites' `+` pip.
- 2026-10-22 (user, [round 50](../../concept-rounds/round-50/README.md) closed): K.1: the special row's limits (`UNDER ICE` / `NO WATER`, the compact greyed row, `ORBITAL LANCE` cut to `ORBITAL`) accepted, the capture `chosen`; the limit chime is S.8 **b** (the soft glass fifth).
