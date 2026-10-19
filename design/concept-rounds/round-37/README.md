---
title: Concept round 37 — M7 part A, the Act 3 systems and arsenal
design: approved
implementation: n/a
art: chosen
depends-on: [../../player/weapons, ../../player/specials, ../../player/systems, ../../player/wingmen, ../../ui/hud, ../../ui/hangar, ../../ui/controls, ../../systems/saves, ../../audio/sfx, ../../audio/voice, ../../tech/roadmap, ../round-36]
updated: 2026-10-13
---

# Concept round 37 — M7 part A, the Act 3 systems and arsenal

**Closed 2026-10-13** (user, on the review page): both changes to approved specs **accepted** (C1, C2); the
Ion Beam's, the light drone's, the EMP Burst's and the Shield Overcharge's art **approved as final**
(F.1–F.4); picks: Deflector Pod **a** "field band", Orbital Lance **b** "descending rings", Act 3 icons
**a**; sounds S.2, S.4, S.6–S.10 **b**, the rest **a**; Lance Control **b** (Celine Major); every capture
accepted (K.1–K.4), every build choice **yes** (B.1–B.6), R.2, R.3 and V.1 accepted. R.1: **apply the act
factor** (against step A2's reading; built and re-planned in step Q1); R.4: **fix all**, (c) included (the
beam stops on the Brood Carrier's first phase and the Siege Spire; step Q1). The picks went into the game
in step Q2; the rejected files moved to each part's `concept/rejected/` and the round-37 generators
route them there (step Q3). M7 part A is built, pending the user's playthrough
([roadmap](../../tech/roadmap/README.md#m7-parts)). The outcome of each row is in its *Outcome* cell.

## Summary

The review round of M7 part A *Act 3 systems* ([roadmap](../../tech/roadmap/README.md#m7-parts)),
built while the user was on vacation: the Ion Beam and its `beam` delivery, the Light Drone Bay and
its drones, the Deflector Pod, the Rear Lance, the EMP Burst with the `machine` flag, the Orbital
Lance, the Shield Overcharge with the cooldown rules and the HUD ring, the Evasive thrusters' dash
and its pip, the third utility bay (save format 4), `reference_dps` 15–21 and the balance plan to
Level 21. No Act 3 level exists yet, so everything is flown on Act 1–2 levels with `--loadout` and
`--special`. The documents stay `design: approved` (round 36); every default, pick and build choice
below was taken on the user's behalf and is **pending the user's review**. Where the round offers
an a/b, the game plays **a** until the round closes.

Open [index.html](index.html) in a browser (regenerate with
`python3 tools/concept/board.py 37 --subjects deflector-pod,orbital-lance,act3-icons,ion-beam-final,light-drone-final,emp-burst-final,shield-overcharge-final,ion-beam-start,ion-beam-stop,beam-impact,beam-impact-rock,deflector-tick,deflector-return,emp-ring,emp-powerdown,lance-rumble,overcharge-whoosh,overcharge-end,cooldown-ready,dash,drone-break,drone-rebuild,voice-lance-control,arsenal-capture,specials-capture,dash-capture,act3-hangar-capture`).
The rows are in this order:

1. **Changes to approved specs** (`C…`): *accept* or *revert*;
2. **Art** (`F.…` final art to approve, `P.…` a/b picks): *approve as final* / *redo*, or *a* / *b*;
3. **Sounds** (`S.…`, the 15 equipment sounds and Lance Control's voice): *a* / *b* / *redo*;
4. **Captures** (`K.…`): the game as built, *accept* or *change*;
5. **Build choices** (`B.…`) per step, each item *yes* (keep as built) or *no* (say what), and the
   **findings** (`R.…`) the steps raised, with the save format (`V.1`).

## In the game

`./gradlew :desktop:run --args="…"` with `--settings <file>` to keep the real settings apart (a debug
run writes no save):
- the arsenal: `--level 5 --loadout front=ion-beam:2,left=light-drone-bay,right=deflector-pod,utility=evasive-thrusters`
  plus `--special emp-burst:3`, `--special orbital-lance:2`, `--special shield-overcharge` or
  `--special smart-bomb:3`; the Dash is V (or the left bumper) or a double-tap of a direction;
- `--difficulty hard` brings enough bullets for the drones and the Deflector Pod;
- the hangar's third bay and the Act 3 shop rows need a save at Level 15 or later (the
  [capture](#captures) used a hand-written one).

## Changes to approved specs

Round 36 approved the drone's HUD pips and the special row's text; the HUD's 8 px font and the
weapons box forced a different layout. Each **C** row is *accept* or *revert*.

| # | Change | Approved | Built (where) | Outcome |
|---|---|---|---|---|
| C1 | **The light drone's hits as a strip under its row** | round 36, G.2 = a: the drone's HUD hits-left pips **in place of** the level pips | the level pips stay; the drone's hits are a 2 px strip of five pips under its row, cyan, refilling dim while its bay rebuilds it ([HUD](../../ui/hud/README.md#decisions)) | **accepted** (user) |
| C2 | **The compact special row for a cooldown special** | the specials' HUD ring: `SHIELD OVERCHARGE 27s` / `READY` beside the ringed icon | the 8 px font cannot fit it: for a cooldown special or a name over 10 characters the row goes compact, the ringed icon where the word `SPECIAL` stands and `OVERCHARGE 27s` / `READY` after it (the Orbital Lance's name and charges likewise); the Act 1–2 specials and the EMP Burst keep the row as it was ([HUD](../../ui/hud/README.md#decisions)) | **accepted** (user) |

## Art — final

Production art drawn from round 36's picks (`tools/art/`), in the game now.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.1 | [Ion Beam](../../player/weapons/ion-beam/README.md#concept-art) | **final art** (`tools/art/ion_beam.py`, G.1 = a): six 64 px column tiles per level width, the tips, the large and small contact flares, the rock-scorch flare, dust, scorch mark and glance spark | [sheet](../../player/weapons/ion-beam/concept/ion-beam-final-r37-a.png), [sheet GIF](../../player/weapons/ion-beam/concept/ion-beam-final-r37-a.gif) | the rock scorch shows on ground units until Level 16 brings rock walls; see the [capture](#captures) for the flare on a big target | approve as final / redo | **approved as final** (user) |
| F.2 | [Light drone](../../player/weapons/light-drone-bay/README.md#concept-art) | **final art** (`tools/art/light_drone.py`, G.2 = a): the disc drone in 16 headings, its shot, break and rebuild, the dock-ring bay on the five banking frames | [sheet](../../player/weapons/light-drone-bay/concept/light-drone-final-r37-a.png), [sheet GIF](../../player/weapons/light-drone-bay/concept/light-drone-final-r37-a.gif) | the rebuild is shortened in the GIF's loop | approve as final / redo | **approved as final** (user) |
| F.3 | [EMP Burst](../../player/specials/README.md#concept-art) | **final art** (`tools/art/act3_specials.py`, G.3 = a): the ring as a 60×128 strip mapped round the circle with four crackle frames; the stun crackle overlay in three sizes | [sheet](../../player/specials/concept/emp-burst-final-r37-a.png), [sheet GIF](../../player/specials/concept/emp-burst-final-r37-a.gif) | the GIF's Ghost Drones are stand-ins: no Act 1–2 unit is a machine | approve as final / redo | **approved as final** (user) |
| F.4 | [Shield Overcharge](../../player/specials/README.md#concept-art) | **final art** (`tools/art/act3_specials.py`, G.4 = b): the white double shell loop and the activation flare | [sheet](../../player/specials/concept/shield-overcharge-final-r37-a.png), [sheet GIF](../../player/specials/concept/shield-overcharge-final-r37-a.gif) | — | approve as final / redo | **approved as final** (user) |

## Art — picks

Round 36 showed no look for these; the game draws **a** until the pick.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| P.1 | [Deflector Pod](../../player/weapons/deflector-pod/README.md#concept-art) | **pick a or b** (`tools/art/deflector_pod.py`): a "field band", an emitter pod with a cyan dome, the field a thin cyan band with a faint glow, a catch lighting a short arc white with sparks; b "plate lattice", a pod with three emitter vanes, the field as five thin hex-cell plates, a catch flashing the plate with a spark ring | [a](../../player/weapons/deflector-pod/concept/deflector-pod-r37-a.png), [a GIF](../../player/weapons/deflector-pod/concept/deflector-pod-r37-a.gif), [b](../../player/weapons/deflector-pod/concept/rejected/deflector-pod-r37-b.png), [b GIF](../../player/weapons/deflector-pod/concept/rejected/deflector-pod-r37-b.gif) | b's plates are busier next to the drone pods and the shield ring | a / b / redo | **a** (user); b in `concept/rejected/` |
| P.2 | [Orbital Lance](../../player/specials/README.md#concept-art) | **pick a or b** (`tools/art/act3_specials.py`): a "piercing ray", a solid red targeting line, a column with a wide white-hot core and a blue halo with striations racing down; b "descending rings", a dashed double red line with chevrons, a narrower core with bright rings travelling down | [a](../../player/specials/concept/rejected/orbital-lance-r37-a.png), [a GIF](../../player/specials/concept/rejected/orbital-lance-r37-a.gif), [b](../../player/specials/concept/orbital-lance-r37-b.png), [b GIF](../../player/specials/concept/orbital-lance-r37-b.gif) | — | a / b / redo | **b** (user; in the game since step Q2); a in `concept/rejected/` |
| P.3 | [Act 3 hangar icons](../../ui/hangar/README.md#concept-art) | **pick a or b** (`tools/art/icons.py`, 16 and 24 px): the seven new items (Ion Beam, Rear Lance, Deflector Pod, Light Drone Bay, EMP Burst, Orbital Lance, Shield Overcharge); a draws each as its effect (a thick beam with a contact star, the Lance Laser's emblem turned round, the pod with its arc, the disc drone, a ring with a bolt, a column on a target ring, two nested shields); b as an emblem (an emitter ring, a lance through two marks, the arc catching a bolt, a drone on an orbit arc, concentric arcs, a satellite over a column, a shield with an up arrow) | [a](../../ui/hangar/concept/act3-icons-r37-a.png), [b](../../ui/hangar/concept/rejected/act3-icons-r37-b.png) | a's EMP ring and Overcharge shields are close at 16 px | a / b / redo | **a** (user); b in `concept/rejected/` |

## Sounds

Round 37's a/b for the Act 3 equipment ([sfx](../../audio/sfx/README.md#concept-art),
`tools/concept/audio/sfx_r37.py`, every source CC0); the game plays **a** until the pick. Not
listened to by Claude: checked by envelopes, spectrograms, levels and loop seams; no bubbly or
popping layers.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [Ion Beam start](../../audio/sfx/README.md#concept-art) | **pick a or b**, played **with** the chosen loop (r36-a): a "charge snap", the round-04 start's rise at 1.8× speed, a hiss and the loop's body rising onto its pitch (0.40 s); b "ion strike", an arc crack and the body settling 220 → 110 Hz (0.45 s) | [a](../../audio/sfx/concept/ion-beam-start-r37-a.ogg), [b](../../audio/sfx/concept/rejected/ion-beam-start-r37-b.ogg), [loop](../../audio/sfx/concept/ion-beam-loop-r36-a.ogg) | the round-04 start (0.9 s) was too long for a beam at full length in 0.05 s | a / b / redo | **a** (user); b in `concept/rejected/` |
| S.2 | [Ion Beam stop](../../audio/sfx/README.md#concept-art) | **pick a or b**: a "power down, short", the round-04 stop's first fall over the body sliding 110 → 45 Hz (0.55 s); b "ion collapse", the body sliding 110 → 35 Hz (0.62 s) | [a](../../audio/sfx/concept/rejected/ion-beam-stop-r37-a.ogg), [b](../../audio/sfx/concept/ion-beam-stop-r37-b.ogg) | — | a / b / redo | **b** (user); in the game since step Q2; a in `concept/rejected/` |
| S.3 | [Beam impact sizzle](../../audio/sfx/README.md#concept-art) | **pick a or b**, a loop while the beam burns a target: a "arc scorch", a fluttering dry sizzle over a burning body; b "cutting torch" (a plasma cutter recording), both 1.5 s at −38 dB | [a](../../audio/sfx/concept/beam-impact-r37-a.ogg), [b](../../audio/sfx/concept/rejected/beam-impact-r37-b.ogg) | at −38 dB, 1–2.5 LU under the beam loop, it may be lost in a busy mix | a / b / redo | **a** (user); b in `concept/rejected/` |
| S.4 | [Beam impact on rock](../../audio/sfx/README.md#concept-art) | **pick a or b**: S.3's a or b with a grit crackle and a stone roar (for the rock walls of Level 16; until then on ground objects) | [a](../../audio/sfx/concept/rejected/beam-impact-rock-r37-a.ogg), [b](../../audio/sfx/concept/beam-impact-rock-r37-b.ogg) | — | a / b / redo | **b** (user); in the game since step Q2; a in `concept/rejected/` |
| S.5 | [Deflector tick](../../audio/sfx/README.md#concept-art) | **pick a or b**, a bullet caught: a the shield hit at 1.6× speed with a glassy tick; b "dry deflect", a high snap with three metal partials (0.12 s) | [a](../../audio/sfx/concept/deflector-tick-r37-a.ogg), [b](../../audio/sfx/concept/rejected/deflector-tick-r37-b.ogg) | heard many times a second under a stream | a / b / redo | **a** (user); b in `concept/rejected/` |
| S.6 | [Deflector charge return](../../audio/sfx/README.md#concept-art) | **pick a or b**: a "soft rise", two voices easing up a fifth; b "charge settle", a fifth fading in over an air whoosh (0.45 s) | [a](../../audio/sfx/concept/rejected/deflector-return-r37-a.ogg), [b](../../audio/sfx/concept/deflector-return-r37-b.ogg) | — | a / b / redo | **b** (user); in the game since step Q2; a in `concept/rejected/` |
| S.7 | [EMP ring](../../audio/sfx/README.md#concept-art) | **pick a or b**: a "the tesla, layered", four ever slower zaps spreading out, arcs and a thump (1.35 s); b "arc ring", a dying electric buzz, a discharge crack, arcs and a thump (1.34 s) | [a](../../audio/sfx/concept/rejected/emp-ring-r37-a.ogg), [b](../../audio/sfx/concept/emp-ring-r37-b.ogg) | b is about 5 LU quieter than a at the same peak level | a / b / redo | **b** (user); in the game since step Q2; a in `concept/rejected/` |
| S.8 | [EMP power-down](../../audio/sfx/README.md#concept-art) | **pick a or b**, a stunned machine (a Vrell unit 6 dB lower): a "spin-down whine", a relay clunk and a whine falling 1400 → 180 Hz; b "buzz dies", a 100 Hz buzz sagging to 70 Hz (0.9 s) | [a](../../audio/sfx/concept/rejected/emp-powerdown-r37-a.ogg), [b](../../audio/sfx/concept/emp-powerdown-r37-b.ogg) | — | a / b / redo | **b** (user); in the game since step Q2; a in `concept/rejected/` |
| S.9 | [Orbital Lance rumble](../../audio/sfx/README.md#concept-art) | **pick a or b**, under the chosen loop c: a "orbital roar", a deep roar over low voices on 41 Hz; b "the ground answers" (an earthquake recording), seamless 3.0 s loops at −36 dB | [a](../../audio/sfx/concept/rejected/lance-rumble-r37-a.ogg), [b](../../audio/sfx/concept/lance-rumble-r37-b.ogg) | 41 Hz is lost on laptop and phone speakers | a / b / redo | **b** (user); in the game since step Q2; a in `concept/rejected/` |
| S.10 | [Overcharge activation](../../audio/sfx/README.md#concept-art) | **pick a or b**: a "the restore, charged", a power-up with a whoosh and shimmer; b "double shell", a pad rising an octave as its filter opens, a whoosh and a bloom (1.0 s) | [a](../../audio/sfx/concept/rejected/overcharge-whoosh-r37-a.ogg), [b](../../audio/sfx/concept/overcharge-whoosh-r37-b.ogg) | — | a / b / redo | **b** (user); in the game since step Q2; a in `concept/rejected/` |
| S.11 | [Overcharge end](../../audio/sfx/README.md#concept-art) | **pick a or b**: a "shimmer fades", high partials gliding down; b "the charge drains", the restore's source backwards under a falling shimmer (0.9 s) | [a](../../audio/sfx/concept/overcharge-end-r37-a.ogg), [b](../../audio/sfx/concept/rejected/overcharge-end-r37-b.ogg) | — | a / b / redo | **a** (user); b in `concept/rejected/` |
| S.12 | [Cooldown ready chime](../../audio/sfx/README.md#concept-art) | **pick a or b**: a "two soft bells", A5 then E6; b "glass fifth", one soft C6–G6 chord (0.5 s) | [a](../../audio/sfx/concept/cooldown-ready-r37-a.ogg), [b](../../audio/sfx/concept/rejected/cooldown-ready-r37-b.ogg) | — | a / b / redo | **a** (user); b in `concept/rejected/` |
| S.13 | [Dash](../../audio/sfx/README.md#concept-art) | **pick a or b**: a "thruster puff", a jet noise burst sweeping 600 → 2500 → 900 Hz over a short roar; b "rocket kick", two afterburner recordings (0.35 s) | [a](../../audio/sfx/concept/dash-r37-a.ogg), [b](../../audio/sfx/concept/rejected/dash-r37-b.ogg) | — | a / b / redo | **a** (user); b in `concept/rejected/` |
| S.14 | [Drone break](../../audio/sfx/README.md#concept-art) | **pick a or b**: a the metal hit and a `tiny` blast with sparks (0.49 s); b "crunch", a metal impact and a small explosion (0.63 s) | [a](../../audio/sfx/concept/drone-break-r37-a.ogg), [b](../../audio/sfx/concept/rejected/drone-break-r37-b.ogg) | — | a / b / redo | **a** (user); b in `concept/rejected/` |
| S.15 | [Drone rebuilt](../../audio/sfx/README.md#concept-art) | **pick a or b**: a "materialise", a soft saw pad easing up a fourth; b "three bells", E5, A5, E6 over a faint sparkle (0.6 s) | [a](../../audio/sfx/concept/drone-rebuild-r37-a.ogg), [b](../../audio/sfx/concept/rejected/drone-rebuild-r37-b.ogg) | b's bells are close to S.12's chime | a / b / redo | **a** (user); b in `concept/rejected/` |
| S.16 | [Lance Control's voice](../../audio/voice/README.md#concept-art) | **pick a or b** (`tools/concept/audio/tts_r37.py`, through radio filter b): the line "Lance uplink locked. Firing!"; a reader Ted Lienhart (132 Hz, 2.06 s); b reader Celine Major (159 Hz, 2.62 s, rising to ≈ 350 Hz on "Firing!"). Until the pick the game shows the line as text with the radio blip | [a](../../audio/voice/concept/rejected/voice-lance-control-r37-a.ogg), [b](../../audio/voice/concept/voice-lance-control-r37-b.ogg) | through the filter the recogniser hears "Lands up, link" (a) and "Plants up, link" (b): "uplink" may blur for a listener too | a / b / redo | **b** (user): Celine Major, cast and rendered in step Q2; a's audition and clip in `concept/rejected/` |

## Captures

The `installDist` build on a private display, the game's own sound (its `wave` mix), on Act 1–2
levels with the part-A loadout; how each was taken is in its folder's `concept/prompts.md`.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| K.1 | [Ion Beam, drones, Deflector Pod](../../player/weapons/README.md#concept-art) | **game capture**: the beam's contact flare and heat tint on a tough target, the drones orbiting, firing, breaking and rebuilding with the HUD hits strip, the Deflector Pod catching bullets and its charges returning (L05 and L07; 53 s with sound and a sheet of 18 frames) | [arsenal-capture-r37-a.png](../../player/weapons/concept/arsenal-capture-r37-a.png), [.mp4](../../player/weapons/concept/arsenal-capture-r37-a.mp4) | scratch data: the drone breaks after 2 hits (with the real 8 it was hit 3–4 times in two hard minutes and never broke); the beam findings are [R.4](#build-choices-and-findings) | accept / change | **accepted** (user) |
| K.2 | [Specials](../../player/specials/README.md#concept-art) | **game capture**: the EMP Burst's ring and the crackle on a stunned machine and a Vrell unit, the Orbital Lance with Lance Control's line, the Shield Overcharge's shell and white cores, then its cooldown ring through to `READY`; the Smart Bomb still working (L05, L07, L09, L14; 46 s) | [specials-capture-r37-a.png](../../player/specials/concept/specials-capture-r37-a.png), [.mp4](../../player/specials/concept/specials-capture-r37-a.mp4) | the machines are Needlers made machines in scratch data (no Act 1–2 unit is one); the READY flash lasts one rendered frame ([R.4](#build-choices-and-findings)) | accept / change | **accepted** (user) |
| K.3 | [Dash](../../player/systems/README.md#concept-art) | **game capture**: the dash by the button and by a double-tap, its afterimages and burst, the dash pip emptying and refilling, a dash refused while it cools (L05 hard; 19 s) | [dash-capture-r37-a.png](../../player/systems/concept/dash-capture-r37-a.png), [.mp4](../../player/systems/concept/dash-capture-r37-a.mp4) | the pip's white "ready" state lasts one rendered frame ([R.4](#build-choices-and-findings)) | accept / change | **accepted** (user) |
| K.4 | [Hangar, Act 3](../../ui/hangar/README.md#concept-art) | **game capture**: the third utility bay bought and its `UTL` tile, the Act 3 shop rows (four weapons, three specials, the Evasive thrusters), the Shield Overcharge bought (before Level 19, a hand-written save; 97 s) | [act3-hangar-capture-r37-a.png](../../ui/hangar/concept/act3-hangar-capture-r37-a.png), [.mp4](../../ui/hangar/concept/act3-hangar-capture-r37-a.mp4) | the bay's own SELL row is not shown (the run ended first); the shop findings are [R.4](#build-choices-and-findings) | accept / change | **accepted** (user) |

## Build choices and findings

What the steps chose where the documents are silent, and what they found. **B** rows: each item
*yes* (keep as built) or *no* (say what). **R** and **V** rows: as their answer cell says.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.1 | [Beam, drones, Deflector Pod in the simulation](../../player/weapons/README.md) | **yes or no, each** (step A1): (a)–(f) below | — | (d) is visible only on hard; (a) makes a beam on a trigger far stronger than a gun on one | all yes / some no | **all yes** (user) |
| B.2 | [Specials and the dash in the simulation](../../player/specials/README.md) | **yes or no, each** (step A3): (a)–(g) below | — | no Act 1–2 unit is a machine, so (a)–(c) were checked on test units and in the capture's scratch data | all yes / some no | **all yes** (user) |
| B.3 | [Data, shop and balance](../../player/balance-plan.yaml) | **yes or no, each** (steps A2, A4): (a)–(d) below | — | — | all yes / some no | **all yes** (user); (a) superseded by R.1's re-plan (step Q1) |
| B.4 | [Hangar](../../ui/hangar/README.md#decisions) | **yes or no, each** (step A5): (a)–(e) below | [K.4](#captures) | (a) puts the tile far from the other two | all yes / some no | **all yes** (user) |
| B.5 | [In flight and the HUD](../../ui/hud/README.md#decisions) | **yes or no, each** (step A6): (a)–(h) below | [K.1–K.3](#captures) | — | all yes / some no | **all yes** (user) |
| B.6 | [Sounds in flight](../../audio/sfx/README.md#decisions) | **yes or no, each** (step A6): (a)–(d) below | — | — | all yes / some no | **all yes** (user) |
| R.1 | [The balance's income without level data](../../player/balance-plan.yaml) | **finding: which reading is right?** The gap check said the income fallback for a level without data (Levels 15–21 today) must be `budget(n) × act factor` (≈ 4 600 a level instead of ≈ 1 800). Step A2 found the opposite and built nothing: `budget(n)` is already the typical haul of level *n* in its own act (Act 2's hauls land on it unscaled: L14 1 703 of 1 687), so the plan's Act 3 visits use `budget(n)` as it is (1 805 at L15 → 2 709 at L21) | — | if A2 is wrong the Act 3 plan is about 2.5× too poor, and the beam's 5 000 would be bought far too late; the Act 3 levels' data (part B on) settles it | A2's reading (no change) / apply the act factor | **apply the act factor** (user): income fallback budget(n) × 1.6^(act − 1), the Act 3 purchases re-planned (step Q1) |
| R.2 | [The hard sheet at Level 18](../../player/weapons/ion-beam/README.md) | **finding**: on hard (income × 0.9) the plan cannot afford the Ion Beam at the L18 visit and stays at 77 forward DPS (medium: the beam at L2 at L18, 80, then 100 from L19). Nothing changed: hard is meant to be tighter (2026-10-05) | — | the reference DPS rises to 88–100 meanwhile, so a hard player is 10–20 % short in L18–L21 until the beam | accept / change (say what) | **accepted** (user); moot since R.1's re-plan (hard buys the beam at L17) |
| R.3 | [The Deflector Pod's data layout](../../player/weapons/deflector-pod/README.md#decisions) | **finding**: the field's numbers (`radius`, `charges`, `recharge`) sit in each of its five `levels` and its `overdrive` under `hits: deflector`, not in a separate `deflector` block as the gap check had it, so every weapon keeps five levels and an overdrive for the shop's prices and draws | — | — | accept / change | **accepted** (user) |
| R.4 | [What the captures found](#captures) | **findings: fix or keep, each** (below; step A8 fixed all but (c), which waits for your call) | [arsenal sheet](../../player/weapons/concept/arsenal-capture-r37-a.png), [hangar sheet](../../ui/hangar/concept/act3-hangar-capture-r37-a.png) | (c) may be the approved rule (hardened ground passed over; the Carrier's first phase may be untargetable) rather than a bug; (f) was seen with a save over its power cap | fix all / some (say which) | **fix all** (user): (a), (b), (d)–(g) as fixed in step A8; (c) fixed in step Q1 (`PlayerFire.cast`'s `CAST_ARMOUR`: a set piece's armour stops the beam on every layer with the glance, its damageable parts burn; `IonBeamBossesTest`) |
| V.1 | [Save format 4](../../systems/saves/README.md#decisions) | **accept or change** (Q1 = a, built in step A2): a `bays` field (the utility bays bought, 0 or 1); a version 3 save is migrated with 0; an owned cooldown special is held in `specials` with 1 (no format change for it); `SaveFormatTest` covers the migration | — | the M6 release's saves are version 3: the migration is the only path they take | accept / change | **accepted** (user) |

**Findings (row R.4)**, seen in the captures and not fixed (Claude's reading):

- **(a)** The heat tint on a Gorgon Frigate tentacle shows as a flat pure-white silhouette for over a
  second, more a blink than a steady tint.
- **(b)** On the frigate's hull the column ends without a contact flare.
- **(c)** The column runs on past the Brood Carrier in its first (high-air) phase and past the Siege
  Spire (Level 14, t 114) instead of stopping on them.
- **(d)** The cooldown ring's white READY flash and the dash pip's white state count rendered frames
  (12 and 10), so at an uncapped 460 fps they last about 20–25 ms; they should count time.
- **(e)** In the hangar the Shield Overcharge shows `SELL 5 000` right after buying (the visit's undo
  refund, probably, not the 60 % sell-back) and an empty `VS FITTED:` line.
- **(f)** The EMP Burst's FIT was refused with "NEEDS 1 MW MORE POWER" at 0 MW draw, while the
  Overcharge fitted in the same state (the scratch save was over its 14 MW cap).
- **(g)** "1 FREE EMP BURST CHARGE" stays on the message line on every slot until the next
  transaction; after buying the bay the cursor lands on the locked IFF spoofer row.

**Fixed in step A8 (row R.4)**: all but (c), which is left as built for your answer (the approved
rule may well pass the Carrier's first phase and the Spire). Not yet seen in a new capture; each has a
test (`ArsenalHudTest`, `ActThreeHangarTest`).

- **(a)** The head of the Gorgon Frigate the beam burns now takes the steady heat tint every other
  target gets instead of the white hit flash, which the beam renewed every step.
- **(b)** Where something the beam cannot damage stops it (the frigate's closed hull, a shielded
  part), the column now ends at its edge (not bored 4 px in) in the glance spark, the "glance at its
  edge" of B.1 (b); a burn's contact flare would read as damage, so it is not used there.
- **(d)** The READY flash and the dash pip's white state now last 0.2 s of game time (12 simulation
  steps) whatever the frame rate; the dash pip's was 10 frames, it now matches the ring's 0.2 s.
- **(e)** The `SELL 5 000` is the designed full refund of something bought during the same visit (the
  sale's dialog says so; a later visit sells it at the 60 % share). The detail now says so too
  (`BOUGHT THIS VISIT: FULL REFUND`), and the `VS FITTED:` line is left out when nothing differs
  (the Overcharge shares no stat with a charge special and draws the same 0 MW).
- **(f)** Specials draw no power; the refusal came from the hand-written save, already 1 MW over
  its generator (15 / 14 MW): every fit was checked against the cap, even one adding nothing, while
  buying the Overcharge was not. Now a change is refused for power only when it widens the gap, so an
  over-cap loadout (a hand-written save, or one from before a draw changed) can still fit a special
  or swap to a lighter item; a change that adds draw stays refused.
- **(g)** The visit's notice (the free charges) leaves the message line on the next move or slot
  change; a transaction's message still stays until the next transaction. After buying a bay the
  cursor goes to the bay's new slot (its `UTL` tile), on its first row.

**Build choices (row B.1)**, each *yes* (keep) or *no* (say what):

- **(a)** A beam on a trigger (which counts hits, not damage) counts one hit per 0.1 s of contact,
  the first at once: 10 a second, like a fast gun.
- **(b)** The beam stops at something it cannot damage above the ground layer (an armoured
  set-piece body or neck, a shielded part, a unit that takes no damage now) and shows a glance at
  its edge; hardened ground is passed over.
- **(c)** A rebuilt drone fades in over 0.3 s moving from its bay to its orbit; while it fades in it
  neither fires nor blocks, and nothing hits it.
- **(d)** A mortar blob that would land on the ship and lands in a field with a charge left is
  caught instead of hitting it (its ring of bullets still forms).
- **(e)** A bullet is caught when its centre enters a field; one on the ship's centre line goes to
  the pod with more charges left (the left one on a tie).
- **(f)** During an overdrive a pod uses the next level's radius and interval, its cap rises to the
  next level's charges with the new charges empty; when the overdrive ends the charges above the
  normal cap are lost.

**Build choices (row B.2)**, each *yes* (keep) or *no* (say what):

- **(a)** A stunned unit's own clock stops: it fires nothing and its timers wait; a unit on a path
  drifts on with its velocity and resumes its path shifted by the drift, without jumping back.
- **(b)** A chain stops whole and flies on after its stun; a latched Lamprey stays latched and its
  drain pauses.
- **(c)** A new burst restarts a running stun (no stacking); bosses, mid-bosses, set-piece parts and
  `sub` units are never stunned; the aura and shield strip does nothing until the Choir Herald's
  `aura` (part D).
- **(d)** A cooldown special is ready at every attempt's start, a retry and a boss checkpoint; a
  press up to 0.1 s before it is ready fires it when it is; a special charge pickup takes 10 s off
  a running cooldown and nothing when it is ready.
- **(e)** The Shield Overcharge ends at an attempt's start; a shield cell picked up during it fills
  only to the raised capacity.
- **(f)** The dash goes in the direction held, straight ahead with none; a double-tap counts only
  that direction pressed, released and pressed again within 0.25 s with nothing else pressed
  between; a dash asked for while it cools does nothing, silently.
- **(g)** `Command.DASH` is the last command bit, and every new state is hashed only when its
  equipment is fitted, so recordings and the Level 01 golden replay are unchanged.

**Build choices (row B.3)**, each *yes* (keep) or *no* (say what):

- **(a)** The balance plan from L15: one EMP charge on top of the free one with the Airstrike fitted
  again (the EMP charges wait for a level with machines), the generator Mk III at L16, saving at
  L17, the Ion Beam bought at L2 at L18 with the Pulse Cannon sold, L3 at L19, the Tail Gun L3 at
  L20, Composite III at L21.
- **(b)** `--loadout` fits a cooldown special with one "charge" (it is owned once); the debug
  option `utility3=` counts the third bay bought.
- **(c)** A second set of Evasive thrusters in the other bay adds nothing.
- **(d)** The EMP Burst counts as nothing for a level's `required` traits; the Orbital Lance counts
  as `anti-ground` (it burns hardened targets).

**Build choices (row B.4)**, each *yes* (keep) or *no* (say what):

- **(a)** The third bay's `UTL` tile sits in the schematic's lower right corner, opposite Rook's
  callout (the module row has no room for an eighth 48 px tile).
- **(b)** The bay is bought from a bay row at the end of the utility slots' shops and sold from its
  own slot's shop (refused while a module is fitted), and is not listed before the L15 visit (no
  locked row).
- **(c)** The Act 3 specials' tiles read `EMP`, `LANCE` and `OVRCHG`; the drone bay's `DRONE BAY`.
- **(d)** The Deflector Pod's test fire has the dummies fire a slow (110 px/s) harmless bullet at
  the ship every 0.6 s, so the catch and the charge return show.
- **(e)** A test-fire dummy stands one second of the L1 Ion Beam.

**Build choices (row B.5)**, each *yes* (keep) or *no* (say what):

- **(a)** The cooldown ring is 28 dots round the 16 px icon: dim cyan while it cools, cyan when
  ready, white for 12 frames as it gets there; the overcharge's white arc drains anticlockwise over it.
- **(b)** The dash pip is a 16×6 cell right of the shield's label plate, filling dim cyan over the
  cooldown, cyan when ready, white for 10 frames as it gets there; the hull shows every other two
  steps while the dash makes it invulnerable, with three fading afterimages.
- **(c)** Until the walls of Level 16 the beam's rock-scorch flare and scorch marks show where it
  burns a ground object, and the scorch marks under a ground unit too.
- **(d)** The EMP ring fades to 65 % over its run; the crackle comes in three sizes by the unit's
  size (under 32 px, under 64 px, above); a stunned Vrell unit's glow drops to 35 % and a
  machine's lights go out.
- **(e)** The lance's scorch columns fade over 4 s, their embers over 1.2 s.
- **(f)** The overcharge's shell fades to half over the excess's bleed.
- **(g)** The Orbital Lance's call is Lance Control's line, urgent like Hammer Lead's (the call is
  picked by the special's kind), shown as text with the blip until the voice is chosen (S.16).
- **(h)** The Options screen's Controls tab shows the Dash's primary slot as the fixed
  `DOUBLE-TAP`; V and the left bumper stay remappable.

**Build choices (row B.6)**, each *yes* (keep) or *no* (say what):

- **(a)** The beam's start plays with its loop, not before it; the stop at the release.
- **(b)** The impact sizzle plays at its file's −38 dB under the loop.
- **(c)** The EMP's power-down plays once a step for what the ring reaches: a machine's at the
  explosions' level, a Vrell unit's 6 dB lower.
- **(d)** A drone's absorbed hit plays the metal hit 12 dB under the hits.

## Decisions

- 2026-10-13: opened (M7 part A, step A7): the part's final art, a/b picks, sounds, captures, build
  choices and findings, pending the user's review.
- 2026-10-13 (user, round 37): closed: every row answered as in its *Outcome* cell; R.1 and R.4 (c)
  built in step Q1, the sound, Orbital Lance and Lance Control picks in step Q2, the rejected files
  moved and the generators routed in step Q3.
