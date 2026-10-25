# Level 27 – concept and production notes

Round 57 (M8 part G, step ART-G: production straight away, as the Act 2–4 levels). None of it is prompted: every file
is rendered by a generator in `tools/art/`. The light on the sheets and GIFs is the review's stand-in for the game's
Level 27 grade and light map ([tools/art/l27kit.py](../../../../../tools/art/l27kit.py): per section the proposed
`underwater_look`'s ambient times its tint, dim blue 0.45 with the headlights in sections 1 and 4, vent-lit 0.6 in
sections 2–3, the gate's teal 0.6 in section 5; the smokers' amber pools and each plume's pool by its phase; the glows,
bullets and pickups added after it), not the game's own render. The mood is round 48's still
`europa-vents-r48-a` (tools/concept/europa_r48.py `vents_still`).

## backdrop-final-r57-a

Not a mockup: the review sheet and GIF of `assets/backdrop/level-27/`, rendered by
[tools/art/backdrop_l27.py](../../../../../tools/art/backdrop_l27.py) from the round-48 mood still (its BASALT, PLAIN
and FLESH colours imported unchanged) and Level 24's structure (exact rows, `lumps`, the scenery relay beacon). Ground
tile sets (480x960, wrapping, unlit): `vent-edge` (the plain's last silt going to basalt, tube-worm clumps),
`smoker-field` (basalt silt, rusty sulphide staining, pale bacterial mats, worms), `chimney-garden` (rougher, more crust
and mats), `ridge-basalt` (blue-grey pillow basalt ribbed along the ridge), `bowl-floor` (resin-stained silt, chitin root
veins). Pieces at exact rows: the plain's last ridge with two of Levels 24–26's beacons on its lip (scenery, the 4 s
pulse), eighteen black smoker clusters (dark mineral chimneys with growth bands, hot mouths, amber mats and tube worms at
their feet; each with its additive mouth glow), the Spine Turret nest's flat-topped stack (t≈34), the smoker the
*Marlin* lies against with her scour (t≈40), the hull-code wreck (a grey Depth Hunter split on the floor, the paint
blistered off by a plume so the hull code `HDX-417` shows pale; mid-screen at t=70, x 175), the Polyp Mortars' mound and
the Sirens' stack (t≈80–81), the four beacons' knolls and footings and the ridge's two shoulders under the turret nests,
the section seams (the last one the bowl's rim, the plain falling away), and the hatchery gate: a ring of ten grown
chitin arches over a dark resin pit with its teal light. No piece within 48 px of a plume disc's centre.
`backdrop-proposal.yaml` holds the generator's `backdrop:` block and, for step G2, the plume rows (16 rows, 2–3 discs,
≥ 164 px apart centre to centre), the ground targets' places, the per-section `underwater_look` and the static
`lights:` (the smokers' amber pools, the old beacons, the gate's teal).

## l27-props-final-r57-a

Not a mockup: the review sheet and GIF of the files written by
[tools/art/l27_props.py](../../../../../tools/art/l27_props.py) (held in `build/art-held/sprites/` until Level 27's data
names `sprite: marlin-hauler` and its vents `look: heat-plume`): the Thera Deep pressure hauler *Marlin* (64x64,
nose-down toward her smoker, the dull orange hull and white band of Thera Deep's craft, the hold sealed under Vrell resin
with teal veins; sealed, hit, opened) with her glow (the stern's tired amber lamp, then the resin's teal through the
cracks and sparks), and the heat plume: the vent's mouth (cold, three tell steps), the tell's light and its lifting
silt, the eruption seen from above (rings of shimmering water rising out of the 64 px disc, silt streaming outward, 16
frames over 1.6 s) with its additive heat, the silt settling and the bend's short water ripple. The GIF: the first
plume row's tell and eruption with shots bent 25 degrees off the burning disc, then the *Marlin* by her stack shot open,
her crate rising.
