# Level 24 – concept and production notes

Round 53 (M8 part D, step ART-D: production straight away, as the Act 2–4 levels; pending the user's review in round 53).
None of it is prompted: every file is rendered by a generator in `tools/art/`.

## backdrop-final-r53-a

Not a mockup: the review sheet and GIF of `assets/backdrop/level-24/`, rendered by
[tools/art/backdrop_l24.py](../../../../../tools/art/backdrop_l24.py) from the round-48 mood still
`europa-abyssal-plain-r48-a` (tools/concept/europa_r48.py `plain_still`: the plain's colours, Thera Deep's warm lamp
light) with Level 23's ice ceiling (tools/art/backdrop_l23.py, unchanged). Ground tile sets (480x960, wrapping, unlit:
the game's under-water grade darkens them section by section): `lamp-edge` (the silt plain at Thera Deep's edge, kelp
stands, pale stones), `plain-reefs` (darker silt, ridges, Vrell coral crust and root veins), `beacon-line` (smooth dark
silt, stones), `deep-silt` (long low dunes, empty); `ice-ceiling` on high-air. Pieces at exact rows: `thera-lamps` (two
of Thera Deep's last pylons at the bottom edge at the start, their pools warming the screen: warm behind),
`lamp-pylon-1..3` (the last grow-lamp pylons, t≈4, 12, 22, a lamp ring on a mast, the pools baked for section 1's
ambient 0.50), `floor-ridge` (a low rock ridge across the plain at t≈18, the Reef Spitter nest's perch), three section
seams, `coral-stack-1..4` (dead coral and rock stacks of the reef field: scenery, no iris, no teal, clear of the four
spawning reefs), `wreck-scar` (the scour, drag furrow, debris plates and snapped tow cable the *Meridian* left; the wreck
is its ground target's sprite), `beacon-footing-1..3` (concrete pads, cable trenches toward the next beacon fading into
the silt, the mast's shadow), `relay-beacon` (16 frames at 8 fps: the grey mast with hazard bands, the cross-arm with its
relay pods and the lamp's lens, red at rest and gold at the flash; tools/art/l24_props.py renders it) with
`relay-beacon-glow` (its additive light, the 2 s pulse: a gold flash fading to a dim red, the light on the floor round
the mast's foot; not placed: the game adds it over the grade), and `trench-mouth` (the plain's lip at the bottom edge at
the level's end, the trench falling into the black beyond, its walls faint; tall enough for the outro). The grade ladder
proposed for round 53 (the proposal's comments): ambient 0.50 / 0.32 / 0.18 / 0.14 by section, the caustics thinning to
none, the headlights rising. The sheet: ten screens at half size under the review's stand-in grade (stand-ins: the
reefs, nests, the wreck, the ship), the tile sets ungraded, three crops at 1x; the GIF: t 97–107 at half size, the first
beacon's pulse passing. `backdrop-proposal.yaml` is the generator's `backdrop:` block for the level's data (`--proposal`).

## l24-props-final-r53-a

The level's props, rendered by [tools/art/l24_props.py](../../../../../tools/art/l24_props.py) (the SDF models of
`tools/concept/render/`): the relay beacon's frames and glow (the backdrop's pieces, shown from `assets/backdrop/level-24/`),
a grow-lamp pylon piece, the wrecked Thera Deep survey sub *Meridian* (`meridian-wreck_0..2`, 80x48: lying silted on its
side in the city's orange with a white band and sail, hit and scorched, opened with its hatch blown off; rule 7's light
rim) with its glow (`meridian-wreck-glow_0..7`: the failing amber hull lamp until the first hit, sparks at the dent
after it), and the Scuttler's silt puff (`scuttler-silt_0..7`, 32x32, a stepped grey-green cloud spreading and settling,
no bubbles). The GIF: on the plain under section 3's review grade, a beacon pulsing on its footing, the wreck shot open
and its crate rising, a Scuttler striding with its puffs.
