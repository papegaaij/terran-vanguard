---
title: Voice
design: approved
implementation: in-progress
art: final
depends-on: [.., ../../story/characters, ../../ui/hud, ../../ui/options, ../../tech/architecture]
updated: 2026-10-31
---

# Voice

## Summary

The radio lines are spoken. Every line in the level data is rendered offline by the Chatterbox
text-to-speech engine, through the shared radio filter, into OGG files under `assets/`; the game
plays the file with the radio subtitle, which stays on screen. The briefings and the Choir are
voiced by the same pipeline; a line that is only a stage direction (`[the Choir sings]`) plays its
speaker's stage sound instead. The engine and the radio filter were chosen in concept
[round 18](../../concept-rounds/round-18/README.md), the generic speakers' voices are cast in
[round 19](../../concept-rounds/round-19/README.md); the build and CI never run the engine.

## Contents

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [refs](refs/README.md) | Reference clips the voices are cloned from: LibriVox readings, public domain or CC0 | approved | in-progress | proposed |

## Design

### Engine

**Chatterbox** (Resemble AI, model `ResembleAI/chatterbox`, 0.5 B, English), MIT code and
weights; every output carries Resemble's inaudible Perth watermark. It clones a voice zero-shot
from a 5–15 s reference clip and has two emotion controls, `exaggeration` (0.25–2) and
`cfg_weight` (lower is slower and more dramatic), plus `temperature`. It runs at about real time
on the RTX 2070 (3.6 GB VRAM). The round's setup (venv, versions, the model download) is in the
header of `tools/concept/audio/tts_r18.py`; the production renderer reuses it.

### Offline pipeline

1. **Line list.** `vanguard.content.voice.VoiceLines` (shared by the game) lists every `radio`
   cue of every level's `data.yaml` (timed and event cues, a secret's line, the `mission-failed`
   line, the `easy` / `hard` line variants, a special's call such as Hammer Lead's, Okafor's
   low-armour line from the [armour](../../player/armor/README.md) data, Rook's 27 radio barks
   from the [wingmen](../../player/wingmen/README.md#radio-barks) data, source
   `wingmen barks <trigger>`), and every
   page of the levels' and acts' briefings (not the hangar teaser); `./gradlew
   :pipeline:voiceLines` writes it with the keys and settings to
   `voice/lines.json` in the pipeline's build dir for the renderer (`tools/art/voice.py` runs it isolated since M8 part
   I: its own project cache dir and an init script that puts the build under `~/.cache/tv-tts/gradle/`, or
   `$TV_VOICE_BUILD`). A line with `{ally}` expands to one line
   per convoy unit (One–Five in Level 04); a line with `{side}` (M5 part B) expands to two, `left`
   and `right`, and the game shows and plays the one of Rook's side
   ([wingmen](../../player/wingmen/README.md#scripted-lines-about-him-user-decision-d4-of-m5-part-b)). The text as spoken drops the `*` of italics and reads
   a dash as a comma. A
   line that is only a stage direction in square brackets (the Choir's `[the Choir sings]` in
   Act 1) is not spoken and is not in the list: it shows as text and plays its speaker's **stage
   sound** instead (`stage` in the speaker table, a file in `assets/voice/<speaker>/` that no key
   names; the Choir's sung sting, concept [round 29](../../concept-rounds/round-29/README.md)).
2. **Key.** Each line gets a key: a hash (SHA-256, first 12 hex digits) of the speaker's voice
   (slug, reference clip, the settings the line is spoken with, the layering), the text as
   spoken, the `expression`, the `shout` flag and the filter (radio, distorted or dry: briefing
   pages are dry). The file is
   `assets/voice/<speaker-slug>/<hash>.ogg` (e.g. `assets/voice/okafor/3f9a0c1b7e2d.ogg`); the
   game finds it by computing the same key from the cue. A line whose key already has a file is
   **not re-rendered**; changing the text, the expression or a speaker's settings changes the key,
   so only that line is redone. The renderer deletes the files no line uses any more (`--keep`
   only lists them).
3. **Render.** Chatterbox speaks the line with the speaker's reference clip and the settings of
   the expression (below). Each take has a fixed seed (from the key); the generation is capped
   at 1.5× the expected length from the word count (2.6 words/s plus a little per punctuation
   mark, more per sentence end: 0.12 s a comma, 0.4 s a sentence end, an ellipsis counting once),
   and a take that hits the cap or runs past it, or is shorter than half the expected
   length, is retried with the next seed (up to eight); the retries are logged. Long lines (the
   briefing pages) are spoken by sentences, in chunks of up to 28 words, joined with 0.35 s of
   silence; leading and trailing silence is trimmed. A take can be pinned by seed in the speaker
   table (`pins`, by key) when the automatic pick sounds wrong.
4. **Post.** The Choir's layering (below) where it applies, then the radio filter as in
   `radio()` of the round's generator: 0.15 s pad either side, static, 300 Hz high-pass and
   3.4 kHz low-pass (two 12 dB/oct passes each), tanh saturation (drive 2), a 4 ms click in and
   out, mastered to −16 LUFS with a −1.5 dBFS ceiling. The static is **filter b** of round 18
   (user decision): a steady hiss bed (noise ×0.019 with a slow 0.7 Hz / 3.1 Hz flutter of
   ±25 %) and crackle on 0.1 % of the samples at up to 0.11, about 27 dB under the voice; it is
   `radio()`'s default in the round's generator. **Briefing pages** skip the radio filter: the
   same render and mastering (−16 LUFS, −1.5 dBFS), no band-pass, static or clicks, since the
   briefing is a face-to-face talk, not a transmission. A speaker marked `filter: pa` (the
   Level 06 perimeter beacon) gets the **public-address filter** instead of the radio filter
   (`pa()` next to `radio()`: a 250 Hz–4 kHz horn band with resonances at 1.1 and 2.6 kHz, a
   driven horn's saturation, a faint mains hum, slap echoes at 0.13, 0.29 and 0.47 s and a 1.4 s
   hall; no static or clicks), as auditioned in round 23. A radio line whose text ends in a
   **dash** is cut off (Level 10's Lifeline Three, "…What is that—", as auditioned in round 32):
   Whisper (faster-whisper base.en) times the raw take's words, the take is cut hard (3 ms) where
   its last word ends (Whisper's end falls inside the word's tail, so the word is clipped), then
   0.3 s of static as loud as the voice's peak (hiss and dense crackle, breaking up, held 0.12 s
   and dying) and 0.25 s of fading hiss, all through filter b; the file ends there, without the
   closing click: the channel goes dead. OGG Vorbis q4, 44.1 kHz mono, with a
   comment tag naming the tool, the speaker and the key.
5. **Commit.** The rendered OGGs are committed under LFS like the other assets.

The renderer is the production generator `tools/art/voice.py`: it runs Gradle for the line list,
Chatterbox in the TTS venv in `~/.cache/tv-tts/` (outside the repo and the Gradle build) and the
post-process with the system Python.

### Reference voices

One reference clip per speaker, **CC0 or public domain** only, never a celebrity or a known
person: LibriVox chapter readings (a 12 s cut, 24 kHz mono) whose archive.org item carries a
public-domain or CC0 licence. The clips live **in the repo** under [refs](refs/README.md)
(`ref-<speaker>.wav` plus the Whisper transcript the cut was checked against), stored in **Git
LFS**, each recorded in [CREDITS.md](../../../CREDITS.md) with the reader, the book and
chapter, the archive.org URL, the licence and the cut.

- **Main cast** (user decision): round 18's readers stay: Okafor (Ruth Golding, CC0 1.0), Varga
  and the Choir's base (Betsie Bush), Rook (Rick Rodstrom), all public domain.
The cast (round 18 for the main cast, [round 19](../../concept-rounds/round-19/README.md) for the
generic speakers; the speaker table in [data.yaml](data.yaml) maps the speakers' names to these):

| Speaker | Reader | Reference clip |
|---|---|---|
| Okafor | Ruth Golding | `refs/ref-okafor.wav` |
| Varga, the Choir's base | Betsie Bush | `refs/ref-varga.wav` |
| Rook | Rick Rodstrom | `refs/ref-rook.wav` |
| Yard Control | Mark Nelson | `refs/ref-yard-control.wav` |
| Dock One–Four (one voice) | Kristin Hughes | `refs/ref-dock.wav` |
| Ring Control | Cori Samuel | `refs/ref-ring-control.wav` |
| Tranquility Control | Karen Savage | `refs/ref-tranquility-control.wav` |
| Crawler One | Kara Shallenberg | `refs/ref-crawler-one.wav` |
| Convoy | Adrian Praetzellis | `refs/ref-convoy.wav` |
| Hammer Lead | Gord Mackenzie | `refs/ref-hammer-lead.wav` |
| Driver Control | Alex Foster | `refs/ref-driver-control.wav` |
| Perimeter beacon | Mark F. Smith | `refs/ref-perimeter-beacon.wav` |
| Lifeboat Seven | Tadhg Hynes | `refs/ref-lifeboat-seven.wav` |
| Civilian (shelter nine) | Faith Abiola-Ellison | `refs/ref-civilian.wav` |
| Kilo Lead | Aaron Bennett | `refs/ref-kilo-lead.wav` |
| Lifeline (Lifeline One, Two, Four, Five, the hit line) | KevinS | `refs/ref-lifeline.wav` |
| Lifeline Three | Maria Kasper | `refs/ref-lifeline-three.wav` |

- **Generic speakers** get readers from the same sources, never a main-cast reader, auditioned in
  [round 19](../../concept-rounds/round-19/README.md). One voice per role, so a role keeps its
  voice across levels: Act 1 has Yard Control and the four docks (Level 02), Ring Control
  (Level 03), Tranquility Control, Crawler One and Convoy (Level 04) and Hammer Lead (the
  Airstrike special). Every role keeps its own voice (round 19, item 8); the four docks are one
  role.
  Level 05 adds **Driver Control** (the mass driver's operator, three lines), cast in
  [round 21](../../concept-rounds/round-21/README.md) (Alex Foster, a).
  Level 06 adds the **Daedalus perimeter beacon** (automated, one line that also loops faintly
  under section 2 as ambience), cast in [round 23](../../concept-rounds/round-23/README.md) (Mark
  F. Smith, a): flat, even settings (`fixed`: exaggeration 0.3, cfg_weight 0.5, temperature 0.6)
  and the **public-address filter** (`filter: pa` in the speaker table) instead of the radio
  filter; the same rendered line feeds the ambience loop.
  Level 07 adds **Lifeboat Seven** (a drifting CDF lifeboat's crew, one line at t=22), cast in
  [round 25](../../concept-rounds/round-25/README.md) (Tadhg Hynes, a), at the neutral settings
  through radio filter b, as auditioned.
  Level 08 adds the **Ikoyi shelter civilian** (speaker `Civilian`, "shelter nine": a civilian in
  a Nova Lagos shelter calling for help, two lines, the call and the secondary objective's thanks;
  the radio portrait `radio-generic-civilian`), auditioned in concept round 30 (user decision D6 a
  of M5 part B): her two real lines with two candidate readers (a, b) from the same CC0 and
  public-domain sources, never a main-cast reader, through radio filter b at the neutral settings
  (`tools/concept/audio/tts_r30.py`), and cast in that round: Faith Abiola-Ellison (b, a woman's
  voice, clip pitch 151 Hz), at the neutral settings through radio filter b, as auditioned; her
  two lines are voiced (both takes pinned in the speaker table).
  Level 09 adds the **Kilo Lead** (speaker `Kilo Lead`: the CDF officer of the Kilo truck convoy
  on the Okonjo Bridge, two lines, the call at t=95 and the secondary objective's thanks; the radio
  portrait `radio-generic-cdf`), auditioned in concept round 31 (user decision D11 = a of M5 part
  C) as the civilian was: his two real lines with two new candidate readers (a, b) from the same
  CC0 and public-domain sources, never a main-cast reader nor a voice already cast for another role,
  through radio filter b at the neutral settings (`tools/concept/audio/tts_r31.py`), and cast in
  that round: Aaron Bennett (a, clip pitch 129 Hz), at the neutral settings through radio filter
  b, as auditioned; his two lines are voiced.
  Level 10 adds two shuttle pilots (user decision D12 = a of M5 part D), each auditioned a/b in
  concept round 32 as the Kilo Lead was (two new candidate readers from the same CC0 and
  public-domain sources, never a main-cast reader nor a voice already cast, through radio filter b
  at the neutral settings, `tools/concept/audio/tts_r32.py`; the radio portrait
  `radio-generic-civilian`): **Lifeline**, Lifeline One's pilot (the t=1 call and the t=196 thanks),
  who is also the voice of Lifeline Two, Four and Five and of the hit line ("Lifeline {ally}, we're
  hit!…", one take per shuttle by `{ally}`): one voice under several names, as the docks; and
  **Lifeline Three**, the lost shuttle's pilot, one line cut off by the lance ("…What is that—";
  the dash reads as a comma, and the renderer cuts the take off after its last word, see
  *Offline pipeline*). Both were cast in that round (user, 2026-10-08): Lifeline is KevinS (b, clip
  pitch 138 Hz), Lifeline Three Maria Kasper (b, 215 Hz), at the neutral settings through radio
  filter b, as auditioned; their lines are voiced.
  Level 11 adds **Atlas Control** (user decision E10 = a of M5 part E; speaker `Atlas Control`: the
  CDF officer of convoy Atlas-Seven; the radio portrait `radio-generic-cdf`), auditioned a/b in
  concept [round 33](../../concept-rounds/round-33/README.md) as the Kilo Lead and Lifeline were: real lines with two new candidate readers from
  the same CC0 and public-domain sources, never a main-cast reader nor a voice already cast, through
  radio filter b at the neutral settings (`tools/concept/audio/tts_r33.py`), marked `uncast: true`
  in the speaker table until the round picks. The candidates: a, Alister (a male voice, clip pitch
  106 Hz, below the Kilo Lead's 129 Hz so the two CDF officers differ), and b, MaryAnn (a female
  voice, 174 Hz, between Dock's 167 Hz and Varga's 193 Hz). Its lines: three timed calls (t=1, t=62.5, t=110), the
  ship-hit and ship-lost lines once per ship name (`{ally}` becomes a naval convoy unit's name,
  Halvorsen, Mbeki or Saint-Laurent: three takes each) and the secondary objective's thanks, about
  ten takes. Cast in round 33 (2026-10-09): Atlas Control is MaryAnn (b), at the neutral settings
  through radio filter b, as auditioned; her ten takes are voiced.
  Level 13 adds **Nansen** (M5 part G, G9 = a, pending the user's review; speaker `Nansen`: the
  CDF officer of the Nansen Relay; the radio portrait `radio-generic-cdf`), auditioned a/b in
  concept round 35 as Atlas Control was
  (`tools/concept/audio/tts_r35.py`): a, Patrick Eaton (male, clip pitch 124 Hz), b, Sharon Riskedahl
  (female, 172 Hz), marked `uncast: true` until the round picked. Cast 2026-10-12 (the user's pick in
  round 34's review): Nansen is Patrick Eaton (a), at the neutral settings through radio filter b, as
  auditioned (`refs/ref-nansen.wav`); his five lines are voiced, two takes pinned. Level 13 plans about 20 radio lines,
  about six of them Nansen's, the radio events `ally-below` (50 % Nansen, 25 % Okafor),
  `secondary-objective` and `mission-failed` (Okafor), Varga's four intel lines and two briefing pages
  (the teaser is text only). Level 14 (M5 part H) adds no speaker: about 15 radio lines (Okafor,
  Rook three, Hammer Lead, Varga five, the Choir's stage sound), four intel lines and the four outro
  pages of [Act 2](../../campaign/act-2-homefront/README.md#act-intro-and-outro), voiced by Okafor.

### Speakers and expression

A speaker table (a `data.yaml` next to this README) lists each speaker's slug, reference clip,
base `exaggeration`, `cfg_weight`, `temperature`, optional pinned seeds and an optional `stage`
sound (see *The Choir*). The cue's
`expression` adjusts them; round 18's settings are the starting point:

| Expression | exaggeration | cfg_weight | temperature | Round 18 example |
|---|---|---|---|---|
| neutral | 0.5 | 0.5 | 0.7 | Varga, calm |
| grim | 0.45 | 0.5 | 0.7 | Okafor, "That's the yards…" |
| fierce | 0.8 | 0.4 | 0.8 | Rook, joking (the same settings) |
| shout (a cue with `shout: true`) | 1.2 | 0.3 | 0.8 | Tranquility Control's shout |

A speaker's own base values can shift the row (Rook livelier, Okafor drier). `distorted` cues
(a damaged channel) get a stronger filter: more static and dropouts.

**Shouting** is its own field on a radio cue, `shout: true` (user decision), separate from the
queue priority: `urgent` stays a queue priority only (it interrupts, see *Playback*), and a line
can be shouted without interrupting, or interrupt without being shouted. A shouted line uses the
shout row whatever its `expression` (the portrait still follows the `expression`).

### The Choir

The Vrell's Choir is the engine's line in the Varga reference voice (exaggeration 0.3,
cfg_weight 0.3), normalised and layered in post as in `choir()` of the round's generator: copies
at −12, −5, 0 and +7 semitones (same length, gains 0.8 / 0.55 / 0.45 / 0.3), staggered 0–45 ms,
plus a reversed copy at 0.22, over a 55/82 Hz drone with low noise, through a 2.2 s reverb
(35 % wet); then the radio filter. The Choir is **voiced** (user decision), layered as in round
18. Its Act 1 cues are stage directions (`[the Choir sings]`), which are not spoken; its first
spoken words come in Acts 3–4. Such a cue plays the Choir's **stage sound** (`stage` in the
speaker table) on the voice bus with the subtitle, as a voice file plays: a few seconds of the
Choir singing without words, at the spoken lines' loudness (−16 LUFS) through radio filter b.
Concept [round 29](../../concept-rounds/round-29/README.md) offers two kinds, a wordless sting
sung in the Choir's voice (Varga's reference voice with the layering above; a, b) and a
synthesized choir pad like the music's (c, d); option **a** plays until the user's choice. The
file is copied into `assets/voice/choir/` by `./gradlew :pipeline:copyPlaceholderStageSounds`;
the renderer leaves files not named by a key alone.

### Act 3 voices (approved, round 36)

**Status: approved in concept round 36 (user, 2026-10-13, D.30), with every proposal of this section
taken.** Act 3 is voiced as Acts 1–2: every radio line and briefing page, the act intro's four
pages (M7 part B) and the outro's four (part H); the hangar teaser and the intel lines stay text. The
speakers per level are in the [Act 3 README](../../campaign/act-3-red-dust/README.md#voice). Two new
speakers: Ares Control was cast in round 36 (S.6 = b), Lance Control is cast a/b in part A's round as
Atlas Control was (real lines, two new candidate readers from the CC0 and public-domain sources,
never a main-cast reader nor a voice already cast, radio filter b, the neutral settings, `uncast:
true` until the round picks). The lines are rendered in the parts that build their levels.

- **Ares Control** (Levels 15–17 and 19–21). One character (the act README's decision, Q.17d):
  Ares Landing's flight controller, who also speaks for the colony in Level 17's
  siege, so the drafts' *Ares Warden* lines (Level 17's nine, Level 21's closing line) are his. A man,
  older than Rook, proud and tired, dry rather than angry; the resentment of the first line ("*Nice of
  Earth to drop by.*") has to thaw audibly by the dinner lines of Levels 17 and 21, so the reader
  needs a warm low register under the dryness. **Cast (round 36, S.6 = b): reader Thomas A** (LibriVox,
  *The Warlord of Mars*, ch. 2; clip 112 Hz, below Nansen's 124 Hz and the Kilo Lead's 129 Hz, so the
  CDF officers and the colonist differ), at base settings a little drier than neutral (exaggeration
  about 0.4, cfg_weight 0.45). His audition held a **pause of about 2 s inside** its second and third
  lines: the render (`tools/art/voice.py`) retries every take that holds a silence longer than 0.8 s
  inside it with the next seed (logged), and his clip, renamed `refs/ref-ares-control.wav` when
  `uncast` is replaced in the voice data, has its own pauses trimmed (0.6 s and 1.2 s silences cut
  to 0.3 s, 10.3 s; [refs](refs/README.md), part B step AUD-B, pending review in round 38), since
  Chatterbox copies a clip's prosody; David Wales (a) is rejected. His Level 20 call "*Rim guns, open fire!*" is a `shout`. The audition lines: Level
  15's t=12 jab and Level 17's t=102 "*Inner ring: Schiaparelli, Burroughs, then Eden.*" (the names
  below). About 31 cues, **about 39 takes**: Level 17's dome hit and dome lost lines expand `{ally}`
  to the five domes (Bradbury, Lowell, Schiaparelli, Burroughs, Eden), as Atlas Control's ship names
  did. Radio portrait: the new colonial generic `radio-generic-colonial`, variant b "the dust-suit
  controller" (round 36, C.1 = b), on the **teal** friendly channel like the CDF officer (amber stays
  the enemy's).
- **Lance Control** (the [Orbital Lance](../../player/specials/README.md)'s call, "*Lance uplink
  locked. Firing!*", from Level 17; cast in part A's round, where the special is built and flown on
  earlier levels). The CDF orbital officer, one take, the generic CDF portrait; Hammer Lead's
  Airstrike call is the model (a role of its own, round 19's rule). Clipped and precise, a voice
  unlike Hammer Lead's. The name Lance Control is decided (round 36, Q.24g).

**The cast speakers in Act 3.** Okafor (she / her) carries the Helix beat: her cut-off "*Not on this
channel, Doctor.*" in Levels 17 and 19 is the same text, speaker and expression, so it has one key and
plays **one file** twice: the refrain sounds identical on purpose (Level 18's varied line has its
own). Varga's hint-1 line in Level 18 (t=71, "*That's not Vrell. That signature is... ours?*") is
the act's key read: doubt rising into the question, her pitch kept in her 190–210 Hz (earlier takes
of hers jumped an octave) and "ours" heard as "ours", not "hours"; pin it. Rook's quiet beat in Level
20 (t=113) is read at the neutral row, not his lively base, slower, with no joke in it; pin it.
Rook's other Act 3 lines keep his `requires: escort` and his settings.

**The Choir speaks** (Level 21, at the Colossus's death; the
[dust colossus](../../enemies/bosses/dust-colossus/README.md#the-choirs-first-words), M14 = a). Its
first words, "*…many… we are many… we are going…*", are a spoken Choir line, not the stage sound: the
engine in the Choir's base voice with the layering of *The Choir* above. Treatment (proposal, part
H's round; the fragments, the filter and the length are approved, the layering is a/b):

- **Fragments**: a Choir line is split at its ellipses and each fragment is spoken on its own
  ("Many." · "We are many." · "We are going."), joined by 0.7 s of the Choir's drone (the briefing
  pages' sentence join, longer), so the pauses are the same in every render and a fragment can be
  re-rolled or pinned alone. The subtitle shows the line as written.
- **Layering**: the round-18 copies (−12, −5, 0, +7 semitones), but the base voice is Varga's
  reader and Varga speaks next ("*Commander… that was not singing.*"), so the unison copy must not let
  her voice through: a/b in part H's round, a the round-18 gains as they are, b the unison copy down
  from 0.45 to 0.25 with the −5 copy leading. Delivery slower than the sung sting, words still clear
  through the radio filter.
- **Filter**: the drafts' `distorted` expression (more static and dropouts); the dropouts fall in the
  gaps between the fragments, never on a word of its first spoken line (pinned by seed).
- **Length**: at most about 6 s with the gaps, the Level 21 radio model's slot (the Choir from the
  kill to ≈ K+6.0, Varga after it); a longer take moves the level's closing lines.
- The Choir still **sings** (its round-29 stage sound) in Levels 15 and 16 and before Level 21's boss.

**Pronunciation.** Chatterbox reads text only (no phoneme input), so the levers are the spelling it
is given and pinned seeds. Whisper's read-back of the Act 2 takes heard "Ares" as "air is" without
a name prompt, as it hears "Vrell" as "rail" and "Aegis" as "Aides" (accepted in Acts 1–2 by ear): that
may be Whisper not knowing the word rather than the voice saying it wrong, so the names are listened
to before anything is respelled. Decided (round 36, Q.24f and S.6's (a)–(c); built in part B): a **respelling list** in
the speaker table (word → spelling as spoken), applied to the text as spoken of every line (the
subtitle keeps the real spelling) and part of the key, so a changed respelling re-renders exactly
the lines that use the word; the Whisper read-back's name prompt gets the Act 3 names. The
respellings Airees, Skyaparelli and Burrows were heard in Ares Control's audition and accepted (round
36, S.6); the others were tried at the render step (M7 part B, step VOX-B: Whisper's read-back of Okafor and
Ares Control, three seeds each, the plain spelling against the respelling) and kept only where they read better:

| Name | Say | Respelling |
|---|---|---|
| Ares (Landing, Control) | AIR-eez, one word | "Airees" |
| Schiaparelli (a dome) | skyah-pah-REL-ee | "Skyaparelli" |
| Valles Marineris | VAL-iss mar-ih-NAIR-iss | "Vallis Marinairis" (kept: plain read "Vales"/"Valet's") |
| Lycus (Survey Station) | LIE-kus | as written ("Lykus", "Lie-kus" and "Lykuss" read no better: "Lyca"/"Like a" every way) |
| Coprates (Rapids) | KOP-ruh-teez | "Coprateez" (kept: plain read "cop rates") |
| Eos (outflow) | EE-oss | as written (plain read "Eos" as well as "Ee-oss", which once read "E-O-S") |
| Helix (research station) | HEE-lix | as written (Level 16, step VOX-C: read "Helix" with and without the name prompt) |
| Noctis, Candor | NOK-tiss, KAN-dor | not in a voiced line yet (Level 16 names them in text only) |
| Burroughs (a dome) | BUR-ohz | "Burrows" (Level 17, step VOX-D: every take of "Burrows is hit!" that read "Burrows" plainly jumped an octave; the pin reads "Barrows") |
| Bradbury, Lowell (domes) | as usually said | as written (Level 17, step VOX-D: read back as written with the name prompt; one "bribery" and one "Below" take replaced by pins) |
| Olympus Mons, Tharsis, Hellas, Noctis, Candor, Europa, Conamara, Thera | as usually said | as written; respelled only if the read-back fails |

### Act 4 voices (draft, round 48)

**Status: draft, decided in round 48 (2026-10-22)** (M8 design phase, step AUD48). Act 4 is voiced as
Acts 1–3: every radio line and briefing page, the act intro's four pages and the outro's four. The speakers per level,
Rook's scripted Level 26 and his silence in Levels 27–28 are in the
[Act 4 README](../../campaign/act-4-deep-water/README.md#voice); this section holds how they are cast and rendered.

- **Two new colony voices**, cast as Ares Control and Lance Control were (real lines, two new candidate readers each
  from the CC0 and public-domain sources, never a main-cast reader nor a voice already cast, radio filter b, the
  neutral settings, `uncast: true` until the round picks), in an audition step of round 48 if the GPU is free,
  otherwise in M8 part B's round:
  - **Dr. Ilse Brandt** (Conamara Station, Level 22): a woman, dry, tired, precise, older than Varga. Her reader must
    sit clearly apart from Varga's 190–210 Hz and from Okafor's voice, since Level 22 has the two scientists talk
    to each other (Varga's "*Ilse? It's Elena.*"): a lower, flatter register. Audition lines: her first call and her
    answer to Varga. Auditioned in round 51 (step AUD-B, `tools/concept/audio/tts_r51b.py`): a, Margaret Espaillat (Eminent
    Victorians, clip 168 Hz, a very narrow range), b, Sue Anderson (Gallipoli Diary, 172 Hz, narrow), both between
    Okafor (152 Hz) and Varga (193 Hz) and much flatter than Varga (IQR 22 / 29 Hz against her 68), at the neutral
    settings through radio filter b. **Cast in round 51 (2026-10-22): Dr. Brandt is Margaret Espaillat (a)**, `uncast`
    gone, her `ref` stays `ref-conamara-r51-a` (her takes in `assets/voice/conamara/`); b's audition, its five line
    files and its clip `ref-conamara-r51-b` moved to `concept/rejected/` (the clip's CREDITS.md row with them). Her
    lines: the t=1 call and, since Level 22 gives her no direct answer to Varga's t=66 line, her next call (t=104). The
    clip's name is part of a line's key, so the chosen clip keeps its `-r51-` name in `refs/` (renaming it to
    `ref-conamara.wav` would re-key and re-render her five lines, about a minute of work). The credits roll must name
    the reader: `CreditsList` credits a clip only as `refs/ref-<speaker>.wav`, so a kept `-r51-` name needs its
    pattern widened (a pipeline edit) or the rename and the re-render (a later code step).
  - **Thera Deep Control** (Levels 23, 27): the city's harbour controller, a calm man, older than Rook; below or
    between the cast men (Ares Control 112 Hz, Nansen 124 Hz, the Kilo Lead 129 Hz) by timbre rather than pitch alone,
    warmer and slower than Ares Control's dryness. Audition lines: Level 23's t=34 call and the t=70 "*Forty thousand
    people eat from those racks.*"
    Auditioned in round 52 (step AUD-C, `tools/concept/audio/tts_r52c.py`): a, Bruce Pirie (The Mayor of Casterbridge,
    clip 100 Hz, a narrow range, 2.6 words a second: a low, even, unhurried narration), b, John Van Stan (Early Greek
    Philosophy & Other Essays, clip 119 Hz, narrow, 1.7 words a second: Ares Control's pitch but much slower and softer),
    at the neutral settings through radio filter b, `uncast: true` until the round picks. Chatterbox lifts both: a's takes
    sit at 116–127 Hz, between Ares Control (118) and the Kilo Lead (129), b's at 126–133 Hz, around Nansen and the Kilo
    Lead, so both stand apart from the cast men by timbre and pace more than by pitch (a is the lower of the two). His
    lines: the t=34 call and the t=70 "*Forty thousand people eat from those racks.*"
    The speaker `thera-control` (names `Thera Deep Control`, Level 23's radio portrait `thera-deep`) is in the speaker
    table (M8 part C, step C2); step VOX-C rendered his four Level 23 lines with both clips (two pins by b's keys).
    **Cast in round 52 (2026-10-22): Thera Deep Control is John Van Stan (b)**, `ref: ref-thera-control-r52-b` (the
    clip keeps its `-r52-` name, part of the lines' key), b's four takes in `assets/voice/thera-control/`; a's audition,
    its four line files and its clip `ref-thera-control-r52-a` moved to `concept/rejected/` (the clip's CREDITS.md row
    with them).
- **The Choir speaks** twice (Level 26 at t=99, Level 28 as the Maw dies): Level 21's treatment as decided for its
  first words (fragments split at the ellipses and joined by 0.7 s of the Choir's drone, the round-18 layering with
  the part-H pick of a or b, the `distorted` filter with the dropouts between the fragments). Level 26's line is
  quieter and closer, "a reversed-whisper line" as its level document asks: proposal, the same layering with the
  reversed copy raised from 0.22 to 0.4 and the take at exaggeration 0.25, so it reads as a whisper through the
  radio, not a chant; one take each, pinned. Step VOX-F (M8 part F) rendered Level 26's line with Level 21's treatment as
  built (one take in the Choir's fixed settings, the round-18 layering, the `distorted` filter; no fragment joins): the
  whisper variant above needs a renderer change and is not built (accepted in round 55 (2026-10-24)). It still **sings** (its round-29 stage sound) before Level 28's boss.
- **Rook's cut-off** (Level 26, t=161.2 since 2026-10-24, cut by the hits at t=163.5; "*Lancer, something's below u—*"): rendered whole and cut by Whisper's word
  timing inside "us", then a burst of static and silence, as Lifeline Three's cut in round 32 (`tts_r32.py`); his
  t=150 line is read at the neutral row, slower, with no joke (Level 20's quiet beat), pinned. Built in step VOX-F: the
  speaker table's respelling `u: us` makes the voice say "us" (the subtitle keeps "u—"), Whisper times it and
  the take is cut inside it; his t=148 (the README's t=150) line pinned at the neutral row.
- **Pronunciation** (to try at the render step, as Act 3's: Whisper's read-back with the name prompt, three seeds,
  the plain spelling against a respelling; kept only where it reads better):

| Name | Say | Respelling |
|---|---|---|
| Thera Deep | THEER-uh deep | as written (Act 4 intro page 2, step VOX-B: read back "Thera Deep" with and without the name prompt) |
| Conamara (Station, Chaos) | kon-uh-MAR-uh, KAY-oss | as written (Level 22, step VOX-B: read back "Conamara" in every line that names it, with the name prompt, as "Konamara" or "Connemara" without: the sound is right) |
| Brandt (Dr. Ilse) | BRAHNT, ILL-zuh | as written: "Brandt" is in no spoken line of Level 22 (her speaker name only); Varga's "Ilse?" read back "Ilse" with the name prompt in every seed ("Else", "Ilsa" or "It'll say" without one, Whisper not knowing the name), so no respelling |
| Meridian (L24's wrecked survey sub) | muh-RID-ee-un | as written (Level 24's secret, step VOX-D: read back "Meridian" with and without the name prompt) |
| Marlin (L27's hauler) | MAR-lin | as written |
| Pelagic-3 (L28's recon sub) | puh-LAJ-ik three | "Pelajic Three" if the read-back fails |

### Act 5 voices (draft, round 60)

**Status: draft, approved in round 60** (M9 design phase, step `m9-d-audio`, 2026-10-27). Act 5 is
voiced as Acts 1–4: every radio line and briefing page, the act intro's four pages (pages 2–3 are Vorne's broadcast,
Q9 a) and the outro's four; the hangar teaser and the intel lines stay text. The speakers, their channels and their
portraits are in [characters](../../story/characters/README.md#act-5-speakers-draft); the lines are the level
documents' and the [act's](../../campaign/act-5-the-belt/README.md); this section holds how they are cast and
rendered.

**The enemy channel** (Q9 a; its look is the [HUD's](../../ui/hud/README.md#act-5-additions-draft) and the
[briefing's](../../ui/briefing/README.md#intercepted-transmissions-draft)). A speaker on the enemy channel (Vorne,
Harker) gets **`filter: enemy`** in the speaker table, as the beacon has `filter: pa`:

- Radio filter b's band and saturation as every radio voice, plus a **steady static bed** under the whole line: band
  noise (400 Hz–4 kHz) about 26 dB under the voice, swelling slowly (±3 dB at about 0.25 Hz: the rolling bands'
  sound). **No crackle and no dropouts on the words** (the `distorted` expression's dropouts are the Choir's and a
  damaged friendly channel's); the transmission is clean and expensive, the interference is the intercept.
- The line opens and closes with the intercept's static sweep ([sfx](../sfx/README.md#act-5-sounds-draft-round-60))
  instead of the squelch.
- **Briefing pages** are voiced without the radio filter (*Briefings* below), except a page with `channel: enemy`: an
  intercepted page is heard through the feed, so it takes the enemy filter too (Vorne's two broadcast pages).
- Never urgent and never shouted in Act 5: Vorne's character rule ("never shouts until the very end of Act 6").

**New speakers** (cast as Ares Control, Lance Control and the Act 4 colony voices were: real lines, new candidate
readers from LibriVox's public-domain and CC0 recordings, never a main-cast reader nor a voice already cast, through
the speaker's filter at the neutral settings, `uncast: true` until the round picks). **Vorne is auditioned in round
60** (the act's tone hangs on his voice; step AUD-P, `tts_r60.py`, if the GPU is free); the three generic speakers in
their parts' rounds (Ceres Hub Control and Harker in part B's, the Bellwether in part D's).

- **Vorne** (the main villain; Act 5 intro pages 2–3, Level 35; Acts 6–7 later). An older man's **polished low
  voice**: long sentences, rhetorical flourishes, patient, amused, warm on the surface and cold under it; a
  chairman, not a soldier ([Vorne](../../story/characters/vorne/README.md)). The reader must sit **below every cast
  man** (Ares Control 112 Hz, Thera Deep Control 119, Nansen 124, the Kilo Lead 129: a clip under about 105 Hz), slow
  (about 2 words a second or less) and with an orator's controlled range, not a monotone: a dramatic reading (a play,
  a speech or an essay read aloud) rather than a flat narration. **Three candidates (a/b/c)** for the main villain; round 60 chose **a**, Jeffrey Church (about 130 Hz as rendered, above the brief's 105 Hz: the production render lowers it toward the brief, VOX-B trying a pitch shift of up to −3 semitones and keeping a only if it stays natural, else it reports).
  Audition lines: the broadcast's first sentence (intro page 2) and the recruitment ("*Lancer. You are wasted on
  Okafor. Come to Callisto. Name your price.*"). Settings (proposal): a theatrical base, exaggeration about 0.6,
  cfg_weight 0.4 (slower), temperature 0.7; his **rage** (Level 35's core opening, the bared-teeth portrait) is read
  at the **grim** row, short and cold, never the shout row, and a build check rejects `shout: true` on his cues
  before Act 6's end. His long broadcast pages are chunked as Okafor's intro pages, every take checked for a pause
  over 0.8 s (the retry rule) and pinned.
- **Ceres Hub Control** (Levels 29 and 35; friendly channel, teal): the loyalist dock controller who keeps Ceres Hub's
  dock channel open while the hub turns. A **man, tired and steady** (Level 29's draft), dock procedure with a
  belt worker's plainness, not a CDF officer's crispness; his t=60 "*Block C just declared!*" is urgent but not
  shouted. Apart from the cast men by timbre or accent more than pitch (a mid register, 130–150 Hz, sits between the
  Kilo Lead and Rook). Audition lines: Level 29's t=1 call and the t=101 Dock 9 line.
- **Harker**, the mutineers' flight lead (Level 29; enemy channel, amber): a Ceres garrison pilot Rook flew convoys
  with, a man of Rook's age, quick and hard, bitter rather than cruel ("*Pity. I'd have bought you a drink.*"). He
  must not be mistaken for Rook (146–188 Hz) when the two trade lines: lower and harder (115–135 Hz) or a clearly
  different accent. Audition lines: Level 29's t=68 declaration and the t=133 line to Rook. About five takes (two
  timed, three events).
- **The Bellwether**, the tug's captain (Level 31; friendly channel): a **belt miner in her fifties, dry and tired**
  (Level 31's draft), who tells how Helix bought Ceres with medicine. She must not sound like Dr. Brandt (Act 4: dry,
  tired, precise, 168 Hz): rougher and warmer, a working voice, ideally a regional accent; lower than Okafor
  (145–152 Hz) or clearly apart by timbre. Audition lines: Level 31's t=1 call and the t=86/t=93 medicine story. Her
  `{section}` lines expand to the tug's three sections as `{ally}` does (`bow`, `cargo`, `engines`: the hit and the
  lost line, six takes), about 12 takes in all.

**The cast speakers in Act 5.** Okafor reads the act intro's pages 1 and 4 around the broadcast and the "*Bulwark,
RTB*" order at the rescue. Rook is silent until the rescue in Level 29 (his barks stay dropped, as in Levels 27–28);
his first line back, "*Miss me? Don't answer that. Just shoot.*", is read at his lively base and pinned; his t=141
line about Harker's wing is read at the neutral row, slower, with no joke (Level 20's quiet beat). Varga's outro beat
(Vorne "looks forty; he was sixty when I left Helix") is read low and level, not as a revelation.

**The Choir speaks once** (Level 33, before the leech swarm: "*…the one who opened… calls to us… we do not
answer…*", [the Choir](../../story/characters/the-choir/README.md)): Level 26's treatment as built (one take in the
Choir's fixed settings, the round-18 layering, the `distorted` filter), with indifference rather than threat; the
level's music ducks under it. It is silent at Level 35 (the boss is Vorne's).

**Pronunciation** (to try at the render steps, as Acts 3–4's: Whisper's read-back with the Act 5 name prompt, three
seeds, the plain spelling against a respelling; kept only where it reads better):

| Name | Say | Respelling |
|---|---|---|
| Ceres (Hub) | SEER-eez | as written (step VOX-B: read back "Ceres" in every Level 29 line with the name prompt) |
| Vesta (the relay) | VES-tuh | as written |
| Callisto | kuh-LIS-toh | as written |
| Helix | HEE-lix | as written (Act 3, step VOX-C) |
| Vorne (Silas) | VORN, SY-lus | as written (step VOX-B; one seed read "Borne", the pin reads "Vorne") |
| Ascendancy, Jovian | uh-SEN-dun-see, JOH-vee-un | as written |
| Sovereign (the Iron Sovereign) | SOV-rin | as written |
| Harker, Bellwether | HAR-ker, BEL-weh-ther | as written |
| Dacosta (L31's claim marker) | duh-KOS-tuh | as written (step VOX-D2: read back "Dacosta" with the name prompt in every seed; the pin also keeps "old" before it) |
| RTB (Okafor's order at the rescue) | R-T-B, the letters | as written (step VOX-B: read back "RTB" in every take) |

**Rendered (M9 part B, step VOX-B, 2026-10-28; accepted by the user in round 62).** The enemy filter is
`enemy()` in `tools/art/voice.py` (round 60's audition filter without the static sweeps, which the game plays; 5 ms
fades instead of filter b's clicks), for Harker's lines and Vorne's two intercepted pages (`channel: enemy`; the
line list marks them with the filter `enemy`). **Vorne's pitch**: shifting the takes themselves (ffmpeg's rubberband,
formants preserved) at −1.5 / −2 / −3 semitones lowered his median from 114–122 Hz to 104–110 / 102–107 / 98–101 Hz but
moved the spectral centroid down 10 / 15 / 18 % and cut the spectral flatness to a half or a third (formant smearing,
a dulled timbre); shifting his **reference clip** instead keeps both (centroid within 6 %), since Chatterbox re-voices
the lowered clip: −2 semitones gives 100–106 Hz on the test chunk, −3 gave 84–96 Hz but one take of six came out
unvoiced (a whisper) and others creaky. His production clip is therefore `refs/ref-vorne.wav`, round 60's clip
pitched −2 semitones (`tools/concept/audio/tts_r62.py --refs`), the takes unshifted: pages 2–3 at 95 and 119 Hz (round
60's renders 129–131 Hz), with the theatrical base as a `shift` (+0.1 exaggeration, −0.1 cfg_weight). Round 62 hears
the same chunk shifted (a) and unshifted (b) to confirm C.9. **Ceres Hub Control and Harker** are cast with two
LibriVox candidates each (QB-1, round 62): Hub Control a Paul Adams (clip 123 Hz; takes 127–143 Hz, the t=60 shout
194), b Phil Chenevert (clip 141 Hz; takes 142–168 Hz, the shout 217); Harker a Ted Delorme (clip 110 Hz; takes
92–119 Hz), b David Clarke (clip 104 Hz, English; takes 110–131 Hz). Their lines are rendered with both, a in
`assets/voice/<speaker>/` (the speaker table's provisional `ref`), b in `concept/voice-<speaker>-r62-b/` under b's keys,
so the pick needs no re-render. The Act 5 names read back as written (no respelling). Hub Control's t=60 line was a
`shout: true` cue in the level data, read at the shout row, though this section asks for "urgent but not shouted";
**step B4b** (2026-10-28) made it `shout: false` when it trimmed Level 29's radio to one HUD page a
line, and re-rendered the 12 changed or new lines (Okafor 3, Varga 2, Hub Control 4 and Harker 2 with both candidates,
Rook 1), each read back with Whisper (plain and with the Act 5 name prompt) and pinned where the key's seed misread:
"Ceres is Vorne's now" read "borns" in most seeds, so Harker's line became "We're Ascendancy now"; "Tell Earth not all
of Ceres said yes" broke after "not" in every seed ("that" added); "their guns are live" read "guns alive" in every
seed ("hot"); candidate b's Hub Control reads "holds" as "holes" in every seed of the t=156 line.
**Cast in round 62 (2026-10-28): Ceres Hub Control is Paul Adams (a), Harker is David Clarke (b)**, Vorne's pitch is a. The speaker table's `ref` is `ref-ceres-control-r62-a` and `ref-harker-r62-b` (the clips keep their `-r62-` names, part of the lines' keys); Harker's four b takes moved from `concept/voice-harker-r62-b/` into `assets/voice/harker/` and his a takes (Ted Delorme) went to `concept/rejected/voice-harker-r62-a/`; Hub Control's seven b takes (Phil Chenevert) are in `concept/rejected/voice-ceres-control-r62-b/`. The unpicked clips `ref-harker-r62-a` and `ref-ceres-control-r62-b` moved to `concept/rejected/` with their CREDITS.md rows.

**Rendered: Level 31 and the Bellwether (M9 part D, steps VOX-D and VOX-D2, 2026-10-28; cast in round 64: b, Laurie Anne Walden).** Level 31's 26 lines (its two briefing pages; Okafor's, Varga's and Rook's radio; the Bellwether's 12, the six `{section}` takes for bow, cargo and engines among them, and the Dacosta's claim secret) are in `assets/voice/`, every non-Bellwether line read back word for word with the Act 5 name prompt and none pinned. **The Bellwether** is cast with two LibriVox candidates as Ceres Hub Control and Harker were: a **Lucy Burgoyne** (*Sisters*, an Australian novel; clip 134 Hz; takes 135–156 Hz: low, level and older, under Okafor), b **Laurie Anne Walden** (*A Diary from Dixie*; clip 181 Hz; takes 167–264 Hz: a slow, plain American read, apart from Okafor by accent and pace rather than pitch). Her lines are rendered with both, a in `assets/voice/bellwether/` (the speaker table's provisional `ref`), b in `concept/voice-bellwether-r64-b/` under b's keys, so the pick needed no re-render (round 64 picked b: see *Cast in round 64* below); seven of a's takes and four of b's are pinned where the key's seed misread or jumped an octave. Dacosta reads as written: "old" was lost in the first five seeds of a's secret line ("That's how/all Dacosta's"), and the first clean one of 12 more seeds reads it word for word with the name prompt, so the respelling "Da Costa" was not needed. Left as weak spots for the round: a's short exclamations are breathy (the engines hit line about 30 % voiced, every seed tried alike), b reads "bow" as "bell" in most seeds (the pins read "bow"), and b's audition reads "phone loss" for "bone loss" once.

**Cast in round 64 (2026-10-28): the Bellwether is Laurie Anne Walden (b)**: the speaker table's `ref` is `ref-bellwether-r64-b` (the clip keeps its `-r64-` name, part of the lines' key); her 12 takes (b's keys, with b's four pins) moved from `concept/voice-bellwether-r64-b/` into `assets/voice/bellwether/`, a's takes, clip and audition into `concept/rejected/` (rows in its README, CREDITS.md path updated); `tools/art/voice.py --keep --list` reports 0 to render, 0 unused. The in-game credits roll names Laurie Anne Walden and no longer Lucy Burgoyne's Bellwether clip.

### Act 6 voices (draft)

**Status: draft, decided 2026-10-31 (round 72)** (M10 design phase, step `m10-d-audio`, 2026-10-31). Act 6 is
voiced as Acts 1–5: every radio line and briefing page, the act intro's four pages (page 3 is Vorne's intercepted
invitation, Q11 a) and the outro's; the hangar teaser and the intel lines stay text. The speakers, their channels and
their portraits are the [characters](../../story/characters/README.md#act-6-speakers-draft)' Act 6 table (step D-story); the lines are the
level documents', the [act's](../../campaign/act-6-jovian-storm/README.md) and the bosses' (the
[Honour Guard](../../enemies/bosses/honour-guard/README.md#lines)'s fight lines,
[the Ascendant](../../enemies/bosses/ascendant/README.md#vorne)'s order of Vorne's lines); this section holds how they
are cast and rendered. The enemy channel is Act 5's as built (`filter: enemy`, the intercept's sweep, the steady
static bed, no crackle or dropouts on the words).

**New speakers** (J4; cast as Act 5's generic speakers: real lines, **two candidate readers each** from LibriVox's
public-domain and CC0 recordings, never a main-cast reader nor a voice already cast, through the speaker's filter at
the neutral settings, `uncast: true` until the round picks; auditioned in round 72, step AUD-R, `voice_r72.py`, if the
GPU is free, else in their parts' rounds as Act 5's were):

- **Harker** (Level 40, the Honour Guard's leader; enemy channel, amber): **the cast voice**, David Clarke (round 62,
  b): the mutineers' flight lead of Level 29, promoted into Vorne's guard (J4), so Rook's rivalry has a voice the
  player has heard. No audition. Harder and colder than at Ceres, quick, never shouted (the fierce row at most).
- **Vesper** (Level 40, the dancer; enemy channel; a woman in her late twenties, an Aurelia-born cloud racer: D-story's):
  light, amused and quick,
  playful cruelty ("*Leave some for me, Harker. The quiet one's mine.*"). On the enemy channel she must never be
  taken for the Choir, whose base is Varga's reader (Betsie Bush), nor for a friendly woman (Okafor 145–152 Hz, the
  Bellwether's slow American read): a lower, smoky register (about 160–190 Hz) or a clearly different accent.
  Audition lines: her entry taunt and her survivor line ("*You'll pay for that one.*"). **Cast in round 72 (2026-10-31, V.1 b): Vesper is Jenny Bradshaw** (clip 172 Hz, takes 167 and 176 Hz; the steadier read), `ref-vesper-r72-b`; Kristen McQuillin's clip a moved to `concept/rejected/`.
- **Kestrel** (Level 40, the feinter; enemy channel; a man in his twenties, a Helix test pilot: D-story's): quick,
  cocky and cold ("*Check your six, Lancer. I'm already there.*"). Apart from Harker (110–131 Hz,
  English) and Rook (146–188 Hz), who trade lines in the same fight: a mid register (about 130–150 Hz) with a clearly
  different accent. Audition lines: his entry taunt and "*No more games.*" The aces' takes: each ace's entry, its
  survivor line and the last ace's line ("*Just us now. Come on, then.*", one take per ace, since any of the three
  can be last): about nine takes, three of them Harker's. **Cast in round 72 (2026-10-31, V.2 a): Kestrel is Expatriate** (even, mid register, takes 152 and 148 Hz), `ref-kestrel-r72-a`; Bob Neufeld's clip b moved to `concept/rejected/`.
- **Aurelia Traffic** (Level 37; friendly channel, teal; the act's friendly generic voice, J4): Aurelia's civil
  traffic controller, who broke with Vorne to get the civilians out. **A woman in her thirties, born on the city** (D-story's; a girl during the 2166 riots), polished
  and cultured (the gilded capital's voice: careful diction, a little formal), **steady at first and frayed by the
  end**, never shouted: her lost-shuttle line and t=128's "*Aegis, please!*" at the fierce row with `shout: false`
  (urgent, not shouted: Ceres Hub Control's lesson, step B4b), her t=150 sign-off ("*Aurelia Traffic, signing
  off.*") at the grim row, slow. Apart from every cast woman (Okafor, Varga, the Bellwether, Dr. Brandt, the
  controllers of Acts 1–4) by timbre or accent: a mid-low register (about 150–175 Hz) or a British read. Seven lines
  (the t=1 call, t=42, t=66, the lost shuttle, t=128, the secondary's thanks, t=150). Audition lines: the t=1 call and
  the t=150 sign-off. **Halcyon One–Six**, the shuttles, have no voice of their own: Aurelia Traffic calls them (the
  level's names, D-story's to confirm).
  **Cast in round 72 (2026-10-31, V.3 b, not the recommendation): Aurelia Traffic is Lizzie Driver** (a higher British read, takes 204 and 210 Hz), `ref-aurelia-traffic-r72-b`; Sibella Denton's clip a moved to `concept/rejected/`. Her sign-off says "liff" for "lift": the production render **re-renders that line until "lift" is clear** (a follow-up voice step; a pitch-lowered re-render toward the 150–175 Hz brief may be tried at the same time, kept only if natural).
- **The carrier** (J10): Okafor herself, from the bridge of the *Steadfast* (D-story's name); Varga speaks from it too; no new voice and no new filter (radio filter b).

**Vorne in Act 6** (the enemy channel for every line; his production clip `refs/ref-vorne.wav`, round 60's pitched
−2 semitones, with the theatrical base as a `shift`): the intro's page 3 (an intercepted briefing page, `channel:
enemy`, courteous, at his theatrical base), Level 37's t=110 line (cold, the **grim** row: his reaction to the turn
after the Choir's, J6), Level 39's t=91 line (neutral, slow), and Level 42's lines 1–3 and 6 (neutral or grim; line 6
"cold again", his composure back). **Line 5 is his only shout** (J6): "*No! We had an agreement!*" at the eruption,
right after the Choir: `shout: true`, so the **shout row** (exaggeration 1.2, cfg_weight 0.3, temperature 0.8) on his
theatrical base, the portrait **rage**. Rendered as one take (the break after "*No!*" under the retry rule's 0.8 s),
several seeds, the best pinned; checked for the clip's pitch shift surviving the shout (a shout lifts the pitch: it
must stay apart from Harker). **Round 72's verdict (V.5 recast, 2026-10-31)**: neither audition take is kept. **a** the shout row (265 Hz) is a real shout but no longer Vorne (his register is 90–106 Hz), **b** the fierce row (exaggeration 0.8, cfg_weight 0.4, 156 Hz) is Vorne's register but not a shout; both rows stay `proposed` ("recast"). A follow-up voice step re-renders the line as the row's recast option says: **the shout row pitched down toward 100–120 Hz with the break after "*No!*" kept, or the fierce row pushed harder**; the best natural take is pinned. Under it the music is track 18's intro (the Choir motif over a drone) with the radio's duck.

**Round 73's pick (R.4 b, 2026-10-31)**: the shout is [`concept/vorne-shout-r73-b.ogg`](concept/vorne-shout-r73-b.ogg)'s treatment, not a plain pipeline take: a shouted render from his clip lowered a further 4 semitones (`ref-vorne.wav` −4, formants preserved) at the shout row (1.3 / 0.2 / 0.8), seed 105, then lowered by WORLD resynthesis (×0.52) to about 104 Hz, through the enemy filter (`tools/concept/audio/voice_r73.py`). Level 42's line 5 is **produced from that take** (the same clip, row, seed and lowering) when part H builds the level; nothing is rendered for it before (M10 part B, step VOX-B: Level 36 does not play it).

**The check's Level 42 exception.** The voice build check that rejects `shout: true` on Vorne's cues "before Act 6's
end" (planned in Act 5, *Act 5 voices*) allows **exactly one** shouted Vorne cue: **Level 42's line 5**, the eruption
line after the Choir's (the Ascendant's line order). Any other Vorne cue with `shout: true`, in any level, fails the
check with its level and trigger; Act 7's lines are the next act's to decide.

**The Choir in Act 6** speaks twice, both on Level 26's treatment as built (one take in the Choir's fixed settings, the
round-18 layering, the `distorted` filter, a 2 s static lead, the music ducked −6 dB): Level 37 at the turn
("*…the one who opened… is no longer needed… the door is ours…*", dismissal rather than threat) and Level 42's line 4
at the eruption ("*…the one who opened is no longer needed…*", nearly the same words: what it told Aurelia it now tells
Vorne). It is silent in Levels 38–41 (Level 41's Silence is named by Varga from the files, never by the Choir).

**The cast speakers in Act 6.** Okafor commands from the carrier (J10). Her two Aurelia lines are read at the **grim**
row, slower: Level 37's t=161 memory ("*I stood on those decks in sixty-six…*", a memory, not an order) and the
intro's page 4 ("*I was at Aurelia. I remember. So will you.*", on every channel, so no intercept). Varga's files
lines in Level 41 (the Silence's first naming, Q12) are read low and level, not as a revelation (Act 5's outro rule),
a short pause before "*the Silence*" in the text, not in the render settings. Rook keeps one pulp line a level at his
lively base; his rivalry lines with Harker in Level 40 at the fierce row, never shouted and never over a tell.

**Pronunciation** (to try at the render steps, as Act 5's: Whisper's read-back with the Act 6 name prompt, three
seeds, the plain spelling against a respelling; kept only where it reads better):

| Name | Say | Respelling |
|---|---|---|
| Aurelia (Aurelia Traffic) | aw-REE-lee-uh | as written (step VOX-B: read back with the name prompt in every Level 36 and intro line; "or really" without it) |
| Io | EYE-oh | as written (step VOX-B: "Eye-oh" read worse; through the radio filter one take still reads "eel") |
| Callisto | kuh-LIS-toh | as written (Act 5) |
| Ascendant (the ship) | uh-SEN-dunt | as written; it must not read "Ascendancy" |
| Jupiter, Jovian | JOO-pih-ter, JOH-vee-un | as written |
| Halcyon (One–Six) | HAL-see-un | as written |
| Harker, Vesper, Kestrel | HAR-ker, VES-per, KES-trul | as written (Harker cast, Act 5) |
| Chimera, Harrow | ky-MEER-uh, HAR-oh | as written; "Ky-meer-a" if it reads "shimmer" |
| Tvashtar, Loki, Pele, Prometheus (Io's works and plains) | tuh-VAHSH-tar, LOH-kee, PAY-lay, pruh-MEE-thee-us | as written; "Pay-lay" if Pele reads "peel" |
| helium-3 | HEE-lee-um three | as written (step VOX-B: "Helium-3" and "helium-3" read back "helium three"; the t=67 line pinned for its length, not its reading) |
| the Silence | as written | as written (Level 41) |
| Aegis | EE-jis | as written (Act 4) |

**Rendered: Level 36 and the Act 6 intro (M10 part B, step VOX-B, 2026-10-31).** 22 files with the cast voices: the intro's four pages (Okafor's 1, 2 and 4 dry; Vorne's page 3 through the enemy filter, `channel: enemy`, at his theatrical base: 105 Hz on the raw take, no pause over 0.8 s), Level 36's two briefing pages (Okafor, Varga), its 15 radio lines (Okafor, Varga, Rook's three: the scripted t=11 line, the t=118 homing bark and the new `first-strike-kill` line) and the pathfinder pod's secret (Varga). Every take read back by Whisper (faster-whisper base.en) plain and with the Act 6 name prompt (Jupiter, Aurelia, Chimera, harvesters, Stormhawk, Vorne, Ascendancy, Callisto, Io, Steadfast, helium-3 …). The four intro pages are pinned (pages 1–3 on their keys' seeds as checked); four more pins where the key's seed misread or failed: the intro's page 4 ("you die first"), Okafor's t=67 helium-3 line (all eight of the key's seeds ran to the length cap or held a 1.0–1.3 s pause), her secondary line ("Io" as "eel") and Varga's t=128 ("lightning" as "whitening"). **No respelling**: "Helium-3" is spoken "helium three" as written; "Eye-oh" for Io ran long and read worse ("it I"). Weak spots left: through the radio filter Whisper still hears "Io" as "eel" in Okafor's secondary line (the raw take and a trial's filtered render read "Io"), "Vrell" as "rel" in Varga's briefing page 2 ("with Vrell tissue": every seed tried, a merge with "with") and "Vrell tissue" as one word at t=30; homophones only otherwise ("wrecks" as "Rex", "defences" spelt US, "sixty-six" as "66"). The act data reads the intro's page 4 at the **fierce** row (step B2), where *The cast speakers in Act 6* above asks for grim and slower: rendered as the data says, pending the user's ear.

### Briefings

The briefing pages (a level's and an act's) are **voiced** (user decision) by the same pipeline
and the same speaker table, without the radio filter (see *Post* above). The voice starts with
the page; the page's text shows in full as today, and the player can skip to the next page,
which stops the voice. The briefing music ducks while a page speaks, as the level music does for
the radio.

### Playback

- The voice file starts with the radio message, together with the portrait's static and the
  subtitle's typing; the subtitle stays (and can be read at its own speed). A subtitle page is
  **held until its voice line ends** (user decision): its 3 s / 5 s timing is a minimum, and the
  next page or message waits for the voice.
- The voice plays on its own **voice** bus (the voice slider times the master volume); the music
  ducks by the existing 4 dB while it plays (the duck now follows the voice, not only the shown
  message).
- An **URGENT** line interrupts: the playing voice is cut (a short fade, ~20 ms, with the squelch
  click), the urgent line plays, and the interrupted line replays from its start, as the
  subtitle already does.
- Pausing the game pauses the voice; leaving the level stops it.
- **Voice volume**: a **separate slider** (user decision) in Options' Audio tab next to the
  music, effects and radio volumes, times the master volume; the radio-blip (radio bus) volume
  stays as it is. It applies to the radio voice and the briefings.
- A missing voice file is not an error: the line shows as text with the blips, as today.
- A stage-direction line (`[the Choir sings]`) plays its speaker's stage sound as its voice file:
  the subtitle is held until it ends and the music ducks under it.
- **Level end (outro mode)**: when the level ends the radio switches to an outro mode. Waiting
  messages and new event or phase lines are dropped. A voiced line that is not a closing line (a
  Rook bark, a timed line, a call's reply) closes as soon as its voice ends, not after its text
  time. The debrief still takes over 15 s after the level's end but waits while a closing line has
  not been heard in full (longest overrun 2.1 s, Level 15). Backdrops hold 3 s past the outro's
  end (not Levels 01 and 02). A voice is no longer clipped one frame early. The all-levels test
  `RadioTimeline.everyRadioLineEndsBeforeTheLevelDoes` models the barks and the outro (overrun at
  most 3 s).

### Build and CI

The game only reads the rendered OGGs in `assets/voice/`; the Gradle build and CI never run TTS
and need no GPU, Python or model. A `content` test checks that every radio cue has its voice file
(by key), so a changed line without a render fails `./gradlew check` with the list of missing
lines; until every level is rendered the check lists them without failing (switched on per
level).

## Implementation

- [x] Reference clips per speaker in `design/audio/voice/refs/` under LFS, each in CREDITS.md
      (Act 1's: the main cast, the generic speakers of round 19, Driver Control, the perimeter
      beacon and Lifeboat Seven)
- [x] Speaker table (`data.yaml`) with clips, base settings and the expression offsets
- [x] Line list and key (hash of speaker settings, text, expression, `shout`, filter; `{ally}`
      and difficulty variants expanded; briefing pages included; bracketed stage directions
      skipped) shared by the renderer and the game
- [x] Radio cue field `shout` in the data model and the
      [data-file schemas](../../tech/architecture/schemas-voice/README.md), separate from
      the queue priority; the shouted Act 1 lines marked
- [x] Offline renderer: Chatterbox in its venv, several takes, length check, the Choir's layering,
      the radio filter (filter b; none for briefing pages), OGG under
      `assets/voice/<speaker>/<hash>.ogg`; skips lines already rendered; deletes unused files
- [x] Act 1 lines rendered and committed; reviewed in a concept round (all 151 of the line list
      in `assets/voice/`: Level 07's 18 lines and the Act 1 outro's four pages accepted in round 25;
      the act briefing, Levels 01–06's briefing pages and radio lines and Hammer Lead's Airstrike
      call, 128 lines, and the low-armour line accepted as rendered in round 26)
- [x] Rook's 27 radio barks (M5 part A, source `wingmen barks <trigger>`) rendered, four takes
      pinned, and accepted as rendered in round 28
- [x] Playback with the radio message: voice on its own voice bus, music ducking while it plays,
      URGENT cuts and replays, a subtitle page held until its voice ends, pause and stop,
      text-only fallback; the mission failed screen speaks the level's line (the cut has no
      20 ms fade yet: a libGDX sound stops at once)
- [x] Briefing pages voiced: the voice starts with the page, stops on skip, ducks the music
- [x] Separate voice volume slider in Options' Audio tab, stored in the settings file
- [x] `content` test: every radio cue has its voice file; CI runs no TTS
- [x] [Options](../../ui/options/README.md) and [HUD](../../ui/hud/README.md) documents updated
      for the voice
- [x] `{side}` in a line: two lines (`left`, `right`) in the line list, the game playing the one of
      Rook's side (M5 part B; `VoiceLines.SIDE`, `RadioSchedule.line`)
- [x] The Ikoyi shelter civilian: reference clips for the round-30 audition under `refs/` and in
      CREDITS.md, the chosen reader in the speaker table (M5 part B; b, Faith Abiola-Ellison,
      `refs/ref-civilian.wav`)
- [x] Level 08's lines (radio, the `{side}` takes, the secret, the briefing pages) and the Act 2
      act briefing rendered and reviewed in concept round 30; the civilian's after her casting
      (M5 part B; rendered 2026-10-06, 19 lines, three takes pinned; the civilian's two lines
      rendered 2026-10-07, both takes pinned; accepted as rendered when round 30 closed)
- [x] The Kilo Lead (Level 09): reference clips for the round-31 audition under `refs/` and in
      CREDITS.md, the chosen reader in the speaker table (M5 part C, D11 = a; a, Aaron Bennett,
      `refs/ref-kilo-lead.wav`)
- [x] Level 09's lines (radio, the briefing pages, the cocoon's line, the mission failed screen's
      `{group}` line once per node) rendered and reviewed in concept round 31; the Kilo Lead's
      after his casting (M5 part C; rendered 2026-10-07, 20 lines, four takes pinned; the Kilo
      Lead's two lines rendered 2026-10-07, no pins needed; accepted as rendered when round 31
      closed its voice rows)
- [x] Lifeline and Lifeline Three (Level 10): reference clips for the round-32 audition under
      `refs/` and in CREDITS.md, the chosen readers in the speaker table (M5 part D, D12 = a; b,
      KevinS, `refs/ref-lifeline.wav`; b, Maria Kasper, `refs/ref-lifeline-three.wav`)
- [x] A line ending in a dash is cut off after its last word into static and a dead channel
      (`tools/art/voice.py`, Lifeline Three's t=116 line)
- [x] Level 10's lines (radio, the briefing pages, the ferry's line, the mission failed screen's
      line, the four level-end lines with the numbers spoken) rendered and reviewed in concept
      round 32; the Lifeline speakers' after their casting (M5 part D; the cast speakers' rendered
      2026-10-08, 22 lines, four takes pinned; the Lifeline speakers' eight lines rendered
      2026-10-08, five takes pinned)
- [x] Atlas Control (Level 11): reference clips for the round-33 audition under `refs/` and in
      CREDITS.md, the chosen reader in the speaker table (M5 part E, E10 = a; steps E1a and E4; b,
      MaryAnn, `refs/ref-atlas-control.wav`)
- [x] `{ally}` as a naval convoy unit's name in the line list (one take per cargo ship name;
      `VoiceLines.radio`, `NavalConvoyLinesTest`; the escort frigate takes no lane, is never hit or
      lost and has none) (M5 part E, steps E2b and E4)
- [x] Level 11's lines (radio, the briefing pages, the pod's line, the level-end lines) rendered and
      reviewed in concept [round 33](../../concept-rounds/round-33/README.md); Atlas Control's after the casting (M5 part E, step E4; the cast
      speakers' rendered 2026-10-09, 15 lines, three takes pinned, accepted as rendered when round
      33 closed; Atlas Control's ten takes rendered 2026-10-09 at the close, no pins needed)
- [x] Level 12's lines (radio, the briefing pages, the canister's line, the level-end and
      secondary lines; Rook's `first-latch` line) rendered with the cast speakers (Okafor, Varga,
      Rook; the Choir's stage sound), no new speaker and no audition; RadioTimelineTest covering
      Level 12 with and without Rook (M5 part F, step 6; rendered 2026-10-11, 18 lines, six takes
      pinned; pending the user's review in the combined round)
- [x] Nansen (Level 13): reference clips for the round-35 audition under `refs/` and in CREDITS.md,
      the chosen reader in the speaker table (M5 part G, step G2; cast as a, Patrick Eaton, and his five
      lines rendered in step K1, 2026-10-12: two takes pinned, `RadioTimelineTest` passing unchanged)
- [x] Level 13's lines of the cast speakers (radio with the events `ally-below` 25 % and
      `mission-failed`, the briefing pages, the hut's line, the level-end line) rendered (Okafor, Varga,
      Rook; the Choir's stage sound); Nansen's lines played as text until round 35 cast him
      (VoiceFilesTest's `AUDITIONING`, empty since step K1); RadioTimelineTest covering Level 13 voiced (M5 part G, step G6;
      rendered 2026-10-12, 15 lines, three takes pinned; pending the user's review in the combined round)
- [x] Level 14's lines (radio, the briefing pages, the armoured car's line; the intel lines and the
      teaser are text) and the four Act 2 outro pages rendered with the cast speakers (Okafor, Varga,
      Rook, Hammer Lead; the Choir's stage sound); RadioTimelineTest covering Level 14 voiced, with
      and without Rook (M5 part H, step H6; rendered 2026-10-12, 20 lines, three takes pinned; pending
      the user's review in the combined round)
- [x] Level 15's lines (radio with Rook's `{side}` takes, the briefing pages, the drop pod's line), the
      Act 3 intro's four pages and the Act 2 outro's page 3 (re-keyed by "Airees") rendered with the cast
      speakers (Okafor, Varga, Rook, Ares Control; the Choir's stage sound); four takes pinned; Ares
      Control's takes free of mid-line pauses (voice.py's `MAX_PAUSE` retry: three lines retried);
      `VoiceFilesTest` covers Level 15 and the Act 3 briefing (M7 part B, step VOX-B, 2026-10-14; accepted
      in round 38)
- [x] Level 16's lines (radio with Rook's `{side}` takes, the first wall touch, the relay hut's and the
      secondary's lines, the level end, the briefing pages) rendered with the cast speakers (Okafor, Varga,
      Rook, Ares Control; the Choir's stage sound); four takes pinned; `VoiceFilesTest` and
      `RadioTimelineTest` cover Level 16 (M7 part C, step VOX-C, 2026-10-18; accepted in round 39, Varga's
      t=47 line re-rendered for R.4)
- [x] Level 17's lines (15 timed with Rook's three, the `ally-hit` and `ally-lost` lines once per dome by its
      name: Bradbury, Lowell, Schiaparelli, Burroughs, Eden; the seed vault's, the secondary's and the level
      end's lines; the two briefing pages: 30 files) rendered with the cast speakers (Okafor, Varga, Rook, Ares
      Control); 17 takes pinned; `VoiceFilesTest` covers Level 17 (M7 part D, step VOX-D, 2026-10-18; pending
      the user's review in round 40). Open: `VoiceLines.radio` and the game's `{ally}` name must read a ground
      ally's name (the dome lines are keyed by it), and `RadioTimelineTest` gets Level 17's runs once the level
      is mapped (step D4)
- [x] Level 18's lines (14 timed with Rook's four, `requires: escort`; the rover wreck's secret, the secondary's
      and the level end's lines; the two briefing pages: 19 files) rendered with the cast speakers (Okafor,
      Varga, Rook); six takes pinned (five re-rendered, Varga's t=71 hint line pinned to its own seed);
      `VoiceFilesTest` covers Level 18 (M7 part E, step VOX-E, 2026-10-19; pending the user's review in round
      41). Open: `RadioTimelineTest` gets Level 18's runs once the level is mapped (step E4)
- [x] Level 19's lines (18 timed with Rook's five, `requires: escort`; the walker's `first-telegraph` flame tell,
      Rook's `boss-part-destroyed` and `boss-destroyed` lines; the payroll safe's secret, the secondary's and the
      level end's lines; the two briefing pages: 25 new files, Okafor's "Not on this channel, Doctor." reusing Level
      17's take) rendered with the cast speakers (Okafor, Varga, Rook, Ares Control); nine takes pinned;
      `VoiceFilesTest` covers Level 19 (M7 part F, step VOX-F, 2026-10-19; pending the user's review in round
      42). Open: `RadioTimelineTest` gets Level 19's runs once the level is mapped (step F4)
- [x] Level 20's lines (17 timed with Rook's five, four `requires: escort`; the emergency cache's secret, the secondary's and the level
      end's lines; the two briefing pages: 22 files) and Rook's three "Up top!" barks of a reversed section (the
      rear-wave bark's `top_lines`, source `wingmen barks rear-wave top`) rendered with the cast speakers (Okafor,
      Varga, Rook, Ares Control); four takes pinned; `VoiceFilesTest` covers Level 20 and the top barks (M7 part G,
      step VOX-G, 2026-10-20; decided in round 48 (2026-10-22)). Open: `RadioTimelineTest` gets Level 20's runs once the
      level is mapped
- [x] Level 21's lines (eight timed with Rook's two side takes, `requires: escort`; the three `boss-phase` lines;
      the four `boss-destroyed` lines with the Choir's spoken "…many… we are many… we are going…", `distorted`, with
      the Choir's layering; the survey crawler's secret, the level end's line; the two briefing pages: 21 files; the
      secondary has no line) and the Act 3 outro's four pages rendered with the cast speakers (Okafor, Varga, Rook,
      Ares Control, the Choir): 25 files, seven takes pinned; `VoiceFilesTest` covers Level 21 and the Act 3 outro
      (M7 part H, step VOX-H, 2026-10-20; decided in round 48 (2026-10-22)). Open: the Choir line's fragment joins (0.7 s
      of drone between separately spoken fragments) and its layering a/b (Design, The Choir) are not built: the
      line is one take through the round-18 layering (a); `RadioTimelineTest` gets Level 21's runs once the level
      is mapped
- [x] A line that is only a stage direction plays its speaker's `stage` sound (speaker table) on
      the voice bus, found by `VoiceLines.radioVoice` for the game and RadioTimelineTest; the
      Choir's is round 29's option b (the sung "ah" F3 to E3), copied by
      `:pipeline:copyPlaceholderStageSounds`; VoiceFilesTest checks the file and every Choir cue
- [x] Act 3 (approved in round 36, M7): Ares Control cast (round 36, S.6 = b, Thomas A): his clip
      renamed `refs/ref-ares-control.wav`, the speaker table without `uncast`, his takes checked for
      mid-line pauses, the colonial portrait b (part B); Lance Control cast a/b in part A's round (round 37, S.16 = b, Celine Major: her clip renamed `refs/ref-lance-control.wav`, the speaker in the table, the call rendered and spoken in flight, step Q2); the respelling list in the text as spoken (built in part B step B2: `respell` in
      the speaker table, `VoiceData.respelled`; Ares Control in the table) and the
      Act 3 names in the read-back prompt (part B); the Choir's spoken line (part H: accepted as one take in round 44, Q.7; held 4.5 s past the death roar, round 45 B.a); each Act 3 level's lines and the act intro and outro pages rendered and
      reviewed in its part's round (the whole Act 3 listen-through, 202 lines, accepted in round 45, V.1), `RadioTimelineTest` voiced per level with and without Rook
- [x] Level 22's lines (11 radio with Rook's two `requires: escort`, the `first-geyser` and `target-destroyed` lines,
      the core-sample cache's secret, the secondary's line; the two briefing pages) and the Act 4 intro's four pages
      rendered with the cast speakers (Okafor, Varga, Rook) and Dr. Brandt's candidate a: 18 files, four takes pinned;
      her five lines also with candidate b in `concept/rejected/voice-conamara-r51-b/`; the Act 4 names read back as written
      (M8 part B, step VOX-B, 2026-10-22; accepted in round 51 (2026-10-22), Dr. Brandt a)
- [x] Level 23's lines (11 radio with Rook's `requires: escort` and the `waterline` line, the survey probe's secret,
      the secondary's line; the two briefing pages) rendered with the cast speakers and Thera Deep Control's candidate
      a: 14 files, five takes pinned; his four lines also with candidate b (two pins), b cast in round 52 (its takes in
      `assets/voice/thera-control/`, a's in `concept/rejected/voice-thera-control-r52-a/`); the Act 4 names read back as
      written (M8 part C, step VOX-C, 2026-10-22; accepted in round 52 (2026-10-22), Thera Deep Control b)
- [x] Level 24's lines (10 radio with Rook's two `requires: escort` lines and the `first-ray` line, the secondary's
      line with its easy and hard variants, the *Meridian* wreck's secret; the two briefing pages) rendered with the cast
      speakers (Okafor, Varga, Rook; no new speaker, no Thera Deep Control line): 16 files, five takes pinned; the Act 4
      names read back as written; `VoiceFilesTest` and `RadioTimelineLevel24Test` (applied in step D4)
      (M8 part D, step VOX-D, 2026-10-22; accepted in round 53 (2026-10-23))
- [x] Level 25's lines (10 radio with Rook's `requires: escort` line, the secondary's line, the survey sub's secret;
      the two briefing pages) rendered with the cast speakers (Okafor, Varga, Rook; no new speaker, no Thera Deep Control
      line): 14 files, seven takes pinned; the Act 4 names read back as written; `VoiceFilesTest` (with Level 25 in
      `briefs/m8voxe-tests.patch`) and `RadioTimelineLevel25Test` (step E3's patch) green in a snapshot
      (M8 part E, step VOX-E, 2026-10-23; accepted in round 54 (2026-10-23))
- [x] Level 26's lines (16 radio, Rook's three scripted lines with his cut-off at t=161.2 and the Choir's t=99 words
      among them; the farm barge's secret; the two briefing pages) rendered with the cast speakers (Okafor, Varga, Rook,
      the Choir; no Thera Deep Control line): 19 files, five takes pinned; Rook's cut spoken "us" (the respelling `u: us`)
      and cut inside it; the Choir's line through Level 21's treatment (one take, the round-18 layering, `distorted`);
      `VoiceFilesTest` and `RadioTimelineLevel26Test` (with Level 26 voiced: `briefs/m8voxf-tests.patch` after step F2's
      `briefs/m8f2-radio.patch`) green in a snapshot (M8 part F, step VOX-F, 2026-10-23; pending the user's review in
      round 55)
- [x] Act 4 (draft, round 48, M8): Dr. Ilse Brandt and Thera Deep Control cast a/b (their clips in `refs/`, the
      speakers in the table without `uncast`); the Choir's two spoken lines (Level 26's whisper, Level 28's death
      line); Rook's cut-off at t=161.2; the Act 4 names tried in the read-back; each Act 4 level's lines and the act
      intro and outro pages rendered and reviewed in its part's round (built: Dr. Brandt cast a in round 51, Thera Deep Control b in
      round 52, Levels 22–26 above, Levels 27 and 28 and the outro in parts G and H, reviewed in rounds 57 and 58; M8 part
      I: the 119 voiced Act 4 lines checked against a Whisper transcript, no file missing, none clipped; the whole Act 4
      listen-through accepted in round 59, V.1)
- [x] Level 29's lines (24 radio lines with the supply cache's secret: Ceres Hub Control, Harker on the enemy filter,
      Okafor, Varga, Rook's two lines from the rescue on), its two briefing pages and the Act 5 intro's four pages (Vorne's
      pages 2–3 on the enemy filter) rendered: 30 files, 11 takes pinned; Ceres Hub Control's and Harker's 11 lines also
      with candidate b (`concept/voice-<speaker>-r62-b/`, seven pins); every take read back right with the Act 5 name
      prompt (homophones only: "Doc 9" for "Dock 9", "guns alive" for "guns are live"); `VoiceFilesTest` with Level 29
      and the Act 5 briefing in `briefs/m9-voxb-tests.patch` (with `VoiceLines`' enemy filter) green in a snapshot
      (M9 part B, step VOX-B, 2026-10-28; cast in round 62); step B4b's 12 trimmed or new lines
      re-rendered with both candidates; heard in the game in round 62's captures (V.1–V.3 the casting and Vorne's pitch)
- [x] Level 30's lines (16 radio lines: 12 timed, the convoy's met and escaped lines, the level end and the secret's; its two briefing
      pages): 18 files in `assets/voice/{okafor,varga,rook}`, two pins (step C2); Okafor's page 1 shortened to four lines
      and re-rendered (step C5: pin `9ed7c0c8776e` 101, read back word for word with the name prompt);
      `VoiceFilesTest` and `RadioTimelineLevel30Test` (with Rook, `--escort none` and the Bulwark; step C4); heard in
      round 63's captures
- [x] Level 31's lines (its two briefing pages; Okafor, Varga and Rook; the Bellwether's 12 with the six `{section}` takes and the Dacosta's claim secret): 26 files, none of the others pinned; the Bellwether cast a/b for round 64 (`refs/ref-bellwether-r64-{a,b}.wav`, cast b in round 64: b's takes (four pins) in `assets/voice/bellwether/`, a's (seven pins) in `concept/rejected/`; the auditions `concept/voice-bellwether-r64-b.ogg`, `concept/rejected/voice-bellwether-r64-a.ogg`); every take read back with the Act 5 name prompt (the weak spots in *Act 5 voices*, Rendered); `VoiceFilesTest` with Level 31 (M9 part D, steps VOX-D and VOX-D2, 2026-10-28; round 64)
- [x] Level 32's lines (its two briefing pages; Okafor's, Varga's and Rook's radio with fork one's two `branch` lines,
      the `topple-crush` line and the consortium store's secret): 18 files with the cast voices, none pinned (four takes
      retried by the length/pause rule); every take read back word for word by Whisper (plain; "Kleopatra" reads as its
      homophone "Cleopatra", "cache" as "cash", "chute" as "shoot", no respelling needed); `VoiceFilesTest` with Level 32
      (M9 part E, step E2, 2026-10-29; recorded in round 65's captures, step E5)
- [x] Level 33's lines (its two briefing pages; Okafor's, Varga's and Rook's radio, the secondary's line and Claim
      114's secret; the Choir's line with its layering, as Level 26's): 18 files, two pinned (Okafor's t=1 `6b18f72afb26`
      and Rook's t=150 `a7ecf962ab3e`, both 101: the keys' seeds ran an octave high), four takes retried by the
      length/pause rule; every take read back by Whisper word for word with the Act 5 name prompt (plain: "leeches" as
      its homophone "leaches", "Claim 114" as "Claim one-fourteen", "Vorne" heard as "Warren" without the prompt; the
      Choir's layered line unreadable, as its earlier ones); no respelling. The Choir's take runs 6.0 s: with its 2 s
      static lead it ends at t≈126, before Varga's answer (t=130); `VoiceFilesTest` with Level 33 (M9 part F, step F2,
      2026-10-29; the secret's Rook line only with him flying, step F4; recorded in round 66's captures, step F5)
- [x] Level 34's lines (its two briefing pages; Okafor's, Varga's and Rook's radio, the Helix core's `story-collected`
      and `story-missed` lines, the secondary's line and the three secrets' lines): 16 files, seven pinned (Okafor's
      briefing page 1 `d33b85b98791` at 1001: every take of the key's seed and of 101 held a 0.8-1.9 s pause; her core
      lines `8119302f4829` 301 and `12dce0570381` 101, read "Cora Board" and "the call"; Varga's t=29 `223a3a8d061e` 101,
      read "holes" for "hulls"; Varga's t=108 `1c6d8c36e19c` and Rook's t=12 `1021bdd87409` 101, an octave up; Rook's
      *Old Marta* `874ab5adcacc` 101, read "Sorry, man": its kept take holds a 0.9 s pause before "Sorry"), every take
      read back by Whisper word for word with the Act 5 name prompt (plain: "wrecks" as "Rex", "ahead" as "a head",
      "grey/colours" spelt US); Varga's *Sable Wren* line kept on its key's seed (about 440 Hz on every seed tried, word
      for word); no respelling; `VoiceFilesTest` with Level 34 (M9 part G, step G2, 2026-10-29). Step FIX2 (pending
      round 67) trimmed *Sable Wren* to one HUD page ("Helix courier. Sealed medical crates. …") and re-rendered it:
      `f035b444ad10`, 4.3 s on its key's seed, read back word for word except "courier" as "Currier" (every seed,
      101-104 too, with and without the name prompt: a homophone; no pin); the old take deleted; recorded in round 67's captures
      (step G5)
- [x] Level 35's lines (its two briefing pages; Okafor's, Varga's, Rook's and Ceres Hub Control's radio, the
      lifeboat's line, Vorne's four on the enemy filter: arrival `smug`, first lock neutral, the core's opening `rage`
      (the grim row, never `shout`), the recruitment neutral; Okafor's answer without Rook), the Act 5 outro's four
      pages and the intro's page 3 again (now `smug`: the expression is part of the key; the old take deleted): 29 files,
      seven pinned (Vorne's intro page 3 `339ad6a165f8` 102: the key's seed read "stand aside", 101 "your souls"; his
      first lock `88f6d2289169` 101, "Pilate"; Okafor's briefing page 1 `8dae64c4ed2a` 101, a 1.5 s pause; her kill line
      `f07eba3bbc15` 102, "orbiters ours"; Ceres Control's t=9.5 `e75df2be2768` 101, "Series"; his lifeboat line
      `3d5ca3461eb5` 101, 364 Hz through the filter; Rook's outro page 3 `c7105bd562bb` 101, "stay in there"), every take
      read back by Whisper with the Act 5 name prompt word for word except Whisper's merges ("Vorne Station", "store's
      ages", "Learn that"); Varga's "Kill a ring's guns" read "Killer rings guns" on every seed: reworded "Destroy a ring's
      guns" in her briefing page and her arena line; no respelling ("nav core" reads right with the name prompt);
      measured on the raw takes Vorne 95–128 Hz, Ceres Control 110–132 Hz; `VoiceFilesTest` with Level 35 and the Act 5
      outro (M9 part H, step H2, 2026-10-29); heard in round 68's captures (step H5).
- [x] Level 36's lines and the Act 6 intro (its four pages, Vorne's page 3 on the enemy filter; Level 36's two briefing
      pages, Okafor's, Varga's and Rook's radio with the `first-strike-kill` line, the pathfinder pod's secret): 22 files,
      eight pinned (the intro's four pages; the t=67, secondary and t=128 lines), every take read back by Whisper with
      the Act 6 name prompt, no respelling (*Act 6 voices*, Rendered); `VoiceFilesTest` with Level 36 and the Act 6
      briefing in `briefs/m10-voxb-tests.patch` for B3/B4 to apply, green in a snapshot (M10 part B, step VOX-B,
      2026-10-31)
- [ ] Act 6 (draft, decided 2026-10-31 (round 72), M10): every Act 6 line and page rendered and read back with the Act 6 name
      prompt (parts B–H); Vesper, Kestrel and Aurelia Traffic cast from round 72's picks (refs and CREDITS.md rows);
      Harker's Level 40 lines on his cast clip; Vorne's Level 42 line 5 at the picked shout take, pinned; the build
      check rejecting `shout: true` on Vorne's cues with Level 42's line 5 as its one exception; `VoiceFilesTest` per
      level

## Concept art

Rejected proposals (41 rows) are listed in [concept/rejected/README.md](concept/rejected/README.md).

Concept [round 19](../../concept-rounds/round-19/README.md) — casting the generic speakers: each
speaker says one of its real lines with two candidate reference voices (`-a`, `-b`), rendered by
Chatterbox through radio filter b (`tools/concept/audio/tts_r19.py`; lines, settings and readers
in [concept/prompts.md](concept/prompts.md), the clips in [refs](refs/README.md)).

| File | What | Status |
|---|---|---|
| [concept/voice-yard-control-r19-a.ogg](concept/voice-yard-control-r19-a.ogg) | Yard Control, reader Mark Nelson (`tools/concept/audio/tts_r19.py`) | chosen |
| [concept/voice-dock-r19-b.ogg](concept/voice-dock-r19-b.ogg) | Dock One–Four, reader Kristin Hughes (`tools/concept/audio/tts_r19.py`) | chosen |
| [concept/voice-ring-control-r19-b.ogg](concept/voice-ring-control-r19-b.ogg) | Ring Control, reader Cori Samuel (`tools/concept/audio/tts_r19.py`) | chosen |
| [concept/voice-tranquility-control-r19-b.ogg](concept/voice-tranquility-control-r19-b.ogg) | Tranquility Control, reader Karen Savage (`tools/concept/audio/tts_r19.py`) | chosen |
| [concept/voice-crawler-one-r19-b.ogg](concept/voice-crawler-one-r19-b.ogg) | Crawler One, reader Kara Shallenberg (`tools/concept/audio/tts_r19.py`) | chosen |
| [concept/voice-convoy-r19-a.ogg](concept/voice-convoy-r19-a.ogg) | Convoy, reader Adrian Praetzellis (`tools/concept/audio/tts_r19.py`) | chosen |
| [concept/voice-hammer-lead-r19-b.ogg](concept/voice-hammer-lead-r19-b.ogg) | Hammer Lead, reader Gord Mackenzie (`tools/concept/audio/tts_r19.py`) | chosen |
| [concept/voice-driver-control-r21-a.ogg](concept/voice-driver-control-r21-a.ogg) | Driver Control (round 21 audition), reader Alex Foster (`tools/concept/audio/tts_r21.py`) | chosen |
| [concept/voice-perimeter-beacon-r23-a.ogg](concept/voice-perimeter-beacon-r23-a.ogg) | Daedalus perimeter beacon (round 23 audition, public-address filter), reader Mark F. Smith (`tools/concept/audio/tts_r23.py`) | chosen |
| [concept/voice-lifeboat-seven-r25-a.ogg](concept/voice-lifeboat-seven-r25-a.ogg) | Lifeboat Seven (round 25 audition), reader Tadhg Hynes (`tools/concept/audio/tts_r25.py`) | chosen |
| [concept/voice-choir-sings-r29-b.ogg](concept/voice-choir-sings-r29-b.ogg) | The Choir sings (round 29): sung sting, "ah" falling F3 to E3 in the Choir's voice, layered (`tools/concept/audio/choir_r29.py`) | chosen |

Concept [round 30](../../concept-rounds/round-30/README.md) (M5 part B) — casting the Ikoyi
shelter civilian of Level 08: her two lines (the t=113 call and the secondary objective's thanks,
1 s apart) with two candidate reference voices (`-a`, `-b`), rendered by Chatterbox at the neutral
settings through radio filter b (`tools/concept/audio/tts_r30.py`; the clips
`refs/ref-civilian-r30-a.wav` and `-b.wav`). Closed 2026-10-07: **b** chosen (its clip renamed
`refs/ref-civilian.wav`); a in `concept/rejected/`, its clip deleted.

| File | What | Status |
|---|---|---|
| [concept/voice-civilian-r30-b.ogg](concept/voice-civilian-r30-b.ogg) | The Ikoyi shelter civilian (round 30 audition), reader Faith Abiola-Ellison (`tools/concept/audio/tts_r30.py`) | chosen |

Concept [round 31](../../concept-rounds/round-31/README.md) (M5 part C) — casting the Kilo Lead of
Level 09: his two lines (the t=95 call and the secondary objective's thanks, 1 s apart) with two
candidate reference voices (`-a`, `-b`), rendered by Chatterbox at the neutral settings through
radio filter b (`tools/concept/audio/tts_r31.py`). Closed 2026-10-07: **a** chosen (its clip renamed
`refs/ref-kilo-lead.wav`); b in `concept/rejected/`, its clip deleted.
Concept [round 32](../../concept-rounds/round-32/README.md) (M5 part D) — casting Level 10's
Lifeline (three of its lines) and Lifeline Three (its one line, cut off) with two candidate
reference voices each (`tools/concept/audio/tts_r32.py`). Decided 2026-10-08: **b** for both (the
clips renamed `refs/ref-lifeline.wav` and `refs/ref-lifeline-three.wav`); the a auditions in
`concept/rejected/`, their clips deleted.

| File | What | Status |
|---|---|---|
| [concept/voice-kilo-lead-r31-a.ogg](concept/voice-kilo-lead-r31-a.ogg) | The Kilo Lead (round 31 audition), reader Aaron Bennett (`tools/concept/audio/tts_r31.py`) | chosen |
| [concept/voice-lifeline-r32-b.ogg](concept/voice-lifeline-r32-b.ogg) | Lifeline (round 32 audition), reader KevinS (`tools/concept/audio/tts_r32.py`) | chosen |
| [concept/voice-lifeline-three-r32-b.ogg](concept/voice-lifeline-three-r32-b.ogg) | Lifeline Three (round 32 audition; the line cut off by the lance), reader Maria Kasper (`tools/concept/audio/tts_r32.py`) | chosen |

Concept round 35 (M5 part G, decision G9 = a, pending the user's review) — casting Level 13's new speaker
Nansen, the CDF officer of Nansen Relay: three of its lines (the t=36 arrival, the `ally-below` 0.5 line and
the t=218 grid sync) with two candidate reference voices, a male (a) and a female (b), rendered by
Chatterbox at the neutral settings through radio filter b (`tools/concept/audio/tts_r35.py`; the readers
and pitches in [concept/prompts.md](concept/prompts.md#voice-nansen)); the candidates' clips
`refs/ref-nansen-r35-a/b.wav`. Closed in concept round 34 (user, 2026-10-12): **a** (Patrick Eaton), his clip
renamed `refs/ref-nansen.wav`; b's clip moved to `concept/rejected/` with its CREDITS.md row.

| File | What | Status |
|---|---|---|
| [concept/voice-nansen-r35-a.ogg](concept/voice-nansen-r35-a.ogg) | Nansen (round 35 audition), reader Patrick Eaton (`tools/concept/audio/tts_r35.py`) | chosen |

Concept [round 36](../../concept-rounds/round-36/README.md) (Act 3 "Red Dust", M7 part B, pending the user's
review) — casting **Ares Control**, Ares Landing's flight controller: three of his lines (Level 15's t=12
jab, Level 17's t=102 dome names, Level 21's closing line, 1 s apart) with two candidate male reference
voices, rendered by Chatterbox at his proposed base settings through radio filter b
(`tools/concept/audio/tts_r36.py`; readers, pitches and measurements in
[concept/prompts.md](concept/prompts.md#voice-ares-control)); the text as spoken uses the proposed
respellings Airees, Skyaparelli and Burrows. The candidates' clips `refs/ref-ares-control-r36-a/b.wav`. Closed in round 36 (user, 2026-10-13): **b** (Thomas A), with the
respellings; a's audition and its reference clip moved to `concept/rejected/` with the clip's CREDITS.md
row; b's clip is renamed `refs/ref-ares-control.wav` at the render step (part B).

| File | What | Status |
|---|---|---|
| [concept/voice-ares-control-r36-b.ogg](concept/voice-ares-control-r36-b.ogg) | Ares Control (round 36 audition), reader Thomas A (clip 112 Hz, 25.2 s, a 2 s pause inside the second and third lines; `tools/concept/audio/tts_r36.py`) | chosen (round 36, S.6: Thomas A; mind his mid-line pauses when rendering) |
| [concept/voice-lance-control-r37-b.ogg](concept/voice-lance-control-r37-b.ogg) | Lance Control (round 37 audition), reader Celine Major (clip 159 Hz, take 162 Hz; seed 101, 2.62 s; `tools/concept/audio/tts_r37.py`) | chosen (round 37, S.16: Celine Major; her clip is `refs/ref-lance-control.wav`, the call rendered from it in step Q2) |
| [concept/voice-conamara-r51-a.ogg](concept/voice-conamara-r51-a.ogg) | Dr. Brandt, `conamara` (round 51 audition), reader Margaret Espaillat (clip 168 Hz, a very narrow range, takes 165 / 162 Hz; Level 22's t=1 and t=100 lines, seeds 100 / 100, 17.1 s; `tools/concept/audio/tts_r51b.py`) | chosen (round 51: Dr. Brandt is Margaret Espaillat) |
| [concept/voice-thera-control-r52-b.ogg](concept/voice-thera-control-r52-b.ogg) | Thera Deep Control, `thera-control` (round 52 audition), reader John Van Stan (clip 119 Hz, a narrow range, 1.7 words a second, takes 133 / 126 Hz; the same two lines, seeds 101 / 105, 14.3 s; `tools/concept/audio/tts_r52c.py`) | chosen (round 52, V.1 = b: Thera Deep Control is John Van Stan) |
| [concept/voice-vorne-r60-a.ogg](concept/voice-vorne-r60-a.ogg) | Vorne, `vorne` (round 60 audition, step AUD-P), reader Jeffrey Church (The Genealogy of Morals; clip 103 Hz, a wide range, 2.0 words a second: a slow, deliberate argument read aloud; takes 131 / 129 Hz), the broadcast's opening and the recruitment through the new enemy filter (`filter: enemy`: filter b's band and saturation, no crackle, a steady static bed 26 dB under the voice swelling ±3 dB at 0.25 Hz, the intercept's sweep in and out), seeds 103 / 102, 19.7 s; `tools/concept/audio/tts_r60.py` | chosen (round 60) |
| [concept/vorne-pitch-r62-a.ogg](concept/vorne-pitch-r62-a.ogg) | Vorne's pitch (round 61 C.9, step VOX-B): the Act 5 intro page 2's chunk "From this hour the Jovian Ascendancy governs itself, and it does not stand alone: the Vrell are our partners." at his settings (0.6 / 0.4 / 0.7) through the enemy filter, **a** with the production clip `refs/ref-vorne.wav` (Jeffrey Church's clip pitched −2 semitones, formants preserved: Chatterbox re-voices him lower, 90 Hz median, IQR 18), seed 100, 6.8 s; `tools/concept/audio/tts_r62.py --pitch` | chosen (round 62, V.3 a: −2 semitones, the production clip) |
| [concept/voice-ceres-control-r62-a.ogg](concept/voice-ceres-control-r62-a.ogg) | Ceres Hub Control, `ceres-control` (round 62 audition, QB-1), candidate a **Paul Adams** (Sidelights on Relativity; clip 123 Hz, IQR 24, 2.7 words a second: an older, low, even voice), Level 29's t=1 and t=101 lines as the game plays them (the production takes in `assets/voice/ceres-control/`, 1 s apart; takes 143 / 144 Hz; t=101 trimmed in step B4b), 15.9 s; `tools/concept/audio/tts_r62.py --auditions` | chosen (round 62, V.1 a: Paul Adams) |
| [concept/voice-harker-r62-b.ogg](concept/voice-harker-r62-b.ogg) | The same with candidate b **David Clarke** (The Count of Monte Cristo; clip 104 Hz, IQR 21, 2.5 words a second: hard and level, an English voice apart from Rook's; takes 122 / 125 Hz), 10.8 s | chosen (round 62, V.2 b: David Clarke) |
| [concept/voice-bellwether-r64-b.ogg](concept/voice-bellwether-r64-b.ogg) | The same with candidate b **Laurie Anne Walden** (A Diary from Dixie, Mary Chesnut; clip 181 Hz, IQR 37, 2.4 words a second: slow, plain and unhurried, American; the takes now in `assets/voice/bellwether/`; 192 Hz, IQR 57), 14.2 s | chosen (round 64, V.1 b: Laurie Anne Walden) |

Concept [round 33](../../concept-rounds/round-33/README.md) (M5 part E, user decision E10 = a) — casting Level 11's Atlas Control, the CDF
officer of convoy Atlas-Seven: three of its lines (the t=1 call, the `ally-hit` line with `{ally}` =
Halvorsen and the t=62.5 sonar contact, 1 s apart) with two candidate reference voices, a male (a)
and a female (b), rendered by Chatterbox at the neutral settings through radio filter b
(`tools/concept/audio/tts_r33.py`; the readers and pitches in
[concept/prompts.md](concept/prompts.md#voice-atlas-control)). Closed 2026-10-09: **b** chosen (MaryAnn,
the clip renamed `refs/ref-atlas-control.wav`); the a audition in `concept/rejected/`, its clip
deleted.

| File | What | Status |
|---|---|---|
| [concept/voice-atlas-control-r33-b.ogg](concept/voice-atlas-control-r33-b.ogg) | Atlas Control ([round 33](../../concept-rounds/round-33/README.md) audition), reader MaryAnn (`tools/concept/audio/tts_r33.py`) | chosen |

Concept [round 29](../../concept-rounds/round-29/README.md) — the sound of the Choir's stage
direction `[the Choir sings]`: a wordless sung sting in the Choir's voice (a, b) or a synthesized
choir pad (c, d), all through radio filter b (`tools/concept/audio/choir_r29.py`; how each is made
in [concept/prompts.md](concept/prompts.md#voice-choir-sings)). Closed 2026-10-06: **b** chosen;
a, c and d in `concept/rejected/`.

Lance Control's audition (concept [round 37](../../concept-rounds/round-37/README.md), S.16; its rows are in
the round-36 table above): closed 2026-10-13 (user): **b** (Celine Major). Her clip is
`refs/ref-lance-control.wav` (the speaker `lance-control` in data.yaml, the call rendered in step Q2);
Ted Lienhart's audition, clip and transcript moved to `concept/rejected/`, the clip's CREDITS.md row with them.

Concept round 72 (M10 "Act 6 - Jovian Storm" design phase, step AUD-R, 2026-10-31; picked 2026-10-31) —
the Act 6 voices of *Act 6 voices (draft)*: two candidate readers each for **Vesper**, **Kestrel** and **Aurelia
Traffic** (new LibriVox readers, public domain / CC0, never a main-cast reader nor a voice already cast or auditioned;
`uncast: true` until the round picks), each reading two of the speaker's lines at the README's rows, Harker's cast
voice on one ace line, and Vorne's shout two ways; rendered by Chatterbox (`tools/concept/audio/voice_r72.py`, four
seeds a line, each pinned take cut after the line's last word), the aces and Vorne through the enemy filter, Aurelia
Traffic through radio filter b. The candidates' clips are `refs/ref-<speaker>-r72-<a|b>.wav`; readers, lines and
measurements in [concept/prompts.md](concept/prompts.md#round-72--act-6-voices). The accents are not checked by ear
(Claude does not listen): the candidates were chosen by clip pitch and pace. Round 73's follow-ups (step m10-f72, 2026-10-31; pending the user's review):
Aurelia Traffic's sign-off re-rendered for "lift" and Vorne's shout recast low (a and b), the last three rows,
`tools/concept/audio/voice_r73.py`; briefs in [concept/prompts.md](concept/prompts.md#round-73--act-6-voice-follow-ups). Round 74 (M10 part B, step B5): a listen-through of
the Act 6 lines part B voiced and the intro's page 4 at two readings, the last four rows, `tools/concept/audio/voice_r74.py`
([concept/prompts.md](concept/prompts.md#round-74--act-6-listen-through-and-page-4)).

| File | What | Status |
|---|---|---|
| [concept/voice-vesper-r72-b.ogg](concept/voice-vesper-r72-b.ogg) | The same with candidate b **Jenny Bradshaw** (Harrington, ch. 1, Edgeworth; clip 172 Hz, IQR 46, 3.8 words a second: the quickest read), seeds 102 / 102, takes 167 / 176 Hz, 5.8 s; `tools/concept/audio/voice_r72.py` | chosen (round 72 V.1 b) |
| [concept/voice-kestrel-r72-a.ogg](concept/voice-kestrel-r72-a.ogg) | Kestrel, `kestrel` (round 72 audition), candidate a **Expatriate** (Life and Death of Harriett Frean, ch. 1, Sinclair; clip 137 Hz, IQR 40, 3.2 words a second), his entry taunt (neutral) and "No more games." (grim, slower) through the enemy filter, seeds 103 / 100, takes 152 / 148 Hz, 4.8 s; `tools/concept/audio/voice_r72.py` | chosen (round 72 V.2 a) |
| [concept/voice-harker-r72-a.ogg](concept/voice-harker-r72-a.ogg) | Harker as an ace: his cast voice (`refs/ref-harker-r62-b.wav`, David Clarke) on his Level 40 entry taunt at the fierce row through the enemy filter, seed 103, 128 Hz (his Level 29 takes 110–131 Hz), 4.0 s; no audition, the cast voice heard in Act 6's fight; `tools/concept/audio/voice_r72.py` | chosen (round 72 V.4 ok) |
| [concept/voice-aurelia-traffic-r73-b.ogg](concept/voice-aurelia-traffic-r73-b.ogg) | Aurelia Traffic, **Lizzie Driver** (round 72 V.3 b), her t=1 call (round 72's take, seed 102, 204 Hz) and the t=150 sign-off re-rendered so it says "lift", not "liff": the same clip, grim slower row and radio filter b, seeds 104–119; Whisper reads "lift" in ten of sixteen, pinned seed 117 (the plainest /t/ in a zoom of the word: the /f/, a 35 ms closure, a release burst before the /r/; round 72's seed 103 ran the /f/ into the /r/), 207 Hz, IQR 39; no respelling needed; 11.9 s, −16 LUFS; `tools/concept/audio/voice_r73.py` | chosen (round 73 R.3 accept) |
| [concept/vorne-shout-r73-b.ogg](concept/vorne-shout-r73-b.ogg) | The same, **b a new shouted render lowered**: his clip cut a further 4 semitones down (`ref-vorne.wav` −4, formants preserved), the shout row (1.3 / 0.2 / 0.8) and a stronger one (1.7 / 0.15 / 0.8) on seeds 100–109; four of twenty heard shouted with the break under 0.8 s (189–269 Hz: the shout still lifts him), the brightest pinned (shout row, seed 105; 1–4 kHz share 0.44 against 0.25–0.41), 211 Hz, then ×0.52 (−11.3 semitones) by WORLD: 104 Hz, IQR 14 (a flat, held shout); centroid −7 %; Whisper hears every word, without the "!"; enemy filter, 3.0 s, −16 LUFS; `tools/concept/audio/voice_r73.py` | chosen (round 73 R.4 b) |
| [concept/act-6-listen-r74-a.ogg](concept/act-6-listen-r74-a.ogg) | Listen-through: the Act 6 intro's four pages, Level 36's two briefing pages and its 16 radio lines (with the pod's secret and both secondary outcomes) as the game plays them (`assets/voice/` joined, 1.2 s apart; 3:33, −19 LUFS) | proposed (round 74) |
| [concept/act-6-listen-r74-a.txt](concept/act-6-listen-r74-a.txt) | The listen-through's cue list: each line's start, speaker, source and text | proposed (round 74; a listening aid) |
| [concept/act-6-intro-page-4-r74-a.ogg](concept/act-6-intro-page-4-r74-a.ogg) | The intro's page 4 as built: Okafor at the act data's **fierce** row (0.8 / 0.4 / 0.8), the pinned take (seed 202, 165 Hz), dry, 11.0 s, −16 LUFS (a copy of the game's file) | proposed (round 74) |
| [concept/act-6-intro-page-4-r74-b.ogg](concept/act-6-intro-page-4-r74-b.ogg) | The same page at this README's reading, the **grim row, slower** (0.45 / 0.4 / 0.7, round 72's sign-off row): seed 103 (101 held a 0.9 s pause, 102 hit the cap), dry, 14.7 s, −16 LUFS; Whisper reads it word for word | proposed (round 74) |

## Open questions

- **Act 3**: the Choir's speech layering (a/b in part H's round).
- **Act 5 (round 60, closed 2026-10-27)**: decided as drafted, Vorne a (Jeffrey Church; see *Decisions*). Asked then: Vorne's voice (three readers, a/b/c); the enemy channel's filter as drafted (a steady static
  bed, no crackle or dropouts on the words) or the friendly radio filter with only the intercept's open and close
  (cheaper, but the amber dress would be the only cue); Vorne auditioned now and the generic speakers in their parts'
  rounds (drafted), or all four in round 60.

## Decisions

Entries dated before 2026-10-21 (M8's start): [decisions-archive.md](decisions-archive.md).

- 2026-10-21 (user, [round 45](../../concept-rounds/round-45/README.md), V.1): the listen-through of all 202 voiced Act 3 lines: all fine, the Act 3 voices accepted.
- 2026-10-22 (step AUD48, M8 design phase, decided in round 48 (2026-10-22)): *Act 4 voices* drafted: the two
  colony voices' casting briefs, the Choir's Level 26 whisper treatment, Rook's cut-off, the Act 4 names to try.
- 2026-10-22 (step AUD-B, M8 part B; accepted in round 51 (2026-10-22)): Dr. Brandt's audition with two new
  readers from the public-domain LibriVox recordings (a Margaret Espaillat, b Sue Anderson; passed over: Mil Nicholson,
  147 Hz, too near Okafor; Elizabeth Klett, 219 Hz and a wide range; Kristin LeMoine, a storybook read), the t=1 and
  t=100 lines (the README's "her answer to Varga" has no line of its own in Level 22: t=100 is the call after Varga's).
  Every take read back right; pins by the pitch nearest the clip's. Measured only, not listened to.
- 2026-10-22 (step VOX-B, M8 part B; accepted in round 51 (2026-10-22)): Level 22's 11 radio lines, the secret's
  line, its two briefing pages and the Act 4 intro's four pages rendered (18 files; four retries for mid-line pauses).
  Dr. Brandt has five lines in Level 22, not six; all five rendered with both candidates (a in `assets/`, b in
  `concept/rejected/voice-conamara-r51-b/`), the speaker table's `ref` pointing at a until the pick, the clip names kept since
  they are part of the key. Four takes pinned (Okafor's t=135 "reficked", Varga's t=66 octave jump, Rook's t=82 whisper,
  Brandt a's `target-destroyed` octave jump). No respelling: Conamara, Ilse, Thera Deep, Europa, Kilo-Seven and Vrell
  read back as written with the Act 4 name prompt. Measured only, not listened to.
- 2026-10-22 (step B3b, M8 part B; accepted in round 51 (2026-10-22)): Level 22's hardened-shell text corrected
  (Micro-missiles are not `anti-ground`): Varga's t=29 line ("…bombs, mortars and the Airstrike get through…", 6.1 s
  raw) and her briefing page 2 ("…bombs, mortars and an Airstrike get through…", 13.5 s) re-rendered by
  `tools/art/voice.py` on their keys' seeds, no retry; the two old files deleted. Whisper (base.en, the Act 4 name
  prompt) reads both back word for word but "harden shell" for "Hardened shell" at t=29, as it read the old take;
  pitch 201 and 193 Hz median, in her range. Three lines retimed only (no re-render): Varga's t=66 → 58, Rook's t=82
  → 87, Dr. Brandt's t=100 → 104 (`RadioTimelineLevel22Test` at the autopilot's event times).
- 2026-10-22 (step B5, M8 part B; accepted in round 51 (2026-10-22)): Dr. Brandt's `secondary-objective` line said
  "Three rigs are down" on every difficulty, but the secondary asks for 2 rigs on easy and all 4 on hard: now "Enough rigs
  are down. The shelf is quiet. Thank you, Aegis." ("That's enough rigs down…" wrapped onto a second radio page and pushed
  her t=104 line 2 s late in `RadioTimelineLevel22Test`; this one keeps one page), re-rendered by `tools/art/voice.py` for
  both candidates on the keys' seeds, no retry (a 4.4 s in `assets/voice/conamara/`, b 4.6 s in
  `concept/rejected/voice-conamara-r51-b/`; the old files deleted). Whisper (base.en, the Act 4 name prompt) reads both back word
  for word. Measured only, not listened to.
- 2026-10-22 (user, round 51, closed; V.1 = a): Dr. Brandt (`conamara`) is cast with candidate a, Margaret Espaillat;
  `uncast` is gone (the speaker table in `data.yaml` already carries `ref: ref-conamara-r51-a`, her five takes are in
  `assets/voice/conamara/`). b (Sue Anderson): the audition `voice-conamara-r51-b.ogg`, the five line files and the
  clip `ref-conamara-r51-b.wav/.txt` moved to `concept/rejected/`, the clip's CREDITS.md row with them. The chosen clip
  keeps its `-r51-` name (the name is part of the lines' key); naming the reader on the credits roll needs
  `CreditsList`'s pattern widened or the rename and re-render (a later code step).
- 2026-10-22 (step AUD-C, M8 part C; accepted in round 52 (2026-10-22)): Thera Deep Control's audition with two new
  readers from the public-domain LibriVox recordings (a Bruce Pirie, b John Van Stan; passed over: John Greenman, 103 Hz
  but quick, 3.2 words a second; Andy Minter, 113 Hz, 2.9; Tom Weiss, 114 Hz with octave jumps in the clip; Bob Neufeld
  and Don W. Jenkins, about 150 Hz; Barry Eads, 142 Hz, a wide range; Phil Chenevert, 130 Hz and quick; Clive
  Catterall, too low and creaky to track), the README's t=34 and t=70 lines. a's clip is cut at 120 s (60 s held a
  1.4 s pause). Pins by the pitch nearest the clip's; b's t=70 needed three more seeds (103–105) because Whisper heard
  "racks" as "wrecks" in five takes (105 read back right). "Thera Deep" read back as written: no respelling. Measured
  only, not listened to.
- 2026-10-22 (M8 part C, step C2): the speaker `thera-control` (Thera Deep Control, Levels 23 and 27) added to the
  speaker table with `uncast: true` until round 52 picks a or b; Level 23's data names him `Thera Deep Control` with the
  portrait `thera-deep` (step ART-C's `radio-thera-deep`). AUD-C's audition rows merged here.
- 2026-10-22 (step VOX-C, M8 part C; accepted in round 52 (2026-10-22)): Level 23's 11 radio lines, the survey
  probe's secret and its two briefing pages rendered (14 files). Thera Deep Control has four lines (t=34, t=70, the
  secondary's, the secret's): all rendered with both candidates (a in `assets/voice/thera-control/`, b in
  `concept/voice-thera-control-r52-b/`), the speaker table's `ref` pointing at a until the pick (`uncast` gone). Seven
  takes pinned after three candidate seeds each: Okafor's briefing page 1 (every seed held a 0.9–2.8 s pause, "drill
  the shaft") and t=1 (pauses and an octave jump, "Age is flight"), Okafor's t=134 ("Hold your light, sieges"), Varga's
  waterline line ("air-striking lands") and t=77 ("into the rail"), b's t=34 ("on the shot") and b's secret (an octave
  jump). No respelling: Thera Deep, Conamara, Europa and Vrell read back as written with the Act 4 name prompt (a's
  t=34 as "Thera, Deep Control", a small break inside the name, as Okafor's Act 3 outro page 4); b's "racks" read back
  right in both of his rack lines (AUD-C's "wrecks" did not recur). Pitch: Thera a 111–125 Hz, b 121–133 Hz. The level's
  timed lines as written start up to 19 s late on the text alone (a two-page line holds the radio 11–12 s); a retime
  is proposed for step C3 in `briefs/m8voxc-tests.patch`. Measured only, not listened to.
- 2026-10-22 (step C5a, M8 part C; accepted in round 52 (2026-10-22)): VOX-C's retime applied to Level 23 (Varga
  t=17→13.5, Thera Deep Control t=34→65 and t=70→88.5, Rook t=9→99.5, Okafor t=134→140) and three lines re-rendered for
  their new words: Varga's t=13.5 "Water in twelve seconds…" (the waterline is 12 s after it), Thera Deep Control's
  secondary line "Enough pumps are off the racks…" (true on every difficulty: the third pump on easy, the fifth on hard;
  as Level 22's "Enough rigs are down…"; both candidates, a in `assets/`, b in `concept/voice-thera-control-r52-b/`) and
  Varga's shortened briefing page 2 (it ran to five lines). Whisper read every one back as written (spelling aside:
  "12", "Underwater", "air strike"); no pins, no retries. Measured only, not listened to.
- 2026-10-22 (step VOX-D, M8 part D; accepted in round 53 (2026-10-23)): Level 24's 10 radio lines (the secondary's
  line with its easy and hard variants), the *Meridian* wreck's secret and its two briefing pages rendered with Okafor,
  Varga and Rook (16 files; Level 24 has no Thera Deep Control line, so round 52's pick of b did not apply). Five takes
  pinned after three candidate seeds each: Okafor's briefing page 1 ("tear a deep's" for "Thera Deep's") and t=1
  ("lumps" for "lamps"), Rook's t=24 (an octave jump, "Darks come in"), Varga's `first-ray` line (an octave jump) and
  the secret (an octave jump, "the realm" for "the Vrell"). Whisper (base.en) reads every take back word for word with
  the Act 4 name prompt (Thera Deep, Vrell, Helix, Meridian; spelling aside: "Torpedo's", "Rule 1", "kilometers"); without
  it "Vrell" reads "rail" and "Thera" "Theta", Whisper not knowing the names. No respelling. Pitch (pyin, raw): Okafor
  134-169 Hz, Varga 189-214 Hz, Rook 154-186 Hz. The level's timed lines start on time with and without Rook
  (`RadioTimelineLevel24Test` in a snapshot, runs from the data's rows: the first Ray at t=58, the reefs' secondary from
  t=52 to t=90, the wreck at t≈105-110): no retime. Measured only, not listened to.
- 2026-10-22 (user, round 52, closed; V.1 = b): Thera Deep Control (`thera-control`) is cast with candidate b, John
  Van Stan: the speaker table's `ref` is `ref-thera-control-r52-b` (step D4), his four Level 23 takes (b's keys, two
  pins) moved from `concept/voice-thera-control-r52-b/` into `assets/voice/thera-control/`. a (Bruce Pirie): the
  audition `voice-thera-control-r52-a.ogg`, his four line files (`concept/rejected/voice-thera-control-r52-a/`, moved
  out of `assets/` in step D4) and the clip `ref-thera-control-r52-a.wav/.txt` in `concept/rejected/`, the clip's
  CREDITS.md row with them. The chosen clip keeps its `-r52-` name (the name is part of the lines' key). The calls of
  round 52 (C.2, the radio retime and Thera's new secondary line) accepted.
- 2026-10-23 (step VOX-E, M8 part E; accepted in round 54 (2026-10-23)): Level 25's 10 radio lines (Rook's t=13
  `requires: escort` line among them), the kill-ratio secondary's line, the survey sub's secret and its two briefing
  pages rendered with Okafor, Varga and Rook (14 files; Level 25 has no Thera Deep Control line, so round 52's pick of b
  did not apply). Seven takes pinned after three candidate seeds each: Okafor's t=1 ("Relness sing" for "Vrell nests")
  and t=158 (an octave jump), Varga's t=20, t=33 and the secret (octave jumps, p90 about 460-500 Hz), t=131.5 ("threat a
  spiral" and a jump) and her briefing page 2 (high, median 216 Hz). Whisper (base.en) reads every take back word for
  word with the Act 4 name prompt (Siren Trench, Nautilus, Vrell, Helix, Conamara; homophones aside: "plane", "sack",
  "armor"); without it "Vrell" reads "Well" or "row" and "Conamara" "Connemara", Whisper not knowing the names. No
  respelling. Pitch (pyin, raw): Okafor 141-186 Hz, Varga 184-219 Hz, Rook 143 Hz. The level's timed lines start on
  time with and without Rook (`RadioTimelineLevel25Test` in a snapshot with step E3's `briefs/m8e3-radio.patch`: the
  survey sub at t≈79-82, the secondary at t=173): no retime. Measured only, not listened to.
- 2026-10-23 (step VOX-F, M8 part F; accepted in round 55 (2026-10-24)): Level 26's 16 radio lines (Rook's three
  scripted lines, his t=160.3 cut-off among them, and the Choir's t=99 "…small ones… in the dark… your kin… came before
  you…"), the farm barge's secret and its two briefing pages rendered with Okafor, Varga, Rook and the Choir (19 files;
  no Thera Deep Control line). Rook's cut: spoken as written ("below u,") the voice said "you" and Whisper read "Thumb,
  something's below you", so the speaker table respells `u` as `us` (whole words; no other line has it) and the take
  is cut where Whisper ends "us", as Lifeline Three's. The Choir's line: Level 21's treatment as built (one take in its
  fixed settings, the round-18 layering, `distorted`), the key's own seed; every raw take of nine seeds reads word for
  word, while the layered and distorted file reads differently on every run of Whisper (the dropouts), so it is not
  pinned; the whisper variant (the reversed copy at 0.4, exaggeration 0.25) and the fragment joins are not built. Five
  takes pinned after four candidate seeds each: Okafor's t=167.5 (an octave jump on 31 %), Varga's briefing page 2
  (a jump on 18 %, p90 about 470 Hz) and t=110.5 (a jump on 22 %), Rook's t=148 (a jump on 20 %; the pin level and
  unhurried, his quiet beat) and his cut (the only seed of four reading "Lancer"). Whisper (base.en) reads every other
  take back word for word with the Act 4 name prompt (Vrell, Blackwater, Commander; homophones aside: "pick-up", "2");
  without it "Vrell" reads "rail" or "well". No other respelling. Pitch (pyin, raw): Okafor 140-173 Hz, Varga 188-220 Hz
  (her shouted "Contacts below!" 240), Rook 146-188 Hz. The level's timed lines start on time voiced, with Rook, without
  him and with the Bulwark (`RadioTimelineLevel26Test` in a snapshot with step F2's `briefs/m8f2-radio.patch` and
  `briefs/m8voxf-tests.patch`: Level 26 out of `UNVOICED`, `VoiceFilesTest` with Level 26): no retime. Measured only,
  not listened to.
- 2026-10-24 (step VOX-AW, M8 AW; accepted in round 56 (2026-10-24)): the three lines the water rework changed
  re-rendered: Okafor's Act 4 intro page 3 (guns lose a third, beams and lasers nearly all, save credits for torpedoes and
  charges; 19.9 s, one retry for a 1.0 s pause inside the first chunk), Varga's Level 23 briefing page 2 (16.2 s) and her
  Level 23 t=13.5 radio line, now "…Torpedoes and charges bite as ever; guns lose a third, beams nearly all." (8.3 s;
  worded to keep its six radio lines: a seven-line wording held the radio until the waterline line started 3.4 s late
  on easy). Whisper (base.en) reads every take back word for word, with and without the Act 4 name prompt (spelling
  aside: "12", "Underwater", "storm hawks", "air strike"); pitch (pyin, raw) Okafor median 145 Hz, Varga 204-209 Hz, no
  octave jumps (at most 7 % of frames over 1.6× the median, p90 237-263 Hz): no pins. The three superseded takes deleted.
  Measured only, not listened to.
- 2026-10-26 (user, [round 59](../../concept-rounds/round-59/README.md) closed, V.1): the listen-through of the 119
  voiced Act 4 lines (14 minutes) **accepted**; the 8 flagged lines stay as they are. The Act 4 voice item is ticked.
- 2026-10-27 (step `m9-d-audio`, M9 design phase; **approved in round 60**): *Act 5 voices* drafted:
  the enemy channel's filter (`filter: enemy`: a steady static bed, no crackle or dropouts, the intercept's open and
  close; also on `channel: enemy` briefing pages), Vorne's casting brief (below every cast man, three candidates,
  auditioned in round 60; his rage at the grim row, never shouted), the generic speakers Ceres Hub Control, Harker and
  the Bellwether (names and genders from the Level 29 and 31 drafts; cast in parts B and D), the Choir's Level 33
  line on Level 26's treatment, the Act 5 names to try.
- 2026-10-28 (step VOX-B, M9 part B; accepted in round 62): Level 29's radio and briefing and the Act 5
  intro rendered (30 files, *Act 5 voices*, Rendered). Vorne lowered by a −2 semitone **clip** shift (`ref-vorne`), not a
  shift of the takes (which smeared the formants at every step tried); −3 rejected (an unvoiced take, creak).
  Ceres Hub Control (a Paul Adams, b Phil Chenevert) and Harker (a Ted Delorme, b David Clarke) cast as candidates
  for QB-1, a's `ref` in the speaker table until the pick (as Dr. Brandt and Thera Deep Control in rounds 51–52;
  `uncast` gone, since a speaker with lines in `assets/` needs its clip). No respelling (Ceres, Vorne, Harker, Helix,
  Vrell, RTB read back as written). Measured only, not listened to.
- 2026-10-27 (user, [round 60](../../concept-rounds/round-60/README.md) closed): the Act 5 voice plan approved as drafted (D.19). **V.1 a**: Vorne is **Jeffrey Church**'s reference (b Clive Catterall and c Gregg Margarite rejected). The casting brief asked for a clip under about 105 Hz and a renders at about 130 Hz (Chatterbox lifts it): a is chosen as the voice, and the production render lowers it toward the brief; step VOX-B tries a pitch shift of up to −3 semitones and keeps a only if it stays natural, otherwise it reports back.
- 2026-10-28 (user, [round 62](../../concept-rounds/round-62/README.md), closed; page answers): **V.1 a** Ceres Hub Control is Paul Adams (LibriVox; clip 123 Hz, takes 127-143 Hz); **V.2 b** Harker is David Clarke (English accent, apart from Rook; clip 104 Hz, takes 110-131 Hz); **V.3 a** Vorne's reference clip stays lowered 2 semitones (`refs/ref-vorne.wav`, takes about 90-119 Hz). The accepted C.6: Hub Control's t=60 call is not shouted. Harker's production takes are candidate b's (the a files were moved out of `assets/` and the b files in; same line keys under the clip name `ref-harker-r62-b`); the speaker table's `ref` follows; `tools/art/voice.py --check` reports 655 lines, 0 to render, 0 unused files. The audition files of the unpicked candidates, their line takes and clips moved to `concept/rejected/` (rows in its README, CREDITS.md paths updated, as rounds 51 and 52 did); `art: final`. The in-game credits roll names David Clarke and Paul Adams and no longer Ted Delorme or Phil Chenevert's Hub Control clip.
- 2026-10-28 (user, [round 64](../../concept-rounds/round-64/README.md), closed; page answers): **V.1 b** the Bellwether is Laurie Anne Walden (LibriVox, *A Diary from Dixie*; clip 181 Hz, takes 167-264 Hz: a slow, plain, unhurried American read, apart from Okafor by accent and pace). The speaker table's `ref` is `ref-bellwether-r64-b` (the clip keeps its `-r64-` name: part of the lines' key); b's 12 takes (their four pins apply) moved into `assets/voice/bellwether/`, a's 12 takes, clip and audition into `concept/rejected/` (rows in its README, CREDITS.md path updated); `tools/art/voice.py --keep --list`: 0 to render, 0 unused. The in-game credits roll names Laurie Anne Walden.
- 2026-10-28 (user, [round 61](../../concept-rounds/round-61/README.md), closed): **C.9** accepted: Vorne keeps reference a with the pitch-down (round 62's V.3 a, −2 semitones, already built).
- 2026-10-28 (steps VOX-D and VOX-D2, M9 part D; decided in round 64, below): Level 31's 26 lines rendered (*Act 5 voices*, Rendered); the Bellwether auditioned with a Lucy Burgoyne and b Laurie Anne Walden (LibriVox), her 12 lines with both, a provisional as the speaker table's `ref` (round 64 picked b); a's secret line pinned on the first clean one of 12 further seeds instead of respelling Dacosta; b pinned where it read "bell" for "bow", "car though" for "cargo" and "Drawing" for "Throwing". Measured only, not listened to.
- 2026-10-29 (user, [round 69](../../concept-rounds/round-69/README.md) V.1 **ok**): the Act 5 voice lines as rendered
  (the listen-through of 188 lines, [index](../../concept-rounds/round-69/listen-through.md)) are accepted; no line to
  redo.
- 2026-10-31 (step `m10-d-audio`, M10 design phase; decided 2026-10-31 (round 72)): *Act 6 voices* drafted: the new speakers
  Vesper, Kestrel (the Honour Guard's aces) and Aurelia Traffic (Level 37), two LibriVox candidates each for round
  72; Harker's cast voice for the aces' leader (J4); the carrier is Okafor (J10); Vorne's only shout (J6) at Level
  42's line 5 with a/b takes, and the build check's one exception for it; the Choir's two Act 6 lines on Level
  26's treatment; the Act 6 names to try. Decided here (trivia): Aurelia Traffic urgent but never shouted (`shout:
  false`, the Ceres Hub lesson), Halcyon One–Six without voices of their own, Okafor's Aurelia lines at the grim
  row, "helium-3" spoken as "helium three".
- 2026-10-31 (user, [round 72](../../concept-rounds/round-72/README.md) closed; page answers): **V.1 b** Vesper is
  Jenny Bradshaw, **V.2 a** Kestrel is Expatriate, **V.3 b** (not the recommendation) Aurelia Traffic is Lizzie Driver
  (both takes sit above the 150–175 Hz brief and the sign-off's "lift" comes out "liff": the production render
  re-renders that line until it is clear, a follow-up voice step), **V.4 ok** Harker reuses David Clarke's cast voice
  (J4 kept), **V.5 recast** Vorne's shout: neither take kept, both rows stay `proposed`; a follow-up voice step
  re-renders it (the shout row pitched down toward 100–120 Hz with the break kept, or the fierce row pushed harder).
  J6 kept (his one shout at Level 42's eruption). The rejected candidates' clips and transcripts moved to
  `concept/rejected/` (CREDITS.md paths updated). The Act 6 voice plan approved as drafted (D.18).
- 2026-10-31, round 73: Aurelia Traffic's "lift" accepted (R.3; round 72's take superseded); **Vorne's shout b** (R.4, not the recommendation): the new shouted render lowered to about 104 Hz; a and round 72's two takes rejected.
- 2026-10-31 (step VOX-B, M10 part B): Level 36's radio and briefing and the Act 6 intro rendered (22 files, *Act 6 voices*, Rendered); no respelling (helium-3, Io, Aurelia, Callisto, Vorne read back as written with the Act 6 name prompt; "Eye-oh" tried for Io and dropped); Vorne's Level 42 shout to be produced from round 73's b take when part H builds the level. Measured only, not listened to.
