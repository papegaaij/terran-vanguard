---
title: Choir Resonator
design: approved
implementation: in-progress
art: chosen
depends-on: [.., ../../generator, ../../../enemies, ../../../systems/economy, ../../../campaign/act-7-beyond-the-gate]
updated: 2026-11-08
---

# Choir Resonator

## Summary

Act 7's one new weapon, a front gun made from a [Gate Warden](../../../enemies/space/gate-warden/README.md)'s eye:
it **sings a wave front**, a thin bright arc that leaves the nose narrow, **widens as it rises** (`spread`) and
**passes through the hulls it meets** (`piercing`), each one taking its damage and leaving it a step weaker until it
is spent. In the shop from the Level 44 visit; the act's strongest front gun per megawatt and its dearest. Designed in
[round 85](../../../concept-rounds/round-85/README.md) (step RES, 2026-11-05) and approved there, from the user's E.2 b of
round 84 (kept as the act's one new weapon); its numbers are in [data.yaml](data.yaml) since M11 part A.

## Design

<!-- data: weapon-properties -->
| Property | Value |
|---|---|
| Slot | Front gun |
| Traits | `spread`, `piercing` |
| Layers hit | `air`, `low-air` and `ground` (a hardened ground target takes nothing: it is not `anti-ground`); not `high-air` (the wave passes under it), not the `sub` layer ([layer rules](../../../enemies/README.md#layer-rules)) |
| Price | 12 000 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 3 000 / 6 000 / 12 000 / 24 000 cr (sell-back per [economy](../../../systems/economy/README.md)) |
| Unlock | L44 (in the shop from the hangar visit before it) |
| Projectile | A **wave front**: an arc of a ring, 32 px wide at the nose, widening evenly to its full width at its reach (L1 160 px at 300 px, L5 240 px at 380 px), flying straight up at 520 px/s (0.58–0.73 s to its reach), then fading |
| Sound family | `resonator` ([weapon sound families](../../../audio/sfx/README.md#weapon-sound-families)) |
| Projectile family (VFX) | `wave` (new: a thin bright arc with a thin dark rim either side; the bell, round 85 A.1 a) |
<!-- /data -->

It covers a level's `forward` in the hangar's launch warning too, as the [Plasma Arc](../plasma-arc/README.md) (a
front gun). Its price is the roster's; the upgrade base of 6 000 is half the price, as the Ion Beam's and the Plasma
Arc's ([sell-back](../../../systems/economy/README.md#sell-back-and-undo)). Its unlock is captured tech (below). Its
sounds are chosen: the fire per wave [a deep falling whoosh, "the heavy pass"](../../../audio/sfx/concept/resonator-fire-r87-a.ogg)
(round 87 S.1 a, played with ±5 % pitch), and per hull passed [a hard metal clang](../../../audio/sfx/concept/resonator-hit-r86-a.ogg)
(round 86 S.2 a). Its numbers are in [data.yaml](data.yaml) since M11 part A.

### Captured tech

The eye of a Gate Warden is a crystal that rings at the Choir's pitch: its `laser-line` is that ring driven hard
through one point. After [Level 43](../../../campaign/act-7-beyond-the-gate/level-43-the-tether-gate/README.md) the
*Steadfast*'s salvage tug, which crossed with her group, lifts eyes from the shattered Wardens at the gate's mouth;
Varga's team sets one in a front mount and drives it with the ship's generator, and fed that way it sings a **wave**
instead of a line. No pickup in the level and nothing to miss: it is in the shop from the L44 visit whatever the
Wardens' secondary did (the act's [missed-captures](../../../campaign/act-7-beyond-the-gate/README.md#story-beats)
rule, with nothing to lose). Varga names it in the
[Level 44 briefing](../../../campaign/act-7-beyond-the-gate/level-44-crossing/README.md#briefing). It is the act's
captured-tech weapon beside the Swarm Call (L45's call organ) and the Mk VI (L46's warm core).

### Per level

<!-- data: weapon-levels -->
| Level | Rate /s | Damage | Hulls | Width at reach | Reach | Single-target DPS | Volley DPS | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|---|---|---|
| 1 | 2.5 | 30 | 3 | 160 px | 300 px | 75 | 173 | 6 | — |
| 2 | 2.5 | 40 | 3 | 180 px | 320 px | 100 | 231 | 6.5 | 3 000 |
| 3 | 2.5 | 52 | 4 | 200 px | 340 px | 130 | 355 | 7 | 6 000 |
| 4 | 3 | 48 | 4 | 220 px | 360 px | 144 | 394 | 7.5 | 12 000 |
| 5 | 3 | 53.5 | 5 | 240 px | 380 px | 160 | 490 | 8 | 24 000 |
| OD | 3.5 | 53.5 | 6 | 260 px | 400 px | 187 | 616 | — (ignores cap) | — |

Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`; do not edit by hand. Single-target DPS = rate × damage (one wave hits a 36 px target 100 px away once). Volley DPS = every hull of the wave's count taking its step (1, 0.75, 0.56, 0.42, 0.32, 0.24): the most a wave does in a packed formation. OD = the overdrive (20 s pickup).
<!-- /data -->

The roster's L5 DPS (160) and price (12 000) are kept. Its L1 (45 in the roster) is raised to 75 and its draw (7 → 11)
lowered to **6 → 8 MW** (decided by me, pending the user's review in round 85): a gun that arrives when the
[Ion Beam](../ion-beam/README.md) L5 (130, 8 MW) is the act's norm is bought for its L3 at once, and at L5 it must
draw no more than the beam it replaces, so the Mk VI's room for a utility holds (the
[Act 7 hand plan](../../generator/README.md#act-7-hand-plan-draft)'s maxed fit is 25 MW with the beam's 8; with a
11-MW Resonator it would be 28 and refused).

### Behaviour

- **The wave:** one wave per shot, from the nose; it flies on in the screen's frame wherever the ship goes after
  (it is not steered), straight up, widening evenly. It is drawn as a thin white arc with an ice-blue glow and a
  thin dark rim either side (the act's readability lesson over the event plane's light), fading out over the last
  fifth of its reach.
- **Through the hulls (`piercing`):** every unit whose hitbox the arc sweeps over is hit **once** by that wave, the
  nearest first; each hull it passes leaves the wave at **0.75** of the damage before it and the arc a step dimmer,
  and at its count of hulls (3 → 5) the wave is **spent** and gone. Every unit it meets counts, whether it takes
  damage or not (a closed shell, a hardened ground target, a mirror's face, a mine, a mote). Each hull it passes
  rings: a thin ring of light on the unit and the `resonator` ring a step lower per hull.
- **The width (`spread`):** narrow close to the ship (one target), wide near its reach (a formation's row): the
  player aims it by **distance** as much as by x. It has no side or rear shots.
- **Bullets:** the wave never touches enemy bullets.
- **Bosses and multi-part units:** the parts of one body count as **one** hull: the wave hits the first part it
  reaches and passes on, never a second part of the same body (as the Plasma Arc's rule). A unit's standing parts (a turret on a hull, a node on a body) count with the body: the front takes the first of
  the body and its parts that it reaches, and when both are equally near it takes the **part**, as a beam does (M11 part I,
  step I2; pending the user's review in round 96).
- **Shields:** not a `shield-breaker`; it wears an [enemy shield](../../../enemies/README.md#enemy-shields) down at its
  damage like any shot. A **closed window shell** ([window shields](../../../enemies/README.md#window-shields), the
  Gate Wardens of Levels 47 and 50) sparks as for any shot and takes nothing, and the wave passes on a step weaker;
  an open one is hit at full damage.
- **Mirrors** ([reflect](../../../enemies/README.md#reflect); proposed for that rule, whose owner is the enemies'
  document): there is no projectile to turn round, so a standing mirror's face takes nothing, bounces nothing and
  spends a step of the wave; a mirror's open flank is hit as a hull. (No mirror unit flies in Act 7; this is for the
  replays and any later use.)
- **Terrain:** a rock or wall stops the part of the front that meets it (as it stops a lance); the rest flies on.
- **Distorted space** (L44, the Heart's phase 2): the ship's shots fly in the screen's frame
  ([distorted space](../../../enemies/README.md#distorted-space)): the wave goes straight up the screen whatever the
  drift does.
- **Motes and mines:** both are hit and both spend a step (the arc is not a seeker); a swarm of motes in front of a
  target soaks a wave, which is the gun's weakness against Mote Swarms.
- **Under water** (none in Act 7; the Act 4 replays): a pressure wave, not light: the guns' × 0.7 of the
  [Europa rules](../../../world/europa/README.md#under-water-rules), not the energy weapons' × 0.2.

### Where it sits

- **Against Act 7's front guns at the same power:** at 6 MW the Resonator L1 does 75 single-target (the Ion Beam L3
  80, the Plasma Arc L2 45); at 7 MW L3 does 130 (the beam L4 105, the Lance Laser L5 95); at 8 MW L5 does 160 (the
  beam L5 130, the arc L5 120) and up to 490 on a packed formation (the arc's chain 395). It is the strongest front
  gun per megawatt, as the campaign's last and dearest (57 000 from L1 to L5 against the beam's 23 750). What it
  gives up: the beam's reach (380 px against the field's height), `high-air` (the beam and `homing` only) and the
  beam's hold on one target (the wave hits a body once a wave, the beam burns it all the time).
- **The power cap:** at L5 it draws the beam's 8 MW, so the hand plan's power table holds as it stands: 24.5 of 27 MW
  at the L47 visit with it in the beam's place, room for one 2-MW utility; at L3 (7 MW) a fit gains 1 MW over the beam.
- **What it covers:** `spread` and `piercing`, so on the front it clears [Level 44](../../../campaign/act-7-beyond-the-gate/level-44-crossing/README.md)'s
  `spread` and [Level 46](../../../campaign/act-7-beyond-the-gate/level-46-graveyard-of-the-choir/README.md)'s `piercing`
  warnings, and with it Levels 44 and 46 are coverable by one loadout without the Fan Blaster or the Lance
  (`TraitCoverageTest`); it takes `beam` off the fit, which [Level 47](../../../campaign/act-7-beyond-the-gate/level-47-seraph-gauntlet/README.md)
  and [Level 50](../../../campaign/act-7-beyond-the-gate/level-50-choir-heart/README.md) recommend.
- **The hand plan does not buy it at L44** (decided by me, pending the user's review in round 85; E.1 a's warnings
  stand as accepted): the plan's beam covers Levels 47 and 50's `beam`, so swapping the beam for the Resonator only
  trades the Level 44 and 46 warnings for those two (the gauntlet and the final boss), and the plan cannot own both:
  the Resonator at the beam's level (L3, 21 000) next to the kept beam leaves the L47 visit about 3 000 short of the
  Mk VI's 32 000 even with the Swarm Tail's L4 put off from L44 to L48, unless the Tesla Coil Pod's L5 also slides
  from L45 to L49 (the forward margin then 0.98 at Level 48, the act's cushion at the L50 visit ≈ 22 000 instead of
  ≈ 43 000). That
  plan is round 85's question; the draft keeps the approved plan.
- **The player's switch:** a player who sells the beam L5 at the L44 visit (60 %: 14 250 back) and buys the Resonator
  to L3 (21 000) flies the same single-target DPS (130) for 6 750 net, plus the wave through the act's packed
  waves (the Rift Skater streams, the Husks, the Seraph pairs), and L4 and L5 from the act's slack later.

## Concept art

Round 85's proposals (step RES; generator `python3 tools/concept/resonator_r85.py [a|b]`, on the frozen
`tools/concept/render/` package): the mount on the production Stormhawk's nose at 1x and 4x with its source render,
one wave at four distances for L1 and L5, the wave through a column of five hulls stepping down, the readability
check over Level 44's brightest and Level 43's darkest window (one Vrell palette) beside the Vrell orbs, a pulse bolt
and the Tesla arc, key frames; the GIF flies L1 then L5 over Level 44's event plane against Rift Skaters (round 84's
a). Prompts: [concept/prompts.md](concept/prompts.md). Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

| File | What | Status |
|---|---|---|
| [concept/choir-resonator-r85-a.png](concept/choir-resonator-r85-a.png) | Choir Resonator a, "the bell": a Warden's eye (a faceted crystal, retuned from crimson to ice-white) in a gunmetal collar with two claws ahead of the nose; the wave one white crescent, thickest in the middle, an ice-blue glow, a thin dark rim either side, two faint rings trailing it; a thin ring on each hull passed (sheet) | chosen (round 85 A.1 a) |
| [concept/choir-resonator-r85-a.gif](concept/choir-resonator-r85-a.gif) | The play field over Level 44's event plane: L1 through a column of four Skaters and a stream, then L5 through a formation of six and a column of five, the Skaters' bursts of three violet orbs, 10 s at 20 fps | chosen (round 85 A.1 a) |

Round 88 (M11 part A, step ART-A): the production frames of A.1 a by `python3 tools/art/act7_gear.py resonator`
(layouts, anchors and blending in the script's header; held in `build/art-held/sprites/` until the weapon's data.yaml
exists): the wave front by its width (`choir-resonator-wave_0..15`, 32 to 260 px, 15 % a step, additive, with its dark
rim `-wave-rim_0..15`, normal), the ring per hull (`-ring_0..7`, 56×56), the mount per banking frame (`-mount_0..4`,
16×24) and the nose glow (`-glow_0..3`, 16×16). Prompts: [concept/prompts.md](concept/prompts.md#choir-resonator-final-r88-a).

| File | What | Status |
|---|---|---|
| [concept/choir-resonator-final-r88-a.png](concept/choir-resonator-final-r88-a.png) | Final frames of A.1 a: the 16 wave widths (light over the rim), one wave at three distances for L1 and L5, the mount on the production hull per banking frame and at 4×, the nose glow, the hull ring; 1× over Level 44's event plane through a column of hulls (Needlers as stand-ins) (sheet) | chosen (round 88 F.1 accept) |
| [concept/choir-resonator-final-r88-a.gif](concept/choir-resonator-final-r88-a.gif) | L1 then L5 firing up through the column over Level 44's event plane, the rings on the hulls passed, each pass a step dimmer, 1×, 5 s | chosen (round 88 F.1 accept) |
| [concept/resonator-capture-r88-a.png](concept/resonator-capture-r88-a.png) | Game capture (A4 rig): fronts at L1 and L5 over Level 43's deep field and L1 over Level 44's brightest band (the Wardens here are the first rig build's stand-in, cycling ART-B's iris frames) | chosen (round 88 F.1 accept) |
| [concept/resonator-capture-r88-a.gif](concept/resonator-capture-r88-a.gif) | L5's fronts over Level 43's field and a Gate Warden, 10 s (made again by step FIXA on Level 43's data after the crash fix) | chosen (round 88 F.1 accept) |
| [concept/resonator-play-capture-r88-a.gif](concept/resonator-play-capture-r88-a.gif) | In play on Level 36 at L5 with the Swarm Call called at 10 s, full screen, 14 s (made again by step FIXA after the crash fix) | chosen (round 88 P.1 ok) |
| [concept/resonator-band-capture-r89-a.gif](concept/resonator-band-capture-r89-a.gif) | Game capture, Level 44 (M11 part B, step B5): the Resonator L5 over the event plane's brightest band, the Narrows (t=186–198) | chosen (round 89 L.2 ok) |

## Implementation

- [x] Weapon data in a `data.yaml` (the numbers above; a new `WAVE` delivery: the widening arc, its speed and reach,
  its count of hulls and the × 0.75 step, one hit per body per wave), checked by content's `WeaponData` (M11 part A, 2026-11-05, step A2a: `delivery: wave`, `WeaponDataTest`)
- [x] The wave against shields, window shells, mirrors, terrain, motes, mines and bodies of several parts as under
  *Behaviour*; a test like `PlasmaArcTest` (the step, the count, one hit per body, the closed shell spending a step) (M11 part A, steps A1b and B1: `WaveFire`, `ResonatorTest`, `ResonatorTerrainTest`; step A4's capture crash when one front killed two units in one step is fixed (step FIXA: the sweep holds units and mines by reference, `ResonatorTest`'s row and pierce-of-five kills); M11 part I, 2026-11-08, step I2: `ResonatorTest`'s rows for a mirror's face (nothing, no bounce, a step spent) and its open flank (a hull), motes and armed mines (each hit, each a step; a swarm in front soaks a wave), a set piece's parts and a unit's unit parts (one hull, the first one the front reaches; a tie goes to the part, as a beam's). The unit parts were missing: `WaveFire` hit the body only; it now meets the first of the body and its standing parts)
- [x] Shop entry from the L44 visit (captured tech, no level pickup); the hangar's trait markers for `spread` and
  `piercing`; it covers `forward` in the launch warning as a front gun (M11 part A, 2026-11-05, steps A2a and A3b)
- [x] The look (round 85's pick of a or b: the mount on the ship's nose, the `wave` family, the ring per hull) and
  the `resonator` sounds (the whoosh per wave, the clang per hull, throttled to one per 0.12 s, 6 % lower per hull) (M11 part A, 2026-11-05, ART-A's frames, step A3b's `ResonatorLooks` and the whoosh and clang cues; round 88 confirms the look)
- [x] `BalanceTest`/`tools/balance.py` count its single-target DPS in the forward DPS; the balance plan as round 85
  decides (as drafted: not bought) (M11 part A, 2026-11-05, step A2a: its hits as `wave`, not bought)
- [x] Varga's Level 44 briefing line fits `BriefingLayoutTest` (the test walks every level's pages; green in M11 part I)

## Open questions

- None open: the sounds are chosen (below).

## Decisions

- 2026-11-05 (user, [round 84](../../../concept-rounds/round-84/README.md) E.2 b, not the recommendation): kept as
  Act 7's one new weapon, a front `spread`/`piercing` gun from captured Vrell tech on sale from L44 (the draft had
  dropped it: [weapons](../README.md#decisions)); E.1 a stands (the plan's trait warnings in Levels 44–46 accepted).
- 2026-11-05 (step RES; decided by me, pending the user's review in round 85): promoted from the roster: the wave
  front (one rule: a widening arc that passes through the hulls it meets, × 0.75 a hull, 3 → 5 hulls); captured from
  the Gate Wardens' eyes after Level 43 (the salvage tug; no pickup), named by Varga in the Level 44 briefing;
  the roster's 12 000 and L5 160 kept, L1 raised from 45 to 75 and the draw lowered from 7 → 11 to 6 → 8 MW (the
  beam's 8 at L5, the Mk VI's utility room kept); an upgrade base of 6 000; the hand plan keeps the beam (Levels 47
  and 50 ask `beam`; the money holds no second front gun before the Mk VI): the plan question goes to round 85.
- 2026-11-05 (user, [round 85](../../../concept-rounds/round-85/README.md) closed): **Q.1 a** the hand plan keeps the
  Ion Beam and does not buy the Resonator at Level 44 (it is the player's choice; the hangar's `spread` and `piercing`
  warnings at Levels 44 and 46 stand); **Q.2 approve**: this document is `design: approved`, with the Level 44 lines it
  added; **A.1 a** "the bell" chosen (`art: chosen`; b "the chord" in `concept/rejected/`); **S.1 and S.2 neither**
  (the user: "Neither of these. These do not read like weapons at all." and "Same, these are like children's toys."):
  the four sounds are rejected and re-taken in round 86, a weapon's shot first and the resonance only a tail.
- 2026-11-05 (user, [round 86](../../../concept-rounds/round-86/README.md) closed): **S.2 a** the hull hit, a hard metal
  clang (`resonator-hit-r86-a`) chosen; the fire sound rejected again ("I'd expect more of a whoosh sound").
- 2026-11-05 (user, [round 87](../../../concept-rounds/round-87/README.md) closed, no notes): **S.1 a** the fire,
  "the heavy pass" (`resonator-fire-r87-a`, a deep falling vwoom, played with ±5 % pitch) chosen, b rejected. Both sounds
  are chosen and the look was chosen in round 85: every asset of the part is `chosen` (still mockups: `art: chosen`).
- 2026-11-05 (step FIXA): the crash step A4's captures found is fixed: one front killing two or more units in one
  step freed a unit while the sweep still held the others' pool indices, which went stale (`Pool.free` moves the last
  unit into the freed slot). The sweep now holds each unit (with its serial) and mine by reference and finds it again
  when it meets it; one taken away by an earlier meet in the same step is passed over and spends nothing. The two
  round 88 captures were made again without the crash.
- 2026-11-06, round 88: F.1 (the Resonator's final art: mount, wave front, rings) accepted and P.1 (the Resonator in play on Level 36) ok; round 89: L.2 ok (the Resonator L5 over Level 44's brightest band). The art rows are chosen.
