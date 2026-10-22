---
title: Spiral Nautilus
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-22
---

# Spiral Nautilus

## Summary

A coiled-shell cephalopod that rolls in an armoured ivory shell along `spiral-in` paths round the ship, then uncoils its violet tentacle crown and fires a `spiral` of shots. Pairs orbit in opposite directions; shoot it while uncoiled.

## Design

### Stat block

**Draft** (Act 4 design phase, round 48; decided in round 48 (2026-10-22)). A hand-written table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the proposed Act 4 reference DPS (L22 96, then +4 a level to 120 at L28; part A sets it on the extended balance plan). Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top, so the fire rates are tuned per level with `levelReport` in the build. The play plane under water is the `air` layer; `sub` is the deep water below it, `ground` the sea floor and the trench walls.

| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (the play plane under water) |
| Size tier | `medium` |
| Size | 56x56 px shell, hitbox 40x40 (the crown 64x64 uncoiled) |
| Parts | single (shell closed, crown open) |
| Orientation | `radial` shell spin (24 frames), the crown at `16 angles` toward the ship |
| HP | 100 (easy 75 / hard 130, from the global multipliers): 0.93 s at the L25 reference of 108, counted while uncoiled |
| Armour / shield | closed: shots spark off the shell with a grey flash, a blast (Smart Bomb) and the harpoon torpedoes excepted (pending the weapon's README); uncoiled: none |
| Speed | 150 px/s on its spiral, 60 px/s while uncoiled |
| Movement | `spiral-in` round the ship: radius 220 px closing to 90 px over 3 s closed (never closer than 90), `spin` 24 frames; uncoils for 2.2 s (0.6 s opening, 1.6 s open, the weak crown), then closes and breaks outward; pairs orbit in opposite directions with their cycles offset by 2 s |
| Attack | `spiral` when open: 3 arms, rotating 90 degrees/s, `small` = 4, 100 px/s base, a shot every 0.1 s over 1.6 s; the crown glows violet 0.4 s before it fires |
| Formations | `circle` (pairs orbiting in opposite directions) |
| Weak points | the violet crown and eyes (open only, glowing lime-white) |
| Effective traits | `homing`, `spread`, `anti-sub` (cracks the shell) |
| Credits | 30 (score 300 x chain) |
| Death | `medium` flash, the shell cracks into two halves that sink, the crown goes dark |
| First level / used in | L25; L26 (the dark: its crown is the glow), L27 |
| Difficulty hooks | easy: 2 arms, a 1.8 s open window; hard: 4 arms, 1.4 s window |

### Behaviour

- **Chosen concept**: round 06's shell and crown (see below) is the look; Europa's lighting (the player's headlight cone and its own bioluminescence) is applied in the in-game grade.
- **Orbit**: the pair spirals in from the sides at 220 px radius, one clockwise and one counter-clockwise; the spiral-in is a pursuit of the ship's position as the centre, the 90 px floor is a rule so that it never collides with the ship (contact `medium` = 15 only if the player flies into the shell).
- **Window**: the closed shell is the armoured state, as the [Dust Devil](../../ground/dust-devil/README.md)'s vanes: it opens at the end of each pass and fires; with the offset, one of the pair is always closed, one open about 60 % of the cycle.
- **Shot**: the spiral arms rotate, the gaps between the arms rotate as well, so the player threads the gap by moving with the spiral; bullets x 0.7 make them wide and slow.
- **Under water**: the shell spark effect is a ring of lime-grey flecks; the spiral reads against the black.

### Difficulty and level use

- L25 first (with the Sirens: a slowed ship on a spiral's gap is the act's hardest moment), L26 and L27 reuse it.
- Pairs only; a lone Nautilus is a hard-mode variation.

## Concept art

Chosen round 06 concept: [spiral-nautilus-r06-a.png](../concept/spiral-nautilus-r06-a.png), [spiral-nautilus-r06-a.gif](../concept/spiral-nautilus-r06-a.gif) (listed in the [naval](../README.md#concept-art) Concept art table). It goes straight to production in the Act 4 part that flies it (24 shell frames, the crown at 16 headings, the open and close steps, the death and an intel portrait).

## Implementation

- [ ] Stat block loaded from a `data.yaml`; global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen)
- [ ] Behaviour, attack and tells per this spec, deterministic and allocation-free
- [ ] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait
- [ ] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec
- [ ] The orbit round the ship (`spiral-in` with the 90 px floor), the armoured closed state and the uncoil window

## Open questions

- Does the harpoon torpedo crack the closed shell (our reading) or only the Smart Bomb? Decided with the weapon's README.

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-06 (round 06): the shell, crown and stripes chosen (Spiral Nautilus r06-a).
- - 2026-10-21: drafted at L25; promoted from the roster fork addition.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
