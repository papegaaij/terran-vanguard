# SAM Nest — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared look (Ascendancy):** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e).

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos, insignia, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for bullets).

## sam-nest-r60-a

**SAM Nest A, twin tubes on a round base (Ascendancy, Level 29)**: 44×44 px (48 px canvas), the base fixed, the rack 16 headings; station-hull launchers that sleep with white lights and wake red when Ceres Hub turns sides. Files: `sam-nest-r60-a.png` (sheet) and `sam-nest-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). A small missile launcher on a station hull seen from above: a round gloss-black armoured base with a gold ring and four small lights, a turning rack of two missile tubes on a yoke with gold muzzle rims under a gold hatch, a red lock light.
- **Negative prompt:** the shared list above.
- **Animation brief:** Rack 16 headings (120°/s). Tell 0.4 s: the rack holds, the hatch lifts (the cells glow gold), the red lock light blinks; then 2 homing diamonds 0.25 s apart. Dormant: white lights, the rack low; waking: the lights turn red and the rack lifts for 1 s. Death: a small ground blast, the rack thrown, a scorched wreck.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py sam-nest a`

## sam-nest-r60-b

**SAM Nest B, four-cell box on a hex base (Ascendancy, Level 29)**: 44×44 px (48 px canvas), the base fixed, the rack 16 headings; station-hull launchers that sleep with white lights and wake red when Ceres Hub turns sides. Files: `sam-nest-r60-b.png` (sheet) and `sam-nest-r60-b.gif` (the play field).

- **Prompt:** As A, but a hexagonal base with gold edges and a square box launcher with four missile cells in a gold frame.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py sam-nest b`
