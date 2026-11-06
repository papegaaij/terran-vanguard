# Husk — concept prompts

## husk-r84-a

**Husk a — "punctured": an ovoid hull punched open from inside (round 84).** Files: `husk-r84-a.png` (sheet) and `husk-r84-a.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. A medium derelict Vrell hull (88×72 px, three hull shapes) of dull lightless ash grey-brown plates (#6e645c, dark #2a2622, paler lip edges), irregular brick-laid plates with dark seams; one to three round holes punched from the inside, the plates round each hole bent outward, a dark empty hollow inside; pale cold crystal (#b8c8d4) growing along the cracks that run from the holes, faintly lit from within. No glow anywhere else.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, bubbles
- **Animation brief:** 32 angles, turning slowly as it drifts. Cracked (at half its HP): a 0.5 s tell in which the crystal flares pale once and splits wider and longer, then a fan of 5 crystal shards. Death: the hull folds in, slow grey slabs and pale crystal splinters fade, a ring of 8 slow outlined orbs. Every glow, haze, ring and beam is a smooth gradient of at least 32 steps that fades to 0 inside its canvas, with no ordered dither.
- **Mockup generator:** `python3 tools/concept/enemies_r84.py husk [a|b]` (round 84, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## husk-r84-b

**Husk b — "split": a hull torn open along its spine (round 84).** Files: `husk-r84-b.png` (sheet) and `husk-r84-b.gif` (the 480×540 play field, 20 fps).

- **Prompt:** late-1990s pre-rendered CGI game sprite, 3D model rendered straight top-down orthographic, key light from the top-left, glossy specular highlights, crisp silhouette, isolated on a flat dark background. The same plating, colours and crystal: one long tear down its back (straight, crooked, or with a side hole), the plates on both lips peeled outward, dark ribs bridging the empty inside, crystal growing along both lips.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, three-quarter view, text, watermark, motion blur, dithered gradients, bubbles
- **Animation brief:** As a. Every glow, haze, ring and beam is a smooth gradient of at least 32 steps that fades to 0 inside its canvas, with no ordered dither.
- **Mockup generator:** `python3 tools/concept/enemies_r84.py husk [a|b]` (round 84, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## husk-final-r91-a

**Husk, production** (round 91, M11 part D, step ART-D, 2026-11-06; production art, pending the user's review). Files:
`husk-final-r91-a.png` (sheet) and `.gif` (three Husks drifting through the quiet over Level 46's production
backdrop; one cracks: the tell, then the collapse). Generator: `python3 tools/art/act7_deadfleet.py husk` (round 84's
`husk_model("a", shape, crack)` from `tools/concept/enemies_r84.py`, imported unchanged, on the production path: 4×,
1-bit alpha, 48 colours a hull shape; held in `build/art-held/sprites/` until step D2's unit data claims `husk`).

- **Brief:** A.3 a "punctured" as chosen: three hull shapes × 32 headings (11.25° apart, 104×104 canvas, the hull
  88×72), whole and cracked (the holes torn wider, the crystal split longer and lit); graded to Level 46's cold key and
  a step darker and harder than the scenery hulks (the paler, flatter backdrop hulks: the sheet's 1:1 row). The tell:
  a pale cold flare swelling round the hull with crystal glints over 0.5 s, peaking at frame 3. The collapse: the hull
  folds in and breaks into nine slow grey slabs turning under the fixed light and pale crystal splinters flung
  farther, shrinking away, with their cold glints and a faint haze decaying from the first frame (never a flash). The
  shard: a pale ice crystal shard with a white core and a dark outline at 16 headings, the point leading; the orb: a
  pale crystal ring on a dark rim, its white core pulsing. The intel portrait (30×30): shape 1 cracked, nose down
  (`tools/art/intel.py husk`). Every glow a smooth float gradient (≥ 32 steps, no dither), fading to 0 inside its canvas.
