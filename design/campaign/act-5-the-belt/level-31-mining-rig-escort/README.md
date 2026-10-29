---
title: Level 31 – Mining Rig Escort
design: approved
implementation: in-progress
art: final
depends-on: [../../../allies/mining-tug, ../../../enemies/air/hornet, ../../../enemies/space/shard-drone, ../../../enemies/air/buzzsaw-drone, ../../../enemies/air/dart-drone, ../../../enemies/air/gilded-gunship, ../../../enemies/air/talon, ../../../enemies/space/asteroid-mite, ../../../enemies/ground/sam-nest, ../../../world/belt, ../../../player/wingmen, ../../../player/generator]
updated: 2026-10-29
---

# Level 31 – Mining Rig Escort

## Summary

The act's escort level. The mining tug *Bellwether*, out of Ceres with four hundred loyalist refugees
in its cargo modules, crawls through the open belt toward the Vesta relay, and the Ascendancy sends
missile boats, linked drones and Dart streams after it. The [mining tug](../../../allies/mining-tug/README.md)
holds a station in the lower half of the play field (a `huge` ally of three hull sections: bow, cargo,
engines) and only attacks aimed at it hurt it: the [Hornet](../../../enemies/air/hornet/README.md)'s
homing diamonds, the [Dart Drone](../../../enemies/air/dart-drone/README.md)s' dives and the
[Shard Drone](../../../enemies/space/shard-drone/README.md) links dragged across its hull. Around it the
[Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md)s chase the ship and ricochet off the drifting
rocks. Density 4, all directions with 85 % of the waves from the front, ≈ 119 enemies a minute at
medium over 184 s (measured in the build, step D2). The refugee captain tells how Helix bought Ceres's loyalty with Vrell cures.

**Status: approved (Act 5 design phase, step `m9-d-levels-2`, 2026-10-27), approved in round 60.** Written from the act roster, the gap analysis's recommended answers (Q4 a the mining tug,
Q6 b the Dart Drone, Q1 a the enemy shields, Q11 a the Ascendancy look, Q12 c the arsenal), the L31
build gap check's recommendations (D1 a the tug's size, D2 a what hurts it) and the wave 1 documents;
every number is a first draft for part D of M9 to measure with `levelReport`.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

<!-- data: briefing -->
> **Commander Okafor:** "The mining tug Bellwether broke out of Ceres with four hundred
> loyalists aboard. Miners, families, the garrison's wounded. The Ascendancy wants them back, or
> gone. Fly with the tug to the Vesta relay. It doesn't get there, we've failed."
>
> **Dr. Varga:** "Missile boats will go for the tug. Shoot the missiles, not only the boats: two
> hits each. The linked drones drag their beams across its hull; kill one drone and its beams
> drop."
<!-- /data -->

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-31-mining-rig-escort`: a nav chart of the open belt: the tug's track from the
  Ceres side to the Vesta relay beacon, the drift fields along it, the ambush point marked in amber.
- Varga's page `level-31-tug-sections`: her diagram of the tug from above: bow, cargo and engines with
  their bars, a Hornet's salvo curving onto the bow, a Shard line sweeping across the cargo, Darts from
  behind on the engines.

*Hangar teaser* (shop screen before L31; Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "Escort duty. Big, slow and full of people. My favourite kind of job."
<!-- /data -->

## Threat profile

Directions are counted by each wave's `from` (the intel's rule): 79 waves (step D2: the draft's 63 with the
offbeat Talon pairs and rock groups), 67 from the front, 7 from the sides, 5 from behind.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `space` (the play plane in vacuum: Talons, Hornets, Gunships, Shard groups, Buzzsaws, Darts, Skitters, motes, Mites on rocks), `ground` (the tug's hull; two rock outcrops with SAM Nests) |
| Attack directions | front 85% · sides 9% · rear 7% |
| Density | 4 |
| Recommended traits | `spread`, `side`, `homing` |
| Hazards | Drifting asteroids (big ones block shots and hurt on contact); homing missiles at the tug |
| Boss / mid-boss | none |
| Sensor-suite detail | none: the open belt, `space` + `ground`, all directions, the objective `ESCORT THE TUG` · L1: + directions (front, sides, rear), density 4, the hazard "drifting rocks", the secondary `CLEAN HULL` · L2: + Hornet (new), Shard Drone (new), Buzzsaw Drone (new), Dart Drone, Gilded Gunship, Talon, Asteroid Mite, SAM Nest portraits, the hooked kinds marked "targets the tug" · L3: + the rear and side entries on the threat arrows |
<!-- /data -->

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "A mining tug full of refugees, and the whole belt after it." |
| L1 | "Attacks from behind too. Watch the tug's engines." |
| L2 | "New: Hornets fire homing missiles at the tug. Shoot them down." |
| L3 | "Rear and side entries marked. Spread for the missiles." |

## Objective

- **Primary** `escort`: bring the [mining tug](../../../allies/mining-tug/README.md) *Bellwether* to the
  Vesta relay. The tug is home when **at least one** of its three sections is still alive at t=178 (the
  climb-out). **Fails at once** when all three sections are lost, through the
  [failed primary objective](../../../systems/retry/README.md#on-a-failed-primary-objective) flow (the
  mission failed screen 3 s later), with Okafor's line on that screen.
- **Pay**: each section home earns **40** credits in Act 1 terms (262 in Act 5, the act factor
  6.5536), Level 04's per-unit escort pay with its debrief row; up to 786. A lost section only lowers
  the pay (Q4 a: "every section lost lowers the reward"). The typical haul counts it at the primary's
  rate 1.0.
- **Secondary** `CLEAN HULL`: every section home with **at least half** its hull. Pays **100** in Act 1
  terms (655); the typical haul counts 50 %.

### The tug in this level

The tug's spec (size, sections, what hurts it, looks, HUD) is in its
[document](../../../allies/mining-tug/README.md); this level sets its station, its HP by difficulty, the
hook per kind and the times.

- **Station** (Q-D1 = b, decided in round 60 (J.6 b, not the recommendation): the tug is **200×300**): centre
  **(240, 380)** px from the field's top-left, its footprint spanning x 140–340 and y 230–530; the upper 43 % of the
  field stays open (the draft's 160×260 left 55 %). It sways slowly round the station on the level clock: **±10 px
  across and ±2.5 px up and down**, a 14 s period (step D2, the build's re-measure for 200×300: the doc's ±20 / ±5 left
  the strips beside the hull 120 px; ±10 keeps them 130 px for the rear and side entries: *Decisions*). The section hit
  boxes are the production sprite's ([mining tug](../../../allies/mining-tug/README.md)).
- **Entry** (t 0–6): it rises from below the bottom edge into its station as the Bright Seam dock scrolls
  away beneath it, **untouchable** (fire and contact pass through it).
- **Climb-out** (t 178–184): its living engines flare and it climbs off the top edge, untouchable; every
  section alive at t=178 is home.
- **Section HP**: bow **240**, cargo **160**, engines **160** at medium (easy 320 / 220 / 220, hard
  200 / 150 / 150: step D2, the draft's 180 / 120 / 120 kept one section home in one of three seeds). **Tuning** (as Level 10's): the HP and the hooked waves are tuned until the
  autopilot, which guards the tug only by shooting what it sees, keeps **at least 2 sections home on
  every difficulty** (with and without Rook), and the balance plan's fit keeps all three home on medium;
  a per-section `damage_factor` is the fallback.

### What hurts the tug (Q-D2 = a, recommended)

Only **hooked** attacks ([target-the-objective hook](../../../enemies/README.md#target-the-objective-hook));
everything else passes over the ground layer. Per kind (the level's `targeted_by`):

| Kind | Hook | What it does to the tug |
|---|---|---|
| [Hornet](../../../enemies/air/hornet/README.md) | `always` (medium, hard); easy: `always` for every second Hornet wave (t=40.5, 104.5) only | Every salvo homes on the **nearest living section** at its launch (the bow, from the Hornets' hold at the top); a missile bursts in that section's box for the tug's missile damage; shot down (2 HP), flared or blocked by the ship's hull (6 on the ship) it does nothing. A missile whose section is lost in flight turns to the nearest living one |
| [Dart Drone](../../../enemies/air/dart-drone/README.md) (streams and the Gunships' launches) | `always` (medium, hard); easy: unhooked (they chase the ship) | Its chase steers at the nearest living section's centre: from the front the bow, from behind the **engines**, from the sides the cargo; crossing a section deals the tug's dive damage once, then it flies on, shootable |
| [Shard Drone](../../../enemies/space/shard-drone/README.md) | the waves marked "sent across the tug": their descent crosses the hull (the [air links](../../../enemies/README.md#air-links)' escort exception) | A lit link touching a section deals the tug's link damage per touch, with a 0.5 s cooldown per link and section; killing a drone drops its links |
| Talon, Gilded Gunship, Buzzsaw Drone, Skitter, Mote Swarm, Asteroid Mite, SAM Nest | never | Their shots, rings, dashes and bodies pass over the tug (`always`-free kinds; the Skitters are never hooked, the hook's rule) |

So the level reads in three directions: **the bow from the front** (missiles: shoot them or put the hull
in the way), **the engines from behind** (the rear Dart streams: the Swarm Tail's level), **the cargo
from the links** (break a Shard line before it reaches the hull).

## Layout

Scroll **120 px/s** throughout (the tug's crawl; 184 s, 22,080 px). Setting:
[the belt](../../../world/belt/README.md#sub-locations-per-level)'s open rock field, vacuum
([vacuum rules](../../../world/belt/README.md#vacuum-rules)): no weather, explosions ring outward, wrecks
drift and turn. **Light**: the belt's sunlit dust band in `deep`, the Sun top-left, rock faces warm and
lit (the Ascendancy readability rule, Q11 b: a step lighter than Act 4). Motion budget: the drifting
rocks and the Shard links are the strong elements; the refugee haulers on `low-air` and the dust on
`high-air` stay quiet.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Bright Seam Dock | 0–24 | 0–2,880 | 120 | clear | `ground`: the mining rock Bright Seam's dock gantries scrolling away, the tug rising into station (t 0–6). `low-air`: two small ore haulers with refugees falling in behind it, on the background only. | Popcorn and the first Darts at the bow. |
| 2. The Open Belt | 24–58 | 2,880–6,960 | 120 | clear | `air`: the first drifting rocks (t≈26), at most 2 big ones on the screen. `deep`: Ceres a fading crescent behind. | The first Hornet (t=24) and the first Buzzsaws (t=30.5); the first rear Darts (t=38). |
| 3. The Linked Line | 58–92 | 6,960–11,040 | 120 | clear | `ground`: rock outcrop 1, a large asteroid's sunlit face passing beside the tug with 3 SAM Nests on its pads (t≈60–66). `air`: sparse rocks. | The Shard groups (t=58 alone, then sent across the hull); the first Gunships. |
| 4. The Drift | 92–128 | 11,040–15,360 | 120 | clear | `air`: the densest rock drift (up to 3 big rocks and 6 small on the screen), small rocks with Mites riding; the secret's claim rock (t≈100). `ground`: rock outcrop 2 with 3 SAM Nests (t≈119–126). | Buzzsaw ricochets off the big rocks; Mites leaping off; rear Darts. |
| 5. The Ambush | 128–164 | 15,360–19,680 | 120 | clear | `air`: rocks thinning. `deep`: Ascendancy running lights in the dust band (the ambushers' carrier, scenery). | The heaviest pressure on the tug: three Hornets (t=134), Gunships, a Shard ring. |
| 6. The Vesta Relay | 164–184 | 19,680–22,080 | 120 | clear | `deep`: the relay's beacon tower and a CDF frigate, growing. | The last waves; the climb-out at t=178. |
<!-- /data -->

**Drifting rocks** ([the belt's hazard](../../../world/belt/README.md#hazards--set-pieces)): on the play
plane, crossing the field at 20–60 px/s on authored lines (deterministic, data); small rocks break into
harmless fragments after a few hits, big ones are indestructible, hurt on contact as a `large` unit's
body and stop the bullets of both sides (the player's shots at a missile behind a big rock too). They pass
over the tug (it is on the ground layer). The [Buzzsaw](../../../enemies/air/buzzsaw-drone/README.md)'s
dash bounces off the big ones and smashes the small ones; the
[Asteroid Mites](../../../enemies/space/asteroid-mite/README.md) ride the small ones
([rock riding](../../../enemies/README.md#rock-riding)). No rock line passes within 40 px of a wave's spawn
point, and no big rock crosses the ship's lowest 120 px while a rear wave enters (the edge warning stays
readable).

**Ground layer.** The tug's hull and the two rock outcrops are the ground layer: the level is not a
space-only level, so the Bomb Rack works over the outcrops (its bombs pass over the tug: player fire never
hurts allies). See *Open questions* for the build gap check's `NO GROUND` alternative.

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). All numbers are **draft**: data
in **Act 1 terms** with a `bounty_scale` (see *Credit budget*).

- **HP** (the units' own pages, set at the proposed L31 reference DPS **128**, judgement call J2): the
  Talon 32 (shield 12 since round 62), the Hornet 96 (shield 8), the Gunship 88 (shield 12, frontal armour), the Shard Drone
  28 and the Buzzsaw 20 (beam era), the Dart and the Mite 1, the Skitter and the motes as built.
- **Density and pacing** (the Act 2+ rules, `PacingTest` and `ActFloorTest`): measured in the build (step D2,
  `levelReport`): **356 enemies** at medium (350 in the waves + 6 SAM Nests; the Gunships' launched Darts on top),
  **≈ 119 a minute** (easy 298, ≈ 100 a minute; hard 414, ≈ 139), a wave about every 2.3 s, 3.1 on the screen and
  60 enemy shots a minute under the plan's fit (the floor 2.5 and 10), one stretch over 2 s with nothing on the
  screen (the climb-out's last 2 s). The draft's 63 waves left 2.3–2.5 on the screen and four empty stretches: the
  beam era's fodder dies within a second, so step D2 added firing Talon pairs and Mite rock groups on the offbeats
  (see the table's notes and *Decisions*).
- **Directions**: 67 of 79 waves from the front (85 %, the act's ≥ 60 %); rear waves are Dart streams
  only (5), entering at the bottom corners beside the hull (x < 0 / > 480, the §6 rule) and crossing the engines
  ≥ 0.8 s after they are on the screen (`levelReport`'s hull line); side waves are Buzzsaw streams, Dart streams
  (above the hull, y 120) and one Talon pincer (7). **Edge warnings** 3 s
  ahead for every side and rear entry.
- **Quiet windows**: no other wave for 3.5 s after the first Hornet (t=24) and after the first Shard
  group (t=58).
- **Limits**: at most 3 Hornets and 16 missiles on the screen (the Hornet's), at most two linked groups,
  at most 6 Buzzsaws (two dashing), at most 16 Darts; the SAM outcrops never share their seconds with a
  Hornet salvo (outcrop 1 between the t=40.5 and t=74.5 pairs, outcrop 2 after the t=104.5 pair leaves).
- **Gunship launches**: every Gunship launches its Dart streams (3 every 5 s, at most 6 alive) while on the
  screen; the launched Darts are hooked like the stream waves and count toward the density (about 6 a
  Gunship).

Draft wave table (`from`: `front`, `side (…)`, `rear` as the intel counts them):

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 6.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | No leader |
| 9 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 11.5 | 1 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front | The first Darts: they dive at the bow (hooked `always`) |
| 14 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Leader at the tip |
| 16.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 19 | 1 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (right) | Hooked |
| 21.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step D2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 24 | 2 | single | [Hornet](../../../enemies/air/hornet/README.md) | 1 | front | The first Hornet, alone, its salvos at the bow: quiet window of 3.5 s |
| 28 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 30.5 | 2 | stream | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 3 | left side | The first Buzzsaws, warned 3 s ahead; they chase the ship only |
| 33 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 35.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 38 | 2 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | rear | First rear entry, warned 3 s ahead, up the side edges beside the hull: they dive at the engines |
| 40.5 | 2 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | Hooked |
| 42 | 2 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Step D2: a rock group on the offbeat (2 rocks with 2 riders arriving together) |
| 43 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 45.5 | 2 | circle | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 4 | front | Tells staggered 0.4 s |
| 48 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 50.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 53 | 2 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | right side | Hooked; warned; above the hull (y 120) |
| 55.5 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 58 | 3 | line abreast | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 3 | front | The first linked group, alone, sent down across the tug: quiet window of 3.5 s |
| 63 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | Over the first SAM outcrop |
| 65.5 | 3 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Step D2: a rock group on the offbeat (2 rocks with 2 riders arriving together) |
| 67 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | Dart streams from the bays (launched Darts hooked) |
| 69.5 | 3 | line abreast | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 4 | front | Sent across the tug |
| 72 | 3 | stream | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 4 | sides | Warned |
| 74.5 | 3 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | Hooked |
| 77 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step D2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 79.5 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 6 | front | A leader at the tip |
| 82 | 3 | circle | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 4 | front | A closed ring drifting toward the ship's x (not sent at the tug) |
| 84.5 | 3 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | rear | Hooked; warned |
| 87 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 89 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step D2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 89.5 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 92 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | On small drifting rocks arriving together: the Drift begins |
| 94.5 | 4 | stream | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 4 | left side | Warned; the big rocks bounce their dashes |
| 97 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 99.5 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Rocks arriving together (2 with 2 riders, 1 with 1) |
| 99.5 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 1 | front | |
| 102 | 4 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | rear | Hooked; warned |
| 104.5 | 4 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | Hooked |
| 107 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Step D2: a rock group on the offbeat (2 rocks with 2 riders arriving together) |
| 109.5 | 4 | circle | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 5 | front | |
| 112 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | |
| 114.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step D2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 117 | 4 | pincer | [Talon](../../../enemies/air/talon/README.md) | 6 | sides | Warned on both edges |
| 119.5 | 4 | line abreast | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 3 | front | Over the second SAM outcrop; sent across the tug |
| 122 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 124.5 | 4 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | rear | Hooked; warned |
| 125.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step D2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 127 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Rocks arriving together (2 with 2 riders, 1 with 1) |
| 127 | 4 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 1 | front | |
| 129 | 5 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | The ambush begins: Darts hooked |
| 129 | 5 | carrier + escorts | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The Gunship's escort in `orbit` at 90 px |
| 131.5 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 134 | 5 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 3 | front | Three Hornets: the most at once; hooked |
| 137 | 5 | circle | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 4 | front | A closed ring (not sent at the tug) |
| 139.5 | 5 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | rear | Hooked; warned |
| 141 | 5 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Step D2: a rock group on the offbeat (2 rocks with 2 riders arriving together) |
| 142 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 144.5 | 5 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | Darts hooked |
| 147 | 5 | stream | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 4 | right side | Warned |
| 149.5 | 5 | line abreast | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 4 | front | Sent across the tug |
| 152 | 5 | swarm | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | Step D2: a rock group on the offbeat (2 rocks with 2 riders arriving together) |
| 154.5 | 5 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 6 | front | A leader at the tip |
| 157 | 5 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | left side | Hooked; warned; above the hull (y 120) |
| 159.5 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 162 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 163.5 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step D2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 165 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 6 | front | The rendezvous in sight; a leader at the tip |
| 167.5 | 6 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front | Hooked |
| 170 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 171.5 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step D2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 172.5 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 175 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 177.5 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step D2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 180 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | Step D2: the last wave, over the climb-out |

Totals: Skitter 90 · Talon 66 · Dart Drone 45 · Mote Swarm 50 · Hornet 10 · Buzzsaw Drone 24 · Asteroid Mite 38 · Shard Drone 22 · Gilded Gunship 5.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 3 | [SAM Nest](../../../enemies/ground/sam-nest/README.md) trio on rock outcrop 1 (t≈60; x 424, 96, 56) | Bounty 15 each (Act 1 terms); their missiles at the ship only (unhooked); at most 3 firing |
| 4 | SAM Nest trio on rock outcrop 2 (t≈119; x 44, 440, 404) | Bounty 15 each |
<!-- /data -->

## Hazards

- **Drifting rocks**: big ones indestructible, contact as a `large` unit's body; small ones break (above).
- **Homing missiles**: the Hornets' (at the tug) and the SAM Nests' (at the ship); the
  [Decoy Flares](../../../player/specials/README.md#decoy-flares-l27) retarget both kinds.
- **Specials**: every special works (open space; [Act 5 specials](../../../player/specials/README.md#act-5-specials-draft));
  the Sonar Pulse reads `NO WATER`. The EMP Burst drops every Shard link and every hex shield on the screen.

## Secrets and pickups

- **Dacosta's claim** (the secret, section 4, t≈100): a small hollow rock with a prospector's claim marker
  bolted to it, drifting through the Drift among the Mite rocks; **12 HP**, broken, it frees a crate worth
  **100** in Act 1 terms (655); its break sounds as a rock's crumble (the chosen small rubble burst,
  `hit-crumble-r08-b`; step D4b), not the lifeboat's cable snap. The Salvage scanner's glint shows it at any range. Its event line, the
  Bellwether: "*That's old Dacosta's claim marker. He'd want it spent on a good cause.*"
- **Shield cell**: dropped by the first Hornet (t=24) at its death.
- **Armour patch**: the Bellwether's crew throws one out of the cargo hatch at t=118 (from the bow if the
  cargo is lost; none if both are), at the tug's position, drifting up toward the ship.
- **Overdrive**: dropped by the last Hornet of the t=134 trio.

## Radio chatter

Spoken radio, draft text for the **1 s rule** (text at about 12 characters a second plus a 1.5 s lead
for the length check; 0.4 s of silence; no timed line more than 1 s late); event lines drop after 6 s.
Rook's lines carry `requires: escort` (he flies from L30, [wingmen](../../../player/wingmen/README.md#rooks-rescue-and-return-l29l30)).
The **Bellwether** is a new generic speaker, the tug's captain (a belt miner in her fifties, dry and
tired; cast and portrait by the act's audio and art steps, a civilian radio portrait). `{section}` is the
hit or lost section's name (`bow`, `cargo`, `engines`), a new placeholder.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | The Bellwether's captain (Bellwether) | "Bellwether to Aegis. Four hundred souls aboard. Stay close." |
| t=10 | Okafor | "Lancer, the tug is yours. Nothing touches it." |
| t=16 | Rook | "Big, slow and friendly. I can work with that." |
| t=25 | Varga | "Hornet. Its missiles go for the tug. Two hits each." |
| First section hit (`first-ally-hit`) | The Bellwether's captain (Bellwether) | "We're hit in the {section}! Get them off us!" |
| t=39 | Okafor | "Contacts behind you, on the tug's engines." |
| t=59 | Varga | "Linked drones. Kill one and its beams drop." |
| t=79 | The Bellwether's captain (Bellwether) | "You know why half of Ceres turned? Helix came with medicine." |
| t=86 | The Bellwether's captain (Bellwether) | "Lung rot, bone loss. Gone in a week. Nobody asked why." |
| t=94 | Varga | "Vrell biotech. They were cured with the enemy's blood." |
| t=118 | The Bellwether's captain (Bellwether) | "Throwing you a patch from the cargo hatch. It's all we've got." |
| t=130 | Okafor | "Ambush ahead. Gunships and missile boats. Hold the line, Aegis." |
| t=137.5 | Rook | "Three of them. I'll take the left one." |
| A section lost (`ally-lost`) | The Bellwether's captain (Bellwether) | "We've lost the {section}! We're still flying." |
| t=166 | Okafor | "The Vesta relay. A frigate is waiting. Bring them in." |
| t=176.5 | The Bellwether's captain (Bellwether) | "Relay in sight. Thank you, Aegis. Tell Ceres we made it." |
| Level end, all three sections home | Okafor | "Every one of them home. Well flown." |
| Level end, one or two sections lost | Okafor | "They made it. Not all of the ship did. That's enough today." |
| Mission failed (the failed screen's line) | Okafor | "The tug is gone, Lancer. Four hundred people. We go again." |
<!-- /data -->

Timeline check (text, by the rule above): the captain's opening line (t=1) closes at about t=9.3, before
Okafor's (t=10); her two-line story (t=79 and t=86) and Varga's answer (t=94) close at about t=100.5, so the radio is
free for the claim's line (two pages, about 14 s with its voice) when the claim is freed (t≈100.4–103.5: a bolt gun
under it, the plan's beam, the autopilot's strays), before the patch line (t=118; step FIX1, `RadioTimelineLevel31Test`);
Okafor's ambush call (t=130) closes before Rook's (t=137.5). The event lines take
the gaps and drop after 6 s. **The story beat** (the act's L31 beat): Helix bought loyalty with Vrell
cures, told by the refugees; Varga names what it was. Without Rook his two lines drop and nothing else
moves.

## Music & ambience

[Track 13](../../../audio/music/README.md#track-list), the Act 5 B theme (driving industrial with Vorne's
motif in the open; in the track list without a file): its intro in section 1, the **B base from section
2**, the **full mix in section 5** (the ambush), out over the relay into the
level end; Vorne's motif stem on top from the base's start (t=24) to the end (the act's motif "in the open"). Ambience: a belt open-space bed (proposed `belt-open`: a low hull hum heard through the cockpit,
distant metal creaks, no wind; the act's audio step decides) with the tug's engine bed under it while its
engines live. New sounds this level needs: the tug's engine bed and its section-hit and section-lost
sounds (a heavy hull impact, a klaxon), the link hum and snap, the saw whine and dash spin-up, the Hornet
launch and the missile run, the rock ricochet; listed in [sfx](../../../audio/sfx/README.md) by the act's
audio step (Freesound for real-world sounds only, no bubbly or popping sounds).

## Credit budget

Budget(31) = 700 × 1.07³⁰ ≈ **5,329** ([economy](../../../systems/economy/README.md#per-level-budget)).
Data in **Act 1 terms** (Skitter 5, Mote 2, Talon 12, its leader 25, Dart 2, Hornet 26, Buzzsaw 12, Shard
Drone 14, Gunship 28, Mite 3, SAM Nest 15, the crate 100, the section pay 40, the secondary 100), × the act
factor **6.5536** (1.6⁴), bounties × the scale. The draft's hand estimate (0.40 at ≈ 112 a minute) is replaced
by the build's fit (step D2, `levelReport`): with the waves at ≈ 119 a minute, a Gunship's launched Darts counted as
two streams and every tug section home paid, a **`bounty_scale` of 0.36** puts the typical haul at **5,303
(−0.5 %)** (the economy's shares: air kills 60 %, ground 80 %, the secret and the secondary 50 %, the escort pay
100 %); `TypicalHaulTest` confirms it.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 90 × 5 + Talon 66 × 12 + Dart Drone 45 × 2 + Mote Swarm 50 × 2 + Hornet 10 × 26 + Buzzsaw Drone 24 × 12 + Asteroid Mite 38 × 3 + Shard Drone 22 × 14 + Gilded Gunship 5 × 28 + launched Dart Drone 30 × 2 | 6,157 | 3,694 |
| Squad leaders: 5 × 13 extra (perfect run only) | 155 | 0 |
| Ground targets: SAM Nest 6 × 15 | 210 | 168 |
| Primary objective: 3 tug sections home × 40 | 786 | 786 |
| Secret: Dacosta's claim (hidden crate, 2% of budget) | 655 | 328 |
| Secondary: every section home with half its hull | 655 | 328 |
| **Total** (bounty scale 0.36) | **8,618** | **5,303** |
| Budget(n) = the typical haul's target; typical -0 %, perfect 1.62 × budget | | 5,329 |
<!-- /data -->

**Balance plan** (the [Act 5 hand plan](../../../player/generator/README.md#act-5-hand-plan-draft)): the
L31 visit buys **Composite IV** (140 armour) for the escort's hits; the loadout is the Ion Beam L4, the
Micro-missile Pods L3 ×2 (spread for the salvos) and the **Swarm Tail** L2 on the rear (homing at the rear
Dart streams); forward DPS 135 against the reference 128 (1.05). The level asks for `spread` against the
salvos and `side`/`rear` against the Darts on the engines, and is flyable with the beam alone by flying
over the tug's path of the missiles.

## Difficulty notes

- **Easy**: the tug's sections 320 / 220 / 220; the Darts unhooked (they chase the ship); half of the
  Hornet waves hooked (t=40.5, 104.5), salvos of 3; Shard groups of 3, links lit after 0.8 s; Buzzsaw tell
  0.8 s; the formation lever thins the streams.
- **Hard**: sections 200 / 150 / 150 (step D2); every Hornet, Dart and Shard group sent at the tug; Shard groups of
  4, links lit after 0.5 s; Hornet salvos every 4 s at 100°/s; the streams one larger.

## Concept art

Part D's production art (step ART-D, concept round 64) is below; the level needs (round 60's proposals, then part D's production art):

- the open-belt backdrop (six sections: the Bright Seam dock, the open belt, the two outcrops, the Drift,
  the relay with its beacon tower and the CDF frigate), the drifting rocks (big and small), the refugee
  haulers on `low-air`;
- the [mining tug](../../../allies/mining-tug/README.md) (its own document's concept), the Hornet, the
  Shard Drone and its links, the Buzzsaw's spin-up tell, the Dart Drone, the homing diamond (the unit
  documents);
- Dacosta's claim rock; the two briefing images; the Bellwether's radio portrait;
- the round's captures: the tug under a Hornet salvo, a missile shot off the bow, a Shard line across the
  cargo, a Buzzsaw bouncing off a rock, a section lost, the HUD's three bars.

Production art, M9 part D (concept round 64, step ART-D): the open-belt backdrop (`tools/art/backdrop_l31.py`, its
places for step D2 in [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml)) and Dacosta's claim rock
(`tools/art/belt_units_d.py`). The tug, the Hornet, the Shard Drone and the Buzzsaw are in their own documents; the
Bellwether's radio portrait in the [characters](../../../story/characters/README.md#concept-art); the two briefing images
in the [briefing](../../../ui/briefing/README.md#concept-art). Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r64-a.png](concept/backdrop-final-r64-a.png) | Production backdrop (`tools/art/backdrop_l31.py`): eight screens at half size with stand-ins (the tug in its cradle at Bright Seam and rising, the open belt with Ceres behind, outcrop 1 with three nests beside the hull, the Drift with Dacosta's claim, outcrop 2, the ambushers' carrier lights, the Vesta relay and a CDF frigate), the pieces and the tile sets | chosen (round 64, F.7: accepted as final) |
| [concept/backdrop-final-r64-a.gif](concept/backdrop-final-r64-a.gif) | t 0-8 at half size: the tug rising out of its cradle as the Bright Seam dock scrolls away, the refugee haulers falling in behind | chosen (round 64, F.7: accepted as final) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block, the sections, the tug's station band, the `ground_targets` on the outcrops' pads (x ≤ 100 or ≥ 380) and the `debris:` block (the belt's rocks, Dacosta's claim) for step D2 (not data) | chosen (round 64, F.7: accepted as final) |
| [concept/claim-rock-final-r64-a.png](concept/claim-rock-final-r64-a.png) | Dacosta's claim (`tools/art/belt_units_d.py`): a small hollow rock with the prospector's claim marker bolted on, 16 spin frames, shot open with the crate's light; beside part B's small rocks (sheet) | chosen (round 64, F.8: accepted as final) |
| [concept/level-31-hud-capture-r64-a.png](concept/level-31-hud-capture-r64-a.png) | Game capture (step D5; [recipe](concept/prompts.md#level-31-capture-r64-a)): the tug at its station with the HUD's `TUG 3 / 3`, its B/C/E bars and the `CLEAN HULL` tag, the Bellwether on the radio (the plan's fit, firing, t≈87); below, not firing (t≈83): the bow and the cargo lost (dark, venting), `HULL FAILED`, "We've lost the bow!" | chosen (round 64: accepted with the level, F.7-F.8) |
| [concept/dart-stream-capture-r64-a.png](concept/dart-stream-capture-r64-a.png) | Game capture, t≈39–41 (not firing): the t=38 rear Dart stream from the bottom corners closing on the engines, the engines' hit flash | chosen (round 64: accepted with the level, F.7-F.8) |
| [concept/claim-rock-capture-r64-a.png](concept/claim-rock-capture-r64-a.png) | Game capture, t≈100.7–102.5 (the Pulse Cannon L3, the ship nudged right): Dacosta's claim drifting in, broken open by the bolts (its chips in a ring, the crate's light), the crate drifting down | chosen (round 64: accepted with the level, F.7-F.8) |
| [concept/level-31-capture-r65-a.mp4](concept/level-31-capture-r65-a.mp4) | Game capture with the game's sound (step E5, after FIX1; [recipe](concept/prompts.md#level-31-capture-r65-a)): the Bellwether's retimed story lines (Laurie Anne Walden), the beam freeing Dacosta's claim, the softened hit flash | chosen (round 65, accepted with the level, C.7–C.8) |
| [concept/claim-rock-capture-r65-a.png](concept/claim-rock-capture-r65-a.png) | Game capture, t=100.4–102.6: the claim rock freed by the beam's touch (round 64 C.1 a) | chosen (round 65, accepted with the level, C.7–C.8) |

## Implementation

- [x] Level data `data.yaml` (when built in part D): sections, the rock drift lines, the outcrops, waves,
      ground targets, the escort block (the tug, its station, sway, HP by difficulty, the per-section pay,
      `targeted_by` per kind and difficulty), radio, budget, threat profile, briefing, backdrop, music
- [x] The mining tug escort: entry, station and sway, the three sections, the hooked damage only, the
      climb-out, the fail when all three are lost ([mining tug](../../../allies/mining-tug/README.md))
- [x] Hooked kinds: Hornet salvos homing on the nearest living section, Dart dives at the nearest section,
      Shard links dragged across the hull; everything else passes over the tug
- [x] Drifting rocks on authored lines (break, indestructible, block shots), the Buzzsaw's ricochet off
      them, the Mites riding them
- [x] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), the quiet windows, edge warnings for
      every side and rear entry, the limits above
- [x] Escort pay per section home and the `CLEAN HULL` secondary in the debrief (step D1: each section home pays its
      own credits; D4: the debrief's `SECTIONS HOME n / 3` (step FIX1; `TUGS` before) and the secondary; D4b: the `CLEAN HULL` tag on the HUD; seen in
      round 64's captures)
- [x] Radio voiced (the Bellwether a new generic speaker; the `{section}` placeholder), every timed line at
      most 1 s late, with Rook and without (`requires: escort`) (step D4b: `VoiceFilesTest` lists Level 31 rendered,
      `RadioTimelineLevel31Test`'s voiced run passes)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel
- [x] Briefing: both pages, each one screen, with their images
- [x] Typical haul within ±5 % of budget(31) = 5,329 with the level's `bounty_scale`
- [x] Music: track 13's stems by section; the belt ambience and the tug's engine bed (step D4: `Tracks` 13, the engine bed a unit loop while the engines live, `MiningRigCues`; step D4b: the motif stem from t=24, `Level31SoundsTest`)
- [x] Easy/hard variations; the autopilot keeps at least 2 sections home on every difficulty (with and
      without Rook)
- [x] Production art and captures for the round (ART-D; step D5: [round 64](../../../concept-rounds/round-64/README.md)'s
      game captures; all accepted)
- [x] A beam's touch counts as one hit per 0.25 s on an Act 5 tow's housing (round 64 C.1 a; opt-in per level, so Level 07's
      lifeboat and the recorded flights stay as they are; Level 30's buoy and this level's claim opt in) (step FIX1:
      `beam_touch`, `TowBeamTest`, `Level31Test.thePlansBeamFreesTheClaim`: the plan's Ion Beam frees it at t≈101.1 on
      medium)
- [x] The round 64 sound picks played (`saw-spinup-r64-ab`, `tug-hit-r64-b`, `serpent-rumble-r64-a`; step FIX1: the
      paths switched, the a files rejected, `SfxFilesTest`, `PlaceholderAuditTest`; the in-game credits name Laurie Anne
      Walden, `:pipeline:credits`)
- [ ] The Bellwether's b voice in `assets/voice/bellwether/` heard in a run (`VoiceFilesTest` passes; no capture with
      sound yet)
- [x] The claim's line plays when the claim is freed (step FIX1: the story lines t=86/93/101 moved to t=79/86/94 and the
      patch line and drop from t=110 to t=118, no text changed; `RadioTimelineLevel31Test.theClaimsLinePlaysInEveryClaimRun`)
- [x] A section's hit is a soft additive tint (40 % of the white flash setting, 2 steps), not a solid white flash; the
      HUD bar keeps its white flash (step FIX1, `TugLooks`)

## Open questions

The design choices below are drafted on the recommended option, **approved in round 60**;
the alternatives:

- **Q4, the escort** (gap analysis): **a** a `huge` tug with three sections (drafted); **b** three ore
  haulers as an L04/L11 convoy (cheap, repeats Acts 1–2); **c** the tug on the play plane ahead of the ship,
  not to be shot through (blocks fire, frustrating).
- **Q-D1, the tug's size** (build gap check; decided in round 60, J.6 **b**, not the recommendation): **b** Q4 a's
  200×300 (the hull dominates, bullets over a busy sprite); rejected: **a** ≈ 160×260 centred ≈ 150 px above the
  bottom (the draft) and **c** a smaller 120×200 tug (less "huge").
- **Q-D2, what hurts the tug** (build gap check): **a** hooked attacks only (drafted); **b** every enemy
  bullet crossing its box (Level 10's air-ally rule: unreadable over a 160 px hull, very punishing); **c**
  only the missiles.
- **Q6, the popcorn** (gap analysis): **b** the Dart Drone streams (drafted, 75 of the 344); **a** the Vrell
  alone carry the count (this level would need about 70 more Skitters and motes); **c** Talons in great
  numbers.
- **The ground layer**: the act roster and the [belt](../../../world/belt/README.md#sub-locations-per-level)
  give L31 a ground layer (the tug's hull), and the [Bomb Rack](../../../player/weapons/bomb-rack/README.md)
  names L30 the act's only space-only level, so this draft keeps a ground layer with two SAM outcrops; the L31
  build gap check had decided `NO GROUND` (the rack idle, the tug an ally). Without the outcrops the haul
  loses 187 (the scale rises to about 0.41).
- **The tug's names** (the *Bellwether*, Bright Seam, Dacosta, the Vesta relay): placeholders for the
  story step.

## Decisions

- 2026-10-27 (Act 5 design phase, step `m9-d-levels-2`): drafted from the roster row, the gap analysis
  (Q4 a, Q6 b, Q1 a, Q11 a, Q12 c), the L31 build gap check (D1 a, D2 a, its decided trivia) and the wave 1
  documents; approved in round 60.
- 2026-10-27: the level meets the Act 2+ floor from the start (≈ 112 a minute, a wave about every 2.9 s);
  81 % of the waves from the front, rear entries are Dart streams on the engines only.
- 2026-10-27: each attacker kind has its section: missiles on the bow (from the Hornets' hold at the top),
  rear Darts on the engines, links across the cargo; the tug keeps its station when a section is lost
  (presentation only), all three lost fails the mission.
- 2026-10-27: Buzzsaws chase the ship only; Rook flies his slot as usual (no guard order); the escort pays
  40 a section home and the `CLEAN HULL` secondary rewards keeping every section above half.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`, D.4) except the tug's size: **J.6 b**, the tug is **200×300 px** (see *Station*; part D re-measures the station, the sway, the lanes and the hit boxes), with the pressure-hull look (A.19 b); J.7 **a** (only hooked attacks hurt it); Q4 **a**. Q11 **b**: gunmetal/charcoal units; M.1 **a**, "Gilded Cage", is the level's B theme.
- 2026-10-28 (M9 part D, step D2: the data, accepted in round 64): built from the approved tables
  and ART-D's backdrop proposal, measured with `levelReport` (its new `hull` part: the sections' HP left, the hits by
  kind, the rear Darts' time to their first crossing, the Hornets' life on the screen). **The sway** ±10 px across and
  ±2.5 px up and down (m9d §6), not the doc's ±20 / ±5: the strips beside the 200 px hull stay 130 px for the rear and
  side entries. **Rear Dart streams** enter at the bottom corners (a route from x −56 / 536 below the bottom edge to
  x 10 / 470, then their chase): the draft's entries under the hull hit at once, x 24 straight up the edge crossed the
  cargo, not the engines; now they reach the engines 0.80–0.88 s after they are on the screen (the rule ≥ 0.8 s; hard's
  80°/s turn 0.80). **Side Dart streams** (t=53, 157) enter at y 120, above the hull. **Dacosta's claim** flies as a
  tow (Level 30's buoy rule: its housing takes 6 hits, the README's 12 HP at a Pulse Cannon bolt's 2, and frees the
  crate), not as a debris chunk (a chunk carries no secret), drawn in the claim rock's frames (`debris-claim-rock`, the
  tows' looks by level). **Density**:
  the draft's 63 waves measured 2.3–2.5 on the screen and four empty stretches over 2 s at medium; step D2 added firing
  Talon pairs (t=21.5, 77, 89, 114.5, 125.5, 163.5, 171.5, 177.5), Mite rock groups (t=42, 65.5, 107, 141, 152) and a
  last Skitter snake (t=180): ≈ 119 a minute, 3.1 on the screen, one stretch over 2 s. The Mite waves of 5 are two
  rock waves at the same t (2 rocks with 2 riders and 1 with 1, round 62's groups). Two large rocks at x 420 (t=121,
  138) enter 2.5 s earlier, out of the rear entries at t=124.5 and 139.5. **The bounty scale** 0.36 (the draft's 0.40
  left the typical haul +16 % once the section pay counts). **Radio**: the timed lines trimmed to one page each where
  they ran two (t=1, 25, 59, 93); the closing line at t=176.5 (t=179 ran 1.9 s past the level's end); the text
  timeline is never more than 1 s late with or without Rook, with the section hit and lost events anywhere. The
  armour patch drops at the cargo's place at t=110 whatever is left (a drop has no condition). The ore haulers on the
  backdrop fall back at 118 px/s of their own (the backdrop's 120 px/s cap) instead of ART-D's hold behind the tug.
  **Hard's sections** 200 / 150 / 150 (the draft's 180 / 120 / 120 kept one section home in one of three seeds: the
  bow lost, its missiles turning to the cargo). Autopilot on the plan's fit: easy 3/3 home and clean, medium 3/3 home
  and clean (6 seeds, the bow 65–80 %), hard 2–3 sections home (6 seeds; the bow lost in three, 21–51 missiles), never
  failed: the doc's tuning target (≥ 2 home on every difficulty, 3 on medium) holds without a `damage_factor` (without
  Rook: step D3's). The Shard groups die before their links reach the
  hull under the plan's fit (0–2 link touches a run).
- 2026-10-28 (step D3, `Level31Test`): the autopilot needs **no escort play**. On the plan's fit, 3 seeds a
  difficulty, never a retry: with Rook easy and medium 3/3 home and clean, hard 2–3 home (the bow lost in one seed);
  without Rook (`--escort none`) easy and medium 3/3 home, hard 2–3 home (the bow lost in two seeds); with the
  Bulwark 3/3 home on every difficulty (one seed). Only the hooked attacks hurt the hull (each step's damage is the
  missiles', the link touches' and the dives' sum); the rear Darts cross the engines 0.80–0.87 s after entering on
  medium and hard (easy's are unhooked and pass over it); the side Darts enter at y 121. **Dacosta's claim** (a tow:
  6 hits) takes the player's shots only, as every tow since Level 07: the plan's Ion Beam passes its housing, so on the
  plan's fit only the pods' stray missiles hit it (2 of 6 in a pass at 146 px/s); a bolt weapon (the Pulse Cannon L3)
  frees it in one pass. Level 30's buoy is the same. Decided in round 64 (C.1 a): a beam's touch counts as a hit on a
  tow's housing (a simulation change), see the last entry.
- 2026-10-28 (step D5, round 64 opened): part D is built; [round 64](../../../concept-rounds/round-64/README.md) asks
  for the production art with game captures, the Bellwether's casting, the sounds and track 13, and the calls the build
  made (accepted in round 64, C.2-C.8): the Hornet's shield 12, the sway ±10 / ±2.5, the hard sections, the
  offbeat waves, the rear and side Dart entries, the claim as a tow and the tows against the beam, the radio trims. Seen
  in the captures: the plan's fit with the ship left where it starts keeps every section home but lets the bow fall
  under half (`HULL FAILED` at t≈114, the Hornet pairs' salvos); the autopilot's runs keep it clean. Found, for the next
  game or content step: the claim's event line ("That's old Dacosta's claim marker…") is freed at t≈101–103, inside
  Varga's t=101 line, and goes stale before the radio is free (6 s): in the capture it never played; the debrief labels
  the hull's sections `TUGS HOME 3 / 3`.
- 2026-10-28 (user, [round 64](../../../concept-rounds/round-64/README.md), closed; page answers): the production art accepted as final (**F.7** the open-belt backdrop, **F.8** Dacosta's claim rock, with the tug **F.3 a** filled to 200×300, the Hornet, Shard Drone, Buzzsaw **F.6 a** with the gold on the cutting edges, the Bellwether's portrait, the briefing images and the intel portraits; `art: final`); **V.1 b**, the Bellwether is Laurie Anne Walden (her 12 takes in `assets/voice/bellwether/`); **M.1** ok (track 13 "Gilded Cage"); **C.2-C.8** accepted (the Hornet's shield 12, the sway ±10 / ±2.5, hard's sections 200 / 150 / 150, the offbeat waves, the rear and side Dart entries, the claim as a tow of 6 hits, the radio trims); sounds: a for S.1-S.4, S.6, S.8-S.10, S.7 b (the tug's hull hit), S.5 b's spin-up on the grinder (`saw-spinup-r64-ab`). **C.1 a (an important concept)**: a beam's touch counts as one hit per 0.25 s on a tow's housing from Act 5 on (opt-in per level, so Level 07's lifeboat and the recorded flights stay unchanged): Dacosta's claim (6 hits) and Level 30's dead buoy (4) can be freed by the Ion Beam build; built in the next game step (the fix step). Found by the captures, not asked, for that step: the claim's event line goes stale (6 s) inside Varga's t=101 line, and the debrief labels the hull's sections `TUGS HOME`.
- 2026-10-28 (step FIX1): round 64 C.1 a built: the claim's tow opts in (`beam_touch: true`); a beam touching the
  housing counts a hit at once and every 0.25 s and passes on: the plan's Ion Beam frees it at t≈101.1 on medium (a
  bolt gun under it at t≈100.4, the autopilot's strays alone at t≈103.4–103.5 in 2 of 9 runs). The claim's line is two
  pages (about 14 s): to play it the story pair and Varga's answer move 7 s earlier (t=79, 86, 94, closing at about
  t=100.5) and the patch line with its drop from t=110 to t=118 (the patch still lands in the Drift); no text changed.
  The debrief's escort row reads `SECTIONS HOME n / 3`; a section's hit is a soft additive tint at 40 % instead of a
  solid white flash (the HUD bar keeps its white flash). `CLEAN HULL` checked with the autopilot flying (the plan's
  fit, `levelReport -Pparts=hull`): met on easy and medium in all three seeds (the bow at 67-97 %); hard loses it
  (the bow 0-12 %), as the idle capture's t≈114 failure: a passive player losing it is fine, left as it is.
- 2026-10-29 (user, [round 65](../../../concept-rounds/round-65/README.md), closed): **C.7** accepted: the Bellwether's story lines retimed to t=79/86/94 and the armour patch line and drop to t=118; the Level 31 capture with sound and the freed claim accepted with the level.
