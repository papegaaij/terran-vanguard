---
title: Level 29 – Betrayal at Ceres
design: approved
implementation: done
art: final
depends-on: [../../../enemies/air/talon, ../../../enemies/ground/sam-nest, ../../../enemies/space/asteroid-mite, ../../../enemies/README.md, ../../../player/wingmen, ../../../world/belt, ../../../ui/hud, ../../../story/characters]
updated: 2026-10-29
---

# Level 29 – Betrayal at Ceres

## Summary

Act 5 opens at **Ceres Hub**, the belt's biggest dock, the morning after Vorne's broadcast. The
Consortium is still voting when Aegis Wing flies into the hub's dock canyon to keep Helix's Talons and
the Vrell off the docks; at t=60 a station block **turns sides** (its lights switch white → red and its
sleeping SAM Nests wake), half the garrison goes with it, and the wing fights its way down the canyon.
Ceres Hub Control, a loyalist who keeps the dock channel open, points the wing to **Dock 9**: the scroll
halts over a Helix prison tender, the wing breaks its four clamp guns and **Rook** launches in his Ember
to fly the last section ([wingmen](../../../player/wingmen/README.md#rooks-rescue-and-return-l29l30)).
New: the [Talon](../../../enemies/air/talon/README.md) with its hex shield and squad leader, the
[SAM Nest](../../../enemies/ground/sam-nest/README.md) and the
[Asteroid Mite](../../../enemies/space/asteroid-mite/README.md). About 172 s of level clock plus the
rescue halt (≈ 14 s typical), ≈ 124 enemies a minute at medium (≈ 113 in the approved draft; step B3b's rework).

**Status: approved in round 60 (drafted Act 5 design phase, 2026-10-27, step `m9-d-levels-1`).** Written from the act roster, the gap analysis's recommended answers (Q1 a,
Q6 b, Q8 a, Q10 b, Q11 a, Q12 c; J2) and the wave 1 and 3 documents (the units above,
[enemy shields](../../../enemies/README.md#enemy-shields), [rock riding](../../../enemies/README.md#rock-riding),
the [belt](../../../world/belt/README.md), the [HUD](../../../ui/hud/README.md)'s enemy channel, the
[Act 5 speakers](../../../story/characters/README.md#act-5-speakers-draft)). **In data since M9 part B (step B2,
2026-10-28)**: the level's numbers live in [data.yaml](data.yaml) and the briefing, teaser, threat profile, sections,
waves, ground targets, radio and credit budget tables below are rendered from it (`tools/sync_tables.py`); step B2's
placements and deviations are in *Decisions* (pending round 62).

## Briefing

Two pages, as Levels 22–28, each one screen (`BriefingLayoutTest`) with room for its image. The act intro
before it is Vorne's broadcast (Q9 a; [the act](../README.md)); this briefing follows it. Texts (draft):

<!-- data: briefing -->
> **Commander Okafor:** "Vorne has told every station to choose. Ceres Hub has not answered: the
> Consortium is voting, and half its garrison is paid in Helix money. We keep Helix's Talons off
> the docks while they vote. If the hub turns, we fight our way out down the dock canyon."
>
> **Dr. Varga:** "The Ascendancy's fighters are Talons. Their hulls carry a light hex shield: it
> soaks the first hit and grows back if you let it. Their missile nests fire homing diamonds:
> shoot them down. And the belt's rocks carry Vrell mites. Shoot the rock before you pass it."
<!-- /data -->

*Images* (production art in part B, [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`), briefs only now:

- Okafor's page `level-29-ceres-hub`: a top-down CDF chart of Ceres Hub's dock canyon cut into the crust,
  the docks numbered 1–12 along it, the Helix dock block in black and gold, Block C in Coalition grey, the
  outer ring and the CDF transport's rendezvous at the canyon mouth.
- Varga's page `level-29-talon`: her scan of a Talon with its hex shield drawn as an overlay, the squad
  leader's gold stripe marked, an inset of the SAM Nest's red-gold homing diamond.

*Hangar teaser* (shop screen before L29; Rook is missing, so Okafor speaks, not voiced):

<!-- data: teaser -->
> **Commander Okafor:** "Ceres has not chosen yet, Lancer. Fly in as if it had."
<!-- /data -->

The hangar before L29 sells Act 5's first gear: the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md)
(`shield-breaker`), the Shield Mk V, Composite IV and Engine Mk IV; the plan raises the Ion Beam to L4 and
fits the EMP Burst ([Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)). The
Harpoon reads `NO WATER` all act; the escort slot still shows Rook **MIA** and holds the loaner Bulwark.

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule; the wavering
Stormhawks are not enemies): the Talon pincers and two Mite rock groups from the sides.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (Talon wings, Skitter and Mote streams, Asteroid Mites on rocks), `ground` (SAM Nests on the station hulls, the tender's clamp guns) |
| Attack directions | front 89% · sides 11% |
| Density | 4 |
| Recommended traits | `spread`, `homing` |
| Hazards | The turning station (sleeping guns wake at t≈60); drifting rocks with riders |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: the belt, Ceres Hub, `air` + `ground`, front · **L1**: + directions (sides), density 4, hazard "the station turns", the objective `REACH THE OUTER RING` and the secondary `HOLD FIRE` · **L2**: + Talon (new, shielded), SAM Nest (new), Asteroid Mite (new), Skitter and Mote portraits · **L3**: + `spread` and `homing` highlighted, the wave strip marking Block C and Dock 9 |
<!-- /data -->

*Varga's intel line* per sensor-suite level (must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Ceres Hub, Lancer. The belt's biggest dock, still deciding." |
| L1 | "Mostly ahead. Some station guns are asleep: watch the lights." |
| L2 | "New: shielded Talons, SAM nests and mites on the rocks." |
| L3 | "Spread for the mites, homing for the Talons and missiles." |

## Objective

- **Primary** `reach-end`: reach Ceres Hub's outer ring, where the CDF transport waits. The rescue
  halt cannot fail: it lasts until the four clamp guns fall (below).
- **Rook's rescue** (Q8 a, the [wingmen](../../../player/wingmen/README.md#rooks-rescue-and-return-l29l30)
  rules): at t=120 the scroll halts over Dock 9 (eased down over 2 s from t=118); **the level clock stops
  while a clamp gun stands**, as Level 28's clock stops while the Maw lives, so every time after it is
  clock time. The halt has its own **halt cycle** of waves (below), repeating every 16 s while it lasts.
  As the last clamp gun falls the tender's hold opens, Rook's Ember launches, the drone in the escort slot
  peels away to its side's edge ("Bulwark, RTB") and Rook flies into the slot over 1 s; the clock resumes
  and the scroll eases up over 2 s. Typical halt ≈ 14 s (the four clamp guns, 1,000 HP together, at the boss
  rule's 0.6 × the reference of 120, between the halt cycle's waves). The clamp guns sleep until the halt (dark,
  not shootable, `dormant_until` 119.98, the clock's last step before 120) and wake with the 1 s tell as the scroll
  stands, so none falls on the way in (step B3b; the autopilot's halt 11–15 s at medium with the plan's fit).
- **Secondary "Hold fire"** (Q10 b, an inverted escape secondary): three pairs of **wavering Ceres Wing
  Stormhawks** (t=77, 98 and 140) enter, blink a white IFF light, hold their fire and peel off; the
  secondary is met when the third pair has left the screen with none of the six destroyed (a hit does
  not fail it: the craft's IFF light flashes red on a hit, as a warning), failed at once by the first one
  destroyed. HUD counter `HOLD FIRE`, `DONE` when met. Pays **100** credits in Act 1 terms (**655** in Act 5,
  the act factor 6.5536 = 1.6⁴); the typical haul counts 50 %. Spread fire can spoil it: only the bonus is
  lost.

**The wavering Stormhawks** (level-owned craft, not a roster unit: the CDF Stormhawk airframe in Ceres
Wing's colours with the Ascendancy crest freshly painted over the CDF roundel; the concept is ART-P-world's
in round 60, under the [act](../README.md)'s `concept/`):

| Field | Value |
|---|---|
| Layer, size | `air`, `small`, 40×40 px, hitbox 28×28 |
| HP | 40 (easy 30 / hard 52, from the global multipliers): two or three stray hits are survivable |
| Path | a pair enters at the top, descends at 90 px/s to ≈ 150 px below the top edge, holds there ≈ 6 s (a slow sway, a wing-waggle), then peels off to the nearer side edge at 260 px/s; on the screen ≈ 9 s |
| Fire | none; no contact damage (the ship passes under them) |
| Look | a white IFF light blinking 1 Hz; a red flash on a hit |
| Targeting | homing shots and Rook never pick them; Rook's and the Bulwark's shots pass through them; the player's shots and specials hit them (the Airstrike would down them) |
| Credits | 0; never counted as enemies (density, the floor, kill ratios) |

## Layout

Scroll **120 px/s** on the approach and the breakout, **100 px/s** in the dock canyon, **60 px/s** past
Block C (the turn stays on the screen), 0 in the rescue halt; each change eased over 2 s. The setting is
[the belt](../../../world/belt/README.md#sub-locations-per-level)'s Ceres Hub: dark brown-grey rock and
station hulls in harsh sunlight from the top left, cool white Coalition lights next to black-and-gold Helix
facilities, Ceres's curve in the deep layer; the belt is a step lighter than Act 4 (Q11 b). Loyalist haulers
and CDF patrol craft fight Talons on the `low-air` and `deep` layers (presentation only). Vacuum rules:
explosions flash short and throw debris in a ring, wrecks drift and turn. Motion budget: the turning lights
and the woken nests are the strong elements; the canyon walls stay still.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Ceres Approach | 0–28 | 0–3,360 | 120 | clear | `deep`: Ceres's curve, the dust band. `low-air`: a loyalist ore hauler under Talon fire (t≈13). `air`: small drifting rocks (the Mites' rocks; no large rock). `ground`: the hub's outer crust, docking arms. | Popcorn, the first Talons and their shield flash, the first riders. |
| 2. The Dock Canyon | 28–56 | 3,360–6,180 | 100 | clear | `ground`: the canyon cut into the crust, docks 1–5 along its walls (scenery, no collision); the **Helix dock block** (black and gold) with 3 SAM Nests (t≈34); Coalition blocks lit white. | The SAM Nest's homing diamonds; the canyon's rhythm. |
| 3. Block C | 56–76 | 6,180–7,420 | 60 | clear | `ground`: **Block C**, a Coalition station block lit white, with 3 **dormant** SAM Nests on its roof. At **t=60** its lights switch white → red over 1 s, a klaxon, and the nests wake (1 s tell); the lights then go red block by block up the canyon ahead. | The signature: the station turns sides. Quiet window t=56–62 (no wave enters). |
| 4. The Red Canyon | 76–115 | 7,420–11,280 | 100 | clear | `ground`: red-lit blocks, docks 6–8, 3 SAM Nests at t≈84 and 3 at t≈100; the loyalists' supply cache in Dock 6 (t≈108, the secret); mutineer Talons out of the side docks. | The mutineers; the wavering pairs (t=77, 98); Hub Control's lead to Dock 9. |
| 5. Dock 9 | 115–118 | 11,280–11,580 | 100 | clear | `ground`: **Dock 9**, the Helix prison tender in four docking clamps, a clamp gun on each. | The approach to the halt (from t=115, a second before the README's 116: the clamp guns' entries at 115.52 lie in it; the same scroll). |
| 6. The Rescue | 118–120 | 11,580–11,680 | 0 | clear | `ground`: the halted screen shows Dock 9 and the tender in the middle third; the four clamp guns at (150, 170), (330, 170), (150, 330), (330, 330). | The rescue halt (the scroll eased down from 100 px/s over 2 s from t=118, the clock stopped while a clamp gun stands, ≈ 14 s typical); Rook launches, the loaner leaves. |
| 7. Breakout | 120–172 | 11,680–17,800 | 120 | clear | `ground`: the canyon mouth, the outer docks with 3 SAM Nests (t≈131), the outer ring. `air`: small rocks with riders. `deep`: the CDF transport at the rendezvous. | Rook back in the slot; the third wavering pair and Harker; the run out. Level end at t=172 as the outer ring reaches the bottom edge. |
<!-- /data -->

The rescue halt is its own section (*The Rescue*, a held arena: `holds: clamps`): the scroll eases down across its
2 s from 100 px/s (as the simulation's held arena), the level clock stands at its end (t=120) while a clamp gun stands,
and the breakout's ramp eases the scroll up again over 2 s. *Dock 9* runs from t=115 (the README's draft said 116; the
scroll is the same) so the clamp guns' entries lie in it.

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). Reworked in step B3b to the Act 2+ rules (pending the user's review in round
62; the approved draft's waves died within a second of their entry under the plan's Ion Beam L4): measured with
`levelReport` (the plan's fit): 375 enemies at medium, 124 a minute; 2.61 on the screen and 72 shots a minute; two
stretches over 2 s (hard two, easy two over 3 s); the small units live 1.46 s, 0.91× Level 10's; the halt cycle's
units counted for one cycle. Round 62 (step B6, the user's F.1 and F.3): the Talon's shield 2 → 12 and the Mites in
**rock groups** (3–5 rocks drifting in together, 1–2 riders a rock, seven times) — 375 enemies, 124 a minute; 2.99 on
the screen and 88 shots a minute; one stretch over 2 s (hard one, easy two over 3 s); the small units 1.73 s,
1.08× Level 10's (the Talon's own life on the screen 1.49 → 1.77 s).

- **HP** from the proposed Act 5 reference DPS (J2: **120** at L29,
  [balancing basis](../../../enemies/README.md#balancing-basis)): every unit here is new or `tiny`, so no act
  HP factor applies; the Talon and the SAM Nest take the beam-era ×2 (their documents). The clamp guns are
  the level's (*Ground targets*). Every Talon wave here swoops at **130 px/s** (the wave's `speed`; the unit's 220
  left the beam a Talon a second after its entry: step B3b).
- **Density and pacing** (the Act 2+ floor from the start): **375 enemies** (waves 344 with the halt waves of
  a typical halt + ground targets 31) over ≈ 186 s (172 s of clock + the 14 s halt), **≈ 124 a minute** at
  medium (the act's minimum 70; the draft's 113 + 10 %); the README's 3 s beat from t=4 with a **firing group
  on the offbeat** (step B3b): 13 Talon pairs, 6 Talon lines of 4 (one of them in the slot of the draft's Mote
  Swarm at t=150) and 3 Talon pincers beside the wavering pairs (t=77, 97, 138.5, warned 3 s ahead,
  their lanes kept clear), the Mote Swarms on long loops (two mirrored routes; the one at t=108 by the second
  wavering pair keeps the straight S), shorter Skitter snakes of 4 at 10 of the draft's slots, and six pairs of
  SAM Nests on the canyon walls (t≈45, 70, 91, 125, 146, 160), and since round 62 seven **Mite rock groups**
  (t=10, 25, 43, 69, 87, 132, 156; 3–5 rocks a group drifting in together, 1–2 riders a rock: a group of mixed
  riders is two waves at the same time; 48 Mites on 29 rocks; the Mote Swarms at t=43 and 69 gave their slots and
  the halt cycle's swarm 8 → 6 motes, so the density holds at 124 a minute). Front 90 % of the waves (71 of 79),
  sides 10 % (six Talon pincers and the two Mite rock groups at t=25, 132), each **edge-warned 3 s**
  (`WarningEdge`; the t=62 pincer 4 s, so its banner closes as Block C's lights turn). The
  floor comes from the Talons (aimed pairs while in the upper 300 px), the SAM salvos (at most 3 nests firing at
  once) and the clamp guns: ≤ 3 pauses over 2 s (`PacingTest`), ≥ 2.5 enemies on the screen and ≥ 10 shots a
  minute (`ActFloorTest`), the small units' life 0.9–1.5× Level 10's (`BeamEraHpTest`). The quiet window t=56–62
  holds Block C and its waking nests only; the wavering pairs come at 77, 98 and 140, the firing groups round
  them out of their lanes (easy flies none in their 8 s holds). The Dart Drone is not flown here: it is first
  built for Level 30 (J4 of the L30 gap check).
- **Halt cycle** (times from the halt's start H, clock stopped): four waves, H+1 to H+13, repeated from H+17
  if a clamp gun still stands, the cycle cut the moment the last one falls (a wave already in flies on).

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 5.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Step B3b: a second stream before the first Talons |
| 7 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 10 | 1 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Round 62: the first rock group, 4 rocks drifting in together in the approach debris (2 with 2 riders, 2 with 1); the Mite's first level |
| 10 | 1 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | front | Round 62: the first group's 2 one-rider rocks |
| 13 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The first Talons, strafing a loyalist hauler on the `low-air` layer; the hex shield's first flash |
| 16 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 17.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B3b: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 19 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 20.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Step B3b: a looping swarm on the offbeat |
| 22 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The first squad leader (gold stripe): kill it and the wing scatters |
| 25 | 1 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 6 | left side | Round 62: 3 rocks × 2 riders drifting in together from the left edge; warned 3 s ahead |
| 26.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step B3b: a firing Talon line on the offbeat (the beam erases the fodder within a second) |
| 28 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 31 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Into the dock canyon |
| 34 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Over the Helix dock block and its three SAM Nests |
| 35.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step B3b: a firing Talon line on the offbeat (the beam erases the fodder within a second) |
| 38.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Step B3b: a looping swarm on the offbeat |
| 40 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Leader at the tip |
| 43 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Round 62: a rock group in the canyon instead of a Mote Swarm, 4 rocks (2 with 2 riders, 2 with 1) |
| 43 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | front | Round 62: the canyon group's 2 one-rider rocks |
| 44.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B3b: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 46 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 49 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 52 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | The last fodder before the turn's quiet window |
| 53.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B3b: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 55.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B3b: the last firing pair before the turn's quiet window (t=56-62: Block C's dark nests on the screen from 57) |
| 62 | 3 | pincer | [Talon](../../../enemies/air/talon/README.md) | 4 | sides | Two pairs out of Block C's launch bays, the first mutineers; warned 4 s ahead (round 62: its 2.5 s banner over Block C closes at t=60.5 as the lights turn; the turn's quiet window t=56-62 is over) |
| 64.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Step B3b: a looping swarm on the offbeat |
| 66 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 67.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step B3b: a firing Talon line on the offbeat (the beam erases the fodder within a second) |
| 69 | 3 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Round 62: hull fragments off the turned blocks instead of a Mote Swarm, 4 rocks (2 with 2 riders, 2 with 1) |
| 69 | 3 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | front | Round 62: the turned blocks' 2 one-rider rocks |
| 70.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Step B3b: a looping swarm on the offbeat |
| 72 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Mutineers; leader at the tip |
| 76 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | Step B3b: a second later, up to the wavering pair |
| 77 | 4 | line abreast | [Wavering Stormhawk](../../../enemies/air/wavering-stormhawk/README.md) | 2 | front | The first wavering pair (Hold fire): white IFF lights, holding fire; not enemies |
| 77 | 4 | pincer | [Talon](../../../enemies/air/talon/README.md) | 4 | sides | Step B3b: mutineer pairs out of the side docks under the wavering pair, its lane kept clear; warned 3 s ahead |
| 81 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B3b: a firing Talon pair on the offbeat (the beam erases the fodder within a second); not on easy (the wavering pair holds 8 s there) |
| 84 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 87 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 6 | front | Round 62: hull fragments torn off the red blocks, 5 rocks (3 with 2 riders, 2 with 1) |
| 87 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | front | Round 62: the red blocks' 2 one-rider rocks |
| 88.5 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Step B3b: a looping swarm on the offbeat |
| 91.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step B3b: a firing Talon line on the offbeat (the beam erases the fodder within a second) |
| 93 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Leader at the tip |
| 97 | 4 | pincer | [Talon](../../../enemies/air/talon/README.md) | 4 | sides | Step B3b: mutineer pairs out of the side docks beside the second wavering pair, its lane kept clear; warned 3 s ahead |
| 98 | 4 | line abreast | [Wavering Stormhawk](../../../enemies/air/wavering-stormhawk/README.md) | 2 | front | The second wavering pair |
| 102 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | After the second wavering pair (t=98) |
| 103.5 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | Step B3b: a second stream past the wavering pair, its lane kept clear |
| 105 | 4 | pincer | [Talon](../../../enemies/air/talon/README.md) | 4 | sides | Two pairs; warned 3 s ahead |
| 108 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 109.5 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Step B3b: a looping swarm on the offbeat |
| 111 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 114 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | Over Dock 9's approach |
| 117 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | The halt begins at t=120 (the halt cycle: `halt_waves`) |
| H+1 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Halt cycle |
| H+5 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Halt cycle; not on easy |
| H+9 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Halt cycle (round 62: 8 → 6 motes, room for the rock groups) |
| H+11 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Halt cycle, hard only (the README's pincer as a front line: a halt cycle's waves come from the front) |
| H+13 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | Halt cycle (the typical halt ends ≈ H+14: the cycle starts again at H+17 if the clamps still stand) |
| 121 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B3b: Helix's pursuit out of Dock 9 as the scroll resumes |
| 123 | 7 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Rook's first fight: leader at the tip |
| 124.5 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B3b: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 126 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 127.5 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step B3b: a firing Talon line on the offbeat (the beam erases the fodder within a second) |
| 130.5 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B3b: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 132 | 7 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 8 | right side | Round 62: 4 rocks × 2 riders drifting in together from the right edge; warned 3 s ahead |
| 133.5 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B3b: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 135 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 138.5 | 7 | pincer | [Talon](../../../enemies/air/talon/README.md) | 4 | sides | Step B3b: Harker's pairs out of the outer docks beside the wavering pair, its lane kept clear; warned 3 s ahead |
| 140 | 7 | line abreast | [Wavering Stormhawk](../../../enemies/air/wavering-stormhawk/README.md) | 2 | front | The third wavering pair; Harker's wing |
| 143.5 | 7 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | Step B3b: past the third wavering pair, its lane kept clear; not on easy |
| 145.5 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B3b: a firing Talon pair on the offbeat (the beam erases the fodder within a second); not on easy (the wavering pair holds 8 s there) |
| 147 | 7 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 7 | front | A full wing; leader at the tip |
| 150 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step B3b: a Talon line instead of the README's Mote Swarm (the beam erases the fodder); not on easy (the wavering pair holds 8 s there) |
| 151.5 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B3b: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 153 | 7 | pincer | [Talon](../../../enemies/air/talon/README.md) | 4 | sides | Two pairs; warned 3 s ahead; Rook's flank bark |
| 156 | 7 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 6 | front | Round 62: the last rock group, 5 rocks (3 with 2 riders, 2 with 1) |
| 156 | 7 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 2 | front | Round 62: the last group's 2 one-rider rocks |
| 157.5 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Step B3b: a looping swarm on the offbeat |
| 162 | 7 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The last wing; leader at the tip |
| 165 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 168 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B3b: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 170 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B3b: the last Talons, out to the outer ring |

Totals: Skitter 52 · Mote Swarm 102 · Asteroid Mite 48 · Talon 142 (with one cycle of the halt waves: a typical halt); the Wavering Stormhawks (6) are not enemies.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 2 | [SAM Nest](../../../enemies/ground/sam-nest/README.md) ×3 on the Helix dock block (t≈34) | Bounty 15 each |
| 3 | SAM Nest ×3 on **Block C**, **dormant until t=60** (x 120, 240, 360; at the turn 160 px below the top edge) | Dark, white-lit, not shootable and not picked until they wake (the SAM Nest's *dormant until t*); bounty 15 each |
| 2 | SAM Nest ×2 on the canyon walls (t≈45; step B3b) | Bounty 15 each |
| 3 | SAM Nest ×2 on the turned canyon walls (t≈70; step B3b) | Bounty 15 each |
| 4 | SAM Nest ×3 on red blocks (t≈84) | Bounty 15 each |
| 4 | SAM Nest ×2 by dock 7 (t≈91; step B3b) | Bounty 15 each |
| 4 | SAM Nest ×3 on the bridging red block (t≈100) | Bounty 15 each |
| 4 | The loyalists' supply cache in Dock 6 (a `ground` trigger, 20 HP, t≈108, x 430) | The secret: a crate worth 100 (655) |
| 5 | **[Clamp gun](../../../enemies/ground/clamp-gun/README.md)** ×4 on the prison tender's docking clamps (the halted screen's middle third: x 150 and 330, y 170 and 330) | Rook's rescue: the halt ends with the last one; bounty 10 each |
| 7 | SAM Nest ×2 on the canyon mouth (t≈125; step B3b) | Bounty 15 each |
| 7 | SAM Nest ×3 on the outer docks (t≈131) | Bounty 15 each |
| 7 | SAM Nest ×4 on the outer canyon walls (t≈146 and 160; step B3b) | Bounty 15 each |
<!-- /data -->

**Clamp gun** ([its stat block](../../../enemies/ground/clamp-gun/README.md), a unit data file since step B2: the
loader, the validator, the looks and the atlas key on unit kinds; `ground`, `medium`, not hardened; a turret on a
docking clamp): 250 HP (step B3b: the four 1,000 together for the ≈ 14 s halt; the draft's 80 fell in ≈ 3 s),
asleep until the halt; an `aimed` single shot every 2.0 s with a 0.3 s red muzzle glint; destroyed, the clamp springs
open with a burst of gas and sparks.
27 SAM Nests and 4 clamp guns: 31 ground enemies. At most 3 nests fire at once (the SAM Nest's rule):
Block C's three wake as the Helix block's have left the screen; step B3b's wall pairs (on the canyon's rock faces by
the docks and the turned blocks, x 52 and 428; no backdrop piece moved) enter as the group before them has left.

## Hazards

- **The turning station** ([belt hazards](../../../world/belt/README.md#hazards--set-pieces)): Block C's
  three nests are scenery until t=60, then live; the lights going red up the canyon ahead tell the player
  every later block is hostile. No terrain collision: the canyon walls and blocks are flown over.
- **Drifting rocks**: small breakable rocks only (Level 03's small-chunk rules; they block shots of both
  sides), carrying the Mites ([rock riding](../../../enemies/README.md#rock-riding)); no large rock in this
  level (the canyon is narrow enough).

## Secrets and pickups

- **The loyalists' supply cache** (t≈108, x 430, a locker under a crane gantry in Dock 6): a `ground` trigger
  of **20 HP**; destroyed, the locker's door blows and a hidden crate worth **100** in Act 1 terms (655 in
  Act 5) pops out; the Salvage scanner's glint marks it. Ceres Hub Control's event line: "*Dock Six stores.
  Take them, Aegis. Better you than Helix.*" (dropped after 6 s if the radio is busy).
- **Overdrive**: dropped by the t=22 squad leader.
- **Shield cell**: dropped by the t=105 Talon pincer's last Talon.
- **Armour patch**: a loyalist ore hauler on the `low-air` layer releases one at (240, 380 y-up) at t=150.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), drafted for the **1 s rule** (≈ 12 characters a
second, a 1.5 s lead, 0.4 s of silence; event lines drop after 6 s); times are **clock** times (the clock
stops in the halt; the halt's lines are events). The build's `RadioTimelineLevel29Test` checks them with and
without an escort. **Rook has no lines before the rescue** (his barks stay dropped, as in L27–L28); from the
rescue on his barks carry `requires: escort` and his scripted lines (the rescue, t=141) do not.

**Speakers** ([Act 5 speakers](../../../story/characters/README.md#act-5-speakers-draft)): Okafor, Varga,
**Ceres Hub Control** (a loyalist dock controller on the **friendly** channel, a man's voice, tired and
steady; his radio portrait the dock control room with its lights going red behind him), **Harker**, the
mutineers' flight lead (a Ceres garrison pilot Rook flew convoys with, on the **enemy channel**: amber, the
steady interference, `INTERCEPT`, [HUD](../../../ui/hud/README.md)), and Rook after the rescue. **Vorne is
silent here**: his broadcast is the act intro; Ceres Hub's declaration is shown, not read (the lights, the
klaxon).

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Ceres Hub (Ceres Hub Control) | "Aegis flight, Ceres Hub Control. You heard him too. The Consortium is still voting. Keep our docks clear while they talk." |
| t=14 | Varga | "Talons. Gold hexes are a light shield: keep firing through it." |
| t=24.5 | Okafor | "The striped one leads the wing. Drop it and the rest break off." |
| First SAM salvo (`first-homing`, t≈35) | Varga | "Helix missiles. Shoot the red-gold diamonds or outrun them." |
| t=46 | Ceres Hub (Ceres Hub Control) | "The vote is closing. If the lights go red, those are not my guns." |
| t=60 | Ceres Hub (Ceres Hub Control) | "Block C just declared! The garrison is split. Their guns are hot!" |
| t=70.5 | Harker | "Ceres Wing, Harker. We're Ascendancy now. Clear the CDF out." |
| t=79 | Varga | "Those white blinkers are holding fire, Lancer. Let them go." |
| A wavering pair leaves (`neutral-left`, the first time) | Harker | "Who broke off? Ceres Wing, get back in line!" |
| t=90 | Okafor | "Ceres has turned, Lancer. Fight your way down to the transport." |
| t=101 | Ceres Hub (Ceres Hub Control) | "Dock 9's prison tender holds a CDF pilot. His fighter, too." |
| t=113 | Okafor | "Dock 9 ahead. Break the clamps, Lancer. Bring him home." |
| t=119.9 (the halt begins; it waits for Okafor's line) | Ceres Hub (Ceres Hub Control) | "Four clamps, a gun on each. Break them and the tender lets go." |
| The last clamp gun falls (`holders-down`) with the Bulwark flying in the slot | Okafor | "Bulwark, RTB. Rook, form on Lancer." |
| The last clamp gun falls with no Rook to join the slot (`--escort none`: the Ember leaves upward) | Okafor | "Rook, you're clear. Form on the transport." |
| Rook joins his slot (`escort-joined`, 1 s after) | Rook | "Miss me? Don't answer that. Just shoot." |
| t=133 | Harker | "Rook on the Aegis net? Pity. I'd have bought you a drink." |
| t=141 | Rook | "Harker's wing. I flew convoys with them. Doesn't matter now." |
| Secondary met (the third pair gone, ≈ t=147) | Varga | "None of the white ones fell. Someone in Ceres will remember that." |
| Secondary failed (`neutral-down`, a wavering craft destroyed) | Harker | "That one held fire, Aegis. Remember that." |
| t=156 | Ceres Hub (Ceres Hub Control) | "Outer ring holds. Go. Tell Earth that not all of Ceres said yes." |
| t=163.5 | Okafor | "You're clear of the canyon. The transport is at the outer ring." |
| Level end (the closing line) | Okafor | "Ceres is lost, but we have Rook back. I'll take that trade." |
<!-- /data -->

Timeline check (step B4b, at the HUD's text model: pages of 3 × 22 characters, 3 s a page and 5 s for the last, as
Acts 1–4): every line but the t=1 call is **one page** (at most 66 characters, ≈ 6.2–7.2 s on the radio); the timed
lines close at ≈ 13.1, 21.1, 31.6, 53.2, 67.2, 77.5, 86.0, 97.1, 108.0, 119.9, 127.0 (into the halt: the clock stops
at 120, the line plays on), 139.9, 148.0, 163.2 and 170.6, before the level's end at 172. `RadioTimelineLevel29Test`
holds every timed line at most 1 s late, text and voiced, in every run: the loaner flying at the last clamp (its
halt 7.6, 13 or 19 s), the loaner lost before it, `--escort none`, a wavering craft shot down. The secondary's line
(≈ t=147–148.5) takes the radio while it is free, so Hub Control's closing line waits for it at t=156. Rook's t=141
line is his Act 5 mutineer line from [Rook](../../../story/characters/rook/README.md) with Harker's name; his pulp
one-liner is the rescue line. **The last clamp's line follows the escort swap** (the `holders-down` cue's `swap`, the
simulation's swap outcome at the release): "Bulwark, RTB" only with the Bulwark (the loaner or an owned one) still
flying in the slot; nothing when the loaner was shot down before the swap (hard's long halt can wear it down); with
`--escort none` (the Ember launches and leaves upward) Okafor's other line, and Rook's lines still play (he flies
elsewhere in the formation).

## Music & ambience

The Act 5 A theme **"Hollow Rock"** ([track 12](../../../audio/music/README.md#track-list), chosen in round
03; full length and stems in M9 part B): its intro (radio static and Vorne's motif in cold brass and organ)
under the approach, the **base stem from section 2** (the canyon) with **Vorne's motif stem on top** from there to the
end (the [Act 5 cues](../../../audio/music/README.md#cues-per-level-act-5): "on"), the **full mix from the turn at t=60**
(Vorne's motif in full brass as the lights go red), held through the halt; at Rook's launch a short brass
lift on the motif's answer (a sting in the stems, D-audio's call), then the full mix to the end. Ambience:
the `belt-station` bed (station hum, hull creaks, distant dock machinery; the gap analysis's bed per
setting) and the turning station's klaxon. New sounds this level needs, for the act's sfx step
([sfx](../../../audio/sfx/README.md)): the klaxon and the lights' heavy relays switching, the nests' wake
(their documents), the clamp gun's shot, a clamp springing open (a heavy hydraulic release and gas), the
Ember's launch from the hold, the Bulwark's departure; real-world sounds from Freesound only (CC0/CC-BY), no
bubbly or popping sounds. The per-level music table is D-audio's ([music](../../../audio/music/README.md)).

## Credit budget

Budget(29) = 700 × 1.07²⁸ ≈ **4,654**, the typical haul's target
([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act 1 terms** (Mote 2, Skitter
5, Talon 12 (leader 25, counted at 12 here), Asteroid Mite 3, SAM Nest 15, clamp gun 10, the crate 100, the
secondary 100), × the act factor **6.5536** (1.6⁴), bounties also × the scale. A **`bounty_scale` of 0.35**
(the earlier acts' model: typical = 0.663 of the kills, 0.8 of the ground targets, half of the secret and
the secondary; the draft's hand estimate 0.44, fitted in step B2 with `levelReport` at 0.48: the halt cycle's waves
for one cycle, the leaders' extra in the perfect run only; re-fitted to 0.35 in step B3b, the rework's Talons and
nests paying more than the fodder they replace) puts the typical haul on budget (table below). No boss or
mid-boss.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 52 × 5 + Mote Swarm 102 × 2 + Asteroid Mite 48 × 3 + Talon 142 × 12 | 5,394 | 3,236 |
| Squad leaders: 7 × 13 extra (perfect run only) | 203 | 0 |
| Ground targets: SAM Nest 27 × 15 + Clamp gun 4 × 10 | 1,010 | 808 |
| Secret: Supply cache (hidden crate, 2% of budget) | 655 | 328 |
| Secondary: hold fire on the wavering Ceres Wing | 655 | 328 |
| **Total** (bounty scale 0.35) | **7,917** | **4,699** |
| Budget(n) = the typical haul's target; typical +1 %, perfect 1.70 × budget | | 4,654 |
<!-- /data -->

The squad leaders' extra 13 each (seven V-wings) is counted in the perfect run; a longer halt's extra waves add to it. **Balance plan**: the plan's L29 visit raises the Ion Beam to L4 and fits the EMP Burst
([Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)); the level expects `spread` and
`homing` and is flyable without them.

## Difficulty notes

- **Easy**: the halt cycle without its Talon wave; the clamp guns fire every 2.6 s; the wavering pairs hold
  8 s (more time to read the white light), and the offbeat Talons and the Skitters in those holds stay out (the
  waves at t=81, 143.5, 145.5 and 150: step B3b); the unit documents' easy hooks.
- **Hard**: the halt cycle adds a Talon pincer (2 + 2) at H+11; the clamp guns fire `aimed` pairs; the
  wavering pairs fly 40 px lower, among the waves; the other waves by the formation lever, trimmed by hand so
  never more than two Talon wings are on the screen.

## Concept art

Round 60's proposals for the level live in their parts: the belt mood still for Ceres Hub (in the
[belt](../../../world/belt/README.md#concept-art)), the wavering Stormhawk (under the [act](../README.md)'s `concept/`), the
speakers' radio portraits ([characters](../../../story/characters/README.md#concept-art)), the units in their documents.

Production art, M9 part B (concept round 62, step ART-B), straight to production in round 60's look (A.18 a, the lighter
belt grade; Q.11 b): the backdrop (`tools/art/backdrop_l29.py`: the dock canyon in Ceres's crust, the crust's edge, the
Helix block, Block C with its white and red lights, the red blocks, Dock 6, Dock 9 with the prison tender and its hold,
the outer docks and ring, Ceres's limb, the CDF transport, the low-air haulers and patrols; its `backdrop:` block for B2 in
[concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml)) and the Dock 9 pieces (`tools/art/belt_units.py clamp
launch`: the clamp gun, its clamps springing open, the gas, Rook's Ember launching). Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r62-a.png](concept/backdrop-final-r62-a.png) | Production backdrop (`tools/art/backdrop_l29.py`): ten screens at half size with stand-ins (the approach and Ceres's limb, the crust's edge and docking arms, the Helix block, Block C white and red, the red canyon, Dock 9 halted with the clamp guns, the canyon's mouth, the outer docks, the outer ring at the end) and the three tile sets (sheet) | chosen (round 62, F.7: accepted as final) |
| [concept/backdrop-final-r62-a.gif](concept/backdrop-final-r62-a.gif) | t 54–66 at half size: Block C's white lights turning red at t=60; then the halted Dock 9 | chosen (round 62, F.7: accepted as final) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block, its sections and the ground targets' places for step B2 (not data) | chosen (round 62, F.7: accepted as final) |
| [concept/clamp-gun-final-r62-a.png](concept/clamp-gun-final-r62-a.png) | Clamp gun final frames (`tools/art/belt_units.py`): 16 headings and the muzzle glint, the left and right docking clamps springing open, the gas burst, Rook's Ember launching from the hold (sheet) | chosen (round 62, F.6: accepted as final) |
| [concept/clamp-gun-final-r62-a.gif](concept/clamp-gun-final-r62-a.gif) | The four clamp guns round the tender falling one by one, each clamp springing open with its gas; the Ember rising out of the hold and joining | chosen (round 62, F.6: accepted as final) |
| [concept/station-turn-capture-r62-a.png](concept/station-turn-capture-r62-a.png) | Game capture (step B5): Block C turning at t=60, its white lights going red, the nests waking ([recipe](concept/prompts.md#level-29-capture-r62-a)) | chosen (round 62: the capture accepted with the part's art) |
| [concept/station-turn-capture-r62-a.mp4](concept/station-turn-capture-r62-a.mp4) | Game capture with the game's sound: level ≈ 54–67 s, the quiet window, the relays and the klaxon, the music's turn to the full mix | chosen (round 62: the capture accepted with the part's art) |
| [concept/rescue-capture-r62-a.png](concept/rescue-capture-r62-a.png) | Game capture: the halt (`CLAMPS 4`, Hub Control's call) and the rescue (the last clamp gun, its clamp opening, the Ember leaving the hold, Rook in his slot) | chosen (round 62: the capture accepted with the part's art) |
| [concept/rescue-capture-r62-a.mp4](concept/rescue-capture-r62-a.mp4) | Game capture with the game's sound: the halt's start and its cycle, then the last clamp guns, the Ember's launch with Rook's sting, the Bulwark peeling away, the scroll resuming and Harker's t=133 line | chosen (round 62: the capture accepted with the part's art) |

## Implementation

- [x] Level data `data.yaml` (M9 part B): sections and speeds with 2 s eases, waves and the halt cycle, ground
      targets, radio, budget, threat profile, briefing, backdrop, music (step B2, 2026-10-28)
- [x] Block C's turn: three SAM Nests dormant until t=60 (dark, not shootable, not picked), the lights white →
      red over 1 s and up the canyon ahead, the klaxon, the 1 s wake tell (steps B1–B4: `DormantTest`,
      `Level29Test.theNestsSleepUntilTheTurnAndTheClampsHoldTheClock`,
      `Level29LooksTest.blockCsLightsTurnRedAtTheNestsWakeOverOneSecond`,
      `Level29SoundsTest.theTalonsTheNestsAndTheTurningStationSound`: the relays at t=60, then the klaxon; seen and
      heard in round 62's captures)
- [x] The rescue halt: the scroll halts at t=120, the level clock stops while a clamp gun stands, the halt
      cycle repeats and is cut at the last clamp; the four clamp guns as ground targets
- [x] The mid-level escort swap ([wingmen](../../../player/wingmen/README.md#rooks-rescue-and-return-l29l30)):
      the Ember launches, the drone peels away, Rook joins over 1 s at full armour; `--escort rook:`, `--escort
      none` and the mission-select replay as that document says; a retry restarts Rook MIA (steps B1–B4b:
      `EscortSwapTest`, `Level29Test.theDebugEscortsFlyTheLevel` and `theLastClampsLineFollowsTheLoaner`,
      `Level29LooksTest.theEmberRisesFromTheHoldAndAnUnclaimedOneLeavesUpward`, `RadioTimelineLevel29Test`; round 62's
      halt and rescue capture)
- [x] The "Hold fire" secondary: three wavering pairs (path, IFF blink, red hit flash, no fire, no contact),
      never picked by homing shots or Rook, Rook's and the Bulwark's shots pass through; `HOLD FIRE` counter;
      met when the third pair is gone with none destroyed, failed at the first destroyed (steps B1–B4: `HoldFireTest`,
      `Level29LooksTest.theIffLightBlinksAtOneHertz` and `theHudCountsTheWaveringCraftAndTheClamps`, `Level29Test`'s
      autopilot keeping the six alive; round 62's capture)
- [x] Wave script: ≥ 70 enemies a minute at medium (`DensityTest`), the Act 2+ floor (`PacingTest`,
      `ActFloorTest`), the quiet window t=56–62, edge warnings for every side entry (step B3b's rework: 124 a
      minute, 2.61 on the screen, 72 shots a minute, 2 stretches over 2 s; the small units 0.91× Level 10; round 62's
      rock groups and Talon shield, step B6: 124 a minute, 2.99 on the screen, 88 shots a minute, 1 stretch over 2 s,
      the small units 1.08×)
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel29Test`, with the loaner, an owned
      Bulwark and `--escort none`); Ceres Hub Control and Harker cast; Harker on the enemy channel; no Rook line
      before the rescue (steps VOX-B, B4, B4b: voiced, cast a/b pending round 62 V.1–V.2, every timed line ≤ 1 s late in
      `RadioTimelineLevel29Test`. Step B5's captures found Rook's barks running while he was in the hold; step B6 fixed
      it: his barks are off until he is in his slot, `LevelScreen.rookInSlot`, `BarksTest`, `LevelScreenRescueTest`,
      `Level29Test`'s autopilot)
- [x] Threat profile, Varga's four intel lines and Okafor's teaser fitting the intel panel
      (`IntelPanelLayoutTest`); the Talon, SAM Nest and Asteroid Mite portraits (steps B2, B4: the intel portraits of
      the five new units, `PortraitsTest`; the RECOMMEND row's cut fixed; round 62 F.11)
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images (steps B2,
      ART-B: `level-29-ceres-hub`, `level-29-talon`; round 62 F.10)
- [x] Typical haul within ±5 % of budget(29) = 4 654 with the level's `bounty_scale` (`TypicalHaulTest`)
- [x] Music and ambience: "Hollow Rock" by section, the `belt-station` bed, the klaxon, the clamp and launch
      sounds (step B4: `Level29SoundsTest`, track 12's three stems and Rook's sting in `Tracks`, the 14 sounds played
      with their round 62 **a** proposals until the user's picks; heard in round 62's capture videos; step FIX1: the
      motif stem on top from section 2, `music.motif`, `Level29SoundsTest`)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level on every difficulty with the
      loaner and with `--escort none`, breaking the clamps and keeping the wavering craft alive in most runs (step B3:
      `Level29Test`, the plan's fit, 3 seeds a difficulty: won 9/9 without a retry, Hold fire met 9/9; after step
      B3b's rework the same, the halt 13.0 s on average at medium, 7.6 s easy, 19.2 s hard)
- [x] Production art and captures for part B's round (steps ART-B, B4, B5: [round
      62](../../../concept-rounds/round-62/README.md), the captures in this part's and the units' `concept/`)

## Open questions

- **Q8** (round 60, Rook's rescue): drafted as **a**, on the screen at Dock 9. Alternatives: (b) radio only,
  loyalists free him off-screen and his line comes at L29's end, he flies from L30; (c) told in the L29
  debrief or the L30 briefing only. Under (b) or (c) the halt, the clamp guns and the swap go and section 5
  becomes canyon.
- **Q10** (round 60, the mutineers): drafted as **b**, the wavering pairs and the "Hold fire" secondary.
  Alternatives: (a) linear, all mutineers fly Talons and the cost is told on the radio (Harker's lines stay,
  the secondary becomes a kill-ratio one); (c) a story choice remembered later (against the linear campaign).
- **Q1** (enemy shields), **Q6** (popcorn: Vrell fill here, the Dart Drone from L30), **Q11** (the Ascendancy
  look) and **Q12** (the arsenal: the Tesla Coil Pod in this hangar) as their documents draft them.
- Is a clock stopped for a ground set piece (not a boss) right, or should the halt run on a timer with the
  clock (Level 13's model, a fixed halt length)? Our reading: stopped, so nothing in the rescue can be missed.

## Decisions

- 2026-10-27 (Act 5 design phase, step `m9-d-levels-1`; **approved in round 60**): promoted from
  the act roster to a draft level document, hand tables only, no `data.yaml`. Confirmed the gap analysis's
  timings: the turn at t=60 (Block C, three dormant SAM Nests, a 6 s quiet window), the rescue halt at t=120
  with the level clock stopped while the four clamp guns (80 HP each) stand; the "Hold fire" secondary with
  three wavering Stormhawk pairs (Q10 b), not counted as enemies; ≈ 113 enemies a minute, `bounty_scale` 0.44.
  Speakers per the Act 5 speakers table: Ceres Hub Control friendly and the one who points to Dock 9, Harker
  (the mutineers' flight lead, a name of this draft) on the enemy channel, Vorne silent in the level; no Dart
  Drones here (J4: first built for L30).
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`, D.2): Q8 **a** (Rook's rescue on the screen), Q10 **b** (the mutineers waver; "Hold fire"), Q1 **a** (shields), Q6 **b**. The wavering Stormhawks are **A.22 a**, Ceres Wing ochre (b, hasty blackout, rejected); Rook's sting is **M.2 a**, "Horn Call". Q11 **b**: the Ascendancy units fly in gunmetal/charcoal with gold, not gloss black.
- 2026-10-28 (M9 part B, step B2, pending the user's review in round 62): the level data. Measured with `levelReport`
  (the plan's fit, medium): 350 enemies (331 in waves with one halt cycle, 19 ground), 116 a minute; the floor 3.25 on
  the screen and 60 shots a minute; no stretch over 2 s; typical haul 4,666 (+0.3 %) at `bounty_scale` **0.48** (the
  draft's 0.44 left it 6 % short once the halt cycle and the leaders were counted), perfect 8,006 (1.72×). The build's
  readings: the rescue is a section of its own (*The Rescue*, 118–120, a held arena `holds: clamps`: the scroll eases
  down across it from 100 px/s, as the simulation's held arena), so *Dock 9* starts at 115 (the same scroll) and the
  breakout is section 7; the clamp guns enter at t=115.52 and 117.12 to stand at y 330 and 170 on the halted screen,
  Block C's nests at t=57.148 to stand 160 px below the top edge at the turn; the Talon's swoop is 280 px across (≈ 3 s
  on the screen; 160–240 left 1–4 stretches over 2 s round the wavering pairs) and its aimed pair a 2-shot fan 4° wide
  (no side-by-side offset in the simulation); the rocks drift at the scroll plus 30 px/s (side rocks 90 px/s across);
  hard's H+11 Talon pincer flies as a front line abreast (a halt cycle's waves are plain front waves); Hub Control's
  halt line is a timed line at t=119.9 (no cue marks a held arena's halt start: it waits ≈ 3.5 s behind Okafor's
  t=117 line); Rook's rescue line plays on `escort-joined` (so not with `--escort none`, and Okafor's `--escort none`
  line cannot be told from the loaner's by the radio's requirements: step B4); the clamp gun and the wavering
  Stormhawk are unit data files (gap check §4); the supply cache is a destructible (20 HP) that reveals the secret,
  drawn as the default container until its locker look exists; the armour patch's drop has no carrier sprite (the
  backdrop's hauler passes there); the secondary is named "Hold fire on the wavering Ceres Wing". Okafor's briefing page trimmed to
  one screen with its image (`BriefingLayoutTest`: 6 lines of 4): "in the system", "yet", "We fly in under the CDF
  flag" and "we do not stay to argue" dropped.
- 2026-10-28 (M9 part B, step B3, pending the user's review in round 62): the level's tests (`Level29Test`) and the
  autopilot's Hold fire (it never lines up under a wavering craft, keeps its x more than 48 px from each one's and holds
  its fire while one is in that band above it): with the balance plan's fit (the Ion Beam L4, both pods L3, the Tail Gun
  L3, the EMP Burst, the loaner and Rook in the hold) it wins on every difficulty without a retry and keeps the six alive
  in 9 of 9 runs (`--escort rook:` and `--escort none`: 5 of 6). Step B2's measure had flown the starter fit (the level
  was missing from the plan's fits); with the plan's fit the waves die within a second of their entry: **1.42 on the
  screen at medium (the Act 2+ floor 2.5), 38 shots a minute, 21 stretches over 2 s (hard 20; easy 5 over 3 s)**, the
  small units' life 1.21 s, **0.75× Level 10's** (0.9–1.5×): the waves' rework is left to the user (`PacingTest` and
  `ActFloorTest` measure and print it, `BeamEraHpTest` holds it at 0.65× until then). **The clamp guns fall as they
  scroll in** (320 HP against the beam and the pods from t≈115.5), so the halt lasts 0 s on easy and medium and 1–2 s
  on hard (the README's ≈ 14 s) and its cycle hardly runs; Rook's rescue still plays at the last gun. The density
  (116 a minute) and the typical haul (4,666, +0.3 %) are unchanged; the clamp gun's bounty 10 is this budget's, so
  `BalanceTest` accepts it. Act 4's `next_act` is gone: the release ends after this level's win (no outro; Level 30 is
  not built).
- 2026-10-28 (M9 part B, step B3b, pending the user's review in round 62): **the waves reworked to the Act 2+ rules and
  the halt fixed**, as Level 22's rework in M8 (building to the approved README's own rules). Kept: the seven sections
  and their times, the scroll and the backdrop (no piece moved), Block C's dormant nests and their t=60 wake, the
  quiet window, the three wavering pairs and their lanes (Hold fire), the seven led V-wings, the five rock waves, the
  Dock 9 approach and the radio (no line moved: its triggers keep their times). Changed: a firing group on each
  offbeat (13 Talon pairs, 6 Talon lines of 4, 3 Talon pincers beside the wavering pairs, out of their lanes), every
  Talon wave at 130 px/s (the unit's 220: a Talon died a second after its entry), the Mote Swarms on two long
  mirrored loops (L21's), Skitter snakes of 4 (the unit's 6–12) at 10 of the 13 slots, two Mote slots (t=43, 150)
  flown by Talon lines, six pairs of SAM Nests on the canyon walls; easy flies no offbeat Talons in the wavering
  pairs' 8 s holds. The clamp guns sleep until the halt (`dormant_until: 119.98`: the clock stands a step before the
  arena's end, so 120 would never wake them; the validator now says so) and carry **250 HP** (the four 1,000, the
  README's ≈ 14 s at the boss rule's 0.6 × the reference; the clamp gun's `BalanceTest` time to kill 2.1 s is
  accepted, pending). Before → after (`levelReport`, the plan's fit): medium 350 → 375 enemies (116 → 124 a
  minute), **1.42 → 2.61 on the screen**, 38 → 72 shots a minute, **21 → 2 stretches over 2 s**, small units
  **0.75× → 0.91×** Level 10; easy 5 → 2 pauses over 3 s (95 → 101 a minute); hard 20 → 2 stretches over 2 s (1.82 →
  4.18 on the screen); **the halt 0–2 s → 13.0 s at medium** (11.5–14.8 over three seeds; easy 7.6, hard 19.2), one
  halt cycle running, Hub Control's t=119.9 line at the halt's start and Rook's rescue at the last gun; typical haul
  4,683 (+0.6 %) at `bounty_scale` **0.35** (0.48 left it 34 % over), perfect 7,889 (1.70×). Level 29 leaves
  `PacingTest.PENDING_WAVES` and `BeamEraHpTest.PENDING_LEAST`. Found: on hard the 17–21 s halt under the clamp guns'
  aimed pairs can wear the loaner Bulwark down (it draws 30 % of the aimed fire) before the swap, so Okafor's
  "Bulwark, RTB" may follow a lost drone (`Level29Test` accepts the loss on hard only).
- 2026-10-28 (M9 part B, step B4b, pending the user's review in round 62): **the radio fitted to the clock**. At the
  HUD's text model the lines needed ≈ 196 s of radio in the 172 s level (step B4: timed lines 1–8 s late, the t=161
  line past the end); every line but the t=1 call is now one HUD page (≤ 66 characters), keeping its meaning and
  voice. Old → new (characters):
  Varga t=14 "Talons. Gold hexes are a shield, a light one: keep firing through it." (69) → "Talons. Gold hexes are a
  light shield: keep firing through it." (62);
  Varga `first-homing` "Missiles off the Helix docks. Red-gold diamonds: shoot them or outrun them." (75) → "Helix
  missiles. Shoot the red-gold diamonds or outrun them." (59);
  Hub Control t=46 "Aegis, the vote is closing. If the lights go red, those are not my guns." (72) → "The vote is
  closing. If the lights go red, those are not my guns." (65);
  Hub Control t=60 "Block C just declared! The garrison is splitting. Their guns are live!" (70, `shout: true`) →
  "Block C just declared! The garrison is split. Their guns are hot!" (65, `shout: false`: the voice README's
  "urgent but not shouted"; "live" read "alive" in every take);
  Harker t=68 → **70.5** "Ceres Wing, Harker. Ceres stands with the Ascendancy. Clear the CDF out of our canyon." (86)
  → "Ceres Wing, Harker. We're Ascendancy now. Clear the CDF out." (60; "Ceres is Vorne's now" was tried and read
  "borns");
  Okafor t=90 "Ceres has turned, Lancer. Fight your way down the canyon; the transport waits at the outer ring."
  (96) → "Ceres has turned, Lancer. Fight your way down to the transport." (63);
  Hub Control t=101 "Aegis, Hub Control. Helix brought a CDF pilot in last month. Dock 9, a prison tender, his
  fighter in the hold." (110) → "Dock 9's prison tender holds a CDF pilot. His fighter, too." (59);
  Varga t=113 "Rook. It has to be Rook." **dropped** (folding it into Hub Control's line did not fit a page);
  Harker t=133 "Is that Rook on the Aegis net? Pity. I'd have bought you a drink." (66, two pages) → "Rook on the
  Aegis net? Pity. I'd have bought you a drink." (57);
  Rook t=141 "I know that wing. Harker leads it. I flew convoys with half of them. Doesn't matter now." (87) →
  "Harker's wing. I flew convoys with them. Doesn't matter now." (60);
  Hub Control t=151 → **156** "Aegis, the outer ring is still ours. Go. Tell Earth not all of Ceres said yes." (78) →
  "Outer ring holds. Go. Tell Earth that not all of Ceres said yes." (64; without "that" every take broke after
  "not");
  Okafor t=161 → **163.5** "You're clear of the canyon, Aegis. The transport is at the outer ring." (70) → "You're
  clear of the canyon. The transport is at the outer ring." (63).
  Retimed: Okafor t=23 → 24.5, Harker t=68 → 70.5, Varga t=78 → 79 (as asked), Okafor's Dock 9 line t=117 → **113**
  (Varga's dropped slot: at 117 it closed 4.4 s into Hub Control's t=119.9 halt line), Hub Control t=151 → **156**
  (the secondary's line at ≈ 147–148.5 takes the free radio for 7 s) and Okafor t=161 → **163.5** (closes ≈ 170.6).
  New: Okafor's line for a swap with no Rook to join (`--escort none`), "Rook, you're clear. Form on the
  transport.", and the `holders-down` cue's **`swap`** (the escort swap's outcome at the release, the simulation's
  cue subject: `bulwark` the Bulwark still flying, `joins` it was lost, `leaves` no Rook to join, `empty` Rook from
  the start), so "Bulwark, RTB" plays only for a live drone and nothing after the loaner's loss. All 12 changed or
  new lines re-rendered (Hub Control and Harker with both round 62 candidates) and read back.
- 2026-10-28 (M9 part B, step B5, pending the user's review in round 62): part B's captures (the turn, the halt and
  the rescue with the game's sound; the units, Harker, the Act 5 intro and the release's end in their parts) and
  [round 62](../../../concept-rounds/round-62/README.md) opened. The captures fly the balance plan's fit with the ship
  left mid-screen, so the halt lasts ≈ 35 s and its cycle runs twice. Found: Rook's barks play before the rescue (the
  radio ticket stays open for the fix in the next game step); the t=62 pincer's edge warning banner covers Block C
  during the turn (t=59–62).
- 2026-10-28 (user, round 62 F.1, C.1, F.3; step B6, pending the user's review): **round 62's Level 29 changes.**
  The Talon's hex shield **2 → 12** (leader 4 → 24; the [Talon](../../../enemies/air/talon/README.md)'s Decisions
  hold the measure: its life on the screen 1.49 → 1.77 s). The Asteroid Mites come in **seven rock groups** of 3–5
  rocks drifting in together, 1–2 riders a rock (a group of mixed riders is two waves at the same time): t=10 (4
  rocks), 25 (3, left edge), 43 (4, the canyon, in the t=43 Mote Swarm's slot), 69 (4, off the turned blocks, in the
  t=69 Mote Swarm's slot), 87 (5), 132 (4, right edge) and 156 (5) — 34 Mites on 10 rocks → **48 on 29**; the halt
  cycle's Mote Swarm 8 → 6, so the density holds. Before → after (`levelReport`, the plan's fit): medium 375 → 375
  enemies (124.3 a minute both), 2.61 → 2.99 on the screen, 72 → 88 shots a minute, 2 → 1 stretches over 2 s, the
  small units 1.46 → 1.73 s (0.91× → 1.08× Level 10), the typical haul 4,683 → 4,699 (+1.0 %, `bounty_scale` 0.35
  kept), the typical halt 12.4 s (`Level29Test`'s three seeds); easy 100.8 → 101.8 a minute, 2 pauses over 3 s both;
  hard 145.2 → 144.2 a minute, 2 → 1 stretches over 2 s. The autopilot wins every difficulty with no retry, Hold
  fire met 3/3. Hard without an escort (a debug fit) is the measure that held the shield at 12: see the Talon's
  Decisions. The t=62 pincer is warned **4 s** ahead (was 3), so its 2.5 s banner (y 164 px below the top edge, over
  Block C) closes at t=60.5 as the lights turn; the wave keeps its time. **Rook's barks** (step B5's finding): the
  game's `Barks` stay off until he is in his slot (`LevelScreen.rookInSlot`: not while he waits in the hold or flies
  into the slot); a bark waiting as he leaves it is withdrawn and his kills there count for no streak, and his "Miss
  me?" counts as a scripted Rook line for the barks' spacing; no radio text changed.
- 2026-10-28 (step FIX1): the music block gains the motif cue the Act 5 table marks "on" (`motif: {from: 28}`, the
  base stem's start in section 2, to the end); the theme's intro keeps its own statement under section 1.
