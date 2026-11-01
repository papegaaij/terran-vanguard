---
title: Level 39 – Io Flyby
design: approved
implementation: in-progress
art: proposed
depends-on: [../../../enemies/ground/cracking-tower, ../../../enemies/ground/pump-station, ../../../enemies/ground/strider, ../../../enemies/ground/sam-nest, ../../../enemies/ground/sentinel-tower, ../../../enemies/air/chimera, ../../../enemies/air/talon, ../../../enemies/air/gilded-gunship, ../../../enemies/air/harrow, ../../../world/jovian, ../../../world/belt, ../../../player/systems, ../../../player/generator, ../../../player/wingmen, ../../../systems/retry, ../../act-2-homefront/level-09-arcology-fall]
updated: 2026-11-01
---

# Level 39 – Io Flyby

## Summary

A low pass over Io's sulphur plains to burn the refineries that fuel Vorne's fleet. A `destroy-targets` level:
**six named cracking towers** in three works (Loki, Pele, Prometheus) must all fall; one that scrolls past alive
fails the mission at once. The scroll **holds** over each works (Level 9's hold zones) while its towers stand.
**Volcanic eruptions** throw burning rocks up into the play plane, the
[Strider](../../../enemies/ground/strider/README.md)s return to guard the yards (a topple onto a tower counts), and
the pump stations on the pipeline racks are the secondary. Front only, density 4, ≈ 100 enemies a minute at medium
over 192 s of script time.

**Status: approved 2026-10-31 (round 72, D.5)** (drafted in the Act 6 design phase, step `m10-d-levels-2`, 2026-10-31).
Written from the act roster, the gap analysis's recommended answers (Q3 a, Q7 b, Q8 a, Q10 a), its judgement calls
(J1 reference DPS, J2 media, J3 eruptions, J5 no new tiny unit), its decided L39 items (`destroy-targets` on ≈ 6 named
refinery targets with the failed-primary flow, eruptions, Striders guarding the refineries, SAM Nests) and the wave 1
documents (the [Jovian hazards](../../../world/jovian/README.md#hazards--set-pieces), the Strider's
[Act 6 section](../../../enemies/ground/strider/README.md#act-6-ios-refineries-draft), the
[IFF spoofer](../../../player/systems/README.md#act-6-the-iff-spoofer-and-the-core-tiers-draft), the
[Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft)); every number is a first draft for the
M10 part of this level to measure with `levelReport`.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Rendered from the level's
[data.yaml](data.yaml) (M10 part E, step E2: the approved texts as they are):

<!-- data: briefing -->
> **Commander Okafor:** "Io's refineries fuel every ship Vorne has left. Three works on the
> plains, Loki, Pele and Prometheus, two cracking towers each. All six burn. One left standing
> and his fleet flies on, and we have failed."
>
> **Dr. Varga:** "Io is all volcano. A vent glows, then throws burning rocks up at you: shoot
> them or move. Striders guard the yards; drop one on a tower and the tower counts. And cut the
> pump stations on the racks. Six, and the pipelines run dry."
<!-- /data -->

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-39-io-flyby`: the fleet's survey map of the Io pass: the sulphur plains, the pipeline from
  the Loki works past Pele to Prometheus, the six towers ringed and named, the vent fields hatched, Jupiter's limb
  at the map's top edge.
- Varga's page `level-39-cracking-tower`: her diagram of a cracking tower from above (the gold flare cap, the pipe
  collars), a Strider's fall line drawn across it, a vent's tell beside it with its rocks' fan up the page.

*Hangar teaser* (shop screen before L39; Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "Io. Volcanoes and refineries. I'm bringing the Mortar."
<!-- /data -->

The shop of this visit gets the **Mk V+ "Crucible"** generator and the **IFF spoofer** (Q8 a, Q7 b: the
[hand plan](../../../player/generator/README.md#act-6-hand-plan-draft)), both marked new; the intel panel's
recommended traits name this level's.

## Threat profile

Rendered from the data. Directions are counted by each wave's `from` (the intel's rule): every wave enters from the
front.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `ground` (the cracking towers, Striders, SAM Nests, Sentinel Towers, pump stations), `air` (Talons, Chimeras, Gilded Gunships and their Darts, Hornets, Harrows, Needlers, Skitter and Dart streams, motes) |
| Attack directions | front 100% |
| Density | 4 |
| Recommended traits | `anti-ground`, `piercing` |
| Hazards | Eruptions (vents throw burning rocks up the screen); Sentinel sweeps; the holds over the works |
| Boss / mid-boss | none |
| Special limits | Sonar Pulse `NO WATER` ([Act 6 specials](../../../player/specials/README.md#act-6-specials-draft)); the Airstrike and the Orbital Lance work over Io |
| Sensor-suite detail | **none**: Io, the refineries, `ground` + `air`, front, the objective `TOWERS 0 / 6` · **L1**: + density 4, the hazards "eruptions" and "beam sweeps", the secondary `PUMPS 0 / 6` · **L2**: + Strider, Sentinel Tower, SAM Nest, Chimera, Talon, Gilded Gunship, Hornet, Harrow, Needler, Dart Drone, Skitter and Mote portraits · **L3**: + the six towers and the three holds on the threat map, `anti-ground` highlighted |
<!-- /data -->

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Io's refineries. Six towers to burn, and volcanoes." |
| L1 | "Vents throw rocks up. Every tower must fall." |
| L2 | "Striders guard the works. SAMs, Sentinels, Chimeras." |
| L3 | "Anti-ground for the towers; a Strider's fall counts." |

## Objective

- **Primary** `destroy-targets`: destroy the six named **cracking towers**, each its own ground-target group:
  **Loki A, Loki B** (section 2), **Pele A, Pele B** (section 4), **Prometheus A, Prometheus B** (section 6). The
  tracker reads **`TOWERS n / 6`**.
  - **Fails at once** (the campaign's [objective failure](../../README.md#pacing-rules), as Levels 05 and 09): the
    first tower that leaves the screen alive fails the mission through the
    [failed primary objective](../../../systems/retry/README.md#on-a-failed-primary-objective) flow (the mission
    failed screen 3 s later; the lost-mission prompt as built, without the fly-on option), with Okafor's
    `mission-failed` line naming the tower.
  - **A topple counts**: a [Strider](../../../enemies/ground/strider/README.md#act-6-ios-refineries-draft) toppled
    onto a tower destroys it and counts for the objective (the player aimed the fall); the Strider's Act 6 section
    leaves this to this level: the towers are ground units, so yes.
  - **Holds** over each works: see *Layout*.
- **Secondary** `escapes` (the pump stations): destroy at least **6 of the 8** pump stations on the pipeline racks
  before they scroll off (`escapes: pump-station`, `allowed: 2`, as Level 19's grey tanks), met as soon as it can no
  longer fail (the sixth down: the tracker reads `DONE`), failed at once by the third away. The tracker's second line
  counts to its target, **`PUMPS n / 6`** (the Act 5 lesson). Pays **100** credits in Act 1 terms (**1,049** in Act 6,
  the act factor 1.6⁵ = 10.486); the typical haul counts 50 %. (A `destroy-targets` primary rules out a `groups`
  secondary: the [level schema](../../../tech/architecture/schemas-levels/README.md).)

### The cracking tower and the pump station

Two ground objects of this level (the refinery kit; the build adds them to the enemies' data, see *Open questions*).
Neither shoots: the works' guards do. Neither is hardened: every weapon that hits `ground` hurts them, `anti-ground`
does ×2.

| | Cracking tower | Pump station |
|---|---|---|
| Layer, tier | `ground`, `large` | `ground`, `small` |
| Size | 96×96 px, hitbox 72×72 (a round column from above, its gold flare cap burning) | 40×40 px, hitbox 32×32 (a pump housing on a pipeline rack) |
| HP | **260** (easy 195 / hard 338 by the global multipliers): ≈ 1.7 s at the L39 reference 156 (J1), a `large` part's 1–3 s | **20** |
| Credits (Act 1 terms) | 30 (under the Hive Node's 40: the scale's band, see *Credit budget*) | 8 |
| Death | Io's near-vacuum ([media](../../../world/jovian/README.md#media-per-level)): a short white flash, the flare cap tumbling off, a ring of burning debris that does not rise; the dark wreck stays, the pipes from it stop glowing | a short flash and a debris ring; the rack's pipe beside it goes dark |
| IFF spoofer | not a turret (nothing to spoof) | not a turret |

## Layout

Scroll **130 px/s** (script time: every time here is the scroll distance ÷ 130; in a hold the level clock slows with
the scroll, Level 9's rule). Setting: [the Jovian system](../../../world/jovian/README.md#sub-locations-per-level)'s
**Io refineries**, Io's **near-vacuum** (the belt's [vacuum rules](../../../world/belt/README.md#vacuum-rules) as
the [Jovian media](../../../world/jovian/README.md#media-per-level) say: short flashes and debris rings, plumes as
umbrellas, dust settling fast, no weather). **Light** ([mood](../../../world/jovian/README.md#moods-per-level-q10),
Q10 a): black lava, desaturated sulphur yellow and ochre plains, white frost, orange vent glow; a harsh small Sun.
The level opens on the plains (the ground covers the screen from t = 0); **no Jupiter** on this top-down ground level
(the user, round 77 F.1 redo: Jupiter only in the briefing images and the space levels). Motion budget: the eruptions
and the Sentinel sweeps are the strong elements; the pipeline racks and the works stay quiet. Every
plume haze and vent glow is smooth (≥ 32 steps, no ordered dither, drawn once: the Act 5 lesson).

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Tvashtar Plains | 0–20 | 0–2,600 | 130 | clear | `ground`: sulphur flats, black lava flows, frost fields from t = 0 (no limb, no Jupiter; round 77 F.1 redo); one vent (t≈12). | The approach; **the eruption taught** alone. |
| 2. Loki Works | 20–62 | 2,600–8,060 | 130 | clear | `ground`: the yard road, pipeline racks, the Loki towers A and B (t≈43–44), the Strider pair (t≈32), a SAM pair (t≈33), three vents. | The first Strider pair (a topple to aim), **the first hold**. |
| 3. The Pipeline Run | 62–104 | 8,060–13,520 | 130 | clear | `ground`: the racks along a lava channel, pump stations P1–P4, vents alternating sides, the survey hut on a lava island (t≈96). | The Gunships; the pumps; the first hunt. |
| 4. Pele Works | 104–142 | 13,520–18,460 | 130 | light | `ground`: the works' gate with the Sentinel pair (t≈110), the yard, the Pele towers (t≈125–126), the Strider pair walking up between them, a SAM trio. `high-air`: Pele's plume haze. | **The second hold**: towers behind sweeping beams, a topple onto a tower. |
| 5. The Plume Field | 142–164 | 18,460–21,320 | 130 | light | `ground`: a field of vents under Pele's umbrella plume, rows of three; pump stations P5–P8 on a rack crossing it; a SAM pair. `high-air`: Pele's plume haze. | The eruptions at their densest; the pumps' last chance. |
| 6. Prometheus Works | 164–192 | 21,320–24,960 | 130 | clear | `ground`: a lone Strider (t≈173.5), a SAM pair (t≈172), the Prometheus towers (t≈175–176), two vents; the works going dark behind. | **The last hold**; the calm before the end. |
<!-- /data -->

**Hold zones** ([Level 9](../../act-2-homefront/level-09-arcology-fall/README.md#layout)'s, built): when the first
tower of a works reaches **y = 200 px** below the top edge (t≈44, ≈126, ≈176), the scroll eases over 1 s to **40 px/s**
(easy 30, hard 50) and stays there until both towers of that works are gone, then eases back to 130 px/s over 1 s. A
tower that is not destroyed leaves the bottom edge about **9.7 s** after the hold starts at medium (12.9 s easy, 7.8 s
hard): that is the window; there is no timer. (Measured in step E2, `levelReport`'s `refinery` part: **8.3 s** at medium,
10.9 s easy, 6.7 s hard from the hold's start to the first tower gone: the 1 s ease and the tower's own edge; the
approved 200 px and 40/30/50 px/s kept, decided 2026-11-01 (round 77 L.2).) In a hold the waves, the ground units' placements and the radio wait with
the scroll; the enemies, bullets, eruptions, Rook and the backdrop's animation run on real time, so the vents beside a
works erupt more than once during its hold. The holds add about 15 s of real time (≈ 3 min 27 s in all). No vent lies
within 100 px of a tower. Holds are not used with a boss or set pieces
(the schema's rule): this level has neither.

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). All numbers are **draft**: data in **Act 1
terms** with a `bounty_scale` (see *Credit budget*).

- **HP**: the units' own pages at the proposed L39 reference DPS **156** (J1): the Strider at its
  [Act 6 values](../../../enemies/ground/strider/README.md#act-6-ios-refineries-draft); the Sentinel Tower with the act
  HP factor of a returning `medium` unit (×156/132 from its L32 values); the Talon, the Gilded Gunship and the Hornet
  with the **Act 6 shields** ([enemy shields](../../../enemies/README.md#enemy-shields)), the Gunship and the Hornet
  also with the act HP factor; the Chimera and the Harrow as their drafts; the SAM Nest, the Skitter, the Needler, the Dart and the
  motes as built.
- **Density and pacing** (the Act 2+ rules, `PacingTest` and `ActFloorTest`, the act's 75 a minute): **320 enemies**
  (57 waves with 279 units, 24 Darts launched by the Gunships, 17 ground units) over 192 s, **≈ 100 a minute** at
  medium (easy ≈ 265, hard ≈ 365 by the formation lever). Built (step E2, decided 2026-11-01 (round 77 C.2)): the 6
  towers and 8 pump stations are **counted** enemies (the [Cracking Tower](../../../enemies/ground/cracking-tower/README.md)
  and the [Pump Station](../../../enemies/ground/pump-station/README.md): a topple's crush and the tally take counted
  units only), the five Striders walker waves (Level 32's): `levelReport` counts **310** at medium (284 in waves, 26
  on the ground), **99.5 a minute** (easy 258, hard 362).
  A wave about every 3 s, no stretch over 2 s with nothing on the screen (the ground units and the waves already in
  hold the holds). The shots come from the Talon and Chimera pairs, the Gunships' rings, the Hornets' and SAM Nests'
  missiles, the Striders' fans, the Sentinels' sweeps and the Harrows' rings; the build sets the rates with
  `levelReport`.
- **Tiny units** (J5): the Dart Drone streams and the Gunships' Darts, and the Mote Swarms (54 Darts, 72 motes).
- **Quiet windows**: no wave enters from t=8 to t=14.5 (the first eruption at t≈12 plays alone, Level 27's teaching
  rule), from t=28.5 to t=35.5 (the Strider pair, t=32) or from t=104.5 to t=113.5 (the Sentinel pair, t=110; built:
  the Skitter stream at t=104.5, the Dart stream at t=101.5 and the Skitter stream at t=141, each moved by at most 1 s from the draft's
  105.5, 102.5 and 140.5 in step E3 for the pacing rule, decided 2026-11-01 (round 77 C.8)).
- **Limits**: at most 2 Striders walking and 2 Sentinels sweeping at once; at most 3 SAM Nests firing; at most 6
  Harrows (3–4 a stream here); no two `medium` hulls overlapping (the Gunship columns 2 s apart, the Hornet pair
  ≥ 80 px between hulls, a Hornet never with a Gunship).
- **The three-way war** ([Q3 a](../../../enemies/README.md#the-three-way-war), decided 2026-10-31 (round 72)): two hunts. The
  t=75.5 Skitters ram the t=74.5 Talon line; the t=150.5 motes lunge at the t=149.5 Talon wing. A prey unit the Vrell
  kill pays the player nothing.

Rendered from the data (M10 part E, step E2; every wave `front`; the five Striders are walker waves as Level 32's,
see *Ground targets*):

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 5.5 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 8 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | The last wave before the first eruption (tell t≈12.8, alone) |
| 14.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | After the first eruption |
| 17 | 1 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | |
| 19.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 22.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 25.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 28.5 | 2 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | The last wave before the Strider pair's quiet window (t 32–35.5) |
| 32 | 2 | convoy | [Strider](../../../enemies/ground/strider/README.md) | 2 | front | The Strider pair down the yard road: a topple onto the SAM pair to aim; the second drops a shield cell |
| 35.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 38.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 41.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Enters before the Loki hold (t≈45); flies on real time in it |
| 44.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 3 | front | A short stream from the front among the Loki towers |
| 47.5 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 50.5 | 2 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | |
| 53.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 56.5 | 2 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front | |
| 59.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 62.5 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | 2 s apart; each launches its Darts (about 12 in all) |
| 65.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 68.5 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 71.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 74.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | **Prey** of the t=75.5 Skitters (three-way war) |
| 75.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | **Hunters** of the t=74.5 Talons: they ram them |
| 78.5 | 3 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 81.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 84.5 | 3 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | ≥ 80 px between the hulls; the Gunships of t=62.5 are gone |
| 87.5 | 3 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | |
| 90.5 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 93.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 96.5 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 99.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 101.5 | 3 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | |
| 104.5 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | The last wave before the Sentinel pair's quiet window (t 110–113.5) |
| 113.5 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 116.5 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip; it drops the overdrive (the README's left Sentinel: see the header) |
| 119.5 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front | Over the Pele yard |
| 122.5 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Enters before the Pele hold (t≈127) |
| 122.5 | 4 | convoy | [Strider](../../../enemies/ground/strider/README.md) | 2 | front | The Strider pair walking up the yard between the Pele towers: a topple onto Pele A or B destroys it and counts |
| 125.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 128.5 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 131.5 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 134.5 | 4 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | |
| 137.5 | 4 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 141 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 143.5 | 5 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | 2 s apart; each launches its Darts (about 12 in all); the Plume Field |
| 146.5 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 149.5 | 5 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip; **prey** of the t=150.5 motes |
| 150.5 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | **Hunters** of the t=149.5 Talons: they lunge at them |
| 153.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 156.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 159.5 | 5 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front | Over the plume field |
| 162.5 | 5 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | |
| 165.5 | 6 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Prometheus works |
| 168.5 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 171.5 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 173.5 | 6 | single | [Strider](../../../enemies/ground/strider/README.md) | 1 | front | A lone Strider walking up between the Prometheus towers: a topple onto one counts |
| 174.5 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | Enters before the Prometheus hold (t≈177) |
| 177.5 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | **The last wave**, a leader at the tip; the calm from ≈ t=185 to the end at t=192 |

Totals: Skitter 60 · Talon 59 · Mote Swarm 72 · Dart Drone 30 · Chimera 32 · Needler 9 · Strider 5 · Harrow 11 · Gilded Gunship 4 · Hornet 2.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 2 | [SAM Nest](../../../enemies/ground/sam-nest/README.md) pair by the Loki yard's rack (t≈33) | Bounty 15 each; the Strider pair walks past them |
| 2 | [Cracking Tower](../../../enemies/ground/cracking-tower/README.md) **Loki A** (t≈43) | Primary target; the Loki hold |
| 2 | Cracking Tower **Loki B** (t≈44), 180 px from Loki A | Primary target; the Loki hold |
| 3 | [Pump Station](../../../enemies/ground/pump-station/README.md)s **P1–P4** on the rack (t≈68, 76, 84, 92) | Secondary; bounty 8 each |
| 3 | SAM Nest on the rack between P2 and P3 (t≈80) | Bounty 15 |
| 3 | The survey hut's dark door lock (30 HP, t≈96) | The secret: frees a crate worth 100 (1,049) |
| 3 | A refinery supply depot's latch (10 HP, t≈103) | An armour patch |
| 4 | [Sentinel Tower](../../../enemies/ground/sentinel-tower/README.md) pair on the Pele gate (t≈110 left, t≈112 right), their rows offset so the two never sweep the same rows at once | Bounty 40 each |
| 4 | SAM Nest trio in the Pele yard (t≈122) | Bounty 15 each |
| 4 | Cracking Tower **Pele A** (t≈125) | Primary target; the Pele hold |
| 4 | Cracking Tower **Pele B** (t≈126) | Primary target; the Pele hold; a Strider toppled onto it destroys it and counts |
| 5 | Pump Stations **P5–P8** on the rack across the plume field (t≈146, 152, 158, 162.5) | Secondary; bounty 8 each |
| 5 | SAM Nest pair on the plume field (t≈150) | Bounty 15 each |
| 6 | SAM Nest pair at the Prometheus works' gate (t≈172) | Bounty 15 each |
| 6 | Cracking Tower **Prometheus A** (t≈175) | Primary target; the Prometheus hold |
| 6 | Cracking Tower **Prometheus B** (t≈176) | Primary target; the Prometheus hold |
<!-- /data -->

The five [Strider](../../../enemies/ground/strider/README.md)s are walker waves (see *Waves*), 32 px/s up the ground:
the Loki pair (t=32 and 33.5) up the yard road at x 90, the first passing the SAM pair's row (a topple onto it to aim),
the second dropping a shield cell; the Pele pair (t=122.5 and 124) and the lone Strider (t=173.5) up the racks
between their works' towers, so that in the hold they walk beside them and a topple can take one (step E2: the
README's t≈118 and t≈168 left the screen before the towers came; the Loki SAM pair at t≈33, not 38, for the same
reason; decided 2026-11-01 (round 77 C.3, C.4)). The Sentinels' rows are 221 px apart (Level 32's offset: the right one
1.7 s after the left). The left Sentinel's overdrive comes from the t=116.5 wing's leader inside the gate (a ground
enemy carries a pickup only as an objective's group). Totals: towers 6 · pump stations 8 · Striders 5 · SAM Nests 10 ·
Sentinel Towers 2. The
[IFF spoofer](../../../player/systems/README.md#act-6-the-iff-spoofer-and-the-core-tiers-draft) (in the shop from
this visit) delays every SAM Nest, Sentinel and Strider here: the level that shows what it does.

## Hazards

- **Eruptions** (J3, decided 2026-10-31 (round 72)): the [Jovian eruption](../../../world/jovian/README.md#hazards--set-pieces)
  as its document defines it (the vent's tell, the burning rocks flying up the screen, their contact damage, the
  umbrella haze, the easy and hard values). This level sets where and when: **27 vents**, each erupting every 5–7 s
  while on the screen:
  - section 1: one vent (entering at t=12, its tell at t≈12.8, x≈340), alone before any wave reaches it;
  - section 2: three (t≈30 by the yard road; two beside the Loki towers, entering at t≈44 so that they erupt in the
    hold: retimed from t≈50 and t≈55, m10e-gaps §4);
  - section 3: six single vents along the lava channel, alternating sides (t≈66, 72, 79, 86, 94, 100);
  - section 4: three (t≈113 inside the gate; two beside the Pele towers entering at t≈126, retimed from t≈133 and
    t≈138);
  - section 5: **four rows of three**, rippling 0.4 s a vent (t≈145, 150, 155, 160), the rack with P5–P8 between the
    rows;
  - section 6: two (t≈170; one beside the Prometheus towers entering at t≈176, retimed from t≈181).
  Built (step E2, decided 2026-11-01 (round 77 L.1 a)): each vent's first tell comes `first` s after it enters (0.3–1.3 s, the
  plume field's rows 0.3, 0.7 and 1.1 s: its ripple), then every 5–7 s on real time; at 130 px/s a vent outside a hold
  erupts once, the works' vents tell about 0.3–0.6 s into their hold and again 6.2–8.2 s later (`levelReport`: two tells
  a vent in every hold at medium).
  The rocks fly **up** the screen, so a vent is most dangerous to a ship that pushes forward to reach a tower: the
  level's tension in the holds. No vent within 100 px of a tower or a pump station.
- **Sentinel sweeps** at the Pele gate ([its page](../../../enemies/ground/sentinel-tower/README.md)).
- **Special limits** (J2): none but the Sonar Pulse (`NO WATER`); the Airstrike's CDF bombers and the Orbital Lance
  work over Io; the Bomb Rack and the Hammer Mortar have their ground. The EMP Burst stuns the guards (the Striders,
  Sentinels and SAM Nests are machines); the Smart Bomb clears the rocks in flight (debris, not units: it does what it
  does to Act 5's breakable debris).

## Secrets and pickups

- **The survey hut** (the secret, section 3, t≈96): a Coalition survey hut from before the Ascendancy took Io, half
  buried on a lava island off the racks, its door lock dark (no lights, unlike the works). A **30 HP** lock;
  destroyed, it frees a crate worth **100** in Act 1 terms (1,049). The Salvage scanner's glint shows it at any range.
  Its event line, Varga: "*A Coalition survey hut. Pre-war. Somebody left in a hurry.*"
- **Shield cell**: dropped by the second Strider of the t≈32 pair.
- **Armour patch**: the refinery supply depot before the Pele gate (t≈103), freed by its 10 HP latch.
- **Overdrive**: dropped by the left Sentinel Tower of the Pele gate (t≈110); built (step E2) as the t=116.5 wing's leader inside the gate (a single ground enemy cannot carry a pickup), decided 2026-11-01 (round 77 C.6).

## Radio chatter

Spoken radio, draft text for the **1 s rule** (text at about 12 characters a second plus a 1.5 s lead for the length
check; 0.4 s of silence; no timed line more than 1 s late); event lines drop after 6 s. Rook's lines carry `requires:
escort`. Vorne speaks once on the enemy channel (the intercepted dress), cold and never shouted (the character rule:
his only shout is Level 42's, J6), his t=91 line at his grim row (`expression: rage`, as his Level 37 order; decided 2026-11-01, round 77 V.2 b: the data and a new take follow, see *Implementation*).

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Io. Three works on the plains, two towers each. Every one burns." |
| t=8.5 | Varga | "Vents. They glow, then throw burning rocks up. Shoot them or move." |
| t=32.5 | Varga | "Striders on the yard. Drop one on a tower and the tower counts." |
| t=41 | Okafor | "Loki works. We hold over the towers until both burn." |
| The first tower destroyed (`first-target`, event, bark) | Rook | "Tower's down. Sorry about the fuel bill, Vorne." |
| t=63 | Varga | "Pump stations on the racks. Cut six and the lines run dry." |
| t=76.5 | Varga | "Skitters on their Talons. The Vrell aren't picking sides." |
| t=91 (t=91, the enemy channel (cold, never shouted)) | Vorne | "Lancer. Burn Io if you like. Callisto holds fuel enough for what I still have to do." |
| t=104.5 | Okafor | "Pele works. Towers behind the gate. Same drill." |
| t=110.5 | Varga | "Sentinels on the gate. They sweep all the way round." |
| The first tower destroyed by a topple (`topple-target`, event) | Okafor | "Dropped it on the tower. That'll do." |
| Secondary met (the sixth pump, event) | Varga | "Six pumps cut. The racks are running dry." |
| t=142.5 | Okafor | "The plume field. Pele has erupted for a thousand years. It won't stop for us." |
| t=164.5 | Okafor | "Prometheus. The last two towers. Burn them and his fleet runs dry." |
| Level end | Okafor | "Io's lines are dark. Callisto next, Lancer." |
| A tower leaves the screen alive (`mission-failed`, the failed screen's line) | Okafor | "{group} is still burning. That fuel flies against us. Pull back, Lancer." |
<!-- /data -->

Timeline check (text, by the rule above; script time, so a hold stretches the gaps): the t=1 line closes at about
t=7.8, before Varga's vent line (t=8.5), which closes at about t=15.5, as the first eruption's rocks fly (tell t≈12).
The Strider line (t=32.5) closes at about t=39.2, before Okafor's Loki line (t=41), which closes at about t=46.8 as
the hold begins (t≈44). The pumps line (t=63) closes at about t=69.3, after P1 (t≈68) is on the screen; the hunt line
(t=76.5) at about t=82.8; **Vorne's line** (t=91) at about t=99.5; the Pele line (t=104.5) at about
t=109.9, before the Sentinel line (t=110.5), which closes at about t=116.3. The plume-field line (t=142.5) closes at
about t=150.4; the Prometheus line (t=164.5) at about t=171.5, before the last hold. Without Rook his line drops.

## Music & ambience

[Track 14](../../../audio/music/README.md#track-list), the Act 6 A theme **"Eye of the Storm"** (the chosen sketch of
round 03; full length and its stems still to make), drafted here as: its open storm strings over the Tvashtar plains,
the base through the works and the pipeline run, the full mix in the Pele works and the plume field, Vorne's motif
under his line (t=91; the radio's duck only). The cues are the music plan's
([cues](../../../audio/music/README.md#cues-per-level-act-6)). Ambience: the `io-refinery` bed
([sfx](../../../audio/sfx/README.md#ambience-per-setting): a deep ground rumble through the rock, refinery machinery,
the hiss of venting gas); the eruption's tell and roar from Level
27's set in a dry variant, with the rocks' crack and thud; the towers' collapse (a heavy metal groan and a short
blast). New sounds this level needs: the dry eruption, the rocks, the tower's fall, the pump station's blast; the
Strider, the Sentinel and the SAM Nest are built. Listed in [sfx](../../../audio/sfx/README.md) by the act's audio
step (Freesound for real-world sounds only, CC0/CC-BY; no bubbly or popping sounds).

## Credit budget

Budget(39) = 700 × 1.07³⁸ ≈ **9,155** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act 1
terms** (Skitter 5, Mote 2, Talon 12, its leader 25, Dart 2, Chimera 14, Harrow 12, Needler 12, Gilded Gunship 28,
Hornet 26, Strider 60, SAM Nest 15, Sentinel Tower 40, the cracking tower 30, the pump station 8, the secret's crate
100, the secondary 100), × the act factor **10.486** (1.6⁵), bounties × the scale. A **`bounty_scale` of 0.41** puts
the typical haul **+2.6 %** over the budget by hand (+0.3 % measured, below) (the economy's shares: air kills 60 %, ground targets 80 %, the secret and
the secondary 50 %). The primary's groups pay no objective credits (the schema's rule); each tower pays its bounty.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 60 × 5 + Talon 59 × 12 + Mote Swarm 72 × 2 + Dart Drone 30 × 2 + Chimera 32 × 14 + Needler 9 × 12 + Strider 5 × 60 + Harrow 11 × 12 + Gilded Gunship 4 × 28 + Hornet 2 × 26 + launched Dart Drone 24 × 2 | 10,416 | 6,508 |
| Squad leaders: 7 × 13 extra (perfect run only) | 385 | 0 |
| Ground targets: SAM Nest 10 × 15 + Cracking Tower 6 × 30 + Pump Station 8 × 8 + Sentinel Tower 2 × 40 | 2,030 | 1,624 |
| Secret: survey hut (hidden crate, 1% of budget) | 1,049 | 524 |
| Secondary: cut 6 of the 8 pump stations | 1,049 | 524 |
| **Total** (bounty scale 0.41) | **14,929** | **9,181** |
| Budget(n) = the typical haul's target; typical +0 %, perfect 1.63 × budget | | 9,155 |
<!-- /data -->

The towers pay **30** (the main agent's call, 2026-10-31) so the scale sits inside the gap analysis's 0.40–0.56 band:
at 40 a tower it was 0.39 (the haul +0.3 %), as the level's ground share (a third of the haul) is high for a
front-only level. A won run destroys all six towers, so the typical haul's 80 % of them is cautious; the build may
count them at 100 % (as the primary's objects) and lower the scale to ≈ 0.39.

**Measured** (`levelReport`, step E2): the typical haul **9,181 (+0.3 %)** at the README's scale **0.41** (kept: it
hits the budget), the perfect run 14,929 (1.63 × budget); easy 8,174, hard 10,187. With the towers at their flown
share (every won run takes all six, 100 % instead of 80 %) the typical haul is ≈ 9,336 (+2.0 %), inside the ±5 %
either way. The hand table's 9,393 counted the Striders as ground targets; built as walker waves they pay as kills.

**Balance plan** (the [Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft), its L39 row): the
visit buys nothing (saving for the Mk V+ and the spoofer, bought at the L40 visit) and fits Rook's **Mortar** for the
towers; the loadout is the L37–L38 one. The level asks `anti-ground` (the towers, the Striders' legs) and `piercing`
(the yards' lined-up guards and the Talon lines). The plan has no `anti-ground` gun of its own: the beam and the pods
take the towers (≈ 1.7 s each at the reference), Rook's Mortar and the topples help; the Bomb Rack or the Hammer Mortar
is the recommended trait's answer and halves a tower's time. The plan flies at 22 of 22 MW here, so the spoofer this
level rewards does not fit it (see *Open questions*).

## Difficulty notes

- **Easy**: the holds at 30 px/s (a 12.9 s window drafted; 10.9 s as built, see *Layout*); the eruptions' easy values (the Jovian document's); the Sentinels'
  easy arcs; the Striders' and SAM Nests' easy hooks; the formation lever thins the popcorn.
- **Hard**: the holds at 50 px/s (a 7.8 s window drafted; 6.7 s as built); the eruptions' hard values; the Sentinels' hard arcs with full
  turns; the Striders' fans of 5 and salvos of 3, the SAM Nests' salvos of 3; the towers' hard HP (338).

## Concept art

Round 77 (M10 part E, step ART-E; production art, proposed): Io and the refinery kit by `tools/art/backdrop_l39.py`
(on the shared Jovian pieces of `tools/art/jovkit.py`), in round 72's L39 grade (desaturated sulphur and ochre, black
lava, white frost, the mean about 115): the level **opens on Io's plains** (round 77 F.1 redo, `r77-b`: the ground
covers the screen from t = 0, no limb, no Jupiter, no plume on the horizon; section 1's ground is two opaque pieces from
under the screen's bottom edge up; the `r77-a` opening over Io's limb is superseded), one periodic terrain under every section's tile set (the yard roads, the pipeline racks, the lava channel,
Pele's red-orange fallout, the plume field's vent crusts, Prometheus's frost) with seam pieces where the racks bend;
the three works from one kit (the yard slab, the towers' octagonal pads, tanks, a shed, lamps, the rack through the
yard), each tower's pipe run to the rack and each pump station's rack segment with a hot line (additive `-glow`) that
goes dark when its tower or pump dies (`dark_with`, the game's hook), the Pele gate with the Sentinels' blockhouses, the
supply depot, the survey hut on its lava island, three cold vents under the 27 vents, Pele's plume haze over sections
4–5 (at most 16 %). The units by `tools/art/refinery_units.py`: the cracking tower (its shadow in its alpha, the flare
cap's flame additive), its fall (a short flash, the top tumbling off, burning chunks in a ring that does not rise) and
its wreck; the pump station and its wreck; the depot's latch and the hut's lock; the eruption's vent tell, burst,
umbrella and burning rocks are the [Jovian system](../../../world/jovian/README.md#concept-art)'s
(`eruption-final-r77-a`), the briefing images the [briefing](../../../ui/briefing/README.md#concept-art)'s
(`briefing-images-final-r77-a`). The proposal (`concept/backdrop-proposal.yaml`, step E2's input, not data) holds the
backdrop block, the sections, the holds, the ground placements (towers, pump stations, guards), the hut's lock and the
depot's latch, and the 27 vents with their `first` (the works' vents retimed into their holds: two throws in each hold
at medium, checked by the generator). Prompts and briefs: [concept/prompts.md](concept/prompts.md). Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r77-b.png](concept/backdrop-final-r77-b.png) | The F.1 redo: the production backdrop opening on Io's plains from t = 0 (no limb, no Jupiter, no plume): twelve screens at half size with stand-ins (t=0 the plains, t=6 the Tvashtar plains, the first vent, then as r77-a), the readability crops at 1×, the pieces, the tile sets (sheet) | proposed (round 77 F.1 redo) |
| [concept/backdrop-final-r77-b.gif](concept/backdrop-final-r77-b.gif) | The F.1 redo: the first 10 s at 130 px/s (half size), the plains under the ship from t = 0 | proposed (round 77 F.1 redo) |
| [concept/backdrop-final-r77-a.png](concept/backdrop-final-r77-a.png) | Superseded by r77-b (F.1 redo) in its opening. Production backdrop: twelve screens at half size with stand-ins (t=0 Jupiter low under Io's limb and Tvashtar's plume, the limb coming down, the first vent, the Loki yard and its SAMs, the Loki hold, the pipeline run and P1, the survey hut, the depot, the Pele gate, the Pele hold under Pele's haze, the plume field, the Prometheus hold), the readability crops at 1× (each section's darkest and brightest window with a Talon, a Chimera, a Harrow, amber needles, an orb, a rock, the ship; the means), the pieces, the tile sets (sheet) | chosen (round 77 F.1 accept; the sheet's opening over the limb is superseded by r77-b, the redo) |
| [concept/backdrop-final-r77-a.gif](concept/backdrop-final-r77-a.gif) | The Loki hold at medium on real time (half size): the scroll easing to 40 px/s, both vents beside the towers telling and throwing their rocks, the umbrellas, Loki A falling and its pipes going dark | chosen (round 77 F.1 accept; the Loki hold; the opening redone in r77-b) |
| [concept/refinery-final-r77-a.png](concept/refinery-final-r77-a.png) | The cracking tower burning (body + flame), its fall in 12 frames, the tower and its wreck at 3×, the flame frames, the pump station and its wreck, the depot's latch (intact, open), the hut's lock (dark, blown) (sheet) | chosen (round 77 F.2 accept) |
| [concept/refinery-final-r77-a.gif](concept/refinery-final-r77-a.gif) | Two towers burning, one falling to its wreck, a pump station destroyed (2×) | chosen (round 77 F.2 accept) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (tile sets, pieces, placements with `dark_with`, the atmospheres), the sections, the holds, the ground placements, the ground objects and the 27 vents for step E2 (not data) | proposed (round 77; step E2's input) |
| [concept/hold-capture-r77-a.png](concept/hold-capture-r77-a.png) | In the game: the Loki hold, both towers, both vents beside them throwing, the rocks and umbrellas, Loki A and B falling (the plan's fit, parked) | chosen (round 77 F.2 accept) |
| [concept/hold-capture-r77-a.gif](concept/hold-capture-r77-a.gif) | In the game: the Loki hold in motion | chosen (round 77 F.2 accept) |
| [concept/pipes-capture-r77-a.png](concept/pipes-capture-r77-a.png) | In the game, 1:1: Loki A's pipe run hot, the kill, the fall, the wreck with its pipe dark | chosen (round 77 F.2 accept) |
| [concept/pump-capture-r77-a.png](concept/pump-capture-r77-a.png) | In the game, 1:1: a pump station on the rack hot, hit, its burst, the wreck with the rack's pipe dark | chosen (round 77 F.2 accept) |
| [concept/gate-capture-r77-a.png](concept/gate-capture-r77-a.png) | In the game: the Pele gate with its walls and the two Sentinels sweeping | chosen (round 77 F.2 accept) |
| [concept/topple-capture-r77-a.png](concept/topple-capture-r77-a.png) | In the game: a Pele Strider's leg cut beside Pele B, the fall line, the fall crushing the tower (`TOWERS 3 / 6`, Okafor's line) | chosen (round 77 F.5 accept) |
| [concept/topple-capture-r77-a.gif](concept/topple-capture-r77-a.gif) | In the game: the topple in motion | chosen (round 77 F.5 accept) |
| [concept/plume-capture-r77-a.png](concept/plume-capture-r77-a.png) | In the game: the plume field's first row, its three tells 0.4 s apart, the rocks and umbrellas | chosen (round 77 F.3 accept) |
| [concept/plume-capture-r77-a.gif](concept/plume-capture-r77-a.gif) | In the game: the row's ripple in motion | chosen (round 77 F.3 accept) |
| [concept/failed-capture-r77-a.png](concept/failed-capture-r77-a.png) | In the game (not invulnerable): Loki A unshot, off the bottom edge with `TOWERS FAILED` at once, the failed screen with Okafor's line | chosen (round 77 F.6 accept) |
| [concept/failed-capture-r77-a.gif](concept/failed-capture-r77-a.gif) | In the game: the tower passing and the failed screen | chosen (round 77 F.6 accept) |
| [concept/hut-capture-r77-a.png](concept/hut-capture-r77-a.png) | In the game, 1:1: the survey hut on its lava island (its lock), the supply depot and its latch | chosen (round 77 F.4 accept) |
| [concept/readability-capture-r77-a.png](concept/readability-capture-r77-a.png) | In the game, 1:1 crops: gold Talons and their bullets, burning rocks, the Pele yard, Prometheus's frost and the lava channel | chosen (round 77 F.7 accept) |
| [concept/level-39-listen-r77-a.ogg](concept/level-39-listen-r77-a.ogg) | Listen-through: Level 39's two briefing pages and its radio as the game plays them (`assets/voice/` joined, 1.2 s apart; 3:03.5, −19 LUFS); `tools/concept/audio/voice_r77.py` ([brief](concept/prompts.md#level-39-listen-r77-a)) | chosen (round 77 V.1 accept) |
| [concept/level-39-listen-r77-a.txt](concept/level-39-listen-r77-a.txt) | The listen-through's cue list: each line's start, speaker, source, expression and text | chosen (round 77 V.1 accept; a listening aid) |
| [concept/vorne-t91-grim-r78-a.ogg](concept/vorne-t91-grim-r78-a.ogg) | Vorne's t=91 line at his grim row (round 77 V.2 b; step VOX-F's take, `expression: rage`, the game's file), for the user's ear in round 78; `tools/concept/audio/voice_r78.py` | proposed (round 78) |

## Implementation

- [x] Level data `data.yaml` (when built): sections and speed, the six tower groups as the `destroy-targets` primary,
      the three holds, the `escapes: pump-station` secondary with `allowed: 2`, waves, ground targets, the vents per
      difficulty, the two hunter/prey links, radio (the `mission-failed` cue with `{group}`), budget, threat profile,
      briefing, backdrop, music
- [x] The cracking tower and the pump station in the enemies' data (ground objects, no attack, not hardened): two
      minimal unit directories (step E2)
- [x] The failed primary: a tower leaving the screen alive fails at once (the built flow, Levels 05 and 09); a topple
      onto a tower counts for the objective (`Level39Test`; step E5's captures: `TOWERS FAILED` the step Loki A leaves,
      the failed screen with its line; a Strider toppled onto Pele B)
- [x] Holds over the three works (Level 9's), validated against the eruptions on real time (`Level39Test`: windows
      10.9/8.3/6.7 s, two tells a vent per hold at medium; round 77 L.2)
- [x] Eruptions as the Jovian document's rule (the tell, the rocks as shootable debris, the haze), the first alone;
      no vent within 100 px of a tower or a pump; Rook's eruption avoidance
- [x] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), the act's 75 a minute (`DensityTest`), the
      quiet windows, ≥ 4 s of calm after the last enemy
- [x] The two hunts (Q3 a); prey killed by the Vrell unpaid
- [x] `TOWERS n / 6` and `PUMPS n / 6` on the two-line tracker (`Level39LooksTest`)
- [x] Radio voiced, every timed line at most 1 s late, with Rook and without (`requires: escort`); Vorne's line on
      the enemy channel, not shouted (`RadioTimelineLevel39Test`, `VoiceFilesTest`; a run that meets `PUMPS` after
      t≈158 holds Okafor's t=164.5 line more than 1 s: round 77 C.12)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel
- [x] Briefing: both pages, each one screen, with their images
- [x] Typical haul within ±5 % of budget(39) = 9,155
- [x] Music ("Eye of the Storm" full length and stems by section) and the Io bed; the dry eruption sounds
- [x] Easy/hard variations; the autopilot flies it on every difficulty, with and without Rook, and wins it (all six
      towers) (`Level39Test`, `RefineryPlay`: 18 of 18 flights)
- [x] Production art and captures for the round (round 77: step ART-E's art in the game since E4, step E5's
      captures)
- [ ] Round 77 S.3 b: the tower's collapse plays `tower-collapse-r77-b` (the pipeline's import list, `Sfx.TOWER_COLLAPSE`,
      `PlaceholderAuditTest`, `Level39SoundsTest`), and the a file leaves `assets/sfx/` (decided 2026-11-01)
- [ ] Round 77 V.2 b: Vorne's t=91 line gets `expression: rage` in the level data and a new take at his grim row (a VOX
      step; then `RadioTimelineLevel39Test` and `VoiceFilesTest`), his neutral take replaced (decided 2026-11-01; the
      data's `expression: rage` in since step DE6c, the take to come)
- [ ] The opening capture redone for the plains opening (round 77 F.1 redo, `backdrop-final-r77-b` already in the game:
      ART-E2 `6c7a875b`), shown to the user in a later round

## Open questions

Decided in round 72 (2026-10-31); left to the build:

- **The holds**: drafted as Level 9's hold zones over each works (the towers' 9.7 s window); the alternative is no
  hold (a tower on the screen ≈ 4 s at 130 px/s: the level then needs `anti-ground` or the topples, and the plan's fit
  would fail it often).
- **The secondary**: drafted as the pump stations (6 of 8, the gap analysis's "pipeline count"); the alternative is
  none (the towers are the level's count).
- **Names** (the Tvashtar Plains and the Loki, Pele and Prometheus works, after Io's real volcanoes): placeholders for
  the story step.

## Decisions

- 2026-10-31 (Act 6 design phase, step `m10-d-levels-2`): drafted from the roster row, the gap analysis (Q3 a, Q7 b,
  Q8 a, Q10 a, J1–J3, J5 and its decided L39 items: `destroy-targets` on six named refinery targets failing at once,
  eruptions, Striders guarding the refineries, SAM Nests) and the wave 1 documents; decided 2026-10-31 (round 72).
- 2026-10-31 (decided by me, trivia): six towers in three works of two, each tower its own group (Level 9's model);
  Level 9's hold zones over each works at 40 px/s; the towers are ground units, so a Strider's topple onto one counts
  (the Strider's Act 6 section left it to this level); the secondary is the pump stations, 6 of 8 (`escapes`, as
  Level 19); 130 px/s and 192 s of script time; 27 vents, the plume field in rows of three; the tiny tier from the
  Darts and the motes (J5); Vorne's one line on the enemy channel; the secret a pre-war survey hut.
- 2026-10-31, design phase (step m10-d-act, the main agent's calls; decided 2026-10-31 (round 72)): the cracking tower pays **30**
  in Act 1 terms (was 40) and the `bounty_scale` is **0.41** (was 0.39, under the band): typical 9,393 (+2.6 %), perfect
  1.63 × budget. **Vorne's t=91 line** is the story's, without a hint of what is coming ("*Lancer. Burn Io if you
  like. Callisto holds fuel enough for what I still have to do.*"; the Silence is named only in Level 41). The
  ambience is D-audio's `io-refinery` bed.
- 2026-10-31, design phase (step m10-d-act): the cracking towers and the pump stations stay **this level's ground
  objects** (rows in the enemies' data in the build, no unit page: single-level objects); the open question is closed.
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): J3 (the eruptions: told vents
  throwing shootable rocks; Level 27's plume discs unchanged rejected), Q3 a (two hunts; c rejected), Q8 a with J11
  (the Crucible and the spoofer at the Level 40 visit, Composite V at the Level 41 visit; the Level 39 swap rejected)
  as drafted. The document approved as drafted (D.5; `design: approved`).
- 2026-11-01 (step ART-E, production art; decided 2026-11-01, round 77 F.1 redo, F.2, F.3, F.4 accepted): the level opens over Io's limb so
  that Jupiter on `deep` and a plume on the horizon can show at all under a top-down ground (≈ 3.7 s, then the surface);
  the towers 90 px either side of their works' rack (Loki A x 150, B 330; Pele A 330, B 150; Prometheus A 160, B 340),
  the pump stations on the racks at x 150 (P8 at t 162.5, before the rack bends at section 6's seam), the s4 road at
  x ≈ 180 through the Pele gate's gateway; the works' vents retimed into their holds (m10e-gaps §4) beside the towers,
  ≥ 104 px from every tower and pump station.
- 2026-11-01 (step E2, Level 39's data; decided 2026-11-01, round 77 L.1 a, L.3, C.1–C.6): built as drafted with the decided trivia
  of the build gap check (`m10e-gaps.md` §4): the towers and pump stations are counted enemies in two minimal unit
  directories; the works' vents retimed into their holds and a per-vent first tell (QE-1 a), the first vent's at
  0.8 s (its easy tell then ends before the t=14.5 wave); the events `first-target` and `topple-target`; the hut's lock
  a trigger spent by 30 damage; the Strider pairs of Pele and Prometheus entering at t=122.5 and t=173.5 (they walk
  beside the towers in the holds), the Loki SAM pair at t=33; the Sentinels' rows 221 px apart (the right one 1.7 s
  later: the gate's right blockhouse moves with it, a backdrop rerun for step E4); the overdrive from the t=116.5
  leader. `levelReport`: medium 99.5 enemies a minute, typical +0.3 % at 0.41, windows 8.3 s (easy 10.9, hard 6.7),
  two tells a vent per hold at medium, every cap held; the plain autopilot wins easy 3/3, medium 2/3, hard 0/3 (step
  E3's refinery autopilot next).
- 2026-11-01 (step E3, the tests and the autopilot; decided 2026-11-01, round 77 C.7, C.8): `Level39Test` and the
  refinery autopilot (`RefineryPlay`: in a hold the lower tower first, from the side away from a telling vent, out of
  the told fans and the Sentinels' arcs) win all six towers in 18 of 18 flights (3 per difficulty, with Rook and
  without); three streams moved by at most 1 s for the pacing rule (the Dart stream 102.5 → 101.5, the Skitter streams
  105.5 → 104.5 and 140.5 → 141); `BalanceTest`'s hold rule counts the forward DPS on unhardened towers.
- 2026-11-01 (step E4, the game side; decided 2026-11-01, round 77 C.9–C.13): the refinery looks (the flare, the fall,
  the wrecks), the production vents, umbrellas and rocks, the pipes' glows going dark on their target's death (drawn
  over the flyers: a faint orange stripe can cross a ship), the pump stations dying with the vacuum burst, the depot's
  latch and the hut's lock with a third frame (the data's), the right Sentinel's blockhouse moved with its entry, the
  tracker `TOWERS` over `PUMPS`, the `io-refinery` bed and round 77's a sounds; Level 39's atlas 5 of 6 pages.
- 2026-11-01 (step E5): the full `check` on a snapshot of `8b3eefef` green but for Level 37's five in-flight reds
  (its 13 shortened lines unvoiced and the radio test's old times, steps VOX-C2 and C6b); the captures from the game
  ([recipe](concept/prompts.md#level-39-capture-r77-a)); [round 77](../../../concept-rounds/round-77/README.md) opened.
- 2026-11-01 (user, round 77 F.1 redo; step ART-E2): the limb opening rejected ("It looks like a disc. There's no
  perspective in it at all", and a curve that tight would pass Io under the ship in about 10 s), and **no Jupiter on
  top-down ground levels**: the level opens on Io's plains, the ground covering the screen from t = 0 (`tvashtar-a`/`-b`
  opaque from row −16); Jupiter's disc and Tvashtar's side-on plume dropped (a side-on umbrella has no top-down
  reading; the vents' own umbrellas carry the eruptions); everything after the opening byte-identical. `r77-b` stays `proposed`
  (the user sees it in a later round).
- 2026-11-01 (user, round 77 closed; page answers and one note): **everything as recommended except F.1, S.3 and V.2**. Art
  F.2–F.8 accepted; **F.1 redo** (the limb opening: "It looks like a disc. There's no perspective in it at all", and the
  planet would pass under the ship in about 10 s): already redone by ART-E2 `6c7a875b` (see the entry above; the user's
  decision of the same day: no Jupiter on top-down ground levels); `backdrop-final-r77-b` stays `proposed` for a later
  round, the opening captures `opening-capture-r77-a` moved to `concept/rejected/`. Sounds S.1 a, S.2 a, S.4 a, **S.3 b**
  (the tower's collapse: the bending-metal buckle, not the recommendation; the build swaps it in, open Implementation
  item); V.1 accepted, **V.2 b** (Vorne's t=91 line at his grim row, `expression: rage`: needs a new take and the data,
  open Implementation item; the neutral take `vorne-t91-r77-a` moved to `concept/rejected/`); L.1 a (per-vent first tell),
  L.2 accepted (the hold windows stay), L.3 as round 76 (Rook's armour settled in M10 part I's Act 6 balance); C.1–C.13
  accepted (every "pending the user's review in round 77" note is now "decided 2026-11-01 (round 77 <id>)").
