---
title: Abyss Ray
design: approved
implementation: in-progress
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-23
---

# Abyss Ray

## Summary

A huge Vrell manta that rises out of the black water below the ship: first a growing shadow, then the ray itself, which sweeps a wide `fan` of shots across the field with four Barbfin escorts and glides off. The act's large set-piece unit (no mid-boss in Act 4).

## Design

### Stat block

Approved in round 48 (2026-10-22). Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)), from its [data.yaml](data.yaml) (M8 part D, step D2; the table is rendered from it); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the Act 4 reference DPS (L24 104). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top. The play plane under water is the `air` layer; `sub` is the deep water below it, `ground` the sea floor and the trench walls. The data: a `rise` movement (the shadow 1.5 s, the rise 0.9 s, the glide 5 s at 70 px/s, the sink 0.9 s; the shadow about 200 px ahead of the ship in its lane, gap check `m8d-gaps.md` §5 D2) and a fan in a burst of three 1.2 s apart, one salvo a visit.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `sub` (the deep water below the play plane) while it is a shadow, flipping to `air` at the middle of the rise, back to `sub` at the middle of the sink |
| Size tier | `large` |
| Size | 200×150 px, hitbox 130×90 |
| Parts | single |
| Orientation | `16 angles` |
| HP | 210 (easy 158 / hard 273, from the global multipliers) |
| Armour / shield | none; while a shadow only `anti-sub` (the harpoon torpedoes and depth charges) and the Smart Bomb reach it |
| Speed | 70 px/s (the glide); the rise 0.9 s |
| Movement | `rise`: a dark shape grows 1.5 s in the ship's lane about 200 px ahead of the ship (the tell, no hazard, no contact), the manta rises under it over 0.9 s (`sub` to `air` at the middle, no contact until the rise ends), glides 5 s at 70 px/s toward the far side and sinks away over 0.9 s (escaped); the escorts peel off at the rise |
| Attack | a 9-way `fan` (spread 110°, 110 px/s, `small` = 4) from its mouth, centred on the ship at the moment of firing: 3 fans 1.2 s apart from the rise's end, then a rest (one salvo a visit) |
| Formations | carrier + escorts (1) (the Ray plus four [Barbfins](../barbfin/README.md) in `circle`, held until its rise) |
| Weak points | lime gill slits along its leading edge (drawn only; the body is the vital part) |
| Effective traits | `anti-sub`, `spread`, `beam` |
| Credits | 60 (score 600 × chain) |
| Death | `large` break-up: the wings fold, a bioluminescent bloom, the body sinks into the dark |
| First level / used in | L24; L26 (a single Ray in the dark, its gill slits the only glow), L27 |
| Difficulty hooks | easy: 7-way fan, two fans; hard: 11-way fan, four fans |
<!-- /data -->

### Behaviour

- **The rise**: from its entry the shadow is a darker patch in the water that grows over 1.5 s in the ship's lane, about 200 px ahead of the ship (its x at the entry, clamped; not on it, gap check `m8d-gaps.md` §5 D2), plus a low horn (the audio tell); the 3 s edge-warning rule does not apply (its own entry), but Varga's first-sight line does (the first Ray radio event). The Ray flips to `air` at the rise's midpoint and is hittable by everything from then; it does no contact damage until the rise ends (the shadow is a tell, not a hazard).
- **Shadow hits**: while a shadow only `anti-sub` fire reaches it: a torpedo hit on a shadow does full damage, which makes the harpoon torpedoes a tool (approved in round 48; the Smart Bomb reaches it too).
- **Fans**: three centred fans across the glide; the gap between two fans is the safe time. Readability: the fans are slow (x 0.7 under water) so lanes between bullets stay wide; the escorts add aimed shots the floor needs.
- **Escorts**: its `carrier + escorts` wave's four Barbfins stay unspawned until the rise's midpoint and fan out from it to their holds (gap check D3); they keep their own cadence after the Ray leaves or dies.
- **Leaving**: it glides off the side and dives (`air` to `sub`), leaving the escorts behind; if killed, the escorts are not removed.
- **One on the screen** at most, announced by Varga on its first sight; an Abyss Ray's whole visit is about 8 s.

### Difficulty and level use

- L24 first; the dark L26 reuses it with the gill glow as its only light (the level draft sets the Ray count: never more than one at a time).
- A Ray is a set piece of the act's pacing (large, once a minute), not a boss.

## Concept art

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): a/b Abyss Ray with its shadow. Files `abyss-ray-r48-a/b` in [naval/concept](../concept/), listed in the [naval](../README.md#concept-art) Concept art table. Notes: a faint at 1x (the dark body against the water); b the alternative.
The production frames (round 48's b, the bone skate; `tools/art/abyss_ray.py`, step ART-D) are `abyss-ray-final-r53-a` with the game captures `abyss-ray-capture-r53-a` and `ray-shadow-hit-capture-r53-a`, in the same table; proposed in [round 53](../../../concept-rounds/round-53/README.md).

## Implementation

- [x] Stat block loaded from a `data.yaml` (step D2: loaded and checked, the `rise` movement and the fan's burst); global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen; step D3: 210 kept, the plan's Harpoon Torpedoes reach its shadow)
- [x] Behaviour, attack and tells per this spec, deterministic and allocation-free (steps D1 and D3: `AbyssRayTest`, `Level24Test`)
- [x] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
      (step ART-D: `tools/art/abyss_ray.py`, round 53; the looks: `Level24LooksTest.theRaysPhasesStepThroughTheirFrames`,
      `theFirstRayRisesGlidesAndSinks`; the portrait: `PortraitsTest.level24sNewUnitsHaveTheirSensorPortraits`)
- [x] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec (step D4: the under-water `large` burst,
      `Level24SoundsTest.theRayAndTheReefDieWithTheirOwnSounds`; the rise, glide, fan and death a/b in round 53; the drops:
      `Level24Test.theRaysCarryTheOverdriveAndTheShieldCell`)
- [x] The shadow tell and the layer flip with its hit rules (`sub` while a shadow) (step D1: `AbyssRayTest.itsShadowIsHitByTheHarpoonAndNotByThePulseUntilItsLayerFlips`,
      `itsShadowIsHitByADepthCharge`, `itTouchesNothingBeforeItsGlideButItsBodyHitsInTheGlide`)

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (decided by me, gap analysis section 3.2): no mid-boss in Act 4; the Abyss Ray is the act's large set-piece unit.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-22 (M8 part D, step D2; gap check `m8d-gaps.md` §5 D2, D3, accepted in round 53 (2026-10-23)): the stat block as
  data, the table rendered from it. The open question closed (the shadow is hittable by `anti-sub` fire and the Smart
  Bomb: approved in round 48). The shadow grows in the ship's lane about 200 px ahead of the ship, not on it, and the
  Ray does no contact damage from the layer flip until the rise ends (a 130 × 90 body flipping to `air` on the ship
  would be an unavoidable hit); the wave's time is the shadow's start (Level 24's `first-ray` line at that moment). The
  escorts are held until the rise's midpoint and fan out from it. The fan: a `burst` of 3 (easy 2, hard 4) 1.2 s apart
  from the rise's end and an `interval` of 20 s (longer than the 8.3 s visit: one salvo a visit); a rising unit's fan
  may come in a burst (as a boss's).
- 2026-10-22 (M8 part D, step D3, accepted in round 53 (2026-10-23)): mapped and flown in Level 24 (`AbyssRules`):
  the rise, the fan in its burst (only a rising unit's fan takes the `burst`; every other fan keeps one volley), the
  first-ray line on the simulation's first shadow. Numbers unchanged: the autopilot holds its line under the shadow
  with the Harpoon Torpedoes (`AbyssPlay`), the fans and the escorts are dodged as any bullets; never two Rays on the
  screen (`Level24Test`, hard).
- 2026-10-22 (M8 part D, steps D1, ART-D and D4; accepted in round 53 (2026-10-23)): step D1's own: the fans fire
  only while the Ray's centre is on the field (on hard a glide may carry it off before its fourth fan), a Ray leaving
  the field is gone, its escorts' holds spread evenly across ± the hold radius. Step ART-D: the final frames from round
  48's b (16 glide headings × 3 wing beats, the rise, the sink, the shadow growing, the gill and spot glow, the mouth
  flash, the break-up; the atlas about 208 × 120 px a frame). Step D4 (`AbyssRayLooks`): the glow drawn ungraded over
  every beat frame, the shadow's last frame under the rise fading with it, the sink to the side by heading 4 or 12, the
  death at the nearest of headings 0/4/12 with the under-water `large` burst. Weak spots shown in the round: the wing
  beat is faint at play size, the body reads dark under the reef field's 0.32 grade, and a Ray rising mid-lane glides
  off the right edge before it sinks (a call in round 53).
- 2026-10-23 (user, round 53, closed): the final frames accepted (F.2; the part is `art: final`); the rise **b**, glide **a**, fan **b** and death **a** sounds picked; C.4 (the glide off the edge) and C.5 (the fans only on the field) accepted.
