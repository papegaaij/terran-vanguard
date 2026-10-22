# Europa – generator notes and prompts

Round 48 (Act 4 "Deep Water" design phase, step A-europa, 2026-10-21; all pending the user's
review). Every file here is made by `python3 tools/concept/europa_r48.py [subject …] [--no-gif]`
(no subject = everything, about two minutes) on the frozen `tools/concept/render/` package, the
Mars round's sheet helpers (`tools/concept/mars_r36.py`, imported unchanged) and, for Thera Deep,
the chosen round-07 scene (`tools/concept/scenes_r07.py`). Units on the stills are production
sprites of returning units (`assets/sprites/`) and the chosen round-06 Nautilus, for scale only;
the new Act 4 units are on their own round-48 sheets, and the Glow Angler, the bore site and the
carriers here are simple stand-ins. Each PNG is a review sheet with the 480×540 play field at 1×;
each GIF is a 4 s loop at 20 fps. Rules: [art direction](../../../art-direction/README.md) and
[Europa](../README.md) (*Under water rules*, *Darkness*).

The light model: the level's albedo is lit by an ambient map, coloured point lights (lamps,
beacons, vents, sacs) and the ship's headlight, in posterized light steps; the water adds a haze
toward the r07 water colour (#084a5c) and the beam's in-scatter; emissive glows stay bright and
bloom; units take the light map per pixel and keep their glow; enemy bullets are always bright.

Common negative prompt for every still: photograph, painterly, perspective camera, horizon, sun,
daylight under water, text, watermark, blurry.

## europa-ice-shelf-r48-a

**The ice shelf (L22).** File: `europa-ice-shelf-r48-a.png`.

- **Prompt:** late-1990s pre-rendered CGI shoot'em-up backdrop, straight top-down orthographic view
  of Europa's surface: a white-blue ice plain (#e2ecf6 to #42607c) of soft pressure ridges and thin
  blue fissures in hard sunlight from the top-left, an open lead of black water that mirrors
  Jupiter's orange-brown bands, Jupiter's warm light on the right side; a Vrell bore site with a
  bone collar, teal growth (#00ff9a) and steaming radial cracks; an ice geyser bursting from a
  glowing crack into a shaded white steam plume with a soft cast shadow.
- **Layers:** deep Jupiter (seen in the lead) · ground ×1.0 ice, rigs, turrets · low-air ×1.3 steam ·
  high-air ×2 ice crystals and vapour wisps.

## europa-descent-r48-a

**The descent and the waterline (L23, sections 1–2).** File: `europa-descent-r48-a.png`.

- **Prompt:** the same style, top-down down a drilled shaft through the ice: pale ice walls at the
  frame's edges with spiral drill grooves, the shaft receding to a dark centre with a faint warm glow
  far below; a shimmering waterline of foam and bubbles across the shaft; below it dry ice lit from
  the shaft mouth behind, above it the water: darker teal, caustics on the walls, marine snow, the
  headlight's beam; Vrell teal collar strips running up the walls.
- **Layers:** ground ×1.0 walls and the receding shaft · low-air bubbles and foam at the waterline ·
  air the play plane.

## europa-thera-deep-r48-a

**Thera Deep's farms, lamp-lit (L23, section 3; Q2 a).** File: `europa-thera-deep-r48-a.png`.

- **Prompt:** the chosen round-07 kelp forest and farm grids of an under-ice city, but lit only by
  warm grow-lamp arrays (#ffd08c) and the domes' lights: pools of warm light over the racks, the
  water between them dark teal; two Vrell siphon pumps (purple chitin, a teal mouth, teal roots in
  the kelp) on the racks.
- **Layers:** as round 07 (ground, kelp, sub, low-air bubbles, air, high-air) with a light map:
  about 58 % ambient plus the lamp pools.

## europa-abyssal-plain-r48-a

**The abyssal plain at the lamps' edge (L24).** File: `europa-abyssal-plain-r48-a.png`.

- **Prompt:** a wide silt sea floor with ripples, ridges and dark kelp tufts, warm lamp light from
  behind (the bottom edge) falling off to deep blue-black ahead; two Vrell spawning reefs (lumpy
  plum and bone coral, a teal iris at the crown); a grey human relay beacon mast head with a
  red-gold lamp; a huge manta's dark shadow gliding below the play plane, faint lime gill lights.
- **Layers:** ground ×1.0 silt, reefs, beacon, walkers · sub ×1.2 the Abyss Ray's shadow · air.

## europa-siren-trench-r48-a

**The Siren trench (L25).** File: `europa-siren-trench-r48-a.png`.

- **Prompt:** a narrow sea-floor trench from above: ice-rock walls in blue-grey strata with purple
  Vrell growth, a cave notch in the right wall, Siren nests on the wall feet (grey throats with teal
  sacs), red-gold beacons on the left wall, a teal sonar ring crossing the trench; about 14 % blue
  ambient, the headlight cone.
- **Layers:** ground ×1.0 floor, walls, nests, beacons · air the play plane, the ring.

## europa-vents-r48-a

**The hydrothermal vents (L27).** File: `europa-vents-r48-a.png`.

- **Prompt:** a dark basalt vent field from above: black smoker chimneys as dark mineral cones with
  glowing orange mouths, amber bacterial mats (#ff8c28) round them, white tube worms with red tips; a
  heat plume erupting as an amber disc with shimmer rings and rising silt; a grey submarine drone
  wreck nose-down and scorched, a pale hull code showing; dim blue ambient between the vents.
- **Layers:** ground ×1.0 floor, chimneys, mats, the wreck · low-air the plume and silt · air.

## europa-hatchery-r48-a

**The hatchery's brood galleries (L28).** File: `europa-hatchery-r48-a.png`.

- **Prompt:** a dark violet flesh-and-resin floor with long curving galleries of Vrell egg racks:
  pale bone pods in pairs along a dark spine, small teal and violet cores, teal light pools along the
  racks; the vent's red-amber glow rising from the top of the frame; drifting teal spores.
- **Layers:** ground ×1.0 floor and racks · low-air spores · air.

## europa-breakout-r48-a

**The breakout (L28, sections 6–8).** File: `europa-breakout-r48-a.png` (three frames at 1×).

- **Prompt:** (1) climbing the drill shaft under water, light growing from a pale cracked ice
  ceiling above, tiny glass fish fleeing upward; (2) the ice ceiling shattering: a white flash at
  the breach, a pressure ring, pale ice slabs (#c4def0) tumbling outward, spray; (3) Europa's
  surface at night lit orange-brown by Jupiter, the broken hole of black water behind the ship
  mirroring Jupiter, ice slabs round it, dark Vrell carriers with teal lights rising from other
  drill holes in steam.
- **Layers:** high-air the ice ceiling and its cracks · the slabs over everything during the burst ·
  after the breakout the surface rules (no water grade).

## underwater-grade-r48-a / -b

**The under-water grade (Q2).** Files: `underwater-grade-r48-{a,b}.png` (L24 at 1× and the
L23–L28 ladder at 0.4×), `underwater-grade-r48-{a,b}.gif` (L24, 4 s: the Ray's shadow passing,
caustics, marine snow, the beacon's pulse; the floor held).

- **a, lamp-lit to black (recommended):** every level its own light: L23 grow lamps, L24 warm behind
  and dark ahead, L25 about 14 % with beacons and sacs, L26 black (the hard cone), L27 vent amber,
  L28 the hatchery's teal and the vent's red.
- **b, dark everywhere:** ambient 6 % in every under-water level, no caustics, the lamps' pools at
  60 %; only the headlight and the glows light the field.

## darkness-r48-a / -b

**The L26 darkness look (Q4 a: a look only).** Files: `darkness-r48-{a,b}.png` (the field at 1×,
the lure next to a pickup at 2×, Rook's lamp at 2×, the Sonar Pulse on, the light map, the glow-only
air layer), `darkness-r48-{a,b}.gif` (4 s).

- **a, the hard cone:** the spec's 70°/260 px headlight with a crisp edge and a small halo; outside
  it units show only their glow and a 15 % rim.
- **b, the soft lamp pool:** a round pool of about 150 px fading out, and a long narrow faint beam
  that lights the marine snow (back-scatter); units fade in gradually near the ship.
- Both: bullets always bright, pickups warm gold, the Glow Angler's lure a teal bead (stand-in
  angler), Rook's 56 px lamp, everything hittable.

## hydro-kit-r48-a / -b

**The Stormhawk's free hydro-kit.** Files: `hydro-kit-r48-{a,b}.png` (a 256 px render, the five
48 px banking frames next to the stock ones, the kit ship in L24 and in L26 at 1×, a 4× frame with
its wake). Rendered from the chosen hull (`render/models.ship_a_model`, the palette of
`tools/art/stormhawk.py`) with the kit parts added, through the production perspective camera.

- **a, sealed refit:** pump-jet shrouds round the engines, orange ballast saddles on the wing roots,
  dark seal plates over the intakes, twin headlights under the canards, red/green wingtip nav lights;
  a tight two-jet pump wake instead of the flame.
- **b, pontoon kit:** a slim pontoon on each wing (a lamp in its nose, an orange clamp), a sonar
  blister behind the canopy, wide ducted engine shrouds; a wider swirling wake. Cost: the pontoons
  sit where the weapon pods mount.
- **Prompt (both):** late-1990s pre-rendered CGI sprite, top-down, the AF-12 Stormhawk
  (forward-swept wings, light grey hull, blue accents) fitted for under-water flight with the parts
  above; key light from the top-left; transparent background.

## hydro-kit-final-r50-a

Production art (M8 part A batch, step ART-A, for concept round 50), not a mockup: `python3 tools/art/act4_gear.py
hydro` writes `ship-hydro_0..4` and `rook-hydro_0..4` into `assets/sprites/` and this review sheet and GIF. The look is
the one round 48 chose (E10 = a, "sealed refit", [hydro-kit-r48-a](hydro-kit-r48-a.png), the kit parts of
`tools/concept/europa_r48.py kit_model("a")`): pump-jet shrouds, orange ballast saddles on the wing roots, dark seal
plates, twin headlights under the canards and red / green nav lights at the wingtips, on the Stormhawk's five banking
frames through its production camera (the stock pivots and pods apply unchanged), and the same parts moved onto Rook's
ship-C airframe in his Ember scheme. The pump wake and the under-water grade are part C.

Artist brief: keep the round-48 pick's look exactly (pre-rendered models lit from the top left, every
heading or state its own render, never rotated at runtime; one palette per frame set; additive effects
stored premultiplied on black; the player's under-water colours toward cyan / teal-white, away from the
Vrell and Siren teal 00ff9a).
- **Prompt:** see [hydro-kit-r48-a](#hydro-kit-r48-a---b).
- **Negative prompt:** pontoons, changed silhouette, photographic realism, side view, watermark.

## underwater-capture-r50-a

Concept round 50 (M8 part A, step A7), a capture of the game, not generated art: the game built by `:desktop:installDist` (branch `m8-act-4`), run on a private Xvfb display (960×540 window) with `--bench <s> --settings <scratch>/settings.properties --invulnerable` (auto-fire on), `ALSOFT_DRIVERS=wave` writing the game's own mix to a file, the screen recorded with ffmpeg `x11grab`; a small XTest driver sweeps the ship left and right and taps the special at set seconds. The `.mp4` is a cut of the recording with the game's sound (H.264 main, AAC 96 kbps); the `.png` is a frame of it (or two side by side). `--level 20 --medium underwater@10 --loadout front=ion-beam:3,rear=rear-lance:2,left=torpedo-pod:2,right=torpedo-pod:2 --special airstrike:2`; the video is level seconds 3–25, the frames 6.3 s (dry) and 12 s (under water).
