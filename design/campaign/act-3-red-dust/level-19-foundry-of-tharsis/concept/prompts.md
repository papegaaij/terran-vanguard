# Level 19 – concept and production notes

Round 42 (M7 part F, step ART-F2: production straight away, as Levels 15-18's; all of it is pending the user's
review in round 42). None of it is prompted: every file is rendered by a generator in `tools/art/`.

## backdrop-final-r42-a

Production art, M7 part F (round 42). Not a mockup: the sheet and GIF of Level 19's backdrop in
`assets/backdrop/level-19/`, rendered by [tools/art/backdrop_l19.py](../../../../../tools/art/backdrop_l19.py)
(`python3 tools/art/backdrop_l19.py`: every image, the proposed `backdrop:` block (written to
`briefs/m7f-backdrop.yaml` for the merge into the level's data), the check and this review; `--review` only the
review, `--check` only the check, `--proposal` only the block). From the approved Tharsis foundry mood still
`mars-tharsis-foundry-r36-a` ([Mars](../../../../world/mars/README.md), round 36): the slag fields of basalt
clinker with glowing cracks and slag heaps, the two roofless conveyor halls with their low walls and column
stubs, the furnace row's riveted iron floor with slag pools glowing at the edges and the round stacks, the yard
approach with its rail pairs and the yard wall (gate and belt slots), the casting yard (cast slabs, cooling
moulds, molten channels) as the arena, the south gate with the rust plateau beyond; the smelter tower drawn from
above at the left edge (sections 1 and 7: the opaque top-down ground has no horizon); the belts as one animated
piece (`belt-plates`, 4 frames at 10 fps: the plates move 40 px/s up the screen) with drum housings at the run
ends; stack and furnace smoke on low-air. The sheet shows the play field at twelve times without units or props
(the vent grates, gantries, safe, ingots and rovers are `tools/art/l19_props.py`'s, a later step); the GIF flies
the whole level at 0.5x, 10 fps.

## l19-props-final-r42-a

Production art, M7 part F (round 42, step ART-F4), straight to production (no concept), designed to the README
(Layout, Up-facing turret nests, Ground targets, Hazards, Secrets) in the backdrop's iron, concrete and furnace
glow: rendered by [tools/art/l19_props.py](../../../../../tools/art/l19_props.py) (`python3 tools/art/l19_props.py
[vent] [gantry] [safe] [ingots] [rover] [--review]`; sprite names and frame layout in its docstring). The furnace
vent grate in its riveted frame with a hazard-striped lip (shut, then the 1.2 s tell heating the bed red, orange,
white), the tell's additive heat glow with the shimmer's broken rings, the 1.0 s flame column from above (44 px
across, 8 frames) and the smoke puff after it; the steel truss gantry across the field and the up-facing nest's
cradle (a deck flaring up the screen with two amber chevrons: the "points back" cue; the Spine Turret sits on its
ring); the payroll safe seen three-quarter with its door facing up the screen toward the light (intact, hit with
the dial knocked off, opened with the scrip inside), its green door lamp (slow blink, a hurried flicker after a
hit) and the roofless shift office it stands in; three ingot stacks (one fresh from the moulds, still glowing) and
a pallet for the outbound belt; the crews' six-wheeled pressurised rover in 16 headings with its headlights and
beacon. Held in `build/art-held/sprites/` until the level's data claims the roots. The GIF plays them over
`tools/art/backdrop_l19.py`'s play field.

