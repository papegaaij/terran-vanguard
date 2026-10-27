# Sentinel Tower — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared look (Ascendancy):** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e).

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos, insignia, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for bullets).

## sentinel-tower-r60-a

**Sentinel Tower A, lighthouse (Ascendancy, Level 32)**: 80×80 px from above, the head 32 headings; a laser tower in the side galleries of the hollowed asteroid sweeping arcs and full turns. Files: `sentinel-tower-r60-a.png` (sheet) and `sentinel-tower-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). A tall laser tower seen straight from above: a gold-banded octagonal armoured collar at the base, a round shaft, and on top a black sphere head with a gold equator ring and a short lens snout whose tip glows crimson (#ff3038) when live; a long shadow to the lower right.
- **Negative prompt:** the shared list above.
- **Animation brief:** Head 32 headings. Arc sweep: the arc drawn crimson for 0.8 s, then a 6 px, 220 px beam turns 120° over 1.6 s; every third sweep a full turn (3.0 s) after 1.0 s of the whole reach ring and the start ray. Death: a medium ground blast, the head topples off, a scorched stump.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py sentinel-tower a`

## sentinel-tower-r60-b

**Sentinel Tower B, pylon (Ascendancy, Level 32)**: 80×80 px from above, the head 32 headings; a laser tower in the side galleries of the hollowed asteroid sweeping arcs and full turns. Files: `sentinel-tower-r60-b.png` (sheet) and `sentinel-tower-r60-b.gif` (the play field).

- **Prompt:** As A, but a square collar with four gold-edged buttress fins and a flat disc head with a gold rim and a long crimson slit lens across it.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py sentinel-tower b`
