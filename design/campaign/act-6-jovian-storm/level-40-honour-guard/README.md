---
title: Level 40 – Honour Guard
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/bosses/honour-guard, ../../../enemies/air/talon, ../../../enemies/air/chimera, ../../../enemies/air/mirror-interceptor, ../../../enemies/air/harrow, ../../../enemies/air/gilded-gunship, ../../../enemies/air/hornet, ../../../enemies/air/dart-drone, ../../../enemies/air/skitter, ../../../enemies/air/mote-swarm, ../../../world/jovian, ../../../player/wingmen, ../../../systems/economy]
updated: 2026-10-31
---

# Level 40 – Honour Guard

## Summary

The road to Callisto. Aegis Wing leaves Jupiter's shadow and flies the Callisto approach in vacuum, through
Vorne's picket line: Talon wings, Chimera swarms, Gunships and their Darts, Mirror Interceptors from the flanks and
from behind, Harrows from every edge, and the Vrell still hunting their old allies. Then the waves stop and the
[Honour Guard](../../../enemies/bosses/honour-guard/README.md) takes the lane: Vorne's three aces in gold Talons,
the sniper Harker first, then the dancer, then the feinter from behind, each with a taunt on the enemy channel and
Rook answering. After the last ace the run goes on over Callisto's orbital ring to the citadel's outer line. About
170 s of level clock plus a ≈ 76 s duel; ≈ 113 enemies a minute; air only (`NO GROUND`). Density 5.

**Status: approved 2026-10-31 (round 72, D.6)** (drafted in the Act 6 design phase, step `m10-d-levels-3`, 2026-10-31). Written from the act roster, the gap analysis's recommended answers (**Q5 a** the staggered three-ace
fight, **Q3 a** the three-way war, **Q9 a** the elite livery, **Q10 a** the Level 40 mood; judgement calls **J1** the
reference DPS 160, **J2** the medium and `NO GROUND`, **J4** the aces, **J5** no new tiny unit, **J10** Okafor on the
carrier, all decided 2026-10-31 (round 72)) and the wave 1 and 2 documents. The mid-boss document owns the duel (the aces,
their joins, playbook, tells, HP, the bounty's share and their lines' wording); this document owns the approach, the
run-out, the timeline, the radio's order and delays and the economy. Every number is a hand-written first draft at
medium for M10's Level 40 part to measure with `levelReport`; there is no `data.yaml` yet.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

> **Commander Okafor:** "Callisto's approach is held by Vorne's pickets and by his Honour Guard: three aces in
> gold Talons. Break the pickets, then the guard. The fleet follows your lane, Lancer."
>
> **Dr. Varga:** "The pickets fly hybrids and mirrors. The aces start one by one and end together: a sniper, a
> dancer, and one who comes from behind. Keep your rear guns fitted."

*Images* (production art in M10's Level 40 part; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-40-callisto-approach`: a tactical chart of the Jovian system's edge: Jupiter's limb, Callisto's
  disc, the fleet's track from Io, the picket line as gold contacts across it, a lane marked in white, three gold
  chevrons at its far end.
- Varga's page `level-40-honour-guard`: her sheet of the three aces from above, each Talon silhouette with its mark
  (the sniper's barrel and red nose band, the dancer's white ribbons, the feinter's chevrons and red tail band) and an
  arrow for where each enters (top, a side, behind).

*Hangar teaser* (shop screen before L40; Okafor, not voiced):

> **Commander Okafor:** "Vorne's guard waits at Callisto. Fit something that shoots behind you."

## Threat profile

Draft; directions are counted by each wave's `from` (the intel's rule): 47 of 58 waves from the front (81 %), 6
from the sides (10 %), 4 from the rear (7 %), 1 from all four edges (2 %). The aces are a mid-boss and are not in the
numbers.

| Field | Value |
|---|---|
| Dominant layers | `air` only (`space` is `air` in vacuum, the [Jovian media](../../../world/jovian/README.md#media-per-level)): the pickets and the Honour Guard. No ground layer: the Bomb Rack and Rook's Mortar read **`NO GROUND`** (J2; the hangar warns) |
| Attack directions | front 81% · sides 10% · rear 7% · all edges 2% (the duel adds the aces' side and rear entries) |
| Density | 5 |
| Recommended traits | `spread` (three aces apart, wide pickets), `rear` (the rear V-wings, the Mirror Interceptors' rear ambush, Kestrel's station); `homing` helps against the moving aces |
| Hazards | Harrow death-burst rings; the Mirror Interceptors' bounced shots; the aces' sniper line and dives; no weather (vacuum) |
| Boss / mid-boss | Mid-boss [Honour Guard](../../../enemies/bosses/honour-guard/README.md) |
| Sensor-suite detail | **none**: the Callisto approach, `air` only, front, sides, rear. **L1**: + the directions, density 5, hazards "death-burst rings" and "sniper line", the objective `BREAK THE HONOUR GUARD`. **L2**: + Talon, Chimera, Gilded Gunship, Hornet, Mirror Interceptor, Harrow, Dart Drone, Skitter, Mote Swarm portraits; the mid-boss's name and three silhouettes (`honour-guard`). **L3**: + `spread` and `rear` highlighted; the timeline strip marks the Harrow cross (65 s) and the duel (102 s); 1 secret. |

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Callisto approach. Air only, no ground at all." |
| L1 | "Pickets from front, sides and rear. Then the guard." |
| L2 | "Chimeras, Mirrors, Harrows. Three gold Talons." |
| L3 | "Spread for the three aces, rear guns for the third." |

## Objective

- **Primary** `reach-end` with the duel as its condition (Level 19's mid-boss rule: the level cannot go on while an
  ace lives): reach Callisto's outer line. The briefing and the objective line read `BREAK THE HONOUR GUARD`; the
  mid-boss bar reads `HONOUR GUARD` with its three segments ([HUD](../../../ui/hud/README.md#act-6-additions-draft)).
  The gap analysis drafted a `boss` primary; a mid-boss in the middle of a level is Level 19's and Level 11's
  `reach-end` (decided 2026-10-31 (round 72)).
- **Secondary** "Break the pickets" (`kill_ratio`, the 65 % rule): destroy at least **65 %** of the level's enemies
  (**209 of 321** at medium, the Gunships' Darts included), judged at the level's end. The aces are not counted
  (`boss_counted: false`), nor Talons the Vrell kill (prey; the [three-way rule](../../../enemies/README.md#the-three-way-war)).
  Pays **100** in Act 1 terms (1,049 in Act 6); the typical haul counts it at 50 %. No radio line. (The gap analysis
  drafted none; every Act 5 level and every act boss level has one, so the act keeps the rule; decided 2026-10-31 (round 72).)

## Layout

Scroll **160 px/s** in open space, eased to the backdrop's **20 px/s drift** for the duel (space has no ground to
halt: the [mid-boss's arena](../../../enemies/bosses/honour-guard/README.md#arena)), then back up to 160. Setting: the
[Jovian system](../../../world/jovian/README.md#sub-locations-per-level)'s Callisto approach, vacuum
([vacuum rules](../../../world/belt/README.md#vacuum-rules): explosions flash short and throw debris in a ring, no
rising smoke). **Light** ([mood](../../../world/jovian/README.md#moods-per-level-q10), Q10 a): black space, Jupiter's
cream-orange limb at the bottom edge in section 1 shrinking to a disc at the right edge, Callisto's dark blue-grey disc
growing at the top from section 3, the CDF fleet's lights (Okafor's carrier group) on the `deep` layer. Motion budget:
the waves are the strong elements; the backdrop only drifts. No ground layer, no `low-air` traffic in the duel (three
aces are enough to read).

**The duel and the clock.** The last approach wave enters at t=93; no wave after it. The **mini-boss sting** (track
21), the bar `HONOUR GUARD` (three segments, hatched) and the **boss checkpoint** (none on hard) come at **t=102**,
at the start of the one-second section *The Guard's Lane* (`arena: true`, the scroll eased to its 20 px/s drift over
its first second); Harker's entry tell starts 1 s later. The level clock **halts at t=103** while an ace lives (the
boss rule); `hold_after_boss: 4` (the last ace's 2 s chain, the credit shower, 2 s more) keeps it halted after the
kill, then the clock resumes at t=103 and the scroll eases up to 160 px/s over 2 s. The first wave after the duel
enters at **t=107**: **≥ 4 s of calm** after the last ace ([the death](../../../enemies/bosses/honour-guard/README.md#death)).
**K** below is the last ace's death.

**Rook and the escort.** Rook flies his slot ([Rook in Act 6](../../../player/wingmen/README.md#rook-in-act-6-draft)) and
follows the mid-boss's [escort rules](../../../enemies/bosses/honour-guard/README.md#the-escort-rook) (the nearest ace,
out of a told line, 40 px off a dive). With `--escort none` the level is the same without him and his lines.

| Section | t (s) | Scroll (px) | Speed (px/s) | Layers and content | Purpose |
|---|---|---|---|---|---|
| 1. Jupiter's Shadow | 0–30 | 0–4,800 | 160 | `deep`: Jupiter's limb filling the bottom edge, its night side, the fleet's lights rising off it. | Talons, Chimeras and popcorn; the Vrell hunting Talons (t=15.5). |
| 2. The Picket Line | 30–64 | 4,800–10,240 | 160 | `deep`: Jupiter shrinking to a disc at the right edge; a picket station far off (scenery). `low-air`: a dead CDF scout drifting (t≈50, the secret's source). | Gunships and Darts, the first Mirror pincer (t=35.5), the first rear V-wing (t=48). |
| 3. Callisto Rising | 64–96 | 10,240–15,360 | 160 | `deep`: Callisto's dark disc rising at the top, its ice catching Jupiter's light. | The Harrow cross (t=65.5), the Mirror rear ambush (t=75.5), the last pickets (t=93). |
| 4. The Guard's Lane | 96–102 | 15,360–16,040 | 160 → 20 | `deep`: Callisto larger; three gold glints far ahead. The scroll at 160 until t=98, eased to the 20 px/s drift by t=102. | The pre-duel pause (t≈97–102); Varga names the aces. |
| 5. The Duel (arena) | 102–103 | 16,040–16,060 | 20 (drift) | `deep`: as section 4, drifting. | The sting at t=102, the duel (≈ 76 s at medium); the clock halted while an ace lives. |
| 6. Callisto's Ring | 103–140 | 16,060–21,840 | 20 → 160 | `deep`: Callisto's orbital ring (black and gold relay platforms, scenery) crossing below; the fleet closing behind. | Calm to t=107, then the waves resume: the ring's defenders. |
| 7. The Ice Line | 140–170 | 21,840–26,640 | 160 | `deep`: Callisto's ice filling the lower half, the black citadel's lights at the top edge by t=165. | The last waves; the level ends at t=170 as the citadel's outer line reaches mid-screen. |

The pre-duel pause and the calm after the duel are the level's planned empty stretches (see *Waves*).

## Waves

Formation names from the [formation vocabulary](../../../enemies/README.md#formation-vocabulary). The **act HP
factor** for the returning `medium`+ units (the proposed reference **160** at Level 40 against the unit's first
level, J1): Gilded Gunship ×160/124 (88 → 114; its [Act 6 shield](../../../enemies/README.md#enemy-shields) 24 → 31),
Hornet ×160/128 (96 → 120, shield 24 → 30), Mirror Interceptor ×160/148 (120 → 130; its mirror's heat by the same
factor). The `small` units (Talon with its Act 6 shield, Chimera, Harrow) keep their pages' HP with the beam era's ×2,
the `tiny` ones (Darts, Skitters, Motes) 1 HP. Every number is a hand estimate for `levelReport`.

- **Density and pacing** (the Act 2+ floor): **321 enemies at medium** over the 170 s of level clock (the duel
  excluded): the waves' 285 and the Gunships' **36 Darts** they launch (counted as the
  [Dart Drone](../../../enemies/air/dart-drone/README.md) doc says), **≈ 113 a minute** (the act's band 90–120). A wave
  every 2.5 s; the Talons' pairs, the Chimeras' pairs, the Gunships' rings, the Hornets' salvos and the Mirrors'
  bursts for the shots. **Empty stretches over 2 s** (at most three): the pre-duel pause (t≈97–102), the calm after
  the duel (t=103–107) and the end's calm (t≈164–170, the ≥ 4 s after the last enemy). The pacing checks must name them.
- **Tiny units** (J5): the Gunships' Darts, the Vrell's Skitters and Mote Swarms carry the count; the act's variety row
  needs them.
- **The three-way war** (Q3 a): the Skitter snakes of t=15.5 and t=114.5 are **hunters** of the Talon waves of t=14 and
  t=112 (prey): they ram the Talons, and a Talon the Vrell kill pays nothing
  ([the rule](../../../enemies/README.md#the-three-way-war)). The Chimeras (hybrids) are never prey.
- **Hulls apart**: the Mirror pincers keep ≥ 80 px between hulls; the t=58 pincer (2 + 2) enters after the t=35.5 pair
  has left (it holds 7 s); never two Gunship columns on the screen.
- **Directions**: 47 of 58 waves from the front (81 %); the side pincers and streams have edge warnings, the rear
  V-wings and the Mirror rear ambushes a BOTTOM warning, the Harrow cross every edge, 3 s ahead.
- **Totals**: Talon 86 (10 leaders) · Mote Swarm 72 · Chimera 50 · Skitter 36 · Gilded Gunship 6 (+ 36 Darts) ·
  Harrow 18 · Mirror Interceptor 11 · Hornet 6.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Jupiter's limb below; the Talons' Act 6 shields flash on the first hits |
| 6.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 9 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front (left) | |
| 11.5 | 1 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 14 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front (right) | Prey of the Skitters of t=15.5 |
| 15.5 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Vrell hunters (ram) of the t=14 Talons; Rook's line at t=21 |
| 18 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 20.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 23 | 1 | pincer | [Talon](../../../enemies/air/talon/README.md) | 6 | sides | Edge warnings at t=20 |
| 25.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 28 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | No leader (a hybrid wing) |
| 30.5 | 2 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | Each launches Dart streams (3 every 5 s, at most 6 alive): ≈ 12 Darts from the pair |
| 33 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 35.5 | 2 | pincer | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 2 | sides | Edge warnings at t=32.5; 1 + 1 |
| 38 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 40.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 43 | 2 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 45.5 | 2 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 48 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | rear | From behind: BOTTOM warning at t=45 |
| 50.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | The dead scout drifts in at the left edge (the secret) |
| 53 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 55.5 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 58 | 2 | pincer | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 4 | sides | Edge warnings at t=55; 2 + 2, ≥ 80 px between hulls |
| 60.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | |
| 63 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 65.5 | 3 | cross | [Harrow](../../../enemies/air/harrow/README.md) | 8 | all edges | 2 per edge, warned on every edge at t=62.5 |
| 68 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 70.5 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | ≈ 12 Darts |
| 73 | 3 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 75.5 | 3 | rear ambush | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 3 | rear | BOTTOM warning at t=72.5; each in its lane, ≥ 80 px apart |
| 78 | 3 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 80.5 | 3 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 83 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | sides | From alternating sides, edge warnings at t=80 |
| 85.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 88 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 90.5 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | |
| 93 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The last approach wave: the pre-duel pause t≈97–102 |
| 102 | 5 | solo set piece | [Honour Guard](../../../enemies/bosses/honour-guard/README.md) | 3 | top, then a side, then the rear | The sting, the bar, the checkpoint; each ace's own entry tell (no edge warning); not in the density |
| 107 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The first wave after the duel (≥ 4 s of calm) |
| 109.5 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 112 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front (right) | Prey of the Skitters of t=114.5 |
| 114.5 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Vrell hunters (ram) of the t=112 Talons |
| 117 | 6 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | ≈ 12 Darts |
| 119.5 | 6 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 122 | 6 | pincer | [Talon](../../../enemies/air/talon/README.md) | 6 | sides | Edge warnings at t=119 |
| 124.5 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 127 | 6 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 129.5 | 6 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | |
| 132 | 6 | rear ambush | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 2 | rear | BOTTOM warning at t=129 |
| 134.5 | 6 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 137 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 139.5 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 142 | 7 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | sides | Edge warnings at t=139 |
| 144.5 | 7 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | rear | BOTTOM warning at t=141.5 |
| 147 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 149.5 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 152 | 7 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 154.5 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 157 | 7 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The last wave; the end's calm from t≈164 |

## Ground targets

None: the level has no ground layer (J2). The picket station, the orbital ring's relay platforms and the citadel are
`deep` scenery.

## Hazards

- **The pickets**: the Harrows' death-burst rings (8 slow bullets on every death on the screen, the shot, a ram,
  Rook's kill: thin them far out), the Mirror Interceptors' bounced shots (the player's own fire, magenta, slow), the
  Gunships' rings and Dart streams, the Hornets' homing salvos. No terrain, no weather (vacuum), no lightning.
- **The duel**: the aces' sniper line, fans, spiral loops, ribbons, feint dives and rear rakes under the conductor's
  limits ([the playbook](../../../enemies/bosses/honour-guard/README.md#the-playbook)).
- **Specials** ([Jovian media](../../../world/jovian/README.md#media-per-level)): the Airstrike flies (CDF bombers from
  Okafor's carrier) and the Orbital Lance has cover; their boss rules cap them per ace; the EMP Burst empties every
  shield on the screen (the aces' three included, never a stun), shatters the mirrors and stuns the hybrids and
  Gunships; the Harpoon, the Depth Charges and the Sonar Pulse read `NO WATER`; the Bomb Rack reads `NO GROUND`.
  The IFF spoofer fools nothing here (no turret; the aces are pilots).

## Secrets and pickups

- **Sentry Two's recorder** (the secret, t≈50, x≈70, drifting down the left edge): the flight-recorder pod of a CDF
  scout lost at Callisto in 2184, still blinking, tumbling with its dead scout on `low-air`. A small breakable object on
  the play plane (Level 30's buoy rule: its housing takes **4 hits**, a beam's touch one hit per 0.25 s); broken, its
  housing frees a hidden crate worth **100** in Act 1 terms (1,049 in Act 6). The Salvage scanner's glint shows it at
  any range. Its event line, Okafor: "*That's Sentry Two's recorder. Our last scouts out here. Bring it home.*"
- **Overdrive**: dropped by the leader of the t=38 V-wing.
- **Shield cell**: dropped by the second Gunship of the t=70.5 column.
- **Armour patches** ×2: a CDF supply drone (a small white craft on `low-air`, marked friendly) releases one at
  (240, 380 y-up) at t=97, in the pre-duel pause; a second at t=145 after the duel.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), draft text for the **1 s rule** (12 characters a second plus a
1.5 s lead; 0.4 s of silence); event lines drop after 6 s. Rook's lines carry `requires: escort`. **Speakers:
Okafor** (from the carrier, J10), **Varga**, **Rook** on the friendly channel; **the three aces** (Harker, Vesper,
Kestrel; J4, call signs and voices the story step's) on the **enemy channel** (the HUD's amber plate, Act 5's
intercepted dress) ([characters](../../../story/characters/README.md)). Vorne and the Choir are silent. The duel's clock
halts, so its lines are events (`boss-arrival` and `boss-phase` per ace), queued as `PHASE` lines (they wait for a gap,
stale after 20 s, dropped at the kill). **The aces' lines are the mid-boss document's**
([lines](../../../enemies/bosses/honour-guard/README.md#lines), wording decided 2026-10-31 (round 72)): this table places them, adds
Rook's rivalry between them and sets the delays. Rook's rivalry wording is a draft for the story step (J4).

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Callisto's picket line, Lancer. Vorne's own guard flies it. Clear a lane for the fleet." |
| t=12 | Varga | "Chimeras in the pickets. They heal if you let them rest: hold the beam on one." |
| t=21 | Rook | "The bugs are still chewing on their old friends. Fine by me." |
| t=33 | Varga | "Mirror interceptors on the flanks. Hit them from the side, or burn the mirror." |
| t=44.5 | Okafor | "Contacts behind you, Lancer. Rear guns." |
| Recorder broken (event) | Okafor | "That's Sentry Two's recorder. Our last scouts out here. Bring it home." |
| t=62.5 | Varga | "Harrows from every side. They burst when they die: thin them far out." |
| t=76 | Rook | "Callisto. Looks cold. Looks like it's waiting for us." |
| t=84 | Okafor | "Long-range scan: three gold Talons holding the lane ahead. The Honour Guard." |
| t=92.5 | Varga | "Vorne's aces. One snipes, one dances, one comes from behind. Watch all three." |
| Harker's entry tell (`boss-arrival`) | Harker (enemy channel) | the mid-boss document's entry taunt ("Rook. They let you fly again?…") |
| After it (`PHASE`) | Rook | "Harker. Still flying for whoever pays the most?" |
| Vesper's entry tell (`boss-phase`) | Vesper (enemy channel) | the document's entry taunt |
| Kestrel's entry tell (`boss-phase`) | Kestrel (enemy channel) | the document's entry taunt |
| The first ace's death (`boss-phase`) | the survivor the document names | the document's line |
| Harker's death, whenever it comes (`boss-phase`) | Rook | "That one's for Ceres, Harker." |
| The second ace's death (`boss-phase`) | the last ace | the document's line |
| The last ace's death (K, `boss-destroyed`, `delay: 2.5`) | Rook | "Three gold Talons. Vorne needs new friends." |
| Then (without Rook: replaces his line) | Okafor | "The Honour Guard is down. Well flown, Lancer." |
| t=108.5 | Okafor | "The guard is gone and the ring is open. Push through to the ice." |
| t=125 | Varga | "Helix HQ is under that ice. I had an office there, once. Three floors down." |
| t=138.5 | Rook | "Bursters again. Shoot them far out, not on top of us." |
| t=158 | Okafor | "That's the citadel's outer line. Form up, Aegis. Callisto is next." |

Timeline check (text model): t=1 closes ≈ 9.7, t=12 ≈ 20.1, t=21 ≈ 27.5, t=33 ≈ 41.0, t=44.5 ≈ 49.3; the recorder
(in reach t≈50–56) is spoken by ≈ 57.6 at the latest, before Varga's t=62.5 (≈ 69.8); t=76 ≈ 81.9, t=84 ≈ 91.9, t=92.5
≈ 100.4, before the sting at t=102. In the duel the entry taunts ride their tells (2.5 s ahead of each entry) and
Rook's answer follows Harker's; the death lines queue behind any line in progress. The last ace's line pair (K+2.5 to
≈ K+7.6) runs over the clock's resume at K+4 (t=103) and closes before Okafor's t=108.5 (≈ 116.1); t=125 ≈ 132.8,
t=138.5 ≈ 144.5 (the Harrow stream at 142, warned at 139), t=158 ≈ 165.1, before the level's end at t=170.
`RadioTimelineTest` checks it with and without Rook.

**Story notes.** Harker is Level 29's mutineer flight lead, promoted into Vorne's guard after Ceres (J4): Rook's
rivalry gets a face, and his "for Ceres" line closes Level 29's prison tender. The Vrell still eat their old allies
in the pickets (Q3 since Level 37). Varga's office line sets up Level 41 (she worked at Helix until 2179,
[Varga](../../../story/characters/varga/README.md)). Vorne does not speak: his guard speaks for him, and his silence
after the last ace is the point.

## Boss / mid-boss

Mid-boss: [Honour Guard](../../../enemies/bosses/honour-guard/README.md) (Q5 a, staggered, then together). Its aces,
joins, stations, playbook and conductor, tells, HP, the escort's rules, the death and the aces' lines live in the
spec. Arena notes for this level:

- The duel starts at t=102 with the sting, the bar and the checkpoint (none on hard); no wave enters from t=93 on, so
  the field is clear when Harker's tell shows (the spec's warning before any ace shows: each ace's own tell, no edge
  warning). Nothing slides in during the duel: no popcorn (the spec).
- **Traits**: `spread` (three bodies apart) and `rear` (Kestrel's station in the ship's band); the plan's Swarm Tail L3
  (30 rear DPS) is the rear answer, Rook takes the nearest ace.
- **Bounty**: the spec's formula (15 % of budget(40) ÷ (10.486 × `bounty_scale`)) with this level's hand scale
  **0.39** (below) gives **360 in Act 1 terms, 120 an ace**, paid **1,472 = 15.0 %** of budget(40). The spec assumed
  0.59 (237); the main agent carries the change to the spec. The build recomputes it with the measured scale.
- **The plan's fit** (the [Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft)'s L40 row: the
  Ion Beam L5, the Tesla Coil Pod L2 on the left wing, a Micro-missile Pod L4 on the right, the Swarm Tail L3, 22 of
  22 MW; Rook's Autocannon) flies the duel at the spec's paper time (≈ 76 s, par 75). The Tesla pod empties an ace's
  shield on a hit; the EMP Burst empties all three.

## Music & ambience

Draft for the audio step (D-audio owns the [Act 6 music plan](../../../audio/music/README.md#track-list)):
[track 14](../../../audio/music/README.md#track-list), the Act 6 A theme "Eye of the Storm": the **base stem** in
section 1, the **full mix from the picket line** (t=30), Vorne's motif **on** (his guard's level), out over the
pre-duel pause (fading t=96–100); the **mini-boss sting** (track 21) at t=102, then the **duel theme** (track 32, the
audio step's) through the duel; after the last ace track 14's full mix returns with the waves at t=107, out with the
level's end. The debrief plays the `mission-complete` jingle (23). Ambience: the `belt-open` bed (open space heard
through the hull, Act 5's; [sfx](../../../audio/sfx/README.md#ambience-per-setting)) throughout; the fleet's distant comms and engine rumble under section 1.
New sounds this level needs: the aces' (the spec's [brief](../../../enemies/bosses/honour-guard/README.md#sounds-brief)),
the units' (their pages), the recorder's crack (a small metal housing splitting with an electrical fizz). Freesound
only for real-world sounds (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(40) = 700 × 1.07³⁹ ≈ **9,796** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act 1
terms** (Talon 12, leader 25, Mote Swarm 2 a mote, Chimera 14, Skitter 5, Gilded Gunship 28, Dart Drone 2, Mirror
Interceptor 30, Hornet 26, Harrow 12, the aces 360, the crate 100, the secondary 100), × the act factor **10.486**
(1.6⁵), bounties also × the level's scale. At ≈ 113 a minute a **`bounty_scale` of 0.39** puts the typical haul on the
budget (**9,876, +0.8 %**), the perfect run at 1.67 × (estimated by hand with the economy's shares: 60 % of the air
kills and launched Darts, half the secret and the secondary, the mid-boss at 100 %); `TypicalHaulTest` confirms it in
the build. The mid-boss keeps its **15 %** share (the band 10–20 %, `BalanceTest`). The scale is just under the gap
analysis's band (0.40–0.56): a long level with a mid-boss earns most of its haul from its 321 enemies.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Talon 86 × 12 + Mote Swarm 72 × 2 + Chimera 50 × 14 + Skitter 36 × 5 + Gilded Gunship 6 × 28 + the Gunships' Darts 36 × 2 + Mirror Interceptor 11 × 30 + Hornet 6 × 26 + Harrow 18 × 12 | 12,260 | 7,356 |
| Squad leaders: 10 × 13 extra (perfect run only) | 532 | 0 |
| Mid-boss: the Honour Guard (3 × 120) | 1,472 | 1,472 |
| Secret: Sentry Two's recorder (hidden crate) | 1,049 | 524 |
| Secondary: break the pickets | 1,049 | 524 |
| **Total** (bounty scale 0.39) | **16,362** | **9,876** |
| Budget(n) = the typical haul's target; typical +0.8 %, perfect 1.67 × budget | | 9,796 |

**Balance plan**: the plan's L40 visit buys the Mk V+ "Crucible" and the IFF spoofer (J11 of round 72, the
[hand plan](../../../player/generator/README.md#act-6-hand-plan-draft)) and fits Rook's Autocannon: forward DPS 168
against the proposed reference **160** (1.05), rear 30, 24 of 25 MW. The level expects `spread` and `rear`; the plan's
fit has the rear in the Swarm Tail and flies the duel on its beam and pod.

## Difficulty notes

- **Easy**: the formation lever thins the wings and the popcorn; one Gunship in each column (fewer Darts); the Mirror
  pincers 1 + 1 and the rear ambushes of 2; the Harrow cross 1 per edge, the streams of 4; the aces' easy hooks (the
  spec: later joins, single sniper line, rests ×1.3); the boss checkpoint.
- **Hard**: the wings larger; a third Gunship in the t=70.5 column; the rear ambush of 4 (the Mirrors' cap); the Harrow
  cross 3 per edge; the aces' hard hooks (earlier joins, the triple tap, rests ×0.8); no boss checkpoint.

## Concept art

None yet. The level needs (M10's Level 40 part; the aces' proposals are in the
[Honour Guard](../../../enemies/bosses/honour-guard/README.md)'s round-72 concept, the Jovian moods in
[the world](../../../world/jovian/README.md)'s):

- the backdrop: Jupiter's limb and night side, the CDF fleet's lights, the picket station, Callisto's disc rising, the
  orbital ring's relay platforms, the ice line and the citadel's lights (all `deep`, smooth glows and haze: ≥ 32 steps,
  no ordered dither, drawn once);
- the dead scout and Sentry Two's recorder; the CDF supply drone; the two briefing images; the intel silhouettes of the
  three aces;
- the round's captures: the level at medium with the plan's fit and Rook: the pickets, the Harrow cross, the duel's
  three joins and the three together, the last ace, the run-out; the aces' marks at 1:1.

## Implementation

- [ ] Level data `data.yaml` (M10's Level 40 part) with the sections, waves, radio, budget, threat profile, briefing,
      backdrop and music; the duel a one-second `arena` section at the 20 px/s drift with `hold_after_boss: 4`
- [ ] Reference DPS 160 at L40 and the Act 6 hand plan's Level 40 fit (M10 part A) before the data; the act HP factor
      for the returning `medium`+ units listed in *Waves*; the Act 6 shields from Level 36 on
- [ ] The level to the Act 2+ rules (`ActFloorTest`, `PacingTest`, the three empty stretches named), the Gunships'
      Darts counted, edge and BOTTOM warnings for side and rear first entries, the Harrow cross warned on every edge
- [ ] The three-way war's prey links (the Skitters of t=15.5 and t=114.5 on the Talons of t=14 and t=112); a prey kill
      by the Vrell pays nothing and is not in the secondary
- [ ] The [mid-boss](../../../enemies/bosses/honour-guard/README.md) duel per its spec: the sting, the bar and the
      checkpoint at t=102 (none on hard), the clock halted while an ace lives, the waves resuming at t=107; Rook and
      the autopilot per the spec
- [ ] Primary `reach-end` with the duel as its condition, `BREAK THE HONOUR GUARD`; secondary `kill_ratio` 65 % (the
      aces and the Vrell's prey kills not counted)
- [ ] `NO GROUND` on the hangar and in the weapons rows (the Bomb Rack, Rook's Mortar)
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`), with Rook and without; the aces on the
      enemy channel as `PHASE` events; Rook's rivalry lines after Harker's taunt and at Harker's death
- [ ] Threat profile, Varga's four intel lines and Okafor's teaser fitting the intel panel; the aces' intel silhouettes
- [ ] Briefing: both pages, each one screen, with their images
- [ ] Typical haul within ±5 % of budget(40) with the level's `bounty_scale`; the mid-boss's share inside 10–20 %
      (`BalanceTest`), its bounty recomputed with the measured scale
- [ ] The recorder secret (4 hits, crate 100), the overdrive, the shield cell, the supply drone's two armour patches
- [ ] Music: track 14's stems by section, the sting and the duel theme (32), track 14 back after the duel; the
      `belt-open` ambience
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on every difficulty
      (`--escort none` too)
- [ ] Production art and the round's captures

## Open questions

Left to the build (the document is approved as drafted):

- **The `bounty_scale` 0.39**, just under the gap analysis's 0.40–0.56: the measured scale decides; fewer waves (≈ 95 a
  minute) would bring it to ≈ 0.45.

## Decisions

- 2026-10-31 (Act 6 design phase, step `m10-d-levels-3`; decided 2026-10-31 (round 72)): drafted from the
  roster row, the gap analysis (§0, §1.1, §2.4, §3: Q3 a, Q5 a, Q9 a, Q10 a; J1, J2, J4, J5, J10), the mid-boss's
  README (the source of truth for the duel and the aces' lines) and Level 35's draft as the model (every section, hand
  tables marked draft, no `data.yaml`).
- 2026-10-31: the duel sits mid-level (t=102) between a 93 s approach and a 67 s run-out, Level 19's mid-boss shape;
  no wave from t=93 to the sting, ≥ 4 s of calm after the last ace; the duel's arena drifts at 20 px/s (no ground to
  halt).
- 2026-10-31: ≈ 113 enemies a minute (321 in 170 s of clock, the Gunships' Darts counted); the Vrell hunt two Talon
  waves (Q3 a); the tiny units are the Darts, Skitters and Motes (J5).
- 2026-10-31: Level 40's hand `bounty_scale` is **0.39**, so the aces' bounty is **360** in Act 1 terms (15 % of
  budget(40)), recomputed from the spec's 237 at 0.59.
- 2026-10-31 (decided here, trivia): a `kill_ratio` secondary where the gap analysis drafted none; the secret is a lost
  CDF scout's recorder (Level 30's breakable-object rule); the music is track 14 with the duel theme between (the
  audio step confirms); Rook's rivalry lines are drafts for the story step.
- 2026-10-31, design phase (step m10-d-act; decided 2026-10-31 (round 72)): the ambience is D-audio's `belt-open` bed (open space;
  `callisto-fortress` is Level 41's); Rook's rivalry lines match the story step's Rook (J4).
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): **Q5 a** (the duel staggered, then
  together; b three duels in sequence and c all three from the start rejected), **J14 reach-end** (the duel is a
  condition of the run, the level runs out to the citadel's outer line; the `boss` primary, about 67 s shorter,
  rejected), **J13 kept** (the secondary "Break the pickets", `kill_ratio` 65 %; none rejected), J4 (the aces, Rook's
  rivalry lines), J2 (`NO GROUND`), J15 (the scale outside the band, the build measures it) and M.2 b (the duel theme
  is "Three Aces"). The document approved as drafted (D.6; `design: approved`).
