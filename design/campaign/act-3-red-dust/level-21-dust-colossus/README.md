---
title: Level 21 – Dust Colossus
design: approved
implementation: done
art: final
depends-on: [../../../enemies/bosses/dust-colossus, ../../../enemies/ground/burrower, ../../../enemies/air/dust-kite, ../../../world/mars, ../../../systems/economy]
updated: 2026-10-21
---

# Level 21 – Dust Colossus

## Summary

The Act 3 finale. After the retreat along the Hellas rim, Aegis Wing turns round and goes down
into the basin, where every Vrell attack on Mars began. Lancer and Rook descend the inner wall,
cross the dune sea and reach the lime hollow at the basin's lowest point. There the act boss rises
out of the sand: the [Dust Colossus](../../../enemies/bosses/dust-colossus/README.md), which
breaches at the edges of the field, front, sides and rear, and then under the ship. 105 s of
approach, then the fight on a slowly crawling arena (about 100 s at medium), then 32 s of settling
dust while the Choir speaks its first words and the Vrell leave Mars. The act end follows Level 14's
order.

**Status: approved in concept round 36 (user, 2026-10-13).** Written in the overnight design
phase (2026-10-12) from the act roster and the gap analysis's recommended answers, all kept or
accepted in the round. M7 part H (step PH2, 2026-10-20) moved the numbers into the level's
[data.yaml](data.yaml) with round 43's answers (H.1–H.7): the tables below are rendered from it.

## Briefing

Two pages, as every level since Level 08, each one screen with its image (`BriefingLayoutTest`),
at most four lines of text so the image fits. Texts (approved in round 36):

<!-- data: briefing -->
> **Commander Okafor:** "No more running. Every Vrell attack on Mars came out of the Hellas
> basin, and Doctor Varga thinks she knows why. We go down there and end it. Ares Landing is
> counting on you, Lancer."
>
> **Dr. Varga:** "The song comes from under the basin floor. It's one organism, the size of a
> colony. It breaches where the sand bulges, from any side, even behind you. The throat glows
> lime. Bring side and rear guns."
<!-- /data -->

*Images* (production art straight away, as in Act 2; [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`): Okafor's page `level-21-hellas-basin` (the basin on a CDF plot:
the rim line held at the end of the retreat, the descent route down the inner wall, the dune sea,
the lime hollow at the lowest point with seismic rings centred on it), Varga's
`level-21-colossus-scan` (her seismic cut through the basin floor: the body under the sand, the
ridge above it, a mouth breaching at an edge with the lime gullet, the bite circle and the suction
lines drawn in).

*Hangar teaser* (shop screen before L21, Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "Something big under Hellas. Bring side guns, rear guns. Bring all the guns."
<!-- /data -->

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule); the
fight's breaches are not edge waves and are not in the numbers.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `ground` (Burrowers when surfaced, Dust Devils, Shellbacks, turret nests and mortars, the Colossus's mouths), `buried` (Burrowers under the sand and the Colossus's body: nothing hits them), `air` (Dust Kites, Needler and Stinger groups, Mantises, Wraiths, the Choir Herald, the Mote stream); `low-air` (risen Dust Devils, the dust haze) |
| Attack directions | front 85% · sides 8% · rear 8% (the approach by its waves; in the fight the mouths breach from all four edges) |
| Density | 4 |
| Recommended traits | `anti-ground`, `rear` |
| Hazards | The Colossus's suction and the debris rocks (the arena); the ridge pass (scenery) |
| Boss / mid-boss | Act boss [Dust Colossus](../../../enemies/bosses/dust-colossus/README.md) |
| Sensor-suite detail | **none**: Hellas basin, `ground` + `air`, all directions. **L1**: + the directions, density 4, hazard "suction", the objective `DESTROY THE DUST COLOSSUS`. **L2**: + Burrower, Dust Devil, Shellback, Choir Herald, Dust Kite, Mantis, Wraith, Needler portraits; the boss's name and silhouette (`dust-colossus`). **L3**: + `anti-ground` and `rear` highlighted; the timeline strip marks the ridge pass (64–72 s) and the boss (105 s); 1 secret. |
<!-- /data -->

*Varga's intel line* per sensor-suite level (fitting the panel is `IntelPanelLayoutTest`'s):

| Sensor suite | Line |
|---|---|
| none | "Hellas basin, Lancer. Every Vrell on Mars came out of it. Something is still down there." |
| L1 | "From every side, even behind you. It comes up where the sand bulges." |
| L2 | "Burrowers, dust devils, a singer. And something under the floor, colony-sized." |
| L3 | "Anti-ground, side and rear guns. Hit the lime when the throat opens." |

## Objective

- **Primary** `reach-end` with the arena as the condition, as Level 14: the level ends when the
  Dust Colossus is destroyed and the settling-dust section has run. The briefing and the objective
  line read `DESTROY THE DUST COLOSSUS` (the boss's name on the bar). It is not a `boss` primary.
- **Secondary** "Clear the basin" (`kill_ratio`, §1.7): destroy at least **65 %** of the level's
  enemies (the approach's; the boss and its debris are not counted), judged at the level's end.
  Burrowers that dive away after their third surfacing count against it. Pays **100** credits in
  Act 1 terms (256 in Act 3, the act factor 1.6² = 2.56); the typical haul counts it at the
  secondary's 50 %. It has **no radio line** (proposal): the act end's five lines hold the radio,
  and the debrief's row shows it.

## Layout

Normal pace for the approach, slowing toward the hollow: **160 px/s** on the rim, **170** down the
inner wall, **150** over the dune sea, **140** in the lime hollow (each change eased over 1 s),
then the arena eases down to **20 px/s** over its first 2 s, t=105–107 (the boss spec's crawl; H.7a:
the arena's `ramp`). The level starts with the usual 5 s launch from the bottom edge over the rim's
CDF line (H.7g). Rook flies
from the escort slot (M17 = a); `--escort none` flies the level without him.

**The arena crawls; the level clock halts** (round 36): the arena section starts at
**t=105** with the boss warning, the bar, the arrival and the boss checkpoint in one moment (Level
14's rule). Its ground is a **seamless basin tile** that keeps crawling at 20 px/s on real seconds
for the whole fight; the level clock halts at the arena section's end (t=245) while the Colossus
lives, and a kill jumps it there (Level 07's and 14's `arenaJump`). The tile hides both: a long
fight (hard, a weak loadout) keeps crawling past t=245 without running out of ground, and the
kill's jump moves no visible ground. The craters the mouths leave scroll on with the tile. The
settling-dust section's settled floor starts at the next 512 px seam (H.6; the game lays it, with
the collapsed crater and the furrow at the kill). In the data the arena is a section of speed 20 with
`arena: true` and a `ramp`; it crawls because the level's boss breaches (content's `ColossusRules`).

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Hellas Rim | 0–22 | 0–3,520 | 160 | light | `ground`: the rim's edge, the CDF line held at the end of the retreat (emplacements, burnt-out walkers and crawlers, scorched pads), then the first slope of the inner wall. `low-air`: dust streaks off the rim. | The launch over the line; popcorn from t=6. |
| 2. Inner Wall | 22–50 | 3,520–8,275 | 170 | light | `ground`: terraced slopes falling into the basin, landslide fans of red sand, a CDF hauler's wreck (the turret nest on it), the survey crawler under a slide on a tread (the secret). `low-air`: thin dust. | Burrowers on the dune fans, the first nest, the Mantis pincer. |
| 3. Dune Sea | 50–78 | 8,275–12,485 | 150 | medium | `ground`: the basin floor, long dune crests, old breach craters filling with sand. **The ridge pass** (t≈64–72): the Colossus's back runs under the dunes from the right edge to the left across the upper half, a travelling ridge with a dust haze. `low-air`: ochre dust banks, the dust devils' trails. | Dust Devils, the first rear ambush; the ridge pass teaches the arena's read. |
| 4. Lime Hollow | 78–105 | 12,485–16,270 | 140 | heavy | `ground`: the basin's lowest point, a lime-veined crust cracked in rings round the centre, Vrell growths, sinkholes. `low-air`: dust haze lit lime from below. | The Herald with escorts, the last nests, two rear waves; the Choir's song, then the screen clears. |
| 5. Dust Colossus | 105–245 | 16,270–19,190 | 20 | heavy | `ground`: the hollow's floor as the seamless tile (512 px); the breaches' bulges, craters and the ridge; the debris rocks (phases 2–3). | The boss fight (≈ 102 s at medium, its measured par); the warning, the arrival and the checkpoint at t=105. |
| 6. Settling Dust | 245–277 | 19,190–19,988 | 25 | clear | `ground`: the collapsed crater, the ridge's sunken furrow, the dust falling out of the air (the settled floor from the next 512 px seam, H.6). `high-air`: far off over the basin, Vrell shapes and spore trails lifting off and climbing out of the frame (scenery, ≤ 40 %). | Release; no enemies, no music; the act end's lines. |
<!-- /data -->

About 105 s of approach (16,260 px), the fight, and 32 s of settling dust. Motion budget: the
strong elements are the dust on the rim and the slides in sections 1–2, the ridge pass in section
3 and the lime glow and sinkholes in section 4; in the arena only the breaches, the ridge and the
debris move (on real seconds).

The backdrop's look comes from the chosen Mars scenes of round 02/03
([art direction](../../../art-direction/README.md)) and the Hellas basin sub-location of
[Mars](../../../world/mars/README.md); step A-mars proposes the Hellas rim/basin mood still tonight.

## Waves

Formation names from the [formation vocabulary](../../../enemies/README.md). The returning
`medium` and larger units get the act HP factor (reference DPS 100 at L21, M18 = a; the ratio to
their first level's): Burrower 44 → 58, Dust Devil 52 → 59, Shellback ≈ 126 (its round-40 rework:
106 at Level 17; the earlier "143 + 29" is stale), Choir Herald 60 → 71, Mantis 26 → 50, Wraith
16 → 24, Spore Bomber 24 → 75. The `small` ones take the beam era's ×2 (`beam_era_hp`, from Level 18)
and the Needler and the Stinger also `small_act_hp` (Level 21 joined its levels in step PH2): Needler
24, Stinger 36, Spine Turret 16, Polyp Mortar 20; the `tiny` ones (Dust Kite, Skitter, Mote) keep theirs.

- **Round 43, H.1–H.3** ("tougher, not more", count-neutral as Level 20; step PH2): the Kite V-wings
  (t=9.5, 30) fly as Kite swarms that loop on the screen (Level 15's fix), the t=64 stream as a long
  loop that holds the ridge pass; four Kite streams and snakes became firing groups (Needler line
  abreasts of 4 at t=20 and 54, Stinger streams of 4 at t=26.5 and 91: in the beam a 1 HP Kite dies in
  a tick, a 24/36 HP Needler or Stinger stays and fires); the t=101 Stinger V-wing is dropped (H.3: the
  screen clears while the Choir sings, no incoming box), its four folded into the t=16.5 Needler line
  (3 → 4), the t=26.5 stream and the t=44 V-wing (4 → 5). Kites 35 → 20, Needlers 11 → 20, Stingers
  8 → 13: **96 waves' units + 10 ground targets = 106 at medium**. Hard: the firing groups one smaller,
  a third Wraith in each ambush, the centre turret pair; easy: the formation lever and the t=61
  Needler V-wing. Step H4 measured the floor and the gaps (the level report, the plan's fit, seed 2185): at medium
  **1.78 enemies on the screen** (the Act 2+ floor asks 2.5) and 20 shots a minute (≥ 10), **5 stretches over 2 s**
  (at most 3): the settling dust, the pre-boss pause and the ridge pass as planned, and 10.5–13.2 s and 61.9–64.3 s
  (the beam kills the t=9.5 and t=64 Kite swarms at once); hard 2.77 and 3 gaps (ok); easy 3 gaps over 3 s against
  the Act 1 rule's 2. Levels 17–20 fly 197–226 units at 2.6–3.7 on the screen; the count-neutral 106 is too thin
  for the beam. H.1 allows more when the floor fails: the rework was round 44's Q.1.
- **Round 44, Q.1** (the waves reworked to the Act 2+ rules, step L21R): the anchors stay at their times; between
  every two approved waves (outside the ridge pass and the pre-boss pause) a firing pair: 22 Needler line abreasts and
  Stinger V-wings of 2 (the beam era's HP; a single unit on hard, the t=92.75 and t=95.75 pairs not on hard: the
  third Wraiths and the centre turrets are there) and a Stinger V-wing of 3 at t=62.5; the approved Kite and Skitter
  streams stay the popcorn; the Kite swarms at t=9.5 and t=30 fly under a Needler ring that holds over them (`circle`, 6 and 5 Needlers, hold 8 s,
  Level 16's), and the t=64 swarm with a slow Stinger stream (4, 1.6 s apart; hard 2) that holds the ridge pass (no
  wave enters after it until t=70.5). **158 waves' units + 10 ground targets = 168 at medium** (easy 143, hard 152;
  ≈ 101 a minute). Measured with the level report (the plan's fit): medium **2.55 on the screen** (≥ 2.5) and 23 shots
  a minute, **2 stretches over 2 s** (the pre-boss pause, 5.0 s, and the settling dust); easy 1.50 and 2 stretches over
  3 s (the pre-boss pause and the settling dust; the ridge pass 2.0 s); hard 3.02 and 2 over 2 s. The beam era's small
  units live 1.21 × Level 10's on the screen. `PacingTest` and `ActFloorTest` assert the level again.
- **Density and pacing** (§1.7, round 36; superseded by round 44's Q.1 above, 168): **107 enemies at medium** (the waves' 97 and
  the 10 turrets and mortars) over the 97 s from t=8 to the arena, **≈ 66 a minute**: the act's
  minimum of 55 plus what the 3 s rule needs, about Level 14's 67.6 for the other act finale, and
  inside the act's band of 60–72 ([Act 3](../README.md#density-and-threat-profile), which brought
  Levels 15–17 down from their drafts' 85–90 to 65–70). More popcorn would put more of the level's
  credits in it and make the approach a second fight before a long boss; a wave every 3–3.5 s of 3–4 small units keeps the
  screen busy without it. One longer pause, the ridge pass (no wave enters from t=64 to t=70.5;
  the Kites of t=64 are still on the screen), as Level 14's root-burst window.
- **Directions** ("all", the roster): two side pincers and one side stream with the LEFT/RIGHT edge
  warnings 3 s ahead, two unwarned Wraith rear ambushes (re-entries) and a rear Mote stream with the BOTTOM warning 3 s
  ahead; Rook's flank and rear barks (his AI, not scripted; none for the Wraiths). Front 75 % (≥ 60 %).
- **Burrowers** return in three fields (their spec's "may return in L20–L21"), only over sand: the
  dune fans of the inner wall, the dune sea's edge and the hollow's crust between the nests. The
  hollow's crust is the spec's "dust or regolith"; the nests' rocks are no-surface zones (Level
  15's proposal).

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (alternating edges) | Popcorn over the rim line |
| 8 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step L21R (round 44 Q.1): a firing pair over the rim line |
| 9.5 | 1 | circle | [Needler](../../../enemies/air/needler/README.md) | 6 | front | Round 44 Q.1 (step L21R): a firing ring held over the Kite swarm (the beam takes the Kites at once) |
| 9.5 | 1 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | H.1: the approved V-wing as a swarm that loops on the screen |
| 11.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step L21R: a firing pair: the rim's screen held (Act 2+ rule) |
| 13 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (left) | |
| 14.75 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step L21R: a firing pair |
| 16.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Over the wrecks of the retreat |
| 18.25 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step L21R: a firing pair between the Needler lines |
| 20 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Down the first slope; a firing group in place of the approved Kite snake (H.1) |
| 21.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step L21R: a firing pair down the first slope |
| 23 | 2 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 3 | front | On the dune fans; act HP factor |
| 24.75 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step L21R: a firing pair over the dune fans |
| 26.5 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (alternating edges) | A firing group in place of the approved Kite stream (H.1) |
| 28.25 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step L21R: a firing pair |
| 30 | 2 | circle | [Needler](../../../enemies/air/needler/README.md) | 5 | front | Round 44 Q.1 (step L21R): a firing ring held over the Kite swarm |
| 30 | 2 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | H.1: the approved V-wing as a looping swarm |
| 31.75 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step L21R: a firing pair before the pincer |
| 33.5 | 2 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | 1 per side; edge warnings; act HP factor |
| 35.25 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step L21R: a firing pair |
| 37 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (right) | Over the hauler's nest |
| 38.75 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step L21R: a firing pair past the hauler's nest |
| 40.5 | 2 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 4 | front | Below the survey crawler's tread |
| 42.25 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step L21R: a firing pair over the survey crawler's tread |
| 44 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front | The leader drops an overdrive |
| 47 | 2 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | sides | Popcorn from the sides; edge warnings |
| 48.75 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step L21R: a firing pair onto the dune sea |
| 50.5 | 3 | pack | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 2 | front | Roaming the dune sea; act HP factor |
| 52.25 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step L21R: a firing pair |
| 54 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | A firing group in place of the approved Kite snake (H.1) |
| 55.75 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step L21R: a firing pair before the crest |
| 57.5 | 3 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | With the dune-crest mortars; act HP factor |
| 59.25 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step L21R: a firing pair over the crest mortars |
| 61 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Cloaked; no edge warning or rear bark (a Wraith's re-entry); act HP factor; easy: a Needler V-wing from the front instead |
| 61 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Easy only: instead of the rear Wraiths (Level 14's rule) |
| 62.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | Step L21R: a firing group into the ridge pass (the screen held to t=64) |
| 64 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (alternating edges) | Round 44 Q.1 (step L21R): a slow firing stream that holds the ridge pass with the Kites (the beam takes the Kites at once) |
| 64 | 3 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | Over the ridge pass (H.1: a looping swarm); no wave until t=70.5 |
| 70.5 | 3 | convoy | [Shellback](../../../enemies/ground/shellback/README.md) | 2 | front | Lumbering after the ridge; act HP factor |
| 72.25 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step L21R: a firing pair after the ridge |
| 74 | 3 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | 1 per side; edge warnings |
| 75.75 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step L21R: a firing pair |
| 77.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 79 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step L21R: a firing pair into the hollow |
| 80.5 | 4 | pincer | [Needler](../../../enemies/air/needler/README.md) | 4 | sides | 2 per side, hold 4 s; edge warnings |
| 82.25 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step L21R: a firing pair |
| 84 | 4 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 4 | front | Between the hollow's nests |
| 85.75 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step L21R: a firing pair between the hollow's nests |
| 87.5 | 4 | carrier + escorts | [Choir Herald](../../../enemies/air/choir-herald/README.md) + [Needler](../../../enemies/air/needler/README.md) | 1 + 4 | front | The escorts in `orbit` under its rings; act HP factor |
| 91 | 4 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (right) | A firing group in place of the approved Kite snake (H.1) |
| 92.75 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Step L21R: a firing pair over the lip mortars (not on hard: the third Wraiths and the centre turrets) |
| 94 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | No edge warning or rear bark (a Wraith's re-entry) |
| 95.75 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | front | Step L21R: a firing pair, the last from the front (not on hard) |
| 97.5 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | rear | The last of Level 20's pursuers catching up; BOTTOM warning; the last wave before the Colossus (H.3: the t=101 V-wing dropped) |
| 105 | 5 | boss | [Dust Colossus](../../../enemies/bosses/dust-colossus/README.md) | 1 | the edges (breach points) | At the arena's start: the warning, the bar and the checkpoint; the breaches as in its spec; no other waves in the arena |

Totals: Dust Kite 20 · Needler 53 · Stinger 42 · Skitter 9 · Burrower 11 · Mantis 4 · Dust Devil 2 · Spore Bomber 2 · Wraith 4 · Shellback 2 · Choir Herald 1 · Mote Swarm 8 · Dust Colossus 1.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 2 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on the CDF hauler's wreck (t≈36) | Bounty 12 each |
| 2 | Landslide over the Hellas survey crawler on a tread (`ground` trigger, 20 HP, t≈44, x 80) | The secret: frees a crate worth 100 (256) |
| 3 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) ×2 on a dune crest (t≈57) | Bounty 15 each; with the Spore Bombers: area denial before the ridge pass |
| 4 | Spine Turret nest ×3 on the lime crust (t≈84; hard: + a pair at the hollow's centre, t≈99); hard ×5 | Bounty; the t=84 Burrowers surface between them |
| 4 | Polyp Mortar ×2 on the hollow's lip (t≈92) | Bounty |
<!-- /data -->

## Hazards

- **The ridge pass** (t≈64–72, scenery; proposal): the Colossus's back runs under the dune sea from
  the right edge to the left across the upper half of the field as a travelling dune ridge with a
  dust haze and a low rumble (no popping), a light screen shake within the player's shake setting.
  It is `buried` (nothing hits it, no contact) and it attacks nothing: it teaches the read the
  arena needs (the body under the sand, the ridge running toward the next breach). Drawn by the
  game, as the boss spec's ridge.
- **In the arena**: the suction (45 px/s within 120 px, falling off to 0 at 360 px; easy 30, hard
  55), the debris rocks (hazards, not enemies, not in the density), the bite circle and phase 3's
  marker, all as in the [boss spec](../../../enemies/bosses/dust-colossus/README.md#suction-and-debris).
- No terrain collision (the rim and the inner wall are scenery; walls are Level 16's).

## Secrets and pickups

- **Hellas survey crawler** (t≈44, x≈80, on a ledge of the inner wall): a colonial deep-survey
  crawler buried to its cab by a landslide. The slide is a trigger with **20 HP** (Levels 17–18's
  triggers), **28 on hard** (its `hard: {hp}`, round 44 Q.5); clearing it uncovers a crate of **100** credits in Act 1 terms (256 in Act 3). The
  Salvage scanner's glint shows on the slide. Its line, Ares Control: "*Our survey crawler, missing
  since spring. Take what's aboard.*"
- **Overdrive**: dropped by the t=44 Stinger leader.
- **Armour patches** ×2: a CDF supply drone (Level 13's, [allies](../../../allies/README.md)) flies
  in from the bottom edge at t≈86 (between the rear waves), releases its patch at t=89 at x 240,
  380 px above the bottom edge and leaves the same way (Level 14's `drops`); and the boss's phase 3
  drops one (its spec).
- Shield cells follow the normal drop table.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), with the HUD's **1 s rule**
(`RadioTimelineTest`), checked with Level 15's text model: a timed line holds the radio for the
longer of its voice and its text (5 s for the last page plus 3 s for each page before it, typed at
30 characters a second), then 0.4 s of silence; a one-page line holds ≈ 7 s, a two-page line
≈ 11 s. No timed line starts late; the voiced check comes with the data. Rook's lines carry
`requires: escort`; his `{side}` line is Level 08's mechanism; his one dry joke of the level is the
phase-2 line, a callback to his Level 15 joke about moles.

Speakers: Okafor, Varga, Rook and **Ares Control** (Level 15's flight controller, the colony's one
voice across the act, a man; [Act 3 voice](../README.md#voice), cast in round 36: S.6 = b). The Choir sings before the boss (the rule that Choir
transmissions precede a Vrell boss) and **speaks** as it dies (M14 = a; the boss spec owns the
words).

The arena's clock runs (the arena crawls), so the t=111 line is timed; the phase lines are
`boss-phase` events queued as `PHASE` lines (Level 14's step H7b: they wait for a gap, stale after
20 s, dropped at the kill). **The five death lines are queued as `CLOSING` lines in order**
(round 36): they never go stale, and the debrief waits for the last one (Level 14's
closing rule). The Choir's words wait **4.5 s** after the kill (`delay: 4.5`, round 45 B.a: the death roar drowned
them), the lines after them queue behind them. Text model from the kill (K): the Choir from K+4.5 to ≈ K+10.5 (its
subtitle 5.6 s), Varga to ≈ K+17.2, Okafor to ≈ K+23.8, Ares Control to ≈ K+31.2, Okafor's closing line to ≈ K+38.5;
the settling-dust section (32 s, sized to the lines before the delay) runs on under them and the debrief waits for
the last.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, no more running. Every Vrell on Mars came out of that basin. Today we go in after them." |
| t=13 | Rook | "On your {side}, Lancer. Let's finish this." |
| t=21 | Generic colonial (Ares Control) | "Ares Control, Aegis. All of Mars is listening today." |
| t=37 | Varga | "Burrowers on the wall. And the song is coming from the floor." |
| t=53 | Okafor | "Basin floor ahead. Keep your speed up and your eyes on the sand." |
| t=65.5 | Varga | "That ridge isn't a dune, Lancer. It's moving under the sand." |
| t=74 | Okafor | "Whatever it is, it's heading for the hollow. So are we." |
| t=84 | Generic colonial (Ares Control) | "Our seismographs just went off the scale, Aegis." |
| t=97.5 | The Choir (distorted) | "[the Choir sings]" |
| t=111 (t=111 (the first tell; the arena's clock runs)) | Varga | "It's coming up! Hit the lime in its throat when it opens." |
| Phase 2 (`boss-phase` Hunting) | Okafor | "It's hunting you now, Lancer. Keep moving and mind the pull." |
| Phase 2, queued after Okafor's line | Rook | "Remember the moles, Lancer? I take it back." |
| Phase 3 (`boss-phase` Breaching anywhere) | Varga | "It's under you! Move when the ground cracks!" |
| Colossus destroyed (`boss-destroyed`, the chain's start), held 4.5 s (`delay`, round 45 B.a: past the death roar): the Choir speaks, with its layering on speech | The Choir (distorted) | "…many… we are many… we are going…" |
| After the chain and the flash (`CLOSING`) | Varga | "Commander… that was not singing. Those were words." |
| Then (`CLOSING`) | Okafor | "I heard it, Doctor. Log everything." |
| Then (`CLOSING`) | Generic colonial (Ares Control) | "Clear sky over every dome, Aegis. Dinner's on Ares Landing." |
| Level end (the closing line, `CLOSING`) | Okafor | "Mars holds. They're pulling off the planet. Come home, Aegis." |
<!-- /data -->

Timeline check (text): t=1 closes ≈ 12.4, t=13 ≈ 20.4, t=21 ≈ 28.1, t=37 ≈ 44.4; the secret's line
has the gap from ≈ 44.4 to Okafor at t=53 (the crawler is in reach at t≈44–49; an event line waits
at most 6 s); t=53 closes ≈ 60.9 (the t=61 Wraiths get no bark or warning since 2026-10-21), t=65.5
≈ 73.0, t=74 ≈ 81.5 (the t=80.5 pincer's flank bark, warned at t=77.5, waits ≈ 4 s), t=84 ≈ 91.0
(the t=94 Wraiths: no bark), the Choir's stage sound (≈ 3.9 s, subtitle 5.6 s) closes
≈ 103.5 before the warning at t=105; t=111 closes ≈ 118.5 before the first breach opens (the first
tell starts about t=111). The t=33.5 pincer's flank bark (warned at t=30.5) falls in the free gap
after Ares Control. The Mote stream's first entry (t=97.5) keeps its BOTTOM warning.

**Story notes.** The Helix beat is not in this level (Levels 16–19 carry it, M13 = a); Okafor's
"Log everything" is the boss spec's line. The colony's resentment of Earth (Level 15's beat) is
paid off by Ares Control's dinner line, an echo of his secondary line in Level 17 ("Earth can stay
for dinner").
Varga tracking the Vrell fleet to Europa is the act outro's (the [Act 3 README](../README.md)).

## Boss / mid-boss

Act boss: [Dust Colossus](../../../enemies/bosses/dust-colossus/README.md). Its breaches, phases,
HP and attacks live in the spec. Arena notes for this level:

- The arena starts at t=105 with the boss warning (track 22), the long bar `DUST COLOSSUS`, the
  arrival and the boss checkpoint in one moment; the Choir has sung at t=97.5, so the first tell
  (the front breach, phase 1) starts about t=111, the spec's ≈ 6 s arrival.
- The arena crawls at 20 px/s on a seamless tile (see *Layout*); the breach points are screen
  points, and the bulges, craters and the ridge scroll with the tile.
- The breaches get **no** edge warnings (the boss never warns, 2026-10-21: the bulge and the tell are the cue);
  the bottom breach counts as a rear wave for Rook (his Trail formation).
- **Rook and the autopilot** must leave the bite circle before the snap, step out of phase 3's
  marker and work against the pull (the spec's last lesson), or the tests cannot finish the fight.
  Rook takes the spec's damage like the ship (`heavy` 10 at the snap or the eruption).
- The Airstrike hits the mouth and the spine vents (ground targets) only while they are up,
  capped at 150 per part per strike by the [specials](../../../player/specials/README.md) rules;
  a strike on a buried body does nothing (M7 = a).
- The boss checkpoint is recorded at the arrival, t=105, on easy and medium, per the
  [retry](../../../systems/retry/README.md) rules; there is none on hard.
- The fight's length is **measured** (the spec; step H4, `DustColossusTest` and the level report's `colossus` line):
  the autopilot on the balance plan's Level 21 fit over five seeds lasts **101.7 s at medium** (every seed
  100.9–103.1 s, inside 90–180 s) at the spec's HP, so the **par is 102 s** (draft 130 s); easy 87 s, hard 142 s, all
  won; no bite and no eruption reached the ship, at most 12 shield and armour lost in a fight.
- **Bounty: 325 in Act 1 terms** (mouth 289, spine vents 12 each), paid **616 = 22.7 %** of the
  budget at this level's measured scale 0.74 (H.5: the share kept while the perfect run stays ≥ 1.4 ×;
  320 at the paper 0.75, 300 at 0.80 before), instead of the spec's 790 (30 %) at an assumed scale of 0.40: see
  *Credit budget*.

## Music & ambience

The Act 3 B theme (track 9 in the [track list](../../../audio/music/README.md#track-list), the
"Grey Signal", D minor dub techno, round 36's S.1 = b) for the approach: the rim's wind alone in
section 1, the **base stem from section 2**, the **full mix from section 3** (the ridge pass).
**Vorne's motif layer stays muted** here (round 36): the Colossus is pure Vrell, and the
faint motif marks the human machines of Levels 18–19 until it returns in Act 4 (L27–28). At the
arena start, t=105, the boss warning (track 22) plays and the Vrell boss theme "The Choir Descends"
(track 18) starts after it and runs through the fight, fading at the kill. The settling-dust
section has **no music**, and there is no mid-level jingle. The debrief plays the
`mission-complete` jingle as every level does, and the act-complete fanfare (track 24) plays under
the first outro page (Level 14's H6 = a order, not over the settling dust).

Ambience: the Martian dust wind (`mars-dust`,
[ambience per setting](../../../audio/sfx/README.md#ambience-per-setting)) throughout; a low seismic
rumble bed from the ridge pass (t≈64), swelling through the hollow into the fight; at the kill the
rumble cuts with the death chain and the wind drops to a calm over the settling dust. New sounds
this level needs (the act's audio step lists them): the ridge pass's travelling rumble, the
breach's bulge and eruption, the suction's draw, the bite's snap, the death's collapse, the Choir's
speech layering (no bubbly or popping sounds).

## Act end

Level 21 ends Act 3 and follows Level 14's order (H6 = a, the Act 1 order): the Colossus's death
(the chain, the white flash of an act boss, the credit shower, the Choir's first words) → the
**settling-dust section** (32 s: the crater collapses, the dust falls out of the air, the Vrell
lift off from the far basin on `high-air`; no enemies, no music; the four `CLOSING` lines) → the
**debrief with the act summary** (`mission-complete` jingle, the tally, the summary
`ACT III COMPLETE · RED DUST`) → the **act outro**, four voiced pages with their images (the data
and the texts are the [Act 3 README](../README.md)'s; they carry the story beat "the Vrell pull back
toward Jupiter's moons and Varga tracks their fleet to Europa") with `act-complete` under page 1 →
the hangar.

There is no Level 22 yet: the release's end (M6 part B, Q1 = a) moves from Level 14 to Level 21 in
M7 part H: the outro is followed by the end card, the name entry and the credits, then the main
menu; Continue opens the hangar before L22, which reads `ACT IV - DEEP WATER: COMING SOON` and
cannot launch (Level 14's `ACT III - RED DUST: COMING SOON` goes). `--act-end` works unchanged for
this level. Since M8 part B (Level 22 built, Act 3's `next_act` removed) the outro leads to Act 4's title card and
intro, then Level 22's briefing and hangar; the release's end moves to Level 28 in part H.

## Credit budget

Budget(21) = 700 × 1.07²⁰ ≈ **2,709**, the typical haul's target
([economy](../../../systems/economy/README.md#per-level-budget)): 60 % of the air kills, 80 % of
the ground units and targets, half the secret and the secondary, the boss at 100 %. Amounts in
**Act 1 terms** (Dust Kite 4, Skitter 5, Needler 12, Stinger 15, Mote 2, Spore Bomber 25, Mantis
30, Wraith 30, Choir Herald 30, Burrower 24, Dust Devil 26, Shellback 48 whole, Spine Turret 12,
Polyp Mortar 15, the boss 480, the crate 100, the secondary 100), each paid × the act factor
**2.56**, bounties also × the level's scale. With round 44's rework (Q.1: 168 enemies, most of them firing pairs) a
**`bounty_scale` of 0.50** (step L21R's measured fit, the level report; step H4's 0.74 for the 106, step PH2's paper
value 0.75) puts the typical haul on the budget (+1.1 %), the perfect run at 1.48 ×; the Colossus pays 614 (480 × 2.56
× 0.50, H.5's 22.7 %).

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Dust Kite 20 × 4 + Needler 53 × 12 + Stinger 42 × 15 + Skitter 9 × 5 + Burrower 11 × 24 + Mantis 4 × 30 + Dust Devil 2 × 26 + Spore Bomber 2 × 25 + Wraith 4 × 30 + Shellback 2 × 52 by parts (48 whole) + Choir Herald 1 × 30 + Mote Swarm 8 × 2 | 2,716 | 1,729 |
| Ground targets: Spine Turret 6 × 12 + Polyp Mortar 4 × 15 | 166 | 133 |
| Boss: Dust Colossus (mouth 444 + vents 3 × 12) | 613 | 613 |
| Secret: survey crawler (hidden crate, 4% of budget) | 256 | 128 |
| Secondary: clear the basin | 256 | 128 |
| **Total** (bounty scale 0.5) | **4,007** | **2,731** |
| Budget(n) = the typical haul's target; typical +1 %, perfect 1.48 × budget | | 2,709 |
<!-- /data -->

**Why the boss pays 22.7 %, not 30 %** (accepted in round 36, Q.21a; as Level 14's 26.8 %): a perfect
run must earn at least 1.4 × the budget (`TypicalHaulTest`), and the boss pays the same in both
columns, so every credit it takes off the kills narrows the gap. With this level's mix the bound
holds up to a share of about 24.8 %; at the spec's 30 % (scale ≈ 0.71, the boss ≈ 445 in Act 1
terms) the perfect run reaches only ≈ 1.38 ×. 22.7 % is inside the act boss's 20–40 % band
(`BalanceTest`). To raise it, the level would need a larger secret or more air bounty (e.g. Stinger
and Mantis waves for Kites), not more density: the bound depends on the mix, not the count. The
scale (0.50, round 44; 0.74 before the rework) is at the top of M12's 0.3–0.5, as Level 17's 0.55: the
density rests on cheap firing pairs and the boss's share is small.

**Balance plan** (part A's Act 3 plan, M18 = a): the level expects `anti-ground` and wing or rear
guns (the edge mouths face the ship's sides and tail) and is flyable with front guns and homing
alone, at a longer fight; the fight's measurement uses the plan's Level 21 fit.

## Difficulty notes

- **Easy**: Burrower fields one smaller (the formation lever) and their easy hooks; the t=61
  Wraiths enter from the front as a Needler V-wing (Level 14's rule: a Wraith flies only the rear
  ambush), the t=94 pair and the Mote stream stay as the level's rear waves; the boss ×0.75 and
  its easy hooks (the pull 30 px/s, the bite's tell 1.0 s, phase 3's marker 1.8 s); the boss
  checkpoint.
- **Hard**: the Burrowers' hard hooks; a Spine Turret pair at the hollow's centre (t≈99); a third
  Wraith in each rear ambush; the survey crawler's slide 28 HP; the boss ×1.3 and its hard hooks
  (rings of 16, the bite's tell 0.6 s, the pull 55 px/s, phase 3's marker 1.2 s, three spiral
  arms). There is no boss checkpoint on hard.

## Concept art

Production art of M7 part H's round 44 (step ART-H1; the mood from the Hellas mood still in
[mars](../../../world/mars/README.md), round 36 MA.4, in Level 20's style), accepted in round 44 (2026-10-20). Prompts:
[concept/prompts.md](concept/prompts.md). The arena's tile repeats every 512 px and section 6's settled floor
starts at the next 512 px seam, its seam piece, the collapsed crater and the furrow laid by the game at the kill
(round 43, H.6); the ridge pass is drawn by the game from Level 20's `colossus-ridge` sprites.

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r44-a.png](concept/backdrop-final-r44-a.png) | Level 21 backdrop final (`tools/art/backdrop_l21.py`): the CDF line on the rim (pads, berm, gun pits) and the retreat's wrecks down the basin wall, the inner wall's terraces with landslide fans of red sand and the CDF hauler's wreck, the dune sea with old breach craters and the mortars' crest, the lime hollow's crust cracked in rings and lit lime from below with sinkholes and Vrell growth, the arena's 512 px basin floor, and after a kill at t=235 the settled floor from the next seam with the collapsed crater, the furrow and the Vrell lifting off on high-air; the play field at twenty times (no units), the pieces (sheet) | chosen (round 44, F.a: approved as final) |
| [concept/backdrop-final-r44-a.gif](concept/backdrop-final-r44-a.gif) | Level 21 backdrop final: the approach, the arena's start and the settling dust after the kill flown at 0.5x, 10 fps | chosen (round 44, F.a: approved as final) |
| [concept/l21-props-final-r44-a.png](concept/l21-props-final-r44-a.png) | Level 21 props final (`tools/art/l21_props.py`): the survey crawler under its slide (buried, hit, cleared with the crate) and on its ledge at t=44, the CDF hauler's wreck with the turret nest's deck spots, the ridge pass over the dune sea from Level 20's ridge sprites (sheet) | chosen (round 44, F.b: approved as final) |
| [concept/l21-props-final-r44-a.gif](concept/l21-props-final-r44-a.gif) | Level 21 props final: the ridge pass at t=63-74 and the crawler's three states on its ledge, 10 fps | chosen (round 44, F.b: approved as final) |
| [concept/level-21-capture-r44-a-p1.mp4](concept/level-21-capture-r44-a-p1.mp4) | Level 21, part 1 of 3 (the approach): the rim and the inner wall's slides, the hauler's wreck nest (0:38), the Mantis pincer under the LEFT/RIGHT warning with the survey crawler on its tread at the left edge under the warning box (0:46; not freed: SECRETS 0 / 1), the dune crest mortars (0:59), the ridge pass with Varga's "That ridge isn't a dune" (1:05–1:13). 78.5 s | chosen (round 44, K.a: accepted) |
| [concept/level-21-capture-r44-a-p2.mp4](concept/level-21-capture-r44-a-p2.mp4) | Part 2 of 3 (the hollow and phases 1–2): the lime hollow, the Choir singing (0:20), the "DUST COLOSSUS APPROACHING" warning (0:27), Varga's t=111 line, the front breach (its mouth partly under the boss bar), the bite tell (teeth flash white, 0:44; no bite circle is drawn), the LEFT edge warning and the left breach with the gullet open (0:47–0:55), phase 2 at 0:53 (Okafor, then Rook's moles line), hunting breaches from the rear and the left with their warnings, the spine vents firing on the ridge (1:04), debris rocks, suction streaks (faint). 91 s | proposed (round 44, K.b: change requested: the vents become organs on the Colossus's spine and a death throe is added; to be re-recorded after steps SIM-K, ART-K, GK, L21R) |
| [concept/level-21-capture-r44-a-p3.mp4](concept/level-21-capture-r44-a-p3.mp4) | Part 3 of 3 (**re-recorded after GFX3**: a new run of the same fit from a fresh snapshot, the same outcome): the rest of phase 2, phase 3 at ≈ 0:25 (Varga's "It's under you!"), **Rook shot down** ("Punching out! Give 'em hell!", 0:30), the eruption marker (a pale lime ring about 90 px across round a cracked bulge, under the ship, 0:33.5–0:35) and one eruption of the mouth (0:35), the kill at ≈ 0:37.6 (boss time 1:40 against par 1:42): the death frames (the shudder with lime ichor, the rearing, the sinking, 0:37.6–0:39.9), the ridge's split crackling lime-yellow (0:39.8), the sinkhole and the furrow fading in (0:40) with the white flash and pale chunks on top of them (0:40.3), Varga's phase-3 line shown a second time over the kill (0:37–0:42, after Rook's line cut it), the Choir's words (0:44), the sinkhole and furrow scrolling off (≈ 0:52), the settled floor (from ≈ 0:56, about 26 s before the debrief) with Varga's "that was not singing", Ares Control (1:04) and Okafor's closing line (1:16), the debrief (1:22–1:37: 100 / 107, A, MET). 97.5 s | proposed (round 44, K.c: change requested: the crawl halts while the Colossus is out of the ground; to be re-recorded after steps SIM-K, GK, L21R) |
| [concept/level-21-capture-r44-a.png](concept/level-21-capture-r44-a.png) | 24-tile sheet of the three parts (HUD left panel + field, captioned part + time; the p3 tiles from the re-recorded part 3) + 1:1 crops: the gullet open, the bite tell, the spine vents firing, the front mouth under the boss bar (from part 2), and from the new part 3 the eruption marker, the suction streaks (and 2×), four death frames, the sinkhole with the furrow, debris rocks | proposed (round 44, K.b/K.c: the sheet of the clips above; re-made with them) |
| [concept/level-21-act-end-r44-a.mp4](concept/level-21-act-end-r44-a.mp4) | After the win (same run): the last of the settling dust and Okafor's closing line, the debrief tally and stamp, ACT III COMPLETE (Level 21 alone recorded: a debug run), Act 3's outro, 4 pages typed and voiced (0:38–1:41), the release end card "ACTS I-III COMPLETE … TO BE CONTINUED / ACT IV - DEEP WATER. EUROPA. COMING SOON." (1:45), then the hangar (snapshot hook, see above) with "ACT IV - DEEP WATER: COMING SOON" in its header and Mission 22's intel "NO INTEL YET: THE MISSION FOLLOWS IN A LATER RELEASE." 139 s | chosen (round 44, K.d: accepted) |
| [concept/level-21-act-end-r44-a.png](concept/level-21-act-end-r44-a.png) | 12-tile sheet of that clip (full frames) | chosen (round 44, K.d: accepted) |
| [concept/level-21-capture-r45-a-p1.mp4](concept/level-21-capture-r45-a-p1.mp4) | Round 45 check capture (step H6, autopilot with the plan's fit and Rook): part 1 of 3, the approach with the reworked waves, 103.5 s | chosen (round 45: accepted; the Choir delay and phase 1 fan follow) |
| [concept/level-21-capture-r45-a-p2.mp4](concept/level-21-capture-r45-a-p2.mp4) | Part 2 of 3, phases 1-2: the ground holds still through the fight, the spine and its vent organs, the breaches, the bite tell, 96 s | chosen (round 45: accepted; the Choir delay and phase 1 fan follow) |
| [concept/level-21-capture-r45-a-p3.mp4](concept/level-21-capture-r45-a-p3.mp4) | Part 3 of 3, phase 3 (two eruptions), the death throe, the sinkhole and the debrief, 87 s | chosen (round 45: accepted; the Choir delay and phase 1 fan follow) |
| [concept/level-21-capture-r45-a.png](concept/level-21-capture-r45-a.png) | 24-tile sheet of the three parts plus crops of the spine, a vent organ, the bite tell and six throe frames | chosen (round 45: accepted; the Choir delay and phase 1 fan follow) |
| [concept/level-21-spine-r46-b.mp4](concept/level-21-spine-r46-b.mp4) | Round 46 b in-game capture (step GSR, autopilot, medium, the plan's fit): the Colossus's spine **rising out of its ridge** — one front runs along the back from the collar to the tail over the rise, the plates coming up out of the sand bulge with sand still on them at the front and puffs off the flanks; the dive runs it back tail to head, the head last with the mouth, then the ridge fades. A left breach (straight back) and a rear breach (a slanted, curved back with the vent organs), each rise and dive at 1× then at 0.25×, 47 s | chosen |
| [concept/level-21-spine-r46-b.png](concept/level-21-spine-r46-b.png) | Sheet of that capture: the left and rear rises and dives frame by frame, 1:1 crops of the front (left rise ×3, rear rise, left dive) | chosen |

The other part H art, in the tables of the parts it belongs to (round 44, accepted in round 44 (2026-10-20)): the Colossus's
sprites in the [bosses' table](../../../enemies/bosses/README.md#concept-art), its intel silhouette in the
[hangar's](../../../ui/hangar/README.md#concept-art), the two briefing images and the Act 3 outro's four images in the
[briefing's](../../../ui/briefing/README.md#concept-art). The round's captures of the level come with the round
(`concept/level-21-*-r44-a`).

## Implementation

- [x] Level data `data.yaml` (M7 part H) with the sections, waves, ground targets, radio, budget,
      threat profile, briefing, backdrop and music; this README's tables become marked tables (step
      PH2; loaded, checked by `ColossusChecks` and mapped by `ColossusRules`, `ColossusKeysTest`; flown by
      the campaign-wide tests from step H4, `Level21Test`)
- [x] Reference DPS 100 at L21 and the Act 3 balance plan's Level 21 fit (M18) before the data;
      the act HP factor for the returning `medium`+ units listed in *Waves* (steps PH2, H4: the plan's fit flies
      the level in `Level21Test`, `TypicalHaulTest`, `BalanceTest`)
- [x] Scroll timeline and sections as in *Layout*; the arena at 20 px/s on a seamless tile on real
      seconds, the clock halting at t=245 while the boss lives and a kill jumping it there without
      a visible jump; the 32 s settling-dust section; primary `reach-end` with the arena as the
      condition, `DESTROY THE DUST COLOSSUS` (M7 part H, steps PH1/HA: `CrawlingArenaTest`, `BackdropSeamsTest`; the settled floor takes up to ≈ 27 s, round 44 Q.8 a)
- [x] The ridge pass (t≈64–72): the travelling ridge, haze, rumble and light shake; `buried`, no
      contact (steps PH2/HA/HB: the `ridge` key, `RidgeBody`, the 2 px `ScreenShake`, the ridge rumble; `Level21LooksTest`, `Level21SoundsTest`; round 44 Q.8 b)
- [x] Wave script: ≥ 55 enemies a minute at medium from t=8 to the arena (`DensityTest`; drafted
      ≈ 66), no empty screen over 3 s on every difficulty (`PacingTest`), the edge warnings for the
      side and rear waves' first entries, Burrower fields only over sand with the nests as no-surface zones (step H4:
      the density holds, 63.6 a minute; step L21R, round 44 Q.1: the rework to the Act 2+ rules, 168 at medium,
      2.55 on the screen, 2 stretches over 2 s, `PacingTest` and `ActFloorTest` assert it; no no-surface zones, round 44
      Q.6)
- [x] Boss arena per the arena notes: the checkpoint at t=105 (none on hard), the breaches' bulges and tells (no edge warnings), the bottom breach as a rear wave for Rook; Rook and the autopilot out of the bite
      circle and the marker, against the pull (steps PH1/PH3/H4: `BreachArenaTest`, `BreachHazardsTest`, `Level21Test`; step L21R: the erupted mouth is no safe spot, Q.4, `DustColossusTest`; `ColossusPlay` re-checked with SIM-K's crawl halt)
- [x] The fight measured on the plan's fit over five seeds (every seed 90–180 s), the HP tuned, the
      par set; the boss's bounty at the share `TypicalHaulTest`'s perfect-run bound allows
      (drafted 22.7 %) (step H4: 101.7 s, the spec's HP kept, par 102, 325 at the scale 0.74 = 22.7 %; step L21R after
      round 44's Q.2/Q.9/Q.11: 104.3 s, par 104, 480 at the scale 0.50; step C45 after round 45 B.b: 106.4 s, par 106)
- [x] Ground targets, the survey-crawler secret (20 HP trigger, hard 28, crate 100, the scanner's
      glint, its line), the overdrive, the supply drone's armour patch at t=89, the boss's phase-3
      patch (steps PH2/H4/ART-H1; step L21R: the hard 28 HP as the destructible's `hard: {hp}`, round 44 Q.5,
      `ColossusKeysTest`)
- [x] Secondary `kill_ratio` 65 %, judged at the level end, 100 in Act 1 terms; no radio line (step H4: `Level21Test`;
      round 44 K.h: the boss not counted, `boss_counted: false`, steps SIM-K and L21R: 168 enemies, the target 110)
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`, with Rook and
      without); the phase lines as `PHASE`, the death's lines as `CLOSING` in order with the debrief
      waiting for the last; the Choir's speech voiced with its layering; Rook's lines
      `requires: escort` (steps VOX-H, HA: `RadioTimelineLevel21Test`, `VoiceFilesTest`; the Choir's speech is one take, round 44 Q.7)
- [x] The Choir's death line held 4.5 s after the kill, the closing lines after it, the debrief waiting (round 45
      B.a: the data's `delay`, step C45, `Level21Test`; the game's hold, step G45: `RadioQueue`'s delayed add, `RadioTimelineLevel21Test`, `RadioQueueTest`)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel
      (`IntelPanelLayoutTest`); the Colossus's intel silhouette; the L3 strip's marks (the ridge
      pass and the boss: Level 14's strip draws no boss mark yet) (step HA: the boss's silhouette is on the strip
      for every boss level; the ridge pass's mark: the content `Intel` mark kind `RIDGE`, 64–72 s, step CI1, `IntelTest`; its drawing in the
      intel panel, step GI1; round 44 Q.8 d)
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their
      images (steps PH2, ART-H3, VOX-H: `BriefingLayoutTest`)
- [x] Act end: the settling dust, the debrief with `ACT III COMPLETE · RED DUST`, the four outro
      pages with `act-complete`, then the release's end moved here from Level 14 and the hangar
      before L22 reading `ACT IV - DEEP WATER: COMING SOON` (`--level 21 --act-end`;
      `OutroOnwardTest`, `DebriefActEndTest`, `CampaignRouteTest`) (steps PH2, HA, HB: `OutroOnwardTest`, `DebriefActEndTest`, `ReleaseTexts`)
- [x] Typical haul within ±5 % of budget(21) = 2,709 with the level's `bounty_scale`
      (`TypicalHaulTest`); the boss's share inside 20–40 % (`BalanceTest`) (step H4: +0.7 % at 0.74; step L21R: +1.1 % at
      0.50)
- [x] Music: track 9's base stem from section 2 and full mix from section 3, the motif layer muted,
      the warning at the arena start, the boss theme after it, none over the settling dust; the
      seismic rumble bed and its cut (step HB: the basin rumble's +4 dB swell and its cut at the kill, the wind's calm from t=245; `Level21SoundsTest`)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on
      every difficulty (`--escort none` too) (step H4: `Level21Test`, its colossus play `ColossusPlay`; three
      seeds a difficulty, no retry)
- [x] Production art (steps ART-H1-H3, drawn by the game) and the round's captures (step H5: the approach, phases 1-3, the kill, the act's end;
      round 44, K.a-K.d; the death reads as a pop, K.e; step ART-K: the spine and the death throe, still pending the user's review in round 45)

## Open questions

- Pending the user's review (step L21R, 2026-10-20): the rework's shape (the Needler rings over the t=9.5 and t=30
  Kite swarms, the Stinger stream that holds the ridge pass, the firing pairs single on hard and the last two left out
  there so the plan's fit without Rook finishes hard), 168 enemies at medium (Q.1 asked 150–170) and the `bounty_scale`
  0.50. The medium floor holds at 2.55 against 2.5: little room for later changes.

## Decisions

Entries dated before 2026-10-21 (M8's start): [decisions-archive.md](decisions-archive.md).

- 2026-10-21 (user, round 45): the Level 21 check capture accepted (B.a) with one change: the Choir's words at the Colossus's death start 4.5 s after the kill; B.b phase 1 gets the 5-way fan; C.1 hard's Act 3 economy accepted for v0.1 and revisited after the playthrough (steps C45, G45, planned).
- 2026-10-21 (step C45, round 45): B.a radio event lines take an optional `delay` (s, at least 0) and the Choir's
  `boss-destroyed` line waits 4.5 s (the closing lines queue behind it; the game's hold is step G45); B.b phase 1 fires
  the fan (the [boss's](../../../enemies/bosses/dust-colossus/README.md#decisions) numbers): the fight 106.4 s at medium,
  par 106, the level report 104.7 s at medium (100.0–107.1), the haul +1.1 % at the `bounty_scale` 0.50 (kept).
- 2026-10-21: Edge-warning rule change (user, built by step EW): the Wraith ambushes (t=61, 94) lose their BOTTOM warning and Rook's
  rear bark (a unit's own re-entry), and the Colossus's breaches get no LEFT/RIGHT/BOTTOM warning (a boss never warns); the
  waves' first entries (the Mantis pincers, the Kite stream, the Mote stream) keep theirs.
- 2026-10-21 (step CI1): the content's `Intel` marks the ridge pass on the L3 wave strip as a `RIDGE` span, the
  `ridge` key's `t` to `t + seconds` (64–72 s; round 44 Q.8 d); the Wraith ambushes carry no `warning` key (none was
  set here). The game's drawing is the next step's.
- 2026-10-21 (user, on the r46 b spine: "ok, much better indeed"; "lets finish up all the other items"): the spine rising out of its ridge accepted; Level 21's art is final (production art throughout; the r45/r46 captures and reviews chosen).
- 2026-10-21: the L3 strip's ridge-pass band drawn (step GI1, `IntelPanelLayoutTest`); every checklist item ticked and round 45 closed, so Level 21 is `implementation: done` (round 44 I.8's rule); the user's Act 3 playthrough stays an act-level item.
