# Hornet — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared look (Ascendancy):** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e).

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos, insignia, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for bullets).

## hornet-r60-a

**Hornet A, twin-pod boat (Ascendancy, Level 31)**: 72×64 px (76 px canvas), fixed, faces down; a hovering missile boat firing salvos of four homing diamonds over the L31 rock field. Files: `hornet-r60-a.png` (sheet) and `hornet-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). A small missile boat seen from above: a slim gloss-black fuselage with a gold spine and gold canards, two fat cylindrical missile pods on short gold pylons with gold nose and tail caps, two hatches on top of each pod, a dark cockpit, twin engines, red lights on the pod tails.
- **Negative prompt:** the shared list above.
- **Animation brief:** Fixed. Enters at 140 px/s, hovers 90–150 px below the top for 9 s with a 20 px/s drift. The hatches glow gold 0.5 s before a salvo of 4 homing diamonds (pods alternating, 0.2 s apart). Hex shield 8. Death: a medium blast; missiles in the pods cook off in small flashes.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py hornet a`

## hornet-r60-b

**Hornet B, delta hover-boat (Ascendancy, Level 31)**: 72×64 px (76 px canvas), fixed, faces down; a hovering missile boat firing salvos of four homing diamonds over the L31 rock field. Files: `hornet-r60-b.png` (sheet) and `hornet-r60-b.gif` (the play field).

- **Prompt:** As A, but a broad flat black delta wing with gold leading edges, four gold-rimmed lift fans set into the wing, the missile cells recessed in the wing roots, red wing-tip lights.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py hornet b`
