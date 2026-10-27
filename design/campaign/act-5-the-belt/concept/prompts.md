# Act 5 – The Belt — concept prompts

Round 60 (Act 5 "The Belt" world proposals, step ART-P-world): a/b mockups by `tools/concept/belt_r60.py`
(`python3 tools/concept/belt_r60.py mutineers [a|b]`), pending the user's review in round 60 (Q10 b).

## wavering-stormhawk-r60-a

**Wavering Stormhawk A, Ceres Wing ochre (level-owned craft, Level 29)**: 40×40 px (hitbox 28), the CDF Stormhawk
airframe of the player and Rook (`render/models.ship_c_model`, the Stormhawk's perspective camera and bank frames)
in Ceres Wing's colours. Files: `wavering-stormhawk-r60-a.png` (sheet) and `wavering-stormhawk-r60-a.gif` (the play
field: a pair beside a Talon wing in the red-lit dock canyon).

- **Prompt:** Late-1990s pre-rendered CGI game sprite, straight top-down, lit from the top-left: a blended-wing
  manta fighter (the CDF Stormhawk) in CDF light grey with sand-ochre wing panels and dark ochre tips, the Ascendancy
  crest (a gold sun and ring on a black disc) freshly painted over the roundel on both wings, a white IFF light on
  the spine behind the canopy, blue engines.
- **Negative prompt:** photo, painterly, perspective or 3/4 view, motion blur, text, black-and-gold Ascendancy hull,
  red running lights, weapons firing.
- **Animation brief:** A pair enters at the top, descends at 90 px/s to about 150 px below the top edge, holds about
  6 s with a slow sway and a wing-waggle (bank frames ±15°/±30°), then turns and peels off to the nearer side edge at
  260 px/s. The IFF light blinks white at 1 Hz; a hit flashes it red for 0.4 s (a warning). No fire, no contact.
- **Mockup generator:** `python3 tools/concept/belt_r60.py mutineers a`

## wavering-stormhawk-r60-b

**Wavering Stormhawk B, hasty blackout (Level 29)**. Files: `wavering-stormhawk-r60-b.png` and
`wavering-stormhawk-r60-b.gif`.

- **Prompt:** As A, but the CDF grey scheme half overpainted black on the outer wings with a ragged brushed edge,
  gold trim, the crest stencilled over the roundel: a craft caught halfway into the Ascendancy's colours.
- **Negative prompt:** as A.
- **Animation brief:** as A.
- **Mockup generator:** `python3 tools/concept/belt_r60.py mutineers b`
