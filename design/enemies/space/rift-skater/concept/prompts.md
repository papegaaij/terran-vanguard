# Rift Skater — concept prompts

## rift-skater-r84-a

**Rift Skater a — "manta": a rounded rust chitin manta with a thin crystal rim (round 84).** Files: `rift-skater-r84-a.png` (sheet) and `rift-skater-r84-a.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. A small Vrell skirmisher (40×32 px), a flat manta of rust-red chitin (#b04a2c, dark #3a1008) with broad curved wings swept back to points, a thin rim of pale lilac crystal (#d6c6f2) along every edge that catches the light, a violet glowing core (#9A4DFF).
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, bubbles
- **Animation brief:** 16 headings, facing its glide. The phase jump: it fades out in 0.2 s, is gone 0.5 s while a thin violet-white ring shrinks from about 44 px onto the landing point, then lands; a muzzle glint 0.25 s later and a burst of 3 violet shots (larger, brighter, outlined). Death: it splinters into four violet flakes that fade. Never a flash. Every glow, haze, ring and beam is a smooth gradient of at least 32 steps that fades to 0 inside its canvas, with no ordered dither.
- **Mockup generator:** `python3 tools/concept/enemies_r84.py rift-skater [a|b]` (round 84, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## rift-skater-r84-b

**Rift Skater b — "shard": a sharp crystal diamond with wide crystal wing panels (round 84).** Files: `rift-skater-r84-b.png` (sheet) and `rift-skater-r84-b.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The same size and colours: a narrow rust body with a dark keel and a diamond-shaped violet core, the long swept wings mostly pale lilac crystal panels with a dark seam where chitin meets crystal.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, bubbles
- **Animation brief:** As a, but the arrival ring grows out of the landing point to the unit's size over the 0.5 s. Every glow, haze, ring and beam is a smooth gradient of at least 32 steps that fades to 0 inside its canvas, with no ordered dither.
- **Mockup generator:** `python3 tools/concept/enemies_r84.py rift-skater [a|b]` (round 84, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## rift-skater-final-r89-a

Production (round 89, M11 part B, step ART-B). Generator: `python3 tools/art/gate_units.py skater` (round 84's
`skater_model` a from `tools/concept/enemies_r84.py`, imported unchanged). Brief: the chosen a "manta" at 16 headings
(48×48 frames: rust chitin, the pale lilac crystal rim, the violet core), the core's soft violet light (additive), the
burst's tell (a violet-white four-point glint over 0.25 s), the death (four chitin flakes with crystal edges splintering
out and fading, their violet light decaying from the first frame, never a flash). The fade and the shrinking ring are
ART-A's shared teleport frames. Every glow a smooth float gradient, no ordered dither, fading to 0 inside its canvas.

## teleport-capture-r88-a

Concept round 88, a capture of the game (M11 part A, step A4): the fade and the shrinking ring on stand-in Skaters. The [A4 capture rig](../../../../world/vrell-space/concept/prompts.md#act7-rig-capture-r88-a).

## skater-drift-capture-r89-a

Concept round 89, a capture of the game, not generated art (M11 part B, step B5): Rift Skaters in Level 44's drift. The [B5 captures](../../../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/prompts.md#game-captures-r89-a).
