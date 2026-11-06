# Gate Warden — concept prompts

## gate-warden-r84-a

**Gate Warden a — "idol": a squat round basalt sentinel in a six-petal egg with gold seams (round 84).** Files: `gate-warden-r84-a.png` (sheet) and `gate-warden-r84-a.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. A medium sentinel (76×84 px) of dark grey-violet basalt (#3a3640, dark #16141a, pale edges #8c8494): a squat round body, a waist band of crimson glyphs (#FF3038) set with pale stone studs, a low crown and a single round crimson eye on top. Closed, a smooth pale gold-white ovoid shell (#efe3c2) of six petals joined by thin gold seams covers it completely.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, bubbles
- **Animation brief:** Fixed, facing down; the eye's pupil turns to its aim. Closed: a slow specular sweep over the shell; hits throw pale gold flakes. Opening (0.15 s): the six petals hinge back at their base like an iris; the glyphs and the eye burn. The laser-line: a thin crimson telegraph line brightening for 0.8 s, then a 10 px crimson beam with a white-hot core for 1.0 s. Broken (a shield-breaker hit, the EMP): the petals shatter into gold flakes and regrow in 0.4 s behind a bright shimmer band. Death: the shell shatters, the glyphs go dark, the body splits into three slabs that fall and fade. Every glow, haze, ring and beam is a smooth gradient of at least 32 steps that fades to 0 inside its canvas, with no ordered dither.
- **Mockup generator:** `python3 tools/concept/enemies_r84.py gate-warden [a|b]` (round 84, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## gate-warden-r84-b

**Gate Warden b — "keystone": a tapered stone block like a piece of the gate in a seamless pearl egg (round 84).** Files: `gate-warden-r84-b.png` (sheet) and `gate-warden-r84-b.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The same size, stone and shell: the body a tapered keystone block cut from the gate ring, two columns of crimson glyphs down its face, a pale lintel, two dark shoulder stones, the crimson eye in the middle. Closed, the shell is one seamless pale gold-white pearl egg.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, bubbles
- **Animation brief:** As a; the seams appear only as the petals part. Every glow, haze, ring and beam is a smooth gradient of at least 32 steps that fades to 0 inside its canvas, with no ordered dither.
- **Mockup generator:** `python3 tools/concept/enemies_r84.py gate-warden [a|b]` (round 84, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## gate-warden-final-r89-a

Production (round 89, M11 part B, step ART-B). Generator: `python3 tools/art/gate_units.py warden` (round 84's
`warden_body`, `shell_scene` and `warden_mats` from `tools/concept/enemies_r84.py`, imported unchanged; layouts and
blending in the script's header). Brief: the chosen a "idol" at play size (frames 120×132, the egg 74×82 px): the
six-petal iris (closed, 0.2–1.0 open), the bare body after a break and the shell regrowing in 0.4 s behind a bright
silver-gold band climbing over it, the closed shell's slow warm-white specular sweep (2.5 s), the glyph band's and the
eye's crimson light as an additive layer the game scales (closed dim, open bright, the leaving tell dimmed and
flickered) and its own layer per tilt, the telegraph eye's 0.8 s charge (a crimson glow round a white-hot core), the
leaving tell's tilt (4–16°, the top toward the top edge), the death's three stone slabs drifting apart and falling,
each slab's glyphs going dark in turn. Every glow a smooth float gradient, no ordered dither, fading to 0 inside its
canvas.

## shell-capture-r88-a

Concept round 88, a capture of the game (M11 part A, step A4): the window shell's stand-in look (A3a) on ART-B's closed frame, broken by the Plasma Arc. The [A4 capture rig](../../../../world/vrell-space/concept/prompts.md#act7-rig-capture-r88-a).

## leaving-capture-r88-a

Concept round 88, a capture of the game (M11 part A, step A4): the leaving tell and the tracker's amber blink, full screen. The [A4 capture rig](../../../../world/vrell-space/concept/prompts.md#act7-rig-capture-r88-a).

## warden-lines-capture-r89-a

Concept round 89, a capture of the game, not generated art (M11 part B, step B5): Level 43's Warden lines. The [B5 captures](../../../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/prompts.md#game-captures-r89-a).

## warden-arc-capture-r89-a

Concept round 89, a capture of the game (step B5): the Tesla arc holding a Warden's shell open. The [B5 captures](../../../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/prompts.md#game-captures-r89-a).

## warden-leaving-capture-r89-a

Concept round 89, a capture of the game (step B5): the leaving tell and the secondary failed. The [B5 captures](../../../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/prompts.md#game-captures-r89-a).
