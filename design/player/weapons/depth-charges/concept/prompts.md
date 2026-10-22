# Depth Charges — prompts

## depth-charges-final-r50-a

Production art (M8 part A batch, step ART-A, for concept round 50), not a mockup: `python3 tools/art/act4_gear.py
charges` writes the frames into `assets/sprites/` and this review sheet and GIF (`--review` rebuilds only the review
files). The look is the one round 48 chose (G2 = a, "drum", [the round-48 mockup](../../concept/depth-charges-r48-a.png),
`tools/concept/gear_r48.py charge_model("a")`, `dc_fuse`, `dc_burst`): the navy drum with yellow bands at 16 spins over
180° in three sinking stages (smaller, darker), the white fuse lamp that blinks faster, and the pressure-dome burst per
level radius (40 / 46 / 52 / 58 / 64 px): a white-teal flash disc, the dark cavity collapsing, the bright rim ring and
the faint silt ring.

Artist brief: keep the round-48 pick's look exactly (pre-rendered models lit from the top left, every
heading or state its own render, never rotated at runtime; one palette per frame set; additive effects
stored premultiplied on black; the player's under-water colours toward cyan / teal-white, away from the
Vrell and Siren teal 00ff9a).
- **Prompt:** top-down sprite frames for a late-1990s pre-rendered CGI vertical shoot'em up: a small navy depth-charge
  drum with light end caps and two yellow bands, tumbling, a white fuse lamp on top; its burst under water a pressure
  dome: a white-teal flash disc, a dark cavity that collapses, a bright teal rim ring and a faint silt ring, no fire;
  Raptor / Tyrian 2000 era look.
- **Negative prompt:** fire balls, smoke, bubbles, orange light, photographic realism, side view, watermark.

## depth-charges-capture-r50-a

Concept round 50 (M8 part A, step A7), a capture of the game, not generated art: the game built by `:desktop:installDist` (branch `m8-act-4`), run on a private Xvfb display (960×540 window) with `--bench <s> --settings <scratch>/settings.properties --invulnerable` (auto-fire on), `ALSOFT_DRIVERS=wave` writing the game's own mix to a file, the screen recorded with ffmpeg `x11grab`; a small XTest driver sweeps the ship left and right and taps the special at set seconds. The `.mp4` is a cut of the recording with the game's sound (H.264 main, AAC 96 kbps); the `.png` is a frame of it (or two side by side). `--level 11 --loadout front=harpoon-torpedoes:3,rear=depth-charges:3,left=micro-missile-pod:2,right=micro-missile-pod:2 --special sonar-pulse:4`, the ship moved up above the convoy; the video is level seconds 22–34 (the first Driftjelly field), the frame 27.2 s.
