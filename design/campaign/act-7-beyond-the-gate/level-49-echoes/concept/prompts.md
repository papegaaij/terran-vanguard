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
