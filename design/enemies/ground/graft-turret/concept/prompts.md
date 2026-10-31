# Graft Turret — concept prompts

## graft-turret-r72-a

**Graft Turret a — "metal first": an Ascendancy deck turret with tissue scars (round 72).** Files: `graft-turret-r72-a.png` (sheet) and `graft-turret-r72-a.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. A small deck turret (44×44 px) on a fixed octagonal base in regular gunmetal plating (#5a606c hull, #464b55 facets) with gold trim, red running lights and a 1 px gold rim on the top-left edges, red on the far edges, split by Vrell tissue: dark teal-black flesh (#163f3e) glowing teal (#00FF9A) in the seams: a gunmetal dome with a gold ring, one barrel with a gold muzzle, a red sensor; thin teal scars on the dome and three cracks in the base.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, magenta glow, bubbles
- **Animation brief:** The turret turns at 32 headings. Shot: the shell splits, the barrel falls off and the root shows (a knot of teal tissue) with a thin teal ring shrinking over the 5 s window and a pulse quickening from 1 to 4 Hz. Root killed: a fibrous crumble and a scorched stump with withered tendrils. The window closes: tissue knits over the wreck in 0.8 s and the turret comes back mostly tissue, its barrel bone-white and veined teal. Every glow, haze and shadow is a smooth gradient of at least 32 steps, drawn once, with no ordered dither (the 2026-10-30 dither audit).
- **Mockup generator:** `python3 tools/concept/enemies_r72.py graft-turret [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## graft-turret-r72-b

**Graft Turret b — "tissue first": the turret sits on a fleshy collar with roots across the deck (round 72).** Files: `graft-turret-r72-b.png` (sheet) and `graft-turret-r72-b.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The same turret with a third of its dome overgrown, a fleshy tissue collar round it and six roots reaching past the mount onto the deck; a bigger root bulb.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, magenta glow, bubbles
- **Animation brief:** As a. Every glow, haze and shadow is a smooth gradient of at least 32 steps, drawn once, with no ordered dither (the 2026-10-30 dither audit).
- **Mockup generator:** `python3 tools/concept/enemies_r72.py graft-turret [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## regrowth-capture-r73-a

Concept round 73, a capture of the game, not generated art (M10 part A, step A4, 2026-10-31): frames of the
[A4 capture rig](../../../../world/jovian/concept/prompts.md#act6-rig-capture-r73-a); the sheet and the GIF are laid out from its screenshots (the 480×540 play field
or crops, scaled with nearest neighbour).
