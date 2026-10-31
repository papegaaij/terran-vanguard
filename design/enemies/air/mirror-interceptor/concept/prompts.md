# Mirror Interceptor — concept prompts

## mirror-interceptor-r72-a

**Mirror Interceptor a — "crescent": one curved polished mirror plate across the nose (round 72).** Files: `mirror-interceptor-r72-a.png` (sheet) and `mirror-interceptor-r72-a.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. A short broad wedge-shaped craft (72×68 px) in the regular livery, gunmetal hull (#5a606c) and charcoal facets (#464b55) with gold trim (at least 20 %), red running lights, a 1 px gold rim, gold edge trim and a gold chevron; across its nose a polished silver crescent mirror 76 px wide with gold tip caps; two gun pods under the mirror's rim, two engines at the rear.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, organic shapes, glass dome over the mirror
- **Animation brief:** 16 headings, turning 60°/s toward the ship at its station. The mirror: a slow specular sweep; heat glows dull red, orange, white with cracks from 70 %; broken, the bare grey frame shows; regrowing, a silver shimmer sweeps in from one tip (0.4 s). Bounced player shots come back as slow magenta small bullets. Tell: a 0.4 s red glow in the pods, then 3 aimed shots. Death: a medium explosion and falling silver shards. Every glow, haze and shadow is a smooth gradient of at least 32 steps, drawn once, with no ordered dither (the 2026-10-30 dither audit).
- **Mockup generator:** `python3 tools/concept/enemies_r72.py mirror-interceptor [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## mirror-interceptor-r72-b

**Mirror Interceptor b — "facets": five flat mirror panels in gold frames round the same arc (round 72).** Files: `mirror-interceptor-r72-b.png` (sheet) and `mirror-interceptor-r72-b.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The same hull with gold side panels; the mirror is five flat silver panels set round the arc in gold frames.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, organic shapes
- **Animation brief:** As a; the facets flash panel by panel as it turns. Every glow, haze and shadow is a smooth gradient of at least 32 steps, drawn once, with no ordered dither (the 2026-10-30 dither audit).
- **Mockup generator:** `python3 tools/concept/enemies_r72.py mirror-interceptor [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## mirror-capture-r73-a

Concept round 73, a capture of the game, not generated art (M10 part A, step A4, 2026-10-31): frames of the
[A4 capture rig](../../../../world/jovian/concept/prompts.md#act6-rig-capture-r73-a); the sheet and the GIF are laid out from its screenshots (the 480×540 play field
or crops, scaled with nearest neighbour).
