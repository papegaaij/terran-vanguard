---
title: Mars
design: approved
implementation: n/a
art: chosen
updated: 2026-10-13
---

# Mars

## Summary

Mars under siege: red canyons, terraforming domes, dust storms, and Olympus Mons on the
horizon. Setting for Act 3 — where the Vrell mysteriously spare every Helix facility. Ends with
the Dust Colossus.

## Design

### Look & feel

Hot colours, cold air. Rust-red deserts, deep canyons of Valles Marineris, green terraforming
domes, atmosphere processors pumping plumes into a butterscotch sky. Vrell here burrow: the
ground itself heaves. Touchstones: *Total Recall* (1990), Red Faction, Tyrian's desert stages.

### Geography

The act crosses the planet west to east, then south (draft, 2026-10-12; real Martian names):

- **Olympus Mons and Lycus** in the west: the entry corridor comes down past the volcano's flank to
  the Lycus escarpment at its foot, then over the **Tharsis dunes** to the colony rail line.
- **Valles Marineris**: from the **Noctis** labyrinth at its western end east through Candor and
  the Coprates rapids to the **Eos** outflow, the one road east the Vrell do not hold from the air.
- **Ares Landing** (founded 2079, the first Mars colony): east of the canyon's end, on the edge of
  the **northern plains**, where the outflow channels open; the rail line from the west arrives
  from the north side.
- **Tharsis Foundry**: on the Tharsis plateau, back west, built with Helix Dynamics money.
- **Hellas basin**: far to the south, the vast impact basin where the Vrell came down and spread
  north from.

### Sub-locations

| Sub-location | Description |
|---|---|
| Ares Landing | The oldest colony: five named greenhouse domes (outer Bradbury and Lowell; inner ring Schiaparelli, Burroughs and Eden, the largest), outer greenhouses, militia gun pits, the ring road, rail lines, the spaceport. |
| Tharsis Foundry | Industrial colony on the Tharsis plateau: slag fields, roofless conveyor halls, furnace rows, the casting yard, the smelter tower; built with Helix Dynamics money. |
| Valles Marineris | Flying through the canyon: walls on both sides rising toward the camera, ledges and burnt cliff-side habitats on the faces, a mesa fork, rock arches, the narrows. |
| Terraforming fields | Atmosphere processors, ice-mining craters, pipelines (on the northern plains, half buried by the storm). |
| Helix sites | Gleaming black-and-gold Helix Dynamics sites, lit and intact, untouched by the Vrell (story hint): the **research station** on the Valles north rim and the **compound** at Ares Landing, drawn alike. Scenery, never a target. |
| Olympus Mons and Lycus | The volcano's caldera and flank seen from the entry corridor and through the cloud deck; Lycus Survey Station, overrun, at the foot of the escarpment. |
| Tharsis dunes | Open dune sea at the foot of Olympus Mons, a crashed colony lander, the colony rail line. |
| Northern plains | Rust flats with dry channels and a dry lake bed, crossed by dust-storm fronts. |
| Hellas basin | A vast impact basin in the south: the floor with the Vrell staging grounds, its terraces and rim road (the retreat), the inner wall, the dune sea and the **lime hollow** at its lowest point, where the Dust Colossus rises. |

### Sub-locations per level

Each level's backdrop is drawn from the chosen Mars scenes (r02-c the approach, r03-c the canyon;
*Chosen scene* below); its sections are in the level's *Layout*.

| Level | Sub-locations, in order | Signature |
|---|---|---|
| [L15 Olympus Descent](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md) | Entry corridor (Mars from high up) → the cloud deck → Olympus Mons' slopes → Lycus escarpment and Lycus Survey Station → Tharsis dunes and the colony rail line | The descent through two cloud decks; the first sand that heaves |
| [L16 Valles Canyon Run](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md) | Noctis edge → into the canyon → the fork round a mesa (the Candor relay hut) → the Coprates rapids, the Helix research station on the north rim → the narrows (180 px) → the Eos outflow | Canyon walls with collision |
| [L17 Dome Siege](../../campaign/act-3-red-dust/level-17-dome-siege/README.md) | Rail approach → outer domes Bradbury and Lowell → burnt outer greenhouses and the Helix compound → the inner ring (Schiaparelli, Burroughs, Eden) → the spaceport | Five named domes as allies |
| [L18 Dust Storm](../../campaign/act-3-red-dust/level-18-dust-storm/README.md) | Open northern plains → the storm front → deep storm (a militia rover wreck) → the eye (a dry lake bed) → the back wall → the clearing, Ares Landing on the horizon | Visibility cut to a radius round the ship |
| [L19 Foundry of Tharsis](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md) | Slag fields → conveyor halls → furnace row → yard approach → the casting yard (the walker's arena and wreck) → the south gate | Conveyors and furnace vents |
| [L20 Retreat from Hellas](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md) | Hellas floor and the Vrell staging grounds (the travelling ridge) → the basin floor again, in reverse → the terraces → an upper terrace (the lull) → the rim road → over the rim | Reverse scroll |
| [L21 Dust Colossus](../../campaign/act-3-red-dust/level-21-dust-colossus/README.md) | Hellas rim (the CDF line held after the retreat) → the inner wall → the dune sea (the ridge pass) → the lime hollow → the arena on the hollow's floor | The Colossus breaching from every side |

### Parallax layers

| Layer | Contents |
|---|---|
| deep | Mars from high up (L15's entry), canyon floor far below, dune seas, the Hellas floor from the rim, Olympus Mons silhouette. |
| ground | Domes, pipelines, rail, turrets, burrow holes, the canyon floor and the walls' foot, conveyors and vents, the Helix sites. |
| low-air | Dust devils, the thick cloud deck (L15), dust plumes in the canyon, heat shimmer over the foundry vents, rovers on bridges. |
| air | Player's plane. |
| high-air | Thin cirrus and dust streaks (at most 40 % over the field, never hiding a bullet), dust storm fronts, canyon arches passing over the player, the transport's underside at L15's launch. |

**Canyon walls** (L16, user decision MA.1 of round 36, 2026-10-13: camera model b): the canyon
floor is the ground layer at scale 1.0 and carries the ground units; the walls rise from it toward
the camera to the plateau at 1.25, leaning outward from the centre like Level 08's towers (the
amended rule in [art direction](../../art-direction/README.md#parallax-layer-model)). The foot
of a wall is the collision line; nothing gameplay touches stands on a leaning face or the plateau,
so the Tendril Anchors and every ground target sit at the foot or on the floor. The ship flies
inside the canyon.

### Palette & lighting

- Rust red, ochre and brown; butterscotch sky haze; small, pale sun from top-left.
- Domes glow green inside; Coalition structures dusty grey-white.
- Dust storms desaturate everything to orange-brown; Vrell teal glow cuts through the haze.
- The grey machines of Levels 18–19 are flat grey with no trim and red sensor eyes; the Helix sites
  are black and gold, the only gloss on the planet.
- The Hellas floor is paler ochre; the lime hollow glows lime from below (the Colossus).

### Hazards & set pieces

- **Canyon walls**: in Valles Marineris the walls narrow the flyable area to as little as 180 px;
  touching them costs armour and pushes the ship back (Level 16: 10 a touch, easy 6, 0.5 s
  cooldown). (Signature set piece.)
- **Burrowers**: the ground bulges before a Vrell burrower erupts — watch for the tell.
- **Dust storm**: the view shrinks to a radius round the ship (about 170 px) with a soft, ragged
  falloff over about 120 px and drifting dust tongues (look b, user decision MA.2 of round 36): the
  ground fades into dust, air units fade gradually to glowing silhouettes, bullets always show.
- **Dust devils**: rotating columns that pull the ship and the pickups toward their core.
- **Foundry**: conveyors carry ground units up the screen; furnace vents flare from the floor.
- **Hellas basin**: the travelling ridge of something under the sand (L20, L21) before the Dust
  Colossus breaches at the edges and under the ship.

### Natives

- Vrell: burrowers and sand-swimmers ([Burrower](../../enemies/ground/burrower/README.md)), ground
  artillery organisms (the [Shellback](../../enemies/ground/shellback/README.md)'s spore mortar),
  dust-camouflaged flyers (the [Dust Kite](../../enemies/air/dust-kite/README.md)), dust-vortex
  organisms ([Dust Devil](../../enemies/ground/dust-devil/README.md)), and the
  [Dust Colossus](../../enemies/bosses/dust-colossus/README.md) under the Hellas basin.
- Coalition: domes and processors to protect, militia defences and gun pits, Ares Control on the
  radio.
- Helix/Ascendancy: the untouched Helix sites — not hostile yet, but suspicious; the unmarked grey
  machines (Ghost Drone, Warden Tank, Revenant Walker) are not yet linked to them.

### Music mood

Mysterious and dry. Tribal percussion, detuned synths, a desert-wind drone; a lonely lead
melody. Track 8 "Red Dust Run" and track 9 (the Act 3 B theme, with Vorne's motif as a faint layer
under the grey machines); the per-level cues are in [Act 3](../../campaign/act-3-red-dust/README.md#music).

### Chosen scene

The look of this setting is set by the chosen parallax scene in art direction: Mars canyon (round 03) —
[sheet](../../art-direction/concept/parallax-r03-c.png), [scroll loop](../../art-direction/concept/parallax-r03-c.gif).
The concept files live in [art-direction](../../art-direction/README.md).

## Concept art

The chosen scene (round 03, in [art direction](../../art-direction/README.md#concept-art)) sets the
look:

| File | What | Status |
|---|---|---|
| [parallax-r03-c.png](../../art-direction/concept/parallax-r03-c.png) | Mars canyon parallax with decoration: dust-storm banks on low-air, dust veil and streaks on high-air, greenhouse tunnels, algae ponds and lichen fields round the colony (sheet) | chosen |
| [parallax-r03-c.gif](../../art-direction/concept/parallax-r03-c.gif) | The same scene: scroll loop | chosen |

Concept [round 36](../../concept-rounds/round-36/README.md) (Act 3 "Red Dust", overnight design
phase 2026-10-12) — one mood still per sub-location (direction, not production backdrops), a/b for
the canyon walls, the storm radius and the dome; generator `tools/concept/mars_r36.py`, prompts
in [concept/prompts.md](concept/prompts.md). The units on the stills are the round-36 proposals,
variant a, for scale; the Ghost Drone's red eye and white running lights are painted onto the
storm stills only, its own sprite (a) has neither (the fix U.5, accepted, is made in the chosen
variant). The stills are accepted as direction; the Valles still shows camera model a and the
storm-plains still radius look a, both since replaced by the b picks.

| File | What | Status |
|---|---|---|
| [concept/mars-olympus-r36-a.png](concept/mars-olympus-r36-a.png) | Olympus descent (L15): the volcano's flank and caldera far below, two cloud decks between; still and layer breakdown | chosen (round 36, MA.4: as direction) |
| [concept/mars-valles-r36-a.png](concept/mars-valles-r36-a.png) | Valles Marineris (L16): the canyon in perspective (camera model a), the Helix research station on the north rim | chosen (round 36, MA.4: as direction) |
| [concept/mars-ares-landing-r36-a.png](concept/mars-ares-landing-r36-a.png) | Ares Landing (L17): outer domes, rail line, ring road, burnt greenhouses, a gun pit, Vrell growth and the untouched Helix compound; Spore Bombers hooked on Lowell, a Choir Herald with Needlers | chosen (round 36, MA.4: as direction) |
| [concept/mars-storm-plains-r36-a.png](concept/mars-storm-plains-r36-a.png) | Storm plains (L18): the northern plains in the dust storm with radius look a, a rover wreck, a pipeline, a dust devil; still, layers and the look's rules | chosen (round 36, MA.4: as direction) |
| [concept/mars-tharsis-foundry-r36-a.png](concept/mars-tharsis-foundry-r36-a.png) | Tharsis Foundry (L19): slag fields, roofless conveyor halls with grey tanks, furnace row, floor vents, the smelter tower | chosen (round 36, MA.4: as direction) |
| [concept/mars-hellas-r36-a.png](concept/mars-hellas-r36-a.png) | Hellas basin (L20/L21): terraces, the dune sea, the travelling ridge, the lime hollow | chosen (round 36, MA.4: as direction) |
| [concept/mars-panorama-r36-a.png](concept/mars-panorama-r36-a.png) | A wide 960×540 mood still of the act (voxel-terrain look): domes, the canyon, Olympus Mons, a storm front; for the title card and briefing horizons (the weakest still: blocky domes) | chosen (round 36, MA.5: as direction) |
| [concept/rejected/canyon-walls-r36-a.png](concept/rejected/canyon-walls-r36-a.png) | Canyon walls A: the rim at scale 1.0 is the collision line, the faces drop to the floor at 0.6 (the decided art-direction rule); frame, collision overlay, frame 1 s later, side view (sheet) | rejected — round 36: b (walls rise toward the camera) picked |
| [concept/rejected/canyon-walls-r36-a.gif](concept/rejected/canyon-walls-r36-a.gif) | Canyon walls A at 160 px/s (motion) | rejected — round 36: b (walls rise toward the camera) picked |
| [concept/canyon-walls-r36-b.png](concept/canyon-walls-r36-b.png) | Canyon walls B: the wall's foot is the collision line, the walls rise toward the camera to 1.25 like Level 08's towers (amends the art-direction rule) (sheet) | chosen (round 36, MA.1: amends the art-direction perspective rule) |
| [concept/canyon-walls-r36-b.gif](concept/canyon-walls-r36-b.gif) | Canyon walls B (motion) | chosen (round 36, MA.1: amends the art-direction perspective rule) |
| [concept/rejected/storm-radius-r36-a.png](concept/rejected/storm-radius-r36-a.png) | Storm radius A: a clear 170 px bubble with a hard 10 px edge; outside a banded rust veil, air units as silhouettes with their glow, bullets always drawn (sheet) | rejected — round 36: b (soft ragged falloff) picked |
| [concept/rejected/storm-radius-r36-a.gif](concept/rejected/storm-radius-r36-a.gif) | Storm radius A at 120 px/s (motion) | rejected — round 36: b (soft ragged falloff) picked |
| [concept/storm-radius-r36-b.png](concept/storm-radius-r36-b.png) | Storm radius B: a soft ragged falloff over about 120 px with drifting dust tongues; units fade gradually to silhouettes (sheet) | chosen (round 36, MA.2) |
| [concept/storm-radius-r36-b.gif](concept/storm-radius-r36-b.gif) | Storm radius B (motion) | chosen (round 36, MA.2) |
| [concept/dome-r36-a.png](concept/dome-r36-a.png) | Ares Landing dome A: geodesic glass on a concrete ring, 120 px (Eden 144), hit and broken states | chosen (round 36, MA.3) |
| [concept/rejected/dome-r36-b.png](concept/rejected/dome-r36-b.png) | Ares Landing dome B: ribbed composite pressure dome on a regolith berm, hit and broken states | rejected — round 36: a (geodesic glass) picked |

## Open questions

- None open: round 36 answered both (see *Decisions*).

## Decisions

- 2026-09-30: Mars as Act 3; signature: Valles Marineris canyon walls.
- 2026-09-30: Tharsis Foundry and Hellas basin added as sub-locations; the Dust Colossus fight is in the Hellas basin (L21), matching the campaign.
- 2026-10-01: Linked the chosen scene from art direction; `art: chosen`.
- 2026-10-12 (overnight design phase): sub-locations per level from the seven draft level
  documents; a *Geography* section; the Helix compound widened to the Helix sites (the Valles rim
  station of L16 and the Ares Landing compound of L17, drawn alike); Ares Landing's five named domes;
  Olympus Mons and Lycus, and the northern plains, added as sub-locations; the Dust Devils pull
  instead of push (their spec); the dust storm's radius, the foundry's conveyors and vents and the
  Hellas ridge added to the hazards; the natives linked to their units. All pending the user's
  review in concept round 36.
- 2026-10-12 (overnight design phase): concept round 36 proposes the mood stills, the canyon
  walls, the storm radius and the dome (a/b), pending the user's choice; `art` stays `chosen` (the
  round-03 scene sets the look).
- 2026-10-13 (user, round 36): the document approved (D.27) with the geography as drafted (Q.24h).
  MA.1 = b: the canyon walls rise toward the camera from the floor (1.0) to the plateau (1.25), the
  collision at their foot; it amends the art-direction perspective rule; a (the rim as the collision
  line) rejected. MA.2 = b: the storm radius has a soft ragged falloff; a (a hard 10 px edge)
  rejected. MA.3 = a: the geodesic glass dome; b rejected. MA.4 and MA.5: the six mood stills and the
  panorama accepted as direction (not production backdrops). The rejected files are in
  `concept/rejected/`.
