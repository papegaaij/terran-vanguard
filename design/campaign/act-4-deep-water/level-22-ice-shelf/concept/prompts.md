# Level 22 – concept and production notes

Round 51 (M8 part B, step ART-B: production straight away, as the Act 2 and Act 3 levels; accepted as final in round 51 (2026-10-22);
F.3: no deep layer, kept). None of it is prompted: every file is rendered by a generator in `tools/art/`.

## backdrop-final-r51-a

Not a mockup: the review sheet and GIF of `assets/backdrop/level-22/`, rendered by
[tools/art/backdrop_l22.py](../../../../../tools/art/backdrop_l22.py) from the round-48 mood still
`europa-ice-shelf-r48-a` (tools/concept/europa_r48.py: the ice ramp, the ridged height field, the open lead's black
water mirroring Jupiter's bands, Jupiter's warm shine from the right). Three opaque ground tile sets (480x960,
wrapping): `ice-shelf` (the white-blue plain, Europa's double ridges crossing it, fissures), `rig-field` (the Conamara
Chaos's broken plates over the refrozen matrix, Vrell teal stains), `conamara-run` (the plates under packed snow, the
ice-breaker road at x 240 with its berms, marker poles and a sodium lamp every 240 px); `ice-crystals` on high-air
(section 1). Pieces at exact rows: the two seams (along a pressure ridge), the pressure ridge under the t~20 nest, the
crevasse under the secret's lip, seven open leads cut along every Skimmer run of the data (2 x 58 px, the weave on
water: `--check`), Conamara Station (`conamara-station`, tools/art/l22_props.py's structures over the run-in's ice,
the dome's foot on the bottom edge at t=150). No deep layer (the shelf is opaque everywhere). The sheet: eight
screens at half size with stand-ins (the rigs, turrets, geysers, the lip, the ship), the tile sets, the high-air
crystals, two screens at 1x; the GIF: t 16-26 at half size (the ridge nest and the first leads).
`backdrop-proposal.yaml` is the generator's `backdrop:` block for the level's data (`--proposal`).

## l22-props-final-r51-a

Not a mockup: the review sheet and GIF of the level's props, rendered by
[tools/art/l22_props.py](../../../../../tools/art/l22_props.py): the ice geyser (`ice-geyser_0..3`: the crack in its
refrozen seam, dormant and the 1.2 s tell glowing lime-white; `-glow_0..2` additive; `-steam_0..7` the 0.8 s column
from above, 40 px, normal blending; `-residue_0..5` the mist after it), the secret's crevasse lip (`crevasse-lip_0..2`:
a snow bridge over the crevasse with the core-sample cache's orange case in its shadow, cracked, fallen; its marker
light `-glow_0..7`), the Conamara ice-crawler (`ice-crawler_0..5`, a red-cabbed tracked survey vehicle up and down
the screen with three track frames; `-lights_0..3` its headlights and amber beacon), and the station's structures
(the backdrop's `conamara-station` piece). The GIF: a geyser row's ripple, the lip shot open, the crawler driving
up and back on the shelf (stand-in plate: the `ice-shelf` tile).
