# Hangar — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-09-30: The intel panel sits in the hangar itself, so the player sees it while shopping.
- 2026-09-30: Converted to the 960×540 baseline (was 640×360).
- 2026-10-01: Concept round 06: layout **B** (central ship schematic with the panels around it) chosen, but in the glass style of the menus, not metal — restyle in round 07. Layout A rejected.
- 2026-10-01: Concept round 07: hangar layout B in the glass style over the tactical map of Mars (r07-b) chosen; the hangar-bay backdrop (r07-a) rejected.
- 2026-10-01: Test fire comes after the first build; the layout keeps its space.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-02: M3 part B1 placeholder (`vanguard.game.screen.HangarScreen`, marked as such on screen;
  the design here is part B2): one glass panel with the credits, the loadout, the armour, the next
  mission with its hangar teaser, difficulty, score, playtime and retries, and Launch (disabled
  while the next level is not built), Save game (the slot list) and Main menu (confirmation). It
  autosaves when it opens and plays the hangar theme. The real hangar replaces the class behind the
  same constructor; every way into it (intro briefing, debrief, Back to hangar, Abort, Load,
  Continue) already passes the campaign.
- 2026-10-02: M3 part B2 (`vanguard.game.screen.HangarScreen` with `vanguard.game.hangar`, the
  rules in `vanguard.content.campaign.Hangar`, `Catalogue`, `Intel`, `Flight`): layout B after
  hangar-r07-b at 960×540. Top bar: HANGAR, the next mission, act and difficulty, the UNDO, REPAIR
  and SAVE commands, the credits and LAUNCH. Left: the shop for the selected slot (fitted, owned,
  buyable by price with the NEW tag and ◆ markers, locked by unlock level with "LOCKED Lnn"; eight
  rows, scrolling), the selected item with its traits, numbers (DPS, output, capacity, regen,
  delay, armour, speed, charges; draw) and the green/red deltas against the fitted item, its
  choices (buy, upgrade, fit, unfit, sell, buy a charge; refused ones dim with the reason), the
  last transaction's message and the empty test-fire box. Centre: the holographic schematic with
  the parked Stormhawk sprite (3×), callouts for the front, wing and rear mounts with level pips,
  the locked escort slot (Act 2), the GEN/SHD/ARM/ENG/SPC/UTL/UTL tiles and the power bar. Right:
  the intel (below). Keys: Q/E (bumpers) cycle the eleven slots, up/down the rows (up from the
  first row reaches the command bar), left/right the choice or the command, Enter (A) confirms,
  Esc (B) asks to quit (and autosaves). Sell, a launch with a warning and quitting ask first. The
  repair panel opens on what the credits repair and changes by 1 (left/right) or 10 (up/down)
  points. Save opens the slot list; Launch is disabled while the next level is not built (after
  Level 01, until M4). The hangar theme plays; the autosave is written as the hangar opens. Intel
  from the level's `threat_profile` data (Level 01's now structured: setting, layers, density,
  traits, hazards, boss, special limits, Varga's line per sensor level) with the directions,
  enemies, wave times and secret count derived from the script; the sensor level gates the fields
  as in [ship systems](../../player/systems/README.md#sensor-levels-and-hangar-intel), and a
  hidden field shows the sensor level that reveals it. The speaker of the hangar teaser (Varga)
  is shown with her briefing portrait and the teaser. Placeholders: the tactical-map backdrop is
  drawn in code (grid, a planet limb, range rings, an approach route), the same for every level;
  test fire keeps its box. Not built: mouse input (so the first item stays open), the per-item
  Varga lines, the L2 portraits and the boss silhouette (so the intel item stays open), test fire,
  Rook's repairs (Act 2). Until M4 only the Pulse Cannon, shield, plating and engine fly; the
  shop says "NOT YET IN FLIGHT: FROM M4" on the other items and the HUD lists them.
- 2026-10-02: User decisions on the B2 open questions: a plating swap keeps the damage, the
  missing armour points carry over (as built; rule under *Transactions*); selling an item bought
  during the visit refunds its full price like the undo, items owned before keep the normal
  sell-back (`Hangar` tracks the visit's purchases wherever they are moved and restores them with
  the undo; the sell confirmation says the full price comes back; tests `HangarTest`:
  `anItemBoughtDuringTheVisitSellsForAllSpentOnIt`,
  `ofTwoEqualItemsOnlyTheOneBoughtDuringTheVisitSellsForAll`); the missing-trait launch warning
  only names traits the sensor level shows (`Intel.markedTraits()`, L3 with the easy bonus;
  `HangarStateTest.theMissingTraitWarningNeedsTheSensorLevelThatShowsTheTraits`). Varga's per-item
  lines (M4 writing pass), the Level 02 portraits and boss silhouette (art track) and the third
  utility bay's data (M4) are deferred; the open questions say where.
- 2026-10-02: M3 close-out (user decision): the navigation and intel items are split; keyboard/gamepad navigation and the sensor-gated intel panel are done. Mouse navigation moves to M6, the test-fire box and Varga's per-item lines to M4, the L2 portraits and boss silhouette to the art track. The document is done for M3.
- 2026-10-02: Production art, UI batch part U2: the tactical map is production art
  (`tools/art/ui_scenes.py`, `assets/ui/hangar-map.png`, loaded by `TacticalMap` instead of the
  code-drawn placeholder): a cyan grid over the dark Earth seen from orbit as a holographic relief,
  its limb across the lower part, orange range rings with bearing ticks round the operation's area
  and a dashed approach route with its arrowhead; one map for Act 1, the same for every level until
  the later acts' settings get theirs (M4/M5). Every shop item of the catalogue (all 46, locked
  ones included) and Rook's escort craft has an icon (`tools/art/icons.py`, `assets/sprites/icons/`,
  16×16 and 24×24): one emblem style, a bevelled dark steel badge with the item on it, the models
  where they exist (the five wing pods with the Stormhawk's production pod materials, the Hornet's
  missile, the mortar shell, the mine, Rook's craft), otherwise per kind a symbol in the kind's
  colour (guns: their barrel block and shots in the firing direction; generator, shield, plating,
  engine, eight utility modules, three specials), the grade (Mk, Composite) as amber studs. The
  shop rows show the 16 px icon (locked items darker) on a glass row band, the selected item and the
  callouts the 24 px one; `vanguard.game.hangar.ItemIcons` derives the names (weapon slug, or kind
  and name) and fails at start if one is missing. The callouts became 46 px tall to hold the icon
  (front 160, rear 154, wings 132 px wide; the escort box 86 px), names that would not fit are cut,
  and the tile names shorten MICRO-MISSILE to MICRO-MSL and drop POD. Review files proposed for
  round 13; `art` stays `chosen`.
- 2026-10-02: Production art, UI batch part U3: the intel's sensor-L2 pictures
  (`tools/art/intel.py`, `assets/sprites/intel/`): a 30×30 sensor portrait per enemy type of
  Levels 01–02 (Skitter, Needler, Stinger, Spine Turret: the chosen round-04 models on the intel's
  dark teal scope) and a 40×40 silhouette per Act 1 boss (Gorgon Frigate, Brood Carrier: the
  outline only, filled flat with the scope's grid). From sensor L2 the panel shows the enemy
  portraits in a row above their names and the boss's silhouette at the right of its row (no
  level with a boss has data yet); the attack directions share a line where two fit, which keeps
  the panel's fields above Varga's line at sensor L3. Level 02's intel lists only the enemies of
  its waves, so its Spine Turret (a ground target) shows when M4 builds that level's intel.
  Review sheet proposed for round 13; `art` stays `chosen`.
- 2026-10-02: Concept round 13 closed (user decision): the Act 1 tactical map (`tools/art/ui_scenes.py`), the equipment icons in both sizes (`tools/art/icons.py`) and the intel's sensor-L2 portraits and Act 1 boss silhouettes (`tools/art/intel.py`) approved as **final**; `art` stays `chosen`, since the later acts' tactical maps and the intel pictures of the later enemies do not exist yet.
- 2026-10-02: M4 part A: the shop's "not yet in flight" note now marks only what the simulation does not fly yet (the specials, the utility modules, the mines and the Torpedo Pod).
- 2026-10-02 (user decision): the intel shows a level's set pieces from sensor L2 as unknown
  contacts, as Level 03's threat profile promises: "UNKNOWN HUGE CONTACT" (the unit's size tier) in
  the boss row's value column with the unit's 40×40 silhouette at the row's right, as a boss's
  (`tools/art/intel.py`, `assets/sprites/intel/boss-leviathan.png`); without a boss the label sits
  below the row's `NONE`. The content derives them from the level's `set_pieces` and the stat
  block's tier (`vanguard.content.campaign.Intel.contacts()`, field `CONTACTS`, sensor L2), so the
  name never shows and nothing is keyed to the Leviathan; below L2 nothing tells of a contact.
  Tests: `IntelTest.aSetPieceIsAnUnknownContactOfItsSizeTierFromSensorL2`,
  `PortraitsTest.everyContactOfALevelHasItsIntelSilhouette`.
- 2026-10-02 (user decision): the intel shows the teaser's speaker with the 72×72 radio portrait
  (`portraits/radio-<speaker>-<expression>`, the existing asset at its own size, not scaled) instead
  of the 144×144 briefing one, the name, `CDF INTEL` and the teaser beside it (22 characters a line),
  and the level's name below whichever ends lower: the fields move up about 64 px. Captured before
  Level 03 at sensor L2 and L3, the panel ran out at the bottom (the wave strip, the secrets and
  traits rows over Varga's four-line line, which itself ended below the panel); now everything
  fits. Varga's line now ends at the panel's foot whatever its length (its last line 10 px above
  the bottom edge) instead of starting at a fixed height. `IntelPanel` lays out on a canvas, and
  `IntelPanelLayoutTest` checks every level at every sensor level: no two parts overlap, nothing
  leaves the panel, every field ends above Varga's line;
  `PortraitsTest.everyHangarTeaserHasItsSpeakersRadioPortrait`.
- 2026-10-03: Concept round 16 closed (user decision): the intel portraits of the Spore Bomber and the Whirl Seed and the Leviathan's "unknown huge contact" silhouette approved as **final**; this doc's `art` stays `chosen`.
- 2026-10-03: Concept round 17 (user decision): the intel portraits of the Brood Pod and the Scuttler approved as **final** ([round 17](../../concept-rounds/round-17/README.md)).
- 2026-10-05: Concept round 23 (user decision): the intel portraits of the Mantis and the Coilwyrm approved as **final** ([round 23](../../concept-rounds/round-23/README.md)).
- 2026-10-05 (user decision D4 of M4 part H): Varga's intel lines are **one line per sensor level**
  per level (none, L1, L2, L3), text only and not voiced, instead of one per intel item; the "one
  line per intel item" item is closed by it. Part H wrote the 21 L1–L3 lines of Act 1 beside the
  existing no-sensor lines (each level's `threat_profile.varga`): each tells what the better sensors
  reveal at that level and stays within the fields the panel shows there. The data now needs all
  four lines (`LevelData.ThreatProfile`), and `Intel.varga()` gives the fitted sensor level's line
  instead of falling back to a lower one. Tests: `IntelPanelLayoutTest`
  (`everyLevelHasVargasLineForEachSensorLevel`: four different lines, no word cut; every level at
  every sensor level fits the panel, with and without an objective row). Level 07's boss silhouette
  and two rows of enemy portraits leave its L2 and L3 lines one panel line (30 characters).
- 2026-10-05: M4 part H, test fire: the shop's box (264×64 below the message lines) loops the
  selected weapon in the real simulation (`vanguard.game.hangar.TestFire`): the ship, with only
  that weapon fitted in the slot being shopped (the starter engine, shield and plating, nothing
  hits it), climbs unseen to the middle of the field and holds fire while three dummies on the
  ground drift down past it at 90 px/s, one on the weapon's line (a wing weapon's 12 px out
  towards its pod) and one 36 px either side, so a forward gun meets them ahead, a side gun
  beside the ship, a rear gun behind it, a bomb under its pod, a mortar shell on its snap and a
  homing missile on its turn. A dummy takes two of the weapon's level-1 hits (one level-1 bomb or
  shell blast), so a higher level kills sooner; when all three are gone (at most about 8 s) the
  loop starts over with the sortie's own retry, the same every time. The box shows the play field
  turned a quarter clockwise at half size (the wide box holds the field's length: ahead is right,
  behind left, the ship's left up), drawn with the level's pieces (`vanguard.game.render.TestFireView`:
  the ship with its pod, the weapon's shots, muzzle flashes, impacts and explosions) over a grid
  moving with the scroll, the dummies as amber target boxes flashing white when hit. Level: the
  detail's level, or the next one while the UPGRADE choice is highlighted (the purchase's result,
  label "TEST FIRE L1>L2"); locked weapons loop at L1; the Act 2 mines and torpedo say "NOT YET IN
  FLIGHT", other items "WEAPONS ONLY". The loop restarts whenever the weapon, slot or level
  changes; stepping allocates nothing; silent until the shop sounds are wired. Tests:
  `TestFireTest` (every flown weapon in every slot it fits at every level hits a dummy where it
  should, the loop is deterministic, starts over and allocates nothing), `TestFirePanelLayoutTest`
  (the box fits the drawer below the message, the hull and every lane fit across it, nearly the
  whole field along it, the labels fit). Capture for round 26: test-fire-capture-r26-a.
- 2026-10-05: Concept round 26 closed (user: the round accepted as proposed): the test fire accepted as built, with the three proposals of its questions: (a) the box shows the play field **turned a quarter clockwise** at half size (ahead is right, the ship's left up); (b) **locked weapons** loop at L1; (c) with the UPGRADE choice highlighted the box shows **the next level** ("TEST FIRE L1>L2"), the purchase's result. The capture is accepted. Varga's 28 intel lines (one per sensor level per level, D4 = A, not voiced) accepted as written, Level 07's short L2 and L3 lines included. `art` stays `chosen` (the later acts' tactical maps and intel pictures do not exist yet).
- 2026-10-05: M4 close-out: the open question on the **third utility bay** is closed. Its data
  entry is made in [ship systems](../../player/systems/README.md#utility-bays-confirmed) (5 000 cr,
  `available: act 3`); the hangar sells it from the visit before Level 15 (`Hangar.available(Bay)`),
  so no Act 1–2 visit offers it and the save is unchanged. The shop keeps its two UTL slots; buying
  the bay and the third UTL slot come with Act 3 (tracked in ship systems).
- 2026-10-06: M5 part A (user decisions D3 = b and D4 = a, see
  [wingmen](../../player/wingmen/README.md#decisions)): the escort UI is designed under *Escort*:
  the callout with his gun, armour and side; his four guns in the shop like a front gun's (escort
  inventory, 60 % prices, the visit's undo); a side toggle on the escort's detail (the L08 HUD hint
  "Rook's side can be set in the hangar"); a `ROOK` repair line at the player's repair cost; launch
  warnings for his armour below 50 % and for a grounded Rook. Main-agent choices: the side toggle
  is a setting outside the undo; his guns are test-fired from one nose muzzle; the mines' test fire
  needs dummies on `air`, since mines trigger only on `air` and `low-air`. The document is
  `in-progress` again while M5 builds it.
- 2026-10-06: M5 part A, the escort UI as built (main-agent choices, for review with concept
  round 28): once Rook is hired the escort is a tab after the rear mount; his callout grows to
  92×92 px under the left wing's callout (the escort title with his side `L`/`R`, his icon and
  `ROOK`, his fitted gun's name with five pips, his armour, amber below the launch warning's 50 %, or
  `GROUNDED` in red), so his longest gun name fits. The side is a last row `ROOK'S SIDE` in the
  escort's shop: left/right pick the side, confirm toggles it, its detail shows `LEFT` / `RIGHT`. The
  repair panel lists `SHIP` and `ROOK` with their missing points; Q/E switch the line (each keeps the
  most points the credits pay for), left/right and up/down set the points as before; it opens on the
  ship's line while that has points missing it can pay for. The launch warnings read `ROOK'S ARMOUR
  34/80` and `ROOK IS GROUNDED AND STAYS HOME`. His guns are test-fired from his craft (his nose
  muzzle at his scale, the dummies as tough as the scale asks). The side change was part of the
  visit's undo as first built (M5 part A1), against *Side* above; closed in the next entry.
- 2026-10-06: user decision: Rook's side stays outside the visit's undo, as *Side* says. A side
  change pushes no undo step, and undoing an earlier transaction keeps the side he has now
  (`Hangar.escortSide`, `Hangar.restore`; test `EscortTest.hisSideIsAHangarSettingOutsideTheUndo`).
- 2026-10-06: The module tiles' names fit their 48 px (M5 part A: names such as `AIRSTRIKE`,
  `SMART BOMB` and `TARGETING COMPUTER` are wider than the tile): at most 6
  characters of the 8 px label font, with the abbreviations under *Panels* (`Names.tile`; the weapon
  callouts use `Names.callout`). For the Salvage scanner `SALVGE` over `SCANNR`: the tile says what
  the module is for (salvage) and `SCANNR` would read like the Sensor suite, the other scanner in
  the utility bays. Test `LoadoutTilesLayoutTest`: every generator, shield, plating, engine,
  special and utility module fits 6 × 8 px, and those of Acts 1–2 (unlocked by Level 14) fit by
  their model or an abbreviation, never by the fallback cut. Later parts still fall back (`CMPIII`,
  `EVASIV`, `AUTO-R`, `PRESSU`, `ASCEND`) until their acts give them abbreviations.
- 2026-10-06: The capture's "1 FREE SMART BOMB CHARGE" notice was an artefact of its hand-made save
  (placed at Level 08), not a bug: the visit gave the Airstrike's and the Smart Bomb's free charges
  at once, and `HangarState.notice` keeps only the last notice. In a played campaign the two gifts
  come at different visits (their unlocks, Levels 04 and 06). No change.
- 2026-10-06: Concept round 28 closed (user: accepted): the escort UI approved as built (the escort
  tab after the rear mount, the 92×92 callout with his side, icon, gun and pips, armour amber below
  50 % or `GROUNDED`, his gun shop at 60 % with the visit's undo, the `ROOK'S SIDE` row outside the
  undo, the `SHIP` / `ROOK` repair lines, the two launch warnings, his guns' test fire from his
  nose), and the module tiles' abbreviations (`CMP I`, `CMP II`, `STRIKE`, `S-BOMB`, `FLARES`,
  `SENSOR`, `MAGNET`, `TARGET`, `SALVGE`) accepted; the later acts' modules keep the cut fallback
  until their acts. Every item but mouse navigation (M6) is ticked, so the document is `done`
  again; `art` stays `chosen` (the later acts' tactical maps and intel pictures do not exist yet).
- 2026-10-07: The intel panel's line under the teaser speaker's name is the briefing speaker's role
  line, no longer a fixed `CDF INTEL` (Level 08's capture, round 30: Rook's teaser read
  `LT. K. TANAKA / CDF INTEL`): `AEGIS TWO` for Rook, `CDF INTELLIGENCE` for Varga, `CDF COMMAND` for
  Okafor (Level 04's teaser) (`Speaker.role`; `IntelPanelLayoutTest.theTeasersSpeakerShowsWithTheirNameAndRole`;
  every level's intel still fits at every sensor level).
- 2026-10-07: [Concept round 30](../../concept-rounds/round-30/README.md) closed (user,
  2026-10-07): the Creeper's intel portrait (re-rendered with its readability lift) approved as
  **final**. Asked separately in the round (question 17), the user **keeps the role labels**: the
  line under the teaser speaker's name comes from the speaker's role (`Speaker.role`): Varga
  `CDF INTELLIGENCE`, Okafor `CDF COMMAND`, Rook `AEGIS TWO`. This supersedes the fixed
  `CDF INTEL` of the 2026-10-02 decision; `IntelPanelLayoutTest` passes for every level at every
  sensor level with the longer label. `art` stays `chosen`.
- 2026-10-07: M5 part C (user decision D7 = a): a level's threat profile can mark a trait
  `required`; its launch warning shows at every sensor level (Level 09's briefing already says that
  the nodes need `anti-ground`) and counts every source that damages hardened targets: an
  `anti-ground` weapon, Rook's Mortar while he flies, an Airstrike or Smart Bomb charge. Rejected:
  b (keep the sensor-L3 rule and only add Rook's gun and the specials: most players would launch
  into a mission they cannot win without a word) and c (as built: Level 09's text would be wrong).
  The document is `in-progress` again for it.
- 2026-10-07: M5 part C, game side: the required trait's warning reads `NO ANTI-GROUND SOURCE
  FITTED` (as long as a weapon's warning, so the launch dialog keeps its width) and comes first; a
  required trait is not warned about a second time by the sensor-L3 rule. Sources: a weapon with the
  trait in any slot, Rook's fitted gun while he flies the level (hired, not grounded), and for
  `anti-ground` an Airstrike or Smart Bomb fitted with at least one charge.
- 2026-10-07: [Concept round 31](../../concept-rounds/round-31/README.md) closed for the intel
  portraits (user, 2026-10-07): Level 09's Hive Node (iris half open) and Ravager (running down)
  approved as **final**.
- 2026-10-08: M5 part D, game side (user decision D9 = a): Level 10's required `rear` is met only by
  the weapon in the rear slot, and only one that fires behind the ship: the Tail Gun, the Fan
  Blaster and the Proximity Mines carry the `rear` trait; the Side Splitter (rear slot, trait
  `side`) fires sideways and does not count; Rook's guns and homing never do. The warning reads `NO
  REAR WEAPON FITTED` (only a weapon can bring it; shorter than Level 09's line). Our reading, for
  review in round 32: the Side Splitter does not silence it. The intel panel's level name drops to
  the label font when it would run into the sensor chip (Level 10's `EVACUATION CORRIDOR`, 19
  characters).
- 2026-10-08: [Concept round 32](../../concept-rounds/round-32/README.md) closed for the intel
  portraits (user): Level 10's Wraith and Mote Swarm approved as **final**; the build choices on the
  hangar (the Side Splitter not silencing the rear warning, the intel's long level name in the label
  font) accepted as built.
- 2026-10-08: M5 part E (stated defaults with the user's decisions of 2026-10-08): the Torpedo Pod
  is test-fired over a water strip with a submerged dummy, the last weapon's "NOT YET IN FLIGHT"
  label going with it. Level 11's threat profile recommends `spread` and `anti-ground` and marks
  `anti-sub` optional, not `required`, so no new launch warning (the sensor-L3 rule as before).
- 2026-10-09: [Concept round 33](../../concept-rounds/round-33/README.md) closed for the intel
  pictures (user): the Driftjelly's and the Reef Spitter's portraits and the Harbour Kraken's
  silhouette approved as **final**, the weak spots as they are (the dark Reef Spitter, the Kraken's
  silhouette without arms); Rook's teaser and Varga's four Level 11 lines approved (L2 and L3 as
  shortened to fit the panel).
- 2026-10-12: M6 part A: mouse navigation as under *Design*, pending the user's review in concept
  round 35. Main-agent default: slots are chosen by a click, not by hover. The command bar's and
  the repair panel's layout moved into `HangarPointer`, which the drawing and the hit regions share.
- 2026-10-12: M6 part B (Q1 = a, pending the user's review in round 35): the top bar's mission line
  before a level that is not built reads `ACT III - RED DUST: COMING SOON`
  (`ReleaseEndScreen.COMING_SOON`); after Act 2 the hangar is reached through Continue, since the
  outro now leads to the release's end.
- 2026-10-12: Back to `design: review` until [concept round 35](../../concept-rounds/round-35/README.md) closes (C4's coming-soon line and the mouse, A.1).
- 2026-10-12 (user, [round 34](../../concept-rounds/round-34/README.md)): the Lamprey's and the Skimmer's
  intel portraits and the Siege Spire's silhouette (12.2e, 13.2e, 14.2g) approved as **final**; Rook's
  teasers and Varga's intel lines for Levels 12–14 accepted. `art` stays `chosen`; `design` stays in
  `review` for concept round 35.
- 2026-10-12 (user, round 35): accepted as built: C4's coming-soon line (`ACT III - RED DUST: COMING
  SOON` before Level 15), the mouse in the hangar and the build choices A.1 (a)–(g) (slots chosen by
  a click, shop rows, chips and commands by hover) and D.3 (a) (the intel's "FOLLOWS IN A LATER
  RELEASE."); `design: approved`, `art` stays `chosen`.
- 2026-10-12: Step K10: the wave strip marks the arrival of a level's boss or mid-boss (its
  placement's `t`) with a red tick the strip's depth, drawn over the waves; Level 14's and Level
  11's *Sensor-suite detail* promised it. Levels 05 and 07 get the same tick, which their documents
  do not spell out (pending the user's review).
- 2026-10-13 (user, round 36, L.2): accepted: the red full-height BOSS tick on the intel's wave strip at the boss's time (sensor L3) on every boss level (L05, L07, L11, L14).
- 2026-10-13: M7 part A, step A5 (defaults pending the user's review): the third bay's `UTL` tile
  sits in the schematic's lower right corner, opposite Rook's callout (the module row has no room
  for an eighth 48 px tile); the bay is bought from a bay row at the end of the utility slots'
  shops and sold from its own slot's shop, and is not listed before the L15 visit (no locked row);
  the Act 3 specials' tiles read `EMP`, `LANCE` and `OVRCHG`; the Deflector Pod's test fire has
  the dummies fire a slow (110 px/s) harmless bullet at the ship every 0.6 s; a dummy stands 1 s
  of the L1 Ion Beam.
- 2026-10-13: M7 part A, step A8 (round 37's capture findings R.4 e–g, **pending the user's
  review**): a loadout already over its generator's output (a hand-written save, or one from before a
  draw changed) is refused for power only by a change that widens the gap, so it can still fit a
  special (no draw) or a lighter item; the detail of an item bought this visit reads `BOUGHT THIS
  VISIT: FULL REFUND` where its `VS FITTED:` line would stand, and that line is left out when nothing
  differs; the visit's free-charge notice leaves the message line on the next move or slot change
  (a transaction's message still stays until the next one); after buying a bay the cursor goes to the
  bay's new slot, on its first row.
- 2026-10-13 (user, round 37): the Act 3 icons **a** chosen (P.3: each item drawn as its effect); the
  emblems (b) rejected and moved to `concept/rejected/` (`tools/art/icons.py` routes the unchosen sheet
  there). The hangar capture accepted (K.4); step A5's defaults kept (B.4 a–e: the `UTL` tile in the
  lower right corner, the bay row from the L15 visit with no locked row, the tiles `EMP`, `LANCE`,
  `OVRCHG` and `DRONE BAY`, the test fire's slow dummy bullets, a dummy standing 1 s of the L1 Ion
  Beam); R.4 fix all: step A8's fixes (e)–(g) above kept; save format 4 accepted (V.1); the balance
  plan's purchases re-planned for R.1's act factor (step Q1).
- 2026-10-14 (M7 part B, step ART-B): production art from the chosen round-36 looks, review sheet `intel-final-r38-a` proposed for round 38 (pending the user's review).
- 2026-10-14 (user, round 38): the Burrower's and Dust Kite's intel portraits `intel-final-r38-a` approved as
  final (F.5); B.2 (d), the intel strip marking the six Burrower fields (`BURROW_FIELD`), kept. `art` stays
  `chosen`.
- 2026-10-18 (user, round 39): the Tendril Anchor's and Threadcrawler's intel portraits `intel-final-r39-a`
  approved as final (F.5); B.4 (f), the intel strip's GATE and CRAWLER ticks, kept. Level 16's L3 intel line
  now "Kill an anchor to drop a tendril. A cache down the left fork." and its traits `forward`, `piercing`
  (R.4, in the level's data). `art` stays `chosen`.
- 2026-10-18 (user decision, Act 2 playtest): the hangar after Continue and Load game comes after
  the next level's briefing; the mission select replay gets the replay hangar (`REPLAY L05`
  header, `> REPLAY` launch; its changes kept in the campaign, written to the autosave at launch,
  the latter pending the user's review).
- 2026-10-19 (user correction of the 2026-10-18 choice): the replay hangar is refit only ("purchasing
  items seems odd at this point"): fitting and unfitting what the campaign owns stays and is kept;
  buying, selling, upgrading, bays and repairs are gone from it. The buyable and locked rows are
  hidden rather than greyed, with `REPLAY: REFIT ONLY` on the message line (pending the user's review).
- 2026-10-19 (user, round 40, F.f): the Choir Herald's and the Shellback's intel pictures approved as final.
- 2026-10-19 (step IP, pending the user's review): the intel's fields close up on a busy level instead of
  running into Varga's line. After the Act 2 wave rework Level 14 at sensor L2/L3 names eight enemy types
  (Stingers, Mantises and Spore Bombers joined), four lines instead of two, and with the boss silhouette its
  WAVES row ran 6 px into the quote. `IntelPanel` now measures the fields first: their rows stay 14 px apart
  where they fit (every other level, unchanged), else close up a pixel at a time to 12 px (Level 14: 13 px),
  and only if that is not enough the enemy names end on `+N MORE` in as many lines as fit. Tests:
  `IntelPanelLayoutTest.aBusyLevelsRowsCloseUpAndKeepEveryName`, `tooManyEnemyTypesEndOnMore`.
- 2026-10-19 (user, round 41): the intel pictures (F.e) approved as final; the intel panel's closing rows and "+N MORE" (P.f) accepted.
- 2026-10-19 (user's playtest): hover only gives a row, chip, slot or command the hover look and never selects; a click selects, a click on a choice or command makes it; a chip click returns the focus from the command bar to the shop; a key press takes the hover look off. Replaces round 35's A.1 choice "shop rows, chips and commands by hover". While the pointer rests on one of the selected row's choices the test fire and power bar preview it (pending the user's review).
- 2026-10-20: round 42 closed: the Level 19 intel pictures (F.e) approved as final by the user; the shop's mouse handling (T.6: the selection stays with the clicked item) accepted.
- 2026-10-20 (user, round 43, Q.5): the hangar's art moves to `final` for the approved items only (the rear-guard bay's icon, F.d); `art` stays `chosen` for the rest. The user asked "When do we move the remaining hangar and briefing art to final?": an audit step lists the `chosen` items left in `ui/hangar` and `ui/briefing` for the next round.
- 2026-10-20: the Dust Colossus's intel silhouette (`boss-dust-colossus`) made with the part H batch (`tools/art/intel.py`, step ART-H2) and its review sheet generated for [round 44](../../concept-rounds/round-44/README.md) (F.d); pending the user's review.
- 2026-10-20 (user, round 44, A.a): the audit approved: `hangar-r07-b` (a layout mockup) moved to `concept/rejected/`; the 22 review sheets and captures of production assets (the Act 1 map and icons sheet, the intel sheets r13-r42, the test-fire and Act 3 hangar captures, the Act 3 icons) marked `final`; the Dust Colossus's intel silhouette (F.d) approved as final; **`art: final`** for the hangar. A later addition needs its own row.
