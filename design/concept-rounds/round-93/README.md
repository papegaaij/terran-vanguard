---
title: Concept round 93 — M11 part F "Level 48 Vorne's End"
design: review
implementation: n/a
art: proposed
depends-on: [../../campaign/act-7-beyond-the-gate/level-48-vornes-end, ../../enemies/bosses/vornes-chimera, ../../enemies/ground/reactor-core, ../../story/characters, ../../audio/sfx, ../../audio/music, ../../audio/voice, ../../ui/briefing, ../../tech/roadmap, ../round-92]
updated: 2026-11-07
---

# Concept round 93 — M11 part F "Level 48 Vorne's End"

## Summary

M11 part F was built (branch `m11-act-7`, uncommitted; the full `./gradlew check` green on 2026-11-07, Acts 1–6's
golden flights and replays unchanged): Level 48 "Vorne's End" with the citadel, its gold decks and reactor cores, the
rear waves, and the mid-boss **Vorne's Chimera** rising out of the spire well: three phases, the rear sweep, the
capsule opening, and Vorne dying mid-sentence, in his fused voice, over track 34. This round asks, in this order: the
fight's **sounds** in place, track 34 **in place**, Vorne's **voice**, part F's **art to confirm**, the level and **the
whole fight in play** (one real-time recording with sound), and the **important calls**, each with its
recommendation. The recommendation is pre-selected on every row; leave a row alone to keep it.

Decided as trivia, not asked: the Chimera sways on its station (a smooth side-to-side), its playbook keeps it
unpredictable; the rear sweep comes at least every second attack in phase 2 (the document said every third) so it shows
two to four times; the drop and each tell fire a covering spore pair, so no stretch of the fight is empty; the Dart
stream stops when its bay dies; the fight's measure counts a sac "healed back" only after it lost more than a fifth of
its health; the Chimera pays 885 credits (recomputed by its formula at the level's money scale, 0.17); the reactor
cores' bonus is paid unscaled, as in Levels 43–47; the scroll brakes from 100 to 20 px/s over the last 8 s into the
arena instead of stopping dead; the rear wings come up in the half of the field the ship is not in, curving toward the
middle; Vorne's archive is a breakable box with the secret's crate; the enemy shots in flight fade out at his death.

## Review

The review page is built from this file by `build/round93-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 93`). The game captures are step EF5's
([how](../../campaign/act-7-beyond-the-gate/level-48-vornes-end/concept/prompts.md#game-captures-r93-a)).

## Sound

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [The Chimera's sounds in the fight](../../audio/sfx/README.md) | **ok or redo**. Round 84's picks, now in place: hear them in the fight's recording (L.3). **The rise**, as it climbs out of the well: a deep stretching groan that tears at the end over a thin strained whine. **The pain**, at the fusion (phase 2's start): one long heavy buckling metal groan. **The sweep**, as it drops out of the top and climbs up behind: a close jet pass, a broad roar fading dark. **The capsule**, as it tears open (phase 3): a dry shell crack, a hiss of gas and a metal gate's creak. **The burst**, at its death, cutting Vorne's last line: a deep metal impact cut short, falling debris and a far rumble settling. Also in the level: **the reactor core's death** (a metal clank and a falling electric whine) and **the archive** breaking (a heavy hinge squeak and a big metal drawer pulled). | [the fight](../../enemies/bosses/vornes-chimera/concept/chimera-fight-r93-a.mp4), [rise](../../audio/sfx/concept/chimera-rise-r84-a.ogg), [pain](../../audio/sfx/concept/chimera-pain-r84-b.ogg), [sweep](../../audio/sfx/concept/chimera-sweep-r84-b.ogg), [capsule](../../audio/sfx/concept/chimera-capsule-r84-b.ogg), [burst](../../audio/sfx/concept/chimera-burst-r84-a.ogg), [core death](../../audio/sfx/concept/reactor-core-death-r84-a.ogg) | its gun ports have no muzzle flash or shot sound of their own per shot; a hit on the capsule shows only its cracks | **ok** | ok: the fight's sounds in place confirmed |

## Music

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| M.1 | [Track 34 "Vorne's last stand" in Level 48](../../audio/music/README.md) | **ok or redo**. Round 86's chosen take, in place: after the mini-boss sting it plays from the Chimera's rise to his death, then **cuts dead** on his last line (no fade, no sting), leaving the bed alone for the calm; track 16 comes back when the scroll runs on. Hear it in the fight's recording (L.3), the cut at about 1:10. | [the fight](../../enemies/bosses/vornes-chimera/concept/chimera-fight-r93-a.mp4), [the track](../../../assets/music/vorne-last-stand.ogg) | — | **ok** | ok: track 34 in place confirmed |

## Voice

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| V.1 | [Vorne's fused voice](../../audio/voice/README.md) | **accept or redo**. Round 84's V.2 b in place: Vorne speaks with the Choir's voices under him, a breath behind, lower, in a big hall; quieter under him at first, then almost as loud from his shout on. His eight lines and the Choir's one, in the fight's order. | [the lines](../../audio/voice/concept/level-48-vorne-r93-a.ogg), [the cue list](../../audio/voice/concept/level-48-vorne-r93-a.txt) | not listened to by Claude: the speech checker drops words in the fused lines (the voices under him), so whether every word is clear is for your ear; the last line is cut after "trying to" | **accept** | accepted: Vorne's fused voice confirmed |
| V.2 | [Vorne's shout](../../audio/voice/README.md) | **accept or redo**. "*Get out of my head!*", his one shout, at the fusion's pain. It plays as you heard it in round 73 (his voice at its natural pitch); the voice document's rule to lower it a little was not applied. | [the shout](../../audio/voice/concept/level-48-vorne-shout-r93-a.ogg) | lowering it would make it darker and closer to his calm lines; as built it is the take you chose | **accept** | accepted: the shout as round 73's take confirmed |

## Art to confirm

Part F's production art (step ART-F), already in the game: accept, or redo with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.1 | [Vorne's Chimera](../../enemies/bosses/vornes-chimera/README.md) | **accept or redo**. Round 84's "thorned graft" made final: the three phases (sealed gold, the sockets torn open with the sacs, the capsule open with Vorne in the seat), banking, the gold parts in every state, the tells, the fusion, the capsule opening, the tissue closing at the death and the burst. | [sheet](../../enemies/bosses/vornes-chimera/concept/vornes-chimera-final-r93-a.png), [motion](../../enemies/bosses/vornes-chimera/concept/vornes-chimera-final-r93-a.gif) | its gold share is a little under the target (34 % of 35 %); in play the dead batteries read as dark rings | **accept** | accepted: production art confirmed (rows chosen) |
| F.2 | [The citadel kit](../../campaign/act-7-beyond-the-gate/level-48-vornes-end/README.md) | **accept or redo**. Level 48's backdrop: the outer growth, the gold decks with the cores in their hulls and their veins, the spire road and its side chasms, the heart at the halt with its lid splitting, the spire's inner wall. | [sheet](../../campaign/act-7-beyond-the-gate/level-48-vornes-end/concept/backdrop-final-r93-a.png), [time-lapse](../../campaign/act-7-beyond-the-gate/level-48-vornes-end/concept/backdrop-final-r93-a.gif) | the wrecks' plating is drawn as buckled plates, busy at play size; in the arena the heart reads as a large flat tan shape at the top | **accept** | accepted: production art confirmed (rows chosen) |
| F.3 | [The reactor cores, the archive and the spire well](../../enemies/ground/reactor-core/README.md) | **accept or redo**. The reactor core (whole, cracked, dead) and its breathing light, its death, Vorne's archive and its break, the open gold hulls, a core's vein going dark, the spire well's frames. | [sheet](../../campaign/act-7-beyond-the-gate/level-48-vornes-end/concept/l48-props-final-r93-a.png), [motion](../../campaign/act-7-beyond-the-gate/level-48-vornes-end/concept/l48-props-final-r93-a.gif) | a core's vein is a thin line: it going dark is easy to miss in play (L.2) | **accept** | accepted: production art confirmed (rows chosen) |
| F.4 | [Level 48's briefing images](../../ui/briefing/README.md) | **accept or redo**. Okafor's chart of the citadel from above (the towers round the spire, the lifeboat's trail ending there) and Varga's scan of a reactor core with its veins leading to the spire. | [images](../../ui/briefing/concept/briefing-images-final-r93-a.png) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.5 | [Vorne's fused portrait](../../story/characters/README.md) | **accept or redo**. Round 84's "edge" made final: his portrait overgrown at one edge with the Choir's teal pattern, for the briefing and the radio. | [portraits](../../story/characters/concept/vorne-fused-final-r93-a.png) | — | **accept** | accepted: production art confirmed (rows chosen) |

## The levels in play

Game captures (step EF5): Level 48 flown by the autopilot with the balance plan's fit and Rook, in real time with the
game's sound.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| L.1 | [Level 48: a rear wave](../../campaign/act-7-beyond-the-gate/level-48-vornes-end/README.md) | **ok or change**. The rear warning, then a Chimera wing rising from the bottom edge in the half the ship is not in. | [the rear wave](../../campaign/act-7-beyond-the-gate/level-48-vornes-end/concept/rear-wave-capture-r93-a.mp4) | — | **ok** | ok: capture rows chosen |
| L.2 | [Level 48: a core burnt](../../campaign/act-7-beyond-the-gate/level-48-vornes-end/README.md) | **ok or change**. A reactor core in its gold hull, its light pulsing up its vein, burnt by the beam; the vein goes dark. | [the core](../../campaign/act-7-beyond-the-gate/level-48-vornes-end/concept/core-veins-capture-r93-a.mp4) | the vein is thin and dark over the busy plating, so its going dark is hard to see at play size; the core's bright light and its flash read well | **ok** | ok: capture rows chosen |
| L.3 | [Vorne's Chimera: the whole fight](../../enemies/bosses/vornes-chimera/README.md) | **ok or change (boss fight)**. The whole fight in real time with sound, the whole screen: the rise out of the well, phase 1 (the gold guns, the Darts, the lance), the fusion, phase 2 (the sacs and three rear sweeps), the capsule opening, phase 3, his last line cut by the burst. About 80 seconds, with Rook. | [the fight](../../enemies/bosses/vornes-chimera/concept/chimera-fight-r93-a.mp4) | flown by the autopilot (easier to watch than to play); the fight lasts about a minute as the document wants | **change** | **change, a Chimera fight rework is needed** (more attacks, a much higher fire rate, pressure that beats the shield's regeneration): open, not started; the user's note: "As with previous boss fights, there's way too little action. The boss fires at such a slow rate that you don't even have to dodge the bullets. The shield generates faster than the boss kills it. From what I can see, even doubling the rate will not be enough. Also, the weapon arsenal of the boss seems very limited. Add some other weapons, increase the rate. See if that makes it better."; the capture row stays chosen as the record of the call; recorded in the Chimera's and Level 48's Decisions |
| L.4 | [The rear sweep](../../enemies/bosses/vornes-chimera/README.md) | **ok or change**. Its tell (the far flank flares teal, Rook calls it), the drop out of the top, the glow at the bottom edge, the climb up the far side firing spores. | [the sweep](../../enemies/bosses/vornes-chimera/concept/rear-sweep-capture-r93-a.mp4) | — | **ok** | ok: capture rows chosen |
| L.5 | [The fusion and the capsule](../../enemies/bosses/vornes-chimera/README.md) | **ok or change**. Phase 1's end: the fusion's green light, the tissue over the dead gold, the sacs growing. Phase 3's start: the capsule tearing open. | [the fusion](../../enemies/bosses/vornes-chimera/concept/fusion-capture-r93-a.mp4), [the capsule](../../enemies/bosses/vornes-chimera/concept/capsule-capture-r93-a.mp4) | in this run the capsule opened while the Chimera was still climbing back from a sweep at the right edge, so the opening is half seen | **ok** | ok: capture rows chosen |
| L.6 | [The death on his last line](../../enemies/bosses/vornes-chimera/README.md) | **ok or change**. The capsule broken: the bar goes, the shots in flight fade, the music cuts, the Chimera hangs and convulses while Vorne speaks his last line, and the burst cuts him off mid-word. | [the death](../../enemies/bosses/vornes-chimera/concept/death-capture-r93-a.mp4) | — | **ok** | ok: capture rows chosen |

## Important calls

Calls the part F steps made where the documents left room, each built as recommended. Accept, or change with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [A denser Level 48](../../campaign/act-7-beyond-the-gate/level-48-vornes-end/README.md) | **accept or change**. With the planned waves alone the screen was too empty, so 63 waves of mixed small enemies were added, all from the front and kept to the edges while the Seraph pair is on the screen; the rear waves are as planned. Recommended: accept. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.2 | [The Chimera's strength](../../enemies/bosses/vornes-chimera/README.md) | **accept or change**. At the document's strength the fight took about 100 seconds against the minute it asks for, so its parts were made weaker (the batteries and the capsule most) and its sacs heal more slowly; the rear sweep now comes at least every second attack, so it shows two to four times. Recommended: accept (judge it in L.3). | [the fight](../../enemies/bosses/vornes-chimera/concept/chimera-fight-r93-a.mp4) | on hard the fight lasts about 75 seconds, one seed 91 | **accept** | accepted as built (the fight's strength is superseded by the rework asked for in L.3); recorded in the part's Decisions |

## Decisions

- 2026-11-07: round 93 opened (M11 part F built, step EF5): S.1 the fight's sounds in place, M.1 track 34 in place, V.1
  Vorne's fused voice and V.2 his shout, F.1–F.5 part F's art to confirm, L.1–L.6 Level 48 and the whole Chimera fight
  in play (one real-time recording with sound), C.1–C.2 the important calls; trivia in the summary.
- 2026-11-07 (user, closed; page answers): **S.1 ok**, **M.1 ok**, **V.1, V.2 accept**, **F.1–F.5 accept** (the `*-final-r93-*` art rows chosen), **L.1, L.2, L.4–L.6 ok** (capture rows chosen), **C.1, C.2 accept**. **L.3 (the whole Chimera fight): change**, the user's note: "As with previous boss fights, there's way too little action. The boss fires at such a slow rate that you don't even have to dodge the bullets. The shield generates faster than the boss kills it. From what I can see, even doubling the rate will not be enough. Also, the weapon arsenal of the boss seems very limited. Add some other weapons, increase the rate. See if that makes it better." Outcome: **a Chimera fight rework is needed** (more attacks, a much higher fire rate, pressure that beats the shield's regeneration); **open, not started** (the user asked for the results to be recorded and no new work); recorded in the Chimera's and Level 48's Open questions and Decisions. The `chimera-fight-r93-a` capture row stays chosen as the record of the call.
