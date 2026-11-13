---
title: Concept round 81 — the Ascendant fight and Level 42 again
design: approved
implementation: n/a
art: chosen
depends-on: [../../enemies/bosses/ascendant, ../../campaign/act-6-jovian-storm/level-42-ascendant, ../round-80]
updated: 2026-11-13
---

# Concept round 81 — the Ascendant fight and Level 42 again

## Summary

**Closed 2026-11-03**: A.7 **ok** (the planet is steady); B.1 and L.1 **change**, built the same day (R81a `987b6fe2`) and
asked again in [round 83](../round-83/README.md) (round 82 is the databank's). Round 80's four changes (B.1, L.1, A.4, A.7) are built (step R80a, `c65e1669`): phase 1 has more and different
attacks (a lance sweep, twin lances and a battery barrage, with a lance at least every second draw), the escape is a short
scene instead of a chase (no target, no bar), the planet in the backdrop no longer stutters, the right cruiser stays on
screen until it scrolls out and Vorne no longer repeats a line. Asked here, by watching: three short questions.

## Review

The review page is built from this file by `build/round81-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 81`). The recordings are the game's own, flown by the content
autopilot at medium with the balance plan's fit and Rook (recipe: [Level 42's prompts](../../campaign/act-6-jovian-storm/level-42-ascendant/concept/prompts.md#level-fight-r80-a)).

## Round 80 follow-ups

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.1 | [Ascendant](../../enemies/bosses/ascendant/README.md) | **confirm**. Does phase 1 have enough variety now, and does the short escape read clearly? New in phase 1: the lance sweep, twin lances and battery barrage; at the end the pod just flies off, nothing to shoot. | [the whole fight with sound (2:24)](../../enemies/bosses/ascendant/concept/ascendant-fight-r81-a.mp4) | Rook survives phase 1 in this run | **ok** / change | **change** (the user: "Still too many quiet periods. You don't even need to dodge the bullets in the first part, because the shield regenerates before the next round. The escape is way too quick now. Also, I just noticed the 2 halves of the ship drift down, but there's no back of the ship. The ship just ends at what was the edge of the screen.") Built in R81a `987b6fe2`, asked again in [round 83](../round-83/README.md) |
| A.7 | [Level 42 backdrop](../../campaign/act-6-jovian-storm/level-42-ascendant/README.md#concept-art) | **confirm**. Is the planet steady now? | [the level's first 15 s](../../campaign/act-6-jovian-storm/level-42-ascendant/concept/backdrop-steady-r81-a.mp4) | the curve drifts down one row every 2.3 s, by design | **ok** / change | **ok** (the planet is steady) |
| L.1 | [Level 42](../../campaign/act-6-jovian-storm/level-42-ascendant/README.md) | **confirm**. Ok? The right cruiser now stays until it scrolls out and Vorne no longer repeats his sentence. (The whole level will be seen again in the act playthrough.) | [the fight recording](../../enemies/bosses/ascendant/concept/ascendant-fight-r81-a.mp4) | this clip is the fight only; the approach and the cruiser are in the act playthrough | **ok** / change | **change** (the user: "How do you want me to judge this if you don't upload the correct clip? This is the boss fight, not the cruisers." Our mistake: the wrong clip) Recorded again as `approach-r82-a`, asked again in [round 83](../round-83/README.md) |

## Decisions

- 2026-11-03: Round 81 opened (step `m10-cl80`, after round 80 closed): B.1 the Ascendant fight, A.7 the steady planet and
  L.1 Level 42's two fixes, the only rows round 80 left open. Everything else of round 80 was answered ok or picked.
- 2026-11-03: Round 81 closed (the user's answers, notes verbatim in the rows): **ok** A.7 (the planet is steady; the
  `backdrop-steady-r81-a` clip accepted); **change** B.1 (still too many quiet periods, phase 1 needs no dodging because
  the shield regenerates between rounds, the escape too quick, the broken halves have no stern) and L.1 (our mistake: the
  clip showed the boss fight, not the cruisers). Built since, **R81a `987b6fe2`**: phase 1's rests 1.0 s with a covering
  port fan, a fallback playbook once the cannon dies, no quiet stretch over 0.5 s, standing still costs armour; the escape
  9 s with Vorne's line in full; the wreck halves carry the stern; hard's HP lever x1.05. The r81 fight recording is
  superseded by `ascendant-fight-r82-a`. Asked again in [round 83](../round-83/README.md): B.1, A.1 (the stern art), L.1.
  Recorded in the parts' Decisions logs. (The round number 82 was already the databank's, so the next round is 83; the
  media keep the file suffix `r82`.)
