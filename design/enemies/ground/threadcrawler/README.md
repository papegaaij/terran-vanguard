---
title: Threadcrawler
design: approved
implementation: done
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../air/coilwyrm, ../../../campaign/act-3-red-dust]
updated: 2026-10-18
---

# Threadcrawler

## Summary

A giant olive Vrell centipede of 10–16 segments that crawls along canyon floors and the foot of the
canyon walls, legs rippling down its body, its spore pores firing head to tail as a travelling wave of
slow aimed shots. The head is vital; cutting the body leaves the rear to die. The act's ground
chain (Level 16; returns in Act 7).

## Design

### Stat block

Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)). The numbers live in
[data.yaml](data.yaml) (M7 part C, step C2), with the `crawl` movement (the path is the wave's), the segment chain's
`cut` and `width` and the `pores` attack of the [schemas](../../../tech/architecture/README.md#data-file-schemas);
the table is rendered from it. HP is in damage units (a Pulse Cannon L1 shot
does 2). Set at its first level from the proposed Act 3 reference DPS (M18 = a, approved in round 36:
L16 = 80): the head's 80 HP is 1.0 s (a `large` head, 1–3 s with the body in the way), a segment
0.1 s. As a `large` unit it gets the act HP factor wherever it returns (every part).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `ground` (the canyon floor and the walls' foot) |
| Size tier | `large` |
| Size | 56×56 px, hitbox 40×40 (the head); segment 48×32 (hitbox 36×24); tail 40×40 (hitbox 28×28) |
| Parts | `vital` head, 10–16 `destroyable` segments (12 by default), `destroyable` tail |
| Orientation | `16 angles` per part, following the head's path history (`chain`) |
| HP | head 80 · segment 8 each · tail 20; whole 196 (easy 147 / hard 255, from the global multipliers) |
| Armour / shield | none |
| Speed | 70 px/s along its path (plus scroll) |
| Movement | `crawl` along an authored ground path over the canyon floor and along the walls' foot (the wave's `crawl`); the segments follow by `chain`; the legs ripple in a wave down the body (4 leg frames, phase offset per segment) |
| Attack | each segment's pore fires one slow `aimed` spore in turn, head to tail, 0.25 s apart (a travelling wave), then 2 s rest; slow 110 px/s, `small` 4; a pore glows lime 0.3 s before it fires |
| Formations | snake (solo); never two on screen |
| Weak points | the head's lime eye clusters (the vital head) |
| Effective traits | `anti-ground`, `piercing`, `beam` |
| Credits | 56 (score 560 × chain); whole (head-first kill) 48: the head's 30 + its first bonus 18; parts: head 30, segment 2 each, tail 2 + 4 if destroyed first (60 with 12 segments) |
| Death | head killed: the body dies segment by segment from head to tail, 0.08 s apart (`small` bursts, a `medium` at the head); a curled husk stays on the ground; a cut's rear part dies 1 s after the cut, from the cut backwards |
| First level / used in | L16; returns L45 (Act 7) with the act HP factor |
| Difficulty hooks | easy: 10 segments, the wave every other pore; hard: 16 segments, rest 1.5 s |
<!-- /data -->

### Behaviour

- **Chain.** As the [Coilwyrm](../../air/coilwyrm/README.md)'s rig, on the ground: the head
  crawls the path, each part follows its path history at its own spacing, so loops and bends
  stay intact. Each part's 16 headings follow its own direction.
- **Floor and walls.** Its path runs over the canyon floor and along the walls' foot (Level 16's
  terrain, M2 = a); the walls rise toward the camera and their faces are scenery (MA.1 = b of
  round 36, the tower rule), so it never climbs a face. Shots that hit bare rock around it stop.
  Once per level it crosses the floor from wall to wall, its body across the path (Q.22c).
- **Travelling wave.** Every 2.0 s rest, the pores fire head to tail 0.25 s apart, each aimed at
  the ship at the moment it fires: with 12 segments a 3 s ripple of 12 spores that sweeps across
  the screen as the body curves. A pore glows brighter for 0.3 s before it fires (the tell). The
  bullet budget trims the tail end of a wave first.
- **Cutting it** (our proposal for review): destroying a body segment splits the chain; the rear
  half **stops, writhes and dies** after 1.0 s from the cut backwards (no new head: regrowing is
  the Coilwyrm's trait). The front half crawls on. The tail pays a bonus when destroyed first
  (6 against a segment's 2).
- **Code fit.** The Coilwyrm's `Chain` rig on the ground layer, a path that follows the terrain's
  walls, and a pore sequence (§2.8 of the gap analysis).

### Difficulty and level use

- Segment counts and the wave per difficulty per the stat block; the global multipliers apply.
- The canyon is narrow: a level keeps its path's loops inside the flyable width and away from
  tendril gates, so its spores and a gate never stack.

## Concept art

Chosen concept from [round 06](../../../concept-rounds/round-06/README.md) (listed in the
[ground](../README.md#concept-art) Concept art table): the olive/lime centipede chain with its leg
ripple and pores firing as a travelling wave. Production art follows the act's art track in its
level's round.

| File | What | Status |
|---|---|---|
| [../concept/threadcrawler-r06-a.png](../concept/threadcrawler-r06-a.png) | Threadcrawler — centipede chain (olive/lime) ~300 px, leg ripple, spore pores firing as a travelling wave (sheet) | chosen |
| [../concept/threadcrawler-r06-a.gif](../concept/threadcrawler-r06-a.gif) | Threadcrawler — the same in motion | chosen |

## Implementation

- [x] `Chain` on the ground layer: head + 10–16 segments + tail, per-part `16 angles`, path history spacing
- [x] `crawl` along an authored path over the floor and along the walls' foot (with the terrain of part C)
- [x] Pore wave: head to tail 0.25 s apart, aimed, slow `small`; 2.0 s rest; a 0.3 s pore glow tell
- [x] Cut: the rear half stops and dies 1.0 s later; head kill chains the deaths to the tail
- [x] Part and whole bounties, husk, death sequence
- [x] Data file with the stat block (when Level 16 is built; M7 part C, step C2)
- [x] Production art (round of part C): step ART-C, approved as final in [round 39](../../../concept-rounds/round-39/README.md) (F.4)

## Open questions

- None open: round 36 answered it (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the [ground roster](../README.md) to a draft
  spec with the round-06 concept as its chosen art. M18 = a taken as the recommended option,
  pending the user's review in round 36. Our proposals: head 80, segment 8, tail 20; the rear
  half of a cut dies (no regrow, unlike the Coilwyrm); bounty 48 whole, 60 in parts (the multi-part
  rule); the 0.25 s pore wave with a 2.0 s rest.
- 2026-10-13 (user, round 36): approved (D.11). Q.22c yes: once per level it crosses the canyon floor from wall to wall, its body across the path. MA.1 = b (the canyon walls rise toward the camera; by the tower rule nothing gameplay touches climbs a face): its path keeps to the canyon floor and the walls' foot, no longer up the wall faces (an adaptation made at the close, pending the user's review).
- 2026-10-14 (M7 part C, step C2): the stat block moved into [data.yaml](data.yaml): the `crawl`
  movement (the path is the wave's), the chain's `cut` (1.0 s) and `width` (hit box 36×24), the `pores`
  attack with its easy and hard hooks. The credits as data (pending the user's review in round 39): a
  head-first kill pays the whole 48 as the head's 30 and its first bonus 18; taken apart 60, the
  tail paying 2 and its first bonus 4 when it goes first ("6 against a segment's 2").
- 2026-10-14 (M7 part C, step C5): every item built (steps C3/C4 (simulation, data), G2 (looks, sounds)), flown in Level 16's capture (round 39); the art and the defaults pending the user's review in round 39.
- 2026-10-18 (user, round 39): closed. The production art `threadcrawler-final-r39-a` approved as final (F.4),
  so `art: final`; the credits as data (the whole 48 as the head's first bonus), the up-canyon crawl paths
  starting 30 rows higher and B.2/B.4's choices (the pore glow from the first frame, the cut members unhittable
  until they burst, the husk, the cut's rip at pitch 0.8, the generic tier bursts) kept; its walk loop accepted
  (S.4).
