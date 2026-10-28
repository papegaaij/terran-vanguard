---
title: Generator
design: approved
implementation: done
art: n/a
depends-on: [../weapons, ../shields]
updated: 2026-10-28
---

# Generator

## Summary

The generator's output (MW) caps the total power draw of the fitted loadout. Heavy weapons need
a bigger generator. Output that is not used boosts shield regeneration. The model is described
in [player](../README.md#power-budget).

## Design

<!-- data: generators -->
| Model | Output | Price (first draft) | Available |
|---|---|---|---|
| Mk I "Spark" | 8 MW | starter | start |
| Mk II "Arc" | 11 MW | 1 500 | act 1 |
| Mk III "Fusion" | 16 MW | 4 000 | act 2 |
| Mk IV "Tokamak" | 20 MW | 9 000 | act 4 |
| Mk V "Helix" | 22 MW | 18 000 | L35 (Helix Dynamics tech, captured) |
| Mk VI "Choir Core" | 27 MW | 32 000 | act 7 (Vrell tech) |
<!-- /data -->

- Buying a generator replaces the current one. The old one goes to the inventory and can be
  sold (see [economy](../../systems/economy/README.md)).
- Spare power bonus: +10 % shield regen per spare MW, max +50 %.
- In a level the generator feeds the HUD power gauge (see [HUD](../../ui/hud/README.md)).
  Normally its output is constant, but some enemies can drain it (below).

### Enemy drain effects

*Final draft of the 2026-09-30 rules, step `m9-d-systems` (2026-10-27), **approved in round 60** (Q2 = a, recommended). The single definition of a drain; the drainer itself, its latch and
how it is shaken off are the [Void Leech](../../enemies/space/void-leech/README.md)'s (introduced in L33, see the
[act](../../campaign/act-5-the-belt/README.md)); the HUD's side is in [HUD](../../ui/hud/README.md#right-panel-ship).*

Some enemies attack the power budget instead of the hull. The rules:

| Rule | Value (draft) |
|---|---|
| Drain | Each latched drainer lowers the **effective output** E by **2 MW**; drains stack to **−6 MW** (a fourth leech latches but drains nothing more). Shaking it off or killing it restores its 2 MW at once. |
| Load at or below E | Nothing changes but the spare-power bonus, which is counted from E (the spare MW are E − load). |
| Load over E: first | The **shield's regeneration stops** (its draw is the first thing shed). The shield keeps what it holds and still takes hits; its regen delay keeps running, so it regrows as soon as the load fits again. |
| Load over E: then | If the shortfall (load − E) is larger than the shield's draw, **every fitted weapon's rate of fire** is multiplied by **f = E ÷ (load − shield draw)**, never below **0.5**. A beam's damage per second, a drone's gun and a Deflector Pod's charge return scale the same way. |
| Never affected | Armour, the engine's speed and the utility modules (they keep working), specials, the escort (its own power), and an overdrive's extra draw (it ignores the cap, below). |
| Feedback | The HUD's power gauge (the drained MW in the warning colour, `POWER DRAIN`, a flash while the load is over E); in the play field the ship's muzzle flashes flicker while f < 1 and the shield's hex ring is drawn dimmed while its regeneration is stopped; a low brown-out hum while the load is over E (a sound for round 60). |
| Warning | The hangar intel of a level with drainers names them; Varga's intel line tells the player to bring spare power (L33's level document has the line). The power dial ([player](../README.md#power-budget)) is the free answer: a weapon turned down in the hangar makes room. |

Why this order: a heavy loadout on a small generator is the most vulnerable, which is Act 5's
"energy" identity and the point of L33; the shield goes first because its loss is felt (the ring
dims, nothing regrows) but survivable, and the weapons' cut is proportional, so a loadout with 1 MW to
spare hardly notices one leech and a loadout at its cap feels every one. What the numbers mean for the
typical plan is in the [Act 5 hand plan](#act-5-hand-plan-draft) below: two leeches stop its shield's
regeneration, three cut its fire rate to about 0.88.

### Overdrive and the power cap

An overdrive pickup (all weapons +1 level for 20 s, see [player](../README.md)) **ignores the
power cap**: the extra draw of the overdrive patterns is not counted, so overdrive never stops
shield regeneration or lowers fire rate. It is a reward, not a risk.

### Act 4 hand plan (draft)

*Draft, decided in round 48 (2026-10-22) (Q1 = b, the Mk IV note below). A hand calculation
for the design phase, no data file and not read by any test; part A of M8 replaces it by the
measured balance plan (`balance-plan.yaml` visits 22–28, `reference_dps` 22–28 in
`enemies/data.yaml`, `TypicalHaulTest`, `ActPlaythroughTest`). Draws and DPS are those of the
weapon documents: [Harpoon Torpedoes](../weapons/harpoon-torpedoes/README.md),
[Depth Charges](../weapons/depth-charges/README.md), the Micro-missile Pod, the
[Ion Beam](../weapons/ion-beam/README.md), the Tail Gun, the [shield](../shields/README.md) Mk II
(3 MW), the Pickup magnet (1 MW), the [pressure hull](../systems/README.md#pressure-hull-l22) (1 MW).*

**The loadout entering Act 4** (the Act 3 plan to L21): Ion Beam L3, Micro-missile Pods L2 ×2,
Tail Gun L3, Pickup magnet, Shield Mk II, Mk III 16 MW: 6 + 3 + 2 + 1 + 3 = **15 of 16 MW**, nothing
`anti-sub`. Single-target DPS 80 + 20 + 20 = **120** on the surface; under the recommended water
rules (non-`anti-sub` × 0.7) it is 56 + 14 + 14 = **84** (70 %); under the as-drafted rules
(× 0.5, beam reach halved) 40 + 10 + 10 = **60** (50 %), which is why Q1 recommends (b).

**The plan, medium** (visit income = the previous level's haul × 1.1 grade bonus, less about 125 cr of
repairs; haul = 700 × 1.07^(n−1): L21 2 709 · L22 2 898 · L23 3 101 · L24 3 318 · L25 3 551 · L26
3 799 · L27 4 065). The first column is the level that follows the visit.

| Visit | Cash | Buys | Sells | Carry |
|---|---|---|---|---|
| L22 | 2 855 | Harpoon Torpedoes 2 000, L2 500 (flown with the beam: L22 is a dry ice surface) | — | 355 |
| L23 | 3 418 (+ 900 sold) | Depth Charges 1 300, L2 400, L3 800; Harpoon L3 1 000 | Tail Gun L3 (900) | 818 |
| L24 | 4 104 | Pressure hull 2 000; one wing pod's Micro-missiles L3 800 | — | 1 304 |
| L25 | 4 829 | the other pod's Micro-missiles L3 800 | — | 4 029 |
| L26 | 7 810 | saving for the Mk IV | — | 7 810 |
| L27 | 11 864 | Generator Mk IV 9 000; Harpoon L4 2 000 | — | 864 |
| L28 | 5 211 | (the armour and shield tiers of part A's plan, the nanites 6 000) | — | — |

**Reference DPS 22–28** (single-target, medium; the proposal is the gap note's: about 96 at L22, then
+4 a level; the DPS columns are the plan's, in the water rules' effective terms):

| Level | Medium | Loadout (single-target DPS) | Effective DPS | Proposed `reference_dps` | Ratio |
|---|---|---|---|---|---|
| 21 | dry | Ion Beam L3 80 + missiles 20 + Tail Gun 20 | 120 | 100 (as is) | 0.83 |
| 22 | ice surface, dry | the same (the harpoon is idle: `NO WATER`) | 120 | **96** | 0.80 |
| 23 | surface, then water | Harpoon L3 62 + Depth Charges L3 52 + missiles 2 × 10 × 0.7 = 14 | 128 | **100** | 0.78 |
| 24 | water | + one pod L3 (15 + 10) × 0.7 = 17.5 | 131.5 | **104** | 0.79 |
| 25 | water | both pods L3: 21 | 135 | **108** | 0.80 |
| 26 | water, dark | the same | 135 | **112** | 0.83 |
| 27 | water | Harpoon L4 80 + Depth Charges 52 + 21 | 153 | **116** | 0.76 |
| 28 | water, then air | the same | 153 | **120** | 0.78 |

The ratio runs 0.76–0.83 against Act 3's 0.83 at L21: Act 4 asks slightly less of the shots that
miss (a harpoon is slower and fewer, bullets × 0.7 are easier to dodge and so are the enemy's), and
part A measures it with `levelReport` before the numbers go into the data. The act HP factor of a
returning `medium`+ unit needs these seven entries (`UnitRules.actHpFactor` throws without them).

**Power check by level** (Mk III 16 MW; the loadout above, shield Mk II 3, magnet 1):

| Level | Fitted | Draw | Mk III 16 | Mk IV 18 | Mk IV 20 |
|---|---|---|---|---|---|
| 23 | Harpoon L3 4 + Depth Charges L3 2.5 + missiles 3 + magnet 1 + shield 3 | **13.5** | fits | fits | fits |
| 24 | + hull 1, one pod L3 (+0.5) | **15** | fits | fits | fits |
| 25 | both pods L3 | **15.5** | fits | fits | fits |
| 27 | Harpoon L4 (+0.5) | **16** | fits exactly | fits (+2: +20 % regen) | fits (+4: +40 %) |
| max anti-sub | Harpoon L5 5 + Depth Charges L3 2.5 + two Torpedo Pods L3 4 + hull 1 + magnet 1 + shield Mk II 3 | **16.5** | **refused** | fits | fits |
| + nanites | + Auto-repair nanites L1 3 | **19.5** | refused | refused | fits |
| + Shield Mk IV (5, +2) | the max build with nanites | **21.5** | refused | refused | refused |

So the Mk III just carries the typical plan (16 of 16 at L27), the 18 MW Mk IV carries a full
`anti-sub` build but no nanites, and a 20 MW unit carries the nanites too; the Shield Mk IV's +2 MW
asks for a choice (a power dial, or one fewer gun), which is the intended wall.

**The Mk IV "Tokamak" note** (the open item of 2026-10-21: "to revisit when Act 4 is designed"). The
user decided **20 MW** in round 48 (2026-10-22, J1); [data.yaml](data.yaml) says 20 since M8 part A (step A2). *Recommendation, accepted:
raise it to **20 MW**.* Why: at 18 MW it is +2 over the Mk III for 9 000 cr and does not carry the nanites
(3 MW), the act's heavy module; at 20 MW it is a real step (+4: the nanites, or the second Torpedo Pod
pair, next to the full plan) and the Mk V "Helix" at 22 stays a step of +2 as the Mk III to IV was in
the first draft. The change is one number in [data.yaml](data.yaml) and is measured in part A on the
extended plan (as the Mk III's 14 → 16 was, with `BalanceTest`), not tonight. Alternative: keep 18 and
leave the nanites a luxury; rejected because a 3 MW module that no generator carries has no use.

### Act 5 hand plan (draft)

*Draft, step `m9-d-systems` (2026-10-27), **approved in round 60** (J2, Q12 = c). A
hand calculation for the design phase, no data file and not read by any test; part A of M9 replaces it
by the measured balance plan (`balance-plan.yaml` visits 29–35, `reference_dps` 29–35 in
`enemies/data.yaml`, `BalanceTest`, `ActPlaythroughTest`, `:content:levelReport`). Draws and DPS are
those of the weapon documents: the [Ion Beam](../weapons/ion-beam/README.md), the Micro-missile Pod,
the Tail Gun, the [Swarm Tail](../weapons/swarm-tail/README.md), the [Tesla Coil
Pod](../weapons/tesla-coil-pod/README.md); the [shield](../shields/README.md) Mk III (4 MW), the
Pickup magnet (1 MW).*

**The loadout entering Act 5** (the balance plan after the L28 visit, `tools/balance.py`): Harpoon
Torpedoes L4, Tail Gun L3, Micro-missile Pods L3 ×2, Pickup magnet, pressure hull, Shield Mk III,
Composite III, Mk IV 20 MW: 16.5 of 20 MW, 3 168 cr left; the Ion Beam L3, the Depth Charges L3 and
the Sonar Pulse wait in the inventory (nothing is sold since M8 AW). In vacuum the Harpoon is idle
(`NO WATER`) and the pressure hull does nothing, so the L29 visit swaps them out: the dry loadout of
the beam L3 would be 80 + 30 = **110** forward DPS (the tool's measure: the front and the wings) against
a reference of 120 (0.92), too thin for the act's first level, hence the **Ion Beam L4** (5 000) at once.

**The plan, medium** (visit income = the previous level's haul × 1.1 grade bonus, less 75 cr of the
ship's repairs and, from L30, Rook's 50; haul = 700 × 1.07^(n−1): L28 4 351 (its data) · L29 4 654 ·
L30 4 980 · L31 5 329 · L32 5 702 · L33 6 101 · L34 6 528 · L35 6 985; the act factor 6.5536 = 1.6⁴; the
act's income ≈ 41 000 over the seven visits). The first column is the level that follows the visit.

| Visit | Cash | Buys and fits | Spent | Left | Load / output |
|---|---|---|---|---|---|
| L29 | 7 954 | the Ion Beam (owned L3) fitted and raised to **L4** 5 000; the Harpoon and the pressure hull to the inventory; the EMP Burst fitted (its charges since L15: Act 5's units are machines with shields); the loaner Bulwark flies (Rook missing) | 5 075 | 2 879 | 18 / 20 |
| L30 | 7 998 | **Swarm Tail** 3 500 and L2 1 750 on the rear (the Tail Gun L3 to the inventory); Rook back with his guns | 5 375 | 2 623 | 19.5 / 20 |
| L31 | 8 101 | **Composite IV** 6 000 (140 armour, the escort level's hits) | 6 125 | 1 976 | 19.5 / 20 |
| L32 | 7 838 | Swarm Tail **L3** 3 500 (the Airstrike is forbidden inside the asteroid; the EMP stays) | 3 625 | 4 213 | 20 / 20 |
| L33 | 10 485 | nothing: no power room (20 of 20) and the Mk V not yet salvaged; saving | 125 | 10 360 | 20 / 20 |
| L34 | 17 071 | nothing: saving for the Mk V (the breather) | 125 | 16 946 | 20 / 20 |
| L35 | 24 127 | **Generator Mk V "Helix"** 18 000 (salvaged in L34, in the shop from this visit); both Micro-missile Pods **L4** 3 200 | 21 325 | 2 802 | 21 / 22 |

The core tiers the plan leaves out: the Shield Mk V (14 000, 6 MW) and the Engine Mk IV (8 000, 2 MW)
need power the Mk IV does not have, and the Tesla Coil Pod is the player's choice for the shielded
units (light in Act 5, see Q1); all three are there for a richer or a hard run, and the Mk V's 22 MW
makes room for one of them in Act 6.

**Reference DPS 29–35** (single-target, medium; J2 of round 60: 120 at L29, then +4 a level). The
forward DPS is the tool's (the front and the wings, as `BalanceTest` measures it); the rear is the
rear gun's.

| Level | Loadout (single-target DPS) | Forward DPS | Rear DPS | Proposed `reference_dps` | Forward / reference |
|---|---|---|---|---|---|
| 28 | Harpoon L4 99 + pods 2 × 15 × 0.7 (under water) | 120 | 20 | 120 (as is) | 1.00 |
| 29 | Ion Beam L4 105 + pods L3 2 × 15 | 135 | 20 (Tail Gun L3) | **120** | 1.13 |
| 30 | the same | 135 | 21 (Swarm Tail L2, homing) | **124** | 1.09 |
| 31 | the same | 135 | 21 | **128** | 1.05 |
| 32 | the same | 135 | 30 (Swarm Tail L3) | **132** | 1.02 |
| 33 | the same (three latched leeches: × 0.875) | 135 (118) | 30 (26) | **136** | 0.99 (0.87) |
| 34 | the same | 135 | 30 | **140** | 0.96 |
| 35 | Ion Beam L4 105 + pods L4 2 × 21 | 147 | 30 | **144** | 1.02 |

Act 4 ran 0.79–1.07 on this measure (the tool's L22–L28, 1.00–1.07 from L24); Act 5 opens higher (1.13: the beam's jump
from the water's × 0.2 back to full, and the act's new shields and machines) and falls to 0.96 at the
breather, where the plan saves for the Mk V; part A measures it with `levelReport` before the numbers go
into the data. The act HP factor of a returning `medium`+ unit needs these seven entries
(`UnitRules.actHpFactor` throws without them). Budgets 29–35: **4 654 · 4 980 · 5 329 · 5 702 · 6 101 ·
6 528 · 6 985**.

**Power check by level** (Mk IV 20 MW, the Mk V 22 from L35; E = the effective output under the
[drain](#enemy-drain-effects), shield Mk III 4 MW):

| Level | Fitted | Load | Two leeches (E = output − 4) | Three leeches (E = output − 6) |
|---|---|---|---|---|
| 29 | beam L4 7 + pods L3 4 + Tail Gun L3 2 + magnet 1 + shield 4 | **18** | E 16: shortfall 2, shield regen stops | E 14: shortfall 4, shield regen stops; weapons full |
| 30–31 | Swarm Tail L2 3.5 for the Tail Gun | **19.5** | E 16: 3.5, shield regen stops | E 14: 5.5 > 4: f = 14 ÷ 15.5 = **0.90** |
| 32–34 | Swarm Tail L3 4 | **20** | E 16: 4, shield regen stops | E 14: 6 > 4: f = 14 ÷ 16 = **0.875** |
| 35 | Mk V 22; pods L4 5 | **21** | E 18: 3, shield regen stops | E 16: 5 > 4: f = 16 ÷ 17 = **0.94** |
| a small generator | Mk III 16: beam L3 6 + pods L2 3 + Tail Gun L3 2 + magnet 1 + Shield Mk II 3 | **15** | E 12: 3, shield regen stops | E 10: 5 > 3: f = 10 ÷ 12 = **0.83** |
| a full build | beam L5 8 + pods L4 5 + Swarm Tail L3 4 + magnet 1 + Shield Mk V 6 + Engine Mk IV 2 | **26** | refused on the Mk V 22: the Act 6 question | — |

So the typical plan at L33 (20 of 20) loses its shield's regeneration to two leeches and an eighth of
its fire to three; a player who turned one weapon down a level, or kept 2 MW spare, loses only the
regrowth. The drain's 2 MW a leech is the lever if part A finds the bite too soft (3 MW a leech: the
typical plan's f 0.75 at three).

**The Mk V "Helix" note.** Q12 = c of round 60 (recommended, decided the user): the Mk V is **captured
Helix tech salvaged in L34** (a set-piece crate of the [Salvage Run](../../campaign/act-5-the-belt/README.md))
and is in the shop from the **L35 visit**, not from the act's first visit as the data's `act 5` reads;
part A sets that availability in [data.yaml](data.yaml) (a level unlock) once approved. Its **22 MW**
stays: +2 over the Mk IV, as the Mk III to IV step was first drafted, carries the typical plan at L35
(21 of 22) and one more MW; the full build above (26) is Act 6's question, where the Composite V and the
Reflex Shield arrive. Alternative: raise it to 24 now; rejected for the draft because Act 6 is not
designed and the step can be measured then.

## Implementation

- [x] Generator models and output values in the item data
- [x] Load vs output check in the hangar with a projected-load bar
- [x] Spare-power shield regen bonus
- [x] In-level output and the drain as under *Enemy drain effects*: the effective output, the shield's regeneration
  stopped first, the weapons' fire-rate factor (at least 0.5), the spare-power bonus from the effective output;
  deterministic (hashed only in a level with a drainer) — M9 part A (step A1: `PowerState`, `Sortie.drain`,
  `PowerDrainTest`; step A2: `drain` in [data.yaml](data.yaml), the flight's budget `SimSpecs.power`/`Flight.of`,
  `BeltKeysTest.theDrainFollowsTheHandPlansTable`; the Void Leech's latch calls `Sortie.drain` in part F)
- [x] The drain's feedback: the HUD gauge (see [HUD](../../ui/hud/README.md)), the muzzle flicker, the dimmed shield
  ring, the brown-out hum — M9 part A (step A3: `PowerRow`, `PlayerPasses`' flicker while f < 1, `ShipLooks`' ring at
  40 % while the regen is stopped, the `brownout-hum` loop while the load is over E, `BeltCues`; `ActFiveLooksTest`,
  `ActFiveSoundsTest`)
- [x] The Mk V "Helix" in the shop from the L35 visit (salvaged in L34), the balance plan's visits 29–35 — M9 part A
  (step A2: `available: L35`, `balance-plan.yaml` 29–35 as the hand plan above, the Act 5 levels' income budget(n) until
  they are built; `BeltCampaignTest`, `BalanceTest`, `tools/balance.py` exit 0)

## Open questions

- Q2 of [round 60](../../concept-rounds/README.md) (the drain, L33): recommended (a), the rules above.
  Alternatives: (b) flat, each leech −20 % fire rate whatever the loadout (cheap and readable, but the
  generator choice stops mattering); (c) each leech blacks out the fitted weapon with the highest draw
  until it is shaken off (very readable, punishes every loadout alike).
- J2 of round 60: `reference_dps` 120 → 144 with the Ion Beam L4 at the L29 visit (above).
- Q12 of round 60: the Mk V salvaged in L34 and sold from the L35 visit (recommended c); under (a) or
  (b) it is in the shop from the L29 visit as the data reads.

## Decisions

- 2026-09-30: Six generator tiers; top two are captured tech tied to the story.
- 2026-09-30: Enemy drain effects defined (Void Leech, L33): output reduction, shield regen
  stops first, then fire rate scales down.
- 2026-10-01: Overdrive ignores the power cap (user accepted the recommendation).
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-02: The generator models and the spare-power bonus moved into [data.yaml](data.yaml) (M2 data files); the model table is rendered from it.
- 2026-10-02: M3 part B2: generators are bought in the hangar (the old one goes to the inventory);
  a smaller one cannot be fitted while the load exceeds its output. The power bar shows the load,
  the projected load of the selected choice (red when it would not fit) and the output.
- 2026-10-02: M4 part A: the spare-power bonus applies in flight (`SimSpecs.regenBonus` on the shield's regen, from `spare_power` in the data) and shows on the HUD's power row.
- 2026-10-05: M4 part H docs reconciliation: implementation `done`; every item is ticked (the spare-power bonus since M4 part A).
- 2026-10-20 (user, round 44, X.k): **`art: n/a`**: the part has only the shop's icons, made with the shop (`tools/art/icons.py`), and no assets of its own.
- 2026-10-21 (the user's Act 3 playthrough: "I can't retrofit some of the other weapons, nor can I buy
  new things because my ship does not have enough power. I've fitted the gen 3 generator."; decided: more
  generator room in Act 3, measured): the **Mk III "Fusion" gives 16 MW** (was 14). 15 and 16 MW were
  measured on the balance plan (`ActPlaythroughTest`, `BalanceTest`, `tools/balance.py`,
  `:content:levelReport` for Levels 15–21): both leave the Act 3 credits and fights the same (easy +2.8 %,
  medium −2.4 % against the expert's bound; hard's accepted exception about −54 %), so 16 is taken: it fits
  Act 3's own items (Shield Mk III, a module, the Evasive thrusters' 2 MW) next to the Ion Beam L3, where
  15 fits one 1-MW item only. The Mk IV's 18 MW (Act 4) is a smaller step now; to revisit when Act 4 is
  designed (decided in round 48 (2026-10-22)). The weapons' [power dial](../README.md#power-budget) came with it.
- 2026-10-21 (Act 4 design phase, step m8-dsystems; **decided in round 48 (2026-10-22)**): the Act 4 hand plan written (the anti-sub loadout's power on the Mk III, Mk IV 18 and Mk IV 20, the reference DPS 22–28, the visits' cash); recommendation: the Mk IV "Tokamak" gives **20 MW** (data unchanged at 18 until decided).
- 2026-10-22 (M8 part A, step A2): the Mk IV "Tokamak" gives 20 MW in [data.yaml](data.yaml) (round 48, J1); the
  balance plan buys it at the L27 visit (16 of 20 MW, see [balance-plan.yaml](../balance-plan.yaml)).
- 2026-10-22 (M8 part A, step A2; accepted in round 50 (2026-10-22)): the Act 4 balance plan fits the free Sonar charge at L23 (the Airstrike is forbidden under water), keeps the beam fitted at L22 (the harpoon idle on the dry ice) and flies the loaner Bulwark at L27–28 (D7); it buys the Harpoon at L22 into the inventory, fits it at L23 and raises it to L3 there and L4 at L26 (the shop upgrades only a fitted item), the pressure hull at L24 and the Shield Mk III at L28 (the nanites stay out of the plan); planned levels from 22 without data earn a temporary budget(n) income (D8), removed in part I.
- 2026-10-22 (user, [round 50](../../concept-rounds/round-50/README.md) closed): B.5 accepted: the Act 4 balance plan's purchases (the Harpoon bought at L22 and fitted at L23 with the Depth Charges, both to L3; the Harpoon to L4 at L26; the pressure hull at L24; the free Sonar charge at L23; the Shield Mk III at L28; the loaner Bulwark at L27–28).
- 2026-10-24 (M8 AW, step AW2; the user's decision of 2026-10-23, the wording accepted in round 56 (2026-10-24)): nothing is
  sold in Act 4 ("Selling wastes a lot of credits … you need the ion beam again in a later level"): the L23 visit buys the
  Depth Charges L1 and fits the Harpoon L1 (bought at L22) and the Sonar Pulse, the beam and the Tail Gun kept; the L24
  visit upgrades the harpoon and the Depth Charges to L3 with one wing pod's Micro-missiles and the pressure hull
  ([balance-plan.yaml](../balance-plan.yaml)). `balance.py` (medium): left after the visit L22 1 980, L23 3 698, L24
  1 597, L25 4 436, L26 6 142, L27 1 770, L28 3 166; the forward DPS against the reference 1.04, 0.79, 1.00, 1.00, 1.07,
  1.03, 1.00 (energy weapons × 0.2 under water, `water(data, n, slug)`).
- 2026-10-27 (Act 5 design phase, step m9-d-systems; **approved in round 60**): the drain rules
  written as a final draft (Q2 = a: −2 MW a latched leech to −6, the shield's regeneration shed first, then the
  weapons' fire rate × E ÷ (load − shield draw), at least 0.5; the feedback and the intel warning); the Act 5 hand
  plan (the Ion Beam L4 at L29, the Swarm Tail at L30 and L32, Composite IV at L31, the Mk V and the pods L4 at
  L35), the proposed `reference_dps` 120 → 144 (J2) and the power check under two and three leeches; the Mk V
  "Helix" salvaged in L34 and sold from the L35 visit at 22 MW (Q12 = c).
- 2026-10-27 (user, [round 60](../../concept-rounds/round-60/README.md) closed): the Act 5 hand plan and the drain approved as drafted (D.15): Q2 **a** (a latched Void Leech drains the generator), J.2 (`reference_dps` 120 to 144), J.12 (2 MW per leech), Q12 **c**; the brown-out hum is S.10 a.
