# Gilded Gunship — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared look (Ascendancy):** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e).

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos, insignia, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for bullets).

## gilded-gunship-r60-a

**Gilded Gunship A, gilded chevron (Ascendancy, Level 30)**: 88×100 px (104 px canvas), fixed, faces down; the chosen r04 hull in the belt livery with the new ring lamps, belly bays and engine glow, over the L30 shipping lane. Files: `gilded-gunship-r60-a.png` (sheet) and `gilded-gunship-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). A slow heavy armoured gunship wedge seen from above: gloss black hull, a polished gold chevron armour plate across the nose, a dark glass dome on the back ringed by twelve small gold lamps, two flank pods, two rear engine nacelles glowing white-gold, bay doors on the flanks edged red, red running lights.
- **Negative prompt:** the shared list above.
- **Animation brief:** Fixed. Descends at 45 px/s with a 20 px sway. The twelve lamps light 0.4 s before a ring of 12; the bay doors glow red as a stream of 3 Dart Drones leaves (0.25 s apart). Front hits spark grey-gold (×0.5), engine hits flash white (×2). Death: the hull breaks in two behind the plate, gold plates spin off, a medium blast.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py gilded-gunship a`

## gilded-gunship-r60-b

**Gilded Gunship B, gold scale plate (Ascendancy, Level 30)**: 88×100 px (104 px canvas), fixed, faces down; the chosen r04 hull in the belt livery with the new ring lamps, belly bays and engine glow, over the L30 shipping lane. Files: `gilded-gunship-r60-b.png` (sheet) and `gilded-gunship-r60-b.gif` (the play field).

- **Prompt:** As A, but the whole front third is overlapping gold scale armour and the pod tops are gold: one big gold face, a black open back.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py gilded-gunship b`
