---
title: Concept round 95 — M11 part H "Level 50 Choir Heart" and the ending
design: review
implementation: n/a
art: proposed
depends-on: [../../campaign/act-7-beyond-the-gate/level-50-choir-heart, ../../enemies/bosses/choir-heart, ../../story/characters, ../../audio/sfx, ../../audio/music, ../../audio/voice, ../../ui/briefing, ../../ui/hud, ../../tech/roadmap, ../round-94]
updated: 2026-11-08
---

# Concept round 95 — M11 part H "Level 50 Choir Heart" and the ending

## Summary

M11 part H was built (branch `m11-act-7`, uncommitted; the full `./gradlew check` green on 2026-11-08, Acts 1–6's golden
flights and replays unchanged): Level 50 "Choir Heart", the campaign's last level: the hive core's approach, the
**Choir Heart** built to your boss recipe (each membrane and node its own gun, silent when destroyed), the gate closing
behind the wing, the race home and the near side, and the **campaign's end screens**. This round asks, in this order:
the part's **art**, the **sounds** and **track 20** in place, the **voices** (the Heart's five lines, the ending pages),
the level, **the whole Heart fight**, the gate scene and the end screens in play, **three boss calls** and the
**important calls**, each with its recommendation. The recommendation is pre-selected on every row; leave a row alone to
keep it. Round 94 comes first: it has the echoes and the reworked Chimera.

Decided as trivia, not asked: the walls are screen-space terrain owned by the Heart, so everything that stops at rock
stops at them; the nodes ride their half-wall's inner face and a cut node's half retreats; the darkness is a look only;
a checkpoint restores the Heart at the start of its phase; the Heart's bounty is paid at the kill, as every boss's; the
race's chambers are a dedicated grey strip; the K+32 radio lines have two versions (with and without Rook); the scene
runs on the level clock after the arena; the Heart's death has no hang and a flash 3.5 s after the kill; the heartbeat
is an interval attack of its phase; the seed vault is the draft's "memory organ"; the level's secondary target is 382
kills (65 % of 587), not the 219 the draft said; the two Seraph pairs and the Heralds were moved a little so their
rules hold; the money scale is 0.20.

## Review

The review page is built from this file by `build/round95-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 95`). The game captures are step GH5's, the gate scene and the
end screens re-shot by step FIXGH
([how](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/prompts.md)).

## Art to confirm

Part H's production art (steps ART-H and ART-H2), already in the game: accept, or redo with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.1 | [The Choir Heart](../../enemies/bosses/choir-heart/README.md) | **accept or redo**. The final boss: the crown by phase (closed, closing, opened), the six membranes opening as they sing, the nodes on the walls, the core, the walls with their veins, the wall pits and spit, the collapse chain at its death. Its light pulses with the heartbeat. | [sheet](../../enemies/bosses/choir-heart/concept/choir-heart-final-r95-a.png), [motion](../../enemies/bosses/choir-heart/concept/choir-heart-final-r95-a.gif) | in the arena's dark the walls read only by their lit edge and veins (L.4); the pulse peaks below full white (luminance 231 of 255) | **accept** / redo | |
| F.2 | [The hive-core backdrop](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/README.md) | **accept or redo**. Level 50's approach: the Throat's ribbed tubes, the singing ribs and vein cables of the Choir Chambers, the Brood Halls' tissue floor with its nests, the Last Chamber, all pulsing with the Heart's beat. | [sheet](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/backdrop-final-r95-a.png), [time-lapse](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/backdrop-final-r95-a.gif) | the pulsing light layers are drawn at half size to fit the picture memory, so they are softer than the rest | **accept** / redo | |
| F.3 | [The closing-gate kit](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/README.md) | **accept or redo**. The gate scene: the hive greying, the race down five grey chambers, the folds, the event plane's disc and rim, the near side with Neptune, the *Steadfast*'s group and the ring's arc, the glyphs going dark one by one. | [sheet](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/closing-gate-final-r95-a.png), [motion](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/closing-gate-final-r95-a.gif) | the plane's disc has a straight run of 65 px (inside the limit); the folds sit in a 40 px band at the bottom | **accept** / redo | |
| F.4 | [The seed vault](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/README.md) | **accept or redo**. The level's secret: the Choir's seed vault in the Brood Halls' floor, whole and broken. | [sheet](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/seed-vault-final-r95-a.png), [motion](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/seed-vault-final-r95-a.gif) | — | **accept** / redo | |
| F.5 | [The Choir's face frames](../../story/characters/README.md) | **accept or redo**. The radio frame of the Choir's last line (the Heart's fifth), a face-like pattern forming in the hive's light, 48 frames. | [sheet](../../story/characters/concept/choir-frame-final-r95-a.png), [motion](../../story/characters/concept/choir-frame-final-r95-a.gif) | — | **accept** / redo | |
| F.6 | [Level 50's briefing images and the four ending images](../../ui/briefing/README.md) | **accept or redo**. Okafor's scan of the hive core and Varga's sketch of the Heart with SINGING = OPEN circled; then the ending's four pages: the system plot greyed, the ring's glyphs as a lock's wards, Rook on the flight deck, the dark ring with the listening buoy. | [images](../../ui/briefing/concept/briefing-images-final-r95-a.png) | — | **accept** / redo | |

## Sound

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [The Heart's sounds](../../audio/sfx/README.md) | **ok or redo**. Round 84's picks, now in place: hear them in the fight (L.3). **Heartbeat**: every pulse of its light (every 3 s, every 2 s in phase 3), a huge slow muffled thud, a timpani slowed through a wall. **Membrane**: each song's tell, a dry leather creak. **Closing**: phase 2's start, the walls grinding in, a long rope groan over dragged stone. **Line**: its told shot, a choir rising into a hard electric crack. **Node cut**: a cable snapping, then the wall's groan easing back. **Opening**: phase 3's start, a long fibrous tearing with a rising tone. **Death**: the heartbeat stopping mid-beat, deep collapsing rumbles and a long fading choral tone. | [heartbeat](../../audio/sfx/concept/heart-beat-r84-b.ogg), [membrane](../../audio/sfx/concept/heart-membrane-r84-a.ogg), [closing](../../audio/sfx/concept/heart-closing-r84-a.ogg), [line](../../audio/sfx/concept/heart-line-r84-a.ogg), [node cut](../../audio/sfx/concept/heart-node-cut-r84-a.ogg), [opening](../../audio/sfx/concept/heart-opening-r84-a.ogg), [death](../../audio/sfx/concept/heart-death-r84-a.ogg), [the fight](../../enemies/bosses/choir-heart/concept/heart-fight-r95-a.mp4) | not listened to by Claude: checked by level only; the heartbeat under track 20 may be hard to hear | **ok** / redo | |
| S.2 | [The gate and the hive](../../audio/sfx/README.md) | **ok or redo**. **Gate groan**: as the gate starts to close (K+10.5), a deep metal moan over grinding stone, under the music. **Greying**: the hive's tissue greying under the groan, a long receding wooden creak. **Glyphs**: each glyph going dark, one soft low thump like a far door (quieter each time). **Warp home**: the race's end at K+29, the transit played backwards, a held roar falling away. **Hive heartbeat**: under the approach from the Choir Chambers to the warning, a deep slow thud every 4 s, felt more than heard. **Seed vault**: the secret broken, a hollow knock-crack with dry pods rattling. | [groan](../../audio/sfx/concept/gate-groan-r84-a.ogg), [greying](../../audio/sfx/concept/gate-greying-r84-b.ogg), [glyph](../../audio/sfx/concept/glyph-dark-r84-a.ogg), [warp home](../../audio/sfx/concept/warp-home-r84-a.ogg), [hive heartbeat](../../audio/sfx/concept/vrell-hive-heartbeat-r84-b.ogg), [seed vault](../../audio/sfx/concept/pod-seed-vault-r84-a.ogg), [the gate scene](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/closing-gate-capture-r95-a.mp4) | not listened to by Claude: checked by level only | **ok** / redo | |

## Music

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| M.1 | [Track 20 "Choir Heart" in place](../../audio/music/README.md) | **ok or redo**. The sketch you chose in round 3, extended to full length as decided (QH-1 a): a 4-bar intro and a loop of about three minutes at 150 BPM, every phrase cadenced, the main theme and the Choir's motif alternating. It plays through the fight; at the kill its Choir stem alone takes over (a 1 s crossfade); the coda (22 s, ending on a held D major at K+29, as the ship warps home) starts at K+12. Listen to the full track, the Choir stem and the coda, and to the whole in the fight (L.3) and the gate scene (L.5). | [full](../../audio/music/concept/final-boss-full-r95-a.ogg), [the Choir stem](../../audio/music/concept/final-boss-choir-r95-a.ogg), [the coda](../../audio/music/concept/final-boss-coda-r95-a.ogg), [the fight](../../enemies/bosses/choir-heart/concept/heart-fight-r95-a.mp4) | the Choir's motif is re-harmonised (a Phrygian close) and the two motifs alternate: not listened to by Claude, measured only; the intro ends on a half cadence | **ok** / redo | |

## Voice

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| V.1 | [The Heart's five lines](../../audio/voice/README.md) | **accept or redo**. The Choir's five lines in the fight, in round 84's fearful dress: slower (0.85), the voices staggered, so it sounds afraid; the last, "*…it is behind us… it has always been behind us…*", ends in one voice. In the fight's order: its arrival, "we sang so it would not hear us", "you opened us", "let us through", "it is behind us". | [the five lines](../../audio/voice/concept/level-50-heart-r95-a.ogg), [the cue list](../../audio/voice/concept/level-50-heart-r95-a.txt) | not listened to by Claude: read back by Whisper, the second line needed a hand-picked take; whether the fear reads is for your ear | **accept** / redo | |
| V.2 | [The ending pages](../../audio/voice/README.md) | **accept or redo**. The four pages that close the act, dry: Okafor ("When the Choir died…"), Varga ("The gate was never a door, Commander…"), Rook, Okafor ("Set course for home…"). | [the four pages](../../audio/voice/concept/act-7-ending-r95-a.ogg), [the cue list](../../audio/voice/concept/act-7-ending-r95-a.txt) | page 1 has a pause of about 1.0 s after "Aegis, Okafor." in every take tried (it needed a hand-picked take); Varga's page is slowed on purpose | **accept** / redo | |
| V.3 | [Level 50's other lines](../../audio/voice/README.md) | **accept or redo**. Level 50's two briefing pages and its radio (Okafor, Varga, Rook; the secret; the closing lines after the kill), in order. The radio's noise falls to nothing once the wing is on the near side. | [the lines](../../audio/voice/concept/level-50-lines-r95-a.ogg), [the cue list](../../audio/voice/concept/level-50-lines-r95-a.txt) | not listened to by Claude; the first briefing page and the "Heralds and Seraphs" line needed hand-picked takes | **accept** / redo | |

## The levels in play

Game captures (step GH5; the gate scene and the end screens re-shot by FIXGH): Level 50 flown by the autopilot with the
balance plan's fit and Rook, replayed by the installed game in real time with its sound.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| L.1 | [Level 50: the Throat](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/README.md) | **ok or change**. The hive core's ribbed tubes and the first popcorn waves. | [the Throat](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/throat-capture-r95-a.mp4) | — | **ok** / change | |
| L.2 | [Level 50: the Brood Halls](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/README.md) | **ok or change**. The tissue floor with its nests, the mortars round the seed vault, the vault found. | [the Brood Halls](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/brood-halls-capture-r95-a.mp4) | the floor's pits are not a hazard (sealed, by design) | **ok** / change | |
| L.3 | [The Choir Heart: the whole fight](../../enemies/bosses/choir-heart/README.md) | **ok or change (boss fight)**. The whole fight in real time with sound, the whole screen, 3 minutes: the warning (0:02), the Heart in place (0:13), phase 1 (0:13–1:54: each membrane opens as it sings, parts dying silent one by one), the closing, walls and dark (1:54), phase 2 (to 2:29: the walls closing in, node spit, the reversal at 2:09), the opening, phase 3 (2:29–3:05: the core's storm and line telegraphs), the kill (3:05) and its flash (3:08). Does it feel like the last fight, and is there enough to dodge? | [the fight](../../enemies/bosses/choir-heart/concept/heart-fight-r95-a.mp4) | flown by the autopilot (it dodges, so most fire misses); phase 1 is long here (about 100 of the 172 s); the soft vertical bands of light in phase 3 are the chamber's vein cables lit by the pulse (they stay because the scroll has stopped) | **ok** / change | |
| L.4 | [The arena phase in the dark](../../enemies/bosses/choir-heart/README.md) | **ok or change**. Phase 2 of the same recording (1:54–2:29): the walls closing in, the lights out, the ship's cone and pools, a quarter of the light back for each node cut. Can you see enough to play it? | [the fight, from 1:54](../../enemies/bosses/choir-heart/concept/heart-fight-r95-a.mp4) | the walls are nearly invisible in the dark (only their lit edge and veins show); on easy the dark is lighter | **ok** / change | |
| L.5 | [The gate scene, end to end](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/README.md) | **ok or change**. From the kill: the last line, the hive greying, `THE GATE IS CLOSING`, the race down the chambers, the warp home, the near side with Neptune and the ring's arc, the plane going out, the Steadfast's group. | [the gate scene](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/closing-gate-capture-r95-a.mp4) | the kill's flash hides most of the collapse; Rook flies home here only because this run gave him the Missiles: with the plan's other guns he ejects during the Heart fight (the game's rule for a wingman), so most players will fly the near side alone | **ok** / change | |
| L.6 | [The campaign's end screens](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/README.md) | **ok or change**. From a real save: the debrief, `ACT VII COMPLETE`, the four ending pages, `CAMPAIGN COMPLETE`, the high score entry and the credits' end. | [the end screens](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/campaign-end-capture-r95-a.mp4) | at normal speed, but still a debug run (the end screens can't be reached from a save without one): the card says `AUTOSAVE OFF - DEBUG RUN`; long level names in the act summary (46 GRAVEYARD OF THE CHOIR) now use the small label font, which looks smaller and wider-spaced than the other rows; the act summary shows DATA CORES NONE FOUND although the debrief counts the vault (the playthrough save found none in Act 7) | **ok** / change | |

## Boss calls

Three calls on the Heart, each built as recommended; the recording is L.3.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.1 | [The Heart built to the recipe](../../enemies/bosses/choir-heart/README.md) | **ok or change**. Each of the six membranes and four nodes has its own gun and goes silent when destroyed; the core fires its storm and told lines; the walls' pits spit; when everything of a phase is dead the next opens with new tricks (the walls and the dark, then the opened core). Recommended: ok. | [the fight](../../enemies/bosses/choir-heart/concept/heart-fight-r95-a.mp4) | a ship standing still loses its armour in about 16 s in phase 1 (your recipe's 15 s), but 22–30 s in phase 2 and 4.5 a second in phase 3 | **ok** / change | |
| B.2 | [The Heart's strength](../../enemies/bosses/choir-heart/README.md) | **ok or change**. At the document's 19,900 hit points the fight ran about 310 s, double its 150–180 s window, so they were cut to 14,400 (most of it in the membranes, where a moving ship loses least) and phase 2 was softened. It now takes 156–173 s at medium. Recommended: ok. | [the fight](../../enemies/bosses/choir-heart/concept/heart-fight-r95-a.mp4) | on hard the fight is 178–216 s | **ok** / change | |
| B.3 | [The drift's speed in the halted arena (QH-2)](../../enemies/bosses/choir-heart/README.md) | **pick a or b**. In phase 2 the chamber drifts sideways. **a** (recommended, in the game now): gently, 60 px/s, at most 16 px/s sideways, the reversal flips it for 3 s. **b**: like the act's other sections, 100 px/s, at most 26 px/s sideways, harsher between the walls. | [the fight, from 1:54](../../enemies/bosses/choir-heart/concept/heart-fight-r95-a.mp4) | only a is built; b is a number change | **a** / b | |

## Important calls

Calls the part H steps made where the documents left room, each built as recommended. Accept, or change with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [The `THE GATE IS CLOSING` banner](../../ui/hud/README.md) | **accept or change**. The HUD has no event line, so the closing gate shows a neutral banner in the warning banner's shape for 3 s: the event plane's violet-white on a dark band, neither the hostile amber nor the friendly green. Recommended: accept. | [the gate scene](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/closing-gate-capture-r95-a.mp4) | — | **accept** / change | |
| C.2 | [A denser Level 50](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/README.md) | **accept or change**. With the planned 69 waves the screen was too empty (1.5 enemies at once), so 59 waves of the act's mix were added (every second one left out on hard): about 190 enemies a minute and 3 on the screen at medium. Recommended: accept (the final act's rule). | [the Throat](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/throat-capture-r95-a.mp4) | — | **accept** / change | |
| C.3 | [The level's end at K+62](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/README.md) | **accept or change**. The draft ended the level at K+58; the closing lines (Varga at K+39.5, Rook at K+51, Okafor at K+58) need until K+62, so the level ends then and the debrief follows. Recommended: accept. | [the gate scene](../../campaign/act-7-beyond-the-gate/level-50-choir-heart/concept/closing-gate-capture-r95-a.mp4) | — | **accept** / change | |
| C.4 | [Level 50 uses 7 picture sheets](../../tech/architecture/README.md) | **accept or change**. The level's pictures need seven texture sheets at once, like Levels 28 and 49 (the budget's exception), after the glowing layers and the race strip were halved to fit; the game plays the same. Recommended: accept. | — | measured only on Linux: tell me if it stutters on your Mac | **accept** / change | |

## Decisions

- 2026-11-08: round 95 opened (M11 part H built, steps H1a–H4, ART-H, ART-H2, MUS-H, VOX-GH, GH5, FIXGH): F.1–F.6 part H's
  art to confirm, S.1–S.2 the Heart's and the gate's sounds, M.1 track 20, V.1–V.3 the Heart's lines, the ending pages and
  Level 50's lines, L.1–L.6 Level 50, the whole Heart fight, the gate scene and the end screens in play, B.1–B.3 the
  Heart's boss calls (B.3 is QH-2), C.1–C.4 the important calls; trivia in the summary.
