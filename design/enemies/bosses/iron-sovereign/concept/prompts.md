# Iron Sovereign — concept prompts

Round 60 (Act 5 "The Belt" world proposals, step ART-P-world): a/b mockups by `tools/concept/belt_r60.py` on the
frozen `render` package (`python3 tools/concept/belt_r60.py sovereign [a|b]`), in the belt look of the gap
analysis's Q11 a and the fight of Q7 a (both pending the user's review in round 60). The rings are built from
identical hull segments (outer 12, middle 8, inner 6) rendered at 1-degree headings under one fixed top-left sun
and placed at their angle each frame, as the game would; the batteries are the SAM Nest's rack, the Rail Bunker and
the Sentinel Tower's emitter from `tools/concept/enemies_r60.py` (look a), each on a black collar whose four lamps
show gold out of the fire arc and red inside it. Each sheet measures the segment sprites and estimates the atlas
pages (the one-page boss budget).

**Shared look:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a vast battle station
seen from directly above in orbit, a strong key light from the top-left (the belt's sun), glossy highlights, crisp
silhouette. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least
a fifth of the silhouette (ring rims and teeth, battery collars, the core's petals), a thin bright gold rim on the
edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). Three concentric rings
round an armoured core of six shutter petals bearing the Ascendancy crest (a rising sun over a ringed planet), the
rings separated by dark gaps with catwalk light strips.

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, tilted view, motion blur, text,
logos other than the crest, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for
bullets), a planet in the foreground.

## iron-sovereign-r60-a

**Iron Sovereign A, continuous wheel (Ascendancy, Level 35)**: about 440 px across (core 80 px; ring bands 70–100,
123–157, 180–220 px from the centre). Files: `iron-sovereign-r60-a.png` (sheet) and `iron-sovereign-r60-a.gif` (the
play field, 26 s).

- **Prompt:** The shared look. Each ring a smooth continuous wheel: the segments butt into one ring with their
  seams hidden in the panel joints, a raised gold lip and gold teeth along every ring's outer rim, dark facet ribs
  across the deck with a red running light on each, small vents. The batteries on black collars with gold rings:
  six twin-tube missile racks on the outer ring, four rail-gun turrets on the middle ring, three laser emitters on
  the inner ring. The core: six armoured gloss-black petals with a gold ray and a gold ring arc on each, gold light
  in the slits between them.
- **Negative prompt:** the shared list above.
- **Animation brief:** Intro 2 s: lights come on ring by ring (outer → inner) and the rings spin up from rest.
  Outer 6°/s clockwise, middle 10°/s counter-clockwise, inner 120° in 4 s then a 4 s hold. The fire arc (120° on
  the ship's bearing, clamped to the lower half): a faint red band on each ring's hull, battery lamps gold → red.
  SAM salvos of two homing diamonds, the rail line (the Rail Bunker's red lock line, then the shot), the laser's
  crimson wedge and sweep during the inner ring's hold, the core's 5-bullet fan after its slits glow gold. A lock:
  the ring grinds to a halt over 2 s, its lights go out, its mounts are wrecks. The core opens over 3 s (petals
  slide apart, white-gold reactor), the gold hex shield, a 2-arm spiral, a Dart stream from a rim bay after its
  gold door-light. Death: the batteries cook off ring by ring (0.15 s apart), the rings crack at their segment
  joints and drift apart turning, the core folds inward and blows, a screen flash.
- **Mockup generator:** `python3 tools/concept/belt_r60.py sovereign a`

## iron-sovereign-r60-b

**Iron Sovereign B, armoured links (Ascendancy, Level 35)**: the same station and fight. Files:
`iron-sovereign-r60-b.png` (sheet) and `iron-sovereign-r60-b.gif` (the play field).

- **Prompt:** As A, but every ring a chain of separate armour blocks: each segment a block with chamfered ends and a
  dark gap to the next, a gold-edged cap plate on top and a gold stripe along its outer edge, red lights at the
  block ends; a gold track ring under each ring shows through the gaps. No teeth: the moving gaps read the turning.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A; at the death the blocks separate along the gaps.
- **Mockup generator:** `python3 tools/concept/belt_r60.py sovereign b`
