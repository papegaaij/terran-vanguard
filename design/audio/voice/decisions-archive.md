# Voice — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-10-03: Created after concept round 18: Chatterbox chosen for the spoken radio lines (user:
  "by far the best"), reversing the 2026-10-01 "no voice acting" decision in
  [Audio](../README.md#decisions); the text stays as subtitles. Draft for the user's review.
- 2026-10-03: The open questions decided by the user: radio filter **b** (more static, round 18
  item 2); the main cast keeps round 18's LibriVox readers, the generic speakers get readers from
  the same CC0 and public-domain sources, auditioned in a short round (round 19); the Choir is
  voiced (layered as in round 18) and the briefings too (same pipeline, without the radio
  filter); a separate voice volume slider; a subtitle page is held until its voice line ends; a
  `shout` field on radio cues, separate from the queue priority (user decision).
- 2026-10-03: The reference clips moved into the repo (`refs/`, Git LFS, in CREDITS.md); concept
  round 19 opened to cast the generic speakers.
- 2026-10-03: Round 19 closed (user): Yard Control Mark Nelson (a), the docks Kristin Hughes (b),
  Ring Control Cori Samuel (b), Tranquility Control Karen Savage (b, not round 18's David Leeson),
  Crawler One Kara Shallenberg (b), Convoy Adrian Praetzellis (a), Hammer Lead Gord Mackenzie (b);
  no voices merged. The rejected takes moved to `concept/rejected/`, the unused clips were
  deleted with their CREDITS.md rows.
- 2026-10-03: Implemented: the speaker table, the line list and keys (`VoiceLines`), the `shout`
  field (seven shouted Act 1 lines: Rook's and Okafor's flank warnings in Level 01, Dock One,
  Rook's right-flank call and Dock Four in Level 02, Convoy and Tranquility Control's walkers in
  Level 04), the renderer `tools/art/voice.py` (it deletes unused files rather than only listing
  them), playback in the level, the briefings and the mission failed screen, and the voice
  slider. Act 1's lines rendered; their review and the timing of the voiced radio are open.
- 2026-10-03: M4 part E (user decision): Level 05's new speaker Driver Control gets its own voice
  from a short audition (two CC0 or public-domain candidates, as in round 19), held later in the
  part; no voice is merged with Tranquility Control.
- 2026-10-03: M4 part E: until its audition, Driver Control is marked `uncast: true` in the
  speaker table (no `ref`): its Level 05 lines have no voice file and play as text with the radio
  blips; VoiceFilesTest allows a missing voice only for such a speaker, and Level 05's lines are
  checked (20 rendered: the radio, the four per-battery failure lines from `{group}`, the
  secret and the briefing pages).
- 2026-10-04: Driver Control audition (user decision D4 of M4 part E) opened in round 21: its
  t=12.5 warning with two public-domain LibriVox voices (`tools/concept/audio/tts_r21.py`).
- 2026-10-04: Round 21 decided (user): Driver Control is Alex Foster (a); `uncast` removed from
  the speaker table, Rebecca's take moved to `concept/rejected/` and her clip deleted with its
  CREDITS.md row; Driver Control's lines rendered by `tools/art/voice.py`.
- 2026-10-04: M4 part F (user decision D7): Level 06's Daedalus perimeter beacon gets an audition
  of two CC0/PD candidates through a public-address filter, in the part's concept round; the same
  line loops under section 2. `uncast` until then. Rejected: reusing a cast generic voice with the
  PA filter (no audition).
- 2026-10-04: Perimeter beacon audition (user decision D7 of M4 part F) opened in round 23: its
  t=50 message with two LibriVox voices, Mark F. Smith (a, CC0) and Lucy Burgoyne (b, public
  domain), through the new public-address filter (`pa()` next to `radio()` in
  `tools/concept/audio/tts_r18.py`; horn band and resonances, slap echoes and a hall instead of
  static and squelch clicks); `tools/concept/audio/tts_r23.py`.
- 2026-10-05: Round 23 decided (user): the perimeter beacon is Mark F. Smith (a), through the
  public-address filter only, as auditioned (not the radio filter on top); `uncast` removed,
  `ref-perimeter-beacon.wav`, `fixed` settings as the audition's and `filter: pa` in the speaker
  table (`VoiceLines.Filter.PA`, part of the line's key; `pa()` in `tools/art/voice.py`'s post);
  Lucy Burgoyne's take moved to `concept/rejected/` and her clip deleted with its CREDITS.md row.
  The beacon's line rendered (6.9 s with the echoes' tail; Whisper reads it back), so it is spoken
  at t=52.5 and loops under section 2.
- 2026-10-05: Okafor's Level 06 line "Domes intact. Airlocks open. Nobody's home." hit the length
  cap with all eight seeds: three short sentences, with Okafor's pauses between them, ran past
  1.5× an estimate that gave a sentence end only 0.12 s. The estimate now gives a sentence end
  0.4 s (an ellipsis counts once); re-rendered, the third seed passed (4.6 s; Whisper reads it
  back). Only missing lines are rendered, so no other file changed.
- 2026-10-05: Lifeboat Seven audition (M4 part G, Level 07) opened in round 25: its t=22 line with
  two public-domain LibriVox voices, Tadhg Hynes (a) and Lizzie Driver (b), neutral settings
  (0.5 / 0.5 / 0.7) through radio filter b (`tools/concept/audio/tts_r25.py`). Until the choice the
  speaker is uncast (an `uncast: true` row in the speaker table, written with Level 07's data) and
  its line plays as text.
- 2026-10-05: M4 part G: Level 07's lines (18: the two briefing pages, 15 radio lines and the lifeboat
  tow secret; Okafor 6, Varga 8, Rook 4) and the Act 1 outro's four pages
  (dry, Okafor 3, Varga 1) rendered by `tools/art/voice.py`; Lifeboat Seven's t=22 line stays text
  until round 25 casts it. One take hit the length cap and passed on the next seed (Okafor's
  "Every bay gutted…"); Rook's "Okay. Okay. That was big. We did big." repeated its last sentence
  with the key's seed and was re-rolled on the next seed, 267639418 (to be pinned in the speaker
  table so a rerun keeps it). Whisper reads every take back (names such as Aegis and Daedalus only
  with a name prompt, as in the earlier levels). Outro page 1 is 14.3 s, under track 24's 16.3 s.
  VoiceFilesTest now checks Level 07 and the outro; RadioTimelineTest no longer exempts Level 07.
- 2026-10-05: Round 25 decided (user): Lifeboat Seven is Tadhg Hynes (a), at the audition's
  neutral settings through radio filter b; `uncast` removed, `ref-lifeboat-seven.wav` in the
  speaker table; Lizzie Driver's take moved to `concept/rejected/` and her clip deleted with its
  CREDITS.md row. The t=22 line rendered by `tools/art/voice.py` on the key's seed (6.2 s of speech,
  6.5 s with the filter's tail); Whisper reads it back word for word, "Aegis" included, without a
  name prompt. VoiceFilesTest now requires every speaker of a rendered level to be cast, and
  RadioTimelineTest no longer exempts an uncast speaker's line. Level 07's other lines and the four
  outro pages were accepted as rendered (round 25, choice 10).
- 2026-10-05: M4 part H: Okafor's low-armour line ("Lancer, your hull won't take much more. Fly
  careful.", grim, from the [armour](../../player/armor/README.md) data; source `armour` in the line
  list) rendered by `tools/art/voice.py`'s pipeline, only that line (one GPU job): the key's seed
  passed at once, 4.5 s of speech under the 7.0 s cap (`assets/voice/okafor/14b14dfe36a3.ogg`,
  4.8 s with the filter's pad). Whisper (base.en) reads the dry take back word for word; through
  radio filter b it hears "howl" for "hull", a short word in the band-pass, so the user's ear
  decides in round 26. VoiceFilesTest requires its file; RadioTimelineTest plays it urgent in every
  level.
- 2026-10-05: Concept round 26 closed (user: the round accepted as proposed): the 128 voiced lines
  no round had reviewed (the act briefing's 5 pages, Levels 01–06's 12 briefing pages and 110 radio
  lines, Hammer Lead's Airstrike call) and Okafor's low-armour line accepted as rendered, none to
  re-render; with round 25's Level 07 and outro, all 151 Act 1 lines are reviewed, so the item is
  ticked and the implementation is `done`. `art` stays `chosen`: the later acts' lines are not
  rendered yet. The document's `design` stays `draft` (not part of this round's approval).
- 2026-10-06: M5 part A: Rook's 27 radio barks ([wingmen](../../player/wingmen/README.md#radio-barks),
  source `wingmen barks <trigger>`; grim for the boss warning and the armour barks, the shout row for
  the rear, sides and eject barks, fierce for the kill streak and the overdrive) rendered by
  `tools/art/voice.py` in one GPU job, radio filter b; no other line of the list was missing. Every
  take passed the length check on its first seed (0.96–4.47 s of speech, none near the cap). Whisper
  (base.en) read each take back, dry and through the filter; four were re-rolled on later seeds and
  pinned in the speaker table: "Rook's hurting, Lancer. Hurting bad." (the key's seed said
  "herding", dry too), "Flank! Watch the edges, Lancer!" ("Clank" through the filter), "Power-up!
  Grab it!" (the "P" lost in the filter) and "Overdrive's up for grabs, Lancer!" ("Over drives");
  each pinned seed is the first of four tried that Whisper reads back word for word both ways. The
  rest read back word for word apart from spelling ("6" for "six", "Punchin'", "Hah"). VoiceFilesTest
  now requires the barks' files. For the user's ear in concept round 28.
- 2026-10-06: Concept round 28 closed (user: accepted): Rook's 27 radio barks accepted as rendered,
  the four pinned takes included, none to re-render (the rear-wave variant "Contacts on six! Why is
  it always six?" stays, beside Level 06's scripted take of the same line). `art` stays `chosen`:
  the later acts' lines are not rendered yet.
- 2026-10-06: The Choir's radio messages made no sound (user): its Act 1 cues are the stage
  direction `[the Choir sings]`, which is not spoken, and the rule "stays text, with the music's
  choir pads" played nothing in the line's place. User decision:
  hear both kinds of sound in a concept round and choose by ear; round 29 opened with a wordless
  sung sting in the Choir's voice (a: Chatterbox's own sustained "ooh", pitch-flattened onto E3;
  b: an "ah" built from half a second of vowel, sung F3 to E3) and a synthesized choir pad like the
  music's (c: an "oo" chord; d: the motif's F4 to E4 over a pad), each through the Choir's
  room and radio filter b at −16 LUFS (`tools/concept/audio/choir_r29.py`). Wired data-driven: the
  speaker table's new `stage` field names the sound a speaker's stage-direction line plays
  (`VoiceLines.stageSound` / `radioVoice`, used by `Voices` and RadioTimelineTest), option a
  provisionally, copied into `assets/voice/choir/` by `:pipeline:copyPlaceholderStageSounds`;
  `tools/art/voice.py` now deletes only unused files named by a key.
- 2026-10-06: Concept round 29 closed (user): the Choir's `[the Choir sings]` plays **b**, the
  sung "ah" in the Choir's voice falling F3 to E3, the motif's landing (the speaker table's
  `stage`, copied into `assets/voice/choir/` by `:pipeline:copyPlaceholderStageSounds`). Rejected:
  a (the held "ooh" on E3) and the synthesized pads c ("oo" chord) and d (the motif over a pad),
  moved to `concept/rejected/` (`choir_r29.py`'s `CHOSEN`).
- 2026-10-06: M5 part B (user decisions D4 = a and D6 = a): Level 08's radio is voiced like Act 1's
  (its "text only" note is reversed in the level's document); a line with **`{side}`** expands to a
  `left` and a `right` line, played by Rook's side; the **Ikoyi shelter civilian** ("shelter nine",
  two lines) is auditioned a/b in concept round 30 and plays as text until cast. The document goes
  back to `in-progress` for part B's renders.
- 2026-10-06: Round 30's civilian audition rendered (`tts_r30.py`): a KirksVoice (African Myths,
  clip pitch 111 Hz), b Faith Abiola-Ellison (Yoruba-speaking Peoples, 151 Hz, near Okafor's 150 Hz),
  both LibriVox public domain (Public Domain Mark 1.0), both lines 1 s apart, neutral, radio filter
  b, −16 LUFS; reference clips and Whisper transcripts in `refs/` and CREDITS.md. Measured only.
- 2026-10-06: M5 part B: `{side}` is built: `VoiceLines` lists a `{side}` line once per side
  (`left`, `right`), and the level screen shows and plays the take of Rook's side setting (left
  when he does not fly).
- 2026-10-06: M5 part B (step B9): Level 08's lines and the Act 2 act briefing rendered by
  `tools/art/voice.py`'s pipeline (only the act's missing lines, nothing deleted), 19 lines: the
  act briefing's four pages and Level 08's two briefing pages (dry), its ten radio lines with
  Rook's t=8.5 side line once per side, his `escort-first-kill` line, and the billboard secret
  (Okafor 10, Varga 4, Rook 5). The Civilian's two lines stay text (uncast until round 30 casts
  her); the Choir's `[the Choir sings]` plays its stage sound; the hangar teaser is not voiced.
  Every take passed the length check, one chunk on its second seed (page 1's "Aegis Actual out.").
  Whisper (base.en) read each take back, dry and through the filter; three Okafor takes were
  re-rolled and pinned in the speaker table, each the first of four seeds that reads back word for
  word both ways: act page 3 (the key's seed said "you've fought"), Level 08's t=1 line ("contact
  inbound") and the level-end line ("arcologies" heard as "all colleges" through the filter). Level
  08's briefing page 1 (32.2 s) keeps the key's seeds: "Bring each other home" reads back only
  with a name prompt, as with all four other seeds tried, and "Ikoyi" and "Vrell" need the prompt
  too. VoiceFilesTest now checks Level 08 and the Act 2 briefing, with the Civilian allowed uncast
  while her audition is open (`AUDITIONING`); RadioTimelineTest no longer exempts Level 08 and
  plays an uncast speaker's line as text in any level. With the real lengths no voiced timed line
  of Level 08 starts more than a second late. For the user's ear in concept round 30.
- 2026-10-07: Level 08's two briefing pages, shortened to fit one screen each, re-rendered by
  `tools/art/voice.py` (only those two lines; their old files deleted as unused): Okafor 20.0 s,
  Varga 12.7 s, both on the key's seed with no retry. Whisper (base.en) reads them back word for
  word but for "the streets in low roofs" for "and" and "Everyone" for "Every one" (with a name
  prompt: Lancer, Rook, Vrell and Ikoyi spelt right).
- 2026-10-07: Round 30's civilian audition re-rendered (`tts_r30.py`, same clips, settings and
  seeds) with the t=113 line's current wording "Shelter nine, Ikoyi! Walkers on the roofs, heading
  our way!" (the first renders read the earlier call): a 10.76 s, b 10.84 s, −16 LUFS. Whisper
  (base.en) reads a back word for word and b with "Sheltonine" for "shelter nine". Measured only.
- 2026-10-07: Concept round 30 closed (user): the Ikoyi shelter civilian is **b**, Faith
  Abiola-Ellison (a woman's voice, clip pitch 151 Hz), at the neutral settings through radio filter
  b, as auditioned; her clip is `refs/ref-civilian.wav` and her two lines are voiced (the t=113
  call pinned on seed 86689958, read back word for word as "Shelter 9, Ikoyi, …"; the secondary's
  thanks on seed 179901323), so `AUDITIONING` is empty again. KirksVoice (a) rejected: his audition
  moved to `concept/rejected/`, his clip deleted with its CREDITS.md row. The 19 lines of Level 08
  and the Act 2 act briefing accepted as rendered, the three pinned Okafor takes included; none to
  re-render. Every item is ticked, so the implementation is `done` again.
- 2026-10-07: M5 part C (user decision D11 = a): Level 09's CDF officer of the Kilo convoy, speaker
  `Kilo Lead`, gets an audition of two new public-domain or CC0 readers (a/b) in concept round 31,
  as the Ikoyi shelter civilian did in round 30; he is `uncast` in the speaker table until then
  and his two lines play as text (`VoiceFilesTest`'s `AUDITIONING` takes him while the round is
  open). Rejected: b (reusing a generic reader already heard in another role, against round 19's
  one voice per role). Level 09's mission failed line names the node (`{group}`), so it is voiced
  once per node, as Level 05's battery line. The document is `in-progress` again for part C.
- 2026-10-07: M5 part C (step C9): Level 09's lines rendered by `tools/art/voice.py`'s pipeline
  (only the missing lines, nothing deleted), 20 lines: its two briefing pages (dry), its eleven
  spoken radio lines (the `hold-start`, `first-kill`, `first-pounce`, `collapse` and level-end event
  lines included; no `{side}` line), the cocoon secret's line and the mission failed line once per
  node (A1–C2) (Okafor 11, Varga 5, Rook 4). The Kilo Lead's two lines stay text (uncast until
  round 31 casts him); the Choir's `[the Choir sings]` plays its stage sound; the hangar teaser and
  the intel lines are not voiced, as in Level 08. Every take passed the length check on its first
  seed. Whisper (base.en) read each take back, dry and through the filter; "arcology", "Vrell" and
  "Airstrike" need a name prompt (without it: "archeology", "Vrel", "air strike"). Four takes were
  re-rolled (four seeds each) and pinned in the speaker table: Varga's briefing page 2 (the key's
  seed read "the streets and packs"; the pin reads "in packs" both ways) and the failed lines of
  nodes A1, A2 and C1 (the key's seeds ran "you, Lancer" into "you'll answer"; the pins read back
  word for word but for sound-alikes: "U Lancer" for A1, "See, one" for C1 through the filter
  without a prompt). VoiceFilesTest now checks Level 09, with the Kilo Lead in `AUDITIONING` while
  round 31 is open; RadioTimelineTest no longer exempts Level 09 (`UNVOICED` is empty). With the
  real lengths no voiced timed line of Level 09 starts more than a second late. For the user's ear
  in concept round 31.
- 2026-10-07: Concept round 31 closed (user): the Kilo Lead is a, Aaron Bennett, neutral as
  auditioned; clip renamed `refs/ref-kilo-lead.wav`, b deleted with its CREDITS.md row; his two
  Level 09 lines voiced (no pins needed), so `AUDITIONING` is empty again. tombooker (b) rejected:
  his audition moved to `concept/rejected/`. The 20 lines of Level 09 accepted as rendered, the four
  pinned takes included; none to re-render. Every item is ticked, so the implementation is `done`
  again.
- 2026-10-08: M5 part D (user decision D12 = a): two new speakers for Level 10, auditioned a/b in
  round 32: `lifeline` (names Lifeline, Lifeline One, Two, Four and Five: the lead pilot and the
  hit line, one voice) and `lifeline-three` (one line, cut off), both `uncast: true` in the speaker
  table until the pick; rejected: b (one voice for every shuttle, the lost pilot sounding like the
  lead) and c (reusing Level 08's civilian and Level 07's Lifeboat Seven). The cast speakers' Level
  10 lines render as soon as the texts are final. The implementation is `in-progress` again.
- 2026-10-08: M5 part D (step D9): Level 10's lines of the cast speakers rendered by
  `tools/art/voice.py`'s pipeline (only the missing lines, nothing deleted), 22 lines: its two
  briefing pages (dry), its spoken radio lines (the `first-decloak`, `first-loop-back`,
  `scripted-loss` and mission failed lines included; `ally-lost` once per shuttle, Lifeline One to
  Five, as Level 04's convoy lines; the four level-end lines by shuttles home; no `{side}` line) and
  the ferry secret's line (Okafor 13, Varga 5, Rook 4). The Lifeline speakers' lines (t=1, t=116,
  t=196 and the `first-ally-hit` line) stay text until round 32 casts them; the Choir's
  `[the Choir sings]` plays its stage sound; the hangar teaser and the intel lines are not voiced,
  as in Levels 08 and 09. Three `ally-lost` takes hit the length cap and kept their second or third
  seed. Whisper (base.en) read each take back, dry and through the filter; "Eko" reads "Eco",
  "Vrell" and "Lagos" need a name prompt, and the numbers come back as digits. Four takes were
  re-rolled (four seeds each) and pinned in the speaker table: Okafor's briefing page 1 (the key's
  seed read "Fides shuttles"), the four-home level-end line ("eight to hundred and eighty" dry),
  Varga's briefing page 2 ("The rail hunt" but with a prompt through the filter; the pin reads
  "Vrell" with a prompt both ways, "rail" without, and "Flock sweep" once through the filter) and
  the ferry line ("berry's" through the filter; the pin reads the sound-alike "ferries" every way).
  Okafor's grim lines leave a pause of about 0.6–1 s between "Lifeline" and its number (the
  `ally-lost` takes and `scripted-loss`): every take does it, so it is left for the ear.
  VoiceFilesTest now checks Level 10, with the Lifeline speakers in `AUDITIONING` while round 32
  is open; RadioTimelineTest no longer exempts Level 10 (`UNVOICED` is empty; the Lifeline lines
  play as text by the uncast rule). With the real lengths no voiced timed line of Level 10 starts
  more than a second late. For the user's ear in concept round 32.
- 2026-10-08: Concept round 32 decided (user) for the two voices, **b** and **b**: Lifeline is
  KevinS (clip pitch 138 Hz), Lifeline Three is Maria Kasper (215 Hz), both at the neutral settings
  through radio filter b, as auditioned; their clips renamed `refs/ref-lifeline.wav` and
  `refs/ref-lifeline-three.wav`, `uncast` replaced by the `ref` in the speaker table. Atul Sharma
  and Kehinde (a) rejected: their auditions moved to `concept/rejected/`, their clips deleted with
  their CREDITS.md rows. `tools/art/voice.py` now cuts off a radio line whose text ends in a dash
  (Lifeline Three's, as auditioned: Whisper times the take, the take is cut where its last word
  ends into 0.3 s of static and 0.25 s of dying hiss, with no closing click; tts_r32's
  `cut_off()`). The Lifeline speakers' eight lines rendered (only the missing lines, nothing
  deleted): t=1 (5.4 s), t=196 (4.1 s), the hit line once per shuttle by `{ally}` (4.6–4.9 s; "Lifeline
  Three, we're hit!" is rendered but never plays, Lifeline Three being untouchable) and Lifeline
  Three's t=116 line (4.6 s, cut after "that" at 3.88 s of the take: the voice at −14 dB (50 ms
  RMS) runs into the static at −16 to −20 dB, dying to −40 dB). Every take passed the length check
  on its first seed and is −16 LUFS. Whisper (base.en) and librosa pyin measured each take raw and
  filtered: four of the five hit lines jumped to about 450–500 Hz on "hit" or "Still" (Two
  throughout; the audition's octave jump) and Four read "Lifeline, or"; t=196 read "Core to clear".
  Five takes re-rolled (six seeds each, Five fourteen) and pinned in the speaker table on takes whose
  words stay at 70–190 Hz: the hit lines One, Two, Four and Five (read back "Lifeline 1/2/4/5,
  we're hit. Still flying. Please stay close.") and t=196 (read "Corridor clear" without a prompt).
  "Eko" still reads "Echo" (the same sound) and "Aegis" "ages" without a prompt; Lifeline Three's
  hit line reads "Lifeline. Free." through the filter (raw: "Three"), left as it never plays.
  `AUDITIONING` is empty again; RadioTimelineTest passes with the real lengths (no voiced timed
  line of Level 10 starts more than a second late). Measured only, not listened to.
- 2026-10-08: M5 part E (user decision E10 = a of 2026-10-08): Level 11's CDF convoy officer, speaker
  `Atlas Control`, gets an audition of two new public-domain or CC0 readers (a/b) in concept [round 33](../../concept-rounds/round-33/README.md),
  as the Kilo Lead and Lifeline did; rejected: b (reusing an Act 1 secondary voice, Convoy or Yard
  Control: one voice in two roles, against round 19's "every role its own voice") and c (cast
  directly: a re-render if disliked). Stated default: `{ally}` in a naval convoy's lines becomes the
  ship's name, three takes per line.
- 2026-10-08: [Round 33](../../concept-rounds/round-33/README.md) candidates for Atlas Control (M5 part E, step E1a): a Alister (Sea-Power in the
  Pacific, Bywater; clip pitch 106 Hz) and b MaryAnn (Eighteen Months in the War Zone, Finzi;
  174 Hz), a male and a female voice, both new and Public Domain Mark 1.0; `atlas-control` added to
  the speaker table as `uncast: true` (no Level 11 data yet, so VoiceFilesTest is unchanged). Three
  of its lines rendered per candidate (`tools/concept/audio/tts_r33.py`, details in
  [concept/prompts.md](concept/prompts.md#voice-atlas-control)). Measured only, not listened to.
- 2026-10-09: M5 part E (step E4): Level 11's lines of the cast speakers rendered by
  `tools/art/voice.py`'s pipeline (only the missing lines, nothing deleted), 15 lines: Okafor 4
  (briefing page 1, `first-telegraph`, the two level-end lines), Varga 7 (briefing page 2, the
  t=21.5, 34, 70.5 and 146.5 lines, `boss-destroyed`, the sunken pod's secret) and Rook 4 (t=14, 55,
  158 and `boss-part-destroyed`); the Choir's `[the Choir sings]` plays its stage sound; the hangar
  teaser and the intel lines are not voiced, as in Levels 08 to 10. **Atlas Control's** five lines
  (t=1, t=62.5, t=110, `secondary-objective` and the `ally-hit` and `ally-lost` lines once per
  ship name) stay text until [round 33](../../concept-rounds/round-33/README.md) casts him: `VoiceLines` now expands `{ally}` in a naval
  convoy's lines to the three cargo ships' names (Halvorsen, Mbeki, Saint-Laurent), six takes in
  all, not number words (`NavalConvoyLinesTest` runs it with Atlas Control cast); `Atlas Control`
  is in VoiceFilesTest's `AUDITIONING` and Level 11 in its `RENDERED`; RadioTimelineTest covers
  Level 11 (seven runs, with and without Rook, all three difficulties): no voiced timed line
  starts more than a second late. Whisper (base.en) read each take back through the filter and
  librosa pyin measured its pitch: three takes re-rolled (four seeds each) and pinned in the speaker
  table: Rook's `boss-part-destroyed` line and t=55 line (the key's seeds jumped an octave, to about
  500 Hz, on their first words) and Okafor's end line "Atlas-Seven is through…" (the key's seed read
  "Good work, ages" without a name prompt; the pin still does, "Aegis" reads right with a prompt:
  a listening point). Other readings: "Vrell" reads "Vrel" and "ceded" for "seeded" in Varga's
  briefing page, "Tiamat" reads "Tiamat" in Okafor's; "Atlas-Seven" reads "Atlas, seven" or
  "Atlas 7". For the user's ear: the names Saint-Laurent, Halvorsen and Mbeki (Atlas Control's
  takes, after the casting), Tiamat (Okafor's briefing page), Vrell (Varga's), "Atlas-Seven" and
  "Aegis" (Okafor's level-end lines).
- 2026-10-09: Concept round 33 decided (user) for Atlas Control, **b**: MaryAnn (clip pitch 174
  Hz), at the neutral settings through radio filter b, as auditioned; her clip renamed
  `refs/ref-atlas-control.wav`, `uncast` replaced by the `ref` in the speaker table and `Atlas
  Control` taken out of VoiceFilesTest's `AUDITIONING`. Alister (a) rejected: his audition moved to
  `concept/rejected/`, his clip deleted with its CREDITS.md row. Her ten takes rendered by
  `tools/art/voice.py` (only the missing lines, nothing deleted): t=1 (6.7 s), t=62.5 (4.2 s),
  t=110 (4.6 s), the `ally-hit` and `ally-lost` lines once per ship name (3.1–4.2 s) and the
  `secondary-objective` thanks (3.7 s). Whisper (base.en) read every take back whole with a name
  prompt (without one: "Halbertson", "St. Laurent", "router" for Ruyter, "halls" for "hulls"); librosa
  pyin on the filtered files read five takes an octave high on single words, but the raw takes of
  the same seeds measured 140–255 Hz throughout (the radio filter's band limit and crackle fool the
  pitch tracker), so no pin was needed. For the user's ear: "hulls" (it may sound like "halls") and
  the three ship names. The cast speakers' 15 lines were accepted as rendered (round 33, user).
- 2026-10-09: M5 part F (a stated default of the part's gap check): Level 12 is voiced with the cast
  speakers only (Okafor, Varga, Rook, the Choir's stage sound): no new speaker, no audition. Its
  radio script is retimed to the 1 s rule (the eye's lines spread over 150.5–175.5 s, Rook's t=102
  dropped for his rear bark), see [Level 12](../../campaign/act-2-homefront/level-12-storm-front/README.md#radio-chatter).
- 2026-10-11: M5 part F (step 6): Level 12's lines of the cast speakers rendered by
  `tools/art/voice.py`'s pipeline (only the missing lines, nothing deleted), 18 lines: Okafor 5
  (briefing page 1, t=2, t=128, t=176 "Say again, Doctor?" and the level-end line), Varga 8
  (briefing page 2, t=30, t=58, t=86, t=157, t=182.5, the canister pickup and the secondary
  objective) and Rook 5 (t=14, `first-latch`, t=44, t=149.5, t=200); the Choir's t=169.5
  `[the Choir sings]` plays its stage sound; the hangar teaser and the intel lines are not voiced,
  as in Levels 08 to 11. No new speaker, so no audition and no `AUDITIONING` entry; Level 12 is in
  VoiceFilesTest's `RENDERED` and no longer in RadioTimelineTest's `UNVOICED`. Whisper (base.en,
  with a name prompt) read each take back and librosa pyin measured the raw take's pitch: six takes
  re-rolled (six seeds each) and pinned in the speaker table: Okafor's t=176 "Say again, Doctor?"
  (the key's seed opened with "Hmm," and said "Doctor" twice), Varga's t=30 "Eel-shaped contacts",
  t=58 sonde line and secondary line (the key's seeds jumped an octave, to about 450 Hz, on one or
  two words) and the canister line (the key's seed ended in seven mumbled "Mm"s), and Rook's
  `first-latch` line (the key's seed said "You got a passenger" and ran unvoiced, then high). Other
  readings, listening points pending the user's review in the combined round: Varga's "They're
  herding the weather" reads "hurting" in Whisper in every one of 24 seeds, so it is probably the
  voice's "herding" and was left at the key's seed; "sonde" reads "sondays", "signed" or "sound" in
  Whisper without the name prompt; "Vrell" reads right in Okafor's briefing page; a few takes end
  in a short breath or murmur after the last word that Whisper hears as a repeated word. Measured
  only, not listened to.
- 2026-10-11: M5 parts G and H (the user is on vacation; the main agent took the gap analyses' recommendations, **pending the user's review** in the combined round): the new speaker is Nansen (Level 13, a/b audition in round 35, G9 = a); Level 14 and the Act 2 outro add no speaker (H7 = a: four voiced pages, Okafor).
- 2026-10-12: M5 part G (step G6): Level 13's lines of the cast speakers rendered by
  `tools/art/voice.py`'s pipeline (only Level 13's missing lines, nothing deleted; the Act 2 outro's
  four pages left to part H), 15 lines: Okafor 8 (briefing page 1, t=1, 47.5, 154, 172, 233.5, the
  `ally-below` 25 % line, `mission-failed`), Varga 4 (briefing page 2, t=13, t=102, the hut's cache)
  and Rook 3 (t=24, t=58.5, `level-end`); the Choir's t=179 plays its stage sound. **Nansen** stays
  `uncast` (his t=36, t=162, t=218, `ally-below` 50 % and `secondary-objective` lines play as text, the
  generic CDF portrait) until round 35 picks a or b; `Nansen` is in VoiceFilesTest's `AUDITIONING` and
  Level 13 in its `RENDERED`; RadioTimelineTest no longer exempts Level 13 (`UNVOICED` is empty): with
  the real lengths no voiced timed line starts more than a second late (three difficulties, every fit,
  with and without Rook), so no line moved. Whisper (base.en) read every take back and librosa pyin
  measured its pitch: three takes re-rolled and pinned in the speaker table: Varga's t=13 "Fast boats in
  the leads…" (the key's seed sat about an octave up, 406 Hz median; ten seeds tried, the pin at 225 Hz
  is the closest to her 190–210 Hz and reads back word for word without a prompt), Rook's t=58.5
  "Contacts, all sides!…" (the key's seed about 430 Hz; the pin at 174 Hz, word for word) and Varga's
  hut line (the key's seed read "hot" and ran at 287 Hz; the pin reads "hut" both ways at 204 Hz).
  Other readings: "floes" reads "flows" (the same sound), "armoured" "armored", "Divers" once
  "Difers" without a prompt, "Atlas-Seven" "Atlas 7". For the user's ear: Varga's t=13 pin still has
  a few high syllables (30 % of its frames above 300 Hz). Measured only, not listened to. When round
  35 casts Nansen, render his lines and rerun RadioTimelineTest's voiced run (his t=36 line runs
  into Okafor's t=47.5 only if it is longer than about 10 s). The same day the main agent shortened
  both briefing pages (so they fit one screen beside their images): re-rendered (Okafor 22.3 s, Varga
  13.3 s, both read back word for word with the name prompt, at 154 and 207 Hz), the old files deleted.
- 2026-10-12: M5 part H (step H6; **pending the user's review** in the combined round): Level 14's and
  the Act 2 outro's lines rendered by `tools/art/voice.py`'s pipeline (only the missing lines, nothing
  deleted), 20 lines with the cast speakers, no new speaker and no audition: Okafor 11 (the outro's four
  pages, 14.3, 9.2, 10.6 and 8.8 s; briefing page 1, 24.6 s; t=1, t=62, the phase-3 line, the
  secondary objective, the level-end line and the armoured car's cache), Varga 6 (briefing page 2,
  14.9 s; t=31.5, t=54, t=102, the phase-2 line and `boss-destroyed`), Rook 2 (t=21 and his phase-3
  line) and Hammer Lead 1 (t=13); the Choir's t=96 plays its stage sound; Rook's hangar teaser and the
  intel lines are not voiced. Level 14 and `act-2-homefront outro` are in VoiceFilesTest's `RENDERED`;
  RadioTimelineTest covers Level 14 (the armoured car's cache early or late, the arena's phase lines
  and the Spire's death in a fast, a medium and a slow fight). The draft's opening did not fit the 1 s
  rule even as text (Okafor's t=1 line holds the radio about 12 s), so the radio is retimed in
  [Level 14's data](../../campaign/act-2-homefront/level-14-siege-spire/data.yaml): Hammer Lead
  16 → 13 (before Rook, still over Hammer flight's pass at t=16), Rook 7.5 → 21, Varga's canopy line
  22 → 31.5 (into the old town) and the Choir 98 → 96 (its text holds 5.6 s, so Varga's t=102 starts
  on time); with the voices no timed line starts late (three difficulties, every fit, with and without
  Rook). Whisper (base.en, with and without a name prompt, raw and through the filter) read every take
  back and librosa pyin (frames with a voicing probability over 0.5; Hammer Lead's low voice by yin)
  measured its pitch: no octave jump. Three takes re-rolled (six seeds each) and pinned in the speaker
  table: Hammer Lead's t=13 (the key's seed read "Hammer flat" through the filter), Varga's t=54 "Hive
  nodes in the Molard" (the key's seed read "five notes" every way; the pin reads "hive nodes" through
  the filter, raw still "five") and the outro's last page (the key's seed said "ships out from Mars").
  Other readings: "Vrell" reads "rail" or "rel" without a prompt, "Ares" "air is", "Root grid" "route
  grid" (the reader's vowel), "seventy-two" as digits. For the user's ear: Varga's t=102 "It's singing
  to the whole city" sits higher than her other lines (259 Hz median against her 190–210 Hz, no jump;
  most seeds tried read it 230–270 Hz, so it was left at the key's seed), and Hammer Lead's "flight".
  Measured only, not listened to.
- 2026-10-12 user, round 34 (13.4e, round 35's audition): Nansen is **a**, Patrick Eaton (male, 124 Hz);
  b (Sharon Riskedahl) rejected (her clip and audition go when the round closes). Step K1: the speaker
  table casts him (`ref: ref-nansen`, no `uncast`), his five Level 13 lines are rendered (portrait still
  `radio-generic-cdf`), VoiceFilesTest's `AUDITIONING` is empty. Checked by Whisper and the pitch of the
  raw takes (the radio filter fools the pitch tracker: through it every take read 115–420 Hz): two takes
  pinned, "Grid sync complete…" (the key's seed said "completes") and "Integrity holding above half…"
  (the key's seed jumped to about 400 Hz for half the line); every take now 130–140 Hz median. Whisper
  hears "Aides"/"ages" for "Aegis" without the name prompt, as with the other voices. RadioTimelineTest
  passes without retiming (the t=36 line runs 5.3 s, Okafor follows at t=47.5). Measured only, not
  listened to.
- 2026-10-12 (user, [round 34](../../concept-rounds/round-34/README.md)): the round closed. The 53 voiced
  lines of Levels 12–14 and the Act 2 outro accepted after the user's listen-through (I.4, P8 done);
  Nansen's b audition and her reference clip (`ref-nansen-r35-b.wav`, `.txt`) moved to
  `concept/rejected/`, the clip's CREDITS.md row with them.
- 2026-10-12 (overnight design phase, **pending the user's review in round 36**): Act 3's voices
  written as a draft (*Act 3 voices*): one new colony speaker, **Ares Control** (the drafts' Ares
  Warden merged into him, as the act README decides; a man, older than Rook, clip pitch about
  100–115 Hz; about 39 takes with the five domes' `{ally}` lines; cast a/b in part B's round), and
  **Lance Control**, the Orbital Lance's caller (one take; part A's round); Okafor's refrain as one
  file; pins planned for Varga's hint-1 line and Rook's quiet beat; the Choir's first spoken words
  rendered in fragments at its ellipses with a layering a/b so Varga's voice does not show through,
  at most about 6 s; a respelling list for the Mars names (Ares, Schiaparelli, Valles Marineris,
  Lycus, Coprates, Eos, Burroughs), applied to the text as spoken and part of the key.
- 2026-10-13 (user, round 36): the *Act 3 voices* section approved (D.30) with its proposals: Ares
  Control one character (Ares Warden merged), the respelling list (Q.24f), Lance Control as the
  orbital officer's call sign (Q.24g). S.6 = b: Ares Control cast as **Thomas A** (112 Hz); his
  audition's 2 s pauses inside two lines are noted for the render step; the respellings Airees,
  Skyaparelli and Burrows accepted; David Wales (a) rejected, his audition and reference clip moved to
  `concept/rejected/` with the clip's CREDITS.md row. C.1 = b: his radio portrait is the dust-suit
  controller on the teal channel. The document's `design` stays `draft` (only its Act 3 section was
  part of this round's approval).
- 2026-10-13: step Q2 (user, round 37, S.16 = b): Lance Control is Celine Major. Her clip renamed
  `refs/ref-lance-control.wav`, `lance-control` added to the speaker table, and the call ("Lance uplink
  locked. Firing!") rendered by `tools/art/voice.py` on the key's own seed (2.32 s, median 158 Hz like
  the clip): Whisper base.en reads "Lance uplink locked, firing." from the raw take and through the
  radio filter, so no pin or respelling was needed (the audition's take read "Plants up, link"). Her
  "Firing!" stays level rather than rising as in the audition — pending the user's review. The level
  screen now plays the call instead of showing it as text with the blip (`VoiceLines` lists the lance's
  call with the Airstrike's; `SpecialCallTest`).
- 2026-10-13 (user, round 37): round 37 closed (step Q3): S.16 = b (Celine Major) as built in step Q2;
  Ted Lienhart's audition (`voice-lance-control-r37-a.ogg`) and his reference clip and transcript
  (`ref-lance-control-r37-a.wav/.txt`, from `refs/`) moved to `concept/rejected/`, the clip's CREDITS.md
  row with them; `tools/concept/audio/tts_r37.py` now renders only the chosen b. B.5 (g), the Orbital
  Lance's call picked by the special's kind: yes.
- 2026-10-14: M7 part B, step VOX-B (**pending the user's review** in round 38): Level 15's 17 radio
  takes (Rook's t=23.5 line in both sides), its two briefing pages, the Act 3 intro's four pages and the
  Act 2 outro's page 3 (re-keyed by "Airees"; the old file deleted) rendered by `tools/art/voice.py`, 26
  files. The respelling list tried by Whisper's read-back (Okafor and Ares Control, three seeds each):
  "Vallis Marinairis" and "Coprateez" kept, Lycus and Eos spoken as written (see *Pronunciation*). Pins:
  Rook's t=23.5 left (the key's seed jumped high on "Lancer") and right (read "Copies"), Ares Control's
  t=84 (of six seeds the one nearest his pitch; every seed reads "Like a station" for Lycus) and the drop
  pod's line (the key's seed ran an octave high). Three takes were retried for a pause over 0.8 s. Level
  15's Varga line at t=33.5 moved to t=34.5: Rook's t=23.5 line held the radio a second too long as text
  (`RadioTimelineTest`).
- 2026-10-14 (user, round 38): accepted: Ares Control's cast (Thomas A, the trimmed reference clip) and his five
  lines, the "Lycus Station" and "last week" takes included (V.1); the respellings Airees, Skyaparelli,
  Burrows, Vallis Marinairis and Coprateez, Lycus and Eos plain (V.2); Level 15's radio (V.3); the Act 3
  intro's four pages and Level 15's two briefing pages (V.4); the Act 2 outro's page 3 re-keyed with
  "Airees" (V.5).
- 2026-10-18: M7 part C, step VOX-C (**pending the user's review** in round 39): Level 16's 17 voiced lines
  (Rook's t=11 line in both sides: 18 takes) and its two briefing pages rendered by `tools/art/voice.py`, 20
  files; Airees, Vallis Marinairis and Coprateez as approved, Eos and Helix as written (Noctis and Candor are
  in no voiced line). Pins: Okafor's t=1 (the key's seed read "Canyon Martha Head"), Varga's t=114 (read
  "Rell"; the pin reads "Vrell"), Ares Control's t=22 (a high jump on one word) and the relay hut's line (the
  key's seed ran high and read "If that's"). Two takes were retried for a pause over 0.8 s. Level 16's Choir
  moved t=162 → 166 and Varga's reply 169.5 → 173 (`RadioTimelineTest`: the secondary's closing line held
  the Choir back when gate 6 fell after t≈155).
- 2026-10-18 (user, round 39): accepted: Level 16's voiced lines and briefing pages, the four pinned seeds, Eos
  and Helix spoken plain (V.1), and the Choir's retiming to t=166 with Varga's reply at t=173 (V.2). R.4 (no beam
  at Level 16 on the plan): Varga's t=47 line rewritten as "Kill an anchor and the tendril drops. Ordinary shots
  pass straight through it." and re-rendered (`tools/art/voice.py`, the key's own seed, read back as written;
  `varga/e80e3305e17d.ogg`, the old `9f06f1dd1404.ogg` deleted); `VoiceFilesTest` and `RadioTimelineTest` pass.
- 2026-10-18: M7 part D, step VOX-D (**pending the user's review** in round 40): Level 17's 28 voiced lines (the
  `ally-hit` and `ally-lost` lines once per dome name: 10 takes) and its two briefing pages rendered by
  `tools/art/voice.py`, 30 files; Airees, Skyaparelli and Burrows as approved, Bradbury, Lowell, Eden and Helix
  as written. "Ares" read back as "Ares"/"Aries" (the respelling) in every pinned take; the key's seed of Ares
  Control's t=1 line read "air is landing" and is pinned. Pins (17): Ares Control 13 (takes an octave high or
  jumping high, and read-backs "air is landing", "Bradbury has hit", "Below", "bribery", and "The sky
  apparently", "sparrows" through the filter),
  Varga 2 (octave jumps), Okafor 1 (t=88.5, pitch), Rook 1 (t=14 read "Copy errors" through the filter).
  Pitch per speaker on the unfiltered takes: Okafor about 140-160 Hz, Varga 190-240, Rook 140-195, Ares
  Control 100-130. One take retried for a pause over 0.8 s (Okafor's level end). No radio retime: the timed
  lines never overlap (the closest, t=80.5 → 88.5, leave 3.6 s). Built in the step's snapshot only, to hand to
  the content and game owners: `VoiceLines.radio` expands `{ally}` by `level.groundAllies()`' names, and
  `LevelScreen.allyName` (now `RadioEvents.allyName`) names a ground ally as a naval one (`sortie.groundAllies()`).
- 2026-10-19: step VOX2 (**pending the user's review**): the round-40 Act 2 rework's new lines rendered with
  the cast and respellings unchanged — Level 11 Okafor on `first-ship-telegraph` ("The arms are rooted at the
  rim. Cut a root and that lane is safe!"; one seed retried for a 1.5 s pause), Level 14 Rook at t=110
  ("Missiles on the roots, I'll keep the flyers off you.") and Varga's trimmed briefing page 2 (seed pinned:
  the key's seed jumped to about 450 Hz on the closing "you"; the pin is the steadiest of six). Rook's calamari
  line kept its text, so its take stays; the old page 2 take (`varga/0e226d3a4ff2.ogg`) is deleted. Pitch on
  the unfiltered takes: Okafor 143 Hz median (a rise to about 250 Hz on "Cut a"), Rook 151, Varga 194;
  Whisper reads every line back as written ("Vrel" for Vrell).
- 2026-10-19: M7 part E, step VOX-E (accepted by the user in round 41): Level 18's 17 voiced lines (Rook's
  four `requires: escort`) and its two briefing pages rendered by `tools/art/voice.py`, 19 files; Airees as
  approved ("Ares Landing" read back as "Aries Landing" plain and "Ares Landing" with the name prompt, as in Level
  17), "Vrell" as "Vrel"/"rail" plain and "Vrell" with the prompt. Pins (6): Okafor's briefing page 1 (the key's
  seed read "Sweet pick" for "Sweep it" both ways) and t=78 "Later, Doctor." (the key's seed sat at about 128 Hz);
  Varga's t=41 and t=56 (octave jumps on "pull" and "it") and her briefing page 2 (every one of 15 seeds jumps
  high on "not shapes"; the pin holds it about 0.1 s); Varga's t=71 hint line pinned to the key's own seed as the
  voice design asks (median about 210 Hz, no jump, a 0.18 s beat before "ours", rising to about 270 Hz at the
  end; reads "ours", not "hours"; seed 204667219 was the runner-up with a 0.24 s beat). Two kept takes were
  retried for a pause over 0.8 s (Rook's t=86, Okafor's level end). Pitch on the unfiltered takes: Okafor 146-180 Hz median (180 on the secondary's line), Varga 193-235, Rook
  139-160. Whisper hears "Militia supply pod" (the secret) as "Malicious supply pod" in all seven seeds tried,
  with "militia" in the prompt too; the take is kept, to be heard in the round. No radio retime: the timed lines
  never overlap (the closest, t=71 → 78 and t=86 → 93, leave about 3.5 s).
- 2026-10-19: M7 part F, step VOX-F (**pending the user's review** in round 42): Level 19's 24 voiced lines (Rook's
  seven `requires: escort`; Okafor's t=148.5 "Not on this channel, Doctor." has Level 17's key and take) and its two
  briefing pages rendered by `tools/art/voice.py`, 25 new files; and Level 17's secondary line reworded for the "No dome
  lost" secondary (domes may be hit, none may break; the user's decision 2026-10-19): "Five domes, not a crack."
  became "All five domes still standing." (Ares Control, the rest of the line kept), rendered on its key's seed
  (about 130 Hz, no jump); the old take and its pin are gone. "Tharsis" is spoken plain and read back right in every
  take; "Vrell" reads "Vrel" (the key's seed of Okafor's t=1 read "The rail", pinned), "Aegis" right except one
  level-end seed ("ages", pinned), "Helix" right. Pins (9): Okafor's t=1, t=88 ("Grade drones" both ways) and level
  end; Varga's flame tell (an octave jump); Rook's t=45.5 (barely voiced), t=77 (an octave jump on a quarter of
  it) and t=116 (a squeal at about 570 Hz); Ares Control's t=105 (high on 30 % of it) and t=155.5 ("We're coming
  up"). Pitch on the unfiltered takes: Okafor 142-168 Hz median, Varga 181-209, Rook 125-181, Ares Control
  110-130. Whisper hears Varga's "Grey tanks" (t=30) as "Great tanks" in all five seeds tried and Okafor's page 1
  "crew are sealed" as "crew were sealed"; both takes kept, to be heard in the round. No radio retime: the timed
  lines never overlap (the closest, t=38 → 45.5, leaves about 3 s).
- 2026-10-19: The last radio message was cut short a few times (the user's playtest): the outro
  handed over to the debrief at a fixed 15 s and stopped the voice, when a Rook bark or a secondary
  judged at the level's end held the closing lines back. Fixed with the outro mode (see Playback).
  **Pending the user's review**: the debrief waiting for a closing line (the user had asked for
  the levels to be extended; Levels 10 and 13 were extended by 1 s and 4 s) and the 3 s overrun limit.
- 2026-10-20: round 42 closed: Level 19's voices (V.a-V.i) and Level 17's reworded line (V.e) accepted by the user; the radio outro fix (T.3, R.g) and the post-win barks (R.h) accepted; Level 16's radio timing (R.k) accepted.
- 2026-10-20: M7 part G, step VOX-G (**pending the user's review**): Level 20's 22 voiced lines (Rook's four `requires:
  escort` lines and his t=113 quiet beat, which plays without him) and Rook's three "Up top!" barks (`top_lines`, fierce,
  shouted) rendered by `tools/art/voice.py`, 25 new files. "Hellas" is spoken plain (as on the Act 3 briefing's
  page 2): Whisper hears HEL-us as "Heller's", "Helis" or "Hela's"; a respelled "Hellus" (three seeds on each of
  the two short lines) read back no better, so no respelling. "Vrell" reads "Vrel", "Aegis" mostly "ages" (as on
  earlier levels). Pins (4): Okafor's t=1 ("Ella's basin, ages"; the pin reads "Heller's basin, Aegis"), Rook's
  t=98 (unvoiced), Ares Control's t=131 ("Aegis" swallowed) and the secret (a jump to about 570 Hz, "the rail").
  Pitch on the unfiltered takes: Okafor 131-175 Hz median, Varga 185-213, Rook 140-162 on the radio lines and
  216-301 on the shouted barks (inside the earlier shouted barks' 179-508), Ares Control 106-137. No radio retime:
  the timed lines never overlap (the closest, t=20 → 27.5, leaves about 3.4 s).
- 2026-10-20: M7 part H, step VOX-H (**pending the user's review**): Level 21's 21 voiced lines and the Act 3 outro's
  four pages rendered by `tools/art/voice.py`, 25 new files. The Choir's first words are one take in Varga's
  reference voice with the Choir's fixed settings, the round-18 layering (option a) and the `distorted` filter
  (4.1 s with the reverb tail, inside the 6 s slot); the fragments are not spoken separately (no drone joins yet).
  Its key's seed and five of six more said "we are going" (or "we are many") twice; the pin speaks each fragment
  once (2.6 s raw). Whisper cannot read the layered file (expected), the raw take reads "Many, we are many, we are
  going." Pins (7): the Choir's line; Okafor's t=53 ("Base and floor" in nearly every seed: Whisper's reading
  of "Basin" at a sentence start; the pin reads "Basin floor" through the filter) and outro page 1 (every take of
  three seeds held a 1.1-2.6 s pause; the pin's sixth take has none); Varga's t=37 (an octave jump) and briefing
  page 2 ("lime, brings"); Rook's t=13 left take (barely voiced); Ares Control's t=21 (a jump to about 440 Hz).
  "Hellas" reads "Hellas Basin" on Okafor's briefing page; "Vrell" mostly "Vrell" with the name prompt ("Vrellon"
  for "Vrell on" at t=1), "Aegis" right, "Ares" right with the prompt ("Airy's"/"Aries" without), "Europa" right,
  "Colossus" right; Okafor's outro page 4 reads "Thera, Deep" (a small break inside the name) and "Conamara" right
  with the prompt. Varga's outro page 3 speaks the quoted "we are going" plainly. Pitch on the unfiltered takes:
  Okafor 135-161 Hz median, Varga 188-210, Rook 132-182, Ares Control 116-133. No radio retime: the timed lines
  never overlap (the closest, t=13 → 21, leaves about 5 s).
