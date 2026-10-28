---
title: The belt
design: approved
implementation: n/a
art: chosen
depends-on: [../../campaign/act-5-the-belt, ../../enemies, ../../player/specials]
updated: 2026-10-28
---

# The belt

## Summary

The asteroid belt: drifting rock fields and hundreds of hollowed-out mining stations, many
chartered to Helix Dynamics. Setting for Act 5, where the Ascendancy fights openly alongside
the Vrell and the stations must choose sides. Ends with the Iron Sovereign battle station.
Act 5's per-level use, the vacuum rules and the hazards are a draft of 2026-10-27, used as approved
by the Level 29–35 documents in round 60 (this README itself awaits the user's sign-off).

## Design

### Look & feel

Frontier and industrial. Tumbling asteroids, some carved into stations with lit windows and
docking arms; ore haulers, conveyor tubes, refineries glowing orange. Split loyalties show in
the architecture: Coalition grey-white stations next to sleek black-and-gold Helix facilities.
Touchstones: *Outland* (1981), the belt in *The Expanse*, Descent's mines.

### Sub-locations

| Sub-location | Description |
|---|---|
| Open rock field | Drifting asteroids of all sizes; ambushes from behind rocks. |
| Ceres Hub | The biggest belt station: a hollowed dwarf planet with docks; its dock canyon cut into the crust. |
| Shipping lane | The marked lane between Ceres and the outer stations: beacon buoys, open space, no surface below. |
| Mining station interiors | Flying through the tunnels of a hollowed asteroid (narrow, walls, forks). |
| Refinery | Molten ore, furnaces, conveyor lines; heat and fire hazards. |
| Vrell-infested field | Big rocks overgrown with Vrell growth, where the leeches nest. |
| Ship graveyard | Derelict hulls of UTC and Helix ships, drifting together after old battles: salvage. |
| Helix yards | Ascendancy shipyards hidden in the rocks; the Iron Sovereign in Ceres orbit. |

### Sub-locations per level

Act 5's seven levels in order ([the act](../../campaign/act-5-the-belt/README.md)); the level documents
own the sections and the timings, these are their settings (draft).

| Level | Sub-location | What the level shows of it | Ground layer |
|---|---|---|---|
| 29 Betrayal at Ceres | Ceres Hub | The dock canyon's walls and station hulls; loyalist and mutineer ships fighting on the background layers; at ≈ t=60 a station block **turns sides** (its lights switch white → red, its dormant turrets wake); at ≈ t=120 Dock 9 and the prison tender (Rook's rescue, [wingmen](../../player/wingmen/README.md#rooks-rescue-and-return-l29l30)) | station hull |
| 30 Minefield | Shipping lane | Open space and the lane's beacon buoys; drifting destructible rocks; the Minelayers' lines; a Gunship convoy | **none** (the act's only space-only level: the Bomb Rack reads `NO GROUND`) |
| 31 Mining Rig Escort | Open rock field | The huge mining tug crawling up the field; rocks the Buzzsaws ricochet off; refugee haulers on the background layers | the tug's hull |
| 32 Deep Core | Mining station interiors, refinery | A hollowed asteroid's tunnels with two forks (a route choice), side galleries, blast doors and mining charges; the refinery's furnace glow as the last section; rock overhead (no Airstrike or Orbital Lance) | tunnel floors |
| 33 Leech Field | Vrell-infested field | Large asteroid surfaces with Rail Bunkers dug in; Vrell growth on the rocks; leeches from behind and the sides | asteroid surfaces |
| 34 Salvage Run | Ship graveyard | Derelict hulls with Warden Tanks in Ascendancy livery; many caches and secrets; mining charges on the wrecks; the crate of Helix tech (the Mk V "Helix" generator) | derelict hulls |
| 35 Iron Sovereign | Helix yards, Ceres orbit | The approach (the Sovereign's escort screen, the mass-relay flash once, Rail Bunkers on its outer hull), then the arena over the station's turning rings | the station's hull and rings |

### Parallax layers

| Layer | Contents |
|---|---|
| deep | Distant asteroids, the Sun as a bright point, faint dust band; Ceres's curve in L29 and L35. |
| ground | Large asteroid surfaces and station hulls below the player: turrets, docks, bunkers. None in L30. |
| low-air | Smaller rocks drifting below, haulers, conveyor tubes. |
| air | Player's plane; drifting asteroids that block and can be destroyed. In the belt the enemies' rosters write this layer `space` (below). |
| high-air | Close rock fragments and dust passing over the player. |

### Vacuum rules

*Draft, used as approved by the Level 29–35 documents (round 60; the gap check's trivia, decided: no new layer).*

- **`space` is `air`.** A unit written as `space` in a roster flies on the player's plane like an `air`
  unit; there is no `space` layer in the game, only a level's **vacuum presentation**. Every weapon hits
  it as it hits `air` ([layer rules](../../enemies/README.md#layer-rules)); the docs keep writing `space`
  in rosters.
- **No weather and no water:** no clouds, wind or rain; the hydro-kit is stowed and the pressure hull
  does nothing ([ship systems](../../player/systems/README.md)); the Sonar Pulse reads `NO WATER` all act.
- **Handling and bullets** are as in air: no drag, the level's normal bullet speed. Sounds play as
  everywhere (the game's convention: no silent vacuum).
- **Explosions** flash short and throw debris outward in a ring instead of rising smoke; a wreck drifts
  off with the scroll and slowly turns rather than falling. Fire is a brief flare only where a hull holds
  air (station interiors, the refinery).
- **Specials:** the Airstrike and the Orbital Lance work everywhere in the belt except **inside the
  hollowed asteroid of L32** (rock overhead, reason word `IN ROCK`); see
  [specials](../../player/specials/README.md#act-5-specials-draft).

### Palette & lighting

- Dark brown-grey rock, harsh distant sunlight from top-left, very high contrast.
- Orange refinery light; cool white station lights.
- Ascendancy structures **gunmetal/charcoal** with gold highlights and red lights (round 60, Q11 b; the L28 cruisers stay gloss black) — new to the palette.
- **Ascendancy readability** (Q11 = **b**, decided in round 60, not the recommendation): black-and-gold units over dark
  rock and space read poorly, so every Ascendancy unit is **gunmetal or charcoal with gold trim on at least 20 %
  of its silhouette**, a strong **top-left rim light** (the belt's sun) and **red running lights**; the
  belt's backdrops are kept **a step lighter than Act 4's** (a sunlit dust band in the deep layer, warm
  sunlit rock faces), so a dark hull always has a lighter ground behind it. One readability check per
  sprite sheet (the unit at play size over the darkest backdrop of its level). It diverges on purpose from the L28
  cruisers (final, gloss black); the Act 5 units' chosen variants are re-rendered in gunmetal/charcoal in their parts'
  production art.
- The Ascendancy's homing missiles are the art direction's **homing diamond** in red-gold with a short
  thin exhaust ([art direction](../../art-direction/README.md)).

### Hazards & set pieces

*Draft, used as approved by the Level 29–35 documents (round 60); the levels place them.*

| Hazard / set piece | Levels | Rules (draft) |
|---|---|---|
| **Drifting asteroids** | L29–L31, L33–L35 | On the `air` (`space`) layer, rocks drift across the field. **Small** rocks break into harmless fragments when shot (a few hits); **big** ones are indestructible and must be avoided (contact damage as a `large` unit's), and enemies hide behind them. Bullets of both sides stop on them. The Buzzsaw Drone ricochets off them; some units ride on them (L29–L31: their unit documents). |
| **Mining charges** | L32, L34 | Shootable charges fixed to rocks and wrecks (a ground object, 1 hit): a **64 px burst** that hurts enemies and ground units, **never the ship**. A tool for the player (shoot one as a wave passes). |
| **The turning station** | L29 (signature) | At ≈ t=60 a Ceres station block declares for the Ascendancy: its lights switch from white to red over 1 s and its turrets, scrolling until then as inert hull, **wake** and open fire (a ground unit that is dormant until a time). |
| **Mass-relay flash** | L35 (approach, once) | An Ascendancy rail cannon fires a `laser-line` across the whole screen: a thin red telegraph line for **1.2 s** (the `Sled` attack's telegraph), then the flash along it. The line is drawn where the ship is when the telegraph starts; moving off it is the answer. |
| **Tunnels and forks** | L32 | Inside a hollowed asteroid the play field narrows with walls; a long rock island splits the tunnel twice, and the side the ship is on at a fork's head picks the branch (its own waves; one safer, one richer); blast doors open side pockets with caches. L32's level document owns the details. |
| **Furnace vents** | L32 (refinery) | The refinery's furnace glow and vents, as the foundry's of [Level 19](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md). |

### Natives

- Ascendancy: precision fighters (Talons, in V-wings led by a gold-striped leader), shielded gunships,
  missile boats, drone trains, rail turrets and bunkers, walker mechs; units in the
  [air](../../enemies/air/README.md), [space](../../enemies/space/README.md) and
  [ground](../../enemies/ground/README.md) rosters.
- Vrell: swarms working with the Ascendancy, Asteroid Mites in the rock, Void Leeches that drain the
  generator ([generator](../../player/generator/README.md#enemy-drain-effects)).
- Coalition: loyal stations, miners' militia, the mining tug of L31 (allied, on background layers or
  as an [ally](../../allies/README.md)).

### Music mood

Industrial and aggressive. Metallic percussion, distorted bass, the Ascendancy leitmotif
(brass, stately and cold) introduced here.

### Chosen scene

The look of this setting is set by the chosen parallax scene in art direction: Asteroid belt (round 06) —
[sheet](../../art-direction/concept/scene-belt-r06-a.png), [scroll loop](../../art-direction/concept/scene-belt-r06-a.gif).
The concept files live in [art-direction](../../art-direction/README.md).

## Concept art

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

The chosen scene (round 06, in [art direction](../../art-direction/README.md#concept-art)) sets the belt's look; see
*Chosen scene* above.

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-world; chosen in round 60: the mood ladder a, the readability frame b) — one mood still per
level, L29–L35 (direction, not production backdrops), in two grades, and the Q11 readability frame: three Ascendancy
units at play size over the darkest window of every level's still in both grades, in Q11's looks a, b and c, with
the measured gold share. Backdrops and units are `tools/concept/enemies_r60.py`'s (the units' own sheets are in
their documents, the station in the [Iron Sovereign](../../enemies/bosses/iron-sovereign/README.md#concept-art), the
tug in the [mining tug](../../allies/mining-tug/README.md#concept-art)). Generator `tools/concept/belt_r60.py`; prompts:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/belt-moods-r60-a.png](concept/belt-moods-r60-a.png) | Mood ladder A, a step lighter (the backdrops' grade, A.18 a): lit Ceres Hub with a block turned red, the shipping lane's dust band and buoys, the rock field with the tug, the tunnels and the refinery's glow, the leech field, the graveyard, Ceres orbit under the station's rim; each still's mean luminance and darkest window | chosen (round 60) |
| [concept/belt-readability-r60-b.png](concept/belt-readability-r60-b.png) | Q11 frame B — gunmetal hulls with the same gold, a softer rim (the chosen look) | chosen (round 60) |

## Open questions

- Q11 of [round 60](../../concept-rounds/README.md) (the Ascendancy look on the belt): decided, **b** (see above).
- J3 of round 60: L33 has asteroid surfaces on the ground layer (for the Rail Bunkers), so L30 is the
  only space-only level.

## Decisions

- 2026-09-30: The belt as Act 5; signature: a station turning sides mid-level.
- 2026-10-01: Linked the chosen scene from art direction; `art: chosen`.
- 2026-10-27 (Act 5 design phase, step m9-d-systems; used as approved by the Level 29–35 documents in round 60): the
  sub-locations per level (three new: the shipping lane, the Vrell-infested field, the ship graveyard);
  the vacuum rules (`space` = `air`, no new layer; no weather; specials forbidden only inside L32's
  asteroid); the hazards with draft numbers (small rocks break, big ones are walls; mining charges burst
  64 px and never hurt the ship; the turning station at ≈ t=60 of L29; the mass-relay flash once in L35's
  approach with a 1.2 s telegraph); ground in every level but L30 (J3); the Ascendancy readability rules
  (Q11 = a at the time of drafting; changed to b in round 60, below).
- 2026-10-27 (user, [round 60](../../concept-rounds/round-60/README.md) closed): **Q11 b** — the Ascendancy look is
  gunmetal/charcoal hulls with gold (not the recommended gloss black; the ≥ 20 % gold, rim light and red lights stay,
  and the L28 cruisers diverge on purpose); the readability frame b is the record. **A.18 a** — the belt's backdrops are
  a step lighter than Act 4's (the lighter grade; b, Act 4's harsh grade, rejected; the darkest windows of Levels 30, 31
  and 35 are re-measured in the backdrop step). This document is not among the round's D rows, so it stays `draft` until
  the user signs it off.
- 2026-10-28 (M9 part A, step A4): set to `review` for [round 61](../../concept-rounds/round-61/README.md) (D.1), where the
  user signs it off.
- 2026-10-28 (user, [round 61](../../concept-rounds/round-61/README.md), closed): **D.1** the belt's world document approved (`design: approved`).
