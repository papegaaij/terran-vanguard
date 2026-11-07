# Choir Seraph — concept prompts

## choir-seraph-r84-a

**Choir Seraph a — "blades": a slim ivory seraph with two pairs of long swept blade wings (round 84).** Files: `choir-seraph-r84-a.png` (sheet) and `choir-seraph-r84-a.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The Choir's elite (72×76 px): a slim body of pale bone and ivory chitin (#dccfb4, dark segments #5c4a5c, bright #f4ecd8), two pairs of long blade wings swept back, pale gold-white light (#F2DC9A) along their leading edges and in a small heart at its chest; never the player's white.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, bubbles
- **Animation brief:** Fixed, facing down. Arrival: a violet ring with a violet-white core shrinking onto its point (1.0 s first, then 0.5 s), never gold, never a flash. Its spiral's tell: a violet flare at the wing roots; then a two-arm spiral of violet shots while it mirrors the ship. The jump's tell: the wings fold back along the body (0.3 s), then it fades (0.2 s). Death: the wings scatter into gold-white feathers of light that drift and fade. Every glow, haze, ring and beam is a smooth gradient of at least 32 steps that fades to 0 inside its canvas, with no ordered dither.
- **Mockup generator:** `python3 tools/concept/enemies_r84.py choir-seraph [a|b]` (round 84, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## choir-seraph-r84-b

**Choir Seraph b — "halo": feathered wings and the Choir Herald's halo ring (round 84).** Files: `choir-seraph-r84-b.png` (sheet) and `choir-seraph-r84-b.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The same size and colours: broader wings with a serrated trailing edge of long feathers in bone and ivory bands, gold-white leading edges, and a glowing gold-white halo on three bone spokes over its back (the Choir Herald's seraph family).
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, bubbles, magenta glow
- **Animation brief:** As a. Every glow, haze, ring and beam is a smooth gradient of at least 32 steps that fades to 0 inside its canvas, with no ordered dither.
- **Mockup generator:** `python3 tools/concept/enemies_r84.py choir-seraph [a|b]` (round 84, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## choir-seraph-final-r92-a

**Choir Seraph, production** (round 92, M11 part E, step ART-E, 2026-11-06; production art, pending the user's review).
Files: `choir-seraph-final-r92-a.png` (sheet) and `.gif` (a pair over Level 47's production inner sphere: the rings,
the mirror and its spirals, the fold, a kill, the leaving tell). Generator: `python3 tools/art/act7_sphere.py seraph`
(round 84's `seraph_model("a", fold, glow)` from `tools/concept/enemies_r84.py`, imported unchanged, on the production
path: 4×, 1-bit alpha, 40 colours over the body sets; in `assets/sprites/` since step E2's level data names
`choir-seraph`).

- **Brief:** A.4 a "blades" as chosen, 72×76 in an 88×88 canvas, fixed, facing down: pale bone and ivory chitin, two
  pairs of swept blade wings with gold-white leading edges and heart. Gliding (4 frames, the heart breathing); banked
  with its sideways speed while it mirrors (7 frames: 30/20/10° into a move left, level, 10/20/30° right, rendered rolled
  under a mild perspective with the key light kept top-left); its soft gold-white glow (additive, over any body frame);
  the spiral's tell, a violet flare at the two fore wing roots over 0.4 s; the fold over 0.3 s (6 frames, the last held
  through the built fade); the leaving tell over 0.8 s (12 frames: the wings folding slower while they flare white, and
  their white-gold light; the 40 px rise is the simulation's); the death, the blade wings breaking into ivory shards
  with gold edges and bone segments, and gold-white feathers of light drifting out and fading from the first frame
  (never a flash); the spiral's shot, a violet orb with a violet-white core pulsing and a dark outline. The arrival ring
  is the built violet `teleport-ring-large` (never gold). The intel portrait (30×30): level, wings open, nose down
  (`tools/art/intel.py choir-seraph`). Every glow a smooth float gradient (≥ 32 steps, no dither), fading to 0 inside
  its canvas.
