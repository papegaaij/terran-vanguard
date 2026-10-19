---
title: Dust Devil
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/systems, ../../../campaign/act-3-red-dust]
updated: 2026-10-13
---

# Dust Devil

## Summary

A Vrell vortex organism: a five-vaned spinning funnel that wanders the Martian plains in a grit
swirl, pulling loose pickups and the ship gently toward it. Once per spin cycle it rises, its
vanes part and its lime core is exposed and hittable while it spits a spiral of grit (Level 18).

## Design

### Stat block

Hand-written draft numbers at **medium** (M19: no `data.yaml` until its level is built; see the
[balancing basis](../../README.md#balancing-basis)). HP is in damage units (a Pulse Cannon L1 shot
does 2). Set at its first level from the proposed Act 3 reference DPS (M18 = a, approved in round 36:
L18 = 88): the core's 52 HP is 0.6 s of focused fire, inside one 0.8 s window. Where it returns it
gets the act HP factor of a `medium` unit.

| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `ground` while closed; `low-air` while risen and open (the "ground → low-air" of the roster) |
| Size tier | `medium` |
| Size | 72×72 px (the concept), core hitbox 26×26 (open only); the grit swirl drawn ≈ 150 px across |
| Parts | single: the core is hittable only while open; the closed vanes are `armoured` |
| Orientation | `radial` (5-fold: 8 frames cover 72° of spin; an open frame set) |
| HP | 52 (easy 39 / hard 68, from the global multipliers) |
| Armour / shield | closed: shots spark off the vanes with a grey flash; open: none |
| Speed | 50 px/s along its wander (plus scroll) |
| Movement | `swirl` wander over the ground with `spin`; a 2.6 s cycle: 1.4 s closed on the ground, 0.4 s rise, 0.8 s open on `low-air`, sinks back at the start of the next cycle |
| Attack | while open: a 2-arm `spiral` of grit, a bullet per arm every 0.16 s (5 per arm per window), slow 110 px/s, `small` 4 |
| Pull | radial pull toward its centre: on the ship within 130 px, 35 px/s at the centre fading to 0 at the edge; on loose pickups within 170 px, 90 px/s (the inverse of the Magnet; with a fitted Magnet the stronger pull wins) |
| Formations | swarm (2–3 roaming), each on its own wander |
| Weak points | the lime core (open only) |
| Effective traits | burst damage in the window, `spread` (two or three at once), `homing` (keeps on the core) |
| Credits | 26 (score 260 × chain) |
| Death | the funnel collapses, a `medium` burst and a grit cloud that drops every pickup it held |
| First level | L18 (Dust Storm) |
| Difficulty hooks | easy: window 1.0 s, pull 25 px/s; hard: 3-arm spiral, pull 45 px/s |

### Behaviour

- **Wander.** A seeded `swirl` around an authored centre line, never closer than 90 px to the
  ship's centre (it slides round the ship rather than over it); it scrolls with the ground and
  leaves at the bottom.
- **Spin cycle.** Closed it spins fast inside its swirl; every **2.6 s** it rises (0.4 s, the
  tell: the swirl tightens and brightens, the spin speeds up), the vanes part and the **lime core
  is exposed for 0.8 s** on `low-air` (no `anti-ground` bonus, all weapons hit, no contact), then
  closes and sinks. Shots on the closed vanes spark off grey, so the player learns the window.
- **Grit spiral.** Only while open: two arms rotating 180°/s. The risk of attacking the open core
  is standing in front of the spiral.
- **Pull.** Within 130 px it draws the ship toward its centre (at most 35 px/s against the ship's
  250–315 px/s: a drift you feel, never a trap) and pickups within 170 px at 90 px/s. Pickups it
  holds circle in its swirl and are released at its death (or carried off the bottom if it
  escapes). The ship never touches it (ground and low-air never collide).
- **In the dust storm** (Level 18, M6 = a): its lime core glows through the haze when open; closed
  it is a darker whirl in the dust.
- **Code fit.** `spin` and `spiral` exist; the radial pull is the Magnet inverted plus a `Wind`-like
  push on the ship; the open window is a periodic timer like the Driftjelly's submerge (§2.8 of the
  gap analysis). Distinct from Mars's dust-devil hazards (wind columns in the level's weather).

### Difficulty and level use

- Window, spiral arms and pull per the stat block; the global multipliers apply.
- At most three on screen; their cycles are offset (0.8 s apart) so the windows alternate.

## Concept art

Chosen concept from [round 06](../../../concept-rounds/round-06/README.md) (listed in the
[ground](../README.md#concept-art) Concept art table): the teal-black five-vaned funnel with its
lime core, spinning in an ochre grit swirl and pulling in pickups. Production art follows the
act's art track in its level's round.

| File | What | Status |
|---|---|---|
| [../concept/dust-devil-r06-a.png](../concept/dust-devil-r06-a.png) | Dust Devil — Vrell vortex organism (teal-black vanes, lime core) spinning in a grit swirl, pulling in pickups (sheet) | chosen |
| [../concept/dust-devil-r06-a.gif](../concept/dust-devil-r06-a.gif) | Dust Devil — the same in motion | chosen |

## Implementation

- [ ] Seeded `swirl` wander with `spin`, kept ≥ 90 px from the ship
- [ ] 2.6 s cycle: closed on `ground` (armoured vanes), 0.4 s rise tell, 0.8 s open on `low-air` with the core hittable
- [ ] Grit spiral while open (2 arms, hard 3)
- [ ] Radial pull on the ship (130 px, 35 px/s) and on pickups (170 px, 90 px/s); held pickups dropped at death
- [ ] Death, bounty and score
- [ ] Data file with the stat block (when Level 18 is built)
- [ ] Production art (round of part E)

## Open questions

- None open: round 36 answered it (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the [ground roster](../README.md) to a draft
  spec with the round-06 concept as its chosen art. M18 = a taken as the recommended option,
  pending the user's review in round 36. Our proposals: 52 HP; the roster's "ground → low-air"
  read as the cycle's rise (closed on `ground`, open on `low-air`); the 2.6 s cycle with a 0.8 s
  window; the spiral only while open; the pull numbers.
- 2026-10-13 (user, round 36): approved (D.13). Q.22e yes: a Smart Bomb or Airstrike hits it while closed (blasts ignore the vanes' armour).
