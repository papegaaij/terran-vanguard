# Deflector Pod — prompts

Round 36 showed no look for the pod: two production proposals for concept round 37 (step ART-A),
`python3 tools/art/deflector_pod.py [--variant a|b] [--review]`; variant a is written to `assets/`
(the pod per banking frame and side, the field's half-disc per level and side, the catch flash at 16
headings), b only to its review files.

Artist brief: keep the chosen round-08/09 families exactly (player light blue / white / cyan,
soft-edged; nothing orange: that is the enemies' bullets); pre-rendered models lit from the top left,
every heading its own render, never rotated at runtime; one palette per frame set; additive effects
stored premultiplied on black.

## deflector-pod-r37-a

**Field band.** A short pod with a cyan emitter dome on its outer side; the field a thin cyan band
round the half-disc (the shield ring's colours) with a faint glow inside it, fading to nothing at the
centre line; a caught bullet lights a short arc of the band white with four sparks.

- **Prompt:** top-down effect frames for a late-1990s pre-rendered CGI vertical shoot'em up: a small
  pod with a glowing cyan dome on each wing of a blue-and-white fighter, a faint thin cyan half-ring
  force field round each side of the ship, enemy pink bullets flashing white on it where they are
  caught; Raptor / Tyrian 2000 era look.
- **Negative prompt:** full opaque bubble, orange, photographic realism, side view, watermark.
- **Animation brief:** the catch flash 6 frames at 2 steps; the field brightens for 0.15 s as a
  charge returns and is gone while the pod is empty.

## deflector-pod-r37-b

**Plate lattice.** A pod with three emitter vanes tipped with cyan lights; the field as five thin
hexagonal-cell plates along the half-disc with gaps between them; a caught bullet flashes the plate it
hit and throws a small spark ring.

- **Prompt:** as A, but the field drawn as a few separate thin cyan lattice plates in a half-ring, a
  plate flashing white with a small spark ring where a bullet is caught.
- **Negative prompt:** as A.
- **Animation brief:** as A.
