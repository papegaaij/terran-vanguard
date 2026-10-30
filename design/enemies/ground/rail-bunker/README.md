---
title: Rail Bunker
design: approved
implementation: done
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials]
updated: 2026-10-29
---

# Rail Bunker

## Summary

A **hardened** Ascendancy gun bunker on asteroid surfaces and station hulls. Its rail turret locks a thin red
line through the ship's position, charges for a second and fires a screen-long rail shot along it: a `heavy`
hit, dodged by leaving the line. Only `anti-ground` weapons and specials break it.

## Design

### Stat block

**Draft** (Act 5 design phase, step D-enemies-B; approved in round 60). Rendered from [data.yaml](data.yaml) since M9 part F (step F2, 2026-10-29; its header lists the build's own readings: a pair's or trio's first lines are staggered by the level (`staggered`)). Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2),
set at its first level from the proposed Act 5 reference DPS (judgement call J2: L33 136): 100 HP is 0.7 s
at the reference, but only an `anti-ground` weapon or a special reaches it. Fire rates are tuned per level
with `levelReport`.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy (machine: the EMP Burst stuns it longest) |
| Layer | `ground` (hardened: only `anti-ground` weapons, the Airstrike, the Orbital Lance and the Smart Bomb damage it) |
| Size tier | `medium` |
| Size | 72×72 px, hitbox 56×56 (the chosen r04 concept's octagon) |
| Parts | single |
| Orientation | `32 angles` (the rail turret's headings, turning 90°/s toward the ship, drawn; the bunker itself fixed) |
| HP | 100 (easy 75 / hard 130, from the global multipliers) |
| Armour / shield | hardened: other shots glance off with a spark; homing shots and Rook do not pick it unless their weapon is `anti-ground` |
| Speed | 0 px/s (scrolls with the rock or hull) |
| Movement | `terrain` |
| Attack | `laser-line` (the rail): at the telegraph's start the turret locks a line from its muzzle through the ship's position to the screen edge and draws it as a thin red line, brightening, for **1 s** (the coils glow gold); then the rail fires along it, an 8 px hit band, live 0.2 s, **`heavy` 10** once; every **4.5 s**, telegraph to telegraph; it holds fire while the ship is within 100 px; any direction, up the screen too; never more than 2 lines telegraphed at once |
| Formations | turret nest (1–3) (single, or 2–3 along a rock face whose lines cross) |
| Weak points | none (the gold coils glow during the charge, drawn only) |
| Effective traits | `anti-ground` |
| Credits | 45 (score 450 × chain) |
| Death | `medium` ground explosion, the rail thrown; the cracked bunker shell stays as a wreck |
| First level / used in | L33; the asteroid surfaces of the Leech Field, judgement call J3; L35's approach on the Sovereign's outer hull; the Iron Sovereign's rail batteries share its line (the boss doc) |
| Difficulty hooks | easy: telegraph 1.2 s, every 6 s; hard: telegraph 0.8 s (the vocabulary's minimum), every 3.6 s |
<!-- /data -->

### Behaviour

- **Lock, not track.** The line's *direction* is fixed when the telegraph starts, and its origin stays on the
  turret and **rides with the bunker** (it scrolls on with the rock or hull; round 66's C.1 b, built in step FIX3): a
  sidestep out of the 8 px band during the second is enough, while the band slides down with the ground (aimed
  ahead so it fires through the ship's locked point: *The lead* below). Its warning is the bright red line (crimson is the lasers' hue; the line
  stays thin until it fires), so it reads at speed.
- **The lead** (round 69's B.2, the user; built in step FIX5). The turret aims in front of the ship, compensating for
  the scroll: at the telegraph's start it predicts where it will be when the rail fires (its move with the ground times
  the telegraph's second) and locks the line from *there* through the ship's position. The telegraph is drawn from the
  muzzle as it scrolls, so the red line sweeps onto the ship's locked point and the rail fires through it: a still ship
  is hit by every line, any direction, and a sidestep out of the 8 px band during the second still dodges (the line
  never tracks). A still origin (a bunker on a halted scroll) aims straight at the ship as before. The same rule leads
  the [Iron Sovereign](../../bosses/iron-sovereign/README.md)'s rail batteries on their turning ring.
- **Hardened.** As the [Hive Node](../hive-node/README.md) and the Drill Rig: a standard loadout glances off,
  the player needs an `anti-ground` weapon (the Bomb Rack, the Hammer Mortar) or a special. A bunker is a
  threat to dodge for a loadout without one; the Strider's topple crushes one under its fall line (see
  [Strider](../strider/README.md)) and so do the belt's mining charges.
- **Enemy fire never hurts enemies**: the rail passes through other units.
- **EMP.** Stunned 3 s: a running telegraph is cancelled, no shot.
- **The mass-relay flash** of L35 is a separate screen-scale `laser-line` hazard (the level doc), not a bunker.
- **Code fit.** The telegraphed full-screen line of the `Sled` and the `laser-sweep`'s telegraph code
  (gap analysis §2.8: `laser-line`, size S).

### Difficulty and level use

- L33: on the large asteroid surfaces (the level's ground layer, so the first level without ground is L30 and
  the Bomb Rack's `NO GROUND` moves there, J3); never more than 2 lines telegraphed at once, never two lines
  that leave no safe column between them.
- L35's approach: on the outer hull before the arena.

### Sounds

Brief for the round-60 sound proposals (Freesound CC0/CC-BY, real recordings, no bubbly or popping
sounds): the charge a rising capacitor whine (a camera flash charging, pitched down) over a low hum; the
shot a violent electric crack with a short metallic ring (a large spark gap or a whip crack layered with a
heavy gunshot); the death a heavy metal explosion.

## Concept art

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

Chosen concept from [round 03](../../../concept-rounds/round-03/README.md) and the round-04 colour pass,
listed in the [ground](../README.md#concept-art) Concept art table: an octagonal hardened bunker with a
twin-rail cannon and gold coils, black and gold with a gunmetal accent and rim light
([rail-bunker-r04-a.png](../concept/rail-bunker-r04-a.png), chosen; r03 superseded). Prompts:
[../concept/prompts.md](../concept/prompts.md). A round-60 readability check against the belt's rock (Q11)
and the production frames (the turret at 32 angles, the charge glow, the wreck) follow in the build.

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (gloss black; the user chose look b, gunmetal/charcoal with gold, in round 60, so the chosen variant is re-rendered in gunmetal in production), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 33 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/rail-bunker-r60-a.png](concept/rail-bunker-r60-a.png) | Rail Bunker A, solid lock line — a thin red line from the muzzle through the ship to the edge, brightening 1 → 2 px over 1.0 s while the coils glow gold; the rail shot (sheet) | chosen (round 60) |
| [concept/rail-bunker-r60-a.gif](concept/rail-bunker-r60-a.gif) | Rail Bunker A, solid lock line — a thin red line from the muzzle through the ship to the edge, brightening 1 → 2 px over 1.0 s while the coils glow gold; the rail shot (motion) | chosen (round 60) |
| [concept/laser-line-capture-r61-a.png](concept/laser-line-capture-r61-a.png) | Game capture on the [capture rig](../../concept/prompts.md#belt-rig-capture-r61-a) (step A4; Spine Turrets on Level 08's landing pad stand in for the bunker): the thin red telegraph lines locked through the ship's position, the same lines flickering just before they fire, the live band (the provisional `LineLooks` gold round a white core until this unit's art) | chosen (round 61, C.5: accepted) |
| [concept/rail-bunker-capture-r69-a.mp4](concept/rail-bunker-capture-r69-a.mp4) | Game capture, Level 33 (step I3; [recipe](concept/prompts.md#rail-bunker-capture-r69-a)), clip with sound: the ridge pair's left bunker telegraphing, its line riding down with the bunker, the direction locked, the rail shot missing the still ship (C.1 b as built; round 69 B.2) | chosen (round 69, B.2 ok (the clip shows the pre-FIX5 aim; the lead is built in FIX5)) |
| [concept/rail-bunker-capture-r69-a.png](concept/rail-bunker-capture-r69-a.png) | Game capture (step I3; [recipe](concept/prompts.md#rail-bunker-capture-r69-a)), frames at 0.6: the same line at t=69.9, 70.4, 70.8 and the shot at 71.0 (round 69 B.2) | chosen (round 69, B.2 ok (the clip shows the pre-FIX5 aim; the lead is built in FIX5)) |

## Implementation

- [x] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers applied; tuned with `levelReport`
- [x] `laser-line`: the locked line, the brightening telegraph (≥ 0.8 s), the rail's band, `heavy` 10 once, any direction
  (M9 part A: the simulation's `LineBeam` in step A1; step A3: a provisional look, `LineLooks`, a thin red telegraph
  brightening and flickering, the live band in gold round a white core, until this unit's art)
- [x] Hardened: glance, the specials' damage, homing and Rook skip it without `anti-ground`
- [x] The locked line rides with the bunker (round 66's C.1 b: the direction locked at the telegraph's start, the origin on the turret; the drawn telegraph, rail and flash follow)
      (step FIX3: `LineBeam.ride` moves a running line's origin onto its unit each step after the moves, a stunned
      unit's too, so the telegraph, the live band and the hit test ride with it; `RailBunkerTest`: on the muzzle with the
      direction locked, a diagonal line slides off a still ship, a near-vertical one hits it and a sidestep dodges, the
      gate of 2 unchanged; `LeechFieldLooks` draws the telegraph, rail and flash from the bunker as drawn, the fading
      rail riding on with it; the autopilot's line rule looks ahead along the moving band. The Iron Sovereign's
      batteries' lines ride the same way, `BossLines`, decided in round 68, 2026-10-29)
- [x] The lead (round 69's B.2): the locked line aims from where the bunker will be when it fires through the ship
      (step FIX5: `LineAim`, shared with `BossLines`; `LineBeam` locks from the unit's last step's move times the
      telegraph's steps; `RailBunkerTest`: a scrolling bunker's live line passes within 1 px of the locked ship point and
      a sidestep dodges; `LaserLineTest`: a still origin aims straight at the ship; the EMP's cancel and the gate of 2
      unchanged; the looks and the autopilot read the line from the simulation, unchanged)
- [x] Production sprites from the chosen r04 concept (the turret at 32 angles, the coil glow, the wreck), the intel portrait
      (step F4: `LeechFieldLooks` in place of `LineLooks`' placeholder for the unit: the turret turning at 90°/s and onto
      the locked line, the coils by the charge, the rail strip live and fading from the muzzle's distance, the flash)
- [x] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds) (step F4: round 60's rail charge and
      shot, round 66's death proposal a, picked: S.7 a)

## Open questions

- J3 (shown in round 60): L33 gets ground (the bunkers need a surface), so `NO GROUND` moves to L30.
- Q11 (decided in round 60, **b**, not the recommendation): the Ascendancy look on the belt is gunmetal/charcoal hulls
  with gold (≥ 20 % gold, rim light, red running lights); a (gloss black) and c (black silhouettes) rejected
  ([enemies](../../README.md#factions-and-visual-language)).

## Decisions

- 2026-09-30: concept round 03 drew the Rail Bunker; the round-04 colour pass (gunmetal accent) chosen.
- 2026-10-27 (step D-enemies-B): promoted from the [ground roster](../README.md) to a draft spec for the Act 5
  design phase; every number is a first draft, approved in round 60.
- 2026-10-27 (decided by me, trivia): the rail aims at the ship (the roster's "along the player's column" read
  as through the ship's position, so it works up the screen too), damage `heavy` 10 (the basis's rail shots);
  the specials' own rules already let the Airstrike, the Smart Bomb and the Orbital Lance damage hardened targets.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`); art A.13 **a**, "Rail Bunker A, solid lock line", chosen (`art: chosen`), the other variant in `concept/rejected/`. Q11 **b** (gunmetal/charcoal with gold, not gloss black): the chosen variant is re-rendered in gunmetal/charcoal in this part's production art, keeping the ≥ 20 % gold and the readability check.
- 2026-10-28 (user, [round 61](../../../concept-rounds/round-61/README.md), closed): **C.5** accepted: the `laser-line` hits the ship once per line and passes through Rook and the escort drones; the provisional look stays until the bunker's art.
- 2026-10-29 (user, [round 66](../../../concept-rounds/round-66/README.md), closed): the production art accepted (F.3 ok: the base, the turret at 32 headings, the coils' glow, the rail strip and flash, the wreck; `art:` stays `chosen` until the line below is built, then `final`); the death sound **S.7 a** (`bunker-death`); **C.1 b** (not the recommendation): the rail line rides with the bunker: the direction is locked at the telegraph's start and the origin stays on the turret, moving with the bunker, so the band slides down with the ground (a still ship is missed by diagonal lines, hit by near-vertical ones); built in FIX3 (sim), replacing step F4's line fixed on the screen. **C.7 b**: hard's third bunker gets a pad at x 240 (FIX3's art part).
- 2026-10-29 (step FIX3): C.1 b built: the line's origin rides with the bunker (sim, look and autopilot), the direction
  locked; `art: final` (F.3 ok, the last open item). The ground's `rail-bunker-capture-r66-a.png` shows C.1 a's line
  leaving the turret: kept as round 66's record and marked stale in the ground's Concept art table (FIX3's 1:1 check
  on Level 33 at t≈69.4–70.5: the red line stays on the scrolling turret's muzzle).
- 2026-10-29 (user, round 69 B.2: "The looks are ok, but the gun must aim in front of the ship. It must compensate for
  the movement of the screen."): the line leads the scroll (Behaviour, *The lead*), built in step FIX5; the looks
  unchanged. `levelReport -Plevel=33`: won 3/3, 0 retries on every difficulty (as before); `Level33Test`'s autopilot
  still struck by 0 lines.
