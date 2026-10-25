---
title: Shellback
design: approved
implementation: done
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../polyp-mortar, ../../../allies, ../../../campaign/act-3-red-dust]
updated: 2026-10-19
---

# Shellback

## Summary

A lumbering four-legged Vrell beast, tortoise and armadillo in one, with overlapping slate shell
plates and a spore-mortar vent on its back. It walks slowly toward its target, lobbing marked
spore blobs; badly hurt it curls into an armoured ball and rolls, crushing what lies in its way,
then uncurls with its belly exposed. Animal, not insect (Level 17).

## Design

### Stat block

Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)). The numbers live
in [data.yaml](data.yaml) (M7 part D, step D2), with the body and vent as parts, the new `roll` block
and its hook of the [schemas](../../../tech/architecture/schemas-units/README.md); the table is
rendered from it. HP is in damage units (a Pulse Cannon L1 shot
does 2). Set at its first level from the proposed Act 3 reference DPS (M18 = a, approved in round 36:
L17 = 84) and made weaker in round 40 (K.a: "The shell creatures seem too strong. None are
destroyed."): the body's 90 HP is 1.1 s and the vent's 16 0.2 s; with the curl, the roll and the
uncurl armoured, a focused kill takes 2–3 s. As a `large` unit it gets the act HP factor wherever
it returns.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `ground` |
| Size tier | `large` |
| Size | 112×112 px, hitbox 84×84 (the concept); vent hitbox 28×28 at the rear of the back; rolled ball 72×72 |
| Parts | body (`vital`), mortar vent (`destroyable`: kills the mortar) |
| Orientation | `16 angles` (16 headings × 6 heavy-walk frames; curl and uncurl frames; the ball rolls with 8 frames per band pair at 16 headings) |
| HP | body 90 · vent 16 (round 40: from 120 · 24); 106 (easy 80 / hard 138, from the global multipliers) |
| Armour / shield | none while walking; fully `armoured` while curled and rolling (shots spark off, grey flash); the body takes ×1.5 for 1.5 s while it uncurls (the belly) |
| Speed | 24 px/s walk (plus scroll); roll 140 px/s |
| Movement | `walk` along an authored ground path, turning at 45°/s; once, at ≤ 40 % body HP, it curls (0.4 s), rolls 2.5 s along its path (`spin`), uncurls (0.4 s) and walks on |
| Attack | `mortar` from the vent every 4 s: the vent swells 0.5 s (tell), the impact marked on the ground 1.2 s ahead at the ship's position, a direct hit `heavy` 10, then a `ring` of 8 (slow 110 px/s, `small` 4); none while curled |
| Formations | single, convoy (a pair: a `convoy` of 2 walking 3 s apart, so their rolls never overlap) |
| Weak points | the lime vent (a destroyable part), the belly while it uncurls (×1.5) |
| Effective traits | `anti-ground`, `area` |
| Credits | 52 (score 520 × chain); whole (body first) 48; parts: body 40, vent 12 (52) |
| Death | `large` organic burst, shell plates scatter; the cracked shell stays as a husk |
| First level / used in | L17; introduced in Level 17 (Dome Siege); the act HP factor of a `large` unit where it returns |
| Difficulty hooks | easy: mortar every 5.0 s, ring of 6; hard: every 3.2 s, ring of 10, the roll 3.0 s |
<!-- /data -->

### Behaviour

- **Walk.** Heavy diagonal-pair gait, the walk phase advancing with distance; it faces where it
  walks. In Level 17 its path leads toward the Ares Landing domes (the level's target-the-objective
  hook decides whether its mortar marks the domes).
- **Mortar.** The [Polyp Mortar](../polyp-mortar/README.md)'s lob from the vent: the vent swells
  for 0.5 s (tell), the blob arcs, the impact is marked on the ground 1.2 s ahead (≥ 1 s rule),
  a direct hit is `heavy` 10, the burst a ring of 8. Destroying the vent stops the mortar for good
  and pays its part bounty; the body walks on. The vent on its back is seen from above: a shot over
  the shell whose line of flight crosses the vent's box (a beam whose column crosses it) hits the
  vent first, from any side (round 40 R.a).
- **Curl and roll** (once). When the body falls to 40 % HP it tucks head and legs in (0.4 s,
  already armoured), rolls **2.5 s** along its path at 140 px/s, then uncurls (0.4 s; both 0.6 s
  until round 40). While curled every shot sparks off with a grey flash, so the player learns to
  wait. Near the bottom it rolls a shorter way (round 40 R.e, `belly_row` 200 px): drifting down
  the screen, the roll ends where the scroll would carry it below that row before its uncurl ends,
  so its belly shows at the row or higher. The roll **crushes ground targets in its path** (our
  reading of the roster, for review): ally structures (the domes in L17, per the ally's spec: one
  hit per roll where the ball touches the dome's footprint, its ring; round 40 R.c), destructible
  scenery and ground crates (a crate's drop still falls); never other enemies, never the ship (it
  stays on `ground`).
- **Belly window.** For 1.5 s after it uncurls its pale belly shows and the body takes ×1.5,
  marked by the lime glow along the belly seam.
- **Code fit.** `walk` and the mortar exist; the curl-roll (a timed state on the path, an
  armoured flag, crushing ground objects: an enemy hurting ground objects is new) is the new part
  (§2.8 of the gap analysis).

### Difficulty and level use

- Mortar interval, ring and roll length per the stat block; the global multipliers apply.
- A pair walks 3 s apart so their rolls never overlap.

## Concept art

Chosen concept from [round 05](../../../concept-rounds/round-05/README.md) (listed in the
[ground](../README.md#concept-art) Concept art table): the slate/lime beast with its 16-heading
heavy walk, the mortar and the curl, roll and uncurl on a Mars plateau. Production art follows the
act's art track in its level's round.

| File | What | Status |
|---|---|---|
| [../concept/shellback-r05-a.png](../concept/shellback-r05-a.png) | Shellback — armoured Vrell beast (slate/lime), 112 px: 16 headings, 6-frame heavy walk, curl sequence (sheet) | chosen |
| [../concept/shellback-r05-a.gif](../concept/shellback-r05-a.gif) | Shellback — lobs spore blobs with marked impacts, curls into a ball and rolls over boulders on a Mars plateau, uncurls (motion) | chosen |

## Implementation

- [x] Body and vent parts with their HP; the vent's loss stops the mortar (`ShellbackTest`; round 40 R.a: a shot whose line crosses the vent hits it first, from any side, so a shot from below reaches the rear vent)
- [x] `walk` with the heavy gait; mortar every 4.0 s with a 0.5 s vent swell and a 1.2 s impact marker (`ShellbackTest`, `ShellbackLooksTest`)
- [x] Curl-roll once at ≤ 40 %: armoured while curled, 2.5 s roll at 140 px/s along its path, uncurl (`ShellbackTest`)
- [x] Round 40 (step D6): weaker (body 90, vent 16, curl and uncurl 0.4 s), the shorter roll near the bottom
  (`belly_row`), the crush where the ball touches a dome's footprint (`ShellbackTest`, `RollCrushTest`, `Level17Test`)
- [x] Crushing: ally structures (one `heavy` hit per roll), destructible scenery and crates in its path (`RollCrushTest`)
- [x] Belly window: ×1.5 for 1.5 s after the uncurl (`ShellbackTest`)
- [x] Death, husk, part and whole bounties (`ShellbackTest`, `ShellbackLooksTest`)
- [x] Data file with the stat block (when Level 17 is built; M7 part D, step D2)
- [x] Production art (round of part D): step ART-D, proposed in round 40 (`shellback-final-r40-a` in [ground](../README.md))

## Open questions

- None open: round 36 answered it (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the [ground roster](../README.md) to a draft
  spec with the round-05 concept as its chosen art. M18 = a taken as the recommended option,
  accepted in round 36 (2026-10-13). Our proposals: body 120 and vent 24 as parts, the curl
  once at 40 %, armoured while rolling, crushing ally structures and scenery but not enemies,
  the ×1.5 belly window, bounty 48 whole / 52 in parts.
- 2026-10-13 (user, round 36): approved (D.12). Q.22d yes: the roll follows its authored path, it does not steer toward the domes. Its hooked mortar and roll hurt Level 17's domes (Q.17a).
- 2026-10-18 (M7 part D, step D2; accepted in round 40 (2026-10-19): the stat block moved into
  [data.yaml](data.yaml): the body (`vital`, 120 HP, 40 with a `first_bonus` of 8: a body-first kill pays the
  whole 48) and the vent (`destroyable`, 24 HP, 12, `attack: mortar`: its loss silences the mortar) as a
  `part_list` on a unit that is not a boss; the new `roll` block (`below` 0.4, `curl` 0.6 s, 2.5 s at 140 px/s,
  `uncurl` 0.6 s, the belly ×1.5 for 1.5 s, the ball 72×72) and its hard hook; the pair a `convoy` of 2 walking
  3 s apart. Defaults of the gap check: armoured against every shot and blast while curled, an EMP Burst
  pausing the roll.
- 2026-10-18 (M7 part D, step D4): the vent's offset moved from 30 to 15 px towards the tail, where the production
  sprite draws it (`assets/pivots/shellback.json`: 14.6 px at every heading). Either way its 28 px box lies inside
  the body's 84 px box at the rear of the back, so a shot from below meets the body first and no forward shot reaches
  the vent while the Shellback walks down the screen (Level 17's autopilot shot none); accepted in round 40 (2026-10-19) (the part's hit test, or the vent's place).
- 2026-10-19 (user, round 40, F.c): the final art approved (`art: final`); R.a = a: a shot inside the vent's box hits the vent first, from any side (queued, Level 17's Implementation); R.e: a Shellback shot near the bottom rolls a shorter way (queued).
- 2026-10-19 (step D6, round 40 K.a, R.a, R.c, R.e; accepted in round 40 (2026-10-19)): weaker so a careful pilot destroys
  it: body 120 → 90, vent 24 → 16, curl and uncurl 0.6 → 0.4 s (Level 17's autopilot kills 37 of 40 at medium over
  8 seeds, was 8); a shot whose line crosses the vent hits the vent first, from any side; the roll's new `belly_row`
  (200 px above the bottom edge) ends a roll drifting down the screen early so its belly shows; the roll crushes a
  dome where the ball touches its footprint (the ring), and its walks pass between the domes (Level 17's paths).
