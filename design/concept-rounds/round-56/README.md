---
title: Concept round 56 — M8 AW, the under-water weapons rework
design: review
implementation: n/a
art: proposed
depends-on: [../../player/weapons/harpoon-torpedoes, ../../player/weapons/depth-charges, ../../player/weapons/ion-beam, ../../world/europa, ../../enemies/naval/barbfin, ../../campaign/act-4-deep-water, ../../ui/hangar, ../../tech/roadmap, ../round-55]
updated: 2026-10-24
---

# Concept round 56 — M8 AW, the under-water weapons rework

## Summary

The review round of the **under-water weapons rework (AW)**, built after your playtest of Levels 22–25 on 2026-10-23
(the Ion Beam outclassed the anti-sub weapons, the Harpoon was tuned for single targets, the Depth Charges were rolled
out behind the ship and trapped a beam player). It is the rework only: part G (Level 27) has **not started**. **You
can now play Levels 22–26 with the rework**; what you are asked is whether the numbers and texts are right. The build
(step log in `m8-gaps.md`, 2026-10-23 and 2026-10-24: the playtest, AW-P, AW1–AW3, VOX-AW): the energy weapons do
×0.2 damage and reach ×0.4 under water; the Harpoon is a fast, light swarm-killer (about 3 a second, 33 damage, a
52 px splash, homing only in a ±15° cone ahead, +50 % speed); the Depth Charges are tossed about 200 px ahead over the
hull and burst on their fuse or on touching a swimmer; the Barbfin is tougher; Levels 23–26's waves are retuned; the
balance plan sells nothing; the warnings are voiced and shown (Okafor's intro, Varga's briefing and radio, the hangar
notice). All checks are green. The recommendation is pre-selected on every row.

## Review

The review page is built from this file by `build/round56-tools/build_review.py` (untracked); the board is
[index.html](index.html) (four cards, written by hand because the stills' names do not fit the board generator's single-letter variants). The four stills are game captures
(Xvfb, `--bench`, medium, `--invulnerable`): Level 23 with the balance plan's fit (Harpoon L1, Depth Charges L1, two
Micro-Missile Pods L2, Sonar Pulse) and Level 24 with `front=ion-beam:3,generator=mk-iii`.

Closed 2026-10-24: everything accepted as built (C.3: the beam may still clear Level 26, the units kept as they are);
Q.1 yes, built in step FX (the strip `UNDER WATER · BULLETS ×0.7 · BEAMS ×0.2 · TORPEDOES +50 %`).

Record of the decisions: [review.html](review.html)

## Art

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| A.1 | [The under-water weapons' looks](../../player/weapons/harpoon-torpedoes/README.md#concept-art) | **accept or change**: the four stills as one row: the **toss** (a depth charge thrown forward from the stern rack over the hull, swelling on its 0.15 s arc and dropping in ahead), the **charges** bursting 150–200 px ahead among a swarm with the harpoon cables above, the **harpoon** (Glassfin swarms falling to the reworked torpedo and its splash) and the **weak beam** (the Ion Beam L3 at 40 % strength, short and faint, scattering out over its last 72 px). Variants not built: the toss from a nose tube, a dim boil-off tip on the beam. | [toss](../../player/weapons/harpoon-torpedoes/concept/aw-capture-r56-a-toss.png), [charges](../../player/weapons/harpoon-torpedoes/concept/aw-capture-r56-a-charges.png), [harpoon](../../player/weapons/harpoon-torpedoes/concept/aw-capture-r56-a-harpoon.png), [beam](../../player/weapons/harpoon-torpedoes/concept/aw-capture-r56-a-beam.png) | the toss has no sound of its own (it reuses the roll-out) | **accept** | closed: accepted |

## Calls the build made

Each is built as described; the recommendation is pre-selected. Every number is in the weapon's `data.yaml`.

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| C.1 | [Harpoon Torpedoes](../../player/weapons/harpoon-torpedoes/README.md#design) | **accept or change** the new numbers: 2.4 to 3 a second (L1 to L4), damage 27 to 33, blast 40 to 52 px with the splash at 0.6, speed 540 to 780 px/s, homing only in a ±15° cone ahead, the turn 90°/s kept; prices and draw unchanged. The Harpoon at L3 does 87 DPS under water for 4 MW. | [the harpoon still](../../player/weapons/harpoon-torpedoes/concept/aw-capture-r56-a-harpoon.png) | one torpedo no longer kills a tough target alone, it needs about two hits | **accept** | closed: accepted |
| C.2 | [Depth Charges](../../player/weapons/depth-charges/README.md#design) | **accept or change** the new numbers: tossed about 200 px ahead, 1.0 to 1.3 charges a second, damage 28 to 50, burst 48 to 60 px, fuse 1.4 to 1.3 s; the charges keep the rear slot. | [the toss](../../player/weapons/harpoon-torpedoes/concept/aw-capture-r56-a-toss.png), [the burst](../../player/weapons/harpoon-torpedoes/concept/aw-capture-r56-a-charges.png) | the rear slot now fires forward, which the name no longer says | **accept** | closed: accepted |
| C.3 | [The beam under water, and Level 26](../../world/europa/README.md#decisions) | **keep or make Level 26's units tougher under water**. The energy weapons do ×0.2 damage and reach ×0.4 (the L3 beam: 80 DPS in the air, 16 under water, a short stub). A flight with a beam plus tail-gun loadout fails Levels 23, 24 and 25 as you wanted, but **still clears Level 26**: its jellies and Anglers are fragile. **keep**: a beam player who gets that far has earned it. **tougher**: more HP for Level 26's swimmers only, so the beam cannot clear it. Recommended: keep | [the beam still](../../player/weapons/harpoon-torpedoes/concept/aw-capture-r56-a-beam.png) | measured by one scratch flight, not asserted by a test | **keep** | closed: kept (the beam may still clear Level 26) |
| C.4 | [The Barbfin and the retuned waves](../../enemies/naval/barbfin/README.md#decisions) | **accept or change**: the Barbfin tougher, 12 to 26 HP (52 at Levels 24 and 25 with the beam era's ×2), because the new Harpoon killed it before its first needle; the waves of Levels 23 to 26 retuned (the Lampreys of Level 23 come as a stream, Level 24's bounty 0.40, Level 25's 0.42). All won on all three difficulties. | | | **accept** | closed: accepted |
| C.5 | [No selling, credits left](../../player/balance-plan.yaml) | **accept or change**: the balance plan sells nothing; the Harpoon L1 is bought at Level 22, the Harpoon L1 and the Depth Charges L1 fitted at Level 23, upgraded at Level 24 (Level 24 also buys the Pressure hull and a wing pod's Micro-missiles L3). Credits left per visit: Level 22 **1,980**, Level 23 **3,698**, Level 24 **1,597**, Level 25 **4,436**. | | the Level 24 margin is the thin one | **accept** | closed: accepted |
| C.6 | [A latched Lamprey and the bosses](../../player/weapons/depth-charges/README.md#decisions) | **accept or change**: a Lamprey latched on the hull sets a charge off at once (its burst never hurts the ship), and a boss's body or living part counts as a swimmer, so a charge bursts on it. | | | **accept** | closed: accepted |
| C.7 | [The warning texts](../../campaign/act-4-deep-water/README.md#decisions) | **accept or change** the four wordings: Okafor's intro page 3 ("Guns lose a third of their bite, beams and lasers nearly all. Save your credits for torpedoes and charges."), Varga's Level 23 briefing ("guns lose a third, beams and lasers nearly all"), her radio line at t=13.5 ("Torpedoes and charges bite as ever; guns lose a third, beams nearly all.") and the hangar's second notice before Level 23, **ENERGY WEAPONS SCATTER IN WATER: FIT TORPEDOES AND DEPTH CHARGES**. The three voice lines are re-recorded. | | | **accept** | closed: accepted |

## Question

| # | Part | What is proposed | Review files | Weak spots (Claude) | Answer | Outcome |
|---|---|---|---|---|---|---|
| Q.1 | [The waterline strip](../../ui/hangar/README.md#decisions) | **yes or no**: the strip shown as the ship goes under (UNDER WATER · BULLETS ×0.7 · DRAG · TORPEDOES +50 %) names the guns, the drag and the torpedoes but not the energy weapons. Should it also say so (for example ENERGY ×0.2)? Recommended: yes, it is the one place a player reads while flying. Not built; it comes with part G's build if you say yes. | | | **yes** | closed: yes; built in step FX: `BULLETS ×0.7 · BEAMS ×0.2 · TORPEDOES +50 %` (`DRAG` dropped to fit one line) |

## Decisions

- 2026-10-24: round opened (step AW4): the rework's art (A.1), seven calls (C.1–C.7) and one question (Q.1). Part G
  (Level 27) is not started.
- 2026-10-24 (user, closed; the round 56 page): A.1 and C.1, C.2, C.4–C.7 accepted; **C.3 keep** (the beam may still
  clear Level 26); **Q.1 yes**: the waterline strip names the energy weapons, built in step FX as `BULLETS ×0.7 ·
  BEAMS ×0.2 · TORPEDOES +50 %` (its `DRAG` dropped to keep one line in the play field, pending the user's review).
  The steps' logged small rules are accepted. The user's playtest of Levels 22–26 with the rework: "the new weapon setup
  works much better"; at medium with everything at L5 a bit overpowered, which the user put down to being too good for
  medium: no balance change.
