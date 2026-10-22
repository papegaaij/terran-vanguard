---
title: Glow Angler
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-22
---

# Glow Angler

## Summary

A small Vrell ambusher of the black water: it hangs a teal lure on a stalk that glows like something worth picking up and, when the ship comes close or touches the lure, lunges and bursts a `ring` of shots.

## Design

### Stat block

**Draft** (Act 4 design phase, round 48; decided in round 48 (2026-10-22)). A hand-written table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the proposed Act 4 reference DPS (L22 96, then +4 a level to 120 at L28; part A sets it on the extended balance plan). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top, so the fire rates are tuned per level with `levelReport` in the build. The play plane under water is the `air` layer; `sub` is the deep water below it, `ground` the sea floor and the trench walls.

| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (the play plane under water) |
| Size tier | `small` |
| Size | 40x32 px body, a stalk of 36 px and a 12 px lure, hitbox 30x24 (the body) |
| Parts | single (the lure is drawn, not a part) |
| Orientation | `16 angles` toward the ship during a lunge |
| HP | 14 (easy 11 / hard 18, from the global multipliers); the beam-era factor x 2 applies by level number: 28 at L26 (0.25 s at the L26 reference of 112) |
| Armour / shield | none |
| Speed | drift 20 px/s; lunge 220 px/s |
| Movement | `drift` hanging in the dark; when the ship is within 130 px, or touches the lure, it `dive`s at the ship for 0.6 s, stops, fires and retreats at 100 px/s |
| Attack | one `ring` of 8 (100 px/s base, `small` = 4) at the end of the lunge; cooldown 5 s; the lure itself does no damage |
| Formations | `field` (4-8, scattered over a dark area), a few `scattered` singletons |
| Weak points | the lure and the glowing eye (drawn only) |
| Effective traits | `spread`, `homing` |
| Credits | 14 (score 140 x chain) |
| Death | `small` flash that blows out the lure; the stalk stays lit for 1 s then goes dark |
| First level / used in | L26; L27 |
| Difficulty hooks | easy: trigger 100 px, ring of 6; hard: trigger 160 px, ring of 10 |

### Behaviour

- **The lure** (Q6 a): a near-miss pickup look: a pulsing teal bead on a visible stalk (a pickup's own colour is never used), drawn in the black as its only visible part; Varga warns at the first sighting ("*that is not a pickup*"); touching the lure triggers the lunge but does no damage.
- **Lunge**: the dive ends 60 px short of the ship's position when it fires, so the ring spreads from a point the ship can leave; contact `small` = 10 only if the ship flies into the body.
- **In the dark** (L26): outside the headlight cone units show only their glow: the lure and the eye are lit; shots are always bright; everything is hittable (round 48 Q4 a). The Sonar Pulse lights every unit and lure for 6 s.
- **Under water**: the ring's bullets carry the x 0.7.

### Difficulty and level use

- L26 is the Angler's level, a field scattered over the black; L27's vent glow makes them a lighter threat.
- Beam-era HP by level number (`small`).

## Concept art

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): the Angler with its lure in the dark. Files `glow-angler-r48-a/b` in [naval/concept](../concept/), listed in the [naval](../README.md#concept-art) Concept art table. Notes: b looks rocket-like.

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen)
- [ ] Behaviour, attack and tells per this spec, deterministic and allocation-free
- [ ] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
- [ ] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec
- [ ] The lure sprite, the near-miss trigger (130 px or touch) and the dark's glow-only render

## Open questions

- Does the first-sight radio line need its own event (like the Ray's)? Level draft.

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (recommended option, decided in round 48 (2026-10-22), Q6 a): a near-miss lure, teal with a visible stalk; rejected: b (looks exactly like a pickup) and c (no mimicry).
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
