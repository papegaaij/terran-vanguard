#!/usr/bin/env python3
"""Production art: the briefings' tactical maps and mission images (design/ui/briefing), one per
briefing page of the Act 1 intro, Levels 01-07, the Act 1 outro, the Act 2 intro, Levels 08-14, the Act 3 intro and
Level 15, in the chosen briefing-r08-a look: a dark
tactical display with its grid, scan rows and edge ticks, holographic planets, cyan routes and
labels, red and violet for the Vrell, amber for objectives.

Outputs (assets/ui/briefing/<name>.png, 672x240, textures of their own like the title scene):
  act-1-tether-gate       Act 1 p1: the Tether Gate beyond Neptune, lit
  act-1-outer-stations    Act 1 p2: the outer system's stations, contact lost one by one
  act-1-l1-strike         Act 1 p3: the strike group at Earth-Moon L1, its vector to the Gagarin yards
  act-1-aegis-wing        Act 1 p4: Earth orbit's squadrons; only Aegis Wing makes the intercept
  act-1-stormhawk         Act 1 p5: the AF-12 Stormhawk as a schematic
  level-01-gagarin-yards  L01 p1: the yards, the south rail (Lancer) and north rail (Aegis Two)
  level-01-vrell-scan     L01 p2: Varga's scan of the Skitter and Needler, the glowing parts
  level-02-burning-yards  L02 p1: the burning south arm, the four crewed drydocks and their growths
  level-02-yield-signal   L02 p2: the transmission's repeating pattern, the turret's blind arc
  level-03-spore-lanes    L03 p1: the high lanes over last week's battle site and its debris field,
                          the spore carriers seeding Earth, Lancer's lane
  level-03-spore-echo     L03 p2: the Spore Bomber below, its spores rising into a wide spread, the
                          long-range echo (the Leviathan as a noisy silhouette)
  level-04-convoy-road    L04 p1: Tranquility Base and the brood pods, the five crawlers on the road
                          across the rille to the mass-driver terminal, walkers in the craters,
                          Hammer flight (the Airstrike)
  level-04-walker-scan    L04 p2: the Scuttler facing where it walks, claws forward, the glowing back
  level-05-crater-nest    L05 p1: the nest crater beside the mass-driver line, the four batteries A-D
                          on its floor, Lancer's run down the rail and over the rim, the rail's sleds
  level-05-mortar-scan    L05 p2: the Polyp Mortar's lob arcing to its lime marker a second ahead, the
                          ship moving out of it; an unknown contact holding in orbit over the crater
  level-06-daedalus-rim   L06 p1: the far side across the terminator, the silent settlements, Daedalus
                          Rim's lit domes, Lancer's run into the dark by headlight along the ore rail
  level-06-edge-scan      L06 p2: the Mantis at the screen edge sweeping its beam from its eye (the
                          production telegraph wedge and charged-lance beam), side-firing guns
                          reaching it; the Coilwyrm at the data's spacing coming round behind the
                          ship (its path history dashed beyond the tail), its open rear
  level-07-l1-carrier     L07 p1: the Brood Carrier (broadside) holding at Earth-Moon L1, the pods
                          that hit Luna traced back to it, its escort screen, the overrun L1 picket,
                          Aegis's way through; its weak points named
  level-07-overhead-scan  L07 p2: the carrier's three stages (part G's D1): nose-down on high air with
                          its shadow, a bay pair open, only missiles reaching up; the turn in place
                          (a turn frame as a hologram); broadside at the ship's level, a pair open,
                          a pair burst, the plate iris open over the lime core
  act-1-outro-carcass     Act 1 outro p1: the carcass at L1 (the break-up chunks, dead, burnt dark, drifting,
                          ichor clouds), Earth beyond, what the act saved
  act-1-outro-daedalus-rim  outro p2: Daedalus Rim still empty (Level 06's gate and domes, lights on),
                          the search's tally, the file kept open
  act-1-outro-second-fleet  outro p3: the long-range plot: the second fleet beyond the Moon, ten times
                          the destroyed carrier group, its track ending in Earth's atmosphere
  act-1-outro-rook        outro p4: Aegis Wing reassigned to Earth defence, Rook's craft (Ember) in the
                          wing slot beside Lancer's Stormhawk, his radio portrait
  act-2-landfall          Act 2 p1: the landers coming down at dawn below the cloud deck over the Gulf
                          of Guinea, 34 burning trails over the sea, the CDF tracking overlay counting
                          them (12 days tracked, none stopped), the three landing zones
  act-2-front-lines       Act 2 p2: the CDF global display (a flat projection of hand-drawn coasts),
                          the three landing zones glowing, the fronts numbered: the cities (Nova Lagos,
                          Geneva Concord), the Atlantic sea lanes, the Arctic relay chain
  act-2-over-home         Act 2 p3: a Nova Lagos street from rooftop height in one-point perspective,
                          people on the low roofs looking up and waving, Lancer passing low
  act-2-scramble          Act 2 p4: Aegis Wing on a coastal CDF airbase at dusk, Lancer, Rook's Ember
                          and three Stormhawks on their pads, canopies closing; Nova Lagos burning 40 km
                          off
  level-08-nova-lagos     L08 p1: the night route: the harbour, the elevated highways, the tower
                          district, the Third Mainland highway over the lagoon to the Ikoyi shelters,
                          the walkers heading for them; Lancer and Rook at the start
  level-08-walker-scan    L08 p2: the Creeper on a low roof (its gland and eye glow added), its aimed
                          five-way fan of orbs, a convoy's fans 0.5 s apart front to back, the
                          anti-ground x2 marker
  level-09-arcology-district  L09 p1: the arcology district before dawn, the route, clusters A-C
  level-09-node-scan      L09 p2: the Hive Node (hardened, its iris cycle), the Ravager's pounce arc
  level-10-evacuation-route  L10 p1: the corridor at first light, west to east: Eko spaceport's five
                          pads with the shuttles, the jammed suburbs, the maglev viaduct between the
                          creep districts, the coast road and the capsized ferry, the lagoon and the
                          climb-out; the route, Lancer and Rook, contacts closing from behind
  level-10-wraith-scan    L10 p2: the Wraith cloaked (its additive shimmer) and decloaked (over its
                          flash); its pass overhead, loop and rear entry with the 3 s warning, its
                          bursts up through the shuttle band; a Mote Swarm's flock and loop-back
  level-11-convoy-route   L11 p1: the North Atlantic from above: Convoy Atlas-Seven (three cargo hulls and
                          the frigate, production ships heading east), the seeded sea lanes with their
                          Driftjellies, the reef line with its gun rafts, Platform Tiamat on the route
                          with the Kraken's gripping arms and its hologram under it, open water beyond
  level-11-sub-scan       L11 p2: a cut-away at the waterline: a Driftjelly surfaced (guns reach it) and
                          submerged (they do not), the shots stopping at the waterline, a torpedo's run
                          under the surface, a Reef Spitter raft and its roots, the unknown large contact
  level-12-storm-front    L12 p1: the storm system from above off the Azores, its cloud bands winding
                          clockwise into the eye, moving east onto the convoy lanes, Vrell flyers in it;
                          Lancer and Rook's route in through the wall, across the eye and out
  level-12-sonde-scan     L12 p2: a CDF weather sonde (the production sprite) blinking in the rain, the
                          fly-through ring and the SONDES tracker; the pressure plot that makes no sense
                          (a regular saw where a storm's curve should be)
  level-14-spire-over-geneva  L14 p1: Geneva Concord from the lake as a hologram elevation: the lake
                          and the Jet d'Eau, the old town's skyline and Saint-Pierre, the Siege Spire's
                          trunk three kilometres into the canopy over the old town, the route in along
                          the lakefront to the Concord rotunda, Lancer and Rook
  level-14-root-scan      L14 p2: the final backdrop's Root Field as a scan, its roots traced and the
                          burst points marked; one burst from the production frames (the 1 s of dust,
                          the root with its Spine Turret and Polyp Mortar); a Wraith from behind
  act-3-arrival           Act 3 p1: Mars filling the transport's forward view (an orthographic globe from a
                          simplified map of the real planet: the Tharsis volcanoes, the Valles Marineris, the
                          craters and dark regions, the thin limb haze), framed by the canopy struts and the nav
                          screen; the CDF overlay on the glass with the colonies' beacons, eleven dark, Ares
                          Landing's lit, the Vrell over it
  act-3-mars-front        Act 3 p2: Varga's CDF globe display of Mars as a flat projection (the same map's relief
                          in Act 3's rust display ramp): the Hellas basin glowing Vrell red, the growth spreading
                          north from it in tendrils, Ares Landing on the edge of the northern plains, Tharsis
                          Foundry on the plateau, the Valles Marineris between them, Olympus Mons
  act-3-colonists         Act 3 p3: a militia gun pit on Bradbury dome's ring at dusk under the butterscotch sky
                          (the sun down behind the mesas in its small blue halo): the gunner in a dust-orange suit
                          (the dust-suit controller's look, generic-colonial-r36-b: goggles on the brow, the
                          respirator at the throat, an air pack) leaning back at a twin autocannon, looking up
                          at the Vrell overhead; the dome's geodesic glass behind him (dome a of design/world/mars)
                          with its patched panels: plates and taped cracks
  act-3-drop              Act 3 p4: the transport's drop bay open over Mars: Level 15's production `transport`
                          piece (tools/art/backdrop_l15.py) turned nose right, the planet through the open bay,
                          Lancer, Rook and two Stormhawks on its launch rails, a callout of the rails at full size
                          (Lancer, Aegis Two and Aegis 4; Rook and Aegis 3); below, Mars as Level 15 draws it
                          (`mars-high` with the `olympus` piece, hazed), the cloud deck's banks (`deck-banks`) and
                          the cirrus (additive at 40 %)
  level-15-descent-route  L15 p1: the entry corridor as a CDF profile plot west to east: the transport in orbit
                          (the Act 2 outro's heavy transport model, tools/art/outro_l14_images.py, as a hologram),
                          the descent over Olympus Mons' caldera (22 km), through the cirrus and the cloud deck
                          along the flank, over the basal escarpment with Lycus Survey Station at its foot, the
                          Tharsis dunes and the rail line's pylons, east toward Ares Landing; the level's five
                          sections numbered on the route; Lancer and Rook
  level-15-burrower-scan  L15 p2: Varga's cut through the regolith: a Burrower swimming under the sand (a scan
                          silhouette of tools/art/burrower.py's sprite, its ripples on the surface above), the
                          guns stopping at the surface over it; the bulge (the 1 s tell) with it rising under
                          it; the surfaced maw over its bow wave, its lime glow added, firing its ring of ten
                          orbs; the dive; the anti-ground x2 marker
  level-17-ares-domes     L17 p1: Okafor's plan of Ares Landing, north to the left: the rail line, the outer
                          domes Bradbury and Lowell, the burnt greenhouses, the inner ring road round
                          Schiaparelli, Burroughs and Eden, the spaceport; red Vrell vectors on the domes (the
                          ally's sprites, tools/art/siege_units.py); an unnamed black-and-gold compound
  level-17-choir-herald   L17 p2: Varga's scan of a Choir Herald, its crest lit, the dashed aura and threads to
                          three ringed Needlers; an inset of a Shellback mid-curl with its lime vent marked
  level-18-storm-front    L18 p1 (M7 part E): Okafor's orbital weather view of the northern plains, north to the
                          left: the storm front as a rust wall with its calm eye, Vrell inside it, its drift
                          toward Ares Landing's domes at its southern edge, the sweep route through it with the
                          level's six sections
  level-18-visibility     L18 p2 (M7 part E): Varga's diagram of the storm's look (the chosen radius look b,
                          from tools/art/backdrop_l18.py's storm plain, veil and tongue textures): the ship in
                          its clear radius, the ground fading into the dust outside it, Dust Kites and a Needler
                          as dark silhouettes keeping their glow, their bullets crisp at any range, a homing
                          trail finding one; no Ghost Drone (its first sight is the level's hint)
  level-19-tharsis-works  L19 p1 (M7 part F): Okafor's CDF site plan of the Tharsis Foundry, north to the left: the
                          slag fields, the two conveyor halls and their belts, the furnace row (stacks, vent rows),
                          the yard gate, the casting yard, the south gate; the smelter tower with its 200 crew; red
                          Vrell vectors and grey, unlabelled contacts on the conveyor lines; the six sections
  level-19-rear-arcs      L19 p2 (M7 part F): Varga's diagram over the level's hall floor: a gantry turret on its
                          cradle (tools/art/l19_props.py) with its arc up the line, silent above the ship, firing once
                          passed; a Warden Tank (tools/art/warden_tank.py) riding a belt, its turret tracking back
                          at the ship, its rear plate circled; the casting yard's thermal: one large blurred contact
                          (the walker's sprites blurred)
  level-20-filed-plate    L20 p2 (M7 part F batch, shared with Level 19's wreck): the Revenant Walker's flank plate
                          close up, rendered from tools/art/revenant_walker.py's wreck model (the plate found by a
                          probe render), the serial field filed to bright bare metal with hand-file strokes; an
                          assay strip clipped beside it, the stock match circled in red pen
  level-20-hellas-fallback  L20 p1 (M7 part G batch): Okafor's CDF terrain plot of the Hellas basin's north side,
                          north up: the rim's lip with the militia's guns, the rim road's switchbacks, the upper
                          terrace, the terraces as contours, the floor with the Vrell staging grounds; the strike
                          route in (solid), the fallback back up the terraces and the road (dashed), the sections
                          (shown on the level's props review, l20-props-final-r43-a; no briefing sheet of its own)
  level-21-hellas-basin   L21 p1 (M7 part H batch): Okafor's CDF plot of the Hellas basin, north up: the rim with
                          the CDF line held at the end of the retreat, the inner wall's terraces as contours, the
                          dune sea's crests with a moving seismic contact, the lime hollow at the lowest point with
                          seismic rings centred on it; the descent route and the five sections
  act-4-arrival           Act 4 p1 (M8 part B batch): Europa in the transport's forward view against Jupiter (its
                          lineae and chaos patches, lit from the upper left), the Vrell fleet's ring of contacts in
                          orbit, the four rigs on the ice at Conamara and the station's beacon; the nav screen's days
                          and tremors
  act-4-europa-cutaway    Act 4 p2: Varga's cut through Europa's shell: the ice kilometres thick, the ocean under
                          it, Thera Deep's habitat (40,000 people) and its kelp farms, the rigs boring down from
                          Conamara on the surface toward the ocean
  act-4-hydro-kit         Act 4 p3: the hydro-kit Stormhawk (tools/art/act4_gear.py's ship-hydro_2 at 4x) with the
                          callouts (sealed intakes, torpedo rack, pressure hull, headlights); the water's rules (x0.7
                          bullets, a third of the bite off all but torpedoes and depth charges) beside it
  act-4-drop              Act 4 p4: the drop over Europa: Level 22's production rig-field tile from above, the
                          Level 15 transport piece turned nose right with Aegis Wing leaving it, the route over the
                          four rigs to the hole they bored, "we follow it down"
  level-22-conamara-map   L22 p1: Okafor's CDF map of the Conamara Chaos, north up, from Level 22's production
                          tiles as a hologram: the route south over the shelf, the four rigs, the station's dome at
                          the south end, the three sections; Jupiter in an inset
  level-22-drill-rig      L22 p2: Varga's scan of the production Drill Rig (tools/art/drill_rig.py: collar, four
                          pumps on their hoses) with the bore core marked, the hardened collar (shots glance off),
                          what gets through; an inset of a geyser's crack glowing, then its steam
                          (tools/art/l22_props.py)
  level-23-drill-shaft    L23 p1 (M8 part C batch): Okafor's CDF section through the ice under Conamara: the dome on
                          the shelf, the Vrell drill shaft down twenty kilometres of ice with their collars in red,
                          the waterline where the hydro-kits switch on, Thera Deep's lit domes and farm racks with
                          the siphon pumps on the floor below; the route and the four sections
  level-23-eel-and-fry    L23 p2: Varga's scan of the Eel Swarm (tools/art/eel_swarm.py's frames: the gills
                          circled, the stripe lit, a cut) and a Glassfin school as a ribbon (tools/art/glassfin.py:
                          a glint, the ram); the rules strip BULLETS X0.7, DRAG, TORPEDO RANGE +50 %
  level-24-abyssal-plain  L24 p1 (M8 part D batch): Okafor's sonar map of the abyssal plain, north up: Thera Deep's
                          last lamps at the north edge, the silt as sonar contours, the four spawning reefs as red
                          clusters, the three relay beacons as dashed amber marks in a line, the trench's mouth at the
                          south end; the route in teal and the four sections
  level-24-abyss-ray      L24 p2: Varga's scan of the Abyss Ray: a depth cut (the play plane, the Ray's shadow far
                          below, its rise, the lime gill slits), the production Ray (tools/art/abyss_ray.py) from
                          above with its gill slits circled and a fan of nine; a Barbfin inset at 2x
                          (tools/art/barbfin.py: the barbed snout, the violet needles)
  level-25-siren-trench   L25 p1 (M8 part E batch): Okafor's sonar chart of the trench, north up (tools/art/
                          backdrop_l25.py's walls, notches, pillar and beacons): the walls as the collision lines, the
                          side caves as notches, the pillar's fork and the narrows, the Siren nests as teal dots on
                          both walls, the red-gold beacons, the survey sub's cave; the route in teal and six sections
  level-25-sonar-ring     L25 p2: Varga's diagram of the Siren's ring: the production nest on a wall (tools/art/
                          siren.py) with its sac, two rippled rings and the gap between them, the ship slowed in the
                          band with its shimmer (60 %, 2 s), a speed strip with the 3 s immunity
  level-26-blackwater     L26 p1 (M8 part F batch): Okafor's chart of the Blackwater, north up: the trench's mouth and
                          its last beacons at the top, the drowned pipeline's last lamps, then nothing charted (no
                          sonar return, the CDF's survey buoys lost and crossed out at the chart's edges), a dotted
                          route down the middle, the vent field's red glow far to the south
  level-26-lure           L26 p2: Varga's diagram of the Glow Angler as the dark shows it (tools/art/glow_angler.py:
                          the bead on its stalk and the eyes, the body a faint rim), a gold pickup beside it, the
                          ship's headlight cone from below, the 130 px lunge radius and the ring it fires
  level-21-colossus-scan  L21 p2 (M7 part H batch): Varga's seismic cut through the basin floor (the body under
                          the sand, colony-sized, the ridge its back raises, a mouth breaching at the cut's edge with
                          the lime gullet) beside a breach seen from above at the field's left edge: the Dust
                          Colossus's mouth (tools/concept/bosses_r36.py's chosen model b, rendered here) with its
                          gullet, the bite circle and the suction's lines toward it, the ship for scale
  design/ui/briefing/concept/briefing-images-final-r13-a.png   review sheet, Act 1 intro + L01-02
  design/ui/briefing/concept/briefing-images-final-r20-a.png   review sheet, L03-04 (M4 batch)
  design/ui/briefing/concept/briefing-images-final-r21-a.png   review sheet, L05 (M4 part E)
  design/ui/briefing/concept/briefing-images-final-r23-a.png   review sheet, L06 (M4 part F)
  design/ui/briefing/concept/briefing-images-final-r25-a.png   review sheet, L07 + Act 1 outro (part G)
  design/ui/briefing/concept/briefing-images-final-r30-a.png   review sheet, Act 2 intro + L08 (M5 part B)
  design/ui/briefing/concept/briefing-images-final-r31-a.png   review sheet, L09 (M5 part C)
  design/ui/briefing/concept/briefing-images-final-r32-a.png   review sheet, L10 (M5 part D)
  design/ui/briefing/concept/briefing-images-final-r33-a.png   review sheet, L11 (M5 part E)
  design/ui/briefing/concept/briefing-images-final-r34-a.png   review sheet, L12 (M5 part F)
  design/ui/briefing/concept/briefing-images-final-r35-a.png   review sheet, L13 (M5 part G)
  design/ui/briefing/concept/briefing-images-final-r36-a.png   review sheet, L14 (M5 part H)
  design/ui/briefing/concept/briefing-images-final-r38-a.png   review sheet, Act 3 intro + L15 (M7 part B)
  design/ui/briefing/concept/briefing-images-final-r40-a.png   review sheet, L17 (M7 part D)
  design/ui/briefing/concept/briefing-images-final-r41-a.png   review sheet, L18 (M7 part E)
  design/ui/briefing/concept/briefing-images-final-r42-a.png   review sheet, L19 + the L20 plate (M7 part F)
  design/ui/briefing/concept/briefing-images-final-r44-a.png   review sheet, L21 (M7 part H)
  design/ui/briefing/concept/briefing-images-final-r51-a.png   review sheet, Act 4 intro + L22 (M8 part B)
  design/ui/briefing/concept/briefing-images-final-r52-a.png   review sheet, L23 (M8 part C)
  design/ui/briefing/concept/briefing-images-final-r53-a.png   review sheet, L24 (M8 part D)
  design/ui/briefing/concept/briefing-images-final-r54-a.png   review sheet, L25 (M8 part E)
  design/ui/briefing/concept/briefing-images-final-r55-a.png   review sheet, L26 (M8 part F)
 Every image is composed
in layers like the hangar map (tools/art/ui_scenes.py): the display and planets posterized to 24
colours with ordered dither, the lines, markers and labels to 16 of their own, then the sprites
from assets/ (the Stormhawk, Skitter, Needler, the Brood Carrier's composed poses, chunks and glows,
backdrop pieces, the portraits, the Creeper, the orb, the bomb, Rook's production Ember craft) with
their own palettes; the Act 1 outro's Rook craft from vfx_r08's chosen round-09 render, as approved;
additive light (the Mantis's beam parts from assets/, its wedge from mantis_beam.py's generator
code; the carrier's core glow, also on its open sacs) added as the game blends it, its pixels to 32 colours of their own.
The labels use the concept pixel font (render/raster.py), as the chosen mockup does.

Run: python3 tools/art/briefing_images.py [name ...] [r13|r20|r21|r23|r25|r30|r31|r32|r33|r34|r35|r36|r38|r39|r40|r41|r42|r43|r44|r51|r52|r53|r54|r55] [--review]   (~15 s; after
stormhawk.py, vrell_air.py, intel.py, vrell_l03.py, leviathan.py, vrell_l04.py, civilian_crawler.py
airstrike_bomber.py, l05_hazards.py, mantis.py, mantis_beam.py, coilwyrm.py, l06_darkness.py,
backdrop_l06.py, brood_carrier.py, brood_carrier_death.py, backdrop_l07.py, portraits.py, rook.py,
creeper.py, enemy_bullets.py, weapon_fx.py, hive_node.py, ravager.py, wraith.py, mote_swarm.py and
shuttle.py, convoy_ships.py (switch the ship pair: its --variant b, then this script again),
driftjelly.py, reef_spitter.py, harbour_kraken.py, backdrop_l11.py, l12_props.py and backdrop_l12.py, skimmer.py,
relay.py, backdrop_l13.py, siege_spire.py, root_burst.py --assets, backdrop_l14.py, burrower.py and backdrop_l15.py, whose sprites
and pieces it shows); the review sheet written is the open round's (r44) unless
a round is named.
"""
import json
import math
import sys

import numpy as np
import yaml
from PIL import Image, ImageDraw

import artkit
import brood_carrier as bc
import mantis_beam
import ui_scenes
import vfx_r08 as v8  # noqa: E402  (concept script, imported unchanged: Rook's craft, as stormhawk.py's ship)
from artkit import DESIGN, ROOT, SPRITES, sprite

from render import raster, terrain  # noqa: E402

SCRIPT = "briefing_images.py"
SOURCE = artkit.source_note(SCRIPT, "UI batch")
SOURCE_M4 = artkit.source_note(SCRIPT, "M4 briefing images")
SOURCE_M5 = artkit.source_note(SCRIPT, "M5 part B batch")
SOURCE_M5C = artkit.source_note(SCRIPT, "M5 part C batch")
SOURCE_M5D = artkit.source_note(SCRIPT, "M5 part D batch")
SOURCE_M5E = artkit.source_note(SCRIPT, "M5 part E batch")
SOURCE_M5F = artkit.source_note(SCRIPT, "M5 part F batch")
SOURCE_M5G = artkit.source_note(SCRIPT, "M5 part G batch")
SOURCE_M5H = artkit.source_note(SCRIPT, "M5 part H batch")
SOURCE_M7B = artkit.source_note(SCRIPT, "M7 part B batch")
SOURCE_M7C = artkit.source_note(SCRIPT, "M7 part C batch")
ROUND = "r55"  # the open round; the sheet of an earlier batch: name its round (r13, r20, r21, r23, r25, r30, ..., r36)
OUT = ROOT / "assets" / "ui" / "briefing"
CONCEPT = DESIGN / "ui" / "briefing" / "concept"
W, H = 672, 240
DISPLAY_COLOURS = 24
LINE_COLOURS = 16
LIGHT_COLOURS = 32   # each additive light layer's own palette (the Mantis's telegraph wedge: 32 colours)
CYAN = (60, 220, 255)
CYAN_DIM = (40, 130, 180)
WHITE = (220, 240, 255)
AMBER = (255, 190, 60)
RED = (255, 70, 50)
VIOLET = (190, 120, 255)
GREEN = (120, 255, 160)
LIME = (180, 255, 70)   # the Brood Carrier's weak points
GREY = (110, 130, 150)
GRID = 32


class Board:
    """An image in its layers: ``base`` the display as float RGB with the ``grid`` on it, ``over``
    the lines, markers and labels, ``sprites`` pasted last with their own palettes; ``light`` the
    additive sprites (premultiplied on black, as the game blends them), ``under`` on the display
    below the lines or ``over`` the sprites, each with a palette of its own."""

    def __init__(self):
        yy, xx = np.mgrid[0:H, 0:W].astype(np.float64)
        self.yy, self.xx = yy, xx
        t = yy / H
        self.base = np.dstack([5 + 4 * t, 9 + 10 * t, 26 + 16 * t])
        self.over = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        self.draw = ImageDraw.Draw(self.over)
        self.sprites = []
        self.light = {"under": np.zeros((H, W, 3)), "over": np.zeros((H, W, 3))}
        self.grid = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        g = ImageDraw.Draw(self.grid)
        for x in range(0, W, GRID):
            g.line([(x, 0), (x, H)], fill=(40, 120, 170, 90 if x % (4 * GRID) == 0 else 40))
        for y in range(0, H, GRID):
            g.line([(0, y), (W, y)], fill=(40, 120, 170, 90 if y % (4 * GRID) == 0 else 40))
        for x in range(0, W, GRID // 4):
            g.line([(x, 0), (x, 2 if x % GRID else 5)], fill=(80, 190, 230, 150))
            g.line([(x, H - 1), (x, H - 3 if x % GRID else H - 6)], fill=(80, 190, 230, 150))

    # ------------------------------------------------------------------ the display layer

    def glow(self, cx, cy, radius, colour, strength=1.0):
        d2 = ((self.xx - cx) ** 2 + (self.yy - cy) ** 2) / radius ** 2
        self.base += (np.exp(-d2) * strength)[..., None] * np.array(colour, float)

    def planet(self, cx, cy, r, kind, seed=7):
        """A holographic planet: Earth's relief, the Moon's or a gas giant's bands through a ramp,
        lit from the top left, with a rim glow."""
        d = np.hypot(self.xx - cx, self.yy - cy)
        inside = d < r
        if kind == "earth":
            tex = terrain.earth_coast(W, H, seed=seed, period=False, cell=max(24, int(r)),
                                      stops=terrain.coast_stops_from(ui_scenes.SEA, ui_scenes.LAND, 0.5))
            lum = np.array(tex.convert("L")).astype(np.float64) / 255 * 1.3
            ramp = ui_scenes.HOLO
        elif kind == "moon":
            lum = 0.55 + 0.45 * raster.fbm(W, H, max(8, int(r // 2)), seed, octaves=4, period=False)
            ramp = [(0.0, (6, 10, 20)), (0.5, (30, 50, 70)), (1.0, (120, 150, 170))]
        else:
            lum = 0.6 + 0.25 * np.sin((self.yy - cy) / r * 9 + 0.6 * np.sin((self.xx - cx) / r * 3))
            ramp = [(0.0, (4, 8, 30)), (0.5, (20, 50, 120)), (1.0, (80, 140, 220))]
        nx, ny = (self.xx - cx) / r, (self.yy - cy) / r
        light = np.clip(0.35 - 0.55 * nx - 0.55 * ny + 0.5 * np.sqrt(np.clip(1 - nx ** 2 - ny ** 2, 0, 1)), 0.08, 1)
        body = raster.ramp(ramp, np.clip(lum * light, 0, 1))
        self.base = np.where(inside[..., None], body, self.base)
        self.base += (np.exp(-((d - r) / 3) ** 2) * 0.8)[..., None] * np.array([40, 150, 200])

    # ------------------------------------------------------------------ the line layer

    def label(self, x, y, text, colour=CYAN, right=False):
        if right:
            x -= raster.text_width(text)
        raster.draw_text(self.over, x, y, text, colour + (255,))

    def title(self, text):
        self.label(10, 9, text)

    def dashed(self, points, colour=CYAN, width=1, dash=6, gap=4):
        pts = np.array(points, float)
        for a, b in zip(pts[:-1], pts[1:]):
            length = np.hypot(*(b - a))
            for s in np.arange(0, length, dash + gap):
                e = min(length, s + dash)
                self.draw.line([tuple(a + (b - a) * s / length), tuple(a + (b - a) * e / length)],
                               fill=colour + (230,), width=width)

    def line(self, points, colour=CYAN, width=1, alpha=230):
        self.draw.line([tuple(p) for p in points], fill=colour + (alpha,), width=width)

    def arrow(self, x0, y0, x1, y1, colour=CYAN, size=7, dashed=True):
        """A route to (x1, y1) with its head."""
        if dashed:
            self.dashed([(x0, y0), (x1, y1)], colour, 2)
        else:
            self.line([(x0, y0), (x1, y1)], colour, 2)
        d = np.array([x1 - x0, y1 - y0], float)
        d /= np.linalg.norm(d)
        n = np.array([-d[1], d[0]])
        tip = np.array([x1, y1], float)
        self.draw.polygon([tuple(tip), tuple(tip - d * size * 1.6 + n * size * 0.8),
                           tuple(tip - d * size * 1.6 - n * size * 0.8)], fill=colour + (240,))

    def ring(self, cx, cy, rx, ry=None, colour=CYAN, alpha=200, width=1):
        ry = rx if ry is None else ry
        self.draw.ellipse([cx - rx, cy - ry, cx + rx, cy + ry], outline=colour + (alpha,), width=width)

    def arc(self, cx, cy, rx, ry, start, end, colour=CYAN_DIM, alpha=160):
        self.draw.arc([cx - rx, cy - ry, cx + rx, cy + ry], start, end, fill=colour + (alpha,))

    def marker(self, x, y, kind, colour, size=4):
        """Map symbols: ``square`` (friendly), ``diamond`` (objective), ``cross`` (lost), ``chevron``
        (hostile, pointing down), ``dot``."""
        d = self.draw
        c = colour + (255,)
        if kind == "square":
            d.rectangle([x - size, y - size, x + size, y + size], outline=c)
            d.rectangle([x - 1, y - 1, x + 1, y + 1], fill=c)
        elif kind == "diamond":
            d.polygon([(x, y - size - 1), (x + size + 1, y), (x, y + size + 1), (x - size - 1, y)], outline=c)
        elif kind == "cross":
            d.line([(x - size, y - size), (x + size, y + size)], fill=c, width=2)
            d.line([(x - size, y + size), (x + size, y - size)], fill=c, width=2)
        elif kind == "chevron":
            d.line([(x - size, y - size), (x, y), (x + size, y - size)], fill=c, width=2)
            d.line([(x - size, y - size + 4), (x, y + 4), (x + size, y - size + 4)], fill=c, width=1)
        else:
            d.ellipse([x - size, y - size, x + size, y + size], fill=c)

    def bracket(self, x0, y0, x1, y1, colour=CYAN, arm=6):
        for cx, cy, sx, sy in ((x0, y0, 1, 1), (x1, y0, -1, 1), (x0, y1, 1, -1), (x1, y1, -1, -1)):
            self.line([(cx, cy + sy * arm), (cx, cy), (cx + sx * arm, cy)], colour)

    def sprite(self, img, x, y):
        """A sprite (already palettised) with its centre at (x, y)."""
        self.sprites.append((img, int(x - img.width / 2), int(y - img.height / 2)))

    def add_light(self, rgb, x, y, layer="under"):
        """An additive sprite (an RGB array, premultiplied on black) with its top left at (x, y)."""
        mantis_beam.add(self.light[layer], np.asarray(rgb, np.float64), int(round(x)), int(round(y)))

    # ------------------------------------------------------------------ out

    def image(self):
        display = raster.to_rgba_image(np.clip(self.base, 0, 255))
        display.alpha_composite(self.grid)
        base = np.array(display).astype(np.float64)[..., :3] * np.where(self.yy % 2 == 1, 0.88, 1.0)[..., None]
        out = ui_scenes.dithered(base, DISPLAY_COLOURS)
        self._lit(out, "under")
        out.alpha_composite(self.over)
        lines = np.array(self.over)[..., 3] > 0
        rgb = np.array(out).astype(np.float64)[..., :3]
        out.alpha_composite(artkit.quantize_set([raster.to_rgba_image(rgb, lines * 255)], LINE_COLOURS)[0])
        for img, x, y in self.sprites:
            out.alpha_composite(img, (x, y))
        self._lit(out, "over")
        return out.convert("RGB")

    def _lit(self, out, layer):
        """Adds a light layer to ``out``; the pixels it touches get a palette of their own."""
        light = self.light[layer]
        lit = light.max(axis=-1) >= 1
        if lit.any():
            rgb = np.clip(np.array(out).astype(np.float64)[..., :3] + light, 0, 255)
            out.alpha_composite(artkit.quantize_set([raster.to_rgba_image(rgb, lit * 255)], LIGHT_COLOURS)[0])


def asset(name, zoom=1):
    return sprite.enlarge(Image.open(SPRITES / f"{name}.png").convert("RGBA"), zoom)


def holo(img, colour=CYAN, colours=12):
    """A sprite as the display draws it: its brightness in one hue, alpha kept."""
    a = np.array(img).astype(np.float64)
    lum = (a[..., :3] @ np.array([0.3, 0.55, 0.15]) / 255)[..., None]
    rgb = np.clip(np.array(colour) * (0.25 + lum * 1.1), 0, 255)
    return artkit.quantize_set([raster.to_rgba_image(rgb, a[..., 3])], colours)[0]


# --------------------------------------------------------------------------- the images

def tether_gate():
    b = Board()
    b.planet(96, 150, 46, "giant")
    b.glow(450, 120, 70, (60, 30, 120), 0.9)
    b.glow(450, 120, 26, (60, 200, 200), 0.8)
    b.title("TETHER GATE - TRANS-NEPTUNIAN, 41 AU")
    b.label(96, 204, "NEPTUNE", CYAN_DIM, right=False)
    for i, (rx, ry) in enumerate(((92, 40), (86, 36), (78, 32))):
        b.ring(450, 120, rx, ry, (VIOLET, CYAN, WHITE)[i], 230 - 40 * i, 2 if i == 0 else 1)
    for k in range(0, 360, 15):
        a = np.radians(k)
        b.line([(450 + 96 * np.cos(a), 120 + 43 * np.sin(a)), (450 + 104 * np.cos(a), 120 + 47 * np.sin(a))],
               VIOLET, 1, 200)
    b.dashed([(150, 140), (340, 124)], CYAN_DIM)
    b.label(170, 112, "1.8 BILLION KM", CYAN_DIM)
    b.label(W - 10, 40, "14 MAR 2185 06:12 UTC", AMBER, right=True)
    b.label(W - 10, 52, "GATE ACTIVE", RED, right=True)
    b.label(W - 10, 64, "DORMANT 44 YEARS", CYAN_DIM, right=True)
    b.label(10, H - 16, "DEEP SPACE ARRAY - LONG-RANGE OPTICAL", CYAN_DIM)
    return b


def outer_stations():
    b = Board()
    b.glow(-30, 120, 90, (120, 90, 30), 1.2)
    b.title("OUTER SYSTEM - STATION NETWORK")
    stations = [(170, "JUPITER", "CALLISTO RELAY", 3), (280, "SATURN", "TITAN HARBOUR", 7),
                (400, "URANUS", "OBERON WATCH", 9), (510, "NEPTUNE", "TRITON STATION", 11)]
    for x, planet, name, day in stations:
        b.arc(-30, 120, x + 30, 150 + x * 0.12, -40, 40)
        y = 120 + 30 * np.sin(x / 70)
        b.marker(x, y, "square", GREY)
        b.marker(x, y, "cross", RED, 6)
        b.label(x - 30, y + 14, name, CYAN_DIM)
        b.label(x - 30, y + 24, f"SILENT DAY {day}", RED)
        b.label(x - 30, 28, planet, CYAN_DIM)
    b.glow(630, 120, 22, (60, 30, 120), 1.0)
    b.ring(630, 120, 12, 18, VIOLET, 230, 2)
    b.label(W - 10, 150, "GATE", VIOLET, right=True)
    b.dashed([(612, 120), (540, 118)], VIOLET)
    b.label(W - 10, 9, "CONTACT LOST: 4 OF 4", RED, right=True)
    b.label(10, H - 16, "NO DISTRESS CALLS RECEIVED", CYAN_DIM)
    return b


def l1_strike():
    b = Board()
    b.planet(110, 130, 78, "earth", seed=19)
    b.planet(600, 110, 30, "moon", seed=5)
    b.title("CISLUNAR SPACE - EARTH-MOON L1")
    lx, ly = 470, 118
    b.ring(lx, ly, 30, colour=RED, alpha=200)
    b.ring(lx, ly, 16, colour=RED, alpha=140)
    for dx, dy in ((-10, -8), (6, -12), (12, 4), (-4, 8), (-14, 4), (2, -2)):
        b.marker(lx + dx, ly + dy, "dot", VIOLET, 2)
    b.label(lx - 40, ly - 50, "VRELL STRIKE GROUP", RED)
    b.label(lx - 40, ly - 40, "L1 - 06:41 UTC", AMBER)
    gx, gy = 196, 96
    b.marker(gx, gy, "diamond", AMBER, 5)
    b.label(gx + 12, gy - 26, "GAGARIN SHIPYARDS", AMBER)
    b.label(gx + 12, gy - 16, "HALF THE NEW FLEET IN DOCK", CYAN_DIM)
    b.arrow(lx - 34, ly - 6, gx + 12, gy + 2, RED)
    b.arc(110, 130, 86, 86, -80, 80, CYAN_DIM)
    b.label(300, 150, "ETA 40 MIN", RED)
    b.label(10, H - 16, "EARTH", CYAN_DIM)
    b.label(W - 10, H - 16, "LUNA", CYAN_DIM, right=True)
    return b


def aegis_wing():
    b = Board()
    b.planet(336, 520, 330, "earth", seed=23)
    b.title("EARTH ORBIT - CDF SQUADRONS")
    gx, gy = 336, 150
    b.marker(gx, gy, "diamond", AMBER, 5)
    b.label(gx - 50, gy + 12, "GAGARIN YARDS", AMBER)
    squadrons = [(110, 160, "PATROL 3", "ETA 2H 10M"), (210, 176, "PATROL 7", "ETA 1H 25M"),
                 (470, 176, "CUTTERS 2", "ETA 3H"), (570, 158, "PATROL 1", "REFIT")]
    for x, y, name, eta in squadrons:
        b.marker(x, y, "square", GREY)
        b.label(x - 24, y + 10, name, GREY)
        b.label(x - 24, y + 20, eta, GREY)
    b.marker(gx + 40, gy - 30, "square", CYAN)
    b.label(gx + 54, gy - 36, "AEGIS WING", CYAN)
    b.label(gx + 54, gy - 26, "READY - ETA 9 MIN", GREEN)
    vx, vy = 336, 34
    for dx in (-24, -8, 8, 24):
        b.marker(vx + dx, vy, "chevron", RED, 5)
    b.label(vx + 40, vy - 4, "STRIKE GROUP", RED)
    b.line([(gx - 120, gy - 52), (gx + 120, gy - 52)], AMBER, 2)
    b.label(gx + 126, gy - 56, "THE LINE", AMBER)
    return b


def stormhawk():
    b = Board()
    b.title("AF-12 STORMHAWK - SCHEMATIC")
    ship = holo(asset("ship_2", 4))
    cx, cy = 336, 124
    b.sprite(ship, cx, cy)
    b.ring(cx, cy, 104, 104, CYAN_DIM, 70)
    callouts = [((cx, cy - 86), (cx - 150, 40), "FRONT MOUNT - PULSE CANNON"),
                ((cx - 70, cy + 12), (cx - 290, 120), "L WING MOUNT"),
                ((cx + 70, cy + 12), (cx + 170, 120), "R WING MOUNT"),
                ((cx, cy + 80), (cx - 150, 206), "REAR MOUNT"),
                ((cx + 8, cy - 20), (cx + 170, 52), "SHIELD + ARMOUR")]
    for (px, py), (lx, ly), text in callouts:
        b.line([(px, py), (lx + (0 if lx > px else raster.text_width(text)), ly + 3)], CYAN, 1, 200)
        b.marker(px, py, "dot", CYAN, 2)
        b.label(lx, ly, text, WHITE)
    b.label(W - 10, 9, "IN SERVICE: 9 DAYS", AMBER, right=True)
    b.label(W - 10, H - 16, "PILOT: LANCER", CYAN, right=True)
    return b


def yards():
    """The Gagarin yards from above: the hub, the north and south arms with their docks."""
    b = Board()
    hub = (110, 120)
    b.ring(*hub, 22, colour=CYAN, alpha=220, width=2)
    b.ring(*hub, 10, colour=CYAN_DIM)
    b.label(hub[0] - 26, hub[1] + 30, "HUB", CYAN_DIM)
    for y0, name in ((78, "NORTH ARM"), (162, "SOUTH ARM")):
        b.line([(hub[0] + 20, hub[1] + (y0 - hub[1]) * 0.4), (150, y0), (640, y0)], CYAN, 2)
        b.line([(150, y0 - 6), (640, y0 - 6)], CYAN_DIM, 1, 120)
        b.line([(150, y0 + 6), (640, y0 + 6)], CYAN_DIM, 1, 120)
        b.label(590, y0 + (12 if y0 > 120 else -18), name, CYAN_DIM)
    return b


def gagarin_yards():
    b = yards()
    b.title("GAGARIN SHIPYARDS - DOCK ARMS")
    for x in range(190, 560, 70):
        for y0 in (78, 162):
            b.draw.rectangle([x, y0 - 14, x + 40, y0 - 8], outline=CYAN_DIM + (200,))
    b.arrow(170, 200, 600, 200, CYAN, dashed=False)
    b.label(180, 210, "SOUTH RAIL - LANCER", CYAN)
    b.arrow(170, 40, 600, 40, CYAN_DIM)
    b.label(180, 24, "NORTH RAIL - AEGIS TWO", CYAN_DIM)
    for x in (380, 470, 560):
        b.marker(x, 120, "chevron", RED, 5)
    b.arrow(660, 120, 610, 120, RED)
    b.label(W - 10, 132, "FROM L1", RED, right=True)
    b.label(W - 10, 9, "KEEP THEM OFF THE YARDS", AMBER, right=True)
    return b


def vrell_scan():
    b = Board()
    b.title("SENSOR SCAN - VRELL SCOUTS")
    for i, (name, frame, zoom, x) in enumerate((("SKITTER", "skitter_0", 3, 160), ("NEEDLER", "needler_0", 3, 470))):
        img = asset(frame, zoom)
        b.glow(x, 118, 60, (10, 40, 50), 0.6)
        b.bracket(x - 62, 50, x + 62, 186)
        b.sprite(img, x, 118)
        b.label(x - 60, 194, name, WHITE)
        b.label(x - 60, 206, "METAL: NONE DETECTED", RED)
        b.label(x - 60, 218, "HEAT: CORE 1400 K", AMBER)
        b.label(x + 70, 74, "AIM FOR", AMBER)
        b.label(x + 70, 84, "THE GLOW", AMBER)
        b.line([(x + 68, 90), (x + 12, 116)], AMBER, 1, 220)
    return b


def burning_yards():
    b = yards()
    b.title("GAGARIN SHIPYARDS - SOUTH ARM BURNING")
    for x in (230, 330, 430, 530):
        b.glow(x + 20, 160, 26, (120, 40, 10), 0.9)
    for i, x in enumerate((210, 310, 410, 510)):
        b.draw.rectangle([x, 172, x + 46, 196], outline=AMBER + (230,))
        b.label(x + 4, 178, f"DOCK {i + 1}", AMBER)
        b.label(x + 4, 188, "CREW", GREEN)
        for k in range(3):
            b.marker(x + 10 + 13 * k, 166, "dot", RED, 2)
    for x in (260, 380, 480, 600):
        b.glow(x, 78, 18, (100, 30, 10), 0.8)
    b.label(W - 10, 9, "4 DOCKS - CREWS ABOARD", AMBER, right=True)
    b.label(10, H - 16, "BURN THE GROWTHS OFF EACH DOCK BEFORE IT LEAVES YOUR SECTOR", CYAN_DIM)
    b.label(560, 24, "AEGIS TWO", CYAN_DIM)
    return b


def yield_signal():
    b = Board()
    b.title("INTERCEPT - VRELL TRANSMISSION, L01 T+160 S")
    rng = np.random.default_rng(160)
    xs = np.arange(10, 400)
    motif = np.sin(np.linspace(0, 6 * np.pi, 40)) * np.hanning(40) * 26
    wave = rng.normal(0, 5, xs.size)
    for start in (20, 110, 200, 290):
        wave[start:start + 40] += motif
        b.draw.rectangle([10 + start, 60, 10 + start + 40, 150], outline=AMBER + (160,))
    b.line(list(zip(xs, 105 + wave)), VIOLET, 1, 240)
    b.label(10, 160, "REPEATING PATTERN X4", AMBER)
    b.label(10, 172, "BEST FIT: 'YIELD'", WHITE)
    b.label(10, 184, "CONFIDENCE 61 %", CYAN_DIM)
    tx, ty = 540, 110
    turret = Image.open(SPRITES / "intel" / "spine-turret.png").convert("RGBA")
    b.sprite(sprite.enlarge(turret, 3), tx, ty)
    for a0, a1, colour, alpha in ((200, 340, RED, 150), (20, 160, GREEN, 120)):
        b.draw.pieslice([tx - 80, ty - 80, tx + 80, ty + 80], a0, a1, outline=colour + (alpha,))
    b.label(tx - 64, 18, "TRACKING ARC", RED)
    b.label(tx - 64, 214, "BLIND BEHIND", GREEN)
    b.label(W - 10, 9, "GROWTH = TURRET", AMBER, right=True)
    return b


def spore_lanes():
    """L03 p1: the high lanes over last week's battle site, the carriers seeding Earth."""
    b = Board()
    b.planet(336, 560, 360, "earth", seed=31)
    b.title("HIGH ORBITAL LANES - OVER THE GAGARIN BATTLE SITE")
    for y, name in ((70, "LANE 2"), (120, "LANE 1")):
        b.dashed([(20, y), (650, y)], CYAN_DIM, 1, 10, 6)
        b.label(24, y - 12, name, CYAN_DIM)
    rng = np.random.default_rng(3)
    for x, y in zip(rng.uniform(250, 470, 26), rng.uniform(84, 150, 26)):
        s = rng.uniform(1.5, 4)
        a = rng.uniform(0, np.pi)
        b.draw.polygon([(x + s * np.cos(a + k * 2.1), y + s * np.sin(a + k * 2.1)) for k in range(3)],
                       outline=GREY + (220,))
    b.bracket(240, 80, 480, 156, GREY)
    b.label(244, 160, "DEBRIS FIELD - LAST WEEK'S BATTLE", GREY)
    b.marker(372, 104, "cross", GREY, 5)
    b.label(384, 92, "KESTREL - WRECK", GREY)
    for x, y in ((150, 96), (300, 94), (440, 112), (560, 98)):
        b.glow(x, y, 16, (60, 30, 90), 0.7)
        b.marker(x, y, "chevron", VIOLET, 5)
        for k in range(4):
            b.marker(x - 6 + 4 * k, y + 16 + 13 * k, "dot", VIOLET, 1)
        b.dashed([(x + 4, y + 10), (x + 22, y + 74)], VIOLET, 1, 3, 4)
    b.label(150, 36, "SPORE CARRIERS", VIOLET)
    b.label(150, 48, "DRIFTING, DROPPING SPORES", VIOLET)
    b.arrow(30, 132, 220, 132, CYAN, dashed=False)
    b.label(30, 140, "LANCER - CLEAR THE LANE", CYAN)
    b.label(W - 10, 9, "10 CARRIERS - NONE GETS THROUGH", AMBER, right=True)
    b.label(W - 10, 176, "SEEDING THE PLANET", RED, right=True)
    b.label(10, H - 16, "EARTH - ATMOSPHERE BELOW", CYAN_DIM)
    return b


def spore_echo():
    """L03 p2: Varga's scan of the Spore Bomber and its rising spores, the long-range echo."""
    b = Board()
    b.title("SENSOR SCAN - SPORE BOMBER, LONG-RANGE ECHO")
    x = 110
    b.glow(x, 112, 60, (10, 40, 50), 0.6)
    b.bracket(x - 76, 36, x + 76, 186)
    b.sprite(asset("spore-bomber_0", 2), x, 112)
    b.label(x - 74, 194, "SPORE BOMBER", WHITE)
    b.label(x - 74, 206, "LOW-AIR - BELOW YOUR LEVEL", AMBER)
    b.label(x - 74, 218, "DROPS SPORE MINES", RED)
    sx, sy = 300, 200
    ship = asset("ship_2")
    b.sprite(ship, sx, sy)
    for a in (-34, -17, 0, 17, 34):
        t = np.radians(a)
        b.dashed([(sx + 14 * np.sin(t), sy - 26), (sx + 150 * np.sin(t), sy - 26 - 130 * np.cos(t))], CYAN, 1, 4, 4)
    mine = Image.open(SPRITES / "spore-mine_2.png").convert("RGBA")
    rng = np.random.default_rng(11)
    for mx, my in zip(rng.uniform(232, 372, 7), rng.uniform(64, 140, 7)):
        b.sprite(sprite.enlarge(mine, 2), mx, my)
        b.line([(mx, my + 16), (mx, my + 30)], VIOLET, 1, 160)
    b.label(232, 30, "SPORES RISE TO YOU", VIOLET)
    b.label(232, 40, "IN ABOUT 1 S", VIOLET)
    b.label(sx + 24, sy - 4, "GO WIDE", CYAN)
    rx, ry, r = 556, 132, 92
    for k in (1, 2, 3):
        b.ring(rx, ry, r * k / 3, colour=CYAN_DIM, alpha=110)
    b.line([(rx - r, ry), (rx + r, ry)], CYAN_DIM, 1, 80)
    b.line([(rx, ry - r), (rx, ry + r)], CYAN_DIM, 1, 80)
    b.draw.pieslice([rx - r, ry - r, rx + r, ry + r], 250, 290, fill=CYAN + (40,))
    b.marker(rx, ry, "square", CYAN, 3)
    big = Image.open(SPRITES / "leviathan-cross_0.png").convert("RGBA")
    big = big.resize((big.width // 4, big.height // 4), Image.LANCZOS)
    a = np.array(big)
    keep = (a[..., 3] > 128) & (np.random.default_rng(62).random(a.shape[:2]) > 0.3)
    keep &= (np.arange(a.shape[0]) % 3 != 0)[:, None]
    a[..., 3] = np.where(keep, 255, 0)
    b.glow(rx + 6, ry - 46, 44, (60, 30, 110), 0.9)
    b.sprite(holo(Image.fromarray(a), VIOLET, 6), rx + 6, ry - 46)
    b.label(rx - r - 10, 28, "LONG-RANGE ECHO", RED, right=True)
    b.label(rx - r - 10, 40, "VERY BIG", WHITE, right=True)
    b.label(rx - r - 10, 52, "VERY SLOW", WHITE, right=True)
    b.label(rx - r - 10, 64, "UNKNOWN", RED, right=True)
    b.label(W - 10, H - 16, "DON'T OUT-SHOOT IT: OUT-FLY IT", AMBER, right=True)
    return b


def convoy_road():
    """L04 p1: Tranquility Base, the brood pods, the convoy's road across the rille to the
    mass-driver terminal, the walkers in the craters, Hammer flight."""
    b = Board()
    b.glow(80, 130, 90, (40, 40, 46), 0.9)
    b.glow(600, 130, 80, (40, 40, 46), 0.7)
    b.title("MARE TRANQUILLITATIS - CONVOY ROAD")
    road = [(70, 150), (170, 160), (270, 136), (330, 140), (390, 128), (490, 150), (600, 138)]
    b.line(road, CYAN_DIM, 7, 120)
    b.line(road, AMBER, 1, 200)
    rille = [(300 + 18 * np.sin(y / 22), y) for y in range(24, H - 20, 4)]
    b.line(rille, CYAN_DIM, 1, 200)
    b.line([(x + 30, y) for x, y in rille], CYAN_DIM, 1, 200)
    b.draw.rectangle([308, 132, 352, 146], outline=CYAN + (230,))
    b.label(358, 206, "RILLE", CYAN_DIM)
    b.label(356, 106, "ROAD BRIDGE", CYAN)
    for cx, cy in ((60, 116), (84, 108), (98, 128), (66, 136)):
        b.ring(cx, cy, 7, colour=CYAN, alpha=200)
    b.marker(80, 124, "diamond", GREEN, 3)
    b.label(14, 36, "TRANQUILITY BASE", CYAN)
    b.label(14, 46, "HERITAGE SITE INTACT", GREEN)
    for px, py in ((44, 172), (126, 98), (150, 194), (30, 80), (200, 82), (230, 196)):
        b.glow(px, py, 12, (50, 20, 80), 0.8)
        b.ring(px, py, 5, colour=VIOLET, alpha=230)
        b.marker(px, py, "dot", VIOLET, 1)
    b.label(14, 214, "BROOD PODS - LANDED OVERNIGHT", VIOLET)
    pip = holo(Image.open(SPRITES / "civilian-crawler-pip.png").convert("RGBA").transpose(Image.ROTATE_270),
               GREEN, 6)
    for k in range(5):
        b.sprite(pip, 120 + 22 * k, 154 + (k > 1) * 3 - (k > 3) * 6)
    b.label(110, 168, "5 CRAWLERS - 400 CIVILIANS", GREEN)
    for cx, cy, walker in ((200, 120, True), (250, 178, False), (420, 172, True), (450, 100, True),
                           (530, 182, True), (560, 104, False)):
        b.ring(cx, cy, 14, 10, GREY, 200)
        if walker:
            b.marker(cx, cy + 2, "chevron", RED, 4)
    b.label(410, 204, "WALKERS IN THE CRATERS", RED)
    tx, ty = 620, 138
    b.draw.rectangle([tx - 16, ty - 12, tx + 16, ty + 12], outline=AMBER + (240,))
    b.marker(tx, ty, "diamond", AMBER, 4)
    b.line([(tx + 16, ty - 4), (W - 4, ty - 70)], CYAN_DIM, 1, 200)
    b.line([(tx + 16, ty + 4), (W - 4, ty - 62)], CYAN_DIM, 1, 200)
    b.label(W - 10, 164, "MASS-DRIVER TERMINAL", AMBER, right=True)
    b.label(W - 10, 9, "LOSE ALL FIVE: MISSION OVER", RED, right=True)
    hx, hy = 470, 52
    b.bracket(hx - 28, hy - 30, hx + 28, hy + 30, CYAN)
    b.sprite(asset("airstrike-bomber_0"), hx, hy)
    b.label(hx + 36, hy - 14, "HAMMER FLIGHT", CYAN)
    b.label(hx + 36, hy - 2, "AIRSTRIKE - RELEASED", GREEN)
    b.label(hx + 36, hy + 10, "TO AEGIS WING", CYAN_DIM)
    return b


def walker_scan():
    """L04 p2: Varga's scan of the Scuttler: it faces where it walks, the claws stop rounds, the
    back glows."""
    b = Board()
    b.title("SENSOR SCAN - VRELL WALKER")
    x, y = 200, 122
    frame = 12 * 6
    b.glow(x, y, 70, (10, 40, 50), 0.6)
    walker = asset(f"scuttler_{frame}", 2)
    glow = asset(f"scuttler-glow_{frame}", 2)
    w = np.array(walker).astype(np.float64)
    g = np.array(glow).astype(np.float64)
    w[..., :3] = np.clip(w[..., :3] + g[..., :3], 0, 255)
    walker = artkit.quantize_set([Image.fromarray(w.astype(np.uint8))], 48)[0]
    for a0, a1, colour, alpha in ((-45, 45, RED, 170), (135, 225, GREEN, 150)):
        b.draw.pieslice([x - 90, y - 90, x + 90, y + 90], a0, a1, outline=colour + (alpha,))
    b.sprite(walker, x, y)
    b.arrow(x + 96, y, x + 150, y, CYAN, dashed=False)
    b.label(x + 100, y + 10, "WALKS", CYAN)
    b.label(x + 100, y + 20, "THIS WAY", CYAN)
    b.label(x + 40, 22, "CLAWS FORWARD", RED)
    b.label(x + 40, 32, "STOP YOUR ROUNDS", RED)
    b.label(x - 150, 200, "BACK GLOWS", GREEN)
    b.label(x - 150, 210, "AIM HERE", GREEN)
    b.label(10, 22, "SCUTTLER", WHITE)
    b.label(10, 32, "TURNS TO FACE", CYAN_DIM)
    b.label(10, 42, "WHERE IT GOES", CYAN_DIM)
    px = 560
    b.label(px - 140, 40, "BE PATIENT", AMBER)
    b.dashed([(px - 130, 150), (px - 60, 150), (px, 110), (px, 74)], RED, 1)
    b.sprite(asset("scuttler_48"), px, 52)
    b.sprite(asset("ship_2"), px, 196)
    b.dashed([(px, 170), (px, 86)], CYAN, 1, 3, 3)
    b.label(px - 140, 56, "LET IT TURN AWAY", CYAN)
    b.label(px + 30, 160, "GET BEHIND IT", CYAN)
    b.label(px + 30, 170, "THEN FIRE", GREEN)
    b.label(W - 10, 9, "GROUND LAYER", CYAN_DIM, right=True)
    return b


def crater_nest():
    """L05 p1: the nest crater beside the mass-driver line, batteries A-D on its floor, Lancer's
    run up the rail and over the rim, the sleds on automatic."""
    b = Board()
    b.planet(336, 760, 560, "moon", seed=51)
    b.title("MASS-DRIVER LINE - VRELL NEST CRATER")
    cx, cy, rx, ry = 400, 128, 170, 84
    b.glow(cx, cy, 120, (30, 14, 50), 0.9)
    b.ring(cx, cy, rx, ry, CYAN, 220, 2)
    b.ring(cx, cy, rx - 10, ry - 7, CYAN_DIM, 140)
    b.label(cx + rx - 4, cy - 50, "CRATER RIM", CYAN_DIM)
    rng = np.random.default_rng(55)
    for _ in range(9):
        a, r = rng.uniform(0, 2 * np.pi), rng.uniform(0.15, 0.7)
        x, y = cx + np.cos(a) * rx * r, cy + np.sin(a) * ry * r
        b.glow(x, y, 10, (50, 20, 80), 0.7)
        b.marker(x, y, "dot", VIOLET, 1)
    b.label(cx - 12, cy + 8, "NEST", VIOLET)
    for name, (x, y) in zip("ABCD", ((280, 104), (350, 170), (450, 92), (520, 160))):
        b.glow(x, y, 14, (90, 60, 10), 0.8)
        b.marker(x, y, "diamond", AMBER, 5)
        for k in (-1, 1):
            b.marker(x + 12 * k, y + 8, "chevron", RED, 3)
        b.label(x + 10, y - 16, f"BATTERY {name}", AMBER)
    ry0 = 30
    b.line([(20, ry0), (650, ry0)], CYAN, 2)
    b.line([(20, ry0 + 5), (650, ry0 + 5)], CYAN_DIM, 1, 140)
    for x in range(40, 650, 30):
        b.marker(x, ry0 + 2, "dot", AMBER, 1)
    b.label(W - 10, 9, "MASS DRIVER: AUTOMATIC, SLEDS EVERY 5 S", AMBER, right=True)
    b.label(W - 10, H - 40, "STAY OFF THE RAIL", RED, right=True)
    b.arrow(30, 52, 200, 52, CYAN, dashed=False)
    b.dashed([(200, 52), (236, 92), (300, 132), (420, 132), (560, 128), (630, 128)], CYAN, 1)
    b.label(30, 60, "LANCER - DOWN THE RAIL, OVER THE RIM", CYAN)
    b.label(10, H - 28, "DESTROY ALL FOUR BATTERIES", AMBER)
    b.label(10, H - 16, "A BATTERY LEFT ALIVE: MISSION FAILED", RED)
    b.label(W - 10, H - 16, "NO SECOND RUN TODAY", WHITE, right=True)
    return b


def mortar_scan():
    """L05 p2: Varga's scan of the Polyp Mortar: the lob arcing to its lime marker a second ahead,
    the ship moving out of it; something in orbit watching the crater."""
    b = Board()
    b.title("SENSOR SCAN - VRELL ACID-THROWER")
    x, y = 90, 150
    b.glow(x, y, 54, (10, 40, 50), 0.6)
    b.bracket(x - 44, y - 44, x + 44, y + 44)
    b.sprite(asset("polyp-mortar_0", 2), x, y)
    b.label(x - 44, y + 52, "POLYP MORTAR", WHITE)
    b.label(x - 44, y + 64, "GROUND LAYER", CYAN_DIM)
    tx, ty = 330, 132
    arc = [(x + (tx - x) * t, y + (ty - y) * t - np.sin(np.pi * t) * 96) for t in np.linspace(0.08, 1, 24)]
    b.dashed(arc, GREEN, 1, 4, 4)
    for t in (0.35, 0.7):
        b.sprite(asset("mortar-blob_0"), *arc[int(t * 23)])
    b.glow(tx, ty, 26, (40, 70, 10), 0.8)
    b.sprite(asset("mortar-marker"), tx, ty)
    b.label(tx - 60, ty + 30, "MARKED 1 S AHEAD", GREEN)
    b.label(tx - 60, ty + 42, "THEN A RING OF ACID", RED)
    ship = asset("ship_2")
    b.sprite(ship, tx + 10, ty + 2)
    b.arrow(tx + 26, ty + 2, tx + 92, ty + 2, CYAN, dashed=False)
    b.label(tx + 30, ty - 12, "KEEP MOVING", CYAN)
    rx, ry, r = 572, 112, 72
    for k in (1, 2, 3):
        b.ring(rx, ry, r * k / 3, colour=CYAN_DIM, alpha=110)
    b.ring(rx, ry, 10, colour=AMBER, alpha=200)
    b.label(rx - 30, ry + 16, "CRATER", AMBER)
    b.arc(rx, ry, r * 0.8, r * 0.8, 200, 340, VIOLET, 220)
    b.glow(rx + 30, ry - 52, 16, (60, 30, 110), 0.9)
    b.marker(rx + 30, ry - 52, "square", RED, 4)
    b.label(rx - r, ry + r + 8, "ORBIT: UNKNOWN CONTACT", RED)
    b.label(rx - r, ry + r + 20, "HOLDING OVER THE CRATER", WHITE)
    b.label(W - 10, 9, "DON'T BE THERE", AMBER, right=True)
    return b


def daedalus_rim():
    """L06 p1: the far side across the terminator, three settlements silent, Daedalus Rim the
    largest with its lights still on, Lancer's run into the dark by headlight."""
    b = Board()
    tx = 190 + 10 * np.sin(b.yy / 30)
    dark = np.clip((b.xx - tx) / 40 + 0.5, 0, 1)
    b.base *= (1 - 0.55 * dark)[..., None]
    b.glow(-60, 120, 170, (70, 64, 50), 1.0)
    b.title("LUNA FAR SIDE - DAEDALUS RIM")
    rng = np.random.default_rng(66)
    for x, y, r in ((60, 70, 22), (120, 170, 30), (170, 92, 16), (40, 200, 12), (150, 40, 10)):
        b.arc(x, y, r, r * 0.8, 140, 320, AMBER, 170)
        b.arc(x, y, r, r * 0.8, -40, 140, GREY, 90)
    for x, y in zip(rng.uniform(230, 650, 14), rng.uniform(30, 220, 14)):
        r = rng.uniform(6, 16)
        b.ring(x, y, r, r * 0.8, GREY, 70)
    b.dashed([(190 + 10 * np.sin(y / 30), y) for y in range(20, H - 16, 6)], AMBER, 1, 4, 4)
    b.label(14, 24, "SUNLIT", AMBER)
    b.label(206, 24, "TERMINATOR", AMBER)
    b.label(206, H - 16, "FULL DARK", CYAN_DIM)
    b.line([(230, 214), (420, 160), (480, 138), (560, 92), (660, 60)], CYAN_DIM, 1, 160)
    b.label(300, 206, "ORE RAIL", CYAN_DIM)
    for x, y, name in ((330, 60, "SETTLEMENT 2"), (600, 70, "SETTLEMENT 3")):
        b.marker(x, y, "square", GREY)
        b.marker(x, y, "cross", RED, 6)
        b.label(x - 30, y + 12, name, GREY)
        b.label(x - 30, y + 22, "SILENT", RED)
    dx, dy = 480, 118
    dome = Image.open(ROOT / "assets" / "backdrop" / "level-06" / "dome-a.png").convert("RGBA")
    dome = np.array(dome.resize((dome.width // 2, dome.height // 2), Image.LANCZOS))
    dome[..., 3] = np.where(dome[..., 3] > 128, 255, 0)
    dome = artkit.quantize_set([Image.fromarray(dome)], 32)[0]
    for ox, oy in ((-22, -6), (14, 10), (24, -14)):
        b.glow(dx + ox, dy + oy, 14, (90, 70, 20), 0.9)
    b.sprite(dome, dx, dy)
    b.bracket(dx - 52, dy - 44, dx + 52, dy + 44, AMBER)
    b.label(dx - 52, dy + 50, "DAEDALUS RIM", AMBER)
    b.label(dx - 52, dy + 60, "2000 PEOPLE - MINERS, FAMILIES", WHITE)
    b.label(dx - 52, dy + 70, "LIGHTS ON - SILENT 30 H", RED)
    sx, sy = 120, 128
    b.sprite(asset("ship_2").transpose(Image.ROTATE_270), sx, sy)
    cone = Image.open(SPRITES / "headlight-cone.png").convert("RGBA").transpose(Image.ROTATE_270)
    cone = np.array(cone.resize((cone.width // 2, cone.height // 2), Image.LANCZOS))[..., 3] / 255
    x0, y0 = sx + 20, sy - cone.shape[0] // 2
    b.base[y0:y0 + cone.shape[0], x0:x0 + cone.shape[1]] += cone[..., None] * np.array([30, 90, 110])
    b.arrow(sx + 26, sy, dx - 60, dy + 4, CYAN)
    b.label(sx - 60, sy + 28, "LANCER - BY HEADLIGHT", CYAN)
    b.label(W - 10, 9, "3 SETTLEMENTS SILENT", RED, right=True)
    b.label(W - 10, 21, "NO DISTRESS CALL - NO WRECKAGE", WHITE, right=True)
    b.label(10, H - 16, "FIND OUT WHAT HAPPENED", AMBER)
    return b


def wyrm_path(points, spacing, sizes):
    """The chain's members along ``points`` (the head first), each ``spacing`` x the mean length of
    two neighbours behind the one before it, as (x, y, direction of travel)."""
    pts = np.array(points, float)
    seg = np.hypot(*(pts[1:] - pts[:-1]).T)
    run = np.concatenate([[0], np.cumsum(seg)])
    out, s = [], 0.0
    for i, size in enumerate(sizes):
        if i:
            s += spacing * (sizes[i - 1] + size) / 2
        k = min(np.searchsorted(run, s, side="right") - 1, len(seg) - 1)
        t = (s - run[k]) / seg[k]
        p = pts[k] + (pts[k + 1] - pts[k]) * t
        travel = pts[k] - pts[k + 1]  # the head leads, so the chain travels toward the path's start
        out.append((p[0], p[1], travel))
    return out


def path_beyond(points, s):
    """The part of the path ``points`` beyond the arc length ``s`` from its start: the head's path
    history behind the tail, the way the chain came round (its own stretch the overlapping members
    hide)."""
    pts = np.array(points, float)
    run = np.concatenate([[0], np.cumsum(np.hypot(*(pts[1:] - pts[:-1]).T))])
    k = min(np.searchsorted(run, s, side="right") - 1, len(pts) - 2)
    p = pts[k] + (pts[k + 1] - pts[k]) * (s - run[k]) / (run[k + 1] - run[k])
    return [tuple(p)] + [tuple(q) for q in pts[k + 1:]]


def heading(travel, count=48):
    """The production heading index of a direction of travel (clockwise from straight down)."""
    deg = np.degrees(np.arctan2(-travel[0], travel[1])) % 360
    return int(round(deg / (360 / count))) % count


def mantis_sweep(b, eye, target, reach=240, at=0.3):
    """The Mantis's telegraph and beam in the production look (round 23 variant b, the charged
    lance: tools/art/mantis_beam.py) from its eye on the left edge: the telegraph wedge at the
    data's arc, centred on the bearing to ``target`` (clamped inward..down, as the game does), and
    the beam ``at`` its way through the sweep with its tip spark, the eye ring over the sprite.
    ``reach`` is the image's beam length (the game's 300 px does not fit the left half)."""
    ex, ey = eye
    bearing = np.arctan2(-(target[0] - ex), target[1] - ey)  # game heading: clockwise from straight down
    centre = np.clip(bearing, -np.pi / 2, 0)
    half = np.radians(mantis_beam.SWEEP["arc"]) / 2
    wedge = artkit.quantize_set([artkit.additive(mantis_beam.r23.wedge_b(int(mantis_beam.SWEEP["arc"]), reach))],
                                LIGHT_COLOURS)[0]
    rgb, c = mantis_beam.rotated(wedge, mantis_beam.r23.heading_to_img(centre), (0, wedge.height // 2))
    b.add_light(rgb, ex - c, ey - c)
    a = mantis_beam.r23.heading_to_img(centre - half + 2 * half * at)
    strip = mantis_beam.lay_beam(artkit.load_frames("mantis-beam")[0], reach)
    rgb, c = mantis_beam.rotated(strip, a, (0, strip.height // 2))
    b.add_light(rgb, ex - c, ey - c, "over")
    for img, x, y in ((artkit.load_frames("mantis-beam-tip")[0], ex + np.cos(a) * reach, ey + np.sin(a) * reach),
                      (artkit.load_frames("mantis-beam-eye")[0], ex, ey)):
        b.add_light(np.array(img.convert("RGB")), x - img.width / 2, y - img.height / 2, "over")


def edge_scan():
    """L06 p2: Varga's two new contacts: the Mantis at the edge sweeping its beam across from its
    eye, side guns reaching it; the Coilwyrm coming round behind the ship, whose rear is open."""
    b = Board()
    b.title("SENSOR SCAN - TWO NEW CONTACTS")
    ex = 22
    b.dashed([(ex, 24), (ex, H - 20)], CYAN_DIM, 1, 4, 4)
    b.label(ex + 4, H - 16, "SCREEN EDGE", CYAN_DIM)
    mx, my = 62, 104
    sx, sy = 236, 188
    mantis = asset("mantis_8")  # the left edge's sweep pose, where the data's sweep.origin is measured
    b.glow(mx, my, 50, (10, 40, 50), 0.6)
    mantis_sweep(b, (mx + mantis_beam.ORIGIN[0], my + mantis_beam.ORIGIN[1]), (sx, sy))
    b.sprite(mantis, mx, my)
    b.bracket(mx - 42, my - 42, mx + 42, my + 42)
    b.label(110, 24, "MANTIS", WHITE)
    b.label(110, 34, "SNIPER AT THE EDGES", RED)
    b.label(200, 108, "ITS BEAM SWEEPS", RED)
    b.label(200, 118, "ACROSS YOU", RED)
    b.sprite(asset("ship_2"), sx, sy)
    b.arrow(sx - 24, sy - 2, mx + 40, sy - 2, GREEN, dashed=False)
    b.label(40, sy + 14, "GUNS THAT FIRE SIDEWAYS", GREEN)
    b.line([(326, 30), (326, H - 20)], CYAN_DIM, 1, 90)
    spec = yaml.safe_load((DESIGN / "enemies" / "air" / "coilwyrm" / "data.yaml").read_text())
    spacing = spec["segment_chain"]["spacing"]
    pivots = json.loads((ROOT / "assets" / "pivots" / "coilwyrm.json").read_text())
    sizes = [58] + pivots["segment_sizes"] + [40]
    path = [(420, 125)]
    for (cx, cy), a0 in (((460, 175), np.pi), ((600, 165), np.pi / 2), ((600, 70), 0)):
        path += [(cx + 40 * np.cos(a), cy + 40 * np.sin(a)) for a in np.linspace(a0, a0 - np.pi / 2, 10)]
    path += [(500, 30)]
    members = wyrm_path(path, spacing, sizes)
    b.dashed(path_beyond(path, spacing * sum((u + v) / 2 for u, v in zip(sizes, sizes[1:])))[::-1],
             VIOLET, 1, 3, 5)
    for i in range(len(members) - 1, -1, -1):
        x, y, travel = members[i]
        k = heading(travel)
        if i == 0:
            name = f"coilwyrm_{k * 4 + 2}"
        elif i == len(members) - 1:
            name = f"coilwyrm-tail_{k}"
        else:
            name = f"coilwyrm-segment_{k * 12 + i - 1}"
        b.sprite(asset(name), x, y)
    px, py = 420, 66
    b.draw.pieslice([px - 46, py - 46, px + 46, py + 46], 50, 130, fill=RED + (40,), outline=RED + (190,))
    b.sprite(asset("ship_2"), px, py)
    b.label(474, 100, "YOUR REAR", RED)
    b.label(474, 110, "IS OPEN", RED)
    b.arrow(px - 26, py + 4, 346, py + 4, CYAN, dashed=False)
    b.label(342, py + 14, "MOVE", CYAN)
    b.label(342, 24, "COILWYRM", WHITE)
    b.label(342, 34, "COMES ROUND BEHIND YOU", VIOLET)
    b.label(474, 140, "DON'T FIGHT", AMBER)
    b.label(474, 150, "IT THERE", AMBER)
    return b


# --------------------------------------------------------------------------- Level 07 and the Act 1 outro

_CARRIER = {}


def carrier_art():
    """The Brood Carrier's production sprites (brood_carrier.py's set), loaded once."""
    if not _CARRIER:
        _CARRIER.update(bc.load_art())
    return _CARRIER


def scaled(img, scale, colours=48):
    """A sprite reduced for the display: smooth, its edge cut at half alpha, on a palette of its own."""
    small = np.array(img.resize((max(1, round(img.width * scale)), max(1, round(img.height * scale))),
                                Image.LANCZOS))
    small[..., 3] = np.where(small[..., 3] > 128, 255, 0)
    return artkit.quantize_set([Image.fromarray(small)], colours)[0]


def carrier(frame, scale, sacs=None, iris=0, turret_to=-np.pi / 2):
    """The carrier as the game composes it (brood_carrier.compose, on a clear canvas): hull ``frame``
    (0 nose-down, 8 broadside) and at the two poses the sac sprites (``sacs``: a stage per bay in
    BAYS order, 0 closed, 3 open, 4 burst), the iris stage and the mandible turret aimed at
    ``turret_to`` (radians counter-clockwise, y up); reduced by ``scale``. Returns the sprite and the
    pose's part offsets at that scale in image coordinates (dx right, dy down)."""
    art = carrier_art()
    hull = art["hull"][frame]
    img = Image.new("RGBA", hull.size, (0, 0, 0, 0))
    img.alpha_composite(hull)
    cx, cy = hull.width // 2, hull.height // 2
    offs = bc.offsets(0 if frame == 0 else 1)
    if frame in (0, len(art["hull"]) - 1):
        sac = art["sac-down" if frame == 0 else "sac-side"]
        parts = [(sac[0 if sacs is None else sacs[i]], offs[name]) for i, name in enumerate(bc.BAYS)]
        parts += [(art["iris"][iris], offs["core"]), (art["turret"][bc.heading_frame(turret_to)], offs["mandibles"])]
        for f, (dx, dy) in parts:
            img.alpha_composite(f, (cx + dx - f.width // 2, cy - dy - f.height // 2))
    return scaled(img, scale), {k: (dx * scale, -dy * scale) for k, (dx, dy) in offs.items()}


def shadow(b, img, x, y, dx, dy, strength=0.55):
    """A high-air unit's shadow on the display: ``img``'s alpha (centred at x, y) offset by dx, dy."""
    a = np.array(img)[..., 3] / 255
    x0, y0 = int(x - img.width / 2 + dx), int(y - img.height / 2 + dy)
    ys, xs = slice(max(0, y0), min(H, y0 + a.shape[0])), slice(max(0, x0), min(W, x0 + a.shape[1]))
    b.base[ys, xs] *= (1 - strength * a[ys.start - y0:ys.stop - y0, xs.start - x0:xs.stop - x0])[..., None]


def core_glow(b, x, y, scale, strength=1.0):
    """The core's additive lime glow (brood-carrier-core-glow) over the sprites at (x, y); smaller,
    the glow the display puts on an open sac."""
    g = carrier_art()["glow"]
    g = np.array(g.resize((round(g.width * scale * 1.6), round(g.height * scale * 1.6)), Image.LANCZOS)).astype(float)
    rgb = g[..., :3] * g[..., 3:4] / 255 * strength
    b.add_light(rgb, x - rgb.shape[1] / 2, y - rgb.shape[0] / 2, "over")


def backdrop(level, name, scale, colours=32):
    """A backdrop piece of a level (assets/backdrop/<level>/), reduced for the display."""
    return scaled(Image.open(ROOT / "assets" / "backdrop" / level / f"{name}.png").convert("RGBA"), scale, colours)


def cloud(b, level, name, scale, x, y):
    """A translucent backdrop piece (an ichor cloud) blended into the display layer, centred at x, y,
    so it takes the display's palette."""
    img = Image.open(ROOT / "assets" / "backdrop" / level / f"{name}.png").convert("RGBA")
    a = np.array(img.resize((round(img.width * scale), round(img.height * scale)), Image.LANCZOS)).astype(float)
    x0, y0 = int(x - a.shape[1] / 2), int(y - a.shape[0] / 2)
    ys, xs = slice(max(0, y0), min(H, y0 + a.shape[0])), slice(max(0, x0), min(W, x0 + a.shape[1]))
    a = a[ys.start - y0:ys.stop - y0, xs.start - x0:xs.stop - x0]
    k = a[..., 3:4] / 255
    b.base[ys, xs] = b.base[ys, xs] * (1 - k) + a[..., :3] * k


def l1_carrier():
    """L07 p1: the carrier holding at the Earth-Moon L1 point where this started, the pods that hit
    Luna traced back to it, its escort screen, the overrun picket, Aegis's way through to it."""
    b = Board()
    b.planet(34, 150, 92, "earth", seed=19)
    b.planet(626, 64, 22, "moon", seed=5)
    b.title("EARTH-MOON L1 - WHERE THIS STARTED")
    cx, cy = 480, 132
    b.glow(cx, cy, 100, (16, 46, 34), 0.9)
    ship, offs = carrier(8, 0.3, turret_to=np.pi)
    for k, (dy0, bend) in enumerate(((-14, -26), (0, -12), (14, 4))):
        tail = (cx + 64, cy + dy0 * 0.5)
        pts = [(tail[0] + (604 - tail[0]) * t, tail[1] + (76 + dy0 * 0.4 - tail[1]) * t + bend * np.sin(np.pi * t))
               for t in np.linspace(0, 1, 12)]
        b.dashed(pts, VIOLET, 1, 3, 4)
    b.label(596, 38, "EVERY POD ON LUNA", VIOLET, right=True)
    b.label(596, 48, "TRACED BACK HERE", VIOLET, right=True)
    b.label(W - 10, 92, "LUNA", CYAN_DIM, right=True)
    b.sprite(ship, cx, cy)
    b.bracket(cx - 102, cy - 50, cx + 102, cy + 50, RED)
    b.label(cx - 84, cy - 68, "BROOD CARRIER", WHITE)
    b.label(cx - 84, cy - 58, "THE SIZE OF A STATION", RED)
    b.label(cx - 84, cy + 56, "WEAK POINTS:", LIME)
    b.label(cx - 84, cy + 66, "LAUNCH SACS, CORE UNDER ARMOUR", LIME)
    for k, a in enumerate(np.linspace(-50, 50, 9)):
        t = np.radians(a)
        x, y = cx - 150 * np.cos(t), cy + 96 * np.sin(t)
        b.glow(x, y, 9, (60, 24, 40), 0.6)
        b.marker(x, y, "chevron" if k % 3 else "dot", RED if k % 3 else VIOLET, 4 if k % 3 else 3)
    b.arc(cx, cy, 150, 96, 130, 230, RED, 120)
    b.label(300, 30, "ESCORT SCREEN", RED)
    for y in (48, 92, 138, 184):
        x = 206 + 8 * np.sin(y / 20)
        b.marker(x, y, "square", GREY)
        b.marker(x, y, "cross", RED, 6)
    b.label(150, 206, "L1 PICKET - OVERRUN", GREY)
    b.arrow(118, 150, 296, 136, CYAN)
    b.label(110, 160, "AEGIS - PUNCH THROUGH", CYAN)
    b.label(W - 10, 9, "WHEN IT DIES, THE PODS STOP", AMBER, right=True)
    b.label(10, H - 16, "DESTROY THE CARRIER", AMBER)
    return b


def overhead_scan():
    """L07 p2: Varga's scan of the carrier's three stages (user decision D1 of part G): it comes over
    nose-down on high air (its shadow on the ship's level) and stops, the bays opening in pairs, only
    missiles reaching up; it descends and turns 90 degrees in place; broadside at the ship's level
    the sacs and the plate iris over the lime core."""
    b = Board()
    b.title("SENSOR SCAN - BROOD CARRIER")
    x1, y1, s1 = 84, 120, 0.3
    high, offs = carrier(0, s1, sacs=[3, 3] + [0] * 6)
    shadow(b, high, x1, y1, 16, 22)
    b.glow(x1, y1 + 30, 50, (30, 60, 10), 0.5)
    b.sprite(high, x1, y1)
    for name in ("bay 1 left", "bay 1 right"):
        core_glow(b, x1 + offs[name][0], y1 + offs[name][1], s1 * 0.6, 0.8)
    b.label(150, 24, "1 HIGH AIR", WHITE)
    b.label(150, 34, "ABOVE YOU", WHITE)
    b.label(150, 46, "NOSE FIRST, STOPS", CYAN_DIM)
    b.label(150, 58, "BAYS OPEN IN PAIRS", LIME)
    b.arrow(20, 34, 20, 100, CYAN_DIM)
    sx, sy = 172, 204
    b.sprite(asset("ship_2"), sx, sy)
    tx, ty = x1 + offs["bay 1 right"][0] + 4, y1 + offs["bay 1 right"][1]
    for bend in (60, -10):  # two homing missiles curving up to the open sac
        pts = [(sx + (tx - sx) * t + (1 - t) * t * bend, sy - 20 + (ty - sy + 20) * t) for t in np.linspace(0, 1, 14)]
        b.dashed(pts, GREEN, 1, 4, 3)
    b.label(150, 84, "ONLY MISSILES", GREEN)
    b.label(150, 94, "REACH THAT HIGH", GREEN)
    b.dashed([(sx + 12, sy - 24), (sx + 12, sy - 60)], GREY, 1, 3, 3)
    b.marker(sx + 12, sy - 64, "cross", GREY, 3)
    b.label(sx + 20, sy - 70, "GUNS: NO", GREY)
    b.line([(266, 30), (266, H - 20)], CYAN_DIM, 1, 90)
    b.line([(394, 30), (394, H - 20)], CYAN_DIM, 1, 90)
    tx2, ty2 = 330, 128
    b.sprite(holo(carrier(4, 0.15)[0], CYAN_DIM, 8), tx2, ty2)
    b.arc(tx2, ty2, 54, 54, 200, 290, CYAN, 220)
    b.arrow(tx2 + 54 * np.cos(np.radians(285)), ty2 + 54 * np.sin(np.radians(285)),
            tx2 + 54 * np.cos(np.radians(300)), ty2 + 54 * np.sin(np.radians(300)), CYAN, 5, dashed=False)
    b.label(274, 24, "2 COMES DOWN", WHITE)
    b.label(274, 34, "TURNS 90 DEG", CYAN_DIM)
    b.label(274, 44, "IN PLACE", CYAN_DIM)
    b.label(274, 198, "NO DAMAGE", RED)
    b.label(274, 208, "WHILE IT TURNS", RED)
    x3, y3, s3 = 530, 110, 0.4
    sx3, sy3 = 512, 210
    tx, ty = x3 + 285 * s3, y3  # the mandibles, broadside
    side, offs3 = carrier(8, s3, sacs=[4, 4, 3, 3, 0, 0, 0, 0], iris=4,
                          turret_to=np.arctan2(-(sy3 - ty), sx3 - tx))
    b.glow(x3, y3, 80, (16, 46, 34), 0.8)
    b.sprite(side, x3, y3)
    cxg, cyg = x3 + offs3["core"][0], y3 + offs3["core"][1]
    core_glow(b, cxg, cyg, s3)
    b.label(402, 24, "3 BROADSIDE, YOUR LEVEL", WHITE)
    b.label(402, 34, "EVERY GUN REACHES IT", CYAN)
    for name in ("bay 2 left", "bay 2 right"):
        core_glow(b, x3 + offs3[name][0], y3 + offs3[name][1], s3 * 0.6, 0.8)
    b.line([(x3 + offs3["bay 2 right"][0] + 8, y3 + offs3["bay 2 right"][1] + 8), (600, 184)], LIME, 1, 200)
    b.label(W - 10, 188, "HIT THE LIME GLOW", LIME, right=True)
    b.label(W - 10, 198, "SACS, THEN THE CORE", LIME, right=True)
    b.line([(cxg - 8, cyg + 10), (440, 184)], VIOLET, 1, 200)
    b.label(402, 188, "PLATE IRIS", VIOLET)
    b.label(402, 198, "OVER THE CORE", VIOLET)
    b.sprite(asset("ship_2"), sx3, sy3)
    for dx in (-6, 6):
        b.dashed([(sx3 + dx, sy3 - 24), (sx3 + dx, cyg + 50)], CYAN, 1, 4, 4)
    b.label(W - 10, 9, "NO MISSILES? SURVIVE UNTIL IT COMES DOWN", AMBER, right=True)
    return b


def outro_carcass():
    """Act 1 outro p1: the carrier's carcass drifting apart at L1 (its production break-up chunks,
    dead and burnt dark, and ichor clouds), Earth beyond; what the week saved."""
    b = Board()
    b.planet(500, 330, 230, "earth", seed=23)
    b.planet(54, 58, 16, "moon", seed=5)
    b.title("EARTH-MOON L1 - THE CARRIER, AFTER")
    cx, cy, s = 250, 112, 0.34
    death = json.loads((ROOT / "assets" / "pivots" / "brood-carrier.json").read_text())["death"]
    darken = death["darken"]
    b.glow(cx, cy, 120, (14, 30, 26), 0.8)
    for k, name in enumerate(("ichor-c", "ichor-a", "ichor-b")):
        cloud(b, "level-07", name, 0.6, cx - 120 + 120 * k, cy + (30, -40, 44)[k])
    rng = np.random.default_rng(71)
    chunks = death["chunks"]
    mx, my = (np.mean([c["drift"][i] for c in chunks]) for i in (0, 1))
    for chunk in chunks:
        # the last tumble frame (the cut flesh cooled to dead tissue, nothing glows) at its centre
        # moved by its drift less the carcass's common drift, the pieces coming apart round the
        # bracket's middle (wider than the data across, as far as the data up and down, 4 px high:
        # clear of the title and the labels); offsets and drift are dy up, the display's y down
        (ox, oy), (dx, dy) = chunk["offsets"][2], chunk["drift"]
        rng.integers(0, 3)  # the frame draw of the approved image, kept so the debris dots stay put
        a = np.array(artkit.load_frames(chunk["sprite"])[2]).astype(float)
        a[..., :3] *= darken
        piece = scaled(Image.fromarray(a.astype(np.uint8)), s, 24)
        b.sprite(piece, cx + (ox + 1.6 * (dx - mx)) * s, cy - 4 - (oy + dy - my) * s)
    for x, y in rng.uniform((cx - 150, cy - 70), (cx + 150, cy + 70), (14, 2)):
        b.marker(x, y, "dot", GREY, 1)
    b.bracket(cx - 160, cy - 76, cx + 160, cy + 82, GREY)
    b.label(cx - 160, cy + 88, "BROOD CARRIER - DEAD, DRIFTING", GREY)
    b.label(cx - 160, cy + 98, "NO LAUNCHES SINCE THE KILL", GREEN)
    b.label(W - 10, 9, "A WEEK AGO, NONE OF THIS WAS CERTAIN", AMBER, right=True)
    for i, (text, colour) in enumerate((("GAGARIN YARDS - SCARRED, STANDING", GREEN),
                                        ("TRANQUILITY CONVOY - HOME", GREEN),
                                        ("LUNA - NOTHING FALLING", GREEN))):
        b.label(W - 10, 30 + 10 * i, text, colour, right=True)
    b.label(40, 80, "LUNA", CYAN_DIM)
    b.label(W - 10, H - 16, "EARTH", CYAN_DIM, right=True)
    b.label(10, H - 16, "WELL FLOWN, AEGIS", AMBER)
    return b


def outro_daedalus():
    """Act 1 outro p2: Daedalus Rim on the far side, still empty: the main airlock open and lit, the
    domes' lights on (Level 06's backdrop pieces), the file on the missing kept open."""
    b = Board()
    b.base *= 0.6
    b.title("LUNA FAR SIDE - DAEDALUS RIM, SEARCHED")
    gx, gy, s = 214, 118, 0.6
    for x, y, r in ((gx, gy - 28, 60), (gx - 92, gy - 62, 40), (gx + 92, gy - 62, 40), (gx, gy + 54, 46)):
        b.glow(x, y, r, (80, 62, 24), 0.8)
    rng = np.random.default_rng(67)
    for x, y in zip(rng.uniform(20, 420, 12), rng.uniform(40, 220, 12)):
        r = rng.uniform(5, 13)
        b.ring(x, y, r, r * 0.8, GREY, 60)
    b.sprite(backdrop("level-06", "daedalus-gate", s), gx, gy)
    for name, x, y in (("dome-a", 52, 178), ("dome-b", 382, 170), ("dome-c", 380, 62)):
        b.glow(x, y, 30, (80, 62, 24), 0.8)
        b.sprite(backdrop("level-06", name, 0.5), x, y)
    b.line([(gx + 14, gy - 14), (gx + 60, gy - 96)], AMBER, 1, 200)
    b.label(gx + 64, gy - 102, "MAIN AIRLOCK - OPEN", AMBER)
    b.line([(gx - 30, gy + 50), (gx - 92, gy + 92)], AMBER, 1, 200)
    b.label(gx - 196, gy + 94, "LIGHTS ON - NOBODY HOME", AMBER)
    px = 470
    b.line([(px - 16, 30), (px - 16, H - 20)], CYAN_DIM, 1, 90)
    b.label(px, 30, "SEARCH - FAR SIDE", WHITE)
    for i, (text, colour) in enumerate((("DAEDALUS RIM", CYAN), ("2000 PEOPLE - NOT FOUND", RED),
                                        ("SETTLEMENTS 2 AND 3", CYAN), ("2000 PEOPLE - NOT FOUND", RED))):
        b.label(px, 50 + 12 * i + 6 * (i > 1), text, colour)
    b.label(px, 118, "NO BODIES - NO WRECKAGE", WHITE)
    b.line([(px, 140), (W - 14, 140)], RED, 1, 160)
    b.label(px, 148, "MISSING: 4000", RED)
    b.draw.rectangle([px - 4, 178, px + 120, 198], outline=AMBER + (230,))
    b.label(px + 4, 184, "FILE: OPEN", AMBER)
    return b


def outro_second_fleet():
    """Act 1 outro p3: Varga's long-range plot: the second fleet beyond the Moon, ten times the
    destroyed carrier group, its track ending in Earth's atmosphere."""
    b = Board()
    b.planet(60, 146, 74, "earth", seed=19)
    b.ring(60, 146, 84, colour=CYAN, alpha=120)
    b.planet(400, 58, 16, "moon", seed=5)
    b.title("LONG RANGE - SECOND FLEET")
    lx, ly = 350, 84
    b.ring(lx, ly, 20, colour=GREY, alpha=180)
    b.marker(lx, ly, "cross", GREY, 5)
    b.label(lx - 60, ly - 36, "CARRIER GROUP", GREY)
    b.label(lx - 60, ly - 26, "DESTROYED", GREEN)
    fx, fy, rx, ry = 584, 134, 60, 66
    b.glow(fx, fy, 70, (60, 24, 90), 0.9)
    rng = np.random.default_rng(24)
    a, r = rng.uniform(0, 2 * np.pi, 70), np.sqrt(rng.uniform(0.02, 1, 70))
    for x, y in zip(fx + rx * r * np.cos(a), fy + ry * r * np.sin(a)):
        b.marker(x, y, "dot", VIOLET, 1)
    b.ring(fx, fy, rx + 6, ry + 6, RED, 200, 1)
    b.label(fx - rx - 4, fy + ry + 12, "SECOND FLEET", RED)
    b.label(fx - rx - 4, fy + ry + 22, "10X THE CARRIER GROUP", WHITE)
    b.label(W - 10, 9, "COURSE: EARTH'S ATMOSPHERE", RED, right=True)
    ex, ey = 60 + 84 * np.cos(np.radians(-10)), 146 + 84 * np.sin(np.radians(-10))
    p0, p1, p2 = np.array([fx - rx - 8, fy + 10.0]), np.array([330, 230.0]), np.array([ex + 4, ey + 6])
    track = [tuple((1 - t) ** 2 * p0 + 2 * (1 - t) * t * p1 + t ** 2 * p2) for t in np.linspace(0, 1, 11)]
    b.dashed(track[:-1], RED, 2)
    b.arrow(*track[-2], *track[-1], RED, dashed=False)
    b.marker(ex, ey, "diamond", AMBER, 5)
    b.label(ex + 16, ey - 30, "IT ENDS IN THE AIR", AMBER)
    b.label(ex + 16, ey - 20, "NOT IN ORBIT", AMBER)
    b.label(10, H - 16, "EARTH", CYAN_DIM)
    b.label(424, 54, "LUNA", CYAN_DIM)
    b.label(100, 222, "ATMOSPHERE", CYAN_DIM)
    b.label(260, H - 16, "THEY'RE COMING TO LAND", RED)
    return b


def outro_rook():
    """Act 1 outro p4: Aegis Wing reassigned to Earth defence; Rook's craft (Ember, the chosen round-09
    sprite from vfx_r08) in the wing slot beside and behind Lancer's Stormhawk, his radio portrait."""
    b = Board()
    b.planet(336, 520, 330, "earth", seed=23)
    b.title("AEGIS WING - REASSIGNED: EARTH DEFENCE")
    lx, ly = 250, 102
    rx, ry = 352, 136
    b.glow(lx, ly, 60, (10, 40, 60), 0.6)
    b.ring(rx, ry, 34, colour=CYAN_DIM, alpha=160)
    b.dashed([(lx + 30, ly + 14), (rx - 28, ry - 10)], CYAN_DIM, 1, 3, 3)
    b.sprite(asset("ship_2", 2), lx, ly)
    b.sprite(sprite.enlarge(v8.rook_sprite(0)[1], 2), rx, ry)
    b.label(lx - 18, ly + 52, "LANCER", CYAN)
    b.label(rx + 44, ry - 4, "ROOK - YOUR WING", AMBER)
    b.label(rx + 44, ry + 6, "FROM TOMORROW", AMBER)
    px, py = 584, 92
    b.sprite(Image.open(SPRITES / "portraits" / "radio-rook-neutral.png").convert("RGBA"), px, py)
    b.bracket(px - 42, py - 42, px + 42, py + 42)
    b.label(px - 42, py + 50, "LT. K. TANAKA", WHITE)
    b.label(px - 42, py + 60, "'ROOK'", CYAN)
    b.label(10, 30, "AEGIS WING", WHITE)
    b.label(10, 40, "OUT OF CISLUNAR SPACE", CYAN_DIM)
    b.label(10, 50, "EFFECTIVE NOW", AMBER)
    b.arrow(60, 66, 150, 186, CYAN)
    b.label(10, 210, "WE MEET THEM ON THE GROUND", RED)
    b.label(W - 10, 9, "KEEP HIM OUT OF TROUBLE", AMBER, right=True)
    return b


# --------------------------------------------------------------------------- Act 2 (M5 part B)

def fill(b, points, colour, alpha=1.0):
    """A polygon filled into the display layer (``b.base``), blended by ``alpha``."""
    mask = Image.new("L", (W, H), 0)
    ImageDraw.Draw(mask).polygon([tuple(p) for p in points], fill=255)
    m = np.array(mask).astype(np.float64)[..., None] / 255 * alpha
    b.base = b.base * (1 - m) + np.array(colour, float) * m


def creeper_lit(frame, zoom):
    """A Creeper frame with its gland and eye glow added as the game blends it, palettised."""
    body = np.array(asset(f"creeper_{frame}", zoom)).astype(np.float64)
    glow = np.array(asset(f"creeper-glow_{frame}", zoom)).astype(np.float64)
    body[..., :3] = np.clip(body[..., :3] + glow[..., :3], 0, 255)
    return artkit.quantize_set([Image.fromarray(body.astype(np.uint8))], 48)[0]


def rook_craft(zoom=1):
    """Rook's craft, the production Ember sprite (tools/art/rook.py), level."""
    return asset("rook_2", zoom)


def landfall():
    """Act 2 p1: the landers coming down at dawn, seen from below the cloud deck over the Gulf of
    Guinea: dozens of burning trails, the CDF tracking overlay counting them."""
    b = Board()
    hy = 196
    sea = b.yy > hy
    b.base = np.where(sea[..., None], np.dstack([4 + 0 * b.yy, 12 + 0 * b.yy, 24 + 10 * (b.yy - hy) / 44]), b.base)
    b.glow(470, hy, 150, (80, 40, 16), 0.9)                       # dawn under the deck, to the east
    deck = raster.fbm(W, H, 24, 811, octaves=4, period=False)
    band = np.exp(-((b.yy - 44) / 15) ** 2) * (0.45 + 0.8 * deck)
    b.base += band[..., None] * np.array([26, 46, 70])
    for y in range(hy + 6, H - 6, 7):                             # the swell, foreshortened
        b.dashed([(10, y), (466, y)], CYAN_DIM, 1, 14 + (y - hy), 6 + (y - hy) // 2)
    b.title("GULF OF GUINEA - 04:00, BELOW THE CLOUD DECK")
    b.line([(0, hy), (470, hy)], CYAN_DIM, 1, 200)
    b.dashed([(0, 64), (470, 64)], CYAN_DIM, 1, 3, 5)
    b.label(12, 68, "CLOUD DECK - 9 KM", CYAN_DIM)
    b.label(12, hy - 12, "SEA LEVEL", CYAN_DIM)
    rng = np.random.default_rng(2185)
    d = np.array([-0.42, 0.91])
    for k in range(34):
        hx = rng.uniform(70, 450)
        hy_ = rng.uniform(78, hy - 8) if k % 3 else rng.uniform(70, 120)
        head = np.array([hx, hy_])
        tail = head - d * (hy_ - 50) / d[1]
        mid = tail + (head - tail) * 0.55
        b.line([tuple(tail), tuple(mid)], RED, 1, 150)
        b.line([tuple(mid), tuple(head)], AMBER, 1, 230)
        b.glow(hx, hy_, 6, (140, 70, 20), 0.9)
        b.marker(hx, hy_, "dot", WHITE, 1)
        if k % 5 == 0:
            b.bracket(hx - 6, hy_ - 6, hx + 6, hy_ + 6, RED, 3)
            b.label(hx + 9, hy_ - 3, f"T{217 + 13 * k:03d}", RED)
    px = 494
    b.line([(px - 14, 26), (px - 14, H - 12)], CYAN_DIM, 1, 90)
    b.label(px, 30, "CDF TRACKING", WHITE)
    b.label(px, 44, "TRACKS IN VIEW: 34", AMBER)
    b.label(px, 56, "TRACKED: 12 DAYS", CYAN)
    b.label(px, 68, "STOPPED: 0", RED)
    b.line([(px, 86), (W - 12, 86)], CYAN_DIM, 1, 120)
    b.label(px, 94, "LANDFALL - 3 ZONES", WHITE)
    for i, zone in enumerate(("GULF OF GUINEA", "JAVA SEA", "RIVER PLATE")):
        b.marker(px + 4, 112 + 14 * i, "chevron", RED, 3)
        b.label(px + 14, 108 + 14 * i, zone, RED if i == 0 else VIOLET)
    b.label(px, 162, "THROUGH THE CLOUD DECK", CYAN_DIM)
    b.label(px, 174, "AT 04:00 LOCAL", CYAN_DIM)
    b.label(W - 10, 9, "THE SECOND FLEET - LANDFALL", RED, right=True)
    b.label(W - 10, H - 16, "WE COULD NOT STOP ONE", AMBER, right=True)
    return b


# Rough coastlines (lon, lat) for the CDF globe display, enough to read the continents.
CONTINENTS = [
    [(-168, 66), (-162, 70), (-140, 70), (-125, 70), (-110, 73), (-95, 72), (-85, 70), (-80, 73), (-65, 62),
     (-60, 55), (-56, 52), (-66, 45), (-70, 42), (-76, 38), (-76, 35), (-81, 31), (-80, 26), (-82, 25),
     (-83, 29), (-90, 30), (-97, 27), (-97, 22), (-92, 18), (-87, 21), (-88, 16), (-83, 10), (-78, 8),
     (-80, 7), (-85, 10), (-92, 14), (-105, 20), (-110, 24), (-112, 29), (-115, 30), (-117, 33), (-121, 35),
     (-124, 40), (-124, 46), (-128, 51), (-135, 57), (-142, 60), (-152, 59), (-158, 56), (-165, 54),
     (-162, 59), (-166, 62)],
    [(-73, 78), (-60, 82), (-30, 83), (-20, 80), (-20, 72), (-25, 68), (-40, 64), (-44, 60), (-50, 62),
     (-55, 68), (-60, 75)],
    [(-80, 9), (-75, 11), (-70, 12), (-62, 11), (-55, 6), (-50, 2), (-45, -2), (-38, -5), (-35, -8),
     (-39, -15), (-41, -22), (-48, -26), (-53, -33), (-57, -35), (-58, -38), (-62, -39), (-65, -42),
     (-66, -47), (-69, -51), (-68, -55), (-72, -54), (-75, -50), (-74, -42), (-72, -30), (-71, -20),
     (-76, -14), (-81, -6), (-80, -1), (-78, 2), (-79, 7)],
    [(-17, 21), (-16, 28), (-10, 32), (-6, 36), (10, 37), (11, 33), (20, 31), (25, 32), (32, 31), (35, 28),
     (39, 20), (43, 12), (51, 12), (46, 4), (41, -2), (40, -10), (40, -16), (35, -24), (32, -29), (27, -34),
     (20, -35), (18, -32), (15, -27), (12, -18), (13, -12), (9, -1), (9, 4), (5, 5), (-2, 5), (-8, 4),
     (-13, 8), (-17, 14)],
    [(-10, 36), (-9, 43), (-2, 44), (-5, 48), (-1, 49), (5, 53), (8, 57), (5, 62), (10, 64), (16, 69),
     (25, 71), (40, 68), (45, 68), (60, 69), (70, 73), (80, 73), (100, 77), (115, 74), (130, 72), (140, 72),
     (160, 70), (170, 70), (180, 68), (180, 65), (177, 62), (163, 60), (160, 55), (156, 51), (155, 57),
     (150, 59), (140, 54), (140, 48), (135, 43), (130, 42), (127, 38), (126, 35), (121, 40), (117, 39),
     (122, 37), (119, 32), (122, 30), (118, 24), (110, 21), (108, 16), (109, 12), (105, 9), (102, 13),
     (100, 13), (98, 8), (103, 1), (100, 3), (98, 10), (97, 17), (92, 22), (88, 22), (80, 15), (77, 8),
     (73, 17), (70, 22), (66, 25), (57, 25), (56, 27), (50, 30), (48, 29), (51, 25), (56, 24), (59, 22),
     (52, 16), (43, 13), (39, 20), (35, 28), (36, 36), (30, 36), (27, 38), (26, 41), (23, 40), (22, 37),
     (20, 40), (16, 38), (13, 41), (13, 45), (19, 42), (12, 46), (9, 44), (3, 43), (-4, 37)],
    [(-5, 50), (1, 51), (1, 53), (-2, 56), (-3, 59), (-6, 58), (-5, 55), (-3, 54)],
    [(95, 5), (98, 4), (104, -2), (106, -6), (102, -4), (96, 2)],
    [(109, 1), (111, 2), (117, 7), (119, 5), (118, 1), (116, -4), (110, -3)],
    [(105, -6), (110, -7), (114, -7), (114, -8), (106, -7)],
    [(131, -1), (141, -3), (150, -10), (142, -9), (138, -8), (132, -4)],
    [(114, -22), (114, -34), (118, -35), (124, -34), (132, -32), (138, -35), (140, -38), (147, -38),
     (150, -37), (153, -30), (153, -25), (146, -19), (142, -11), (141, -17), (136, -12), (130, -12),
     (126, -14), (122, -18)],
    [(130, 31), (135, 34), (140, 36), (142, 40), (141, 45), (140, 41), (136, 36), (131, 34)],
    [(44, -25), (47, -25), (50, -15), (49, -12), (44, -16)],
]
MAP_X, MAP_Y, MAP_K = 30, 26, 1.7      # lon -180..180, lat 80..-45 at 1.7 px a degree


def geo(lon, lat):
    return MAP_X + (lon + 180) * MAP_K, MAP_Y + (80 - lat) * MAP_K


def front_lines():
    """Act 2 p2: the CDF globe display (a flat projection) with the three landing zones glowing and
    the act's fronts: the cities, the Atlantic sea lanes, the Arctic relay chain."""
    b = Board()
    mask = Image.new("L", (W, H), 0)
    md = ImageDraw.Draw(mask)
    for poly in CONTINENTS:
        md.polygon([geo(*p) for p in poly], fill=255)
    land = np.array(mask) > 0
    tex = 0.55 + 0.5 * raster.fbm(W, H, 16, 905, octaves=4, period=False)
    b.base = np.where(land[..., None], raster.ramp(ui_scenes.HOLO, np.clip(tex, 0, 1)), b.base)
    zones = [(3, 3, "GULF OF GUINEA"), (110, -5, "JAVA SEA"), (-56, -35, "RIVER PLATE")]
    for lon, lat, _ in zones:
        b.glow(*geo(lon, lat), 16, (110, 30, 120), 1.1)
    b.title("CDF GLOBAL DISPLAY - THE FRONTS")
    for lon in range(-150, 180, 30):
        b.line([geo(lon, 80), geo(lon, -45)], CYAN_DIM, 1, 50)
    for lat in (60, 30, 0, -30):
        b.line([geo(-180, lat), geo(180, lat)], CYAN_DIM, 1, 50 if lat else 90)
    for poly in CONTINENTS:
        b.line([geo(*p) for p in poly + poly[:1]], CYAN_DIM, 1, 200)
    for i, (lon, lat, name) in enumerate(zones):
        x, y = geo(lon, lat)
        b.ring(x, y, 6, colour=RED, alpha=230, width=2)
        b.ring(x, y, 11, colour=VIOLET, alpha=180)
        b.marker(x, y, "dot", WHITE, 1)
    b.label(geo(3, 3)[0] - 34, geo(3, 3)[1] + 15, "GULF OF GUINEA", RED)
    b.label(geo(110, -5)[0] - 20, geo(110, -5)[1] + 15, "JAVA SEA", RED)
    b.label(geo(-56, -35)[0] + 14, geo(-56, -35)[1] + 2, "RIVER PLATE", RED)
    lagos = geo(3.4, 6.5)
    b.marker(*lagos, "diamond", AMBER, 3)
    b.line([lagos, (lagos[0] - 30, lagos[1] - 6)], AMBER, 1, 200)
    b.label(lagos[0] - 32, lagos[1] - 10, "NOVA LAGOS", WHITE, right=True)
    b.label(lagos[0] - 32, lagos[1], "1 THE CITIES", AMBER, right=True)
    for lon, lat in ((6.1, 46.2), (106.8, -6.2), (-58.4, -34.6)):
        b.marker(*geo(lon, lat), "diamond", AMBER, 2)
    b.label(geo(6.1, 46.2)[0] + 8, geo(6.1, 46.2)[1] - 2, "GENEVA CONCORD", AMBER)
    for path in (((0, 2), (-20, 20), (-35, 38), (-70, 39)), ((0, 2), (-14, 22), (-12, 40), (-6, 47))):
        b.dashed([geo(*p) for p in path], CYAN, 1, 4, 3)
    b.label(geo(-62, 25)[0], geo(-62, 25)[1], "2 THE SEA LANES", CYAN)
    relays = [(-70, 74), (-40, 76), (-10, 75), (20, 76), (50, 75), (80, 76), (110, 75)]
    b.line([geo(*p) for p in relays], GREEN, 1, 200)
    for p in relays:
        b.marker(*geo(*p), "square", GREEN, 2)
    b.label(geo(120, 75)[0], geo(120, 75)[1] - 4, "3 THE ARCTIC RELAYS", GREEN)
    b.label(W - 10, 9, "WHERE THE LINE IS BREAKING", AMBER, right=True)
    b.label(10, H - 16, "LANDING ZONES", RED)
    return b


def person(b, px, py, tall, waving):
    """A small figure standing at (px, py), head up (looking at the sky), maybe an arm raised."""
    w = max(1.0, tall * 0.28)
    r = max(1.0, tall * 0.13)
    hip, neck = py - tall * 0.38, py - tall * 0.78
    colour = WHITE + (240,)
    b.draw.line([(px - w * 0.3, py), (px - w * 0.2, hip)], fill=colour, width=1)
    b.draw.line([(px + w * 0.3, py), (px + w * 0.2, hip)], fill=colour, width=1)
    b.draw.rectangle([px - w / 2, neck, px + w / 2, hip], fill=colour)
    b.draw.ellipse([px - r, neck - 2 * r - 1, px + r, neck - 1], fill=colour)
    if waving:
        b.draw.line([(px + w / 2, neck + 1), (px + w / 2 + tall * 0.25, neck - tall * 0.4)], fill=colour, width=1)


def over_home():
    """Act 2 p3: a city street at night from rooftop height, people on the low roofs and the
    balconies looking up as a Stormhawk passes low over the street."""
    b = Board()
    vx, vy, f, cam = 336.0, 118.0, 210.0, 2.2

    def at(x, y, z):
        return vx + f * x / z, vy - f * (y - cam) / z

    b.glow(vx, vy, 100, (90, 38, 12), 0.9)                       # the city burning far off
    fill(b, [at(-1.5, 0, 1.0), at(1.5, 0, 1.0), at(1.5, 0, 60), at(-1.5, 0, 60)], (8, 10, 22))
    rng = np.random.default_rng(3108)
    blocks = []
    for side in (-1, 1):
        z = 2.0 if side < 0 else 1.6
        low = True                                              # the near ones low, with people
        while z < 36:
            depth = rng.uniform(1.4, 2.4) * (1 + z / 10)
            hgt = rng.uniform(1.1, 1.7) if low else rng.uniform(3.2, 6.5)
            blocks.append((side, z, z + depth, hgt))
            low = not low if rng.random() < 0.8 else low
            z += depth + 0.1
    people = []
    for side, z0, z1, hgt in sorted(blocks, key=lambda blk: -blk[1]):   # far ones first
        x = side * 1.6
        face = [at(x, 0, z0), at(x, 0, z1), at(x, hgt, z1), at(x, hgt, z0)]
        fill(b, face, (26, 36, 74) if side < 0 else (18, 26, 58))
        top = [at(x, hgt, z0), at(x, hgt, z1), at(x + side * 4, hgt, z1), at(x + side * 4, hgt, z0)]
        if hgt < cam:                                             # a low roof, seen from above
            fill(b, top, (34, 46, 86))
            if z0 < 12:
                for _ in range(int(rng.integers(3, 7))):
                    people.append((x + side * rng.uniform(0.15, 2.2), hgt, rng.uniform(z0 + 0.2, z1 - 0.2), True))
        rows = int(hgt / 0.5)
        for fl in range(rows):
            yf = 0.25 + fl * 0.5
            for c in range(int((z1 - z0) / 0.45)):
                zc = z0 + 0.2 + c * 0.45
                if rng.random() > 0.32:
                    continue
                wx, wy = at(x, yf, zc)
                size = max(1, int(round(f * 0.07 / zc)))
                col = AMBER if rng.random() < 0.75 else CYAN
                b.draw.rectangle([wx, wy - size, wx + size - 1, wy], fill=col + (210,))
            if hgt > cam and 0 < fl < rows - 1 and fl % 2 == 0 and z0 < 7 and rng.random() < 0.5:
                b.line([at(x, yf - 0.2, z0 + 0.2), at(x, yf - 0.2, z0 + 1.0)], GREY, 1, 230)   # a balcony
                people.append((x, yf - 0.2, z0 + 0.6, False))
        b.line([at(x, hgt, z0), at(x, hgt, z1)], CYAN, 1, 220)
        b.line([at(x, 0, z0), at(x, hgt, z0)], CYAN_DIM, 1, 160)
    for x, y, z, waving in sorted(people, key=lambda p: -p[2]):
        person(b, *at(x, y, z), max(5.0, f * 0.36 / z), waving and rng.random() < 0.45)
    for z in np.arange(1.6, 30, 1.8):                              # street lamps and the lane marks
        for side in (-1, 1):
            b.glow(*at(side * 1.3, 0.8, z), max(1.5, 22 / z), (130, 86, 30), 0.8)
        b.line([at(0, 0, z), at(0, 0, z + 0.7)], CYAN_DIM, 1, 140)
    b.title("NOVA LAGOS - A STREET, ROOFTOP HEIGHT")
    sx, sy = 400, 52
    b.glow(sx, sy + 26, 12, (40, 90, 140), 0.8)
    b.sprite(asset("ship_2", 2), sx, sy)
    b.label(sx + 52, sy - 14, "LANCER - LOW PASS", CYAN)
    b.label(sx + 52, sy - 4, "60 M OVER THE ROOFS", CYAN_DIM)
    b.label(10, 30, "PEOPLE ON THE ROOFS", WHITE)
    b.label(10, 40, "LOOKING UP", WHITE)
    b.label(10, 50, "WATCHING THE SKY", AMBER)
    b.label(W - 10, 9, "OVER HOME NOW", CYAN_DIM, right=True)
    b.label(W - 10, H - 16, "FLY ACCORDINGLY", AMBER, right=True)
    return b


def scramble():
    """Act 2 p4: Aegis Wing's Stormhawks and Rook's Ember on a coastal CDF airbase at dusk,
    canopies closing, Nova Lagos burning on the horizon."""
    b = Board()
    b.glow(W + 20, 110, 170, (100, 44, 16), 1.0)                  # dusk and the fires to the east
    coast = 70 + 14 * np.sin(b.yy / 23) + 6 * np.sin(b.yy / 9)
    b.base = np.where((b.xx < coast)[..., None], b.base * 0.55 + np.array([2, 8, 22]), b.base)
    b.title("CDF COASTAL AIRBASE - DUSK")
    b.line([(coast[y, 0], y) for y in range(20, H, 4)], CYAN_DIM, 1, 200)
    b.label(12, H - 16, "GULF OF GUINEA", CYAN_DIM)
    ry = 206
    b.draw.rectangle([120, ry - 9, 560, ry + 9], outline=CYAN_DIM + (220,))
    b.dashed([(130, ry), (550, ry)], CYAN_DIM, 1, 8, 6)
    b.label(124, ry - 22, "RUNWAY", CYAN_DIM)
    pads = [("LANCER", "ship_2", CYAN), ("ROOK", "rook", AMBER), ("AEGIS TWO", "ship_2", GREY),
            ("AEGIS 3", "ship_2", GREY), ("AEGIS 4", "ship_2", GREY)]
    for i, (name, kind, colour) in enumerate(pads):
        x, y = 170 + 76 * i, 112
        b.draw.rectangle([x - 28, y - 30, x + 28, y + 30], outline=CYAN_DIM + (180,))
        b.sprite(rook_craft() if kind == "rook" else asset("ship_2"), x, y)
        b.label(x - 26, y + 36, name, colour)
        b.label(x - 26, y + 46, "CANOPY", GREEN if i < 2 else CYAN_DIM)
        b.dashed([(x, y + 30), (x, ry - 10)], CYAN_DIM, 1, 3, 3)
        if i < 2:
            b.bracket(x - 30, y - 32, x + 30, y + 32, colour, 5)
    b.label(130, 50, "AEGIS WING - SCRAMBLE", WHITE)
    b.label(130, 60, "CANOPIES CLOSING", GREEN)
    cx, cy = 628, 100
    for k, (dx, dy) in enumerate(((-18, -10), (-6, 6), (8, -16), (14, 12), (-14, 18), (2, -2))):
        b.draw.rectangle([cx + dx - 4, cy + dy - 4, cx + dx + 4, cy + dy + 4], outline=GREY + (200,))
        if k % 2 == 0:
            b.glow(cx + dx, cy + dy, 9, (150, 60, 14), 0.9)
            b.marker(cx + dx, cy + dy, "dot", RED, 1)
    b.label(W - 10, 140, "NOVA LAGOS", RED, right=True)
    b.label(W - 10, 150, "BURNING - 40 KM", AMBER, right=True)
    b.arrow(566, ry, 610, 126, CYAN)
    b.label(W - 10, 9, "WE CAN STOP THEM STAYING", AMBER, right=True)
    return b


def nova_lagos():
    """L08 p1: the night route over Nova Lagos: in over the harbour, along the elevated highways,
    through the tower district to the Third Mainland highway; the Ikoyi shelters at its far end,
    the walkers heading for them; Lancer and Rook."""
    b = Board()
    lagoon = (b.yy > 150 + 30 * np.sin(b.xx / 90)) & (b.xx > 430)
    sea = b.yy > 196 - 0.12 * b.xx
    water = lagoon | sea
    b.base = np.where(water[..., None], b.base * 0.5 + np.array([2, 8, 24]), b.base)
    rng = np.random.default_rng(808)
    for _ in range(170):                                          # the city's lights
        x, y = rng.uniform(20, 650), rng.uniform(22, 220)
        if not water[int(y), int(x)]:
            b.glow(x, y, rng.uniform(2, 5), (90, 70, 30), 0.5)
    for x, y in ((450, 120), (500, 96), (560, 120)):
        b.glow(x, y, 26, (60, 50, 50), 0.7)                       # smoke over the district
    b.title("NOVA LAGOS - TONIGHT'S ROUTE")
    b.label(14, H - 16, "GULF OF GUINEA", CYAN_DIM)
    b.label(520, H - 16, "LAGOS LAGOON", CYAN_DIM)
    for x in (40, 70, 100):                                       # the harbour piers and cranes
        b.draw.rectangle([x, 178 - 0.12 * x, x + 10, 214 - 0.12 * x], outline=CYAN_DIM + (220,))
        b.line([(x + 5, 182 - 0.12 * x), (x + 5, 168 - 0.12 * x), (x + 12, 168 - 0.12 * x)], CYAN, 1, 220)
    b.label(18, 152, "HARBOUR", CYAN)
    hw = [(110, 150), (170, 128), (240, 132), (290, 112)]
    for off in (-3, 3):
        b.line([(x, y + off) for x, y in hw], CYAN_DIM, 1, 220)
    b.label(170, 140, "ELEVATED HIGHWAYS", CYAN_DIM)
    for x, y, s in ((300, 70, 14), (322, 96, 10), (346, 66, 18), (370, 100, 12), (330, 124, 9), (392, 74, 11),
                    (304, 100, 8), (360, 130, 8)):
        b.draw.rectangle([x - s, y - s, x + s, y + s], outline=CYAN + (220,))
        b.draw.rectangle([x - s + 3, y - s + 3, x + s - 3, y + s - 3], outline=CYAN_DIM + (160,))
    b.label(300, 30, "TOWER DISTRICT", CYAN)
    b.label(452, 72, "SMOKE", GREY)
    tm = [(420, 130), (470, 150), (540, 170), (600, 160), (630, 130)]
    for off in (-3, 3):
        b.line([(x, y + off) for x, y in tm], CYAN, 1, 230)
    b.label(450, 184, "THIRD MAINLAND HIGHWAY", CYAN)
    sx, sy = 636, 108
    b.draw.rectangle([sx - 18, sy - 12, sx + 18, sy + 12], outline=AMBER + (240,))
    b.marker(sx, sy, "diamond", AMBER, 4)
    b.label(W - 10, 76, "IKOYI SHELTERS", AMBER, right=True)
    b.label(W - 10, 86, "CIVILIANS", GREEN, right=True)
    route = [(60, 200), (110, 150), (170, 128), (240, 132), (290, 112), (350, 112), (420, 130), (470, 150),
             (540, 170), (600, 160), (618, 128)]
    b.dashed(route[:-1], AMBER, 2, 7, 4)
    b.arrow(*route[-2], *route[-1], AMBER, dashed=False)
    for x, y in ((250, 108), (380, 150), (420, 104), (500, 132), (560, 146)):
        b.marker(x, y, "chevron", RED, 4)
        b.arrow(x + 7, y - 2, x + 27, y - 2, RED, size=3)
    b.label(250, 210, "WALKERS - HEADING FOR THE SHELTERS", RED)
    b.sprite(asset("ship_2"), 40, 92)
    b.sprite(rook_craft(), 76, 104)
    b.label(14, 58, "LANCER", CYAN)
    b.label(66, 124, "ROOK", AMBER)
    b.label(W - 10, 9, "FIRST SORTIE WITH ROOK", AMBER, right=True)
    b.label(W - 10, 22, "NIGHT - FLY LOW", CYAN_DIM, right=True)
    return b


def creeper_walker_scan():
    """L08 p2: Varga's scan of the Creeper: on a low roof, its five-way fan aimed at the ship; a
    convoy's fans 0.5 s apart; anything that hits the ground hurts it twice as hard."""
    b = Board()
    b.title("SENSOR SCAN - CREEPER")
    cx, cy = 196, 104
    b.glow(cx, cy, 70, (10, 40, 50), 0.6)
    b.draw.rectangle([cx - 80, cy - 44, cx + 80, cy + 44], outline=GREY + (200,))
    b.label(cx + 20, cy + 48, "LOW ROOF", GREY)
    creeper = creeper_lit(4 * 6, 2)                               # heading 4: walks to the left
    b.sprite(creeper, cx, cy)
    gland = np.array([cx - 46, cy + 1.0])
    ship = np.array([84.0, 190])
    b.sprite(asset("ship_2"), *ship)
    aim = np.arctan2(*(ship - gland)[::-1])
    orb = asset("orb_0")
    for k in range(5):
        a = aim + np.radians(-25 + 12.5 * k)
        u = np.array([np.cos(a), np.sin(a)])
        b.dashed([tuple(gland + u * 8), tuple(gland + u * 92)], VIOLET, 1, 3, 3)
        b.sprite(orb, *(gland + u * 66))
    b.label(cx - 150, 26, "CREEPER", WHITE)
    b.label(cx - 150, 36, "CRAWLS THE STREETS", CYAN_DIM)
    b.label(cx - 150, 46, "AND THE LOW ROOFS", CYAN_DIM)
    b.label(cx - 20, 168, "5-WAY FAN, AIMED AT YOU", VIOLET)
    b.label(cx - 20, 178, "EVERY 3 SECONDS", VIOLET)
    b.line([(cx - 40, cy - 12), (cx - 10, 50)], AMBER, 1, 200)
    b.label(cx - 14, 40, "FAN GLAND", AMBER)
    bx, by = 330, 40
    b.draw.rectangle([bx - 4, by - 6, bx + 114, by + 26], outline=GREEN + (240,))
    b.sprite(asset("bomb-rack-shot"), bx + 8, by + 10)
    b.label(bx + 18, by - 1, "ANTI-GROUND", GREEN)
    b.label(bx + 18, by + 9, "DAMAGE X2", GREEN)
    b.arrow(bx - 6, by + 18, cx + 64, cy - 18, GREEN)
    sy = 150
    b.line([(400, sy - 16), (W - 8, sy - 16)], CYAN_DIM, 1, 120)
    b.line([(400, sy + 16), (W - 8, sy + 16)], CYAN_DIM, 1, 120)
    b.label(404, sy + 20, "STREET", CYAN_DIM)
    small = asset(f"creeper_{4 * 6}")
    ship2 = np.array([448.0, 214])
    b.sprite(asset("ship_2"), *ship2)
    for i, x in enumerate((486, 554, 622)):
        b.sprite(small, x, sy)
        g = np.array([x - 23, sy + 1.0])
        aim = np.arctan2(*(ship2 - g)[::-1])
        r = (1.0 - 0.5 * i) * 40 + 10                         # the first volley furthest out
        for k in range(5):
            a = aim + np.radians(-25 + 12.5 * k)
            u = np.array([np.cos(a), np.sin(a)])
            b.line([tuple(g + u * 5), tuple(g + u * (r - 3))], VIOLET, 1, 110)
            b.marker(*(g + u * r), "dot", VIOLET, 2)
        b.label(x - 20, sy - 30, f"T+{0.5 * i:.1f} S", AMBER)
    b.label(404, 92, "CONVOY: FANS 0.5 S APART", WHITE)
    b.label(404, 102, "FRONT TO BACK", CYAN_DIM)
    b.label(W - 10, 9, "GROUND LAYER", CYAN_DIM, right=True)
    b.label(10, H - 16, "EVERY ONE YOU STOP NEVER REACHES THE SHELTERS", AMBER)
    return b


def arcology_district():
    """L09 p1: the arcology district before dawn, the grey glow in the east: the route in along the
    ruined elevated highway, Unity Plaza (cluster A), the boulevard, the lagoon and the Okonjo Bridge
    with the Kilo convoy (cluster B at its bridgehead), the Ndidi Arcology's lobby plaza (cluster C);
    Lancer and Rook at the start."""
    b = Board()
    b.glow(W + 40, 120, 260, (40, 44, 58), 0.9)                   # the grey glow in the east
    lagoon = (b.xx > 360 + 22 * np.sin(b.yy / 40)) & (b.xx < 520 + 18 * np.sin(b.yy / 55 + 1))
    b.base = np.where(lagoon[..., None], b.base * 0.5 + np.array([4, 10, 28]) + (b.xx / W)[..., None] * 14, b.base)
    rng = np.random.default_rng(909)
    for _ in range(150):                                          # the district's last lights
        x, y = rng.uniform(20, 650), rng.uniform(22, 220)
        if not lagoon[int(y), int(x)]:
            b.glow(x, y, rng.uniform(2, 4), (80, 64, 30), 0.4)
    for x, y in ((210, 60), (580, 70), (600, 150)):
        b.glow(x, y, 24, (55, 50, 52), 0.6)                       # smoke
    b.title("NOVA LAGOS - ARCOLOGY DISTRICT - BEFORE DAWN")
    hw = [(20, 210), (80, 180), (130, 150)]                       # the elevated highway in
    for off in (-3, 3):
        b.line([(x, y + off) for x, y in hw], CYAN_DIM, 1, 220)
    b.label(82, 196, "ELEVATED HIGHWAY", CYAN_DIM)                # below the highway, clear of ROOK
    px, py = 180, 120                                             # Unity Plaza
    b.draw.rectangle([px - 34, py - 26, px + 34, py + 26], outline=CYAN + (220,))
    b.draw.ellipse([px - 7, py - 7, px + 7, py + 7], outline=CYAN_DIM + (200,))
    b.label(px - 34, py + 32, "UNITY PLAZA", CYAN)
    bl = [(214, 112), (270, 96), (330, 96), (366, 100)]           # the boulevard
    for off in (-4, 4):
        b.line([(x, y + off) for x, y in bl], CYAN_DIM, 1, 220)
    b.label(244, 74, "BOULEVARD", CYAN_DIM)
    bx = 440                                                      # the Okonjo Bridge over the lagoon
    for off in (-4, 4):
        b.line([(bx - 40 + off, 196), (bx + 40 + off, 40)], CYAN, 1, 230)
    b.label(436, 206, "OKONJO BRIDGE", CYAN)
    b.label(380, 226, "LAGOS LAGOON", CYAN_DIM)
    for k in range(4):                                            # the Kilo convoy on it
        t = 0.25 + 0.12 * k
        cx, cy = bx - 40 + 80 * t, 196 - 156 * t
        b.draw.rectangle([cx - 2, cy - 3, cx + 2, cy + 3], fill=GREEN + (255,))
    b.label(468, 120, "KILO CONVOY", GREEN)
    b.label(468, 130, "60 CIVILIANS", GREEN)
    ax, ay = 600, 96                                              # the Ndidi Arcology
    for r in (22, 15, 8):
        b.draw.rectangle([ax - r, ay - r, ax + r, ay + r], outline=(VIOLET if r == 22 else CYAN_DIM) + (230,))
    b.glow(ax, ay, 22, (60, 20, 70), 0.7)
    b.label(W - 10, 30, "NDIDI ARCOLOGY", VIOLET, right=True)
    b.label(W - 10, 40, "HOLLOW WITH CREEP", GREY, right=True)
    clusters = {"A": [(166, 112), (194, 128)], "B": [(470, 60), (492, 72)], "C": [(574, 122), (590, 134)]}
    for name, nodes in clusters.items():
        for x, y in nodes:
            b.glow(x, y, 8, (40, 10, 60), 0.9)
            b.marker(x, y, "diamond", AMBER, 4)
        cx = sum(x for x, _ in nodes) / 2
        cy = min(y for _, y in nodes) - 14
        b.label(int(cx) - 3, int(cy) - 4, name, AMBER)
    b.label(500, 84, "BRIDGEHEAD", CYAN_DIM)
    route = [(30, 214), (80, 184), (130, 154), (180, 120), (270, 100), (366, 100), (420, 120), (480, 66),
             (540, 90), (596, 128)]
    b.dashed(route[:-1], AMBER, 2, 7, 4)
    b.arrow(*route[-2], *route[-1], AMBER, dashed=False)
    b.sprite(asset("ship_2"), 40, 120)
    b.sprite(rook_craft(), 72, 132)
    b.label(14, 88, "LANCER", CYAN)
    b.label(90, 150, "ROOK", AMBER)
    b.label(W - 10, 9, "DESTROY 6 HIVE NODES", AMBER, right=True)
    b.label(W - 10, 216, "LAST HOUR OF NIGHT", GREY, right=True)
    return b


def node_lit(frame, zoom):
    """A Hive Node frame with its glow added as the game blends it, palettised."""
    body = np.array(asset(f"hive-node_{frame}", zoom)).astype(np.float64)
    glow = np.array(asset(f"hive-node-glow_{frame}", zoom)).astype(np.float64)
    body[..., :3] = np.clip(body[..., :3] + glow[..., :3] * glow[..., 3:4] / 255, 0, 255)
    return artkit.quantize_set([Image.fromarray(body.astype(np.uint8))], 48)[0]


def node_scan():
    """L09 p2: Varga's scan: a Hive Node with its iris open (hardened: normal rounds spark off), its
    iris cycle breeding Skitters, the anti-ground sources that crack it; a Ravager's pounce arc at
    the ship with its air window."""
    b = Board()
    b.title("SENSOR SCAN - HIVE NODE / RAVAGER")
    nx, ny = 104, 120
    b.glow(nx, ny, 70, (6, 40, 34), 0.8)
    creep = asset("hive-node-creep_0")
    b.sprite(creep, nx, ny)
    b.sprite(node_lit(20, 2), nx, ny)
    b.label(14, 26, "HIVE NODE", WHITE)
    b.label(14, 36, "HARDENED SPAWNER", CYAN_DIM)
    b.draw.rectangle([10, 210, 78, 224], outline=GREY + (230,))
    b.label(14, 214, "HARDENED", GREY)
    b.line([(44, 210), (66, 180)], GREY, 1, 200)
    for k, (fx, it) in enumerate(((0, "IRIS SHUT"), (20, "OPEN"))):              # the iris cycle
        x = 252 + k * 74
        b.sprite(node_lit(fx, 1), x, 176)
        b.label(x - 26, 218, it, CYAN_DIM)
    b.arrow(282, 176, 300, 176, CYAN_DIM, size=3)
    for dx, dy in ((372, 160), (390, 186)):
        b.sprite(asset("skitter_0"), dx, dy)
        b.arrow(352, 176, dx - 8, dy, VIOLET, size=2)
    b.label(352, 208, "EVERY 4 S:", VIOLET)
    b.label(352, 218, "2 SKITTERS", VIOLET)
    gx, gy = 200, 30                                              # what cracks it
    b.draw.rectangle([gx - 4, gy - 6, gx + 196, gy + 70], outline=GREEN + (240,))
    b.label(gx + 2, gy - 1, "ONLY ANTI-GROUND CRACKS IT", GREEN)
    b.sprite(asset("bomb-rack-shot"), gx + 12, gy + 22)
    b.label(gx + 26, gy + 18, "BOMBS", GREEN)
    b.sprite(asset("hammer-mortar-shot"), gx + 12, gy + 38)
    b.label(gx + 26, gy + 34, "MORTARS, ROOK'S TOO", GREEN)
    b.sprite(asset("airstrike-bomber_0"), gx + 170, gy + 36)
    b.label(gx + 26, gy + 50, "THE AIRSTRIKE", GREEN)
    b.label(gx - 4, gy + 80, "NORMAL ROUNDS SPARK OFF", GREY)
    rx, ry = 446, 76                                              # the Ravager's pounce
    ship = np.array([630.0, 200])
    b.label(422, 26, "RAVAGER", WHITE)
    b.label(422, 36, "PACK HUNTER", CYAN_DIM)
    b.sprite(asset(f"ravager_{14 * 8}", 1), rx, ry)
    pts = []
    for k in range(25):
        s_ = k / 24
        x = rx + 20 + (ship[0] - 20 - rx - 20) * s_
        y = ry + 16 + (ship[1] - 18 - ry - 16) * s_ - math_sin(s_) * 34
        pts.append((x, y))
    air0, air1 = int(24 * 0.3), int(24 * 0.7)
    b.dashed(pts[:air0 + 1], CYAN_DIM, 1, 4, 3)
    b.line(pts[air0:air1 + 1], RED, 3)
    b.dashed(pts[air1:], CYAN_DIM, 1, 4, 3)
    b.sprite(asset(f"ravager-leap_{14 * 4 + 1}"), *pts[3])
    b.sprite(asset("ship_2"), *ship)
    b.label(452, 196, "AIR 0.3 S:", RED)
    b.label(452, 206, "IT HITS YOU", RED)
    b.label(W - 10, 26, "POUNCES WITHIN", AMBER, right=True)
    b.label(W - 10, 36, "200 PX", AMBER, right=True)
    b.label(422, 224, "SHOOT IT BEFORE IT JUMPS", AMBER)
    b.label(W - 10, 9, "GROUND LAYER", CYAN_DIM, right=True)
    return b


def math_sin(s_):
    return float(np.sin(np.pi * s_))


def scaled_sprite(name, scale, colours=48):
    """A production sprite at ``scale`` (box-filtered, alpha stepped), palettised."""
    img = asset(name)
    w, h = max(1, round(img.width * scale)), max(1, round(img.height * scale))
    a = np.array(img.resize((w, h), Image.BOX)).astype(np.float64)
    a[..., 3] = np.where(a[..., 3] >= 128, 255, 0)
    return artkit.quantize_set([Image.fromarray(a.astype(np.uint8), "RGBA")], colours)[0]


def additive(name, scale=1.0, strength=1.0):
    """An additive sprite (the Wraith's shimmer, its decloak flash) as an RGB array premultiplied
    on black, as the game blends it, at ``scale``."""
    img = asset(name)
    if scale != 1.0:
        img = img.resize((max(1, round(img.width * scale)), max(1, round(img.height * scale))), Image.BOX)
    a = np.array(img).astype(np.float64)
    return a[..., :3] * a[..., 3:4] / 255 * strength


def light_at(b, rgb, cx, cy, layer="over"):
    b.add_light(rgb, cx - rgb.shape[1] / 2, cy - rgb.shape[0] / 2, layer)


DAWN = (70, 52, 40)   # the first light in the east (Level 10)


def evacuation_route():
    """L10 p1: the evacuation corridor at first light, west to east: Eko spaceport with its five
    pads and the shuttles on them, the suburbs with their jammed highways and refugee lights, the
    maglev viaduct between the two creep districts, the coast road with the capsized ferry on its
    sandbar, the lagoon and the climb-out over the sea; the five shuttles' route, Lancer and Rook
    with them, the contacts closing from behind."""
    b = Board()
    b.glow(W + 60, 110, 300, DAWN, 1.0)                           # first light in the east
    b.glow(W + 20, 40, 120, (60, 44, 46), 0.6)
    coast = 520 + 16 * np.sin(b.yy / 46) + 0.18 * (b.yy - 120)
    water = b.xx > coast
    b.base = np.where(water[..., None], b.base * 0.55 + np.array([4, 12, 30]) + ((b.xx - 520) / 150).clip(0, 1)[..., None]
                      * np.array([22, 18, 16]), b.base)
    rng = np.random.default_rng(1010)
    for _ in range(120):                                          # the sprawl's last lights
        x, y = rng.uniform(150, 510), rng.uniform(26, 222)
        if not water[int(y), int(x)]:
            b.glow(x, y, rng.uniform(2, 4), (80, 64, 32), 0.35)
    for cx, cy in ((352, 58), (360, 182)):                        # the creep districts either side of the viaduct
        for _ in range(16):
            b.glow(cx + rng.uniform(-44, 44), cy + rng.uniform(-22, 22), rng.uniform(6, 12), (40, 12, 56), 0.5)
    for x, y in ((300, 40), (410, 200), (230, 200)):
        b.glow(x, y, 22, (56, 50, 52), 0.55)                      # smoke
    b.title("NOVA LAGOS - EVACUATION CORRIDOR - FIRST LIGHT")
    px0, py0 = 18, 64                                             # Eko spaceport and its five pads
    b.draw.rectangle([px0, py0, px0 + 104, py0 + 120], outline=CYAN_DIM + (220,))
    b.line([(px0 + 4, py0 + 116), (px0 + 100, py0 + 116)], CYAN_DIM, 1, 160)
    pads = [(70, 86), (44, 116), (96, 116), (44, 154), (96, 154)]  # One leads, Two to Five in a double column
    for x, y in pads:
        b.ring(x, y, 13, colour=CYAN, alpha=200)
        b.ring(x, y, 9, colour=CYAN_DIM, alpha=140)
    b.label(px0, py0 + 126, "EKO SPACEPORT", CYAN)
    b.label(px0, py0 + 136, "5 PADS", CYAN_DIM)
    for y in (100, 136):                                          # the suburbs' jammed highways
        pts = [(124, y), (180, y - 6), (236, y + 4), (288, y - 2)]
        for off in (-3, 3):
            b.line([(x, yy + off) for x, yy in pts], CYAN_DIM, 1, 200)
        for k in range(26):
            s_ = k / 26
            x = 128 + 156 * s_
            yy = np.interp(x, [p[0] for p in pts], [p[1] for p in pts])
            b.draw.point((x, yy + rng.choice([-1, 1])), fill=(AMBER if k % 3 else GREEN) + (255,))
    b.label(150, 74, "SUBURBS", CYAN)
    b.label(150, 84, "ROADS JAMMED", CYAN_DIM)
    vy = 118                                                      # the maglev viaduct
    for off in (-4, 4):
        b.line([(296, vy + off), (452, vy + off)], CYAN, 1, 230)
    for x in range(300, 452, 14):
        b.line([(x, vy - 4), (x, vy + 4)], CYAN_DIM, 1, 140)
    b.label(318, 100, "MAGLEV VIADUCT", CYAN)
    b.label(318, 28, "CREEP DISTRICT", VIOLET)
    b.label(318, 206, "CREEP DISTRICT", VIOLET)
    cr = [(452, vy), (488, 130), (510, 160), (528, 196)]           # the coast road
    for off in (-3, 3):
        b.line([(x + off, y) for x, y in cr], CYAN_DIM, 1, 220)
    b.label(430, 210, "COAST ROAD", CYAN_DIM)
    fx, fy = 566, 166                                             # the capsized ferry on its sandbar
    b.glow(fx, fy + 4, 16, (40, 46, 40), 0.7)
    ang = np.radians(-28)
    hull = [(-22, 0), (-17, -6), (14, -6), (24, 0), (14, 6), (-17, 6)]
    rot = [(fx + x * np.cos(ang) - y * np.sin(ang), fy + x * np.sin(ang) + y * np.cos(ang)) for x, y in hull]
    b.draw.polygon(rot, outline=CYAN + (230,))
    b.line([rot[0], ((rot[2][0] + rot[4][0]) / 2, (rot[2][1] + rot[4][1]) / 2)], CYAN_DIM, 1, 200)   # the keel
    b.label(fx - 24, fy + 18, "CAPSIZED FERRY", CYAN_DIM)
    shore = [(float(520 + 16 * np.sin(y / 46) + 0.18 * (y - 120)), float(y)) for y in range(22, 232, 6)]
    b.dashed(shore, CYAN_DIM, 1, 3, 3)
    b.label(578, 64, "LAGOON", CYAN_DIM)
    b.label(W - 10, 216, "FIRST LIGHT", GREY, right=True)
    route = [(70, 70), (120, 104), (180, 112), (236, 120), (296, 118), (452, 118), (500, 126), (560, 120), (620, 96),
             (640, 50)]
    b.dashed(route[:-1], AMBER, 2, 7, 4)
    b.arrow(*route[-2], *route[-1], AMBER, dashed=False)
    b.label(W - 10, 26, "CLIMB-OUT", AMBER, right=True)
    b.label(W - 10, 36, "OVER THE SEA", AMBER, right=True)
    for x, y in ((150, 160), (330, 156), (420, 150)):             # contacts closing from behind
        b.marker(x, y, "chevron", RED, 4)
        b.arrow(x + 7, y - 4, x + 27, y - 14, RED, size=3)
    b.label(170, 214, "CONTACTS FROM BEHIND", RED)
    shuttle = scaled_sprite("evacuation-shuttle_2", 0.5)
    for k, (x, y) in enumerate(pads):
        b.sprite(shuttle, x, y)
        b.label(x + 12, y - 15, str(k + 1), GREEN)
    b.sprite(asset("ship_2"), 196, 52)
    b.sprite(rook_craft(), 236, 66)
    b.label(176, 22, "LANCER", CYAN)
    b.label(252, 54, "ROOK", AMBER)
    b.label(W - 10, 9, "ESCORT 5 SHUTTLES", AMBER, right=True)
    b.label(px0, 34, "LIFELINE 1-5", GREEN)
    b.label(px0, 44, "1,100 PEOPLE", GREEN)
    return b


def wraith_scan():
    """L10 p2: Varga's scan: the Wraith cloaked (its shimmer on high air: only homing finds it) and
    decloaked; its pass overhead, out the bottom edge, the loop and the rear entry with the 3 s
    warning, the decloak flash and its bursts up the screen through the shuttle band; a Mote
    Swarm sweeping down, out the bottom and back up from behind."""
    b = Board()
    b.title("SENSOR SCAN - WRAITH / MOTE SWARM")
    cx, cy = 58, 110                                              # cloaked
    b.glow(cx, cy, 46, (24, 14, 44), 0.6)
    light_at(b, additive("wraith-cloak_0"), cx, cy)
    b.label(14, 26, "WRAITH", WHITE)
    b.label(14, 36, "CLOAKED: HIGH AIR", VIOLET)
    b.label(14, 164, "ONLY HOMING", GREEN)
    b.label(14, 174, "FINDS IT", GREEN)
    b.sprite(asset("hornet-launcher-shot_0"), 108, 168)
    dx, dy = 158, 110                                             # decloaked
    light_at(b, additive("wraith-decloak_1", 1.0, 0.55), dx, dy, "under")
    b.sprite(asset("wraith_32"), dx, dy)
    b.arrow(96, 110, 116, 110, CYAN_DIM, size=3)
    b.label(124, 152, "DECLOAKED", WHITE)
    b.label(124, 162, "0.4 S FLASH", VIOLET)
    b.label(124, 202, "THEN EVERY GUN", GREEN)
    b.label(124, 212, "HITS IT", GREEN)
    x0, y0, x1, y1 = 232, 28, 404, 192                            # the play field, top down
    b.draw.rectangle([x0, y0, x1, y1], outline=CYAN_DIM + (200,))
    band0, band1 = 52, 96                                         # the shuttle band
    for x in range(x0 + 2, x1 - 1, 6):
        b.line([(x, band0), (x + 4, band0)], GREEN, 1, 110)
        b.line([(x, band1), (x + 4, band1)], GREEN, 1, 110)
    shuttle = scaled_sprite("evacuation-shuttle_2", 0.5)
    for sx, sy in ((292, 64), (344, 64), (318, 84)):
        b.sprite(shuttle, sx, sy)
    b.label(x1 + 6, band0 + 4, "SHUTTLES", GREEN)
    ship = np.array([300.0, 160])
    pts = [(262, y0 - 14), (258, 60), (270, 120), (284, y1), (292, y1 + 26), (330, y1 + 32), (352, y1 + 18),
           (356, y1 - 12)]
    b.dashed(pts[:4], VIOLET, 1, 4, 3)                            # the pass overhead, cloaked
    b.dashed(pts[3:], GREY, 1, 2, 3)                              # the loop, off screen
    light_at(b, additive("wraith-cloak_0", 0.5), 262, 44)
    b.label(176, 38, "PASSES", VIOLET)
    b.label(176, 48, "OVERHEAD", VIOLET)
    b.label(410, y1 + 10, "LOOPS BEHIND", GREY)
    for k in range(3):                                            # the 3 s warning at the bottom edge
        b.marker(340 + 12 * k, y1 - 3, "chevron", AMBER, 3)
    b.label(x1 + 6, y1 - 6, "3 S WARNING", AMBER)
    wx, wy = 356, 168
    light_at(b, additive("wraith-decloak_2", 0.5, 0.8), wx, wy, "under")
    b.sprite(scaled_sprite("wraith_32", 0.5), wx, wy)
    orb = scaled_sprite("orb-medium_0", 0.6)
    for k, ang in enumerate((-100, -112, -124)):                  # its bursts up the screen
        u = np.array([np.cos(np.radians(ang)), np.sin(np.radians(ang))])
        p0 = np.array([wx, wy - 10.0])
        b.dashed([tuple(p0), tuple(p0 + u * 128)], RED, 1, 3, 3)
        for r in (36, 76, 108):
            b.sprite(orb, *(p0 + u * r))
    b.label(x1 + 6, 112, "DECLOAKS AT", VIOLET)
    b.label(x1 + 6, 122, "YOUR SIX", VIOLET)
    b.label(x1 + 6, 136, "BURSTS UP", RED)
    b.label(x1 + 6, 146, "THROUGH THE", RED)
    b.label(x1 + 6, 156, "SHUTTLES", RED)
    b.sprite(asset("ship_2"), *ship)
    mx0 = 500                                                     # the Mote Swarm
    b.label(mx0, 26, "MOTE SWARM", WHITE)
    b.label(mx0, 36, "A FLOCK, NOT", CYAN_DIM)
    b.label(mx0, 46, "A FORMATION", CYAN_DIM)
    path = [(530, 66), (560, 112), (604, 160), (640, 206)]
    back = [(640, 206), (656, 228), (668, 200), (650, 150), (618, 104)]
    b.dashed(path, VIOLET, 1, 4, 3)
    b.dashed(back[:3], GREY, 1, 2, 3)
    b.arrow(*back[2], *back[3], RED, size=3)
    b.arrow(*back[3], *back[4], RED, size=4, dashed=False)
    b.label(mx0, 196, "TURNS BACK", RED)
    b.label(mx0, 206, "FROM BELOW", RED)
    mrng = np.random.default_rng(1012)
    for k in range(16):                                           # the flock, aligned on the route
        s_ = mrng.uniform(0.15, 0.6)
        bx = np.interp(s_, [0, 1 / 3, 2 / 3, 1], [p[0] for p in path]) + mrng.normal(0, 9)
        by = np.interp(s_, [0, 1 / 3, 2 / 3, 1], [p[1] for p in path]) + mrng.normal(0, 7)
        heading = 14 + int(mrng.integers(-1, 2))                  # flying down and to the right
        b.sprite(asset(f"mote-swarm_{(heading % 16) * 3 + k % 3}"), bx, by)
    b.sprite(asset("mote-swarm_25", 2), 646, 72)
    b.label(W - 10, 9, "FROM BEHIND", AMBER, right=True)
    b.label(10, H - 16, "FIT A REAR GUN, LANCER", AMBER)
    return b

# the convoy's ship pair: variant a's names, which convoy_ships.py writes (``--variant b`` writes b's under the
# same names, so switching the pair means re-running convoy_ships.py --variant b and this script again)
CARGO, FRIGATE = "cargo-ship_0", "escort-frigate_0"
SEA = (10, 44, 62)    # the sea on the display (Level 11)
SEA_DEEP = (2, 12, 24)


def sea_display(b, below=None):
    """The ocean on the display: the level's swell piece as a faint relief in teal over the dark base;
    with ``below`` (a y) the water under that line is darker and deeper (the cut-away of Level 11's scan)."""
    swell = np.array(Image.open(ROOT / "assets" / "backdrop" / "level-11" / "swell.png").convert("L")).astype(float) / 255
    tile = np.hstack([swell[:H, :], swell[:H, ::-1], swell[:H, :]])[:, :W]
    relief = (tile - tile.mean()) * 2.2
    b.base += relief[..., None] * np.array(SEA) * 1.1 + np.array(SEA) * 0.55
    if below is not None:
        k = np.clip((b.yy - below) / 50, 0, 1)[..., None]
        b.base = b.base * (1 - 0.55 * k) + np.array(SEA_DEEP) * 0.5 * k


def turned(img, degrees):
    """A sprite turned clockwise by ``degrees`` (a multiple of 90), its palette kept."""
    return img.rotate(-degrees, expand=True)


def convoy_route():
    """L11 p1: Okafor's map of the Atlantic from above: Convoy Atlas-Seven (the three cargo hulls and the
    frigate, production ships) heading east and then to open water, the seeded sea lanes with their
    Driftjellies, the reef line with its Reef Spitter rafts and Platform Tiamat on the route with the
    Kraken's gripping arms on it and an unknown large contact under it (the Kraken's hologram)."""
    b = Board()
    sea_display(b)
    b.glow(W + 40, 10, 260, (30, 40, 36), 0.5)
    b.title("NORTH ATLANTIC - CONVOY ATLAS-SEVEN - ARCTIC RELAY RUN")
    rng = np.random.default_rng(1101)
    tx, ty = 478, 84                                                  # Platform Tiamat's centre
    for x in (196, 252):                                              # the seeded lanes, two bands crossing the route
        for off in (-22, 22):
            pts = [(x + off, 46), (x + off + 10, 112), (x + off - 6, 216)]
            b.dashed(pts, VIOLET, 1, 5, 4)
    b.label(176, 22, "SEEDED SEA LANES", VIOLET)
    b.label(176, 32, "DRIFTING JELLIES", CYAN_DIM)
    jelly = scaled_sprite("driftjelly_0", 0.7)
    for k in range(11):
        lane = 196 if k % 2 else 252
        b.sprite(jelly, lane + rng.uniform(-14, 14), 54 + k * 15 + rng.uniform(-3, 3))
    chain = [(318, 58), (336, 88), (354, 124), (374, 160), (392, 188)]  # the reef line
    b.dashed(chain, VIOLET, 1, 3, 3)
    reef = [backdrop("level-11", f"reef-growth-{c}_0", 0.55) for c in "abc"]
    for k, (x, y) in enumerate(chain):
        b.sprite(reef[k % 3], x + (14 if k % 2 else -14), y)
    raft = scaled_sprite("reef-spitter-raft_0", 0.45)
    for x, y in ((344, 76), (372, 142), (402, 176)):
        b.sprite(raft, x, y)
    b.label(316, 206, "REEF LINE", VIOLET)
    b.label(316, 216, "GUN RAFTS", CYAN_DIM)
    route = [(150, 170), (230, 158), (310, 128), (384, 100), (412, 92)]
    b.dashed(route, AMBER, 2, 7, 4)
    b.dashed([(544, 80), (590, 70)], AMBER, 2, 7, 4)
    b.arrow(590, 70, 640, 50, AMBER, dashed=False)
    b.label(W - 10, 96, "OPEN WATER", AMBER, right=True)
    b.label(W - 10, 106, "ARCTIC RELAYS BEYOND", CYAN_DIM, right=True)
    b.sprite(holo(scaled_sprite("harbour-kraken-sub", 0.5), VIOLET), tx, ty + 76)   # under the platform
    b.sprite(backdrop("level-11", "platform-tiamat_0", 0.5, 64), tx, ty)
    grip = scaled_sprite("harbour-kraken-grip_0", 0.5)
    b.sprite(grip, tx, ty + 11)
    b.marker(tx - 78, ty - 20, "diamond", AMBER, 4)
    b.label(tx - 66, 22, "PLATFORM TIAMAT", AMBER)
    b.label(tx - 66, 32, "OVERRUN FUSION PLATFORM", CYAN_DIM)
    b.label(tx + 52, 166, "UNKNOWN LARGE", RED)
    b.label(tx + 52, 176, "CONTACT UNDER IT", RED)
    b.draw.line([(tx + 40, 160), (tx + 50, 168)], fill=RED + (230,))
    cargo = turned(scaled_sprite(CARGO, 0.6), 90)
    frigate = turned(scaled_sprite(FRIGATE, 0.6), 90)
    for x, y in ((112, 134), (140, 170), (112, 206)):
        b.sprite(cargo, x, y)
    b.sprite(frigate, 62, 170)
    b.dashed([(112, 134), (112, 206)], CYAN_DIM, 1, 2, 3)
    b.bracket(24, 118, 168, 226, CYAN)
    b.label(30, 80, "ATLAS-SEVEN", CYAN)
    b.label(30, 90, "3 CARGO + 1 FRIGATE", CYAN_DIM)
    b.label(30, 100, "REACTOR PARTS", GREEN)
    b.label(W - 10, 9, "STEAM THEM TO OPEN WATER", AMBER, right=True)
    b.sprite(asset("ship_2"), 232, 200)
    b.label(262, 196, "LANCER", CYAN)
    return b


def sub_scan():
    """L11 p2: Varga's cut-away at the waterline: a Driftjelly surfaced (the guns reach it) and submerged
    (they don't), the shots stopping at the waterline, a torpedo's run under the surface to a submerged
    jelly, a Reef Spitter raft (its gun above, its roots below) and the unknown large contact deep under it all."""
    b = Board()
    wl = 112                                                          # the waterline
    sea_display(b, wl)
    b.title("SENSOR SCAN - DRIFTJELLY / REEF SPITTER - WATERLINE")
    b.line([(0, wl), (W, wl)], CYAN, 1, 230)
    for x in range(6, W, 22):
        b.line([(x, wl + 3), (x + 9, wl + 3)], CYAN_DIM, 1, 140)
    b.label(W - 10, wl - 12, "SURFACE", CYAN, right=True)
    b.label(W - 10, wl + 8, "BELOW", CYAN_DIM, right=True)
    jx = 84                                                           # the jelly, surfaced and submerged
    b.sprite(asset("driftjelly_0", 2), jx, wl - 36)
    b.sprite(asset("driftjelly-sub_0", 2), jx, wl + 48)
    b.label(14, 26, "DRIFTJELLY", WHITE)
    b.label(14, 36, "SURFACED: ALL GUNS", GREEN)
    b.label(14, wl + 94, "SUBMERGED: GUNS", RED)
    b.label(14, wl + 104, "CAN'T REACH IT", RED)
    b.arrow(jx + 52, wl - 20, jx + 52, wl + 26, VIOLET, size=4)
    b.arrow(jx + 58, wl + 26, jx + 58, wl - 20, VIOLET, size=4)
    b.label(jx + 66, wl - 6, "SWAPS EVERY", VIOLET)
    b.label(jx + 66, wl + 4, "6-10 S", VIOLET)
    sx = 262                                                          # the guns stop at the waterline
    b.sprite(asset("ship_2"), sx, 58)
    for dx in (-10, 10):
        b.dashed([(sx + dx, 78), (sx + dx, wl - 4)], AMBER, 1, 4, 3)
        b.marker(sx + dx, wl - 2, "cross", AMBER, 2)
    b.label(sx + 36, 52, "GUNS STOP AT", AMBER)
    b.label(sx + 36, 62, "THE WATERLINE", AMBER)
    ty = wl + 56                                                      # the torpedo's run
    b.sprite(asset("driftjelly-sub_0", 2), 392, ty)
    b.sprite(asset("torpedo-pod-shot_8", 2), 252, ty)
    for k, x in enumerate((236, 224, 212, 202, 194)):
        b.sprite(asset(f"torpedo-pod-bubbles_{k % 6}", 1), x, ty + (k % 2) * 3 - 1)
    b.dashed([(272, ty), (356, ty)], GREEN, 2, 6, 4)
    b.arrow(340, ty, 356, ty, GREEN, size=5, dashed=False)
    b.label(sx - 30, wl + 22, "TORPEDO POD", GREEN)
    b.label(sx - 30, wl + 32, "RUNS UNDER", GREEN)
    b.label(sx - 30, wl + 42, "THE SURFACE", GREEN)
    b.label(sx - 30, wl + 88, "HITS WHAT DIVES", GREEN)
    rx = 520                                                          # the Reef Spitter raft
    b.sprite(holo(scaled_sprite("reef-spitter-raft-sub", 0.9), VIOLET), rx, wl + 38)
    b.sprite(asset("reef-spitter-raft_0"), rx, wl - 40)
    b.sprite(asset("reef-spitter_8"), rx, wl - 40)
    b.label(rx - 40, 26, "REEF SPITTER", WHITE)
    b.label(rx - 40, 36, "GUN RAFT: ALL GUNS", GREEN)
    b.label(rx - 38, wl + 86, "ROOTS IN THE REEF", VIOLET)
    b.sprite(holo(scaled_sprite("harbour-kraken-sub", 0.36), RED, 10), 634, wl + 80)
    b.label(W - 78, H - 26, "UNKNOWN, LARGE,", RED, right=True)
    b.label(W - 78, H - 16, "SUBMERGED", RED, right=True)
    b.label(W - 10, 9, "FIT TORPEDO PODS", AMBER, right=True)
    return b


STORM = (120, 140, 160)   # the storm's cloud on the display (Level 12)


def storm_sea(b):
    """Level 12's storm swell as a faint relief on the display."""
    swell = np.array(Image.open(ROOT / "assets" / "backdrop" / "level-12" / "swell.png").convert("L")).astype(float) / 255
    tile = np.hstack([swell[:H, :], swell[:H, ::-1], swell[:H, :]])[:, :W]
    b.base += (tile - tile.mean())[..., None] * np.array(SEA) * 1.6 + np.array(SEA) * 0.45


def storm_spiral(b, cx, cy, radius, eye, seed=1212):
    """A storm system from above on the display: cloud bands along spiral arms that wind in
    clockwise toward the eye (the tell: a northern storm turns the other way), denser toward the
    wall, the eye clear; a hologram's grey-cyan."""
    dx, dy = b.xx - cx, b.yy - cy
    r = np.hypot(dx, dy)
    th = np.arctan2(dy, dx)
    n = raster.fbm(W, H, 16, seed, octaves=4, period=False)
    arms = 0.5 + 0.5 * np.cos(3 * (th + 1.6 * np.log(np.maximum(r, 1) / radius)) + 3 * (n - 0.5))
    fall = np.clip(1 - r / radius, 0, 1)
    wall = np.exp(-((r - eye * 1.5) / (eye * 0.9)) ** 2)
    cloud = np.clip((arms * 1.0 + n * 0.45) * fall ** 0.5 + wall * 0.8 - 0.5, 0, 1) * (r > eye)
    cloud *= np.clip((r - eye) / 6, 0, 1)
    b.base += (cloud ** 1.2)[..., None] * np.array(STORM) * 1.1
    return cloud


def storm_front():
    """L12 p1: Okafor's map: the storm system from above off the Azores, its cloud bands winding
    into the eye, moving east onto the convoy lanes; Vrell contacts inside it; Lancer and Rook's
    route in through the wall, across the eye and out (the breakout)."""
    b = Board()
    storm_sea(b)
    cx, cy, radius, eye = 392, 128, 150, 26
    storm_spiral(b, cx, cy, radius, eye)
    b.glow(cx, cy, eye * 0.9, (70, 90, 110), 0.5)                  # the calm in the eye
    b.title("NORTH ATLANTIC - OFF THE AZORES - STORM FRONT")
    for x, y, r in ((70, 132, 3), (88, 140, 2), (104, 128, 2), (128, 152, 3), (150, 166, 2)):   # the Azores
        b.draw.ellipse([x - r, y - r, x + r, y + r], outline=CYAN + (230,))
    b.label(58, 172, "AZORES", CYAN_DIM)
    for y in (66, 206):                                            # the convoy lanes, west to east
        b.dashed([(20, y), (W - 20, y + 6)], GREEN, 1, 8, 6)
    b.label(W - 10, 54, "CONVOY LANES", GREEN, right=True)
    b.arrow(cx + radius - 12, cy + 30, cx + radius + 40, cy + 44, CYAN, size=5)
    b.label(cx + radius - 34, cy + 56, "MOVING EAST", CYAN)
    b.label(cx + radius - 34, cy + 66, "FORMED IN 6 H", CYAN_DIM)
    b.ring(cx, cy, eye, colour=CYAN_DIM, alpha=160)
    b.label(cx - 10, cy - 4, "EYE", WHITE)
    for a in (40, 160, 280):                                       # the sense of rotation
        a0 = np.radians(a)
        rr = radius * 0.72
        b.arc(cx, cy, rr, rr, a, a + 28, CYAN, 200)
        tip = (cx + rr * np.cos(a0 + np.radians(30)), cy + rr * np.sin(a0 + np.radians(30)))
        tang = np.array([-np.sin(a0 + np.radians(30)), np.cos(a0 + np.radians(30))])
        n_ = np.array([-tang[1], tang[0]])
        tip = np.array(tip)
        b.draw.polygon([tuple(tip + tang * 6), tuple(tip - tang * 2 + n_ * 4), tuple(tip - tang * 2 - n_ * 4)],
                       fill=CYAN + (230,))
    rng = np.random.default_rng(1213)
    for _ in range(9):                                             # flyers in the cloud
        a, rr = rng.uniform(0, 2 * np.pi), rng.uniform(eye * 1.8, radius * 0.8)
        b.marker(cx + np.cos(a) * rr, cy + np.sin(a) * rr * 0.9, "chevron", RED, 3)
    b.label(cx - 150, 26, "VRELL FLYERS IN COVER", RED)
    route = [(196, 200), (268, 176), (cx - 50, cy + 24), (cx, cy), (cx + 60, cy - 30), (cx + 120, cy - 70)]
    b.dashed(route[:-1], AMBER, 2, 7, 4)
    b.arrow(*route[-2], *route[-1], AMBER, dashed=False)
    b.label(cx + 60, 26, "BREAKOUT", AMBER)
    b.label(214, 120, "STORM WALL", WHITE)
    b.line([(262, 126), (cx - eye * 2.2, cy + 8)], WHITE, 1, 140)
    b.sprite(asset("ship_2"), 186, 204)
    b.sprite(rook_craft(), 160, 216)
    b.label(206, 214, "LANCER, ROOK", CYAN)
    b.label(W - 10, 9, "CLEAR THE FRONT", AMBER, right=True)
    return b


def pressure_plot(b, x0, y0, w, h):
    """Varga's plot: pressure against distance to the eye, the curve a storm makes (grey, dashed)
    and what the sondes send: a regular saw that repeats like a signal (amber)."""
    b.line([(x0, y0), (x0, y0 + h), (x0 + w, y0 + h)], CYAN_DIM, 1, 220)
    for k in range(1, 5):
        b.line([(x0 - 3, y0 + h * k / 5), (x0, y0 + h * k / 5)], CYAN_DIM, 1, 200)
        b.dashed([(x0 + 2, y0 + h * k / 5), (x0 + w, y0 + h * k / 5)], CYAN_DIM, 1, 1, 5)
    u = np.linspace(0, 1, 160)
    expected = y0 + h * (0.15 + 0.7 * (1 - np.exp(-((u - 1.0) / 0.35) ** 2)))
    b.dashed([(x0 + w * a, e) for a, e in zip(u[::8], expected[::8])], GREY, 1, 4, 3)
    saw = (u * 7) % 1.0
    measured = y0 + h * (0.2 + 0.55 * saw + 0.04 * np.sin(u * 60)) + (expected - (y0 + h * 0.5)) * 0.25
    b.line([(x0 + w * a, m) for a, m in zip(u, measured)], AMBER, 2, 240)
    b.label(x0, y0 - 14, "PRESSURE, HPA", CYAN_DIM)
    b.label(x0 + w, y0 + h + 6, "TO THE EYE", CYAN_DIM, right=True)
    b.label(x0 + w - 90, y0 - 14, "EXPECTED", GREY, right=True)
    b.label(x0 + w, y0 - 14, "MEASURED", AMBER, right=True)


def sonde_scan():
    """L12 p2: Varga's scan: a CDF weather sonde blinking in the rain (the production sprite, its
    strobe), fly-through ring and the SONDES tracker; beside it the pressure plot that makes no sense:
    a regular saw where a storm's curve should be."""
    b = Board()
    storm_sea(b)
    b.base *= 0.7
    b.title("SENSOR SCAN - CDF WEATHER SONDE - PRESSURE")
    sx, sy = 112, 124
    rng = np.random.default_rng(1220)
    rain = [asset(f"weather-rain_{k}") for k in range(4)]
    for _ in range(70):                                            # the rain round it
        x, y = rng.uniform(10, 236), rng.uniform(24, 228)
        light_at(b, additive(f"weather-rain_{int(rng.integers(0, 4))}", 1.0, 0.8), x, y, "under")
    del rain
    b.sprite(asset("weather-sonde_0", 3), sx, sy)
    light_at(b, additive("weather-sonde-collect_1", 2.0, 0.5), sx, sy, "under")
    b.ring(sx, sy, 56, colour=GREEN, alpha=150)
    b.label(sx + 40, sy + 54, "FLY THROUGH", GREEN)
    b.label(14, 26, "CDF WEATHER SONDE", WHITE)
    b.label(14, 36, "3 STILL TRANSMITTING", CYAN)
    b.label(14, 214, "SONDES 0 / 3", GREEN)
    b.label(14, 202, "CAN'T BE SHOT", CYAN_DIM)
    pressure_plot(b, 300, 52, 330, 140)
    b.label(300, 214, "A STORM DOESN'T REPEAT ITSELF", RED)
    b.label(W - 10, 9, "COLLECT ALL THREE", AMBER, right=True)
    return b


ICE_HOLO = [(0.0, (2, 8, 22)), (0.3, (6, 26, 48)), (0.65, (24, 70, 100)), (1.0, (96, 160, 186))]   # Level 13's ice


def halt_frame():
    """Level 13's halted frame from its final backdrop (assets/backdrop/level-13): the top 540 rows of
    pack-ice-4 (the halt's screen) over the arctic water, as RGB floats 540 x 480."""
    folder = ROOT / "assets" / "backdrop" / "level-13"
    water = np.array(Image.open(folder / "arctic-water.png").convert("RGB")).astype(np.float64)[:540]
    ice = np.array(Image.open(folder / "pack-ice-4.png").convert("RGBA")).astype(np.float64)[:540]
    k = ice[..., 3:4] / 255
    return water * (1 - k) + ice[..., :3] * k


def ice_display(b, x0, y0, scale, gain=1.0):
    """The halted frame on the display: its brightness through the ice hologram's ramp, ``scale`` x,
    with its top left at (x0, y0); returns the box (x0, y0, x1, y1)."""
    rgb = halt_frame()
    img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB")
    img = img.resize((round(480 * scale), round(540 * scale)), Image.BOX)
    lum = np.array(img.convert("L")).astype(np.float64) / 255
    body = raster.ramp(ICE_HOLO, np.clip(lum * 1.15 * gain, 0, 1))
    h_, w_ = body.shape[:2]
    ys, xs = slice(max(0, y0), min(H, y0 + h_)), slice(max(0, x0), min(W, x0 + w_))
    b.base[ys, xs] = body[ys.start - y0:ys.stop - y0, xs.start - x0:xs.stop - x0]
    return x0, y0, x0 + w_, y0 + h_


def polar(cx, cy, r, lon, lat, turn=-100):
    """The north polar view: latitude 90 at the centre, 20 at the rim, longitude ``turn`` down."""
    rho = (90 - lat) / 70 * r
    a = np.radians(lon - turn + 90)
    return cx + rho * np.cos(a), cy + rho * np.sin(a)


def relay_grid():
    """L13 p1: Okafor's map: the Arctic from above the pole, Nansen Relay on the pack ice north of
    Svalbard and the defence grid it ties together (the Arctic relays, guns, radar, satellites on their
    orbit); the inset: the relay on its floe, the landing force's approach over the pack ice from every
    edge, Lancer and Rook holding over it; three minutes to synchronise."""
    b = Board()
    cx, cy, r = 150, 132, 96
    mask = Image.new("L", (W, H), 0)
    md = ImageDraw.Draw(mask)
    for poly in CONTINENTS:
        pts = [polar(cx, cy, r, lon, max(lat, 14)) for lon, lat in poly]
        md.polygon(pts, fill=255)
    disc = np.hypot(b.xx - cx, b.yy - cy) < r
    land = (np.array(mask) > 0) & disc
    tex = 0.55 + 0.5 * raster.fbm(W, H, 16, 1301, octaves=4, period=False)
    b.base = np.where(disc[..., None], b.base * 0.6 + np.array((4, 16, 34)), b.base)
    b.base = np.where(land[..., None], raster.ramp(ui_scenes.HOLO, np.clip(tex, 0, 1)), b.base)
    pack = disc & ~land & (np.hypot(b.xx - cx, b.yy - cy) < r * 0.33)
    b.base = np.where(pack[..., None], b.base + (raster.fbm(W, H, 6, 1302, octaves=3, period=False)[..., None] > 0.45)
                      * np.array((26, 54, 70)), b.base)                      # the pack ice round the pole
    b.title("HIGH ARCTIC - NANSEN RELAY - THE DEFENCE GRID")
    for lat in (80, 60, 40):
        rr = (90 - lat) / 70 * r
        b.ring(cx, cy, rr, colour=CYAN_DIM, alpha=70)
    b.ring(cx, cy, r, colour=CYAN_DIM, alpha=160)
    for lon in range(0, 360, 30):
        b.line([polar(cx, cy, r, lon, 88), polar(cx, cy, r, lon, 20)], CYAN_DIM, 1, 45)
    relay = polar(cx, cy, r, 18, 83)
    relays = [(-70, 74), (-40, 76), (-10, 75), (20, 76), (50, 75), (80, 76), (110, 75), (150, 72)]
    for lon, lat in relays:
        p = polar(cx, cy, r, lon, lat)
        b.line([relay, p], GREEN, 1, 150)
        b.marker(*p, "square", GREEN, 2)
    for lon, lat in ((-77, 39), (2, 49), (37, 56), (116, 40), (139, 36), (-122, 38), (6, 46)):   # the guns and radars
        p = polar(cx, cy, r, lon, lat)
        b.dashed([relay, p], GREEN, 1, 3, 3)
        b.marker(*p, "dot", GREEN, 1)
    for k in range(7):                                                          # the satellites on their orbit
        a = np.radians(k * 360 / 7 + 12)
        p = (cx + (r + 14) * np.cos(a), cy + (r + 14) * np.sin(a) * 0.96)
        b.dashed([relay, p], CYAN_DIM, 1, 2, 4)
        b.marker(*p, "square", CYAN, 2)
    b.arc(cx, cy, r + 14, r + 13, 0, 360, CYAN_DIM, 110)
    b.marker(*relay, "diamond", AMBER, 4)
    b.ring(*relay, 9, colour=AMBER, alpha=200)
    b.label(14, 26, "EVERY GUN, RADAR", GREEN)
    b.label(14, 36, "AND SATELLITE: ONE GRID", GREEN)
    b.label(14, H - 16, "NORTH POLE VIEW", CYAN_DIM)
    x0, y0, x1, y1 = ice_display(b, 418, 20, 0.40)                              # the inset: the halt's frame
    b.bracket(x0 - 4, y0 - 4, x1 + 4, y1 + 4, CYAN)
    b.line([(relay[0] + 10, relay[1] - 4), (x0 - 6, y0 + 2)], AMBER, 1, 160)
    rx, ry = x0 + 240 * 0.40, y0 + 250 * 0.40
    b.sprite(scaled_sprite("nansen-relay_0", 0.42), rx, ry)
    for (ax, ay), (bx, by) in (((x0 - 2, y0 + 30), (x0 + 30, y0 + 50)), ((x1 + 2, y0 + 120), (x1 - 30, y0 + 116)),
                               ((rx - 20, y0 - 2), (rx - 12, y0 + 26)), ((rx + 30, y1 + 2), (rx + 20, y1 - 26)),
                               ((x0 - 2, y1 - 30), (x0 + 30, y1 - 50))):
        b.arrow(ax, ay, bx, by, RED, size=4)
    for x, y in ((x0 + 22, y0 + 40), (x1 - 22, y0 + 104), (rx + 16, y1 - 18), (x0 + 24, y1 - 40), (rx - 16, y0 + 18)):
        b.marker(x, y, "chevron", RED, 3)
    b.label(262, 100, "NANSEN RELAY", AMBER)
    b.label(262, 110, "BACK AT FULL POWER", CYAN_DIM)
    b.label(260, 160, "LANDING FORCE", RED)
    b.label(260, 170, "OVER THE PACK ICE,", RED)
    b.label(260, 180, "FROM EVERY SIDE", RED)
    b.sprite(asset("ship_2"), rx + 6, ry + 52)
    b.sprite(rook_craft(), rx - 22, ry + 58)
    b.label(260, 204, "LANCER, ROOK: HOLD", CYAN)
    b.label(260, 214, "OVER THE RELAY", CYAN)
    b.label(W - 10, 9, "HOLD 3 MINUTES", AMBER, right=True)
    return b


def ice_field():
    """L13 p2: Varga's scan of the halt from above: the relay's floe and the leads round it (the final
    backdrop's frame), a Skimmer weaving along its lead (the lane, the weave either side: lead your shots
    or spray), a Scuttler walking the floe with its claws' armoured arc to the front and the side shots
    that get in; shots aimed at the relay that the ship can stand in."""
    b = Board()
    x0, y0, x1, y1 = ice_display(b, 224, 0, 0.444, 0.9)
    b.title("SENSOR SCAN - PACK ICE - SKIMMER / SCUTTLER")
    sc = 0.444
    rx, ry = x0 + 240 * sc, y0 + 250 * sc
    b.sprite(scaled_sprite("nansen-relay_0", 0.46), rx, ry)
    b.marker(rx, ry - 30, "diamond", AMBER, 3)
    # the Skimmer's lane along the south lead (screen y 462 in the halt) and its weave
    ly = y0 + 462 * sc
    b.dashed([(x0, ly), (x1, ly)], VIOLET, 1, 6, 4)
    u = np.linspace(0, 1, 120)
    weave = [(x0 + (x1 - x0) * a, ly + 40 * sc * np.sin(a * 2 * np.pi * 3.2)) for a in u]
    b.line(weave, RED, 1, 220)
    sk = asset("skimmer_24", 1)                                                  # heading 12 (moving right), tail frame 0
    b.sprite(sk, x0 + (x1 - x0) * 0.62, ly + 40 * sc * np.sin(0.62 * 2 * np.pi * 3.2))
    b.label(14, 26, "SKIMMER", WHITE)
    b.label(14, 36, "WEAVES ALONG THE LEADS", RED)
    b.sprite(asset("skimmer_24", 2), 62, 92)
    b.ring(62, 92, 30, colour=RED, alpha=140)
    b.label(14, 132, "180 PX/S, 40 EITHER SIDE", CYAN_DIM)
    b.label(14, 146, "LEAD YOUR SHOTS", GREEN)
    b.label(14, 156, "OR SPRAY", GREEN)
    b.line([(98, 100), (x0 + 120, ly - 6)], RED, 1, 120)
    # the Scuttler on the floe: its claws' armoured front, the side shots that get in
    wx, wy = W - 90, 112
    b.sprite(asset("scuttler_72", 2), wx, wy)                                   # heading 12 (walking right)
    b.arc(wx, wy, 56, 56, -45, 45, RED, 230)
    b.arc(wx, wy, 60, 60, -45, 45, RED, 160)
    b.label(wx + 4, 40, "ARMOURED FRONT", RED)
    for ax, ay, bx_, by in ((wx - 6, 22, wx - 6, wy - 46), (wx - 6, H - 20, wx - 6, wy + 46)):
        b.arrow(ax, ay, bx_, by, GREEN, size=4)
    b.label(W - 10, H - 26, "HIT IT FROM THE SIDE", GREEN, right=True)
    b.label(W - 10, 26, "SCUTTLER", WHITE, right=True)
    sx, sy = x0 + 82 * sc, y0 + 250 * sc
    b.sprite(asset("scuttler_72"), sx, sy)
    # stand in the way: a shot aimed at the relay, the ship across its line
    b.dashed([(x1 - 6, y0 + 22), (rx + 20, ry - 16)], RED, 1, 4, 3)
    b.marker(x1 - 6, y0 + 22, "chevron", RED, 3)
    b.sprite(asset("ship_2"), x1 - 46, y0 + 50)
    b.label(x1 + 6, 36, "AIMED AT THE RELAY:", AMBER)
    b.label(x1 + 6, 46, "STAND IN THE WAY", AMBER)
    b.label(W - 10, 9, "SIDE AND REAR GUNS PAY", AMBER, right=True)
    return b


GENEVA_HOLO = [(0.0, (3, 8, 22)), (0.35, (10, 36, 60)), (0.7, (34, 96, 130)), (1.0, (120, 196, 220))]   # Level 14


def spire_over_geneva():
    """L14 p1: Okafor's picture: Geneva Concord seen from the lake: the water in front with the Jet d'Eau,
    the old town's roofs and the cathedral's towers, the Siege Spire's trunk rising out of the Concord
    rotunda three kilometres into the canopy, which spreads over the whole old town; the route in along
    the lakefront and through the old town to the rotunda, Lancer and Rook going in first."""
    b = Board()
    b.title("GENEVA CONCORD - THE SIEGE SPIRE")
    rng = np.random.default_rng(1401)
    ss = 4
    shape = Image.new("L", (W * ss, H * ss), 0)            # the lit structures, as brightness
    d = ImageDraw.Draw(shape)
    horizon = 188
    # the old town's skyline: row houses with ridged roofs, the cathedral's two towers and spire
    x = 0
    while x < W:
        w = int(rng.integers(10, 26))
        h = int(rng.integers(8, 20)) + (6 if 330 < x < 600 else 0)
        d.rectangle([x * ss, (horizon - h) * ss, (x + w - 1) * ss, horizon * ss], fill=int(rng.integers(70, 110)))
        d.polygon([(x * ss, (horizon - h) * ss), ((x + w / 2) * ss, (horizon - h - 5) * ss),
                   ((x + w - 1) * ss, (horizon - h) * ss)], fill=int(rng.integers(110, 150)))
        x += w + int(rng.integers(0, 3))
    for tx in (258, 274):                                  # Saint-Pierre's towers
        d.rectangle([tx * ss, (horizon - 44) * ss, (tx + 9) * ss, horizon * ss], fill=125)
        d.polygon([(tx * ss, (horizon - 44) * ss), ((tx + 4.5) * ss, (horizon - 54) * ss),
                   ((tx + 9) * ss, (horizon - 44) * ss)], fill=160)
    d.polygon([(268 * ss, (horizon - 38) * ss), (270 * ss, (horizon - 64) * ss), (272 * ss, (horizon - 38) * ss)], fill=150)
    # the trunk out of the rotunda, its ridges, the crown's claws in the canopy
    tx0, tw0, tw1, top = 470, 46, 14, 34
    d.polygon([((tx0 - tw0) * ss, horizon * ss), ((tx0 - tw1) * ss, top * ss), ((tx0 + tw1) * ss, top * ss),
               ((tx0 + tw0) * ss, horizon * ss)], fill=150)
    d.polygon([((tx0 + 4) * ss, horizon * ss), ((tx0 + 2) * ss, top * ss), ((tx0 + tw1) * ss, top * ss),
               ((tx0 + tw0) * ss, horizon * ss)], fill=105)             # its shaded side (the light from the left)
    for k in range(-2, 3):
        d.line([((tx0 + k * tw0 / 3) * ss, horizon * ss), ((tx0 + k * tw1 / 3) * ss, top * ss)], fill=190, width=ss)
    for a in (-1.0, -0.55, 0.55, 1.0):                     # the claw petals
        d.polygon([((tx0 + a * 10) * ss, (top + 4) * ss), ((tx0 + a * 40) * ss, (top - 18) * ss),
                   ((tx0 + a * 18) * ss, (top + 2) * ss)], fill=140)
    for a in (-1, 1):                                      # roots arching into the city
        for k, (ln, hh) in enumerate(((90, 26), (150, 16), (60, 34))):
            pts = [((tx0 + a * (tw0 - 4 + ln * u)) * ss, (horizon - hh * math_sin(u * 3.14159)) * ss) for u in np.linspace(0, 1, 16)]
            d.line(pts, fill=105, width=5 * ss // 2)
    lum = np.array(shape.resize((W, H), Image.BOX)).astype(np.float64) / 255
    # the canopy: a veined lens over the old town, the trunk under its middle
    cx, cy, rx, ry = tx0, 30, 330, 34
    nx, ny = (b.xx - cx) / rx, (b.yy - cy) / ry
    lens = (nx ** 2 + ny ** 2 < 1) & (b.yy < cy + ry * 0.55)
    cells = raster.fbm(W, H, 18, 1402, octaves=2, period=False)
    vein = np.abs(cells - 0.5) < 0.035
    lum = np.where(lens, np.maximum(lum, 0.32 + 0.25 * (1 - np.abs(ny))), lum)
    lum = np.where(lens & vein, 0.85, lum)
    shade = (b.xx > cx - rx) & (b.xx < cx + rx) & (b.yy > cy + ry * 0.5) & (b.yy < horizon)
    b.base = np.where(shade[..., None], b.base * 0.65, b.base)          # under the canopy: dusk
    # the lake in front, its ripples, the Jet d'Eau
    lake = b.yy > horizon
    ripple = (np.sin(b.xx / 9 + b.yy * 1.7) > 0.93) & lake
    body = raster.ramp(GENEVA_HOLO, np.clip(lum, 0, 1))
    b.base = np.where((lum > 0.02)[..., None], body, b.base)
    b.base = np.where(lake[..., None], b.base * 0.6 + np.array((4, 18, 40)), b.base)
    b.base = np.where(ripple[..., None], b.base + np.array((20, 60, 80)), b.base)
    b.line([(96, horizon + 30), (92, horizon - 60)], WHITE, 1, 140)
    b.line([(94, horizon + 30), (90, horizon - 52)], CYAN, 1, 110)
    b.label(102, horizon - 58, "JET D'EAU", CYAN_DIM)
    # labels: the height, the canopy, the rotunda
    b.line([(tx0 + 58, top), (tx0 + 58, horizon)], AMBER, 1, 200)
    for yy_ in (top, horizon):
        b.line([(tx0 + 54, yy_), (tx0 + 62, yy_)], AMBER, 1, 220)
    b.label(tx0 + 66, 100, "3 KM", AMBER)
    b.label(tx0 + 66, 110, "STILL GROWING", AMBER)
    b.label(250, 70, "THE CANOPY", VIOLET)
    b.label(250, 80, "OVER THE OLD TOWN", VIOLET)
    b.marker(tx0, horizon + 4, "diamond", AMBER, 4)
    b.label(tx0 - 30, horizon + 14, "CONCORD ROTUNDA", AMBER)
    b.label(244, horizon + 14, "OLD TOWN", CYAN)
    # the route: along the lakefront, through the old town, to the rotunda
    b.dashed([(20, H - 14), (150, H - 14), (300, horizon + 26), (tx0 - 12, horizon + 8)], GREEN, 2)
    b.arrow(tx0 - 60, horizon + 14, tx0 - 10, horizon + 7, GREEN, size=4, dashed=False)
    b.label(14, H - 30, "LAKEFRONT", GREEN)
    b.sprite(scaled_sprite("ship_2", 0.6), 40, H - 46)
    b.sprite(scaled_sprite("rook_2", 0.6), 22, H - 40)
    b.label(14, 26, "TWO NIGHTS:", WHITE)
    b.label(14, 36, "IT GREW OUT OF THE CITY", WHITE)
    b.label(W - 10, 9, "EVERYTHING WE HAVE LEFT", AMBER, right=True)
    return b


def root_scan():
    """L14 p2: Varga's scan of the root grid under the old town: the Root Field from the final backdrop
    (assets/backdrop/level-14/root-field) through the hologram's ramp, its roots traced (the backdrop's
    own courses), the burst points marked; one burst in detail: the 1 s of cracking paving and dust, then
    the root and its two growths, a Spine Turret and a Polyp Mortar; anti-ground for the roots. And when
    the crown opens, the Wraiths it launches come from behind."""
    import backdrop_l14 as l14
    b = Board()
    b.title("SENSOR SCAN - ROOT GRID UNDER THE OLD TOWN")
    tile = np.array(Image.open(ROOT / "assets" / "backdrop" / "level-14" / "root-field.png").convert("RGB"))[:540]
    sc = 0.444
    img = Image.fromarray(tile).resize((round(480 * sc), round(540 * sc)), Image.BOX)
    lumi = np.array(img.convert("L")).astype(np.float64) / 255
    body = raster.ramp(GENEVA_HOLO, np.clip(lumi * 1.25, 0, 1))
    x0, y0 = 214, 0
    h_, w_ = body.shape[:2]
    b.base[y0:y0 + h_, x0:x0 + w_] = body[:H - y0]
    # the roots' courses (the backdrop plan's), traced violet
    for pts, width in l14.plan_root_field()["roots"]:
        line = [(x0 + x * sc, y0 + y * sc) for x, y in pts if 0 <= y <= 540]
        b.line(line, VIOLET, 2, 200)
    bursts = [(110, 120), (330, 230), (150, 330), (300, 440)]          # the scan's burst points (screen px)
    for i, (bx, by) in enumerate(bursts):
        px_, py_ = x0 + bx * sc, y0 + by * sc
        b.marker(px_, py_, "diamond", AMBER, 3)
        b.ring(px_, py_, 9, colour=AMBER, alpha=160)
    b.label(x0 + w_ + 8, 26, "BURST POINTS", AMBER)
    b.line([(x0 + 330 * sc + 10, y0 + 230 * sc), (x0 + w_ + 6, 30)], AMBER, 1, 140)
    # one burst in detail: dust, then the root and its growths
    dx = 560
    b.sprite(asset("root-burst-decal_5"), dx - 70, 100)
    b.label(dx - 112, 150, "1 S: PAVING CRACKS,", CYAN)
    b.label(dx - 112, 160, "DUST RISES", CYAN)
    b.arrow(dx - 28, 100, dx - 4, 100, CYAN, size=4, dashed=False)
    b.sprite(asset("root-burst-root_5"), dx + 50, 96)
    b.sprite(asset("spine-turret_0"), dx + 24, 100)
    b.sprite(asset("polyp-mortar_0"), dx + 76, 95)
    b.label(dx - 112, 180, "THEN THE ROOT GROWS", RED)
    b.label(dx - 112, 190, "A TURRET AND A MORTAR", RED)
    b.label(W - 10, 9, "ANTI-GROUND X2 ON ROOTS", GREEN, right=True)
    # left: what to bring, and behind you
    b.label(14, 26, "KILL THE ROOTS", WHITE)
    b.label(14, 36, "AND THE TRUNK STARVES", WHITE)
    b.label(14, 56, "BRING ANTI-GROUND", GREEN)
    b.label(14, 66, "AND SOMETHING THAT PIERCES", GREEN)
    b.sprite(scaled_sprite("wraith_0", 0.75), 60, 150)
    b.sprite(scaled_sprite("ship_2", 0.7), 150, 128)
    b.arrow(60, 190, 140, 146, RED, size=4)
    b.label(14, 202, "WHEN THE CROWN OPENS,", RED)
    b.label(14, 212, "LOOK BEHIND YOU", RED)
    return b


# --------------------------------------------------------------------------- Act 3 (M7 part B)

MARS_HOLO = [(0.0, (8, 4, 8)), (0.35, (40, 18, 16)), (0.7, (96, 46, 30)), (1.0, (180, 108, 68))]   # Act 3's display ramp
MARS_TRUE = [(0.0, (12, 5, 6)), (0.22, (52, 20, 13)), (0.5, (118, 50, 26)), (0.78, (186, 100, 56)),
             (1.0, (230, 164, 108))]                   # Mars through the transport's glass
BUTTERSCOTCH = (214, 150, 92)                          # the sky (design/world/mars, Palette)
MW, MH = 720, 360                                      # the Mars map: equirectangular, half a degree a pixel
# Act 3's places (east longitude, north latitude; design/world/mars, Geography)
OLYMPUS = (-134.0, 18.0)
THARSIS_FOUNDRY = (-106.0, 6.0)
ARES_LANDING = (-31.0, 16.0)
HELLAS = (70.0, -42.0)
VALLES = [(-101, -6), (-92, -8), (-80, -9), (-68, -11), (-56, -12), (-47, -11), (-40, -7), (-36, 0), (-33, 8)]
_MARS_MAP = []


def gc(lon, lat, lon0, lat0):
    """The great-circle angle in degrees from (lon0, lat0)."""
    p, p0 = np.radians(lat), np.radians(lat0)
    c = np.sin(p) * np.sin(p0) + np.cos(p) * np.cos(p0) * np.cos(np.radians(lon - lon0))
    return np.degrees(np.arccos(np.clip(c, -1, 1)))


def polyline_distance(lon, lat, points):
    """The distance in degrees (east-west shortened by the latitude) to a polyline of (lon, lat)."""
    best = np.full(np.shape(lon), 1e9)
    k = np.cos(np.radians(lat))
    for (x0, y0), (x1, y1) in zip(points[:-1], points[1:]):
        ax, ay = (lon - x0) * k, lat - y0
        bx, by = (x1 - x0) * k, y1 - y0
        t = np.clip((ax * bx + ay * by) / (bx * bx + by * by), 0, 1)
        best = np.minimum(best, np.hypot(ax - t * bx, ay - t * by))
    return best


def mars_map():
    """Mars as an equirectangular map (``h`` height about -1..1.5, ``a`` albedo 0..1, ``cap`` the polar
    ice), its real features simplified: the northern lowlands, the Tharsis bulge and its volcanoes
    (Olympus Mons on its escarpment, its caldera), the Valles Marineris into the outflow channels,
    the Hellas and Argyre basins, craters on the southern highlands, the dark albedo regions."""
    if _MARS_MAP:
        return _MARS_MAP[0]
    lon = (np.arange(MW) + 0.5) * 360 / MW - 180
    lat = 90 - (np.arange(MH) + 0.5) * 180 / MH
    LON, LAT = np.meshgrid(lon, lat)
    n = raster.fbm(MW, MH, 40, 3801, octaves=6)
    n2 = raster.fbm(MW, MH, 90, 3802, octaves=3)
    rl = np.radians(LON)
    edge = 20 + 9 * np.sin(rl * 2 + 0.6) + 5 * np.sin(rl * 5 + 1.3)    # the dichotomy: lowlands north
    h = 0.32 * np.tanh((edge - LAT) / 9) + 0.2 * (n - 0.5)
    h += 0.6 * np.exp(-(gc(LON, LAT, -108, 2) / 26) ** 2)
    for lo, la, r, amp in ((-121, -9, 3.2, 0.55), (-113, 1, 3.2, 0.5), (-104, 11, 3.2, 0.55),
                           (147, 25, 4.5, 0.4), (-110, 41, 9, 0.18)):
        d = gc(LON, LAT, lo, la)
        h += amp * np.exp(-(d / r) ** 2) - 0.3 * amp * np.exp(-(d / (0.22 * r)) ** 2)
    d = gc(LON, LAT, *OLYMPUS)
    h += np.where(d < 9.5, 0.3 + 1.0 * np.clip(1 - d / 9.5, 0, 1) ** 1.3, 0) - 0.35 * np.exp(-(d / 0.9) ** 2)
    d = gc(LON, LAT, *HELLAS)
    h += -0.95 * np.exp(-(d / 16) ** 2) + 0.2 * np.exp(-((d - 21) / 4) ** 2)
    d = gc(LON, LAT, -43, -50)
    h += -0.45 * np.exp(-(d / 8) ** 2) + 0.12 * np.exp(-((d - 10) / 2.5) ** 2)
    dv = polyline_distance(LON, LAT, VALLES)
    h -= 0.5 * np.exp(-(dv / 1.1) ** 2)
    rng = np.random.default_rng(3803)
    for _ in range(240):                                 # craters on the southern highlands
        lo, la, r = rng.uniform(-180, 180), rng.uniform(-76, 34), rng.uniform(0.6, 3.6)
        if la > 20 + 9 * np.sin(np.radians(lo) * 2 + 0.6) - 8:
            continue
        rows = slice(max(0, int((90 - la - 4 * r) * 2)), min(MH, int((90 - la + 4 * r) * 2) + 1))
        dc = gc(LON[rows], LAT[rows], lo, la)
        h[rows] += -0.16 * np.exp(-(dc / r) ** 2) + 0.06 * np.exp(-((dc - r) / (0.3 * r)) ** 2)
    a = 0.62 + 0.3 * (n2 - 0.5) + 0.16 * (n - 0.5)
    for lo, la, r, s in ((70, 8, 12, 0.35), (-30, 45, 14, 0.25), (-25, -22, 16, 0.3), (0, -6, 9, 0.3),
                         (100, -18, 14, 0.25), (145, -24, 15, 0.25), (-150, -30, 14, 0.2), (-70, -28, 10, 0.15),
                         (40, -10, 10, 0.2)):
        a -= s * np.exp(-(gc(LON, LAT, lo, la) / r) ** 2) * (0.6 + 0.8 * n2)
    a += 0.18 * np.exp(-(gc(LON, LAT, *HELLAS) / 13) ** 2) + 0.1 * np.exp(-(gc(LON, LAT, -110, 5) / 25) ** 2)
    a -= 0.4 * np.exp(-(dv / 1.0) ** 2)
    cap = ((LAT < -82 + 5 * n) | (LAT > 79 + 5 * n)).astype(np.float64)
    _MARS_MAP.append({"h": h, "a": np.clip(a, 0, 1), "cap": cap})
    return _MARS_MAP[0]


def mars_sample(field, lon, lat):
    """``field`` of mars_map() at (lon, lat), bilinear, the longitude wrapping."""
    x = ((np.asarray(lon, float) + 180) % 360) / 360 * MW - 0.5
    y = np.clip((90 - np.asarray(lat, float)) / 180 * MH - 0.5, 0, MH - 1.001)
    x0, y0 = np.floor(x).astype(int), np.floor(y).astype(int)
    fx, fy = x - x0, y - y0
    x1, x0 = (x0 + 1) % MW, x0 % MW
    y1 = np.minimum(y0 + 1, MH - 1)
    top = field[y0, x0] * (1 - fx) + field[y0, x1] * fx
    bot = field[y1, x0] * (1 - fx) + field[y1, x1] * fx
    return top * (1 - fy) + bot * fy


class Globe:
    """An orthographic view of Mars: centre (cx, cy), radius r px, looking at (lon0, lat0)."""

    def __init__(self, cx, cy, r, lon0, lat0):
        self.cx, self.cy, self.r = cx, cy, r
        self.lon0, self.p0 = lon0, np.radians(lat0)

    def inverse(self, xx, yy):
        """Per pixel: inside the disc, lon, lat and the normal (nx right, ny up, nz to the viewer)."""
        x, y = (xx - self.cx) / self.r, -(yy - self.cy) / self.r
        rho = np.hypot(x, y)
        inside = rho < 1
        c = np.arcsin(np.clip(rho, 0, 1))
        rr = np.maximum(rho, 1e-9)
        lat = np.degrees(np.arcsin(np.clip(np.cos(c) * np.sin(self.p0) + y * np.sin(c) * np.cos(self.p0) / rr, -1, 1)))
        lon = self.lon0 + np.degrees(np.arctan2(x * np.sin(c), rr * np.cos(c) * np.cos(self.p0) - y * np.sin(c) * np.sin(self.p0)))
        return inside, lon, lat, x, y, np.sqrt(np.clip(1 - rho ** 2, 0, 1))

    def project(self, lon, lat):
        """Screen (x, y) of (lon, lat) and whether it faces the viewer."""
        p, dl = np.radians(lat), np.radians(lon - self.lon0)
        x = np.cos(p) * np.sin(dl)
        y = np.cos(self.p0) * np.sin(p) - np.sin(self.p0) * np.cos(p) * np.cos(dl)
        front = np.sin(self.p0) * np.sin(p) + np.cos(self.p0) * np.cos(p) * np.cos(dl) > 0
        return self.cx + self.r * x, self.cy - self.r * y, front


def tag(b, x, y, text, colour, right=False):
    """A label on a dark tag, readable over the planet."""
    if right:
        x -= raster.text_width(text)
    b.draw.rectangle([x - 2, y - 2, x + raster.text_width(text) + 1, y + 8], fill=(6, 8, 18, 200))
    b.label(x, y, text, colour)


def big(b, x, y, text, colour, right=False):
    """A label at twice the font's size (the counts)."""
    if right:
        x -= raster.text_width(text, 2)
    raster.draw_text(b.over, x, y, text, colour + (255,), scale=2)


def arrival():
    """Act 3 p1: Mars filling the transport's forward view (its glass framed by the canopy struts and
    the nav console), the CDF overlay on the glass with the colonies' beacons, every one dark but
    Ares Landing's."""
    b = Board()
    b.base *= 0.55
    rng = np.random.default_rng(3811)
    for _ in range(110):                                    # stars over the limb
        x, y = int(rng.integers(0, W)), int(rng.integers(0, H))
        b.base[y, x] += rng.uniform(40, 150)
    g = Globe(290, 268, 236, -72, -28)
    inside, lon, lat, nx, ny, nz = g.inverse(b.xx, b.yy)
    m = mars_map()
    hgt = np.where(inside, mars_sample(m["h"], lon, lat), 0)
    alb = mars_sample(m["a"], lon, lat)
    cap = mars_sample(m["cap"], lon, lat) > 0.5
    gy, gx = np.gradient(hgt)
    relief = np.clip(1 - 7 * (gx + gy), 0.5, 1.6)
    lam = -0.62 * nx + 0.5 * ny + 0.6 * nz                 # the sun from the top left
    light = np.clip(lam * 1.15 + 0.04, 0, 1) ** 0.75
    body = raster.ramp(MARS_TRUE, np.clip((0.35 + 0.75 * alb) * light * relief, 0, 1))
    body = np.where(cap[..., None], np.array([226, 214, 206.0]) * light[..., None], body)
    rho = np.hypot(nx, ny)
    b.base = np.where(inside[..., None], body, b.base)
    haze = np.exp(-((rho - 1) / 0.022) ** 2) * np.clip(lam + 0.5, 0, 1.2)       # the thin atmosphere at the limb
    b.base += haze[..., None] * np.array(BUTTERSCOTCH) * 0.7
    # the canopy: top bar, the left pillar, the strut before the nav screen, the console lip
    frame = (9, 11, 20)
    fill(b, [(0, 0), (W, 0), (W, 20), (0, 20)], frame)
    fill(b, [(0, 20), (24, 20), (8, H), (0, H)], frame)
    fill(b, [(512, 20), (530, 20), (548, H), (530, H)], frame)
    fill(b, [(530, 20), (W, 20), (W, H), (548, H)], (6, 10, 24))
    fill(b, [(0, 230), (548, 230), (548, H), (0, H)], frame)
    for x in range(40, 512, 48):
        b.glow(x, 235, 3, (60, 120, 160) if x % 96 else (160, 110, 40), 0.9)
    b.line([(0, 20), (512, 20)], CYAN_DIM, 1, 150)
    b.line([(24, 20), (8, H)], CYAN_DIM, 1, 110)
    b.line([(512, 20), (530, H - 10)], CYAN_DIM, 1, 110)
    b.line([(8, 230), (531, 230)], CYAN_DIM, 1, 150)
    b.title("CDF TRANSPORT - FORWARD VIEW")
    b.label(W - 10, 9, "MARS - DAY 9 AT FULL BURN", AMBER, right=True)
    # the overlay on the glass: the beacons, the Vrell in orbit over Ares Landing
    beacons = [(-139, 25), (-106, 6), (-100, -6), (-72, -7), (-47, -11), (-60, 30), (-12, 24), (-88, 34),
               (-150, -6), (-120, 38), (-20, -2), (-84, -24)]
    for lo, la in beacons:
        x, y, front = g.project(lo, la)
        if front and y < 226:
            b.draw.ellipse([x - 4, y - 4, x + 4, y + 4], fill=(6, 8, 18, 190))
            b.ring(x, y, 4, colour=GREY, alpha=230)
            b.line([(x - 2, y - 2), (x + 2, y + 2)], GREY, 1, 230)
    ax, ay, _ = g.project(*ARES_LANDING)
    b.glow(ax, ay, 10, (220, 140, 40), 1.0)
    for k, r in enumerate((5, 10, 16, 23)):
        b.ring(ax, ay, r, colour=AMBER, alpha=235 - 45 * k)
    b.marker(ax, ay, "diamond", AMBER, 3)
    b.line([(ax - 8, ay - 8), (ax - 28, ay - 44), (ax - 44, ay - 44)], AMBER, 1, 220)
    tag(b, ax - 48, ay - 48, "ARES LANDING", AMBER, right=True)
    tag(b, ax - 48, ay - 38, "BEACON LIT", WHITE, right=True)
    for k in range(7):
        a = rng.uniform(0, 2 * np.pi)
        r = rng.uniform(26, 44)
        b.marker(ax + r * np.cos(a), ay - abs(r * np.sin(a)) * 0.7 - 6, "chevron", RED, 2)
    tx, ty, _ = g.project(*THARSIS_FOUNDRY)
    b.line([(tx + 5, ty - 5), (tx + 22, ty - 28), (tx + 26, ty - 28)], GREY, 1, 220)
    tag(b, tx + 30, ty - 32, "THARSIS FOUNDRY", WHITE)
    tag(b, tx + 30, ty - 22, "DARK", RED)
    ox, oy, _ = g.project(*OLYMPUS)
    b.bracket(ox - 12, oy - 9, ox + 12, oy + 9, CYAN_DIM, 4)
    tag(b, ox - 20, oy + 13, "OLYMPUS MONS", CYAN)
    b.bracket(30, 26, 506, 226, CYAN_DIM, 10)
    # the nav screen
    px = 560
    b.label(px, 30, "COLONY BEACONS", WHITE)
    b.label(px, 44, "LISTED", CYAN_DIM)
    b.label(W - 12, 44, "12", CYAN, right=True)
    b.label(px, 54, "ANSWERING", CYAN_DIM)
    b.label(W - 12, 54, "1", AMBER, right=True)
    b.label(px, 64, "DARK", CYAN_DIM)
    b.label(W - 12, 64, "11", RED, right=True)
    b.line([(px, 80), (W - 12, 80)], CYAN_DIM, 1, 120)
    b.label(px, 88, "ARES LANDING", AMBER)
    b.label(px, 98, "HOLDING ALONE", WHITE)
    big(b, px, 112, "5 WEEKS", AMBER)
    b.label(px, 136, "NO FLEET", CYAN_DIM)
    b.label(px, 146, "NO SUPPLY DROPS", CYAN_DIM)
    b.label(px, 156, "MILITIA GUNS", CYAN_DIM)
    b.line([(px, 172), (W - 12, 172)], CYAN_DIM, 1, 120)
    b.marker(px + 4, 184, "chevron", RED, 3)
    b.label(px + 14, 180, "VRELL OVER", RED)
    b.label(px + 14, 190, "ARES LANDING", RED)
    b.label(px, 214, "CUT OFF FROM", CYAN_DIM)
    b.label(px, 224, "EVERY OTHER", CYAN_DIM)
    return b


FRONT_X, FRONT_Y, FRONT_K = 28, 26, 1.55               # the front map: x, y of (FRONT_LON, FRONT_LAT), px a degree
FRONT_LON, FRONT_LAT, FRONT_LON1, FRONT_LAT1 = -165.0, 62.0, 105.0, -70.0


def mgeo(lon, lat):
    return FRONT_X + (lon - FRONT_LON) * FRONT_K, FRONT_Y + (FRONT_LAT - lat) * FRONT_K


def wiggle(p0, p1, amp, seed, n=40):
    """A path from p0 to p1 (lon, lat) bent sideways by a few sines, ends fixed."""
    rng = np.random.default_rng(seed)
    p0, p1 = np.array(p0, float), np.array(p1, float)
    s = np.linspace(0, 1, n)
    d = p1 - p0
    nrm = np.array([-d[1], d[0]]) / np.hypot(*d)
    off = sum(rng.uniform(0.4, 1.0) * np.sin(np.pi * s * k + rng.uniform(0, 6.3)) / k for k in (1, 2, 3, 5))
    off = (off - off[0] * (1 - s) - off[-1] * s) * amp
    return p0 + s[:, None] * d + off[:, None] * nrm


def mars_front():
    """Act 3 p2: Varga's CDF globe display of Mars as a flat projection: the Hellas basin in the south
    glowing Vrell red, the growth spreading north from it in tendrils, Ares Landing on the edge of the
    northern plains, Tharsis Foundry on the plateau, the Valles Marineris between them."""
    b = Board()
    x0, y0 = mgeo(FRONT_LON, FRONT_LAT)
    x1, y1 = mgeo(FRONT_LON1, FRONT_LAT1)
    box = (b.xx >= x0) & (b.xx < x1) & (b.yy >= y0) & (b.yy < y1)
    lon = FRONT_LON + (b.xx - FRONT_X) / FRONT_K
    lat = FRONT_LAT - (b.yy - FRONT_Y) / FRONT_K
    m = mars_map()
    hgt = mars_sample(m["h"], lon, lat)
    alb = mars_sample(m["a"], lon, lat)
    gy, gx = np.gradient(hgt)
    shade = np.clip(0.5 + 0.28 * hgt - 9 * (gx + gy), 0, 1.2) * (0.6 + 0.5 * alb)
    body = raster.ramp(MARS_HOLO, np.clip(shade, 0, 1))
    b.base = np.where(box[..., None], body, b.base)
    hx, hy = mgeo(*HELLAS)
    dh = gc(lon, lat, *HELLAS)
    tint = np.exp(-(dh / 16) ** 2) * box
    b.base = b.base * (1 - 0.6 * tint[..., None]) + tint[..., None] * np.array([150, 24, 40.0]) * 0.8
    b.glow(hx, hy, 34, (120, 16, 36), 1.0)
    b.title("CDF GLOBAL DISPLAY - MARS")
    for lo in range(-150, 105, 30):
        b.line([mgeo(lo, FRONT_LAT), mgeo(lo, FRONT_LAT1)], CYAN_DIM, 1, 45)
    for la in (60, 30, 0, -30, -60):
        b.line([mgeo(FRONT_LON, la), mgeo(FRONT_LON1, la)], CYAN_DIM, 1, 90 if la == 0 else 45)
    b.draw.rectangle([x0, y0, x1 - 1, y1 - 1], outline=CYAN_DIM + (180,))
    # the Vrell growth: tendrils out of the basin, spreading north, a few branching
    targets = [((58, -24), (74, 14), 7), ((80, -26), (98, 6), 5), ((50, -32), (22, 20), 9),
               ((46, -40), (-8, -2), 8), ((40, -46), (-24, 6), 10), ((60, -58), (-62, -40), 9),
               ((90, -38), (100, -16), 3), ((-62, -40), (-92, -16), 5)]
    rng = np.random.default_rng(3821)
    for k, (p0, p1, amp) in enumerate(targets):
        path = wiggle(p0, p1, amp, 3830 + k)
        pts = [mgeo(*p) for p in path]
        for i in range(len(pts) - 1):
            w = 3 if i < len(pts) * 0.35 else 2 if i < len(pts) * 0.7 else 1
            b.line([pts[i], pts[i + 1]], VIOLET, w, 220)
        for p in pts[::5]:
            b.glow(*p, 4, (90, 20, 60), 0.6)
        for _ in range(2):                                  # side shoots
            i = int(rng.integers(len(path) // 3, len(path) - 6))
            tip = path[i] + rng.normal(0, 1, 2) * 3 + (path[-1] - path[0]) * 0.18
            b.line([mgeo(*q) for q in wiggle(path[i], tip, 2, 3900 + k * 7 + i, 10)], VIOLET, 1, 180)
    # the canyon, the volcano, the colonies
    b.line([mgeo(*p) for p in VALLES], CYAN, 1, 200)
    vx, vy = mgeo(-80, -9)
    b.label(vx - 30, vy + 10, "VALLES MARINERIS", CYAN)
    ox, oy = mgeo(*OLYMPUS)
    b.marker(ox, oy, "dot", CYAN_DIM, 2)
    b.label(ox - 30, oy - 14, "OLYMPUS MONS", CYAN_DIM)
    ax, ay = mgeo(*ARES_LANDING)
    for k, r in enumerate((5, 10, 15)):
        b.ring(ax, ay, r, colour=AMBER, alpha=230 - 60 * k)
    b.glow(ax, ay, 8, (200, 120, 30), 0.9)
    b.marker(ax, ay, "diamond", AMBER, 3)
    b.label(ax - 30, ay - 30, "ARES LANDING", AMBER)
    b.label(ax - 30, ay - 20, "NORTHERN PLAINS", CYAN_DIM)
    tx, ty = mgeo(*THARSIS_FOUNDRY)
    b.marker(tx, ty, "diamond", GREY, 3)
    b.label(FRONT_X + 4, ty + 8, "THARSIS FOUNDRY", GREY)
    b.label(FRONT_X + 4, ty + 18, "THARSIS PLATEAU", CYAN_DIM)
    b.ring(hx, hy, 18, colour=RED, alpha=220, width=2)
    b.ring(hx, hy, 26, colour=VIOLET, alpha=150)
    b.label(hx - 36, hy + 30, "HELLAS BASIN", RED)
    b.label(hx - 36, hy + 40, "WHERE THEY LANDED", RED)
    # the analysis
    px = 462
    b.line([(px - 8, 26), (px - 8, H - 12)], CYAN_DIM, 1, 90)
    b.label(px, 30, "VRELL ON MARS", WHITE)
    b.label(px, 44, "CAME DOWN: HELLAS BASIN", RED)
    b.label(px, 54, "SPREAD: NORTH", VIOLET)
    b.line([(px, 70), (W - 12, 70)], CYAN_DIM, 1, 120)
    b.label(px, 78, "CHANGED FOR MARS", WHITE)
    b.label(px, 92, "SWIM THROUGH THE SAND", AMBER)
    b.label(px, 102, "RIDE THE DUST STORMS", AMBER)
    b.line([(px, 118), (W - 12, 118)], CYAN_DIM, 1, 120)
    b.marker(px + 4, 132, "diamond", AMBER, 3)
    b.label(px + 14, 129, "COLONY - HOLDING", AMBER)
    b.marker(px + 4, 146, "diamond", GREY, 3)
    b.label(px + 14, 143, "COLONY - CUT OFF", GREY)
    b.line([(px - 1, 160), (px + 9, 157)], VIOLET, 2)
    b.label(px + 14, 157, "VRELL GROWTH", VIOLET)
    b.label(W - 10, 9, "LEARNING THIS PLANET FASTER THAN WE DID", AMBER, right=True)
    return b


def rim_lit(rgba, colour, reach, strength=0.9):
    """``rgba`` (a float array) with a rim light from the right: the pixels within ``reach`` of the
    shape's right-hand edge brightened toward ``colour``."""
    a = rgba[..., 3] > 0
    edge = np.zeros_like(a)
    for k in range(1, reach + 1):
        shifted = np.zeros_like(a)
        shifted[:, :-k] = a[:, k:]
        edge |= a & ~shifted
    out = rgba.copy()
    out[edge, :3] = out[edge, :3] * (1 - strength) + np.array(colour, float) * strength
    return out


GUNNER_SS = 4                                          # the gun pit's supersampling


def gun_pit(scale=1.45):
    """The militia gun pit at dusk as one sprite (160x100 at ``scale``, the muzzles of the guns to the top right): a twin
    autocannon on its pedestal, the gunner in a dust-orange suit (the dust-suit controller's look of
    design/story/characters, generic-colonial-r36-b: goggles on the brow, the respirator at the
    throat, the suit's seal ring, an air pack) in its seat, head tilted back to look up; the regolith
    bags of the pit in front. Silhouetted against the sunset, rim-lit from the right."""
    s = GUNNER_SS
    w, h = 160, 100
    img = Image.new("RGBA", (w * s, h * s), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    def P(*pts):
        return [(x * s, y * s) for x, y in pts]

    metal, metal2 = (40, 36, 42, 255), (62, 56, 60, 255)
    suit, suit2 = (104, 52, 28, 255), (132, 70, 36, 255)
    # the gun: pedestal, cradle, twin barrels at 35 degrees, the drum
    d.polygon(P((84, 98), (92, 98), (91, 66), (85, 66)), fill=metal)
    el = np.radians(35)
    dv, nv = np.array([np.cos(el), -np.sin(el)]), np.array([np.sin(el), np.cos(el)])
    c = np.array([88.0, 62.0])
    for off in (-3.0, 3.0):
        p0, p1 = c + nv * off - dv * 4, c + nv * off + dv * 70
        d.line(P(tuple(p0), tuple(p1)), fill=metal2, width=3 * s)
        q0, q1 = c + nv * off + dv * 64, c + nv * off + dv * 72
        d.line(P(tuple(q0), tuple(q1)), fill=metal, width=5 * s)
    box = [c - dv * 10 - nv * 7, c + dv * 10 - nv * 7, c + dv * 10 + nv * 8, c - dv * 10 + nv * 8]
    d.polygon(P(*[tuple(p) for p in box]), fill=metal)
    d.ellipse(P((96, 62), (110, 76)), fill=metal2)                 # the drum
    d.line(P((76, 74), (84, 70)), fill=metal, width=2 * s)          # the grips
    # the gunner, in the seat left of the cradle, leaning back, face turned up to the top right
    d.polygon(P((44, 84), (62, 84), (62, 89), (44, 89)), fill=metal)                # the seat
    d.polygon(P((33, 57), (41, 54), (47, 79), (39, 81)), fill=(66, 60, 58, 255))   # the air pack
    d.polygon(P((45, 85), (63, 85), (57, 58), (41, 60)), fill=suit)                # the torso, leaning back
    d.polygon(P((56, 78), (72, 74), (76, 96), (69, 97), (66, 82), (56, 86)), fill=suit)   # the leg
    d.line(P((53, 61), (64, 71), (77, 72)), fill=suit2, width=5 * s, joint="curve")    # the arm to the grips
    d.ellipse(P((42, 53), (58, 60)), fill=(58, 64, 66, 255))                        # the seal ring
    d.ellipse(P((43, 38), (57, 53)), fill=(70, 50, 42, 255))                        # the hood
    d.polygon(P((50, 41), (56, 37), (61, 44), (58, 51), (52, 50)), fill=(150, 98, 74, 255))   # the face, upturned
    d.line(P((44, 44), (55, 37)), fill=(40, 34, 34, 255), width=2 * s)              # the goggles' strap
    d.ellipse(P((51, 34), (58, 40)), fill=(255, 190, 60, 255))                     # the goggles on the brow
    d.polygon(P((55, 50), (61, 47), (63, 52), (57, 55)), fill=(58, 72, 72, 255))   # the respirator at the throat
    # the regolith bags along the front of the pit
    rng = np.random.default_rng(3841)
    for row, y in enumerate((80, 88)):
        x = -6 + row * 9
        while x < w:
            bw = rng.uniform(16, 22)
            d.rounded_rectangle(P((x, y + rng.uniform(-1, 1)), (x + bw, y + 11)), radius=4 * s,
                                fill=(50 + int(rng.integers(0, 14)), 30, 22, 255))
            x += bw + 1
    a = np.array(img).astype(np.float64)
    a = rim_lit(a, (240, 166, 98), 2 * s)
    small = np.array(Image.fromarray(a.astype(np.uint8), "RGBA").resize((round(w * scale), round(h * scale)), Image.LANCZOS))
    small[..., 3] = np.where(small[..., 3] > 110, 255, 0)
    return artkit.quantize_set([Image.fromarray(small)], 40)[0]


def colonists():
    """Act 3 p3: a militia gun pit on a dome's ring at Ares Landing at dusk under a butterscotch sky
    (the sun just down behind the horizon's mesas, its small blue halo), the gunner looking up at
    the Vrell overhead; behind him the dome's geodesic glass (dome a of design/world/mars: white struts
    in a triangle lattice, crops glowing green inside), its panels patched with plates and tape."""
    b = Board()
    hz = 196
    t = np.clip(b.yy / hz, 0, 1)
    sky = raster.ramp([(0.0, (34, 20, 30)), (0.45, (92, 52, 46)), (0.8, (176, 116, 74)), (1.0, BUTTERSCOTCH)], t)
    b.base = sky
    sx, sy = 590, hz - 4
    b.glow(sx, sy, 70, (60, 40, 20), 1.0)
    b.glow(sx, sy, 14, (60, 100, 150), 1.0)                # the Martian sunset's blue halo
    b.glow(sx, sy, 3, (200, 220, 230), 1.0)
    mesa = hz - 2 + 4 * np.sin(b.xx / 37) + 3 * np.sin(b.xx / 11) + np.where((b.xx > 470) & (b.xx < 540), -8, 0)
    b.base = np.where((b.yy > mesa)[..., None], np.array([60, 32, 26.0]) * (1 + 0.2 * np.sin(b.xx / 5))[..., None], b.base)
    # the dome: a sphere's side, seen from its ring
    cx, cy, R = 150.0, 268.0, 280.0
    nx, ny = (b.xx - cx) / R, (b.yy - cy) / R
    rr = nx ** 2 + ny ** 2
    dome = (rr < 1) & (b.yy < 214)
    nz = np.sqrt(np.clip(1 - rr, 0, 1))
    az = np.arctan2(nx, nz)
    el = np.arcsin(np.clip(-ny, -1, 1))
    sp = 0.16
    u, v, w_ = el / sp, (az + el * 0.577) / sp, (az - el * 0.577) / sp
    strut = (np.abs(u - np.round(u)) < 0.05 / np.maximum(nz, 0.3)) | \
            (np.abs(v - np.round(v)) < 0.045 / np.maximum(nz, 0.3)) | \
            (np.abs(w_ - np.round(w_)) < 0.045 / np.maximum(nz, 0.3))
    cell = np.floor(u) * 7919 + np.floor(v) * 104729 + np.floor(w_) * 1299709
    hsh = (np.sin(cell * 12.9898) * 43758.5453) % 1.0
    rows = (np.sin(b.yy / 3.2) > 0.2) & (b.yy > 120)       # the crop rows inside
    glass = np.array([16, 44, 34.0]) + rows[..., None] * np.array([30, 90, 46.0]) * np.clip((b.yy - 110) / 90, 0, 1)[..., None]
    sheen = np.clip(nx * 1.4 - 0.2, 0, 1) ** 2 * np.clip(1 - nz * 0.8, 0, 1)
    glass = glass + sheen[..., None] * np.array([150, 100, 60.0])
    near = np.exp(-(((b.xx - 236) / 70) ** 2 + ((b.yy - 140) / 50) ** 2))    # the patched stretch
    plate = hsh < 0.05 + 0.3 * near
    taped = (hsh >= 0.05 + 0.3 * near) & (hsh < 0.1 + 0.5 * near)
    glass = np.where(plate[..., None], np.array([76, 70, 70.0]) + 10 * np.sin(b.xx * 2.1)[..., None], glass)
    tape = taped & ((np.abs((b.xx - b.yy) % 14 - 7) < 1.0) | (np.abs((b.xx + b.yy) % 14 - 7) < 1.0))
    glass = np.where(tape[..., None], np.array([210, 168, 84.0]), glass)
    lit = np.clip(0.4 + 0.6 * nx + 0.3 * nz, 0.25, 1)
    glass = np.where(strut[..., None], np.array([170, 156, 146.0]) * lit[..., None], glass)
    b.base = np.where(dome[..., None], glass, b.base)
    b.base += (np.exp(-((np.sqrt(rr) - 1) / 0.004) ** 2) * (b.yy < 214))[..., None] * np.array([120, 90, 60])
    # the ring: concrete, its joints
    ring = b.yy >= 214
    b.base = np.where(ring[..., None], np.array([66, 52, 50.0]) + 14 * ((b.xx % 48) > 1)[..., None] - 30 * (b.yy < 216)[..., None], b.base)
    b.title("ARES LANDING - BRADBURY DOME RING, DUSK")
    b.label(W - 10, 9, "THEY WILL NOT THANK US", AMBER, right=True)
    # the pit and the gunner
    pit = gun_pit()
    gx, gy = 396, 240 - pit.height // 2 - 2
    b.sprite(pit, gx, gy)
    b.label(gx - 40, gy - pit.height // 2 - 2, "MILITIA GUN PIT 4", AMBER)
    # the patches on the glass
    b.bracket(186, 104, 290, 178, WHITE, 5)
    b.label(186, 92, "PATCHED PANELS", WHITE)
    b.label(14, 26, "FIVE WEEKS UNDER SIEGE", WHITE)
    b.label(14, 36, "THE DOMES STILL MAKE AIR", GREEN)
    # what the gunner is watching: the Vrell high overhead
    for x, y in ((560, 44), (580, 56), (600, 40), (542, 62), (618, 66)):
        b.marker(x, y, "chevron", RED, 3)
    b.bracket(530, 30, 632, 76, RED, 5)
    b.label(530, 82, "VRELL - OVERHEAD", RED)
    b.label(W - 10, H - 16, "WE FLY FOR THEM ANYWAY", AMBER, right=True)
    return b


def transport_holo(scale):
    """The heavy transport of the Act 2 outro (tools/art/outro_l14_images.py's model) as the display
    draws it."""
    import outro_l14_images as o14                    # noqa: E402  (imports this module: kept local)
    return holo(scaled(o14.transport_sprite(), scale), CYAN, 12)


def descent_ground(x):
    """The ground's height in px on the descent plot at x: Olympus Mons' shield with its caldera and
    the basal escarpment, the station's floor, the dunes, the rail line's plain."""
    x = np.asarray(x, float)
    west = 122 * np.clip(1 - (np.abs(x - 92) / 120) ** 1.5, 0, 1)
    east = np.where(x < 248, 52 + 70 * np.clip(1 - (np.clip(x - 92, 0, None) / 156) ** 1.2, 0, 1), 0)
    hgt = np.where(x < 92, west, east)
    hgt -= 12 * np.exp(-((x - 92) / 7) ** 6)                       # the caldera's flat floor
    hgt = np.where((x >= 248) & (x < 258), 52 * (258 - x) / 10, hgt)  # the escarpment
    dunes = (x > 286) & (x < 430)
    hgt += np.where(dunes, 4 + 3 * np.abs(np.sin((x - 286) / 9)) ** 0.6 * np.sign(np.sin((x - 286) / 18)), 0)
    return hgt


def descent_route():
    """L15 p1: Okafor's entry corridor as a CDF profile plot, west to east: the transport in orbit,
    the descent through the entry corridor over Olympus Mons' caldera, down through the cloud deck
    along the flank, over the Lycus escarpment with Lycus Survey Station at its foot, across the
    Tharsis dunes to the colony rail line, east toward Ares Landing; Lancer and Rook; the numbers
    are the level's sections."""
    b = Board()
    datum = 218
    ground = datum - descent_ground(b.xx[0])
    under = b.yy > ground[None, :]
    plot = b.xx < 500
    tex = raster.fbm(W, H, 12, 3851, octaves=3, period=False)
    depth = np.clip((b.yy - ground[None, :]) / 60, 0, 1)
    body = raster.ramp(MARS_HOLO, np.clip(0.95 - 0.7 * depth + 0.18 * (tex - 0.5), 0, 1))
    b.base = np.where((under & plot)[..., None], body, b.base)
    # the cloud deck along the flank, the cirrus above
    puff = raster.fbm(W, H, 14, 3852, octaves=4, period=False)
    band = np.exp(-((b.yy - 140) / 12) ** 2) * np.clip((b.xx - 112) / 30, 0, 1) * np.clip((440 - b.xx) / 40, 0, 1)
    cloud = np.clip((puff * band - 0.22) * 3.0, 0, 1) * ~under * plot
    b.base = b.base * (1 - 0.8 * cloud[..., None]) + cloud[..., None] * np.array([150, 124, 120.0])
    streak = np.zeros((H, W))
    for y, x0, x1 in ((74, 130, 300), (82, 200, 390), (68, 320, 470)):       # the thin cirrus
        streak += np.exp(-((b.yy - y) / 1.6) ** 2) * np.clip((b.xx - x0) / 30, 0, 1) * np.clip((x1 - b.xx) / 30, 0, 1)
    b.base += (np.clip(streak, 0, 1) * (0.5 + 0.5 * puff))[..., None] * np.array([60, 60, 74.0])
    b.line([(x, ground[x]) for x in range(0, 500, 2)], CYAN, 1, 220)
    b.line([(0, datum), (500, datum)], CYAN_DIM, 1, 90)
    b.title("OLYMPUS DESCENT - THE ENTRY CORRIDOR")
    b.label(W - 10, 9, "DON'T KEEP THEM WAITING", AMBER, right=True)
    # the transport in orbit, the corridor down
    b.dashed([(0, 36), (500, 32)], CYAN_DIM, 1, 3, 5)
    b.label(14, 26, "ORBIT", CYAN_DIM)
    b.sprite(transport_holo(0.3), 70, 40)
    route = [(96, 46), (140, 64), (184, 92), (214, 118), (240, 146), (262, 172), (280, 192), (330, 196),
             (400, 198), (470, 202)]
    for p, q in zip(route[:3], route[1:3]):
        b.glow(*q, 6, (120, 60, 20), 0.6)                        # the entry glow, fading
    b.dashed(route, AMBER, 2)
    b.arrow(470, 202, 504, 203, AMBER, size=5, dashed=False)
    b.sprite(scaled_sprite("ship_2", 0.6), 168, 54)
    b.sprite(scaled_sprite("rook_2", 0.6), 194, 62)
    b.label(208, 46, "LANCER, ROOK", CYAN)
    for n, (x, y) in enumerate(((128, 70), (226, 116), (234, 160), (266, 202), (380, 186)), 1):
        b.draw.rectangle([x - 4, y - 5, x + 4, y + 5], fill=(10, 14, 30, 230), outline=AMBER + (255,))
        b.label(x - 2, y - 3, str(n), AMBER)
    # the places
    b.label(54, 82, "OLYMPUS MONS", CYAN)
    b.label(54, 92, "22 KM", CYAN_DIM)
    b.line([(92, 108), (92, 100)], CYAN_DIM, 1, 200)
    b.label(330, 118, "CLOUD DECK", WHITE)
    b.label(330, 58, "CIRRUS", CYAN_DIM)
    b.marker(272, 212, "square", GREY, 3)
    tag(b, 270, 224, "LYCUS SURVEY STATION - NO SIGNAL", GREY)
    for x in range(436, 500, 12):                                 # the rail line and its pylons
        b.line([(x, datum - 2), (x, datum - 12)], CYAN_DIM, 1, 200)
    b.line([(430, datum - 3), (500, datum - 3)], CYAN, 1, 220)
    b.label(430, 160, "RAIL LINE", CYAN)
    b.line([(440, 168), (446, datum - 14)], CYAN_DIM, 1, 160)
    for x, y in ((236, 98), (252, 108), (318, 150), (300, 166)):
        b.marker(x, y, "chevron", RED, 2)
    # the legend: the level's sections
    px = 516
    b.line([(px - 8, 26), (px - 8, H - 12)], CYAN_DIM, 1, 90)
    b.label(px, 30, "THE WAY IN, WEST TO EAST", WHITE)
    rows = [("1", "ENTRY CORRIDOR", CYAN), ("2", "THE CLOUD DECK", CYAN), ("3", "OLYMPUS SLOPES", CYAN),
            ("4", "LYCUS ESCARPMENT", CYAN), ("5", "THARSIS DUNES", CYAN)]
    for i, (n, name, colour) in enumerate(rows):
        y = 48 + 14 * i
        b.label(px, y, n, AMBER)
        b.label(px + 12, y, name, colour)
    b.label(px + 12, 118, "TO THE RAIL LINE", CYAN_DIM)
    b.line([(px, 134), (W - 12, 134)], CYAN_DIM, 1, 120)
    b.label(px, 142, "ARES LANDING", AMBER)
    b.label(px, 152, "EAST ALONG THE RAIL", CYAN_DIM)
    b.label(px, 166, "HOLDING 5 WEEKS", WHITE)
    b.marker(px + 4, 190, "chevron", RED, 3)
    b.label(px + 14, 186, "VRELL OWN THE SKY", RED)
    b.label(px + 14, 196, "OVER ARES LANDING", RED)
    b.label(W - 10, H - 16, "WE COME IN FROM THE WEST", CYAN_DIM, right=True)
    return b


def burrower_scan():
    """L15 p2: Varga's cut through the regolith: a Burrower swimming under the sand (a scan silhouette of
    the production sprite, its ripples on the surface above), the guns stopping at the surface over it;
    the bulge, the 1 s tell, with the Burrower rising under it; the surfaced maw (its bow wave, the open
    maw and its lime glow) firing its ring of ten orbs; the anti-ground x2 marker."""
    b = Board()
    sl = 118                                                         # the surface
    tex = raster.fbm(W, H, 16, 3861, octaves=4, period=False)
    strata = 0.5 + 0.5 * np.sin((b.yy - sl) / 7 + 3 * tex)
    depth = np.clip((b.yy - sl) / (H - sl), 0, 1)
    body = raster.ramp(MARS_HOLO, np.clip(0.75 - 0.55 * depth + 0.12 * strata + 0.1 * (tex - 0.5), 0, 1))
    b.base = np.where((b.yy > sl)[..., None], body, b.base)
    b.title("SENSOR SCAN - BURROWER - A CUT THROUGH THE REGOLITH")
    b.label(W - 10, 9, "SHOOT WHERE THE SAND BULGES", AMBER, right=True)
    b.line([(0, sl), (W, sl)], CYAN, 1, 230)
    b.label(W - 10, sl - 12, "SURFACE", CYAN, right=True)
    b.label(W - 10, sl + 6, "REGOLITH", CYAN_DIM, right=True)
    # 1: buried, swimming; nothing reaches it
    bx, by = 112, sl + 58
    b.sprite(holo(asset("burrower_0"), VIOLET), bx, by)
    b.dashed([(bx - 92, by + 6), (bx - 40, by + 2)], VIOLET, 1, 4, 3)
    b.arrow(bx + 40, by, bx + 84, by - 10, VIOLET, size=4)
    for k, x in enumerate((bx - 50, bx - 18, bx + 14)):                # its ripples above, heading right
        b.sprite(asset(f"burrower-ripple_{12 * 4 + k}"), x, sl - 2)
    b.sprite(scaled_sprite("ship_2", 0.75), bx, 40)
    for dx in (-8, 8):
        b.dashed([(bx + dx, 58), (bx + dx, sl - 6)], AMBER, 1, 4, 3)
        b.marker(bx + dx, sl - 4, "cross", AMBER, 2)
    b.label(14, 26, "BURIED: NOTHING", RED)
    b.label(14, 36, "TOUCHES IT", RED)
    b.label(14, H - 16, "IT SWIMS UNDER THE SAND", VIOLET)
    # 2: the bulge, the tell
    ux = 300
    b.sprite(holo(asset("burrower_0"), VIOLET), ux, sl + 34)
    b.arrow(ux + 40, sl + 52, ux + 40, sl + 18, VIOLET, size=4, dashed=False)
    b.sprite(asset("burrower-bulge_7"), ux, sl - 4)
    b.bracket(ux - 34, sl - 38, ux + 34, sl + 26, AMBER, 6)
    b.label(ux - 34, sl - 62, "THE BULGE: 1 S", AMBER)
    b.label(ux - 34, sl - 52, "SHOOT THERE", AMBER)
    # 3: surfaced, firing its ring, then it dives
    mx, my = 486, sl - 30
    b.sprite(asset("burrower-bow_0"), mx, my)
    b.sprite(asset("burrower_3"), mx, my)
    glow = np.array(asset("burrower-glow_3")).astype(np.float64)
    light_at(b, glow[..., :3] * glow[..., 3:4] / 255, mx, my)
    orb = asset("orb_0")
    for k in range(10):
        a = 2 * np.pi * k / 10 + 0.3
        ox, oy = mx + 58 * np.cos(a), my + 58 * np.sin(a) * 0.82
        b.sprite(orb, ox, oy)
        b.line([(mx + 38 * np.cos(a), my + 38 * np.sin(a) * 0.82), (ox - 7 * np.cos(a), oy - 7 * np.sin(a))], VIOLET, 1, 150)
    b.label(mx + 72, my - 34, "SURFACED:", WHITE)
    b.label(mx + 72, my - 24, "A RING OF 10", VIOLET)
    b.label(mx + 72, my - 14, "THEN IT DIVES", CYAN_DIM)
    b.sprite(holo(asset("burrower-dive_5"), VIOLET), mx - 6, sl + 62)
    b.arrow(mx + 30, sl + 30, mx + 24, sl + 52, VIOLET, size=3, dashed=False)
    # what to bring
    gx, gy = 360, 30
    b.draw.rectangle([gx - 4, gy - 6, gx + 72, gy + 26], outline=GREEN + (240,))
    b.sprite(asset("bomb-rack-shot"), gx + 8, gy + 10)
    b.label(gx + 18, gy - 1, "ANTI-", GREEN)
    b.label(gx + 18, gy + 9, "GROUND X2", GREEN)
    b.arrow(gx + 72, gy + 14, mx - 34, my - 22, GREEN, size=4)
    return b



# =========================================================================== Level 16 (M7 part C)

HELD_SPRITES = ROOT / "build" / "art-held" / "sprites"
L16 = ROOT / "assets" / "backdrop" / "level-16"
CANYON_X, CANYON_Y, CANYON_K = 22, 26, 5.4            # the canyon plot: x, y of (CANYON_LON, CANYON_LAT), px a degree
CANYON_LON, CANYON_LAT, CANYON_LON1, CANYON_LAT1 = -106.0, 22.0, -24.0, -14.5
CANYON_ROUTE = [(-101, -6), (-92, -8), (-80, -9), (-68, -11), (-56, -12), (-47, -11), (-40, -7), (-36, 0), (-33, 8),
                (-31, 16)]
HELIX = (-63.5, -7.2)                                 # Helix's research station on the north rim (not named)


def held_asset(name):
    """A production sprite from assets/, or from build/art-held/ while no data claims it yet."""
    for folder in (SPRITES, HELD_SPRITES):
        if (folder / f"{name}.png").exists():
            return Image.open(folder / f"{name}.png").convert("RGBA")
    raise FileNotFoundError(name)


def cgeo(lon, lat):
    return CANYON_X + (lon - CANYON_LON) * CANYON_K, CANYON_Y + (CANYON_LAT - lat) * CANYON_K


def canyon_route():
    """L16 p1: Okafor's plot of the Valles Marineris from orbit, west to east: the Noctis Labyrinthus's maze of
    graben at the west end, the canyon cut deep through the plateau, the fork's mesa, the Coprates rapids, the
    narrows, the Eos outflow's chaos opening north toward Ares Landing; the route along it with the level's six
    sections; on the north rim by the rapids a small black square the plot does not name (Helix's station)."""
    b = Board()
    x0, y0 = cgeo(CANYON_LON, CANYON_LAT)
    x1, y1 = cgeo(CANYON_LON1, CANYON_LAT1)
    box = (b.xx >= x0) & (b.xx < x1) & (b.yy >= y0) & (b.yy < y1)
    lon = CANYON_LON + (b.xx - CANYON_X) / CANYON_K
    lat = CANYON_LAT - (b.yy - CANYON_Y) / CANYON_K
    m = mars_map()
    hgt = mars_sample(m["h"], lon, lat) * 0.6
    alb = mars_sample(m["a"], lon, lat)
    fine = raster.fbm(W, H, 16, 3871, octaves=4, period=False)
    # the canyon at this scale: the route's trough, wide in the middle, the fork's mesa, the Noctis maze, Eos's chaos
    valles = CANYON_ROUTE[:-2]
    dv = polyline_distance(lon, lat, valles)
    width = 1.5 + 1.2 * np.exp(-((lon + 68) / 14) ** 2) - 0.7 * np.exp(-((lon + 52) / 4) ** 2)
    trough = np.clip(1 - dv / width, 0, 1) ** 0.6 * (lon < -36)
    mesa = np.exp(-(((lon + 80) / 2.4) ** 2 + ((lat + 9) / 0.5) ** 2))
    maze = np.exp(-gc(lon, lat, -102, -7) ** 2 / 30) * ((np.abs(np.sin(lon * 1.9 + 4.8 * fine)) < 0.18)
                                                        | (np.abs(np.sin(lat * 2.3 + 4 * fine)) < 0.15))
    chaos = np.exp(-gc(lon, lat, -40, -4) ** 2 / 18) * (fine > 0.55)
    hgt = hgt - 0.9 * trough + 0.6 * mesa * trough - 0.4 * np.clip(maze, 0, 1) - 0.3 * chaos + 0.08 * (fine - 0.5)
    gy, gx = np.gradient(hgt)
    shade = np.clip(0.55 + 0.25 * hgt - 14 * (gx + gy), 0, 1.2) * (0.65 + 0.45 * alb) * (1 - 0.55 * trough)
    body = raster.ramp(MARS_HOLO, np.clip(shade, 0, 1))
    b.base = np.where(box[..., None], body, b.base)
    b.title("VALLES MARINERIS - THE CANYON RUN")
    b.label(W - 10, 9, "THE ONE ROAD THEY DON'T HOLD", AMBER, right=True)
    for lo in range(-100, -24, 10):
        b.line([cgeo(lo, CANYON_LAT), cgeo(lo, CANYON_LAT1)], CYAN_DIM, 1, 40)
    for la in (20, 10, 0, -10):
        b.line([cgeo(CANYON_LON, la), cgeo(CANYON_LON1, la)], CYAN_DIM, 1, 80 if la == 0 else 40)
    b.draw.rectangle([x0, y0, x1 - 1, y1 - 1], outline=CYAN_DIM + (180,))
    # the route along the canyon, the sections numbered
    route = [cgeo(*p) for p in wiggle(CANYON_ROUTE[0], CANYON_ROUTE[1], 0.3, 3872, 6)[:-1]]
    for p0, p1 in zip(CANYON_ROUTE[1:-1], CANYON_ROUTE[2:]):
        route += [cgeo(*p) for p in wiggle(p0, p1, 0.25, 3873 + int(p0[0]), 8)[:-1]]
    route.append(cgeo(*CANYON_ROUTE[-1]))
    b.dashed(route[:-6], AMBER, 2)
    b.dashed(route[-7:], AMBER, 1, 3, 4)
    b.arrow(*route[-3], *route[-1], AMBER, size=5, dashed=False)
    sx, sy = cgeo(-102, 3)
    b.sprite(scaled_sprite("ship_2", 0.5), sx, sy - 8)
    b.sprite(scaled_sprite("rook_2", 0.5), sx + 14, sy)
    b.label(sx + 26, sy - 8, "LANCER, ROOK", CYAN)
    for n, (lo, la, dx, dy) in enumerate(((-100, -6, -4, -16), (-91, -8, 0, 12), (-80, -9, 0, -18), (-67, -11, 0, 12),
                                          (-55, -12, 0, 12), (-43, -9, 10, 4)), 1):
        x, y = cgeo(lo, la)
        x, y = x + dx, y + dy
        b.draw.rectangle([x - 4, y - 5, x + 4, y + 5], fill=(10, 14, 30, 230), outline=AMBER + (255,))
        b.label(x - 2, y - 3, str(n), AMBER)
    # the places
    nx, ny = cgeo(-104, -13)
    b.label(nx, ny, "NOCTIS LABYRINTHUS", CYAN_DIM)
    fx, fy = cgeo(-82, -4.5)
    b.label(fx - 10, fy - 4, "THE FORK", CYAN)
    cx_, cy_ = cgeo(-74, -14.0)
    b.label(cx_, cy_, "COPRATES", CYAN_DIM)
    ex, ey = cgeo(-45, -14.0)
    b.label(ex - 6, ey, "EOS CHAOS", CYAN_DIM)
    hx, hy = cgeo(*HELIX)
    b.draw.rectangle([hx - 2, hy - 2, hx + 2, hy + 2], fill=(0, 0, 0, 255), outline=(30, 40, 52, 255))
    ax, ay = cgeo(-31, 16)
    for k, r in enumerate((4, 8, 12)):
        b.ring(ax, ay, r, colour=AMBER, alpha=230 - 60 * k)
    b.glow(ax, ay, 7, (200, 120, 30), 0.9)
    b.marker(ax, ay, "diamond", AMBER, 3)
    b.label(ax - 78, ay - 4, "ARES LANDING", AMBER)
    # the legend: the level's sections
    px = 474
    b.line([(px - 8, 26), (px - 8, H - 12)], CYAN_DIM, 1, 90)
    b.label(px, 30, "THE CANYON, WEST TO EAST", WHITE)
    rows = [("1", "NOCTIS EDGE"), ("2", "INTO THE CANYON"), ("3", "FORK ONE"), ("4", "COPRATES RAPIDS"),
            ("5", "THE NARROWS"), ("6", "EOS OUTFLOW")]
    for i, (n, name) in enumerate(rows):
        y = 46 + 13 * i
        b.label(px, y, n, AMBER)
        b.label(px + 12, y, name, CYAN)
    b.line([(px, 128), (W - 12, 128)], CYAN_DIM, 1, 120)
    b.label(px, 136, "2,000 KM TO ARES LANDING", AMBER)
    b.label(px, 150, "WALLS ON BOTH SIDES", WHITE)
    b.label(px, 160, "KEEP OFF THE ROCK", RED)
    b.label(px, 174, "ROOK: ON LANCER'S TAIL", CYAN_DIM)
    b.label(px, 184, "IN THE NARROWS", CYAN_DIM)
    b.label(W - 10, H - 16, "FLY IT FAST", CYAN_DIM, right=True)
    return b


def tendril_scan():
    """L16 p2: Varga's scan of a cut through the canyon from above: the two walls, an anchor pair at their
    foot with the tendril between them, an Ion Beam burning through the tendril (white-hot where it crosses),
    a second pair broken by killing an anchor (the rope withered back), and a Threadcrawler at the right
    wall's foot with its pores glowing; the `beam` marker."""
    b = Board()
    wl = 150 + 10 * np.sin(b.yy[:, 0] / 40)                       # the walls' foot (x per row)
    wr = 470 + 8 * np.sin(b.yy[:, 0] / 33 + 1)
    tex = raster.fbm(W, H, 12, 3881, octaves=3, period=False)
    rock_l = (b.xx < wl[:, None]) & (b.xx > 40)
    rock_r = (b.xx > wr[:, None]) & (b.xx < 600)
    band = 0.45 + 0.35 * np.sin(b.xx / 5 + 3 * tex)
    rock = raster.ramp(MARS_HOLO, np.clip(band * 0.7 + 0.15 * tex, 0, 1))
    b.base = np.where((rock_l | rock_r)[..., None], rock, b.base)
    floor = raster.ramp(MARS_HOLO, np.clip(0.3 + 0.12 * tex, 0, 1))
    b.base = np.where((~(rock_l | rock_r) & (b.xx >= 40) & (b.xx < 600))[..., None], floor, b.base)
    b.line([(wl[y], y) for y in range(0, H, 2)], CYAN, 1, 230)
    b.line([(wr[y], y) for y in range(0, H, 2)], CYAN, 1, 230)
    b.title("SENSOR SCAN - TENDRIL ANCHOR - A GATE ACROSS THE CANYON")
    b.label(W - 10, 9, "BURN IT OR KILL AN ANCHOR", AMBER, right=True)
    b.label(46, 26, "WALL", CYAN_DIM)
    b.label(wr[26] + 8, 26, "WALL", CYAN_DIM)
    tendril = np.array(held_asset("tendril_1")).astype(np.float64)
    wither = np.array(held_asset("tendril-wither_3")).astype(np.float64)

    def rope(strip, ax, bx, y, sprites):
        h, w = strip.shape[:2]
        img = np.zeros((h, int(bx - ax), 4))
        for k in range(img.shape[1]):
            img[:, k] = strip[:, k % w]
        sprites.append((Image.fromarray(img.astype(np.uint8), "RGBA"), (ax + bx) / 2, y))

    # gate one: standing, the beam burning through it
    y1 = 70
    ax_, bx_ = wl[y1] + 14, wr[y1] - 14
    parts = []
    rope(tendril, ax_ + 12, bx_ - 12, y1, parts)
    for img, x, y in parts:
        b.sprite(img, x, y)
    b.sprite(asset("tendril-anchor_72"), ax_, y1)
    b.sprite(asset("tendril-anchor_24"), bx_, y1)
    bx_beam = 300
    col = np.array(asset("ion-beam-column-3_0")).astype(np.float64)
    beam = np.vstack([col] * 3)[: H - y1 - 40]
    light_at(b, beam[..., :3] * beam[..., 3:4] / 255, bx_beam, y1 + beam.shape[0] / 2 + 2)
    burn = np.array(held_asset("tendril-burn_1")).astype(np.float64)
    light_at(b, burn[..., :3] * burn[..., 3:4] / 255 * 1.2, bx_beam, y1)
    b.sprite(scaled_sprite("ship_2", 0.75), bx_beam, H - 20)
    b.label(bx_beam + 26, y1 + 18, "A BEAM BURNS", WHITE)
    b.label(bx_beam + 26, y1 + 28, "THROUGH THE TENDRIL", WHITE)
    b.label(ax_ - 4, y1 - 30, "ANCHOR", RED)
    b.line([(ax_, y1 - 20), (ax_, y1 - 12)], RED, 1)
    b.label(ax_ + 70, y1 - 22, "TENDRIL: IT BURNS ON TOUCH", RED)
    # gate two: an anchor killed, the rope withered back into the other
    y2 = 150
    ax2, bx2 = wl[y2] + 14, wr[y2] - 14
    parts = []
    rope(wither, bx2 - 80, bx2 - 12, y2 + 2, parts)
    for img, x, y in parts:
        b.sprite(img, x, y)
    b.marker(ax2, y2, "cross", AMBER, 6)
    b.sprite(asset("tendril-anchor_24"), bx2, y2)
    b.label(ax2 + 16, y2 - 4, "KILL EITHER ANCHOR:", AMBER)
    b.label(ax2 + 16, y2 + 6, "THE GATE FALLS", AMBER)
    # the Threadcrawler at the right wall's foot, crawling up
    seg, head, tail = "threadcrawler-segment", "threadcrawler-head", "threadcrawler-tail"
    pts = [(wr[y] - 22, y) for y in range(178, 250, 15)]
    pore = np.array(asset("threadcrawler-pore_2")).astype(np.float64)
    for i, (x, y) in reversed(list(enumerate(pts))):
        name = f"{head}_{8 * 4}" if i == 0 else f"{tail}_8" if i == len(pts) - 1 else f"{seg}_{8 * 4 + (i % 4)}"
        b.sprite(scaled_sprite(name, 0.62), x, y)
        if i in (3, 4):
            light_at(b, pore[..., :3] * pore[..., 3:4] / 255 * 0.8, x, y)
    b.label(wr[200] - 150, 196, "SOMETHING LONG", GREEN)
    b.label(wr[200] - 150, 206, "CRAWLS AT THE FOOT", GREEN)
    # what to bring
    gx, gy = 70, 200
    b.draw.rectangle([gx - 4, gy - 6, gx + 58, gy + 16], outline=GREEN + (240,))
    b.label(gx + 4, gy, "BEAM", GREEN)
    b.arrow(gx + 58, gy + 4, bx_beam - 22, y1 + 40, GREEN, size=4)
    return b

# =========================================================================== Level 17 (M7 part D)

SOURCE_M7D = artkit.source_note(SCRIPT, "M7 part D batch")


def held_frame(name, scale=1.0, colours=48):
    """A frame from assets/ or build/art-held/ at ``scale`` (box-filtered, 1-bit alpha), palettised."""
    img = held_asset(name)
    if scale != 1.0:
        img = img.resize((max(1, round(img.width * scale)), max(1, round(img.height * scale))), Image.BOX)
    a = np.array(img).astype(np.float64)
    a[..., 3] = np.where(a[..., 3] >= 128, 255, 0)
    return artkit.quantize_set([Image.fromarray(a.astype(np.uint8), "RGBA")], colours)[0]


def colony_y(fx):
    """The colony plan's y of a play-field x (north is to the left: west at the bottom, east at the top)."""
    return 20 + (480 - fx) * 0.42


def ares_domes():
    """L17 p1: Okafor's plan of Ares Landing in the CDF map style, north to the left: the rail line from the
    north, the outer domes Bradbury and Lowell, the burnt outer greenhouses, the inner ring road round
    Schiaparelli, Burroughs and Eden (the largest), the spaceport to the south; red Vrell vectors converging
    on the domes; the level's five sections along the route. East of the burnt greenhouses a small black
    compound with a gold edge that the plan does not name (Helix's)."""
    b = Board()
    x0, x1 = 16, 468
    box = (b.xx >= x0) & (b.xx < x1) & (b.yy >= 18) & (b.yy < H - 10)
    tex = raster.fbm(W, H, 24, 3901, octaves=4, period=False)
    fine = raster.fbm(W, H, 6, 3903, octaves=2, period=False)
    body = raster.ramp(MARS_HOLO, np.clip(0.42 + 0.3 * (tex - 0.5) + 0.12 * (fine - 0.5), 0, 1))
    b.base = np.where(box[..., None], body, b.base)
    domes = [("BRADBURY", 150, 130, 11), ("LOWELL", 176, 350, 11), ("SCHIAPARELLI", 318, 110, 11),
             ("BURROUGHS", 344, 370, 11), ("EDEN", 380, 240, 15)]
    growth = np.zeros((H, W))
    for name, mx, fx, r in domes:                                   # Vrell growth creeping toward the rings
        my = colony_y(fx)
        growth += np.exp(-(((b.xx - mx) ** 2 + (b.yy - my) ** 2) / (28 + 2 * r) ** 2)) * (tex > 0.52)
    gm = (growth > 0.3) & box
    b.base = np.where(gm[..., None], b.base * 0.55 + np.array((70, 30, 90)) * 0.45, b.base)
    b.title("ARES LANDING - THE FIVE DOMES")
    b.label(W - 10, 9, "KEEP THE BOMBERS OFF THE GLASS", AMBER, right=True)
    b.draw.rectangle([x0, 18, x1 - 1, H - 11], outline=CYAN_DIM + (180,))
    b.arrow(46, 30, 26, 30, CYAN, size=3, dashed=False)
    b.label(50, 27, "N", CYAN)
    # the rail line from the north, into the yard
    rail = [(x0, colony_y(310)), (90, colony_y(310)), (128, colony_y(240)), (200, colony_y(240)), (232, colony_y(160)),
            (268, colony_y(160))]
    b.line(rail, GREY, 3, 200)
    b.dashed(rail, WHITE, 1, 2, 3)
    b.label(22, colony_y(310) - 12, "RAIL", CYAN_DIM)
    # the burnt outer greenhouses (west) and the unnamed compound (east)
    for k in range(5):
        gx, gy = 214 + 11 * k, colony_y(120) - 6 + (k % 2) * 8
        b.draw.rectangle([gx, gy, gx + 7, gy + 16], fill=(40, 34, 34, 230), outline=(90, 80, 76, 255))
    b.label(204, colony_y(70) + 2, "BURNT", GREY)
    b.label(204, colony_y(70) + 12, "GREENHOUSES", GREY)
    hx, hy = 246, colony_y(390)
    b.draw.rectangle([hx - 7, hy - 6, hx + 7, hy + 6], fill=(0, 0, 0, 255), outline=(200, 160, 60, 255))
    b.draw.rectangle([hx - 3, hy - 2, hx + 3, hy + 2], fill=(220, 180, 80, 255))
    # the inner ring road
    b.ring(350, colony_y(240), 62, 78, colour=GREY, alpha=170, width=2)
    b.label(300, 22, "INNER RING", CYAN_DIM)
    # the spaceport
    for px, py in ((420, colony_y(300)), (446, colony_y(180)), (432, colony_y(380))):
        b.ring(px, py, 7, colour=WHITE, alpha=200)
        b.marker(px, py, "dot", WHITE, 1)
    b.draw.rectangle([450, colony_y(420) - 6, 462, colony_y(420) + 8], outline=WHITE + (180,))
    b.label(408, H - 30, "SPACEPORT", AMBER)
    # the domes: the ally's sprite small, lit green, named
    for name, mx, fx, r in domes:
        my = colony_y(fx)
        b.glow(mx, my, r * 1.6, (20, 120, 50), 0.9)
        sp = held_frame("ares-dome-eden_1" if name == "EDEN" else "ares-dome_1", (2 * r + 6) / (144 if name == "EDEN" else 120))
        b.sprite(sp, mx, my)
        ly = my + r + 4 if fx > 240 or name == "EDEN" else my - r - 12
        tag(b, mx - raster.text_width(name) / 2, ly, name, GREEN)
    # Vrell vectors converging on the domes
    for (sx, sy), (name, mx, fx, r) in zip(((120, 10), (230, 228), (300, 232), (300, 10), (412, 10)), domes):
        my = colony_y(fx)
        d = np.array([mx - sx, my - sy], float)
        d /= np.linalg.norm(d)
        b.arrow(sx, sy, mx - d[0] * (r + 6), my - d[1] * (r + 6), RED, size=3)
    # the route along the colony, the sections numbered
    route = [(x0 + 4, 121), (468, 121)]
    b.dashed(route, AMBER, 1, 3, 4)
    for n, x in enumerate((64, 150, 246, 340, 430), 1):
        b.draw.rectangle([x - 4, 228 - 5, x + 4, 228 + 5], fill=(10, 14, 30, 230), outline=AMBER + (255,))
        b.label(x - 2, 228 - 3, str(n), AMBER)
    sx, sy = 30, 121
    b.sprite(scaled_sprite("ship_2", 0.5), sx, sy)
    # the legend
    px = 480
    b.line([(px - 6, 26), (px - 6, H - 12)], CYAN_DIM, 1, 90)
    b.label(px, 30, "NORTH TO SOUTH", WHITE)
    rows = [("1", "RAIL APPROACH"), ("2", "OUTER DOMES"), ("3", "BURNT GREENHOUSES"), ("4", "INNER RING"), ("5", "SPACEPORT")]
    for i, (n, name) in enumerate(rows):
        y = 46 + 13 * i
        b.label(px, y, n, AMBER)
        b.label(px + 12, y, name, CYAN)
    b.line([(px, 116), (W - 12, 116)], CYAN_DIM, 1, 120)
    b.label(px, 124, "REACH THE SPACEPORT", AMBER)
    b.label(px, 138, "NO DOME HIT", GREEN)
    b.label(px, 152, "BOMBERS GO FOR THE GLASS", RED)
    b.label(px, 166, "THE PEOPLE ARE", CYAN_DIM)
    b.label(px, 176, "BELOW GROUND", CYAN_DIM)
    b.label(W - 10, H - 16, "MILITIA GUNS: NOT YET", CYAN_DIM, right=True)
    return b


def herald_scan():
    """L17 p2: Varga's scan of a Choir Herald (its crest lit) with the dashed circle of its aura and thin threads
    to three ringed escorts; an inset of a Shellback mid-curl with its lime vent marked."""
    b = Board()
    tex = raster.fbm(W, H, 16, 3911, octaves=4, period=False)
    inset = (b.xx >= 430) & (b.xx < W - 12) & (b.yy >= 30) & (b.yy < H - 14)
    ground = raster.ramp(MARS_HOLO, np.clip(0.4 + 0.25 * (tex - 0.5), 0, 1))
    b.base = np.where(inset[..., None], ground, b.base)
    b.title("SENSOR SCAN - CHOIR HERALD - IT SINGS SHIELDS")
    b.label(W - 10, 9, "KILL THE SINGER, THEN THE REST", AMBER, right=True)
    hx, hy, r = 200, 124, 84
    aura = np.array(asset("choir-herald-aura")).astype(np.float64)
    aura = np.array(Image.fromarray(aura.astype(np.uint8), "RGBA").resize((2 * r + 4, 2 * r + 4), Image.BOX)).astype(np.float64)
    light_at(b, aura[..., :3] * aura[..., 3:4] / 255 * 1.6, hx, hy, "under")
    b.sprite(asset("choir-herald_2", 2), hx, hy)
    crest = np.array(asset("choir-herald-crest_2", 2)).astype(np.float64)
    light_at(b, crest[..., :3] * crest[..., 3:4] / 255, hx, hy)
    ring = np.array(asset("choir-herald-ring_6").resize((54, 54), Image.NEAREST)).astype(np.float64)
    ring3 = np.array(asset("choir-herald-ring_2").resize((54, 54), Image.NEAREST)).astype(np.float64)
    for k, a in enumerate((-2.4, -0.3, 1.6)):
        ex, ey = hx + 68 * math.cos(a), hy + 62 * math.sin(a)
        b.dashed([(hx + 30 * math.cos(a), hy + 30 * math.sin(a)), (ex - 20 * math.cos(a), ey - 20 * math.sin(a))],
                 VIOLET, 1, 2, 2)
        b.sprite(asset(f"needler_{k}"), ex, ey)
        rr = ring3 if k == 1 else ring
        light_at(b, rr[..., :3] * rr[..., 3:4] / 255, ex, ey)
    b.line([(hx + 44, 44), (hx + 30, hy - 30)], AMBER, 1)
    b.label(hx + 30, 34, "THE CREST: THE SINGER", AMBER)
    b.label(20, 30, "EVERY UNIT IN ITS REACH", VIOLET)
    b.label(20, 40, "WEARS A SHIELD RING", VIOLET)
    b.label(20, H - 40, "A RING SOAKS UP SHOTS,", WHITE)
    b.label(20, H - 30, "THEN REFILLS", WHITE)
    b.label(hx + 94, hy - 22, "SEGMENTS DROP", VIOLET)
    b.label(hx + 94, hy - 12, "AS IT DRAINS", VIOLET)
    b.label(296, H - 64, "SINGER DOWN:", AMBER)
    b.label(296, H - 54, "EVERY RING COLLAPSES", AMBER)
    b.label(296, H - 30, "EMP STRIPS THEM", GREEN)
    # the inset: a Shellback curling, its vent marked
    b.draw.rectangle([430, 30, W - 13, H - 15], outline=CYAN_DIM + (200,))
    b.label(436, 36, "SHELLBACK", WHITE)
    sx, sy = 540, 128
    b.sprite(asset("shellback-curl_1"), sx, sy)
    vent = json.loads((ROOT / "assets" / "pivots" / "shellback.json").read_text(encoding="utf-8"))["vent"][0]
    vx, vy = sx - 56 + vent[0], sy - 56 + vent[1]
    glow = np.array(asset("shellback-vent_2")).astype(np.float64)
    light_at(b, glow[..., :3] * glow[..., 3:4] / 255, vx, vy)
    b.ring(vx, vy, 12, colour=LIME, alpha=240)
    b.line([(vx + 12, vy - 6), (608, 64)], LIME, 1)
    b.label(578, 52, "VENT", LIME)
    b.label(436, H - 50, "CURLED: SHOTS", RED)
    b.label(436, H - 40, "SPARK OFF. WAIT", RED)
    b.label(436, H - 28, "THE BELLY AFTER", GREEN)
    b.arrow(sx + 44, sy + 40, sx + 70, sy + 72, AMBER, size=3)
    return b


L15 = ROOT / "assets" / "backdrop" / "level-15"


def l15_layer(name, turn=True, scale=1.0):
    """A Level 15 backdrop image (tools/art/backdrop_l15.py) as a float RGBA array, turned so its
    flight direction points right, at ``scale``."""
    img = Image.open(L15 / f"{name}.png").convert("RGBA")
    if turn:
        img = img.rotate(-90, expand=True)
    if scale != 1.0:
        img = img.resize((round(img.width * scale), round(img.height * scale)), Image.LANCZOS)
    return np.array(img).astype(np.float64)


def tiled(a, w, h, dx=0, dy=0):
    """A wrapping tile array repeated over w x h, shifted by (dx, dy)."""
    ys = (np.arange(h) + dy) % a.shape[0]
    xs = (np.arange(w) + dx) % a.shape[1]
    return a[np.ix_(ys, xs)]


def drop():
    """Act 3 p4: the transport's drop bay open over Mars (Level 15's production `transport` piece,
    turned nose right, the planet showing through the open bay), Aegis Wing on the launch rails in it,
    shown again in a callout; below, Mars from high up as Level 15 draws it (`mars-high` with the
    `olympus` piece), the cloud deck's banks far below it (`deck-banks`) and the thin cirrus."""
    b = Board()
    ground = tiled(l15_layer("mars-high", scale=0.5), W, H, 40, 30)[..., :3]
    oly = l15_layer("olympus", scale=0.5)
    ox, oy = 232, 20
    sl = ground[oy:oy + oly.shape[0], ox:ox + oly.shape[1]]
    part = oly[:sl.shape[0], :sl.shape[1]]
    sl[:] = part[..., :3] * part[..., 3:4] / 255 + sl * (1 - part[..., 3:4] / 255)
    rgb = ground * 0.8 * (1 - 0.22) + np.array(BUTTERSCOTCH) * 0.22        # far below: the haze over it
    deck = tiled(l15_layer("deck-banks", scale=0.55), W, H, 260, 90)
    a = deck[..., 3:4] / 255 * np.clip((300 - b.xx) / 120, 0, 0.9)[..., None]     # the banks clear of the volcano
    rgb = deck[..., :3] * a + rgb * (1 - a)
    cirrus = tiled(l15_layer("cirrus", scale=0.9), W, H, 0, 40)
    rgb = rgb + cirrus[..., :3] * cirrus[..., 3:4] / 255 * 0.4                 # additive at 40 %, as the game
    b.base = rgb * 0.7
    b.title("CDF TRANSPORT - DROP BAY OPEN")
    b.label(W - 10, 9, "DROP IN 90 MINUTES", AMBER, right=True)
    # the transport, nose right, its bay open over the planet
    tsp = Image.open(L15 / "transport.png").convert("RGBA").rotate(-90, expand=True)
    k = 0.42
    tsp = scaled(tsp, k, 48)
    tx, ty = 170, 126
    shadow(b, tsp, tx, ty, 10, 14, 0.35)
    b.sprite(tsp, tx, ty)
    bay = (tx - tsp.width / 2 + (659 - 260) * k, ty - tsp.height / 2 + 200 * k)   # the bay's centre
    bw, bh = 62 * k, 40 * k
    lineup = [("ship_2", -12, -8), ("rook", -12, 8), ("ship_2", 12, -8), ("ship_2", 12, 8)]
    for kind, dx, dy in lineup:
        img = rook_craft() if kind == "rook" else asset("ship_2")
        b.sprite(turned(scaled(img, 0.3), 90), bay[0] + dx, bay[1] + dy)
    b.bracket(bay[0] - bw - 3, bay[1] - bh - 3, bay[0] + bw + 3, bay[1] + bh + 3, AMBER, 5)
    # the callout: the rails at full size
    cx0, cy0, cx1, cy1 = 452, 28, 662, 214
    fill(b, [(cx0, cy0), (cx1, cy0), (cx1, cy1), (cx0, cy1)], (6, 10, 24), 0.92)
    b.draw.rectangle([cx0, cy0, cx1, cy1], outline=CYAN_DIM + (220,))
    b.dashed([(bay[0] + bw + 3, bay[1] - bh - 3), (cx0, cy0)], AMBER, 1, 3, 3)
    b.dashed([(bay[0] + bw + 3, bay[1] + bh + 3), (cx0, cy1)], AMBER, 1, 3, 3)
    b.label(cx0 + 8, cy0 + 6, "DROP BAY - LAUNCH RAILS", WHITE)
    rails = [(84, [("LANCER", "ship_2", CYAN), ("AEGIS TWO", "ship_2", GREY), ("AEGIS 4", "ship_2", GREY)]),
             (150, [("ROOK", "rook", AMBER), ("AEGIS 3", "ship_2", GREY)])]
    for ry, crew in rails:
        b.line([(cx0 + 10, ry), (cx1 - 10, ry)], CYAN_DIM, 2, 200)
        b.line([(cx0 + 10, ry - 4), (cx0 + 10, ry + 4)], CYAN_DIM, 1, 200)
        for i, (name, kind, colour) in enumerate(crew):
            x = cx1 - 36 - 64 * i
            img = rook_craft() if kind == "rook" else asset("ship_2")
            b.sprite(turned(img, 90), x, ry)
            b.label(x - raster.text_width(name) // 2, ry + 26, name, colour)
        b.arrow(cx1 - 22, ry - 22, cx1 - 6, ry - 22, AMBER, size=3, dashed=False)
    b.label(cx0 + 8, cy1 - 14, "AEGIS ACTUAL OUT", CYAN_DIM)
    # what lies below
    vx, vy = ox + oly.shape[1] / 2, oy + oly.shape[0] / 2
    b.ring(vx, vy, 20, colour=CYAN, alpha=170)
    tag(b, vx - 50, vy + 64, "OLYMPUS MONS", CYAN)
    tag(b, vx - 50, vy + 74, "FAR BELOW", CYAN_DIM)
    tag(b, 14, 26, "WE GO IN FROM THE WEST", WHITE)
    tag(b, 14, 36, "PAST OLYMPUS MONS", AMBER)
    tag(b, 14, 214, "THE CLOUD DECK", WHITE)
    return b



# --------------------------------------------------------------------------- Level 18 (M7 part E)

SOURCE_M7E = artkit.source_note(SCRIPT, "M7 part E batch")
L18 = ROOT / "assets" / "backdrop" / "level-18"
DUST_HOLO = [(0.0, (60, 30, 18)), (0.4, (132, 78, 44)), (0.75, (196, 140, 90)), (1.0, (236, 196, 146))]


def storm_front_l18():
    """L18 p1: Okafor's orbital weather view of the northern plains, north to the left: the storm front as a
    rust wall bowed toward the south with the calm eye inside, Vrell markers in it, its drift toward Ares
    Landing's domes at its southern edge; the sweep route through the storm with the six sections."""
    b = Board()
    x0, x1 = 16, 468
    box = (b.xx >= x0) & (b.xx < x1) & (b.yy >= 18) & (b.yy < H - 10)
    tex = raster.fbm(W, H, 24, 4601, octaves=4, period=False)
    fine = raster.fbm(W, H, 6, 4603, octaves=2, period=False)
    ground = raster.ramp(MARS_HOLO, np.clip(0.4 + 0.3 * (tex - 0.5) + 0.12 * (fine - 0.5), 0, 1))
    b.base = np.where(box[..., None], ground, b.base)
    # the storm: a swirl of dust (north-west of the domes), its front bowed toward them, the calm eye within
    sx, sy, ex, ey = 236, 118, 250, 112
    ang = np.arctan2(b.yy - sy, (b.xx - sx) * 0.62)
    r = np.hypot((b.xx - sx) * 0.62, b.yy - sy) / 100
    swirl = raster.fbm(W, H, 18, 4605, octaves=4, period=False)
    arms = 0.5 + 0.5 * np.sin(3 * ang + 7 * r + 3 * (swirl - 0.5))
    front = (b.xx - (sx + 128 - 30 * ((b.yy - sy) / 90) ** 2)) / 14
    dens = np.clip((1.25 - r) * 1.6, 0, 1) * np.clip(-front, 0, 1) * (0.55 + 0.45 * arms) + np.clip(1 - np.abs(front), 0, 1) * 0.5 * (r < 1.25)
    eye = np.hypot(b.xx - ex, (b.yy - ey) * 1.15)
    calm = np.clip((eye - 16) / 10, 0, 1)
    dens = np.clip(dens * calm + 0.25 * (swirl - 0.5), 0, 1) * box
    dust = raster.ramp(DUST_HOLO, np.clip(0.3 + 0.7 * dens + 0.15 * (fine - 0.5), 0, 1))
    b.base = b.base * (1 - dens[..., None] * 0.9) + dust * dens[..., None] * 0.9
    b.title("NORTHERN PLAINS - A STORM FRONT WITH VRELL IN IT")
    b.label(W - 10, 9, "SWEEP IT BEFORE IT REACHES THE DOMES", AMBER, right=True)
    b.draw.rectangle([x0, 18, x1 - 1, H - 11], outline=CYAN_DIM + (180,))
    b.arrow(46, 30, 26, 30, CYAN, size=3, dashed=False)
    b.label(50, 27, "N", CYAN)
    b.ring(ex, ey, 18, 16, colour=CYAN, alpha=200)
    tag(b, ex - 10, ey - 4, "EYE", CYAN)
    tag(b, sx - 150, 32, "STORM FRONT", AMBER)
    b.line([(sx - 100, 42), (sx - 40, 70)], AMBER, 1)
    for k in range(3):                                           # its drift toward the domes
        b.arrow(sx + 140, 70 + 50 * k, sx + 178, 74 + 50 * k, AMBER, size=3)
    tag(b, sx + 134, 30, "MOVING SOUTH", AMBER)
    rng = np.random.default_rng(4607)                            # Vrell inside the dust
    for _ in range(9):
        a, rr = rng.uniform(0, 6.28), rng.uniform(30, 92)
        vx, vy = sx + math.cos(a) * rr / 0.62, sy + math.sin(a) * rr * 0.8
        if math.hypot(vx - ex, vy - ey) > 26 and x0 + 8 < vx < sx + 120:
            b.marker(vx, vy, "chevron", RED, 4)
    # Ares Landing's domes at the storm's southern edge (the ally's sprite, small)
    for k, (dx, dy) in enumerate(((432, 70), (446, 112), (430, 160), (452, 186))):
        b.glow(dx, dy, 12, (20, 120, 50), 0.8)
        b.sprite(held_frame("ares-dome_1", 16 / 120), dx, dy)
    tag(b, 392, 200, "ARES LANDING", GREEN)
    # the sweep route north to south through the storm, the sections numbered
    route = [(x0 + 6, 121), (x1 - 52, 121)]
    b.dashed(route, AMBER, 1, 3, 4)
    for n, x in enumerate((44, 112, 190, 252, 316, 384), 1):
        b.draw.rectangle([x - 4, 228 - 5, x + 4, 228 + 5], fill=(10, 14, 30, 230), outline=AMBER + (255,))
        b.label(x - 2, 228 - 3, str(n), AMBER)
    b.sprite(scaled_sprite("ship_2", 0.5), 30, 121)
    px = 480
    b.line([(px - 6, 26), (px - 6, H - 12)], CYAN_DIM, 1, 90)
    b.label(px, 30, "NORTH TO SOUTH", WHITE)
    rows = [("1", "OPEN PLAINS"), ("2", "STORM FRONT"), ("3", "DEEP STORM"), ("4", "THE EYE"), ("5", "BACK WALL"),
            ("6", "CLEARING")]
    for i, (n, name) in enumerate(rows):
        y = 46 + 13 * i
        b.label(px, y, n, AMBER)
        b.label(px + 12, y, name, CYAN)
    b.line([(px, 128), (W - 12, 128)], CYAN_DIM, 1, 120)
    b.label(px, 136, "SWEEP THE STORM", AMBER)
    b.label(px, 150, "KILL RATIO 65 %", GREEN)
    b.label(px, 164, "INSIDE: A FEW", CYAN_DIM)
    b.label(px, 174, "SHIP-LENGTHS OF VIEW", CYAN_DIM)
    b.label(W - 10, H - 16, "CLEAR AIR IN THE EYE", CYAN_DIM, right=True)
    return b


def _l18(name):
    return np.array(Image.open(L18 / f"{name}.png").convert("RGBA")).astype(np.float64)


def visibility_l18():
    """L18 p2: Varga's diagram of the storm's look (radius look b), from the level's own textures: the ship in
    its clear radius, the plain fading into the rust veil through the ragged falloff, Dust Kites and a
    Needler outside as dark silhouettes that keep their teal glow, their orbs crisp at any range, a homing
    trail curving onto one; the radius and the falloff marked."""
    import mars_r36 as m36
    b = Board()
    x1 = 456
    yy, xx = b.yy, b.xx
    ground = _l18("storm-plain")[..., :3]
    veil = _l18("storm-veil")[..., :3]
    ta, tb = _l18("storm-tongues-a")[..., 3] / 255, _l18("storm-tongues-b")[..., 3] / 255
    g = ground[(yy.astype(int) + 300) % ground.shape[0], xx.astype(int) % ground.shape[1]]
    v_ = veil[(yy.astype(int) + 100) % veil.shape[0], xx.astype(int) % veil.shape[1]]
    na = ta[yy.astype(int) % ta.shape[0], xx.astype(int) % ta.shape[1]]
    nb = tb[(yy.astype(int) + 90) % tb.shape[0], (xx.astype(int) + 40) % tb.shape[1]]
    sx, sy, R = 196, 150, 74
    d = np.hypot(xx - sx, yy - sy)
    from render import sdf
    vis = sdf.smoothstep(R + 30, R - 36, d + 40 * (na - 0.5) + 24 * (nb - 0.5))
    field = g * 0.85 + np.array((176, 118, 74)) * 0.15
    img = field * vis[..., None] + v_ * (1 - vis[..., None])
    box = (xx >= 16) & (xx < x1) & (yy >= 18) & (yy < H - 10)
    b.base = np.where(box[..., None], img * 0.92, b.base)
    b.title("SENSOR SCAN - THE DUST STORM: GLOW, NOT SHAPES")
    b.label(W - 10, 9, "BULLETS ALWAYS SHOW", AMBER, right=True)
    b.draw.rectangle([16, 18, x1 - 1, H - 11], outline=CYAN_DIM + (180,))
    b.ring(sx, sy, R, colour=WHITE, alpha=150)
    b.ring(sx, sy, R + 30, colour=CYAN_DIM, alpha=110)
    b.line([(sx, sy), (sx + R * 0.71, sy - R * 0.71)], WHITE, 1, 200)
    tag(b, sx + R * 0.71 + 4, sy - R * 0.71 - 12, "CLEAR RADIUS", WHITE)
    tag(b, sx + R + 34, sy + 8, "RAGGED EDGE", CYAN_DIM)
    b.sprite(scaled_sprite("ship_2", 1.0), sx, sy)
    for k in range(3):                                           # the ship's bolts up the screen
        b.draw.rectangle([sx - 11, sy - 40 - 26 * k, sx - 9, sy - 30 - 26 * k], fill=(120, 220, 255, 255))
        b.draw.rectangle([sx + 9, sy - 40 - 26 * k, sx + 11, sy - 30 - 26 * k], fill=(120, 220, 255, 255))
    # Vrell outside the radius: dark silhouettes keeping their glow (mars_r36.silhouette, the look's rule)
    kite, needler = asset("dust-kite_0"), asset("needler_0")
    units = [(kite, 70, 52), (kite, 104, 40), (kite, 330, 60), (needler, 372, 112), (kite, 64, 200)]
    for sp, ux, uy in units:
        b.sprite(m36.silhouette(sp), ux, uy)
    orb = asset("orb_0")
    for ox, oy in ((96, 76), (118, 98), (346, 92), (362, 132), (300, 150)):  # their orbs: crisp at any range
        b.sprite(orb, ox, oy)
    # a homing trail curving out of the radius onto one silhouette
    pts = [(sx + 14, sy - 20), (sx + 60, sy - 70), (sx + 120, sy - 92), (330, 66)]
    b.dashed(pts, GREEN, 1, 3, 3)
    tag(b, 236, 24, "HOMING FINDS THEM", GREEN)
    tag(b, 24, 24, "VRELL GLOW", CYAN)
    tag(b, 24, H - 26, "THE GROUND IS GONE", AMBER)
    tag(b, 324, H - 26, "BULLETS: FULL CONTRAST", AMBER)
    px = 466
    b.line([(px - 6, 26), (px - 6, H - 12)], CYAN_DIM, 1, 90)
    b.label(px, 30, "IN THE DUST", WHITE)
    b.label(px, 46, "INSIDE: ALL OF IT", CYAN)
    b.label(px, 60, "OUTSIDE: GLOW ONLY", CYAN)
    b.label(px, 74, "GROUND: GONE", CYAN)
    b.label(px, 88, "BULLETS: ALWAYS", AMBER)
    b.line([(px, 104), (W - 12, 104)], CYAN_DIM, 1, 120)
    b.label(px, 112, "EVERYTHING IS HITTABLE", GREEN)
    b.label(px, 126, "HOMING LOCKS WHAT", GREEN)
    b.label(px, 136, "YOUR EYES CAN'T", GREEN)
    b.label(px, 156, "DEEP STORM 170", CYAN_DIM)
    b.label(px, 166, "BACK WALL 150", CYAN_DIM)
    b.label(px, 180, "THE EYE: CLEAR", CYAN_DIM)
    return b

SOURCE_M7F = artkit.source_note(SCRIPT, "M7 part F batch")
L19 = ROOT / "assets" / "backdrop" / "level-19"
THERMAL = [(0.0, (8, 4, 20)), (0.3, (70, 10, 60)), (0.55, (200, 40, 20)), (0.8, (255, 160, 40)), (1.0, (255, 250, 210))]


def _l19(name):
    return Image.open(L19 / f"{name}.png").convert("RGBA")


def tharsis_works():
    """L19 p1: Okafor's CDF site plan of the Tharsis Foundry, north to the left: the slag fields, the two
    conveyor halls with their belts, the furnace row between the smelter stacks, the yard approach and its gate,
    the casting yard and the south gate; the smelter tower marked with its crew count; red Vrell vectors into
    the works and grey, unlabelled contacts on the conveyor lines; the level's sections along the route."""
    b = Board()
    x0, x1 = 16, 468
    box = (b.xx >= x0) & (b.xx < x1) & (b.yy >= 18) & (b.yy < H - 10)
    tex = raster.fbm(W, H, 24, 4901, octaves=4, period=False)
    fine = raster.fbm(W, H, 6, 4903, octaves=2, period=False)
    ground = raster.ramp(MARS_HOLO, np.clip(0.36 + 0.28 * (tex - 0.5) + 0.12 * (fine - 0.5), 0, 1))
    b.base = np.where(box[..., None], ground, b.base)
    # slag in the fields (north and south): dark clinker with glowing cracks
    for lo, hi in ((x0, 98), (412, x1)):
        zone = box & (b.xx >= lo) & (b.xx < hi)
        slag = zone & (tex < 0.47)
        b.base = np.where(slag[..., None], b.base * 0.55, b.base)
        crack = zone & (np.abs(fine - 0.5) < 0.012)
        b.base = np.where(crack[..., None], b.base * 0.5 + np.array((255, 120, 30)) * 0.5, b.base)
    # the buildings as plan outlines over darker floors
    floors = [(104, 34, 196, 104), (104, 138, 196, 208),          # the two conveyor halls
              (206, 26, 294, 216),                                 # the furnace row
              (350, 30, 404, 212)]                                 # the casting yard
    for fx0, fy0, fx1, fy1 in floors:
        zone = (b.xx >= fx0) & (b.xx < fx1) & (b.yy >= fy0) & (b.yy < fy1)
        b.base = np.where(zone[..., None], b.base * 0.62 + np.array((30, 34, 44)) * 0.38, b.base)
        b.draw.rectangle([fx0, fy0, fx1 - 1, fy1 - 1], outline=GREY + (220,))
    for gx in range(354, 404, 10):                                 # the casting yard's slabs
        for gy in range(34, 210, 12):
            b.draw.rectangle([gx, gy, gx + 7, gy + 8], outline=(80, 92, 110, 150))
    for cy in (78, 168):                                           # molten channels across the yard
        b.line([(352, cy), (402, cy)], (255, 120, 40), 1, 200)
    # the belts: lines along the route (they run up the screen = south), arrows on them
    belts = [((104, 196), 62), ((104, 196), 80), ((104, 196), 162), ((104, 196), 184),   # x 140/340 in each hall
             ((206, 294), 121), ((296, 350), 70), ((296, 350), 172), ((404, 440), 121)]
    for (bx0, bx1), by in belts:
        b.line([(bx0, by), (bx1, by)], (150, 140, 120), 3, 200)
        b.line([(bx0, by), (bx1, by)], (40, 38, 36), 1, 230)
        for ax in range(bx0 + 12, bx1 - 4, 26):
            b.draw.polygon([(ax + 3, by), (ax - 1, by - 2), (ax - 1, by + 2)], fill=AMBER + (220,))
    # the furnace row: stacks (rings with the glow) and the vent rows between them
    for sx in (218, 250, 282):
        for sy in (52, 192):
            b.glow(sx, sy, 6, (255, 110, 30), 0.9)
            b.ring(sx, sy, 7, colour=GREY, alpha=230)
    for vx in (230, 262):
        for vy in list(range(40, 110, 10)) + list(range(136, 208, 10)):
            b.draw.rectangle([vx - 2, vy - 2, vx + 2, vy + 2], fill=(255, 140, 40, 210))
    tag(b, 212, 108, "VENTS", AMBER)
    # the yard gate (a wall across the approach with its gate and belt slots) and the south gate
    b.line([(346, 26), (346, 64)], WHITE, 2, 220)
    b.line([(346, 76), (346, 112)], WHITE, 2, 220)
    b.line([(346, 130), (346, 166)], WHITE, 2, 220)
    b.line([(346, 178), (346, 216)], WHITE, 2, 220)
    b.line([(440, 22), (440, 112)], WHITE, 2, 220)
    b.line([(440, 130), (440, 222)], WHITE, 2, 220)
    tag(b, 300, 24, "YARD GATE", CYAN_DIM)
    tag(b, 412, 24, "SOUTH GATE", CYAN_DIM)
    tag(b, 106, 24, "CONVEYOR HALLS", CYAN_DIM)
    tag(b, 22, 24, "SLAG FIELDS", CYAN_DIM)
    tag(b, 354, 104, "CASTING YARD", CYAN_DIM)
    # the smelter tower: east of the route (the play field's left), the crews sealed in it
    tx, ty = 294, 200
    b.glow(tx, ty, 16, (255, 120, 30), 0.7)
    b.ring(tx, ty, 13, colour=WHITE, alpha=230, width=2)
    b.ring(tx, ty, 7, colour=AMBER, alpha=230)
    tag(b, tx - 64, ty + 16, "SMELTER TOWER", WHITE)
    big(b, tx + 17, ty - 6, "200", GREEN)
    tag(b, tx + 18, ty + 10, "CREW", GREEN)
    # red Vrell vectors into the works
    for sx_, sy_, ex_, ey_ in ((70, 10, 120, 46), (170, 10, 216, 40), (250, 236, 262, 214), (320, 10, 360, 44),
                               (60, 236, 108, 196)):
        b.arrow(sx_, sy_, ex_, ey_, RED, size=3)
    rng = np.random.default_rng(4911)
    for _ in range(10):
        vx, vy = rng.uniform(26, 340), rng.uniform(36, 206)
        if not any(abs(vy - by) < 8 for _, by in belts):
            b.marker(vx, vy, "chevron", RED, 3)
    # grey, unlabelled contacts on the conveyor lines (no tag: the display does not know them)
    for cx_, cy_ in ((128, 62), (166, 80), (142, 162), (184, 184), (232, 121), (270, 121), (318, 70), (330, 172)):
        b.draw.rectangle([cx_ - 4, cy_ - 3, cx_ + 4, cy_ + 3], fill=(150, 156, 162, 255), outline=(220, 226, 232, 255))
        b.draw.rectangle([cx_ - 1, cy_ - 1, cx_ + 1, cy_ + 1], fill=(60, 64, 70, 255))
    # the route, the sections numbered, the ship at the start
    route = [(x0 + 6, 121), (x1 - 6, 121)]
    b.dashed(route, AMBER, 1, 3, 4)
    for n, x in enumerate((56, 150, 250, 322, 378, 452), 1):
        b.draw.rectangle([x - 4, 228 - 5, x + 4, 228 + 5], fill=(10, 14, 30, 230), outline=AMBER + (255,))
        b.label(x - 2, 228 - 3, str(n), AMBER)
    b.sprite(scaled_sprite("ship_2", 0.5), 30, 121)
    b.title("THARSIS FOUNDRY - SITE PLAN")
    b.label(W - 10, 9, "GET THE CREWS OUT", AMBER, right=True)
    b.draw.rectangle([x0, 18, x1 - 1, H - 11], outline=CYAN_DIM + (180,))
    b.arrow(46, 40, 26, 40, CYAN, size=3, dashed=False)
    b.label(50, 37, "N", CYAN)
    px = 480
    b.line([(px - 6, 26), (px - 6, H - 12)], CYAN_DIM, 1, 90)
    b.label(px, 30, "NORTH TO SOUTH", WHITE)
    rows = [("1", "SLAG FIELDS"), ("2", "CONVEYOR HALLS"), ("3", "FURNACE ROW"), ("4", "YARD APPROACH"),
            ("5", "CASTING YARD"), ("6", "SOUTH GATE")]
    for i, (n, name) in enumerate(rows):
        y = 46 + 13 * i
        b.label(px, y, n, AMBER)
        b.label(px + 12, y, name, CYAN)
    b.line([(px, 128), (W - 12, 128)], CYAN_DIM, 1, 120)
    b.label(px, 136, "CLEAR THE FOUNDRY", AMBER)
    b.label(px, 150, "NO GREY TANK LEAVES", GREEN)
    b.label(px, 160, "12 OF 14", GREEN)
    b.label(px, 176, "VRELL IN THE LOWER WORKS", RED)
    b.label(px, 186, "GREY CONTACTS: UNKNOWN", GREY)
    b.label(W - 10, H - 16, "CREWS SEALED IN", CYAN_DIM, right=True)
    return b


def floor_panel(b, x0, y0, w, h, dy, scale=0.5, dim=0.7):
    """A strip of Level 19's hall floor (its production tile) as the display's background, dimmed."""
    tile = np.array(_l19("hall-floor"))[..., :3].astype(np.float64)
    small = np.array(Image.fromarray(tile.astype(np.uint8)).resize((round(tile.shape[1] * scale),
                                                                     round(tile.shape[0] * scale)), Image.BOX))
    sx = (small.shape[1] - w) // 2 + 36
    crop = small[(np.arange(h)[:, None] + dy) % small.shape[0], (np.arange(w)[None, :] + sx) % small.shape[1]]
    b.base[y0:y0 + h, x0:x0 + w] = crop * dim + np.array((6, 14, 30)) * (1 - dim)
    b.draw.rectangle([x0, y0, x0 + w - 1, y0 + h - 1], outline=CYAN_DIM + (180,))


def arc_wedge(b, x, y, up=True, length=56, half=80, colour=RED):
    """A turret's firing arc: the facing (up) +-``half`` degrees, dashed edges and a faint arc."""
    base = -90 if up else 90
    for a in (base - half, base + half):
        r = math.radians(a)
        b.dashed([(x, y), (x + math.cos(r) * length, y + math.sin(r) * length)], colour, 1, 3, 3)
    b.arc(x, y, length, length, base - half, base + half, colour, 150)


def nest(b, x, y, aim, scale=0.6):
    b.sprite(held_frame("gantry-cradle", scale), x, y)
    b.sprite(scaled_sprite(f"spine-turret_{aim}", scale), x, y)


def aim32(x0, y0, x1, y1, n=32):
    ang = math.atan2(y1 - y0, x1 - x0) - math.pi / 2
    return int(round((ang % (2 * math.pi)) / (2 * math.pi / n))) % n


def rear_arcs():
    """L19 p2: Varga's diagram: a gantry turret's firing arc pointing up the line, silent while the ship is below it
    and firing once it has passed; a Warden Tank riding a belt with its turret tracking backwards, its thin rear
    plate circled; a large blurred thermal blob in the casting yard in the corner."""
    b = Board()
    gantry = held_frame("gantry", 0.5)
    pw, ph, py = 124, 206, 22
    for i, (gx, label) in enumerate(((16, "1 ABOVE YOU: SILENT"), (148, "2 PASSED: IT FIRES"))):
        floor_panel(b, gx, py, pw, ph, 120 + 90 * i)
        cx = gx + pw // 2
        gy = py + (62 if i == 0 else 176)
        b.sprite(gantry.crop((60, 0, 60 + pw - 2, gantry.height)), cx, gy)
        ny = gy - 16
        ship = (cx + 14, py + (176 if i == 0 else 70))
        arc_wedge(b, cx, ny, length=48 if i == 0 else 92, colour=RED if i else GREY)
        nest(b, cx, ny, 16 if i == 0 else aim32(cx, ny, *ship))
        b.sprite(scaled_sprite("ship_2", 0.6), *ship)
        if i:
            a = math.atan2(ship[1] - ny, ship[0] - cx)
            for d in (24, 44, 64):
                b.sprite(asset("orb_0"), cx + math.cos(a) * d, ny + math.sin(a) * d)
        tag(b, gx + 4, py + 4, label, RED if i else CYAN)
    tag(b, 20, py + 124, "FACES UP", GREY)
    tag(b, 20, py + 134, "THE LINE", GREY)
    # the Warden Tank on its belt: hull up the belt, turret tracking back down at the ship, the rear plate circled
    tx0, tw = 280, 160
    floor_panel(b, tx0, py, tw, ph, 300)
    belt = np.array(_l19("belt-plates_0").resize((48, 288), Image.BOX))
    bx = tx0 + 28
    belt_img = Image.fromarray(belt[40:40 + ph - 2], "RGBA")
    b.sprite(held_frame_img(belt_img), bx, py + ph // 2)
    hull = held_frame("warden-tank_16", 0.8)
    tank = (bx, py + 74)
    ship = (tx0 + 118, py + 176)
    aim = aim32(*tank, *ship)
    b.sprite(hull, *tank)
    b.sprite(held_frame(f"warden-tank-turret_{aim * 2}", 0.8), *tank)
    rear = (tank[0], tank[1] + 21)
    b.ring(rear[0], rear[1], 14, 6, colour=AMBER, alpha=255, width=1)
    b.line([(rear[0] + 14, rear[1] + 2), (tx0 + 92, py + 112)], AMBER, 1)
    tag(b, tx0 + 66, py + 114, "REAR PLATE", AMBER)
    tag(b, tx0 + 66, py + 124, "X1.5", AMBER)
    b.arrow(bx + 32, py + 176, bx + 32, py + 146, CYAN, size=3, dashed=False)
    tag(b, bx + 38, py + 160, "BELT", CYAN)
    b.arrow(tank[0] + 34, tank[1] - 30, tank[0] + 34, tank[1] - 4, GREY, size=3, dashed=False)
    tag(b, tx0 + 82, py + 30, "IT PASSES YOU", GREY)
    b.dashed([tank, ship], RED, 1, 3, 3)
    b.sprite(scaled_sprite("ship_2", 0.6), *ship)
    tag(b, bx + 30, py + 4, "GREY TANK", WHITE)
    tag(b, bx + 52, py + 186, "TURRET", RED)
    tag(b, bx + 52, py + 196, "TRACKS BACK", RED)
    # the corner: the casting yard's thermal, one large blurred contact
    ix0, iy0, iw, ih = 452, 128, 210, 98
    legs = held_asset("revenant-walker-legs_0")
    hullw = held_asset("revenant-walker_0")
    comp = Image.new("RGBA", legs.size, (0, 0, 0, 0))
    comp.alpha_composite(hullw, ((legs.width - hullw.width) // 2, (legs.height - hullw.height) // 2))
    comp.alpha_composite(legs)
    from PIL import ImageFilter
    blob = np.array(comp.split()[3].resize((90, 90), Image.BOX).filter(ImageFilter.GaussianBlur(5))).astype(np.float64) / 255
    heat = np.zeros((ih, iw))
    hx, hy = 120, 6
    heat[hy:hy + 90, hx:hx + 90] = blob
    noise = raster.fbm(iw, ih, 5, 4931, octaves=3, period=False)
    heat = np.clip(heat * 1.25 + 0.16 * (noise - 0.5) + 0.12, 0, 1)
    b.base[iy0:iy0 + ih, ix0:ix0 + iw] = raster.ramp(THERMAL, heat * 0.95)
    b.draw.rectangle([ix0, iy0, ix0 + iw - 1, iy0 + ih - 1], outline=CYAN_DIM + (200,))
    tag(b, ix0 + 4, iy0 + 4, "CASTING YARD - THERMAL", WHITE)
    tag(b, ix0 + 4, iy0 + ih - 24, "ONE CONTACT", AMBER)
    tag(b, ix0 + 4, iy0 + ih - 13, "LARGE. UNKNOWN", AMBER)
    b.bracket(ix0 + hx + 2, iy0 + hy + 2, ix0 + hx + 88, iy0 + hy + 88, AMBER)
    b.title("SENSOR SCAN - THEY FIRE AFTER YOU PASS")
    b.label(W - 10, 9, "REAR GUNS, LANCER", AMBER, right=True)
    px = 456
    b.label(px, 30, "GANTRY TURRETS", WHITE)
    b.label(px, 42, "FACE UP THE LINE:", CYAN)
    b.label(px, 52, "SILENT ABOVE YOU,", CYAN)
    b.label(px, 62, "FIRING AFTER YOU PASS", CYAN)
    b.label(px, 78, "GREY TANKS RIDE THE BELTS", WHITE)
    b.label(px, 88, "THIN REAR PLATE", CYAN)
    b.label(px, 104, "REAR + ANTI-GROUND", GREEN)
    return b


def held_frame_img(img, colours=32):
    a = np.array(img).astype(np.float64)
    a[..., 3] = np.where(a[..., 3] >= 128, 255, 0)
    return artkit.quantize_set([Image.fromarray(a.astype(np.uint8), "RGBA")], colours)[0]


def _file_strokes(cx, cy):
    """Bare metal worked with a hand file: bands of fine straight strokes, each band at its own slight angle (the
    file's passes), a few deeper scratches across them."""
    def f(p, n):
        x, y = p[:, 0] - cx, p[:, 1] - cy
        band = np.floor((x + 0.4) / 0.045)
        ang = 0.12 * np.sin(band * 2.3) + 0.05
        u = -np.sin(ang) * x + np.cos(ang) * y
        fine = 0.5 + 0.5 * np.sin(u * 1400 + band * 1.7)
        coarse = 0.5 + 0.5 * np.sin(u * 310 + 3 * np.sin(x * 40))
        deep = np.abs(np.sin((y - 0.35 * x) * 160)) > 0.996
        return np.clip(0.88 + 0.16 * fine * coarse + 0.08 * np.sin(x * 90 + band) - 0.35 * deep, 0.6, 1.3)
    return f


def plate_render(size, extent):
    """The Revenant Walker's wreck (tools/art/revenant_walker.py's model, heading 0) around its serial plate, rendered
    close: the plate found with a probe render, the plate's material worked with a hand file."""
    import revenant_walker as rw
    from bosses_r36 import W_PLATE
    from dataclasses import replace
    from render import sdf
    from render.sdf import Material
    scene = rw.topple_scene(1.0, wreck=True)
    mats = rw.mats(vent=0.0, wreck=True)
    probe = [Material((0, 0, 0), emission=(1, 1, 1) if i == W_PLATE else (0, 0, 0)) for i in range(len(mats))]
    ext0, n0 = 3.9, 140
    pr = sdf.render(scene, probe, (n0, n0), ext0, shadows=False)
    ys, xs = np.nonzero(pr[..., 0] > 0.5)
    cx, cy = (xs.mean() + 0.5 - n0 / 2) * ext0 / n0, -(ys.mean() + 0.5 - n0 / 2) * ext0 / n0
    mats[W_PLATE] = replace(mats[W_PLATE], albedo=(0.9, 0.9, 0.88), pattern=_file_strokes(cx, cy))
    ss = 2
    hi = sdf.render(scene, mats, (size[0] * ss, size[1] * ss), extent, center=(cx + 0.06, cy - 0.02))
    img = sprite.to_image(sprite.downsample(hi, ss), 0.5)
    return sprite.sharpen(img, 50), (cx, cy)


def filed_plate():
    """L20 p2: the Revenant Walker's flank plate close up (Level 19's wreck render): the serial field filed to bright
    bare metal, the file strokes catching the light; an assay strip clipped beside it, one line circled by hand."""
    b = Board()
    rx, ry, rw_, rh = 16, 20, 420, 210
    photo, _ = plate_render((rw_, rh), 0.78)
    photo = artkit.quantize_set([photo], 64)[0]
    bg = np.array(photo).astype(np.float64)
    a = bg[..., 3:4] / 255
    region = b.base[ry:ry + rh, rx:rx + rw_]
    b.base[ry:ry + rh, rx:rx + rw_] = bg[..., :3] * a + region * (1 - a)
    b.draw.rectangle([rx, ry, rx + rw_ - 1, ry + rh - 1], outline=CYAN_DIM + (200,))
    # the serial field and the callouts (the plate sits left of the render's centre: centre - 0.06 units)
    k = rw_ / 0.78
    px_, py_ = rx + rw_ / 2 - 0.06 * k, ry + rh / 2 - 0.02 * k
    b.bracket(px_ - 62, py_ - 34, px_ + 62, py_ + 34, AMBER, arm=8)
    tag(b, px_ - 62, py_ - 48, "SERIAL FIELD", AMBER)
    b.line([(px_ + 30, py_ + 14), (rx + 300, ry + rh - 30)], WHITE, 1, 200)
    tag(b, rx + 300, ry + rh - 28, "FILED BY HAND", WHITE)
    tag(b, rx + 300, ry + rh - 17, "NOT WORN", WHITE)
    tag(b, rx + 6, ry + 6, "WRECK 19-A, FLANK, CLOSE", CYAN)
    # the assay strip, clipped beside the photo, slightly askew
    sw, sh = 92, 196
    strip = Image.new("RGBA", (sw, sh), (226, 222, 204, 255))
    d = ImageDraw.Draw(strip)
    for yy in range(0, sh, 2):
        d.line([(0, yy), (sw, yy)], fill=(218, 214, 196, 255))
    ink = (40, 44, 60, 255)
    lines = ["ASSAY 19-A", "FLANK PLATE", "", "FE   61.8", "CR   18.2", "NI    9.4", "MO    3.1", "MN    1.7",
             "TI    0.9", "V     0.4", "", "GRAIN: CAST", "ROLLED", "", "STOCK MATCH", "HELIX TF-7", "97.2 %"]
    for i, text in enumerate(lines):
        raster.draw_text(strip, 6, 14 + 10 * i, text, ink)
    d.rectangle([sw // 2 - 14, 0, sw // 2 + 14, 7], fill=(150, 156, 166, 255), outline=(80, 84, 96, 255))
    ring = Image.new("RGBA", (sw, sh), (0, 0, 0, 0))                # her pen: a wobbly loop round the match
    rd = ImageDraw.Draw(ring)
    cy0 = 14 + 10 * 15 + 1.5
    pts = [(sw / 2 + math.cos(t) * (44 + 2 * math.sin(3 * t)) + 2 * math.sin(7 * t),
            cy0 + 2 + math.sin(t) * (6.5 + 1.0 * math.cos(5 * t))) for t in np.linspace(0.3, 2 * math.pi + 0.9, 60)]
    rd.line(pts, fill=(200, 30, 40, 255), width=1)
    strip.alpha_composite(ring)
    strip = strip.rotate(-3, resample=Image.NEAREST, expand=True)
    sa = np.array(strip)
    sa[..., 3] = np.where(sa[..., 3] >= 128, 255, 0)
    strip = artkit.quantize_set([Image.fromarray(sa, "RGBA")], 16)[0]
    b.sprite(strip, 494, 126)
    px = 548
    b.label(px, 30, "THE PLATE", WHITE)
    b.label(px, 44, "SERIAL FIELD", CYAN)
    b.label(px, 54, "FILED BLANK", CYAN)
    b.label(px, 64, "BY HAND", CYAN)
    b.label(px, 82, "NO COLOURS", GREY)
    b.label(px, 92, "NO NUMBERS", GREY)
    b.label(px, 110, "THE ALLOY", WHITE)
    b.label(px, 120, "READS AS HELIX", AMBER)
    b.label(px, 130, "FOUNDRY STOCK", AMBER)
    b.title("REVENANT WALKER - FLANK PLATE")
    b.label(W - 10, 9, "FOR THE COMMANDER ONLY", RED, right=True)
    b.label(W - 10, H - 16, "DR. VARGA", CYAN_DIM, right=True)
    return b


SOURCE_M7G = artkit.source_note(SCRIPT, "M7 part G batch")


def hellas_fallback():
    """L20 p1: Okafor's CDF terrain plot of the Hellas basin's north side, north up: the rim's lip at the top with
    the militia's guns on it, the rim road's switchbacks, the wide upper terrace, the terrace steps as contour
    lines, the basin floor with the Vrell staging grounds; the strike route in (solid) and the fallback route back
    up the terraces and the rim road (dashed); the level's sections along the fallback."""
    b = Board()
    x0, x1, top, bot = 16, 468, 18, H - 10
    box = (b.xx >= x0) & (b.xx < x1) & (b.yy >= top) & (b.yy < bot)
    tex = raster.fbm(W, H, 24, 5001, octaves=4, period=False)
    fine = raster.fbm(W, H, 6, 5003, octaves=2, period=False)
    wig = 4 * np.sin(b.xx / 37.0) + 3 * np.sin(b.xx / 13.0 + 1.0)
    rim_y = 40 + wig                                               # the rim's lip
    elev = np.clip((150 - b.yy + 0.6 * wig) / 110, 0, 1)          # 1 at the rim .. 0 on the floor
    plain = b.yy < rim_y
    ground = raster.ramp(MARS_HOLO, np.clip(0.3 + 0.32 * elev + 0.22 * (tex - 0.5) + 0.1 * (fine - 0.5), 0, 1))
    floor = raster.ramp([(0.0, (30, 24, 20)), (1.0, (140, 112, 76))], np.clip(0.55 + 0.4 * (tex - 0.5), 0, 1))
    ground = np.where((elev <= 0)[..., None], floor, ground)
    ground = np.where(plain[..., None], raster.ramp(MARS_HOLO, np.clip(0.42 + 0.25 * (tex - 0.5), 0, 1)) * 0.8,
                      ground)
    b.base = np.where(box[..., None], ground, b.base)
    # the rim's lip, the terrace steps as contours, the upper terrace's pillars
    xs = np.arange(x0, x1, 2)
    b.line([(x, 40 + 4 * np.sin(x / 37.0) + 3 * np.sin(x / 13.0 + 1.0)) for x in xs], WHITE, 2, 220)
    for k, cy in enumerate((116, 126, 136, 146)):
        b.line([(x, cy + 3 * np.sin(x / (21.0 + 4 * k) + k) + 2 * np.sin(x / 9.0 + 2 * k)) for x in xs], CYAN_DIM, 1, 200)
    for px_, py_ in ((60, 100), (110, 94), (360, 98), (420, 104), (300, 92)):
        b.ring(px_, py_, 2, colour=GREY, alpha=200)
    # the staging grounds on the floor: Vrell growth, red contacts massing
    rng = np.random.default_rng(5011)
    for _ in range(22):
        gx, gy = rng.uniform(250, 440), rng.uniform(170, 222)
        b.glow(gx, gy, rng.uniform(3, 7), (150, 70, 200), 0.55)
    for _ in range(14):
        b.marker(rng.uniform(240, 450), rng.uniform(168, 222), "chevron", RED, 3)
    b.bracket(236, 160, 456, 226, RED)
    tag(b, 346, 150, "STAGING GROUNDS", RED)
    # the rim road's switchbacks between the upper terrace and the lip (the fallback's last climb)
    road = [(240, 92), (300, 84), (190, 74), (300, 64), (190, 54), (250, 44), (250, 34)]
    for a_, b_ in zip(road[:-1], road[1:]):
        b.line([a_, b_], (120, 100, 80), 3, 200)
    # the strike route in (solid) and the fallback (dashed) back up the terraces and the road
    b.line([(70, 22), (96, 70), (130, 128), (200, 176), (296, 194)], CYAN, 2, 230)
    b.arrow(200, 176, 296, 194, CYAN, size=3, dashed=False)
    tag(b, 78, 24, "STRIKE IN", CYAN)
    fall = [(330, 192), (300, 160), (262, 134), (244, 112), (240, 92)] + road[1:]
    b.dashed(fall, AMBER, 2, 4, 3)
    b.arrow(250, 40, 250, 26, AMBER, size=3, dashed=False)
    tag(b, 314, 108, "FALLBACK", AMBER)
    # the militia's guns on the lip, firing over the road
    for gx in (186, 222, 278, 316):
        gy = 40 + 4 * np.sin(gx / 37.0) + 3 * np.sin(gx / 13.0 + 1.0)
        b.marker(gx, gy, "square", GREEN, 3)
    tag(b, 330, 30, "MILITIA GUNS", GREEN)
    tag(b, 22, 24, "RIM", WHITE)
    tag(b, 22, 100, "UPPER TERRACE", CYAN_DIM)
    tag(b, 22, 128, "TERRACES", CYAN_DIM)
    tag(b, 22, 200, "BASIN FLOOR", CYAN_DIM)
    tag(b, 22, 60, "RIM ROAD", CYAN_DIM)
    # the sections along the fallback
    for n, (sx, sy) in enumerate(((300, 214), (318, 172), (286, 150), (252, 124), (226, 102), (178, 64), (262, 30)), 1):
        b.draw.rectangle([sx - 4, sy - 5, sx + 4, sy + 5], fill=(10, 14, 30, 230), outline=AMBER + (255,))
        b.label(sx - 2, sy - 3, str(n), AMBER)
    b.sprite(scaled_sprite("ship_2", 0.5), 318, 196)
    b.title("HELLAS BASIN - NORTH SIDE")
    b.label(W - 10, 9, "FALL BACK TO THE RIM", AMBER, right=True)
    b.draw.rectangle([x0, top, x1 - 1, bot - 1], outline=CYAN_DIM + (180,))
    b.arrow(452, 46, 452, 26, CYAN, size=3, dashed=False)
    b.label(449, 50, "N", CYAN)
    px = 480
    b.line([(px - 6, 26), (px - 6, H - 12)], CYAN_DIM, 1, 90)
    b.label(px, 30, "TERRAIN PLOT", WHITE)
    rows = [("1", "STRIKE"), ("2", "THE TURN"), ("3", "BASIN FLOOR"), ("4", "TERRACES"), ("5", "UPPER TERRACE"),
            ("6", "RIM ROAD"), ("7", "OVER THE RIM")]
    for i, (n, name) in enumerate(rows):
        y = 46 + 12 * i
        b.label(px, y, n, AMBER)
        b.label(px + 12, y, name, CYAN)
    b.line([(px, 134), (W - 12, 134)], CYAN_DIM, 1, 120)
    b.label(px, 142, "NOSE TO THE ENEMY", AMBER)
    b.label(px, 154, "REAR GUNS CLEAR THE ROAD", AMBER)
    b.label(px, 170, "MILITIA COVER AT THE RIM", GREEN)
    b.label(px, 186, "VRELL MASSING ON THE FLOOR", RED)
    b.label(W - 10, H - 16, "IF IT TURNS", CYAN_DIM, right=True)
    return b


SOURCE_M7H = artkit.source_note(SCRIPT, "M7 part H batch")
LIME_DIM = (110, 170, 50)
HB_C = (300.0, 206.0)                                          # the basin plot: the lime hollow, the lowest point
HB_RIM, HB_WALL, HB_DUNE, HB_HOLLOW = 184.0, 150.0, 108.0, 40.0   # radii: the rim, the wall's foot, the dunes, the hollow


def hb_at(r, ang):
    """A point of the basin plot at radius r and angle ang (degrees, 0 = east, 90 = north)."""
    a = math.radians(ang)
    return HB_C[0] + r * math.cos(a), HB_C[1] - r * math.sin(a)


def hellas_basin():
    """L21 p1: Okafor's CDF plot of the Hellas basin, north up: the plateau outside the rim, the rim with the CDF
    line held at the end of the retreat, the inner wall's terraces as contours, the dune sea's crests with a moving
    seismic contact, the lime hollow at the lowest point with seismic rings centred on it; the descent route in from
    the north rim and the five sections."""
    b = Board()
    x0, x1, top, bot = 16, 468, 18, H - 10
    box = (b.xx >= x0) & (b.xx < x1) & (b.yy >= top) & (b.yy < bot)
    tex = raster.fbm(W, H, 24, 5101, octaves=4, period=False)
    fine = raster.fbm(W, H, 6, 5103, octaves=2, period=False)
    ang = np.arctan2(-(b.yy - HB_C[1]), b.xx - HB_C[0])
    r = np.hypot(b.xx - HB_C[0], b.yy - HB_C[1]) + 3 * np.sin(ang * 7) + 2 * np.sin(ang * 13 + 1)
    elev = np.clip((r - HB_DUNE) / (HB_RIM - HB_DUNE), 0, 1)       # 0 on the floor .. 1 at the rim
    ground = raster.ramp(MARS_HOLO, np.clip(0.22 + 0.42 * elev + 0.22 * (tex - 0.5) + 0.1 * (fine - 0.5), 0, 1))
    plateau = raster.ramp(MARS_HOLO, np.clip(0.5 + 0.25 * (tex - 0.5), 0, 1)) * 0.85
    ground = np.where((r > HB_RIM)[..., None], plateau, ground)
    crest = np.abs(np.sin((b.yy + 9 * np.sin(b.xx / 23.0) + 4 * np.sin(b.xx / 7.0)) / 5.0)) > 0.93   # the dunes
    ground = np.where((crest & (r < HB_DUNE) & (r > HB_HOLLOW + 6))[..., None], ground * 1.35, ground)
    vein = np.abs(np.sin(ang * 9 + 6 * (fine - 0.5))) < 0.06                               # the hollow's lime veins
    hollow = r < HB_HOLLOW
    ground = np.where(hollow[..., None], raster.ramp([(0.0, (26, 24, 14)), (1.0, (90, 96, 40))],
                                                     np.clip(0.4 + 0.5 * (tex - 0.5), 0, 1)), ground)
    ground = np.where((hollow & vein)[..., None], np.array([150, 210, 70.0]), ground)
    b.base = np.where(box[..., None], ground, b.base)
    b.glow(*HB_C, 22, (60, 90, 20), 0.8)
    # the rim, the terraces as contours, the wall's foot
    angs = np.arange(0, 181, 2)
    def arc(rad, wob=0.0):
        pts = []
        for a in angs:
            x, y = hb_at(rad + wob * (3 * math.sin(math.radians(a) * 7) + 2 * math.sin(math.radians(a) * 13 + 1)), a)
            if x0 <= x < x1 and top <= y < bot:
                pts.append((x, y))
        return pts
    b.line(arc(HB_RIM, -1), WHITE, 2, 255)
    for k, rad in enumerate((172, 162, 152)):
        b.line(arc(rad, -1), CYAN_DIM, 1, 190 - 30 * k)
    b.line(arc(HB_DUNE + 4, -1), CYAN_DIM, 1, 110)
    for k, rad in enumerate((12, 22, 33, 46, 60)):                 # the seismic rings on the lowest point
        b.ring(*HB_C, rad, colour=LIME, alpha=220 - 30 * k)
    # the CDF line held on the north rim (the retreat's end), the wrecks below it
    for a in (121, 127, 133, 139, 145):
        gx, gy = hb_at(HB_RIM + 1, a)
        b.marker(gx, gy, "square", GREEN, 3)
    for a, rr in ((124, 174), (136, 168), (142, 176)):
        b.marker(*hb_at(rr, a), "cross", GREY, 2)
    tag(b, 30, 30, "CDF LINE - HELD", GREEN)
    tag(b, 30, 41, "WHERE THE RETREAT ENDED", CYAN_DIM)
    b.line([(126, 38), (*hb_at(HB_RIM + 6, 136),)], GREEN, 1, 160)
    # the Vrell: growths and contacts on the floor, thickest toward the hollow
    rng = np.random.default_rng(5111)
    for _ in range(18):
        rr, a = rng.uniform(20, 70), rng.uniform(0, 180)
        b.glow(*hb_at(rr, a), rng.uniform(3, 6), (150, 70, 200), 0.5)
    for _ in range(10):
        rr, a = rng.uniform(46, 120), rng.uniform(15, 165)
        x, y = hb_at(rr, a)
        if top + 8 < y < bot - 6:
            b.marker(x, y, "chevron", RED, 3)
    # a moving seismic contact under the dune sea (the ridge pass): a lime dashed trace with its heading
    trace = [hb_at(88, a) for a in range(30, 72, 4)]
    b.dashed(trace, LIME, 1, 4, 3)
    b.arrow(*trace[-2], *trace[-1], LIME, size=3, dashed=False)
    tag(b, 346, 100, "SEISMIC - MOVING", LIME)
    tag(b, HB_C[0] + 52, HB_C[1] - 30, "LOWEST POINT", LIME)
    # the descent route from the rim down the inner wall, over the dune sea into the hollow
    route = [hb_at(HB_RIM + 14, 116), hb_at(HB_RIM - 4, 114), hb_at(160, 110), hb_at(140, 106), hb_at(118, 102),
             hb_at(96, 98), hb_at(70, 96), hb_at(44, 94), hb_at(18, 92)]
    b.line(route, CYAN, 2, 230)
    b.arrow(*route[-2], *route[-1], CYAN, size=3, dashed=False)
    b.marker(*HB_C, "diamond", AMBER, 4)
    tag(b, 276, 26, "IN FROM THE RIM", CYAN)
    tag(b, 150, 120, "INNER WALL", CYAN_DIM)
    tag(b, 202, 176, "DUNE SEA", CYAN_DIM)
    tag(b, 24, 196, "LIME HOLLOW", LIME)
    b.line([(88, 199), (HB_C[0] - 42, HB_C[1] - 5)], LIME_DIM, 1, 160)
    for n, (rr, a) in enumerate(((HB_RIM - 2, 106), (152, 100), (100, 92), (56, 88), (26, 70)), 1):
        sx, sy = hb_at(rr, a)
        sx += 14
        b.draw.rectangle([sx - 4, sy - 5, sx + 4, sy + 5], fill=(10, 14, 30, 230), outline=AMBER + (255,))
        b.label(sx - 2, sy - 3, str(n), AMBER)
    b.sprite(scaled_sprite("ship_2", 0.5), *hb_at(HB_RIM + 8, 117))
    b.title("HELLAS BASIN - RIM TO THE LOWEST POINT")
    b.label(W - 10, 9, "NO MORE RUNNING", AMBER, right=True)
    b.draw.rectangle([x0, top, x1 - 1, bot - 1], outline=CYAN_DIM + (180,))
    b.arrow(452, 46, 452, 26, CYAN, size=3, dashed=False)
    b.label(449, 50, "N", CYAN)
    px = 480
    b.line([(px - 6, 26), (px - 6, H - 12)], CYAN_DIM, 1, 90)
    b.label(px, 30, "CDF TERRAIN PLOT", WHITE)
    rows = [("1", "HELLAS RIM"), ("2", "INNER WALL"), ("3", "DUNE SEA"), ("4", "LIME HOLLOW"), ("5", "THE SOURCE")]
    for i, (n, name) in enumerate(rows):
        y = 46 + 12 * i
        b.label(px, y, n, AMBER)
        b.label(px + 12, y, name, LIME if n == "5" else CYAN)
    b.line([(px, 112), (W - 12, 112)], CYAN_DIM, 1, 120)
    b.label(px, 120, "EVERY VRELL ATTACK", RED)
    b.label(px, 130, "ON MARS CAME FROM HERE", RED)
    b.label(px, 146, "THE SONG: UNDER THE FLOOR", LIME)
    b.label(px, 162, "WE GO DOWN THERE", AMBER)
    b.label(px, 172, "AND END IT", AMBER)
    b.label(W - 10, H - 16, "ARES LANDING IS COUNTING ON YOU", CYAN_DIM, right=True)
    return b


def colossus_sprite(size=104, open_=1.0):
    """The Dust Colossus's mouth from above, open: round 36's chosen model (tools/concept/bosses_r36.py, variant b:
    the bone mandibles, the palps, the lime throat in its ring of teeth), rendered here at ``size`` px (the play
    field's mouth is ~150 px across)."""
    import bosses_r36 as b36
    from render import enemy_rigs
    ms = enemy_rigs.ModelSpaceAngleSprites(lambda rise=1.0, open_=0.0: b36.colossus_b(rise, open_), size, 4,
                                           extent=300 / b36.C_S, factor=3, colors=48)
    return held_frame_img(ms.frame(0, rise=1.0, open_=open_), 48)


def colossus_scan():
    """L21 p2: Varga's seismic cut through the basin floor: the surface with the ridge the body raises, the body
    under the sand (one organism, colony-sized, violet-veined segments), a mouth breaching at the cut's left edge
    with the lime gullet; beside it a breach seen from above at the field's left edge: the mouth with its gullet,
    the bite circle, the suction's lines toward it and the ship for scale."""
    b = Board()
    # ---- the seismic cut
    cx0, cx1, ctop, cbot = 16, 376, 22, H - 10
    surf = 86 + 2.5 * np.sin(b.xx / 13.0) + 1.5 * np.sin(b.xx / 5.0 + 1)
    ridge = 14 * np.exp(-((b.xx - 206) / 70) ** 4)                 # the ridge its back raises
    surf = surf - ridge
    cut = (b.xx >= cx0) & (b.xx < cx1) & (b.yy >= ctop) & (b.yy < cbot)
    tex = raster.fbm(W, H, 18, 5201, octaves=4, period=False)
    depth = np.clip((b.yy - surf) / (cbot - 86), 0, 1)
    strata = 0.5 + 0.5 * np.sin((b.yy - 0.3 * surf) / 4.0 + 3 * tex)
    sand = raster.ramp(MARS_HOLO, np.clip(0.62 - 0.45 * depth + 0.12 * (strata - 0.5) + 0.15 * (tex - 0.5), 0, 1))
    under = cut & (b.yy > surf)
    b.base = np.where(under[..., None], sand, b.base)
    # the body: a long chain of segments under the ridge, its veins violet, curving up into the breach at the left
    seg = [(326 - 21 * i, 150 - 26 * math.exp(-((326 - 21 * i - 206) / 90) ** 2)) for i in range(12)]
    seg += [(84, 146), (66, 134), (52, 118), (44, 102)]
    sd = np.full((H, W), 1e9)
    seam = np.zeros((H, W), bool)
    for i, (sx, sy) in enumerate(seg):
        rr = 22.0 if i < 12 else 17.0 - 2.5 * (i - 12)
        d = np.hypot((b.xx - sx) / 1.15, b.yy - sy) - rr
        seam |= (np.abs(d) < 0.7) & (d < sd + 2.5) & (sd < 0)    # a segment's front edge over the one behind it
        sd = np.minimum(sd, d)
    body = (sd < 0) & cut
    veins = np.abs(np.sin(b.xx / 6.0 + 4 * tex) * np.cos(b.yy / 5.0)) < 0.08
    shade = np.clip(0.5 - sd / 20, 0.3, 1.2)
    flesh = np.array([62, 36, 78.0]) * shade[..., None]
    flesh = np.where((veins | seam)[..., None], np.array([150, 80, 200.0]), flesh)
    b.base = np.where(body[..., None], flesh, b.base)
    b.base += (np.exp(-(sd / 1.8) ** 2) * cut)[..., None] * np.array([90, 40, 120.0])
    b.glow(206, 140, 120, (40, 14, 60), 0.6)
    # the breach at the left edge: the funnel in the surface and the mouth's lime gullet at its top
    fill(b, [(22, 80), (60, 80), (52, 98), (30, 98)], (20, 12, 22), 0.9)
    b.glow(41, 84, 10, (120, 200, 40), 1.0)
    b.glow(41, 84, 3, (200, 255, 120), 1.0)
    # the surface line, the ridge's dust haze, the depth ticks
    xs = np.arange(cx0, cx1, 2)
    sline = [(x, 86 + 2.5 * math.sin(x / 13.0) + 1.5 * math.sin(x / 5.0 + 1) - 14 * math.exp(-((x - 206) / 70) ** 4))
             for x in xs if not 22 <= x <= 60]
    b.line([p for p in sline if p[0] < 22], WHITE, 1, 220)
    b.line([p for p in sline if p[0] > 60], WHITE, 1, 220)
    b.glow(206, 66, 40, (70, 46, 30), 0.5)
    for i, y in enumerate((86, 124, 162, 200)):                    # the depth ticks on the cut's right edge
        b.line([(cx1 - 6, y), (cx1 - 1, y)], CYAN, 1, 200)
        b.label(cx1 - 9, y - 3, f"{i * 150} M", CYAN_DIM, right=True)
    tag(b, 172, 46, "RIDGE - ITS BACK", WHITE)
    b.line([(206, 56), (206, 70)], WHITE, 1, 180)
    tag(b, 150, 196, "ONE ORGANISM - COLONY-SIZED", VIOLET)
    tag(b, 150, 207, "NOTHING REACHES IT DOWN HERE", GREY)
    tag(b, 64, 58, "BREACH", LIME)
    b.line([(66, 66), (46, 80)], LIME, 1, 200)
    b.draw.rectangle([cx0, ctop, cx1 - 1, cbot - 1], outline=CYAN_DIM + (180,))
    tag(b, cx0 + 4, ctop + 4, "SEISMIC CUT - HELLAS FLOOR", CYAN)
    # ---- a breach from above, at the field's left edge
    fx0, fx1, ftop, fbot = 388, 556, 22, H - 10
    fw, fh = fx1 - fx0, fbot - ftop
    field = (b.xx >= fx0) & (b.xx < fx1) & (b.yy >= ftop) & (b.yy < fbot)
    dunes = 0.5 + 0.5 * np.sin((b.yy + 8 * np.sin(b.xx / 19.0)) / 6.0)
    floor = raster.ramp(MARS_HOLO, np.clip(0.5 + 0.18 * (dunes - 0.5) + 0.2 * (tex - 0.5), 0, 1))
    b.base = np.where(field[..., None], floor, b.base)
    mx, my = fx0 + 18, ftop + 86                                    # the mouth, two thirds on the screen
    k = 104 / 150                                                   # the sprite's px per play-field px
    dm = np.hypot(b.xx - mx, b.yy - my)
    b.base = np.where((field & (dm < 64))[..., None], b.base * (0.45 + 0.55 * np.clip((dm - 30) / 34, 0, 1))[..., None],
                      b.base)                                       # the funnel
    for a in range(-60, 61, 24):                                    # the suction's lines toward the mouth
        ca, sa = math.cos(math.radians(a)), math.sin(math.radians(a))
        r0, r1 = min(150, (my - ftop - 6) / max(1e-6, -sa) if sa < 0 else (fbot - 6 - my) / max(1e-6, sa)), 62
        b.dashed([(mx + ca * r0, my + sa * r0), (mx + ca * r1, my + sa * r1)], CYAN_DIM, 1, 3, 3)
        b.arrow(mx + ca * (r1 + 6), my + sa * (r1 + 6), mx + ca * r1, my + sa * r1, CYAN_DIM, size=2, dashed=False)
    mouth = colossus_sprite()
    full = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    full.alpha_composite(mouth, (int(mx - mouth.width / 2), int(my - mouth.height / 2)))
    clip = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    clip.paste(full.crop((fx0, ftop, fx1, fbot)), (fx0, ftop))
    b.sprites.append((clip, 0, 0))
    b.arc(mx, my, 70 * k, 70 * k, -108, 108, RED, 230)               # the bite circle (70 px), clipped to the field
    ship = scaled_sprite("ship_2", 0.5)
    b.sprite(ship, fx0 + 120, ftop + 150)
    b.draw.rectangle([fx0, ftop, fx1 - 1, fbot - 1], outline=CYAN_DIM + (180,))
    for i in range(3):                                              # the edge warning, 3 s ahead
        y0 = ftop + 132 + 12 * i
        b.draw.polygon([(fx0 + 2, y0), (fx0 + 8, y0 - 4), (fx0 + 8, y0 + 4)], fill=AMBER + (230,))
    tag(b, fx0 + 12, ftop + 136, "LEFT - 3 S", AMBER)
    tag(b, fx0 + 4, ftop + 4, "A BREACH, FROM ABOVE", CYAN)
    tag(b, fx0 + 62, ftop + 40, "BITE CIRCLE", RED)
    tag(b, fx0 + 70, ftop + 104, "SUCTION", CYAN)
    tag(b, fx0 + 4, fbot - 14, "GULLET - LIME", LIME)
    b.line([(fx0 + 20, fbot - 16), (mx + 2, my + 8)], LIME, 1, 200)
    # ---- the notes
    px = 566
    b.line([(px - 5, 26), (px - 5, H - 12)], CYAN_DIM, 1, 90)
    b.label(px, 30, "IT BREACHES", WHITE)
    b.label(px, 40, "WHERE THE", WHITE)
    b.label(px, 50, "SAND BULGES", AMBER)
    b.label(px, 66, "ANY SIDE", CYAN)
    b.label(px, 76, "EVEN BEHIND", RED)
    b.label(px, 92, "THROAT: LIME", LIME)
    b.label(px, 102, "THE WEAK SPOT", LIME)
    b.label(px, 118, "IT PULLS YOU", CYAN)
    b.label(px, 128, "IN - THEN", CYAN)
    b.label(px, 138, "IT BITES", RED)
    b.label(px, 154, "BRING SIDE", AMBER)
    b.label(px, 164, "AND REAR GUNS", AMBER)
    b.title("DUST COLOSSUS - SEISMIC SCAN")
    b.label(W - 10, 9, "THE SONG COMES FROM UNDER THE FLOOR", LIME, right=True)
    b.label(W - 10, H - 16, "DR. VARGA", CYAN_DIM, right=True)
    return b



# --------------------------------------------------------------------------- Act 4 intro and Level 22 (M8 part B)

SOURCE_M8B = artkit.source_note(SCRIPT, "M8 part B batch")
L22 = ROOT / "assets" / "backdrop" / "level-22"
EUROPA_HOLO = [(0.0, (40, 34, 36)), (0.35, (120, 104, 96)), (0.7, (206, 196, 184)), (1.0, (246, 240, 230))]
LINEA = np.array((150, 80, 52.0))                      # Europa's lineae: rust-brown
CONAMARA = (-88.0, 10.0)                              # Conamara Chaos on the globe (lon, lat)
JUP_BANDS = [(0.0, (70, 40, 26)), (0.25, (150, 96, 60)), (0.45, (214, 176, 132)), (0.6, (236, 214, 180)),
             (0.75, (178, 118, 74)), (1.0, (120, 70, 44))]   # tools/concept/europa_r48.py's JUP


def jupiter_disc(b, cx, cy, r, seed=4501, light_dir=(-0.62, -0.5, 0.6), strength=1.0):
    """Jupiter on the display: latitude belts and zones with turbulence, the Great Red Spot, lit from the upper
    left, its night side dark."""
    nx, ny = (b.xx - cx) / r, (b.yy - cy) / r
    r2 = nx ** 2 + ny ** 2
    inside = r2 < 1
    nz = np.sqrt(np.clip(1 - r2, 0, 1))
    lat = np.arcsin(np.clip(ny, -1, 1))
    turb = raster.fbm(W, H, 24, seed, octaves=4, period=False)
    belts = np.sin(lat * 9.0 + 0.6 * turb) * 0.6 + np.sin(lat * 23.0 + 1.6 * turb + 0.4 * np.sin(nx * 7)) * 0.4
    col = raster.ramp(JUP_BANDS, 0.5 + 0.3 * belts)
    spot = np.exp(-(((nx + 0.2) / 0.14) ** 2 + ((ny - 0.34) / 0.07) ** 2))
    col = col * (1 - 0.55 * spot[..., None]) + np.array((196, 84, 52.0)) * 0.55 * spot[..., None]
    L = np.array(light_dir) / np.linalg.norm(light_dir)
    light = np.clip(nx * L[0] + ny * L[1] + nz * L[2], 0, 1)
    body = col * (0.05 + 0.95 * np.clip(light * 1.25, 0, 1) ** 0.8)[..., None] * strength
    edge = np.clip((1 - np.sqrt(r2)) * r, 0, 1)[..., None]
    b.base = b.base * (1 - edge) + body * edge
    return inside


def europa_globe(b, g, seed=4502):
    """Europa through Globe ``g``: the cream-white ice with its rust lineae (great-circle bands), dark chaos
    patches, lit from the upper left."""
    inside, lon, lat, nx, ny, nz = g.inverse(b.xx, b.yy)
    rng = np.random.default_rng(seed)
    lo, la = np.radians(lon), np.radians(lat)
    p = np.stack([np.cos(la) * np.cos(lo), np.cos(la) * np.sin(lo), np.sin(la)], -1)
    lines = np.zeros(lon.shape)
    for _ in range(16):                                  # great circles: distance to a random plane through the centre
        v = rng.normal(size=3)
        v /= np.linalg.norm(v)
        d = np.abs(p @ v)
        w = rng.uniform(0.006, 0.016)
        lines = np.maximum(lines, np.exp(-(d / w) ** 2) * rng.uniform(0.5, 1.0))
    n = raster.fbm(W, H, 18, seed + 1, octaves=4, period=False)
    chaos = np.zeros(lon.shape)
    for clo, cla, cr in ((CONAMARA[0], CONAMARA[1], 9.0), (-40, -30, 12.0), (-130, 40, 8.0), (-60, 52, 10.0)):
        cc = np.radians([clo, cla])
        q = np.array([np.cos(cc[1]) * np.cos(cc[0]), np.cos(cc[1]) * np.sin(cc[0]), np.sin(cc[1])])
        ang = np.degrees(np.arccos(np.clip(p @ q, -1, 1)))
        chaos = np.maximum(chaos, np.clip(1 - ang / cr, 0, 1) * (0.5 + 0.5 * n))
    alb = 0.78 + 0.12 * (n - 0.5) - 0.3 * chaos
    lam = -0.62 * nx + 0.5 * ny + 0.6 * nz
    light = np.clip(lam * 1.15 + 0.05, 0, 1) ** 0.75
    body = raster.ramp(EUROPA_HOLO, np.clip(alb * light, 0, 1))
    body = body * (1 - lines[..., None] * 0.7) + LINEA * light[..., None] * lines[..., None] * 0.7
    b.base = np.where(inside[..., None], body, b.base)
    return inside


def canopy(b):
    """The transport's canopy as act-3-arrival's: the top bar, the left pillar, the strut before the nav screen."""
    frame = (9, 11, 20)
    fill(b, [(0, 0), (W, 0), (W, 20), (0, 20)], frame)
    fill(b, [(0, 20), (24, 20), (8, H), (0, H)], frame)
    fill(b, [(512, 20), (530, 20), (548, H), (530, H)], frame)
    fill(b, [(530, 20), (W, 20), (W, H), (548, H)], (6, 10, 24))
    fill(b, [(0, 230), (548, 230), (548, H), (0, H)], frame)
    for x in range(40, 512, 48):
        b.glow(x, 235, 3, (60, 120, 160) if x % 96 else (160, 110, 40), 0.9)
    b.line([(0, 20), (512, 20)], CYAN_DIM, 1, 150)
    b.line([(24, 20), (8, H)], CYAN_DIM, 1, 110)
    b.line([(512, 20), (530, H - 10)], CYAN_DIM, 1, 110)
    b.line([(8, 230), (531, 230)], CYAN_DIM, 1, 150)


def arrival4():
    """Act 4 p1: Europa in the transport's forward view against Jupiter, the Vrell fleet in orbit round it, the
    drill rigs on the ice at Conamara, the station's beacon; the nav screen's count of days and tremors."""
    b = Board()
    b.base *= 0.5
    rng = np.random.default_rng(4511)
    for _ in range(90):
        x, y = int(rng.integers(0, W)), int(rng.integers(0, H))
        b.base[y, x] += rng.uniform(40, 140)
    jupiter_disc(b, 420, 96, 150)
    g = Globe(230, 136, 96, -70, 8)
    europa_globe(b, g)
    canopy(b)
    b.title("CDF TRANSPORT - FORWARD VIEW")
    b.label(W - 10, 9, "EUROPA - DAY 12 AT FULL BURN", AMBER, right=True)
    # the Vrell fleet in orbit: a tilted ring of contacts round the moon
    pts = []
    for k in range(64):
        a = k / 64 * 2 * np.pi
        pts.append((230 + np.cos(a) * 140, 136 + np.sin(a) * 36 - np.cos(a) * 18))
    b.dashed(pts[:34], RED, 1, 3, 4)
    for k in range(0, 34, 4):
        x, y = pts[k]
        b.marker(x, y, "chevron", RED, 3)
    # the rigs on the ice at Conamara, the station's beacon
    cx, cy, _ = g.project(*CONAMARA)
    for dx, dy in ((-14, -10), (-6, -16), (4, -12), (12, -18)):
        b.marker(cx + dx, cy + dy, "dot", RED, 2)
    b.glow(cx, cy, 8, (220, 140, 40), 1.0)
    for k, r in enumerate((4, 8, 13)):
        b.ring(cx, cy, r, colour=AMBER, alpha=235 - 50 * k)
    b.marker(cx, cy, "diamond", AMBER, 3)
    b.line([(cx - 6, cy + 6), (cx - 30, cy + 40), (cx - 46, cy + 40)], AMBER, 1, 220)
    tag(b, cx - 50, cy + 36, "CONAMARA STATION", AMBER, right=True)
    tag(b, cx - 50, cy + 46, "TREMORS", WHITE, right=True)
    b.line([(cx + 14, cy - 20), (cx + 40, cy - 48), (cx + 52, cy - 48)], RED, 1, 220)
    tag(b, cx + 56, cy - 52, "DRILL RIGS ON THE ICE", RED)
    tag(b, 420, 196, "JUPITER", CYAN)
    tag(b, 60, 40, "VRELL FLEET - IN ORBIT", RED)
    b.bracket(30, 26, 506, 226, CYAN_DIM, 10)
    px = 560
    b.label(px, 30, "EUROPA", WHITE)
    b.label(px, 44, "AHEAD OF US", CYAN_DIM)
    b.label(W - 12, 44, "7 D", RED, right=True)
    b.label(px, 54, "FLEET", CYAN_DIM)
    b.label(W - 12, 54, "ORBIT", RED, right=True)
    b.label(px, 64, "DRILL RIGS", CYAN_DIM)
    b.label(W - 12, 64, "4", RED, right=True)
    b.line([(px, 80), (W - 12, 80)], CYAN_DIM, 1, 120)
    b.label(px, 88, "CONAMARA STATION", AMBER)
    b.label(px, 98, "TREMORS FOR", WHITE)
    big(b, px, 112, "7 DAYS", AMBER)
    b.label(px, 136, "40 IN THE DOME", CYAN_DIM)
    b.label(px, 146, "ON THE ICE SHELF", CYAN_DIM)
    b.line([(px, 162), (W - 12, 162)], CYAN_DIM, 1, 120)
    b.label(px, 172, "12 DAYS AT", CYAN_DIM)
    b.label(px, 182, "FULL BURN", CYAN_DIM)
    b.marker(px + 4, 202, "chevron", RED, 3)
    b.label(px + 14, 198, "THEY BEAT US", RED)
    b.label(px + 14, 208, "BY A WEEK", RED)
    return b


def europa_cutaway():
    """Act 4 p2: Varga's cut through Europa's shell: the ice kilometres thick, the ocean under it, Thera Deep's
    habitat and kelp farms in it, the Vrell rigs boring a shaft down through the ice toward them."""
    b = Board()
    x0, x1 = 20, 520
    surf = 46.0
    ice_bot = 112.0
    floor_y = 226.0
    xx, yy = b.xx, b.yy
    curve = 0.00006 * (xx - 270) ** 2                       # the moon's curve
    s_y, i_y = surf + curve, ice_bot + curve + 6 * np.sin(xx / 47) + 3 * np.sin(xx / 13)
    inside = (xx > x0) & (xx < x1)
    ice = inside & (yy > s_y) & (yy < i_y)
    n = raster.fbm(W, H, 16, 4521, octaves=4, period=False)
    icecol = raster.ramp([(0.0, (60, 90, 120)), (0.6, (140, 176, 206)), (1.0, (210, 228, 242))],
                         np.clip(0.4 + 0.5 * (1 - (yy - s_y) / (i_y - s_y)) + 0.2 * (n - 0.5), 0, 1))
    b.base = np.where(ice[..., None], icecol * 0.85, b.base)
    sea = inside & (yy >= i_y) & (yy < floor_y)
    depth = np.clip((yy - i_y) / (floor_y - i_y), 0, 1)
    seacol = np.array((10, 60, 84.0)) * (1 - depth[..., None]) + np.array((2, 10, 22.0)) * depth[..., None]
    b.base = np.where(sea[..., None], seacol, b.base)
    b.line([(x, float(surf + 0.00006 * (x - 270) ** 2)) for x in range(x0, x1, 4)], WHITE, 1, 220)
    b.title("CDF SCIENCE - EUROPA, A CUT THROUGH THE SHELL")
    b.label(W - 10, 9, "DR. VARGA", CYAN_DIM, right=True)
    # Thera Deep: the habitat under the ice, the lamp-lit kelp farms round it
    hx, hy = 330, 150
    for k in range(9):                                       # kelp farm rows
        fx = 250 + k * 20
        top = hy + 30 + 6 * np.sin(k)
        b.line([(fx, top), (fx, floor_y - 2)], (70, 200, 120), 1, 150)
        b.line([(fx + 2, top + 8), (fx + 5, floor_y - 2)], (70, 200, 120), 1, 110)
    b.glow(hx, hy + 52, 60, (40, 110, 70), 0.6)
    b.draw.ellipse([hx - 26, hy - 12, hx + 26, hy + 12], outline=AMBER + (240,), width=2)
    b.draw.rectangle([hx - 4, hy - 34, hx + 4, hy - 12], outline=AMBER + (220,))
    for k in range(5):
        b.glow(hx - 18 + 9 * k, hy, 2, (255, 190, 80), 0.9)
    b.line([(hx, hy - 34), (hx, float(ice_bot) + 4)], AMBER, 1, 200)          # the lift up to the ice
    tag(b, hx + 34, hy - 6, "THERA DEEP", AMBER)
    tag(b, hx + 34, hy + 4, "40,000 PEOPLE", WHITE)
    tag(b, 300, 212, "KELP FARMS", GREEN)
    # Conamara on the surface, the rigs boring down
    cx = 150
    b.marker(cx, surf - 6, "square", AMBER, 3)
    tag(b, cx - 30, surf - 22, "CONAMARA", AMBER)
    for k, rx in enumerate((90, 118, 182, 206)):
        ry = surf + 0.00006 * (rx - 270) ** 2
        b.marker(rx, ry - 4, "chevron", RED, 3)
        depth_k = (0.75, 0.45, 0.55, 0.3)[k]
        y_end = ry + (ice_bot - surf) * depth_k
        b.line([(rx, ry), (rx, y_end)], RED, 2, 230)
        b.glow(rx, y_end, 4, (255, 60, 40), 0.9)
    b.dashed([(90, surf + 52), (90, ice_bot + 30), (240, 160)], RED, 1, 4, 3)
    tag(b, 44, 168, "THEY DRILL FOR", RED)
    tag(b, 44, 178, "THE OCEAN", RED)
    # the scale bar
    b.line([(x1 + 10, surf + 2), (x1 + 10, ice_bot)], CYAN, 1, 220)
    for y in (surf + 2, ice_bot):
        b.line([(x1 + 6, y), (x1 + 14, y)], CYAN, 1, 220)
    b.line([(x1 + 10, ice_bot + 4), (x1 + 10, floor_y)], CYAN_DIM, 1, 220)
    tag(b, x1 + 18, 74, "ICE", CYAN)
    tag(b, x1 + 18, 84, "KILOMETRES", CYAN)
    tag(b, x1 + 18, 160, "OCEAN", CYAN)
    tag(b, x1 + 18, 170, "UNDER IT", CYAN)
    b.label(x1 + 18, 206, "NOT HERE", RED)
    b.label(x1 + 18, 216, "FOR THE ICE", RED)
    return b


def hydro_kit():
    """Act 4 p3: the Stormhawk refitted for the water (the hydro-kit's production frame, ship-hydro_2, at 4x) with
    callouts to the sealed intakes, the torpedo rack and the pressure hull; the water's rules beside it."""
    b = Board()
    b.title("CDF REFIT - AF-12 STORMHAWK, HYDRO-KIT")
    b.label(W - 10, 9, "EVERYTHING SLOWS DOWN", AMBER, right=True)
    ship = asset("ship-hydro_2", 4)
    cx, cy = 180, 126
    b.glow(cx, cy, 90, (10, 60, 84), 0.9)
    for k in range(6):                                       # the water's pressure rings
        b.ring(cx, cy, 70 + 10 * k, colour=CYAN_DIM, alpha=90 - 12 * k)
    b.sprite(ship, cx, cy)
    calls = [((cx - 34, cy - 8), (40, 40), "SEALED INTAKES"),
             ((cx + 28, cy + 22), (300, 52), "TORPEDO RACK"),
             ((cx, cy + 52), (300, 196), "PRESSURE HULL"),
             ((cx - 10, cy - 70), (40, 210), "HEADLIGHTS")]
    for (px_, py_), (tx, ty), text in calls:
        b.line([(px_, py_), (tx + (0 if tx > cx else raster.text_width(text) + 4), ty + 4)], CYAN, 1, 200)
        b.marker(px_, py_, "dot", CYAN, 2)
        tag(b, tx + (6 if tx > cx else 0), ty, text, WHITE)
    px = 420
    b.label(px, 30, "UNDER WATER", WHITE)
    b.line([(px, 42), (W - 12, 42)], CYAN_DIM, 1, 120)
    b.label(px, 50, "BULLETS, BOTH SIDES", CYAN_DIM)
    big(b, px, 62, "X 0.7", AMBER)
    b.label(px, 88, "NOT A TORPEDO", CYAN_DIM)
    b.label(px, 98, "OR A DEPTH CHARGE", CYAN_DIM)
    big(b, px, 110, "-1/3 BITE", RED)
    b.line([(px, 134), (W - 12, 134)], CYAN_DIM, 1, 120)
    b.label(px, 142, "HARPOON TORPEDOES", GREEN)
    b.label(px, 152, "DEPTH CHARGES", GREEN)
    b.label(px, 162, "FULL BITE", GREEN)
    tor = asset("harpoon-torpedoes-shot_16", 2)
    b.sprite(tor, W - 40, 150)
    b.line([(px, 178), (W - 12, 178)], CYAN_DIM, 1, 120)
    b.label(px, 188, "FLY ACCORDINGLY", AMBER)
    b.label(px, 206, "FREE: THE KIT", CYAN_DIM)
    b.label(px, 216, "FITS EVERY SHIP", CYAN_DIM)
    return b


def drop4():
    """Act 4 p4: the drop: the transport over Europa's ice shelf (Level 22's production tiles from above, hazeless),
    the route down to the four rigs on the shelf, then into the shaft they bored; Jupiter low on the horizon."""
    b = Board()
    shelf = np.array(Image.open(L22 / "rig-field.png").convert("RGB").resize((240, 480), Image.LANCZOS)).astype(float)
    shelf = np.rot90(shelf, -1)                               # the flight direction to the right
    rgb = tiled(shelf, W, H, 30, 0)
    b.base = rgb * 0.62
    b.title("CDF TRANSPORT - DROP IN 90 MINUTES")
    b.label(W - 10, 9, "AEGIS ACTUAL", AMBER, right=True)
    tsp = Image.open(L15 / "transport.png").convert("RGBA").rotate(-90, expand=True)
    tsp = scaled(tsp, 0.3, 48)
    tx, ty = 104, 70
    shadow(b, tsp, tx, ty, 18, 26, 0.4)
    b.sprite(tsp, tx, ty)
    rigs = [(250, 150), (330, 96), (400, 168), (470, 110)]
    route = [(tx + 60, ty + 6), (220, 150)] + rigs + [(560, 150)]
    b.dashed(route, CYAN, 2)
    for k, (x, y) in enumerate(rigs):
        b.ring(x, y, 10, colour=RED, alpha=230)
        b.marker(x, y, "dot", RED, 2)
        tag(b, x - 12, y + 16, f"RIG {k + 1}", RED)
    hx, hy = 560, 150
    for k, r in enumerate((6, 11, 17)):
        b.ring(hx, hy, r, colour=AMBER, alpha=230 - 50 * k)
    b.arrow(hx, hy + 20, hx, hy + 70, AMBER, 6)
    tag(b, hx - 24, hy + 26, "THE HOLE", AMBER, right=True)
    tag(b, hx - 24, hy + 36, "WE FOLLOW IT DOWN", WHITE, right=True)
    tag(b, 196, 214, "1  THE RIGS ON THE SURFACE", WHITE)
    tag(b, 430, 214, "2  THEN THE SHAFT", AMBER)
    for k, x in enumerate((14, 30, 46)):
        ship = scaled(turned(asset("ship_2"), 90), 0.6)
        b.sprite(ship, tx + 40 + 16 * k, ty + 30 + 8 * k)
    return b


def conamara_map():
    """L22 p1: Okafor's top-down CDF map of the Conamara Chaos ice field, north up: the shelf's fissures and leads,
    the four rigs on the way south, Conamara Station's dome at the south end, the route and the three sections;
    Jupiter in an inset."""
    b = Board()
    field = np.array(Image.open(L22 / "ice-shelf.png").convert("RGB")).astype(float)
    chaos = np.array(Image.open(L22 / "rig-field.png").convert("RGB")).astype(float)
    mx0, mx1 = 20, 420
    strip = np.concatenate([field[:320], chaos[:400], field[320:640]], axis=0)       # north to south
    small = np.array(Image.fromarray(strip.astype(np.uint8)).resize((240, 640), Image.LANCZOS)).astype(float)
    small = np.rot90(small, 1)[:, ::-1] if False else small
    img = np.array(Image.fromarray(small.astype(np.uint8)).resize((mx1 - mx0, H - 40), Image.LANCZOS)).astype(float)
    lum = img @ np.array([0.3, 0.55, 0.15]) / 255
    holo_rgb = raster.ramp([(0.0, (6, 20, 34)), (0.5, (30, 90, 120)), (1.0, (130, 200, 230))], np.clip(lum, 0, 1))
    b.base[24:H - 16, mx0:mx1] = holo_rgb
    b.title("CDF MAP - CONAMARA CHAOS, NORTH UP")
    b.label(W - 10, 9, "FOUR RIGS ON THE SHELF", RED, right=True)
    route_x = [220, 230, 140, 330, 150, 330, 240, 240]
    route_y = [30, 70, 92, 120, 148, 170, 196, 210]
    b.dashed(list(zip(route_x, route_y)), CYAN, 2)
    for (x, y), name in zip(((140, 92), (330, 120), (150, 148), (330, 170)), ("RIG 1", "RIG 2", "RIG 3", "RIG 4")):
        b.draw.rectangle([x - 6, y - 6, x + 6, y + 6], outline=RED + (240,), width=2)
        b.marker(x, y, "dot", RED, 2)
        tag(b, x + 10, y - 4, name, RED)
    sx, sy = 240, 214
    for k, r in enumerate((5, 9, 14)):
        b.ring(sx, sy, r, colour=AMBER, alpha=235 - 50 * k)
    b.marker(sx, sy, "diamond", AMBER, 3)
    tag(b, sx + 18, sy - 4, "CONAMARA STATION", AMBER)
    for y, label in ((50, "1 THE SHELF"), (130, "2 THE RIG FIELD"), (196, "3 THE RUN-IN")):
        b.line([(mx0, y + 12), (mx1, y + 12)], CYAN_DIM, 1, 90)
        tag(b, mx0 + 6, y, label, CYAN)
    b.label(mx1 - 16, 30, "N", WHITE)
    b.arrow(mx1 - 13, 58, mx1 - 13, 42, WHITE, 4, dashed=False)
    # the inset: Jupiter over the shelf
    ix0, iy0, ix1, iy1 = 436, 28, 560, 120
    fill(b, [(ix0, iy0), (ix1, iy0), (ix1, iy1), (ix0, iy1)], (4, 6, 14), 1.0)
    jb = Board()
    jupiter_disc(jb, 60, 52, 44)
    b.base[iy0:iy1, ix0:ix1] = np.where(jb.base[:iy1 - iy0, :ix1 - ix0].max(axis=-1, keepdims=True) > 40,
                                        jb.base[:iy1 - iy0, :ix1 - ix0], b.base[iy0:iy1, ix0:ix1])
    b.draw.rectangle([ix0, iy0, ix1, iy1], outline=CYAN_DIM + (220,))
    tag(b, ix0 + 4, iy1 - 12, "JUPITER, OVERHEAD", CYAN)
    px = 576
    b.label(px - 8, 130, "THE SHELF", WHITE)
    b.label(px - 8, 140, "CRACKS", CYAN_DIM)
    b.label(px - 8, 150, "LEADS", CYAN_DIM)
    b.label(px - 8, 166, "THE RIGS", RED)
    b.label(px - 8, 176, "BORING", RED)
    b.label(px - 8, 192, "40 PEOPLE", AMBER)
    b.label(px - 8, 202, "IN THE DOME", AMBER)
    return b


def rig_scan():
    """L22 p2: Varga's scan of a Drill Rig: the production rig (collar, four pumps on their hoses) with the bore
    core marked, the hardened shell (shots glance off) and the weapons that get through; an inset of a crack
    glowing before a geyser's steam."""
    b = Board()
    b.title("CDF SCAN - VRELL DRILL RIG")
    b.label(W - 10, 9, "DR. VARGA", CYAN_DIM, right=True)
    base = held_asset("drill-rig-base")
    core = held_asset("drill-rig_0")
    pump = held_asset("drill-rig-pump_1")
    glow = held_asset("drill-rig-glow_0")
    rig = Image.new("RGBA", base.size, (0, 0, 0, 0))
    rig.alpha_composite(base)
    for dx, dy in ((-62, 58), (62, 58), (62, -58), (-62, -58)):
        rig.alpha_composite(pump, (96 + dx - 18, 96 - dy - 18))
    rig.alpha_composite(core, (32, 32))
    cx, cy = 170, 126
    rig = scaled(rig, 1.0, 64)
    shadow(b, rig, cx, cy, 6, 8, 0.4)
    b.sprite(rig, cx, cy)
    g = np.array(glow).astype(float)
    b.add_light(g[..., :3] * g[..., 3:4] / 255, cx - 64, cy - 64, "over")
    b.ring(cx, cy, 20, colour=LIME, alpha=240)
    b.line([(cx + 16, cy - 14), (cx + 120, cy - 70), (cx + 140, cy - 70)], LIME, 1, 230)
    tag(b, cx + 144, cy - 74, "BORE CORE - THE TARGET", LIME)
    b.ring(cx, cy, 58, colour=CYAN_DIM, alpha=160)
    b.line([(cx + 50, cy + 30), (cx + 130, cy + 60), (cx + 140, cy + 60)], CYAN, 1, 220)
    tag(b, cx + 144, cy + 56, "COLLAR - HARDENED", CYAN)
    for dx, dy in ((62, 58), (-62, -58)):
        b.ring(cx + dx, cy - dy, 15, colour=AMBER, alpha=200)
    b.line([(cx + 74, cy - 66), (cx + 120, cy - 96), (cx + 140, cy - 96)], AMBER, 1, 220)
    tag(b, cx + 144, cy - 100, "4 PUMPS - EACH SLOWS IT", AMBER)
    # glancing shots, the weapons that get through
    for k in range(3):
        x = cx - 90 + 8 * k
        b.line([(x, cy + 100), (x + 30, cy + 60)], WHITE, 1, 160)
        b.marker(x + 30, cy + 60, "cross", WHITE, 2)
    tag(b, 20, 216, "SHOTS GLANCE OFF", WHITE)
    px = 470
    b.label(px, 116, "GETS THROUGH", WHITE)
    b.line([(px, 128), (W - 12, 128)], CYAN_DIM, 1, 120)
    for k, name in enumerate(("BOMBS", "MORTARS", "MISSILES", "AIRSTRIKE")):
        b.label(px, 136 + 10 * k, name, GREEN)
    b.label(px, 186, "RING OF 10 SHARDS", RED)
    b.label(px, 196, "EVERY 3.5 S", RED)
    # the inset: a crack glowing before a geyser
    ix0, iy0, ix1, iy1 = 548, 24, 662, 104
    fill(b, [(ix0, iy0), (ix1, iy0), (ix1, iy1), (ix0, iy1)], (8, 14, 26), 1.0)
    crack = held_asset("ice-geyser_3")
    steam = held_asset("ice-geyser-steam_3")
    b.sprite(crack, ix0 + 30, iy0 + 42)
    b.sprite(steam, ix0 + 84, iy0 + 40)
    gl = np.array(held_asset("ice-geyser-glow_2")).astype(float)
    b.add_light(gl[..., :3] * gl[..., 3:4] / 255, ix0 + 30 - 32, iy0 + 42 - 32, "over")
    b.arrow(ix0 + 50, iy0 + 42, ix0 + 60, iy0 + 42, AMBER, 3, dashed=False)
    b.draw.rectangle([ix0, iy0, ix1, iy1], outline=CYAN_DIM + (220,))
    tag(b, ix0 + 4, iy1 - 12, "GLOW, THEN STEAM", LIME)
    return b


# --------------------------------------------------------------------------- Level 23 (M8 part C)

SOURCE_M8C = artkit.source_note(SCRIPT, "M8 part C batch")
TEAL_V = (0, 255, 154)


def drill_shaft():
    """L23 p1: Okafor's CDF section through the ice under Conamara: the shelf and the station's dome on the
    surface, the Vrell drill shaft straight down through twenty kilometres of ice with their collars as red marks,
    the waterline where the hydro-kits switch on, the ocean below, Thera Deep's lit pressure domes and farm racks
    on the floor at the shaft's foot; the route down the shaft and the level's four sections."""
    b = Board()
    xx, yy = b.xx, b.yy
    x0, x1 = 16, 456
    surf, ice_bot, floor_y = 34.0, 150.0, 226.0
    sx = 168.0                                                # the shaft
    inside = (xx > x0) & (xx < x1)
    s_y = surf + 2 * np.sin(xx / 31)
    i_y = ice_bot + 5 * np.sin(xx / 41 + 1) + 2 * np.sin(xx / 11)
    n = raster.fbm(W, H, 14, 4631, octaves=4, period=False)
    ice = inside & (yy > s_y) & (yy < i_y)
    icecol = raster.ramp([(0.0, (50, 78, 108)), (0.6, (120, 156, 190)), (1.0, (200, 220, 238))],
                         np.clip(0.35 + 0.55 * (1 - (yy - s_y) / (i_y - s_y)) + 0.2 * (n - 0.5), 0, 1))
    b.base = np.where(ice[..., None], icecol * 0.8, b.base)
    for k in range(5):                                        # strata in the ice
        y = surf + 18 + k * 22
        b.base = np.where((inside & (np.abs(yy - y - 3 * np.sin(xx / 23 + k)) < 0.6))[..., None],
                          b.base * 0.75, b.base)
    sea = inside & (yy >= i_y) & (yy < floor_y)
    depth = np.clip((yy - i_y) / (floor_y - i_y), 0, 1)
    seacol = np.array((10, 56, 70.0)) * (1 - depth[..., None]) + np.array((3, 14, 22.0)) * depth[..., None]
    b.base = np.where(sea[..., None], seacol, b.base)
    shaft = (np.abs(xx - sx) < 5 + 9 * np.clip((surf + 12 - yy) / 12, 0, 1)) & (yy > s_y - 1) & (yy < i_y + 1)
    b.base = np.where(shaft[..., None], np.array((8, 14, 26.0)) + 30 * np.clip(1 - (yy - surf) / 120, 0, 1)[..., None],
                      b.base)
    floor = inside & (yy >= floor_y)
    b.base = np.where(floor[..., None], np.array((18, 30, 30.0)), b.base)
    b.title("CDF OPS - THE DRILL SHAFT, CONAMARA TO THERA DEEP")
    b.label(W - 10, 9, "CMDR. OKAFOR", CYAN_DIM, right=True)
    b.line([(x0, surf), (x1, surf)], WHITE, 1, 200)
    # Conamara's dome on the shelf, beside the shaft's mouth
    cx = 118
    b.draw.arc([cx - 18, surf - 16, cx + 18, surf + 16], 180, 360, fill=AMBER + (240,), width=2)
    b.glow(cx, surf - 4, 10, (90, 60, 20), 0.6)
    tag(b, cx - 96, surf + 8, "CONAMARA", AMBER)
    # the Vrell collars down the shaft walls
    for y in np.arange(surf + 14, ice_bot - 4, 17):
        for side in (-1, 1):
            b.line([(sx + side * 4, y), (sx + side * 13, y + 3)], RED, 2, 240)
    b.line([(sx + 10, 74), (sx + 40, 64), (sx + 52, 64)], RED, 1, 220)
    tag(b, sx + 56, 60, "VRELL COLLARS", RED)
    # the waterline in the shaft
    wl = float(ice_bot) - 16
    b.line([(sx - 7, wl), (sx + 7, wl)], CYAN, 2, 250)
    b.glow(sx, wl, 9, (20, 90, 110), 0.7)
    b.line([(sx + 9, wl), (sx + 52, wl + 8), (sx + 62, wl + 8)], CYAN, 1, 230)
    tag(b, sx + 66, wl + 4, "WATERLINE - HYDRO-KITS ON", CYAN)
    # the route down
    b.arrow(sx, surf - 14, sx, wl - 10, AMBER, 5)
    tag(b, sx - 84, surf + 36, "AEGIS FLIGHT", AMBER)
    b.dashed([(sx, i_y[0, int(sx)] if np.ndim(i_y) else ice_bot), (sx + 30, 190), (sx + 150, 200), (x1 - 10, 196)],
             AMBER, 1, 4, 3)
    # Thera Deep on the floor: pressure domes, lit farm racks
    for k, (dx, r) in enumerate(((210, 16), (262, 22), (318, 14), (352, 10))):
        b.draw.arc([dx - r, floor_y - r, dx + r, floor_y + r], 180, 360, fill=AMBER + (230,), width=2)
        for j in range(3):
            b.glow(dx - r * 0.5 + j * r * 0.5, floor_y - r * 0.45, 2, (255, 190, 90), 0.9)
    for k in range(10):                                       # farm racks with grow lamps
        fx = 196 + k * 22
        b.line([(fx, floor_y - 2), (fx + 14, floor_y - 2)], GREEN, 1, 160)
        b.line([(fx + 3, floor_y - 2), (fx + 2, floor_y - 12 - 4 * np.sin(k))], (70, 200, 120), 1, 140)
        b.glow(fx + 7, floor_y - 6, 6, (70, 60, 20), 0.5)
    for k, (px_, py_) in enumerate(((240, floor_y - 4), (300, floor_y - 4), (380, floor_y - 4))):
        b.marker(px_, py_ - 6, "chevron", VIOLET, 3)
    tag(b, 236, floor_y - 44, "THERA DEEP - 40,000", AMBER)
    tag(b, 392, floor_y - 22, "SIPHON PUMPS", VIOLET)
    tag(b, 392, floor_y - 12, "ON THE RACKS", VIOLET)
    # the depth scale
    b.line([(x1 + 10, surf), (x1 + 10, ice_bot)], CYAN, 1, 220)
    for y in (surf, ice_bot):
        b.line([(x1 + 6, y), (x1 + 14, y)], CYAN, 1, 220)
    tag(b, x1 + 18, 82, "20 KM", CYAN)
    tag(b, x1 + 18, 92, "OF ICE", CYAN)
    # the level's sections
    px = 520
    b.label(px, 30, "THE ROUTE", WHITE)
    b.line([(px, 42), (W - 12, 42)], CYAN_DIM, 1, 120)
    for k, (text, col) in enumerate((("1 SHAFT MOUTH, DRY", WHITE), ("  WATERLINE", CYAN), ("2 WET SHAFT", CYAN),
                                     ("3 THERA DEEP FARMS", AMBER), ("4 THE PLAIN'S EDGE", GREY))):
        b.label(px, 52 + 12 * k, text, col)
    b.label(px, 128, "WALLS HURT", RED)
    b.label(px, 140, "SHOOT THE PUMPS,", VIOLET)
    b.label(px, 150, "NOT THE RACKS", VIOLET)
    return b


def eel_and_fry():
    """L23 p2: Varga's scan of the two new swimmers: an Eel Swarm chain (the production sprites: the head with its
    lime gills circled, the stripe that lights before it fires, the cut that scatters the tail) and a Glassfin school
    as a ribbon (a glint, then a ram down the lane); the under-water rules strip beside them."""
    b = Board()
    b.base = b.base * 0.85 + np.array((2, 18, 22.0)) * 0.6
    b.title("CDF SCAN - EEL SWARM, GLASSFIN SCHOOL")
    b.label(W - 10, 9, "DR. VARGA", CYAN_DIM, right=True)
    # the eel: head at the lower left, the chain curving up to the right
    pts = [(70 + 26 * k, 150 - 34 * math.sin(k * 0.55)) for k in range(9)]
    frames = []
    for k, (x, y) in enumerate(pts):
        ahead = pts[k - 1] if k else (x - 26, y + 18)
        hk = int(round(math.atan2(-(ahead[0] - x), ahead[1] - y) / (2 * math.pi) * 16)) % 16
        lit = 3 <= k <= 5
        name = (f"eel-swarm_{hk * 3 + 1}" if k == 0 else f"eel-swarm-tail_{hk * 2}" if k == len(pts) - 1
                else f"eel-swarm-segment_{hk * 2 + int(lit)}")
        frames.append((held_frame(name), x, y))
    for img, x, y in reversed(frames):
        shadow(b, img, x, y, 4, 6, 0.35)
        b.sprite(img, x, y)
    hx, hy = pts[0]
    b.ring(hx, hy, 15, colour=LIME, alpha=240)
    b.line([(hx - 10, hy + 12), (hx - 20, hy + 42), (hx - 12, hy + 42)], LIME, 1, 230)
    tag(b, 18, hy + 46, "GILLS - THE HEAD IS VITAL", LIME)
    b.line([(pts[4][0], pts[4][1] - 12), (pts[4][0] + 6, 70), (pts[4][0] + 16, 70)], VIOLET, 1, 230)
    tag(b, pts[4][0] + 20, 66, "STRIPE LIGHTS: IT FIRES", VIOLET)
    cx_, cy_ = (pts[6][0] + pts[7][0]) / 2, (pts[6][1] + pts[7][1]) / 2
    b.line([(cx_ - 6, cy_ - 10), (cx_ + 6, cy_ + 10)], RED, 2, 240)
    b.line([(cx_ + 4, cy_ + 12), (cx_ + 30, 206), (cx_ + 40, 206)], RED, 1, 220)
    tag(b, cx_ + 44, 202, "CUT: THE TAIL SCATTERS", RED)
    # the Glassfin school: a ribbon swaying across, one glinting and diving
    rib = [(346 + 13 * k, 62 + 80 * (k / 11) + 16 * math.sin(k * 0.8)) for k in range(12)]
    for k, (x, y) in enumerate(rib):
        nx, ny = rib[min(k + 1, 11)] if k < 11 else (x + 15, y + 8)
        hk = int(round(math.atan2(-(nx - x), ny - y) / (2 * math.pi) * 16)) % 16
        if k == 7:
            continue
        b.sprite(sprite.enlarge(held_frame(f"glassfin_{hk * 2}"), 2), x, y)
    gx, gy = rib[7]
    b.sprite(sprite.enlarge(held_frame("glassfin_1"), 2), gx, gy)
    gl = np.array(sprite.enlarge(held_asset("glassfin-glint_2"), 2)).astype(float)
    b.add_light(gl[..., :3] * gl[..., 3:4] / 255, int(gx - 24), int(gy - 24), "over")
    b.arrow(gx, gy + 10, gx, 214, CYAN, 4, dashed=True)
    tag(b, gx + 8, 178, "GLINT, THEN A RAM", CYAN)
    tag(b, gx + 8, 188, "DOWN YOUR LANE", CYAN)
    tag(b, 340, 34, "GLASSFIN - RIBBONS (2X)", TEAL_V)
    # the rules strip
    px = 520
    b.label(px, 30, "UNDER WATER", WHITE)
    b.line([(px, 42), (W - 12, 42)], CYAN_DIM, 1, 120)
    big(b, px, 52, "BULLETS X0.7", AMBER)
    big(b, px, 78, "DRAG", AMBER)
    b.label(px, 104, "TORPEDO RANGE", GREEN)
    big(b, px, 116, "+50 %", GREEN)
    b.label(px, 146, "THE REST OF YOUR", RED)
    b.label(px, 156, "GUNS LOSE A THIRD", RED)
    return b


# --------------------------------------------------------------------------- Level 24 (M8 part D)

SOURCE_M8D = artkit.source_note(SCRIPT, "M8 part D batch")
BEACON_RG = (255, 110, 50)


def l24_map_xy(t, x):
    """Level 24's (t s, play-field x px) on Okafor's map: north (the level's start) up."""
    return 24 + x * 400 / 480, 34 + t / 172 * 192


def abyssal_plain():
    """L24 p1: Okafor's sonar map of the abyssal plain, north up: Thera Deep's last lamps at the north edge, the
    silt floor as sonar contours, the four spawning reefs as red clusters, the three relay beacons as dashed amber
    marks in a line, the trench's mouth at the south end; the route in teal and the level's four sections."""
    b = Board()
    mx0, mx1, my0, my1 = 20, 428, 24, H - 10
    inside = (b.xx > mx0) & (b.xx < mx1) & (b.yy > my0) & (b.yy < my1)
    depth = np.clip((b.yy - my0) / (my1 - my0), 0, 1)
    n = raster.fbm(W, H, 40, 2461, octaves=4, period=False)
    floor = np.array((6, 22, 34.0)) * (1 - depth[..., None]) + np.array((2, 8, 18.0)) * depth[..., None]
    b.base = np.where(inside[..., None], floor + 10 * (n[..., None] - 0.5), b.base)
    level = (n * 9 + depth * 2) % 1.0                         # sonar contours of the silt
    contour = inside & (np.abs(level - 0.5) < 0.05)
    b.base = np.where(contour[..., None], b.base + np.array((10, 50, 60.0)) * (1 - 0.6 * depth[..., None]), b.base)
    # the trench's mouth at the south end
    tx, ty = l24_map_xy(172, 240)
    mouth = inside & (np.abs(b.xx - tx) < 70 + 30 * (b.yy - ty + 18) / 18) & (b.yy > ty - 18)
    b.base = np.where(mouth[..., None], np.array((1, 3, 8.0)), b.base)
    edge = inside & (np.abs(np.abs(b.xx - tx) - (70 + 30 * (b.yy - ty + 18) / 18)) < 1.0) & (b.yy > ty - 18)
    b.base = np.where(edge[..., None], np.array((40, 130, 180.0)), b.base)
    b.title("CDF SONAR - THE ABYSSAL PLAIN, NORTH UP")
    b.label(W - 10, 9, "CMDR. OKAFOR", CYAN_DIM, right=True)
    # Thera Deep's last lamps at the north edge, thinning out
    for k, (t, x) in enumerate(((0, 170), (0, 300), (2, 400), (4, 200), (12, 360), (22, 150))):
        mx, my = l24_map_xy(t, x)
        b.glow(mx, my, 12 - k, (200, 130, 40), 1.6 - 0.2 * k)
        b.marker(mx, my, "dot", AMBER, 1)
    for k, x in enumerate((60, 110)):                          # Thera Deep's domes at the north edge
        b.draw.arc([x - 14, my0 + 2, x + 14, my0 + 26], 180, 360, fill=AMBER + (220,), width=2)
    tag(b, mx0 + 6, my0 + 16, "THERA DEEP", AMBER)
    # the route
    pts = [l24_map_xy(t, x) for t, x in ((0, 240), (30, 250), (55, 220), (95, 240), (135, 230), (172, 240))]
    b.dashed(pts, TEAL_V, 2)
    b.arrow(*pts[-2], *pts[-1], TEAL_V, 4, dashed=False)
    # the four spawning reefs as red clusters
    rng = np.random.default_rng(2462)
    for k, (t, x) in enumerate(((36, 120), (48, 350), (64, 130), (80, 340))):
        mx, my = l24_map_xy(t, x)
        for _ in range(7):
            b.marker(mx + rng.normal(0, 3.2), my + rng.normal(0, 2.2), "dot", RED, 1)
        b.ring(mx, my, 8, colour=RED, alpha=230)
        tag(b, mx + (12 if x < 240 else -12), my - 4, f"REEF {k + 1}", RED, right=x >= 240)
    # the three relay beacons, dashed amber marks in a line
    marks = [l24_map_xy(t, x) for t, x in ((100, 60), (112, 420), (126, 100))]
    b.dashed(marks, BEACON_RG, 1, 3, 3)
    for mx, my in marks:
        b.line([(mx - 5, my), (mx + 5, my)], BEACON_RG, 2, 240)
        b.line([(mx, my - 5), (mx, my + 5)], BEACON_RG, 2, 240)
        b.glow(mx, my, 5, (120, 40, 10), 0.8)
    tag(b, marks[1][0] - 8, marks[1][1] + 8, "RELAY BEACONS", BEACON_RG, right=True)
    tag(b, tx + 76, ty - 14, "THE TRENCH", CYAN)
    for t, label in ((0, "1 THE PLAIN'S EDGE"), (30, "2 THE REEF FIELD"), (95, "3 THE BEACON LINE"),
                     (135, "4 THE RAY CROSSING")):
        _, y = l24_map_xy(t, 0)
        if t:
            b.line([(mx0, y), (mx1, y)], CYAN_DIM, 1, 90)
        tag(b, mx1 - 4, y + 3, label, CYAN, right=True)
    b.label(mx1 + 8, 30, "N", WHITE)
    b.arrow(mx1 + 11, 58, mx1 + 11, 42, WHITE, 4, dashed=False)
    px = 470
    b.label(px, 30, "THE PLAIN", WHITE)
    b.line([(px, 42), (W - 12, 42)], CYAN_DIM, 1, 120)
    b.label(px, 52, "THE LAMPS END:", AMBER)
    b.label(px, 62, "YOUR OWN LIGHTS", AMBER)
    b.label(px, 80, "4 SPAWNING REEFS", RED)
    big(b, px, 92, "BURN 3", RED)
    b.label(px, 114, "SOFT: TORPEDOES", GREEN)
    b.label(px, 124, "DO FULL DAMAGE", GREEN)
    b.label(px, 144, "RELAY BEACONS", BEACON_RG)
    b.label(px, 154, "HUMAN-BUILT", BEACON_RG)
    b.label(px, 172, "A SHADOW BELOW:", VIOLET)
    b.label(px, 182, "A RAY RISING", VIOLET)
    return b


def abyss_ray_scan():
    """L24 p2: Varga's scan of the Abyss Ray: a depth cut (the play plane with the ship, the Ray's shadow far below
    in the dark, its rise, the lime gill slits), the production Ray from above with its gill slits circled and a fan
    of nine needles drawn from its mouth; a Barbfin inset at 2x (the barbed snout, the violet needle)."""
    b = Board()
    b.base = b.base * 0.8 + np.array((1, 12, 20.0)) * 0.6
    b.title("CDF SCAN - ABYSS RAY, BARBFIN")
    b.label(W - 10, 9, "DR. VARGA", CYAN_DIM, right=True)
    # the depth cut, left: the play plane, the dark water under it, the shadow and its rise
    cx0, cx1 = 16, 196
    plane_y, sub_y = 62, 176
    b.line([(cx0, plane_y), (cx1, plane_y)], CYAN, 1, 220)
    tag(b, cx0, plane_y - 14, "YOUR PLANE", CYAN)
    dz = (b.xx > cx0) & (b.xx < cx1) & (b.yy > plane_y + 1) & (b.yy < 224)
    k = np.clip((b.yy - plane_y) / (224 - plane_y), 0, 1)
    b.base = np.where(dz[..., None], b.base * (1 - 0.6 * k[..., None]), b.base)
    ship = scaled(turned(asset("ship_2"), 90), 0.55)
    b.sprite(ship, 70, plane_y - 2)
    # the Ray in profile: a flat wing section with the tail, drawn as the shadow far below and again on the rise
    for (x, y, s, a) in ((120, sub_y, 1.0, 120), (150, 112, 0.8, 200)):
        pts = [(x - 46 * s, y), (x - 20 * s, y - 6 * s), (x + 16 * s, y - 7 * s), (x + 40 * s, y - 2 * s),
               (x + 44 * s, y), (x + 20 * s, y + 4 * s), (x - 30 * s, y + 3 * s)]
        b.draw.polygon(pts, fill=(14, 22, 30, a), outline=VIOLET + (a,))
        b.line([(x + 44 * s, y), (x + 70 * s, y + 2 * s)], VIOLET, 1, a)
        for g in range(3):
            b.glow(x - 6 * s + g * 6 * s, y + 2 * s, 2.2, (60, 110, 10), 0.9)
    b.arrow(120, sub_y - 12, 146, 124, VIOLET, 4, dashed=True)
    tag(b, cx0, sub_y + 16, "SHADOW: TORPEDOES REACH IT", GREEN)
    tag(b, cx0, sub_y + 28, "1.5 S, THEN IT RISES", VIOLET)
    # the Ray from above, centre
    rx, ry = 318, 124
    ray = held_frame("abyss-ray_0", 0.85, 48)
    shadow(b, ray, rx, ry, 8, 10, 0.4)
    b.sprite(ray, rx, ry)
    try:
        gl = np.array(scaled(held_asset("abyss-ray-glow_0"), 0.85, 32)).astype(float)
        b.add_light(gl[..., :3] * gl[..., 3:4] / 255, rx - gl.shape[1] / 2, ry - gl.shape[0] / 2, "over")
    except FileNotFoundError:
        pass
    for sx in (-1, 1):                                       # the lime slits on the leading edges
        b.line([(rx + sx * 58, ry + 46), (rx + sx * 70, ry + 58)], LIME, 1, 230)
    b.line([(rx - 70, ry + 58), (rx - 74, 214), (rx - 80, 214)], LIME, 1, 230)
    tag(b, rx - 80, 210, "GILL SLITS GLOW", LIME, right=True)
    for j in range(9):                                        # a fan of nine, 110 degrees, toward the ship
        a = np.radians(90 - 55 + 110 * j / 8)
        x0, y0 = rx + np.cos(a) * 14, ry + 64 + np.sin(a) * 14
        b.line([(x0, y0), (x0 + np.cos(a) * 22, y0 + np.sin(a) * 22)], VIOLET, 1, 220)
    tag(b, rx - 90, 30, "THREE FANS OF NINE", VIOLET)
    # the Barbfin inset
    ix0, iy0, ix1, iy1 = 470, 28, 660, 150
    fill(b, [(ix0, iy0), (ix1, iy0), (ix1, iy1), (ix0, iy1)], (4, 14, 22), 1.0)
    fin = held_frame("barbfin_0", 2.0, 48)
    b.sprite(fin, ix0 + 52, iy0 + 56)
    b.ring(ix0 + 52, iy0 + 56 + 24, 8, colour=VIOLET, alpha=230)
    for j in range(3):
        b.line([(ix0 + 52, iy0 + 92 + 12 * j), (ix0 + 52, iy0 + 98 + 12 * j)], VIOLET, 2, 240)
    b.draw.rectangle([ix0, iy0, ix1, iy1], outline=CYAN_DIM + (220,))
    tag(b, ix0 + 96, iy0 + 30, "BARBFIN (2X)", TEAL_V)
    b.label(ix0 + 96, iy0 + 46, "HOLDS AND", WHITE)
    b.label(ix0 + 96, iy0 + 56, "SPITS NEEDLES", WHITE)
    b.label(ix0 + 96, iy0 + 72, "PEELS OFF THE", VIOLET)
    b.label(ix0 + 96, iy0 + 82, "RAY'S SHADOW", VIOLET)
    px = 470
    b.label(px, 166, "THE RAY", WHITE)
    b.line([(px, 178), (W - 12, 178)], CYAN_DIM, 1, 120)
    b.label(px, 186, "A SHADOW FIRST:", GREEN)
    b.label(px, 196, "TORPEDOES, DEPTH", GREEN)
    b.label(px, 206, "CHARGES REACH IT", GREEN)
    b.label(px, 220, "THEN IT GLIDES OFF", GREY)
    return b


# --------------------------------------------------------------------------- Level 25 (M8 part E)

SOURCE_M8E = artkit.source_note(SCRIPT, "M8 part E batch")
L25_NESTS = [(18, "R"), (26.5, "R"), (28, "L"), (36.5, "R"), (38, "L"), (46.5, "R"), (48, "L"), (56.5, "R"),
             (58, "L"), (59.5, "R"), (69.5, "R"), (71, "L"), (79.5, "R"), (81, "L"), (91, "R"), (92.5, "L"),
             (94, "R"), (98.5, "L"), (98.5, "R"), (115, "R"), (116.5, "L"), (131, "R"), (132.5, "L"), (134, "R"),
             (142, "R"), (143.5, "L"), (145, "R"), (152, "R"), (153.5, "L"), (155, "R"), (156.5, "L")]


def l25_map_xy(t, x):
    """Level 25's (t s, play-field x px) on Okafor's chart: north (the level's start) up."""
    return 24 + x * 400 / 480, 30 + t / 173 * 198


def siren_trench():
    """L25 p1: Okafor's sonar chart of the trench, north up: the plain's edge at the top, the walls closing in from
    the field's sides (the rock beyond them dark), the side caves as notches cut into the wall lines, the pillar
    splitting the fork and the narrows, the Siren nests as teal dots along both walls, the red-gold relay beacons as
    dots at the walls' foot, the floor opening at the south end; the route in teal down the middle and the six
    sections."""
    import backdrop_l25 as b25                              # noqa: E402  (the trench's terrain and pieces)
    b = Board()
    mx0, mx1, my0, my1 = 24, 424, 26, H - 8
    ts = np.linspace(0, 173, 400)
    rows = np.array([b25.scroll(t) for t in ts])
    xl = np.nan_to_num(b25.foot(0, rows), nan=0.0)
    xr = np.nan_to_num(b25.foot(1, rows), nan=480.0)
    inside = (b.xx > mx0) & (b.xx < mx1) & (b.yy > my0) & (b.yy < my1)
    t_of = np.clip((b.yy - 30) / 198 * 173, 0, 173)
    fx = (b.xx - 24) * 480 / 400
    wl = np.interp(t_of, ts, xl)
    wr = np.interp(t_of, ts, xr)
    rock = inside & ((fx < wl) | (fx > wr))
    n = raster.fbm(W, H, 32, 2641, octaves=4, period=False)
    floor = np.array((4, 16, 26.0)) + 10 * (n[..., None] - 0.5)
    b.base = np.where(inside[..., None], floor, b.base)
    b.base = np.where(rock[..., None], np.array((14, 30, 40.0)) + 14 * (n[..., None] - 0.5), b.base)
    level = (n * 7 + t_of / 60) % 1.0
    contour = rock & (np.abs(level - 0.5) < 0.06)
    b.base = np.where(contour[..., None], b.base + np.array((10, 40, 50.0)), b.base)
    for side, xs in ((0, xl), (1, xr)):                       # the wall lines (the collision line)
        pts = [l25_map_xy(t, x) for t, x in zip(ts, xs) if 0 < x < 480]
        if len(pts) > 1:
            b.line(pts, CYAN, 1, 230)
    span = b25.PILLAR
    poly = [l25_map_xy(b25.time_at(r), x) for x, r in span]
    b.draw.polygon(poly, fill=(14, 30, 40, 255), outline=CYAN + (230,))
    for nt in b25.NOTCHES:                                    # the side caves: notches cut into the wall lines
        side = 0 if nt["side"] == "left" else 1
        t = b25.time_at(nt["scroll"])
        x0, y = l25_map_xy(t, float(b25.foot(side, nt["scroll"])))
        x1 = x0 - 7 if side == 0 else x0 + 7
        b.draw.rectangle([min(x0, x1), y - 2, max(x0, x1), y + 2], fill=(2, 6, 12, 255), outline=CYAN + (230,))
    b.title("CDF SONAR - THE SIREN TRENCH, NORTH UP")
    b.label(W - 10, 9, "CMDR. OKAFOR", CYAN_DIM, right=True)
    pts = [l25_map_xy(t, x) for t, x in ((0, 240), (40, 245), (80, 235), (100, 170), (106, 185), (113, 240),
                                         (140, 240), (173, 240))]
    b.dashed(pts, TEAL_V, 2)
    b.arrow(*pts[-2], *pts[-1], TEAL_V, 4, dashed=False)
    for t, side in L25_NESTS:                                 # the nests on both walls
        s = 0 if side == "L" else 1
        x = float(b25.foot(s, b25.scroll(t) + 28))
        mx, my = l25_map_xy(t, x + (5 if s == 0 else -5))
        b.glow(mx, my, 4, (0, 120, 80), 0.9)
        b.marker(mx, my, "dot", TEAL_V, 1)
    for t, side in b25.BEACONS:                               # the relay beacons, red-gold
        x = float(b25.foot(side, b25.scroll(t))) + (26 if side == 0 else -26)
        mx, my = l25_map_xy(t, x)
        b.glow(mx, my, 3.5, (160, 60, 10), 0.9)
        b.marker(mx, my, "dot", BEACON_RG, 1)
    sub = min(b25.NOTCHES, key=lambda nt: abs(b25.time_at(nt["scroll"]) - 78) + (nt["side"] != "left") * 99)
    sx, sy = l25_map_xy(b25.time_at(sub["scroll"]), float(b25.foot(0, sub["scroll"])))
    b.ring(sx - 4, sy, 6, colour=AMBER, alpha=230)
    tag(b, sx + 8, sy - 4, "SURVEY SUB?", AMBER)
    for t, label in ((0, "1 MOUTH"), (22, "2 THE WALLS"), (60, "3 CAVE MOUTHS"), (95, "4 THE NARROWS"),
                     (130, "5 THE SONG"), (160, "6 THE FLOOR")):
        _, y = l25_map_xy(t, 0)
        if t:
            b.line([(mx0, y), (mx1, y)], CYAN_DIM, 1, 70)
        tag(b, mx1 - 4, y + 3, label, CYAN, right=True)
    b.label(mx1 + 8, 30, "N", WHITE)
    b.arrow(mx1 + 11, 58, mx1 + 11, 42, WHITE, 4, dashed=False)
    px = 470
    b.label(px, 30, "THE TRENCH", WHITE)
    b.line([(px, 42), (W - 12, 42)], CYAN_DIM, 1, 120)
    b.label(px, 52, "WALLS HURT:", CYAN)
    b.label(px, 62, "260 PX, 190 AT", CYAN)
    b.label(px, 72, "THE NARROWS", CYAN)
    b.label(px, 90, "SIREN NESTS", TEAL_V)
    big(b, px, 102, "31", TEAL_V)
    b.label(px + 28, 106, "ON BOTH WALLS", TEAL_V)
    b.label(px, 126, "SIDE CAVES:", WHITE)
    b.label(px, 136, "ENEMIES SWIM OUT", WHITE)
    b.label(px, 154, "RED-GOLD BEACONS", BEACON_RG)
    b.label(px, 164, "ON THE WALLS", BEACON_RG)
    b.label(px, 182, "CROSS IT", GREEN)
    b.label(px, 192, "END TO END", GREEN)
    return b


def sonar_ring():
    """L25 p2: Varga's diagram of the Siren's ring: the nest on a wall with its sac glowing (the production
    sprite), two rings expanding from it with the gap between them, the ship inside the outer band with the sonar
    shimmer (slowed to 60 % for 2 s), the 3 s immunity after it on a speed strip; the rules beside it."""
    b = Board()
    b.base = b.base * 0.8 + np.array((1, 12, 20.0)) * 0.6
    b.title("CDF SCAN - SIREN, THE SONAR RING")
    b.label(W - 10, 9, "DR. VARGA", CYAN_DIM, right=True)
    wx = 40                                                    # the wall at the left, its foot at x = wx
    wall = (b.xx < wx + 4 * np.sin(b.yy / 13.0)) & (b.yy > 24) & (b.yy < 230)
    n = raster.fbm(W, H, 16, 2651, octaves=3, period=False)
    b.base = np.where(wall[..., None], np.array((22, 38, 50.0)) + 20 * (n[..., None] - 0.5), b.base)
    b.line([(wx + 4 * np.sin(y / 13.0), y) for y in range(24, 231, 2)], CYAN, 1, 230)
    sx, sy = wx + 14, 128
    try:
        nest = held_frame("siren_4", 1.5, 48)
        b.sprite(nest, sx, sy)
        gl = np.array(scaled(held_asset("siren-glow_4"), 1.5, 32)).astype(float)
        b.add_light(gl[..., :3] * gl[..., 3:4] / 255, sx - gl.shape[1] / 2, sy - gl.shape[0] / 2, "over")
    except FileNotFoundError:
        b.marker(sx, sy, "dot", TEAL_V, 6)
    cx, cy = sx + 14, sy
    for r, a in ((70, 230), (165, 200)):                       # two rings: the second 1.5 s behind (180 px)
        th = np.linspace(-np.pi / 2 + 0.06, np.pi / 2 - 0.06, 120)
        for k, off in enumerate((0, -6)):
            rr = r + off + 2.2 * np.sin(th * r / 9)
            pts = [(cx + q * np.cos(t), cy + q * np.sin(t)) for t, q in zip(th, rr)]
            b.line(pts, TEAL_V, 2 if k == 0 else 1, a if k == 0 else a // 2)
    b.arrow(cx + 70, cy - 8, cx + 100, cy - 8, TEAL_V, 3, dashed=True)
    tag(b, cx + 74, cy - 24, "GAP", WHITE)
    tag(b, cx + 4, 30, "120 PX/S TO 230 PX, NO DAMAGE", TEAL_V)
    shp = turned(asset("ship_2"), 0)
    ax = cx + 165 * np.cos(0.5)
    ay = cy + 165 * np.sin(0.5)
    b.sprite(scaled(shp, 0.8), ax, ay)
    for k in range(3):
        b.ring(ax, ay, 14 + 5 * k, 12 + 4 * k, colour=(140, 255, 210), alpha=200 - 50 * k)
    tag(b, ax + 24, ay - 4, "SLOWED 60 %, 2 S", (140, 255, 210))
    b.ring(sx, sy, 15, colour=RED, alpha=230)
    tag(b, sx - 8, sy + 44, "SHOOT THE SAC", RED)
    # the speed strip: 100 % -> 60 % for 2 s -> 3 s immune -> 100 %
    x0, x1, y0 = 330, 450, 200
    b.label(x0, y0 - 24, "YOUR SPEED", WHITE)
    seg = [(0, 0.15, 1.0, CYAN), (0.15, 0.48, 0.6, (140, 255, 210)), (0.48, 0.98, 1.0, GREY)]
    for a, c, v, col in seg:
        b.line([(x0 + (x1 - x0) * a, y0 - 14 * v), (x0 + (x1 - x0) * c, y0 - 14 * v)], col, 2, 240)
    b.line([(x0, y0 + 2), (x1, y0 + 2)], CYAN_DIM, 1, 160)
    tag(b, x0 + 18, y0 + 8, "2 S", (140, 255, 210))
    tag(b, x0 + 66, y0 + 8, "3 S IMMUNE", GREY)
    px = 470
    b.label(px, 30, "THE SIREN", WHITE)
    b.line([(px, 42), (W - 12, 42)], CYAN_DIM, 1, 120)
    b.label(px, 52, "THE SAC GLOWS", TEAL_V)
    b.label(px, 62, "0.8 S, THEN SINGS", TEAL_V)
    b.label(px, 80, "A RING SLOWS YOU", WHITE)
    b.label(px, 90, "ONCE, THEN 3 S", WHITE)
    b.label(px, 100, "OF IMMUNITY", WHITE)
    b.label(px, 118, "SLIP THE GAP", GREEN)
    b.label(px, 128, "OR SHOOT THE SAC", GREEN)
    b.label(px, 146, "SIDE AND", GREEN)
    b.label(px, 156, "ANTI-SUB GUNS", GREEN)
    return b


SOURCE_M8F = artkit.source_note(SCRIPT, "M8 part F batch")
VENT_RED = (255, 80, 40)
GOLD_P = (255, 196, 64)


def l26_map_xy(t, x):
    """Level 26's (t s, play-field x px) on Okafor's chart: north (the level's start) up."""
    return 24 + x * 400 / 480, 30 + t / 182 * 198


def blackwater():
    """L26 p1: Okafor's chart of the Blackwater, north up: the trench's mouth and its last beacons at the top, then
    nothing on the charts (no sonar return, the CDF's survey buoys lost off the chart's edges, crossed out), a dotted
    route down the middle, and far to the south the vent field's red glow."""
    b = Board()
    b.base = b.base * 0.55
    mx0, mx1, my0, my1 = 24, 424, 26, H - 8
    inside = (b.xx > mx0) & (b.xx < mx1) & (b.yy > my0) & (b.yy < my1)
    n = raster.fbm(W, H, 32, 2771, octaves=4, period=False)
    b.base = np.where(inside[..., None], np.array((2, 7, 14.0)) + 6 * (n[..., None] - 0.5), b.base)
    # the trench's mouth at the top: two wall lines opening out to the chart's edges
    for side in (0, 1):
        pts = [l26_map_xy(t, (150 - 15 * t) if side == 0 else (330 + 15 * t)) for t in np.linspace(0, 10, 6)]
        b.line(pts, CYAN, 1, 220)
    for x, t in ((64, 2.4), (420, 3.6)):
        mx, my = l26_map_xy(t, x)
        b.glow(mx, my, 3.5, (160, 60, 10), 0.9)
        b.marker(mx, my, "dot", BEACON_RG, 1)
    # the drowned pipeline and its lamp posts (section 1)
    p0, p1 = l26_map_xy(0, 150), l26_map_xy(20, 150)
    b.line([p0, p1], GREY, 1, 200)
    for k in range(6):
        mx, my = l26_map_xy(1 + k * 2.6, 168)
        b.glow(mx, my, 3, (150, 110, 40), 0.9)
        b.marker(mx, my, "dot", AMBER, 1)
    tag(b, l26_map_xy(4, 140)[0] - 4, l26_map_xy(12, 0)[1] - 4, "LAST LAMPS", AMBER, right=True)
    # no sonar return between: a faint hatch, the label
    band = inside & (b.yy > l26_map_xy(24, 0)[1]) & (b.yy < l26_map_xy(160, 0)[1])
    hatch = band & (np.mod(b.xx + b.yy, 14) < 1)
    b.base = np.where(hatch[..., None], b.base + np.array((6, 18, 26.0)), b.base)
    tag(b, 150, l26_map_xy(70, 0)[1], "NO SONAR RETURN", CYAN_DIM)
    tag(b, 168, l26_map_xy(70, 0)[1] + 12, "NOTHING CHARTED", CYAN_DIM)
    # the CDF's survey buoys, lost off the chart's edges
    for t, x in ((34, 30), (58, 410), (96, 26), (128, 418)):
        mx, my = l26_map_xy(t, x)
        b.marker(mx, my, "square", GREY, 3)
        b.marker(mx, my, "cross", RED, 3)
    tag(b, l26_map_xy(34, 30)[0] + 8, l26_map_xy(34, 30)[1] - 4, "CDF BUOYS: NO SIGNAL", GREY)
    # the vent field far to the south
    for k, (x, t, r) in enumerate(((140, 176, 10), (210, 172, 14), (290, 178, 11), (360, 174, 9), (250, 181, 12))):
        mx, my = l26_map_xy(t, x)
        b.glow(mx, my, r, (230, 40, 8), 1.4)
        b.marker(mx, my, "dot", VENT_RED, 2)
    tag(b, l26_map_xy(176, 380)[0], l26_map_xy(166, 0)[1], "VENT GLOW", VENT_RED)
    # the route: a dotted line straight down the middle
    pts = [l26_map_xy(t, 240 + 10 * np.sin(t / 30)) for t in np.linspace(0, 170, 30)]
    b.dashed(pts, TEAL_V, 1, dash=3, gap=5)
    b.arrow(*pts[-2], *pts[-1], TEAL_V, 4, dashed=False)
    sx, sy = l26_map_xy(4, 240)
    b.marker(sx, sy, "square", CYAN, 3)
    tag(b, sx + 22, sy + 4, "LANCER", CYAN)
    rx, ry = l26_map_xy(4, 272)
    b.marker(rx + 12, ry + 6, "square", CYAN_DIM, 2)
    b.title("CDF CHART - THE BLACKWATER, NORTH UP")
    b.label(W - 10, 9, "CMDR. OKAFOR", CYAN_DIM, right=True)
    b.label(mx1 + 8, 30, "N", WHITE)
    b.arrow(mx1 + 11, 58, mx1 + 11, 42, WHITE, 4, dashed=False)
    px = 470
    b.label(px, 30, "THE BLACKWATER", WHITE)
    b.line([(px, 42), (W - 12, 42)], CYAN_DIM, 1, 120)
    b.label(px, 52, "NO LAMPS,", CYAN)
    b.label(px, 62, "NO CHARTS", CYAN)
    b.label(px, 80, "THE VRELL ROUTE", WHITE)
    b.label(px, 90, "RUNS THROUGH IT", WHITE)
    b.label(px, 108, "FLY ON YOUR", AMBER)
    b.label(px, 118, "HEADLIGHT", AMBER)
    b.label(px, 136, "KEEP ROOK CLOSE", CYAN)
    b.label(px, 164, "THE VENTS", VENT_RED)
    b.label(px, 174, "AT THE FAR END", VENT_RED)
    b.label(px, 192, "CROSS IT", GREEN)
    return b


def lure_scan():
    """L26 p2: Varga's diagram of the Glow Angler in the dark: the angler as the dark shows it (its glow only: the
    teal bead on its stalk, the crimson eyes; the body a faint rim, tools/art/glow_angler.py), a warm gold pickup
    beside it for contrast, the ship's headlight cone from below, the 130 px lunge radius and the ring it fires."""
    b = Board()
    b.base = b.base * 0.35
    b.title("CDF SCAN - GLOW ANGLER, THE LURE")
    b.label(W - 10, 9, "DR. VARGA", CYAN_DIM, right=True)
    k = 0.7                                                    # the diagram's scale (px on the display per field px)
    ax, ay = 150, 70                                           # the angler (heading 0: facing down)
    sx, sy = 196, 228                                          # the ship, below
    # the headlight cone from the ship (a faint wedge under the lines), its edge
    ang = np.degrees(np.arctan2(b.xx - sx, -(b.yy - sy)))
    r = np.hypot(b.xx - sx, b.yy - sy)
    wedge = (np.abs(ang) < 35) & (r < 260 * k) & (b.yy < sy)
    b.base = np.where(wedge[..., None], b.base + np.array((10, 16, 18.0)), b.base)
    for s in (-1, 1):
        a = np.radians(35 * s)
        b.line([(sx, sy - 10), (sx + np.sin(a) * 260 * k, sy - np.cos(a) * 260 * k)], CYAN_DIM, 1, 140)
    tag(b, sx + 70, sy - 40, "HEADLIGHT", CYAN_DIM)
    try:
        body = held_asset("glow-angler_0")
        dim = np.array(body).astype(np.float64)
        dim[..., :3] *= 0.22                                   # the 15 % rim in the dark
        b.sprite(held_frame_img(Image.fromarray(dim.astype(np.uint8), "RGBA"), 24), ax, ay)
        for name in ("glow-angler-glow_0", "glow-angler-stalk_0"):
            g = np.array(held_asset(name)).astype(np.float64)
            b.add_light(g[..., :3] * g[..., 3:4] / 255, ax - g.shape[1] / 2, ay - g.shape[0] / 2, "over")
        piv = json.loads((ROOT / "assets" / "pivots" / "glow-angler.json").read_text(encoding="utf-8"))
        lx, ly = ax + piv["lure"][0][0] - 40, ay + piv["lure"][0][1] - 40
        lure = np.array(held_asset("glow-angler-lure_4")).astype(np.float64)
        b.add_light(lure[..., :3] * lure[..., 3:4] / 255, lx - 12, ly - 12, "over")
    except FileNotFoundError:
        b.marker(ax, ay, "dot", TEAL_V, 3)
        lx, ly = ax, ay + 34
    b.ring(ax, ay, 130 * k, colour=RED, alpha=200)
    tag(b, ax - 30, ay + 130 * k + 6, "130 PX: IT LUNGES", RED, right=True)
    d = np.array((ax - sx, ay - sy), float)
    d /= np.linalg.norm(d)
    ex, ey = sx + d[0] * 60 * k + 0, sy - 8 + d[1] * 60 * k      # the lunge stops 60 px short of the ship: its ring
    b.arrow(ax + d[0] * -40, ay + 40, ex + d[0] * 20, ey + d[1] * 20, RED, 4, dashed=True)
    for j in range(8):
        a = TAU_B * j / 8
        b.marker(ex + 16 * np.cos(a), ey + 16 * np.sin(a), "dot", (255, 110, 200), 2)
    tag(b, ex + 22, ey - 4, "THE RING OF 8", (255, 110, 200))
    tag(b, lx - 12, ly - 4, "TEAL ON A STALK:", TEAL_V, right=True)
    tag(b, lx - 12, ly + 6, "NOT A PICKUP", TEAL_V, right=True)
    # a real pickup beside it, warm gold
    gx, gy = 300, 96
    b.glow(gx, gy, 10, (120, 80, 10), 1.2)
    try:
        pk = held_frame("pickup-salvage-small_0", 1.0, 32)
        b.sprite(pk, gx, gy)
    except FileNotFoundError:
        b.marker(gx, gy, "dot", GOLD_P, 4)
    tag(b, gx + 18, gy - 4, "PICKUP: GOLD", GOLD_P)
    shp = held_frame("ship-hydro_2", 0.8, 48) if (SPRITES / "ship-hydro_2.png").exists() else scaled(asset("ship_2"), 0.8)
    b.sprite(shp, sx, sy - 8)
    px = 470
    b.label(px, 30, "THE GLOW ANGLER", WHITE)
    b.line([(px, 42), (W - 12, 42)], CYAN_DIM, 1, 120)
    b.label(px, 52, "YOU SEE ONLY", CYAN)
    b.label(px, 62, "WHAT GLOWS", CYAN)
    b.label(px, 80, "A TEAL BEAD ON", TEAL_V)
    b.label(px, 90, "A STALK IS VRELL", TEAL_V)
    b.label(px, 108, "TOO CLOSE: IT", RED)
    b.label(px, 118, "LUNGES AND FIRES", RED)
    b.label(px, 128, "A RING", RED)
    b.label(px, 146, "PICKUPS GLOW GOLD", GOLD_P)
    b.label(px, 164, "BULLETS ALWAYS SHOW", WHITE)
    b.label(px, 182, "SONAR PULSE:", GREEN)
    b.label(px, 192, "ALL LIT FOR 6 S", GREEN)
    return b


TAU_B = 2 * np.pi


IMAGES = {
    "act-1-tether-gate": tether_gate,
    "act-1-outer-stations": outer_stations,
    "act-1-l1-strike": l1_strike,
    "act-1-aegis-wing": aegis_wing,
    "act-1-stormhawk": stormhawk,
    "level-01-gagarin-yards": gagarin_yards,
    "level-01-vrell-scan": vrell_scan,
    "level-02-burning-yards": burning_yards,
    "level-02-yield-signal": yield_signal,
    "level-03-spore-lanes": spore_lanes,
    "level-03-spore-echo": spore_echo,
    "level-04-convoy-road": convoy_road,
    "level-04-walker-scan": walker_scan,
    "level-05-crater-nest": crater_nest,
    "level-05-mortar-scan": mortar_scan,
    "level-06-daedalus-rim": daedalus_rim,
    "level-06-edge-scan": edge_scan,
    "level-07-l1-carrier": l1_carrier,
    "level-07-overhead-scan": overhead_scan,
    "act-1-outro-carcass": outro_carcass,
    "act-1-outro-daedalus-rim": outro_daedalus,
    "act-1-outro-second-fleet": outro_second_fleet,
    "act-1-outro-rook": outro_rook,
    "act-2-landfall": landfall,
    "act-2-front-lines": front_lines,
    "act-2-over-home": over_home,
    "act-2-scramble": scramble,
    "level-08-nova-lagos": nova_lagos,
    "level-08-walker-scan": creeper_walker_scan,
    "level-09-arcology-district": arcology_district,
    "level-09-node-scan": node_scan,
    "level-10-evacuation-route": evacuation_route,
    "level-10-wraith-scan": wraith_scan,
    "level-11-convoy-route": convoy_route,
    "level-11-sub-scan": sub_scan,
    "level-12-storm-front": storm_front,
    "level-12-sonde-scan": sonde_scan,
    "level-13-relay-grid": relay_grid,
    "level-13-ice-field": ice_field,
    "level-14-spire-over-geneva": spire_over_geneva,
    "level-14-root-scan": root_scan,
    "act-3-arrival": arrival,
    "act-3-mars-front": mars_front,
    "act-3-colonists": colonists,
    "act-3-drop": drop,
    "level-15-descent-route": descent_route,
    "level-15-burrower-scan": burrower_scan,
    "level-16-canyon-route": canyon_route,
    "level-16-tendril-scan": tendril_scan,
    "level-17-ares-domes": ares_domes,
    "level-17-choir-herald": herald_scan,
    "level-18-storm-front": storm_front_l18,
    "level-18-visibility": visibility_l18,
    "level-19-tharsis-works": tharsis_works,
    "level-19-rear-arcs": rear_arcs,
    "level-20-filed-plate": filed_plate,
    "level-20-hellas-fallback": hellas_fallback,
    "level-21-hellas-basin": hellas_basin,
    "level-21-colossus-scan": colossus_scan,
    "act-4-arrival": arrival4,
    "act-4-europa-cutaway": europa_cutaway,
    "act-4-hydro-kit": hydro_kit,
    "act-4-drop": drop4,
    "level-22-conamara-map": conamara_map,
    "level-22-drill-rig": rig_scan,
    "level-23-drill-shaft": drill_shaft,
    "level-23-eel-and-fry": eel_and_fry,
    "level-24-abyssal-plain": abyssal_plain,
    "level-24-abyss-ray": abyss_ray_scan,
    "level-25-siren-trench": siren_trench,
    "level-25-sonar-ring": sonar_ring,
    "level-26-blackwater": blackwater,
    "level-26-lure": lure_scan,
}

# Review sheets per batch: round, the images on it, the batch name.
BATCHES = {
    "r13": (list(IMAGES)[:9], "UI BATCH"),
    "r20": (list(IMAGES)[9:13], "M4 BRIEFING IMAGES"),
    "r21": (list(IMAGES)[13:15], "M4 PART E"),
    "r23": (list(IMAGES)[15:17], "M4 PART F"),
    "r25": (list(IMAGES)[17:23], "M4 PART G"),
    "r30": (list(IMAGES)[23:29], "M5 PART B"),
    "r31": (list(IMAGES)[29:31], "M5 PART C"),
    "r32": (list(IMAGES)[31:33], "M5 PART D"),
    "r33": (list(IMAGES)[33:35], "M5 PART E"),
    "r34": (list(IMAGES)[35:37], "M5 PART F"),
    "r35": (list(IMAGES)[37:39], "M5 PART G"),
    "r36": (list(IMAGES)[39:41], "M5 PART H"),
    "r38": (list(IMAGES)[41:47], "M7 PART B"),
    "r39": (list(IMAGES)[47:49], "M7 PART C"),
    "r40": (list(IMAGES)[49:51], "M7 PART D"),
    "r41": (list(IMAGES)[51:53], "M7 PART E"),
    "r42": (list(IMAGES)[53:56], "M7 PART F"),
    "r43": (list(IMAGES)[56:57], "M7 PART G"),
    "r44": (list(IMAGES)[57:59], "M7 PART H"),
    "r51": (list(IMAGES)[59:65], "M8 PART B"),
    "r52": (list(IMAGES)[65:67], "M8 PART C"),
    "r53": (list(IMAGES)[67:69], "M8 PART D"),
    "r54": (list(IMAGES)[69:71], "M8 PART E"),
    "r55": (list(IMAGES)[71:73], "M8 PART F"),
}


def build(names):
    OUT.mkdir(parents=True, exist_ok=True)
    for name in names:
        source = (SOURCE if name in BATCHES["r13"][0] else SOURCE_M5 if name in BATCHES["r30"][0]
                  else SOURCE_M5C if name in BATCHES["r31"][0] else SOURCE_M5D if name in BATCHES["r32"][0]
                  else SOURCE_M5E if name in BATCHES["r33"][0]
                  else SOURCE_M5F if name in BATCHES["r34"][0]
                  else SOURCE_M5G if name in BATCHES["r35"][0]
                  else SOURCE_M5H if name in BATCHES["r36"][0]
                  else SOURCE_M7B if name in BATCHES["r38"][0]
                  else SOURCE_M7C if name in BATCHES["r39"][0]
                  else SOURCE_M7D if name in BATCHES["r40"][0]
                  else SOURCE_M7E if name in BATCHES["r41"][0]
                  else SOURCE_M7F if name in BATCHES["r42"][0]
                  else SOURCE_M7G if name in BATCHES["r43"][0]
                  else SOURCE_M7H if name in BATCHES["r44"][0]
                  else SOURCE_M8B if name in BATCHES["r51"][0]
                  else SOURCE_M8C if name in BATCHES["r52"][0]
                  else SOURCE_M8D if name in BATCHES["r53"][0]
                  else SOURCE_M8E if name in BATCHES["r54"][0]
                  else SOURCE_M8F if name in BATCHES["r55"][0]
                  else SOURCE_M4)
        artkit.save_png(IMAGES[name]().image(), OUT / f"{name}.png", source)
    print(f"{len(names)} briefing images in {OUT.relative_to(ROOT)}")


def review(rnd):
    names, batch = BATCHES[rnd]
    cols = 2
    rows = -(-len(names) // cols)
    sheet = raster.sheet(16 + cols * (W + 16), 56 + rows * (H + 30),
                         f"BRIEFING IMAGES (FINAL {rnd.upper()}): ONE PER PAGE",
                         f"PRODUCTION ART, {batch} - {rnd.upper()}")
    raster.draw_text(sheet, 16, 38, f"{len(names)} IMAGES {W}X{H} AT 1X, AS THE BRIEFING SCREEN DRAWS THEM ABOVE THE TEXT",
                     raster.LABEL)
    for i, name in enumerate(names):
        img = Image.open(OUT / f"{name}.png").convert("RGBA")
        x, y = 16 + (i % cols) * (W + 16), 54 + (i // cols) * (H + 30)
        sheet.alpha_composite(img, (x, y))
        raster.draw_text(sheet, x, y + H + 6, f"{name.upper()} ({artkit.colour_count([img])} COLOURS)", raster.LABEL_DIM)
    path = CONCEPT / f"briefing-images-final-{rnd}-a.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")


if __name__ == "__main__":
    args = [a for a in sys.argv[1:] if a != "--review"]
    rounds = [a for a in args if a in BATCHES]
    names = [a for a in args if a not in BATCHES]
    if "--review" not in sys.argv[1:]:
        build(names or list(IMAGES))
    for rnd in rounds or [ROUND]:
        review(rnd)
