---
title: Spawning Reef
design: review
implementation: in-progress
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../hive-node, ../../naval/glassfin, ../../../campaign/act-4-deep-water/level-24-abyssal-plain]
updated: 2026-10-23
---

# Spawning Reef

## Summary

A Vrell nursery grown out of the abyssal plain: a coral tower with a teal iris that breeds Glassfin fry and releases
them at the ship while it is on the screen. Level 24's secondary target: soft (not hardened), 200 HP, four of them in
the reef field; razing three of the four meets the secondary.

## Design

### Stat block

The approved text of [Level 24](../../../campaign/act-4-deep-water/level-24-abyssal-plain/README.md#ground-targets)
(round 48: "a coral tower with an iris that releases 4 Glassfin every 3.5 s while it is on the screen, 200 HP, not
hardened", bounty 40) promoted to a part of its own in M8 part D (step D2), because the secondary's `escapes` counts a
unit by its slug and the loader finds units only here (gap check `m8d-gaps.md` §5 D1; the level's README allowed the
promotion). Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)), from its
[data.yaml](data.yaml) (the table is rendered from it); 200 HP is 1.9 s at the Level 24 reference DPS of 104.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `ground` (the sea floor of the abyssal plain) |
| Size tier | `large` |
| Size | 96×96 px, hitbox 72×72 |
| Parts | single |
| Orientation | `radial` (no headings; the coral pulses, the iris opens and shuts) |
| HP | 200 (easy 150 / hard 260, from the global multipliers) |
| Armour / shield | none (soft, not hardened like the [Hive Node](../hive-node/README.md): the torpedo weapons at full damage, the others × 0.7 under water) |
| Speed | 0 px/s (scrolls with the ground) |
| Movement | `terrain` (grown out of the plain's floor) |
| Attack | `spawn`: its teal iris opens over 0.5 s every 3.5 s and releases 4 Glassfin fry, flying out at 160 px/s in a 120° arc toward the ship, each drawing its ram (the Glassfin's share) as it leaves the iris; it stays shut while the ship is within 96 px; no direct fire |
| Formations | single (placed one by one, each its own named target) |
| Weak points | open iris (drawn only; it glows teal while the iris opens, the spawn's telegraph) |
| Effective traits | `anti-sub`, `anti-ground` |
| Credits | 40 (score 400 × chain); the fry it releases pay their own 2 and count into the level's enemies as released |
| Death | `large` coral burst: the iris closes, the tower crumbles to a razed stump; the fry already out live on |
| First level / used in | L24; Level 24 (the secondary `REEFS`); a later Europa level may reuse it |
| Difficulty hooks | easy: 3 fry a release; the secondary's allowed escapes change too (easy 2, medium 1, hard 0) |
<!-- /data -->

### Behaviour

- **A spawner on the sea floor**: the [Hive Node](../hive-node/README.md)'s periodic spawn, softened for the water: its
  iris opens over 0.5 s (the telegraph, a teal glow) every 3.5 s while it is on the screen and releases 4 Glassfin fry
  (3 on easy) in a 120° arc toward the ship; it stays shut while the ship is within 96 px. The fry fly straight out at
  160 px/s, then each one rams on its own as a single [Glassfin](../../naval/glassfin/README.md) (the glint tell, the
  dive down the ship's lane), without a school's flock (gap check D4).
- **Razed**: the iris closes, a `large` coral burst, a razed stump; it releases no more fry (the Hive Node's rule). The
  fry already out live on: the level README's "the fry stop when a reef dies" read as no new releases (gap check D4;
  killing the released fry with their reef would be a new link, accepted in round 53 (2026-10-23)).
- **Soft**: no armour (unlike the hardened Hive Node), so the plan's harpoon loadout can raze it: under water the torpedo
  weapons hit it at full damage, every other weapon at × 0.7 (after `anti-ground`'s × 2 for bolts), Europa's
  [under water rules](../../../world/europa/README.md#under-water-rules).
- **The secondary** (Level 24, `REEFS`): the level's `escapes: spawning-reef` counts each reef that leaves the screen
  alive; the secondary is met when the third falls and failed by the second away (easy: the third, hard: the first).

### Difficulty and level use

- Easy: 3 fry a release (the hook `spawn_count`); the secondary's allowed escapes change too (easy 2, medium 1, hard 0).
- Level 24 only for now; a later Europa level may reuse it.

## Concept art

Production art straight away (M8 part D, step ART-D, `tools/art/spawning_reef.py --look a|b`, from the abyssal plain mood
still), two looks for [round 53](../../../concept-rounds/round-53/README.md): **b** "bone chimneys" (a plum dome ringed by
seven leaning bone tube chimneys, the iris at the crown) was **chosen** (accepted as final, in `assets/` since step E5)
and **a** "coral knot" (plum coral bulbs, bone finger coral, teal polyps, a six-petal iris) rejected; both in the Hive Node's frame layout (6 iris states ×
4 breathing frames, `-glow`, `-creep`, `-death`, `-tatters`, `-stump`), so the game's periodic-spawner looks draw either.
The sheets, GIFs and the game capture `spawning-reef-capture-r53-a` are in the [ground](../README.md#concept-art) Concept
art table.

## Implementation

- [x] Stat block in a `data.yaml`, loaded and checked (M8 part D, step D2)
- [x] The fry released as single rammers (a flock kind's fan-out, step D1) and mapped (step D3); the four reefs counted by
      Level 24's secondary (`escapes: spawning-reef`, step D3)
- [x] Production sprite (coral tower, iris open/close frames over the 0.5 s telegraph, the teal glow, the coral-burst
      death, the razed stump), its iris and collapse sounds (steps ART-D, AUD-D and D4; look b and the sounds picked in
      round 53; `Level24LooksTest.theArtIsWhatTheLooksDraw`, `Level24SoundsTest.theRayAndTheReefDieWithTheirOwnSounds`)

## Decisions

- 2026-10-22 (M8 part D, step D2; gap check `m8d-gaps.md` §5 D1 and D4, accepted in round 53 (2026-10-23)):
  promoted from Level 24's ground targets to its own part (the loader and the secondary need a unit), its text Level
  24's approved one; `tier: large` (200 HP is 1.9 s at the L24 reference, first level 24 so act factor 1), soft (no
  armour), the Hive Node's spawn rule including its 96 px `shut_within`; the fry released as single rammers that outlive
  the reef. Step D2's own: the 96 × 96 sprite with a 72 × 72 hit box, the `radial` orientation, the effective traits
  `anti-sub` (the torpedo weapons at full damage under water) and `anti-ground` (× 2), as the siphon pump's.
- 2026-10-22 (M8 part D, step D3, accepted in round 53 (2026-10-23)): the fry **draw their ram as they leave the
  iris** (the spawn's new `ram_after: 0`; the school's ram still after 3 s on the field): with the Glassfin's own 3 s no
  fry ever rammed, as they are gone in 0.6–1.2 s (measured on Level 24 at every difficulty); now about a quarter of them
  (the Glassfin's share by difficulty) glint and dive. Hard's REEFS (all four, none away) is met on none of three
  autopilot seeds: the fry soak up the Harpoon Torpedoes (the homing pods ignore them) and an eel shields the second reef;
  the reef's 200 HP (260 on hard) and hard's 0 allowed are kept as approved.
- 2026-10-22 (M8 part D, steps ART-D and D4; accepted in round 53 (2026-10-23)): two production looks (a "coral
  knot", b "bone chimneys") on the Hive Node's frame layout, b in `assets/` since the pick (step E5); the reef in the game's death
  tiers as `large` (under water the deep large burst) with its own collapse sound. Weak spot: a release plays no sound of
  its own yet (the iris sound plays with the 0.5 s telegraph only).
- 2026-10-23 (user, round 53, closed): look **b** "bone chimneys" chosen (F.4; a, "coral knot", rejected and moved to `design/enemies/ground/concept/rejected/`; b in `assets/` since step E5; the part is `art: final`); the iris sound **a** and the razed sound **b** picked; **C.1 allow 1** (hard's REEFS asks for 3 of 4, built in step E3); C.3 (the fry ram at once) accepted.
