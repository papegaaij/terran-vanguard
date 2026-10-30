---
title: Concept round 70 — the Sovereign rework and Level 29's friendly marker
design: review
implementation: n/a
art: proposed
depends-on: [../../enemies/bosses/iron-sovereign, ../../campaign/act-5-the-belt/level-35-iron-sovereign, ../../campaign/act-5-the-belt/level-29-betrayal-at-ceres, ../../campaign/act-5-the-belt/level-30-minefield, ../../enemies/space/minelayer, ../../ui/hud, ../round-68, ../round-69]
updated: 2026-10-30
---

# Concept round 70 — the Sovereign rework and Level 29's friendly marker

## Summary

Your Act 5 playthrough's findings (2026-10-30, medium, from your Level 29 save): the clear fixes are **built** (step
FIX6, the full `check` green): a wavering Stormhawk shot down now bursts and sounds as a CDF craft's kill, the HUD label
`HOLD FIRE` became `SPARE WHITES` and the briefing says "BONUS: DON'T SHOOT THE WHITE-BLINKING CERES WING", Varga's warning
moved to t=72.5 (before the first pair, which enters at t=77.2); Level 30's Minelayers lay before they die and the
asteroid collisions are cut by more than half; Level 32's seam at the end is gone; Levels 29–34 end with ≥ 4 s of calm
after the last enemy (a new pacing rule from Act 5 on); Level 33's "Field's clean" plays in that calm; Level 35's boss
warning comes at t=84, before the station's first pixel (t=86).

Decided as trivia and only logged (FIX6, on the recommendation): the Minelayer's numbers (rise 120 → 600 px/s, strafe
150 → 200 px/s, HP 110 → 280, bounty 26 → 30; its 2.3 s time-to-kill listed as an accepted `BalanceTest` exception), the
large rocks' touch 20 → 10, Level 30's reshaped trains (the rear train at t=122 and the t=145 pincer's loops kept off the
lower centre), Harker's Level 29 line moved before the station turns (t=70.5 → 51.6).

The rows ask what is left: **the Iron Sovereign's rework** (A.1 the HP moved from the core to the rings, measured; A.2
the core phase's "something extra, less predictable", three variants with storyboards), and **Level 29's friendly
marker** (B.1, four variants on the game's own frames) with its **HUD label** (B.2). Nothing of A or B is built: the
drafts are in the [Iron Sovereign](../../enemies/bosses/iron-sovereign/README.md#the-round-70-rework-draft-round-70)
and [Level 29](../../campaign/act-5-the-belt/level-29-betrayal-at-ceres/README.md#objective) documents.

## Review

The review page is built from this file by `build/round70-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 70`). The record of the decisions is [review.html](review.html)
(`tools/concept/review_record.py`). The storyboards are drawn by
`tools/concept/sovereign_core_r70.py`, the markers and labels by `tools/concept/friendly_marker_r70.py`, both over
real game stills (recipes in the parts' `concept/prompts.md`). A.1 was flown on a temporary copy of the Sovereign's
data (restored unchanged after the measure).

## The boss fight

Your note: *"The last part of the boss is a bit boring. We should add something extra to it and make it shorter. We can
extend the first part (shooting the guns on the rings). These guns can have some more hitpoints and the core less. We
need to add something extra to the core part as well and make it less predictable."*

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| A.1 | [The HP shift: longer rings, a shorter core](../../enemies/bosses/iron-sovereign/README.md#the-round-70-rework-draft-round-70) | **ok or redo** (your direction, Claude's numbers): the SAM batteries 220 (kept), **rail 330 → 1,200, laser 440 → 1,500, the core 14,000 → 6,600**. Medium (five seeds, the plan's fit and Rook): the fight **145.2 s** (now 147.1), the rings **≈ 91 s** (now ≈ 36) with **every phase ≥ 20 s on every seed** (45 / 23 / 23 s; now 31 / 4 / 1.5) and the core **≈ 54 s** (now 110); the par 150 → 145. The Ion Beam's column (it crosses all three rings at once, so they died together) is answered by the HP alone: graded outward-in, the rings lock **outer, middle, inner** on every seed. The numbers per difficulty below | [a ring locking (as built)](../../enemies/bosses/iron-sovereign/concept/sovereign-lock-capture-r68-a.mp4), [the core's spiral (as built)](../../enemies/bosses/iron-sovereign/concept/sovereign-spiral-capture-r68-a.mp4) | the longer inner ring means more laser sweeps: the autopilot is struck 6–8 times a fight (now 0–3) and loses ≈ 50–70 instead of ≈ 10; easy's phase 3 stays short (4–10 s); a strict order with less spread by seed would need code (shuttered rings, S): not needed by the measure | **ok** (the user, 2026-10-30) | Decided: the HP shift as proposed (SAM 220, rail 1,200, laser 1,500, core 6,600; par 150 → 145). Written as decided in the Iron Sovereign's README; built by step BOSS70 (`m9-pt-notes.md` "From CL70"). |
| A.2 | [The core phase: something extra, less predictable](../../enemies/bosses/iron-sovereign/README.md#the-round-70-rework-draft-round-70) | **pick a, b or c** (each seeded by the sim, so a replay plays the same; storyboards at game scale on a real still): **a** (recommended) *the playbook*: after each attack the core draws its next from four, never the same twice running, each with its own 0.8 s tell: the spiral (white flare), a rail volley of three lines fanned through your spot (red petals), a Dart surge from all four bays (gold bays), a ring of eight Minelayer mines (amber rim); code M. **b** *the wreck throw*: every 2.5–4 s the core tears a dead battery's wreck off its ring (gold glow, 0.8 s) and hurls it at you, tumbling (200 px/s, 60 HP: dodge or shoot), from any ring and side; spiral and Darts as now; code M. **c** *the core breaks loose*: the reactor flies between five stops (a gold ring marks the next, 1 s ahead), holds 6–9 s and fires from there; code L. a is recommended: it answers "less predictable" head-on with attacks and tells you already know, keeps the arena and its art, and the core's HP sets its length as now | [the three side by side](../../enemies/bosses/iron-sovereign/concept/sovereign-core-r70-abc.png), [a the playbook](../../enemies/bosses/iron-sovereign/concept/sovereign-core-r70-a.png), [b the wreck throw](../../enemies/bosses/iron-sovereign/concept/sovereign-core-r70-b.png), [c the core breaks loose](../../enemies/bosses/iron-sovereign/concept/rejected/sovereign-core-r70-c.png) | storyboards, not the game: the numbers are drafts to measure in the build; a's mine ring and b's wrecks land on dark rings (the Darts already read dark there, round 68); c moves the core and its 72 px no-spawn ring, new art for the empty cradle and the thrusters | **a and b combined**: "I like a combination of a and b." (the user, 2026-10-30) | Decided, read as **the playbook (a) with the wreck throw (b) as its fifth attack**: after each attack the core draws one of five at random (seeded), never the same twice running: spiral, 3-line rail volley, 12-Dart surge, ring of 8 mines and a wreck throw (2–3 dead batteries' wrecks hurled at the ship), each with its own tell. c rejected (storyboard in `concept/rejected/`); a and b `chosen` as the combination's look. Built by step BOSS70. |

**Measured (row A.1)** the autopilot's fight from the bar to the kill (mean of five seeds, the range) and its phases
(mean seconds: the spin-up and phase 1, phase 2, phase 3, the core with its opening), the plan's fit with Rook;
`levelReport`'s test fit in brackets.

| Fight | Difficulty | Fight, s | Spin-up + phase 1 | Phase 2 | Phase 3 | The core |
|---|---|---|---|---|---|---|
| now (as built) | easy | 108.4 | 24.5 | 1.7 | 3.1 | 78.1 |
| now (as built) | medium | 147.1 (144.1–151.9) | 30.6 | 3.7 (2.3–6.9) | 1.5 (0.6–2.2) | 110.4 |
| now (as built) | hard | 252.2 | 44.6 | 2.6 | 2.0 | 202.0 |
| proposed | easy | 106.1 (102.9–108.6) [105.4] | 46.7 | 12.8 | 6.9 | 38.8 |
| proposed | medium | 145.2 (143.6–150.3) [145.4] | 45.0 (44.8–45.0) | 23.0 (20.0–27.9) | 22.7 (20.9–24.0) | 53.5 (52.9–54.3) |
| proposed | hard | 230.8 (228.1–236.1) [229.2] | 50.1 | 48.0 | 33.3 | 98.4 |

Without Rook at medium (two seeds): proposed 150.1 s (48.5 / 18.6 / 28.1 / 53.9). Every seed inside the act boss's
window (90–180 s) at medium; hard has no window. Two earlier tries: rail 700 / laser 2,200 / core 6,400 left phase 2 at
5 s (the middle ring died with the outer); rail 1,150 / laser 1,600 / core 6,800 ran the rings ≈ 92 s.

**Lines (row A.2)** the radio each variant needs (to write and record in the build; list only): **a** Rook at the first
rail volley ("Red petals, three lines — move!"), Okafor at the first mine ring ("Mines off the rim, Lancer. Give the
lights room."), Vorne at the half ("You read the Sovereign well, pilot. Read faster."); **b** Rook at the first throw
("It's throwing its own wreckage at us!"), Vorne ("Nothing on the Sovereign is wasted, Lancer. Not even its dead."),
Okafor at the half ("Shoot the scrap down or get clear of it."); **c** Rook at the break ("The core's off its cradle —
it's moving!"), Vorne ("The Sovereign does not sit still for anyone."), Okafor at a low stop ("It's coming down on you,
Lancer. Give it room.").

## Level 29's friendly marker

Your note: *"Radio lines often do not come at the exact moment. We need a way to recognize these ships as friendly."*
Every variant is drawn on the game's own frames: t=75 (the t=77 pincer's banner) and t=80 (the first wavering pair among
shielded Talons). No ally in the game has a friendly look yet (Rook, the Bulwark, Level 10's shuttles and Level 31's tug
are told by their shape), so these are new.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.1 | [How the wavering Ceres Wing reads as friendly](../../campaign/act-5-the-belt/level-29-betrayal-at-ceres/README.md#objective) | **pick a, b, c or d**: **a** HUD-green IFF brackets with a `FRIENDLY` tag on each craft while it is on the screen; **b** a green-white outline on the craft and much bigger, brighter white blinkers; **c** a banner "FRIENDLIES INBOUND — HOLD FIRE" ≈ 2 s before each pair, in the warning banner's shape in CDF green; **d = c + a** (recommended): the banner announces, the brackets keep telling once the radio and the banner are gone. Each is a small step (S). With auto-fire on you cannot stop shooting, only steer: the pairs hold ≈ 6 s in a lane the side pincers keep clear, so steering aside spares them; no variant needs an input change | [the four side by side](../../campaign/act-5-the-belt/level-29-betrayal-at-ceres/concept/friendly-marker-r70-abcd.png), [a](../../campaign/act-5-the-belt/level-29-betrayal-at-ceres/concept/rejected/friendly-marker-r70-a.png), [b](../../campaign/act-5-the-belt/level-29-betrayal-at-ceres/concept/rejected/friendly-marker-r70-b.png), [c](../../campaign/act-5-the-belt/level-29-betrayal-at-ceres/concept/rejected/friendly-marker-r70-c.png), [d = c + a](../../campaign/act-5-the-belt/level-29-betrayal-at-ceres/concept/friendly-marker-r70-d.png) | c's banner comes when the t=77 and t=97 pincers' amber banners show (the game shows one at a time), so it gets its own slot under the hostile band (as drawn); b's outline is faint at 1:1 on the light deck plates; spread fire can still spoil the bonus (accepted in round 60) | **d** (the user, 2026-10-30) | Decided: the banner "FRIENDLIES INBOUND – HOLD FIRE" in its own slot (not at the same moment as the t=77 and t=97 pincers' amber banners) **plus** the green IFF brackets with the `FRIENDLY` tag on each wavering craft while on screen. a, b, c renderings rejected. Built by step MARK70. |
| B.2 | [The secondary's HUD label](../../campaign/act-5-the-belt/level-29-betrayal-at-ceres/README.md#objective) | **pick a, b or c** (the panel's box holds 13 letters beside `2 / 6`, `DONE` or `FAILED`): **a** `SPARE WHITES` (built in FIX6), **b** `SPARE ALLIES` (recommended: it names who to spare and matches the `FRIENDLY` tag), **c** `DON'T SHOOT` | [the three on the panel, counting and failed](../../campaign/act-5-the-belt/level-29-betrayal-at-ceres/concept/spare-label-r70-abc.png) | "DON'T SHOOT … FAILED" reads oddly; "WHITES" only works while the craft are known by their white blinkers | **b** (the user, 2026-10-30) | Decided: the HUD label `SPARE ALLIES` (`SPARE WHITES`, built in FIX6, and `DON'T SHOOT` rejected). Built by step MARK70. |

## Decisions

- 2026-10-30: round opened (step R70) from the user's Act 5 playthrough (`m9-pt-notes.md`): FIX6's fixes built and its
  trivia logged; A.1 the Sovereign's HP shift (measured on a temporary data copy, restored), A.2 three core variants with
  storyboards (a recommended), B.1 four friendly markers on real game frames (d = c + a recommended), B.2 the HUD label
  (`SPARE ALLIES` recommended).
- 2026-10-30 (user, closed; the page's answers): **A.1 ok** (the HP shift as proposed; par 150 → 145); **A.2 "I like a
  combination of a and b"**, read (main agent) as the playbook (a) with the wreck throw (b) as its fifth attack, the core
  drawing one of five attacks at random (seeded) after each, never the same twice running, each with its own tell; c
  rejected; **B.1 d** (the pre-entry banner in its own slot plus the green IFF brackets with the `FRIENDLY` tag; a, b, c
  rejected); **B.2 b** (`SPARE ALLIES`). The intro's trivia (the Minelayer's numbers, large rocks 10, the reshaped trains,
  Harker's line before the turn) raised no objection: decided. A and B.2 are built by steps BOSS70 and MARK70.
