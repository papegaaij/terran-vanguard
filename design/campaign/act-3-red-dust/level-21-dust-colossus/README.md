---
title: Level 21 – Dust Colossus
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/bosses/dust-colossus, ../../../enemies/ground/burrower, ../../../enemies/air/dust-kite, ../../../world/mars, ../../../systems/economy]
updated: 2026-10-13
---

# Level 21 – Dust Colossus

## Summary

The Act 3 finale. After the retreat along the Hellas rim, Aegis Wing turns round and goes down
into the basin, where every Vrell attack on Mars began. Lancer and Rook descend the inner wall,
cross the dune sea and reach the lime hollow at the basin's lowest point. There the act boss rises
out of the sand: the [Dust Colossus](../../../enemies/bosses/dust-colossus/README.md), which
breaches at the edges of the field, front, sides and rear, and then under the ship. 105 s of
approach, then the fight on a slowly crawling arena (about 130 s at medium), then 32 s of settling
dust while the Choir speaks its first words and the Vrell leave Mars. The act end follows Level 14's
order.

**Status: approved in concept round 36 (user, 2026-10-13).** Written in the overnight design
phase (2026-10-12) from the act roster and the gap analysis's recommended answers, all kept or
accepted in the round. The numbers are still hand-written tables (M19: no `data.yaml` yet); M7
part H moves them into the level's data and turns these tables into marked ones.

## Briefing

Two pages, as every level since Level 08, each one screen with its image (`BriefingLayoutTest`),
at most four lines of text so the image fits. Texts (approved in round 36):

> **Commander Okafor:** "No more running. Every Vrell attack on Mars came out of the Hellas
> basin, and Doctor Varga thinks she knows why. We go down there and end it. Ares Landing is
> counting on you, Lancer."
>
> **Dr. Varga:** "The song comes from under the basin floor. It's one organism, the size of a
> colony. It breaches where the sand bulges, from any side, even behind you. The throat glows
> lime. Bring side and rear guns."

*Images* (production art straight away, as in Act 2; [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`): Okafor's page `level-21-hellas-basin` (the basin on a CDF plot:
the rim line held at the end of the retreat, the descent route down the inner wall, the dune sea,
the lime hollow at the lowest point with seismic rings centred on it), Varga's
`level-21-colossus-scan` (her seismic cut through the basin floor: the body under the sand, the
ridge above it, a mouth breaching at an edge with the lime gullet, the bite circle and the suction
lines drawn in).

*Hangar teaser* (shop screen before L21, Rook, not voiced):

> **Rook:** "Something big under Hellas. Bring side guns, rear guns. Bring all the guns."

## Threat profile

Hand table. Directions by each wave's `from`, counted by enemy (the intel's rule); the
fight's breaches are not edge waves and are not in the numbers.

| Field | Value |
|---|---|
| Dominant layers | `ground` (Burrowers when surfaced, Dust Devils, Shellbacks, turret nests and mortars, the Colossus's mouths), `buried` (Burrowers under the sand and the Colossus's body: nothing hits them), `air` (Dust Kites, Mantises, Wraiths, the Choir Herald); `low-air` (risen Dust Devils, the dust haze) |
| Attack directions | front 75 % · sides 12 % · rear 12 % (the approach by its waves; in the fight the mouths breach from all four edges) |
| Density | 4 |
| Recommended traits | `anti-ground`, `rear`; `side` is in Varga's L3 line (three traits do not fit the intel panel, as Level 14) |
| Hazards | The Colossus's suction and the debris rocks (the arena); the ridge pass (scenery) |
| Boss / mid-boss | Act boss [Dust Colossus](../../../enemies/bosses/dust-colossus/README.md) |
| Sensor-suite detail | **none**: Hellas basin, `ground` + `air`, all directions. **L1**: + the directions, density 4, hazard "suction", the objective `DESTROY THE DUST COLOSSUS`. **L2**: + Burrower, Dust Devil, Shellback, Choir Herald, Dust Kite, Mantis, Wraith, Needler portraits; the boss's name and silhouette (`dust-colossus`). **L3**: + `anti-ground` and `rear` highlighted; the timeline strip marks the ridge pass (64–72 s) and the boss (105 s); 1 secret. |

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
then the arena eases down to **20 px/s** over 2 s from t=103 (the boss spec's crawl). The level
starts on the rim (no launch: the wing takes off from the line it held in Level 20). Rook flies
from the escort slot (M17 = a); `--escort none` flies the level without him.

**The arena crawls; the level clock halts** (round 36): the arena section starts at
**t=105** with the boss warning, the bar, the arrival and the boss checkpoint in one moment (Level
14's rule). Its ground is a **seamless basin tile** that keeps crawling at 20 px/s on real seconds
for the whole fight; the level clock halts at the arena section's end (t=245) while the Colossus
lives, and a kill jumps it there (Level 07's and 14's `arenaJump`). The tile hides both: a long
fight (hard, a weak loadout) keeps crawling past t=245 without running out of ground, and the
kill's jump moves no visible ground. The craters the mouths leave scroll on with the tile. The
settling-dust section's strip starts at the tile's seam.

| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Hellas Rim | 0–22 | 0–3,520 | 160 | light, haze | `ground`: the rim's edge, the CDF line held at the end of the retreat (emplacements, burnt-out walkers and haulers, scorched pads), then the first terraces of the inner wall. `low-air`: dust streaks off the rim. | The take-off from the line; popcorn from t=6. |
| 2. Inner Wall | 22–50 | 3,520–8,280 | 170 | light | `ground`: terraced slopes falling into the basin, landslide fans of red sand, a CDF hauler's wreck, the survey crawler under a slide on a ledge (the secret). `low-air`: thin dust. | Burrowers on the dune fans, the first nest, the Mantis pincer. |
| 3. Dune Sea | 50–78 | 8,280–12,480 | 150 | medium | `ground`: the basin floor, long dune crests, old breach craters filling with sand. **The ridge pass** (t≈64–72): the Colossus's back runs under the dunes from the right edge to the left across the upper half, a travelling ridge with a dust haze. `low-air`: dust devils' trails. | Dust Devils, the first rear ambush; the ridge pass teaches the arena's read. |
| 4. Lime Hollow | 78–105 | 12,480–16,260 | 140 | medium, dust | `ground`: the basin's lowest point, a lime-veined crust cracked in rings round the centre, Vrell growths, sinkholes. `low-air`: dust haze lit lime from below. | The Herald with escorts, the last nests, two rear waves; the Choir's song. |
| 5. Dust Colossus | 105–245 | 16,260–19,060 (then the tile) | 20 | dust | `ground`: the hollow's floor as the seamless tile; the breaches' bulges, craters and the ridge; the debris rocks (phases 2–3). | The boss fight (≈ 130 s at medium, the spec's par); the warning, the arrival and the checkpoint at t=105. |
| 6. Settling Dust | 245–277 | 19,060–19,860 | 25 | clearing | `ground`: the collapsed crater, the ridge's sunken furrow, the dust falling out of the air. `high-air`: far off over the basin, Vrell shapes and spore trails lifting off and climbing out of the frame (scenery, ≤ 40 %). | Release; no enemies, no music; the act end's lines. |

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
their first level's): Burrower 44 → 58, Dust Devil 52 → 59, Shellback body 120 → 143 and vent
24 → 29, Choir Herald 60 → 71, Mantis 26 → 50, Wraith 16 → 24, Spore Bomber 24 → 75; the `tiny` and
`small` ones keep their HP.

- **Density and pacing** (§1.7, my choice for review): **107 enemies at medium** (the waves' 97 and
  the 10 turrets and mortars) over the 97 s from t=8 to the arena, **≈ 66 a minute**: the act's
  minimum of 55 plus what the 3 s rule needs, about Level 14's 67.6 for the other act finale, and
  inside the act's band of 60–72 ([Act 3](../README.md#density-and-threat-profile), which brought
  Levels 15–17 down from their drafts' 85–90 to 65–70). More popcorn would put more of the level's
  credits in it and make the approach a second fight before a long boss; a wave every 3–3.5 s of 3–4 small units keeps the
  screen busy without it. One longer pause, the ridge pass (no wave enters from t=64 to t=70.5;
  the Kites of t=64 are still on the screen), as Level 14's root-burst window.
- **Directions** ("all", the roster): two side pincers and one side stream with the LEFT/RIGHT edge
  warnings 3 s ahead, two Wraith rear ambushes and a rear Mote stream with the BOTTOM warning 3 s
  ahead; Rook's flank and rear barks (his AI, not scripted). Front 75 % (≥ 60 %).
- **Burrowers** return in three fields (their spec's "may return in L20–L21"), only over sand: the
  dune fans of the inner wall, the dune sea's edge and the hollow's crust between the nests. The
  hollow's crust is the spec's "dust or regolith"; the nests' rocks are no-surface zones (Level
  15's proposal).

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (alternating edges) | Popcorn over the rim line |
| 9.5 | 1 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | |
| 13 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (left) | |
| 16.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Over the wrecks of the retreat |
| 20 | 1 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | Down the first terraces |
| 23 | 2 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 3 | front (buried) | On the dune fans; act HP factor (58) |
| 26.5 | 2 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | |
| 30 | 2 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | |
| 33.5 | 2 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | 1 per side; edge warnings; act HP factor (50) |
| 37 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (right) | Over the hauler's nest |
| 40.5 | 2 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 4 | front (buried) | Below the survey crawler's ledge |
| 44 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | The leader drops an overdrive |
| 47 | 2 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | sides (alternating edges) | Popcorn from the sides; edge warnings |
| 50.5 | 3 | swarm | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 2 | front | Roaming the dune sea; act HP factor (59) |
| 54 | 3 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | |
| 57.5 | 3 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | With the dune-crest mortars; act HP factor (75) |
| 61 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Cloaked; BOTTOM warning; Rook's rear bark; act HP factor (24) |
| 64 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (alternating edges) | Over the ridge pass; no wave until t=70.5 |
| 70.5 | 3 | pair | [Shellback](../../../enemies/ground/shellback/README.md) | 2 | front | Lumbering after the ridge; act HP factor (143 + 29) |
| 74 | 3 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | 1 per side; edge warnings |
| 77.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 80.5 | 4 | pincer | [Needler](../../../enemies/air/needler/README.md) | 4 | sides | 2 per side, hold 4 s; edge warnings |
| 84 | 4 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 4 | front (buried) | Between the hollow's nests |
| 87.5 | 4 | carrier + escorts | [Choir Herald](../../../enemies/air/choir-herald/README.md) + [Needler](../../../enemies/air/needler/README.md) | 1 + 4 | front | The escorts in `orbit` under its rings; act HP factor (71) |
| 91 | 4 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | |
| 94 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | BOTTOM warning; Rook's rear bark |
| 97.5 | 4 | stream | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | rear | The last of Level 20's pursuers catching up; BOTTOM warning |
| 101 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | The last wave before the Colossus |
| 105 | 5 | boss | [Dust Colossus](../../../enemies/bosses/dust-colossus/README.md) | 1 | the edges (breach points) | At the arena's start: the warning, the bar and the checkpoint; the breaches as in its spec |

Totals at medium: Dust Kite 35 · Burrower 11 · Needler 11 · Skitter 9 · Mote 8 · Stinger 8 ·
Mantis 4 · Wraith 4 · Spore Bomber 2 · Dust Devil 2 · Shellback 2 · Choir Herald 1 (97), plus the
ground targets' 6 Spine Turrets and 4 Polyp Mortars.

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 2 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on the CDF hauler's wreck (t≈36) | Bounty |
| 2 | Landslide over the Hellas survey crawler on a ledge (t≈44, x≈80) | The secret (see below) |
| 3 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) ×2 on a dune crest (t≈57) | With the Spore Bombers: area denial before the ridge pass |
| 4 | Spine Turret nest ×3 on the lime crust (t≈84) | Bounty; the t=84 Burrowers surface between them |
| 4 | Polyp Mortar ×2 on the hollow's lip (t≈92) | Bounty |
| 4 | Spine Turret pair at the hollow's centre (t≈99), **hard only** | Bounty |

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
  triggers); clearing it uncovers a crate of **100** credits in Act 1 terms (256 in Act 3). The
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
closing rule). Text model from the kill (K): the Choir from K to ≈ K+6.0 (its subtitle 5.6 s), Varga to ≈ K+12.7,
Okafor to ≈ K+19.3, Ares Control to ≈ K+26.7, Okafor's closing line to ≈ K+34; the settling-dust
section (32 s) is sized to them.

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, no more running. Every Vrell on Mars came out of that basin. Today we go in after them." |
| t=13 | Rook | "On your {side}, Lancer. Let's finish this." |
| t=21 | Ares Control | "Ares Control, Aegis. All of Mars is listening today." |
| t=37 | Varga | "Burrowers on the wall. And the song is coming from the floor." |
| Crawler uncovered (the secret) | Ares Control | "Our survey crawler, missing since spring. Take what's aboard." |
| t=53 | Okafor | "Basin floor ahead. Keep your speed up and your eyes on the sand." |
| t=65.5 | Varga | "That ridge isn't a dune, Lancer. It's moving under the sand." |
| t=74 | Okafor | "Whatever it is, it's heading for the hollow. So are we." |
| t=84 | Ares Control | "Our seismographs just went off the scale, Aegis." |
| t=97.5 | The Choir (distorted) | "[the Choir sings]" |
| t=105 (the boss warning) | Rook | His boss-warning bark (his AI, not scripted; Level 14's rule) |
| t=111 | Varga | "It's coming up! Hit the lime in its throat when it opens." |
| Phase 2 (`boss-phase` Hunting) | Okafor | "It's hunting you now, Lancer. Keep moving and mind the pull." |
| Phase 2, queued after Okafor's line | Rook | "Remember the moles, Lancer? I take it back." |
| Phase 3 (`boss-phase` Breaching anywhere) | Varga | "It's under you! Move when the ground cracks!" |
| Colossus destroyed (`boss-destroyed`, the chain's start) | The Choir (speech) | "…many… we are many… we are going…" |
| After the chain and the flash (`CLOSING`) | Varga | "Commander… that was not singing. Those were words." |
| Then (`CLOSING`) | Okafor | "I heard it, Doctor. Log everything." |
| Then (`CLOSING`) | Ares Control | "Clear sky over every dome, Aegis. Dinner's on Ares Landing." |
| Level end (the closing line, `CLOSING`) | Okafor | "Mars holds. They're pulling off the planet. Come home, Aegis." |

Timeline check (text): t=1 closes ≈ 12.4, t=13 ≈ 20.4, t=21 ≈ 28.1, t=37 ≈ 44.4; the secret's line
has the gap from ≈ 44.4 to Okafor at t=53 (the crawler is in reach at t≈44–49; an event line waits
at most 6 s); t=53 closes ≈ 60.9 (the t=61 Wraiths' rear bark, warned at t=58, waits ≈ 3 s), t=65.5
≈ 73.0, t=74 ≈ 81.5 (the t=80.5 pincer's flank bark, warned at t=77.5, waits ≈ 4 s), t=84 ≈ 91.0
(the t=94 Wraiths' rear bark at ≈ 91.4), the Choir's stage sound (≈ 3.9 s, subtitle 5.6 s) closes
≈ 103.5 before the warning at t=105; t=111 closes ≈ 118.5 before the first breach opens (the first
tell starts about t=111). The t=33.5 pincer's flank bark (warned at t=30.5) falls in the free gap
after Ares Control. The Mote stream's warning (t=94.5) follows Rook's rear bark; his bark cooldown
may drop it (no loss: the arrow shows).

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
- The side and rear breaches get the dynamic edge warnings (LEFT, RIGHT, BOTTOM) 3 s ahead of the
  rise; the bottom breach counts as a rear wave for Rook (his Trail formation and rear bark).
- **Rook and the autopilot** must leave the bite circle before the snap, step out of phase 3's
  marker and work against the pull (the spec's last lesson), or the tests cannot finish the fight.
  Rook takes the spec's damage like the ship (`heavy` 10 at the snap or the eruption).
- The Airstrike hits the mouth and the spine vents (ground targets) only while they are up,
  capped at 150 per part per strike by the [specials](../../../player/specials/README.md) rules;
  a strike on a buried body does nothing (M7 = a).
- The boss checkpoint is recorded at the arrival, t=105, on easy and medium, per the
  [retry](../../../systems/retry/README.md) rules; there is none on hard.
- The fight's length is **measured** (the spec): the autopilot on the balance plan's Level 21 fit
  over five seeds, every seed 90–180 s, the par the measured mean (draft 130 s).
- **Bounty: 300 in Act 1 terms** (mouth 264, spine vents 12 each), paid **614 = 22.7 %** of the
  budget at this level's scale 0.80, instead of the spec's 790 (30 %) at an assumed scale of 0.40:
  see *Credit budget* (pending; the spec's own note says this level's scale decides the share).

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
this level.

## Credit budget

Budget(21) = 700 × 1.07²⁰ ≈ **2,709**, the typical haul's target
([economy](../../../systems/economy/README.md#per-level-budget)): 60 % of the air kills, 80 % of
the ground units and targets, half the secret and the secondary, the boss at 100 %. Amounts in
**Act 1 terms** (Dust Kite 4, Skitter 5, Needler 12, Stinger 15, Mote 2, Spore Bomber 25, Mantis
30, Wraith 30, Choir Herald 30, Burrower 24, Dust Devil 26, Shellback 48 whole, Spine Turret 12,
Polyp Mortar 15, the boss 300, the crate 100, the secondary 100), each paid × the act factor
**2.56**, bounties also × the level's scale. A **`bounty_scale` of 0.80** (estimate) puts the
typical haul on the budget: paid per kill Dust Kite 8, Skitter 10, Needler 25, Stinger 31, Mote 4,
Spore Bomber 51, Mantis, Wraith and Herald 61, Burrower 49, Dust Devil 53, Shellback 98, Spine
Turret 25, Polyp Mortar 31; the Colossus 614.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills, air: Dust Kite 35 × 4 + Skitter 9 × 5 + Needler 11 × 12 + Stinger 8 × 15 + Mote 8 × 2 + Spore Bomber 2 × 25 + Mantis 4 × 30 + Wraith 4 × 30 + Choir Herald 1 × 30 = 773 | 1,583 | 950 |
| Ground: Burrower 11 × 24 + Dust Devil 2 × 26 + Shellback 2 × 48 + Spine Turret 6 × 12 + Polyp Mortar 4 × 15 = 544 | 1,114 | 891 |
| Boss: Dust Colossus (mouth 264 + spine vents 3 × 12 = 300) | 614 | 614 |
| Secret: survey crawler (hidden crate 100, 9 % of budget) | 256 | 128 |
| Secondary: clear the basin (65 % of the enemies) | 256 | 128 |
| **Total** (bounty scale 0.80, act factor 2.56) | **3,823** | **2,711** |
| Budget(21) = the typical haul's target; typical +0.1 %, perfect 1.41 × budget | | 2,709 |

**Why the boss pays 22.7 %, not 30 %** (accepted in round 36, Q.21a; as Level 14's 26.8 %): a perfect
run must earn at least 1.4 × the budget (`TypicalHaulTest`), and the boss pays the same in both
columns, so every credit it takes off the kills narrows the gap. With this level's mix the bound
holds up to a share of about 24.8 %; at the spec's 30 % (scale ≈ 0.71, the boss ≈ 445 in Act 1
terms) the perfect run reaches only ≈ 1.38 ×. 22.7 % is inside the act boss's 20–40 % band
(`BalanceTest`). To raise it, the level would need a larger secret or more air bounty (e.g. Stinger
and Mantis waves for Kites), not more density: the bound depends on the mix, not the count. The
scale (0.80) is above M12's 0.3–0.5, as Levels 17–18's (0.55 and 0.92): the density rests on cheap
Kites and the boss's share is small.

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

None yet (`art: none`). The level will need, in M7 part H's round (production straight away, as
Act 2's levels did; the boss's own art is step A-bosses' and part H's):

- the production backdrop: the rim with the CDF line and the wrecks of the retreat, the inner
  wall's terraces and landslides, the dune sea with old breach craters, the lime hollow's cracked
  crust and sinkholes, the arena's **seamless basin tile** (≥ 2,800 px or a loop) with the craters
  the mouths leave, the settling-dust strip from the tile's seam, the Vrell lifting off on
  `high-air`; the ridge pass's look (the travelling ridge and haze, drawn by the game, shared with
  the boss's ridge);
- the props: the survey crawler under its slide (cleared frames, the crate), the CDF hauler's
  wreck;
- the two briefing images; the Act 3 outro's four images (the Act 3 README lists them);
- the round's captures: the approach at medium with the plan's fit, the ridge pass, each of the
  three phases (an edge breach with its warning, the hunting breaches with the spine vents, a
  breach under the ship), the death with the Choir's words, the settling dust, the act end through
  the outro to the hangar.

## Implementation

- [ ] Level data `data.yaml` (M7 part H) with the sections, waves, ground targets, radio, budget,
      threat profile, briefing, backdrop and music; this README's tables become marked tables
- [ ] Reference DPS 100 at L21 and the Act 3 balance plan's Level 21 fit (M18) before the data;
      the act HP factor for the returning `medium`+ units listed in *Waves*
- [ ] Scroll timeline and sections as in *Layout*; the arena at 20 px/s on a seamless tile on real
      seconds, the clock halting at t=245 while the boss lives and a kill jumping it there without
      a visible jump; the 32 s settling-dust section; primary `reach-end` with the arena as the
      condition, `DESTROY THE DUST COLOSSUS`
- [ ] The ridge pass (t≈64–72): the travelling ridge, haze, rumble and light shake; `buried`, no
      contact
- [ ] Wave script: ≥ 55 enemies a minute at medium from t=8 to the arena (`DensityTest`; drafted
      ≈ 66), no empty screen over 3 s on every difficulty (`PacingTest`), the edge warnings for the
      side and rear waves, Burrower fields only over sand with the nests as no-surface zones
- [ ] Boss arena per the arena notes: the checkpoint at t=105 (none on hard), the breaches' edge
      warnings, the bottom breach as a rear wave for Rook; Rook and the autopilot out of the bite
      circle and the marker, against the pull
- [ ] The fight measured on the plan's fit over five seeds (every seed 90–180 s), the HP tuned, the
      par set; the boss's bounty at the share `TypicalHaulTest`'s perfect-run bound allows
      (drafted 22.7 %)
- [ ] Ground targets, the survey-crawler secret (20 HP trigger, hard 28, crate 100, the scanner's
      glint, its line), the overdrive, the supply drone's armour patch at t=89, the boss's phase-3
      patch
- [ ] Secondary `kill_ratio` 65 %, judged at the level end, 100 in Act 1 terms; no radio line
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`, with Rook and
      without); the phase lines as `PHASE`, the death's lines as `CLOSING` in order with the debrief
      waiting for the last; the Choir's speech voiced with its layering; Rook's lines
      `requires: escort`
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel
      (`IntelPanelLayoutTest`); the Colossus's intel silhouette; the L3 strip's marks (the ridge
      pass and the boss: Level 14's strip draws no boss mark yet)
- [ ] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their
      images
- [ ] Act end: the settling dust, the debrief with `ACT III COMPLETE · RED DUST`, the four outro
      pages with `act-complete`, then the release's end moved here from Level 14 and the hangar
      before L22 reading `ACT IV - DEEP WATER: COMING SOON` (`--level 21 --act-end`;
      `OutroOnwardTest`, `DebriefActEndTest`, `CampaignRouteTest`)
- [ ] Typical haul within ±5 % of budget(21) = 2,709 with the level's `bounty_scale`
      (`TypicalHaulTest`); the boss's share inside 20–40 % (`BalanceTest`)
- [ ] Music: track 9's base stem from section 2 and full mix from section 3, the motif layer muted,
      the warning at the arena start, the boss theme after it, none over the settling dust; the
      seismic rumble bed and its cut
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on
      every difficulty (`--escort none` too)
- [ ] Production art and captures for part H's round

## Open questions

- None open: round 36 answered every question (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the [Act 3](../README.md) roster to a draft
  level document. Taken as the recommended options, pending the user's review in round 36: M7 = a
  (the `buried` layer for the Burrowers and the Colossus's body), M10 = b (the Dust Kite as
  popcorn), M12 = a (a `bounty_scale` per level), M14 = a (the Choir's first words at the
  Colossus's death), M17 = a (Rook flies, one dry joke), M18 = a (reference DPS 100), M19 (hand
  tables, no data). Our proposals: the six sections (rim, inner wall, dune sea, lime hollow, the
  arena, settling dust) and speeds; the arena crawling at 20 px/s on a seamless tile with the
  clock halting at its end; the ridge pass as the arena's lesson; 107 enemies (≈ 66 a minute,
  Level 14's level, not the L15–L17 drafts' 85–90) with side and rear waves; the survey-crawler
  secret; the kill-ratio secondary without a radio line; the death's five lines as `CLOSING`
  lines and a 32 s settling-dust section; Vorne's motif muted; Level 14's act-end order with the
  release's end moved to Level 21; `bounty_scale` ≈ 0.80 and the boss at 300 in Act 1 terms
  (22.7 % of the budget, the perfect-run bound) instead of the spec's 790 at 0.40.
- 2026-10-12 (overnight, harmonised with the act README): Ares Warden merged into Ares Control, the
  colony's one voice across the act (his `CLOSING` dinner line and the speakers' note; pending the
  user's review in round 36); the density notes point to the act's band of 60–72 a minute (≈ 66 and
  the scale ≈ 0.80 kept); the music already matches the act (track 9 for the approach, Vorne's
  motif muted).
- 2026-10-13 (user, round 36): approved (D.8), M10, M14 and M17 kept. Q.21 all yes: the boss's
  share 22.7 % (300 in Act 1 terms at scale 0.80), the arena's crawl as a seamless tile with the clock
  halting at t=245, the 65 % kill-ratio secondary, Vorne's motif muted, the five closing lines (≈ 34
  s, the debrief waits), ≈ 66 enemies a minute. The Colossus is the "Sinkhole colony" (B.2 = b).
