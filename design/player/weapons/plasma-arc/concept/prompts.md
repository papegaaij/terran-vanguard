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
