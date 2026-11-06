# Music briefs and prompts — concept round 01

Two short sketches (~40 s each) of the same job, an in-level action theme, in the two
candidate styles for the game's soundtrack. They are code-generated mockups that show style,
structure and melody. They are not production quality. Reproduce with:

```
python3 tools/concept/audio/music.py          # writes both files into this directory
python3 tools/concept/audio/music.py a        # only sketch A (b for sketch B)
python3 tools/concept/audio/analyze.py design/audio/music/concept/*.ogg
```

Generator: `tools/concept/audio/music.py` (synth library `tools/concept/audio/synth.py`).
Both pieces are written as tracker-style patterns: one token per 16th step, `A4` starts a note,
`-` holds it, `.` is a rest. The melodies can be read and edited directly in the script.
Mastering: −14 LUFS integrated, sample-peak ceiling −2 dBFS (true peak ≈ −1 dBTP). Format:
OGG Vorbis q6, 44.1 kHz stereo, fixed seeds.

---

## music-r01-a — "Afterburner" (tracker-style trance/techno, 37.5 s)

**Style**: late-90s tracker/demoscene trance-techno, as in the Tyrian / Raptor / Amiga
module soundtracks: four-on-the-floor drive, a rolling off-beat bass, a 16th arpeggio, a
detuned supersaw lead with a dotted-eighth echo, and sidechain "pump" on the bass and pads.

**Tempo / key**: 140 BPM, 4/4, A minor (natural minor with a harmonic-minor E major dominant).

**Structure** (bars of 1.71 s):

| Bars | Section | What happens |
|---|---|---|
| 1–4 | Intro | Pad + 16th arpeggio with its low-pass opening from 500 Hz to ~3 kHz; hats from bar 2; kick and rolling bass enter at bar 3; breakbeat fill (snare/tom) on the last two beats of bar 4 |
| 5–12 | A | Crash; full groove (kick, clap on 2 and 4, 16th hats, open hats on the off-beats); lead melody, theme statement; breakbeat fill in bar 12 |
| 13–20 | B | Crash; the melody climbs into a higher, more heroic answer over new chords; noise riser and a 16th snare roll crescendo in bars 19–20; one beat of silence |
| 21 | Ending | Kick, crash, A minor chord, lead holds A5, reverb tail |

**Chord progression**:
- Intro: `Am | F | C | G`
- A: `Am | F | C | G | Am | F | G | E`
- B: `Dm | Am | F | E | Dm | Am | F | E` → final `Am`

**Melody** (lead, A section): starts A4-C5-E5, then falls D5-C5-B4-C5. The main hook is the
dotted rhythm (3+3+2 feel) over the first bar. B answers from F5 up to A5 / C6 / D6, with the
leading tone G♯ over each E chord.

**AI prompt** (Suno/Udio style): *"1990s tracker module trance techno, video game shoot'em up
level music, 140 BPM, A minor, driving four-on-the-floor kick, rolling off-beat bass,
16th-note square arpeggios, detuned supersaw lead with dotted-eighth delay, sidechain pumping
pads, breakbeat drum fills, heroic melodic hook, Amiga demoscene, Tyrian, Raptor: Call of the
Shadows, instrumental, loopable."*

---

## music-r01-b — "Coalition Rising" (synth-orchestral action, 40.7 s)

**Style**: synth-orchestral, as in the late-90s action scores made on hardware synths and
samplers (Colony Wars, Wing Commander, Einhänder). A galloping string ostinato, a heroic horn
motif, off-beat brass stabs, taiko-style big drums, timpani and cymbal crashes.

**Tempo / key**: 132 BPM, 4/4, D minor (the A major dominant uses C♯).

**Structure** (bars of 1.82 s):

| Bars | Section | What happens |
|---|---|---|
| 1–4 | Intro | Galloping low string ostinato on D (1-2-3 accent pattern), string pad swelling in, timpani on each downbeat; horn call D4 → A4 → C♯5–E5 in bars 3–4; timpani roll and riser into A |
| 5–12 | A | Crash + timpani; big drums in a 3-3-2 pattern (steps 1, 4, 7, 9, 12, 15) with snare on 2 and 4; the heroic horn motif in brass, doubled an octave lower; snare roll in bar 12 |
| 13–20 | B | The theme lifts an octave into high strings with brass underneath; ostinato doubles at the octave; brass stabs on the off-beats of 2 and 4; hats; snare roll into the end |
| 21 | Ending | Tutti D minor hit (strings, brass, high D6, low D2), taiko, timpani, crash, long reverb tail |

**Chord progression**:
- Intro: `Dm | Dm | B♭ | A`
- A: `Dm | B♭ | C | Dm | Dm | B♭ | Gm | A`
- B: `B♭ | C | F | Dm | Gm | A | B♭ | A` → final `Dm`

**Melody** (horn motif): D4 held, then A4 A4 (dotted rhythm), leaping to D5. This is the
"call to arms" rising fourth and fifth, and it works as a leitmotif for the Terran Coalition.
The second phrase falls D5-C5-B♭4-C5. B restates it higher (F5-D5-F5-B♭5, …) and climbs through
Gm and A to C♯6 before resolving to the final D.

**AI prompt** (Suno/Udio style): *"Late 1990s synth-orchestral video game action score,
132 BPM, D minor, galloping staccato string ostinato, heroic French horn and brass motif,
off-beat brass stabs, taiko war drums, timpani rolls, cymbal crashes, military snare, epic
space battle, Colony Wars, Wing Commander, hardware synth orchestra, instrumental, loopable."*

---

## Notes for choosing

- A leans into the retro-arcade identity. B leans into the serious war story. A hybrid is
  also possible: electronic drums and bass under orchestral strings and brass, a common late-90s
  approach (e.g. for boss and story tracks while levels stay electronic).
- Both sketches are written so the hook can become the game's main theme. Whichever style is
  chosen, the other's melody can be reused as a leitmotif.

---

# Round 02 — five loopable themes

Round 01 outcome: both sketches were chosen and became the Act 1 level themes A ("Afterburner")
and B ("Coalition Rising"). Round 02 composes five more themes in the same two idioms,
tracker-era electronic and synth-orchestral, and blends them where the brief asks for it.

Reproduce with:

```
python3 tools/concept/audio/music_r02.py                 # all five (~3.5 min)
python3 tools/concept/audio/music_r02.py title boss      # or any of: title hangar boss mars europa
python3 tools/concept/audio/analyze.py design/audio/music/concept/*-r02-a.ogg
```

Generator: `tools/concept/audio/music_r02.py` (reuses the instruments of `music.py` and the
synth in `synth.py`). Mastering is the same as round 01: −14 LUFS integrated, −2 dBFS sample
ceiling, OGG Vorbis q6, fixed seeds.

**Loop format.** Each file is *intro → loop → 2-bar fade-out tail*. The loop points are
stored in the file as Vorbis comments `LOOPSTART` and `LOOPLENGTH` (in samples at 44.1 kHz).
A looping player jumps from `LOOPSTART + LOOPLENGTH` back to `LOOPSTART` and never plays the
tail. The tail exists only so the file ends cleanly in an ordinary player. The loop was rendered
twice and the second pass kept, so reverb and echo tails carry across the seam. Tempos were
picked so a 16th note is a whole number of samples, which makes the loop exact. The loop starts
one beat after the intro ends, because the intro joins the loop through a one-beat crossfade.

**Motifs** (defined in the [music README](../README.md#motifs)):
- humanity's **main motif** is degrees 1-5-8-9-♭10 (D minor: D A D E F);
- the Vrell **Choir motif** is 8-♭7-♭6-5, a tritone drop to ♭2, then 1 (E: E D C B F E).

---

## title-theme-r02-a — "Terran Vanguard" (title / main menu, 57.6 s)

**Brief**: the game's calling card. Heroic and memorable, and the first statement of
humanity's motif. It blends the two chosen styles: an orchestral core (gallop strings, horns,
timpani, taiko) on a tracker backbone (four-on-the-floor kick, clap, 16th hats, pulse
arpeggio, rolling bass, supersaw lead).

**Tempo / key**: 126 BPM, D minor → F major lift.

**Loop**: `LOOPSTART` 357000 (8.095 s), `LOOPLENGTH` 2016000 (45.714 s = 24 bars).

| Bars | Section | What happens |
|---|---|---|
| 1–4 (intro) | Call | Timpani and swelling strings; horns call the motif slowly (D… A… D-E-F); filtered arp enters; snare roll and riser |
| L1–8 | A | Crash; the motif in brass (with a lower octave) over gallop strings, taiko hits, four-on-the-floor beat, quiet arp |
| L9–16 | B | The motif in **F major** (F-C-F-G-A) in supersaw lead + strings, brass underneath; rolling tracker bass, 16th hats, taiko on 1 and 3 |
| L17–20 | C: breakdown | Drums out; strings, arp opening up, the motif at half speed in low brass |
| L21–24 | C: build | Beat back; brass stabs from quarters to 8ths; gallop; snare roll into the loop start |

**Chords**:
- Intro: `Dm | Dm | B♭ | A`
- A: `Dm | B♭ | F | C | Dm | B♭ | Gm | A`
- B: `F | C | Dm | B♭ | F | C | B♭ | A`
- C: `Dm | B♭ | F | A | Dm | B♭ | C | A` → loop

**AI prompt**: *"Heroic 1990s video game title theme, 126 BPM, D minor with a lift to F major,
synth-orchestral brass fanfare motif over galloping staccato strings, taiko and timpani,
combined with a tracker-style four-on-the-floor beat, rolling bass and square-wave arpeggios,
detuned supersaw lead in the second half, epic space war, Wing Commander meets Tyrian,
instrumental, seamless loop."*

---

## hangar-theme-r02-a — "Dry Dock" (hangar / shop, 64.7 s)

**Brief**: music to shop to between missions. Calm, groovy and warm, and built to survive
dozens of repeats: no drops, no harsh highs (the top octaves sit 15–25 dB below the mids),
narrow dynamics and a melody that rests more than it plays. The feel is mid-90s downtempo
tracker (swung 16ths), with faint metallic hangar clanks as the "mechanical ambience".

**Tempo / key**: 90 BPM, 16ths swung by 30 %, D dorian.

**Loop**: `LOOPSTART` 264600 (6.000 s), `LOOPLENGTH` 2352000 (53.333 s = 20 bars).

| Bars | Section | What happens |
|---|---|---|
| 1–2 (intro) | Doors open | Electric-piano chords and soft pad, distant clanks, shaker fading in |
| L1–8 | A | Groove: soft kick (1, 2a, 3&), laid-back snare on 2 and 4, swung hats and shaker, round sub bass, FM electric piano comping on 1 and the "and" of 3 with suitcase tremolo |
| L9–16 | B | Flute melody. Its first phrase is a relaxed fragment of humanity's motif (A-D-E-F = 5-8-9-♭10) |
| L17–20 | C | Breakdown: rim clicks instead of drums, long bass notes, electric-piano "vibes" answer |

**Chords** (rootless electric-piano voicings): `Dm9 | G13 | Dm9 | G13 | B♭maj9 | Am7 | Gm9 | A7`
(A and B); C = the second half `B♭maj9 | Am7 | Gm9 | A7` → loop.

**AI prompt**: *"Relaxed downtempo lounge groove, 90 BPM swung 16ths, D dorian, warm FM
electric piano chords with tremolo, round sub bass, soft brushed-sounding drums and shaker,
gentle flute melody, faint distant metallic workshop clanks, 1990s tracker module chill-out,
spaceship hangar shop music, unobtrusive, not fatiguing on repeat, instrumental, seamless
loop."*

---

## boss-theme-r02-a — "The Choir Descends" (Vrell boss, 61.2 s)

**Brief**: intense, fast and dark: the Vrell act bosses. The Choir motif is the core, sung by
a wordless formant choir (over an "oo" drone chord in the intro) and doubled by low brass.
Pounding drums, a distorted 16th bass ostinato with a ♭2 bite, orchestral stabs, and a
tritone-laden synth riff.

**Tempo / key**: 150 BPM, E minor / E phrygian (♭2 = F), with B major as the dominant.

**Loop**: `LOOPSTART` 299880 (6.800 s), `LOOPLENGTH` 2257920 (51.200 s = 32 bars).

| Bars | Section | What happens |
|---|---|---|
| 1–4 (intro) | The Choir | Low E drone, the Choir motif alone over taiko; distorted bass and a snare roll build over bars 3–4 |
| L1–8 | A | Full fight: kick with pushes, snare on 2 and 4, taiko, tom fills; Choir motif (bars 1–2 and 5–6) with brass; orchestral stabs on a 3-3-2 grid; choir chords in between |
| L9–16 | B | Synth riff (E-G-E-F-B♭-A…, tritone E–B♭) over the ostinato; high string tremolo; 16th hats |
| L17–24 | A′ | Choir motif returns, with the riff two octaves down as a counterline |
| L25–28 | C: half-time | Kick/snare in half time, taiko 3-3-2, the Choir motif at half speed in low brass (E D C B → F … E) |
| L29–32 | C: build | Four-on-the-floor, snare 8ths → 16ths, riser into the loop start |

**Chords** (two bars each):
- A / A′: `Em | C | F | B`
- B: `Em | C | Am | B`
- C: `Em | F | C | B` → loop

**AI prompt**: *"Intense dark boss battle music, 150 BPM, E phrygian, eerie wordless choir
singing a descending phrase with a tritone drop, pounding taiko and electronic drums,
distorted 16th-note bass ostinato, orchestral brass stabs, aggressive detuned synth riff,
alien hive mind, 1990s synth-orchestral and tracker hybrid, instrumental, seamless loop."*

---

## mars-theme-r02-a — "Red Dust Run" (Act 3 level theme A, 53.8 s)

**Brief**: Mars: driving, dusty, dry heat. Breakbeat energy under a desert scale: E phrygian
dominant (E F G♯ A B C D, "Hijaz"). The world doc's mood is carried by a desert-wind drone
(band-passed noise that wanders), hand drums (doum/tek pattern and shaker), and a lonely
nasal reed lead that scoops into its notes. A detuned reese bass drives it. The dust-storm
breakdown muffles the drums and swells the wind, mirroring the level hazard.

**Tempo / key**: 135 BPM, E phrygian dominant; the B section uses the Andalusian cadence
(Am–G–F–E).

**Loop**: `LOOPSTART` 333200 (7.556 s), `LOOPLENGTH` 1881600 (42.667 s = 24 bars).

| Bars | Section | What happens |
|---|---|---|
| 1–4 (intro) | Desert | Wind drone, hand drums, low E drone, the reed's lonely call; snare pickup |
| L1–8 | A | Breakbeat (kick on 1, 1&, 3&, 3a; snares on 2, 2a, 3e, 4, 4a), reese bass, reed melody climbing E-F-G♯, tom fills every 4 bars |
| L9–16 | B | Andalusian cadence; 16th arpeggio in the scale; the melody an octave higher |
| L17–20 | C: dust storm | Drums low-passed to 700 Hz, hand drums louder, wind swells, long bass notes, the reed call |
| L21–24 | C: build | Drum filter opens back up, arp returns, snare roll; F → E (♭II–I) back into the loop |

**Chords**:
- A: `E | F | E | Dm | Am | F | Dm | E`
- B: `Am | G | F | E | Am | G | F | E`
- C: `E | E | F | F | Dm | Dm | F | F` → loop (E)

**AI prompt**: *"Driving desert breakbeat, 135 BPM, E phrygian dominant scale, dusty
amen-style breakbeat, darbuka and shaker hand percussion, detuned reese bass, nasal
mizmar-like reed lead melody with pitch scoops, howling desert wind drone, dust storm
breakdown with muffled drums, Mars colony, 1990s tracker module, Red Faction meets Tyrian,
instrumental, seamless loop."*

---

## europa-theme-r02-a — "Thera Deep" (Act 4 level theme A, 58.1 s)

**Brief**: under the ice of Europa. Ambient trance heard through water: the whole band runs
through a low-pass (1.8 kHz in the A section) that opens to 4.8 kHz in the B section as
if "surfacing", then sinks to 1.3 kHz in the deep breakdown. Sonar pings, whale-like glides and
bubbles stay outside the filter with long echoes, so they sound "in the water" around the
listener. Mysterious, but kept propulsive for action by a steady kick and a sidechain-pumped
rolling bass.

**Tempo / key**: 125 BPM, F minor (C major as the dominant, D♭maj7 with a ♯11 colour).

**Loop**: `LOOPSTART` 359856 (8.160 s), `LOOPLENGTH` 2032128 (46.080 s = 24 bars).

| Bars | Section | What happens |
|---|---|---|
| 1–4 (intro) | Descent | Pads behind a closed filter, sonar pings, a distant whale, the bass stirring in bars 3–4 |
| L1–8 | A | Muffled four-on-the-floor, open hats on the off-beats, pumping rolling bass, pluck arpeggio in long ping-pong echoes, sonar every 2 bars |
| L9–16 | B | Filter opens; the lead melody (supersaw with a flute an octave below) enters, soft claps |
| L17–20 | C: the deep | No drums; sub bass notes, whale song, echoing flute fragments, filter at its darkest |
| L21–24 | C: build | Drums return, riser, the filter comes back to its A-section setting at the loop point |

**Chords** (voicings: Fm9 = F-A♭-C-G, D♭maj7, B♭m9, Csus4 → C):
- A: `Fm9 | Fm9 | D♭ | D♭ | B♭m9 | B♭m9 | Csus4 | C`
- B: `D♭ | E♭ | Fm9 | Fm9 | D♭ | E♭ | Csus4 | C`
- C: as A → loop

**AI prompt**: *"Underwater ambient trance, 125 BPM, F minor, muffled low-passed mix as if
heard through water, steady soft four-on-the-floor kick, sidechain-pumping rolling bass,
plucked synth arpeggio drowning in ping-pong echoes, sonar pings, whale song, bubbles, lush
pads, filter slowly opening for a mysterious lead melody, deep sea city, The Abyss, 1990s
trance, instrumental, seamless loop."*

---

# Round 03 — five more loopable themes

Round 02 outcome: all five themes were chosen. Round 03 covers the rest of the campaign's
emotional arc: the fight for Earth, the twist in the belt, the hunt through Jupiter's storms,
the Ascendancy's own boss music, and the finale.

Reproduce with:

```
python3 tools/concept/audio/music_r03.py        # all five (~5 min)
python3 tools/concept/audio/music_r03.py belt   # or any of: earth belt jovian ascendancy final
python3 tools/concept/audio/analyze.py design/audio/music/concept/*-r03-a.ogg
```

Generator: `tools/concept/audio/music_r03.py` (builds on `music_r02.py`). The loop format
and mastering are identical to round 02: intro → loop → 2-bar fade-out tail,
`LOOPSTART`/`LOOPLENGTH` Vorbis comments in samples, −14 LUFS, −2 dBFS ceiling.

**Vorne's motif** is new in this round. It is degrees **1 – ♭3 – 5 – 7**, a minor-major-seventh
arpeggio, in a stately rhythm (dotted quarter, eighth, half, whole). In B minor:
B D F♯ A♯. The unresolved major 7th over a minor chord is the "cold" in it: it sounds
ordered, correct and wrong at the same time.

---

## earth-theme-r03-a — "Homefront" (Act 2 level theme A, 55.9 s)

**Brief**: the fight for home. Urgent and heroic but embattled: megacities under siege,
stormy seas, the arctic. Humanity's motif in a new guise: diminished into a relentless
16th-note figure (1-5-8-9-♭3-9-8-5) that the strings hammer under everything, re-rooted on each
chord (minor or major third as the chord demands). The world doc asks for heavier drums, choir
pads and the main theme in a minor key: urgent breakbeat, orchestral hits, a human choir, an
air-raid siren in the intro.

**Tempo / key**: 147 BPM, C minor (G major as the dominant).

**Loop**: `LOOPSTART` 306000 (6.939 s), `LOOPLENGTH` 2016000 (45.714 s = 28 bars).

| Bars | Section | What happens |
|---|---|---|
| 1–4 (intro) | Air raid | Siren rising and falling, choir swelling, timpani, the motif called by a far horn; the riff starts as a filtered pluck in bars 3–4; snare roll |
| L1–8 | A | Breakbeat (kick on 1, 1a, 3, 3&; ghost snares), the riff in strings with a pulse double, low brass pushing 8ths, orchestral hits every 4 bars, choir |
| L9–16 | B | Horns state the motif in full (C-G-C-D-E♭), trumpets an octave up; the riff drops an octave |
| L17–20 | C | Embattled: half-time drums, taiko, big choir, a supersaw lament; the riff as a quiet pluck |
| L21–28 | D | Everything: horns and supersaw lead on the motif, riff, breakbeat; snare roll and riser into the loop |

**Chords**:
- Intro: `Cm | Cm | A♭ | G`
- A: `Cm | Cm | A♭ | A♭ | Fm | Fm | G | G`
- B and D: `Cm | A♭ | Fm | G | Cm | A♭ | B♭ | G`
- C: `A♭ | Fm | Cm | G`

**AI prompt**: *"Urgent heroic war theme, 147 BPM, C minor, relentless 16th-note string
ostinato built from a rising heroic motif, driving breakbeat with orchestral hits, big choir
pads, French horns stating the theme, air raid siren intro, embattled half-time lament
section, cities under siege, 1990s synth-orchestral video game music, instrumental, seamless
loop."*

---

## belt-theme-r03-a — "Hollow Rock" (Act 5 level theme A, 60.9 s)

**Brief**: the asteroid belt and its mining stations: industrial, metallic and cold. This is
where the twist lands. Act 5 opens with Vorne's broadcast, so the intro is radio static,
two broadcast beeps, and his motif played by cold brass and organ. After that the machines play
his arpeggio: every sequencer line in the groove is 1-♭3-5-7. Metallic percussion (tuned anvil
hits on a 16th grid, steam bursts, a conveyor tick) and a distorted syncopated bass. In the
breakdown, a single distant horn plays humanity's motif, as the stations choose sides.

**Tempo / key**: 135 BPM, B minor, with a cold chromatic shift to G minor under the motif's
A♯ (= B♭, the third of G minor).

**Loop**: `LOOPSTART` 333200 (7.556 s), `LOOPLENGTH` 2195200 (49.778 s = 28 bars).

| Bars | Section | What happens |
|---|---|---|
| 1–4 (intro) | The broadcast | Static and beeps, low drone, Vorne's motif in brass with organ below; anvils start in bars 3–4 |
| L1–8 | A | Industrial four-on-the-floor, metal-layered snare, anvil pattern, distorted bass, pluck arp and distorted 8th line both playing Vorne's arpeggio |
| L9–16 | B | Vorne's motif in full brass (doubled low, with organ), answered by its own inversion; timpani |
| L17–20 | C | Conveyor breakdown: no kick, anvils and 16th metal ticks, long bass, a lone horn with humanity's motif in long echoes |
| L21–28 | D | Riser and build, then Vorne's motif as a tutti (brass in three octaves); snare roll into the loop |

**Chords**:
- A: `Bm | Bm | G | G | Em | Em | F♯ | F♯`
- B: `Bm | Gm | Bm | F♯ | Bm | Gm | Em | F♯`
- C: `Bm` pedal
- D: `G | G | F♯ | F♯ | Bm | Gm | Em | F♯`

**AI prompt**: *"Cold industrial techno, 135 BPM, B minor, metallic anvil percussion on a 16th
grid, steam hiss, distorted syncopated bass, arpeggio sequencer playing a minor-major-seventh
chord, stately cold brass and organ villain leitmotif, radio broadcast static intro, asteroid
mining station, 1990s tracker module industrial, Descent, instrumental, seamless loop."*

---

## jovian-theme-r03-a — "Eye of the Storm" (Act 6 level theme A, 65.6 s)

**Brief**: Jupiter's cloud cities and Callisto: vast, stormy and grand, the most bombastic act,
and the hunt for Vorne. The two motifs duel. Vorne's motif in low brass and choir (on G, then
on C) is answered each time by humanity's motif in *major* in the trumpets (on E♭, then on D).
In the final section both play at once, over a Gm/F♯ bass that makes Vorne's major 7th the
bass note. Thunder, wind and 32nd-note storm strings frame it.

**Tempo / key**: 140 BPM, G minor (D major as the dominant; a G–A♭ phrygian swing in the storm).

**Loop**: `LOOPSTART` 321300 (7.286 s), `LOOPLENGTH` 2419200 (54.857 s = 32 bars).

| Bars | Section | What happens |
|---|---|---|
| 1–4 (intro) | Storm front | Thunder, wind, tremolo strings; Vorne's motif far off, a trumpet answers; timpani roll |
| L1–8 | A | The hunt: humanity's motif in horns over gallop strings, four-on-the-floor, taiko and big pads |
| L9–16 | B | Vorne's motif (low brass + choir) vs. humanity's motif in major (trumpets), two bars each |
| L17–20 | C: the storm | No kick; thunder, tremolo strings, the choir swinging between G and A♭ |
| L21–24 | C: build | Gallop and drums return, snare roll, riser |
| L25–32 | D | Both motifs at once: humanity's in trumpets and supersaw, Vorne's in the bass brass |

**Chords**:
- Intro: `Gm | Gm | E♭ | D`
- A: `Gm | E♭ | B♭ | F | Gm | E♭ | Cm | D`
- B: `Gm(maj7) | Gm(maj7) | E♭ | E♭ | Cm(maj7) | Cm(maj7) | D | D`
- C: `Gm | A♭ | Gm | A♭ | E♭ | Cm | D | D`
- D: `Gm | Gm/F♯ | B♭ | F | Gm | Gm/F♯ | Cm | D`

**AI prompt**: *"Grand stormy orchestral-electronic battle theme, 140 BPM, G minor, thunder
and wind, tremolo storm strings, galloping string ostinato, taiko and four-on-the-floor,
choir, a cold villain brass motif answered by a heroic trumpet fanfare in major, both themes
colliding at the climax, gas giant cloud cities, Bespin, Einhänder, 1990s video game
music, instrumental, seamless loop."*

---

## ascendancy-boss-r03-a — "Iron Sovereign" (Ascendancy boss theme, 62.4 s)

**Brief**: for the Iron Sovereign battle station and Vorne's flagship Ascendant. Martial,
cold, human-military menace, deliberately the opposite of the organic Vrell boss theme: no
choir, strict rhythm, military snare rudiments (accents on 2 and 4 with drags in between),
timpani, low-brass 8th-note ostinato, trumpets on Vorne's motif, and a machine section where
a distorted 16th riff runs the motif's arpeggio shape on every chord (mM7 on minor chords,
maj7 on D, dominant 7th on C♯).

**Tempo / key**: 147 BPM, F♯ minor (C♯ major as the dominant; G major as a cold Neapolitan
in the half-time section).

**Loop**: `LOOPSTART` 306000 (6.939 s), `LOOPLENGTH` 2304000 (52.245 s = 32 bars).

| Bars | Section | What happens |
|---|---|---|
| 1–4 (intro) | Parade | Snare rudiments growing, timpani, Vorne's motif in low brass on F♯ then C♯, organ below |
| L1–8 | A | March: kick 1, 3, 3&; rudiment snare; low-brass 8ths; trumpets on the motif (F♯, then B); short-short-long brass stab figures |
| L9–16 | B | Machine: four-on-the-floor, metal hits, 16th riff on the motif shape, off-beat trumpet stabs |
| L17–20 | C: menace | Half time; the motif at one note per bar in low brass and organ (F♯-A-C♯-E♯) |
| L21–24 | C: build | Snare roll, riser |
| L25–32 | D | Tutti: trumpets on the motif with a low-brass canon a bar behind, two octaves down; riff, march, snare roll into the loop |

**Chords**:
- A and D: `F♯m(maj7) | F♯m | D | D | Bm(maj7) | Bm | C♯ | C♯`
- B: `F♯m | D | Bm | C♯` × 2
- C: `F♯m | G | F♯m | G | D | Bm | C♯ | C♯`

**AI prompt**: *"Cold martial boss battle theme, 147 BPM, F sharp minor, military snare
drum rudiments, timpani, low brass staccato ostinato, trumpets playing a stately
minor-major-seventh villain motif, distorted 16th-note synth riff, metallic industrial hits,
brass canon, human military menace, no choir, 1990s synth-orchestral and industrial hybrid,
instrumental, seamless loop."*

---

## final-boss-r03-a — "Choir Heart" (final boss, 67.6 s)

**Brief**: the final battle against the Choir Heart, with everything at stake and the biggest
arrangement so far. The Choir motif (descending) and humanity's motif (rising) are written to
be played *at the same time*: in section C they run in counterpoint (E5 over E4, D5 over B4,
C5 over E5, B4 over F♯5 …). The Heart beats through the intro and the breakdown. In the
breakdown the Choir sings Vorne's motif, since he was absorbed at L48. The loop ends with
humanity's motif in **E major**: hope, before the fight goes round again.

**Tempo / key**: 150 BPM, E minor / phrygian (F as ♭II); E major climax.

**Loop**: `LOOPSTART` 299880 (6.800 s), `LOOPLENGTH` 2540160 (57.600 s = 36 bars).

| Bars | Section | What happens |
|---|---|---|
| 1–4 (intro) | The Heart | Heartbeat, organ, the Choir motif huge (choir + low brass); humanity's motif answers far off; snare roll |
| L1–8 | A | Vrell fight: the Choir motif twice, distorted 16th bass, pounding drums, orchestral stabs, the heartbeat underneath |
| L9–16 | B | Humanity's fanfare (trumpets + supersaw), gallop strings, rolling bass, four-on-the-floor; the choir holds chords |
| L17–24 | C | Both motifs at once, in counterpoint; 16th string figures, timpani on every other bar |
| L25–28 | D: the Heart | Only the heartbeat, a drone and the choir ("oo"): Vorne's motif, then a whispered Choir motif |
| L29–32 | D: build | Taiko 3-3-2, snare roll, riser |
| L33–36 | E | Humanity's motif in E major: trumpets, brass, supersaw, choir and organ; then B major back into the loop |

**Chords**:
- Intro: `Em | F | Em | B`
- A: `Em | Em | C | C | F | F | B | B`
- B: `Em | C | Am | B | Em | C | D | B`
- C: `Em | F | C | B | Em | F | Am | B`
- D: `Em` × 4 `| C | C | B | B`
- E: `E | E | C | B`

**AI prompt**: *"Epic final boss battle, 150 BPM, E minor resolving to E major, a huge alien
choir singing a descending motif in counterpoint against a heroic rising brass fanfare,
pulsing heartbeat, organ, distorted bass ostinato, pounding taiko and electronic drums,
galloping strings, orchestral stabs, breakdown with only a heartbeat and whispering choir,
triumphant major-key climax, 1990s synth-orchestral video game finale, instrumental,
seamless loop."*

---

# Round 08 — cues for Acts 1–2 and full-length versions

Generator: `python3 tools/concept/audio/music_r08.py [name ...]` (names: `briefing act2b
miniboss warning complete actcomplete failed gameover title hangar afterburner coalition
homefront choir`). Deterministic; loops use the round-02 format (intro + loop + 2-bar fade
tail, `LOOPSTART` / `LOOPLENGTH` in samples). Stings are one-shot files mastered to −14 LUFS
with a −2 dBFS ceiling (peak after encoding ≤ −1 dBFS).

**Verification and mix pass (2026-10-01)**: all 14 files measured (`analyze.py`, spectrograms,
octave-band balance against the chosen r01–r03 tracks, loudness per 4 bars, seam jump) and
re-rendered where needed. All are at −14.0 LUFS (−13.9 for "Game Over"), peak ≤ −1.6 dBFS
after encoding, no DC, clean starts and ends; every loop's seam jump is below the local
sample-to-sample change (ratio ≤ 0.65), and re-rendering gives bit-identical files. Fixes:
"Dry Dock" full length −4 dB below 60 Hz (its sub was ~3 dB above the other tracks);
"Situation Room" −4.5 dB around 240 Hz (150–400 Hz was its loudest band) and its radio static
made loop-periodic (it jumped slightly at the seam); "Coalition Rising" back at 132 BPM and
its lyrical interlude +3 dB (it sat 5–6 LU under the rest); "Homefront" turnaround now carries
humanity's motif (it dipped 4 LU). The EQ is a zero-phase corrective EQ on the whole mix
(`MIX_EQ` in the generator). Remaining dips of ~3–5 LU are the intended breakdowns
("Afterburner" pluck breakdown, title and boss breakdowns, the "Firestorm" lament).

## miniboss-sting-r08-a — "Contact Heavy" (#21 mini-boss stinger, 4.6 s)

**Brief**: a riser into three orchestral hits on a 3-3 rhythm (A minor brass stabs, timpani,
taiko, distorted low A), then a sustained A minor chord with a dissonant ♭9 in the choir
(B♭ over A) and a high string E. Key-neutral enough to cut in over any level track, which
resumes after it.

**Tempo / key**: 120 BPM feel, A minor (♭9 colour).

**AI prompt**: *"4-second orchestral stinger for a video game mini-boss entrance, short
riser into three heavy brass and timpani hits, then a dissonant sustained choir and string
chord, 1990s synth-orchestral, punchy, instrumental."*

## boss-warning-r08-a — "Red Alert" (#22 boss warning, 3 bars at 150 BPM = 4.8 s + tail)

**Brief**: two-tone klaxon on every beat, a 3-bar riser, an E drone in choir ("oo"), strings
and distorted bass, taiko getting denser (half notes → quarters → eighths), snare and timpani
roll in the last bar. The final beat is silent so the Vrell boss theme (150 BPM, E minor)
starts on the downbeat; only a short reverb tail remains.

**Tempo / key**: 150 BPM, E (matches "The Choir Descends").

**AI prompt**: *"5-second boss warning cue, alternating two-tone alarm klaxon, rising noise
sweep, low choir drone in E, accelerating taiko drums and snare roll, stops dead on the last
beat before the boss music, 1990s arcade shoot 'em up, instrumental."*

## mission-complete-r08-a — "Mission Complete" (#23 victory jingle, 6.4 s)

**Brief**: humanity's motif in **D major** (D-A-D-E-F♯) in horns, trumpets and low brass over
D | G – A, a snare build, then a ringing D major tutti (strings, choir, brass, timpani,
crash).

**Tempo / key**: 126 BPM (the title tempo), D major.

**AI prompt**: *"6-second victory jingle, heroic brass fanfare on a rising five-note motif in
D major, timpani and snare roll, final big orchestral D major chord with cymbal, 1990s
synth-orchestral game music, instrumental."*

## act-complete-r08-a — "Act Complete" (#24 fanfare, 16.3 s)

**Brief**: the longer fanfare after an act boss. Horns state humanity's motif over
D | B♭ | G | A (the ♭10 on B♭ keeps a hint of the minor), then trumpets take it up from B♭
over B♭ | C with choir, taiko and an arpeggio shimmer, a snare roll and riser, and the final
D major chord.

**Tempo / key**: 108 BPM, D major with ♭VI–♭VII.

**AI prompt**: *"15-second triumphant act-complete fanfare, horns then trumpets on a heroic
rising motif, choir, timpani, taiko, shimmering synth arpeggio, snare roll into a huge D major
chord, 1990s synth-orchestral video game, instrumental."*

## mission-failed-r08-a — "Mission Failed" (#25 downbeat sting, 5.8 s)

**Brief**: low brass descending D-C-B♭-A in two octaves over D minor, then a dark D minor
chord with timpani and a low "oo" choir; the whole mix sinks through a low-pass sweep (7 kHz
→ 350 Hz), like the ship's systems powering down.

**Tempo / key**: 80 BPM, D minor.

**AI prompt**: *"5-second mission failed sting, slow descending low brass line in D minor,
timpani hit, dark low choir chord, sound filtering down as if powering off, somber, 1990s
synth-orchestral game, instrumental."*

## game-over-r08-a — "Game Over" (#26 somber, 20.7 s)

**Brief**: humanity's motif slowly and in minor on a lone horn over Dm | B♭ | Gm | A | Dm,
soft strings and "oo" choir, a flute echoing the last phrase an octave up, ending on a low D
minor chord.

**Tempo / key**: 72 BPM, D minor.

**AI prompt**: *"20-second somber game over theme, lone French horn playing a slow heroic
motif in D minor, soft strings and distant wordless choir, flute echo, quiet timpani,
melancholic ending, 1990s synth-orchestral, instrumental."*

## briefing-theme-r08-a — "Situation Room" (#3 briefing, 96.6 s)

**Brief**: tense but quiet, under Okafor's and Varga's briefing text. Dark low-passed pads,
a clock-tick rim on every beat, telemetry blips (short plucks on chord tones through a
ping-pong delay), a sonar ping every four bars and faint radio static (loop-periodic, so it does not jump at the seam). Then pulse bass and a
soft kick, muted-horn fragments of humanity's motif (D-A-D, B♭-F-B♭), a D minor / E♭
(Neapolitan) tension section with tremolo strings and timpani, and a thin bar back to the
loop start.

**Tempo / key**: 100 BPM, D minor. **Loop**: `LOOPSTART` 238140 (5.400 s), `LOOPLENGTH`
3810240 (86.400 s = 36 bars). **Chords**: `Dm Dm B♭ B♭ Gm Gm A A` × 2 | `B♭ C Dm Dm B♭ C A A`
| `Dm E♭ Dm E♭ Gm Gm A A` | `Dm Dm B♭ A`.

**AI prompt**: *"Tense military briefing room music, 100 BPM, D minor, dark ambient synth
pads, ticking clock percussion, sparse telemetry beeps and sonar pings, quiet radio static,
soft pulsing bass, distant muted horn motif, 1990s sci-fi game, understated, instrumental,
seamless loop."*

## act2-b-theme-r08-a — "Firestorm" (#7 Act 2 B, 120.4 s)

**Brief**: the second Earth-surface theme, contrasting with "Homefront": an amen-style
breakbeat, reese bass, orchestral hits and a syncopated brass-and-string stab riff. A string
and choir melody in A; humanity's motif in brass (G-D-G-A-B♭) in B; a 16th pluck figure on
the motif's shape; a half-time lament; a storm section (toms, thunder, tremolo strings,
choir stabs); and an acid-arp tracker break.

**Tempo / key**: 140 BPM, G minor. **Loop**: `LOOPSTART` 321300 (7.286 s), `LOOPLENGTH`
4838400 (109.714 s = 64 bars). **Chords**: A `Gm Gm E♭ E♭ Cm Cm D D`; B `E♭ B♭ F Gm E♭ B♭ Cm D`;
C `Cm Cm E♭ E♭ F F D D`; storm `Cm E♭ B♭ F Cm E♭ D D`; break `Gm E♭ Cm D` × 2.

**AI prompt**: *"Urgent 1990s breakbeat and orchestral hybrid level theme, 140 BPM, G
minor, amen break drums, growling reese bass, orchestral brass stabs and hits, heroic brass
motif, choir, storm section with toms, thunder and tremolo strings, acid synth arpeggio
break, video game action, instrumental, seamless loop."*

## Full-length versions (round 08)

Each is the chosen sketch extended to production length with the same instruments, motifs
and mix. New sections are added for variation; the chosen sketch remains the reference for
style.

| File | Tempo / key | Loop (samples) | Sections (loop) |
|---|---|---|---|
| title-theme-full-r08-a — "Terran Vanguard" | 126 BPM, D minor | 357000 + 5376000 (64 bars) | A motif · B major lift · A2 motif in strings + brass counter-line · new G minor bridge (`Gm E♭ B♭ F Gm E♭ C A`) · breakdown/build · B2 + trumpets · A3 tutti · horn-call turnaround |
| hangar-theme-full-r08-a — "Dry Dock" | 90 BPM swing, D dorian | 264600 + 6585600 (56 bars) | groove · flute · vibes counter-line · new e-piano solo (`B♭maj9 Am7 Gm9 A7 B♭maj9 C9 Dm9 A7`) · breakdown · flute + vibes · half-time walking bass · breakdown |
| afterburner-full-r08-a — "Afterburner" | 140 BPM, A minor | 623700 + 6048000 (80 bars) | A · B · pluck breakdown · build · A2 · B2 + counter-arp · breakbeat/acid tracker break · new C melody (`C G Am F C G F E`) · A3 · turnaround |
| coalition-rising-full-r08-a — "Coalition Rising" | 132 BPM, D minor | 340772 + 5773091 (72 bars) | horn theme · string theme · battle (trumpet counter-melody, 3-3-2 stabs) · lyrical interlude (strings, flute, harp; +3 dB in the mix pass) · A2/B2 + choir, trumpets · humanity's motif · build · horn-call reprise |
| homefront-full-r08-a — "Homefront" | 147 BPM, C minor | 306000 + 5760000 (80 bars) | A + string/choir counter-melody · horns · lead variation · lament · siege (augmented motif in choir) · counterattack in E♭ major · lament in strings · horns · tutti · A3 · turnaround with humanity's motif in brass and siren |
| choir-descends-full-r08-a — "The Choir Descends" | 150 BPM, E minor | 299880 + 5080320 (72 bars) | A · riff · A2 · half-time/build · phase 2 tritone grind · chase with the Choir motif in canon · riff + brass · breakdown, motif augmented in the choir · tutti |

**132 BPM with a sample-exact loop**: a 16th at 132 BPM is 5011.36 samples, so the round-02
integer-sample grid cannot hold it (the first render moved to 135 BPM). `FracLoopSong` rounds
the *loop length* instead: 72 bars = 5773091 samples (131.99999 BPM), the step is derived from
it, and every event is placed on the nearest sample of that exact grid (at most ½ sample =
11 µs early or late, inaudible). Each loop pass is then the previous one shifted by exactly
`LOOPLENGTH` samples, so the seam stays sample-exact.

**AI prompt** (per track): use the round-01/02/03 prompt of the original sketch and add
*"full-length 2–3 minute arrangement with varied sections, bridge and breakdown, seamless
loop"*.

## Round 11 — stems (`tools/concept/audio/music_r11.py`)

### coalition-rising-base-r11-a
Base stem of "Coalition Rising" full length (track 5), for Level 01 until section 4. One render
of `music_r08.coalition_full()`: the timpani hits are tagged on their way into the percussion
group and collected in a separate group, and the base stem is mixed from the ostinato, string
pad, timpani, choir, harp, flute and riser groups with the full mix's own effect chains,
coefficients and interlude lift. Mastering is linked (each loudness pass measures the full mix
and gives both the same gain and ceiling; the encoder's peak correction is shared), so base and
full have the same length (142.3 s), the same `LOOPSTART` / `LOOPLENGTH` (340772 + 5773091) and
the same gain: a linear crossfade from base to full adds exactly the brass, lead strings and
drums. The full mix of that pass is byte-identical to `coalition-rising-full-r08-a.ogg`, so the
chosen file is the pair's other half (the script checks this; `--full` writes it as r11 if it
ever differs). The base stem sits 6.4 LU under the full mix; it is not re-mastered on its own,
since that would break the crossfade.

**AI prompt:** use the "Coalition Rising" prompt and add *"underscore stem only: string ostinato,
pads, timpani, harp and choir, no brass melody, no drums, same tempo and length as the full mix"*.

## themes-final-r13-a — title, hangar and briefing themes (production art)

Not a proposal of new music: the production files of the three chosen themes, for the UI batch
review (round 13). `python3 tools/art/themes.py` renders `title-theme-full-r08-a`,
`hangar-theme-full-r08-a` and `briefing-theme-r08-a` again with their generator
(`tools/concept/audio/music_r08.py`, `render_loop`: same composition, seeds, mix EQ and master at
−14 LUFS, OGG Vorbis q6, `LOOPSTART` / `LOOPLENGTH` in samples), remuxes the stream with a
`SOURCE` comment into `assets/music/{title,hangar,briefing}-theme.ogg` and checks them
(`--check`: loudness within 0.5 LU, true peak below −1 dBTP, nominal 192 kbit/s, seam jump no
larger than the nearby sample-to-sample change, and whether the decoded audio equals the chosen
file). The sheet `themes-final-r13-a.png` (`--review`) draws each file's waveform from
`assets/music/` with the intro (grey), loop (blue) and fade tail (dark) and the loop points
(amber), with the measured numbers.


## boss-music-final-r25-a — tracks 18, 22 and 24 (production art)

Not a proposal of new music: the production files of the Act 1 boss and act-end tracks, for the M4
part G review (round 25). `python3 tools/art/themes.py boss warning actcomplete` renders the chosen
`choir-descends-full-r08-a` (#18 "The Choir Descends"), `boss-warning-r08-a` (#22 "Red Alert") and
`act-complete-r08-a` (#24 "Act Complete") again with their generator
(`tools/concept/audio/music_r08.py`, unchanged: `render_loop` for the boss theme, `render_sting` for
the two one-shot cues; same composition, seeds and master at −14 LUFS, OGG Vorbis q6, the boss
theme with `LOOPSTART` / `LOOPLENGTH` in samples), remuxes each stream with a `SOURCE` comment into
`assets/music/{choir-descends,boss-warning,act-complete}.ogg` and checks them. The decoded audio is
identical to the chosen files; only the comment is new.

- `choir-descends-final-r25-a.ogg`, `boss-warning-final-r25-a.ogg`, `act-complete-final-r25-a.ogg`:
  byte copies of the three files in `assets/music/`, for the review board.
- `boss-handoff-final-r25-a.ogg`: listening aid, the boss cue as the game mixes it
  (`vanguard.game.audio.BossCue`): track 22 once, track 18 added from sample 211 680 (4.8 s, three
  bars at 150 BPM, `Tracks.BOSS_WARNING_BARS_SECONDS`), both as 16-bit samples summed and clamped,
  8 bars of track 18, 1 s fade-out.
- `choir-descends-seam-final-r25-a.ogg`: listening aid, the last 8 s of track 18's loop followed by
  the first 8 s after `LOOPSTART`, as the looping stream plays the seam (0.5 s fades at both ends).
- `boss-music-final-r25-a.png` (`--review`): the waveforms of the three files and of the hand-off
  mix, with the loop points and the hand-off marked, and the measured numbers.

`--check` adds, for the warning, the hand-off the game makes: the hand-off sample is exactly three
bars at 150 BPM, track 18 starts on its own downbeat (first sample above −60 dBFS at sample 8, the
5 ms fade-in), the warning's tail under track 18 stays more than 30 dB below it, the summed stream
never clips, and the loudness step from the warning's last bar (up to its silent beat) into track
18's first bar stays within 4 LU. Measured: last bar −11.6 LUFS → first bar −14.9 LUFS (−3.3 LU:
the warning's build drops into the boss theme's quieter choir-and-drone intro after the silent
beat), silent beat 7 dB under the bar (the choir, string and timpani tails ring into it), tail
−39 dB under the track, peak after the hand-off −2.1 dBFS, no clamped samples. Track 18's seam
jump is 0.35 of the nearby sample-to-sample change; one-shot ends: −145 dBFS (22), −70 dBFS (24).

## music-final-r26-a — tracks 4, 5, 21, 23, 25 and 26 (production art)

Not a proposal of new music: the production files of the rest of the music the game plays, for the
M4 part H review (round 26; user decision D6 = A, "as they are"). `python3 tools/art/themes.py
afterburner afterburner-base coalition coalition-base miniboss complete failed gameover` renders the
chosen `afterburner-full-r08-a` (#4 "Afterburner"), `coalition-rising-full-r08-a` (#5 "Coalition
Rising"), `miniboss-sting-r08-a` (#21 "Contact Heavy"), `mission-complete-r08-a` (#23),
`mission-failed-r08-a` (#25) and `game-over-r08-a` (#26) again with their generator
(`tools/concept/audio/music_r08.py`, unchanged), and the base stems `afterburner-base-r15-a` and
`coalition-rising-base-r11-a` with theirs (`music_r15.py`, `music_r11.py`, unchanged; they stop
unless their full mix is the chosen one byte for byte), remuxes each stream with a `SOURCE` comment
into `assets/music/` and checks them. A base stem is checked against its full mix instead of
−14 LUFS: quieter than it (it carries the full mix's gain), same length, same `LOOPSTART` /
`LOOPLENGTH`.

The check failed only on the true peak of "Afterburner" (−0.5 dBTP) and its base stem (−0.8 dBTP),
limit −1 dBTP: inter-sample overs at single kick transients made by the Vorbis encoder. The pair is
rendered as `music_r15.py` renders it (both encodes proven identical to the chosen files), then one
gain envelope for both files dips only around the transients whose decoded 4x peak is above
−1.2 dB (aimed at −1.3 dB; raised-cosine 2 ms attack, 1 ms hold, 50 ms release; repeated until no
over is left) and both are encoded again at q6. Result: 57 dips, at most −1.1 dB, 3.1 s of 154.7 s;
−1.2 dBTP both, loudness and seams unchanged; the intended change is 55 dB under full scale (RMS),
16 dB under the encoder's own noise in the chosen file. The other six files are identical to the
chosen ones; only the comment is new.

- `afterburner-final-r26-a.ogg`, `afterburner-base-final-r26-a.ogg`, `coalition-rising-final-r26-a.ogg`,
  `coalition-rising-base-final-r26-a.ogg`, `miniboss-sting-final-r26-a.ogg`,
  `mission-complete-final-r26-a.ogg`, `mission-failed-final-r26-a.ogg`, `game-over-final-r26-a.ogg`:
  byte copies of the eight files in `assets/music/`, for the review board.
- `afterburner-seam-final-r26-a.ogg`, `afterburner-base-seam-final-r26-a.ogg`,
  `coalition-rising-seam-final-r26-a.ogg`, `coalition-rising-base-seam-final-r26-a.ogg`: listening
  aids, the last 8 s of each loop followed by the first 8 s after `LOOPSTART`, as the looping stream
  plays the seam (0.5 s fades at both ends).
- `music-final-r26-a.png` (`--review`): the eight files' waveforms with the intro (grey), loop
  (blue), fade tail (dark), the loop points (amber) and the one-shots (green), with the measured
  numbers.

## music-final-r30-a — track 6 and its base stem (production art)

Not a proposal of new music: the production files of track 6 "Homefront", Level 08's theme, for the
M5 part B review (round 30; user decision D6 = a: the chosen concept straight to production).
`python3 tools/art/themes.py homefront homefront-base` renders the chosen `homefront-full-r08-a`
again with its generator (`tools/concept/audio/music_r08.py`, `homefront_full`, unchanged) and the
base stem with the new frozen stem generator `tools/concept/audio/music_r30.py`, remuxes each stream
with a `SOURCE` comment into `assets/music/` and checks them (q6, −14 LUFS ±0.5 LU, true peak
≤ −1 dBTP, `LOOPSTART` / `LOOPLENGTH`, a click-free seam; the base stem quieter than the full mix,
with its length and loop points).

Base stem brief (`music_r30.py`, the round-11 method of `music_r11.py` / `music_r15.py`): one render
of `music_r08.homefront_full()`, same composition and seeds; the kicks and timpani are tagged on
their way into the "drums" and "perc" groups and also collected in "kick" and "timp" groups, and the
full mix's `mixdown()` call is captured, so the base stem is the same mixdown (each group's own
high-pass, sidechain duck, delay, reverb seed, widening and gain) over the base groups only. Kept:
the string / pluck 16th riff, the arpeggio, the bass, the string and choir pads (with the choir's
"oo" lines), the kicks, the timpani, the sirens and risers. Dropped: the lead synth, all brass (stabs,
hits and horn lines), the counter-melody strings and the breakbeat top (snares, ghost snares,
rolls, hats), taiko and crashes. Mastering is linked (each loudness pass and the encoder's peak
correction measure the full mix and apply its gain to both), so base + w × (full − base) crossfades
sample-aligned. The stem had no concept round: the generator writes `homefront-base-r30-a.ogg` only
into the temporary render directory, and proves its full mix is the chosen file byte for byte.

Measured (`themes.py --check homefront homefront-base`): `homefront.ogg` 140.82 s, loop 306000 +
5760000 samples (6.939 s + 130.612 s), −14.0 LUFS, −1.6 dBTP, seam 0.21, 3.14 MB, audio identical to
the chosen file; `homefront-base.ogg` same length and loop, −18.2 LUFS (−4.2 LU under the full mix),
−3.3 dBTP, seam 0.61, 2.97 MB. No fix was needed. Measured only, not listened to.

- `homefront-final-r30-a.ogg`, `homefront-base-final-r30-a.ogg`: byte copies of the two files in
  `assets/music/`, for the review board.
- `homefront-seam-final-r30-a.ogg`, `homefront-base-seam-final-r30-a.ogg`: listening aids, the last
  8 s of each loop followed by the first 8 s after `LOOPSTART`, as the looping stream plays the seam
  (0.5 s fades at both ends).
- `music-final-r30-a.png` (`--review`): both files' waveforms with the intro (grey), loop (blue),
  fade tail (dark) and the loop points (amber), with the measured numbers.

Outcome (user, 2026-10-07): both files approved as **final** in round 30.

## music-final-r31-a — track 7 and its base stem (production art)

Not a proposal of new music: the production files of track 7 "Firestorm", Level 09's theme (it also
serves Levels 11, 12 and 14's approach), for the M5 part C review (round 31; the chosen concept
straight to production, as "Homefront" in round 30). `python3 tools/art/themes.py firestorm
firestorm-base` renders the chosen `act2-b-theme-r08-a` again with its generator
(`tools/concept/audio/music_r08.py`, `firestorm`, unchanged) and the base stem with the new frozen
stem generator `tools/concept/audio/music_r31.py`, remuxes each stream with a `SOURCE` comment into
`assets/music/` and checks them (q6, −14 LUFS ±0.5 LU, true peak ≤ −1 dBTP, `LOOPSTART` /
`LOOPLENGTH`, a click-free seam; the base stem quieter than the full mix, with its length and loop
points).

Base stem brief (`music_r31.py`, the round-11 method of `music_r11.py` / `music_r15.py` /
`music_r30.py`): one render of `music_r08.firestorm()`, same composition and seeds; the kicks and
timpani are tagged on their way into the "drums" and "perc" groups and also collected in "kick" and
"timp" groups, and the full mix's `mixdown()` call is captured, so the base stem is the same mixdown
(each group's own high-pass, sidechain duck, delay, reverb seed, widening and gain) over the base
groups only. Kept: the reese bass (the acid bass in the tracker break), the 16th pluck figure, the
acid arpeggio, the string and choir pads (with the choir's "oo" melody in A and its storm stabs), the
storm's tremolo strings, the kicks, the timpani, the thunder, radio static and risers. Dropped: the
lead synth, all brass (orchestral stabs, the stab riff, horn and trumpet lines), the short string
stabs of the stab riff, the melody strings and the breakbeat top (snares, ghost snares, rolls, hats),
the storm toms and the crashes. Mastering is linked (each loudness pass and the encoder's peak
correction measure the full mix and apply its gain to both), so base + w × (full − base) crossfades
sample-aligned. The stem had no concept round; the generator proves its full mix is the chosen file
byte for byte. Per 8-bar section the base sits 1.7–5.9 LU under the full mix (−16.2 to −18.3 LUFS,
evener than the full mix's −11.3 to −15.8); the largest steps are the brass sections B and E.

True-peak fix (as round 26's "Afterburner"): the chosen full mix measured −0.2 dBTP and the base stem
−0.1 dBTP (limit −1 dBTP; 8 inter-sample overs in the full mix, 2 in the stem, made by the Vorbis
encoder); a plain gain cut of 0.9 dB would have taken the full mix to −14.9 LUFS. So the pair gets
one gain envelope for both files (`TRUE_PEAK_FIX` in `themes.py`) that dips only around those
transients, aimed at −1.3 dB, and both are encoded again at q6: 8 dips, at most −1.32 dB, 433 ms of
120.4 s below unity; loudness and seams unchanged. The decoded full mix differs from the chosen file
41 dB under the signal (the dips plus the re-encode's own codec noise; "Afterburner" 37 dB).

Measured (`themes.py --check firestorm firestorm-base`): `firestorm.ogg` 120.43 s, loop 321300 +
4838400 samples (7.286 s + 109.714 s, 64 bars), −14.0 LUFS, −1.3 dBTP, seam 0.63, 2.66 MB;
`firestorm-base.ogg` same length and loop, −17.7 LUFS (−3.6 LU under the full mix), −1.4 dBTP,
seam 0.52, 2.61 MB. Measured only, not listened to.

- `firestorm-final-r31-a.ogg`, `firestorm-base-final-r31-a.ogg`: byte copies of the two files in
  `assets/music/`, for the review board.
- `firestorm-seam-final-r31-a.ogg`, `firestorm-base-seam-final-r31-a.ogg`: listening aids, the last
  8 s of each loop followed by the first 8 s after `LOOPSTART`, as the looping stream plays the seam
  (0.5 s fades at both ends).
- `music-final-r31-a.png` (`--review`): both files' waveforms with the intro (grey), loop (blue),
  fade tail (dark) and the loop points (amber), with the measured numbers.

Outcome (user, 2026-10-07): both files approved as **final** in round 31.

# Round 36 — the Act 3 B theme (track 9), two sketches

Brief (M7 "Act 3 – Red Dust" design phase, step A-audio; pending the user's review in round 36): track 9,
the Act 3 B theme, "dusty mid-tempo techno, Vorne's motif as a faint layer", for Levels 18, 19 and the
Level 21 approach. Two contrasting a/b sketches of about a minute from
`tools/concept/audio/music_r36b.py` (deterministic, round-02 loop format: intro + loop + 2-bar fade tail,
`LOOPSTART` / `LOOPLENGTH`, −14 LUFS, −2 dBFS sample-peak ceiling). In both, Vorne's motif
(1 – ♭3 – 5 – 7) is its own group ("motif"), absent in the intro and the A section and entering at the
loop's bar 8, so the production can deliver it as a separate layer (Level 18 brings it in at the Ghost
Drones' first sight, Level 21's approach mutes it). A dust wind made periodic in the loop (no jump at the
seam) swells in each sketch's breakdown. Not listened to by Claude: checked by loudness, peaks, the seam
(the jump at the loop point against the nearby sample steps; about 1 or less is clean), the motif layer's
level against the mix (RMS per section) and spectrograms.

## act3-b-theme-r36-a — "Oxide Drift" (#9 Act 3 B, 65.2 s)

**Rejected** (round 36, 2026-10-13: b picked); the files are in `rejected/`.

**Brief**: track 8's Martian world slowed into techno: a muffled four-on-the-floor kick, a rolling 16th
bass (every 16th off the kick, the octave on the last), a filtered pluck sequencer on the chord tones
(its filter opening through the A section, closed in the breakdown, open in D, with a 16th arp over the
last bars), Martian hand drums (track 8's doum and tek) and shaker, and the lonely reed of "Red Dust Run"
back as a call over the B section and the end of D. A 4-bar dust breakdown (C) drops the kick for a sub
and a wind swell. Vorne's motif in cold organ (an octave down) and low brass far back (1.4 kHz low-pass,
4.5 s reverb at 60 %), A3 C4 E4 G♯4 at bars 8, 12, 16, 20 and 24: A harmonic minor holds his
minor-major-7th chord, and E phrygian dominant (track 8's scale) is its fifth mode, so the Ascendancy's
motif hides inside the Martian scale; its G♯ rings on over the F chord that follows. The layer sits
17.7 dB under the mix in B (14.6 in the breakdown, 17.2 in D).

**Tempo / key**: 126 BPM, A minor (harmonic) / E phrygian dominant. **Loop**: `LOOPSTART` 357000
(8.095 s), `LOOPLENGTH` 2352000 (53.333 s = 28 bars). **Chords**: A `Am Am F F Dm Dm E E`; B `Am F Dm E`
× 2; C `Am Am F E`; D `F G Am Am Dm E Am E`. **Measured**: −14.0 LUFS, peak −1.3 dBFS, true peak
−1.1 dBTP, seam 0.24.

**AI prompt**: *"Dusty mid-tempo techno level theme for a 1990s sci-fi shooter on Mars, 126 BPM, A
harmonic minor / E phrygian dominant, muffled four-on-the-floor kick, rolling sixteenth-note bass,
filtered pluck sequencer, Middle-Eastern hand drums and shaker, a lonely double-reed melody, desert
wind, a cold organ and low brass motif far in the background, hypnotic, instrumental, seamless loop."*

## act3-b-theme-r36-b — "Grey Signal" (#9 Act 3 B, 65.8 s)

**Chosen** (round 36, 2026-10-13).

**Brief**: dub techno: syncopated chord stabs (minor-ninth and major-seventh voicings on steps 4, 11 and
15 of each bar) through a dotted-8th ping-pong delay and a long reverb, their filter opening through
the loop; a deep sub bass, rimshots and swung 16th hats (open hats in D), the hand drums and shaker
from the B section on, a dark pad, the same looped dust wind swelling in the 4-bar breakdown (C:
stabs, pad and sub alone, a kick fill back in). Vorne's motif as an intercepted signal: a thin whistle
voice through a radio band-pass (700 Hz – 2.6 kHz) over a swell of static (band noise only, no
crackle), D4 F4 A4 C♯5 at bars 8, 12, 16, 20 and 24, its C♯ grinding against the C of the D minor-ninth
chord: a human transmission where none should be. The layer sits 19.8 dB under the mix in B and D
(17.3 in the breakdown).

**Tempo / key**: 125 BPM, D minor, 10 % swing. **Loop**: `LOOPSTART` 359856 (8.160 s), `LOOPLENGTH`
2370816 (53.760 s = 28 bars). **Chords**: A, B and D `Dm9 Dm9 B♭maj7 B♭maj7 Gm9 Gm9 A7(♭9) A7(♭9)`; C
`Dm9 Dm9 B♭maj7 A7(♭9)`. **Measured**: −14.0 LUFS, peak −1.4 dBFS, true peak −1.0 dBTP, seam 0.57.

**AI prompt**: *"Dusty dub techno level theme for a 1990s sci-fi shooter on Mars, 125 BPM, D minor,
syncopated minor-ninth chord stabs with ping-pong delay and long reverb, deep sub bass, rimshots and
swung hi-hats, hand drums, desert wind, a faint whistled four-note motif heard through radio static
like an intercepted transmission, hypnotic, instrumental, seamless loop."*

## music-final-r38-a — track 8 and its provisional base stem

Brief (M7 part B, step AUD-B; pending the user's review in round 38): track 8 "Red Dust Run" (Act 3 A:
Levels 15–17 and 20) at full length, as the [*Act 3 music*](../README.md#track-8-red-dust-run-in-production-m7-part-b)
section asks: a full-length mix of about 3:00 from the chosen round-02 sketch
([mars-theme-r02-a](mars-theme-r02-a.ogg), 135 BPM, E phrygian dominant, 53.8 s), checked against the final
spec (OGG Vorbis q6, −14 LUFS ±0.5 LU, true peak ≤ −1 dBTP, `LOOPSTART` / `LOOPLENGTH`, a `SOURCE`
comment), plus a base stem from the same render. `python3 tools/art/themes.py red-dust red-dust-base`
renders both with the new generator `tools/concept/audio/music_r38.py` and checks them.

Composition (`music_r38.py`): the sketch's 4-bar intro (wind, drone, hand drums, the reed's call), then a
96-bar loop of twelve 8-bar sections on its material: A (the sketch's A melody over E–F–E–Dm–Am–F–Dm–E),
B (the Andalusian cadence Am–G–F–E with the 16th arpeggio and the B melody), **D "caravan"** (new: half
time, the hand drums forward, a low patient reed melody over an E pedal), A2 (the A melody an octave
down over the arpeggio), C (the sketch's dust storm: muffled kit, the wind swelling, the call; then its
build), **E bridge** (new: Dm–Am–F–G–E, a high open reed melody, full kit), B2 (the B melody, strings
doubling it an octave under), F desert (no kit: hand drums, wind, drone and the call alone), G build (the
kick, then the hats, then the full kit return, the arpeggio's filter opening, the A melody's second
half), B3 (the climax), A3 (the A melody with the strings under it) and T (the call answered and falling
home, a snare roll into the loop start). The sketch's chains and seeds are kept; new are the drone (an E
pedal in low strings, an A pedal under the cadence) and the wind made loop-periodic (its last 0.5 s
cross-faded into its first, so the seam is clean). Section loudness of the full mix: −12.9 (B3) to
−16.7 LUFS (F, the desert), the intro −16.0.

Base stem: one render, the base groups through the full mix's own chains (every chain linear, so the full
mix is exactly the sum of its groups), linked mastering as in `music_r11.py`: kept the hand drums and
shaker, the reese bass, the wind, the drone, the string pads and the kicks; dropped the snares, hats,
toms and crashes (the breakbeat top), the reed and the strings' melody, and the 16th arpeggio. Per section
the base sits 1.6 (C, the storm) to 3.3 LU (B3) under the full mix (−16.0 to −19.3 LUFS). **Provisional**: the music README
wants the full mix reviewed before the stem, but Level 15 plays the stem from section 2, so it is
rendered now and re-rendered from the same generator if the review changes the full mix.

Measured (`themes.py --check red-dust red-dust-base`): `red-dust-run.ogg` 181.78 s, loop 333200 +
7526400 samples (7.556 s + 170.667 s), −14.0 LUFS, −1.6 dBTP, seam 0.28, 4.08 MB; `red-dust-run-base.ogg`
same length and loop, −16.8 LUFS (−2.8 LU under the full mix), −2.5 dBTP, seam 0.17, 4.22 MB. No
true-peak dips were needed (the pair goes through the same envelope pass as "Firestorm", `PAIR_RENDERED`).
Measured only, not listened to.

- `red-dust-run-final-r38-a.ogg`, `red-dust-run-base-final-r38-a.ogg`: byte copies of the two files in
  `assets/music/`, for the review board.
- `red-dust-run-seam-final-r38-a.ogg`, `red-dust-run-base-seam-final-r38-a.ogg`: listening aids, the last
  8 s of each loop followed by the first 8 s after `LOOPSTART` (0.5 s fades at both ends).
- `music-final-r38-a.png`: both files' waveforms with the intro (grey), loop (blue), fade tail (dark) and
  the loop points (amber), with the measured numbers.

# Round 40 — track 29 "Debrief", two proposals

Brief (step MUS-D, the user's request of 2026-10-18; pending the user's review in round 40): the debrief has
only the 6 s mission complete jingle (track 23); a quiet music loop should carry on under the debrief after
it. New track 29 "Debrief" (P2): 60–90 s with intro + seamless loop in the round-02 format (`LOOPSTART` /
`LOOPLENGTH`, a 2-bar fade tail), −18…−20 LUFS so it sits under the debrief's sounds and voice, in D major
(the jingle's last chord, humanity's motif in its major form) so it follows the jingle; calm and a little
proud, not the hangar theme "Dry Dock" (swung 90 BPM D dorian, FM electric piano, flute, vibes) again.
`tools/concept/audio/music_r40.py` renders both (deterministic, mastered to −19 LUFS with a −3 dBFS
ceiling) and a 15 s listening aid each: the jingle (`mission-complete-final-r26-a.ogg`, the game's file),
then the loop file from 5.714 s (three jingle bars at 126 BPM: the downbeat after its final chord, while it
rings out), both at their mastered levels as the game would play them. Not listened to by Claude: checked
by loudness (whole file, intro, per section), peaks, the loop seam (the jump at the loop point against the
nearby sample steps; about 1 or less is clean), a band balance against the chosen hangar and briefing
themes, and the levels across the handover.

Band balance (dB of the total energy, 20–60 / 60–250 / 250–2k / 2–6k / 6–16k Hz): "Dry Dock" full −8.2 /
−2.8 / −5.1 / −19.8 / −26.3; "Situation Room" −7.0 / −4.9 / −3.4 / −16.7 / −27.2.

## debrief-theme-r40-a — "Homeward" (#29 Debrief, 77.1 s)

**Brief**: warm synth-orchestral, no beat. A 2-bar intro holds the jingle's D major chord on in strings and
a soft "oo" choir, the harp coming in on its second bar. The 16-bar loop: slow string pads (0.6 s attack)
over a low synth pad, low strings and a quiet sub on the roots, a soft harp (decaying partials, a 6 ms
attack, no pluck transient) in rising eighths. A half: humanity's motif slow on a solo French horn, D4 A4 D5
E5 F♯5 over D – Bm, an answer (E5 D5 B4, C♯5), the motif again climbing on to G5 A5, then down to E5 over
the A. B half: the choir joins the pads, a solo string counter-melody over G – A – F♯m – Bm, the horn's
8-9-10 fragment (D5 E5 F♯5) over G – D/F♯ and a closing phrase back to the loop start; one soft timpani
note and a quiet cymbal swell mark the B half.

**Tempo / key**: 63 BPM (half the jingle's 126, so a bar of the loop is two of the jingle), D major.
**Loop**: `LOOPSTART` 378000 (8.571 s), `LOOPLENGTH` 2688000 (60.952 s = 16 bars). **Chords**: A
`D Bm G A D Bm Em7 Asus–A`; B `G A F♯m Bm G D/F♯ Em7 Asus–A`. **Measured**: −19.0 LUFS (intro −21.1, A
−18.4, B −19.1), peak and true peak −7.1 dB, seam 0.37; bands −9.2 / −5.8 / −2.2 / −19.1 / −39.0 dB:
mid-weighted and darker above 6 kHz than the references (no hats; leaves room above the voice). Join: the
jingle's last 2 s −12.6 LUFS, the loop's first seconds −22.1 LUFS (the intended step down from the −14
jingle to the −19 bed), no hole at the handover (the quietest 0.4 s −28.9 dBFS RMS), peak −1.9 dBFS.

**AI prompt**: *"Quiet, warm synth-orchestral loop for the mission debrief of a 1990s sci-fi shooter, 63
BPM, D major, slow string and soft choir pads, a gentle harp in eighths, a solo French horn playing a
slow rising five-note heroic motif, a solo violin counter-melody, calm and a little proud, no drums,
instrumental, seamless loop."*

## debrief-theme-r40-b — "Stand Down" (#29 Debrief, 80.7 s)

**Brief**: laid-back electronic, straight (no swing). A 2-bar intro: the Dmaj9 pad swelling, the eighth
pulses fading in with their filter opening, the sub on the second bar. The 24-bar loop: maj9 / m9 pads
pumping gently on a soft kick (beats 1 and the "and" of 3), eighth-note pulses on the chord tones (a soft
pulse-and-triangle voice, 8 ms attack, a low-pass with no resonance, so nothing pops) through a
dotted-eighth ping-pong delay, a round sub bass, a brushed snare (band noise, no tonal body) on 2 and 4,
closed eighth hats and a shaker. A: the groove, the pulses' filter opening. B: the motif hinted on a soft
glass lead (sine with a fading octave FM sheen), 5-8-9-10 (A4 D5 E5 F♯5) and a falling answer. C: no kit
(the shaker alone), the pads up, the motif in full once (D4 A4 D5 E5 F♯5, then E5 D5 C♯5 A4, D5) and its
hint an octave down; the kick comes back under the last bar.

**Tempo / key**: 84 BPM, D major. **Loop**: `LOOPSTART` 283500 (6.429 s), `LOOPLENGTH` 3024000 (68.571 s
= 24 bars). **Chords**: A and B `Dmaj9 Dmaj9 Bm9 Bm9 Gmaj9 Gmaj9 Em9 A11`; C `Gmaj9 A6 F♯m7 Bm9 Gmaj9 A6
Em9 A`. **Measured**: −19.0 LUFS (intro −21.1, A −19.9, B −18.6, C −17.9), peak and true peak −5.6 dB,
seam 0.80; bands −6.7 / −4.6 / −3.6 / −24.1 / −30.1 dB: a little more sub than the references, softer in
2–6 kHz. Join: the jingle's last 2 s −12.6 LUFS, the loop's first seconds −21.4 LUFS, the quietest 0.4 s at
the handover −27.0 dBFS RMS, peak −1.9 dBFS.

**AI prompt**: *"Laid-back downtempo electronic loop for the mission debrief of a 1990s sci-fi shooter, 84
BPM, D major, warm major-ninth pads gently pumping on a soft kick, soft eighth-note synth pulses with
ping-pong delay, round sub bass, brushed snare and shaker, a glassy lead hinting at a rising heroic
motif, calm and a little proud, instrumental, seamless loop."*

- `debrief-join-r40-a.ogg`, `debrief-join-r40-b.ogg`: listening aids, the jingle → each loop, 15 s (1 s fade
  out).

## music-r41-a — track 9 "Grey Signal" and its three stems

Brief (M7 part E, step MUS-E; pending the user's review in round 41): track 9 "Grey Signal" (Act 3 B:
Levels 18, 19 and the Level 21 approach) at about 3:00 from the chosen round-36 sketch
[act3-b-theme-r36-b](act3-b-theme-r36-b.ogg), as the music README's
[*Track 9 brief*](../README.md#track-9-the-act-3-b-theme-brief) asks: 125 BPM, D minor, 10 % swing, dusty dub
techno, Vorne's motif as an intercepted radio whistle; three stems (base, full, motif) from one render so
they line up; −14 LUFS, true peak ≤ −1 dBTP, intro + seamless loop with `LOOPSTART` / `LOOPLENGTH`; no
bubbly or popping layers. Generator `tools/concept/audio/music_r41.py` (deterministic, ~5 min); it follows
how `music_r38.py` built track 8 and its base stem.

Composition: the sketch's 4-bar intro (wind, the stabs closed and opening, the sub, the kick from bar 2),
then an 88-bar loop of eleven 8-bar sections on `Dm9 Dm9 B♭maj7 B♭maj7 Gm9 Gm9 A7(♭9) A7(♭9)`: A (the
groove: kick, sub, rimshots, swung 16th hats, the stabs opening 800 → 1500 Hz), B (+ track 8's hand drums),
B2 (+ a fourth stab, the pad up), C (the breakdown, `Dm9 Dm9 B♭maj7 A7(♭9)` twice: stabs, pad and held sub
alone, the wind swelling, the hand drums and a kick fill back in), D and D2 (the full groove, open hats,
the stabs wide open), E (new, "dust": `Gm9 Gm9 Dm9 Dm9 B♭maj7 B♭maj7 A7 A7`, the kit through a 700 Hz
low-pass as track 8's storm, no hats, the hand drums forward, held sub, closed stabs, the wind swelling),
F (the build: kick, then rimshots, then hats; the stabs opening; a riser and a rimshot roll), D3 (the
climax), A3 (the groove with hand drums) and T (the turn: open hats and hand drums out, the stabs closing
back to A's 800 Hz). The sketch's voices, chains and seeds, except the chord stab: resonance q 1.1 (the
sketch 1.8) and an 80 ms filter envelope (50 ms), so it cannot read as a resonant bloop, and its mix gain
1 dB up to keep the sketch's 250 Hz – 2 kHz balance (measured per section against the sketch).

Stems (the round-11 method): one render, every group through the full mix's own chain, linked mastering
(the full mix's gain for all three files, the encoder's peak correction too), so they share length, gain
and loop comments: **base** kick, sub, pad, shaker and hand drums, the wind; **full** the base plus the
hats, rimshots, stabs, crashes and risers; **motif** Vorne's motif alone (D4 F4 A4 C♯5 on the sketch's
whistle through the 700 Hz – 2.6 kHz radio band-pass over a 2-bar static swell, no crackle), one statement
on each section's first D minor bar (≈ 15.4 s apart) and two more in the breakdown and the climax, none
in the intro; the game adds it on top of the base/full crossfade. Its level is set by measurement: the
statements 19 dB under the full mix in the same 2-bar windows, 17 dB in the breakdown.

**Measured** (not listened to): full mix 180.72 s, `LOOPSTART` 349272 (7.920 s, step 2 of the loop's
first bar), `LOOPLENGTH` 7451136 (168.960 s = 88 bars), −14.0 LUFS, −1.4 dBTP, seam 0.50; base stem −14.4
LUFS (−0.4 LU: the kick, sub and pad carry most of the loudness, the split is the stabs, hats and
rimshots), −1.8 dBTP, seam 0.32; motif stem −33.2 LUFS, −21.2 dBTP, seam 0.04, its statements 18.4–18.8 dB
under the full mix (16.8 in the breakdown); full + motif summed −14.0 LUFS, −1.3 dBTP. All three the same
length and loop points. Loudness per section in the sheet.

**AI prompt**: *"Dusty dub techno level theme for a 1990s sci-fi shooter on Mars, 125 BPM, D minor,
syncopated minor-ninth chord stabs with ping-pong delay and long reverb, deep sub bass, rimshots and swung
hi-hats, Middle-Eastern hand drums and shaker, a dark pad, desert wind swelling in the breakdowns, a faint
whistled four-note motif heard through radio static like an intercepted transmission, hypnotic, about
three minutes, instrumental, seamless loop."*

- `grey-signal-r41-a.ogg`, `grey-signal-base-r41-a.ogg`, `grey-signal-motif-r41-a.ogg`: the full mix and
  its two stems.
- `grey-signal-seam-r41-a.ogg`, `grey-signal-base-seam-r41-a.ogg`, `grey-signal-motif-seam-r41-a.ogg`:
  listening aids, the last 8 s of each loop followed by the first 8 s after `LOOPSTART` (0.5 s fades).
- `grey-signal-motif-mix-r41-a.ogg`: listening aid, the full mix with the motif stem on top, loop bars
  17–40 (46.1 s).
- `music-r41-a.png`: review sheet: waveforms with intro (grey), loop (blue), fade tail (dark) and loop
  points (amber), the measured numbers and a per-section loudness table.

# Round 48 — the Act 4 B theme (track 11) and the reveal sting (track 30)

Brief (M8 "Act 4 – Deep Water" design phase, step AUD48; decided in round 48 (2026-10-22)): track 11, the Act 4
B theme, "muffled ambient drum & bass, Vorne's motif as a faint layer" for Level 25 and Levels 27–28, and a new
one-shot, the reveal sting, for Level 28's Ascendancy cruisers. Synthesized (own score) by
`tools/concept/audio/music_r48.py` (deterministic). Round 48's Q12 a: **no bubble layer** (the round-02 A theme's
`bubble` hits are not used), no resonant short plucks (the arpeggio is a soft sine-and-triangle voice, the stabs at
q 1.0), soft attacks on every voice but the drums. The themes in the round-02 loop format (intro + loop + 2-bar fade
tail, `LOOPSTART` / `LOOPLENGTH`, −14 LUFS target, −2 dBFS sample-peak ceiling), mastered before the assembly with
the next loop pass appended so the limiter's gain matches across the loop point; Vorne's motif (1 – ♭3 – 5 – 7) in
its own group, from the loop's C section on, measured in its 2-bar statements against the whole mix (Act 3's
target: 17 dB under in the breakdown, 19 in the full sections). The stings in the round-08 sting format (−14 LUFS
target, −2 dBFS ceiling, a 1.2 s fade at the end), the hit at 4.0 s (the cruisers rise for 4 s, then their gun
ports open). Not listened to by Claude: checked by loudness, peaks, the seam and spectrograms.

## act4-b-theme-r48-a — "Pressure Line" (#11 Act 4 B, 52.5 s)

**Proposed** (round 48).

**Brief**: the A theme's world, deeper: liquid ambient drum & bass. A two-step break (kick on 1 and the "and" of 3,
snares on 2 and 4, ghost snares) under a water low-pass that breathes with the sections and never opens fully
(1.1–3.2 kHz), a deep reese on the roots and a sub, the A theme's wide pads, a soft lamp-like arpeggio of eighths
(sine and triangle, a 4 ms attack) through a dotted-8th delay, the A theme's sonar ping (softened) every two bars, a
slow flute line in B and an octave up in D, far synthesized whale calls in the drumless breakdown C, and a deep
current of low noise made periodic in the loop. Vorne's motif: track 9's intercepted whistle (F4 A♭4 C5 E5) through
the radio band-pass and then a 1.8 kHz low-pass with a 4.5 s reverb: the Act 3 signal, now heard through water; at
bars 16 and 20 (C) and 24 and 28 (D), 16.3 and 16.2 dB under the mix in C, 19.8 and 20.0 in D.

**Tempo / key**: 175 BPM, F minor. **Loop**: `LOOPSTART` 257040 (5.829 s), `LOOPLENGTH` 1935360 (43.886 s = 32
bars). **Chords**: A and C `Fm9 Fm9 D♭maj7 D♭maj7 B♭m9 B♭m9 Csus C`; B and D `Fm9 Fm9 D♭maj7 A♭ B♭m9 B♭m9 Csus C`.
**Measured**: −14.3 LUFS, peak −1.9 dBFS, true peak −1.9 dBTP, seam 0.28.

**AI prompt**: *"Muffled liquid drum and bass level theme for a 1990s sci-fi shooter under the ice of Europa,
175 BPM, F minor, a rolling two-step breakbeat under a low-pass filter as if heard underwater, deep reese bass and
sub, wide warm pads, a soft echoing arpeggio, distant sonar pings and whale calls, a slow breathy flute melody, a
faint whistled four-note motif through radio static, no bubbles, instrumental, seamless loop."*

## act4-b-theme-r48-b — "Black Current" (#11 Act 4 B, 58.7 s)

**Proposed** (round 48).

**Brief**: half-time deep drum & bass, darker and roomier, closer to Level 26's dark: a heavy kick with a sub tail on
1 with two broken pick-ups, one snare a bar on 3 through a long dark reverb, rolling 16th shakers (closed hats on the
off-steps in D), a growling reese swelling over each pair of bars, dub chord stabs (low resonance) on steps 4, 7 and
12 through a long ping-pong echo, their filter opening through the loop, a dark pad and a low pressure drone looped
with the loop; a drumless breakdown C with a kick fill back in. Vorne's motif: not a signal but the machines
themselves: cold organ (an octave down) and low brass, C3 E♭3 G3 B3, under a 900 Hz low-pass and a 5 s reverb, far
behind the hull; at bars 16 (C), 20 and 24 (D), 17.5 dB under the mix in C, 18.4 and 17.7 in D.

**Tempo / key**: 140 BPM (half-time feel), C minor. **Loop**: `LOOPSTART` 321300 (7.286 s), `LOOPLENGTH` 2116800
(48.000 s = 28 bars). **Chords**: A, B and D `Cm9 Cm9 A♭maj7 A♭maj7 Fm9 Fm9 G7(♭9) G7(♭9)`; C `Cm9 Cm9 A♭maj7
G7(♭9)`. **Measured**: −14.3 LUFS, peak −1.8 dBFS, true peak −1.8 dBTP, seam 1.72 (the render itself is
sample-periodic across the loop point, checked before the encoding; the step is the Vorbis encoder's error at the
loop start, where the shakers and the kick's tail run; to be rechecked in the production render).

**AI prompt**: *"Dark half-time drum and bass level theme for a 1990s sci-fi shooter in the black depths of an
alien ocean, 140 BPM, C minor, heavy kick and one cavernous snare per bar, rolling shakers, a growling reese bass
swelling and falling, dub chord stabs with long ping-pong echo, a pressure drone, a cold organ and low brass motif
far in the background as if heard through a submarine hull, no bubbles, instrumental, seamless loop."*

## reveal-sting-r48-a — "Black and Gold" (#30 Reveal sting, 9.0 s)

**Proposed** (round 48).

**Brief**: orchestral, B minor (the motif's home key, Act 5's "Hollow Rock"). 0–4 s, the cruisers rising: low
tremolo strings (B1 F♯2 B2), an organ pedal and a timpani roll swelling with the square of time, a noise riser; the
whole starts muffled (0.7 kHz) and opens over the last second as the water lifts (the under-water grade lifts over
1 s at the breakout). 4.0 s, the gun ports: a brass-and-organ tutti whose notes enter one by one in the motif's
rhythm (B on the beat, D a dotted quarter later, F♯ an eighth after, A♯ on the next bar), each with its timpani and a
taiko and crash on the first, so the motif arrives stacked into its minor-major-seventh chord instead of as a tune;
the A♯ is left on top, unresolved, as the low strings fall away. **Measured**: 9.0 s, −15.0 LUFS, peak −3.1 dBFS.

**AI prompt**: *"A nine-second orchestral reveal sting for a 1990s sci-fi game: low tremolo strings, an organ pedal
and a rolling timpani crescendo rising from muffled to open for four seconds, then a cold brass and organ chord
built note by note, B minor with a dissonant major seventh left hanging, ominous, black and gold warships rising
through ice."*

## reveal-sting-r48-b — "Ours" (#30 Reveal sting, 9.0 s)

**Proposed** (round 48).

**Brief**: the signal comes true. 0–4 s: track 9's thin intercepted whistle plays the motif (B4 D5 F♯5 A♯5) through
the radio band-pass (700 Hz – 2.6 kHz) out of a static swell, while a sub on B1 swells under it with a low brass
cluster (B1 C2 F♯2 A♯2, muffled at 900 Hz) and a riser. 4.0 s: a deep impact (a falling 70 → 32 Hz boom, a 3 ms
attack) and a taiko; the radio band-pass opens over 0.6 s into full range, and low brass (two octaves) and organ play
the motif's last two notes, F♯ then A♯, the A♯ held over a B-minor drone (organ B0 B1 F♯2, slow strings B1 D2 F♯2):
the voice heard on Mars was the fleet. **Measured**: 9.0 s, −14.5 LUFS, peak −2.4 dBFS.

**AI prompt**: *"A nine-second sci-fi reveal sting: a thin whistled four-note melody heard through radio static over
a rising sub-bass and low brass cluster, then a deep impact and the melody's last two notes blasting out in full low
brass and organ over a dark B minor drone, unresolved, the moment the enemy fleet is revealed to be human."*

# Round 51 — the Act 4 themes at full length (tracks 10 and 11)

Brief (M8 part B, step AUD-B; accepted in round 51 (2026-10-22)): the two Act 4 themes at about 3:00 with their
stems, as the music README's [*Act 4 music*](../README.md#act-4-music-draft-round-48) asks: −14 LUFS, true peak
≤ −1 dBTP, intro + seamless loop with `LOOPSTART` / `LOOPLENGTH`, every stem from the same render (linked mastering:
the full mix's gain for all of a track's files) so they play sample-aligned; **no bubbles, no bloopy plucks, nothing
that pops** (round 48, Q12 a). Generator `tools/concept/audio/music_r51b.py` (deterministic, ~6 min a track); it
follows `music_r38.py` (track 8 from a round-02 sketch) and `music_r41.py` (track 9's three stems).

## thera-deep-r51-a — track 10 "Thera Deep" and its three stems

From the chosen round-02 sketch [europa-theme-r02-a](europa-theme-r02-a.ogg) (125 BPM, F minor). Composition: the
sketch's 4-bar intro (pad, sonar, a far whale, the bass stirring) and an 88-bar loop of eleven 8-bar sections on its
two progressions, A = `Fm9 Fm9 D♭ D♭ B♭m9 B♭m9 Csus C` and B = `D♭ E♭ Fm9 Fm9 D♭ E♭ Csus C`: A (four-on-the-floor
kick, off-beat hats, the rolling 16th bass, the arpeggio), B (+ claps, the melody on lead and low flute, the water
opening 1.8 → 4.8 kHz), A2 (the groove at 2.4 kHz, the flute's echo phrase twice), C (the sketch's deep breakdown:
no kit, held sub, two whale calls, the flute echo; the kit back under a 4-bar riser), D (surfacing: the melody
again), E (new, "drift": `B♭m9 B♭m9 Fm9 Fm9 D♭ D♭ Csus C`, the kick alone, a quiet held sub, a new low flute melody,
one whale call, the water at 1.3 kHz), F (the build: kick, then hats and bass, then claps, the water opening), B2
(the climax), A3 (the echo phrase an octave up), C2 (deep again, six bars without the kit, two whale calls) and T (the
turn: the hats out, the water closing into A). Changes to the sketch's sound: the `bubble` hits are left out, the
sonar ping has round 48's softer 6 ms attack, and the arpeggio's pluck has its filter resonance lowered (q 1.4, the
sketch's 3.0) with an 80 ms filter envelope (50 ms) so it cannot read as a bloop.

Stems: **base** kick, bass, pad, the sonar pings and the whale calls (Level 22 from section 2, Levels 23–24);
**full** everything (Level 22 from section 3); **dark** (Level 26's darkness, the third lockstep stream played `alone`
from t=8–18): the sonar pings, the same pad through the same water but without the kick's ducking (alone it must not
pump), and an F1 + C2 pedal drone (a dark pad at 300 Hz, only in this stem).

**Measured** (not listened to): 180.72 s, `LOOPSTART` 349272 (7.920 s, step 2 of the loop's first bar, an off-beat),
`LOOPLENGTH` 7451136 (168.960 s = 88 bars); full −14.0 LUFS, −1.7 dBTP, seam 0.29; base −14.9 LUFS (−0.9 LU: the
kick, bass and pad carry most of the loudness), −2.6 dBTP, seam 0.47; dark −18.1 LUFS (−4.2 LU), −6.3 dBTP, seam
0.30. Each stem's source is periodic to the bit across the loop (checked before encoding). Loudness per section in
the sheet.

**AI prompt**: *"Muffled ambient trance level theme for a 1990s sci-fi shooter under the ice of Europa, 125 BPM,
F minor, four-on-the-floor kick and off-beat hats under a slowly moving low-pass like deep water, rolling sixteenth
bass, warm wide pads, an echoing soft arpeggio, a slow lead and flute melody, sonar pings with long echoes, distant
whale calls in the breakdowns, no bubbles, about three minutes, instrumental, seamless loop."*

- `thera-deep-r51-a.ogg`, `thera-deep-base-r51-a.ogg`, `thera-deep-dark-r51-a.ogg`: the full mix and its two stems.
- `thera-deep-seam-r51-a.ogg`, `thera-deep-base-seam-r51-a.ogg`, `thera-deep-dark-seam-r51-a.ogg`: listening aids,
  the last 8 s of each loop followed by the first 8 s after `LOOPSTART` (0.5 s fades).
- `thera-deep-dark-join-r51-a.ogg`: listening aid, loop bars 1–16, the full mix crossfading over 2 s into the dark
  stem alone at bar 9 (30.7 s).
- `thera-deep-sheet-r51-a.png`: review sheet: waveforms with intro (grey), loop (blue), fade tail (dark) and loop
  points (amber), the measured numbers and a per-section loudness table.

**Chosen** (round 51, M.1, 2026-10-22: accepted as proposed).

## pressure-line-r51-a — track 11 "Pressure Line" and its three stems

From the round-48 sketch [act4-b-theme-r48-a](act4-b-theme-r48-a.ogg) (picked in round 48; 175 BPM, F minor), the
sketch's voices, chains and seeds unchanged (no bubble layer, the soft lamp arpeggio). Composition: its 4-bar intro
and a 128-bar loop of sixteen 8-bar sections on A = `Fm9 Fm9 D♭maj7 D♭maj7 B♭m9 B♭m9 Csus C` and B = `Fm9 Fm9
D♭maj7 A♭ B♭m9 B♭m9 Csus C`: A (the two-step break, reese and sub, the arpeggio), B (+ open hats, the slow flute
line), A2 (the flute an octave down), C (the breakdown: drums out until bar 7, two whale calls, the deep current
swelling), D (full, the flute an octave up), E (new, "pressure": half time, kick on 1 and snare on 3, the reese
held, the arpeggio in quarters, a new low flute melody), B2, D2 (the flute at pitch and an octave up), F (a drumless
deep section, the new melody high and far, two whale calls, a ghost-snare fill out), G (the build: kick, snares,
ghosts and hats, open hats, a riser), D3 (the climax, the water at its most open, 3.2 kHz, never fully), A3, E2
(pressure again, the new melody an octave down), B3, D4 and T (the turn: the hats out, a ghost-snare roll, the
water closing into A).

Stems: **base** the kick and backbeat snares, sub, pad, whale calls, sonar pings and the deep current; **full** the
base plus the ghost snares, hats, crashes, reese, arpeggio, flute and risers; **motif** Vorne's motif alone (F4 A♭4 C5
E5 on track 9's whistle through the 700 Hz – 2.6 kHz radio band-pass, a 1.8 kHz low-pass and a 4.5 s reverb over a
2-bar static swell: the same intercepted signal, now under water), on each section's first bar (≈ 11 s apart) and
on bar 5 of the breakdowns C and F and the climax D3, none in the intro; not part of the full mix (the game adds it
as the third stream in Levels 27–28, muted in Level 25). Its level is set by measurement: 19 dB under the full mix in
the same 2-bar windows, 16 dB in the breakdowns.

**Measured** (not listened to): 183.94 s, `LOOPSTART` 249480 (5.657 s, step 2 of the loop's first bar),
`LOOPLENGTH` 7741440 (175.543 s = 128 bars); full −14.0 LUFS, −1.8 dBTP, seam 1.21 (the loop point falls on a
16th hat's onset, so the step there is the music's own; the source is periodic); base −15.6 LUFS (−1.6 LU), −2.0
dBTP, seam 1.00; motif −33.8 LUFS, −23.9 dBTP, seam 0.35, its statements 18.0–19.9 dB under the full mix (15.5–16.1
in the breakdowns); full + motif summed −14.0 LUFS, −1.6 dBTP. Loudness per section in the sheet.

**AI prompt**: *"Liquid ambient drum and bass level theme for a 1990s sci-fi shooter deep under Europa's ice, 175 BPM,
F minor, a rolling two-step break under a water-like low-pass that breathes but never opens fully, deep reese and
sub bass, warm wide pads, a soft lamp-like arpeggio through a delay, sonar pings, a slow breathy flute, distant whale
calls and a deep current in the breakdowns, a faint whistled four-note motif like an intercepted transmission heard
through water, no bubbles, about three minutes, instrumental, seamless loop."*

- `pressure-line-r51-a.ogg`, `pressure-line-base-r51-a.ogg`, `pressure-line-motif-r51-a.ogg`: the full mix and its
  two stems.
- `pressure-line-seam-r51-a.ogg`, `pressure-line-base-seam-r51-a.ogg`, `pressure-line-motif-seam-r51-a.ogg`:
  listening aids, the last 8 s of each loop followed by the first 8 s after `LOOPSTART` (0.5 s fades).
- `pressure-line-motif-mix-r51-a.ogg`: listening aid, the full mix with the motif stem on top, loop bars 25–48 (C, D,
  E; 32.9 s).
- `pressure-line-sheet-r51-a.png`: review sheet, as track 10's (the third column the motif against the full mix).

**Chosen** (round 51, M.2, 2026-10-22: accepted as proposed).

## thera-deep-warm-r55-a / -b — track 10's warm pad (Level 26 from t=168)

Proposed (round 55, M8 part F, step AUD-F), synthesized by `tools/concept/audio/sfx_r55f.py` (the music tool: the
voices of `music.py` and `music_r02.py` on `music_r51b.py`'s track-10 grid; no Freesound source). Use: Level 26's
aftermath: the music is out at t=160 (Rook's loss), and from t=168 "the vent field's warm pad creeps in" to the level's
end (t≈182; gap check F12). Each file is a fourth stem of track 10 on the same grid (180.72 s, `LOOPSTART` 349272,
`LOOPLENGTH` 7451136, sample-equal to the other stems), so the game can play it through the third lockstep stream as
it plays the dark stem (`alone`), faded in over about 10 s. Levelled to −20 LUFS over the loop, 2 LU under the dark
stem: it sits under the aftermath's radio lines (t=165, 171, 176).

- **a, "the dark stem's pad opened"**: the dark stem's own pad (the `v_pad` supersaw, the same chords, unducked)
  brighter (its filter at 3200 Hz, the dark stem's 1600) through the water low-pass held open at 4800 Hz (the dark
  stem's moves 1300–4800 Hz with the sections), its F1 + C2 pedal drone quieter and warmer (900 Hz, the dark stem's
  300), no sonar pings: the same music as the dark, come up into warmer water. Measured: −20.0 LUFS, −8.2 dBTP,
  seam 0.30; spectral centroid 438 Hz (the dark stem's 334).
- **b, "a synth pad"**: a new warm voice, two detuned saws (±0.3 %) and a triangle through a soft 1400 Hz low-pass
  (q 0.6) with a 1.4 s attack, 1.5 s release and a slow 0.3 Hz vibrato, over a sine sub, on a warm 8-bar progression
  in D♭ (the A theme's ♭VI: D♭maj9, A♭/C, B♭m9, G♭maj9, two bars each, eleven times round the loop), a 5 s reverb:
  rounder and darker than a, a new colour after the silence. Measured: −20.0 LUFS, −9.0 dBTP, seam 0.77; centroid
  215 Hz.
- `thera-deep-warm-join-r55-a.ogg`, `thera-deep-warm-join-r55-b.ogg`: listening aids, Level 26 from t=146 to 186 as
  the plan plays it: the dark stem (6 dB down from 150, out at 160), silence, the pad creeping in from 168 over 10 s.

**AI prompt** (if replaced by a musician): *"A warm, slow synth pad for the end of a dark underwater level, after a
silence: soft detuned analog pad swelling in over ten seconds, D-flat major colours, a low sine sub, long reverb, no
drums, no melody, gentle and consoling but still deep and muffled, seamless loop, about three minutes."*


# Round 58 — Level 28: track 30 in production and track 18's deep copy

Generator: `tools/concept/audio/music_r58h.py` (M8 part H, step AUD-H, 2026-10-25; deterministic, measured only).

## reveal-sting-r58-a — "Black and Gold" in production (#30 Reveal sting, 9.0 s)

**Proposed** (round 58). Use: Level 28 at t=118 (K+18), once a run, as the two Ascendancy cruisers rise through the ice
on both flanks (4 s) and their gun ports open; Varga's and Okafor's lines speak over its tail; the run under Vorne's
motif follows.

**Brief**: the round-48 pick a (above) as composed: `music_r48.py`'s `sting_black_and_gold()` called unchanged (the
rise muffled under the ice and opening as the hulls break through, the motif stacked into a chord at 4.0 s, the A♯
hanging). Production adds a voice pocket: from 4.6 s the 0.9–4 kHz band of the whole mix eased down over 0.6 s (a
zero-phase FFT band cut, 5 dB at the band's centre, about 3 dB on the band as measured), so the two radio lines sit
over the tail. Mastered as the round-48 stings (−14 LUFS target under a −2 dBFS ceiling, a 1.2 s fade). **Measured**:
9.0 s, −14.9 LUFS, true peak −3.0 dBTP (the sketch: −15.0, −3.1).

**AI prompt**: as round 48's a, plus *"leave room in the upper mids after the hit for two lines of radio dialogue."*

## choir-descends-deep-r58-b — "The Choir Descends", the deep copy (#18 Boss: Vrell, Level 28)

**Proposed** (round 58, Q.1 b; a is the built theme, `assets/music/choir-descends.ogg` =
[choir-descends-final-r25-a](choir-descends-final-r25-a.ogg)). Use: Level 28's arena from the boss warning (t=104)
through the fight, dropping away under the dive's darkness and back after the lunge, fading at the kill.

**Brief**: the boss document asks for the Vrell boss theme "in its Act 4 setting (muffled, reverberant)". No runtime
filter exists for music, so the copy is pre-rendered: `music_r08.py`'s `choir_full()` (the generator of the game's
file) rendered again, its whole mix through the A theme's water low-pass at its deepest (one state-variable low-pass
at 1.3 kHz, q 1.1, "Thera Deep"'s drift section) and a 5.5 s dark reverb (mix 0.35, damping 0.7) before the assembly
(intro + crossfade + second loop pass, so the reverb's tail wraps round the seam), then `render_loop`'s own steps: the
same 2-bar tail, −14 LUFS under a −2 dBFS ceiling, `LOOPSTART` 299880 and `LOOPLENGTH` 5080320 as the game's file.
**Measured**: 125.2 s, −14.0 LUFS, −1.9 dBTP, seam 0.94; spectral centroid 790 Hz (the built theme's 4,480 Hz); the
first bar after the warning −14.1 LUFS. Listening aids: `choir-descends-deep-seam-r58-b.ogg` (the seam, 8 s each side),
`boss-handoff-deep-r58-b.ogg` (track 22 then b from 4.8 s, as the game mixes the boss cue).

**AI prompt** (if replaced by a musician): *"The same pounding choir-and-drums boss theme heard from deep under water:
the drums and choir muffled and far, a long dark reverb, the kick still heavy, seamless loop on the original's loop
points."*

# Round 60 — the Act 5 B theme (track 13) and Rook's return sting (track 31)

Step AUD-P (M9 design phase, 2026-10-27), synthesized by `tools/concept/audio/music_r60.py` (deterministic: fixed
seeds; the round-02 loop format for the themes, the round-08 sting format). Measured only, not listened to by Claude.
The brief is the music README's *Act 5 music*: "colder, more industrial: the enemy is human now"; no bubbly or popping
sounds, no resonant plucks, static only as band noise.

## act5-b-theme-r60-a — "Gilded Cage" (#13 Act 5 B, 57.6 s)

**Proposed** (round 60).

**Brief**: EBM and industrial techno with Vorne's motif in the open. A four-on-the-floor kick, a gated, distorted 16th
bass sequence (the root with octave and fifth jumps, gaps in the gate), struck sheet metal on the backbeat with a
tight snare under it, chains rattling on the off-16ths, a press hammer every other bar (every bar in C), hats and a
high metal hit from B; a cold string pad. The motif (E G B D♯) stated by low brass and organ every 8 bars over the
groove (loop bars 0, 8, 16, and once in the intro), 4.6 dB under the full mix (the README's "about 6 dB, its brass
statement at full"), and the machines answer it: the struck metal plays it back in eighths in the next two bars.

**Tempo / key**: 126 BPM (the draft's 128 needs a fractional-sample 16th; 126 is the nearest tempo that loops
sample-exactly), E minor. **Loop**: `LOOPSTART` 357000 (8.095 s), `LOOPLENGTH` 2016000 (45.714 s = 24 bars).
**Chords**: `Em Em C C Am Am B B` three times (A, B, C). **Measured**: −14.3 LUFS, −1.2 dBTP, seam 0.23.

**AI prompt** (if replaced by a musician): *"Cold EBM / industrial techno at 128 BPM in E minor: four-on-the-floor
kick, a gated distorted 16th-note bass sequence, struck sheet metal, chains and a hydraulic press as percussion, a
cold string pad; a villain's four-note motif (E G B D♯) stated by low brass and organ every eight bars, answered by
the metal percussion. Seamless loop."*

## act5-b-theme-r60-b — "Iron Vein" (#13 Act 5 B, 58.7 s)

**Proposed** (round 60).

**Brief**: darker and roomier, the motif only in the machines. A heavy half-time beat (the kick on 1, the "and" of 3
and a pick-up, one big snare a bar on 3 with a long dark reverb and a low metal hit), a sub bass on the roots, a
grinding drill texture (band noise sweeping 700 Hz → 1.5 kHz chopped at 16ths over a growling detuned saw two
octaves up the root, saturated), a cold pad; a drumless C section (the drill, the pad and the sequencer alone) with a
kick pick-up into D. The motif (G B♭ D F♯) only as a sequencer arpeggio inside the groove: two detuned saws through a
soft low-pass (q 0.8), 16ths from the B section on, an octave up on alternate bars in D; 9.4–10.9 dB under the mix.

**Tempo / key**: 140 BPM half-time, G minor (Hollow Rock's shift key). **Loop**: `LOOPSTART` 321300 (7.286 s),
`LOOPLENGTH` 2116800 (48.000 s = 28 bars). **Chords**: A, B and D `Gm Gm E♭ E♭ Cm Cm D D`; C `Gm E♭ Cm D`.
**Measured**: −14.3 LUFS, −1.7 dBTP, seam 0.40.

**AI prompt**: *"Dark half-time industrial at 140 BPM in G minor: a heavy kick and one huge reverberant snare a bar,
a sub bass, a grinding drill-like texture, a cold pad; a sequencer arpeggio of G, B-flat, D, F-sharp running inside
the groove (no brass, no lead). Seamless loop."*

## rook-return-r60-a — "Horn Call" (#31 Rook's return sting, 5.6 s)

**Proposed** (round 60).

**Brief**: as the prison tender opens and the Ember launches (Level 29), over the theme ducked −6 dB for 5 s: the
main motif's horn call answering Vorne's motif, a short brass lift on a timpani roll, ending on the theme's tonic.
This one: a timpani roll and low strings on B for one beat, then the horn call (1 – 5 – 8 – 9 – 10 in D major: D A D E
F♯) on horns doubled an octave down, over D – A – G strings; its last note held over a B minor chord (organ, strings,
a timpani hit, a sub). 135 BPM (the A theme's tempo). **Measured**: −14.0 LUFS, −1.9 dBTP.

**AI prompt**: *"A five-second heroic sting: a short timpani roll, then a French-horn call D–A–D–E–F♯ over strings,
landing on a B minor chord with the horn's F♯ held on top."*

## rook-return-r60-b — "Answer" (#31 Rook's return sting, 6.2 s)

**Proposed** (round 60).

**Brief**: as a, but the answer is heard: Vorne's motif first, low and quiet (organ and cellos, B D F♯ A♯ in eighths,
one bar), a snare roll and a swell, then the horn call answers it brighter (trumpets an octave up over the horns),
landing on the same B minor chord with a timpani hit and a cymbal. **Measured**: −14.0 LUFS, −2.2 dBTP.

**AI prompt**: *"A six-second sting: a sinister four-note organ figure, low and quiet, answered by a bright heroic
horn-and-trumpet call over a snare roll, landing on a B minor chord."*

## hollow-rock-r62-a — track 12 "Hollow Rock" and its three stems

From the chosen round-03 sketch [belt-theme-r03-a](belt-theme-r03-a.ogg) (135 BPM, B minor), by
`tools/concept/audio/music_r62.py`. Composition: the sketch's 4-bar intro (radio static, two beeps, the pad, Vorne's
motif in brass and organ, the anvils from bar 3, a snare roll) and a 96-bar loop of twelve 8-bar sections on its
progressions, A = `Bm Bm G G Em Em F♯ F♯`, B = `Bm Gm Bm F♯ Bm Gm Em F♯`, D = `G G F♯ F♯ Bm Gm Em F♯`: A (the machine:
four-on-the-floor kick, snare and an anvil on the backbeat, open hats, the syncopated distorted bass, the anvils'
pattern, the distorted machine line and the arpeggio playing Vorne's 1–♭3–5–7 at speed; the motif once in cold low
brass and organ), B (Vorne's 8-bar statement, timpani), A2, C (the conveyor breakdown doubled to 8 bars: no kit, the
anvil 16ths, the bass held, the lone human horn twice, a riser), D (the build with the motif tutti, trumpets on top, a
snare roll), B2, E (new, "drift": `Em Em G G Bm Bm F♯ F♯`, half time, no hats, the bass held, the arpeggio dark), F
(the build: kick, then hats, then the snare, the machine line and a riser), D2 (the climax), B3, C2 (the breakdown
again) and T (the turn back into A). Changes to the sketch's sound: the arpeggio's pluck without its resonance (q 1.4,
the sketch's 3.0; an 80 ms filter envelope), the intro's static without its crackle.

Stems: **base** kick, distorted bass, the anvils' low hits, pad, the conveyor, the radio static (Level 29 from section
2, Level 34 throughout); **full** the base plus snare, hats, the anvils' top, crashes, steam, timpani, risers, the
machine line, the arpeggio and the lone horn; **motif** Vorne's motif in brass and organ alone, in the open: its
statements 6 dB under the full mix on average (the game adds it on top; alone under Vorne's broadcast in the Act 5
intro).

**Measured** (not listened to): 181.78 s, `LOOPSTART` 333200 (7.556 s), `LOOPLENGTH` 7526400 (170.667 s = 96 bars);
full −14.1 LUFS, −1.6 dBTP, seam 0.35; base −14.7 LUFS (−0.6 LU), −2.2 dBTP, seam 0.91; motif −18.2 LUFS (−4.1 LU),
−8.2 dBTP, seam 0.13; full + motif summed −13.5 LUFS, peak −0.3 dBFS. Loudness per section in the sheet.

**AI prompt**: *"Driving industrial level theme for a 1990s sci-fi shooter in an asteroid-belt mining station, 135 BPM,
B minor, four-on-the-floor kick, struck anvils and steam hisses as percussion, a syncopated distorted bass, a machine
arpeggio, a cold pad, a villain's four-note motif stated in low brass and organ, a breakdown with a conveyor's ticking
and a lone horn, about three minutes, instrumental, seamless loop."*

- `hollow-rock-r62-a.ogg`, `hollow-rock-base-r62-a.ogg`, `hollow-rock-motif-r62-a.ogg`: the full mix and its two stems.
- `hollow-rock-seam-r62-a.ogg`, `hollow-rock-base-seam-r62-a.ogg`, `hollow-rock-motif-seam-r62-a.ogg`: listening aids,
  the last 8 s of each loop followed by the first 8 s after `LOOPSTART` (0.5 s fades).
- `hollow-rock-motif-mix-r62-a.ogg`: listening aid, loop bars 1–24, the full mix with the motif stem on top (42.7 s).
- `hollow-rock-sheet-r62-a.png`: review sheet: waveforms with intro (grey), loop (blue), fade tail (dark) and loop
  points (amber), the measured numbers and a per-section loudness table.

## gilded-cage-r64-a — track 13 "Gilded Cage" and its three stems

From the chosen round-60 sketch [act5-b-theme-r60-a](act5-b-theme-r60-a.ogg) (126 BPM, E minor), by
`tools/concept/audio/music_r64.py`. Composition: the sketch's 4-bar intro (the pad and the chains, Vorne's motif in
the open, the kick, the press and a riser from bar 3) and an 88-bar loop of eleven 8-bar sections, A = `Em Em C C Am
Am B B`: A (the cage: four-on-the-floor kick, struck sheet metal on the backbeat over a tight snare, chains on the
off-16ths, the press hammer every other bar, the gated distorted 16th bass sequence, the cold string pad; the motif in
low brass and organ, the struck metal playing it back at speed), B (+ hats and the high metal), C (+ the press
doubled), Br (new, "the line stops": `Em Em Em Em C C B B`, no kick, the bass held, the chains on the beat, the struck
metal playing the motif slowly, a riser and a kick fill), D (the hammer: the motif tutti with trumpets, timpani, a
snare roll), B2, E (new, "drift": `Am Am C C Em Em B B`, half time, no hats, the bass held), F (the build: kick, then
chains and metal, then the bass sequence, hats and a riser, a snare roll), D2, C2 and T (the turn back into A).

Stems: **base** kick, the bass, the pad, the press hammer (Levels 31 and 33 from section 2, the Level 35 approach);
**full** the base plus the struck sheet metal, snare, hats, chains, the high metal, crashes, timpani, risers and the
struck metal's answer; **motif** Vorne's motif in brass and organ alone, in the open: its statements 6 dB under the
full mix (the game adds it on top; alone under Level 35's recruitment after the boss).

**Measured** (not listened to): 179.52 s, `LOOPSTART` 357000 (8.095 s), `LOOPLENGTH` 7392000 (167.619 s = 88 bars);
full −14.1 LUFS, −1.4 dBTP, seam 0.24; base −14.3 LUFS (−0.2 LU), −2.4 dBTP, seam 0.20; motif −20.4 LUFS (−6.3 LU),
−9.9 dBTP, seam 0.20; full + motif summed −13.9 LUFS, peak −0.7 dBFS. Loudness per section in the sheet.

**AI prompt**: *"Cold EBM and industrial techno level theme for a 1990s sci-fi shooter in an asteroid belt, 126 BPM,
E minor, four-on-the-floor kick, struck sheet metal, chains and a press hammer as percussion, a gated distorted 16th
bass sequence, a cold string pad, a villain's four-note motif stated in low brass and organ every eight bars and
answered by the struck metal, a drumless breakdown and a half-time drift, about three minutes, instrumental, seamless
loop."*

- `gilded-cage-r64-a.ogg`, `gilded-cage-base-r64-a.ogg`, `gilded-cage-motif-r64-a.ogg`: the full mix and its two stems.
- `gilded-cage-seam-r64-a.ogg`, `gilded-cage-base-seam-r64-a.ogg`, `gilded-cage-motif-seam-r64-a.ogg`: listening aids,
  the last 8 s of each loop followed by the first 8 s after `LOOPSTART` (0.5 s fades).
- `gilded-cage-motif-mix-r64-a.ogg`: listening aid, loop bars 1–24, the full mix with the motif stem on top (45.7 s).
- `gilded-cage-sheet-r64-a.png`: review sheet: waveforms with intro (grey), loop (blue), fade tail (dark) and loop
  points (amber), the measured numbers and a per-section loudness table.

## hollow-rock-conveyor-r65-a — track 12 "Hollow Rock"'s conveyor stem (Level 32's forks)

A fourth stem for [hollow-rock-r62-a](hollow-rock-r62-a.ogg), by `tools/concept/audio/music_r65.py` (music_r62.py's
render, voices, chains and seeds; new groups only). Level 32 asks for "the conveyor breakdown with the lone horn at the
forks (sections 3 and 5)"; the game plays a theme's stems sample-aligned and switches them at times (`stem_times`, as
Level 26's dark stem), so the breakdown cannot be jumped to: this stem plays the breakdown's material (sections C and C2)
over the whole loop on each section's own chords: the conveyor (anvil 16ths at 3.1 kHz), the anvils' pattern at the
breakdown's weight, the distorted bass held on each bar's root, the cold pad, the machine's arpeggio dark and quiet,
a steam hiss every 4 bars and the **lone human horn** (humanity's motif on the flute) twice in every 8 bars, from the
chord's root (B3, G3, E4, F♯3), minor or major with the chord, on bars 1 and 5 where the chord holds (bars 2 and 6 in C
and C2, as the sketch). No kick, snare, hats, riser or brass (the motif stem can still play on top). Mastered linked to
the full mix (its gain and peak correction); the run checks that the re-rendered full mix decodes to the shipped
`hollow-rock-r62-a.ogg` sample for sample (identical), so the stem lines up with the files in `assets/music`.

**Measured** (not listened to): 181.78 s, `LOOPSTART` 333200 (7.556 s), `LOOPLENGTH` 7526400 (170.667 s), aligned with
the r62 files; −16.6 LUFS (−2.5 LU under the full mix; −16.4 to −16.8 LUFS in every section, against the full mix's
breakdown C at −14.9), −3.0 dBTP, seam 0.24.

**AI prompt**: *"The breakdown of an industrial asteroid-mining level theme, 135 BPM, B minor: no drums, a conveyor's
ticking metal 16ths, struck anvils, a held distorted bass and a cold pad, a quiet machine arpeggio, a lone human horn
calling a five-note motif every four bars, about three minutes, instrumental, seamless loop."*

- `hollow-rock-conveyor-r65-a.ogg`: the stem.
- `hollow-rock-conveyor-seam-r65-a.ogg`: listening aid, the last 8 s of the loop followed by the first 8 s after
  `LOOPSTART` (0.5 s fades).
- `hollow-rock-conveyor-join-r65-a.ogg`: listening aid, loop bars 1–16 (28.4 s): the full mix crossfading over 2 s into
  the conveyor stem alone at bar 9, as a fork would switch.

## iron-sovereign-full-r68-a — track 19 "Iron Sovereign" at full length (one full mix)

From the chosen round-03 sketch [ascendancy-boss-r03-a](ascendancy-boss-r03-a.ogg) (147 BPM, F♯ minor), by
`tools/concept/audio/music_r68.py`, the way track 18 went from its round-02 sketch to `choir-descends-full-r08-a` in
round 08: the sketch's voices, grooves, chords, figures, mix chains and seeds; one full mix, no stems (a boss fight
switches nothing). Composition: the sketch's 4-bar intro (the parade: rudiments growing, timpani, Vorne's motif in low
brass on F♯ then C♯, the organ below) and a 72-bar loop of nine 8-bar sections, so the 2:00 does not play the 32-bar
sketch twice: **A** march, **B** machine, **C** menace (half time, the motif at one note a bar) and build, **D** tutti
(trumpets on the motif, the low-brass canon a bar behind) — the sketch's four, as they were — then **Br** (new, the
machine alone: no drums, the 16th riff over a held bass, the organ on the chords, a metal tick and the timpani; from
bar 5 the motif in low brass and organ, the kick and rudiments returning, a roll and a riser), **B2** (new, B's groove
with the trumpets and the low brass playing the motif in diminution, one bar each on F♯m and Bm), **E** (new, parade
ground: the snare corps in front, a heavier kick, tom runs, the motif in low brass and then in trumpets with the canon,
stab figures on the G Neapolitan and on C♯), **F** (new, the build: kick and timpani on every beat over a held bass,
the march from bar 3, the riff, the low-brass 8ths and the trumpets' motif on B from bar 5, a 4-bar riser and a roll)
and **D2** (the tutti again, the organ on the motif an octave down), rolling into A. The breakdowns (Br, F's first
half) and build-ups (C, Br, F) give a three-ring fight somewhere to breathe; the music does not follow the phases.

| Bars | Section | Chords |
|---|---|---|
| 1–8 | A | `F♯m(maj7) F♯m D D Bm(maj7) Bm C♯ C♯` |
| 9–16 | B | `F♯m D Bm C♯` × 2 |
| 17–24 | C | `F♯m G F♯m G D Bm C♯ C♯` |
| 25–32 | D | as A |
| 33–40 | Br | `F♯m F♯m D D Bm Bm C♯ C♯` |
| 41–48 | B2 | as B |
| 49–56 | E | `F♯m F♯m G G F♯m F♯m C♯ C♯` |
| 57–64 | F | as A |
| 65–72 | D2 | as A |

**Measured** (not listened to): 127.76 s, `LOOPSTART` 306000 (6.939 s, the sketch's), `LOOPLENGTH` 5184000 (117.551 s =
72 bars; a 16th is exactly 4500 samples), −14.0 LUFS, −1.5 dBTP (the limiter's ceiling −3 dBFS, as track 12's: at
round 08's −2 the encode reached −0.8 dBTP), seam 0.13, 2.7 MB; the tonal balance within 0.6 dB of the sketch's in every
octave band. Loudness per section (LUFS): A −13.6, B −15.0, C −14.8, D −13.1, Br −16.6 (bars 1–4 −19.7, the drop),
B2 −13.5, E −13.3, F −14.7 (bars 1–4 −16.6), D2 −12.9.

**AI prompt**: *"Cold martial boss battle theme, 147 BPM, F sharp minor, military snare drum rudiments, timpani, low
brass staccato ostinato, trumpets playing a stately minor-major-seventh villain motif, distorted 16th-note synth riff,
metallic industrial hits, brass canon, human military menace, no choir, 1990s synth-orchestral and industrial hybrid,
instrumental, full-length two-minute arrangement with a drumless machine breakdown, a snare-corps section and long
build-ups, seamless loop."*

# Round 72 — the Act 6 B theme (track 15), the duel theme (track 32), the Ascendant's hand-off and the escape sting (track 33)

Step AUD-R (M10 design phase, 2026-10-31), synthesized by `tools/concept/audio/music_r72.py` (deterministic: fixed
seeds; the round-02 loop format for the themes, the round-08 sting format; the hand-offs cut from the game's
`assets/music/iron-sovereign.ogg` and `choir-descends.ogg`). Measured only, not listened to by Claude. The brief is
the music README's *Act 6 music (draft)*; no bubbly or popping sounds, no resonant plucks, thunder and static only as
band noise. Pending the user's review in round 72.

## act6-b-theme-r72-a — "Storm Wall" (#15 Act 6 B, 60.9 s)

**Proposed** (round 72).

**Brief**: an orchestral-metal hybrid, heavy and stormy. A drop-tuned distorted bass riff in half-time (a chug on the
root with a flat-2 rub, the sub an octave down), war drums (a deep kick on 1 and the and-of-2, a huge half-time snare
and clap on 3, taiko, low tom fills every 4 bars, a double kick under the last section), tremolo storm strings, a
wordless choir in long minor chords from the B section, hats from B; the storm's wind as band noise (louder in the
breakdown). Vorne's motif (C E♭ G B) in the open: cold low brass and organ at loop bars 0, 8 and 20 (and once in the
intro), 5.6–6.4 dB under the full mix. A 4-bar breakdown (loop bars 16–19): the choir alone over low strings, a far
thunder roll, taiko into the last section. The production's `fall` stem would be this breakdown's material under the
whole loop.

**Tempo / key**: 135 BPM, C minor. **Loop**: `LOOPSTART` 333200 (7.556 s), `LOOPLENGTH` 2195200 (49.778 s = 28 bars).
**Chords**: `Cm Cm A♭ A♭ Fm Fm G G` (A, B, D), `Cm A♭ Fm G` (the breakdown). **Measured**: −14.3 LUFS, −1.1 dBTP,
seam 0.45.

**AI prompt**: *"Orchestral metal at 135 BPM in C minor: a drop-tuned distorted bass riff in half-time, war drums and
low toms, tremolo storm strings, a wordless choir in long minor chords, wind; a villain's four-note motif (C E♭ G B)
in cold low brass and organ every eight bars; a breakdown of the choir alone over low strings. Seamless loop."*

## act6-b-theme-r72-b — "Red Spot" (#15 Act 6 B, 61.2 s)

**Proposed** (round 72).

**Brief**: darker and driving, the Vrell with the upper hand. A distorted, gated 16th bass on the roots (octave jumps
and a flat-2 rub), an industrial kit (a four-on-the-floor kick, a tight snare and struck sheet metal on the backbeat,
hats, tom fills), cold strings; the choir chanting the Choir motif's tritone drop (E D C B, F E) as a two-bar riff in
the B and D sections, slowed to half-bar notes in the drumless C section (toms, a held sub). Vorne's motif only inside
the machine: the arpeggio E G B D♯ in 16ths (two detuned saws, a soft low-pass) from the B section on, 11.6–14.5 dB
under the mix, no brass; the chord progression turns to the flat-2 (F) before each phrase's end.

**Tempo / key**: 150 BPM, E minor (track 18's key). **Loop**: `LOOPSTART` 299880 (6.800 s), `LOOPLENGTH` 2257920
(51.200 s = 32 bars). **Chords**: A `Em Em C C Am Am B B`; B–D `Em Em C C Am Am F Em`. **Measured**: −14.3 LUFS, −0.7
dBTP, seam 0.64.

**AI prompt**: *"Dark industrial metal at 150 BPM in E minor: a distorted 16th-note bass, an industrial drum kit with
struck metal, a choir chanting a descending E–D–C–B then F–E figure as a riff; a cold E–G–B–D♯ sequencer arpeggio
running inside the bass, no brass. Seamless loop."*

## duel-theme-r72-a — "Honour Guard" (#32 duel theme, 61.2 s)

**Proposed** (round 72).

**Brief**: Level 40's three-ace duel after the mini-boss sting (A minor, the sting's last chord): martial and orchestral
in track 19's family. Snare rudiments (the Iron Sovereign's grid), timpani on the roots and fifths, a kick on the
beats, low-brass 8ths and galloping strings over Am – F – Dm – E, a string pad. A (loop bars 0–7): the joust, Vorne's
motif (A C E G♯) in gold brass (a trumpet an octave up) answered by the main motif's horn call (A E A B C) in the
horns, twice. B (8–15): a three-voice brass fugato on Vorne's motif (A low, E, A high in the trumpet: the three
aces), closing on a held G♯–A / B–E. C (16–23): both motifs at once (Vorne's low, the horn call in trumpets), then in
canon a bar apart. D (24–31): the tutti, the horn call on top and Vorne's motif under it.

**Tempo / key**: 150 BPM, A minor. **Loop**: `LOOPSTART` 299880 (6.800 s), `LOOPLENGTH` 2257920 (51.200 s = 32 bars).
**Measured**: −14.2 LUFS, −2.0 dBTP, seam 0.16. Production: ≈ 2:00, one full mix (no stems).

**AI prompt**: *"A martial orchestral duel theme at 150 BPM in A minor: snare rudiments, timpani, low brass eighths,
galloping strings; a villain's motif A–C–E–G♯ in gold brass answered by a heroic horn call A–E–A–B–C, a three-voice
brass fugato, then both themes together. Seamless loop."*

## duel-theme-r72-b — "Three Aces" (#32 duel theme, 63.4 s)

**Proposed** (round 72).

**Brief**: leaner and more arcade: a drum-and-bass breakbeat (kick on 1 and the and-of-3, snares on 2 and 4 with ghost
notes, 16th hats), a reese sub on the roots, synth-brass stabs on the off-beats; Vorne's motif as a fast sequencer
arpeggio (A C E G♯ in 16ths) from the B section; the main motif soaring in a distorted lead (C and E sections, a
third up in answer); a half-time D section of the sequencer and the stabs alone.

**Tempo / key**: 175 BPM, A minor. **Loop**: `LOOPSTART` 257040 (5.829 s), `LOOPLENGTH` 2419200 (54.857 s = 40 bars).
**Measured**: −14.2 LUFS, −1.1 dBTP, seam 0.16.

**AI prompt**: *"Drum and bass at 175 BPM in A minor: a breakbeat, a reese bass, synth brass stabs, a fast A–C–E–G♯
sequencer arpeggio and a soaring distorted lead playing a heroic five-note call. Seamless loop."*

## ascendant-handoff-r72-a, -b — the Ascendant's hand-off from track 19 to track 18 (Level 42)

**Proposed** (round 72).

**Brief**: at the eruption (phase 2) the Choir's music takes Vorne's ship: track 19 "Iron Sovereign" (phase 1) hands
over to track 18 "The Choir Descends", whose intro is the Choir motif alone over a drone, under the Choir's line and
Vorne's shout (the radio's duck, not applied in these files). Both cut from the game's own files (track 19 from its
loop bar 8, 10 s, before the hand-off; 10 s of track 18 after), levelled to −14 LUFS.
**a, crossfade** (recommended: no new render): track 19 out over 2 s (a raised-cosine fade) while track 18 starts from
its beginning. 20.0 s, −14.3 LUFS.
**b, bridge**: track 19's next 4 s played at a speed gliding 1.0 → 0.89 (a tape stop two semitones down: F♯ minor onto
track 18's E; the draft says "a semitone", two land on the key) under a low-pass closing 8 kHz → 600 Hz, fading over
its last 2 s while track 18's intro rises under it. 22.0 s, −14.3 LUFS.

## escape-sting-r72-a — "Collapse" (#33 escape sting, 6.6 s)

**Proposed** (round 72).

**Brief**: Level 42's `ESCAPED`, as the lifeboat leaves the top edge, cutting track 18; then the ambience alone. Brass
and choir: a timpani roll and low strings on E; Vorne's motif in E (E dotted, G, B, D♯) in low brass doubled by organ,
126 BPM, the D♯ bending down a semitone and dying (its filter closing); the choir takes the Choir motif's descent (E D
C B, each falling note a little quieter and further right) and the tritone drop: a held low E (strings E1–E3, organ, a
sub, a timpani hit) with the choir's F over it, unresolved, ringing out. **Measured**: −14.3 LUFS, −2.3 dBTP.

**AI prompt**: *"A six-second sting: a villain's four-note brass motif E–G–B–D♯, its last note bending down and dying,
taken over by a choir descending E–D–C–B and dropping to F over a held low E, unresolved."*

## escape-sting-r72-b — "Dying Engine" (#33 escape sting, 6.6 s)

**Proposed** (round 72).

**Brief**: the same moment as a machine: Vorne's motif as a 16th arpeggio (E G B D♯, alternate beats an octave up, two
detuned saws over a distorted bass on the beats) for 1 s, then slowing and pitching down like a dying engine (a tape
stop: the speed gliding 1.0 → 0.45 over 2.4 s, a low-pass closing 7 kHz → 500 Hz, the level sinking), into the
choir's drop (C B, then F over E) and the same held low E with the F over it. **Measured**: −14.4 LUFS, −1.9 dBTP.

**AI prompt**: *"A six-second sting: a cold synth arpeggio E–G–B–D♯ slowing and sinking in pitch like a dying engine,
into a choir dropping a tritone onto a held low E with an unresolved F above it."*

## eye-of-the-storm-r74-a — track 14 "Eye of the Storm" and its three stems

From the chosen round-03 sketch [jovian-theme-r03-a](jovian-theme-r03-a.ogg) (140 BPM, G minor), by
`tools/concept/audio/music_r74.py`. Composition: the sketch's 4-bar intro (the storm strings swelling, a far thunder
roll, the wind, Vorne's motif in low brass answered by humanity's motif in trumpets, a timpani roll and a riser) and a
96-bar loop of twelve 8-bar sections: A (the hunt: `Gm Eb Bb F Gm Eb Cm D`, four-on-the-floor kick, clap and snare,
16th hats, the taiko pattern, tom fills, the gallop strings, the bass, the pad and the choir's chords, the main motif
and its melody in horns and trumpets), B (Vorne against the Coalition: `GmM7 GmM7 Eb Eb CmM7 CmM7 D D`, Vorne's motif
in low brass and choir answered by humanity's motif in major in trumpets), A2, C (the storm: `Gm Ab Gm Ab Eb Cm D D`,
four calm bars of thunder rolls, taiko and held bass under the choir's G to A-flat line, then the groove back with a
riser and a snare roll), D (both motifs: `Gm GmF# Bb F Gm GmF# Cm D`, the melody in trumpets and the lead an octave up
over Vorne's motif in low brass), B2, E (new, the drift: `Eb Eb Cm Cm Gm Gm D D`, half time, no hats, no gallop, the
human motif alone in a quiet horn), F (new, the build: the kick alone, then hats, then the backbeat and the gallop,
a riser and a snare roll), D2, B3, C2 and T (the turn back into A). The thunder is the sketch's roll without its crack
and the wind is made periodic over the loop.

Stems: **base** the storm strings' tremolo under every bar, the kick and the taiko, the bass, the pad, the wind and the
thunder rolls (Levels 36, 39 and 40 from section 2); **full** the base plus the gallop strings, the clap, snare, toms
and hats, crashes, timpani and risers, the horns, trumpets and lead, and the choir (its chords and the storm's line);
**motif** Vorne's motif in low brass and choir alone, in the open: its statements about 6 dB under the full mix (the
game adds it on top; alone under the Act 6 intro's page 3).

**Measured** (not listened to): 175.29 s, `LOOPSTART` 321300 (7.286 s), `LOOPLENGTH` 7257600 (164.571 s = 96 bars);
full −14.0 LUFS, −1.8 dBTP, seam 0.41; base −17.8 LUFS (−3.8 LU), −4.4 dBTP, seam 0.74; motif −21.2 LUFS (−7.2 LU),
−11.4 dBTP, seam 0.00, its statements B −5.1 dB and D −7.6 dB under the full mix; full + motif summed −13.9 LUFS,
peak −1.2 dBFS. Loudness per section in the sheet.

**AI prompt**: *"Vast, stormy orchestral level theme for a 1990s sci-fi shooter in Jupiter's cloud bands, 140 BPM,
G minor, tremolo storm strings, galloping string ostinato, war drums and taiko, a heroic five-note horn motif with
trumpet answers, a villain's four-note minor-major-seventh motif in low brass and wordless choir, a calm storm
breakdown with thunder rolls and the choir alone, a half-time drift, about three minutes, instrumental, seamless
loop."*

- `eye-of-the-storm-r74-a.ogg`, `eye-of-the-storm-base-r74-a.ogg`, `eye-of-the-storm-motif-r74-a.ogg`: the full mix
  and its two stems.
- `eye-of-the-storm-seam-r74-a.ogg`, `eye-of-the-storm-base-seam-r74-a.ogg`, `eye-of-the-storm-motif-seam-r74-a.ogg`:
  listening aids, the last 8 s of each loop followed by the first 8 s after `LOOPSTART` (0.5 s fades).
- `eye-of-the-storm-motif-mix-r74-a.ogg`: listening aid, loop bars 1–24, the full mix with the motif stem on top
  (41.1 s).
- `eye-of-the-storm-sheet-r74-a.png`: review sheet: waveforms with intro (grey), loop (blue), fade tail (dark) and
  loop points (amber), the measured numbers and a per-section loudness table.

## storm-wall-r75-a — track 15 "Storm Wall" and its four stems

From the chosen round-72 sketch [act6-b-theme-r72-a](act6-b-theme-r72-a.ogg) (135 BPM, C minor), by
`tools/concept/audio/music_r75.py`. Composition: the sketch's 4-bar intro (the storm strings swelling, two far thunder
rolls, Vorne's motif in low brass and organ, the deep kick, a taiko and the riff's chug from bar 3, a low tom fill and
a riser) and a 96-bar loop of twelve 8-bar sections: A (`Cm Cm Ab Ab Fm Fm G G`: the war drums, a deep kick on 1, the
and-of-2 and 3, a huge half-time snare and clap, taiko, the drop-tuned riff in half time with its sub, the storm
strings, low tom fills), B (A plus 8th hats and the wordless choir), A2, C (the breakdown, `Cm Ab Fm G` twice: the
choir alone over the low strings and the wind, thunder rolls, the kick and sub back in bars 5–8, a taiko build and a
riser), D (all in, the double kick), B2, E (new, the drift: `Ab Ab Fm Fm Cm Cm G G`, half time, no riff, the strings
and the choir up), F (new, the build: the kick alone, then the snare, then the riff and taiko, a riser and a snare
roll), D2, B3, C2 and T (the turn back into A). The thunder is a roll without a crack; the wind the sketch's periodic
current.

Stems: **base** the storm strings, the deep kick and the taiko, the riff and its sub, the low strings, the wind and
the thunder rolls (the approaches of Levels 38, 41 and 42); **full** the base plus the snare and clap, hats, toms,
crashes, timpani and risers and the choir; **motif** Vorne's motif in cold low brass and organ alone, in the open
(about 6 dB under the full mix; never in Level 37 after the turn); **fall** the breakdown's choir and low strings under
every bar, nothing else (Level 37 plays it alone over the falling city).

**Measured** (not listened to): 181.78 s, `LOOPSTART` 333200 (7.556 s), `LOOPLENGTH` 7526400 (170.667 s = 96 bars);
full −14.1 LUFS, −1.8 dBTP, seam 0.31; base −15.4 LUFS (−1.3 LU), −2.2 dBTP, seam 0.21; motif −19.4 LUFS (−5.3 LU),
−8.8 dBTP, seam 0.10, its statements −4.8 to −5.9 dB under the full mix; fall −15.8 LUFS (−1.7 LU), −3.1 dBTP, seam
0.58; full + motif summed −13.7 LUFS, peak −0.1 dBFS. Loudness per section in the sheet.

**AI prompt**: *"Heavy, stormy orchestral-metal level theme for a 1990s sci-fi shooter over a burning cloud city on
Jupiter, 135 BPM, C minor, a drop-tuned distorted bass riff in half time, war drums, taiko and low toms, tremolo storm
strings, a wordless choir in long minor chords, a villain's four-note minor-major-seventh motif in cold low brass and
organ, a breakdown of the choir alone over low strings and wind, about three minutes, instrumental, seamless loop."*

- `storm-wall-r75-a.ogg`, `storm-wall-base-r75-a.ogg`, `storm-wall-motif-r75-a.ogg`, `storm-wall-fall-r75-a.ogg`: the
  full mix and its three stems.
- `storm-wall-seam-r75-a.ogg`, `storm-wall-base-seam-r75-a.ogg`, `storm-wall-motif-seam-r75-a.ogg`,
  `storm-wall-fall-seam-r75-a.ogg`: listening aids, the last 8 s of each loop followed by the first 8 s after
  `LOOPSTART` (0.5 s fades).
- `storm-wall-motif-mix-r75-a.ogg`: listening aid, loop bars 1–24, the full mix with the motif stem on top (42.7 s).
- `storm-wall-fall-join-r75-a.ogg`: listening aid, loop bars 1–16, the full mix crossfading over 2 s into the fall
  stem alone at bar 9 (28.4 s).
- `storm-wall-sheet-r75-a.png`: review sheet: waveforms with intro (grey), loop (blue), fade tail (dark) and loop
  points (amber), the measured numbers and a per-section loudness table.

## duel-theme-full-r78-a — track 32 "Three Aces" at full length, one full mix

From the chosen round-72 sketch [duel-theme-r72-b](duel-theme-r72-b.ogg) (175 BPM, A minor), by
`tools/concept/audio/music_r78.py`. Composition: the sketch's 4-bar intro (the sequencer alone playing Vorne's motif,
A C E G♯, in 16ths, filtered up, the break from bar 3, a riser) and an 88-bar loop of eleven 8-bar sections (A =
`Am Am F F Dm Dm E E`, C = `Am F C E Am F Dm E`): A (the drum-and-bass break, the reese sub bass, the synth-brass
stabs on the off-beats), B (plus Vorne's motif in the sequencer), C (plus the main motif, A E A B C, soaring in the
distorted lead), J (new, the joust: Vorne's motif stated in held synth brass, answered by the main motif in the lead,
twice), D (half time: the sequencer and the stabs alone, a snare roll), T (new, the three aces: the sequencer in a
three-voice canon entering every two bars, panned left, right and centre, the break full from bar 5 under a riser),
E (everything), Br (new, a breakdown: four bars without drums, the reese held under brass chords, then the kick, the
lead and a riser), K (new, the canon: the lead with itself a bar behind an octave down, Vorne's motif in the brass,
the sequencer under them), B2 and A2 (the turn back into A).

**Measured** (not listened to): 129.26 s, `LOOPSTART` 257040 (5.829 s), `LOOPLENGTH` 5322240 (120.686 s = 88 bars);
−14.0 LUFS, −1.6 dBTP, peak −2.2 dBFS, seam 0.17; the sections from −12.7 LUFS (K) to −15.7 (D), the breakdown's
drumless half −16.9.

**AI prompt**: *"Fast drum-and-bass duel theme for a 1990s sci-fi shooter mid-boss, three enemy aces in black and
gold, 175 BPM, A minor, a breakbeat with ghost notes and 16th hats, a reese sub bass, synth-brass stabs, a villain's
four-note minor-major-seventh motif as a fast sequencer arpeggio, the hero's horn-call motif in a soaring distorted
lead, call and response then canon, about two minutes, instrumental, seamless loop."*

- `duel-theme-full-r78-a.ogg`: the full mix (`assets/music/duel-theme.ogg`).
- `duel-theme-seam-r78-a.ogg`: listening aid, the last 8 s of the loop followed by the first 8 s after `LOOPSTART`
  (0.5 s fades).

## escape-sting-r80-a — track 33 "Collapse" in production

From the chosen round-72 sketch [escape-sting-r72-a](escape-sting-r72-a.ogg) (126 BPM, E minor), by
`tools/concept/audio/music_r80.py`, the reveal sting's method (music_r58h.py): the sketch's notes, voices, chains and
seeds unchanged (Vorne's motif E G B D♯ once in low brass and organ over a timpani roll, the D♯ bending down a semitone
and dying; the choir's E D C B and the tritone drop to F; the held low E in strings, organ, a sub and a timpani hit,
the F over it), with the production's layers on their own groups: a low hit on the one (a deep kick and a timpani on E:
the downbeat the cut from track 18 lands on), the motif doubled an octave down in soft brass with its bend, low strings
under the choir's descent (E3 D3 C3 B2, then F2), the held F and E in quiet tremolo strings to the end, the chord left
ringing to 7.0 s (a 1.4 s fade). A voice pocket for Varga's closing line at `ESCAPED` + 2 s: from 1.8 s the 0.9–4 kHz
band eased down 4 dB over 0.6 s (the radio's own duck still applies in the game).

**Measured** (not listened to): 7.00 s, −13.9 LUFS, −2.0 dBTP, peak −2.0 dBFS; the voice band 3.9 dB down over
2.5–6 s; RMS −12.7 dB under the cut and the motif, −16.2 under the choir's descent, −16.3 under the held end.

**AI prompt**: *"Six-second orchestral sting for a 1990s sci-fi shooter, the villain escaping: a low brass statement
of a four-note minor-major-seventh motif in E (E G B D-sharp) over a timpani roll, the last note bending down a
semitone and dying, a wordless choir taking over with a falling line (E D C B) and a tritone drop to F, ending on a
held low E with the F above it in strings, organ and tremolo, unresolved, dark and hollow."*

- `escape-sting-r80-a.ogg`: the sting (`assets/music/escape-sting.ogg`), a one-shot.

# Round 84 — the Act 7 themes, Vorne's last stand, the Ending, the Credits and the memory filter

Step AUD-R (M11 design phase, 2026-11-04), synthesized by `tools/concept/audio/music_r84.py` (deterministic: fixed seeds; the round-02 loop format: intro + loop + a 2-bar fade tail, `LOOPSTART`/`LOOPLENGTH` in samples, −14 LUFS, a −2 dBFS ceiling; the memory files cut from the game's `assets/music/iron-sovereign.ogg`). Measured only, not listened to by Claude. The brief is the music README's *Act 7 music (draft)*; no bubbly or popping sounds, no resonant plucks. Pending the user's review in round 84.

## act7-a-theme-r84-a — "Far Shore" (#16 Act 7 A theme, 65.6 s)

**Proposed** (round 84).

**Brief**: Plays: Levels 43, 45 and the Level 48 approach (the push through); should be synth-orchestral or electronic, driving, the main motif carrying the wing through, the Choir motif answering (its own `choir` stem). This one: synth-orchestral over a driving breakbeat: a low string ostinato in 8ths, the main motif (D A D E F) in horns at loop bars 0, 8, 20 and 24, answered by the Choir motif (E D C B, F E: E-rooted, its F rubbing the key) in the formant choir at bars 4, 12 and 28, a breakdown (bars 16–19) of the choir alone over a pad.

**Tempo / key**: 140 BPM, D minor. **Measured**: 65.6 s, loop 321300 + 2419200 samples (7.286 s + 54.857 s), seam 0.51, −14.2 LUFS, −1.4 dBTP; the `choir` stem 4.9 / 4.9 / 1.6 / 5.0 dB under the mix over its statements (loop bars 4, 12, 16, 28).

**AI prompt**: *"Synth-orchestral action at 140 BPM in D minor over a driving breakbeat: a low string ostinato, a heroic five-note horn motif (D A D E F) every eight bars, answered by a wordless alien choir singing a falling line with a tritone drop (E D C B, F E); a breakdown of the choir alone over a pad. Seamless loop."*

## act7-a-theme-r84-b — "Event Horizon" (#16 Act 7 A theme, 72.9 s)

**Proposed** (round 84).

**Brief**: Plays: as sketch a. This one: darker and electronic: an industrial-trance pulse (four-on-the-floor, an off-beat reese, struck sheet metal on the backbeat, a 16th pulse through a delay), reversed pad swells into every fourth bar, the main motif as a supersaw lead (loop bars 8, 12, 24, 28), the Choir motif's tritone drop (B F E) as a falling choir riff under it from bar 8 (the `choir` stem); a kickless C section.

**Tempo / key**: 126 BPM, D minor. **Measured**: 72.9 s, loop 357000 + 2688000 samples (8.095 s + 60.952 s), seam 0.20, −14.2 LUFS, −1.3 dBTP; the `choir` stem 7.3 / 2.5 / 7.2 dB under the mix over its statements (loop bars 8, 16, 24).

**AI prompt**: *"Dark industrial trance at 126 BPM in D minor: four-on-the-floor kick, off-beat reese bass, metallic backbeat, reversed pad swells, a supersaw lead playing a five-note heroic motif, a wordless choir repeating a falling tritone riff (B F E) underneath. Seamless loop."*

## act7-b-theme-r84-a — "Inner Sphere" (#17 Act 7 B theme, 69.1 s)

**Proposed** (round 84).

**Brief**: Plays: Levels 44, 46 (its base stem alone) and 47, the Level 50 approach (the unsettled half); should be slower and stranger, thin enough to drop to a pulse. This one: slow heavy drums (a deep kick, a huge half-time snare and clap, taiko), a sub drone on E, reversed choir and string swells, the Choir motif augmented (one note a half bar, four bars) in low brass, organ and a choir double at loop bars 0, 8 and 16 (the `choir` stem), the main motif's first three notes only (E B E, never completed) in a muted horn at bars 5, 13 and 21; the last bars thin to the kick and the drone.

**Tempo / key**: 105 BPM half-time, E Phrygian. **Measured**: 69.1 s, loop 428400 + 2419200 samples (9.714 s + 54.857 s), seam 0.18, −14.3 LUFS, −1.8 dBTP; the `choir` stem 1.5 / 2.1 / 2.1 dB under the mix over its statements (loop bars 0, 8, 16).

**AI prompt**: *"Slow, heavy, unsettling half-time at 105 BPM in E Phrygian: deep drums, a sub drone, reversed choir and string swells, an alien falling motif in low brass at half speed, a muted horn playing the first three notes of a heroic motif and never finishing it. Seamless loop."*

## act7-b-theme-r84-b — "Hollow Light" (#17 Act 7 B theme, 64.3 s)

**Proposed** (round 84).

**Brief**: Plays: as sketch a. This one: ambient and glassy: bowed-glass pads (sine partials, a slow bow), a soft heartbeat kick on every bar, reversed glass swells, the Choir motif in one wordless voice (loop bars 4 and 12, the `choir` stem), the main motif once per loop in a distant trumpet (bar 16).

**Tempo / key**: 98 BPM, E minor. **Measured**: 64.3 s, loop 459000 + 2160000 samples (10.408 s + 48.980 s), seam 0.46, −14.3 LUFS, −1.7 dBTP; the `choir` stem 1.4 / 1.6 dB under the mix over its statements (loop bars 4, 12).

**AI prompt**: *"Ambient and glassy at 98 BPM in E minor: bowed-glass pads, a soft heartbeat kick, a single wordless voice singing a falling alien line, a distant trumpet playing a heroic motif once. Seamless loop."*

## vorne-last-stand-r84-a — "Fused" (#34 Vorne's last stand (new), 61.2 s)

**Proposed** (round 84).

**Brief**: Plays: Level 48, Vorne's Chimera, after the mini-boss sting until his death (K), where it cuts with his last line; should be track 19's martial machine against track 18's choir, Vorne's motif bending into the Choir motif's tritone drop (he has been absorbed); one full mix. This one: orchestral-martial (track 19's family): snare rudiments, timpani, low-brass 8ths, galloping strings, the choir in long chords over Em – C – Am – B; Vorne's motif (E G B D♯) in cold brass at loop bars 0, 8, 16 and 24, its last note bending a semitone down and dying, the choir answering with the tritone drop (B F E).

**Tempo / key**: 150 BPM, E minor. **Measured**: 61.2 s, loop 299880 + 2257920 samples (6.800 s + 51.200 s), seam 0.42, −14.2 LUFS, −1.5 dBTP.

**AI prompt**: *"Martial orchestral boss music at 150 BPM in E minor: snare rudiments, timpani, low brass eighths, galloping strings, a choir in long chords; a cold four-note villain motif (E G B D-sharp) in brass whose last note bends down and dies, answered by the choir's falling tritone (B F E). Seamless loop."*

## vorne-last-stand-r84-b — "Overgrown" (#34 Vorne's last stand (new), 62.5 s)

**Proposed** (round 84).

**Brief**: Plays: as sketch a. This one: industrial: a 16th machine riff (distorted bass with a sequencer double) slowly detuning over the loop (a third of a semitone down, back at the loop), a four-on-the-floor kit with sheet metal on the backbeat, the choir chanting the Choir motif in 8ths; in the loop's last third (bars 22–31) the choir sings Vorne's motif.

**Tempo / key**: 147 BPM, E minor. **Measured**: 62.5 s, loop 306000 + 2304000 samples (6.939 s + 52.245 s), seam 0.31, −14.2 LUFS, −1.3 dBTP.

**AI prompt**: *"Industrial boss music at 147 BPM in E minor: a distorted sixteenth-note machine riff that slowly sags out of tune, a hard kit with struck sheet metal, a choir chanting a falling alien line; in the last third the choir sings the villain's four-note motif. Seamless loop."*

## ending-r84-a — "Stand Down, Aegis" (#27 Ending (new), 64.8 s)

**Proposed** (round 84).

**Brief**: Plays: under the ending pages after Level 50's debrief, the end card and the name entry (in the act-complete fanfare's place); should tell the main motif's story once more, never triumphant, the Choir motif once at peace, an open last chord. This one: orchestral (strings, horns, piano, a wordless choir): a solo horn alone (the intro), then strings carry the main motif, the piano joins in broken chords, the full statement in D major (horns, trumpet, strings, timpani, choir; loop bars 8–11), the Choir motif from D at peace in the strings (bars 12–13, its E♭ resolving to D over a D major chord), the last bars over a quiet low E♭ under D that does not resolve before the loop.

**Tempo / key**: 75 BPM, D minor to D major. **Measured**: 64.8 s, loop 317520 + 2257920 samples (7.200 s + 51.200 s), seam 0.23, −13.9 LUFS, −1.6 dBTP.

**AI prompt**: *"A quiet orchestral ending at 75 BPM: a solo horn plays a five-note motif alone, strings and piano take it up, then a full orchestral statement in D major; a falling line resolves peacefully; the last chord rests over a low E-flat that never resolves. Seamless loop after the intro."*

## ending-r84-b — "Home Light" (#27 Ending (new), 64.7 s)

**Proposed** (round 84).

**Brief**: Plays: as sketch a. This one: warmer and more personal: the hangar theme's flute and soft electric piano (quarter-note broken chords throughout) grown into an orchestra: the main motif in the flute (the intro and loop bars 0–7), strings in, the major statement with horns, strings and timpani (bars 8–11), the Choir motif at peace in the flute (bars 12–13), settling with the low E♭ under the last two bars.

**Tempo / key**: 90 BPM, D major. **Measured**: 64.7 s, loop 264600 + 2352000 samples (6.000 s + 53.333 s), seam 0.82, −14.2 LUFS, −1.8 dBTP.

**AI prompt**: *"A warm ending at 90 BPM in D major: soft electric piano and flute play a five-note motif, strings join, horns and timpani lift it into a full statement, a falling line resolves gently, the end rests over a low E-flat. Seamless loop after the intro."*

## credits-r84-a — "Coalition Forever" (#28 Credits (new), 65.6 s)

**Proposed** (round 84).

**Brief**: Plays: the credits roll after the campaign and the main menu's Credits item; should be the title theme's upbeat remix. This one: the title theme (track 1's chords and its B and C melodies) as tracker trance (track 4's family: the campaign's first sound back at its end): a four-on-the-floor kick, an off-beat bass, a 16th pulse arpeggio, claps, a supersaw pad; the main motif and the title's melodies in the supersaw lead; a breakdown (loop bars 16–23) on the title's slow C melody.

**Tempo / key**: 140 BPM, D minor. **Measured**: 65.6 s, loop 321300 + 2419200 samples (7.286 s + 54.857 s), seam 0.50, −14.2 LUFS, −0.9 dBTP.

**AI prompt**: *"Upbeat tracker trance at 140 BPM in D minor: four-on-the-floor, off-beat bass, a sixteenth-note pulse arpeggio, claps, supersaw pads; a heroic five-note motif and the title theme's melodies in a supersaw lead; a breakdown on a slow melody. Seamless loop."*

## credits-r84-b — "Seven Acts" (#28 Credits (new), 61.2 s)

**Proposed** (round 84).

**Brief**: Plays: as sketch a. This one: synth-orchestral over a breakbeat, the main motif told in each act's colour four bars each: Earth's brass stabs and brass lead, Mars's double reed and hand drums, Europa's sonar and pad with a flute, the belt's struck metal and drop bass with a sequencer, Jupiter's storm strings and thunder, the Choir's voices (its motif, then the main motif sung), ending on the title's brass statement with trumpet answers (loop bars 24–31).

**Tempo / key**: 150 BPM, D minor. **Measured**: 61.2 s, loop 299880 + 2257920 samples (6.800 s + 51.200 s), seam 0.27, −14.2 LUFS, −1.2 dBTP.

**AI prompt**: *"Synth-orchestral breakbeat at 150 BPM in D minor: one heroic five-note motif told in seven colours in turn (brass, a desert reed with hand drums, sonar pings and flute, struck metal and drop bass, storm strings and thunder, a choir), ending on a full brass statement. Seamless loop."*

## memory-filter-r84-a (Level 49's memory filter, 40.0 s)

**Proposed** (round 84).

**Brief**: Plays: Level 49, every echo's music and every remembered bed as the Choir remembers it: offline memory copies of tracks 4, 8, 12, 18 and 19 (the boss warning unfiltered); heard here on the Iron Sovereign's echo. This one, the filter as drafted: 6 s of the built track 19 (`assets/music/iron-sovereign.ogg` from its loop start), a 2 s crossfade as the memory forms, then the memory copy: slowed 6 % with its pitch (about a semitone down), a low-pass at 2.5 kHz (12 dB/octave), a 3.5 s hall at 40 % wet, the stereo narrowed to 60 %, −4 dB against the original; the file levelled to −14 LUFS.

**Tempo / key**: track 19 (built). **Measured**: 40.0 s, −14.0 LUFS, −1.0 dBTP.

## memory-filter-r84-b (Level 49's memory filter, 40.0 s)

**Proposed** (round 84).

**Brief**: Plays: as a. This one, a heavier memory on the same cut: slowed 10 % (about 1.8 semitones) with a slow tape wow (±0.3 %, 0.5 Hz), a low-pass at 1.4 kHz (24 dB/octave), a 5 s hall at 55 % wet, the stereo at 30 %.

**Tempo / key**: track 19 (built). **Measured**: 40.0 s, −14.1 LUFS, −1.0 dBTP.

Concept round 85: Step MUS-R85 (M11, 2026-11-05), the round-84 answers' re-takes, synthesized by `tools/concept/audio/music_r85.py` (deterministic: fixed seeds; it imports music_r84.py's voices and tables). The two theme/fight loops use round 84's format (intro + loop + a 2-bar fade tail, `LOOPSTART`/`LOOPLENGTH` in samples); the two endings are one-shots (no loop tags). −14 LUFS, a −2 dBFS ceiling. Measured only, not listened to by Claude: section lists from the score (`--sections`), ffmpeg ebur128, a spectrogram, the loop seam. Pending the user's review in round 85.

## act7-a-theme-r85-a — "Far Shore", the answer in low brass (#16 Act 7 A theme, 65.6 s)

**Proposed** (round 85).

**Brief**: Plays: Levels 43 and 45 and the Level 48 approach (the push through). Round 84's a, kept (picked), but the Choir motif's answer is no longer the formant choir, which did not sit with the main motif in the horns (the user's round-84 note). This one: the answer (E D C B, F E, E-rooted so its F rubs the key) in low brass (trombones and a bass trombone an octave under) doubled by a detuned synth lead (Event Horizon's family, dark, slow vibrato): the brass section answering its own horns, the synth giving it the cold. The breakdown (loop bars 16–19) is kept, carried by the same voice over the pad (bar 16, an octave lower at 18) with only a faint "oo" choir chord under it. Sections: 0:00 intro (ostinato, pad, the break from bar 2) · 0:07 A, horns' main motif at bar 0, the answer at 4 · 0:21 B, horns at 8, the answer at 12 · 0:34 C breakdown · 0:41 D all in, horns at 20 and 24 · 0:55 E the answer at 28 · loop at 1:02.

**Tempo / key**: 140 BPM, D minor. **Measured**: 65.6 s, loop 321300 + 2419200 samples (7.286 s + 54.857 s), seam 0.46 (no click; RMS −12.2 dB before the loop end, −12.0 dB after the loop start: no gap), −14.1 LUFS, −1.6 dBFS; the `choir` stem (the answer) 5.0 / 5.0 / 2.8 / 5.2 dB under the mix over its statements (bars 4, 12, 16, 28), levelled to round 84's.

**AI prompt**: *"Driving synth-orchestral loop at 140 BPM in D minor over a breakbeat: a low string ostinato, a rising five-note horn motif, answered every eight bars by a falling six-note phrase in trombones doubled by a dark, detuned synth lead; a breakdown where the trombones and synth carry the falling phrase over a pad. Seamless loop."*

## act7-a-theme-r85-b — "Far Shore", the answer in strings and glass (#16 Act 7 A theme, 65.6 s)

**Proposed** (round 85).

**Brief**: As act7-a-theme-r85-a, the answer in the orchestra's strings instead: violins and cellos in octaves with a bowed-glass shimmer on the violins' line. The horns call and the strings answer (the same orchestra, so it sits with the motif); the Choir's otherness is kept by its E-rooted flat 2 against D minor and the glass's slightly stretched partials. The breakdown: the strings and glass over the pad, the faint "oo" chord under it. Same sections and timings as a.

**Tempo / key**: 140 BPM, D minor. **Measured**: 65.6 s, loop 321300 + 2419200 samples, seam 0.48 (RMS −12.4 / −12.1 dB around the seam), −14.1 LUFS, −1.6 dBFS; the `choir` stem 4.9 / 5.0 / 2.9 / 4.9 dB under the mix.

**AI prompt**: *"Driving synth-orchestral loop at 140 BPM in D minor over a breakbeat: a rising five-note horn motif answered by a falling six-note phrase in violins and cellos in octaves with a cold bowed-glass shimmer; a breakdown of strings and glass over a pad. Seamless loop."*

## vorne-last-stand-r85-a — "Fused", five sections (#34 Vorne's last stand, 125.2 s)

**Proposed** (round 85).

**Brief**: Plays: Level 48 after the mid-boss sting until Vorne dies (it cuts with his last line). Round 84's a sound (picked) as a 1:55 loop with real development, never the same 8 bars more than twice in a row (the user: "you can't play the 2 tunes over and over"). Orchestral-martial: timpani, snare rudiments, low-brass 8ths, galloping strings, the choir in long chords; Vorne's motif (E G B D♯) in cold brass, its last note bending a semitone down into the choir's tritone drop (B F E). Sections: 0:00 intro · 0:06 A statement (round 84's: his motif at loop bars 0 and 8, the drop at 2 and 10) · 0:32 B bridge: a new march tune in the relative major, trumpets (16–23), then horns with a string descant (24–31), dotted low brass, march snare · 0:58 C breakdown: drums out, his motif augmented (half speed) in a muted horn over the choir (34; 38 bending into the drop) · 1:14 build (timpani crescendo, snare roll) · 1:17 D variation in F minor (a semitone up, the Choir's flat 2): the motif syncopated in horns and trumpets over a 16th riff made of the motif in the low strings · 1:39 the turn: the whole brass section's C chord bends a semitone down into B · 1:42 E the bend: back in E minor, his motif in full brass and trumpet into the full choir's drop (with low brass), the Choir's whole motif sung over him (64) · loop at 2:02.

**Tempo / key**: 150 BPM, E minor (F minor in section D). **Measured**: 125.2 s, loop 299880 + 5080320 samples (6.800 s + 115.200 s), seam 0.40 (RMS −12.3 / −13.4 dB around the seam: no gap), −14.1 LUFS, −1.8 dBFS.

**AI prompt**: *"An orchestral-martial boss loop at 150 BPM in E minor, about two minutes: snare rudiments, timpani, low-brass eighths and galloping strings under a choir; a four-note brass motif whose last note bends down a semitone into a choir's falling tritone; a march bridge in the relative major, a drumless breakdown with a muted horn, a variation a semitone higher with a driving string riff, and the whole orchestra bending back down into the key for a final statement. Seamless loop."*

## vorne-last-stand-r85-b — "Fused", six sections (#34 Vorne's last stand, 125.2 s)

**Proposed** (round 85).

**Brief**: The same sound and plan as vorne-last-stand-r85-a, a different arrangement. Sections: 0:00 intro · 0:06 A statement in canon: his motif in cellos and low brass (0), horns (2), trumpets bending (4), the drop at 6; full brass at 8, trumpets at 12 · 0:32 B chorale: the choir and strings sing a new slow hymn over a falling bass (E, D♯, D, C♯, C, B…), march snare, trumpet stabs · 0:51 C breakdown: the choir itself sings Vorne's motif (he has been absorbed) · 1:01 build · 1:04 D variation in G minor: the motif in horns in a 3-against-4 rhythm (36, 40, 44), stately in trumpets (48), the choir's drop from G (50) · 1:30 E the bend: his motif augmented in full brass bending into an augmented drop, over the 16th riff · 1:49 F turnaround: the roles swapped, the choir sings his motif, the brass plays the Choir's · loop at 2:02.

**Tempo / key**: 150 BPM, E minor (G minor in section D). **Measured**: 125.2 s, loop 299880 + 5080320 samples, seam 0.48 (RMS −14.7 / −15.5 dB around the seam), −14.0 LUFS, −1.7 dBFS.

**AI prompt**: *"An orchestral-martial boss loop at 150 BPM in E minor, about two minutes: a four-note brass motif in canon through cellos, horns and trumpets, its last note bending into a choir's falling tritone; a slow choral hymn over a falling bass; a breakdown where the choir sings the brass motif; a variation in G minor in a three-against-four rhythm; the motif augmented in full brass; brass and choir swapping motifs at the end. Seamless loop."*

## ending-r85-a — "Stand Down, Aegis", through-composed (#27 Ending, 83.0 s)

**Proposed** (round 85).

**Brief**: Plays once under the ending pages after Level 50's debrief and the end card. Recomposed after the user's round-84 note (the motif too short to loop; it should continue, not start over): a few statements of the main motif while the instruments join, then the melody continues into new phrases and never restarts (credits-r84-a's hand-over at 0:23 the model). Round 84's a voice (horn, strings, piano). Sections: 0:00 the solo horn alone, the main motif · 0:06 the motif again, strings in under the horn · 0:13 the strings carry it, the piano joins · 0:19 the motif in strings and horn, its second bar continuing (no held F) into · 0:26 a second strain in F major (the title theme's C-A-C-F phrase) · 0:38 the climb: rising string phrases, horns, a timpani roll · 0:51 the full statement in D major, extended to four bars (brass, trumpet, strings, choir, timpani) · 1:04 the Choir motif at peace in the strings, its E♭ resolving to D · 1:10 the open ending: D major over a low E♭, the horn's first two notes, fading to silence by 1:23.

**Tempo / key**: 75 BPM, D minor to D major. **Measured**: 83.0 s one-shot (no loop tags), −13.9 LUFS, LRA 7.4 LU, −1.9 dBFS; the bar loudness rises from the horn (−19 dB RMS) through the strain (−18) to the D major statement (−13), the last second silent.

**AI prompt**: *"A through-composed orchestral ending at 75 BPM, about 80 seconds, played once: a solo horn states a five-note motif, strings and piano take it up, then the melody continues into a new strain and a rising climb, a full orchestral statement in D major, a falling line resolving peacefully, and a last chord over a low E-flat that never resolves, fading out."*

## ending-r85-b — "Home Light", through-composed (#27 Ending, 85.7 s)

**Proposed** (round 85).

**Brief**: As ending-r85-a, in round 84's b voice: the hangar theme's flute and soft electric piano grown into an orchestra. Sections: 0:00 the electric piano alone · 0:05 the flute's main motif (D major) · 0:11 the motif from A, strings in · 0:16 the motif again, a cello counter-line · 0:21 the motif in flute and horn with timpani, its second bar continuing into · 0:27 a second strain in the flute (G, D/F♯, Em, A) · 0:37 the strings take the strain an octave lower under a flute descant, climbing into · 0:48 the climb, flute and strings rising, horns, timpani · 0:59 the full statement in D major, extended (brass, strings, flute, choir, timpani) · 1:09 the Choir motif at peace in the flute · 1:15 the open ending: D major over a low E♭, the piano's first notes of the motif, fading to silence by 1:26.

**Tempo / key**: 90 BPM, D major. **Measured**: 85.7 s one-shot, −13.9 LUFS, LRA 9.9 LU, −1.7 dBFS; the bar loudness rises from the piano (−26 dB RMS) to the statement (−13).

**AI prompt**: *"A warm through-composed ending at 90 BPM, about 85 seconds, played once: a soft electric piano, a flute states a five-note motif in D major as strings, cellos and a horn join, then the flute sings a new strain, the strings take it over, a rising climb into a full orchestral statement, a peaceful falling flute line, and a last chord over a low E-flat that never resolves, fading out."*

Round 86 (step MUS-R86), three Act 7 tracks recomposed with tunes:

Step MUS-R86 (M11, 2026-11-05), the third take of three Act 7 tracks after the user's round-85 notes, synthesized by `tools/concept/audio/music_r86.py` (deterministic, fixed seeds; it imports music_r84.py's and music_r85.py's voices, tables and helpers and changes neither). This time every tune is written first as a melody: a main theme in phrases that ask and answer, each ending on a cadence; a second theme with its own tune; sections that change only where a phrase has ended; one tempo per track. The generator's self-check (`--check`) prints the phrase map, the motif share, the section starts and the strong-beat non-chord tones. The loops use round 84's format (intro + loop + a 2-bar fade tail, `LOOPSTART`/`LOOPLENGTH` in samples); the ending is a one-shot. All at −14 LUFS with a −2 dBFS ceiling. Checked only by analysis, not listened to by Claude: the self-check, ffmpeg ebur128, a spectrogram, the loop seam. Pending the user's review in round 86.

## act7-a-theme-r86-a — "Far Shore", recomposed with tunes (#16 Act 7 A theme, 2:07)

**Proposed** (round 86).

**Brief**: Plays: Levels 43 and 45 and the Level 48 approach (the push through), looping. Round 84 a's background rhythm is kept (the breakbeat, the low string ostinato, the reese bass, 140 BPM), which the user liked. There is no Choir answer over the main motif. **Theme A** is a 16-bar horn tune grown from the main motif. The antecedent starts with the motif, rises to F, falls to A, then makes a second, longer rise to A5 and sighs down onto E over A (a half cadence). The consequent starts with the motif again, moves through a sequence a third higher, climbs to B♭5 and comes down through G, E and C♯ to D (a full cadence). **Theme B** is the strings' own tune in F major: falling thirds that step down, each second note off the beat, a half cadence, then an answer that climbs to D6 and ends on F. A climbing **bridge** leads onto A major (the dominant). The Choir motif appears only once, alone, as a colour in the breakdown, over C, B♭ and A, the chords its notes belong to. The track builds up phrase by phrase: horn alone over the break, then hats and strings, then the strings' tune, then the peak, the breakdown, the rebuild and the turnaround.

Sections: 0:00 intro (round 84's: the ostinato, a pad, the break) · 0:07 A, theme A in the horn over the break and the ostinato (sparse); hats, strings and a second horn join at 0:21 for the consequent · 0:34 B, theme B in the cellos, then in the violins with a horn counter-line from 0:48 · 1:02 bridge, a climbing string sequence onto A, a snare build and a riser · 1:09 A peak, theme A in horns, trumpets and violins, a string descant, all drums · 1:36 breakdown, drums out, theme B's first phrase in bowed glass, then the Choir motif alone at 1:43 (C → B♭ → A) · 1:46 rebuild, theme A's consequent with the kick (1:46), the break (1:50), and hats and strings (1:53) · 2:00 turnaround, B♭ → A, the horn's line leading into bar 0 · loop at 2:04.

**Tempo / key**: 140 BPM throughout, D minor (B in F major). **Self-check**: motif cells in 12 of 68 loop bars (18 %), 0 strong-beat non-chord tones, 0 sections starting mid-phrase, every phrase cadenced (HC / PAC / Phrygian HC). **Measured**: 127.3 s, loop 321300 + 5140800 samples (7.286 s + 116.571 s), seam 0.55, no click. RMS is −15.1 dB before the loop end and −18.8 dB after the start: the turnaround drops to the sparse bar 0 by design, with no gap. −14.0 LUFS, LRA 6.9 LU, −1.5 dBFS. Bar loudness: A about −18 dB, the peak about −14, the breakdown −22, the rebuild rising from −18 to −15.

**AI prompt**: *"Driving synth-orchestral loop at 140 BPM in D minor over a breakbeat and a low string ostinato, about two minutes: a sixteen-bar heroic horn melody in two answering phrases that grows from a rising five-note motif; a second, lyrical string melody in F major in cellos and then violins with a horn counter-line; a climbing bridge onto the dominant; the horn melody at full orchestra; a drumless breakdown with bowed glass and a lone choir phrase; a rebuild layer by layer; a two-bar turnaround back to the start. Seamless loop."*

## vorne-last-stand-r86-a — "Fused", with Vorne's own theme (#34 Vorne's last stand, 2:18)

**Proposed** (round 86).

**Brief**: Plays: Level 48 after the mid-boss sting until Vorne dies (it cuts with his last line), looping. vorne-last-stand-r85-a's opening is kept as it was up to 1:16 (the user: "starts nice"), and its tempo (150 BPM) and its normal rhythm hold throughout: no syncopated or 3-against-4 section. After that comes new material.
- **Theme V, "Vorne the man"**: a 16-bar lament. The antecedent is a rising sixth (B to G), a falling sigh (F♯ E D♯), a reach up to C6 and a long fall to a half cadence on B. The consequent rises a fourth, reaches D6 over D7 and falls through C6 A5 G5 F♯5 to E (a full cadence).
- **A development on the first theme**: the head of Vorne's motif (1 ♭3 5) in the horns, answered by theme V's sigh in the violins. The pair climbs a step at a time (Am–B7, Bm–C, C–D), the trumpets play the head on Em, and a string run leads into the climax.
- **The climax**: his motif in full brass under the violins' held B, its D♯ bending down. The Choir's tritone drop follows: B over Em, F over F, E over Em (a Phrygian cadence; chords its notes belong to). Then theme V's cadence is played by the full orchestra.
- **The turnaround**: C–Am–F–B in the horn, the drums out, then a timpani-and-snare build back to bar 0.

Sections: 0:00 intro (kept) · 0:06 A statement (kept) · 0:32 B march bridge (kept) · 0:58 C breakdown (kept) · 1:14 build (kept) · 1:17 D theme V in cellos and horn, then the violins at 1:30 · 1:42 E development · 1:55 F climax: the motif and the drop, then theme V's cadence at 2:02 · 2:08 turnaround · loop at 2:15.

**Tempo / key**: 150 BPM, E minor. **Self-check**: motif cells in 24 of 80 loop bars (30 %; the new material has 8 of 36, 22 %). 0 unresolved strong-beat non-chord tones in the new material. The kept r85 opening has 21: Vorne's D♯ over Em, the drop's B over C and its F/E over Em/F in the breakdown, the march tune's last A over B. They are left as they were because the brief keeps that opening. 0 sections starting mid-phrase. **Measured**: 138.0 s, loop 299880 + 5644800 samples (6.800 s + 128.000 s), seam 0.41, RMS −11.4 / −12.4 dB around the seam, −14.1 LUFS, LRA 4.7 LU, −1.6 dBFS. Compared with r85 a, the kept opening is 0.6 dB louder (the mastering), and the residual after matching the gain is 25–30 dB under the signal (reverb tails at the loop start).

**AI prompt**: *"An orchestral-martial boss loop at 150 BPM in E minor, about two minutes and a quarter: snare rudiments, timpani, low brass and galloping strings under a choir; a cold four-note brass motif bending into a choir's falling tritone; a march bridge; then a tragic lyrical string-and-horn melody for the fallen commander in two answering phrases; a development where horns and violins trade fragments up a rising sequence; a climax where the brass motif bends into the choir's tritone over a Phrygian cadence and the lament returns in full orchestra; a turnaround back to the start. One steady tempo. Seamless loop."*

## ending-r86-a — "Stand Down, Aegis", the melody finished (#27 Ending, 1:42)

**Proposed** (round 86).

**Brief**: Plays once under the ending pages after Level 50's debrief and the end card. ending-r85-a is kept as it was up to 1:04 (the user: "very good until it suddenly breaks at 1:04"). At 1:04 the D major statement's antecedent, which ended on a half cadence with a pickup, is answered by its consequent in the same instruments and rhythm (bars 20–23). It ends with a full cadence on D (A7 → D) at 1:13, so the melody finishes before anything new begins. Only then does the Choir motif at peace start, in its own phrase in the strings (1:17). Its notes sit over chords they belong to: D C B♭ A over G minor, the E♭ over E♭ major resolving to D. The horn answers with a plagal close (G → D, 1:23). Then comes the open ending: D major over the low E♭, faded to silence.

Sections: 0:00–1:04 as r85 a (horn, strings, piano, the second strain, the climb, the statement's antecedent) · 1:04 the statement's consequent, a full cadence on D at 1:13 · 1:17 the Choir motif at peace (strings) · 1:23 the horn's answer (G → D) · 1:30 the open ending (D major over a low E♭, piano, faded) · silence by 1:42.

**Tempo / key**: 75 BPM, D minor to D major. **Self-check**: motif cells in 12 of 31 bars (39 %; the new bars have 2 of 11, 18 %; the kept opening is the motif stated four times). 0 unresolved strong-beat non-chord tones in the new material. The kept part has 3: the motif's held F over A (bar 5) and G over A in the strain (bar 11). 0 sections starting mid-phrase. **Measured**: 102.2 s one-shot, −13.9 LUFS, LRA 7.7 LU, −1.8 dBFS. Compared with r85 a, the kept opening is 0.5 dB quieter (the mastering), and the residual after matching the gain is about 30 dB under the signal. Bar loudness: the statement −15, the Choir at peace −20, the open ending −25 fading to silence.

**AI prompt**: *"A through-composed orchestral ending at 75 BPM, about 100 seconds, played once: a solo horn states a five-note motif, strings and piano take it up, a second strain and a climb lead to a full D major statement in two answering phrases ending on a firm cadence; then a quiet falling string phrase over G minor and E-flat resolving to D, a horn's gentle amen, and a last D major chord over a low E-flat that never resolves, fading out."*

Round 87 (step MUS-CHK), the two Act 7 tracks picked in round 84 checked, and a recomposed take of each:

Step MUS-CHK (M11, 2026-11-05): the two Act 7 tracks picked in round 84 and never re-checked (track 17 b "Hollow Light", track 28 a "Coalition Forever") measured against round 86's standard, and one recomposed take of each, synthesized by `tools/concept/audio/music_r87.py` (deterministic, fixed seeds; it imports music_r84.py's voices and tables and music_r86.py's Score self-check and changes neither). `--check-r84` prints the round-84 checks, `--check` the new takes'. No other Act 7 music is new for this act and unchecked: tracks 16, 34 and 27 were redone in round 86; track 20 is the round-03 sketch extended in production; the memory copies are filters; the stings are built. Every tune is written first as a melody in phrases that ask and answer, each ending on a cadence; sections change only where a phrase has ended; one tempo per track. The loop uses round 84's format (intro + loop + a 2-bar fade tail, `LOOPSTART`/`LOOPLENGTH` in samples); the credits are a one-shot. Both at −14 LUFS with a −2 dBFS ceiling. Checked only by analysis, not listened to by Claude: the self-check, ffmpeg ebur128, a spectrogram, the loop seam. Pending the user's review in round 87.

## The round-84 takes, checked

- **act7-b-theme-r84-b "Hollow Light"** fails. Its only tunes are motif cells: the Choir motif twice and the main motif once. That is 6 of 20 bars (30 %, 2 re-keyed), and all 6 of its tune bars. No phrase is answered or cadenced, so none of its 3 section starts follows a phrase end. The second Choir statement's F and E sit over G major: 2 unresolved strong-beat clashes. The loop repeats wholesale after 49.0 s.
- **credits-r84-a "Coalition Forever"** fails. Motif cells fill 16 of 32 bars (50 %), and 12 of those are the cell re-keyed (the B melody's opening in F) or re-timed (the slow C melody twice). The A section is the bare main motif twice, unanswered, with 2 silent bars after each. There are 6 unresolved strong-beat clashes: the B melody's A over C four times, and the C melody's F over C and E over B♭. The B melody plays twice in the 32 bars, an 8-bar block heard twice. Every phrase ends on a half cadence, and the loop repeats after 54.9 s.

## act7-b-theme-r87-a — "Hollow Light", written as tunes (#17 Act 7 B theme, loops 2:17)

**Proposed** (round 87).

**Brief**: Plays in Levels 44, 46 and 47 and over the Level 50 approach, looping (the unsettled half of Act 7). Round 84 b's sound is kept, as the user picked it: the bowed-glass pads, the soft heartbeat kick, the sub, reversed glass swells into the bar lines, a breath of band noise, the distant trumpet with the main motif once per loop, and a wordless solo voice ("oo") for the Choir.
- **Theme G** is a 16-bar tune in bowed glass. The glass has a quicker bow than the pads, so short notes still speak. The antecedent is a rising call (B E G), a long fall to a half close on F♯, then the call again reaching higher (B5 over G, C6), settling on F♯ over B7 (a half cadence). The consequent climbs to D6 over G and falls through C6 A5 F♯5 D♯5 to E (a full cadence).
- **Theme T** is the distant trumpet's own tune in G major. It is opened by the main motif in E (E B E F♯ G, its only statement in the loop), then a dotted, speaking line falls to a half close on D. The answer rises from G and falls back to G (D → G).
- **The peak** is theme G with the glass doubled an octave down and a soft glass shimmer on the chord tones. Under the consequent, the wordless voice doubles the tune an octave below.
- **The breakdown**: the heartbeat and the sub stop and the glass pads play alone. Then the voice sings the Choir motif alone: E D C B over A minor, then its F over F major falling to E over E minor, a Phrygian cadence on chords its notes belong to. It is answered in the voice's own phrase (C E D, D♯ over B).
- **The rebuild** is theme G's consequent with the heartbeat coming back. The turnaround is C → B in the glass, leading into bar 0.

Sections: 0:00 intro (round 84's: the glass alone, the heartbeat from its bar 2) · 0:10 A: theme G in the glass, the heartbeat on beat 1, then both beats and the sub from 0:29 · 0:49 T: the trumpet's tune, the breath and the reversed swells · 1:09 peak: theme G doubled and shimmering, the voice under the consequent from 1:28 · 1:48 breakdown: the glass alone, then the Choir motif in the voice at 1:53 and its answer · 2:02 rebuild: theme G's consequent, the heartbeat back, the sub from 2:12 · 2:22 turnaround · loop at 2:27.5.

**Tempo / key**: 98 BPM throughout, E minor (T in G major). **Self-check**: motif cells in 4 of 56 bars (7 %; 2 re-keyed). 0 strong-beat non-chord tones. 0 sections starting mid-phrase, and every phrase is cadenced (HC / PAC). 35 new tune bars; the longest block heard twice is 8 bars (theme G's antecedent in A and in the peak, orchestrated differently). **Measured**: 152.4 s file, loop 459000 + 6048000 samples (10.408 s + 137.143 s), seam 0.24, RMS −18.7 / −18.9 dB around the seam (no click, no gap). −14.2 LUFS, LRA 6.8 LU, −1.8 dBFS. By section: A −16.8 dB, T −14.9, peak −15.3 then −13.6, breakdown −24.5 (the glass alone) then −16.1 (the voice), rebuild −16.3, turnaround −15.9. Stems for production as the README says (base: heartbeat, sub, glass pads; full; `choir`: the voice). In this take the voice is under the peak's consequent and in the breakdown.

**AI prompt**: *"An ambient, glassy space loop at 98 BPM in E minor, about two and a quarter minutes: bowed-glass pads and a soft heartbeat kick over a sub; a slow, singing bowed-glass melody of sixteen bars in two answering phrases; a distant trumpet's own melody, opened once by a rising five-note motif, in G major; the glass melody again, doubled and shimmering, with a wordless solo voice under it; a breakdown where the heartbeat stops and the lone voice sings a falling alien phrase that settles in a Phrygian cadence; the heartbeat returning under the glass melody; a two-bar turnaround. Seamless loop."*

## credits-r87-a — "Coalition Forever", the title theme given a form (#28 Credits, plays once, 2:58)

**Proposed** (round 87).

**Brief**: Plays once under the credits roll after the campaign (and from the main menu's Credits item). Note: the credits README still says the track loops until the roll is left; this take follows the step brief (a one-shot that ends on D). Round 84 a's tracker trance is kept, as the user picked it: four-on-the-floor kick, off-beat bass, the 16th pulse arpeggio, claps, the supersaw pad and the supersaw lead, 140 BPM, D minor. The title theme's approved tunes are kept note for note: its A melody, its B melody, its slow C melody, and the full title's bridge and two calls. What is new is a real form and the answers that close the tunes:
- **A's consequent** opens high (A F C) instead of on the motif, climbs to D6 and falls through G E C♯ to D (a full cadence).
- **B's consequent** (F major) opens on C6 falling, climbs again to D6 and closes onto F.
- **The slow C melody's answer** is G4 to D5, F D, B♭ C♯, D.
- Under the B melody's A, the chord is now A minor instead of C. The old A-over-C clash was unresolved.

Sections: 0:00 intro (the arp alone, its filter opening; the kick and the title's call in the lead at 0:07) · 0:14 verse: the title's A melody answered by its new consequent (0:27), kick, off-beat bass, hats, claps · 0:41 chorus: the title's B melody answered (0:55), 16th hats, brass under the lead · 1:09 bridge: the full title's bridge (Gm–E♭–B♭–F), strings doubling, rolling bass · 1:22 breakdown: drums out, the slow C melody over the pad and a closed arp, answered (1:29) · 1:36 build: the title's two calls, the kick back, a snare roll and a riser, the filter opening · 1:50 chorus at the peak, the lead doubled an octave up · 2:17 verse at the peak, lead and brass · 2:45 outro: the title's brass call closing on D, the kick out at 2:48, the last D minor chord (2:52) fading out by 2:58.

**Tempo / key**: 140 BPM throughout, D minor (the chorus in F major). **Self-check**: motif cells in 26 of 102 bars (25 %). Of those, 20 are re-keyed or re-timed: they are the title's own phrase openings (the B melody in F, the slow C melody, the calls). 9 strong-beat non-chord tones, all resolved appoggiaturas, 0 unresolved. 0 sections starting mid-phrase, and every phrase is cadenced. The verse and the chorus each come twice (the form). **Measured**: 177.9 s one-shot, −14.0 LUFS, LRA 4.1 LU, −1.7 dBFS (true peak −0.9 dBTP). By section: intro −28 then −17.7, verse −15.1, chorus −14.8, bridge −14.9, breakdown −19.9, build −15.7, peak −14.7, outro −15.8, the last chord −18.4 and fading to silence.

**AI prompt**: *"An upbeat tracker-trance remix of a heroic D-minor title theme at 140 BPM for an end-credits roll, about three minutes, played once: four-on-the-floor kick, off-beat bass, a 16th-note pulse arpeggio, claps, a supersaw pad and lead; an intro on the theme's brass call; a verse and a chorus where each of the theme's melodies is answered and closes on a cadence; a bridge; a drumless breakdown on a slow melody; a snare-roll build on the theme's calls; the chorus and verse again at full energy; an outro that ends on the theme's call landing on D."*

## act7-a-theme-full-r86-a, act7-b-theme-base-r87-a — the Act 7 themes' production stems (M11 part B, step MUS-B)

**Chosen** (cut from round 86 M.1 and round 87 M.1; no new composition). By `tools/concept/audio/music_r86.py --stems`
(track 16) and `music_r87.py --stems` (track 17): each chosen take is rendered again by its own function (the same
score, voices, chains and seeds), re-encoded byte-identical to the chosen file as a check, and every mix group is kept
apart; a stem is a sum of the chosen mix's own groups with the chosen mix's mastering gain (its loudness gains and its
limiter's gain curve), so the files share length, `LOOPSTART` / `LOOPLENGTH` and gain and play sample-aligned.

- `act7-a-theme-full-r86-a.ogg`: track 16's full stem, the chosen mix without its `choir` group.
- `act7-a-theme-base-r86-a.ogg`: track 16's base stem, the kick, the low string ostinato, the reese bass, the sub, the
  pads.
- `act7-a-theme-choir-r86-a.ogg`: track 16's `choir` stem, the Choir motif's one statement (loop bar 56) as the chosen
  mix has it; full + `choir` add back to the chosen file within −44 dB RMS.
- `act7-b-theme-base-r87-a.ogg`: track 17's base stem, the heartbeat, the sub and the glass pads (its full stem is the
  chosen file itself).
- `act7-b-theme-sparse-r87-a.ogg`: track 17's sparse base for Level 46, the heartbeat and the sub alone.

**Measured** (not listened to): track 16 127.29 s, loop 321300 + 5140800 samples; full −14.1 LUFS, seam 0.55; base
−19.6 LUFS, seam 0.14; `choir` −10.4 dBTP, 4.7 dB under the full stem in its window. Track 17 152.45 s, loop 459000 +
6048000 samples; base −18.3 LUFS, seam 0.43; sparse −19.8 LUFS, seam 0.00.
