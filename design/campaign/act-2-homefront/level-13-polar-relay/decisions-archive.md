# Level 13 – Polar Relay — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-10-01: Promoted from the Act 2 roster to a draft level document.
- 2026-10-01: Primary `defend`: the relay destroyed means mission failed; failing the secondary
  objective only loses its bonus (user decision).
- 2026-10-01: The relay's spec moved to [allies](../../../allies/README.md#nansen-relay). Open question resolved: the per-unit relay-targeting overrides are replaced by the general target-the-objective hook in the enemies README, configured here as a small table.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../../reviews/acts-1-2/README.md).
- 2026-10-11: M5 part G, step G1a: reworked for the M4/M5 rules, so `design: review`. **The user is on
  vacation: the main agent took every recommendation of the gap analysis; all are accepted in round 34 (2026-10-12) in the one combined concept round at the end of M5.
- 2026-10-11: **G1 = a** (recommended; accepted in round 34 (2026-10-12)) the hook modes made exact on the
  relay-aimed rule: `alternate` (every second attack, the relay closer than the ship, a burst is one
  attack), `in-arc` (the fan centred on the relay within ±45° of the facing), `always` (the Stinger's
  dive and fan at the relay, a single 10-point hit crossing the footprint, then it flies on; the
  mortar's marker, direct hit and lob on the relay). Rejected: b (everything hooked that crosses the
  relay hurts it: far more damage, no counterplay) and c (only aimed shots follow the hook: the divers'
  idea lost).
- 2026-10-11: **G2 = a** (recommended; accepted in round 34 (2026-10-12)) the waves kept and the integrity tuned
  per difficulty with the defend autopilot (medium never loses, ≥ 50 % in about half the seeds; easy
  nearly always; hard may lose the secondary, never the relay); measured numbers recorded here later.
  Rejected: b (change the approved wave table) and c (accept what is measured).
- 2026-10-11: **G3 = a** (recommended; accepted in round 34 (2026-10-12)) halted time counts as scroll time;
  ≈ 196 units, the extra 114 unhooked popcorn from all edges; no rule change. Rejected: b (a lower
  minimum for `defend` levels) and c (more hooked Skimmers).
- 2026-10-11: **G4 = a** (recommended; accepted in round 34 (2026-10-12)) Skimmers are lane walkers on
  ground-space lanes with the weave; the leads and the relay's floe are fixed in the halt, loose ice
  drifts. Rejected: b (flight paths: boats would cross the ice) and c (drifting leads).
- 2026-10-11: **G5 = a** (recommended; accepted in round 34 (2026-10-12)) `water: true` for the whole field in
  the simulation, the water splash by unit (a level flag). Rejected: b (a lead mask) and c
  (`water: false`).
- 2026-10-11: **G6 = a** (recommended; accepted in round 34 (2026-10-12)) the supply drone is presentation only
  and releases an ordinary patch at the arc's top. Rejected: b (a carried patch) and c (no drone).
- 2026-10-11: **G7 = a** (recommended; accepted in round 34 (2026-10-12)) the whiteout banks hide ground units
  at the edges, air stays visible. Rejected: b (a veil dimming air units) and c (snow only).
- 2026-10-11: **G8 = a** (recommended; accepted in round 34 (2026-10-12)) the jingle plays once at t=218, the
  debrief without it. Rejected: b (twice) and c (none).
- 2026-10-11: **G9 = a** (recommended; accepted in round 34 (2026-10-12)) round 35: a/b for the relay, the
  whiteout peak, three sounds and Nansen; production straight away for the backdrop and the Skimmer;
  one version for the drone and the hut.
- 2026-10-11: Stated defaults of the gap analysis (all accepted in round 34 (2026-10-12)): the halt's 2 s eases;
  hit box 80×80; Act 1 terms with a `bounty_scale` (estimate 0.67), the hut 110 and the secondary 95;
  no act HP factor for the Skimmer; Scuttler lanes ending 100–140 px short of the relay; the mortar
  floe from the left at t=66; the circle centred above the relay; the drones' patches at (240, 380) at
  t≈162 and t≈202; the radio retimed to the 1 s rule with Rook's t=82 and t=122 lines dropped; Varga's
  four intel lines; the HUD tracker; the fog from t=140; the threat profile's defend exception; Rook
  keeps the Autocannon; L13's units in L13's own atlas.
- 2026-10-11: M5 part G, step G3 (the simulation; **accepted in round 34 (2026-10-12)**, the user is on vacation):
  the timed halt eases by smoothstep on the level clock, the ramp read from the halted section (default
  2 s: down over the last 2 s before it, up over the first 2 s after it); the approach then scrolls
  ≈ 5,460 px to the halt and the exit ≈ 1,960 px. The defend has a start and a duration (t=40 + 178 s:
  the synchronisation completes at t=218, when the integrity secondary is judged; the scroll resumes at
  t=220); the relay stands at its place at the start and is safe once the synchronisation is over
  (nothing aims at it or hurts it); `--invulnerable` keeps a lost relay from failing the level, as a lost
  escort. Under `always` the Stinger's fan keeps its stat block's timing (0.6 s into the dive), so it fires
  from below the relay back up at it, which the player can body-block (rejected: firing as it crosses
  the relay, unblockable); its dive's 10 is dealt when its hit box crosses the relay's 80×80 hit box. A
  relay-aimed lob also hurts a ship inside its impact circle, as any lob. `in-arc` uses the walker's
  `front_arc` (the Scuttler's 45°); `alternate` compares the distances centre to centre and decides at a
  burst's first shot. A column's pair flies 50 px apart. The sim enforces only the structural rules (no
  timed halt with holds or an arena, no defend beside an escort); "a timed halt only with a defend" and
  "nothing enters at the top edge in the halt" are the validator's (G4, `LevelScript.timedHaltAt`).
- 2026-10-11: The draft's errors corrected: the relay at play-field x = 240 (not "480 in screen
  coordinates"), entering at t≈37 (not 34), the edge warning 3 s (not 1.5 s), the budget 1,577 (not
  the perfect-run 2,252), the hut's 180 and the secondary's 150 are the act-scaled values of 110 and 95.
- 2026-10-12: M5 part G, step G4 (the content step; **accepted in round 34 (2026-10-12)**, the user is on
  vacation): the level's [data.yaml](data.yaml) and the marked tables rendered from it. The relay's
  integrity tuned per G2 = a with the guarding autopilot over 8 seeds: **240 medium / 340 easy / 520 hard**
  (the draft's 600 / 900 / 450; see *Nansen Relay* for the measurements and why hard's is higher than
  medium's). The popcorn reworked for the pacing rule with the plan's fit: streams of 4 at 2–2.5 s every
  3–5 s, a stream at t=24 after the introduction, the introduction's three Skimmers on easy too; **208
  units** (54 a minute) instead of the plan's 196; the rear popcorn as Skitter line abreasts and the t=140
  rear stream as a snake on a path (only chasers stream from the rear), the t=200 rear Needlers as a line
  abreast (a V-wing enters only from the front). The typical haul 1,606 at `bounty_scale` 0.67 (+1.8 %).
  Recommended traits `spread` and `rear` (`side` in the notes: three do not fit the intel panel). The
  secret's radio line is Varga's (new). Nansen's lines use the generic CDF portrait. The briefing pages
  have no image until step G5 renders them; the backdrop is a placeholder (Level 12's sea, `images:
  level-12`) until G5's arctic one. Rejected: three recommended traits (the panel), the draft's popcorn
  timing (the plan fit clears it with up to 7.5 s empty).
- 2026-10-12: M5 part G, step G6 (the game step; **accepted in round 34 (2026-10-12)**, the user is on
  vacation): the relay drawn in variant a (relay.py; state by integrity: smoke below 50 %, fire below
  25 %, dark at 0 with a smoke column of puffs every 0.15 s rising 42 px/s and drifting 16 px/s; a hit
  flashes it white). Grid online at the synchronisation's end, **t=218** (the README's "t=220" read as
  the sync's completion, so the lights, their sound and Nansen's line coincide): the lights' sequence
  over 2 s, then their steady loop; the jingle after the theme's 1 s fade (t=219). The supply drone's
  arc: 4 s from the helipad (the relay's pivot) to the drop's point, swinging 110 px out to one side (the
  first drone right, the second left) and arriving at the arc's top, then 4 s back with its clamps open
  on the other side; it flies only while the relay stands. The Skimmer: its plain body per heading in
  the sub pass, its wake per heading on the surface, its tail beat at 10 fps (the organic rate; the art
  suggested 8), its death a small burst over the water splash and the flip-and-sink at the nearest of
  8 headings (6 steps a frame). On the ice the water splash goes by unit: a burst within 8 px of a
  Skimmer's hit box is the water splash, everything else the ground burst. The arctic setting snows on
  high-air by the atmosphere (none in light, 30 % in medium, full in the heavy whiteout, ramping with
  the backdrop), on the real clock, at most 40 % per flake. Easy's whiteout (6 s, 179–185) and the
  wind's +3 dB come from two new optional keys of a section peak, `easy` (s) and `ambience_db`; the
  data line `peak: {atmosphere: heavy, from: 176, to: 188, easy: 6, ambience_db: 3}` is the main agent's
  merge (the data was being edited by step G5).
- 2026-10-12: M5 part G, step G5 (the backdrop and the briefing images, straight to production, G9 = a;
  all accepted in round 34 (2026-10-12): **the ground is one unique strip** (four `ground` pieces
  of 480×1,536, the approach and the halt's frame in one render) instead of a repeating ice tile: in a
  timed halt every later section's tile set begins at the halt's top edge (ground 6,000), so the halt
  shows section 1's ground, and a tile would have repeated the halt's leads through the approach (across
  the hut's floe and the introduction's lead). The leads are cut along **every** Skimmer lane (the
  simulation's Catmull-Rom curves, 2 × 44 px: the 40 px weave and most of the hull), so the halt's frame
  is the relay's floe in a ring of leads with floes beyond them; the Scuttlers' paths run in the west and
  east leads' margins and cross the leads (the data's), so the leads are choked with brash rubble where
  they cross. The water is the `deep` tile drifting 3 px/s left on the real clock (the leads' water moves
  in the halt); the loose ice is one `loose-ice` piece with 8 headings (a different floe per direction)
  on straight 6 px/s paths along the leads' outer margins (outside the lanes is ice: they drift in the
  leads), and the mortars' floe is its east-heading frame on a 10 px/s path in step with the simulation's
  side entry; that keeps the motion budget's two elements (the water's drift and the loose ice). The fog
  (light ~10 %, medium ~20 % at the edges) and the whiteout (look a's banks, ~30 % opaque at the edges, the
  centre clear) are the atmosphere's static banks, cross-faded over the 4 s ramp; their real-clock roll,
  the snow and easy's 6 s peak are the game's (G6). The exit has its own `open-floes`, open water where it
  begins. Rejected: an opaque halt piece over a repeating tile (its water could not drift and slides
  against the deep layer while it scrolls in).
- 2026-10-12: M5 part G, step G7, the capture (round 35's [level-13-capture-final-r35-a](concept/level-13-capture-final-r35-a.png);
  the user is on vacation, so **accepted in round 34 (2026-10-12)**). Fixed in the game: the Skimmers were drawn
  as flyers (no wake, a drop shadow, over the whiteout banks): a lane walker is now on the ground units'
  depth; the relay's damage smoke below 50 % (three static 16 px puffs, unreadable on the ice) now rises
  and drifts off each smoke point at 2× as tools/art/relay.py's review loop; the drone launch +2 dB.
  Seen and left for review: a Skimmer of the t = 20 introduction crossing a floe instead of its lead; the
  mortars' wreck stays at the left edge while their floe drifts on; grid online's chord lands on the
  jingle's first second; the debrief comes ≈ 16 s after the level's end (t 235) behind two radio lines.
- 2026-10-12: M5 part G, step G5b, the backdrop's fixes after the capture (**accepted in round 34 (2026-10-12)**).
  The t = 20 Skimmer crossing a floe: the sim lays a lane on the ground at each unit's *entry*
  (`Formations.lanes`: a convoy's units 1.5 s apart), the generator at the wave's time, so the second and
  third units' lanes lay 210 and 420 px further up the ice than their leads (32 % and 38 % of their
  points on ice); `--check` missed it because it only counted lane points on screen at the wave's time
  (the introduction's lane starts above the top edge). Now every unit's lead is cut at its entry and
  `--check` follows each unit's run (centre and weave, per wave). The lead's rough edge no longer narrows
  below 42 px, so the weaving hull stays in it (97–100 % on water per wave; the ice a little thinner: the
  halt's frame 29 % ice instead of 32 %). The fog banks read as grey smudges (r03's bank shaded their thin
  edges darker than the ice, two hard alpha steps): now pale, soft-edged fog lighter than the ice it covers
  (`--check`: +16 / +25 luminance over the ice, wisps / banks). The mortars' floe path matches the sim's
  side entry (centre x −22 at t = 66, 10 px/s); their wreck staying at the left edge is game-side.
- 2026-10-12: M5 part G, step G5c, the Level 13 lead margin (**accepted in round 34 (2026-10-12)**). The
  supplementary clip ([level-13-capture-fixes-r35-a](concept/level-13-capture-fixes-r35-a.png)) still
  showed the t=20 Skimmers' hulls on floe edges (t 24.5, 27.5): `--check` tested only the weaving hull's
  centre, and a lead 44 px either side (rough edge >= 42 px) leaves the 40 px weave 2 px for a hull
  14.5 px wide either side of its centre. `--check` now tests the hull's full width (the weave's centre
  +- 15 px across the lane, all three on water) and the leads are 58 px either side, the rough edge never
  narrower than 57 px, the margins' brash only in the bays beyond the hulls' reach. Every hull sample is
  on water except 131 of 3,780 on the rubble where a Scuttler's path crosses a lead (G4's choke, kept).
  The ice is thinner: the approach 38 % (was about 41 %), the halt's frame 20 % (was 29 %): the halt's
  lanes leave only 24 % of the field beyond the hulls' reach, so the halt is the relay's floe in brash-
  strewn water with floes at the edges; the relay's and the hut's floes are unchanged (100 % ice).
- 2026-10-12: M5 part G, step G8, the game's fixes after the capture (**accepted in round 34 (2026-10-12)**).
  **Grid online and the jingle**: the chord (2.6 s from t=218) sat on the jingle's first second, both
  near −17 dBFS; the music block's `jingle` is now `{t: 218, fade: 2.6}`, so the theme fades with the
  chord and the jingle starts as it ends (t=220.6). **The mortars' wreck**: a ground unit that drifts on
  top of the scroll (the mortars' floe, 10 px/s) now hands its drift to the game with its death (a new
  simulation event, `WRECK_DRIFT`, presentation only), and its death burst and remains drift on with the
  floe instead of staying at the left edge where it was shot.
- 2026-10-12 user, round 34: grid online **b** (a spin-up into a contact clunk; 2.6 s like a, so the
  jingle's 2.6 s fade stays), the other relay sounds a; Nansen's voice **a**, Patrick Eaton: step K1 cast
  him and rendered his five lines ([voice](../../../audio/voice/README.md#decisions); the portrait stays
  the generic CDF one, `RadioTimelineTest` passes unchanged).
- 2026-10-12 (user, round 34): concept round 34 closed. The whiteout **b** "blizzard curtain"
  (a "rolling banks" rejected, its files in `concept/rejected/`; b built into `backdrop_l13.py` in step
  K3) and the Nansen Relay **b** "grid node" ([allies](../../../allies/README.md#decisions)); the
  arctic backdrop, the drone and the research hut, the briefing images and the Skimmer's art and intel
  portrait approved as **final**, so `art: final`; the captures (13.3a–c), the texts (C5), the numbers
  (C11: integrity 240 / 340 / 520, hard above medium), C4 (the grid-online chord and the jingle at
  t=220.6), G1–G9 and every build choice accepted; the sounds and Nansen as in the line above; the 15
  voiced lines heard. The document leaves `review` for `approved`. Still open: the relay-lost mission
  failed screen (P5, being captured) and the user's playthrough of Act 2 (P9).
- 2026-10-12: Step K10: the relay-lost `mission-failed` cue gets `expression: grim` like every other
  level's failure line (the failed screen showed Okafor's neutral portrait); the line re-rendered with
  the grim settings (`tools/art/voice.py`, `okafor/7f735b28f819.ogg`, Whisper and pitch checked; the
  old `49386929d4bb.ogg` deleted, round 34's link updated).
- 2026-10-19: The user's playtest (2026-10-18: "The icebergs have very strange drift patterns. They can't all
  drift in different directions like that. … I also noticed they sometimes drift through the platform
  partially"; the user's choice: all drift east at 10 px/s, re-pathed off the relay's floe, the mortars'
  floe up 30 px): every floe now drifts **east at 10 px/s** along the halt's north and south margins,
  and `backdrop_l13.py --check` fails if a floe's opaque pixels touch the relay's floe (before: up to
  850 px at t≈86, 260 at t≈188; now none). The mortars and their floe moved up **40 px** (y up 480/440,
  the floe's centre 80 px below the top), not 30: at 30 the floe still grazed the relay floe's north
  tongue by 88 px. Each floe keeps its own look without a game change: the old `loose-ice` (8 headings,
  a look per direction) became eight pieces, `mortar-floe` and `loose-ice-a` .. `-g`, of 2 headings
  each with the same image twice (an east-bound path shows frame 1). As the motion budget counts moving
  pieces by name, the **water no longer drifts** (it drifted 3 px/s west, under the east-bound floes),
  and the floes are timed so that at most two move on the screen at once. Accepted in round 41 (2026-10-19):
  the still water, the 40 px.
- 2026-10-19: The user's Act 2 playtest, P.e "yes, all" (as step CA did Levels 08–09): the waves
  **rewritten L10-style** in the halt (a wave about every 2.5 s, Skitter streams of 6 at 1 s and
  Needler groups of 4 from every edge between them; 438 units at medium, was 208) with every Skimmer,
  Scuttler, Mantis, Stinger and set-piece wave unchanged; the Needler (12 HP) and the Stinger (18) take
  the capped act HP factor here (`small_act_hp.levels` + 12, 13; without it 2.0 enemies on the screen).
  Medium 2.7 on the screen and 25.0 shots a minute (was 1.1 and 17.0), two pauses over 2 s (was 14);
  hard 2.8 and 50.9 with Needler groups of 2; `bounty_scale` 0.67 → 0.33 (typical haul 1,539,
  −2.4 %). The guarding pilot has more to shoot, so the relay lost 122–158 of 240 on medium (none of
  the 8 seeds kept half) and up to 504 of 520 on hard: the integrity is retuned to **260 medium** (the
  ally's `hp`) and **560 hard** (medium's median seed 48 %, 3 of 8 keep half; hard 19–25 %). Accepted in round 41 (2026-10-19): the number of units, the integrity; the test edits (the level's totals and
  integrity, `PacingTest.PENDING_ACT_TWO_RULE`) are the main agent's.
- 2026-10-19 (round 41, step FX-C; the game's new all-levels radio test, `RadioTimeline.everyRadioLineEndsBeforeTheLevelDoes`,
  after the user's playtest "the last message is cut short"; accepted in round 41 (2026-10-19)): the Grid Online exit ends
  at **t=239** (was 235), so Okafor's t=233.5 line (ending at 238.2 s at the latest) finishes before the level
  does. The waves, the halt and the hauls are unchanged.
