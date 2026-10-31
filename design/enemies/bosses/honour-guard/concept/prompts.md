# Honour Guard — concept prompts

Round 72 (Act 6 "Jovian Storm" design phase, step ART-R-B, 2026-10-31): a/b mockups by `tools/concept/act6_r72.py`
on the frozen `render` package (`python3 tools/concept/act6_r72.py guard [a|b]`), in the elite livery of the gap
analysis's Q9 a and the fight of Q5 a (both pending the user's review in round 72). The aces are the Talon's own
airframe (`em.talon`'s plates, re-modelled nose-forward in the generator) at 1.5× the Talon (76 px cells, 30 px a
model unit), rendered at 16 headings with the round-60 rim (`enemies_r60.belt_rim`); each sheet measures the gold
share. The storyboard is hand-placed key frames of the 480×540 field over Level 40's backdrop; the conductor's
timeline is a seeded run of the three playbooks under the conductor's rules (the generator's `conductor`).

**Shared look:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a forward-swept stealth
interceptor seen from directly above, strong key light from the top-left, glossy highlights, crisp silhouette.
Ascendancy elite livery: gloss black hex-panelled hull (#1a1622), heavy gold (#ffa800) on 35–45 % of the silhouette
(leading edges, outer wing panels, the spine, the tail tips), a thin bright gold rim on the edges facing the light
and a red rim on the far edges, small red running lights (#ff2a1e) at the wing tips.

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, motion blur, text, logos other
than the Ascendancy crest, grey Coalition livery, blue or cyan lights, magenta glows (reserved for bullets).

## honour-guard-r72-a

**Honour Guard A, marks on one airframe (Level 40 mid-boss)**: the three aces on the Talon's airframe, told apart by
paint. Files: `honour-guard-r72-a.png` (sheet).

- **Prompt:** The shared look. Three identical forward-swept interceptors. *Harker* (the sniper): a long gunmetal
  sniper barrel under the nose, a red band round the nose. *Vesper* (the dancer): white ribbon stripes along the
  wings' trailing edges and the tail. *Kestrel* (the feinter): gold-and-black chevrons across the wings, a red tail
  band, twin swept vertical fins. Sheet: the source renders, 8 of 16 headings each at 1×, rest / tell / hex-shield
  crops (the barrel's red glint; the ribbons flaring white; the engines flaring white), the three over Level 40 at
  1× and over Jupiter's cream limb at 2×.

## honour-guard-r72-b

**Honour Guard B, three silhouettes**: each ace's airframe changed so the shape tells it before the paint. Files:
`honour-guard-r72-b.png` (sheet).

- **Prompt:** The shared look and A's marks. *Harker*: the nose stretched to a needle, a longer barrel. *Vesper*:
  long, slim, swept-back wings with trailing white ribbon streamers. *Kestrel*: a short, wide body with small
  forward canards and bigger twin swept fins.

## honour-guard-joins-r72-a

**The joins, look A**: the join order and the conductor's one-line-at-a-time rule. Files:
`honour-guard-joins-r72-a.png` (storyboard sheet).

- **Brief:** Eight key frames at 0.5× over Level 40 (black space, Callisto's disc, Jupiter's limb): the sting and
  the bar `HONOUR GUARD` in three hatched segments with Harker's chevron tell at the top edge; Harker alone with the
  sniper line; Vesper's chevron tell on the side away from Harker; two aces (Vesper's spiral loop, Harker sliding);
  Kestrel's chevron tell at the bottom edge; three aces with Kestrel's dive told and Harker's line waiting; the
  pincer; Vesper down and the survivors' sharper edges. Under them a 60 s timeline in three lanes: tells hatched,
  lines and dives red, firing gold, the measured worst case (one line at a time, two aces firing).

## honour-guard-joins-r72-b

**The joins, look B**: as A with look B's aces. Files: `honour-guard-joins-r72-b.png` (storyboard sheet).
