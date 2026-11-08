---
title: Concept round 96 — M11 part I "Close-out and v1.0"
design: review
implementation: n/a
art: n/a
depends-on: [../../tech/release, ../../tech/roadmap, ../../campaign/act-7-beyond-the-gate, ../../audio/voice, ../../player/weapons/choir-resonator, ../../ui/mission-select, ../round-94, ../round-95]
updated: 2026-11-08
---

# Concept round 96 — M11 part I "Close-out and v1.0"

## Summary

M11 part I closes the campaign (branch `m11-act-7`; the steps I1, I2, I3a, VOX-I, REL-I and DOC-I are done, the full
`./gradlew check` green; `v1.0` is not tagged). All fifty levels now fly as one campaign on every difficulty, into the
end card and the completed save; the balance tests cover Levels 01–50; the Choir Resonator's behaviour tests are in; the
Act 7 voices were read back; the `1.0.0` bundles were built and run on Linux. This round asks, in this order: **the Act 7
listen-through**, **your playthrough of Act 7 on the Mac bundle** (the last step before the tag), **Act 7's pay**, **four
documents to approve as built**, and **three calls**. The recommendation is pre-selected on every row; leave a row alone
to keep it. Rounds 94 and 95 come first; the merge and the signed `v1.0` tag are asked directly after the playthrough's
fixes, not here.

Decided as trivia, not asked: the version is `1.0.0` and the tag `v1.0` (a fix release is `v1.0.1`); the release's end
(the "more levels follow" screen) stays in the code for builds short of Level 50, pinned by tests, but the campaign never
reaches it; the unused vocabulary rows stay in the enemies README marked "not used by the campaign (v1.0)"
(`grid`, `wall`, `solo set piece`, `surface`, and `submerged ambush`, closed unused); Level 50's Rook ejecting in the
Heart fight is printed by the playthrough test, not failed (he leaves in normal play, on all three difficulties); the
golden flights for Levels 36–50 are recorded last, after the playthrough's fixes; no Apple silicon "untested" mark in the
release notes (you have played on an Apple silicon Mac since 2026-10-30).

## Review

The review page is built from this file by `build/round96-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 96`). The listen files are step VOX-I's
([how](../../audio/voice/concept/prompts.md)); the Mac bundle is step REL-I's.

## Voice

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| V.1 | [The Act 7 listen-through](../../audio/voice/README.md) | **ok or note**. Every voiced line of Act 7 (201 files: briefing pages, radio, the ending), one joined file per level in game order, with its cue list (line, speaker, where it plays). Every line was read back by a speech recogniser with Act 7's name prompt and compared with its text; one line was really wrong and is fixed (Rook, Level 44 at 1:38: the take added a "it", now word for word). Listen as you play: name the level and line in a note if one is wrong. Recommended: ok. | [the Act 7 intro](../../audio/voice/concept/act-7-intro-listen-r96-a.ogg) ([cues](../../audio/voice/concept/act-7-intro-listen-r96-a.txt)), [Level 43](../../audio/voice/concept/level-43-listen-r96-a.ogg) ([cues](../../audio/voice/concept/level-43-listen-r96-a.txt)), [Level 44](../../audio/voice/concept/level-44-listen-r96-a.ogg) ([cues](../../audio/voice/concept/level-44-listen-r96-a.txt)), [Level 45](../../audio/voice/concept/level-45-listen-r96-a.ogg) ([cues](../../audio/voice/concept/level-45-listen-r96-a.txt)), [Level 46](../../audio/voice/concept/level-46-listen-r96-a.ogg) ([cues](../../audio/voice/concept/level-46-listen-r96-a.txt)), [Level 47](../../audio/voice/concept/level-47-listen-r96-a.ogg) ([cues](../../audio/voice/concept/level-47-listen-r96-a.txt)), [Level 48](../../audio/voice/concept/level-48-listen-r96-a.ogg) ([cues](../../audio/voice/concept/level-48-listen-r96-a.txt)), [Level 49](../../audio/voice/concept/level-49-listen-r96-a.ogg) ([cues](../../audio/voice/concept/level-49-listen-r96-a.txt)), [Level 50](../../audio/voice/concept/level-50-listen-r96-a.ogg) ([cues](../../audio/voice/concept/level-50-listen-r96-a.txt)), [the ending](../../audio/voice/concept/act-7-ending-listen-r96-a.ogg) ([cues](../../audio/voice/concept/act-7-ending-listen-r96-a.txt)) | what is left is known: the recogniser mishears names and near-homophones ("Aegis" as "ages", "Seraphs" as "serifs", "buoy" as "boy", "Wyrm's" as "worms"), every radio line is harder to read back under the noise steps (steps 3–5, four lines misread), Vorne's fused lines are not read back at all, and the ending's page 1 keeps a pause of 1.0 s after "Aegis, Okafor." in every take (your ear decides) | **ok** / note | |

## Playthrough

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| P.1 | [Play Act 7 on the Mac bundle (QI-1)](../../tech/release/README.md) | **yes or no**. Play Act 7 on the Apple silicon bundle (`build/release-1.0/terran-vanguard-1.0.0-macM1.zip`, `HOW-TO-RUN.txt` beside it, and the saves `saves-L43`, `saves-L49`, `saves-L50` to start in the act, at its middle and at its end) instead of from source, and tell me the frame rate at **Level 49's rush** (the tinted memory, three bosses in a row) and **Level 50's dark phase** (the lights out, pulse lights on every layer). It is the one thing I cannot measure here: Act 7 adds the heaviest frames (the tint, the dark, the pulse lights). On Linux (software renderer) Level 50's phase 2 costs about 1.9 times a Level 42 frame (10.8 ms against 5.8 ms; Level 49's rush 7.7 ms): that is the reference. Recommended: yes. | — | the numbers are from a software renderer; a real GPU should be faster, but only your Mac can say; the bundle is unsigned (the how-to has the one Terminal line) | **yes** / no | |

## Balance

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.1 | [Act 7's pay on hard (QI-2)](../../systems/economy/README.md) | **pick a or b**. With the balance plan's purchases, hard ends Act 7 29.4 % under the expert's bound (easy +5.7 %, medium −1.8 %, inside). Almost all of it is Act 6's deficit carried in: Act 7's own share of the bound is met at 99.1 %. **a** (recommended): accept as pending, as Acts 3–6 (the bound stays printed in the playthrough test). **b**: raise Act 7's pay, so a hard player has more to buy with in the last act. | — | the bound is an expert's, not a player's: no hard run fails; **b** moves the money of Levels 43–50 and so their tuning | **a** / b | |
| B.2 | [The accepted exceptions, restated](../../tech/roadmap/README.md) | **accept or change**. Rounds 89–93 already accepted these; the tests now say so: the beam's mean time-to-kill under the 0.9 rule on Levels 44, 45, 46, 47 and 48 (0.80, 0.69, 0.65, 0.66, 0.62 times; Level 48's 0.62 is entered as accepted with round 93's close, my call), Levels 45 and 46's three quiet windows on easy, the one-off pacing exceptions. Still pending, in their own rounds: Level 50's 0.83 (round 95) and the Chimera's time-to-kill (round 94). Recommended: accept. | — | — | **accept** / change | |

## Documents to approve

Four documents whose art and play you already accepted (rounds 90, 91 and 93, and the end screens' route), still marked "review". Approve them as built, or say what to change.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| D.1 | [The reactor core](../../enemies/ground/reactor-core/README.md) | **approve or change**. The live gold cores in the citadel's wrecks, Level 48's secondary target (eight to burn), as built (round 93). Recommended: approve. | — | — | **approve** / change | |
| D.2 | [The spawning organ](../../enemies/ground/spawning-organ/README.md) | **approve or change**. Level 45's organ that releases the swarm, as built (round 90). Recommended: approve. | — | — | **approve** / change | |
| D.3 | [The seed casket](../../enemies/space/seed-casket/README.md) | **approve or change**. Level 46's casket, as built (round 91). Recommended: approve. | — | — | **approve** / change | |
| D.4 | [The mission select](../../ui/mission-select/README.md) | **approve or change**. The replay screen, with every mission open once the campaign is complete (built in part A, in play from Level 50's end), as built. Recommended: approve. | — | — | **approve** / change | |

## Important calls

Calls part I made where the documents left room, each built as recommended. Accept, or change with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [The Resonator's wave and a unit's parts](../../player/weapons/choir-resonator/README.md) | **accept or change**. When the Choir Resonator's wave front reaches a unit's body and one of its parts equally near (a turret on a hull, a node on a body), it hits the part, as a beam does; before, it hit only the body and the parts were ignored. Recommended: accept. | — | changes only units with standing parts; no recorded flight moved | **accept** / change | |
| C.2 | [The bundle's README for the whole campaign](../../../desktop/bundle/README.txt) | **accept or change**. The text in every bundle: "This release holds the whole campaign: seven acts, fifty levels … A save from the previous release (Acts 1 to 6) carries on where it stopped: Continue opens Act 7's intro. After the campaign every mission stays open for replay in Missions." No stop is named any more. Recommended: accept. | — | — | **accept** / change | |
| C.3 | [The release notes for `v1.0`](../../tech/release/release-notes.md) | **ok or change**. What the GitHub release says: the whole campaign, Act 7 and its new shop items, the ending, a `v0.4` save carrying on into Act 7's intro, the download table, the unsigned-game steps. Read it. Recommended: ok. | — | the file has two placeholders the release job fills in (`@version@`, `@tag@`) | **ok** / change | |
| C.4 | [The rest of the trivia](../../tech/release/README.md) | **accept or change**. The one-line list in the summary above (version, the release's end kept as code, the unused vocabulary rows, golden flights last, Rook's ejection printed, no Mac mark). Recommended: accept. | — | — | **accept** / change | |

## Decisions

- 2026-11-08: round 96 opened (M11 part I, steps I1, I2, I3a, VOX-I, REL-I, DOC-I): V.1 the Act 7 listen-through, P.1 the
  Mac playthrough (QI-1), B.1–B.2 Act 7's pay (QI-2) and the restated exceptions, D.1–D.4 the four documents to approve,
  C.1–C.4 the calls; trivia in the summary.
