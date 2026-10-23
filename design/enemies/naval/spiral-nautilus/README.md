---
title: Spiral Nautilus
design: approved
implementation: in-progress
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-23
---

# Spiral Nautilus

## Summary

A coiled-shell cephalopod that rolls in an armoured ivory shell along `spiral-in` paths round the ship, then uncoils its violet tentacle crown and fires a `spiral` of shots. Pairs orbit in opposite directions; shoot it while uncoiled.

## Design

### Stat block

Approved in round 48 (2026-10-22). Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)), from its [data.yaml](data.yaml) (M8 part E, step E3; the table is rendered from it); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the Act 4 reference DPS (L25 108): 0.93 s, counted while uncoiled. Bullet speeds are base values: the level's water factor (bullets x 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top. The play plane under water is the `air` layer. The data: its `coil` movement (the orbit, the uncoil, the offset of a pair), the `shell` armour of the closed coil and its `spiral` fired `while: open` (the Dust Devil's rule).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (the play plane under water) |
| Size tier | `medium` |
| Size | 56×56 px, hitbox 40×40 (the shell; the crown 64×64 uncoiled) |
| Parts | single (shell closed, crown open) |
| Orientation | `radial` shell spin (24 frames), the crown at `16 angles` toward the ship |
| HP | 100 (easy 75 / hard 130, from the global multipliers) |
| Armour / shield | closed (`shell`): shots spark off with a grey flash, the specials' blasts (Smart Bomb, Airstrike) and `anti-sub` fire (Harpoon Torpedoes, Torpedo Pods, Depth Charges) excepted; uncoiled: none |
| Speed | 150 px/s on its spiral, 60 px/s while uncoiled |
| Movement | `spiral-in` round the ship: radius 220 px closing to 90 px over 3 s closed (never closer than 90), `spin` 24 frames; uncoils for 2.2 s (0.6 s opening, 1.6 s open, the weak crown), then closes and breaks outward; pairs orbit in opposite directions with their cycles offset by 2 s; in a trench its orbit's centre is clamped so its circle fits the flyable width; it leaves after its wave's hold |
| Attack | `spiral` while open: 3 arms rotating 90°/s, `small` = 4, 100 px/s base, a shot per arm every 0.1 s over the 1.6 s window; the crown glows violet 0.4 s before it fires |
| Formations | circle (2) (pairs orbiting in opposite directions) |
| Weak points | the violet crown and eyes (open only, glowing lime-white) |
| Effective traits | `homing`, `spread`, `anti-sub` |
| Credits | 30 (score 300 × chain) |
| Death | `medium` flash, the shell cracks into two halves that sink, the crown goes dark |
| First level / used in | L25; L26 (the dark: its crown is the glow), L27 |
| Difficulty hooks | easy: 2 arms, a 1.8 s open window; hard: 4 arms, 1.4 s window |
<!-- /data -->

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

The production frames (round 06's a; `tools/art/spiral_nautilus.py`, step ART-E) are `spiral-nautilus-final-r54-a` with the game capture `spiral-nautilus-capture-r54-a` (the first pair opening and firing, with a video), in the [naval](../README.md#concept-art) Concept art table, for [round 54](../../../concept-rounds/round-54/README.md).

Chosen round 06 concept: [spiral-nautilus-r06-a.png](../concept/spiral-nautilus-r06-a.png), [spiral-nautilus-r06-a.gif](../concept/spiral-nautilus-r06-a.gif) (listed in the [naval](../README.md#concept-art) Concept art table). It goes straight to production in the Act 4 part that flies it (24 shell frames, the crown at 16 headings, the open and close steps, the death and an intel portrait).

## Implementation

- [x] Stat block loaded from a `data.yaml` (step E3); global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen) (`Level25Test`, `ContentLoaderTest`)
- [x] Behaviour, attack and tells per this spec, deterministic and allocation-free (`SpiralNautilusTest`, `aPairReplaysWithoutAllocating`)
- [x] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait (`tools/art/spiral_nautilus.py`, `tools/art/intel.py`; `Level25LooksTest.theNautilusRollsAndUncoils`; round 54)
- [x] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec (`Level25SoundsTest.theSirenAndTheNautilusDieWithTheirOwnSounds`; the a/b in round 54)
- [x] The orbit round the ship (`spiral-in` with the 90 px floor), the armoured closed state and the uncoil window (`SpiralNautilusTest.itSpiralsInFrom220To90OverThreeSecondsUncoilsForTwoPointTwoAndBreaksOutward`, `closedItsShellGlancesOffAllButTheSpecialsBlastsAndAntiSubFireOpenItsCrownTakesTheDamage`, `inATrenchTooNarrowForTheSpiralItsOrbitIsClampedNearTheChannelsMiddle`)

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-06 (round 06): the shell, crown and stripes chosen (Spiral Nautilus r06-a).
- - 2026-10-21: drafted at L25; promoted from the roster fork addition.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-23 (step E3, gap check `m8e-gaps.md` §5, pending the user's review in round 54): the stat block as data; the open
  question closes: the closed shell glances off all but the specials' blasts and `anti-sub` deliveries (the Harpoon, the
  Torpedo Pods, the Depth Charges): the stat block's "`anti-sub` (cracks the shell)" (E8); in a trench the orbit's centre is
  clamped so its 90 px circle and hit box fit the flyable width, a pair holds for its wave's `hold` (15 s, about 3 cycles),
  then breaks off the nearer side; "open about 60 % of the cycle" read as either of the pair, the numbers win (E7).
- 2026-10-23 (steps E2, E5 and ART-E, pending the user's review in round 54): a Nautilus that would orbit closer is
  pushed out to the 90 px floor (as the Dust Devil); its orbit's centre is raised to keep the pair on the field; the shell
  stays armoured through the 0.6 s opening (the crown takes damage only once open); all four `anti-sub` weapons crack the
  closed shell (Harpoon Torpedoes, Depth Charges, both Torpedo Pods); under the EMP a pair holds still; a pair leaving
  after its hold crosses the wall's rock sideways, drawn behind the wall face. Drawn: the closed shell rolling (24 spin
  frames by the distance rolled), the crown at 16 headings, the glance flecks, the halves sinking; the sounds round 54's
  a until the picks.
