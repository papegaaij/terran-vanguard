---
title: Chairman Silas Vorne
design: approved
implementation: n/a
art: final
updated: 2026-10-28
---

# Chairman Silas Vorne

## Summary

CEO of Helix Dynamics and Chairman of the Jovian Ascendancy. The man who opened the Tether
Gate. A theatrical, silver-tongued pulp villain who believes he is saving humanity from its own
mediocrity.

## Design

- **Role**: the human villain. Silent through Act 4's reveal; from his broadcast that opens Act 5 on
  he interrupts the radio with transmissions, especially at boss fights in Acts 5–7. Escapes the wreck of his flagship
  Ascendant at the end of Act 6 (L42) and dies in Act 7 (L48), absorbed by the Choir.
- **Personality**: charming, grandiose, patient, utterly convinced. Treats the war as a
  business transaction. Genuinely admires skill — and wants to own it.
- **Voice**: long, polished sentences, rhetorical flourishes, the occasional quote. Never
  shouts — until the very end of Act 6. The designated pulp villain: allowed to be
  over-the-top.
- **Background**: 60s but looks 40 (Vrell biotech treatments — a detail Varga notices in
  Act 5: he was sixty when she left Helix in 2179). Built Helix on Jovian helium-3. Funded the 2179 gate survey and made first contact in
  secret.
- **Arc**:
  - Act 1–3: a name in political news snippets during briefings.
  - Act 4 end: the reveal is the cruisers' (L28); Vorne says nothing yet.
  - Act 5: opens with his system-wide broadcast declaring the Ascendancy's "partnership" (the act
    intro); taunts at the Iron Sovereign and tries to recruit Lancer after it falls (see *Act 5* below).
  - Act 6: the Vrell betray him; he fights on, desperate, from the Ascendant. When it breaks
    apart he flees toward the gate in a Vrell lifeboat, still believing the Choir will honour
    the deal (L42).
  - Act 7: the Choir has fused him with the lifeboat: [Vorne's Chimera](../../../enemies/bosses/README.md),
    the L48 mid-boss. Death: his last transmission breaks off mid-sentence.

### Act 5 (draft)

*Draft, step `m9-d-story` (2026-10-27), **approved in round 60**. The beats are kept
here; the texts belong to the [act's](../../../campaign/act-5-the-belt/README.md) intro and outro and to the
level documents' radio.*

- **The broadcast** (the Act 5 intro, Q9 a): pages 2–3 of the act intro are Vorne on every channel,
  *intercepted* (amber, the steady interference: [briefing](../../../ui/briefing/README.md#intercepted-transmissions-draft));
  page 1 is Okafor's "all channels — he is on every one", page 4 her answer and the order to Ceres.
  Images: Vorne at a gilded lectern under the Ascendancy crest; Ceres Hub's lights turning red. What he
  says: he names himself and the Ascendancy, declares its independence and its partnership with the
  Vrell in the open ("I merely opened the door"), and invites the belt's stations to choose the side
  that will still exist. Courteous, patient, never a raised voice; he never mentions the cruisers or
  the hull code (Act 4 told them), and he never says why the Vrell wanted the door (Act 6, L41).
- **L29**: Ceres Hub chooses him. He is not on the radio in the level; the Ascendancy's voice there is
  the mutineers' flight lead ([characters](../README.md#act-5-speakers-draft)).
- **L35, the Iron Sovereign**: his station, so his fight. He announces it on the enemy channel as the
  Choir announces a Vrell boss, taunts two or three times, admiring and amused, and when the core opens
  his portrait turns to **rage** while the line stays short and cold: he still never shouts (that is
  Act 6's end). After the Sovereign falls he calls Lancer by name and names a price (the recruitment
  line below), the act's last enemy transmission; the captured data Varga reads next points to Callisto
  too. The order and the draft lines are the [boss's](../../../enemies/bosses/iron-sovereign/README.md#vorne).
- **The act outro**: Varga sees his face on the recruitment feed: "he looks forty; he was sixty when I
  left Helix" ([Varga](../varga/README.md)). The cures the L31 refugees describe are on him.
- **Presentation** (Act 5 on): the enemy channel on the radio ([HUD](../../../ui/hud/README.md)); his
  production portraits (radio 72 px and briefing 144 px in neutral, smug and rage, from the chosen
  r04 style) are proposed in round 60, not Act 6 as planned in 2026-10-02; his voice is cast from a
  CC0/public-domain reading, an older man's polished low voice that is no main-cast reader's
  ([voice](../../../audio/voice/README.md)); his motif plays under the broadcast
  ([music](../../../audio/music/README.md)).

### Sample lines

| Situation | Line |
|---|---|
| Reveal broadcast | "Citizens of the solar system. The old order is ending. I merely opened the door." |
| Act 5 broadcast | "No Ascendancy city has been touched. Ask yourselves why — then ask who you would rather stand beside." |
| Boss fight | "Such dedication. Such waste. You could have been on the winning side, pilot." |
| Recruitment | "Lancer. You are wasted on Okafor. Come to Callisto. Name your price." |
| Betrayed (Act 6–7) | "They were supposed to… no. No! We had an *agreement*!" |
| Final (L48) | "Do you know what's coming after them, pilot? Do you have any idea what I was trying to—" |

### Portrait brief

Tall, lean man, looks forty but with old eyes; slicked-back silver hair, sharp cheekbones,
neatly trimmed beard. High-collared black suit with gold embroidery and the Ascendancy crest (a
rising sun over a ringed planet) on the lapel. A faint teal shimmer under the skin at his
temple (Vrell biotech). Neutral: faint smile; alternates: smug (head tilted, one eyebrow up),
rage (bared teeth, static-distorted). Warm golden rim light against black, scanline overlay,
late-90s pre-rendered CGI style.

## Concept art

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

Round 60 (Act 5 design phase, step ART-P-world; chosen in round 60): his production portraits in the chosen
r04 style, rendered by `tools/concept/belt_r60.py` through `tools/art/portraits.py`'s style-B pipeline (imported
unchanged; nothing written to `assets/` until chosen), a/b by the background behind him.

Concept round 03 portrait styles. Prompts for polished portraits:
[concept/prompts.md](concept/prompts.md). Generated by `tools/concept/portraits_r03.py`; the
procedural faces only show the style (framing, lighting, colour treatment, frame and overlay).

| File | What | Status |
|---|---|---|
| [concept/rejected/portrait-r03-a.png](concept/rejected/portrait-r03-a.png) | Style A "pre-rendered 3D bust": Vorne at 144×144 (briefing), 72×72 in the HUD A radio frame, 3× enlargement | rejected — style B preferred |
| [concept/portrait-r03-b.png](concept/portrait-r03-b.png) | Style B "comm-screen pixel portrait": Vorne as a 72 px dithered amber enemy-channel portrait, 2× for the briefing, HUD radio frame | superseded by r04 |
| [concept/portrait-r04-a.png](concept/portrait-r04-a.png) | Round 04: style B with retained colour — Vorne's own colours under the amber enemy-channel tint, 36 colours, soft dither; briefing 144×144, HUD 72×72, 3× (`tools/concept/portraits_r04.py`) | chosen |
| [concept/vorne-portraits-r60-a.png](concept/vorne-portraits-r60-a.png) | Round 60, production portraits A — the enemy screen: radio 72 px and briefing 144 px in neutral, smug (head back, one brow up) and rage (cold: brows down, teeth bared and clenched, the screen tearing with static), one palette, on the plain amber screen | chosen (round 60) |

Production art, M9 part B (concept round 62, step ART-B): round 60's chosen A.20 a (the plain amber enemy screen) written as production art by `tools/art/portraits.py vorne`: `radio-vorne-<neutral|smug|rage>` (72 px) and `briefing-vorne-<…>` (144 px), one palette over the three. Prompts: [concept/prompts.md](concept/prompts.md#round-62--production-vornes-portraits-m9-part-b).

| File | What | Status |
|---|---|---|
| [concept/vorne-portraits-final-r62-a.png](concept/vorne-portraits-final-r62-a.png) | Review sheet (`tools/art/portraits.py`, M9 part B): Vorne neutral, smug and rage at briefing size and at radio size 1× and 3× | chosen (round 62, F.8: accepted as final) |

## Open questions

- Round 60 Q9 (how the broadcast is shown): recommended (a), the act intro is the broadcast (above);
  the alternatives are in the [story](../../README.md#open-questions).
- Round 60 Q7 (the Iron Sovereign's fight) decides the phases his L35 lines follow; the
  [boss](../../../enemies/bosses/iron-sovereign/README.md) owns it.

## Decisions

- 2026-09-30: Vorne as pulp villain, revealed end of Act 4.
- 2026-09-30: Vorne survives Act 6 and dies in Act 7 (L48, Vorne's Chimera), matching the
  campaign: his escape is what the UTC fleet follows through the gate.
- 2026-09-30: Concept round 03: portrait style **B** (comm-screen pixel portrait) chosen, with a bit more colour retained so it looks less flat.
- 2026-09-30: Concept round 04: style-B portrait with retained colour chosen.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../../reviews/acts-1-2/README.md).
- 2026-10-27 (Act 5 design phase, step m9-d-story; **approved in round 60**): his Act 5
  beats (the broadcast as the act intro, Q9 a; silent in L29; the Sovereign's taunts with a cold rage in
  the core phase; the recruitment; Varga's look at his face in the outro); the Arc's "Act 4 end:
  broadcast" corrected to the 2026-09-30 story decision (the broadcast opens Act 5); his production
  portraits and voice come with Act 5 instead of Act 6.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the Act 5 beats approved as drafted (J.8: cold, never shouting). **A.20 a**: the production portraits are the plain amber enemy screen (b, the Chairman's office with the crest, rejected); the rage face's faint teeth at 72 px are re-checked in production. His voice is V.1 **a** (Jeffrey Church; see the [voice](../../../audio/voice/README.md#act-5-voices-draft-round-60)).
- 2026-10-28 (user, [round 62](../../../concept-rounds/round-62/README.md), closed; page answers): Vorne's three portraits (neutral, smug, rage; F.8) accepted as final; `art: final`. His pitch is V.3 a (the reference clip lowered 2 semitones, about 90 Hz), already the production render.
