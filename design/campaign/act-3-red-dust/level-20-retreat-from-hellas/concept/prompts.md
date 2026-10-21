# Level 20 – concept and production notes

Round 43 (M7 part G, step ART-G1: production straight away, as Levels 15-19's; all of it is accepted in round 43 (2026-10-20). None of it is prompted: every file is rendered by a generator in `tools/art/`.

## backdrop-final-r43-a

Production art, M7 part G (round 43). Not a mockup: the sheet and GIF of Level 20's backdrop in
`assets/backdrop/level-20/`, rendered by [tools/art/backdrop_l20.py](../../../../../tools/art/backdrop_l20.py)
(`python3 tools/art/backdrop_l20.py`: every image, the proposed `backdrop:` block (written to
`briefs/l20-backdrop.yaml` for the merge into the level's data), the check and this review; `--review` only the
review, `--check` only the check, `--proposal` only the block). From the approved Hellas mood still
`mars-hellas-r36-a` ([Mars](../../../../world/mars/README.md), round 36): the pale ochre basin floor of salt-cracked
flats under drifted sand and a dune sea with its crests across the screen, the Vrell staging grounds' growth, a
dune ridge for the t≈42 nest, the basin wall's terraces (sinuous risers of layered rock lit as they face the basin,
sand on the treads), the wide upper terrace of wind-fluted bedrock with its wind-carved pillars, the rocky slope
below the rim with the rim road's switchbacks and abandoned militia crawlers, the rim's lip with the militia's twin
flak guns firing up (an animated piece) and the rust plain beyond. **One strip, keyed by strip row** (the level flies
back over ground it has shown: the zones and pieces sit at fixed rows, so the ground is the same both ways), lit
from the upper left and nothing mirrored, so it reads in either scroll direction. The sheet shows the play field at
twelve times without units or props, the whole strip at 1/40 and the pieces and seams; the GIF flies the whole
level at 0.5x, 10 fps (the ground moving up the screen from the turn at t≈22).

## l20-props-final-r43-a

Production art, M7 part G (round 43, step ART-G1), straight to production (no concept), designed to the README
(Layout, Secrets and pickups) and the Hellas mood still: rendered by
[tools/art/l20_props.py](../../../../../tools/art/l20_props.py) (`python3 tools/art/l20_props.py [ridge] [cache]
[--review]`; sprite names and frame layout in its docstring). The travelling ridge (the Dust Colossus's body under
the sand, shared with Level 21): a length, the head with its bow wave of heaving sand and the tail's furrow, in 16
headings, the crest's lime cracks as an additive glow the game pulses, a sand slide's puff; laid along a stand-in
path crossing the floor near the top edge at t=12-20. The Hellas Outpost emergency cache (the secret): a militia
shelter hatch in a concrete collar half under a sand drift with its yellow strobe (intact, hit, opened) and the
strobe's eight glow frames. The sheet also shows Okafor's briefing image `level-20-hellas-fallback`
([tools/art/briefing_images.py](../../../../../tools/art/briefing_images.py), batch r43): the CDF terrain plot of
the basin's north side with the strike route in and the fallback back up the terraces and the rim road. The GIF
shows the ridge's crossing over the backdrop, 10 fps.
