# Husk — concept prompts

## husk-r84-a

**Husk a — "punctured": an ovoid hull punched open from inside (round 84).** Files: `husk-r84-a.png` (sheet) and `husk-r84-a.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. A medium derelict Vrell hull (88×72 px, three hull shapes) of dull lightless ash grey-brown plates (#6e645c, dark #2a2622, paler lip edges), irregular brick-laid plates with dark seams; one to three round holes punched from the inside, the plates round each hole bent outward, a dark empty hollow inside; pale cold crystal (#b8c8d4) growing along the cracks that run from the holes, faintly lit from within. No glow anywhere else.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, bubbles
- **Animation brief:** 32 angles, turning slowly as it drifts. Cracked (at half its HP): a 0.5 s tell in which the crystal flares pale once and splits wider and longer, then a fan of 5 crystal shards. Death: the hull folds in, slow grey slabs and pale crystal splinters fade, a ring of 8 slow outlined orbs. Every glow, haze, ring and beam is a smooth gradient of at least 32 steps that fades to 0 inside its canvas, with no ordered dither.
- **Mockup generator:** `python3 tools/concept/enemies_r84.py husk [a|b]` (round 84, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## husk-r84-b

**Husk b — "split": a hull torn open along its spine (round 84).** Files: `husk-r84-b.png` (sheet) and `husk-r84-b.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The same plating, colours and crystal: one long tear down its back (straight, crooked, or with a side hole), the plates on both lips peeled outward, dark ribs bridging the empty inside, crystal growing along both lips.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, bubbles
- **Animation brief:** As a. Every glow, haze, ring and beam is a smooth gradient of at least 32 steps that fades to 0 inside its canvas, with no ordered dither.
- **Mockup generator:** `python3 tools/concept/enemies_r84.py husk [a|b]` (round 84, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)
