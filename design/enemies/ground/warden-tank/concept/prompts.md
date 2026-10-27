# Warden Tank — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared look (Ascendancy):** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e).

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos, insignia, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for bullets).

## warden-tank-r60-a

**Warden Tank A, r05 livery with red running lights (Ascendancy, Level 34)**: 76 px, the hull 16 headings × 2 tread frames, the turret 32 headings; the chosen r05 tank in Ascendancy livery on L34's derelict hulls. Files: `warden-tank-r60-a.png` (sheet) and `warden-tank-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). The chosen round-05 tracked tank seen from above in Ascendancy livery: gloss black hull and hexagonal turret, gold side stripes, a gold turret ring and muzzle, gold track edges, white accent panels, bright red running lights.
- **Negative prompt:** the shared list above.
- **Animation brief:** Hull 16 headings × 2 tread frames following a hull road, turret 32 headings; 3-shot bursts 0.15 s apart after a 0.3 s still turret, in any direction, up the screen too. Wreck: darkened black and gold.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py warden-tank a`

## warden-tank-r60-b

**Warden Tank B, heavier gold (Ascendancy, Level 34)**: 76 px, the hull 16 headings × 2 tread frames, the turret 32 headings; the chosen r05 tank in Ascendancy livery on L34's derelict hulls. Files: `warden-tank-r60-b.png` (sheet) and `warden-tank-r60-b.gif` (the play field).

- **Prompt:** As A, but with gold track guards, a gold engine deck and a gold turret roof: unmistakably Ascendancy from across the screen.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py warden-tank b`
