---
title: Concept round 83 — the Ascendant fight and Level 42, third time
design: review
implementation: n/a
art: proposed
depends-on: [../../enemies/bosses/ascendant, ../../campaign/act-6-jovian-storm/level-42-ascendant, ../round-81]
updated: 2026-11-03
---

# Concept round 83 — the Ascendant fight and Level 42, third time

## Summary

Round 81's two changes (B.1, L.1) are built (step R81a, `987b6fe2`): phase 1 never rests longer than 1.0 s and
standing still costs armour, the escape lasts 9 s with Vorne's line in full, the broken halves carry the stern, and
Level 42's approach is recorded again (round 81 showed the wrong clip). Asked here, by watching: three short questions.
(Round 82 is the databank's, so this round is 83.) The recommendation is pre-selected on every row.

## Review

The review page is built from this file by `build/round83-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 83`). The recordings are the game's own, flown by the content
autopilot at medium with the balance plan's fit and Rook (recipe: [the fight's prompts](../../enemies/bosses/ascendant/concept/prompts.md#ascendant-fight-r82-a)).
The media files carry the suffix `r82` (named before the round number was settled).

## Round 81 follow-ups

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.1 | [Ascendant](../../enemies/bosses/ascendant/README.md) | **confirm**. Does phase 1 keep you busy now, does the escape last long enough, and do the broken halves look whole as they sink? Times: the eruption 1:36, the heart dies 2:18, the pod hangs 2:21–2:23 and climbs with Vorne's line, ESCAPED 2:30; the halves sink from 2:21. | [the whole fight with sound (2:37)](../../enemies/bosses/ascendant/concept/ascendant-fight-r82-a.mp4) | Rook flies the whole fight in this run | **ok** / change | open |
| A.1 | [Ascendant](../../enemies/bosses/ascendant/README.md#concept-art) | **confirm**. Does the stern of the broken halves look right? | [the stern art (sheet)](../../enemies/bosses/ascendant/concept/ascendant-fx-final-r82-a.png), [animation](../../enemies/bosses/ascendant/concept/ascendant-fx-final-r82-a.gif) | the sheet is at half size | **ok** / change | open |
| L.1 | [Level 42](../../campaign/act-6-jovian-storm/level-42-ascendant/README.md) | **confirm**. Does the right cruiser now stay until it scrolls away (0:40–0:58), and does Vorne's arrival line play once (1:40–1:52)? | [the approach with sound (1:54)](../../campaign/act-6-jovian-storm/level-42-ascendant/concept/approach-r82-a.mp4) | the whole level will be seen again in the act playthrough | **ok** / change | open |

## Decisions

- 2026-11-03: Round 83 opened (step `m10-cl81`, after round 81 closed): B.1 the Ascendant fight, A.1 the halves' stern art
  and L.1 Level 42's approach, the rows round 81 left as change. The round number 82 was already the databank's.
