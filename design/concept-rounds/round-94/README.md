---
title: Concept round 94 — M11 part G "Level 49 Echoes" and the reworked Chimera
design: approved
implementation: n/a
art: chosen
depends-on: [../../campaign/act-7-beyond-the-gate/level-49-echoes, ../../enemies/bosses/vornes-chimera, ../../enemies/bosses/brood-carrier, ../../enemies/bosses/dust-colossus, ../../enemies/bosses/iron-sovereign, ../../audio/sfx, ../../audio/music, ../../audio/voice, ../../ui/briefing, ../../tech/roadmap, ../round-93]
updated: 2026-11-13
---

# Concept round 94 — M11 part G "Level 49 Echoes" and the reworked Chimera

## Summary

M11 part G was built (branch `m11-act-7`, uncommitted; the full `./gradlew check` green on 2026-11-08, Acts 1–6's
golden flights and replays unchanged): Level 49 "Echoes" flies through the Choir's memory of Luna, Mars and Ceres, all
in a spectral tint, then the **boss rush**: the echoes of the Brood Carrier, the Dust Colossus and the Iron Sovereign,
each told by its own warning, each followed by a refill. The three echoes were built to your boss recipe (each part its
own gun and silent when destroyed, then the next stage with new tricks), and so was **Vorne's Chimera**, which round 93
sent back with "way too little action". This round asks, in this order: the part's **art**, the **sounds** and the
**music copies** in place, the **voices**, the level and **the whole rush and the whole reworked Chimera fight** in play
(real time, with sound), **three boss calls** and the **important calls**, each with its recommendation. The
recommendation is pre-selected on every row; leave a row alone to keep it.

Decided as trivia, not asked: an echo is a variant of its original's data, so Levels 7, 21 and 35 cannot move; the tint
is a look only and never touches the bullets; an echo forms in 3 s from its station, untouchable, and its shots in flight
fade at its dissolve, its launched units vanishing unpaid; the second warning has no Choir line; the floor of Hellas
exists only in gate 2's stretch and the station only in gate 3's; the secondary's deadline is the first warning; the
databank skips the echoes; the money scale is 0.51; every new gun of the Chimera (the batteries' aimed bursts, the rack's
homing missiles, the bay's fans, the sacs' spore bursts, the spine's lobs, the capsule's port bursts, the rise volley,
the climb volley, the lance sweep and the twin lances) is trivia, judged in L.6.

## Review

The review page is built from this file by `build/round94-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 94`). The game captures are step GH5's
([how](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/prompts.md)).

## Art to confirm

Part G's production art (step ART-G), already in the game: accept, or redo with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.1 | [The memory backdrops](../../campaign/act-7-beyond-the-gate/level-49-echoes/README.md) | **accept or redo**. Luna with Earth over its limb, Mars with Valles's walls, the belt with Ceres's lights, the remembered Hellas floor, each forming from the top edge down and melting into dark tissue at the sides, no straight seam between the worlds. | [sheet](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/backdrop-final-r94-a.png), [Luna melting, Mars forming](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/backdrop-final-r94-a.gif) | Luna's limb has the longest straight edge (77 px, inside the limit); the big pictures are pieces over one tissue tile; the third gate has no ground tile (the Sovereign is its own station) | **accept** | accepted: production art confirmed (rows chosen) |
| F.2 | [The echo tint](../../campaign/act-7-beyond-the-gate/level-49-echoes/README.md) | **accept or redo**. How a remembered ship or echo looks: its colours drained toward violet-white, its edge eroding into motes, a slow shimmer; the bullets stay untinted so they read. The twelve remembered units original and tinted over their worlds. | [sheet](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/echo-tint-final-r94-a.png) | a still sheet: the shimmer and the erosion are only judged in motion (L.1, L.2) | **accept** | accepted: production art confirmed (rows chosen) |
| F.3 | [The supply pod](../../campaign/act-7-beyond-the-gate/level-49-echoes/README.md) | **accept or redo**. The level's secret: a remembered Coalition supply pod tumbling over Luna in the tint, shot apart. | [sheet](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/supply-pod-final-r94-a.png), [motion](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/supply-pod-final-r94-a.gif) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.4 | [Level 49's briefing images](../../ui/briefing/README.md) | **accept or redo**. Okafor's long-range scan past the citadel (three terrain signatures stacked on one point, LOCATION: INVALID in red) and Varga's comparison of Level 7's Brood Carrier with a new violet trace of the same shape (MATCH 98 % circled). | [images](../../ui/briefing/concept/briefing-images-final-r94-a.png) | — | **accept** | accepted: production art confirmed (rows chosen) |

## Sound

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| S.1 | [An echo forming and dissolving](../../audio/sfx/README.md) | **ok or redo**. Round 84's picks, now in place: hear them in the recordings (L.2, L.3). **Forming**: plays once per echo, as the memory builds the next boss out of violet motes; a soft rising metal wash, like a bowed cymbal swell, 2.5 s, no chime. **Dissolving**: plays at an echo's death, as it comes apart into motes; the same wash played backwards, falling away, 2.5 s. A dissolving unit plays it quieter. | [forming](../../audio/sfx/concept/echo-form-r84-a.ogg), [dissolving](../../audio/sfx/concept/echo-dissolve-r84-a.ogg), [forming in the game](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/echo-forming-capture-r94-a.mp4), [dissolving in the game](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/echo-refill-capture-r94-a.mp4) | not listened to by Claude: checked by level only | **ok** | ok: the echo sounds in place confirmed |
| S.2 | [The memory beds](../../audio/sfx/README.md) | **ok or redo**. Plays under the approach, one bed per world, crossfading over 2 s as each memory forms (Luna from the start, Mars at t=31, Ceres at t=61), then the hive's bed for the unmaking. Each is the level's built bed for that place heard from far away: slower, duller, in a long hall. **Luna**: the space-wind drone. **Mars**: the dust wind. **Ceres**: open space's far engine hum and creaks. | [Luna](../../audio/sfx/concept/luna-memory-r84-a.ogg), [Mars](../../audio/sfx/concept/mars-dust-memory-r84-a.ogg), [Ceres](../../audio/sfx/concept/belt-open-memory-r84-a.ogg), [the approach](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/memory-wave-capture-r94-a.mp4) | not listened to by Claude: each loop is seamless and about the same level (−33.5 LUFS), but whether the three read as three places is for your ear | **ok** | ok: the memory beds in place confirmed |

## Music

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| M.1 | [The five memory copies](../../audio/music/README.md) | **ok or redo**. The tracks you know, heard as memories (far off, hollow, slowed a little), in place: the approach plays *Afterburner* over Luna, *Red Dust Run* over Mars and *Hollow Rock* over Ceres, crossfading as each memory forms; each echo plays its boss track's copy (the Carrier and the Colossus *The Choir Descends*, the Sovereign *Iron Sovereign*), falling away at its death. Hear them in the rush (L.5). | [Afterburner](../../audio/music/concept/afterburner-memory-r84-a.ogg), [Red Dust Run](../../audio/music/concept/red-dust-run-memory-r84-a.ogg), [Hollow Rock](../../audio/music/concept/hollow-rock-memory-r84-a.ogg), [The Choir Descends](../../audio/music/concept/choir-descends-memory-r84-a.ogg), [Iron Sovereign](../../audio/music/concept/iron-sovereign-memory-r84-a.ogg), [the rush](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/echo-rush-r94-a.mp4) | set at the originals' level minus 4 dB (−18 LUFS): it may be too quiet next to the guns; the loops' seams are within 3 dB, not listened to by Claude | **ok** | ok: the five memory copies in place confirmed |

## Voice

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| V.1 | [Level 49's lines](../../audio/voice/README.md) | **accept or redo**. The 22 spoken lines of the level in its order: Okafor's and Varga's briefing pages, then the radio (Okafor, Varga and Rook, with the pod's secret), as the game plays them. | [the lines](../../audio/voice/concept/level-49-echoes-r94-a.ogg), [the cue list](../../audio/voice/concept/level-49-echoes-r94-a.txt) | not listened to by Claude: checked by level and read back by Whisper; Rook's line at t=23.5 needed a hand-picked take | **accept** | accepted: the lines confirmed |
| V.2 | [The Choir's two lines](../../audio/voice/README.md) | **accept or redo**. "*…you broke these… we remember… we remember everything we lose…*" before the first echo and "*…he remembered this one… he is ours now… so it is ours…*" before the third: the Choir on its clean channel, unfiltered like its other lines. | [the two lines](../../audio/voice/concept/level-49-choir-r94-a.ogg), [the cue list](../../audio/voice/concept/level-49-choir-r94-a.txt) | not listened to by Claude | **accept** | accepted: the lines confirmed |

## The levels in play

Game captures (step GH5): Level 49 and the reworked Chimera flown by the autopilot on the balance plan's fit with Rook,
replayed by the installed game in real time with its sound.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| L.1 | [Level 49: a remembered wave](../../campaign/act-7-beyond-the-gate/level-49-echoes/README.md) | **ok or change**. Remembered Stingers and Skitters over Luna, the side warning, the tint on the units and their untinted bullets, Luna dissolving into the tissue at its edges. | [the wave](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/memory-wave-capture-r94-a.mp4) | few enemy bullets over Luna in this stretch | **ok** | ok: capture rows chosen |
| L.2 | [Level 49: an echo forming](../../campaign/act-7-beyond-the-gate/level-49-echoes/README.md) | **ok or change**. The first warning, the Brood Carrier's echo forming out of the memory already broadside, its forming volley of pink spores before the bar. | [the forming](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/echo-forming-capture-r94-a.mp4) | — | **ok** | ok: capture rows chosen |
| L.3 | [Level 49: the refill](../../campaign/act-7-beyond-the-gate/level-49-echoes/README.md) | **ok or change**. The Carrier's death and dissolve, the second warning, the armour and shield bars refilling and `ECHO 2 / 3` with its pips. | [the refill](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/echo-refill-capture-r94-a.mp4) | the Colossus's sand floor forms top-down within about a second behind the warning banner, so its forming edge is hidden by the banner | **ok** | ok: capture rows chosen |
| L.4 | [Level 49: the tint and the bullets](../../campaign/act-7-beyond-the-gate/level-49-echoes/README.md) | **ok or change**. Three game frames at 1:1: do the bullets read against the tinted units over Luna, Mars and the belt? | [the frames](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/tint-bullets-r94-a.png) | the Luna frame has few bullets | **ok** | ok: capture rows chosen |
| L.5 | [Level 49: the whole boss rush](../../campaign/act-7-beyond-the-gate/level-49-echoes/README.md) | **ok or change (boss fight)**. The whole rush in real time with sound, the whole screen, about 3 minutes: the Carrier (0:03–1:04, its second stage from 0:37), the Colossus (1:07–1:57, second stage from 1:39: breaches, vents, the sealed mouth), the Sovereign (2:00–3:01, laser sweeps from 2:10, second stage from 2:37). Each echo's parts fire and go silent when destroyed; then the next stage. Is there enough to dodge, and does it feel like the boss recipe? | [the rush](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/echo-rush-r94-a.mp4) | flown by the autopilot (it dodges, so the fire mostly misses); the echoes last 53, 45 and 53 s | **ok** | ok: capture rows chosen |
| L.6 | [Vorne's Chimera, reworked: the whole fight](../../enemies/bosses/vornes-chimera/README.md) | **ok or change (boss fight)**. Round 93's L.3 follow-up: is the pressure right now? The whole fight in real time with sound: the rise firing (0:06), the gold guns with their port flashes, parts dying silent, the fusion (0:22), phase 2's sac spores, spine lobs and three rear sweeps, the capsule (0:49), the lance sweep (0:52) and twin lances (0:56), the burst (1:02). The second recording is hard: its phase 2 runs 0:29–0:53, denser. | [medium](../../enemies/bosses/vornes-chimera/concept/chimera-fight-r94-a.mp4), [hard](../../enemies/bosses/vornes-chimera/concept/chimera-fight-hard-r94-a.mp4) | the autopilot dodges, so most fire misses and its armour is still 140 at the end on medium; the port flashes are small; this hard seed's phase 2 is only 24 s (other seeds measure 65–124 s fights); a ship standing still loses all its armour in about 14 s | **ok** | ok: the reworked Chimera fight is right; **closes round 93 L.3's rework follow-up** (the user asked for more action and a higher fire rate; step F5/F6 built it, this recording confirmed it); capture rows chosen |

## Boss calls

Three calls on the echoes, each built as recommended; the recordings are L.5.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| B.1 | [Two stages per echo](../../enemies/bosses/README.md) | **ok or change**. Round 84 said each echo is one phase. They are now two stages, as your recipe asks: first the subtargets (the Carrier's eight sacs, the Colossus's three vents, the Sovereign's six gun turrets), each with its own gun, silent when destroyed; when all are dead the core or mouth opens with new tricks. Recommended: ok, the single phase had too little to do. | [the rush](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/echo-rush-r94-a.mp4) | the Colossus's bar leaves out its vents, so it stands still during the first stage | **ok** | ok: built as recommended; recorded in the boss's Decisions |
| B.2 | [The Colossus echo, paced by its breaches](../../enemies/bosses/dust-colossus/README.md) | **ok or change**. The Colossus echo lasts about 44 s on every seed, because its stages follow its breaches (the front one every second time); its mouth seals for the rest of a breach after a wound, firing from the dunes while it does. Recommended: ok. | [the Colossus in the rush](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/echo-rush-r94-a.mp4) | one hard seed took 65.7 s (a vent outlived three breaches) | **ok** | ok: built as recommended; recorded in the boss's Decisions |
| B.3 | [The Sovereign's gun turrets](../../enemies/bosses/iron-sovereign/README.md) | **ok or change**. The Sovereign echo's six outer turrets (two rails, two guns, two SAMs) fire all round; the gun turrets are drawn with the laser battery's art, because the original has no gun-turret art. Recommended: ok. | [the Sovereign in the rush](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/echo-rush-r94-a.mp4) | the Sovereign echo has quiets of 1.5–2.4 s on easy (not asserted by a test) | **ok** | ok: built as recommended; recorded in the boss's Decisions |

## Important calls

Calls the part G steps made where the documents left room, each built as recommended. Accept, or change with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [The echoes are weaker than their originals](../../enemies/bosses/README.md) | **accept or change**. Each echo lasts about 45–55 seconds (the rush about three minutes with its warnings), so their hit points were cut well below the document's paper numbers (the Carrier's from about 10,000 to 4,000, the Sovereign's from 8,500 to 4,400; the Colossus's length comes from its breaches, B.2). Recommended: accept. | [the rush](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/echo-rush-r94-a.mp4) | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.2 | [A denser Level 49](../../campaign/act-7-beyond-the-gate/level-49-echoes/README.md) | **accept or change**. With the planned waves alone the approach was too empty, so 22 waves of mixed remembered enemies were added to the 30 planned, all kept to the memory of the place they are in. Recommended: accept (the final act's rule). | [the wave](../../campaign/act-7-beyond-the-gate/level-49-echoes/concept/memory-wave-capture-r94-a.mp4) | — | **accept** | accepted as built; recorded in the part's Decisions |

## Decisions

- 2026-11-08: round 94 opened (M11 part G built, steps G1–G6, ART-G, AUD-GH, MUS-G, VOX-GH, GH5): F.1–F.4 part G's art to
  confirm, S.1–S.2 the echo shimmers and the memory beds in place, M.1 the five memory copies, V.1–V.2 Level 49's lines and
  the Choir's two, L.1–L.6 Level 49 and the reworked Chimera in play (the whole rush and the whole fight as real-time
  recordings), B.1–B.3 the echoes' boss calls, C.1–C.2 the important calls; trivia in the summary.
- 2026-11-08 (user, closed; page answers): **F.1–F.4 accept** (the `*-final-r94-*` art rows chosen), **S.1, S.2 ok**, **M.1 ok**, **V.1, V.2 accept**, **L.1–L.6 ok** (capture rows chosen; **L.6 the reworked Chimera fight ok: round 93's L.3 follow-up is closed**), **B.1–B.3 ok**, **C.1, C.2 accept**. No redo, no rejected files; recorded in the named parts' Decisions.
