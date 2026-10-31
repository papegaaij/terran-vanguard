---
title: Data file schemas — level backdrops
design: approved
implementation: in-progress
art: n/a
updated: 2026-10-31
---

# Data file schemas — level backdrops

## Summary

The schema of a level's `backdrop` block (layers, tile sets, seams, tower pieces), from which the level README's *Backdrop* table is rendered. Moved unchanged from the [architecture](../README.md#data-file-schemas)'s Data file schemas, which keep the shared conventions (keys, units, sizes) and the index of every schema.

## Design

- **Level backdrop** (`backdrop` in a level's data file; the *Backdrop* table is rendered from it):
  `scroll_factors` per layer (`deep`, `far`, `ground` = 1.0, `low-air`, `high-air`; `deep` may be
  left out on a top-down surface, Level 04's Luna: then the ground is the layer that covers the
  screen, its tile set in every section and its pieces' edges checked as the deep layer's are, and
  where a ground tile set is not opaque the section's far tile set must be); `ramp` (s an
  atmosphere change takes, centred on the section boundary); `haze_colour` (`rrggbb`);
  `atmosphere` per intensity the level uses (`clear`/`light`/`medium`/`heavy`: optional `banks`
  tile set on low-air, optional `wisps` tile set on high-air, `haze` 0..1 over deep and far);
  `tile_sets` by id (`layer`, `height`; 480 px wide, repeating along the layer; optional `drift`
  px/s sideways, wrapping); `pieces` by id (`layer`, `size`, an animation's `frames` and `fps` or
  an angle set's `headings`, `mid_size` if it counts towards the density rule); `placed` set
  pieces (`piece`, `t` when its centre passes the middle of the screen, `x` in the play field,
  optional `mirror`, a `path` of `[t, dx, dy]` waypoints for a piece with headings, `overhead`
  for a ground piece's part above the road (a bridge's arches, a gate's blockhouse: drawn over
  the convoy and the ground objects and under the ground units, so the crawlers pass under it and
  a turret placed on it stands on it); later pieces on a layer are drawn over earlier ones). M7 part G (Level 20,
  a level that flies back over ground it has shown): a **`strip`** (`origin`, the strip row at the screen's bottom
  edge at t = 0; `zones`, back to back from the bottom up, each `{from, to, tiles}`: the tile sets over strip rows
  `[from, to)`, at most one per layer and one on the base layer, together covering every row the screen shows over
  the level and its outro): the ground's tile sets keyed by strip row (= `origin` + the ground scroll, signed;
  a tile's pixel row is the strip row mod its height, from the image's bottom) instead of by section, whose
  `tiles` are then empty; a ground piece of a strip level may be placed by **`row`** (its centre's strip row)
  instead of `t` (no path, no repeat; `LevelData.pieceCentre` = row − origin). A layer position is the layer's scroll (the ground's
  scroll × its factor) plus the screen height; a section's tile set begins at the seam that enters
  at the top edge when the section starts. The images are `assets/backdrop/level-NN/<id>.png`
  (frames and headings `<id>_<n>.png`); `images: level-NN` takes them from another level's
  folder (Level 05 reuses Level 04's Luna until its own art). `content` checks that the ids and layers resolve and,
  sampled every simulation step, the art direction's density (at most 3 mid-size set pieces on
  screen) and motion budget (at most 2 strongly animated elements: animated or moving pieces,
  drifting tile sets, the atmosphere's banks counting as one; nothing moving faster than 120 px/s,
  about 2 px per frame, on its own).
  **Tower pieces** for the perspective towers of the
  [art direction](../../../art-direction/README.md#parallax-layer-model) (user decision D1 of M5
  part B: scenery only): a `ground` piece with a `tower` block is drawn in true perspective
  instead of flat. Its `size` is the footprint on the ground; its image `<id>.png` is the roof
  seen from above **at its drawn size**, the footprint × *k* rounded to whole pixels (62 × 64 at
  *h* 1.35: 80 × 83), so it is never resampled at run time. `tower` has `height` (*h* in camera
  units, 0 < *h* ≤ 1.5: the roof is drawn at scale *k* = 6 ÷ (6 − *h*) round the projection
  centre, the play field's (240, 297) counted from its top left, so 1.35 gives 1.29 and 1.5 gives
  1.33), `wall` (the id of a wall texture `assets/backdrop/level-NN/<wall>.png` in kebab-case,
  not a piece's or tile set's id; its columns run along a wall as seen from outside, left to
  right, and repeat every texture width along a long wall; its rows run from the foot, the
  image's bottom row, up to the roof's edge, window rows included, stretched over the wall in
  true perspective: a row at height *z* is drawn at 6 ÷ (6 − *z*), so the storeys grow a little
  towards the roof; several towers may share one) and optional `shade` (0..1, the brightness of
  the walls facing right and down, away from the key light, default 0.5; the walls facing up and
  left are drawn as the texture). Each frame the renderer (`TowerProjection`) draws a tower's
  visible walls (the sides that face the projection centre: the roof leans out past them) and
  then its roof at its projected place on whole pixels, the towers on screen ordered each frame
  outermost first (of two whose drawn boxes overlap, the one beyond the other as seen from the
  projection centre, along an axis whose footprint gap lies on one side of it; a topological pass,
  lowest first where nothing constrains the order; playtest 2026-10-18: by height alone an inner
  roof passed under the taller tower beyond it), after the ground layer's tiles and flat pieces and before the ground
  objects and units, so a tower never hides a unit, a ground target or a bullet; it marks the
  shadow stencil with the rest of the ground, so flyers' shadows fall on walls and roofs at the
  ground offset. A wall is drawn as textured quads, cells of at most 16 px along it and bands up
  it that widen by at most a tenth (about 1 000 quads with 30 towers on screen, 0.1–0.4 ms of
  CPU a frame, one or two draw calls). Placed as other pieces (`t`, `x` of the footprint's
  centre); no `mirror` (the roof and walls are lit from the upper left: a mirrored roof is a
  piece of its own, as for every lit piece), `path` or `overhead`. Nothing the simulation knows
  stands on a tower: ground targets, walker paths and crates stay on streets and low structures
  drawn flat. `content` checks a tower's `height`, `shade` and `wall` id, that it is a ground
  piece with one image, neither mirrored nor overhead, and that its roof's own speed, (*k* − 1) ×
  the scroll, stays inside the motion budget while it is on screen (a tower is on screen while
  its footprint or its roof is); a tower is not one of the budget's animated elements and counts
  towards the density rule only with `mid_size`. A placed piece may also carry a **`repeat`**
  (`count` placements in all, at least 2, each `every` s after the one before, its `path` shifted
  in time with it; a stream of traffic): the renderer and the checks see every copy, a copy's
  problem is reported as `placed[i] (repeat n)`, and the *Backdrop* table lists the stream once
  with "×count every s". Planned (M5 part C, the level clock of hold zones): a placed piece's
  `path` and a tile set's `drift` animate on the real clock, so moving scenery (Level 09's Kilo
  trucks) keeps its speed in a hold; a path's waypoints are timed from the moment its first
  waypoint's script time is reached, and the placements themselves stay in script time (the
  simulation's real clock is `Sortie.realSeconds()`, script time `levelSeconds()`, and between
  steps script time moves at `scriptRate()`).

M10 part A (step A2b; design/world/jovian, *Hazards & set pieces*: foreground cloud banks): a backdrop's optional
**`banks`** (`t`, `x`, `w`, `h` px and an optional `skip` list of difficulties): translucent banks drawn over the `air`
bodies on the `high-air` scenery, each centred on `x` and passing the middle of the screen at `t` as a placed piece
does, drifting down with the `high-air` scroll factor. `content` (`JovianHazardChecks`) samples the level every
0.05 s: at most the [Jovian](../../../world/jovian/README.md#hazards--set-pieces) `banks.cover` share of the play
field's area under banks at a time (their union; `easy_cover` on easy) and never a bank over the ship's band (the
bottom `banks.band` px) for more than `banks.band_seconds` s at a time. Presentation only: the simulation never reads it.
