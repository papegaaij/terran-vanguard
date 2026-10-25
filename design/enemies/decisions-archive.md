# Enemies — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-09-30: Enemies are organised by layer category (air, ground, naval, space) plus bosses.
- 2026-09-30: Global difficulty levers and the bullet budget moved to
  [difficulty](../systems/difficulty/README.md) (single source); stat blocks keep overrides.
- 2026-09-30: Bullet colours tied to the faction colours; score vs credits deferred to scoring.
- 2026-09-30: Converted to the 960×540 baseline (was 640×360).
- 2026-09-30: Concept round 03: first enemy visuals and the lineup sheet; Vrell design-language choice put to the user.
- 2026-09-30: Concept round 03: Vrell design language is mainly **A "sleek chitin"**, with B "armoured brood" allowed where it gives a unit character (the Needler uses B). Chosen: Skitter A, Needler B, Stinger, Spore Bomber, Brood Pod, Mantis, Spine Turret A, Polyp Mortar, Talon, Gilded Gunship, Rail Bunker, Brood Carrier.
- 2026-09-30: Concept round 03 feedback: enemies need more distinct colours — the Vrell set reads too uniformly magenta. Colour differentiation pass in round 04.
- 2026-09-30: Concept round 04: role colours drafted (chitin base = role family, glow = kind of threat; Ascendancy accent + rim light); all chosen enemies re-rendered as r04 proposals. Vrell needles now yellow per the bullet rules.
- 2026-09-30: Enemy variety (user feedback after round 04: enemies too similar in size and shape,
  all insect-like flyers with a fixed front and back). Added on top of the roster: size tiers,
  multi-part rules, orientation (16/32 pre-rendered angles, radial spinners), 13 movement
  patterns, a per-act variety checklist, and 13 new units — Coilwyrm, Leviathan, Scuttler,
  Threadcrawler, Whirl Seed, Mote Swarm, Warden Tank, Strider, Buzzsaw Drone, Rail Serpent, Halo
  Platform, plus Dust Devil and Spiral Nautilus (roster fork's own additions).
- 2026-09-30: Concept round 04: role colours adopted ("the new colors are much better") — chitin base = role family, glow = kind of threat; Ascendancy black & gold with a per-unit accent and the 1 px gold/red rim light. All r04 re-colours chosen.
- 2026-10-01: Round 05 review: the new archetypes are liked. Warden Tank and Strider are kept for the Ascendancy (the Warden Tank keeps its unmarked Act 3 hint at L19); two animal-like Vrell ground walkers added — Ravager (Act 2) and Shellback (Act 3).
- 2026-10-01: Concept round 05 closed: Ravager and Shellback chosen; size lineup r05-b is the reference.
- 2026-10-01: Concept round 06: all new enemies and bosses liked except the Harbour Kraken (doesn't read as a creature from the depths) and the Halo Platform (rotation too jagged); Driftjelly's waterline ring reads as a drawn circle. All three are redone in round 07 under the new water and smooth-rotation rules in art direction.
- 2026-10-01: Boss and mid-boss weak points always glow lime (user decision). The Wraith uses rust chitin with blue-violet veins everywhere (its own round-06 sheet); the bone/violet Wraiths in the Siege Spire mockup are superseded.
- 2026-10-01: Layer hit rules settled (user accepted the recommendation): all weapons hit `air`, `low-air` and `ground`; `anti-ground` deals ×2 to ground and is required for hardened ground targets; only `homing` and `beam` hit `high-air`; only `anti-sub` hits `sub` (seen from above). Low-air and ground enemies don't collide with the player.
- 2026-10-01: Chain splitting: a cut Coilwyrm's tail end grows a new head once per chain; a second cut kills the severed part. Machines (Rail Serpent) never regrow.
- 2026-10-01: The 25 units of levels 01–14 promoted to full specs in their category directories; balancing basis (reference DPS, TTK targets, damage classes, bullet speeds, bounty classes) added.
- 2026-10-01: Balance: reference DPS for L08–L14 lowered to the typical loadout (≈ 60–80) instead of growing the economy; Act 2 HP to be rescaled.
- 2026-10-01: Returning units use an act HP factor (reference DPS ratio), no elite variants.
- 2026-10-01: Enemy damage values in the balancing basis confirmed; this document owns them.
- 2026-10-01: Concept round 09: enemy bullet set chosen.
- 2026-10-01: Act 2 unit and boss HP rescaled to the lowered L08–L14 reference DPS (Creeper, Hive Node, Ravager, Wraith, Driftjelly, Reef Spitter, Lamprey, Skimmer, Harbour Kraken, Siege Spire); the Mote Swarm stays at 1 HP per mote; bounties unchanged. Act 1 units list no Act 2 HP, so no act factor was applied.
- 2026-10-01: Balancing basis: the player being about 1.5× stronger than the reference in L01–L03 is accepted as gentle onboarding (user decision).
- 2026-10-01: Variety coverage rechecked against the level documents: Act 1 uses 14 units (3 insectoid), Act 2 about 20 (5 insectoid: Skitter, Stinger, Creeper, Wraith, Mantis); both still pass.
- 2026-10-01: New target-the-objective behaviour hook (modes nearest, alternate, in-arc, always), configured per level for defend and escort levels; L04 and L13 use it instead of per-unit overrides.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../reviews/acts-1-2/README.md).
- 2026-10-01: M1: the layer rules for weapons without special traits are encoded in `vanguard.sim.Layer` (hit by standard shots: air, low-air, ground; contact: air only); `sub`, hardened ground targets and the trait exceptions follow with the units that need them.
- 2026-10-01: Rules from M1 (user decisions): HP wording — a Pulse Cannon L1 shot does 2 damage units; `tiny` and `small` rammers are destroyed on impact; the snake readability rule is about the head (on screen ≥ 1.5 s before it can reach the player), so snakes of any length stay allowed.
- 2026-10-02: The balancing basis (reference DPS, damage to the player) and the formation vocabulary moved into [data.yaml](data.yaml) (M2 data files); their tables are rendered from it, and `tools/balance.py` reads the reference DPS from there. Each promoted enemy's stat block lives in a `data.yaml` next to its README.
- 2026-10-02: M2 (Level 01): the formation spawner places snake, V-wing, line abreast
  (front and rear), stream, pincer and circle by entry edge (`vanguard.sim.Formations`), with
  edge warnings ahead of side and rear waves (at least 1.5 s, or the wave's `warning`); enemy
  bullets are drawn above every layer, need 72 px to the ship and respect the bullet budget;
  standard shots hit `air` and `ground` targets, `air` units ram. The other formations, attack
  patterns, layers and telegraphs follow with the levels that use them, so those items stay open.
- 2026-10-02: First tuning values accepted as documented first values (user decision): burst gap
  0.15 s, `small` enemy bullet hit box 6×6, V rank spacing 50 px, pincer 60 px from its edge,
  circle centre 180 px below the top, 0.5 s between circle break groups, a Needler breaking off
  at 120 px/s. They are still constants in code (`vanguard.sim.EnemyGun` for the burst gap and
  the bullet hit box, `vanguard.sim.Formations` for the layouts); moving them into data needs the
  gun, bullet and formation planner to carry them, which waits for the next tuning pass.
- 2026-10-02: The Needler's thorn uses the standard orb look; the yellow needle stays reserved for
  the fast class (user decision).
- 2026-10-02: Edge warnings start at least 3 s ahead (was 1.5 s; user decision after playing Level 01, the warnings were easy to miss).
- 2026-10-02: Production art (Level 01 batch, `tools/art/enemy_bullets.py`): the Vrell orb as 4 frames (core pulse; the game now animates it at 15 fps, each bullet at its own phase) and the needle as a 16-heading angle set (`needle_<k>`, k × 22.5° clockwise from down; not used by Level 01). The body has 1-bit alpha, the outer glow four translucency steps. Review files proposed for round 12.
- 2026-10-02: Concept round 12 closed (user decision): the Vrell `small` bullets (orb and needle) approved as **final**; this index's `art` stays `chosen`, since the other enemies' art is still concept art.
- 2026-10-02: M4 part A: the layer rules of the weapons are in the simulation (`WeaponSpec.Delivery.reaches`, hardened ground targets glance off weapons without `anti-ground`); a level's ground target can be `hardened` in its data.
- 2026-10-02: M4 part B (user decision): a single-part unit's weak point is drawn only (the glowing spot, no multiplier); damage multipliers belong to the parts of multi-part units (the bosses, the Leviathan), which have their own hit boxes.
- 2026-10-03: Target-the-objective hook (main-agent choice, M4 part D): `nearest` is re-evaluated
  every step, an aimed shot picks its target as it fires, and only aimed attacks follow the hook,
  so with the crawler rule (only crawler-aimed shots hurt crawlers, user decision) a Scuttler's
  facing fan never hurts the convoy. The `fan` may be centred on the unit's facing.
- 2026-10-03: M4 part D (Level 04) adds to the vocabulary in the simulation: the `drift` and `sine`
  movement, the `walk` movement on authored ground paths with facing as state, the `spawn` attack
  (Brood Pod), a fan along the facing and an aimed attack only while facing away (Scuttler),
  directional frontal armour, the `carrier + escorts` formation and the walker `single`, `pincer` and
  `convoy`, and the target-the-objective hook in mode `nearest`. The broader items above stay open.
- 2026-10-05: M4 part G (main-agent default, stated to the user): the `space` layer item was
  tagged for "the first space level" in M4, but Level 07 at the L1 point flies on `air` like
  Levels 01–03 (Earth orbit); retagged to Act 5, whose open-belt levels (from L31's Mining Rig
  Escort) are the first on `space`.
- 2026-10-05: M4 part H docs reconciliation: the cross-cutting items are built for Act 1, so each
  is ticked for what Act 1 uses and the rest is split off with the milestone or act of its first
  user (the movement, attack and formation vocabulary, the hook's other modes, the machine chain,
  the variety check of the later acts), so the document is `done` for M4. Render order,
  telegraphs, warnings, the budget, the difficulty overrides and the angle sets are ticked whole.
- 2026-10-05: M4 part H (user decision D3 = A): the overrides' first bullet (per-bullet `medium+` /
  `hard-only` tags) is reworded to the per-difficulty pattern changes in the stat blocks and the
  waves' `skip`, which cover every Act 1 case; see [difficulty](../systems/difficulty/README.md).
- 2026-10-05: Concept round 26 closed (user: the round accepted as proposed): the `medium` bullet's large pulsing orb approved as **final**, drawn for the Leviathan's and the Scuttler's aimed shots with the heavy shot sound. The part's `art` stays `chosen`: the other types of the round-09 bullet set (among them the Ascendancy colours) have no production art yet; they come with their first enemies (M5 and later).
- 2026-10-06: M5 part A (user decision D5 = c): the act HP factor, designed but never built, applies
  only to returning `medium` and larger units in levels from Act 2 on; `tiny` and `small` units keep
  their HP. Rejected: a (every returning unit: a Skitter would take 3–4 hits, against the dense
  "many small, easy units" rule) and b (dropping the rule: returning mediums would die 2–3× faster
  than their time-to-kill targets). Main-agent details: the factor covers every part and segment,
  is rounded once with the difficulty lever, and leaves bounties alone.
- 2026-10-06: M5 part A default: boss and mid-boss bounties stay planned as absolute credits, but
  their data holds Act 1 terms (absolute ÷ act factor) and the code act-scales every payout; the
  exemption the text implied is not built. The document is `in-progress` again while M5 builds its
  Act 2 items.
- 2026-10-06: M5 part A built the act HP factor (D5 = c) as written: `SimSpecs.level` knows the
  level's act and number and gives every unit it builds (waves, ground units, a spawner's brood, a
  set piece's parts, a boss's parts and the units it releases) the factor of its stat block's tier
  and `first_level`; `SimSpecs.enemy` without a level stays at the factor 1. Act 1's levels are
  unchanged (replay hashes and the Level 01–07 tests as before). Factors today, from the reference
  curve: Spore Bomber 45 HP at L08 to 60 at L14, Scuttler 44 to 59, Mantis 30 to 40, Brood Pod 47 to
  63, Coilwyrm 118 to 149 (each part rounded, then summed), each keeping its time to kill. A level beyond the
  reference curve (Act 3 before its curve is written) is an error, so the curve is extended before
  a level uses it. The boss-bounty rule needed no code: the payout already applies the act factor to
  every bounty, boss parts included; `BalanceTest` and `tools/balance.py` now compare a boss's
  share at its level with the act factor applied (Act 1's unchanged), and `UtilityEffectsTest`
  pins it (188 Act 1 terms pay 301 in Act 2).
- 2026-10-06: Concept round 28 closed (user: accepted): the act HP factor as built (D5 = c; the
  table of [round 28](../concept-rounds/round-28/README.md#act-hp-factor): each part rounded half to
  even, so the Coilwyrm is 118–149 HP at medium over L08–L14) and the boss-bounty rule (data in Act 1
  terms, every payout act-scaled, no exemption) approved; the Coilwyrm head's ×2 fix noted. The
  document stays `in-progress` while M5 builds its Act 2 items.
- 2026-10-07: M5 part C (user decisions D1–D11 of 2026-10-07 and the stated defaults): the
  formation **`pack`** joins the vocabulary (3–5 walkers entering together, each on its own ground
  path, 0.25 s apart; the Ravager); **hardened** extends from ground objects to enemies (the Hive
  Node: only `anti-ground`, the Airstrike and the Smart Bomb damage it; homing shots and Rook skip
  a hardened unit unless their weapon is `anti-ground`); the Ravager's pounce is the one ground
  unit's contact with the player (D4 = a: `air` for the middle 0.3 s of its leap). The
  [Hive Node](ground/hive-node/README.md) and [Ravager](ground/ravager/README.md) stat blocks moved
  into their `data.yaml` with their weak points drawn only; their new keys (a periodic `spawn`, the
  `pounce`) are planned in the [schemas](../tech/architecture/README.md#data-file-schemas).
- 2026-10-08: M5 part D (user decisions D6, D8 and D9 of 2026-10-08 and the stated defaults): the
  formations **`swarm`** (a flock of 6–24 round a leader point flying an authored route, from the
  top or the bottom edge, often looping back; the Mote Swarm, D8 = a: real boids) and **`rear
  ambush`** (2–4 across the bottom edge, each in its lane; a cloaked Wraith enters at the top first,
  D6 = a) are planned for Level 10, their vocabulary entries (in [data.yaml](data.yaml)) made
  precise; every rear entry and re-entry is warned 3 s ahead (the 1.5 s left in the unit and level
  documents replaced). The [Wraith](air/wraith/README.md) and the
  [Mote Swarm](air/mote-swarm/README.md) stat blocks moved into their `data.yaml`, the Wraith's
  veins drawn only; their new keys (the cloak, the ambush path, the flock) are planned in the
  [schemas](../tech/architecture/README.md#data-file-schemas). A snake wave may give authored
  `paths` (the Skitter's item; streams keep their shapes).
- 2026-10-08: M5 part E (user decisions E1–E10 of 2026-10-08 and the stated defaults): **E2 = a**
  the `sub` layer seen from above water is reached by `anti-sub` and the Smart Bomb only, nothing
  else (rejected: b, area blasts landing on water also hitting `sub`: a rule per delivery and the
  torpedo losing its point; c, as b at 50 %: one more multiplier); the layer row made precise
  (homing shots and Rook skip it, no contact). **E1 = a** a level's `water: true`
  makes its whole play field water (a *Water* paragraph under the layer rules). Stated defaults: the
  formation **`field`** (scattered units entering with the sea and drifting on the current; the
  Driftjelly's "swarm (scattered)") joins the vocabulary in [data.yaml](data.yaml), `swarm` staying
  part D's flock; submerged units scroll with the sea (factor 1); `submerged ambush` retagged to Act 4
  (no Act 2 level uses it); a surfacing unit's current layer flips at the middle of its swap; the
  boss-bounty conversion restated with the level's `bounty_scale` (the Kraken's ≈ 207 paid). The
  Driftjelly's, Reef Spitter's and Harbour Kraken's new keys are planned in the
  [schemas](../tech/architecture/README.md#data-file-schemas).
- 2026-10-12: M5 part I (close-out): Act 2 checked against the variety checklist as built, from
  Levels 08–14's data: it passes. Correction: Act 2 has no `large` unit as built (the Kraken and the
  Siege Spire are `huge`), so its tiers are tiny, small, medium and huge (still at least three,
  with tiny and huge). The Act 2 movement patterns, attack patterns, formations and hook modes
  ticked with their tests; the remaining items of those kinds retagged to Acts 3–7.
- 2026-10-13 (user, round 36, Q.25d): the variety checklist's Act 3 row lists the Dust Kite (M10 = b)
  as the act's new `tiny` unit, beside the returning tiny Skitters and Whirl Seeds; the act's new
  units are now 12 (the Dust Kite a glider, not insectoid). The Act 3 unit and boss specs are
  approved (round 36, D.9–D.19).
- 2026-10-18: The user's Act 2 playtest (PT2-B 2c, with the recommendation "for Needlers and Stingers
  only", chosen by the user): the Needler and the Stinger get the act HP factor capped at ×3
  (`small_act_hp` in [data.yaml](data.yaml); `SimSpecs.actHpFactor`, `tools/balance.py`,
  `ActHpFactorTest`, `BalanceTest`'s time-to-kill bands) in Levels 08–09 only, the levels whose numbers
  change: applied from L08 on, Level 10's escort check (3 of 4 shuttles home at medium, D2 = a) failed
  at 2 of 4 and Level 11's hard runs stopped completing, so Levels 10–17 keep the old HP until the
  user extends `levels` (accepted in round 39 (2026-10-18)). The brief's "Stinger 18 at L08" assumed the full
  ×3; the reference DPS ratio gives ×2.31 there (14 HP; 15 at L09).
- 2026-10-19: `small_act_hp.levels` extended to Levels 12–13 with their L10-style waves (step WA, the user's
  P.e "yes, all"): Needler 12, Stinger 17/18 HP there; without it Level 12 measured 1.7 enemies on the
  screen and 4 shots a minute at medium (the Act 2 floor: 2.5 and 10), with it 2.7 and 18 (accepted in round 41 (2026-10-19)).
- 2026-10-19: `small_act_hp.levels` extended to Levels 11 and 14 with their L10-style waves (step WB, the
  user's P.e "yes, all"): Needler 12 HP there; with it Level 14 measures 2.8 enemies on the screen and 23
  shots a minute at medium, Level 11 2.9 and 14 (the floor: 2.5 and 10), and Level 11's hard run keeps
  all three ships afloat (accepted in round 40 (2026-10-19)).
- 2026-10-19: `small_act_hp.levels` extended to Level 18 (M7 part E, step E2; the user's decision of 2026-10-19:
  Level 18 meets the Act 2+ pacing and floor rules, round 40 P.e "all later levels"): Needler 12, Stinger 18 HP
  there, with its waves of firing groups; Levels 15–17 join with their own P.e rework (accepted in round 41 (2026-10-19).
- 2026-10-19: `small_act_hp.levels` extended to Level 15 (step PE15, the user's round 40 P.e "all later levels"):
  Needler 12, Stinger 18 HP there, with its waves of firing groups; Level 15 measures 2.68 enemies on the screen
  and 17.2 shots a minute at medium (the floor: 2.5 and 10). Levels 16–17 join with their own rework (accepted in round 40 (2026-10-19)).
- 2026-10-19: **Beam-era HP** (the user's decision of 2026-10-19, "tougher, not more": raise the HP for the beam era
  so units live long enough to shoot back, instead of step E4's 605 enemies on Level 18; later levels follow the
  same rule; M7 part E, step E5): `beam_era_hp` ×2 on `small` units from Level 18 (the plan's first Ion Beam), set
  so a killed small unit lives as long on the screen as on Level 10 (0.9 s → 1.5 s at Level 10's density, Level 10
  1.6 s). Built for `small` only: doubled, the Dust Devil (vanes) went unkilled at hard and the Wraith broke its
  rear check; `tiny` gains nothing against the beam (accepted in round 41 (2026-10-19).
- 2026-10-19: `small_act_hp.levels` extended to Level 19 (M7 part F, step F2; the gap check's default, accepted in round 42 (2026-10-20): Needler 12, Stinger 18 HP there, then ×2 by `beam_era_hp` (24 and 36), with its
  waves re-planned to the Act 2+ floor (light firing groups instead of popcorn).
- 2026-10-19 (user, round 41): the beam-era HP (B.e, `small` units ×2 from Level 18) accepted, all yes.
- 2026-10-19: `small_act_hp.levels` extended to Levels 16 and 17 (steps PE16 and PE17, the user's round 40 P.e
  "all later levels"): Needler 12, Stinger 18 HP there, with their waves to the Act 2+ floor; Level 17 measures
  2.60 enemies on the screen and 15.1 shots a minute at medium, most of the shots from its Burrower fields' rings
  (the plan's fit still kills a Needler group before its first shot). Every Act 2-3 level from 11 to 18 is listed
  now; Level 10 (the user's reference) is not (accepted in round 44 (2026-10-20)).
- 2026-10-20 (user, round 44, A.b (a): production assets (a `Source` chunk in `assets/`, every Concept art row decided)): **`art: final`** for this part. A later addition (a new sprite, a new concept round) is its own row and does not reopen the rest.
