---
title: Naval enemies
design: approved
implementation: in-progress
art: final
updated: 2026-10-20
---

# Naval enemies

## Summary

Enemies on water: surface units (treated as `ground` layer targets on the water surface) and
submerged units on the `sub` layer. They appear on Earth's oceans and arctic in Act 2, and are
the core of the underwater Act 4 on Europa, where the `sub` layer becomes the play plane.

## Contents

Promoted to their own documents for the Acts 1–2 wrap-up, plus the Act 4 drafts of the design phase (2026-10-21, round 48); no unit stays in a roster.

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [driftjelly](driftjelly/README.md) | Jellyfish mine on or below the surface, proximity ring (L11) | approved | in-progress | final |
| [reef-spitter](reef-spitter/README.md) | Barnacle gun on a kelp raft, 3-way fans (L11) | approved | in-progress | final |
| [skimmer](skimmer/README.md) | Flying-fish skiff weaving between floes from every edge (L13) | approved | done | final |
| [glassfin](glassfin/README.md) | Transparent fry school, the act's `tiny` popcorn that streams and rams (L23) | approved | done | chosen |
| [eel-swarm](eel-swarm/README.md) | Segmented eel chain, vital head fires aimed shots (L23) | approved | done | chosen |
| [barbfin](barbfin/README.md) | Small swimmer that holds and spits aimed needles, the Ray's escort (L24) | approved | not-started | chosen |
| [abyss-ray](abyss-ray/README.md) | Manta that rises from a shadow and sweeps a wide fan (L24) | approved | not-started | chosen |
| [siren](siren/README.md) | Wall nest whose sonar ring slows the ship (L25) | approved | not-started | chosen |
| [spiral-nautilus](spiral-nautilus/README.md) | Armoured shell orbiting the ship, uncoils to fire a spiral (L25) | approved | not-started | chosen |
| [glow-angler](glow-angler/README.md) | Dark-water ambusher with a pickup-like lure that lunges and rings (L26) | approved | not-started | chosen |
| [depth-hunter](depth-hunter/README.md) | Unmarked grey human sub drone, shootable homing torpedoes from below, hint 3 (L27) | approved | not-started | chosen |

## Design

- Above water (Act 2), `sub` units are seen under the waves: hittable only by `anti-sub` and the
  Smart Bomb, per the [layer rules](../README.md#layer-rules) (its *Water* paragraph: a level's
  `water: true`, the current layer of a unit that surfaces). Surfaced units are ground targets.
- Underwater (Act 4), the play plane stays the `air` layer in code (a level or section sets `medium: underwater`);
  `sub` is the deep water *below* it (a rising Abyss Ray, the Maw), `ground` the sea floor and trench walls and
  `high-air` the ice ceiling. All weapons hit the play plane, but weapons without `anti-sub` do reduced damage
  (round 48 Q1 recommends x 0.7, the table drafted 50 %); enemy bullets and movement are slowed by the water. All
  numbers: [under water rules](../../world/europa/README.md#under-water-rules) (reworded by the system docs of
  the same phase). Stat blocks here give base speeds; the level's water factor applies on top.
- **Popcorn under water** (round 48 Q3 a): the act has its own `tiny` and `small` swimmers, the Glassfin and the
  Barbfin, so density and the Act 2+ floor are met without Skitters and Needlers.
- Every Act 4 draft is hand-written (no `data.yaml`: the loader would fly it); HP is set from the proposed
  reference DPS 96 at L22 rising 4 a level to 120 at L28, pending part A.

### Returning units under water

| Unit | Under water | Notes |
|---|---|---|
| [Driftjelly](driftjelly/README.md) | returns as a `field` drifting on the play plane (its spec says Europa); the surface/submerge swap is dropped (always in the water), the ring unchanged | tint and caustic pass only; ring x 0.7 |
| [Reef Spitter](reef-spitter/README.md) | sits on the sea floor (`ground`) instead of a kelp raft; its fans x 0.7 | the raft is replaced by a rock/kelp base (art tint) |
| [Lamprey](../air/lamprey/README.md) | reads naturally as a water unit: a homing eel that latches and drains | candidate for the swimming levels; no art change planned |
| [Scuttler](../ground/scuttler/README.md) | walks the sea floor at L24 (its stat block already says so) | tint only; a `ground` target |
| [Skimmer](skimmer/README.md) | stays above the ice (L22 open leads), not below | no change |

## Concept art

Concept [round 06](../../concept-rounds/round-06/README.md) — generator `tools/concept/enemies_r06.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/driftjelly-r06-a.png](concept/driftjelly-r06-a.png) | Driftjelly — jellyfish mine (olive bell, lime veins), surface and submerged, pulses a ring of shots (sheet) | superseded by r07 (waterline) |
| [concept/driftjelly-r06-a.gif](concept/driftjelly-r06-a.gif) | Driftjelly — jellyfish mine (olive bell, lime veins), surface and submerged, pulses a ring of shots (motion) | superseded by r07 (waterline) |
| [concept/reef-spitter-r06-a.png](concept/reef-spitter-r06-a.png) | Reef Spitter — barnacle gun at 32 headings on a bobbing kelp raft, 3-way fans (sheet) | chosen |
| [concept/reef-spitter-r06-a.gif](concept/reef-spitter-r06-a.gif) | Reef Spitter — barnacle gun at 32 headings on a bobbing kelp raft, 3-way fans (motion) | chosen |
| [concept/skimmer-r06-a.png](concept/skimmer-r06-a.png) | Skimmer — flying-fish skiff (rust, fan fins) weaving through ice floes with foam wakes (sheet) | chosen |
| [concept/skimmer-r06-a.gif](concept/skimmer-r06-a.gif) | Skimmer — flying-fish skiff (rust, fan fins) weaving through ice floes with foam wakes (motion) | chosen |
| [concept/spiral-nautilus-r06-a.png](concept/spiral-nautilus-r06-a.png) | Spiral Nautilus — rolling striped shell with a tentacle crown, orbiting pairs under Europa's ice (sheet) | chosen |
| [concept/spiral-nautilus-r06-a.gif](concept/spiral-nautilus-r06-a.gif) | Spiral Nautilus — rolling striped shell with a tentacle crown, orbiting pairs under Europa's ice (motion) | chosen |

Concept [round 07](../../concept-rounds/round-07/README.md) — real waterline (art-direction Water rules); generator `tools/concept/enemies_r07.py`.

| File | What | Status |
|---|---|---|
| [concept/driftjelly-r07-a.png](concept/driftjelly-r07-a.png) | Driftjelly r07 — bell cut at the water plane, lower bell and tentacles visible under water, broken foam collar, ripple trains; trigger radius now a labelled diagram (sheet) | chosen |
| [concept/driftjelly-r07-a.gif](concept/driftjelly-r07-a.gif) | Driftjelly r07 — surfaced and submerged jellies drifting, ripples left behind as they pulse | chosen |

Concept round 48 (Act 4 "Deep Water" design phase, 2026-10-21; the round's review board follows) — a/b
proposals for the Act 4 units, each a PNG sheet plus a GIF in the 480×540 play field under the
under-water grade of the unit's first level (Q2 a, pending the user's review); generator
`tools/concept/enemies_r48.py`. Prompts: [concept/prompts.md](concept/prompts.md#round-48--act-4-deep-water-units-under-europas-ice).

| File | What | Status |
|---|---|---|
| [concept/eel-swarm-r48-a.png](concept/eel-swarm-r48-a.png) | Eel Swarm A, ribbon eel — rust eels with a violet stripe, the lead eel's lime gills and jaws; stripe tell, chain, dark glow (sheet) | chosen — round 48 |
| [concept/eel-swarm-r48-a.gif](concept/eel-swarm-r48-a.gif) | Eel Swarm A — the chain over the lamp-lit L23 farms: aimed shots and a fan, a cut tail drifting off, the head kill unzipping it (motion) | chosen — round 48 |
| [concept/rejected/eel-swarm-r48-b.png](concept/rejected/eel-swarm-r48-b.png) | Eel Swarm B, lantern eel — teal-black plated eels with violet photophores, a bone skull and violet maw (sheet) | rejected — round 48: a picked |
| [concept/rejected/eel-swarm-r48-b.gif](concept/rejected/eel-swarm-r48-b.gif) | Eel Swarm B — the same scene as A (motion) | rejected — round 48: a picked |
| [concept/eel-swarm-final-r52-a.png](concept/eel-swarm-final-r52-a.png) | Eel Swarm final frames (`tools/art/eel_swarm.py`, round 48 a, the ribbon eel): the head at 16 headings (rest, tell, shot), body eel and tail (rest, stripe lit), the stripe's bloom and the muzzle (additive), the husks sinking; three screens over the farms (sheet) | proposed (round 52) |
| [concept/eel-swarm-final-r52-a.gif](concept/eel-swarm-final-r52-a.gif) | A 12-segment snake: the stripe tell, aimed shots and a fan, a cut and the tail scattering, the head kill unzipping it, the husks sinking | proposed (round 52) |
| [concept/eel-swarm-capture-r52-a.png](concept/eel-swarm-capture-r52-a.png) | Game capture, M8 part C (step C5b; Level 23 on medium, `--invulnerable`, the balance plan's Level 23 fit: Harpoon Torpedoes L3, Depth Charges L3, two Micro-Missile Pods, the Sonar Pulse, Rook with his gun): Level 23's second eel at t=60.2 (the stripe tell), 60.8 (the fan), 62.4 (cut, the tail scattering) and 64.5 (the head killed) | proposed (round 52) |
| [concept/eel-swarm-capture-r52-a.mp4](concept/eel-swarm-capture-r52-a.mp4) | The same eel as a video with the game's sound (t=57.5–66) | proposed (round 52) |
| [concept/rejected/abyss-ray-r48-a.png](concept/rejected/abyss-ray-r48-a.png) | Abyss Ray A, manta — teal-black wings with teal veins, lime gill slits, violet mouth; flap frames, the shadow-and-rise sequence (sheet) | rejected — round 48: b picked |
| [concept/rejected/abyss-ray-r48-a.gif](concept/rejected/abyss-ray-r48-a.gif) | Abyss Ray A — a shadow grows, the ray rises, four Barbfins peel off, three 9-way fans, it glides off and sinks; L24's lamps' edge (motion) | rejected — round 48: b picked |
| [concept/abyss-ray-r48-b.png](concept/abyss-ray-r48-b.png) | Abyss Ray B, bone skate — a bone disc with a tubercle ridge, violet spots, lime gill slits, whip tail (sheet) | chosen — round 48 |
| [concept/abyss-ray-r48-b.gif](concept/abyss-ray-r48-b.gif) | Abyss Ray B — the same scene as A (motion) | chosen — round 48 |
| [concept/rejected/siren-r48-a.png](concept/rejected/siren-r48-a.png) | Siren A, conch throat — a slate spiral shell in a cave mouth, teal throat sac; tell frames, both walls, the smooth sound ring, the slowed ship's shimmer (sheet) | rejected — round 48: b picked |
| [concept/rejected/siren-r48-a.gif](concept/rejected/siren-r48-a.gif) | Siren A — three nests in the dark L25 trench with red-gold beacons: one ring slows the ship 2 s, a later one passes during the 3 s immunity, a nest shot (motion) | rejected — round 48: b picked |
| [concept/siren-r48-b.png](concept/siren-r48-b.png) | Siren B, frill bell — a slate dome with five membrane frills round a teal sac; a rippled double-wavefront ring (sheet) | chosen — round 48 |
| [concept/siren-r48-b.gif](concept/siren-r48-b.gif) | Siren B — the same scene as A (motion) | chosen — round 48 |
| [concept/glow-angler-r48-a.png](concept/glow-angler-r48-a.png) | Glow Angler A, rust angler — toothed gape, crimson eyes, a teal lure on a bone stalk; next to a real salvage pickup in the black (sheet) | chosen — round 48 |
| [concept/glow-angler-r48-a.gif](concept/glow-angler-r48-a.gif) | Glow Angler A — L26 black water, the headlight cone only: lures and eyes in the dark, a lunge and a ring of 8, two shot, a real pickup collected (motion) | chosen — round 48 |
| [concept/rejected/glow-angler-r48-b.png](concept/rejected/glow-angler-r48-b.png) | Glow Angler B, teal-black gulper — pouch mouth, whip tail, the lure on a longer stalk (sheet) | rejected — round 48: a picked |
| [concept/rejected/glow-angler-r48-b.gif](concept/rejected/glow-angler-r48-b.gif) | Glow Angler B — the same scene as A (motion) | rejected — round 48: a picked |
| [concept/rejected/depth-hunter-r48-a.png](concept/rejected/depth-hunter-r48-a.png) | Depth Hunter A, pump-jet cigar — unmarked plain grey, ducted shroud, bow torpedo tubes, red sensor; next to the Ghost Drone and the torpedo (sheet) | rejected — round 48: b picked |
| [concept/rejected/depth-hunter-r48-a.gif](concept/rejected/depth-hunter-r48-a.gif) | Depth Hunter A — a column rises from the rear in the L27 vents, holds low, slow homing torpedoes up the screen; one torpedo and one hunter shot by the tail gun (motion) | rejected — round 48: b picked |
| [concept/depth-hunter-r48-b.png](concept/depth-hunter-r48-b.png) | Depth Hunter B, flat glider — unmarked grey lifting body, twin dorsal tubes, red sensor slit (sheet) | chosen — round 48 |
| [concept/depth-hunter-r48-b.gif](concept/depth-hunter-r48-b.gif) | Depth Hunter B — the same scene as A (motion) | chosen — round 48 |
| [concept/glassfin-r48-a.png](concept/glassfin-r48-a.png) | Glassfin A, needle fry — a 16 px clear glass needle with a teal spine; glint tell, dark, glass-shatter death (sheet) | chosen — round 48 |
| [concept/glassfin-r48-a.gif](concept/glassfin-r48-a.gif) | Glassfin A — two ribbons of 14 over the L23 farms, glint and ram, one hits the shield (motion) | chosen — round 48 |
| [concept/rejected/glassfin-r48-b.png](concept/rejected/glassfin-r48-b.png) | Glassfin B, leaf larva — a flat clear glass leaf with a teal spine (sheet) | rejected — round 48: a picked |
| [concept/rejected/glassfin-r48-b.gif](concept/rejected/glassfin-r48-b.gif) | Glassfin B — the same scene as A (motion) | rejected — round 48: a picked |
| [concept/glassfin-final-r52-a.png](concept/glassfin-final-r52-a.png) | Glassfin final frames (`tools/art/glassfin.py`, round 48 a, the needle fry): 16 headings swimming and glinting, the glint sparkle, the shatter's flash and shards (sheet) | proposed (round 52) |
| [concept/glassfin-final-r52-a.gif](concept/glassfin-final-r52-a.gif) | Two ribbons swaying over the farms, fry glinting and ramming down the ship's lane, shatters | proposed (round 52) |
| [concept/glassfin-capture-r52-a.png](concept/glassfin-capture-r52-a.png) | Game capture, M8 part C (step C5b; Level 23 on medium, `--invulnerable`, the balance plan's Level 23 fit: Harpoon Torpedoes L3, Depth Charges L3, two Micro-Missile Pods, the Sonar Pulse, Rook with his gun): Level 23's first school (t=29.2–32.2): the S-route down the shaft, the glints and the rams down the ship's lane, shatters | proposed (round 52) |
| [concept/glassfin-capture-r52-a.mp4](concept/glassfin-capture-r52-a.mp4) | The same school as a video with the game's sound (t=28–37) | proposed (round 52) |
| [concept/barbfin-r48-a.png](concept/barbfin-r48-a.png) | Barbfin A, bone lionfish — bone fin rays and webbing, a barbed violet-tipped snout; 32 headings, swim frames (sheet) | chosen — round 48 |
| [concept/barbfin-r48-a.gif](concept/barbfin-r48-a.gif) | Barbfin A — a column of four and a group of three dart in, hold, spit needles, two shot (ink cloud, sinking shell) on the L24 plain (motion) | chosen — round 48 |
| [concept/rejected/barbfin-r48-b.png](concept/rejected/barbfin-r48-b.png) | Barbfin B, plated box — bone armour plates with violet seams, barbed snout (sheet) | rejected — round 48: a picked |
| [concept/rejected/barbfin-r48-b.gif](concept/rejected/barbfin-r48-b.gif) | Barbfin B — the same scene as A (motion) | rejected — round 48: a picked |

## Implementation

- [x] Each enemy promoted to its own directory with a stat block before it is implemented (every unit of Acts 1–2; the rule stands for the units of Acts 3–7, M6 part D)
- [x] Surfacing/submerging transition visuals (top-down, the generic `sub` pass and per-unit
      surfacing steps, user decision E4 = c) — M5 part E (the Driftjelly and the Harbour Kraken,
      Level 11)
- [ ] The Act 4 swimmers (Glassfin, Eel Swarm, Barbfin, Abyss Ray, Siren, Spiral Nautilus, Glow Angler, Depth Hunter) built from their drafts, each ticking its own README's checklist (M8)

## Open questions

- Does the Glow Angler's fake pickup risk frustrating players? Proposal: its lure glows a
  slightly different colour than real pickups, and Varga warns about it on its first appearance.

## Decisions

- 2026-09-30: Roster of 8 naval enemies drafted.
- 2026-09-30: Converted to the 960×540 baseline (was 640×360).
- 2026-09-30: Enemy variety pass: size tier column added to every row; new units Spiral Nautilus (roster fork addition).
- 2026-10-01: Concept round 06: Reef Spitter, Skimmer and Spiral Nautilus chosen; Driftjelly chosen but its waterline ring must become real foam/ripples (round 07).
- 2026-10-01: Concept round 07: Driftjelly waterline fixed; r07 chosen.
- 2026-10-01: Acts 1–2 units promoted to full specs: Driftjelly, Reef Spitter, Skimmer.
- 2026-10-01: Above-water `sub` targets are hit by `anti-sub` only (`area` removed), matching the layer rules in the enemies README.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-08: M5 part E (user decisions E2 = a and E4 = c of 2026-10-08): above water the `sub`
  layer is reached by `anti-sub` and the Smart Bomb only; the surfacing visuals are drawn by a generic
  `sub` pass with per-unit pre-rendered collars and surfacing steps
  ([art direction](../../art-direction/README.md#animation-rules)); the Driftjelly's and Reef
  Spitter's specs reworked for Level 11.
- 2026-10-12: M5 part I (close-out): the surfacing and submerging visuals ticked as built in part E (the `sub` pass and the units' surfacing steps, approved in round 33).
- 2026-10-20 (user, round 44, A.b (a): production assets (a `Source` chunk in `assets/`, every Concept art row decided)): **`art: final`** for this part. A later addition (a new sprite, a new concept round) is its own row and does not reopen the rest.
- 2026-10-21 (Act 4 design phase, step m8-denemies): the six roster units plus two new popcorn swimmers (Glassfin `tiny`, Barbfin `small`, round 48 Q3 a, pending the user's review) promoted to draft specs; the roster is empty. The Glow Angler's lure follows round 48 Q6 a (a near-miss lure, which answers the open question above). Returning units' under-water notes added.
