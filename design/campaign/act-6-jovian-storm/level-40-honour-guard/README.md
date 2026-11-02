---
title: Level 40 – Honour Guard
design: approved
implementation: done
art: chosen
depends-on: [../../../enemies/bosses/honour-guard, ../../../enemies/air/talon, ../../../enemies/air/chimera, ../../../enemies/air/mirror-interceptor, ../../../enemies/air/harrow, ../../../enemies/air/gilded-gunship, ../../../enemies/air/hornet, ../../../enemies/air/dart-drone, ../../../enemies/air/skitter, ../../../enemies/air/mote-swarm, ../../../world/jovian, ../../../player/wingmen, ../../../systems/economy]
updated: 2026-11-02
---

# Level 40 – Honour Guard

## Summary

The road to Callisto. Aegis Wing leaves Jupiter's shadow and flies the Callisto approach in vacuum, through
Vorne's picket line: Talon wings, Chimera swarms, Gunships and their Darts, Mirror Interceptors from the flanks and
from behind, Harrows from every edge, and the Vrell still hunting their old allies. Then the waves stop and the
[Honour Guard](../../../enemies/bosses/honour-guard/README.md) takes the lane: Vorne's three aces in gold Talons,
the sniper Harker first, then the dancer, then the feinter from behind, each with a taunt on the enemy channel and
Rook answering. After the last ace the run goes on over Callisto's orbital ring to the citadel's outer line. About
170 s of level clock plus a ≈ 60–72 s duel (par 66; measured at medium, step F6c); ≈ 112 enemies a minute; air only (`NO GROUND`).
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
mid-boss and are not in the numbers. The rear share is the two Talon V-wings rising from the bottom edge (t=48,
t=144.5; flown from the front until step DB1, after F1c built the rising V-wing) and the Mirror Interceptors' rear
ambushes.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` only (`space` is `air` in vacuum, the [Jovian media](../../../world/jovian/README.md#media-per-level)): the pickets and the Honour Guard. No ground layer: the Bomb Rack and Rook's Mortar read **`NO GROUND`** (J2; the hangar warns) |
| Attack directions | front 83% · sides 11% · rear 6% (counted by enemy; the duel adds the aces' side and rear entries) |
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
the scroll at its end while an ace lives (step F2, decided 2026-11-01 (round 78 B.4 accept): the approved draft had the
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
| 2. The Picket Line | 30–64 | 4,800–10,240 | 160 | clear | `deep`: Jupiter shrinking to a disc at the right edge; a picket station far off (scenery). `low-air`: a dead CDF scout drifting down the left edge (t≈50, the secret's source). | Gunships and Darts, the first Mirror pincer (t=35.5), the first V-wing from the rear (t=48, rising from the bottom edge). |
| 3. Callisto Rising | 64–96 | 10,240–15,360 | 160 | clear | `deep`: Callisto's dark disc rising at the top, its ice catching Jupiter's light. | The Harrow cross (t=65.5), the Mirror rear ambush (t=68), the last pickets (t=93). |
| 4. The Guard's Lane | 96–98 | 15,360–15,680 | 160 | clear | `deep`: Callisto larger; three gold glints far ahead. | The pre-duel pause begins (t≈97); the supply drone's first armour patch (t=97). |
| 5. The Guard's Lane, braking | 98–102 | 15,680–16,040 | 20 | clear | `deep`: the three glints climbing away out of the top edge (the aces leaving to make their entries). | The scroll eased from 160 to 20 px/s over t=98–102; Varga names the aces. |
| 6. The Duel | 102–103 | 16,040–16,060 | 20 | clear | `deep`: as section 5, held still behind the duel (Callisto's lit lower limb across the screen). | The sting at t=102, the duel (≈ 66 s at medium); the clock halted while an ace lives. |
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
- **Directions**: 45 of 58 waves from the front (78 %); the side pincers and streams have edge warnings, the rear
  V-wings and the Mirror rear ambushes a BOTTOM warning, the Harrow cross every edge, 3 s ahead.
- **Totals**: Talon 86 (10 leaders) · Mote Swarm 72 · Chimera 50 · Skitter 36 · Gilded Gunship 6 (+ 36 Darts) ·
  Harrow 18 · Mirror Interceptor 11 · Hornet 6.

Rendered from the data (M10 part F, step F2; "left"/"right" is the side edge a wave enters by). Step F2's changes
(decided 2026-11-01, round 78 L.1, L.2 and C.3 accept): the two V-wings from the rear flew from the front until the simulation flew a
front formation from the rear (since step DB1 they rise from the bottom edge); the Skitter swarms are streams (Skitters fly no flock); the Mirror rear ambush moved
from t=75.5 to t=68 (swapped with the motes: at 75.5 the t=70.5 Gunships drifted down through its stations); hard's
Harrow cross 2 per edge (the README's 3 is beyond the Harrow's cross of 4–8).

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Jupiter's limb below; the Talons' Act 6 shields flash on the first hits |
| 6.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 9 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front (left) | |
| 11.5 | 1 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Step F7: a hook at 170 px/s (in at x=300, across the upper third, out at the left edge at y≈320), not the loop down to the bottom edge (two of six looped 8–9 s above the ship) |
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
| 45.5 | 2 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Step F7: a hook at 170 px/s (in at x=180, across the upper third, out at the right edge at y≈320), not the loop down to the bottom edge |
| 48 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | rear | The README's first wing from the rear: it rises from the bottom edge (BOTTOM warning at t=45) |
| 50.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | The dead scout drifts in at the left edge (the secret) |
| 53 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 55.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | The README's Skitter swarm: a stream (Skitters fly no flock) |
| 58 | 2 | pincer | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 4 | sides | Edge warnings at t=55; 2 + 2, ≥ 80 px between hulls |
| 60.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | |
| 63 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 65.5 | 3 | cross | [Harrow](../../../enemies/air/harrow/README.md) | 8 | all edges | 2 per edge, warned on every edge at t=62.5 (hard 2 per edge: the README's 3 is beyond the Harrow's cross of 4–8) |
| 68 | 3 | rear ambush | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 3 | rear | BOTTOM warning at t=65; each in its lane, ≥ 80 px apart (step F2: t=68, not 75.5: at 75.5 they held their stations as the t=70.5 Gunships drifted down through them) |
| 70.5 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | ≈ 12 Darts; the last Gunship drops the Shield cell |
| 73.5 | 3 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Step F7: a hook at 170 px/s (in at x=180, out at the right edge at y≈320), not the loop down to the bottom edge; step F3b: the left lane at t=73.5, not t=73 down the right |
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
| 119.5 | 7 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Step F7: a hook at 170 px/s (in at x=180, out at the right edge at y≈320), not the loop down to the bottom edge |
| 122 | 7 | pincer | [Talon](../../../enemies/air/talon/README.md) | 6 | sides | Edge warnings at t=119 |
| 124.5 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 127 | 7 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 129.5 | 7 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | |
| 132 | 7 | rear ambush | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 2 | rear | BOTTOM warning at t=129 |
| 134.5 | 7 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | The README's Skitter swarm: a stream (Skitters fly no flock) |
| 137 | 7 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 139.5 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 142 | 8 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | sides (alternating edges) | Edge warnings at t=139 |
| 144.5 | 8 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | rear | The README's second wing from the rear: it rises from the bottom edge (BOTTOM warning at t=141.5) |
| 147 | 8 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 149.5 | 8 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 152 | 8 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Step F7: a hook at 170 px/s (in at x=180, out at the right edge at y≈320), not the loop down to the bottom edge; step F3b: the left lane (down the right they met the ship low on the t=144.5 rear wing) |
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
- **Armour patches** ×2: a CDF supply drone (a small white craft on `low-air`; no friendly brackets: nobody can hit it, round 79) releases one at
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
| t=23.5 | Rook | "The bugs are still chewing on their old friends. Fine by me." |
| t=33 | Varga | "Mirror interceptors on the flanks. Hit them from the side, or burn the mirror." |
| t=44.5 | Okafor | "Contacts behind you, Lancer. Rear guns." |
| t=62.5 | Varga | "Harrows from every side. They burst when they die: thin them far out." |
| t=75 | Rook | "Callisto. Looks cold. Looks like it's waiting for us." |
| t=82 | Okafor | "Long-range scan: three gold Talons holding the lane ahead. The Honour Guard." |
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

Timeline check (text model): t=1 closes ≈ 9.7, t=12 ≈ 20.1 (the game's ≈ 23.0: Rook's line moved from t=21 to 23.5,
step F4), t=23.5 ≈ 30.5, t=33 ≈ 41.0, t=44.5 ≈ 49.3; the recorder
(in reach t≈50–56) is spoken by ≈ 57.6 at the latest, before Varga's t=62.5 (≈ 69.8); t=75 ≈ 81.8, t=82 ≈ 92.6, t=92.5
≈ 103.8 (step F4's game timeline, `RadioTimelineLevel40Test`: Okafor's two pages typed out run ≈ 10.6 s, so his line
moved from t=84 to 82 and Rook's from 76 to 75; Varga's starts ≤ 1 s late), Harker's taunt queued behind it in his tell. In the duel the entry taunts ride their tells (2.5 s ahead of each entry) and
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
  Ion Beam L5, the Tesla Coil Pod L2 on the left wing, a Micro-missile Pod L4 on the right, the Swarm Tail L3, the Mk V+
  "Crucible" and the IFF spoofer bought at this visit, 24 of 25 MW; Rook's Autocannon) flies the duel; its measured
  times are below. The Tesla pod empties an ace's shield on a hit; the EMP Burst empties all three.
- **Measured** (M10 part F; round 78 B.1 **change**, decided 2026-11-01: the numbers stay as built, the user gave no
  verdict on them; the numbers and the tuning live in the
  [spec's *Duration and HP*](../../../enemies/bosses/honour-guard/README.md#duration-and-hp)): the three HP are tuned
  together to **1,900 an ace** (5,700; 2,120 before round 79's wider turns) and the par is **66**; the medium duel runs
  about 67 s (the window 45–75 s) since the aces fly like aircraft in **separate bands** of 140 px, turning no tighter
  than 60 px (round 78 B.1 and B.2 b, round 79 H.1; steps F6s–F6s4, R79b2: Harker y 27–167, Vesper 205–345, Kestrel
  380–520, the spec's *Arena*). The bounty stays 360 (the measured scale is the hand 0.39:
  `levelReport` typical 9,847, +0.5 %). The autopilot wins every flight with Rook and with `--escort none` on every
  difficulty (`Level40Test`). The whole duel as one real-time recording is the spec's `duel-fight-r78-b.mp4` (step F6r),
  re-recorded after the wider turns as `duel-fight-r79-b.mp4` (step R79b2).

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
  cross 3 per edge; the aces' hard hooks (earlier joins, the triple tap, rests ×0.8) at ×0.95 HP (their own HP lever, M10
  part I: at the difficulty's ×1.3 the duel ran past the 75 s window); no boss checkpoint.

## Concept art

Round 78 (M10 part F, steps ART-F1 and ART-F2; production art, decided 2026-11-01 (round 78 F.6–F.11 accept)). The aces themselves, their tells, wrecks and
entry chevrons are the [Honour Guard](../../../enemies/bosses/honour-guard/README.md#concept-art)'s, their intel
silhouette `intel/boss-honour-guard` the hangar's ([review](../../../ui/hangar/concept/intel-final-r78-a.png)); the CDF
supply drone is Levels 13/18's `supply-drone` sprite (reused); the two briefing images are the
[briefing's](../../../ui/briefing/README.md#concept-art) (step F4). The game captures of round 78 (step F5, the plan's
fit, the ship parked; recipe in [concept/prompts.md](concept/prompts.md#level-40-capture-r78-a)) and the listen-through
of the level's voices are listed below; the duel's captures are the [Honour Guard](../../../enemies/bosses/honour-guard/README.md#concept-art)'s.

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
smooth (≥ 32 levels, checked), no ordered dither; no red but the citadel's beacons (the sniper's line is red). Decided
2026-11-01 (round 78 C.8 accept): Callisto's growth as one body coming into view instead of 3–4 authored sizes (a rigid
layer cannot swap discs without a pop), the deep layer's 0.06, section 4 split at t=98 so the ease to the 20 px/s
drift starts there (the README's px column). **Sentry Two's recorder** (`tools/art/scout_recorder.py`): a hazard-orange
crash-survivable pod with a white band and a torn cable stub, its beacon blinking white, tumbling with the dead CDF
scout (the fleet's Stormhawk airframe, engines and canopy dark, the right wing torn off, scorched); its break in vacuum
(a short flash, the shell chips in a ring, no smoke). The proposal (`concept/backdrop-proposal.yaml`, step F2's input,
not data) holds the backdrop block, the sections and the recorder's `tows` row. Prompts and briefs:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r78-a.png](concept/backdrop-final-r78-a.png) | Production backdrop: twelve screens at half size with stand-ins (Jupiter's limb and the fleet rising, Talons over Jupiter's shadow, the picket station and a pincer, the dead scout and its recorder, Callisto rising over the Harrow cross, the three glints, the brake, Harker's entry and the three aces together over the held backdrop, Callisto's ring and the fleet behind, the ice line, the citadel's lights), the readability crops at 1× (each section's darkest and brightest window with a Talon, Harker, a Harrow, amber needles, an orb, the ship; the means), the pieces, the tile sets (sheet) | chosen (round 78 F.6 accept) |
| [concept/backdrop-final-r78-a.gif](concept/backdrop-final-r78-a.gif) | t 95–103 at half size: the glints climbing away and the brake to the 20 px/s drift, then the held duel backdrop with Harker's entry from the top | chosen (round 78 F.6 accept) |
| [concept/scout-recorder-final-r78-a.png](concept/scout-recorder-final-r78-a.png) | Sentry Two's recorder tumbling (16 frames, 3×) with its beacon, the dead scout tumbling (16 frames, 2×), the recorder's break (8 frames, 2×), the pair at 1× on the backdrop (sheet) | chosen (round 78 F.7 accept) |
| [concept/scout-recorder-final-r78-a.gif](concept/scout-recorder-final-r78-a.gif) | The recorder and the scout drifting down the left edge from t=50, the beacon blinking once a second, the housing shot open after 4 s, the scout drifting on | chosen (round 78 F.7 accept) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (tile sets, pieces, placements with their paths, the atmosphere), the sections and the recorder's `tows` row for step F2 (not data) | proposed (round 78; step F2's input) |
| [concept/pickets-capture-r78-a.png](concept/pickets-capture-r78-a.png) | In the game (step F5): the approach at half size, Talons over Jupiter's limb, a Chimera swarm, Talons and Darts, a Gunship column, the first Mirror pincer, Mirrors and Gunships | chosen (round 78 F.6 accept) |
| [concept/cross-capture-r78-a.png](concept/cross-capture-r78-a.png) | In the game: the Harrow cross of 8, warned on every edge, the Harrows entering and closing | chosen (round 78 F.11 accept) |
| [concept/cross-capture-r78-a.gif](concept/cross-capture-r78-a.gif) | In the game, motion: the cross from its warning to its bursts (t≈61.6–71) | chosen (round 78 F.11 accept) |
| [concept/rear-wing-capture-r78-a.png](concept/rear-wing-capture-r78-a.png) | In the game: the first V-wing from the rear (t=48), the BOTTOM warning, the wing rising from the bottom edge and firing up past the ship | chosen (round 78 F.11 accept) |
| [concept/rear-wing-capture-r78-a.gif](concept/rear-wing-capture-r78-a.gif) | In the game, motion: the warning and the rising wing (t≈44.8–50.8) | chosen (round 78 F.11 accept) |
| [concept/recorder-capture-r78-a.png](concept/recorder-capture-r78-a.png) | In the game, 1:1 strips of the left edge: the dead scout and Sentry Two's recorder drifting down (t≈51–56; the parked beam leaves it unbroken) | chosen (round 78 F.7 accept) |
| [concept/runout-capture-r78-a.png](concept/runout-capture-r78-a.png) | In the game: the run-out, the credit shower, the scroll easing up, the waves back at t=107, Callisto's ring, the ice line, the citadel's lights | chosen (round 78 F.6 accept) |
| [concept/runout-capture-r78-a.mp4](concept/runout-capture-r78-a.mp4) | In the game with its sound (23 s): the last ace's death, the duel theme stopping, the chain and the credit shower, track 14's full mix back with the waves | chosen (round 78 F.11 accept) |
| [concept/level-40-listen-r78-a.ogg](concept/level-40-listen-r78-a.ogg) | Listen-through: Level 40's two briefing pages and its radio as the game plays them (`assets/voice/` joined, 1.2 s apart; 2:49, every duel variant); `tools/concept/audio/voice_r78.py` | chosen (round 78 V.1 accept) |
| [concept/level-40-listen-r78-a.txt](concept/level-40-listen-r78-a.txt) | The listen-through's cue list: each line's start, speaker, source, expression and text | chosen (round 78 V.1 accept; a listening aid) |

## Implementation

- [x] Level data `data.yaml` (M10's Level 40 part) with the sections, waves, radio, budget, threat profile, briefing,
      backdrop and music; the duel a one-second `arena` section at the 20 px/s drift with `hold_after_boss: 4` (step
      F2: ART-F2's eight sections; the arena halts the scroll, the built clock's rule; step DB1: the two rear V-wings
      rise from the bottom edge, F1c's rising frame)
- [x] Reference DPS 160 at L40 and the Act 6 hand plan's Level 40 fit (M10 part A) before the data; the act HP factor
      for the returning `medium`+ units listed in *Waves*; the Act 6 shields from Level 36 on (M10 part A; step F2's
      data; `BalanceTest`'s Level 40 row: forward 168 against 160, 24 of 25 MW)
- [x] The level to the Act 2+ rules (`ActFloorTest`, `PacingTest`, the three empty stretches named), the Gunships'
      Darts counted, edge and BOTTOM warnings for side and rear first entries, the Harrow cross warned on every edge
      (steps F2, F3, DB1, F3b: `levelReport` medium 4.84 on the screen, 142 shots a minute, three stretches over 2 s
      (at most 3: ok), the end calm 9.8 s; the warnings in round 78's captures)
- [x] The three-way war's prey links (the Skitters of t=15.5 and t=114.5 on the Talons of t=14 and t=112); a prey kill
      by the Vrell pays nothing and is not in the secondary (step F2: `hunts:` on both waves, part C's prey rules)
- [x] The [mid-boss](../../../enemies/bosses/honour-guard/README.md) duel per its spec: the sting, the bar and the
      checkpoint at t=102 (none on hard), the clock halted while an ace lives, the waves resuming at t=107; Rook and
      the autopilot per the spec (steps F1a–F4: `Level40Test`, `HonourGuardFightTest`; round 78's captures)
- [x] Primary `reach-end` with the duel as its condition, `BREAK THE HONOUR GUARD`; secondary `kill_ratio` 65 % (the
      aces and the Vrell's prey kills not counted) (step F2: 186 of the 285 planned units; met on every autopilot flight)
- [x] `NO GROUND` on the hangar and in the weapons rows (the Bomb Rack, Rook's Mortar) (step F2: `ground: none`)
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`), with Rook and without; the aces on the
      enemy channel as `PHASE` events; Rook's rivalry lines after Harker's taunt and at Harker's death (step F4:
      `RadioTimelineLevel40Test`, Rook's "for Ceres" whenever Harker dies, his kill included; Rook's t=21 line moved
      to 23.5, Rook's t=76 to 75 and Okafor's t=84 to 82, decided 2026-11-01 (round 78 C.7 accept; V.2 keep: Okafor's 1.1 s pause stays))
- [x] Threat profile, Varga's four intel lines and Okafor's teaser fitting the intel panel; the aces' intel silhouettes
      (step F2's data, `IntelPanelLayoutTest`; ART-F1's `intel/boss-honour-guard`)
- [x] Briefing: both pages, each one screen, with their images (step F4: `level-40-callisto-approach`,
      `level-40-honour-guard`, tools/art/briefing_images.py; `BriefingLayoutTest`)
- [x] Typical haul within ±5 % of budget(40) with the level's `bounty_scale`; the mid-boss's share inside 10–20 %
      (`BalanceTest`), its bounty recomputed with the measured scale (steps F2, F3: typical 9,847, +0.5 %, at 0.39; the
      aces 1,473 = 15.0 %)
- [x] The recorder secret (4 hits, crate 100), the overdrive, the shield cell, the supply drone's two armour patches
      (steps F2, F4: `Level40Test` frees the recorder with the plan's beam and takes the supply drones' patches)
- [x] Music: track 14's stems by section, the sting and the duel theme (32), track 14 back after the duel; the
      `belt-open` ambience (step F4: `LevelMusic.midBoss`, the fleet's layer over section 1; `Level40SoundsTest`)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on every difficulty
      (`--escort none` too) (step F3: `Level40Test`, every flight won without a retry)
- [x] Production art and the round's captures (ART-F1, ART-F2, step F4's briefing images; step F5's captures, round 78;
      accepted 2026-11-01, round 78 F.6–F.11)
- [x] Round 78's follow-up builds, recorded 2026-11-02: the four b sounds and the last ace's wreck with its 2 s chain
      (F6g `b31fb221`); the aces fly like aircraft in separate bands, avoidance instead of shoves (F6s `1a460fdc`, F6c
      `a62441a4`, F6s3 `eb7057ef`, F6s4 `7c67bf95`: each ace 2,120 HP, par 66, the medium duel about 70 s); the duel as
      one real-time recording with sound (F6r `79974c83`, the debug `--replay` option)
- [x] Round 79's follow-ups: the aces turn smoother and wider, each 1,900 HP, par 66, re-recorded (R79b2; the spec's
      *Duration and HP*); the supply drones' inert `marked` keys dropped (R79b2, the friendly brackets rule)

## Open questions

Left to the build (the document is approved as drafted):

- **Round 78 (step F2's build; decided 2026-11-01: B.4 accept, L.1 accept, L.2 a)**: the duel's arena **halts** the scroll (the built clock;
  ART-F2's backdrop is laid out for it) where the draft drifts at 20 px/s; the two **V-wings from the rear** rise
  from the bottom edge since step DB1 (F1c's rising frame: their turn 214–255 px above the bottom edge, about half their
  shots from behind the ship, 0–2 hits on the ship a run, levelReport `rising`); the **Harrow cross of 8** (2 an
  edge, the approved table) puts up to 8 Harrows on the screen and 4 in their tell or ram (flown, the plan's fit), over
  the Harrow's caps of 6 and 2 (its page): **kept** (L.2 a), the cross of 8 an accepted exception recorded in the
  [Harrow](../../../enemies/air/harrow/README.md)'s page (the alternative, a cross of 4 inside the caps, rejected).

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
- 2026-11-01 (M10 part F, step F2; trivia decided 2026-11-01, round 78 C.3 and C.4 accept): the data on ART-F2's proposal (its
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
- 2026-11-01: Step DB1 (after F1c's rising V-wing): the two V-wings from the rear (t=48, t=144.5) fly `from: rear`,
  rising from the bottom edge under the BOTTOM warning; the attack directions read front 83 % · sides 11 % · rear 6 %.
  Measured (levelReport `rising`, 3 seeds a difficulty, the plan's fit): the Talons turn 214–255 px above the bottom
  edge, on the screen 5–10 s a run, 7–40 shots of which about half leave from below the ship, 0–2 hits on the ship a
  run (4 hits in 9 runs), their closest 44–81 px from the ship; no big-hull overlaps; the autopilot wins 9 of 9 without a
  retry. But the beam era's small-unit time on the screen rises from 1.43× to **1.73×** Level 10's (limit 1.5:
  `BeamEraHpTest` red at Level 40; the Chimeras' 2.56 → 3.76 s, the Talons' 2.32 → 2.26 s): to tune in the next L40 step
  (decided 2026-11-01, round 78 L.1 and C.6 accept).
- 2026-11-01 (M10 part F, step F5; decided 2026-11-01, round 78 B.3, L.3 and C.5 accept): part F closed out: the full check green
  (5,319 tests) on a snapshot of `24fe6985`; `levelReport -Plevel=40 -Pseeds=5`: medium 112 a minute, the floor and the
  pacing ok, typical 9,847 (+0.5 %), the duel 66.0 s (61.4–70.6; par 62), every autopilot flight won; still flagged and
  asked in round 78: the Harrow cross of 8 over the Harrow's caps (`dashers [flown]` FAIL at medium and hard), hard's
  duel outside the window (0 of 5). The captures from the game (the plan's fit, the ship parked) and the listen-through
  added to *Concept art*; every Implementation item ticked (`implementation: done`).
- 2026-11-02 (M10 round 78 follow-up, step F6c; B.2 b): the aces' separate bands and the re-par to the measured mean,
  **66** (was 62), in the [spec](../../../enemies/bosses/honour-guard/README.md#decisions); the duel's section row says
  ≈ 66 s.
- 2026-11-02 (user, [round 78](../../../concept-rounds/round-78/README.md) closed, 2026-11-01; page answers and one
  note): **everything as recommended except S.5 b, S.7 b, S.8 b, S.9 b, V.2 keep and B.1 change**. Art F.1–F.11 accepted;
  sounds S.1–S.11 picked (the unchosen variants moved to `concept/rejected/` of the sfx part); M.1 ok; V.1 and V.3
  accepted; **V.2 keep** (Okafor's t=82 line keeps its 1.1 s pause before "The Honour Guard"; the draft's comma fix not
  made); **B.2 b** (the aces' separate bands; built, F6c, F6s3); **B.1 change** (the question was unreadable: the numbers
  stay as built, no verdict; the one observation: the other crafts' movement is not natural, answered by the aces' new
  flight, F6s–F6s4, and the real-time recording F6r for the next round); B.3–B.5, L.1, L.3 and C.1–C.9 accepted; L.2 a
  (the Harrow cross of 8 kept as an exception); L.4 as round 76 (Rook's armour in part I's balance). Every "pending the
  user's review in round 78" note is now "decided 2026-11-01 (round 78 <id>)". Built since the round opened: F6g
  `b31fb221`, F6s `1a460fdc`, F6c `a62441a4`, F6s3 `eb7057ef`, F6s4 `7c67bf95`, F6r `79974c83` (see *Implementation*).
- 2026-11-02 (M10 part F, step F7; decided as trivia, F3b's kind of move): `BeamEraHpTest` red again at Level 40, **1.57×**
  Level 10's small-unit time on the screen (2.52 s vs 1.60 s; F3b's 1.44×). Moved by F6s4 (the aces' rough edges, a
  simulation change only: 1.45× before it, 1.57× after): the approach flies as before, the run-out after the duel from
  another state; F3b's 1.44× was one lucky seed (six seeds: 1.55–1.68×). Most of the excess is the five Chimera swarms
  (3.6 s a kill over six seeds; on the loop down to the bottom edge two of six looped 7–11 s above the ship). They now
  fly a **hook** at 170 px/s (was 160): in from the top, across the upper third and out at the far side edge at y≈320
  (t=11.5 from the right lane, the other four from the left); counts and times unchanged. Measured: 1.35× (2.16 s), six
  seeds 1.31–1.42×; the duel at medium 63.2–68.9 s on `HonourGuardFightTest`'s five seeds (mean 65.8, par 66); 112 a
  minute, every autopilot flight won.
- 2026-11-02 (M10 part I, step I2; pending the user's review in round 80): the hard duel in the mid-boss window: the aces' HP lever on
  hard 0.95 (the Honour Guard's `hp` hook, not the difficulty's 1.3): 65.5 s, 60.5-70.9 over five seeds (91.1 s, 0 of 3
  before; at 1.0 71.2 s, 4 of 5); medium unchanged (66.0 s, par 66). The Harrow cross of 8 printed by `levelReport` as
  its exception (round 78 L.2 a), not a failure.
