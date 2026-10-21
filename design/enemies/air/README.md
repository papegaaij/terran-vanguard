---
title: Air enemies
design: approved
implementation: in-progress
art: final
updated: 2026-10-20
---

# Air enemies

## Summary

Flying enemies on the `air` layer (the player's plane), plus `low-air` flyers below it and
`high-air` units passing above. Air enemies also appear in space levels; vacuum-only enemies are
in [space](../space/README.md). This is the largest category and carries most waves in most
levels.

## Contents

Promoted to their own documents for the Acts 1–2 wrap-up, plus the Act 3 drafts of the overnight design phase (2026-10-12); the remaining units stay in the roster below.

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [skitter](skitter/README.md) | Tiny swarm fodder that rams; teaches shooting (L01) | approved | done | final |
| [needler](needler/README.md) | Basic gunner, hovers and fires aimed thorns (L01) | approved | done | final |
| [stinger](stinger/README.md) | Diver that fires a fan at the bottom of its dive (L02) | approved | done | final |
| [spore-bomber](spore-bomber/README.md) | Low-air bomber dropping rising spore mines (L03) | approved | done | final |
| [whirl-seed](whirl-seed/README.md) | Tiny radial spinner released in clusters (L03) | approved | done | final |
| [brood-pod](brood-pod/README.md) | Egg sac that bursts into Skitters when killed or after 8 s (L04) | approved | done | final |
| [mantis](mantis/README.md) | Side-entering laser sweeper (L06) | approved | done | final |
| [coilwyrm](coilwyrm/README.md) | Segment-chain serpent that loops to the rear and regrows a head once (L06) | approved | done | final |
| [wraith](wraith/README.md) | Cloaked manta that decloaks behind the player (L10) | approved | done | final |
| [mote-swarm](mote-swarm/README.md) | Flock of tiny motes that loops round for a rear attack (L10) | approved | in-progress | final |
| [lamprey](lamprey/README.md) | Homing eel that latches and drains the shield (L12) | approved | done | final |
| [choir-herald](choir-herald/README.md) | Support flyer whose aura rings shield nearby enemies (L17) | approved | done | final |
| [ghost-drone](ghost-drone/README.md) | Grey human-built strafing drone, hint 1 (L18) | approved | in-progress | final |
| [dust-kite](dust-kite/README.md) | Tiny Martian popcorn glider that rams (L15, M10 = b) | approved | in-progress | final |

## Roster

| Name | Faction | Layer | Tier | Role/ability | Formations | First level | Design |
|---|---|---|---|---|---|---|---|
| Talon | Ascendancy | air | small | Fast interceptor. `swoop` with paired `aimed` shots; small hex shield (2 HP). | V-wing, line abreast | 29 | idea |
| Gilded Gunship | Ascendancy | air | medium | Heavy and slow. Armoured front (50% damage reduction) with a `ring` burst every 3 s; exposed engines at the rear take ×2. | column, carrier + escorts | 30 | idea |
| Hornet | Ascendancy | air | medium | Missile boat. `hover` at the top and launch 4 shootable `homing` missiles. | line abreast | 31 | idea |
| Chimera | Hybrid | air | small | Talon airframe with Vrell tissue: **regenerates** 20% HP/s after 1 s without taking damage. Rewards burst damage. | V-wing, swarm | 36 | idea |
| Mirror Interceptor | Ascendancy | air | medium | Frontal `reflect` shield bounces non-`beam` shots back. Vulnerable from the sides and rear, or to `beam`/`shield-breaker`. | pincer, rear ambush | 37 | idea |
| Harrow | Hybrid | air | small | `kamikaze` with a Vrell biomass payload; `death-burst` of 8 bullets whether shot or impacting. Kill it at range. | stream, cross | 38 | idea |
| Choir Seraph | Vrell (elite) | air | medium | Elite. `teleport` next to the player's flank and `mirror` its movement while firing a `spiral`. 40 HP. | circle (pairs) | 47 | idea |
| Buzzsaw Drone | Ascendancy | air / space | small | Radially symmetric spinning blade drone (4 blades, `radial`). `chase`s the player, then `ricochet`s off edges and asteroids at speed; contact damage only, but lethal in groups. Blades spin faster just before a dash (telegraph). | stream, circle | 31 | idea |
| Rail Serpent | Ascendancy | air / space | large | Armoured drone train of 8–14 cars (`chain`) that snakes across the screen (`swirl`, `cross`) and **enters from the sides or the rear**. Every third car carries a turret firing `aimed` shots; the engine car is `vital`. Destroying a car splits the train: the rear half stops and explodes. Orientation 32 angles per car. | snake, pincer (two trains) | 30 | idea |

## Design

- Air enemies make up most of every wave table. Every level mixes at least one fodder type
  (Skitter / Talon / Chimera) with one "problem" enemy that needs attention.
- `low-air` and `high-air` members follow the [layer rules](../README.md): they don't collide
  with the player, `high-air` is only hit by `homing` and `beam`, and their bullets always
  travel on the player plane.
- Sizes follow the [size tiers](../README.md#size-tiers): fodder `tiny`/`small` (24–30 px),
  gunners `small` (36 px), heavies `medium` (60–84 px), chains `large`.
- Not all air enemies fly nose-down: chains (Coilwyrm, Rail Serpent) and the Mote Swarm turn
  freely at 16/32 angles, and spinners (Whirl Seed, Buzzsaw Drone) are `radial`. See
  [orientation](../README.md#orientation-and-rotation).
- The first rear specialists come with Level 10 (M5 part D): the [Wraith](wraith/README.md)
  passes overhead cloaked on `high-air` and decloaks behind the player (`rear ambush`), the
  [Mote Swarm](mote-swarm/README.md) is a boids flock (`swarm`) that loops back from below; both
  re-enter at the bottom edge without an edge warning (a unit's own flight, 2026-10-21).

## Concept art

Concept [round 03](../../concept-rounds/round-03/README.md). Two Vrell design languages are proposed: **A "Sleek chitin"** (smooth, elongated, glossy violet chitin with thin glowing teal seams, pink eye as weak point) and **B "Armoured brood"** (bulky segmented carapace plates, claws and spikes, glow only between plates and in eye clusters). The key Act 1 enemies are shown in both; the others in A. Each sheet: source render, native sprite with palette, animation hint, in-game view with formation, shadows, bullets and the player for scale. Prompts: [concept/prompts.md](concept/prompts.md); generator `tools/concept/enemies_r03.py`.

| File | What | Status |
|---|---|---|
| [concept/skitter-r03-a.png](concept/skitter-r03-a.png) | Skitter, language A: needle dart with scythe wings; snake formation over a station hull | superseded by the r04 colour pass |
| [concept/rejected/skitter-r03-b.png](concept/rejected/skitter-r03-b.png) | Skitter, language B: armoured beetle with split glowing elytra | rejected — other variant preferred |
| [concept/rejected/needler-r03-a.png](concept/rejected/needler-r03-a.png) | Needler, language A: slim body with thorn proboscis; V formation firing aimed thorns | rejected — other variant preferred |
| [concept/needler-r03-b.png](concept/needler-r03-b.png) | Needler, language B: crab with two claws around a thorn launcher | superseded by the r04 colour pass |
| [concept/stinger-r03-a.png](concept/stinger-r03-a.png) | Stinger (A): wasp-like diver, 3-way fan at the bottom of its dive | superseded by the r04 colour pass |
| [concept/spore-bomber-r03-a.png](concept/spore-bomber-r03-a.png) | Spore Bomber (A): gas-bag blimp on `low-air` dropping spore mines, over Earth from orbit | superseded by the r04 colour pass |
| [concept/brood-pod-r03-a.png](concept/brood-pod-r03-a.png) | Brood Pod (A): pulsing egg sac bursting into six Skitters, over lunar regolith | superseded by the r04 colour pass |
| [concept/mantis-r03-a.png](concept/mantis-r03-a.png) | Mantis (A): side-holding laser sweeper with raptorial arms | superseded by the r04 colour pass |
| [concept/talon-r03-a.png](concept/talon-r03-a.png) | Talon (Ascendancy fighter): forward-swept black-and-gold stealth interceptor with paired aimed shots, asteroid belt | superseded by the r04 colour pass |
| [concept/gilded-gunship-r03-a.png](concept/gilded-gunship-r03-a.png) | Gilded Gunship (Ascendancy gunship): gold front armour, ring burst, exposed rear engines | superseded by the r04 colour pass |

Concept [round 04](../../concept-rounds/round-04/README.md) — colour pass on the chosen enemies with the [role colours](../../README.md#role-colours) (chitin base = role family, glow = kind of threat; Ascendancy black & gold with a per-unit accent and a thin gold/red rim light). Same models and sheet layout; generator `tools/concept/enemies_r04.py`.

| File | What | Status |
|---|---|---|
| [concept/skitter-r04-a.png](concept/skitter-r04-a.png) | Skitter — plum chitin, teal glow (fodder, contact) | chosen |
| [concept/needler-r04-a.png](concept/needler-r04-a.png) | Needler (language B crab) — bone/ivory carapace, violet glow (gunner, aimed shots); yellow needles | chosen |
| [concept/stinger-r04-a.png](concept/stinger-r04-a.png) | Stinger — rust chitin, crimson glow (diver) | chosen |
| [concept/spore-bomber-r04-a.png](concept/spore-bomber-r04-a.png) | Spore Bomber — olive chitin, lime glow and spore bulbs (area denial) | chosen |
| [concept/brood-pod-r04-a.png](concept/brood-pod-r04-a.png) | Brood Pod — teal-black sac, teal veins (spawner); bursts into plum Skitters | chosen |
| [concept/mantis-r04-a.png](concept/mantis-r04-a.png) | Mantis — bone/ivory chitin, crimson glow (laser sweeper) | chosen |
| [concept/talon-r04-a.png](concept/talon-r04-a.png) | Talon — black & gold with red accent and gold/red rim light | chosen |
| [concept/gilded-gunship-r04-a.png](concept/gilded-gunship-r04-a.png) | Gilded Gunship — black & gold with white accent and rim light | chosen |

Concept [round 05](../../concept-rounds/round-05/README.md) — new archetypes, each a PNG sheet plus a GIF in the 480×540 play field; generator `tools/concept/enemies_r05.py`.

| File | What | Status |
|---|---|---|
| [concept/coilwyrm-r05-a.png](concept/coilwyrm-r05-a.png) | Coilwyrm — Vrell serpent (rust/teal): 58 px head + 12 overlapping segments + tail, ~330 px, each part at 16 headings following the head's looping swirl (sheet) | superseded by r08 (model-space re-render) |
| [concept/coilwyrm-r05-a.gif](concept/coilwyrm-r05-a.gif) | Coilwyrm — Vrell serpent (rust/teal): 58 px head + 12 overlapping segments + tail, ~330 px, each part at 16 headings following the head's looping swirl (motion) | superseded by r08 (model-space re-render) |
| [concept/whirl-seed-r05-a.png](concept/whirl-seed-r05-a.png) | Whirl Seed — tiny 26 px radial seed pod (plum/teal), spinning clusters of five that spiral and bounce (sheet) | chosen |
| [concept/whirl-seed-r05-a.gif](concept/whirl-seed-r05-a.gif) | Whirl Seed — tiny 26 px radial seed pod (plum/teal), spinning clusters of five that spiral and bounce (motion) | chosen |
| [concept/mote-swarm-r05-a.png](concept/mote-swarm-r05-a.png) | Mote Swarm — 30 tiny ember motes (rust/crimson) flocking, exiting and returning from behind the player after a REAR! edge warning (sheet) | chosen |
| [concept/mote-swarm-r05-a.gif](concept/mote-swarm-r05-a.gif) | Mote Swarm — 30 tiny ember motes (rust/crimson) flocking, exiting and returning from behind the player after a REAR! edge warning (motion) | chosen |
| [concept/buzzsaw-drone-r05-a.png](concept/buzzsaw-drone-r05-a.png) | Buzzsaw Drone — Ascendancy six-blade spinning drone (gunmetal/gold, rim light), ricochets off the edges (sheet) | chosen |
| [concept/buzzsaw-drone-r05-a.gif](concept/buzzsaw-drone-r05-a.gif) | Buzzsaw Drone — Ascendancy six-blade spinning drone (gunmetal/gold, rim light), ricochets off the edges (motion) | chosen |
| [concept/rail-serpent-r05-a.png](concept/rail-serpent-r05-a.png) | Rail Serpent — Ascendancy drone train (red accent): head car + 8 cars at 16 headings on a winding route (sheet) | chosen |
| [concept/rail-serpent-r05-a.gif](concept/rail-serpent-r05-a.gif) | Rail Serpent — Ascendancy drone train (red accent): head car + 8 cars at 16 headings on a winding route (motion) | chosen |

Concept [round 06](../../concept-rounds/round-06/README.md) — generator `tools/concept/enemies_r06.py`.

| File | What | Status |
|---|---|---|
| [concept/wraith-r06-a.png](concept/wraith-r06-a.png) | Wraith — Vrell manta ghost (rust membrane, blue-violet veins), cloaked shimmer, loops behind the player and decloaks for a rear attack (sheet) | chosen |
| [concept/wraith-r06-a.gif](concept/wraith-r06-a.gif) | Wraith — Vrell manta ghost (rust membrane, blue-violet veins), cloaked shimmer, loops behind the player and decloaks for a rear attack (motion) | chosen |
| [concept/lamprey-r06-a.png](concept/lamprey-r06-a.png) | Lamprey — Vrell eel (rust, teal mouth disc): homing stream, latches on and drains shield until shaken off (sheet) | chosen |
| [concept/lamprey-r06-a.gif](concept/lamprey-r06-a.gif) | Lamprey — Vrell eel (rust, teal mouth disc): homing stream, latches on and drains shield until shaken off (motion) | chosen |

Concept [round 08](../../concept-rounds/round-08/README.md) — model-space re-render (seams and patterns now turn with the body), design unchanged; generator `tools/concept/rerender_r08.py`.

| File | What | Status |
|---|---|---|
| [concept/coilwyrm-r08-a.png](concept/coilwyrm-r08-a.png) | Coilwyrm re-render (sheet) | chosen |
| [concept/coilwyrm-r08-a.gif](concept/coilwyrm-r08-a.gif) | Coilwyrm re-render (motion) | chosen |

Concept round 36 (Act 3 "Red Dust", overnight design phase 2026-10-12; the round's review board
follows) — each a PNG sheet plus a GIF in the 480×540 play field over Mars; generator
`tools/concept/enemies_r36.py`.

| File | What | Status |
|---|---|---|
| [concept/rejected/choir-herald-r36-a.png](concept/rejected/choir-herald-r36-a.png) | Choir Herald A, cantor bell — teal-black bell with a sound-wave aura: source, wing beat, headings, escort ring states (sheet) | rejected — round 36: b (seraph) picked |
| [concept/rejected/choir-herald-r36-a.gif](concept/rejected/choir-herald-r36-a.gif) | Choir Herald A — with six Needler escorts in a circle; one escort shot through its ring, then the Herald dies and the rings fade (motion) | rejected — round 36: b (seraph) picked |
| [concept/choir-herald-r36-b.png](concept/choir-herald-r36-b.png) | Choir Herald B, seraph — bone seraph with a floating halo crest and segmented violet rings (sheet) | chosen (round 36, U.3) |
| [concept/choir-herald-r36-b.gif](concept/choir-herald-r36-b.gif) | Choir Herald B — the same scene as A (motion) | chosen (round 36, U.3) |
| [concept/choir-herald-final-r40-a.png](concept/choir-herald-final-r40-a.png) | Choir Herald final sprites (`tools/art/siege_units.py`, the chosen seraph b): wing beat, crest bloom, segmented ring 0-6, absorb flash, thread, aura circle (sheet) | chosen (round 40, F.b: approved as final) |
| [concept/choir-herald-final-r40-a.gif](concept/choir-herald-final-r40-a.gif) | Choir Herald final: four ringed Needlers, one ring draining, the Herald dies and the rings collapse, 10 fps | chosen (round 40, F.b: approved as final) |
| [concept/ghost-drone-r36-a.png](concept/ghost-drone-r36-a.png) | Ghost Drone A, facet dart — grey human-built drone with the U.5 fix (2×2 px red eye, white wing-tip running lights, additive glow), 16 headings, next to a Talon and the player (sheet) | chosen (round 36, U.4; re-rendered with U.5) |
| [concept/ghost-drone-r36-a.gif](concept/ghost-drone-r36-a.gif) | Ghost Drone A with the U.5 eye and running lights — line abreast, then a pincer, over dusty Mars plains (motion) | chosen (round 36, U.4; re-rendered with U.5) |
| [concept/rejected/ghost-drone-r36-b.png](concept/rejected/ghost-drone-r36-b.png) | Ghost Drone B, crescent — grey drone with a forward-swept wing (sheet) | rejected — round 36: a (facet dart) picked |
| [concept/rejected/ghost-drone-r36-b.gif](concept/rejected/ghost-drone-r36-b.gif) | Ghost Drone B — the same scene as A (motion) | rejected — round 36: a (facet dart) picked |
| [concept/rejected/dust-kite-r36-a.png](concept/rejected/dust-kite-r36-a.png) | Dust Kite A, diamond kite — rust membrane on a bone cross strut with a teal spine: source, 16 headings, membrane flex, entry shimmer, death puff, next to a Skitter and the player (sheet) | rejected — round 36: b (crescent sail) picked |
| [concept/rejected/dust-kite-r36-a.gif](concept/rejected/dust-kite-r36-a.gif) | Dust Kite A — a stream, a V-wing of 7 and a loose swarm over Mars; most shot down, a few ram the ship (motion) | rejected — round 36: b (crescent sail) picked |
| [concept/dust-kite-r36-b.png](concept/dust-kite-r36-b.png) | Dust Kite B, crescent sail — bat-like swept rust sail on bone finger struts, teal-glowing spine nodes (sheet) | chosen (round 36, U.7) |
| [concept/dust-kite-r36-b.gif](concept/dust-kite-r36-b.gif) | Dust Kite B — the same scene as A (motion) | chosen (round 36, U.7) |
| [concept/dust-kite-final-r38-a.png](concept/dust-kite-final-r38-a.png) | Dust Kite final sprites (`tools/art/dust_kite.py`): 16 headings × 4 membrane-flex frames, death spark and dust puff (sheet) | chosen (round 38, F.4: approved as final) |
| [concept/dust-kite-final-r38-a.gif](concept/dust-kite-final-r38-a.gif) | Dust Kite final: stream, V-wing and swarm from the production frames (motion) | chosen (round 38, F.4: approved as final) |
| [concept/ghost-drone-final-r41-a.png](concept/ghost-drone-final-r41-a.png) | Ghost Drone final sprites (`tools/art/storm_units.py`, the chosen facet dart a with the U.5 lamps): 16 headings, additive eye/tip glow, EMP lights-out, the glint tell, sparks and smoke death, in / out of the radius (sheet) | chosen (round 41) |
| [concept/ghost-drone-final-r41-a.gif](concept/ghost-drone-final-r41-a.gif) | Ghost Drone final: a line abreast and a pincer strafing with their glint-told bursts over the storm plain, look b R 170, 15 fps | chosen (round 41) |

## Implementation

- [x] Each enemy promoted to its own directory with a stat block before it is implemented (every unit of Acts 1–2; the rule stands for the units of Acts 3–7, M6 part D)

## Open questions

- None open.

## Decisions

- 2026-09-30: Roster of 17 air enemies drafted.
- 2026-09-30: Converted to the 960×540 baseline (was 640×360).
- 2026-09-30: Concept round 03: first enemy sheets — Act 1 air enemies (Skitter and Needler in both Vrell design languages) plus Talon and Gilded Gunship for faction contrast.
- 2026-09-30: Concept round 04: colour pass on air enemies with the role colours (r04 proposals).
- 2026-09-30: Enemy variety pass: size tier column added to every row; new units Whirl Seed, Coilwyrm, Mote Swarm, Buzzsaw Drone, Rail Serpent.
- 2026-10-01: Concept round 06: Wraith and Lamprey chosen.
- 2026-10-01: Lamprey latch drains the shield only; it does not slow the ship. A cut Coilwyrm regrows a head once per chain.
- 2026-10-01: Acts 1–2 units promoted to full specs: Skitter, Needler, Stinger, Spore Bomber, Whirl Seed, Brood Pod, Mantis, Coilwyrm, Wraith, Mote Swarm, Lamprey.
- 2026-10-01: Concept round 08: model-space re-renders accepted; they supersede the r05 sheets.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-08: M5 part D (user decisions of 2026-10-08): the Wraith (D6 = a, its stat block flown;
  D7 = a, HP 16) and the Mote Swarm (D8 = a, real flocking) get their data files and go to `review`
  for round 32; their art goes straight to production (D10 = a). The Skitter's authored-paths item
  is narrowed to snakes and swarms (D8 = a: the route as data), streams keeping their shapes.
- 2026-10-12 (overnight design phase): Act 3 units promoted to draft specs (Choir Herald, Ghost
  Drone) and the new Dust Kite (M10 = b, Act 3's popcorn; name pending); concept round 36 proposes
  the Choir Herald, Ghost Drone and Dust Kite a/b, accepted in round 36 (2026-10-13).
- 2026-10-13 (user, round 36): the Act 3 air units approved (D.15–D.17), the Dust Kite's name included. Picks: Choir Herald **b** (seraph), Ghost Drone **a** (facet dart, with the eye and running lights fix U.5 for the art step), Dust Kite **b** (crescent sail). The rejected variants moved to `concept/rejected/`.
- 2026-10-20 (user, round 44, A.b (a): production assets (a `Source` chunk in `assets/`, every Concept art row decided)): **`art: final`** for this part. A later addition (a new sprite, a new concept round) is its own row and does not reopen the rest.
