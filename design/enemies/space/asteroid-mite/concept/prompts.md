# Asteroid Mite — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared negative prompt (Vrell):** photo, painterly, cel shading, perspective or 3/4 view, mechanical parts, metal, human markings, magenta or yellow glows (reserved for bullets), cute.

## asteroid-mite-r60-a

**Asteroid Mite A, tick (Vrell, Level 29)**: 20×20 px (22 px canvas), 16 headings, 4 crawl frames, a crouch and a leap pose; Vrell riders clinging to a small drifting rock, leaping at the ship. Files: `asteroid-mite-r60-a.png` (sheet) and `asteroid-mite-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, key light from the top-left, glossy chitin highlights, crisp silhouette, isolated on a flat dark background. A tiny alien tick seen from above: a round plated abdomen of glossy plum chitin (#a020a8) with a glowing teal (#00ff9a) seam down its back, a small dark head with two bone-white mandibles and teal eyes, six short splayed legs.
- **Negative prompt:** the shared list above.
- **Animation brief:** 16 headings × 4 crawl frames, a crouch (the tell: legs tucked, the teal glow flares for 0.4 s) and a leap pose. Rides a small rock in groups of 2–5 and leaps straight at the ship (260 px/s) when it comes within 160 px; a shot rock frees them after a 0.3 s tumble. Death: a tiny chitin burst with a teal spark.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py asteroid-mite a`

## asteroid-mite-r60-b

**Asteroid Mite B, flea (Vrell, Level 29)**: 20×20 px (22 px canvas), 16 headings, 4 crawl frames, a crouch and a leap pose; Vrell riders clinging to a small drifting rock, leaping at the ship. Files: `asteroid-mite-r60-b.png` (sheet) and `asteroid-mite-r60-b.gif` (the play field).

- **Prompt:** As A, but a long flat segmented plum body with three glowing teal spots on the back and two big hind legs folded at the sides with bone-white knees: a leaper.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py asteroid-mite b`
