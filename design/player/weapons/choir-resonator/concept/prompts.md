# Choir Resonator — concept prompts

## choir-resonator-r85-a

**Choir Resonator a — "the bell"** (round 85, step RES). Files: `choir-resonator-r85-a.png` (sheet) and `choir-resonator-r85-a.gif` (the 480×540 play field over Level 44's event plane, 20 fps).

- **Artist brief:** the mount: a Gate Warden's eye, a faceted crystal sphere retuned from the Warden's crimson to ice-white (#bcd8f0, a pale blue glow), held ahead of the Stormhawk's nose by a short gunmetal collar with two claws (the ship's own hull colours); about 8×10 px of the 48 px ship at 1x. The fire: a **wave front**, an arc of a ring 32 px wide at the nose, widening evenly to 160 px (L1) or 240 px (L5) at its reach of 300 / 380 px, flying up at 520 px/s; drawn as one white crescent, thickest in the middle and tapering to nothing at its ends, an ice-blue glow (#96e1ff) and a deep-blue fringe (#288cff), a thin dark violet-black rim either side of the bright edge (it must read over Level 44's pale plane), two faint rings trailing it 8 and 15 px behind (a struck bell). It fades out over the last fifth of its reach; each hull it passes leaves it a step dimmer (× 0.75) and rings a thin ring on the unit. Blue / white only (the player's shot hues): no violet, no teal, no gold. Additive light, smooth (at least 32 steps, no dither, drawn once), every glow fading to 0 inside its canvas.
- **AI prompt:** "pre-rendered 1990s CGI game effect, a thin glowing white-blue crescent sound wave widening as it rises from a small crystal emitter on a fighter's nose, two faint ripples behind it, a thin dark outline, passing through alien ships, top-down shoot'em up, on black, additive glow"
- **Mockup generator:** `python3 tools/concept/resonator_r85.py a` (round 85, step RES; the mount built in the file on the frozen `tools/concept/render/` package; the backdrops and the Rift Skater from `enemies_r84.py`, the one Vrell palette from `act7_r84.py`, the Stormhawk from `gear_r36.py`)

## choir-resonator-r85-b

**Choir Resonator b — "the chord"** (round 85, step RES). Files: `choir-resonator-r85-b.png` (sheet) and `choir-resonator-r85-b.gif` (the same scene).

- **Artist brief:** the mount: a tuning fork, two slim gunmetal prongs either side of the nose, their inner faces lit ice-white, the eye's crystal (a small faceted lens) set between them at their root. The fire: the same widening arc drawn as **three thin parallel arcs** 5.5 px apart (a chord), the middle one brightest, white cores with a pale blue glow (#aacdff) and a wide deep-blue fringe (#1e5aff), small nodes of light running along the arcs (the chord's shimmer), a thin dark rim round each arc; the same fade, steps and ring per hull as a. Finer and lighter than a. Blue / white only; additive, smooth, drawn once, fading to 0 inside its canvas.
- **AI prompt:** "pre-rendered 1990s CGI game effect, three thin parallel glowing blue-white arcs widening as they rise from a tuning-fork emitter on a fighter's nose, nodes of light running along them, passing through alien ships, top-down shoot'em up, on black, additive glow"
- **Mockup generator:** `python3 tools/concept/resonator_r85.py b` (as a)

## choir-resonator-final-r88-a

**Choir Resonator — production frames of round 85's A.1 a "the bell"** (round 88, M11 part A, step ART-A). Files:
`choir-resonator-final-r88-a.png` (sheet) and `.gif` (1×). Generator: `python3 tools/art/act7_gear.py resonator`
(the mount is `tools/concept/resonator_r85.py`'s `mount_model("a")` rendered per banking frame; the wave is its
`draw_wave` variant a as a supersampled light field).

- **Brief:** as A.1 a's: the wave one white crescent, thickest in the middle and tapering to nothing at its ends, an
  ice-blue glow (#96e1ff) and a deep-blue fringe (#288cff), a thin dark violet-black rim either side of the bright edge,
  two faint rings trailing it 8 and 15 px behind; drawn as 16 frames by width (32 to 260 px) that the game scales in x
  to the wave's exact width, its brightness the flight's fade (up in the first 12 px, out over the last fifth) times
  0.75 a hull passed. The ring per hull: a thin ice-blue ring ringing out from 6 to 20 px over 0.27 s with a white centre.
  The mount: the Warden's eye, an ice-white faceted crystal, in a gunmetal collar with two claws ahead of the nose,
  rolled with the hull's five banking frames; the nose's soft ice-blue glow, brighter for 0.1 s after each wave.
  Blue / white only; additive, smooth (unquantised, no dither), every glow fading to 0 inside its canvas.

## resonator-capture-r88-a

Concept round 88, a capture of the game (M11 part A, step A4): the fronts at L1 and L5 over Level 43's and 44's tiles. The [A4 capture rig](../../../../world/vrell-space/concept/prompts.md#act7-rig-capture-r88-a). The GIF was made again by step FIXA (2026-11-05) after the crash fix: Level 43's own data in the game built by `:desktop:installDist` (its ambience and music swapped for Act 5's `belt-open` and track 15 in a scratch copy, as neither exists yet), `--level 43 --invulnerable --loadout front=choir-resonator:5`, `--shots` every 4 ticks from tick 2160 to 2760 (36–46 s, the first Gate Warden), the play field cropped and scaled to 360×405, 15 frames a second (ffmpeg palettegen/paletteuse); the still is A4's.

## resonator-play-capture-r88-a

Concept round 88, a capture of the game (M11 part A, step A4): the Resonator in play on Level 36. Made again by step FIXA (2026-11-05) after the crash fix, the game built by `:desktop:installDist` on a private Xvfb display: `--level 36 --invulnerable --loadout front=choir-resonator:5 --special swarm-call`, auto-fire on, the Swarm Call called (XTest key X) at tick 600, `--shots` every 8 ticks from tick 512 to 1392 (14 s), full screen scaled to 640×360, 7.5 frames a second (ffmpeg palettegen/paletteuse). A4's settings as in the [A4 capture rig](../../../../world/vrell-space/concept/prompts.md#act7-rig-capture-r88-a).

## resonator-band-capture-r89-a

Concept round 89, a capture of the game, not generated art (M11 part B, step B5): the Choir Resonator over Level 44's brightest band. The [B5 captures](../../../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/concept/prompts.md#game-captures-r89-a).
