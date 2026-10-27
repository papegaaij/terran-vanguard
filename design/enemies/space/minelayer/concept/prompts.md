# Minelayer — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared look (Ascendancy):** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e).

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos, insignia, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for bullets).

## minelayer-r60-a

**Minelayer A, hopper barge (Ascendancy, Level 30)**: 88×64 px (92 px canvas), 16 headings; it comes up from behind, turns into its row and lays a line of 3 × 4 mines with one gap over the L30 lane. Files: `minelayer-r60-a.png` (sheet) and `minelayer-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). A wide flat mine-laying barge seen from above: gloss-black hull with a gold edge band, two engine sponsons with glowing nozzles, a round gold mine drum at the stern with a dark chute, a small cockpit, red running lights. Its mine: a small black ball with four gold spikes and a red lamp on top.
- **Negative prompt:** the shared list above.
- **Animation brief:** 16 headings; the mine 3 lamp states. Rear entry up a side lane (edge-warned), a turn into its lay row, a strafe across the field laying 3 clusters of 4 (32 px apart, 56 px between clusters) with one 112 px gap. A mine arms after 1 s (slow red blink), blinks fast 0.6 s when the ship is within 48 px and bursts into a ring of 8; a shot mine bursts and sets off the mines within 40 px.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py minelayer a`

## minelayer-r60-b

**Minelayer B, catamaran (Ascendancy, Level 30)**: 88×64 px (92 px canvas), 16 headings; it comes up from behind, turns into its row and lays a line of 3 × 4 mines with one gap over the L30 lane. Files: `minelayer-r60-b.png` (sheet) and `minelayer-r60-b.gif` (the play field).

- **Prompt:** As A, but two slim black hulls with gold noses and tails joined by a gold-edged bridge deck, the gold mine rack between the hulls at the rear. Its mine: a flat black disc with a gold rim, three short gold prongs and a red eye.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py minelayer b`
