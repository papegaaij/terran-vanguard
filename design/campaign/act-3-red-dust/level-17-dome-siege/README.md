---
title: Level 17 – Dome Siege
design: approved
implementation: done
art: final
depends-on: [../../../enemies/air/choir-herald, ../../../enemies/ground/shellback, ../../../enemies/air/spore-bomber, ../../../enemies/ground/burrower, ../../../enemies/air/dust-kite, ../../../allies, ../../../player/wingmen, ../../../world/mars]
updated: 2026-10-21
---

# Level 17 – Dome Siege

## Summary

Ares Landing, the oldest colony on Mars, is under siege. Its people are sheltered below ground,
but the five greenhouse domes that feed it stand in the open, and Vrell
[Spore Bombers](../../../enemies/air/spore-bomber/README.md) and the new
[Shellback](../../../enemies/ground/shellback/README.md) beasts go for the glass. The level
introduces the Shellback and the [Choir Herald](../../../enemies/air/choir-herald/README.md), a
support flyer that sings shield rings onto its escorts, and the domes as a new ally (M16 = a): the
secondary objective is that no dome is lost (round 41: hits allowed; it was "no dome hit"). On the way between the outer domes and the inner ring
the Vrell sweep past the untouched Helix compound. 172 s: two slow dome sections (80 and 70 px/s)
framed by faster approach, compound and spaceport sections.

**Status: approved in concept round 36 (user, 2026-10-13).** Written in the overnight design
phase (2026-10-12) from the act roster and the gap analysis's recommended answers, all kept or
accepted in the round. M7 part D (step D2) moved the numbers into the level's
[data.yaml](data.yaml); the marked tables below are rendered from it (`tools/sync_tables.py`).

## Briefing

Two pages, as Levels 08–14, each one screen (`BriefingLayoutTest`) with room for its image: Okafor's
mission (the domes), then Varga's two new forms. Texts (approved in round 36):

<!-- data: briefing -->
> **Commander Okafor:** "Ares Landing is the oldest colony on Mars, and the Vrell are at its
> domes. The people are below ground. The crops and the air plants are not. Keep the bombers off
> the glass until the militia can man its guns."
>
> **Dr. Varga:** "Two new forms. A shelled walker that lobs spores and rolls when it's hurt, and
> a flyer that sings shields onto the others. Kill the singer, then the rest."
<!-- /data -->

*Images* (production art straight away, as Act 2; [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`):

- Okafor's page `level-17-ares-domes`: a top-down colony plan in the CDF map style: the rail line
  from the north, the outer domes Bradbury and Lowell, the burnt outer greenhouses, the inner ring
  (Schiaparelli, Burroughs, Eden, the largest), the spaceport to the south; red Vrell vectors
  converging on the domes. East of the outer greenhouses a black-and-gold compound, **unlabelled**
  (the player may notice it; the map does not point at it).
- Varga's page `level-17-choir-herald`: her scan of a Choir Herald (crest lit) with the dashed aura
  circle and thin threads to three ringed escorts; an inset of a Shellback mid-curl with the lime
  vent marked.

*Hangar teaser* (shop screen before L17; Rook, not voiced, as Level 10's):

<!-- data: teaser -->
> **Rook:** "Ares Landing wants its glass in one piece. Spread guns for the bombers, I'd say."
<!-- /data -->

## Threat profile

Rendered from the data; directions are counted by each wave's `from` (the intel's rule).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (bombers, the Herald and its escorts, Stinger streams, popcorn), `ground` (Shellbacks, Burrower fields, spine-turret nests, Polyp Mortars, the domes) |
| Attack directions | front 95% · sides 5% |
| Density | 4 |
| Recommended traits | `spread`, `anti-ground` |
| Hazards | Bombers and Shellbacks going for the domes (the secondary); Shellback spore mortars |
| Boss / mid-boss | none |
| Sensor-suite detail | none: Ares Landing, `air` + `ground`, front · L1: + directions (sides), density 4, hazard "dome attacks", the objective `REACH THE SPACEPORT` and the secondary `NO DOME LOST` · L2: + Shellback (new), Choir Herald (new), Spore Bomber, Burrower, Mantis, Dust Kite, Skitter, Needler, Stinger portraits · L3: + `spread` and `anti-ground` highlighted, the wave strip marking the two dome sections, 1 secret |
<!-- /data -->

*Varga's intel line* per sensor-suite level (in the data's `threat_profile.varga`; they must fit `IntelPanelLayoutTest`, two lines beside the rows, as Level 11's):

| Sensor suite | Line |
|---|---|
| none | "Ares Landing, Lancer. A colony that wants its glass in one piece." |
| L1 | "Front and flanks. Bombers will go for the domes." |
| L2 | "New: a shelled walker and a singing flyer." |
| L3 | "Spread for the bombers, anti-ground for the shells." |

## Objective

- **Primary** `reach-end`: reach the spaceport. The domes never fail the mission: a broken dome
  loses its crops, not lives (the colonists are below ground).
- **Secondary** "No dome lost" (round 41, the user's playtest 2026-10-19, "both": the secondary and the
  bombers' run-in below; accepted in round 41 (2026-10-19)): **failed** when the first dome breaks, **met** when the
  last dome (Eden) leaves the bottom edge (t≈135.8) with all five standing; hits are allowed. Pays **100**
  credits in Act 1 terms (256 in Act 3, the act factor 2.56); the typical haul counts the economy's secondary
  share (50 %). Kind: `standing` (round 41), next to `untouched` (the ally group took no hit: M16 = a's "No dome
  hit", the roster's "no bomber reaches the domes" widened to every attack that can hit a dome, which the
  autopilot met 4 of 8 times at medium and never on hard), Level 11's `afloat` and Level 13's `integrity`
  ([schemas](../../../tech/architecture/README.md#data-file-schemas), for the build).

### The Ares Landing domes (new ally, M16 = a)

Five greenhouse domes, a new ally kind *Ares Landing dome* that reuses the
[Nansen Relay](../../../allies/README.md#nansen-relay)'s rules (`follows: ground`, damaged only by
attacks aimed at it through the [target-the-objective hook](../../../enemies/README.md#target-the-objective-hook))
and counts **hits** like Level 11's cargo ships instead of an integrity bar. Its spec is its own document,
[Ares Landing dome](../../../allies/ares-landing-dome/README.md) (round 36, Q.25b; its numbers in
[allies/data.yaml](../../../allies/data.yaml)): a 120 px geodesic glass dome (MA.3 = a; Eden 144 px)
on `ground` that scrolls through with the ground, breaks on the **3rd** hit (easy 4th, hard 2nd),
is hurt only by hooked attacks (below), never fails the mission; the HUD's tracker `DOMES` with five
pips and the debrief's row `DOMES UNTOUCHED n / 5`; `{ally}` is the dome's name in the radio.

The five domes (the data's `allies.units`: each enters at the top edge at its t, its bottom edge
there, and scrolls with the ground; the times below are from the scroll without the 2 s eases):

| Dome | x (px) | Section | Enters the top edge | Centre passes y≈220 (the bombers' row) | Leaves the bottom edge |
|---|---|---|---|---|---|
| Bradbury | 130 | 2 (80 px/s) | t=38 | t≈41.5 | t≈46.3 |
| Lowell | 350 | 2 | t=46 | t≈49.5 | t≈54.3 |
| Schiaparelli | 110 | 4 (70 px/s) | t=104 | t≈108.0 | t≈113.4 |
| Burroughs | 370 | 4 | t=110 | t≈114.0 | t≈119.4 |
| Eden (144 px) | 240 | 4 | t=126 | t≈130.2 | t≈135.8 |

(x from the play field's left edge, y below its top edge.)

### What goes for the domes: the hook

Per unit type (the data's `allies.targeted_by`, as Level 13's: every Spore Bomber and Shellback wave
is hooked, the hooked rows in *Waves*; easy unhooks the Shellbacks); the popcorn, the Needlers, the
Stingers, the Mantises, the Herald, the Burrowers and the ground nests are never hooked. The hooked bombers from the
front hold the row 220 px below the top edge (`bomb_row`; 160 until round 40 R.b), a wave's bombers on x
slots 40 px apart round the dome's x (`bomb_slot`); the side pairs keep their entry row, 140 px. Round 41
(accepted in round 40 (2026-10-19)): a hooked bomber flies **2.5 s on the play field before it may drop**
(`bomb_run_in`); over a dome before then it keeps its load and steers on to the next one (the t=124 bombers
dropped on Eden as they entered, before the player could reach them).

| Units | Mode | Exact rule (our reading, for review) |
|---|---|---|
| Spore Bombers (hooked waves) | `always` | The bomber's `strafe` holds a row at y≈140–220 and steers (at its 45 px/s) to the x of the next dome coming down; when the dome's footprint passes beneath it, it drops its load: **one hit**, once per bomber. It then flies on, laying its normal spore mines on the ship's plane. Each hooked wave enters ≈ 5 s before its dome's centre reaches the bombers' row: the kill window |
| Shellbacks (hooked) | `alternate` | Every second mortar lob goes at the nearest dome while it is closer than the ship (the marker on the dome, 1.2 s ahead); a direct hit is one hit. Destroying the vent ends it; a shot whose line crosses the vent hits it first, from any side (round 40 R.a). The walks pass **between the domes** (round 40 R.c): down a dome's ring lane, the body clear of the glass (its hit box), then aside before coming level. Its **roll** (once, at ≤ 40 % body HP, up to 350 px along its path, shorter near the bottom: R.e) crushes a dome whose **footprint** the ball touches, one hit per roll: set off on the ring lane behind a dome, it grazes the dome's ring. The player who wounds a Shellback just behind a dome should wait until it has stepped aside, then finish it |
| Choir Herald | — | Not hooked itself; at t=124 it holds over Eden's lane and its aura rings the hooked bombers beside it (a bomber's 63 HP plus a 40 ring) |

## Layout

Scroll **130 px/s** on the approach and the spaceport, **80 / 70 px/s** in the two dome sections
(each dome is on the screen 8–10 s, so a hooked wave has time to be read and killed), **110 px/s**
past the Helix compound. The setting is [Mars](../../../world/mars/README.md)'s Ares Landing: rust
plains with the rail line and the colony's grey-white structures, green-lit domes, Vrell teal nest
growth spreading between them. Motion budget: the domes' inner glow and the Herald's aura are the
strong elements; dust plumes and smoke stay faint. The data eases each speed change over 2 s (the
sections' `ramp`); the scroll column includes the eases.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Rail Approach | 0–30 | 0–3,900 | 130 | light | `deep`: rust plain, Olympus Mons on the horizon. `ground`: the rail line from the north with a stranded freight train, a spine-turret nest on the embankment (t≈18), wind-blown dust drifts. `low-air`: thin dust wisps. | Popcorn from t=6, a Burrower field under the plain (t=9); the first ground targets. |
| 2. Outer Domes | 30–72 | 3,900–7,310 | 80 | light | `ground`: Bradbury (x 130) and Lowell (x 350) on their rings, greenhouse annexes, rail spurs, Vrell nest growth creeping up to the rings; a Polyp Mortar floe at t≈64. | The Shellback alone (t=30.5), the first hooked bombers, the Herald alone (t=55.5); a Burrower field in the nest growth after Lowell (t=60). |
| 3. Helix Compound | 72–98 | 7,310–10,140 | 110 | light, medium peak 72–80 | `ground`: the burnt outer greenhouses (collapsed frames, the secret's capsule at t≈76), then the **Helix compound** at x 300–460 (t≈78–85) black-and-gold, lit, a landing pad, its perimeter fence clean, no Vrell growth within 100 px while everything round it burns; a rail yard with a spine-turret nest (t≈90). | The Helix beat (the t=77 swarm bends round the compound); a Burrower field after it (t=86); Mantis flankers. |
| 4. Inner Ring | 98–140 | 10,140–13,120 | 70 | light | `ground`: Schiaparelli (x 110), Burroughs (x 370), then Eden (x 240, the largest, its glass lit brightest); the ring road, airlocks, militia gun pits (unmanned); a Polyp Mortar floe at t≈117. | The hooked waves at their densest, a Burrower field between the domes (t=112, never on one); the second Herald over Eden; the secondary judged at t≈135.8. |
| 5. Spaceport | 140–172 | 13,120–17,220 | 130 | clear | `ground`: the spaceport's pads and hangars, a spine-turret nest on the apron (t≈150), the militia's flak guns opening up at t≈160 (presentation). | Release, with two Burrower fields (t=137.5 as Eden leaves, t=161.5 through the apron); level end. |
<!-- /data -->

**Helix compound** (story beat L16–17, [twist](../../../story/twist/README.md)): a backdrop set piece,
not a unit and not destructible (shots pass over it as over scenery). Nothing in the level fires
on it, no ground target is placed within 100 px of it, and the t=77 Dust Kite swarm's authored route
bends round its fence and closes up below it, in plain view. The radio carries the beat (Varga's
observation, Okafor's "Not on this channel, Doctor.", M13 = a). The compound's look and Level 16's
sighting must match (step D-L15-16's document).

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms**
with a `bounty_scale` (see *Credit budget*).

- **Act HP factor** ([balancing basis](../../../enemies/README.md#balancing-basis)) from the proposed
  reference DPS 84 at L17 (M18 = a, round 36): **Spore Bomber** (L03, 32) ×2.63 → 63 HP,
  **Mantis** (L06, 52) ×1.62 → 42 HP, **Burrower** (L15, 44) ×84/76 → 49 HP (step PE17). The Dust Kite,
  Skitter, Mote Swarm, Spine Turret and Polyp Mortar are `tiny`/`small` and keep their HP; the **Needler**
  and the **Stinger** take the factor capped at ×3 (`small_act_hp`, step PE17): 12 and 18 HP. The
  Shellback and the Choir Herald are new here.
- **Density and pacing** (§1.7; step PE17, the user's round 40 P.e "all later levels" and "tougher, not
  more", accepted in round 40 (2026-10-19)): **213 enemies** (waves 196 + ground targets 17) over the 167 s from
  the launch, **≈ 77 a minute** at medium (≥ 55; above the act's band 60–72,
  [Act 3](../README.md#density-and-threat-profile); Levels 15 and 16 landed at 99 and 119); a wave every
  3–3.5 s from t=6, the popcorn in small waves (Dust Kite streams and V-wings of 3, the long V-wings and
  snakes of 4, Mote Swarms of 6, Skitter snakes of 4) so the hooked waves read against less noise; no
  rear waves, the side waves warned 3 s ahead at their edge (`WarningEdge`). The Act 2+ rules hold: at
  medium and hard no pause over 2 s (`PacingTest`; before: 14 and 10 with `small_act_hp`, 17 and 9
  without) and, with the plan's fit at medium, **2.60 enemies on the screen and 15.1 enemy shots a
  minute** (`ActFloorTest`: 2.5 and 10; before: 1.09 and 0.4). The floor comes from long-lived, firing
  groups rather than more popcorn: **six Burrower fields** of 4 (the inner one 3; hard one fewer each)
  at t=9, 60, 86, 112, 137.5 and 161.5, buried ahead of the ship and surfacing to fire their rings
  (most of the shots), never while an outer dome or a new unit's quiet window is on the screen; the
  inner one between Schiaparelli, Burroughs and Eden, kept off their footprints (and the Helix
  compound) by the data's `no_surface`; and **nine Stinger streams** of 3 in place of three Skitter
  waves, five Dust Kite waves and the t=81.5 Stinger V-wing. The pulse-era fit (two Micro-missile Pods,
  Rook's Autocannon) kills a Needler circle or line before its first shot, so circles were tried and
  dropped. Hard: 249 enemies, **3.38 on the screen**, 21.9 shots a minute; easy 165, one pause over 3 s
  (the end). Front 95 % by count. The two new units
  each get a **quiet window**: no other wave enters for 3.5 s after the Shellback (t=30.5) or after
  the Herald (t=55.5), and the popcorn before each leaves the screen first, with no dome on the
  screen.
- **Hooked waves** are marked; see *What goes for the domes*.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (right) | The first targets, over the rail line |
| 9 | 1 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 4 | front | Under the rust plain (buried, ahead of the ship); gone before the Shellback's quiet window |
| 13 | 1 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | Along the rail line |
| 16.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 19.5 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | Over the spine-turret nest on the embankment |
| 23 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | The leader drops an overdrive |
| 26.5 | 1 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | Gone before the Shellback's quiet window |
| 30.5 | 2 | single | [Shellback](../../../enemies/ground/shellback/README.md) | 1 | front | **New.** Alone until t=34 (no dome on the screen yet: nothing for its hooked mortar); Varga's line at t=29 |
| 34 | 2 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (left) | Down the left edge, clear of the bombers |
| 35.5 | 2 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | **Hooked** (`always`): hang at y≈220 over Bradbury's lane (x 130); Bradbury's centre passes their row at t≈41.5; Varga's line at t=36.5 |
| 38.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Down the right lane |
| 40.5 | 2 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | |
| 43.5 | 2 | stream | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | right side | **Hooked**: in at y 140 from the right edge, 1 s apart, over Lowell (x 350; its centre passes at t≈48.8); warned 3 s ahead |
| 46.5 | 2 | single | [Shellback](../../../enemies/ground/shellback/README.md) | 1 | front | **Hooked**: walks down beside Lowell's ring from 130 px behind it, then steps aside between the domes; a roll set off before it steps aside grazes the ring |
| 48.5 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (alternating edges) | |
| 52 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | Gone before the Herald's quiet window; Lowell leaves at t≈54.3 |
| 55.5 | 2 | carrier + escorts | [Choir Herald](../../../enemies/air/choir-herald/README.md) + [Needler](../../../enemies/air/needler/README.md) | 1 + 4 | front | **New.** The Herald with 4 Needlers in `orbit`, ringed while inside its 90 px, in at 50 px/s (round 40); alone until t=59 (no dome on the screen); Varga's line at its first ring |
| 59 | 2 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (right) | |
| 60 | 2 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 4 | front | In the nest growth between the outer domes, after Lowell has left |
| 62.5 | 2 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | |
| 66 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 69 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 71 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (alternating edges) | Over the burnt outer greenhouses |
| 74 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (left) | Down the left edge before the Helix beat |
| 77 | 3 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 8 | front | **The Helix beat**: the swarm's route bends round the compound's fence (x 300–460) and closes up below it; nothing fires on the compound |
| 79.5 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | Down the left edge, away from the compound |
| 83.5 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | Down the left edge as the compound passes |
| 86 | 3 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 4 | front | After the compound has passed |
| 91.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 95 | 3 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | One per edge, hover 6 s; Rook's flank bark at t=95 |
| 99 | 4 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | The inner ring comes into view |
| 102 | 4 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | **Hooked**: over Schiaparelli (x 110; its centre passes their row at t≈108.0) |
| 105.5 | 4 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | Down the right edge |
| 108.5 | 4 | stream | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | right side | **Hooked**: over Burroughs (x 370; its centre passes at t≈113.1); warned 3 s ahead |
| 111 | 4 | convoy | [Shellback](../../../enemies/ground/shellback/README.md) | 2 | front | **Hooked**: walk down 3 s apart beside Burroughs' ring, then step aside between the domes; their rolls never overlap |
| 112 | 4 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 3 | front | In the nest growth between the inner domes; none surfaces on a dome (`no_surface`) |
| 114.5 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 118 | 4 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | |
| 119.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Gone before the Herald holds over Eden's lane |
| 124 | 4 | carrier + escorts + line abreast | [Choir Herald](../../../enemies/air/choir-herald/README.md) + [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 1 + 3 | front | The Herald holds over Eden's lane (x 240) with three **hooked** bombers under its rings (a line abreast on its slots, inside the 90 px): over Eden (its centre passes their row at t≈130.2); Varga's line at t=123 |
| 126 | 4 | stream | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | left side | Hard only: an extra **hooked** pair from the left edge over Eden, warned |
| 127.5 | 4 | single | [Shellback](../../../enemies/ground/shellback/README.md) | 1 | right side | **Hooked**: walks in from the right edge toward Eden; warned 3 s ahead |
| 127.5 | 4 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (left) | Clear of the Herald's ring |
| 131 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 134.5 | 4 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | One per edge, hover 6 s; Eden leaves at t≈135.8 and the secondary is judged |
| 137.5 | 4 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 4 | front | As Eden leaves, on into the spaceport |
| 138 | 4 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | |
| 141.5 | 5 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | Over the spaceport |
| 145 | 5 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | |
| 148.5 | 5 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 152 | 5 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (alternating edges) | |
| 155.5 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 157.5 | 5 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 161.5 | 5 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 4 | front | Up through the apron's cracked slabs |
| 162 | 5 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | |
| 165.5 | 5 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (alternating edges) | |
| 168 | 5 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | The last wave |

Totals: Dust Kite 56 · Burrower 23 · Stinger 35 · Needler 17 · Shellback 5 · Spore Bomber 12 · Mote Swarm 30 · Skitter 12 · Choir Herald 2 · Mantis 4.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 1 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×4 on the rail embankment (t≈18) | Bounty 12 each |
| 2 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) floe ×3 in the nest growth between the outer domes (t≈64, after Lowell has left) | Bounty 15 each; not hooked |
| 3 | Seed-vault capsule under a collapsed greenhouse frame (`ground` trigger, 20 HP, t≈76, x 70) | The secret: frees a crate worth 100 (256) |
| 3 | Spine Turret nest ×3 in the rail yard, left half (t≈90, clear of the compound) | Bounty |
| 4 | Polyp Mortar floe ×3 between Burroughs and Eden (t≈117), not hooked | Bounty |
| 5 | Spine Turret nest ×4 on the spaceport apron (t≈150) | Bounty |
<!-- /data -->

The Shellbacks are waves (they walk); their husks stay on the ground. A Shellback's roll crushes
destructible scenery and crates in its path, as its spec says, but no ground target of this level
lies on a roll path.

## Hazards

- **Shellback mortars**: the marked impacts (1.2 s) and the ring of 8 on the ship's plane.
- **Spore mines** from every Spore Bomber, hooked or not.
- No terrain collision; the domes and the compound are flown over.

## Secrets and pickups

- **Seed-vault capsule** (the secret, t≈76, x 70 at the left edge, under a collapsed greenhouse
  frame in section 3, opposite the Helix compound): a `ground` trigger of **20 HP** (anti-ground
  counts as usual); destroyed, the frame lifts and the capsule pops as a hidden crate worth **100**
  in Act 1 terms (256 in Act 3). The Salvage scanner's glint marks it. Its line, Ares Control:
  "*That's Eden's seed stock. You'll get a drink for that, Earth.*" (an event line, dropped after
  6 s if the radio is busy).
- **Overdrive**: dropped by the t=23 Stinger leader.
- **Armour patch**: an Ares militia supply drone (the [CDF supply drone](../../../allies/README.md#cdf-supply-drone)'s
  presentation, flying in from below as in Level 14) releases one at (240, 380 y-up) at t=100.
- **Shield cell**: dropped by the second Choir Herald (t=124) at its death.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), retimed to the HUD's **1 s rule** (§1.7: a
timed line holds the radio for the longer of its voice and its text, 5 s for the last page plus 3 s
for each page before it with the text typed at 30 characters a second, then 0.4 s of silence; no
timed line more than 1 s late). The times below start every line after the one before has closed
(as text; the voiced check runs when the takes exist). Event lines drop after 6 s. Rook's lines
carry `requires: escort`; his one dry joke is at t=143. Varga's line about the Herald's rings is the
event `first-ring` (round 40 K.a; it was timed at t=57.5 and came after the Herald was shot): it starts
in the step the first Herald rings its escorts as it reaches the top edge (t≈55.8).

**The colony's voice: Ares Control**, [Level 15](../level-15-olympus-descent/README.md#radio-chatter)'s
speaker (no new speaker here): the colonist who runs Ares Landing's flight control and, in the
siege, speaks for the colony; proud, dry, resentful that Earth came late (the act's L15 beat), a
man's voice. One character across the act ([Act 3 voice](../README.md#voice)), cast in round 36 (S.6 = b, Thomas A). **Pronunciation**: the voice tests read "Ares" as
"air is" without a prompt, and "Schiaparelli" will need one too: the voice step pins seeds or
prompts for both.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Generic colonial (Ares Control) | "Aegis flight, Ares Landing. Took Earth long enough. Five domes still hold air: keep the bombers off the glass." |
| t=14 | Rook | "Copy, Ares. Lancer, I'll keep an eye on the flanks." |
| t=29 | Varga | "Shelled walker. Hit the lime vent; when it curls up, wait." |
| t=36.5 | Varga | "Bombers on the domes! Get them before they're over the glass." |
| t=47 | Generic colonial (Ares Control) | "Bradbury and Lowell grow our food, Aegis. Every pane counts." |
| t=71 | Generic colonial (Ares Control) | "Outer greenhouses are gone. The inner ring is still sealed." |
| t=78 | Varga | "Commander, they have not touched a single Helix facility. Not one." |
| t=88.5 | Okafor | "Not on this channel, Doctor." |
| t=95 | Rook | "Flankers, both edges. Mind their beams." |
| t=102 | Generic colonial (Ares Control) | "Inner ring: Schiaparelli, Burroughs, then Eden. Eden feeds the whole colony." |
| t=123 | Varga | "Singer with bombers under its rings. Singer first, Lancer!" |
| t=130.5 | Generic colonial (Ares Control) | "Eden is under you. Everything we grow is under that glass." |
| t=143 | Rook | "If we save the greenhouse, I'm owed a tomato." |
| t=150 | Okafor | "Ares Landing holds. Clear the spaceport, Aegis, then set down." |
| The first Herald's first ring (`first-ring`, round 40) | Varga | "That flyer is singing shields onto the others. See the rings? Kill the singer first." |
| A dome's first hit (`ally-hit`) | Generic colonial (Ares Control) | "{ally} is hit! The glass is holding... for now." |
| A dome breaks (`ally-lost`) | Generic colonial (Ares Control) | "We've lost {ally}. Its people are below. Its crops aren't." |
| Secondary met (`secondary-objective`) | Generic colonial (Ares Control) | "All five domes still standing. Fine. Earth can stay for dinner." |
| Level end | Okafor | "The domes are behind you and the colony is breathing. Good work, Aegis." |
<!-- /data -->

The secret's line (the seed capsule freed, an event line dropped after 6 s if the radio is busy), Ares
Control: "That's Eden's seed stock. You'll get a drink for that, Earth." (the data's `secrets`).

Timeline check: `RadioTimelineLevel17Test` plays the queue with the text and with the voices, with
and without Rook, over runs with the domes' hits and losses by name: every timed line starts at most
1 s late (step D5 retimed four lines for it, see Decisions). The domes' event lines wait for a gap and
are often dropped as stale when they fall under a timed line (the test lists them). Varga's L16–17 beat and Okafor's silencing follow M13 = a: in the level
only Varga's observation and Okafor's one line; the private conversation is in the L18 briefing
(Okafor's page) and the hangar intel. Varga's t=80.5 line is her approved Act 3 line from her
[character sheet](../../../story/characters/varga/README.md), said after she has seen two Helix
sites (the station in Level 16, the compound here); the swarm bending round the fence shows the
rest.

## Music & ambience

The Act 3 A theme **"Red Dust Run"** (track 8 in the [track list](../../../audio/music/README.md#track-list),
chosen in round 02; its production stems come with part B): the Mars colony ambience alone in
section 1, the **base stem from section 2** (the first dome), the **full mix from section 4** (the
inner ring), the base stem again in section 5 to the end. Ambience: a new `mars-colony` bed (plains
wind, the domes' air-plant hum, distant rail clank), listed for the act's sfx step; the militia
flak at t≈160 plays a distant flak sound. New sounds this level needs: the dome's hit (glass stress,
no bubbly or popping sounds), its break (a long glass cave-in), the Herald's song and aura hum, the
ring's absorb flash, the Shellback's walk, lob, curl and roll; listed in
[sfx](../../../audio/sfx/README.md) by the act's audio step.

## Credit budget

Budget(17) = 700 × 1.07¹⁶ ≈ **2,067**, the typical haul's target
([economy](../../../systems/economy/README.md#per-level-budget)). The data keeps **Act 1 terms**
(Dust Kite 4, Skitter 5, Needler 12, Stinger 15, Mote 2, Spore Bomber 25, Choir Herald 30, Mantis
30, Shellback 48 whole, Spine Turret 12, Polyp Mortar 15, the crate 100, the secondary 100); the
code pays each × the act factor **2.56**, bounties also × the scale. A **`bounty_scale` of 0.40**
(step PE17, from round 36's 0.60, Q.17c, with the reworked waves: the Burrowers pay 24 each) puts the
typical haul at **2,048 (−0.9 %**, `TypicalHaulTest`; the table's model −2 %), the perfect run at
3,145. No boss or mid-boss.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Dust Kite 56 × 4 + Burrower 23 × 24 + Stinger 35 × 15 + Needler 17 × 12 + Shellback 5 × 52 by parts (48 whole) + Spore Bomber 12 × 25 + Mote Swarm 30 × 2 + Skitter 12 × 5 + Choir Herald 2 × 30 + Mantis 4 × 30 | 2,411 | 1,599 |
| Ground targets: Spine Turret 11 × 12 + Polyp Mortar 6 × 15 | 222 | 178 |
| Secret: seed vault (hidden crate, 5% of budget) | 256 | 128 |
| Secondary: no dome lost | 256 | 128 |
| **Total** (bounty scale 0.4) | **3,145** | **2,032** |
| Budget(n) = the typical haul's target; typical -2 %, perfect 1.52 × budget | | 2,067 |
<!-- /data -->

Whole-unit Shellback bounty (48) is used; a player who kills the vent first earns 52 (the part
bounties). **Balance plan**: the plan's L17 visit comes from part A's Act 3 balance plan (M18); the
level expects `spread` and `anti-ground` and is flyable without either.

## Difficulty notes

- **Easy**: a dome breaks on its 4th hit; the Shellbacks are not hooked (their mortar lobs only at
  the ship; their roll still crushes); the three-bomber waves (t=102, t=124) have two; the Herald's
  and Shellback's easy hooks from their specs.
- **Hard**: a dome breaks on its 2nd hit; an extra hooked Spore Bomber pair from the left edge over
  Eden (t≈126, warned); the other waves by the formation lever, trimmed by hand on the Burrower fields
  (one fewer each) and the t=119.5 Needler line (2) for at most ~3.5 enemies on the screen (step PE17:
  3.38); the Herald's radius 105 px and the Shellback's longer roll from their specs.

## Concept art

Production art of M7 part D's round 40 (step ART-D; the mood from the Ares Landing mood still and the chosen dome **a**, geodesic glass on a concrete ring, in [mars](../../../world/mars/README.md), round 36 MA.3–MA.4). The dome sprites are the [Ares Landing dome](../../../allies/ares-landing-dome/README.md)'s, the briefing images and intel portraits the [briefing](../../../ui/briefing/README.md)'s and [hangar](../../../ui/hangar/README.md)'s. The round's capture (step D5): the level at medium with the plan's fit and Rook, with a short second run on hard without firing for the domes cracking and breaking. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r40-a.png](concept/backdrop-final-r40-a.png) | Level 17 backdrop final (`tools/art/backdrop_l17.py`): the play field at twelve times with the domes, and the main pieces (sheet) | chosen (round 40, F.d / F.e: approved as final) |
| [concept/backdrop-final-r40-a.gif](concept/backdrop-final-r40-a.gif) | Level 17 backdrop final: the whole level flown at 0.5x, 10 fps | chosen (round 40, F.d / F.e: approved as final) |
| [concept/l17-props-final-r40-a.png](concept/l17-props-final-r40-a.png) | The seed-vault secret (`tools/art/siege_units.py`): the collapsed greenhouse frame (whole, hit, badly hit, lifted), the capsule and its glow (sheet) | chosen (round 40, F.d / F.e: approved as final) |
| [concept/l17-props-final-r40-a.gif](concept/l17-props-final-r40-a.gif) | The frame shot apart and the capsule popping free, 10 fps | chosen (round 40, F.d / F.e: approved as final) |
| [concept/greenhouse-frame-final-r45-a.png](concept/greenhouse-frame-final-r45-a.png) | Round 44 G.c (`tools/art/siege_units.py props`): the greenhouse frame split off its burnt pad, the frame transparent round the structure, the pad its own ground decal `greenhouse-pad` (64×48), the frame over its pad and the hit flash following the structure (sheet) | chosen (round 45) |
| [concept/greenhouse-frame-final-r45-a.gif](concept/greenhouse-frame-final-r45-a.gif) | The frame over its pad shot apart, hit flashes on the structure only, the capsule popping free, 10 fps | chosen (round 45) |
| [concept/level-17-capture-r40-a.png](concept/level-17-capture-r40-a.png) | Capture of the game (step D5): twenty frames of the medium run with the plan's fit and Rook (and four of a hard run without firing), the last the debrief | chosen (round 40, K.a: accepted with notes, the fixes queued in Implementation) |
| [concept/level-17-capture-r40-a.mp4](concept/level-17-capture-r40-a.mp4) | Capture of the game with its sound: the whole medium run, then the hard run's domes cracking and breaking and the Herald's rings | chosen (round 40, K.a: accepted with notes, the fixes queued in Implementation) |
| [concept/level-17-fixes-r42-a.mp4](concept/level-17-fixes-r42-a.mp4) | Check capture of the dome fixes (round 42, medium, the plan's L17 fit, the test autopilot): the dome sections with the bombers' run-ins, a dome turning yellow and still counting, the debrief `DOMES STANDING 5 / 5` (172 s with sound) | chosen (round 42, K.c: accepted) |
| [concept/level-17-fixes-r42-a.png](concept/level-17-fixes-r42-a.png) | 20-frame sheet of that excerpt | chosen (round 42, K.c: accepted) |
| [concept/greenhouse-frame-glow-r44-a.mp4](concept/greenhouse-frame-glow-r44-a.mp4) | The seed vault's glow: the collapsed greenhouse frame drawn with the capsule's `seed-capsule-glow` (it has no glow of its own). **Fire held by the snapshot hook from 66 to 78.5 s**, because in a normal run the autopilot shoots the frame about 1.5 s after it enters (checked in a live run). The frame enters at ≈ 0:01, a small warm-yellow glint at its centre swells and fades slowly (brightest ≈ 0:02.4); fire resumes, the frame's hit flash (a flat white rectangle over the whole sprite box), it breaks at ≈ 0:04.2 and the capsule is caught (+256, ≈ 0:04.5). The glow reads weakly at play size: a soft glint, not a strobe you notice. With sound, 8 s | chosen (round 44, G.b: accepted) |
| [concept/greenhouse-frame-glow-r44-a.png](concept/greenhouse-frame-glow-r44-a.png) | Six field frames + five 1:1 crops of the frame at 3× across one swell | chosen (round 44, G.b: accepted) |
| [concept/greenhouse-frame-r45-a.mp4](concept/greenhouse-frame-r45-a.mp4) | Round 45 check capture (step H6): the greenhouse frame hit in a normal run, the capsule popping out and caught, 6.5 s with sound | chosen (round 45) |
| [concept/greenhouse-frame-r45-a.png](concept/greenhouse-frame-r45-a.png) | Six field frames and seven 2x crops following the frame | chosen (round 45) |

## Implementation

- [x] Level data `data.yaml` (when the level is built, part D): sections and speeds with 2 s eases,
      waves, ground targets, radio, budget, threat profile, briefing, backdrop, music (step D2; step D4
      merged step ART-D's backdrop, the domes' aprons under the domes as the simulation places them)
- [x] The Ares Landing dome ally (M16 = a): five domes `follows: ground` at their x and scroll,
      hits (3, easy 4, hard 2), the crack states and the break, never failing the mission; its own
      document in [allies](../../../allies/README.md) (Q.25b), the geodesic glass look (MA.3 = a)
      (`GroundAlliesTest`, `Level17Test`, `DomeLooksTest`; the cracks and a break in the capture)
- [x] The hook: hooked Spore Bombers `always` holding a row over the next dome and dropping one hit
      as it passes beneath; hooked Shellbacks `alternate` mortar on the domes; the Shellback roll
      crushing a dome it crosses (`Level17Test`)
- [x] Secondary `untouched` "No dome hit": failed at the first hit, met when Eden leaves; 100 in
      Act 1 terms; the `DOMES` tracker and the debrief row `DOMES UNTOUCHED n / 5` (`UntouchedTest`,
      `LevelScreenSiegeTest`; both in the capture)
- [x] Round 41 (the user's playtest 2026-10-19, "both"; steps FX-S2 and FX-C): the secondary `standing` "No
      dome lost" (a broken dome fails it, hits do not; `DomesStandingTest`, `SiegeDataTest`) and the hooked
      bombers' `bomb_run_in: 2.5`; the autopilot over 8 seeds meets it 8/8 on easy, 8/8 on medium and 1/8 on
      hard (every hard loss Eden at t≈129) (`Level17Test`)
- [x] The debrief row `DOMES STANDING n / 5` (`Sortie.alliesStanding()`) instead of `DOMES UNTOUCHED` (step RX,
      `LevelScreenSiegeTest`), and the secondary's radio line reworded and voiced for hits allowed: "All five domes
      still standing. Fine. Earth can stay for dinner." (step VOX-F, 2026-10-19; accepted in round 40 (2026-10-19))
- [x] The dome secondary measured with the autopilot over several seeds, as Level 11's convoy: the
      hits per dome and the hooked waves tuned so a careful pilot keeps every dome about half the
      time at medium (step D4 measured it: 8/8 on easy, 0/8 at medium and hard; step D6 after round
      40's fixes, the bombers' row 160 → 220: easy 8/8, medium 5/8, hard 5/8; `Level17Test`; step PE17 with
      the reworked waves: easy 8/8, medium 4/8, hard 0/8, every first hit a bomb on Eden at t≈129; the old waves
      with `small_act_hp` alone, on today's simulation: 8/8, 8/8, 1/8)
- [x] Wave script: ≥ 55 enemies a minute at medium (`DensityTest`: 76.5), no empty screen over 3 s
      (`PacingTest`), the two quiet windows (`Level17Test`), edge warnings for the side waves
- [x] The Act 2+ rules (round 40 P.e): ≤ 3 pauses over 2 s at medium and hard (`PacingTest`: 0 and 0), ≥ 2.5
      enemies on the screen and ≥ 10 shots a minute at medium with the plan's fit (`ActFloorTest`: 2.60, 15.1),
      the Needler and the Stinger in `small_act_hp` (`ActHpFactorTest`), the dome secondary re-measured
      (`Level17Test`) — step PE17
- [x] The Helix compound set piece (backdrop) with the t=77 swarm's route bending round it
      (`SiegeChecks`' clearance, `SiegeDataTest`; the compound in the capture)
- [x] Ground targets, the seed-vault secret (20 HP trigger, crate 100), the supply drone's armour
      patch at t=100, the overdrive and the Herald's shield cell (`SiegeDataTest`, `Level17Test`; the
      secret found in the capture)
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel17Test`, with Rook and
      without; four lines retimed in step D5, see Decisions), `{ally}` dome names (each dome's hit and
      loss line rendered by name, `VoiceFilesTest`), Rook's lines `requires: escort`; Ares Control
      (Level 15's) cast
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel
      (`IntelPanelLayoutTest`); the Shellback's and Herald's portraits
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images
- [x] Typical haul within ±5 % of budget(17) = 2,067 with the level's `bounty_scale` (`TypicalHaulTest`:
      2,048 at 0.40, −0.9 %; the perfect run 3,145; step PE17)
- [x] Music: "Red Dust Run"'s stems by section; the `mars-colony` ambience; the dome and Herald sounds
      (`Level17SoundsTest`)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on
      every difficulty (`--escort none` too) (step D5; `Level17Test` flies every difficulty without Rook, `ActPlaythroughTest` with him: each won first time, M7 part I)
- [x] Production art and captures for part D's round (steps ART-D and D5; round 40: the art approved as final, `art`
      stays `chosen` until the dome-break redo (S.b) and the part D fixes below land)
- [x] Part D fixes (round 40 K.a, the user's note: "The second shell creature rolls over a dome and again some time
      later. That shouldn't happen. The radio about the shield formation is too late, it is spoken after they are all
      destroyed. The shell creatures seem too strong. None are destroyed."): the Shellbacks no longer roll over a dome
      twice, the shield-formation radio line is spoken before the Herald's shields are shot, the Shellbacks are
      weaker so a careful pilot destroys them (step D6: one roll per Shellback, its walk clear of the glass,
      `theShellbacksWalkBetweenTheDomes`; the line on the Herald's first ring, `first-ring`,
      `theFirstHeraldComesInSlowlyAndVargasLineStartsAtItsFirstRing`; body 90, vent 16, curl and uncurl 0.4 s:
      the autopilot kills 40/40 on easy, 37/40 at medium, 30/40 on hard over 8 seeds)
- [x] R.a: a shot inside the Shellback vent's hit box hits the vent first, from any side (the radio's "Hit the lime vent")
      (step D6: a bolt over the body whose line of flight crosses the vent's box, a beam whose column crosses it;
      `ShellbackTest`; the autopilot shoots every vent at medium, 40/40)
- [x] R.b: re-measure the dome secondary with the autopilot over several seeds after R.a (it was 0 / 8 on medium and hard;
      step D6: with R.a, R.c-R.e and the weaker Shellback still 0/8 at medium, every first hit a bomb on Eden; the bombers'
      row 160 → 200 → 220 in two passes: medium 3/8, then 4/8; with the t=69 swarm (below) medium 5/8, hard 5/8, easy 8/8)
- [x] R.c: route the Shellbacks' walks between the domes, not across their glass (step D6: the t=46.5 and t=111 paths
      down the dome's ring lane, then aside; `SiegeChecks`' walk rule, `SiegeDataTest`; `theShellbacksWalkBetweenTheDomes`)
- [x] R.d: the first Choir Herald (t=55.5) enters higher or slower so its rings are seen (step D6: in at 50 px/s, the
      wave's `speed`; its first ring at t≈55.8, the autopilot shooting it first downs it at t≈57.4)
- [x] R.e: a Shellback shot near the bottom of the screen rolls a shorter way, so its belly window shows (step D6: the
      roll's `belly_row` 200 px; `ShellbackTest`, `aShellbacksRollCrushesTheDomeAheadOfIt`)
- [x] R.f (decided: keep): the dome `ally-hit` / `ally-lost` lines stay event lines (wait for a gap, dropped after 6 s)
      (unchanged; `RadioTimelineLevel17Test`)
- [x] The dome-break sound redone as a rumble from a cave-in or collapse recording (S.b, step AUD-S; round 41 S.h chose `dome-break-r40-c`)

## Open questions

- None open: round 36 answered every question (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the Act 3 roster to a draft level document.
  Taken as the recommended options, accepted in round 36 (2026-10-13): M16 = a (the dome as a new
  ally on the Nansen Relay's rules, secondary "no dome hit"), M8 = a (the Herald's absorbing rings),
  M10 = b (the Dust Kite as the act's popcorn), M12 = a, M13 = a (in-level only Varga's observation
  and Okafor's "Not on this channel, Doctor."), M17 = a (Rook flies, one dry joke), M18 = a
  (reference DPS 84), M19 (hand tables). Our proposals: five named domes counting hits (3 / easy 4 /
  hard 2) instead of an integrity bar; the hooks (bombers `always` holding over the next dome,
  Shellbacks `alternate` and their roll); the new secondary kind `untouched`; the slow dome sections
  (80 and 70 px/s) instead of a halt (a halt needs a `defend` primary); the Helix compound as a
  backdrop set piece with a swarm bending round it; Ares Warden as the colony's voice; the seed-vault
  secret; 247 enemies (≈ 89 a minute) and `bounty_scale` 0.55 for a typical 2,072.
- 2026-10-12 (overnight, harmonised with the act README): density brought into the act's band of
  60–72 a minute (accepted in round 36 (2026-10-13) by smaller popcorn waves at the same
  cadence: Dust Kite streams 4–5 → 3, V-wings 5 → 3 and 7 → 4, snakes 6 → 4, the Helix swarm
  10 → 8; Mote Swarms 8 → 6; Skitter snakes 6 → 4; 196 enemies (≈ 70 a minute, front 95 % · sides 5 %), the hooked waves,
  Shellbacks and Heralds unchanged; `bounty_scale` 0.55 → 0.60 (typical 2,063, −0.2 %), and the
  open question corrected (thinning raises the scale). Ares Warden merged into Ares Control, one
  colony voice (the nine lines and the secret's text); Varga's t=80.5 line replaced by her approved
  Act 3 line "Commander, they have not touched a single Helix facility. Not one." (closes ≈ 88.1,
  before Okafor's t=88.5 cut-off, which stays).
- 2026-10-13 (user, round 36): approved (D.4), M16 accepted. Q.17 all yes: the hooked Shellbacks'
  mortar and roll hurt the domes too; "No dome hit" is all or nothing, tuned by measurement;
  `bounty_scale` 0.60 accepted; Ares Control is the colony's one voice (Ares Warden merged; cast S.6
  = b). MA.3 = a: the dome is the geodesic glass dome on a concrete ring. The dome becomes its own
  allies document in part D (Q.25b; now a draft roster row in [allies](../../../allies/README.md)).
- 2026-10-18 (M7 part D, step D2): the level's [data.yaml](data.yaml) written from the approved hand tables;
  the briefing, teaser, threat profile, layout, waves, ground targets, radio and credit budget tables are now
  rendered from it (the typical haul 2,044, −1.1 %, with the Shellback whole at the ground rate; step D4's
  `TypicalHaulTest` fits it). The dome's spec moved to its own document,
  [Ares Landing dome](../../../allies/ares-landing-dome/README.md). Built as the gap check's defaults, accepted in round 40 (2026-10-19): the new level block `allies` (the five domes at their entry `[t, x]`,
  Eden's 144 px `size` and sprite overrides), the hook per unit type (every Spore Bomber wave `always`, every
  Shellback `alternate`; easy unhooks the Shellbacks), the bombers' row 160 px below the top edge and their
  x slots 40 px apart, the t=124 bombers a plain hooked wave of the Herald's escorts (step D4: a line abreast beside it, not its escorts, as a bomb run never applies to escorts), the Polyp Mortar floes
  static nests, the Shellback pair a `convoy` of 2 walking 3 s apart, the Herald's shield cell by
  `dropped_by.enemy`, the militia's flak a presentation key `flak`, the dome sections marked as phases on the
  intel's wave strip; the 3-bomber waves' easy two and hard four come from the formation lever. The backdrop
  is a placeholder (Level 15's images) until step D4 merges step ART-D's.
- 2026-10-18 (M7 part D, step D4): the level mapped for the simulation (content's `SiegeRules`: the five domes
  as ground allies with the hook per unit type, the `untouched` secondary, the Herald's aura and song, the
  Shellback's vent and roll, `dropped_by.enemy`). Built as defaults, accepted in round 40 (2026-10-19): the
  t=124 bombers a plain hooked line abreast beside the Herald on the ±40 px slots, not its escorts (a bomb run
  never applies to escorts); the side bomber pairs (t=43.5, 108.5, hard's 126) streams on a route in at the row
  140 px below the top edge, 1 s apart (a side column needs a hovering or diving unit); six popcorn waves moved by
  0.5–1.5 s for `PacingTest` on easy (t=73.5 → 72.5, 80.5 → 81.5, 98.5 → 100, 105 → 105.5, 162.5 → 162, 166 →
  165.5; the quiet windows unchanged); step ART-D's backdrop merged, the domes' aprons a footprint later than its
  top-edge reading (the simulation enters a dome by its lower edge), the rail pieces one transparent row longer at
  either end, the aprons', compound's and gun pit's edges faded, the spaceport apron's slab grid off its wrap. The
  dome secondary with the autopilot over 8 seeds (the plan's fit, no Rook): easy 8/8 (5.0 of 5 domes kept),
  medium 0/8 (2.0 of 5), hard 0/8 (2.1 of 5). Every medium and hard run loses Lowell to the t=46.5 Shellback:
  shot, its roll crosses Lowell; left alone, its second lob lands on Lowell; its vent, which the radio tells the
  pilot to hit, sits inside the body's hit box at the rear of its back and no shot from below reaches it (0 vents
  shot). Open for round 40 (the vent's hit test or the walk behind the domes), not tuned further.
- 2026-10-19 (step D5, accepted in round 40 (2026-10-19): the radio retimed for
  `RadioTimelineLevel17Test` (the text, not the voices, held the radio too long: Varga's two-page t=36.5
  bomber line opened Ares Control's t=45 line 2.0 s late, her t=80.5 Helix line Okafor's t=88.5 cut-off
  2.7 s late, and with Rook his t=95 and Ares Control's t=102 lines 2.5 and 2.3 s): t=45 → 47, 55.5 →
  57.5, 71.5 → 71, 80.5 → 78 (Varga's Helix line now 3.5 s before the compound enters; Okafor's, Rook's
  and the inner ring's times, which `Level17Test` pins, unchanged). The capture (medium, the plan's fit,
  Rook, no `--invulnerable`) survived with armour untouched, 190 / 196 kills, the secret, every dome hit
  once and none lost: DOMES UNTOUCHED 0 / 5.
- 2026-10-19 (user, round 40, phone page): the final art approved (F.d backdrop, F.e props: `art: chosen`, to `final` once the
  dome-break redo and the fixes land), the sounds accepted except the dome break (S.b: "It needs a rumble. Look in the
  online database for cave in or collapse sounds."), the voices (V.a), the retimed radio (C.a) and the build choices
  (B.a-B.e) accepted. The capture K.a came back with a note: "The second shell creature rolls over a dome and again some
  time later. That shouldn't happen. The radio about the shield formation is too late, it is spoken after they are all
  destroyed. The shell creatures seem too strong. None are destroyed." Decided and queued (see Implementation): R.a = a
  (a shot inside the vent's box hits the vent first), R.b re-measure the secondary after R.a, R.c route the walks between
  the domes, R.d the first Herald enters higher or slower, R.e roll a shorter way near the bottom, R.f keep the dome lines
  as they are.
- 2026-10-19 (step D6, round 40's fixes, accepted in round 40 (2026-10-19)): **K.a / R.c** the rule chosen: a Shellback
  walks between the domes (its body's hit box never over a dome's hit box, the glass; `SiegeChecks` checks every
  walker with a roll), and its roll, once per Shellback, crushes a dome whose **footprint** (the concrete ring) the
  ball touches, one hit (the dome's `rolls: crossing its footprint`; it was the hit box). The t=46.5 and t=111
  Shellbacks walk down the dome's ring lane 90 px to its left from 130 px behind it, then step aside 50 px short of
  level: a roll set off on the ring lane grazes the ring, one set off after the step aside misses it, so a careful
  pilot shoots the vent, waits for the step aside and finishes it (a walk beside the dome all the way would keep the
  threat for its whole pass at 24 px/s); the t=127.5 path never comes near Eden (its lobs remain). The convoy's
  `interval: 3` now applies (walkers ignored it: 1.5 s). **K.a** weaker: body 120 → 90, vent 24 → 16, curl and
  uncurl 0.6 → 0.4 s; Varga's ring line the new event `first-ring`. **R.a**: a shot over the body whose line of
  flight crosses the vent's box (a beam whose column crosses it) hits the vent. **R.d**: the first Herald's wave
  in at 50 px/s (its `speed`). **R.e**: the roll's new `belly_row` (200 px): a roll drifting down the screen ends
  where the scroll would carry it below that row before its uncurl ends. **R.b**: medium 0/8 → bombers' row
  (`bomb_row`) 160 → 200 (3/8) → 220 (4/8; hard 3/8, easy 8/8); then the t=69.5 Mote Swarm moved to t=69 for
  `PacingTest` on easy (the autopilot clears the t=66 Needlers sooner now: 3.0 s empty), and the final runs give
  medium 5/8, hard 5/8, easy 8/8 (the autopilot is chaotic: a 0.5 s move changes its runs). The dome table and the hooked waves' notes
  give the centres' times at the new row.
- 2026-10-19 (user, round 41): Level 17's round 40 fixes (L.a, step D6, (a)-(g)) accepted, all yes. S.h = c: the dome-break redo is option c (the low-passed building collapse over a crust rumble with glass smash); step FXS wires it, so its checklist item stays open until then.
- 2026-10-19 (step PE17; the user's round 40 P.e "all later levels" and "tougher, not more"; accepted in round 40 (2026-10-19)): the waves reworked to the Act 2+ rules. The five domes, the hooked bomber waves, the Shellbacks, the
  Heralds, the Helix beat, the Mantis pincers, the quiet windows, the ground targets, the pickups' carriers and the
  radio keep their times. Added: six **Burrower fields** (t=9, 60, 86, 112, 137.5, 161.5; 4 each, the inner one
  3, hard one fewer each; the t=9.5 V-wing and the t=88 stream made way), the inner one kept off the domes and
  the compound by a `no_surface` block (new in this level); nine **Stinger streams** of 3 for three Skitter waves,
  five Dust Kite waves and the t=81.5 V-wing; a Kite stream at t=74 and a V-wing at t=168. Moved: the t=42 V-wing
  to 40.5, the t=84.5 snake to 83.5 (left edge), the t=100 snake to 99, the t=159 V-wing to 157.5; the t=121
  Needler line of 4 became 3 at t=119.5 (hard 2), else it outlived the t=124 Herald's arrival and Eden broke in
  every medium run. Needler circles (Level 16's) were tried first: the plan's fit kills them before their first
  shot here (medium 1.8 shots a minute), so the Burrowers' rings carry the floor. The Needler and the Stinger
  joined `small_act_hp` (12 and 18 HP). Medium 196 → 213 enemies (≈ 70 → 77 a minute), 1.09 → 2.60 on the
  screen, 0.4 → 15.1 shots a minute, 17 → 0 pauses over 2 s; hard 240 → 249, 1.58 → 3.38 on the screen, 0.7 →
  21.9 shots, 9 → 0 pauses; easy 154 → 165, 2 → 1 pause over 3 s. `bounty_scale` 0.60 → 0.40 (typical 2,048,
  −0.9 %; perfect 3,145). The dome secondary with the autopilot (8 seeds): easy 8/8, medium 4/8 (the README's
  "about half"), hard 0/8, every first hit the t=124 bombers on Eden, before step FX-C's bomber run-in and "No
  dome lost". Burrowers on the colony ground and the spaceport apron (t=161.5 "through the cracked slabs") are a
  setting stretch for the user's review.
- 2026-10-19 (round 41, the user's playtest: "both"; steps FX-S2 and FX-C, accepted in round 41 (2026-10-19)): the
  secondary becomes **"No dome lost"** (`standing: true`: failed when a dome breaks, hits allowed; was
  `untouched`, "No dome hit") and the hooked bombers fly **2.5 s on the play field before they may drop**
  (`bomb_run_in`). The autopilot over 8 seeds: easy 8/8, medium 8/8 (4/8 under the old rule), hard 1/8 (0/8);
  every hard failure is Eden breaking at t≈129 under the t=124 bombers. Left alone, easy now loses no dome (the
  secondary is met; 4 hits break a dome there), medium loses 3 and hard 4. The radio's secondary line ("Five
  domes, not a crack.") was reworded by step VOX-F: "All five domes still standing. Fine. Earth can stay for
  dinner.", with a new take (accepted in round 42 (2026-10-20)).
- 2026-10-20: round 42 closed: the rework to the Act 2+ rules (R.c) and the dome fixes with their check capture (K.c, T.4: both fixes, every sub-choice yes) accepted by the user; the hard dome rate (R.i) kept.
- 2026-10-20 (user, round 44, A.b (a): production assets (a `Source` chunk in `assets/`, every Concept art row decided)): **`art: final`** for this part. A later addition (a new sprite, a new concept round) is its own row and does not reopen the rest.
- 2026-10-20 (user, round 44, closed): **G.b** the greenhouse frame's seed-capsule glint accepted; **G.c** the frame's hit flash lights the whole 64x48 sprite as a white slab because the burnt ground pad is baked into it: **split the pad off as a ground decal** (the frame sprite becomes the frame only and the pad a decal under it; a small art step, planned, not started; the final asset changes and its generator stays reproducible).
- 2026-10-20 (steps ART-K, GK): round 44 G.c built: the greenhouse frame is transparent round the structure, the burnt pad is the ground decal `greenhouse-pad` (drawn under the frame and claimed so it stays in the atlas), the hit flash follows the structure only (`tools/art/siege_units.py`); the files are listed under Concept art as proposed and are a row in [round 45](../../../concept-rounds/round-45/README.md).
- 2026-10-21 (user, round 45, A.c): the greenhouse frame and its burnt pad approved.
