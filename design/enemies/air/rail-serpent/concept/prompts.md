# Rail Serpent — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared look (Ascendancy):** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e).

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos, insignia, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for bullets).

## rail-serpent-r60-a

**Rail Serpent A, gold spine and skirts (Ascendancy, Level 30)**: engine 64 px, cars 48 px, 32 headings; the chosen r05 cars in the belt livery with the turret cars, the tail and the machine split, entering from the side of the L30 lane. Files: `rail-serpent-r60-a.png` (sheet) and `rail-serpent-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). An armoured drone train seen from above, each car an angular gloss-black module with a gold spine bar and gold side skirts; the engine car has a sensor prow and a glowing white-gold reactor disc on its deck; every third car carries a small black dome turret with a twin gun and a red lamp; the tail car has gold fins.
- **Negative prompt:** the shared list above.
- **Animation brief:** 32 headings per car. Enters from a side (edge-warned 3 s). Turret cars fire aimed shots after a 0.2 s red lamp. A car shot out cuts the train: the cars behind lose power, drift at 40 % slowing, dark and sparking, for 1.5 s, then blow from the cut backwards 0.1 s apart; the front half runs on.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py rail-serpent a`

## rail-serpent-r60-b

**Rail Serpent B, gold roofs and links (Ascendancy, Level 30)**: engine 64 px, cars 48 px, 32 headings; the chosen r05 cars in the belt livery with the turret cars, the tail and the machine split, entering from the side of the L30 lane. Files: `rail-serpent-r60-b.png` (sheet) and `rail-serpent-r60-b.gif` (the play field).

- **Prompt:** As A, but every car has gold roof armour with a black centre stripe and the cars are joined by gold coupler links: one gold chain.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py rail-serpent b`
