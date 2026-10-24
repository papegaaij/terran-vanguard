# Level 26 – concept and production notes

Round 55 (M8 part F, step ART-F: production straight away, as the Act 2–4 levels). None of it is prompted: every file
is rendered by a generator in `tools/art/`. The dark look on the sheets and GIFs is the review's stand-in for the game's
Level 26 light map ([tools/art/l26kit.py](../../../../../tools/art/l26kit.py): the 6 % ambient, the hydro-kit's 70
degree / 260 px headlights with a hard edge, Rook's 56 px lamp, a 15 % rim pool per unit, the static lamps' pools; the
glows, bullets and pickups' gold halos added after it), not the game's own render.

## backdrop-final-r55-a

Not a mockup: the review sheet and GIF of `assets/backdrop/level-26/`, rendered by
[tools/art/backdrop_l26.py](../../../../../tools/art/backdrop_l26.py) from round 48's darkness look a
(tools/concept/europa_r48.py `black_still`, the hard cone) and Level 25's structure (backdrop_l25.py's scroll helpers,
rock wall, foot band, cave mouth and the walls' projection, imported unchanged). Almost all black: the ground is unlit
albedo, the light map shows it. Ground tile sets (480x960, wrapping): `pipeline-floor` (section 1: dark silt, the
drowned Thera Deep pipeline with flanged sections and anchor blocks), `black-water` (sections 2–6: the far floor, near
black, the cone revealing a dim murk). The fault scarp is the terrain's single right wall (WallProjection's ice path):
`ice-wall` (backdrop_l25's fractured rock, its own seed, darker), `ice-wall-glow` (sparse teal growth nodules, no lip
rim: the game's rim light draws the edge), `ice-wall-foot`; `cave-mouth-right` (not placed: the game draws it at each of
the three notches). Pieces at exact rows: `seam-black-water` (the pipeline broken off), two of the trench's last
`relay-beacon`s falling behind at the start (Level 24's beacon and its `-glow`), six `lamp-post`s along the pipeline
(a leaning mast, an arm over the pipe, a sodium lamp; 4 frames of a slight flicker) with the last one a
`lamp-post-dying` (8 frames: the lamp failing), each with its `-glow`, two `amber-seep`s far below in section 5 with
their faint amber `-glow`, and `vent-field` (480x2040: far chimney silhouettes) with `vent-field-glow`, the vent
field's red glow rising at the top of the screen from t~168. The sheet: ten screens at half size in the dark look's
stand-in (the ambient ramping 20 -> 6 % over t=8–18), the tiles unlit, the scarp texture, a cave mouth, a lamp post and
its glow, three 1x crops; the GIF: t 10–18 at half size, the last lamps and the light going out.
`backdrop-proposal.yaml` holds the generator's `backdrop:` block for the level's data, the `terrain:` block (the
scarp, three notches) the art is drawn for and the static lights it assumes (`--proposal`).

## l26-props-final-r55-a

Not a mockup: the review sheet and GIF of the files written by
[tools/art/l26_props.py](../../../../../tools/art/l26_props.py) (held in `build/art-held/sprites/` until Level 26's data
names `sprite: farm-barge`): the secret, a drowned Thera Deep farm barge (80x48: a grey-green flat hull lying askew,
an amber gunwale, rows of empty grow trays, the cargo pod on its stern, a short mast with the one working sodium lamp,
silt drifted against its side; lying, hit with the pod's lid dented, opened with the lid blown off and the hold dark),
the light rim of readability rule 7, and its additive glow (the lamp burning with a slow sodium flicker, then
stuttering with sparks at the dent after the first hit). The GIF: at t 84–89 in the dark look's stand-in, the barge's
warm lamp seen from the dark's far side among Glow Anglers' teal lures, the ship's cone finding the hull, the barge
shot open and its crate rising with a pickup's gold halo. The armour patch's carrier is Level 25's survey buoy, reused.

## rook-loss-final-r55-a

Not a mockup: the review sheet and GIF of the files written by
[tools/art/rook_loss.py](../../../../../tools/art/rook_loss.py) (assets/sprites/, `rook-*` is always claimed): Rook's
hydro-kit Ember (tools/art/act4_gear.py `kit_model("c")`, imported unchanged) tumbling in 16 frames, scorched and
holed with the lamp dark (`rook-spin`), its failing lamp's light and the torn engine's red fire (`rook-spin-glow`,
additive), the torpedo trail as a 16x64 additive strip the game repeats along each trail (a thin white-hot core in a
churning orange wake, 8 frames), the torpedo's motor (`rook-torpedo`) and the hit's flash under water (`rook-flash`: a
white core, an orange fireball, a pale pressure ring). The GIF: section 6 in the dark look's stand-in, t 155.6–161.6:
Rook's lamp beside the ship, three trails rising from the bottom edge in their lanes (Rook's x and x +- 48, never the
ship's lane), converging on him, three flashes, the spin-down off the bottom edge in 2.5 s with the lamp stuttering
and dying, then the dark.
