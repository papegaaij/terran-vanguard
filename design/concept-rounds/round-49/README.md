---
title: Concept round 49 — the dome hit, third try
design: review
implementation: n/a
art: chosen
depends-on: [../../audio/sfx, ../round-47]
updated: 2026-10-22
---

# Concept round 49 — the dome hit, third try

## Summary

One sound is left from round 47's redos: the Level 17 dome hit, rejected twice (rounds 46 and 47). Below is where and when it plays,
what it should sound like, your two earlier verdicts with the rejected files to play for reference, and three new variants
**a**, **b** and **c**, each described by how it sounds, next to the **old** file the game plays now. Answer: a, b, c, or redo (say why).
Nothing is in `assets/` yet. Round 48 is the Act 4 round on another branch, so the rounds index here jumps from 47 to 49.

## Review

The review page is built from this file by `build/round49-tools/build_review.py` (untracked); the proposals are in
[the sfx part](../../audio/sfx/README.md#concept-art) and the generator is `tools/concept/audio/sfx_r49m.py`.

Record of the decisions: [review.html](review.html)

## Sounds: the dome hit

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| M.1 | [dome-hit](../../audio/sfx/README.md#concept-art) | **Dome hit (Level 17), third try.** It plays at every enemy shot that hits one of Level 17's colony greenhouse domes (`Sfx.DOME_HIT`): often, several in a row, so it is at most 1.2 s and must not tire. The dome breaking is a separate sound (no shatter here). It should sound like a glass dome tens of metres across, the size of a stadium roof, being hit: both sharp and huge, a hard bright strike and then the big structure answering (a long low glassy resonance or boom, cracks running away across a large surface like a frozen lake's ice), heard from a distance. Your verdicts so far: round 46 (two thuds, rejected, playable below): "Neither sound like glass being hit. Glass being hit is a sharp sound, not a thud." Round 47 (a sharp hit on a pane plus a glass crack; a tapped window plus a cooling-glass crack, both rejected, playable below): "These are too small. a is a bottle cracking, b is nothing like glass breaking. A dome is a very large structure." So each variant below is built to be both sharp and huge. **a**: a steel hammer striking a big glass pane on a wall (loud and bright, not breaking) and, right under it, a frozen lake's ice cracking: one deep crack, then a run of smaller cracks racing away across the sheet, each with the ice's falling 'zing', slowed to 0.8 so the sheet sounds bigger. Sharp, glassy and wide; the brightest of the three, 1.2 s (CC0). **b**: the same hammer on a glass pane, slowed to 0.85 (heavier), over a struck glass table's ringing (a contact-mic recording) slowed to 0.5, an octave down, so a large glass body hums on after the hit; both played into a real cathedral (convolved with its recorded impulse response), so it rings out in a huge space as if heard from far below. A bright 'clank' with a long low glassy 'hmmm'; no ice, the most 'glass in a big hall', 1.2 s (CC0, CC-BY 4.0). **c**: a rock slammed onto a frozen lake (a hard icy crack with the ice's 'zing') on a deep crack and boom of the lake's thick ice slowed to 0.85, and from 0.25 s a tall steel door's low groan under it, quiet, like the dome's frame taking the blow. Harder and darker than a, no glass pane; the most 'huge' (most of its energy is low), the least bright, 1.2 s (CC0, CC-BY 4.0). No recommendation (I cannot listen). | [old (replaced in the game)](../../audio/sfx/concept/dome-hit-r40-a.ogg), [a](../../audio/sfx/concept/rejected/dome-hit-r49-a.ogg), [b](../../audio/sfx/concept/dome-hit-r49-b.ogg), [c](../../audio/sfx/concept/rejected/dome-hit-r49-c.ogg), [rejected r46 a](../../audio/sfx/concept/rejected/dome-hit-r46-a.ogg), [rejected r46 b](../../audio/sfx/concept/rejected/dome-hit-r46-b.ogg), [rejected r47 a](../../audio/sfx/concept/rejected/dome-hit-r47-a.ogg), [rejected r47 b](../../audio/sfx/concept/rejected/dome-hit-r47-b.ogg) | b uses a real recorded cathedral reverb (a long tail); c is the darkest and may read as a thud, the thing round 46 rejected; the proposals are cut from Freesound's free previews, a pick is rebuilt from the original | **b** picked | closed: **b** picked |

## Decisions

- 2026-10-22: round opened: the dome hit, third try (three variants a, b, c from Freesound's HQ previews; round 46's and round 47's variants rejected).
- 2026-10-22 (user, closed; M.1): dome hit **b** (the hammer strike over the glass ring in the cathedral reverb; Freesound 155619, 636555 and the impulse response 648945). a and c rejected (moved to `concept/rejected/` in the sfx part); it replaces round 40's synthesized dome hit. The pick is still cut from the free previews: the originals wait for the download limit, then it is rebuilt with `tools/concept/audio/sfx_r49m.py` and imported into `assets/`.
