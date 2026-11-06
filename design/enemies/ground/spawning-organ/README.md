---
title: Spawning organ
design: review
implementation: done
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../hive-node, ../../air/whirl-seed, ../../air/skitter, ../../../world/vrell-space, ../../../campaign/act-7-beyond-the-gate/level-45-hive-reef]
updated: 2026-11-06
---

# Spawning organ

## Summary

A swollen breeding sac rooted in the Vrell hive reef, the largest and brightest thing on the plates: it throws Whirl
Seed clusters and Skitter pairs at the ship by turns while it is on the screen. Level 45's eight `destroy-targets`
targets (O1–O8): soft (not hardened), 183 HP; one that scrolls past alive fails the mission at once.

## Design

### Stat block

The approved text of [Level 45](../../../campaign/act-7-beyond-the-gate/level-45-hive-reef/README.md#the-spawning-organ)
(round 84: A.5 b "reef polyp", J16, D.4) promoted to a part of its own in M11 part C (step C2), because the primary's
`destroy-targets` groups and the loader find units only here (the [Spawning Reef](../spawning-reef/README.md)'s
precedent; gap check `m11c-gaps.md` §4). Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)),
from its [data.yaml](data.yaml) (the table is rendered from it); 183 HP is the Hive Node's 64 × the act HP factor of a
returning `medium` unit from its first level (L09, reference 63) at L45 (reference 180), about 1.0 s at the reference.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `ground` (the hive reef's chitin plates) |
| Size tier | `medium` |
| Size | 80×80 px, hitbox 60×60 |
| Parts | single |
| Orientation | `radial` (no headings; the sac breathes, its pods swell, the iris opens and shuts) |
| HP | 183 (easy 137 / hard 238, from the global multipliers) |
| Armour / shield | none (soft, not hardened like the [Hive Node](../hive-node/README.md): every weapon that hits `ground` hurts it, `anti-ground` × 2, `area` blasts whole) |
| Speed | 0 px/s (scrolls with the ground) |
| Movement | `terrain` (rooted in the reef's plates on nine roots) |
| Attack | `spawn`: 1.5 s after it enters, then every 3 s, it releases in turn a [Whirl Seed](../../air/whirl-seed/README.md) cluster of 6 after its seed pod's 0.5 s swell and a Skitter pair after its 0.5 s iris (the [Hive Node](../hive-node/README.md)'s, 120° toward the ship at 160 px/s); a release due while the ship is within 120 px waits until it is off; its clock runs on under the EMP; no direct fire |
| Formations | single (placed one by one, each its own named target O1–O8) |
| Weak points | open iris (drawn only; the pods swell and the iris glows before a release, the spawn's telegraph) |
| Effective traits | `anti-ground` |
| Credits | 45 (score 450 × chain); not counted among the level's enemies (the primary's objects); the seeds and Skitters it releases pay their own 3 and 5 and count as released |
| Death | Vrell space's vacuum: the sac tears and collapses, a short flash and a ring of ichor that spreads and fades; the torn husk stays on the plates, its veins going dark outward |
| First level / used in | L45; Level 45 (the primary `ORGANS`, O1–O8) |
| Difficulty hooks | easy: clusters of 5; hard: clusters of 8 and Skitter triples |
<!-- /data -->

### Behaviour

- **A spawner on the reef**: the [Hive Node](../hive-node/README.md)'s periodic spawn with Level 45's changes. Its first
  release comes **1.5 s** after its centre crosses the top edge, then one every **3.0 s**, taking turns: a
  [Whirl Seed](../../air/whirl-seed/README.md#act-7-the-hive-reefs-organs-draft) **cluster** of 6 (easy 5, hard 8)
  spiralling out of its crown after its seed pod's 0.5 s swell, then a **Skitter pair** (hard 3) after its 0.5 s iris,
  flying out at 160 px/s in a 120° arc toward the ship. At 100 px/s an organ is on the screen about 6.2 s: at most two
  releases before it leaves, fewer when it dies fast.
- **Holds, never skips**: a release due while the ship's centre is within **120 px** waits until the ship is off (the
  reef's rule, so no cluster opens on the ship); the next one comes a whole cycle after the late one.
- **The EMP**: its release clock runs on under the EMP Burst (an object, nothing to stun; round 84 D.4).
- **Soft**: no armour (unlike the hardened Hive Node): every weapon that hits `ground` hurts it, `anti-ground` × 2,
  `area` blasts whole.
- **Not counted**: Level 45's primary objects are not among the level's enemies (its density and kill counts); the
  seeds and Skitters it releases count as released and pay their own bounties.
- **Death**: Vrell space's vacuum: the sac tears and collapses, a short flash and a ring of ichor; the torn husk (its
  `-stump`) stays on the plates, its veins going dark outward (its `-creep`).

### Difficulty and level use

- Easy: clusters of 5 (`spawn_count`); hard: clusters of 8 and Skitter triples (`spawn_count`, `alternate_count`); its
  HP by the global multipliers (137 / 238).
- Level 45 only.

## Concept art

Round 84's proposals and round 90's production art live with the level that places it:
[Level 45's Concept art](../../../campaign/act-7-beyond-the-gate/level-45-hive-reef/README.md#concept-art) (A.5 b chosen
in round 84; the production frames `spawning-organ-final-r90-a`, `tools/art/act7_reef.py organ`, in `assets/` since
step C2 claimed them).

## Implementation

- [x] Stat block in a `data.yaml`, loaded and checked (M11 part C, step C2): the spawn's `first`, `formation: whirl
      cluster`, `hold`, `emp: false` and `alternate` keys (CD1's spawner) and the hook `alternate_count`
- [x] Placed as Level 45's eight `destroy-targets` groups O1–O8, uncounted (`counted: false`), flown by `Level45Test`
- [x] Looks and sounds in the game (step C4): the breath, the pod swell, the iris, the tear and ichor ring, the torn husk

## Decisions

- 2026-11-06 (M11 part C, step C2; gap check `m11c-gaps.md` §4, pending the user's review in round 90): promoted from
  Level 45's "row in the enemies' data" to its own part (the loader and the primary need a unit); `tier: medium`, the
  `radial` orientation, `anti-ground` as its effective trait; "holds a release" built as a wait (the next release a whole
  cycle after the late one), not the Hive Node's skip.
- 2026-11-06, round 90: F.1 (the organ's production art) accepted; C.4 accepted: the organ's six seeds spiral out together, not one after another.
