---
title: Concept round 87 — Act 7: the Resonator's fire as a whoosh and two tracks re-checked
design: approved
implementation: n/a
art: chosen
depends-on: [../../player/weapons/choir-resonator, ../../audio/sfx, ../../audio/music, ../../ui/credits, ../round-86]
updated: 2026-11-13
---

# Concept round 87 — Act 7: the Resonator's fire as a whoosh and two tracks re-checked

## Summary

The follow-up to [round 86](../round-86/README.md) (Act 7 design phase, branch `m11-act-7`). About the Choir Resonator's fire sound you wrote: "I'm not sure this will sound great when played in a loop. Also, the artwork make it look like some sort of soundwave. I'd expect more of a whoosh sound. Neither is one. Both have a sharp part in it." It is now a whoosh with no sharp start, in two takes, each with its single shot and a 4-second loop of it at the fire rate. The hull hit (round 86 a) stays. The music: the two other Act 7 tracks you picked in round 84 and nobody had checked, track 17 b "Hollow Light" and track 28 a "Coalition Forever", were measured against the standard of round 86 (real tunes, phrases that finish, no clashes). **Both failed**, so each has one new take: say **ok** or **change**. Tracks 16, 34 and 27 passed the same check in round 86 and need nothing. Every row has the recommendation pre-selected: leave a row alone to keep it. Everything was measured, not listened to by Claude: the pre-selected answer is a default.

**Closed 2026-11-05**: the answers are in the Answer and Outcome columns and the Decisions. S.1 a, M.1 ok, M.2 ok (no notes); Act 7's design rounds are done.

## Review

The review page is built from this file by `build/round87-tools/build_review.py` (untracked); the board is [index.html](index.html) (`python3 tools/concept/board.py 87 --subjects resonator-fire,act7-b-theme,credits`).

## Sounds: the Choir Resonator

Two takes, levelled to the same loudness (`tools/concept/audio/sfx_r87.py`); cut from public Freesound previews (CC0 and CC-BY).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [`resonator` fire](../../audio/sfx/README.md) | **pick a or b (sound)**. **Plays:** the Resonator's shot, once for every wave: 2.5 to 3.5 times a second, so it is the sound heard most while you fly with it. **Should sound like:** a whoosh matching the wave front it pushes out: a soft quick swell, no sharp start, heavy but not airy, pleasant when repeated. **a "the heavy pass":** a deep falling "VWOOMmm", like a heavy thing rushing past, 0.30 s. **b "the fwoosh":** a quicker, brighter "FWOOSH" with a low hum under it, 0.26 s. Each has its single shot and a 4-second loop (3 a second, with the game's ±5 % pitch change). Recommended a: it is the closest to "a wave pushed out"; b's hum is the only steady tone. | [a, the shot](../../audio/sfx/concept/resonator-fire-r87-a.ogg), [a, 4 s loop](../../audio/sfx/concept/resonator-fire-r87-a-loop.ogg), [b, the shot](../../audio/sfx/concept/rejected/resonator-fire-r87-b.ogg), [b, 4 s loop](../../audio/sfx/concept/rejected/resonator-fire-r87-b-loop.ogg) | measured, not listened to; b's hum under the whoosh may read as a tone when repeated | **a** | a ("the heavy pass") chosen, its loop kept as the review aid; b rejected (moved to `concept/rejected/`, CREDITS.md paths updated); the game plays it with ±5 % pitch |

## Music

One take each of the two tracks that failed the check, at the same loudness (`tools/concept/audio/music_r87.py`). Section times are from the score; measured, not listened to.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| M.1 | [Act 7 B theme "Hollow Light" (track 17)](../../audio/music/README.md) | **ok or change (music)**. **Check of your round-84 pick (b):** its only tunes are the two motifs (6 of its 20 bars), no phrase answered or finished, two notes clashing with the chord, and the whole loop repeated after 49 seconds. **Plays:** Levels 44, 46 and 47 and the Level 50 approach, looping (the unsettled half of Act 7). **Now:** 2:17 loop. **Kept:** the sound you picked: bowed-glass pads, the soft heartbeat, the sub, reversed glass swells, the distant trumpet, a wordless solo voice; 98 BPM, E minor. **New:** a 16-bar glass tune in two answering phrases, a tune of its own for the trumpet (opened once by the main motif), the Choir motif only once, sung by the lone voice. Sections: 0:00 intro, 0:10 the glass tune with the heartbeat, 0:49 the trumpet's tune, 1:09 the peak (glass tune doubled, the voice under it from 1:28), 1:48 a breakdown with the voice alone, 2:02 the rebuild, 2:22 a two-bar turnaround, loop back at 2:27. Every phrase ends on a cadence and no section starts mid-phrase. | [the take](../../audio/music/concept/act7-b-theme-r87-a.ogg) | measured, not listened to | **ok** | `act7-b-theme-r87-a` chosen (track 17, loops 2:17); round 84 b rejected, superseded by the round 87 take |
| M.2 | [Credits "Coalition Forever" (track 28)](../../audio/music/README.md) | **ok or change (music)**. **Check of your round-84 pick (a):** half its bars are motif cells, the first section is the bare main motif twice, six notes clash with the chord, and the 55-second loop repeats. **Plays:** the credits roll after the campaign and the main menu's Credits item. **Now:** 2:58 and **it plays once**: if the roll or the Credits item lasts longer, it starts again after 2 seconds of silence (decided by the main agent, not looped like before). **Kept:** the tracker trance you picked (140 BPM, D minor, kick, off-beat bass, pulse arpeggio, supersaw) and the title theme's melodies note for note, now with new answering phrases that finish. Sections: 0:00 intro, 0:14 verse (the title's A melody and its answer from 0:27), 0:41 chorus (the B melody and its answer from 0:55), 1:09 bridge, 1:22 a drumless breakdown on the slow C melody, 1:36 the build with the title's two calls and a snare roll, 1:50 the chorus at full strength, 2:17 the verse at full strength, 2:45 the outro landing on D and fading by 2:58. | [the take](../../audio/music/concept/credits-r87-a.ogg) | measured, not listened to; the fixed length and the restart after 2 s of silence are not yours yet: say **change** if you would rather have a loop | **ok** | `credits-r87-a` chosen (track 28, 2:58, plays once and restarts after 2 s of silence); round 84 a rejected, superseded by the round 87 take |

## Decisions

- 2026-11-05: round 87 opened (follow-up to round 86: the Choir Resonator's fire re-taken as a whoosh, a/b with a single shot and a 4 s loop each; the two Act 7 tracks picked in round 84 and never checked, 17 b and 28 a, failed round 86's standard and were recomposed, one take each). Track 28 becomes a play-once piece that starts again after 2 s of silence when the credits outlast it (decided by the main agent, [credits](../../ui/credits/README.md), [music](../../audio/music/README.md)).
- 2026-11-05 (user, closed; page answers, no notes): **S.1 a** (`resonator-fire-r87-a`, "the heavy pass", a deep falling vwoom) chosen, its loop kept as the review aid, b and its loop rejected. **M.1 ok**: `act7-b-theme-r87-a` (track 17, loops 2:17) chosen; `act7-b-theme-r84-b` rejected (superseded by the round 87 take: the check found it mostly motif cells, no cadences, looping after 49 s). **M.2 ok**: `credits-r87-a` (track 28, 2:58, plays once, restarts after 2 s of silence) chosen; `credits-r84-a` rejected (superseded by the round 87 take: half motif cells, the A section the bare motif twice, clashes). The Resonator's sounds and all five Act 7 tracks are now chosen; Act 7's design rounds are done, the M11 build parts follow.
