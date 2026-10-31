# Chimera — concept prompts

## chimera-r72-a

**Chimera a — "seams": the Talon airframe cracked by thin glowing tissue seams (round 72).** Files: `chimera-r72-a.png` (sheet) and `chimera-r72-a.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. A small forward-swept interceptor (40×36 px) in regular gunmetal plating (#5a606c hull, #464b55 facets) with gold trim, red running lights and a 1 px gold rim on the top-left edges, red on the far edges, split by Vrell tissue: dark teal-black flesh (#163f3e) glowing teal (#00FF9A) in the seams: thin teal cracks along the wing roots, across each wing and the spine; the outer wing panels and tail fins gold.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, chitin plates, insect legs, magenta glow
- **Animation brief:** 16 headings. The seam glow is its own emission-only layer added over the sprite: faint at full health, dark just after a hit, a teal pulse running out from the body twice a second while it heals (a second without a hit, 20 %/s), brightening toward full; the HP bar grows back with a teal edge. Tell: the Talon's 0.25 s red glint on the twin muzzles. Death: a small explosion with gold sparks and teal tissue flecks. Every glow, haze and shadow is a smooth gradient of at least 32 steps, drawn once, with no ordered dither (the 2026-10-30 dither audit).
- **Mockup generator:** `python3 tools/concept/enemies_r72.py chimera [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## chimera-r72-b

**Chimera b — "overgrown": the same airframe with wide wavering tissue bands and nodules bulging from the seams (round 72).** Files: `chimera-r72-b.png` (sheet) and `chimera-r72-b.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The same interceptor in regular gunmetal plating (#5a606c hull, #464b55 facets) with gold trim, red running lights and a 1 px gold rim on the top-left edges, red on the far edges, split by Vrell tissue: dark teal-black flesh (#163f3e) glowing teal (#00FF9A) in the seams, the seams twice as wide and veined, tissue nodules bulging at the wing roots, on the spine and the wing panels.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, chitin plates, insect legs, magenta glow
- **Animation brief:** As a; the lumpier outline tells it from the Act 5 Talon at a glance. Every glow, haze and shadow is a smooth gradient of at least 32 steps, drawn once, with no ordered dither (the 2026-10-30 dither audit).
- **Mockup generator:** `python3 tools/concept/enemies_r72.py chimera [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)
