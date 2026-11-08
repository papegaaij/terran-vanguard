# Level 49 – Echoes: concept prompts

## backdrop-final-r94-a

**The Choir's memory, production** (round 94, M11 part G, step ART-G, 2026-11-07; production art, pending the user's
review). Files: `backdrop-final-r94-a.png` (sheet: ten screens with tinted stand-ins and untinted bullets, the tile
sets over the tissue, the pieces and the mask) and `.gif` (Luna melting into the tissue, sides first, then Mars
forming from the top edge down, t=28.5-34.5); the `backdrop:` block and a section proposal in
`backdrop-proposal.yaml`. Generator: `nice -n 19 python3 tools/art/act7_memory.py backdrop` (held in
`build/art-held/backdrop/level-49/` until the level's data has a `backdrop` block, then `assets/backdrop/level-49/`).

- **Prompt:** late-1990s pre-rendered CGI game backdrop, straight top-down, the earlier acts' worlds as a hive mind
  remembers them: each in its own colours drained about 40 % toward a cold violet-white of its own brightness and a
  step darker than the units flying over it, dissolving at its sides into the hive's dark tissue (near-black violet,
  #1a0c22, faint curving veins) along a ragged edge with a thin violet rim (#966ec8). Luna: its grey limb across the
  lower half, Earth's crescent above it, fragments of cratered regolith and the L1 picket's wreck lights drifting
  past. Mars: the rust disc with Valles Marineris's scar, the polar cap and the dust limb, Phobos; Valles's terraced
  walls far below along both sides and thin dust veils. Ceres: the belt's dark rock mass and tumbling rocks, Ceres far
  off with its station lights. Gate 1 the L1 point (Earth's crescent, the Moon, the picket's wreck lights in a broken
  ring); gate 2 the remembered Hellas basin floor (rust sand in bent ripples, old breach bowls, stones) under the
  Colossus; gate 3 Ceres's curve and the station's lights; the unmaking: tissue and the core's red-violet glow far
  ahead. Each memory forms from the top edge down over 2 s and melts sides first: smooth, no flash.
- **No straight lines:** every limb, wall, ripple and edge curved or ragged (a row/column scan finds no run of 60 px
  in a tile or 120 px in a piece).
- **Negative prompt:** photograph, painterly brush strokes, perspective camera, side view, ruler-straight lines,
  saturated colours, a flash, ordered dither, clipped glow, text, watermark.

## echo-tint-final-r94-a

**The spectral tint's reference sheet** (round 94, step ART-G; the constants step G4's `EchoTint` shader builds on,
round 84 A.8 a). File: `echo-tint-final-r94-a.png`. Generator: `python3 tools/art/act7_memory.py backdrop --review`.

- **Brief:** every remembered unit (Act 1's Skitter, Needler, Stinger, Spore Bomber; Act 3's Dust Kite, Ghost Drone,
  Mote Swarm, Choir Herald; Act 5's Dart, Talon, Gilded Gunship, Hornet) original and tinted at 1:1 over its tinted
  world, untinted bullets beside them, the luminance of the tinted unit, of the world under it and of the world's
  brightest tenth; the three echoes (the Brood Carrier broadside, the Dust Colossus's mouth, the Iron Sovereign's
  base) at half size over their gates; the shimmer at four phases. The constants: rgb' = spectral(rgb, 0.4) × 0.74 +
  violet-white (226, 206, 255) × 0.26, spectral(c, k) = c (1 − k) + lum(c) × violet-white / 255 × 1.08 × k; alpha'
  = alpha × smoothstep((blur(alpha, edge / 2.5) − 0.3) / 0.55), edge = clamp(8 % of the sprite's smaller side,
  1.5, 10) px; the shimmer rgb' × (1 + 0.06 sin(2π × 0.35 t)); bullets, beams and their glows never tinted.

## supply-pod-final-r94-a

**The remembered supply pod** (round 94, step ART-G; pending the user's review). Files: `supply-pod-final-r94-a.png`
(sheet: the 16 tumble frames in its own colours and in the tint, the break, the pod in place at 1:1) and `.gif` (the
pod tumbling in the tint over Luna remembered, then broken). Generator: `nice -n 19 python3 tools/art/act7_memory.py
pod` (`debris-supply-pod_0..15` 36×36, `debris-supply-pod-break_0..7` 64×64, held in `build/art-held/sprites/` until
the level's `debris` names the `supply-pod` chunk).

- **Prompt:** late-1990s pre-rendered CGI sprite, top-down with a 30° tilt, a small Coalition supply container of
  Act 1's convoys: rust-brown ribbed sides, hazard-striped yellow and black ends, grey frame bands and lugs, tumbling
  slowly; its upper right dissolving into small violet-white motes. Drawn in its own colours: the game tints it like
  every remembered unit. Broken: its chips flung out, turning violet-white as they fade, a soft violet puff, no
  flash.
- **Negative prompt:** photograph, painterly, a fireball, a white flash, text, watermark.

## game-captures-r94-a

Captures of the game, not generated art (M11 parts G and H close-out, step GH5, 2026-11-08): the game built by `:desktop:installDist` (branch `m11-act-7`, parts G and H in the tree, the full `check` green), flown by the content autopilot's recording: `tools/concept/replay/record_replay.sh 49 medium l49.rec --loadout front=ion-beam:5,left=tesla-coil-pod:5,right=micro-missile-pod:5,rear=swarm-tail:5,generator=6,shield=6,armor=5 --special vrell-swarm-call --escort rook:autocannon:1` (the balance plan's Level 49 fit, Rook L1), then the start script with the same options plus `--level 49 --difficulty medium --replay l49.rec --bench <s>` on a private 960×540 Xvfb display, a scratch `--settings` (a 960×540 window, auto-fire), nothing played aloud (the audio environment stripped, OpenAL Soft's `wave` driver writing the mix to a file, checked in `/proc/<pid>/environ`); `ffmpeg -f x11grab -framerate 60`. The game's state hash at the recording's end equal to the recorder's (`86a66a70c7c005a4`, 17,189 steps, 286.5 s, won with 160 armour). Level t = 0 at the log's `[replay] the level's first step`; the mix aligned on its end. Clips: H.264 at 60 fps, AAC 160 kb/s at −3 dB; the play field (x 240–720) cut out unless the whole screen is named. Cut by step GH5's scratch scripts (not kept).
Files: `memory-wave-capture-r94-a.mp4` (t=7–17), `echo-forming-capture-r94-a.mp4` (t=87.5–102), `echo-refill-capture-r94-a.mp4`
(t=150–163, the whole screen), `echo-rush-r94-a.mp4` (t=87–274, the whole screen) and `tint-bullets-r94-a.png` (three
play-field frames at 1×, t=12, 51 and 77). The rush on the recorder's clock (video seconds of the rush file in brackets):
the first warning t=90 (3), the Carrier's echo forming 95–98 (8–11), its stage 2 at 124.3 (37.3), its death 151.2 (64.2),
the dissolve 153.2; the second warning 154.2, the Colossus's echo forming 159.1 and the refill 159.2 (72.2), stage 2 186.0
(99.0), its death 204.1 (117.1); the third warning 207.1, the Sovereign's echo 212.1–215.1 (125–128), its laser sweeps
from 217.6, stage 2 243.7 (156.7), its death 268.5 (181.5), the dissolve 270.5.

## memory-wave-capture-r94-a

A capture (step GH5, [how](#game-captures-r94-a)): a remembered wave over Luna.

## echo-forming-capture-r94-a

A capture (step GH5, [how](#game-captures-r94-a)): the first warning and the Carrier's echo forming.

## echo-refill-capture-r94-a

A capture (step GH5, [how](#game-captures-r94-a)): the refill and `ECHO 2 / 3`.

## tint-bullets-r94-a

Frames of the same run (step GH5, [how](#game-captures-r94-a)) at 1×.

## echo-rush-r94-a

The whole rush as one recording (step GH5, [how](#game-captures-r94-a)).
