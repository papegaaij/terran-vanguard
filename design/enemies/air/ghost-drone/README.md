---
title: Ghost Drone
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../story/twist, ../../../campaign/act-3-red-dust]
updated: 2026-10-13
---

# Ghost Drone

## Summary

A grey, angular, human-built drone flying with the Vrell: no biology, no markings, no trim. It
drops into a lane, strafes across the screen and fires one precise burst at the ship. Varga's
first sight of it in the Level 18 dust storm is **hint 1** of the twist.

## Design

### Stat block

Hand-written draft numbers at **medium** (M19: no `data.yaml` until its level is built; see the
[balancing basis](../../README.md#balancing-basis)). HP is in damage units (a Pulse Cannon L1 shot
does 2). Set at its first level from the proposed Act 3 reference DPS (M18 = a, approved in round 36:
L18 = 88): 14 HP is a time-to-kill of 0.16 s. As a `small` unit it keeps its HP in every later
level.

| Field | Value |
|---|---|
| Faction | Unmarked (Ascendancy) |
| Layer | `air` |
| Size tier | `small` |
| Size | 40×40 px, hitbox 28×24 |
| Parts | single |
| Orientation | `16 angles` (it banks into the strafe and faces its direction of travel) |
| HP | 14 (easy 10 / hard 18, from the global multipliers) |
| Armour / shield | none |
| Speed | descent 180 px/s; strafe 150 px/s |
| Movement | enters at the top, drops to its lane (120–220 px below the top edge) and `strafe`s across to the far side; pincer pairs enter from the sides at their lane (edge-warned 3 s ahead) |
| Attack | one 3-shot `burst` per pass, fired when it is within 60 px of the ship's x: the shots 0.15 s apart, each re-aimed at the ship; fast 210 px/s, `small` 4 |
| Formations | line abreast (3–5, the lanes 40 px apart), pincer (2 + 2) |
| Weak points | the red sensor eye (drawn only) |
| Effective traits | `forward`, `spread`, `homing`, EMP Burst (a machine: stunned 3 s, see [specials](../../../player/specials/README.md)) |
| Credits | 14 (score 140 × chain) |
| Death | `small` mechanical burst: sparks, a puff of black smoke, grey fragments (no Vrell ichor) |
| First level | L18 (Dust Storm) |
| Difficulty hooks | easy: 2-shot burst; hard: 4-shot burst and a second pass for line-abreast drones that survive (they turn at the edge and strafe back once) |

### Behaviour

- **Descent and strafe.** A front-entry drone drops straight to its lane, banks 90° and strafes
  across at a fixed height; it leaves through the far side edge. A line abreast enters
  together and splits half left, half right. Side-entering pincers (warned at their edge 3 s
  ahead) strafe in at their lane and leave through the opposite side.
- **Precise burst.** When it passes within 60 px of the ship's x it fires one fast 3-shot burst,
  each shot re-aimed: the tell is a 0.3 s red glint on its sensor eye before the first shot. One
  burst per pass, so the threat is predictable.
- **EMP.** A machine: the EMP Burst stuns it for 3 s; it keeps its momentum, drifts on its line
  and fires nothing.
- **Look and the storm.** Grey, angular, no Vrell glow: in Level 18's dust storm (M6 = a) it is
  harder to see than the Vrell, whose teal glow cuts through the haze; only its red eye and white
  running lights show at range. This is on purpose: the drone that does not belong is the one you
  cannot read at a glance.
- **Hint 1.** Its first appearance is scripted with Varga's line ("*That's not Vrell. That
  signature is... ours?*", the [twist](../../../story/twist/README.md)); the level document
  places it. No wreck beat; the drones fall into the dust.
- **Code fit.** `strafe` (the Spore Bomber's) and `burst` exist (§2.8 of the gap analysis).

### Difficulty and level use

- Burst size and the hard second pass per the stat block; the global multipliers apply.
- A level keeps Ghost Drones apart from Vrell air waves at first sight, so the grey machines read
  as a different force.

## Concept art

Proposals of concept round 36 (step A-units, listed in the [air](../README.md#concept-art)
Concept art table; prompts in [../concept/prompts.md](../concept/prompts.md), generator
`tools/concept/enemies_r36.py`): an a/b of the human-built drone, grey, no markings. Round 36 (2026-10-13): **a** "facet dart" chosen (U.4), b "crescent" rejected. **U.5 (accepted, for the art step):** the chosen variant is re-rendered with an emissive red eye of at least 2×2 px and two white wing-tip running lights, their glow drawn additively like the Vrell glow, so the dust-storm silhouette works from the sprite itself. Done 2026-10-13: the sheet and GIF are re-rendered with it (checked at 1× over the Level 18 storm, look b: the red eye and both white tips read on the dark silhouette outside the clear radius).

| File | What | Status |
|---|---|---|
| [../concept/ghost-drone-r36-a.png](../concept/ghost-drone-r36-a.png) | Ghost Drone A, facet dart — grey human-built drone with the U.5 fix (2×2 px red eye, white wing-tip running lights, additive glow), 16 headings, next to a Talon and the player (sheet) | chosen (round 36, U.4; re-rendered with U.5) |
| [../concept/ghost-drone-r36-a.gif](../concept/ghost-drone-r36-a.gif) | Ghost Drone A with the U.5 eye and running lights — line abreast, then a pincer, over dusty Mars plains (motion) | chosen (round 36, U.4; re-rendered with U.5) |
| [../concept/rejected/ghost-drone-r36-b.png](../concept/rejected/ghost-drone-r36-b.png) | Ghost Drone B, crescent — grey drone with a forward-swept wing (sheet) | rejected — round 36: a (facet dart) picked |
| [../concept/rejected/ghost-drone-r36-b.gif](../concept/rejected/ghost-drone-r36-b.gif) | Ghost Drone B — the same scene as A (motion) | rejected — round 36: a (facet dart) picked |

## Implementation

- [ ] Descent to a lane, banked `strafe` across, exit at the far side; side entries edge-warned
- [ ] One 3-shot re-aimed burst per pass within 60 px of the ship's x, 0.3 s eye glint tell
- [ ] Hard: survivors of a line abreast strafe back once
- [ ] EMP stun 3 s (machine)
- [ ] Mechanical death, bounty and score
- [ ] Data file with the stat block (when Level 18 is built)
- [ ] Production art (round of part E)

## Open questions

- None open: round 36 answered it (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the [air roster](../README.md) to a draft
  spec. M18 = a taken as the recommended option, pending the user's review in round 36. Our
  proposals: 14 HP, one re-aimed 3-shot fast burst per pass with a 0.3 s glint, lanes
  120–220 px, bounty 14, the deliberately weak readability in the storm.
- 2026-10-12 (merge): the round-36 a/b proposals are listed under Concept art (`art: proposed`).
- 2026-10-13 (user, round 36): approved (D.16). Q.22h yes: the intel shows "unidentified contacts" until Level 18 is first flown. U.4 = a: the facet dart chosen, the crescent (b) rejected and moved to `../concept/rejected/`. U.5 accepted: the chosen sprite gets an emissive red eye (≥ 2×2 px) and two white wing-tip running lights (additive glow), made by the art step that updates `enemies_r36.py`.
- 2026-10-13: U.5 done in the concept art: `ghost-drone-r36-a.png/.gif` re-rendered by `enemies_r36.py ghost-drone-a` (a 2×2 px red eye and one white pixel per wing tip set after the palette step, plus the additive glow layer `ghost_glow`); the production sprite (with its additive glow frames) follows in the art step.
