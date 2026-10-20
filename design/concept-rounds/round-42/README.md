---
title: Concept round 42 — M7 part F (Level 19), the overnight playtest fixes, Levels 15-17's rework, the refactors
design: approved
implementation: n/a
art: chosen
depends-on: [../../campaign/act-3-red-dust/level-19-foundry-of-tharsis, ../../enemies/bosses/revenant-walker, ../../enemies/ground/warden-tank, ../../campaign/act-2-homefront/level-14-siege-spire, ../../campaign/act-3-red-dust/level-17-dome-siege, ../../audio/sfx, ../../audio/voice, ../../tech/roadmap, ../round-41]
updated: 2026-10-20
---

# Concept round 42 — M7 part F (Level 19), the overnight playtest fixes, Levels 15-17's rework, the refactors

## Summary

Everything below was built while you were away (overnight, 2026-10-19 to 2026-10-20); every row is pending your review
except the decisions of yours that are recorded as done. Five things wait for an answer:

1. **M7 part F, Level 19 Foundry of Tharsis** ([roadmap](../../tech/roadmap/README.md#m7-parts)): the final art (the
   Revenant Walker, the Warden Tank, the backdrop and props, the intel pictures, the briefing images: `F` rows), the
   thirteen sounds (`S`), the 25 voices (`V`), the capture (`K`) and the build choices (`B`).
2. **Your six playtest findings** of the evening (`pt3-findings`, `T.1`-`T.6`): each with its fix and, where there is
   one, its capture: accept or redo.
3. **Levels 15, 16 and 17 brought to the Act 2+ rules** (`R.a`-`R.c`) and the other review points the steps logged
   (`R.d`-`R.k`): the walker's and the tank's rules, the radio outro, the hard difficulty's crowding.
4. **The refactors** you approved (`X.a`): the `LevelScript` builder and split, `LevelData`'s `*Block` interfaces (you
   asked to review these), `SimSpecs`'s rules classes (with the duplicate `ChainRules` name), the autopilot's plays,
   the simulation split and the level-metrics tool.
5. **Records** (last section): your decisions taken as given, and what is not done.

**Part F in short.** The [Revenant Walker](../../enemies/bosses/revenant-walker/README.md) strides across the casting
yard (a stride arena: lanes, passes from every edge, 180° turns), fires homing missiles from two shoulder pods (a
hatch tell 0.4 s before a salvo; the missiles can be shot down), burns everything behind it with a flame cone (a 0.6 s
tell, the cone trigger by the ship's angle behind it) and vents its reactor after each burn (the weak spot); it
topples and its wreck, the serial plates filed off, is hint 2 (the crawl, Vorne's motif alone). The
[Warden Tank](../../enemies/ground/warden-tank/README.md) rides the conveyor belts (the belt speed added), its turret
tracks and fires a 3-shot burst, and the rear plate takes ×1.5. Furnace vents ripple across the floor (tell, burst,
`heavy` 10 once per burst), the gantry turrets face up the line (`arc: up`, they fire after you pass), and the
secondary is 12 of the 14 tanks.

Open [index.html](index.html) in a browser (regenerate with
`python3 tools/concept/board.py 42 --subjects revenant-walker-final,warden-tank-final,backdrop-final,l19-props-final,intel-final,briefing-images-final,walker-stomp,walker-hatch,walker-missile,walker-flame-tell,walker-flame,vent-tell,vent-burst,tank-tracks,tank-turret,tank-burst,tank-death,belt-rumble,mars-foundry,level-19-capture,level-14-fixes,level-17-fixes,level-15-end`).
Each row carries a recommendation; **B**, **T** and **X** rows list build choices: *yes* (keep as built) or *no* (say what).

## In the game

`./gradlew :desktop:run --args="..."` with `--settings <file>` (a debug run writes no save):
- Level 19 with the balance plan's ship and Rook's mortar: `--start level --level 19 --loadout front=ion-beam:3,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:2,generator=2,shield=1,armor=2 --special airstrike:2 --escort rook:mortar:1`
  (the plan's entry "19": the Ion Beam at level 3 and Rook's owned Mortar; `--difficulty easy|hard`; `--invulnerable`
  to see the whole level, `--escort none` to fly without Rook);
- Level 14's Spire, Level 17's domes and Level 15's end: the same options with `--level 14|17|15` and your Act 2 or
  the plan's ship.

## Art — final

Production art for part F (`tools/art/`, steps ART-F1 to ART-F5), in the game now; every file is pending your review.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.a | [Revenant Walker](../../enemies/bosses/revenant-walker/README.md#concept-art) | **final art** (`tools/art/revenant_walker.py`, the chosen r36 a "Strider heavy" biped): 8 headings × an 8-frame walk, the hull with the front plate and the chin cannon, the two reactor vents (shut, open, overheated), the shoulder pods (16 aim headings, the hatch cells lit as the 0.4 s tell) and stubs, the flamer (32 aims) and its pilot light, the 6-step topple and the wreck with the serial plate ground blank (both at 4×), the homing missile and its smoke, the flame-cone puffs. **Recommendation: approve as final** | [sheet](../../enemies/bosses/concept/revenant-walker-final-r42-a.png), [GIF](../../enemies/bosses/concept/revenant-walker-final-r42-a.gif) | 72 px/unit; the hatch tell was weak in the first pass and was re-rendered with lit cells (look at it in the capture); the hull flash was solid white under fire (G1), now a short partial flash (N.f); the wreck is seen from the flank | approve as final / redo (say what) | approved as final (user, 2026-10-20); the walker's art row moves with step WR (K.a) |
| F.b | [Warden Tank](../../enemies/ground/warden-tank/README.md#concept-art) | **final art** (`tools/art/warden_tank.py`, the chosen r36 grey): hull 16 headings × 2 tread frames, the turret 32 aims at rest and recoil, the muzzle flash, the turret stub after it is shot off, the burnt grey wreck (16 headings). **Recommendation: approve as final** | [sheet](../../enemies/ground/concept/warden-tank-final-r42-a.png), [GIF](../../enemies/ground/concept/warden-tank-final-r42-a.gif) | 76 px sprites; a grey rim light; the silhouette is a bit noisy at game size; the stub's embers; the wreck is plain | approve as final / redo (say what) | approved as final (user, 2026-10-20) |
| F.c | [Level 19 backdrop](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#concept-art) | **final art** (`tools/art/backdrop_l19.py`, 22 files): the slag fields with the smelter tower seen from above, the halls, the furnace row, the yard approach and gate, the casting yard, the wreck's crawl and the south gate; the belts' four frames and the belt plates (the belt runs through the south gate; the first pass stopped 30 px short of its wall, fixed in ART-F5). **Recommendation: approve as final** | [sheet](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/concept/backdrop-final-r42-a.png), [GIF](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/concept/backdrop-final-r42-a.gif) | the slag reads rust-brown rather than molten; the gate is built of segments; the smoke is faint; the tower is only a mark from above | approve as final / redo (say what) | approved as final (user, 2026-10-20) |
| F.d | [Level 19 props](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#concept-art) | **final art** (`tools/art/l19_props.py`, 15 sets): the furnace vent grate (shut, the tell's three steps) with its glow, the flame column and smoke; the ore gantry and the up-facing nest's cradle (chevrons pointing up the line); the payroll safe (intact, hit, opened) and its glow; the shift office; the ingot stacks and pallet; the crews' rover. **Recommendation: approve as final** | [sheet](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/concept/l19-props-final-r42-a.png), [GIF](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/concept/l19-props-final-r42-a.gif) | the safe is drawn three-quarters with its door up; the assay strip on the plate file reads "HELIX TF-7" (the Helix tell); the gantries are sprites, so a cradle may sit up to 52 px off its truss (G2) | approve as final / redo (say what) | approved as final (user, 2026-10-20) |
| F.e | [Intel pictures](../../ui/hangar/README.md#concept-art) | **final art** (`tools/art/intel.py`): the Warden Tank's and the Revenant Walker's portraits and the `boss-revenant-walker` "unclassified, huge" contact (a silhouette with sensor noise, shown until the walker has been flown) | [sheet](../../ui/hangar/concept/intel-final-r42-a.png) | — | approve as final / redo | approved as final (user, 2026-10-20) |
| F.f | [Briefing images](../../ui/briefing/README.md#concept-art) | **final art** (`tools/art/briefing_images.py`): `level-19-tharsis-works` (Okafor's page), `level-19-rear-arcs` (Varga's page: the tanks' rear plates and the gantries' arcs) and `level-20-filed-plate` (Varga's Level 20 page, the walker wreck's plate, made early). **Recommendation: approve as final** | [sheet](../../ui/briefing/concept/briefing-images-final-r42-a.png) | the rear-arcs image follows the briefing's wording ("they fire after you pass"); the tank's rear plate faces the ship while it drives up the belt (B.a h) | approve as final / redo | approved as final (user, 2026-10-20) |

## Sounds

Part F's sounds ([sfx](../../audio/sfx/README.md#concept-art), `tools/concept/audio/sfx_r42.py`, all synthesized, step
AUD-F), **one variant each**, imported into the game (step G1). Not listened to by Claude: checked by envelopes,
spectrograms, levels and loop seams; no bubbly or popping layers, every blast with a ramped attack.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.a | [Walker stomp](../../audio/sfx/README.md#concept-art) | a dull heavy stomp with a leg clank and a hydraulic hiss, 1.0 s, every footfall | [a](../../audio/sfx/concept/walker-stomp-r42-a.ogg) | −19 dB (the −4 dBFS ceiling limits it): may be too quiet for the biggest unit | accept / redo | accepted (user, 2026-10-20) |
| S.b | [Walker hatch](../../audio/sfx/README.md#concept-art) | a latch tick, a pneumatic hiss and a hinge clack, 0.45 s, 0.4 s before a salvo | [a](../../audio/sfx/concept/walker-hatch-r42-a.ogg) | −22 dB; the tell it carries is easy to miss in a busy fight | accept / redo | **redo** (round 43): "That's not a hatch, that's a pop. Isn't this supposed to be a heavy thing? Search for sounds in the free database." |
| S.c | [Walker missile](../../audio/sfx/README.md#concept-art) | a dry ignition crack and a rocket whoosh sliding to one side, 0.8 s, one pod missile (the planned choice was the round-08 enemy missile). **Recommendation: accept** the new one | [a](../../audio/sfx/concept/walker-missile-r42-a.ogg), [enemy-missile r08-a](../../audio/sfx/concept/enemy-missile-r08-a.ogg) | compare with r08-a; up to six alive at once | accept / use the r08-a / redo | **use the r08-a** (user, 2026-10-20): the walker missile takes the round 08 variant a |
| S.d | [Walker flame tell](../../audio/sfx/README.md#concept-art) | a gas hiss rising and swelling over a low gas rush, 0.7 s, the cone's 0.6 s tell | [a](../../audio/sfx/concept/walker-flame-tell-r42-a.ogg) | −22 dB | accept / redo | **redo** (round 43): "Again way too small. Search in the database for large venting." |
| S.e | [Walker flame](../../audio/sfx/README.md#concept-art) | a flickering flame roar over a low body and a gas hiss, a seamless 2.0 s loop while the cone burns | [a](../../audio/sfx/concept/walker-flame-r42-a.ogg) | −28 dB, a loop | accept / redo | **redo** (round 43): "Also too small. Search for large gas burns or large flamethrower" |
| S.f | [Vent tell](../../audio/sfx/README.md#concept-art) | a rising gas roar over a deepening rumble, 1.05 s, the vent's 1.0 s tell | [a](../../audio/sfx/concept/vent-tell-r42-a.ogg) | −22 dB | accept / redo | **redo** (round 43): "Same thing" |
| S.g | [Vent burst](../../audio/sfx/README.md#concept-art) | a fire blast (40 ms attack, no pop) roaring out and dying, 1.5 s | [a](../../audio/sfx/concept/vent-burst-r42-a.ogg) | a whole ripple of vents plays it up to three at once | accept / redo | **redo** (round 43): "Same thing" |
| S.h | [Tank tracks](../../audio/sfx/README.md#concept-art) | grinding treads with track-link ticks, a diesel hum and a faint turbine whine, a seamless 2.0 s loop, the nearest tank | [a](../../audio/sfx/concept/tank-tracks-r42-a.ogg) | −32 dB, a loop | accept / redo | **redo** (round 43): "Search for sounds of a real tank driving" |
| S.i | [Tank turret](../../audio/sfx/README.md#concept-art) | a servo whine with gear ticks, 0.6 s, the slew | [a](../../audio/sfx/concept/tank-turret-r42-a.ogg) | G2 flags the whine at the lock (the tell) as maybe too long | accept / redo | **redo** (round 43): "You should really stop trying to make these sounds yourself. Use the database." |
| S.j | [Tank burst](../../audio/sfx/README.md#concept-art) | a heavy autocannon, four rounds 0.12 s apart, 0.75 s | [a](../../audio/sfx/concept/tank-burst-r42-a.ogg) | −18 dB (the ceiling limits it); the burst is 3 shots, the file 4 rounds | accept / redo | **redo** (round 43): "Not a heavy cannon. Not even a small cannon." |
| S.k | [Tank death](../../audio/sfx/README.md#concept-art) | a deep boom, a falling fire roar, a metal clang, eight pieces of debris and sparks, 1.8 s | [a](../../audio/sfx/concept/tank-death-r42-a.ogg) | up to 14 tanks die in a level | accept / redo | accepted (user, 2026-10-20) |
| S.l | [Belt rumble](../../audio/sfx/README.md#concept-art) | a roller rumble, plate seams knocking six a second and a motor hum, a seamless 2.4 s loop, optional (the plan folds the belts into the bed). **Recommendation: keep the loop, but drop it if it muddies the bed** | [a](../../audio/sfx/concept/belt-rumble-r42-a.ogg) | compare with the bed below | keep / drop / redo | kept (user, 2026-10-20) |
| S.m | [The `mars-foundry` bed](../../audio/sfx/README.md#concept-art) | a conveyor rumble, a far furnace roar, a slow hammer every 4 s with a hall echo and the plateau wind, a seamless 16 s loop | [a](../../audio/sfx/concept/mars-foundry-r42-a.ogg) | −36 dB, a loop | accept / redo | accepted (user, 2026-10-20) |
| S.n | The sounds not made | the walker's vent hit, its death (the `huge` rung of the explosion ladder) and the wreck's settle are not made (not in the AUD-F brief); the game falls back to its existing sounds there, or to none. **Recommendation: accept for now** | — | the walker's death is the level's biggest moment | accept / make these three | **make these three** (user, 2026-10-20): the walker's vent hit, its death and the wreck's settle, in round 43 (from the free database) |

## Voices

Level 19's voices (step VOX-F, `tools/art/voice.py`, 25 new files, nine takes pinned; Okafor's t=148.5 "Not on this
channel, Doctor." reuses Level 17's take) and Level 17's reworded line. Not heard by Claude beyond a speech check (Whisper
read-back) and pitch: the speaker's pitch on the unfiltered takes is Okafor 142-168 Hz, Varga 181-209, Rook 125-181,
Ares Control 110-130. The timed lines never overlap (the closest, t=38 → 45.5, leaves about 3 s).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| V.a | [Varga, "Grey tanks" (t=30)](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#radio-chatter) | **listen**: "Grey tanks on the conveyors. Their back plates are thin." | [t=30](../../../assets/voice/varga/f7af6e49cb2a.ogg), [briefing page 2](../../../assets/voice/varga/a3f6de76fed2.ogg) | Whisper hears "Great tanks" in all five seeds tried (the page says "Grey tanks" too) | accept / redo (say which line) | accepted (user, 2026-10-20) |
| V.b | [Okafor's briefing page 1](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#briefing) | **listen**: "Tharsis Foundry went quiet two days ago…" | [page 1](../../../assets/voice/okafor/4993eb9c18ab.ogg) | "crew are sealed" is read back as "crew were sealed" | accept / redo | accepted (user, 2026-10-20) |
| V.c | [Rook, "Six o'clock! Cloakers!" (t=45.5)](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#radio-chatter) | **listen**: the key's first take was barely voiced; the pinned seed is the repair. Rook's other lines: t=38 "Tanks on a conveyor belt. Someone hates walking.", t=77 "Coming up behind, Lancer!" (an octave jump on a quarter of it, pinned), t=97.5, t=116 "Behind us!" (a squeal at about 570 Hz, pinned) | [t=45.5](../../../assets/voice/rook/9484b960ac26.ogg), [t=38](../../../assets/voice/rook/4e9cd3cc388d.ogg), [t=77](../../../assets/voice/rook/d30d8395ac50.ogg), [t=97.5](../../../assets/voice/rook/315f97f1cd3f.ogg), [t=116](../../../assets/voice/rook/edd94197e037.ogg) | the pinned seeds may still be thin; Rook's lines are only for flights with him | accept / redo (say which line) | accepted (user, 2026-10-20) |
| V.d | [Varga, the flame tell](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#radio-chatter) | **listen**: "It burns what's behind it. Stay out of the cone, then hit the vents." at the walker's first telegraph (pinned: the first seed had an octave jump) | [take](../../../assets/voice/varga/33887c6a4a28.ogg) | the line is long for a 0.6 s tell; it plays at the first telegraph only | accept / redo | accepted (user, 2026-10-20) |
| V.e | [Level 17's reworded line](../../campaign/act-3-red-dust/level-17-dome-siege/README.md#radio-chatter) | **listen** (the secondary is now "No dome lost"; "Five domes, not a crack." no longer fits): Ares Control, "All five domes still standing. Fine. Earth can stay for dinner." (about 130 Hz, no jump) | [take](../../../assets/voice/ares-control/4ea0bb4b9661.ogg) | — | accept / redo | accepted (user, 2026-10-20) |
| V.f | Okafor's other Level 19 lines | **accept or redo**: t=1 "Tharsis Foundry, Lancer…" (pinned: the key's seed said "The rail" for Vrell), t=88 "Grey drones again, same as the storm…" (pinned), t=124.5, t=163, the secondary's "Not one grey tank left the foundry. Good." (the secondary is 12 of 14: B.a g) and the level end's "Tharsis is breathing again. Come home, Aegis." (pinned) | [t=1](../../../assets/voice/okafor/ffc6912d14b0.ogg), [t=88](../../../assets/voice/okafor/972444750345.ogg), [t=124.5](../../../assets/voice/okafor/8ff4058d9873.ogg), [t=163](../../../assets/voice/okafor/5ac0046769db.ogg), [secondary](../../../assets/voice/okafor/05202713a201.ogg), [level end](../../../assets/voice/okafor/3c758d205aa3.ogg) | "Vrell" reads "Vrel" | accept / redo (say which line) | accepted (user, 2026-10-20) |
| V.g | [Varga's other Level 19 lines](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#radio-chatter) | **accept or redo**: t=14 "Gantry turrets ahead face up the line. They fire once you're past.", t=66 "A singer with escorts…", t=137 "Lancer, the serial plates are filed off. By hand. And that alloy is Helix foundry stock." (hint 2) | [t=14](../../../assets/voice/varga/7b13fcf9e4b2.ogg), [t=66](../../../assets/voice/varga/7d492352cab0.ogg), [t=137](../../../assets/voice/varga/6aafccca2ac9.ogg) | — | accept / redo (say which line) | accepted (user, 2026-10-20) |
| V.h | [Ares Control's Level 19 lines](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#radio-chatter) | **accept or redo**: t=22.5 "Two hundred foundry crew are sealed in the smelter tower.", t=57.5 "Furnace row is next…", t=105 (pinned: high on 30 % of it), t=155.5 (pinned), the payroll safe's line | [t=22.5](../../../assets/voice/ares-control/23d37efa6ea0.ogg), [t=57.5](../../../assets/voice/ares-control/61724c8b6f28.ogg), [t=105](../../../assets/voice/ares-control/0eabee53ee1a.ogg), [t=155.5](../../../assets/voice/ares-control/4e76806dd7b1.ogg), [safe](../../../assets/voice/ares-control/3343f7759aff.ogg) | — | accept / redo (say which line) | accepted (user, 2026-10-20) |
| V.i | [Rook's boss lines](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#radio-chatter) | **accept or redo**: the first pod, "Pod's gone. One more."; the walker's death, "Walker's down. No colours, no numbers." | [pod](../../../assets/voice/rook/98c0dc787878.ogg), [down](../../../assets/voice/rook/061bf3ba4be2.ogg) | — | accept / redo | accepted (user, 2026-10-20) |

## Captures

Game captures from a snapshot of the finished build (step F5a, `briefs/r42-captures.md`; Xvfb, the sound from a wave
file); every run is flown by the content tests' autopilot, medium, with the balance plan's fit.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| K.a | [Level 19](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#concept-art) | **game capture**: a whole medium run with the plan's fit and Rook's mortar (`concept/prompts.md`). To see: the tanks on the belts (the turret tracking, the lock tell, a burst up the screen, the rear-plate hit), the vents' tell and burst, the gantries and nests, the walker's warning box, its passes, the hatch tell and missiles, the cone's tell and burn, the topple, the wreck crawl with the motif alone, the south gate, the 12/14 counter and the debrief | [video](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/concept/level-19-capture-r42-a.mp4), [sheet](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/concept/level-19-capture-r42-a.png) | flown by the test autopilot, not by you (a ship with no input never kills the walker): the capture runs to the topple, the crawl, the south gate and the debrief (261 s); re-recorded after the readability fixes (N.f): the hull stays grey under the beam, short partial hit flashes, the cone a filled flame, the counter DONE at the 12th tank. Still weak (the capture step's eye): the missiles are small (≈ 30 px with the trail) and get lost against the hull; a destroyed pod leaves a black stump blotch beside the hull; the beam heat on a part is too faint to see | accept / change | **change** (user, 2026-10-20): "The walker looks like a real easy target. It needs to do a lot more shooting and the flame needs to be bigger." Step WR (walker rework) |
| K.b | [Level 14's Spire](../../campaign/act-2-homefront/level-14-siege-spire/README.md) | **check capture** of your findings 1, 2 and 5: the last waves, the gap, the warning box before the Spire arrives, no fire under it, one sweep cycle, the death | [video](../../campaign/act-2-homefront/level-14-siege-spire/concept/level-14-fixes-r42-a.mp4), [sheet](../../campaign/act-2-homefront/level-14-siege-spire/concept/level-14-fixes-r42-a.png) | — | accept / change | accepted (user, 2026-10-20) |
| K.c | [Level 17's domes](../../campaign/act-3-red-dust/level-17-dome-siege/README.md) | **check capture** of your finding 4: a medium run's dome section, the bombers' run-in and the debrief's `DOMES STANDING n / 5` (5 / 5 in this run; one dome turns yellow and still counts) | [video](../../campaign/act-3-red-dust/level-17-dome-siege/concept/level-17-fixes-r42-a.mp4), [sheet](../../campaign/act-3-red-dust/level-17-dome-siege/concept/level-17-fixes-r42-a.png) | — | accept / change | accepted (user, 2026-10-20) |
| K.d | [Level 15's end](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md) | **check capture** of your finding 3: the last 25 s, the hand-over to the debrief, the closing line heard in full (the closing voice ends about 3.2 s before the debrief opens) | [video](../../campaign/act-3-red-dust/level-15-olympus-descent/concept/level-15-end-r42-a.mp4), [sheet](../../campaign/act-3-red-dust/level-15-olympus-descent/concept/level-15-end-r42-a.png) | the cut was modelled, not heard (R.g) | accept / change | accepted (user, 2026-10-20) |

## Playtest fixes

Your six findings of the evening ("Just record these findings, don't fix them yet"), fixed overnight. Each row names
what was built; **T.1**, **T.2** and **T.5** are checked in K.b, **T.4** in K.c and **T.3** in K.d.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| T.1 | [Level 14's Spire approach](../../campaign/act-2-homefront/level-14-siege-spire/README.md) | **accept or redo** (steps FX-S2, FX-G, FX-C): a short quiet gap between the last enemies and the Spire, the warning box shown *before* the Spire is reached and the Spire silent until it has settled. Build choices below. **Recommendation: accept** | [K.b](#captures) | the gap is data (the waves were pulled earlier); the check is the capture | accept / redo (say what) | accepted (user, 2026-10-20); every sub-choice yes |
| T.2 | [Level 14's sweep](../../campaign/act-2-homefront/level-14-siege-spire/README.md) | **accept or redo** (FX-S2, FX-C): the sweep leads the ship and turns so that no spot is safe while standing; the autopilot has to cross the line. Build choices below | [K.b](#captures) | the core's HP was cut (4,950 → 3,450) so that the fight lasts about 102 s at medium with the moving ship: my own call | accept / redo | accepted (user, 2026-10-20); every sub-choice yes |
| T.3 | [The last radio line](../../audio/voice/README.md) | **accept or redo** (steps FX-G, RX, FX-C): a test on every level that its last radio line, with its take's length, ends before the debrief; Levels 10 and 13 extended; the radio outro mode in the game. Build choices below | [K.d](#captures) | the cut was found by a model of the outro, not heard in the game; the debrief waits for a closing line instead of the level being extended (you said extend it) | accept / change (extend the levels) | accepted (user, 2026-10-20); every sub-choice yes |
| T.4 | [Level 17's domes](../../campaign/act-3-red-dust/level-17-dome-siege/README.md) | **accept or redo** (steps FX-S2, FX-C, PE17, RX): the secondary is "No dome lost" (a dome may be hit and yellow, none may break), the bombers' first drop waits 2.5 s (the run-in), the debrief row `DOMES STANDING n / 5`; you chose *both* fixes. Measured with the autopilot at 8 seeds: easy 8/8, medium 8/8, hard 1/8 | [K.c](#captures) | hard loses a dome nearly every run (Eden's bombers at t≈129): is that what you want on hard? | accept / ease hard | accepted (user, 2026-10-20); every sub-choice yes |
| T.5 | [Level 14's Spire death](../../campaign/act-2-homefront/level-14-siege-spire/README.md) | **accept or redo** (FX-S2, FX-G): the dead arena boss scrolls with the ground and its death bursts stay on the ground, like the wreck | [K.b](#captures) | — | accept / redo | accepted (user, 2026-10-20) |
| T.6 | [Shop mouse handling](../../ui/hangar/README.md) | **accept or redo** (step FX-G, `HangarPointerTest`): the selection stays with the clicked item; hover only outlines and moves nothing, so you can click a weapon and move to the upgrade control | — (no capture: a test) | hovering over a choice chip previews it | accept / redo | accepted (user, 2026-10-20) |

**Build choices (row T.1)** — Level 14's approach (each yes/no):
- **(a)** A `boss.incoming` event 7.5 s before the Spire's arrival raises the large warning box; the box closes when the boss fights under it.
- **(b)** The Spire never fires before it has settled; the Roots' first delay is 1.5 s.
- **(c)** The waves are pulled earlier so the last enemies die about 3-4 s before the Roots start.

**Build choices (row T.2)** — Level 14's sweep (each yes/no):
- **(a)** The sweep line is 600 px long, telegraphed 0.8 s and leads the ship by 12°, so a still ship is hit; there is no safe still spot.
- **(b)** The autopilot crosses the line to dodge it.
- **(c)** The core's HP 4,950 → 3,450 (medium 101.9 s).
- **(d)** Not done: the sweep is not clipped to the play field, and alternate sweeps pin the corner rather than "a line toward the core" (your wording).

**Build choices (row T.3)** — the last radio line (each yes/no):
- **(a)** `RadioTimelineLevelsTest` checks every level that its last line, with the voice take's length, ends before the level does (Level 19's run checked by `RadioTimelineLevel19Test`).
- **(b)** Levels 10 and 13 extended by 1 s and 4 s (the level ends at t=201 and t=239).
- **(c)** The radio outro mode: waiting and new event lines are dropped, a non-closing voiced line closes at its voice's end, and the debrief waits for the closing lines; the longest overrun is 2.1 s (Level 15), the limit 3 s; backdrops hold 3 s past the outro (not Levels 01-02).
- **(d)** A Rook bark at the end no longer holds the closing lines back (`Barks.levelComplete()` withdraws the barks after the win); the voice's last frame is no longer clipped.
- **(e)** Level 16's secondary line "Every barrier down" no longer queues behind the 166/173/180 lines (those moved to 168/175/182).

**Build choices (row T.4)** — Level 17's domes (each yes/no):
- **(a)** The secondary is "No dome lost": a dome may be hit and turn yellow, only a dome that breaks fails it.
- **(b)** The bombers' run-in is 2.5 s (`allies.bomb_run_in`) and the secondary counts the domes still standing at the end (`Secondary.standing`).
- **(c)** The Burrower fields: six, with a `no_surface` block (PE17).
- **(d)** The old line "Five domes, not a crack." became "All five domes still standing." (V.e).

## Level 19's build

**B** rows: each item *yes* (keep as built) or *no* (say what). Level 19's numbers (the level-metrics tool, medium): **226
enemies (193 + 33) ≈ 107 a minute**, 2.83 on the screen and 27.8 enemy shots a minute, one gap of 3.8 s (the death
chain), the haul +1.4 % of the budget (2,399 against 2,366), the walker's fight 56.7 s against its par of 53.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.a | [Level 19's data](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#waves) | **yes or no, each** (steps F2, F4): (a)-(h) below. **Recommendation: all yes** | [K.a](#captures) | the floor is met by density (not an exception); 107 a minute is above the 88 the plan said, the beam era's "tougher, not more" would argue for fewer | all yes / some no | all yes (user, 2026-10-20) |
| B.b | [The Revenant Walker in the simulation](../../enemies/bosses/revenant-walker/README.md) | **yes or no, each** (steps F1, F4): (a)-(h) below. **Recommendation: all yes** | [K.a](#captures) | measured with the autopilot, not by you | all yes / some no | all yes (user, 2026-10-20) |
| B.c | [Tanks, belts, vents and gantries in the simulation](../../enemies/ground/warden-tank/README.md) | **yes or no, each** (step F3): (a)-(g) below. **Recommendation: all yes** | [K.a](#captures) | — | all yes / some no | all yes (user, 2026-10-20) |
| B.d | [Level 19 in the game](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#implementation) | **yes or no, each** (steps G1, G2): (a)-(h) below | [K.a](#captures) | the walker's death and the crawl (sections 6-7) were not seen live before the capture | all yes / some no | all yes (user, 2026-10-20) |

**Build choices (row B.a)** — Level 19's data (each yes/no):
- **(a)** The floor by density, not by an exception in `PacingTest`: 226 enemies, ≈ 107 a minute, 2.83 on the screen, one gap (3.8 s, the death chain); easy 38 for the walker, hard 79.
- **(b)** `bounty_scale` 0.28 (below the 0.3 aim): the haul +1.4 % of the budget; the walker's bounty 495.
- **(c)** Rook flies his mortar at Level 19 (the balance plan's fit `[escort, mortar]`; round 36's open question, my recommendation yes).
- **(d)** `small_act_hp` +19 and the beam-era HP (`beam_era_hp`) apply; bosses aside.
- **(e)** The walker: 3,740 HP (hull 2,720, pods 510 each), ×1.7 of the first design's 2,200; par 53 s (56.7 s measured at medium), easy 38 s, hard 79 s.
- **(f)** The scroll: the walker's lanes at y 200 and 330, the incoming warning 3 s, the outbound belt at x 240, the crawl section at 133-145 in the level's data (the scroll holds after the boss and eases into the crawl).
- **(g)** The secondary is 12 of the 14 tanks while Okafor says "Not one grey tank left the foundry" (the line fits the success; 12 of 14 is the data).
- **(h)** The tank's rear plate faces the ship while the tank drives up the belt toward it (the plate takes ×1.5 on the approach), while the briefing says the gantries fire "after you pass".

**Build choices (row B.b)** — the walker in the simulation (each yes/no):
- **(a)** A stride arena like the Kraken's slam arena: lanes, passes from every edge, the 180° turn takes 1.5 s.
- **(b)** The flame cone triggers when the ship is behind it at any angle within its length + 40 px.
- **(c)** The phase-2 swivel comes after phase 1 only.
- **(d)** It fires only from pods and parts that are on the screen.
- **(e)** The homing missiles can be shot down; the walker's homing does not lock onto missiles.
- **(f)** Phase 1 is not kinder than the later phases (the design's "kinder phase 1" is only the lane beside the ship).
- **(g)** The autopilot's walker play (pods first, escape the cone, shoot missiles, filter the vents) and Rook's (keeps out of the cone, pods first); the random dodge 12.3 against the Kraken's 11.4.
- **(h)** The wreck stays as scenery under the crawl; the topple and the wreck the same step as the death.

**Build choices (row B.c)** — tanks, belts, vents, gantries (each yes/no):
- **(a)** Belts carry ground units and wrecks only (not pickups) and are roads moving at 40 px/s.
- **(b)** The vent's hit box 44 × 44; the ripple starts from the first vent's x.
- **(c)** The turret stays locked through its burst and a lost turret silences the tank.
- **(d)** The scroll holds after the boss (`hold_after_boss`) with a smoothstep ease; `MarsRules` keeps the crawl ease after the arena.
- **(e)** Rook keeps out of the cone, the vents and the pods' fire (`RookHazards`).
- **(f)** `arc: up` nests are silent while the ship is above them and fire after it passes.
- **(g)** `FurnaceVents` always exist for a sortie (`sortie.vents()` is never null).

**Build choices (row B.d)** — Level 19 in the game (each yes/no):
- **(a)** The HUD counter `GREY TANKS n/12`.
- **(b)** The looks: the walker's (`WalkerLooks`, death and missile effects), the tanks' (`TankLooks`, `BeltWrecks`) and the foundry's (`FoundryLooks`: vents, gantries and cradles, the office and safe, ingots, rovers); sprites claimed in `SpriteUse`.
- **(c)** The 13 sounds are imported; the `mars-foundry` ambience; `WalkerCues` and `FoundryCues`.
- **(d)** The motif stem alone over the crawl (`StemMix` stems gain and `motifAlone`).
- **(e)** The intel: hazards wrap, the unclassified boss row, `unclassified_until_flown`.
- **(f)** The cradles may sit up to 52 px off their truss and trusses run under the tanks (the props are placed game-side).
- **(g)** The hit flash: a short partial blend, at most one every 0.25 s (the Kraken's rule; step GFX, after the first capture showed a solid white hull).
- **(h)** `AtlasPacker` crops the walker's legs, topple and wreck so Level 19 fits 6 pages.

## Review notes

What the steps of the night logged as "review" (each with my recommendation); accept the recommendation, or say what.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| R.a | [Level 15's rework](../../campaign/act-3-red-dust/level-15-olympus-descent/README.md#waves) | **finding** (step PE15): waves on the Act 2+ rules: 306 enemies ≈ 99 a minute (above the band), medium 2.68 on the screen and 17.2 shots a minute, one pause (the Burrower window); easy 227; **hard 388, 4.79 on the screen and 43.5 shots a minute (more crowded than Level 18's hard, no hard-only trims)**; `bounty_scale` 0.42 → 0.26 (the haul −0.5 %); `small_act_hp` +15. **Recommendation: trim hard** (fewer on the screen) after your playthrough | [K.d](#captures) | hard | keep / trim hard / change | kept (user, 2026-10-20) |
| R.b | [Level 16's rework](../../campaign/act-3-red-dust/level-16-valles-canyon-run/README.md#waves) | **finding** (step PE16): 367 enemies ≈ 119 a minute (the beam era says "tougher, not more": a Needler line dies before it fires with the plan's fit and Rook), medium 2.63 and 13.9, one pause (gate 1's window); **hard 4.38 and 43.8 (the aim was ≤ 3.5)**; `bounty_scale` 0.54 → 0.27 (−0.3 %); `small_act_hp` +16; extra Burrower fields and Spine ledges. **Recommendation: keep medium, trim hard** | — | 119 a minute is the busiest level so far | keep / trim | kept (user, 2026-10-20) |
| R.c | [Level 17's rework](../../campaign/act-3-red-dust/level-17-dome-siege/README.md#waves) | **finding** (step PE17): 213 enemies ≈ 77 a minute, medium 2.60 and 15.1, no pause, hard 3.38 and 21.9; `bounty_scale` 0.60 → 0.40 (−0.9 %); Burrower fields ×6 on the colony's ground and apron. **Recommendation: accept** (the last of Levels 15-17 out of `PENDING_ACT_TWO_RULE`) | [K.c](#captures) | Burrowers on a colony apron are odd | accept / change | accepted (user, 2026-10-20) |
| R.d | [The walker's cone and pods](../../enemies/bosses/revenant-walker/README.md) | **finding** (step F1/F4): the 180° turn takes 1.5 s; the cone triggers at any angle behind within its length + 40 px; the swivel comes after phase 1; only visible pods fire; homing missiles are shootable but not locked; the walker's phase 1 is not kinder. **Recommendation: keep** (B.b) | [K.a](#captures) | a fight designed on paper and measured only by the autopilot | keep / change | kept (user, 2026-10-20) |
| R.e | [Vent hit box and the belts](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#hazards) | **finding** (step F3): a vent hit 44 × 44; the ripple from the first x; belts carry ground units, not pickups; the turret locked through its burst. **Recommendation: keep** (B.c) | [K.a](#captures) | — | keep / change | kept (user, 2026-10-20) |
| R.f | [Level 19's tank counter and props](../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md#implementation) | **finding** (step G2): the `n/12` counter, the cradles up to 52 px off the truss, the trusses under the tanks, the turret whine at the lock, the prop placement game-side (a prop that can sit off by design). **Recommendation: keep the counter, fix the cradles after your playthrough** | [K.a](#captures) | the walker's hull flash was solid white (G1), fixed in GFX (N.f) | keep / fix now | kept (user, 2026-10-20) |
| R.g | [The radio outro](../../audio/voice/README.md) | **finding** (steps FX-G, RX): a test could not reproduce the cut of Levels 15 and 16 at first; RX found the cause (the outro handed over to the debrief at a fixed 15 s and stopped the voice, while a Rook bark or a secondary judged at the level's end held the closing lines back) and modelled it: the old outro would have cut Level 15 by 4.0 s and Level 18 by 3.1 s. The debrief now waits instead of extending the levels (the 3 s limit). **Recommendation: accept**; extend the levels instead only if you hear a cut again | [K.d](#captures) | you said "extend the level" | accept / extend | accepted (user, 2026-10-20) |
| R.h | [Barks after the win](../../ui/hud/README.md) | **finding** (FX-G): `Barks.levelComplete()` withdraws the wingmen's barks after the win; the shop's hover previews a choice chip. **Recommendation: accept** | — | — | accept / change | accepted (user, 2026-10-20) |
| R.i | [The Level 17 hard dome rate](../../campaign/act-3-red-dust/level-17-dome-siege/README.md) | **finding** (FX-C): on easy a ship that never fires still keeps every dome; on hard all five domes stand in only 1 of 8 autopilot runs (Eden's bombers at t≈129). **Recommendation: keep** (T.4) | [K.c](#captures) | — | keep / ease hard | kept (user, 2026-10-20) |
| R.j | [Level 19's coneTell cue](../../enemies/bosses/revenant-walker/README.md) | **fix** (step F4): the walker's first telegraph sends the cue for Varga's line (`BossPasses.coneTell`), which it did not before. **Recommendation: accept** | — | — | accept / change | accepted (user, 2026-10-20) |
| R.k | [Level 16's radio timing](../../audio/voice/README.md) | **fix** (step F4): Level 16's radio lines 166 → 168, 173 → 175 and 180 → 182 so that "Every barrier down" opens at once. **Recommendation: accept** | — | — | accept / change | accepted (user, 2026-10-20) |

## Refactors

You approved the level-metrics tool, the content refactors and the `LevelScript` plan for the night (the behaviour and
the golden replay hashes unchanged, none re-recorded); the sim split was done in the afternoon. One row, each part
yes/no.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| X.a | [The refactors](../../tech/architecture/README.md) | **yes or no, each** (steps SR2, LS-1 to LS-5, CR1-CR3, MT): (a)-(h) below. Every one is behaviour-neutral (the full `check` green, the goldens and `levelReport` output identical). **Recommendation: all yes**; (d) and (e) are the ones you asked to review | — | the names | all yes / some no | all yes (user, 2026-10-20) |

**Build choices (row X.a)** — the refactors (each yes/no):
- **(a)** Step SR2, the simulation split: `PlayerFire` 1,675 → 881 lines (Beams, Homing, GroundFire) and `EnemyForce` 1,779 → 1,109 (EnemyFire, ObjectiveHook, RootBursts, ChainRules, Burrowers).
- **(b)** Step LS-1: `LevelScript` 4,201 → 4,044 lines with a builder (`builder()`, `toBuilder()`, 38 setters) and one-line withers. API unchanged.
- **(c)** Steps LS-2 to LS-5: the 74 constructor calls migrated to the builder and the 15 delegating constructors deleted (LS-2); the split into a slim `LevelScript` + `ObjectiveSpec`, `GroundSpec`, `HazardSpec`, `DropSpec`, with 13 shared types top-level (LS-3); every caller in sim, content and game moved to the sub-record accessors (LS-4) and the temporary delegates, flat setters and forwarding withers deleted (LS-5). `LevelScript`: 4,201 → **373** lines; each step ended with the full `check` green and the goldens unchanged.
- **(d)** Step CR1, `LevelData` 3,550 → about 830 lines: **12 public sealed `*Block` interfaces** (permitting `LevelData`) hold the records (the names `LevelData.X` stay by inheritance), plus `LevelScroll` and `Requirement`; `Entry`, `Music` and `Visibility` stay. The step's plan called such interfaces "fake supertypes": **say if you want real top-level records instead**; the loader's (Jackson) error messages name the types.
- **(e)** Step CR2, `SimSpecs` 2,620 → 277 lines as a facade over `LoadoutRules`, `WeaponRules`, `RewardRules`, `ScriptRules`, `GroundRules`, `WaveRules`, `BossRules` (618), `UnitRules` (524) and `ChainRules` (content): **the content `ChainRules` has the same simple name as the simulation's package-private `ChainRules`** (a rename to `ChainSpecRules` would remove it: say so).
- **(f)** Step CR3, the autopilot: `Autopilot` 2,340 → 403 lines with `DodgePlay`, `TargetPlay`, `HazardPlay`, `MovePlanner`, `KrakenPlay`, `SpirePlay`, `WalkerPlay`, `SiegePlay`; the shared constants package-private (`levelReport` JSON byte-identical on Levels 10, 14, 17 and 19).
- **(g)** Step MT, the level-metrics tool: `./gradlew :content:levelReport -Plevel=N [-Pdifficulty, -Pseeds, -Pfit=test|plan|user, -Pparts, -Pjson]`, about 3 s a level, `LevelMetrics` shared by the tests, `LevelReportTest`; named in CLAUDE.md's Tools.
- **(h)** The sizes are the rule: a class of thousands of lines is split into its own step (CLAUDE.md), so the next refactors are `GameScreen` and the biggest tests.

## Records

A record, nothing to answer.

| # | Item | Blocked by | Then |
|---|---|---|---|
| N.a | **Level 17's domes: both fixes** (your decision of 2026-10-19): "No dome lost" and the bombers' 2.5 s run-in | — | built (T.4) |
| N.b | **Level 18 meets the Act 2+ floor** and the beam era is "tougher, not more" (your decisions of 2026-10-19, round 41) | — | built; Level 19 follows the same rules (B.a) |
| N.c | **The walker's death and the crawl (sections 6-7) were not seen live before the capture**: the game step's smoke run never killed the walker without input | — | seen in K.a (the autopilot's run); not yet flown by you |
| N.d | **The sweep is not clipped to the play field** (T.2 d) | your answer on T.2 (accepted, d = yes: not clipped) | none |
| N.f | **Readability fixes after the capture** (step GFX, what the capture K.a and K.b showed): the walker's beam-tint flicker, the white hit flash, the sparse flame cone, the tiny missiles, Level 14's crown flash strobing and its full-field death flash, and Level 19's 12/12 counter sitting for about 90 s | — | fixed after the capture; the Level 19 and Level 14 captures (`level-19-capture-r42-a`, `level-14-fixes-r42-a`) are re-recorded under the same names after the fixes (2026-10-20, step F5R): K.a and K.b show the fixed build; what still reads weakly is in K.a; the Spire's hit wash still shows faintly during the sweeps, and its flat lime core disc (older) looks artificial |
| N.e | **The bosses' and Level 19's art stay `chosen`** until you approve F.a-F.f; the roadmap's part F is "built, pending round 42" | your answers (F.a-F.f approved as final, 2026-10-20) | the art is `final`; the part is done except the walker rework (K.a) and the round 43 sounds |

## Decisions

- 2026-10-19: opened round 42 (built while the user was away): M7 part F (Level 19 Foundry of Tharsis), the playtest
  fixes of the evening (`pt3-findings`: the Spire's approach, sweep and death, the radio's last line, Level 17's domes,
  the shop's mouse), Levels 15-17 on the Act 2+ rules, and the refactors the user approved. The user's decision that
  Level 17's domes get *both* fixes is built as given. Every other row is pending the user's review.
- 2026-10-20: closed round 42 (the user's answers from the phone page): everything accepted / yes / kept / noted /
  approved as final except the notes below. **F.a-F.f approved as final**: the Warden Tank, the Level 19 backdrop and
  props, the intel pictures and the briefing images (the walker's art row follows its rework). **K.a change**: "The
  walker looks like a real easy target. It needs to do a lot more shooting and the flame needs to be bigger." (step WR,
  the walker rework). **Sounds redone in round 43** (step SND43), each from the free database: S.b "That's not a hatch,
  that's a pop. Isn't this supposed to be a heavy thing? Search for sounds in the free database."; S.d "Again way too
  small. Search in the database for large venting."; S.e "Also too small. Search for large gas burns or large
  flamethrower"; S.f "Same thing"; S.g "Same thing"; S.h "Search for sounds of a real tank driving"; S.i "You should
  really stop trying to make these sounds yourself. Use the database."; S.j "Not a heavy cannon. Not even a small
  cannon." **S.c**: "use the r08-a" (the walker missile takes the round 08 variant a). **S.n**: "make these three" (the
  walker's vent hit, death and wreck settle, round 43). S.a, S.k, S.m accepted, S.l kept. The voices V.a-V.i, the
  captures K.b-K.d, the playtest fixes T.1-T.6 (all sub-choices yes), the review notes R.a-R.k (R.a, R.b, R.d, R.e, R.f,
  R.i kept), the build choices B.a-B.d and the refactors X.a (all yes) stand as built; the records N.a-N.f are noted.
