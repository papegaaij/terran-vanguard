# Strider — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared look (Ascendancy):** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e).

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos, insignia, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for bullets).

## strider-r60-a

**Strider A, rigid fall (Ascendancy, Level 32)**: the chosen r05 walker (120 px) in the belt livery; only what changed: the knee glow, the shoulder pod, the stagger with its fall line, the topple and the crush. Files: `strider-r60-a.png` (sheet) and `strider-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). The chosen round-05 biped walker mech (black and gold, red accents) seen from above, toppling sideways as one rigid piece, frames from upright to lying on its side; a small armoured missile pod on its left shoulder with a gold-glowing hatch.
- **Negative prompt:** the shared list above.
- **Animation brief:** Stagger 0.6 s: sparks at the knee, a 5 px lurch, the fall line (a dark strip 48 × 140 px on the lost leg's side) marked on the ground; fall 0.8 s; on landing a dust ring and the crush of every ground unit under the strip (credits paid); the darkened hulk stays. Pod: the hatch glows gold 0.4 s before a salvo of 2 homing diamonds.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py strider a`

## strider-r60-b

**Strider B, buckle and crumple (Ascendancy, Level 32)**: the chosen r05 walker (120 px) in the belt livery; only what changed: the knee glow, the shoulder pod, the stagger with its fall line, the topple and the crush. Files: `strider-r60-b.png` (sheet) and `strider-r60-b.gif` (the play field).

- **Prompt:** As A, but the knee buckles first (the body sinks and leans), then it pitches over and the torso breaks away from the legs: two wreck pieces.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py strider b`
