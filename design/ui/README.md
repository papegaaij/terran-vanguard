---
title: User interface
design: approved
implementation: done
art: chosen
depends-on: [../art-direction, ../systems]
updated: 2026-10-18
---

# User interface

## Summary

Every screen outside the action and the HUD around it: main menu, briefing, hangar, in-level
HUD, pause, debrief, and the controls. Two styles (user decision, round 06): **out-of-game
screens** (menus, difficulty, load game, hangar/shop, briefing, debrief) use translucent glass
panels with thin metal trim over a pre-rendered scene, as in main menu A; the **in-game HUD**
alone uses bevelled metal panels with green/amber phosphor readouts. Both share portrait
frames and chunky bitmap fonts.

## Contents

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [main-menu](main-menu/README.md) | Title screen, continue/new/load, options, credits, quit | approved | done | final |
| [briefing](briefing/README.md) | Story briefing before each level: portraits, typed text, objectives | approved | done | chosen |
| [hangar](hangar/README.md) | Ship configurator: intel, shop, loadout, repair, save, launch | approved | in-progress | chosen |
| [hud](hud/README.md) | In-level side panels: status, weapons, special, radio chatter, progress | approved | in-progress | final |
| [pause](pause/README.md) | Pause menu during a level | approved | done | final |
| [debrief](debrief/README.md) | Level complete: kills, credits, bonuses, grade | approved | done | final |
| [mission-select](mission-select/README.md) | Replay a flown mission: the act-by-act list with best grades, later missions locked | review | done | n/a |
| [controls](controls/README.md) | Keyboard and gamepad mapping, remapping, auto-fire | approved | done | n/a |
| [options](options/README.md) | Video, audio, controls (remapping, auto-fire) and gameplay settings | approved | done | final |
| [credits](credits/README.md) | Scrolling credits including the CC-BY attributions | approved | done | final |

## Design

### Screen flow

```
Title ─► Main menu ─┬─ Continue ─────────────► Briefing ─► Hangar ─► Level
                    ├─ New game ─► Difficulty ─► Intro briefing ─► Hangar ─► Level
                    ├─ Load game ─► Slot list ─► Briefing ─► Hangar ─► Level
                    ├─ Missions ─► Mission select ─► Briefing ─► Hangar (replay) ─► Level (replay) ─► Debrief ─► Mission select
                    ├─ Scores (the high scores per difficulty; M6 part B)
                    ├─ Options
                    ├─ Credits
                    └─ Quit
Hangar ─► Launch ─► Level ─► Debrief ─► Briefing ─► Hangar ─► …
Act's last level ─► Debrief + act summary ─► Act outro ─► Next act's intro (title card, act briefing) ─► Briefing ─► Hangar
Last built act (Act 2) ─► Act outro ─► End card ─► Name entry (top 10) ─► Credits ─► Main menu
                                         (Continue: the hangar before L15, "ACT III - RED DUST: COMING SOON")
Briefing ─► Back ─► Quit to main menu? ─► Main menu                 (the campaign kept)
Level ─► Pause ─► Resume / Restart / Options / Abort to hangar / Quit
Level ─► Mission failed ─► Retry / Back to hangar / Quit      (hard, no retry left: Game over + name entry ─► Main menu)
```

**Act end** (M4 part G, user decision D2): after an act's last level the debrief adds the act
summary ([debrief](debrief/README.md)), then the act-end outro plays on the briefing screen
(its pages as in the act document, e.g. [Act 1](../campaign/act-1-first-contact/README.md#act-intro-and-outro)),
then the next act's intro opens it as at a new game (its title card and act briefing), then that
level's mission briefing and the hangar. The autosave happens when the hangar opens, as always.
A replay from mission select shows neither the act summary nor the outro.

**Every mission starts with its briefing** (user decision 2026-10-18, from the Act 2 playtest):
briefing, then hangar, then level, on every path. Continue and Load game open the next level's
mission briefing (an act intro that is still due, the `act-intro:N` story flag of
[saves](../systems/saves/README.md), plays first, as before), then the hangar, which does not
autosave right after a load (except after such an act intro, to keep its flag). A mission select
replay opens that level's mission briefing (without its act's intro) and then the replay hangar
on the real campaign ([mission select](mission-select/README.md)). Only the debug starts
(`--level`, `--loadout` …) go straight into the level.

### Shared UI rules

- Internal resolution 960×540, integer scaled where the display allows; the
  [art direction](../art-direction/README.md) owns the details. Bitmap fonts: 8×12 for labels,
  radio subtitles and control prompts, 10×20 for body text, 20×30 for headings. A HUD well's
  196 px of text hold 24 label characters (radio lines are written for 22) or 19 body characters.
- Every screen is fully usable with keyboard or gamepad; the mouse is optional (hangar
  benefits from it).
- Confirm = Enter / A, Back = Esc / B everywhere.
- Panel frames, buttons and bars form one shared UI kit, designed once and reused.

### Mouse

The mouse works on every out-of-game screen and overlay (M6 part A); keyboard and gamepad stay
the primary input and are unchanged, and so are the key-hint plates.

- **Pointer**: the kit's glass arrow (`ui/pointer.png`, [tools/art/pointer.py](../../tools/art/README.md),
  16×16, hotspot the tip) as a hardware cursor at the window's integer scale; the system arrow
  where the art is missing or the platform refuses a custom cursor. The window is mapped into the
  960×540 screen by the inverse of the letterbox, in logical window units, so HiDPI windows (macOS
  Retina, Windows scaling) and the sharp-bilinear scaling map right; a point on a letterbox bar
  lies outside every screen.
- **Hover selects, a click confirms.** Hover only moves the selection while the pointer moves, so
  the last device used owns the selection and a resting pointer never fights the keys. A click
  selects and confirms in one; a click on a disabled item or outside every region does nothing.
  The click that only gives the window the focus confirms nothing (one tenth of a second after the
  focus is gained).
- **Back**: the right button and the mouse's back button act as Esc / B on every screen (back,
  resume, close, cancel a capture, ask to quit where Esc asks).
- **Wheel**: scrolls the lists that scroll — the mission select and the hangar's shop list (none
  of the Acts 1–2 slots has more than its eight rows yet) — and scrubs the credits roll (36 px a
  notch).
- **Per screen**: the title screen goes on with a click anywhere; the menus (main menu, pause,
  mission failed, load / save slots, mission select) and every confirm dialog take hover and
  clicks on their items and buttons; the difficulty cards are hovered and a click starts the
  campaign; a click turns the briefing's and the act outro's pages as Enter does (the title card
  too), and goes on in the debrief, the act summary and the game over screen; the credits take a
  click once the roll has stopped. The [hangar](hangar/README.md) and the [options](options/README.md)
  have their own regions (see there).
- **In flight** the pointer is hidden by a transparent cursor (not caught, so it can still leave a
  window) and the mouse does nothing; it shows again in the pause menu, on the mission failed
  screen and from the level's won outro on.

## Concept art

The HUD and logo concepts for round 01 live in [hud](hud/README.md) and
[main-menu](main-menu/README.md).

## Concept art

Concept [round 08](../concept-rounds/round-08/README.md) — the shared UI kit; generator `tools/concept/ui_r08.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/ui-kit-r08-a.png](concept/ui-kit-r08-a.png) | Shared UI kit: glass widgets (panels, menu states, chips, sliders, toggles, tabs, dialogs), metal HUD widgets, and bitmap font specimens 8×12 / 10×20 / 20×30 with the full character set | chosen |

Production art, UI batch part U2 (for concept round 13, opened by part U3): the glass kit rendered by [tools/art/ui_kit.py](../../tools/art/README.md) into `assets/sprites/ui/` and drawn by `vanguard.game.ui.Glass`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/ui-kit-final-r13-a.png](concept/ui-kit-final-r13-a.png) | Review sheet: the widgets as the screens draw them over the darkened title scene (panels with trim and corner tabs, header rules, list rows with the selection and cursor, menu items, chips on / off / locked, NEW tag, slider, toggle, tabs, pips, chevron, the confirm dialog with its holo brackets, callouts, amber card frame, inset, key-hint plate), a 2× detail and every piece with its size | chosen |
| [concept/ui-kit-capture-final-r13-a.png](concept/ui-kit-capture-final-r13-a.png) | Game capture: the quit confirmation over the main menu | chosen |

Production art, M6 part A (for concept round 35): the glass mouse pointer rendered by
[tools/art/pointer.py](../../tools/art/README.md) into `assets/ui/pointer.png`. Prompts:
[concept/prompts.md](concept/prompts.md#pointer-r35-a).

| File | What | Status |
|---|---|---|
| [concept/pointer-r35-a.png](concept/pointer-r35-a.png) | Review sheet: the glass pointer (11×16 steel arrow, amber lit edge, navy outline, hotspot the tip) at 1×, 4× and 8× on a checker, then at 1×, 2× and 3× over a glass menu item, a bright and a dark backdrop | final |

Game capture, M6 part D (for concept round 35): the release build's out-of-game flow. Prompts:
[concept/prompts.md](concept/prompts.md#m6-capture-r35-a).

| File | What | Status |
|---|---|---|
| [concept/m6-capture-r35-a.png](concept/m6-capture-r35-a.png) | Sheet of eight screens from the capture: the title with `v0.1` and the glass pointer, the main menu with SCORES, the SCORES tabs, the hangar driven by mouse (an upgrade bought by click), the hard game over with Okafor's last transmission, the release's end card, the name entry and the credits roll | chosen |
| [concept/m6-capture-r35-a.mp4](concept/m6-capture-r35-a.mp4) | The capture with the game's sound (6.5 min): title → menu → SCORES (tabs clicked) → New game → difficulty → briefing, the hangar by mouse (slots, items, upgrade, undo, Launch hovered), a hard Level 08 lost a fourth time → game over with Okafor's voiced line, Level 14 won → debrief → act summary → outro → end card → name entry → credits → main menu | chosen |

Production art, UI batch part U3 (concept round 13): the bitmap fonts rendered by [tools/art/fonts.py](../../tools/art/README.md) into `assets/fonts/`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/fonts-final-r13-a.png](concept/fonts-final-r13-a.png) | Review sheet: the label 8×12, body 10×20 and heading 20×30 fonts, each glyph of the 127-character set at 2× on its cell grid with the baseline marked, and sample lines from the game's texts at 1× | chosen |

## Implementation

- [x] Screen/state machine for the flow above
- [x] Every mission starts briefing → hangar → level (user decision 2026-10-18): Continue, Load game and the mission select replay open the level's briefing first (`CampaignRoute.onContinue` / `beforeReplay` / `missionBriefing`, `HangarScreen.onContinue` / `replay`; tests `MissionFlowTest`, `CampaignRouteTest`)
- [x] Shared UI kit: panels, buttons, lists, bars, portrait frame, bitmap fonts (glass pieces: `tools/art/ui_kit.py`; fonts: `tools/art/fonts.py`)
- [x] Keyboard and gamepad navigation on every screen (every screen of `vanguard.game.screen` reads the shared menu input, `services.menu`: menus, briefing, hangar, options, pause, debrief with the act summary and outro, mission select, mission failed, game over, credits)
- [x] Mouse support on every out-of-game screen (hover selects, click confirms, wheel scrolls lists, right-click or a back button goes back), with the hangar's panels and the options sliders and remapping usable by mouse; the in-level game stays keyboard/gamepad only, and the pointer is hidden in flight — M6 part A (`vanguard.game.input.Pointer`, `GdxPointer`, `display.PointerCursor`; see *Mouse* above), accepted by the user in concept round 35

## Open questions

- None open.

## Decisions

- 2026-09-30: Controls are a UI part (`controls/`), since remapping lives in the options screen.
- 2026-09-30: Converted to the 960×540 baseline (was 640×360).
- 2026-09-30: Fonts were not scaled ×1.5 (that would give 12×12 / 12×24 / 24×24): body text uses 10×20 so the wider panels fit more text per line.
- 2026-10-01: Concept round 06: out-of-game screens use the glass-over-scene style of main menu A; bevelled metal is reserved for the in-game HUD.
- 2026-10-01: Concept round 08: accepted.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../reviews/acts-1-2/README.md).
- 2026-10-02: M3 part A. Screen state machine (`vanguard.game.screen.ScreenFlow`): a stack of
  screens of which only the top one is updated and drawn; a screen *opens* another over itself
  (options over the main menu, pause over the level, options over pause) and gets it back
  unchanged with its music when that one goes back, or *replaces* all of them (difficulty → level,
  level → debrief, quit to main menu). Briefing, hangar and the load-game slot list plug in as
  screens in part B; until then New game goes difficulty → Level 01 and the debrief returns to the
  main menu. UI kit (`vanguard.game.ui`): `Glass` draws the chosen kit's glass panel, header,
  menu items (selected / disabled), chips, slider, rank chevron, key hints and the confirm dialog
  with the generator's colours; `Menu` and `Dialog` are the navigation models. The portrait frame
  comes with the briefing (part B), so the kit item stays open. Bitmap fonts: the kit's proposed
  production fonts (DejaVu Sans Mono Bold rasterised 1-bit into 8×12, 10×20 and 20×30 cells by
  `ui_r08.bitmap_glyphs`) are written as BMFont files into `assets/fonts/` by
  `tools/concept/ui_assets.py`; they replace libGDX's built-in font in the HUD, the debrief and
  every menu. Text has the kit's one-pixel shadow on glass. Keyboard and gamepad navigation on
  every screen built so far (menus use fixed keys: arrows / D-pad / left stick, Enter / A,
  Esc / B / Back, Q / E and the bumpers for tabs, with key repeat); the item stays open until the
  part B screens have it too. Mouse: not supported yet (optional per the rules above).
- 2026-10-02: Characters per line (user decision): radio subtitles and control prompts use the
  8×12 font (22+ characters in the 196 px well); body text at 10×20 fits about 19 characters
  there. The rule "a 240 px side panel fits about 22 body characters" is corrected.
- 2026-10-02: Fonts (user decision): permissive font licences (Bitstream Vera, SIL OFL) are allowed
  as a written exception to the CC0/CC-BY rule, for fonts only, recorded in CREDITS.md and named
  on the credits screen (CLAUDE.md). The DejaVu Sans Mono Bold bitmap fonts stay.
- 2026-10-02: M3 part B1: the campaign screens are in the flow: New game → difficulty → intro
  briefing (act title card, act briefing, L01 briefing) → hangar placeholder → Level 01 → debrief →
  next level's briefing, or the hangar while the next level is not built; Continue and Load game
  open the hangar with a save. A destroyed ship opens the mission failed screen over the level
  (Retry / Back to hangar / Quit), or the game over screen on hard with no retry left; both are now
  drawn in the flow above (from [retry](../systems/retry/README.md)), as is Pause's Abort to hangar.
- 2026-10-02: Mouse support for the out-of-game screens is planned for M6 (user decision after playing M3); keyboard and gamepad stay the primary input.
- 2026-10-02: Production art, UI batch part U2: the glass kit is production art
  (`tools/art/ui_kit.py`, `assets/sprites/ui/`, on the shared sprite pages) and `Glass` draws it
  instead of flat fills, with the same layouts and fonts: the glass body (stepped sheen along the
  top, drawn at the panel's opacity) under a separate trim nine-patch (a 2 px rounded metal rail,
  lit top-left, with the four corner tabs), an amber trim for the selected difficulty card, the
  dialog's trim with cyan holographic corner brackets, the recessed inset, the header rule with its
  end cap, the fading amber selection bar (dithered along its length only, so stretching keeps it
  clean), faceted cursors per font, chips and tabs as pixel bevels, the slider's knob, a lit bar
  cell tinted per use, list rows, the key-hint plate, a tag, scroll markers, the trait diamond, the
  rank chevron and the hangar's holographic callouts. Fills remain only for lines, grids, ticks and
  the screen's dimming. The portrait frames are the trim nine-patch, so the shared-kit item is
  ticked. Review files proposed for round 13; `art` stays `chosen`.
- 2026-10-02: Production art, UI batch part U3: the bitmap fonts move from the placeholder script
  `tools/concept/ui_assets.py` to `tools/art/fonts.py` (`Source` chunk on every page). Same font,
  sizes, cells, baselines (label 9, body 15, heading 24) and line heights, so no layout moves: the
  114 glyphs the fonts had are pixel for pixel the same (FreeType's hinted 1-bit raster was already
  clean, and no glyph was clipped by its cell). Each glyph is now placed on the cell's baseline
  explicitly and may overflow the cell (accents over capitals). The set grows to 127 characters:
  the en dash, the typographic quotes ‘ ’ “ ”, → and ≈ (used by the data and the code but missing)
  and the upper-case É È Ë Ü Ö Ä (the screens upper-case their texts). `fonts.py --check` scans
  every data file and the game's and content's string literals (also upper-cased) and fails on a
  character without a glyph or a capital, digit or lower-case letter off the shared rows. Review
  sheet proposed for round 13; `art` stays `chosen`.
- 2026-10-02: Concept round 13 closed (user decision): the glass UI kit (`tools/art/ui_kit.py`) and the three bitmap fonts (`tools/art/fonts.py`) approved as **final**; this doc's `art` stays `chosen`, since not every screen under it is final (hangar, briefing, credits).
- 2026-10-05: M4 part G (user decision D2): the act-end branch of the screen flow: the act's last
  debrief with the act summary, the act outro, then the next act's intro; until Act 2 is built the
  outro leads to the hangar before L08, which shows it as not built yet. No act summary and no
  outro on a replay from mission select (the summary's part of that is a main-agent default).
- 2026-10-05: M4 part H docs reconciliation: keyboard and gamepad navigation reaches every screen
  of the build; ticked. Only mouse support (M6) is open, so the document is `done` for M4.
- 2026-10-07: The mouse pointer stays visible in full screen during a level (seen in the round-30
  capture, confirmed by the user); hiding it in flight is part of the M6 mouse support work (user
  decision).
- 2026-10-12: M6 part A, mouse support (pending the user's review in concept round 35), with the
  defaults of the M6 plan: Q6 = b, the custom glass pointer with the system arrow as the fallback
  (art proposed, `pointer-r35-a`); Q7, the pause and mission failed screens take the mouse and the
  pointer is hidden only while flying. Main-agent defaults for the review: hover selects only
  while the pointer moves (the last device owns the selection); a click on an item selects and
  confirms at once (also a difficulty card: it starts the campaign); the right button is Back
  everywhere, also in the briefing, where it asks to quit as Esc does (there is no page back); in
  the hangar a slot's callout or tile is selected by a click, not by hover (hovering the schematic
  would switch the shop list under the pointer), while shop rows, choice chips and commands follow
  hover; a click that gives the window the focus is dropped for 0.1 s after the focus is gained
  (a window that never reports the focus, X11 without a window manager, takes every click).
  Smoke run on Xvfb driven by XTest: title → New game → difficulty card → the briefing's pages by
  clicks → hangar (the Pulse Cannon's upgrade bought by its chip) → Launch; the cursor read back
  as the 32×32 glass arrow at scale 2 on the menus and the pause menu and as an empty 16×16 cursor
  in flight. Headless tests: `PointerTest`, `ViewportTest` (scales 1–4, letterbox, HiDPI,
  sharp-bilinear), `MenuTest`, `DialogTest`, `MouseRegionsTest` (every menu's rows, the
  difficulty cards, the Options tabs, rows, chips, sliders, sound tests and binding cells, the
  remap capture), `HangarPointerTest`.
- 2026-10-12: Back to `design: review` until [concept round 35](../concept-rounds/round-35/README.md) closes (Q6, Q7 and the build choices A.1; the pointer art proposed).
- 2026-10-12 (user, round 35): accepted as built: Q6, the glass pointer (`pointer-r35-a`,
  `tools/art/pointer.py`) approved as **final**; Q7 (the pause and mission failed screens take the
  mouse, the pointer hidden only while flying); the build choices A.1 (a)–(g); the release build's
  capture (K.1, `m6-capture-r35-a`) accepted. `design: approved`; `art` back to `chosen`, since not
  every screen under it is final (the hangar and the briefing are `chosen`).
- 2026-10-18 (user, Act 2 playtest): every mission starts with its briefing, then the hangar, then
  the level: Continue and Load game open the next level's briefing (an act intro that is due first)
  instead of the hangar, and a mission select replay opens the level's briefing and the replay
  hangar instead of the level. Screen flow updated; details in
  [mission select](mission-select/README.md#decisions).
