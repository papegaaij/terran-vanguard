---
title: Shard Drone
design: draft
implementation: not-started
art: proposed
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials]
updated: 2026-10-27
---

# Shard Drone

## Summary

A small spinning Ascendancy drone flown in groups of 3–4 joined by damaging energy **links**: a moving
barrier that sweeps down the field. Every shot passes through a link; killing a drone breaks its links,
and the EMP drops them all.

## Design

### Stat block

**Draft** (Act 5 design phase, step D-enemies-B; pending the user's review in round 60). A hand-written
table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2),
set at its first level from the proposed Act 5 reference DPS (judgement call J2: L31 128). As a `small`
unit it takes the **beam-era ×2** on its HP (the basis's `beam_era_hp`, from Level 18): 14 is flown as
28, 0.22 s at the reference, inside `small`'s ≤ 0.3 s. Fire rates are tuned per level with `levelReport`.

| Field | Value |
|---|---|
| Faction | Ascendancy; a **machine** (stunned 3 s by the EMP Burst) |
| Layer | `space` (the `air` layer in vacuum) |
| Size tier | `small` |
| Size | 40×40 px, hitbox 26×26 |
| Parts | single drone; a group's links are not parts (no HP, never hit) |
| Orientation | `radial` (a three-bladed gold shard spinning 360°/s) |
| HP | 14 (easy 10 / hard 18, from the global multipliers); ×2 in the beam era: 28 (easy 21 / hard 36) |
| Armour / shield | none (not on the [enemy shields](../../README.md#enemy-shields)' Act 5 list) |
| Speed | the group's: 45 px/s down the field (line), 40 px/s (circle) |
| Movement | the group flies as one: **line abreast** (3–4 drones 120 px apart) descending with a `sine` sway of 60 px over 5 s; or **circle** (4 drones on a 64 px radius turning 60°/s round a centre that descends and drifts toward the ship's x at 10 px/s) |
| Attack | `link`: an energy beam between neighbouring drones (line: 2–3 links; circle: 4, closed), 8 px drawn, a 6 px contact band, `medium` 6 per touch with a 0.5 s cooldown, the ship not pushed; lit 0.8 s after the group's last drone enters (the flicker is the tell). Each drone also fires an `aimed` bolt every 3 s, staggered across the group (standard 150 px/s, `small` 4) |
| Formations | line abreast (3–4), circle (4) |
| Weak points | none (the hub's red light is drawn only) |
| Effective traits | `piercing`, `spread`, `forward` |
| Credits | 14 (score 140 × chain) |
| Death | `small` mechanical burst, gold shards; its links snap with a spark |
| First level / used in | L31; later Act 5 levels may use it (their docs decide) |
| Difficulty hooks | easy: groups of 3 only, bolts every 4 s; hard: groups of 4 only, links lit after 0.5 s, bolts every 2.4 s |

### Behaviour

- **Links are energy** (the gap analysis's decision; the shared [air links](../../README.md#air-links) rule): every shot passes through a link, the beam
  included (unlike the [Tendril Anchor](../../ground/tendril-anchor/README.md)'s tendril, which the beam
  burns). A link cannot be destroyed, only broken by killing one of its two drones: killing the middle
  drone of a line drops both its links, an end drone only its own.
- **The barrier.** A lit line sweeps down the field; the player kills a drone to open it, or slips round
  an end. A link longer than 150 px is never drawn (the formation keeps them shorter); the circle's
  closed square turns, so its gaps open only by killing.
- **EMP.** The [EMP Burst](../../../player/specials/README.md#emp-burst-l15) drops every link on the
  screen for its stun (the EMP's "strips shields and aura rings", extended to links); the drones drift
  and stop firing for 3 s, then the links relight with their 0.8 s flicker.
- **Who links hurt.** Links touch only the play plane: they hurt the ship, never Rook nor other enemies.
  **One exception** to the shared rule's "never allies" (Q4 a lists the links among the tug's attackers): in L31 a group that crosses the mining tug drags its links over the hull (the target-the-objective
  hook, Q4 a): a link touching a tug section does **4** per touch with the same 0.5 s cooldown per link
  and section (the [mining tug](../../../allies/mining-tug/README.md)'s doc owns the number, the level doc
  the HP).
- **Code fit.** The `Links` of the tendril anchors, generalised from fixed anchors to moving air units
  (gap analysis §2.8, size S–M).

### Difficulty and level use

- At most two linked groups on the screen at once; a group always leaves a way round or a drone to kill
  in the ship's reach before its link reaches the ship's row.
- L31 uses them against the tug and as barriers in the open belt.

### Sounds

Brief for the round-60 sound proposals (Freesound CC0/CC-BY, real recordings, no bubbly or popping
sounds): the links an electrical transformer hum (a steady 50/60 Hz buzz) that crackles when lit; the
drone a small turbine whir; a broken link a sharp electric snap; the death a crisp metal crack.

## Concept art

None yet: proposals (a/b) in round 60 (step ART-P-units, `tools/concept/enemies_r60.py`): a gloss-black
hub with three gold shard blades and a red hub light (Q11 a), the link a pale gold energy beam (gold, not
a reserved bullet hue).

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (pending the user's review), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 31 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/shard-drone-r60-a.png](concept/shard-drone-r60-a.png) | Shard Drone A, tri-blade, beam links — three long gold shard blades round a black hub with a red light; links: a crimson beam with a dark rim and a pale core; strong rim (sheet) | proposed |
| [concept/shard-drone-r60-a.gif](concept/shard-drone-r60-a.gif) | Shard Drone A, tri-blade, beam links — three long gold shard blades round a black hub with a red light; links: a crimson beam with a dark rim and a pale core; strong rim (motion) | proposed |
| [concept/shard-drone-r60-b.png](concept/shard-drone-r60-b.png) | Shard Drone B, tri-prism, lightning links — a black triangular frame with a gold prism at each corner; links: crackling crimson lightning with a gold core (sheet) | proposed |
| [concept/shard-drone-r60-b.gif](concept/shard-drone-r60-b.gif) | Shard Drone B, tri-prism, lightning links — a black triangular frame with a gold prism at each corner; links: crackling crimson lightning with a gold core (motion) | proposed |

## Implementation

- [ ] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers and the beam-era factor applied; tuned with `levelReport`
- [ ] Group movement (line abreast with a sway, the turning circle), deterministic and allocation-free
- [ ] Links between moving units (`Links` generalised): lit after the tell, contact with a cooldown, broken by a drone's death, dropped by the EMP for its stun; every shot passes through
- [ ] The tug hook in L31 (with the level and the tug)
- [ ] Production sprites (the spin, the link beam), the intel portrait
- [ ] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds)

## Open questions

- Q4 (pending round 60): the L31 escort, **a** recommended (a huge mining tug the links can touch);
  with **b** (ore haulers as a convoy) or **c** (a tug on the play plane) the tug hook changes with it.
- Q11 (pending round 60): the Ascendancy look on the belt, **a** recommended; alternatives **b**
  gunmetal with gold, **c** black silhouettes with gold accents only.

## Decisions

- 2026-10-27 (step D-enemies-B): drafted for the Act 5 design phase from the roster row and the gap
  analysis; every number is a first draft, pending the user's review in round 60.
- 2026-10-27 (the gap analysis's decision): links are energy (every shot passes through, the beam too),
  the EMP drops them for its stun, killing a drone breaks its links; touch damage 6.
- 2026-10-27, design phase: a link's touch on the mining tug does **4**, not 6 (reconciled with the tug's
  doc, which owns the number; step m9-d-act, pending the user's review in round 60). The ship's touch
  stays 6.
