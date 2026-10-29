---
title: Reference voices
design: approved
implementation: in-progress
art: proposed
depends-on: [..]
updated: 2026-10-28
---

# Reference voices

## Summary

The 12 s clips Chatterbox clones the speakers' voices from: LibriVox chapter readings whose
archive.org items are public domain or CC0, cut to mono 24 kHz WAV and stored in Git LFS. Each
`.txt` is the clip's Whisper (faster-whisper `base.en`) transcript, the check that the cut is the
reader's narration and not an intro or music. Every clip is in [CREDITS.md](../../../../CREDITS.md).

## Design

How the clips are chosen and used: [voice](../README.md#reference-voices).

| Clip | For | Reader | Source | Licence | Status |
|---|---|---|---|---|---|
| `ref-okafor.wav` | Okafor | Ruth Golding | [The Haunted Man and the Ghost's Bargain, ch. 1 (Dickens)](https://archive.org/details/haunted_man_rg_librivox) | CC0 1.0 | chosen (round 18) |
| `ref-varga.wav` | Varga and the Choir's base | Betsie Bush | [Deephaven, ch. 1 (Jewett)](https://archive.org/details/deephaven_0812_bb_librivox) | public domain | chosen (round 18) |
| `ref-rook.wav` | Rook | Rick Rodstrom | [You Know Me Al, ch. 1 (Lardner)](https://archive.org/details/youknowme_al_0908_librivox) | public domain | chosen (round 18) |
| `ref-yard-control.wav` | Yard Control | Mark Nelson | [A Princess of Mars, foreword (Burroughs)](https://archive.org/details/princess_mars_0810_librivox) | public domain | chosen (round 19) |
| `ref-dock.wav` | Dock One–Four | Kristin Hughes | [The Strange Case of Dr. Jekyll and Mr. Hyde, ch. 1 (Stevenson)](https://archive.org/details/jekyll_and_hyde_klh_0904_librivox) | public domain | chosen (round 19) |
| `ref-ring-control.wav` | Ring Control | Cori Samuel | [Frankenstein (1818), preface (Shelley)](https://archive.org/details/frankenstein_cs_librivox) | CC0 1.0 | chosen (round 19) |
| `ref-tranquility-control.wav` | Tranquility Control | Karen Savage | [The Scarlet Pimpernel, ch. 1 (Orczy)](https://archive.org/details/scarlet_pimpernel_ks_librivox) | public domain | chosen (round 19) |
| `ref-crawler-one.wav` | Crawler One | Kara Shallenberg | [Heidi, introduction (Spyri)](https://archive.org/details/heidi_solo_librivox) | public domain | chosen (round 19) |
| `ref-convoy.wav` | Convoy | Adrian Praetzellis | [Treasure Island, front matter (Stevenson)](https://archive.org/details/treasure_island_ap_librivox) | public domain | chosen (round 19) |
| `ref-hammer-lead.wav` | Hammer Lead | Gord Mackenzie | [Scaramouche, book 1 ch. 1 (Sabatini)](https://archive.org/details/scaramouche_gm_librivox) | public domain | chosen (round 19) |
| `ref-driver-control.wav` | Driver Control | Alex Foster | [The Invisible Man, ch. 1–2 (Wells)](https://archive.org/details/invisible_man_librivox) | public domain | chosen (round 21) |
| `ref-perimeter-beacon.wav` | Daedalus perimeter beacon | Mark F. Smith | [The Time Machine (version 2), ch. 1 (Wells)](https://archive.org/details/time_machine_ms_librivox) | CC0 1.0 | chosen (round 23) |
| `ref-lifeboat-seven.wav` | Lifeboat Seven | Tadhg Hynes | [Far From the Madding Crowd, ch. 1 (Hardy)](https://archive.org/details/far_from_the_madding_crowd_th_librivox) | public domain | chosen (round 25) |
| `ref-civilian.wav` | The Ikoyi shelter civilian (Level 08) | Faith Abiola-Ellison | [The Yoruba-speaking Peoples of the Slave Coast of West Africa, selections, section 1 (Ellis)](https://archive.org/details/yorubapeoples_2408_librivox) | public domain (Public Domain Mark 1.0) | chosen (round 30) |
| `ref-kilo-lead.wav` | Kilo Lead (Level 09) | Aaron Bennett | [On Mamba Station: U.S. Marines in West Africa, ch. 1 (Antal, Vanden Berghe)](https://archive.org/details/onmambastation_2507_librivox) | public domain (Public Domain Mark 1.0) | chosen (round 31) |
| `ref-lifeline.wav` | Lifeline: Lifeline One, Two, Four and Five and the hit line (Level 10) | KevinS | [Over the Ocean to Paris, ch. 1 (Dixon)](https://archive.org/details/overtheoceantoparis_2404_librivox) | public domain (Public Domain Mark 1.0) | chosen (round 32) |
| `ref-lifeline-three.wav` | Lifeline Three (Level 10) | Maria Kasper | [The Curtiss Aviation Book, ch. 1 (Curtiss)](https://archive.org/details/curtissaviationbook_2006_librivox) | public domain (Public Domain Mark 1.0) | chosen (round 32) |
| `ref-atlas-control.wav` | Atlas Control (Level 11) | MaryAnn | [Eighteen Months in the War Zone: A Record of a Woman's Work, ch. 1 (Finzi)](https://archive.org/details/eighteenmonthsinthewarzone_1409_librivox) | public domain (Public Domain Mark 1.0) | chosen (round 33) |
| `ref-nansen.wav` | Nansen (Level 13) | Patrick Eaton | [In the Arctic Seas, ch. 1 (McClintock)](https://archive.org/details/arctic_seas_pje_1311_librivox) | public domain (Public Domain Mark 1.0) | chosen (round 35, picked in round 34's review) |
| `ref-ares-control.wav` | Ares Control (Levels 15–17, 19–21) | Thomas A | [The Warlord of Mars, ch. 2 (Burroughs)](https://archive.org/details/warlord_mars_tac_librivox) | public domain (Public Domain Mark 1.0) | chosen (round 36, S.6 = b; renamed at the cast, part B, its pauses trimmed: 10.3 s) |
| `ref-lance-control.wav` | Lance Control (the Orbital Lance's call) | Celine Major | [Campaigns of the Civil War, ch. 1 (Geer)](https://archive.org/details/campaignsofthecivilwar_2603_librivox) | public domain (Public Domain Mark 1.0) | chosen (round 37, S.16 = b) |
| `ref-conamara-r51-a.wav` | Dr. Brandt (`conamara`), candidate a | Margaret Espaillat | [Eminent Victorians, ch. 1 (Strachey)](https://archive.org/details/eminent_victorians_1204_librivox) | public domain (Public Domain Mark 1.0) | chosen (round 51, V.1 = a) |
| `ref-thera-control-r52-b.wav` | Thera Deep Control (`thera-control`), candidate b | John Van Stan | [Early Greek Philosophy & Other Essays, ch. 1 (Nietzsche)](https://archive.org/details/earlygreekphilosophy_2103_librivox) | public domain (Public Domain Mark 1.0) | chosen (round 52, V.1 = b) |
| `ref-vorne-r60-a.wav` | Vorne (`vorne`), candidate a | Jeffrey Church | [The Genealogy of Morals, ch. 1 (Nietzsche)](https://archive.org/details/genealogy_of_morals_1507_librivox) | public domain (Public Domain Mark 1.0) | proposed (round 60) |
| `ref-vorne-r60-b.wav` | Vorne (`vorne`), candidate b | Clive Catterall | [The Prince (version 4), ch. 1 (Machiavelli)](https://archive.org/details/theprincever4_1501_librivox) | public domain (Public Domain Mark 1.0) | proposed (round 60) |
| `ref-vorne-r60-c.wav` | Vorne (`vorne`), candidate c | Gregg Margarite | [Rastignac the Devil, ch. 2 (Farmer)](https://archive.org/details/rastignac_devil_1003_librivox) | public domain | proposed (round 60) |
| `ref-vorne.wav` | Vorne (production clip) | Jeffrey Church | [The Genealogy of Morals, ch. 1 (Nietzsche)](https://archive.org/details/genealogy_of_morals_1507_librivox) | Public Domain Mark 1.0 | `ref-vorne-r60-a.wav` pitched −2 semitones, formants preserved (step VOX-B, `tools/concept/audio/tts_r62.py --refs`); pending round 62 (C.9) |
| `ref-ceres-control-r62-a.wav` | Ceres Hub Control | Paul Adams | [Sidelights on Relativity, ch. 1 (Einstein)](https://archive.org/details/sidelights_relativity_pa_librivox) | Public Domain Mark 1.0 | chosen (round 62, V.1 = a); keeps its `-r62-` name (part of the lines' key) |
| `ref-harker-r62-b.wav` | Harker | David Clarke | [The Count of Monte Cristo, ch. 1 (Dumas)](https://archive.org/details/count_montecristo_1308_librivox) | CC0 1.0 | chosen (round 62, V.2 = b); keeps its `-r62-` name (part of the lines' key) |
| `ref-bellwether-r64-b.wav` | The Bellwether (`bellwether`), candidate b | Laurie Anne Walden | [A Diary from Dixie, ch. 1 (Chesnut)](https://archive.org/details/diaryfromdixie_1101_librivox) | public domain (Public Domain Mark 1.0) | chosen (round 64, V.1 = b); keeps its `-r64-` name (part of the lines' key) |

Round 19's chosen candidates were renamed to `ref-<speaker>.wav`; the rejected candidates and
round 18's Tranquility Control clip (David Leeson), which no speaker uses any more, were deleted
with their CREDITS.md rows (their sources and cuts stay in `tools/concept/audio/tts_r19.py` and
`tts_r18.py`, which rebuild them).

## Implementation

- [x] Main cast clips (Okafor, Varga, Rook) in the repo, in CREDITS.md
- [x] One clip per generic speaker, cast in [round 19](../../../concept-rounds/round-19/README.md)
- [x] Driver Control's clip, cast in [round 21](../../../concept-rounds/round-21/README.md); the chosen
      candidate renamed `ref-driver-control.wav`, the other deleted with its CREDITS.md row
- [x] The perimeter beacon's clip, cast in [round 23](../../../concept-rounds/round-23/README.md); the chosen
      candidate renamed `ref-perimeter-beacon.wav`, the other deleted with its CREDITS.md row
- [x] Lifeboat Seven's clip, cast in [round 25](../../../concept-rounds/round-25/README.md); the chosen
      candidate renamed `ref-lifeboat-seven.wav`, the other deleted with its CREDITS.md row
- [x] The Ikoyi shelter civilian's clip (Level 08), cast in [round 30](../../../concept-rounds/round-30/README.md);
      the chosen candidate renamed `ref-civilian.wav`, the other deleted with its CREDITS.md row
- [x] The Kilo Lead's clip (Level 09), cast in [round 31](../../../concept-rounds/round-31/README.md);
      the chosen candidate renamed `ref-kilo-lead.wav`, the other deleted with its CREDITS.md row
- [x] Lifeline's and Lifeline Three's clips (Level 10), cast in [round 32](../../../concept-rounds/round-32/README.md);
      the chosen candidates renamed `ref-lifeline.wav` and `ref-lifeline-three.wav`, the others
      deleted with their CREDITS.md rows
- [x] Atlas Control's clip (Level 11), cast in [round 33](../../../concept-rounds/round-33/README.md);
      the chosen candidate renamed `ref-atlas-control.wav`, the other deleted with its CREDITS.md row

## Decisions

- 2026-10-03: Created: the round 18 clips moved here from `~/.cache/tv-tts/refs/`; the round 19
  candidates added (licences checked on each archive.org item's `licenseurl`).
- 2026-10-03: Round 19 closed: one clip per generic speaker (convoy a, crawler one b, dock b,
  Hammer Lead b, Ring Control b, Tranquility Control b, Yard Control a); the rejected candidates
  and `ref-control.wav` deleted with their CREDITS.md rows.
- 2026-10-04: Round 21 candidates for Driver Control added (licences checked on each archive.org
  item's `licenseurl`).
- 2026-10-04: Round 21 decided (user): Driver Control is Alex Foster (a), renamed
  `ref-driver-control.wav`; Rebecca's candidate (b) deleted with its CREDITS.md row (its source and
  cut stay in `tools/concept/audio/tts_r21.py`).
- 2026-10-04: Round 23 candidates for the Daedalus perimeter beacon added (licences checked on each
  archive.org item's `licenseurl`).
- 2026-10-05: Round 23 decided (user): the perimeter beacon is Mark F. Smith (a), renamed
  `ref-perimeter-beacon.wav`; Lucy Burgoyne's candidate (b) deleted with its CREDITS.md row (its
  source and cut stay in `tools/concept/audio/tts_r23.py`).
- 2026-10-05: Round 25 candidates for Lifeboat Seven added (Tadhg Hynes, Lizzie Driver; licences
  checked on each archive.org item's `licenseurl`).
- 2026-10-05: Round 25 decided (user): Lifeboat Seven is Tadhg Hynes (a), renamed
  `ref-lifeboat-seven.wav`; Lizzie Driver's candidate (b) deleted with its CREDITS.md row (its
  source and cut stay in `tools/concept/audio/tts_r25.py`).
- 2026-10-05: Every item is ticked (Act 1's speakers all cast, the last in round 25), so the implementation is `done`.
- 2026-10-06: Round 30 candidates for the Ikoyi shelter civilian (Level 08) added (KirksVoice, Faith
  Abiola-Ellison; licences checked on each archive.org item's `licenseurl`: Public Domain Mark 1.0).
- 2026-10-07: Round 30 decided (user): the Ikoyi shelter civilian is Faith Abiola-Ellison (b),
  renamed `ref-civilian.wav`; KirksVoice's candidate (a) deleted with its CREDITS.md row (its
  source and cut stay in `tools/concept/audio/tts_r30.py`).
- 2026-10-07: Round 31 candidates for the Kilo Lead (Level 09) added (Aaron Bennett, tombooker;
  licences checked on each archive.org item's `licenseurl`: Public Domain Mark 1.0).
- 2026-10-07: Round 31 decided (user): the Kilo Lead is Aaron Bennett (a), renamed
  `ref-kilo-lead.wav`; tombooker's candidate (b) deleted with its CREDITS.md row (its source and
  cut stay in `tools/concept/audio/tts_r31.py`).
- 2026-10-08: Round 32 candidates for Lifeline (Atul Sharma, KevinS) and Lifeline Three (Kehinde,
  Maria Kasper) added (Level 10); licences checked on each archive.org item's `licenseurl`.
- 2026-10-08: Round 32 decided (user): Lifeline is KevinS (b), renamed `ref-lifeline.wav`, and
  Lifeline Three is Maria Kasper (b), renamed `ref-lifeline-three.wav`; Atul Sharma's and Kehinde's
  candidates (a) deleted with their CREDITS.md rows (their sources and cuts stay in
  `tools/concept/audio/tts_r32.py`).
- 2026-10-08: Round 33 candidates for Atlas Control (Level 11) added (Alister, MaryAnn; licences
  checked on each archive.org item's `licenseurl`: Public Domain Mark 1.0).
- 2026-10-09: Round 33 decided (user): Atlas Control is MaryAnn (b), renamed
  `ref-atlas-control.wav`; Alister's candidate (a) deleted with its CREDITS.md row (its source and
  cut stay in `tools/concept/audio/tts_r33.py`).
- 2026-10-12: Nansen's clip (round 35, picked in round 34's review) added to the table; round 36's candidates for Ares Control added (sources and licences as recorded in CREDITS.md).
- 2026-10-13 (user, round 36, S.6 = b): Ares Control's reference is Thomas A's clip
  (`ref-ares-control-r36-b.wav`, renamed `ref-ares-control.wav` when his lines are rendered in part B);
  David Wales's clip a moved to `../concept/rejected/` with its transcript and CREDITS.md row.
- 2026-10-13 (user, round 37, S.16 = b): Lance Control is Celine Major; her candidate clip
  (`ref-lance-control-r37-b.wav`) renamed `ref-lance-control.wav` (step Q2, which renders the call);
  Ted Lienhart's clip a stays until the round closes.
- 2026-10-13 (user, round 37 closed, step Q3): Ted Lienhart's clip a (`ref-lance-control-r37-a.wav`)
  moved to `../concept/rejected/` with its transcript and CREDITS.md row.
- 2026-10-14 (M7 part B, step AUD-B): Thomas A's clip renamed `ref-ares-control.wav` (and its
  transcript) and **trimmed**: Chatterbox copies a clip's prosody, and his audition's takes held 2 s
  pauses inside two lines, so the lead-in is cut to 0.1 s and the 0.6 s and 1.2 s silences inside the
  reading to 0.3 s (`trim_pauses` in `tools/concept/audio/tts_r36.py`, which `--refs` applies to a
  fresh cut too): 12.0 → 10.3 s, the same words (Whisper), median pitch 118 Hz. The render checks
  every take for a pause as well (`tools/art/voice.py`, `MAX_PAUSE`). Pending the user's review with
  his Level 15 lines (round 38).
- 2026-10-22 (user, round 51, V.1 = a): Dr. Brandt (`conamara`) is Margaret Espaillat; her clip `ref-conamara-r51-a.wav` keeps its `-r51-` name (it is part of her lines' key). Sue Anderson's clip b (`ref-conamara-r51-b.wav/.txt`) moved to `../concept/rejected/` with its CREDITS.md row.
- 2026-10-22 (user, round 52, V.1 = b): Thera Deep Control (`thera-control`) is John Van Stan; his clip `ref-thera-control-r52-b.wav` keeps its `-r52-` name (it is part of his lines' key). Bruce Pirie's clip a (`ref-thera-control-r52-a.wav/.txt`) moved to `../concept/rejected/` with its CREDITS.md row.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): Vorne's reference candidates: **a** (Jeffrey Church) chosen in round 60, V.1 (the production render lowers its pitch toward the brief, see the [voice README](../README.md#decisions)); b and c rejected (the clips stay here as the record).
- 2026-10-28 (step VOX-B, M9 part B; accepted in round 62): `ref-vorne.wav` added, Vorne's production clip (round 60's a pitched −2 semitones, so Chatterbox re-voices him nearer the brief's 105 Hz; the speaker table's `ref`); round 62's candidates for Ceres Hub Control and Harker added (`-r62-a/-b`, sources and licences in CREDITS.md via `briefs/credits-m9-voxb.md`).
- 2026-10-28 (user, round 62, V.1 = a, V.2 = b): Ceres Hub Control is Paul Adams (`ref-ceres-control-r62-a.wav`), Harker is David Clarke (`ref-harker-r62-b.wav`); both keep their `-r62-` names (part of the lines' keys). Phil Chenevert's clip b (`ref-ceres-control-r62-b.wav/.txt`) and Ted Delorme's clip a (`ref-harker-r62-a.wav/.txt`) moved to `../concept/rejected/` with their CREDITS.md rows.
- 2026-10-28 (steps VOX-D and VOX-D2, M9 part D; decided in round 64: b): the Bellwether's two candidate clips `ref-bellwether-r64-a.wav` (Lucy Burgoyne, 134 Hz) and `-b.wav` (Laurie Anne Walden, 181 Hz) added (`tools/concept/audio/tts_r64.py --refs`, CREDITS.md rows); the pick keeps its clip's `-r64-` name (part of the lines' key), the other moves to `concept/rejected/` with its row.
- 2026-10-28 (user, round 64, V.1 = b): the Bellwether is Laurie Anne Walden (`ref-bellwether-r64-b.wav`, keeps its `-r64-` name: part of the lines' key). Lucy Burgoyne's clip a (`ref-bellwether-r64-a.wav/.txt`) moved to `concept/rejected/` (CREDITS.md path updated).
