---
title: Briefing screen
design: approved
implementation: done
art: final
depends-on: [../../story, ../../campaign]
updated: 2026-10-29
---

# Briefing screen

## Summary

Before every level, the story is told in a briefing: character portraits and typed text on a
tactical display, ending with the mission objectives. This is the main storytelling vehicle,
together with in-level radio chatter (see [HUD](../hud/README.md)).

## Design

```
┌──────────────────────────────────────────────────────────────────────────────┐
│ ACT II · HOMEFRONT                           MISSION 10: EVACUATION CORRIDOR │
├───────────────┬──────────────────────────────────────────────────────────────┤
│ ┌───────────┐ │  ╔════════════════════════════════════════════════════════╗  │
│ │           │ │  ║                                                        ║  │
│ │ PORTRAIT  │ │  ║        TACTICAL MAP / MISSION IMAGE (672×240)          ║  │
│ │ 144×144   │ │  ║                                                        ║  │
│ │           │ │  ╚════════════════════════════════════════════════════════╝  │
│ └───────────┘ │                                                              │
│ CMDR OKAFOR   │  "Lancer, the Vrell are in the Nova Lagos outskirts.         │
│ CDF COMMAND   │   The evacuation corridor is still open. Keep them off the   │
│               │   shuttles, and watch your six. Okafor out."█                │
├───────────────┴──────────────────────────────────────────────────────────────┤
│ OBJECTIVES: ▪ Escort the evacuation shuttles  ▪ Bonus: no shuttle lost       │
│                                            [ENTER] continue  [ESC] main menu │
└──────────────────────────────────────────────────────────────────────────────┘
```

The mock shows level 10 *Evacuation Corridor* from [act 2](../../campaign/act-2-homefront/README.md);
the real briefing text is written in each level document.

- A briefing is a script of **pages**. Each page has a speaker (portrait + name), text, and
  optionally a map image (672×240 above the text, 4 lines of text below it; a longer page goes on
  over the next screens with the same image, each counted as a page). Several speakers can alternate (Okafor, Varga, Rook, intercepted
  transmissions from Vorne or the Choir).
- Text types out at twice the radio's speed with a soft blip: 60 characters/s at the default
  text speed of 30; the Gameplay tab's text speed scales both. Confirm shows the full page, then
  continues.
- **Every mission starts here** (user decision 2026-10-18): after a debrief, a new game, an act
  start, a Continue or a loaded slot (the act intro first only while it is due), and a mission
  select replay (the level's own pages, without its act's title card and intro); confirm on the
  last page goes on to the hangar. Only the debug starts go straight into the level.
- **Back (Esc / B) quits to the main menu** (user decision 2026-10-04), on the title card too,
  after the kit's confirm dialog ("QUIT TO MAIN MENU?", as the hangar's quit asks). It leaves the
  campaign as it is: after a won level the briefing writes the autosave the hangar would have
  written as it opened, so the level stays won and Continue opens this briefing again; a new game's
  intro briefing has nothing to keep yet and writes no save (an older campaign's autosave stays).
  Back no longer skips to the objectives; confirm still shows each page at once.
- Portraits: 144×144, pre-rendered, three expressions per main character (neutral, grim, fierce);
  a page names its expression in the data, neutral otherwise.
  Interference/static effect for intercepted transmissions.
- Act start/end briefings may be longer; normal levels are 2–4 pages.
- Briefing text lives with each level in the [campaign](../../campaign/README.md).

### Intercepted transmissions (draft)

*Draft, step `m9-d-systems` (2026-10-27), **approved in round 60** (Q9 = a). The
steady interference "comes with the first intercepted briefing" (Decisions, 2026-10-02): that is the
Act 5 intro, whose pages 2–3 are Vorne's system-wide broadcast; the texts and the page order are the
[act's](../../campaign/act-5-the-belt/README.md).*

A page whose speaker is on the **enemy channel** (Vorne, later other Ascendancy voices; the Choir keeps
its own glyph) is an *intercepted* page. It keeps the layout above and changes its dress:

- **Steady interference** over the portrait and the page's image for the whole page, not only the
  static on open and close: the transmission-static noise at about a third of its open/close strength,
  three slow rolling bands, and a short horizontal tear (a 2–4 px line shifted sideways for 0.1 s) every
  2.5–4 s. The text itself stays clean and readable.
- The **header** reads `INTERCEPTED · ALL CHANNELS` in place of the act and mission line for those
  pages; the speaker plate reads `VORNE` over `JOVIAN ASCENDANCY`, in **amber** (the enemy channel's
  colour, as on the radio: [HUD](../hud/README.md#act-5-additions-draft)).
- The text types out in amber at the normal speed; the typing blip is replaced by a faint carrier hiss
  that runs while the text types.
- Vorne's portrait is the chosen amber enemy-channel style (round 04, concept r04-a: his own colours
  under the amber tint, the three expressions); production portraits are proposed in round 60.
- In the data a page names its channel (`channel: enemy`), the friendly channel by default; the same
  flag gives a radio line its enemy dress.
- A page's image (the gilded lectern, Ceres Hub's lights turning red) is drawn under the interference
  too, as if received on the same feed.

## Concept art

Concept [round 08](../../concept-rounds/round-08/README.md) — glass style (out-of-game) per the ui style rule; generator `tools/concept/ui_r08.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/rejected/briefing-r08-a.png](concept/rejected/briefing-r08-a.png) | Briefing — L10 *Evacuation Corridor*: Okafor portrait, typewriter text, tactical map with the shuttle route, threat summary with direction dial, objectives and hangar teaser | rejected — round 44, A.a: superseded by the production art (a layout mockup: the screen is built by `tools/art/briefing_images.py` and the glass kit) |
| [concept/rejected/act-title-r08-a.png](concept/rejected/act-title-r08-a.png) | Act title card — "ACT II / HOMEFRONT" in the logo-D chrome over Nova Lagos | rejected — round 44, A.a: superseded by the production art (`assets/ui/act-N-title.png` is made by `tools/art/act_stills.py`) |

Production art, UI batch part U2 (for concept round 13, opened by part U3): no art of its own; the screen draws the production glass kit ([tools/art/ui_kit.py](../../../tools/art/README.md)), its sheet made from the capture by `tools/art/ui_review.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/briefing-final-r13-a.png](concept/briefing-final-r13-a.png) | Review sheet: the game capture at 1× with a 2× detail of the header, portrait frame and text panel | final (round 44, A.a: a record of production assets) |
| [concept/briefing-capture-final-r13-a.png](concept/briefing-capture-final-r13-a.png) | Game capture (retaken in part U3): page 1 of the Act 1 intro with its image, the Tether Gate, above the text; Okafor in the trim frame, the objectives and hangar teaser panels, the key-hint plate | final (round 44, A.a: a record of production assets) |

Production art, UI batch part U3: the briefing images, rendered by [tools/art/briefing_images.py](../../../tools/art/README.md) into `assets/ui/briefing/`; the portraits' expressions are the [characters](../../story/characters/README.md)' review files. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/briefing-images-final-r13-a.png](concept/briefing-images-final-r13-a.png) | Review sheet: the nine 672×240 images, one per page of the Act 1 intro (Tether Gate, outer stations, the L1 strike group, Earth orbit's squadrons, the Stormhawk schematic), Level 01 (the Gagarin yards' rails, the scan of the Skitter and Needler) and Level 02 (the burning south arm's docks, the "yield" pattern and the turret's blind arc) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r20-a.png](concept/briefing-images-final-r20-a.png) | Review sheet, batch "M4 briefing images" ([round 20](../../concept-rounds/round-20/README.md)): the four 672×240 images of Level 03 (the high lanes over the battle site's debris field with the spore carriers seeding Earth; the Spore Bomber, its rising spores and a wide spread, the long-range echo) and Level 04 (the convoy road from Tranquility Base across the rille to the mass-driver terminal, brood pods, walkers in the craters, Hammer flight; the Scuttler's claws and glowing back) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r21-a.png](concept/briefing-images-final-r21-a.png) | Review sheet, M4 part E ([round 21](../../concept-rounds/round-21/README.md)): the two 672×240 images of Level 05 (the nest crater beside the mass-driver line with batteries A–D, Lancer's run over the rim, the sleds; the Polyp Mortar's lob to its marker a second ahead, the ship moving out, an unknown contact in orbit) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r23-a.png](concept/briefing-images-final-r23-a.png) | Review sheet, M4 part F ([round 23](../../concept-rounds/round-23/README.md)): the two 672×240 images of Level 06 (the far side across the terminator, the silent settlements, Daedalus Rim's lit domes, Lancer's run into the dark by headlight; the Mantis at the screen edge sweeping its beam, side-firing guns reaching it, the Coilwyrm coming round behind the ship) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r25-a.png](concept/briefing-images-final-r25-a.png) | Review sheet, M4 part G (round 25): the two 672×240 images of Level 07 (the Brood Carrier holding at L1 with its escort screen, the overrun picket and the pods on Luna traced back to it; the overhead scan: nose-down on high air over the ship with its shadow and a bay pair open, only missiles reaching up, the turn in place, broadside at the ship's level with a pair open and the plate iris open over the lime core) and the four of the Act 1 outro, one per page (the carcass drifting apart at L1 with Earth beyond; Daedalus Rim still empty, lights on, the file open; the second fleet's track from beyond the Moon into Earth's atmosphere; Aegis Wing reassigned to Earth defence, Rook's craft on Lancer's wing) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r30-a.png](concept/briefing-images-final-r30-a.png) | Review sheet, M5 part B (round 30): the four 672×240 images of the Act 2 intro, one per page (the landers' burning trails coming down through the cloud deck over the Gulf of Guinea with the CDF tracking overlay counting them; the CDF global display with the three landing zones and the act's fronts: the cities, the Atlantic sea lanes, the Arctic relay chain; a Nova Lagos street from rooftop height, people on the low roofs looking up as Lancer passes low; Aegis Wing and Rook's Ember on a coastal airbase at dusk, Nova Lagos burning 40 km off) and the two of Level 08 (the night route over the harbour, the elevated highways, the tower district and the Third Mainland highway to the Ikoyi shelters, the walkers heading for them, Lancer and Rook; the Creeper on a low roof with its aimed five-way fan, a convoy's fans 0.5 s apart, the anti-ground ×2 marker) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r31-a.png](concept/briefing-images-final-r31-a.png) | Review sheet, M5 part C (round 31): the two 672×240 images of Level 09 (`level-09-arcology-district`: the route over the arcology district with the three node clusters, the Okonjo Bridge and the Ndidi Arcology; `level-09-node-scan`: the Hive Node's iris cycle and the Ravager's pounce, from the production sprites) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r32-a.png](concept/briefing-images-final-r32-a.png) | Review sheet, M5 part D (round 32): Level 10's two 672×240 images (`level-10-evacuation-route`: the corridor at first light from Eko spaceport's five pads over the suburbs, the viaduct and the coast road with the capsized ferry to the lagoon's climb-out; `level-10-wraith-scan`: the Wraith cloaked and decloaked, its pass, loop and rear entry with the 3 s warning and bursts through the shuttle band, a Mote Swarm's loop-back, from the production sprites) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r33-a.png](concept/briefing-images-final-r33-a.png) | Review sheet, M5 part E (round 33): Level 11's two 672×240 images (`level-11-convoy-route`: the North Atlantic from above with Convoy Atlas-Seven's four hulls, the seeded sea lanes, the reef line, Platform Tiamat with the Kraken's arms and its hologram under it, the route to open water; `level-11-sub-scan`: a waterline cut-away with a Driftjelly surfaced and submerged, the shots stopping at the waterline, a torpedo's run, a Reef Spitter raft with its roots, the unknown large contact; from the production sprites) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r34-a.png](concept/briefing-images-final-r34-a.png) | Review sheet, M5 part F (round 34): Level 12's two 672×240 images (`level-12-storm-front`: the storm system from above off the Azores, its cloud bands winding clockwise into the eye, moving east onto the convoy lanes, Vrell flyers in it, Lancer and Rook's route in through the wall, across the eye and out; `level-12-sonde-scan`: the production weather sonde blinking in the rain with its fly-through ring and the SONDES tracker, beside a pressure plot whose measured curve is a regular saw where a storm's curve should be) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r35-a.png](concept/briefing-images-final-r35-a.png) | Review sheet, M5 part G (round 35): Level 13's two 672×240 images (`level-13-relay-grid`: the Arctic from above the pole, Nansen Relay north of Svalbard and the defence grid it ties together (the Arctic relays, guns and radars, the satellites on their orbit), an inset of the halt's frame from the final backdrop with the relay, the landing force closing from every edge and Lancer and Rook holding over it; `level-13-ice-field`: the halt's frame as a scan, a Skimmer weaving along the south lead (its lane and the 40 px weave), a Scuttler with its claws' armoured front arc and the side shots that get in, a shot aimed at the relay with the ship standing in its way) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r36-a.png](concept/briefing-images-final-r36-a.png) | Review sheet, M5 part H (round 36): Level 14's two 672×240 images (`level-14-spire-over-geneva`: Geneva Concord seen from the lake, the Jet d'Eau, the old town's roofs and Saint-Pierre's towers, the Siege Spire's trunk rising three kilometres out of the Concord rotunda into the canopy spread over the old town, the route along the lakefront and through the old town to the rotunda, Lancer and Rook going in first; `level-14-root-scan`: Varga's scan of the root grid, the final backdrop's Root Field through the hologram with its roots traced and the burst points marked, one burst in detail (the cracking paving's 1 s of dust, the root with its Spine Turret and Polyp Mortar), anti-ground for the roots, and a Wraith coming from behind once the crown opens) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r38-a.png](concept/briefing-images-final-r38-a.png) | Review sheet, M7 part B (round 38): the Act 3 intro's four and Level 15's two 672×240 images (`act-3-arrival`: Mars in the transport's forward view with the colonies' beacons, most dark, Ares Landing lit; `act-3-mars-front`: the Mars front display, Hellas glowing, the spread north, the colonies marked; `act-3-colonists`: a dome-ring gun pit at dusk; `act-3-drop`: the drop bay over Mars; `level-15-descent-route`: the entry-corridor profile from the transport over Olympus Mons, the escarpment and the dunes to the rail line; `level-15-burrower-scan`: the Burrower scan) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r39-a.png](concept/briefing-images-final-r39-a.png) | Review sheet, M7 part C (round 39): Level 16's two 672×240 images (`level-16-canyon-route`: the Valles on a CDF plot, the route's six sections, Ares Landing, an unlabelled black square on the north rim (Helix); `level-16-tendril-scan`: a gate with a beam burning through the tendril, a pair broken by an anchor kill, a Threadcrawler at the wall's foot) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r40-a.png](concept/briefing-images-final-r40-a.png) | Level 17's briefing images (`level-17-ares-domes`, `level-17-choir-herald`; `tools/art/briefing_images.py`) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r41-a.png](concept/briefing-images-final-r41-a.png) | Level 18's briefing images (`level-18-storm-front`, `level-18-visibility`; `tools/art/briefing_images.py`) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r42-a.png](concept/briefing-images-final-r42-a.png) | Level 19's briefing images and Level 20's filed plate (`level-19-tharsis-works`, `level-19-rear-arcs`, `level-20-filed-plate`; `tools/art/briefing_images.py`, step ART-F4) | final (round 44, A.a: a record of production assets) |
| [concept/briefing-images-final-r44-a.png](concept/briefing-images-final-r44-a.png) | Review sheet, M7 part H (round 44): Level 21's two 672×240 images (`level-21-hellas-basin`: the basin on a CDF plot, the CDF line held on the rim, the inner wall's terraces, the dune sea with a moving seismic contact, the lime hollow with seismic rings, the descent route and five sections; `level-21-colossus-scan`: Varga's seismic cut with the body under the sand, the ridge and a breaching mouth, beside a breach from above at the left edge with the gullet, the bite circle and the suction; `tools/art/briefing_images.py`, step ART-H3) | final (round 44, F.e: approved as final) |
| [concept/briefing-images-final-r51-a.png](concept/briefing-images-final-r51-a.png) | Review sheet, M8 part B (round 51): the Act 4 intro's four 672×240 images (`act-4-arrival`, `act-4-europa-cutaway`, `act-4-hydro-kit`, `act-4-drop`) and Level 22's two (`level-22-conamara-map`, `level-22-drill-rig`; `tools/art/briefing_images.py`) | chosen (round 51, F.7: accepted as final) |
| [concept/briefing-images-final-r52-a.png](concept/briefing-images-final-r52-a.png) | Review sheet, M8 part C (round 52): Level 23's two 672×240 images, `level-23-drill-shaft` (Okafor's section through the ice: the shaft, the collars, the waterline, Thera Deep) and `level-23-eel-and-fry` (Varga's scan: the eel's gills and stripe, a Glassfin ribbon, the rules strip; `tools/art/briefing_images.py`) | chosen (round 52, F.9: accepted as final) |
| [concept/briefing-images-final-r53-a.png](concept/briefing-images-final-r53-a.png) | Review sheet, M8 part D (round 53): Level 24's two 672×240 images, `level-24-abyssal-plain` (Okafor's sonar map: Thera Deep's last lamps, four red reefs, the beacon line, the trench, the route in teal) and `level-24-abyss-ray` (Varga's scan: the depth cut with the shadow and the rise, the Ray from above with its gill slits and a fan of nine, a Barbfin inset; `tools/art/briefing_images.py`) | chosen (round 53, F.6: accepted as final) |
| [concept/briefing-images-final-r54-a.png](concept/briefing-images-final-r54-a.png) | Review sheet, M8 part E (round 54): Level 25's two 672×240 images, `level-25-siren-trench` (Okafor's sonar chart: the walls, side caves, pillar and narrows, the nests on both walls, the beacons, the survey sub's cave, the route) and `level-25-sonar-ring` (Varga's diagram: the Siren on a wall, two rings and the gap, the ship slowed in the band, the 2 s slow and 3 s immunity; `tools/art/briefing_images.py`) | chosen (round 54, F.6: accepted as final) |
| [concept/level-25-briefing-capture-r54-a.png](concept/level-25-briefing-capture-r54-a.png) | Game capture, M8 part E (step E6, a playthrough save at Level 25 on medium): the briefing's two pages in the game, Okafor's chart and Varga's ring diagram | chosen (round 54, F.6: accepted as final) |
| [concept/briefing-images-final-r55-a.png](concept/briefing-images-final-r55-a.png) | Review sheet, M8 part F (round 55): Level 26's two 672x240 images, `level-26-blackwater` (Okafor's chart: the trench's mouth, the last lamps, nothing charted, the lost CDF buoys, the dotted route, the vents' glow far south) and `level-26-lure` (Varga's diagram: the angler as the dark shows it, a gold pickup beside it, the headlight cone, the 130 px lunge radius, the ring of 8; `tools/art/briefing_images.py`) | chosen (round 55, F.7: accepted as final) |
| [concept/briefing-images-final-r57-a.png](concept/briefing-images-final-r57-a.png) | Review sheet, M8 part G (round 57): Level 27's two 672x240 images, `level-27-vent-field` (Okafor's sonar chart: the smokers, every plume vent ringed amber, the ridge's four red-gold relays, the gate's teal, the route, Aegis Two's last position) and `level-27-plume-cycle` (Varga's diagram: glowing 1.4 s, erupting 1.6 s, cold 1.5 s, a shot bent 25 degrees; a grey hull below with a torpedo rising; `tools/art/briefing_images.py`) | chosen (round 57: accepted as final) |
| [concept/briefing-images-final-r58-a.png](concept/briefing-images-final-r58-a.png) | Review sheet, M8 part H (round 58): Level 28's two and the Act 4 outro's four 672x240 images (`tools/art/briefing_images.py`): `level-28-hatchery` (Okafor's sonar chart: the gate with grey unmarked contacts, the egg-rack galleries, the spawning cavern, the antechamber, the caldera with a ring of faint returns under it, Level 23's shaft as the way out, teal Vrell vectors), `level-28-hull-code` (Varga's scan: `HDX-417` under the burnt paint, the Helix Dynamics emblem, the code's pulse train matching a beacon carrier, the guardian's silhouette round a vent), `act-4-outro-orbit`, `act-4-outro-hullcode` (a 2x crop of the production cruiser's bow, its port side, the code upright with the bow to the right), `act-4-outro-ready-room`, `act-4-outro-aegis` | chosen (round 58: accepted as final) |
| [concept/level-28-briefing-capture-r58-a.png](concept/level-28-briefing-capture-r58-a.png) | Game capture, M8 part H (step H7): Level 28's two briefing pages in the game from a playthrough save (Okafor's hatchery chart, Varga's hull-code scan) | chosen (round 58: accepted as final) |
| [concept/level-26-briefing-capture-r55-a.png](concept/level-26-briefing-capture-r55-a.png) | Game capture, M8 part F (step F6, a playthrough save at Level 26 on medium): the briefing's two pages in the game, Okafor's chart and Varga's lure diagram | chosen (round 55, F.7: accepted as final) |
| [concept/level-27-briefing-capture-r57-a.png](concept/level-27-briefing-capture-r57-a.png) | Game capture, M8 part G (step G5, a playthrough save at Level 27): Level 27's two pages in the game, Okafor's sonar chart of the vent field and Varga's plume cycle with the grey hull below | chosen (round 57: accepted as final) |
| [concept/act3-outro-final-r44-a.png](concept/act3-outro-final-r44-a.png) | Review sheet, M7 part H (round 44): the Act 3 outro's four 672×240 images (`act-3-outro-hellas`: the basin at dawn, the collapsed crater, the last spore trails; `act-3-outro-domes`: Ares Landing's domes at night, every pane lit, colonists on the ring road looking up; `act-3-outro-europa`: Varga's plot of the courses off Mars converging on Europa, the filed plate in a minimised window; `act-3-outro-aegis`: Aegis Wing refitting on the spaceport apron at dusk, Jupiter a bright point; `tools/art/outro_l21_images.py`, step ART-H3) | final (round 44, F.f: approved as final) |

Production art, M9 part B (concept round 62, step ART-B): the Act 5 intro's four images and Level 29's two, rendered by `tools/art/briefing_images.py` into `assets/ui/briefing/`. Prompts: [concept/prompts.md](concept/prompts.md#round-62--production-the-act-5-intro-and-level-29-m9-part-b).

| File | What | Status |
|---|---|---|
| [concept/briefing-images-final-r62-a.png](concept/briefing-images-final-r62-a.png) | Review sheet, M9 part B (round 62): the Act 5 intro's four 672×240 images (`act-5-all-channels`, `act-5-broadcast`, `act-5-stations-choose`, `act-5-burn-for-ceres`) and Level 29's two (`level-29-ceres-hub`, `level-29-talon`) | chosen (round 62, F.10: accepted as final) |
| [concept/intercept-briefing-capture-r61-a.png](concept/intercept-briefing-capture-r61-a.png) | Game capture of an intercepted page (step A4, the [capture rig](../../enemies/concept/prompts.md#belt-rig-capture-r61-a)'s third Level 08 page, reached by Continue from a Level 08 save): `INTERCEPTED · ALL CHANNELS`, the amber plate and text, the steady interference with a tear over the portrait. Varga stands in for Vorne (the briefing has no plate for him yet) and the page has no image (a page with one crashes: round 61) | chosen (round 61, C.6: accepted) |
| [concept/intercept-briefing-capture-r61-a.mp4](concept/intercept-briefing-capture-r61-a.mp4) | The page typing with the game's sound: the intercept's open, the carrier hiss under the typing | chosen (round 61, C.6: accepted) |

Production art, M9 part C (concept round 63, step ART-C): Level 30's two images, rendered by `tools/art/briefing_images.py` into `assets/ui/briefing/`. Prompts: [concept/prompts.md](concept/prompts.md#round-63--production-level-30-m9-part-c).

| File | What | Status |
|---|---|---|
| [concept/briefing-images-final-r63-a.png](concept/briefing-images-final-r63-a.png) | Review sheet, M9 part C (round 63): Level 30's two 672×240 images (`level-30-shipping-lane`: Okafor's chart of the lane, its buoys, the rock drift, the convoy's track and the tug's departure point; `level-30-minelayer`: Varga's scan of a Minelayer over its line, the clusters of four ringed, the gap marked, a lit mine inset) | chosen (round 63, F.7: accepted as final) |

Production art, M9 part D (concept round 64, step ART-D): Level 31's two images, rendered by `tools/art/briefing_images.py` into `assets/ui/briefing/`. Prompts: [concept/prompts.md](concept/prompts.md#round-64--production-level-31-m9-part-d).

| File | What | Status |
|---|---|---|
| [concept/briefing-images-final-r64-a.png](concept/briefing-images-final-r64-a.png) | Review sheet, M9 part D (round 64): Level 31's two 672×240 images (`level-31-mining-rig-escort`: Okafor's nav chart of the open belt, the tug's track from Bright Seam to the Vesta relay, the drift fields, the two outcrops' nests, the ambush point; `level-31-tug-sections`: Varga's diagram of the tug from above with its three bars, a Hornet's salvo onto the bow, a Shard line across the cargo, Darts from behind on the engines) | chosen (round 64, F.10: accepted as final) |
| [concept/briefing-images-final-r65-a.png](concept/briefing-images-final-r65-a.png) | Review sheet, M9 part E (round 65): Level 32's two 672×240 images (`level-32-deep-core`: Okafor's consortium shaft chart of Kleopatra in section, the shaft, galleries, both forks with their channels named, the pumping hall, the refinery, the ore chute out, the towers marked; `level-32-sentinel-sweep`: Varga's diagram of a Sentinel with its emitter, collar, 120° arc and reach ring, and a Strider losing a leg, its fall line across a SAM Nest) | chosen (round 65, F.6 ok: accepted as final) |
| [concept/briefing-images-final-r66-a.png](concept/briefing-images-final-r66-a.png) | Review sheet, M9 part F (round 66): Level 33's two 672×240 images (`level-33-leech-field`: Okafor's nav chart of the Leech Field, the route through the big rocks from the field's edge to clear rock, the growth shaded violet, the Rail Bunkers as red ticks, the ridge, Ceres's light at the top edge; `level-33-power-drain`: Varga's diagram of two leeches latched on the Stormhawk, the generator gauge with 4 MW drained in amber, the shield ring dimmed, the shake-off as arrows) | chosen (round 66, F.7 ok: accepted as final) |
| [concept/briefing-images-final-r67-a.png](concept/briefing-images-final-r67-a.png) | Review sheet, M9 part G (round 67): Level 34's two 672×240 images (`level-34-graveyard`: Okafor's nav chart of the ship graveyard, the lane through it, the CDF picket's frigates in grey, the Helix hulks dark with gold edges, the freighter boxed in gold, the tanks' roads as red dots, the containers as amber diamonds, Ceres and the Sovereign at the top right; `level-34-helix-hold`: Varga's salvage scan of the hold, the crate in its four clamps from `tools/art/belt_units_g.py`'s sprites, the Helix seal lit, the core's 22 MW signature beside the Mk IV's 20) | chosen (round 67, G.6 ok: accepted as final) |
| [concept/briefing-images-final-r68-a.png](concept/briefing-images-final-r68-a.png) | Review sheet, M9 part H (round 68): Level 35's two 672×240 images (`level-35-ceres-orbit`: Okafor's chart of Ceres orbit, the escort screen in gold, the outer hull to the Sovereign over the north pole, the turned mass relay's red line, Ceres Hub red and Ceres Control still CDF; `level-35-sovereign-rings`: Varga's plan of the three rings with their SAM, rail and laser batteries and turning arrows, the 120° fire arc toward the ship in red, the core's six petals shut) and the Act 5 outro's four (`act-5-outro-ceres`: the broken rings drifting over Ceres's pole, the hub's lights white again; `act-5-outro-face`: the recruitment feed paused on Vorne beside his aged 2179 Helix photo, the nav core's route to Callisto; `act-5-outro-ember`: Rook's Ember on its rail with the clamps' scorch marks, Rook on the ladder; `act-5-outro-jupiter`: Jupiter and Callisto through the bay door, all four rails filled) | chosen (round 68, H.6 ok: accepted as final) |

## Implementation

- [x] Briefing script format: pages, speaker, text, objectives
- [x] A page's image (tactical map or mission image): `image` in the data, drawn above the text
- [x] Typewriter text with skip and page advance
- [x] Back (Esc / B) asks, then quits to the main menu, keeping the campaign (the won level's
      autosave; no save for a new game's intro): `vanguard.game.briefing.BriefingExit`, tested in
      `BriefingExitTest`
- [x] Portrait frame with the transmission-static effect
- [x] Portrait expressions (neutral, grim, fierce): `expression` in the data, neutral by default
- [x] Level 06's two images rendered again for round 23's outcomes: the Coilwyrm at spacing 0.5
      (overlapping) and the Mantis's beam and telegraph from its head
- [x] Level 07's two images and the Act 1 outro's four (one per page, part G's D3), named in the
      level's and the act's data; `BriefingLayoutTest` checks every level page's and outro page's
      image at the screen's size
- [x] The Act 2 intro's four images and Level 08's two (one per page, D3 of M5 part B), named in
      the act's and the level's data; `BriefingLayoutTest` passes with them
- [x] Intercepted pages as under *Intercepted transmissions* (draft): the steady interference over the portrait and
      image, the `INTERCEPTED · ALL CHANNELS` header, the amber plate and text, the carrier hiss, `channel: enemy` in
      the data — M9 (the act intro; part A, step A2: a page's `channel`, `BriefingPage.onChannel`, a
      radio cue's `channel` and the voice table's `filter: enemy` for Vorne and Harker, `RadioCue.channel(voices)`;
      step A3: the look and the sound, `BriefingScreen` (the header, the amber plate and text, the steady interference
      over the portrait and the image with its tear, the intercept's open and close, the carrier while it types);
      the act intro's pages carry `channel: enemy` in the Act 5 data; round 62 F.8 accepted the capture)
- [ ] The act-title lettering (`assets/ui/act-N-*-title.png`, Acts 1-3) from a `tools/art` generator with the pixels unchanged
      and a `Source` chunk, instead of `tools/concept/ui_assets.py` (round 44, X.l: planned, step ART-K)

## Open questions

- Q9 of [round 60](../../concept-rounds/README.md) (Vorne's broadcast as the Act 5 intro): recommended
  (a), Okafor, two intercepted Vorne pages, Okafor's answer. Alternatives: (b) all four pages Vorne
  (loses Act 4's "we hear it together"); (c) Okafor's usual intro with Vorne heard as radio lines at L29's
  start (cheapest; the broadcast shrinks to the radio box).

## Decisions

- 2026-09-30: Briefing comes before the hangar (see [systems](../../systems/README.md)).
- 2026-09-30: Converted to the 960×540 baseline (was 640×360).
- 2026-10-01: Concept round 08: accepted.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-02: M3 part B1 (`vanguard.game.screen.BriefingScreen`, `vanguard.game.briefing.BriefingPager`).
  Briefing text lives in the data: a level's `briefing` (pages of speaker and line, and the hangar
  teaser) in its data.yaml, the act's title card and act briefing in the act's new data.yaml
  (`design/campaign/act-N-slug/data.yaml`, with the act's level range); the READMEs render them.
  The briefing before a level that opens its act starts with the act title card (held 3.5 s,
  confirm skips) and the act briefing, so a new game's intro briefing is the Act 1 title card, the
  act briefing and the Level 01 briefing (7 pages). Layout after briefing-r08-a: the header with
  the act and the mission, the 144×144 portrait (cut from the chosen r04 sheets) with name plate,
  role and page count, the typed text (upper case, 10×20) with a block cursor, the objectives and
  the hangar teaser below; Back skips to the last page, confirm on the last page goes on to the
  hangar. The text types at the Gameplay tab's text speed (the options document: "text speed for
  briefings and radio"; default 30 characters/s, the Design section says ~60: open question), with
  the typewriter blip on every other character, over the briefing theme. The objective lines are
  generated from the level's objectives ("SURVIVE TO THE END OF THE MISSION", "BONUS: DESTROY 65 %
  OF ALL ENEMIES"). The act title card's chrome lettering is rendered by
  `tools/concept/ui_assets.py acts` with the chosen card's chrome treatment; its scene is the title
  scene until a still of the Gagarin yards exists. Not built: map images (no level has one), the
  concept's threat summary (the hangar intel's job, part B2), expressions and the transmission
  static, so the format and portrait items stay open.
- 2026-10-02: User decision: briefings type at **twice the radio speed**, 60 characters/s at the
  default text speed of 30 (`GameplaySettings.briefingTextSpeed()`); the text-speed setting scales
  both. This settles the B1 note's open question (30 vs ~60).
- 2026-10-02: M3 close-out (user decision). The script format is data: a level's `briefing` pages
  (speaker and line) and teaser in its data.yaml, the act's title card and act briefing in the act's
  data.yaml, the objective lines generated from the level's objectives
  (`vanguard.content.campaign.BriefingScript`); so that part of the item is done and the page image
  waits for the first briefing image from the art track. The portrait opens through a burst of
  transmission static (0.35 s, fading out) on the first page and whenever the speaker changes, and
  closes through one (fading in) after the last page before the hangar opens
  (`vanguard.game.render.TransmissionStatic`: one noise texture generated at start-up, drawn from a
  random offset each frame with three brighter rolling bands, no allocation per frame). The
  expressions need new portrait art and move to the art track (see the
  [production plan](../../art-direction/production/README.md#order-of-work)). The steady
  interference for intercepted transmissions has no page in Acts 1–2 (the radio's Choir line is
  not a briefing); it comes with the first intercepted briefing.
- 2026-10-02: Production art, UI batch part U2: the briefing draws the production glass kit
  (panels, the trim nine-patch as the portrait frame, header rules, lit bar cells for the bullets
  and the typing cursor, the key-hint plate) over the title scene; layouts and fonts unchanged.
  Review files proposed for round 13; `art` stays `chosen`.
- 2026-10-02: Production art, UI batch part U3 (the two items deferred to the art track). A briefing
  page takes an optional `expression` (`neutral`, `grim`, `fierce`; neutral if not given) and an
  optional `image`, the name of a 672×240 picture in `assets/ui/briefing/`
  (`tools/art/briefing_images.py`); the screen draws the image in the trim frame at the top of the
  text panel and the text below it, 4 lines to a screen: a page longer than that goes on over the
  next screens with the same speaker and image, each counted in the page number (the Level 01
  briefing's first page takes two). Images for every page of the Act 1 intro and of Levels 01
  and 02 (Level 02's wait for its data in M4; its README names them). Expressions set in the data
  where the text calls for them: the Act 1 intro's page 2 ("Eleven days later our outer stations
  stopped answering…") grim, page 4 ("That makes us the line.") fierce; the rest neutral. The
  hangar teaser keeps neutral. Review files proposed for round 13; `art` stays `chosen`.
- 2026-10-02: Concept round 13 closed (user decision): the screen in the production glass kit and the nine briefing images of the Act 1 intro and Levels 01–02 (`tools/art/briefing_images.py`) approved as **final**; `art` stays `chosen`, since the images of the later levels and a still per act for the act title cards do not exist yet.
- 2026-10-03: Briefing images of Levels 03 and 04 (batch "M4 briefing images", `tools/art/briefing_images.py`), one per page, named in the levels' data; the review sheet `briefing-images-final-r20-a` proposed for [round 20](../../concept-rounds/round-20/README.md). `art` stays `chosen`.
- 2026-10-03: Concept round 20 closed (user decision, "both accepted"): the four briefing images of Levels 03 and 04 (`briefing-images-final-r20-a`) approved as **final**; `art` stays `chosen`, since the images of the later levels and a still per act for the act title cards do not exist yet.
- 2026-10-04: Briefing images of Level 05 (M4 part E, `tools/art/briefing_images.py`), one per page, named in the level's data; the review sheet `briefing-images-final-r21-a` proposed for [round 21](../../concept-rounds/round-21/README.md). `art` stays `chosen`.
- 2026-10-04 (user decision): Esc in the briefing did nothing useful (it skipped to the last
  page), so the player had to go on to the hangar to reach the main menu. Back now quits to the
  main menu after a confirm dialog and keeps the campaign (see Design); it no longer skips. The
  confirm dialog and the autosave on leaving are main-agent choices, for the user to confirm.
- 2026-10-04: Concept round 21 (user): Level 05's two briefing images (`briefing-images-final-r21-a`)
  approved as **final**; `art` stays `chosen` as after round 20.
- 2026-10-04: Briefing images of Level 06 (M4 part F, straight to production per user decision D8,
  `tools/art/briefing_images.py`), one per page, named in the level's data; they show the
  production Mantis, Coilwyrm, headlight cone and a Daedalus dome (`mantis.py`, `coilwyrm.py`,
  `l06_darkness.py`, `backdrop_l06.py`); the review sheet `briefing-images-final-r23-a` proposed
  for [round 23](../../concept-rounds/round-23/README.md). `art` stays `chosen`.
- 2026-10-05: Concept round 23 (user decision): Level 06's two briefing images
  (`level-06-daedalus-rim`, `level-06-edge-scan`) accepted. **Pending**: both are to be rendered
  again for the round's other outcomes, the Coilwyrm's spacing of 0.5 (an overlapping chain of
  about 330 px instead of beads with gaps) and the Mantis's beam and telegraph starting at its
  head (its eye), not its centre; `art` stays `chosen`.
- 2026-10-05: Level 06's two images rendered again for round 23's outcomes
  (`tools/art/briefing_images.py`, sheet `briefing-images-final-r23-a` rebuilt). `level-06-edge-scan`:
  the Coilwyrm at the data's spacing of 0.5, an overlapping chain of about 330 px on the same loop,
  with the head's path history dashed beyond the tail so the image still shows it coming round; the
  Mantis's telegraph and beam start at its eye (`sweep.origin`) in the chosen charged-lance look
  (`tools/art/mantis_beam.py`: its beam, tip and eye-ring sprites, the 70° wedge from its generator
  code at the image's 240 px reach, centred on the bearing to the ship), the beam 30 % into the
  sweep; "its beam sweeps across you" moved inside the new wedge. The wedge is no longer drawn in the
  lines layer, so that layer's 16 colours are no longer pulled toward its red: the labels and arrows
  show their intended red, violet, green and cyan. `level-06-daedalus-rim` shows neither unit and
  came out pixel-identical. `art` stays `chosen`.
- 2026-10-05: Briefing images of Level 07 and the Act 1 outro (M4 part G step A4, straight to
  production as part F's D8, `tools/art/briefing_images.py`), one per page (D3 for the outro), named
  in the level's and the act's data: `level-07-l1-carrier`, `level-07-overhead-scan`,
  `act-1-outro-carcass`, `act-1-outro-daedalus-rim`, `act-1-outro-second-fleet`,
  `act-1-outro-rook`. They show the production Brood Carrier (`brood_carrier.py`: composed as the
  game draws it, nose-down and broadside, a turn frame; part G's D1 for the three stages), its
  break-up chunks and Level 07's ichor clouds (`brood_carrier_death.py`, `backdrop_l07.py`),
  Level 06's Daedalus gate and domes, Rook's radio portrait (`portraits.py`) and his craft from the
  chosen round-09 render (`vfx_r08.py`; no production wingman sprite until the escort slot is built).
  The earlier seventeen images and sheets re-render byte-identical. Review sheet
  `briefing-images-final-r25-a` proposed for round 25. `art` stays `chosen`.
- 2026-10-05: Round 25 closed (user): the two Level 07 images and the four Act 1 outro images
  approved as **final** as they are; `art` stays `chosen`, as after round 20 (the images of the
  later levels and the act title cards' stills do not exist yet). The carcass image still shows the
  head chunk's eye lights from before the carcass fix; a trial re-render with the current death
  art moved the chunks (one over the header's "AFTER"), so the approved file was kept and a
  re-render is left for a later round.
- 2026-10-05: `act-1-outro-carcass` re-rendered with the current carcass (the round-25 close left
  it for later): the eight break-up chunks now take their last tumble frame (the cut flesh cooled,
  no lime glow, no eye lights) instead of a random one, and only their placement changed. The
  image had drawn the death data's offsets with y down although they are y up, so the carrier was
  mirrored top to bottom; the pieces now assemble the right way up, each moved by its drift less
  the carcass's common drift (1.6× across, 1× up and down, 4 px high), so they come apart round
  the bracket's middle, all inside it, clear of the title and the labels. Scale, labels, colours,
  Earth, Luna, the ichor clouds and the debris dots are unchanged; the other twenty-two images stay
  byte-identical. Review sheet `briefing-images-final-r25-a` rebuilt.
- 2026-10-06: Briefing images of the Act 2 intro and Level 08 (M5 part B, straight to production
  per D6, `tools/art/briefing_images.py`), one per page (D3), named in the act's and the level's
  data: `act-2-landfall`, `act-2-front-lines`, `act-2-over-home`, `act-2-scramble`,
  `level-08-nova-lagos`, `level-08-walker-scan`. They show the production Creeper with its gland
  glow (`creeper.py`, the walk frame at heading 4), the Vrell orb (`enemy_bullets.py`), the bomb
  rack's shot (`weapon_fx.py`), the Stormhawk and Rook's craft, now the production Ember
  sprite (`rook.py`; the Act 1 outro image keeps the round-09 render it was approved with). The
  globe display is a flat projection of hand-drawn coastlines, enough to read the continents; the
  street view a perspective drawn in the display's lines. The earlier twenty-three images and their
  sheets re-render byte-identical. Review sheet `briefing-images-final-r30-a` proposed for round
  30; `art` stays `chosen`.
- 2026-10-07: From the Level 08 capture (round 30). From Act 2 on, a level's briefing page fits
  one screen (4 lines below its image), as every act page already does; Act 1's level pages were
  written and voiced to go on over the next screens and keep doing so, as the act outros do
  (`BriefingLayoutTest.anActPageAndALevelPageFromAct2OnFitOneScreen`). An act outro's NEXT panel and
  its last page's hint follow the route: the next act's title card and the next briefing when the
  next level opens an act with data (`ACT II: HOMEFRONT, THEN THE BRIEFING FOR MISSION 08: NEON
  SKYLINE`, `ENTER TO ACT II`), the next briefing within an act (`--act-end`), the hangar only when
  the next level has no data (`OutroOnwardTest`).
- 2026-10-07: `level-08-walker-scan` re-rendered with the Creeper's readability lift (round 30:
  lighter chitin and legs, back pores, light rim), so the scan shows the Creeper the level flies; the
  other twenty-eight images stay byte-identical. Review sheet `briefing-images-final-r30-a` rebuilt.
- 2026-10-07: [Concept round 30](../../concept-rounds/round-30/README.md) closed (user,
  2026-10-07): the six briefing images of the Act 2 intro and Level 08 approved as **final**
  (`level-08-walker-scan` with the Creeper's lift; the rough coastlines, the less holographic
  "over home", the busy convoy fans and the agent's numbers on the images as they are). The rule
  found in the capture is **kept**: from Act 2 on, an act page and a level page each fit one screen
  (4 lines below the image, `BriefingLayoutTest.anActPageAndALevelPageFromAct2OnFitOneScreen`), so
  later acts say less per page or use more pages; Act 1's level pages keep running on over the next
  screens, as written and voiced. `art` stays `chosen`.
- 2026-10-07: Level 09's briefing images (M5 part C, straight to production per D9 = a,
  `tools/art/briefing_images.py`), named in the level's data: `level-09-arcology-district` and
  `level-09-node-scan`; review sheet `briefing-images-final-r31-a` proposed for round 31.
- 2026-10-07: [Concept round 31](../../concept-rounds/round-31/README.md) closed for the briefing
  images (user, 2026-10-07): Level 09's two images, `level-09-arcology-district` (corner label
  "LAST HOUR OF NIGHT") and `level-09-node-scan`, approved as **final** (the small cluster letters
  and the level's numbers on the images as they are). `art` stays `chosen`.
- 2026-10-08: Level 10's briefing images (M5 part D, straight to production per D10 = a,
  `tools/art/briefing_images.py`): `level-10-evacuation-route` and `level-10-wraith-scan`; review sheet
  `briefing-images-final-r32-a` proposed for round 32.
- 2026-10-08: [Concept round 32](../../concept-rounds/round-32/README.md) closed for the briefing
  images (user): Level 10's two images, `level-10-evacuation-route` (with shuttle a, the picked one,
  so no redraw) and `level-10-wraith-scan`, approved as **final**. `art` stays `chosen`.
- 2026-10-09: [Concept round 33](../../concept-rounds/round-33/README.md) closed for the briefing
  images (user): Level 11's two images, `level-11-convoy-route` (with ship pair a, the picked one, so
  no redraw) and `level-11-sub-scan`, approved as **final**, the weak spots as they are (the faint
  relief, the small ships and reefs, the route under the platform). `art` stays `chosen`.
- 2026-10-12 (user, [round 34](../../concept-rounds/round-34/README.md)): Levels 12–14's six briefing images
  (12.2d, 13.2d, 14.2f) approved as **final**, and the two-page briefings (C5) accepted. `art` stays
  `chosen`.
- 2026-10-14 (M7 part B, step ART-B): the Act 3 intro's four and Level 15's two images rendered by `tools/art/briefing_images.py` (key `r38`), review sheet `briefing-images-final-r38-a` proposed for round 38; the earlier images stay byte-identical. Pending the user's review: the colony positions (Ares Landing 31°W 16°N, Tharsis Foundry 106°W 6°N), 12 beacons with 1 answering, the rust display ramp for Act 3.
- 2026-10-14 (user, round 38): the Act 3 intro's four and Level 15's two images `briefing-images-final-r38-a`
  approved as final (F.8); the intro pages wearing Level 15's header and objectives (K.2) accepted as
  built. `art` stays `chosen`.
- 2026-10-18 (user, round 39): Level 16's two briefing images `briefing-images-final-r39-a` approved as final
  (F.6). `art` stays `chosen`.
- 2026-10-18 (user decision, Act 2 playtest): every mission starts with its briefing, then the
  hangar: Continue, Load game and the mission select replay now open the briefing first (see the
  [screen flow](../README.md#screen-flow)).
- 2026-10-19 (user, round 40, F.g): Level 17's two briefing images approved as final.
- 2026-10-20: round 42 closed: Level 19's briefing images and Level 20's filed plate approved as final by the user (F.f).
- 2026-10-20: round 43 closed: Okafor's `level-20-hellas-fallback` image approved as final by the user (F.e; it is listed with Level 20's art). `art` stays `chosen`: Q.5, the rest of the briefing art moves to final only item by item, as the user approves it (an audit step lists what is left for the next round).
- 2026-10-20: Level 21's briefing images and the Act 3 outro's four images made as production art (step ART-H3); pending the user's review in round 44.
- 2026-10-20 (user, round 44, A.a): the audit approved: `briefing-r08-a` and `act-title-r08-a` (superseded mockups) moved to `concept/rejected/`; the 19 review sheets and captures of production assets (`briefing-final-r13-a`, its capture and the 17 briefing-image sheets r13-r42) marked `final`; Level 21's two briefing images (F.e) and the Act 3 outro's four images (F.f) approved as final; **`art: final`** for the briefing. A later addition needs its own row.
- 2026-10-20 (user, round 44, X.l): the act-title lettering gets a `tools/art` generator, the pixels unchanged where the user approved them (planned, step ART-K; ticked in the Implementation list when built).
- 2026-10-22 (M8 part B, step ART-B; accepted in round 51 (2026-10-22)): the Act 4 intro's four images and Level 22's two
  (`briefing-images-final-r51-a`, `tools/art/briefing_images.py`).
- 2026-10-22 (user, round 51, closed; F.7): the Act 4 intro's four images and Level 22's two accepted as final.
- 2026-10-22 (user, round 52, closed): Level 23's two briefing images accepted as final (F.9).
- 2026-10-25 (user, [round 57](../../concept-rounds/round-57/README.md) closed): Level 27's two briefing images accepted as final (F.7).
- 2026-10-26 (user, [round 58](../../concept-rounds/round-58/README.md) closed): Level 28's two briefing images and the Act 4 outro's four accepted as final (F.7, F.8; the Helix Dynamics emblem as drawn).
- 2026-10-27 (Act 5 design phase, step m9-d-systems; **approved in round 60**): the intercepted
  page drafted for the Act 5 intro's broadcast (Q9 = a): a steady interference over portrait and image, the
  `INTERCEPTED · ALL CHANNELS` header, the amber speaker plate and text, a carrier hiss instead of the typing blip,
  `channel: enemy` in the data. Alternatives of Q9 (all four pages Vorne; or no intercepted page, Vorne as radio
  lines at L29's start) change only how many pages use it.
- 2026-10-27 (user, [round 60](../../concept-rounds/round-60/README.md) closed): the intercepted briefing pages approved as drafted (D.17; Q9 **a**: Vorne's broadcast as the act intro); the intercept sounds are S.11 a, S.12 a and S.13 a.
- 2026-10-28 (user, [round 62](../../concept-rounds/round-62/README.md), closed; page answers): the six Act 5 briefing images (F.10: the intro's four and Level 29's two) accepted as final.
- 2026-10-28 (user, rounds [61](../../concept-rounds/round-61/README.md) and [63](../../concept-rounds/round-63/README.md), closed): the intercepted page's amber look accepted (round 61 C.6); Level 30's two briefing images accepted (round 63 F.7).
- 2026-10-28 (user, [round 64](../../concept-rounds/round-64/README.md), closed): **F.10** accepted: Level 31's two briefing images (Okafor's nav chart, Varga's tug diagram) are final.
- 2026-10-29 (user, [round 65](../../concept-rounds/round-65/README.md), closed): **F.6** ok: Level 32's two briefing images (Okafor's shaft chart, Varga's arc and fall-line diagram) accepted as final.
- 2026-10-29 (user, rounds [round 67](../../concept-rounds/round-67/README.md) and [round 68](../../concept-rounds/round-68/README.md), closed): **G.6** ok and **H.6** ok: Level 34's and Level 35's two briefing images each accepted as final.
