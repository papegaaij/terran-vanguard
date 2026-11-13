---
title: Concept round 91 — M11 part D "Level 46 Graveyard of the Choir"
design: approved
implementation: n/a
art: chosen
depends-on: [../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir, ../../enemies/space/husk, ../../enemies/space/seed-casket, ../../enemies/space/leviathan, ../../audio/sfx, ../../ui/briefing, ../../ui/hangar, ../../tech/roadmap, ../round-90]
updated: 2026-11-13
---

# Concept round 91 — M11 part D "Level 46 Graveyard of the Choir"

## Summary

M11 part D is built up to its captures (branch `m11-act-7`, uncommitted): Level 46 "Graveyard of the Choir" with the
Husks, the seed casket, the warm hull with its core, the Helix wreck, Vorne's courier, the dying Leviathan, the dead-fleet
backdrop, the radio voiced and the sounds in place. This round asks, in this order: the level's ambience **bed** (a/b),
part D's **art to confirm**, the level **in play** from game captures, and the **important calls**, each with its
recommendation. The recommendation is pre-selected on every row; leave a row alone to keep it.

The in-play rows are game captures (step CD5). The starless patch is drawn by the game's code, so it is judged in play
(L.1), not as a picture.

Decided as trivia, not asked: the moved Wraith and Husk times; Vorne's courier has 20 HP on every difficulty; the Husks
all spin one way; at most 6 Husks at once, by the line layout; Level 46's `bounty_scale` is 0.135; a hulk was dropped
from the quiet's motion budget; the Husk's hull shape (of three) is picked by its serial; the
starless patch is drawn for Level 46 by its level number (no data key); the hangar's long mission line drops "BEFORE"; a
level name too long for the HUD is set in its small font; the seed casket has an intel portrait of its own.

**Closed 2026-11-06**: the answers are in the Answer and Outcome columns and the Decisions. S.1 a, F.1–F.6 accept, L.1–L.4 ok, C.1–C.7 accept (no notes).

## Review

The review page is built from this file by `build/round91-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 91`). The game captures are step CD5's `--shots` screenshots.

## Sound

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [The `dead-fleet` bed (QD-1)](../../audio/sfx/README.md) | **pick a or b**. Plays under the whole of Level 46 and alone through its 24 s quiet, when the music is out. It should sound like an empty, low space with the slow creak and tick of cold hulls, very sparse: long stretches of near nothing; no wind, nothing organic. **a** (recommended): a low drone with a slow creak every 8 s or so (stressed metal, ice, groaning metal). **b**: near silence, a very low hull tone with five far faint metal ticks and one slow far creak in a loop. | [a](../../audio/sfx/concept/dead-fleet-r91-a.ogg), [b](../../audio/sfx/concept/rejected/dead-fleet-r91-b.ogg) | not listened to by Claude: checked by level, peaks, event spacing and loop seams | **a** | a (`dead-fleet-r91-a`) chosen, already played as the stand-in bed; b rejected (moved to `concept/rejected/`, CREDITS.md paths updated) |

## Art to confirm

Part D's production art, already in the game: accept, or redo with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.1 | [The Husk](../../enemies/space/husk/README.md) | **accept or redo**. The dead Vrell hull in three shapes, whole and cracked, the tell's flare, the collapse, the shard and the orb, and the Husks beside the paler, flatter scenery hulks. | [frames](../../enemies/space/husk/concept/husk-final-r91-a.png), [motion](../../enemies/space/husk/concept/husk-final-r91-a.gif) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.2 | [The dead-fleet kit](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md) | **accept or redo**. Level 46's backdrop: the outer wrecks and the grey world small and far, the graveyard lanes, the Helix wreck, the quiet's densest torn hulks, the warm hull and the world filling the lower sky. | [sheet](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/concept/backdrop-final-r91-a.png), [time-lapse](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/concept/backdrop-final-r91-a.gif) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.3 | [The warm hull, the casket and the wrecks](../../enemies/space/seed-casket/README.md) | **accept or redo**. The warm hull with its core in four sinews (cut one by one, the core lifting out), the seed casket whole and broken, the Helix wreck and its hatch, and Vorne's courier as a tumbling chunk. | [sheet](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/concept/l46-props-final-r91-a.png), [motion](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/concept/l46-props-final-r91-a.gif) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.4 | [The Leviathan, dying](../../enemies/space/leviathan/README.md) | **accept or redo**. The Leviathan from Level 3 graded for Level 46: greyer, darker, its violet glows dimmed, the blowhole pulsing faintly. | [frames](../../enemies/space/leviathan/concept/leviathan-dying-final-r91-a.png), [motion](../../enemies/space/leviathan/concept/leviathan-dying-final-r91-a.gif) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.5 | [Level 46's briefing images](../../ui/briefing/README.md) | **accept or redo**. The fleet's long-range scan, some 300 hull echoes before a grey world with one patch of stars left blank, and Varga's thermal scan of a Husk beside the warm hull. | [images](../../ui/briefing/concept/briefing-images-final-r91-a.png) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.6 | [The Husk's intel portrait](../../ui/hangar/README.md) | **accept or redo**. The hangar's 30×30 picture of the Husk, nose down, cracked, its crystal lit. | [portrait](../../ui/hangar/concept/intel-final-r91-a.png) | — | **accept** | accepted: production art confirmed (rows chosen) |

## The levels in play

Game captures of Level 46 (step CD5, silent; the ship flies invulnerable).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| L.1 | [Level 46: the quiet](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md) | **ok or change**. The quiet with the Leviathan overhead and the starless patch (drawn by the game's code). | [the quiet](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/concept/quiet-capture-r91-a.gif) | the starless patch is faint at play size, a soft dark disc that shows best where the nebula is brightest (as the documents want: hinted, never shown); the Leviathan fills the upper field as it passes | **ok** | ok: capture rows chosen |
| L.2 | [Level 46: the cue's cracks](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md) | **ok or change**. The Husks cracking in turn on the cue. | [the cracks](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/concept/cracks-capture-r91-a.gif) | in this flight only two of the five Husks were still on the screen at t=144 (the others had drifted off), so the "in turn" shows as two cracks; the remnants' edge warnings cover the top of the field as they crack | **ok** | ok: capture rows chosen |
| L.3 | [Level 46: the core taken](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md) | **ok or change**. The warm hull's sinews cut, the core lifting out and the ship taking it. | [the core taken](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/concept/core-taken-capture-r91-a.gif) | the core is a dark violet sphere with a thin gold rim, not much warmer than the hull; its 2 s lift reads more as the core riding down with the hull than rising (as Level 45's horn); the clip holds fire after the cut so the beam does not cover it | **ok** | ok: capture rows chosen |
| L.4 | [Level 46: Husks and scenery hulks](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md) | **ok or change**. Husks (they crack and shoot) beside the scenery hulks (they do nothing), at 1:1: can you tell them apart? | [Husks and hulks, 1:1](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/concept/husk-vs-hulk-capture-r91-a.png) | the Husks (darker, rounder, crystal at the cracks) stand apart from the pale long hulks; a Husk's pale blue shards are faint over the palest hulk (middle crop) | **ok** | ok: capture rows chosen |

## Important calls

Calls the part D steps made where the documents left room, each built as recommended. Accept, or change with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [A denser Level 46](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md) | **accept or change**. Level 46 was made denser with mixed kinds of enemy (47 added waves). Recommended: accept. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.2 | [The rear Mote swarm](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md) | **accept or change**. A rear Mote swarm comes at t=145.5, just before the document's "no wave until 147". Recommended: accept (the alternative moves it to 147). | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.3 | [The Leviathan's bounty](../../enemies/space/leviathan/README.md) | **accept or change**. The Leviathan taken whole pays 130, not the document's 120. Recommended: accept. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.4 | [Level 46's small units](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md) | **accept or change**. Its small units die faster than the rule wants (0.65× of Level 10's life against 0.9×). Recommended: accept. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.5 | [The quiet's hold](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md) | **accept or change**. The quiet's fire hold starts at t=118, so Rook does not wake the first Husks. Recommended: accept. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.6 | [The Lamprey pincers](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md) | **accept or change**. The Lamprey pincers are two warned streams, not one sweep. Recommended: accept. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.7 | [The crack cue](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md) | **accept or change**. At t=144 the quiet ends with every Husk on the screen cracking in turn, but in the flights most of the quiet's five Husks have already drifted off by then, so the moment shows as two cracks (L.2). Recommended: accept the fix — slow the quiet's Husks and enter them later, so all five are still on the screen at the cue. Alternative: keep it as built. | [the cracks](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/concept/cracks-capture-r91-a.gif) | — | **accept** | fix accepted: the quiet's Husks slowed and entered later so all five are on the screen at t=144; done in step D6; recorded in Level 46's Decisions |

## Decisions

- 2026-11-06: round 91 opened (M11 part D built up to its captures; steps ART-D, D2, AUD-CD, VOX-CD): S.1 the
  `dead-fleet` bed a/b (QD-1), F.1–F.6 part D's art to confirm, L.1–L.4 the level in play (capture rows pending),
  C.1–C.6 the important calls; trivia in the summary.
- 2026-11-06: the capture rows L.1–L.4 filled in (step CD5): the quiet with the Leviathan and the patch, the cue's
  cracks, the core taken, the Husks against the scenery hulks at 1:1.
- 2026-11-06 (user, closed; page answers, no notes): **S.1 a** `dead-fleet-r91-a` chosen (already played as the stand-in bed); `dead-fleet-r91-b` rejected (moved to `concept/rejected/`, CREDITS.md paths updated). **F.1–F.6 accept**: the `*-final-r91-*` art rows chosen (Level 46 and its parts `art: chosen`). **L.1–L.4 ok**: the capture rows chosen (`cracks-capture-r91-a` stays chosen as the record of the call: it showed the fault). **C.1–C.7 accept**; C.7 (the crack cue): the fix accepted, the quiet's Husks slowed and entered later so all five are on the screen at t=144, done in step D6 (recorded in the parts' Decisions).
