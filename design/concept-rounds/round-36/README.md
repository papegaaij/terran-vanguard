---
title: Concept round 36 — the Act 3 "Red Dust" design phase
design: approved
implementation: n/a
art: chosen
depends-on: [../../campaign/act-3-red-dust, ../../world/mars, ../../enemies/ground, ../../enemies/air, ../../enemies/bosses, ../../player/weapons, ../../player/specials, ../../player/systems, ../../player/wingmen, ../../audio/music, ../../audio/sfx, ../../audio/voice, ../round-34, ../round-35]
updated: 2026-10-13
---

# Concept round 36 — the Act 3 "Red Dust" design phase

**Closed 2026-10-13** (user, on the review page): every Act 3 document D.1–D.30 **approved** (they stay
`implementation: not-started` until M7 builds them); every *your call* kept as recommended and every
other M answer accepted (M19 noted); every open-question proposal Q.15–Q.25 **yes**. Picks: Burrower
**a**, Tendril Anchor **a**, Choir Herald **b**, Ghost Drone **a** with the eye and running lights fix
(U.5), the grey Warden Tank accepted, Dust Kite **b**; Revenant Walker **a** (biped), Dust Colossus
**b**; canyon walls **b** (it amends the art-direction perspective rule; Level 16 adapted), storm radius
**b**, dome **a**, the mood stills and panorama accepted as direction; Ion Beam **a**, light drone **a**,
EMP Burst **a**, Shield Overcharge **b** (white); track 9 **b** "Grey Signal", burrow eruption **a**,
canyon scrape **b**, Ion Beam loop **a**, dust-storm bed **a**, Ares Control's voice **b** (Thomas A)
with the respellings; Ares Control's portrait **b** on the teal channel. Act 2: L.1 add the Spore Bomber
pair and L.5 move the latch spots (both built by step N2), L.2–L.4 accepted, L.6 play the Act 3 intro
once on release saves. The rejected files moved to `concept/rejected/` (step N1); the art step (N3)
updates the round-36 generators. M7 is on the [roadmap](../../tech/roadmap/README.md#m7-parts) with
parts A–I. The outcome of each row is in its *Outcome* cell.

## Summary

The Act 3 design phase, built overnight on the user's behalf (2026-10-12, the user away and asked
for it: design documents and concept proposals only, no game code). Levels 15–21 on Mars are
draft level documents; the act README, the Mars setting, nine new units, two bosses, the Act 3
arsenal and the Act 3 audio briefs are drafts; every part without chosen art has an a/b proposal.
Every open design question of the gap analysis (`m7-gaps.md` §3, M1–M19) was answered with its
**recommended** option, and everything below is **pending the user's review**. Nothing here is
`approved` until the user says so; M7's build parts A–I start from the answers.

Open [index.html](index.html) in a browser (regenerate with
`python3 tools/concept/board.py 36 --subjects burrower,tendril-anchor,choir-herald,ghost-drone,warden-tank,revenant-walker,dust-colossus,mars-olympus,mars-valles,mars-ares-landing,mars-storm-plains,mars-tharsis-foundry,mars-hellas,mars-panorama,canyon-walls,storm-radius,dome,ion-beam,light-drone,emp-burst,shield-overcharge,act3-b-theme,burrow-eruption,canyon-scrape,ion-beam-loop,dust-storm-bed,dust-kite,generic-colonial,voice-ares-control`:
round 34 already used the `r36` names for Level 14's files, so the board keeps only tonight's
subjects). The rows are in this order:

1. **Your calls** (`M…`): the questions marked *user* in the gap analysis, with the recommendation
   taken and what the drafts built from it: *keep* or *change*;
2. **The other design answers** (`M…`): one *accept* or *change* each;
3. **Act 2 leftovers** (`L.…`) found tonight;
4. **Art** per area (`U.…` units, `B.…` bosses, `MA.…` Mars, `G.…` gear, `C.…` characters, `S.…` audio): *pick a or
   b*, or *accept* / *redo*;
5. **Documents to approve** (`D.…`): *approve* or *change*; the act's intro and outro texts and
   every level's summary are quoted under their rows;
6. **Open questions** the drafts raised (`Q.…`): each row's proposals are answered one by one,
   *yes* (take the proposal) or *no* (say what in the note).

## Your calls

The gap analysis's questions marked *user*. The drafts were written from the recommended option of
each; *keep* leaves the drafts as they are, *change* names the other option.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| M1 | [Canyon wall contact](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md) | **your call**: (a, taken) damage and push-back: a touch costs 10 armour/shield (easy 6) and pushes the ship 28 px off the wall, then 0.5 s without contact damage; (b) instant death; (c) a hard clamp, no damage. Built into Level 16's draft, the Mars README and the sfx brief (the wall scrape, [S.3](#audio)) | [canyon-walls-r36-a.png](../../world/mars/concept/rejected/canyon-walls-r36-a.png) | a dense 180 px narrows plus gusts can bounce a ship between both walls; the push is untested with Rook's AI | keep a / b / c | **kept a** (user) |
| M3 | [Branching canyon](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md) | **your call**: (a, taken) visual branches on one screen: a mesa splits the canyon into two channels of 150 px, each with its own targets (the Candor relay hut secret on one side), rejoining later; (b) a real route choice with separate sections | — | (a) is a short fork, not a choice of path; (b) doubles the level's waves, tests and art | keep a / b | **kept a** (user) |
| M4 | [Reverse scroll and the pursuers](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md) | **your call**: (a, taken) the terrain moves up the screen, the ship keeps facing up and retreats toward the bottom; the Mote Swarm presses from the **top** (front guns hold it), ambushes come up from the **bottom** (rear guns clear the way). Level 20: 20 s of strike, then the scroll eases through zero into reverse; a TOP edge warning; a new `pursuit` formation; (b) the swarm from the bottom; (c) normal scroll, the retreat only in the story | — | a retreat with the guns facing the pursuers is the opposite of the roster's text; the bottom entries are unwarned after the first three (Q.20) | keep a / b / c | **kept a** (user) |
| M6 | [What the dust storm hides](../../campaign/act-3-red-dust/level-18-dust-storm/README.md) | **your call**: (a, taken) a look only: outside about 170 px the ground and `low-air` vanish into dust, air units dim to silhouettes with their glow, **bullets always visible**, everything hittable; (b) as a, but shots cannot hit what is unseen; (c) bullets hidden too. Level 18's radius 170 px (easy 200, hard 150; 150 / 180 / 130 deep in the storm); the look's a/b is [MA.2](#art--mars) | [storm-radius-r36-a.png](../../world/mars/concept/rejected/storm-radius-r36-a.png) | homing gets no real edge in (a): the storm only hides the ground | keep a / b / c | **kept a** (user) |
| M10 | [Act 3's popcorn](../../enemies/air/dust-kite/README.md) | **your call**: (b, taken) returning Skitters, Needlers, Stingers, Whirl Seeds and Wraiths **plus** a new Martian popcorn flyer, the **Dust Kite** (tiny, 1 HP, a rust membrane glider with a teal spine that rams), in every Act 3 level; (a) the returning units only | — | the Dust Kite's art: see [U.7](#art--units); its name is a working name | keep b / a | **kept b**: the Dust Kite (user) |
| M13 | [How Okafor silences Varga](../../campaign/act-3-red-dust/README.md#voice) | **your call**: (a, taken) in private: the hangar intel and the briefings carry it; in the levels only Varga's observation and Okafor's cut-off: Level 16 "Eyes on the canyon, Doctor.", Levels 17 and 19 "Not on this channel, Doctor.", Level 18 "Later, Doctor. Eyes on the storm."; Varga's approved Act 3 Helix line placed in Level 17; (b) openly on the radio | — | the refrain twice and a variant once may feel scripted; the Level 18 briefing carries one Helix sentence (Q.18) | keep a / b | **kept a** (user) |
| M14 | [Where the Choir first speaks](../../enemies/bosses/dust-colossus/README.md) | **your call**: (a, taken) Level 21, as the Colossus dies: "…many… we are many… we are going…" in fragments, then Varga's "that was not singing" and Okafor's "Log everything"; (b) Level 20 under the retreat; (c) the act outro | — | the speech treatment (Choir layering a/b) waits for part H's round | keep a / b / c | **kept a** (user) |
| M17 | [Rook's Act 3 role](../../player/wingmen/README.md) | **your call**: (a, taken) he flies all seven levels from the escort slot (his lines `requires: escort`, one dry joke a level), the canyon tests his AI, and one quiet beat in Level 20's lull foreshadows Act 4; (b) also a new Act 3 gun for him | — | the quiet beat plays even without Rook in the slot (Q.20) | keep a / b | **kept a** (user) |

## The other design answers

Not user calls in the gap analysis, but each changes what M7 builds. *Accept* keeps the
recommendation, *change* says what.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| M2 | [Wall geometry](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md) | **accept or change**: per-side wall polylines by scroll distance plus island polygons in the level data; walls stop bolts, lances, beams, enemy bullets, homing missiles and mines; pickups slide along them | — | a new data type and collision queries in the sim (the largest single piece of M7, part C) | accept / change | **accepted** (user) |
| M5 | [Reverse-scroll implementation](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md) | **accept or change**: a signed scroll speed per section; the entry edge follows the sign; front/rear and the 60 % front rule read by travel direction; a TOP warning edge | — | — | accept / change | **accepted** (user) |
| M7 | [The buried layer](../../enemies/ground/burrower/README.md) | **accept or change**: a new `BURIED` layer after `SUB`: nothing hits it, not even the Smart Bomb; no contact | — | — | accept / change | **accepted** (user) |
| M8 | [The Choir Herald's aura](../../enemies/air/choir-herald/README.md) | **accept or change**: units within about 90 px get a ring that absorbs damage up to 1× their HP (at most 40), refilled slowly while the Herald lives; the EMP Burst strips it | — | — | accept / change | **accepted** (user) |
| M9 | [The tendril barrier](../../enemies/ground/tendril-anchor/README.md) | **accept or change**: the tendril has its own 40 HP, hit only by `beam`; the anchors (12 HP) by everything; touching it deals `medium` 6 with a cooldown | — | Level 16 must stay passable without a beam: the anchors are the way for other loadouts | accept / change | **accepted** (user) |
| M11 | [Power for the Ion Beam](../../player/weapons/ion-beam/README.md) | **accept or change**: the hand check found a Mk III generator cannot carry the roster's beam (draw 5 → 9) by Level 16; proposal: keep the generator tiers, beam draw **4 → 8**, L1 raised 35 → **45 DPS**, upgrades priced on 2 500; the Evasive thrusters' 2 MW do not fit a typical Act 3 loadout | — | a hand check only; `balance-plan.yaml` gets its Act 3 rows in part A | accept / change | **accepted** (user) |
| M12 | [Economy at the act step](../../campaign/act-3-red-dust/README.md#density-and-threat-profile) | **accept or change**: accept a low `bounty_scale` where the act factor jumps (Level 15 at 0.42); the act's scales then run 0.42–0.95; `ActPlaythroughTest`'s expert bound extended to Act 3 | — | Levels 18 and 20 at 0.92–0.95 (cheap mixes on high budgets) | accept / change | **accepted** (user) |
| M15 | [Hint 2's presentation](../../enemies/bosses/revenant-walker/README.md) | **accept or change**: after the walker dies the scroll crawls past the wreck at 20 px/s for 10 s with a radio exchange; Level 20's briefing image shows the filed plate | [revenant-walker-r36-a.png](../../enemies/bosses/concept/revenant-walker-r36-a.png) | — | accept / change | **accepted** (user) |
| M16 | [The domes in Level 17](../../campaign/act-3-red-dust/level-17-dome-siege/README.md) | **accept or change**: a new ally, the *Ares Landing dome* (`ground`, 120 px, Eden 144; breaks on the 3rd hit, easy 4, hard 2), five named domes, hurt only by hooked bombers (and the hooked Shellbacks, Q.17); the secondary "No dome hit" (a new kind `untouched`, 100) | [dome-r36-a.png](../../world/mars/concept/dome-r36-a.png) | the dome is a draft roster row in allies, not its own document yet | accept / change | **accepted** (user) |
| M18 | [Reference DPS for Act 3](../../campaign/act-3-red-dust/README.md#units-in-the-act) | **accept or change**: 76 · 80 · 84 · 88 · 92 · 96 · 100 for Levels 15–21 (an act-opener dip, then +4 a level), the act HP factors from it | — | to be confirmed by the Act 3 balance plan in part A | accept / change | **accepted** (user) |
| M19 | Draft numbers | **check**: hand-written tables only tonight, no `data.yaml` anywhere under `design/` (the strict loader would read it); each build part moves its numbers into data and marks the tables | — | — | noted | noted (user) |

## Act 2 leftovers

Found while the round-34 and round-35 follow-ups ran tonight (steps K6–K10).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| L.1 | [Level 12's Spore Bomber reveal](../../campaign/act-2-homefront/level-12-storm-front/README.md) | **your call**: the reveal capture (round 34's P5) needed a scratch data variant (the t=146 Spore Bomber pair moved to t=101.5): the real data never puts a Spore Bomber under a bank at a lightning flash, so the promised reveal does not happen in play. Add a pair at t≈101.5 to the real data, as the capture's variant? | [level-12-capture-reveal-r34-a.png](../../campaign/act-2-homefront/level-12-storm-front/concept/level-12-capture-reveal-r34-a.png), [.mp4](../../campaign/act-2-homefront/level-12-storm-front/concept/level-12-capture-reveal-r34-a.mp4) | an extra pair changes Level 12's density, haul and replay hash (a re-recorded golden replay) | add the pair / leave the data | **add the pair** (user): the Spore Bomber pair at t≈101.5 goes into the Level 12 data (step N2) |
| L.2 | [The intel's boss mark](../../ui/hangar/README.md) | **accept or change**: a red full-height BOSS tick on the hangar intel's wave strip at the boss's time (sensor L3), now on every boss level: L05, L07, L11 and L14 (Level 14's approved spec promised it; the others follow) | — | not captured | accept / change | **accepted** (user) |
| L.3 | [Mission failed: the radio box](../../systems/retry/README.md) | **accept or change**: when the mission failed or game over screen opens, the HUD's radio queue is cleared and a playing voice cut, so the screen's own line is the only one (Level 13 showed Varga's stale line next to Okafor's) | — | an approved document's sentence changed (retry: back to review) | accept / change | **accepted** (user) |
| L.4 | [Okafor's Level 13 failure line](../../campaign/act-2-homefront/level-13-polar-relay/README.md) | **listen**: "Nansen is gone. Pull back, Lancer." now with `expression: grim` (the portrait and the voice's grim settings), re-rendered | [the take](../../../assets/voice/okafor/7f735b28f819.ogg) | — | accept / redo | **accepted** (user) |
| L.5 | [The Lamprey's latch spots](../../enemies/air/lamprey/README.md) | **your call**: the latch look b's frames sit 1 px off the latch spots in the data | — | invisible at speed; noticeable only on a still frame | move the data 1 px / leave it | **move the data 1 px** (user; step N2) |
| L.6 | [Release saves at the Level 15 hangar](../../campaign/act-3-red-dust/README.md#act-intro-and-outro) | **your call**: every player of the M6 release has a save at the Level 15 hangar, written before Act 3 existed; Act 2's precedent would skip Act 3's intro for all of them. Proposal: Continue on such a save plays the title card, the four intro pages and the Level 15 briefing once, then opens the hangar | — | needs a flag in the save (intro seen) | play it once / skip it | **play it once** (user): in the act README's checklist, built in M7 part B |

## Art — units

The new Act 3 units, a/b each (the 480×540 field over Mars; `tools/concept/enemies_r36.py`).
The Threadcrawler, Shellback and Dust Devil keep their chosen art; the Dust Kite (U.7) was added
after the round opened.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| U.1 | [Burrower](../../enemies/ground/burrower/README.md) | **pick a or b**: a "sand maw", a slate/lime radial worm head (furrow, bulge tell, breach, ring of orbs, submerge); b "sand clam", a bone/violet armoured clam shell that faces the ship | [a](../../enemies/ground/concept/burrower-r36-a.png), [a GIF](../../enemies/ground/concept/burrower-r36-a.gif), [b](../../enemies/ground/concept/rejected/burrower-r36-b.png), [b GIF](../../enemies/ground/concept/rejected/burrower-r36-b.gif) | the slate was lightened to read on the rust; check it against the sand | a / b / redo | **a** (user); b in `concept/rejected/` |
| U.2 | [Tendril Anchor](../../enemies/ground/tendril-anchor/README.md) | **pick a or b**: a polyp clamps on the walls with a fleshy rope link; b spine pylons with a crackling three-strand filament | [a](../../enemies/ground/concept/tendril-anchor-r36-a.png), [a GIF](../../enemies/ground/concept/tendril-anchor-r36-a.gif), [b](../../enemies/ground/concept/rejected/tendril-anchor-r36-b.png), [b GIF](../../enemies/ground/concept/rejected/tendril-anchor-r36-b.gif) | the burning beam is a mock-up, not the Ion Beam's look ([G.1](#art--gear)) | a / b / redo | **a** (user); b in `concept/rejected/` |
| U.3 | [Choir Herald](../../enemies/air/choir-herald/README.md) | **pick a or b**: a "cantor bell", teal-black with a sound-wave aura and escort rings; b "seraph", bone with a floating halo crest and segmented violet rings | [a](../../enemies/air/concept/rejected/choir-herald-r36-a.png), [a GIF](../../enemies/air/concept/rejected/choir-herald-r36-a.gif), [b](../../enemies/air/concept/choir-herald-r36-b.png), [b GIF](../../enemies/air/concept/choir-herald-r36-b.gif) | the role colours: the support's rings must not read as a bullet colour | a / b / redo | **b** (user); a in `concept/rejected/` |
| U.4 | [Ghost Drone](../../enemies/air/ghost-drone/README.md) | **pick a or b**: a "facet dart", a grey faceted arrowhead; b "crescent", a forward-swept wing round a sensor pod; both plain grey, no trim, no markings (hint 1) | [a](../../enemies/air/concept/ghost-drone-r36-a.png), [a GIF](../../enemies/air/concept/ghost-drone-r36-a.gif), [b](../../enemies/air/concept/rejected/ghost-drone-r36-b.png), [b GIF](../../enemies/air/concept/rejected/ghost-drone-r36-b.gif) | see U.5: at 40 px neither shows its red eye or running lights | a / b / redo | **a** (user); b in `concept/rejected/` |
| U.5 | [Ghost Drone: eye and running lights](../../enemies/air/ghost-drone/README.md) | **fix to propose** (found by the Mars step): the spec says "only its red eye and white running lights show at range", and the storm stills ([MA.2](#art--mars), [MA.4](#art--mars)) paint them on, but the drone's own sprite has no running lights in either variant and its red eye (on the 260 px source) is lost at 40 px. Proposal: re-render the chosen variant with an emissive red eye of at least 2×2 px and two white wing-tip running lights (glow drawn additively, like the Vrell glow), so the dust-storm silhouette works from the sprite itself | [storm-radius-r36-a.png](../../world/mars/concept/rejected/storm-radius-r36-a.png) | — | fix in the chosen variant / leave it | **fix in the chosen variant** (user): the art step re-renders Ghost Drone a |
| U.6 | [Warden Tank, unmarked](../../enemies/ground/warden-tank/README.md) | **accept or redo**: the chosen r05-a model with only its livery changed for Act 3: unmarked grey, no gold, no accent, next to r05-a | [sheet](../../enemies/ground/concept/warden-tank-r36-a.png), [GIF](../../enemies/ground/concept/warden-tank-r36-a.gif) | the unmarked livery keeps the red sensors (the grey machines' shared mark) | accept / redo | **accepted** (user): the grey livery chosen |
| U.7 | [Dust Kite](../../enemies/air/dust-kite/README.md) | **pick a or b**: a "diamond kite", a rust membrane on a bone cross strut with a teal-glowing spine and a whip tail; b "crescent sail", a bat-like swept rust sail on bone finger struts, teal nodes along a segmented body; both 24 px, 16 headings × 4 membrane-flex frames, the 0.3 s entry shimmer and a rust dust-puff death (M10 = b) | [a](../../enemies/air/concept/rejected/dust-kite-r36-a.png), [a GIF](../../enemies/air/concept/rejected/dust-kite-r36-a.gif), [b](../../enemies/air/concept/dust-kite-r36-b.png), [b GIF](../../enemies/air/concept/dust-kite-r36-b.gif) | at 24 px the two read alike in motion; the rust-and-teal pairing is the Lamprey's and the Ravager's (told apart by size) | a / b / redo | **b** (user); a in `concept/rejected/` |

## Art — bosses

The Act 3 mid-boss and act boss, a/b each (`tools/concept/bosses_r36.py`).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.1 | [Revenant Walker](../../enemies/bosses/revenant-walker/README.md) | **pick a or b**: a "Strider heavy", an unmarked grey reverse-knee biped, two shoulder missile boxes, a tail flamer, a hot-amber reactor on its back, the wreck's plate ground blank; b "Siege crawler", a low four-legged hull with a dozer prow, vertical launch blocks, a twin flamer, the plate cut off with a torch | [a](../../enemies/bosses/concept/revenant-walker-r36-a.png), [a GIF](../../enemies/bosses/concept/revenant-walker-r36-a.gif), [b](../../enemies/bosses/concept/rejected/revenant-walker-r36-b.png), [b GIF](../../enemies/bosses/concept/rejected/revenant-walker-r36-b.gif) | the GIFs condense the fight (one pass; the spec has passes from every edge and three phases); the hot-amber reactor follows the Ascendancy rule for exposed cores; the spec's *Size* row says "four legs", which fits only b | a / b / redo | **a** (user): the biped; the *Size* and *Look* rows follow; b in `concept/rejected/` |
| B.2 | [Dust Colossus](../../enemies/bosses/dust-colossus/README.md) | **pick a or b**: a "Rearing maw", a segmented slate worm arching out of a cracking sand dome, three jaw plates round rings of teeth and a lime throat; b "Sinkhole colony", the ground collapses into a funnel, four bone mandibles round a lime throat, violet vein ridges under the sand | [a](../../enemies/bosses/concept/rejected/dust-colossus-r36-a.png), [a GIF](../../enemies/bosses/concept/rejected/dust-colossus-r36-a.gif), [b](../../enemies/bosses/concept/dust-colossus-r36-b.png), [b GIF](../../enemies/bosses/concept/dust-colossus-r36-b.gif) | — | a / b / redo | **b** (user); the spec's *Look* follows; a in `concept/rejected/` |

## Art — Mars

The setting's looks (`tools/concept/mars_r36.py`); the chosen round-03 scene stays the base.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| MA.1 | [Canyon walls](../../world/mars/README.md#concept-art) | **pick a or b**: a the rim at scale 1.0 is the collision line and the faces drop to the floor at 0.6 (the decided art-direction rule, the chosen r03-c); b the floor at 1.0 and the walls rise toward the camera to 1.25 like Level 08's towers, the collision at their foot (reads as flying inside the canyon; amends the art-direction rule) | [a](../../world/mars/concept/rejected/canyon-walls-r36-a.png), [a GIF](../../world/mars/concept/rejected/canyon-walls-r36-a.gif), [b](../../world/mars/concept/canyon-walls-r36-b.png), [b GIF](../../world/mars/concept/canyon-walls-r36-b.gif) | Level 16's draft is written for a (gameplay on the rim bands); b moves the anchors to the walls' foot | a / b | **b** (user): amends the art-direction perspective rule; Level 16 adapted (anchors at the walls' foot, the rim bands gone); a in `concept/rejected/` |
| MA.2 | [Storm radius](../../world/mars/README.md#concept-art) | **pick a or b**: a a clear 170 px bubble with a hard 10 px edge, a banded rust veil beyond; b a soft ragged falloff over about 120 px with drifting dust tongues, units fading gradually to silhouettes | [a](../../world/mars/concept/rejected/storm-radius-r36-a.png), [a GIF](../../world/mars/concept/rejected/storm-radius-r36-a.gif), [b](../../world/mars/concept/storm-radius-r36-b.png), [b GIF](../../world/mars/concept/storm-radius-r36-b.gif) | the Ghost Drones' lights are painted on here (U.5) | a / b | **b** (user): in Level 18 and Mars; a in `concept/rejected/` |
| MA.3 | [Ares Landing dome](../../world/mars/README.md#concept-art) | **pick a or b**: a geodesic glass on a concrete ring, green-lit crops inside; b a ribbed composite pressure dome on a regolith berm; both 120 px (Eden 144) with hit and broken states | [a](../../world/mars/concept/dome-r36-a.png), [b](../../world/mars/concept/rejected/dome-r36-b.png) | the states are overlays; production renders them | a / b | **a** (user); b in `concept/rejected/` |
| MA.4 | [Mood stills](../../world/mars/README.md#concept-art) | **accept or redo, as direction** (not production backdrops): one still per sub-location: Olympus descent (L15), Valles with the Helix station (L16), Ares Landing (L17), the storm plains (L18), the Tharsis Foundry (L19), the Hellas basin with the ridge and the lime hollow (L20–L21) | [Olympus](../../world/mars/concept/mars-olympus-r36-a.png), [Valles](../../world/mars/concept/mars-valles-r36-a.png), [Ares Landing](../../world/mars/concept/mars-ares-landing-r36-a.png), [storm plains](../../world/mars/concept/mars-storm-plains-r36-a.png), [Tharsis](../../world/mars/concept/mars-tharsis-foundry-r36-a.png), [Hellas](../../world/mars/concept/mars-hellas-r36-a.png) | the units on them are variant a of tonight's proposals, for scale only | accept all / redo some (say which) | **accepted all** (user), as direction |
| MA.5 | [Panorama](../../world/mars/README.md#concept-art) | **accept or redo, as direction**: a wide 960×540 mood still of the act in a voxel-terrain look (domes, the canyon, Olympus Mons, a storm front) for the title card and the briefing horizons | [panorama](../../world/mars/concept/mars-panorama-r36-a.png) | the weakest still: blocky domes | accept / redo | **accepted** (user), as direction |

## Art — gear

The Act 3 equipment's looks, a/b each (`tools/concept/gear_r36.py`).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| G.1 | [Ion Beam](../../player/weapons/ion-beam/README.md) | **pick a or b**: a the chosen round-08/09 beam family drawn per level (6 → 14 px, overdrive 18 px), first-target contact, a tendril burnt through, rock-scorch, the glance spark over hardened ground; b "ion stream", a white core in a cyan sheath with helix strands and packets, a ring splash, slag beads on rock | [a](../../player/weapons/concept/ion-beam-r36-a.png), [a GIF](../../player/weapons/concept/ion-beam-r36-a.gif), [b](../../player/weapons/concept/rejected/ion-beam-r36-b.png), [b GIF](../../player/weapons/concept/rejected/ion-beam-r36-b.gif) | the rock island is a stand-in for the canyon art (MA.1) | a / b | **a** (user); b in `concept/rejected/` |
| G.2 | [Light drone](../../player/weapons/light-drone-bay/README.md) | **pick a or b**: a a disc drone with a dock-ring bay pod; b a dart drone like a miniature Stormhawk with a launch-rail bay; both with the orbit, convergence, block, break and rebuild | [a](../../player/weapons/concept/light-drone-r36-a.png), [a GIF](../../player/weapons/concept/light-drone-r36-a.gif), [b](../../player/weapons/concept/rejected/light-drone-r36-b.png), [b GIF](../../player/weapons/concept/rejected/light-drone-r36-b.gif) | the HUD's hits-left pips take the place of the weapon's level pips | a / b | **a** (user); b in `concept/rejected/` |
| G.3 | [EMP Burst](../../player/specials/README.md) | **pick a or b**: a a ring with crackling arcs, machines' lights out for 3 s, the Vrell's glow dimmed for 1.5 s; b "arc front", a dashed double ring forking into each unit, flicker and a spark cage | [a](../../player/specials/concept/emp-burst-r36-a.png), [a GIF](../../player/specials/concept/emp-burst-r36-a.gif), [b](../../player/specials/concept/rejected/emp-burst-r36-b.png), [b GIF](../../player/specials/concept/rejected/emp-burst-r36-b.gif) | — | a / b | **a** (user); b in `concept/rejected/` |
| G.4 | [Shield Overcharge](../../player/specials/README.md) | **pick a or b**: a a gold flare and shimmer, the shield bar 0–90 with a gold upper half, the cooldown ring; b a white-hot double shell, the bar 0–45 with white second-layer cores | [a](../../player/specials/concept/rejected/shield-overcharge-r36-a.png), [a GIF](../../player/specials/concept/rejected/shield-overcharge-r36-a.gif), [b](../../player/specials/concept/shield-overcharge-r36-b.png), [b GIF](../../player/specials/concept/shield-overcharge-r36-b.gif) | a's gold is the Ascendancy's colour; the special row is too narrow for "OVERCHARGE" | a / b | **b** (user): the Overcharge is white in the specials document; a in `concept/rejected/` |

## Art — characters

Act 3's one new radio portrait, in the chosen style B (`tools/concept/portraits_r36.py`), added after
the round opened.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [Ares Control's radio portrait](../../story/characters/README.md#concept-art) | **pick a or b**: Level 15's `radio-generic-colonial`, Ares Landing's flight controller (Levels 15–17 and 19–21), older than Rook, proud and tired: a "the old controller", a dust-orange militia cap, a boom mic and a rust work jacket; b "the dust-suit controller", the same man as a surface worker, goggles on the brow and a respirator. Both on the **teal** friendly channel like the CDF officer (amber stays the enemy's) | [a](../../story/characters/concept/rejected/generic-colonial-r36-a.png), [b](../../story/characters/concept/generic-colonial-r36-b.png) | on the teal channel he is told from the CDF officer only by the cap, the rust clothes and his age | a / b / redo | **b** (user), the teal channel **yes** (C.1a); a in `concept/rejected/` |

**Proposals (row C.1)**

- **(a)** Ares Control stays on the teal friendly channel (the colony is a friend; amber stays the
  enemy's), not a channel colour of his own.

## Audio

Sketches and sounds for Act 3 (`tools/concept/audio/music_r36b.py`, `sfx_r36b.py`; two CC-BY
sources, in CREDITS.md). No bubbly or popping layers.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [Track 9, the Act 3 B theme](../../audio/music/README.md) | **pick a or b**: a "Oxide Drift", 126 BPM, A harmonic minor / E phrygian dominant, track 8's hand drums and reed over techno, Vorne's motif on cold organ and brass far back; b "Grey Signal", 125 BPM D minor dub techno, the motif as an intercepted whistle through a radio band-pass. Both about 65 s sketches with the motif in its own group | [a](../../audio/music/concept/rejected/act3-b-theme-r36-a.ogg), [b](../../audio/music/concept/act3-b-theme-r36-b.ogg) | the brief said B phrygian at 122 BPM; neither sketch keeps that key (Q.24) | a / b / redo | **b** (user): track 9's brief follows "Grey Signal"; a in `concept/rejected/` |
| S.2 | [Burrow eruption](../../audio/sfx/README.md) | **pick a or b**: a "sand burst", the tell's rumble swelling over 1.0 s, then an impact and sand raining down; b "the swimmer breaks out", sand churning, a muffled growl, gravel thrown up (2.1 s each) | [a](../../audio/sfx/concept/burrow-eruption-r36-a.ogg), [b](../../audio/sfx/concept/rejected/burrow-eruption-r36-b.ogg) | — | a / b / redo | **a** (user); b in `concept/rejected/` |
| S.3 | [Canyon wall scrape](../../audio/sfx/README.md) | **pick a or b**: a "rock grind", the hull dragged on stone with grit falling (0.65 s); b "metal screech" with a spark hiss (0.6 s) | [a](../../audio/sfx/concept/rejected/canyon-scrape-r36-a.ogg), [b](../../audio/sfx/concept/canyon-scrape-r36-b.ogg) | — | a / b / redo | **b** (user); a in `concept/rejected/` |
| S.4 | [Ion Beam loop](../../audio/sfx/README.md) | **pick a, b or keep**: a "the chosen hum, heavier", the round-04 loop's source with an ion body (2.6 s loop); b "ion stream", synthesized buzz and shimmer (2.0 s loop); or keep the chosen round-04 loop unchanged | [a](../../audio/sfx/concept/ion-beam-loop-r36-a.ogg), [b](../../audio/sfx/concept/rejected/ion-beam-loop-r36-b.ogg), [r04-a](../../audio/sfx/concept/shot-beam-r04-a.ogg) | — | a / b / keep r04-a | **a** (user); b in `concept/rejected/` |
| S.5 | [Dust-storm bed](../../audio/sfx/README.md) | **pick a or b**: Level 18's `mars-storm` ambience, a seamless 16 s loop: a "recorded front", a sandstorm over a furious low howl; b "synthesized front", a roar, grit hiss and a wandering howl swelling with gusts | [a](../../audio/sfx/concept/dust-storm-bed-r36-a.ogg), [b](../../audio/sfx/concept/rejected/dust-storm-bed-r36-b.ogg) | — | a / b / redo | **a** (user); b in `concept/rejected/` |
| S.6 | [Ares Control's voice](../../audio/voice/README.md#concept-art) | **pick a or b**: casting Ares Control, three of his lines (Level 15's t=12 jab, Level 17's dome names, Level 21's closing line) through radio filter b at his drier base settings: a reader David Wales (clip 102 Hz, 21.7 s); b reader Thomas A (clip 112 Hz, 25.2 s). The text as spoken uses the proposed respellings, answered below | [a](../../audio/voice/concept/rejected/voice-ares-control-r36-a.ogg), [b](../../audio/voice/concept/voice-ares-control-r36-b.ogg) | measured only, not listened to; b holds a pause of about 2 s inside its second and third lines | a / b / redo | **b** (user): Thomas A, his mid-line pauses noted for the render step; the respellings (a)–(c) **yes**; a and its reference clip in `concept/rejected/` |

**Proposals (row S.6)**

- **(a)** Ares is spoken from the respelling "Airees" (AIR-eez).
- **(b)** Schiaparelli is spoken from the respelling "Skyaparelli" (skyah-pah-REL-ee).
- **(c)** Burroughs is spoken from the respelling "Burrows".

## Documents to approve

Every Act 3 document written tonight (`design: draft`). *Approve* sets it to `approved` after the
round's other answers are worked in; *change* says what. The texts the player reads or hears are
quoted under the rows.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| D.1 | [Act 3 – Red Dust](../../campaign/act-3-red-dust/README.md) | **approve or change**: the act README: the Contents of seven levels, the timeline (nine days' crossing, five weeks of siege, June 2185), the title card, the act intro (four pages) and outro (four pages, the Vrell leave Mars for Europa), the units with their HP factors, the new-mechanics table, the density band 60–72 a minute (breather 48–55) applied to Levels 15, 16, 17 and 19, the threat profiles, music and voice per level (one colony voice, Ares Control) | — | the texts below are drafts; Okafor's "We fly for them anyway" carries the colonists' resentment | approve / change | **approved** (user) |
| D.2 | [Level 15 – Olympus Descent](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md) | **approve or change**: 190 s in five sections, the descent through two cloud decks; the Burrower's first sight at t=68; Dust Kite popcorn; 200 enemies ≈ 65 a minute; `bounty_scale` 0.42; a drop-pod secret; Ares Control's first lines | [Olympus still](../../world/mars/concept/mars-olympus-r36-a.png) | Skitter streams 4 → 3 in the harmonising; the secret is 14 % of the budget (Q.15) | approve / change | **approved** (user) |
| D.3 | [Level 16 – Valles Canyon Run](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md) | **approve or change**: terrain collision (M1–M3), the mesa fork, minimum width 180 px, six tendril gates, the Coprates rapids at 230 px/s, the Candor hut secret; 215 enemies ≈ 70 a minute; `bounty_scale` 0.54; the Helix glimpse | [Valles still](../../world/mars/concept/mars-valles-r36-a.png) | the wall camera model (MA.1); new radio events (first surfacing, first wall contact) | approve / change | **approved** (user) |
| D.4 | [Level 17 – Dome Siege](../../campaign/act-3-red-dust/level-17-dome-siege/README.md) | **approve or change**: five named domes as allies (M16), the secondary "No dome hit"; the Shellback and Choir Herald introduced; the Helix compound set piece and "Not on this channel"; 172 s, 196 enemies ≈ 70 a minute; `bounty_scale` 0.60 | [Ares Landing still](../../world/mars/concept/mars-ares-landing-r36-a.png) | Shellbacks threaten the domes too (Q.17); the Helix swarm 10 → 8 in the harmonising | approve / change | **approved** (user) |
| D.5 | [Level 18 – Dust Storm](../../campaign/act-3-red-dust/level-18-dust-storm/README.md) | **approve or change**: the act's breather, 183 s; the visibility radius (M6); Dust Devils; the Ghost Drones' first sight at t=69.5 (hint 1) with Vorne's motif; 149 enemies ≈ 50 a minute; `bounty_scale` 0.92; secondary 65 % kills | [storm still](../../world/mars/concept/mars-storm-plains-r36-a.png) | the radius and pickups' glints (Q.18) | approve / change | **approved** (user) |
| D.6 | [Level 19 – Foundry of Tharsis](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md) | **approve or change**: Warden Tanks on conveyors (40 px/s up), furnace vents, turrets firing after the ship passes; the Revenant Walker mid-boss and the hint-2 wreck crawl; 172 enemies ≈ 62 a minute; `bounty_scale` 0.52 (walker bounty 262, 14.8 %); secondary 12 of 14 tanks | [Tharsis still](../../world/mars/concept/mars-tharsis-foundry-r36-a.png) | the turrets are up-facing Spine Turrets (a new `arc` parameter) | approve / change | **approved** (user) |
| D.7 | [Level 20 – Retreat from Hellas](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md) | **approve or change**: 20 s of strike, then the reverse scroll (M4, M5); the Mote Swarm pursuit from the top (53 of 178 enemies), ambushes from the bottom; a TOP edge warning; ≈ 64 a minute; `bounty_scale` 0.95; Rook's quiet beat | [Hellas still](../../world/mars/concept/mars-hellas-r36-a.png) | a new `pursuit` formation; the scale is high (Q.20) | approve / change | **approved** (user) |
| D.8 | [Level 21 – Dust Colossus](../../campaign/act-3-red-dust/level-21-dust-colossus/README.md) | **approve or change**: 105 s of approach (rim, inner wall, dune sea with the ridge pass, lime hollow) ≈ 66 a minute, the Colossus on an arena crawling at 20 px/s, 32 s of settling dust with the Choir's first words; Level 14's act-end order (the release's end moves to Level 21, the hangar before L22 reads `ACT IV - DEEP WATER: COMING SOON`); budget 2,709, `bounty_scale` ≈ 0.80 | [Hellas still](../../world/mars/concept/mars-hellas-r36-a.png) | the boss's bounty share 22.7 % against the spec's 30 % (Q.21) | approve / change | **approved** (user) |
| D.9 | [Burrower](../../enemies/ground/burrower/README.md) | **approve or change**: sand-swimmer under the new `buried` layer; 1.0 s bulge tell, surfaces, a `ring`, dives after 2 s; HP 44, bounty 24 (L15) | — | — | approve / change | **approved** (user) |
| D.10 | [Tendril Anchor](../../enemies/ground/tendril-anchor/README.md) | **approve or change**: anchor pairs (12 HP, bounty 12) with a 40 HP tendril hit only by `beam`, `medium` 6 on touch (M9) (L16) | — | — | approve / change | **approved** (user) |
| D.11 | [Threadcrawler](../../enemies/ground/threadcrawler/README.md) | **approve or change**: a ground centipede of 10–16 segments on canyon floors and walls (head 80, 8 a segment, tail 20), a travelling spore wave (L16) | — | — | approve / change | **approved** (user) |
| D.12 | [Shellback](../../enemies/ground/shellback/README.md) | **approve or change**: an armoured beast (body 120, vent 24 HP) with a spore mortar; curls and rolls once, crushing, then shows its belly (L17) | — | — | approve / change | **approved** (user) |
| D.13 | [Dust Devil](../../enemies/ground/dust-devil/README.md) | **approve or change**: a spinning vortex (52 HP, bounty 26) that pulls the ship and pickups; its core opens once a cycle (L18) | — | — | approve / change | **approved** (user) |
| D.14 | [Warden Tank](../../enemies/ground/warden-tank/README.md) | **approve or change**: unmarked grey in Act 3, hull and turret (64 + 20), the turret fires back up the screen after passing (L19) | — | — | approve / change | **approved** (user) |
| D.15 | [Choir Herald](../../enemies/air/choir-herald/README.md) | **approve or change**: a support flyer (60 HP, bounty 30) whose aura rings shield nearby enemies (M8) (L17) | — | — | approve / change | **approved** (user) |
| D.16 | [Ghost Drone](../../enemies/air/ghost-drone/README.md) | **approve or change**: a grey human-built strafing drone (14 HP, bounty 14), one re-aimed 3-shot burst per pass with a 0.3 s eye-glint tell; hint 1 (L18) | — | the `machine` flag for the EMP is new in the stat block | approve / change | **approved** (user) |
| D.17 | [Dust Kite](../../enemies/air/dust-kite/README.md) | **approve or change**: the tiny Martian popcorn glider (1 HP, bounty 4) that rams (M10 = b), in every Act 3 level | — | art: see [U.7](#art--units); the enemies index's variety row does not list it yet | approve / change | **approved** (user) |
| D.18 | [Revenant Walker](../../enemies/bosses/revenant-walker/README.md) | **approve or change**: the Act 3 mid-boss: passes on lanes with 3 s edge warnings, a front plate, two homing pods, two vents, a rear flame cone; phases Stride / Hunt / Overheat; HP 2,200 at reference 92 (par 60 s); bounty 262 (Level 19's scale); the wreck crawl (hint 2) | — | the *Size* row reads "four legs" (only B.1's b) | approve / change | **approved** (user) |
| D.19 | [Dust Colossus](../../enemies/bosses/dust-colossus/README.md) | **approve or change**: the act boss, buried, breaching front, left, right and rear, then under the ship (1.5 s marker); mouths open 7 s, bite `heavy` 10, suction 45 px/s, debris, spine vents; HP 5,600 + 840 (par 130 s); the Choir speaks at its death | — | the bounty follows Level 21's 300 (Q.21) | approve / change | **approved** (user) |
| D.20 | [Ion Beam](../../player/weapons/ion-beam/README.md) | **approve or change**: the front `beam`: 45 → 130 DPS, 5 000 cr, from L15; the beam rules (first target, walls, tendrils, hardened targets passed over, reaches `high-air`); the M11 power check | — | — | approve / change | **approved** (user) |
| D.21 | [Rear Lance](../../player/weapons/rear-lance/README.md) | **approve or change**: a rear piercing lance, 18 → 60 DPS, draw 3 → 5, 2 800 cr, from L17; walls stop it; in the reverse scroll "rear" stays the screen's bottom | — | — | approve / change | **approved** (user) |
| D.22 | [Deflector Pod](../../player/weapons/deflector-pod/README.md) | **approve or change**: a wing pod with a half-disc field (22 → 30 px) catching 3 → 5 bullets, recharging every 0.67 → 0.4 s; draw 2 → 3, 1 600 cr, from L18; bullets only | — | — | approve / change | **approved** (user) |
| D.23 | [Light Drone Bay](../../player/weapons/light-drone-bay/README.md) | **approve or change**: a wing bay launching a drone: 10 → 30 DPS, 8 → 12 hits, rebuilt in 6 → 4 s, draw 2 → 3, 2 000 cr, from L15; aims at a convergence point | — | — | approve / change | **approved** (user) |
| D.24 | [Specials, Act 3 rows](../../player/specials/README.md) | **approve or change** (draft rows in an approved document): the EMP Burst (machines 3 s, Vrell 1.5 s, auras and shields stripped, bosses never stunned), the Orbital Lance (a 40 px column for 3 s, 400 a second, at most 400 per boss part) and the Shield Overcharge (twice the capacity for 8 s, 45 s cooldown); the cooldown rules and the HUD ring | — | the HUD and controls documents do not have the cooldown ring and the dash pip yet | approve / change | **approved** (user) |
| D.25 | [Systems, Act 3 sections](../../player/systems/README.md) | **approve or change** (draft sections in an approved document): the Evasive thrusters (a dash of 72 px in 0.12 s by double-tap or a button, 0.25 s invulnerable, 3 s cooldown) and the third utility bay (on sale once from L15) | — | the thrusters' 2 MW do not fit a typical Act 3 loadout | approve / change | **approved** (user) |
| D.26 | [Wingmen, Act 3 drones](../../player/wingmen/README.md) | **approve or change** (draft sections in an approved document): the light drone (orbit 36 px, convergence 140 px, break and rebuild) and the rear-guard drone (40 px behind, fires backwards, 9 → 26 DPS; its bay needs a weapons document in part G) | — | — | approve / change | **approved** (user) |
| D.27 | [Mars](../../world/mars/README.md) | **approve or change**: the geography (west to east, then south), the sub-locations per level, the Helix sites (the Valles rim station and the Ares Landing compound), the five domes, the hazards, the natives linked to their units | — | — | approve / change | **approved** (user) |
| D.28 | [Music, Act 3](../../audio/music/README.md) | **approve or change** (a draft section in an approved document): track 8 in Levels 15–17 and 20, track 9 in 18, 19 and the Level 21 approach; Vorne's motif as a third stem in Levels 18–19, muted in 21; the cues per level | — | track 8 exists only as a 53.8 s sketch (its full length is part B's) | approve / change | **approved** (user) |
| D.29 | [Sound effects, Act 3](../../audio/sfx/README.md) | **approve or change** (a draft section in an approved document): Act 3's sounds with briefs (terrain, units, bosses, equipment) and five Mars ambiences; reuses where a chosen file fits; a unit's loop plays once per kind, for the nearest unit | — | — | approve / change | **approved** (user) |
| D.30 | [Voice, Act 3](../../audio/voice/README.md) | **approve or change** (a draft section): Ares Control (a man, older than Rook, about 39 takes, cast a/b in part B), Lance Control (the Orbital Lance's caller), Okafor's refrain as one file, the Choir's speech in fragments, a respelling list for the Mars names | — | — | approve / change | **approved** (user) |

**Act intro and outro (row D.1)**

> ACT III · RED DUST · Mars · June 2185 (the title card, 3.5 s over the act's still)

> **Page 1, Commander Okafor (grim):** "Nine days at full burn. Ares Landing has now held alone for five weeks: no fleet, no supply drops, only militia guns and the air its domes still make. Every colony on Mars is cut off from every other."
> **Page 2, Dr. Varga:** "The Vrell came down in the Hellas basin, in the south, and spread north from there. They have changed for Mars: they swim through the sand and ride the dust storms. They are learning this planet faster than we did."
> **Page 3, Commander Okafor:** "The colonists asked Earth for help long before the Gate opened, and got committees. They will not thank us for coming late. They don't have to. We fly for them anyway."
> **Page 4, Commander Okafor (fierce):** "We go in from the west, past Olympus Mons, and take the canyons back one at a time. Drop in ninety minutes. Mission briefing follows. Aegis Actual out."

The outro, after the Level 21 debrief (track 24 under page 1):

> **Page 1, Commander Okafor (grim):** "The Colossus is dead. Within a day the Vrell lifted off from every site on Mars: the basin, the plains, the foundry. Ares Landing opened its shelters this morning, for the first time in six weeks."
> **Page 2, Commander Okafor:** "The colonists have not forgiven Earth for coming late, and they are right not to. But last night every dome in Ares Landing burned its lights for Aegis Wing. Take that with you."
> **Page 3, Dr. Varga (grim):** "I tracked their fleet off Mars. It is not going home. Every course bends toward Jupiter and ends at one moon: Europa. It told us '*we are going*'. Now we know where."
> **Page 4, Commander Okafor (fierce):** "Europa means Thera Deep and Conamara Station, and Conamara reports tremors under the ice. We refit, and we follow them. Aegis Wing leaves Mars in four days. Aegis Actual out."

## Open questions

The questions the drafts raised, one row per document. Each proposal is answered on its own:
*yes* takes it, *no* says what instead (in the note).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| Q.15 | [Level 15](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md#open-questions) | **proposals**: (a)–(d) below | — | — | all yes / some no | **all yes** (user) |
| Q.16 | [Level 16](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#open-questions) | **proposals**: (a)–(d) below (the wall camera model is [MA.1](#art--mars)) | — | — | all yes / some no | **all yes** (user) |
| Q.17 | [Level 17](../../campaign/act-3-red-dust/level-17-dome-siege/README.md#open-questions) | **proposals**: (a)–(d) below | — | — | all yes / some no | **all yes** (user) |
| Q.18 | [Level 18](../../campaign/act-3-red-dust/level-18-dust-storm/README.md#open-questions) | **proposals**: (a)–(d) below | — | — | all yes / some no | **all yes** (user) |
| Q.19 | [Level 19](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#open-questions) | **proposals**: (a)–(e) below | — | — | all yes / some no | **all yes** (user) |
| Q.20 | [Level 20](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#open-questions) | **proposals**: (a)–(f) below | — | — | all yes / some no | **all yes** (user) |
| Q.21 | [Level 21](../../campaign/act-3-red-dust/level-21-dust-colossus/README.md#open-questions) | **proposals**: (a)–(f) below | — | — | all yes / some no | **all yes** (user) |
| Q.22 | [The units](../../enemies/README.md) | **proposals**: (a)–(i) below, one per unit document | — | — | all yes / some no | **all yes** (user) |
| Q.23 | [The bosses and the arsenal](../../enemies/bosses/README.md) | **proposals**: (a)–(i) below | — | — | all yes / some no | **all yes** (user) |
| Q.24 | [The act, audio and voice](../../campaign/act-3-red-dust/README.md#open-questions) | **proposals**: (a)–(h) below | — | — | all yes / some no | **all yes** (user) |
| Q.25 | [Bookkeeping](../../tech/roadmap/README.md) | **proposals**: (a)–(f) below, the shared documents the drafts touch but did not change | — | — | all yes / some no | **all yes** (user) |

**Proposals (row Q.15)**

- **(a)** Ares Control gets a new generic colonial radio portrait (`radio-generic-colonial`), cast a/b in part B's round.
- **(b)** A submerged Burrower counts as on the screen for `PacingTest` (its ripple is visible).
- **(c)** The drop-pod secret stays 100 in Act 1 terms (256 at ×2.56, 14 % of the budget).
- **(d)** The Burrower's formation is called `field` (not the spec's `swarm`).

**Proposals (row Q.16)**

- **(a)** A new secondary kind `links` (break every tendril gate), not the standard 65 % kill ratio.
- **(b)** Wall contact costs 6 on easy (10 on medium and hard).
- **(c)** The Helix glimpse here (Varga's observation, Okafor's "Eyes on the canyon, Doctor."), the silencing in Level 17; not the whole beat in Level 17.
- **(d)** Rook trails the player at 240 px through the canyon.

**Proposals (row Q.17)**

- **(a)** The hooked Shellbacks' mortar and roll hurt the domes too (not bombers only, as M16 reads).
- **(b)** "No dome hit" is all or nothing, tuned by measurement as Level 11's convoy (not "4 of 5 untouched").
- **(c)** `bounty_scale` 0.60, above M12's 0.3–0.5, accepted.
- **(d)** Ares Control is the colony's one voice across the act (Ares Warden merged into him).

**Proposals (row Q.18)**

- **(a)** The radius 170 / 150 px (easy 200 / 180, hard 150 / 130), checked in part E's captures.
- **(b)** Pickups outside the radius show as a glint (not hidden like the ground).
- **(c)** `bounty_scale` 0.92 for the breather, kept (not densified to ≈ 55 a minute at 0.84).
- **(d)** One Helix sentence on Okafor's briefing page (M13 = a's "briefings carry it"), not only in the hangar intel.

**Proposals (row Q.19)**

- **(a)** The rear-firing turrets are up-facing Spine Turrets (`arc: up`), not a new grey machine turret.
- **(b)** The secondary is "12 of 14 grey tanks" (not all 14, nor the 65 % kill ratio).
- **(c)** The furnace vents fire every 3.5 s for `heavy` 10 once per burst (not `medium` 6).
- **(d)** Kite pairs at the edges during the wreck crawl keep `PacingTest` green (the crawl does not count as one of the level's two longer pauses).
- **(e)** `bounty_scale` 0.52, slightly above M12's 0.3–0.5, accepted.

**Proposals (row Q.20)**

- **(a)** The strike opener: 20 s of normal scroll, then the turn (not reversed from the start).
- **(b)** The pursuit: 10 motes plus refills of 3 every 10 s, a lunge every 6 s at 240 px/s, tuned in part G's captures.
- **(c)** Bottom entries unwarned after the first three (not warned throughout like a rear entry).
- **(d)** Rook's quiet beat plays without `requires: escort` (not only with Rook in the slot).
- **(e)** Track 8 for the retreat (not track 9, as the roadmap's part G row reads).
- **(f)** `bounty_scale` 0.95 kept (no pricier units to bring it toward 0.6).

**Proposals (row Q.21)**

- **(a)** The boss's share 22.7 % (300 in Act 1 terms at scale 0.80, the perfect-run bound); the spec's 790 does not hold with this level's mix (not reshaping the approach to reach 25–27 %).
- **(b)** The arena's crawl is a seamless tile on real seconds, the clock halting at t=245 (not a long unique strip).
- **(c)** The secondary is the 65 % kill ratio (not "destroy 6 spine vents").
- **(d)** Vorne's motif muted in Level 21 (the Colossus is pure Vrell).
- **(e)** Five closing lines (≈ 34 s, the debrief waits), not shortened to ≈ 26 s without Ares Control's line.
- **(f)** ≈ 66 enemies a minute is right for the act's finale.

**Proposals (row Q.22)**

- **(a)** Burrower: in Level 18's storm only its bulge shows, not its ripple.
- **(b)** Tendril Anchor: a broken tendril does not regrow while both anchors live.
- **(c)** Threadcrawler: once per level it crosses the canyon floor from wall to wall, its body across the path.
- **(d)** Shellback: the roll follows its authored path (it does not steer toward the domes).
- **(e)** Dust Devil: a Smart Bomb or Airstrike hits it while closed (blasts ignore the vanes' armour).
- **(f)** Warden Tank: the intel lists the Act 3 tanks as "unidentified", not Ascendancy.
- **(g)** Choir Herald: a unit's ring thins as its absorb value drains.
- **(h)** Ghost Drone: the intel shows "unidentified contacts" until Level 18 is first flown.
- **(i)** Dust Kite: the name and the unit (M10 = b).

**Proposals (row Q.23)**

- **(a)** Revenant Walker: it leaves the screen between passes (3 s under an edge warning), not turning on the spot.
- **(b)** Revenant Walker: the flame deals `heavy` 10 once per burn (not `small` ticks).
- **(c)** Dust Colossus: the suction pulls the ship gently (any engine outruns it), not only the debris.
- **(d)** Dust Colossus: the debris is rock hazards (not partly ore pickups).
- **(e)** Dust Colossus: phase 3 breaches under the ship (not at random points).
- **(f)** Ion Beam: draw 4 → 8 (not the roster's 5 → 9), L1 at 45 DPS (not 35), upgrades on a 2 500 base.
- **(g)** Ion Beam: no `shield-breaker` (the EMP Burst strips shields from L15).
- **(h)** Deflector Pod: a caught bullet pays nothing back.
- **(i)** The walker's weak point is a hot-amber reactor on its back (the Ascendancy rule for exposed cores), as both concepts draw it.

**Proposals (row Q.24)**

- **(a)** The density band 60–72 a minute (the breather 48–55) for the act, as applied to Levels 15, 16, 17 and 19.
- **(b)** The timeline: nine days' crossing, five weeks of siege, June 2185 (not "three weeks" and a near-instant crossing).
- **(c)** Okafor's refrain "Not on this channel, Doctor." in Levels 17 and 19, varied in Level 18; Varga's approved Act 3 line in Level 17.
- **(d)** Vorne's motif as a third lockstep stream (not five premixed files).
- **(e)** Track 9's key and tempo follow the picked sketch (S.1), not the brief's B phrygian at 122 BPM.
- **(f)** The Mars names respelled for the voice (Ares, Schiaparelli, Valles Marineris, Lycus, Coprates, Eos, Burroughs), not seed pins only.
- **(g)** The Orbital Lance's caller is "Lance Control".
- **(h)** The geography: Ares Landing east of the Valles' end on the edge of the northern plains, the foundry back west on the plateau.

**Proposals (row Q.25)**

- **(a)** The roadmap gets the M7 row and its parts A–I from the gap analysis (§4.1), with track 9 moved from part G to part E.
- **(b)** The Ares Landing dome becomes its own allies document in part D (now a draft roster row).
- **(c)** The architecture's secondary kinds get `untouched` (Level 17) and `links` (Level 16), the formations `pursuit` (Level 20) and `field` (Level 15), the Spine Turret an `arc` parameter (Level 19), in the parts that build them.
- **(d)** The enemies index's variety checklist lists the Dust Kite as Act 3's new `tiny` unit.
- **(e)** The HUD and controls documents get the cooldown ring and the dash pip in part A.
- **(f)** The Revenant Walker's *Size* row follows the variant picked in B.1.

## Decisions

- 2026-10-12: Round opened (the Act 3 design phase, built overnight on the user's behalf; every
  M question answered with the gap analysis's recommendation, every row pending the user's
  review). The board uses `board.py --subjects` because round 34 already holds files named `r36`.
- 2026-10-12: Three extra proposals added after the round opened: U.7 (the Dust Kite a/b), C.1 (Ares
  Control's radio portrait a/b, the teal channel) and S.6 (Ares Control's voice a/b, the respellings).
- 2026-10-13 (user): closed as summarised at the top; the rejected variants moved to
  `concept/rejected/` and every part's statuses, Concept art rows and Decisions updated (step N1);
  the board regenerated with the same `--subjects` (the rejected files show as rejected).
