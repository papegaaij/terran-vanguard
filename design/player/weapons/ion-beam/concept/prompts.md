# Ion Beam — prompts

## ion-beam-final-r37-a

Production art (M7 part A batch, step ART-A, for concept round 37), not a mockup: `python3 tools/art/ion_beam.py` writes the effects into `assets/sprites/` and this review sheet
and GIF (`--review` rebuilds only the review files). The look is the one round 36 chose (G.1 = a,
[the round-36 mockup](../../concept/ion-beam-r36-a.png), `tools/concept/gear_r36.py`): round 09's beam
column (`vfx_r09.beam_segment`, its periods snapped so a 64 px tile loops in 8 frames) per level width
6 → 14 px and 18 px in overdrive, the round-09 contact flare (`contact_frames`, scale 0.8 and 0.55),
the dust-tinted rock-scorch flare with dust thrown back and a scorch mark, the glance spark over
hardened ground.

Artist brief: keep the chosen round-08/09 families exactly (player light blue / white / cyan,
soft-edged; nothing orange: that is the enemies' bullets); pre-rendered models lit from the top left,
every heading its own render, never rotated at runtime; one palette per frame set; additive effects
stored premultiplied on black.
- **Prompt:** top-down effect frames for a late-1990s pre-rendered CGI vertical shoot'em up: a continuous
  cyan ion beam with a white-hot core rising from a small fighter's nose, faint ripples running up it,
  narrowing and whitening where it bores into the first target, a white-blue contact flare with a flat
  splash crescent and sparks thrown back; on rock the flare dusty with dust kicked out and a dark
  scorch; Raptor / Tyrian 2000 era look.
- **Negative prompt:** orange or red heat, lightning, photographic realism, side view, watermark.
