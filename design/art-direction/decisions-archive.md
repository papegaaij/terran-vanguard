# Art direction — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-09-30: Late-90s pre-rendered CGI sprite look; 16:9 screen with a portrait play field and
  HUD side panels (project kick-off).
- 2026-09-30: Concept round 01: palette **B "90s Neon CGI"** chosen; A and C rejected.
- 2026-09-30: Concept round 01: both parallax scenes rejected — A too empty with the ground scrolling too slowly, B too crowded and busy. Redo in round 02.
- 2026-09-30: Resolution **960×540** chosen (open question resolved): play field 480×540 at
  x = 240, side panels 240×540, player ship 48×48; all sprite sizes scale 1.5×. Palette B is the
  reference palette for all art from round 02 on.
- 2026-09-30: Layer model revised for round 02 (draft, under review with the round 02 parallax
  scenes): new `far` layer, deep 0.12, low-air 1.35, high-air 2.2, shadow offsets scaled to
  960×540, ground speed guideline, density guideline, perspective walls between layers.
- 2026-09-30: Concept round 02: all three parallax approaches liked — density follows level pace (B calm, C normal, A fast). The revised layer model (far layer, faster foreground factors, speed guideline) is adopted with them.
- 2026-09-30: Concept round 02 feedback: scenes need more decoration — heavier clouds / mist / dust streaks and vegetation. Decoration guideline added; the three scenes get a decoration pass in round 03.
- 2026-09-30: Concept round 03: decoration pass chosen for all three scenes; it represents the heavy end. Atmosphere intensity must vary through a level (clear / light / medium / heavy per section, heavy only as a short peak).
- 2026-09-30: Enemy variety (user feedback after round 04): turning enemies are pre-rendered at
  16 angles (32 for large/slow ones and turrets) and the nearest frame is shown; sprite-size
  table extended with tiny and huge tiers; animation rules added for independent parts, segment
  chains (per segment type, not per pose), articulated parts, walk cycles and radially
  symmetric spinners.
- 2026-10-01: Concept round 06: Luna, belt, Jupiter and Vrell-space scenes chosen; Europa rejected (not recognisably under water) — redo with vegetation, bubbles and caustics. New rules: smooth slow rotation (dense angle sets for large rotating structures) and water-surface interaction (waterline foam, ripple trains, visible submerged parts, top-down surfacing).
- 2026-10-01: Menus and other out-of-game screens use the glass-over-scene style of main menu A; the bevelled metal style is reserved for the in-game HUD (user decision, round 06).
- 2026-10-01: Concept round 07: Europa A "kelp forest" chosen; B "open water" rejected.
- 2026-10-01: Scaling: integer scaling with letterboxing by default, sharp-bilinear as an option. Banking: 5 frames for the player ship and wingman. Fitted wing pods are drawn on the ship sprite. Layer hit rules settled in [enemies](../enemies/README.md#layer-rules).
- 2026-10-01: Concept round 08: open-ocean scene deferred — it must be finished first (round 09, with the other Earth scenes).
- 2026-10-01: Concept round 09: arctic and Luna far side scenes and the explosions chosen; Geneva to be redone (not recognisable as a city); storm and ocean to be calmed and smoothed. New rule: motion budget for scene animation.
- 2026-10-01: Concept round 10: Geneva, storm and ocean revisions chosen ("much better now").
- 2026-10-01: High-air drawing rule (user decision): the ~40 % opacity cap applies only to weather and decoration. Enemies on `high-air` (Leviathan overhead pass, Brood Carrier pass) are drawn fully opaque, scaled per the perspective rule, and cast shadows.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../reviews/acts-1-2/README.md).
- 2026-10-01: M1 rendering: the play plane draws over the Earth orbit layers (deep 0.12, far 0.45, ground 1.0, low-air 1.4, high-air 2.4 at ≤ 40 % opacity, additive) with the ground at Level 01's 130 px/s. Placeholders are the 0.4× layer breakdown of `parallax-r03-a.png`, enlarged with linear filtering and mirrored every second tile so they scroll without a seam; bolts and explosions are drawn additively; the hit flash is a GLES 2 shader that blends a sprite towards a colour. Positions are interpolated between simulation steps and rounded to whole pixels.
- 2026-10-02: Readability rule 6 follows the ship's hull-shaped hit box (user decision); the old "about 6×6 px" note was out of date (the ship doc said 9×9).
- 2026-10-02: M2 backdrop: the game no longer repeats one crop of `parallax-r03-a.png` per layer.
  Levels lay out their backdrop as data (tile sets per section and layer, placed set pieces,
  atmosphere intensity per section), and the content loader checks the density and motion-budget
  rules above. Placeholders for Level 01 are rendered from this scene's kit and palette
  (`tools/concept/backdrop_l01.py`); see [Level 01](../campaign/act-1-first-contact/level-01-break-at-dawn/README.md#backdrop).
- 2026-10-02: Readability rule 7, loot targets read as targets (user decision, after playing
  Level 01: the code-drawn containers and beacon did not stand out from the yard): warm hazard
  markings against cool structures, 1 px light rim, glint every ~2 s, hit flash, damage state,
  break-apart; secrets also blink and are at least as large as a container's marking.
- 2026-10-02: Warm accents on structures resolved (user decision): warm amber on the ground layer
  is reserved for loot targets; structures and scenery use muted accents (the UTC kit's orange
  `ff7a2a` becomes beige ochre `a48366`). Level 01's backdrop placeholders were re-rendered with
  it (`tools/concept/backdrop_l01.py`); the chosen concept scenes keep their orange.
- 2026-10-02: Bullets and loot told apart by look, both reserved-colour rules kept (user
  decision): enemy bullets are saturated and glowing with a bright core (rule 1); loot markings
  are dull matte amber with black hazard stripes, and only their glint shines (rule 7).
- 2026-10-02: Deep layer coverage rule (a tile set in every section, no visible set-piece edge on `deep` until the debrief), after a gap at the end of Level 01's outro; the content loader and the backdrop asset test check it.
- 2026-10-02: M3 part A: the renderer scales by integer factors with letterboxing by default and
  sharp-bilinear from the Video tab of [options](../ui/options/README.md) (`PixelScreen`: a GLES 2
  shader that samples as an integer pre-scale followed by bilinear filtering, filling the window
  with bars only where it is not 16:9); optional CRT scanlines.
- 2026-10-02: Production art plan drafted ([production](production/README.md)), for review.
- 2026-10-02: Production art (Level 01 batch, `tools/art/explosions.py`): every frame of the chosen round-09 fireball for tiny, small, medium and large (the large placeholder had 11 of its 14 frames and the frame numbers of the sheet masked out), premultiplied on black with one palette per rung. The game's ship death now plays 14 frames (the ship doc's 12 is the old count). Review files proposed for round 12.
- 2026-10-02: Concept round 12 closed (user decision): the explosion ladder up to large approved as **final**; this doc's `art` stays `chosen`, since the rest of its art (huge rung, other effects, palette sheets) is concept art.
- 2026-10-05: High-air enemies are drawn at 75 % opacity, not fully opaque (user decision in M4 part G,
  after the Brood Carrier hid the ship for about 10 s; as the Leviathan already was), coming to full
  opacity as they descend to the play plane.
- 2026-10-05: Round 25 closed (user): the 75 % high-air opacity kept as it is for the Brood
  Carrier (the ship under the hull reads at about 25 % contrast; the shared set-piece value, so
  the Leviathan keeps it too).
- 2026-10-05: M4 part H docs reconciliation: the runtime items are ticked for what Act 1 builds
  (the layer stack without `sub`, the bullet sprites in use, hit flashes and explosions) and the
  rest is tagged with its first user: `sub` and the towers' perspective in M5, the canyon walls in
  Act 3, the homing diamond in Act 4. Still open in M4: runtime drop shadows (only the Airstrike
  bombers and the hull bosses cast shadows, as sprites; the ship's shadow is part H's work) and
  the large orb for `medium` bullets.
- 2026-10-05: M4 part H, runtime drop shadows (`Shadows`): every flyer on `air` or `low-air` (the
  units, the Coilwyrm's segments, the ship) is drawn a second time as a dark silhouette from its
  frame's alpha (texels at least half opaque), at 50 % opacity and 85 % scale, offset (21, 30) for
  `air` and (9, 13) for `low-air`, after the ground units and before the low flyers and the
  low-air layer. Masking: while the ground layer's tiles, road and pieces (and the overhead pieces)
  are drawn they mark the stencil buffer wherever they cover a pixel, and the shadows draw only on
  marked pixels, each pixel once (overlapping shadows do not darken twice). So shadows fall on
  terrain, stations, wrecks and capital-ship hulls but not on open space, the far layer or the
  deep Earth (Levels 01–03 and 07 show them only on structures). The internal screen got a
  depth-stencil buffer for it. Cost per frame: one stencil clear, two shader switches and one
  extra draw per flyer (bench on llvmpipe unchanged in feel). Not drawn: the second shadow on bank
  tops at (12, 17), the 1–1.5 px blur, and shadows of the non-boss set pieces (the Leviathan) and
  the lifeboat tow; the bombers and hull bosses keep their sprite shadows. Bullets stay readable:
  they are drawn above every layer, and the shadow only darkens the ground under them.
- 2026-10-05: M4 part H: `medium` bullets are drawn as the large pulsing orb (round 09's large orb in
  the Vrell colours, 23×23 with a 13 px body, 6 frames at 10 fps; `tools/art/bullet_medium.py`),
  chosen by the damage of the `medium` class. The yellow needle stays unused: no Act 1 attack is in
  the fast speed class, and a bullet does not carry its speed class yet.
- 2026-10-05: Concept round 26 closed (user: the round accepted as proposed): the runtime drop
  shadows accepted as built (`Shadows`: dark at 50 %, 85 % scale, offset (21, 30) for air and
  (9, 13) for low-air, masked by the stencil to the shadow-catching ground layer), without the
  1–1.5 px blur and the second shadow on low-air bank tops; the `medium` bullet's large orb approved
  as final. This document's `art` stays `chosen` (it covers the later acts' art too).
- 2026-10-06: Open question on the perspective towers closed (user decision **D1 = a** of M5
  part B): parallax B's true-perspective towers are kept, as **scenery only**: the renderer
  projects each tower (a pre-rendered roof and textured wall quads) round the play field's
  (240, 297) each frame, and everything gameplay touches stays at street level or on low
  structures drawn without lean; walkers keep to streets, ramps and low roofs. Rejected: (b) full
  perspective with units on roofs (heights in the simulation, projected hit boxes, walls as climb
  paths: a large simulation, content and render task carried into Levels 09 and 14) and (c) no
  runtime perspective (orthographic tiles with a baked lean, dropping parallax B's turning walls).
  Our readings, for review in concept round 30: ground units, ground targets and bullets are drawn
  over the towers (readability before occlusion), and flyers' shadows fall on walls and roofs at
  the ground offset. The towers item now covers Level 08 (M5 part B); the canyon walls stay
  **later: Act 3**.
- 2026-10-06: Perspective towers built (M5 part B, renderer task): the roof is pre-rendered at its
  drawn size (footprint × *k*), so it is never resampled; the wall texture's rows are spread up
  the wall in true perspective (storeys a little taller towards the roof); the walls facing right
  and down are drawn at the tower's `shade` (default 0.5), those facing up and left as the
  texture; a tower is never mirrored (the symmetry rule: a mirrored roof is a piece of its own).
  The readings of the D1 entry are what the renderer does (ground units, targets and bullets over
  the towers; flyers' shadows on walls and roofs at the ground offset); the test capture
  `towers-capture-r30-a` (placeholder kit) goes to the user in concept round 30. The towers do not
  yet cast the short (6, 8) structure shadow onto the ground below them.
- 2026-10-07: [Concept round 30](../concept-rounds/round-30/README.md) closed (user, 2026-10-07):
  the perspective towers accepted as built (the test capture `towers-capture-r30-a` and Level 08's
  production towers, roofs leaning out from (240, 297) with the walls turning), so the towers item
  is ticked for Level 08; the canyon walls' perspective geometry stays Act 3.
- 2026-10-08: M5 part E (user decision E4 = c of 2026-10-08): the water rules are drawn as a hybrid,
  a generic run-time pass for everything on `sub` (tint, darker, blur, wave) and pre-rendered
  collars, ripples and surfacing steps per unit; rejected: a (all run time: the waterline mask the
  hardest part and the most GL risk) and b (all baked: atlas growth and separate under-water segment
  sets). Stated defaults: submerged units scroll with the sea (the 0.8–0.9 factor is for sea-floor
  scenery, which open ocean does not have); on a water level kills sink without a crater or wreck,
  landing blasts splash and flyers' shadows fall on the sea.
- 2026-10-08: The `sub` pass as built (M5 part E, step E3c; user decision E4 = c): everything on
  `sub` is drawn plain into a play-field-sized buffer between the deep layer and the ground layer
  (Level 11: between the swell and the chop), then laid over the field through one shader with the
  art scripts' stand-in look: colour × 0.55 × 0.85 + water tint (6, 40, 78) × 0.45, opacity 0.85,
  a 3×3 blur (about 0.7 px) and a row-wise sway of 0.7 px (0.5 rad a row, 5 rad/s). Without a frame
  buffer it falls back to tinting each sprite with the same sums (no blur, no sway). Kills under the
  water burst through the pass (`explosion-under-*`); kills and landing blasts on the surface burst
  in the `explosion-water-*` rung of their size, alpha-blended, with a ripple train after them. The
  pass ran on Mesa's GL under xvfb; the three-OS check is CI's smoke test and the round's capture.
- 2026-10-09: Hit flash on a large boss part (the Harbour Kraken's head and slam arms, round 33's
  capture): under sustained fire the 2-step flash restarted on every hit and turned the head into a
  white silhouette for seconds. Such a part now flashes at most once each 0.25 s (a hit within 15
  steps of the last flash's start starts none), and the head only halfway to white, so its eyes and
  beak stay readable. The other bosses keep the 1–2 frame flash on every hit. Over water, a shape
  that must read from under the surface (the Kraken's foreshadowing shadow, a torpedo's lit back
  and its bubbles) is drawn over the chop and under the convoy rather than through the `sub` pass,
  whose tint takes dark and slate shapes to the sea's own colour.
- 2026-10-09: Concept round 33 closed (user): the water and torpedo effects (`tools/art/water_fx.py`)
  and Level 11's props (`tools/art/l11_props.py`) approved as **final**, the weak spots as they are
  (the faint wake, the rectangular container collar, small reefs and fires, the pod on its root hard
  to see); the Kraken's lane telegraph and slam **a** "edge dashes + spray sheets" (already in the
  game) approved as final, its bright spray included; b "lane boil, chevrons, rollers" rejected and
  moved to `concept/rejected/` (`water_fx.py --variant b` now writes its review there). The quiet
  `sub` look (build choice a), the faint reef growths (x) and the invisible under-water stretch of
  the arms at 1:1 (y) kept as built.
- 2026-10-13 (user, round 36, MA.1 = b): the perspective rule amended for canyons the ship flies
  through: the floor at 1.0, the walls rising toward the camera to the plateau at about 1.25 like
  Level 08's towers, the collision line at the walls' foot, nothing gameplay touches on a face or
  the plateau. Variant a (the rim at 1.0 as the collision line, the faces dropping to the floor at
  0.6, as the chosen scene r03-c draws it) rejected: b reads as flying inside the canyon. Cliffs
  seen from above keep the old rule.
- 2026-10-20 (round 42, step GFX, pending the user's review): the 2026-10-09 hit-flash rule now holds for the
  Revenant Walker and the Siege Spire's crown, pods and maw too (at most one flash every 0.25 s, a 0.3 blend
  towards white, none on a part that is beam-hot). The Siege Spire's death flash is a local glow (190 px round
  the core, the flash-reduction setting applies); the Brood Carrier keeps its full-field flash.
