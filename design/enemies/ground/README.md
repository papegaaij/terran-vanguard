---
title: Ground enemies
design: approved
implementation: in-progress
art: final
updated: 2026-10-29
---

# Ground enemies

## Summary

Stationary and crawling enemies on the `ground` layer: turrets, walkers, bunkers, spawners and
shield pylons, on planetary surfaces, station hulls and asteroid rock. They scroll with the
terrain, never collide with the player, and are what `anti-ground` and `area` weapons are for.

## Contents

Promoted to their own documents for the Acts 1–2 wrap-up, plus the Act 3 drafts of the overnight design phase (2026-10-12) and the Act 5 drafts of its design phase (2026-10-27,
approved in round 60); the remaining units stay in the roster below.

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [spine-turret](spine-turret/README.md) | Basic grown turret with aimed thorns (L02) | approved | done | final |
| [polyp-mortar](polyp-mortar/README.md) | Acid mortar with a marked impact and ring burst (L05) | approved | done | final |
| [scuttler](scuttler/README.md) | Crab walker with frontal claw armour and facing fans (L04) | approved | done | final |
| [creeper](creeper/README.md) | Salamander walker crawling in convoys, 5-way fans (L08) | approved | done | final |
| [hive-node](hive-node/README.md) | Hardened spawner mound, anti-ground only (L09) | approved | done | final |
| [ravager](ravager/README.md) | Animal pack hunter that gallops and pounces (L09) | approved | done | final |
| [burrower](burrower/README.md) | Sand-swimmer that surfaces after a bulge tell, fires a ring, dives (L15) | approved | in-progress | final |
| [tendril-anchor](tendril-anchor/README.md) | Wall anchor pairs with a damaging tendril barrier; beam burns through (L16) | approved | done | final |
| [threadcrawler](threadcrawler/README.md) | Ground centipede chain on canyon floors and walls, travelling spore wave (L16) | approved | done | final |
| [shellback](shellback/README.md) | Armoured beast with a spore mortar; curls and rolls once, crushing (L17) | approved | done | final |
| [dust-devil](dust-devil/README.md) | Spinning vortex with a core window, pulls ship and pickups (L18) | approved | in-progress | final |
| [drill-rig](drill-rig/README.md) | Hardened Vrell drill structure on Europa's ice shelf, vital core and four pumps (L22) | approved | done | final |
| [siphon-pump](siphon-pump/README.md) | Vrell pump on Thera Deep's farm racks, no attack; Level 23's secondary (L23) | review | done | final |
| [spawning-reef](spawning-reef/README.md) | Soft Vrell coral spawner releasing Glassfin fry; Level 24's secondary (L24) | review | in-progress | final |
| [relay-beacon](relay-beacon/README.md) | Human-built relay mast with the Helix handshake, no attack; Level 27's secondary (L27) | review | in-progress | final |
| [warden-tank](warden-tank/README.md) | Unmarked grey tank, hull and turret, bursts from behind (L19); Ascendancy livery at L34 | approved | done | final |
| [sam-nest](sam-nest/README.md) | Ascendancy launcher, salvos of shootable homing diamonds; dormant until a station turns (L29) | approved | done | final |
| [clamp-gun](clamp-gun/README.md) | Helix turret on the prison tender's docking clamps; the four hold Level 29's rescue halt (L29) | approved | done | final |
| [sentinel-tower](sentinel-tower/README.md) | Tower with a laser sweep in any direction, a full turn every third sweep, hardened collar (L32) | approved | done | final |
| [strider](strider/README.md) | Biped mech with fans and a homing pod; a lost leg topples it, crushing ground units (L32) | approved | done | final |
| [rail-bunker](rail-bunker/README.md) | Hardened bunker, a locked 1 s telegraph line then a `heavy` rail shot (L33) | approved | done | final |

## Roster

| Name | Faction | Layer | Tier | Role/ability | Formations | First level | Design |
|---|---|---|---|---|---|---|---|
| Graft Turret | Hybrid | ground | small | An Ascendancy turret overgrown with Vrell tissue. **Regrows** 5 s after destruction unless its tissue root (weak point) is destroyed too. | turret nest | 37 | idea |
| Shield Pylon | Ascendancy | ground | medium | Hardened pylon projecting a dome that makes every enemy inside it invulnerable. Destroy pylons to open fortress sections. | turret nest | 41 | idea |
| Halo Platform | Ascendancy | ground (fortress mount) | large | A ring of 6 turret segments rotating around a shielded core (`radial` ring + 32-angle turrets). Each turret is `destroyable` and fires as it swings round (`aimed`, `laser-line`); the core's shield drops when half the ring is gone. The ring's rotation speeds up as turrets die. | turret nest (solo centrepiece) | 41 | idea |

## Design

- Ground targets use the [layer rules](../README.md): all weapons hit them, `anti-ground`
  does ×2, and **hardened** targets can only be damaged by `anti-ground` weapons.
- Destroyed ground targets leave wreckage (a crater or burnt husk) on the ground layer for the
  rest of the level, a staple of late-90s shooters.
- Ground targets pay more credits than air enemies of similar toughness, so an `anti-ground`
  loadout is rewarded on surface levels.
- Not everything on the ground stands still: walkers (Scuttler, Creeper, Strider), tanks
  (Warden Tank) and crawlers (Threadcrawler) move on their own heading with 16/32
  angle sprites and walk cycles; they still never collide with the player.

## Concept art

Concept [round 03](../../concept-rounds/round-03/README.md). Two Vrell design languages are proposed: **A "Sleek chitin"** (smooth, elongated, glossy violet chitin with thin glowing teal seams, pink eye as weak point) and **B "Armoured brood"** (bulky segmented carapace plates, claws and spikes, glow only between plates and in eye clusters). The key Act 1 enemies are shown in both; the others in A. Prompts: [concept/prompts.md](concept/prompts.md); generator `tools/concept/enemies_r03.py`.

| File | What | Status |
|---|---|---|
| [concept/spine-turret-r03-a.png](concept/spine-turret-r03-a.png) | Spine Turret, language A: smooth bulb with petal collar and one thorn barrel, rooted on a station hull; aim frames | superseded by the r04 colour pass |
| [concept/rejected/spine-turret-r03-b.png](concept/rejected/spine-turret-r03-b.png) | Spine Turret, language B: plated barnacle with a triple-spike barrel cluster | rejected — other variant preferred |
| [concept/polyp-mortar-r03-a.png](concept/polyp-mortar-r03-a.png) | Polyp Mortar (A): acid mouth ringed by tentacles; impact markers ahead of the player on lunar regolith | superseded by the r04 colour pass |
| [concept/rail-bunker-r03-a.png](concept/rail-bunker-r03-a.png) | Rail Bunker (Ascendancy ground turret): octagonal hardened bunker, twin-rail cannon with gold coils, red telegraph line | superseded by the r04 colour pass |

Concept [round 04](../../concept-rounds/round-04/README.md) — colour pass on the chosen enemies with the [role colours](../../README.md#role-colours) (chitin base = role family, glow = kind of threat; Ascendancy black & gold with a per-unit accent and a thin gold/red rim light). Same models and sheet layout; generator `tools/concept/enemies_r04.py`.

| File | What | Status |
|---|---|---|
| [concept/spine-turret-r04-a.png](concept/spine-turret-r04-a.png) | Spine Turret — mauve-grey slate chitin, violet glow (rooted, aimed shots); yellow needles | chosen |
| [concept/polyp-mortar-r04-a.png](concept/polyp-mortar-r04-a.png) | Polyp Mortar — slate chitin, lime acid (rooted, area denial) | chosen |
| [concept/rail-bunker-r04-a.png](concept/rail-bunker-r04-a.png) | Rail Bunker — black & gold with gunmetal accent and rim light | chosen |

Concept [round 05](../../concept-rounds/round-05/README.md) — new archetypes, each a PNG sheet plus a GIF in the 480×540 play field; generator `tools/concept/enemies_r05.py`.

| File | What | Status |
|---|---|---|
| [concept/scuttler-r05-a.png](concept/scuttler-r05-a.png) | Scuttler — Vrell six-legged walker (slate/lime): tripod gait, 16 headings × 6 walk phases, turns to its path, spits acid (sheet) | superseded by r08 (model-space re-render) |
| [concept/scuttler-r05-a.gif](concept/scuttler-r05-a.gif) | Scuttler — Vrell six-legged walker (slate/lime): tripod gait, 16 headings × 6 walk phases, turns to its path, spits acid (motion) | superseded by r08 (model-space re-render) |
| [concept/warden-tank-r05-a.png](concept/warden-tank-r05-a.png) | Warden Tank — Ascendancy tank (white accent): hull 16 headings following a road, turret 32 headings tracking the player, tread marks (sheet) | chosen — Ascendancy only (too mechanical for the Vrell) |
| [concept/warden-tank-r05-a.gif](concept/warden-tank-r05-a.gif) | Warden Tank — Ascendancy tank (white accent): hull 16 headings following a road, turret 32 headings tracking the player, tread marks (motion) | chosen — Ascendancy only (too mechanical for the Vrell) |
| [concept/strider-r05-a.png](concept/strider-r05-a.png) | Strider — Ascendancy 120 px biped mech (red accent): 8-frame walk cycle, torso at 32 headings twisting to fire shoulder cannons (sheet) | chosen — Ascendancy only (too mechanical for the Vrell) |
| [concept/strider-r05-a.gif](concept/strider-r05-a.gif) | Strider — Ascendancy 120 px biped mech (red accent): 8-frame walk cycle, torso at 32 headings twisting to fire shoulder cannons (motion) | chosen — Ascendancy only (too mechanical for the Vrell) |
| [concept/ravager-r05-a.png](concept/ravager-r05-a.png) | Ravager — Vrell pack hunter (rust/teal), 56 px: 16 headings, 8-frame gallop, pounce from ground to apex (sheet); generator `tools/concept/enemies_r05b.py` | chosen |
| [concept/ravager-r05-a.gif](concept/ravager-r05-a.gif) | Ravager — packs sweeping across lunar regolith, two hunters pouncing at the player's ground position (motion) | chosen |
| [concept/shellback-r05-a.png](concept/shellback-r05-a.png) | Shellback — armoured Vrell beast (slate/lime), 112 px: 16 headings, 6-frame heavy walk, curl sequence (sheet) | chosen |
| [concept/shellback-r05-a.gif](concept/shellback-r05-a.gif) | Shellback — lobs spore blobs with marked impacts, curls into a ball and rolls over boulders on a Mars plateau, uncurls (motion) | chosen |

Concept [round 06](../../concept-rounds/round-06/README.md) — generator `tools/concept/enemies_r06.py`.

| File | What | Status |
|---|---|---|
| [concept/creeper-r06-a.png](concept/creeper-r06-a.png) | Creeper — six-legged salamander walker (slate, violet fan gland), convoy along a megacity road (sheet) | chosen |
| [concept/creeper-r06-a.gif](concept/creeper-r06-a.gif) | Creeper — six-legged salamander walker (slate, violet fan gland), convoy along a megacity road (motion) | chosen |
| [concept/hive-node-r06-a.png](concept/hive-node-r06-a.png) | Hive Node — hardened spawner mound (teal-black, bone plates) in Vrell creep, iris releases Skitters (sheet) | chosen |
| [concept/hive-node-r06-a.gif](concept/hive-node-r06-a.gif) | Hive Node — hardened spawner mound (teal-black, bone plates) in Vrell creep, iris releases Skitters (motion) | chosen |
| [concept/threadcrawler-r06-a.png](concept/threadcrawler-r06-a.png) | Threadcrawler — centipede chain (olive/lime) ~300 px, leg ripple, spore pores firing as a travelling wave (sheet) | chosen |
| [concept/threadcrawler-r06-a.gif](concept/threadcrawler-r06-a.gif) | Threadcrawler — centipede chain (olive/lime) ~300 px, leg ripple, spore pores firing as a travelling wave (motion) | chosen |
| [concept/rejected/halo-platform-r06-a.png](concept/rejected/halo-platform-r06-a.png) | Halo Platform — Ascendancy turret ring: shielded core, six ring segments and turrets at 32 headings, shield drops when turrets die (sheet) | rejected — rotation far too jagged; redo with a dense angle set (round 07) |
| [concept/rejected/halo-platform-r06-a.gif](concept/rejected/halo-platform-r06-a.gif) | Halo Platform — Ascendancy turret ring: shielded core, six ring segments and turrets at 32 headings, shield drops when turrets die (motion) | rejected — rotation far too jagged; redo with a dense angle set (round 07) |
| [concept/dust-devil-r06-a.png](concept/dust-devil-r06-a.png) | Dust Devil — Vrell vortex organism (teal-black vanes, lime core) spinning in a grit swirl, pulling in pickups (sheet) | chosen |
| [concept/dust-devil-r06-a.gif](concept/dust-devil-r06-a.gif) | Dust Devil — Vrell vortex organism (teal-black vanes, lime core) spinning in a grit swirl, pulling in pickups (motion) | chosen |

Concept [round 07](../../concept-rounds/round-07/README.md) — smooth rotation (art-direction rule); generator `tools/concept/enemies_r07.py`.

| File | What | Status |
|---|---|---|
| [concept/halo-platform-r07-a.png](concept/halo-platform-r07-a.png) | Halo Platform r07 — ring as one 6-fold model at 128 frames per 60° step (768 per turn, ~0.9 px edge step), turrets at 64 headings; comparison with r06 and a rotation-over-time chart (sheet) | chosen |
| [concept/halo-platform-r07-a.gif](concept/halo-platform-r07-a.gif) | Halo Platform r07 — smooth rotation with eased speed-ups as turrets die, shield drops | chosen |

Concept [round 08](../../concept-rounds/round-08/README.md) — model-space re-render (seams and patterns now turn with the body), design unchanged; generator `tools/concept/rerender_r08.py`.

| File | What | Status |
|---|---|---|
| [concept/scuttler-r08-a.png](concept/scuttler-r08-a.png) | Scuttler re-render (sheet) | chosen |
| [concept/scuttler-r08-a.gif](concept/scuttler-r08-a.gif) | Scuttler re-render (motion) | chosen |

Concept round 36 (Act 3 "Red Dust", overnight design phase 2026-10-12; the round's review board
follows) — each a PNG sheet plus a GIF in the 480×540 play field over Martian ground; generator
`tools/concept/enemies_r36.py`.

| File | What | Status |
|---|---|---|
| [concept/burrower-r36-a.png](concept/burrower-r36-a.png) | Burrower A, sand maw — slate/lime radial worm head: furrow, bulge tell, breach, ring of orbs, submerge (sheet) | chosen (round 36, U.1) |
| [concept/burrower-r36-a.gif](concept/burrower-r36-a.gif) | Burrower A — three Burrowers cycling in the play field (motion) | chosen (round 36, U.1) |
| [concept/rejected/burrower-r36-b.png](concept/rejected/burrower-r36-b.png) | Burrower B, sand clam — bone/violet armoured clam-shell that faces the ship (sheet) | rejected — round 36: a (sand maw) picked |
| [concept/rejected/burrower-r36-b.gif](concept/rejected/burrower-r36-b.gif) | Burrower B — the same scene as A (motion) | rejected — round 36: a (sand maw) picked |
| [concept/tendril-anchor-r36-a.png](concept/tendril-anchor-r36-a.png) | Tendril Anchor A — polyp clamps on the canyon walls with a fleshy rope link; pulse, link and burning link (sheet) | chosen (round 36, U.2) |
| [concept/tendril-anchor-r36-a.gif](concept/tendril-anchor-r36-a.gif) | Tendril Anchor A — scrolling canyon: one pair broken by killing an anchor, one cut by a beam (mock-up beam) (motion) | chosen (round 36, U.2) |
| [concept/rejected/tendril-anchor-r36-b.png](concept/rejected/tendril-anchor-r36-b.png) | Tendril Anchor B — spine pylons with a crackling three-strand energy filament (sheet) | rejected — round 36: a (polyp clamps) picked |
| [concept/rejected/tendril-anchor-r36-b.gif](concept/rejected/tendril-anchor-r36-b.gif) | Tendril Anchor B — the same scene as A (motion) | rejected — round 36: a (polyp clamps) picked |
| [concept/warden-tank-r36-a.png](concept/warden-tank-r36-a.png) | Warden Tank, unmarked grey — the chosen r05-a model with only its livery changed (no gold, no accent), side by side with r05-a (sheet) | chosen (round 36, U.6: the unmarked grey livery) |
| [concept/burrower-final-r38-a.png](concept/burrower-final-r38-a.png) | Burrower final sprites (`tools/art/burrower.py`): body and maw bloom, the sand bow wave (screen-fixed while up, Q1 = a), 16-heading ripple, bulge, breach, dive, hole, crater, death (sheet) | chosen (round 38, F.3: approved as final) |
| [concept/burrower-final-r38-a.gif](concept/burrower-final-r38-a.gif) | Burrower final: three Burrowers cycling over a scrolling Level 15 ground stand-in, 10 fps (motion) | chosen (round 38, F.3: approved as final) |
| [concept/tendril-anchor-final-r39-a.png](concept/tendril-anchor-final-r39-a.png) | Tendril Anchor final sprites (`tools/art/canyon_units.py`): the anchor at 40 px in 32 headings with its pulse, the tendril's standing loop, dormant tell, flare, wither, frayed end and burn spot (sheet) | chosen (round 39, F.3: approved as final) |
| [concept/tendril-anchor-final-r39-a.gif](concept/tendril-anchor-final-r39-a.gif) | Tendril Anchor final: a gate dormant, flaring, standing, burnt through by a beam (mock-up) and withering, 10 fps | chosen (round 39, F.3: approved as final) |
| [concept/threadcrawler-final-r39-a.png](concept/threadcrawler-final-r39-a.png) | Threadcrawler final sprites (`tools/art/canyon_units.py`): head (16 headings × 4 jaw frames), segment (16 × 4 leg frames), tail, pore glow, husk (sheet) | chosen (round 39, F.4: approved as final) |
| [concept/threadcrawler-final-r39-a.gif](concept/threadcrawler-final-r39-a.gif) | Threadcrawler final: the chain crawling up a Level 16 floor, leg ripple and the pores' wave, 10 fps | chosen (round 39, F.4: approved as final) |
| [concept/shellback-final-r40-a.png](concept/shellback-final-r40-a.png) | Shellback final sprites (`tools/art/siege_units.py`, the chosen r05 a): 16 headings × 6 walk, curl, roll, vent swell and burst vent, belly glow, husk (sheet) | chosen (round 40, F.c: approved as final) |
| [concept/shellback-final-r40-a.gif](concept/shellback-final-r40-a.gif) | Shellback final: walk, vent swell, curl, roll, uncurl with the belly glowing, 10 fps | chosen (round 40, F.c: approved as final) |
| [concept/warden-tank-r36-a.gif](concept/warden-tank-r36-a.gif) | Warden Tank, unmarked grey — on a dirt road on Mars (motion) | chosen (round 36, U.6: the unmarked grey livery) |
| [concept/dust-devil-final-r41-a.png](concept/dust-devil-final-r41-a.png) | Dust Devil final sprites (`tools/art/storm_units.py`, the chosen r06 a): closed spin, rise, open with the lime core, core glow, grit swirls, in / out of the radius, collapse, death light, grit cloud (sheet) | chosen (round 41) |
| [concept/dust-devil-final-r41-a.gif](concept/dust-devil-final-r41-a.gif) | Dust Devil final: two devils cycling on the eye's dry lake, the grit spiral, pickups pulled in, one killed in its window, 15 fps | chosen (round 41) |
| [concept/warden-tank-final-r42-a.png](concept/warden-tank-final-r42-a.png) | Warden Tank final sprites, Act 3 grey (`tools/art/warden_tank.py`, the chosen r36 a; held in `build/art-held/` until the tank's `data.yaml` claims the root `warden-tank`): hull 16 headings × 2 tread frames, turret 32 aims at rest / recoil, muzzle flash, turret stub, burnt grey wreck 16 headings (sheet); accepted in round 42 (2026-10-20) | chosen (round 42, closed: approved as final) |
| [concept/warden-tank-final-r42-a.gif](concept/warden-tank-final-r42-a.gif) | Warden Tank final: a convoy of three riding a foundry belt, turrets tracking and firing 3-shot bursts (flash, recoil), back up the screen after the ship passes; a turret shot off, a wreck riding on, 20 fps; accepted in round 42 (2026-10-20) | chosen (round 42, closed: approved as final) |

Concept round 48 (Act 4 "Deep Water" design phase, 2026-10-21) — the Level 22 Drill Rig on Europa's
ice shelf; generator `tools/concept/enemies_r48.py`. Prompts:
[concept/prompts.md](concept/prompts.md#round-48--act-4-opener-the-drill-rig-level-22).

| File | What | Status |
|---|---|---|
| [concept/rejected/drill-rig-r48-a.png](concept/rejected/drill-rig-r48-a.png) | Drill Rig A, bone bore tower — slate base ring, a spinning crown of bone teeth round the lime bore, four lime-veined pump sacs on hoses (sheet) | rejected — round 48: b picked |
| [concept/rejected/drill-rig-r48-a.gif](concept/rejected/drill-rig-r48-a.gif) | Drill Rig A — on the ice shelf: shard rings, bolts glancing off, bombs take the pumps then the core; a bore hole and a steam plume (motion) | rejected — round 48: b picked |
| [concept/drill-rig-r48-b.png](concept/drill-rig-r48-b.png) | Drill Rig B, rust auger flower — a rust mound with three turning auger blades round the lime bore, ribbed bellows pumps (sheet) | chosen — round 48 |
| [concept/drill-rig-r48-b.gif](concept/drill-rig-r48-b.gif) | Drill Rig B — the same scene as A (motion) | chosen — round 48 |
| [concept/drill-rig-final-r51-a.png](concept/drill-rig-final-r51-a.png) | Drill Rig final frames (`tools/art/drill_rig.py`, round 48 b with stronger augers): the core's 8 spin frames and glow, the stump, pumps (heave, dead), base and crack decals, the steam plume, the shard ring's tell and release, three screens on the shelf (sheet) | chosen (round 51, F.1: accepted as final) |
| [concept/drill-rig-final-r51-a.gif](concept/drill-rig-final-r51-a.gif) | The collar spinning, a tell and a ring of ten shards, bombs taking the pumps (the ring slowing), the core's death, stump, crack and steam | chosen (round 51, F.1: accepted as final) |
| [concept/siphon-pump-final-r52-a.png](concept/siphon-pump-final-r52-a.png) | Siphon pump final frames (`tools/art/siphon_pump.py`, from the Thera Deep mood still): the heave loop, the sump's glow, the dead pump, the break, the root decal live and withered (sheet) | chosen (round 52, F.4: accepted as final) |
| [concept/siphon-pump-final-r52-a.gif](concept/siphon-pump-final-r52-a.gif) | Two pumps heaving on the farm floor, one shot and breaking | chosen (round 52, F.4: accepted as final) |
| [concept/rejected/spawning-reef-final-r53-a.png](concept/rejected/spawning-reef-final-r53-a.png) | Spawning reef, proposal **a** "coral knot" (`tools/art/spawning_reef.py --look a`, from the abyssal plain mood still; rejected, b is in `assets/` since step E5): plum coral bulbs, bone finger coral, teal polyps, the six-petal iris (6 states × 4 breathing frames, the Hive Node's layout), its glow, the reef-bed decal and wither, the coral burst, tatters, the razed stump (sheet) | rejected (round 53, F.4: b picked) |
| [concept/rejected/spawning-reef-final-r53-a.gif](concept/rejected/spawning-reef-final-r53-a.gif) | Two reefs on the plain at 80 px/s: the iris opening, four Glassfin fanning out every 3.5 s, one reef shot and razed | rejected (round 53, F.4: b picked) |
| [concept/spawning-reef-final-r53-b.png](concept/spawning-reef-final-r53-b.png) | Proposal **b** "bone chimneys" (`--look b`): a plum dome ringed by seven leaning bone tube chimneys with violet lips and teal-lit mouths, the iris at the crown; the same states, decal, burst, tatters and stump (sheet) | chosen (round 53, F.4 = b: "bone chimneys", accepted as final) |
| [concept/spawning-reef-final-r53-b.gif](concept/spawning-reef-final-r53-b.gif) | b's reefs on the plain: releases, one razed | chosen (round 53, F.4 = b: "bone chimneys", accepted as final) |
| [concept/rejected/spawning-reef-capture-r53-a.png](concept/rejected/spawning-reef-capture-r53-a.png) | Game capture, M8 part D (step D5; Level 24 on medium, `--invulnerable`, the balance plan's Level 24 fit: Harpoon Torpedoes L3, Depth Charges L3, Micro-Missile Pods L3/L2, the Pressure hull, the Sonar Pulse, Rook along): the first spawning reef, look a (2× detail; two runs) at t=39.8 and 40.2, its iris open and the fry darting out, then razed in a second run where the ship sat under it, t=39.9 and 40.6, the coral burst and the dead husk | rejected (round 53, F.4: b picked) |
| [concept/rejected/spawning-reef-capture-r53-a.mp4](concept/rejected/spawning-reef-capture-r53-a.mp4) | The same as a video with the game's sound (t=36.5–43): the reef arriving, its iris opening, fry, razed | rejected (round 53, F.4: b picked) |
| [concept/relay-beacon-final-r57-a.png](concept/relay-beacon-final-r57-a.png) | Relay beacon final frames (`tools/art/relay_beacon.py`, Levels 24-26's mast as Level 27's target): the 4 s pulse, its light, the destroyed dark frame, the break (sheet) | chosen (round 57: accepted as final) |
| [concept/relay-beacon-final-r57-a.gif](concept/relay-beacon-final-r57-a.gif) | Four masts pulsing on the beacon ridge in the dim blue, two shot down | chosen (round 57: accepted as final) |
| [concept/drill-rig-capture-r51-a.png](concept/drill-rig-capture-r51-a.png) | Game capture, Level 22 (M8 part B, step B5, medium, `--invulnerable`, Hammer Mortar + two Bomb Racks): the first rig's tell (t=41.5), its ring of shards (t=42), the next tell with the bombs on it (t=45), the core's death (t=46.5), the stump and dead pumps (t=48); the second rig's remains (t=63.5) | chosen (round 51, F.1: accepted as final) |
| [concept/drill-rig-capture-r51-a.mp4](concept/drill-rig-capture-r51-a.mp4) | The same run, t 37–51 with the game's sound: the ship moving under the first rig, the collar's hum, the tells and rings, the pumps and the core breaking, the death | chosen (round 51, F.1: accepted as final) |

Production art, M9 part B (concept round 62, step ART-B): the SAM Nest re-rendered from round 60's chosen A.11 a in the Q.11 b look; generator `tools/art/belt_units.py sam`, held in `build/art-held/` until its data claims it. Prompts: [concept/prompts.md](concept/prompts.md#round-62--production-the-sam-nest-m9-part-b).

| File | What | Status |
|---|---|---|
| [concept/sam-nest-final-r62-a.png](concept/sam-nest-final-r62-a.png) | SAM Nest final frames (`tools/art/belt_units.py`): the round base awake (red lights) and dormant (white), the wreck, the twin-tube rack at 16 headings shut and open (the tell), the folded dormant rack and the 1 s wake, the assembled nest (sheet) | chosen (round 62, F.2: accepted as final) |
| [concept/sam-nest-final-r62-a.gif](concept/sam-nest-final-r62-a.gif) | A dormant white-lit nest waking red beside an awake one; racks turning to the ship, the hatch tell, salvos of two homing diamonds | chosen (round 62, F.2: accepted as final) |
| [concept/sam-nest-capture-r62-a.png](concept/sam-nest-capture-r62-a.png) | Game capture, Level 29 (step B5): a SAM Nest pair on the Helix dock block, the hatch tell and a salvo of homing diamonds | chosen (round 62, F.2: accepted as final) |
| [concept/sentinel-tower-final-r65-a.png](concept/sentinel-tower-final-r65-a.png) | Sentinel Tower final frames (`tools/art/belt_units_e.py`, round 60's A.12 a in Q.11 b's gunmetal and gold): the collar, the stump, the head's topple, the eye glow, the beam strip and tip, the emitter head at 16 of its 32 headings, the assembled tower at 1x and 3x (sheet) | chosen (round 65, F.3 ok: accepted as final) |
| [concept/sentinel-tower-final-r65-a.gif](concept/sentinel-tower-final-r65-a.gif) | A tower in the tunnel: two arc sweeps (the 0.8 s wedge telegraph, the beam across the ship's bearing), the full turn's reach ring and sweep, the emitter shot down (the head toppling off, the stump) | chosen (round 65, F.3 ok: accepted as final) |
| [concept/strider-final-r65-a.png](concept/strider-final-r65-a.png) | Strider final frames (`tools/art/belt_units_e.py`, the r05 walker with round 60's pod and A.14 b's fall, Q.11 b): legs + torso + pod at 8 headings, the walk cycle, the torso and pod (shut / open), the fall to one side (stagger 2 + fall 4), the wreck (sheet) | chosen (round 65, F.4 ok: accepted as final) |
| [concept/strider-final-r65-a.gif](concept/strider-final-r65-a.gif) | A Strider walking and firing, its pod's tell, the knee hits, the leg lost: the stagger with the fall line, the buckle and fall, the crush of the SAM Nest under the line | chosen (round 65, F.4 ok: accepted as final) |
| [concept/sentinel-tower-capture-r65-a.png](concept/sentinel-tower-capture-r65-a.png) | Game capture, Level 32 (step E5; [recipe](concept/prompts.md#sentinel-tower-capture-r65-a)): the gallery tower's full turn on medium (QE-3 a) and its arc on easy | chosen (round 65, accepted with the level, F.3–F.4) |
| [concept/strider-capture-r65-a.png](concept/strider-capture-r65-a.png) | Game capture, Level 32 ([recipe](concept/prompts.md#strider-capture-r65-a)): Striders walking, the stagger and fall, the crush of the hall's SAM pair | chosen (round 65, accepted with the level, F.3–F.4) |
| [concept/strider-capture-r65-a.mp4](concept/strider-capture-r65-a.mp4) | Game capture with the game's sound: the hall Strider's topple onto the SAM pair | chosen (round 65, accepted with the level, F.3–F.4) |
| [concept/rail-bunker-final-r66-a.png](concept/rail-bunker-final-r66-a.png) | Rail Bunker final frames (`tools/art/belt_units_f.py`, the chosen r04 bunker with A.13 a's solid lock line in Q.11 b's gunmetal, gold 26-27 %): the octagon, the turret at 8 of its 32 headings, the coils' charge glow, the wreck, the rail strip and flash (sheet) | chosen (round 66, F.3 ok: accepted as final) |
| [concept/rail-bunker-final-r66-a.gif](concept/rail-bunker-final-r66-a.gif) | A bunker on the grown rock: the locked red line brightening over 1.0 s with the coils, the rail shot, a second lock, the bunker destroyed and its wreck | chosen (round 66, F.3 ok: accepted as final) |
| [concept/rail-bunker-capture-r66-a.png](concept/rail-bunker-capture-r66-a.png) | Game capture, Level 33 (step F5; [recipe](concept/prompts.md#rail-bunker-capture-r66-a)): the ridge pair's lines, the line leaving the turret as the bunker scrolls, the rail shot, a still ship hit | chosen (round 66, F.3 ok; stale: it shows C.1 a's line leaving the turret, which C.1 b replaced in step FIX3, where the line rides on the muzzle) |
| [concept/warden-tank-asc-final-r67-a.png](concept/warden-tank-asc-final-r67-a.png) | Warden Tank in its Act 5 Ascendancy livery, final frames (`tools/art/warden_tank.py --livery asc`: round 60's A.15 b, heavier gold, in Q.11 b's gunmetal with the 1 px gold/red rim; `warden-tank-asc*`, the grey tank's frame layout): the hull at 16 headings and its tread frames, the turret at 32 aims with the recoil, the stub, the wreck burnt black and gold (sheet) | chosen (round 67, G.3 ok with the addition of G.1's ground shadows: FIX4) |
| [concept/warden-tank-asc-final-r67-a.gif](concept/warden-tank-asc-final-r67-a.gif) | A convoy of three on a Level 34 Helix hulk's keel: the bursts, firing back up the screen, a turret shot off, a tank destroyed and its wreck | chosen (round 67, G.3 ok with the addition of G.1's ground shadows: FIX4) |
| [concept/warden-tank-asc-capture-r67-a.png](concept/warden-tank-asc-capture-r67-a.png) | Game capture, Level 34 (step G5; [recipe](concept/prompts.md#warden-tank-asc-capture-r67-a)): convoys in livery on a CDF walkway and a Helix keel road, a mining charge taking a hit tank, the keel charge blown as a convoy passes | chosen (round 67, G.3 ok with the addition of G.1's ground shadows: FIX4) |
| [concept/warden-tank-asc-clip-r67-1.mp4](concept/warden-tank-asc-clip-r67-1.mp4) | Game clip 1 with the game's sound (re-shown in round 67 on 2026-10-29; [recipe](concept/prompts.md#warden-tank-asc-clip-r67-1-4)): a convoy of three on a Helix hulk's keel road (the t=88 convoy, t 87.9–97.5, the ship holding fire); Level 34 at medium, cropped 300×540 on the tanks | chosen (round 67, G.3 ok with the addition of G.1's ground shadows: FIX4) |
| [concept/warden-tank-asc-clip-r67-2.mp4](concept/warden-tank-asc-clip-r67-2.mp4) | Game clip 2 with the game's sound (re-shown in round 67 on 2026-10-29; [recipe](concept/prompts.md#warden-tank-asc-clip-r67-1-4)): a convoy of two on a CDF frigate's walkway (the t=32 convoy, t 31.8–40.5, the ship holding fire); Level 34 at medium, cropped 300×540 on the tanks | chosen (round 67, G.3 ok with the addition of G.1's ground shadows: FIX4) |
| [concept/warden-tank-asc-clip-r67-3.mp4](concept/warden-tank-asc-clip-r67-3.mp4) | Game clip 3 with the game's sound (re-shown in round 67 on 2026-10-29; [recipe](concept/prompts.md#warden-tank-asc-clip-r67-1-4)): a mining charge taking a passing tank: the t=50 convoy on the picket frigate's walkway, the lead tank hit, the charge beside the road blown at t≈54.2 and the tank wrecked (t 50.0–57.8); Level 34 at medium, cropped 300×540 on the tanks | chosen (round 67, G.3 ok with the addition of G.1's ground shadows: FIX4) |
| [concept/warden-tank-asc-clip-r67-4.mp4](concept/warden-tank-asc-clip-r67-4.mp4) | Game clip 4 with the game's sound (re-shown in round 67 on 2026-10-29; [recipe](concept/prompts.md#warden-tank-asc-clip-r67-1-4)): tanks wrecked on the freighter's fore deck: the t=123 pair killed by the beam and the escort, their burnt black-and-gold wrecks scrolling down (t 123.0–131.0); Level 34 at medium, cropped 300×540 on the tanks | chosen (round 67, G.3 ok with the addition of G.1's ground shadows: FIX4) |

## Implementation

- [x] Each enemy promoted to its own directory with a stat block before it is implemented (every unit of Acts 1–2; the rule stands for the units of Acts 3–7, M6 part D)
- [x] Ground targets scroll with the terrain; destroyed states persist as wreckage (Acts 1–2's ground targets, M6 part D)

## Open questions

- Should the regrow timer of the Graft Turret be shown (a pulsing root)? Proposal: yes.

## Decisions

- 2026-09-30: Roster of 12 ground enemies drafted.
- 2026-09-30: Concept round 03: Spine Turret (both Vrell languages), Polyp Mortar and the Ascendancy Rail Bunker drafted as sprites.
- 2026-09-30: Concept round 04: colour pass on ground enemies with the role colours (r04 proposals).
- 2026-09-30: Enemy variety pass: size tier column added to every row; new units Scuttler, Threadcrawler, Warden Tank, Strider, Halo Platform, and Dust Devil (roster fork addition).
- 2026-10-01: Ravager (L09) and Shellback (L17) added: animal-like Vrell ground walkers, after the user found the Warden Tank and Strider too mechanical for the Vrell. Both mechanical units stay Ascendancy; the Warden Tank keeps its unmarked Act 3 hint at L19.
- 2026-10-01: Concept round 05: Ravager and Shellback concepts chosen ("very nice").
- 2026-10-01: Concept round 06: Creeper, Hive Node, Threadcrawler and Dust Devil chosen; Halo Platform's idea liked but its rotation too jagged — redo with a dense angle set (round 07).
- 2026-10-01: Concept round 07: Halo Platform with the dense angle set chosen — rotation now smooth.
- 2026-10-01: Acts 1–2 units promoted to full specs: Spine Turret, Polyp Mortar, Scuttler, Creeper, Hive Node, Ravager.
- 2026-10-01: Concept round 08: model-space re-renders accepted; they supersede the r05 sheets.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-12 (overnight design phase): Act 3 units promoted to draft specs (Burrower, Tendril
  Anchor, Threadcrawler, Shellback, Dust Devil, Warden Tank); concept round 36 proposes the Burrower and
  Tendril Anchor a/b and the unmarked-grey Warden Tank, accepted in round 34 (2026-10-12).
- 2026-10-13 (user, round 36): the Act 3 ground units approved (D.9–D.14). Picks: Burrower **a** (sand maw), Tendril Anchor **a** (polyp clamps); the unmarked grey Warden Tank accepted (U.6). The rejected variants (Burrower b, Tendril Anchor b) moved to `concept/rejected/`. With the canyon walls' b (MA.1) the anchors and the Threadcrawler keep to the walls' foot.
- 2026-10-18 (user, round 39): the Tendril Anchor's and the Threadcrawler's production art
  (`tendril-anchor-final-r39-a`, `threadcrawler-final-r39-a`) approved as final (F.3, F.4); both parts `art: final`.
- 2026-10-20 (user, round 44, A.b (b): its last `proposed` rows are decided (round 44 and the closed rounds 41-43)): **`art: final`** for this part. A later addition (a new sprite, a new concept round) is its own row and does not reopen the rest.
- 2026-10-21 (Act 4 design phase, step m8-denemies): the Drill Rig (L22, a hardened ground target) added as a draft spec.
- 2026-10-22 (user, round 52, closed): the siphon pump's final frames accepted (F.4; the part is `art: final`).
- 2026-10-23 (user, round 53, closed): the spawning reef is look **b** "bone chimneys" (F.4; a moved to `concept/rejected/`); the part is `art: final`.
- 2026-10-25 (user, [round 57](../../concept-rounds/round-57/README.md) closed): the relay beacon's final frames accepted as final (F.3); the part stays `art: final`.
- 2026-10-27 (Act 5 design phase, step D-enemies-B; approved in round 60): the SAM Nest, the
  Sentinel Tower, the Rail Bunker and the Strider promoted from the roster to draft specs (the Rail Bunker and the
  Strider keep their chosen r04 and r05 concepts); the Warden Tank's Act 5 return drafted (`review`). **J1**: the
  roster's Crawler Tank (L34) dropped: the Warden Tank already fires back up the screen after passing and was
  scheduled in Ascendancy livery from Act 5, so L34 flies it instead (one unit, its art and sounds saved).
- 2026-10-28 (user, [round 62](../../concept-rounds/round-62/README.md), closed; page answers): the **SAM Nest** (F.2) and the **clamp gun** (F.6, with the docking clamps, the gas and the Ember's launch, in Level 29's `concept/`) production art accepted as final. Sounds: the nest's hatch **b** (S.3), lock beep **b** (S.4) and wake `ab` (S.5 a's servo with S.4 b's lock beep, rebuilt by `tools/concept/audio/sfx_r62.py nest-wake-r62-ab`); the clamp gun's shot **b** (S.6) and the clamp's release **b** (S.7). C.2 accepted: the clamp guns sleep until the halt and carry 250 HP each. The a files moved to the sfx part's `concept/rejected/`.
- 2026-10-29 (user, [round 65](../../concept-rounds/round-65/README.md), closed): **F.3** (Sentinel Tower) and **F.4** (Strider) ok: their production sprites, GIFs and Level 32 captures are chosen as final; the sounds are in the [sfx](../../audio/sfx/README.md#decisions) decisions.
