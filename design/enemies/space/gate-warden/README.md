---
title: Gate Warden
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../player/weapons/tesla-coil-pod, ../../../art-direction, ../../../world/vrell-space, ../../../campaign/act-7-beyond-the-gate]
updated: 2026-11-05
---

# Gate Warden

## Summary

A stone-and-glyph sentinel grown from the Tether Gate's own material (Level 43, the gate approach). A pale shell
closes it completely: shots spark off it and do nothing. The shell opens only while the Warden fires its
`laser-line`, from the thin red telegraph to the beam's end, or for 3 s after a `shield-breaker` hit or the EMP.
Wardens hold the gate's mouth in lines abreast and open in turn: time the windows or break them open.

## Design

### Stat block

**Draft** (Act 7 design phase, step m11-d-enemies-a, 2026-11-04; decided 2026-11-05, round 84, Q2 = a). A
hand-written table: there is no `data.yaml` yet, the build turns it into one. Numbers at **medium** (see the
[balancing basis](../../README.md#balancing-basis)); HP is in damage units (a Pulse Cannon L1 shot does 2), set at its
first level from the proposed Act 7 reference DPS (J11, L43 = 172, the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). A `medium` unit: no beam-era factor;
the act HP factor in its later levels. Fire rates are first values: the build tunes them with `levelReport`.

| Field | Value |
|---|---|
| Faction | Vrell (gate-grown, *gate stone* look in [factions](../../README.md#factions-and-visual-language)); biomechanical: the [EMP Burst](../../../player/specials/README.md#emp-burst-l15) stuns it 1.5 s and opens its shell 3 s |
| Layer | `space` (the `air` layer) |
| Size tier | `medium` |
| Size | 76×84 px, hitbox 56×60; the shell a closed ovoid 84 px tall round the body |
| Parts | single (the shell is a state of the unit, not a part) |
| Orientation | `fixed` (faces down); its eye (the line's emitter) turns to aim, the body does not |
| HP | 360 (easy 270 / hard 468, from the global multipliers): **≈ 1.9 s** of the plan's L43 forward DPS (189), 2.1 s at the reference (172). Above the `medium` band (0.4–1.5 s) on purpose: only open time counts, so one window plus a little, or one breaker hold, kills it |
| Armour / shield | the **window shield** ([window shields](../../README.md#window-shields)): closed it is invulnerable; open from its line's telegraph start to the beam's end (0.8 s + 1.0 s = **1.8 s** of every 4.0 s); a `shield-breaker` hit or the EMP opens it **3 s**. No hex shield (J4) |
| Speed | 90 px/s entering and leaving; still at its station |
| Movement | `hover`: enters from the top to a station **100–180 px** below the top edge, holds it **14 s** (three to four windows), then rises out through the top edge |
| Attack | `laser-line` every **4.0 s**, telegraph to telegraph: a thin crimson line for **0.8 s** (brightening, the eye glowing), then the beam along it, a 10 px band live **1.0 s**, `heavy` **10** once per line (a ship touching it any time in that second); either **straight down** from the eye or **aimed** (locked through the ship's position at the telegraph's start, then fixed); the wave says which. Contact `medium` = 15 |
| Formations | line abreast of **2–3** (hulls ≥ 80 px apart: at most three across the 480 px field), the windows staggered **1.3 s** apart so they open in turn, left to right or from the middle out; pairs; at most 2 lines telegraphed at once, at most 4 Wardens on the screen |
| Weak points | the eye at the shell's centre (drawn only) |
| Effective traits | `shield-breaker` (the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md)'s arc opens it at every hit), `forward` |
| Credits | 40 (score 400 × chain); a kill pays as usual whatever opened it |
| Death | `medium` explosion of stone: the shell shatters, the glyphs go dark one by one, the body splits into three slabs that fall and fade (presentation, no damage) |
| First level / used in | L43 (the gate approach, the level's `shield-breaker` trait); later Act 7 levels as the level drafts place it |
| Difficulty hooks | easy: a window every 4.5 s, only straight-down lines; hard: every 3.5 s, telegraph still 0.8 s (the floor), lines abreast of 3 with 1.0 s stagger |

### Behaviour

- **The window.** The Warden is a turret that must open to shoot. While closed, every hit sparks off the shell with
  a pale gold flake and does nothing: no damage, no bounce (it is not a [mirror](../../README.md#reflect)). From the
  first frame of its line's telegraph to the beam's last frame the shell is drawn back like an iris (0.15 s) and the
  eye and glyphs burn crimson: every weapon hits at full damage.
- **The play.** A straight-down line falls on the column below the eye, where the forward guns would sit: fire into
  the eye during the 0.8 s telegraph and slip aside before the beam, let the wing pods and the homing missiles work
  through the beam's second, or come within the Tesla pod's reach (170 px at L4, the plan's L43 fit) and keep the
  shell open with the arc (each arc hit restarts the 3 s), killing it in about two seconds of the whole fit. An
  aimed line asks the reverse: stand off its line and shoot from where the beam is not. A line abreast opens in turn,
  so the player crosses the row from one open Warden to the next.
- **The leaving tell** (Level 43's secondary, the round 80 lesson): for the last **2 s** at its station a Warden's
  glyphs dim and flicker and it tilts its top toward the edge it leaves by; the HUD's tracker blinks amber while one
  shows it (the [HUD](../../../ui/hud/README.md#act-7-additions-draft)'s shared leaving blink).
- **Targeting.** Homing shots, Rook and the Tesla's arc choose an open Warden over a closed one; they pick a closed
  one only when nothing else is in reach (the arc's hit opens it, the others spark).
- **Spacing** (the Act 5 lesson): Wardens in a line keep ≥ 80 px between hull edges; they never share a station
  with another `medium` or larger hull.
- **Look** (Q10 b, the *gate stone* look): a squat basalt sentinel, dark grey-violet stone ringed with the gate's
  glyphs in crimson, a single round eye at its centre. The shell closed: a smooth pale gold-white ovoid with a slow
  specular sweep (the glyphs dim behind it); opening: the shell parts into six petals that fold back; after a break:
  the petals shatter into gold flakes and regrow in 0.4 s behind the shared regrowth shimmer (the Act 6 tell). Over
  L43's dark stone backdrop the shell must stay clear of the gate's ring: the art step checks it over the level's
  brightest and darkest band.
- **Sounds** (brief for the audio step, real recordings from Freesound, CC0/CC-BY): a slow stone grind as the shell
  opens and closes (a heavy stone slab dragged), a low resonant rising hum under the telegraph (a bowed metal plate or
  a rubbed singing bowl), a sustained electric crackle for the beam's second; on a closed shell a dull stone tick,
  never the hex shield's chime; the death a heavy rockfall.

### Difficulty and level use

- At most 4 on the screen and 2 lines telegraphed at once; a straight-down line never falls on a column a
  neighbour's line already holds.
- Level 43 places its first Wardens alone or in pairs before a line abreast, and never with popcorn dense enough to
  pin the ship under a straight-down line.

## Concept art

Round 84's proposals (step ART-R-A, M11 design phase, picked 2026-11-05; generator `tools/concept/enemies_r84.py gate-warden`). Each sheet holds the source render, the sprites at play size with their states, the readability check over Level 43's brightest and darkest band beside the player (a stand-in of the level's light in the one Vrell palette, Q10 b), and key frames; each GIF is the 480×540 play field. Prompts: [concept/prompts.md](concept/prompts.md). The ring's teal-white glyphs and the Warden's crimson stay apart, as the world document asks.

| File | What | Status |
|---|---|---|
| [concept/gate-warden-r84-a.png](concept/gate-warden-r84-a.png) | Gate Warden a, "idol": a squat round basalt sentinel with a crimson glyph band and the eye on top, in a six-petal pale gold-white egg with gold seams; closed / opening / open / telegraph / broken / regrowing; closed and open over the gate ring and deep space | chosen (round 84 A.1 a) |
| [concept/gate-warden-r84-a.gif](concept/gate-warden-r84-a.gif) | Three Wardens abreast at the gate ring: hits sparking off closed shells, the windows opening in turn 1.3 s apart (straight and aimed lines: the 0.8 s telegraph, the 1.0 s beam), one killed in its second window, the Tesla arc holding one open, the death's shattered shell and three slabs | chosen (round 84 A.1 a) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [ ] Stat block in a `data.yaml`; global difficulty multipliers; HP re-measured with `levelReport` at L43 (the plan's
  fit with and without the Tesla pod)
- [ ] The [window shield](../../README.md#window-shields): closed invulnerable with sparks, open for its line's
  telegraph and beam, the 3 s break by `shield-breaker` or the EMP and the regrowth; deterministic and allocation-free
- [ ] `laser-line` with a 1.0 s live band, straight down or aimed; the staggered windows of a line abreast; at most 2
  lines telegraphed at once
- [ ] Targeting: homing, Rook and the arc prefer open Wardens
- [ ] Production sprites (the shell's sweep, opening petals, shatter and regrowth, the eye's charge, the death's
  slabs) via `tools/art/`; sounds (Freesound, CC0/CC-BY)

## Open questions

- The HP of 360 sits above the `medium` time-to-kill band on purpose (only open time counts); part A measures it at
  L43 with and without the Tesla pod and may lower it.

## Decisions

- 2026-11-04 (step m11-d-enemies-a): drafted for the Act 7 design phase from the roster row and the M11 gap analysis
  (§2.2, Q2 a, J4): the window shield, the line abreast with staggered windows, HP ≈ 2 s of the plan's forward DPS;
  the 1.0 s live beam, the 4.0 s cycle, the 1.3 s stagger, the targeting preference and the *gate stone* look are
  this step's. Decided 2026-11-05, round 84.
- 2026-11-04 (step m11-d-act, decided 2026-11-05, round 84): the 2 s leaving tell (Level 43's proposal) and the HUD's shared
  amber leaving blink.
- 2026-11-05, round 84 (the user's page answers; decided as drafted unless stated, the alternatives rejected). The open items were:
  - **Q2** (round 84): how closed is the shell? **a** (drafted) invulnerable while closed, open only while it fires,
    `shield-breaker` or the EMP open it 3 s; **b** an Act 5 hex pool every weapon wears down, open while firing (Act 5's
    unit again); **c** Act 6's mirror while closed (repeats L37 and punishes the beam).
- 2026-11-05 (user, round 84 closed): **A.1 a "idol"** chosen (a squat round basalt sentinel with a crimson glyph band and one eye, in a six-petal gold-white egg whose seams show); b "keystone" rejected (moved to `concept/rejected/`). **Q2 a** as drafted; **D.10 approved**; the shell sounds are S.3 a, S.4 b, S.5 b, S.6 a, S.7 a (see the [sound effects](../../../audio/sfx/README.md)).
