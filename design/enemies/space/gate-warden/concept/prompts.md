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
