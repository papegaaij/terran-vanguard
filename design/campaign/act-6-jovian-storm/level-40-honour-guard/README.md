---
title: Level 40 – Honour Guard
design: approved
implementation: in-progress
art: proposed
depends-on: [../../../enemies/bosses/honour-guard, ../../../enemies/air/talon, ../../../enemies/air/chimera, ../../../enemies/air/mirror-interceptor, ../../../enemies/air/harrow, ../../../enemies/air/gilded-gunship, ../../../enemies/air/hornet, ../../../enemies/air/dart-drone, ../../../enemies/air/skitter, ../../../enemies/air/mote-swarm, ../../../world/jovian, ../../../player/wingmen, ../../../systems/economy]
updated: 2026-11-01
---

# Level 40 – Honour Guard

## Summary

The road to Callisto. Aegis Wing leaves Jupiter's shadow and flies the Callisto approach in vacuum, through
Vorne's picket line: Talon wings, Chimera swarms, Gunships and their Darts, Mirror Interceptors from the flanks and
from behind, Harrows from every edge, and the Vrell still hunting their old allies. Then the waves stop and the
[Honour Guard](../../../enemies/bosses/honour-guard/README.md) takes the lane: Vorne's three aces in gold Talons,
the sniper Harker first, then the dancer, then the feinter from behind, each with a taunt on the enemy channel and
Rook answering. After the last ace the run goes on over Callisto's orbital ring to the citadel's outer line. About
170 s of level clock plus a ≈ 62 s duel (measured at medium, step F2); ≈ 112 enemies a minute; air only (`NO GROUND`).
Density 5.

**Status: approved 2026-10-31 (round 72, D.6)** (drafted in the Act 6 design phase, step `m10-d-levels-3`, 2026-10-31). Written from the act roster, the gap analysis's recommended answers (**Q5 a** the staggered three-ace
fight, **Q3 a** the three-way war, **Q9 a** the elite livery, **Q10 a** the Level 40 mood; judgement calls **J1** the
reference DPS 160, **J2** the medium and `NO GROUND`, **J4** the aces, **J5** no new tiny unit, **J10** Okafor on the
carrier, all decided 2026-10-31 (round 72)) and the wave 1 and 2 documents. The mid-boss document owns the duel (the aces,
their joins, playbook, tells, HP, the bounty's share and their lines' wording); this document owns the approach, the
run-out, the timeline, the radio's order and delays and the economy. Every number is a hand-written first draft at
medium for M10's Level 40 part to measure with `levelReport`; there is no `data.yaml` yet.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Rendered from the level's
[data.yaml](data.yaml) (M10 part F, step F2: the approved texts as they are):

<!-- data: briefing -->
> **Commander Okafor:** "Callisto's approach is held by Vorne's pickets and by his Honour Guard:
> three aces in gold Talons. Break the pickets, then the guard. The fleet follows your lane,
> Lancer."
>
> **Dr. Varga:** "The pickets fly hybrids and mirrors. The aces start one by one and end
> together: a sniper, a dancer, and one who comes from behind. Keep your rear guns fitted."
<!-- /data -->

*Images* (production art in M10's Level 40 part; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-40-callisto-approach`: a tactical chart of the Jovian system's edge: Jupiter's limb, Callisto's
  disc, the fleet's track from Io, the picket line as gold contacts across it, a lane marked in white, three gold
  chevrons at its far end.
- Varga's page `level-40-honour-guard`: her sheet of the three aces from above, each Talon silhouette with its mark
  (the sniper's barrel and red nose band, the dancer's white ribbons, the feinter's chevrons and red tail band) and an
  arrow for where each enters (top, a side, behind).

*Hangar teaser* (shop screen before L40; Okafor, not voiced):

<!-- data: teaser -->
> **Commander Okafor:** "Vorne's guard waits at Callisto. Fit something that shoots behind you."
<!-- /data -->

## Threat profile

Rendered from the data. Directions are counted by each wave's `from` (the intel's rule), by enemy. The aces are a
mid-boss and are not in the numbers. The README's two V-wings from the rear (t=48, t=144.5) fly from the front until
the simulation flies a front formation from the rear (step F2; m10f-gaps.md §1 #11), so the rear share is the Mirror
Interceptors' rear ambushes' alone for now.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` only (`space` is `air` in vacuum, the [Jovian media](../../../world/jovian/README.md#media-per-level)): the pickets and the Honour Guard. No ground layer: the Bomb Rack and Rook's Mortar read **`NO GROUND`** (J2; the hangar warns) |
| Attack directions | front 86% · sides 11% · rear 2% (counted by enemy; the duel adds the aces' side and rear entries) |
| Density | 5 |
| Recommended traits | `spread` (three aces apart, wide pickets), `rear` (the Mirror Interceptors' rear ambushes, Kestrel's station); `homing` helps against the moving aces |
| Hazards | Harrow death-burst rings; the Mirror Interceptors' bounced shots; the aces' sniper line and dives; no weather (vacuum) |
| Boss / mid-boss | Mid-boss [Honour Guard](../../../enemies/bosses/honour-guard/README.md) |
| Sensor-suite detail | **none**: the Callisto approach, `air` only, front, sides, rear. **L1**: + the directions, density 5, hazards "death-burst rings" and "sniper line", the objective `BREAK THE HONOUR GUARD`. **L2**: + Talon, Chimera, Gilded Gunship, Hornet, Mirror Interceptor, Harrow, Dart Drone, Skitter, Mote Swarm portraits; the mid-boss's name and three silhouettes (`honour-guard`). **L3**: + `spread` and `rear` highlighted; the timeline strip marks the Harrow cross (65 s) and the duel (102 s); 1 secret. |
<!-- /data -->

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
  (**186 of the 285** planned units at medium; the Gunships' launched Darts count as kills but not in the requirement, Level 35's built rule: step F2), judged at the level's end. The aces are not counted
  (`boss_counted: false`), nor Talons the Vrell kill (prey; the [three-way rule](../../../enemies/README.md#the-three-way-war)).
  Pays **100** in Act 1 terms (1,049 in Act 6); the typical haul counts it at 50 %. No radio line. (The gap analysis
  drafted none; every Act 5 level and every act boss level has one, so the act keeps the rule; decided 2026-10-31 (round 72).)

## Layout

Scroll **160 px/s** in open space, eased to **20 px/s** over t=98–102 for the duel's one-second arena, which **halts**
the scroll at its end while an ace lives (step F2, pending the user's review in round 78: the approved draft had the
backdrop drift at 20 px/s through the duel, but the built clock halts a non-crawling arena and ART-F2's backdrop is laid
out for the halt: Callisto's lit limb held still behind the duel), then back up to 160 over 2 s. Setting: the
[Jovian system](../../../world/jovian/README.md#sub-locations-per-level)'s Callisto approach, vacuum
([vacuum rules](../../../world/belt/README.md#vacuum-rules): explosions flash short and throw debris in a ring, no
rising smoke). **Light** ([mood](../../../world/jovian/README.md#moods-per-level-q10), Q10 a): black space, Jupiter's
cream-orange limb at the bottom edge in section 1 shrinking to a disc at the right edge, Callisto's dark blue-grey disc
growing at the top from section 3, the CDF fleet's lights (Okafor's carrier group) on the `deep` layer. Motion budget:
the waves are the strong elements; the backdrop only drifts. No ground layer, no `low-air` traffic in the duel (three
aces are enough to read).

**The duel and the clock.** The last approach wave enters at t=93; no wave after it. The **mini-boss sting** (track
21), the bar `HONOUR GUARD` (three segments, hatched) and the **boss checkpoint** (none on hard) come at **t=102**,
at the start of the one-second section *The Duel* (`arena: true`, the scroll eased to 20 px/s over t=98–102 in
*The Guard's Lane, braking*, ART-F2's split of the draft's section 4); Harker's entry tell starts 1 s later. The level clock **halts at t=103** while an ace lives (the
boss rule); `hold_after_boss: 4` (the last ace's 2 s chain, the credit shower, 2 s more) keeps it halted after the
kill, then the clock resumes at t=103 and the scroll eases up to 160 px/s over 2 s. The first wave after the duel
enters at **t=107**: **≥ 4 s of calm** after the last ace ([the death](../../../enemies/bosses/honour-guard/README.md#death)).
**K** below is the last ace's death.

**Rook and the escort.** Rook flies his slot ([Rook in Act 6](../../../player/wingmen/README.md#rook-in-act-6-draft)) and
follows the mid-boss's [escort rules](../../../enemies/bosses/honour-guard/README.md#the-escort-rook) (the nearest ace,
out of a told line, 40 px off a dive). With `--escort none` the level is the same without him and his lines.

Rendered from the data (ART-F2's eight sections: the draft's section 4 split at t=98 where the ease starts):

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Jupiter's Shadow | 0–30 | 0–4,800 | 160 | clear | `deep`: Jupiter's limb filling the bottom edge, its night side, the fleet's lights rising off it. | Talons, Chimeras and popcorn; the Vrell hunting Talons (t=15.5). |
| 2. The Picket Line | 30–64 | 4,800–10,240 | 160 | clear | `deep`: Jupiter shrinking to a disc at the right edge; a picket station far off (scenery). `low-air`: a dead CDF scout drifting down the left edge (t≈50, the secret's source). | Gunships and Darts, the first Mirror pincer (t=35.5), the first V-wing from the rear (t=48; flown from the front until the simulation's rear V-wing). |
| 3. Callisto Rising | 64–96 | 10,240–15,360 | 160 | clear | `deep`: Callisto's dark disc rising at the top, its ice catching Jupiter's light. | The Harrow cross (t=65.5), the Mirror rear ambush (t=68), the last pickets (t=93). |
| 4. The Guard's Lane | 96–98 | 15,360–15,680 | 160 | clear | `deep`: Callisto larger; three gold glints far ahead. | The pre-duel pause begins (t≈97); the supply drone's first armour patch (t=97). |
| 5. The Guard's Lane, braking | 98–102 | 15,680–16,040 | 20 | clear | `deep`: the three glints climbing away out of the top edge (the aces leaving to make their entries). | The scroll eased from 160 to 20 px/s over t=98–102; Varga names the aces. |
| 6. The Duel | 102–103 | 16,040–16,060 | 20 | clear | `deep`: as section 5, held still behind the duel (Callisto's lit lower limb across the screen). | The sting at t=102, the duel (≈ 62 s at medium); the clock halted while an ace lives. |
| 7. Callisto's Ring | 103–140 | 16,060–21,840 | 160 | clear | `deep`: Callisto's orbital ring (black and gold relay platforms, scenery) crossing below; the fleet closing behind. | Calm to t=107, then the waves resume; the ring's defenders. |
| 8. The Ice Line | 140–170 | 21,840–26,640 | 160 | clear | `deep`: Callisto's ice filling the lower half, the black citadel's lights at the top edge by t=165. | The last waves; the level ends at t=170 as the citadel's outer line reaches mid-screen. |
<!-- /data -->

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

Rendered from the data (M10 part F, step F2; "left"/"right" is the side edge a wave enters by). Step F2's changes
(pending the user's review in round 78): the two V-wings from the rear fly from the front until the simulation flies a
front formation from the rear; the Skitter swarms are streams (Skitters fly no flock); the Mirror rear ambush moved
from t=75.5 to t=68 (swapped with the motes: at 75.5 the t=70.5 Gunships drifted down through its stations); hard's
Harrow cross 2 per edge (the README's 3 is beyond the Harrow's cross of 4–8).

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Jupiter's limb below; the Talons' Act 6 shields flash on the first hits |
| 6.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 9 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front (left) | |
| 11.5 | 1 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 14 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front (right) | **Prey** of the Skitters of t=15.5 |
| 15.5 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Vrell **hunters** (ram) of the t=14 Talons; Rook's line at t=21 |
| 18 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 20.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 23 | 1 | pincer | [Talon](../../../enemies/air/talon/README.md) | 6 | sides | Edge warnings at t=20 |
| 25.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 28 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | No leader (a hybrid wing) |
| 30.5 | 2 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | Each launches Dart streams (3 every 5 s, at most 6 alive): ≈ 12 Darts from the pair |
| 33 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | The README's Skitter swarm: a stream (Skitters fly no flock) |
| 35.5 | 2 | pincer | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 2 | sides | Edge warnings at t=32.5; 1 + 1 |
| 38 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The leader drops the Overdrive |
| 40.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 43 | 2 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | ≥ 80 px between the hulls |
| 45.5 | 2 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 48 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The README's first wing from the rear (BOTTOM warning at t=45): from the front until the simulation flies a rear V-wing |
| 50.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | The dead scout drifts in at the left edge (the secret) |
| 53 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 55.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | The README's Skitter swarm: a stream (Skitters fly no flock) |
| 58 | 2 | pincer | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 4 | sides | Edge warnings at t=55; 2 + 2, ≥ 80 px between hulls |
| 60.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | |
| 63 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 65.5 | 3 | cross | [Harrow](../../../enemies/air/harrow/README.md) | 8 | all edges | 2 per edge, warned on every edge at t=62.5 (hard 2 per edge: the README's 3 is beyond the Harrow's cross of 4–8) |
| 68 | 3 | rear ambush | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 3 | rear | BOTTOM warning at t=65; each in its lane, ≥ 80 px apart (step F2: t=68, not 75.5: at 75.5 they held their stations as the t=70.5 Gunships drifted down through them) |
| 70.5 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | ≈ 12 Darts; the last Gunship drops the Shield cell |
| 73 | 3 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 75.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Step F2: t=75.5, not 68 (the rear ambush's slot) |
| 78 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | The README's Skitter swarm: a stream (Skitters fly no flock) |
| 80.5 | 3 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 83 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | sides (alternating edges) | From alternating sides, edge warnings at t=80 |
| 85.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 88 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 90.5 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | |
| 93 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The last approach wave: the pre-duel pause t≈97–102 |
| 102 | 6 | mid-boss | [Honour Guard](../../../enemies/bosses/honour-guard/README.md) | 1 | top, then a side, then the rear (each ace's own entry tell, 2.5 s ahead) | At the arena's start: the sting, the bar and the checkpoint; Harker told 1 s later; no other waves in the duel |
| 107 | 7 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The first wave after the duel (≥ 4 s of calm) |
| 109.5 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 112 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front (right) | **Prey** of the Skitters of t=114.5 |
| 114.5 | 7 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Vrell **hunters** (ram) of the t=112 Talons |
| 117 | 7 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | ≈ 12 Darts |
| 119.5 | 7 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 122 | 7 | pincer | [Talon](../../../enemies/air/talon/README.md) | 6 | sides | Edge warnings at t=119 |
| 124.5 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 127 | 7 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 129.5 | 7 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | |
| 132 | 7 | rear ambush | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 2 | rear | BOTTOM warning at t=129 |
| 134.5 | 7 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | The README's Skitter swarm: a stream (Skitters fly no flock) |
| 137 | 7 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 139.5 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 142 | 8 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | sides (alternating edges) | Edge warnings at t=139 |
| 144.5 | 8 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The README's second wing from the rear (BOTTOM warning at t=141.5): from the front until the simulation flies a rear V-wing |
| 147 | 8 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 149.5 | 8 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 152 | 8 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 154.5 | 8 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 157 | 8 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The last wave; the end's calm from t≈164 |

Totals: Talon 86 · Mote Swarm 72 · Chimera 50 · Skitter 36 · Gilded Gunship 6 · Mirror Interceptor 11 · Hornet 6 · Harrow 18 · Honour Guard 1.
<!-- /data -->

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
- **Shield cell**: dropped by the last Gunship of the t=70.5 column (the second at medium; easy flies one).
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

Rendered from the data (M10 part F, step F2). The aces' death lines are keyed by the simulation's subjects: the
first loss is spoken by the senior flying survivor (Harker, then Vesper, then Kestrel: `first-loss-<ace>`), "Just us
now" by the last ace (`last-ace-<ace>`), Rook's "for Ceres" at `harker-down`; Harker's portrait is `harker-guard`.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Callisto's picket line, Lancer. Vorne's own guard flies it. Clear a lane for the fleet." |
| t=12 | Varga | "Chimeras in the pickets. They heal if you let them rest: hold the beam on one." |
| t=21 | Rook | "The bugs are still chewing on their old friends. Fine by me." |
| t=33 | Varga | "Mirror interceptors on the flanks. Hit them from the side, or burn the mirror." |
| t=44.5 | Okafor | "Contacts behind you, Lancer. Rear guns." |
| t=62.5 | Varga | "Harrows from every side. They burst when they die: thin them far out." |
| t=76 | Rook | "Callisto. Looks cold. Looks like it's waiting for us." |
| t=84 | Okafor | "Long-range scan: three gold Talons holding the lane ahead. The Honour Guard." |
| t=92.5 | Varga | "Vorne's aces. One snipes, one dances, one comes from behind. Watch all three." |
| Harker's entry tell (`boss-arrival`) | Harker | "Rook. They let you fly again? Then let's settle Ceres properly." |
| After Harker's taunt (`boss-arrival`, queued behind it) | Rook | "Harker. Still flying for whoever pays the most?" |
| Vesper's entry tell (`boss-phase`) | Vesper | "Leave some for me, Harker. The quiet one's mine." |
| Kestrel's entry tell (`boss-phase`) | Kestrel | "Check your six, Lancer. I'm already there." |
| The first ace's death, Harker surviving (`boss-phase`) | Harker | "That was one of mine." |
| The first ace's death, Vesper the senior survivor (`boss-phase`) | Vesper | "You'll pay for that one." |
| The first ace's death, Kestrel the only survivor flying (`boss-phase`) | Kestrel | "No more games." |
| Harker's death, whenever it comes (`boss-phase`) | Rook | "That one's for Ceres, Harker." |
| The second ace's death, Harker the last (`boss-phase`) | Harker | "Just us now. Come on, then." |
| The second ace's death, Vesper the last (`boss-phase`) | Vesper | "Just us now. Come on, then." |
| The second ace's death, Kestrel the last (`boss-phase`) | Kestrel | "Just us now. Come on, then." |
| The last ace's death (K, `boss-destroyed`, 2.5 s later) | Rook | "Three gold Talons. Vorne needs new friends." |
| The last ace's death without Rook (replaces his line) | Okafor | "The Honour Guard is down. Well flown, Lancer." |
| t=108.5 | Okafor | "The guard is gone and the ring is open. Push through to the ice." |
| t=125 | Varga | "Helix HQ is under that ice. I had an office there, once. Three floors down." |
| t=138.5 | Rook | "Bursters again. Shoot them far out, not on top of us." |
| t=158 | Okafor | "That's the citadel's outer line. Form up, Aegis. Callisto is next." |
<!-- /data -->

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
- **Measured** (M10 part F, step F2, `levelReport -Plevel=40 -Pparts=guard,boss -Pseeds=5`: the plan's L40 fit with
  the Crucible, the spoofer's load and Rook, step F2's first-cut autopilot that lines up under the nearest ace): on the
  draft's 1,600 HP an ace and its joins the duel ran 47.0 s at medium (42.4–49.1) with **0 s of all three together** on
  every seed (Vesper died before Kestrel's 15 s ran out; easy 33.0 s, 0 s; hard 58.2 s, 17–26 s). Tuned (the three HP
  together, the joins earlier, as the spec's lesson says; pending the user's review in round 78): **2,240 an ace**
  (6,720; easy 5,040 / hard 8,736), Kestrel **4 s after Vesper** crossed (easy and hard 3 s): medium **62.0 s**
  (59.6–63.2, 5 of 5 in 45–75 s), all three together 30–33 s; easy 46.5 s (43.6–50.5), together 14–26 s; hard 79.8 s
  (72.6–84.6), together 36–56 s. The par is the measured mean, **62**; the bounty stays 360 (the measured scale is the
  hand 0.39). Step F3 measures it again with its own autopilot (the told lines and dives, the Kraken's calibration).

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
budget (by hand 9,876, +0.8 %; rendered from the data below **9,847, +0.5 %**, the perfect run 1.66 ×; `levelReport`'s
fit 0.39: step F2), with the economy's shares (60 % of the air kills and launched Darts, half the secret and the
secondary, the mid-boss at 100 %); `TypicalHaulTest` confirms it in the build. The mid-boss keeps its **15 %** share (the band 10–20 %, `BalanceTest`). The scale is just under the gap
analysis's band (0.40–0.56): a long level with a mid-boss earns most of its haul from its 321 enemies.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Talon 86 × 12 + Mote Swarm 72 × 2 + Chimera 50 × 14 + Skitter 36 × 5 + Gilded Gunship 6 × 28 + Mirror Interceptor 11 × 30 + Hornet 6 × 26 + Harrow 18 × 12 + launched Dart Drone 36 × 2 | 12,209 | 7,325 |
| Squad leaders: 10 × 13 extra (perfect run only) | 530 | 0 |
| Mid-boss: Honour Guard (harker 120 + vesper 120 + kestrel 120) | 1,473 | 1,473 |
| Secret: Sentry Two's recorder (hidden crate, 1% of budget) | 1,049 | 524 |
| Secondary: break the pickets | 1,049 | 524 |
| **Total** (bounty scale 0.39) | **16,310** | **9,847** |
| Budget(n) = the typical haul's target; typical +1 %, perfect 1.66 × budget | | 9,796 |
<!-- /data -->

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

Round 78 (M10 part F, steps ART-F1 and ART-F2; production art, proposed). The aces themselves, their tells, wrecks and
entry chevrons are the [Honour Guard](../../../enemies/bosses/honour-guard/README.md#concept-art)'s, their intel
silhouette `intel/boss-honour-guard` the hangar's ([review](../../../ui/hangar/concept/intel-final-r78-a.png)); the CDF
supply drone is Levels 13/18's `supply-drone` sprite (reused); the two briefing images wait for a later art step.

**The Callisto approach** (`tools/art/backdrop_l40.py`, ART-F2, on the shared Jovian pieces of `tools/art/jovkit.py`),
in round 72's L40 grade (black space, Callisto's dark blue-grey ice, Jupiter's cream-orange limb, the fleet's lights):
one continuous `deep` field cut into the sections' tile sets (no seam at a section change) scrolling at **0.06** (9.6
px/s at 160: the README's motion budget, the bodies drift) - Jupiter's lit limb and night side filling the bottom right
at the start and sinking out over the right edge by t≈44, the fleet's lights rising off it, a picket station far off on
the right in section 2, Ganymede and Europa as points; **Callisto rising** at the top from t≈66 (its sunlit near side
first, a multi-ring basin, saturated with small craters and bright fresh ones), its lit lower limb crossing the screen
at y≈330 behind the duel (the scroll halts there: the backdrop holds still), filling the screen from section 6; three
gold glints far ahead in section 4 that climb away out of the top edge just before the sting; Callisto's orbital ring
with two black and gold relay platforms crossing below on `far` (0.3) in section 6, the fleet closing in from behind;
**the ice line** (Callisto's terminator) entering at the top as section 7 begins, the sunlit ice in the lower half by
t≈160 and the black citadel's gold and red lights on the night side at the top edge from t≈159. Every glow additive and
smooth (≥ 32 levels, checked), no ordered dither; no red but the citadel's beacons (the sniper's line is red). Pending
the user's review in round 78: Callisto's growth as one body coming into view instead of 3–4 authored sizes (a rigid
layer cannot swap discs without a pop), the deep layer's 0.06, section 4 split at t=98 so the ease to the 20 px/s
drift starts there (the README's px column). **Sentry Two's recorder** (`tools/art/scout_recorder.py`): a hazard-orange
crash-survivable pod with a white band and a torn cable stub, its beacon blinking white, tumbling with the dead CDF
scout (the fleet's Stormhawk airframe, engines and canopy dark, the right wing torn off, scorched); its break in vacuum
(a short flash, the shell chips in a ring, no smoke). The proposal (`concept/backdrop-proposal.yaml`, step F2's input,
not data) holds the backdrop block, the sections and the recorder's `tows` row. Prompts and briefs:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r78-a.png](concept/backdrop-final-r78-a.png) | Production backdrop: twelve screens at half size with stand-ins (Jupiter's limb and the fleet rising, Talons over Jupiter's shadow, the picket station and a pincer, the dead scout and its recorder, Callisto rising over the Harrow cross, the three glints, the brake, Harker's entry and the three aces together over the held backdrop, Callisto's ring and the fleet behind, the ice line, the citadel's lights), the readability crops at 1× (each section's darkest and brightest window with a Talon, Harker, a Harrow, amber needles, an orb, the ship; the means), the pieces, the tile sets (sheet) | proposed (round 78) |
| [concept/backdrop-final-r78-a.gif](concept/backdrop-final-r78-a.gif) | t 95–103 at half size: the glints climbing away and the brake to the 20 px/s drift, then the held duel backdrop with Harker's entry from the top | proposed (round 78) |
| [concept/scout-recorder-final-r78-a.png](concept/scout-recorder-final-r78-a.png) | Sentry Two's recorder tumbling (16 frames, 3×) with its beacon, the dead scout tumbling (16 frames, 2×), the recorder's break (8 frames, 2×), the pair at 1× on the backdrop (sheet) | proposed (round 78) |
| [concept/scout-recorder-final-r78-a.gif](concept/scout-recorder-final-r78-a.gif) | The recorder and the scout drifting down the left edge from t=50, the beacon blinking once a second, the housing shot open after 4 s, the scout drifting on | proposed (round 78) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (tile sets, pieces, placements with their paths, the atmosphere), the sections and the recorder's `tows` row for step F2 (not data) | proposed (round 78; step F2's input) |

## Implementation

- [x] Level data `data.yaml` (M10's Level 40 part) with the sections, waves, radio, budget, threat profile, briefing,
      backdrop and music; the duel a one-second `arena` section at the 20 px/s drift with `hold_after_boss: 4` (step
      F2: ART-F2's eight sections; the arena halts the scroll, the built clock's rule; the two rear V-wings from the
      front until the simulation's rear V-wing)
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

- **Round 78 (step F2's build, pending the user's review)**: the duel's arena **halts** the scroll (the built clock;
  ART-F2's backdrop is laid out for it) where the draft drifts at 20 px/s; the two **V-wings from the rear** fly from
  the front until a simulation step builds a front formation from the bottom edge; the **Harrow cross of 8** (2 an
  edge, the approved table) puts up to 8 Harrows on the screen and 4 in their tell or ram (flown, the plan's fit), over
  the Harrow's caps of 6 and 2 (its page): keep the cross of 8, or a cross of 4 (1 an edge, 281 units) inside the caps.

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
- 2026-11-01 (M10 part F, step F2; trivia pending the user's review in round 78): the data on ART-F2's proposal (its
  eight sections, the recorder's tow, the backdrop block as it is); the secondary's requirement **186 of the 285**
  planned units (the launched Darts count as kills, not in the requirement: Level 35's built rule; the draft's 209 of
  321); the Skitter swarms flown as streams; the Mirror rear ambush moved to t=68 (big hulls apart); hard's Harrow
  cross 2 an edge; the shield cell from the column's last Gunship; the second armour patch at the first's point; the
  briefing's texts as approved (each four lines or fewer); the duel measured and tuned in the mid-boss's data (see
  *Boss / mid-boss*).
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): **Q5 a** (the duel staggered, then
  together; b three duels in sequence and c all three from the start rejected), **J14 reach-end** (the duel is a
  condition of the run, the level runs out to the citadel's outer line; the `boss` primary, about 67 s shorter,
  rejected), **J13 kept** (the secondary "Break the pickets", `kill_ratio` 65 %; none rejected), J4 (the aces, Rook's
  rivalry lines), J2 (`NO GROUND`), J15 (the scale outside the band, the build measures it) and M.2 b (the duel theme
  is "Three Aces"). The document approved as drafted (D.6; `design: approved`).
