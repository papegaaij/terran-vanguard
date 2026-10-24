# Level 25 – concept and production notes

Round 54 (M8 part E, step ART-E: production straight away, as the Act 2–4 levels; accepted in round 54 (2026-10-23)).
None of it is prompted: every file is rendered by a generator in `tools/art/`.

## backdrop-final-r54-a

Not a mockup: the review sheet and GIF of `assets/backdrop/level-25/`, rendered by
[tools/art/backdrop_l25.py](../../../../../tools/art/backdrop_l25.py) from the round-48 mood still
`europa-siren-trench-r48-a` (tools/concept/europa_r48.py `trench_still`: ice-rock strata walls with purple Vrell growth,
the foot the collision line, dark silt between, ~14 % blue ambient). The walls and the rock pillar are the level's
`terrain`, projected by the game (WallProjection's ice path, Level 23's names): `ice-wall` (fractured ice-rock blocks
with dark cracks, silted ledges, a frosted lip, blotchy Vrell growth climbing from the foot with bone-rimmed nodules),
`ice-wall-glow` (the nodules' teal glow, additive, so the walls read in the dark), `ice-wall-foot` (the collision line:
a dark crack, the lit rock edge, rubble and growth tendrils), `pillar-face` and `pillar-top_0..1` (the fork's pillar,
backdrop_l16.py's butte model). Ground tile sets (480x960, wrapping, unlit): `trench-floor` (dark silt, rubble fans,
boulders, current lines), `trench-scour` (the fork and the narrows: scoured, rippled, more boulders), `trench-song` (the
Song: Vrell root mats over the silt), `abyss-floor` (the open floor, almost bare). Pieces at exact rows: `trench-lip`
(Level 24's plain and its ragged lip at the start, the slope into the trench), three section seams, `rock-ledge-left/
right` (under each Reef Spitter, at the walls' and the pillar's foot), `relay-beacon` (Level 24's beacon at a 4 s
pulse, its foot 26 px off a wall's foot line, with `relay-beacon-glow`); `cave-mouth-left/right` (112x124, not placed:
the game draws one over the wall at each terrain notch: the recess darkening into the overhang, lit or shaded side
walls, growth round the mouth). The sheet: ten screens at half size under the review's stand-in grade (ambient 0.14;
the walls and the pillar projected approximately as the game does; stand-ins: Reef Spitters, the ship), the tile sets
and the wall texture ungraded, three crops at 1x; the GIF: t 96–104 at half size, the fork round the pillar.
`backdrop-proposal.yaml` holds the generator's `backdrop:` block for the level's data and the `terrain:` block (walls,
the pillar island, 25 notches) the art is drawn for (`--proposal`).

## backdrop-final-r55-a

Round 54's F.1/F.2 redo (step LX), the same generator and sheet as `backdrop-final-r54-a` (now `ROUND = "r55"`):
the rock pillar's two ends rounded (25 x 40 px half-ellipses in the level's `terrain.islands`, so `pillar-top_0..1`
are rebuilt) and a faint cool rim along the walls' frosted lip in `ice-wall-glow` (additive, ungraded: 22 % of
(176, 204, 224) over the lip's outer half); on the sheet the glow is laid on the walls' face only. The headlights that
light the world and the rim light along the rock edges are the game's (`UnderwaterGrade`, the level's
`underwater_look`); the sheet's stand-in grade shows the ambient only, the `*-capture-r55-a.png` game captures show
the light (their frames from a game recording on Xvfb, the field at 1:1, the level time on each frame).

## l25-props-final-r54-a

The level's props, rendered by [tools/art/l25_props.py](../../../../../tools/art/l25_props.py) (the SDF models of
`tools/concept/render/`): Conamara's survey sub (`survey-sub_0..2`, 56x40: nose-down in the left cave's mouth in
Conamara's yellow with a white band, the cargo pod on its back shut, hit and scorched, opened with the lid blown off and
the hold empty; rule 7's light rim) with its glow (`survey-sub-glow_0..7`: its lamp dead, only a faint red emergency
pinger until the first hit, sparks at the dent after it), and the moored survey buoy (`survey-buoy_0..2`, 40x40, the
armour patch's carrier: a yellow float with a white band, a short mast, a radar reflector, its side hatch opening) with
its strobe (`survey-buoy-glow_0..3`). The GIF: in the trench under the review grade, the sub in its cave shot open and
its crate rising, the buoy blinking at the narrows' end and releasing the patch.
