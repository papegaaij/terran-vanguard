---
title: Concept round 98 — the two heartbeats from real recorded hearts
design: approved
implementation: n/a
art: chosen
depends-on: [../../audio/sfx, ../../enemies/bosses/choir-heart, ../round-97]
updated: 2026-11-13
---

# Concept round 98 — the two heartbeats from real recorded hearts

## Summary

Round 97's six heartbeats were all rejected. S.1: "All bad. These sounds like beats on a drum." S.2: "Also all bad.
Doesn't sound like a heart at all". Fair: they were drums with a heart's rhythm. This round has no drum, timpani, taiko or
knock left: each of the three beats for the Heart (S.1) is cut from a real recorded heart, slowed for a giant heart, with
the lub and the dub kept, and each of the three hive layers (S.2) is the matching beat far off. Pick a, b or c for S.1,
then the one matching it for S.2. The pick goes into the game after the choice.

## Review

The review page is built from this file by `build/round98-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 98`). How the files were made and measured:
[prompts](../../audio/sfx/concept/prompts.md) (*Concept round 98*).

## Sound

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [The Choir Heart's heartbeat (`heart-beat`)](../../enemies/bosses/choir-heart/README.md#sounds-brief) | **pick a, b or c** (recommended: **c**). **Plays:** on every pulse of the Heart's light, every 3 s (2 s in phase 3), under the whole final fight over track 20. Should sound like a giant, slow heart beating, a lub-dub, no drum, and you still hear it on a phone. **a:** a close stethoscope heartbeat, dry, a deep soft lub and a shorter dub. **b:** an ultrasound heartbeat: two whooshes of rushing blood, a hospital monitor's sound. **c:** a heartbeat through the chest with a soft wet squelch, rounder and longer than a. | over track 20 (20 s): [a](../../audio/sfx/concept/rejected/heart-beat-mix-r98-a.ogg), [b](../../audio/sfx/concept/rejected/heart-beat-mix-r98-b.ogg), [c](../../audio/sfx/concept/heart-beat-mix-r98-c.ogg), [now (r84-b)](../../audio/sfx/concept/rejected/heart-beat-mix-r98-r84b.ogg); the beat alone: [a](../../audio/sfx/concept/rejected/heart-beat-r98-a.ogg), [b](../../audio/sfx/concept/rejected/heart-beat-r98-b.ogg), [c](../../audio/sfx/concept/heart-beat-r98-c.ogg) | Not listened to, only measured. All three are real heart recordings with the lub-dub kept, and through a phone-speaker model at their hit they stand level with the music (a −0.5, b 0.0, c +1.2 LU) where the game's is 15 LU under; a may still read as a kick drum, and a and c are limited hard (they may sound a little squashed). | **c** (no notes) | chosen: `heart-beat-r98-c` and its mix preview stay (the review record); a and b and the `r84b` preview rejected, in `rejected/`; import into the game pending |
| S.2 | [The hive's heartbeat layer (`vrell-hive-heartbeat`)](../../audio/sfx/README.md#ambience-per-setting) | **pick a, b or c** (recommended: the one matching S.1's pick). **Plays:** over the `vrell-hive` bed on Level 50's approach, rising from the Choir Chambers to full in the Last Chamber, out at the boss warning. Should sound like the giant heart heard through the hive's walls, far off, slower and quieter, still heard on a phone. **a, b, c:** S.1's matching heartbeat slowed and darkened, one lub-dub every 4 s (a: a distant stethoscope thump; b: a distant ultrasound whoosh; c: a distant wet pulse). | over the bed (20 s): [a](../../audio/sfx/concept/rejected/vrell-hive-heartbeat-mix-r98-a.ogg), [b](../../audio/sfx/concept/rejected/vrell-hive-heartbeat-mix-r98-b.ogg), [c](../../audio/sfx/concept/vrell-hive-heartbeat-mix-r98-c.ogg), [now (r84-b)](../../audio/sfx/concept/rejected/vrell-hive-heartbeat-mix-r98-r84b.ogg); the layer alone (at its full level): [a](../../audio/sfx/concept/rejected/vrell-hive-heartbeat-r98-a.ogg), [b](../../audio/sfx/concept/rejected/vrell-hive-heartbeat-r98-b.ogg), [c](../../audio/sfx/concept/vrell-hive-heartbeat-r98-c.ogg) | Not listened to, only measured. Through the phone model each lub stands about level with the bed (a +0.3, b +1.3, c +0.7 LU; the game's is 12 LU under); the previews are the layer at its full level, the end of the approach, and earlier it is quieter. | **c** (no notes) | chosen: `vrell-hive-heartbeat-r98-c` and its mix preview stay (the review record); a and b and the `r84b` preview rejected, in `rejected/`; import into the game pending |

## Decisions

- 2026-11-08: round 98 opened (step `m11-r98`, from SND-HB2's rows) after round 97 was closed with every proposal rejected
  ("beats on a drum", "doesn't sound like a heart"): S.1 the Heart's heartbeat and S.2 the hive layer, a, b or c each, all
  from real recorded hearts (recommended c); the pick goes into the game (`assets/`, `Sfx.java`) after the choice.
- 2026-11-09: round 98 closed (the user: S.1 **c**, S.2 **c**, no notes): the real heartbeat through the chest (contact mic, soft wet squelch) and its hive layer chosen; a, b and the two `r84b` baseline previews (10 files) moved to `concept/rejected/`, CREDITS.md and the sfx README updated. The import into the game is **open** (`assets/sfx/`, `Sfx.HEART_BEAT`, `AMBIENCE_HIVE_HEARTBEAT`, the pipeline lists and tests, the CC-BY names on the credits screen); the `-r84-b` files stand in until then ([record](review.html)).
