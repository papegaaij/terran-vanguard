---
title: Concept round 90 — M11 part C "Level 45 Hive Reef"
design: review
implementation: n/a
art: proposed
depends-on: [../../campaign/act-7-beyond-the-gate/level-45-hive-reef, ../../enemies/ground/spawning-organ, ../../world/vrell-space, ../../audio/sfx, ../../ui/briefing, ../../tech/roadmap, ../round-89]
updated: 2026-11-06
---

# Concept round 90 — M11 part C "Level 45 Hive Reef"

## Summary

M11 part C is built up to its captures (branch `m11-act-7`, uncommitted): Level 45 "Hive Reef" with the spawning
organs, the pits in the brood fields, the call organ and its horn, the courier's cyst and the spore clouds, with the
hive-reef backdrop, the radio voiced and the sounds in place. This round asks, in this order: the level's ambience
**bed** (a/b), part C's **art to confirm**, the level **in play** from game captures, and the **important calls**, each
with its recommendation. The recommendation is pre-selected on every row; leave a row alone to keep it.

The in-play rows are game captures (step C5, silent); [how they were made](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/concept/prompts.md#game-captures-r90-a).

Decided as trivia, not asked: the pit wave moved to t=173→178 with its followers and the cyst to t≈176 (a gap between
the reefs); the early radio lines cut to one page and Varga's briefing page shortened; the organ's torn husk is its stump;
the call organ glows only once it is loose; Level 45's `bounty_scale` is 0.17; nine voice seeds pinned.

**Closed 2026-11-06**: the answers are in the Answer and Outcome columns and the Decisions. S.1 a, F.1–F.5 accept, L.1–L.4 ok, C.1–C.6 accept (no notes).

## Review

The review page is built from this file by `build/round90-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 90`). The game captures are step C5's `--shots` screenshots.

## Sound

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [The `vrell-hive` bed (QC-1)](../../audio/sfx/README.md) | **pick a or b**. Plays under the whole of Level 45, below the music (later under Levels 48 and 50). It should sound like slow, deep breathing of something very large, with a faint dry creak of growing tissue; nothing wet, no voices. **a** (recommended): a big animal's slow breath, over an octave down, one every 8 s, with a faint leather and rope creak in the pauses. **b**: a deep cave breathing in and out every 8 s (an air draught, not an animal), with two sparse leather creaks. | [a](../../audio/sfx/concept/vrell-hive-r90-a.ogg), [b](../../audio/sfx/concept/rejected/vrell-hive-r90-b.ogg) | not listened to by Claude: checked by level, peaks and loop seams | **a** | a (`vrell-hive-r90-a`) chosen, already played as the stand-in bed; b rejected (moved to `concept/rejected/`, CREDITS.md paths updated) |

## Art to confirm

Part C's production art, already in the game: accept, or redo with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.1 | [The spawning organ](../../enemies/ground/spawning-organ/README.md) | **accept or redo**. The organ made final: the iris opening with its glow, the breath, the pods swelling and bursting, its death and the torn husk that stays as its stump. | [frames](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/concept/spawning-organ-final-r90-a.png), [motion](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/concept/spawning-organ-final-r90-a.gif) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.2 | [The hive-reef kit](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/README.md) | **accept or redo**. Level 45's backdrop: the rim's plates over the nebula, the brood fields and their pits, the spine, the living bridge over the gap, the second reef, the brood well and the far rim with the dead fleet ahead. | [sheet](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/concept/backdrop-final-r90-a.png), [time-lapse](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/concept/backdrop-final-r90-a.gif) | fixed after the clips: the spine (section 3) had thin, ruler-straight dark lines across its plates, and the living bridge hard straight joints; both now have soft, curved ribs and joints (the sheet and time-lapse here are the fixed ones) | **accept** | accepted: production art confirmed (rows chosen) |
| F.3 | [The call organ, its roots and the courier's cyst](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/README.md) | **accept or redo**. The call organ rooted on the spine, its four roots cut one by one, the horn lifting and drifting loose; the courier's cyst whole, cracked and broken open. | [sheet](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/concept/l45-props-final-r90-a.png), [motion](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/concept/l45-props-final-r90-a.gif) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.4 | [The spore cloud](../../world/vrell-space/README.md) | **accept or redo**. The cloud and its burn over the brood fields, at 1:1: the ship inside a cloud, a second cloud burnt away. | [sheet](../../world/vrell-space/concept/spore-cloud-final-r90-a.png), [motion](../../world/vrell-space/concept/spore-cloud-final-r90-a.gif) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.5 | [Level 45's briefing images](../../ui/briefing/README.md) | **accept or redo**. The fleet's scan of the reef with the eight organs ringed, and Varga's sketch of an organ and the call organ with its four roots marked. | [images](../../ui/briefing/concept/briefing-images-final-r90-a.png) | — | **accept** | accepted: production art confirmed (rows chosen) |

## The levels in play

Game captures of Level 45 (step C5, silent; the ship flies invulnerable).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| L.1 | [Level 45 Hive Reef: an organ and a pit](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/README.md) | **ok or change**. An organ releasing its pods beside an opening pit, and the pit's units rising untouchable until they have cleared it. | [organ and pit](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/concept/organ-pit-capture-r90-a.gif) | at play size the organ is a dark violet sac with a thin yellow-green rim: it reads, but it is not the brightest thing on the reef; the clip steers the ship aside so the organ lives past the pit's opening; re-captured over the fixed spine (step CD5): no straight lines left in it | **ok** | ok: capture rows chosen |
| L.2 | [Level 45: a spore cloud burning](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/README.md) | **ok or change**. A spore cloud set alight and burning away, and the ship inside a cloud. | [the burn](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/concept/cloud-burn-capture-r90-a.gif), [inside a cloud](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/concept/cloud-inside-capture-r90-a.gif) | the first cloud is faint over the rim's violet nebula (it shows best as it burns); the inside clip flies without Rook so no blast burns the cloud first | **ok** | ok: capture rows chosen |
| L.3 | [Level 45: the horn taken](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/README.md) | **ok or change**. The call organ's four roots cut, the horn lifting over 2 s and drifting loose, and the ship taking it. | [the horn taken](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/concept/horn-taken-capture-r90-a.gif) | the cut roots stay as grey knots, and the 2 s lift reads more as the horn riding down with the reef than rising; the clip steers the ship under the roots and up into the horn | **ok** | ok: capture rows chosen |
| L.4 | [Level 45: Vrell bullets over the veins](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/README.md) | **ok or change**. Vrell bullets crossing the brightest veins of the reef, at 1:1: can you still see them? | [bullets over veins, 1:1](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/concept/bullets-veins-capture-r90-a.png) | none seen: the white-cored magenta bullets stand clear of the teal veins | **ok** | ok: capture rows chosen |

## Important calls

Calls the part C steps made where the documents left room, each built as recommended. Accept, or change with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [A denser Level 45](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/README.md) | **accept or change**. Level 45 was made denser with mixed kinds of enemy (61 added waves); hard is much harder (5.1 enemies on the screen, 46 shots a minute). Recommended: accept. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.2 | [Level 45's small units](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/README.md) | **accept or change**. Its small units still die faster than the rule wants (0.69× of Level 10's life against 0.9×). Recommended: accept. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.3 | [The pits' hold](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/README.md) | **accept or change**. A pit's units are held 120 px, measured from the pit's centre, and a pit that is still held off the bottom edge of the screen loses its wave. Recommended: accept. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.4 | [The organ's seeds](../../enemies/ground/spawning-organ/README.md) | **accept or change**. The organ's six seeds spiral out together, not one after another. Recommended: accept. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.5 | [The Threadcrawlers' crossing](../../campaign/act-7-beyond-the-gate/level-45-hive-reef/README.md) | **accept or change**. The Threadcrawlers cross the screen diagonally: they cannot cross level against the scroll. Recommended: accept. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.6 | [The beds' loudness](../../audio/sfx/README.md) | **accept or change**. The new beds are levelled to the gate bed's loudness (−33.5 LUFS), so they sit with the old ones. Recommended: accept. | — | — | **accept** | accepted as built; recorded in the part's Decisions |

## Decisions

- 2026-11-06: round 90 opened (M11 part C built up to its captures; steps CD1, ART-C, C2, AUD-CD, VOX-CD): S.1 the
  `vrell-hive` bed a/b (QC-1), F.1–F.5 part C's art to confirm, L.1–L.4 the level in play (capture rows pending),
  C.1–C.6 the important calls; trivia in the summary.
- 2026-11-06: the capture rows L.1–L.4 filled in (step C5): the organ and the pit, the cloud's burn and the ship inside
  a cloud, the horn taken, the bullets over the veins at 1:1.
- 2026-11-06: L.1 re-captured over the spine fixed in step FIXC-SPINE (step CD5); F.2's weak spot updated.
- 2026-11-06 (user, closed; page answers, no notes): **S.1 a** `vrell-hive-r90-a` chosen (already played as the stand-in bed); `vrell-hive-r90-b` rejected (moved to `concept/rejected/`, CREDITS.md paths updated). **F.1–F.5 accept**: the `*-final-r90-*` art rows chosen (Level 45 and its parts `art: chosen`). **L.1–L.4 ok**: the capture rows chosen. **C.1–C.6 accept** (recorded in the parts' Decisions).
