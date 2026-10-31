# Jovian system — concept prompts

Round 72 (Act 6 "Jovian Storm" design phase, step ART-R-B, 2026-10-31): a/b mockups by `tools/concept/act6_r72.py`
(`python3 tools/concept/act6_r72.py moods|banks|city [a|b]`), pending the user's review in round 72. The backdrops are
procedural (storm bands, towering cloud heads, Io's surface, Callisto's ice, space), the Aurelia parts are
`render/scene_models.py`'s (the chosen round-06 scene's), the units and bullets the production sprites
(`assets/sprites`) plus Harker's ace from the Honour Guard's proposal. **Every cloud, haze, glow and shadow is a
smooth float gradient drawn once** (no ordered dither, no posterising; the 2026-10-30 dither audit's rule).

**Shared look:** late-1990s pre-rendered CGI backdrop, straight top-down, Jupiter's cream, ochre and rust storm bands
under a warm top-left sun (the chosen round-06 scene); Aurelia's gilded decks, domes and lift balloons; Io's
sulphur and black lava; Callisto's dark blue-grey ice and the black citadel; black space with Jupiter's limb.

## jovian-moods-r72-a

**Moods per level, A: a grade per level (Q10 a)**. Files: `jovian-moods-r72-a.png` (sheet).

- **Brief:** The ladder L36 (high sunlit bands), L37 before the turn (Aurelia's decks a step paler than the units'
  gold) and after it (the sky a step darker, fire-orange glows, teal Vrell growth with glowing seams, smoke), L38
  (storm-dark slate cloud walls, the Red Spot's rust glow far off, a lightning column's blue-white telegraph), L39
  (desaturated sulphur, black lava, frost, vent glows, the refinery lines), L40 (black space, Callisto's disc,
  Jupiter's limb, the fleet's lights), L41 (the black citadel on blue-grey ice, long low-sun shadows, red and gold
  lights), L42 (Callisto orbit, the home fleet's cruisers far off). Each with its mean luminance and its brightest
  and darkest 200×96 window, and the readability set laid over both windows at 1×: a red sniper line, a Vrell spore,
  an elite ace, a gunmetal Talon, the ship, Rook, gold needles and magenta orbs.

## jovian-moods-r72-b

**Moods per level, B: one storm-dusk grade (Q10 b)**. Files: `jovian-moods-r72-b.png` (sheet).

- **Brief:** A's scenes under one grade: darker, violet shadows, amber highlights.

## jovian-banks-r72-a

**Foreground banks, A: cream veil (Q2 a)**. Files: `jovian-banks-r72-a.png` (sheet).

- **Brief:** Level 36's brightest cream band at 1×, a foreground bank on `high-air` (towering cloud heads in the band's
  own lit cream, about a quarter of the field, clear of the ship's band) drawn at 65 % over Talons, an ace and a
  Chimera-sized body (the Mote Swarm stands in), their red running lights and a muzzle tell at full above it; the
  bullets, the ship, Rook and a pickup above everything; a 2× crop with and without the bank.

## jovian-banks-r72-b

**Foreground banks, B: shadowed underside**. Files: `jovian-banks-r72-b.png` (sheet).

- **Brief:** As A, the bank in a cool grey-ochre shade (its underside): bodies sink as dark shapes under it.

## falling-city-r72-a

**The falling city, A: the slow tilt (Q4 a, Level 37's last 40 s)**. Files: `falling-city-r72-a.png` (storyboard
sheet).

- **Brief:** Eight key frames at 0.5×: the balloons tear and collapse (no pop); the main deck tilts and drops; over
  20 s the city layer slows, darkens and shrinks toward the centre (scale 1.0 → 0.42, hazing toward the deep cloud's
  rust) in one piece, tilting; smoke rising from the decks; the ship flying down beside the spires (on `low-air`)
  through debris and Vrell; the last wave; the last kill with Okafor's line; open cloud. A 40 s bar under them.

## falling-city-r72-b

**The falling city, B: breaking up**. Files: `falling-city-r72-b.png` (storyboard sheet).

- **Brief:** As A, but the deck breaks along a fault line: the two halves drift apart, turn opposite ways and sink at
  their own rates; the spires topple on them.

## falling-city-r73-b

**The falling city, B: breaking up along a jagged fault** (round 73, step m10-f72, 2026-10-31; round 72 A.11's rule
for the Ascendant applied to the chosen A.13 b). Files: `falling-city-r73-b.png` (storyboard sheet), generated with
`python3 tools/concept/act6_r72.py city73 b` (the r72 sheet stays as it was).

- **Why:** in `falling-city-r72-b` the fault line is a polyline of five straight segments and it crosses the city on
  its walkway struts, so the strut ends are cut clean and straight, the same "clean cut" the user flagged on the
  Ascendant (round 72 A.11): a hull breaks along a **jagged fracture**, never a straight clean cut.
- **Brief:** as `falling-city-r72-b`, but the city stays whole until the deck drops (−36 s) and then breaks along a
  seeded ragged version of the same fault (`ragged_line`, seed 7311): ragged strut and deck ends with torn plate flaps
  bent outward on both halves, charred and glowing hot at the edge (`torn_half`), shards flung into the widening gap
  (larger and brighter while near, darker and smaller as the halves sink), dark smoke puffs and fire glow venting
  from both torn edges (`city_break`, `break_effects`; smooth Gaussian puffs and glows, no dither). The halves drift,
  turn and sink as in r72; the spires topple on them.
- **Prompt (AI generator):** late-1990s pre-rendered CGI, straight top-down, a golden cloud city of octagonal decks on
  walkway struts breaking in two over Jupiter's rust-coloured cloud bands, the struts snapped with ragged torn ends
  and bent plates, charred edges glowing orange, debris shards and smoke venting from the break, the two halves
  sinking apart into the haze.

## lightning-final-r73-a

**Lightning production frames** (round 73, M10 part A, step ART-A). Files: `lightning-final-r73-a.png` (sheet) and
`.gif` (1×). Generator: `python3 tools/art/act6_gear.py lightning` (frames `lightning-tell_0..1`, a 48×64 tile repeated
down the column, and `lightning-strike_0..2`, 112×540).

- **Brief:** the telegraph as the hazards row has it: a 40 px column with faint blue-white edges and a thin blue-white
  line down its middle, brightening for 1.0 s (never the red of an enemy `laser-line`); the strike as QA-2's pick; Level
  12's sky flash with it (the review uses a stand-in flash). Additive, smooth, drawn once.

## lightning-strike-r73-a

**QA-2 a, "jagged bolt"** (round 73, recommended). Files: `lightning-strike-r73-a.png` (sheet) and `.gif` (1×).
Generator: `python3 tools/concept/act6_r73.py lightning a` (frames from `tools/art/act6_gear.py`).

- **Brief:** a jagged blue-white (#aac8ff) bolt down the whole play field, wandering inside the 40 px column, a white
  core, a blue fringe, two short side forks (one in each half, down and out), the lane lit faintly; three shapes 2 steps
  each through the 0.25 s, faded in the last 0.1 s; shown over round 72's Level 36 grade and Level 38's storm.
- **AI prompt:** "pre-rendered 1990s CGI game effect, a vertical jagged blue-white lightning bolt from the top of the
  screen to the bottom with a white core and two short forks, over storm clouds, top-down shoot'em up"

## lightning-strike-r73-b

**QA-2 b, "light column"** (round 73). Files: `lightning-strike-r73-b.png` (sheet) and `.gif` (1×). Generator:
`python3 tools/concept/act6_r73.py lightning b`.

- **Brief:** a straight full-height column of white light filling the 40 px lane with a thin jagged core line, its
  soft edges fading to the core over the 0.25 s (four frames).
- **AI prompt:** "pre-rendered 1990s CGI game effect, a straight vertical column of white-blue light with a jagged
  bright core line, fading at the edges, over storm clouds, top-down shoot'em up"
