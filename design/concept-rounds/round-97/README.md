---
title: Concept round 97 — the two heartbeats for a phone speaker
design: review
implementation: n/a
art: proposed
depends-on: [../../audio/sfx, ../../enemies/bosses/choir-heart, ../round-95]
updated: 2026-11-08
---

# Concept round 97 — the two heartbeats for a phone speaker

## Summary

Round 95 answered "ok or redo" on the Heart's and the hive's sounds with two notes: S.1 "Heartbeat is too weak,
especially on my phone. Probably due to the low frequency." and S.2 "Heartbeat is again hard to hear on a phone". The
cause is as you guessed: nearly all of the old beats' energy sits below what a phone speaker plays. This round has three
new beats for the Heart (S.1) and three matching layers for the hive (S.2), each keeping the old deep thud and adding a
body a small speaker plays. Pick a, b or c for S.1, then the one matching it for S.2. The pick goes into the game after
the choice.

## Review

The review page is built from this file by `build/round97-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 97`). How the files were made and measured:
[prompts](../../audio/sfx/concept/prompts.md) (*Concept round 97*).

## Sound

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [The Choir Heart's heartbeat (`heart-beat`)](../../enemies/bosses/choir-heart/README.md#sounds-brief) | **pick a, b or c** (recommended: **a**). **Plays:** on every pulse of the Heart's light, every 3 s (2 s in phase 3), under the whole final fight over track 20. Should sound like a huge, slow, muffled heartbeat thud that you still hear on a phone. **a:** the same timpani as now with its skin left in: one deep, round boom with a soft attack. **b:** a lub-dub: two big taiko hits, the second softer, woody. **c:** the old thud with a heavy knock on top, like a fist on a huge wooden door: darker and drier than a. | over track 20 (20 s): [a](../../audio/sfx/concept/rejected/heart-beat-mix-r97-a.ogg), [b](../../audio/sfx/concept/rejected/heart-beat-mix-r97-b.ogg), [c](../../audio/sfx/concept/rejected/heart-beat-mix-r97-c.ogg), [now (r84-b)](../../audio/sfx/concept/rejected/heart-beat-mix-r97-r84b.ogg); the beat alone: [a](../../audio/sfx/concept/rejected/heart-beat-r97-a.ogg), [b](../../audio/sfx/concept/rejected/heart-beat-r97-b.ogg), [c](../../audio/sfx/concept/rejected/heart-beat-r97-c.ogg) | Not listened to, only measured. Through a phone-speaker model a, b and c stand level with the music at their hit (+0.3, 0.0, +1.0 LU) where now is 15 LU under; on full speakers they are about 3 LU louder than before. c's knock is limited hardest (it may sound a little squashed), and b's double beat may blur at phase 3's 2 s rhythm. | a / b / c | **none**: "All bad. These sounds like beats on a drum." | all three rejected (files in `rejected/`); round 98 follows with real recorded hearts |
| S.2 | [The hive's heartbeat layer (`vrell-hive-heartbeat`)](../../audio/sfx/README.md#ambience-per-setting) | **pick a, b or c** (recommended: the one matching S.1's pick). **Plays:** over the `vrell-hive` bed on Level 50's approach, rising from the Choir Chambers to full in the Last Chamber, out at the boss warning. Should sound like the Heart's beat far off, slower and quieter, felt and still heard on a phone. **a, b, c:** S.1's beat slowed and darkened, one thud every 4 s (a: a round distant boom; b: a distant lub-dub; c: a distant knock over the boom). | over the bed (20 s): [a](../../audio/sfx/concept/rejected/vrell-hive-heartbeat-mix-r97-a.ogg), [b](../../audio/sfx/concept/rejected/vrell-hive-heartbeat-mix-r97-b.ogg), [c](../../audio/sfx/concept/rejected/vrell-hive-heartbeat-mix-r97-c.ogg), [now (r84-b)](../../audio/sfx/concept/rejected/vrell-hive-heartbeat-mix-r97-r84b.ogg); the layer alone (at its full level): [a](../../audio/sfx/concept/rejected/vrell-hive-heartbeat-r97-a.ogg), [b](../../audio/sfx/concept/rejected/vrell-hive-heartbeat-r97-b.ogg), [c](../../audio/sfx/concept/rejected/vrell-hive-heartbeat-r97-c.ogg) | Not listened to, only measured. Through the phone model each thud stands over the bed (a +1.3, b +1.1, c +2.1 LU; now −12.3); on full speakers the layer stays 3 LU under the bed, as before. The previews are the layer at its full level, the end of the approach. | a / b / c | **none**: "Also all bad. Doesn't sound like a heart at all" | all three rejected (files in `rejected/`); round 98 follows with real recorded hearts |

## Decisions

- 2026-11-08: round 97 opened (step `m11-r97`, from SND-HB's rows): S.1 the Heart's heartbeat and S.2 the hive layer, a, b
  or c each, answering round 95's S.1 and S.2 notes; the pick goes into the game (`assets/`, `Sfx.java`) after the choice.
- 2026-11-08: round 97 closed: S.1 **none** (the user: "All bad. These sounds like beats on a drum.") and S.2 **none** ("Also all bad. Doesn't sound like a heart at all"); all 14 files moved to `concept/rejected/` (sfx rows to its README, CREDITS.md paths updated). The heartbeat must be a real heart, never a drum; round 98 follows ([record](review.html)).
