---
title: Concept round 81 — the Ascendant fight and Level 42 again
design: review
implementation: n/a
art: proposed
depends-on: [../../enemies/bosses/ascendant, ../../campaign/act-6-jovian-storm/level-42-ascendant, ../round-80]
updated: 2026-11-03
---

# Concept round 81 — the Ascendant fight and Level 42 again

## Summary

Round 80's four changes (B.1, L.1, A.4, A.7) are built (step R80a, `c65e1669`): phase 1 has more and different
attacks (a lance sweep, twin lances and a battery barrage, with a lance at least every second draw), the escape is a short
scene instead of a chase (no target, no bar), the planet in the backdrop no longer stutters, the right cruiser stays on
screen until it scrolls out and Vorne no longer repeats a line. Asked here, by watching: three short questions. The
recommendation is pre-selected on every row.

## Review

The review page is built from this file by `build/round81-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 81`). The recordings are the game's own, flown by the content
autopilot at medium with the balance plan's fit and Rook (recipe: [Level 42's prompts](../../campaign/act-6-jovian-storm/level-42-ascendant/concept/prompts.md#level-fight-r80-a)).

## Round 80 follow-ups

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.1 | [Ascendant](../../enemies/bosses/ascendant/README.md) | **confirm**. Does phase 1 have enough variety now, and does the short escape read clearly? New in phase 1: the lance sweep, twin lances and battery barrage; at the end the pod just flies off, nothing to shoot. | [the whole fight with sound (2:24)](../../enemies/bosses/ascendant/concept/ascendant-fight-r81-a.mp4) | Rook survives phase 1 in this run | **ok** / change | open |
| A.7 | [Level 42 backdrop](../../campaign/act-6-jovian-storm/level-42-ascendant/README.md#concept-art) | **confirm**. Is the planet steady now? | [the level's first 15 s](../../campaign/act-6-jovian-storm/level-42-ascendant/concept/backdrop-steady-r81-a.mp4) | the curve drifts down one row every 2.3 s, by design | **ok** / change | open |
| L.1 | [Level 42](../../campaign/act-6-jovian-storm/level-42-ascendant/README.md) | **confirm**. Ok? The right cruiser now stays until it scrolls out and Vorne no longer repeats his sentence. (The whole level will be seen again in the act playthrough.) | [the fight recording](../../enemies/bosses/ascendant/concept/ascendant-fight-r81-a.mp4) | this clip is the fight only; the approach and the cruiser are in the act playthrough | **ok** / change | open |

## Decisions

- 2026-11-03: Round 81 opened (step `m10-cl80`, after round 80 closed): B.1 the Ascendant fight, A.7 the steady planet and
  L.1 Level 42's two fixes, the only rows round 80 left open. Everything else of round 80 was answered ok or picked.
