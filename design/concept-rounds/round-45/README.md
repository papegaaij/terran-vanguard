---
title: Concept round 45 — the Colossus's spine and death throe, Level 21 in play, hard's Act 3 economy, the Act 3 listen-through
design: approved
implementation: n/a
art: chosen
depends-on: [../../enemies/bosses/dust-colossus, ../../campaign/act-3-red-dust/level-21-dust-colossus, ../../campaign/act-3-red-dust/level-17-dome-siege, ../../enemies/bosses/revenant-walker, ../../audio/voice, ../../systems/economy, ../round-44]
updated: 2026-10-21
---

# Concept round 45 — the Colossus's spine and death throe, Level 21 in play, hard's Act 3 economy, the Act 3 listen-through

## Summary

Your rule of 2026-10-20: **ask only about artwork, sounds, boss fights and important game concepts.** Round 44's
answers were built (step ART-K: the spine and the vents as organs, the death throe, the greenhouse frame and its pad;
SIM-K and GK: the ground holding still under the Colossus, the rings; L21R: Level 21's wave rework). This round holds
only what is left to look at or decide:

1. **Art** (`A` rows): the Colossus's spine and vents, its death throe, the greenhouse frame and pad, the debris rocks.
2. **Boss fights** (`B` rows): the Colossus in play, phase 1's pressure, the walker's brighter stomp ring.
3. **An important game concept** (`C.1`): hard's economy in Act 3.
4. **The voices** (`V.1`): one row for the Act 3 listen-through (202 lines in play order; the review page has them on
   a second page, "Listen").

**For your information** (no answer needed): the arena now hardly crawls in the fight (your K.c: the ground holds
still from the rise to the end of the dive, so only the intro, about half a second in phase 3's gaps and the time after
the kill move); Level 21 has 168 enemies at medium (easy 143, hard 152) after the floor rework. The spine's soft shadow
and the geyser ring round the mouth (used when the ridge is too short for the geysers along it) are part of A.a and A.b.

There is no index.html board in this round: the review page is built from this file by
`build/round45-tools/build_review.py` (untracked), and the voices are on [listen-through.html](listen-through.html)
(generator `tools/concept/listen_through.py`, index in [listen-through.md](listen-through.md)).

## Review

Record of the decisions: [review.html](review.html)

## In the game

`./gradlew :desktop:run --args="..."` with `--settings <file>` (a debug run writes no save):
- Level 21 with an approximation of the balance plan's ship and Rook's Autocannon: `--start level --level 21 --loadout front=ion-beam:3,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:3,generator=2,shield=1,armor=3 --special airstrike:2 --escort rook:autocannon:1`
  (`--invulnerable` to see the whole level, `--act-end` the act's end, `--debug-speed 4` shortens the approach);
- the numbers: `./gradlew :content:levelReport -Plevel=21 -Pparts=pacing,floor,haul,boss`.

## Art

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| A.a | [The Colossus's spine and vent organs](../../enemies/bosses/dust-colossus/README.md#concept-art) | **Question: the spine reads as bubbles.** Built as asked (round 44 K.b): behind an edge mouth, 4 segments in 3 states with the vents as organs on them and a soft shadow. At 1x the humps read as a chain of pale bubbles, not as a back; three render iterations did not fix it. Options: accept as is; **one continuous ridged back** (re-render the spine as one ridge with the plates overlapping, vents on it; a small art step); or say what you want. **Recommendation: the continuous back** | [sheet](../../enemies/bosses/concept/dust-colossus-final-r45-a.png), [motion](../../enemies/bosses/concept/dust-colossus-final-r45-a.gif) | the same 4-segment layout and heading frames are kept, so no game change follows the re-render | one continuous ridged back | accepted with a change: the spine is re-rendered as one continuous ridged back (step ART-S, planned, r46) |
| A.b | [The death throe](../../enemies/bosses/dust-colossus/README.md#concept-art) | **check** (4.5 s): the Colossus rears, collapses, geysers run tail to head along the ridge (a ring round the mouth when the ridge is short), the fissure ring opens, 18 rocks fly, the dust plume rises, the shockwave runs 2.9 s, the screen shakes up to 7 px and flashes, the credits pour. The sheet shows the frames, the clip the game. **Recommendation: approve** | [sheet](../../enemies/bosses/concept/dust-colossus-final-r45-a.png), [phase 3 and the throe (video with sound)](../../campaign/act-3-red-dust/level-21-dust-colossus/concept/level-21-capture-r45-a-p3.mp4), [sheet](../../campaign/act-3-red-dust/level-21-dust-colossus/concept/level-21-capture-r45-a.png) | flown by the autopilot; the pale spine shows (A.a); the first recording's geysers and plume were too faint: step GFX4 fixed them and **the phase 3 clip and the sheet show the GFX4 fixes**; the rearing, rocks, collapse, shockwave and sinkhole read well | approve | approved |
| A.c | [The greenhouse frame and pad](../../campaign/act-3-red-dust/level-17-dome-siege/README.md#concept-art) | **check** (round 44 G.c): the frame is transparent round the structure, the burnt pad is its own ground decal and the hit flash follows the structure only. **Recommendation: approve** | [sheet](../../campaign/act-3-red-dust/level-17-dome-siege/concept/greenhouse-frame-final-r45-a.png), [motion](../../campaign/act-3-red-dust/level-17-dome-siege/concept/greenhouse-frame-final-r45-a.gif), [in play](../../campaign/act-3-red-dust/level-17-dome-siege/concept/greenhouse-frame-r45-a.mp4), [crops](../../campaign/act-3-red-dust/level-17-dome-siege/concept/greenhouse-frame-r45-a.png) | in play the frame is a thin grey wire skeleton about 56 px wide that hardly stands out from the burnt ground; with 20 HP it breaks in 0.4 s, so the shaped hit flash cannot be told from the shots' sparks; **the burnt pad does not show as its own patch** (it melts into the burnt ground of that stretch) | approve | approved |
| A.d | [The debris rocks](../../enemies/bosses/dust-colossus/README.md#concept-art) | **Question: the rocks read dark.** The debris the Colossus throws (and the 18 of its death) are dark against the dune floor. Options: accept; **lighter rocks** (a recolour, no number moves). **Recommendation: accept**, since a dark rock is easy to spot against the sand and the throe's plume lightens the scene | [motion](../../enemies/bosses/concept/dust-colossus-final-r45-a.gif), [phase 3 and the throe (video with sound)](../../campaign/act-3-red-dust/level-21-dust-colossus/concept/level-21-capture-r45-a-p3.mp4), [sheet](../../campaign/act-3-red-dust/level-21-dust-colossus/concept/level-21-capture-r45-a.png) | accept | accepted |

## Boss fights

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.a | [The Colossus in play](../../enemies/bosses/dust-colossus/README.md#design) | **check capture** (Level 21, the fight about 104 s): the ground holds still while the Colossus is out, the spine, the bite circle, about 3 eruptions in phase 3. **Recommendation: accept** | [part 1](../../campaign/act-3-red-dust/level-21-dust-colossus/concept/level-21-capture-r45-a-p1.mp4), [part 2](../../campaign/act-3-red-dust/level-21-dust-colossus/concept/level-21-capture-r45-a-p2.mp4), [part 3](../../campaign/act-3-red-dust/level-21-dust-colossus/concept/level-21-capture-r45-a-p3.mp4), [sheet](../../campaign/act-3-red-dust/level-21-dust-colossus/concept/level-21-capture-r45-a.png) | flown by the test autopilot, not by you (win, boss 1:49 against par 1:44, 163 of 168, Rook survived); the spine is the pale one of A.a; **phase 3 showed two eruptions, not about three** (the boss died during the second); the bite circle was hidden under the mouth's body (a bug, not a question): step GFX4 fixed it and **the part 3 clip and the sheet show the GFX4 fixes** | change: choir text delayed 4.5 s | changed: the choir text at the Colossus's death is impossible to hear under the big roar; it gets a **4.5 s delay** (C45 + G45, planned) |
| B.b | **Question: phase 1's pressure** | the calibration harness measures phase 1 at **0.70x the Kraken's** (round 44 asked 0.8-1.2x) with the rings every 2 s; the phase is as long as it can be without the fight leaving its window. Options: **add the 5-way fan of phase 2 to phase 1** (more pressure, small data and sim change); accept 0.70x; or say. **Recommendation: add the fan** | [the Colossus](../../enemies/bosses/dust-colossus/README.md#phases) | the fan raises the fight's length a little; it is re-measured | add the fan | add the 5-way fan of phase 2 to phase 1 (C45, planned) |
| B.c | [The walker's brighter stomp ring](../../enemies/bosses/revenant-walker/README.md) | **check** (round 44 W.h, built in GK): the stomp's brace ring and the expanding ring are brighter and thicker. **Recommendation: accept** | [the stomp](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/concept/level-19-stomp-r45-a.mp4), [crops](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/concept/level-19-stomp-r45-a.png) | the brace ring now reads well; the expanding ring was too short in the first recording (a flash at the feet): step GFX4 lengthened it and **the clip and crops show the GFX4 fix** | change: ring less bright | changed: the stomp ring is "too bright, somewhere in between is better" (G45, planned) |

## Hard's economy in Act 3

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [Credits on hard in Act 3](../../systems/economy/README.md) | **Question (an important concept).** The campaign's balance test (step I1) plays Levels 01-21 with the expert's perfect-run bound. After Act 3 the bound is met **+2.4 % on easy and -4.4 % on medium** (allowed 25 %), but on **hard the credits end 56.5 % under it**. The plan's levers cannot fix it: the purchases are unaffordable and `bounty_scale` scales the prices and the bounties both. Also, the autopilot loses Levels 18 and 19 on hard without Rook. Options: **accept for v0.1 and revisit after your Act 3 playthrough** (you may find hard fine or not); a **hard-only Act 3 bounty bonus** measured to -25 %; or say. **Recommendation: accept for v0.1** | [the economy](../../systems/economy/README.md) | the autopilot is the measure, you play better and worse than it | accept for v0.1 | accept hard's Act 3 economy for v0.1, revisit after the user's playthrough |

## Voices

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| V.1 | [The Act 3 listen-through](listen-through.md) | **listen** to the voiced Act 3 lines in play order, 202 lines (54 pinned, 38 on an earlier listen row): the [listen page](listen-through.html) is the second page of the review (the "Listen" button at the top). A line you dislike is a redo; **answer once**: all fine, or redo these (note the lines, e.g. "L17 #12") | [the list](listen-through.md) | Claude has not listened to them | all fine | all fine: the Act 3 voices are accepted |

## Decisions

- 2026-10-21 (user, closed): A.a one continuous ridged back (re-render, step ART-S, planned); A.b the death throe, A.c the greenhouse frame and pad and A.d the debris accepted; B.a changed: the Choir's words at the Colossus's death start 4.5 s after the kill (the roar drowns them; C45 + G45, planned); B.b the 5-way fan added to phase 1 (C45, planned); B.c the walker's stomp ring is to be dimmed, between the old and the new (G45, planned); C.1 hard's Act 3 economy accepted for v0.1, revisited after the user's Act 3 playthrough; V.1 all 202 Act 3 voiced lines accepted.
