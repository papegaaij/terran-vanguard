---
title: Bosses
design: approved
implementation: done
art: final
updated: 2026-10-26
---

# Bosses

## Summary

Seven act bosses end each act; five mid-bosses break up the longer stretches. Every boss is a
multi-part set piece that tests the mechanics its act introduced. Level 49 is a boss rush of
shortened re-creations of earlier bosses.

## Contents

Promoted to their own documents for the Acts 1–2 wrap-up, plus the Act 3 drafts of the overnight design phase (2026-10-12) and the Act 4 boss of its design phase (2026-10-21, draft); the remaining units stay in the roster below.

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [gorgon-frigate](gorgon-frigate/README.md) | Act 1 mid-boss: medusa-bell warship with three serpent-neck turrets (L05) | approved | done | final |
| [brood-carrier](brood-carrier/README.md) | Act 1 boss: living carrier, overhead pass, broadside bays, core (L07) | approved | done | final |
| [harbour-kraken](harbour-kraken/README.md) | Act 2 mid-boss: cephalopod around a platform, lane slams, surfacing head (L11) | approved | in-progress | final |
| [siege-spire](siege-spire/README.md) | Act 2 boss: rooted citadel that tears free and rises (L14) | approved | in-progress | final |
| [revenant-walker](revenant-walker/README.md) | Act 3 mid-boss: unmarked grey war walker striding through the casting yard, homing pods, rear flamer; its wreck is hint 2 (L19) | approved | in-progress | final |
| [dust-colossus](dust-colossus/README.md) | Act 3 boss: buried colony organism breaching at the edges and under the ship, mouths with rings, suction and bite (L21) | approved | in-progress | final |
| [abyssal-maw](abyssal-maw/README.md) | Act 4 boss: serpent leviathan coiled round the hatchery vent, gill rings, fan walls, coil sweeps, one dive and lunge, Helix implants; then the breakout and the run under Ascendancy cruisers (L28) | approved | in-progress | final |

## Roster

### Act bosses

| Name | Faction | Layer | Role/ability | Formations | First level | Design |
|---|---|---|---|---|---|---|
| Iron Sovereign | Ascendancy | space + ground | A battle station with three concentric rotating rings. Each ring carries batteries (rail, SAM, laser) that rotate into view; destroy the batteries to stop a ring, and stop all rings to expose the core. Tests piercing and positioning. | turret nest | 35 | idea |
| Ascendant | Hybrid | air + space | Vorne's flagship, half warship and half Vrell organism. **Phase 1**: gold hull with shield generators and mirror panels (`reflect`). **Phase 2**: the Vrell half erupts and regenerates tissue while spawning Chimeras. **Phase 3**: Vorne's Vrell lifeboat breaks free and flees (can't be killed; it escapes). Taunts from Vorne over the radio. | carrier + escorts | 42 | idea |
| Choir Heart | Vrell | all | The hive mind's core: a vast luminous organ in the hive's centre. It switches between the attack styles of every Vrell enemy in the game, reshapes the arena (walls of tissue, darkness, distorted scroll) and speaks through the Choir. The final phase is a single weak point in a storm of bullets. | – | 50 | idea |

### Mid-bosses

| Name | Faction | Layer | Role/ability | Formations | First level | Design |
|---|---|---|---|---|---|---|
| Honour Guard | Ascendancy | air | Three ace pilots in custom gold Talons attacking from front, sides and rear in turn. Each ace has its own pattern (sniper `laser-line`, `spiral` dancer, `kamikaze` feints) and taunts over the radio. | pincer, rear ambush | 40 | idea |
| Vorne's Chimera | Hybrid | air + ground | Vorne fused with his Vrell lifeboat inside a Vrell-grown citadel. Alternates between Ascendancy tactics (shields, missiles) and Vrell ones (spawning, regeneration). Pulp villain monologue; a tragic last line. | – | 48 | idea |

### Boss rush (L49 *Echoes*)

Shortened, single-phase "echo" versions of the Brood Carrier, Dust Colossus and Iron Sovereign,
re-created by the Choir. Armour and specials refill between fights. These re-use the original
boss assets with a spectral tint, not new designs.

## Design

- Every boss has a **health bar** with its name at the top of the play field ([HUD](../../ui/hud/README.md#in-the-play-field); a mid-boss's bar is shorter), visible **phases** (a distinct change in
  look and pattern), and weak points marked by glow (Vrell) or exposed machinery (Ascendancy).
- Bosses use only attack patterns from the [vocabulary](../README.md), combined; they add no
  new hidden rules.
- Every boss fight has a short **intro** (the boss enters, a radio line, its name on the HUD)
  and a large **death sequence** (chain explosions, screen flash) followed by a credit shower.
- Target duration: act bosses 90–180 s at medium, mid-bosses 45–75 s. Every boss and mid-boss
  has a **par** time in its data; a kill under par pays the
  [Boss rush](../../systems/scoring/README.md#level-end-bonuses) bonus.
- A boss's stat block and script (parts, neck or limb chains, phases, par) live in its
  `data.yaml` ([schema](../../tech/architecture/schemas-units/README.md)); the first is the
  [Gorgon Frigate](gorgon-frigate/README.md) (M4 part E).

## Concept art

Concept [round 03](../../concept-rounds/round-03/README.md). Prompts: [concept/prompts.md](concept/prompts.md); generator `tools/concept/enemies_r03.py`.

| File | What | Status |
|---|---|---|
| [concept/brood-carrier-r03-a.png](concept/brood-carrier-r03-a.png) | Brood Carrier (Act 1 boss, language A): full 288×626 sprite with labelled parts and weak points, phase 2 broadside in the play field, bay and core iris closed/open details | superseded by the r04 colour pass |

Concept [round 04](../../concept-rounds/round-04/README.md) — colour pass on the chosen enemies with the [role colours](../../README.md#role-colours) (chitin base = role family, glow = kind of threat; Ascendancy black & gold with a per-unit accent and a thin gold/red rim light). Same models and sheet layout; generator `tools/concept/enemies_r04.py`.

| File | What | Status |
|---|---|---|
| [concept/brood-carrier-r04-a.png](concept/brood-carrier-r04-a.png) | Brood Carrier — teal-black chitin with teal veins; bay sacs and core (weak points) in contrasting lime | chosen |

Concept [round 06](../../concept-rounds/round-06/README.md) — generator `tools/concept/bosses_r06.py`; all weak points glow lime like the Brood Carrier.

| File | What | Status |
|---|---|---|
| [concept/gorgon-frigate-r06-a.png](concept/gorgon-frigate-r06-a.png) | Gorgon Frigate (Act 1 mid-boss) — medusa-bell warship with three serpent-neck turrets, phases: turrets → stumps → crown opens over the core (sheet) | chosen |
| [concept/gorgon-frigate-r06-a.gif](concept/gorgon-frigate-r06-a.gif) | Gorgon Frigate (Act 1 mid-boss) — medusa-bell warship with three serpent-neck turrets, phases: turrets → stumps → crown opens over the core (motion) | chosen |
| [concept/rejected/harbour-kraken-r06-a.png](concept/rejected/harbour-kraken-r06-a.png) | Harbour Kraken (Act 2 mid-boss) — cephalopod wrapped around an offshore platform, telegraphed arm slams, head surfaces to fire fans (sheet) | rejected — doesn't read as rising from the depths; redo (round 07) |
| [concept/rejected/harbour-kraken-r06-a.gif](concept/rejected/harbour-kraken-r06-a.gif) | Harbour Kraken (Act 2 mid-boss) — cephalopod wrapped around an offshore platform, telegraphed arm slams, head surfaces to fire fans (motion) | rejected — doesn't read as rising from the depths; redo (round 07) |
| [concept/siege-spire-r06-a.png](concept/siege-spire-r06-a.png) | Siege Spire (Act 2 boss) — rooted citadel with turret and mortar roots, Wraith-launching maw; tears free and rises in its last phase (sheet) | chosen |
| [concept/siege-spire-r06-a.gif](concept/siege-spire-r06-a.gif) | Siege Spire (Act 2 boss) — rooted citadel with turret and mortar roots, Wraith-launching maw; tears free and rises in its last phase (motion) | chosen |

Concept [round 07](../../concept-rounds/round-07/README.md) — Kraken redone as a creature from the depths; generator `tools/concept/kraken_r07.py`.

| File | What | Status |
|---|---|---|
| [concept/harbour-kraken-r07-a.png](concept/harbour-kraken-r07-a.png) | Harbour Kraken r07 — one connected animal: arms continue under water to the mantle (four depth bands), foam collars where arms break the surface, swell-band water, top-down surfacing and diving, slam sequence, phases (sheet) | chosen |
| [concept/harbour-kraken-r07-a.gif](concept/harbour-kraken-r07-a.gif) | Harbour Kraken r07 — 10.4 s loop at 15 fps: churning lane telegraph, arm rises and slams with spray, head surfaces crown-first, lime eyes, 7-orb fan, dives | chosen |

Concept round 36 (Act 3 "Red Dust", overnight design phase 2026-10-12; the round's review board
follows) — the Act 3 mid-boss and act boss, a/b each; generator `tools/concept/bosses_r36.py`.
The walker's weak point is a hot-amber reactor on its back (the Ascendancy rule for exposed cores).

| File | What | Status |
|---|---|---|
| [concept/revenant-walker-r36-a.png](concept/revenant-walker-r36-a.png) | Revenant Walker A "Strider heavy" — unmarked grey reverse-knee biped: two shoulder missile boxes (2×3 cells) that die separately, tail flamer between fuel tanks, hot-amber reactor grille on its back; the wreck with its serial plate ground blank, close-up (sheet) | chosen (round 36, B.1) |
| [concept/revenant-walker-r36-a.gif](concept/revenant-walker-r36-a.gif) | Revenant Walker A — strides through the foundry; front guns kill the launchers, rear guns the back reactor once it is below the ship; the wreck and the plate's glint (motion) | chosen (round 36, B.1) |
| [concept/rejected/revenant-walker-r36-b.png](concept/rejected/revenant-walker-r36-b.png) | Revenant Walker B "Siege crawler" — low four-legged hull with a dozer prow, two vertical-launch blocks (2×4 cells), twin-nozzle rear flamer barbette, reactor under radiator fins; the wreck with its plate cut off by torch (sheet) | rejected — round 36: a (Strider heavy, biped) picked |
| [concept/rejected/revenant-walker-r36-b.gif](concept/rejected/revenant-walker-r36-b.gif) | Revenant Walker B — the same fight as A (motion) | rejected — round 36: a (Strider heavy, biped) picked |
| [concept/revenant-walker-final-r42-a.png](concept/revenant-walker-final-r42-a.png), [.gif](concept/revenant-walker-final-r42-a.gif) | Revenant Walker production art (`tools/art/revenant_walker.py`, step ART-F1; held in `build/art-held/` until the walker's `data.yaml` claims the root `revenant-walker`): 8 headings × 8-frame walk, the hull with the front plate and chin cannon, the two reactor vents (shut / open / overheated), the shoulder pods (16 aim headings, hatch tell) and stubs, the flamer (32 aims) and pilot light, the 6-step topple and the wreck with the serial plate ground blank, the homing missile, its smoke puffs and the flame-cone puffs; the pass, the cone and the topple over the foundry yard in the GIF | chosen (round 42, F.a: approved as final) |
| [concept/dust-colossus-final-r44-a.png](concept/dust-colossus-final-r44-a.png), [.gif](concept/dust-colossus-final-r44-a.gif) | Dust Colossus production art (`tools/art/dust_colossus.py`, step ART-H2; held in `build/art-held/` until the Colossus's `data.yaml` claims the root `dust-colossus`): the chosen r36 b mouth at 72 px/unit in five poses (four edge breaches leaning 30° into the field on the body's neck, upright under the ship) with rise, risen, half open, open, the bite's tell and snap, and the dive; the gullet glow; the death (rears, shudders with lime ichor, sinks under pouring sand); the funnel loop and the crater it leaves, the bulge, phase 3's marker, the spine vent, the death's bursts, four debris rocks, the intel silhouette; a breach in each phase and the death over the arena floor in the GIF | chosen (round 44, F.c: approved as final) |
| [concept/dust-colossus-final-r45-a.png](concept/dust-colossus-final-r45-a.png), [.gif](concept/dust-colossus-final-r45-a.gif) | Dust Colossus round 45 production art (`tools/art/dust_colossus.py`, step ART-K, round 44 K.b): the armoured spine breaking the sand behind an edge mouth (4 tapering segments × 3 rise states × 16 headings, toward the field's centre), the three vents as organs on its segments (shut, up, venting, destroyed) instead of turrets; the death throe: the forebody rearing and screaming, the geysers bursting along the ridge tail to head, the fissure ring, falling debris, the collapse into the sinkhole and the dust plume; a rear and a following front breach and the throe in the GIF | proposed |
| [concept/dust-colossus-final-r46-a.png](concept/dust-colossus-final-r46-a.png), [.gif](concept/dust-colossus-final-r46-a.gif) | Dust Colossus round 46 production art (`tools/art/dust_colossus.py spine`, step ART-S, round 45 A.a): the spine re-rendered as one continuous ridged back: the four segment frames (same names and frame scheme) are stretches of one model (dark shingled armour plates over the violet hide, a bone keel spike per plate, a low sand berm on both flanks, one far key light), cut on plate edges and overlapping 12 px, so they join behind the mouth; the back laid behind rear, slanted front, left and right mouths (up and rising) on the sheet, the rear and front breaches in the GIF | chosen |
| [concept/dust-colossus-final-r46-b.png](concept/dust-colossus-final-r46-b.png), [.gif](concept/dust-colossus-final-r46-b.gif) | Dust Colossus round 46 b review (`tools/art/dust_colossus.py --review`, step GSR; no new frames): the game's new rule for the spine drawn in the review — the back rises out of the ridge's sand bulge head to tail (one front along it, each segment shown up to it with a soft edge, sand on the plates at the front, its three states blended, puffs off the flanks) and sinks back tail to head; the GIF's ground now stands still from the tell to the dive's end, as the game's | chosen |
| [concept/rejected/dust-colossus-r36-a.png](concept/rejected/dust-colossus-r36-a.png) | Dust Colossus A "Rearing maw" — segmented slate worm arching out of a cracking sand dome; three jaw plates around rings of teeth and a lime throat; furrow and bulge tell, breach, rings, suction (sheet) | rejected — round 36: b (Sinkhole colony) picked |
| [concept/rejected/dust-colossus-r36-a.gif](concept/rejected/dust-colossus-r36-a.gif) | Dust Colossus A — breaches at the edges of the slow-scrolling Hellas arena in turn (motion) | rejected — round 36: b (Sinkhole colony) picked |
| [concept/dust-colossus-r36-b.png](concept/dust-colossus-r36-b.png) | Dust Colossus B "Sinkhole colony" — the ground collapses into a funnel; four bone mandibles unfold around a lime throat, violet-lit vein ridges under the sand (sheet) | chosen (round 36, B.2) |
| [concept/dust-colossus-r36-b.gif](concept/dust-colossus-r36-b.gif) | Dust Colossus B — the same arena as A (motion) | chosen (round 36, B.2) |

Concept round 48 (Act 4 "Deep Water", design phase 2026-10-21, step A-boss; the round's review board follows) —
the [Abyssal Maw](abyssal-maw/README.md) a/b (Q9) and the reveal's Ascendancy cruiser a/b (Q10, the faction's first
black-and-gold look); generator `tools/concept/bosses_r48.py`. The Maw keeps the lime Vrell weak points; its Helix
implants are grey with a hot-amber core and nothing on it is gold (Q11 a).

| File | What | Status |
|---|---|---|
| [concept/rejected/abyssal-maw-r48-a.png](concept/rejected/abyssal-maw-r48-a.png) | Abyssal Maw A "Rising maw" (Q9 a, the roster's draft) — a broad two-jawed head of dark chitin rising out of the black water: jaw shells round a ring of bone teeth and a lime throat, violet gill frills, two Helix implants on the lower jaw; rise and jaw states (sheet) | rejected — round 48: b picked |
| [concept/rejected/abyssal-maw-r48-a.gif](concept/rejected/abyssal-maw-r48-a.gif) | Abyssal Maw A — the shadow growing under the ship, the jaw-snap up through the play plane, Eel Swarms peeling off as it sinks, fan walls from the deep, a second snap with the implants sparking, over the L28 vent arena (motion) | rejected — round 48: b picked |
| [concept/abyssal-maw-r48-b.png](concept/abyssal-maw-r48-b.png) | Abyssal Maw B "Coiled guardian" (Q9 b, recommended) — a 1,750 px serpent coiled round the hatchery vent: 31 shingled segments (dark plates over violet hide, teal flank line; 32 angles, a four-step taper, the sweep's lime and the shed's flare), the head (shut, throat glow, open, lunge), four gill sacs (idle, swelling, torn), the Helix implant (shut, sparking, open, blown), the glow-only twins for the dark (sheet) | chosen — round 48 |
| [concept/abyssal-maw-r48-b.gif](concept/abyssal-maw-r48-b.gif) | Abyssal Maw B — the coil rising round the edges, phase 1 gill rings, the throat fan and a sac shot off, phase 2 fan walls with a gap and a coil sweep, the dive into the dark, the shadow column and the lunge, phase 3's implants sparking open with Varga's line; the 130 s fight in 20 s (motion) | chosen — round 48 |
| [concept/rejected/ascendancy-cruiser-r48-a.png](concept/rejected/ascendancy-cruiser-r48-a.png) | Ascendancy cruiser A "Regent wedge" — a 220×640 black-and-gold faceted wedge: stepped spine, bridge tower, a row of gun ports down each flank (cold and in a broadside's tell), point-defence turrets, crimson accent, the gold sunburst emblem and the Helix hull code on the bow (placeholder text), the gold / red rim light (sheet) | rejected — round 48: b picked |
| [concept/rejected/ascendancy-cruiser-r48-a.gif](concept/rejected/ascendancy-cruiser-r48-a.gif) | Ascendancy cruiser A — two cruisers break up through the Europa ice beside the wing (cracks, slabs sheeting off, mist, an open-water channel), Okafor's "Those are ours", then the run: broadside bands, flak markers, point-defence bursts, two bands at once (motion) | rejected — round 48: b picked |
| [concept/ascendancy-cruiser-r48-b.png](concept/ascendancy-cruiser-r48-b.png) | Ascendancy cruiser B "Forked monitor" — a forked ram bow, sponsons with three twin casemate guns a side and gun ports between them, gold crown fins across the spine, tiered bridge, ivory accent, the emblem and the hull code (sheet) | chosen — round 48 |
| [concept/ascendancy-cruiser-r48-b.gif](concept/ascendancy-cruiser-r48-b.gif) | Ascendancy cruiser B — the same breakout and run as A (motion) | chosen — round 48 |
| [concept/abyssal-maw-final-r58-a.png](concept/abyssal-maw-final-r58-a.png) | Abyssal Maw production art (`tools/art/abyssal_maw.py`, round 48 b): the coil segment at 32 headings and 4 tapers with its teal glow and lime tell, the head's 7 frames (rest, throat glow, open, reared, dive, lunge, dead) with glows and the jaw rim's orbs, the gill sacs, the Helix implants (shut, sparking, open, blown) and their arc, the shed flare, silt, the lunge's shadow, the blow-out, the ichor and the vent's flare; 0.86 of one atlas page (sheet) | chosen (round 59: accepted as final) |
| [concept/abyssal-maw-final-r58-a.gif](concept/abyssal-maw-final-r58-a.gif) | The round-48 compressed fight redrawn with the production frames on Level 28's caldera: the rise, phase 1, phase 2 with a sweep and a shed, the dive, the shadow and the lunge, phase 3's implants, the death (the implants blow, the coil sinks tail to head, the vent flares) | chosen (round 59: accepted as final) |
| [concept/ascendancy-cruiser-final-r58-a.png](concept/ascendancy-cruiser-final-r58-a.png) | Ascendancy cruiser production art (`tools/art/ascendancy_cruiser.py`, round 48 b "Forked monitor"): the 240x660 hull with `HDX-417` in gold on the bow, its running glow and the broadside's tell, its shadow, the ice crust sheeting off (6 frames; step I2: the night ice it lifts) and the hole it breaks in the ice, the port blast, point defence, tracer dash, flak marker and airburst, sparks (sheet) | chosen (round 59: accepted as final) |
| [concept/ascendancy-cruiser-final-r58-a.gif](concept/ascendancy-cruiser-final-r58-a.gif) | Level 28's breakout on the night ice: the cruisers rising through the ice and shedding it (step I2: through the intact night ice where they are, each hull moving with the ice at first, its hole left behind), Okafor's line, then the run (broadside tells and volleys, two bands, flak, point defence) | chosen (round 59: accepted as final) |
| [concept/maw-arrival-capture-r58-a.png](concept/maw-arrival-capture-r58-a.png) | Game capture (step H7; Level 28, Xvfb, `--bench`, medium, `--invulnerable`, auto-fire, the ship unsteered, the balance plan's Level 28 fit, the Bulwark; the fight lasts 1:50, its seconds from the warning): the Maw's arrival: the caldera before the warning, the warning's first frame (the coil and the bar there at once), 5.3 s (the head brightened) and 7.7 s (the first sac swelling), the play field at 1:1 | chosen (round 59: accepted as final) |
| [concept/maw-arrival-capture-r58-a.mp4](concept/maw-arrival-capture-r58-a.mp4) | The same run from the Choir's song (t≈97.7) to the fight's 13.7 s with the game's sound: the caldera's rumble, the warning, track 18 as built, the arrival roar, the first rings and fans (the a stand-ins) | chosen (round 59: accepted as final) |
| [concept/maw-phases-capture-r58-a.png](concept/maw-phases-capture-r58-a.png) | The phases at 1:1: phase 1 at 23.7 s (a gill sac's ring of 10) and 31.7 s (the throat open, its fan of 5); phase 2 at 55.2 s (a fan wall lighting along the jaw's rim, a sweep's bight told on the right) and 56.7 s (the wall in flight with its gap, the bight swung in); phase 3 at 103.7 s (the bottom sweep) | chosen (round 59: accepted as final) |
| [concept/maw-dive-capture-r58-a.png](concept/maw-dive-capture-r58-a.png) | The dive and the lunge at 62.7 s (the coil sliding off the edges, the last wall), 64.7 s and 69.2 s (the dark, the vent guttering), 70.7 s (the head racing up its column) and 72.2 s (the body as a wall up the column), 1:1 | chosen (round 59: accepted as final) |
| [concept/maw-dive-capture-r58-a.mp4](concept/maw-dive-capture-r58-a.mp4) | The same run 57.7–81.7 s with the game's sound: a sweep's groan and swing, the dive's roar, the music dropping away, the lunge, the coil's return | chosen (round 59: accepted as final) |
| [concept/maw-implants-capture-r58-a.png](concept/maw-implants-capture-r58-a.png) | Phase 3's implants at 87.7 s (the left coil's implant open, its hot-amber core) and 103.2 s (discharging under its arc), the upper play field at 1:1 | chosen (round 59: accepted as final) |
| [concept/maw-death-capture-r58-a.png](concept/maw-death-capture-r58-a.png) | The death at the kill (111.2 s: the implants blowing out), K+1, K+2 (the coil convulsing and sinking), K+3.5 (the vent's flare) and K+4.5 (the credit shower), 1:1 | chosen (round 59: accepted as final) |
| [concept/maw-death-capture-r58-a.mp4](concept/maw-death-capture-r58-a.mp4) | The same run from 105.7 s to K+12.7 with the game's sound: the implants' cracks, the death, the Choir's fragment, Okafor's "up the shaft", the shaft's climb (the a stand-ins) | chosen (round 59: accepted as final) |
| [concept/maw-crops-capture-r58-a.png](concept/maw-crops-capture-r58-a.png) | 1:1 crops: the head in phase 1 (31.7 s, the jaw open, a sac's ring), in phase 2 (39.7 s, closed, a sac swelling), an implant open (87.7 s) and discharging (103.2 s) | chosen (round 59: accepted as final) |
| [concept/cruisers-capture-r58-a.png](concept/cruisers-capture-r58-a.png) | Game capture (step H7; Level 28, Xvfb, `--bench`, medium, `--invulnerable`, auto-fire, the ship unsteered, the balance plan's Level 28 fit, the Bulwark; the level clock): the cruisers rising through the split ice at t≈117.9, 119.5 and 120.9, then the run at 123.9 (flak markers by the ship), 131.7 (a broadside's tracer band, the gun ports lit) and 143.7 (a volley crossing, point defence), the play field at 1:1 | chosen (round 59: accepted as final) |
| [concept/cruiser-bow-capture-r58-a.png](concept/cruiser-bow-capture-r58-a.png) | 1:1 crops of the right cruiser's bow with `HDX-417` along the hull at t≈131.7 and rising at 120.9 | chosen (round 59: accepted as final) |
| [concept/maw-arrival-capture-r59-a.png](concept/maw-arrival-capture-r59-a.png) | Game capture for round 59 R.1 (step I4; Level 28, Xvfb, `--bench`, medium, `--invulnerable`, auto-fire, the ship unsteered, the balance plan's Level 28 fit, the Bulwark; the fight lasts 1:57, its seconds from the warning): the new arrival at 1:1, fight 1.5 s (the head swimming in at the top-right corner), 2.3 (down the right edge), 3.1 (along the bottom), 3.9 (round the corner, up the left edge), 4.7 (along the top), 5.5 (the head bending down into place), 6.3 (rising onto the play plane), 7.3 (settled) | chosen (round 59: accepted as final) |
| [concept/maw-arrival-capture-r59-a.mp4](concept/maw-arrival-capture-r59-a.mp4) | The same run from the Choir's song (t≈97) to the fight's 15.4 s with the game's sound: the warning, the head winding in, the arrival roar, track 18, the first rings and fans | chosen (round 59: accepted as final) |
| [concept/maw-sweep-capture-r59-a.png](concept/maw-sweep-capture-r59-a.png) | A left sweep at 1:1 (the left third of the field): the tell at 47.4 s, swinging in 48.0, in at 48.3, held 48.6 and 48.9, swinging back 49.5, back 50.1: the bight's segments turning with the bend | chosen (round 59: accepted as final) |
| [concept/maw-dive-capture-r59-a.png](concept/maw-dive-capture-r59-a.png) | The dive at 1:1: the coil sliding off at 63.0 s, the dark (66.5), the head bursting up (70.4) and racing up its column (71.1), the body a wall (72.3), the tail leaving the top (73.5), the screen empty (74.0), the head back in at the top right (74.7), winding round (75.3, 75.9), the head along the top (76.7), settled (77.9) | chosen (round 59: accepted as final) |
| [concept/maw-dive-capture-r59-a.mp4](concept/maw-dive-capture-r59-a.mp4) | The same run 59.0–80.5 s with the game's sound: a sweep, the dive's roar, the music dropping away, the lunge, the body leaving through the top, the wind-in and the light returning | chosen (round 59: accepted as final) |
| [concept/maw-corners-capture-r59-a.png](concept/maw-corners-capture-r59-a.png) | The coil at rest at 7.3 s, 1:1, with its four corners at 2× (nearest): the rounded turns, every segment along the bend; the tail running off the top edge at the top right | chosen (round 59: accepted as final) |
| [concept/cruisers-rise-capture-r59-a.png](concept/cruisers-rise-capture-r59-a.png) | Game capture for round 59 R.2 (the same run; the level clock): the cruisers' rise at 1:1, t≈117.9 (the intact night ice), 118.5 (the crust lifting on both flanks), 119.1 (splitting, sheeting off), 119.7 (the hulls rising through), 120.3, 120.9 (risen, moving with the ice), 121.7 (slowing to the wing's pace), 122.9 (the run: the first broadside's tell) | chosen (round 59: accepted as final) |
| [concept/cruisers-hole-capture-r59-a.png](concept/cruisers-hole-capture-r59-a.png) | The lower half of both flanks at t≈119.5, 120.1 and 120.7, 1:1: the crust over the rising hull and the hole's rim at the hull's stern | chosen (round 59: accepted as final) |
| [concept/cruisers-rise-capture-r59-a.mp4](concept/cruisers-rise-capture-r59-a.mp4) | The same run t≈107–133 with the game's sound: the shaft's climb, the breakout at the ship's breach, the cruisers' rise with the reveal sting and the b rise sound, Varga's and Okafor's lines, the run's first broadsides | chosen (round 59: accepted as final) |

## Implementation

- [x] Each boss promoted to its own directory with phases, attack scripts and weak-point
      layout before it is implemented: the Acts 1–2 bosses (Gorgon Frigate and Brood Carrier,
      built in M4; Harbour Kraken and Siege Spire for M5).
- [x] The same for the Act 3 bosses (the Revenant Walker, L19, and the Dust Colossus, L21): promoted to specs
      (overnight design phase, 2026-10-12) and built with Act 3 (M7 parts F and H).
- [x] The same for the Act 4 boss (the Abyssal Maw, L28): promoted to its spec in the Act 4 design phase (2026-10-21)
      and built with Act 4 (M8 part H).
- [ ] The same for the roster's bosses of Acts 5–7 — **later: Act 5** to Act 7.
- [x] Shared boss framework: multi-part hitboxes, neck chains, phase transitions, HUD boss bar,
      the intro descent, the chained death and the credit shower (M4 part E, with the Gorgon
      Frigate; drawn in plain shapes until its production sprites).
- [x] The intro's sting (with Level 05's music) and the death's screen flash (an act boss's
      only, with the Brood Carrier in M4 part G).

## Open questions

- Should the Ascendant's third phase (Vorne escaping) be skippable, or is it a scripted end?
  Proposal: scripted. The player can damage the lifeboat for bonus credits, but it always escapes.

## Decisions

- 2026-09-30: 7 act bosses and 5 mid-bosses drafted, plus a boss rush at L49.
- 2026-09-30: Concept round 03: Brood Carrier mockup (language A) with launch bays, bay sacs and core iris as marked weak points.
- 2026-09-30: Concept round 04: colour pass on the Brood Carrier with the role colours (r04 proposals).
- 2026-10-01: Concept round 06: Gorgon Frigate and Siege Spire chosen. Harbour Kraken rejected: the arms look detached, the waves are plain circles, it pops up instead of surfacing top-down, and the arms' submerged parts and splash where they enter the water are missing — redo in round 07.
- 2026-10-01: Concept round 07: the redone Harbour Kraken chosen ("great").
- 2026-10-01: Brood Carrier length: about one screen, as in the chosen concept (the roster's "two screens" is dropped).
- 2026-10-01: Acts 1–2 units promoted to full specs: Gorgon Frigate, Brood Carrier, Harbour Kraken, Siege Spire.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-03: M4 part E: the boss bar sits at the top of the play field, as the HUD document has
  it (the "side HUD" here was a slip; main-agent choice); mid-bosses get a par and earn the Boss
  rush bonus (user decision); the boss data schema is planned with the Gorgon Frigate.
- 2026-10-03: M4 part E: the shared boss framework is built with the Gorgon Frigate (a set piece
  with a boss script, see the [architecture](../../tech/architecture/README.md) log); the sting
  comes with Level 05's data, the screen flash and the sprites with the production art.
- 2026-10-05: M4 close-out bookkeeping (round 26): the promotion item is split into the Acts 1–2 bosses, all promoted (two built in M4), and the roster's Acts 3–7 bosses, tagged with their acts; every other item is ticked, so the document is `done` for M4.
- 2026-10-12 (overnight design phase): the Revenant Walker (L19) and the Dust Colossus (L21)
  promoted from the roster to draft specs; concept round 36 proposes an a/b of each, accepted in round 36 (2026-10-13).
- 2026-10-13 (user, round 36): the Revenant Walker and the Dust Colossus approved (D.18, D.19). Picks: the walker **a** (the "Strider heavy" biped), the Colossus **b** (the "Sinkhole colony"); the rejected variants moved to `concept/rejected/`.
- 2026-10-20 (user, round 42): the Revenant Walker's production art approved as final (F.a): its row `chosen`, the walker `art: final`.
- 2026-10-20: the Dust Colossus's production art made (step ART-H2, round 44); accepted in round 44 (2026-10-20).
- 2026-10-20 (user, round 44, A.b (b): its last `proposed` rows are decided (round 44 and the closed rounds 41-43)): **`art: final`** for this part. A later addition (a new sprite, a new concept round) is its own row and does not reopen the rest.
- 2026-10-21 (Act 4 design phase, step D-boss; decided in round 48 (2026-10-22)): the [Abyssal Maw](abyssal-maw/README.md) promoted from the roster to a draft spec (Q9 b, the coiled guardian; Q10 a, the breakout and the run, the Ascendancy cruisers specced in its document); its roster row removed.
- 2026-10-26 (user, [round 58](../../concept-rounds/round-58/README.md) closed): the Abyssal Maw's production art (F.2) and the Ascendancy cruisers' (F.3) are **redone** for round 59 and stay `proposed` (their rows say "redo in round 59"). F.2 note, verbatim: "The creature just appears, a snake should wrap around you. The corners are just sharp 90 degree angles. The bump does not rotate the segments. After the creature runs through the middle, it teleports around you. Make it exit the screen entirely and then fold around you like at the (improved start)." F.3 note, verbatim: "The cruisers seem to ascent from underneath the ice, yet they pop up at places where's no ice. The appearance must align with the backdrop." C.4 (the hull code on the bows, as it is) accepted.
- 2026-10-26 (user, [round 59](../../concept-rounds/round-59/README.md) closed): the round 58 redos **accepted**: R.1 the
  Abyssal Maw's motion and R.2 the Ascendancy cruisers' rise (through the intact night ice where each hull is), so the
  Maw's and the cruisers' production art and the round 58 and 59 captures are `chosen` (accepted as final) and the Maw
  is `art: final`; C.1 keep (the Maw's par 138), C.2-C.5 accepted (C.4: the hulls ride the ice for 2 s while they
  rise).
