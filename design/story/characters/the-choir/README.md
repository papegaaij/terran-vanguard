---
title: The Choir
design: approved
implementation: n/a
art: chosen
updated: 2026-10-31
---

# The Choir

## Summary

The collective voice of the Vrell as it bleeds into human radio: layered, whispering, singing.
Not a character in the usual sense — an eerie presence that grows louder, clearer and, by the
end, afraid.

## Design

- **Role**: interrupts the radio at Vrell boss fights and key story moments. Final boss is the
  Choir Heart (see [enemies](../../../enemies/README.md)).
- **Voice**: fragments in lowercase with ellipses; plural "we"; no names for humans ("the
  small ones", later "the small bright one" for Lancer). Audio: many voices in unison, pitched
  and reversed, over a drone.
- **Evolution across the campaign**:
  - Acts 1–2: the Choir only sings; subtitles show only "[the Choir sings]". Dr. Varga
    interprets the song on the radio and in briefings ("*yield*" at L02, "*many*" and "*late*"
    at L07). Those are her readings, never subtitled as the Choir's words.
  - Acts 3–4: first words — "…many… we are many…".
  - Act 5–6: speaks of the door and the "one who opened" (Vorne), with indifference. In Act 5 it
    speaks once, in L33 (*Act 5* below); the act's boss is Vorne's, so the Choir is silent at L35.
    In Act 6 it speaks twice: the turn at Aurelia (L37) and the Ascendant's eruption (L42) (*Act 6* below).
  - Act 7: addresses Lancer; lines turn fearful — "…the silence comes… it is behind us…".
- **Gameplay tie-in**: Choir transmissions always precede a Vrell boss or a big swarm. The
  static in the HUD portrait frame is a visual warning.

### Act 5 (draft)

*Draft, step `m9-d-story` (2026-10-27), **approved in round 60**. The level
document places the line and the reactions to it ([Act 5](../../../campaign/act-5-the-belt/README.md),
Level 33).*

- **L33, the leech field**: the Choir's one Act 5 transmission, before the leech swarm (its usual
  warning role). It is the first time it names Vorne's part, and it does not care: the
  [Void Leeches](../../../enemies/space/void-leech/README.md) in the same level feed on Ascendancy
  craft as readily as on the wing's. The line (draft):
  "*…the one who opened… calls to us… we do not answer… the door is open… we are going…*"
  It echoes Act 3's "*we are going*" and sets up Act 6's "*no longer needed*". It shows the
  alliance as one-sided; Varga's reaction is the level's line.
- No sung warnings are added in Act 5 beyond the level's: the act's enemy voice is Vorne's.

### Act 6 (draft)

*Draft, step `m10-d-story` (2026-10-31), **decided 2026-10-31 (round 72)** (Q3 a, J6). The level and boss
documents place the lines and the reactions to them.*

- **L37, the turn** (Q3 a, t=100 of the [level](../../../campaign/act-6-jovian-storm/level-37-fall-of-aurelia/README.md)):
  its one line of the turn, with its 2 s static lead, before the growth bursts over the decks: the Act 6 sample,
  "*…the one who opened… is no longer needed… the door is ours…*". The alliance ends in one sentence, without anger;
  Vorne's cold order and Varga's "only a door" follow it.
- **L42, the eruption** (J6): the Vrell half breaks through the Ascendant and the Choir speaks once more, right before
  Vorne's shout. It must **not** repeat L37's line (the turn is told; this is the claim on him): it says that the ship,
  and the man in it, are theirs. Draft: "*…the one who opened… is inside us now… he was always inside us…*". It
  answers his "agreement" before he says it, and it seeds Act 7 (Vorne fused with the lifeboat, L48) without naming
  anything. The [Ascendant](../../../enemies/bosses/ascendant/README.md#vorne)'s line 4 and Level 42 speak it.
- **Silent elsewhere**: no line at L36, L38–L41. Above all **nothing of the Silence**: the Choir's fear is Act 7's
  ("*…the silence comes…*"); in Act 6 the Silence is named only by Varga, from Vorne's files (L41). Its indifference
  holds to the end of the act: it does not taunt and it does not explain.
- **The hybrids are its own**: the Vrell never hunt the hybrids after the turn (the
  [three-way war](../../../enemies/README.md#the-three-way-war)); in the story that is because the tissue in them is
  the Vrell's, not the Ascendancy's.

### Sample lines

| Situation | Line |
|---|---|
| Act 1 boss | "[the Choir sings]" |
| Act 3 | "…many… we are many… we are going…" |
| Act 5 (L33) | "…the one who opened… calls to us… we do not answer… the door is open… we are going…" |
| Act 6 (L37, the turn) | "…the one who opened… is no longer needed… the door is ours…" |
| Act 6 (L42, the eruption) | "…the one who opened… is inside us now… he was always inside us…" |
| Act 7 | "…small bright one… why do you stand in the door…" |
| Final | "…it is behind us… it has always been behind us…" |

### Portrait brief

No face. A square frame of teal and violet bioluminescent patterns pulsing to the voice, like
a sound waveform made of living tissue; in Act 7 the pattern briefly forms something
face-like, then scatters. Heavy static and interference, late-90s pre-rendered CGI style.

## Concept art

Concept [round 08](../../../concept-rounds/round-08/README.md) — style-B portrait; generator `tools/concept/portraits_r08.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/portrait-r08-a.png](concept/portrait-r08-a.png) | The Choir — alien interference glyph (five-armed violet/teal sigil in rippling rings) at briefing and HUD sizes, loop strip | chosen |
| [concept/portrait-r08-a.gif](concept/portrait-r08-a.gif) | The Choir — pulsing, turning glyph loop for the radio panel | chosen |

## Open questions

- None open.

## Decisions

- 2026-09-30: The Choir as a faceless, evolving voice; frightened in Act 7.
- 2026-10-01: Concept round 08: portraits accepted.
- 2026-10-01: Choir in Acts 1–2 confirmed by the user: it only sings ("[the Choir sings]") and Varga interprets it; the Choir, story and act documents agree.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../../reviews/acts-1-2/README.md).
- 2026-10-27 (Act 5 design phase, step m9-d-story; **approved in round 60**): the Choir
  speaks once in Act 5, in L33 before the leech swarm, of "the one who opened" with indifference; silent
  at L35 (the Ascendancy's boss).
- 2026-10-31 (Act 6 design phase, step m10-d-story; **decided 2026-10-31 (round 72)**): the Choir speaks
  twice in Act 6: the turn at L37 (the Act 6 sample, with "the door is ours") and the eruption at L42, a new line
  that claims Vorne ("…is inside us now…") before his shout; silent elsewhere, nothing of the Silence before Act 7;
  the hybrids are its tissue, so it never hunts them.
- 2026-10-31, design phase (step m10-d-act; decided 2026-10-31 (round 72)): the L42 line is written into the Ascendant's line 4
  and Level 42's radio.
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): Q3 a with c's backdrop (the L37
  line announces a hunt that is played) and the L42 line as drafted: Level 37 keeps the Act 6 sample and Level 42 has
  its own line, so the Choir never repeats itself.
