# Harrow — concept prompts

## harrow-r72-a

**Harrow a — "dart and sac": a gunmetal dart with a swollen tissue sac on its back (round 72).** Files: `harrow-r72-a.png` (sheet) and `harrow-r72-a.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. A small kamikaze dart (34×34 px) in regular gunmetal plating (#5a606c hull, #464b55 facets) with gold trim, red running lights and a 1 px gold rim on the top-left edges, red on the far edges, split by Vrell tissue: dark teal-black flesh (#163f3e) glowing teal (#00FF9A) in the seams: gold leading edges, swept fins, a swollen sac of dark teal tissue behind the nose with glowing veins.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, magenta glow, bubbles
- **Animation brief:** 16 headings. Hunting: a slow sac pulse; the 0.6 s lock tell: it slows, the sac pulses fast and brightens, the nose swings onto the ship; then a straight ram. Any death: a teal flash, flecks, the shell tumbling away, and a ring of 8 slow teal bullets turned so the ship's bearing falls between two. Every glow, haze and shadow is a smooth gradient of at least 32 steps, drawn once, with no ordered dither (the 2026-10-30 dither audit).
- **Mockup generator:** `python3 tools/concept/enemies_r72.py harrow [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## harrow-r72-b

**Harrow b — "fork": two gold-tipped prongs cradling a longer sac (round 72).** Files: `harrow-r72-b.png` (sheet) and `harrow-r72-b.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The same size and livery as a: two gunmetal prongs joined at the tail with gold tips hold a long teal tissue sac between them in plain view.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, magenta glow, bubbles
- **Animation brief:** As a. Every glow, haze and shadow is a smooth gradient of at least 32 steps, drawn once, with no ordered dither (the 2026-10-30 dither audit).
- **Mockup generator:** `python3 tools/concept/enemies_r72.py harrow [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)
