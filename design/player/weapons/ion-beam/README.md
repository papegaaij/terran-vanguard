---
title: Ion Beam
design: approved
implementation: in-progress
art: final
depends-on: [.., ../../generator, ../../../systems/economy, ../../../enemies]
updated: 2026-10-21
---

# Ion Beam

## Summary

A continuous ion beam from the nose that burns into the first thing in its path. The Act 3 front
gun and the first `beam` weapon: it reaches `high-air`, burns through the Vrell's tendril
barriers and never misses what is straight ahead, but it stops at its first target and draws more
power than any gun before it.

## Design

The numbers the game reads are in [data.yaml](data.yaml) (M7 part A); the marked tables are rendered from it. Prices are shop prices;
damage is in the units of every stat block.

| Property | Value |
|---|---|
| Slot | Front gun |
| Traits | `beam` |
| Layers hit | `air`, `low-air`, `ground` (hardened ground targets glance off) and **`high-air`**; not `sub` or the buried layer ([layer rules](../../../enemies/README.md#layer-rules)) |
| Price | 5 000 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 1 250 / 2 500 / 5 000 / 10 000 cr: the factors on an upgrade base of 2 500 (round 36, Q.23f; see *Price and power*; sell-back per [economy](../../../systems/economy/README.md)) |
| Unlock | L15 (in the shop from the hangar visit before it) |
| Delivery | `beam`: a continuous column from the front muzzle straight up, damage every simulation step while fire is held (see *Beam rules*) |
| Sound family | `beam`: start → loop a → stop ([weapon sound families](../../../audio/sfx/README.md#weapon-sound-families)) |
| Projectile family (VFX) | `beam` (the projectile sheet of concept round 08, with the round-09 beam impact) |

### Per level

<!-- data: weapon-levels -->
| Level | Width px | Damage / s | Volley DPS | Single-target DPS | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|
| 1 | 6 | 45 | 45.0 | 45.0 | 4 | — |
| 2 | 8 | 60 | 60.0 | 60.0 | 5 | 1 250 |
| 3 | 10 | 80 | 80.0 | 80.0 | 6 | 2 500 |
| 4 | 12 | 105 | 105.0 | 105.0 | 7 | 5 000 |
| 5 | 14 | 130 | 130.0 | 130.0 | 8 | 10 000 |
| OD | 18 | 160 | 160.0 | 160.0 | — (ignores cap) | — |

Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`; do not edit by hand. One beam: whatever it touches first takes all of its damage per second, so volley and single-target DPS are the same. OD = the overdrive (20 s pickup).
<!-- /data -->

One beam, so volley and single-target DPS are the same: whatever the beam touches first takes
all of it. The width is the beam's hit width and drawn core; a target is touched when its hit box
overlaps the column. Draw **4 → 8 MW** (the roster had 5 → 9; lowered by M11, round 36, see
*Price and power*); L1 was 35 DPS in the roster and is raised to 45 here, so that L2 overtakes the
typical Pulse Cannon L4 (57) the player arrives with (round 36, Q.23f).

### Against the reference DPS

The typical player reaches Act 3 with a Pulse Cannon L4 (57) and two Micro-missile Pods L2 (10
each): forward DPS 77, load 11 of 11 MW ([balance sheet](../../balance-plan.yaml), L14). The Act
3 reference DPS is proposed by the enemy documents (M18 = a: about 76 at L15 rising to about 100
at L21). Against it:

| Beam level | Beam + two pods L2 | Same with pods L3 | Reached in the typical plan |
|---|---|---|---|
| 1 | 65 | 75 | — (a downgrade from the Pulse L4: the beam is bought at L2) |
| 2 | 80 | 90 | the L18 visit (80 vs reference ≈ 88) |
| 3 | 100 | 110 | from the L19 visit (100 vs reference ≈ 92) |
| 4–5 | 125–150 | — | not in Act 3 (power, see below) |

The single-target number undersells the beam against small units: it burns the first target in its column
from the step it touches, with no flight time and no miss, so a small unit dies far sooner after its entry
than its HP ÷ DPS says. From the plan's first beam (Level 18) the enemies therefore double every `small` unit's
HP (**beam-era HP**, the user's "tougher, not more" of 2026-10-19; see the
[balancing basis](../../../enemies/README.md#balancing-basis)): a killed small unit lives about as long on the
screen as on Level 10.

### Beam rules

The `beam` delivery is new; these rules also hold for the Orbital Lance special
([specials](../../specials/README.md)) where it says so.

- **Firing.** While fire is held (or auto-fire runs) the beam is on: it reaches full length in
  0.05 s, with the `beam` start sound, then the loop; on release the stop sound and a 0.08 s
  fade. There is no heat or ammunition. Damage is applied every simulation step (60 per second),
  one sixtieth of the level's damage per second each, to the one target the beam touches.
- **First target only.** The beam runs from the front muzzle straight up the column of its
  width and ends at the first thing that stops it, nearest the ship: an enemy it can damage, or
  a canyon wall or rock island. It never pierces (a tendril is the one thing it burns on its way
  past, below). With nothing in its path it runs
  off the top edge (no cap, round 09). At a target it bores 4 px into the silhouette and ends in
  the round-09 contact flare (back-sparks, scorch, heat shimmer).
- **What stops it.** Every unit on `air`, `low-air`, `ground` and `high-air` that it can damage
  stops it; on a tie the higher layer is hit. **Hardened** ground targets do not stop it: it is
  not `anti-ground`, so it passes over them with a faint glance spark at the crossing, like a
  ground unit under an air shot. Units in the `sub` and buried layers, allies (domes, Rook,
  drones), pickups and enemy bullets do not stop it (the beam does not clear bullets).
  **Bosses and set pieces stop it on every layer** (round 37 R.4 c, user decision): a part that
  takes no damage now (a shut bay, a core before its phase, the Siege Spire's trunk and maw in the
  root phase, though on the ground layer) and an armoured body or neck stop it at their edge with
  the glance spark; a part it can damage burns. A `high-air` boss's hull stops it like any armoured
  body (the Brood Carrier in its first phase), where bolts pass beneath it; an arena boss's platform
  (the Spire's rooted body between its roots) stays scenery, as for shots.
- **Walls** (M2 = a): a canyon wall or rock island in the beam's
  column stops it at the wall's edge, with a rock-scorch impact (the contact flare tinted with
  dust); targets beyond the wall are safe. The wall data is the terrain polylines of
  [Act 3](../../../campaign/act-3-red-dust/README.md) (Level 16).
- **Tendrils** (M9 = a): the energy link between two Tendril Anchors
  ([ground units](../../../enemies/ground/README.md)) has its own HP, which only `beam` damage
  lowers: the Ion Beam and the Orbital Lance. A tendril crossing the beam's column takes the
  beam's full damage and the beam **goes on past it** to its first target; a burnt-through tendril
  stays broken (the Tendril Anchor's document owns the link's HP and what other shots do at it).
  The 40 HP proposed there last 0.9 s of this beam at L1 (45 DPS).
- **`high-air`**: the beam reaches it like a homing shot; a `high-air` unit over the column is
  hit at full damage.
- **`reflect`**: a frontal shield that bounces non-`beam` shots does not bounce the beam; the
  shield part stops it and takes the damage as its hit rules say.
- **Auras** (M8 = a): a Choir Herald's aura ring on a unit absorbs beam damage like any other
  damage until it is empty.
- **Weak points and bosses**: multipliers apply per step. Bosses assume 0.6 × reference as
  ever; the beam's sure hit makes it a strong boss weapon, which its price and draw pay for.
- **Kills and score**: as any shot: bounty, chain, objectives, statistics. The hit flash of a
  unit under the beam is a steady heat tint while it burns, not a blink per step.
- **Rook** has no beam gun (M17 = a); the Ion Beam is not one of his four guns.
- **Under water** (Act 4): later, with the [Europa rules](../../../world/europa/README.md#under-water-rules).

### Price and power (M11)

The beam is the costliest and hungriest gun the shop has sold so far. Checked by hand against the
[generator](../../generator/README.md) and [shield](../../shields/README.md) tiers, with the
typical Act 2 kit the player keeps (two Micro-missile Pods L2 at 1.5 MW each, Tail Gun L2 at 1.5,
engine Mk I at 0):

| Beam | Shield Mk II (3) | Shield Mk III (4) | Fits Mk II "Arc" 11 MW? | Fits Mk III "Fusion" 16 MW? | Fits Mk IV "Tokamak" 18 MW (act 4)? |
|---|---|---|---|---|---|
| L1 (4) | 11.5 | 12.5 | no | yes | yes |
| L2 (5) | 12.5 | 13.5 | no | yes | yes |
| L3 (6) | 13.5 | 14.5 | no | yes | yes |
| L4 (7) | 14.5 | 15.5 | no | yes | yes |
| L5 (8) | 15.5 | 16.5 | no | with shield Mk II | yes |

The table has the approved 4 → 8 draw and the Mk III's 16 MW of the power room (2026-10-21, below); the
M11 check below was made with the roster's 5 → 9 and the Mk III's 14 MW.

- The player arrives at 11 of 11 MW on the Mk II, so **any** beam needs the Mk III generator
  first (4 000, on sale since Act 2). Generator plus beam at L2, less the Pulse Cannon's 1 260,
  cost 8 990 against about 5 400 credits at the L16 visit after the repairs (budget(n) income, the
  user's decision of 2026-10-14): the typical player cannot fly a useful beam in Level 16 (an L1 beam, 65
  forward, would be a downgrade from the Pulse L4's 77). Level 16's tendrils therefore must stay
  breakable through their anchors (M9 = a).
- A hand plan (credits from the budget curve plus the typical B grade, 125 cr of repairs a
  visit): L15 one EMP charge; L16 the Mk III; L17 save; L18 sell the Pulse Cannon (1 260) and buy
  the beam at L2 (6 250); L19 beam L3 (2 500, only with the draw recommended below); L20 Tail Gun
  L3 (600); L21 Composite III (3 000). Forward DPS 77 · 77 · 77 · 80 · 100 · 100 · 100 for
  L15–L21, at most 14 of 14 MW (15 of 16 since the power room: the plan adds a Pickup magnet at the L20
  visit). The [balance plan](../../balance-plan.yaml) follows it on budget(n)
  income (the user's decision of 2026-10-14, round 37 R.1's act-factor income reverted; Level 15's own
  typical haul), each item as soon as it is affordable, which gives exactly this hand plan's visits
  (medium: 387 credits left at the tightest visit, L19).
  On hard the beam is refused at L18, so the hard sheet keeps the Pulse Cannon L4 (accepted in round 34 (2026-10-12); hard is tighter on purpose). Without the beam the same credits buy the Pulse L5 and pods L3 for 100 already
  at L18 (4 000 instead of about 7 500 net), the usual price of a trait weapon (the Lance Laser
  at L05 did the same).
- With draw 5 → 9, beam L3 is **0.5 MW over** the Mk III with the Act 2 kit: the beam plan stalls
  at L2 (13.5 of 14) for the rest of Act 3, and the Evasive thrusters (2 MW) do not fit either.
- **Decision (M11, accepted in round 36):** keep the generator tiers (option a; the
  Tokamak's 9 000 is out of reach next to the beam in Act 3, so moving it, option b, would not
  help the typical player before L21) and lower the beam's draw to **4 → 8 MW** (4 / 5 / 6 / 7 /
  8): beam L3 then fits the Mk III with the Act 2 kit and shield Mk II at 13.5 of 14, the beam
  plan reaches 100 at L19 like the gun plan, and L4–L5 still wait for the Tokamak in Act 4. The
  weapon's Per level table has the 4 → 8 draw; the power table in this section had the roster's 5 → 9
  it was checked against until step PD-C (2026-10-21) brought it up to date.
- **The power room (the user, 2026-10-21):** the [Mk III](../../generator/README.md) gives **16 MW**
  (was 14), and every fitted weapon has a free [power dial](../../README.md#power-budget). With the
  typical kit the beam L3 now leaves 2.5 MW with shield Mk II (1.5 with shield Mk III): room for a
  utility module or the Evasive thrusters. Power no longer keeps beam L4 out of Act 3 (L5 only with
  shield Mk II); its 5 000 (L4) and 10 000 (L5) keep it out of the typical plan (pending the user's
  review).
- The upgrade base of 2 500 (the Lance Laser's prices) instead of the price is taken so L3 is
  reachable in Act 3 at all: on the price, L2 + L3 would cost 7 500 more instead of 3 750.

## Concept art

Proposals: concept round 36 (step A-gear, listed in the [weapons' Concept art
table](../README.md#concept-art); prompts in [../concept/prompts.md](../concept/prompts.md#ion-beam-r36-a),
generator `tools/concept/gear_r36.py`); round 36 (2026-10-13) chose **a** (G.1), b is rejected:
[a](../concept/ion-beam-r36-a.png) ([GIF](../concept/ion-beam-r36-a.gif)), the chosen round-08/09
beam family drawn per level (6 → 14 px, overdrive 18 px) with tendril burn-through, rock-scorch and
the glance spark over hardened ground; and [b "ion stream"](../concept/rejected/ion-beam-r36-b.png)
([GIF](../concept/rejected/ion-beam-r36-b.gif)), a white core in a cyan sheath with helix strands and
packets, a ring splash at the contact and slag beads on rock.

Production art (M7 part A step ART-A, for concept round 37; prompts in
[concept/prompts.md](concept/prompts.md)): `tools/art/ion_beam.py` draws look a into `assets/`: the
column body per level as a 64 px tile that repeats upwards (`ion-beam-column-1..6`, 6 = overdrive),
the contact end (`ion-beam-tip-1..6`), the contact flare large and small, the rock-scorch flare with its
dust and scorch mark, and the glance spark; the heat tint on the target is the game's.

| File | What | Status |
|---|---|---|
| [concept/ion-beam-final-r37-a.png](concept/ion-beam-final-r37-a.png) | Final effects: the six column tiles, the tips, contact flares, rock-scorch flare, dust, scorch mark and glance spark, and the beam in a scene (sheet) | chosen (round 37, F.1: approved as final) |
| [concept/ion-beam-final-r37-a.gif](concept/ion-beam-final-r37-a.gif) | The beam at L2 on a Needler, sweeping onto a rock island (scorch, dust) and over a hardened Hive Node, then L5 and the overdrive into open sky (motion) | chosen (round 37, F.1: approved as final) |

## Implementation

- [x] Weapon data (`data.yaml`) with the numbers above, the `beam` hits and its width per level — M7 part A (step A2: `WeaponData`, validated by `ContentValidator` and its record checks)
- [x] The `beam` delivery: a continuous column, first-target rule, per-step damage, hardened
  targets passed over, `high-air` reached, allies and bullets ignored (M7 part A step A1:
  `WeaponSpec.Delivery.BEAM`, `vanguard.sim.Beam`, the single-pass cast in `PlayerFire.burn`; tests
  `BeamTest`, `ActThreeArsenalFlightTest`)
- [ ] Walls and rock islands stop the beam (with the terrain data of Level 16)
- [ ] Tendrils: beam damage on a link's own HP (with the Tendril Anchor)
- [x] Beam effects: the round-08 beam family and the round-09 impact as production art, the
  steady heat tint on the target, the rock-scorch impact; the `beam` start, loop and stop sounds
  (the art is drawn: `tools/art/ion_beam.py`, M7 part A step ART-A, approved as final in round 37
  (F.1); in flight step A6: `ArsenalLooks` draws the column per level width, its tip bored in,
  the large or small contact flare by the target's size, a muzzle glow at the nose, the glance
  spark, the 0.08 s fade; the steady heat tint in `LevelRenderer`; until the walls of Level 16 the
  rock-scorch flare and its scorch marks show where it burns a ground object, the scorch marks under
  a ground unit too; the start with the loop, the stop, and the sizzle while it burns)
- [x] Power draw per level counted in the loadout; upgrades priced as listed (content: `Catalogue`/`Hangar` price and count it, M7 part A step A2; in the live shop since step A5 drew its hangar icon and emptied `Catalogue.AWAITING_ICONS`; tests `ActThreeShopTest`, `ActThreeHangarTest`)
- [x] The Act 3 balance plan and `BalanceTest` with the beam (M7 part A: the plan's rows 15–21 buy
  it at L18 and raise it at L19, step A2; `BalanceTest` checks the hand formulas against the flown
  beam, step A4; the hard plan could not afford it at L18, round 37 R.2, accepted; re-planned for
  round 37 R.1's act-factor income and back to L2 at L18, L3 at L19 since the user reverted it on
  2026-10-14, M7 part B step B3; hard again keeps the Pulse Cannon)
- [x] Round 37 R.4 (c): the beam stops on the Brood Carrier's hull in its first, high-air phase and on
  the Siege Spire's shielded and armoured parts on the ground layer (`PlayerFire.burn`'s cast: a set
  piece's armour is never passed over; tests `BeamTest`, `IonBeamBossesTest` with the real bosses)

## Open questions

- None open: round 36 answered them (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the weapons roster with full per-level
  numbers in Act 1 terms; the `beam` delivery rules (first target, walls per M2 = a, tendrils per
  M9 = a, hardened targets passed over, `high-air` reached); the M11 power check by hand
  (recommendation: keep the generator tiers, draw 4 → 8). All accepted in round 36 (2026-10-13).
- 2026-10-12 (overnight design phase): concept round 36 proposes an a/b of the beam's look;
  `art: proposed`, pending the user's choice.
- 2026-10-13 (user, round 36): approved (D.20). M11 accepted and Q.23f–g yes: the draw lowered to
  4 → 8 MW in the table, L1 at 45 DPS, upgrades on a 2 500 base, no `shield-breaker` (the EMP Burst
  strips shields from L15). G.1 = a: the round-08/09 beam family per level with the tendril
  burn-through, rock-scorch and glance spark chosen; the "ion stream" (b) rejected and moved to
  `../concept/rejected/`. Its loop sound is the heavier hum (S.4 = a, [sfx](../../../audio/sfx/README.md)).
- 2026-10-13 (M7 part A, step A2): data.yaml written (`hits: beam`, each level a `width` and a damage
  per second); the per-level table is rendered from it.
- 2026-10-13 (round 37 R.1, user decision: the act factor on the balance's income without level data):
  the balance plan buys the beam straight to L3 at the L17 visit instead of L2 at L18 and L3 at L19
  (forward DPS 100 from Level 17); hard (income × 0.9) now affords it at L17 too, so round 37 R.2's
  "hard stays at 77 until the beam" no longer occurs. The Act 3 levels' own data (part B on) replaces
  the fallback income.
- 2026-10-13 (round 37 R.4 c, user decision: fix): the beam no longer runs past the Brood Carrier in
  its first phase (a high-air boss's hull was skipped as for bolts, which pass beneath it) or the
  Siege Spire (its maw, trunk and core are on the ground layer and were passed over like hardened
  ground): a set piece's armour stops it on every layer with the glance at its edge (B.1 b), its
  damageable parts burn.
- 2026-10-13 (user, round 37): the production art approved as final (F.1; `art: final`), the
  capture accepted (K.1), the simulation's build choices kept (B.1 a–f: a trigger counts one hit per
  0.1 s of contact; the beam stops at what it cannot damage above the ground layer with a glance), the
  rock-scorch flare on ground objects until the walls (B.5 c) and its sounds (S.1 start a "charge
  snap", S.2 stop b "ion collapse", S.3 sizzle a "arc scorch", S.4 on rock b; B.6 a–b) kept; R.1 the act
  factor applied, R.2 accepted (now moot), R.4 (c) fixed as recorded above with Q1's `CAST_ARMOUR` rule.
- 2026-10-14 (user: budget(n) is the absolute typical haul; round 37 R.1 reverted; M7 part B, step B3): the
  balance plan buys the beam at L2 at the L18 visit and raises it to L3 at L19 again (A2's hand plan),
  the Tail Gun L3 at L20 and Composite III at L21; hard keeps the Pulse Cannon (accepted in round 38 (2026-10-14)).
- 2026-10-19 (M7 part E, step E5; the user's "tougher, not more"): the enemies' beam-era HP (×2 on `small` units
  from Level 18, the plan's first beam) answers the beam's instant reach; the beam's own numbers are unchanged.
- 2026-10-21 (step PD-C, the user's power room): the stale *Price and power* table (the roster's 5 → 9 MW draws)
  brought up to the approved 4 → 8 and the Mk III's new 16 MW; beam L4 now fits the Mk III power-wise (L5 with
  shield Mk II), the price keeps it out of the typical plan (pending the user's review).
