# Level 21 – concept and production notes

Round 44 (M7 part H, step ART-H1: production straight away, as Levels 15-20's; all of it is pending the user's
review in round 44). None of it is prompted: every file is rendered by a generator in `tools/art/`.

## backdrop-final-r44-a

Production art, M7 part H (round 44). Not a mockup: the sheet and GIF of Level 21's backdrop in
`assets/backdrop/level-21/`, rendered by [tools/art/backdrop_l21.py](../../../../../tools/art/backdrop_l21.py)
(`python3 tools/art/backdrop_l21.py`: every image, the proposed `backdrop:` block (written to
`briefs/l21-backdrop.yaml` for the merge into the level's data), the check and this review; `--review` only the
review, `--check` only the check, `--proposal` only the block). From the approved Hellas mood still
`mars-hellas-r36-a` ([Mars](../../../../world/mars/README.md), round 36) and in Level 20's style (the same basin,
flown the other way: into it): the level's start on the rim, the rust plain behind the CDF line with two scorched
pads, the line's sandbag berm and trench with four gun pits, the crest; the basin wall's upper slope (gullies,
strata, the retreat's shell craters and scorch) with burnt-out crawlers and toppled militia walkers; the inner
wall's terraces (risers of layered rock lit as they face the basin, red sand from the slides on the treads) with
landslide fans of red sand and the CDF hauler's wreck (the t≈36 turret nest's, `tools/art/l21_props.py`); the pale
dune sea with old breach craters filling with sand and a dune crest for the t≈57 mortars; the lime hollow's crust
of polygons cracked in rings round the centre ahead, its veins and ring cracks lit lime from below, sinkholes with
lime-lit throats and Vrell growth; the arena's basin floor, a calm crust under drifted sand with glowing veins,
**repeating every 512 px** (round 43, H.6) so the crawl never runs out of ground; section 6's settled floor (the
same floor under fallen dust, the veins dark) from the next 512 px seam with its seam piece, the collapsed crater
and the ridge's sunken furrow (16 headings) laid at the kill, and Vrell shapes lifting off on their spore trails
on high-air. Dust on low-air: streaks off the rim (light), ochre banks (medium), denser dust lit lime from below
(heavy, the hollow and the arena), clearing after the kill. Lit from the upper left, nothing mirrored. The sheet
shows the play field at twenty times without units (a kill at t=235: the clock jumps to 245, the ground runs on;
the crater, furrow and seam placement are stand-ins for the game's) and the pieces; the GIF flies the approach, the
arena's first 25 s and 47 s after the kill at 0.5x, 10 fps.

## l21-props-final-r44-a

Production art, M7 part H (round 44, step ART-H1), straight to production (no concept), designed to the README
(Layout, Ground targets, Secrets and pickups) and the Hellas mood still: rendered by
[tools/art/l21_props.py](../../../../../tools/art/l21_props.py) (`python3 tools/art/l21_props.py [crawler]
[hauler] [--review]`; names and frame layout in its docstring). The Hellas survey crawler (the secret, a 20 HP
trigger): a colonial deep-survey crawler, off-white with the amber band, parked across a terrace's tread under a
landslide of red sand: buried to its cab, hit (the slide blown lower and scorched), cleared (the sand pushed aside,
the hold open, the supply crate on it), with rule 7's light rim; held in `build/art-held/sprites/` until the
level's data names it. The CDF hauler's wreck (a backdrop piece): a heavy hauler slewed across the slope, its
container torn open, crates spilled, burnt, with the turret nest's three deck spots on the container's roof. The
ridge pass (t≈64-72) over the dune sea, drawn from Level 20's `colossus-ridge` sprites
([tools/art/l20_props.py](../../../../../tools/art/l20_props.py), reused unchanged) along a stand-in path (the
game's is the level's `ridge` key, round 43 Q7f). The GIF shows the ridge pass and the crawler's three states on
its ledge, 10 fps.
