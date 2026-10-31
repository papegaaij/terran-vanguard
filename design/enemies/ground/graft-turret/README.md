---
title: Graft Turret
design: approved
implementation: in-progress
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../spine-turret, ../../air/coilwyrm]
updated: 2026-10-31
---

# Graft Turret

## Summary

An Ascendancy deck turret overgrown with Vrell tissue, on Aurelia's platforms. Destroyed, it exposes a glowing
tissue **root** that pulses for 5 s: kill the root in that window and the turret stays dead, miss it and the turret
**regrows** and fires again. The first Act 6 hybrid on the ground (Level 37).

## Design

### Stat block

**Draft** (Act 6 design phase, step D-enemies-B; decided 2026-10-31 (round 72)). A hand-written table: there
is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at its
first level from the proposed Act 6 reference DPS (judgement call J1, decided 2026-10-31 (round 72): L37 = 148). As a `small` unit
it takes the beam era's ×2 on top ([beam-era HP](../../README.md#balancing-basis)): the turret's 12 plays as 24
(0.16 s), the root's 8 as 16 (0.11 s), so a turret and its root fall to one short burst of aimed fire.

| Field | Value |
|---|---|
| Faction | Hybrid (Ascendancy hardware, Vrell tissue); a **machine** for the EMP (its gun is Ascendancy) |
| Layer | `ground` |
| Size tier | `small` |
| Size | 44×44 px, hitbox 32×32; the root 24×24 on the turret's centre (under it) |
| Parts | turret (`vital` of the turret), root (`vital` of the unit: hittable only while exposed) |
| Orientation | the turret's barrel `32 angles` (turning 120°/s); the base fixed |
| HP | turret 12 · root 8 (easy 9 · 6 / hard 16 · 10, from the global multipliers); a regrown turret has its full HP |
| Armour / shield | none; not hardened; the root cannot be hit while the turret stands over it |
| Speed | 0 px/s (scrolls with the deck) |
| Movement | `terrain` |
| Attack | `aimed` **pair** (two shots 4° apart, the Ascendancy's precision) every 2.2 s along its barrel, standard 160 px/s, `small` 4; tell: a 0.25 s red glint on the muzzle; silent once the ship is more than 110° off its facing |
| Formations | turret nest (3–6), on deck edges and dome rims; never more than 6 Graft Turrets on the screen |
| Weak points | the root (exposed for the window), its teal glow pulsing |
| Effective traits | `anti-ground`, `area`, `beam` (a beam that kills the turret burns the root under it in the same touch) |
| Credits | 15 once (score 150 × chain), paid at the turret's **first** death; the root pays a bonus of **5** when it dies; a regrown turret pays nothing |
| Death | turret: a `small` mechanical burst with sparks, the barrel falls off and the root shows; root: a fibrous tearing crumble of teal tissue (no pop), a scorched stump with withered tendrils stays on the deck |
| First level / used in | L37 (Fall of Aurelia, Aurelia's decks); later Act 6 levels may use it (their docs decide) |
| Difficulty hooks | easy: the window 6 s, a pair every 2.8 s; hard: the window 4 s, a burst of 3 every 1.8 s, the regrowth 0.6 s |

### Regrowth

The roster's mechanic, written here as its one definition (the shared rules in the [enemies](../../README.md)
README link it):

1. **The turret dies** (its 12 HP gone): it pays its bounty (the first time only), counts as a kill once, and its
   shell bursts open. The **root** is exposed at once: a knot of teal tissue under the wreck.
2. **The window** (5 s at medium): the root pulses, slowly at first and faster as the window closes (1 Hz → 4 Hz:
   the timer is the pulse, the ground README's open question answered yes), with a thin teal ring round it shrinking
   to nothing over the window. Every weapon hits the root (`anti-ground` ×2, homing shots and Rook pick it as a
   ground target).
3. **The root dies in the window**: the unit is gone for good; the root pays its bonus, the stump stays.
4. **The window closes**: the turret **regrows** over **0.8 s** (the tell: tissue knits over the wreck and the
   barrel pushes out of it, teal-veined; not hittable while it knits), then it is a turret again with its full HP,
   firing after a further 0.5 s. Killing it again pays nothing and opens the window again; the cycle repeats until
   the root dies or the turret scrolls off.

Rules round it: a regrown turret never counts toward the level's enemies, density or kill ratio a second time;
a blast that kills the turret does not reach the root (it was covered when the blast landed); a beam held on the
spot burns the root in the next touches (the beam is the clean answer, as for [regeneration](../../README.md#regeneration)).
The [Coilwyrm](../../air/coilwyrm/README.md)'s regrowing head is the precedent; this regrowth is its own mechanism
(a two-part ground unit with a respawn, gap analysis §2.10: small–medium).

### Behaviour

- **Hybrid look** (Q9 **a**, recommended, decided 2026-10-31 (round 72); the [enemies](../../README.md#factions-and-visual-language)
  README owns the rule): an Ascendancy deck turret in gunmetal with gold trim, **tissue first** (A.4 b, chosen in round 72): a third of the
  dome overgrown, a fleshy collar and six roots across the deck, a bigger root, teal seams glowing brightest at the root
  (gold 28 %, teal 6 %; regrown 10 %); a regrown turret is more tissue than metal (the barrel veined teal), so a turret that
  has come back reads as such.
- **The three-way war.** Before the turn in Level 37 the turrets are Ascendancy guns with Vrell growth on them; they
  fire only at the ship. The turn changes nothing in their fire (the [three-way rule](../../README.md#the-three-way-war) is the
  enemies README's: chosen Vrell waves hunt chosen Ascendancy waves; the Graft Turret is neither hunter nor prey).
- **EMP.** Stunned 3 s (its gun is a machine): no fire. The root's window is biological and keeps running.
- **Rook** shoots exposed roots like any ground target (his target rules), so he helps close a nest.

### Difficulty and level use

- Level 37: nests of 3–6 on Aurelia's deck edges and dome rims, where `area` and `anti-ground` weapons sweep a nest
  and the beam burns roots; the level's draft places them (D-levels-1).
- Never more than 6 on the screen, so the windows can be followed.

### Sounds

Brief for the round-72 sound proposals (Freesound CC0/CC-BY, real recordings, no bubbly or popping sounds): the shot
the Ascendancy's small gun pair; the shell bursting a short metal crack with a spark hiss; the root's pulse a low
muffled heartbeat thump, quickening with the window; the regrowth a **creak** (leather or rope stretched and
twisting; never wet or bubbly, the [regeneration](../../README.md#regeneration) sound's rule); the root's death a fibrous
tearing crunch.

## Concept art

Round 72's proposals (step ART-R-A, picked 2026-10-31; generator `tools/concept/enemies_r72.py graft-turret`). Each sheet holds the source render, the sprites at play size with their states, the readability check over Level 37's brightest and darkest band beside the player with the measured gold and teal shares, and key frames; each GIF is the 480×540 play field. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/graft-turret-r72-b.png](concept/graft-turret-r72-b.png) | Graft Turret b, "tissue first": a third of the dome overgrown, a fleshy collar and six roots across the deck, a bigger root bulb; the same cycle | chosen (round 72 A.4 b) |
| [concept/graft-turret-r72-b.gif](concept/graft-turret-r72-b.gif) | The same scene as a | chosen (round 72 A.4 b) |
| [concept/regrowth-capture-r73-a.png](concept/regrowth-capture-r73-a.png) | Game capture (the A4 rig; stand-in sprite, the root given 3 000 HP so the window runs out under the beam): killed, the root exposed with its pulse and shrinking ring, the knit and the regrown turret, over the cream bands, 1× | chosen (round 73 F.6 accept) |
| [concept/regrowth-capture-r73-a.gif](concept/regrowth-capture-r73-a.gif) | The same, 5 s at 15 fps, 1× | chosen (round 73 F.6 accept) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [ ] Stat block loaded from a `data.yaml` (when the design is approved); global difficulty multipliers and the beam-era factor applied; tuned with `levelReport` in Level 37
- [x] Regrowth: the exposed root, the window with its pulse, the regrowth's tell, the full-HP return, the cycle (M10 part A, 2026-10-31: `RegrowthTest`, the root's pulse and the knit step A3a's)
- [x] Bounty once, the root's bonus, nothing for a regrown turret; one kill and one density count (M10 part A, `RegrowthTest`)
- [ ] Production sprites (the turret at 32 angles, the wreck and the root's pulse, the regrowth frames, the regrown look, the stump), the intel portrait
- [ ] Death effects and sounds per this spec (Freesound, no bubbly sounds)

## Open questions

- None open.

## Decisions

- 2026-09-30: roster row (Hybrid, regrows 5 s after death unless its root dies).
- 2026-10-31 (Act 6 design phase, step D-enemies-B; decided 2026-10-31 (round 72)): promoted from the
  [ground roster](../README.md) to a draft spec; every number is a first draft.
- 2026-10-31 (judgement call **J7**, decided 2026-10-31 (round 72)): the turret pays once, the root shows its timer as a quickening
  pulse (the ground README's open question: yes), regrown turrets pay nothing.
- 2026-10-31 (decided by me, trivia): the root lies under the turret, exposed only by its death (so the window is the
  mechanic, not a second target); a blast cannot kill both at once, a beam can; the EMP stops the gun but not the
  root's window; the unlimited cycle ends with the root or the scroll.
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): Q9 a (the hybrid livery), Q3 a
  with c's backdrop (the three-way war: the turret is neither hunter nor prey; b full faction fire rejected) and J7
  (the turret pays once) as drafted; the document approved (D.10; `design: approved`); **A.4 b**, "tissue first",
  chosen (`art: chosen`; a "metal first" in `concept/rejected/`), the look above.
- 2026-10-31, round 73: the regrowth capture accepted (F.6); call C.5 accepted (the root cannot be hit on the step it is exposed).
