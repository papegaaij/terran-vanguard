---
title: Concept round 41 — M7 part E (Level 18), Level 17's fixes, the Act 2 finish
design: approved
implementation: n/a
art: chosen
depends-on: [../../campaign/act-3-red-dust/level-18-dust-storm, ../../enemies/air/ghost-drone, ../../enemies/ground/dust-devil, ../../campaign/act-3-red-dust/level-17-dome-siege, ../../campaign/act-2-homefront, ../../audio/music, ../../audio/sfx, ../../audio/voice, ../../tech/roadmap, ../round-40]
updated: 2026-10-19
---

# Concept round 41 — M7 part E (Level 18), Level 17's fixes, the Act 2 finish

## Summary

Four things wait for an answer, all built while you were away (every row is pending your review):

1. **M7 part E, Level 18 Dust Storm** ([roadmap](../../tech/roadmap/README.md#m7-parts)): the final art, 7 sounds,
   track 9 "Grey Signal" with its motif stem, 19 voices, the game capture and the build choices (`F`, `S`, `M`, `V`,
   `K`, `B`, `R` rows). Built on your two decisions of 2026-10-19 (Level 18 meets the Act 2+ floor; the beam era is
   "tougher, not more"). **R.a and R.e are the open points**: hint 1 is not alone on the screen, and the level is
   hard on a blind pilot (the shots a minute are about twice Level 10's).
2. **The dome-break redo** (`S.h`, round 40 S.b): pick b or c (or redo).
3. **Level 17's round 40 fixes** (`L.a`, step D6): what was built for your capture note and R.a-R.e.
4. **The Act 2 finish** (`P` rows): the Kraken and Spire fights in the data (CB), the Act 2 waves (WA, WB), the
   new voices (VOX2), the debrief music wiring (MW), the intel panel (IP) and the replay hangar's refit-only rule
   (FLOW2, your correction).

Records, nothing to answer: your hard-tolerance decision and Levels 15-17 still below the new pacing rules (last
section).

**Part E in short.** The [Level 18](../../campaign/act-3-red-dust/level-18-dust-storm/README.md) storm: a look only
(the visibility radius around the ship with look b's soft ragged falloff, ground and low-air gone outside it, air
units as silhouettes with their glow, bullets always drawn); the [Dust Devil](../../enemies/ground/dust-devil/README.md)
(closed and armoured on the ground, rising and opening on low-air with its core hittable, the grit spiral, the pull on
the ship and the pickups, held pickups released at its death); the [Ghost Drone](../../enemies/air/ghost-drone/README.md)
(down to a lane, a strafe across, one burst told by a red eye glint, hard's second pass; hint 1 at t=69.5); the plains
backdrop with the rover-wreck secret; track 9 with Vorne's motif layer from hint 1; the beam-era HP (small units ×2
from Level 18).

Open [index.html](index.html) in a browser (regenerate with
`python3 tools/concept/board.py 41 40 --subjects ghost-drone-final,dust-devil-final,backdrop-final,l18-props-final,intel-final,briefing-images-final,devil-spin,devil-open,devil-spiral,ghost-drone-drop,ghost-drone-glint,ghost-drone-burst,ghost-drone-death,grey-signal,grey-signal-base,grey-signal-motif,grey-signal-motif-mix,grey-signal-seam,grey-signal-base-seam,grey-signal-motif-seam,music,dome-break,level-18-capture,level-18-crops`;
`41 40` brings in round 40's dome-break files, and with them round 40's `backdrop-final` (Level 17's), `intel-final`
and `briefing-images-final` sections, already final: round 41's are the r41 ones). Each row carries a recommendation; **B** rows are *yes* (keep as built) or *no* (say what).

## In the game

`./gradlew :desktop:run --args="..."` with `--settings <file>` (a debug run writes no save):
- Level 18 with the balance plan's ship: `--start level --level 18 --loadout front=ion-beam:2,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:2,generator=2,shield=1,armor=2 --special airstrike:2 --escort rook:autocannon:1`
  (`--difficulty easy|hard`; `--special emp-burst:2` to stun the drones and stop a devil's pull);
- Level 17 after the fixes: the same with `--level 17 --loadout front=pulse-cannon:4,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:2,generator=2,shield=1,armor=2`;
- Levels 11 and 14 (the reworked Kraken and Spire): `--level 11` / `--level 14` with your Act 2 ship.

## Art — final

Production art from round 36's picks (`tools/art/`, step ART-E), in the game now.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.a | [Ghost Drone](../../enemies/air/ghost-drone/README.md#concept-art) | **final art** (`tools/art/storm_units.py`, the chosen facet dart a with the U.5 lamps): 16 headings, the additive eye and wing-tip glow, the EMP lights-out set, the 0.3 s glint, sparks and smoke death, in and out of the radius | [sheet](../../enemies/air/concept/ghost-drone-final-r41-a.png), [GIF](../../enemies/air/concept/ghost-drone-final-r41-a.gif) | the stunned (lights-out) set is an extra the brief did not ask for; in the capture the drones read well outside the radius by their eyes | approve as final / redo | approved as final (2026-10-19) |
| F.b | [Dust Devil](../../enemies/ground/dust-devil/README.md#concept-art) | **final art** (`storm_units.py`, the chosen r06 a): closed spin, the 0.4 s rise, open with the lime core, core glow, grit swirls (loose and tight), collapse, death light, grit cloud | [sheet](../../enemies/ground/concept/dust-devil-final-r41-a.png), [GIF](../../enemies/ground/concept/dust-devil-final-r41-a.gif) | the tight swirl (the rise tell) is an extra set; the grit bullets are the standard orb (no sprite of their own: the reserved bullet colours read in the dust) | approve as final / redo | approved as final |
| F.c | [Level 18 backdrop](../../campaign/act-3-red-dust/level-18-dust-storm/README.md#concept-art) | **final art** (`tools/art/backdrop_l18.py`): the northern plain (rust flats, dry channels, the half-buried pipeline), the storm plain under dust sheets, the eye's dry lake with the storm's walls, the pipeline ridges under the nests, Ares Landing's outskirts at the end; dust banks and streaks; the front (t≈28) and the back wall (t≈130) passing over; the veil and tongue textures of the storm look | [sheet](../../campaign/act-3-red-dust/level-18-dust-storm/concept/backdrop-final-r41-a.png), [GIF](../../campaign/act-3-red-dust/level-18-dust-storm/concept/backdrop-final-r41-a.gif) | **no deep layer**: the storm wall of section 1 and the eye's open sky are not there (the front is a high-air piece at ≤ 50 %, the back wall ≤ 60 %), and in the capture the front reads as a soft haze, not a wall (R.d); the high-air layer at 2.0 (E2's placeholder had 2.2) | approve as final / redo | **redo**: the user saw a horizontal line where the dust ends abruptly at about 5 s in the loop (the backdrop or the loop?); FXS fixes it, the art stays `chosen` until then |
| F.d | [Level 18 props](../../campaign/act-3-red-dust/level-18-dust-storm/README.md#concept-art) | **final art** (`storm_units.py`): the militia rover wreck half buried (intact, hit, destroyed), its red beacon, the pod popping | [sheet](../../campaign/act-3-red-dust/level-18-dust-storm/concept/l18-props-final-r41-a.png), [GIF](../../campaign/act-3-red-dust/level-18-dust-storm/concept/l18-props-final-r41-a.gif) | not seen in the capture: the beam shot it at the top of the screen, out of the radius (R.c) | approve as final / redo | approved as final |
| F.e | [Intel pictures](../../ui/hangar/README.md#concept-art) | **final art** (`tools/art/intel.py`): the Dust Devil's and the Ghost Drone's portraits and the `unclassified` sensor picture shown until the drone has been flown | [sheet](../../ui/hangar/concept/intel-final-r41-a.png) | — | approve as final / redo | approved as final |
| F.f | [Briefing images](../../ui/briefing/README.md#concept-art) | **final art** (`tools/art/briefing_images.py`): `level-18-storm-front` (Okafor's page: the front over the plains, the six sections) and `level-18-visibility` (Varga's page: the sensor scan, glow not shapes) | [sheet](../../ui/briefing/concept/briefing-images-final-r41-a.png) | Varga's image hides the drone (no human-built shape before hint 1) | approve as final / redo | approved as final |

## Music

Track 9 "Grey Signal", the Act 3 B theme (round 36 S.1 = b), at full length with its three stems (step MUS-E,
`tools/concept/audio/music_r41.py`), in the game now (step EG1). **Not listened to by Claude** (analysis only).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| M.a | [Track 9 "Grey Signal"](../../audio/music/README.md#concept-art) | **accept or redo**: 125 BPM, D minor dusty dub techno, 180.7 s with a loop of 88 bars; three sample-aligned stems: the **full mix** (sections 5 and the climax), the **base** stem (kick, sub, pad, hand drums, the wind; sections 2, 4 and 6) and the **motif** stem (Vorne's motif as an intercepted radio whistle, once every 8 bars; from hint 1 at t=69.5, off in the eye). **Recommendation: accept** | [full mix](../../audio/music/concept/grey-signal-r41-a.ogg), [base](../../audio/music/concept/grey-signal-base-r41-a.ogg), [motif](../../audio/music/concept/grey-signal-motif-r41-a.ogg), [as Level 18 plays them](../../audio/music/concept/grey-signal-motif-mix-r41-a.ogg), seams: [full](../../audio/music/concept/grey-signal-seam-r41-a.ogg), [base](../../audio/music/concept/grey-signal-base-seam-r41-a.ogg), [motif](../../audio/music/concept/grey-signal-motif-seam-r41-a.ogg), [review sheet](../../audio/music/concept/music-r41-a.png) | the base stem is only 0.4 LU under the full mix (the change at section 5 is small); the stab's resonance (q 1.1) may read sharp; the motif every 8 bars may feel frequent | accept / redo (say what) | accepted |

## Sounds

Part E's sounds ([sfx](../../audio/sfx/README.md#concept-art), `tools/concept/audio/sfx_r41.py`, all synthesized,
step AUD-E), **one variant each**, in the game now (step EG2). Not listened to by Claude: checked by envelopes,
spectrograms, levels and loop seams; no bubbly or popping layers. **S.h** is round 40's dome-break redo.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.a | [Devil spin](../../audio/sfx/README.md#concept-art) | a whirling wind roar with grit, a seamless 3 s loop, the nearest Dust Devil | [a](../../audio/sfx/concept/devil-spin-r41-a.ogg) | — | accept / redo | accepted |
| S.b | [Devil open](../../audio/sfx/README.md#concept-art) | the vanes parting: a gust, five scrapes and a low hollow tone (0.7 s); played at the rise, so it ends as the core opens | [a](../../audio/sfx/concept/devil-open-r41-a.ogg) | — | accept / redo | accepted |
| S.c | [Devil spiral](../../audio/sfx/README.md#concept-art) | the grit spiral's turning hiss with sand ticks (0.45 s) | [a](../../audio/sfx/concept/devil-spiral-r41-a.ogg) | — | accept / redo | accepted |
| S.d | [Ghost Drone drop](../../audio/sfx/README.md#concept-art) | a clean turbine whine descending into its lane (0.9 s) | [a](../../audio/sfx/concept/ghost-drone-drop-r41-a.ogg) | — | accept / redo | accepted |
| S.e | [Ghost Drone glint](../../audio/sfx/README.md#concept-art) | the 0.3 s eye glint: an electronic lock tick, two pings | [a](../../audio/sfx/concept/ghost-drone-glint-r41-a.ogg) | — | accept / redo | accepted |
| S.f | [Ghost Drone burst](../../audio/sfx/README.md#concept-art) | three dry precise shots, 65 ms apart (human guns, not Vrell) | [a](../../audio/sfx/concept/ghost-drone-burst-r41-a.ogg) | below its target level (the ceiling limits it); one file of 3 shots for the easy 2 and hard 4 shots, its 65 ms spacing tighter than the burst's 0.15 s | accept / redo | accepted |
| S.g | [Ghost Drone death](../../audio/sfx/README.md#concept-art) | a dry bang, ringing metal pieces and sparks, no fireball rumble | [a](../../audio/sfx/concept/ghost-drone-death-r41-a.ogg) | below its target level | accept / redo | accepted |
| S.h | [Dome break, the redo](../../audio/sfx/README.md#concept-art) | **pick** (round 40 S.b: "It needs a rumble. Look in the online database for cave in or collapse sounds."; step AUD-S, Freesound CC0, in [CREDITS](../../../CREDITS.md)): **b** a collapse with glass shatter and a 55 → 38 Hz sub thump (2.4 s); **c** a low-passed building collapse over a crust rumble with glass smash (2.4 s). The game still plays a until you pick. **Recommendation: c** (the longer rumble under the glass is the cave-in you asked for; b's thump is shorter) | [a (round 40)](../../audio/sfx/concept/rejected/dome-break-r40-a.ogg), [b](../../audio/sfx/concept/rejected/dome-break-r40-b.ogg), [c](../../audio/sfx/concept/dome-break-r40-c.ogg) | not listened to; both at the explosion level | b / c / redo | **c** (FXS wires it; a and b move to `rejected`) |

## Voices

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| V.a | [Level 18's radio and briefing](../../campaign/act-3-red-dust/level-18-dust-storm/README.md#radio-chatter) | **accept or redo** (step VOX-E, `tools/art/voice.py` through the radio filter, the pages dry): 19 files, the 14 timed lines (Rook's four only with him), the rover's, the secondary's and the level end's lines and the two briefing pages; six takes pinned; Varga's hint line "That's not Vrell. That signature is... ours?" pinned to its own seed as the voice design asks | the files under `assets/voice/` (listed in [voice](../../audio/voice/README.md)); [Varga t=71](../../../assets/voice/varga/906ac709736e.ogg), [Varga page 2](../../../assets/voice/varga/9c3fe80acbcb.ogg), [Okafor page 1](../../../assets/voice/okafor/0b7d21583672.ogg), [Okafor t=78](../../../assets/voice/okafor/487c90a16e48.ogg) | **listen**: Varga's t=71 beat before "ours?" is 0.18 s (seed 204667219, the runner-up, has 0.24 s); the rover's "Militia supply pod" is heard as "Malicious supply pod" by the speech check in all seven seeds; Varga's page 2 jumps high on "not shapes" (every seed does; the pin is the shortest); Okafor's page 1 ("Sweep it", once "Sweet pick") and t=78 ("Later, Doctor.") re-rendered | accept / redo (say which line) | accepted |

## Captures

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| K.a | [Level 18](../../campaign/act-3-red-dust/level-18-dust-storm/README.md#concept-art) | **game capture** (step E6, `concept/prompts.md`): a whole medium run with the plan's L18 ship and Rook, **with `--invulnerable`** (two runs without it lost the armour at t≈80 and t≈95: R.e), 230 / 301 kills (76 %), the rover's secret, the kill-ratio secondary met; then a no-fire run parked left of centre for the pull. Seen: the plains and the front (t 26-33), the radius closing in (t 33-36), the Dust Devils closed and opening with the spiral (t 42-46), the ship pulled about 60 px into the gap between them (run 2), hint 1's Ghost Drones with an eye glint (t≈71), the eye's clear air and its devil holding the supply drone's armour patch, the pickups released at its death (t≈127.5), the back wall with a devil's core glowing through the veil (t 142), rear and side warnings, the clearing with Ares Landing ahead, the debrief. Plus readability crops at 1:1 and 2× | [sheet](../../campaign/act-3-red-dust/level-18-dust-storm/concept/level-18-capture-r41-a.png), [video](../../campaign/act-3-red-dust/level-18-dust-storm/concept/level-18-capture-r41-a.mp4), [crops](../../campaign/act-3-red-dust/level-18-dust-storm/concept/level-18-crops-r41-a.png) | the findings R.a-R.e below; the radius does not read as a ring (by design soft), so "half blind" is mild: the veil is light and the air units read at every distance; the HUD's KILLS n / 196 is the secondary's required count (65 % of 301), not the level's total | accept / change | **change**: at 24 s of the part 2 recording the Dust Devil disappears for a second (FXS fixes it; the capture is redone after) |

## Build choices and findings

**B** rows: each item *yes* (keep as built) or *no* (say what). **R** rows: as their answer cell says. Your two decisions
of 2026-10-19 are built as given and recorded here, not asked again: **Level 18 meets the Act 2+ pacing and floor rules**
(a breather by threat, not by count) and the beam era is **"tougher, not more"** (B.e).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.a | [The gap check's defaults](../../campaign/act-3-red-dust/level-18-dust-storm/README.md) | **yes or no, each** (gap check, built in E1-E5, G1-G2): (a)-(g) below | [K.a](#captures) | — | all yes / some no | all yes |
| B.b | [The Dust Devil in the simulation](../../enemies/ground/dust-devil/README.md) | **yes or no, each** (step E1): (a)-(k) below | [K.a](#captures) | — | all yes / some no | all yes |
| B.c | [The Ghost Drone in the simulation](../../enemies/air/ghost-drone/README.md) | **yes or no, each** (step E3): (a)-(d) below | [K.a](#captures) | — | all yes / some no | all yes |
| B.d | [Level 18's data](../../campaign/act-3-red-dust/level-18-dust-storm/README.md#waves) | **yes or no, each** (steps E2, E4): (a)-(j) below | — | — | all yes / some no | all yes |
| B.e | [The beam-era HP](../../enemies/README.md) | **yes or no, each** (step E5, your "tougher, not more"): (a)-(f) below. **Recommendation: yes**, with R.e in mind | [K.a](#captures) | the price of units that live long enough to shoot back: 46.5 enemy shots a minute at medium, about twice Level 10's 22 (hard 77.5) | all yes / some no | all yes |
| B.f | [In the game](../../campaign/act-3-red-dust/level-18-dust-storm/README.md#implementation) | **yes or no, each** (steps EG1, EG2, ART-E, MUS-E): (a)-(m) below | [K.a](#captures) | — | all yes / some no | all yes |
| R.a | [Hint 1 not alone](../../campaign/act-3-red-dust/level-18-dust-storm/README.md#waves) | **finding** (the capture, t 69.5-72.5): the t=67.5 Needler line (3, now 24 HP with the beam era) is still on the screen when the three Ghost Drones enter, so their first sight shares the screen with Vrell air. `Level18Test` checks the quiet window by wave start times only. **Recommendation: move the t=67.5 line to t≈64** (or drop it) and let the test check the enemies on the screen | [K.a](#captures) (t 69-73), [crops](../../campaign/act-3-red-dust/level-18-dust-storm/concept/level-18-crops-r41-a.png) | — | move / drop / keep | keep (as built) |
| R.b | [Silhouettes in the dust](../../campaign/act-3-red-dust/level-18-dust-storm/README.md#visibility-in-the-storm-m6--a) | **finding**: outside the radius the air units keep most of their detail against the light veil (the Needlers' white legs, the Stingers' red), so the "glow, not shapes" of the briefing is mild; the Ghost Drones read by their eye and tips as designed. **Recommendation: darken the silhouette pass** (flatter, darker shapes outside the falloff) and re-capture | [crops](../../campaign/act-3-red-dust/level-18-dust-storm/concept/level-18-crops-r41-a.png) | — | darken / keep | **change**: the silhouettes should share the dust's colour, the ships' lights poking through; darker would read as dark ships, not silhouettes (FXS) |
| R.c | [The rover's beacon](../../campaign/act-3-red-dust/level-18-dust-storm/README.md#secrets-and-pickups) | **finding**: the beam reaches the whole column, so a ship at the left edge shoots the rover as it enters the top of the screen, out of the radius: the crate pops (t≈90.5) but the beacon is never seen. Everything stays hittable (M6 = a). **Recommendation: keep** (a player flying to the edge to look sees it; the scanner's glint shows it at any range) | [K.a](#captures) (t 88-91) | — | keep / move the rover lower or later | keep |
| R.d | [The storm wall](../../campaign/act-3-red-dust/level-18-dust-storm/README.md#layout) | **finding**: with no deep layer the storm wall standing on the plain (section 1) is not drawn, and the front passing at t≈28 reads as a soft haze (F.c). **Recommendation: keep for now**; a darker billowing edge on the front piece if you miss the wall | [K.a](#captures) (t 20-34) | — | keep / a stronger front / a deep layer with the wall | keep |
| R.e | [Hard on a blind pilot](../../campaign/act-3-red-dust/level-18-dust-storm/README.md#difficulty-notes) | **finding**: the capture's scripted ship (random sweeps, no dodging) lost its armour at t≈80 and t≈95 at medium with the plan's fit; Level 17's same driver survived. The autopilot (it dodges) flies every difficulty without a retry. **Recommendation: keep pending your playthrough** (it is the beam era's "shoot back" at work); if it is too hard, lower the Needlers' fire in the storm first | [K.a](#captures) | — | keep / ease the storm's fire | keep |

**Build choices (row B.a)** — the gap check's defaults (each yes/no):
- **(a)** The lane strafe is a simulation movement (not a path), as the Ghost Drone's spec reads.
- **(b)** A blast flag on damage: only the Smart Bomb and the Airstrike are blasts (they hurt a closed devil); the weapons' own blasts and the Orbital Lance glance off its vanes.
- **(c)** Look b's falloff is centred on the radius (clear to R−65, gone at R+55, broken by the tongues): at hard's back wall (130) the pull's edge sits at about half visibility.
- **(d)** The motif stem is off in the eye (section 4) and the base stem plays over the clearing (section 6).
- **(e)** Level 18 is added alone to `small_act_hp` (Levels 15-17 join in their rework).
- **(f)** A spine-turret nest on the ground in the hint-1 window (5 turrets), so the window is not empty.
- **(g)** The autopilot's Level 18 play is its own strategy (closed devils no target, open or rising ones and those late in their closed time first).

**Build choices (row B.b)** — the Dust Devil, step E1 (each yes/no):
- **(a)** The sink after the window is 0.2 s.
- **(b)** The first volley of a window is aimed.
- **(c)** The devil wanders in a swirl and keeps 90 px away from the ship.
- **(d)** An EMP stun pauses the cycle and stops the pull.
- **(e)** Held pickups circle the devil (26 px, a turn in 1.25 s) and their life is paused while held.
- **(f)** The pull works in screen space; a pickup magnet wins a tie with a devil.
- **(g)** One hit box: the core box (the vanes glance shots on it while closed).
- **(h)** A devil pays as a kill (KILLS in the debrief).
- **(i)** Devils never claw convoys (no ally hook).
- **(j)** It is open on low-air, closed on the ground.
- **(k)** Balance: about 4.5 s on the screen, one or two windows each.

**Build choices (row B.c)** — the Ghost Drone, step E3 (each yes/no):
- **(a)** The lanes are squeezed into the 120-220 px band below the top.
- **(b)** The bank into the strafe is 24 px.
- **(c)** Hard's second pass: survivors of a line abreast from the top only, one lane lower.
- **(d)** A burst started before an EMP stun finishes after it.

**Build choices (row B.d)** — Level 18's data, steps E2 and E4 (each yes/no):
- **(a)** The wave plan: the anchors (devils at 41.5, 77, 116.5, 138.5; drones at 69.5, 87.5, 131.5, 145.5, 166.5) filled to a wave about every 2.5 s with firing groups, kite swarms and nests, the quiet windows kept.
- **(b)** The Needler pincers are 2 per edge.
- **(c)** Hard's firing groups are one smaller.
- **(d)** The devil trio is 3; the devils fly as packs, each on its own path.
- **(e)** The devils wander up to 60 px; easy's window is 1.0 s.
- **(f)** The vanes glance on the core box.
- **(g)** The ambience is `mars-dust` in section 1, the eye and the clearing.
- **(h)** The kill-ratio secondary at 65 %, judged at the level end.
- **(i)** The intel's hazard reads "dust storm, half blind" and its setting "Mars, northern plains" ("low visibility" overflowed the panel).
- **(j)** The storm-plain tile rolled 672 rows (a busy band sat at its repeat); the backdrop's high-air factor 2.0.

**Build choices (row B.e)** — the beam-era HP, step E5 (each yes/no):
- **(a)** `beam_era_hp` in the enemies' basis: ×2 on `small` units from Level 18 (the plan's first Ion Beam), on top of the act factor, bosses aside.
- **(b)** The rule: a killed small unit lives about as long on the screen as on Level 10 (1.60 s); `BeamEraHpTest` keeps it in 0.9-1.5 × of that.
- **(c)** Medium units are left out (doubled, a hard devil could not be killed in its window; the Wraith is a rear gun).
- **(d)** Level 18 rebuilt from E2's waves (E4's 605-enemy fill dropped): 301 enemies, about 101 a minute; medium 2.77 on the screen and 46.5 shots a minute, hard 2.93 and 77.5, easy 1.26 and 15.8.
- **(e)** E2's short kite V-wings and streams became Stinger groups of 3.
- **(f)** `bounty_scale` 0.37: the typical haul 2,186 (−1.1 % of the budget 2,211); Levels 15-17 untouched (pulse era).

**Build choices (row B.f)** — in the game, steps EG1, EG2, ART-E, MUS-E (each yes/no):
- **(a)** The storm look is one mask framebuffer with look b's edge and two tongue fields at their drift speeds; the veil drawn after low-air.
- **(b)** The silhouette colour of the air units outside the radius (see R.b).
- **(c)** An open devil's core glows through the veil at 0.45.
- **(d)** The storm's swell is +3 dB times the storm's strength.
- **(e)** Chains (multi-part units) are not silhouetted.
- **(f)** Rook outside the radius is a silhouette with his running lights; pickups glint.
- **(g)** The Ghost Drone's eye and tip glows are drawn in every level, not only in storms.
- **(h)** The devil's open sound plays at the rise.
- **(i)** The burst sound is one 3-shot file for every difficulty (S.f).
- **(j)** The unclassified intel reads "Unclassified small air" and is pictured until the drone is flown; a failed Level 18 does not count as flown.
- **(k)** The devil's spin frames at their set rate (8 frames per 72°).
- **(l)** No deep layer: the front and the back wall as high-air pieces (F.c, R.d); extra stunned and tight-swirl sprite sets.
- **(m)** Track 9: the stab's q 1.1, the base 0.4 LU under the full mix, the motif every 8 bars (M.a).

## Level 17's fixes

What step D6 built for round 40's K.a note ("The second shell creature rolls over a dome and again some time later. That
shouldn't happen. The radio about the shield formation is too late, it is spoken after they are all destroyed. The shell
creatures seem too strong. None are destroyed.") and R.a-R.e. Not captured again.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| L.a | [Level 17's round 40 fixes](../../campaign/act-3-red-dust/level-17-dome-siege/README.md#implementation) | **yes or no, each** (step D6): (a)-(g) below. **Recommendation: all yes** | — | not captured since; the autopilot is chaotic (a 0.5 s move changes its runs), so the dome rates are a rough measure | all yes / some no | all yes |

**Build choices (row L.a)** — Level 17's fixes, step D6 (each yes/no):
- **(a)** K.a / R.c: a Shellback walks between the domes (its body never over a dome's glass, `SiegeChecks`); its roll, once per Shellback, crushes a dome whose concrete ring the ball touches (one hit). The t=46.5 and t=111 Shellbacks walk down the dome's ring lane 90 px to its left, then step aside 50 px short of level; the convoy's `interval: 3` now applies (walkers ignored it).
- **(b)** K.a weaker: body 120 → 90 HP, vent 24 → 16, curl and uncurl 0.6 → 0.4 s (the autopilot kills 40/40 on easy, 37/40 at medium, 30/40 on hard over 8 seeds).
- **(c)** R.a: a shot over the body whose line of flight crosses the vent's box hits the vent (a beam whose column crosses it); the autopilot shoots every vent at medium.
- **(d)** K.a radio: Varga's shield-formation line now waits for the Herald's first ring (a new event, `first-ring`).
- **(e)** R.d: the first Herald's wave comes in at 50 px/s.
- **(f)** R.e: a roll drifting down the screen ends before the scroll carries it below 200 px (`belly_row`), so the belly shows.
- **(g)** R.b: the bombers' row 160 → 220 px and the t=69.5 Mote Swarm at t=69: the domes kept easy 8/8, medium 5/8, hard 5/8.

## Act 2 finish

What was built after round 40 to finish Act 2 (your P.e "yes, all" and A.a). Not flown by you yet.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| P.a | [The Kraken and Spire fights](../../campaign/act-2-homefront/level-11-atlantic-convoy/README.md) | **yes or no, each** (step CB): (a)-(f) below. **Recommendation: all yes** | — | not flown by you; measured with the autopilot | all yes / some no | all yes |
| P.b | [Levels 08, 12 and 13](../../campaign/act-2-homefront/level-13-polar-relay/README.md) | **yes or no, each** (step WA): (a)-(e) below | — | the review sheet `traffic-final-r30-a` still shows the old cars | all yes / some no | all yes |
| P.c | [Levels 11 and 14's waves](../../campaign/act-2-homefront/level-14-siege-spire/README.md) | **yes or no, each** (step WB): (a)-(d) below | — | — | all yes / some no | all yes |
| P.d | [New Act 2 voices](../../audio/voice/README.md) | **accept or redo** (step VOX2): Okafor's Level 11 line at the first telegraph ("The arms are rooted at the rim. Cut a root and that lane is safe!"), Rook's Level 14 t=110 ("Missiles on the roots, I'll keep the flyers off you.") and Varga's Level 14 page 2 (pinned) | [Okafor L11](../../../assets/voice/okafor/752cef155066.ogg), [Rook L14](../../../assets/voice/rook/dab90be8b963.ogg), [Varga L14 page 2](../../../assets/voice/varga/d04034114ff1.ogg) | **listen**: Okafor's rise on "Cut a"; a 0.1 s blip in Varga's page at "Its roots" | accept / redo (say which) | accepted |
| P.e | [The debrief music](../../ui/debrief/README.md) | **yes or no, each** (step MW, track 29 b "Stand Down" as you picked): (a)-(d) below | — | — | all yes / some no | all yes |
| P.f | [The intel panel](../../ui/hangar/README.md) | **fix** (step IP): when a level's intel does not fit, the panel closes its rows from 14 to 12 px, and as a last resort ends with "+N MORE" (Level 14's panel overflowed). **Recommendation: accept** | — | Level 14's intel note in the data is stale | accept / change | accepted |
| P.g | [The replay hangar](../../ui/mission-select/README.md) | **confirm** (step FLOW2, your correction of 2026-10-19: "purchasing items seems odd at this point… restrict this to refitting only"): in a replay's hangar owned items are fitted and unfitted and kept; buying, selling, upgrading and repairs are off (`REFIT` drawer, `REPLAY: REFIT ONLY`) | — | — | as built / change | as built |

**Build choices (row P.a)** — the Kraken and Spire fights in the data, step CB (each yes/no):
- **(a)** The Kraken's head re-measured to 3,400 HP for a fight of about 55 s (1,400 died in 26 s), all ships kept at 0-2 hits.
- **(b)** Hard's telegraph 1.1 s.
- **(c)** Hard's Spire Wraiths: 2 (the maw's Wraiths budgeted at 3 launches of 1).
- **(d)** Varga's Level 14 page trimmed ("Missiles find the root guns while you dodge; bombs crack them."); the weapon hint `homing`.
- **(e)** Level 14's intel L3 "Homing missiles reach every root." (it drops "When the crown opens, look behind you.", which Varga's page keeps).
- **(f)** Rook's Level 14 line at t=110 (the halt eases over t=108-109).

**Build choices (row P.b)** — step WA (each yes/no):
- **(a)** Level 08's cars are ground pieces drawn before the highways (not `overhead`); three towers removed over the t=114 convoy.
- **(b)** Level 13's floes all drift east at 10 px/s as separate pieces, the mortars' floe 40 px up (30 still overlapped), the water no longer drifts.
- **(c)** Levels 12 and 13's waves L10-style: Level 12 0.7 → 2.7 on the screen, 1.1 → 17.7 shots a minute; Level 13 1.1 → 2.7, 17 → 25; `small_act_hp` + 12, 13.
- **(d)** The relay's integrity: medium 260, hard 560.
- **(e)** `bounty_scale` 0.30 (Level 12) and 0.33 (Level 13).

**Build choices (row P.c)** — step WB (each yes/no):
- **(a)** Level 11's waves L10-style: 2.90 on the screen, 14.4 shots a minute, 1 pause over 2 s at medium; three ships afloat on every difficulty.
- **(b)** Level 14's: 2.76 on the screen, 23.3 shots a minute, 1 pause at medium; the root bursts' quiet window t=59-66.
- **(c)** The bosses' bounties so their parts pay what they did: the Kraken 216 → 300, the Spire 372 → 768; `bounty_scale` 0.43 (L11) and 0.40 (L14); `small_act_hp` + 11, 14.
- **(d)** `PacingTest` and the floor leave out the engaged boss fight (a set piece is not an enemy on the screen).

**Build choices (row P.e)** — the debrief music, step MW (each yes/no):
- **(a)** Track 29 at level 0.8 after the jingle (from 5.714 s), looping.
- **(b)** It plays under the act summary too.
- **(c)** It fades over 0.5 s and the screen waits for the fade when you leave.
- **(d)** Leaving early cuts the jingle.

## Records

A record, nothing to answer.

| # | Item | Blocked by | Then |
|---|---|---|---|
| N.a | **Your hard tolerance** (2026-10-19): `expert.hard_under` 0.25: the hard playthrough may end up to 25 % under its bound (Act 2 ends easy +3.5 %, medium +1.2 %, hard −21.6 %); over-earning stays capped at +15 % ([economy](../../systems/economy/README.md)) | — | applied (balance plan, `ActPlaythroughTest`) |
| N.b | **Levels 15-17 below the new pacing rules** (round 40 P.e: "yes, all", 15-17 later): they stay in `PacingTest.PENDING_ACT_TWO_RULE` and out of `small_act_hp` | the rework step, one level at a time (pulse era: Level 18's beam-era HP does not apply) | each leaves the test's exception list when done |

## Decisions

- 2026-10-19: opened round 41 (built while the user was away): M7 part E (Level 18 Dust Storm), the dome-break redo
  (round 40 S.b), Level 17's round 40 fixes (step D6) and the Act 2 finish (steps CB, WA, WB, VOX2, MW, IP, FLOW2). The
  user's decisions of 2026-10-19 are built as given (Level 18 meets the floor; "tougher, not more"; the replay hangar
  refit only; the hard tolerance 0.25). Every other row is pending the user's review.
- 2026-10-19: closed round 41 (the user's answers from the phone page): everything accepted / yes / approved as final
  except three notes. **F.c redo**: "At 5 seconds in the loop, there's a horizontal line where the dust seems to end
  abruptly. Is this caused by the rendering of the loop or an error in the backdrop?" **K.a change**: "at 24 seconds in
  the part 2 recording, the dust devil disappears for a second." **R.b**: "Isn't the idea to show the ships as
  silhouettes through the dust? They currently read like dark versions of the ships. Through the dust, means they should
  share the color of the dust mostly, with the lights poking through. Making them darker will only make them look less
  like silhouettes." **S.h = c** (dome-break c; a and b move to `rejected`). R.a, R.c, R.d, R.e keep as built; P.g as
  built; N.a and N.b noted. The three fixes (F.c, K.a, R.b) and the dome-break wiring are step FXS's; Level 18's art
  stays `chosen` until they land. Approved as final: Ghost Drone, Dust Devil, the L18 props, intel pictures and
  briefing images, track 9, sounds S.a-S.g, the voices.
