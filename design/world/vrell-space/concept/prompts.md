# Vrell space – concept prompts

Round 84 (Act 7 design phase, step ART-R-B, 2026-11-05; pending the user's review). Generator:
`tools/concept/act7_r84.py moods echo transit`. Levels 43–47's stand-ins are ART-R-A's (`tools/concept/enemies_r84.py`, imported
unchanged); Levels 48–50 are drawn in act7_r84.py. Every haze and glow is a smooth float gradient (no ordered dither).

## vrell-moods-r84-a

Q10 a, a grade per level from the chosen round-06 scene: L43 the gate's dark stone and cold teal-white glyphs under a
violet and teal nebula; L44 the event plane, the nebula inverted (pale violet-white, dark violet streaks); L45 dark
chitin plates with glowing pink veins and warm pits under a pink-violet haze; L46 grey-brown dead hulks, one cold
white light from the top left, a starless patch; L47 pale bone ribs of the inner sphere lit warm gold-white; L48
teal-black Vrell growth over dull gold wreckage plates; L49 Luna as remembered, grey regolith in a cold violet-white
spectral tint, its sides dissolving into dark violet tissue with a pale rim; L50 near-black violet tissue ribs
converging on the Heart above, lit by its red-violet pulse.

## vrell-moods-r84-b

Q10 b: the same eight scenes through one Vrell palette (round 06's black-violet to violet to teal to pale mint),
each keeping only its own light.

## vrell-echo-r84-a

An echo (the Brood Carrier's production hull) in the spectral tint: its colours desaturated about 40 % toward a cold
violet-white and lifted a step brighter than its tinted Luna, the edges translucent and fading to 0 inside the
sprite; its bullets as built. Below: Level 49's memory melting into tissue between echoes and Mars forming.

## vrell-echo-r84-b

"Afterimage": as a, the hull 80 % opaque with a wider soft edge, two faint afterimages trailing its motion and a slow
violet scan band passing over it (a memory replaying; smooth, never a flicker).

## gate-transit-r84-a

Q11 a: Level 43's last seconds: the Tether Gate's ring ahead with the event plane's pale violet filling its mouth, the
ship and Rook in formation; a 1.5 s warp in which the whole image bends smoothly toward the ring's centre with a slight
twist while its colours grade in one step to the plane's violet (brightness rising one way only); the debrief over the
violet; Level 44 opening in the same violet as the warp unbends. A luminance plot proves no flash.

## gate-transit-r84-b

Q11 b, the sketch's: a 0.5 s full colour inversion, then the violet (shown with its luminance spike).

## spore-cloud-final-r88-a

**Spore cloud, stand-in** (round 88, M11 part A, step ART-A; the *Spore clouds* row of *Hazards & set pieces*; Level
45's final look is part C's). Files: `spore-cloud-final-r88-a.png` (sheet) and `.gif` (1×). Generator:
`python3 tools/art/act7_gear.py spore`.

- **Brief:** a soft cloud of glowing violet spores, 120–200 px across, that tints and never covers: a smooth veil of
  the palette's dark violet (#1e0c2c) at most 25 % over the backdrop and the play-plane units under it, a dim violet
  glow over it with pale violet specks (#e2ceff, 1–2 px, dim, never bullet-sized dots) drifting slowly round (a 4 s
  loop), its edge soft and wobbling. Burnt by an `area` hit or the Plasma Arc: an ember edge (#ff8c32 to #ffdc96)
  eating inward from the rim over 1 s, the cloud's glow left inside it. Smooth (≥ 32 steps, no dither), every glow
  fading to 0 inside its canvas.

## act7-rig-capture-r88-a

Concept round 88, captures of the game, not generated art (M11 part A, step A4, 2026-11-05): the game built by
`:desktop:installDist` (branch `m11-act-7`, part A in the tree) on a private 1920×1080 Xvfb display, `--bench 900
--settings <scratch>/settings.properties` (auto-fire on, sound off), `--invulnerable`, `--shots <tick>,…` writing the
960×540 screenshots (the play field at x 240–720), keys sent through XTest (the special, the menus, F12 for the screens
after the level). No Act 7 level exists yet. **The capture rig:** a scratch copy of the tree (never committed) in which
Level 42's data is a stand-in Act 7 level (Level 30's data as `VrellKeysTest`'s drift level: 150 s at 120 px/s,
`beyond_gate: true`, a `current` of ±20° with one reversal at t=40 told 2 s ahead, spore clouds at t=9 and t=108, the
secondary `escapes: gate-warden` for the leaving blink), its backdrop `images: level-44` with ART-B's real tiles (Level
43's `deep-2` copied in for the first 40 s, Level 44's `plane-2` and `ribbons` for the Current, `plane-4` and
`fast-streaks` for its brightest band; the two levels' fields meet at a hard seam, a rig artefact), stand-in Gate
Wardens and Rift Skaters with `VrellKeysTest`'s numbers drawn with ART-B's held frames (the Warden's closed frame
`gate-warden_0` only; the first build cycled all six iris frames, seen in the Resonator stills), production Needlers;
Act 6 made the campaign's last act (its outro's last two pages as the `ending`, no `next_act`) so that winning the rig
level runs the campaign end. A second build made the rig a boss rush (three stand-in Honour Guards in three held gates
over `plane-4`, `rush: {refill: true, checkpoint: every-difficulty, par: 150}`). The runs: the Pulse Cannon L5 with the
Swarm Call (the swarm, the end screens), the Plasma Arc L3 (the shell's break and regrowth, the spore burn, the leaving
tell, the teleport, the reversal), the Choir Resonator L1 (the whole level) and L5 (until the crash), and Level 36
itself with the Resonator L1 and the Swarm Call (until the crash). **The Resonator crashed the game** whenever one front
killed two units in one step (`WaveFire.meet` → `Pool.free`, an index out of range: L5 on Level 36 at 7 s and in the
rig at 17 s, L1 on Level 36 at 20 s): a sim bug, reported for the next code step. Stills and GIFs are laid out from the
screenshots by a scratch script (not kept: the rig is scratch).

## drift-capture-r88-a

Concept round 88, a capture of the game (M11 part A, step A4): a reversal and its chevrons, full screen. The [A4 capture rig](../../../world/vrell-space/concept/prompts.md#act7-rig-capture-r88-a).

## spore-capture-r88-a

Concept round 88, a capture of the game (M11 part A, step A4): a spore cloud's veil over the ship and the Plasma Arc burning one. The [A4 capture rig](../../../world/vrell-space/concept/prompts.md#act7-rig-capture-r88-a).

## spore-cloud-final-r90-a

**Spore cloud, production** (round 90, M11 part C, step ART-C, 2026-11-06; production art, pending the user's review;
replaces round 88's stand-in with the same sprite keys and sizes). Files: `spore-cloud-final-r90-a.png` (sheet) and
`.gif` (1× over Level 45's production reef). Generator: `python3 tools/art/act7_reef.py spore` (`tools/art/act7_gear.py
spore` is retired).

- **Brief:** as round 88's, with a cloud's body: eight soft billows round the centre rolling slowly with a fine
  texture through them (a 4 s loop), the palette's dark violet veil (#1e0c2c) at most 25 %, a dim violet glow
  (#7038b0) brightest in the dense cores, fine pale violet motes (#e2ceff, under 2 px, dim, never bullet-sized)
  drifting round. Burnt: a ragged ember edge (#ff8c32 to #ffdc96) eating inward over 1 s, ember sparks drifting off
  beyond it and fading, the cloud's glow left inside. Smooth (≥ 32 steps, no dither), fading to 0 inside its canvas.
