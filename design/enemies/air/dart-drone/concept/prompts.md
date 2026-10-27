# Dart Drone — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared look (Ascendancy):** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e).

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos, insignia, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for bullets).

## dart-drone-r60-a

**Dart Drone A, needle dart (Ascendancy, Level 30)**: 20×20 px (22 px canvas), 16 headings; tiny expendable rammers launched in streams from a Gunship's bays and from the edges. Files: `dart-drone-r60-a.png` (sheet) and `dart-drone-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). A tiny drone seen from above: a slim gloss-black needle body, a pointed gold nose cone, two small swept gold fins at the tail, one red light, a hot tail glow.
- **Negative prompt:** the shared list above.
- **Animation brief:** 16 headings. 0.4 s straight out of the bay, a 60°/s chase for 3 s, then straight on and out. No shield. Death: a tiny spark puff with gold flecks.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py dart-drone a`

## dart-drone-r60-b

**Dart Drone B, chevron dart (Ascendancy, Level 30)**: 20×20 px (22 px canvas), 16 headings; tiny expendable rammers launched in streams from a Gunship's bays and from the edges. Files: `dart-drone-r60-b.png` (sheet) and `dart-drone-r60-b.gif` (the play field).

- **Prompt:** As A, but a black arrowhead chevron with gold leading edges, a red light in the notch and two small tail glows.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py dart-drone b`
