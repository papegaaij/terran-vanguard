# Plasma Arc — concept prompts

## plasma-arc-r72-a

**Plasma Arc a — "the wide Tesla"** (round 72, step ART-R-A). Files: `plasma-arc-r72-a.png` (sheet) and `plasma-arc-r72-a.gif` (the 480×540 play field, 20 fps).

- **Artist brief:** the Tesla Coil Pod's `arc` family drawn wider and hotter: a jagged white core in a blue-white glow (#8cc8ff) with a violet fringe (#9650ff) and two short forks, 1.7× the pod's width on the first strike; each jump of the chain is drawn 0.72× as wide as the one before; every discharge shows for 0.1 s; on every struck unit a white flash and six short sparks fading in 0.2 s. Additive light, smooth (at least 32 steps, no dither, drawn once). The player's fire is never gold.
- **AI prompt:** "pre-rendered 1990s CGI game effect, a thick jagged blue-white electric arc with a violet fringe forking from target to target, thinner at each jump, white flashes where it strikes, on black, additive glow, top-down shoot'em up"
- **Mockup generator:** `python3 tools/concept/enemies_r72.py plasma-arc [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## plasma-arc-r72-b

**Plasma Arc b — "the braid"** (round 72, step ART-R-A). Files: `plasma-arc-r72-b.png` (sheet) and `plasma-arc-r72-b.gif` (the 480×540 play field, 20 fps).

- **Artist brief:** each strike two smooth plasma strands twisting round each other along the line, pinched at both ends, a cyan glow (#78ecff) and an indigo fringe (#697dff) kept away from the mirror's magenta, a white core; each jump a thinner braid; the same flash and sparks per struck unit. Additive light, smooth, drawn once.
- **AI prompt:** "pre-rendered 1990s CGI game effect, two cyan plasma strands braided into a glowing rope that jumps from target to target, thinner at each jump, white core, on black, additive glow, top-down shoot'em up"
- **Mockup generator:** `python3 tools/concept/enemies_r72.py plasma-arc [a|b]` (round 72, step ART-R-A; models built in the file on the frozen `tools/concept/render/` package)

## plasma-arc-final-r73-a

**Plasma Arc production frames** (round 73, M10 part A, step ART-A). Files: `plasma-arc-final-r73-a.png` (sheet) and
`.gif` (1×, 30 fps). Generator: `python3 tools/art/act6_gear.py arc` (frames: `plasma-arc-arc-<j>_0..2`,
`plasma-arc-spark_0..5`, `plasma-arc-glow_0..1`; layouts in the script's header).

- **Brief:** round 72's A.7 a as the game draws it: the Tesla Coil Pod's jagged bolt (white core, blue-white #8cc8ff
  glow, violet #9650ff fringe, two short forks) in a 160×48 strip stretched from source to target, 1.7× the pod's width
  on the first strike and 0.72× as wide each jump (six widths), three shapes in turn; a white flash and six short sparks
  per struck unit over 0.2 s; the nose's glow while it discharges. Additive light, smooth (unquantised), drawn once.
- **Note for A4:** over Level 36's cream the additive arc reads white, its violet fringe faint; the bounced orbs keep
  their dark rim and read apart. The small explosion's additive flash nearly vanishes on the cream band (the round-72
  note), to judge in part B's captures.

## plasma-arc-capture-r73-a

Concept round 73, a capture of the game, not generated art (M10 part A, step A4, 2026-10-31): frames of the
[A4 capture rig](../../../../world/jovian/concept/prompts.md#act6-rig-capture-r73-a); the sheet and the GIF are laid out from its screenshots (the 480×540 play field
or crops, scaled with nearest neighbour).
