# Level 12 – Storm Front — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-10-01: Promoted from the Act 2 roster to a draft level document.
- 2026-10-01: Heavy atmosphere only as a 15 s peak at the storm wall; sea and rain are the two
  strong elements everywhere (motion budget, round 09 feedback).
- 2026-10-01: Open question resolved: L12 keeps "Firestorm" (Act 2 B); no separate storm variant (user decision).
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../../reviews/acts-1-2/README.md).
- 2026-10-09: M5 part F (user decisions of 2026-10-09, all as recommended): **F1 = a** the cloud
  banks hide only `low-air` bodies (draw order, a level flag), air waves "out of the banks" enter
  at the edge with a flash timed to their entry; rejected: b (air units hidden in a bank's
  footprint: a coverage mask, and it breaks the layer rule) and c (banks hide nothing: loses the
  level's idea). **F2 = a** the canister is always hittable, drawn under its bank with a glint,
  fully seen in two authored flashes; rejected: b (Level 06's hit-only-while-lit rule: one or two
  0.5 s windows for 3 hits, and the flash schedule in the simulation) and c (a plain visible
  canister). **F3 = a** what hits a latched Lamprey goes by weapon
  ([Lamprey](../../../enemies/air/lamprey/README.md#behaviour)). **F4 = a** one latch per Lamprey.
  **F5 = a** the base stem at −6 dB in the eye; rejected: b (a third "eye" stem: a three-stream
  mixer and a stem render) and c (the theme fading out in the eye). **F6 = a** a rain-only bed cut
  from the same CC0 source and thunder one-shots after the flashes; rejected: b (thunder on top of
  the bed's own: muddy) and c (flashes timed to the bed's thunder: the schedule coupled to an audio
  loop). **F7 = b** the sondes on the play plane, drifting down at 60 px/s, not pulled by the
  magnet; rejected: a (on the sea at 140 px/s: ≈ 5 s on screen) and c (the magnet pulling them:
  the secondary nearly free). **F8 = a** art straight to production with a/b only for the eye and
  the latch, sounds a/b for thunder, gust, latch and drain; rejected: b (also the whole storm
  backdrop a/b) and c (no a/b at all: the eye unseen). **F9 = a** a Lamprey swarm is a swarm of
  chasers; rejected: b (real flocking until near the ship) and c (swarms as tight streams).
- 2026-10-09: M5 part F stated defaults (accepted with the decisions): data in Act 1 terms with a
  `bounty_scale` (the canister 130, the secondary 75); the waves densified to at least 50 a minute
  with the 3 s pacing rule, popcorn from L12's own roster, the t=32 introduction alone; the act HP
  factor for the returning `medium` units (Spore Bomber, Mantis, Wraith; the `tiny` and `small`
  ones keep their HP by the factor's rule); the wind in the data (gusts by `t` from the left,
  2.5 s, 40 px/s, easy 25, hard 55, telegraph 1.0 s, easy 1.5 s; the wall's 20 px/s drift, none on
  easy; hard's extra gusts at t=34 and 72; none in the eye), added to positions with 0.25 s ramps,
  moving the ship, Rook, Lampreys and Whirl Seeds only; lightning times in the data with a
  simulation event for the thunder; the Lamprey's rules (its spec's *Decisions*); shield cells
  ×1.5 as a level drop factor; the eye's armour patch as a placed pickup; the sondes' tracker,
  chime and event, Rook not collecting them; the radio retimed with the events `first-latch` and
  `secondary-objective`, Rook's t=102 dropped, his t=44 kept, his lines `requires: escort`; voiced
  with the cast speakers, Varga's four intel lines, two briefing pages, Rook's teaser; the banks
  hiding `low-air` bodies in L12 only; the storm bed with the ocean in the eye; L12's own atlas;
  the plan's L12 visit refitting Rook's Autocannon with `BalanceTest`'s Lamprey checks; hard's
  Mantis pincer in the eye, the canister's 5 hits and 2 latch slots; the eye calm (no rain, gusts,
  `clear`) with the cloud wall turning at ≈ 3°/s and the Needler circle clockwise. The draft's
  contradictions fixed: the budget (2,105 on the old curve → budget(12) ≈ 1,473), its Act-2-scaled
  amounts, the density (≈ 33 a minute), the radio clash in the eye, the "air units hidden under
  low-air banks", and the music's "base stem drops out in the eye".
- 2026-10-09: Settled in step 1 (for review in round 34): the Choir moves from t=164 to t=164.5,
  between Varga's two eye lines, and Okafor's "Say again" answers it at t=170; the `water: true`
  flag is set (open sea throughout); the sondes' collect radius is the ship's centre within 36 px.
- 2026-10-09: The storm turns **clockwise** (the eye's cloud wall, the Needler circle), as the
  approved draft says (user): unnatural for a northern-hemisphere storm on purpose, a tell that the
  storm is being steered.
- 2026-10-11: M5 part F step 3 (simulation built; accepted in round 34 (2026-10-12):
  a drift ramps up and down inside its span; the gust telegraph runs from its time ahead until the
  gust has ramped up (`Wind.telegraph`); a sonde enters and leaves with its body (24 px) just outside
  the edge, is collected only while the ship flies and counts as lost once wholly below the bottom
  edge (the secondary's failure raises the usual objective-failed event); a placed pickup enters with
  its centre on the top edge; the drifting canister is not hit by bombs or shells bursting on the
  ground nor by torpedoes, and is gone once it leaves the screen; sondes are not supported in a level
  with a boss checkpoint (none in Level 12).
- 2026-10-11: M5 part F step 4 (data, loader and autopilot; accepted in round 34 (2026-10-12): **234 units at medium (70 a minute)**, not the estimated 175–185: with the plan's
  two Micro-missile Pods the autopilot clears the waves fast, and leaner popcorn left empty screens
  over 3 s; the eye keeps four small popcorn waves (t=150, 155, 162.5, 169.5). **`bounty_scale`
  0.75** (typical haul 1,476, +0.2 %). **Directions front 70 · sides 24 · rear 6 %**: no Skitter
  stream from the rear (the simulation flies a rear `stream` only for chasers), so the rear is the
  Wraiths' and the Lampreys'; the popcorn added from the sides instead. The t=32 introduction: the
  popcorn before it at t=26.5 and nothing else until t=38 (6 s). Sondes at x 400, 70 and 390 (off
  the lanes of the waves round them); the canister enters at (420, top) at t=99 and drifts
  (−24, −70) px/s, ≈ 8 s on the screen, lit by the flashes at 99.5 and 103.5. The whirl clusters on
  the wind are released at x = −16 (just outside the left edge). The armour patch is placed at
  x 300. Rain keys: none until t=10, 0.4 in sections 2–3, 1 in the wall's peak, 0 in the eye, 0.4 at
  the breakout thinning to 0 by the end. The ambience key `earth-storm` waits for step 6 (the game has no storm bed yet). The
  plan's rear check of the returning Wraith (18 HP, 0.62 of its window against the limit 0.6) is
  reported in `BalanceTest` as pending the user's decision.
- 2026-10-11: M5 part F step 4, the radio (accepted in round 34 (2026-10-12)): the eye's lines retimed to
  149.5 (Rook), 157 (Varga), 169.5 (the Choir), 176 (Okafor) and 182.5 (Varga) for the typing time
  the step-1 estimate left out; Varga's L2 and L3 intel lines shortened to "New: an eel that drains your
  shield. Shake it off." and "Homing sees through the rain. Spread for the seeds.", and the shake-off prompt's keys to `LEFT-RIGHT` to fit the intel panel and
  the prompt line (`IntelPanelLayoutTest`, `MissionLayoutTest`).
- 2026-10-11: M5 part F step 6, the game side (accepted in round 34 (2026-10-12):
  the looks follow tools/art/backdrop_l12.py's review composite. **Lightning**: the bolt (at x 84
  for even flashes, 396 for odd) and a flat sky tint for 0.1 s, then the lit cloud over the bolt's
  side and the banks tinted toward (190, 205, 235) at 0.45 and seen through by 0.6, fading over the
  0.5 s reveal; timed by the data on the script clock (the simulation's `LIGHTNING` event plays the
  thunder); a flash within 1/3 s of the last one shown is dropped (none in the data); the flash
  reduction scales the bolt, the sky tint, the lit cloud and the banks' tint to 0.35 but **not the
  see-through**, so the reveal still shows what is under the banks. **Occlusion** is the existing
  draw order (`low-air` units and the canister under the low-air layer); each `low-air` unit gets
  the `weather-glow` pulse (4 fps) and the canister its own beacon glow over the banks.
  **Rain**: 220 streaks at density 1 (the data's keys), falling 480 px/s with a 1:4 slant on the
  steps' real clock, additive at 40 % over the play plane and under the bullets. **Gust
  telegraph**: the arrow 36 px in from the upwind edge (mirrored for a gust from the right) and four
  wind streaks blowing downwind at 216 px/s, while `Wind.telegraph` runs; the **upwind cue** is the
  gust sound panned 0.3 to the upwind side. **The eye**: the wall ring turned clockwise at 3°/s from
  t=150, linear-filtered for its draw, the shaft additive over the calm sea under the ground objects,
  both faded over the backdrop's 5 s ramp centred on 150 and 175 (the section with the clear
  atmosphere); the breakout's light at the top edge fades in over 190–200 (15 s into the last
  section) at 40 % and is held. **Latched Lampreys** are drawn over the ship at the ship's
  position plus their spot (no shadow), gulping at 10 fps with the shield ring's pulse (0.55 /
  0.3 / 0.12 / 0); stunned they tumble through their 8 frames over the stun. **The event prompt**
  (`SHAKE OFF` · `LEFT-RIGHT`) shows from the attempt's first latch until no Lamprey is latched or
  its 6 s are over, once an attempt. **Sounds** (round 34 a): thunder 1–3 in turn, 0.3–1.5 s after
  each flash (a golden-ratio spread over the flash index, so it replays alike); the gust as its
  telegraph starts; the latch's bite; the drain loop while a Lamprey drains in flight; the
  shake-off; the sonde's chime. The theme's `section_db` ramps over 1 s.
- 2026-10-11: M5 part F step 6, the voices (accepted in round 34 (2026-10-12): the 18
  voiced lines (Okafor 5, Varga 8, Rook 5; the Choir's line plays its stage sound) rendered and
  measured; six takes pinned (see [voice](../../../audio/voice/README.md)); `RadioTimelineTest` passes
  with the real lengths, with and without Rook, so no radio time was changed.
- 2026-10-11: M5 part F, fixes after the round 34 capture (the user is on vacation: the capture's
  defaults, **accepted in round 34 (2026-10-12)****). **Shake-off prompt**: it showed in
  the left panel's three-line well under the radio, far from the ship, for 0.6 s and once an
  attempt, so the real shake-off had none; it now stands in the play field as a warning (the
  [HUD](../../../ui/hud/README.md#in-the-play-field) rule), a one-line box below the ship, on every
  latch of at least 0.3 s, at most 6 s each time, until the attempt's first shake-off. **Rain**: it
  was very faint (about 1 % streak pixels, 1.5 % in the wall): 480 streaks at density 1 instead of
  220, half as long again at density 1 (the wall's sheets; ×1.2 at the steady 0.4), and drawn so
  the brightest streak pixel adds 40 % of white (the high-air cap read on what is drawn, not on the
  dim image: 0.64 of the image instead of 0.4); still additive under the bullets, and still the
  only strong motion beside the sea. **Gust telegraph**: it was weak and under the side edge
  warnings: the arrow at twice its size, drawn twice additive, its centre 72 px in from the upwind
  edge and 60 px below the top edge (clear of the side warnings' bars and glow, 99–441 px down, and
  the wave banner); seven wind streaks instead of four, twice as thick, at the high-air cap.
  **Reveal**: a flash lit only a cloud puff by the bolt, the bodies under the stacked bank layers
  (tiles, banks, tall banks, each at 1 − 0.6·reveal) still hidden: during the reveal the `low-air`
  units and the canister are drawn again over the banks at 0.6·reveal, wherever they are on the
  screen, and the reveal holds at full for its first 0.2 s before fading over 0.3 s. **Sounds**:
  the latch, shake-off and gust at the warnings' level (the gust from −4 dB, the shake-off from −2
  dB; the latch was there already) and a latch or shake-off ducks the theme by 6 dB for 1 s.
  Left as review items: the eye's 2 s dissolve, the plan fit not latched before t≈112, the armour
  use.
- 2026-10-12: M5 part G, step G8 (**accepted in round 34 (2026-10-12)****): the shake-off
  cue measured about 6 dB under the busy mix and was marginal at one of the capture's two shake-offs,
  though it already plays at the warnings' level (the game's ceiling). Its file is now 3.5 dB louder
  (round 34's placeholder regenerated at −13.5 dB instead of −17; [sfx](../../../audio/sfx/README.md));
  the latch, the gust and the theme's 6 dB duck stay.
- 2026-10-12 user, round 34: the thunder **b** (three close strikes, a crack then the roll) and the gust
  **b** (a recorded gust) instead of a; the Lamprey's latch and drain sounds stay a; the latch look is
  **b**, "siphon" ([lamprey](../../../enemies/air/lamprey/README.md#decisions)). Step K1 wired them (the
  b files at the a's levels, within 1 dB; [sfx](../../../audio/sfx/README.md#decisions)).
- 2026-10-12 (user, round 34): concept round 34 closed. The eye **a** "billowing wall, cool shaft"
  (b "banded spiral wall" rejected, its files in `concept/rejected/`); the storm backdrop, the props,
  the briefing images and the Lamprey's art and intel portrait approved as **final**, so `art: final`;
  the captures (12.3a–c, the mine drop as raised since), the texts (page 1 shortened, C5), the numbers,
  F1–F9 and every build choice accepted but 12.7b (b): **no**, a latched Lamprey keeps draining
  through the ship's mercy time ([Lamprey](../../../enemies/air/lamprey/README.md#decisions)); the
  sounds and the latch look as in the line above; the 18 voiced lines heard; P4 (the Wraith rear check
  at 0.62) accepted. The document leaves `review` for `approved`. Still open: the Spore Bomber
  revealed by a flash (P5, being captured) and the user's playthrough of Act 2 (P9).
- 2026-10-13 (user, round 36 L.1/L.5): **L.1**: the lightning reveal never showed a Spore Bomber
  under a bank at a flash in the built level, so a Spore Bomber pair (`line abreast`, front) is
  **added** at t=101.5 as in the K6 capture's scratch variant, the t=146 pair kept: its right unit
  flies into the tall bank on the right and is seen through it in the t=103.5 flash. 236 units at
  medium (was 234); the typical haul 1,512 against the budget 1,473 (+2.6 %, was +0.2 %), within
  ±5 %, so `bounty_scale` stays 0.75; density, pacing and the balance checks pass. **L.5**: the
  Lamprey's latch spots in its data move 1 px to look b's pivots
  ([Lamprey](../../../enemies/air/lamprey/README.md#decisions)).
- 2026-10-18: The user's playtest: "The last transmission about the storm clearing is too late. It was
  played during the post mission screen. Probably extend the mission until it is fully played." With
  Rook flying and the sondes met late in the eye, the secondary's line waited for a gap until the level
  end (205), so Okafor's level-end line opened at 218.1 and the outro's 15 s cap handed over to the
  debrief while he spoke. **Breakout now ends at 218** (its light peak too): the secondary plays after
  Rook's t=200 line and the level-end line is spoken by ≈ 229, inside the outro. The game now also cuts
  a voice still speaking when the debrief takes over, and `RadioTimelineTest` checks every level's
  closing voices end inside the outro. The extension leaves a quiet exit of 13–15 s after the last wave
  (`PacingTest`: the level's second longer pause on easy and medium, inside the rule's two).
- 2026-10-19: The user's Act 2 playtest, P.e "yes, all" (Levels 11–14's waves too, as step CA did
  Levels 08–09): the waves **rewritten L10-style**, a wave about every 2.5 s from t=6.5 to t=214 (so
  the breakout's quiet 14 s tail is gone), 462 units at medium in 94 waves (236 in 57), Needlers the
  bulk (39 %, Skitters 33 %), Stingers new to the level (V-wings and columns of 4), more Spore Bomber
  lines (3 a line), Mantis pincers at t=55.5 and 180.5 and Wraith ambushes at t=75.5, 138.5 and 206.5
  added; every set piece, hold, radio time and story beat kept (the t=32 introduction alone, the
  Lampreys, the seeds on the wind, the canister's beat, the eye's circle). The Needler (12 HP) and the
  Stinger (17) take the capped act HP factor here (`small_act_hp.levels` + 12, 13): without it the
  plan's fit kills them before their first shot (1.7 enemies on the screen, 4 shots a minute). Medium
  2.7 on the screen and 17.7 shots a minute (was 0.7 and 1.1), one pause over 2 s (was 22); hard 2.7
  and 19.7 with its groups one or two smaller; the directions now front 85 · sides 10 · rear 4 % (was
  70 · 24 · 6: side Skitter streams gave way to Stingers); `bounty_scale` 0.75 → 0.30 (typical haul 1,486, +0.9 %). The
  test edits (the level's totals, `PacingTest.PENDING_ACT_TWO_RULE`) are the main agent's. Accepted in round 41 (2026-10-19): the Stingers in the storm, the Needler HP, the number of units.
