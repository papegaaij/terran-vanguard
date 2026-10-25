# Sound effects — decisions archive

The Decisions entries of [README.md](README.md#decisions) dated before 2026-10-21 (M8's start), moved here in order and unchanged; newer ones stay in the README.

- 2026-09-30: Priorities P1–P3 guide production order; player fire is deliberately quiet.
- 2026-09-30: Concept round 01: all three pickups chosen, one per item type — A standard power-ups, B rare upgrades, C credits.
- 2026-09-30: Concept round 01: all synthesized player shots and explosions rejected. Explosions must be somewhat realistic; source recorded sound effects from free online libraries instead.
- 2026-09-30: Concept round 02: recorded shots and explosions are much better; all ten kept as a starting set. Needed next: a distinct shot sound per weapon type (see [weapons](../../player/weapons/README.md)) and more explosions covering the full range from small pops to large booms.
- 2026-09-30: Concept round 03: rejected huge-a, medium-a, tiny-c, underwater-b and both beam loops (B barely audible); all other r03 shots and explosions chosen. The `beam` family needs new sources (round 04).
- 2026-09-30: Concept round 04: all new sounds chosen (beam loops, start/stop, huge and under-water explosions, sonar A); sonar B rejected — A is enough.
- 2026-10-01: Concept round 08: remaining Acts 1–2 sounds proposed — 40 recorded (CC0/CC-BY, band-levelled; five sub-bass-only candidates rejected) and 14 synthesized pickups and UI blips. Overdrive pickup maps to pickup-r01-a, data core to pickup-r01-b, salvage medium to pickup-r01-c. Still open: equip/upgrade and grade-stamp UI sounds, a fourth Vrell screech, an Airstrike radio call (text + squelch for now, no voice).
- 2026-10-01: Confirmed by the user: pickups and UI sounds stay synthesized; everything else (shots, explosions, hits, enemies, specials, radio, klaxon, ambience) uses recorded CC0/CC-BY sounds.
- 2026-10-01: Round 08 verified (peak, 200 Hz–5 kHz band level, DC, edges, loop seams, spectrograms) and fixed: bell and shimmer tails in four synthesized sounds (armour patch, special charge, shield cell, debrief total) ended in a click — the bell now has a release and the armour patch is longer; ship destroyed is high-passed at 20 Hz (sub-sonic drift left a DC offset); shield hit a is low-passed at 7 kHz (83 % of its energy was above 5 kHz); crumble b got a new source (the first was almost all sub-bass and stayed ~8 dB quieter than a on its audible band, even high-passed). Added the missing Acts 1–2 sounds: heavy enemy shot b, Vrell screech d, two Vrell spawn sounds, and synthesized equip, upgrade and grade-stamp UI sounds. Rejected on licence grounds: Artninja's "morphing burst" (built from Warner Bros and Zapsplat library sounds). `sfx.py` now writes the rejected round-01 shots and explosions to `concept/rejected/`.
- 2026-10-01: Concept round 08: all round-08 sounds chosen except screech a and b, shield hit b, shield restore b and klaxon b (rejected).
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-01: M1: the spike's `SfxBank` is carried over (64 OpenAL sources, played only from the render thread because libGDX's source pool is not thread-safe) and now enforces the per-sound instance limits by stopping the oldest instance. Levels relative to player damage: explosions −2 dB, hits −8 dB (the mixing rules give no level for hits), player fire −14 dB; ±5 % pitch (±4 % explosions), two-variant sounds alternate, pan up to ±40 % by play-field x. Voice stealing by priority is not done yet. Copies of the used files are in `assets/sfx/` (rows in CREDITS.md).
- 2026-10-01: Enemy-hit level added to the mixing rules (user decision): −6 dB on this scale, 8 dB below player damage, as the M1 SFX player uses.
- 2026-10-02: M2 imports the Level 01 sounds into `assets/sfx` (`:pipeline:importPlaceholders`,
  CREDITS.md rows for the third-party ones): small enemy shots, the small explosion family for
  Needlers and containers, metal hits for ground objects, the salvage, shield cell and armour patch
  pickups (salvage large for the hidden crate), radio squelch and typing, the debrief tick, total
  and stamp, and the Earth-orbit ambience loop.
- 2026-10-02: The launch rail sound goes into the next concept round (user decision); listed as a
  pending item in the Player ship table, no file yet.
- 2026-10-02: Concept round 11 proposed: two synthesized launch-rail sounds and two edge-warning
  tones (`tools/concept/audio/sfx_r11.py`). The edge warning tone is a new P1 row: the user
  found the side and rear warnings easy to miss after playing Level 01.
- 2026-10-02: Concept round 11 (user choices): launch rail **b** "catapult" and edge-warning tone **b** "contact ping" chosen; a "mag-lev" and a "triple chirp" moved to `concept/rejected/`. The launch-rail item is no longer pending.
- 2026-10-02: M3 part B1: the launch rail (b) plays at the start of every attempt's 5 s launch in
  Level 01, so its buffer clunk lands at 3.6 s; the edge-warning tone (b) plays when an edge warning
  starts, at the player-damage level, panned towards the warned edge, with an instance limit of 1
  so overlapping warnings never stack. Both are synthesized (no CREDITS.md rows) and imported by
  `:pipeline:importPlaceholders`. The launch rail plays at full level (the file is levelled like the
  interface sounds); no level is given for it beyond that.
- 2026-10-02: Production audio (Level 01 batch, part P2): the 75 chosen recorded sounds are rebuilt
  from the Freesound originals (cached by `tools/concept/audio/freesound_fetch.py`) with their
  unchanged `import_sfx.py` settings by `tools/art/sfx_originals.py` and written to `assets/sfx/`
  with a `SOURCE` comment; the concept files stay as the record of the choice. One step is added
  before the treatment: 13 originals decode above full scale (lossy originals and float WAVs, up
  to +18.7 dBFS for "Machine Gun 001") and are clipped at full scale first, as 16-bit playback
  clips them; levelled on the unclipped peak, the Autocannon Pod shot came out 11 dB quieter
  than the chosen sound and "explosion_big_02" 1.7 dB. Checked against the chosen files: same
  lengths, and peak and 200 Hz–5 kHz band RMS within 1 dB, except the peak of
  `enemy-shot-small-r08-b`, 1.7 dB lower at the same band RMS (it is levelled on its band, so only
  the crest of its attack differs from the preview's; the audible level is the same).
- 2026-10-02: Concept round 12 closed (user decision): the 75 recorded sounds rebuilt from the Freesound originals approved as **final** (the 1.7 dB lower peak of `enemy-shot-small-r08-b` accepted); this doc's `art` stays `chosen`, since the synthesized sounds were not part of the round.
- 2026-10-02: Concept round 16: the Leviathan's whale-song cry proposed as a synthesized sound
  (`tools/concept/audio/sfx_r16.py`, written to `concept/` like the other synthesized sounds,
  which `importPlaceholders` copies). The 2026-10-01 decision has enemy sounds recorded; this one
  is synthesized for the M4 part C batch, so a deep alien call with its own glide and formants
  exists without a source search. The user decides in round 16 whether it stays or a recorded
  CC0/CC-BY call replaces it.
- 2026-10-03: Concept round 16 closed (user decision): the synthesized Leviathan whale-song cry (`enemy-leviathan-cry-r16-a`) approved as **final**. The user accepts this synthesized cry, so its placeholder note against the 2026-10-01 rule (enemy sounds are recorded CC0/CC-BY) is resolved: no recorded replacement is sought. This doc's `art` stays `chosen`.
- 2026-10-04: Round 21 opened with the sounds of Level 05's props (user decision D8 of M4 part E): recorded a/b pairs for the sled's whine and pass and the Polyp Mortar's lob and impact.
- 2026-10-04: Round 21 closed for the sounds (user): mortar lob a, mortar impact a, sled whine a
  (its 0.78 s loop played twice over the 1.5 s chase) and sled pass b chosen and played by the game
  (`Sfx.MORTAR_LOB`, `MORTAR_IMPACT`, `SLED_WHINE`, `SLED_PASS`, in the `:pipeline` copy list),
  replacing the placeholders; the four others moved to `concept/rejected/`, their CREDITS.md rows
  kept with the new paths, as for the earlier rounds' rejected recordings.
- 2026-10-04: Round 23 opened with Level 06's new sounds (user decision D8 of M4 part F): recorded
  a/b pairs for the Mantis's telegraph and beam sweep, the flare launch and burn loop and the
  Coilwyrm's head regrowth. The data core keeps its round-08 mapping to the chosen `pickup-r01-b`
  and the Smart Bomb its chosen `special-smartbomb-r08-a`, so neither is in the round.
- 2026-10-05: Round 23 decided (user): Mantis telegraph **a** and sweep **b** (CC-BY), flare
  launch **a** (CC-BY) and burn **a**, Coilwyrm regrowth **b** chosen; the other five moved to
  `concept/rejected/` (their CREDITS.md rows follow). Wired into the game as round 21's sounds
  were: copied into `assets/sfx/` under their concept names by `:pipeline:importPlaceholders`
  (`copyPlaceholderSounds`), played by `FlightSounds` through new `Sfx` entries: the telegraph at
  the warnings' level, the sweep at the hits' level (a little above enemy fire), the launch with
  the burn's 3 s loop played back to back while the flare burns (8 s, 12 s on easy; the last play
  at half level as the pool fades), the regrowth at the explosions' level. The two CC-BY picks
  reach the credits screen through their CREDITS.md rows (the screen's list is generated from
  them, skipping rejected files). Rebuilding them (and round 21's) from the Freesound originals
  with `tools/art/sfx_originals.py` needs `freesound_fetch.py --download` first: the originals
  are not cached.
- 2026-10-05: Production audio for rounds 21 and 23, now that the originals are cached: the nine
  chosen recorded sounds (sled whine a, sled pass b, mortar lob a and impact a; Mantis telegraph a
  and sweep b, flare launch a and burn a, Coilwyrm regrowth b) rebuilt from the Freesound
  originals with their unchanged `import_sfx.py` settings by `tools/art/sfx_originals.py` into
  `assets/sfx/` under their concept names, with a `SOURCE` comment and CREDITS.md rows for the
  `assets/` files; `copyPlaceholderSounds` keeps them, as round 12's. The script builds every
  sound whose Concept art row is `chosen` and stops at the first one whose original is not cached,
  which was the stop above; the rows stay `chosen` (the concept files record the choice, the
  `SOURCE` comment marks the final file). Only the sled pass's original decodes above full scale
  (82 samples, +0.01 dBFS) and is clipped first. Checked against the chosen files: same lengths
  (the whine's 0.78 s and the burn's 3.00 s loops still seamless), peaks within 0.3 dB, 200 Hz–5
  kHz band RMS within 0.5 dB and 50 ms envelopes within 0.7 dB, except the regrowth: held at its
  −6 dBFS ceiling by the original's 0.6 dB higher crest, it is 0.8 dB lower on its band and 0.9
  dB on average over its envelope (within the 1 dB tolerance). This doc's `art` stays `chosen` (the synthesized sounds are not final).
- 2026-10-05: Round 24 closed (user): the Coilwyrm's head burst **c** (CC-BY) and segment burst
  **b**; a and d and the round's previews moved to `concept/rejected/` (their CREDITS.md rows
  follow). Rebuilt from the Freesound originals by `tools/art/sfx_originals.py`, which now also
  takes sounds with a treatment of their own (`DERIVED`: `sfx_r24.py`'s `PRODUCTION`, the cut,
  the loudest-100-ms levelling and the head's slowed thump), into `assets/sfx/` under their
  concept names; within 0.4 dB of the concept files on peak and band. The chained death plays one
  burst every 0.25 s at the explosion level (they no longer overlap), each pitched by the member's
  width down the taper; `enemy-coilwyrm-death-final-r24-a` previews it.
- 2026-10-05: Round 25 opened (M4 part G, Level 07): recorded a/b pairs for the Brood Carrier's
  roar (its arrival and its turn), a bay sac opening, closing and bursting, a unit launched from a
  sac, the plate iris opening and the lifeboat tow's cable snap (`tools/concept/audio/sfx_r25.py`,
  the first round cut from the Freesound originals, so the chosen files need no rebuild). Option a
  plays in Level 07 for now, through new `Sfx` entries and `FlightSounds`: the roar 0.75 s after
  the klaxon starts and again at 0.85 pitch as the turn begins; one sac-opening sound for the sacs
  that open together, a quieter closing as they shut; up to three launch spits 0.1 s apart after
  an opening (an opening's units all leave in one step); a sac shot off bursts with the generic
  part explosion under it, and the death chain bursts every sac with it in its tail-to-head order;
  the iris as the core is exposed; the cable snap as the pod falls free (the cable's hits keep the
  metal hit). The carrier plays no generic phase blast. All at the explosions' level. The klaxon
  keeps the chosen, already final `ui-klaxon-r08-a`. Not listened to: picked by description,
  rating, envelope, band balance and level.
- 2026-10-05: Round 25 closed (user): carrier roar **b**, sac opening **b**, sac closing **b**,
  launch **b**, sac burst **a** (CC-BY 4.0, SilverIllusionist), iris **b** and cable snap **a**
  (CC-BY 3.0, CosmicEmbers); the other variants moved to `concept/rejected/` (their CREDITS.md
  rows follow). The chosen files were already cut from the Freesound originals; the production
  files are written by `tools/art/sfx_originals.py`, which takes round 25's chosen treatment as
  `DERIVED` (`sfx_r25.py`'s `PRODUCTION`: the cut, the slowdown or reversal, the loudest-100-ms
  levelling), into `assets/sfx/` under their concept names with a `SOURCE` comment; `Sfx` and
  `copyPlaceholderSounds` play the chosen ones, the provisional option-a files that are no longer
  used were removed from `assets/sfx/`. The two CC-BY sounds join the credits screen's list.
- 2026-10-05: M4 part H, the SFX pass. **Voice limit**: at most 32 effects at once (`VoiceLimit`
  in `SfxBank`); a play past an effect's own instance limit stops its oldest instance, a play past
  32 steals the oldest voice of the lowest priority at or below its own or is dropped. Libgdx does
  not report a sound's end, so a voice counts for its file's length (read from the Ogg file's last
  page, divided by the pitch). Priorities as in the mixing rules; the groups they do not name were
  placed by the main-agent brief and this pass: the interface with the warnings, the specials with
  the boss sounds, hazards with enemy fire, hits with player fire; loops are never stolen.
  **Wired** (chosen sounds already in `assets/sfx/`): enemies explode on their size rung (`medium`
  for the Brood Pod, Mantis, Scuttler and Spore Bomber; two files a rung, alternated); a set
  piece's part blows with the `medium` rung, a boss's phase end with the `large` one; a set piece's
  death with the `huge` rung (an act boss, and a set piece that is no boss, the Leviathan) or the
  `large` one (a mid-boss, the Gorgon Frigate); an act boss's tail-to-head chain starts on the
  `large` rung, bursts on the `medium` one and ends on the `huge` blast with the `large` rung's boss
  blast (r02-e) 0.05 s later; the Leviathan's break-up layers the `large` and `medium` rungs instead
  of slowed small explosions. A destroyed ground target bursts with the small ladder's r02-b or a and
  crumbles under it: collapse a for a target of at least 1 000 px², rubble b below. The Smart Bomb
  plays its chosen energy blast with the `huge` rung's sub-heavy boom under it at −3 dB (the blast
  swells over 0.5 s; the boom gives the instant flash its punch). The shield's restore chime plays
  once the shield is full again after a break (not after every hit's regeneration). The low-armour
  beep repeats every 1.2 s at or below 30 % armour and every 0.6 s at or below 15 %, while the ship
  flies, 6 dB under the warnings' level, since armour does not regenerate and the beeping can last
  the rest of the level. Pickups by type: salvage medium r01-c, the special charge's own chime, the
  data core r01-b, the overdrive pickup r01-a with the overdrive's start cue. Heavy enemy shots and
  the hangar's shop sounds have their `Sfx` entries but are not played yet (see Implementation).
  The nine synthesized sounds are copied by `copyPlaceholderSounds`. Not played, with the reason:
  the table *Chosen sounds the game does not play*. Gaps for concept round 26: a save-done sound, the
  Coilwyrm's chain-cut tear, the Vrell screech's cue. Not listened to: levels set by rule.
- 2026-10-05: Concept round 26 closed (user: the round accepted as proposed): the SFX pass accepted
  as built: the 32-voice limit with priority stealing and the newly wired chosen sounds (the shop,
  the pickups by type, the explosions by size, the ground targets' crumble, the Smart Bomb, the
  shield's restore chime, the low-armour beeps, the heavy enemy shot), at the levels set by rule.
  The row's two questions had no proposal and were not answered: the Vrell screech's cue and
  whether to make concepts for a save-done sound and the Coilwyrm's chain-cut tear stay open
  questions, tagged `later: M5`. Every item is ticked or tagged later, so the document is `done`;
  `art` stays `chosen`, since the synthesized sounds are still concept copies and the save-done and
  chain-cut sounds have none.
- 2026-10-06: Round 26's open questions decided (user): the **Vrell screech** (c and d, in turn)
  plays as a large Vrell unit enters the screen, the Mantis, the Coilwyrm's head, the Spore Bomber
  and the Scuttler, throttled to one every **3 s** so a wave does not stack them; no simulation event
  marks an enemy's entry, so the game watches each step for a unit of those kinds whose hit box
  first overlaps the play field (by its serial, so it screeches once). It ranks with enemy fire.
  Concepts for the **save-done sound** and the **Coilwyrm's chain-cut tear** go into [concept
  round 27](../../concept-rounds/round-27/README.md) (two synthesized, two recorded options; a of
  each wired until the choice: the save at the hangar's autosave and a slot save, the tear at the
  cut).
- 2026-10-06: Concept round 27 closed (user): save done **b**, the calm chime (latch click, two warm
  bells over a pad; a, the rising data ticks on a bright fifth, rejected); the Coilwyrm's chain-cut
  tear **a**, "rip_tear FLESH!!!!.wav" by aust_paul (CC0; b, "Tearing Flesh" by dereklieu, CC-BY,
  rejected, so it leaves the credits roll); the Vrell screech approved as built. The save sound
  ships as a concept copy like the other synthesized UI blips, the tear rebuilt from its Freesound
  original by `tools/art/sfx_originals.py`. The game now plays no proposed sound.
- 2026-10-06: M5 plan (user decision D1 of part A): the Act 2 weapon sounds are retagged by part:
  the Hornet, the rear guns and the mines' drop and arm with part A, the torpedo with part E
  (Level 11).
- 2026-10-06: M5 part A built the Act 2 weapon sounds from the chosen families: the Hornet's
  `missile` (shot-missile-r03-a) and the mines' drop (shot-mine-r03-a) are played now, so they
  leave the not-played list; rear guns keep the 10 % lower pitch, the mine's drop does not (it is
  not a gun). The arming beep the `mine` family names has no chosen sound: a gap for round 28.
- 2026-10-06: M5 part A, concept round 28: the proximity mine's arming beep gets two synthesized
  proposals (`tools/concept/audio/sfx_r28.py`, round-08 UI family, peak −12 dBFS): a, a rising
  two-blip "armed chirp", and b, a single gliding "sensor ping". Main-agent brief: short (≤ 0.15 s),
  dry and quiet, since a mount drops up to two mines a second and keeps up to six. The simulation
  now marks the step a mine arms (`PROXIMITY_MINE_ARMED`, at the mine; it allocates nothing and the
  replay hashes are unchanged, events are not hashed); option a plays there provisionally
  (`Sfx.MINE_ARM`, two instances, −10 dB, between the hits' −8 and the shots' −14), so the game plays
  one proposed sound until the round closes.
- 2026-10-06: Concept round 28 closed (user): the mine's arming beep **a**, the armed chirp (two
  rising pulse blips a fifth apart); b, the gliding sensor ping, rejected and moved to
  `concept/rejected/` (`sfx_r28.py`'s `CHOSEN` writes it there). The game already played a, as a
  concept copy like the other synthesized sounds (`copyPlaceholderSounds`, `Sfx.MINE_ARM`); it
  again plays no proposed sound. This doc's `art` stays `chosen` (the synthesized sounds are not
  final).
- 2026-10-06: M5 part B (gap defaults with the user's decisions D1–D6): the **megacity ambience**
  (round 08's a, used as it is) plays under Level 08 by the key `earth-megacity`, a new column of
  the *Ambience* table that names each setting's key for a level's `music.ambience`; the
  **Creeper** screeches on entry like the Scuttler; it needs no other new sound (the enemy fan
  shot, the `medium` explosion). The billboard's topple plays the destructible break or the
  `medium` explosion unless its concept pair brings a sound. The document goes back to
  `in-progress` for part B.
- 2026-10-06: M5 part B: the game plays the megacity ambience by the key `earth-megacity`
  (`Sfx.AMBIENCE_CITY`; its row left the *Chosen sounds the game does not play* table) and the
  Creeper screeches on entry (`ScreechCue`).
- 2026-10-07: M5 part C (user decision D9 = a and the stated defaults): Level 09 needs two new
  sounds, the **Ravager's pounce** and the **arcology's collapse** (a rumble under the shadow
  swelling into a long crash), each an a/b in concept round 31 (recorded CC0 / CC-BY sources, each
  in CREDITS.md); the **Hive Node** reuses the Brood Carrier's iris b and the Vrell spawn a, and
  dies on the `large` rung; the Ravager dies on the `small` rung. Our reading, for review: neither
  unit screeches on entry (the node's iris and the pounce are their cues; a pack would screech over
  itself).
- 2026-10-07: M5 part C, game side: the Hive Node plays the Brood Carrier's iris b as its iris opens
  and the Vrell spawn a (so it leaves the "not played" list) as it releases; the Ravager's take-off
  and the arcology's collapse are wired to their own slots with placeholders (carrier launch b, a
  slime lunge; hit-crumble a, "building collapse", from the fall) until round 31's picks are in
  `assets/sfx/`, when the collapse sound moves to the shadow's start
  (`FlightSounds.COLLAPSE_AT_WARNING`).
- 2026-10-07: Round 31 closed for the sounds: collapse a (CC-BY 4.0, YleArkisto, on the credits
  roll), both pounces kept and picked at random per pounce; rebuilt from the originals into
  `assets/sfx/` by `tools/art/sfx_originals.py` with `sfx_r31.py`'s `PRODUCTION`; the collapse
  plays from the shadow (crash at 1.5 s; to move to the impact when the fall is reworked). Collapse
  b (a steel-frame cave-in) moved to `concept/rejected/`. The Hive Node's reuse of the carrier iris
  b and the Vrell spawn a, and no screech for either unit, confirmed (round 31 row 8).
- 2026-10-07: The collapse's crash moved to the impact (round 31's look c built: the 1.5 s lean,
  then the 1.5 s drop): `sfx_r31.py`'s `PRODUCTION` now mixes the crash at its `IMPACT` (3.0 s)
  over a rumble cut 1.5 s longer and swelling for those 3.0 s, rebuilt by
  `tools/art/sfx_originals.py` into `assets/sfx/collapse-r31-a.ogg` (7.0 s; the same two
  originals, so the credits are unchanged; its check flags the length against the 5.5 s concept
  file, which stays as the record of the choice). `FlightSounds.COLLAPSE_CRASH_SECONDS` is 3.0.
- 2026-10-08: M5 part D (stated defaults with the user's decisions of 2026-10-08): three new sounds
  for Level 10, each an a/b in round 32 (CC0 or CC-BY only, recorded in CREDITS.md): the Wraith's
  decloak, the Mote Swarm's whoosh (on its entry and its loop-back) and the lance's strike; the
  shuttles reuse the ally hit and loss sounds. Round 08's ocean loop is brought forward from part E
  as `earth-ocean`, crossfading in over 4 s from the megacity at Level 10's section 4.
- 2026-10-08: M5 part D, game side: Level 10's three new sounds play from the simulation with
  existing chosen sounds as placeholders until round 32's picks reach `assets/sfx/` (as part C did):
  the decloak the Mantis's telegraph a (a short laser charge-up), the swarm the sled's pass b (a
  rushing flyby), the lance the large crumble a, chosen because its crash lies 1.2 s in, where round
  32's lance files put their impact, so the timing is already the final one. Our readings, for
  review: the decloak and the swarm at the Vrell spawns' level, the lance at the player-damage
  level; a swarm is heard once as its first member comes onto the screen, its later members silent.
  The ocean loop is played (it leaves the "not played" list).
- 2026-10-08: Round 32 closed for the sounds (user picks): the Wraith's decloak **b** (a metallic
  sci-fi rip over a membrane swish; its cloak layer CC-BY 4.0, F.M.Audio, on the credits roll), the
  Mote Swarm **b** (an insect chitter over a whoosh) and the lance **a** (an alien falling whine into
  a thunder crack, its impact 1.2 s in); rebuilt from the originals into `assets/sfx/` by
  `tools/art/sfx_originals.py` with `sfx_r32.py`'s `PRODUCTION` and played by `Sfx.WRAITH_DECLOAK`,
  `MOTE_SWARM` and `LANCE_STRIKE` in place of the placeholders (the lance still started at t 116.8;
  the swarm still heard once at its entry by `SwarmCue` and on each loop-back). Decloak a, swarm a
  and lance b moved to `concept/rejected/`.
- 2026-10-08: M5 part E (user decisions of 2026-10-08; sounds are not an E-question, so one
  proposal each unless the main agent asks for an a/b): Level 11 plays the chosen torpedo launch and
  water explosion (round 03), the ocean ambience from t=0, and new sounds for the Kraken (churn, slam,
  surfacing, death), the ships (hit, sinking) and the frigate's distant flak, CC0 or CC-BY only. In a
  water level every kill on the play field plays the `water` explosion over its size rung and
  landing blasts play it instead of their ground blast (a stated default: their impacts splash).
- 2026-10-08: concept [round 33](../../concept-rounds/round-33/README.md) proposes the Level 11 sounds (one each, all CC0 recordings plus synthesis):
  the Kraken's churn, slam (impact 0.5 s in), surfacing swell and death, the ships' hit and
  sinking, the frigate's distant flak and the Driftjelly's pulse
  (`tools/concept/audio/sfx_r33.py`); awaiting the user's pick, then `tools/art/sfx_originals.py`
  gets a `PRODUCTION` for the chosen ones and the game wires them.
- 2026-10-09: Round 33 closed for the sounds (user): all eight accepted, the slam, surfacing, ship
  hit and jelly pulse as first made; the churn (now a surge of waves over a rumble), the death and the
  sinking reworked without bubbles, the flak raised twice (to −16 dB, low-passed at 4 kHz) after
  listening, then accepted. Seven rebuilt from the originals into `assets/sfx/` by
  `tools/art/sfx_originals.py` with `sfx_r33.py`'s `PRODUCTION` (identical to the concept files by its
  check), under the concept names, so `Sfx` keeps its paths; their `copyPlaceholderSounds` entries
  dropped; the Driftjelly's pulse, fully synthesized, stays copied from its concept file.
- 2026-10-09: M5 part F (user decisions F6 = a and F8 = a of 2026-10-09): Level 12's storm bed is a
  new rain-only cut of the chosen round-08 source ("Rain and Thunder 4", FlatHill, CC0), the chosen
  file kept, and thunder plays as one-shots after each lightning flash (rejected: b, thunder on top
  of the bed's own, muddy; c, flashes timed to the bed's thunder, coupling the schedule to an audio
  loop). New sounds for round 34: thunder, gust, the Lamprey's latch and drain loop a/b, the
  shake-off and the sonde chime one each; CC0 or CC-BY only.
- 2026-10-11: M5 part F step 6 (pending the user's review in the combined round 34): the game plays
  round 34's **a** sounds as placeholders until the round closes. The thunder: the three variants in
  turn, each 0.3–1.5 s after its flash (spread by the golden ratio over the flash index, so a replay
  sounds alike), panned 0.2 to the bolt's side, at the explosions' level; the gust as its telegraph
  starts, panned 0.3 to the upwind side (the upwind cue) at −4 dB; the latch's bite at the player
  damage level; the drain loop at −6 dB while a Lamprey drains in flight (stopped with the latch, the
  ship's loss, the level's end and the screen); the shake-off at −2 dB; the sonde chime at −4 dB. The
  rain-only bed is `earth-storm`; the ocean ambience crossfades in for the eye and back (the level's
  `ambience_changes`).
- 2026-10-11: M5 parts G and H (the user is on vacation; the main agent took the gap analyses' recommendations, **pending the user's review** in the combined round): Level 13's sounds are the relay hit, the integrity alarm, relay destroyed, the drone launch and grid online, with the arctic ambience (`AMBIENCE_ARCTIC`, +3 dB in the whiteout peak); Level 14's are the canopy hum, the root burst, the growth, the tear-free roar and the canopy withering; the rest reuse existing sounds. Auditions in rounds 35 and 36 (a/b where the rows say so).
- 2026-10-11: M5 part F, after the round 34 capture (the user is on vacation; **pending the user's
  review in the combined round**): the latch, the shake-off and the gust could not be heard in the
  storm's mix, though their files are as loud as the shield hit's (about −15 LUFS). The gust (from
  −4 dB) and the shake-off (from −2 dB) now play at the warnings' level with the latch, the edge
  warning and the shield hit; a latch and a shake-off also duck the theme by 6 dB for 1 s (as Level
  10's scripted loss does), since the latch was at that level already. The drain loop (−6 dB) and
  the chime (−4 dB) stay.
- 2026-10-12: M5 part G, step G6 (**pending the user's review in the combined round**): Level 13's sounds
  wired as round 35's a until the round closes. Levels over the files' own (sfx_r35.py levelled them
  against the set): the relay hit with the explosions (−2 dB; it plays often), the integrity alarm,
  grid online and relay destroyed at the warnings' level (0 dB), the drone launch with the explosions
  (−2 dB, not a quiet pickup cue: the round 34 capture could not hear the quiet one-shots). The alarm
  plays once on each `ALLY_BELOW` (50 % and 25 %), grid online on `DEFEND_COMPLETE` (t=218), the drone
  launch 4 s before each drop as its arc starts (only while the relay stands), relay destroyed on its
  `ALLY_LOST`. The arctic wind rises 3 dB through the whiteout by the peak's `ambience_db`. On the ice
  only a burst on a naval unit (the Skimmer) plays the water explosion (G5 = a); every other burst on
  Level 13 plays the ground's.
- 2026-10-12: M5 part H, step H6 (the user is on vacation; **pending the user's review** in round 36):
  Level 14 plays round 36's variant a (the root burst, the tear-free roar, the canopy withering) and the
  one proposals (the root growth, the canopy hum) from `assets/sfx/` under their concept names, at the
  files' own levels; the canopy hum replaces the megacity's sirens as the level's ambience and hands
  back to them at the Spire's death (out over 2 s, the city in over 7 s); the withering starts 0.5 s
  after the death, with the canopy's first tear frame. The maw opening reuses the Brood Carrier's sac
  open (the user's round 25 pick, played at 0.85 pitch) as the spec says; no bubbly or popping layer
  was added.
- 2026-10-12: M5 part G, step G8 (**pending the user's review in the combined round**): the Lamprey's
  shake-off (round 34's placeholder) measured about 6 dB under Level 12's busy mix and was marginal at one
  of its two shake-offs, though it already plays at the warnings' level (0 dB, the game's ceiling). The
  file itself is now 3.5 dB louder (`sfx_r34.py`: −13.5 dB instead of −17, the same −2 dBFS ceiling;
  −12.1 LUFS instead of −16.1); the latch, the gust and the theme's 6 dB duck stay as they were.
- 2026-10-12: M5 part H, step H7b (**pending the user's review**): the canopy withering starts 3 s
  after the Siege Spire's death, at the end of its death chain (`FlightSounds.WITHERING_DELAY_SECONDS`
  0.5 → 3.0): at 0.5 s, with the tear's first frame, the chain's bursts masked it in the H7 capture.
- 2026-10-12: M5 part I, after step CF (**pending the user's review**): a Proximity Mine's drop plays at
  the arming beep's −10 dB instead of the shots' −14 dB (`FlightSounds.MINE_DROPPING`): in the CF capture
  the front gun firing on the same tick masked the drop completely (10–20 dB under the mix); the arming
  beep was heard. Not re-captured.
- 2026-10-12 user, round 34 (Levels 12–14's sounds, rounds 34–36's proposals): the thunder **b** (three
  close strikes, loganzsound CC0), the gust **b** (the recorded gust, cobratronik CC0), grid online **b**
  (the spin-up into a contact clunk, Glitchedtones CC0), the tear-free roar **b** (the creature's bellow
  with the lift) and the canopy withering **b** (the dying exhale, groan and sigh); every other sound of
  the three rounds **a** or accepted as its one proposal. Rejected: thunder, gust, grid online, roar and
  withering a; the latch, drain, relay hit, integrity alarm and root burst b (their files move to
  `concept/rejected/` when the round closes). Step K1 wired the b files (`Sfx`, `copyPlaceholderSounds`;
  the a copies left `assets/sfx/`); measured as G8 did (the loudest 100 ms of the 200 Hz–5 kHz band),
  each b is within 1 dB of the a it replaces (the third thunder 2 dB over), so the levels stay; grid
  online's b is 2.6 s like a, so Level 13's jingle fade stays 2.6 s. None has a bubble, squelch or water
  layer (the roar's wet tearing is the user's pick). The picked sounds are the concept files, already
  built from the Freesound originals or synthesized, without a `SOURCE` mark (`PlaceholderAuditTest`
  lists them).
- 2026-10-12 (user, [round 34](../../concept-rounds/round-34/README.md)): the round closed. The one-proposal
  sounds accepted (12.4e the shake-off, the sonde chime and the rain-only bed; 13.4d the drone launch and
  relay destroyed; 14.4d the root growth and the canopy hum); the picks as in the line above. The
  rejected files (thunder 1–3, gust, grid online, roar and withering a; the latch, drain, relay hit,
  integrity alarm and root burst b) moved to `concept/rejected/`, their CREDITS.md rows with them.
  Pending: the picked sounds still lack a `SOURCE` mark (`PlaceholderAuditTest` lists them).
- 2026-10-12 (overnight design phase, **pending the user's review in round 36**): Act 3's sounds
  listed with briefs (*Act 3 sounds*): terrain and set pieces (the canyon wall scrape, the dome's hit
  and break, the furnace vents, the basin rumble), the new units and the two bosses, the Act 3
  equipment (the Ion Beam with its impact, the EMP Burst, the Orbital Lance, the Shield Overcharge,
  the Deflector Pod, the drones, the dash), and five Mars ambiences; reuses named where a chosen file
  fits (the frigate's flak, the swarm's whoosh, the enemy missile launch for the walker's pods, the
  lock beep for the lance's targeting line, the tesla shot for the EMP). Round 36 proposes a/b for the
  burrow eruption, the canyon wall scrape, the Ion Beam loop and the dust-storm bed; the rest go to
  the round of the part that builds them. No bubbly, popping or squelching sounds (user rule). A
  unit's loop plays once per kind, for the nearest unit (proposal). The unplayed-sound reasons of the
  beam, tesla, missile and lock files name their Act 3 use.
- 2026-10-13 (user, round 36): the *Act 3 sounds* section approved (D.29). Picks: burrow eruption
  **a** "sand burst", canyon wall scrape **b** "metal screech" (with Halleck's CC-BY scrape: on the
  credits roll), Ion Beam loop **a** "the chosen hum, heavier", dust-storm bed **a** "recorded front"
  (`mars-storm`; dobroide's CC-BY howl on the credits roll). The rejected files (burrow eruption b,
  scrape a, ion loop b, storm bed b) moved to `concept/rejected/`, CREDITS.md updated.
- 2026-10-13: M7 part A, step A6: the Act 3 arsenal's sounds wired in flight with round 37's a as
  placeholders (copied into `assets/sfx/` by `importPlaceholders`) until the round closes; the Ion
  Beam loop a, the lock beep and loop c left the *Chosen sounds the game does not play* table. Our
  reading, pending the user's review: the beam's start plays with its loop (not before it), the
  sizzle's −38 dB file plays as it is under the loop, the EMP's power-down plays once a step for what
  the ring reaches (a machine's at the explosions' level, a Vrell unit's 6 dB lower), a drone's
  absorbed hit plays the metal hit 12 dB under the hits.
- 2026-10-13: step Q2: round 37's sound picks in the game (user: S.2, S.4, S.6–S.10 **b**, the rest
  **a**): the seven b files replace their a copies in `assets/sfx/` (`Sfx`, the `importPlaceholders`
  list, `PlaceholderAuditTest`, `ArsenalSoundsTest`), played at the a's levels. Measured: no clicks or
  loop-seam jumps; the EMP ring b sits about 5 LU and the overcharge whoosh b about 5.5 LU under their
  a (the ring has no headroom at −2.7 dBFS peak, the whoosh 4 dB at −7 dBFS), so both are quieter than
  the Smart Bomb and the shield restore in flight — pending the user's review.
- 2026-10-13 (user, round 37): round 37 closed (step Q3): the picks S.1–S.15 as built in step Q2
  (S.2, S.4, S.6–S.10 **b**, the rest **a**); the fifteen unchosen files moved to `concept/rejected/`
  (`sfx_r37.py` routes them by `CHOSEN`, CREDITS.md paths updated). B.6 (a)–(d) yes: the beam's start
  plays with its loop, the sizzle at its file's −38 dB, the EMP power-down once a step (a Vrell unit
  6 dB lower), a drone's absorbed hit 12 dB under the hits.
- 2026-10-14 (M7 part B, step AUD-B, **pending the user's review in round 38**): round 38's sounds
  proposed (`sfx_r38.py`): the bulge a (the eruption a alone, started at the bulge: its built-in swell is
  the tell, so a separate rumble would double it) / b (a crack layer over it), the dive, the entry roar
  and `mars-dust` a/b. The chosen eruption a and each a are copied into `assets/sfx/` until the pick
  (`PlaceholderAuditTest` lists them); the game plays the eruption at the bulge's start, not at the
  surfacing.
- 2026-10-14: M7 part C (step AUD-C): the chosen canyon wall scrape b ([canyon-scrape-r36-b](concept/canyon-scrape-r36-b.ogg),
  Halleck, CC-BY, already in CREDITS.md and now on the credits roll) is copied into `assets/sfx/`; round 39's
  proposals (`sfx_r39.py`, all synthesized, option a each): the tendril hum, burn loop and break, the Threadcrawler's
  walk and the `mars-canyon` ambience, copied into `assets/sfx/` until the pick, pending the user's review.
- 2026-10-14 (user, round 38): closed (step R38C): S.1 = **b**, the crack layer `burrow-bulge-r38-b` played over
  the eruption a, both from the bulge's start at the same pitch (`Sfx.BURROW_BULGE`, `FlightSounds`); S.2 =
  **b**, the grain slide; S.3 = **a**, the recorded entry roar; S.4 = **a**, the recorded dust wind. The game
  ships the four chosen files (`copyPlaceholderSounds`, `PlaceholderAuditTest`); the dive a, the entry roar b
  and `mars-dust` b moved to `concept/rejected/` (`sfx_r38.py` routes them by `CHOSEN`, CREDITS.md paths
  updated). B.3 (d)–(e) yes: `mars-entry` crossfading over 2 s to `mars-dust` at section 3, the eruption from
  the bulge's start.
- 2026-10-18: M7 part D (step AUD-D): round 40's proposals (`sfx_r40.py`, all synthesized, option a each, no
  third-party layers and none bubbly or popping): the dome hit and break, the Choir Herald's song, aura loop,
  absorb ping and collapse chord, the Shellback's walk, lob, curl, roll loop and uncurl, and the `mars-colony`
  ambience; copied into `assets/sfx/` until the pick (`PlaceholderAuditTest` lists them), pending the user's review.
- 2026-10-18 (user, round 39): closed: the tendril hum (S.1), burn (S.2) and break (S.3), the Threadcrawler's walk
  (S.4) and the `mars-canyon` ambience (S.5) accepted as proposed (one variant each, all synthesized), so all five
  `chosen`; the game already ships them. B.4 (b), (d), (e) kept: the scrape at −2 dB panned at the contact, the
  unit loops and the tendril hum's 0.4 while dormant with 0.2 s fades, the crawler's cut as the Coilwyrm's rip at
  pitch 0.8, −3 dB.
- 2026-10-19 (user, round 40): the dome hit, the Herald's song, aura, absorb and collapse, the Shellback's walk, lob, curl, roll and uncurl and the `mars-colony` ambience accepted (S.a, S.c-S.l, `chosen`); the dome break (S.b) is a redo: "It needs a rumble. Look in the online database for cave in or collapse sounds." (step AUD-S).
- 2026-10-19: M7 part E (step AUD-E): round 41's proposals (`sfx_r41.py`, all synthesized, option a each, nothing for
  CREDITS.md, none bubbly or popping): the Dust Devil's spin loop, opening and grit spiral and the Ghost Drone's drop,
  glint, burst and death; not in `assets/sfx/` until E4/EG1 imports them, pending the user's review.
- 2026-10-19 (user, round 41 S.h): the dome break is **c**, the low-passed building collapse over the crust rumble with
  smashing glass (chosen; the recommendation: its longer rumble under the glass is the cave-in asked for); b (the shorter
  sub thump) and round 40's synthesized a moved to `concept/rejected/`. The game plays `dome-break-r40-c.ogg` (step FXS).
- 2026-10-19: M7 part F (step AUD-F): round 42's proposals (`sfx_r42.py`, all synthesized, option a each, nothing for
  CREDITS.md, none bubbly or popping, every blast with a ramped attack): the Revenant Walker's stomp, pod hatch, pod
  missile (an alternative to the planned enemy missile launch r08 a), flamer tell and flame loop, the furnace vents' tell
  and burst, the Warden Tank's tracks loop, turret slew, burst and death, a conveyor belt loop (the plan folds the belts
  into the bed; offered all the same) and the `mars-foundry` ambience; not in `assets/sfx/` until the game step's import
  copies them, pending the user's review.
- 2026-10-20 (user, round 43, S.a-S.n): the Freesound redo and additions, picks: hatch **b**, flamer tell **a**, flame
  **b**, vent tell **a**, vent burst **a**, tank tracks **a**, tank turret **a**, tank burst **a**, walker vent hit
  **a**, walker death **a**, wreck settle **a**, `basin-rumble` **a** (Level 20's second ambience layer), `swarm-hum`
  **a**, `swarm-lunge` **a** (all `chosen`). The other variants (hatch a, flamer tell b, flame a, vent tell b, vent
  burst b, tracks b, turret b, burst b, vent hit b, death b, settle b, rumble b, hum b, lunge b) and round 42's
  synthesized hatch, flamer tell, flame, vent tell, vent burst, tracks, turret and burst, plus its pod missile (round
  08 a is used), moved to `concept/rejected/`; the r42 stomp, tank death, belt loop and `mars-foundry` stay chosen.
  CREDITS.md paths updated.
- 2026-10-20: M7 part H (step AUD-H, round 44's S rows): eighteen proposals from the Freesound database (`sfx_r44.py`, CREDITS.md +54 lines, never synthesized): the Dust Colossus's bulge, eruption, suction loop, bite, death and ridge pass and the Revenant Walker's lance charge, lance shot and stomp shockwave, a and b each. **Provisional picks until the user listens** (step HB; nothing is approved): the game plays **a** for all of them except the ridge pass, which plays **b** (a pans left to right, the ridge travels right to left); the walker's brace tell stays a placeholder. No separate sound for a mouth's death (the death's own sound covers it; round 44, S.j asks). Review points: the bites are slowed bone crunches (crackle check), the loudness against the bed, the 3 s death chain under the 6.1 s sound.
- 2026-10-20 (user, round 44, closed; S.a-S.j): **Colossus** bulge **a**, eruption **a**, suction loop **a**, bite **a**, death **a**, ridge pass **a** (a subway train passing under a bridge, sand flowing off the ridge; the concept file keeps its left-to-right pan and the game plays a render with the channels swapped, right to left, for Level 21's ridge, written by `tools/concept/audio/sfx_r44.py --mirror colossus-ridge-r44-a`); **walker** lance charge **b** (a high-voltage discharge's buzz with Tesla sparks), lance shot **a** (a real railgun), shockwave **a**; S.j: **no mouth's death sound** (the death's own sound covers it). The picks are `chosen`; the nine other r44 files moved to `concept/rejected/` (bulge b, eruption b, suction b, bite b, death b, ridge b, lance charge a, lance shot b, shockwave b; CREDITS.md rows point there). The CC-BY items among the picks go on the in-game credits screen. All part-H sounds are decided, so `art` is `final` for this part.
