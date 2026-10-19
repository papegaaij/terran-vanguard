---
title: Deflector Pod
design: approved
implementation: in-progress
art: chosen
depends-on: [.., ../../generator, ../../shields, ../../ship, ../../../systems/economy]
updated: 2026-10-13
---

# Deflector Pod

## Summary

A defensive wing pod with no gun: it projects a small deflector field round its side of the ship
that swallows enemy bullets until its charges run out, then recharges. The first wing mount that
trades damage for survival, for Act 3's side- and all-direction levels and the blind dust storm.

## Design

The numbers the game reads are in [data.yaml](data.yaml) (M7 part A); the marked tables are rendered from it.

| Property | Value |
|---|---|
| Slot | Wing mount (per pod) |
| Traits | none (defensive); it deals no damage |
| Price | 1 600 cr |
| Upgrade cost L2 / L3 / L4 / L5 | 800 / 1 600 / 3 200 / 6 400 cr (the common factors on the price; sell-back per [economy](../../../systems/economy/README.md)) |
| Unlock | L18 (in the shop from the hangar visit before it) |
| Field | A half-disc round the ship's centre on the pod's side (left of the centre line for the left pod), its radius per level below; a pair covers a full disc |
| Sound family | none of the weapon families: a short dry deflect "tick" per bullet caught (the shield-hit sound pitched up and cut short) and a soft rising tone when a charge returns; never bubbly or popping ([sfx](../../../audio/sfx/README.md)) |

### Per level

<!-- data: weapon-levels -->
| Level | Field radius px | Charges | One charge back every | Bullets per 2 s (sustained) | Draw MW | Upgrade cost |
|---|---|---|---|---|---|---|
| 1 | 22 | 3 | 0.67 s | 3 | 2 | — |
| 2 | 24 | 3 | 0.55 s | 3.6 | 2.5 | 800 |
| 3 | 26 | 4 | 0.5 s | 4 | 2.5 | 1 600 |
| 4 | 28 | 4 | 0.45 s | 4.4 | 3 | 3 200 |
| 5 | 30 | 5 | 0.4 s | 5 | 3 | 6 400 |
| OD | 32 | 6 | 0.35 s | 5.7 | — (ignores cap) | — |

Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`; do not edit by hand. Values are per pod; it deals no damage. Bullets per 2 s: one caught on every charge's return. OD = the overdrive (20 s pickup).
<!-- /data -->

L1 is the roster's "up to 3 bullets per 2 s on its side"; draw 2 → 3 MW as in the roster. The
numbers are per pod.

### Behaviour

- **Catching.** An enemy bullet that enters the pod's field while the pod holds a charge is
  absorbed: the bullet is removed, one charge is used, and a short arc of the field lights up
  where it was caught. The field sits round the ship's centre, outside the hull's hit boxes on
  most of its edge, so it catches a bullet before it reaches the hull; it also catches near
  misses on its side, which use charges too (the trade-off of a bubble).
- **What it catches**: every enemy bullet on the player's plane (aimed, straight, spread, ring,
  the mortar blob as it reaches the plane). Not lasers or beams (`laser-sweep`, `laser-line`),
  `link`s and tendrils, contact attacks, ground blasts, hazards (furnace vents, wall contact) or
  a Lamprey's latch.
- **Charges.** A pod starts every attempt full. Charges return one at a time at the level's
  interval, also while bullets are being caught; the field is drawn only while it holds a charge
  (faint, brighter for a moment as a charge returns), so an empty pod is readable.
- **Pairs.** Two Deflector Pods give a full disc; each pod catches on its own side with its own
  charges. A bullet straight on the centre line goes to the pod with more charges left.
- **Shield and armour.** A caught bullet never reaches the shield, so it does not restart the
  shield's regen delay. The pod takes no damage and cannot be destroyed.
- **Scoring.** A caught bullet scores nothing and does not break the chain.
- **Spare power.** Its draw counts like a gun's, so the spare-power regen bonus falls by its
  draw: a Deflector pair at L1 draws 4 MW, 1 more than the typical pair of Micro-missile Pods L2.
- **In the dust storm** (Level 18, M6 = a) bullets stay visible, so the pod is a help, not a
  blind spot; on Level 17's side waves it covers the flank its side faces.

### Against the reference

It adds no DPS: fitting one in place of a Micro-missile Pod L2 costs 10 forward DPS (and 0.5
MW at L1). One pod catches up to 3 bullets per 2 s; at a `small` or `medium` bullet's 4–6 damage
([damage classes](../../../enemies/README.md#balancing-basis)) that is up to 6–9 shield points a second on its side, more than the Mk III shield's 4 a second regen. It is meant
for players who lose too much armour, not for the typical plan, which keeps its pods.

## Concept art

Round 36 showed no look for the pod, so M7 part A step ART-A proposes two for concept round 37
(`tools/art/deflector_pod.py`, production generator; prompts in [concept/prompts.md](concept/prompts.md));
round 37 chose **a** "field band" (P.1, user 2026-10-13), b is in `concept/rejected/`; a is written to
`assets/`: the pod on each wing
per banking frame (`pod-deflector-left/right`, offsets in `pivots/pods.json`), the field's half-disc
per level and side (`deflector-pod-field-<1..6>-<left|right>`) and the catch flash at 16 headings
(`deflector-pod-catch`).

| File | What | Status |
|---|---|---|
| [concept/deflector-pod-r37-a.png](concept/deflector-pod-r37-a.png) | A "field band": an emitter pod with a cyan dome, the field a thin cyan band with a faint glow inside, the catch lighting a short arc white with sparks (sheet) | chosen (round 37, P.1) |
| [concept/deflector-pod-r37-a.gif](concept/deflector-pod-r37-a.gif) | A pair at L1 catching a stream from the left until the left field is empty, charges returning (brighter for a moment) (motion) | chosen (round 37, P.1) |
| [concept/rejected/deflector-pod-r37-b.png](concept/rejected/deflector-pod-r37-b.png) | B "plate lattice": a pod with three emitter vanes, the field as five thin hex-cell plates with gaps, the catch flashing the plate and a spark ring (sheet) | rejected — round 37: a (field band) picked |
| [concept/rejected/deflector-pod-r37-b.gif](concept/rejected/deflector-pod-r37-b.gif) | B in the same scene (motion) | rejected — round 37: a (field band) picked |

## Implementation

- [x] Weapon data (`data.yaml`) with the numbers above (`hits: deflector`: every level a field `radius`, `charges` and `recharge` instead of shots) — M7 part A (step A2: `WeaponData`, validated by `ContentValidator` and its record checks)
- [x] The field per pod side, catching enemy bullets while charged; charge use and return (M7
  part A step A1: `vanguard.sim.Deflectors`, an `Armament.Mount` of its own kind; tests
  `DeflectorTest`, `ActThreeArsenalFlightTest`)
- [x] Field look (a faint arc, the catch flash, the charge-return brighten) and the deflect sounds
  (the art is drawn, a chosen in round 37 (P.1): `tools/art/deflector_pod.py`, M7 part A step ART-A; in
  flight step A6: `ArsenalLooks` draws the field per level, the overdrive's own, at 70 % and
  brighter for 8 steps as a charge returns, the catch flash by its heading; `FlightSounds` plays the
  tick and the return, round 37's picks: the tick a, the return b (user, 2026-10-13))
- [x] Power draw per level counted in the loadout; upgrades priced as listed (content: `Catalogue`/`Hangar` price and count it, M7 part A step A2; in the live shop since step A5 drew its hangar icon and emptied `Catalogue.AWAITING_ICONS`; tests `ActThreeShopTest`, `ActThreeHangarTest`)

## Open questions

- None open: round 36 answered it (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the weapons roster: a half-disc field per
  pod (22 → 30 px), charges 3 → 5 returning every 0.67 → 0.4 s, draw 2 → 3, 1 600 cr, unlock L18;
  bullets only (no lasers, beams, links, contacts or hazards). Pending the user's review in
  concept round 36.
- 2026-10-13 (user, round 36): approved (D.22); Q.23h yes: a caught bullet pays nothing back.
- 2026-10-13 (M7 part A, step A2): data.yaml written; the field numbers are the `levels` and the
  `overdrive` (`radius`, `charges`, `recharge`) under `hits: deflector` instead of a separate block, so
  every weapon keeps five levels and an overdrive for the shop's prices and draws (step agent's
  choice, pending the user's review). The per-level table is rendered from it.
- 2026-10-13 (user, round 37): look **a** "field band" chosen (P.1; `art: chosen`): an emitter pod with a
  cyan dome, the field a thin cyan band; the "plate lattice" (b) rejected (busier next to the drone pods
  and the shield ring) and moved to `concept/rejected/` (`tools/art/deflector_pod.py` routes it there).
  The data layout above accepted (R.3); the build choices kept (B.1 d–f: a mortar blob landing in a
  charged field is caught, a bullet is caught when its centre enters a field, the overdrive's raised
  cap empties when it ends; B.4 d: the test fire's slow dummy bullets); its sounds S.5 tick a and S.6
  return b "charge settle"; the capture accepted (K.1).
