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
