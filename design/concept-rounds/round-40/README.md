---
title: Concept round 40 — M7 part D (Level 17), the Level 16 canyon redo, the Act 2 playtest rework
design: approved
implementation: n/a
art: chosen
depends-on: [../../campaign/act-3-red-dust/level-17-dome-siege, ../../campaign/act-3-red-dust/level-16-valles-canyon-run, ../../enemies/bosses/harbour-kraken, ../../allies/ares-landing-dome, ../../enemies/air/choir-herald, ../../enemies/ground/shellback, ../../audio/music, ../../audio/sfx, ../../audio/voice, ../../tech/roadmap, ../round-39]
updated: 2026-10-19
---

# Concept round 40 — M7 part D (Level 17), the Level 16 canyon redo, the Act 2 playtest rework

**Closed 2026-10-19** (user, on the phone review page): the Act 2 rows **accepted** (P.a-P.g, the A.a Kraken r40 art
approved with its six build choices, P.e "yes, all": Levels 11-14 get the new waves rule now, 15-17 later); Level 17's
final art (F.a-F.g), sounds (except S.b), voices (V.a), the changes and build choices (C.a, B.a-B.f) **approved or
accepted**; the Level 16 canyon walls **final** (W.a-W.d); the debrief music **b "Stand Down"** (M.a); **S.b** the dome
break is **redone** ("It needs a rumble", cave-in sounds from Freesound); **K.a** the capture came back with a note and
part D's findings are decided and queued (R.a = a, R.b re-measure, R.c, R.d, R.e, R.f keep). Every row is answered in
its *Outcome* cell.

## Summary

Four things wait for an answer:

1. **M7 part D, Level 17 Dome Siege** (built on the user's behalf, [roadmap](../../tech/roadmap/README.md#m7-parts)): the
   final art, 12 sounds, 30 voices, the game capture and the build choices (`C`, `F`, `S`, `V`, `K`, `B`, `R` rows).
   **R.a is the open decision**: the Shellback's vent cannot be shot from below, so the dome secondary is met 0 / 8 on medium and hard.
2. **The Level 16 canyon redo** (`W` rows, round 39 R.1): the user said "this looks much better indeed" in chat; the rows ask
   for the final verdict, the redo's defaults and the two items left from round 39's close.
3. **The Act 2 playtest rework** (`P` and `A` rows): what was built after the user's playtest of Levels 08-14 (the tower fix, Level 12's
   end, Levels 08/09's waves, Ravagers and HP, the new pacing tests), the **Harbour Kraken's new art** (`A.a`, four rooted arms, everything
   124 px up), the Kraken/Spire simulation built but **not yet in the levels' data** (step CB), the refactor and build-speed changes.
   `P.e` asks whether Levels 11-17 get the new waves rule too.
4. **Debrief music**, track 29: "Homeward" or "Stand Down" (`M.a`).

Not done yet (a record in the last section, nothing to answer): the Level 08 cars under the viaducts and the Level 13 icebergs (FX2), the
Level 11 and 14 data (CB), the build-time step BT2b.

**Part D in short.** The review part of M7 part D *Level 17* ([roadmap](../../tech/roadmap/README.md#m7-parts)): the [Ares Landing domes](../../allies/ares-landing-dome/README.md) as ground allies scrolling with the ground (hits, cracks, the break, never failing the mission) with the new `untouched` secondary, its `DOMES` tracker and the debrief row; the target-the-objective hook without a defend primary (hooked Spore Bombers' bomb runs, the Shellbacks' alternate lobs, the roll's crush); the [Choir Herald](../../enemies/air/choir-herald/README.md)'s `aura` rings; the [Shellback](../../enemies/ground/shellback/README.md) (vent part, mortar, curl-roll, belly); the Dust Kite's `flock` (moved forward from part E); [Level 17](../../campaign/act-3-red-dust/level-17-dome-siege/README.md)'s data, Ares Landing backdrop with the Helix compound, its props, sounds and voices.

Open [index.html](index.html) in a browser (regenerate with
`python3 tools/concept/board.py 40 --subjects ares-dome-final,choir-herald-final,shellback-final,backdrop-final,l17-props-final,intel-final,briefing-images-final,dome-hit,dome-break,herald-song,herald-aura,aura-absorb,aura-collapse,shellback-walk,shellback-lob,shellback-curl,shellback-roll,shellback-uncurl,mars-colony,level-17-capture,canyon-redo,canyon-redo-compare,harbour-kraken,debrief-theme,debrief-join`;
`backdrop-final` r40 is Level 17's, Level 16's wall redo is `canyon-redo`). Rows are in this order: changes to approved specs, final art,
art picks, music, sounds, voices, captures, the Level 16 redo, the Act 2 rework, build choices and findings, what is not done yet.
Each row carries a recommendation; **B** rows are *yes* (keep as built) or *no* (say what).

## In the game

`./gradlew :desktop:run --args="..."` with `--settings <file>` (a debug run writes no save):
- Level 17 with the balance plan's ship: `--start level --level 17 --loadout front=pulse-cannon:4,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:2,generator=2,shield=1,armor=2 --special airstrike:2 --escort rook:autocannon:1`
  (`--special emp-burst:2` to see the rings stripped);
- Level 16 (the redo): the same ship with `--level 16`, `--special orbital-lance:2` to burn the tendrils.

## Changes to approved specs

| # | Change | Approved | Built (where) | Answer | Outcome |
|---|---|---|---|---|---|
| C.a | **Level 17's radio retimed** | Level 17's radio table: t=45 (Ares Control, "Bradbury and Lowell grow our food…"), t=55.5 (Varga, "That flyer is singing shields…"), t=71.5 (Ares Control, "Outer greenhouses are gone…"), t=80.5 (Varga, "…not touched a single Helix facility…") | t=**47**, **57.5**, **71**, **78** (step D5, a data edit; the voices are keyed by text, unchanged). `RadioTimelineLevel17Test` found the text holding the radio too long: Varga's two-page t=36.5 bomber line opened t=45 2.0 s late, her Helix line opened Okafor's t=88.5 cut-off 2.7 s late, and with Rook his t=95 and the t=102 inner-ring line 2.5 / 2.3 s late. Okafor's, Rook's and the inner ring's times stay (pinned by `Level17Test`). Varga's Helix line now comes 3.5 s before the compound enters the screen (t ≈ 81.5) | accept / revert (say which line moves instead) | accepted (2026-10-19) |

## Art — final

Production art from round 36's picks (`tools/art/`, step ART-D), in the game now.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.a | [Ares Landing dome](../../allies/ares-landing-dome/README.md#concept-art) | **final art** (`tools/art/siege_units.py`, from `mars_r36.dome_a`, MA.3 = a): 120 px and Eden 144 px, breathing loop, cracks 1–3, flicker, white flash, cave-in, dust plume, HUD pip | [sheet](../../allies/ares-landing-dome/concept/ares-dome-final-r40-a.png), [GIF](../../allies/ares-landing-dome/concept/ares-dome-final-r40-a.gif) | the greenhouse annexes on the apron stay when a dome breaks | approve as final / redo | approved as final (2026-10-19) |
| F.b | [Choir Herald](../../enemies/air/choir-herald/README.md) | **final art** (`siege_units.py`, the chosen seraph b): wing beat, crest bloom, segmented ring 0–6, absorb flash, thread, aura circle | [sheet](../../enemies/air/concept/choir-herald-final-r40-a.png), [GIF](../../enemies/air/concept/choir-herald-final-r40-a.gif) | the wing beat is faint seen from above | approve as final / redo | approved as final (2026-10-19) |
| F.c | [Shellback](../../enemies/ground/shellback/README.md) | **final art** (`siege_units.py`, the chosen r05 a): 16 headings × 6 walk, curl, roll, vent swell and burst vent, belly glow, husk | [sheet](../../enemies/ground/concept/shellback-final-r40-a.png), [GIF](../../enemies/ground/concept/shellback-final-r40-a.gif) | the dead vent is an overlay on the back | approve as final / redo | approved as final (2026-10-19) |
| F.d | [Level 17 backdrop](../../campaign/act-3-red-dust/level-17-dome-siege/README.md#concept-art) | **final art** (`tools/art/backdrop_l17.py`): rust plain and rail line with the stranded freight train, the outer domes' aprons and annexes with Vrell growth, burnt greenhouses, the Helix compound (black and gold, clean fence, from `mars_r36.helix_site`), rail yard, inner ring road with gun pits, spaceport apron | [sheet](../../campaign/act-3-red-dust/level-17-dome-siege/concept/backdrop-final-r40-a.png), [GIF](../../campaign/act-3-red-dust/level-17-dome-siege/concept/backdrop-final-r40-a.gif) | no deep layer, no Olympus horizon; the flak guns are gun-pit pieces; in the capture the tile-set changes are hard horizontal edges (t ≈ 75 burnt fields, t ≈ 143 spaceport) and the spaceport apron is mostly an empty grey grid | approve as final / redo | approved as final (2026-10-19) |
| F.e | [Level 17 props](../../campaign/act-3-red-dust/level-17-dome-siege/README.md#concept-art) | **final art** (`siege_units.py`): the collapsed greenhouse frame over the seed vault (whole, hit, badly hit, lifted), the capsule and its glow | [sheet](../../campaign/act-3-red-dust/level-17-dome-siege/concept/l17-props-final-r40-a.png), [GIF](../../campaign/act-3-red-dust/level-17-dome-siege/concept/l17-props-final-r40-a.gif) | the frame sprite is 64×48 against the data's 56×48 hit box; the renders only adequate | approve as final / redo | approved as final (2026-10-19) |
| F.f | [Intel pictures](../../ui/hangar/README.md#concept-art) | **final art** (`tools/art/intel.py`): the Choir Herald's and the Shellback's portraits | [sheet](../../ui/hangar/concept/intel-final-r40-a.png) | — | approve as final / redo | approved as final (2026-10-19) |
| F.g | [Briefing images](../../ui/briefing/README.md#concept-art) | **final art** (`tools/art/briefing_images.py`): `level-17-ares-domes` (the compound unlabelled) and `level-17-choir-herald` | [sheet](../../ui/briefing/concept/briefing-images-final-r40-a.png) | — | approve as final / redo | approved as final (2026-10-19) |

## Art — picks

The Harbour Kraken for the reworked Level 11 fight (the user's playtest: "move everything up the screen", the arms' attachments as targets). The
look is the approved Kraken ("The creature looks great"); the new files have their own names, the chosen/final Kraken stays until you pick.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| A.a | [Harbour Kraken, r40](../../enemies/bosses/harbour-kraken/README.md#concept-art) | **proposal a** (`tools/art/harbour_kraken.py --r40`, step ART-K): four rooted arms, each root a readable fleshy socket with states (intact, hit flash, raised with a glow tell, cut and oozing, sinking), shorter arms down each lane, the head and platform **124 px higher** (head centre y 170, mantle 70-270, beak 268). **Two layout changes for the data (step CB)**: the platform anchor is -46 (not the brief's 20), so the platform piece enters at t 163.749 (was 162.863), the boss halts at scroll +586 (was +462) and `hold_clear` is 676 (was 552); the root sockets sit at x 60 / 154 / 326 / 420 (not on the lane centres 60 / 180 / 300 / 420: lanes 2 and 3 there would cover the eyes). **Recommendation: approve a** | [sheet](../../enemies/bosses/harbour-kraken/concept/harbour-kraken-r40-a.png), [GIF](../../enemies/bosses/harbour-kraken/concept/harbour-kraken-r40-a.gif) | the deck is mostly off screen at the halt; ships and lane marks in the GIF are stand-ins; not yet in the level's data | approve a / redo / change the layout | approved: the r40 Kraken is chosen (2026-10-19); build choices (a)-(f) (A.aa-A.af) all yes; its `art` stays as it is until step CB puts r40 in Levels 11 and 14 |

**Build choices (row A.a)** (the Kraken's and the Siege Spire's simulation and looks, steps BS1 and KG; built, **not yet reachable** in the levels), each *yes* or *no*:
- **(a)** The aimed orb gets its own turn (`aim_turns`: every 2.0 s, glow 0.6 s); the Spire's pods aim down.
- **(b)** A raised arm's lane is worth twice the damage for its whole cycle (`raised: 2`).
- **(c)** Cutting an own-lane root turns its volley into two ship lanes; a cut root's ships are skipped.
- **(d)** The root's glow lasts the whole raised window; the glow and the Spire's crimson tint scale with the effect.
- **(e)** The new events reuse sounds: a root hit = the Polyp Mortar's impact, an aim tell = the Mantis telegraph (1.3 / 0.6).
- **(f)** A cut root pops along the arm's rest chain, and the under-water reach stretches to a cut socket.

## Music

Track 29, the new debrief loop, because the user noticed the debrief had no music after the 6 s jingle. Both proposals start at the jingle's last
chord (D major), are about -19 LUFS and loop; **not listened to by Claude** (analysis only).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| M.a | [Debrief music, track 29](../../audio/music/README.md#concept-art) | **pick a / b / redo** (`tools/concept/audio/music_r40.py`, step MUS-D): **a "Homeward"**, 63 BPM, slow string and choir pads, a soft harp, the human motif on a solo horn, no beat; **b "Stand Down"**, 84 BPM, laid-back electronic, pads on a soft kick, a glass lead hinting the motif. The jingle hands over at 5.714 s (the join files). **Recommendation: a** (the design asks for quiet, calm, a little proud; b is the livelier one) | [a](../../audio/music/concept/rejected/debrief-theme-r40-a.ogg), [b](../../audio/music/concept/debrief-theme-r40-b.ogg), join [a](../../audio/music/concept/rejected/debrief-join-r40-a.ogg), [b](../../audio/music/concept/debrief-join-r40-b.ogg) | a is dark above 6 kHz; b is soft between 2 and 6 kHz; the handover from the jingle is by the clock, not heard | a / b / redo | **b "Stand Down"** (2026-10-19); step MW wires it into the debrief and moves a "Homeward" to `concept/rejected/` |

## Sounds

Round 40's part D sounds ([sfx](../../audio/sfx/README.md#concept-art), `tools/concept/audio/sfx_r40.py`, all
synthesized, step AUD-D), **one variant each**, in the game now (wired in steps G1, G2). Not listened to by
Claude: checked by envelopes, spectrograms, levels and loop seams; no bubbly or popping layers.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.a | [Dome hit](../../audio/sfx/README.md#concept-art) | glass stress, no shatter, on each dome hit | [a](../../audio/sfx/concept/dome-hit-r40-a.ogg) | played at the explosion level | accept / redo | accepted (2026-10-19) |
| S.b | [Dome break](../../audio/sfx/README.md#concept-art) | the cave-in as a dome breaks | [a](../../audio/sfx/concept/dome-break-r40-a.ogg) | played at the explosion level | accept / redo | **redo** (2026-10-19): "It needs a rumble. Look in the online database for cave in or collapse sounds." Step AUD-S redoes the dome break from Freesound (CC0 or CC-BY only, recorded in CREDITS.md); the r40 a stays `proposed` until the redo is picked |
| S.c | [Herald song](../../audio/sfx/README.md#concept-art) | the song ring's call | [a](../../audio/sfx/concept/herald-song-r40-a.ogg) | formant saws: may read buzzy | accept / redo | accepted (2026-10-19) |
| S.d | [Herald aura](../../audio/sfx/README.md#concept-art) | loop while any ring is up, panned to the nearest | [a](../../audio/sfx/concept/herald-aura-r40-a.ogg) | — | accept / redo | accepted (2026-10-19) |
| S.e | [Aura absorb](../../audio/sfx/README.md#concept-art) | a ring taking a hit (at most one per 0.15 s) | [a](../../audio/sfx/concept/aura-absorb-r40-a.ogg) | — | accept / redo | accepted (2026-10-19) |
| S.f | [Aura collapse](../../audio/sfx/README.md#concept-art) | the rings collapsing at the Herald's death | [a](../../audio/sfx/concept/aura-collapse-r40-a.ogg) | — | accept / redo | accepted (2026-10-19) |
| S.g | [Shellback walk](../../audio/sfx/README.md#concept-art) | the heavy gait, loop, the nearest Shellback panned | [a](../../audio/sfx/concept/shellback-walk-r40-a.ogg) | 74 % of its energy below 200 Hz: small speakers may lose it | accept / redo | accepted (2026-10-19) |
| S.h | [Shellback lob](../../audio/sfx/README.md#concept-art) | the vent's mortar shot (the impact reuses the Polyp Mortar's) | [a](../../audio/sfx/concept/shellback-lob-r40-a.ogg) | — | accept / redo | accepted (2026-10-19) |
| S.i | [Shellback curl](../../audio/sfx/README.md#concept-art) | curling into the ball | [a](../../audio/sfx/concept/shellback-curl-r40-a.ogg) | — | accept / redo | accepted (2026-10-19) |
| S.j | [Shellback roll](../../audio/sfx/README.md#concept-art) | the roll (once per roll, the nearest) | [a](../../audio/sfx/concept/shellback-roll-r40-a.ogg) | not heard in the capture (no roll) | accept / redo | accepted (2026-10-19) |
| S.k | [Shellback uncurl](../../audio/sfx/README.md#concept-art) | uncurling | [a](../../audio/sfx/concept/shellback-uncurl-r40-a.ogg) | — | accept / redo | accepted (2026-10-19) |
| S.l | [Mars colony ambience](../../audio/sfx/README.md#concept-art) | `mars-colony`: plains wind, the domes' air-plant hum, distant rail clank, loop from t=0 | [a](../../audio/sfx/concept/mars-colony-r40-a.ogg) | — | accept / redo | accepted (2026-10-19) |

## Voices

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| V.a | [Level 17's radio and briefing](../../campaign/act-3-red-dust/level-17-dome-siege/README.md#radio-chatter) | **accept or redo** (step VOX-D, `tools/art/voice.py` through the radio filter, the pages dry): 30 files, the 15 timed lines, the `ally-hit` and `ally-lost` lines once per dome (Bradbury, Lowell, Schiaparelli, Burroughs, Eden: 10 takes), the secret's, the secondary's and the level end's lines and the two briefing pages; respelt Airees (Ares), Skyaparelli, Burrows; Bradbury, Lowell, Eden, Helix as written; 17 seeds pinned (Ares Control 13). In the game the dome lines name the dome (`VoiceLines`/`RadioEvents`) | the files under `assets/voice/` (listed in [voice](../../audio/voice/README.md)) | **listen**: Burroughs may read "Barrows"; Lowell "The lower" through the filter; the comma after Schiaparelli; the 0.6 s beat after "Five domes"; Rook's "Copy, Ares" | accept / redo (say which line) | accepted (2026-10-19) |

## Captures

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| K.a | [Level 17](../../campaign/act-3-red-dust/level-17-dome-siege/README.md#concept-art) | **game capture** (step D5, `concept/prompts.md`): a whole medium run with the plan's L17 ship and Rook, **no `--invulnerable`** (190 / 196 kills, armour untouched, the seed vault found, every dome hit once and none lost: DOMES UNTOUCHED 0 / 5), then a short hard run without firing (`--invulnerable`): hooked bombers over Bradbury, Lowell and Eden, the Shellbacks, Bradbury's cracks growing, Lowell broken, the Herald's rings, radius circle and threads, the Helix compound, the inner ring, the spaceport and its flak, the DOMES tracker (red label from the first hit, FAILED once Eden has passed), the debrief. The radio plays the times before C.a | [sheet](../../campaign/act-3-red-dust/level-17-dome-siege/concept/level-17-capture-r40-a.png), [video](../../campaign/act-3-red-dust/level-17-dome-siege/concept/level-17-capture-r40-a.mp4) | not seen: a Shellback's roll and belly, a vent shot (R.a), the EMP strip (no charge on the plan's ship); the first Herald dies about 2 s after it enters (R.d); Shellbacks walk across the domes' glass (R.c); the section seams and the empty spaceport (F.d) | accept / change | **change** (2026-10-19), the note verbatim: "The second shell creature rolls over a dome and again some time later. That shouldn't happen. The radio about the shield formation is too late, it is spoken after they are all destroyed. The shell creatures seem too strong. None are destroyed." Queued as part D fixes with R.a, R.c-R.e (Level 17's README, Implementation) |

## Level 16 canyon redo

Round 39 R.1 ("make it look more like a real canyon. Maybe you can take inspiration from pictures from the Grand Canyon from above") redone in
step CW; you said in chat "this looks much better indeed". Level 16 stays `art: chosen` until the verdict.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| W.a | [Level 16's canyon walls](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#concept-art) | **final art, the redo** (`CanyonProfile`, `WallProjection`, `tools/art/backdrop_l16.py`): stepped walls, side canyons, strata, light from the upper left, buttes. **Recommendation: approve as final** unless the weak spots below bother you | [capture](../../campaign/act-3-red-dust/level-16-valles-canyon-run/concept/canyon-redo-r40-a.png), [video](../../campaign/act-3-red-dust/level-16-valles-canyon-run/concept/canyon-redo-r40-a.mp4), [before / after](../../campaign/act-3-red-dust/level-16-valles-canyon-run/concept/canyon-redo-compare-r40-a.png) | in sections 2-4 the walls stand 40-70 px from the edge, so only the inner gorge and the first bench show and they still read as bands; the mesa shows mostly a pale cap strip, the pillar a rounded cap | approve as final / redo (say how) / tweak the defaults | approved as final (2026-10-19): the canyon walls are final |
| W.b | [The redo's defaults](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#walls-look-in-detail) | **yes or no, each** (step CW): (a)-(f) below | [W.a](#level-16-canyon-redo) | the points CW left open: setbacks, the buttes' scales, the shadow strength, the light, 768-row canyons | all yes / some no | all yes (2026-10-19) |
| W.c | [Varga's t=47 line](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#radio-chatter) | **wording** (round 39 R.4, you chose "change the plan or the hint"; Claude changed the hint): Varga now says "Kill an anchor and the tendril drops. Ordinary shots pass straight through it.", her intel line "Kill an anchor to drop a tendril. A cache down the left fork.", the recommended traits `forward` and `piercing`; the briefing's "a beam burns through the tendril" stays (true from the Lance and Ion Beam on). **Recommendation: accept** | [Varga t=47](../../../assets/voice/varga/e80e3305e17d.ogg) | re-voiced once; listen to how it reads | accept / reword (say how) | accepted (2026-10-19) |
| W.d | [The fork's right channel](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#layout) | **finding** (after round 39's C2, a fork wave flies the widest channel, so the left one first): the right channel gets only the mirrored t=66.5 kites and the turret ledge. **Recommendation: keep** (the left fork has the cache and the path; a thin right channel is a quiet choice) | [canyon capture](../../campaign/act-3-red-dust/level-16-valles-canyon-run/concept/canyon-redo-r40-a.mp4) | not flown on the plan's ship since | keep / add a wave that picks the right channel | keep (2026-10-19) |

**Build choices (row W.b)**, step CW, each *yes* or *no*:
- **(a)** Each wall descends from the rim in steps: three cliff bands (dark inner gorge, red-orange middle cliffs, pale cap cliff), each above a talus bench.
- **(b)** Every step's edge is set back toward the rock, never past the collision line (the foot), the more the more room there is to the field's edge.
- **(c)** Side canyons of three sizes make the rim ragged; strata run round them as contour bands; the plateau beyond is pale and flat, with gullies.
- **(d)** Light from the upper left: the left wall's cliffs in shadow with cast shadows on its benches and the floor, the right wall lit.
- **(e)** The mesa and the pillar are buttes (the same steps as rings round a flat cap) with cast shadows.
- **(f)** The rim keeps clear of the Helix station on the north plateau.

## Act 2 playtest rework

After the user's playtest of Levels 08-14 (2026-10-18): what was built; the **Answer** says what you decide. Not listened to or flown by the user yet.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| P.a | [Stacked buildings, Levels 08 and 09](../../campaign/act-2-homefront/level-08-neon-skyline/README.md) | **fix** (step FX1): the tower projection sorted once by height, so a tower could pass underneath one to its right; it now orders outermost-first per frame (no allocation) with tests. **Recommendation: accept** | — | seen only in tests, not flown | accept / change | accepted (2026-10-19) |
| P.b | [Level 12's end and Level 6's last line](../../campaign/act-2-homefront/level-12-storm-front/README.md) | **fix** (step FX1): the Breakout now ends at t=218 so the storm-clearing line plays before the debrief, the voices stop before the debrief, `RadioTimelineTest` checks every closing line against the outro (allowance 0.35 s for Level 6's Rook line, 0.3 s late). The quiet tail of Level 12 is 13-15 s (`PacingTest` at its limit). **Recommendation: accept; retime Level 6's last section 190 to 191** so the allowance can go; a light last wave for Level 12 only if its tail feels empty | — | Level 12's tail is quiet by the pacing test's own limit | accept / retime L06 / add a light wave to L12 | accepted (2026-10-19) |
| P.c | [Level 13's Nansen](../../campaign/act-2-homefront/level-13-polar-relay/README.md) | **finding**: the user's build predated Nansen's voice (text only); the tree has it since round 35 and the voice files are to be committed; a stale comment fixed. **Recommendation: accept** | — | — | accept | accepted (2026-10-19) |
| P.d | [Levels 08 and 09's waves, Ravagers and HP](../../campaign/act-2-homefront/level-09-arcology-fall/README.md) | **rework** (step CA, the user's answers: all recommended): waves L10-style, about 2.5 s apart; on screen 0.9 to 2.6 (L08) and 1.3 to 3.5 (L09), enemy shots a minute 6.5 to 20 and 4.2 to 22 (L10: 4.2 / 21.8), gaps over 2 s with the user's fit 17 to 2 and 14 to 2; hauls on budget (bounty scale 0.28 / 0.29); Ravagers 120 px/s (was 160), pounce every 4 s (hard 3), range 170, packs at t 62 / 76 / 100 / 106 / 141 / 147, the two cross-screen packs head-on, never mixed with the nodes; Needler 12 HP and Stinger 14 HP from Level 08 (small-unit act factor capped at x3). New tests: `PacingTest` Act 2 rule (at most 3 gaps over 2 s) and `ActFloorTest` (at least 2.5 on screen, 10 shots a minute). Items (a)-(e) below were left pending | — | five items, below | all yes / some no | accepted (2026-10-19) |
| P.e | [Levels 11-17 under the new rules](../../tech/roadmap/README.md) | **question**: Levels 11-17 are **below** the new pacing rules (`PacingTest.PENDING_ACT_TWO_RULE`: a list of exceptions that keeps the build green). Rework their waves too? **Recommendation: yes**, one level a step, Level 11 and 14 together with their boss rework (CB), 12-13 and 15-17 after; the test's exception list shrinks as each is done | — | Level 17 was just tuned to its own pacing (about 70 enemies a minute): it may already be close | yes, all / Acts 2 only (11-14) / later / no | **yes, all** (2026-10-19): Levels 11-14 now (11 and 14 with the boss rework), Levels 15-17 later |
| P.f | [Refactors](../../tech/architecture/README.md) | **yes or no, each** (steps SR-G, SR1): (a)-(d) below | — | the sim refactor stopped at a safe spot (usage limit); nothing reverted | all yes / some no | all yes (2026-10-19) |
| P.g | [Build speed](../../tech/README.md) | **yes or no, each** (steps BT1, BT2a, BT2c): the full `check` went 49.9 to 11-12 min; (a)-(e) below | — | BT2b (content loading) not done | all yes / some no | all yes (2026-10-19) |

**Build choices (row P.d)**, the five pending items, each *yes* or *no*:
- **(a)** Waves have 4.4-4.7 units (the brief asked 6-7: that was twice Level 10's fire and killed the plan fit on hard), Skitters 47 % (L08) / 52 % (L09) (brief: 40 %).
- **(b)** Hard has fewer units in places (firing groups -1, packs as medium, bridge 3), so `ActPlaythroughTest` hard reads -13.9 % of its +-15 % band.
- **(c)** The act HP factor cap applies to Levels 08-09 only (`small_act_hp.levels`): a wider range broke Level 10's escort and Level 11 hard.
- **(d)** Two Creeper convoys stay at 3 (not 5).
- **(e)** Okafor's bridge line stays at t=107 (101 was 6 s late on easy), so the Ravager packs there are 106 / 141 / 147.

**Build choices (row P.f)**, each *yes* or *no*:
- **(a)** Sortie split 4,912 to 2,187 lines (hit, contact, objective, ally, ground, pickup and boss passes, hold zones, latches, sightings, checkpoint) and Enemy 3,463 to 2,918 (aura ring, submerging, swooping, bomb run, curl-roll, pouncing, sweep beam, spawner iris, part HP); the public API identical (`javap`), replay outputs identical on a snapshot for all but the last move.
- **(b)** `LevelScript` stays whole: its 44 nested types are API used outside sim.
- **(c)** In the game, `LevelRenderer` 2,625 to 913 lines, `FlightSounds` 1,665 to 620, `LevelScreen` 1,986 to 1,003 (pass, cue and event classes; `PassOrderTest`).
- **(d)** A few resets run in a different order, and `FlightSounds` throws when one event is claimed twice (a bug guard).

**Build choices (row P.g)**, each *yes* or *no*:
- **(a)** The full `check` runs test forks at half the cores with a 1 GB test heap, Gradle parallel on (49.9 to 11-12 min, green twice, same outcomes).
- **(b)** `packAtlases` is parallel and cacheable (cold 240 to 50-60 s, cached about 7 s); its output byte-identical.
- **(c)** The backdrop pages are numbered in folder order and duplicate entries sorted (so atlases do not churn).
- **(d)** `RadioTimelineTest` is a base class plus one class per level (game tests 297 to 116 s); a guard test fails when a level has no class.
- **(e)** The backdrop check scrolls once per step (patch applied), with a plan for the rest (BT2b: content loaded once per JVM, data tests only for the edited level).

## Build choices and findings

**B** rows: each item *yes* (keep as built) or *no* (say what). **R** rows: as their answer cell says.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.a | [The gap check's defaults](../../campaign/act-3-red-dust/level-17-dome-siege/README.md) | **yes or no, each** (gap check, built in D1–D4): (a)–(n) below | [K.a](#captures) | — | all yes / some no | all yes (2026-10-19) |
| B.b | [Domes and the hook in the simulation](../../allies/ares-landing-dome/README.md) | **yes or no, each** (step D1): (a)–(g) below | [K.a](#captures) | — | all yes / some no | all yes (2026-10-19) |
| B.c | [Aura and Shellback in the simulation](../../enemies/air/choir-herald/README.md) | **yes or no, each** (step D3): (a)–(k) below | [K.a](#captures) | — | all yes / some no | all yes (2026-10-19) |
| B.d | [Level 17's data](../../campaign/act-3-red-dust/level-17-dome-siege/README.md) | **yes or no, each** (steps D2, D4): (a)–(i) below | — | — | all yes / some no | all yes (2026-10-19) |
| B.e | [In the game](../../campaign/act-3-red-dust/level-17-dome-siege/README.md#implementation) | **yes or no, each** (steps G1, G2): (a)–(p) below | [K.a](#captures) | — | all yes / some no | all yes (2026-10-19) |
| B.f | [The flow fix](../../ui/README.md) | **yes or no, each** (step FLOW, after the user's Act 2 playtest): (a)–(d) below | — | — | all yes / some no | all yes (2026-10-19) |
| R.a | [The Shellback's vent](../../enemies/ground/shellback/README.md) | **open decision**: the vent (the radio: "Hit the lime vent") sits at the rear of the back, inside the body's hit box, so no shot from below reaches it (0 vents shot by the autopilot over 24 runs). In Level 17 the t=46.5 Shellback then always hits Lowell (shot, its roll crosses Lowell; left alone, its second lob lands on it), so the dome secondary is met on medium and hard **0 / 8** (easy 8 / 8, its Shellbacks unhooked). **Recommendation: (a)** a shot inside the vent's box hits the vent first (from any side), **or (b)** the vent faces the player (front of the back). Then re-measure the secondary (R.b) | [K.a](#captures) (t 48–51.5) | — | a / b / keep / other | **a** (2026-10-19): a shot inside the vent's box hits the vent first, from any side; queued (Level 17's README, Implementation) |
| R.b | [The dome secondary's rate](../../campaign/act-3-red-dust/level-17-dome-siege/README.md#implementation) | **finding** (step D4): "a careful pilot keeps every dome about half the time at medium" measured with the autopilot over 8 seeds: easy 8/8, medium 0/8, hard 0/8, all from R.a; not tuned further (3 passes cap). After R.a: re-measure and tune the hits or the hooked waves | — | — | re-measure after R.a / keep | **re-measure after R.a** (2026-10-19); queued |
| R.c | [Shellbacks over the domes](../../campaign/act-3-red-dust/level-17-dome-siege/README.md#waves) | **finding**: the Shellbacks' walk paths cross the domes, and the walker is drawn over the glass: it reads as walking on the dome (K.a t 48–50.5 over Lowell, 113–118 over Schiaparelli and Burroughs) | [K.a](#captures) | — | keep / route the walks between the domes | **route the walks between the domes** (2026-10-19); queued |
| R.d | [The first Herald's life](../../enemies/air/choir-herald/README.md) | **finding**: on the plan's ship the t=55.5 Herald dies about 2 s after it enters (K.a t ≈ 57.5), its rings barely seen; the second one (t=124) shows them | [K.a](#captures) | — | keep / enter it higher or slower | **enter the first Herald higher or slower** (2026-10-19); queued |
| R.e | [Rolls off the bottom](../../enemies/ground/shellback/README.md) | **finding** (step D4): a Shellback shot to 40 % near the bottom rolls off the screen before it uncurls, so its belly window rarely shows | — | — | keep / roll a shorter way near the bottom | **roll a shorter way near the bottom** (2026-10-19); queued |
| R.f | [Dome lines dropped](../../campaign/act-3-red-dust/level-17-dome-siege/README.md#radio-chatter) | **finding** (step D5, `RadioTimelineLevel17Test`): the dome `ally-hit` / `ally-lost` lines are event lines: they wait for a gap and are dropped after 6 s, and under the dense timed lines most of them are dropped (in a run losing every dome, all of them; Bradbury's first hit under Varga's bomber warning in every run) | — | — | keep / let a dome's loss line queue as a phase line (kept 20 s) | keep (2026-10-19): decided, recorded in Level 17's Decisions with an unticked checklist item |

**Build choices (row B.a)** — the gap check's defaults (each yes/no):
- **(a)** The hook is set per unit type (`targeted_by`: every Spore Bomber wave `always`, every Shellback `alternate`).
- **(b)** Hooked bombers hold a row 160 px below the top edge, a wave's bombers on slots 40 px apart round the dome's x.
- **(c)** Objective lobs land in ground space (on the scrolling dome), one hit per direct lob.
- **(d)** The Herald's escorts orbit at 0.8 × its aura radius (inside the rings).
- **(e)** The t=124 bombers are a plain hooked line abreast beside the Herald (not its escorts: a bomb run never applies to escorts).
- **(f)** The aura rings every layer but buried and submerged units.
- **(g)** A curled Shellback's armour also stops blasts.
- **(h)** An EMP stun pauses a roll.
- **(i)** The vent is a general non-boss unit part (`parts` on a unit).
- **(j)** The Dust Kite's `flock` is built in part D (Level 17's swarm) and reused by part E.
- **(k)** The Polyp Mortar nests stay static.
- **(l)** The dome has its own directory `design/allies/ares-landing-dome/`, its numbers in `design/allies/data.yaml`.
- **(m)** The intel's PHASE marks for the two dome sections.
- **(n)** The militia's flak is a presentation key `flak: {from: 160, every: 1.5}`; a carried pickup can name its carrier (`dropped_by.enemy`, the Herald's shield cell).

**Build choices (row B.b)** — domes and the hook, step D1 (each yes/no):
- **(a)** A bomber steers to the lowest dome whose box top reaches its row.
- **(b)** Escorts, chain members, flock members and walkers never bomb.
- **(c)** A dome lob is one hit.
- **(d)** `always` aims at the nearest dome.
- **(e)** Dives do not hurt domes.
- **(f)** A dome breaking never fails the mission; the `untouched` secondary is failed at the first hit and met when Eden has passed.
- **(g)** An `untouched` secondary without ground allies is refused by the simulation.

**Build choices (row B.c)** — aura and Shellback, step D3 (each yes/no):
- **(a)** Ramming and a spawner's burst bypass the ring (every other damage path spends the ring first).
- **(b)** After an EMP strip the refill waits out the stun.
- **(c)** A unit leaving the radius keeps its ring 1 s, then it fades over 0.5 s.
- **(d)** The Herald leaves upward; a `carrier + escorts` Herald wave hovers.
- **(e)** The Shellback is armoured from the curl through the uncurl and stands still while curling, uncurling and showing its belly.
- **(f)** A blast covering the vent and the body hurts both, the vent first.
- **(g)** The 0.5 s mortar tell is not repeated after a roll.
- **(h)** Crushed ground objects pay nothing; hardened objects and triggers are never crushed.
- **(i)** The Shellback's numbers: body 120 HP / 40 bounty, vent 12 HP, 8 extra for the vent shot first.
- **(j)** The Herald's song is a 10-bullet fan over nine tenths of a turn every 4 s.
- **(k)** A ring absorb is reported at most once per unit every 6 steps; the ring is capped by the unit's HP.

**Build choices (row B.d)** — Level 17's data, steps D2 and D4 (each yes/no):
- **(a)** 51 waves, 196 enemies at medium (≈ 70 a minute), typical haul 2,068 (+0.1 %), perfect 3,289.
- **(b)** The t=111 pair as a convoy of 2.
- **(c)** The side bomber pairs (t=43.5, 108.5, hard's 126) as streams routed in at the row, 1 s apart.
- **(d)** Six popcorn waves moved 0.5–1.5 s for `PacingTest` on easy (73.5 → 72.5, 80.5 → 81.5, 98.5 → 100, 105 → 105.5, 162.5 → 162, 166 → 165.5).
- **(e)** The domes enter by their lower edge at their time, the backdrop's aprons placed to match (`backdrop_l17.py` `dome_centre`).
- **(f)** The compound piece is named `helix-compound`; nothing in the level within 100 px of it (`SiegeChecks`).
- **(g)** The vent's hit box offset 15 px (aligned with the art's pivot, was 30).
- **(h)** Rail pieces one transparent row longer at each end; the aprons', compound's and gun pits' edges faded; the spaceport apron's slab grid off its wrap.
- **(i)** The Autopilot's siege targeting (the urgent hooked bomber, the Herald, bombers by drop time, never a Shellback with a dome in its roll reach).

**Build choices (row B.e)** — in the game, steps G1 and G2 (each yes/no):
- **(a)** A bomb drop reuses the bomb whistle; no extra effect for it.
- **(b)** The dome sounds play at the explosion level.
- **(c)** The dome's flicker at 10 fps, its white flash 0.1 s.
- **(d)** The flak puffs in a band over the spaceport.
- **(e)** The DOMES tracker reads FAILED from the first hit's end of the secondary until the level ends (seen in K.a).
- **(f)** The capsule's glow follows the halves rule.
- **(g)** The supply drone's launch needs a `defend` primary (a bug fix).
- **(h)** The unit one-shots at 1.0.
- **(i)** Absorb sounds at most every 0.15 s.
- **(j)** The ring's size and margin round each unit.
- **(k)** The Herald's radius circle pulses.
- **(l)** The threads drawn under the flyers.
- **(m)** The collapse shrinks the rings to 65 %.
- **(n)** A glance flashes B8B8C0 for 4 steps.
- **(o)** The belly glows in its last 20 %.
- **(p)** The aura loop pans to the nearest ring; the vent bursts when shot.

**Build choices (row B.f)** — the flow fix, step FLOW (each yes/no):
- **(a)** Briefings skip the act title card and intro once shown.
- **(b)** The replay hangar autosaves at launch (the real campaign; a REPLAY header and chip). Since the
  user's correction of 2026-10-19 it is refit only: owned items fitted and unfitted and kept, nothing
  bought, sold, upgraded or repaired (`REFIT` drawer, `REPLAY: REFIT ONLY`).
- **(c)** The labels on the briefing and replay screens.
- **(d)** The game-over footer now reads "…CONTINUE RETURNS TO THE BRIEFING BEFORE THIS MISSION."

## Not done yet

A record, nothing to answer: these were queued but not started when the session ran out of budget.

| # | Item | Blocked by | Then |
|---|---|---|---|
| N.a | **FX2**: Level 08 cars under the viaducts (ground traffic, five cross highways overhead) and Level 13 icebergs (all drift east 10 px/s, re-pathed off the relay floe, the mortars' floe up 30 px, per-piece frame override) | nothing now (FX1 and D4 are done) | the user's answers of 2026-10-18 are the spec; then a capture |
| N.b | **CB**: Level 11 and 14 data and mapping for the reworked Kraken and Spire (four rooted arms, thinner fans, root HP -33 %, lanes, a homing hint in the radio, `hold_clear` and the platform t from `A.a`), the tests and the replays of L09, L11, L14 (re-recording allowed, L01 never) | the pick in `A.a` (the layout may change) | the Kraken and Spire fights reach the game |
| N.c | **BT2b**: the content module's load fixes (content loaded once per JVM, data tests only for the edited level) | nothing | about 2.5-3 min `check` |

## Decisions

- 2026-10-19: opened round 40 (built while the user was away): M7 part D (Level 17), the Level 16 canyon redo (round 39 R.1), the Act 2 playtest
  rework and the Kraken's r40 art, the debrief music (track 29). Every row is pending the user's review.
- 2026-10-19 (user, round 40, phone page): closed: every row answered as in its *Outcome* cell. F.a-F.g approved as
  final (the dome, the Choir Herald, the Shellback, Level 17's backdrop and props, the intel pictures, the briefing
  images: their concept rows `chosen`); A.a the Kraken r40 art chosen (its build choices A.aa-A.af yes, still to be put in
  the levels by step CB); W.a Level 16's canyon walls final (W.b-W.d yes/accepted/keep); S.a and S.c-S.l accepted, S.b
  redo (AUD-S); V.a, C.a accepted; B.a-B.f and P.a-P.g recorded (accepted as built); P.e yes, all (Levels 11-14 now,
  15-17 later); M.a = b "Stand Down" (MW wires it, a moves to `concept/rejected/`). K.a: "The second shell creature rolls
  over a dome and again some time later. That shouldn't happen. The radio about the shield formation is too late, it is
  spoken after they are all destroyed. The shell creatures seem too strong. None are destroyed." Part D's findings decided
  and queued: R.a = a (a shot inside the vent's box hits the vent first), R.b re-measure after R.a, R.c route the walks
  between the domes, R.d the first Herald enters higher or slower, R.e roll a shorter way near the bottom, R.f keep.
  Level 17 stays `art: chosen` until the dome-break redo (S.b) and the part D fixes land.
