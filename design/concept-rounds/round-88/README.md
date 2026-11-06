---
title: Concept round 88 — M11 part A "Act 7 systems"
design: review
implementation: n/a
art: proposed
depends-on: [../../player/specials, ../../player/weapons/choir-resonator, ../../enemies/space/gate-warden, ../../enemies/space/rift-skater, ../../world/vrell-space, ../../ui/hangar, ../../ui/hud, ../../ui/debrief, ../../ui/credits, ../../enemies/bosses, ../../player/wingmen, ../../tech/roadmap, ../round-87]
updated: 2026-11-06
---

# Concept round 88 — M11 part A "Act 7 systems"

## Summary

M11 part A was built (branch `m11-act-7`, uncommitted; the full `./gradlew check` green on 2026-11-05, Acts 1–6's
golden flights and replays unchanged): distorted space and its reversals, the window shield, `teleport`, `mirror`,
spore clouds, the quiet's rules, the boss rush's fights with refills and checkpoints, the campaign end's screens and
save, the `beyond-gate` limit, the Bastion Shield, the Vrell Swarm Call, the Mk VI at Level 47, the Choir Resonator and
the Act 7 balance plan, all flown on stand-ins (no Act 7 level exists yet: Levels 43 and 44 are part B). This round
asks, in this order: the **Swarm Call drone's shell** (QA-1), part A's production **art to confirm** with the first game
captures, **the Resonator in play**, and the **important calls** the steps made, each with its recommendation. The
recommendation is pre-selected on every row; leave a row alone to keep it.

**Found while capturing and fixed:** the Choir Resonator crashed the game when one wave front killed two units in the
same step; step FIXA fixed it (with tests) and re-made the Resonator's captures in full. The captures come from a scratch capture rig: a stand-in Act 7 level over ART-B's real
Level 43 and 44 tiles, stand-in Wardens and Skaters on ART-B's held frames, and Act 6 made the last act so the campaign
end runs ([the rig](../../world/vrell-space/concept/prompts.md#act7-rig-capture-r88-a)). Rig artefacts, not the game:
the hard seam where Level 43's and 44's fields meet, and the Warden cycling its iris frames in the Resonator stills.

Decided as trivia, not asked: a regrowing shell counts as open; the leaving tilt is a squash to 0.86 and a 3 px lift
over 0.5 s; the backdrop slides sideways only in the drift; spore clouds are 60–100 px in radius; the transit's
formation point is (240, 96), eased over 2 s; a ring slides away from a blocked arrival; a mirroring unit's row has no
upper cap; a landing kill counts by anyone's shot.

**Closed 2026-11-06**: the answers are in the Answer and Outcome columns and the Decisions. A.1 a, F.1–F.7 accept (F.6 and F.7 with a note about a hard line in the background: a capture-rig artefact, not a game bug), P.1 ok, C.1–C.8 accept.

## Review

The review page is built from this file by `build/round88-tools/build_review.py` (untracked); the board is
[index.html](index.html) (`python3 tools/concept/board.py 88`). The game captures are the A4 rig's screenshots, sheets
and GIFs; the drone sheet over the real tiles is `tools/concept/swarm_tiles_r88.py`.

## Art questions

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| A.1 | [The Swarm Call's drone (QA-1)](../../player/specials/README.md) | **pick a or b**. Six friendly drones you call in; they must never look like a Vrell wave. **a "teal-black"** (recommended, in the game now): the Needler's body re-grown with a dark teal shell and white-cyan glow. **b "bone-white"**: the same body paled to bone-white. Over the real tiles, a stands apart from the pale Needlers everywhere and is sharp on Level 44's bright plane; b reads like a small Needler. | [over the real tiles](../../player/specials/concept/swarm-call-drone-tiles-r88-a.png), [a](../../player/specials/concept/swarm-call-drone-r88-a.png), [a, motion](../../player/specials/concept/swarm-call-drone-r88-a.gif), [b](../../player/specials/concept/rejected/swarm-call-drone-r88-b.png), [b, motion](../../player/specials/concept/rejected/swarm-call-drone-r88-b.gif), [in the game](../../player/specials/concept/swarm-call-capture-r88-a.gif) | a is low in contrast on the dark fields (Level 43's and Level 46's): there only its glow and trail show it | **a** | a "teal-black" chosen (already in the game); b rejected (moved to `concept/rejected/`: b not picked, a reads more clearly as friendly) |

## Art to confirm

Part A's production art (ART-A, `tools/art/act7_gear.py`, `tools/art/icons.py --act7`), already in the game, with the
first game captures of A3a/A3b's looks: accept, or redo with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| F.1 | [The Choir Resonator](../../player/weapons/choir-resonator/README.md) | **accept or redo**. Round 85's "bell" made final: the mount on the nose, the widening wave front (16 widths), the ring on each hull it passes; in the game at L1 and L5 over Level 43's and 44's tiles. | [frames](../../player/weapons/choir-resonator/concept/choir-resonator-final-r88-a.png), [motion](../../player/weapons/choir-resonator/concept/choir-resonator-final-r88-a.gif), [in the game](../../player/weapons/choir-resonator/concept/resonator-capture-r88-a.png), [in the game, motion](../../player/weapons/choir-resonator/concept/resonator-capture-r88-a.gif) | the pale blue front is faint over Level 44's brightest band | **accept** | accepted: production art confirmed (rows chosen) |
| F.2 | [The Swarm Call's drones and bloom](../../player/specials/README.md) | **accept or redo**. The drones (16 headings, claws), their glow and trail, the 0.3 s bloom behind the ship and the 0.5 s wither; in the game on Level 36. | [frames](../../player/specials/concept/swarm-call-final-r88-a.png), [motion](../../player/specials/concept/swarm-call-final-r88-a.gif), [in the game](../../player/specials/concept/swarm-call-capture-r88-a.png), [in the game, motion](../../player/specials/concept/swarm-call-capture-r88-a.gif) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.3 | [The Act 7 shop icons](../../ui/hangar/README.md) | **accept or redo**. The hangar badges of the Choir Resonator, the Bastion Shield, the Swarm Call and the Mk VI. | [icons](../../ui/hangar/concept/act7-icons-final-r88-a.png) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.4 | [The window shield's spark and break](../../enemies/README.md) | **accept or redo**. The pale-gold flake of a spent hit on a closed shell and the shell's shatter; in the game on the code-drawn stand-in shell (the iris at the telegraph, a Plasma Arc break, 3 s open, the regrowth) and the leaving tell with the tracker blinking amber. The Warden's own shell frames come with part B. | [frames](../../enemies/concept/window-shell-final-r88-a.png), [motion](../../enemies/concept/window-shell-final-r88-a.gif), [in the game](../../enemies/space/gate-warden/concept/shell-capture-r88-a.png), [in the game, motion](../../enemies/space/gate-warden/concept/shell-capture-r88-a.gif), [leaving tell](../../enemies/space/gate-warden/concept/leaving-capture-r88-a.gif) | the stand-in iris is faint at play size | **accept** | accepted: production art confirmed (rows chosen) |
| F.5 | [The teleport ring](../../enemies/README.md) | **accept or redo**. The 0.2 s fade and the shrinking ring where a unit will land; in the game on stand-in Skaters. | [frames](../../enemies/concept/teleport-final-r88-a.png), [motion](../../enemies/concept/teleport-final-r88-a.gif), [in the game](../../enemies/space/rift-skater/concept/teleport-capture-r88-a.png), [in the game, motion](../../enemies/space/rift-skater/concept/teleport-capture-r88-a.gif) | — | **accept** | accepted: production art confirmed (rows chosen) |
| F.6 | [The spore cloud](../../world/vrell-space/README.md) | **accept or redo**. The cloud, its veil and its 1 s burn; in the game over the ship and burnt by the Plasma Arc. A stand-in until part C draws Level 45's clouds. | [frames](../../world/vrell-space/concept/spore-cloud-final-r88-a.png), [motion](../../world/vrell-space/concept/spore-cloud-final-r88-a.gif), [in the game](../../world/vrell-space/concept/rejected/spore-capture-r88-a.png), [in the game, motion](../../world/vrell-space/concept/rejected/spore-capture-r88-a.gif) | — | **accept** (note: "I do see a very hard line in the background. Is that a bug in the capture?") | accepted: the cloud art is chosen. The hard line is a capture-rig artefact, not a game bug: part A's rig ran on Level 42 with Level 43's and 44's tiles mixed, meeting at a hard seam; Level 44's own backdrop has no seam (round 89's [reversal clip](../../campaign/act-7-beyond-the-gate/level-44-crossing/concept/reversal-capture-r89-a.gif), chevrons over the real tiles, accepted as L.2). The in-game spore capture is rejected (rig seam; superseded by round 89's Level 44 captures), moved to `concept/rejected/` |
| F.7 | [The code-drawn screens](../../ui/hud/README.md) | **accept or change**. Drawn by the game, no art files: the reversal chevrons (told 2 s ahead, dim while reversed), the boss rush's `ECHO 1 / 3` line and spectral boss bar, the campaign end's card (`CAMPAIGN COMPLETE`, `THE GATE IS DARK.`), the name entry's `WON` and the credits over the ending's last image. | [chevrons](../../world/vrell-space/concept/rejected/drift-capture-r88-a.gif), [echo line](../../ui/hud/concept/echo-capture-r88-a.png), [campaign end](../../ui/debrief/concept/campaign-end-capture-r88-a.png), [credits](../../ui/credits/concept/credits-capture-r88-a.png) | the rig shows Act 6's pages and image where the ending's will be | **change** (note: "chevrons … shows a bug in the background. There is a very hard line with 2 different backgrounds. All others are fine.") | the line is a capture-rig artefact, not a game bug: part A's rig ran on Level 42 with Level 43's and 44's tiles mixed, meeting at a hard seam; Level 44's own backdrop has no seam (round 89's [reversal clip](../../campaign/act-7-beyond-the-gate/level-44-crossing/concept/reversal-capture-r89-a.gif), chevrons over the real tiles, accepted as L.2); the chevrons' own look is kept as it is. The other screens (echo line, campaign end, credits) accepted (chosen). The in-game chevron capture is rejected (rig seam; superseded by round 89's Level 44 captures), moved to `concept/rejected/` |

## The Resonator in play

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| P.1 | [The Choir Resonator on Level 36](../../player/weapons/choir-resonator/README.md) | **ok or change**. The Resonator fitted at the front, firing on its own, with the Swarm Call called once, on a real level. Judge how it looks in play: the wave fronts and the rings (no sound in the clip). | [in play](../../player/weapons/choir-resonator/concept/resonator-play-capture-r88-a.gif) | fitted at L5, 14 s; the double-kill crash A4 found is fixed (step FIXA) and the clip was made again | **ok** | ok: the Resonator in play confirmed |

## Important calls

Calls the part A steps made where the documents left room, each built as recommended; the alternative is in the
cell. Accept, or change with a note.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [The Skater's burst after landing](../../enemies/space/rift-skater/README.md) | **accept or change**. The hard Skater's "fire 0.20 s after landing" is kept at the shared 0.25 s minimum, so a landing is never followed by an instant shot. Alternative: allow 0.20 s on hard. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.2 | [Wardens fire while rising out](../../enemies/space/gate-warden/README.md) | **accept or change**. A Warden keeps firing its line while it rises out of the top edge. Alternative: it stops firing once it starts to leave. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.3 | [The refill between echoes](../../enemies/bosses/README.md) | **accept or change**. The refill between two boss fights is instant; the HUD bars fill up over 1 s to show it. Alternative: the armour and shield actually fill over 1 s. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.4 | [Bombs burn spore clouds](../../world/vrell-space/README.md) | **accept or change**. The Bomb Rack's blasts burn a spore cloud like any other blast. Alternative: only air blasts (missiles, Rook's Mortar) burn them. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.5 | [The drones' orbit](../../player/specials/README.md) | **accept or change**. A drone circles its target at 90° a second while it spits thorns. Alternative: a faster or slower circle. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.6 | [The thorns](../../player/specials/README.md) | **accept or change**. The drones' thorns are the Pulse Cannon's bolt tinted white-cyan, with no art of their own. Alternative: a thorn sprite. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.7 | [Breaking the quiet](../../world/vrell-space/README.md) | **accept or change**. When the player breaks Level 46's quiet (hits a drifting Husk), there is no special look or sound; the Husks simply wake. Alternative: a cue as they wake. | — | — | **accept** | accepted as built; recorded in the part's Decisions |
| C.8 | [A mid-boss in a rush](../../enemies/bosses/README.md) | **accept or change**. A boss rush may include a mid-boss among its fights. Alternative: only full bosses. | — | — | **accept** | accepted as built; recorded in the part's Decisions |

## Decisions

- 2026-11-05: round 88 opened (M11 part A built, step A4): QA-1 the Swarm Call's drone a/b, part A's art to confirm
  with the first game captures (F.1–F.7), the Resonator in play (P.1; the double-kill crash found in A4 fixed by FIXA),
  eight important calls (C.1–C.8); trivia in the summary.
- 2026-11-06 (user, closed): **A.1 a** "teal-black" drone chosen (already in the game); b ("bone-white") rejected, its sheet and clip moved to the specials' `concept/rejected/` (b not picked, a reads more clearly as friendly). **F.1–F.5 accept**, **P.1 ok**: the production art rows (`*-final-r88-*`) and game captures chosen. **F.6 accept** and **F.7 change** carried the same note: "I do see a very hard line in the background. Is that a bug in the capture?" and "chevrons … shows a bug in the background. There is a very hard line with 2 different backgrounds. All others are fine." Outcome for both: a capture-rig artefact, not a game bug (part A's rig ran on Level 42 with Level 43's and 44's tiles mixed, meeting at a hard seam; Level 44's own backdrop has no seam, as round 89's reversal clip shows). `drift-capture-r88-a` and `spore-capture-r88-a` rejected (rig seam; superseded by round 89's Level 44 captures) and moved to `concept/rejected/`; the chevron design is kept as it is; F.7's other screens accepted. **C.1–C.8 accept** (recorded in the parts' Decisions).
