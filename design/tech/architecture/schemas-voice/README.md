---
title: Data file schemas — voice
design: approved
implementation: in-progress
art: n/a
updated: 2026-10-29
---

# Data file schemas — voice

## Summary

The schema of the voice file: Chatterbox's expression settings and the speakers' reference clips. Moved unchanged from the [architecture](../README.md#data-file-schemas)'s Data file schemas, which keep the shared conventions (keys, units, sizes) and the index of every schema.

## Design

- **Voice** (`audio/voice/data.yaml`): `expressions` (`neutral`, `grim`, `fierce`, M9 part H's `smug` and `rage`
  (Vorne's: the neutral and the grim rows' numbers) and `shout`,
  each `exaggeration`, `cfg_weight`, `temperature`: Chatterbox's settings) and `speakers` by voice
  slug: `names` (the speakers as the level data writes them), `ref` (the clip in
  `design/audio/voice/refs/`), optional `shift` (added to the expression's row), `fixed` (settings
  for every line instead), `layering` (`choir`), `pins` (a line's seed by its key) and `filter`
  (`pa`: the public-address filter instead of the radio filter, part of the key; M9 part A `enemy`: the enemy
  channel's filter, the speaker's lines and pages on the enemy channel, `VoiceData.channelOf`) and `stage` (the
  sound file in `assets/voice/<voice>/` a radio line plays when it is only a stage direction,
  `[the Choir sings]`). The line list and keys: `vanguard.content.voice.VoiceLines`; the files
  `assets/voice/<voice>/<key>.ogg`. M7 part B (round 36, Q.24f): the optional top-level **`respell`**
  (a word or name → its spelling as spoken, `Ares: Airees`): applied to the text as spoken of every
  line (whole words, case-sensitive, the longest first; `VoiceData.respelled`), so part of the key; the
  subtitle keeps the real spelling.
