# Shields — concept prompts

## shield-ring-final-r26-a

Round 26, production art (M4 part H batch). Review sheet (`.png`) and loop (`.gif`) made from the final files in `assets/` by `tools/art/ship_fx.py --review`; the frames themselves are rendered by `tools/art/ship_fx.py` (see `tools/art/README.md`). The look is the chosen round-08 shield-hit shimmer (`tools/concept/vfx_r08.py`, `shield_frames`: a hex grid on the bubble, a ripple running from the hit point and a white flare there, in the player shot blues `00C0FF` and white), drawn without the hull on the asset table's 60×60 round bubble with slightly larger cells (5.5 across the radius) so they survive at 1×. Check that it reads as a shield and not as a bullet, that it never hides incoming bullets (it is additive and dim apart from the flare), and that the fade over 4 frames is not too abrupt. Not an image-generator prompt; for an artist: "a 60 px round energy bubble of faint hexagonal cells around a top-down fighter, lighting up in pale cyan around an impact at the front, a ripple running back over it and fading, additive, 4 frames, late-90s pre-rendered game effect".

## reflex-shield-final-r73-a

**Reflex Shield production frames** (round 73, M10 part A, step ART-A). Files: `reflex-shield-final-r73-a.png` (sheet)
and `.gif` (2×). Generator: `python3 tools/art/act6_gear.py reflex` (frames `ship-reflex_0..3`, `ship-reflex-bolt_0..3`,
`ship-reflex-bolt-trail`).

- **Brief:** while the window is open the shield hit's hex flash is gold: `tools/art/ship_fx.py`'s ring (the hexes round
  a hit at the front, a ripple over the bubble) in gold #ffce5a and white-gold, additive, smooth; the bolt leaves from
  the hit straight up at 640 px/s.

## reflex-bolt-r73-a

**QA-3 a, "white-gold bullet"** (round 73, recommended unless the renders show bolts read as enemy fire; written to the
game's files until the pick). Files: `reflex-bolt-r73-a.png` (sheet) and `.gif` (2×). Generator:
`python3 tools/concept/act6_r73.py bolt a` (frames from `tools/art/act6_gear.py`).

- **Brief:** the enemy orb's round shape (15×15) in the shield's white-gold: a dark brown-gold rim, a gold ring, a white
  core pulsing, normal blend; under it an additive gold trail 25 px long; shown beside the enemy orb, the Ascendancy's
  gold needle and the mirror's bounced magenta orb on Level 36's cream.
- **AI prompt:** "pre-rendered 1990s CGI game sprite, a small round white-gold energy bullet with a dark rim and a
  short gold trail flying straight up, top-down shoot'em up"

## reflex-bolt-r73-b

**QA-3 b, "gold needle"** (round 73). Files: `reflex-bolt-r73-b.png` (sheet) and `.gif` (2×). Generator:
`python3 tools/concept/act6_r73.py bolt b`.

- **Brief:** a thin gold needle (7×24) with a white core and a 1 px dark rim, a faint thin trail, so a bolt never
  resembles an enemy bullet in the field; the rest as a.
- **AI prompt:** "pre-rendered 1990s CGI game sprite, a thin gold energy needle with a white core flying straight up,
  faint trail, top-down shoot'em up"
