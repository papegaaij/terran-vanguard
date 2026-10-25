# Level 14 – Siege Spire — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-10-01: Promoted from the Act 2 roster to a draft level document.
- 2026-10-01: Open question resolved: the maw's Wraiths pay their full Wraith bounty at the Act 2 factor, as budgeted here (user decision; recorded in the Siege Spire spec).
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../../reviews/acts-1-2/README.md).
- 2026-10-11: M5 part H, step H1a: the approved first draft reworked to the design the gap
  analysis decided. **The user is on vacation: H1–H10 are all the recommendation (a) and all the
  stated defaults apply, accepted in round 34 (2026-10-12).** **H1 = a (recommended; accepted in round 34 (2026-10-12)): the approved 15 px/s creep of boss
  phase 1 is dropped, a change to an approved spec that the user must confirm**: the arena is a
  halt (speed 0), all six roots on the screen from the start, "in turn" from the staggered fire
  and the player's choice of root; rejected: b (an arena of speed 15 in phase 1 and 0 afterwards: a
  phase-keyed scroll, a changed `haltScroll`, the backdrop's boss pieces and the daylight offset
  all move with it) and c (a creep through the whole arena: breaks the anchor, the phases and the
  stump). **H2 = a** data-driven root bursts (`root_bursts`; invulnerable silent growths; 100–400
  px below the top edge, not within 60 px of the ship's x; no collision); rejected: b (growths
  enter at the top: the set piece loses its point) and c (bursts near the ship: harder to read).
  **H3 = a** the maw's Wraiths are ambush launches with a dynamic 3 s bottom-edge warning, Rook's
  Trail formation and at most 4 alive; rejected: b (straight launches: no rear attack) and c (no
  warning: against the act's rule). **H4 = a** one boss and one bar: a phase `move` lifts the whole
  boss over ≈ 3 s (invulnerable), the core on `air`, one armour patch at the start, the stump drawn
  from the boss's state; rejected: b (a second set piece for phase 3: more state) and c (the spire
  stays on the ground: loses the open-air phase). **H5 = a** the boss attacks `lob` and `sweep`,
  built on the Polyp Mortar's lob and the Mantis's sweep with the spec's numbers; rejected: b
  (patterns the boss has: loses the telegraphed marker and laser) and c (lob only, `spiral` for
  the sweep). **H6 = a** the act-end order of Act 1 (kill, daylight, debrief with the act summary,
  outro with `act-complete` under page 1, hangar), so the draft's "the fanfare plays over the
  daylight section" is corrected; rejected: b (fanfare over the daylight, no jingle: differs from
  Act 1) and c (both: three fanfares in two minutes). **H7 = a** four outro pages, one per
  paragraph of the approved text, each with an image and voice (data in the Act 2 README);
  rejected: b (one page, one still) and c (images a/b). **H8 = a** round 36 auditions a/b for the
  root burst, the daylight outro, the tear-free roar and the canopy withering; the rest straight
  to production; rejected: b (also the Spire's death) and c (everything straight to production).
  **H9 = a** after the outro the hangar reads `BEFORE L15 - NOT BUILT YET`; M6 owns the end of the
  release; rejected: b (an end card, a new screen the design does not describe) and c (straight to
  the menu with the credits). **H10 = a** the Spire is built with the spec's HP, the fight is
  measured over several seeds and the HP tuned (not the attacks) to a mean of ≈ 135 s inside
  90–180 s, the par the measured mean; the bounty ≈ 30 % of the budget as paid (≈ 506, was 720 =
  43 %); rejected: b (keep 720 and let `bounty_scale` absorb it: breaks the 30 % share) and c (keep
  the HP and accept the measurement).
- 2026-10-11: M5 part H stated defaults (accepted in round 34 (2026-10-12) with the decisions): arena
  start, boss warning, arrival and checkpoint all at **t=108** (the draft's warning time; its
  boss arrival at t=113 and the checkpoint "at the warning" were two moments) and the Concord
  approach 95–108 (was 95–113); the clash at t=106/107/108 on top of the boss warning goes (the
  Choir t=98, Varga t=102, Rook's line is his boss-warning bark); sections as in *Layout* (the
  creep dropped, the draft's "16,550 px" and "≈ 17,800 px" were wrong with the creep); primary
  `reach-end` with the arena as the condition, "DESTROY THE SIEGE SPIRE" (the draft's `boss`
  primary is not one); secondary `groups` of the 8 growths, 63 in Act 1 terms, tracker `ROOTS
  n/8`; popcorn from t=8 and at least 50 enemies a minute (the draft's "no enemies before t=24"
  breaks the 3 s pacing rule; ≈ 92 units, was 42); the t=82 and t=104 rear Wraiths kept, the
  Hive Nodes releasing 2 Skitters once each in the budget; root bursts as in *Hazards*, the t=72
  Ravager crater a burst without growths; the data in Act 1 terms with a `bounty_scale`
  (estimate 0.6–0.8); the act HP factor for the returning medium units (Creeper 40, Hive Node,
  Ravager and Wraith at their L14 values), none for the Spire and the small units; the Spire's
  bounty ≈ 506 paid (pods 42, roots 42, maw 84, spire 170); the maw's Wraiths ambush plans
  paid at the normal Wraith bounty × 1.6 × the scale and budgeted at 4 launches (8); the
  budget(14) = 1,687 and the typical-haul table replace the draft's 2,410 "perfect collection";
  Varga's four intel lines (the draft had one) and two briefing pages; Rook's teaser attributed
  to him, his t=80 line dropped (his bark says it) and his t=3 line retimed after Okafor's, his
  phase-3 line an event line 5 s after the phase starts; the supply drone's patch a placed pickup
  without a drone sprite; the armoured car's crate 88 (pays 141, the draft's 140 was written in
  Act 2 terms); edge warnings 3 s; the daylight section without enemies or music, the canopy's
  tear a real-seconds timeline, the closing line queued `CLOSING`; "Firestorm" base from
  section 2 and full from 4, no mid-level jingle; the balance plan's L14 entry unchanged (`rear`
  L2, one Airstrike, Rook's Autocannon), re-checked by the measured fight; the threat profile
  computed from the data (the draft's "front 71 / rear 29" counted a different wave list); the
  secondary's wording "before they leave the screen" counted from the growth's placement; root
  burst hard timing: the first shot after 1.0 s, not the telegraph (the draft's wording read as
  if the telegraph shortened); L14's units in an atlas of their own with the Spire on its own
  pages. `design` goes to `review` for the reworked texts; `implementation` is `in-progress`.
- 2026-10-12: M5 part H, step H4 (accepted in round 34 (2026-10-12) with H1–H10): the level's `data.yaml` and
  the loader (`root_bursts`, the Siege Spire's boss keys, `PartHRules`/`PartHChecks`), the tables above
  rendered from it. Settled while the user is away: **the root field (section 3) scrolls at 80 px/s**
  (a growth appears 1 s after its dust and fires 1.5 s later: at 140 px/s it would by then have scrolled
  350 px from its burst and stood on the screen for a moment only; the bursts lie 100–140 px below the
  top edge, the least the simulation allows, so the growths fire about 200–240 px above the bottom edge
  and stand ≈ 3 s on the screen), the scroll totals 14,700 px; **the Spire's bounty 26.8 % of the budget
  as paid** (452; `bounty_scale` 0.76; at 30 % a perfect run stays under 1.4 × the budget); the
  arena section 108–250; the waves retimed to a wave every 3–3.5 s with smaller popcorn (95 wave units,
  112 with the ground units; 67.6 a minute), the root bursts' quiet window t=62–67, easy's t=82 Wraiths
  replaced by a Needler V-wing from the front (a Wraith flies only the rear ambush); the hard Molard
  nest of three Spine Turrets at x 200–280 between the nodes; the growths 52 px apart (the art's growth
  points), the Spine Turret left; the burst positions (t=63 x 150, 74 x 330, 84 x 170, 92 x 320) for
  the backdrop (step H5); the armoured car entering at t=14.5, x 330 (on screen ≈ 15–19 s), its secret's
  line given to Okafor; the supply drone's patch placed at t=100, x 240; the secondary named "Destroy
  every root-burst growth" with its tracker `ROOTS` (a `groups` objective may now name its tracker);
  the briefing's objective line `DESTROY THE SIEGE SPIRE` for an anchored act boss holding a
  `reach-end` level; Rook's phase-3 line a second `boss-phase` cue queued after Okafor's; the boss's
  launches budgeted at the measured three of 2 (`spawns`), not four; the intel strip's root-field mark
  the sections holding the bursts (60–95 s); the briefing images left out of the data until step H5
  renders them (as Level 13's); Rook's teaser shortened to "Roots first, flyers from behind, then the
  Spire. I'll take the six o'clock." and Varga's L2 and L3 lines shortened (the intel panel's room,
  `IntelPanelLayoutTest`).
- 2026-10-12: M5 part H, step H6 (the user is on vacation; **accepted in round 34 (2026-10-12)**:
  the game's looks and sounds. The root bursts draw look a (the cracks over the telegraph, the dust 6 px
  above the point over the telegraph and the eruption, the root's 0.5 s eruption leaving the crater) and
  grow their units in from 0.35 of their size, tinted from the creep's violet; the armoured car shows a
  frame a hit (one creep blob cleared per hit), its cleared frame for 0.2 s after the sixth, then the
  hatch open. The daylight runs on the Spire's death (real seconds): the canopy's veins fade out from 2
  to 7 s, the tear frames show from 0.5 to 8.5 s (2 s each, tiled on the high-air scroll), the beams
  ramp in from 2.5 to 5 s (the second frame from 6.25 s), as `backdrop_l14.py`'s review composite does.
  The ambience is the canopy's hum (`earth-canopy`), fading out over 2 s at the kill while the
  megacity's own ambience (with its distant sirens) rises over 7 s under the canopy's withering (0.5 s
  after the kill; 3 s since step H7b); round 36's sounds play as variant a. Rook's rear bark for the maw's launches is timed
  on the real clock (the level clock may halt in the arena). Pod 1's offset in the Spire's data had its
  sign flipped (−24.3, below the anchor, where its root's pod is 24 px above); fixed to 24.3 so the pod's
  hit box sits on its sprite (the measured medium fight is then 129.5 s, the par 130 s). The voices
  (20 lines: the radio, the two briefing pages, the Act 2 outro's four pages) are rendered; with their
  lengths the radio is retimed to the 1 s rule: Hammer Lead 16 → 13 (before Rook; his flight still
  passes during the line), Rook 7.5 → 21, Varga's canopy line 22 → 31.5, the Choir 98 → 96; three
  seeds are pinned (Hammer Lead's "Hammer flight", Varga's "hive nodes", Okafor's "ships out for Mars").
- 2026-10-12: M5 part H, step H7b (from the H7 capture; the user is on vacation, **all accepted in round 34 (2026-10-12)): Rook's phase-3 line went stale behind Okafor's (an event line waits 6 s at most):
  a `boss-phase` cue now queues as a `PHASE` line, stale after 20 s, dropped at the kill. Okafor's
  root-grid line waited behind the Choir (96) and Varga (102) and played over the boss's arrival,
  which also made Rook's boss-warning bark (and his t≈104 rear bark, which the higher-priority warning
  bark replaces anyway) go stale: the secondary's line is dropped once a boss's warning starts. The
  growths' first-shot delay 1.5 → 0.75 s (hard 1.0 → 0.5 s): they fired about 220 px above the
  bottom edge, now about 280 px; the depths were already at the low end (100–140 px). The daylight
  drifts at 40 px/s instead of 140, so the stump stays on the screen while the light falls on it (the
  sim has no post-kill hold; a slower section is data only): 13,500 px of ground, the backdrop's strip
  sized by the daylight's speed (`backdrop_l14.py`, regenerated, `--check` 0 problems). Hammer flight
  passes at x 130 over the lake instead of x 340, where it hid the armoured car (x 330) at t 16–18.5.
  The canopy withering starts 3 s after the death (the death chain's end) instead of 0.5 s, where the
  chain's bursts masked it. The stump is drawn as muted ash (blended 60 % to a grey) so the free
  spire's dark star above it reads (darkening it made one dark mass). Kept: the death's white flash
  (the act bosses' death flash, the same as the Brood Carrier's in Level 07, design/enemies/bosses)
  and the silent act summary page (Act 1's is the same: the debrief plays no music, the outro's
  `act-complete` follows).
- 2026-10-12: M5 part H, step H9 (from the H8 capture `level-14-capture-fixes-r36-a`; the user is on
  vacation, **all accepted in round 34 (2026-10-12)): (1) Okafor's root-grid line never played: the roots
  die at about t=93.5 with the radio free, but at the default text speed the line holds the radio
  about 10 s (two pages), so it found no gap before the Choir (96) or Varga (102) and was dropped at
  the Spire's warning. A closing line queued while the radio is free (nothing on it, nothing waiting)
  now goes ahead at once and the timed lines that fall due wait for it (`RadioQueue`, design/ui/hud):
  it plays at 93.5, the Choir about 104, Varga about 110 (over the Spire's arrival). Rejected: moving
  the Choir to 98–99 (the line ends about 104: the three lines take about 23 s of the 15 s before the
  warning, so no retiming keeps the 1 s rule). Roots that die late, while a line plays, still wait for
  a gap and are dropped at the warning (`RadioTimelineTest`). (2) The daylight drifts at 25 px/s
  instead of 40: the stump left the bottom edge about 9 s after the kill, 10 s before the debrief; now
  about 21 s after it (measured in `level-14-capture-daylight-r36-a`: the body leaves at about 212, the root tips at about 215; the debrief at about 217.5), the scroll
  running on to the debrief; 13,320 px of ground; the backdrop regenerated
  (`backdrop_l14.py`, `--check` 0 problems), its proposal merged (the strip's top piece and the
  daylight boulevard later). (3) The t=100 armour patch is carried in by Level 13's CDF supply drone,
  flying in from below the bottom edge (no helipad here), releasing it at x 240, 380 px up and
  leaving the same way (the data's `drops`); without a relay no launch sound plays.
- 2026-10-12 user, round 34: the tear-free roar **b** (a creature's bellow rising with the lift, wet
  tearing, an air shockwave) and the canopy withering **b** (a dying exhale, a groan and a sigh into the
  wind, 7.5 s) instead of a; the root burst stays a. Step K1 wired them
  ([sfx](../../../audio/sfx/README.md#decisions)).
- 2026-10-12 user, round 34 P2 (a kinder Spire for players who do not dodge well): the proposal
  (the outer pods 1 and 4 fire straight down, the inner ones stay aimed) plus the lobs every 8 s per
  mortar root (was 4; markers unchanged), measured with step H8b's calibration harness (the XTest
  driver's random dodge, armour lost per 10 s in the fight): medium 30.6 → 9.8 (the Harbour Kraken
  10.5), easy 12.1 → 1.5, hard 38.1 → 23.4. The roots' HP ×1.04 keeps the autopilot's fight at the par
  (medium mean 130.4 s, par 130). Details, the variants tried and what is pending in the
  [Siege Spire's Decisions](../../../enemies/bosses/siege-spire/README.md#decisions).
- 2026-10-12 (user, round 34): concept round 34 closed. The root burst **a** "breaking paving" and
  the daylight **a** "golden hour" (b "creep surge" and b "pale dawn" rejected, their files in
  `concept/rejected/`); the Geneva backdrop, the armoured car, the Act 2 outro images, the briefing
  images and the Siege Spire's art and intel silhouette approved as **final**, the Spire's death after
  the redo r36-b ([Siege Spire](../../../enemies/bosses/siege-spire/README.md#decisions)), so
  `art: final`; the captures (14.3a–d), the texts (C5, the teaser for Rook), the numbers (C6 hard over
  the window, C8 the bounty at 26.8 %, C9 the HP ×1.8), C1 (no creep), C2 (the root field at 80 px/s),
  C3 (the daylight at 25 px/s), C12 (the timeline and radio), H1–H10 and every build choice accepted;
  P1 (Rook ejects in the Spire fight) and P6 (easy's refused Airstrike charge) accepted, P2 and the
  sounds as in the lines above; the 20 voiced lines heard. The document leaves `review` for
  `approved`. Still open: the user's playthrough of Act 2 (P9).
- 2026-10-12: Step K10: the intel strip's boss mark at 108 s (the *Sensor-suite detail*) built, a red
  tick the strip's depth drawn over the waves (the same for every level with a boss, see
  [hangar](../../../ui/hangar/README.md)); every item ticked, `implementation: done`.
- 2026-10-19 (step CB, round 40; the user approved proposals 4a, 4b and 4c on 2026-10-18): the kinder
  Spire in the data (its README), the weapon hint `homing` (traits `homing`, `anti-ground` instead of
  `anti-ground`, `piercing`; Varga's page: "Missiles find the root guns while you dodge; bombs crack
  them." instead of "Bring anti-ground and something that pierces. Kill the roots and the trunk
  starves." (the page holds 4 lines; she says the second at t=31.5 anyway); intel L3 "Homing missiles
  reach every root."; Rook at t=110 "Missiles on the roots, I'll keep the flyers off you."), the
  maw's Wraiths budgeted at 3 launches of 1, the `bounty_scale` 0.76 → **0.81** so a perfect run keeps
  1.4 × the budget (typical 1,722, +2.1 %; perfect 2,400, 1.42 ×; the Spire paid 482, 28.6 %).
  Accepted in round 41 (2026-10-19): the scale, Rook's line at t=110 (the halt eases over t=108–109), intel
  L3 drops "When the crown opens, look behind you." (Varga's page keeps it). L14 stays in `PacingTest.PENDING_ACT_TWO_RULE` (its waves next).
- 2026-10-19 (step WB, round 40 P.e "yes, all", accepted in round 40 (2026-10-19)): the waves rewritten
  L10-style (43 waves, 181 enemies before the arena at medium, a wave about every 2.5 s: Needler and
  Stinger groups, Skitters, four Creeper convoys of 5, Spore Bomber lines, Mantis pincers round the kept
  Ravager pack, Wraith ambushes, ground targets and root bursts), so the level passes `PacingTest`'s Act
  2 rule and `ActFloorTest`: medium 0.53 → 2.76 enemies on the screen, 3.1 → 23.3 shots a minute, 12 → 1
  pauses over 2 s; hard 0.94 → 3.31, 8.3 → 39.3, 4 → 1. The root bursts' quiet window now t=59–66 (was
  62–67). The Needler and the Stinger get the capped act HP factor here (`small_act_hp` levels 11 and 14
  added). `bounty_scale` 0.81 → 0.40 (typical 1,744, +3.4 %; perfect 2,458, 1.46 ×) and the Spire's
  bounty 372 → 768, so its parts pay what they did (29.2 % of the budget; `BalanceTest`'s 20–40 %).
  The floor leaves out the boss fight (the Spire is a set piece, not an enemy on the screen; it has its
  own tests). L14 leaves `PacingTest.PENDING_ACT_TWO_RULE`.
- 2026-10-19 (round 41, the user's playtest; steps FX-S2, FX-G and FX-C, accepted in round 41 (2026-10-19)): **the
  approach to the Spire gets a pause and the warning comes first.** The boss block's `incoming: 7.5` opens the
  warning box, the klaxon and track 22 at t=100.5, closing (5 s) before the Spire's roots enter the top edge;
  the Roots phase holds its fire 1.5 s after the halt. The t=102 Stinger V-wing and the t=105.5 Needler line are
  dropped (Needler 62 → 58, Stinger 21 → 18 at medium), the Creeper convoy moves from t=97 to 89, the side
  stream 97 → 95.5, the rear Wraiths 104 → 95 and the Needler line 99.5 → 97.5 (the last wave), so the
  autopilot's screen is empty from t≈101.5 (medium), 102.5 (hard) or earlier (easy) to the roots' entry
  (t≈105.5); with the 5 s box over the warning, the last enemies can still be on the screen for its first
  1–2 s. The typical haul 1,707 (+1.2 %), the perfect run 2,396 (1.42 ×), `bounty_scale` 0.40 kept. With the
  Spire's leading sweep (its README) the fight at medium stays at its par (101.9 s mean, par 103 s).
- 2026-10-20: round 42 closed: the playtest fixes (T.1 approach, T.2 sweep, T.5 death) and their check capture (K.b) accepted by the user, every sub-choice yes; the sweep is not clipped to the play field (T.2 d).
- 2026-10-20 (user, round 44, X.a-X.d, X.f-X.i): the Level 11 arena and first-ship-cue captures and the Level 14 briefing page and kinder fight accepted; Rook's halt line at level t≈118 kept (X.f); the stale "pending the user's review" notes rewritten as "accepted in round N (date)" (X.g); the final production rows `final` (X.i). The checklist is ticked and its rounds are closed: **`implementation: done`**; the act's playthrough stays an act-level item in the [Act 2 README](../README.md).
