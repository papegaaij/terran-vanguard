# Buzzsaw Drone — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared look (Ascendancy):** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e).

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos, insignia, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for bullets).

## buzzsaw-drone-r60-a

**Buzzsaw Drone A, gunmetal blades, gold tips (Ascendancy, Level 31)**: 36×36 px (42 px canvas), radial (6-fold); the chosen r05 drone in the belt livery with the spin-up tell, ricocheting off the edges and the big rocks of L31. Files: `buzzsaw-drone-r60-a.png` (sheet) and `buzzsaw-drone-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). A radially symmetric spinning blade drone seen from above: six hooked gunmetal blades with gold tips round a black hub with a gold ring and a red eye.
- **Negative prompt:** the shared list above.
- **Animation brief:** 8 spin frames cover 60°. Chases 2.5 s (120°/s), then the 0.6 s spin-up: it slows to 40 px/s, spins 2 → 6 turns/s and its gold flickers red; the dash follows the bearing at the tell's start, ricochets off the edges and the big rocks up to 3 times with a short after-image trail, slows, chases again. Death: the blades fly off as three spinning sparks.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py buzzsaw-drone a`

## buzzsaw-drone-r60-b

**Buzzsaw Drone B, gold blades (Ascendancy, Level 31)**: 36×36 px (42 px canvas), radial (6-fold); the chosen r05 drone in the belt livery with the spin-up tell, ricocheting off the edges and the big rocks of L31. Files: `buzzsaw-drone-r60-b.png` (sheet) and `buzzsaw-drone-r60-b.gif` (the play field).

- **Prompt:** As A, but six solid polished gold blades round a black hub with the red eye: a spinning gold star.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py buzzsaw-drone b`
