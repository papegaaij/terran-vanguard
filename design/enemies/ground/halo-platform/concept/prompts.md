# Halo Platform — concept prompts

Moved here from [the ground roster's prompts](../../concept/prompts.md) on 2026-10-31 (Act 6 design phase, step D-enemies-B)
with the Halo Platform's concept files, unchanged. The generators still write to `ground/concept/`; the round-72
proposal step writes this part's new files here. Round 06's variant is in `rejected/`.

## halo-platform-r06-a

**Halo Platform (Ascendancy rotating turret ring)** — a ~300 px multi-part fortress centrepiece. Files: `halo-platform-r06-a.png` (sheet) and `halo-platform-r06-a.gif` (motion in the 480×540 play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. A human-built fortress weapon seen from directly above: a shielded octagonal armoured core in black-violet (#201430) with gold trim and a red reactor eye under a glass dome, surrounded by a ring of six curved armoured rail segments with gold edge trim, each carrying a domed turret with twin barrels and a white (#e8e4f0) accent; thin gold/red rim light. Render core, one ring segment and one turret as separate sprites.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, organic shapes, magenta or hot-pink glow, yellow glow, blue or cyan body colour
- **Animation brief:** The ring rotates around the core (segments at 32 headings); each turret aims at the player independently (32 headings) and fires gold shells as it swings round. Each destroyed turret speeds the ring up; with half the ring gone the red hexagonal shield around the core drops and the core is exposed.
- **Mockup generator:** `python3 tools/concept/enemies_r06.py halo-platform` (models `tools/concept/render/r06_models.py`, rendered with `ModelSpaceAngleSprites` from `tools/concept/render/enemy_rigs.py`)

## halo-platform-r07-a

**Halo Platform with smooth rotation (round 07)**: the Ascendancy rotating turret ring, re-rendered because the round-06 rotation was "very jagged". Files: `halo-platform-r07-a.png` (sheet) and `halo-platform-r07-a.gif` (motion in the 480×540 play field, 25 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, on a dark violet hex-plated fortress deck. A huge Ascendancy turret platform seen from directly above: a shielded octagonal black-and-gold core (#141018 hull, #c08000 gold trim) with a red reactor eye under a glass dome, surrounded by a closed ring of six curved black armour segments with gold edge trim and white accents. Each segment carries a domed twin-barrel turret, and a thin red hexagonal energy shield surrounds the core. Thin gold rim light on top-left edges, red on bottom-right edges.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, organic shapes, chitin, insect, magenta glow
- **Animation brief:** The ring turns slowly and perfectly smoothly around the core, 0.35 rad/s at first. It is pre-rendered as one 6-fold model over a single 60° step at 128 frames (0.47° per frame, 768 per full turn), so its outer edge moves about 0.9 px per frame change. All ring frames share one palette. Turrets ride on the segments and track the player at 64 headings, firing gold shells. Each destroyed turret leaves a scorched stump and ramps the ring speed up by 0.25 rad/s over 0.8 s, with no jump. With half the turrets gone the red hex shield fades out and the core is exposed.
- **Mockup generator:** `python3 tools/concept/enemies_r07.py halo-platform`. The assembled ring is `halo_ring()` in `tools/concept/render/r07_models.py`, built from the round-06 `halo_segment`. Its 128 frames render in parallel processes (about 15 s each) and are cached in the system temp dir. This follows the "Smooth slow rotation" rule in design/art-direction.

## halo-platform-r72-a

**Halo Platform a — "gold trim": the chosen r07 ring re-rendered in the elite livery (round 72, J8).** Files: `halo-platform-r72-a.png` (sheet) and `halo-platform-r72-a.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The r07 platform unchanged in shape, in the elite livery, gloss black hull (#1a1622, hex panelling) with heavy gold (about 40 % of the silhouette), red running lights and a strong 2 px gold rim on the top-left edges: the ring's gold edge rails, the turret mount rings and the segments' end caps gold; three twin-barrel gun turrets and three rail turrets (one long barrel with three gold coils) alternating; a red reactor eye under a glass dome in the core.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, organic shapes, chitin
- **Animation brief:** The ring as r07: one 6-fold model, 128 frames per 60° step, turrets at 64 headings; 20°/s, +8°/s per mount lost, eased over 0.8 s. The live 120° arc facing the ship is a faint red band on the ring and a mount's light turns gold → red inside it. The core's red hex barrier drops when the third mount falls; the eye flares white 0.5 s before each ring of 10. Rail turrets telegraph a thin red line for 1 s. Every glow, haze and shadow is a smooth gradient of at least 32 steps, drawn once, with no ordered dither (the 2026-10-30 dither audit).
- **Mockup generator:** `python3 tools/concept/enemies_r72.py halo-platform [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## halo-platform-r72-b

**Halo Platform b — "gold decks": the same ring with gold top decks between black rails (round 72, J8).** Files: `halo-platform-r72-b.png` (sheet) and `halo-platform-r72-b.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The same platform in the elite livery with the segments' top decks gold between black edge rails, the mount rings black, the core's drum gold.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, organic shapes, chitin
- **Animation brief:** As a. Every glow, haze and shadow is a smooth gradient of at least 32 steps, drawn once, with no ordered dither (the 2026-10-30 dither audit).
- **Mockup generator:** `python3 tools/concept/enemies_r72.py halo-platform [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## halo-platform-final-r79-a

**Halo Platform production frames** (round 79, M10 part G, step ART-G1). Files: `halo-platform-final-r79-a.png` (sheet)
and `.gif` (1×, the held arena). Generator: `python3 tools/art/halo_platform.py` (frames `halo-platform-segment_0..87`,
`-rim`, `-shade`, `-arc`, `-lamp-gold`, `-lamp-red`, `-gun_0..63`, `-rail_0..63`, `-gun-muzzle_0..3`,
`-rail-charge_0..3`, `-gun-wreck`, `-rail-wreck`, `-core_0..9`, `-core-wreck`, `-barrier_0..11`, `-barrier-hit_0..5`,
`-eye-glow_0..3`, `-eye-flare_0..3`, `-cookoff_0..9`, `-cookoff-debris_0..9`, `-blowout_0..11`, `-scorch`,
`pivots/halo-platform.json`).

- **Brief:** the chosen r07 model in round 72's a "gold trim" (`tools/concept/enemies_r72.py` `halo_segment_v('a')`,
  `halo_gun_v`, `halo_rail_v`, `halo_core_v`, unchanged): gloss black, the gold rails, mount rings and end caps, red
  running lights. The ring by QG-1's segment rule: one segment rendered top-lit at 0–45° in 87 steps (the outer edge
  moves ≤ 1 px a step), drawn at any heading by quarter turns and a mirror; the Sun's top-left light and the elite rim as
  static overlays over the band; the turrets (gun, rail) at 64 headings and the core (76 px, the eye shut → waking →
  open → the white flare) sunlit; the red hex barrier, the arc band, the lamps (gold → red), the eye's glow and flare,
  the cook-off and the eye's blowout as smooth additive glows (≥ 32 steps, no ordered dither, drawn once); the wreck
  the segments tinted under a soot overlay with the burnt core and stumps.

## halo-rotation-final-r79-a

**QG-1's proof** (round 79, step ART-G1). Files: `halo-rotation-final-r79-a.png` (sheet) and `.gif` (50 fps, 1×).
Generator: `python3 tools/art/halo_platform.py --review` (from the written frames).

- **Brief:** the ring composed by the segment rule turning at 20°/s and 60°/s: ten consecutive 60 fps frames of a
  quarter at 1:1 and five at 3× (a join and a mount ring), the outer edge's measured step per frame against the true
  motion and its lag, the rule's turned and mirrored frames against direct renders at headings off the set; the GIF the
  two speeds side by side.
