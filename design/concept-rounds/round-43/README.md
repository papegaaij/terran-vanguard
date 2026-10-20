---
title: Concept round 43 — the walker's rework, the Freesound sounds, M7 part G (Level 20), part H's questions
design: approved
implementation: n/a
art: chosen
depends-on: [../../enemies/bosses/revenant-walker, ../../campaign/act-3-red-dust/level-19-foundry-of-tharsis, ../../campaign/act-3-red-dust/level-20-retreat-from-hellas, ../../enemies/air/mote-swarm, ../../player/weapons/rear-guard-drone-bay, ../../audio/sfx, ../../audio/voice, ../../tech/roadmap, ../round-42]
updated: 2026-10-20
---

# Concept round 43 — the walker's rework, the Freesound sounds, M7 part G (Level 20), part H's questions

## Summary

Your rule of 2026-10-20: **every undecided point is a question here, with a recommendation; nothing undecided gets
built.** Five things wait for an answer:

1. **The Revenant Walker's rework** (round 42's `K.a` change, `W` rows): the capture, the HP cut, the fiercer phase 1,
   hard's long fight, and one question (a safe spot over the walker).
2. **The sounds from the Freesound database** (`S` rows): Level 19's eleven redone or new subjects (two variants each:
   *a*, *b* or *redo*) and Level 20's three. Claude has not listened to any of them.
3. **M7 part G, Level 20 Retreat from Hellas** ([roadmap](../../tech/roadmap/README.md#m7-parts)): the final art (`F`),
   the voices (`V`), the capture (`K`) and the build choices of the steps (`B`), then **five questions** (`Q`): the
   swarm that still mostly melts, the beam-era limit for a reversed level, whether turrets count as seekers, the
   ridge's data key, and the hangar's and the briefing's art status.
4. **Part H's questions** (`H` rows, Level 21 and the act end), answered here so part H starts decided.
5. **Records** (last section): the macOS CI fix, the allocation guard, round 42's close.

**Part G in short.** Level 20 is the retreat: the scroll eases to zero at the turnabout and runs the other way, so the
ground moves down the screen and the rear guns face what is ahead by travel. A pursuing
[Mote Swarm](../../enemies/air/mote-swarm/README.md) holds a band behind the ship, tells, lunges and climbs back; the
rim's flak and the militia's guns wait at the top; Rook flies his Autocannon; the
[rear-guard drone bay](../../player/weapons/rear-guard-drone-bay/README.md) is the new wing item. The level is a
count-neutral rework of the approved waves (197 enemies, about 71 a minute at medium). Part H (Level 21, the Dust
Colossus) is **not started**.

Open [index.html](index.html) in a browser (regenerate with
`python3 tools/concept/board.py 43 --subjects walker-hatch,walker-flame-tell,walker-flame,vent-tell,vent-burst,tank-tracks,tank-turret,tank-burst,walker-vent-hit,walker-death,walker-wreck-settle,basin-rumble,swarm-hum,swarm-lunge,level-19-walker,backdrop-final,l20-props-final,rear-guard-drone-final,rear-guard-icon-final,level-20-capture,level-20-swarm,level-20-drone,rear-guard-drone-testfire`).
Each row carries a recommendation; **B** rows list build choices: *yes* (keep as built) or *no* (say what). Where the
Answer column lists options, the first marked *(recommended)* is mine.

## In the game

`./gradlew :desktop:run --args="..."` with `--settings <file>` (a debug run writes no save):
- Level 20 with the balance plan's ship and Rook's Autocannon: `--start level --level 20 --loadout front=ion-beam:3,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:3,generator=2,shield=1,armor=2 --special airstrike:2 --escort rook:autocannon:1`
  (`--difficulty easy|hard`; `--invulnerable` to see the whole level; `--escort none` to fly without Rook);
- the rear-guard drone bay on the wings instead of the pods (K.c's run): `left=rear-guard-drone-bay,right=rear-guard-drone-bay`;
- Level 19's reworked walker: the same options with `--level 19` and `--escort rook:mortar:1`;
- the numbers: `./gradlew :content:levelReport -Plevel=20 -Pparts=pursuit,pacing,floor,haul,hp`.

## The Walker's rework

Round 42's `K.a` change ("The walker looks like a real easy target. It needs to do a lot more shooting and the flame
needs to be bigger.") was built in step WR
([the walker](../../enemies/bosses/revenant-walker/README.md#duration-and-hp), numbers measured with the
autopilot and the plan's Level 19 fit, five seeds).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| W.a | [The reworked fight](../../enemies/bosses/revenant-walker/README.md) | **check capture** (step WR): 83 s of the fight with sound: the chin's fans every phase (3-way in phase 1, three aimed in phase 2, 5-way over 40° in phase 3, in bursts), the flank guns (a fan of 2 over 180°, bursts of 3 and 4), salvos of 3 (at most 8 missiles alive), the cone 240 px ±32° with its swivel and a tracking burn (40°/s from phase 2). About 100 shots a minute against 41 before (phase 1 ≈ 125). **Recommendation: accept** | [video](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/concept/level-19-walker-r43-a.mp4), [sheet](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/concept/level-19-walker-r43-a.png) | flown by the test autopilot, not by you; the autopilot's phase 3 lasts about 10 s and much of it hugs the walker (see W.d) | accept (recommended) / change | **change** (user, 2026-10-20): "The first missile hatch is destroyed before the walker has a chance of ever using it. That's too easy. When both missile hatches are destroyed, the walker is mostly toothless. Can't you add something else that keeps the final part interesting?" Decided with the user: the pods stay armoured until their hatch opens, a back-mounted rail lance fires once the pods are gone, and the stomp shockwave (step WR2) |
| W.b | [HP cut 3,740 → 3,170](../../enemies/bosses/revenant-walker/README.md#duration-and-hp) | **yes or no**: with the new fire the autopilot stands off behind the 240 px cone and the fight ran 80–110 s at medium, phase 3 alone up to 54 s. The HP are cut to **3,170** (hull 2,310, pods 430; ×0.85): the fight **56–60 s** on all five seeds (mean 57.1; in the level 56.8–68.2 s), the **par 57** (easy 33–39 s), `WALKER_EFFICIENCY` 1.39 → 1.08. Bounty unchanged. **Recommendation: yes** | [W.a](#the-walkers-rework) | the cut is for the autopilot's play; a human who does not hug the cone may need more or less | yes (recommended) / no (say what) | yes (recommended) (user, 2026-10-20) |
| W.c | [Phase 1's loss](../../enemies/bosses/revenant-walker/README.md#duration-and-hp) | **yes or no**: the random dodger now loses **21.0 per 10 s** of phase 1 against the Kraken's 11.4 (×1.84; the calibration test asks 1.3–2×). Round 36's "a kinder phase 1" is reversed at your request: fierce but fair. **Recommendation: yes** | [W.a](#the-walkers-rework) | the random dodger is a harness, not a player | yes (recommended) / no (say what) | yes (recommended) (user, 2026-10-20) |
| W.d | [Hard's long fight](../../enemies/bosses/revenant-walker/README.md#duration-and-hp) | **yes or no**: on hard the fight runs **98–106 s alone, 111–174 s in the level, with one retry** (hard's pace 185 shots a minute, phase 3 about 240). All won, no retry at medium. **Recommendation: yes**, hard is meant to be long | [W.a](#the-walkers-rework) | 174 s is a long time at one boss; hard's HP stay as the difficulty table says | yes (recommended) / no, shorten hard | yes (recommended) (user, 2026-10-20) |
| W.e | **Question: the safe spot over the walker** | no gun fires within **72 px** of the hull, so a ship that hugs the walker is safe from every gun (only the cone, which the autopilot keeps out of, can still burn it); this is how the autopilot survives phase 3. The options: leave it; a short-range **stomp shockwave** (a ring when a leg lands, 72–110 px, the same tell as the footfall) so that hugging costs something; or shrink the dead zone. **Recommendation: the stomp shockwave** (for instance a ring of about 110 px with a short tell on the footfall, damage like a missile, walking phases only; the numbers would be set when built) | — (nothing built: an undecided point is not built) | a shockwave makes phase 3 harder again and the par moves (re-measure after it) | stomp shockwave (recommended) / leave it / shrink the dead zone | **stomp shockwave** (recommended) (user, 2026-10-20): "And as noted above, something else for the longer range." Step WR2 builds the shockwave and the rail lance (W.a) |

## Sounds

All from the Freesound database (CC0 or CC-BY, [CREDITS.md](../../../CREDITS.md)), cut and layered by
`tools/concept/audio/sfx_r43.py` and `sfx_r43g.py`, **never synthesized**; two variants each, pending your listening
(Claude has not listened: envelopes, levels and loop seams only; no bubbly or popping layers). A CC-BY file you pick
goes on the in-game credits screen. The game plays the *a* variants of Level 20 for now; Level 19 keeps round 42's
sounds until you pick. Your round-42 notes are quoted in the *What is proposed* column. My recommendations are guesses
from the descriptions.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.a | [Walker hatch](../../audio/sfx/README.md#concept-art) | redo (you: "That's not a hatch, that's a pop. Isn't this supposed to be a heavy thing? Search for sounds in the free database."). **a**: air brakes releasing and a large metal door slamming (CC-BY 4.0 + CC0), 1.05 s. **b**: a submarine hatch's clank and creak over a steam release with a deep impact under the clank (CC0), 1.01 s. **Recommendation: b** (the deep impact is the weight) | [a](../../audio/sfx/concept/rejected/walker-hatch-r43-a.ogg), [b](../../audio/sfx/concept/walker-hatch-r43-b.ogg) | both are loudest 100 ms −20.1 dB; the tell is 0.4 s before a salvo | a / b / redo | **b** (user, 2026-10-20) |
| S.b | [Walker flame tell](../../audio/sfx/README.md#concept-art) | redo (you: "Again way too small. Search in the database for large venting."). **a**: a huge gas vent opening (CC-BY 3.0), 0.87 s. **b**: a 1000 hp boiler's safety valve opening, slowed to 0.85 (CC0), 0.84 s. **Recommendation: a** | [a](../../audio/sfx/concept/walker-flame-tell-r43-a.ogg), [b](../../audio/sfx/concept/rejected/walker-flame-tell-r43-b.ogg) | the cone's tell is 0.6 s: both may be longer than the tell | a / b / redo | **a** (user, 2026-10-20) |
| S.c | [Walker flame](../../audio/sfx/README.md#concept-art) | redo (you: "Also too small. Search for large gas burns or large flamethrower"). **a**: a flamethrower's long burst, slowed to 0.85 (CC0), a seamless 4 s loop. **b**: a flamethrower's deep roar (CC-BY 3.0), a seamless 4 s loop. **Recommendation: b** (deeper, 3 dB louder at the peak) | [a](../../audio/sfx/concept/rejected/walker-flame-r43-a.ogg), [b](../../audio/sfx/concept/walker-flame-r43-b.ogg) | loops: −28 dB in the band | a / b / redo | **b** (user, 2026-10-20) |
| S.d | [Vent tell](../../audio/sfx/README.md#concept-art) | redo (you: "Same thing"). **a**: a balloon burner's roar swelling in (CC0), 1.05 s. **b**: a brighter balloon burner's roar swelling in (CC0), 1.05 s. **Recommendation: a** (darker; the vents are furnace vents) | [a](../../audio/sfx/concept/vent-tell-r43-a.ogg), [b](../../audio/sfx/concept/rejected/vent-tell-r43-b.ogg) | both are balloon burners, so the difference is small | a / b / redo | **a** (user, 2026-10-20) |
| S.e | [Vent burst](../../audio/sfx/README.md#concept-art) | redo (you: "Same thing"). **a**: a deep flame burst with the end of a gas burner's burn, dying away (CC0 ×2), 1.6 s. **b**: a fire burst roaring out (CC-BY 4.0), 1.6 s. **Recommendation: a** (CC0, deeper) | [a](../../audio/sfx/concept/vent-burst-r43-a.ogg), [b](../../audio/sfx/concept/rejected/vent-burst-r43-b.ogg) | a ripple plays up to three at once | a / b / redo | **a** (user, 2026-10-20) |
| S.f | [Tank tracks](../../audio/sfx/README.md#concept-art) | redo (you: "Search for sounds of a real tank driving"). **a**: a T-26 tank driving, tracks and engine (CC0), a seamless 4 s loop. **b**: a tracked bulldozer driving (CC-BY 4.0), a seamless 4 s loop. **Recommendation: a** (a real tank) | [a](../../audio/sfx/concept/tank-tracks-r43-a.ogg), [b](../../audio/sfx/concept/rejected/tank-tracks-r43-b.ogg) | loops: −32 dB in the band; the nearest tank only | a / b / redo | **a** (user, 2026-10-20) |
| S.g | [Tank turret](../../audio/sfx/README.md#concept-art) | redo (you: "You should really stop trying to make these sounds yourself. Use the database."). **a**: a real artillery turret re-aligning (CC0), 0.8 s. **b**: a factory overhead crane rolling (CC0), 0.9 s. **Recommendation: a** | [a](../../audio/sfx/concept/tank-turret-r43-a.ogg), [b](../../audio/sfx/concept/rejected/tank-turret-r43-b.ogg) | −23 dB in the band, quiet; the turret's lock is the tell | a / b / redo | **a** (user, 2026-10-20) |
| S.h | [Tank burst](../../audio/sfx/README.md#concept-art) | redo (you: "Not a heavy cannon. Not even a small cannon."). **a**: a tank cannon firing three shots 0.15 s apart (CC0), 1.18 s. **b**: a heavy artillery shot, three times 0.15 s apart (CC-BY 4.0), 1.38 s. **Recommendation: a** (a real tank's gun) | [a](../../audio/sfx/concept/tank-burst-r43-a.ogg), [b](../../audio/sfx/concept/rejected/tank-burst-r43-b.ogg) | the burst is three shots | a / b / redo | **a** (user, 2026-10-20) |
| S.i | [Walker vent hit](../../audio/sfx/README.md#concept-art) | new (you: "make these three"). **a**: a big stereo metal hit on a lift door (CC0), 0.55 s. **b**: a hollow metal impact with a burst of steam (CC0 ×2), 0.52 s. **Recommendation: b** (the vent is steam) | [a](../../audio/sfx/concept/walker-vent-hit-r43-a.ogg), [b](../../audio/sfx/concept/rejected/walker-vent-hit-r43-b.ogg) | plays on every hit of the vent window | a / b / redo | **a** (user, 2026-10-20) |
| S.j | [Walker death](../../audio/sfx/README.md#concept-art) | new (the `huge` rung). **a**: a large explosion with debris, the hull landing with a massive impact and a metal crash (CC0 ×3), 5.6 s. **b**: two chained blasts, the final boom with a car crash's metal and a radiator rumbling (CC0 ×4), 5.6 s. **Recommendation: a** (the topple's landing) | [a](../../audio/sfx/concept/walker-death-r43-a.ogg), [b](../../audio/sfx/concept/rejected/walker-death-r43-b.ogg) | the level's biggest moment; the topple and the chained blasts are one step in the game | a / b / redo | **a** (user, 2026-10-20) |
| S.k | [Walker wreck settle](../../audio/sfx/README.md#concept-art) | new. **a**: a bunker door's low metallic groan slowed to 0.85 over an engine cooling (CC0 ×2), 5.0 s. **b**: a giant metal structure creaking over an engine cooling (CC0 ×2), 5.0 s. **Recommendation: a** | [a](../../audio/sfx/concept/walker-wreck-settle-r43-a.ogg), [b](../../audio/sfx/concept/rejected/walker-wreck-settle-r43-b.ogg) | −24 dB in the band, quiet by design | a / b / redo | **a** (user, 2026-10-20) |
| S.l | [Basin rumble (Level 20)](../../audio/sfx/README.md#concept-art) | new, a second ambience loop at the Colossus ridge's pass. **a**: an earthquake's earth rumble, deep, with rock grinding (CC0), a seamless 16 s loop. **b**: an earthquake's rumble with creaks and groans over a deep distant rumble (CC-BY 4.0 + CC0), a seamless 12 s loop. **Recommendation: a** | [a](../../audio/sfx/concept/basin-rumble-r43-a.ogg), [b](../../audio/sfx/concept/rejected/basin-rumble-r43-b.ogg) | at the bed's loudness (−28 LUFS) the band RMS is −42 (a) and −34 (b); the sources are designed rumbles, not field recordings | a / b / redo | **a** (user, 2026-10-20) |
| S.m | [Swarm hum (Level 20)](../../audio/sfx/README.md#concept-art) | new, the pursuit's band hum. **a**: a designed swarm drone slowed to 0.85 over a bee box's hive hum (CC0 ×2), a seamless 6 s loop. **b**: a large swarm of flies slowed to 0.8 over blowflies (CC-BY 3.0 + CC0), a seamless 6 s loop. **Recommendation: a** | [a](../../audio/sfx/concept/swarm-hum-r43-a.ogg), [b](../../audio/sfx/concept/rejected/swarm-hum-r43-b.ogg) | a's bee box has birds in the background; the loop joins are +10 / +7.6 dB louder than the body | a / b / redo | **a** (user, 2026-10-20) |
| S.n | [Swarm lunge (Level 20)](../../audio/sfx/README.md#concept-art) | new. **a**: hum a's swarm swelling in and gliding up, a powerful whoosh at its end (CC0 ×2), 0.8 s. **b**: hum b's flies swelling in, a big swoosh at its end (CC-BY 3.0 + CC0), 0.8 s. **Recommendation: a** (goes with hum a) | [a](../../audio/sfx/concept/swarm-lunge-r43-a.ogg), [b](../../audio/sfx/concept/rejected/swarm-lunge-r43-b.ogg) | the whoosh must not read as a pop; it is 0.8 s, the tell 0.8 s | a / b / redo | **a** (user, 2026-10-20) |

## Art — final

Level 20's production art (steps ART-G1, ART-G2, ICON-G; `tools/art/`), in the game now; every file is pending your
review. The Mote Swarm's own art is final since round 32 and does not change.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.a | [Level 20 backdrop](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#concept-art) | **final art** (`tools/art/backdrop_l20.py`): one strip keyed by strip row and flown both ways: the basin floor's salt flats and dune sea with the Vrell staging grounds and the dune ridge at t≈42, the terraces, the upper terraces and the rim. **Recommendation: approve as final** | [sheet](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/concept/backdrop-final-r43-a.png), [GIF](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/concept/backdrop-final-r43-a.gif) | the pillars' sizes are set in data (160 / 112 / 168 × 160) | approve as final / redo (say what) | approved as final (user, 2026-10-20) |
| F.b | [Level 20 props](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#concept-art) | **final art** (`tools/art/l20_props.py`): the travelling Colossus ridge (lengths, head and tail in 16 headings, the crest glow, sand slides; shared with Level 21) crossing the floor at t=12-20, and the emergency cache. **Recommendation: approve as final** | [sheet](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/concept/l20-props-final-r43-a.png), [GIF](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/concept/l20-props-final-r43-a.gif) | the ridge's path is a stand-in (see Q.4) | approve as final / redo (say what) | approved as final (user, 2026-10-20) |
| F.c | [Rear-guard drone and bay pod](../../player/weapons/rear-guard-drone-bay/README.md#concept-art) | **final art** (`tools/art/light_drone.py --rear`): the light drone's disc with a forked twin gun and blue fins, 16 gun headings, the trailing pair firing back and blocking, the break and the rebuild; the bay pod turned round (dock ring at the back) on each bank frame. **Recommendation: approve as final** | [sheet](../../player/weapons/rear-guard-drone-bay/concept/rear-guard-drone-final-r43-a.png), [GIF](../../player/weapons/rear-guard-drone-bay/concept/rear-guard-drone-final-r43-a.gif) | my reading: the light drone with a twin gun is close to the light drone at game size | approve as final / redo (say what) | approved as final (user, 2026-10-20) |
| F.d | [Rear-guard hangar icon](../../player/weapons/rear-guard-drone-bay/README.md#concept-art) | **final art** (`tools/art/icons.py --rear-guard`): the bay's shop icon, small and large. **Recommendation: approve as final** | [sheet](../../player/weapons/rear-guard-drone-bay/concept/rear-guard-icon-final-r43-a.png) | beside the Light Drone Bay's icon it differs by the reversed dock only | approve as final / redo (say what) | approved as final (user, 2026-10-20) |
| F.e | [Okafor's briefing image `level-20-hellas-fallback`](../../ui/briefing/README.md#concept-art) | **final art** (`tools/art/briefing_images.py`, 672×240): a CDF terrain plot of the Hellas basin's north side, the fallback route to the rim. **Recommendation: approve as final** | [image](../../../assets/ui/briefing/level-20-hellas-fallback.png) | seen only in the briefing; the walker report's page is Level 19's | approve as final / redo (say what) | approved as final (user, 2026-10-20) |

## Voices

Level 20's voices (step VOX-G, `tools/art/voice.py`: 22 lines and Rook's three "Up top!" barks, 25 files, four takes
pinned). Not heard by Claude beyond a speech check (Whisper read-back) and pitch. A name check: "Hellas" is read back
as "Heller's" or "Helis" in some takes, so listen for the name first.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| V.a | [Rook's "Up top!" barks](../../player/wingmen/README.md) | **listen**: the three rear-wave barks, said when a wave comes from the top by travel: "Up top! Coming down on us!", "Up top, Lancer! They're on our tails!", "Contacts up top! Keep moving!" | [1](../../../assets/voice/rook/46b437f17faf.ogg), [2](../../../assets/voice/rook/60315a316c8f.ogg), [3](../../../assets/voice/rook/e2f4b105f151.ogg) | flagged by VOX-G for a listen | accept / redo (say which) | accept (user, 2026-10-20) |
| V.b | [Okafor, the opening (t=1)](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#radio-chatter) | **listen**: "Hellas basin, Aegis. The Vrell are massing on the floor. Hit them before they move." (pinned) | [t=1](../../../assets/voice/okafor/3a33859ee853.ogg) | flagged by VOX-G: low in the first take, and one read-back said "the rail" | accept / redo | accept (user, 2026-10-20) |
| V.c | [Ares Control, the emergency cache](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#secrets-and-pickups) | **listen**: "Hellas Outpost's emergency cache. Better you than the Vrell." (pinned) | [secret](../../../assets/voice/ares-control/625324f0c4ed.ogg) | flagged by VOX-G for a listen; the name "Hellas" | accept / redo | accept (user, 2026-10-20) |
| V.d | [Ares Control (t=131)](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#radio-chatter) | **listen**: "Militia on the rim, Aegis. We can see your dust. Keep coming." | [t=131](../../../assets/voice/ares-control/69d12604b55b.ogg) | flagged by VOX-G for a listen | accept / redo | accept (user, 2026-10-20) |
| V.e | [Rook (t=113)](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#radio-chatter) | **listen**: the quiet beat, "Lancer... if I don't come back from one of these, call my mum yourself. No form letters." (pinned) | [t=113](../../../assets/voice/rook/ecd5c0dc3734.ogg) | flagged by VOX-G for a listen | accept / redo | accept (user, 2026-10-20) |
| V.f | [Okafor, the secondary](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#radio-chatter) | **listen**: "You thinned them on the way out. They'll remember that." (pinned) | [secondary](../../../assets/voice/okafor/da4292c3163f.ogg) | flagged by VOX-G for a listen | accept / redo | accept (user, 2026-10-20) |
| V.g | The other Level 20 lines | **accept or redo**: Okafor t=20 "All Aegis units, break off! Fall back to the rim!", t=75, t=152, the level end "We gave up the basin today…" and briefing page 1; Varga t=12.5, 27.5, 47, 106, 124.5 and briefing page 2; Ares Control t=35 and t=160; Rook t=56, 64, 98, 143.5 | [Okafor t=20](../../../assets/voice/okafor/a2a2f76cb87a.ogg), [t=75](../../../assets/voice/okafor/31f53ed02b6a.ogg), [t=152](../../../assets/voice/okafor/fe29401962a4.ogg), [level end](../../../assets/voice/okafor/37c606f178d8.ogg), [page 1](../../../assets/voice/okafor/9f5c31ec0e5f.ogg), [Varga t=12.5](../../../assets/voice/varga/d17f3ff0463f.ogg), [t=27.5](../../../assets/voice/varga/6e6d9867a780.ogg), [t=47](../../../assets/voice/varga/0460ab62a7ab.ogg), [t=106](../../../assets/voice/varga/9b39b49dfabb.ogg), [t=124.5](../../../assets/voice/varga/f4e186ca1f60.ogg), [page 2](../../../assets/voice/varga/e25bb31d7399.ogg), [Ares t=35](../../../assets/voice/ares-control/8ca54427014f.ogg), [t=160](../../../assets/voice/ares-control/c3b98722acb7.ogg), [Rook t=56](../../../assets/voice/rook/414f83c9beb8.ogg), [t=64](../../../assets/voice/rook/7ff3d2e81ad6.ogg), [t=98](../../../assets/voice/rook/3a9819cc3b09.ogg), [t=143.5](../../../assets/voice/rook/e5dc0742715c.ogg) | checked by Whisper read-back only | accept all / redo (say which) | accept all (user, 2026-10-20) |

## Captures

Game captures from a snapshot of the finished build (step G5a, `briefs/r43-captures.md`; Xvfb, the sound from a wave
file); flown by the content tests' autopilot, medium, the balance plan's fit (the walker's capture, W.a, is in its own
section). **They show the state before step GFX2's readability fixes** (record N.f) and are re-recorded under the same names.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| K.a | [Level 20](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#concept-art) | **game capture** (205 s, with sound): a whole medium run with the plan's fit and Rook's Autocannon (`concept/prompts.md`). To see: the strike and the Colossus ridge crossing, the turnabout (the scroll easing to zero at about 0:21 and reversing, the TOP "! REAR" and bottom "! AHEAD" warnings), the pursuing swarm's entry at 0:23, Rook's "Up top!" (0:55, 1:37, 2:22), the Wraiths from the rear, the nests and mortars on the dune ridge, the terraces and the rim road, the emergency cache on its ledge, the swarm dropping back, the lull with the supply drone and the armour patch, the second pursuit, the rim guns and their flak, the debrief (rank A, 167 of 197, the secondary met). **Recommendation: accept** | [video](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/concept/level-20-capture-r43-a.mp4), [sheet](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/concept/level-20-capture-r43-a.png) | flown by the test autopilot, not by you; **the swarm melts under the beam within a second or two, each refill too** (Q.1); the emergency cache passes unopened (SECRETS 0 of 1: the autopilot does not take it) | accept / change | accept (user, 2026-10-20) |
| K.b | [The pursuit swarm alone](../../enemies/air/mote-swarm/README.md) | **demonstration**: the same fit with the autopilot's fire held and no Rook, so the motes live: the entry under the TOP warning, the band at the top following the ship, the tell (the flock contracts and brightens), lunges down the lane and the climb back, refills joining, the break-off at t=108, the lull's start (105 s). Not a normal run: the ship never fires. It is what the swarm is meant to look like. **Recommendation: accept the look** (the lever question is Q.1) | [video](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/concept/level-20-swarm-r43-a.mp4), [sheet](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/concept/level-20-swarm-r43-a.png) | the ship never fires, so it shows the design, not a flight | accept / change (say what) | accept (user, 2026-10-20) |
| K.c | [The rear-guard drone in flight](../../player/weapons/rear-guard-drone-bay/README.md) | **game capture** (48 s, Level 20 from t≈15 to 63): the Rear-guard Drone Bay L1 in place of the left pod (the plan does not fit it): the drone trails below the ship through the turnabout and the first pursuit, swings out in turns and fires straight back beside the Tail Gun. **Recommendation: accept** | [video](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/concept/level-20-drone-r43-a.mp4), [sheet](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/concept/level-20-drone-r43-a.png) | one bay at level 1; the pursuit's motes die at the beam before the drone meets one | accept / change | accept (user, 2026-10-20) |
| K.d | [The drone's test fire in the shop](../../player/weapons/rear-guard-drone-bay/README.md) | **game capture** (12 s, silent): the hangar before Level 20, the left wing, the Rear-guard Drone (NEW, 2,400) selected, its test-fire box at level 1: the drone trails and fires back at the dummies as they pass behind. **Recommendation: accept** | [video](../../player/weapons/rear-guard-drone-bay/concept/rear-guard-drone-testfire-r43-a.mp4), [sheet](../../player/weapons/rear-guard-drone-bay/concept/rear-guard-drone-testfire-r43-a.png) | a test save, no sound | accept / change | accept (user, 2026-10-20) |

## Level 20's build

**B** rows: each item *yes* (keep as built) or *no* (say what). Level 20's numbers (the level-metrics tool, medium,
the plan's fit): **197 enemies (175 + 22 ground) ≈ 71 a minute**, 3.38 on the screen and 34.5 shots a minute, 2 gaps
over 2 s (hard none), the haul −0.3 % of the budget (2,525 against 2,532), the autopilot wins every seed on every
difficulty with Rook and without him.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.a | [Reverse scroll in the simulation](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#implementation) | **yes or no, each** (step PG1): (a)-(f) below. **Recommendation: all yes** | [K.a](#captures) | tested on a script built in the test, mirror property only | all yes / some no | all yes (user, 2026-10-20) |
| B.b | [Level 20's data and waves](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#waves) | **yes or no, each** (steps PG2, G4): (a)-(j) below. **Recommendation: all yes** | [K.a](#captures) | 71 a minute is below Levels 18–19's 101–107, by design | all yes / some no | all yes (user, 2026-10-20) |
| B.c | [The pursuit in the simulation](../../enemies/air/mote-swarm/README.md) | **yes or no, each** (step PG3): (a)-(g) below. **Recommendation: all yes** | [K.a](#captures) | the swarm itself melts (Q.1) | all yes / some no | all yes (user, 2026-10-20) |
| B.d | [The rear-guard drone bay](../../player/weapons/rear-guard-drone-bay/README.md) | **yes or no, each** (steps PG3, G4, GB): (a)-(d) below. **Recommendation: all yes** | [F.c](#art--final) | not in the balance plan, so no level test fits it | all yes / some no | all yes (user, 2026-10-20) |
| B.e | [Level 20 in the game](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#implementation) | **yes or no, each** (steps GA, GB): (a)-(h) below. **Recommendation: all yes** | [K.a](#captures) | the wording and colours are judgement calls | all yes / some no | all yes (user, 2026-10-20) |
| B.f | [Rook in the retreat](../../player/wingmen/README.md) | **yes or no, each** (steps PG1, PG3, G4): (a)-(d) below. **Recommendation: all yes** | [K.a](#captures) | Rook's play is the autopilot's, measured only | all yes / some no | all yes (user, 2026-10-20) |
| B.g | [The allocation guard](../../tech/architecture/README.md) | **yes or no, each** (step C1G, approved "if cheap"): (a)-(c) below. **Recommendation: all yes** | — | +34 s on `:content:check` (4:42 → 5:16, measured on a loaded machine) | all yes / some no | all yes (user, 2026-10-20) |

**Build choices (row B.a)** — reverse scroll (each yes/no):
- **(a)** A signed `Section` speed and `Sortie.travel`/`reversed`/`travelAt`: the sign picks entry edges, routes, ground objects, roads, backdrop tiling and parallax; front and rear read travel (`TravelFrame`).
- **(b)** A wave's `flipped` state is fixed per wave time (the mirror), so a wave is not flipped half-way through.
- **(c)** Chains and armoured walkers are never flipped.
- **(d)** A script that reverses is cross-checked and refuses terrain, `no-surface` set pieces and the other things that cannot run backwards.
- **(e)** A new `WarningEdge.TOP` (bit 3).
- **(f)** `MarsRules` eases the scroll after a reversed section.

**Build choices (row B.b)** — Level 20's data and waves (each yes/no):
- **(a)** Count-neutral, "tougher, not more": 197 enemies ≈ 71 a minute (easy 157, hard 239), the approved anchors keep their times.
- **(b)** The eight bottom popcorn threes became firing groups, then **firing pairs** on every difficulty (the rear guns serve one at a time); the Stinger V at t=71 a three; the Burrower fields at 52.5, 87 and 150 pairs.
- **(c)** Three spine nests ×3 (t≈66 on the basin floor, ≈96 on a terrace step, ≈155 on the rim road), and the rim road's nests at t≈140 and 155 stand on the road.
- **(d)** The lull as six kite flocks of 3, 2.5 s apart (t=110.5–123), three strike waves (t=11.5, 15, 18.5) and Needler pincers at t=22.5 and 25.5.
- **(e)** `bounty_scale` 0.83 (the haul −0.3 % of the budget), within the design's 0.70–0.85 band.
- **(f)** `small_act_hp` +20 and the beam-era HP apply; the Burrower, Dust Devil and Wraith take the act factor.
- **(g)** Each pursuit's break-off is its wave's `break_at` (t=108 and t=163; the second inside section 7, not "its section's end").
- **(h)** The BOTTOM lessons use the existing wave key `warning` on a front wave in a reversed section (no new `warn` key); the pursuits' and Wraiths' TOP warnings are 4 s on easy; the t=20.5 Skitters stream 0.6 s apart.
- **(i)** Motes that have no room in a 24-member flock are never spawned and count nowhere (not in the density, not in the kill ratio).
- **(j)** The secondary is the kill ratio at 65 %, reachable without a mote (`Level20Test`), judged at the level end.

**Build choices (row B.c)** — the pursuit (each yes/no):
- **(a)** Five phases: band, tell, dive, climb, break (`Pursuit`), the `WaveSpec.Pursuit` block in data.
- **(b)** The leader bobs ±40 px in the band.
- **(c)** The tell waits until the leader has reached the band.
- **(d)** The dive goes diagonally (the design says "straight down the ship's lane" to 140 px below where the ship was at the tell's start; PG3 flies it at an angle from the leader's band position).
- **(e)** The clump swirls during the tell.
- **(f)** Pursuit events fire only while motes are in the flock.
- **(g)** The trail drone's station follows with a smoothed lag.

**Build choices (row B.d)** — the rear-guard drone bay (each yes/no):
- **(a)** The drone flies at every level and in overdrive (`SimSpecs.flies` is true), 40 px behind the ship, firing back.
- **(b)** It is not in the balance plan: the plan's Level 20 visit buys the Tail Gun L3 and no level test is flown with the bay; it is the player's survival choice.
- **(c)** The weapons document's numbers (price 2,400 cr, draw 2 → 3 MW, unlock at Level 20, the per-level DPS and hits) are the wingmen table moved over unchanged (round 36, D.26).
- **(d)** The shop sells it from the hangar visit before Level 20.

**Build choices (row B.e)** — Level 20 in the game (each yes/no):
- **(a)** The warning labels by travel ("! REAR" and "! AHEAD", step GA's wording), the TOP edge's warning and the wave banners by travel.
- **(b)** The intel panel's TURN and LULL marks (the lull rule, the mark colours: GA's choices).
- **(c)** The tell's glow on the swarm (`PursuitLooks`) and the band's hum with the lunge sound (`PursuitCues`).
- **(d)** The rim flak is a `band: top` flak, and it acts on low-air units (a review point of step GB).
- **(e)** The ridge's path is a stand-in line for now (the pass's key is Q.4).
- **(f)** The *a* variants of the new sounds are in `assets/` as provisional (S.l–S.n), until you pick.
- **(g)** Rook's "Up top!" barks come from the data's `top_lines`, by travel, and stay silent for the BOTTOM entries.
- **(h)** The basin rumble is a second ambience layer (`ambience_layers`), and the ridge's pass is tied to its layer start (Q.4).

**Build choices (row B.f)** — Rook in the retreat (each yes/no):
- **(a)** His slot reads travel and he ignores the BOTTOM edge while the scroll is reversed.
- **(b)** The plan's fit gives him his owned Autocannon again (air-heavy level; your Q.2 of 2026-10-20).
- **(c)** He stays out of the lunge lane during a tell.
- **(d)** The autopilot's retreat play (`RetreatPlay`: the middle band, the unit it kills soonest, out of a lunge's path, a look-ahead over the bullets from both edges) is the measure.

**Build choices (row B.g)** — the allocation guard (each yes/no):
- **(a)** `:content:allocationTest` runs the 20 zero-allocation tests under `-XX:TieredStopAtLevel=1` (the C1-only JIT that exposed the macOS failure) in their own forks, parallel to `test`.
- **(b)** It is wired into `check`: +34 s (4:42 → 5:16).
- **(c)** The zero-allocation tests therefore run twice (once in `test`, once here).

## Questions

Points that came out of part G and were **not built** until you decide; the *Answer* column has the options, my
recommendation first.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| Q.1 | [The Mote Swarm still mostly melts](../../enemies/air/mote-swarm/README.md) | **Question.** After "homing ignores motes" (step SW) the swarm holds **0.77 motes on the screen** at medium (easy 0.35, hard 1.03) and 2–4 of 18 lunges carry motes: the beam and Rook's Autocannon still hit it on the nose. Levers, measured and not applied: **the band's lag 90 → 30 px/s: 0.87** (3–6 lunges with motes); **a lunge every 4 s instead of 6: 0.94** (6–7 of 27); together not measured. Others: the motes' HP, more refills. **Recommendation: both measured levers together** (data only, no code, no new rule; I would re-measure them together and report) | [K.a](#captures), [K.b](#captures) | the capture K.a shows the swarm dying within a second or two of its entry and of each refill; the levers are for the autopilot's play; a swarm that is mostly empty is a pursuit in name only | both levers (recommended) / band lag only / lunge every 4 s only / motes' HP / more refills / leave it | **both levers** (recommended) (user, 2026-10-20); data only, built after the close and re-measured |
| Q.2 | [The beam-era limit for a reversed level](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#waves) | **Question.** `BeamEraHpTest` holds small units to 2.5× Level 10's time on the screen; for a **reversed** level (the rear guns, not the beam, face them) it allows 2.5× and Level 20 is at **2.48×** (2.30× before the seekers skipped motes). **Recommendation: accept** (2.5× for a reversed level) | — | 2.48 against 2.5: no room left, a later change may break it | accept (recommended) / lower the limit and lower the HP | accept (recommended) (user, 2026-10-20) |
| Q.3 | [Turrets count as seekers](../../enemies/air/mote-swarm/README.md) | **Question.** `seekers_ignore` covers the player's homing shots, torpedoes, **turrets**, and Rook's target pick. The turrets are my reading: a turret that picks its own target is a seeker. **Recommendation: yes** | — | a turret that cannot aim at a mote may look broken | yes (recommended) / no, turrets still shoot motes | yes (recommended) (user, 2026-10-20) |
| Q.4 | [The ridge's path](../../campaign/act-3-red-dust/level-20-retreat-from-hellas/README.md#hazards) | **Question.** The travelling ridge's pass is tied to the **basin rumble's layer start** (`RidgeLooks`), and its path is a stand-in line. Level 21 shares the ridge. **Recommendation: a data key**: a level key `ridge {t, seconds, from, to, depth}` read by `RidgeLooks` (Level 20's track moves into its data; Level 21 uses the same key) | — | a key is a small content and game step | data key (recommended) / keep it tied to the rumble | **data key** (recommended) (user, 2026-10-20); built after the close |
| Q.5 | [`art: final` for the hangar and the briefing](../../ui/hangar/README.md) | **Question.** Round 42's close left `design/ui/hangar` and `design/ui/briefing` at `art: chosen` because their Concept art tables still hold older mockups next to the approved finals. **Recommendation: `final` for the approved items only** (the older mockups stay `chosen`, rejected files are not touched); the READMEs' `art:` stays `chosen` until every item is final | — | the frontmatter has one `art` value for a table of mixed items | final for the approved items only (recommended) / the parts' `art` to final too / leave | **final for the approved items only** (recommended) (user, 2026-10-20); the user asked: "When do we move the remaining hangar and briefing art to final?" An audit step lists the remaining `chosen` items for the next round |

## Part H's questions

Level 21 and the act end (the Dust Colossus), from the gap check ([part H plan](../../tech/roadmap/README.md#m7-parts)).
Part H starts as soon as these are answered; **nothing is built before**. Recommendations first.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| H.1 | [Level 21's pacing](../../campaign/act-3-red-dust/level-21-dust-colossus/README.md) | **Question.** Count-neutral swaps as Level 20: the Kite V-wings become swarms and 3–4 Kite groups become Needler/Stinger firing groups (≈ 107 enemies, ≈ 66 a minute), and more only if the measured floor fails. **Recommendation: yes** (your Level 20 answer 4) | — | the floor is measured only after the build | yes (recommended) / no, more enemies (say what) | yes (user, 2026-10-20), as recommended |
| H.2 | [Rook's gun at Level 21](../../player/wingmen/README.md) | **Question.** The balance plan carries Level 20's Autocannon. The approach is air-heavy and the debris rocks are `air`, which the Mortar cannot hit; the Autocannon still hits the risen mouth (`ground`). **Recommendation: keep the Autocannon** | — | the boss fight is `ground`: the Mortar would hit more of it | keep the Autocannon (recommended) / the Mortar | keep the Autocannon (user, 2026-10-20), as recommended |
| H.3 | [A pause before the boss](../../campaign/act-3-red-dust/level-21-dust-colossus/README.md) | **Question** (your round-41 note on Level 14). Drop the t=101 Stinger V-wing (or fold its four into earlier groups) so the screen clears while the Choir sings, with **no** incoming box (nothing to fly toward; the warning stays at t=105). Uses the third allowed gap. **Recommendation: yes** | — | the level loses 4 enemies and its last Stinger wave | yes (recommended) / no, keep the wave | yes (user, 2026-10-20), as recommended |
| H.4 | [Phase 1's calibration band](../../campaign/act-3-red-dust/level-21-dust-colossus/README.md) | **Question.** The spec says about the Kraken's loss or less; your walker note in round 42 asked for more shooting. **Recommendation: 0.8–1.2× the Kraken's 11.4 per 10 s** with the random-dodge harness: kind, but not idle (the walker is ×1.84, W.c) | — | the harness is not a player | 0.8–1.2× (recommended) / the walker's ×1.3–2 / less than the Kraken | 0.8–1.2× (user, 2026-10-20), as recommended |
| H.5 | [The boss bounty](../../campaign/act-3-red-dust/level-21-dust-colossus/README.md) | **Question.** When the measured `bounty_scale` moves from 0.80: keep the boss's share at **22.7 %** (bounty = 0.227 × 2,709 ÷ (2.56 × scale), ≈ 320 at 0.75), as long as the perfect run stays at least 1.4×. **Recommendation: yes** | — | the share is the highest of the campaign's bosses | yes (recommended) / a lower share (say what) | yes (user, 2026-10-20), as recommended |
| H.6 | [Section 6's ground](../../campaign/act-3-red-dust/level-21-dust-colossus/README.md) | **Question.** A "strip from the tile's seam" can wait up to a tile's length at 25 px/s, longer than the 32 s section, for a long tile. **Recommendation:** the arena tile repeats at 512 px and section 6's tiles start at the next seam (at most 20 s); the collapsed crater and the furrow are props placed at the kill | — | one more 512 px tile and a few props | the 512 px tile and props (recommended) / keep the strip | the 512 px tile and props (user, 2026-10-20), as recommended |
| H.7 | [Part H's small defaults](../../campaign/act-3-red-dust/level-21-dust-colossus/README.md) | **Proposals, each yes/no**: (a)-(h) below. **Recommendation: all yes** | — | (d) changes the release's end texts | all yes / some no | all yes (user, 2026-10-20) |

**Proposals (row H.7)** — part H's small defaults (each yes/no):
- **(a)** The ease 140 → 20 over the arena's first 2 s (t=105–107; the table's scroll px shift ≈ 120 px) instead of from t=103.
- **(b)** The suction pulls no pickups (phase 3's patch is not swallowed).
- **(c)** Debris stops the player's shots, not the enemy's bullets.
- **(d)** The release's end reads "ACTS I-III COMPLETE" and "ACT IV - DEEP WATER. EUROPA. COMING SOON.", the Continue button "…AT THE END OF ACT III", and the next act's name comes from a `next_act` key in Act 3's data.
- **(e)** The Level 3 strip's boss mark: the boss's silhouette at its time, drawn for Level 14 too.
- **(f)** The ridge pass as a level key `ridge {t, seconds, from, to, depth}` read by `RidgeLooks` (Level 20's track moved into its data), as Q.4's recommendation.
- **(g)** The usual 5 s launch from the bottom edge over the rim's CDF line (the design says "no launch" in the README).
- **(h)** A phase-2 point is fixed when its edge warning starts (3 s before the rise), the front one at its tell.

## Records

A record, nothing to answer.

| # | Item | Blocked by | Then |
|---|---|---|---|
| N.a | **The macOS CI fix** (commit b239705): `Level16Test.steppingAWholeLevelDoesNotAllocate` allocated 7,660,800 bytes on macOS only (ubuntu and windows green): the test autopilot's `HazardPlay.channelSpan` made a `double[2]` 21 times a step (672 B × 11,400 steps, exactly), which only the C1-only JIT does not remove; replaced by two doubles, bit-identical; the sim was clean | — | built, pushed; noted (user, 2026-10-20) |
| N.b | **The C1-only allocation guard** (your decision of 2026-10-20: "if cheap"): `:content:allocationTest`, +34 s on `check` | B.g | built; yes/no in B.g; noted (user, 2026-10-20) |
| N.c | **Round 42 closed** (docs 0): your 116 answers recorded; the Warden Tank and the Walker and Level 19's art final; `ui/hangar` and `ui/briefing` left at `art: chosen` | Q.5 | closed; noted (user, 2026-10-20) |
| N.d | **Commit 423f0ff** (signed, pushed to `m7-act-3`): part F, the playtest fixes, Levels 15–17's rework, the refactors | — | pushed; noted (user, 2026-10-20) |
| N.f | **Fixed after the first capture, re-recorded** (steps GFX2, G5R): the motes now pale-rimmed with light shadows, the swarm's tell a white-hot clump with a ring, the rim flak with a bright core (still short and small), the rear-guard drone outlined and its shot amber, the shop's names unclipped ("REAR-GUARD BAY" / "REAR-GUARD DRONE BAY L1"), no empty warning box. The emergency cache's strobe was a bug (the game never loaded a destructible's glow frames): fixed in step CSF; the clip `level-20-cache-strobe-r43-a` was recorded from a patched build before that fix. K.a-K.d show the fixed build | — | noted (user, 2026-10-20) |
| N.e | **Not started**: part H (Level 21), the ridge's data key (Q.4), the walker's stomp (W.e), the lever changes (Q.1) | the answers above | built after the round; noted (user, 2026-10-20) |

## Decisions

- 2026-10-20: round 43 opened (pending the user's review; nothing here is approved).
- 2026-10-20: closed round 43 (the user's answers from the phone page): everything accepted / yes / approve as final /
  as recommended / noted except the notes below. **W.a change**: "The first missile hatch is destroyed before the walker
  has a chance of ever using it. That's too easy. When both missile hatches are destroyed, the walker is mostly
  toothless. Can't you add something else that keeps the final part interesting?" **W.e stomp shockwave**: "And as
  noted above, something else for the longer range." Decided with the user the same day: the shoulder pods are armoured
  until their hatch opens, a back-mounted rail lance fires once the pods are gone (the longer range) and the stomp
  shockwave (step WR2; W.b-W.d's numbers are re-measured with it). **Sounds**: S.a **b**, S.c **b**, every other S row
  **a**; the picks are `chosen`, the other r43 variants and round 42's replaced sounds moved to
  `design/audio/sfx/concept/rejected/` (see the [sfx decisions](../../audio/sfx/README.md#decisions)). **F.a-F.e approved
  as final**: the Level 20 backdrop and props, the rear-guard drone, bay pod and hangar icon and Okafor's briefing image.
  The voices V.a-V.g and the captures K.a-K.d accepted; the build choices B.a-B.g (every sub-choice) stand as built.
  **Q.1 both levers** (the band's lag 90 to 30 px/s and a lunge every 4 s, data only), **Q.2 accept**, **Q.3 yes**,
  **Q.4 the ridge's pass gets a data key**, **Q.5 final for the approved items only**: the user asked "When do we move
  the remaining hangar and briefing art to final?", so an audit step lists the `chosen` items left in `ui/hangar` and
  `ui/briefing` for the next round. **Part H**: H.1, H.3, H.5 yes, H.2 keep the Autocannon, H.4 0.8-1.2x, H.6 the
  512 px tile and props, H.7 (a)-(h) all yes, recorded in the [Level 21](../../campaign/act-3-red-dust/level-21-dust-colossus/README.md#decisions)
  README and the part H note. The records N.a-N.f are noted. Still to build after the close: the walker's rework (WR2),
  the Q.1 levers, the Q.4 key and part H.
