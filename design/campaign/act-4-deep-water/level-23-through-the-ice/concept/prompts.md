# Level 23 – concept and production notes

Round 52 (M8 part C, step ART-C: production straight away, as the Act 2–4 levels; accepted in round 52 (2026-10-22)).
None of it is prompted: every file is rendered by a generator in `tools/art/`.

## backdrop-final-r52-a

Not a mockup: the review sheet and GIF of `assets/backdrop/level-23/`, rendered by
[tools/art/backdrop_l23.py](../../../../../tools/art/backdrop_l23.py) from the round-48 mood stills
`europa-descent-r48-a` and `europa-thera-deep-r48-a` (tools/concept/europa_r48.py) and the chosen round-07 kelp floor
(tools/concept/scenes_r07.py, without its fixed farm plots and domes). Ground tile sets (480x960, wrapping):
`shaft-dry` (the dry shaft from above: the ice floor receding to a dark trough, the drill's spiral grooves converging
as chevrons, a faint warm glow far below), `shaft-wet` (the same under water, teal, the glow stronger), `thera-farms`
(r07's meadows, reefs and anemones), `plain-edge` (the dark silt plain, sparse kelp); `ice-ceiling` on high-air (the
ice ceiling's cracks of light, additive). Pieces at exact rows: `shelf-lip` (the shelf's ice, the mouth's ragged lip
with Vrell collars, the shaft beyond), `waterline` (the band at ground row 2,646, where the ship's start height is at
t = 25.5 s: meniscus, foam fringe, the surface's light) and `waterline-foam` (low-air, translucent), ice ledges with
Vrell collars under every wall nest (dry and wet, left and right; `probe-ledge` with the probe's torn cable), the
seams into the farms and the plain, five kelp racks under the siphon pumps (float-line frames, kelp rows, grow-lamp
bars, the pump's socket with teal roots; rack 3 long enough for the t~108 nest), Thera Deep's dome clusters (the r06
dome model) and the plain's last lamp pylons, each with its lamp pool baked over the floor under it. The ice-shaft
wall textures for the game's WallProjection: `ice-wall`, `ice-wall-wet`, `ice-wall-glow` (CanyonProfile's 128x1280
layout: drilled glacial ice, the drill's helix grooves, four bone-and-teal collar strips a period),
`ice-wall-foot`, `ice-wall-foot-wet`. The sheet: ten screens at half size (the walls approximated, a stand-in grade
above the waterline row; stand-ins: nests, pumps, the hydro-kit ship), the tile sets and walls, three crops at 1x;
the GIF: t 20–30 at half size, the waterline crossing ("dry behind, water ahead"). `backdrop-proposal.yaml` is the
generator's `backdrop:` block for the level's data (`--proposal`).

## l23-props-final-r52-a

Round 52, production art (M8 part C batch, step ART-C; straight to production, designed to the README's *Ground
targets* and *Secrets and pickups* and the naval README's *Returning units under water*). Not a mockup: the review sheet
and a loop of the files written by [tools/art/l23_props.py](../../../../../tools/art/l23_props.py): Conamara's survey
probe snagged on the wet ledge (a yellow instrument torpedo hooked by its tether; snagged, hit, freed and empty; the
amber marker strobe; rule 7's light rim), the Thera Deep tender (an orange and white pressure-hull supply drone with
two pump-jets, the armour patch in its cradle; warm headlights and an amber beacon), the Reef Spitter's floor base (rock
gripped by its violet-brown root plate, no raft) and the Driftjelly's under-water body (no foam collar, the bell's rim
translucent, a cool rim light); the GIF on the farm floor: the probe shot free and its crate, the tender swimming up
with the patch and back, a spitter turning on its base, two jellies pulsing. The probe and the tender are held until
the level's data names them; the floor base and the jelly frames are claimed by their units.

**Accepted as final** in round 52 (F.1 the backdrop, F.5 the props, 2026-10-22).
