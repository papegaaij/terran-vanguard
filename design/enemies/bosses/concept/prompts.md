# Bosses — concept prompts

## brood-carrier-r03-a

**Brood Carrier (Act 1 boss)** — a living carrier: segmented violet chitin hull, armoured dorsal plates, eight launch bays with glowing sac weak points along the flanks, a central core under a six-plate iris, flight membranes, a head with mandible turrets and eye clusters, trailing tendrils.

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background, like Raptor: Call of the Shadows or Tyrian 2000. sleek biomechanical alien, smooth elongated glossy violet chitin (#a020a8 to #d050d0, deep shadows #40004a), thin glowing teal (#00ff9a) seam along the spine, bright pink (#ff40ff) glowing eye as weak point. A colossal living alien carrier seen from directly above, about 2.2 times longer than wide, segmented body with armoured dorsal plates, four launch bays per flank with pulsing pink sacs, a glowing teal core in the centre behind a closing iris of chitin plates, membranous flight fins along the flanks, head with mandibles and pink eyes at the bottom, tendrils trailing at the top. Boss sprite, weak points glow brightest.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, three-quarter view, side view, text, watermark, busy background, anti-aliased soft edges, motion blur, caterpillar, cute, cartoon.
- **Mockup generator:** `python3 tools/concept/enemies_r03.py brood-carrier` (models in `tools/concept/render/enemy_models.py`)

# Round 04 — role colours

## brood-carrier-r04-a

**Brood Carrier, round 04 role colours** — teal-black chitin, teal glow.

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. Biomechanical alien Vrell creature: colossal living carrier, segmented hull with dorsal plates, launch bays with sacs along the flanks, a core behind a chitin iris, flight membranes, head with mandibles at the bottom. Glossy teal-black chitin (#1e5c5a, shadows #06201e, spikes #8ab8a8), bioluminescent teal glow (#00ff9a) in seams, veins and eyes; weak points glow brightest in #a8ff2a (contrasting). Facing down.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, text, watermark, magenta or hot-pink glow, yellow glow, orange glow, blue or cyan body colour.
- **Mockup generator:** `python3 tools/concept/enemies_r04.py brood-carrier`

# Round 06 — Gorgon Frigate, Harbour Kraken, Siege Spire

All three follow the round 04 role colours and one boss convention: **weak points glow lime
(#a8ff2a)**, like the Brood Carrier. Each item has a sheet (PNG) and a motion study (GIF).

## gorgon-frigate-r06-a

**Gorgon Frigate (Act 1 mid-boss, L05)** — a medusa-bell warship with three serpent necks; each
neck ends in a cobra-like turret head that aims on its own. Turrets die separately, then the
crown petals open over the core.

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background, like Raptor: Call of the Shadows or Tyrian 2000. A biomechanical alien warship shaped like a jellyfish bell seen from above: domed hull of glossy bone-ivory chitin (#dccfb4, shadows #5c4a5c, highlights #f4ecd8) with dark radial ribs, a scalloped rim of glowing bulbs (violet #9a4dff), a five-petal bone crown in the centre hiding a lime (#a8ff2a) core, a short veil of tendrils trailing at the top. Three long segmented serpent necks emerge from sockets (one at the front, two at the rear flanks), each ending in a wedge-shaped cobra head with a low hood, a hinged jaw around a violet glowing gun mouth and small lime eyes. Facing down, about 230 px across plus necks.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, text, watermark, insect wings, moth, beetle, cute, cartoon, magenta or hot-pink glow, yellow glow, orange glow, blue or cyan body colour.
- **Animation brief:** necks are 5-segment chains (16 headings per segment) that bend from their socket toward the player and wave; heads (32 headings) open the jaw 0.3 s before a 3-shot aimed burst. Destroyed turrets leave a torn stump leaking violet ichor; the last turret fires faster; when all three are gone the petals part and the core fires ring bursts. Air layer, shadow (21, 30).
- **Mockup generator:** `python3 tools/concept/bosses_r06.py gorgon` (models in `tools/concept/render/boss_models.py`)

## harbour-kraken-r06-a

**Harbour Kraken (Act 2 mid-boss, L11)** — a real cephalopod wrapped round an offshore UTC
harbour platform; arms rise from the water along lanes to slam, the head surfaces to fire.

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy wet specular highlights, crisp silhouette. A giant alien octopus seen from directly above in dark night-time ocean water: bulbous mottled rust-red mantle (#b04a2c, shadows #3a1008, pale #e0b890 suckers) covered in small papillae, a glowing crimson (#ff3038) seam along the back and a crimson lit beak, two large lime (#a8ff2a) eyes, eight thick sucker-lined arms; four arms coil over the edges of a square blue-grey offshore platform with a helipad, shipping containers, a crane and a torn corner; one arm lies stretched along a straight lane toward the bottom of the image with splash rings. Submerged parts are dark, blue-tinted and blurred. Facing down.
- **Negative prompt:** photograph, painterly brush strokes, perspective camera, side view, text, watermark, squid with fins, cute, cartoon, insect, crab, magenta glow, yellow glow, orange glow, bright turquoise water.
- **Animation brief:** arms are segment chains (13 segments + curled tip, 32 headings). A lane flashes with a red dashed telegraph 0.8 s ahead, the arm's shadow moves under the waves, then the arm rises along the lane (segments surface from base to tip in 0.3 s), slams with splash rings, holds 0.7 s and sinks. Between slams the head surfaces (foam ring), opens its eyes (weak points) and fires 7-orb fans from the beak, then submerges. Submerged sprites are tinted toward the sea colour, faded to ~55 % and softened (sub layer).
- **Mockup generator:** `python3 tools/concept/bosses_r06.py kraken`

## siege-spire-r06-a

**Siege Spire (Act 2 boss, L14)** — a rooted Vrell citadel in the UTC capital: six roots end in
root turrets and acid mortars, the crown's maw launches Wraiths, then the spire tears itself
free and sweeps a laser from the air.

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered top-down with a slight camera-height perspective so the tall structure's top is pushed upward, key light from the top-left, glossy organic surfaces, crisp silhouette. A colossal alien bio-tower growing out of a ruined night-time city: six thick mauve-grey (#7c6878, shadows #241a24) roots spreading over the city blocks with dark knots and faint violet (#9a4dff) veins, each ending in a bulbous turret pod with a petal collar; a ribbed tapering trunk with spiral violet ridges and pale bone spines; at the top a crown of six slate petals ending in curved bone claws with crimson (#ff3038) emitter tips around a glowing lime (#a8ff2a) launch maw. Purple-grey alien creep with glowing veins covers the streets around its base.
- **Negative prompt:** photograph, painterly brush strokes, side view, isometric, text, watermark, starfish, flower, cute, cartoon, magenta glow, yellow glow, orange glow, blue body colour.
- **Animation brief:** root turrets (32 headings) aim and fire aimed orbs; mortar pods open and lob acid blobs with marked impacts; the maw opens to launch cloaked Wraiths that rise toward the camera (scale in) and fly off to circle round. When the root pods die (chain explosions) the roots wither, the spire tears itself out (shockwave, debris), rises — drawn 1.0 → 1.3× with its shadow sliding away and blurring — and sweeps a telegraphed laser from its petal tips across the lower screen. The trunk's roof is pushed away from the screen centre (tall-structure perspective rule).
- **Mockup generator:** `python3 tools/concept/bosses_r06.py spire`

## harbour-kraken-r07-a

**Harbour Kraken (Act 2 mid-boss, L11), round 07 redo** — the same cephalopod, now clearly a
creature that operates from the depths: one connected animal whose arms run under water to its
head, real lit water instead of drawn rings, and top-down surfacing (see the Water rules in
`design/art-direction/README.md`).

- **Prompt:** late-1990s pre-rendered CGI game art, straight top-down orthographic view of a dark night-time ocean, key light and moonlight from the top-left, glossy wet specular highlights, crisp silhouettes. A giant alien octopus lurking just below the surface beside a square blue-grey offshore platform (helipad, shipping containers, crane, a torn corner): its mottled rust-red mantle (#b04a2c, shadows #3a1008) with small papillae and a glowing crimson (#ff3038) seam is seen through the water as a large dark shape, its crown just breaking the surface with a swell and a ragged ring of white foam. Eight thick sucker-lined arms (pale #e0b890 suckers) radiate from the head: four reach up along the sides of the mantle under the water and climb out over the platform's edges onto the deck, each with an irregular churning foam collar and a train of uneven ripples where it leaves the water; one arm lies awash in a straight lane with spray and foam; two trail deep below as faint blurred silhouettes. Submerged parts get bluer, darker, blurrier and slightly wavy with depth, and stay continuous with the parts above the surface. Long moonlit swell bands with sparkling glints cross the whole surface. Two large lime (#a8ff2a) eyes (weak points) only when the head is surfaced.
- **Negative prompt:** photograph, painterly brush strokes, perspective camera, side view, text, watermark, clean geometric circles or outlined rings for waves, cartoon water, flat untextured water, arms floating separately from the body, squid with fins, cute, cartoon, insect, crab, magenta glow, yellow glow, orange glow, bright turquoise tropical water.
- **Animation brief:** the water is a height field — directional swell bands plus every ripple train, V-wake, swell and splash added to it and lit by the same light, so they read as waves; foam and spray are particles. Arms are segment chains (32 headings per segment size) whose segments carry a height above or below the water: submerged segments are drawn through four depth bands (tint, darken, blur, refraction displacement), segments above water are drawn sharp, raised ones larger with their shadow pushed out over the water, and every waterline crossing spawns a foam collar and ripple trains (faster when the arm moves → wakes). The head carries a per-pixel height map: as the water level drops, the crown breaks first (preceded by a rising swell), then the eyes and the lower parts, with foam streaming outward and a fading wet sheen with rivulets; diving reverses the order with foam pulled inward. Slam: the lane churns (boiling foam, small ripples) and flashes a red dashed telegraph while the arm slides into place under water, rises base to tip (the waterline collar runs along it), hangs raised, slams down with spray, mist and large ripple trains, lies awash, then sinks darker and fainter. While surfaced, the lime eyes are open and the crimson beak lights before each 7-orb fan. 10.4 s seamless loop at 15 fps (warm-up cycle so foam and ripples carry over the seam; the mockup GIF holds the swell at 7.5 fps and posterizes the water to stay under 8 MB).
- **Mockup generator:** `python3 tools/concept/kraken_r07.py` (re-uses the round-06 platform and arm models in `tools/concept/render/boss_models.py`)

# Round 36 — Revenant Walker and Dust Colossus (Act 3)

Overnight design phase for Act 3 (2026-10-12), two variants per boss. The Revenant Walker is an
**unmarked human machine**: neutral grey, no trim, no insignia, nothing that names a maker (the
rule for unmarked machines in [enemies](../../README.md)); its weak point follows the Ascendancy
rule (an exposed reactor at the rear, hot amber), not the Vrell lime. The Dust Colossus keeps the
Vrell boss convention (**weak points glow lime, #a8ff2a**). Each item has a sheet (PNG) and a
motion study (GIF) in the 480×540 play field at 1×.

## revenant-walker-r36-a

**Chosen** (round 36, 2026-10-13).

**Revenant Walker A "Strider heavy" (Act 3 mid-boss, L19 Tharsis foundry)** — a reverse-knee
biped with two shoulder missile boxes, a tail flamer turret between fuel tanks and a reactor
grille on its back; the wreck's serial plate is ground blank.

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, metallic specular highlights, crisp silhouette, isolated on a flat dark background, like Raptor: Call of the Shadows or Tyrian 2000. A heavy two-legged war walker seen from directly above: a wedge-shaped armoured torso in plain neutral gunmetal grey (#787d82, shadows #2a2c30, light edges #b0b2b4) with recessed panel lines, a dark sensor visor with two small red lights at the front, two box-shaped missile launchers on the shoulders with 2×3 square cell hatches, a dark spine ridge, a round reactor grille on the back glowing hot amber-orange (#ff7a20) between two pale fuel tanks, a small flamer turret with a long nozzle on the tail; reverse-knee legs with hydraulic rods and big three-toed clawed feet. A small riveted bare-metal plate on the front deck. No markings, no insignia, no coloured trim, no faction colours. Facing down, about 230 px across.
- **Negative prompt:** photograph, painterly brush strokes, perspective camera, side view, text, numbers, logos, insignia, decals, hazard stripes, gold trim, black-and-gold, camouflage, cockpit canopy, pilot, cute, cartoon, anime mecha, bright colours.
- **Wreck / plate brief:** the wreck folds flat onto splayed legs, torso tilted, pods torn off, a crater where the reactor was, scorched hull. Close-up: the serial plate is still riveted on but its face has been ground smooth with an angle grinder — overlapping fine circular swirl marks in bright bare metal where the stamped serial used to be; nothing legible remains (hint 2). The same plate is the subject of the L20 briefing image.
- **Animation brief:** legs 8-frame walk cycle (16 facings in the game), feet plant on the ground layer and leave footprints; torso drawn over the legs with its own shadow; missile boxes light their cells (dim red) 0.4 s before a salvo of three homing missiles each (grey body, red nose light, orange exhaust, smoke trail); each box dies separately (explosion, smoke, torn stub). The flamer turret (32 headings, ±60° about the rear) swings toward the ship once it is behind the walker, blinks its pilot light 0.5 s before a burst and burns a cone of flame puffs (white-yellow core → orange → red → smoke). Reactor death: chain of blasts, the walker drops into the wreck sprite; the scroll then eases in slowly past the wreck and the bare plate glints once. 12 s at 20 fps; the scroll holds during the fight (boss lock), the ground steps at 5 Hz afterwards (GIF size).
- **Mockup generator:** `python3 tools/concept/bosses_r36.py walker-a` (models in the generator)

## revenant-walker-r36-b

**Rejected** (round 36, 2026-10-13: a picked); the files are in `rejected/`.

**Revenant Walker B "Siege crawler" (Act 3 mid-boss, L19)** — a low four-legged armoured hull
with a dozer prow, two vertical-launch missile blocks, a twin-nozzle flamer barbette on the rear
deck and a reactor under radiator fins at the tail; the wreck's plate is cut off with a torch.

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, metallic specular highlights, crisp silhouette, isolated on a flat dark background. A four-legged armoured siege walker seen from directly above: a long low chamfered hull in plain neutral gunmetal grey (#787d82, shadows #2a2c30, light edges #b0b2b4) with panel lines, an angled dark dozer prow and a sensor slit at the front, two vertical-launch missile blocks with 2×4 square hatches either side of a dark spine, a round flamer barbette with twin nozzles on the rear deck, a reactor bay glowing hot amber-orange (#ff7a20) under four dark radiator fins at the tail; four jointed spider legs from the hull corners with high knees, hydraulic rods and round foot pads. A small riveted bare-metal plate on the right front deck. No markings, no insignia, no coloured trim. Facing down, about 260 px across.
- **Negative prompt:** photograph, painterly brush strokes, perspective camera, side view, text, numbers, logos, insignia, decals, hazard stripes, gold trim, black-and-gold, camouflage, cockpit canopy, insect, organic, cute, cartoon, bright colours.
- **Wreck / plate brief:** the wreck slumps flat with its legs splayed, the launch blocks blown out and scorched, a crater over the reactor. Where the serial plate was there is only a scorched rectangle of bare scraped metal with four sheared rivet stubs at its corners — the plate was cut off on purpose (hint 2); nothing legible.
- **Animation brief:** as A, with 4 legs stepping in diagonal pairs (8 frames); the launch cells light dim red 0.4 s before a salvo; missiles pop straight up out of the cells (drawn growing from 45 % to full size over 0.35 s) before turning and homing; the barbette turns toward the ship once it is behind the crawler (pilot lights blink 0.5 s before the burst).
- **Mockup generator:** `python3 tools/concept/bosses_r36.py walker-b`

## dust-colossus-r36-a

**Rejected** (round 36, 2026-10-13: b picked); the files are in `rejected/`.

**Dust Colossus A "Rearing maw" (Act 3 boss, L21 Hellas basin)** — one colony-sized segmented
worm under the dunes; it breaches at the edges of the arena, arches its head into the field and
opens three jaw plates round a lime throat that fires rings and sucks debris in.

- **Prompt:** late-1990s pre-rendered CGI game art, straight top-down orthographic view of a rust-ochre Martian dune field (#7c4a2c, #a26a42, highlights #dcb488) lit from the top-left. A colossal alien sandworm breaching near the left edge of the image: a thick segmented body of glossy mauve-slate chitin (#7c6878, shadows #241a24) with darker joints, a row of pale bone spines (#c8b8c0) along its back and small teal (#00ff9a) lights along its flanks, arching up out of a cracked crater rim of thrown sand; its head rears toward the viewer and opens three heavy jaw plates like a flower around two rings of curved bone teeth and a glowing lime (#a8ff2a) throat. Sand streams off the body, a low dust cloud drifts away, rocks and sand streaks spiral into the open mouth. Behind it a long raised furrow runs under the sand to the edge of the image.
- **Negative prompt:** photograph, painterly brush strokes, perspective camera, side view, horizon, text, watermark, Dune movie still, cute, cartoon, snake head with eyes, earthworm pink, magenta or hot-pink glow, yellow body, blue or cyan body colour, green sand.
- **Animation brief:** the tell: a furrow runs in under the sand from the edge (height-field displacement, lit like the dunes) and a dome bulges and cracks (dark cracks with faint teal light) over 2 s while the edge warning shows 3 s ahead (sides and rear; the front needs none). Breach: sand thrown out (particles), a dust cloud (≤ 40 % opacity), the head rising through four pre-rendered rise states clipped at the sand line, a crater rim forming where the body leaves the sand, its shadow pushed out with height. Open: jaws open over 0.45 s (two open states), the throat lights; three 14-orb rings, one per second; suction pulls rocks (5 sprites) and sand streaks inward along spirals, and the ship drifts toward the mouth. Close, sink back (rise states in reverse), the crater fills over 2 s; the next furrow heads for another edge. Each breach side is its own render (headings 0, 90, 180, 270°) so the light stays top-left.
- **Mockup generator:** `python3 tools/concept/bosses_r36.py colossus-a` (models in the generator; rendered in parallel)

## dust-colossus-r36-b

**Chosen** (round 36, 2026-10-13).

**Dust Colossus B "Sinkhole colony" (Act 3 boss, L21)** — the organism is the basin itself: vein
ridges swell under the sand toward a spot near an edge, the ground collapses into a funnel and
four bone mandibles unfold from its rim around a lime throat.

- **Prompt:** late-1990s pre-rendered CGI game art, straight top-down orthographic view of a rust-ochre Martian dune field (#7c4a2c, #a26a42, highlights #dcb488) lit from the top-left. Near the left edge the sand has collapsed into a deep funnel-shaped sinkhole with spiral slide marks; at its bottom a ring-shaped fleshy collar of pale bone-ivory chitin (#dccfb4, shadows #5c4a5c) surrounds a glowing lime (#a8ff2a) throat lined with two rings of curved teeth; four huge hooked bone mandibles splay outward from the rim across the sand like an antlion's, with eight thin palps tipped with violet (#9a4dff) lights between them. Raised vein ridges run under the sand from all sides toward the pit, cracks glowing faint violet. Rocks and sand streaks spiral down into the funnel.
- **Negative prompt:** photograph, painterly brush strokes, perspective camera, side view, horizon, text, watermark, flower, starfish, spider, cute, cartoon, magenta or hot-pink glow, yellow body, blue or cyan body colour, green sand.
- **Animation brief:** the tell: five vein ridges swell under the sand and converge on the breach point, which bulges and cracks (violet light) over 2 s under the edge warning (3 s ahead). Breach: the sand slumps into a funnel (height-field displacement with spiral ridges sliding inward), sand thrown out, the collar rises through three rise states clipped at the sand, the crossed mandibles unfold (two open states) and the throat lights. Open: three 14-orb rings; rocks and sand streaks spiral into the funnel and the ship drifts toward it. Close: mandibles cross shut, the collar sinks, the funnel fills over 2 s. The model is four-fold symmetric, so one render serves every breach side.
- **Mockup generator:** `python3 tools/concept/bosses_r36.py colossus-b`

# Round 42 — production art (M7 part F)

## revenant-walker-final-r42-a

**Production art, pending the user's review** (step ART-F1, 2026-10-19). The chosen round-36 a
"Strider heavy" straight to production: the concept's legs, torso layout, materials and missile from
`tools/concept/bosses_r36.py` imported unchanged, re-laid to the [walker's spec](../revenant-walker/README.md):
drawn at 72 px per model unit (the concept 92), the round reactor grille split into the **two reactor
vents** (shut by slats; open hot amber; overheated white-orange in phase 3), a **front plate** wrapping
the prow (the `armoured` part) with a short chin cannon under it, the shoulder pods as separate sprites
that turn to their aim (16 headings, the hatch tell with the cells lit red) over mount blocks, a torn stub
for a destroyed pod, the flamer turret in 32 aim headings with an additive pilot light (idle → blue-white
flare, the cone's tell). The death topples the walker forward along its heading in 6 steps (the legs
buckle, the hull pitches forward and rolls onto its left flank, serial-plate side up) and ends in the
wreck that stays (burnt, the reactor blown, the serial plate ground to bare metal: hint 2). 8 body headings
× an 8-frame walk; the missile (16 headings), its grey smoke puffs and the flame-cone puffs (additive,
white-yellow core → orange → red) are 2D/model frames for the game to place.

- **Brief:** as [revenant-walker-r36-a](#revenant-walker-r36-a) (prompt, negative prompt, palette); no markings, no insignia.
- **Generator:** `python3 tools/art/revenant_walker.py` (`--review` rebuilds this sheet and GIF from the
  written frames); the sprite names and frame layout are in the generator's docstring.
- **Fixes (step ART-F3, 2026-10-19):** the open hatch now shows the six missile cells lit red (raised out of
  their recesses in a hot red material of their own; ART-F1's open frames read as grey at play size), so the
  0.4 s tell reads; the topple and wreck frames are rendered at the quality bar's 4x like the other sets
  (ART-F1 had them at 2x). Sheet and GIF regenerated.
