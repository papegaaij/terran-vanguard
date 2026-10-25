# HUD — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-09-30: Status info in the side panels; the play field stays clean.
- 2026-09-30: Edge warnings for side and rear waves are always shown; the sensor suite only adds
  arrows for individual off-screen threats. Power gauge shows enemy drain.
- 2026-09-30: Concept round 01: HUD **A** (metallic bevelled panels, LCD readouts) chosen; B (glass/neon) rejected as not fitting the style.
- 2026-09-30: Converted to the 960×540 baseline (was 640×360).
- 2026-09-30: Concept round 02: HUD A at 960×540 with 240 px panels in palette B confirmed.
- 2026-10-01: Concept round 08: accepted.
- 2026-10-01: Concept round 09: edge warnings chosen.
- 2026-10-01: Objective tracker added to the left panel (user decision) for the objective levels L02, L04, L05, L09, L10 and L13. No concept art for it yet; it follows the HUD A style.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-01: M1 placeholder HUD (`vanguard.game.render.HudPanels`): metal panels drawn in code with colours sampled from `hud-r08-a.png` and libGDX's built-in font until the UI kit and its bitmap fonts exist; right panel armour and shield bars with numbers (the shield bar flickers while down after a break) and the front weapon with level pips; left panel the test sortie's name and attempt number.
- 2026-10-02: M2 placeholder left panel (`vanguard.game.render.MissionPanel`): mission
  number and name, score, credits (launch balance plus earnings), chain with its window bar,
  radio with the speaker's 72×72 portrait (cut from the chosen portrait sheets: Rook, Okafor,
  Varga r04, the Choir r08, static) and the typed subtitle with squelch and typing blips, the
  first section's control prompts, a secondary-objective tracker and the progress bar; in the play
  field the edge-warning arrows and floating credit numbers. Not yet: queue interrupts for urgent
  warnings, warning banners, power, special and escort.
- 2026-10-02: The objective tracker is shown for every secondary objective too, so Level 01's
  `KILLS n / 76` counter stays (user decision); showing it only in the listed objective levels
  was the alternative.
- 2026-10-02: Radio lines keep paging (3 × 22 characters per page, typed, each page held 1.5 s),
  with the writing rule that a line is at most two pages (user decision); shortening every line to
  one page was the alternative.
- 2026-10-02: The two-page rule counts word-wrapped lines, not characters (user decision); Level 07's t=95 line (3 pages) is shortened when Level 07 is built in M4.
- 2026-10-02: Radio pages hold twice as long (3 s, the last page 5 s; was 1.5 s and 2.5 s) and edge warnings start at least 3 s ahead (was 1.5 s), user decision after playing Level 01: with the eyes on the action there was too little time to read the radio, and the side and rear warnings were easy to miss.
- 2026-10-02: Left panel layout (after playing Level 01: "crowded, the instructions overflow
  their box, the boxes are crammed together"): a fixed stack of regions with one gap (see *Left
  panel layout*), the level's name as text under the mission plate as on `hud-r08-a.png`, and
  every text cut off at its well instead of running over it. The control prompts get their own
  three-line region between the radio and the objective tracker: the doc only says "side HUD";
  prompts also appear mid-level next to an objective tracker (L02, L04's escort, L12), so they
  cannot take the tracker's place, and the right panel fills up with power, special and escort.
  Prompts are one line each (action and keys), with the arrow keys as `ARROW KEYS` (the four
  key names did not fit). Not chosen: prompts in the play field (the play field stays clean).
- 2026-10-02: Control prompts keep their own region in the left panel under the radio (user decision); they appear mid-level next to objective trackers too, so they cannot share the tracker's place.
- 2026-10-02: M3 part A: the HUD draws with the UI kit's bitmap fonts instead of libGDX's
  built-in font, in the same regions: 8×12 for the plates, the radio subtitles and the control
  prompts (a 22-character radio line is 176 px, inside the 196 px well; in the 10×20 font it would
  be 220 px), 10×20 for the mission name, the readouts, the chain, the speaker's name, the tracker
  and the right panel's numbers and weapon. `MissionLayoutTest` now measures with these fonts. The
  [ui](../README.md) rule "a 240 px side panel fits about 22 body characters" does not hold for the
  10×20 body font in a 196 px well (19 characters): open question there. Text speed and flash
  reduction from the Gameplay tab apply to the radio and the hit flashes.
- 2026-10-02: Concept round 11 proposed: three edge-warning looks (A big pulse, B sweeping chevrons,
  C edge glow band) in the round-09 warning amber `FFC800`, plus two warning tones in
  [sfx](../../audio/sfx/README.md). The M2 placeholder draws a small red (`FF4030`) arrow, not
  the chosen round-09 look; whichever variant is chosen replaces it.
- 2026-10-02: Concept round 11 (user choice): edge-warning look **A** "big pulse" chosen (the round-09 bar and chevrons at about 3× with a glow, growing in over 0.2 s and pulsing between 45 % and 100 %); B and C moved to `concept/rejected/`. The round-09 sheet stays chosen for the sensor threat arrows and the banners. The game still draws a small red placeholder arrow; it follows A when the HUD art is implemented.
- 2026-10-02: Characters per line settled in [ui](../README.md) (user decision): radio subtitles and
  control prompts use the 8×12 font, as built; body text fits about 19 characters in a well.
- 2026-10-02: M3 part B1: the edge warnings follow the chosen look A "big pulse"
  (`vanguard.game.render.EdgeWarnings`, drawn in code with the generator's geometry: a 6 px
  `FFC800` bar 301 px long at the sides and 341 px at the rear with a blurred glow, three 20 px
  chevrons at 100 / 70 / 42 %, "! LEFT" / "! RIGHT" / "! REAR" in the 10×20 font, growing in from
  the edge's middle over 0.2 s, then pulsing 45–100 % on the 0.267 s cycle); the small red
  placeholder arrow is gone. Each warning that starts plays the edge-warning tone
  ([sfx](../../audio/sfx/README.md)). The label is the 10×20 font, close to the concept's 2× 5×7
  raster font.
- 2026-10-02: M3 part B2, TEMPORARY until M4: the right panel's front weapon shows the Pulse
  Cannon's flown level, and under the weapons a "NOT YET AVAILABLE" list names the fitted items
  the sortie leaves out (other weapons, the special, utility modules), so a player who fitted them
  in the hangar sees why they do not fire. It goes when M4 flies them.
- 2026-10-02: M3 close-out: the radio portrait opens and closes through the transmission static of
  the [briefing](../briefing/README.md) (`vanguard.game.render.TransmissionStatic`): it fades out in
  the first 0.35 s of a message and in over the last 0.35 s of its last page
  (`RadioQueue.sinceOpened()` / `untilClosed()`). Priority interrupts stay open, so the radio item
  does too.
- 2026-10-02: The outro waits for the radio (user decision after playing the final-art build): the debrief follows once the radio has shown its last message, at most 10 s after the level end (was a fixed 5 s, which cut Okafor's level-end line short), so the player can read it.
- 2026-10-02: The outro cap is 15 s instead of 10 s (user decision), so the level-end and secondary-objective lines both fit when they queue together (about 14 s); Earth's limb and the Vrell glow moved later in the data (t=189.6 and t=202.8) so their top edges stay above the screen; the art is unchanged.
- 2026-10-02: Production art, UI batch part U1 (user: "It should be metallic bevelled panels, but
  at the moment it's flat and no texture at all. Also the rivets in the corners are missing."):
  the HUD's metal parts are rendered by `tools/art/hud.py` in the chosen HUD A look and drawn by
  `vanguard.game.render.HudKit` instead of flat fills, in the same regions and with the same
  fonts. The side panels are whole 240×540 plates (the screen is a fixed 960×540, and a
  stretched centre would smear the brushed texture): a 3 px chamfer on a dark chassis, four domed
  rivets in recessed washers, brushed streaks and mottling on palette B's UTC HULL ramp,
  ordered-dithered on the face. The label plate is a fixed 120×22 piece; the LCD well (also every
  bar trough and the weapon box), the portrait well with an idle CRT screen, the phosphor fill
  cell (tinted per bar, segment, pip and the tracker flash) and the readout glow (tinted green or
  amber, 28/255) are nine-patches. Armour and shield are segmented gauges (13 px pitch) as on the
  concepts; chain and progress stay continuous. The key light sits lower than the sprites' (about
  27° instead of 43°) so a 45° bevel reads. Review files proposed for round 13; `art` stays
  `chosen` until the user approves them.
- 2026-10-02: Production art, UI batch part U3: the radio shows the production portraits
  (`tools/art/portraits.py`, [characters](../../story/characters/README.md)) in the radio line's
  `expression` (neutral when not given), the Choir's glyph as its 32-frame loop at 12 fps from the
  moment its message opens. Level 01's lines set grim or fierce where the text calls for it:
  Okafor's "Contacts inbound. Weapons free." and "Contacts on your six, Lancer!" and Rook's
  "Movement on your left!" fierce; Varga's "Those ones are armed." and "That isn't noise…" and
  Okafor's level-end "…The rest are coming." grim; the rest neutral.
- 2026-10-02: Concept round 13 closed (user decision): the HUD's production metal parts (side-panel plates, label plate, LCD and portrait wells, phosphor fill, readout glow; `tools/art/hud.py`) approved as **final**, `art: final`.
- 2026-10-02: M4 part A: the right panel shows the power row (spare power as a bar, the regen bonus as a number, glowing amber during an overdrive), a weapons box with one row per slot (F, R, L, R as in the mock: the weapon's name, a pod named by what it fires, and five level pips) and the overdrive timer row (ten pips and the seconds left). "Not yet available" now lists only the special and the utility modules.
- 2026-10-02: Concept round 14 closed (user decision): the right panel with the power row, the weapon rows and the overdrive timer approved as final.
- 2026-10-02: M4 part B: the tracker shows a group objective (Level 02's docks) as its label ("DOCKS") and a pip per dock, dim while open, green when saved, red when lost, flashing as it changes; a level's timed prompts (Level 02's `GROUND` · `FLY OVER, FIRE`) share the control prompts' well.
- 2026-10-02: M4 part C (choice for review): an escapes objective (Level 03's "nothing gets
  through") shows its enemy's last word in plural as the label (`BOMBERS`) and the units destroyed
  of all the level sends (`n / 10`), then `DONE` in green or `FAILED` in red, the whole box
  flashing green or red as it is won or lost; a timed prompt with a `skip` layer leaves once an
  enemy on that layer is destroyed (Level 03's `LOW-AIR` prompt; Level 02's `GROUND` prompt now
  says `skip: ground` in its data instead of a special case in the game).
- 2026-10-02 (user decision): radio queue priorities, after Level 03's captures showed the lines
  of the Leviathan's first pass playing seconds late behind an escaped bomber's line: timed lines
  go first; an event line waits for a gap that ends before the next timed line is due and is
  dropped as stale after 6 s of waiting (`RadioQueue.STALE_SECONDS`, `RadioQueue.Priority`,
  `vanguard.game.level.RadioSchedule` for the time to the next timed line); the level-end and
  secondary-objective lines are never dropped, since the outro waits for them (our reading of the
  rule). The queue has no clock or randomness of its own. Tests: `RadioQueueTest`, and
  `RadioTimelineTest` for Levels 01–03 with the event lines a player can set off (every timed line
  within 1 s of its time; Level 01's Varga line after the Choir is exempt, it follows that line).
- 2026-10-03 (user decision): the radio reading above accepted as it is: the level-end and secondary-objective lines (`RadioQueue.Priority.CLOSING`) are never dropped.
- 2026-10-03: Concept round 16 closed again (user decision, choice 16): the M4 part C choice for review above accepted as it is: the escapes objective's tracker (`BOMBERS n / 10`, then `DONE` in green or `FAILED` in red with the box flashing) and the timed prompt with a `skip` layer (Level 03's `LOW-AIR`, Level 02's `GROUND` via `skip: ground`).
- 2026-10-03 (user decision, M4 part D): a two-line objective tracker for levels with a primary and
  a secondary objective (L04, L05, L13); it takes one line from the control prompts' well. Level
  04: `CRAWLERS` with five pips (green; amber below 50 %; a white flash on a hit; a red flash, then
  dark, when lost) and `PODS n / 6`, then `DONE`/`FAILED`. The region bounds (prompts 384–430,
  tracker 436–482) follow from the well sizes (main-agent choice).
- 2026-10-03: M4 part D step 2 (main-agent choices): an **urgent** radio line (the Airstrike's
  "Hammer flight, inbound!", which had queued as an event line and could be dropped behind long
  lines) interrupts the line on the radio at once, without the gap, and the interrupted line plays
  again from its start right after it (`RadioQueue.Priority.URGENT`). The two-line tracker is
  drawn when the level has a convoy: line one is the ally's label (`CRAWLERS`) and a pip per unit
  (the `civilian-crawler-pip` sprite tinted green, amber below 50 % HP, white for 6 steps after a
  hit, a red flash for 1.5 s then dark when lost; the line flashes red when the last one is lost),
  line two the secondary objective as in a one-line tracker; the control prompts show two lines
  then.
- 2026-10-03: The radio is spoken ([voice](../../audio/voice/README.md)): the voice starts with
  the message, a page is held until its voice has reached it, the music ducks while the voice
  plays and an urgent line cuts it.
- 2026-10-03: M4 part E (main-agent choice): the boss bar and name sit at the top of the play
  field, not in the side HUD; it shows the sum of the boss's remaining part HP, shorter for a
  mid-boss. The tracker for Level 05 reads `BATTERIES A B C D`, a letter struck through once
  that battery is cleared.
- 2026-10-03: M4 part E, Level 05's tracker built: line one `BATTERIES` and the letters A–D (dim
  while open, struck through in green once cleared, red when lost, the line flashing red as the
  primary fails), line two the *Scorched crater* count `NEST n / 30`, then `DONE` or `FAILED`; the
  batteries' units carry a 1 px outline in the objective colour (amber `FFE04A`, a placeholder
  until the production round).
- 2026-10-05: Round 25 closed (user): the boss warning banner accepted as built (Level 07's
  capture); the warnings item split into what is built (edge arrows, credit numbers, the boss
  banner) and what is not (wave banners, the sensor's threat arrows, the numbers' option).
- 2026-10-05: M4 part H (defaults of the part's plan; choices for review in round 26):
  - **Wave banners** (`vanguard.game.render.WaveBanners`): no data of their own; an edge warning that
    starts opens "WARNING — HOSTILES FROM THE REAR / LEFT / RIGHT" (both sides "LEFT AND RIGHT",
    a side with the rear "REAR AND LEFT", all three "ON ALL SIDES") with its tone, in the round-09
    look: a 440×30 dark band with an amber frame (pulsing with the edge warnings) and a light top
    line, the 10×20 font in amber, its middle 376 px above the bottom edge (above the boss banner's
    band). It opens over 0.15 s, shows 2.5 s and fades out over its last 0.3 s. One banner at a
    time: a warning that starts while one shows waits and gets its banner after it if its edge is
    still warned, else it is dropped. Drawn in code until production art.
  - **Threat arrows** (`vanguard.game.render.ThreatArrows`, user decision D2 = A): with the fitted
    sensor suite at L2+ (the easy difficulty's +1 is the intel's only), an amber arrowhead with a
    dark outline (10–18 px, 59–100 % opaque as the enemy closes in from 240 px out) sits 14 px
    inside the edge at the enemy's place along it, pointing out at it; for every air enemy off the
    screen that is coming in (not moving away; ground units scroll in, in sight). Enemies within
    28 px along an edge share one arrow, at most eight show, and arrows also show under an edge
    warning (the warning names the edge, the arrow where along it). The simulation knows a unit only
    once it spawns 40 px outside its edge, so a wave's units show their arrows for about 0.3 s
    before they enter; a Coilwyrm's body coming up from the rear shows them for 1–2 s. Its dive out
    on the loop is not tracked (it moves away). No homing enemy missiles exist in Act 1, so the
    concept's ringed missile arrow is not built. The sensor suite is no longer listed under "NOT
    YET AVAILABLE".
  - **Credit numbers option**: the Gameplay tab's `CREDIT NUMBERS` (OFF / ON, default on,
    `gameplay.credit-numbers`) hides the floating numbers; they are still followed while hidden.
  - **Low-armour flash** (`vanguard.game.level.LowArmour`, `ShipPanel`): at or below 30 % of the
    max armour the armour number turns red and a red wash with a glow lies over the gauge, lit
    0.6 s and dark 0.6 s; at or below 15 % 0.3 s each, one flash per low-armour beep (the beeps'
    1.2 s and 0.6 s periods and thresholds); on simulation steps, so it stops in the pause.
  - **Boss bar plate**: `vanguard.game.render.BossBar` draws the bar on `hud/boss-bar-plate`
    (`assets/sprites/hud/boss-bar-plate.9.png`, a nine-patch stretched to the bar's 400 px or a
    mid-boss's 240 px, the fill in the nine-patch's content box, at the plate's own height) once it
    exists, and the plain bar until then.
- 2026-10-05: M4 part H (round 26 batch): the boss bar's production plate `hud/boss-bar-plate`
  (`assets/sprites/hud/boss-bar-plate.9.png`, `tools/art/boss_bar.py`): 64×20 nine-patch, splits
  left/right 14 and top/bottom 7, padding left/right 11 and top/bottom 7, so the content box is
  the trough's 6 px glass the fill goes in; it stretches to 422 px round the act-boss bar and 262 px
  round the mid-boss bar and is drawn at its own 20 px height. Review sheet proposed for round 26.
- 2026-10-05: M4 part H: Okafor's low-armour line ([armour](../../player/armor/README.md), once per
  attempt at 15 %) queues as an **urgent** line (`RadioQueue.Priority.URGENT`, like the Airstrike's
  call): it is a warning that matters only now, so it neither waits for a gap nor goes stale; the
  interrupted line replays after it, and the timed lines it pushes back may start late (urgent
  lines are outside the one-second rule). `RadioTimelineTest` plays it every 10 s of each Act 1
  level: it opens at once and every timed line still plays; it lists the lines pushed back, by up
  to 18 s when it cuts a long line near its end in a dense run of timed lines (Level 03 from
  t=36.5), mostly 2–13 s, once per attempt and only when the ship is nearly lost.
- 2026-10-05: Concept round 26 closed (user: the round accepted as proposed): the boss bar's plate approved as **final**; the wave banners, the sensor suite's threat arrows, the low-armour flash and the credit-numbers option accepted as built, the banners' **code-drawn look as final** (no rendered banner art), with the arrows' short lead (about 0.3 s before a wave enters) as it is. The objective tracker item is ticked for Act 1 (Levels 06 and 07 use the generic tracker; the later levels' trackers belong to those levels), so every item is ticked or tagged later and the document is `done`.
- 2026-10-06: M5 part A: the escort box is specified after the chosen mock (`hud-r08-a.png`:
  `ESCORT ROOK` over his armour bar): under the special row, his icon and name, the bar with the
  number, the low-armour flash at 30 %, `EJECTED` after he ejects, and nothing drawn in a level
  without an escort (main-agent choices; the mock shows no states). His barks use the radio queue
  as event lines (see [wingmen](../../player/wingmen/README.md#radio-barks)). The document is
  `in-progress` again while M5 builds it.
- 2026-10-06: M5 part A, the escort box as built: the `ESCORT` plate under the special's row,
  a 38 px well with his 16 px icon, `ROOK` and his armour number (or `EJECTED` in red) on its top
  row and his armour bar (8 px, the armour colours) under them; the number and the bar flash red at
  30 % in the player's rhythm. The "not yet available" list moved under the escort's region, so it
  stays put whether an escort flies or not. Capture:
  [rook-ingame-capture-r28-a.png](../../player/wingmen/concept/rook-ingame-capture-r28-a.png).
- 2026-10-06: The radio row names Rook's eject bark as an urgent line (M5 part A, after the round-28 capture): it interrupts like the Airstrike's call, is exempt from the barks' 8 s spacing and never goes stale ([wingmen](../../player/wingmen/README.md#radio-barks)).
- 2026-10-06: Concept round 28 closed (user: accepted): the escort box approved as built after the
  chosen mock (`hud-r08-a`), with the main-agent choices the mock did not show (`EJECTED` in red, the
  number and bar flashing red at 30 % in the player's rhythm, nothing drawn without an escort, the
  "not yet available" list under the escort's region); its code-drawn look is **final** like the
  rest of the panel's, so `art` stays `final`. The radio row's urgent eject bark is accepted. Every
  item is ticked, so the document is `done` again.
- 2026-10-07: M5 part C (stated defaults with the user's decisions D1–D11): Level 09's six nodes
  are six ground-target groups `Node A1` … `Node C2`, so its tracker reads `NODES A1 A2 B1 B2 C1
  C2`, a mark per group as Level 05's letters but two characters wide (about 190 of the well's
  208 px), over its secondary `RAVAGERS n / 8`. No HUD element shows a hold zone (Okafor's line at
  the first one says it). The document is `in-progress` again for the wider marks.
- 2026-10-07: M5 part C, game side: the targets tracker's marks are as wide as the widest one plus
  4 px (Level 09's pairs: 24 px, right-aligned so the last ends at the well's text edge, 6 px after
  `NODES`); a one-letter mark keeps the 16 px step and place (Level 05 unchanged). Line two is the
  escapes line scoped to the bridge waves (`RAVAGERS n / 8`, 6 / 10 by difficulty).
- 2026-10-08: M5 part D (user decision D3 = a): Level 10's escort shows in the two-line tracker,
  `SHUTTLES n / 4` (the four saveable shuttles) over five small armour bars in order, instead of
  the five portrait frames with armour bars its draft asked for (no room in the left panel next to
  the radio and the prompts) or Level 04's pips (three states only for 120 armour). Level 10 has no
  secondary, so the bars take line two. The document stays `in-progress` for the bar drawer.
- 2026-10-08: M5 part D, game side: Level 10's tracker built as decided (D3 = a). Our readings, for
  review in round 32: the bars are 32×8 px, 41 px apart, each filled to its shuttle's armour share
  (a full bar while it lifts off and while Lifeline Three is untouchable); a lost shuttle's bar
  flashes red for 1.5 s and then stays an empty dark trough, Lifeline Three's goes dark at once;
  when the mission fails, line one (the label and the count) flashes red as Level 04's does.
- 2026-10-08: Fixes from the round 32 capture of Level 10: the rear-warning banner (its middle 376 px
  above the bottom edge, 150–180 px below the top) covered Lifeline One's station, so with an air
  escort it sits at 428 px (97–127 px below the top edge), above the shuttle band, with a layout
  test against Level 10's stations, sway and sprite; and a shuttle home after the climb-out no longer
  reads like a lost one: its bar is full in pale mint (`C8FFE0`) under a green glow, whatever its
  armour, where a flying shuttle's is green, white or amber by its armour and a lost one's dark.
- 2026-10-08: [Concept round 32](../../concept-rounds/round-32/README.md) closed (user): Level 10's
  tracker bars, the rear-warning banner above the shuttle band and a home shuttle's pale-mint bar
  accepted as built, and a near-dead shuttle's empty-looking bar in the climb-out kept as it is
  (build choices 21f, g, m and o).
- 2026-10-08: M5 part E (stated defaults with the user's decisions of 2026-10-08): Level 11's
  secondary objective "Convoy afloat" (E8 = a) shows the one-line tracker `CONVOY` with three ship
  pips, decided at the Kraken's death (`DONE` or `FAILED`), the debrief's row `HULLS AFLOAT n / 3`;
  a Torpedo Pod in a level without water (E1 = a: a level flag) reads `NO WATER` in its weapon row
  and still draws its power. Our reading, for review in round 33: the pips turn amber after a ship's
  first slam whatever its hits left (on easy a ship survives two).
- 2026-10-08: Level 11's tracker as built (M5 part E, step E3c; our reading, for review in round
  33): the line flashes green when the afloat secondary is met (`DONE` replaces the pips) and red
  the moment a ship sinks (the label red, the sunk pip flashing then dark); the pips stay until the
  Kraken is down, then `FAILED` replaces them. `NO WATER` is drawn in the label's dim colour.
- 2026-10-09: M5 part F (stated defaults of the part's gap check): Level 12's `SONDES n / 3`
  tracker for the `collect` secondary with its debrief row, `SHIELD DRAIN` with the shield bar's
  flicker while a Lamprey drains, and an event prompt for the shake-off on the first latch; the gust
  telegraph is a weather look in the play field, distinct from the edge warnings.
- 2026-10-11: M5 part F step 6 (pending the user's review in the combined round 34): `SHIELD
  DRAIN` in amber (the warning colour of the HUD's readouts) fits the 120 px label plate, so the
  short `DRAIN` is not used; the bar flickers as after a break while any Lamprey drains. The
  `SONDES n / 3` tracker is the escapes tracker's line (label and count, `DONE` / `FAILED`) with a
  green blink of the well for each sonde collected; the debrief's row `SONDES n / 3` pays the
  secondary's credits when all three were collected. The shake-off's event prompt shows once an
  attempt, from the first latch until no Lamprey is latched or 6 s.
- 2026-10-11: M5 parts G and H (the user is on vacation; the main agent took the gap analyses'
  recommendations, **pending the user's review** in the combined round): Level 13's tracker is
  `RELAY` with an integrity bar, a 50 % tick and the sync countdown, over `INTEGRITY ≥ 50 %`;
  Level 14's is `ROOTS n / 8` (easy `n / 4`); the debrief gets the rows `RELAY n %` and
  `ROOTS n / 8`, Level 14's BOSS TIME row and the Act 2 summary; the intel gets the L3 strip marks.
  The tracker texts are in the table above, defined once.
- 2026-10-11: M5 part F, after the round 34 capture (the user is on vacation; **pending the user's
  review in the combined round**): the shake-off's event prompt was barely seen in the control
  prompts' well (far from the ship, a three-line box for one line, once an attempt, so the real
  shake-off had none). It moves into the play field as a warning: a one-line box in the wave
  banner's look below the ship, on every latch of at least 0.3 s, at most 6 s each time, until the
  attempt's first shake-off. Level 12's gust arrow moves above the side edge warnings (it was
  drawn under them).
- 2026-10-12: M5 part G, step G6 (the user is on vacation; **pending the user's review in the combined
  round**): Level 13's tracker built as specified, with these readings of the spec: the countdown
  shows the synchronisation's 178 s (t=40–218), so it starts at `2:58`, not `2:59`, rounds up (`0:00`
  only at the end), shows from the halt's start and stays at `0:00` after it; the bar's tick sits at
  the secondary's share (50 %) and the bar turns amber below it; the font has no `≥`, so line two
  draws `> 50 %` with a one-pixel bar under the `>`; the debrief's `RELAY n %` rounds the integrity
  left down, so a relay under half never reads `50 %`. The hangar intel's L3 wave strip marks a defend
  level's three halt sections as dim bands under the wave ticks and its whiteout as a pale bar along
  their foot.
- 2026-10-12: M5 part H, step H6 (the user is on vacation; **pending the user's review** in round 36):
  Level 14's `ROOTS n / 8` is the one-line tracker of a groups secondary that has its own `label`: it
  counts the group's units destroyed of all (easy `n / 4`), turning `DONE` or `FAILED` as the other
  counters; a groups secondary without a label (Level 02's docks) keeps its pips. The debrief's row is
  `ROOTS n / 8`, paid when met. The Act 2 summary page lists Levels 08–14 and reads `NONE FOUND` under
  DATA CORES (no Act 2 level hides one).
- 2026-10-12: M5 part H, step H7b (after the H7 capture of Level 14; **pending the user's review**):
  a `boss-phase` cue queues as a `PHASE` line, stale after 20 s instead of 6 s
  (`RadioQueue.PHASE_STALE_SECONDS`), so Rook's phase-3 reply behind Okafor's line plays; the phase
  lines still waiting at the boss's death are dropped. A secondary objective's line that waits out the
  timed lines before a boss is dropped when the boss's warning starts, or cut if it opened less than
  1.5 s before (`RadioQueue.drop`, `LevelScreen`): Level 14's root-grid line otherwise played over the
  Spire's arrival and made Rook's warning bark go stale. Tests: `RadioQueueTest`,
  `RadioTimelineTest.theSpiresPhaseLinesPlayOneAfterTheOther`.
- 2026-10-12: M5 part H, step H9 (pending the user's review): a closing line (`RadioQueue.Priority.CLOSING`)
  queued while the radio is free goes ahead at once, and the timed lines that fall due while it plays wait
  for it (they may then start more than 1 s late); queued while a line plays or waits, it waits for a gap
  as before. Level 14's root-grid line (about 10 s at the default text speed) found no gap before the
  Choir's t=96 line 2.5 s later and was dropped at the Spire's warning; now it plays as the roots die
  (`RadioQueueTest`, `RadioTimelineTest`).
- 2026-10-12: `design` back to `review` (from `approved`) for the radio rule changes of steps H7b and
  H9 above, which apply to every level; listed for the user as C10 in the combined
  [concept round 34](../../concept-rounds/round-34/README.md#changes-to-approved-specs).
- 2026-10-12 (user, [round 34](../../concept-rounds/round-34/README.md)): C10 accepted (the radio rule of
  H7b and H9 for all levels: a `boss-phase` line stale after 20 s and dropped at the boss's death, a
  secondary line waiting at a boss warning dropped, a closing line on a free radio ahead of the next
  timed line), and the Level 12–14 trackers and words; `design` back to `approved`.
- 2026-10-13 (user, round 36, Q.25e): the cooldown ring and the dash pip of the Act 3 equipment added to the checklist as M7 part A items.
- 2026-10-13: M7 part A, step A6 (our reading, **pending the user's review** in round 37): the 8 px
  HUD font cannot fit `SHIELD OVERCHARGE` (nor `OVERCHARGE` beside `READY` after the word
  `SPECIAL`, nor `ORBITAL LANCE` beside its charges), so the special row goes **compact** for a
  cooldown special or a name over 10 characters: the icon (in its ring) where the word `SPECIAL`
  stands, the name after it; the Act 1–2 specials and the EMP Burst keep the row as it was. The
  ring is 28 dots: dim cyan as the cooldown runs, cyan when ready, white for 0.2 s as it gets
  there, the overcharge's white arc over it. The **dash pip** is a 16×6 cell right of the shield's
  label plate, filling dim cyan over the cooldown, cyan when ready, white for 0.2 s as it gets
  there (step A8: both count game time; they counted 12 and 10 rendered frames, round 37 R.4 d). A drone bay's **hits** are a 2 px strip of five pips under its row's level pips (the
  level pips stay), cyan, refilling dim while its bay rebuilds it.
- 2026-10-13 (user, round 37): both changes to the approved specs accepted: the light drone's hits as a
  2 px strip under its row with the level pips kept (C1) and the compact special row for a cooldown
  special or a name over 10 characters (C2); step A6's HUD choices kept (B.5 a, b: the 28-dot cooldown
  ring with the overcharge's white arc, the dash pip right of the shield's label plate); R.4 (d) fixed:
  the READY flash and the dash pip's white state last 0.2 s of game time (step A8).
- 2026-10-19: The boss warning banner opens at the boss's lead warning when it has one, and closes early rather than hide a boss that fights under it.
- 2026-10-20: round 42 closed: the barks after the win (`Barks.levelComplete()`, R.h) accepted by the user.
