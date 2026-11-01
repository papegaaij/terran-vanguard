---
title: Harrow
design: approved
implementation: done
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../art-direction, ../../../campaign/act-6-jovian-storm]
updated: 2026-11-01
---

# Harrow

## Summary

A hybrid kamikaze (Level 38): a gunmetal dart carrying a swollen sac of Vrell biomass. It comes in from every
edge, locks on with a clear tell and rams; however it dies (shot, ramming, struck by lightning) the sac bursts
into a slow ring of 8 bullets. Kill it at range.

## Design

### Stat block

**Draft** (Act 6 design phase, decided 2026-10-31 (round 72)). Rendered from its [data.yaml](data.yaml) (M10 part D,
step D2; its header lists the build's readings). Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at
its first level from the proposed Act 6 reference DPS (judgement call J1, L38 = 152). As a `small` unit it takes the
[beam-era](../../README.md#balancing-basis) ×2 on its HP and keeps it in every later level. Speeds are first
values: the build tunes them per level with `levelReport`.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Hybrid (machine: the EMP Burst stuns it longest) |
| Layer | `air` |
| Size tier | `small` |
| Size | 34×34 px, hitbox 22×22 |
| Parts | single |
| Orientation | `16 angles` (the nose along its heading) |
| HP | 8 (easy 6 / hard 10, from the global multipliers) |
| Armour / shield | none; HP 8 base flies at **16** with the beam era's ×2 (0.11 s at the L38 reference of 152) |
| Speed | 110 px/s hunting; **300 px/s** ramming |
| Movement | enters from any edge (each first side or rear entry edge-warned 3 s ahead); `chase` at 110 px/s (turn rate 90°/s) for 1.5–2.5 s (seeded); then the **lock tell 0.6 s** (it slows to 50 px/s, the sac pulses fast and brightens, the nose swings onto the ship); then `kamikaze`: a **straight ram** along the bearing at the tell's end, no steering; a miss flies on and out through the far edge (no burst off the screen). The EMP Burst stuns it 3 s: it drifts on along its line and does not ram |
| Attack | `kamikaze`: contact `small` = 10 (destroyed by the impact); **`death-burst`**: a `ring` of **8** `small` bullets (4 each) at a **slow 90 px/s** from where it dies, on **every death on the screen**: shot, ram, lightning, the Smart Bomb, Rook. The ring is turned so that the ship's bearing falls **between two bullets**; after a ram its bullets cannot hit the ship for their first 0.25 s (they leave the hull) |
| Formations | stream (3–6), cross (4–8) (a stream of 3–6 from alternating edges, 0.6 s apart; a cross of 1–2 per edge from all four edges, warned on every edge); at most 6 on the screen, at most 2 in their tell or ram at once |
| Weak points | the payload sac (drawn only) |
| Effective traits | `homing`, `beam`, `spread`, `side`, `rear` |
| Credits | 12 (score 120 × chain), however it dies on the screen (a ram pays as a kill, as the Skitter's; lightning pays, J3) |
| Death | the sac splits in a teal flash with a few tissue flecks, then the ring; the dart's gunmetal shell tumbles away (presentation) |
| First level / used in | L38; the storm layer, all directions, among the lightning; its later Act 6 levels per the level drafts |
| Difficulty hooks | easy: tell 0.8 s, ram 240 px/s, at most 4 on the screen; hard: tell 0.5 s (the floor), ram 340 px/s, ring bullets 100 px/s |
<!-- /data -->

### Behaviour

- **Death-burst on contact** (the shared [`death-burst`](../../README.md#attack-pattern-vocabulary) of Act 6): the
  ring comes on any death, a ram included, so a Harrow is never a free kill up close: shot at point-blank range its
  ring opens around the ship, rammed it costs the contact damage and leaves a ring the ship must not turn back
  into. The fair answer is range: the homing, spread and rear weapons take it far away, the beam on its line.
- **The tell is the rule.** A ram never starts without the full 0.6 s lock tell (≥ 0.5 s at every difficulty),
  and its line is the bearing at the tell's end: a ship moving sideways through the ram's last 0.3 s is missed.
  A stream staggers its tells (0.6 s apart), so one ram comes at a time.
- **Lightning** (Level 38, J3, the [Jovian world](../../../world/jovian/README.md)'s rule): a strike that crosses a
  Harrow kills it, pays the player and sets off its ring; a level may time its strikes beside a Harrow stream on
  purpose.
- **Bullets on the screen**: 6 Harrows bursting at once would be 48 bullets; the cap of 2 rams at once and the
  stream's spacing keep the rings apart (the level's `levelReport` counts the enemy shots a minute).
- **Look** (Q9 a, the hybrid livery): a short gunmetal dart with gold leading edges; behind the nose a swollen sac
  of dark teal tissue with a teal glow through its veins, pulsing slowly while it hunts and fast in the tell.
- **Sounds** (brief for the audio step): a thin rising wind-whistle while it hunts (a real jet flyby or a
  whistling dive from Freesound, CC0/CC-BY); the tell a short rising whine; the ram a hard whoosh; the burst a
  dull heavy thud with a crackle (a muffled blast or a firework mortar), never a pop or a wet sound.

### Difficulty and level use

- At most 6 on the screen; never with another `death-burst` unit's rings in the same seconds.
- A cross is warned on every edge and gives each Harrow its own lane.

## Concept art

Round 72's proposals (step ART-R-A, picked 2026-10-31; generator `tools/concept/enemies_r72.py harrow`); round 76's
production frames (M10 part D, step ART-D, pending the user's review: the sac's glow as one additive layer the game
draws at the state's strength, the lock tell's halo as a second). Each sheet holds the source render, the sprites at play size with their states, the readability check over Level 38's brightest and darkest band beside the player with the measured gold and teal shares, and key frames; each GIF is the 480×540 play field. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/harrow-r72-a.png](concept/harrow-r72-a.png) | Harrow a, "dart and sac": a gunmetal dart with gold leading edges and a swollen tissue sac on its back; 16 headings; the sac dim / hunting / tell / tell peak; its ring bullet beside the player | chosen (round 72 A.3 a) |
| [concept/harrow-r72-a.gif](concept/harrow-r72-a.gif) | Four Harrows from three edges in the storm: one shot at range (its ring of 8 with the gap on the ship's bearing), one lock tell and a missed ram, one killed by a lightning strike (J3), one shot in its tell | chosen (round 72 A.3 a) |
| [concept/harrow-final-r76-a.png](concept/harrow-final-r76-a.png) | Final frames (M10 part D, `tools/art/storm_layer_units.py harrow`; held in `build/art-held/sprites/` until its data.yaml): the 16 headings at 2× hunting over Level 38's production storm, the sac by state (dim 0.2, hunting 0.45–0.75, the tell 0.75 → 1.0 with its halo, the lock's flare, the ram 0.9), the tell's 12 frames, the burst's 8 over the small explosion, the shell's tumble, the read at 1× over the darkest and brightest windows of three sections beside the ship; teal 8 % hunting, 14 % at the tell's peak, gold 24 % (sheet) | proposed (round 76) |
| [concept/harrow-final-r76-a.gif](concept/harrow-final-r76-a.gif) | Over Level 38 at t 30–34: one Harrow from the left hunts, tells and rams past the sidestepping ship; one from the top shot at range, its ring of 8 opening with the gap on the ship's bearing, the shell tumbling away (the ring's bullets are stand-ins: the game's `small` bullets) | proposed (round 76) |
| [concept/harrow-capture-r76-a.png](concept/harrow-capture-r76-a.png) | In the game (step D5, Level 38's parked medium run; [recipe](concept/prompts.md#harrow-capture-r76-a)): the first lure, the t=42.5 stream dragged through the told t=43.5 column, the strike killing one, its ring opening | proposed (round 76 F.1) |
| [concept/harrow-capture-r76-a.gif](concept/harrow-capture-r76-a.gif) | The same, t 42.7–46 | proposed (round 76 F.1) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [x] Stat block loaded from a `data.yaml`; global difficulty multipliers and the beam-era factor applied; re-measured with `levelReport` at L38
- [x] Chase → lock tell → straight ram, the miss flying out; the `death-burst` ring on every death on the screen (a ram and a lightning kill included), turned to put the ship between two bullets, the 0.25 s grace after a ram; deterministic and allocation-free
- [x] Entries from every edge with the edge warnings (step D1: dashers stream in from the rear and fly the cross; `Level38Test` checks the warnings on every edge); the caps on the screen and in the ram (authored, measured in `levelReport` and asserted on `Level38Test`'s flown runs; the script flown unharmed exceeds them: round 76 L.2)
- [x] Production sprites (16 headings, the sac's pulse, the burst) via `tools/art/storm_layer_units.py` (step ART-D, in the game since D4: `Level38LooksTest`); death effect, bounty and sounds (Freesound, CC0/CC-BY: round 72's lock b and burst a, the whistle and the ram's a variants until round 76's picks)

## Open questions

- None open.

## Decisions

- 2026-10-31 (step m10-d-enemies-a): drafted for the Act 6 design phase from the roster row and the M10 gap
  analysis (§2.2: small, about 16 HP, 8 bullets shot or on impact, a slow ring no faster than Act 1's death-burst,
  entries from all edges with the 3 s warning, lightning kills it too); every number is a first draft decided 2026-10-31 (round 72). The gap analysis's "≈ 16" is read as its flown HP (8 base with the beam era's ×2, as
  the Buzzsaw Drone's 10 flies at 20); the ring's gap toward the ship and the 0.25 s grace after a ram are this
  step's.
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): Q9 a (the hybrid livery; b and c
  rejected) and the document approved as drafted (D.9; `design: approved`); **A.3 a**, "dart and sac", chosen (`art:
  chosen`; b "fork" in `concept/rejected/`).
- 2026-11-01 (M10 part D, step D2): the stat block is its [data.yaml](data.yaml), rendered above: the Buzzsaw's
  `dash` with the variant keys step D1 flies (`chase: [1.5, 2.5]`, `lock: tell-end`, `after: out`, `bounces: 0`,
  `dashes: 1`) and part A's `death-burst` attack (`on_contact`, `between`, `grace`); a dashing unit may carry a
  death-burst (its only attack). Measured at Level 38 (`levelReport`): 1.3 s on the screen before it dies, at most 4 on
  the screen and 2 in their tell or ram in the autopilot's flights; the EMP's drift without a ram is the simulation's
  (step D1). The caps are authored and measured, not a runtime rule (m10d-gaps.md §4).
- 2026-11-01 (M10 part D, step D5): implementation done (part D's full `check` green but for Level 39's three known
  tests); the production frames, the hunting whistle and the ram whoosh, and the caps of a script flown unharmed go
  to [round 76](../../../concept-rounds/round-76/README.md) (F.1, S.1, S.2, L.2).
