# Talon — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared look (Ascendancy):** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e).

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos, insignia, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for bullets).

## talon-r60-a

**Talon A, gold trim (Ascendancy, Level 29)**: 40×36 px (46 px canvas), 16 headings; the chosen r04 airframe in the belt livery, a V-wing behind a gold-striped leader over Ceres Hub's hull. Files: `talon-r60-a.png` (sheet) and `talon-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). A forward-swept stealth interceptor seen from above: faceted gloss-black body, gold chines along the wing leading edges and down the spine, gold wing-tip caps, twin orange-red engines, red running lights at the wing tips. The leader: a broad gold stripe down the spine and brighter red lights. A thin gold hexagon-grid shield outline hugging the hull at 40 % opacity.
- **Negative prompt:** the shared list above.
- **Animation brief:** 16 headings. A V-wing of 5 swoops in (lowest point 200–260 px below the top), pairs of aimed shots every 1.6 s after a 0.25 s red glint on the twin muzzles; a hit flashes the hexes where it lands, an emptied shield shatters in a 0.2 s ring of gold fragments. Killing the leader scatters the wing sideways at 260 px/s, no fire. Death: a small explosion, gold sparks, a spinning wing.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py talon a`

## talon-r60-b

**Talon B, gold panels (Ascendancy, Level 29)**: 40×36 px (46 px canvas), 16 headings; the chosen r04 airframe in the belt livery, a V-wing behind a gold-striped leader over Ceres Hub's hull. Files: `talon-r60-b.png` (sheet) and `talon-r60-b.gif` (the play field).

- **Prompt:** As A, but the outer half of each wing and the tail fins are solid polished gold, the body black: a bold two-tone read. The hex shield as a round bubble round the craft.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py talon b`
