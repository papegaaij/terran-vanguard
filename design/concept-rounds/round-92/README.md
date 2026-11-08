---
title: Concept round 92 — M11 part E "Level 47 Seraph Gauntlet"
design: review
implementation: n/a
art: proposed
depends-on: [../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet, ../../enemies/air/choir-seraph, ../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir, ../../audio/sfx, ../../audio/music, ../../ui/briefing, ../../ui/hangar, ../../tech/roadmap, ../round-91]
updated: 2026-11-07
---

# Concept round 92 — M11 part E "Level 47 Seraph Gauntlet"

## Summary

M11 part E was built (branch `m11-act-7`, uncommitted; the full `./gradlew check` green on 2026-11-07, Acts 1–6's
golden flights and replays unchanged): Level 47 "Seraph Gauntlet" with the Choir Seraphs jumping in by their rings,
mirroring the ship in pairs and folding away, the Heralds over three of the pairs, the inner-sphere backdrop and the
core's light, the memory pearl, track 17 by section and the radio voiced. This round asks, in this order: the inner
sphere's ambience **bed** (a/b), track 17 **in place**, part E's **art to confirm**, the level **in play** from game
captures (and round 91's crack cue again, fixed), and the **important calls**, each with its recommendation. The
recommendation is pre-selected on every row; leave a row alone to keep it. The real-time captures have the game's sound; the two flown without firing are silent.

Decided as trivia, not asked: the Herald hangs a little closer over its pair so its ring reaches the upper Seraph;
the Herald leaves as its pair starts to leave, so a rising Seraph never touches it; a Seraph's ring ends at once when it
jumps away; a pair's two rows are moved together so they never overlap; easy's single Seraphs and hard's extra pair and
Harrow streams are separate waves per difficulty, the Harrows 1 s later so their warnings never meet a pair's arrival;
the small enemies keep off the centre while a pair is on the screen; Varga's briefing page was shortened to fit one
screen, the meaning kept; the memory pearl is a tumbling stone shard that drifts down the field; Level 47's money
scale is 0.17.

## Review

The review page is built from this file by `build/round92-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 92`). The game captures are step EF5's
([how](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/concept/prompts.md#game-captures-r92-a)).

## Sound

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [The `vrell-inner-sphere` bed (QE-1)](../../audio/sfx/README.md) | **pick a or b**. Plays under the whole of Level 47, below the music: the stone shell round the Choir's core. It should sound like a vast hollow space, warm and low, with a faint far hum of air in pipes; never a melody, never a voice. **a** (recommended, in the game now): a big empty hall's room tone, slowed and muffled, with a real organ pipe slowed to a far low hum under it that swells a little every 8 s. **b**: a deep hollow wash, like air blowing through a huge steel tank, with a faint high hiss of organ wind on top and no hum at all. | [a](../../audio/sfx/concept/rejected/vrell-inner-sphere-r92-a.ogg), [b](../../audio/sfx/concept/rejected/vrell-inner-sphere-r92-b.ogg) | not listened to by Claude: checked by level, peaks and loop seams; b has no pitched hum on purpose and may sound like plain room noise next to Level 46's bed | **neither** | neither variant: a reads as voices, b as machinery ("don't we already have something that is good enough?"); b rejected (moved to `concept/rejected/`, CREDITS.md paths updated); a stays as the game's stand-in until the bed is swapped, then moves to `rejected/`; the user asks to reuse an existing chosen bed: open, pending the user's pick (recommended `vrell-gate-r89-a`, Act 7's open Vrell-space bed); nothing started |

## Music

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| M.1 | [Track 17 "Hollow Light" in Level 47](../../audio/music/README.md) | **ok or redo**. Round 87's chosen take, now split in two: the Choir's statement (its one sung phrase and the answer) is its own file and plays only in Level 47's Choir hall (t=120–200), over the full mix. Levels 44 and 46 now play track 17 without it (the approved rule: the Choir's part is off elsewhere). Level 47: the base over the shell's edge, the full mix from the first pair (t=62), the statement in the hall, back to the base under the Choir's last line. Listen to the full track and the statement. | [full, without the statement](../../../assets/music/hollow-light.ogg), [the statement](../../../assets/music/hollow-light-motif.ogg) | the two files sum back to the chosen take within −46 dB; the voice that doubles the glass tune at the peak stays in the full track (it is not the Choir's phrase) | **ok** | ok: track 17 split in place confirmed |

## Art to confirm

Part E's production art (step ART-E), already in the game: accept, or redo with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.1 | [The Choir Seraph](../../enemies/air/choir-seraph/README.md) | **accept or redo**. Round 84's "blades" made final: gliding, banking with its sideways speed, the violet flare before its spiral, the fold before a jump, the leaving tell (the wings flaring white as it rises), the death in blade shards and feathers of light, its violet shot, over Level 47's brightest and darkest stone. | [frames](../../enemies/air/choir-seraph/concept/choir-seraph-final-r92-a.png), [motion](../../enemies/air/choir-seraph/concept/choir-seraph-final-r92-a.gif) | in the game a faint halo of its glow stays round the wings while it is folded | **accept** | accepted: production art confirmed (rows chosen) |
| F.2 | [The inner-sphere kit](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/README.md) | **accept or redo**. Level 47's backdrop: the dead fleet's last hulls, the shell's rim, its ribs at three sizes in violet stone lit gold-white along their edges, the Choir hall's vault of rings and spokes, the core's light growing at the top, the citadel's dark mass at the end. | [sheet](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/concept/backdrop-final-r92-a.png), [time-lapse](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/concept/backdrop-final-r92-a.gif) | the core's light is moderate (it fills the top half only near the end, see L.4); the stone is violet, not pale bone (round 84's one palette) | **accept** | accepted: production art confirmed (rows chosen) |
| F.3 | [The memory pearl](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/README.md) | **accept or redo**. The level's secret: a pearl in a shard of shell stone, tumbling down the right of the field, flickering with the remembered Moon; shot apart, its light released. | [sheet](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/concept/l47-props-final-r92-a.png), [motion](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/concept/l47-props-final-r92-a.gif) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.4 | [Level 47's briefing images](../../ui/briefing/README.md) | **accept or redo**. Okafor's chart of the inner sphere (a ring of ribbed stone round the core, the fleet's way in dotted through the wall) and Varga's scan of a Seraph mirroring the ship across the centre line. | [images](../../ui/briefing/concept/briefing-images-final-r92-a.png) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.5 | [The Seraph's intel portrait](../../ui/hangar/README.md) | **accept or redo**. The hangar's 30×30 picture of the Seraph, wings open, nose down, its heart lit. | [portrait](../../ui/hangar/concept/intel-final-r92-a.png) | — | **accept** | accepted: production art confirmed (rows chosen) |

## The levels in play

Game captures (step EF5, silent): Level 47 flown by the autopilot with the balance plan's fit and Rook, and round 91's
crack cue in Level 46 again after its fix.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| L.1 | [Level 47: a pair mirroring](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/README.md) | **ok or change**. A pair arrives in its rings, mirrors the ship across the centre and fires; the ship crosses the middle and kills one. | [the pair and the kill](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/concept/pair-kill-capture-r92-a.mp4) | in play the beam kills a pair within about two seconds of its landing, so the mirroring shows only briefly | **ok** | ok: capture rows chosen |
| L.2 | [Level 47: a pair under a Herald](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/README.md) | **ok or change**. A Herald over a pair, its ring on the upper Seraph while it passes under. | [the pair under a Herald](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/concept/herald-pair-capture-r92-a.mp4) | flown without firing: in the real run the beam on the centre line kills each Herald before its pair lands (it hangs right above the ship), so a player with the beam rarely sees a pair under a Herald; the ring also lands on the small enemies passing under it (C.3) | **ok** | ok: capture rows chosen |
| L.3 | [Level 47: a Seraph leaving](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/README.md) | **ok or change**. A Seraph's leaving tell (its wings flare white as it rises) and the HUD's well blinking amber while it shows. | [the leaving tell](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/concept/leaving-capture-r92-a.mp4) | flown with the ship held at the left edge so the Seraph escapes; the well's blink is faint: the well only lightens to a dark grey, it hardly reads as amber | **ok** | ok: capture rows chosen |
| L.4 | [Level 47: the core's light](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/README.md) | **ok or change**. Near the end the core's light fills the top of the field, the citadel's dark mass over it. | [the level's end](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/concept/core-light-capture-r92-a.mp4) | the core's light is moderate, mostly in the lit ribs; the citadel's dark mass came in with a soft but straight lower edge, like the bottom of a box: **fixed after this clip** (step FIXE-CITADEL: a ragged foot of uneven roots and tapered sides; the ribs' shadow edges warped too; see F.2's sheet) | **ok** | ok: capture rows chosen |
| L.5 | [Level 47: a Seraph over the ribs](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/README.md) | **ok or change**. At 1:1: can you pick the Seraph and its shots out of the lit ribs? | [Seraphs over the ribs, 1:1](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/concept/seraph-vs-ribs-capture-r92-a.png) | the ivory Seraphs stand out well from the violet stone; their violet shots are clearest over the dark stone, a little softer over the lit edges | **ok** | ok: capture rows chosen |
| L.6 | [Level 46: the crack cue, fixed](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md) | **ok or change**. Round 91's C.7: the quiet's Husks now come in later and slower, so all five are on the screen when the cue cracks them in turn. | [the cracks](../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/concept/cracks-capture-r92-a.gif) | all five Husks are on the screen and crack in turn; the edge warnings of the next waves still cover the top and the sides while they crack | **ok** | ok: capture rows chosen |

## Important calls

Calls the part E steps made where the documents left room, each built as recommended. Accept, or change with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [A denser Level 47](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/README.md) | **accept or change**. With the planned waves alone the screen was nearly empty between the Seraphs (the beam clears small enemies fast), so 69 waves of mixed small enemies were added, kept off the centre while a pair is on the screen. Recommended: accept. | — | hard is much busier (about 4.6 enemies on the screen against 3.0 on medium) | **accept** | accepted as built; recorded in the part's Decisions |
| C.2 | [Level 47's small enemies](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/README.md) | **accept or change**. Its small enemies die faster than the rule wants (they last two thirds of Level 10's time, the rule asks nine tenths), as in Levels 45 and 46. Recommended: accept. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.3 | [The Herald's ring](../../enemies/air/choir-herald/README.md) | **accept or change**. A Herald's ring shields any enemy that passes under it, so the small enemies crossing below a Herald get its ring too, not only the Seraph. Recommended: accept (the Herald's own rule); the alternative keeps those waves clear of the Heralds. | [the pair under a Herald](../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/concept/herald-pair-capture-r92-a.mp4) | — | **accept** | accepted as built; recorded in the part's Decisions |

## Decisions

- 2026-11-07: round 92 opened (M11 part E built, step EF5): S.1 the `vrell-inner-sphere` bed a/b (QE-1), M.1 track 17
  split (the Choir's statement only in Level 47's hall), F.1–F.5 part E's art to confirm, L.1–L.5 Level 47 in play and
  L.6 round 91's crack cue re-captured, C.1–C.3 the important calls; trivia in the summary.
- 2026-11-07 (user, closed; page answers): **S.1 neither**, the user's note: "The first one has voices, the second machinery. Don't we already have something that is good enough?" Both variants rejected (a reads as voices, b as machinery): `vrell-inner-sphere-r92-b` moved to `concept/rejected/` (CREDITS.md paths updated); `vrell-inner-sphere-r92-a` stays in `concept/` as the game's stand-in until the bed is swapped (it moves to `rejected/` then). The user asks to reuse an existing chosen bed: **open, pending the user's pick** (recommended: `vrell-gate-r89-a`, Act 7's open Vrell-space bed); nothing started; recorded in Level 47's Open questions and the sfx README. **M.1 ok**. **F.1–F.5 accept**: the `*-final-r92-*` art rows chosen. **L.1–L.6 ok**: the capture rows chosen (L.6, round 91's crack cue fixed, included). **C.1–C.3 accept**, recorded in the parts' Decisions.
