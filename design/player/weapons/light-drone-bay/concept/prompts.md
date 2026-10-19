# Light Drone Bay — prompts

## light-drone-final-r37-a

Production art (M7 part A batch, step ART-A, for concept round 37), not a mockup: `python3 tools/art/light_drone.py` writes the sprites into `assets/` and this review sheet and
GIF (`--review` rebuilds only the review files). The look is the one round 36 chose (G.2 = a,
[the round-36 mockup](../../concept/light-drone-r36-a.png)): the disc drone (`gear_r36.drone_a`) at 16
headings, its shot (round 08's energy bolt at 3×8) at 32 headings, an additive break (white flash,
spark ring, glowing chips) and rebuild (a cyan ring closing on the dock), and the dock-ring bay
(`gear_r36.bay_model("a")`) cut from renders of the Stormhawk with the bay on both wings, as
`tools/art/stormhawk.py` cuts the other pods.

Artist brief: keep the chosen round-08/09 families exactly (player light blue / white / cyan,
soft-edged; nothing orange: that is the enemies' bullets); pre-rendered models lit from the top left,
every heading its own render, never rotated at runtime; one palette per frame set; additive effects
stored premultiplied on black.
- **Prompt:** top-down sprites for a late-1990s pre-rendered CGI vertical shoot'em up: a tiny round
  light-grey disc drone with a blue rim band, a dark dome with a cyan lens and a short barrel, seen
  from above in 16 headings; a small dock-ring pod on each wing of a blue-and-white fighter; a small
  blue-white break burst and a cyan ring closing in as it is rebuilt; Raptor / Tyrian 2000 era look.
- **Negative prompt:** orange flames, red tint, photographic realism, side view, watermark.
