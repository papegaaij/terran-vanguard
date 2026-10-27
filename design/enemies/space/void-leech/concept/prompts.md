# Void Leech — concept prompts

Round 60 (Act 5 "The Belt" units, step ART-P-units): a/b mockups by `tools/concept/enemies_r60.py` on the frozen
`render` package, in the belt look of the gap analysis's Q11 a (pending the user's review in round 60).

**Shared negative prompt (Vrell):** photo, painterly, cel shading, perspective or 3/4 view, mechanical parts, metal, human markings, magenta or yellow glows (reserved for bullets), cute.

## void-leech-r60-a

**Void Leech A, ringed leech (Vrell, Level 33)**: 32×32 px, 16 headings × 4 swim frames, a latched curl and a stunned pose; black-violet leeches that latch on the ship and drain the generator in the Leech Field. Files: `void-leech-r60-a.png` (sheet) and `void-leech-r60-a.gif` (the play field).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, key light from the top-left, glossy chitin highlights, crisp silhouette, isolated on a flat dark background. An alien space leech seen from above: five tapering rings of glossy black-violet chitin (#2c1838), a round oral sucker disc at the front ringed with small bone-white hooks, a glowing violet (#9a4dff) power-draw sac on its back.
- **Negative prompt:** the shared list above.
- **Animation brief:** 16 headings × 4 swim frames; latched it curls round the hull spot (left, right, tail) and the sac pulses violet once a second (−2 MW each); a shake-off throws it, stunned 1 s (glow off), then it flees. In L33 a leech latched on a Talon: the Talon's shield gone, it drifts down silent. Death: a dry chitin crack with a violet spark.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py void-leech a`

## void-leech-r60-b

**Void Leech B, ray leech (Vrell, Level 33)**: 32×32 px, 16 headings × 4 swim frames, a latched curl and a stunned pose; black-violet leeches that latch on the ship and drain the generator in the Leech Field. Files: `void-leech-r60-b.png` (sheet) and `void-leech-r60-b.gif` (the play field).

- **Prompt:** As A, but a flat broad undulating mantle like a flatworm, a glowing violet seam and veins, a small hooked mouth at the front and the violet sac.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/enemies_r60.py void-leech b`
