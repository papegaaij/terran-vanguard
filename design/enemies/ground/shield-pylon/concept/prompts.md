# Shield Pylon — concept prompts

## shield-pylon-r72-a

**Shield Pylon a — "crown": a gold crown of six prongs on a short black mast, a solid feed beam (round 72).** Files: `shield-pylon-r72-a.png` (sheet) and `shield-pylon-r72-a.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. A fortress shield mast (56×56 px) seen from above in the elite livery, gloss black hull (#1a1622, hex panelling) with heavy gold (about 40 % of the silhouette), red running lights and a strong 2 px gold rim on the top-left edges: a black octagonal base with a gold edge and red corner lights, a short black mast ringed by a gold crown of six prongs, an emitter lens leaning toward its dome.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, organic shapes, blue glow
- **Animation brief:** The crown turns to face its dome (16 facings). The dome: a circle of radius 110 px, 140 px out, a thin gold hex lattice at about 20 % with a bright rim and a slow shimmer, joined to the crown by a solid thin gold beam. Covered units wear a faint gold hex sheen; shots on them spend in gold sparks. Death: the crown topples and the dome collapses into it as a shrinking gold ring (0.4 s). Every glow, haze and shadow is a smooth gradient of at least 32 steps, drawn once, with no ordered dither (the 2026-10-30 dither audit).
- **Mockup generator:** `python3 tools/concept/enemies_r72.py shield-pylon [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## shield-pylon-r72-b

**Shield Pylon b — "obelisk": a tall faceted black spire with gold vanes, a feed of flowing packets (round 72).** Files: `shield-pylon-r72-b.png` (sheet) and `shield-pylon-r72-b.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The same base; a tall faceted black spire with four gold vanes down its edges and a gold emitter dish at its tip.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, organic shapes, blue glow
- **Animation brief:** As a, the feed a faint line with bright packets running to the dome, the dome's lattice fading toward its middle. Every glow, haze and shadow is a smooth gradient of at least 32 steps, drawn once, with no ordered dither (the 2026-10-30 dither audit).
- **Mockup generator:** `python3 tools/concept/enemies_r72.py shield-pylon [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)
