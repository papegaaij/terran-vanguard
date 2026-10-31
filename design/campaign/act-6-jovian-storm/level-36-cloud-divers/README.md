---
title: Level 36 – Cloud Divers
design: approved
implementation: in-progress
art: proposed
depends-on: [../../../enemies/air/chimera, ../../../enemies/air/talon, ../../../enemies/air/gilded-gunship, ../../../enemies/air/wraith, ../../../enemies/air/spore-bomber, ../../../enemies/air/lamprey, ../../../enemies/README.md, ../../../world/jovian, ../../../player/generator, ../../../player/wingmen, ../../../ui/hud]
updated: 2026-10-31
---

# Level 36 – Cloud Divers

## Summary

Act 6 opens with **the dive**: Aegis Wing drops out of orbit ahead of the fleet into Jupiter's upper
atmosphere to break the Ascendancy's fighter cover over the storm bands. The first section runs above the
cloud decks at dive speed with the CDF carrier group behind and its drop craft (marked friendly) diving past;
then the wing goes down among the cream and ochre bands, where **foreground cloud banks** drift over the
player's plane and the act's first hybrid, the regenerating [Chimera](../../../enemies/air/chimera/README.md),
flies beside the Talons. Helium-3 **harvesters** skimming a band are the secondary; wind gusts and, at the
storm edge, the act's first three **lightning strikes** close it. Air only (`NO GROUND`). 192 s, ≈ 102 enemies a
minute at medium.

**Status: approved 2026-10-31 (round 72, D.2)** (drafted in the Act 6 design phase, step `m10-d-levels-1`, 2026-10-31). Written from the act roster, the gap analysis's recommended answers (Q2 a, Q3 a,
Q8 a, Q9 a, Q10 a, Q11 a; J1, J2, J3, J5, J10) and the wave 1 documents (the
[Chimera](../../../enemies/air/chimera/README.md), [regeneration](../../../enemies/README.md#regeneration), the Act 6
[enemy shields](../../../enemies/README.md#enemy-shields), the [Jovian system](../../../world/jovian/README.md)'s
media, moods and hazards, the [Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft),
[Rook in Act 6](../../../player/wingmen/README.md#rook-in-act-6-draft), the HUD's
[Act 6 additions](../../../ui/hud/README.md#act-6-additions-draft)). M10 part B (step B2, 2026-10-31): the hand tables
are the level's [data.yaml](data.yaml), rendered here, and measured with `levelReport` (*Waves*, *Credit budget*).

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. The [act](../README.md)'s intro comes
before it (Q11 a: Okafor from the carrier's bridge, four pages, page 3 Vorne intercepted); this briefing follows
it, in **September 2185** (J10). Rendered from the level's [data.yaml](data.yaml) (M10 part B, step B2):

<!-- data: briefing -->
> **Commander Okafor:** "Callisto sits behind Jupiter's air defences, and Vorne fuels his fleet
> from these clouds. Aegis Wing dives first: break the fighter cover over the upper bands so the
> fleet can follow, and bring down his helium-3 harvesters on the way. Rook flies your wing.
> Aurelia is below us. We go there next."
>
> **Dr. Varga:** "The wrecks from Ceres carried something new: Talon airframes with Vrell tissue
> grown through the seams. I call them Chimeras. Give one a second and it heals. A pilot still
> flies each one, and the healing costs him more than the machine. And the cloud banks down
> there drift over your plane: what flies under them dims, their shots never do. There is no
> ground, Lancer. The bombs stay idle."
<!-- /data -->

*Images* (production art in part B, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-36-the-dive`: a CDF chart of Jupiter's upper bands seen from orbit, the dive corridor as
  a dotted descent line through three cloud decks, the harvesters' lanes along one band in amber, the fleet's
  carrier group at the top, Aurelia marked far below.
- Varga's page `level-36-chimera`: her scan of a Chimera, the Talon airframe in grey outline with the tissue
  seams glowing teal, an HP readout climbing back with a teal edge, a time stamp "1.0 s".

*Hangar teaser* (shop screen before L36, the act's first; Rook speaks, not voiced):

<!-- data: teaser -->
> **Rook:** "Varga says the new ones heal. So no tickling, Lancer. Burn them."
<!-- /data -->

The hangar before L36 adds the [Plasma Arc](../../../player/weapons/plasma-arc/README.md) to the shop (Q7 b); the
plan raises the [Ion Beam](../../../player/weapons/ion-beam/README.md) to **L5** and keeps Rook's Autocannon
([Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft): 22 of 22 MW). The intel panel's
missing-ground warning names the Bomb Rack and Rook's Mortar if fitted ([Rook in Act 6](../../../player/wingmen/README.md#rook-in-act-6-draft)).
A `v0.3` save at the Act 6 hangar plays the act intro once on Continue before this briefing (the `act-intro:6` flag,
the act's document).

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule; the harvesters and the
drop craft are not enemies): the Lamprey streams from the sides, the Wraiths' rear ambushes from behind (as built).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (Talon wings, Chimera wings and swarms, Mote Swarms, Lamprey streams, Gilded Gunships and their Darts; Wraiths cloaked on `high-air` until they decloak), `low-air` (Spore Bombers, the harvesters); **no ground** |
| Attack directions | front 94% · sides 3% · rear 3% (the Wraiths decloak behind the ship, as built) |
| Density | 4 |
| Recommended traits | `forward`, `homing` |
| Hazards | Foreground cloud banks over the play plane; wind gusts (two spells); lightning strikes (three, section 4) |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: Jupiter, the upper atmosphere, `air`, front · **L1**: + directions (sides), density 4, hazards "cloud banks", "wind" and "lightning", the objective `BREAK THE AIR COVER`, the secondary `HARVESTERS 0 / 6` and the row `NO GROUND` · **L2**: + Chimera (new), Talon, Wraith, Spore Bomber, Lamprey, Gilded Gunship, Dart Drone and Mote portraits · **L3**: + `forward` and `homing` highlighted, the wave strip marking the harvester pairs and the three strikes |
<!-- /data -->

*Varga's intel line* per sensor-suite level (must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Jupiter's upper air, Lancer. Big sky, and it hides things." |
| L1 | "Mostly ahead. Clouds over your plane. No ground: bombs idle." |
| L2 | "New: Chimeras, hybrids that heal. Talons, Wraiths, bombers." |
| L3 | "Hold the beam on a Chimera. Homing finds what clouds hide." |

## Objective

- **Primary** `reach-end`: break the air cover down to the storm edge (the level ends where the bands thin
  over Aurelia's sky, Level 37's start).
- **Secondary** `escapes` (the harvesters): eight helium-3 **harvesters** cross the field in four pairs (t=72,
  86, 100 and 114, each pair with two Talon escorts). The secondary is met when **6 of the 8** are destroyed
  (`escapes: harvester`, `allowed: 2`); it fails at once when a third harvester leaves the bottom edge. The HUD
  counts to its target (the Act 5 lesson): `HARVESTERS n / 6`, n the harvesters destroyed, `DONE` in green at 6,
  `FAILED` in red at the third escape. Pays **100** credits in Act 1 terms (**1,049** in Act 6, the act factor
  10.486 = 1.6⁵); the typical haul counts 50 %. Easy: 5 of 8 (`n / 5`, `allowed: 3`); hard: 7 of 8.
- **No ground** (`ground: none`, J2: the cloud decks are scenery): the Bomb Rack and every bomb delivery read
  `NO GROUND` and stay idle while drawing their power (Level 30's rule, built); the content validator rejects
  ground targets here. The Airstrike (CDF bombers from the carrier) and the EMP Burst work
  ([media per level](../../../world/jovian/README.md#media-per-level)).

### The harvesters

Level-owned craft (not a roster unit, as Level 12's sondes): an uncrewed Ascendancy helium-3 scoop that skims a
band below the play plane. Numbers are first values for the level data.

| Field | Value |
|---|---|
| Layer, size | `low-air` (hit by all weapons, no contact: the [layer rules](../../../enemies/README.md#layer-rules)), sprite 96×56 px at `low-air`'s scale, hit box 72×40 |
| HP | **160** (easy 120 / hard 208, the global multipliers); a level object: no act or beam-era factor, no shield. ≈ 1.1 s under the reference of 144, ≈ 0.9 s under the plan's beam L5 |
| Path | a pair enters at the top edge 160 px apart and skims **down the screen at 50 px/s** (it flies with the band's wind, slower than the scroll) on a shallow diagonal (30 px/s sideways, the pair's two hulls mirrored), ≈ 11 s on the screen, and escapes at the bottom edge; never over the other's hull |
| Escorts | each pair's two Talons fly `orbit` at 90 px round the pair's middle (the `carrier + escorts` formation); they leave on their own swoop when both harvesters are gone |
| Fire | none; never hurts anything |
| Look | a long gunmetal hull with gold trim and red running lights, a wide intake scoop under it trailing a pale gas plume into the band below; the plume a smooth gradient (≥ 32 steps, no dither) |
| Targeting | homing shots, Rook and the Targeting computer pick them like enemies; not counted as enemies (density, the floor, kill ratios) |
| Credits | **30** each (Act 1 terms), counted with the ground targets' share in the typical haul (they are slow and in plain sight) |
| Destroyed | the hull breaks in two and falls into the band, the plume flaring once along its length (a smooth glow, no debris on the play plane); a `medium` explosion sound |

### The fleet's drop craft

Two pairs of CDF Stormhawks from the carrier dive past the ship at **t=4 and t=10**, up from the bottom edge and
out at the top at 400 px/s (≈ 1.5 s on the screen), a presentation of the fleet following the wing down. They are
flagged **`neutral`**: the HUD's [friendly marker](../../../ui/hud/README.md#in-the-play-field) opens its
"FRIENDLIES INBOUND — HOLD FIRE" banner 2 s ahead and brackets them while they are on the screen (the Act 5
lesson). No fire, no contact; enemy fire and contact pass through them; homing shots and Rook never pick them.
The carrier group itself stays on the `deep` layer (section 1).

## Layout

Scroll **160 px/s** in the dive (section 1), **120 px/s** into the bands, **100 px/s** over the harvester fields
(a pair stays readable), **120 px/s** at the storm edge; each change eased over 2 s. The setting is the
[Jovian system](../../../world/jovian/README.md#sub-locations-per-level)'s upper atmosphere in Q10 a's **L36 grade**
([moods](../../../world/jovian/README.md#moods-per-level-q10)): high sunlit bands, cream, ochre and rust under a
pale haze, the sun from the top left; enemy bullets keep their dark outline over the brightest cream. Media:
Jupiter's **air** (J2): explosions rise as smoke and fire, weather allowed. Motion budget: the banks' slow drift
and the Chimeras' seam pulse are the strong elements; the deep bands crawl.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Dive | 0–24 | 0–3,840 | 160 | clear | `deep`: Jupiter's curve and the bands far below, the CDF carrier group and its escorts (lights blinking). `low-air`: the first cloud tops rushing past. `high-air`: thin ice haze. No banks yet. | The dive: popcorn and Talons at speed; the drop craft marked friendly (t=4, t=10). |
| 2. Into the Bands | 24–70 | 3,840–9,400 | 120 | clear | `deep`: storm bands in cream and ochre. `low-air`: cloud wisps. `high-air`: the **first foreground banks** from t≈28 (small, slow). | The Chimera's intro pair (t=34, quiet window t=32–38), the first wings; Wraiths; gust spell 1 (t=58, t=64). |
| 3. Harvester Fields | 70–125 | 9,400–14,920 | 100 | clear | `deep`: a broad rust band. `low-air`: the **harvesters** in four pairs (t=72–125) with their plumes, Spore Bombers among them. `high-air`: banks at their densest (a third of the field). | The secondary; the Chimera swarms under the banks (`homing`). |
| 4. Storm Edge | 125–192 | 14,920–22,940 | 120 | clear | `deep`: the band turns brown-grey, the storm wall of Level 38 far ahead with lightning inside it. `high-air`: banks thinning; the pathfinder pod (the secret, t≈146). Lightning flashes from t=130, **strikes** at t=138, 152 and 170. Level end at t=192 as the bands thin over Aurelia's sky. | The act's first strikes; gust spell 2 (t=156, t=162); the run to Aurelia. |
<!-- /data -->

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). Rendered from the data (M10 part B, step B2) and measured with `levelReport`.

- **HP** from the proposed Act 6 reference DPS (J1: **144** at L36,
  [balancing basis](../../../enemies/README.md#balancing-basis)): the Chimera is new here (40 flown, its
  [regeneration](../../../enemies/README.md#regeneration)); the Talon flies its **Act 6 shield of 24** (leader 48,
  the [Act 6 weights](../../../enemies/README.md#enemy-shields)) over its beam-era 32; the returning `medium` units
  take the act HP factor: the Gilded Gunship ×144/124 (hull ≈ 102, shield ≈ 28), the Wraith ×144/66 (16 → 35),
  the Spore Bomber ×144/32 (24 → 108). The Lamprey is `small` (beam era ×2: 6 HP); Motes and Darts are `tiny`.
- **Tiny units** (J5, the working note's open item): Act 6 has no new tiny unit, so this level flies the Vrell's
  [Mote Swarms](../../../enemies/air/mote-swarm/README.md) (136) and the Gunships'
  [Dart Drones](../../../enemies/air/dart-drone/README.md) (≈ 30); they carry the count, the Chimeras the lesson.
- **Before the turn** ([the three-way war](../../../enemies/README.md#the-three-way-war)): the Vrell (Motes, Lampreys,
  Wraiths, Spore Bombers) fly beside the Ascendancy and its hybrids; no unit hurts another, no prey links.
- **Density and pacing** (the Act 2+ floor from the start): the README's 58 waves (325 enemies with an estimated 30
  launched Darts, ≈ 102 a minute) flew 2.00 on the screen with 10 pauses over 2 s at medium (`levelReport`, the plan's
  fit, step B2), so step B2 tuned them as Act 5's levels were (pending the user's review in round 74): every Talon
  wave but the escorts at **130 px/s** through its swoop, and **ten firing Talon pairs on the offbeats** (t=16.5, 23,
  26.5, 50, 61.5, 93.5, 110.5, 133.5, 147.5, 161.5); the t=140 Lampreys at t=139 (easy's pause after the struck
  wing), the t=151 Spore Bombers at t=150 and the t=169 Chimera wing at t=168 (the world's strike rule: no tell
  within 1 s of a wave's entry). Measured (`levelReport -Plevel=36`): **315 enemies, 101 a minute** at medium (the
  wave units; the act's minimum 75), **2.76 on the screen**, 73 shots a minute, two pauses over 2 s (the quiet
  window's t=35.4–39.2 and t=39.9–42.2), 12.8 s of calm at the end; easy 252 (81 a minute, two pauses over 3 s), hard
  382 (123 a minute, 4.6 on the screen, two pauses); the harvesters and the drop craft are not counted. Waves by
  direction: front 94 %, sides 3 % (the Lamprey streams on the gusts' wind, each side entry **edge-warned 3 s**), rear
  3 % (the Wraiths' rear ambushes, as built). Quiet window t=32–38: the Chimera pair alone.
- **Spacing** (the Act 5 lesson): no two `medium` air hulls overlap: the Gunships keep ≥ 80 px between hull edges
  (the t=166 pair 2 s apart), and no Gunship enters while a Wraith is decloaked in its lane. At most two Chimera
  groups on the screen (the [Chimera](../../../enemies/air/chimera/README.md#difficulty-and-level-use)'s rule).
- **Calm at the end** (the Act 5 lesson): the last wave enters at t=178 and is gone by ≈ t=185; **≥ 4 s** of calm
  before the level end at t=192.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Out of the haze below the dive; the fleet's drop craft pass at t=4 and t=10 (not enemies) |
| 4 | 1 | line abreast | [Fleet Stormhawk](../../../enemies/air/fleet-stormhawk/README.md) | 2 | rear | The fleet's drop craft, up from the bottom edge (neutral: the friendly banner from t=2) |
| 6 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 9 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 10 | 1 | line abreast | [Fleet Stormhawk](../../../enemies/air/fleet-stormhawk/README.md) | 2 | rear | The second pair of drop craft (the banner from t=8) |
| 12 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Squad leader at the tip |
| 15 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 16.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 18 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 21 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 23 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 25 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Into the bands; the first foreground bank at t≈28 |
| 26.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 28 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The last wave before the quiet window |
| 34 | 2 | line abreast | [Chimera](../../../enemies/air/chimera/README.md) | 2 | front | **The Chimera's first appearance**: an intro pair in open sky (no bank), quiet window t=32–38; Varga's line ahead of it |
| 39 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 42 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Squad leader; drops the Overdrive |
| 45 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | The first Chimera wing, under the edge of a bank |
| 48 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 50 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 51 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Cloaked on `high-air`, decloaking behind the ship (their rear re-entry warned as built) |
| 54 | 2 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | About 6 Darts launched (two streams of 3); drops the Shield cell |
| 57 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Gust spell 1 (from the left) at t=58 and t=64 |
| 60 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 61.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 63 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 66 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | left side | Storm chasers on the gust's wind; edge-warned 3 s ahead |
| 69 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 72 | 3 | carrier + escorts | [Helium-3 Harvester](../../../enemies/air/harvester/README.md) | 2 | front | **Harvesters 1–2** (the secondary, not counted as enemies) |
| 72 | 3 | carrier + escorts | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Their two Talon escorts in `orbit` at 90 px round the pair (hard three) |
| 75 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 78 | 3 | column | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | `low-air`, among the harvesters' lane; spore mines rise to `air` |
| 81 | 3 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front | The first Chimera swarm |
| 84 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 86 | 3 | carrier + escorts | [Helium-3 Harvester](../../../enemies/air/harvester/README.md) | 2 | front | **Harvesters 3–4** |
| 86 | 3 | carrier + escorts | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Their two Talon escorts |
| 89 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Cloaked; decloak behind the ship |
| 92 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 93.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 95 | 3 | column | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | `low-air` |
| 98 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 100 | 3 | carrier + escorts | [Helium-3 Harvester](../../../enemies/air/harvester/README.md) | 2 | front | **Harvesters 5–6** |
| 100 | 3 | carrier + escorts | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Their two Talon escorts |
| 103 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | front | Storm chasers out of a bank |
| 106 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | About 6 Darts; keeps ≥ 80 px from every other medium hull |
| 109 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 110.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 112 | 3 | column | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | `low-air` |
| 114 | 3 | carrier + escorts | [Helium-3 Harvester](../../../enemies/air/harvester/README.md) | 2 | front | **Harvesters 7–8** |
| 114 | 3 | carrier + escorts | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Their two Talon escorts |
| 117 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Squad leader |
| 120 | 3 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front | Under the banks: the homing lesson |
| 123 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 126 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Cloaked; decloak behind the ship |
| 129 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 132 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The storm edge darkens; lightning flashes (no damage) from t=130 |
| 133.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 136 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | The first lightning **strike** at t=138 (telegraph from t=137) lands among them (step B3: t=136, not 135: from t=135 the wing had swooped past the column before it struck; the tell 1.0 s after its entry) |
| 139 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | front | Step B2: t=139, not 140 (easy's pause after the struck wing) |
| 143 | 4 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | About 6 Darts |
| 146 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The pathfinder pod (the secret) drifts in under a bank at t≈146 |
| 147.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 149 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Squad leader; strike 2 at t=152 |
| 150 | 4 | column | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | `low-air`; step B2: t=150, not 151 (strike 2's tell at t=151) |
| 154 | 4 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front | Gust spell 2 (from the right) at t=156 and t=162 |
| 157 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Cloaked; decloak behind the ship |
| 160 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 161.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step B2: a firing Talon pair on the offbeat (the beam erases the fodder within a second) |
| 163 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | right side | On the second spell's wind; edge-warned 3 s ahead |
| 166 | 4 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | 2 s apart, ≥ 80 px between the hulls; about 12 Darts; strike 3 at t=170 |
| 168 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 7 | front | Step B2: t=168, not 169 (strike 3's tell at t=169) |
| 172 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 175 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 178 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The last wave; gone by ≈ t=185, calm to the end at t=192 |

Totals: Mote Swarm 136 · Talon 80 · Chimera 56 · Wraith 8 · Gilded Gunship 5 · Lamprey 20 · Spore Bomber 10; the Fleet Stormhawks (4), the Helium-3 Harvesters (8) are not enemies.
<!-- /data -->

The Talons count the harvesters' 8 escorts (hard 12) and the 20 of the offbeat pairs; the Gilded Gunships launch
≈ 30 Dart Drones (two streams of 3 before a typical kill), counted with the kills in the credit budget.

## Ground targets

None: the level has no ground layer (`ground: none`, J2).

## Hazards

All three are the [Jovian system](../../../world/jovian/README.md#hazards--set-pieces)'s rules; this level places them.

- **Foreground cloud banks** (Q2 a): banks on the `high-air` scenery drawn **over `air` bodies**, translucent (a
  body under a bank at ≈ 35 % with its running lights, engine glow, seam glow and attack tells at full); bullets,
  the ship, Rook and pickups always above them; homing shots, Rook and the Targeting computer track as usual. In
  this level: from t≈28 (section 2: small, slow banks, never over the Chimera pair at t=34), densest in section 3
  (the cap: a third of the field), thinning in section 4. Never a bank resting over the ship's band (the bottom
  140 px) for more than 2 s; a lightning flash makes a bank see-through for its 0.5 s. They drift down with
  `high-air`'s parallax, so a bank crosses the screen in ≈ 3–4 s. Smooth: ≥ 32 steps, no ordered dither, drawn once.
  The t=120 Chimera swarm and the t=103 Lampreys enter **under** banks: the `homing` lesson.
- **Wind gusts** (Level 12's [gusts](../../act-2-homefront/level-12-storm-front/README.md#weather) as built:
  40 px/s for 2.5 s, telegraphed 1.0 s by the brightening streaks and the upwind arrow, pushing positions, never
  bullets): **spell 1 from the left** at t=58 and t=64, **spell 2 from the right** at t=156 and t=162 (Jupiter's
  bands run their jets in opposite directions; the storm streaks in the backdrop change direction with the band).
  The Lamprey streams ride each spell's wind in from its upwind edge (t=66 left, t=163 right).
- **Lightning strikes** (J3; "a few allowed in L36"): three strikes as the act's foretaste of Level 38, at **t=138,
  152 and 170** (telegraph from 1.0 s before), each a 40 px column at an authored x: the first through the t=136
  Chimera wing's lane (lightning kills a Chimera and pays: the lesson that the sky can work for the player), the
  others in the open lanes left of and right of the centre. Flash-only lightning (Level 12's) from t=130 dresses the
  storm edge; never two strikes within 1.5 s, never a telegraph starting on the ship's x within 1 s of a wave's
  entry (the world's rule). Rook keeps out of a told column ([Rook in Act 6](../../../player/wingmen/README.md#rook-in-act-6-draft)).

## Secrets and pickups

- **The pathfinder pod** (t≈146, x 400, drifting down under a foreground bank): a CDF pathfinder's cargo pod from
  the scouts who dived before the fleet, a small breakable object on the play plane (20 HP, like a small rock);
  broken, it frees a hidden crate worth **100** in Act 1 terms (1,049 in Act 6). The Salvage scanner's glint marks it
  through the bank. Varga's event line (dropped after 6 s if the radio is busy): "*A pathfinder's pod, from the
  scouts who dived before you. Take it.*" In the data a debris chunk `pathfinder-pod` (28×28 px, ART-B's sprite) that
  `reveals: pathfinder pod` (step B2), drifting [−6, −90] px/s from x 400 at t=146.
- **Overdrive**: dropped by the t=42 squad leader.
- **Shield cell**: dropped by the t=54 Gilded Gunship.
- **Armour patch**: a [CDF supply drone](../../../allies/README.md#cdf-supply-drone) from the carrier releases one at
  (240, 380 y-up) at t=96, between the second and third harvester pairs.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), drafted for the **1 s rule** (≈ 12 characters a second, a
1.5 s lead, 0.4 s of silence; event lines drop after 6 s); `RadioTimelineLevel36Test` checks them with Rook and
without. Rook's barks carry `requires: escort` (the t=118 homing bark, the first lightning kill); his scripted line
(t=11, his pulp line of the level) does not. **Speakers**: Okafor (from the carrier, J10), Varga, Rook (the Act 6
speakers are D-story's table). **Vorne is silent here**: his intercepted invitation is the act intro's page 3.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, you're cleared to dive. Break their air cover over the bands. The fleet follows you down." |
| t=11 | Rook | "Jupiter. All sky and no floor. If we go down here, Lancer, we fall for a week." |
| t=20 | Varga | "Under the haze the cloud banks lie over your plane. Bodies dim under them. Their shots never do." |
| t=30 | Varga | "There: Chimeras. Talon frames, Vrell tissue in the seams. Pick one and keep hitting it." |
| t=40 | Varga | "See the seams glow? It's healing. Hold the beam on it, or it grows back." |
| t=48 | Okafor | "Wraiths overhead, cloaked. They come out behind you." |
| t=67 | Okafor | "Helium-3 harvesters ahead, skimming the bands. That's Vorne's fuel. Bring down six." |
| t=90 | Varga | "Spore bombers under the harvesters. Vrell, still flying with the gold ones. For now." |
| Secondary met (`secondary-objective`, the sixth harvester down) | Okafor | "Six harvesters down. His fleet will feel that at Io." |
| Secondary failed (`secondary-failed`: the third harvester away; easy the fourth, hard the second) | Varga | "Too many got through. Their fuel reaches the refineries." |
| t=118 | Rook | "Homing's earning its keep. Those clouds are full of them." |
| t=128 | Varga | "A white line drawn down the sky means lightning. Get out of it. A second later it strikes." |
| The first enemy killed by a strike (event, bark) | Rook | "Ha! The sky's on our side." |
| t=172 | Okafor | "The bands are thinning. Aurelia is ahead, Aegis." |
| Level end | Okafor | "First dive done. The fleet is in the clouds behind you. Aurelia next." |
<!-- /data -->

Timeline check (text): the timed lines close at ≈ 10.5, 19.0, 29.5, 38.8, 47.4, 53.8, 75.4, 98.5, (124.2), 137.0 and
177.5; nothing overlaps. The secondary resolves between ≈ t=100 and t=125 (the third and fourth pairs), in the gap
between t=98.5 and t=128 (its line waits up to 6 s for the homing bark). Varga's t=30 line comes ahead of the
Chimera pair (t=34) and her t=40 line after its first heal; the t=128 line lands 9 s before the first strike's
telegraph. "Fuel at Io" points to Level 39, "Aurelia" to Level 37.

## Music & ambience

The Act 6 A theme **"Eye of the Storm"** ([track 14](../../../audio/music/README.md#track-list), chosen in round 03;
full length and stems in M10 part B): its intro (storm strings, a far thunder roll) over the dive, the **base stem**
from section 2 (into the bands), the **full mix** from the harvester fields (section 3) to the end, Vorne's motif
against the main motif as the theme has it. The per-level music table and the B theme's levels (track 15) are
D-audio's ([music](../../../audio/music/README.md)). Ambience: the `jovian-sky` bed ([sfx](../../../audio/sfx/README.md#ambience-per-setting): high wind on the hull,
distant thunder, the carrier's radio hiss), the dive's wind as a layer over section 1. New sounds this level needs,
for the act's sfx step ([sfx](../../../audio/sfx/README.md)): the dive's rushing wind (section 1), the drop craft's
pass-by, the harvester's scoop drone and its break-up, the lightning strike's close crack over Level 12's thunder
(J3), the gust's roar (Level 12's), the Chimera's sounds (its document's brief); Freesound only for real things
(CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(36) = 700 × 1.07³⁵ ≈ **7,474** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act
1 terms** (Mote 2, Talon 12 (a leader 25, counted at 12 here), Chimera 14, Dart Drone 2, Lamprey 12, Spore Bomber 25,
Wraith 30, Gilded Gunship 28, the harvester 30, the crate 100, the secondary 100), × the act factor **10.486**
(1.6⁵), bounties (the harvesters too) also × the scale. A **`bounty_scale` of 0.31** (the earlier acts' model:
typical = 0.663 of the kills, 0.8 of the harvesters as ground targets, half of the secret and the secondary; a first
estimate for part B to confirm with `TypicalHaulTest` and `levelReport`, the Dart count with it) puts the typical
haul at **7,504 (+0.4 %)**, the perfect run at 11,674. No boss or mid-boss, no ground targets.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Mote Swarm 136 × 2 + Talon 80 × 12 + Chimera 56 × 14 + Wraith 8 × 30 + Gilded Gunship 5 × 28 + Lamprey 20 × 12 + Spore Bomber 10 × 25 + launched Dart Drone 30 × 2 | 9,687 | 5,812 |
| Helium-3 Harvesters (not enemies): 8 × 30 | 784 | 627 |
| Squad leaders: 4 × 13 extra (perfect run only) | 168 | 0 |
| Secret: pathfinder pod (hidden crate, 1% of budget) | 1,049 | 524 |
| Secondary: bring down 6 of the 8 harvesters | 1,049 | 524 |
| **Total** (bounty scale 0.31) | **12,737** | **7,488** |
| Budget(n) = the typical haul's target; typical +0 %, perfect 1.70 × budget | | 7,474 |
<!-- /data -->

**Measured** (`levelReport`, step B2, after the waves' tuning): the typical haul **7,488 (+0.2 %)** at the README's
scale **0.31** (kept), the perfect run 12,737 (1.70 × budget; `TypicalHaulTest`); easy 6,528, hard 8,542. The
harvesters' 30 credits are paid with the ground targets' share (`haul: ground-targets`), the Squad leaders' extra in
a perfect run only. The scale sits under the gap analysis's 0.40–0.56 band: the opener's paying mix (56 Chimeras at 14, the Wraiths and
Spore Bombers at 25–30) is richer than Act 5's popcorn-heavy levels, whose built scales also fell to 0.27–0.43 once
measured. **Balance plan**: the plan's L36 visit buys the Ion Beam L5 (**forward / reference 1.19**, the act's
highest: [Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft)) and keeps 22 of 22 MW, no room
for a utility until the L40 visit (Q8 a, J11). The level expects `forward` (the beam on the Chimeras) and `homing` (under
the banks) and is flyable without them (the Chimeras heal only between hits; Pulse-style steady fire denies it).

**Measured** (step B2): part A built the plan's L36 visit as the hand plan's own alternative (the Tesla Coil Pod L2 on
the left wing, the beam L5 at L37: the beam L5 first did not fit `BalanceTest`'s sheet; pending round 73), so the
level flies a **forward / reference of 0.99** (143 against 144, `tools/balance.py`), not 1.19. With that fit the small
units killed live **1.72 s** on the screen at medium against Level 10's 1.60 s (**1.07×**, inside 0.9–1.5×), the
Chimeras **1.81 s** (1.13×; 44 killed), so they live past their 1.0 s heal delay; the autopilot wins 3/3 on every
difficulty and meets the secondary 3/3 on easy and medium (hard 0/3: step B3's autopilot).

**The 1.19 ratio** (the working note's open item): this level does **not** need the generator document's alternative
plan (the Tesla Coil Pod at L36, the beam L5 at L37). The surplus is spent on purpose: the Chimeras' regeneration
asks for a held beam, the Talons fly twice their Act 5 shield, and the returning mediums take the act factor, so the
beam L5 is the level's answer rather than an overshoot. The alternative stays the fallback if part B's `levelReport`
finds a killed small unit living under **0.9×** Level 10's time on the screen (the beam-era rule) or the Chimeras
dying before their first heal shows.

## Difficulty notes

- **Easy**: the Chimera's easy hooks (15 %/s after 1.3 s, a pair every 2.0 s); the harvesters 5 of 8; two strikes
  (t=138 and t=170, never within 60 px of the ship's x at the telegraph's start) and Level 12's easy gusts (25 px/s,
  a 1.5 s telegraph); the banks capped at a quarter of the field.
- **Hard**: the Chimera's hard hooks (25 %/s after 0.8 s); the harvesters 7 of 8 with three escorts a pair; the
  strikes plus a **tracking** column at t=160 that locks on the ship's x at its telegraph's start; gusts at 55 px/s;
  the other waves by the formation lever, trimmed by hand so never more than two Chimera groups and two Gunships are
  on the screen.

## Concept art

Round 72 (wave 5): Q10's mood sheet for the L36 grade with Q2's foreground-bank readability (ART-R-B, in the
[Jovian system](../../../world/jovian/README.md#concept-art)), the Chimera's unit sheets in its document (ART-R-A).

Round 74 (M10 part B, step ART-B; production art, pending the user's review): the backdrop kit by
`tools/art/backdrop_l36.py`, the harvester, the drop craft and the pod by `tools/art/jovian_units.py` (held in
`build/art-held/sprites/` until step B2's data claims them; the Chimera's frames in
[its document](../../../enemies/air/chimera/README.md#concept-art)), the briefing images in the
[briefing](../../../ui/briefing/README.md#concept-art) (`briefing-images-final-r74-a`). The harvester's look is QB-1 a
(m10b-gaps §7: the drafted look; b a scoop hull under a gold anti-grav ring), pending round 74. The banks are A.12 a's
cream veil: each bank piece's image is its rectangle in the proposal's `banks:`, measured there at most 27.4 % of the
field (easy 21.7 %) and at most 1.8 s over the ship's band. Prompts and briefs: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r74-a.png](concept/backdrop-final-r74-a.png) | Production backdrop: eight screens at half size with stand-ins (the limb and the carrier group, the bands far below, the first bank, a Chimera wing under a bank's edge, the harvester fields' banks with the Lampreys and the swarm, the pod under a bank beside the storm wall's lightning, the end), the six bank pieces with the cap and band numbers, the deep pieces at 1×, the tile sets, and the explosion read (`explosion-small` over each section's brightest window and a bank) | proposed (round 74) |
| [concept/backdrop-final-r74-a.gif](concept/backdrop-final-r74-a.gif) | t 100–110 at half size: the harvester fields, banks drifting over the play plane, a harvester pair with their plumes | proposed (round 74) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (tile sets cut at the sections' rows, pieces, placements, the `banks:` with their pieces), the sections and the pod's `debris:` for step B2 (not data) | proposed (round 74) |
| [concept/harvester-final-r74-a.png](concept/harvester-final-r74-a.png) | The helium-3 harvester (QB-1 a): both directions at 2×, the plume's flow, the break (12 frames with the flare and a medium explosion), a pair with two Talon escorts, one under a bank (sheet) | proposed (round 74) |
| [concept/harvester-final-r74-a.gif](concept/harvester-final-r74-a.gif) | A pair skimming down the rust band with their plumes, the left one breaking in two and sinking | proposed (round 74) |
| [concept/fleet-stormhawk-final-r74-a.png](concept/fleet-stormhawk-final-r74-a.png) | The drop craft: the CDF Stormhawk in the fleet's grey and blue at 16 headings beside the player's ship, two moments of the dive (sheet) | proposed (round 74) |
| [concept/fleet-stormhawk-final-r74-a.gif](concept/fleet-stormhawk-final-r74-a.gif) | Two pairs diving up the screen at 400 px/s over the dive's bands with their exhaust streaks, at 1× | proposed (round 74) |
| [concept/pathfinder-pod-final-r74-a.png](concept/pathfinder-pod-final-r74-a.png) | The pathfinder pod: its 16-frame tumble and the 8-frame break in air at 2× (sheet) | proposed (round 74) |
| [concept/pathfinder-pod-final-r74-a.gif](concept/pathfinder-pod-final-r74-a.gif) | The pod drifting down the storm edge and shot open | proposed (round 74) |

## Implementation

- [x] Level data `data.yaml` (M10 part B): `ground: none`, sections and speeds with 2 s eases, waves, radio, budget,
      threat profile, briefing, backdrop, music, the weather block (gusts, flashes, strikes) (step B2, 2026-10-31:
      `CloudDiversDataTest`; the banks and the pod from ART-B's proposal)
- [ ] `NO GROUND`: bomb deliveries idle and drawing power, the HUD's word; the validator rejects ground content
      here; the Airstrike and the EMP Burst work
- [ ] Foreground banks over `air` bodies (Q2 a: ≈ 35 %, tells at full, bullets/ship/Rook/pickups above, targeting
      unchanged, the third-of-field cap, the ship's band rule, the flash see-through), smooth (≥ 32 steps, no
      dither, drawn once)
- [ ] The harvester secondary (`escapes: harvester`, `allowed: 2`, `HARVESTERS n / 6` counting to its target;
      easy 5 of 8, hard 7 of 8); the harvesters not counted as enemies; their escorts in `orbit`
- [ ] The drop craft flagged `neutral` (the friendly marker's banner and brackets), untouchable, never picked
- [ ] Three lightning strikes and the two gust spells as above (the [Jovian hazards](../../../world/jovian/README.md#hazards--set-pieces)); strike kills pay
- [x] Wave script: ≥ 75 enemies a minute at medium with the launched Darts (`DensityTest`), the Act 2+ floor
      (`PacingTest`, `ActFloorTest`), side entries edge-warned 3 s, the quiet window t=32–38, ≥ 4 s calm after the
      last enemy, no two medium hulls overlapping (step B2: 101 a minute, 2.76 on the screen, two pauses, 12.8 s
      calm, no overlap; the waves' tuning in *Waves*)
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel36Test`, with Rook and without); Rook's
      barks `requires: escort`
- [ ] Threat profile with the `NO GROUND` row, Varga's four intel lines and Rook's teaser fitting the intel panel
      (`IntelPanelLayoutTest`); the Chimera's portrait
- [ ] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images; the act intro
      before it once (`act-intro:6`)
- [x] Typical haul within ±5 % of budget(36) = 7 474 with the level's `bounty_scale` (`TypicalHaulTest`; step B2:
      7,488 at 0.31, +0.2 %)
- [ ] Music and ambience: "Eye of the Storm" by section, the high-wind bed, the level's sounds (`Level36SoundsTest`)
- [ ] Easy/hard variations as in *Difficulty notes*
- [x] The autopilot (holding the beam on a hurt Chimera, the harvesters, out of a told column, the drop craft never a
      target) flies the level with Rook on every difficulty and with `--escort none` (step B3, `Level36Test`: 3/3 per
      difficulty both ways, no retry, `HARVESTERS` met on every run, no strike on the ship or Rook, no drop craft hit;
      Varga's failed-secondary line at the third harvester away; the t=138 strike kills in the Chimera wing's lane and
      pays; `ActPlaythroughTest` to Level 36, the release ending after it; Act 6's intro once on a v0.3 save)
- [ ] Production art and captures for part B's round

## Open questions

Left to the build (the document is approved as drafted):

- **The harvesters' numbers** (160 HP, 30 credits, 6 of 8): first values of this draft; part B tunes the HP so the
  typical player downs six with the plan's fit. Step B2: the plan's fit meets the secondary 3/3 on easy and medium
  with 160 HP (no tuning), hard 0/3; step B3: with the dive's autopilot (the harvesters first, from below them, the
  moves planned among the bullets) 3/3 on every difficulty with Rook and without, so the HP stay.
- **The harvesters' drift** (QB-5, round 74): the 30 px/s sideways cannot keep both "never over the other's hull" and
  "escapes at the bottom edge"; the data flies 8 px/s apart (the [harvester](../../../enemies/air/harvester/README.md)).
- **The Chimera's intro pair**: its document lists V-wings and swarms; the level adds a two-unit `line abreast` for
  the safe intro (the gap analysis's "safe pair"). Our reading: a level may thin a formation for an intro.

## Decisions

- 2026-10-31 (Act 6 design phase, step `m10-d-levels-1`; **decided 2026-10-31 (round 72)**): promoted from
  the act roster to a draft level document, hand tables only, no `data.yaml`. Air only (`ground: none`, J2), the
  Airstrike allowed; the dive at 160 px/s with the fleet's drop craft flagged `neutral`; the foreground banks by Q2 a
  from t≈28; the Chimera's intro pair at t=34 in a quiet window; the harvester secondary 6 of 8
  (`HARVESTERS n / 6`, level-owned craft, not enemies); three strikes (J3) and two gust spells; ≈ 102 enemies a minute
  with ≈ 30 launched Darts and 136 Motes as the tiny tier (J5); `bounty_scale` 0.31; the 1.19 forward ratio kept
  (the alternative plan only on a `levelReport` finding); Vorne silent (the intro carries him).
- 2026-10-31, design phase (step m10-d-act; decided 2026-10-31 (round 72)): Varga's briefing page names the hybrids' cost once
  ("*A pilot still flies each one, and the healing costs him more than the machine.*", the story's L36 beat, never
  shown); the ambience is D-audio's `jovian-sky` bed.
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): Q2 a (the foreground banks over
  `air`, translucent, bullets always above; b Level 12's rule only and c full hide rejected), Q3, Q8–Q11 and J1–J3,
  J5, J10 as drafted; A.12 a: the banks are the cream veil, and clouds at different altitudes do not differ in colour
  (the [world](../../../world/jovian/README.md#hazards--set-pieces)). The document approved as drafted (D.2; `design:
  approved`).
- 2026-10-31 (M10 part B, step B2; pending the user's review in round 74): the level's data from the approved tables
  and ART-B's proposal. Measured before tuning (`levelReport`, the plan's fit): 94.7 enemies a minute, 2.00 on the
  screen, 10 pauses over 2 s at medium (the floor failed), the haul −6 %. Tuned as Act 5's levels: the Talon waves at
  130 px/s, ten firing Talon pairs on the offbeats, the t=140 Lampreys at t=139; the t=151 Spore Bombers at t=150 and
  the t=169 Chimera wing at t=168 (the strike rule); `bounty_scale` 0.31 kept (+0.2 %). The Wraiths fly the built
  `rear ambush`, the Gunships a `column`. The harvester and the drop craft are unit data files (gap check §4); the pod
  a debris chunk that `reveals` its secret; the forward / reference measures 0.99 (part A's plan), not 1.19.
- 2026-10-31 (M10 part B, step B3; pending the user's review in round 74): the tests. Varga's "*Too many got
  through*" moves to a new radio event `secondary-failed` (the secondary's failure: the third harvester away, easy the
  fourth, hard the second), no longer the first harvester away. The t=135 Chimera wing enters at **t=136** (from t=135
  it had swooped past the first strike's column before it struck; t=136 keeps the tell 1.0 s after its entry). Rook
  now leaves a told strike's column to the side he is on (by his slot's side he crossed the column as the ship drifted
  and was struck in it on easy).
