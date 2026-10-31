---
title: Concept round 73 — M10 part A "Act 6 systems"
design: review
implementation: n/a
art: proposed
depends-on: [../../enemies, ../../enemies/air/mirror-interceptor, ../../enemies/ground/graft-turret, ../../enemies/ground/shield-pylon, ../../player/weapons/plasma-arc, ../../player/shields, ../../world/jovian, ../../art-direction, ../../audio/sfx, ../../audio/voice, ../../tech/roadmap, ../round-72]
updated: 2026-10-31
---

# Concept round 73 — M10 part A "Act 6 systems"

## Summary

**Closed 2026-10-31**: the answers are in the Answer and Outcome columns and in the *Decisions* entry (everything as recommended except **A.3 b**, the recommendation, and **R.4 b**, not the recommendation). M10 part A was built (branch `m10-act-6`, local commits through `fb579041`; the full `./gradlew check` green on
2026-10-31, Acts 1–5's golden flights and replays unchanged): the mirror's `reflect`, regeneration, the Graft Turret's
regrowth, the death-burst on contact, the Act 6 shields, lightning strikes, dry eruptions, foreground banks, the pylon
dome and gates, the three-way prey link, the IFF spoofer, the Plasma Arc, the Reflex Shield, Composite V and the
Mk V+ "Crucible", flown on fixtures (no Act 6 level exists yet: Level 36 is part B). This round asks, in your order:
the mirror and dome **sounds** (QA-1), the **lightning strike** and the **Reflex bolt** (QA-2, QA-3) and a new question,
**explosions over Level 36's bright bands**; then part A's production **art to confirm** with the first game captures,
the round-72 **follow-ups** (the Ascendant's jagged break, the falling city, Aurelia Traffic's "lift", Vorne's shout) and
last the **important calls** the steps made, each with its recommendation. The recommendation is pre-selected on every
row; leave a row alone to keep it.

The captures come from a scratch capture rig (a stand-in Level 36 over ART-B's real Level 36 tiles and over round 72's
mood grade, stand-in sprites for the new units; [the rig](../../world/jovian/concept/prompts.md#act6-rig-capture-r73-a)).
Found while capturing: the mirror's code-drawn plate is pale on the cream (production art is part C's); the banks
are flat pale veils until part B draws ART-B's bank pieces; the arc's violet-pink fringe and the bounced orbs' magenta
read apart by shape and brightness, though the hues are close. Gaps noted for later parts: homing missiles do not seek
prey (the doc does not say they should; not built); Level 39's vent "ripple" is lost after the first eruption (each vent
draws its own period; part E).

Decided as trivia, not asked (m10a-gaps §4): the mirror's facing is its own state; a bounce mirrors about that facing
at 110 px/s (off the bullet-speed lever) and a homing missile bounces as a bullet; lightning kills pay as a special's;
eruption rocks are their own pool, fanned evenly, never homed on; a dome covers by unit centre each step; the prey link
is a wave's `hunts: <tag>`; the spoofer delays a stored aim; several gates per level; banks are rectangles drifting with
`high-air`; `shields.heavy` from Level 36; the Crucible at index 5; Composite V in the shop from Level 38; the Harrow's
hard ring speed as written.

## Review

The review page is built from this file by `build/round73-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 73`). The record of the decisions is [review.html](review.html) (`python3 tools/concept/review_record.py 73`). Sounds are levelled alike per subject (AUD-A); the
sound rows were measured, not listened to. The game captures are the A4 rig's screenshots, sheets and GIFs.

## Sounds

The mirror set beyond round 72's ping and the dome hit (QA-1), cut from Freesound's public previews (CC0, in
[CREDITS.md](../../../CREDITS.md)) by `tools/concept/audio/sfx_r73.py`. Until your pick the game plays stand-ins (the
heat: the tendril burn pitched 0.85→1.35; the break: the Glassfin shatter; the regrow: the sonde chime low; the dome
hit: the enemy shield hit; the tick: the Deflector's).

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [`mirror-heat`](../../audio/sfx/README.md) | **pick a or b**. **Plays:** while a beam holds on a Mirror Interceptor's face (Levels 38–42, the Ascendant's panels): one loop for the nearest heating mirror, its pitch rising with the heat (0.85 at 0 % to 1.35 at the break, about 1 s of the plan's beam), stopping when the beam leaves or the plate breaks; under the beam's own loop. **Should sound like:** a rising hot-metal hiss, dry: a gas torch on sheet steel, the pitch climbing as the plate goes red to white; no quench, no steam, nothing bubbly. **a:** a gas torch on steel: a hissing roar that climbs. **b:** an arc welder's hum: a buzzier, more electric tone. The demos play each loop as the game glides it over 1 s. | [a](../../audio/sfx/concept/mirror-heat-r73-a.ogg), [b](../../audio/sfx/concept/rejected/mirror-heat-r73-b.ogg), [demo a](../../audio/sfx/concept/mirror-heat-demo-r73-a.ogg), [demo b](../../audio/sfx/concept/rejected/mirror-heat-demo-r73-b.ogg) | — | **a** / b | Gas-torch heat loop chosen; the welder b rejected (with its demo) |
| S.2 | [`mirror-break`](../../audio/sfx/README.md) | **pick a or b**. **Plays:** a mirror breaking by its heat, a `shield-breaker` hit or the EMP, as its shards fly (Levels 38–42; the Ascendant's panels); a reward, a few a fight. **Should sound like:** a real glass pane shattering, heavier and longer than the enemy shield's break and apart from the player shield's falling zap. **a:** a glass pane shattering and its shards falling: long, heavy. **b:** a car's side window (tempered glass) bursting into grains: shorter, grainier. | [a](../../audio/sfx/concept/mirror-break-r73-a.ogg), [b](../../audio/sfx/concept/rejected/mirror-break-r73-b.ogg) | b sits nearer the enemy shield's glass break | **a** / b | Glass pane shattering chosen; the tempered-window b rejected |
| S.3 | [`mirror-regrow`](../../audio/sfx/README.md) | **pick a or b**. **Plays:** a mirror coming back with its 0.4 s silver shimmer, 3 s after a break; now and then, the tell that the plate stands again. **Should sound like:** a low soft chime: a singing bowl struck softly, short; never a ping (the bounce) or an alarm bell. **a:** a deep singing bowl, soft stick, sped to 1.5: a warm low hum-tone that blooms and fades, 1.0 s. **b:** a smaller bowl: brighter and clearer, ringing higher, 1.0 s. | [a](../../audio/sfx/concept/mirror-regrow-r73-a.ogg), [b](../../audio/sfx/concept/rejected/mirror-regrow-r73-b.ogg) | b's higher ring comes nearer the mirror's ping | **a** / b | Deep singing bowl chosen; b rejected |
| S.4 | [`pylon-dome-hit`](../../audio/sfx/README.md) | **pick a or b**. **Plays:** a shot spent on a Shield Pylon's dome (Level 41; at most once per 0.15 s); often in a pylon gate. **Should sound like:** a short bright electric zap with a glassy ring; never Level 17's big dome boom or the mirror's ping. **a:** a stun gun's tenth-of-a-second zap over a struck glass bowl: a crackling snap and a short clear ring, 0.47 s. **b:** a buzzy electric snap over a wine glass struck hard: a thinner, higher ring. | [a](../../audio/sfx/concept/pylon-dome-hit-r73-a.ogg), [b](../../audio/sfx/concept/rejected/pylon-dome-hit-r73-b.ogg) | — | **a** / b | Stun-gun zap over a struck bowl chosen; b rejected |
| S.5 | [`bolt-absorbed`](../../audio/sfx/README.md) | **accept or redo**. **Plays:** a Reflex bolt (or a Plasma Arc jump) earthed on a standing mirror's face: a spark, no bounce; now and then, quiet, under the fire. **Should sound like:** a short dry electric tick. **a:** a quick burst of static cut to a tick: a dry electric crackle, 0.1 s. | [a](../../audio/sfx/concept/bolt-absorbed-r73-a.ogg) | one variant only | **accept** / redo | The static tick accepted |

## Art questions

The two open art questions of part A's gap check (QA-2, QA-3) and a new one from the captures (A.3). Variant a of
QA-2 and QA-3 is in the game until your pick.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| A.1 | [The lightning strike's look (QA-2)](../../world/jovian/README.md) | **pick a or b**. The doc fixes the 1.0 s tell (the 40 px column's faint edges and a thin blue-white line), not the 0.25 s strike. **a "jagged bolt"** (recommended): a jagged blue-white bolt filling the column top to bottom, a white core, two short side forks and Level 12's sky flash; it reads as lightning, and the column's straight tell already marks the lane. **b "light column"**: a straight full-height column of white light with a jagged core line, its edges fading to the core over the 0.25 s. The final frames and the game capture are a. | [a sheet](../../world/jovian/concept/lightning-strike-r73-a.png), [a motion](../../world/jovian/concept/lightning-strike-r73-a.gif), [b sheet](../../world/jovian/concept/rejected/lightning-strike-r73-b.png), [b motion](../../world/jovian/concept/rejected/lightning-strike-r73-b.gif), [a final](../../world/jovian/concept/lightning-final-r73-a.png), [a final motion](../../world/jovian/concept/lightning-final-r73-a.gif), [in the game](../../world/jovian/concept/hazards-capture-r73-a.png), [in the game, motion](../../world/jovian/concept/hazards-capture-r73-a.gif) | in the game the tell's thin line is faint on the brightest cream (Level 38's storm, where the strikes live, is dark) | **a** / b | a "jagged bolt" chosen; b rejected |
| A.2 | [The Reflex Shield's bolt (QA-3)](../../player/shields/README.md) | **pick a or b**. **a "white-gold bullet"** (recommended): the enemy bullet's round shape in white-gold, a dark rim, a short gold trail. **b "gold needle"**: a thin gold needle with a white core, a dark rim and a faint trail. The gap check said: b if the game shows the bolts reading as enemy fire. In the game (Level 33) the gold bolts read apart from the magenta orbs at a glance, and b's needle is the shape of the Ascendancy's own gold needles, so a. | [a sheet](../../player/shields/concept/reflex-bolt-r73-a.png), [a motion](../../player/shields/concept/reflex-bolt-r73-a.gif), [b sheet](../../player/shields/concept/rejected/reflex-bolt-r73-b.png), [b motion](../../player/shields/concept/rejected/reflex-bolt-r73-b.gif), [a in the game](../../player/shields/concept/reflex-capture-r73-a.png), [a in the game, motion](../../player/shields/concept/reflex-capture-r73-a.gif) | captured over Level 33's dark field; over the cream only ART-A's sheets show it | **a** / b | a "white-gold bullet" chosen; b rejected |
| A.3 | [Explosions over Level 36's bright bands](../../art-direction/README.md) | **pick a, b or c** (new; round 72's A.7 note, ART-B's measure). The game draws kill bursts additively: over the brightest cream a `small` burst lifts its core only about +30 (over 90 % clipped) and changes its 24 px disc by 8 on average over its life: a pale smudge; over rust it reads (22). **a "as built"**: no change. **b "soot under the flash"** (recommended): a soft soot puff drawn with normal blending under the same flash: warm dark grey, at most 0.42 opaque, feathered to nothing (no edge, no ring), rising a little as it fades; the burst changes its disc by 19 on the cream and the bank, 21 on rust, and Jupiter's air "rises as smoke and fire". **c "deeper core"**: the frames recoloured deeper orange for Act 6, still additive: warmer, but it adds less light, so it reads weaker on the cream (5). Part B's B5 checks the pick in Level 36. | [a sheet](../../art-direction/concept/rejected/explosion-bright-r73-a.png), [a motion](../../art-direction/concept/rejected/explosion-bright-r73-a.gif), [b sheet](../../art-direction/concept/explosion-bright-r73-b.png), [b motion](../../art-direction/concept/explosion-bright-r73-b.gif), [c sheet](../../art-direction/concept/rejected/explosion-bright-r73-c.png), [c motion](../../art-direction/concept/rejected/explosion-bright-r73-c.gif) | b's puff is a dark halo for a moment at 2× (soft, no edge); it costs one normal-blended layer per burst, Act 6 only or everywhere is part B's call | a / **b** / c | **b** "soot under the flash" chosen (the recommendation); a and c rejected. Whether the soot puff is Act 6 only or everywhere is part B's call (B4), an open build item in the act README's *Implementation* |

## Art to confirm

Part A's production art (ART-A, `tools/art/act6_gear.py`, `tools/art/icons.py --act6`), already in the game, and the
first game captures of A3a/A3b's looks on the stand-in units: accept, or redo with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.1 | [The Plasma Arc's frames (round 72 A.7 a)](../../player/weapons/plasma-arc/README.md) | **accept or redo**. The `arc` family drawn wider: per place in the chain a 160×48 strip (the first strike 1.7× the Tesla pod's width, each jump 0.72× thinner), three shapes each, the 64 px spark burst, the nose glow; in the game at L5 over round 72's grade, ART-B's cream and rust bands, beside mirrors bouncing the pods' missiles back as magenta orbs, and in Level 35. | [frames](../../player/weapons/plasma-arc/concept/plasma-arc-final-r73-a.png), [motion](../../player/weapons/plasma-arc/concept/plasma-arc-final-r73-a.gif), [in the game](../../player/weapons/plasma-arc/concept/plasma-arc-capture-r73-a.png), [in the game, motion](../../player/weapons/plasma-arc/concept/plasma-arc-capture-r73-a.gif), [Level 35](../../player/weapons/plasma-arc/concept/plasma-arc-l35-capture-r73-a.png) | the arc's fringe is violet-pink, the orbs magenta: apart by shape and brightness, close in hue; on the cream the fringe pales | **accept** / redo | Accepted |
| F.2 | [The bounced bullet](../../enemies/README.md) | **accept or redo**. `orb-bounced_0..15`: the standard magenta orb with a short soft magenta wake (16 headings), slower (110 px/s) than a fresh orb, so a bounced shot reads as yours coming back. | [frames](../../enemies/concept/bounced-bullet-final-r73-a.png), [motion](../../enemies/concept/bounced-bullet-final-r73-a.gif), [in the game](../../player/weapons/plasma-arc/concept/plasma-arc-capture-r73-a.png) | in a dense bounce stream the wake is the only difference from a fresh orb | **accept** / redo | Accepted |
| F.3 | [The pylon dome's effects](../../enemies/ground/shield-pylon/README.md) | **accept or redo**. The 232 px dome (a ≈20 % hex lattice, the rim, a 12-frame shimmer), the spent shot's hit, the 0.4 s collapse into the crown, the feed strip, the covered unit's sheen; held until part G's pylon data, shown in the game on a stand-in mast. | [frames](../../enemies/ground/shield-pylon/concept/pylon-dome-final-r73-a.png), [motion](../../enemies/ground/shield-pylon/concept/pylon-dome-final-r73-a.gif), [in the game](../../enemies/ground/shield-pylon/concept/dome-capture-r73-a.png), [in the game, motion](../../enemies/ground/shield-pylon/concept/dome-capture-r73-a.gif) | the rig's covered nest missed the dome, so the hits and the collapse are only on ART-A's sheet | **accept** / redo | Accepted |
| F.4 | [The Act 6 shop icons](../../ui/hangar/README.md) | **accept or redo**. The hangar badges of the Plasma Arc, the Reflex Shield, the Mk V+ "Crucible" and the IFF spoofer in the icon set's style. | [icons](../../ui/hangar/concept/act6-icons-final-r73-a.png) | — | **accept** / redo | Accepted |
| F.5 | [The Reflex Shield's flash](../../player/shields/README.md) | **accept or redo**. The gold hex flash `ship-reflex_0..3` (the blue ring's shapes in gold) while the shield is at 75 % or more, a bolt up per hit; the blue ring below 75 %. | [frames](../../player/shields/concept/reflex-shield-final-r73-a.png), [motion](../../player/shields/concept/reflex-shield-final-r73-a.gif), [in the game](../../player/shields/concept/reflex-capture-r73-a.png) | — | **accept** / redo | Accepted |
| F.6 | [The mirror, regrowth and dome looks in the game](../../enemies/README.md) | **accept or change**. A3a's code-drawn looks on stand-in sprites: the mirror's plate heating dull red → orange → white, its cracks from 70 %, the break's shards, the 3 s down time and the 0.4 s silver regrowth; the Graft root's pulse (1 → 4 Hz) and shrinking ring and the 0.8 s knit; the units' production sprites are parts B–G's. | [mirror](../../enemies/air/mirror-interceptor/concept/mirror-capture-r73-a.png), [mirror, motion](../../enemies/air/mirror-interceptor/concept/mirror-capture-r73-a.gif), [regrowth](../../enemies/ground/graft-turret/concept/regrowth-capture-r73-a.png), [regrowth, motion](../../enemies/ground/graft-turret/concept/regrowth-capture-r73-a.gif) | the plate is pale on the cream until white-hot: its cool state reads as a faint grey crescent | **accept** / change | Accepted |
| F.7 | [The hazards in the game](../../world/jovian/README.md) | **accept or change**. The told strike (tell, strike, flash), a dry eruption (the vent's tell, a rock; Io's rock art is part E's) and the foreground banks over `air` bodies (A3b's 65 % code veils until part B draws ART-B's bank pieces). | [sheet](../../world/jovian/concept/hazards-capture-r73-a.png), [motion](../../world/jovian/concept/hazards-capture-r73-a.gif) | the code veils read as flat pale rectangles on the cream and rust (stand-ins) | **accept** / change | Accepted |

## Round 72 follow-ups

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| R.1 | [The Ascendant's break (A.11 change)](../../enemies/bosses/ascendant/README.md) | **accept or redo**. Storyboard frame 8 redrawn as you asked: the hull torn in two along a jagged fracture down the keel, torn plate flaps bent outward, a charred, hot-glowing rim, shards in the gap, orange and teal gas venting from both edges. | [storyboard](../../enemies/bosses/ascendant/concept/ascendant-phases-r73-a.png) | round 72's unit sheet still shows clean wreck halves (sheet only; the storyboard is the rule) | **accept** / redo | The jagged break accepted; round 72's storyboard rows superseded |
| R.2 | [The falling city, b (A.13 b)](../../world/jovian/README.md) | **accept or redo**. Look b redrawn by A.11's rule: whole until the deck drops, then breaking along a jagged fault (ragged strut and deck ends, torn flaps, a charred glowing rim, shards in the widening gap, smoke and fire from both edges); round 72's b was a clean cut. | [storyboard](../../world/jovian/concept/falling-city-r73-b.png) | — | **accept** / redo | The falling city's jagged break accepted; round 72's b superseded |
| R.3 | [Aurelia Traffic's "lift" (V.3 b)](../../audio/voice/README.md) | **accept or redo**. Lizzie Driver's sign-off re-rendered so it says "lift", not "liff" (seed 117; Whisper reads "lift" in ten of sixteen seeds). | [take](../../audio/voice/concept/voice-aurelia-traffic-r73-b.ogg) | — | **accept** / redo | "Lift" accepted; round 72's Aurelia Traffic take superseded |
| R.4 | [Vorne's shout (V.5 recast)](../../audio/voice/README.md) | **pick a or b, or recast again**. "No! We had an agreement!" in his low register, lowered with WORLD resynthesis. Does it still sound like a person shouting? **a:** round 72's shout take lowered (×0.41, to about 113 Hz), the break after "No!" kept. **b:** a new shouted render from his clip lowered 4 semitones, then lowered to about 104 Hz. | [a](../../audio/voice/concept/rejected/vorne-shout-r73-a.ogg), [b](../../audio/voice/concept/vorne-shout-r73-b.ogg) | the lowering may sound vocoded; only your ear can judge | a / **b** / recast | **b** (not the recommendation) chosen: the new shouted render lowered to about 104 Hz; a and round 72's two takes rejected |

## Important calls

Calls the part A steps made where the documents left room (the step logs in the working note), each built as
recommended; the alternative is in the cell. Accept, or change with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [The dome's bearing (A1a)](../../enemies/ground/shield-pylon/README.md) | **accept or change**. Each pylon's dome bearing comes from its own placement (its nest's `crown`), not one bearing for the unit type. Alternative: one bearing per pylon type. | — | — | **accept** / change | Accepted |
| C.2 | [The mirror turns only at its station (A1a)](../../enemies/air/mirror-interceptor/README.md) | **accept or change**. The plate turns toward the ship (60 °/s) only while the unit holds its station; on its way in and out it faces along its flight, so a mirror arriving from a side or the rear is open. Alternative: it turns from its entry on. | — | — | **accept** / change | Accepted |
| C.3 | [`fire_within: 30` (A1a)](../../enemies/air/mirror-interceptor/README.md) | **accept or change**. A new stat-block key: the mirror's pods fire only while the ship is within 30° of its facing (the doc's "the burst only while facing the ship"). | — | — | **accept** / change | Accepted |
| C.4 | [Blasts pass a mirror (A1a)](../../enemies/README.md) | **accept or change**. Bombs, shells and splash damage reach a mirrored hull past its plate (no bounce, no heat); only shots that hit the face bounce. Alternative: the plate also stops blasts on its side. | — | — | **accept** / change | Accepted |
| C.5 | [The root on its first step (A1a)](../../enemies/ground/graft-turret/README.md) | **accept or change**. The Graft root cannot be hit on the step it is exposed, so the shot or blast that kills the turret never also hits the root (the doc: "a blast that kills the turret misses the root"). | — | — | **accept** / change | Accepted |
| C.6 | [Prey lost to the war (A1a)](../../enemies/README.md) | **accept or change**. A prey unit the Vrell kill counts as escaped for a secondary (it pays nothing and is no player kill). | — | — | **accept** / change | Accepted |
| C.7 | [A strike hits enemies once (A1b)](../../world/jovian/README.md) | **accept or change**. The 0.25 s strike hurts the `air` enemies in its column on its first step only (60 each); the ship and Rook once per strike. | — | — | **accept** / change | Accepted |
| C.8 | [A vent's first tell (A1b)](../../world/jovian/README.md) | **accept or change**. A vent's first eruption is told one period (5–7 s) after it enters the screen, not at once. | — | the rig needed a 2 s period to erupt on the screen at 100 px/s; Level 39's scroll must leave room (part E) | **accept** / change | Accepted |
| C.9 | [The ship's hazard hits (A1b)](../../world/jovian/README.md) | **accept or change**. A strike's `heavy` 10 and a rock's `small` 10 go to the shield first, then the armour, like a bullet (never reflected). | — | — | **accept** / change | Accepted |
| C.10 | [The Reflex bolt's damage (A1b)](../../player/shields/README.md) | **accept or change**. A bolt deals 2× the reflected bullet's damage as it arrives (after the act and difficulty factors; the shield still takes half of it), not 2× its class's base. | — | — | **accept** / change | Accepted |
| C.11 | [A hardened arc target (A1b)](../../player/weapons/plasma-arc/README.md) | **accept or change**. When the arc's first strike lands on a hardened unit it glances and the chain ends there. | — | — | **accept** / change | Accepted |
| C.12 | [The balance plan at Levels 36–37 (A2a)](../../player/generator/README.md) | **accept or change**. `BalanceTest` reaches the Level 36 visit 208 cr short of the Ion Beam L5, so the plan takes the hand plan's alternative: the Tesla Coil Pod and its L2 at the L36 visit (0.99 of the reference), the beam L5 at L37 (1.14); from L38 the table stands. | — | Level 36's opener sits at 0.99, not 1.19, until part B's hauls are measured | **accept** / change | Accepted |
| C.13 | [`crown` (A2a)](../../tech/architecture/schemas-levels/README.md) | **accept or change**. A pylon nest's `crown` is in degrees from straight up the screen, positive to the right. | — | — | **accept** / change | Accepted |
| C.14 | [Where the hazard checks live (A2b)](../../tech/architecture/schemas-levels/README.md) | **accept or change**. The strike and eruption checks are in content's `JovianHazardChecks`, not `LevelChecks`. | — | — | **accept** / change | Accepted |
| C.15 | [The entry rule by time (A2b)](../../world/jovian/README.md) | **accept or change**. "No tell on the ship's x within 1 s of a wave's entry" is checked on time only (no strike within 1 s of any entry) in the data, with the runtime shift on every difficulty. | — | stricter than the rule: it may cost a strike placement | **accept** / change | Accepted |
| C.16 | [The rock's hit box (A2b)](../../world/jovian/README.md) | **accept or change**. An eruption rock's hit box is 24×24. | — | — | **accept** / change | Accepted |
| C.17 | [Stand-ins until the picks (A3a, A3b)](../../audio/sfx/README.md) | **accept or change**. Until S.1–S.5 and the units' art: the heat plays the tendril burn pitched 0.85→1.35, the break the Glassfin shatter, the regrow the sonde chime low, the dome hit the enemy shield hit, the tick the Deflector's, the root's death the tendril snap; the plate, cracks and shards are code-drawn; the banks are 65 % veils; the rocks' sounds are stand-ins. | — | — | **accept** / change | Accepted |
| C.18 | [`withX` keeps newer components (RF9b)](../../tech/architecture/README.md) | **accept or change**. The spec builders' `withX` copies now keep every newer component (a refactor rule: an Act 6 component is never dropped by an older `withX`). | — | code structure; no game change | **accept** / change | Accepted |
| C.19 | [`WaterEffects` holds the arena's parts (RF9e)](../../tech/architecture/README.md) | **accept or change**. `arenaPartDown` and `armPop` moved into `WaterEffects` with the other water deaths. | — | code structure; no game change | **accept** / change | Accepted |
| C.20 | [The `EffectStarts` helper (RF9e)](../../tech/architecture/README.md) | **accept or change**. A new small helper class, `EffectStarts`, for the shared effect starts split out of `EventEffects`. | — | code structure; no game change | **accept** / change | Accepted |
| C.21 | [`LevelScreen.react` stays (RF9e)](../../tech/architecture/README.md) | **accept or change**. The screen's event dispatch stays in `LevelScreen.react` (not moved to a new class). | — | code structure; no game change | **accept** / change | Accepted |

## Decisions

- 2026-10-31: round opened (step A4) after part A's full check; the captures from a scratch rig (stand-in Level 36
  over ART-B's tiles and round 72's mood grade); the explosion question added from round 72's A.7 note and ART-B's
  measure.
- 2026-10-31 (user, closed; page answers, no notes): **every row as recommended except R.4**. Sounds S.1-S.4 a, S.5
  accepted (the five QA-1 picks wait in the sfx README's *Chosen sounds the game does not play* for the game: B4 / the
  next game step imports and plays them, the stand-ins go); A.1 a (jagged bolt), A.2 a (white-gold bullet); **A.3 b**
  (explosions: the soft soot puff under the additive flash; whether it is Act 6 only or everywhere is part B's call, an
  open build item in the act README's *Implementation* for B4); F.1-F.7 accepted; R.1-R.3 accepted; **R.4 b** (not the
  recommendation): Vorne's shout is the new shouted render lowered to about 104 Hz; C.1-C.21 accepted (every "pending the
  user's review in round 73" note is now "decided 2026-10-31 (round 73 <id>)"). Rejected files moved to the parts'
  `concept/rejected/`: the b variants of S.1-S.4, A.1, A.2, a and c of A.3, vorne-shout-r73-a; round 72's rows superseded
  by the follow-ups: the Ascendant storyboard (a, b), falling-city-r72-b, voice-aurelia-traffic-r72-b and both
  vorne-shout-r72 rows.
