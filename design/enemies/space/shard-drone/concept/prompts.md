# Shard Drone — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared look (Ascendancy):** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e).

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos, insignia, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for bullets).

## shard-drone-r60-a

**Shard Drone A, tri-blade, beam links (Ascendancy, Level 31)**: 40×40 px, radial (3-fold, 360°/s); groups of 3–4 joined by energy links that sweep down the field. Files: `shard-drone-r60-a.png` (sheet) and `shard-drone-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). A small spinning drone seen from above: three long tapering gold crystal-like shard blades round a gloss-black hub ring with a red light in the middle. Energy links between drones drawn as a crimson beam with a dark outline and a pale pink-white core.
- **Negative prompt:** the shared list above.
- **Animation brief:** 8 frames cover 120°. A line of four 120 px apart sways down the field; 0.8 s after the group is in, the links flicker (the tell), then light (a 6 px contact band). Shots pass through links; killing a drone drops its links. The circle formation: four on a 64 px radius, a closed square of links, turning. Death: a small burst of gold shards, its links snap with a spark.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py shard-drone a`

## shard-drone-r60-b

**Shard Drone B, tri-prism, lightning links (Ascendancy, Level 31)**: 40×40 px, radial (3-fold, 360°/s); groups of 3–4 joined by energy links that sweep down the field. Files: `shard-drone-r60-b.png` (sheet) and `shard-drone-r60-b.gif` (the play field).

- **Prompt:** As A, but a black triangular frame with a gold crystal prism at each corner and the red hub; the links as crackling crimson lightning with a thin gold core, re-drawn every frame.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py shard-drone b`
