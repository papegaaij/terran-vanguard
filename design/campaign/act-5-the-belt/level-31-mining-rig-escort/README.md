---
title: Level 31 – Mining Rig Escort
design: draft
implementation: not-started
art: none
depends-on: [../../../allies/mining-tug, ../../../enemies/air/hornet, ../../../enemies/space/shard-drone, ../../../enemies/air/buzzsaw-drone, ../../../enemies/air/dart-drone, ../../../enemies/air/gilded-gunship, ../../../enemies/air/talon, ../../../enemies/space/asteroid-mite, ../../../enemies/ground/sam-nest, ../../../world/belt, ../../../player/wingmen, ../../../player/generator]
updated: 2026-10-27
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
rocks. Density 4, all directions with 81 % of the waves from the front, ≈ 112 enemies a minute at
medium over 184 s. The refugee captain tells how Helix bought Ceres's loyalty with Vrell cures.

**Status: draft (Act 5 design phase, step `m9-d-levels-2`, 2026-10-27), pending the user's review in
round 60.** Written from the act roster, the gap analysis's recommended answers (Q4 a the mining tug,
Q6 b the Dart Drone, Q1 a the enemy shields, Q11 a the Ascendancy look, Q12 c the arsenal), the L31
build gap check's recommendations (D1 a the tug's size, D2 a what hurts it) and the wave 1 documents;
every number is a first draft for part D of M9 to measure with `levelReport`.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

> **Commander Okafor:** "The mining tug Bellwether broke out of Ceres with four hundred loyalists aboard.
> Miners, families, the garrison's wounded. The Ascendancy wants them back, or gone. Fly with the tug to
> the Vesta relay. It doesn't get there, we've failed."
>
> **Dr. Varga:** "Missile boats will go for the tug. Shoot the missiles, not only the boats: two hits
> each. The linked drones drag their beams across its hull; kill one drone and its beams drop."

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-31-mining-rig-escort`: a nav chart of the open belt: the tug's track from the
  Ceres side to the Vesta relay beacon, the drift fields along it, the ambush point marked in amber.
- Varga's page `level-31-tug-sections`: her diagram of the tug from above: bow, cargo and engines with
  their bars, a Hornet's salvo curving onto the bow, a Shard line sweeping across the cargo, Darts from
  behind on the engines.

*Hangar teaser* (shop screen before L31; Rook, not voiced):

> **Rook:** "Escort duty. Big, slow and full of people. My favourite kind of job."

## Threat profile

Draft; directions are counted by each wave's `from` (the intel's rule): 63 waves, 51 from the front,
7 from the sides, 5 from behind.

| Field | Value |
|---|---|
| Dominant layers | `space` (the play plane in vacuum: Talons, Hornets, Gunships, Shard groups, Buzzsaws, Darts, Skitters, motes, Mites on rocks), `ground` (the tug's hull; two rock outcrops with SAM Nests) |
| Attack directions | front 81% · sides 11% · rear 8% |
| Density | 4 |
| Recommended traits | `spread`, `side` (the roster's), `homing` |
| Hazards | Drifting asteroids (big ones block shots and hurt on contact); homing missiles at the tug |
| Boss / mid-boss | none |
| Sensor-suite detail | none: the open belt, `space` + `ground`, all directions, the objective `ESCORT THE TUG` · L1: + directions (front, sides, rear), density 4, the hazard "drifting rocks", the secondary `CLEAN HULL` · L2: + Hornet (new), Shard Drone (new), Buzzsaw Drone (new), Dart Drone, Gilded Gunship, Talon, Asteroid Mite, SAM Nest portraits, the hooked kinds marked "targets the tug" · L3: + the rear and side entries on the threat arrows |

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

- **Station** (Q-D1 = a, recommended): centre **(240, 390)** px from the field's top-left, its 160×260
  footprint spanning x 160–320 and y 260–520; the upper 55 % of the field stays open. It sways slowly
  round the station on the level clock (the ally's sway: 20 px across, 5 px up and down, a 14 s period).
- **Entry** (t 0–6): it rises from below the bottom edge into its station as the Bright Seam dock scrolls
  away beneath it, **untouchable** (fire and contact pass through it).
- **Climb-out** (t 178–184): its living engines flare and it climbs off the top edge, untouchable; every
  section alive at t=178 is home.
- **Section HP**: bow **240**, cargo **160**, engines **160** at medium (easy 320 / 220 / 220, hard
  180 / 120 / 120). **Tuning** (as Level 10's): the HP and the hooked waves are tuned until the
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
lit (the Ascendancy readability rule, Q11 a: a step lighter than Act 4). Motion budget: the drifting
rocks and the Shard links are the strong elements; the refugee haulers on `low-air` and the dust on
`high-air` stay quiet.

| Section | t (s) | Scroll (px) | Layers and content | Purpose |
|---|---|---|---|---|
| 1. Bright Seam Dock | 0–24 | 0–2,880 | `ground`: the mining rock Bright Seam's dock gantries scrolling away, the tug rising into station (t 0–6). `low-air`: two small ore haulers with refugees falling in behind it, on the background only. | Popcorn and the first Darts at the bow. |
| 2. The Open Belt | 24–58 | 2,880–6,960 | `air`: the first drifting rocks (t≈26), at most 2 big ones on the screen. `deep`: Ceres a fading crescent behind. | The first Hornet (t=24) and the first Buzzsaws (t=30.5); the first rear Darts (t=38). |
| 3. The Linked Line | 58–92 | 6,960–11,040 | `ground`: rock outcrop 1, a large asteroid's sunlit face passing under the tug with 3 SAM Nests (t≈60–66). `air`: sparse rocks. | The Shard groups (t=58 alone, then sent across the hull); the first Gunships. |
| 4. The Drift | 92–128 | 11,040–15,360 | `air`: the densest rock drift (up to 3 big rocks and 6 small on the screen), small rocks with Mites riding; the secret's hollow rock (t≈100). `ground`: rock outcrop 2 with 3 SAM Nests (t≈119–126). | Buzzsaw ricochets off the big rocks; Mites leaping off; rear Darts. |
| 5. The Ambush | 128–164 | 15,360–19,680 | `air`: rocks thinning. `deep`: Ascendancy running lights in the dust band (the ambushers' carrier, scenery). | The heaviest pressure on the tug: three Hornets (t=134), Gunships, a Shard ring. |
| 6. The Vesta Relay | 164–184 | 19,680–22,080 | `deep`: the relay's beacon tower and a CDF frigate, growing. | The last waves; the climb-out at t=178. |

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
  Talon 32 (shield 2), the Hornet 96 (shield 8), the Gunship 88 (shield 12, frontal armour), the Shard Drone
  28 and the Buzzsaw 20 (beam era), the Dart and the Mite 1, the Skitter and the motes as built.
- **Density and pacing** (the Act 2+ rules, `PacingTest` and `ActFloorTest`): **344 enemies** (waves
  308 + about 30 Darts launched by the five Gunships + 6 SAM Nests) over 184 s, **≈ 112 a minute** at
  medium (easy ≈ 290, hard ≈ 390 by the formation lever), a wave about every 2.9 s, no stretch over 2 s
  with nothing on the screen (Hornets hold 9 s, Shard groups descend for 10 s or more). The enemy-shot rate
  comes from the Talon pairs, the Shard bolts, the Gunship rings and the missiles; part D sets the fire
  rates with `levelReport`.
- **Directions**: 51 of 63 waves from the front (81 %, the act's ≥ 60 %); rear waves are Dart streams
  only (5); side waves are Buzzsaw streams, Dart streams and one Talon pincer (7). **Edge warnings** 3 s
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

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 6.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | No leader |
| 9 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) |  |
| 11.5 | 1 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front | The first Darts: they dive at the bow (hooked `always`) |
| 14 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Leader at the tip |
| 16.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 19 | 1 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front (right) | Hooked |
| 24 | 2 | single | [Hornet](../../../enemies/air/hornet/README.md) | 1 | front | The first Hornet, alone, its salvos at the bow: quiet window of 3.5 s |
| 28 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 30.5 | 2 | stream | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 3 | side (left) | The first Buzzsaws, warned 3 s ahead; they chase the ship only |
| 33 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 35.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 38 | 2 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | rear | First rear entry, warned 3 s ahead: they dive at the engines |
| 40.5 | 2 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | Hooked |
| 43 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 45.5 | 2 | circle | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 4 | front | Tells staggered 0.4 s |
| 48 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 50.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 53 | 2 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | side (right) | Hooked; warned |
| 55.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 58 | 3 | line abreast | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 3 | front | The first linked group, alone, sent down across the tug: quiet window of 3.5 s |
| 63 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | Over the first SAM outcrop |
| 67 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | Dart streams from the bays (launched Darts hooked) |
| 69.5 | 3 | line abreast | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 4 | front | Sent across the tug |
| 72 | 3 | stream | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 4 | side (both, alternating) | Warned |
| 74.5 | 3 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | Hooked |
| 79.5 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 6 | front | A leader at the tip |
| 82 | 3 | circle | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 4 | front | A closed ring drifting toward the ship's x |
| 84.5 | 3 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | rear | Hooked; warned |
| 87 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 89.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 92 | 4 | ride | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front | On a small drifting rock: the Drift begins |
| 94.5 | 4 | stream | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 4 | side (left) | Warned; the big rocks bounce their dashes |
| 97 | 4 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 99.5 | 4 | ride | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 5 | front |  |
| 102 | 4 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | rear | Hooked; warned |
| 104.5 | 4 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | Hooked |
| 109.5 | 4 | circle | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 5 | front |  |
| 112 | 4 | ride | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 4 | front |  |
| 117 | 4 | pincer | [Talon](../../../enemies/air/talon/README.md) | 6 | side (both) | Warned on both edges |
| 119.5 | 4 | line abreast | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 3 | front | Over the second SAM outcrop; sent across the tug |
| 122 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 124.5 | 4 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | rear | Hooked; warned |
| 127 | 4 | ride | [Asteroid Mite](../../../enemies/space/asteroid-mite/README.md) | 5 | front |  |
| 129 | 5 | carrier + escorts | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | The ambush begins: Darts hooked |
| 129 | 5 | carrier + escorts | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The Gunship's escort in `orbit` at 90 px |
| 131.5 | 5 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 134 | 5 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 3 | front | Three Hornets: the most at once; hooked |
| 137 | 5 | circle | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 4 | front |  |
| 139.5 | 5 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | rear | Hooked; warned |
| 142 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 144.5 | 5 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | Darts hooked |
| 147 | 5 | stream | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 4 | side (right) | Warned |
| 149.5 | 5 | line abreast | [Shard Drone](../../../enemies/space/shard-drone/README.md) | 4 | front | Sent across the tug |
| 154.5 | 5 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 6 | front | A leader at the tip |
| 157 | 5 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | side (left) | Hooked; warned |
| 159.5 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 162 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 165 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 6 | front | The rendezvous in sight; a leader at the tip |
| 167.5 | 6 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 4 | front | Hooked |
| 170 | 6 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 172.5 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front |  |
| 175 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The last wave |

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 3 | [SAM Nest](../../../enemies/ground/sam-nest/README.md) trio on rock outcrop 1 (t≈60) | Bounty 15 each (Act 1 terms); their missiles at the ship only (unhooked); at most 3 firing |
| 4 | SAM Nest trio on rock outcrop 2 (t≈119) | Bounty 15 each |

## Hazards

- **Drifting rocks**: big ones indestructible, contact as a `large` unit's body; small ones break (above).
- **Homing missiles**: the Hornets' (at the tug) and the SAM Nests' (at the ship); the
  [Decoy Flares](../../../player/specials/README.md#decoy-flares-l27) retarget both kinds.
- **Specials**: every special works (open space; [Act 5 specials](../../../player/specials/README.md#act-5-specials-draft));
  the Sonar Pulse reads `NO WATER`. The EMP Burst drops every Shard link and every hex shield on the screen.

## Secrets and pickups

- **Dacosta's claim** (the secret, section 4, t≈100): a small hollow rock with a prospector's claim marker
  bolted to it, drifting through the Drift among the Mite rocks; **12 HP**, broken, it frees a crate worth
  **100** in Act 1 terms (655). The Salvage scanner's glint shows it at any range. Its event line, the
  Bellwether: "*That's old Dacosta's claim marker. He'd want it spent on a good cause.*"
- **Shield cell**: dropped by the first Hornet (t=24) at its death.
- **Armour patch**: the Bellwether's crew throws one out of the cargo hatch at t=110 (from the bow if the
  cargo is lost; none if both are), at the tug's position, drifting up toward the ship.
- **Overdrive**: dropped by the last Hornet of the t=134 trio.

## Radio chatter

Spoken radio, draft text for the **1 s rule** (text at about 12 characters a second plus a 1.5 s lead
for the length check; 0.4 s of silence; no timed line more than 1 s late); event lines drop after 6 s.
Rook's lines carry `requires: escort` (he flies from L30, [wingmen](../../../player/wingmen/README.md#rooks-rescue-and-return-l29l30)).
The **Bellwether** is a new generic speaker, the tug's captain (a belt miner in her fifties, dry and
tired; cast and portrait by the act's audio and art steps, a civilian radio portrait). `{section}` is the
hit or lost section's name (`bow`, `cargo`, `engines`), a new placeholder.

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Bellwether | "Bellwether to Aegis. Four hundred souls aboard and one engine room. Stay close." |
| t=10 | Okafor | "Lancer, the tug is yours. Nothing touches it." |
| t=16 | Rook | "Big, slow and friendly. I can work with that." |
| t=25 | Varga | "Hornet. Its missiles are going for the tug. Shoot them down: two hits each." |
| first section hit (event) | Bellwether | "We're hit in the {section}! Get them off us!" |
| t=39 | Okafor | "Contacts behind you, on the tug's engines." |
| t=59 | Varga | "Linked drones. The beam cuts across the hull. Kill one and the link drops." |
| t=86 | Bellwether | "You know why half of Ceres turned? Helix came with medicine." |
| t=93 | Bellwether | "Lung rot, bone loss. Gone in a week. Nobody asked where it came from." |
| t=101 | Varga | "Vrell biotech. They were cured with the enemy's blood." |
| t=110 | Bellwether | "Throwing you a patch from the cargo hatch. It's all we've got." |
| secret found (event) | Bellwether | "That's old Dacosta's claim marker. He'd want it spent on a good cause." |
| t=130 | Okafor | "Ambush ahead. Gunships and missile boats. Hold the line, Aegis." |
| t=137.5 | Rook | "Three of them. I'll take the left one." |
| section lost (event) | Bellwether | "We've lost the {section}! We're still flying." |
| t=166 | Okafor | "The Vesta relay. A frigate is waiting. Bring them in." |
| t=179 | Bellwether | "Relay in sight. Thank you, Aegis. Tell Ceres we made it." |
| level end, all three home (`allies: [3]`) | Okafor | "Every one of them home. Well flown." |
| level end, one or two lost (`allies: [1, 2]`) | Okafor | "They made it. Not all of the ship did. That's enough today." |
| mission failed | Okafor | "The tug is gone, Lancer. Four hundred people. We go again." |

Timeline check (text, by the rule above): the captain's opening line (t=1) closes at about t=9.3, before
Okafor's (t=10); her two-line story (t=86 and t=93) closes at about t=100.3, Varga's answer (t=101) at about
t=107.2, before the patch line (t=110); Okafor's ambush call (t=130) closes before Rook's (t=137.5). The event lines take
the gaps and drop after 6 s. **The story beat** (the act's L31 beat): Helix bought loyalty with Vrell
cures, told by the refugees; Varga names what it was. Without Rook his two lines drop and nothing else
moves.

## Music & ambience

[Track 13](../../../audio/music/README.md#track-list), the Act 5 B theme (driving industrial with Vorne's
motif in the open; in the track list without a file): its intro in section 1, the **B base from section
2**, the **full mix in section 5** (the ambush), out over the relay into the
level end. Ambience: a belt open-space bed (proposed `belt-open`: a low hull hum heard through the cockpit,
distant metal creaks, no wind; the act's audio step decides) with the tug's engine bed under it while its
engines live. New sounds this level needs: the tug's engine bed and its section-hit and section-lost
sounds (a heavy hull impact, a klaxon), the link hum and snap, the saw whine and dash spin-up, the Hornet
launch and the missile run, the rock ricochet; listed in [sfx](../../../audio/sfx/README.md) by the act's
audio step (Freesound for real-world sounds only, no bubbly or popping sounds).

## Credit budget

Budget(31) = 700 × 1.07³⁰ ≈ **5,329** ([economy](../../../systems/economy/README.md#per-level-budget)).
Data in **Act 1 terms** (Skitter 5, Mote 2, Talon 12, its leader 25, Dart 2, Hornet 26, Buzzsaw 12, Shard
Drone 14, Gunship 28, Mite 3, SAM Nest 15, the crate 100, the section pay 40, the secondary 100), × the act
factor **6.5536** (1.6⁴), bounties × the scale. With the waves at ≈ 112 a minute a **`bounty_scale` of
0.40** (Skitter 13, Mote 5, Talon 31, leader 66, Dart 5, Hornet 68, Buzzsaw 31, Shard 37, Gunship 73, Mite 8,
SAM Nest 39 a kill) puts the typical haul at **5,342 (+0.3 %)** (by hand with the economy's shares: air
kills 60 %, ground 80 %, the secret and the secondary 50 %, the escort pay 100 %); `TypicalHaulTest`
confirms it in the build.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 84 × 5 + Mote Swarm 50 × 2 + Talon 45 × 12 + leader 5 × 25 + Dart Drone 75 × 2 (45 in waves, ≈ 30 launched) + Hornet 10 × 26 + Buzzsaw Drone 24 × 12 + Shard Drone 22 × 14 + Gilded Gunship 5 × 28 + Asteroid Mite 18 × 3 | 6,189 | 3,713 |
| Ground targets: SAM Nest 6 × 15 | 234 | 187 |
| Primary objective: 3 sections home × 40 | 786 | 786 |
| Secret: Dacosta's claim (hidden crate) | 655 | 328 |
| Secondary: clean hull | 655 | 328 |
| **Total** (bounty scale 0.40) | **8,520** | **5,342** |
| Budget(n) = the typical haul's target; typical +0.3 %, perfect 1.60 × budget | | 5,329 |

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
- **Hard**: sections 180 / 120 / 120; every Hornet, Dart and Shard group sent at the tug; Shard groups of
  4, links lit after 0.5 s; Hornet salvos every 4 s at 100°/s; the streams one larger.

## Concept art

None yet. The level needs (round 60's proposals, then part D's production art):

- the open-belt backdrop (six sections: the Bright Seam dock, the open belt, the two outcrops, the Drift,
  the relay with its beacon tower and the CDF frigate), the drifting rocks (big and small), the refugee
  haulers on `low-air`;
- the [mining tug](../../../allies/mining-tug/README.md) (its own document's concept), the Hornet, the
  Shard Drone and its links, the Buzzsaw's spin-up tell, the Dart Drone, the homing diamond (the unit
  documents);
- Dacosta's claim rock; the two briefing images; the Bellwether's radio portrait;
- the round's captures: the tug under a Hornet salvo, a missile shot off the bow, a Shard line across the
  cargo, a Buzzsaw bouncing off a rock, a section lost, the HUD's three bars.

## Implementation

- [ ] Level data `data.yaml` (when built in part D): sections, the rock drift lines, the outcrops, waves,
      ground targets, the escort block (the tug, its station, sway, HP by difficulty, the per-section pay,
      `targeted_by` per kind and difficulty), radio, budget, threat profile, briefing, backdrop, music
- [ ] The mining tug escort: entry, station and sway, the three sections, the hooked damage only, the
      climb-out, the fail when all three are lost ([mining tug](../../../allies/mining-tug/README.md))
- [ ] Hooked kinds: Hornet salvos homing on the nearest living section, Dart dives at the nearest section,
      Shard links dragged across the hull; everything else passes over the tug
- [ ] Drifting rocks on authored lines (break, indestructible, block shots), the Buzzsaw's ricochet off
      them, the Mites riding them
- [ ] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), the quiet windows, edge warnings for
      every side and rear entry, the limits above
- [ ] Escort pay per section home and the `CLEAN HULL` secondary in the debrief
- [ ] Radio voiced (the Bellwether a new generic speaker; the `{section}` placeholder), every timed line at
      most 1 s late, with Rook and without (`requires: escort`)
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel
- [ ] Briefing: both pages, each one screen, with their images
- [ ] Typical haul within ±5 % of budget(31) = 5,329 with the level's `bounty_scale`
- [ ] Music: track 13's stems by section; the belt ambience and the tug's engine bed
- [ ] Easy/hard variations; the autopilot keeps at least 2 sections home on every difficulty (with and
      without Rook)
- [ ] Production art and captures for the round

## Open questions

The design choices below are drafted on the recommended option, **pending the user's review in round 60**;
the alternatives:

- **Q4, the escort** (gap analysis): **a** a `huge` tug with three sections (drafted); **b** three ore
  haulers as an L04/L11 convoy (cheap, repeats Acts 1–2); **c** the tug on the play plane ahead of the ship,
  not to be shot through (blocks fire, frustrating).
- **Q-D1, the tug's size** (build gap check): **a** ≈ 160×260 centred ≈ 150 px above the bottom (drafted);
  **b** Q4 a's 200×300 (the hull dominates, bullets over a busy sprite); **c** a smaller 120×200 tug (less
  "huge").
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
  documents; pending the user's review in round 60.
- 2026-10-27: the level meets the Act 2+ floor from the start (≈ 112 a minute, a wave about every 2.9 s);
  81 % of the waves from the front, rear entries are Dart streams on the engines only.
- 2026-10-27: each attacker kind has its section: missiles on the bow (from the Hornets' hold at the top),
  rear Darts on the engines, links across the cargo; the tug keeps its station when a section is lost
  (presentation only), all three lost fails the mission.
- 2026-10-27: Buzzsaws chase the ship only; Rook flies his slot as usual (no guard order); the escort pays
  40 a section home and the `CLEAN HULL` secondary rewards keeping every section above half.
