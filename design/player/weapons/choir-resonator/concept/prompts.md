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
