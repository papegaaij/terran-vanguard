# Act 4 – Deep Water: concept prompts

Production art for the act's title card; the briefing images of the act intro are in
[design/ui/briefing/concept/prompts.md](../../../ui/briefing/concept/prompts.md#briefing-images-final-r51-a).

## act-4-still-final-r51-a

Production art, M8 part B (round 51; straight to production as Act 3's still, pending the user's review). Not a
mockup: the review sheet of `assets/ui/act-4-deep-water-still.png` (960×540, the still behind the Act 4 title card)
and of the title card as the game draws it over it (darkened to 55 %, letterbox, the chrome lettering
`assets/ui/act-4-deep-water-title.png` from [tools/art/act_titles.py](../../../../tools/art/act_titles.py), read from
the act's `title_card`), rendered by [tools/art/act_stills.py](../../../../tools/art/act_stills.py)
(`python3 tools/art/act_stills.py act-4`) from Level 22's production backdrop
([tools/art/backdrop_l22.py](../../../../tools/art/backdrop_l22.py)), as Act 3's was from Level 15's. Europa's ice
shelf from the entry corridor, the level's ground tile sets in perspective from high up: the shelf's double ridges
(`ice-shelf`) near, the Conamara Chaos's broken plates (`rig-field`) beyond, running to a sharp curved horizon (no
air: no haze, a black sky with a few stars); Jupiter huge over the horizon on the right, banded (belts, zones, the
Great Red Spot), lit from the upper left with its night side to the right; a thin steam thread rising from the first
rig far out on the plates, lit on its sunward side. One 96-colour palette with 4×4 ordered dither.

## act-4-still-r52-a, act-4-still-r52-b

Round 52 (F.6 redo; the user: "Maybe we can add a geyser to the title card somewhere. Europa is known for its giant
geysers shooting up up to 200km."). Rendered by [tools/art/act_stills.py](../../../../tools/art/act_stills.py)
(`python3 tools/art/act_stills.py act-4-r52`; writes only these proposals and `act-4-still-r52-sheet.png`, the still in
`assets/ui/` stays r51 until the pick: then set `A4_GEYSER` to the chosen variant and run `act-4`). The accepted r51
still with a giant water-vapour plume: ice grains thrown on ballistic arcs from a vent in nine jets (their own angles
and heights), so the plume is thin and bright at the vent, a tall column fanning out, a rounded crown and a faint veil
falling back; lit by the sun from the upper left (its own grains shadow the far side, a trace of Jupiter's warm light
there), sparse ice crystals glittering on the sunlit side. **a**: one huge plume rising from behind the horizon left of
Jupiter, its crown in the clear sky left of ACT IV. **b**: a nearer plume from a dark fissure on the ice below the
lettering's left end, its column towering past the D of DEEP, with a curtain of three smaller plumes along the fissure
receding to the horizon on the left. AI prompt: *late-90s pre-rendered CGI, Europa's cracked ice plain from high up
to a sharp curved horizon, black sky, Jupiter huge on the right; a giant water-vapour plume ~200 km high erupting from
a fissure, thin at the base, a faint tall column fanning out high up, sunlit on its left side, ice crystals
glittering, falling back as a faint veil.*
