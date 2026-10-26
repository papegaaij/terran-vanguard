# Level 28 – concept and production notes

Round 58 (M8 part H, step ART-H2: production straight away, as the Act 2–4 levels). None of it is prompted: every file
is rendered by a generator in `tools/art/`. The light on the sheets and GIFs is the review's stand-in for the game's
Level 28 grade and light map ([tools/art/l28kit.py](../../../../../tools/art/l28kit.py): per section the proposed
`underwater_look`'s ambient times its tint, the gate's teal, the galleries' cool light under the racks' teal and violet
glows, the cavern's amber, the antechamber's red-amber, the arena's lime-white, the shaft's cool light from above; the
static pools; the glows, bullets and pickups added after it; in air from the breakout, the grade lifting over 1 s), not
the game's own render. The mood is round 48's Europa stills (tools/concept/europa_r48.py) and the act's earlier
production backdrops. The Abyssal Maw and the cruisers are ART-H1's (not on these sheets).

## backdrop-final-r58-a

Not a mockup: the review sheet and GIF of `assets/backdrop/level-28/`, rendered by
[tools/art/backdrop_l28.py](../../../../../tools/art/backdrop_l28.py) from the act's production backdrops (Level 27's
sea-floor maker and the hatchery gate's arches, Level 23's drill shaft, Level 22's ice), imported unchanged. Ground
tile sets (480x960, wrapping): `gate-hall` (the hatchery's resin floor past the gate, chitin root veins),
`brood-gallery` (resin grooved along the galleries), `spawning-floor` (resin ridges round hatching pools: pale eggs in
amber fluid), `antechamber-floor` (dark basalt under resin, a network of red fissures), `shaft-climb` (Level 23's wet
shaft floor run in reverse, the vent's lime-amber glow far below), `night-ice` (Level 22's ice shelf at night under
Jupiter: blue shadows, a cream shine from the right); high-air `ice-ceiling` (Level 23's cracks of light) and
`ice-crystals` (Level 22's, dimmed). The shaft's walls are Level 23's ice-shaft textures, rows reversed (the climb).
Pieces at exact rows: the ring of chitin arches seen from inside at the start (Level 27's gate, its teal light), lamps
of grown flesh on stalks, the gallery's mouth (a ribbed chitin lintel with lamps), the egg racks (bone rails, uneven
ribs, pale pods glowing teal or violet, some hatched), three racks whose ends carry the t≈30 Spine Turrets' sockets,
the gallery walls' bulges where the t≈36 Sirens are grown, the fallen rack over *Pelagic-3*'s scour (t≈58, x 90),
the Spine Turrets' resin ridge (t≈66, five seats: medium's three in the middle), the mortars' far ridge (t≈74), the
cavern's last stack (t≈76, three Siren seats: hard's third the leftmost), four steaming fissures (red-amber glows), and
**the caldera** (one piece, 480x1508): from the outer rim with the t≈92 mortars' seats down the terraced slope to the
basin's crust, which fills the halted arena's whole screen, cracked in rings and radial fissures round the vent's throat
at screen (240, 330), a ~140 px funnel round a lime-white core (its light the `caldera-glow`); above it the far wall
darkens into the shaft's floor. Then the bore's broken lip on the breakout line (`shaft-mouth`: the shaft's dark water,
a ring of shattered ice blocks, foam, radial cracks in the night ice), the ceiling's fractures closing on the ship's row
at t=115 (high-air `ceiling-cracks`), two other Vrell drill holes (bone collars, teal strips, steam), a Vrell carrier
risen beside the wing (high-air, the brood carrier's hull under the night's light); the night ice stays intact where
the cruisers rise (step I2, round 58 F.3: the two open leads `ice-split-left/-right` showed water before the hulls came
up and are gone; the game draws each hull's hole, `ascendancy-cruiser-hole`, as it breaks through). The review approximates the
shaft's walls as flat bands (the game projects them). `backdrop-proposal.yaml` holds the generator's `backdrop:` block
and, for step H3, the ten sections with their speeds, eases and tiles, the per-section `underwater_look`, the shaft's
`terrain` (Level 23's 320 px, flattening to the bore's lip on the breakout line, pushing without damage), the fixed rows
(the halted arena's screen and the throat's row, the breakout line), the ground places, the buoy's drop and the static
`lights:`.

## l28-props-final-r58-a

Not a mockup: the review sheet and GIF of [tools/art/l28_props.py](../../../../../tools/art/l28_props.py)'s frames
(held in `build/art-held/sprites/` until Level 28's data names them; the breakout's ice until SpriteUse claims it, step
H5). *Pelagic-3* (`recon-sub`, 64x64): the UTC recon sub nose-down under two of the fallen rack's bones, the slate-grey
Coalition hull with its pale band and the sail's number plate, the dorsal hatch sealed under Vrell resin (violet-brown,
teal veins); sealed, hit (the resin cracked), opened (the resin burst off, the hatch dark: the crate rises); its glows:
the emergency beacon's faint red blink, then the resin's teal glints and sparks; rule 7's light rim. The service buoy
(`service-buoy`, 40x40): Level 25's buoy model in Thera Deep's dull orange with a white band, its strobe. The breakout's
ice: four slabs of the shattered ceiling, each tumbling over in 8 frames (the lit top, the edge, the dark wet underside;
`ice-slab`, 96x96), and the ceiling's burst (`ice-shatter`, 320x320 additive: a flash, a ring of light running out
along radial cracks, fading). The GIF stands on Level 28's backdrop: *Pelagic-3* shot open and her crate rising, the
buoy releasing its patch at t=88, the breakout at t=115 (the burst, the slabs flying out, the night ice).
