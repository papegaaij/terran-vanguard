---
title: Concept round 89 — M11 part B "the Act 7 intro and Levels 43–44"
design: review
implementation: n/a
art: proposed
depends-on: [../../campaign/act-7-beyond-the-gate, ../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate, ../../campaign/act-7-beyond-the-gate/level-44-crossing, ../../enemies/space/gate-warden, ../../enemies/space/rift-skater, ../../audio/sfx, ../../audio/music, ../../ui/briefing, ../../tech/roadmap, ../round-88]
updated: 2026-11-05
---

# Concept round 89 — M11 part B "the Act 7 intro and Levels 43–44"

## Summary

M11 part B was built (branch `m11-act-7`, uncommitted; the full `./gradlew check` green on 2026-11-05, Acts 1–6's
golden flights and replays unchanged): the Act 7 title card and intro, Level 43 "The Tether Gate" with the Gate Wardens,
the glyph stones and the gate transit, Level 44 "Crossing" with the Rift Skaters, the drift and its two reversals, the
gate kit and the event-plane backdrop, tracks 16 and 17 by section and the noisy radio. This round asks, in this
order: the gate's ambience **bed** (a/b), tracks 16 and 17 **in place**, part B's **art to confirm**, the two **levels in
play** from game captures, and the **important calls**, each with its recommendation. The recommendation is
pre-selected on every row; leave a row alone to keep it. The captures are silent.

**Found while capturing, fixed after:** Continue on a save at Level 43 crashed the game when the Act 7 intro
opened (no briefing name plate for the Choir's page 3). The plate now reads `THE CHOIR` over `VRELL` (a test checks
every act intro's speakers); the intro and title card were captured before the fix, so F.6–F.8 show the art itself.

Decided as trivia, not asked: the transit's formation point (240, 96), eased over 2 s; the warp's violet `#A798CC`; the
radio portrait's static 0.1 a noise step; the warp's roar from t=264.5; Level 43's and 44's radio lines moved by up to
8 s so none starts more than 1 s late, the three long briefing pages shortened to one screen and re-voiced; the ring's
pieces kept from showing a cut edge (the mouth's canvas made taller, the near arcs held above the screen's top).

## Review

The review page is built from this file by `build/round89-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 89`). The game captures are step B5's `--shots` screenshots
([how](../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/prompts.md#game-captures-r89-a)).

## Sound

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [The `vrell-gate` bed (QB-1)](../../audio/sfx/README.md) | **pick a or b**. Plays under the whole of Levels 43 and 44, below the music (in Level 44 with the event plane's groan on top). It should sound like open space heard through the ship's hull with a slow resonance from the stone ring. **a** (recommended, in the game now): a low hull room tone with a slow bowed-stone drone under it, steady. **b**: a thinner, airy space tone with a distant deep stone hum that swells every 8 s. | [a](../../audio/sfx/concept/vrell-gate-r89-a.ogg), [b](../../audio/sfx/concept/vrell-gate-r89-b.ogg) | not listened to by Claude: checked by level, peaks and loop seams | **a** / b / neither | |

## Music

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| M.1 | [Track 16 "Far Shore" in Level 43](../../audio/music/README.md) | **ok or redo**. Round 86's chosen take cut into its stems and played by section: the intro over the mouth, the base from the pickets, the full mix from the ring's shadow, the Choir's stem from its line at t=208, faded out at the transit. Listen to the three files: the full track, the base stem and the Choir's stem. | [full](../../../assets/music/far-shore.ogg), [base](../../../assets/music/far-shore-base.ogg), [choir](../../../assets/music/far-shore-motif.ogg) | the stems sum back to the take within −44 dB | **ok** / redo | |
| M.2 | [Track 17 "Hollow Light" in Level 44](../../audio/music/README.md) | **ok or redo**. Round 87's chosen take cut into its stems: the intro and base over the far mouth, the full mix from the current to the narrows, the base again out of the gate, ducked under each reversal's groan. The sparse stem is Level 46's (C.6). | [full](../../../assets/music/hollow-light.ogg), [base](../../../assets/music/hollow-light-base.ogg), [sparse](../../../assets/music/hollow-light-sparse.ogg) | — | **ok** / redo | |

## Art to confirm

Part B's production art (step ART-B), already in the game: accept, or redo with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.1 | [The Gate Warden](../../enemies/space/gate-warden/README.md) | **accept or redo**. Round 84's "idol" made final: the closed shell's sweep, the iris opening in six frames, the eye charging, the shatter; in the game on Level 43. | [frames](../../enemies/space/gate-warden/concept/gate-warden-final-r89-a.png), [motion](../../enemies/space/gate-warden/concept/gate-warden-final-r89-a.gif), [lines in the game](../../enemies/space/gate-warden/concept/warden-lines-capture-r89-a.gif) | a bright white flower-shaped flash when a shell opens or a Warden escapes reads as a solid blob ([t=53.0](../../enemies/space/gate-warden/concept/warden-leaving-capture-r89-a.png)) | **accept** / redo | |
| F.2 | [The Rift Skater](../../enemies/space/rift-skater/README.md) | **accept or redo**. The manta shard in 16 headings, its fade, the violet landing ring, the death flakes; in the game on Level 44. | [frames](../../enemies/space/rift-skater/concept/rift-skater-final-r89-a.png), [motion](../../enemies/space/rift-skater/concept/rift-skater-final-r89-a.gif), [in the game](../../enemies/space/rift-skater/concept/skater-drift-capture-r89-a.gif) | small and dark over the pale plane, but it reads | **accept** / redo | |
| F.3 | [The gate kit](../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/README.md) | **accept or redo**. Level 43's backdrop: the nebula, the ring at three sizes cross-faded by section, the fleet's lights, spore sacs and dead tendrils, the ring's surface strip, the event plane in the mouth. | [sheet](../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/backdrop-final-r89-a.png), [time-lapse](../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/backdrop-final-r89-a.gif) | the mouth and the near arcs were redrawn in B5 so no cut edge shows | **accept** / redo | |
| F.4 | [The event plane](../../campaign/act-7-beyond-the-gate/level-44-crossing/README.md) | **accept or redo**. Level 44's backdrop: the pale violet plane with dark streaks sliding with the drift, the ribbons and streaks, the parting curtain on Vrell space and a far reef. | [sheet](../../campaign/act-7-beyond-the-gate/level-44-crossing/concept/backdrop-final-r89-a.png), [time-lapse](../../campaign/act-7-beyond-the-gate/level-44-crossing/concept/backdrop-final-r89-a.gif) | — | **accept** / redo | |
| F.5 | [The glyph stone, the Helix pod and the gold shard](../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/README.md) | **accept or redo**. Level 43's stone (intact, cracked, split) and the pod tumbling; Level 44's gold shard tumbling and breaking. | [L43 props](../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/l43-props-final-r89-a.png), [L43 motion](../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/l43-props-final-r89-a.gif), [L44 shard](../../campaign/act-7-beyond-the-gate/level-44-crossing/concept/l44-props-final-r89-a.png), [stones in the game](../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/glyph-stones-capture-r89-a.png) | — | **accept** / redo | |
| F.6 | [The act still and title card](../../campaign/act-7-beyond-the-gate/README.md) | **accept or redo**. "The mouth": the ring head-on with the event plane in it, and the title card over it. | [still and card](../../campaign/act-7-beyond-the-gate/concept/act-7-still-final-r89-a.png) | not seen in the game (the intro crash above) | **accept** / redo | |
| F.7 | [The eight intro and briefing images](../../ui/briefing/README.md) | **accept or redo**. The Act 7 intro's four images and Levels 43's and 44's briefing images. | [images](../../ui/briefing/concept/briefing-images-final-r89-a.png) | not seen in the game (the intro crash above) | **accept** / redo | |
| F.8 | [The Choir's portrait and the intel pictures](../../story/characters/the-choir/README.md) | **accept or redo**. The Choir's glyph on its intercepted briefing page (round 08's loop at its brightest), and the hangar's intel pictures of the Warden and the Skater. | [the Choir](../../../assets/sprites/portraits/briefing-the-choir-neutral.png), [intel](../../ui/hangar/concept/intel-final-r89-a.png) | — | **accept** / redo | |

## The levels in play

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| L.1 | [Level 43 The Tether Gate](../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/README.md) | **ok or change**. Warden lines opening in turn (straight, then aimed), the Tesla arc holding a shell open, a Warden's leaving tell and escape, the glyph stones, and the transit with its brightness rising one way only. | [lines](../../enemies/space/gate-warden/concept/warden-lines-capture-r89-a.gif), [the arc](../../enemies/space/gate-warden/concept/warden-arc-capture-r89-a.gif), [leaving](../../enemies/space/gate-warden/concept/warden-leaving-capture-r89-a.png), [stones](../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/glyph-stones-capture-r89-a.png), [transit](../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/transit-capture-r89-a.png), [transit, motion](../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/transit-capture-r89-a.gif) | the arc clip flies the ship up into the Wardens (invulnerable) to show the arc; the amber blink of the `WARDENS` box falls between the 0.25 s frames | **ok** / change | |
| L.2 | [Level 44 Crossing](../../campaign/act-7-beyond-the-gate/level-44-crossing/README.md) | **ok or change**. Skater streams and their rings in the drift, a Coilwyrm swirling, reversal 1 with its chevrons and rear wave, a Choir Herald wave, and the Choir Resonator L5 over the brightest band. | [Skaters](../../enemies/space/rift-skater/concept/skater-drift-capture-r89-a.gif), [chain](../../campaign/act-7-beyond-the-gate/level-44-crossing/concept/chain-capture-r89-a.gif), [reversal](../../campaign/act-7-beyond-the-gate/level-44-crossing/concept/reversal-capture-r89-a.gif), [Herald](../../campaign/act-7-beyond-the-gate/level-44-crossing/concept/herald-capture-r89-a.png), [Resonator](../../player/weapons/choir-resonator/concept/resonator-band-capture-r89-a.gif) | clips at 10 frames a second to keep them small | **ok** / change | |

## Important calls

Calls the part B steps made where the documents left room, each built as recommended. Accept, or change with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [Denser Levels 43 and 44](../../campaign/act-7-beyond-the-gate/README.md) | **accept or change**. Both levels were made denser with more kinds of enemy, as you asked; hard is much harder now (on Level 43's hard, the autopilot kills all 18 Wardens in 1 of 6 runs). Recommended: accept. | — | — | **accept** / change | |
| C.2 | [Level 44's small units](../../campaign/act-7-beyond-the-gate/level-44-crossing/README.md) | **accept or change**. Level 44's small units still die a little faster than the rule wants (0.80× of Level 10's life against 0.9×: its Skaters and Lampreys). Recommended: accept. | — | — | **accept** / change | |
| C.3 | [Act 7's small units and the reversals](../../campaign/act-7-beyond-the-gate/README.md) | **accept or change**. Act 7's small units have 2.5× their base HP, and a reversal counts as a scripted pause in the pacing check. Recommended: accept (kept as built). | — | — | **accept** / change | |
| C.4 | [The shared radio lines](../../audio/voice/README.md) | **accept or change**. Rook's barks and the specials' calls use shared voice files, so they play without the level's radio static. Recommended: accept. | — | — | **accept** / change | |
| C.5 | [The gate ring's glyphs](../../world/vrell-space/README.md) | **accept or change**. The ring's glyphs are teal-white, as the Vrell document says, not crimson as the act's brief said. Recommended: accept. | — | — | **accept** / change | |
| C.6 | [Track 17's sparse stem](../../audio/music/README.md) | **accept or change**. Level 46's sparse version of track 17 is its heartbeat and sub-bass only. Recommended: accept. | — | — | **accept** / change | |

## Decisions

- 2026-11-05: round 89 opened (M11 part B built, step B5): S.1 the `vrell-gate` bed a/b (QB-1), M.1–M.2 tracks 16 and 17
  in place (QB-2), F.1–F.8 part B's art to confirm, L.1–L.2 the levels in play from game captures, C.1–C.6 the
  important calls; trivia in the summary; the Act 7 intro's crash on Continue found while capturing (fixed after by the main agent).
