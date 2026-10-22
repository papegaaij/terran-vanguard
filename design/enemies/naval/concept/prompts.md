# Naval enemies — concept prompts

AI image-generator prompts and animation briefs for the naval concept sheets. Palette B, role colours from `design/enemies/README.md`.

# Round 06 — Act 2 introductions and roster additions

## driftjelly-r06-a

**Driftjelly (Vrell floating mine)** — a 40 px radial jellyfish. Files: `driftjelly-r06-a.png` (sheet) and `driftjelly-r06-a.gif` (motion in the 480×540 play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. An alien jellyfish seen from directly above, radially symmetric: a glossy translucent-looking olive-green bell (#7c8c3a lightened) with sixteen glowing lime (#a8ff2a) radial veins, a glowing lime knob at the top, a frilled dark rim and eight pale trailing tentacles spreading out under it.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, robot, mechanical parts, rivets, metal plating, magenta or hot-pink glow, yellow glow, blue or cyan body colour, insect
- **Animation brief:** Static radial sprite, 4 bell-pulse frames (the bell contracts and the tentacles spread). Drifts with the current at the surface (with a ripple ring) or just below it (sub layer: blue-green tint, softer). When the player comes within 96 px the bell flashes and pulses a ring of shots.
- **Mockup generator:** `python3 tools/concept/enemies_r06.py driftjelly` (models `tools/concept/render/r06_models.py`, rendered with `ModelSpaceAngleSprites` from `tools/concept/render/enemy_rigs.py`)

## reef-spitter-r06-a

**Reef Spitter (Vrell barnacle gun on a raft)** — a 36 px gun on an 84 px raft. Files: `reef-spitter-r06-a.png` (sheet) and `reef-spitter-r06-a.gif` (motion in the 480×540 play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. Two sprites seen from directly above: (1) a floating biomass raft, an irregular mat of dark olive kelp lobes (#263010 to #7c8c3a) with glossy air bladders and a seaweed fringe; (2) a plated barnacle gun in mauve-grey slate chitin (#7c6878) with three bone mouths fanned forward, glowing violet (#9a4dff) inside.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, robot, mechanical parts, rivets, metal plating, magenta or hot-pink glow, yellow glow, blue or cyan body colour, boat, ship, insect
- **Animation brief:** The raft bobs and drifts with the scroll (static sprite, small vertical bob). The barnacle turns at 32 headings to aim at the player and fires a 3-way fan from its three mouths (recoil frame, violet muzzle flash).
- **Mockup generator:** `python3 tools/concept/enemies_r06.py reef-spitter` (models `tools/concept/render/r06_models.py`, rendered with `ModelSpaceAngleSprites` from `tools/concept/render/enemy_rigs.py`)

## skimmer-r06-a

**Skimmer (Vrell flying-fish skiff)** — a 40 px fast surface skimmer. Files: `skimmer-r06-a.png` (sheet) and `skimmer-r06-a.gif` (motion in the 480×540 play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. A sleek alien flying fish seen from directly above: a torpedo body in rust-red chitin (#b04a2c) with a pale bone dorsal ridge, two long outrigger pectoral fins skimming the water, a forked tail, a glowing violet (#9a4dff) gland on the snout.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, robot, mechanical parts, rivets, metal plating, magenta or hot-pink glow, yellow glow, blue or cyan body colour, insect, aircraft, boat
- **Animation brief:** 16 headings x 2 tail-beat frames. Skimmers weave between ice floes in sine paths from every edge (left, right, top), leaving a fading foam wake, and fire aimed shots from the snout gland.
- **Mockup generator:** `python3 tools/concept/enemies_r06.py skimmer` (models `tools/concept/render/r06_models.py`, rendered with `ModelSpaceAngleSprites` from `tools/concept/render/enemy_rigs.py`)

## spiral-nautilus-r06-a

**Spiral Nautilus (Vrell shell roller)** — a 56 px coiled-shell cephalopod. Files: `spiral-nautilus-r06-a.png` (sheet) and `spiral-nautilus-r06-a.gif` (motion in the 480×540 play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. Two sprites seen from directly above: (1) a coiled nautilus-like shell lying flat, ivory/bone (#dccfb4) with dark tiger stripes and growth ribs following the spiral, a faint violet (#9a4dff) glowing lip at the aperture; (2) a tentacle crown: a hooded mantle with two violet eyes and a fan of eight dark tentacles tipped with violet light.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, robot, mechanical parts, rivets, metal plating, magenta or hot-pink glow, yellow glow, blue or cyan body colour, snail, insect
- **Animation brief:** The shell rolls (24 spin frames over 360°, stripes turning with it) along spiral-in paths around the player, pairs orbiting in opposite directions. It uncoils - the tentacle crown appears at 16 headings facing the player - and fires a spiral of shots; the shell is armoured, so it must be shot while uncoiled. Under Europa's ice it is lit only by the player's headlight cone and its own bioluminescence.
- **Mockup generator:** `python3 tools/concept/enemies_r06.py spiral-nautilus` (models `tools/concept/render/r06_models.py`, rendered with `ModelSpaceAngleSprites` from `tools/concept/render/enemy_rigs.py`)


## driftjelly-r07-a

**Driftjelly at the waterline (round 07)**: the chosen round-06 Driftjelly, now meeting the water surface properly. The user had asked whether the plain circle around it was a wave. Files: `driftjelly-r07-a.png` (sheet) and `driftjelly-r07-a.gif` (motion in the 480×540 play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic view, key light from the top-left, a floating alien jellyfish mine half-submerged in a deep blue-green ocean seen from directly above. The glossy olive bell (#7c8c3a) with glowing lime (#a8ff2a) radial veins rises out of the water. A broken, uneven ring of white sea foam clings to the waterline and sheds small specks. Its pale tentacles and the lower bell are visible just below the surface, tinted blue-green, softened and slightly wavy from refraction. Two or three uneven concentric ripples spread out from it, each a light crest with a darker trough. Crisp pixel-art finish.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, perfect geometric circles, clean outline ring, cartoon splash, magenta or hot-pink glow, mechanical parts, insect
- **Animation brief:** The bell pulses (4 frames). Each contraction sends out a train of three uneven ripples (light crest and dark trough, wobbling in radius, broken along their length) that stay where they were made while the jelly drifts on with the current and the scroll. The foam collar wobbles, opens and closes in gaps and sheds specks that drift away and fade. The submerged part sways with a sub-pixel row shift. Jellies fully below the surface (sub layer) have no foam. When the player comes within 96 px the bell flashes lime and pulses a ring of 10 shots. The 96 px trigger radius is a diagram on the sheet and is never drawn in game.
- **Mockup generator:** `python3 tools/concept/enemies_r07.py driftjelly`. The model is cut at the water plane with `clipped()` in `tools/concept/render/r07_models.py`; the foam, ripples and refraction are in `enemies_r07.py`. This follows the "Water" rule in design/art-direction.

# Round 48 — Act 4 "Deep Water" units (under Europa's ice)

Proposals a/b for the Act 4 units without art, each a PNG sheet and a GIF in the 480×540 play field
under the under-water grade of the unit's first level (round 48 Q2 a, lamp-lit to black, pending the
user's review: L23 lamp-lit farms, L24 the lamps' edge, L25 the dark trench, L26 black with the
headlight cone only, L27 the vents' glow). Mockup generator for all of them:
`python3 tools/concept/enemies_r48.py <subject> [a|b]` (models in the generator itself, on the frozen
`tools/concept/render/` package). Shared negative prompt for the Vrell units: photograph, painterly
brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion
blur, robot, mechanical parts, rivets, metal plating, magenta or hot-pink glow, yellow glow, blue or
cyan body colour, insect legs.

## eel-swarm-r48-a

**Eel Swarm A, ribbon eel (Vrell, Level 23)**: a chain of small eels swimming as one snake: a 48 px
lead eel and 12 body eels (40 px canvas) and a tail eel, 16 headings each. Files: `eel-swarm-r48-a.png`
(sheet) and `eel-swarm-r48-a.gif` (the chain over the lamp-lit Thera Deep farms).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down
  orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on
  a flat dark background. A small alien eel seen from directly above: a thick tapering body in deep
  rust chitin (#b04a2c to #3a1008) with fine ridges, a broad glowing violet (#9a4dff) lateral stripe on
  each flank, a pale bone dorsal ribbon fin, small bone pectoral fins and a bone tail fan, two tiny
  violet eye points. The lead eel: a broad flat skull with long hooked bone jaws, violet eyes and four
  lime (#a8ff2a) gill frills a side.
- **Negative prompt:** the shared Vrell list above, plus snake scales, cartoon face.
- **Animation brief:** the body eels follow the lead eel's path history 24 px apart, with a 36 px sine
  sway at 0.8 Hz about the route. 0.3 s before each shot the stripes light in a wave from the tail to
  the head (2 stripe frames); the lead eel opens its jaws (2 jaw frames) and fires an aimed shot every
  1.8 s and a 3-way fan every 5 s. A cut leaves the rear part drifting on 3 s, then it dies; the head
  kill unzips the chain head to tail, 0.07 s apart.
- **Mockup generator:** `python3 tools/concept/enemies_r48.py eel-swarm a`

## eel-swarm-r48-b

**Eel Swarm B, lantern eel (Vrell, Level 23)**: the same chain, armoured. Files: `eel-swarm-r48-b.png`
and `eel-swarm-r48-b.gif` (same scene as A).

- **Prompt:** as A, but each eel is armoured in teal-black chitin plates (#1e5c5a to #06201e) with
  three round glowing violet (#9a4dff) photophores along each flank and a pale bone spine ridge; the
  lead eel wears a smooth bone skull plate with a violet glowing maw and lime (#a8ff2a) gill frills.
- **Negative prompt:** as A.
- **Animation brief:** as A (the photophores light in the tail-to-head wave as the tell).
- **Mockup generator:** `python3 tools/concept/enemies_r48.py eel-swarm b`

## abyss-ray-r48-a

**Abyss Ray A, manta (Vrell, Level 24)**: a 200×150 px ray that rises from a shadow. Files:
`abyss-ray-r48-a.png` (sheet: source, headings, flap frames, the rise sequence, 1x with a Barbfin and the
player, gills only in the dark) and `abyss-ray-r48-a.gif` (shadow, rise, four Barbfins peel off, three
fans, glide off and sink, on the plain at the lamps' edge).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, key light from
  the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. A huge
  alien manta ray seen from directly above: broad swept crescent wings in dark teal-black chitin
  (#1e5c5a to #06201e) laced with glowing teal (#00ff9a) veins, a low ridged central body, two curled
  bone cephalic lobes in front, a glowing violet (#9a4dff) mouth slit, five glowing lime (#a8ff2a) gill
  slits along each leading edge near the body, a thin whip tail.
- **Negative prompt:** the shared Vrell list above, plus stingray barb, spaceship, aircraft.
- **Animation brief:** 16 headings × 3 wing-flap frames (wing tips up / level / down, a 2.2 s beat).
  Entry: a dark blurred silhouette grows under the ship's lane for 1.5 s, then the ray rises 0.9 s
  (tinted toward the water and soft, then crisp; on the play plane at the midpoint) while four Barbfins
  peel off; three 9-way fans over 110 degrees from the mouth 1.2 s apart; it banks, glides off to the
  side at 70 px/s and sinks away (tinted, smaller, fading).
- **Mockup generator:** `python3 tools/concept/enemies_r48.py abyss-ray a`

## abyss-ray-r48-b

**Abyss Ray B, bone skate (Vrell, Level 24)**. Files: `abyss-ray-r48-b.png` and `abyss-ray-r48-b.gif`
(same scene as A).

- **Prompt:** as A, but a rounded diamond-shaped skate disc in pale bone chitin (#dccfb4 to #5c4a5c)
  with fine ridges, a row of bone tubercles down the middle of the back, two pairs of glowing violet
  (#9a4dff) spots on the wings, a violet mouth, five lime (#a8ff2a) gill slits along each leading edge,
  a long thin bone whip tail with one spine.
- **Negative prompt:** as A, plus flying saucer.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r48.py abyss-ray b`

## siren-r48-a

**Siren A, conch throat (Vrell, Level 25)**: a 64×56 px nest in a cave mouth of the trench walls.
Files: `siren-r48-a.png` (sheet: source, tell frames, both wall facings, glow only, the ring, the
slowed ship) and `siren-r48-a.gif` (three nests in the dark trench, one ring slows the ship, a later
ring passes during the immunity, one nest shot).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, key light from
  the top-left, crisp silhouette, isolated on a flat dark background. An alien conch shell lying in a
  dark rock cave mouth, seen from directly above: a tapering spiral spire in matte mauve-grey slate
  chitin (#7c6878) studded with bone knobs, its wide opening facing the open water, fleshy dark lips
  and five bone spines round a swollen glowing teal (#00ff9a) throat sac.
- **Negative prompt:** the shared Vrell list above, plus seashell photo, snail, chrome.
- **Animation brief:** fixed, two facings (left and right wall) × 3 tell frames. Every 4.5 s the sac
  swells and flickers teal for 0.8 s, then a teal sound ring (a 10 px band with a bright leading edge)
  expands at 120 px/s to 230 px and fades. A ship it passes through is slowed to 60 % for 2 s (a teal
  sonar shimmer round the hull), then immune for 3 s. Death: a flash, a pale plume, the cave goes dark.
- **Mockup generator:** `python3 tools/concept/enemies_r48.py siren a`

## siren-r48-b

**Siren B, frill bell (Vrell, Level 25)**. Files: `siren-r48-b.png` and `siren-r48-b.gif` (same scene).

- **Prompt:** as A, but a smooth slate dome (#7c6878) in the cave mouth with five dark membrane frills
  on bone rays fanned round a glowing teal (#00ff9a) sac, teal veins in the membranes.
- **Negative prompt:** as A, plus flower petals.
- **Animation brief:** as A; on the tell the frills spread and the sac swells. The ring is a rippled
  double wavefront instead of a smooth band.
- **Mockup generator:** `python3 tools/concept/enemies_r48.py siren b`

## glow-angler-r48-a

**Glow Angler A, rust angler (Vrell, Level 26)**: a 40×32 px body with a 36 px stalk and a 12 px lure
(64 px canvas). Files: `glow-angler-r48-a.png` (sheet: source, headings, lunge and lure frames, a real
salvage pickup next to the lure in the black) and `glow-angler-r48-a.gif` (four anglers in the black
water, the headlight cone, one lunges and fires its ring, two are shot, a real pickup drifts past).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, key light from
  the top-left, glossy highlights, crisp silhouette, isolated on a black background. A small alien
  anglerfish seen from directly above: a bulbous body in deep rust chitin (#b04a2c to #3a1008), a wide
  gaping mouth ringed with small bone teeth, two glowing crimson (#ff3038) eyes, bone pectoral fins
  and a fan tail; a thin bone stalk arches forward from its head over the mouth to a glowing teal
  (#00ff9a) bead, the lure.
- **Negative prompt:** the shared Vrell list above, plus cyan lure, gem, coin, pickup icon.
- **Animation brief:** 16 headings × 2 jaw frames × 2 lure-pulse frames. It hangs drifting in the dark
  with the scroll; outside the headlight cone only the lure and the eyes show (a glow-only twin of the
  sprite); within 130 px it lunges 0.6 s at the ship, stops 60 px short, fires a ring of 8 and backs
  off. Death blows out the lure; the stalk stays faintly lit 1 s. The lure is teal, never a pickup's
  cyan, and sits on a visible stalk (Q6 a).
- **Mockup generator:** `python3 tools/concept/enemies_r48.py glow-angler a`

## glow-angler-r48-b

**Glow Angler B, teal-black gulper (Vrell, Level 26)**. Files: `glow-angler-r48-b.png` and
`glow-angler-r48-b.gif` (same scene as A).

- **Prompt:** as A, but a slender teal-black (#1e5c5a to #06201e) body tapering into a whip tail
  behind an enormous pouch-like mouth with glowing seams, one pair of small crimson (#ff3038) eyes, the
  teal (#00ff9a) lure on a longer stalk.
- **Negative prompt:** as A.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r48.py glow-angler b`

## depth-hunter-r48-a

**Depth Hunter A, pump-jet cigar (unmarked, Helix Dynamics, Level 27)**: a 72×40 px grey sub drone,
16 headings. Files: `depth-hunter-r48-a.png` (sheet: source, headings, next to the Act 3 Ghost Drone, the
torpedo and the player) and `depth-hunter-r48-a.gif` (a column of three rises from the bottom edge with
a rear warning, holds low and fires slow homing torpedoes up the screen; the ship climbs, one torpedo and
one hunter are shot down by the tail gun; the vents' orange glow).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, key light from
  the top-left, glossy metal highlights, crisp silhouette, isolated on a flat dark background. A
  military unmanned submarine seen from directly above: a long rounded cigar hull in plain mid grey
  (#7c818b) with recessed panel lines, a ducted pump-jet shroud at the stern with an orange thruster
  glow inside, four small cruciform fins, two torpedo tubes along the bow flanks, a dark narrow spine
  window, one glowing red sensor lens near the nose. No markings, no insignia, no gold, no colour accent.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view,
  text, watermark, motion blur, organic shapes, glow veins, gold trim, black-and-gold livery, insignia,
  numbers, flags.
- **Animation brief:** 16 headings (it faces up while it holds). Enters from the bottom edge after a 3 s
  rear warning, holds 380–460 px from the top with a 12 px drift, fires a homing torpedo every 3.5 s
  straight up (the Revenant Walker's missile sprite with a pale wake: 90 px/s × 0.7, 70°/s, 5 s life,
  shootable). Death: a hull break with grey debris and a dark oil bloom.
- **Mockup generator:** `python3 tools/concept/enemies_r48.py depth-hunter a`

## depth-hunter-r48-b

**Depth Hunter B, flat glider (unmarked, Level 27)**. Files: `depth-hunter-r48-b.png` and
`depth-hunter-r48-b.gif` (same scene as A).

- **Prompt:** as A, but a broad flat lifting-body hull (a manta-shaped military glider drone) in plain
  grey with panel lines, twin dorsal torpedo tubes, a red sensor slit across the nose, two small stern
  nozzles with an orange glow. No markings.
- **Negative prompt:** as A.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r48.py depth-hunter b`

## glassfin-r48-a

**Glassfin A, needle fry (Vrell, Level 23, tiny)**: a 16×16 px school fry, 16 headings. Files:
`glassfin-r48-a.png` (sheet: source, headings, glint and dark frames, the shatter death, next to a
Skitter and the player) and `glassfin-r48-a.gif` (two ribbons of 14 over the lamp-lit farms, a few glint
and ram, one hits the shield).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, key light from
  the top-left, glossy glassy highlights, crisp silhouette, isolated on a flat dark background. A tiny
  transparent alien fish fry seen from directly above: a thin needle body of clear pale aqua glass
  (#9edcdc), a glowing teal (#00ff9a) spine down its middle, two dark eye dots, a small glass fan tail.
- **Negative prompt:** the shared Vrell list above, plus opaque body, scales.
- **Animation brief:** 16 headings × 2 glint frames. Schools string out in ribbons along an S-route with
  a 14 px sway at 1.4 Hz; after 3 s a few flash teal for 0.3 s (the tell) and ram the ship's lane at
  230 px/s (aimed once, no homing). Death: a grey-teal flash and four slow glass shards, no bubbles. In
  the dark only the spine glow shows.
- **Mockup generator:** `python3 tools/concept/enemies_r48.py glassfin a`

## glassfin-r48-b

**Glassfin B, leaf larva (Vrell, Level 23, tiny)**. Files: `glassfin-r48-b.png` and `glassfin-r48-b.gif`
(same scene as A).

- **Prompt:** as A, but a flat transparent glass leaf (like an eel larva) with the teal (#00ff9a)
  spine down its middle and a tiny dark head.
- **Negative prompt:** as A, plus plant leaf.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r48.py glassfin b`

## barbfin-r48-a

**Barbfin A, bone lionfish (Vrell, Level 24)**: a 36×28 px Needler-role swimmer (40 px canvas), 32
headings. Files: `barbfin-r48-a.png` (sheet: source, headings, swim frames, in the dark, next to a
Needler and the player) and `barbfin-r48-a.gif` (a column of four from the left, three from the right,
holding and spitting needles; two shot).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, key light from
  the top-left, glossy highlights, crisp silhouette, isolated on a flat dark background. A small alien
  fish seen from directly above: a stubby body in pale bone chitin (#dccfb4) with fine ridges, five
  long bone fin rays fanned out on each side with dark webbing between them, a long thin barbed bone
  snout tipped with a glowing violet (#9a4dff) point, two violet eyes, a bone fan tail.
- **Negative prompt:** the shared Vrell list above, plus real lionfish stripes.
- **Animation brief:** 32 headings × 2 tail frames. Darts in on a curve from a side or the top, stops
  90–200 px above the ship's lane and holds, turning to the ship; an aimed needle every 2.4 s (first
  0.9 s after it stops, × 0.7 under water); leaves to the nearest side after 9 s. Death: a small flash,
  a sinking darkened shell and an ink-grey cloud.
- **Mockup generator:** `python3 tools/concept/enemies_r48.py barbfin a`

## barbfin-r48-b

**Barbfin B, plated box (Vrell, Level 24)**. Files: `barbfin-r48-b.png` and `barbfin-r48-b.gif` (same
scene as A).

- **Prompt:** as A, but a boxy body of pale bone armour plates (#dccfb4) whose gaps glow violet
  (#9a4dff), short bone fins, a forked tail, the same barbed violet-tipped snout.
- **Negative prompt:** as A.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r48.py barbfin b`

## eel-swarm-final-r52-a

Round 52, production art (M8 part C batch, step ART-C; from the chosen concept eel-swarm-r48-a, the rust ribbon eel of
`tools/concept/enemies_r48.py`, models imported unchanged). Not a mockup: the review sheet and a loop of the files
written by [tools/art/eel_swarm.py](../../../../tools/art/eel_swarm.py) (held in `build/art-held/sprites/` until the
Eel Swarm's `data.yaml` exists): the 48 px head at 16 headings (rest, the tell with the stripe lit, the shot with the
jaws wide), the 40 px body eel and the 36 px tail (rest, stripe lit), the violet stripe bloom of the tell (additive),
the muzzle pulse, the husks sinking; the GIF over Thera Deep's farm floor under a stand-in of the under-water grade: a
12-segment snake with the stripe running to the head before each aimed shot and a 3-way fan, a cut at segment 8 and
its rear part scattering for 3 s, the head kill unzipping the rest head to tail 0.07 s apart (the under-water bursts
of `explosions.py`, variant a), the husks sinking. The stripe is lit brighter than the concept (1.6 instead of 1.0)
and gets an additive bloom so the tell reads under the grade.

**Accepted as final** in round 52 (F.2, 2026-10-22).

## glassfin-final-r52-a

Round 52, production art (M8 part C batch, step ART-C; from the chosen concept glassfin-r48-a, the needle fry). Not a
mockup: the review sheet and a loop of the files written by [tools/art/glassfin.py](../../../../tools/art/glassfin.py)
(held until the Glassfin's `data.yaml` exists): the 16 px fry at 16 headings swimming and glinting (the ram's tell),
the glint sparkle, the shatter's flash and four glass shards (no bubbles); the GIF: two ribbons of 14 swaying over the
farms, three fry glinting and ramming down the ship's lane at 230 px/s (x 0.7 under water), five shattering.

**Accepted as final** in round 52 (F.3, 2026-10-22).

## abyss-ray-final-r53-a

Round 53, production art (M8 part D batch, step ART-D; from the chosen concept abyss-ray-r48-b, the bone skate of
`tools/concept/enemies_r48.py`, model imported unchanged). Not a mockup: the review sheet and a loop of the files
written by [tools/art/abyss_ray.py](../../../../tools/art/abyss_ray.py) (held in `build/art-held/sprites/` until the
Abyss Ray's `data.yaml` exists): the 208 px ray at 16 glide headings x 3 wing-beat frames (level, up, down; the beat
seen through a mild perspective camera so the raised wings read larger), the additive glow of the lime gill slits and
the violet spots and mouth (drawn ungraded, alone in the dark levels), the shadow growing 1.5 s on `sub`, the rise and
the sink (the body tinted to the deep water, blurred, smaller and faint, to crisp), the fan's violet mouth flash, the
`large` break-up (the wings folding up over the back, the glow dying, the body sinking) at the rise's and the two
glides' headings, the lime-violet bloom. The GIF: a whole visit on Level 24's plain under the review grade (section
2): the shadow growing ahead of the ship, the rise, four Barbfins peeling off to their holds, three 9-way fans 1.2 s
apart (x 0.7 under water), the glide to the far side and the sink; then a second visit killed after its second fan
(section 3 grade).

## barbfin-final-r53-a

Round 53, production art (M8 part D batch, step ART-D; from the chosen concept barbfin-r48-a, the bone lionfish). Not
a mockup: the review sheet and a loop of the files written by [tools/art/barbfin.py](../../../../tools/art/barbfin.py)
(held until the Barbfin's `data.yaml` exists): the 40 px fish at 32 headings x (two swim frames, the shot's tell with
the snout barb and eyes lit), the violet snout sparkle (additive), the dead shell sinking (8 headings x 4 steps) and the
ink-grey cloud (no bubbles); the needles are **violet** (the README), not the mockup's yellow
(`enemy_bullets.py violet`). The GIF: a column of four from the left and three from the right darting in, holding
90-200 px above the ship turned toward it, a violet needle every 2.4 s, two kills over the under-water burst.
