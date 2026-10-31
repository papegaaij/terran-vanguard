---
title: Jovian system
design: draft
implementation: n/a
art: chosen
depends-on: [../../campaign/act-6-jovian-storm, ../../enemies, ../../player/specials, ../belt]
updated: 2026-10-31
---

# Jovian system

## Summary

Jupiter: an endless sky of storm bands with gilded cloud cities floating in it, Io's refineries, and Callisto,
where the Ascendancy has its fortress HQ. Setting for Act 6, the hunt for Vorne, ending with his flagship, the
Ascendant. Act 6's per-level use, the media, the hazards and the moods per level are a draft of 2026-10-31, decided 2026-10-31 (round 72).

## Design

### Look & feel

Opulent and violent. Towering cloud banks in cream, ochre and rust; lightning; the Great Red Spot on the horizon.
Cloud cities are gilded domes and spires hanging from huge balloons and anti-grav rings. Callisto is dark, cratered
ice with a black fortress city carved into it. Mid-act, the Vrell turn on the Ascendancy: the gold cities burn.
Touchstones: Bespin in *The Empire Strikes Back*, Art Deco, Einhänder's opulent stages.

### Sub-locations

| Sub-location | Description |
|---|---|
| Upper atmosphere | Open storm bands, lightning, helium-3 harvesters skimming the clouds. |
| Aurelia | The first and grandest cloud city; site of the 2166 riots; the city that falls (L37). |
| The storm layer | Below the cloud cities: storm cells and lightning columns round the Great Red Spot's rim; the only covered route to Io. |
| The Great Red Spot | A storm eye with violent winds and electric discharges (seen from the storm layer, not flown into in Act 6). |
| Io refineries | Volcanic moon: Ascendancy fuel and sulphur refineries, eruptions that throw debris into the sky. |
| Callisto approach | Orbital defences, Ascendancy fleet, the ice moon below. |
| Callisto HQ | The black citadel: hangars, rail cannons, shield pylons, Vorne's flagship docked. |
| Callisto orbit | Both fleets above the ice moon; the Ascendant's arena. |

### Sub-locations per level

Act 6's seven levels in order ([the act](../../campaign/act-6-jovian-storm/README.md)); the level documents own the
sections and the timings, these are their settings (draft, decided 2026-10-31 (round 72)).

| Level | Sub-location | What the level shows of it | Ground layer |
|---|---|---|---|
| 36 Cloud Divers | Upper atmosphere | The dive from orbit: the first section above the banks (Jupiter's curve, the CDF carrier group on the background layers, marked friendly where it reaches the play plane), then down among the storm bands; helium-3 harvesters skimming on `low-air`; the **foreground banks** (Q2) | **none** (cloud decks are scenery: `NO GROUND`) |
| 37 Fall of Aurelia | Aurelia | Decks, domes, balloons and anti-grav rings; the evacuation shuttles leaving (friendly marker); at ≈ t=100 **the turn**: Vrell growth bursts over the decks and the gold burns (Q3's backdrop); the last ≈ 40 s **the falling city** (Q4) | Aurelia's decks (until the fall) |
| 38 Lightning Belt | The storm layer | Storm-dark cloud walls, storm cells as backdrop only, **lightning strikes**, the Great Red Spot's rim as a rust glow far off; gusts | **none** (`NO GROUND`) |
| 39 Io Flyby | Io refineries | Sulphur plains and black lava, refinery lines and pipeline racks, **eruption** vents, umbrella plumes on the horizon, Jupiter huge in the sky | Io's surface and the refineries |
| 40 Honour Guard | Callisto approach | Black space, Callisto's dark disc below, Jupiter's limb at an edge, the CDF fleet's lights on the background layers | **none** (`NO GROUND`) |
| 41 Callisto Fortress | Callisto HQ | The black citadel cut into blue-grey ice: causeways, hangars, the **pylon gates**, the [Halo Platform](../../enemies/ground/halo-platform/README.md) at the centre, Helix HQ's data core | the citadel's decks and the ice |
| 42 Ascendant | Callisto orbit | Both fleets above the ice moon (the L28 cruisers in gloss black as the home fleet), then the Ascendant's arena and the lifeboat's escape toward the gate | **none** (`NO GROUND`: the cruisers are invulnerable set pieces on `low-air`, the Level 42 draft's call) |

### Parallax layers

| Layer | Contents |
|---|---|
| deep | Deeper cloud bands scrolling slowly, lightning deep below, Jupiter's curve at high altitude; at Io and Callisto, Jupiter's banded disc or limb, the other moons as points. |
| ground | Cloud-city decks and domes, harvesters, turrets (Io: the sulphur plains and the refineries; Callisto: the ice surface and the fortress). |
| low-air | Balloons, anti-grav rings, cables, lower cloud wisps; the harvesters (L36); the falling city's debris (L37). |
| air | Player's plane (`space` in vacuum, as in [the belt](../belt/README.md#vacuum-rules)). |
| high-air | Thick clouds and storm fronts passing over the player; lightning flashes; in L36 the **foreground banks** drawn over `air` bodies (Q2, below). |

### Media per level

*Draft, judgement call **J2**, decided 2026-10-31 (round 72).*

| Levels | Medium | Rules |
|---|---|---|
| 36–38 | Jupiter's **air** | Weather allowed: wind gusts, lightning, storm haze; no rain. Explosions rise as smoke and fire as on Earth. |
| 39 | Io's **near-vacuum** | The belt's [vacuum rules](../belt/README.md#vacuum-rules): explosions flash short and throw debris in a ring, wrecks do not burn; plumes rise as umbrellas and fall back; dust settles fast (no rising clouds); no weather. |
| 40–42 | **vacuum** (Callisto and its orbit) | The vacuum rules as in the belt; `space` is `air`. |

- **Specials:** the Airstrike flies everywhere in Act 6 (CDF bombers from Okafor's carrier, in Jupiter's air and over
  Io and Callisto; no `IN ROCK` anywhere); the Orbital Lance has cover everywhere (the fleet is at Jupiter); the
  Harpoon, the Depth Charges and the Sonar Pulse read `NO WATER` all act. See
  [specials](../../player/specials/README.md).
- **Ground-only weapons** (the Bomb Rack and every bomb delivery) read **`NO GROUND`** in L36, L38, L40 and L42, the four
  levels without a ground layer (Level 30's rule; the hangar warns).
- **Sounds** play as everywhere (the game's convention: no silent vacuum).

### Palette & lighting

- Cream, ochre, rust and brown storm bands; sunlight diffused and warm.
- Cloud cities gold and white with black accents; red Ascendancy lights.
- Lightning flashes briefly light the whole scene blue-white.
- Callisto: dark blue-grey ice, the black citadel, red and gold lights.
- Burning cities (after the Vrell turn): fire orange and Vrell teal together.
- **Smooth haze** (the Act 5 lesson, the 2026-10-30 dither audit): every cloud bank, glow, haze and shadow layer is a
  smooth gradient of **≥ 32 steps**, no ordered dither, drawn once (not stacked). The art direction's allowance for
  ordered dither on wide background gradients does not apply to Act 6's haze and glow layers.

#### Moods per level (Q10)

*Draft, Q10 **a** (recommended, decided 2026-10-31 (round 72)): a grade per level, each with a readability
check (the ship, Rook, the Ascendancy's gold units and amber/red bullets laid over the level's brightest and darkest
band; step ART-R-B's mood sheets).*

| Level | Grade | Readability note |
|---|---|---|
| 36 | High sunlit bands: cream, ochre and rust under a pale haze, warm sun from the top-left | The brightest cream band is the risk: enemy bullets keep their dark outline ([bullet rules](../../enemies/README.md#bullet-readability-rules)), a little heavier over cream; gold units read by their dark hulls |
| 37 | Aurelia's gold and white over warm cream cloud; after the turn the sky darkens a step over 4 s, the decks burn fire-orange with smoke, Vrell teal growth spreads | Gold-on-gold: the decks are kept a step paler and less saturated than the units' gold; after the turn, teal bullets over teal growth get the larger, brighter Vrell bullets (the Europa rule) |
| 38 | Storm-dark blue-grey and slate, purple-grey cloud walls, the lightning's blue-white | The darkest act grade: the ship's and Rook's running lights and the bullets' cores carry the read; the lightning column's telegraph is blue-white, never the red of an enemy `laser-line` |
| 39 | Io: black lava, desaturated sulphur yellow and ochre plains, white frost, orange vent glow; harsh small Sun, Jupiter's disc huge and low | Yellow sulphur against gold units and amber bullets: the plains are desaturated, the bullets' outline does the rest; the vent tells glow orange-white |
| 40 | Black space, Callisto's dark blue-grey disc below, Jupiter's cream-orange limb at an edge, the fleet's lights | Gloss-black aces over black space: their heavy gold and rim light (Q9 a) and Jupiter's limb behind the arena |
| 41 | Dark blue-grey ice, the black citadel, red and gold lights; the Sun low, long shadows | The fortress structures' gold and the domes' gold hexes over dark ice; the citadel's decks a step lighter than the units |
| 42 | Callisto orbit: black space, Jupiter's limb and Callisto's curve, both fleets' running lights; the Ascendant's gold, then Vrell teal in its phase 2 | The boss's gold and teal over black; the friendly fleet marked |

### Hazards & set pieces

*Draft, decided 2026-10-31 (round 72); the levels place them.*

| Hazard / set piece | Levels | Rules (draft) |
|---|---|---|
| **Wind shear** | L36, L38 | [Level 12](../../campaign/act-2-homefront/level-12-storm-front/README.md)'s gusts as built: a telegraphed sideways push that adds to positions (the ship, Rook and the wind-borne units), never to bullets, mines or pickups (bullets stay readable; the sketch's "slow or speed up bullets" is dropped). Two short gust spells a level. |
| **Lightning strikes** (J3) | L38; a few allowed in L36 | A sky flash (Level 12's look, ≤ 0.1 s, softened by the flash-reduction [option](../../ui/options/README.md)) marks a vertical **column 40 px wide**: a thin blue-white line down its middle and faint edges, brightening for **1.0 s** (the ≥ 0.8 s telegraph rule). Then the **strike**, 0.25 s, down the whole play field: `heavy` **10** to the ship and Rook, once per strike, and **60** to every enemy on `air` in the column (a Chimera or a Harrow dies, a medium unit survives); `low-air`, `high-air`, bullets and pickups untouched. Kills by lightning **pay their bounty** and count for kill ratios (the player lured them; a Harrow bursts as on any death). Authored times and x in the level data; never two strikes within 1.5 s; no telegraph starts on the ship's x within 1 s of a wave's entry. About one strike every 5–7 s in the storm sections (L38 decides). Easy: fewer (≈ 1 every 9 s), never within 60 px of the ship's x at the telegraph's start; hard: plus **tracking** columns that lock on the ship's x at their telegraph's start. Thunder from Level 12's set keyed to the strike, with a close crack (D-audio). Rook keeps out of a column's telegraph (Level 27's hazard AI). |
| **Eruptions** (J3) | L39 | A ground vent's mouth glows orange-white and the ground cracks round it for **1.2 s** (the tell; a rising rumble), then it throws a column of **3–5 burning rocks**: they rise for 0.5 s over the vent (growing, harmless, a shadow on the ground), then fly out on the play plane in a fan up the screen (100° wide, centred straight up), 120 px/s easing to 60 px/s, for 2.5 s, and fall back (shrinking 0.4 s, harmless). A rock is shootable debris: `small`, **6 HP** (no act or beam-era factor), it hurts the ship and Rook on contact (`small` 10, destroyed by the impact), pays nothing and is no kill; enemy bullets pass through it and it never hurts enemies. Over the vent an umbrella of sulphur haze opens and settles (looks only, smooth). A vent erupts every 5–7 s while on the screen; the level's first eruption plays alone before any wave reaches it (Level 27's teaching rule). Easy: tell 1.6 s, 3 rocks; hard: tell 1.0 s, 5 rocks at 150 px/s. Rook keeps out of a tell's reach. Level 27's plume rows and Act 5's breakable debris are the parts. |
| **Foreground cloud banks** (Q2) | L36 (L38 may use them) | Banks on the `high-air` scenery drawn **over `air` bodies**, translucent: a body under a bank shows at ≈ **35 %**, its running lights, engine glow and attack tells at full. **Bullets, the ship, Rook and pickups are always drawn above** the banks (the [bullet rules](../../enemies/README.md#bullet-readability-rules)); homing shots, Rook and the Targeting computer track as usual (it rewards L36's `homing`). A lightning flash makes a bank see-through for its 0.5 s (Level 12's). At most a third of the field's area under banks at a time, and never a bank resting over the ship's band (the bottom 140 px) for more than 2 s. Smooth (≥ 32 steps, no ordered dither, drawn once). **The look (A.12 a, round 72): a cream veil**: the bank is drawn in the band's own lit cream and the bodies under it fade toward cream. **Clouds at different altitudes do not have different colours** (the user, 2026-10-31): the banks, the deep layer's cloud heads and the cloud wisps share one colour, and telling a bank from the deep layer by colour is not a goal. |
| **The falling city** (Q4, signature set piece) | L37's last ≈ 40 s | After the turn Aurelia loses its balloons (they tear and collapse, no pop) and **breaks up** (A.13 b, round 72): the main deck breaks along a fault line, an authored, **jagged** cut (torn plating and a ragged edge, never a straight line), the two halves drift apart and sink at their own rates, and the spires topple. Over ≈ **20 s** each half, a ground layer of its own (**two city layers**), slows, darkens and shrinks (scaled toward the centre) while the ship flies down beside the spires through `low-air` debris and Vrell; the level ends over open cloud. Ground units still on the falling decks sink with it: they stop firing as the fall starts and leave play unpaid (the level places none on the decks in its last 20 s). A renderer set piece with two scaled ground layers and an authored cut line, and a production backdrop part (gap analysis §2.10: large, now two layers); ≥ 4 s of calm after the last enemy. Okafor's Aurelia line plays over it (L37 decides). |
| **The three-way battle** | L37 on | From the turn the Vrell attack the Ascendancy as well as the player: the [three-way rule](../../enemies/README.md#the-three-way-war) (Q3 **a**, the enemies README's) in play, and **c**'s backdrop on top: Vrell growth eating Aurelia's decks, Vrell swarms and Ascendancy craft fighting on the `deep` and `low-air` layers, burning hulks falling past. |
| **Pylon gates** | L41 | The fortress's sections halt until their [Shield Pylons](../../enemies/ground/shield-pylon/README.md#the-pylon-gates-level-41) fall (the pylon's document owns the rule). |

The numbers the game reads (M10 part A) are in [data.yaml](data.yaml), once for the world; a level places and times
them (its `weather.strikes`, `eruptions` and backdrop `banks`, the
[level schema](../../tech/architecture/schemas-levels/README.md)):

<!-- data: jovian-hazards -->
| Rule | Number |
|---|---|
| Lightning strike | told 1 s, strikes 0.25 s down a column 40 px wide: 10 to the ship and Rook (once), 60 to every `air` enemy in it |
| Strike spacing | never two within 1.5 s; no tell within 1 s of a wave's entry on the ship's x; easy: never within 60 px of the ship's x |
| Eruption | tell 1.2 s, rise 0.5 s, flight 2.5 s in a 100° fan up the screen at 120 → 60 px/s, fall 0.4 s; every 5–7 s while on the screen, 3–5 rocks |
| Rock | 24×24 px, 6 HP (no factor), 10 on contact; pays nothing |
| Eruption, easy / hard | easy: tell 1.6 s, 3 rocks; hard: tell 1 s, 5 rocks at 150 px/s |
| Foreground banks | at most a third of the field under banks (easy a quarter); never over the ship's band (the bottom 140 px) longer than 2 s |

Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`.
<!-- /data -->

### Natives

- Ascendancy: the full fleet: precision fighters, shielded gunships, rail platforms, the fortress's pylons and the
  [Halo Platform](../../enemies/ground/halo-platform/README.md), and the Vrell **hybrids** (gunmetal with teal tissue
  seams, Q9 a: the [Graft Turret](../../enemies/ground/graft-turret/README.md) and the act's air hybrids); the elite in
  gloss black and heavy gold. Units in the [air](../../enemies/air/README.md) and
  [ground](../../enemies/ground/README.md) rosters.
- Vrell: storm-riding swarms, gas-bag organisms; turning on the Ascendancy at Aurelia (L37).
- Coalition: the assault fleet under Okafor's carrier (allied, background layers, marked friendly where it nears the
  play plane; the [HUD](../../ui/hud/README.md)'s friendly marker); Aurelia's evacuation shuttles (L37, the
  [allies](../../allies/README.md)' Level 10 shuttles with the marker).

### Music mood

Grand and furious. The Ascendancy leitmotif at full orchestra-synth strength; the main theme fights back. Most
bombastic act. (Per-level cues: the [music](../../audio/music/README.md) README.)

### Chosen scene

The look of this setting is set by the chosen parallax scene in art direction: Jupiter cloud city Aurelia (round 06) —
[sheet](../../art-direction/concept/scene-jovian-r06-a.png), [scroll loop](../../art-direction/concept/scene-jovian-r06-a.gif).
The concept files live in [art-direction](../../art-direction/README.md).

## Concept art

The chosen scene (round 06, in [art direction](../../art-direction/README.md#concept-art)) sets the look; see *Chosen
scene* above.

Concept round 72 (Act 6 design phase, step ART-R-B; **decided 2026-10-31 (round 72)**): Q10's moods per level
with the readability windows, Q2's foreground banks over Level 36's brightest band, and Q4's falling-city storyboard,
a and b each. Every cloud, haze, glow and shadow is a smooth gradient drawn once (the 2026-10-30 dither audit).
Generator `tools/concept/act6_r72.py moods banks city` (round 73's falling city: `city73 b`); prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/jovian-moods-r72-a.png](concept/jovian-moods-r72-a.png) | Moods A, a grade per level (Q10 a): L36, L37 before and after the turn, L38–L42, each with its mean luminance and the readability set over its brightest and darkest window at 1× (sheet) | chosen (round 72 Q.10 a) |
| [concept/jovian-banks-r72-a.png](concept/jovian-banks-r72-a.png) | Foreground banks A, cream veil: the bank in the band's lit cream at 65 % over the bodies, lights and tells at full, bullets and the ship above; 25 % of the field, 0 % of the ship's band (sheet) | chosen (round 72 A.12 a) |
| [concept/falling-city-r72-b.png](concept/falling-city-r72-b.png) | The falling city B, breaking up: the deck breaking along a fault line, the halves sinking apart, the spires toppling (storyboard) | chosen (round 72 A.13 b) |
| [concept/falling-city-r73-b.png](concept/falling-city-r73-b.png) | The falling city B redrawn by round 72 A.11's rule: whole until the deck drops, then breaking along a jagged fault: ragged strut and deck ends, torn plate flaps, a charred, hot-glowing rim, shards in the widening gap, smoke and fire venting from both edges (storyboard) | proposed (round 73: the falling city's jagged break, round 72 A.11) |

Round 73 (M10 part A, step ART-A): the lightning strike's production frames by `tools/art/act6_gear.py lightning` and
the strike's open look (QA-2), a/b proposals by `tools/concept/act6_r73.py lightning` (a, the recommendation, written
until the pick; the frames wait in `build/art-held/sprites/` until the game claims `lightning`). Prompts:
[concept/prompts.md](concept/prompts.md#lightning-final-r73-a).

| File | What | Status |
|---|---|---|
| [concept/lightning-final-r73-a.png](concept/lightning-final-r73-a.png) | Final frames: the telegraph tile `lightning-tell_0..1` (the 40 px column's faint edges, the thin blue-white line) and the strike `lightning-strike_0..2` (a); the tell, the strike and the flash over Level 38's storm and Level 36's bands, a Talon in the column dying (sheet) | proposed (round 73) |
| [concept/lightning-final-r73-a.gif](concept/lightning-final-r73-a.gif) | Tell, strike and flash over Level 38 and Level 36 side by side, 1× | proposed (round 73) |
| [concept/lightning-strike-r73-a.png](concept/lightning-strike-r73-a.png) | QA-2 a "jagged bolt" (recommended): a jagged blue-white bolt filling the 40 px column top to bottom, a white core, two short side forks, the sky flash; over Level 36's grade and Level 38's storm (sheet) | proposed (round 73 QA-2) |
| [concept/lightning-strike-r73-a.gif](concept/lightning-strike-r73-a.gif) | a: the 1.0 s tell, the 0.25 s strike, over Level 36 and Level 38, 1× | proposed (round 73 QA-2) |
| [concept/lightning-strike-r73-b.png](concept/lightning-strike-r73-b.png) | QA-2 b "light column": a straight full-height column of white light with a jagged core line, its edges fading to the core over the 0.25 s (sheet) | proposed (round 73 QA-2) |
| [concept/lightning-strike-r73-b.gif](concept/lightning-strike-r73-b.gif) | b: the same moments, 1× | proposed (round 73 QA-2) |
| [concept/hazards-capture-r73-a.png](concept/hazards-capture-r73-a.png) | Game capture (the [A4 rig](concept/prompts.md#act6-rig-capture-r73-a)): the told strike on ART-B's cream bands and over round 72's grade, a dry eruption's vent tell and rock, the foreground banks over air units (A3b's code veils until B4 draws ART-B's bank pieces) on cream and rust | proposed (round 73) |
| [concept/hazards-capture-r73-a.gif](concept/hazards-capture-r73-a.gif) | The strike's tell and strike over the cream bands, 1× | proposed (round 73) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Open questions

- None open.

## Decisions

- 2026-09-30: Jovian system as Act 6; signature: a sinking cloud city.
- 2026-09-30: Io refineries added as a sub-location (used by L39 *Io Flyby*).
- 2026-10-01: Linked the chosen scene from art direction; `art: chosen`.
- 2026-10-31 (Act 6 design phase, step D-enemies-B; decided 2026-10-31 (round 72)): the sub-locations per
  level, the media per level (J2), the moods per level (Q10 a), the hazards (lightning and eruptions J3, the
  foreground banks Q2 a, wind shear, the falling city Q4 a, the three-way battle Q3 a + c, the pylon gates) drafted.
- 2026-10-31 (decided by me, trivia): wind never moves bullets (Level 12's built rule replaces the sketch's "slow or
  speed up bullets"); lightning does 60 to an `air` enemy (Level 27's plume did 40 at reference 116; 60 at L38's 152
  kills the small units and not the medium); eruption rocks hurt only the ship and Rook and pay nothing; the
  storm layer and Callisto orbit added as sub-locations; Act 6's haze and glow layers never use ordered dither.
- 2026-10-31, design phase (step m10-d-act; decided 2026-10-31 (round 72)): Level 42 has no ground layer (its draft keeps the
  cruisers on `low-air`), so `NO GROUND` covers L36, L38, L40 and L42.
- 2026-10-31 (user, round 72, closed): Q2 a (the foreground banks over `air`, translucent, bullets above; b Level 12's rule and
  c full hide rejected), Q4 a (the falling city at Level 37's end; b the act outro or briefing image only and c moved to
  Level 41 rejected), Q10 a (a grade per level; b one storm-dusk grade and c the bright r06 grade rejected), Q3 a with c's
  backdrop (b full faction fire and c backdrop and radio only rejected), J2 and J3 kept (the media per level; lightning
  and eruptions as drafted). **A.12 a** (not the recommendation): the banks are the **cream veil**; the user: "we do not
  need to tell these clouds apart; clouds at different altitudes do not have different colors", so the rule is written
  in the foreground-banks row and the worry about telling a bank from the deep layer is dropped. **A.13 b** (not the
  recommendation): the falling city **breaks up** along an authored, jagged cut line into two city layers that sink at
  their own rates, the spires toppling (it needs two ground layers where "one layer" was drafted; Level 37, the act README
  and the engine feature follow). The moods ladder a and the falling-city storyboard b are chosen, the other variants
  are in `concept/rejected/`. Approved as drafted with these changes (D.16).
