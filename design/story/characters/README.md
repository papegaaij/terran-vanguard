---
title: Characters
design: approved
implementation: n/a
art: final
updated: 2026-10-31
---

# Characters

## Summary

The cast the player meets in briefings, radio chatter and enemy transmissions. Small on
purpose: six voices, each instantly recognisable by portrait and speech style.

## Contents

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [lancer](lancer/README.md) | The player: silent CDF pilot of the AF-12 Stormhawk | approved | n/a | n/a |
| [okafor](okafor/README.md) | Commander Adaeze Okafor, Aegis Wing CO, gives the briefings | approved | n/a | chosen |
| [rook](rook/README.md) | Lt. Kenji "Rook" Tanaka, the AI wingman, banter and warnings | approved | n/a | chosen |
| [varga](varga/README.md) | Dr. Elena Varga, intel officer and xenobiologist, hangar intel | approved | n/a | chosen |
| [vorne](vorne/README.md) | Chairman Silas Vorne, leader of the Ascendancy, the villain | approved | n/a | final |
| [the-choir](the-choir/README.md) | The collective voice of the Vrell | approved | n/a | chosen |

## Design

### Portrait rules (for all characters)

- Portraits are **pre-rendered 3D busts**, late-90s CGI style (think Wing Commander IV /
  Colony Wars briefing portraits), shown in a square frame in the HUD side panel and larger on
  briefing screens.
- Head-and-shoulders, three-quarter view facing the play field (left panel faces right, right
  panel faces left; mirror if needed).
- Each main character has a neutral portrait plus the expressions **grim** and **fierce**
  ([briefing](../../ui/briefing/README.md)); minor speakers (the generic officer and civilian, the
  Choir's glyph) have the neutral one. A briefing page or radio line names its expression in the
  data (`expression`, neutral when not given).
- Frame colour identifies the side: CDF navy/blue, Ascendancy black/gold, Vrell teal static.
- Enemy transmissions add scanlines and static distortion.

### Who speaks when

| Situation | Speaker |
|---|---|
| Act and mission briefings | Okafor (Varga for intel-heavy missions; Vorne's broadcast, intercepted, in the Act 5 intro; Vorne's intercepted invitation on the Act 6 intro's page 3) |
| Hangar intel panel | Varga |
| Wave warnings, banter, "on your six" | Rook |
| Low armour, objective changes | Okafor or Rook |
| Boss appears | Varga (analysis), Vorne or the Choir (transmission) |
| Enemy broadcasts | Vorne, the Choir; from Act 5 other Ascendancy voices on the enemy channel (the mutineers' flight lead, L29; the Honour Guard's three aces, L40) |

### Act 5 speakers (draft)

*Draft, step `m9-d-story` (2026-10-27), **approved in round 60**. The lines are the level
documents' ([Act 5](../../campaign/act-5-the-belt/README.md)); the voices are cast in
[voice](../../audio/voice/README.md); the enemy channel's dress is the [HUD](../../ui/hud/README.md)'s and the
[briefing](../../ui/briefing/README.md#intercepted-transmissions-draft)'s.*

| Speaker | Channel | Where | Who | Portrait |
|---|---|---|---|---|
| [Vorne](vorne/README.md) | enemy (amber, intercepted) | Act 5 intro pages 2–3, L35 | The Chairman, on every channel | his chosen r04 style; production portraits (neutral, smug, rage; 72 and 144 px) proposed in round 60 |
| Ceres Hub Control | friendly (teal) | L29, L35 | A loyalist dock controller who keeps Ceres Hub's dock channel open while the hub turns; points the wing to Dock 9 ([Rook](rook/README.md#act-5-captivity-and-return-draft)) and guides the L35 approach | own radio portrait, as Ares Control's and Thera Deep Control's (Level 29's part) |
| The tug's captain, call sign **Bellwether** | friendly (teal) | L31 | A belt miner in her fifties hauling loyalist refugees in the mining tug *Bellwether*; tells how Helix bought the stations with Vrell cures | own radio portrait (Level 31's part) |
| **Harker**, the mutineers' flight lead | enemy (amber, intercepted) | L29 | A Ceres garrison pilot who turned with the hub; Rook flew convoys with him | own radio portrait: a CDF flight suit with the Ascendancy crest stitched over the patch (Level 29's part) |

Ceres Hub's own voice after the hub declares is Vorne's broadcast; the controller who stays on the radio
is a loyalist, so he keeps the friendly channel.

**Act 5's names** (the level drafts' placeholders, adopted as the story's names, draft decided in round 60): the
speakers **Harker** (L29) and the **Bellwether** (the tug and its captain's call sign, L31); the places **Bright
Seam** (the mining rock whose dock the tug leaves, L31), **Dacosta**'s claim (an old prospector's claim marker, L31's
secret), the **Vesta relay** (the tug's destination, L31) and **Kleopatra** (the old consortium mine hollowed into
the Ascendancy's Deep Core refinery, L32). Their pronunciation is in [voice](../../audio/voice/README.md#act-5-voices-draft-round-60).

### Act 6 speakers (draft)

*Draft, step `m10-d-story` (2026-10-31), **decided 2026-10-31 (round 72)** (J4, J6, J10). The lines are the
level and boss documents' ([Act 6](../../campaign/act-6-jovian-storm/README.md); the aces' fight lines the
[Honour Guard](../../enemies/bosses/honour-guard/README.md#lines)'s, Vorne's Level 42 lines the
[Ascendant](../../enemies/bosses/ascendant/README.md#vorne)'s); the voices are cast in
[voice](../../audio/voice/README.md#act-6-voices-draft); the enemy channel's dress is Act 5's
([HUD](../../ui/hud/README.md), [briefing](../../ui/briefing/README.md#intercepted-transmissions-draft)), the aces'
tags on the mid-boss bar the HUD's [Act 6 additions](../../ui/hud/README.md#act-6-additions-draft).*

| Speaker | Channel | Where | Who | Portrait |
|---|---|---|---|---|
| [Vorne](vorne/README.md) | enemy (amber, intercepted) | Act 6 intro page 3, L37, L39, L42 | The Chairman, losing his partners and his capital and still sure of the deal ([his Act 6](vorne/README.md#act-6-draft)) | his final neutral, smug and rage portraits; **rage** for his one shout (L42) |
| [The Choir](the-choir/README.md) | its own (distorted, a 2 s static lead) | L37 (the turn), L42 (the eruption) | The Vrell's voice, indifferent to the one who opened ([its Act 6](the-choir/README.md#act-6-draft)) | its chosen glyph loop |
| **Aurelia Traffic** | friendly (teal) | L37 | J4, the act's friendly generic voice: Aurelia's civil traffic controller, a woman in her thirties, born on the city; she was a girl during the 2166 riots. She breaks with Vorne to get the civilians out on the Halcyon shuttles; steady at first, frayed at the end, and she signs off as the lift rings fail (whether she got out is never said) | own radio portrait, as Ceres Hub Control's: Aurelia's white-and-gold civil traffic uniform with a headset, the tower's windows on the gold domes behind her (proposed in round 72, made in Level 37's part) |
| **Harker**, the Honour Guard's leader | enemy (amber, intercepted) | L40 | The Ceres mutineers' flight lead of L29 (Rook flew convoys with him). After Ceres he went to Callisto with what was left of the belt's turned pilots, and Vorne, who buys the skill he cannot recruit, took him into his Honour Guard: a former CDF pilot leading the Chairman's own. He opens the duel and has unfinished business with Rook ([Rook](rook/README.md#act-6-draft)) | his final L29 face in the guard's gloss black and gold flight suit, the crest on the collar instead of stitched over a CDF patch (a variant of his final portrait, round 72) |
| **Vesper**, the dancer | enemy (amber, intercepted) | L40 | A woman in her late twenties, an Aurelia-born cloud racer before the war and a believer in the Ascendancy; flies for the show of it, calls Lancer "the quiet one" | own radio portrait: a black flight helmet with white ribbon stripes (her Talon's mark), a gold visor pushed up (round 72) |
| **Kestrel**, the feinter | enemy (amber, intercepted) | L40 | A man in his twenties, a Helix test pilot who flew the Talon's trials; quick, cocky, the one who comes from behind | own radio portrait: black and gold chevrons on the helmet, a red band (his Talon's tail mark) (round 72) |

**Okafor** speaks from the bridge of the CDF carrier ***Steadfast***, the flagship of the fleet at Jupiter (J10), on the
friendly channel as ever; her portraits do not change. **Rook** flies the whole act (no absence) and **Varga** speaks
from the *Steadfast* too. The aces have one channel and three voices: Harker's is his L29 voice (cast in round 62), so
two new voices are cast (J4). In Act 6 they speak only in L40, nothing before the duel: their entry tells and
lines introduce them.

**Act 6's names** (the level drafts' placeholders and J4's call signs, adopted as the story's names, draft pending
round 72): the speakers **Aurelia Traffic** (L37) and the aces **Harker**, **Vesper** and **Kestrel** (L40); the
carrier ***Steadfast*** (new here, J10); the shuttles **Halcyon One to Six** and Aurelia's **Spire Plaza** (L37);
**Under Aurelia** and **the Red Spot's Rim** (L38's sections, the storm layer's two stretches); the **Tvashtar
Plains** and the **Loki**, **Pele** and **Prometheus** works (L39, after Io's real volcanoes). Their pronunciation is
in [voice](../../audio/voice/README.md#act-6-voices-draft).

## Concept art

Concept round 03 portrait-style lineups (per-character sheets are in each character's
directory). Prompts: [concept/prompts.md](concept/prompts.md). Generated by
`tools/concept/portraits_r03.py`.

| File | What | Status |
|---|---|---|
| [concept/rejected/cast-r03-a.png](concept/rejected/cast-r03-a.png) | Style A lineup: Okafor, Rook, Varga, Vorne as pre-rendered 3D busts with name plates, plus 72×72 HUD versions | rejected — style B preferred |
| [concept/cast-r03-b.png](concept/cast-r03-b.png) | Style B lineup: the same four as comm-screen pixel portraits (teal for the CDF, amber for Vorne) | superseded by r04 |
| [concept/cast-r04-a.png](concept/cast-r04-a.png) | Round 04: style B with retained colour — all four with name plates plus 72×72 HUD versions (`tools/concept/portraits_r04.py`) | chosen |
| [concept/act5-speakers-r60-a.png](concept/act5-speakers-r60-a.png) | Round 60 (Act 5, step ART-P-world; chosen in round 60): the three new radio speakers at 72 px in style B (`tools/concept/belt_r60.py speakers`, through `tools/art/portraits.py`'s pipeline): Ceres Hub Control and the *Bellwether*'s captain (teal), Harker (amber, the Ascendancy crest stitched over his CDF patch); 1×, 3× and a line-up with the production speakers | chosen (round 60) |

Concept [round 08](../../concept-rounds/round-08/README.md) — generic speakers in style B; generator `tools/concept/portraits_r08.py`.

| File | What | Status |
|---|---|---|
| [concept/generic-cdf-r08-a.png](concept/generic-cdf-r08-a.png) | Generic CDF officer (beret, boom mic, grey-green jacket) for convoy, relay and control speakers | chosen |
| [concept/generic-civilian-r08-a.png](concept/generic-civilian-r08-a.png) | Generic civilian (padded jacket, scarf, handheld radio) for shuttle and evacuation speakers | chosen |

Production art, UI batch part U3 (for concept [round 13](../../concept-rounds/round-13/README.md)): every speaker of Acts 1–2, rendered by [tools/art/portraits.py](../../../tools/art/README.md) into `assets/sprites/portraits/`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/portraits-final-r13-a.png](concept/portraits-final-r13-a.png) | Review sheet: Okafor, Rook and Varga in neutral, grim and fierce (144×144 briefing portraits for Okafor and Varga, 72×72 radio portraits at 1× and 2×, one 36-colour palette per character), the generic CDF officer and civilian (neutral, 72×72), 8 of the Choir's 32 loop frames | chosen |
| [concept/portraits-final-r13-a.gif](concept/portraits-final-r13-a.gif) | The three radio portraits cycling neutral → grim → fierce beside the Choir's loop at 12 fps | chosen |
| [concept/portraits-capture-final-r13-a.png](concept/portraits-capture-final-r13-a.png) | Game capture: Level 01 at 22 s, Okafor's fierce radio portrait with "Contacts inbound. Weapons free." | chosen |

Concept [round 36](../../concept-rounds/round-36/README.md) (Act 3 "Red Dust", overnight design
phase) — the generic colonial speaker in style B, Level 15's `radio-generic-colonial`: the radio
portrait of Ares Control (Levels 15–17 and 19–21), on the teal friendly channel like the CDF officer;
generator `tools/concept/portraits_r36.py`, prompts in [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/rejected/generic-colonial-r36-a.png](concept/rejected/generic-colonial-r36-a.png) | Generic colonial A, the old controller — older colonist in a dust-orange militia cap with a boom mic, rust clothes (72×72 at 1× and 2×) | rejected — round 36: b (dust-suit controller) picked |
| [concept/generic-colonial-r36-b.png](concept/generic-colonial-r36-b.png) | Generic colonial B, the dust-suit controller — the same speaker as a surface worker, goggles on the brow and a respirator | chosen (round 36, C.1) |
| [concept/portraits-final-r38-a.png](concept/portraits-final-r38-a.png) | Review sheet (`tools/art/portraits.py`, M7 part B): the colonial generic radio portrait `radio-generic-colonial-neutral` (Ares Control, from the chosen dust-suit look b), final art | chosen (round 38, F.6: approved as final) |
| [concept/portraits-final-r51-a.png](concept/portraits-final-r51-a.png) | Review sheet (`tools/art/portraits.py`, M8 part B): Dr. Ilse Brandt's radio portrait `radio-conamara-neutral` (Conamara Station's director: dark ash bob, red polar parka, headset, the sodium-lit dome control room behind her) | chosen (round 51, F.5: accepted as final) |
| [concept/portraits-final-r52-a.png](concept/portraits-final-r52-a.png) | Review sheet (`tools/art/portraits.py`, M8 part C): Thera Deep Control's radio portrait `radio-thera-deep-neutral` (the harbour controller: olive skin, short dark hair, a close beard, the grey-blue harbour coverall with a pressure suit's neck ring, the control room's viewport on the lit farms behind him) | chosen (round 52, F.8: accepted as final) |

Production art, M9 part B (concept round 62, step ART-B): the Act 5 radio speakers accepted in round 60 (A.21), written as production art by `tools/art/portraits.py ceres-control harker` (round 60's `tools/concept/belt_r60.py` busts through the same pipeline). Prompts: [concept/prompts.md](concept/prompts.md#round-62--production-the-act-5-radio-speakers-m9-part-b).

| File | What | Status |
|---|---|---|
| [concept/portraits-final-r62-a.png](concept/portraits-final-r62-a.png) | Review sheet (`tools/art/portraits.py`, M9 part B): `radio-ceres-control-neutral` (Ceres Hub Control, teal) and `radio-harker-neutral` (Harker, amber, the crest over his CDF patch) at 1×, 2×, 4× and beside the other minor speakers and Vorne | chosen (round 62, F.8: accepted as final) |
| [concept/harker-capture-r62-a.png](concept/harker-capture-r62-a.png) | Game capture, Level 29 (step B5): Harker's t=70.5 line on the enemy channel | chosen (round 62, F.8: the capture accepted) |
| [concept/portraits-final-r64-a.png](concept/portraits-final-r64-a.png) | Review sheet (`tools/art/portraits.py`, M9 part D): `radio-bellwether-neutral`, the *Bellwether*'s captain (teal; round 60 A.21's accepted bust) at 1×, 2× and 4× and at 1× beside the other radio speakers | chosen (round 64, F.9: accepted as final) |

Concept round 72 (Act 6 design phase, step ART-R-B; **decided 2026-10-31 (round 72)**): the four new Act 6
radio speakers ([Act 6 speakers](#act-6-speakers-draft)) at 72 px in style B, neutral only (`tools/concept/act6_r72.py
speakers`, through `tools/art/portraits.py`'s pipeline). Prompts:
[concept/prompts.md](concept/prompts.md#act6-speakers-r72-a).

| File | What | Status |
|---|---|---|
| [concept/act6-speakers-r72-a.png](concept/act6-speakers-r72-a.png) | A, the plain channel screens: Harker in the guard's black and gold suit (his final L29 face, the crest on the collar), Vesper (black helmet, white ribbon stripes, gold visor up), Kestrel (black and gold chevrons, a red band) on the amber enemy channel, Aurelia Traffic (white-and-gold civil uniform, headset) on the teal channel; 1×, 3× and beside the production speakers | chosen (round 72 A.15 a) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Decisions

- 2026-09-30: Six-character cast; pre-rendered 3D bust portraits with side-coded frames.
- 2026-09-30: Concept round 03: portrait style **B** chosen for the whole cast, with a small bit more colour retained so it looks less flat.
- 2026-09-30: Concept round 04: style-B portraits with retained colour chosen for the whole cast.
- 2026-10-01: Concept round 08: portraits accepted.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-02: Production art, UI batch part U3: `tools/art/portraits.py` renders the speakers of Acts 1–2 in the chosen style B with retained colour: Okafor, Rook and Varga in neutral, grim and fierce (the concept busts with the expression's brows, lids, mouth and head pitch; one 36-colour palette per character, so an expression change keeps the colours), the briefing size for the briefing speakers Okafor and Varga, the generic CDF officer and civilian neutral, the Choir's 32-frame glyph loop (animated on the radio at 12 fps). Vorne, who does not speak in Acts 1–2, comes with Act 6. The portrait rules' "1–2 expressions (urgent, grim, smug)" are now the briefing document's three. Review files proposed for round 13; `art` stays `chosen`.
- 2026-10-02: Concept round 13 closed (user decision): the production portraits of Acts 1–2 (Okafor, Rook, Varga in neutral, grim and fierce, the generic officer and civilian, the Choir's 32-frame loop; `tools/art/portraits.py`) approved as **final**; `art` stays `chosen`, since Vorne's portraits come with Act 6.
- 2026-10-12 (overnight design phase): Ares Control's radio portrait (generic colonial a/b, teal friendly channel kept) proposed in concept round 36, pending the user's choice.
- 2026-10-13 (user, round 36, C.1 = b): Ares Control's radio portrait is the dust-suit controller (b), on the teal friendly channel (amber stays the enemy's); the old controller (a) rejected and moved to `concept/rejected/`. Its production portraits (neutral, grim, fierce) are made in M7 part B.
- 2026-10-14 (M7 part B, step ART-B): production art from the chosen round-36 look (generic colonial b), review sheet `portraits-final-r38-a` proposed for round 38 (pending the user's review).
- 2026-10-14 (user, round 38): Ares Control's production portrait `portraits-final-r38-a` approved as final
  (F.6). `art` stays `chosen`.
- 2026-10-22 (M8 part B, step ART-B; accepted in round 51 (2026-10-22)): Dr. Ilse Brandt's radio portrait `radio-conamara-neutral`
  (`portraits-final-r51-a`): it reads plain next to the main cast's (accepted in [round 51](../../concept-rounds/round-51/README.md), F.5).
- 2026-10-22 (user, round 51, closed; F.5): Dr. Brandt's radio portrait accepted as final (the plain face is kept); her voice is Margaret Espaillat (V.1 a).
- 2026-10-22 (M8 part C, step ART-C; accepted in round 52 (2026-10-22)): Thera Deep Control's radio portrait
  `radio-thera-deep-neutral` (`portraits-final-r52-a`, four tries): the harbour controller in the grey-blue harbour
  coverall with a pressure suit's neck ring, the control room's viewport on the lit farms behind him; on Level 23's radio
  (`radio-capture-r52-a`, `PortraitsTest.theraDeepControlSpeaksWithTheHarboursPortrait`).
- 2026-10-22 (user, round 52, closed): Thera Deep Control's radio portrait `radio-thera-deep-neutral` accepted as final (F.8); his voice is candidate b, John Van Stan (V.1).
- 2026-10-27 (Act 5 design phase, step m9-d-story; **approved in round 60**): Act 5's
  speakers: Vorne on the enemy channel (his production portraits and voice now, not with Act 6 as the
  2026-10-02 entry planned), Ceres Hub Control (a loyalist, friendly channel), the tug's captain (L31) and
  the mutineers' flight lead (L29, enemy channel), each with an own radio portrait made in its level's part
  like Ares Control's and Thera Deep Control's.
- 2026-10-27, design phase: the level drafts' placeholder names adopted into the Act 5 speakers table (step m9-d-act;
  approved in round 60): Harker (the mutineers' flight lead), the Bellwether (the tug and its
  captain's call sign), Bright Seam, Dacosta's claim, the Vesta relay and Kleopatra.
- 2026-10-27 (user, [round 60](../../concept-rounds/round-60/README.md) closed): the Act 5 speakers approved as drafted (D.18, J.10); **A.21** accepted: the radio sheet of Ceres Hub Control, the *Bellwether*'s captain (teal) and Harker (amber, the Ascendancy crest stitched over his CDF patch) is the portraits' reference (`concept/act5-speakers-r60-a.png`, chosen).
- 2026-10-28 (user, [round 62](../../concept-rounds/round-62/README.md), closed; page answers): the Act 5 radio portraits (Ceres Hub Control and Harker, F.8) accepted as final; `art: final`. Casting: Ceres Hub Control is Paul Adams (V.1 a), Harker is David Clarke (V.2 b), see the [voice part](../../audio/voice/README.md#decisions).
- 2026-10-28 (user, [round 64](../../concept-rounds/round-64/README.md), closed): **F.9** accepted: the Bellwether's radio portrait (`radio-bellwether-neutral`, teal) is final; **V.1 b**: her voice is Laurie Anne Walden (see [voice](../../audio/voice/README.md#decisions)).
- 2026-10-31 (Act 6 design phase, step m10-d-story; **decided 2026-10-31 (round 72)**): Act 6's speakers
  (J4, J10): Vorne and the Choir on their channels (intro page 3, L37, L39, L42; L37, L42), Aurelia Traffic as the
  act's friendly generic voice (L37), the Honour Guard's aces Harker (L29's flight lead, promoted into Vorne's guard;
  his L29 voice kept), Vesper and Kestrel (two new voices), each ace with a radio portrait proposed in round 72; Okafor
  and Varga on the carrier *Steadfast*. The level drafts' placeholder names adopted (Halcyon One–Six, the Spire Plaza,
  Under Aurelia, the Red Spot's Rim, the Tvashtar Plains, the Loki, Pele and Prometheus works). Trivia decided here:
  the call sign **Kestrel** is kept although Level 03's broken CDF frigate is the *Kestrel* (another side, 37 levels
  apart; a ship and a call sign); Aurelia Traffic and Vesper are women, Kestrel a man, for three voices apart.
- 2026-10-31 (user, [round 72](../../concept-rounds/round-72/README.md) closed): **A.15 a** (the Act 6 radio
  portraits as plain channel screens: Harker, Vesper, Kestrel on the amber enemy channel, Aurelia Traffic on the teal
  channel; b "their rooms" rejected, in `concept/rejected/`); the voices are V.1 b (Vesper, Jenny Bradshaw), V.2 a
  (Kestrel, Expatriate), V.3 b (Aurelia Traffic, Lizzie Driver), V.4 ok (Harker on David Clarke's voice); the story
  documents approved as drafted (D.17).
