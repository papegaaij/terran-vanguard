---
title: Abyss Ray
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-22
---

# Abyss Ray

## Summary

A huge Vrell manta that rises out of the black water below the ship: first a growing shadow, then the ray itself, which sweeps a wide `fan` of shots across the field with four Barbfin escorts and glides off. The act's large set-piece unit (no mid-boss in Act 4).

## Design

### Stat block

**Draft** (Act 4 design phase, round 48; decided in round 48 (2026-10-22)). A hand-written table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the proposed Act 4 reference DPS (L22 96, then +4 a level to 120 at L28; part A sets it on the extended balance plan). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top, so the fire rates are tuned per level with `levelReport` in the build. The play plane under water is the `air` layer; `sub` is the deep water below it, `ground` the sea floor and the trench walls.

| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `sub` (the deep water below the play plane) while it is a shadow, flipping to `air` at the middle of the rise, back to `sub` when it dives away |
| Size tier | `large` |
| Size | wingspan 200x150 px, hitbox 130x90 (the body) |
| Parts | single (a vital body) |
| Orientation | `16 angles` (the glide direction) |
| HP | 210 (easy 158 / hard 273, from the global multipliers): 2.0 s at the L24 reference of 104 |
| Armour / shield | while a shadow only `anti-sub` (the harpoon torpedoes and depth charges) and the Smart Bomb reach it; afterwards none |
| Speed | rise 0.9 s, glide 70 px/s across the field |
| Movement | `dive` reversed: a dark shape grows 1.5 s below the ship's lane (the tell, no hazard), the manta then rises under it over 0.9 s, glides sideways 5 s to the far edge and sinks away; the escorts peel off at the rise |
| Attack | a 9-way `fan` (spread 110 degrees, 110 px/s base, `small` = 4) from its mouth, three fans 1.2 s apart at the top of the glide, then a rest; the fan is centred on the ship at the moment of firing |
| Formations | `carrier + escorts` (the Ray plus four [Barbfins](../barbfin/README.md) in `circle`) |
| Weak points | the lime gill slits along its leading edge (drawn only; the body is the vital part) |
| Effective traits | `anti-sub` (while a shadow), `spread`, `beam` |
| Credits | 60 (score 600 x chain) |
| Death | `large` break-up: the wings fold, a bioluminescent bloom, the body sinks into the dark |
| First level / used in | L24; L26 (a single Ray in the dark, its gill slits the only glow), L27 |
| Difficulty hooks | easy: 7-way fan, two fans; hard: 11-way fan, four fans |

### Behaviour

- **The rise**: from its entry the shadow is a darker patch in the water that grows over 1.5 s under the ship's lane, plus a low horn (the audio tell); the 3 s edge-warning rule does not apply (its own entry), but Varga's first-sight line does (the first Ray radio event). The Ray flips to `air` at the rise's midpoint and is hittable by everything from then.
- **Shadow hits**: while a shadow only `anti-sub` fire reaches it: a torpedo hit on a shadow does full damage, which makes the harpoon torpedoes a tool (pending the user's review that the shadow is hittable at all: the roster promises "grows from a shadow" and the water rules say nothing).
- **Fans**: three centred fans across the glide; the gap between two fans is the safe time. Readability: the fans are slow (x 0.7 under water) so lanes between bullets stay wide; the escorts add aimed shots the floor needs.
- **Leaving**: it glides off the side and dives (`air` to `sub`), leaving the escorts behind; if killed, the escorts are not removed.
- **One on the screen** at most, announced by Varga on its first sight; an Abyss Ray's whole visit is about 8 s.

### Difficulty and level use

- L24 first; the dark L26 reuses it with the gill glow as its only light (the level draft sets the Ray count: never more than one at a time).
- A Ray is a set piece of the act's pacing (large, once a minute), not a boss.

## Concept art

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): a/b Abyss Ray with its shadow. Files `abyss-ray-r48-a/b` in [naval/concept](../concept/), listed in the [naval](../README.md#concept-art) Concept art table. Notes: a faint at 1x (the dark body against the water); b the alternative.

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen)
- [ ] Behaviour, attack and tells per this spec, deterministic and allocation-free
- [ ] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
- [ ] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec
- [ ] The shadow tell and the layer flip with its hit rules (`sub` while a shadow)

## Open questions

- Is the shadow hittable by `anti-sub` fire? (Our reading: yes, it gives the gear a use.) The pending question is only for the build to confirm.

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (decided by me, gap analysis section 3.2): no mid-boss in Act 4; the Abyss Ray is the act's large set-piece unit.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
