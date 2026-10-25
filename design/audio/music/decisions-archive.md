# Music — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-09-30: Two themes per act, faction boss themes, two-stem intensity system.
- 2026-09-30: Vorne's motif is planted in Acts 3–4 alongside the story hints and heard openly
  from Act 5.
- 2026-09-30: Concept round 01: both sketches chosen — "Afterburner" (tracker trance/techno) and "Coalition Rising" (synth-orchestral) are both the right direction. More tracks in this style are wanted for the other themes.
- 2026-09-30: "Afterburner" and "Coalition Rising" assigned to the Act 1 A and B slots.
- 2026-09-30: Concept round 02 proposed: title, hangar, Vrell boss, Act 3 A (Mars) and Act 4 A
  (Europa) themes. The main motif and the Choir motif are pinned down as notes (see Motifs).
- 2026-09-30: Concept round 02: all five themes chosen ("awesome and spot on"). Keep composing the remaining tracks in this style.
- 2026-09-30: Concept round 03 proposed: Act 2 A (Earth), Act 5 A (belt), Act 6 A (Jupiter),
  the Ascendancy boss theme and the final boss theme. Vorne's motif is pinned down as notes
  (1-♭3-5-7, see Motifs).
- 2026-09-30: Concept round 03: all five themes chosen ("all music is great").
- 2026-10-01: Concept round 08 proposed: briefing, Act 2 B, mini-boss sting, boss warning, mission complete, act complete, mission failed and game over cues, plus full-length versions (2–2.7 min) of the title, hangar, Act 1 A/B, Act 2 A and Vrell boss themes. "Coalition Rising" full length runs at 135 BPM (was 132) so the loop is sample-exact. Stale "(proposed)" labels on round-03 tracks corrected.
- 2026-10-01: Round 08 verification and mix pass: all 14 files measured against the chosen tracks (loudness, peak, DC, band balance, per-section loudness, loop seams). "Coalition Rising" full length restored to 132 BPM with a sample-exact loop (loop length rounded to whole samples instead of the 16th step); "Dry Dock" full length less sub below 60 Hz; "Situation Room" less 150–400 Hz mud and loop-periodic radio static; the "Coalition Rising" interlude and the "Homefront" turnaround lifted so no section sits more than ~3 LU under the rest except intended breakdowns. Details in [concept/prompts.md](concept/prompts.md).
- 2026-10-01: Concept round 08: all cues and full-length versions chosen (after the verification and mix pass).
- 2026-10-01: L12 (storm) uses "Firestorm" (Act 2 B); the storm variant of the act theme is dropped (user decision).
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-01: Music player built in M0 from the spike's streamer (stb_vorbis with sample-exact
  seek, intro once then the loop section, its own thread); it plays the title theme. A headless
  test compares the stream with a straight decode across two loop seams.
- 2026-10-01: M1: the test sortie plays "Coalition Rising" (full length, track 5) with its loop points; on death the music cuts and `mission-failed-r08-a` plays as a one-shot, and the track restarts with the sortie.
- 2026-10-02: M2: Level 01 plays "Coalition Rising" from the section 2 transition over the
  Earth-orbit ambience, cuts it when the ship is destroyed, fades it over 1 s at the level end and
  starts the debrief with the mission complete jingle (`vanguard.game.audio.LevelMusic`).
- 2026-10-02: Mission complete jingle marked chosen in the track list (it was chosen in round
  08; the "(proposed)" label was stale, as were those of the other round 08 cues and full-length
  versions, corrected too). Level 01's "base stem until section 4" waits for the
  production audio after M2, which delivers the stems; until then the full mix plays from
  section 2 (user decision).
- 2026-10-02: Concept round 11 proposed: the base stem of "Coalition Rising" for Level 01's
  sections 1–3, sample-aligned with the chosen full mix (`tools/concept/audio/music_r11.py`).
- 2026-10-02: Concept round 11 (user choice): the "Coalition Rising" base stem accepted; Level 01 plays it until section 4 and crossfades to the sample-aligned full mix.
- 2026-10-02: M3 part B1: Level 01 plays the "Coalition Rising" base stem from section 2 and
  crossfades to the sample-aligned full mix at section 4 (`vanguard.game.audio.StemMix`: two
  `LoopingStream`s read in lockstep on the music thread, a linear crossfade moving by one step per
  frame over 1 s; a test checks the stems' length and loop points match). The intensity layer is
  driven by the level script; driving it by on-screen density is not built (no level asks for it
  yet). The briefing theme, the hangar theme and the game over cue are imported for the briefing,
  the hangar placeholder and the game over screen.
- 2026-10-02: Level 01 cue changed (user decisions after playing the final-art build): "Coalition
  Rising" starts at the launch with the base stem at −6 dB, rising to full in 2 s at the section 2
  transition, and crossfades to the full mix at section 3 (t=60) instead of section 4; a level's
  `music.start_db` sets the level through its start section (`LevelMusic`).
- 2026-10-02: Production art, UI batch part U3: the title ("Terran Vanguard" full length), hangar
  ("Dry Dock" full length) and briefing ("Situation Room") themes are rendered into `assets/music/`
  by `tools/art/themes.py` from the chosen generator (`music_r08.py`'s `render_loop`, unchanged),
  which already used the final settings (q6, −14 LUFS, intro + loop + 2-bar fade tail with
  sample-exact `LOOPSTART` / `LOOPLENGTH`); the encoded stream is remuxed with a `SOURCE` comment,
  so the audio is identical to the chosen files. `importPlaceholders` copies the music through
  `PlaceholderSounds` now (`copyPlaceholderMusic`, concept name `=` game name), which keeps an OGG
  with a `SOURCE` comment. `themes.py --check` verifies loudness (±0.5 LU), true peak, q6, the loop
  comments and the seam. Review sheet proposed for round 13; `art` stays `chosen`.
- 2026-10-02: Concept round 13 closed (user decision): the title, hangar and briefing themes (`tools/art/themes.py`, identical to the chosen mixes) approved as **final**; `art` stays `chosen`, since the other tracks of the 28 are not final yet.
- 2026-10-02: Concept round 15 closed (user decision): the "Afterburner" base stem approved as **final**.
- 2026-10-03: M4 part E: track 21, the mini-boss sting, is imported (`assets/music/miniboss-sting.ogg`)
  and plays when Level 05's frigate arrives (`music.boss_sting`): the level theme dips out over
  0.5 s, the sting plays, the theme comes back over 0.5 s with the intensity stem on.
- 2026-10-05: Production art, M4 part G: tracks 18 ("The Choir Descends" full length), 22 ("Red
  Alert") and 24 ("Act Complete") are rendered into `assets/music/` by `tools/art/themes.py` from
  the chosen generator (`music_r08.py`, `render_loop` / `render_sting`, unchanged) and remuxed with
  a `SOURCE` comment, as round 13 did for the title, hangar and briefing themes; the audio is
  identical to the chosen files, so their character is kept as it is. No stems for the boss theme.
  `copyPlaceholderMusic` keeps its lines (concept name `=` game name): `PlaceholderSounds` leaves the
  final files alone. `themes.py --check` now also covers one-shot cues (no loop comments, silent
  end) and the boss cue's hand-off as `BossCue` mixes it (track 18 from the sample at
  `Tracks.BOSS_WARNING_BARS_SECONDS`, exactly three bars at 150 BPM; no clipping; tail more than
  30 dB under the track; loudness step within 4 LU: measured −3.3 LU). Measured only, not listened
  to. The Options sound test lists the three tracks. Review files and the sheet proposed for round
  25; `art` stays `chosen`.
- 2026-10-05: Round 25 closed (user): tracks 18, 22 and 24 approved as final as they are (the
  22 → 18 hand-off's 3.3 LU drop and the loop seam accepted); the boss warning's crossfade and the
  hand-off into track 18 are built (`BossCue`), so the crossfade item is done. The document's `art`
  stays `chosen`: the level themes, the jingles and the mini-boss sting in `assets/music/` are still
  the chosen concept files.
- 2026-10-05: The open question "tracker modules (.xm/.it) or rendered audio?" is settled by what
  was built: rendered audio, OGG Vorbis q6 with the intro and loop in one file and the loop points
  as `LOOPSTART` / `LOOPLENGTH` comments (*Design*), generated by `tools/art/themes.py` and played
  by the game's music player. Closed in the M4 part H docs reconciliation.
- 2026-10-05: Production art, M4 part H (user decision D6 = A: check the remaining concept copies
  against the final spec, fix only what fails, offer them as they are): tracks 4 and 5 with their
  base stems, the mini-boss sting and the jingles 23, 25 and 26 are rendered into `assets/music/` by
  `tools/art/themes.py` (keys `afterburner`, `afterburner-base`, `coalition`, `coalition-base`,
  `miniboss`, `complete`, `failed`, `gameover`) from their chosen generators unchanged and remuxed
  with a `SOURCE` comment. `--check` now covers the stems too: instead of −14 LUFS a stem must sit
  under its full mix (it carries the full mix's gain for the crossfade) with the same length and
  loop comments. Before the pass all eight files failed only for the missing `SOURCE` comment,
  except **"Afterburner" (true peak −0.5 dBTP) and its base stem (−0.8 dBTP)**: the limit is −1
  dBTP, the overs were inter-sample peaks on single kick transients (31 and 19 places) that the
  Vorbis encoder makes from a PCM limited at −2 dBFS. A plain gain cut would have needed 0.5 dB and
  taken the full mix to −14.5 LUFS, outside the ±0.5 LU tolerance, so the pair gets one shared gain
  envelope that dips only around those transients (57 dips, at most −1.1 dB, on average −0.24 dB,
  3.1 s of 154.7 s in total; 2 ms attack, 50 ms release), applied to both files so the base → full
  crossfade stays exact, then re-encoded at q6 (`TRUE_PEAK_FIX`). Measured after: −1.2 dBTP both,
  loudness unchanged (−14.0 and −15.4 LUFS), loop seam unchanged (0.47, 0.28), the intended change
  55 dB under full scale (RMS), 16 dB under the encoder's own noise in the chosen file. The other
  six files are identical to the chosen ones. Measured only, not listened to. `copyPlaceholderMusic`
  reports all 14 music files kept as final. Review files, four loop-seam aids and the sheet
  `music-final-r26-a.png` proposed for round 26; `art` stays `chosen` until the user approves them.
- 2026-10-05: Concept round 26 closed (user: the round accepted as proposed, D6 = A): #4 "Afterburner" and #5 "Coalition Rising" with their base stems, #21 "Contact Heavy", #23 "Mission Complete", #25 "Mission Failed" and #26 "Game Over" approved as **final**, Afterburner's and its stem's true-peak gain dips included; with them every file in `assets/music/` (the 14 tracks and stems Acts 1 uses) is final. `art` stays `chosen`, since the other tracks of the 28 (Acts 2–7, the Ascendancy and final bosses, the ending and credits) are not final yet.
- 2026-10-06: M5 part B (user decision D6 = a and the gap defaults): "Homefront" (track 6) goes
  straight to production for Level 08: the chosen full-length mix checked against the final spec
  and rendered by `tools/art/themes.py`, plus one base stem from a new frozen stem generator
  (`music_r30.py`, the round-11 method), both reviewed as final in concept round 30. Level 08:
  ambience only in section 1, the base stem from section 2, the full mix in section 5. The
  document goes back to `in-progress` for the track map.
- 2026-10-07: [Concept round 30](../../concept-rounds/round-30/README.md) closed (user,
  2026-10-07): track 6 "Homefront" and its base stem approved as **final** (the base stem without
  a concept round of its own, its 0.61 seam and the city ambience in section 1 as they are). The
  track map item is ticked, so the implementation is `done` again; `art` stays `chosen` (the other
  Act 2 tracks are concepts until their rounds).
- 2026-10-07: M5 part C (user decision D9 = a and the stated defaults): "Firestorm" (track 7) goes
  straight to production for Level 09, the chosen full mix checked against the final spec and a
  base stem from a new frozen stem generator (`music_r31.py`), both reviewed as final in concept
  round 31. Level 09: the base stem from the launch, the full mix in each hold zone (1 s in, 4 s out)
  and from the collapse, through a new run-time hook (`full_on`). Corrected: the intensity layer is
  switched by section only; "driven by density", here and in Level 08's text, was never built. The
  document goes back to `in-progress`.
- 2026-10-07: M5 part C: "Firestorm" (track 7) goes straight to production for Level 09 (also L11,
  L12 and L14's approach): the chosen `act2-b-theme-r08-a` rendered by `themes.py` plus a base stem
  from the new frozen `music_r31.py` (the round-11 method; the base without lead, brass, the stab
  riff's short strings, the melody strings, the breakbeat top, toms and crashes). The pair failed the
  true-peak limit (−0.2 / −0.1 dBTP) and got round 26's shared transient dip (8 dips, at most
  −1.32 dB); reviewed as final in concept round 31. Measured only, not listened to.
- 2026-10-07: M5 part C, game side: the `full_on` hook is built in `LevelMusic` (the full mix while
  the sortie's hold zone runs or from the collapse on, as the level's `full_on` names them; 1 s in,
  4 s out after a hold ends, a section's own change keeps the 1 s crossfade); track 7 maps to
  `firestorm` / `firestorm-base`.
- 2026-10-07: [Concept round 31](../../concept-rounds/round-31/README.md) closed for the music
  (user, 2026-10-07): track 7 "Firestorm" and its base stem approved as **final** (the base stem
  without a concept round of its own, its thin sections A and D, and the 8 transient dips as they
  are). Every item is ticked, so the implementation is `done` again; `art` stays `chosen` (the other
  Act 2 tracks are concepts until their rounds).
- 2026-10-08: M5 part D (stated defaults with the user's decisions of 2026-10-08): Level 10 plays
  "Homefront" (track 6) with the base stem from section 2 and the full mix from section 3 (t=70; the
  draft's "intensity stem from t=82" had no hook, mid-section); at its scripted loss the music ducks
  −6 dB for 3 s with no sting, a new scripted duck that does not stack with the radio's (the lower
  wins); the ambience crossfades from the megacity to the ocean at section 4. The implementation is
  `in-progress` again for the duck and the ambience change.
- 2026-10-08: M5 part D, game side: the scripted duck and the ambience change by section are built.
  The duck's target is the lower of the radio's −4 dB and the event's −6 dB while its 3 s run, and
  the level glides to it at the radio duck's rate (no click); the crossfade is linear over its 4 s.
  Our reading, for review: a restart goes back to the level's first ambience at once (no crossfade).
- 2026-10-08: M5 part E (stated defaults of 2026-10-08): Level 11 plays "Firestorm" (base stem from
  section 2, full mix from section 4) over the ocean ambience, with the mini-boss sting at the arena
  halt as the other mid-bosses (the sting rule: a mid-boss's sting plays when its bar appears; the
  level draft's "when the Kraken surfaces" reworded); no new music file. The implementation stays
  `done`: the level's data uses the existing keys.
- 2026-10-09: M5 part F, **F5 = a** (user): Level 12 plays "Firestorm"'s base stem at −6 dB in the
  eye and the full mix again at the breakout (the level document's "base stem drops out in the eye"
  would have left the loud half); a per-section level (`section_db`) is the only new key, the
  section switches use the built `stems`. Rejected: b (a third, sparse "eye" stem and a
  three-stream mixer) and c (the theme fading out in the eye). The implementation is `in-progress`
  until part F builds it.
- 2026-10-11: M5 part F step 6 (pending the user's review in the combined round 34): a section's
  `section_db` level is reached linearly over 1 s from its start (Level 12: down to −6 dB as the eye
  begins, back to full as the breakout begins); it multiplies the theme's other levels (the start
  level's rise, the radio's and the scripted ducks, the fades), so the radio's duck in the eye lands
  at −10 dB.
- 2026-10-11: M5 parts G and H (the user is on vacation; the main agent took the gap analyses' recommendations, **pending the user's review** in the combined round): Level 13 plays "Homefront" from section 2 with a mid-level jingle at t=218 and none at the debrief (G8 = a); Level 14 plays "Firestorm" with the boss warning and theme from t=108, no music in the daylight and `act-complete` under the outro's first page (H6 = a).
- 2026-10-12: M5 part G, step G6 (**pending the user's review in the combined round**): Level 13's jingle
  starts after the theme's 1 s fade (t=219, under Nansen's line; the theme does not return in that
  attempt, a lost relay's cut music plays none); the debrief of a level whose jingle played inside it
  starts silent. Grid online's sound (round 35) plays at the synchronisation's end (t=218), under the
  fade, so its chord leads into the jingle.
- 2026-10-12: M5 part G, step G8 (**pending the user's review in the combined round**): grid online's
  chord (2.6 s from t=218, about as loud as the jingle) sat on the jingle's first second. Level 13's
  `jingle` is now `{t: 218, fade: 2.6}`: the theme still starts fading with the chord, over its length,
  and the jingle starts as the chord ends (t=220.6, still under Nansen's line). Rejected: moving
  `jingle.t` to 219.6 with the 1 s fade (the theme would play on at full under the chord) and a separate
  jingle delay in the game (the fade models it already).
- 2026-10-12 (overnight design phase, **pending the user's review in round 36**): Act 3 music
  written as a draft (*Act 3 music*): track 8 in Levels 15–17 and 20, track 9 (the B theme) in
  Levels 18, 19 and the Level 21 approach, as the act README plans; track 9's brief (122 BPM, B
  phrygian, dusty four-on-the-floor with track 8's reed, three stems); Vorne's motif as a third stem
  played where the grey machines are (Level 18 from the Ghost Drones' first sight, Level 19 under the
  walker and alone over its wreck), muted in Level 21; the cues per level. Track 8's full length is
  new composition (only a 53.8 s sketch exists) and gets its own review in part B.
- 2026-10-13 (user, round 36): the *Act 3 music* section approved (D.28) with the motif as a third
  lockstep stream (Q.24d) and track 8 for Level 20 (Q.20e; track 9 is built in part E). **S.1 = b**:
  track 9 follows the sketch "Grey Signal" (125 BPM, D minor dub techno, the motif as an intercepted
  radio whistle through a band-pass); its brief and the motif layer rewritten to match (Q.24e), the
  first brief's 122 BPM B phrygian dropped. Sketch a "Oxide Drift" rejected and moved to
  `concept/rejected/`.
- 2026-10-14 (M7 part B, step AUD-B, **pending the user's review in round 38**): track 8 "Red Dust Run"
  composed out to full length (`music_r38.py`: 4-bar intro + a 96-bar loop of A, B, the new "caravan"
  D, A2, the storm C, the new bridge E, B2, the desert F without kit, the build G, B3, A3 and the turn
  T; 181.8 s) and its base stem rendered from the same pass (provisional: the README's order, full mix
  before stem, is kept by re-rendering the stem if the review changes the full mix), both as final files
  in `assets/music/` (`themes.py`, `PAIR_RENDERED`; no true-peak dips needed). The game's track map
  entry (8 → `red-dust-run`) is part B's game step.
- 2026-10-14 (user, round 38): track 8 "Red Dust Run" at full length approved as final (M.1); its base stem,
  rendered from the same pass ahead of the README's order, approved as it is (M.2): it stays in
  `assets/music/`. `art` stays `chosen` (the other Act 3 tracks are to come).
- 2026-10-18 (user request; step MUS-D, **pending the user's review in round 40**): the debrief gets a music loop of its own after the 6 s mission complete jingle: new track 29 "Debrief" (P2), quiet (−19 LUFS), D major so it follows the jingle, not "Dry Dock" again. Two proposals: a "Homeward" (synth-orchestral, the motif on a solo horn) and b "Stand Down" (laid-back electronic, the motif hinted), each with a jingle → loop listening aid; the game wiring follows the pick.
- 2026-10-19 (user, round 40 M.a = b): track 29 "Debrief" is b "Stand Down"; a "Homeward" and its
  listening aid moved to `concept/rejected/`. Wired the same day (step MW, **pending the user's review**
  for the judgement calls): the theme comes in on the jingle's downbeat (5.714 s), plays at the jingle's
  level (0.8, so the handover sounds as the listening aid), carries on under the act summary page (which
  had no music of its own after the jingle), and fades out over 0.5 s when the player leaves, the next
  screen waiting for the fade; the act outro keeps its act-complete fanfare.
- 2026-10-20 (user, round 44, A.b (a): production assets (a `Source` chunk in `assets/`, every Concept art row decided)): **`art: final`** for this part. A later addition (a new sprite, a new concept round) is its own row and does not reopen the rest.
