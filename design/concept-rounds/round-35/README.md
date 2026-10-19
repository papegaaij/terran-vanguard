---
title: Concept round 35 — M6, the Acts 1–2 release
design: approved
implementation: n/a
art: chosen
depends-on: [../../ui, ../../ui/main-menu, ../../ui/debrief, ../../ui/credits, ../../ui/hangar, ../../ui/options, ../../systems/scoring, ../../systems/retry, ../../player/wingmen, ../../tech, ../../tech/release, ../../tech/roadmap, ../../art-direction/production, ../round-34]
updated: 2026-10-12
---

# Concept round 35 — M6, the Acts 1–2 release

**Closed 2026-10-12** (user): everything accepted as built. The changes to approved specs C1–C5
accepted (the SCORES menu item, the game over screen's differences from game-over-r08-a, when a
score is entered, the release's end after Act 2, Rook's guns table `act 2`); Q1 the end card's text,
Q2 = **a**, Q4, Q5 (Okafor's game-over line, text and voice), Q7, Q8 (unsigned), Q10, Q12, Q15 and
Q14 = **b** accepted; Q6 the glass pointer and Q9 the application icon approved as **final**; the
build choices A.1, B.1, C.1 and D.3 all *yes*; C.2 the release notes, C.3 the bundle sizes, D.1 the
placeholder audit, D.2 the bench and K.1 the capture accepted. Q13: **Apache-2.0** confirmed for the
game's own art, music and voices (recorded in CREDITS.md, [credits](../../ui/credits/README.md) and
[release](../../tech/release/README.md)). R.1–R.3 and R.5 noted (still blocked by round 34's
leftovers, the user's playthrough and the user's Mac). **Still open:** Q11 (the title and the
repository's name: kept for now, the user settles it next week) and R.4 (merge and tag: not possible
until the commits can be signed, next week). The parts are `design: approved`, the pointer and the
icon `art: final`; the outcome of each row is in its *Outcome* cell.

## Summary

The review round of M6 *Acts 1–2 release* ([roadmap](../../tech/roadmap/README.md)): part A mouse
support, part B the high scores and the release's end, part C the release plumbing and part D the
polish and close-out, built while the user was on vacation. Every default, pick and build choice
below was taken on the user's behalf (the gap check's recommendation) and is **pending the user's
review**; three of them were flagged risky (Q1 the end card's text, Q3 the main-menu change, Q5
Okafor's new line). It can be answered in the same sitting as [round 34](../round-34/README.md).

Open [index.html](index.html) in a browser (regenerate with
`python3 tools/concept/board.py 35 --subjects pointer,app-icon,m6-capture`: round 34 already used
the `r35` file names for Level 13, so the board keeps only this round's three subjects). The
questions keep their numbers from the M6 gap check (Q1–Q15); the build's own choices are
numbered per part (`A.1`, `B.1`, …), each answered *yes* (keep as built) or *no* (say what to
change). Read the two boxes first: what changed in **approved** specs, and the two questions only
the user can answer.

## Changes to approved specs

Approved parts of the design that M6 changed, checked against the approved text in git (`50d4dce`)
and the docs as they are now. Each **C** row is *accept* or *revert*. The documents involved are
back in `design: review` until this round closes.

| # | Change | Approved | Built (where) | Outcome |
|---|---|---|---|---|
| C1 | **A SCORES item in the main menu** (Q3 = a, flagged risky) | the menu: Continue, New game, Load game, Missions, Options, Credits, Quit | **SCORES** between Missions and Options opens the high scores: three tabs EASY / MEDIUM / HARD (Q / E, left / right or a click), opening on the most recent save's difficulty; the menu panel moved up 33 px so eight items end where seven did ([main menu](../../ui/main-menu/README.md#decisions)) | **accepted** (user) |
| C2 | **The game over screen differs from the chosen concept** (game-over-r08-a) | the concept: chrome red-steel `GAME OVER`, a `CREDITS SPENT` row, the placeholder transmission "Aegis One is down. We hold the line without you, Lancer.", the footer "The last save can be loaded from the main menu.", a `.` key in the letter grid | `GAME OVER` in the red heading font at 2× (not the chrome lettering); **SHIPS LOST** instead of CREDITS SPENT (the campaign does not count spending); Okafor's new voiced line (Q5); the footer "NO RETRIES LEFT. CONTINUE RETURNS TO THE HANGAR BEFORE THIS MISSION."; `SP` (space) instead of `.` in the grid ([retry](../../systems/retry/README.md#decisions), [debrief](../../ui/debrief/README.md#decisions)) | **accepted** (user) |
| C3 | **When a score is entered** (Q2 = a) | scoring: "It is filled in at game over (quitting a campaign) and at the campaign's end." | at the **hard game over** and at the **release's end** only; quitting a campaign to the menu enters nothing (a save can be continued), so on easy and medium the release's end is the only entry ([scoring](../../systems/scoring/README.md#high-scores)) | **accepted** (user) |
| C4 | **The release's end after Act 2** (Q1 = a) | credits: reachable "after the campaign ends" (later: Act 7); round 34's C13: Act 2's outro → autosave → the hangar reading `BEFORE L15 - NOT BUILT YET` | Act 2's outro → the end card → the name entry (when the score qualifies) → the credits roll → the main menu; the card writes the hangar's autosave, so Continue opens the hangar before Level 15, whose top bar reads `ACT III - RED DUST: COMING SOON` ([credits](../../ui/credits/README.md#at-the-releases-end-m6-part-b), [hangar](../../ui/hangar/README.md#decisions), [Act 2](../../campaign/act-2-homefront/README.md)) | **accepted** (user) |
| C5 | **Rook's guns table: Available `act 2`** (Q14 = b) | the hand-written table: Available `L08` for all four guns | the table is rendered from the data, whose `available` is `act 2` (he joins at L08) ([wingmen](../../player/wingmen/README.md#decisions)) | **accepted** (user) |

## Needs the user only

These two block the `v0.1` tag, not the work; nothing was changed for them.

| # | Question | Default (nothing changed) | Why only the user | Answer |
|---|---|---|---|---|
| Q11 | **The final title and the repository's name** | keep *Terran Vanguard* (the menu, the bundles, the zips) and the repository `conceptgame` (it is in the release URL) | the main menu's open question says "confirm before release"; renaming the repository changes the public URLs | keep both / keep title, rename repo / new title → **open**: keep both for now; the user settles it next week |
| Q13 | **The licence of the game's own art, music and voices** | Apache-2.0, as the repository's `LICENSE` already implies: the bundle README and the credits roll say "Terran Vanguard is open source under the Apache Licence 2.0. Third-party assets keep their own licences, listed in CREDITS.md." | legal: a licence for the art, music and voices is the owner's choice (e.g. CC-BY 4.0 for the assets instead) | Apache-2.0 confirmed / another licence → **Apache-2.0 confirmed** (user) for the game's own art, music and voices |

## In the game

`./gradlew :desktop:run --args="…"` with `--settings <file>` to keep the real settings apart. A debug
run never writes a save or a score (the screens say so):
- the mouse, the SCORES screen and the hangar: no arguments, then New game;
- the hard game over with Okafor's line: `--difficulty hard --level 8` and lose four times;
- the release's end: `--level 14 --invulnerable --loadout front=lance-laser:5,left=micro-missile-pod:5,right=micro-missile-pod:5`,
  kill the Spire, then confirm through the debrief, the act summary and the outro. A played
  campaign (from a save after Level 13) shows the end card without the debug line and enters the score.

## A — Mouse (part A)

Mouse support on every out-of-game screen and overlay ([ui](../../ui/README.md#mouse),
[hangar](../../ui/hangar/README.md), [options](../../ui/options/README.md)).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| Q6 | [The pointer](../../ui/README.md#concept-art) | **final art** (`tools/art/pointer.py` → `assets/ui/pointer.png`): Q6 = b, a glass arrow, 11×16 steel with an amber lit edge and a navy outline, hotspot at the tip, drawn at the integer pixel scale as a hardware cursor; the system arrow is the fallback (Q6 = a) | [pointer-r35-a.png](../../ui/concept/pointer-r35-a.png) | at 1× (a 960×540 window) it is small; the amber edge reads as a yellow stripe on the yellow menu highlight | glass pointer as final / system arrow / redo | **approved as final** (user): the glass pointer |
| Q7 | [Mouse in the overlays](../../ui/README.md#mouse) | **accept or change**: the pause and mission failed screens take the mouse; the pointer is hidden only while flying | the [capture](#captures) | a lost or paused flight shows the pointer at once; flying hides it even over the HUD panels | accept / change | **accepted** (user) |
| A.1 | [Part A build choices](../../ui/README.md#decisions) | **yes or no, each**: (a)–(g) below | — | (f) is untested by a player: no Acts 1–2 shop list is longer than the panel | all yes / some no | **all yes** (user) |

**Build choices (row A.1)**, each *yes* (keep) or *no* (say what):

- **(a)** Hover selects only while the pointer moves: the last device used owns the selection, so
  a resting pointer does not fight the keys.
- **(b)** A click on an item selects and confirms it at once; a click on a difficulty card starts
  the campaign.
- **(c)** The right button is Back everywhere; in the briefing it asks to quit, as Esc does (there
  is no page back).
- **(d)** In the hangar a slot (a mount's callout, Rook's callout, a module tile) is chosen by a
  click, not by hover, so moving across the schematic does not switch the shop list; shop rows,
  choice chips and commands follow hover.
- **(e)** A click that gives the window the focus is dropped for 0.1 s after the focus is gained.
- **(f)** The wheel scrolls the shop list (in Acts 1–2 the list always fits, so it never moves).
- **(g)** The system arrow is used when the custom cursor cannot be made.

## B — Scores and the release's end (part B)

The high scores ([scoring](../../systems/scoring/README.md#high-scores)), the game over screen
([retry](../../systems/retry/README.md)), the release's end ([credits](../../ui/credits/README.md))
and the SCORES screen (C1).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| Q1 | [The release's end](../../ui/credits/README.md#at-the-releases-end-m6-part-b) | **text review** (flagged risky, new player-facing text): the end card after Act 2's outro, then the name entry, the credits roll and the main menu (Q1 = a; b would go from the outro straight to the credits). The card's text in full: | [m6-capture-r35-a.png](../../ui/concept/m6-capture-r35-a.png) (screens 6–8) | "MARS." stands alone; "CONTINUE IN THE MAIN MENU RETURNS TO THE HANGAR AT THE END OF ACT II." is long and dim at 1× | accept / change the text / b: no end card | **accepted** (user): the end card's text as built |
| Q5 | [Okafor's last transmission](../../systems/retry/README.md#decisions) | **text and voice review** (flagged risky, new story text): at the hard game over, 2.5 s after the game over cue, voiced with the radio filter (grim): *"Lancer is down. Aegis Wing, close ranks and hold the line. Okafor out."* The gap check's draft was "Lancer is down. Aegis Wing fights on without you. Okafor out."; the concept's placeholder "Aegis One is down. We hold the line without you, Lancer." | [the take](../../../assets/voice/okafor/111125288031.ogg); [m6-capture-r35-a.png](../../ui/concept/m6-capture-r35-a.png) (screen 5) | the seed was pinned after eight takes because Whisper heard "age is wing" for "Aegis Wing" in the default seed; not listened to by Claude | accept / change the line / redo the voice / b: leave it out | **accepted** (user): the line and its voice |
| Q4 | [The name's form](../../systems/scoring/README.md#high-scores) | **accept or change**: A–Z, 0–9, space and `-`, 3–10 characters without a space at either end, upper case in the 10×20 font; the letter grid (two rows of 20 keys, DEL and END) and typing (lower case raised), the mouse on the keys; the last name entered is offered | [m6-capture-r35-a.png](../../ui/concept/m6-capture-r35-a.png) (screen 7) | — | accept / change | **accepted** (user) |
| Q2 | [When the name is entered](../../systems/scoring/README.md#high-scores) | **pick a or b** (a built, see C3): **a** at the hard game over and at the release's end, on every difficulty; **b** also when the player quits a campaign to the menu | — | on easy and medium a player who never finishes Act 2 never enters a score | a / b | **a** (user), as built |
| B.1 | [Part B build choices](../../systems/scoring/README.md#decisions) | **yes or no, each**: (a)–(j) below | — | (j) makes the menu's corner busier in the capture's title screen | all yes / some no | **all yes** (user) |

**The end card (row Q1)**, as built (`ReleaseEndScreen`):

> **ACTS I-II COMPLETE**
> MEDIUM - MISSIONS 01 - 14 FLOWN
>
> CAMPAIGN: SCORE · ENEMIES DESTROYED · DATA CORES FOUND · A+ GRADES · SHIPS LOST · PLAYTIME
>
> **TO BE CONTINUED**
> ACT III - RED DUST. MARS. COMING SOON.
> CONTINUE IN THE MAIN MENU RETURNS TO THE HANGAR AT THE END OF ACT II.
>
> AUTOSAVED · hint `ENTER HIGH SCORES` (or `ENTER CREDITS` when the score does not qualify)

The name entry that follows is headed `NEW HIGH SCORE` / `MEDIUM - SCORE 187 920 - RANK 1`; the
hangar before Level 15 reads `ACT III - RED DUST: COMING SOON`. The title theme plays from the card
through the credits.

**Build choices (row B.1)**, each *yes* (keep) or *no* (say what):

- **(a)** A score of 0 is never entered.
- **(b)** Esc (B, the right button) deletes a character; the entry cannot be skipped.
- **(c)** The last name is offered with the cursor on END, so a confirm keeps it.
- **(d)** An equal score ranks below the entries already there.
- **(e)** A damaged `scores.yaml` is read as an empty table and kept as `scores.yaml.bad` when the
  next score is written.
- **(f)** The SCORES screen opens on the most recent save's difficulty (medium without one); Esc,
  Enter or a click beside the tabs goes back.
- **(g)** The release's end plays the title theme from the end card through the credits.
- **(h)** The end card writes the hangar's autosave (never in a debug run), so Continue opens the
  hangar before Level 15.
- **(i)** The game over's footer: "NO RETRIES LEFT. CONTINUE RETURNS TO THE HANGAR BEFORE THIS
  MISSION.", and `GAME OVER` in the red heading font (C2).
- **(j)** `v0.1` small and dim in the bottom-left corner of the title and the menu (`dev` in an IDE
  run), as the approved mock shows.

## C — Release (part C)

The version, the icon, the bundles and the release notes ([release](../../tech/release/README.md)).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| Q9 | [The app icon](../../tech/release/README.md#application-icon) | **final art** (`tools/art/app_icon.py`): the Stormhawk from above, rendered from its production model, on a dark navy tile with the title logo's steel-blue trim, one 1024 px master drawn at 16–1024 px (window icons, `.ico`, `.icns`) | [app-icon-r35-a.png](../../tech/release/concept/app-icon-r35-a.png) | at 16 px the ship is a small light cross and the trim takes a third of the tile | approve as final / redo | **approved as final** (user) |
| Q8 | [Signing](../../tech/release/README.md#signing) | **accept or change**: ship unsigned, with the SmartScreen and Gatekeeper steps in the release notes and the bundle README (b: an Apple Developer ID and a Windows certificate, which cost money every year) | — | whether the zipped, unsigned arm64 app opens after the `xattr` step (or needs `codesign --sign -`) is unverified: it needs the user's Mac | unsigned / buy certificates | **unsigned** (user); R.5 open |
| Q10 | [The version](../../tech/release/README.md#version) | **accept or change**: `0.1.0` in Gradle, `v0.1` on the menu, the zips `terran-vanguard-0.1.0-<target>.zip`, the tag `v0.1` (the CI release job fails when tag and version differ) | — | — | accept / change | **accepted** (user) |
| Q15 | [What the bundles carry](../../tech/release/README.md#bundles) | **accept or change**: `README.txt` (start, unsigned steps, controls incl. the mouse, where settings, saves, scores and screenshots live, Acts 1–2 of 7), `LICENSE.txt`, `CREDITS.md`; on macOS beside the app | — | — | accept / change | **accepted** (user) |
| C.1 | [Part C build choices](../../tech/release/README.md#decisions) | **yes or no, each**: (a)–(b) below | — | — | all yes / some no | **all yes** (user) |
| C.2 | [The release notes](../../tech/release/release-notes.md) | **text review**: the template `gh release create --notes-file` uses (the CI fills in the version and tag); quoted under the row on the review page | — | "keyboard or gamepad" under Requirements does not mention the mouse in the menus | accept / change | **accepted** (user) |
| C.3 | [Bundle sizes](../../tech/release/README.md#bundles) | **accept or change**: zips 148.6–152.2 MB (unpacked 185–198 MB; the fat jar is 124 MB of each); the Linux bundle with no JDK on the `PATH` ran at 322 fps with a 1.74 GB peak resident set (software renderer) | — | the atlases and music dominate; no size budget was ever set | accept / change | **accepted** (user) |

**Build choices (row C.1)**, each *yes* (keep) or *no* (say what):

- **(a)** Each zip unpacks into a folder of its own, `terran-vanguard-0.1.0/`, instead of spilling
  the game's files into the folder it is unpacked in.
- **(b)** The licence ships as `LICENSE.txt` (the repository's `LICENSE`), so it opens with a double
  click on Windows.

## D — Polish (part D)

The close-out ([roadmap](../../tech/roadmap/README.md)).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| Q12 | [The polish pass](../../tech/roadmap/README.md#decisions) | **accept or change**: the gap check's list: dev text a player can see (the hangar's not-built line, the intel panel's milestone), the placeholder audit as a test, the sync renderers (Q14), the docs reconciled, a per-level bench; the attract-mode demo stays out of v0.1 | — | — | accept / change | **accepted** (user) |
| Q14 | [The rendered tables](../../player/wingmen/README.md#decisions) | **pick a, b or c** (b built): **a** Rook's guns and barks only; **b** also the act outro's pages (`act-outro-pages`); **c** also the specials, allies and economy tables (would become Act 3 tooling) | — | the guns' Available cell changed (C5) | a / b / c | **b** (user), as built |
| D.1 | [The placeholder audit](../../art-direction/production/README.md#decisions) | **accept or change** (`PlaceholderAuditTest`): no shipped image of Levels 01–14 is a placeholder; 48 shipped files carry no production mark: 27 synthesized concept sounds (pickups, UI, launch rail, edge warning, the Leviathan's cry, save, mine arm, Driftjelly, the Choir's sting) treated as final, 2 act-title letterings, 19 Level 12–14 sounds waiting for round 34 | — | the 27 synthesized sounds were never given a `SOURCE` comment: final by the SFX rule, but the audit only knows them by a list | accept / change | **accepted** (user) |
| D.2 | [The per-level bench](../../tech/README.md#per-level-bench-m6-part-d) | **accept or change** (report only): Levels 01–14 on the software renderer at 250–367 fps, the longest frame 33–71 ms once per run, the live heap 40–90 MiB, the longest pause 1.0 ms with a 1 GiB heap, the resident set 1.1–1.7 GiB; no fix follows | — | the long frame is not located (likely the level start); llvmpipe keeps the textures in the process, so the RSS overstates a GPU machine | accept / change | **accepted** (user) |
| D.3 | [Part D build choices](../../tech/roadmap/README.md#decisions) | **yes or no, each**: (a)–(d) below | — | — | all yes / some no | **all yes** (user) |

**Build choices (row D.3)**, each *yes* (keep) or *no* (say what):

- **(a)** The hangar intel's not-built line reads "FOLLOWS IN A LATER RELEASE." (was "FOLLOWS IN A
  LATER BUILD (M4).").
- **(b)** The SCORES screen's line reads "A SCORE IS ENTERED AT A GAME OVER (HARD) AND AT THE END OF
  THE CAMPAIGN." (in v0.1 the end of the campaign is the release's end).
- **(c)** In the name entry the End key jumps to the END cell.
- **(d)** The synthesized concept sounds without a `SOURCE` comment count as final (D.1).

## Captures

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| K.1 | [The release build](../../ui/README.md#concept-art) | **game capture** (part D, the `installDist` build, sound from the game's mix): the title with `v0.1` and the pointer → the menu → SCORES (tabs clicked) → New game → the briefing → the hangar by mouse (slots, items, an upgrade bought, undo, Launch hovered); a hard Level 08 lost a fourth time → the game over with Okafor's line; Level 14 won → debrief → act summary → outro → end card → name entry (`LANCER`) → credits → menu. 6.6 min with sound and a sheet of eight screens | [m6-capture-r35-a.png](../../ui/concept/m6-capture-r35-a.png), [.mp4](../../ui/concept/m6-capture-r35-a.mp4) | debug runs, so `AUTOSAVE OFF - DEBUG RUN` and `SCORES OFF - DEBUG RUN: NOT SAVED.` are on the screen; the game over run scored 0, so it shows "NO SCORES YET." and no name entry at the game over; not listened to by Claude | accept / change | **accepted** (user) |

## After round 34

Blocked until round 34 closes; listed so nothing is lost. *Noted*, or a note.

| # | Item | Blocked by | Then |
|---|---|---|---|
| R.1 | The credits roll regenerated (28 attributed items today) | round 34's sound picks (CC-BY files may come or go) | `./gradlew :pipeline:credits` rewrites `assets/ui/credits.txt` from CREDITS.md ([credits](../../ui/credits/README.md)); *noted* (user) |
| R.2 | The placeholder audit's 19 Level 12–14 sounds | round 34's sound picks | the known list in `PlaceholderAuditTest` shrinks; *noted* (user) |
| R.3 | The user's playthrough fixes of Act 2 | round 34's P9 | fixed before the tag; *noted* (user) |
| R.4 | Merge and tag | rounds 34 and 35, the user's playthrough | M5 into `main`, then M6, a green CI on three OSes, then `v0.1` on `main` (the user; Q11 and Q13 first); **open**: not possible until the commits can be signed (the user, next week) |
| R.5 | The unsigned arm64 app opened on a real Mac | the user's Mac | Q8's notes confirmed or `codesign --sign -` added; *noted* (user) |

## Decisions

- 2026-10-12: Round opened (M6 built on the user's behalf while on vacation, defaults of the gap
  check applied; every row pending the user's review). The board uses `board.py --subjects`
  because round 34 already holds files named `r35`.
- 2026-10-12 (user): closed as summarised at the top: everything accepted as built, the pointer
  and the application icon final, Q13 Apache-2.0; Q11 and R.4 (merge and tag) stay open until
  next week. The board regenerated with the new statuses (no file was rejected).
