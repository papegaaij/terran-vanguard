---
title: Generator
design: approved
implementation: done
art: n/a
depends-on: [../weapons, ../shields]
updated: 2026-10-22
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
| Mk V "Helix" | 22 MW | 18 000 | act 5 (Helix Dynamics tech, captured) |
| Mk VI "Choir Core" | 27 MW | 32 000 | act 7 (Vrell tech) |
<!-- /data -->

- Buying a generator replaces the current one. The old one goes to the inventory and can be
  sold (see [economy](../../systems/economy/README.md)).
- Spare power bonus: +10 % shield regen per spare MW, max +50 %.
- In a level the generator feeds the HUD power gauge (see [HUD](../../ui/hud/README.md)).
  Normally its output is constant, but some enemies can drain it (below).

### Enemy drain effects

Some enemies attack the power budget instead of the hull, first the
[Void Leech](../../enemies/space/README.md) (introduced in L33). Rules (first draft):

- An attached drainer lowers the **effective output** by 2 MW (Void Leech); drains stack up to
  −6 MW. Shaking it off (moving fast) or shooting it restores the output at once.
- While the load is at or below the effective output, nothing changes except a smaller
  spare-power bonus.
- When the load exceeds the effective output: **shield regeneration stops first**; if the
  shortfall is larger than the shield's draw, **weapon fire rate** drops in proportion to
  output ÷ remaining load. Armour, specials and the escort are never affected.
- The HUD power gauge shows the drained part in a warning colour and flashes when the load
  exceeds the output. A heavy loadout on a small generator is the most vulnerable, which is the
  point of L33 (see its threat profile in [act 5](../../campaign/act-5-the-belt/README.md)).

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

## Implementation

- [x] Generator models and output values in the item data
- [x] Load vs output check in the hangar with a projected-load bar
- [x] Spare-power shield regen bonus

## Open questions

- None open.

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
- 2026-10-22 (M8 part A, step A2; pending the user's review in round 50): the Act 4 balance plan fits the free Sonar charge at L23 (the Airstrike is forbidden under water), keeps the beam fitted at L22 (the harpoon idle on the dry ice) and flies the loaner Bulwark at L27–28 (D7); it buys the Harpoon at L22 into the inventory, fits it at L23 and raises it to L3 there and L4 at L26 (the shop upgrades only a fitted item), the pressure hull at L24 and the Shield Mk III at L28 (the nanites stay out of the plan); planned levels from 22 without data earn a temporary budget(n) income (D8), removed in part I.
