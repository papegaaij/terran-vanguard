---
title: Gate Warden
design: approved
implementation: done
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

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (`space`); Vrell, gate-grown (the *gate stone* look): the [EMP Burst](../../../player/specials/README.md#emp-burst-l15) stuns it 1.5 s and opens its shell 3 s |
| Size tier | `medium` |
| Size | 76×84 px, hitbox 56×60 (the shell a closed ovoid 84 px tall round the body) |
| Parts | single |
| Orientation | `fixed` (faces down); its eye (the line's emitter) turns to aim, the body does not |
| HP | 360 (easy 270 / hard 468, from the global multipliers) |
| Armour / shield | the **window shield** ([window shields](../../README.md#window-shields)): closed it is invulnerable; open from its line's telegraph start to the beam's end (0.8 s + 1.0 s = **1.8 s** of every 4.0 s); a `shield-breaker` hit or the EMP opens it **3 s**. No hex shield (J4). HP **≈ 1.9 s** of the plan's L43 forward DPS (189), 2.1 s at the reference (172): above the `medium` band (0.4–1.5 s) on purpose, only open time counts, so one window plus a little, or one breaker hold, kills it |
| Speed | 90 px/s entering and leaving; still at its station |
| Movement | `hover`: enters from the top to a station **100–180 px** below the top edge, holds it **14 s** (three to four windows), then rises out through the top edge at 90 px/s; for the last **2 s** at its station the glyphs dim and flicker and it tilts its top toward the edge (the leaving tell) |
| Attack | `laser-line` every **4 s**, telegraph to telegraph: a thin crimson line for **0.8 s** (brightening, the eye glowing), then the beam along it, a 10 px band live **1 s**, `heavy` **10** once per line (a ship touching it any time in that second); either **straight down** from the eye or **aimed** (locked through the ship's position at the telegraph's start, then fixed); the wave says which. Contact `medium` = 15 |
| Formations | single, line abreast (2–3) (line abreast of **2–3**, hulls ≥ 80 px apart: at most three across the 480 px field; the windows staggered **1.3 s** apart so they open in turn, left to right or from the middle out; pairs); at most 2 lines telegraphed at once, at most 4 Wardens on the screen |
| Weak points | the eye at the shell's centre (drawn only) |
| Effective traits | `shield-breaker`, `forward` |
| Credits | 40 (score 400 × chain); a kill pays as usual whatever opened it |
| Death | `medium` explosion of stone: the shell shatters, the glyphs go dark one by one, the body splits into three slabs that fall and fade (presentation, no damage) |
| First level / used in | L43; the gate approach, the level's `shield-breaker` trait; later Act 7 levels as the level drafts place it |
| Difficulty hooks | easy: a window every 4.5 s, only straight-down lines; hard: every 3.5 s, telegraph still 0.8 s (the floor), lines abreast of 3 with 1.0 s stagger |
<!-- /data -->

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
| [concept/gate-warden-final-r89-a.png](concept/gate-warden-final-r89-a.png) | Production frames (`tools/art/gate_units.py warden`, M11 part B, step ART-B): the iris closed to open in six frames with the glyph light at the game's level, the closed shell's sweep, the shell broken (bare) and regrowing behind the shimmer, the telegraph eye's charge, the leaving tell's four tilts, the death's three slabs going dark in turn, closed and open over the level's dark band at 1× (sheet) | proposed (round 89) |
| [concept/gate-warden-final-r89-a.gif](concept/gate-warden-final-r89-a.gif) | One Warden's cycle at 2×: closed with the sweep, the iris opening, the eye charging, closing again | proposed (round 89) |
| [concept/shell-capture-r88-a.png](concept/shell-capture-r88-a.png) | Game capture (A4 rig over Level 43's deep field): the window shell's stand-in look on ART-B's closed frame: the iris at the telegraph, the live line, a Plasma Arc hit breaking it, open 3 s, the regrowth, closed | proposed (round 88) |
| [concept/shell-capture-r88-a.gif](concept/shell-capture-r88-a.gif) | The same, 4.7 s at the game's speed | proposed (round 88) |
| [concept/leaving-capture-r88-a.png](concept/leaving-capture-r88-a.png) | Game capture, full screen: the leaving tell (the tilt, the `WARDENS` tracker blinking amber), the rise out of the top edge, the secondary failed | proposed (round 88) |
| [concept/leaving-capture-r88-a.gif](concept/leaving-capture-r88-a.gif) | The same, 3 s | proposed (round 88) |
| [concept/warden-lines-capture-r89-a.gif](concept/warden-lines-capture-r89-a.gif) | Game capture, Level 43 (M11 part B, step B5): the t=62 pair's straight lines and the t=94 pair's aimed lines, the windows opening in turn 1.3 s apart (a weak fit, so the shells hold), 19 s at the game's speed | proposed (round 89) |
| [concept/warden-arc-capture-r89-a.gif](concept/warden-arc-capture-r89-a.gif) | Game capture, Level 43: the plan's fit (Tesla Coil Pod L4) flown up to the t=62 pair, the arc opening a shell and holding it open while the beam kills it, 14 s | proposed (round 89) |
| [concept/warden-leaving-capture-r89-a.png](concept/warden-leaving-capture-r89-a.png) | Game capture, Level 43 with the HUD: the first Warden's leaving tell, its rise out of the top edge (firing as it goes) and `WARDENS FAILED`, 0.25 s apart | proposed (round 89) |

Production (round 89, M11 part B, step ART-B): `tools/art/gate_units.py warden` renders round 84's a "idol" unchanged through the production path; the shell's spark and shatter are ART-A's `shell-spark`/`shell-break` ([enemies](../../concept/prompts.md)), the line itself the game's (`LineLooks`). The frames are held in `build/art-held/sprites/` until this unit's `data.yaml` exists (step B2). Its intel portrait is in the [hangar](../../../ui/hangar/README.md#concept-art) (`intel-final-r89-a`).

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

- [x] Stat block in a `data.yaml`; global difficulty multipliers; HP re-measured with `levelReport` at L43 (the plan's
  fit with and without the Tesla pod) (M11 part B, step B2, 2026-11-05: 1.9 s of the whole fit with the arc, ≈ 2.3 s of
  open time without it; the secondary met 3 of 3 at medium, so 360 HP stays)
- [x] The [window shield](../../README.md#window-shields): closed invulnerable with sparks, open for its line's
  telegraph and beam, the 3 s break by `shield-breaker` or the EMP and the regrowth; deterministic and allocation-free (M11 part A, 2026-11-05, steps A1a and A3a: `WindowShell`, `WindowShieldTest`, the stand-in look `WindowShellLooks`; the Warden's own frames are part B's)
- [x] `laser-line` with a 1.0 s live band, straight down or aimed; the staggered windows of a line abreast; at most 2
  lines telegraphed at once (M11 part A, 2026-11-05, step A1a: `aim: straight`, the wave's `line_stagger`, `WardenLineTest`)
- [x] Targeting: homing, Rook and the arc prefer open Wardens (M11 part A, 2026-11-05, step A1a: closed shells shunned while an open one is on the screen, `WindowShieldTest`)
- [x] Production sprites (the shell's sweep, opening petals, shatter and regrowth, the eye's charge, the death's
  slabs) via `tools/art/`; sounds (Freesound, CC0/CC-BY) (M11 part B, 2026-11-05: `tools/art/gate_units.py` (ART-B),
  `WardenLooks` and the cues (B4); in the game in round 89)

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
