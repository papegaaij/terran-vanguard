---
title: Shield Pylon
design: approved
implementation: in-progress
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../air/choir-herald, ../clamp-gun, ../../../ui/hud]
updated: 2026-10-31
---

# Shield Pylon

## Summary

An Ascendancy emitter mast on Callisto's fortress decks that projects a gold hex **dome** beside it: every ground unit
under the dome takes no damage. The pylon itself stands just outside its dome, open to every weapon. In Level 41 the
pylons are the fortress's **gates**: each section's scroll halts until its pylons fall.

## Design

### Stat block

**Draft** (Act 6 design phase, step D-enemies-B; decided 2026-10-31 (round 72)). A hand-written table: there
is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at its
first level from the proposed Act 6 reference DPS (judgement call J1, decided 2026-10-31 (round 72): L41 = 164): 240 HP is 1.5 s
(the top of the `medium` band, 0.4–1.5 s), so a gate of three is ≈ 7 s at the boss rule's 0.6 × the reference.

| Field | Value |
|---|---|
| Faction | Jovian Ascendancy; a **machine** (stunned 3 s by the EMP Burst) |
| Layer | `ground` |
| Size tier | `medium` |
| Size | 56×56 px (the mast seen from above, a gold emitter crown on a black octagonal base), hitbox 40×40 |
| Parts | single (the mast); the dome is an effect, not a part |
| Orientation | `fixed`; the crown's emitter turns toward its dome |
| HP | 240 (easy 180 / hard 312, from the global multipliers) |
| Armour / shield | none of its own: **not hardened** (every weapon breaks a gate, decided in the gap analysis), never covered by its own dome or another pylon's |
| Speed | 0 px/s (scrolls with the fortress; halts with the section) |
| Movement | `terrain` |
| Attack | none; its dome is its weapon (the [dome](#the-dome) below) |
| Formations | turret nest: a **gate** of 2–3 pylons per section, each with its dome over a group of guns |
| Weak points | the emitter crown (drawn only) |
| Effective traits | `anti-ground`, `area`, `piercing` |
| Credits | 45 (score 450 × chain) |
| Death | `medium` mechanical explosion; the crown topples and the dome **collapses** over 0.4 s (a shrinking gold ring into the crown); the burnt base stays as a wreck |
| First level / used in | L41 (Callisto Fortress, the pylon gates); later levels may use it (their docs decide) |
| Difficulty hooks | easy: dome radius 95 px, 2 pylons a gate; hard: dome radius 125 px, the dome back 1.0 s sooner after an EMP or `shield-breaker` hit |

### The dome

- **Shape.** A circle of radius **110 px** centred **140 px** from the pylon along the crown's facing, so the pylon
  stands 30 px outside the dome's edge; a thin gold beam joins the crown to the dome's rim (who feeds which dome reads
  at a glance). Drawn on the ground layer: a translucent gold hex dome at ≈ 20 % with a brighter rim, smooth (≥ 32
  steps, no ordered dither, drawn once: the 2026-10-30 dither audit), a slow shimmer running over the hexes.
- **What it covers.** Every **ground** unit whose centre lies inside it takes **no damage**: a shot that hits a
  covered unit is spent with a gold spark on the dome over it, a blast inside does nothing to covered units, a beam
  touching a covered unit sparks and stops there. `air`, `low-air` and `high-air` units are never covered (they fly
  above the dome), nor the pylons themselves. Shots that cross the dome without meeting a covered unit fly on, and
  enemy bullets leave it freely. The ship flies over it (ground never collides).
- **Covered units** show a faint gold hex sheen on their sprite and their running lights stay lit, so they read as
  protected, not dead; homing shots and Rook skip covered units while an uncovered target is on the screen.
- **Openers.** The pylon's death drops its dome for good. The **EMP Burst** drops every dome on the screen for the
  stun (a machine's 3 s), as it strips the [Choir Herald](../../air/choir-herald/README.md)'s aura rings. A
  **`shield-breaker`** hit on the pylon drops its dome for **2 s** (the emitter disrupted; the Tesla Coil Pod's play
  in Act 6). A dropped dome flickers back over 0.3 s.
- **Origin.** The Herald's `aura` made total and bound to the ground: the same idea (a projected protection that dies
  with its source), a different reach (an area, not a ring per unit) and no HP pool (invulnerable while it stands).

### The pylon gates (Level 41)

Level 29's rescue halt with the pylons as its holders (the [clamp guns](../clamp-gun/README.md)' `holds:` rule): a
fortress section's scroll eases to a halt as its gate reaches the field's upper half, the level clock stands while a
pylon of the gate stands, and the section's own waves run meanwhile. The HUD counts the gate down, **`PYLONS n/m`**
counting to the gate's total (the HUD lesson: a count shows its target; the [HUD](../../../ui/hud/README.md) owns the
line). The last pylon's fall opens the gate (the citadel's blast doors part ahead: Level 41's backdrop) and the
scroll eases back up over 2 s. The level document (D-levels-3) places the gates, their pylons and what they cover:
recommended, a dome over a Rail Bunker or a pair of [Warden Tanks](../warden-tank/README.md) and a turret nest, so the
gate is a choice of order (the pylons first, then the guns) and never a wall of invulnerable fire. Never the
[Halo Platform](../halo-platform/README.md) (its barrier is the gate there).

### Behaviour

- **Placement rule.** A pylon is never placed under its own or another dome, and a dome's centre is at least 140 px
  from the ship's band at the halt (y ≤ 360 in the 480×540 field), so covered guns never sit on top of the ship.
- **EMP.** Stunned 3 s: the dome drops for the stun (*Openers*).
- **Livery** (Q9 **a**, recommended, decided 2026-10-31 (round 72); the [enemies](../../README.md#factions-and-visual-language)
  README owns the rule): the fortress is elite, so **gloss black and heavy gold** with red running lights and a
  strong top-left rim light, readable over Callisto's dark ice (the readability check with the frames).

### Difficulty and level use

- Level 41: three gates of 2–3 pylons (draft; D-levels-3 decides), each halt ≈ 8–12 s typical.
- At most one gate's pylons on the screen at once.

### Sounds

Brief for the round-72 sound proposals (Freesound CC0/CC-BY, real recordings, no bubbly or popping sounds): the dome
a low steady electrical **hum** (a transformer or a large neon sign, a loop on the nearest dome); a hit on the dome a
short bright electrical zap with a glassy ring; the dome's collapse a falling hum into a hard electrical snap; the
pylon's death a metal mast's explosion and a crash.

## Concept art

Round 72's proposals (step ART-R-A, picked 2026-10-31; generator `tools/concept/enemies_r72.py shield-pylon`). Each sheet holds the source render, the sprites at play size with their states, the readability check over Level 41's brightest and darkest band beside the player with the measured gold share, and key frames; each GIF is the 480×540 play field. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/shield-pylon-r72-a.png](concept/shield-pylon-r72-a.png) | Shield Pylon a, "crown": the elite black base with a gold crown of six prongs and the emitter lens, a solid gold feed beam; the crown's 16 facings, the wreck, the dome over a covered gun on the darkest ice | chosen (round 72 A.5 a) |
| [concept/shield-pylon-r72-a.gif](concept/shield-pylon-r72-a.gif) | A pylon gate on Callisto's citadel: the scroll halts, PYLONS 0/2, shots spend on the domes over a Rail Bunker and two guns, the pylons broken in turn (the dome's collapse into the crown), the gate opens and the scroll resumes | chosen (round 72 A.5 a) |

Production art for concept round 73 (M10 part A, step ART-A): the dome's effects by `tools/art/act6_gear.py dome`
(waiting in `build/art-held/sprites/` until the pylon's `data.yaml` exists; the mast, crown and wreck are part G's).
Prompts: [concept/prompts.md](concept/prompts.md#pylon-dome-final-r73-a).

| File | What | Status |
|---|---|---|
| [concept/pylon-dome-final-r73-a.png](concept/pylon-dome-final-r73-a.png) | Final dome effects of A.5 a: the 232 px dome (lattice ≈ 20 %, rim, the shimmer's 12 frames), the spent shot's hit, the 0.4 s collapse ring, the feed strip, the covered sheen; over Level 41's ice with two covered Rail Bunkers and a stand-in mast (sheet) | proposed (round 73) |
| [concept/pylon-dome-final-r73-a.gif](concept/pylon-dome-final-r73-a.gif) | Shots spent on the dome, the mast destroyed, the ring collapsing into it, 1× | proposed (round 73) |
| [concept/dome-capture-r73-a.png](concept/dome-capture-r73-a.png) | Game capture (the A4 rig; a stand-in mast, ART-A's held dome frames): the dome 140 px along the crown with its feed and shimmer over the cream bands, round 72's grade and the rust band (the arc jumping to the mast) | proposed (round 73) |
| [concept/dome-capture-r73-a.gif](concept/dome-capture-r73-a.gif) | The dome scrolling through the pods' fight over the cream bands, 1× | proposed (round 73) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [ ] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers applied; tuned with `levelReport` in Level 41
- [x] The dome: ground units inside take no damage (shots spent with a spark, blasts and beams stopped), never itself or the air layers; the covered sheen; homing and Rook skip covered units (M10 part A, 2026-10-31: `DomeTest`; the dome's frames held until part G)
- [x] The openers: the pylon's death (collapse), the EMP (the stun), `shield-breaker` (2 s) (M10 part A, `DomeTest`)
- [x] The pylon gates: the rescue halt with the pylons as holders, the HUD's `PYLONS n/m` (M10 part A, 2026-10-31: `HeldGatesTest`, `GateTracker`; Level 41's placements are its part's)
- [ ] Production sprites (the mast, the crown at its headings, the wreck), the dome and its beam, the intel portrait
- [ ] Death effect, bounty and sounds per this spec (Freesound, no bubbly sounds)

## Open questions

- Whether a dome should also cover `air` units inside it (a parked gunship under a dome) was decided no (trivia): the
  user may want it for the fortress's last gate.

## Decisions

- 2026-09-30: roster row (a hardened pylon whose dome makes every enemy inside invulnerable; pylons open fortress
  sections).
- 2026-10-31 (Act 6 design phase, step D-enemies-B; decided 2026-10-31 (round 72)): promoted from the
  [ground roster](../README.md) to a draft spec; every number is a first draft.
- 2026-10-31 (the gap analysis's decision): **not hardened**, so every weapon breaks a gate (a hardened gate would
  lock out the loadouts without `anti-ground`); the pylon stands outside its own dome; the gates are Level 29's rescue
  halt with a `PYLONS n/m` count.
- 2026-10-31 (decided by me, trivia): the dome covers ground units only (air units would flicker in and out of
  cover); shots on covered units are spent, not passed through, so the dome reads as a wall; the EMP drops every dome
  for the stun and `shield-breaker` drops one for 2 s; no attack of its own.
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): Q9 a (the heartland livery; b and
  c rejected) and the document approved as drafted (D.10; `design: approved`); **A.5 a**, "crown", chosen (`art:
  chosen`; b "obelisk" in `concept/rejected/`). A dome that covers `air` units stays no (trivia).
