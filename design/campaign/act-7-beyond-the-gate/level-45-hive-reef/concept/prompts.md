# Level 45 – Hive Reef — concept prompts

## spawning-organ-r84-a

**Spawning organ a — "teal bulb": the Hive Node's teal-black spawner flesh with one crown pod (round 84).** Files: `spawning-organ-r84-a.png` (sheet) and `spawning-organ-r84-a.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. A rooted ground organ (80×80 px) on the hive reef: a round swollen sac of dark teal-black flesh (#1e5c5a, dark #06201e) on a fleshy base, seven thick roots running into the chitin plates, faint teal veins (#00FF9A), one bone-green seed pod (#8ab8a8) on its crown and a small iris on its front.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, bubbles
- **Animation brief:** Ground, fixed. The seed pod swells and glows for 0.5 s, then tears open in a cross as a cluster of Whirl Seeds spirals out; the iris opens over 0.5 s for a Skitter pair. It holds its release while the ship is within 120 px. In the screen's bottom third it blinks amber. Death: the sac tears and collapses, a short flash and a ring of ichor that spreads and fades; the torn husk stays on the plates, its veins dark. Every glow, haze, ring and beam is a smooth gradient of at least 32 steps that fades to 0 inside its canvas, with no ordered dither.
- **Mockup generator:** `python3 tools/concept/enemies_r84.py spawning-organ [a|b]` (round 84, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## spawning-organ-r84-b

**Spawning organ b — "reef polyp": the reef's own pink flesh with a ring of six pods (round 84).** Files: `spawning-organ-r84-b.png` (sheet) and `spawning-organ-r84-b.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The same size: a taller ribbed sac of the reef's pink flesh (#9a4462, dark #36121e) on nine thinner roots, the same teal veins, a ring of six pale pink seed pods (#e0b0c0) round an iris on its crown.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, bubbles
- **Animation brief:** As a; the six pods swell together and burst, the crown iris opens for the Skitters. Every glow, haze, ring and beam is a smooth gradient of at least 32 steps that fades to 0 inside its canvas, with no ordered dither.
- **Mockup generator:** `python3 tools/concept/enemies_r84.py spawning-organ [a|b]` (round 84, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## spawning-organ-final-r90-a

**Spawning organ, production** (round 90, M11 part C, step ART-C, 2026-11-06; production art, pending the user's review).
Files: `spawning-organ-final-r90-a.png` (sheet) and `.gif` (1× over this level's production backdrop). Generator:
`python3 tools/art/act7_reef.py organ` (round 84's `organ_model("b")` from `tools/concept/enemies_r84.py`, imported
unchanged, recoloured; sprites held in `build/art-held/sprites/` until step C2's unit data claims `spawning-organ`).

- **Brief:** round 84 A.5 b "reef polyp" in the one Vrell palette (Q10 b), not the proposal's reef pink: a tall ribbed
  violet sac (#562c70) on nine dark roots, mint-teal veins (#46beaa) glowing in it and down each root, six pale mint
  seed pods (#9edbcc) in a ring round a crown iris; 80×80, top-down, key light from the top left; the largest, brightest
  thing on the reef. A slow breath (4 frames); the iris opening over 0.5 s with a soft mint halo (the Skitter pair's
  tell); the pods swelling and lighting over 0.5 s, then bursting open and shrinking back (a Whirl Seed cluster's
  release); the death: the sac tears and collapses, the pods and flesh scraps fly out and shrink, a short mint-white
  flash and a ring of ichor spreading and fading (vacuum: no smoke), the torn husk left on the plates while the root
  veins round it go dark outward over 2 s. Every glow a smooth float gradient (≥ 32 steps, no dither), fading to 0
  inside its canvas.

## l45-props-final-r90-a

**The call organ, its roots and the courier's cyst, production** (round 90, step ART-C). Files:
`l45-props-final-r90-a.png` (sheet) and `.gif` (the roots cut one by one, the horn lifting, then loose). Generator:
`python3 tools/art/act7_reef.py call cyst` (held until step C2's data names the story pickup `call-organ` and the
cyst's sprite).

- **Call organ:** a pale ribbed horn (#ccc2db, about 64×40), conch-like, its bell open to the right and dark inside,
  lying on the spine's plates and held by four pale violet tendons to four root knots; its ribs breathe slowly (one
  breath in 4 s); no glow on the reef. Its roots cut, it lifts off over 2 s, growing to 1.2 (seen rising toward the
  camera), the cut tendon ends hanging; loose, it drifts with its ribs breathing once a second inside a faint pale
  violet-white halo (#e2ceff at about 40 %), so the pickup reads.
- **Root knot:** a ribbed tendon bulb diving into a dark socket in the plates (28×28): whole; hit (a gash, a pool of
  dark teal ichor); cut (a severed stump). Torn through: tendon shreds and ichor flecks flung out, shrinking.
- **Courier's cyst:** a lump of the reef's violet chitin (40×40) grown over a gold Ascendancy courier pod, a gold edge
  showing, no glow; cracked (splits across it); broken open (the chitin split away, the gold-lined hollow empty).
  Its break: chitin chips and gold fragments flung out (vacuum: no smoke).

## backdrop-final-r90-a

**The hive-reef kit, production** (round 90, step ART-C). Files: `backdrop-final-r90-a.png` (eight screens with
stand-ins, the pits' frames, the pieces and tiles), `.gif` (the level in a time-lapse, a frame every 4 s) and
`backdrop-proposal.yaml` (the `backdrop:` block, the sections and the pits' places for step C2). Generator:
`python3 tools/art/act7_reef.py reef` (writes `assets/backdrop/level-45/`; Level 43's spore sacs and round 84's
nebula, `tools/concept/act7_r84.py space_rgb`, imported unchanged).

- **Brief:** a moon-sized hive reef seen from just above, in the one Vrell palette with Level 45's light: chitin
  plates (teal-black #102c32 to violet #321844) lit softly from the top left, mint-teal veins (#46beaa) along the seams
  and through the plates, pulsing slowly (a separate vein layer the game pulses at ≤ 0.5 Hz; never bullet-sized dots),
  the whole reef a step darker than the units. By section: the rim's plates rising out of the violet and teal spore
  nebula with gaps between them; the brood fields' small dark plates in rows with breathing pits; the spine, a raised
  backbone of segmented plates down the middle; the first reef's ragged edge falling away, the living bridge of tendon
  and chitin arching over the gap on `low-air`, the second reef's lip; the brood heart's dark teal plates round a deep
  ribbed well with a faint mint glow far down; the far rim falling away to the nebula, the dead fleet's first dark hulls
  far ahead on `deep`. The spawning pits: fleshy lips round a mouth in the plates, breathing shut; their tell (the
  lips glow mint and part over 2 s); open (the throat lit from below); closing. Every haze and glow a smooth float
  gradient (≥ 32 steps, no ordered dither).

## game-captures-r90-a

Concept round 90, captures of the game, not generated art (M11 part C, step C5, 2026-11-06): the game built by
`:desktop:installDist` (branch `m11-act-7`, part C in the tree, a snapshot of the worktree) on a private 1920×1080 Xvfb
display, sound off, `--bench`, a scratch `--settings` with auto-fire on, `--level 45 --invulnerable` and `--shots
<tick>,…` writing the 960×540 screens every 3 ticks (20 a second); the GIFs take every second one (10 a second) and cut
out the play field (x 240–720), the HUD's event line inside it. Fits: the balance plan's L45 fit (Ion Beam L5, Tesla
Coil Pod L5, Micro-missile Pod L5, Swarm Tail L4; Rook with the Mortar L5) for the burn, the horn and the veins; Ion Beam
L5 and the Tesla Coil Pod without Rook for the organ (the ship steered left at t=109.5 so organ O4 lives past pit P6's
opening at t=117); the Ion Beam alone without Rook for the ship inside the first cloud (steered right at t=19, so no
blast burns it first). The horn clip steers the ship under the call organ's roots and up into the loose horn with key
presses (XTest), timed on the written shots. The veins crop: the three frames whose 160 px windows hold the most bullet
pixels on the brightest vein pixels, cut at 1:1. Laid out by a scratch script (not kept).

## organ-pit-capture-r90-a

Concept round 90, a capture of the game (step C5): organ O4 (t=112–119) releasing beside pit P6 opening (its tell from
t=115, the Mote Swarm rising at t=117). The [C5 captures](#game-captures-r90-a). Re-captured the same way in step CD5
(2026-11-06) after step FIXC-SPINE replaced the spine's ruled lines by soft arched ribs (the ship steered left at
t=109.5 for 0.4 s, `--escort none`, Ion Beam L5 front and Tesla Coil Pod L5 left).

## cloud-burn-capture-r90-a

Concept round 90, a capture of the game (step C5): the first spore cloud (t=18.5–21.5) burnt away by Rook's Mortar
burst at t=20. The [C5 captures](#game-captures-r90-a).

## cloud-inside-capture-r90-a

Concept round 90, a capture of the game (step C5): the ship inside the first spore cloud (t=19–23, no Rook). The
[C5 captures](#game-captures-r90-a).

## horn-taken-capture-r90-a

Concept round 90, a capture of the game (step C5): the call organ's roots cut, the horn loose and flown through,
`CALL ORGAN TAKEN` (t=127–132.5). The [C5 captures](#game-captures-r90-a).

## bullets-veins-capture-r90-a

Concept round 90, a capture of the game (step C5): Vrell bullets over the reef's brightest veins, three 240 px crops at
1:1 (t=145.85, 130.00, 115.10). The [C5 captures](#game-captures-r90-a).
