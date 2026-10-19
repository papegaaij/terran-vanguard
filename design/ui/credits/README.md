---
title: Credits
design: approved
implementation: done
art: final
depends-on: [../../../CREDITS.md]
updated: 2026-10-12
---

# Credits

## Summary

Scrolling credits screen from the main menu (and after the campaign ends). Carries the attributions that the CC-BY licences of third-party assets require.

## Design

- Glass style; a slowly scrolling column over the menu scene; skip/back at any time.
- Sections: team roles, music, sound (with the **CC-BY attributions** — title, author, licence —
  generated from [CREDITS.md](../../../CREDITS.md), skipping rejected files), the fonts with
  their licences (permissive font licences are allowed for fonts, see CLAUDE.md), thanks to the
  CC0 authors, tools.
- The list is generated at build time from CREDITS.md so it can never drift from the files
  actually shipped.

### The roll as built (M4 part H)

- **Generated file.** `./gradlew :pipeline:credits` (`vanguard.pipeline.CreditsList`, also run by
  `importPlaceholders`) writes `assets/ui/credits.txt` from [CREDITS.md](../../../CREDITS.md): one
  `kind|text` entry per line (logo, gap, title, role, name, item, detail, text). It is committed
  like the other generated assets; `CreditsListTest` fails while the committed roll differs from
  what CREDITS.md and the assets give, so a CREDITS.md change cannot ship without it.
- **What counts as shipped**, decided by the row's path: a file under `assets/` that exists, a
  concept file whose copy of the same name is in `assets/`, or a voice reference clip whose speaker
  has rendered lines in `assets/voice/`. A path in a `rejected/` directory never counts; concept
  files the game does not use are left out.
- **What is listed.** Every shipped asset whose licence asks for attribution (CC-BY, the fonts'
  permissive licences; anything that is not CC0 or public domain) as "title" by author, then the
  licence and the source (the link without `https://`), once per asset even when CREDITS.md has
  both the concept and the `assets/` row. The authors of shipped CC0 and public-domain assets are
  thanked by name (sorted, without their notes). Groups by path, in this order: sound effects,
  voices (the LibriVox readers of the reference clips), music, fonts, other.
- **Own credits.** Around the generated groups: logo, "a game by", art and music (the game's own
  generators) before; tools (libGDX and LWJGL, Java 21, Gradle, Construo, Python with NumPy and
  Pillow, FFmpeg, Chatterbox, Claude Code), the Apache-2.0 licence, "made with libGDX" and "thank
  you for playing" after. The texts live in `CreditsList`.
  The Apache-2.0 licence covers the game's own art, music and voices as well as the code (user,
  round 35, Q13); third-party assets keep their own licences, listed in CREDITS.md.
- **Today's roll** (2026-10-12, M6 part D; to be regenerated after round 34): 28 attributed items,
  27 sound-effect attributions (CC-BY 3.0/4.0) and 1 font (DejaVu Sans Mono Bold, Bitstream Vera
  Fonts licence), 70 thanked CC0 sound authors and 18 thanked LibriVox readers (public domain /
  CC0); no third-party music.
- **Screen.** A 560 px glass column centred over the title scene at half brightness, its trim
  running past the screen's top and bottom; the lines are centred, word-wrapped to 520 px in the
  UI kit's fonts and fade over 40 px at the column's ends (y 14–500). The roll enters from the
  bottom at 30 px/s and stops with its last line in the middle. Holding confirm or down (Enter,
  D-pad down, A) runs it 8× faster, holding up runs it back; back (Esc / B) returns to the screen
  below at any time, confirm too once the roll has stopped.

### At the release's end (M6 part B)

The Acts 1–2 release ends after Act 2's outro (Q1 = a, accepted by the user in round 35): the
end card (`ACTS I-II COMPLETE`, the campaign's totals, `TO BE CONTINUED`, `ACT III - RED DUST.
MARS. COMING SOON.`), the name entry when the score makes the difficulty's top 10
([scoring](../../systems/scoring/README.md#high-scores)), then this roll over the end screen with
the title theme playing on, and when it goes back, the main menu. The same rule will bring the roll
after the campaign's final level (Act 7).

## Concept art

Concept [round 08](../../concept-rounds/round-08/README.md) — glass style; generator
`tools/concept/ui_r08.py`. The capture of the built screen was accepted in concept round 26. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/credits-r08-a.png](concept/credits-r08-a.png) | Credits — scrolling glass column with placeholder roles, the CC-BY sound attributions from CREDITS.md and thanks to CC0 authors | chosen |
| [concept/credits-screen-capture-r26-a.png](concept/credits-screen-capture-r26-a.png) | Capture of the built credits screen (xvfb, `--bench`): the roll with the generated CC-BY attributions | chosen |

## Implementation

- [x] Scrolling credits with skip
- [x] CC-BY attributions generated from CREDITS.md for every shipped CC-BY asset
- [x] Reachable from the main menu
- [x] Reachable at the release's end, after Act 2's outro (end card, name entry, credits, main menu) — M6 part B
- [ ] Reachable after the final level — **later: Act 7** (the campaign end)

## Decisions

- 2026-10-01: Screen added for the Acts 1–2 vertical slice (concept round 08).
- 2026-10-01: Concept round 08: accepted.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-02: The credits name the fonts with their licences too (user decision on permissive font licences, CLAUDE.md).
- 2026-10-05: Built in M4 part H (user decision D5 = B; the high-score table goes to M6): the roll
  is generated from CREDITS.md by `:pipeline:credits` into the committed `assets/ui/credits.txt`
  and checked against it by a test; a row counts as shipped by its path, rejected files never.
  The roll stops at its end instead of returning by itself; Esc leaves at any time. Reaching it
  after the final level waits for the campaign end (Act 7).
- 2026-10-05: Concept round 26 closed (user: the round accepted as proposed): the roll accepted as
  built and its capture approved; the own credit lines are kept as proposed, **"A GAME BY Emond
  Papegaaij"** and the tools line **"Written with Claude Code by Anthropic"**, with the art and
  music lines ("made by the game's own generators"); this closes the open question. The screen is
  drawn in the final UI kit over the final title scene, so `art: final`; the roll after the final
  level stays tagged for Act 7, so the document is `done`.
- 2026-10-12: M6 part B (Q1 = a, **pending the user's review in round 35**): the roll plays at the
  release's end (`ReleaseEndScreen`: end card → name entry if the score qualifies → this roll →
  main menu), when an act outro finishes and the content has no next level
  (`CampaignRoute.releaseEnd`; `--act-end` on an earlier level is unaffected). The end card's text
  is new player-facing text for the round. It writes the autosave the hangar would have written,
  so Continue opens the hangar before Level 15.
- 2026-10-12: Back to `design: review` until [concept round 35](../../concept-rounds/round-35/README.md) closes (C4 and Q1, the release's end).
- 2026-10-12 (user, round 35): accepted as built: C4 and Q1 = a (the release's end after Act 2's
  outro: end card, name entry, credits roll, main menu; the end card's text as built) and the build
  choices B.1 (g) and (h). Q13: the game's own art, music and voices are Apache-2.0 like the code,
  as the roll already says. `design: approved`; R.1 (the roll regenerated after round 34's sound
  picks) stays open.
