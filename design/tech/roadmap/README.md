---
title: Implementation roadmap
design: approved
implementation: in-progress
art: n/a
depends-on: [../architecture, ../../campaign]
updated: 2026-10-26
---

# Implementation roadmap

## Summary

The order in which the approved Acts 1–2 design is built: seven milestones, each ending in a
playable build that runs on all three OSes and is reviewed by the user. The tickets are the
*Implementation* checklists of the design documents (about 525 items over 97 documents); a
milestone lists which documents it covers, it does not copy their items.

## Design

### Milestones

| # | Milestone | Playable result | Main tickets |
|---|---|---|---|
| M0 | **Skeleton** | The game starts in full screen, shows an empty title screen, toggles display mode, quits; CI green on three OSes | [architecture](../architecture/README.md) (build, carried-over foundations, CI), [options](../../ui/options/README.md) display mode and settings file |
| M1 | **First flight** | Fly the ship over one scrolling setting with parallax, fire the starting gun, shoot Skitters, take damage, die and retry | [ship](../../player/ship/README.md), the starting front weapon, [Skitter](../../enemies/air/skitter/README.md), shield/armour, [retry](../../systems/retry/README.md), a minimal [HUD](../../ui/hud/README.md), music and SFX players, [controls](../../ui/controls/README.md) |
| M2 | **Level 01** | [Level 01](../../campaign/act-1-first-contact/level-01-break-at-dawn/README.md) from launch to end: scroll timeline, waves from its data file, ground targets, radio chatter, objectives, debrief totals | data-file loader and table sync, level script runner, formations, layers, [scoring](../../systems/scoring/README.md), [debrief](../../ui/debrief/README.md), replay test for L01 |
| M3 | **The campaign loop** | Menu → new game → briefing → hangar (buy, fit, sell) → level → debrief → save; load game | [main menu](../../ui/main-menu/README.md), [briefing](../../ui/briefing/README.md), [hangar](../../ui/hangar/README.md), [economy](../../systems/economy/README.md), [saves](../../systems/saves/README.md), [difficulty](../../systems/difficulty/README.md), [pause](../../ui/pause/README.md), [options](../../ui/options/README.md) |
| M4 | **Act 1** | Levels 02–07 with their enemies, bosses, weapons, specials and allies; balance tests green | [act 1](../../campaign/act-1-first-contact/README.md) levels, the Act 1 [enemies](../../enemies/README.md) and bosses, [weapons](../../player/weapons/README.md), [specials](../../player/specials/README.md), [allies](../../allies/README.md); the M3 documents' `later: M4` items: [economy](../../systems/economy/README.md) bounties and balancing sheet, [difficulty](../../systems/difficulty/README.md) bullet tags, [retry](../../systems/retry/README.md) boss checkpoint, [debrief](../../ui/debrief/README.md) data cores and act summary, [hangar](../../ui/hangar/README.md) test fire and Varga's intel lines |
| M5 | **Act 2** | Levels 08–14 and their new units and equipment | [act 2](../../campaign/act-2-homefront/README.md) levels and their enemies and equipment, the [wingmen](../../player/wingmen/README.md) escort slot (Rook from L08), the [saves](../../systems/saves/README.md) `escort` field, the Targeting computer in the shop with its effects and the Salvage scanner's ([ship systems](../../player/systems/README.md), part A), the perspective towers and roofs of L08 (art direction); built in parts A–I (below) |
| M6 | **Acts 1–2 release** (its `v0.1` tag moved to the end of M7: the first release is Acts 1–3, round 44 I.2) | Credits screen with the CC-BY attributions, polish pass, release bundles for all OSes on a `v0.1` tag | [credits](../../ui/credits/README.md), mouse support in the out-of-game screens ([ui](../../ui/README.md)), the [scoring](../../systems/scoring/README.md) high-score table per difficulty with its name entry at game over and at the campaign's end, remaining checklist items of Acts 1–2 |
| M7 | **Act 3** | Levels 15–21 and their new units and equipment | [act 3](../../campaign/act-3-red-dust/README.md) levels and their enemies, bosses and equipment (Ion Beam and `beam`, drones, EMP Burst, Orbital Lance, Shield Overcharge and cooldowns, Evasive thrusters, the third utility bay), terrain collision and canyon walls, reverse scroll, the dust-storm visibility, the Mars backdrops; built in parts A–I (below; the design approved in concept round 36) |
| M8 | **Act 4** (design decided in round 48, 2026-10-22; part A in progress) | Levels 22–28 and their new units and equipment | [act 4](../../campaign/act-4-deep-water/README.md) levels and their enemies, boss and equipment: under-water mode, darkness for every unit, the trench and shaft on `Terrain`, the Siren's slow, heat plumes, anti-sub gear (Harpoon Torpedoes, Depth Charges, Sonar Pulse, Decoy Flares, pressure hull, Mk IV), the Bulwark heavy drone and Rook's absence, the Abyssal Maw and the reveal; built in parts A–I (below) |

### Rules

- Work happens on a branch per milestone (`m0-skeleton`, …), merged into `main` when the user
  has reviewed the playable build; smaller steps inside a milestone are commits.
- A milestone is done when its documents' checklist items are ticked (deferred items aside, see
  below), their `implementation` status is `done`, CI is green and the user has played the build.
- Chosen concept art stands in for production art (see [architecture](../architecture/README.md#assets)).
  **Production art** (final-quality renders, every angle set and animation) is a separate track
  that **starts after M2**, once Level 01 has proven the sprite sizes, layers and atlas budget in
  the real game; it then replaces placeholders part by part.
- Design changes found during implementation go into the design tree first, in the same change.
- **Deferred items.** A checklist item that belongs to a later milestone stays in its document,
  unticked, and ends in `— **later: M4** (reason)` (or `later: art track` for work waiting on
  production art); an item that is partly done is split into a ticked part and a deferred part.
  The named milestone's row lists the item among its tickets unless its documents imply it. A
  document whose unticked items all carry a `later:` tag counts as done for its milestone and has
  `implementation: done`; it goes back to `in-progress` when the later milestone starts on it.
  Work for the acts after the Acts 1–2 release names the act instead (`— **later: Act 4**
  (under water)`), since no milestone covers it yet.

### M4 parts

M4 is built in parts, each a commit series the user can play, in this order: **A** the Act 1
arsenal on Level 01 (the simulation split into weapon, enemy and objective parts, the seven Act 1
weapons, the trait layer rules, overdrive, spare power); **B** Level 02; **C** Level 03; **D**
Level 04; **E** Level 05 (the first boss); **F** Level 06; **G** Level 07 and the act end; **H**
the close-out (balance tests, balancing sheet, bullet tags, test fire, Varga's intel lines). The
open points in a part's documents are settled with the user when the part starts. Each level's
production art is a concept round right after its part, so M4 ships no placeholders.

### M5 parts

M5 is built like M4: each part is a commit series the user can play, opened with its own gap check
and the user's decisions, closed by its production-art concept round, so M5 ships no
placeholders. The order is linear; H comes last among the levels, as it reuses B's Creeper, C's
Hive Node and Ravager and D's Wraith.

| Part | Scope | Depends on | Round |
|---|---|---|---|
| **A** Act 2 systems — **done** | Rook and the escort slot ([wingmen](../../player/wingmen/README.md): AI, guns, escort inventory, side, repairs, eject, retry and checkpoint, barks, `--escort`), save format 3 with `escort`, the hangar's escort UI and the HUD's escort box; the Act 2 arsenal without water (final effects and sounds of the Tail Gun, Fan Blaster, Hornet Launcher and Swivel Gun, the Proximity Mines' delivery and `area`); the Targeting computer and the Salvage scanner; the act HP factor and the Act 1 terms in data ([enemies](../../enemies/README.md#balancing-basis), [economy](../../systems/economy/README.md)). Flown on the Act 1 levels with `--loadout` and `--escort` | — | 28 (closed): Rook's craft and eject pod, the weapon effects, the Act 2 weapon sounds, the barks' voices, the scanner's glint |
| **B** Level 08 and the Act 2 intro — **done** | Level data (dense, typical haul, four Varga lines, voiced); Creeper; the megacity backdrop with perspective towers (scenery only) and traffic lanes; Act 2's data (title card, briefing pages, images); "Homefront" final and stems; the Act 1 → Act 2 transition | A | 30 (closed): the Creeper, the megacity backdrop and towers, the Act 2 still and briefing images, Rook's briefing portraits, "Homefront" and its base stem, the billboard (a), the traffic (a), the civilian's voice (b) |
| **C** Level 09 — **done** | Hive Node (hardened, periodic spawn), Ravager (`pack`, the pounce's air window), hardened enemies; hold zones and the level clock in script time; the named-target tracker; a missed node fails at once; the collapse; the bridge secondary (a wave tag); the `required` launch warning; the balance plan's anti-ground (Bomb Rack and Rook's Mortar); multi-level units in each level's atlas; "Firestorm" final and stems with a run-time stem hook; the Kilo Lead's audition | B | 31 (closed): the collapse's look (a and b rejected, the rework c approved with two tweaks and built), the Hive Node, the Ravager, the Level 09 backdrop with the trucks and the cocoon, the briefing images, "Firestorm" and its base stem approved as final; the collapse sound (a), both pounce sounds (picked at random), the Kilo Lead's voice (a); the captures, the voiced lines, the texts, the numbers, the decisions and the build choices accepted |
| **D** Level 10 — **done** | Wraith (cloak, loop, rear entry, `rear ambush`); Mote Swarm (`flock`, `swarm`, authored paths: the Skitter's item); the air escort (shuttles: stations and lane sway, liftoff and climb-out, every bullet and contact hurting) and the scripted loss with its music duck; the `SHUTTLES` tracker with armour bars; `required: [rear]` and the plan's rear check; the ambience change by section; rear-heavy pacing; the Lifeline voices' auditions | B | 32 (closed): the Wraith, the Mote Swarm, the Level 10 backdrop at first light with the ferry hatch, the briefing images and intel portraits approved as final; the shuttle (a), the loss's look (b, the iris column), the decloak sound (b, CC-BY), the swarm sound (b), the lance sound (a), the Lifeline and Lifeline Three voices (b, b); the captures, the voiced lines, the texts, the numbers, the decisions and the build choices accepted |
| **E** Level 11 — **done** | The `sub` layer and the water rules (a `water` level flag; the `sub` pass with pre-rendered collars); the Torpedo Pod and `anti-sub` (torpedoes seeking sunken triggers); Driftjelly (`field`, proximity ring above and below the water), Reef Spitter (rafts on the flanks); the naval convoy (cargo ships and frigate: stations, the glide to the arena lanes, the hold clear of Tiamat) with the `CONVOY` tracker and the `afloat` secondary; the Harbour Kraken mid-boss (anchored arena, lane slams, surfacing head); the ocean backdrop; the Bomb Rack over water; Atlas Control's audition | A | 33 (closed): the Driftjelly, the Reef Spitter, the Harbour Kraken (reworked on the user's feedback), the water and torpedo effects, the props, the ocean backdrop, the briefing images and intel pictures approved as final; the convoy ship pair (a), the lane telegraph and slam (a), Atlas Control's voice (b); the eight sounds, the captures, the voiced lines, the texts, the numbers, the decisions and the build choices accepted, the sunken pod at 3 hits |
| **F** Level 12 — **done** (combined round 34 closed) | Weather (rain, lightning reveal of the `low-air` banks, gusts and the wall's drift, the eye); Lamprey (chase, latch, drain, shake-off) with the chaser `swarm` and the rear `stream`; the sondes and the `collect` secondary; the drifting canister; the storm backdrop; the eye's music level; the mines' sounds in play | D, E | 34 |
| **G** Level 13 — **done** (combined round 34 closed) | `defend` with a timed halt; the Nansen Relay and its integrity bar; hook modes `alternate`, `in-arc`, `always`; Skimmer; `cross`; whiteout; supply drones; the arctic backdrop | E | 35 |
| **H** Level 14 and the act end — **done** (combined round 34 closed) | Root bursts; the Siege Spire act boss (root parts, maw launches, ground → air phase); the Geneva backdrop; the daylight section; Act 2's outro pages and act summary; the act-end order | B, C, D | 36 |
| **I** Close-out — **built, round 34 closed** (the voice listen-through done; the user's playthrough and the `sub` layer's three-OS CI check still open) | `ActPlaythroughTest` and `BalanceTest` for Act 2 with Rook and the Act 2 plan; the variety checklist as built; the later tags (drones and the other utility modules to Acts 3–6); leftover sounds and the voice listen-through; the user's playthrough | all | 37 |

### M7 parts

M7 is built like M5: each part is a commit series the user can play, opened with its own gap check
and closed by its production-art concept round. The Act 3 design was approved in concept
[round 36](../../concept-rounds/round-36/README.md) (2026-10-13), so a part's gap check only covers
what building it brings up. The architecture's new kinds come with the parts that build them
(Q.25c): `field` (B), `links` (C), `untouched` (D), the Spine Turret's `arc` (F), `pursuit` (G).

| Part | Scope | Depends on | Round |
|---|---|---|---|
| **A** Act 3 systems — **done** (round 37 closed; the user's playthrough open) | Ion Beam (`beam` delivery), Light Drone Bay and drone behaviours (orbit, block, rebuild), Rear Lance, Deflector Pod; EMP Burst (stun, strip), Orbital Lance, Shield Overcharge with cooldown timers; Evasive thrusters (dash, double-tap); buying the third bay (`UTILITY_3`); `reference_dps` 15–21 and the Act 3 balance plan. The [HUD](../../ui/hud/README.md)'s **cooldown ring** on the special row and the **dash pip**, and the [controls](../../ui/controls/README.md)' Dash binding (Q.25e). Flown on Act 1–2 levels with `--loadout`/`--special` | — | 37 |
| **B** Level 15 + Act 3 intro — **done** (round 38 closed; the user's playthrough open) | Act 3 `data.yaml` (title card, four pages, images), the Act 2 → 3 handover and the release saves at the L15 hangar playing the intro once (L.6); L15 data; the descent (clouds on `high-air`); Burrower and the `BURIED` layer; Olympus backdrop; Ares Control's lines rendered (cast in round 36, Thomas A) with the respelling list, his radio portrait (C.1 = b) | A | 38 |
| **C** Level 16 — **done** except the canyon walls' look (round 39 closed; R.1's wall redo in progress, to round 40) | Terrain: walls, islands, contact, shots vs rock, Rook and paths; the canyon walls rising toward the camera with the collision at their foot (MA.1 = b); Tendril Anchor (`link`, beam vs tendril); Threadcrawler (ground chain along the walls' foot); Valles backdrop | B | 39 |
| **D** Level 17 — **done** except the dome-break sound's redo; round 40 closed 2026-10-19 | Choir Herald (`aura`); Shellback (walk, mortar, curl-roll crush); the Ares Landing dome as its own [allies](../../allies/README.md) document (Q.25b; the geodesic glass look, MA.3 = a) and its secondary; Helix compound set piece; Ares Landing backdrop | B | 40 |
| **E** Level 18 — **done**; round 41 closed 2026-10-19, the three fixes (step FXS) accepted | Dust-storm visibility (the soft ragged falloff, MA.2 = b); Ghost Drone (hint 1; the eye and running lights, U.5); Dust Devil (pull, spiral, core window); plains backdrop; **track 9** "Grey Signal", the Act 3 B theme, with its motif stem (moved here from part G: Level 18 needs it first) | B | 41 |
| **F** Level 19 — **done**; round 42 closed 2026-10-20; the walker's reworks (round 42 K.a, round 43 W.a/W.e) and the sounds accepted in round 44 (W.h's brighter ring planned) | Revenant Walker mid-boss (the "Strider heavy" biped; hint 2, wreck beat); Warden Tank unmarked (hull + turret); conveyors, furnace vents, rear-firing turrets; Tharsis foundry backdrop; Vorne's motif layer | C or D (walkers) | 42 |
| **G** Level 20 — **done**; round 43 closed 2026-10-20: the art final, the voices, captures and build choices accepted; its two follow-ups (the swarm's two levers and the ridge's data key) accepted in round 44 (L rows) | Reverse scroll (signed scroll, TOP warnings, rules by travel direction); Mote Swarm pursuit; Rear-guard drone (its bay's weapons document); Hellas rim backdrop; track 8 for the retreat | A, B | 43 |
| **H** Level 21 + act end — **built**; round 44 closed 2026-10-20: the final art, voices, build choices (B rows) and the eleven questions answered as recommended (the wave rework to the rules, rings every 2.0 s, 5,600 HP, the safe spot's re-measure, the `hard_hp` key and the rest: **planned, not started**); K.b and K.c asked for changes (the vents become organs on the Colossus's spine, a death throe, the crawl halts while the Colossus is out: steps SIM-K, ART-K, GK, L21R) | Dust Colossus (the "Sinkhole colony": breach points, mouths, suction, ring); slow arena; Choir's first words; Act 3 outro and summary; Hellas basin backdrop | B (burrow), G | 44 |
| **I** Close-out — **done** (`ActPlaythroughTest`/`BalanceTest` through Level 21; the doc ticks and later tags; the playthrough save tool `./gradlew :content:playthroughSave`; the listen-through; the `EnemySpec` refactor; the user's playthrough on 2026-10-21; the `v0.1` release on 2026-10-22) | `ActPlaythroughTest`/`BalanceTest` through Act 3 (the same rules as Acts 1-2; the bound's tolerance never moves; at most three passes, else a round-45 finding), variety as built, later tags → Act 4 (the specials, allies and economy tables stay hand-written: the `later: Act 3` tag is dropped), the voice listen-through (one page of the voiced Act 3 lines in round 45), a tool that writes the medium playthrough save at the Level 15 hangar (one playthrough for M5 and M7), one refactor step (`EnemySpec` into per-behaviour spec records) before Act 4, the release **`v0.1` = Acts 1-3** tagged after part I, the playthrough fixes and the merge, blocked on the Mac check only if the user can open it within a day (else shipped marked untested); hard in Act 3 is reported in round 45 with no new ceiling test. Decided in round 44 (I.1-I.11) | all | 45 |
| **J** Act 4 design phase | After part I and the `v0.1` release: Act 4 "Deep Water" (Europa) as Act 3's design phase (the act README, the levels, the enemies, the story beats, concept art, one big round); nothing is built before the user approves it. Decided in round 44 (J.a) | I | 46 |

C is the riskiest part (terrain touches every system); it can start right after A if B's Burrower
slips. G's reverse scroll is independent of C–F and could run in parallel on its own files.

### M8 parts

Proposed by the Act 4 design phase (2026-10-21) and **decided in round 48** (2026-10-22, the act's design approved). Built like M7: each part is a commit series with its own gap check and a production-art concept round; E (the trench) can run beside D on its own files once C's under-water mode is in.

| Part | Scope | Depends on | Round |
|---|---|---|---|
| **A** Act 4 systems — **done**; round 50 closed 2026-10-22: the finals accepted, the sounds picked (the recorded picks from the Freesound previews until their originals), the seven calls accepted, the Level 23 dry shaft kept as approved (Q.1 a) | Under-water mode (medium field, trait factor, bullet speed, drag, beam reach, torpedo reach, section switch), special setting restrictions and the greyed HUD, Sonar Pulse, Decoy Flares, Harpoon Torpedoes, Depth Charges, pressure hull, Auto-repair nanites, Mk IV (20 MW, round 48 J1), the Bulwark heavy drone, Rook's per-level availability; `reference_dps` 22–28 and the Act 4 balance plan; flown on built levels with the `--medium underwater` debug option and on simulation fixtures | — | 50 |
| **B** Level 22 + Act 4 intro — **built** (2026-10-22, steps B1–B5, ART-B, AUD-B, VOX-B); [round 51](../../concept-rounds/round-51/README.md) closed (2026-10-22): the art accepted but the Act 4 title card (F.6 redo, a geyser added, round 52), Dr. Brandt cast a, the sounds picked, tracks 10 and 11 and the calls accepted; F.3: no deep layer | Act 4 `data.yaml` (title card, four pages, the Act 3 handover), ice surface backdrop, geysers, drill rigs, open leads, the Conamara voice | A | 51 |
| **C** Level 23 — **built** (2026-10-22, steps C1–C5b, ART-C, ART-T, AUD-C, VOX-C); [round 52](../../concept-rounds/round-52/README.md) closed (2026-10-22): the art accepted, the bursts a, the title card's geyser a, Thera Deep Control cast b, the sounds picked (the under-water explosion family S.10 asked again in round 53), the AI-made r04-a replaced, the calls accepted | The descent shaft (`Terrain`), the waterline switch, the under-water render grade and particles, Eel Swarm, the tiny school, Thera Deep's farms | A, B | 52 |
| **D** Level 24 — **built** (2026-10-22, steps D1–D5, ART-D, AUD-D, VOX-D); [round 53](../../concept-rounds/round-53/README.md) closed (2026-10-23): the art accepted, the spawning reef b, the sounds picked (rise b, glide a, fan b, death a, needle b, iris a, razed b, pulse a, hatch b, the explosion family a), hard's reefs allow 1, the other calls accepted, the Nautilus rule as decided | Abyss Ray, the small firing swimmer, Scuttler and Reef Spitter on the sea floor, spawning reefs, beacons | C | 53 |
| **E** Level 25 — **built** (2026-10-23, steps E1–E6, ART-E, AUD-E, VOX-E); [round 54](../../concept-rounds/round-54/README.md) closed (2026-10-23): F.3–F.7 accepted, the backdrop and the trench's darkness to be redone (round 55), the sounds picked (ring a, slow b, death a, spin a, uncoil a, Nautilus death b, bed a), C.1–C.8 accepted | Trench walls and side caves, Siren (slow), Spiral Nautilus | C | 54 |
| **F** Level 26 — **built** (2026-10-23, steps F1–F6, LX, ART-F, AUD-F, VOX-F); [round 55](../../concept-rounds/round-55/README.md) closed (2026-10-24): Level 25's redo and F.1–F.4, F.6–F.8 accepted, the sounds picked (spin, click, static and the warm pad b), C.2/C.4 changed after the playtest and built in step FX (the hits cut Rook's line, his spin on the screen), F.5 shown again in round 57 | Darkness for every unit, Glow Angler, the Choir's words, Rook's shoot-down | C, A (heavy drone) | 55 |
| **AW** Under-water weapons rework — **decided by the user (2026-10-23, playtest of Levels 22–25), built before part G**; built (2026-10-24), [round 56](../../concept-rounds/round-56/README.md) closed (2026-10-24): all accepted, C.3 keep, Q.1 yes (the strip names the energy weapons, step FX) | Energy weapons (Ion Beam, Lance Laser, Rear Lance) nearly useless under water (proposed ×0.2 damage, ×0.4 reach); the Harpoon Torpedoes tuned for swarms (faster rate, about 2 → 3 a second, about +50 % travel speed, blast 20 → 40 px, less damage a torpedo, homing only in a narrow ±15° cone ahead); the Depth Charges thrown forward, sinking ahead and bursting on their fuse or on touching a swimmer (hitting swimmers, ground and `sub`); the Act 4 intro briefing (and the hangar notice before Level 23) warning to keep credits for under-water weapons; the balance plan without selling (the beam kept for later levels; the Harpoon L1 bought at Level 22, the Harpoon L1 and the Depth Charges fitted at Level 23, upgraded at Level 24). No extra prevention tests (the user: "You just have to play it to see"); the Nautilus and the micro-missiles' angle unchanged until replayed | F | — |
| **G** Level 27 — **built** (2026-10-25, steps M8-PG, G1–G5, ART-G, AUD-G); [round 57](../../concept-rounds/round-57/README.md) closed (2026-10-25): all accepted (Level 26's Rook loss and the waterline strip, the art final, C.1–C.7), sounds bed a, tell b, eruption a, bend a, launch a, run b, impact b, hum a, break b, crack a, hull break a, scan b (the b picks in the game since step H5) | Depth Hunter (hint 3), homing torpedoes and flares, heat plumes, Rook's absence, Vorne's motif | F, AW | 57 |
| **H** Level 28 + act end — **built** (2026-10-26, steps M8-PH, H1–H7, ART-H1, ART-H2, AUD-H); [round 58](../../concept-rounds/round-58/README.md) closed 2026-10-26 (the Maw's and the cruisers' art redone in round 59, the timing of the breakout's burst and three b sounds to build) | Abyssal Maw, the breakout, the reveal set piece (cruisers), the Act 4 outro, the release end moves to Act V | G | 58 |
| **I** Close-out — **built** (2026-10-26, step I3: `ActPlaythroughTest`/`BalanceTest` through Level 28 on the levels' own income, part A's temporary `budget(n)` removed; Act 4's variety as built; the `later` tags to Act 5; the playthrough save at the Level 28 hangar; the listen-through's rows; step I4: the full `check`, the captures of the round 58 fixes); [round 59](../../concept-rounds/round-59/README.md) open (2026-10-26) | Playthrough and balance tests through Act 4 (the temporary budget(n) income of part A removed), variety as built, `later` tags → Act 5, the voice listen-through | all | 59 |

## Implementation

- [x] M0 Skeleton
- [x] M1 First flight
- [x] M2 Level 01
- [x] M3 The campaign loop
- [x] M4 Act 1 (built, parts A–H; concept round 26 closed and every M4 part's art final; the
      balance tests pass in part H's builds; the user played the build — its one finding, the
      Choir's silent stage directions, fixed in round 29 —; CI green on the branch; merged into
      main on 2026-10-06)
- [ ] M5 Act 2 (parts A–I under *M5 parts*; part A done, concept round 28 closed; part B, Level 08
      and the Act 2 intro, done, concept round 30 closed; part C, Level 09, done, concept round 31
      closed; part D, Level 10, done, concept round 32
      closed; part E, Level 11, done, concept round 33 closed; parts F, Level 12, G, Level 13, H,
      Level 14 and the act end, done, the combined concept round 34 closed on 2026-10-12; part I, the
      close-out, built, the voice listen-through done, the user's playthrough of Act 2 done and accepted
      (2026-10-20); open: the `sub` layer's CI check on three OSes after the push)
- [ ] M6 Acts 1–2 release: done; the `v0.1` tag carrying Acts 1–3 was made on 2026-10-22 (still open: the arm64 app on a real Mac, R.5, shipped marked untested, and round 34's other leftovers R.1–R.3, not verified here); the tag's plan: after M7 part I (round 44 I.2; Q11 settled 2026-10-19: *Terran Vanguard*,
      repository `terran-vanguard`; merged into main as signed a45e75e); concept
      round 35 closed on 2026-10-12, every part accepted as built (part A, mouse support on every
      out-of-game screen with the glass pointer, [ui](../../ui/README.md#mouse); part B, the high
      scores and the release's end, [scoring](../../systems/scoring/README.md#high-scores); part C,
      the release plumbing, [release](../release/README.md); part D, the polish and close-out, the
      per-level bench in [tech](../README.md#per-level-bench-m6-part-d)); the pointer and the
      application icon final, the game's own assets Apache-2.0 (Q13). Still waiting for round 34's
      leftovers and the user: the credits roll's regeneration (R.1), the placeholder audit's list
      (R.2), the Act 2 playthrough's fixes (R.3) and the arm64 app on a real Mac (R.5)
- [ ] M7 Act 3 (parts A–I under *M7 parts*; the design approved in concept round 36 on 2026-10-13;
      part A, the Act 3 systems and arsenal, done: concept round 37 closed on 2026-10-13, every pick
      built (R.1's act factor and R.4 c in step Q1, the picks in step Q2), pending the user's
      playthrough; part B, Level 15 and the Act 3 intro, done: built on 2026-10-14 (steps B1–B5), concept round 38
      closed the same day, its picks built (step R38C), pending the user's playthrough; part C, Level 16,
      built on 2026-10-14 (steps C1–C5, G1, G2, ART-C, AUD-C; voices step VOX-C), concept round 39 closed on
      2026-10-18 (step R39C), done except R.1's redo of the canyon walls' look (step CW, in progress, to round
      40), pending the user's playthrough; part D, Level 17, built on 2026-10-18/19 (steps D1–D5, G1, G2, ART-D,
      AUD-D, VOX-D), concept round 40 closed 2026-10-19; parts E (Level 18), F (Level 19), G (Level 20) and H (Level 21 and the act end) built and their rounds 41-44 closed by 2026-10-20 (round 44's K.b and K.c changes and the Q rows' reworks are planned: steps SIM-K, ART-K, GK, L21R); part I, the close-out, done: the user's playthrough of Act 3 on 2026-10-21 and the `v0.1` release = Acts 1-3 on 2026-10-22; open: Levels 18 and 19's "Easy/hard variations" items and the act's "All 7 levels" item)
- [ ] M8 Act 4 (design decided in round 48; parts A–I under *M8 parts*; the design phase of 2026-10-21 wrote the act, its seven levels, the units, the boss and the systems as drafts; parts A–H built and their rounds 50–58 closed, part I built on 2026-10-26; open: round 59 (the Maw's and the cruisers' art redo, the Act 4 listen-through, hard's Act 4 economy) and the user's playthrough of Act 4)

## Open questions

- None open.

## Decisions

Entries dated before 2026-10-21 (M8's start): [decisions-archive.md](decisions-archive.md).

- 2026-10-22 (user): M7 part I done: the playthrough of Act 3 (2026-10-21) and the release `v0.1` = Acts 1-3 (no Mac check, so the release notes keep the "untested on Apple silicon" mark). M6's tag is made; M6 and M7 stay unticked for the open items named in their rows (M6: R.5 and round 34's leftovers; M7: Levels 18/19 variations).
- 2026-10-21 (Act 4 design phase, step m8-dact; **pending the user's review in round 48**): the M8 row and its *M8 parts* A–I proposed (the gap analysis's plan); nothing is built before the design is approved.
- 2026-10-22 (M8 part A, step A2): the *M8 parts* table follows round 48: the parts' rounds renumbered (round 49 was an
  Act 3 sound redo: part A → 50 … part I → 58), the "pending round 48" wording dropped, the Auto-repair nanites and
  the Mk IV's 20 MW named in part A, part A flown on built levels with a debug option (D1 of its gap check) and part I
  removing part A's temporary budget(n) income.
- 2026-10-22 (M8 part A, step A7): part A **built** (steps A1–A6, ART-A, AUD-A; the full `check`, `tools/balance.py` and
  `tools/check_docs.py` clean); the parts' checklists ticked with their tests, the steps' small rules logged in the
  parts' *Decisions* as pending the user's review in round 50; concept [round 50](../../concept-rounds/round-50/README.md)
  opened (the finals with game captures, eight sound pairs, seven calls the build made, part C's dry-shaft question).
- 2026-10-22 (user, round 50, closed): part A **done**. All art accepted (F.1–F.7, K.1); the sounds picked (harpoon launch b, run a, hit a; depth-charge roll-out b, burst a; the Torpedo Pod keeps r03-a; the Sonar Pulse keeps r04-a; limit chime b), in the game from step R50C; the seven calls B.1–B.7 and the steps' logged small rules accepted; Q.1 a: Level 23's dry shaft stays as approved, part C measures it with `levelReport` and the autopilot.
- 2026-10-22 (M8 part B, step B5): part B **built** (steps B1–B4, B3b, ART-B, AUD-B, VOX-B; the full `check`,
  `tools/balance.py` and `tools/check_docs.py` clean); Dr. Brandt's secondary line made true on every difficulty and the
  ice-crawler's drop moved onto the road; the parts' checklists ticked with their tests, the steps' small rules logged in
  the parts' *Decisions* as accepted in round 51 (2026-10-22); concept [round 51](../../concept-rounds/round-51/README.md)
  opened (the finals with game captures and the deep-layer question, Dr. Brandt's casting, seven sound pairs, tracks 10
  and 11, seven calls the build made).
- 2026-10-22 (user, round 51, closed): part B's art accepted (F.1, F.2, F.4, F.5, F.7, F.8; the Drill Rig, Level 22 and the briefing, hangar parts are `art: final`); **F.3 keep** (no deep layer under the shelf; Level 22's design text matches); **F.6 redo** (the title card gets a giant geyser, round 52); Dr. Brandt is Margaret Espaillat (V.1 a); sounds: geyser tell b, burst a, rig collar b, ring b, death b, ice bed a, station bed a (the b files of S.1, S.3–S.5 replace the a files in the next code step); tracks 10 and 11 and the seven calls accepted.
- 2026-10-22 (M8 part C, step C5b): part C **built** (steps C1–C4, C5a, C5b, ART-C, ART-T, AUD-C, VOX-C; the full `check`,
  `tools/balance.py` and `tools/check_docs.py` clean): Level 23 *Through the Ice* with the dry shaft, the waterline switch,
  the under-water grade, the Eel Swarm, the Glassfin, the siphon pumps and Thera Deep; the parts' checklists ticked with
  their tests, the steps' small rules logged in the parts' *Decisions* as accepted in round 52 (2026-10-22); the
  frame rate of a full medium run 291 fps (longest frame 56.9 ms) on the software renderer; concept
  [round 52](../../concept-rounds/round-52/README.md) opened (the finals with game captures, the under-water bursts a/b,
  the Act 4 title card's redo, Thera Deep Control's casting, nine sounds and the under-water explosion family, seven
  calls the build made).
- 2026-10-22 (user, round 52, closed): part C's art accepted (Level 23, the Eel Swarm, the Glassfin and the siphon pump
  `art: final`); the under-water kill bursts a; the Act 4 title card's geyser a; Thera Deep Control cast b; the sounds
  picked (waterline a, refit a, shatter b, eel pulse b, segment b, pump hum b, break b, scrape a, bed a; in the game from
  step D4); the under-water explosion family (S.10) asked again in round 53; the AI-made `explosion-underwater-r04-a`
  replaced (Q.1); the seven calls and the steps' logged small rules accepted.
- 2026-10-22 (M8 part D, step D5): part D **built** (steps D1–D4, D5, ART-D, AUD-D, VOX-D; the full `check`,
  `tools/balance.py` and `tools/check_docs.py` clean): Level 24 *Abyssal Plain* with the Abyss Ray (the shadow, the rise,
  the fans, the escorts), the Barbfin, the spawning reefs and their secondary, the beacons, the *Meridian* wreck and the
  plain's grade by section; the parts' checklists ticked with their tests, the steps' small rules logged in the parts'
  *Decisions* as accepted in round 53 (2026-10-23); the frame rate of a full medium run 419 fps (longest frame
  46.6 ms) on the software renderer; concept [round 53](../../concept-rounds/round-53/README.md)
  opened (the finals with game captures, the spawning reef's look a/b, nine sound pairs and round 52's under-water
  explosion family, the calls the build made, and Level 25's one rule ahead).
- 2026-10-23 (user, round 53, closed): part D's art accepted (Level 24, the Abyss Ray, the Barbfin and its violet needle,
  the props, the briefing images and the intel portraits; the Abyss Ray, the Barbfin and the spawning reef are
  `art: final`); the spawning reef is look **b** "bone chimneys" (a to `concept/rejected/`); the sounds picked (the Ray's
  rise b, glide a, fan b, death a, the Barbfin's needle b, the reef's iris a, razed b, the beacon pulse a, the
  *Meridian*'s hatch b, the under-water explosion family a); **C.1 allow 1** (hard's REEFS asks for 3 of 4); C.2-C.7
  accepted; **A.1 as decided** (the Nautilus rule); the steps' logged small rules accepted.
- 2026-10-23 (step E6): M8 part E built (steps E1–E6, ART-E, AUD-E, VOX-E): Level 25 *Siren Trench* flown, drawn and
  heard: the trench's walls with the 25 side caves (`terrain.notches`, cave waves warned 3 s at their wall), the rock
  pillar's fork and the 190 px narrows, the Siren's wall nests and their slowing sonar rings on the ship and Rook, the
  Spiral Nautilus pairs orbiting the ship (clamped to the channel, armoured while closed, cracked by `anti-sub` fire),
  the survey sub (the secret) and the survey buoy's armour patch, the kill-ratio secondary; the parts' checklists ticked
  with their tests (`Level25Test`, `SirenTest`, `SpiralNautilusTest`, `CaveEntryTest`, `TerrainNotchTest`,
  `Level25LooksTest`, `Level25SoundsTest`, `RadioTimelineLevel25Test`), the steps' small rules logged in the parts'
  *Decisions* as accepted in round 54 (2026-10-23); the frame rate of a full medium run 190 fps on the software
  renderer (the longest frame 73–90 ms, the level's first frame; none of 50 ms or more after it); concept
  [round 54](../../concept-rounds/round-54/README.md) opened (the finals with game captures, the trench's darkness,
  seven sound pairs and the calls the build made).
- 2026-10-23 (user, round 54, closed): part E's art F.3–F.7 accepted as final (the Siren, the Spiral Nautilus, the props,
  the briefing images, the intel portraits); **F.1/F.2 redo** (Level 25's backdrop is too dark and the cave edges can
  hardly be seen, the fork's start too sharp: the dark ambient kept, headlights that light the world, a faint rim light on
  the walls' edges and cave mouths, the pillar's head rounded; step LX, shown again in round 55); the sounds picked
  (the Siren's ring a, slow **b**, death a, the shell spin a, uncoil a, the Nautilus's death **b**, the bed a); C.1-C.8
  accepted; the steps' logged small rules accepted.
- 2026-10-23 (step F6): M8 part F built (steps F1–F6, LX, ART-F, AUD-F, VOX-F): Level 26 *Blackwater* flown, drawn and
  heard: the dark as Level 26's values on step LX's light map (the ambient ramp 20 → 6 %, the hard 70° × 260 px cones
  with a faint beam haze, a 15 % rim per unit, the static lamps, Rook's 56 px lamp, every unit whole in the Sonar
  Pulse's window, the Dark levels assist), the Glow Angler's lure and lunge, the glow-only Driftjelly and Eel Swarm, the
  fault scarp's three notches and Siren nests, the Choir's line with its HUD static and the music's duck, Rook's
  scripted loss (the trails, the hit, the spin-down, the silence, the vents' warm pad, `rook_missing` at the win and the
  debrief's `ROOK  MIA`), the farm barge (the secret) and the kill-ratio secondary; the parts' checklists ticked with
  their tests (`Level26Test`, `GlowAnglerTest`, `RookLossTest`, `Level26LooksTest`, `RookLossLooksTest`,
  `Level26SoundsTest`, `StaticLeadsTest`, `RadioTimelineLevel26Test`), the steps' small rules logged in the parts'
  *Decisions* as accepted in round 55 (2026-10-24); concept [round 55](../../concept-rounds/round-55/README.md)
  opened (Level 25's redo, the finals with game captures, the sounds and the warm pad, the calls the build made).
- 2026-10-23 (user, playtest of Levels 22–25): **decided, to be built next as rework AW, before part G** (see the *M8
  parts* row AW): the energy weapons nearly useless under water, the Harpoon tuned for swarms with a narrow aim cone,
  the Depth Charges thrown forward, the Act 4 briefing's warning, no selling in the balance plan; the numbers are
  changed in that step, not before.
- 2026-10-24: rework AW built (steps AW-P, AW1–AW4, VOX-AW): energy ×0.2 damage / ×0.4 reach under water, the swarm-killer
  Harpoon, the Depth Charges tossed forward, the tougher Barbfin and retuned Levels 23–26, the plan without selling, the warnings
  (intro, briefing, radio, hangar notice); concept [round 56](../../concept-rounds/round-56/README.md) opened (the numbers, the
  beam against Level 26, the texts, one question); part G has not started.
- 2026-10-24 (user, rounds 55 and 56 closed; the playtest of Levels 22–26 with the rework: "the new weapon setup works
  much better"; at medium with everything at L5 a bit overpowered, put down to the player being too good for medium: no
  balance change): step FX built the playtest's fixes: Rook's cut line before the hits and the hits cutting it, his spin
  on the screen, the rest of Level 26 6 s later; the replay hangar showing the replayed mission's escort (the shop
  showed the Bulwark for every older mission); the depth charge's burst as the under-water large explosion; the default
  volumes effects 50 %, radio blips 90 %; round 55's sound picks and round 56's Q.1 (the waterline strip's `BEAMS ×0.2`).
  Round 55's F.5 (Rook's loss) is shown again in round 57 with part G.
- 2026-10-25: part G built (steps M8-PG, G1–G5, ART-G, AUD-G): Level 27 "Hydrothermal Vents" in the data, flown and
  drawn: the Depth Hunter's columns with their homing torpedoes, the heat plumes (tell, eruption, burn, the 25° bend),
  the Bulwark's vent-field play, the hull-code wreck with the scan ping and Vorne's motif alone, the relay beacons as the
  secondary, the *Marlin*, the gate; the balance plan refits the Tail Gun at Level 27 (Q.1 a); production art, 24 sound
  proposals (the a variants as stand-ins), the voice; full `check` green. Concept [round 57](../../concept-rounds/round-57/README.md)
  opened (Level 26's Rook loss again and the waterline strip, the art finals, the sounds, the calls).
- 2026-10-26: part H built (steps M8-PH, H1–H7, ART-H1, ART-H2, AUD-H): Level 28 "Abyssal Maw" in the data, flown and
  drawn to the release's end: the hatchery approach, the Choir, the Abyssal Maw on its halted arena (the coil, the gill
  sacs, the throat, the fan walls, the sweeps, the one dive and lunge, the Helix implants, the death), the shaft, the
  breakout into the air, the Ascendancy cruisers' rise and run (broadsides, flak, point defence; the armour floor at 1
  from the kill), the orbit; the debrief with `ACT IV COMPLETE`, the Act 4 outro and the release end with Act V's name.
  Production art, 19 sound pairs and track 30 (the a variants as stand-ins), the voice; Level 26's last line made its
  level-end line (step H7); Level 28's atlases 7 pages, one over the budget (accepted in round 58 (2026-10-26)); full `check` green.
  Concept [round 58](../../concept-rounds/round-58/README.md) opened (the art finals with captures, the sounds, Q.1 the
  Maw's music, the calls) and closed 2026-10-26: F.1, F.4-F.8 accepted (F.4 with a timing fix for the next code step: the
  burst at the moment the ship breaches the surface, wherever it is), F.2 (the Maw) and F.3 (the cruisers) redo in round
  59, Q.1 a, C.1-C.8 accepted, sounds a except S.13, S.14 and S.16 b (to put into the game in a later step), track 30
  accepted.
- 2026-10-26 (step I3): part I built. Part A's temporary income is gone (`Unmapped` and `BalanceTest`'s and
  `tools/balance.py`'s `budget(n)` fallback for an Act 4 level without data: every planned level earns its typical
  haul); the returning units' time-to-kill check and `tools/balance.py`'s act HP and beam-era tables run through
  Level 28. No tuning pass was needed: the campaign playthrough ends Act 4 at -2.3 % of the expert's bound on medium
  and +8.8 % on easy; hard ends 49.4 % under (13,164 against 25,991), still the pending round-59 finding (Act 3's
  deficit carried in, the autopilot's lower grades; a level's `bounty_scale`, the plan's purchases and the prices
  move the bound and the playthrough together). Level 28's beam-era time stays 1.52× (allowed 1.6×). Act 4 passes
  the variety checklist as built; the `later: Act 4` tags are resolved or moved to Act 5;
  `./gradlew :content:playthroughSave -Plevel=28` writes the save at the Level 28 hangar; `tools/art/voice.py`'s inner
  Gradle run isolated; the 119 voiced Act 4 lines checked against a Whisper transcript for round 59's listen-through.
- 2026-10-26 (step I4): the full `check` green, `tools/balance.py` and `levelReport -Plevel=28` clean; game captures of
  the round 58 fixes (the Maw's arrival, sweep, dive and corners; the cruisers' rise; the breakout's burst for a ship at
  the left, the middle, the right and moving) and the ambience beds' A/B renders; concept
  [round 59](../../concept-rounds/round-59/README.md) opened (R.1–R.3 the redone art, V.1 the Act 4 listen-through, Q.1
  the beds' volume, C.1–C.7), M8's last round.
