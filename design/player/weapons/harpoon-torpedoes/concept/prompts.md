# Harpoon Torpedoes — prompts

## harpoon-torpedoes-final-r50-a

Production art (M8 part A batch, step ART-A, for concept round 50), not a mockup: `python3 tools/art/act4_gear.py
harpoon` writes the frames into `assets/sprites/` and this review sheet and GIF (`--review` rebuilds only the review
files). The look is the one round 48 chose (G1 = a, "barbed harpoon",
[the round-48 mockup](../../concept/harpoon-torpedoes-r48-a.png), `tools/concept/gear_r48.py harpoon_model("a")`,
`h_trail`, `h_hit`): the 6×22 harpoon at 32 headings in a 24×24 cell, its teal motor glow, the cable-thin line of light
(a 3×4 tile the game stretches along the path, slack wave toward its end), the launch light, and the impact: a dark
cavity flash, the teal pressure ring to the 20 px blast radius, five barb sparks.

Artist brief: keep the round-48 pick's look exactly (pre-rendered models lit from the top left, every
heading or state its own render, never rotated at runtime; one palette per frame set; additive effects
stored premultiplied on black; the player's under-water colours toward cyan / teal-white, away from the
Vrell and Siren teal 00ff9a).
- **Prompt:** top-down sprite frames for a late-1990s pre-rendered CGI vertical shoot'em up: a slender dark gunmetal
  harpoon torpedo with a steel spear head, two swept barbs, cobalt bands and a teal motor ring, trailing a hair-thin line
  of pale teal-white light that slackens into a slow wave; its impact a dark cavity flash, a teal pressure ring and white
  barb sparks; Raptor / Tyrian 2000 era look.
- **Negative prompt:** fire balls, bubbles, orange or magenta light, photographic realism, side view, watermark.

## harpoon-torpedoes-capture-r50-a

Concept round 50 (M8 part A, step A7), a capture of the game, not generated art: the game built by `:desktop:installDist` (branch `m8-act-4`), run on a private Xvfb display (960×540 window) with `--bench <s> --settings <scratch>/settings.properties --invulnerable` (auto-fire on), `ALSOFT_DRIVERS=wave` writing the game's own mix to a file, the screen recorded with ffmpeg `x11grab`; a small XTest driver sweeps the ship left and right and taps the special at set seconds. The `.mp4` is a cut of the recording with the game's sound (H.264 main, AAC 96 kbps); the `.png` is a frame of it (or two side by side). `--level 18 --medium underwater@10 --loadout front=harpoon-torpedoes:3,rear=depth-charges:3,left=micro-missile-pod:2,right=micro-missile-pod:2 --special sonar-pulse:4`; the video is level seconds 5–28, the frame 14 s.
