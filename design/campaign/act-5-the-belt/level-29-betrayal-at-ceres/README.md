---
title: Level 29 – Betrayal at Ceres
design: draft
implementation: not-started
art: none
depends-on: [../../../enemies/air/talon, ../../../enemies/ground/sam-nest, ../../../enemies/space/asteroid-mite, ../../../enemies/README.md, ../../../player/wingmen, ../../../world/belt, ../../../ui/hud, ../../../story/characters]
updated: 2026-10-27
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
rescue halt (≈ 14 s typical), ≈ 113 enemies a minute at medium.

**Status: drafted (Act 5 design phase, 2026-10-27, step `m9-d-levels-1`); everything below is pending the
user's review in round 60.** Written from the act roster, the gap analysis's recommended answers (Q1 a,
Q6 b, Q8 a, Q10 b, Q11 a, Q12 c; J2) and the wave 1 and 3 documents (the units above,
[enemy shields](../../../enemies/README.md#enemy-shields), [rock riding](../../../enemies/README.md#rock-riding),
the [belt](../../../world/belt/README.md), the [HUD](../../../ui/hud/README.md)'s enemy channel, the
[Act 5 speakers](../../../story/characters/README.md#act-5-speakers-draft)). There is **no `data.yaml`**: the
build (M9 part B) turns the hand tables into one; no number is measured.

## Briefing

Two pages, as Levels 22–28, each one screen (`BriefingLayoutTest`) with room for its image. The act intro
before it is Vorne's broadcast (Q9 a; [the act](../README.md)); this briefing follows it. Texts (draft):

> **Commander Okafor:** "Vorne has told every station in the system to choose. Ceres Hub has not answered
> yet: the Consortium is voting, and half its garrison is paid in Helix money. We fly in under the CDF
> flag and keep Helix's Talons off the docks while they vote. If the hub turns, we do not stay to argue.
> We fight our way out down the dock canyon."
>
> **Dr. Varga:** "The Ascendancy's fighters are Talons. Their hulls carry a light hex shield: it soaks the
> first hit and grows back if you let it. Their missile nests fire homing diamonds: shoot them down. And
> the belt's rocks carry Vrell mites. Shoot the rock before you pass it."

*Images* (production art in part B, [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`), briefs only now:

- Okafor's page `level-29-ceres-hub`: a top-down CDF chart of Ceres Hub's dock canyon cut into the crust,
  the docks numbered 1–12 along it, the Helix dock block in black and gold, Block C in Coalition grey, the
  outer ring and the CDF transport's rendezvous at the canyon mouth.
- Varga's page `level-29-talon`: her scan of a Talon with its hex shield drawn as an overlay, the squad
  leader's gold stripe marked, an inset of the SAM Nest's red-gold homing diamond.

*Hangar teaser* (shop screen before L29; Rook is missing, so Okafor speaks, not voiced):

> **Okafor:** "Ceres has not chosen yet, Lancer. Fly in as if it had."

The hangar before L29 sells Act 5's first gear: the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md)
(`shield-breaker`), the Shield Mk V, Composite IV and Engine Mk IV; the plan raises the Ion Beam to L4 and
fits the EMP Burst ([Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)). The
Harpoon reads `NO WATER` all act; the escort slot still shows Rook **MIA** and holds the loaner Bulwark.

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `air` (Talon wings, Skitter and Mote streams, Asteroid Mites on rocks), `ground` (SAM Nests on the station hulls, the tender's clamp guns) |
| Attack directions | front 90 % · sides 10 % |
| Density | 4 |
| Recommended traits | `spread`, `homing` |
| Hazards | The turning station (sleeping guns wake at t≈60); drifting rocks with riders |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: the belt, Ceres Hub, `air` + `ground`, front · **L1**: + directions (sides), density 4, hazard "the station turns", the objective `REACH THE OUTER RING` and the secondary `HOLD FIRE` · **L2**: + Talon (new, shielded), SAM Nest (new), Asteroid Mite (new), Skitter and Mote portraits · **L3**: + `spread` and `homing` highlighted, the wave strip marking Block C and Dock 9 |

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
  and the scroll eases up over 2 s. Typical halt ≈ 14 s (the four clamp guns, 320 HP together, at the boss
  rule's 0.6 × the reference of 120, between the halt cycle's waves).
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
facilities, Ceres's curve in the deep layer; the belt is a step lighter than Act 4 (Q11 a). Loyalist haulers
and CDF patrol craft fight Talons on the `low-air` and `deep` layers (presentation only). Vacuum rules:
explosions flash short and throw debris in a ring, wrecks drift and turn. Motion budget: the turning lights
and the woken nests are the strong elements; the canyon walls stay still.

| Section | t (s) | Scroll (px, approx.) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Ceres Approach | 0–28 | 0–3,360 | 120 | clear | `deep`: Ceres's curve, the dust band. `low-air`: a loyalist ore hauler under Talon fire (t≈13). `air`: small drifting rocks (the Mites' rocks; no large rock). `ground`: the hub's outer crust, docking arms. | Popcorn, the first Talons and their shield flash, the first riders. |
| 2. The Dock Canyon | 28–56 | 3,360–6,160 | 100 | clear | `ground`: the canyon cut into the crust, docks 1–5 along its walls (scenery, no collision); the **Helix dock block** (black and gold) with 3 SAM Nests (t≈34); Coalition blocks lit white. | The SAM Nest's homing diamonds; the canyon's rhythm. |
| 3. Block C | 56–76 | 6,160–7,360 | 60 | clear | `ground`: **Block C**, a Coalition station block lit white, with 3 **dormant** SAM Nests on its roof. At **t=60** its lights switch white → red over 1 s, a klaxon, and the nests wake (1 s tell); the lights then go red block by block up the canyon ahead. | The signature: the station turns sides. Quiet window t=56–62 (no wave enters). |
| 4. The Red Canyon | 76–116 | 7,360–11,360 | 100 | clear | `ground`: red-lit blocks, docks 6–8, 3 SAM Nests at t≈84 and 3 at t≈100; the loyalists' supply cache in Dock 6 (t≈108, the secret); mutineer Talons out of the side docks. | The mutineers; the wavering pairs (t=77, 98); Hub Control's lead to Dock 9. |
| 5. Dock 9 | 116–122 (+ the halt) | 11,360–11,780 | 100 → 0 → 120 | clear | `ground`: **Dock 9**, the Helix prison tender in four docking clamps, a clamp gun on each; the halted screen shows the tender in the middle third. | The rescue halt (clock stopped while a clamp gun stands); Rook launches, the loaner leaves. |
| 6. Breakout | 122–172 | 11,780–17,780 | 120 | clear | `ground`: the canyon mouth, the outer docks with 3 SAM Nests (t≈131), the outer ring; `air`: small rocks with riders; `deep`: the CDF transport at the rendezvous. Level end at t=172 as the outer ring reaches the bottom edge. | Rook back in the slot; the third wavering pair and Harker; the run out. |

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

- **HP** from the proposed Act 5 reference DPS (J2: **120** at L29,
  [balancing basis](../../../enemies/README.md#balancing-basis)): every unit here is new or `tiny`, so no act
  HP factor applies; the Talon and the SAM Nest take the beam-era ×2 (their documents). The clamp guns are
  the level's (*Ground targets*).
- **Density and pacing** (the Act 2+ floor from the start): **350 enemies** (waves 331 with the four halt
  waves of a typical halt + ground targets 19) over ≈ 186 s (172 s of clock + the 14 s halt), **≈ 113 a
  minute** at medium (the act's minimum 70, the target band 80–120); a wave every 3 s from t=4. Front 90 % of
  the waves (47 of 52), sides 10 % (the Talon pincers at t=63, 105, 153 and two Mite rock pairs at t=25,
  132), each **edge-warned 3 s** (`WarningEdge`). The floor comes from the Talon pairs (a pair every 1.6 s a
  Talon in the upper 300 px), the SAM salvos (at most 3 nests firing at once) and the clamp guns: expected
  to meet ≤ 3 pauses over 2 s (`PacingTest`), ≥ 2.5 enemies on the screen and ≥ 10 shots a minute
  (`ActFloorTest`), **to be confirmed with `levelReport`** in part B. The quiet window t=56–62 holds Block C
  and its waking nests only; the wavering pairs come at 77, 98 and 140 with a 6 s gap in the waves round
  each (a calm moment to read the white light). The Dart Drone is not flown here: it is first built for
  Level 30 (J4 of the L30 gap check).
- **Halt cycle** (times from the halt's start H, clock stopped): four waves, H+1 to H+13, repeated from H+17
  if a clamp gun still stands, the cycle cut the moment the last one falls (a wave already in flies on).

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) |  |
| 7 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 10 | 1 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 6 | front | 2 rocks × 3 riders in the approach debris; the Mite's first level |
| 13 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The first Talons, strafing a loyalist hauler on the `low-air` layer; the hex shield's first flash |
| 16 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) |  |
| 19 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 22 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The first squad leader (gold stripe): kill it and the wing scatters |
| 25 | 1 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 6 | side (left) | 2 rocks × 3 riders drifting in from the left edge; warned 3 s ahead |
| 28 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Into the dock canyon |
| 31 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 34 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Over the Helix dock block and its three SAM Nests |
| 37 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) |  |
| 40 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Leader at the tip |
| 43 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 46 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) |  |
| 49 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 52 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The last wave before the turn's quiet window |
| 63 | 3 | pincer | [Talon](../../../enemies/air/talon/README.md) | 4 | sides | Two pairs out of Block C's launch bays, the first mutineers; warned 3 s ahead (the turn's quiet window t=56-62 is over) |
| 66 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) |  |
| 69 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 72 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Mutineers; leader at the tip |
| 75 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) |  |
| 81 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | After the first wavering pair (t=77) |
| 84 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 5 | front |  |
| 87 | 4 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 8 | front | 2 rocks × 4 riders: hull fragments torn off the red blocks |
| 90 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) |  |
| 93 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Leader at the tip |
| 96 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 102 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | After the second wavering pair (t=98) |
| 105 | 4 | pincer | [Talon](../../../enemies/air/talon/README.md) | 4 | sides | Two pairs; warned 3 s ahead |
| 108 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 111 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 5 | front |  |
| 114 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | Over Dock 9's approach |
| 117 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The halt begins at t=120 |
| H+1 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Halt cycle, repeats while a clamp gun stands |
| H+5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Halt cycle |
| H+9 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Halt cycle |
| H+13 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | Halt cycle (the typical halt ends ≈ H+14: the cycle starts again at H+17 if the clamps still stand) |
| 123 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Rook's first fight: leader at the tip |
| 126 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 129 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Out of the canyon mouth |
| 132 | 6 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 8 | side (right) | 2 rocks × 4 riders from the right edge; warned 3 s ahead |
| 135 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 6 | front |  |
| 138 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 144 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | After the third wavering pair (t=140) |
| 147 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 7 | front | A full wing; leader at the tip |
| 150 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 153 | 6 | pincer | [Talon](../../../enemies/air/talon/README.md) | 4 | sides | Two pairs; warned 3 s ahead; Rook's flank bark |
| 156 | 6 | swarm (on rocks) | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 6 | front | 2 rocks × 3 riders |
| 159 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) |  |
| 162 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The last wing; leader at the tip |
| 165 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | The last wave |

Totals (a typical 14 s halt): Mote Swarm 126 · Skitter 90 · Talon 81 · Asteroid Mite 34; the wavering
Stormhawks (6) are not enemies.

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 2 | [SAM Nest](../../../enemies/ground/sam-nest/README.md) ×3 on the Helix dock block (t≈34; x 90, 140, 400) | Bounty 15 each |
| 3 | SAM Nest ×3 on **Block C**, **dormant until t=60** (x 120, 240, 360; at the turn 100–220 px below the top edge) | Dark, white-lit, not shootable and not picked until they wake (the SAM Nest's *dormant until t*); bounty 15 each |
| 4 | SAM Nest ×3 on red blocks (t≈84) and ×3 (t≈100) | Bounty 15 each |
| 4 | The loyalists' supply cache in Dock 6 (a `ground` trigger, 20 HP, t≈108, x 430) | The secret: a crate worth 100 (655) |
| 5 | **Clamp gun** ×4 on the prison tender's docking clamps (the halted screen's middle third: x 150 and 330, y 170 and 330) | Rook's rescue: the halt ends with the last one; bounty 10 each |
| 6 | SAM Nest ×3 on the outer docks (t≈131) | Bounty 15 each |

**Clamp gun** (level-owned target, `ground`, `medium`, not hardened; a turret on a docking clamp): 80 HP
(easy 60 / hard 104, from the global multipliers); an `aimed` single shot every 2.0 s (standard 160 px/s,
`small` = 4) with a 0.3 s red muzzle glint; destroyed, the clamp springs open with a burst of gas and sparks.
15 SAM Nests and 4 clamp guns: 19 ground enemies. At most 3 nests fire at once (the SAM Nest's rule):
Block C's three wake as the Helix block's have left the screen.

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

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Ceres Hub Control | "Aegis flight, Ceres Hub Control. You heard him too. The Consortium is still voting. Keep our docks clear while they talk." |
| t=14 | Varga | "Talons. Gold hexes are a shield, a light one: keep firing through it." |
| t=23 | Okafor | "The striped one leads the wing. Drop it and the rest break off." |
| First SAM salvo (`first-homing`, t≈35) | Varga | "Missiles off the Helix docks. Red-gold diamonds: shoot them or outrun them." |
| t=46 | Ceres Hub Control | "Aegis, the vote is closing. If the lights go red, those are not my guns." |
| t=60 | Ceres Hub Control | "Block C just declared! The garrison is splitting. Their guns are live!" |
| t=68 | Harker (enemy channel) | "Ceres Wing, Harker. Ceres stands with the Ascendancy. Clear the CDF out of our canyon." |
| t=78 | Varga | "Those white blinkers are holding fire, Lancer. Let them go." |
| A wavering pair leaves (event, the first time) | Harker (enemy channel) | "Who broke off? Ceres Wing, get back in line!" |
| t=90 | Okafor | "Ceres has turned, Lancer. Fight your way down the canyon; the transport waits at the outer ring." |
| t=101 | Ceres Hub Control | "Aegis, Hub Control. Helix brought a CDF pilot in last month. Dock 9, a prison tender, his fighter in the hold." |
| t=113 | Varga | "Rook. It has to be Rook." |
| t=117 | Okafor | "Dock 9 ahead. Break the clamps, Lancer. Bring him home." |
| The halt begins (event) | Ceres Hub Control | "Four clamps, a gun on each. Break them and the tender lets go." |
| The last clamp gun falls (event, first) | Okafor | "Bulwark, RTB. Rook, form on Lancer." |
| The last clamp gun falls (event, after Okafor's) | Rook | "Miss me? Don't answer that. Just shoot." |
| t=133 | Harker (enemy channel) | "Is that Rook on the Aegis net? Pity. I'd have bought you a drink." |
| t=141 | Rook | "I know that wing. Harker leads it. I flew convoys with half of them. Doesn't matter now." |
| Secondary met (event, the third pair gone, ≈ t=147) | Varga | "None of the white ones fell. Someone in Ceres will remember that." |
| Secondary failed (event, a wavering craft destroyed) | Harker (enemy channel) | "That one held fire, Aegis. Remember that." |
| t=151 | Ceres Hub Control | "Aegis, the outer ring is still ours. Go. Tell Earth not all of Ceres said yes." |
| t=161 | Okafor | "You're clear of the canyon, Aegis. The transport is at the outer ring." |
| Level end | Okafor | "Ceres is lost, but we have Rook back. I'll take that trade." |

Timeline check (text): the timed lines close at ≈ 13.0, 21.6, 30.1, (43.1), 53.9, 67.7, 77.1, 84.8, 99.9,
112.1, 116.9, 123.5 (into the halt: the clock stops at 120, the line plays on), 140.3, 150.2, 159.4 and
168.7; nothing overlaps. Rook's t=141 line is his Act 5 mutineer line from [Rook](../../../story/characters/rook/README.md)
with Harker's name; his pulp one-liner is the rescue line. With `--escort none` (the Ember launches and
leaves upward) Okafor's rescue line is "Rook, you're clear. Form on the transport." and Rook's lines still
play (he flies elsewhere in the formation).

## Music & ambience

The Act 5 A theme **"Hollow Rock"** ([track 12](../../../audio/music/README.md#track-list), chosen in round
03; full length and stems in M9 part B): its intro (radio static and Vorne's motif in cold brass and organ)
under the approach, the **base stem from section 2** (the canyon), the **full mix from the turn at t=60**
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
secondary 100), × the act factor **6.5536** (1.6⁴), bounties also × the scale. A **`bounty_scale` of 0.44**
(the earlier acts' model: typical = 0.663 of the kills, 0.8 of the ground targets, half of the secret and
the secondary; a first estimate for part B to confirm with `TypicalHaulTest` and `levelReport`) puts the
typical haul at **4,662 (+0.2 %)**, the perfect run at 7,196. No boss or mid-boss.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Mote Swarm 126 × 2 + Skitter 90 × 5 + Talon 81 × 12 + Asteroid Mite 34 × 3 (a typical halt's four halt waves included) | 5,121 | 3,395 |
| Ground targets: SAM Nest 15 × 15 + clamp gun 4 × 10 | 764 | 611 |
| Secret: the loyalists' supply cache (hidden crate) | 655 | 328 |
| Secondary: Hold fire | 655 | 328 |
| **Total** (bounty scale 0.44) | **7,196** | **4,662** |
| Budget(n) = the typical haul's target; typical +0.2 %, perfect 1.55 × budget | | 4,654 |

The squad leaders' extra 13 each (eight V-wings) and a longer halt's extra waves add to the perfect run; the
build counts both. **Balance plan**: the plan's L29 visit raises the Ion Beam to L4 and fits the EMP Burst
([Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)); the level expects `spread` and
`homing` and is flyable without them.

## Difficulty notes

- **Easy**: the halt cycle without its Talon wave; the clamp guns fire every 2.6 s; the wavering pairs hold
  8 s (more time to read the white light); the unit documents' easy hooks.
- **Hard**: the halt cycle adds a Talon pincer (2 + 2) at H+11; the clamp guns fire `aimed` pairs; the
  wavering pairs fly 40 px lower, among the waves; the other waves by the formation lever, trimmed by hand so
  never more than two Talon wings are on the screen.

## Concept art

No art yet. Round 60 proposals (wave 5): the belt mood still for Ceres Hub (ART-P-world, in the
[belt](../../../world/belt/README.md#concept-art)), the wavering Stormhawk in Ceres Wing's colours (ART-P-world,
under the [act](../README.md)'s `concept/`), the radio portraits of Ceres Hub Control and Harker (their level
part, M9 part B), the unit sheets in their documents. Production backdrop (the dock canyon, Block C's two
lighting states, Dock 9 and the tender), the clamp guns and the briefing images come in part B.

## Implementation

- [ ] Level data `data.yaml` (M9 part B): sections and speeds with 2 s eases, waves and the halt cycle, ground
      targets, radio, budget, threat profile, briefing, backdrop, music
- [ ] Block C's turn: three SAM Nests dormant until t=60 (dark, not shootable, not picked), the lights white →
      red over 1 s and up the canyon ahead, the klaxon, the 1 s wake tell
- [ ] The rescue halt: the scroll halts at t=120, the level clock stops while a clamp gun stands, the halt
      cycle repeats and is cut at the last clamp; the four clamp guns as ground targets
- [ ] The mid-level escort swap ([wingmen](../../../player/wingmen/README.md#rooks-rescue-and-return-l29l30)):
      the Ember launches, the drone peels away, Rook joins over 1 s at full armour; `--escort rook:`, `--escort
      none` and the mission-select replay as that document says; a retry restarts Rook MIA
- [ ] The "Hold fire" secondary: three wavering pairs (path, IFF blink, red hit flash, no fire, no contact),
      never picked by homing shots or Rook, Rook's and the Bulwark's shots pass through; `HOLD FIRE` counter;
      met when the third pair is gone with none destroyed, failed at the first destroyed
- [ ] Wave script: ≥ 70 enemies a minute at medium (`DensityTest`), the Act 2+ floor (`PacingTest`,
      `ActFloorTest`), the quiet window t=56–62, edge warnings for every side entry
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel29Test`, with the loaner, an owned
      Bulwark and `--escort none`); Ceres Hub Control and Harker cast; Harker on the enemy channel; no Rook line
      before the rescue
- [ ] Threat profile, Varga's four intel lines and Okafor's teaser fitting the intel panel
      (`IntelPanelLayoutTest`); the Talon, SAM Nest and Asteroid Mite portraits
- [ ] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(29) = 4 654 with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music and ambience: "Hollow Rock" by section, the `belt-station` bed, the klaxon, the clamp and launch
      sounds (`Level29SoundsTest`)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level on every difficulty with the
      loaner and with `--escort none`, breaking the clamps and keeping the wavering craft alive in most runs
- [ ] Production art and captures for part B's round

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

- 2026-10-27 (Act 5 design phase, step `m9-d-levels-1`; **pending the user's review in round 60**): promoted from
  the act roster to a draft level document, hand tables only, no `data.yaml`. Confirmed the gap analysis's
  timings: the turn at t=60 (Block C, three dormant SAM Nests, a 6 s quiet window), the rescue halt at t=120
  with the level clock stopped while the four clamp guns (80 HP each) stand; the "Hold fire" secondary with
  three wavering Stormhawk pairs (Q10 b), not counted as enemies; ≈ 113 enemies a minute, `bounty_scale` 0.44.
  Speakers per the Act 5 speakers table: Ceres Hub Control friendly and the one who points to Dock 9, Harker
  (the mutineers' flight lead, a name of this draft) on the enemy channel, Vorne silent in the level; no Dart
  Drones here (J4: first built for L30).
