---
title: Air enemies
design: approved
implementation: in-progress
art: final
updated: 2026-10-31
---

# Air enemies

## Summary

Flying enemies on the `air` layer (the player's plane), plus `low-air` flyers below it and
`high-air` units passing above. Air enemies also appear in space levels; vacuum-only enemies are
in [space](../space/README.md). This is the largest category and carries most waves in most
levels.

## Contents

Promoted to their own documents for the Acts 1–2 wrap-up, plus the Act 3 drafts of the overnight design phase (2026-10-12), the Act 5 drafts of the design phase (2026-10-27, decided in round 60), and the Act 6 drafts of its design phase (2026-10-31, decided 2026-10-31 (round 72)); the remaining units stay in the roster below.

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
| [talon](talon/README.md) | Ascendancy interceptor: swoops, aimed pairs, a 2 HP hex shield, squad leaders (L29) | approved | done | final |
| [wavering-stormhawk](wavering-stormhawk/README.md) | Ceres Wing Stormhawk that holds fire: a neutral craft, Level 29's "Hold fire" (L29) | approved | done | final |
| [dart-drone](dart-drone/README.md) | Tiny Ascendancy ram drone launched in streams by Gunships, stations and the Sovereign (L30) | approved | done | final |
| [gilded-gunship](gilded-gunship/README.md) | Slow gunship: gilded front ×0.5, engines ×2, ring bursts, Dart streams (L30) | approved | done | final |
| [rail-serpent](rail-serpent/README.md) | Drone train of 8–14 cars from the sides and rear; a cut's rear half drifts and blows (L30) | approved | done | final |
| [hornet](hornet/README.md) | Hovering missile boat, salvos of 4 shootable homing diamonds (L31) | approved | done | final |
| [buzzsaw-drone](buzzsaw-drone/README.md) | Radial blade drone: chase, 0.6 s spin-up, a dash that ricochets off edges and rocks (L31) | review | done | final |
| [chimera](chimera/README.md) | Hybrid on the Talon airframe: swoops and aimed pairs, regenerates 20 %/s after 1 s untouched (L36) | approved | not-started | chosen |
| [mirror-interceptor](mirror-interceptor/README.md) | Ascendancy interceptor with a frontal mirror: bounces shots, a beam breaks it, `shield-breaker` shatters it; open flanks (L37) | approved | not-started | chosen |
| [harrow](harrow/README.md) | Hybrid kamikaze from every edge: lock tell, ram, a ring of 8 on any death (L38) | approved | not-started | chosen |

## Roster

| Name | Faction | Layer | Tier | Role/ability | Formations | First level | Design |
|---|---|---|---|---|---|---|---|
| Choir Seraph | Vrell (elite) | air | medium | Elite. `teleport` next to the player's flank and `mirror` its movement while firing a `spiral`. 40 HP. | circle (pairs) | 47 | idea |

## Design

- Air enemies make up most of every wave table. Every level mixes at least one fodder type
  (Skitter / Talon / Chimera; in Act 5 the [Dart Drone](dart-drone/README.md)) with one "problem"
  enemy that needs attention.
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

Production art, M9 part B (concept round 62, step ART-B): the Talon re-rendered from round 60's chosen A.1 b in the Q.11 b look (gunmetal hull, gold panels, a 1 px gold rim); generator `tools/art/belt_units.py talon`, the frames held in `build/art-held/` until the Talon's data claims them. Prompts: [concept/prompts.md](concept/prompts.md#round-62--production-the-talon-m9-part-b).

| File | What | Status |
|---|---|---|
| [concept/talon-final-r62-a.png](concept/talon-final-r62-a.png) | Talon final frames (`tools/art/belt_units.py`): 16 headings, the squad leader's 16 with the gold spine stripe, the twin muzzles' red glint, the spinning wing of the death, the units on Level 29's canyon with the hex shield (sheet); gold share 42–50 % | chosen (round 62, F.1: accepted as final); the user finds the Talons die easily and wants a stronger shield (→ part B, in the data) |
| [concept/talon-final-r62-a.gif](concept/talon-final-r62-a.gif) | A V-wing swooping with its glint-told pairs and the hex shields, the leader shot and the wing scattering; then a line abreast | chosen (round 62, F.1: accepted as final); the user finds the Talons die easily and wants a stronger shield (→ part B, in the data) |
| [concept/talon-capture-r62-a.png](concept/talon-capture-r62-a.png) | Game capture, Level 29 (step B5): a Talon line's hex shields flashing under the beam; a V-wing's leader shot and the wing scattering | chosen (round 62, F.1: accepted as final); the user finds the Talons die easily and wants a stronger shield (→ part B, in the data) |
| [concept/talon-capture-r62-a.mp4](concept/talon-capture-r62-a.mp4) | Game capture with the game's sound: Level 29 ≈ 11–27 s, the first Talons, the first led V-wing, the Mite rocks from the left | chosen (round 62, F.1: accepted as final); the user finds the Talons die easily and wants a stronger shield (→ part B, in the data) |

Production art, M9 part C (concept round 63, step ART-C): the Gilded Gunship (round 60 A.3 b), the Dart Drone (A.4 b) and the Rail Serpent (A.5 b) re-rendered in the Q.11 b look (gunmetal and charcoal hulls with gold, a 1 px gold/red rim), the gold kept to about 20–30 % (round 62 flagged the Talon's 42–50 % as heavy): the Gunship keeps b's gold scale front third with its pods gunmetal (28 %), the Serpent b's gold roof on the inner bands beside the black centre stripe with gold coupler links (23–32 %), the Dart's gold leading edges a little narrower (20–26 %). Generator `tools/art/belt_units_c.py gunship dart serpent`; briefs in [concept/prompts.md](concept/prompts.md#round-63--production-the-gilded-gunship-the-dart-drone-and-the-rail-serpent-m9-part-c).

| File | What | Status |
|---|---|---|
| [concept/gilded-gunship-final-r63-a.png](concept/gilded-gunship-final-r63-a.png) | Gilded Gunship final frames (`tools/art/belt_units_c.py`): facing down (fixed) at the four lamp steps of the ring's tell and the four bay steps of the Dart launch, the engines' additive glow, the plate's glancing spark, the bay flash, the death's two halves and a spinning gold plate; on Level 30's space with the hex shield (sheet) | chosen (round 63, F.2-F.4: accepted as final) |
| [concept/gilded-gunship-final-r63-a.gif](concept/gilded-gunship-final-r63-a.gif) | The Gunship descending with its sway under the hex shield: the gold lamps telling each ring of 12, the bays opening for a stream of three Darts, front shots glancing off the plate, then broken in two behind the plate | chosen (round 63, F.2-F.4: accepted as final) |
| [concept/dart-drone-final-r63-a.png](concept/dart-drone-final-r63-a.png) | Dart Drone final frames (`tools/art/belt_units_c.py`): the chevron dart at 16 headings; beside the Lancer and a Talon at 1× (sheet) | chosen (round 63, F.2-F.4: accepted as final) |
| [concept/dart-drone-final-r63-a.gif](concept/dart-drone-final-r63-a.gif) | Two streams of three Darts: straight out of the bay, a lazy 60°/s chase, then straight on; two shot down | chosen (round 63, F.2-F.4: accepted as final) |
| [concept/rail-serpent-final-r63-a.png](concept/rail-serpent-final-r63-a.png) | Rail Serpent final frames (`tools/art/belt_units_c.py`): the engine car, the car, the turret car (lamp dark and the red tell) and the tail at 32 headings, the cut-off cars' wrecks (lights dead, scorched), the dead cars' sparks and the coupler's snap (sheet) | chosen (round 63, F.2-F.4: accepted as final) |
| [concept/rail-serpent-final-r63-a.gif](concept/rail-serpent-final-r63-a.gif) | An 11-car train snaking in from the left, its turret cars telling red and firing; a car shot out: the rear cars drift on slowing and sparking, then pop from the cut back while the front runs on | chosen (round 63, F.2-F.4: accepted as final) |
| [concept/gilded-gunship-capture-r63-a.png](concept/gilded-gunship-capture-r63-a.png) | Game capture, Level 30 (step C5): a Gunship entering, its lamps lit and a Dart out of the bays, the ring of 12, the beam on the front plate, broken in two | chosen (round 63, F.2-F.4: accepted as final) |
| [concept/rail-serpent-capture-r63-a.png](concept/rail-serpent-capture-r63-a.png) | Game capture, Level 30 (step C5): the t=62 train snaking in, the beam reaching it, the cut, the rear cars drifting dead, then blowing car by car | chosen (round 63, F.2-F.4: accepted as final) |
| [concept/rail-serpent-capture-r63-a.mp4](concept/rail-serpent-capture-r63-a.mp4) | Game capture with the game's sound: Level 30 ≈ 59–73 s, the right-edge warning, the train's rumble and turret shots, the split | chosen (round 63, F.2-F.4: accepted as final) |
| [concept/hornet-final-r64-a.png](concept/hornet-final-r64-a.png) | Hornet final frames (`tools/art/belt_units_d.py`): round 60's A.2 b delta hover-boat in the Q.11 b gunmetal (gold leading edges, about 27 %), facing down (fixed) at the four hatch steps of the 0.5 s tell × four lift-fan rotor steps, the hatches' additive gold bloom, the launch flash at a wing-root cell; on Level 31's space beside the Lancer, a Talon and a homing diamond, under part A's hex shield (sheet) | chosen (round 64, F.4: accepted as final) |
| [concept/hornet-final-r64-a.gif](concept/hornet-final-r64-a.gif) | A Hornet hovering in under its hex shield, the hatches glowing gold, then a salvo of four homing diamonds, the cells alternating | chosen (round 64, F.4: accepted as final) |
| [concept/buzzsaw-drone-final-r64-a.png](concept/buzzsaw-drone-final-r64-a.png) | Buzzsaw Drone final frames (`tools/art/belt_units_d.py`): round 60's A.6 b in gunmetal, the gold kept to the blades' outer cutting edges (about 33 %; the picked all-gold blades read 47 %); 8 spin frames over the 60° loop, the red-flickered spin-up tell, the spun-up blur, the ricochet sparks, a flung blade (sheet) | chosen (round 64, F.6 a: the gold on the cutting edges, accepted as final) |
| [concept/buzzsaw-drone-final-r64-a.gif](concept/buzzsaw-drone-final-r64-a.gif) | A Buzzsaw chasing, its 0.6 s spin-up tell flickering red, then the straight dash ricocheting off the edges and a big rock, sparks at each bounce | chosen (round 64, F.6 a: the gold on the cutting edges, accepted as final) |
| [concept/hornet-capture-r64-a.png](concept/hornet-capture-r64-a.png) | Game capture, Level 31 (step D5, the plan's fit, t≈43–44; [recipe](concept/prompts.md#hornet-capture-r64-a)): the t=40.5 pair under their shields, their salvos curving onto the tug's bow, the bow's hit flash | chosen (round 64, F.4: accepted as final) |
| [concept/buzzsaw-drone-capture-r64-a.png](concept/buzzsaw-drone-capture-r64-a.png) | Game capture, Level 31 (not firing, t≈33–34, 2×): the t=30.5 stream chasing beside the tug's left tank (a dash and a rock ricochet not caught clearly at game size) | chosen (round 64, F.6 a: the gold on the cutting edges, accepted as final) |

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
- 2026-10-27 (step m9-d-enemies-a, Act 5 design phase, approved in round 60): the
  Act 5 air units promoted to draft specs: Talon, Gilded Gunship, Hornet, Buzzsaw Drone and Rail
  Serpent from the roster, plus the new Dart Drone (the gap analysis's Q6 b, the Ascendancy's
  popcorn). Their Ascendancy hex shields follow the shared [enemy shields](../README.md#enemy-shields)
  rule (Q1 a); the Rail Serpent's machine split is the multi-part rule's.
- 2026-10-28 (user, [round 62](../../concept-rounds/round-62/README.md), closed; page answers): the **Talon's** production art accepted as final (F.1: 16 headings, the leader's gold spine stripe, the twin muzzles' glint, the spinning wing of its death). The user's note: the Talons "die a bit easy" and the hex shield "could be a bit stronger" (also C.1, "the shield needs a bit more strength"): part B raises the Talon's shield in its data and README. Sounds: the turn-in whine a (S.10), the twin shot **b** (S.11), the leader's squelch **b** (S.12); a's shot and squelch moved to `concept/rejected/` in the sfx part.
- 2026-10-31 (step m10-d-enemies-a, Act 6 design phase; decided 2026-10-31 (round 72)): the Act 6 air units
  promoted from the roster to draft specs: the [Chimera](chimera/README.md) (L36, [regeneration](../README.md#regeneration)),
  the [Mirror Interceptor](mirror-interceptor/README.md) (L37, [reflect](../README.md#reflect), Q1 c) and the
  [Harrow](harrow/README.md) (L38, the `death-burst` on a ram); the [Buzzsaw Drone](buzzsaw-drone/README.md)'s Level 38
  return added to its document (`review`). The hybrids wear the gap analysis's Q9 a livery (gunmetal with teal
  tissue seams). The Choir Seraph stays a roster row (Act 7).
