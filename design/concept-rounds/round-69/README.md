---
title: Concept round 69 — M9 part I, Act 5's close-out
design: review
implementation: n/a
art: proposed
depends-on: [../../campaign/act-5-the-belt, ../../enemies/bosses/iron-sovereign, ../../enemies/ground/rail-bunker, ../../enemies/ground/warden-tank, ../../audio/voice, ../../tech/release, ../../tech/roadmap, ../round-66, ../round-67, ../round-68]
updated: 2026-10-29
---

# Concept round 69 — M9 part I, Act 5's close-out

## Summary

The close-out round of M9 ([roadmap](../../tech/roadmap/README.md#m9-parts), part I): **Act 5 "The Belt" is built end to
end**, Levels 29–35 on every difficulty, from the Act V card through the Iron Sovereign to `ACTS I-V COMPLETE`, and
rounds 60–68 are closed. What is left is your playthrough of the act, your ear on its voices and the release.

The rows ask, in this order: **V.1** the Act 5 listen-through (188 voiced lines; the 29 to listen to first on top),
**B.1–B.7** seven built changes to confirm (ok or redo), each with a before | after rendering at the same view: the
clips that went stale when steps FIX3 and FIX4 built rounds 66–68's answers, and three side effects of those steps,
and **Q.1–Q.4, F.1, F.2, R.1** the calls before the release (the recommendation is pre-selected).

Decided as trivia and only logged: the Act 5 art audit (part I's question 5): the Talon, the SAM Nest, the Asteroid
Mite, the wavering Stormhawk, the belt's world doc and the Act 5 README are `art: final` (their production frames are in
the game; step I2); the listen-through's marks (a transcript that misses or changes a needed word, over 0.7 s a word, a
peak of 0.98 or more, a tail over -35 dBFS, -25 on the enemy channel, and every line of Harker and the Bellwether, the
act's new voices); the hunter drone and the other unbuilt patterns retagged **later: Act 6**; the refactoring RF9 waits
until after `v0.3`; the 159 game sounds still cut from Freesound previews are listed (`briefs/freesound-originals.md`) for
a fetch of the originals when the quota allows; round 68's looks-only notes (the spin-up under the `WARNING` banner, the
segments fading within a second of the white flash, the bay Darts dark on the dark rings) stay as accepted with H.1 ok;
the generators that wrote round 67/68's rejected files still name their old `concept/` paths (do not re-run them for
those files: a tidy-up for the next tool step).

## Review

The review page is built from this file by `build/round69-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 69`). The listen-through is
[listen-through.html](listen-through.html) with its index [listen-through.md](listen-through.md)
(`tools/concept/listen_through.py`, the rerun line in its docstring); the page plays every line inline. The before |
after sheets of B.5–B.7 are drawn by `tools/concept/before_after_r69.py`; the clips are recordings of the game with its
own sound (recipes in the parts' `concept/prompts.md`), each checked on an eight-frame sheet. The record of the
decisions is [review.html](review.html) (`tools/concept/review_record.py`).

## In the game

The full `./gradlew check` is green after steps FIX3, FIX4, I1, VOX-I, REL-I and I2 (`GoldenFlightsTest` and
`ReplayTest` unchanged); `tools/balance.py` reports no problem. Step I1's `levelReport` (three seeds, the test fit): the
autopilot wins every level on every difficulty 3 of 3 (Level 32 on hard after two retries on two routes); every medium
level holds the Act 2+ floor, the pacing rule and its credit budget (−3.0 … +6.2 %); Level 32's floor holds on every
route, the right-right route's margin is 0.05 (2 %); the Iron Sovereign falls in 149.0 s at medium (par 150), 108.5 s on
easy, 249.3 s on hard. Medium ends Act 5 8.2 % under the expert's bound (inside the ±15 %); hard 59.3 % under (Q.1).
The release is prepared as `0.3.0` (step REL-I, not tagged): the four bundles 354–357 MB zipped, a `v0.2` save carries on
into Act 5's intro.

**Your playthrough: what to watch** (medium from Level 29, or the saves of Q.3):

1. **Level 29**, Betrayal at Ceres: the rescue and the escort swap (the Bulwark flies until Rook is freed, then Rook);
   the wavering Stormhawks; the clamp guns.
2. **Level 30**, Minefield: the Gilded Gunships' gold plate and the mine lines (a shot clears a cluster of four).
3. **Level 31**, Mining Rig Escort: the tug (200×300), what hurts its hull and how its sections read; on hard the bow is
   hard to keep (F.1).
4. **Level 32**, Deep Core: the fork choice and its two routes; whether the lit door reads as the way on (round 65's
   QE-2); whether the Strider's topple reads (its legs are thin).
5. **Level 33**, Leech Field: the leeches' drain (the latch, the brown-out warning) and the rail lines riding with their
   bunkers (B.2).
6. **Level 34**, Salvage Run: the salvage, the Helix core and the Mk V generator in the shop; the tanks on the black
   hulls (B.3); one quiet stretch of 8 s at medium (F.2).
7. **Level 35**, Iron Sovereign: the fight's length (round 68 C.1 a, about 2.5 minutes), the rail lines (B.1), the
   lifeboat (B.4), the act end and `ACTS I-V COMPLETE`.
8. On hard: whether the shop money feels short from Act 3 on (Q.1).

**How to play it**: `./gradlew :desktop:installDist` on branch `m9-act-5`, then a save per level in a folder of its own
(`./gradlew :content:playthroughSave -Plevel=29 -Pout=<folder>/l29`, the same with `-Plevel=35` into `<folder>/l35`;
`-Pdifficulty=hard` for a hard save), started with
`desktop/build/install/terran-vanguard/bin/terran-vanguard --settings <folder>/l29/settings.properties`: Continue opens
the Act V card and Level 29's briefing (Level 35's save: its briefing). Q.3 asks which saves Claude makes for you and
where. Findings go into a playtest note; each becomes its own fix step.

## Voices

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| V.1 | [The Act 5 listen-through](listen-through.md) | **listen** to every voiced Act 5 line in play order: the act intro, Levels 29–35 (the briefing pages, the radio cues by time and by event, the escort-only and `requires_not` lines, both `{side}` takes, the secrets), Rook's barks, the other calls heard in the act and the act outro: **188 lines, 16.6 minutes** (41 pinned takes, 4 already listened to in earlier rounds, no file missing). The **29 lines on "Listen first"** come first: there the transcript (Whisper, the renderer's model) misses or changes a word the line needs, the delivery is slow, or the speaker is new in this act (Harker, the Bellwether). **Answer once**: ok, or redo and give the lines in the note by section and number (e.g. "L29 #9") | [the list](listen-through.md), [the page](listen-through.html) | Claude has not listened; the marks are a transcript's hints, not verdicts (the small model misreads names and the enemy channel's filter) | **ok** (the user, 2026-10-29) | Accepted: the Act 5 voice lines as rendered (188 lines); nothing to redo. |

## Built picks to confirm

Seven changes that are built and in the game, each shown before and after at the same view: four game clips that went
stale when rounds 66–68's answers were built (steps FIX3 and FIX4), and three side effects of those steps. Answer ok to
keep the build, redo to change it (say how in the note).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.1 | [The Sovereign's rail lines ride with their batteries](../../enemies/bosses/iron-sovereign/README.md#phases) | **ok or redo** (round 68 C.9 a, built in FIX3): a rail battery's line is aimed when its telegraph starts and keeps that direction, its origin riding on the battery as the ring turns (the outer ring ≈ 21 px/s, the middle ≈ 24 px/s), so the band slides with the ring before the shot | before: [round 68's clip, the line fixed where it started](../../enemies/bosses/iron-sovereign/concept/sovereign-rail-capture-r68-a.mp4); after: [the line riding with the ring (clip)](../../enemies/bosses/iron-sovereign/concept/sovereign-rail-capture-r69-a.mp4), [frames at 1:1](../../enemies/bosses/iron-sovereign/concept/sovereign-rail-capture-r69-a.png) | at ≈ 24 px/s the origin moves only ≈ 12 px in the 0.5 s between the telegraph and the shot: the riding is barely visible at play speed (the 1:1 frames show it); the core's own rail lines, fixed at the centre, fire beside it | **ok** (the user, 2026-10-29), note: "The aim of the gun should compensate for the movement so that it hits after it has moved" | Confirmed, with the note: the rail lines must lead the ring's turn. **Built in FIX5** (the sim's `LineAim`, `m9-fix5-notes.md`). |
| B.2 | [Level 33's rail line rides with the bunker](../../enemies/ground/rail-bunker/README.md#behaviour) | **ok or redo** (round 66 C.1 b, built in FIX3): the line's direction is locked when the telegraph starts, its origin stays on the turret and scrolls down with the ground (≈ 130 px in the 1 s telegraph), so a still ship is missed by a diagonal line and hit by a near-vertical one | before: [round 66's stills, the line left behind on the screen](../../enemies/ground/concept/rail-bunker-capture-r66-a.png); after: [the line riding down with the bunker (clip)](../../enemies/ground/rail-bunker/concept/rail-bunker-capture-r69-a.mp4), [frames](../../enemies/ground/rail-bunker/concept/rail-bunker-capture-r69-a.png) | the thin telegraph line is faint over the bright grown rock; with the line riding, the still ship in the clip is missed (as designed for a diagonal line) | **ok** (the user, 2026-10-29), note: "The looks are ok, but the gun must aim in front of the ship. It must compensate for the movement of the screen." | Confirmed (the looks), with the note: the Rail Bunker's line must lead the scroll. **Built in FIX5** (`LineAim`, `m9-fix5-notes.md`). |
| B.3 | [The liveried tanks' shadows on the black Helix hulls](../../campaign/act-5-the-belt/level-34-salvage-run/README.md#concept-art) | **ok or redo** (round 67 G.1 c, built in FIX4): Level 34's Helix hulls back in round 60's black, each liveried Warden Tank and its wreck with a faint ground shadow (the silhouette 4, 5 px down-right, softly blurred, 50 % black) | before: [round 67's 1:1 crops on the grey-gold hulls](../../campaign/act-5-the-belt/level-34-salvage-run/concept/tank-readability-capture-r67-a.png); after: [FIX4's 1:1 crops](../../campaign/act-5-the-belt/level-34-salvage-run/concept/tank-readability-capture-r67-c.png), [the convoy on the black hull (clip)](../../campaign/act-5-the-belt/level-34-salvage-run/concept/tank-shadows-capture-r69-a.mp4), [frames at 1:1](../../campaign/act-5-the-belt/level-34-salvage-run/concept/tank-shadows-capture-r69-a.png) | the shadows fall on the grey steel road and are faint at 1:1 (as the mockup's 50 %); off the road the black hull hides them | **ok** (the user, 2026-10-29) | Confirmed: the liveried tanks' shadows on the black Helix hulls; the clips are `chosen`. |
| B.4 | [The Ceres lifeboat opening](../../campaign/act-5-the-belt/level-35-iron-sovereign/README.md#secrets-and-pickups) | **ok or redo** (round 68 H.5's capture, fixed in FIX3): shot open, the lifeboat plays its six open frames over 1 s (the hatch blown, the panels flung out, the stores' light, the burnt hull) as its crate drifts out. Round 68's clip read the capsule as drawn whole: the six frames played in a 0.4 s burst at the top edge under the Ion Beam and the last one (the hatch blown dark) reads close to the whole capsule; FIX3 slowed the opening to 1 s (Claim 114 keeps its 0.4 s) | before: [round 68's clip](../../campaign/act-5-the-belt/level-35-iron-sovereign/concept/lifeboat-capture-r68-a.mp4); after: [the lifeboat shot open mid-screen (clip)](../../campaign/act-5-the-belt/level-35-iron-sovereign/concept/lifeboat-capture-r69-a.mp4), [frames at 1:1](../../campaign/act-5-the-belt/level-35-iron-sovereign/concept/lifeboat-capture-r69-a.png) | the Ion Beam's glare covers the capsule's middle while it opens; the last open frame (the hatch blown dark) still reads close to the whole capsule (art, accepted with H.5) | **ok** (the user, 2026-10-29) | Confirmed: the lifeboat's 1 s opening; the clips are `chosen`. |
| B.5 | [Level 34's far hulk went black too](../../campaign/act-5-the-belt/level-34-salvage-run/README.md#concept-art) | **ok or redo** (a side effect of FIX4): the far Helix hulk turning on the deep layer and the far wreck `far-wreck-b` use the same Helix ramp as the hulls, so G.1 c turned them black with the near hulls; **ok** keeps them black (one Helix colour in the level), **redo** pins them to round 67 a's grey-gold as Level 35's tenders are (B.6) | [before and after, 2x](../../campaign/act-5-the-belt/level-34-salvage-run/concept/far-hulk-before-after-r69-a.png) | black on the deep layer's black space: the far hulk reads by its gold edge and the stars it hides | **ok** (the user, 2026-10-29) | Confirmed: the far Helix hulk and `far-wreck-b` stay black with the near hulls. |
| B.6 | [Level 35's tenders kept grey-gold](../../campaign/act-5-the-belt/level-35-iron-sovereign/README.md#concept-art) | **ok or redo** (a side effect of FIX4): Level 35's backdrop builds the Sovereign's tenders from Level 34's Helix hull kit; FIX4 pinned them to the grey-gold they were accepted with in round 68 (H.3 ok) instead of letting them go black. **ok** keeps them grey-gold (as accepted), **redo** renders them black like Level 34's Helix | [as built and black, 2x](../../campaign/act-5-the-belt/level-35-iron-sovereign/concept/tenders-before-after-r69-a.png) | the tenders are far below and hazed: either colour is a few dim pixels in the game | **ok** (the user, 2026-10-29) | Confirmed: Level 35's tenders stay grey-gold, as accepted in round 68 (H.3). |
| B.7 | [Level 33's middle nest moved for hard's pad](../../campaign/act-5-the-belt/level-33-leech-field/README.md#concept-art) | **ok or redo** (a side effect of FIX3, round 66 C.7 b): the pad for hard's third bunker of the t≈132 nests at x 240 lands on `nest-face-1`'s middle leech nest, so the nest moved from (250, 230) to (320, 160) on the face | [before and after, 1:1](../../campaign/act-5-the-belt/level-33-leech-field/concept/nest-face-before-after-r69-a.png) | the moved nest sits closer to the face's right edge; the pad is drawn on every difficulty (empty on easy and medium) | **ok** (the user, 2026-10-29) | Confirmed: the middle nest of `nest-face-1` stays moved to (320, 160) for hard's pad. |

## Calls

The calls before the release: part I's questions and step I1's two findings, with the recommendation pre-selected.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| Q.1 | [Hard's credits](../../player/README.md) | **pick a, b or c** (round 59 C.6, still open): on hard the autopilot ends Act 3 54 %, Act 4 49 % and Act 5 59 % under the expert's bound (23,009 credits against 56,496 after Level 35; the autopilot earns about a quarter under an expert on hard); medium ends Act 5 8 % under. **a** (recommended) keep as is and judge it in your own hard playthrough of Acts 3–5, then decide; **b** lift hard's income now (a hard bounty bonus, the bound moving with it); **c** accept hard as meant to be tight (Acts 1–2's rule, 25 % under) and set Acts 3–5's hard bound to that | — | the number comes from the autopilot, not a player: b and c would change the economy for it | **a** (the user, 2026-10-29) | Decided: hard's credits stay as they are, judged after the user's own hard playthrough of Acts 3–5 ([economy](../../systems/economy/README.md#decisions)). |
| Q.2 | [What `v0.3` holds and when](../../tech/release/README.md) | **pick a, b or c**: **a** (recommended) Acts 1–5 as `v0.3` after your playthrough and its fixes, tagged after the merge (as `v0.1` and `v0.2`); **b** tag the branch as built now and fix after; **c** wait for Act 6 | — | — | **a** (the user, 2026-10-29) | Decided: `v0.3` = Acts 1–5, tagged after the user's playthrough and its fixes ([release](../../tech/release/README.md#decisions)). |
| Q.3 | [Your playthrough saves](../../tech/release/README.md) | **pick**: Claude makes the saves with `:content:playthroughSave` (the plan's purchases, Acts 1–4 won): **medium L29 + L35** (recommended: Level 29's hangar with Rook not yet rescued, and Level 35's for the boss and the act end), **+ hard L29** as well (for Q.1's answer), or **none** (you play from your own saves). **Name the folder in the note** (somewhere you keep, not `/tmp`) | — | the saves come from the plan's purchases, not yours | **none** (the user, 2026-10-29), note: "I'll continue from my own save at level 29" | Decided: no playthrough saves are written; the user plays from their own save at Level 29. |
| Q.4 | [The macOS arm64 check](../../tech/release/README.md) | **pick** (round 44 I.9's rule, unchanged): **block the tag only if you can open the macM1 bundle on a Mac within a day** (recommended, as before); otherwise the release ships with that download marked "untested on Apple silicon" as `v0.2` did | — | — | **as before** (the user, 2026-10-29) | Decided: the Mac rule unchanged (block the tag only if the macM1 bundle can be opened on a Mac within a day; else "untested on Apple silicon"). |
| F.1 | [Level 31 on hard](../../campaign/act-5-the-belt/level-31-mining-rig-escort/README.md#difficulty-notes) | **accept or fix now** (step I1's `levelReport`, three seeds): on hard the autopilot loses the tug's bow in 2 of 3 runs and never makes the secondary (0 of 3); it still wins every run, and at medium the hull comes home clean 3 of 3 (the bow at 67–78 %). **accept** (recommended): judge it in your playthrough (the autopilot does not shield the tug the way a player can); **fix now**: soften hard's attacks on the bow | — | — | **accept** (the user, 2026-10-29) | Accepted: Level 31 on hard (the tug's bow lost in 2 of 3 autopilot runs, the secondary never made) as an exception; judged in the playthrough. |
| F.2 | [Level 34's quiet stretch](../../campaign/act-5-the-belt/level-34-salvage-run/README.md#waves) | **accept or fix now** (step I1): at medium the level has one gap of 8.2 s with nothing to shoot (t 114.0–122.1, the hold's clamps and the core); the pacing rule passes. **accept** (recommended): judge it in your playthrough (it is the salvage's moment); **fix now**: a small wave in it | — | — | **accept** (the user, 2026-10-29) | Accepted: Level 34's 8.2 s gap at medium (t 114.0–122.1) stays; judged in the playthrough. |
| R.1 | [The release texts](../../tech/release/release-notes.md) | **ok or change**: the release notes ("Acts 1 to 5", the new act in one paragraph: the belt from Ceres Hub's dock canyon to Ceres orbit, the first human-built enemy with shielded ships and walkers, the Tesla Coil Pod, the Swarm Tail, the Shield Mk V and the captured "Helix" generator, the Iron Sovereign; a `v0.2` save carries on into Act 5's intro) and the bundle's `README.txt` (the same in short) | [release notes](../../tech/release/release-notes.md) | the bundle README (`desktop/bundle/README.txt`) is outside the design tree: its first paragraph is quoted under the row | **ok** (the user, 2026-10-29) | Accepted: the release notes and the bundle `README.txt` as written. |

**The bundle README's first paragraph (row R.1)**

> A vertical shoot'em up set in 2185. This release holds Acts 1 to 5 of the campaign's seven acts: thirty-five levels
> on easy, medium or hard, from Earth orbit to the asteroid belt and the battle station over Ceres. The campaign stops
> before Act 6, Jovian Storm; the later acts follow in later releases. A save from the previous release (Acts 1 to 4)
> carries on where it stopped: Continue opens Act 5's intro.

## Decisions

- 2026-10-29: round opened (step I3): Act 5 built and closed out; V.1 the listen-through (188 lines, 29 to listen to
  first), B.1–B.7 the built changes with before | after renderings (the stale clips re-recorded on the final code), Q.1–Q.4
  part I's questions, F.1–F.2 step I1's findings, R.1 the release texts.
- 2026-10-29 (user, closed; page answers, no notes except the four below): **all ok / as recommended.** V.1 ok (the Act 5
  voices accepted); B.1 ok with the note "The aim of the gun should compensate for the movement so that it hits after it
  has moved" and B.2 ok with "The looks are ok, but the gun must aim in front of the ship. It must compensate for the
  movement of the screen.", both built in FIX5 (the rail lines lead their target); B.3–B.7 ok; Q.1 a (hard's credits
  judged after the user's hard playthrough), Q.2 a (`v0.3` = Acts 1–5 after the playthrough and its fixes), Q.4 as
  before, **Q.3 none** ("I'll continue from my own save at level 29": no playthrough saves are written); F.1 and F.2
  accepted; R.1 ok. Left open: the user's playthrough (2026-10-30), its fixes and the tag.
