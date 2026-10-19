---
title: Level 16 – Valles Canyon Run
design: approved
implementation: in-progress
art: final
depends-on: [../../../enemies/ground/tendril-anchor, ../../../enemies/ground/threadcrawler, ../../../enemies/air/dust-kite, ../../../player/weapons/ion-beam, ../../../player/wingmen, ../../../world/mars, ../../../art-direction]
updated: 2026-10-19
---

# Level 16 – Valles Canyon Run

## Summary

The canyon run: Aegis Wing flies the Valles Marineris east toward Ares Landing, the one road the
Vrell do not hold from the air. The level introduces **terrain collision**: canyon walls that
narrow the flyable area and hurt on touch, a rock island that splits the canyon into two channels,
and a fast scroll. The Vrell have strung the canyon shut with
[Tendril Anchor](../../../enemies/ground/tendril-anchor/README.md) barriers (a `beam` burns
through the tendril) and a [Threadcrawler](../../../enemies/ground/threadcrawler/README.md)
crawls along the walls' foot. On the north rim the Helix research station stands untouched. 190 s of
scroll in six sections, no boss.

**Status: approved in concept round 36 (user, 2026-10-13).** The level carries the terrain design
(M1–M3 of the gap analysis) with the recommended options, and the canyon's camera model **b**
(MA.1: the walls rise toward the camera, the collision at their foot; the ship flies inside the
canyon). M7 part C builds it: the numbers live in [data.yaml](data.yaml) (step C2) and the tables
below are rendered from it (`tools/sync_tables.py`).

## Briefing

Two pages, each one screen with its image (`BriefingLayoutTest`), at most four lines of text.
Texts (approved in round 36):

<!-- data: briefing -->
> **Commander Okafor:** "Ares Landing is two thousand kilometres east, and the one road the
> Vrell don't hold from the air is the Valles Marineris. Fly it fast and keep off the walls.
> Rook, stay on Lancer's tail in the narrows."
>
> **Dr. Varga:** "They grow barriers across the canyon: an anchor on each wall and a burning
> tendril between. Kill either anchor and it falls; a beam burns through the tendril. Something
> long crawls along the walls, too."
<!-- /data -->

*Images* (production art; [briefing images](../../../ui/briefing/README.md)): Okafor's page
`level-16-canyon-route` (the Valles from orbit on a CDF plot: the route from the Noctis edge
through the fork, the Coprates rapids, the narrows and the Eos outflow toward Ares Landing; on the
north rim a small **unlabelled** black square, the Helix station, which the plot does not name: a
quiet hint), Varga's `level-16-tendril-scan` (an anchor pair across a cut through the canyon, the
tendril between them, an Ion Beam burning through it, a Threadcrawler at a wall's foot).

*Hangar teaser* (shop screen before L16, Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "Canyon run. Walls on both sides and nowhere to swerve. Scrape the paint and I'm
> telling Okafor."
<!-- /data -->

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule); the
gates are ground targets (linked nests), so the wave strip marks them apart (`GATE`), as it marks
the crawlers (`CRAWLER`).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `ground` (tendril anchors, Threadcrawlers and turrets at the walls' foot, a Burrower field on the outflow's floor), `air`; `low-air` dust plumes and `high-air` rock arches (scenery) |
| Attack directions | front 100% |
| Density | 4 |
| Recommended traits | `forward`, `piercing`: `piercing` for the Threadcrawler; an anchor kill drops each tendril (a beam also burns it, once the ship has one: the plan buys the Ion Beam at Level 18) |
| Hazards | **Canyon walls** (terrain collision: 10 per touch and a push-back), **tendril barriers** (6 per touch) |
| Boss / mid-boss | none |
| Sensor-suite detail | none: Mars, Valles Marineris, `ground` + `air`, front · L1: + directions, density 4, hazards "canyon walls" and "barriers", the objective `REACH ARES LANDING` · L2: + Tendril Anchor (new), Threadcrawler (new), Dust Kite, Skitter, Needler and Mantis portraits; no boss · L3: + `forward` and `piercing` highlighted, the wave strip marking the six barriers and the two crawlers, 1 secret |
<!-- /data -->

*Varga's intel line* per sensor-suite level (in the data's `threat_profile.varga`):

| Sensor suite | Line |
|---|---|
| none | "The Valles Marineris, Lancer. Walls on both sides, and the Vrell have strung it shut." |
| L1 | "Everything comes from ahead. The walls are the danger: keep off them." |
| L2 | "New: tendril barriers on the walls, and a long crawler." |
| L3 | "Kill an anchor to drop a tendril. A cache down the left fork." |

## Objective

- **Primary** `reach-end`: reach the canyon's eastern end (the Eos outflow).
- **Secondary** "Open road" (a new kind `links`): break **every tendril barrier**
  before it leaves the screen, by killing an anchor or burning the tendril: **9** tendrils at
  medium (easy 7, hard 10). Failed at the first tendril that leaves the bottom edge standing;
  decided when the last one is gone (gate 6, t≈155), with its line. The HUD's tracker
  `BARRIERS n / 9`, the debrief's row `BARRIERS BROKEN n / 9`. Pays **80** in Act 1 terms (205
  in Act 3). It rewards shooting the gates rather than eating the 6-point touch as they pass;
  every gate stands in the shared canyon (none in the fork's channels), so either channel keeps
  it reachable (the new kind decided in round 36, Q.16a).

## Layout

A fast level (the art direction's fast range 190–240 px/s): **190 px/s** on the plateau and the
canyon mouth, **200** in the canyon, **180** in the gate stretches (the fork and the narrows: a
gate entering at the top edge then reaches the ship's row in ≈ 2.5 s, the anchor spec's
2.5–3.5 s), **230** in the Coprates rapids, which carry no gates, and **200** for the outflow; each
change eased over 1 s (the moving sections' `ramp`). The launch takes the first 5 s (from the rail
head at Lycus, a CDF field pad, scenery). About **190 s**, 37,245 px. Rook flies from the escort
slot (M17 = a).

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Noctis Edge | 0–25 | 0–4,750 | 190 | light | `ground`: the plateau east of Lycus, the labyrinth of Noctis cracking into chasms; from t=15 the ship drops into the canyon mouth, the floor becomes the ground layer and the walls rise at the field's edges. `far`: the first canyon floor through the cracks. `high-air`: thin dust. | Popcorn on the plateau; the walls close in (480 → 400 px) with nothing on them. |
| 2. Into the Canyon | 25–60 | 4,750–11,745 | 200 | light | `ground`: the canyon floor, a dry riverbed, with ledges at the walls' foot. The walls rise toward the camera to the plateau (1.25), burnt cliff habitats of the colonies on their faces (scenery). `low-air`: dust plumes in the canyon. | Gate 1 alone (t=34), gates 2 and 3; the first ledge turrets and mortars. |
| 3. Fork One | 60–95 | 11,745–18,055 | 180 | light | `ground`: A **mesa** (rock island) splits the canyon into two 150 px channels from t=66 to t=80, the left channel past the **Candor relay hut** at the left wall's foot (the secret), the right channel past a turret ledge at the mesa's foot. | The branch (M3 = a); the Threadcrawler's first sight along the left wall (t=84) after the channels rejoin. |
| 4. Coprates Rapids | 95–125 | 18,055–24,930 | 230 | light, medium peak 110–120 | `ground`: a rock pillar mid-canyon (t≈100), an S-bend, the cliff road along the right wall's foot with stranded cargo rovers; from t≈113 the **Helix research station** on the wide north (right) plateau, lit, pads intact. `high-air`: two rock arches passing over the canyon (scenery, ≤ 40 % where they cross the field). | Speed; the dodge round the pillar; the Helix glimpse (radio). No gates. |
| 5. The Narrows | 125–160 | 24,930–31,255 | 180 | medium | `ground`: the walls close to **180 px** (t=140–148) and stand at their tallest, the floor in their shadow; mortar ledges at their foot. | Gates 4–6 in the narrowing canyon; Rook tucks in behind. |
| 6. Eos Outflow | 160–190 | 31,255–37,245 | 200 | light, clear peak 178–190 | `ground`: the canyon opens onto the outflow's chaos terrain and its sand floor (the walls sinking back to the floor's level by t≈182), a turret nest on the floor. | The second Threadcrawler, a Burrower field on the sand, the last waves; the level ends as the walls leave the field. |
<!-- /data -->

The look comes from the chosen Mars canyon scene r03-c ([art direction](../../../art-direction/README.md))
and the sub-location *Valles Marineris* of [Mars](../../../world/mars/README.md); the walls follow
the chosen canyon walls b of round 36 (MA.1, `canyon-walls-r36-b` in Mars's concept art).

## Terrain

The act's terrain system, designed here for Level 16 and reused by later acts. It answers the gap
analysis's M1 (contact), M2 (geometry, what walls stop) and M3 (branching) with the recommended
options, and the camera model b (MA.1), all approved in round 36.

### Walls, islands and the play plane

- **Data form (M2 = a).** The level data gets a `terrain` block: two wall **polylines**, `left`
  and `right`, each a list of `[scroll, x]` points (the scroll at which that row of the ground
  reaches the play field's top edge, and the x of the wall's collision line in px from the field's
  left edge), linear between points; and **islands**, each a polygon of `[x, scroll]` points. A
  wall without points at a scroll means no wall there. The walls scroll with the ground (scroll
  factor 1). No image-driven mask (M2's b): the geometry is data, so the simulation stays
  deterministic and the validator can check it.
- **What the collision line is** (camera model b, user decision MA.1 of round 36, amending the
  [art direction](../../../art-direction/README.md#parallax-layer-model)'s perspective rule for
  canyons). The **canyon floor is the ground layer** at scale 1.0 and the ship flies inside the
  canyon, over the floor. The walls rise from the floor toward the camera to the plateau at
  about **1.25**, leaning outward from the field's centre like Level 08's towers (user decision D1
  of M5 part B); the **foot of a wall is the collision line**, and everything beyond it is rock on
  the play plane. The faces and the plateau are scenery that nothing gameplay touches. The foot is
  drawn with a dark crag line and a lit scree edge, so the collision line is sharp at 1:1.
- **The wall's foot.** Everything gameplay touches at a wall stands at its **foot**: a 24 px band
  of the floor along the collision line, drawn at 1.0 without lean and scrolling with the ground.
  Anchors, the Threadcrawler's path, ledge turrets, mortars, the relay hut and the cargo rovers all
  stand there. A ground unit there is hittable: a shot that meets its hit box before it reaches
  the rock hits it. Ground units, ground targets and bullets are drawn over the walls (the tower
  rule), so a leaning face never hides one.
- **Islands (M3 = a).** A rock island is a mesa rising from the floor like the walls, its foot the
  collision line and its top at 1.25 (near the centre the top overhangs the foot by up to about
  12 px; units at its foot are drawn over it), splitting the canyon into channels on one screen
  that rejoin later.
  Level 16 has one fork island and one small rock pillar in the rapids.
- **Geometry rules** (validator): the flyable width at least **180 px** (a channel at least
  **130 px**; the ship's hull is 36 px wide); a wall moves at most **0.5 px sideways per px of
  scroll** (at 230 px/s that is 115 px/s, well inside the ship's 270–315 px/s); no wave path,
  ground target, pickup, secret or spawn point inside rock (a wave's path is checked from the top
  edge to the bottom edge at its scroll, with the unit's half hit box as margin).
- **The same geometry on every difficulty**: the walls are the backdrop's art, so the
  difficulties change damage and waves, not the canyon.

### Wall contact (M1 = a)

- The ship touching rock (any hull box over the collision line) takes **10** (`heavy`; shield
  first, overflow to armour) and is **pushed back 28 px** along the wall's normal over 0.12 s,
  with a rock spark, a scrape sound (step A-audio's "canyon scrape") and a short hit flash. A
  **0.5 s cooldown** follows during which further contact only clamps the ship (no damage, no
  push). The 0.25 s mercy invulnerability applies to the armour part as usual.
- **Easy**: 6 per touch (`medium`), the same push. Hard as medium.
- Holding into a wall costs 10 every 0.5 s (20 a second): it hurts, it does not kill outright, in
  keeping with "armour is lost by attrition" (M1's b, instant death, rejected in the
  recommendation).
- **Rook** takes the same contact on his armour; his AI keeps 24 px off the walls, so he touches
  only when pushed (gusts are not in this level).
- The touch is independent of the [Tendril Anchor](../../../enemies/ground/tendril-anchor/README.md)'s
  own contact (6, its own 0.5 s cooldown).

### What walls stop (M2 = a)

| Thing | At rock |
|---|---|
| Player bolts, spread shots, wing and rear guns, the Lance | Stop at the collision line with a small dust spark (the lance's "terrain walls stop a lance") |
| `beam` (Ion Beam) | Ends at the first rock, as at its first target; a tendril does not stop it ([ion beam](../../../player/weapons/ion-beam/README.md)) |
| Orbital Lance | Not stopped: its column burns the full play field's height, rock or not, and burns a tendril it crosses ([specials](../../../player/specials/README.md); round 39 C1) |
| Homing missiles | Detonate on rock; the blast hits units in its radius (units at the walls' foot included) |
| Bombs and mortar shells | Land where aimed; over rock they burst at the wall's foot and hit the units there in the blast |
| Proximity mines | Laid over the flyable area only; a mine pushed against a wall stays at its edge |
| Airstrike, Smart Bomb | Unchanged (from above, whole screen) |
| Enemy bullets | Stop at rock with a spark (turrets at the walls' foot fire inward) |
| Pickups | Slide off rock onto the flyable side within 0.3 s, then drift as usual |
| Air enemies | Authored clear of rock (the validator); the Dust Kite's swoop and any flocking are clamped to the flyable side (no damage to them) |

### Rook in the canyon

- He keeps 24 px off the walls and drops from his side station to a **trail position** behind and
  below the player whenever the flyable width is under **240 px** or the canyon is split
  (Rook's briefing promise "I'll tuck in behind"); he returns to his side when it widens again.
- At an island he follows the player's channel, chosen by the player's x when the island's tip
  reaches the ship's row.
- His shots stop at rock like the player's; he treats a standing tendril as a wall (the anchor
  spec).

### Rendering

Projected wall strips per side, from the foot at 1.0 up to the plateau's edge at 1.25, around the
play field's (240, 297), recomputed each frame so the walls turn and lean as they scroll (Level 08's
tower projection, the walls as continuous strips instead of single towers); the plateau's top at
1.25 racing past beyond them; the floor and the foot bands at 1.0; the mesa and the pillar as
islands with their own faces and tops. Ground units, ground targets and bullets are drawn over the
walls. Shadows of flyers fall on the floor and the foot bands, and on the wall faces and the plateau
where they cross them (round 39 C3).

Round 40 (R.1, "more like a real canyon", the Grand Canyon from above; step CW, pending the user's
review): each wall descends from the plateau's rim to its foot in steps, three cliff bands (the dark
inner gorge at the foot, the red-orange middle cliffs, the pale cap cliff) each above a talus bench,
every step's edge set back toward the rock (never past the collision line, which stays the foot), the
more the more room there is to the field's edge. Side canyons of three sizes bite back into the plateau,
so the rim is ragged and the strata run round the amphitheatres as contour bands; the plateau beyond is
flat and pale, its gullies draining into the big side canyons. Lit from the upper left: the left wall's
cliffs in deep shadow, its benches partly in their cast shadow and the floor beside it in its shadow,
the right wall's cliffs lit. The mesa and the pillar are buttes: the same steps as rings round a flat
cap, with their cast shadows. The rim keeps clear of the Helix station on the north plateau.

### Wall table

The collision line by the time a row reaches the top edge (the data's `terrain`: the row's scroll
in brackets, the time it reaches the top edge in front), every row where a line bends, the walls'
and the islands'. A wall point's optional third value is the wall's height (0 flat, 1 its full rise
to the plateau, presentation only): the walls rise from the floor at the canyon mouth (2,850 to
4,180) and sink back over the outflow (31,250 to 34,850), so the plateau rises into them smoothly
(a default of the gap check, accepted in round 39, B.3). The rock pillar's rows 19,200
and 19,476 are explicit points of both walls, so its left channel stays at 130 px.

<!-- data: terrain -->
| t at the top edge (scroll px) | Left x | Right x | Flyable width | Notes |
|---|---|---|---|---|
| 0–15 (0–2,850) | — | — | 480 | The plateau, no walls |
| 15 (2,850) | 0 | 480 | 480 | The canyon mouth: the walls begin at the field's edges; wall height 0 |
| 22 (4,180) | 40 | 440 | 400 | The walls at full height |
| 30 (5,750) | 70 | 410 | 340 | |
| 40 (7,750) | 90 | 390 | 300 | Gate 1 (t=34) enters at ≈ 320 |
| 50 (9,750) | 80 | 380 | 300 | |
| 57 (11,150) | 70 | 400 | 330 | Gate 3 |
| 64 (12,470) | 40 | 440 | 400 | Widening for the fork |
| 66 (12,830) | 40 | 440 | 200 + 200 | The fork island (below) from its tip |
| 69 (13,370) | 40 | 440 | 150 + 150 | |
| 77 (14,810) | 40 | 440 | 150 + 150 | |
| 80 (15,350) | 40 | 440 | 200 + 200 | The fork island's last row |
| 84 (16,070) | 60 | 420 | 360 | Rejoined; the first Threadcrawler along the left wall's foot |
| 95 (18,050) | 70 | 400 | 330 | The rapids begin |
| 100.1 (19,200) | 70 | 400 | 142 + 162 | The rock pillar, x 200–250 |
| 100.2 (19,224) | 70 | 400 | 130 + 150 | |
| 101.2 (19,452) | 70 | 400 | 130 + 150 | |
| 101.3 (19,476) | 70 | 400 | 142 + 162 | The rock pillar's last row |
| 105.1 (20,350) | 90 | 370 | 280 | The S-bend |
| 110.1 (21,500) | 50 | 330 | 280 | |
| 114.1 (22,420) | 40 | 320 | 280 | The north plateau 160 px wide: the Helix station (t≈113–124) |
| 120.1 (23,800) | 60 | 340 | 280 | |
| 125.1 (24,950) | 90 | 390 | 300 | The narrows begin |
| 132 (26,210) | 110 | 370 | 260 | Gate 4 |
| 140 (27,650) | 150 | 330 | 180 | The narrowest stretch |
| 148 (29,090) | 150 | 330 | 180 | Gate 5 (t=144) |
| 152 (29,810) | 130 | 350 | 220 | Gate 6 |
| 160 (31,250) | 80 | 400 | 320 | The outflow opens; the walls start sinking |
| 170 (33,250) | 30 | 450 | 420 | wall height 0.4 |
| 178 (34,850) | 0 | 480 | 480 | The walls leave the field; none after t≈182; wall height 0 |

**Fork mesa** (`mesa`; x, scroll at the top edge): (240, 12,830) → (190, 13,370) → (190, 14,810) → (240, 15,350) → (290, 14,810) → (290, 13,370).

**Rock pillar** (`pillar`; x, scroll at the top edge): (212, 19,200) → (238, 19,200) → (250, 19,224) → (250, 19,452) → (238, 19,476) → (212, 19,476) → (200, 19,452) → (200, 19,224).

**Contact**: 10 per touch (easy 6, hard 10), pushed 28 px along the wall's normal over 0.12 s, then a 0.5 s cooldown in which a touch only clamps.
<!-- /data -->

### Branching (M3 = a)

One visual branch on one screen, no route choice in the data: the mesa splits the canyon for
≈ 14 s and the two channels rejoin. The **left channel** holds the Candor relay hut (the secret);
the **right channel** a Spine Turret ledge at the mesa's foot (bounty). The popcorn laid out by its
formation flies the widest channel (the left; round 39 C2), the t=66.5 Dust Kite stream two down each
channel; the autopilot flies each channel in the tests. Real route choice with separate
sections (M3's b) is rejected in the recommendation: it doubles the waves, tests and art.

## Waves

The returning `medium` units get the act HP factor (reference DPS 80 at L16, M18 = a: Mantis
26 → 40, Burrower 44 → 46); `tiny` and `small` keep their HP.

- **Density and pacing**: **215 enemies at medium** (the waves' 200, the 18 anchors among them,
  and 15 turrets and mortars; a Threadcrawler counts as one) over the 185 s after the launch,
  **≈ 70 a minute** (minimum 55, the act's band 60–72, [Act 3](../README.md#density-and-threat-profile));
  a wave every 3–4 s with small popcorn waves (Dust Kite streams of 3, V-wings of 4–5, snakes of 4;
  Skitter streams of 3, snakes of 4), and **only pairs in the narrows** (t=140–148, 180 px); never
  an empty screen over 3 s (the Threadcrawler's and the gates' time on the screen included).
- **Gates**: six gates of nine pairs at medium; never two gates closer than 1.5 s of scroll (the
  anchor spec); gate 1 alone (no other wave until t=37.5), each later gate with only popcorn above
  it; the crawlers' paths never under a gate.
- **All from the front**: walls make side and rear entries unreadable at this speed. No beam is
  needed: every gate falls to an anchor kill, and the balance plan buys the Ion Beam at Level 18, so
  the recommended traits are `forward` and `piercing` (round 39 R.4: the hint changed, not the plan).

The gates are not waves: each is a linked nest of [Tendril Anchor](../../../enemies/ground/tendril-anchor/README.md)
pairs among the ground targets (below; `link: pairs`, the pairs placed at the walls' foot, a default of
the gap check, accepted in round 39). A fork wave laid out by its formation flies the **widest
channel** at its entry row, mapped onto that channel's span (both are 150 px, so the left one); its
`edge` does not choose the channel (round 39 C2, accepted). The Dust Kites' authored paths fly either
channel (the t=66.5 stream sends two down each). The Threadcrawlers crawl a ground path of their own (the wave's
`crawl`, in the walls' rows): **up-canyon**, their head along increasing rows at 70 px/s, so on the
screen they come down at the scroll less 70 (≈ 5 s on the screen; down-canyon would cross in 2 s), the
t=160 one crossing the floor from the right wall's foot to the left's (Q.22c; both defaults of the gap
check, accepted in round 39). Hard adds a third along the right wall's foot in the
rapids (t=100).

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 7 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | Over the plateau before the canyon mouth |
| 10.5 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 13.5 | 1 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 5 | front | |
| 17 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 20.5 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | Over the canyon mouth as the walls close in |
| 24.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 28 | 2 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | The first wave between the walls |
| 31 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Gone before the first barrier |
| 37.5 | 2 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | |
| 41 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 43 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | Over the first turret ledge |
| 48.5 | 2 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | |
| 53 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 59.5 | 2 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 5 | front | |
| 63 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | The leader drops an overdrive |
| 66.5 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (alternating edges) | As the mesa splits the canyon: two down each channel (alternating channels) |
| 70 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | In the fork: the widest channel at its row (the left) |
| 72 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (left) | Past the relay hut (left channel) |
| 75.5 | 3 | stream | [Needler](../../../enemies/air/needler/README.md) | 4 | front (alternating edges) | Alternating the edges of the widest channel (the left) |
| 78 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | As the channels close (the widest channel, the left) |
| 81.5 | 3 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | After the channels rejoin |
| 84 | 3 | snake | [Threadcrawler](../../../enemies/ground/threadcrawler/README.md) | 1 | front | **New.** Crawls along the left wall's foot (up-canyon: on screen at the scroll less its 70 px/s); popcorn kept light until t=88 |
| 88 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (right) | |
| 91.5 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 95 | 4 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 5 | front | Into the rapids |
| 98 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Either side of the rock pillar |
| 100 | 4 | snake | [Threadcrawler](../../../enemies/ground/threadcrawler/README.md) | 1 | front | Hard only: a third crawler along the right wall's foot, gone before the S-bend |
| 101 | 4 | line abreast | [Mantis](../../../enemies/air/mantis/README.md) | 2 | front | Act HP factor (40 HP) |
| 104 | 4 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | Through the S-bend |
| 107 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Over the cliff road's rovers |
| 110 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 113 | 4 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | |
| 116 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | Past the Helix station on the north rim |
| 119 | 4 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 5 | front | Vrell flyers pass the station by |
| 122.5 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 126 | 5 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | Into the narrows |
| 129.5 | 5 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | The last one drops an armour patch |
| 135.5 | 5 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | Down the narrows' middle (their sway kept off the walls) |
| 138 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 142 | 5 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (alternating edges) | Only pairs in the narrows (180 px) |
| 147 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 2 | front (left) | Only pairs in the narrows (180 px) |
| 150 | 5 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | |
| 155.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 158.5 | 5 | stream | [Needler](../../../enemies/air/needler/README.md) | 4 | front (alternating edges) | |
| 160 | 6 | snake | [Threadcrawler](../../../enemies/ground/threadcrawler/README.md) | 1 | front | Along the right wall's foot as the outflow opens, then across the floor from wall to wall (its body across the path); no gate after it |
| 162 | 6 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 5 | front | |
| 165.5 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 169 | 6 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 4 | front | The outflow's sand floor (buried, ahead of the ship); act HP factor (46 HP) |
| 172.5 | 6 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | |
| 176 | 6 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 179.5 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 183 | 6 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 5 | front | The last wave, as the walls fall away |

Totals: Dust Kite 85 · Skitter 55 · Needler 26 · Stinger 8 · Threadcrawler 2 · Mantis 2 · Burrower 4.
<!-- /data -->

The totals count the waves at medium: with the gates' 18 anchors (9 pairs) and the ground targets'
11 Spine Turrets and 4 Polyp Mortars that is 215 enemies.

## Ground targets

The gates first enter here: six linked nests (`link: pairs`), nine pairs at medium (easy 7, hard 10),
each pair's anchors 20 px off the collision line in the walls' 24 px foot band; every target at a
wall stands in that band (the validator checks every placement against the rock).

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 2 | Gate 1: one [Tendril Anchor](../../../enemies/ground/tendril-anchor/README.md) pair across the canyon (t=34), alone (no other wave until t=37.5) | **New.** The barrier comes down the screen; kill either anchor or burn the tendril |
| 2 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) ledge ×3 at the left wall's foot (t≈43) | Bounty; their thorns stop at the opposite rock |
| 2 | Gate 2: one pair (t=46); hard 2 pairs | With popcorn above it only |
| 2 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) ×2 at the right wall's foot (t≈53) | Bounty |
| 2 | Gate 3: two pairs at different heights, 1.5 s of scroll apart (t=57); easy 1 pair | With popcorn above it only |
| 3 | Spine Turret ledge ×3 at the mesa's right foot (right channel, t≈70) | Bounty; only reachable from the right channel |
| 3 | Candor relay hut at the left wall's foot (left channel, trigger, t≈71) | 3 hits (hard 5) burst its cargo door: the hidden crate rolls out onto the flyable side (the secret) |
| 4 | Spine Turret pair on a burnt cliff habitat's pad at the left wall's foot (t≈104) | Bounty |
| 4 | Stranded cargo rovers ×4 on the cliff road at the right wall's foot (`ground`, 3 HP, t≈107) | Each drops a small salvage pickup (10); the pickups slide off the rock |
| 5 | Gate 4: two pairs at different heights (t=132) | In the narrowing canyon |
| 5 | Polyp Mortar ×2 on ledges at the narrows' walls' foot (t≈138) | Bounty; their shells arc over the canyon |
| 5 | Gate 5: one pair at the narrowest point (t=144, 180 px) | Only pairs of popcorn round it |
| 5 | Gate 6: two pairs, the last barrier (t=152); easy 1 pair | The secondary is decided when its last tendril is gone (t≈155) |
| 6 | Spine Turret nest ×3 on the outflow's floor (t≈170) | Bounty |
<!-- /data -->

## Hazards

- **Canyon walls**: see *Terrain* (10 per touch, push-back, 0.5 s cooldown; easy 6).
- **Tendril barriers**: a standing tendril passing over the ship costs 6 per touch (the anchor
  spec); breaking it is the secondary.
- The rock arches on `high-air` and the dust plumes on `low-air` are scenery and never hide
  bullets.

## Secrets and pickups

- **Candor relay hut** (t≈71, left channel, at the left wall's foot): a colonial relay hut with its
  cargo door sealed; **3 hits** (hard 5) burst the door and a crate rolls out onto the flyable side,
  worth **100** in Act 1 terms (256 in Act 3). Only the left channel reaches it (shots stop at the
  mesa). The Salvage scanner's glint shows on the door. Its line, Ares Control: "*That's our relay
  cache. Take it, Aegis.*" (draft). Varga's L3 intel line points at it.
- **Overdrive**: the t=63 Stinger leader. **Armour patch**: the last Stinger of t=129.5.
- **Salvage**: the four cargo rovers, 10 each (Act 1 terms).
- Shield cells follow the normal drop table.

## Radio chatter

Spoken radio, the **1 s rule** checked with the same model as Level 15 (a two-page line ≈ 11 s, a
one-page line ≈ 7 s): no timed line starts late. Event lines: `first-wall-contact` (new: the
attempt's first touch of rock; it plays in the next free gap or goes stale after 6 s), the
secret's, `secondary-objective`, the level end. Rook's lines carry `requires: escort`; his one dry
joke is at t=94.

**The Helix beat** (story, M13 = a): Level 16 shows the station untouched and gives Varga **one
observation**; Okafor deflects ("Eyes on the canyon, Doctor."), she does not yet silence her. The
private order to sit on it (the hangar's intel line, "Not on this channel, Doctor.") is Level 17's
beat, where the compound is the set piece ([Act 3 story beats](../README.md)).

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, canyon mouth ahead. Stay fast, and stay off the walls." |
| t=11 | Rook | "On your {side}, Lancer. Where it gets narrow, I'll tuck in behind." |
| t=22 | Generic colonial (Ares Control) | "Aegis, this canyon is our only road east. The Vrell have strung it shut." |
| First wall touch (`first-wall-contact`) | Okafor | "Mind the walls, Lancer. They don't give." |
| t=33 | Varga | "Barrier ahead. Two anchors on the walls: kill either one and it falls." |
| t=47 | Varga | "Kill an anchor and the tendril drops. Ordinary shots pass straight through it." |
| t=60 | Okafor | "The canyon splits ahead. Pick a side and commit." |
| t=81 | Varga | "Movement on the left wall. Something long." |
| t=94 | Rook | "Coprates rapids. And Okafor calls this a road." |
| t=114 | Varga | "That's the Helix research station on the rim. Vrell all round it, and not a scratch." |
| t=126 | Okafor | "Eyes on the canyon, Doctor." |
| t=146 | Generic colonial (Ares Control) | "Eos outflow ahead. You're nearly through, Aegis." |
| Secondary met (`secondary-objective`) | Generic colonial (Ares Control) | "Every barrier down. The road east is open." |
| t=166 | The Choir (distorted) | "[the Choir sings]" |
| t=173 | Varga | "The singing is louder out east. Toward Ares Landing." |
| t=180 | Okafor | "Ares Landing has you on its scopes. Bring it home." |
| Level end | Okafor | "Canyon run complete. The domes are yours to hold now. Get some sleep." |
<!-- /data -->

The relay hut's line (the secret's, Ares Control): "That's our relay cache. Take it, Aegis."

The secret's line has the gap from Okafor's t=60 line (closing ≈ t=67) to Varga at t=81 (the hut
is in reach at t≈71–74); the secondary's line the gap after t=146's close (≈ t=153) before the
Choir at t=166 (gate 6 is decided at t≈155, as late as t≈159: a closing line takes a free radio at once
and holds it about 7 s).

## Boss / mid-boss

None. The narrows' three gates are the level's climax, the outflow its release.

## Music & ambience

The Act 3 A theme **"Red Dust Run"** (track 8), as Level 15: the **base stem from section 2** (into
the canyon) and the **full mix from section 4** (the rapids). Ambience: the Martian dust wind with
a canyon howl (new, [ambience per setting](../../../audio/sfx/README.md#ambience-per-setting)) from
t=0. New sounds for this level: the canyon wall scrape (step A-audio proposes it), the tendril hum
and break (the unit's), the relay hut's door burst.

## Credit budget

Budget(16) = 700 × 1.07¹⁵ ≈ **1,931**. Amounts in **Act 1 terms** (Dust Kite 4, Skitter 5,
Needler 12, Stinger 15, Mantis 30, Tendril Anchor 12, a burnt tendril 8, Threadcrawler 48 whole or
60 by parts, Burrower 24, Spine Turret 12, Polyp Mortar 15; crate 100, secondary 80, salvage 10),
paid × **2.56** (bounties also × the scale, rounded per kill). A **`bounty_scale` of 0.54**
(estimate) puts the typical haul at 1,939 (+0.4 %): paid per kill Dust Kite 6, Skitter 7, Needler
17, Stinger 21, Mantis 41, anchor 17, tendril 11, Threadcrawler 66 (83 by parts), Burrower 33, Spine
Turret 17, Polyp Mortar 21. The scale is higher than Level 15's 0.42 because this level is air-heavy (air kills count
at 60 % in the typical haul) where Level 15's 27 Burrowers count at the ground's 80 %; the Dust
Kite's 4 makes the scale coarse (it pays 5 from 0.44 to 0.53 and 6 from 0.54, and the typical haul
jumps from −4.7 % to +0.4 % between them), so `TypicalHaulTest` fits the final value.

Rendered from the data (one kills row; per payout × 2.56 × 0.54, rounded): a perfect run takes the
Threadcrawlers apart (the tail first, with its bonus) and burns every tendril besides killing the
anchors.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Dust Kite 85 × 4 + Skitter 55 × 5 + Needler 26 × 12 + Stinger 8 × 15 + Threadcrawler 2 × 60 by parts (48 whole) + Mantis 2 × 30 + Burrower 4 × 24 | 1,891 | 1,163 |
| Ground targets: Tendril Anchor 18 × 12 + Spine Turret 11 × 12 + Polyp Mortar 4 × 15 + cargo rovers 4 × small salvage 10 + 9 tendrils burnt × 8 | 780 | 528 |
| Secret: relay hut (hidden crate, 5% of budget) | 256 | 128 |
| Secondary: open road (every tendril broken) | 205 | 102 |
| **Total** (bounty scale 0.54) | **3,132** | **1,922** |
| Budget(n) = the typical haul's target; typical -0 %, perfect 1.62 × budget | | 1,931 |
<!-- /data -->

The typical haul counts the Threadcrawlers whole (head first) and the anchors at the ground rate
without the tendril bonus (a typical player kills an anchor; burning needs the beam). Wall touches
cost armour, not credits, but the repairs come out of the next visit (5 cr a point at medium): the
autopilot's wall touches are measured in part C, and a canyon that costs the typical player more
than ≈ 30 armour points is narrowed less, not paid more.

**Balance plan**: no change at the L16 visit is needed for the level (forward DPS 77 against the
reference 80; anchors die in 0.15 s); the Ion Beam is optional here and planned for the L18 visit
(at L2; the balance plan on budget(n) income, the user's decision of 2026-10-14; L16's visit buys the
Mk III generator it needs).

## Difficulty notes

- **Easy**: wall contact 6; gates 3 and 6 one pair each (7 tendrils for the secondary); the
  tendril's 30 HP (the spec); the formation lever on the waves.
- **Hard**: gate 2 two pairs (10 tendrils); the anchors' aimed orbs (the spec); 16-segment
  Threadcrawlers with a 1.5 s rest (the spec) and a third one along the right wall's foot in the rapids
  (t≈100, gone before the S-bend); the relay hut's door takes 5 hits.
- The canyon's geometry is the same on every difficulty.

## Concept art

Production art of M7 part C (step ART-C, round 39; notes in [concept/prompts.md](concept/prompts.md)).
The backdrop is approved as final except the walls' and islands' rendering and textures, which are
being redone to look like a real canyon (round 39 R.1, step CW, for round 40); until then `art: chosen` (round 40: the redo approved as final, `art: final`):

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r39-a.png](concept/backdrop-final-r39-a.png) | Production backdrop (`tools/art/backdrop_l16.py`): the tile sets (the Noctis plateau and its chasms, the canyon floor, the rapids' riverbed, the Eos chaos, the plateau at 1.25), the wall and foot textures, the islands' faces and tops, the seam pieces, the cliff road, the Helix station, the arches, the dust; composites with the walls projected as the game will (sheet) | chosen (round 39, F.1: approved as final except the walls' and islands' look, R.1 redo for round 40); the redo approved as final in round 40 (W.a) |
| [concept/backdrop-final-r39-a.gif](concept/backdrop-final-r39-a.gif) | The canyon mouth, the fork's mesa and the rapids' pillar, 10 fps | chosen (round 39, F.1: approved as final except the walls' and islands' look, R.1 redo for round 40); the redo approved as final in round 40 (W.a) |
| [concept/l16-props-final-r39-a.png](concept/l16-props-final-r39-a.png) | Props (`tools/art/canyon_units.py`): the Candor relay hut hit by hit and its beacon, the cargo rovers (intact, damaged, wreck), the wall spark and its dust (sheet) | chosen (round 39, F.2: approved as final) |
| [concept/l16-props-final-r39-a.gif](concept/l16-props-final-r39-a.gif) | The same props in motion | chosen (round 39, F.2: approved as final) |
| [concept/level-16-capture-r39-a.png](concept/level-16-capture-r39-a.png) | Game capture (step C5, medium, the plan's L16 ship with the Orbital Lance as its special, Rook): the mouth's walls rising from flat, gate 1 dormant → flare → anchor kill, the Lance burning gate 2, a wall touch, the fork and the relay hut, a crawler's pore wave, the pillar, the rovers, the Helix station, the narrows' gates, the outflow's crawler and Burrowers, the debrief with BARRIERS 9 / 9 (sheet) | chosen (round 39, K.1: accepted) |
| [concept/level-16-capture-r39-a.mp4](concept/level-16-capture-r39-a.mp4) | The whole run with the game's sound (t −1 to 230; radio as text, no voices yet) | chosen (round 39, K.1: accepted) |
| [concept/canyon-redo-r40-a.png](concept/canyon-redo-r40-a.png) | Game capture of the canyon redo (step CW, medium, `--invulnerable`): the mouth, full height, a burnt habitat on the red cliff, the fork's mesa as a butte, the pillar, the Helix station, the narrows (sheet of eight at 1:1) | chosen (round 40, W.a: approved as final) |
| [concept/canyon-redo-r40-a.mp4](concept/canyon-redo-r40-a.mp4) | The same run cut to 59 s with the game's sound: the mouth, the fork, the pillar, the Helix station, the narrows | chosen (round 40, W.a: approved as final) |
| [concept/canyon-redo-compare-r40-a.png](concept/canyon-redo-compare-r40-a.png) | Before (round 39's capture) and after at the same level times: full height, the fork's mesa, the pillar, the narrows | chosen (round 40, W.a: approved as final) |

## Implementation

- [x] Terrain (M7 part C): the `terrain` block (wall polylines, islands) in the level data and its
      schema; the validator's geometry and placement checks (step C2)
- [x] Ship wall contact: 10 per touch (easy 6), the 28 px push-back, the 0.5 s cooldown, spark,
      scrape and flash
- [x] Walls stop shots, the beam, homing missiles, bombs and shells, enemy bullets; mines and
      pickups kept on the flyable side; air enemies clamped (*What walls stop*)
- [x] Rook in the canyon: 24 px off the walls, the trail position under 240 px and in channels,
      the island's channel by the player's x, his shots stopped by rock
- [x] Rendering: projected wall strips from the foot (1.0) up to the plateau (1.25) leaning
      outward like Level 08's towers, the plateau's top, the floor and foot bands at 1.0, the
      islands' faces and tops, ground units and bullets drawn over the walls; the foot's crag line
      readable at 1:1
- [x] Round 40 redo (R.1): stepped walls (cliffs and talus benches set back toward the rock, side
      canyons in a ragged rim, strata as contour bands), lit from the upper left with cast shadows,
      the pale plateau with gullies, the islands as buttes; the foot stays the collision line
      (`CanyonProfile`, `WallProjectionTest`), pending the user's review
- [x] The fork island and the rock pillar; the autopilot flies both channels
- [x] Tendril gates (the anchor spec), the Threadcrawler along the walls' foot, the outflow's Burrower
      field on sand only
- [x] Secondary `links` (new kind): every tendril broken before it leaves the screen, the
      `BARRIERS` tracker and debrief row, 80 in Act 1 terms
- [x] Wave script: ≥ 55 enemies a minute (`DensityTest`), no empty screen over 3 s
      (`PacingTest`), the act HP factor for the Mantis and Burrower
- [x] Ground targets, the relay hut secret (3 hits, hard 5), the rovers' salvage
- [x] Radio voiced and timed (`RadioTimelineTest`, with and without Rook); the
      `first-wall-contact` event; Rook's lines `requires: escort`
      (the event and the timings built in steps C4/G2; the voices rendered in step VOX-C, accepted in
      round 39; Varga's t=47 line re-rendered for R.4)
- [x] Threat profile, Varga's four lines and Rook's teaser fitting the intel panel; the anchor's
      and Threadcrawler's intel portraits
- [x] Briefing: two one-screen pages with their images
- [x] Typical haul within ±5 % of 1,931 (`TypicalHaulTest`); the autopilot's wall damage measured
- [x] Music and ambience as in *Music & ambience*; easy/hard as in *Difficulty notes*
- [x] Production art and captures for part C's round (round 39: ART-C's backdrop and props, step C5's
      capture; built in steps C1–C4, G1, G2; accepted in round 39)
- [ ] The walls' and islands' look redone to read as a real canyon from above (round 39 R.1: the user,
      "take inspiration from pictures from the Grand Canyon from above"; step CW, for round 40)

## Open questions

- None open: round 36 answered every question (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the [Act 3](../README.md) roster to a draft
  level document. Taken as the recommended options, pending the user's review in round 36: M1 = a
  (damage and push-back, 0.5 s cooldown), M2 = a (wall polylines and island polygons in the level
  data; walls stop shots, beams, missiles and enemy bullets; pickups slide), M3 = a (a visual
  branch on one screen), M10 = b, M13 = a (in-level only Varga's observation), M17 = a, M18 = a
  (reference DPS 80), M19. Our proposals: the camera model a (the rim as the collision line, the
  rim band at 1.0), 10 per touch (easy 6) with a 28 px push, the 180 px / 130 px minimum widths and
  the 0.5 px-per-px slope rule, the six sections and speeds (230 in the gate-free rapids), the wall
  table, the fork mesa and the pillar, six gates of nine pairs, 279 enemies (≈ 90 a minute), the
  relay hut secret, the `links` secondary (80), Rook's trail position, the radio and intel texts,
  `bounty_scale` ≈ 0.45.
- 2026-10-12 (overnight, harmonised with the act README): density brought into the act's band of
  60–72 a minute (pending the user's review in round 36) by smaller popcorn waves at the same
  cadence: Dust Kite streams 5 → 3, V-wings 7 → 5 and 5 → 4, snakes 6 → 4, the fork's channel
  streams 6 → 4 and 4 → 3; Skitter streams 4 → 3, snakes 6 → 4; only pairs in the narrows (the
  t=142 Kites and the t=147 Skitters); 215 enemies (≈ 70 a minute), the gates, the crawlers and
  the Burrower field unchanged; `bounty_scale` 0.45 → 0.54 (typical 1,939, +0.4 %; closes the
  draft's −3 % gap).
- 2026-10-13 (user, round 36): approved (D.3), M1–M3, M10, M13, M17 and M18 kept. **MA.1 = b**: the
  walls rise toward the camera from the floor (1.0) to the plateau (1.25) like Level 08's towers,
  the collision line at their foot (it amends the art-direction rule); the level adapted: the rim
  bands go, every anchor, turret, mortar, the relay hut, the rovers and the Threadcrawlers' paths
  stand at the walls' foot on the floor, gameplay is inside the canyon, ground units and bullets
  draw over the walls, the rendering and the briefing's "along the walls". Model a (the rim as the
  collision line, the faces dropping to the floor at `far` 0.6) rejected. Q.16 all yes: the `links`
  secondary, easy's 6 per touch, the Helix glimpse here with the silencing in Level 17, Rook's
  240 px trail threshold.
- 2026-10-14 (M7 part C, step C2): the numbers moved into [data.yaml](data.yaml) and the tables
  rendered from it (the briefing, the teaser, the threat profile, the sections, the wall table, the
  waves, the ground targets, the radio, the credit budget). Built as the gap check's defaults, pending
  the user's review in round 39: the gates as six linked enemy nests among the ground targets (`link:
  pairs`, moved from the wave table; the anchors 12 px off the collision line), the walls' height
  ramp at the mouth and the outflow, the crawlers up-canyon with the t=160 one crossing the floor,
  the Threadcrawler's whole 48 as its head's first bonus. The backdrop block in the data is a
  placeholder (Level 15's images) until step ART-C's is merged.
- 2026-10-14 (M7 part C, step C4): mapped for the simulation (gates from the linked nests, the
  anchors' tendril, the crawl paths with the pore wave and cut, the contact per difficulty, the
  `links` secondary, the `first-wall-contact` cue) and flown by the autopilot in both channels. The
  gates' anchors moved from 12 to 20 px off the collision line (a beam reaches each one from a ship
  4 px clear of the rock for ≥ 1.7 s); the crawl paths start 30 rows higher (hard's 16-segment body
  enters from above the top edge); seven popcorn waves moved by ≤ 1 s for the pacing rule (13.5, 17,
  20.5, 48.5, 81.5, 138); the intel setting shortened to "Mars, Valles Marineris" (the panel's
  width). Typical haul 1,944 (+0.7 %, `bounty_scale` 0.54 kept), 69.7 enemies a minute; the
  autopilot touches a wall about 6 times a run (60 damage, 0–3 of it armour, the README's ≤ ≈ 30
  armour kept). Pending the user's review in round 39.
- 2026-10-14 (M7 part C, step C5): part C built (steps C1–C4, G1, G2, ART-C, AUD-C); the capture
  `level-16-capture-r39-a` flown on medium with the plan's ship (the Orbital Lance as its special for the
  beam burn) and Rook, no invulnerability: 210 / 215, no armour lost, the secret, BARRIERS 9 / 9, A+. A crash
  at every Level 16 start fixed (the cargo rovers have no break-apart frames: the game now only needs them
  when they exist). `art: proposed`; the art, the capture and every built default pending the user's review in
  [round 39](../../../concept-rounds/round-39/README.md); the voices (step VOX-C) are not rendered yet.
- 2026-10-18 (M7 part C, step VOX-C): the 17 voiced lines (Rook's t=11 line once per side) and both briefing
  pages rendered (`tools/art/voice.py`; Okafor, Rook, Varga, Ares Control; the Choir's song reused); "Airees",
  "Vallis Marinairis" and "Coprateez" respelled as approved, Eos and Helix spoken plain; four seeds pinned
  (design/audio/voice). The Choir moved t=162 → 166 and Varga's reply 169.5 → 173: the secondary's closing
  line takes a free radio at once and held the Choir back up to 6 s when gate 6 fell after t≈155
  (`RadioTimelineTest` now plays Level 16, with and without Rook). Pending the user's review in round 39.
- 2026-10-18 (user, round 39): closed. C1 accepted: the Orbital Lance is **not** stopped by rock (the
  specials spec wins; *What walls stop* now lists it apart from the Ion Beam); C2 accepted: a fork wave flies
  the widest channel at its entry row, `edge` does not choose (the wave notes and *Branching* updated); C3
  accepted: flyer shadows fall on the wall faces and the plateau too (*Rendering*). The backdrop (F.1) and
  props (F.2) approved as final, **except** the walls' and islands' look: R.1, "Make it look more like a real
  canyon. Maybe you can take inspiration from pictures from the Grand Canyon from above.", is redone in step
  CW (in progress) for round 40, so `art: chosen` until then. The voices (V.1) and the retimed Choir (V.2)
  accepted; the capture accepted (K.1); every build choice yes (B.1–B.5); R.2 (no rover break-apart), R.3 (the
  Lance's scorch) and R.5 (the ≈ 3 s Helix glimpse) kept. R.4 "change the plan or the hint": Claude chose the
  **hint** (the approved Act 3 economy stays): Varga's t=47 line now "Kill an anchor and the tendril drops.
  Ordinary shots pass straight through it." (re-rendered, `tools/art/voice.py`), her L3 intel line "Kill an
  anchor to drop a tendril. A cache down the left fork.", the recommended traits `forward`, `piercing`; the
  briefing's "a beam burns through the tendril" stays (true from the Lance and the Ion Beam on). Pending the
  user's review: the R.4 wording.
- 2026-10-18 (step CW, round 39 R.1): the canyon redone from Grand Canyon views from above: stepped
  walls (three cliff bands over talus benches, set back toward the rock by the room to the field's edge
  and by side canyons of three sizes, the big ones repeating with the plateau tile so its gullies drain
  into them), per-step lighting from the upper left with cast shadows on the benches and the floor, the
  pale plateau, the islands as buttes, the rim kept clear of the Helix station; mockups in three render
  iterations, then `tools/art/backdrop_l16.py` (the model, the textures) and the game (`CanyonProfile`,
  `WallProjection`). Pending the user's review (round 40): where a wall stands near the field's edge
  (most of sections 2-4) only its inner gorge and first bench are on screen, so it still reads as a
  band; the buttes show mostly their pale caps.
- 2026-10-19 (user, round 40, W.a): the canyon walls' redo approved as final (`art: final`); W.b the redo's defaults (a)-(f) all yes, W.c Varga's t=47 line accepted, W.d the fork's right channel kept.
