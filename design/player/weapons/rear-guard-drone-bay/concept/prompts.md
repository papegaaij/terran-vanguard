# Rear-guard Drone Bay — prompts

## rear-guard-drone-final-r43-a

Production art (M7 part G batch, step ART-G2, for concept round 43), not a mockup:
`python3 tools/art/light_drone.py --rear` writes the sprites into `assets/` and this review sheet
and GIF (`--rear --review` rebuilds only the review files). The drone is the
[light drone](../../light-drone-bay/concept/prompts.md)'s disc (round 36 G.2 = a,
`gear_r36.drone_a`'s hull, rim band and dome) with a twin gun on a raised housing and two blue fins
on the other side, at 16 headings (frame k has its gun k × 22.5° clockwise from up: the trail station
firing straight back draws frame 8). The bay is the light drone's dock-ring bay turned round: the
ring and its lit pad at the back of the pod (the drone launches backwards) and a blue nose cap, cut
from renders of the Stormhawk with the bay on both wings, as `tools/art/stormhawk.py` cuts the other
pods. The shot is the pulse bolt (drawn smaller by the game) and the break and rebuild are the light
drone's frames, so the review flies those; the GIF shows the pair trailing 40 px behind and 12 px to
each side with a 0.15 s lag, firing back at 6 a second, blocking bullets from behind, one breaking
and being rebuilt at its dock (the rebuild shortened for the review).

Artist brief: keep the chosen round-08/09 families exactly (player light blue / white / cyan,
soft-edged; nothing orange: that is the enemies' bullets); pre-rendered models lit from the top left,
every heading its own render, never rotated at runtime; one palette per frame set; read as the light
drone's sibling, told apart by the forked twin gun.
- **Prompt:** top-down sprites for a late-1990s pre-rendered CGI vertical shoot'em up: a tiny round
  light-grey disc drone with a blue rim band, a dark dome and a short forked twin gun on a raised
  block, seen from above in 16 headings; a small pod on each wing of a blue-and-white fighter with
  a dock ring and a glowing pad at its back and a blue nose cap; Raptor / Tyrian 2000 era look.
- **Negative prompt:** orange flames, red tint, photographic realism, side view, watermark.
