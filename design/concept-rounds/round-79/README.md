---
title: Concept round 79 — M10 part G "Level 41 Callisto Fortress"
design: review
implementation: n/a
art: proposed
depends-on: [../../campaign/act-6-jovian-storm/level-41-callisto-fortress, ../../enemies/ground/shield-pylon, ../../enemies/ground/halo-platform, ../../audio/sfx, ../../enemies/bosses/honour-guard, ../../campaign/act-6-jovian-storm/level-37-fall-of-aurelia, ../../campaign/act-6-jovian-storm/level-39-io-flyby, ../../ui/databank, ../round-78]
updated: 2026-11-02
---

# Concept round 79 — M10 part G "Level 41 Callisto Fortress"

## Summary

**Open.** Level 41 "Callisto Fortress" is built (M10 part G, branch `m10-act-6`): the run over Callisto's ice to Helix
HQ, three pylon gates that halt the scroll until their pylons fall, Varga's files uploaded from the archive cores, the
Halo Platform held at the centre, the Silence line and the empty berth. This round asks what can be seen or heard: the
whole level as a real-time recording with sound, part G's art as it looks in the game, the level's sounds, and four
items held over from earlier rounds. The recommendation is pre-selected on every row; leave a row alone to keep it.

## Review

The review page is built from this file by `build/round79-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 79`). The captures are the game's own, flown by the content
autopilot at medium with the balance plan's fit (recipe: [Level 41's prompts](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/concept/prompts.md#level-fight-r79-a)).
The sounds are levelled alike per subject.

## Level 41

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| L.1 | [Level 41](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/README.md) | **confirm**. Does Level 41 play and look right? | [the whole level with sound](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/concept/level-fight-r79-a.mp4) | Rook is shot down at the first gate, so he is missing after it. | **ok** / change | open |

## Art

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| A.1 | [Halo Platform](../../enemies/ground/halo-platform/README.md) | **confirm**. Does the Halo Platform look right, with its ring turning smoothly? | [its halt, the barrier up and dropped](../../enemies/ground/halo-platform/concept/halo-capture-r79-a.png), [the ring turning, 1:1](../../enemies/ground/halo-platform/concept/halo-rotation-final-r79-a.png) | Its guns are small at play size. | **ok** / change | open |
| A.2 | [Shield Pylon and its dome](../../enemies/ground/shield-pylon/README.md) | **confirm**. Do the pylons and their domes look right? | [a gate's halt](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/concept/gate-capture-r79-a.png), [a pylon beside Warden Tanks, 1:1](../../enemies/ground/shield-pylon/concept/pylon-capture-r79-a.png), [a dome's rim, 1:1](../../enemies/ground/shield-pylon/concept/dome-rim-capture-r79-a.png) | | **ok** / change | open |
| A.3 | [Archive core](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/README.md#concept-art) | **confirm**. Do the archive core and its upload ring look right? | [open, uploading, done](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/concept/core-capture-r79-a.png) | | **ok** / change | open |
| A.4 | [Lab vault](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/README.md#concept-art) | **confirm**. Does Varga's lab vault look right, sealed and blown open? | [sealed and open](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/concept/vault-capture-r79-a.png) | | **ok** / change | open |
| A.5 | [CDF beacon](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/README.md#concept-art) | **confirm**. Does the CDF beacon's drop look right? | [falling, then the armour patch](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/concept/beacon-capture-r79-a.png) | It is small and passes quickly. | **ok** / change | open |
| A.6 | [Fortress backdrop](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/README.md#concept-art) | **confirm**. Does the fortress backdrop with its blast doors look right? | [ice, causeways, citadel, HQ](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/concept/backdrop-capture-r79-a.png), [the blast doors opening](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/concept/doors-capture-r79-a.png), [the empty berth](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/concept/berth-capture-r79-a.png) | The doors open in under a second. | **ok** / change | open |

## Sounds

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [Level 41: the fortress's machinery (`fortress-machinery`)](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/README.md#music--ambience) | **pick a or b**. **Plays:** a loop under the Callisto bed from the outpost to Helix HQ, and should sound like the fortress's machinery heard through the hull: deep hums, a door servo now and then, a distant klaxon. **a:** a deep, steady factory hum with a far klaxon blast and a big door's motor whining once a loop. **b:** a rougher, throatier machine hum with a far pulsing alarm and short servo bursts. | [a](../../audio/sfx/concept/fortress-machinery-r79-a.ogg), [b](../../audio/sfx/concept/fortress-machinery-r79-b.ogg) | both are mostly low hum, faint on small speakers; a's klaxon may read as a warning | **a** / b | open |
| S.2 | [Shield Pylon: the dome's hum (`pylon-dome-hum`)](../../enemies/ground/shield-pylon/README.md#sounds) | **pick a or b**. **Plays:** a loop on the nearest pylon dome while the gate holds, and should sound like a low steady electrical hum. **a:** a transformer humming in a large room: deep and even. **b:** a neon sign buzzing close: brighter and fizzier. | [a](../../audio/sfx/concept/pylon-dome-hum-r79-a.ogg), [b](../../audio/sfx/concept/pylon-dome-hum-r79-b.ogg) | both are very steady and may grate through a long halt; b sounds a little quieter | **a** / b | open |
| S.3 | [Shield Pylon: its death (`pylon-death`)](../../enemies/ground/shield-pylon/README.md#sounds) | **pick a or b**. **Plays:** a pylon destroyed, eleven times a level, over its explosion and the dome's collapse, and should sound like a metal mast blowing up and crashing down. **a:** a metallic bang, then scaffolding collapsing with poles clattering down. **b:** a deep metal boom, then a big crash of sheet metal and scrap. | [a](../../audio/sfx/concept/pylon-death-r79-a.ogg), [b](../../audio/sfx/concept/pylon-death-r79-b.ogg) | a is heavier and sounds louder than b; heard over the dome's own collapse, either may be busy | **a** / b | open |
| S.4 | [Halo Platform: the barrier's collapse (`halo-barrier-collapse`)](../../enemies/ground/halo-platform/README.md#sounds) | **pick a or b**. **Plays:** the red barrier on the Halo's core failing as its third turret falls, once a level, and should sound like a falling electrical hum cut off by a heavy relay clunk. **a:** a transformer's hum sliding down, cut by a big breaker switch thrown. **b:** a neon buzz sliding down, cut by a circuit breaker echoing in a hallway. | [a](../../audio/sfx/concept/halo-barrier-collapse-r79-a.ogg), [b](../../audio/sfx/concept/halo-barrier-collapse-r79-b.ogg) | a has the same shape as a pylon dome going down, so it may sound like one | **a** / b | open |
| S.5 | [Halo Platform: the open core's whine (`halo-core-whine`)](../../enemies/ground/halo-platform/README.md#sounds) | **pick a or b**. **Plays:** the open core's eye flaring before each ring of bullets, every few seconds after the barrier falls, and should sound like a short rising whine. **a:** a gas turbine's start-up whine sped up: a clean, rising whine. **b:** a jet engine's start-up whine: airier and rougher. | [a](../../audio/sfx/concept/halo-core-whine-r79-a.ogg), [b](../../audio/sfx/concept/halo-core-whine-r79-b.ogg) | both are engines spinning up, so they may read as a motor rather than an energy core | **a** / b | open |
| S.6 | [Level 41: the blast doors (`gate-doors`)](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/README.md#music--ambience) | **pick a or b**. **Plays:** a gate's blast doors parting as its last pylon falls, four times a level, and should sound like Level 32's blast door, deeper and longer: a huge steel door rumbling on rails, hydraulic clunks, a final heavy thud. **a:** a hydraulic clunk, then Level 32's rolling door and thud slowed down: a lower, longer grind. **b:** a hydraulic clunk, then a hydraulic door's smooth run and slam. | [a](../../audio/sfx/concept/gate-doors-r79-a.ogg), [b](../../audio/sfx/concept/gate-doors-r79-b.ogg) | a reuses Level 32's door, so it sounds familiar; b's slam comes a little after the doors stop moving | **a** / b | open |
| S.7 | [Level 41: an archive core's hatch (`hatch-break`)](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/README.md#music--ambience) | **pick a or b**. **Plays:** a core's armoured hatch shot open, three times a level, and should sound like a heavy armour plate cracking off. **a:** a hard blow on heavy steel and a manhole cover dropped clanging. **b:** a manhole cover blown off with a deep boom and sheet metal clanging. | [a](../../audio/sfx/concept/hatch-break-r79-a.ogg), [b](../../audio/sfx/concept/hatch-break-r79-b.ogg) | b's boom may sound like an explosion more than a plate breaking | **a** / b | open |
| S.8 | [Level 41: the lab vault (`vault-crack`)](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/README.md#music--ambience) | **pick a or b**. **Plays:** Varga's lab vault broken open (the secret), once a level at most, over the crate break, and should sound like a steel door's bolts shearing. **a:** three bolt-cutter snaps, each lower, and a vault door's heavy clank. **b:** two crunchier bolt-cutter snaps and a vault lock clanking open. | [a](../../audio/sfx/concept/vault-crack-r79-a.ogg), [b](../../audio/sfx/concept/vault-crack-r79-b.ogg) | a repeats one snap three times and may sound mechanical; b's lock is light | **a** / b | open |
| S.9 | [Level 41: the uplink seating (`uplink-seat`)](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/README.md#music--ambience) | **pick a or b**. **Plays:** Varga's uplink spike seating as the ship hovers over an open core, and should sound like a short rising electronic tone (synthesized). **a:** a click and a clean tone gliding up an octave. **b:** a click and three quick steps up. | [a](../../audio/sfx/concept/uplink-seat-r79-a.ogg), [b](../../audio/sfx/concept/uplink-seat-r79-b.ogg) | b sounds like a menu cue | **a** / b | open |
| S.10 | [Level 41: the upload (`uplink-chatter`)](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/README.md#music--ambience) | **pick a or b**. **Plays:** a loop while the file uploads (about two seconds of hovering), and should sound like a steady electronic data chatter (synthesized). **a:** a modem's warbling two-tone chatter. **b:** a brighter, raspier chirr of short data packets. | [a](../../audio/sfx/concept/uplink-chatter-r79-a.ogg), [b](../../audio/sfx/concept/uplink-chatter-r79-b.ogg) | a sounds like an old dial-up modem (retro); b is rhythmic and may buzz | **a** / b | open |
| S.11 | [Level 41: the file complete (`uplink-chirp`)](../../campaign/act-6-jovian-storm/level-41-callisto-fortress/README.md#music--ambience) | **pick a or b**. **Plays:** a file complete, three times a level, over the objective tone, and should sound like a clean confirmation chirp (synthesized). **a:** two clean notes stepping up, "bi-ding". **b:** one quick upward sweep, a single "tweet". | [a](../../audio/sfx/concept/uplink-chirp-r79-a.ogg), [b](../../audio/sfx/concept/uplink-chirp-r79-b.ogg) | both are short and high and may clash with the objective tone they play over | **a** / b | open |

## Held over

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| H.1 | [Honour Guard](../../enemies/bosses/honour-guard/README.md) | **confirm**. Do the aces fly naturally now? | [the whole duel with sound](../../enemies/bosses/honour-guard/concept/duel-fight-r78-b.mp4) | Rook is shot down early, so he is not in the duel. | **ok** / change | open |
| H.2 | [Level 37's fall](../../campaign/act-6-jovian-storm/level-37-fall-of-aurelia/README.md#concept-art) | **confirm**. Does the city's fall look right now? | [the redone fall](../../campaign/act-6-jovian-storm/level-37-fall-of-aurelia/concept/fall-final-r75-b.gif) | | **ok** / change | open |
| H.3 | [Level 39's opening](../../campaign/act-6-jovian-storm/level-39-io-flyby/README.md#concept-art) | **confirm**. Does Level 39's opening over Io's plains look right? | [the first 10 s in the game](../../campaign/act-6-jovian-storm/level-39-io-flyby/concept/opening-capture-r79-a.gif), [two frames](../../campaign/act-6-jovian-storm/level-39-io-flyby/concept/opening-capture-r79-a.png) | | **ok** / change | open |
| H.4 | [Databank](../../ui/databank/README.md) | **confirm**. Do the databank's screens look right in the game? | [the main menu](../../ui/databank/concept/databank-capture-r82-menu.png), [the list and an enemy](../../ui/databank/concept/databank-capture-r82-l3.png), [a weapon](../../ui/databank/concept/databank-capture-r82-weapon.png) | | **ok** / change | open |

## Decisions

- 2026-11-02: Round 79 opened (step `m10-g5`): L.1 the whole level, A.1–A.6 part G's art in the game, S.1–S.11 the
  level's sounds, H.1–H.4 held over (the Honour Guard duel of round 78 B.1, Level 37's fall redo of round 75 F.6, Level
  39's opening redo of round 77 F.1, the databank's in-game screens of round 82). The steps' trivia were decided in
  their parts' Decisions logs, not asked; QG-1 (the Halo's ring by the segment rule) is asked as A.1.
