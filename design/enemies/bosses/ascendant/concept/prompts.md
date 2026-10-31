# Ascendant — concept prompts

Round 72 (Act 6 "Jovian Storm" design phase, step ART-R-B, 2026-10-31): a/b mockups by `tools/concept/act6_r72.py`
on the frozen `render` package (`python3 tools/concept/act6_r72.py ascendant [a|b]`), in the elite livery of the gap
analysis's Q9 a and the fight of Q6 a (both pending the user's review in round 72). The hull is drawn 1:1 in screen
pixels (100 px a model unit) at the arena table's places; the parts (bow cannon, generators, batteries with their
mirror panels, sacs, heart, lifeboat) are separate sprites in their states; each sheet measures the pieces' share of
one 2048×2048 atlas page (the one-page boss budget).

**Shared look:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a vast warship seen bow-on
from directly above (bow pointing down the screen), strong key light from the top-left, glossy highlights.
Ascendancy elite livery: gloss black hex-panelled armour (#1a1622), heavy gold (#ffa800) on 35–45 % of the
silhouette, red running lights (#ff2a1e). Phase 2: teal-black Vrell tissue (#1e5c5a) with glowing teal seams
(#00ff9a) and lime weak points (#beff3c) erupting through the gold plates.

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos other
than the Ascendancy crest, grey Coalition livery, blue lights, magenta glows (reserved for the bounced bullets).

## ascendant-r72-a

**Ascendant A, the gilded wedge (Level 42 boss)**: one smooth wedge from the top edge (x 40–440) to the bow tip
(240, 262). Files: `ascendant-r72-a.png` (sheet).

- **Prompt:** The shared look. A smooth gloss-black wedge, two slanted faces with gold ribs fanning down them, a gold
  spine and edge, the crest disc on the bow, red lights along the faces. Two gold-collared shield generators with
  white-gold cores on the shoulders, four squat batteries behind polished silver-gold crescent mirror panels, a
  domed bow cannon with a red lens under a gold hex shield. Phase 2: the keel split open, teal-black tissue bulging
  out along it, gold plates peeled outward at the rim, two breaches with tissue sacs (lime cores), the heart (a lime
  core under a teal membrane) at the hull's centre. The lifeboat: a round Vrell seed-pod with the gold escape capsule
  half swallowed, its amber-lit canopy showing through.

## ascendant-r72-b

**Ascendant B, the armoured tiers**: the bow stepped in six tiers of one identical armour block. Files:
`ascendant-r72-b.png` (sheet).

- **Prompt:** The shared look. A stepped, terraced bow: rows of identical gloss-black armour blocks with gold fronts
  and spines (7 blocks wide at the top, 2 at the bow), dark gaps between them, gold crown fins at the step corners;
  the same parts as A. Phase 2: blocks thrown aside at six gaps, tissue bursting through them (asymmetric). The
  lifeboat: a long thorned Vrell pod, bone thorns, tendrils trailing, the capsule's canopy at its head.

## ascendant-phases-r72-a

**The phases, look A**: the phase storyboard. Files: `ascendant-phases-r72-a.png` (storyboard sheet).

- **Brief:** Nine key frames at 0.5× over Level 42 (black space, Callisto's curve, Jupiter's limb, the home fleet far
  off): the `WARNING` banner with no hull yet; the hull sliding in bow-first, lights coming on; phase 1 from the far
  side (the left face's panels open to shots from the right, the spinal lance's red line, magenta bounced shots); the
  generators dead and the cannon's shield gone, a panel cracked, one down; the eruption (the keel splitting, Vorne's
  shout); phase 2 (the heart open, a 3-arm spore spiral, a brood); K (the chain down the keel, the flash, the credit
  shower); the escape (the wreck's halves drifting apart, the lifeboat climbing with its spore wake, the escape bar
  `VORNE`); `ESCAPED` (the flash at the top edge).

## ascendant-phases-r72-b

**The phases, look B**: as A with look B's hull. Files: `ascendant-phases-r72-b.png` (storyboard sheet).

## ascendant-phases-r73-a

**The phases, look A, frame 8's jagged break** (round 73, step m10-f72, 2026-10-31; round 72 A.11 "change": "the hard
lines of the boss breaking up in sheet 8 look a bit strange. It says the hull breaks, but it is a clean cut").
Files: `ascendant-phases-r73-a.png` (storyboard sheet), generated with
`python3 tools/concept/act6_r72.py phases73 a` (the r72 sheets stay as they were).

- **Brief:** as `ascendant-phases-r72-a`, with frame 8 (the escape) redrawn by the rule now in the README: the hull
  breaks along a **jagged fracture**, never a straight clean cut. The phase-2 hull splits down the keel along a seeded,
  irregular line (long kinks, small alternating teeth, an odd spike; `ragged_line`, seed 7301); both halves keep a
  ragged edge with torn plate flaps bent outward across the gap (gunmetal, their lit lip catching the top-left sun),
  the plating charred dark toward the edge and glowing hot in places (`torn_half`); the halves drift apart, each
  turned 6° outward; shards of plate (gunmetal, now and then gold) tumble in the gap and gas vents from both broken
  edges, hot orange from the plating and teal from the Vrell tissue (`wreck_break`; smooth Gaussian plumes, no
  dither). The lifeboat climbs out of the gap with its spore wake. Hull a only (chosen, A.10 a).
- **Prompt (AI generator):** late-1990s pre-rendered CGI game sprite, straight top-down, a vast gloss-black and gold
  wedge warship torn in two down its keel over black space, the two halves drifting apart and tilting outward, a
  ragged, jagged fracture edge with torn and bent armour plates, charred edges glowing orange-hot in spots, debris
  shards tumbling in the gap, jets of orange fire and teal vapour venting from both broken edges, teal-black alien
  tissue torn along the break, a small organic escape pod with a gold capsule climbing out of the gap.
