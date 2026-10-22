---
title: Depth Hunter
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-22
---

# Depth Hunter

## Summary

A grey human-built submarine drone, unmarked and unexplained, that rises in columns from below and fires slow, shootable `homing` torpedoes up the screen. **Hint 3**: its hull code is found under the paint on a wreck (Helix Dynamics).

## Design

### Stat block

**Draft** (Act 4 design phase, round 48; decided in round 48 (2026-10-22)). A hand-written table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the proposed Act 4 reference DPS (L22 96, then +4 a level to 120 at L28; part A sets it on the extended balance plan). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top, so the fire rates are tuned per level with `levelReport` in the build. The play plane under water is the `air` layer; `sub` is the deep water below it, `ground` the sea floor and the trench walls.

| Field | Value |
|---|---|
| Faction | Unmarked (Helix Dynamics); a **machine** (`machine: true`, stunned 3 s by the EMP Burst) |
| Layer | `air` (the play plane under water) |
| Size tier | `medium` |
| Size | 72x40 px, hitbox 56x28 |
| Parts | single |
| Orientation | `16 angles` (the torpedoes are launched along its facing, up) |
| HP | 120 (easy 90 / hard 156, from the global multipliers): 1.0 s at the L27 reference of 116; torpedo 3 |
| Armour / shield | none |
| Speed | 80 px/s entering, holds in the lower band |
| Movement | `column` from the bottom edge (`from: rear`, a 3 s edge warning): 3-4 hunters in a line, hold between 380 and 460 px from the top, drift 12 px/s sideways, leave downward after 10 s |
| Attack | a `homing` torpedo every 3.5 s straight up, 90 px/s base, turn rate 70 degrees/s, 3 HP, `heavy` = 10 on impact, life 5 s; the Decoy Flares retarget a torpedo onto the flare; the torpedo is shot down with the ship's weapons |
| Formations | `column` (3-4, entering from below) |
| Weak points | none (a plain grey hull; the thruster glow is drawn only) |
| Effective traits | `spread`, `homing`, `rear`, `anti-sub` |
| Credits | 30 (score 300 x chain); torpedoes 0 |
| Death | `medium` hull break, grey debris and an oil bloom; leaves a wreck with a hull code on the first one only |
| First level / used in | L26 as unseen torpedo trails at the end (Rook's shoot-down, no unit on the screen), first seen at L27; L28 as escort |
| Difficulty hooks | easy: torpedo every 4.5 s, turn rate 55; hard: every 2.8 s, 85 degrees/s |

### Behaviour

- **Grey, no marks** (Q11 a): the Depth Hunters are unmarked grey like Act 3's machines, no black and gold; the radio and the L27 briefing image of the hull code under the paint are the hint; the beacon colours stay red-gold until the reveal.
- **The torpedo**: slow and shootable like the Revenant Walker's missile and homing pods: the ship's weapons or Rook shoot it down in 1-3 hits; the Decoy Flares retarget it (an Act 4 special, part A).
- **Lower band**: the hunters hold below the ship's usual lane and fire upward, so the player must fly to the top and weave, a play-style change from Act 3's top-down fire; the column's entry is warned (the bottom edge).
- **Under water**: the torpedoes carry the x 0.7 and the Hunter's drag-free movement; a ship slowed by the Siren is the L27 danger the level avoids by not mixing the two.
- **Rook's loss** (L26 end): the unseen torpedo trails come from this unit but no Hunter is drawn (the level doc owns it).

### Difficulty and level use

- L27 first on the screen; L26's end has trails only, no unit.
- Never more than one column on the screen.

## Concept art

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): a grey hull with a thruster glow. Files `depth-hunter-r48-a/b` in [naval/concept](../concept/), listed in the [naval](../README.md#concept-art) Concept art table. Notes: the torpedoes reuse the walker missile art (a stand-in).

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen)
- [ ] Behaviour, attack and tells per this spec, deterministic and allocation-free
- [ ] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
- [ ] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec
- [ ] The homing torpedo as a shootable projectile with retargeting (the Decoy Flares), launched from the bottom edge

## Open questions

- How a wreck's hull code is shown (briefing image and radio only): the L27 level doc.

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (recommended option, decided in round 48 (2026-10-22), Q11 a): grey until L28; the black and gold look is kept for the cruisers; rejected: b (black-and-gold trim spoils the reveal).
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
