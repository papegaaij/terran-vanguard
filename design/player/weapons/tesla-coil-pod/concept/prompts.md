# Tesla Coil Pod — prompts and briefs

Generator: `tools/art/act5_gear.py` (`arc`, `pod`; production art of M9 part A, round 61). The frames are 2D light
fields (the arc, the burst) and a lit model (the pod, cut like `tools/art/stormhawk.py`'s pods).

## tesla-arc

Artist brief: a short-range electric arc from a small wing pod to the nearest enemy, drawn for 0.08 s, three jagged
shapes in turn so a stream of arcs flickers. A thin white core, a soft glow and a wider faint fringe, two or three
short forks off the main line; at the target a white flash and a burst of eight short jagged sparks reaching about
32 px, fading in 0.2 s. Proposal a: blue-white glow, violet fringe (the player's cool colours). Proposal b: the same
shapes white-gold (the colour of the hex shield it breaks).

AI prompt: "pre-rendered 1990s CGI game sprite, a jagged blue-white electric arc with a violet fringe, thin white
core, small forks, on black, additive glow, pixel art, top-down shoot'em up"

## tesla-coil-pod

Artist brief: the wing pod on the AF-12 Stormhawk: a short dark pod with two copper coil rings, a white ceramic
insulator neck and a glowing terminal sphere at its nose (the arc's colour); at 48 px the pod is about 8 by 12 px, so
the copper rings and the white tip carry the read.

AI prompt: "tiny top-down sprite of a tesla coil weapon pod, dark gunmetal body, two copper coil rings, white ceramic
insulator, glowing blue-white sphere at the tip, pre-rendered 3D, 1990s CGI"

## tesla-coil-pod-capture-r61-a

Concept round 61, a capture of the game, not generated art (M9 part A, step A4, 2026-10-28): the game built by `:desktop:installDist` (branch `m9-act-5`) on a private 960×540 Xvfb display, `--bench <s> --settings <scratch>/settings.properties --invulnerable` (auto-fire on), `ALSOFT_DRIVERS=wave` writing the game's own mix to a file, the screen recorded with ffmpeg `x11grab` at 30 fps, a small XTest driver steering the ship; the `.mp4` is a cut with the game's sound (H.264 main, AAC 96 kbps), the `.png` frames of it (the 480×540 play field, or crops). No Act 5 level exists yet. `--level 8 --loadout left=tesla-coil-pod:3,right=tesla-coil-pod:3,rear=swarm-tail:2`, the real
Level 08 data; the ship moved up and swept left and right through the waves. The `.mp4` is recording seconds 5–45
(level ≈ recording − 1.5 s); the `.png` four frames with the most arc light (level ≈ 14.7, 30.8, 35.7, 37.7 s).
