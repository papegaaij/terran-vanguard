# Rail Bunker — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared look (Ascendancy):** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e).

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos, insignia, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for bullets).

## rail-bunker-r60-a

**Rail Bunker A, solid lock line (Ascendancy, Level 33)**: the chosen r04 octagonal bunker (72 px, turret 32 headings) in the belt livery with a gold rim on the octagon; only what changed: the rail line and the shot. Files: `rail-bunker-r60-a.png` (sheet) and `rail-bunker-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a strong key light from the top-left (the belt's sun), glossy highlights, crisp silhouette, isolated on a flat dark background. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least a fifth of the silhouette, a thin bright gold rim on the edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). The chosen round-04 octagonal rail-gun bunker seen from above, its three rail coils glowing gold as it charges; a thin bright red targeting line from the muzzle to the screen edge; then a 10 px rail shot: crimson with a dark rim and a white core.
- **Negative prompt:** the shared list above.
- **Animation brief:** Turret 32 headings (90°/s), 4 coil-glow steps. The line locks at the telegraph's start (1.0 s), then the rail fires along it (live 0.2 s, fading). Bolts glance off (hardened).
- **Mockup generator:** `python3 tools/concept/enemies_r60.py rail-bunker a`

## rail-bunker-r60-b

**Rail Bunker B, dashed line with running chevrons (Ascendancy, Level 33)**: the chosen r04 octagonal bunker (72 px, turret 32 headings) in the belt livery with a gold rim on the octagon; only what changed: the rail line and the shot. Files: `rail-bunker-r60-b.png` (sheet) and `rail-bunker-r60-b.gif` (the play field).

- **Prompt:** As A, but the targeting line is dashed red with small gold chevrons running outward along it, speeding up as the charge builds.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py rail-bunker b`

## laser-line-capture-r61-a

Concept round 61, frames of the [capture rig](../../../concept/prompts.md#belt-rig-capture-r61-a)'s run (a): the
480×540 play field at rig ≈ 49.5, 50.3 and 51.0 s (the telegraph, its flicker, the live line).

## rail-bunker-capture-r69-a

Concept round 69, a capture of the game, not generated art (M9 part I, step I3, 2026-10-29): the game built by `:desktop:installDist` from branch `m9-act-5` (the working tree after steps FIX3, FIX4 and I1, the full `check` green) on a private 960×540 Xvfb display, `--bench <s> --settings <scratch>/settings.properties`, `--level 33 --difficulty medium --invulnerable`, the plan's fit of [level-33-capture-r66-a](../../../../campaign/act-5-the-belt/level-33-leech-field/concept/prompts.md#level-33-capture-r66-a) with Rook, `controls.auto-fire=true`, the ship still (**run C**; the level time from the level's first frame). Clips: `ALSOFT_DRIVERS=wave` writing the game's own mix to a file, the screen recorded with ffmpeg `x11grab` at 30 fps, cut to the subject (≤ 1 s lead-in), cropped and muxed (H.264 main, AAC; the sound 0.47 s behind the recording, checked on the shots' cracks); checked on an eight-frame sheet. PNGs: frames of the same recording, labelled. Script: step I3's `clips69.py` (scratch).

- **rail-bunker-capture-r69-a**: t=69.3–71.9, the play field: the ridge pair's left bunker telegraphing its line from t≈69.9, the line's origin riding down on the turret as the bunker scrolls, the direction locked, the rail shot at t≈71.0 passing left of the still ship (round 66 C.1 b as built in FIX3); the PNG 320×480 at 0.6 at t=69.9, 70.4, 70.8, 71.0.
