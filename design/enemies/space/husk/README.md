---
title: Husk
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../art-direction, ../../../world/vrell-space, ../../../story, ../../../campaign/act-7-beyond-the-gate]
updated: 2026-11-05
---

# Husk

## Summary

A dead Vrell hull drifting in the graveyard of the Choir (Level 46). It is hollow: something emptied it from inside,
and pale crystal grows along its cracks. A Husk is harmless until it is hurt; at half its HP it cracks open and
sprays crystalline shards, and it bursts in a ring when it dies. Its look is the game's one visual hint of the
Silence: never shown, only what it left.

## Design

### Stat block

**Draft** (Act 7 design phase, step m11-d-enemies-a, 2026-11-04; decided 2026-11-05, round 84, Q4 = a). A
hand-written table: there is no `data.yaml` yet. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP in damage units (a Pulse Cannon L1 shot does 2), set at its
first level from the proposed Act 7 reference DPS (J11, L46 = 184). A `medium` unit: no beam-era factor; the act HP
factor in its later levels.

| Field | Value |
|---|---|
| Faction | Vrell, **Silence-touched** (its look in [factions](../../README.md#factions-and-visual-language)); dead tissue: the [EMP Burst](../../../player/specials/README.md#emp-burst-l15) stuns it 1.5 s as any Vrell unit (its crack and drift clocks stop) |
| Layer | `space` (the `air` layer) |
| Size tier | `medium` |
| Size | 88×72 px (three hull shapes, the art step's), hitbox 64×48 |
| Parts | single (whole, then cracked: a state at 50 % HP) |
| Orientation | `radial`: it turns slowly as it drifts (10–25°/s, its own sign), drawn at 32 angles |
| HP | 150 (easy 112 / hard 195, from the global multipliers): ≈ 0.82 s at the L46 reference (184); it cracks at 75 |
| Armour / shield | none |
| Speed | 20–40 px/s |
| Movement | `drift`: a slow straight drift in any direction across the field, entering from any edge (unwarned: a drifting derelict is scenery until hit, so no edge warning, as a re-entry), leaving through the far edge |
| Attack | **none while whole**: it never fires undamaged (the quiet's rule). **Crack**: when its HP first falls to 50 %, a **0.5 s** fracture tell (the cracks widen and flare pale), then a `fan` of **5** crystalline shards centred on the ship, 15° apart, fast class 220 px/s, `small` = 4 (drawn as the bullet rules' fast needle, shard-shaped); once. **Death**: `death-burst`, a `ring` of **8** slow orbs (100 px/s, `small` = 4). A level may crack every Husk on the screen on cue (L46's end of the quiet), with the same 0.5 s tell. Contact `medium` = 15 |
| Formations | swarm (scattered): 3–6 Husks on parallel or crossing drifts, hulls ≥ 80 px apart and on lines that never meet on the screen; at most 6 on the screen |
| Weak points | the cracks, after it cracks (drawn only) |
| Effective traits | `homing` (they drift from every side), `piercing` (a column of drifting hulls) |
| Credits | 25 (score 250 × chain) |
| Death | `medium` collapse: the hull folds inward and breaks into slow grey slabs and pale crystal splinters that fade (presentation, no damage beyond the ring) |
| First level / used in | L46 (Graveyard of the Choir, the act's breather); later Act 7 levels as the level drafts place it |
| Difficulty hooks | easy: fan of 3, death ring of 6; hard: fan of 7, ring of 10, crack tell 0.4 s |

### Behaviour

- **The quiet** (Q4 a, the level's): about 20 s in which Husks drift across the screen and nothing fires unless the
  player fires first. The drifting hulls keep the screen "busy" for the pacing rules (`PacingTest` counts them on
  the screen); L46's document sets the breather's shots-floor exception. Shooting a Husk in the quiet is the player's
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

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [ ] Stat block in a `data.yaml`; global multipliers; checked with `levelReport` at L46 (the quiet inside the pacing
  rules)
- [ ] `drift` with a slow spin; the crack at 50 % (the 0.5 s tell, the `fan`, once), the level's crack cue, the
  `death-burst` ring; deterministic and allocation-free
- [ ] Production sprites (three hull shapes, 32 angles, whole and cracked, the crack flare, the collapse) via
  `tools/art/`; sounds (Freesound, CC0/CC-BY)

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
