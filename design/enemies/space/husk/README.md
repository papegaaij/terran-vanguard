---
title: Husk
design: approved
implementation: done
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../art-direction, ../../../world/vrell-space, ../../../story, ../../../campaign/act-7-beyond-the-gate]
updated: 2026-11-06
---

# Husk

## Summary

A dead Vrell hull drifting in the graveyard of the Choir (Level 46). It is hollow: something emptied it from inside,
and pale crystal grows along its cracks. A Husk is harmless until it is hurt; at half its HP it cracks open and
sprays crystalline shards, and it bursts in a ring when it dies. Its look is the game's one visual hint of the
Silence: never shown, only what it left.

## Design

### Stat block

The approved stat block (Act 7 design phase, step m11-d-enemies-a, 2026-11-04; decided 2026-11-05, round 84, Q4 = a),
**built** in M11 part D (step D2): numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)), from
its [data.yaml](data.yaml) (the table is rendered from it); HP in damage units (a Pulse Cannon L1 shot does 2), set at its
first level from the Act 7 reference DPS (J11, L46 = 184). A `medium` unit: no beam-era factor; the act HP factor in its
later levels. Its drift is `movement.line_drift` (along its wave's `lines`, unwarned), its crack the `crack` key naming
its `shards` fan (the hard tell is the hook `telegraph`), its death ring the `death-burst` (the hooks' `attacks.ring.count`).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (`space`) |
| Size tier | `medium` |
| Size | 88×72 px, hitbox 64×48  (three hull shapes, the art step's) |
| Parts | single |
| Orientation | `radial` : it turns slowly as it drifts (10–25°/s, its own sign), drawn at 32 angles |
| HP | 150 (easy 112 / hard 195, from the global multipliers) |
| Armour / shield | none; **Silence-touched** dead tissue (its look in [factions](../../README.md#factions-and-visual-language)): the [EMP Burst](../../../player/specials/README.md#emp-burst-l15) stuns it 1.5 s as any Vrell unit (its crack and drift clocks stop) |
| Speed | 30 px/s (each Husk drifts at 20–40 px/s) |
| Movement | `line_drift`: a slow straight drift in any direction across the field along its wave's line, entering from any edge (unwarned: a drifting derelict is scenery until hit, so no edge warning, as a re-entry), leaving through the far edge |
| Attack | **none while whole**: it never fires undamaged (the quiet's rule). **Crack**: when its HP first falls to 50 %, a **0.5 s** fracture tell (the cracks widen and flare pale), then a `fan` of **5** crystalline shards centred on the ship, 15° apart, fast class 220 px/s, `small` = 4; once. A level may crack every Husk on the screen on cue (L46's end of the quiet), with the same tell; **Death**: `death-burst`, a `ring` of **8** slow orbs (100 px/s, `small` = 4). Contact `medium` = 15 |
| Formations | swarm (3–6), single (scattered: 3–6 Husks on parallel or crossing drifts, hulls ≥ 80 px apart and on lines that never meet on the screen; at most 6 on the screen) |
| Weak points | the cracks (after it cracks; drawn only) |
| Effective traits | `homing`, `piercing` |
| Credits | 25 (score 250 × chain) |
| Death | `medium` collapse: the hull folds inward and breaks into slow grey slabs and pale crystal splinters that fade (presentation, no damage beyond the ring) |
| First level / used in | L46; L46 (Graveyard of the Choir, the act's breather); later Act 7 levels as the level drafts place it |
| Difficulty hooks | easy: fan of 3, death ring of 6; hard: fan of 7, ring of 10, crack tell 0.4 s |
<!-- /data -->

### Behaviour

- **The quiet** (Q4 a, the level's): about 24 s (Level 46's t=120–144) in which Husks drift across the screen and
  nothing fires unless the player fires first. The drifting hulls keep the screen "busy" for the pacing rules
  (`PacingTest` counts them on the screen); the shots floor is level-wide, so no shots-floor exception is needed
  (Level 46's *Density*). Shooting a Husk in the quiet is the player's
  choice, and it answers.
- **The play.** Kill it fast (past the crack before its tell ends: a burst of the whole fit from 100 % does it) or
  stand off its fan; the death ring follows wherever it dies, so a Husk killed beside the ship costs a dodge. Hulls
  drift from every side: homing weapons pay.
- **Spacing**: two Husks never overlap (their drift lines are authored apart), nor drift over another `medium` or
  larger hull.
- **Look** (Q4 a, Q10 b; the hint): grey-brown Vrell hull plates, dull and lightless (the only Vrell with no glow at
  all), breached from the **inside**: the plates bent outward round empty holes, the interior a dark hollow with
  nothing in it. Pale cold crystal grows along every crack and seam, faintly lit from within. Cracked, the crystal
  splits and flares once. Nothing about it says what did this.
- **Sounds** (brief for the audio step, Freesound CC0/CC-BY): a slow hull creak while it drifts (stressed ice or old
  metal), the crack a real ice fracture, the shards a short glassy splinter, the death a hollow collapse with a glass
  tinkle; no voice, no bioluminescent hum.

### Difficulty and level use

- At most 6 on the screen; never a Husk's crack while another Husk's fan is still in flight next to it (the level
  keeps cracked hulls ≥ 120 px apart, or cues them in turn).
- Level 46's waves after the quiet mix Husks with the Vrell's remnants (the level's choice of returning units).

## Concept art

Round 84's proposals (step ART-R-A, M11 design phase, picked 2026-11-05; generator `tools/concept/enemies_r84.py husk`). Each sheet holds the source render, the sprites at play size with their states, the readability check over Level 46's brightest and darkest band beside the player (a stand-in of the level's light in the one Vrell palette, Q10 b), and key frames; each GIF is the 480×540 play field. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/husk-r84-a.png](concept/husk-r84-a.png) | Husk a, "punctured": ash grey-brown hulls punched open from inside, the plates bent out round round holes, pale crystal along the cracks; three hull shapes, the crack tell, cracked, 8 of 32 angles, its shard and orb; whole and cracked over the cold light and the dark | chosen (round 84 A.3 a) |
| [concept/husk-r84-a.gif](concept/husk-r84-a.gif) | The quiet over the dead fleet: three Husks drift, nothing fires until the ship does; one cracks at half its HP (the tell, the fan of 5), dies (slabs, splinters, the ring of 8); then the level's cue cracks the other two 0.6 s apart | chosen (round 84 A.3 a) |
| [concept/husk-final-r91-a.png](concept/husk-final-r91-a.png) | Production (round 91, step ART-D, `tools/art/act7_deadfleet.py husk`; in `assets/` since step D2's data claimed `husk`): the three hull shapes at 8 of their 32 headings whole and cracked (104×104), the tell's flare, the collapse (slabs and the cold glints), the shard (16 headings) and the orb, the intel portrait, and the Husks beside the paler, flatter scenery hulks at 1:1 | chosen (round 91 F.1 accept) |
| [concept/husk-final-r91-a.gif](concept/husk-final-r91-a.gif) | Three Husks drifting through the quiet over Level 46's production backdrop, one cracking (the tell) and collapsing | chosen (round 91 F.1 accept) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [x] Stat block in a `data.yaml`; global multipliers; checked with `levelReport` at L46 (the quiet inside the pacing
  rules; M11 part D, step D2)
- [x] `drift` with a slow spin; the crack at 50 % (the 0.5 s tell, the `fan`, once), the level's crack cue, the
  `death-burst` ring; deterministic and allocation-free (step CD1's simulation, step D2's data and `Level46Test`)
- [x] Production sprites (three hull shapes, 32 angles, whole and cracked, the crack flare, the collapse) via
  `tools/art/`; sounds (Freesound, CC0/CC-BY) (steps ART-D, AUD-CD and D4; round 91)

## Open questions

None open: round 84's items were decided 2026-11-05 (see *Decisions*).

## Decisions

- 2026-11-04 (step m11-d-enemies-a): drafted for the Act 7 design phase from the roster row and the M11 gap analysis
  (§2.2, Q4 a, Q10 b): harmless until damaged, the crack at 50 % with a 0.5 s tell and a `fan`, the `death-burst`
  ring, hollowed from inside with pale crystal at the cracks. The space roster's open question ("should Husks show
  the Silence?") is answered by that look. The HP, the fan of 5 fast shards, the ring of 8, the level's crack cue and
  the unwarned drift are this step's. Decided 2026-11-05, round 84.
- 2026-11-05, round 84 (the user's page answers; decided as drafted unless stated, the alternatives rejected). The open items were:
  - **Q4** (round 84): **a** (drafted) the quiet with harmless drifting Husks and their hollow look, a starless patch in
    the backdrop; **b** the same plus a glimpse at the level's end (the patch moves: breaks "never shown"); **c** radio
    only, no patch (a weaker hint). The Husk's own rules are the same under a, b and c.
- 2026-11-05 (user, round 84 closed): **A.3 a "punctured"** chosen (a hull punched open from inside, plates bent outward round round holes); b "split" rejected (moved to `concept/rejected/`); **Q4 a** as drafted; **D.10 approved**; the sounds are S.9 b, S.10 a, S.11 a, S.12 a.
- 2026-11-06 (M11 part D, step D2; pending the user's review in round 91): built as data. The drift is a new movement key
  `line_drift` (the vocabulary's `drift` is the straight-down one): a speed and a spin range, the Husk's lines its
  wave's; the crack's fan is an attack of its own (`shards`, a `fan` of 5 with a 60° spread, 15° apart however the hooks
  size it) named by the `crack` key, the hard tell the hook `telegraph`, the death ring's easy/hard counts the hooks'
  `attacks.ring.count`; `orientation: radial` with its 32 drawn angles as presentation (the spin). "A Husk wave waits
  while 4 drift" is not built: Level 46's lines keep at most 6 on the screen (`Level46Test` measures it).
- 2026-11-06, round 91: F.1 accepted (the Husk's production art: whole and cracked, the tell's flare).
