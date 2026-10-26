---
title: Concept round 59 — M8 part I, the round 58 fixes and the close-out of Act 4
design: review
implementation: n/a
art: proposed
depends-on: [../../campaign/act-4-deep-water, ../../campaign/act-4-deep-water/level-28-abyssal-maw, ../../enemies/bosses, ../../enemies/bosses/abyssal-maw, ../../audio/sfx, ../../audio/voice, ../../systems/economy, ../../art-direction, ../../tech/roadmap, ../round-58]
updated: 2026-10-26
---

# Concept round 59 — M8 part I, the round 58 fixes and the close-out of Act 4

## Summary

The last round of M8 ([roadmap](../../tech/roadmap/README.md#m8-parts)): the fixes round 58 asked for and part I,
the close-out. **Act 4 is complete**: the campaign runs from Level 01 through the Abyssal Maw, the breakout, the act's
end and the release's end card, and you can play Act 4 through. A save to start from: `./gradlew
:content:playthroughSave -Plevel=22 -Pout=<dir>` (the Level 22 hangar, Act 4's intro first) or `-Plevel=28` (the Level
28 hangar), then `./gradlew :desktop:run --args="--settings <dir>/settings.properties"` and Continue.

What changed since round 58: the [Abyssal Maw](../../enemies/bosses/abyssal-maw/README.md)'s motion is redone (it
swims in and winds round the arena, rounded corners, the segments turning through every bend and the sweeps' bights,
the body leaving through the top after the lunge and winding back in; step I1); the
[cruisers](../../enemies/bosses/abyssal-maw/README.md#the-cruisers-draft) break up through the intact night ice where
they are, and the breakout's burst starts where and when the ship breaches the ice (step I2); the three b sounds you
picked (the implants' zap, the Maw's death, the cruisers' rise) play in the game, and Act 4 has one ambience bed per
setting (`europa-ice` on the surface, `europa-under` under water). Part I (step I3): the playthrough and balance tests
run through Level 28 on the levels' own income (part A's temporary income removed), Act 4's variety checked as built,
the `later` tags moved to Act 5, and every voiced Act 4 line checked against a transcript.

Answer the rows below: **R.1–R.3** the redone art with game captures (accept or redo), **V.1** the listen-through of the
119 voiced Act 4 lines (accept, or list the lines to redo), **Q.1** the ambience beds' volume, and seven calls the build
made (the recommendation pre-selected). The small rules decided on the way are in the parts' *Decisions* logs, marked
"pending the user's review in round 59" (collected in
[Level 28's](../../campaign/act-4-deep-water/level-28-abyssal-maw/README.md#decisions) step I4 entry); they are not
asked here.

## Review

The review page is built from this file by `build/round59-tools/build_review.py` (untracked); the board is
[index.html](index.html). The captures are game recordings with the game's own sound (Xvfb, `--bench`, medium,
`--invulnerable`, auto-fire, the balance plan's Level 28 fit: Harpoon Torpedoes L4, the Tail Gun L3 on the rear, two
Micro-Missile Pods L3, the Mk IV, the Shield Mk III, Composite III, the Pressure hull, the Pickup magnet, the Sonar
Pulse, the Bulwark loaner); the sound is the game's output through OpenAL Soft's wave writer, muxed into the video. The
Maw's and the cruisers' captures come from one run with the ship unsteered (the fight lasts 1:57, par 2:18); the burst's
from four runs with the ship held at the left, in the middle, at the right and weaving left-right and up-down (the
keys held from just after the kill). Their PNGs are frames of the play field at 1:1 side by side, labelled with the
fight's second (from the warning) or the level's clock.

## In the game

The redone motion and the cruisers' rise are in the game, as are the three round 58 b sounds and the two Europa beds.
The frame rate of a full medium Level 28 run on the software renderer (Xvfb 1280×720, llvmpipe, no capture):
**364 fps** on average (round 58: 399); the longest frame (93 ms) is the level's first frame, the first-time loads; no frame of 50 ms or more after it. The full `./gradlew check` is green; `tools/balance.py` reports no problem; Level 28's numbers
(`levelReport`, three seeds): medium 147 enemies, 89 a minute, 4.9 on the screen, no gap over 2 s, the haul on the
budget (+0.0 %), the autopilot wins 3 of 3 on every difficulty.

## Art — redo

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| R.1 | [The Abyssal Maw's motion](../../enemies/bosses/abyssal-maw/README.md#arena) | **accept or redo** (round 58 F.2, your note: "The creature just appears, a snake should wrap around you. The corners are just sharp 90 degree angles. The bump does not rotate the segments. After the creature runs through the middle, it teleports around you. Make it exit the screen entirely and then fold around you like at the (improved start)."). What changed (step I1): **the arrival**: a second after the warning the head swims in from off the screen at the top-right corner, just under the play plane, and the body follows it segment by segment down the right edge, along the bottom, up the left edge and along the top; the head bends down into its place, rises onto the play plane and the fight starts (5 s, the gill sacs turning with the head). **Rounded corners**: every corner of the coil's path is a 90 px arc (the neck into the top run 55 px), every segment faces along the line where it lies. **The bight**: a sweep bends the body along its length and every segment in it turns with the bend, every frame, round a corner too. **The dive**: the coil slides off the edges into the dark, the head sinks and fades; after the lunge the head races on out of the top and the body streams up the column after it until the tail has left the screen, then it winds back in the same way as the arrival (3 s) | [capture: the arrival](../../enemies/bosses/concept/maw-arrival-capture-r59-a.png), [video: the arrival](../../enemies/bosses/concept/maw-arrival-capture-r59-a.mp4), [capture: a sweep's bight](../../enemies/bosses/concept/maw-sweep-capture-r59-a.png), [capture: the dive and the way back](../../enemies/bosses/concept/maw-dive-capture-r59-a.png), [video: the dive to the return](../../enemies/bosses/concept/maw-dive-capture-r59-a.mp4), [capture: the rounded corners](../../enemies/bosses/concept/maw-corners-capture-r59-a.png) | at its fullest swing the bight bends hard: its segments turn up to about 40° from their neighbours, so for the 0.8 s hold it reads as a zig-zag of plates with gaps between them rather than a smooth curve (the sweep capture, 48.3–48.9 s); the 3 s wind-in is fast (the head crosses the whole field in about a second a side); the tail's last three segments rest off the top edge at the top right, so the coil runs off the screen there | **accept** | accepted: the Maw's art is final |
| R.2 | [The cruisers' rise](../../enemies/bosses/abyssal-maw/README.md#the-reveal-the-breakout-and-the-run-q10-a) | **accept or redo** (round 58 F.3, your note: "The cruisers seem to ascent from underneath the ice, yet they pop up at places where's no ice. The appearance must align with the backdrop."). What changed (step I2): the backdrop's two open leads (open water before the hulls came up) are gone; each cruiser breaks up through the **intact night ice where it is**: its crust is the ground's own ice, lifting in the hull's shape, splitting and sheeting off as the hull rises; the hull moves with the ice at first and slows to the wing's pace by half the rise (2 s); its hole (black water, plates, a white rim, cracks running out) stays on the ice where it broke through. The run's lanes are unchanged | [capture: the rise](../../enemies/bosses/concept/cruisers-rise-capture-r59-a.png), [capture: the crust and the hole](../../enemies/bosses/concept/cruisers-hole-capture-r59-a.png), [video: the breakout and the rise with the reveal sting](../../enemies/bosses/concept/cruisers-rise-capture-r59-a.mp4) | the hole is hardly seen: the hull covers it while the ground scrolls it out of the bottom edge, so only its rim shows at the stern for about a second; the crust starts near the top edge, so the rise is seen from above the ship rather than beside it (see C.4) | **accept** | accepted: the cruisers' art is final |
| R.3 | [The breakout's burst](../../campaign/act-4-deep-water/level-28-abyssal-maw/README.md#layout) | **accept or redo** (round 58 F.4, accepted with your note: "The effect is good, but the timing is off. It needs to be timed exactly when this ship breaches the surface. This is not fixed timing, because the ship can be anywhere on the screen and even move at that moment."). What changed (step I2): the ceiling's burst and its sound start on the frame the shaft mouth's lip reaches the ship's nose, wherever the ship is, at the point on the lip where it crossed; the burst stays anchored there on the ice, scrolling with the ground, while the ship flies on. The lip is the bore's circle, so a ship at a flank or high on the screen breaches earlier than one low in the middle (see C.5). Tested for a ship at the left, in the middle, at the right and moving | [capture: left, middle, right, moving](../../campaign/act-4-deep-water/level-28-abyssal-maw/concept/breakout-burst-capture-r59-a.png), [video: a moving ship](../../campaign/act-4-deep-water/level-28-abyssal-maw/concept/breakout-burst-capture-r59-a.mp4) | — | **accept** | accepted |

## Voices

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| V.1 | [The Act 4 listen-through](listen-through.md) | **listen** to every voiced Act 4 line in play order: the act intro, Levels 22–28 (the briefing pages, the radio cues, the secret) and the act outro, **119 lines, 14 minutes**, grouped by level below with their speaker, cue and text. The **8 lines marked listen** come first: there the transcript (Whisper, the renderer's model) differs on a word the line needs, or the delivery is unusually slow. No line clips, every line ends quietly (the two cut-off lines end in their static as designed), every line's first and last word are heard. Rook's barks and the other calls are unchanged since round 45. **Answer once**: accept all, or redo and name the lines in the note (e.g. "L27 #6") | [the list](listen-through.md) | Claude has not listened; the marks are a transcript's hints, not verdicts | **accept** | accepted: the 8 flagged lines as they are |

## The ambience beds

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| Q.1 | [The ambience beds' volume](../../audio/sfx/README.md#ambience-per-setting) | **raise or leave.** You found the beds hard to hear. Measured in the game's mix, every act's beds sit **24.7–29.5 dB under the level's theme**: Act 4's Europa beds −27.9 LUFS against the themes' −13.9, the loudest of all acts (Earth arctic as loud; the Martian dust wind the faintest, 29.5 dB under). The game plays every bed at `AMBIENCE_VOLUME` 0.35 on the effects bus. **a**: raise every act's beds by **6.0 dB** (`AMBIENCE_VOLUME` 0.35 → **0.70**, one constant, every bed alike, so the acts keep their balance; Level 13's and 18's storm swells ride on top as before). **b**: leave them as a faint background. **c**: raise them by 9.1 dB (0.35 → 1.0, the most a sound's volume allows; anything louder means re-rendering the bed files). **Listen** (Level 24's first 20 s as the game mixes them, recorded from the game's own output, the bed `europa-under` with track 10, the radio and the guns; each player another run of the same seconds): a as it is, +6 dB, +9.1 dB. Measured: in the level's first seconds, before the music and the guns, the bed's low band rises 4.8 dB (b) and 7.6 dB (c); from about t=5 on the music and the fights cover it and the mix changes by under 0.5 dB. **Recommendation: a** (0.70) | [as it is](../../audio/sfx/concept/rejected/beds-mix-l24-r59-a.ogg), [+6 dB](../../audio/sfx/concept/beds-mix-l24-r59-b.ogg), [+9.1 dB](../../audio/sfx/concept/rejected/beds-mix-l24-r59-c.ogg) | under the music and the guns even +9.1 dB is barely heard: a bed is heard in the quiet stretches (a level's start, the pauses before a boss, the arenas' gaps), not in a fight | **a** | a: built (`AMBIENCE_VOLUME` 0.70); b and c not taken |

## Calls the build made

Seven of the rules the steps had to decide where the approved documents left a gap or a choice. Each is built as
described; the recommendation is pre-selected. The rest are in the parts' *Decisions* logs (Level 28's step I4 entry
collects them).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [The Maw's par](../../enemies/bosses/abyssal-maw/README.md#duration-and-hp) | **keep the par at 138 or trim the HP**: the motion redo keeps the head out of the guns' reach about 5 s longer (it leaves through the top after the lunge and winds back in under the play plane), so the fight runs longer at the approved HP: `AbyssalMawTest` (five seeds, as every boss's par is set) **138.4 s** at medium (120–163, every seed in the window), the par moved 127 → **138**; over sixteen whole-level seeds the mean moved 118.6 → 125.1 s. **keep**: the HP as approved, the par 138 (the debrief's boss time is graded against it). **trim**: the head's HP down about 8 % so the mean returns to about 127 s and the par stays 127. Recommended: keep | [video: the dive to the return](../../enemies/bosses/concept/maw-dive-capture-r59-a.mp4) | the whole-level flights' mean (125 s) is under the par, so the par is an easy mark | **keep** | keep: the par 138 |
| C.2 | [The Maw on easy](../../enemies/bosses/abyssal-maw/README.md#duration-and-hp) | **accept or change**: the act-boss window (90–180 s) is the medium rule; on easy the fight is shorter: `AbyssalMawTest` ≈ 99 s, the level's flights 84.6 s over five seeds (67.5–96.7 s, two under 90). **accept**: easy as the difficulty's factors make it (as the earlier bosses on easy). **change**: raise the Maw's HP on easy only until every seed lasts 90 s. Recommended: accept | — | — | **accept** | accepted |
| C.3 | [The head on the way back](../../enemies/bosses/abyssal-maw/README.md#phases) | **accept or change**: after the lunge the head winds back in under the play plane (the layer `sub`, 3 s): only the anti-sub weapons (the Harpoon, the Depth Charges) and the Smart Bomb reach it until it rises into its place. **change**: the head on the play plane while it winds in (every gun reaches it; about 3 s more damage, the fight a little shorter). Recommended: accept (it reads as the body coming back from the deep) | [capture: the dive and the way back](../../enemies/bosses/concept/maw-dive-capture-r59-a.png) | — | **accept** | accepted |
| C.4 | [The cruisers' crust and hole](../../enemies/bosses/abyssal-maw/README.md#the-cruisers-draft) | **accept or change**: each hull rides the ice for 2 s while it rises (the crust is the ground's own ice, so it moves with the scroll) and then keeps the wing's pace, so the crust first shows near the top edge and the hole it leaves stays under the hull until the ground carries it out of the bottom edge. **change**: the hull stops sooner (1 s), so the crust lifts beside the ship and the hole shows behind the stern as the ice runs on. Recommended: accept | [capture: the rise](../../enemies/bosses/concept/cruisers-rise-capture-r59-a.png), [capture: the crust and the hole](../../enemies/bosses/concept/cruisers-hole-capture-r59-a.png) | the hole is barely seen (R.2) | **accept** | accepted |
| C.5 | [The breach point](../../campaign/act-4-deep-water/level-28-abyssal-maw/README.md#layout) | **accept or change**: the burst starts at the ship's **nose** crossing the shaft mouth's lip; the lip is the bore's circle, so a ship high on the screen or at a flank breaches up to about 3 s before one low in the middle (t=115). The medium (water to air: the bullet factor, the drag, the specials' limits) still switches for the whole field at t=115. **change**: the medium switches for the ship when it breaches. Recommended: accept (one switch for the whole field keeps the enemy shots and the escort on one rule) | [capture: left, middle, right, moving](../../campaign/act-4-deep-water/level-28-abyssal-maw/concept/breakout-burst-capture-r59-a.png) | a high ship is in the air for up to 3 s under the water's rules | **accept** | accepted |
| C.6 | [Hard's credits in Acts 3 and 4](../../systems/economy/README.md#decisions) | **accept for now or raise hard's income**: the campaign playthrough (the autopilot on the balance plan, every level on its own income) ends Act 4 at **+8.8 %** of the expert's bound on easy and **−2.3 %** on medium, but on hard **−49.4 %** (13,164 against 25,991; the rule allows −25 % on hard). Act 3 ends hard at **−54.2 %** (accepted for v0.1 in round 45) and that deficit carries into Act 4. The plan's levers cannot close it: a level's `bounty_scale`, the purchases and the prices move the bound and the playthrough together. **accept for now**: hard as it is, revisited after your playthrough of hard. **raise**: a hard-only income bonus (e.g. on the bounties), measured to −25 % at each act's end. Recommended: accept for now | — | the autopilot is the measure; you play better and worse than it | **accept for now** | accepted for now; revisited after the user's playthrough of hard |
| C.7 | [The homing diamond](../../art-direction/README.md) | **accept or change**: the art direction's homing diamond (a bullet look for homing enemy shots) was tagged "later: Act 4"; Act 4's only homing projectile is the Depth Hunter's torpedo, drawn as its own sprite with its wake (accepted in round 57), so the diamond moves to Act 5, waiting for the first homing enemy bullet. **change**: draw it now. Recommended: accept | — | — | **accept** | accepted: the homing diamond to Act 5 |

## Decisions

- 2026-10-26: round opened (step I4): the round 58 fixes (R.1 the Maw's motion, R.2 the cruisers' rise, R.3 the
  breakout's burst) with game captures, V.1 the Act 4 listen-through (119 lines), Q.1 the ambience beds' volume
  (recommendation a, +6 dB) and seven calls (C.1–C.7). Act 4 is complete and playable; M8's last round.
- 2026-10-26 (user, closed): R.1 the Maw's motion, R.2 the cruisers' rise and R.3 the breakout's burst **accepted**, all
  as recommended: the Maw's and the cruisers' art are `final`, the round 58 and 59 captures `chosen`, the round 58 redo
  notes closed (the burst now starts as the ship's nose crosses the shaft mouth's lip). V.1 the Act 4 listen-through
  (119 lines) **accepted**, the 8 flagged lines as they are. Q.1 **a**: the beds 6 dB louder (`AMBIENCE_VOLUME` 0.70,
  built in `4de9c627`; Level 21's boss swell clamped to about 3.1 dB, accepted); the a ("as it is") and c renders moved
  to `design/audio/sfx/concept/rejected/`, the b render stays as the record. C.1 **keep** (the Maw's par 138), C.2-C.5
  accepted, C.6 **accept for now** (hard's Act 3/4 credits, revisited after the user's playthrough of hard), C.7
  accepted (the homing diamond to Act 5). The steps' small rules ("pending round 59") are accepted.
