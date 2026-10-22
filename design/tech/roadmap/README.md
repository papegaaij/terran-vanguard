---
title: Implementation roadmap
design: approved
implementation: in-progress
art: n/a
depends-on: [../architecture, ../../campaign]
updated: 2026-10-22
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

## Open questions

- None open.

## Decisions

- 2026-10-01: Drafted after the tech stack was approved.
- 2026-10-01: User decisions: a branch per milestone, merged after the user has played the
  build; production art starts after M2.
- 2026-10-01: Approved by the user.
- 2026-10-01: M0 Skeleton done: played and accepted by the user, CI green on three OSes.
- 2026-10-01: M1 First flight done: played and accepted by the user.
- 2026-10-02: M2 Level 01 done: played and accepted by the user (data files, level runner, Level 01 with backdrop, HUD and debrief). Production art can start now (see Rules).
- 2026-10-02: M3 part B1 built (round 11 wiring, campaign state, saves, briefing, mission failed and
  game over, a placeholder hangar); part B2 builds the hangar.
- 2026-10-02: M3 part B2 built: the hangar (shop, loadout with the power budget, repair, undo,
  intel by sensor level, save, launch with warnings), the fitted Pulse Cannon level, shield,
  plating and engine flying, and the six B1 follow-up decisions. M3 stays open for the user's
  review; the other weapons, specials, utility effects and the spare-power bonus fly with M4.
- 2026-10-02: Mouse support for the menus, hangar and options added to M6 (user decision).
- 2026-10-02: M3 close-out (user decision): later-milestone items are marked `later: Mn` in their documents (rule under *Rules*) and added to the M4 and M5 rows; the sound test and the transmission static are built; the portrait expressions, briefing images and the intel's L2 portraits go to the art track. Every M3 document is now `done` under that rule; M3 stays open until the user has played the build.
- 2026-10-02: M3 The campaign loop done: played and accepted by the user (menus, options, briefings, hangar, saves, retry and game over); deferred items carry their `later:` milestone.
- 2026-10-02: M4 plan (user decisions): parts A–H as under *M4 parts*; a production-art round per part; the wingman's escort slot moves to M5, since Rook joins at L08.
- 2026-10-02: M4 part A built: the Act 1 arsenal on Level 01 (`--loadout` to fly it before its shop levels), overdrive and the spare-power bonus in flight, the HUD's weapon rows, the effects proposed as final in concept round 14. Deferred items of acts beyond M6 are tagged with their act (rule under *Rules*).
- 2026-10-02: M4 part B built: Level 02 playable from Level 01 in the campaign (`--level 2` to start there), with its production art proposed in concept round 15.
- 2026-10-05: M4 part G started (user decisions D1–D4, see [Level 07](../../campaign/act-1-first-contact/level-07-brood-carrier/README.md)): the Brood Carrier comes over, stops, descends and turns broadside through pre-rendered frames; a 35 s aftermath, then the debrief with the act summary, the act outro (four voiced pages with an image each) and the hangar before L08 (not built yet until M5; the Act 2 title card comes with Act 2's intro); the Decoy Flares leave part G (their unlock moves to L27, `later: Act 4`). The `space` layer and the Bomb Rack's NO GROUND items move from M4 to Act 5.
- 2026-10-05: M4 part H started (user decisions on the close-out): **D2** the Pickup magnet's
  radius and the sensor suite's L2 threat arrows are built in H; the Targeting computer stays out of
  the shop until M5 (its L06 data-core unlock stays in the save). **D3** the per-bullet difficulty
  tags are closed as met by the per-enemy `difficulty:` overrides and the waves' `skip`
  ([difficulty](../../systems/difficulty/README.md)). **D4** Dr. Varga gets one text line per sensor
  level per level. **D5** the credits screen, generated from CREDITS.md, is built in H; the
  high-score table moves to M6 (row above). **D6** the music files are checked against the final
  spec and offered as they are in concept round 26. The docs are reconciled with the build: built
  items ticked, half-built ones split, later ones tagged with their milestone or act.
- 2026-10-05: Concept round 26 closed (user: accepted as proposed): M4 part H's art is final, so
  every M4 part's production art is final and Act 1's `art` is `final` (no placeholders). M4's
  done-criteria now: the Act 1 documents are `done` (every item ticked or tagged later), except the
  [Skitter](../../enemies/air/skitter/README.md)'s "entry paths authored as data" item (the snakes
  and streams are still laid out in code by `Formations`, open since M2) and the hangar's open
  point on the third utility bay (part H's default data entry with `available: act 3` is not made
  yet); the balance tests (`BalanceTest`, `ActPlaythroughTest`) pass in part H's builds; CI
  on the branch was red on macOS until part H's fix, and the run for the part H commit had not
  finished at the close; **the user has not played the M4 build yet**. M4 stays open until then.
- 2026-10-06: M5 plan, from the gap analysis of the `m5-act-2` branch: parts A–I as under *M5
  parts*, concept rounds 28–36, one per part, as in M4. Part A started with the user's decisions
  D1–D7: **D1 = a** (part A builds Rook and the escort slot, the five Act 2 weapons that need no
  water, the Targeting computer and the Salvage scanner; the Torpedo Pod and the `sub` layer move
  to part E), **D2 = a** (Rook's kills pay like the player's), **D3 = b** (a hangar repair line for
  Rook, launch warning below 50 %), **D4 = a** (Rook's guns in an escort inventory at 60 %),
  **D5 = c** (the act HP factor only for returning `medium` and larger units from Act 2 on),
  **D6 = a** (the Targeting computer's HP bars, weak-point brackets and +20 % homing turn),
  **D7 = a** (the Salvage scanner's +10 / +20 % and glint built in part A); the gap analysis's
  stated defaults apply (save format 3, Rook only in levels from 08, `--escort`, the barks' queue
  rules, data in Act 1 terms). The Targeting computer moves from part H to part A. Details are in
  [wingmen](../../player/wingmen/README.md), [ship systems](../../player/systems/README.md),
  [enemies](../../enemies/README.md) and [economy](../../systems/economy/README.md).
- 2026-10-06: M5 part A done: concept round 28 closed with everything accepted (the mine's arming
  beep a, the secret glint a). Rook and the escort slot, the five Act 2 weapons that need no water,
  the Targeting computer and the Salvage scanner (their design rows now `approved`), the act HP
  factor and the boss-bounty rule are built and reviewed; Rook's sprites, the five weapons' effects
  and the glint are final. Two wingmen items stay open: the guns and barks tables rendered from the
  data (not built), and the balance item (`BalanceTest` with his DPS, the plan buying his guns, the
  autopilot and `ActPlaythroughTest` flying with him), tagged **later: M5 part I** with the
  close-out's Act 2 balance tests. The next part is **B**, Level 08 and the Act 2 intro (round 29).
- 2026-10-06: M4 done: the user played the build (one finding, the Choir's stage directions made no
  sound: fixed with a stage sound, concept round 29), CI green on m4-act-1; merged into main.
- 2026-10-06: The M5 concept rounds shift by one: round 29 went to the Choir's stage-sound fix on
  m4-act-1 (merged into main with M4), so part B's round is 30 and parts C–I's are 31–37 (the
  *M5 parts* table).
- 2026-10-06: M5 part B started, from its gap check, with the user's decisions D1–D6:
  **D1 = a** (the megacity's towers in true perspective as scenery only, drawn by the renderer;
  turrets, Creepers, ground targets and crates at street level and on low structures drawn
  without lean; no wall crawling: [art direction](../../art-direction/README.md#parallax-layer-model)),
  **D2 = a** (civilian traffic is backdrop scenery, shots pass through), **D3 = a** (the Act 2
  intro texts rewritten: about four act briefing pages and two Level 08 pages, no repetition of
  the Act 1 outro, the contradictions fixed), **D4 = a** (a radio event `escort-first-kill` for
  Rook's first kill and `{side}` line variants: [wingmen](../../player/wingmen/README.md)),
  **D5 = a** (the opener shortened to about 10 s, the level densified with its own units, the
  Creepers 12 → 15) and **D6 = a** (the chosen concepts and the four new backdrop sections
  straight to production; a/b only for the billboard, the traffic and the Ikoyi shelter
  civilian's voice). The gap check's stated defaults apply: the data in Act 1 terms (crate 100,
  secondary 56), no act HP factor on Level 08's units (all `tiny` or `small`); Rook's scripted
  flank line dropped, no glide-in, his side prompt at about t=6; the radio retimed to the 1 s
  rule; the Creeper's weak point drawn only, its fan aimed, the 0.5 s stagger, a 16-heading husk
  and the screech; ambience in section 1, the "Homefront" base stem from section 2 and the full
  mix in section 5; the Act 2 title card over its still with one image per act page and two for
  Level 08's briefing; an M4 save already at the Level 08 hangar skips the Act 2 intro (a new
  campaign or `--level 7` shows it); Act 2 keeps the Act 1 tactical map. Details in
  [Act 2](../../campaign/act-2-homefront/README.md), its
  [Level 08](../../campaign/act-2-homefront/level-08-neon-skyline/README.md), the
  [Creeper](../../enemies/ground/creeper/README.md), [music](../../audio/music/README.md),
  [sfx](../../audio/sfx/README.md), [voice](../../audio/voice/README.md) and the
  [schemas](../architecture/README.md#data-file-schemas). Part B's concept round is 30.
- 2026-10-07: M5 part B done: concept round 30 closed (user, 2026-10-07). The production art
  approved as final, the billboard **a** (the neon sky-sign), the traffic **a** (the wedge cars
  and the CDF gunship) and the Ikoyi shelter civilian **b** (Faith Abiola-Ellison) chosen and
  produced at the close; the captures, the voiced lines, the texts (Level 08 and Act 2 now
  `approved`), the part B numbers and the D1–D6 checklist accepted; the rule "from Act 2 on, an act
  page and a level page each fit one screen" kept, and the hangar teaser's role labels kept
  (`Speaker.role`). The next part is **C**, Level 09 (round 31).
- 2026-10-07: M5 part C started (Level 09), from its gap check, with the user's decisions D1–D11:
  **D1 = a** (a missed hive node fails the mission at once, as Level 05; no rear stream, no
  flown-on mission), **D2 = a** (in a hold zone the level clock slows with the scroll: the level's
  times are script time), **D3 = a** (no hold timeout; the hold speed by difficulty, 20 / 30 /
  40 px/s), **D4 = a** (the Ravager is `air` for the middle 0.3 s of its pounce), **D5 = a** (the
  collapse is a simulated sweep that kills the ground units under it and pays like Airstrike
  kills), **D6 = a** (the bridge secondary counts the two tagged bridge waves), **D7 = a** (a
  `required` trait warns at launch at every sensor level, counting Rook's Mortar and the specials),
  **D8 = c** (the balance plan's L09 visit: a Bomb Rack for the left Autocannon and Rook's Mortar,
  the Tail Gun to L10; a hardened check in `BalanceTest`), **D9 = a** (art straight to production
  in the last hour of the night; a/b only for the collapse's look, the Kilo Lead's voice and the
  pounce and collapse sounds), **D10 = a** (a unit several levels use goes into each of their unit
  atlases) and **D11 = a** (the Kilo Lead auditioned a/b in round 31). The gap check's stated
  defaults apply: weak points drawn only (the node's ×2 and the Ravager's ×1.5 struck); hardened
  flown for enemies, homing shots and Rook skipping them unless anti-ground; the node's spawn every
  4 s (2 Skitters, hard 3; shut within 96 px; one opening per node in the checks); six named groups
  and the `NODES A1 … C2` tracker; `pack` in the vocabulary; the data in Act 1 terms with a
  `bounty_scale`; the Creeper's act HP factor; density from the level's own roster; the radio
  retimed with the events `hold-start`, `first-pounce`, `collapse` and `requires: escort`; two
  one-screen briefing pages, Rook's teaser, Varga's four lines; the Airstrike charge only with a
  special; the cocoon on a low roof; the trucks as scenery and nothing on `far`; the base stem from
  the launch and the full mix in the holds and from the collapse; moving scenery on the real clock;
  no hold indicator; the Act 2 README's fixes. Details in
  [Level 09](../../campaign/act-2-homefront/level-09-arcology-fall/README.md), the
  [Hive Node](../../enemies/ground/hive-node/README.md), the
  [Ravager](../../enemies/ground/ravager/README.md), the
  [schemas](../architecture/README.md#data-file-schemas), the [hangar](../../ui/hangar/README.md),
  the [HUD](../../ui/hud/README.md), [music](../../audio/music/README.md),
  [sfx](../../audio/sfx/README.md), [voice](../../audio/voice/README.md) and the
  [production plan](../../art-direction/production/README.md). Part C's concept round is 31.
- 2026-10-07: M5 part C: concept round 31 closed but for row 4 (user, 2026-10-07). The Hive Node,
  the Ravager, Level 09's backdrop with the Kilo trucks and the cocoon, the two briefing images and
  "Firestorm" with its base stem approved as final; the collapse sound **a** (CC-BY, on the credits
  roll), **both** Ravager pounces kept (one picked at random per pounce) and the Kilo Lead **a**
  (Aaron Bennett) chosen and produced; the capture, the 20 voiced lines, the texts (Level 09 now
  `approved`), the part C numbers, the D1–D11 checklist and the six build choices (a)–(f) accepted.
  The collapse's look: both **a** (topples across) and **b** (pancakes into a dust wave) rejected;
  the user asked for a rework (it sways a little, then collapses straight down; its shadow moves
  with it; dust in every direction from just before the impact and a large dust blast at it; no
  sharp dust edge), variant c in progress, and the collapse's kills then spread outward from the
  tower's foot at the impact over about 1 s instead of sweeping left to right (planned, built after
  c is approved). Part C is done once row 4 closes.
- 2026-10-07: M5 part C done, concept round 31 closed (user): the collapse's look **c** approved
  with two tweaks (the shadow halved; a single lean to the right instead of a sway) and built in the
  game (the lean and the drop by the tower projection, the cast shadow, the puff sprites, the
  rubble heap, the kills outward from the tower's foot over 1 s, the sound's crash at the impact);
  hold C lasts until the collapse's dust has settled (9.0 s after the warning starts, no longer the
  blast's end at 4.0 s), so the heap stays in sight after a late kill. The next part is **D**,
  Level 10 (round 32).
- 2026-10-08: M5 part D started (Level 10), from its gap check, with the user's decisions D1–D12,
  all (a): **D1** the shuttles hold authored stations in a band with a slow lane sway and never
  react to threats; **D2** every enemy bullet and contact hurts a shuttle, tuned until the
  autopilot keeps at least 3 of the 4 saveable on medium; **D3** the tracker `SHUTTLES n / 4` over
  five armour bars; **D4** Lifeline Three untouchable before t=118 (fire passes through), the
  scripted loss costing nothing; **D5** the mission fails at once when the four saveable shuttles
  are lost, the escort's pay per shuttle home, no secondary, four level-end lines; **D6** the Wraith
  as its stat block (cloaked from the top, rear entry, decloaked exit along a side lane); **D7** the
  Wraith's HP 27 → 16, a rear check in `BalanceTest`, the plan's L10 visit refitting Rook's
  Autocannon (a `fit` action); **D8** real flocking for the Mote Swarm (its route as data, the
  Skitter's authored-paths item for snakes and swarms); **D9** `required: [rear]`; **D10** art
  straight to production, a/b only for the shuttle and the loss's glow and lance; **D11** first
  light; **D12** two voices auditioned, Lifeline and Lifeline Three. The gap check's stated
  defaults apply: data in Act 1 terms with a `bounty_scale`; the waves filled with popcorn to at
  least 50 a minute and the 3 s pacing rule, the first enemies at t≈8; 190 px/s; 3 s edge warnings
  everywhere; directions by `from`, the looping swarms `from: front` with the share by threat in the
  text; the formations `swarm` and `rear ambush`; the Wraith's veins drawn only; hard's 7-shot
  bursts, 3.0 s holds, an extra pair at t=170 and two loop-backs; the radio retimed with the events
  `first-decloak`, `first-loop-back`, `scripted-loss` and `ally-lost`, Rook's t=50 line dropped, a
  mission-failed line, "two hundred and twenty"; two one-screen briefing pages, Rook's teaser,
  Varga's four lines; the shuttles untouchable in the liftoff and the climb-out, the glide into
  `far`, armour 180 / 120 / 90; the ferry hatch with a hidden crate; "Homefront"'s base stem from
  section 2, the full mix from section 3, the −6 dB duck, the ambience to the ocean at section 4; no
  change to Rook's AI. Details in
  [Level 10](../../campaign/act-2-homefront/level-10-evacuation-corridor/README.md), the
  [Wraith](../../enemies/air/wraith/README.md), the [Mote Swarm](../../enemies/air/mote-swarm/README.md),
  the [allies](../../allies/README.md), the [schemas](../architecture/README.md#data-file-schemas),
  the [HUD](../../ui/hud/README.md), [wingmen](../../player/wingmen/README.md),
  [music](../../audio/music/README.md), [sfx](../../audio/sfx/README.md) and
  [voice](../../audio/voice/README.md). Part D's concept round is 32.
- 2026-10-08: M5 part D done, concept round 32 closed (user). After the capture: the Wraith's
  bursts fly as a fixed 40° fan straight up through the shuttle band, not aimed, and the no-fire
  distance does not apply to them (user); the capture's bugs fixed (the popcorn routes round the
  band, the wreck's full 3 s glide, the ferry hatch at 50 HP with a 12 s crate, the rear banner
  above the band, the home bar in pale mint). Hard keeps 1 of 4 shuttles home on the autopilot
  (user: hard is meant to be hard). The next part is **E**, Level 11 (round 33).
- 2026-10-08: M5 part E started (Level 11), from its gap check, with the user's decisions E1–E10,
  all as recommended: **E1 = a** water is a level flag, `water: true` (Levels 11–13; the whole play
  field), so the Torpedo Pod runs only there and reads `NO WATER` elsewhere; **E2 = a** only
  `anti-sub` (and the Smart Bomb) reaches the `sub` layer, the weapons README's "and `area`"
  corrected; **E3 = b** submerged Driftjellies fire their ring too, the pulse showing through the
  water; **E4 = c** the water drawn as a hybrid, a generic run-time `sub` pass (tint, blur, wave)
  with pre-rendered collars, ripples and surfacing steps per unit; **E5 = a** each of the Kraken's
  slam arms owns a half of the four lanes, a severed arm making its half safe; **E6 = a** the
  Kraken's HP tuned to a ≈ 60 s fight with a window-aware duration in `BalanceTest`; **E7 = a** its
  two slam arms as runtime chains with authored motion, the gripping arms baked, the idle arms as
  `sub` shadows, the head pre-rendered; **E8 = a** the secondary all or nothing (no cargo ship sunk),
  decided at the Kraken's death; **E9 = a** art straight to production, a/b only for the ship pair
  and the lane telegraph and slam look; **E10 = a** Atlas Control auditioned a/b. The gap check's 28
  stated defaults apply: data in Act 1 terms with a `bounty_scale` (estimate 0.6–0.8); the Kraken's
  paid bounty ≈ 15 % of budget(11) ≈ 207 after the scale; no act HP factor; the waves densified to at
  least 50 a minute with the 3 s pacing rule, no rear waves, the introductions as short quiet
  windows; the formation `field`; submerged units scrolling with the sea; seeded jelly swap timers;
  torpedoes hitting `sub` and every `ground` unit over water at full damage; an idle pod drawing its
  power; ground-only blasts over water unchanged with a water splash; the Smart Bomb hitting `sub`;
  Rook skipping `sub`, keeping out of telegraphed lanes, hit by slams; the plan's L11 visit refitting
  Rook's Mortar, no torpedo; lane choice by the player's x; the convoy at screen stations, gliding to
  lanes 1, 2, 4 at the halt, the frigate dropping back; the frigate's flak presentation only; the
  arena's jelly field released at the first surfacing; the sting at the arena halt; "Firestorm"'s
  base stem from section 2 and full mix from section 4, the ocean ambience throughout; the `CONVOY`
  tracker and `HULLS AFLOAT n / 3`; the sunken pod counting hits (3, hard 5); the boss checkpoint
  keeping the ships; ship names as `{ally}`; flyers' shadows on the sea; two briefing pages, Rook's
  teaser, Varga's four lines; the radio retimed with the events `first-telegraph`,
  `boss-part-destroyed`, `ally-hit`, `ally-lost`, `boss-destroyed` and `secondary-objective`;
  `submerged ambush` retagged to Act 4. Details in
  [Level 11](../../campaign/act-2-homefront/level-11-atlantic-convoy/README.md), the
  [Driftjelly](../../enemies/naval/driftjelly/README.md), the
  [Reef Spitter](../../enemies/naval/reef-spitter/README.md), the
  [Harbour Kraken](../../enemies/bosses/harbour-kraken/README.md), the
  [allies](../../allies/README.md), the [Torpedo Pod](../../player/weapons/torpedo-pod/README.md),
  the [enemies](../../enemies/README.md#layer-rules), the [art direction](../../art-direction/README.md),
  the [schemas](../architecture/README.md#data-file-schemas), the [HUD](../../ui/hud/README.md), the
  [hangar](../../ui/hangar/README.md), [wingmen](../../player/wingmen/README.md),
  [music](../../audio/music/README.md), [sfx](../../audio/sfx/README.md) and
  [voice](../../audio/voice/README.md). Part E's concept round is 33.
- 2026-10-09: M5 part E done, concept round 33 closed (user). During the part: the convoy bonus
  measured with an arm-first autopilot, then tuned by the cargo ships' HP (four slams at medium) so
  such a pilot keeps all three most of the time; the four small fixes (the ships hold clear of
  Tiamat, the Kraken's bounty 216 for its 15 %, torpedoes seeking sunken triggers, the rafts on the
  flanks); the capture's bugs fixed (the head's hit flash, the torpedo's visibility, the foreshadow,
  the arms across the mantle). After the round, on the user's feedback: the Kraken's head surfaces
  and sinks off the deck's south edge with Platform Tiamat raised, hits on it read as flesh, the
  slamming arms whip and only the hit part flashes; the sunken pod takes 3 torpedo hits (hard 5);
  four sounds reworked. Hard keeps no ship on the arm-first autopilot (user: hard is supposed to be
  hard). The next part is **F**, Level 12 (round 34).
- 2026-10-09: M5 part F started (Level 12), from its gap check, with the user's decisions F1–F9,
  all as recommended: **F1 = a** the cloud banks hide only `low-air` bodies (draw order, a level
  flag), the lightning lighting them for 0.5 s, air waves "out of the banks" entering at the edge
  with a flash; **F2 = a** the drifting canister always hittable, under its bank with a glint, fully
  seen in two authored flashes; **F3 = a** a latched Lamprey is hit only by rear-mount shots, `side`
  or `rear` weapons and blasts, homing and Rook skipping it; **F4 = a** one latch per Lamprey, then
  stun and flee; **F5 = a** "Firestorm"'s base stem at −6 dB in the eye; **F6 = a** a rain-only bed
  cut from the chosen storm ambience's source with thunder one-shots after the flashes; **F7 = b**
  the sondes on the play plane, drifting down at 60 px/s, not pulled by the magnet; **F8 = a** art
  straight to production, a/b only for the eye and the latch, sounds a/b for thunder, gust, latch
  and drain; **F9 = a** a Lamprey swarm is a swarm of chasers. The gap check's stated defaults
  apply: data in Act 1 terms with a `bounty_scale` (the canister 130, the secondary 75); the waves
  densified to at least 50 a minute with the 3 s pacing rule and L12's own popcorn, the t=32
  introduction alone; the act HP factor for the returning `medium` units; the wind in the data
  (gusts from the left, 2.5 s, 40 px/s, easy 25, hard 55, telegraph 1.0 s, easy 1.5 s; the wall's
  20 px/s drift, none on easy; hard's extra gusts; none in the eye), added to positions of the ship,
  Rook, Lampreys and Whirl Seeds only; lightning times in the data with a simulation event; the
  Lamprey's chase, latch slots (hard 2), drain on the shield only with the regen paused, the
  shake-off by four commanded reversals in 1.2 s; shield cells ×1.5 as a level drop factor; the
  eye's armour patch as a placed pickup; the `SONDES n / 3` tracker; the radio retimed to the 1 s
  rule with the events `first-latch` (with the shake-off event prompt) and `secondary-objective`,
  Rook's t=102 dropped; voiced with the cast speakers, Varga's four intel lines, two briefing pages,
  Rook's teaser; the storm bed with the ocean in the eye; L12's own atlas; the plan's L12 visit
  refitting Rook's Autocannon with the Lamprey's checks in `BalanceTest`; the eye calm, its cloud
  wall turning clockwise; the mines' stale `sub` item struck. The new state is hashed only in a
  level that uses it, so the replay hashes stay. Details in
  [Level 12](../../campaign/act-2-homefront/level-12-storm-front/README.md), the
  [Lamprey](../../enemies/air/lamprey/README.md), the
  [schemas](../architecture/README.md#data-file-schemas), the [HUD](../../ui/hud/README.md),
  [wingmen](../../player/wingmen/README.md), the
  [Proximity Mines](../../player/weapons/proximity-mines/README.md),
  [music](../../audio/music/README.md), [sfx](../../audio/sfx/README.md) and
  [voice](../../audio/voice/README.md). Part F's concept round is 34.
- 2026-10-11: M5 part G started (Level 13), from its gap check (**the user is on vacation: G1–G9 = a
  are the gap check's recommendations, pending the user's review** in the combined round at the end
  of M5): **G1 = a** the hook's `alternate`, `in-arc` and `always` made exact on the relay-aimed rule
  (a Stinger dives at the relay, crosses it for one 10-point hit and flies on); **G2 = a** the
  relay's integrity 600 / 900 / 450 tuned with the defend autopilot over several seeds, the measured
  numbers recorded; **G3 = a** the halt counts for the density rule, filled with unhooked popcorn;
  **G4 = a** Skimmers as lane walkers with the weave, the leads cut along the lanes; **G5 = a**
  `water: true` over the whole field with the water splash only on naval units; **G6 = a** the
  supply drone is presentation, the patch a pickup released at a point; **G7 = a** the whiteout banks
  hide only `ground` units; **G8 = a** the mission-complete jingle at t=218, none at the debrief;
  **G9 = a** straight to production but for a/b of the relay, the whiteout peak, three sounds and
  the Nansen voice. The stated defaults apply (the halt as a section of speed 0 without an arena, the
  `defend` primary reusing the escort's machinery, the `integrity` secondary, data in Act 1 terms,
  the waves densified to ≥ 50 a minute, Rook not guarding the relay). The steps are G1a–G8, G1b
  being this shared-doc text; the schema text is marked "planned (M5 part G)" in the
  [schemas](../architecture/README.md#data-file-schemas) until built. Details in
  [Level 13](../../campaign/act-2-homefront/level-13-polar-relay/README.md), the
  [hook](../../enemies/README.md#target-the-objective-hook), the [HUD](../../ui/hud/README.md),
  [wingmen](../../player/wingmen/README.md), [music](../../audio/music/README.md),
  [sfx](../../audio/sfx/README.md) and [voice](../../audio/voice/README.md). Part G's concept round
  is 35.
- 2026-10-11: M5 part H started (Level 14 and the act end), from its gap check (**the user is on
  vacation: H1–H10 = a are the gap check's recommendations, pending the user's review**; **H1 is a
  change to an approved spec**): **H1 = a** no creep in phase 1 (the Spire's arena is a halt with all
  six roots in range; the approved README's creep and its section table are corrected); **H2 = a**
  data-driven root bursts; **H3 = a** the maw's Wraiths fly the ambush path with a 3 s bottom-edge
  warning; **H4 = a** the Spire lifts off its anchor by a phase `move`, one boss and one bar, the
  stump drawn from the boss's state; **H5 = a** the boss attacks `lob` and `sweep` built from the
  enemy code; **H6 = a** the act-end order of Act 1 (kill, daylight, debrief with the act summary,
  outro, hangar), the Act 2 README corrected; **H7 = a** four voiced outro pages, the approved text
  unchanged; **H8 = a** straight to production but for a/b of the root burst, the daylight and two
  sounds; **H9 = a** the outro ends at the hangar (M6 owns the release's end); **H10 = a** the
  fight's HP measured on the autopilot and tuned to a ≈ 135 s mean, the par set to it, the bounty
  ≈ 30 % of the budget. The steps are H1a–H8, H1b being this shared-doc text. Details in
  [Level 14](../../campaign/act-2-homefront/level-14-siege-spire/README.md), the
  [Siege Spire](../../enemies/bosses/siege-spire/README.md), [Act 2](../../campaign/act-2-homefront/README.md),
  the [boss rules](../../enemies/README.md#boss-attacks-and-root-bursts), the
  [schemas](../architecture/README.md#data-file-schemas), the [HUD](../../ui/hud/README.md),
  [wingmen](../../player/wingmen/README.md), [music](../../audio/music/README.md),
  [sfx](../../audio/sfx/README.md) and [voice](../../audio/voice/README.md). Part H's concept round
  is 36.
- 2026-10-12: M5 part I (close-out) built: `ActPlaythroughTest` flies Acts 1 and 2 as one campaign
  with Rook on every difficulty (every level won at the first attempt), `BalanceTest` and
  `tools/balance.py` print Rook's DPS, Act 2 passes the variety checklist as built, and the Act 2
  items of the cross-cutting documents are ticked or retagged to Acts 3–7 (or M6). Parts F–I are
  built and wait for the combined concept round; M5 is not done until the user has reviewed them,
  listened to the voices and played the act.
- 2026-10-12: M6 part C (release plumbing) built with the defaults of Q8 (unsigned), Q9 (the
  Stormhawk icon), Q10 (0.1.0, tag `v0.1`) and Q15 (README, licence, credits in each bundle);
  all four bundles dry-run on JDK 21 and the Linux bundle started under Xvfb; pending the user's
  review in concept round 35. Q11 (title, repository name) and Q13 (the own assets' licence)
  stay the user's and block the tag. See [release](../release/README.md).
- 2026-10-12: M6 part A (mouse support) built with the defaults of Q6 (b, the glass pointer with
  the system arrow as the fallback) and Q7 (the pause and mission failed screens take the mouse,
  the pointer is hidden only while flying): hover, clicks, the right button as Back and the wheel
  on every out-of-game screen and overlay, the hangar's and the options' own regions; pending the
  user's review in concept round 35. See [ui](../../ui/README.md#mouse).
- 2026-10-12: M6 part B (high scores and the release's end) built with the defaults of Q1 (a: end
  card, name entry, credits, main menu; the hangar before Level 15 reads `ACT III - RED DUST:
  COMING SOON`), Q2 (a: name entry at the hard game over and at the release's end), Q3 (a: a
  SCORES main-menu item; the main menu is back in `review`), Q4 (the name's form) and Q5 (a: one
  new Okafor line at the game over, voiced); pending the user's review in concept round 35 (Q1,
  Q3 and Q5 flagged risky). The main menu and title show `v0.1`. See
  [scoring](../../systems/scoring/README.md#high-scores), [retry](../../systems/retry/README.md),
  [credits](../../ui/credits/README.md), [main menu](../../ui/main-menu/README.md) and
  [hangar](../../ui/hangar/README.md).
- 2026-10-12: M6 part D (polish and close-out) built with the default of Q12 (the polish list of
  the gap check, no attract mode in v0.1) and Q14 = b (Rook's guns and barks tables and the act
  outro's page table rendered); the hangar's intel no longer names a milestone, the placeholder
  audit runs as a test, the per-level bench is recorded in [tech](../README.md#per-level-bench-m6-part-d).
  M6 is built and waits for the user's review in concept round 35 (and round 34 for the credits
  roll's regeneration and Levels 12–14's sounds); it is not done until the user has played it.
- 2026-10-12: concept round 34 closed (user): Levels 12–14, their units and the Act 2 end accepted
  with C1–C13; the a/b picks, the redone Spire death, the kinder Spire (P2) and the economy's model
  (P3 = A) built in the follow-up steps K1–K4; parts F–H done. M5 stays open for the user's
  playthrough of Act 2 (P9) and the `sub` layer's three-OS CI check (P7).
- 2026-10-12 (user): concept round 35 closed: M6's parts A–D accepted as built (C1–C5, Q1, Q2 = a,
  Q4–Q10, Q12, Q14 = b, Q15, the build choices and the capture); the glass pointer and the
  application icon final; Q13: the game's own art, music and voices are Apache-2.0. M6 is done
  except Q11 (title and repository name) and the merge and `v0.1` tag (R.4: not possible until the
  commits can be signed), both next week; it is not released yet.
- 2026-10-13 (user, round 36, Q.25a): the M7 row and its parts A–I added from the gap analysis
  (`m7-gaps.md` §4.1), with track 9 moved from part G to part E (Level 18 needs it first), the dome
  as its own allies document in part D (Q.25b), the HUD's cooldown ring and dash pip and the
  controls' Dash binding as part A items (Q.25e), the release saves' Act 3 intro in part B (L.6) and
  the round's picks named where a part builds them.
- 2026-10-13: M7 part A built (steps A1–A7, the user away; defaults pending the user's review): the
  Ion Beam's `beam` delivery, the Light Drone Bay's drones, the Deflector Pod, the Rear Lance, the
  EMP Burst with the `machine` flag, the Orbital Lance, the Shield Overcharge with the cooldown rules
  and the HUD ring, the Evasive thrusters' dash and pip, the third utility bay (save format 4),
  `reference_dps` 15–21 and the balance plan to L21; flown on Act 1–2 levels with `--loadout` and
  `--special`. Its production art, sounds, captures and defaults go to concept
  [round 37](../../concept-rounds/round-37/README.md).
- 2026-10-13 (user, round 37): concept round 37 closed: everything accepted or picked as in the
  round's *Outcome* cells (C1–C2 accepted, F.1–F.4 final, P.1 a, P.2 b, P.3 a, the sound picks, Lance
  Control b, K.1–K.4 and B.1–B.6 yes, R.1 = apply the act factor, R.4 = fix all incl. (c), V.1
  accepted); steps Q1 (R.1 re-plan, the beam stopping on the Carrier's first phase and the Spire with
  the `CAST_ARMOUR` rule), Q2 (the picks in the game) and Q3 (the round closed, the rejected files
  moved, the generators routed) built it. M7 part A is **done**, pending the user's playthrough; M7
  stays open.
- 2026-10-14: M7 part B built (the user away; Q1 = a and the economy decision of 2026-10-14 from the user,
  every other default pending the user's review): the `buried` layer and the Burrower's screen-fixed
  burrow, the Dust Kite's swoop, Level 15's data with the drop launch and eased sections, the Act 3 act
  data and the intro played once from a release save (L.6, `act-intro:N` flags), Ares Control voiced with
  the respelling list, the Olympus backdrop and the part's production art, track 8 with a provisional
  base stem, the Mars ambiences; budget(n) the absolute typical haul (round 37 R.1 reverted) with the
  Act 3 plan re-planned. Its art, sounds, voices, captures and defaults go to concept
  [round 38](../../concept-rounds/round-38/README.md). Interim builds: with Level 15 present the release's
  end after Act 2 no longer fires, and the hangar after Level 15 cannot launch Level 16 until part C.
- 2026-10-14 (user, round 38): concept round 38 closed: everything accepted or picked as in the round's
  *Outcome* cells (C1–C2 accepted, F.1–F.8 and M.1 final, M.2 approved, sounds bulge b, dive b, entry roar a,
  Mars dust a, V.1–V.5, K.1–K.2 and B.1–B.3 yes, R.1 noted, R.2 accepted, R.3 keep all); step R38C put the
  bulge's crack layer and the dive b into the game. M7 part B is **done**, pending the user's playthrough;
  M7 stays open.
- 2026-10-14: M7 part C built (the user away; every default pending the user's review): the terrain (walls,
  islands, contact and push, shots, beams and missiles vs rock, Rook in the canyon), the walls projected rising
  toward the camera (MA.1 = b), the Tendril Anchor gates (`link`, beam vs tendril) and the `links` secondary with
  its BARRIERS tracker, the Threadcrawler as a ground chain, Level 16's data and the Valles backdrop, the part's
  sounds. Its art, sounds, capture and defaults go to concept [round 39](../../concept-rounds/round-39/README.md);
  Level 16's voices (step VOX-C) are not rendered yet (radio as text). Step C5 fixed a crash at every Level 16
  start (the cargo rovers have no break-apart frames). M7 stays open.
- 2026-10-18 (user, round 39): concept round 39 closed: everything accepted as in the round's *Outcome* cells
  (C1–C3 accepted, F.1–F.6 final, S.1–S.5, V.1–V.2, K.1 and B.1–B.5 yes, R.2, R.3, R.5 keep); R.1 asks for the
  canyon walls to look like a real canyon (the Grand Canyon from above): their rendering and textures are redone
  in step CW (in progress) for round 40; R.4: Level 16's hint changed (no beam promised), the Act 3 economy kept.
  M7 part C is **done** except R.1's redo, pending the user's playthrough; M7 stays open.
- 2026-10-19: M7 part E (Level 18 Dust Storm) **built** (the user away; every default pending the user's review in
  concept [round 41](../../concept-rounds/round-41/README.md)): the Dust Devil (vortex cycle, pull on the ship and
  the pickups, held pickups, the grit spiral, the blast flag) and the Ghost Drone (lane strafe, pass-triggered
  burst with the eye glint, hard's second pass) in the simulation; Level 18's data at the Act 2+ floor (the
  user's decision) with the beam-era HP (`beam_era_hp`, the user's "tougher, not more"; 301 enemies, about 101 a
  minute); `StormRules`, `StormChecks`; the storm look (`StormLooks`), the units' looks, the unclassified intel,
  track 9 "Grey Signal" with its motif stem, seven sounds, nineteen voices, the production art; the capture.
- 2026-10-19 (user, round 41 closed): everything accepted or approved as final (Ghost Drone and Dust Devil `art: final`;
  track 9, sounds, voices, the build choices, Level 17's fixes, the Act 2 finish) except three notes: the backdrop's
  horizontal line at about 5 s (F.c, redo), the Dust Devil vanishing for a second at 24 s in the capture (K.a) and the
  air units' silhouettes sharing the dust's colour (R.b), plus S.h = c (dome-break c). M7 part E is done except step FXS's
  fixes; Level 18 stays `art: chosen` until they land. Act 2: the build is accepted, the user's own playthrough is open.
- 2026-10-19: M7 part F (Level 19 Foundry of Tharsis) **built** overnight (the user away; every default pending the user's
  review in concept [round 42](../../concept-rounds/round-42/README.md)): the Revenant Walker (a stride arena, homing
  missiles, the flame cone, the vents), the Warden Tank (turret, rear plate), conveyor belts, furnace vents and up-facing
  nests in the simulation; Level 19's data (the Act 2+ floor, the beam-era HP, Rook's mortar), the looks, thirteen sounds,
  25 voices, the production art; together with the fixes of the user's six playtest findings, Levels 15-17 on the Act 2+
  rules and the refactors (`SimSpecs`, `LevelData`, `Autopilot`, `LevelScript`, the sim split, the level-metrics tool).
  M7 part F is done except the walker's death and the crawl seen live and the user's review of round 42.
- 2026-10-20: round 42 closed: part F's art approved as final, everything else accepted. M7 part F is done except the
  walker rework (K.a: it must shoot a lot more and the flame must be bigger, step WR) and the round 43 sounds (S.b and
  S.d-S.j redone from the free database, S.c = r08-a, S.n the walker's vent hit, death and wreck settle).
- 2026-10-20: M7 part G (Level 20) built; concept [round 43](../../concept-rounds/round-43/README.md) opened with its review, the walker's rework, the Freesound sounds and part H's questions; part H starts when they are answered.
- 2026-10-20: round 43 closed: M7 part G (Level 20) is done except the Q.1 levers (the swarm's band lag 90 to 30 px/s and a lunge every 4 s) and the Q.4 key (the ridge's pass as a level data key); its art (backdrop, props, rear-guard drone, bay pod, hangar icon, Okafor's image) approved as final, the sounds picked (S.a, S.c b, the rest a). The walker (W.a, W.e) needs another rework (step WR2: the pods armoured until their hatch opens, a back-mounted rail lance once the pods are gone, the stomp shockwave). Part H's questions answered (H.1-H.7, as recommended); part H starts decided.
- 2026-10-20: M7 part H (Level 21, the Dust Colossus and the act end) **built** (steps PH1-PH3, ART-H1-H3, AUD-H, H4, VOX-H, HA, HB), together with the walker's second rework (WR2), Level 20's levers and the ridge key (LQ); concept [round 44](../../concept-rounds/round-44/README.md) opened. Part H is done except what the round decides: the wave rework for the floor and the gaps (Q.1), phase 1's rings (Q.2), the par (Q.3), the erupted mouth's safe spot (Q.4), the crawler's hard key (Q.5), the nests' zones (Q.6), the Choir's take (Q.7) and the small defaults (Q.8). The round also asks Act 2's last items and part I's eleven questions (the playthrough rules, the first release as Acts 1-3, the playthrough save, the listen-through, the art audit).
- 2026-10-20 (user, round 44, I.1-I.11 and J.a, all as recommended): see the part I gap check's log (a local working note, `m7i-gaps.md`) and the *M7 parts* table: v0.1 is Acts 1-3 (I.2); an Act 3 level is `done` when its round has closed and its checklist is ticked, the playthrough staying an act-level item (I.8); the Act 4 design phase follows part I and the release (J.a, part J).
- 2026-10-22 (user): M7 part I done: the playthrough of Act 3 (2026-10-21) and the release `v0.1` = Acts 1-3 (no Mac check, so the release notes keep the "untested on Apple silicon" mark). M6's tag is made; M6 and M7 stay unticked for the open items named in their rows (M6: R.5 and round 34's leftovers; M7: Levels 18/19 variations).
