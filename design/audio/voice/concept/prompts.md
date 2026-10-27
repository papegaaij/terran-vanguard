# Voice briefs and prompts — concept round 19

Round 19 casts the generic radio speakers of Act 1. Each speaker says one of its real lines with
two candidate reference voices (`-a`, `-b`), cloned zero-shot by Chatterbox and put through
radio filter b (round 18's choice). Reproduce with:

```
python3 tools/concept/audio/tts_r19.py          # generate (Chatterbox venv) + post-process
python3 tools/concept/audio/tts_r19.py --post   # post-process only (raw WAVs in ~/.cache/tv-tts/raw/r19/)
```

Generator: `tools/concept/audio/tts_r19.py` (Chatterbox 0.1.7, model `ResembleAI/chatterbox`,
set up as in `tools/concept/audio/tts_r18.py`; the filter is `radio()` there). Seed 19 + the
take's index, except Tranquility Control a (seed 4019: five other seeds ran on past the line).
Files `voice-<speaker>-r19-<a|b>.ogg`, OGG Vorbis q4, 44.1 kHz mono, −16 LUFS. Settings are
`exaggeration` / `cfg_weight` / `temperature` from the [voice](../README.md#speakers-and-expression)
expression table. Reference clips: [refs](../refs/README.md), sources in
[CREDITS.md](../../../../CREDITS.md). Pitch is the median F0 of the reference clip (female
≈ 165–255 Hz, male ≈ 85–155 Hz).

## voice-yard-control

Yard Control (L02, t 78), fierce (0.8 / 0.4 / 0.8): "Crane Four is still on automatic. Watch the arm, Aegis!"

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Mark Nelson | [A Princess of Mars, foreword (Burroughs)](https://archive.org/details/princess_mars_0810_librivox), cut at 60 s | public domain | 145 Hz |
| b | Elizabeth Klett | [Lady Audley's Secret, ch. 1 (Braddon)](https://archive.org/details/lady_audleys_secret_ek_librivox), cut at 60 s | public domain | 177 Hz |

## voice-dock

Dock One–Four (L02, t 34 (Dock One; the four docks share the lines)), shout (1.2 / 0.3 / 0.8): "Dock One to anyone! They're growing on the hull — burn it off us!"

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Tom Crawford | [The Sea Wolf, ch. 1 (London)](https://archive.org/details/sea_wolf_0907_librivox), cut at 60 s | public domain | 93 Hz |
| b | Kristin Hughes | [The Strange Case of Dr. Jekyll and Mr. Hyde, ch. 1 (Stevenson)](https://archive.org/details/jekyll_and_hyde_klh_0904_librivox), cut at 60 s | public domain | 167 Hz |

## voice-ring-control

Ring Control (L03, t 36.5), neutral (0.5 / 0.5 / 0.7): "Debris field ahead. Big pieces will stop your rounds. And theirs."

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Bob Neufeld | [A Study in Scarlet, part 1 ch. 1 (Doyle)](https://archive.org/details/studyinscarlet_bn_librivox), cut at 60 s | CC0 1.0 | 143 Hz |
| b | Cori Samuel | [Frankenstein (1818), preface (Shelley)](https://archive.org/details/frankenstein_cs_librivox), cut at 60 s | CC0 1.0 | 222 Hz |

## voice-tranquility-control

Tranquility Control (L04, t 86), shout (1.2 / 0.3 / 0.8): "Walkers coming over the crater rims, both sides of the road!"

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | John Greenman | [Adventures of Huckleberry Finn, explanatory note (Twain)](https://archive.org/details/huckleberry_finn_0908_librivox), cut at 60 s | public domain | 131 Hz |
| b | Karen Savage | [The Scarlet Pimpernel, ch. 1 (Orczy)](https://archive.org/details/scarlet_pimpernel_ks_librivox), cut at 60 s | public domain | 198 Hz |

## voice-crawler-one

Crawler One (L04, t 11), neutral (0.5 / 0.5 / 0.7): "Crawler One rolling. We're slow and we're loud, Aegis. Please don't leave us."

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Phil Chenevert | [Pollyanna, ch. 1 (Porter)](https://archive.org/details/pollyanna-1_pc_librivox), cut at 60 s | CC0 1.0 | 164 Hz |
| b | Kara Shallenberg | [Heidi, introduction (Spyri)](https://archive.org/details/heidi_solo_librivox), cut at 60 s | public domain | 197 Hz |

## voice-convoy

Convoy (L04, first crawler hit, {ally} = Three), shout (1.2 / 0.3 / 0.8): "We're taking fire! Crawler {ally} is hit!"

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Adrian Praetzellis | [Treasure Island, front matter (Stevenson)](https://archive.org/details/treasure_island_ap_librivox), cut at 60 s | public domain | 118 Hz |
| b | Judy Bieber | [Dorothy and the Wizard in Oz, "To My Readers" (Baum)](https://archive.org/details/dorothy_wizard_oz_librivox), cut at 60 s | public domain | 211 Hz |

## voice-hammer-lead

Hammer Lead (Airstrike special, the call), fierce (0.8 / 0.4 / 0.8): "Hammer flight, inbound!"

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Mark F. Smith | [The Lost World, ch. 1 (Doyle)](https://archive.org/details/lost_world_mfs_librivox), cut at 120 s | public domain | 102 Hz |
| b | Gord Mackenzie | [Scaramouche, book 1 ch. 1 (Sabatini)](https://archive.org/details/scaramouche_gm_librivox), cut at 60 s | public domain | 100 Hz |

## voice-driver-control

Round 21 (the Level 05 audition, user decision D4 of M4 part E), generated with
`tools/concept/audio/tts_r21.py` the same way as round 19 (seed 21 + the take's index). Driver
Control (L05, t 12.5), fierce (0.8 / 0.4 / 0.8): "Mass driver's still on automatic, Aegis. Sleds
every five seconds. Watch the rail lights."

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Alex Foster | [The Invisible Man, ch. 1–2 (Wells)](https://archive.org/details/invisible_man_librivox), cut at 60 s | public domain | 81 Hz (a deep, slow narrator) |
| b | Rebecca | [The War of the Worlds, book 1 ch. 1 (Wells)](https://archive.org/details/war_worlds_solo_librivox), cut at 60 s | public domain | 113 Hz (a low female voice) |

## voice-perimeter-beacon

Round 23 (the Level 06 audition, user decision D7 of M4 part F), generated with
`tools/concept/audio/tts_r23.py` the same way as round 21 (seed 23 + the take's index), through
the **public-address filter** (`pa()` in `tools/concept/audio/tts_r18.py`, next to the radio
filter): 250–4000 Hz horn band with resonances at 1.1 and 2.6 kHz, a driven horn's saturation, a
faint 60 Hz mains hum, slap echoes from the farther speakers at 0.13, 0.29 and 0.47 s and a
1.4 s hall reverb; no static and no squelch clicks. The Daedalus perimeter beacon (L06, t 50,
automated), flat (0.3 / 0.5 / 0.6): "…Daedalus perimeter. All residents report to shelter… all
residents report…" (the leading ellipsis is not spoken).

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Mark F. Smith | [The Time Machine (version 2), ch. 1 (Wells)](https://archive.org/details/time_machine_ms_librivox), cut at 60 s | CC0 1.0 | 105 Hz (an even, measured male narrator; Hammer Lead's rejected candidate a in round 19) |
| b | Lucy Burgoyne | [Madame Midas, ch. 1 (Hume)](https://archive.org/details/madame_midas_lb_librivox), cut at 60 s | public domain | 147 Hz (a calm female voice) |

Outcome (user, 2026-10-05): **a** cast (`refs/ref-perimeter-beacon.wav`; the production line by
`tools/art/voice.py` with `filter: pa`); b moved to `concept/rejected/`, its clip deleted.

## voice-lifeboat-seven

Round 25 (the Level 07 audition, M4 part G), generated with `tools/concept/audio/tts_r25.py` the
same way as round 21 (seed 25 + the take's index), through radio filter b. Lifeboat Seven (L07,
t 22, a drifting CDF lifeboat's crew calling in: tired, not panicking), neutral (0.5 / 0.5 / 0.7):
"Lifeboat Seven, crew of four, drifting. Aegis, that pod we're towing is yours if you cut it loose."
Whisper (faster-whisper base.en) reads both takes back whole; it hears "Aegis" as "eegis" (a) and
"ages" (b), and b's filtered take once as "telling" for "towing" (the raw take reads "towing").

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Tadhg Hynes | [Far From the Madding Crowd, ch. 1 (Hardy)](https://archive.org/details/far_from_the_madding_crowd_th_librivox), cut at 60 s | public domain | 105 Hz (a warm Irish male narrator) |
| b | Lizzie Driver | [Gulliver's Travels, part 1 ch. 1 (Swift)](https://archive.org/details/gulliver_ld_librivox), cut at 60 s | public domain | 190 Hz (a clear female narrator) |


Outcome (user, 2026-10-05): **a** cast (`refs/ref-lifeboat-seven.wav`; the production line by
`tools/art/voice.py` at the neutral settings through radio filter b); b moved to
`concept/rejected/`, its clip deleted.

## voice-choir-sings

Round 29 (2026-10-06): the sound of the Choir's stage direction `[the Choir sings]` (Act 1's five
Choir cues: Level 01 t 160, Level 03 t 58.5, Level 05 t 144.5, Level 07 t 94 and its
boss-destroyed cue), which is not spoken. Two kinds, two options each, generated with
`tools/concept/audio/choir_r29.py`; every option goes through radio filter b (`radio()` in
`tools/concept/audio/tts_r18.py`) and is mastered like the voice lines (−16 LUFS, −1.5 dBFS
ceiling; OGG Vorbis q4, 44.1 kHz mono, 3.7–4.0 s with the reverb's tail and the filter's pad).
The voice is the Choir's base, Varga's reference clip (`refs/ref-varga.wav`, Betsie Bush, public
domain), at the Choir's settings (0.3 / 0.3 / 0.7).

| Variant | Kind | What | How |
|---|---|---|---|
| a | sung sting (voice) | "Ooh", one held note (E3) | Chatterbox's own sustained vowel: a take of "Ooooooooooooooh." (seed 30) that holds the vowel for 16 s at about 170 Hz; its steadiest 2.2 s, level evened out, pitch-flattened onto E3 (165 Hz) with a slow vibrato (5.2 Hz, ±0.5 %) by TD-PSOLA (the voice's formants kept); then the Choir's layering as `choir()` of `tts_r18.py` (copies at −12, −5, 0 and +7 semitones, gains 0.8 / 0.55 / 0.45 / 0.3, staggered 0–45 ms, a reversed copy at 0.22, the 55/82 Hz drone with low noise, the 2.2 s reverb 35 % wet) |
| b | sung sting (voice) | "Ah", the Choir motif's landing: F3 falling to E3 | Chatterbox does not hold an "aah" (its "Aaaah" spellings close into "eh" or stop), so the tone is built: 0.5 s of the steady open vowel of a take of "Ahhhhhhhhhhhhhhhh..." (seed 31; F1 ≈ 440 Hz, F2 ≈ 1200 Hz), its pitch periods replayed back and forth by TD-PSOLA and sung as F3 for 0.85 s, a 0.15 s glide and E3 for 1.4 s (the motif's b2 to the root); then `choir()`, as a |
| c | choir pad (synthesized) | "Oo" chord E3 B3 E4 B4, held 1.8 s | the music's choir voice, `v_choir` of `tools/concept/audio/music_r02.py` (four detuned saws through three vowel formants, the pads of "The Choir Descends"), in the layering's voicing and gains; the music choir bus's 150 Hz high-pass, then `choir()`'s room without its copies (the 55/82 Hz drone with low noise, the 2.2 s reverb 35 % wet) |
| d | choir pad (synthesized) | The motif's last two notes, F4 then E4, sung "ah" over an "oo" pad on E4 and B4 | `v_choir` as in the intro of "The Choir Descends" (motif "ah" at 0.55, pad "oo" at 0.35 per voice; F4 0.75 s, E4 1.0 s); treated as c |

Brief: a few seconds of the alien Choir breaking into the radio with no words — eerie, choral,
in the key of the Choir's music (E minor / phrygian, the motif ending on the b2-to-root fall), and
plainly a transmission (band-limited, static, squelch clicks), at the loudness of the spoken
lines so it neither drowns them nor gets lost under the music's duck.

Outcome (user, 2026-10-06): **b** chosen, the sung "ah" F3 → E3 (the speaker table's `stage`,
copied into `assets/voice/choir/`); a, c and d moved to `concept/rejected/`.

## voice-civilian

Round 30 (the Level 08 audition, M5 part B, user decision D6 = a), generated with
`tools/concept/audio/tts_r30.py` the same way as round 25 (seed 30 + 10 × the variant's index + the
line's index), each line through radio filter b on its own (its clicks, −16 LUFS), the two joined
with 1 s of silence. The Ikoyi shelter civilian (L08, speaker `Civilian`, "shelter nine": a civilian
in a Nova Lagos shelter under attack, then relieved), neutral (0.5 / 0.5 / 0.7), both of her lines
as Level 08's data has them: t=113 "Shelter nine, Ikoyi! Walkers on the roofs, heading our way!"
and the secondary objective's "Shelter nine here. The roofs are quiet. Thank you, Aegis."
Re-rendered on 2026-10-07 with the t=113 line's current wording (the first renders of 2026-10-06
read the earlier "Aegis, this is shelter nine in Ikoyi! They're on the roofs above us!"); the same
clips, settings and seeds, so the second line is the same take up to GPU sampling. Measured: a
10.76 s, −16.1 LUFS; b 10.84 s, −15.9 LUFS (OGG Vorbis q4, 44.1 kHz mono). Whisper (faster-whisper
base.en) reads both files back whole: a word for word ("Shelter 9, Ikoyi, walkers on the roofs,
heading our way. Shelter 9 here, the roofs are quiet. Thank you, Aegis."); b the same except
"shelter nine" as "Sheltonine" twice. The raw takes (before the filter) read back word for word,
b's "Shelter 9" included; a's raw first line has "Ikoyee". Measured only, not listened to.

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | KirksVoice | [African Myths, ch. 1 (Woodson)](https://archive.org/details/africanmyths_2601_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 111 Hz (takes 132 / 121 Hz); a male voice by pitch |
| b | Faith Abiola-Ellison | [The Yoruba-speaking Peoples of the Slave Coast of West Africa, selections, section 1 (Ellis)](https://archive.org/details/yorubapeoples_2408_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 151 Hz (takes 149 / 148 Hz); the same reader reads Blyden's Lagos lecture for LibriVox; near Okafor's clip (150 Hz), who answers the t=113 call at t=120.5 |

Both chapters are Yoruba subjects (the Ifa creation myth, the Yoruba pantheon), chosen for a Lagos
speaker; the readers' accents are not claimed here, only the pitch (librosa pyin median over the
voiced frames). Neither reader is in the cast.

Outcome (user, 2026-10-07): **b** cast, Faith Abiola-Ellison (`refs/ref-civilian.wav`; the
production lines by `tools/art/voice.py` at the neutral settings through radio filter b, both takes
pinned in the speaker table); a moved to `concept/rejected/`, its clip deleted with its CREDITS.md
row.

## voice-kilo-lead

Round 31 (the Level 09 audition, M5 part C, user decision D11 = a), generated with
`tools/concept/audio/tts_r31.py` the same way as round 30 (seed 31 + 10 × the variant's index + the
line's index), each line through radio filter b on its own (its clicks, −16 LUFS), the two joined
with 1 s of silence. The Kilo Lead (L09, speaker `Kilo Lead`: the CDF officer of the Kilo truck
convoy on the Okonjo Bridge, sixty civilians in his trucks; the radio portrait `radio-generic-cdf`),
neutral (0.5 / 0.5 / 0.7), both of his lines as Level 09's README has them: t=95 "Aegis, Kilo convoy
on the Okonjo Bridge. Sixty civilians in the trucks, and things are coming over the far bank." and
the secondary objective's "Kilo convoy is across. Thank you, Aegis." Measured: a 11.52 s,
−15.8 LUFS; b 14.28 s, −16.1 LUFS (OGG Vorbis q4, 44.1 kHz mono); b speaks slower (the t=95 take
8.84 s against a's 6.72 s). Whisper (faster-whisper base.en) reads both files back whole: a "Aegis,
Kilo Convoy and the Okanjo Bridge. Sixty civilians in the trucks and things are coming over the far
bank. Kilo Convoy is across. Thank you Aegis." ("on" heard as "and", also in the raw take); b "Ages,
Kilo Kondvoi on the Okanjo Bridge, 60 civilians in the trucks and things are coming over the far
bank. Kilo Kondvoi is across, thank you ages." The raw takes (before the filter) read back word for
word except a's "and the" for "on the", b's "ages" for "Aegis" in both lines and "Conboy" in the
second; "Okonjo" is read "Okanjo" in all four (Whisper's spelling of an unknown name). Measured only,
not listened to.

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Aaron Bennett | [On Mamba Station: U.S. Marines in West Africa, 1990–2003, ch. 1 (Antal, Vanden Berghe)](https://archive.org/details/onmambastation_2507_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 129 Hz (takes 131 / 150 Hz); a male voice |
| b | tombooker | [The Colored Regulars in the United States Army, ch. 1 (Steward)](https://archive.org/details/coloredregulars_2605_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 101 Hz (takes 105 / 109 Hz); a deeper male voice, near Hammer Lead's clip (99 Hz) |

Both are military non-fiction read solo, chosen for an officer's measured delivery; a's book is the
story of the Marines' civilian evacuations from West Africa, close to the convoy's job. The readers'
accents are not claimed here, only the pitch (librosa pyin median over the voiced frames). Neither
reader is in the cast or was auditioned before.

Outcome (user, 2026-10-07): **a** cast, Aaron Bennett (`refs/ref-kilo-lead.wav`; the production
lines by `tools/art/voice.py` at the neutral settings through radio filter b, no pins needed); b
moved to `concept/rejected/`, its clip deleted with its CREDITS.md row.

## voice-lifeline

Round 32 (the Level 10 auditions, M5 part D, user decision D12 = a), generated with
`tools/concept/audio/tts_r32.py` the same way as round 31 (seed 32 + 100 × the speaker's index +
10 × the variant's index + the line's index), each line through radio filter b on its own (its
clicks, −16 LUFS), joined with 1 s of silence. Lifeline (L10, speaker `lifeline`: Lifeline One's
pilot, a civilian flying an evacuation shuttle with two hundred and twenty people aboard, also the
voice of Lifeline Two, Four and Five and of the hit line, one voice under several names as the
docks; the radio portrait `radio-generic-civilian`), neutral (0.5 / 0.5 / 0.7), three of its lines
as Level 10's README has them: t=1 "Eko Control, Lifeline One. Five birds, eleven hundred souls.
We're going.", the first shuttle hit with `{ally}` = Two "Lifeline Two, we're hit! Still flying.
Please stay close!" and t=196 "Corridor clear. We can see the sky. Thank you, Aegis. Thank you."
Measured: a 16.66 s, −15.8 LUFS; b 17.18 s, −15.9 LUFS (OGG Vorbis q4, 44.1 kHz mono). Whisper
(faster-whisper base.en) reads both files back whole: a "Echo control, lifeline one, five birds,
eleven hundred souls, we're going. Lifeline two, we're hit, still flying. Please stay close. Court
or clear, we can see the sky. Thank you, Aegis. Thank you."; b "Echo control, lifeline one, five
birds, eleven hundred souls. We're going. Lifeline two, we're hit. Still flying. Please stay close.
Corrid are clear. We can see the sky. Thank you, Aegis. Thank you." "Eko" is written "Echo" (the
same sound); "Corridor clear" is misheard in all four readings, raw and filtered (raw a "Cord or
clear", raw b "Core it or clear"): check by ear that the word is whole. Measured only, not listened
to.

Both candidates are male (Lifeline Three's are female, so the two shuttle pilots never sound
alike, whichever pair is picked), readers of aviation novels, neither in the cast nor auditioned
before.

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Atul Sharma | [The Mysterious Aviator (So Disdained), ch. 1 (Shute)](https://archive.org/details/mysteriousaviator_2510_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 116 Hz (takes 132 / 119 / 119 Hz); a male voice, lower than the Kilo Lead (129 Hz), above Hammer Lead (99 Hz) |
| b | KevinS | [Over the Ocean to Paris, ch. 1 (Dixon)](https://archive.org/details/overtheoceantoparis_2404_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 138 Hz (takes 121 / 228 / 133 Hz); a male voice near the Kilo Lead's pitch; the hit line's take jumps an octave ("we're hit!") |

Outcome (user, 2026-10-08): **b** cast, KevinS (`refs/ref-lifeline.wav`; the production lines by
`tools/art/voice.py` at the neutral settings through radio filter b, five takes pinned: four hit
lines whose key's seed jumped to about 450 Hz, as this audition's did, and the t=196 line for
"Corridor clear"); a moved to `concept/rejected/`, its clip deleted with its CREDITS.md row.

## voice-lifeline-three

Round 32, generated with `tools/concept/audio/tts_r32.py` as `voice-lifeline` above. Lifeline Three
(L10, speaker `lifeline-three`: the pilot of the shuttle the lance takes at t=118; one line; the
radio portrait `radio-generic-civilian`), neutral, its t=116 line "Aegis, there's a light above the
clouds. What is that—", spoken as the game speaks it (the dash read as a comma: "What is that,").
**The cut**: the lance cuts the line off. The raw take is cut hard (3 ms) where Whisper's word
timing ends "that", which falls inside the word (a: in its vowel, 0.14 s before the voice ends; b:
0.1 s before), then 0.3 s of static as loud as the voice's peak (hiss and dense crackle, on at once,
breaking up, held 0.12 s and dying) and 0.25 s of fading hiss; the channel goes dead without the
radio's closing click. Measured: a 4.28 s, b 4.82 s, both −15.9 LUFS. Whisper reads back the files:
a "Ages. There's a light above the clouds. What is that?" ("Aegis" heard as "Ages", in the raw take
too); b "Aegis, there's a light above the clouds. What is that?" Whisper completes the clipped
"that", so the cut is visible only in the envelope: the voice at −17 dBFS (50 ms RMS) runs straight
into the static at −16 to −19 dBFS, which dies to the −44 dBFS hiss. Measured only, not listened to.
In production the cut needs the same treatment in `tools/art/voice.py` (a line ending in a dash).

Both candidates are female and higher than every female voice in the cast (Okafor 152, the
Civilian 149, Varga 193 Hz), neither in the cast nor auditioned before.

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Kehinde | [Travels in West Africa, ch. 1 (Kingsley)](https://archive.org/details/travels_westafrica_0910_librivox), cut at 60 s | public domain (LibriVox) | 235 Hz (take 271 Hz); a female voice |
| b | Maria Kasper | [The Curtiss Aviation Book, ch. 1 (Curtiss)](https://archive.org/details/curtissaviationbook_2006_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 215 Hz (take 272 Hz); a female voice |

Outcome (user, 2026-10-08): **b** cast, Maria Kasper (`refs/ref-lifeline-three.wav`; the production
line by `tools/art/voice.py` at the neutral settings through radio filter b, cut off after "that" as
here: the renderer cuts any radio line ending in a dash, reusing this script's `cut_off()`); a moved
to `concept/rejected/`, its clip deleted with its CREDITS.md row.

## voice-atlas-control

Round 33 (the Level 11 audition, M5 part E, user decision E10 = a), generated with
`tools/concept/audio/tts_r33.py` the same way as round 31 (seed 33 + 10 × the variant's index + the
line's index), each line through radio filter b on its own (its clicks, −16 LUFS), joined with 1 s
of silence. Atlas Control (L11, speaker `Atlas Control`: the CDF officer of convoy Atlas-Seven, three
cargo hulls of reactor parts and the frigate Ruyter; the radio portrait `radio-generic-cdf`), neutral
(0.5 / 0.5 / 0.7), three of its lines as Level 11's README has them, chosen for range: the order
(t=1 "Aegis flight, Atlas-Seven. Three hulls of reactor parts for the Arctic grid. We'd like to
arrive with all three."), a ship-name line (the `ally-hit` line with `{ally}` = Halvorsen: "The
Halvorsen is hit! Taking on water, but holding!") and the tense one (t=62.5 "Sonar has a contact.
Big. Very big. It's gone deep again."). Measured: a 18.74 s, −16.1 LUFS; b 18.62 s, −16.0 LUFS (OGG
Vorbis q4, 44.1 kHz mono); the takes a 7.20 / 3.80 / 4.84 s, b 6.92 / 3.88 / 4.92 s. Whisper
(faster-whisper base.en) reads both files back whole: a "Aegis flight atlas 7. Three holes of
reactor parts for the Arctic grid. We'd like to arrive with all three. The Halvorson is hit, taking
on water but holding. Sonar has a contact. Big, very big, it's gone deep again."; b "Ages flight,
Atlas 7. Three hulls of reactor parts for the Arctic Grid. We'd like to arrive with all three. The
Halborson is hit, taking on water but holding. Sonar has a contact. Big. Very big. It's gone deep
again." The raw takes read "hulls" in a (the filter makes it "holes"), "Halvostin" in a and "Ages"
for "Aegis" in b; the ship's name is Whisper's spelling of an unknown word in all four. No take
jumps an octave inside a word (librosa pyin: the few frames above 300 Hz in a's t=1 and b's hit
line fall in the pauses between sentences, breaths). Measured only, not listened to.

A male and a female candidate, so the round picks between two clearly different officers; both read
naval or wartime non-fiction, chosen for a measured delivery, neither in the cast nor auditioned
before.

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Alister | [Sea-Power in the Pacific, ch. 1 (Bywater)](https://archive.org/details/seapowerpacific_2406_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 106 Hz (takes 133 / 136 / 110 Hz); a male voice, below the Kilo Lead (129 Hz), near Lifeboat Seven and the perimeter beacon (105 Hz) |
| b | MaryAnn | [Eighteen Months in the War Zone: A Record of a Woman's Work, ch. 1 (Finzi)](https://archive.org/details/eighteenmonthsinthewarzone_1409_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 174 Hz (takes 167 / 167 / 165 Hz); a female voice, between Dock (167 Hz) and Varga (193 Hz) |

Outcome (user, 2026-10-09): **b** cast, MaryAnn (`refs/ref-atlas-control.wav`; the production lines
by `tools/art/voice.py` at the neutral settings through radio filter b, ten takes, no pins needed);
a moved to `concept/rejected/`, its clip deleted with its CREDITS.md row.

## voice-nansen

Round 35 (the Level 13 audition, M5 part G, decision G9 = a, pending the user's review), generated with
`tools/concept/audio/tts_r35.py` as round 33 (seed 35 + 10 × the variant's index + the line's index),
each line through radio filter b on its own (−16 LUFS), joined with 1 s of silence. Nansen (L13, the
CDF officer of Nansen Relay; the radio portrait `radio-generic-cdf`), neutral (0.5 / 0.5 / 0.7), three
of its lines from Level 13's README, chosen for range: the arrival (t=36 "Aegis, Nansen. Very glad to
see you. They come at us from every direction out here."), the tense one (the `ally-below` 0.5 event:
"Integrity at half! We can't take much more!") and the release (t=218 "Grid sync complete. Every battery
on Earth just came back online."). Measured: a 15.06 s, −16.0 LUFS; b 17.66 s, −16.0 LUFS; the takes a
5.28 / 3.12 / 3.76 s, b 6.80 / 3.40 / 4.56 s. Whisper (faster-whisper base.en) reads back a "Aegis,
Nansen, very glad to see you. They come at us from every direction out here. Integrity at half. We can't
take much more. Good sync complete. Every battery on Earth just came back online."; b "Ages, Nansen, very
glad to see you, they come at us from every direction out here. Integrity at half, we can't take much
more. Grid sync complete, every battery on earth just came back online." No take jumps an octave
(librosa pyin: a's frames above 300 Hz lie in the leading silence; b's 305 Hz at 2.4 s is the excited
rise on "very glad"). Measured only, not listened to.

A male and a female candidate, both readers of polar-expedition non-fiction (b reads Nansen's own
*Farthest North*), neither in the cast nor auditioned before.

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Patrick Eaton | [In the Arctic Seas, ch. 1 (McClintock)](https://archive.org/details/arctic_seas_pje_1311_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 124 Hz (takes 126 / 141 / 140 Hz); a male voice, near the Kilo Lead (129 Hz) |
| b | Sharon Riskedahl | [Farthest North, Volume 1, ch. 1 part 1 (Nansen)](https://archive.org/details/farthestnorth_vol1_0912_librivox) (file 02; file 01 is the contents), cut at 60 s | public domain | 172 Hz (takes 221 / 188 / 190 Hz); a female voice, near Atlas Control (174 Hz) |

## voice-ares-control

Round 36 (the Ares Control audition, Act 3 "Red Dust", M7 part B; pending the user's review), generated
with `tools/concept/audio/tts_r36.py` as round 35 (seed 36 + 10 × the variant's index + the line's
index), each line through radio filter b on its own (−16 LUFS), joined with 1 s of silence. Ares
Control (L15–17 and L19–21, Ares Landing's flight controller and the colony's one voice; the radio
portrait `radio-generic-colonial`, proposed in this round) at the voice README's proposed base
settings for him, a little drier than neutral (0.4 / 0.45 / 0.7), three of his lines chosen for the
arc from resentment to the thaw: Level 15's t=12 jab ("Aegis flight, Ares Landing. Five weeks we held
alone. Nice of Earth to drop by."), Level 17's t=102 with the dome names ("Inner ring: Schiaparelli,
Burroughs, then Eden. Eden feeds the whole colony.") and Level 21's closing line ("Clear sky over every
dome, Aegis. Dinner's on Ares Landing."). The text as spoken applies the
[proposed respelling list](../README.md#act-3-voices) for the names these lines use (Ares → "Airees",
Schiaparelli → "Skyaparelli", Burroughs → "Burrows"), so the round auditions the respellings too.
Measured: a 21.66 s, −15.7 LUFS; b 25.22 s, −16.0 LUFS; the takes a 7.52 / 6.12 / 5.12 s, b 7.20 /
7.96 / 7.16 s (b's second and third takes hold a pause of about 2 s before "Eden feeds" and before
"Dinner's"). Whisper (faster-whisper base.en, prompted with the real names) reads back a "Aegis
Flight, Ares Landing. Five weeks we held alone, nice of Earth to drop by. Inner Ring, Schiaparelli,
Burroughs, then Eden, Eden feeds the whole colony. Clear Sky over every dome, Aegis, dinners on Ares
Landing."; b the same words. Without the prompt it hears b's "Aegis" as "ages" and "Airees" as
"Aries". No take jumps an octave (librosa pyin: the frames above 250 Hz lie in the leading silence or,
in b's third take, in its pause, below −40 dB; b's second take rises to ≈ 280 Hz on the breath after
"Eden."). Measured only, not listened to.

Two men, as the spec asks (older than Rook, a clip pitch about 100–115 Hz, below Nansen's 124 Hz and
the Kilo Lead's 129 Hz), neither in the cast nor auditioned before: a reads a history of the
loyalist settlers of Upper Canada (the colonial settler-militia register), b Burroughs's Mars.

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | David Wales | [Pioneer Life Among the Loyalists in Upper Canada, ch. 1 (Herrington)](https://archive.org/details/pioneerlifeuppercanada_1407_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 102 Hz (takes 110 / 119 / 97 Hz); near the perimeter beacon and Lifeboat Seven (105 Hz) |
| b | Thomas A | [The Warlord of Mars, ch. 2 (Burroughs)](https://archive.org/details/warlord_mars_tac_librivox) (ch. 1 at 60 s measured 116 Hz), cut at 60 s | public domain (Public Domain Mark 1.0) | 112 Hz (takes 132 / 119 / 146 Hz); the takes come out higher than the clip, near Nansen (124 Hz) |

Closed in round 36 (user, 2026-10-13, S.6 = b): **b** (Thomas A) chosen with the respellings; mind his
mid-line pauses when his lines are rendered (part B). a's audition and its reference clip moved to
`concept/rejected/`.

## voice-lance-control

Round 37 (the Lance Control audition, M7 part A; pending the user's review), generated with
`tools/concept/audio/tts_r37.py` as round 33, the take through radio filter b (−16 LUFS). Lance Control
(the CDF orbital officer who fires the [Orbital Lance](../../../player/specials/README.md), from Level 17;
speaker `Lance Control`, the radio portrait `radio-generic-cdf`; Hammer Lead's Airstrike call is the
model), neutral (0.5 / 0.5 / 0.7), its one line "Lance uplink locked. Firing!". The seeds are pinned:
of six takes per candidate (seeds 100–105) the shortest, most clipped take that Whisper (faster-whisper
base.en) read back right from the raw WAV, a 104 and b 101 ("Lance uplink locked, firing."; other
takes read "Landsup link", "Plants", "Plans are blank", one began with a breath). Measured: a 2.06 s
(take 1.76 s), b 2.62 s (take 2.32 s), both −16.0 LUFS (OGG Vorbis q4, 44.1 kHz mono). Through the
radio filter Whisper hears "Lands up, link locked, firing." (a) and "Plants up, link locked, firing."
(b): the filter's band limit blurs "uplink" for the recogniser; the listener judges it. b's take rises
to ≈ 350 Hz on "Firing!" (a lift, not an octave jump inside a word). Measured only, not listened to.

The voice README asks for clipped and precise, unlike Hammer Lead's (his clip 99 Hz); both candidates
read military or war non-fiction in a brisk, even delivery, neither in the cast nor auditioned before
(also measured and passed over: Norman Elfer, 99 Hz, Hammer Lead's pitch; Clive Catterall, 89 Hz, slow;
Scott Danneker, a wide pitch range and long pauses; Susan Morin, a storybook read).

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Ted Lienhart | [Our Air Force: The Keystone of National Defense, ch. 1 (Mitchell)](https://archive.org/details/ourairforce_2502_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 132 Hz, a narrow range (IQR 16 Hz), 2.7 words/s (take 126 Hz); the clipped staff officer, near the Kilo Lead (129 Hz) |
| b | Celine Major | [Campaigns of the Civil War, ch. 1 (Geer)](https://archive.org/details/campaignsofthecivilwar_2603_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 159 Hz, 3.0 words/s (take 162 Hz); quicker and lighter, the precise operator, near Dock (167 Hz) |

## voice-conamara

Round 51 (the Dr. Ilse Brandt audition, M8 part B, step AUD-B; pending the user's review), generated with
`tools/concept/audio/tts_r51b.py` as round 37, each line through radio filter b on its own, 1 s apart (−15.8 /
−15.9 LUFS, OGG Vorbis q4, 44.1 kHz mono). Dr. Brandt (Conamara Station's director, Level 22; speaker `conamara`,
`uncast: true` until the pick (gone: round 51 cast her), the radio portrait `radio-conamara`), neutral (0.5 / 0.5 / 0.7), two of her lines:
Level 22's t=1 call ("Aegis flight, Conamara Station. Four rigs on the shelf, forty of us in the dome, and the ice will
not hold still.") and her next call after Varga's "Ilse? It's Elena. Hold on, we are coming.", t=100 ("Something went
through under the north dome. There is water in the shaft at Kilo-Seven. It leads down."): Level 22 gives her no line
that answers Varga directly, so the second audition line is the one that follows Varga's. Seeds 100–102 per candidate
and line; every take read back right (Whisper base.en with the names prompt); the pin is the take whose median pitch is
nearest the clip's without a jump: a 100 / 100, b 100 / 102. Through the filter both read back word for word.
Measured: a 17.1 s, b 17.3 s. Measured only, not listened to.

The voice README asks for a woman, dry, tired, precise, older than Varga, a lower and flatter register than Varga's
190–210 Hz (her clip 193 Hz, a wide range) and apart from Okafor (152 Hz). Both candidates read plain non-fiction in a
level, unhurried delivery, neither in the cast nor auditioned before (also measured and passed over: Mil Nicholson,
147 Hz, too near Okafor; Elizabeth Klett, 219 Hz with a wide range, above Varga; Kristin LeMoine, a storybook read).

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Margaret Espaillat | [Eminent Victorians, ch. 1 (Strachey)](https://archive.org/details/eminent_victorians_1204_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 168 Hz, a very narrow range (IQR 22 Hz), 2.4 words/s (takes 165 / 162 Hz); dry and even |
| b | Sue Anderson | [Gallipoli Diary, ch. 1 (Gillam)](https://archive.org/details/gallipoli_diary_sa_librivox), cut at 60 s | public domain | 172 Hz, a narrow range (IQR 29 Hz), 2.4 words/s (takes 173 / 173 Hz); steady and plain, a diary read |

**Her Level 22 lines with candidate b** (step VOX-B, M8 part B; rejected in round 51: Dr. Brandt is a): all five of Dr. Brandt's
lines (t=1, `target-destroyed`, t=100, `secondary-objective`, the core-sample cache's secret) rendered by the production
renderer `tools/art/voice.py` exactly as the game's own takes, but with `ref: ref-conamara-r51-b`, into
`concept/rejected/voice-conamara-r51-b/<key>.ogg` under the keys that ref gives (candidate a's takes are in
`assets/voice/conamara/`, the speaker table's provisional `ref`). Round 51 picked a: nothing changed in `assets/`, and b's five files moved to `concept/rejected/` with the audition. No take needed a retry or a pin; median pitch 170–185 Hz, no jump; every take read back word for word from
the raw take with the name prompt ("collar" heard as "color" through the filter in one).

Closed in round 51 (user, 2026-10-22, V.1 = a): **Chosen: a** (Margaret Espaillat, `refs/ref-conamara-r51-a.wav`). **Rejected: b** (Sue Anderson: `concept/rejected/voice-conamara-r51-b.ogg`, its five line files in `concept/rejected/voice-conamara-r51-b/` and the clip `concept/rejected/ref-conamara-r51-b.wav/.txt`).

## voice-thera-control

Round 52 (the Thera Deep Control audition, M8 part C, step AUD-C; pending the user's review), generated with
`tools/concept/audio/tts_r52c.py` as round 51, each line through radio filter b on its own, 1 s apart (−16.0 /
−16.2 LUFS, OGG Vorbis q4, 44.1 kHz mono). Thera Deep Control (the city's harbour controller, Levels 23 and 27; speaker
`thera-control`, `uncast: true` until the pick), neutral (0.5 / 0.5 / 0.7), his two audition lines: Level 23's t=34
call ("Aegis, Thera Deep Control. We see you on the shaft. The farms are below you, and so is whatever is draining
them.") and the t=70 line ("Those are our farms, Aegis. Forty thousand people eat from those racks."). Seeds 100–102
per candidate and line (b's t=70 also 103–105); the pin is the take Whisper (base.en, the names prompt) read back right
with its median pitch nearest the clip's without a jump: a 100 / 100, b 101 / 105. b's "racks" read back as "wrecks"
in five of his six t=70 takes (his open vowel); 105 is the one heard right. "Thera Deep" read back as written in every
take (no respelling). Through the filter both files read back word for word. Measured: a 14.6 s, b 14.3 s. Measured
only, not listened to.

The voice README asks for a calm man, older than Rook, below or between the cast men (Ares Control 118 Hz on his
trimmed clip, Nansen 123 Hz, the Kilo Lead 129 Hz) by timbre rather than pitch alone, warmer and slower than Ares
Control (3.0 words a second). Both candidates read in a low, unhurried way, neither in the cast nor auditioned before
(also measured and passed over: John Greenman, 103 Hz but quick; Andy Minter, 113 Hz, 2.9 words/s; Tom Weiss, 114 Hz
with octave jumps in the clip; Bob Neufeld and Don W. Jenkins, about 150 Hz; Barry Eads, 142 Hz, a wide range; Phil
Chenevert, 130 Hz and quick; Clive Catterall, too low and creaky for the pitch tracker). Chatterbox lifts both voices:
a's takes sit at 116 / 127 Hz (between Ares Control and the Kilo Lead), b's at 133 / 126 Hz (around Nansen and the Kilo
Lead), so the pair differs from the cast men by timbre and pace more than by pitch.

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Bruce Pirie | [The Mayor of Casterbridge, ch. 1 (Hardy)](https://archive.org/details/mayor_casterbridge_1207_librivox), cut at 120 s | public domain (Public Domain Mark 1.0) | 100 Hz, a narrow range (IQR 19 Hz), 2.6 words/s (takes 116 / 127 Hz); a low, even country narration |
| b | John Van Stan | [Early Greek Philosophy & Other Essays, ch. 1 (Nietzsche)](https://archive.org/details/earlygreekphilosophy_2103_librivox), cut at 60 s | public domain (Public Domain Mark 1.0) | 119 Hz, a narrow range (IQR 26 Hz), 1.7 words/s (takes 133 / 126 Hz); slow and soft, a measured essay read |

**His Level 23 lines with candidate b** (step VOX-C, M8 part C; accepted in round 52 (2026-10-22)): all four of
Thera Deep Control's lines (t=34, t=70, `secondary-objective`, the survey probe's secret) rendered by the production
renderer `tools/art/voice.py` exactly as the game's own takes, but with `ref: ref-thera-control-r52-b`, into
`concept/voice-thera-control-r52-b/<key>.ogg` (in `assets/voice/thera-control/` since the pick) under the keys that ref gives (candidate a's takes are in
`assets/voice/thera-control/`, the speaker table's provisional `ref`). Two takes pinned by b's keys in the speaker
table (t=34: the key's seed read "on the shot" for "shaft"; the secret: an octave jump); median pitch 121–133 Hz; every
file read back word for word through the filter with the Act 4 name prompt ("racks" heard right in both rack lines).
Round 52 picked b: `ref` became `ref-thera-control-r52-b` and these four files moved into `assets/voice/thera-control/`
(a's four to `concept/rejected/voice-thera-control-r52-a/`; step D4); no re-render.

Closed in round 52 (user, 2026-10-22, V.1 = b): **Chosen: b** (John Van Stan, `refs/ref-thera-control-r52-b.wav`).
**Rejected: a** (Bruce Pirie: `concept/rejected/voice-thera-control-r52-a.ogg`, its four line files in
`concept/rejected/voice-thera-control-r52-a/` and the clip `concept/rejected/ref-thera-control-r52-a.wav/.txt`).

## voice-vorne

Round 60 (the Vorne audition, M9 design phase, step AUD-P; pending the user's review), generated with
`tools/concept/audio/tts_r60.py` as round 52, each line through the new enemy-channel filter on its own, 1 s apart
(−16.1 / −15.9 / −16.2 LUFS, OGG Vorbis q4, 44.1 kHz mono). Chairman Silas Vorne (the main villain; Act 5 intro pages
2–3, Level 35, Acts 6–7; speaker `vorne`, `uncast: true` until the pick) at the README's proposed settings (a theatrical
base: exaggeration 0.6, cfg_weight 0.4, temperature 0.7), two lines: the broadcast's opening ("Citizens of the solar
system. My name is Silas Vorne. From this hour the Jovian Ascendancy governs itself, and it does not stand alone.") and
the recruitment ("Lancer. You are wasted on Okafor. Come to Callisto. Name your price."). Seeds 100–102 per candidate
and line (a's and b's first line also 103–105); the pin is the take Whisper (base.en, the names prompt) read back right
with its median pitch nearest the clip's, no octave jump and no pause over 0.8 s: a 103 / 102, b 104 / 100, c 102 /
102. a's first line paused about a second between sentences or jumped an octave in all six takes (his clip rests 1.28
s between sentences too); 103 is pinned with a 1.06 s rest after "Silas Vorne." (judgement call: an orator's rest, not
a stumble). Every name read back as written (no respelling). Through the filter all three read back word for word.
Measured only, not listened to.

**The enemy filter** (`filter: enemy`, the voice README's *Act 5 voices*): radio filter b's band (300 Hz–3.4 kHz),
saturation and its steady hiss with the slow flutter, **no crackle and no clicks**; a steady static bed under the
whole line (band noise 400 Hz–4 kHz, 26 dB under the voice's RMS, swelling ±3 dB at 0.25 Hz); the line opens and closes
with the intercept's static sweep (here round 60's proposals `intercept-open-r60-a` / `intercept-close-r60-a`, the
sfx concept files) instead of the squelch.

The voice README asks for an older man's polished low voice, a chairman, not a soldier: below every cast man (Ares
Control 112 Hz, Thera Deep Control 119, Nansen 124, the Kilo Lead 129: a clip under about 105 Hz), slow, with an
orator's controlled range, not a monotone; never a main-cast reader nor a voice already cast or auditioned. Also
measured and passed over: Paul Adams (The Prince), 110 Hz; Peter Dann (Heart of Darkness), 99 Hz but 2.7 words/s and no
licence on the archive.org item; Beyond Good and Evil's reader, 87 Hz but nearly monotone (IQR 13 Hz); The Use and
Abuse of History's, 105 Hz and monotone; the Philippics', 108 Hz and 2.9 words/s; David Clarke, 111 Hz, 3.1 words/s;
Peter Yearsley, 120 Hz; Martin Geeson, 173 Hz. Chatterbox lifts every voice: a's takes sit at 129–131 Hz (by the Kilo
Lead), b's at 115–123 Hz (around Thera Deep Control and Nansen), c's at 93 Hz, the only one under the cast men; a and
b differ from them by timbre and pace more than by pitch.

| Variant | Reader | Source | Licence | Clip pitch |
|---|---|---|---|---|
| a | Jeffrey Church | [The Genealogy of Morals, ch. 1 (Nietzsche)](https://archive.org/details/genealogy_of_morals_1507_librivox), cut at 90 s | public domain (Public Domain Mark 1.0) | 103 Hz on the clip (100 Hz over 30 s, a wide range: IQR 54 Hz), 2.0 words/s (takes 131 / 129 Hz); a slow, deliberate argument with a rhetorical rise and fall |
| b | Clive Catterall | [The Prince (version 4), ch. 1 (Machiavelli)](https://archive.org/details/theprincever4_1501_librivox), cut at 40 s | public domain (Public Domain Mark 1.0) | 92 Hz (96 Hz over 30 s, IQR 44 Hz), 2.5 words/s (takes 115 / 123 Hz); an older English voice, measured and dry |
| c | Gregg Margarite | [Rastignac the Devil, ch. 2 (Farmer)](https://archive.org/details/rastignac_devil_1003_librivox), cut at 120 s | public domain | 89 Hz (84 Hz over 30 s, IQR 39 Hz), 2.6 words/s (takes 93 / 93 Hz); a deep American bass, warm and theatrical |

