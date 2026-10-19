# Mars – generator notes and prompts

Round 36 (Act 3 "Red Dust" design phase). Every file here is made by
`python3 tools/concept/mars_r36.py [subject …] [--no-gif]` (no subject = everything, about five
minutes) on the frozen `tools/concept/render/` package and the earlier scene helpers
(`parallax_r02/r03`, `scenes_r06`); units on the stills are the round-36 proposals of their own
parts, variant a (`tools/concept/enemies_r36.py`), and the chosen round 04 Vrell, for scale only.
Each PNG is a review sheet with the 480×540 play field at 1×; each GIF is a seamless 4 s loop at
25 fps with the bullet colours reserved. Rules: [art direction](../../../art-direction/README.md)
(layer model, *Perspective between layers*, decoration and atmosphere intensity, readability).

## mars-olympus-r36-a

**Olympus descent (L15)**: high over the Olympus Mons flank, down through two cloud decks.
File: `mars-olympus-r36-a.png` (still + layer breakdown).

- **Prompt:** late-1990s pre-rendered CGI shoot'em-up backdrop, straight top-down orthographic view
  from very high altitude over Mars, key light from the top-left. Far below: the flank of a giant
  shield volcano in dark basalt and rust (#523428, #804228), a caldera of three overlapping circular
  collapse pits with steep shadowed walls and flat dark floors, radial lava channels and an outer
  scarp, hazed toward a butterscotch sky colour (#d6a670). Between camera and ground: a deck of
  Martian water-ice cloud, pale peach tops (#ead6be) and cool mauve undersides (#926e64), with a
  wide gap over the caldera, casting soft shadows on the volcano. Cloud tongues at the frame's edges
  only.
- **Negative prompt:** photograph, painterly, perspective camera, horizon, Earth-blue sky, white
  cumulus, green vegetation, text, watermark
- **Layers:** deep ×0.4 volcano · far ×0.7 lower cloud deck (below the play plane, never over a
  bullet; flyers shadow its tops) · low-air ×1.3 upper-deck tongues at the edges (≈ 20 %) ·
  high-air ×2 ice wisps under 35 %.

## mars-valles-r36-a

**Valles Marineris (L16)**: the canyon in true perspective with the Helix research station on the
north rim. File: `mars-valles-r36-a.png`.

- **Prompt:** late-1990s pre-rendered CGI shoot'em-up backdrop, top-down view down into a deep
  winding red canyon, key light from the top-left. Plateau of rust regolith with low ridges and a
  few craters; the canyon walls drop in eight layered strata bands (#c47848, #964e2c, #763a22)
  to a pale, hazed sandy floor with a dry meandering riverbed and talus at the foot of the walls;
  the left wall's face in shadow, throwing a shadow strip on the floor; the right face sunlit. On
  the left plateau, near the rim: a compact research station of glossy black blocks with gold roof
  chines, warm lit skylight strips, a gold-tinted glass atrium, a gold-ringed landing pad with an H,
  a mast with a red light — immaculate, the only gloss on the planet.
- **Negative prompt:** photograph, painterly, side view, horizon, rivers of water, vegetation,
  damage or Vrell growth on the station, text, watermark
- **Notes:** canyon camera model a (the decided rule); see `canyon-walls-r36-a/b`.

## mars-ares-landing-r36-a

**Ares Landing (L17)**: the outer domes, the rail line from the north, the ring road, greenhouses,
a gun pit, Vrell growth and the untouched Helix compound. File: `mars-ares-landing-r36-a.png`.

- **Prompt:** late-1990s pre-rendered CGI shoot'em-up backdrop, straight top-down orthographic,
  key light from the top-left, dull rust plain (#904e30). Two geodesic glass colony domes glowing
  green inside (crop rows visible) on round concrete aprons with airlocks and greenhouse annex
  tubes; a double-track rail line running in from the top; an arc of grey ring road; a sandbagged
  gun pit with a small turret; three burnt, roofless greenhouse frames under thin dark smoke;
  violet Vrell nest growth with glowing teal veins creeping in from one corner; in the opposite
  corner a black-and-gold compound behind a clean white fence, lit, landing pad, no growth near it.
- **Negative prompt:** photograph, painterly, perspective camera, green fields outside the domes,
  blue sky, text, watermark
- **Units shown:** Spore Bombers steering for Lowell (the hook), the round-36 Choir Herald a with
  four Needlers.

## mars-storm-plains-r36-a

**Storm plains (L18)**: the northern plains inside the dust storm, radius look a. File:
`mars-storm-plains-r36-a.png` (still, layer breakdown, the look's rules panel).

- **Prompt:** late-1990s pre-rendered CGI shoot'em-up frame, top-down, a Martian rust plain with
  dry branching channels, a half-buried pipeline on short supports, a wrecked six-wheeled rover
  with a red beacon, a dust devil (a spiral of pale dust round a dark eye ringed in lime light);
  everything beyond a 170 px circle round the player's ship dissolves into a flat, banded
  orange-brown dust veil (#8a5432 to #d6a26e); enemy aircraft outside the circle are dark
  silhouettes showing only their lights (white running lights, a red sensor eye, teal Vrell glow);
  bright enemy bullets are crisp everywhere.
- **Negative prompt:** photograph, fog that hides bullets, night, blue tint, vignette to black,
  text, watermark

## mars-tharsis-foundry-r36-a

**Tharsis Foundry (L19)**: slag fields, conveyor halls, furnace row, vents, the smelter tower.
File: `mars-tharsis-foundry-r36-a.png`.

- **Prompt:** late-1990s pre-rendered CGI shoot'em-up backdrop, top-down, key light from the
  top-left: a dark basalt slag field with sparse dull-orange glowing cracks (#c44a16); two long
  roofless conveyor halls running up the frame — black rubber belts with grey slats and painted
  amber arrows pointing up, rollers along the edges, low light-grey concrete walls, steel girders
  spanning the halls every 90 px (flat, no perspective lean); a row of furnace mouths glowing
  white-orange across the top; round floor vents with grates glowing orange; the top of a round
  smelter tower at the right edge with a glowing core; dark smoke drifting. Grey unmarked tanks
  ride the belts.
- **Negative prompt:** photograph, painterly, perspective lean on the girders, logos or faction
  markings on the tanks, gold trim, text, watermark

## mars-hellas-r36-a

**Hellas basin (L20/L21)**: terraces, the dune sea, the travelling ridge, the lime hollow. File:
`mars-hellas-r36-a.png`.

- **Prompt:** late-1990s pre-rendered CGI shoot'em-up backdrop, top-down, key light from the
  top-left, pale ochre sand (#cca06c to #f4deb6): the basin wall stepping down in five terraces at
  the top; below, a dune sea with soft wind ripples; a long sinuous hump of sand running
  diagonally, pushed up by something moving underneath, its crest cracked with thin glowing lime
  lines (#a8ff2a), sand sliding off its flanks, a long cast shadow on its lower-right side; at the
  bottom a darker bowl-shaped hollow whose floor is veined with glowing lime cracks, lit from below.
- **Negative prompt:** photograph, painterly, a visible creature, eyes, teeth, red rust colours,
  text, watermark
- **Animation brief (later, L20/L21):** the ridge travels across the field at about 60 px/s,
  sand avalanching off its flanks, the crest cracks pulsing; it sinks before the Colossus breaches.

## mars-panorama-r36-a

**A wide still (960×540)**: the act in one view, a mood piece for the title card and the
briefing images' horizons. File: `mars-panorama-r36-a.png`.

- **Prompt:** late-1990s voxel-terrain flight-sim render (Comanche / NovaLogic look), a raised
  camera over a rust Martian plain, three green-lit glass colony domes in the foreground, a long
  canyon cut across the middle distance showing its sunlit far wall, the broad shield of Olympus
  Mons on the left horizon, a towering brown dust-storm front rolling in from the right, a small
  pale sun with a faint halo top left in a butterscotch sky (#d6a670) darkening toward the top,
  96-colour palette with visible banding.
- **Negative prompt:** photograph, blue sky, clouds like Earth, green vegetation, text, watermark

## canyon-walls-r36-a

**Rejected** (round 36, 2026-10-13: b picked); the files are in `rejected/`.

**Canyon walls A, the rim is the collision line (L16)**. Files: `rejected/canyon-walls-r36-a.png` (frame,
collision overlay, the frame 1 s later, side view) and `rejected/canyon-walls-r36-a.gif` (160 px/s).

- **Prompt / brief:** as `mars-valles-r36-a`; the plateau and the rim scroll at ×1.0 with the
  ground; the wall faces are drawn in eight strata bands from the rim (scale 1.0) to the floor
  (scale 0.6), projected round the play field's (240, 297) — the camera model of Level 08's towers
  — so the walls turn as they scroll. The rim is the collision line (white on the overlay); the
  floor is far and holds nothing to hit. Tendril-anchor pairs sit on the rims. Matches the
  art-direction rule ("canyon walls and cliffs run from the rim at scale 1.0 down to the floor at
  the `far` scale").
- **Negative prompt:** flat painted walls, walls leaning outward, a floor scrolling at the
  ground's speed

## canyon-walls-r36-b

**Chosen** (round 36, 2026-10-13).

**Canyon walls B, the wall's foot is the collision line (L16)**. Files: `canyon-walls-r36-b.png`,
`canyon-walls-r36-b.gif`.

- **Prompt / brief:** the canyon floor scrolls at ×1.0 and carries the ground units; the walls
  rise toward the camera from the floor (1.0) to the plateau (1.25), leaning outward from the
  centre like Level 08's towers; the plateau races past at 1.25×. The foot of the wall is the
  collision line; by the tower rule (user decision D1, M5 part B) nothing gameplay touches stands
  on the leaning face or the plateau, so the anchors sit at the foot. Choosing b amends the
  art-direction rule for canyons.
- **Negative prompt:** walls narrowing toward the camera, a deep far floor

## storm-radius-r36-a

**Rejected** (round 36, 2026-10-13: b picked); the files are in `rejected/`.

**Storm radius A, a clear bubble (L18)**. Files: `rejected/storm-radius-r36-a.png` (frame, layers, the
visibility mask and rules), `rejected/storm-radius-r36-a.gif` (120 px/s).

- **Brief:** visibility 170 px round the ship with a hard 10 px edge; inside, the ground and
  low-air are drawn (tinted 20 % by the dust); outside, a moving banded rust veil replaces them;
  air units outside are dark silhouettes keeping only their glow (Vrell teal/lime, the Ghost
  Drone's red eye and white running lights, added to the silhouette as its spec says);
  bullets, the ship and its shots are always drawn (M6 = a); the high-air streaks stay under 40 %.

## storm-radius-r36-b

**Chosen** (round 36, 2026-10-13).

**Storm radius B, a soft, ragged falloff (L18)**. Files: `storm-radius-r36-b.png`,
`storm-radius-r36-b.gif`.

- **Brief:** as A, but the visibility fades over about 120 px around 170 px, its edge broken by
  two drifting noise fields (dust tongues), and units fade gradually from drawn to silhouette.

## dome-r36-a

**Chosen** (round 36, 2026-10-13).

**Ares Landing dome A, geodesic glass (L17 ally)**. File: `dome-r36-a.png` (field with Bradbury
and Lowell and a hooked bomber, the 120 px dome at 2×, Eden at 144 px, the states at 1×).

- **Prompt:** late-1990s pre-rendered CGI game sprite, top-down orthographic, key light from the
  top-left: a colony greenhouse dome — a glass hemisphere on white struts in a triangle (geodesic)
  lattice, dark green glass glowing green from inside with visible crop rows, on a concrete ring
  with four small airlock blocks and two green glass annex tubes. 120 px footprint (Eden 144).
- **Negative prompt:** photograph, side view, transparent with a visible floor plan, blue glass,
  text, watermark
- **States brief:** hit = amber hairline cracks radiating from an impact point, a few dark panes;
  broken = the glass gone, the inside dark and dead, white shards scattered round the ring. The
  concept states are overlays; production renders them.

## dome-r36-b

**Rejected** (round 36, 2026-10-13: a picked); the files are in `rejected/`.

**Ares Landing dome B, ribbed pressure dome (L17 ally)**. File: `rejected/dome-r36-b.png`.

- **Prompt:** late-1990s pre-rendered CGI game sprite, top-down orthographic, key light from the
  top-left: a low colony habitat dome of off-white composite with twelve radial grey ribs, a ring
  of green-lit glazing near its base and a green ribbed skylight on top, banked with a brown
  regolith berm, two short airlock tubes. 120 px footprint (Eden 144).
- **Negative prompt:** photograph, side view, glass all over, blue, text, watermark
- **States brief:** hit = amber cracks on the shell, a darkened sector; broken = the shell breached
  (a dark ragged hole), the glazing dead, debris on the berm.
