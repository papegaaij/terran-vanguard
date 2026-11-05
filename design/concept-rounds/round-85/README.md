---
title: Concept round 85 — Act 7: the Choir Resonator and the music re-takes
design: review
implementation: n/a
art: proposed
depends-on: [../../player/weapons/choir-resonator, ../../audio/music, ../round-84]
updated: 2026-11-05
---

# Concept round 85 — Act 7: the Choir Resonator and the music re-takes

## Summary

The follow-up to [round 84](../round-84/README.md) (Act 7 design phase, branch `m11-act-7`, nothing committed). You asked for three music pieces to be reworked (M.1 the Far Shore theme, M.3 Vorne's last stand, M.4 the ending) and kept the Choir Resonator weapon (E.2 b). This round has the new music takes and the Resonator: its two open questions, its look and its sounds, and its document to approve. Every row has the recommendation pre-selected: leave a row alone to keep it. The music and sound rows are measured, not listened to: their pre-selected answer is a default.

**Closed 2026-11-05**: the answers are in the Answer and Outcome columns and the Decisions. Q.1 a, Q.2 approved, A.1 a; the Resonator's sounds (S.1, S.2) and the music re-takes (M.1, M.3, M.4) all neither, re-taken in round 86.

## Review

The review page is built from this file by `build/round85-tools/build_review.py` (untracked); the board is [index.html](index.html) (`python3 tools/concept/board.py 85 --subjects …`).

## Questions

The Resonator's open questions, in plain words (its look and sounds are the rows below).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| Q.1 | [Does the plan buy the Resonator?](../../player/weapons/choir-resonator/README.md) | **pick a or b (important game concept)**. The Resonator is on sale from Level 44; the question is whether the money plan that checks Act 7's levels buys it. **a** the plan keeps the Ion Beam and the Resonator is only the player's choice; the hangar still warns that Level 44 wants a spreading gun and Level 46 a piercing one (as you accepted in round 84, E.1 a). **b** the plan buys the Resonator to level 3 at Level 44 next to the beam, so both warnings go away. The price of b: the Tesla Coil Pod's and the Swarm Tail's upgrades come four visits later, the forward margin at Level 48 gets thinner, and at Level 50 about half the money cushion is gone. Recommended a (drafted). | — | — | **a** | the hand plan keeps the Ion Beam and does not buy the Resonator at Level 44; the hangar warnings at Levels 44 and 46 stand |
| Q.2 | [The Choir Resonator and Level 44's new lines](../../player/weapons/choir-resonator/README.md) | **approve or change**. The Resonator's document (the wave that widens as it rises and passes through the hulls it meets, its five levels, where it sits among the front guns) and the lines it added to Level 44's document (the shop line and Varga's line in the briefing). Level 44 is already approved: only these added lines are new. Recommended: approve. | — | — | **approve** | the Resonator's document is `design: approved` (updated 2026-11-05), with the Level 44 lines it added |

## Art: the Choir Resonator

Two looks for the Resonator (`tools/concept/resonator_r85.py`).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| A.1 | [Choir Resonator: the look](../../player/weapons/choir-resonator/README.md) | **pick a or b** (artwork). **What:** the new front gun on the Stormhawk's nose and its wave, flown over Level 44's bright plane against Rift Skaters (the GIF); the sheet shows the mount, the wave at four distances, the wave through five hulls and the check over the brightest and the darkest window. **a "the bell":** the Gate Warden's eye in a gunmetal collar; the wave is one bright white crescent, thickest in the middle, with two faint rings trailing it and a thin ring on every hull it passes. **b "the chord":** a tuning fork on the nose; the wave is three thin parallel arcs with small lights running along them, finer and lighter than a. Recommended a: the single crescent reads as one wave at a glance over the bright plane, and the eye ties it to the Wardens. | [a](../../player/weapons/choir-resonator/concept/choir-resonator-r85-a.png), [a GIF](../../player/weapons/choir-resonator/concept/choir-resonator-r85-a.gif), [b](../../player/weapons/choir-resonator/concept/rejected/choir-resonator-r85-b.png), [b GIF](../../player/weapons/choir-resonator/concept/rejected/choir-resonator-r85-b.gif) | b's three thin arcs may get lost over the brightest part of Level 44 | **a** "the bell" | chosen (`art: chosen`); b "the chord" rejected, moved to `concept/rejected/` |

## Sounds: the Choir Resonator

Two takes each, levelled to the same loudness (`tools/concept/audio/sfx_r85.py`); every file is cut from a public Freesound preview (CC0).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [`resonator` fire](../../audio/sfx/README.md) | **pick a or b (sound)**. **Plays:** the Resonator's shot, once for every wave: two and a half to three and a half times a second, so it is the sound heard most while you fly with it. **Should sound like:** a short sung ring with a soft start, glass or a bowl, with a little air for the wave's push; at most a third of a second so it never tires; never a laser zap, never a pop. **a "the bell":** a struck singing bowl, a round ringing "hmmng" that falls away quickly, 0.34 s. **b "the chord":** a bowed wine glass sliding a little down with a light tuning-fork tap at the front: a sung glassy "aaah", airier and higher than a, 0.34 s. Recommended a (it goes with the look a, a bell). | [a](../../audio/sfx/concept/rejected/resonator-fire-r85-a.ogg), [b](../../audio/sfx/concept/rejected/resonator-fire-r85-b.ogg) | measured, not listened to; b's bowed start is soft and may blur at three shots a second | **neither**: "Neither of these. These do not read like weapons at all." | a and b rejected (moved to `concept/rejected/`, CREDITS.md paths updated); re-take in round 86: must sound like a weapon first, the resonance only a tail |
| S.2 | [`resonator` ring on a hull](../../audio/sfx/README.md) | **pick a or b (sound)**. **Plays:** every hull the wave passes through (at most one every 0.12 s, each next hull of a wave a little lower). **Should sound like:** a quiet clear glass ring, short, under the shots and the hits. **a:** a tuning fork struck, a short pure "ting", 0.22 s. **b:** a small wine glass tapped, a brighter, glassier "tink", 0.2 s. Recommended a. | [a](../../audio/sfx/concept/rejected/resonator-ring-r85-a.ogg), [b](../../audio/sfx/concept/rejected/resonator-ring-r85-b.ogg) | measured, not listened to | **neither**: "Same, these are like children's toys." | a and b rejected (moved to `concept/rejected/`); re-take in round 86 with S.1 |

## Music

Two takes each (`tools/concept/audio/music_r85.py`), at the same loudness. Section times are from the score; measured, not listened to.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| M.1 | [Act 7 A theme "Far Shore" (track 16)](../../audio/music/README.md) | **pick a or b** (music). **You said (round 84):** "a is the best, but 'the Choir in a formant choir' doesn't really work very well next to the main motif." **Plays:** Levels 43 and 45 and the approach of Level 48, the push through. **Now:** the horns' main motif is answered by the Choir's falling phrase in a new voice, 66 s with the loop from 1:02. Sections: 0:00 intro, 0:07 the horns' motif, the answer at 0:14, 0:21 again, 0:34 the quiet middle, 0:41 everything in, 0:55 a last answer. **a:** the answer in low brass (trombones and a bass trombone an octave under) with a dark, slowly wavering synth lead on top: the brass answers its own horns, the synth adds the cold. **b:** the answer in violins and cellos in octaves with a cold bowed-glass shimmer: the same orchestra as the horns. Recommended a (judged by measure, not by ear: the answer sits 5 dB under the mix in both, and brass next to horns is the closer match to what you asked). | [a](../../audio/music/concept/rejected/act7-a-theme-r85-a.ogg), [b](../../audio/music/concept/rejected/act7-a-theme-r85-b.ogg) | b: the Choir's F (the phrase starts on E) rubs against the D-minor key; that is meant, but strings may make it sound wrong rather than strange | **neither**: "This isn't any better, perhaps even worse for both. The problem is that these 2 motifs don't play together. It just sounds really bad. Also the repeating loop makes it worse. The background rhythm is fine, it just lacks a real build-up." | a and b rejected (moved to `concept/rejected/`); round 84's a stays `proposed`, change requested; re-take in round 86 |
| M.3 | [Vorne's last stand (track 34)](../../audio/music/README.md) | **pick a or b** (music). **You said (round 84):** "a is best, but it needs more variation. You can't play the 2 tunes over and over again, that gets boring quickly." **Plays:** Level 48 after the mid-boss sting until Vorne dies. **Now:** a loop of about two minutes (2:02, the loop starts after the intro) with five or six sections instead of the two tunes. **a, five sections:** 0:06 his motif, 0:32 a new march tune in trumpets then horns, 0:58 a quiet middle with the drums out, 1:17 a variation a semitone higher with a driving string riff, 1:42 the whole brass bends down back into the key for a last statement with the choir. **b, six sections:** 0:06 his motif in canon through cellos, horns and trumpets, 0:32 a slow choir hymn over a falling bass, 0:51 the choir itself sings his motif, 1:04 a variation in G minor with a three-against-four rhythm, 1:30 his motif stretched out in full brass, 1:49 the brass and the choir swap motifs. Recommended a (judged by measure, not by ear: it keeps the sound you picked in round 84 and adds the variation; b is the more adventurous). | [a](../../audio/music/concept/rejected/vorne-last-stand-r85-a.ogg), [b](../../audio/music/concept/rejected/vorne-last-stand-r85-b.ogg) | both loop without a gap; b is about 2 dB quieter around the seam and the stranger of the two | **neither**: "a starts nice, but takes a terrible turn at 1:16. The rhythm is suddenly wrong. It gets better when the normal speeds comes back later. But I'm still not happy with both. It still variations of the same motif played over and over and over and over and over and over. It the same issue with all the new music you keep composing for this act. A motif can be a theme, but you need to build on that theme, not just repeat it over and over with slight changes in tone or speed." | b rejected; a stays `proposed` (its opening to 1:16 liked); re-take in round 86: build on the theme |
| M.4 | [The Ending (track 27)](../../audio/music/README.md) | **pick a or b** (music). **You said (round 84):** "Both tracks suffer from the same problem: the motif is just too short to keep playing in a loop. It seems unfinished that way. You can start with a few repeats while the instruments build up like in b, but then you expect it to continue at about 0:31 and not start over, like it continues in M.5 a at 0:23." **Plays:** once, under the ending pages after Level 50; it does not loop. If the pages last longer, they go on in silence until you move on. **Now:** the motif four times while the instruments join, then new music from about 0:26 that never goes back to the start, and an open ending that fades to silence. **a, 83 s:** the solo horn, strings and piano join; at 0:26 a second tune in F major, 0:38 a climb, 0:51 the full orchestra in D major, 1:04 the Choir's phrase at peace, 1:10 a last chord over a low note that does not resolve. **b, 86 s:** the flute and soft electric piano of the hangar theme grown into an orchestra; at 0:27 a second tune, 0:48 the climb, 0:59 the full statement, 1:09 the Choir's phrase at peace in the flute, 1:15 the open chord. Recommended a (judged by measure, not by ear: it keeps the voice you picked in round 84 and builds from quiet to full; b starts quieter and builds wider). | [a](../../audio/music/concept/rejected/ending-r85-a.ogg), [b](../../audio/music/concept/rejected/ending-r85-b.ogg) | measured, not listened to; the open ending is meant to feel unresolved | **neither**: "a is very good until it suddenly breaks at 1:04. The melody cuts out right in the middle. You can't start a new section without finishing the melody. b has the same issue at 1:08." | b rejected; a stays `proposed` (finish the melody at 1:04); re-take in round 86 |

## Decisions

- 2026-11-05: round 85 opened (follow-up to round 84: the Choir Resonator, the music re-takes M.1, M.3 and M.4).
- 2026-11-05 (decided by me, pending the user's review): track 27 (the ending) is **one play-once piece** (no loop); when
  the ending pages outlast it, the pages go on in silence until the player moves on. Track 34's loop is about 2 minutes.
- 2026-11-05 (user, closed; page answers): **Q.1 a** (the hand plan keeps the beam), **Q.2 approve** (the Resonator's
  document and Level 44's added lines approved), **A.1 a** "the bell" (b rejected). **S.1 and S.2 neither** ("Neither of
  these. These do not read like weapons at all."; "Same, these are like children's toys."): the four sounds rejected.
  **M.1, M.3, M.4 neither** (the notes are verbatim in the rows and the music README): both M.1 takes, M.3 b and M.4 b
  rejected; M.3 a and M.4 a stay `proposed` (change requested), round 84's M.1 a likewise. The recurring complaint: the
  Act 7 music repeats a short motif with changes of tone or speed instead of building on it. **Round 86** re-takes the
  Resonator's sounds (a weapon's shot first, the resonance only a tail) and the three pieces.
