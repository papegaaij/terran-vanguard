# Level 11 – Atlantic Convoy — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-10-01: Promoted from the Act 2 roster to a draft level document.
- 2026-10-01: Primary `reach-end`; the convoy ships only matter for the secondary objective, so
  a sunk ship loses its bonus but never fails the mission.
- 2026-10-01: Open question resolved: the Kraken's lane-choice and ship-damage rules moved into the Harbour Kraken spec; the arena notes link to it.
- 2026-10-01: The cargo ship and frigate specs moved to [allies](../../../allies/README.md).
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../../reviews/acts-1-2/README.md).
- 2026-10-08: M5 part E (user decisions of 2026-10-08, all as recommended): **E1 = a** a level flag
  `water: true` (the whole play field is water; Levels 11–13); rejected: b (a section flag: more
  data and a HUD that switches mid-level; for a later coastal level) and c (a water mask from the
  backdrop: far more work). **E2 = a** only `anti-sub` (and the Smart Bomb) reaches the `sub` layer
  ([enemies](../../../enemies/README.md#layer-rules)). **E3 = b** submerged jellies fire their ring
  too, their pulse showing through the water; rejected: a (only surfaced ones fire: tamer fields,
  the torpedo only for credits) and c (a half ring when submerged: a second attack profile).
  **E4 = c** the hybrid water drawing ([art direction](../../../art-direction/README.md)). **E5 = a**
  each slam arm owns a half of the lanes; **E6 = a** the Kraken's HP tuned to a ≈ 60 s fight;
  **E7 = a** its two slam arms as runtime chains ([Harbour Kraken](../../../enemies/bosses/harbour-kraken/README.md)).
  **E8 = a** the secondary all or nothing at the Kraken's death; rejected: b (pay per hull afloat:
  the haul and the tracker change) and c (both: two payouts). **E9 = a** art straight to production,
  a/b only for the ship pair and the lane telegraph and slam look; rejected: b (also the Kraken's
  surfacing and the platform: the largest render twice) and c (everything straight: the ships an
  unseen design). **E10 = a** Atlas Control auditioned a/b in round 33; rejected: b (reusing an Act 1
  voice, against round 19's "every role its own voice") and c (cast without audition).
- 2026-10-08: M5 part E stated defaults (accepted with the decisions): the data in Act 1 terms
  (crate 100, secondary 100, container salvage 10, Reef Spitter 14, Driftjelly 10) with a
  `bounty_scale` fitted by `TypicalHaulTest` (estimate 0.6–0.8); the Kraken's paid bounty ≈ 15 % of
  1,377 with arms : head 1 : 4; no act HP factor (the returning units are `tiny`/`small`); the waves
  densified with popcorn to at least 50 a minute and the 3 s pacing rule, no rear waves, the two
  introductions as quiet windows of at most 3 s; the new formation `field`; submerged units
  scrolling with the sea; each jelly's own seeded swap timer, a field starting about half
  submerged; torpedoes hitting `sub` and every `ground` unit over water at full damage; the torpedo
  pod idle and reading `NO WATER` without water; the Bomb Rack, the mortars and the Airstrike over
  water unchanged (they hit surfaced units only) with a water splash; the Smart Bomb hitting `sub`;
  Rook skipping `sub`, keeping out of telegraphed and slamming lanes, hit by slams; the plan's L11
  visit refitting Rook's Mortar, no torpedo; lane choice "nearest ship" by the player's x (ties to
  the lower lane), a slam hitting the player and a ship in one lane both; the convoy at stations in
  the lower half, nothing hurting it before the arena, the glide to lanes 1, 2, 4 and the frigate
  dropping back; the frigate's flak presentation only; the arena's jelly field released at the first
  surfacing (the level clock halts at the arena); the sting at the arena halt; "Firestorm"'s base
  stem from section 2, the full mix from section 4, the ocean ambience throughout; the `CONVOY`
  tracker with three pips and `HULLS AFLOAT n / 3`; the sunken pod counting hits (3, hard 5) and
  freed by a Smart Bomb; the boss checkpoint keeping the ships; ship names as `{ally}`; flyers'
  shadows on the sea; two one-screen briefing pages, Rook's teaser, Varga's four lines; the radio
  retimed to the 1 s rule with the events `first-telegraph`, `boss-part-destroyed`, `ally-hit`,
  `ally-lost`, `boss-destroyed` and `secondary-objective`, no mission-failed line; `submerged
  ambush` retagged to Act 4. The draft's contradictions are fixed: the budget (1,967 on the old
  curve → budget(11) ≈ 1,377), its Act-2-scaled values, the Kraken's "300 absolute", the density
  (≈ 27 a minute), the radio clashes, the single Varga line, the unattributed teaser, the sting "on
  entry" against "when the Kraken surfaces" (now: at the halt), "swarm (field)" (now `field`). Our
  readings, for review in round 33: the station and lane first values; the arena section with
  `speed: 0` and the anchored boss; the briefing, intel, teaser and radio texts; the second
  level-end line. `design` goes to `review` for the new texts; `implementation` is `in-progress`.
- 2026-10-08: M5 part E, step E3a (the level data): the waves keep the set pieces and fill the gaps
  with cheap Act 2 popcorn (Skitter streams and snakes, Mote Swarms of 8, a few Needler lines), 254
  enemies before the arena at medium (≈ 98 a minute; the pacing rule needs about that many, as in
  Levels 09 and 10), and `bounty_scale` **0.6** puts the typical haul on budget(11) (1,385 of
  1,377); the Kraken's paid share is then 13 % (its bounty was written for ≈ 0.69). The second raft
  nest moved from t≈92 to t≈101 (with the t=100 field), so no raft draws the torpedoes off the
  sunken pod; the pod enters at t≈93.8 on the backdrop's reef root (x 296). The floating containers
  are a destructible `group` of their own (new: the pickup on the group drops from its last
  destructible once all are destroyed; the objectives never count it) and the pod's hard hits a
  trigger's `hard: {hits: 6}` (new). The convoy's first values stand (stations, lanes 1, 2 and 4 at
  y 450, a 3 s glide, easy 3 HP). The radio keeps step E0's times (as text every timed line on time).
  Settled for review in round 33: one Torpedo Pod at L1 lands only 3 of the pod's 4 hits in its
  pass; the autopilot (no torpedo) kills about half the jellies, against the model's 80 %; it never
  meets the convoy secondary (5 of 15 ships afloat at medium on five seeds); after the halt the ships
  glide straight back to their stations while Platform Tiamat scrolls down through Mbeki's (E3b's
  "hold clear" suggestion is not built). The intel panel (`IntelPanelLayoutTest`) holds two lines
  of Varga's quote beside this level's rows (hazard, objective, boss, five portraits): her L2 and L3
  lines are shortened ("New: Driftjellies and Reef Spitters. And something huge.", "Anti-ground for
  rafts. Torpedoes for the sunken cache.") and the recommended traits are `spread` and
  `anti-ground` only (a third does not fit the row; `anti-sub` stays optional in the notes).
- 2026-10-09: The convoy bonus, measured then tuned (user: "measure, then tune"). The autopilot
  learned an **arm-first** play (`ARMS`: the arm guarding the most ships first, each slam baited to
  where it costs least by the Kraken's lane rules, the arm cut while it lies awash; the old play
  stays as `EYES` for the earlier measurements). It showed that no pilot could keep the ships with
  two slams each: every left-half slam strikes Halvorsen's or Mbeki's lane and the left arm takes
  about six awash slams to cut. Lever: **the cargo ships' HP** (the slams they take), 2 → **4** at
  medium, easy 3 → 5, hard kept at 2 (`hard: {hp: 2}`); rejected: the lane rule (how often a slam
  goes for a ship: the left half has no free lane, so a left-arm slam costs a hit whatever the rule),
  the slam cadence (a longer awash breaks the 4 s volleys) and 3 HP (arm-first kept all three on
  0 of 5 seeds). Result at medium: arm-first all three afloat on 5 of 5 fixture seeds and 3 of 5
  whole-level runs, the fight 58.6 / 55.5 s; eyes first on none. The typical haul keeps the
  secondary at 50 %. The ships **hold clear of Platform Tiamat** after the Kraken (`hold_clear: 520`
  px, Mbeki's `hold` off the bottom edge), the rafts moved to the **flanks** (an 84 px raft does not
  fit a 54 px gap between hulls), and the Kraken's bounty is **216** (paid 208, 15 % of the budget).
  Torpedoes now seek the sunken pod; a lined-up L1 pod frees it when the pilot fires from a second
  before the pod enters (out of step it lands 3 of 4: the pass is ≈ 4 s at 0.8 torpedoes a second,
  which seeking does not lengthen).
- 2026-10-09: Round 33 (user): the Kraken's head surfaces off the deck's south edge, not through
  the platform, so the arena halt **raises Platform Tiamat 32 px** (option b): its centre at
  (240, **78**) at the halt, the deck at y 2–154, the lanes from y 154 (`backdrop_l11.py`
  `PLATFORM_AT`, the piece placed at t 162.863, ground 22,930.83 px; the Kraken's `anchored: {y: 78}`)
  and the convoy's `hold_clear` **552** (the deck's top edge 538 px above the bottom edge at the
  halt). The **sunken pod takes 3 torpedo hits (hard 5)** instead of 4 (6): one Torpedo Pod at L1
  lined up frees it whether the pilot fires in step or out of step, and 24 px off the line its
  seeking torpedoes free it too; on hard it takes a pod on each wing (`Level11Test`). The fight's
  numbers are in the [Kraken's Decisions](../../../enemies/bosses/harbour-kraken/README.md#decisions).
- 2026-10-09: Concept round 33 closed (user): the backdrop, the props, the briefing images, the
  intel pictures and the Driftjelly's, Reef Spitter's and Harbour Kraken's art approved as final, so
  `art: final`; the convoy ship pair **a** "Atlantic line" (b "Reactor run" rejected); the lane
  telegraph and slam **a** "edge dashes + spray sheets" (b rejected); Atlas Control's voice **b**
  (MaryAnn; her ten takes rendered at the close); the eight sounds accepted, the churn, death,
  sinking and flak after a rework (no bubbles; the flak raised twice to −16 dB), produced into
  `assets/sfx/`. After the user's feedback the Kraken's head surfaces and sinks off the deck's south
  edge with Platform Tiamat raised (centre y 78, option b), with flesh hits, whipping arms and a
  part-only flash, and the sunken pod takes 3 torpedo hits (hard 5, build choice n); the retaken
  Kraken capture accepted. The captures, the 15 voiced lines, the texts, the part E numbers (hard
  kept as built: "hard is supposed to be hard"), the decisions and every build choice (a)–(y)
  accepted; the texts approved, so `design: approved`; every Implementation item is ticked, so the
  implementation is `done`.
- 2026-10-19 (step CB, round 40; the user approved proposal 3a and the r40 Kraken art on 2026-10-18):
  the arena moved 124 px up (Tiamat's centre −46, the piece at t 163.749, `hold_clear` 676: the deck's
  top edge 662 px above the bottom edge at the halt), the Kraken's four rooted arms, Okafor's line at
  the first telegraph on a ship's lane ("The arms are rooted at the rim. Cut a root and that lane is
  safe!"), Rook's calamari line at the first root cut. The Kraken's head re-measured to 3,400 for the
  ≈ 55 s fight (accepted in round 40 (2026-10-19); see its README). The perfect run 2,118 → 2,116 (the four
  roots' bounty rounds 2 lower). L11 stays in `PacingTest.PENDING_ACT_TWO_RULE` (its waves next).
- 2026-10-19 (step WB, round 40 P.e "yes, all", accepted in round 40 (2026-10-19)): the waves rewritten
  L10-style (61 waves, 313 enemies before the arena at medium, a wave about every 2.5 s; Needler groups,
  Mote Swarms and Skitter streams round the kept set pieces), so the level passes `PacingTest`'s Act 2
  rule and `ActFloorTest`: medium 1.87 → 2.90 enemies on the screen, 6.0 → 14.4 shots a minute, 10 → 1
  pauses over 2 s; hard 2.40 → 3.15, 12.3 → 15.1, 4 → 2; three ships afloat on every difficulty. The
  Needler and the Stinger get the capped act HP factor here (`small_act_hp` levels 11 and 14 added, as
  WA did for 12–13: with 4 HP the plan's fit killed them before they fired). `bounty_scale` 0.6 → 0.43
  (typical 1,345, −2.4 %; perfect 2,043, 1.48 ×); the Kraken's bounty 216 → 300 (head 200, roots 25) so
  its parts pay what they did and its share stays 15 % (`KrakenTest`). The floor leaves out the boss
  fight (a set piece is not an enemy on the screen; the Kraken has its own tests). L11 leaves
  `PacingTest.PENDING_ACT_TWO_RULE`.
- 2026-10-20 (user, round 44, X.a-X.d, X.f-X.i): the Level 11 arena and first-ship-cue captures and the Level 14 briefing page and kinder fight accepted; Rook's halt line at level t≈118 kept (X.f); the stale "pending the user's review" notes rewritten as "accepted in round N (date)" (X.g); the final production rows `final` (X.i). The checklist is ticked and its rounds are closed: **`implementation: done`**; the act's playthrough stays an act-level item in the [Act 2 README](../README.md).
