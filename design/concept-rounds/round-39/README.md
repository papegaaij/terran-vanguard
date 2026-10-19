---
title: Concept round 39 — M7 part C, Level 16 Valles Canyon Run
design: approved
implementation: n/a
art: chosen
depends-on: [../../campaign/act-3-red-dust/level-16-valles-canyon-run, ../../enemies/ground/tendril-anchor, ../../enemies/ground/threadcrawler, ../../player/specials, ../../world/mars, ../../audio/sfx, ../../audio/voice, ../../tech/roadmap, ../round-38]
updated: 2026-10-18
---

# Concept round 39 — M7 part C, Level 16 Valles Canyon Run

**Closed 2026-10-18** (user, on the phone review page): the three changes to approved specs **accepted**
(C1: the Orbital Lance is not stopped by rock, the specials spec wins and Level 16's *What walls stop* is
fixed; C2: a fork wave flies the widest channel; C3: flyer shadows on the walls and the plateau); the Valles
backdrop, Level 16's props, the Tendril Anchor, the Threadcrawler, the intel pictures and the two briefing
images **approved as final** (F.1–F.6), **except** the canyon walls' look: R.1 asks to "make it look more like
a real canyon. Maybe you can take inspiration from pictures from the Grand Canyon from above.", so the walls'
and islands' rendering and textures are being redone (step CW, in progress) for
round 40, and Level 16 stays `art: chosen` until then; the five sounds accepted (S.1–S.5,
`chosen`); the voices and the Choir's retiming accepted (V.1, V.2); the capture accepted (K.1); every build
choice **yes** (B.1–B.5); R.2, R.3 and R.5 **keep**; R.4 "change the plan or the hint": Claude changed the
**hint** (the approved Act 3 economy stays), so Level 16 no longer promises a beam the plan does not have
(Varga's t=47 line, re-rendered, her L3 intel line, the traits `forward`, `piercing`), pending the user's
review. M7 part C is built except R.1's redo ([roadmap](../../tech/roadmap/README.md#m7-parts)). The outcome
of each row is in its *Outcome* cell.

## Summary

The review round of M7 part C *Level 16* ([roadmap](../../tech/roadmap/README.md#m7-parts)), built while
the user was on vacation: terrain collision (the canyon's walls and rock islands, the ship's and Rook's
contact and push, shots, beams, missiles and enemy bullets against rock), the walls projected rising toward
the camera from their foot (round 36 MA.1 = b), the
[Tendril Anchor](../../enemies/ground/tendril-anchor/README.md) gates (`link`, a beam burning the tendril)
with the new `links` secondary and its BARRIERS tracker, the
[Threadcrawler](../../enemies/ground/threadcrawler/README.md) as a ground chain along the walls' foot,
[Level 16 Valles Canyon Run](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md)'s data,
its Valles backdrop and props, and the part's sounds. The documents stay `design: approved` (round 36);
every default, pick and build choice below was taken on the user's behalf and is **pending the user's
review**. Level 16's voices were rendered afterwards (step VOX-C, 2026-10-18): the capture still shows its
radio as text, the game plays the voices.

Open [index.html](index.html) in a browser (regenerate with
`python3 tools/concept/board.py 39 --subjects backdrop-final,l16-props-final,tendril-anchor-final,threadcrawler-final,intel-final,briefing-images-final,tendril-hum,tendril-burn,tendril-break,crawler-walk,mars-canyon,level-16-capture`).
The rows are in this order:

1. **Changes to approved specs** (`C…`): *accept* or *revert* (C1: pick which document wins);
2. **Art** (`F.…` final art to approve): *approve as final* / *redo*;
3. **Sounds** (`S.…`, one variant each): *accept* / *redo*;
4. **Voices** (`V.…`): *accept* / *redo* (say which line), the texts in [Listening](#listening);
5. **Captures** (`K.…`): the game as built, *accept* or *change*;
6. **Build choices** (`B.…`) per step, each item *yes* (keep as built) or *no* (say what), and the
   **findings** (`R.…`).

## In the game

`./gradlew :desktop:run --args="…"` with `--settings <file>` to keep the real settings apart (a debug
run writes no save):
- Level 16 with the balance plan's ship: `--start level --level 16 --loadout front=pulse-cannon:4,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:2,generator=2,shield=1,armor=2 --special airstrike:2 --escort rook:autocannon:1`;
- with a beam for the tendrils: `--special orbital-lance:2` (as the capture), or `front=ion-beam:2` in the
  loadout.

## Changes to approved specs

What the steps found contradicting or changed in the approved documents. Each **C** row is *accept* or
*revert*; C1 asks which document wins.

| # | Change | Approved | Built (where) | Answer | Outcome |
|---|---|---|---|---|---|
| C1 | **The Orbital Lance and rock** | The [specials](../../player/specials/README.md) spec: the Lance's column is "not stopped" (it burns the full play field's height). Level 16's *What walls stop* table and the plan: `beam` (Ion Beam, **Orbital Lance**) "ends at the first rock" | **not stopped by rock** (the specials spec; step C1). The Ion Beam ends at the first rock. Level 16's table still lists the Lance with the beam | specials wins (fix Level 16's table) / Level 16 wins (the Lance stops at rock) || **accepted** (user): specials wins; Level 16's *What walls stop* lists the Lance apart (not stopped), the Ion Beam ends at rock; the act plan's L16 row and the anchor spec say so too |
| C2 | **The fork's channel is the widest, not `edge`** | Level 16's wave table: in the fork (t=66–80) a wave's `edge` picks the channel (left, right, alternating channels) | the span mapping puts a fork wave in the **widest** channel at its row (both are 150 px, so in practice the left one first); `edge` does not choose (step C4, `Formations.plan` with the span) | accept / revert (make `edge` choose) || **accepted** (user): Level 16's waves prose, *Branching* and the fork waves' notes now say the widest channel (the left) |
| C3 | **Flyer shadows on the walls** | Level 16's rendering: shadows fall on the floor | the air units' shadows fall on the wall faces and the plateau too, where they cross them (step G1) | accept / revert (floor only) || **accepted** (user): in Level 16's *Rendering* |

## Art — final

Production art from round 36's picks (`tools/art/`, step ART-C), in the game now.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.1 | [Valles backdrop](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#concept-art) | **final art** (`tools/art/backdrop_l16.py`, from the canyon walls b and the Valles still of round 36): the Noctis plateau with its chasms (a new `far` set `noctis-chasm` at 0.6), the canyon floor, the rapids' riverbed, the Eos chaos, the plateau at 1.25, the wall and foot textures, the mesa and pillar faces and tops, the seam pieces, the cliff road, the burnt cliff habitats on the faces, the Helix station, the arches and the dust | [sheet](../../campaign/act-3-red-dust/level-16-valles-canyon-run/concept/backdrop-final-r39-a.png), [sheet GIF](../../campaign/act-3-red-dust/level-16-valles-canyon-run/concept/backdrop-final-r39-a.gif) | **in flight the walls read as flat vertical bands** (a lip line, the face's striations, the foot band) rather than as cliffs rising toward the camera; the mesa and the pillar read as flat polygons ([K.1](#captures), [R.1](#build-choices-and-findings)) | approve as final / redo || **approved as final** (user), **except** the walls' and islands' rendering and textures: R.1's redo (step CW, in progress, to round 40); Level 16 `art: chosen` until then |
| F.2 | [Level 16's props](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#concept-art) | **final art** (`tools/art/canyon_units.py`): the Candor relay hut hit by hit and its beacon, the cargo rovers (intact, damaged, wreck), the wall spark and its dust | [sheet](../../campaign/act-3-red-dust/level-16-valles-canyon-run/concept/l16-props-final-r39-a.png), [sheet GIF](../../campaign/act-3-red-dust/level-16-valles-canyon-run/concept/l16-props-final-r39-a.gif) | the rovers have no break-apart frames ([R.2](#build-choices-and-findings)) | approve as final / redo || **approved as final** (user) |
| F.3 | [Tendril Anchor](../../enemies/ground/tendril-anchor/README.md#concept-art) | **final art** (`tools/art/canyon_units.py`, from the chosen r36-a): the polyp clamp in its headings and pulse, the tendril (dormant flicker, flare, rope, burn, wither, frayed ends) | [sheet](../../enemies/ground/concept/tendril-anchor-final-r39-a.png), [sheet GIF](../../enemies/ground/concept/tendril-anchor-final-r39-a.gif) | at 1:1 the anchors are small grey clamps at the walls' foot; the dormant tendril a thin red line | approve as final / redo || **approved as final** (user); the Tendril Anchor `art: final` |
| F.4 | [Threadcrawler](../../enemies/ground/threadcrawler/README.md#concept-art) | **final art** (`tools/art/canyon_units.py`): head, segments and tail in 16 headings with jaw and leg phases, the pore glow and flash, the husk | [sheet](../../enemies/ground/concept/threadcrawler-final-r39-a.png), [sheet GIF](../../enemies/ground/concept/threadcrawler-final-r39-a.gif) | — | approve as final / redo || **approved as final** (user); the Threadcrawler `art: final` |
| F.5 | [Intel pictures](../../ui/hangar/README.md#concept-art) | **final art** (`tools/art/intel.py`): the sensor-L2 portraits of the Tendril Anchor and the Threadcrawler | [sheet](../../ui/hangar/concept/intel-final-r39-a.png) | — | approve as final / redo || **approved as final** (user) |
| F.6 | [Briefing images](../../ui/briefing/README.md#concept-art) | **final art** (`tools/art/briefing_images.py`): Level 16's two, the canyon route and the tendril scan | [sheet](../../ui/briefing/concept/briefing-images-final-r39-a.png) | — | approve as final / redo || **approved as final** (user) |

## Sounds

Round 39's sounds ([sfx](../../audio/sfx/README.md#concept-art), `tools/concept/audio/sfx_r39.py`, all
synthesized, step AUD-C), **one variant each**, in the game now; the wall scrape is round 36's chosen b. Not
listened to by Claude: checked by envelopes, spectrograms, levels and loop seams; no bubbly or popping layers.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [Tendril hum](../../audio/sfx/README.md#concept-art) | a taut 120 Hz drone with odd harmonics over a steady fizz, no wobble, seamless 6 s loop; the nearest tendril, panned, at 0.4 while dormant | [a](../../audio/sfx/concept/tendril-hum-r39-a.ogg) | 77 % of its energy below 200 Hz: small speakers may lose it | accept / redo || **accepted** (user), `chosen` |
| S.2 | [Tendril burn](../../audio/sfx/README.md#concept-art) | a noise-grain crackle over a dry sizzle, seamless 2 s loop while a beam burns a tendril | [a](../../audio/sfx/concept/tendril-burn-r39-a.ogg) | — | accept / redo || **accepted** (user), `chosen` |
| S.3 | [Tendril break](../../audio/sfx/README.md#concept-art) | a whip-crack and a falling whine, 0.9 s, when a tendril or an anchor dies | [a](../../audio/sfx/concept/tendril-break-r39-a.ogg) | — | accept / redo || **accepted** (user), `chosen` |
| S.4 | [Threadcrawler walk](../../audio/sfx/README.md#concept-art) | twelve legs ticking at 8.4 steps a second over a rock rustle, seamless 3 s loop (the nearest crawler, panned) | [a](../../audio/sfx/concept/crawler-walk-r39-a.ogg) | — | accept / redo || **accepted** (user), `chosen` |
| S.5 | [Mars canyon ambience](../../audio/sfx/README.md#concept-art) | `mars-canyon`: a dust wind with a resonant canyon howl breathing with the gusts, seamless 16 s loop | [a](../../audio/sfx/concept/mars-canyon-r39-a.ogg) | the gusts swing ±9 dB | accept / redo || **accepted** (user), `chosen` |

## Voices

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| V.1 | [Level 16's radio and briefing](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#radio-chatter) | **accept or redo** (step VOX-C, `tools/art/voice.py` through the radio filter, the pages dry): the two briefing pages (Okafor 14.4 s, Varga 11.9 s), Okafor's six, Varga's five, Rook's two (his t=11 `{side}` line in both takes), Ares Control's four, the Choir's song reused; Airees, Vallis Marinairis and Coprateez as approved, Eos and Helix plain (Noctis and Candor are in no voiced line); four seeds pinned (Okafor t=1 read "Canyon Martha Head", Varga t=114 "Rell", Ares Control t=22 a high jump, the relay hut's line ran high and read "If that's") | [briefing 1](../../../assets/voice/okafor/87e20a9bb6a8.ogg), [briefing 2](../../../assets/voice/varga/c66f61226d1a.ogg), [Okafor t=1](../../../assets/voice/okafor/100852a91877.ogg), [Rook t=11 left](../../../assets/voice/rook/601e8e194c0c.ogg), [Rook t=11 right](../../../assets/voice/rook/81da6aa7b614.ogg), [Ares Control t=22](../../../assets/voice/ares-control/728e990f43d1.ogg), [Okafor first wall touch](../../../assets/voice/okafor/f052a5892823.ogg), [Varga t=33](../../../assets/voice/varga/42d94f4d28f0.ogg), [Varga t=47](../../../assets/voice/varga/e80e3305e17d.ogg), [Okafor t=60](../../../assets/voice/okafor/acc1ccbb21c2.ogg), [relay hut (secret)](../../../assets/voice/ares-control/beaf54a50a1b.ogg), [Varga t=81](../../../assets/voice/varga/e48eeca2fe4c.ogg), [Rook t=94](../../../assets/voice/rook/25aeb30c5848.ogg), [Varga t=114](../../../assets/voice/varga/4e10c065c26c.ogg), [Okafor t=126](../../../assets/voice/okafor/9836d26ce91b.ogg), [Ares Control t=146](../../../assets/voice/ares-control/542a8ae3d550.ogg), [secondary](../../../assets/voice/ares-control/c784b5d61643.ogg), [Varga t=173](../../../assets/voice/varga/333c46eba272.ogg), [Okafor t=180](../../../assets/voice/okafor/a4014d88e261.ogg), [Okafor end](../../../assets/voice/okafor/3af9b9cd6a58.ogg) | LISTEN Ares Control t=146 ("Eos": read as "Eos" only with the name prompt, "Beause" without), Rook t=94 ("Coprateez" and "Okafor"), Varga t=114 ("Vrell all round it" reads as "…all rounded"), Ares Control t=22 (the opening: one read-back heard "The Aegis"); the texts are in [Listening](#listening) | accept / redo (say which) || **accepted** (user); Varga's t=47 line re-rendered for R.4 (step R39C) |
| V.2 | [Level 16's radio timing](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#radio-chatter) | **accept or revert** (step VOX-C, a data edit): the Choir t=162 → 166 and Varga's reply t=169.5 → 173. The secondary's closing line takes a free radio at once and holds it about 7 s, so with gate 6 falling after t≈155 it held the Choir back by up to 6 s; `RadioTimelineTest` now plays Level 16 (the wall touch early or late, the relay hut, the secondary at t=155, 158 or 159, with and without Rook), every timed line within 1 s | — | — | accept / revert || **accepted** (user) |

## Captures

The `installDist` build on a private display, the game's own sound (its `wave` mix); how it was taken is in
the level's `concept/prompts.md`.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| K.1 | [Level 16](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#concept-art) | **game capture**: a whole medium run with the plan's L16 ship and Rook, the Orbital Lance as its special (the plan has no beam before Level 18), **no `--invulnerable`**: the mouth's walls rising from flat, gate 1 dormant → flare → anchor kill → wither, the Lance burning gate 2, three wall touches (shield, push), the fork's mesa and the relay hut (secret found), the first crawler's pore waves, the pillar, the rovers, the Helix station, the narrows' gates, the second crawler crossing the floor, the Burrowers, the debrief (210 / 215, no armour lost, BARRIERS 9 / 9, A+ 99); 3:51 with sound and a sheet of 20 frames | [level-16-capture-r39-a.png](../../campaign/act-3-red-dust/level-16-valles-canyon-run/concept/level-16-capture-r39-a.png), [.mp4](../../campaign/act-3-red-dust/level-16-valles-canyon-run/concept/level-16-capture-r39-a.mp4) | radio as text (V.1); no crawler cut shown; the driver flies by dead reckoning at the bottom, so the wall touches are deliberate | accept / change || **accepted** (user) |

## Build choices and findings

What the steps chose where the documents are silent, and what they found. **B** rows: each item
*yes* (keep as built) or *no* (say what). **R** rows: as their answer cell says.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.1 | [Terrain in the simulation](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#terrain) | **yes or no, each** (step C1): (a)–(g) below | [K.1](#captures) | — | all yes / some no || **all yes** (user) |
| B.2 | [Tendrils and crawlers in the simulation](../../enemies/ground/tendril-anchor/README.md) | **yes or no, each** (step C3): (a)–(f) below | [K.1](#captures) | — | all yes / some no || **all yes** (user) |
| B.3 | [Level 16's data](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md) | **yes or no, each** (steps C2, C4): (a)–(l) below | — | — | all yes / some no || **all yes** (user) |
| B.4 | [In the game](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#implementation) | **yes or no, each** (steps G1, G2): (a)–(k) below | [K.1](#captures) | — | all yes / some no || **all yes** (user) |
| B.5 | [The backdrop's names](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#concept-art) | **yes or no, each** (step ART-C): (a)–(c) below | [F.1](#art--final) | — | all yes / some no || **all yes** (user) |
| R.1 | [The walls' look in flight](#captures) | **finding**: in the capture the walls read as flat bands of striped rock at the field's edges; the lean toward the camera and the plateau's height do not come across at 1:1 or on the sheet; the mesa and the pillar look like flat polygons | [K.1 sheet](../../campaign/act-3-red-dust/level-16-valles-canyon-run/concept/level-16-capture-r39-a.png) | Claude's reading of stills; motion may help a little | keep / rework the projection or textures (say how) || **rework** (user): "Make it look more like a real canyon. Maybe you can take inspiration from pictures from the Grand Canyon from above." Redo (step CW, in progress), to round 40 |
| R.2 | [The rovers' break-apart](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#ground-targets) | **fix made in step C5**: every Level 16 start crashed (`no frames 'canyon-rover-break'`): the game wanted break-apart frames for every destructible ground object and the rovers have none. A ground object now breaks apart only when its look has the frames; a rover leaves its wreck and the small explosion | — | the tests did not start Level 16's screen with the real atlas | keep / add a rover break-apart (art) || **keep** (user): no rover break-apart |
| R.3 | [The Lance's scorch on sand](../../player/specials/README.md) | **finding**: the Orbital Lance's scorch line (round 37) reads as a solid black 40 px stripe on the canyon's pale sand for several seconds | [K.1](#captures) (t ≈ 50–53) | — | keep / soften it || **keep** (user): the scorch as built |
| R.4 | [A beam at Level 16](../../player/balance-plan.yaml) | **finding**: Level 16 recommends `beam` and its radio says "A beam burns through those tendrils", but the plan buys the Ion Beam at Level 18 and the Lance unlocks at Level 17, so a player on the plan meets Level 16 with no beam; every gate still falls to an anchor kill (the capture's gate 1 fell ≈ 0.6 s after it entered, before its tell ended) | — | — | noted / change the plan or the hint || **change the plan or the hint** (user): Claude changed the **hint** (the approved Act 3 economy stays): Varga's t=47 "Kill an anchor and the tendril drops. Ordinary shots pass straight through it." (re-rendered), her L3 intel "Kill an anchor to drop a tendril. A cache down the left fork.", traits `forward`, `piercing`; the briefing's "a beam burns through the tendril" kept (true later); pending the user's review |
| R.5 | [The Helix glimpse](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#layout) | **finding**: the station on the plateau layer (×1.25 at 230 px/s) is on screen for about 3 s (t ≈ 115.5–118.5), where the README says t ≈ 113–124 | [K.1](#captures) | — | keep / slow it (say how) || **keep** (user): the ≈ 3 s glimpse |

**Build choices (row B.1)**, step C1, each *yes* (keep) or *no* (say what):

- **(a)** The Orbital Lance is not stopped by rock (C1 asks which document wins).
- **(b)** A dash (Evasive thrusters) into rock clamps the ship without damage.
- **(c)** The field's own edges (x 0 and 480) do no damage.
- **(d)** Islands are convex polygons.
- **(e)** A wave entering from a side under the span mapping emerges from the rock.
- **(f)** Homing missiles without a blast only spark on rock; rock stops a shot after its hit has passed.
- **(g)** Rook keeps 24 px off the walls and trails the ship (the TRAIL position) where the canyon is
  narrow.

**Build choices (row B.2)**, step C3, each *yes* (keep) or *no* (say what):

- **(a)** A pore glows from the crawler's first frame on screen; off-screen or dying pores skip their shot.
- **(b)** A beam burns a dormant tendril too.
- **(c)** The touch cooldown is shared per craft (all tendrils and walls).
- **(d)** Rook's 12 px clearance below a standing tendril may lag a step.
- **(e)** A burnt tendril's bounty counts as a part kill.
- **(f)** A cut crawler's members cannot be hit until they burst.

**Build choices (row B.3)**, steps C2 and C4, each *yes* (keep) or *no* (say what):

- **(a)** The gates are linked nests among the ground targets (`link: pairs`).
- **(b)** The anchors stand **20 px** off the wall line (C2 had 12; at 20 a beam reaches each one from a
  ship clear of the rock).
- **(c)** The crawl paths are terrain rows, starting 30 rows higher so hard's 16-segment body enters from
  above the top edge.
- **(d)** The walls' height ramps from 0 to full over rows 2,850–4,180 (the mouth) and back over
  31,250–34,850 (the outflow).
- **(e)** The Threadcrawler's whole bounty paid as its head's first bonus (48); the anchors' orb fires
  1.0 s after it appears.
- **(f)** The section peaks (the rapids' medium dust 110–120, the outflow clear from 178).
- **(g)** The slope rule applies to the walls only (not the islands).
- **(h)** The hangar's CRAWLER marks on medium only.
- **(i)** Seven popcorn waves moved by ≤ 1 s for the pacing rule (13.5, 17, 20.5, 48.5, 81.5, 138).
- **(j)** Four Dust Kite waves fly straight authored paths (66.5 and 72 at x 115 / mirror 365; 135.5 and
  142 at x 240): their 50 px sway hit the rock.
- **(k)** The intel setting reads "Mars, Valles Marineris" (the panel's width).
- **(l)** Typical haul 1,944 (+0.7 % of budget(16)), 69.7 enemies a minute, the autopilot touching a wall
  about six times a run.

**Build choices (row B.4)**, steps G1 and G2, each *yes* (keep) or *no* (say what):

- **(a)** The islands have no foot band; the plateau layer does not drift.
- **(b)** The scrape at −2 dB, panned at the contact; the hit flash 0.1 s.
- **(c)** The burnt cliff habitats are backdrop placements drawn on the wall faces (`cliff-habitat`).
- **(d)** The unit loops at 1.0, the tendril hum at 0.4 while dormant, 0.2 s fades, the nearest unit.
- **(e)** A crawler's cut plays the Coilwyrm's rip at pitch 0.8, −3 dB.
- **(f)** The intel strip's ticks: GATE in cyan (full depth), CRAWLER in green (lower half).
- **(g)** The anchors turn to face their partner; the tendril sways 5 px.
- **(h)** The relay hut's burst is a small explosion.
- **(i)** The BARRIERS tracker flashes green per tendril broken; the debrief row `BARRIERS BROKEN n / 9`.
- **(j)** The crawler's husk stays where its head died; the cut part writhes.
- **(k)** The Threadcrawler's tier bursts are the generic ones, not the Coilwyrm's wet ones.

**Build choices (row B.5)**, step ART-C, each *yes* (keep) or *no* (say what):

- **(a)** `noctis-plateau` is the ground of section 1, with a new far set `noctis-chasm` (×0.6) seen
  through its cracks.
- **(b)** The extra names: `dust-veil`, `narrows-dust`, `dust-streaks`, `canyon-mouth`, `seam-rapids`,
  `seam-narrows`, `seam-eos`, `habitat-pad`.
- **(c)** The Helix station on the right (north) plateau.

## Listening

Every line step VOX-C rendered for Level 16, as the game plays it (the Choir's song reused).

| Where | Speaker | Line | File |
|---|---|---|---|
| L16 briefing page 1 | Okafor | "Ares Landing is two thousand kilometres east, and the one road the Vrell don't hold from the air is the Valles Marineris…" | [play](../../../assets/voice/okafor/87e20a9bb6a8.ogg) |
| L16 briefing page 2 | Varga | "They grow barriers across the canyon: an anchor on each wall and a burning tendril between…" | [play](../../../assets/voice/varga/c66f61226d1a.ogg) |
| L16 t=1 | Okafor | "Aegis, canyon mouth ahead. Stay fast, and stay off the walls." | [play](../../../assets/voice/okafor/100852a91877.ogg) |
| L16 t=11 | Rook (left) | "On your left, Lancer. Where it gets narrow, I'll tuck in behind." | [play](../../../assets/voice/rook/601e8e194c0c.ogg) |
| L16 t=11 | Rook (right) | "On your right, Lancer. …" | [play](../../../assets/voice/rook/81da6aa7b614.ogg) |
| L16 t=22 | Ares Control | "Aegis, this canyon is our only road east. The Vrell have strung it shut." — LISTEN: the opening | [play](../../../assets/voice/ares-control/728e990f43d1.ogg) |
| L16 first wall touch | Okafor | "Mind the walls, Lancer. They don't give." | [play](../../../assets/voice/okafor/f052a5892823.ogg) |
| L16 t=33 | Varga | "Barrier ahead. Two anchors on the walls: kill either one and it falls." | [play](../../../assets/voice/varga/42d94f4d28f0.ogg) |
| L16 t=47 | Varga | "Kill an anchor and the tendril drops. Ordinary shots pass straight through it." (R.4, re-rendered in step R39C) | [play](../../../assets/voice/varga/e80e3305e17d.ogg) |
| L16 t=60 | Okafor | "The canyon splits ahead. Pick a side and commit." | [play](../../../assets/voice/okafor/acc1ccbb21c2.ogg) |
| L16 relay hut (secret) | Ares Control | "That's our relay cache. Take it, Aegis." | [play](../../../assets/voice/ares-control/beaf54a50a1b.ogg) |
| L16 t=81 | Varga | "Movement on the left wall. Something long." | [play](../../../assets/voice/varga/e48eeca2fe4c.ogg) |
| L16 t=94 | Rook | "Coprates rapids. And Okafor calls this a road." — LISTEN: "Coprateez", "Okafor" | [play](../../../assets/voice/rook/25aeb30c5848.ogg) |
| L16 t=114 | Varga | "That's the Helix research station on the rim. Vrell all round it, and not a scratch." — LISTEN: "all round it" | [play](../../../assets/voice/varga/4e10c065c26c.ogg) |
| L16 t=126 | Okafor | "Eyes on the canyon, Doctor." | [play](../../../assets/voice/okafor/9836d26ce91b.ogg) |
| L16 t=146 | Ares Control | "Eos outflow ahead. You're nearly through, Aegis." — LISTEN: "Eos" | [play](../../../assets/voice/ares-control/542a8ae3d550.ogg) |
| L16 secondary met | Ares Control | "Every barrier down. The road east is open." | [play](../../../assets/voice/ares-control/c784b5d61643.ogg) |
| L16 t=166 | The Choir | "[the Choir sings]" (round 29's b, reused) | [play](../../../assets/voice/choir/voice-choir-sings-r29-b.ogg) |
| L16 t=173 | Varga | "The singing is louder out east. Toward Ares Landing." | [play](../../../assets/voice/varga/333c46eba272.ogg) |
| L16 t=180 | Okafor | "Ares Landing has you on its scopes. Bring it home." | [play](../../../assets/voice/okafor/a4014d88e261.ogg) |
| L16 level end | Okafor | "Canyon run complete. The domes are yours to hold now. Get some sleep." | [play](../../../assets/voice/okafor/3af9b9cd6a58.ogg) |

## Decisions

- 2026-10-14: opened (M7 part C, step C5): the part's final art, sounds, the capture, the changes to
  approved specs, build choices and findings, pending the user's review.
- 2026-10-18 (M7 part C, step VOX-C): Level 16's voices rendered (V.1, the record that they were missing
  replaced) and its Choir retimed (V.2), pending the user's review.
- 2026-10-18 (user, round 39): closed: every row answered as in its *Outcome* cell; C1–C3 applied to Level 16's
  approved texts (and the act plan's and the anchor spec's beam wording for C1); the finals and the five sounds
  recorded as `chosen`/`final`; R.1's wall redo is step CW (in progress), to round 40; R.4: the hint changed (Varga's
  t=47 line re-rendered with `tools/art/voice.py`, the stale file deleted, her L3 intel line and the traits), the
  economy unchanged (step R39C).
