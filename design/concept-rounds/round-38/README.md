---
title: Concept round 38 — M7 part B, Level 15 and the Act 3 intro
design: approved
implementation: n/a
art: chosen
depends-on: [../../campaign/act-3-red-dust, ../../campaign/act-3-red-dust/level-15-olympus-descent, ../../enemies/ground/burrower, ../../enemies/air/dust-kite, ../../world/mars, ../../audio/music, ../../audio/sfx, ../../audio/voice, ../../systems/saves, ../../systems/economy, ../../tech/roadmap, ../round-37]
updated: 2026-10-14
---

# Concept round 38 — M7 part B, Level 15 and the Act 3 intro

**Closed 2026-10-14** (user, on the phone review page): both changes to approved specs **accepted** (C1, C2;
in Level 15's radio table and difficulty notes); the Olympus backdrop, Level 15's props, the Burrower, the
Dust Kite, the intel pictures, Ares Control's portrait, the Act 3 title card and the six briefing images
**approved as final** (F.1–F.8); track 8 **approved as final** (M.1) and its base stem **approved** as
rendered (M.2); sounds: bulge **b** (the crack layer over the eruption, wired into the game in step R38C),
dive **b**, entry roar **a**, Mars dust wind **a**; Ares Control, the respellings and every voiced line
accepted (V.1–V.5); both captures accepted (K.1, K.2); every build choice **yes** (B.1–B.3); R.1 noted (the
hard plan unchanged), R.2 accepted, R.3 **keep all** (the ≈ 3 s deck, the first surfacing at ≈ 72.5 and the
level's end over the dunes stay as built; Level 15's prose updated to the measured values; the Act 3 intro
pages under Level 15's header accepted as they are). The rejected sounds moved to `concept/rejected/` and
`sfx_r38.py` routes them there (step R38C). M7 part B is built, pending the user's playthrough
([roadmap](../../tech/roadmap/README.md#m7-parts)). The outcome of each row is in its *Outcome* cell.

## Summary

The review round of M7 part B *Level 15 + Act 3 intro* ([roadmap](../../tech/roadmap/README.md#m7-parts)),
built while the user was on vacation: the `buried` layer and the
[Burrower](../../enemies/ground/burrower/README.md) (screen-fixed while it swims, bulges and is up, the
user's Q1 = a of 2026-10-14), the [Dust Kite](../../enemies/air/dust-kite/README.md)'s swoop,
[Level 15 Olympus Descent](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md) with its
drop launch, cloud deck and Olympus backdrop, the [Act 3](../../campaign/act-3-red-dust/README.md) title
card and intro played once from a release save (L.6), Ares Control voiced with the respelling list,
track 8 "Red Dust Run" with a provisional base stem, the Mars ambiences, and the economy re-planned on
the user's decision of 2026-10-14 (budget(n) is the absolute typical haul; round 37 R.1 reverted). The
documents stay `design: approved` (round 36); every default, pick and build choice below was taken on the
user's behalf and is **pending the user's review**. Where the round offers an a/b, the game plays **a**
until the round closes.

Open [index.html](index.html) in a browser (regenerate with
`python3 tools/concept/board.py 38 --subjects backdrop-final,l15-props-final,burrower-final,dust-kite-final,intel-final,portraits-final,act-3-still-final,briefing-images-final,burrow-bulge,burrow-bulge-mix,burrow-dive,entry-roar,mars-dust,red-dust-run-final,red-dust-run-seam-final,red-dust-run-base-final,red-dust-run-base-seam-final,music-final,level-15-capture,act-3-intro-capture`).
The rows are in this order:

1. **Changes to approved specs** (`C…`): *accept* or *revert*;
2. **Art** (`F.…` final art to approve): *approve as final* / *redo*;
3. **Sounds** (`S.…`) and **Music** (`M.…`): *a* / *b* / *redo*, or *approve* / *redo*;
4. **Voices** (`V.…`): Ares Control's cast and lines, the respellings, Level 15's and the Act 3 intro's
   lines, the re-keyed Act 2 outro page; *accept* / *redo* (say which line);
5. **Captures** (`K.…`): the game as built, *accept* or *change*;
6. **Build choices** (`B.…`) per step, each item *yes* (keep as built) or *no* (say what), and the
   **findings and records** (`R.…`).

## In the game

`./gradlew :desktop:run --args="…"` with `--settings <file>` to keep the real settings apart (a debug
run writes no save):
- Level 15 with the balance plan's ship: `--level 15 --loadout front=pulse-cannon:4,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:2,generator=1,shield=1,armor=2 --special airstrike:2 --escort rook:autocannon:1`;
- the Act 3 intro: `--start title` with a version 3 save at the Level 15 hangar in `saves/` next to the
  settings file (the [capture](#captures) used step B4's hand-written one), then CONTINUE;
- in a build of this branch, the release's end after Act 2 no longer fires (Level 15 exists), and the
  hangar after Level 15 cannot launch Level 16 until part C (expected).

## Changes to approved specs

What the steps changed in the approved Level 15 document to make it work. Each **C** row is *accept* or
*revert*.

| # | Change | Approved | Built (where) | Outcome |
|---|---|---|---|---|
| C1 | **Varga's kite warning at t=34.5** | Level 15's radio: Varga's "Those kites ride the dust…" at t=33.5 | t=34.5: Rook's t=23.5 line (with the escort only) holds the radio as text until a second past 33.5, so the line would start late (`RadioTimelineTest`, step VOX-B; [Level 15](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md#radio-chatter)) | **accepted** (user): in Level 15's radio table |
| C2 | **Easy's first pincer as two ranks** | Difficulty notes: on easy both Needler pincers enter from the front as a line abreast | the t=49 one as two ranks of 2 at t=49 and 51 (the autopilot clears one line in under a second, leaving the screen empty over 3 s before the t=52.5 snake, `PacingTest`; step B3; [Level 15](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md#difficulty-notes)) | **accepted** (user): in Level 15's difficulty notes |

## Art — final

Production art drawn from round 36's picks (`tools/art/`), in the game now.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.1 | [Olympus backdrop](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md#concept-art) | **final art** (`tools/art/backdrop_l15.py`, from the Olympus still r36-a and parallax r02-c): Mars far below on `deep` (Olympus Mons' caldera, the flank's lava channels), cirrus and entry streaks on `high-air`, the cloud deck's banks and its close-over on `low-air`, the slopes, Lycus escarpment and station, the dunes, the crashed lander and the rail line; the transport's underside over the launch | [sheet](../../campaign/act-3-red-dust/level-15-olympus-descent/concept/backdrop-final-r38-a.png), [sheet GIF](../../campaign/act-3-red-dust/level-15-olympus-descent/concept/backdrop-final-r38-a.gif) | the deep layer's parallax factor 0.15 (a default); the great cliff drawn as perspective towers along the right edge reads as a narrow wall strip in flight ([capture](#captures)) | approve as final / redo | **approved as final** (user) |
| F.2 | [Level 15's props](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md#concept-art) | **final art** (`tools/art/l15_props.py`): the station's supply containers (Level 01's container model in colony off-white and amber; intact, damaged, wreck, break-apart), the drop pod under basalt rubble, cleared hit by hit, with its blinking beacon | [sheet](../../campaign/act-3-red-dust/level-15-olympus-descent/concept/l15-props-final-r38-a.png), [sheet GIF](../../campaign/act-3-red-dust/level-15-olympus-descent/concept/l15-props-final-r38-a.gif) | — | approve as final / redo | **approved as final** (user) |
| F.3 | [Burrower](../../enemies/ground/burrower/README.md#concept-art) | **final art** (`tools/art/burrower.py`, U.1 = a): the sand maw at 64 px, the ripple in 16 headings, the bulge with lime cracks, the breach, the sand bow wave while it is up (Q1 = a), the maw opening and firing, the dive, the filling hole, the crater and the death | [sheet](../../enemies/ground/concept/burrower-final-r38-a.png), [sheet GIF](../../enemies/ground/concept/burrower-final-r38-a.gif) | the bulge glows lime (the spec says only cracks); the ripple is small and quiet on dark basalt | approve as final / redo | **approved as final** (user) |
| F.4 | [Dust Kite](../../enemies/air/dust-kite/README.md#concept-art) | **final art** (`tools/art/dust_kite.py`, U.7 = b): the crescent sail in 16 headings × 4 membrane flexes, its tatters and the death's teal spark and dust puff | [sheet](../../enemies/air/concept/dust-kite-final-r38-a.png), [sheet GIF](../../enemies/air/concept/dust-kite-final-r38-a.gif) | at the capture's half size the kites are hard to pick out over the rust (as designed: "you barely see them") | approve as final / redo | **approved as final** (user) |
| F.5 | [Intel pictures](../../ui/hangar/README.md#concept-art) | **final art** (`tools/art/intel.py burrower dust-kite`): the 30×30 sensor-L2 portraits of the two new units | [sheet](../../ui/hangar/concept/intel-final-r38-a.png) | — | approve as final / redo | **approved as final** (user) |
| F.6 | [Ares Control's portrait](../../story/characters/README.md#concept-art) | **final art** (`tools/art/portraits.py generic-colonial`, C.1 = b): the dust-suit controller on the friendly teal channel, next to the CDF and civilian generics | [sheet](../../story/characters/concept/portraits-final-r38-a.png) | — | approve as final / redo | **approved as final** (user) |
| F.7 | [Act 3 title card](../../campaign/act-3-red-dust/README.md#concept-art) | **final art** (`tools/art/act_stills.py act-3`, from Level 15's backdrop): Mars from the entry corridor, Olympus Mons far below, the deck's banks, a dust-storm front on the horizon; the chrome `RED DUST` lettering over it | [sheet](../../campaign/act-3-red-dust/concept/act-3-still-final-r38-a.png) | the `RED DUST` lettering is taller than Act 2's (the shorter words) | approve as final / redo | **approved as final** (user) |
| F.8 | [Briefing images](../../ui/briefing/README.md#concept-art) | **final art** (`tools/art/briefing_images.py`, six): the Act 3 intro's four (Mars in the transport's forward view, the CDF globe of the Vrell's spread, the colonists' dome, the drop route) and Level 15's two (the descent route, the Burrower scan) | [sheet](../../ui/briefing/concept/briefing-images-final-r38-a.png) | the Mars map is a simplified drawing of the real planet: some places sit a few degrees off | approve as final / redo | **approved as final** (user) |

## Sounds

Round 38's a/b for Level 15 ([sfx](../../audio/sfx/README.md#concept-art), `tools/concept/audio/sfx_r38.py`,
from credited CC0 and CC-BY originals plus synthesized layers); the game plays **a** until the pick. Not
listened to by Claude: checked by envelopes, spectrograms, levels and loop seams; no bubbly or popping
layers.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [Burrower bulge](../../audio/sfx/README.md#concept-art) | **pick a or b**: a = **no file**, the chosen eruption (round 36 a) started at the bulge, its first 1.0 s the rumble and its impact at the surfacing (the game's default); b = a crack layer of sand and crust cracking faster towards the break, played over the eruption (1.27 s) | [b](../../audio/sfx/concept/burrow-bulge-r38-b.ogg), [b as mixed](../../audio/sfx/concept/burrow-bulge-mix-r38-b.ogg) | b's sparse early cracks are the one layer with isolated transients (masked in the mix) | a / b / redo | **b** (user): the crack layer over the eruption, both from the bulge's start (`Sfx.BURROW_BULGE`, step R38C) |
| S.2 | [Burrower dive](../../audio/sfx/README.md#concept-art) | **pick a or b**: a "sand pour", a sand hiss under a falling low-pass with small stones dropping in (0.62 s); b "grain slide", a dense rain of noise grains falling 5 → 1 kHz over a slump (0.55 s) | [a](../../audio/sfx/concept/rejected/burrow-dive-r38-a.ogg), [b](../../audio/sfx/concept/burrow-dive-r38-b.ogg) | — | a / b / redo | **b** (user), in the game since step R38C; a in `concept/rejected/` |
| S.3 | [Entry roar](../../audio/sfx/README.md#concept-art) | **pick a or b**, the `mars-entry` ambience of sections 1–2, crossfading to `mars-dust` over 2 s at section 3: a "recorded roar", a howl high-passed to its thin roar over a hull buffet; b "synthesized roar", a thin air roar with a wandering whistle; seamless 12 s loops at −36 dB | [a](../../audio/sfx/concept/entry-roar-r38-a.ogg), [b](../../audio/sfx/concept/rejected/entry-roar-r38-b.ogg) | — | a / b / redo | **a** (user); b in `concept/rejected/` |
| S.4 | [Mars dust wind](../../audio/sfx/README.md#concept-art) | **pick a or b**, `mars-dust` (Level 15 from section 3, Levels 20–21): a "recorded dust wind", a sandstorm on slow gusts over a cold wind's moan, calmer than `mars-storm`; b "synthesized dust wind", a thin wind with a faint whistle and grit rising with the gusts; seamless 16 s loops at −36 dB | [a](../../audio/sfx/concept/mars-dust-r38-a.ogg), [b](../../audio/sfx/concept/rejected/mars-dust-r38-b.ogg) | — | a / b / redo | **a** (user); b in `concept/rejected/` |

## Music

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| M.1 | [Track 8 "Red Dust Run"](../../audio/music/README.md#track-8-red-dust-run-in-production-m7-part-b) | **final mix** (`tools/art/themes.py red-dust`, `music_r38.py`): the round-02 sketch at full length, 181.8 s: the sketch's intro, then a 96-bar loop of twelve sections (A, B, the new "caravan" D, A2, the dust storm C, the new bridge E, B2, the desert F, the build G, the climax B3, A3, the turnaround); −14 LUFS, a clean loop seam | [full mix](../../audio/music/concept/red-dust-run-final-r38-a.ogg), [loop seam](../../audio/music/concept/red-dust-run-seam-final-r38-a.ogg), [loudness sheet](../../audio/music/concept/music-final-r38-a.png) | not listened to by Claude; sections range −12.9 to −16.7 LUFS | approve as final / redo | **approved as final** (user) |
| M.2 | [Track 8's base stem](../../audio/music/README.md#track-8-red-dust-run-in-production-m7-part-b) | **provisional stem**, a default: the music README wants the full mix approved before its stems, but Level 15 plays the base stem from section 2, so it was rendered with the full mix (the same render: hand drums, bass, wind, drone, string pads and kicks; no breakbeat top, reed, melody or arpeggio) and is reviewed with it | [base stem](../../audio/music/concept/red-dust-run-base-final-r38-a.ogg), [its loop seam](../../audio/music/concept/red-dust-run-base-seam-final-r38-a.ogg) | a redo of M.1 re-renders the stem too | approve / redo | **approved** (user): the base stem stays as rendered |

## Voices

The lines as the game plays them from `assets/voice/` (rendered in step VOX-B through the radio filter;
all of them in the [Listening](#listening) table). **LISTEN** marks the takes Claude flagged.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| V.1 | [Ares Control](../../audio/voice/README.md#concept-art) | **accept the cast and his five lines** (round 36 S.6 = b, Thomas A): his reference clip trimmed of its long pauses (12.0 → 10.3 s), his takes retried by `voice.py` when a mid-line pause is over 0.8 s (three retried); t=12, t=84, the drop pod, t=158.5, the secondary | [reference](../../audio/voice/refs/ref-ares-control.wav), [t=12](../../../assets/voice/ares-control/884c091c79bb.ogg), [t=84](../../../assets/voice/ares-control/9beef5fc32a2.ogg), [drop pod](../../../assets/voice/ares-control/d5164ec2b9e1.ogg), [t=158.5](../../../assets/voice/ares-control/c901a8fc4732.ogg), [secondary](../../../assets/voice/ares-control/988b50c6dbd3.ogg) | LISTEN t=84 ("…Lycus Station…" may sound like "Like a station") and the drop pod ("…last week" may sound like "ages") | accept / redo (say which) | **accepted** (user) |
| V.2 | [The respellings](../../audio/voice/README.md#concept-art) | **accept or change**: the text as spoken uses Airees (Ares), Skyaparelli, Burrows (accepted at the audition), Vallis Marinairis and Coprateez (kept: the plain spellings were read back as "Vales/Valet's" and "cop rates"); Lycus and Eos stay plain (read back as well as the respelled ones) | [Okafor, "Airees"](../../../assets/voice/okafor/543313dab5f6.ogg) | read back by a recogniser, not by ear | accept / change | **accepted** (user) |
| V.3 | [Level 15's radio](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md#radio-chatter) | **accept or redo**: Okafor's five, Varga's four, Rook's three (his `{side}` line in both takes, pinned) | [Okafor t=1](../../../assets/voice/okafor/15e482c34ea8.ogg), [Rook t=23.5 left](../../../assets/voice/rook/aef1e736b4b8.ogg), [Rook t=23.5 right](../../../assets/voice/rook/f8ce36c82ede.ogg), [Varga t=34.5](../../../assets/voice/varga/90fe04e9c27e.ogg), [Okafor t=45.5](../../../assets/voice/okafor/d44ba7a6cd66.ogg), [Varga t=57](../../../assets/voice/varga/045cc85a820e.ogg), [Varga first surfacing](../../../assets/voice/varga/af6ba53edfea.ogg), [Rook t=100](../../../assets/voice/rook/6c2bcadeaff8.ogg), [Okafor t=110](../../../assets/voice/okafor/4aae6723796c.ogg), [Varga t=149.5](../../../assets/voice/varga/60f9f139a66e.ogg), [Okafor t=168](../../../assets/voice/okafor/b52e65ba5d17.ogg), [Okafor end](../../../assets/voice/okafor/78127783274c.ogg) | the texts are in [Listening](#listening) | accept / redo (say which) | **accepted** (user) |
| V.4 | [The Act 3 intro and Level 15's briefing](../../campaign/act-3-red-dust/README.md#act-intro-and-outro) | **accept or redo**: the four intro pages (Okafor, Varga, Okafor, Okafor fierce; 17.5, 11.4, 12.6, 11.7 s) and Level 15's two (Okafor, Varga) | [intro 1](../../../assets/voice/okafor/543313dab5f6.ogg), [intro 2](../../../assets/voice/varga/70e7a8277e18.ogg), [intro 3](../../../assets/voice/okafor/473f82872a3c.ogg), [intro 4](../../../assets/voice/okafor/33b81d9b9254.ogg), [briefing 1](../../../assets/voice/okafor/0b9c66f21b87.ogg), [briefing 2](../../../assets/voice/varga/16033c921c9d.ogg) | — | accept / redo (say which) | **accepted** (user) |
| V.5 | [Act 2 outro, page 3](../../campaign/act-2-homefront/README.md#act-intro-and-outro) | **accept the re-keyed take**: the respelling changes the line's key, so the accepted Act 2 file was rendered again with "Airees" (the old take deleted) | [the take](../../../assets/voice/okafor/dbf8451c1c3f.ogg) | LISTEN: "Airees" | accept / redo | **accepted** (user) |

## Captures

The `installDist` build on a private display, the game's own sound (its `wave` mix); how each was taken
is in its folder's `concept/prompts.md`.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| K.1 | [Level 15](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md#concept-art) | **game capture**: a whole medium run with the plan's L15 ship and Rook, **no `--invulnerable`** (armour 100 → 93): the drop under the transport, the deck closing over, the Burrowers' bulge, surfacing, rings and dive, the Dust Kites, Lycus Survey Station, the drop pod's rubble (secret found), the pincer, the dune field, the rail line, the debrief (198 / 200, A+) and the hangar; 3:53 with sound and a sheet of 20 frames | [level-15-capture-r38-a.png](../../campaign/act-3-red-dust/level-15-olympus-descent/concept/level-15-capture-r38-a.png), [.mp4](../../campaign/act-3-red-dust/level-15-olympus-descent/concept/level-15-capture-r38-a.mp4) | the driver stays at the bottom, so the crate and the containers' salvage are not picked up; the timing findings are [R.3](#build-choices-and-findings) | accept / change | **accepted** (user) |
| K.2 | [Act 3 intro (L.6)](../../campaign/act-3-red-dust/README.md#concept-art) | **game capture**: a version 3 save at the Level 15 hangar continued from the main menu: the title card (by itself after ≈ 3.5 s), the four voiced intro pages, Level 15's two briefing pages, the hangar; a sheet of 10 frames and 42 s of video | [act-3-intro-capture-r38-a.png](../../campaign/act-3-red-dust/concept/act-3-intro-capture-r38-a.png), [.mp4](../../campaign/act-3-red-dust/concept/act-3-intro-capture-r38-a.mp4) | step B4's hand-written save, not one written by the M6 release; a debug run writes no save, so "only once" is shown by the tests, not here; the intro pages wear Level 15's header and objectives, as the briefing screen draws act pages | accept / change | **accepted** (user) |

## Build choices and findings

What the steps chose where the documents are silent, and what they found. **B** rows: each item
*yes* (keep as built) or *no* (say what). **R** rows: as their answer cell says.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.1 | [Burrower and Dust Kite in the simulation](../../enemies/ground/burrower/README.md) | **yes or no, each** (step B1): (a)–(f) below | [K.1](#captures) | — | all yes / some no | **all yes** (user) |
| B.2 | [Level 15's data and the content](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md) | **yes or no, each** (steps B2, B3): (a)–(g) below | — | (g) touched the simulation from a content step (old waves unchanged, a test added) | all yes / some no | **all yes** (user) |
| B.3 | [In the game](../../enemies/ground/burrower/README.md#implementation) | **yes or no, each** (step B4): (a)–(f) below | [K.1](#captures), [K.2](#captures) | — | all yes / some no | **all yes** (user); (e) now with S.1's crack layer over the eruption |
| R.1 | [The Act 3 economy](../../systems/economy/README.md) | **record of your decision** (2026-10-14, option a): budget(n) is the absolute typical haul, round 37 R.1's act-factor income reverted, the Act 3 purchases re-planned (step B3): an EMP charge at L15, the Mk III at L16, saving at L17, the Ion Beam at L2 at L18 (the Pulse Cannon sold), L3 at L19, the Tail Gun L3 at L20, Composite III at L21; Level 15's own data earns 1 800 (`bounty_scale` 0.42). **Finding**: on hard the beam is refused at L18 (about 330 short after the repairs), so hard keeps the Pulse Cannon L4 (77 forward, 0.77× the reference at L21) and banks about 7 100 by the L21 visit | — | hard is tighter on purpose (2026-10-05); a hard player banking 7 100 without the beam may be a plan artefact rather than a real squeeze | noted / change the hard plan (say how) | **noted** (user): the hard plan unchanged |
| R.2 | [The Burrowers' kill share](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md#credit-budget) | **finding**: the autopilot kills every Burrower it sees surface (27 / 27 medium, 21 / 21 easy, 32 / 32 hard, none escaped), above the 80 % the haul assumes, so the surfaced windows were not retuned; the capture's pilot killed 198 / 200 of everything | — | the autopilot lines up under a bulge; a human may not | accept / change | **accepted** (user) |
| R.3 | [What the Level 15 capture found](#captures) | **findings: fix or keep, each** (below; nothing changed) | [K.1 sheet](../../campaign/act-3-red-dust/level-15-olympus-descent/concept/level-15-capture-r38-a.png) | — | keep all / fix some (say which) | **keep all** (user): (a)–(c) as built; Level 15's prose now gives the measured ≈ 3 s close-over and the ≈ 72.5 first surfacing |

**Build choices (row B.1)**, each *yes* (keep) or *no* (say what):

- **(a)** A Burrower lives at most 30 s (never cut short while it is up), then swims off the bottom edge
  under the sand: an escape, no bounty.
- **(b)** The Dust Kite's swoop bends 60 px across the ship's lane.
- **(c)** The surfacing spot is picked as the swim starts and checked again as the bulge starts (at
  least 110 px from the ship).
- **(d)** When the top edge leaves too little room ahead of the ship, the spot is mirrored below it.
- **(e)** The stagger: a unit bulges 0.8 s after its wave's last bulge, and only while fewer than two of
  its wave would be up when it surfaces.
- **(f)** A spot in a no-surface zone moves to the nearest allowed point beside or beyond the zone;
  without one the surfacing is skipped (it still counts as one of its three).

**Build choices (row B.2)**, each *yes* (keep) or *no* (say what):

- **(a)** `launch: drop`: the ship and Rook drop in from above the top edge to the start spot over the
  first 5 s, under the transport's pass.
- **(b)** The no-surface zones cover the station's pads and hab, the escarpment's cliff strip and the rail
  line.
- **(c)** The hard-only Spine Turret pair stands beside the rail line at t≈185.
- **(d)** The hangar's intel strip marks the six Burrower fields (`BURROW_FIELD`).
- **(e)** L.6's flag: a story flag `act-intro:N` per act whose intro was shown; a version 3 save is
  migrated with `act-intro:1` and, from Level 08 on, `act-intro:2`, so only Act 3's intro plays on
  Continue.
- **(f)** The `mars-entry` ambience key for sections 1–2 (a new name next to `mars-dust`).
- **(g)** The simulation's V-wing places gliding units (the Dust Kites) in their wing slots as they fly
  down; earlier V-wings are unchanged.

**Build choices (row B.3)**, each *yes* (keep) or *no* (say what):

- **(a)** The transport's underside is drawn as a 75 % overhead pass in normal blending, chosen by its
  piece name (no data key).
- **(b)** Up to 32 Burrower craters stay on the ground at once (the oldest goes).
- **(c)** A Burrower killed early in its dive leaves its crater but no open hole.
- **(d)** `mars-entry` plays in sections 1–2 and crossfades over 2 s to `mars-dust` at section 3.
- **(e)** The eruption sound starts with the bulge (S.1 a), the dive plays its own sound.
- **(f)** Continue goes through the intro check from both the main menu and the save slots.

**Findings (row R.3)**, seen in the Level 15 capture and not changed (Claude's reading):

- **(a)** The cloud deck covers the whole field for about 3 s (t≈61.5–64.5; closing from ≈ 60, parted by
  ≈ 65.5) where the README says about 2 s at t≈62–64.
- **(b)** The first field's first bulge came at t≈71.5 and its surfacing at ≈ 72.5; the README expects
  the first surfacing from t≈69.5. Varga's `first-surfacing` line still falls in the t=68–84 gap.
- **(c)** The rail line crosses the screen at t≈186–188, but the last field is still surfacing to
  t≈194 and the level's end lines start at ≈ 196: the level ends over the dunes, not over the rail line.

## Listening

Every line part B rendered, as the game plays it (step VOX-B).

| Where | Speaker | Line | File |
|---|---|---|---|
| Act 2 outro page 3 (re-keyed) | Okafor | "An hour ago Ares Landing sent a priority signal…" — LISTEN: "Airees" | [play](../../../assets/voice/okafor/dbf8451c1c3f.ogg) |
| Act 3 intro page 1 | Okafor | "Nine days at full burn. Ares Landing has now held alone for five weeks…" | [play](../../../assets/voice/okafor/543313dab5f6.ogg) |
| Act 3 intro page 2 | Varga | "The Vrell came down in the Hellas basin, in the south…" | [play](../../../assets/voice/varga/70e7a8277e18.ogg) |
| Act 3 intro page 3 | Okafor | "The colonists asked Earth for help long before the Gate opened…" | [play](../../../assets/voice/okafor/473f82872a3c.ogg) |
| Act 3 intro page 4 | Okafor (fierce) | "We go in from the west, past Olympus Mons…" | [play](../../../assets/voice/okafor/33b81d9b9254.ogg) |
| L15 briefing page 1 | Okafor | "The Vrell own the sky over Ares Landing, so we come in from the west…" | [play](../../../assets/voice/okafor/0b9c66f21b87.ogg) |
| L15 briefing page 2 | Varga | "The colonists call them Burrowers…" | [play](../../../assets/voice/varga/16033c921c9d.ogg) |
| L15 t=1 | Okafor | "Aegis, you're clear of the transport. The corridor is hot. Weapons free." | [play](../../../assets/voice/okafor/15e482c34ea8.ogg) |
| L15 t=12 | Ares Control | "Aegis flight, Ares Landing. Five weeks we held alone. Nice of Earth to drop by." | [play](../../../assets/voice/ares-control/884c091c79bb.ogg) |
| L15 t=23.5 | Rook (left) | "Copy, Lancer. I'm on your left. Watch the clouds, things are hiding in them." | [play](../../../assets/voice/rook/aef1e736b4b8.ogg) |
| L15 t=23.5 | Rook (right) | "Copy, Lancer. I'm on your right. …" | [play](../../../assets/voice/rook/f8ce36c82ede.ogg) |
| L15 t=34.5 | Varga | "Those kites ride the dust. You barely see them until they turn. Shoot early." | [play](../../../assets/voice/varga/90fe04e9c27e.ogg) |
| L15 t=45.5 | Okafor | "Clouds are breaking up. Olympus Mons dead ahead, all twenty-two kilometres of it." | [play](../../../assets/voice/okafor/d44ba7a6cd66.ogg) |
| L15 t=57 | Varga | "Ground movement on the slopes. Something is swimming under the sand." | [play](../../../assets/voice/varga/045cc85a820e.ogg) |
| L15 first surfacing | Varga | "There! The sand bulges before it comes up. Hit it while it's out." | [play](../../../assets/voice/varga/af6ba53edfea.ogg) |
| L15 t=84 | Ares Control | "Those things took our survey posts. Lycus Station went dark a week ago." — LISTEN: "Lycus Station" | [play](../../../assets/voice/ares-control/9beef5fc32a2.ogg) |
| L15 t=100 | Rook | "Bugs that swim in the dirt. Back home we just had moles." | [play](../../../assets/voice/rook/6c2bcadeaff8.ogg) |
| L15 t=110 | Okafor | "Lycus Station ahead. The growths on its pads are Vrell. Burn them off." | [play](../../../assets/voice/okafor/4aae6723796c.ogg) |
| L15 drop pod (secret) | Ares Control | "Our supply drop from last week. It's yours, Aegis." — LISTEN: "last week" | [play](../../../assets/voice/ares-control/d5164ec2b9e1.ogg) |
| L15 t=149.5 | Varga | "That song again. Lancer, it's coming from under the ground." | [play](../../../assets/voice/varga/60f9f139a66e.ogg) |
| L15 t=158.5 | Ares Control | "Rail line in sight. Half the colony is watching you come in." | [play](../../../assets/voice/ares-control/c901a8fc4732.ogg) |
| L15 t=168 | Okafor | "Big contact under the dunes. Lots of them. Keep moving." | [play](../../../assets/voice/okafor/b52e65ba5d17.ogg) |
| L15 secondary met | Ares Control | "The slopes are quiet. Maybe Earth can fly after all." | [play](../../../assets/voice/ares-control/988b50c6dbd3.ogg) |
| L15 level end | Okafor | "You're down, Aegis. Refuel at the rail head. The canyon run starts at dawn." | [play](../../../assets/voice/okafor/78127783274c.ogg) |

## Decisions

- 2026-10-14: opened (M7 part B, step B5): the part's final art, sounds, music, voices, captures, build
  choices and findings, pending the user's review.
- 2026-10-14 (user, round 38): closed: every row answered as in its *Outcome* cell; S.1's crack layer
  (`Sfx.BURROW_BULGE`) and S.2's dive b put into the game, the rejected sounds moved and `sfx_r38.py`
  routed, Level 15's prose set to the measured deck and surfacing times (step R38C).
