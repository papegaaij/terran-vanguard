---
title: Warden Tank
design: approved
implementation: in-progress
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../player/specials, ../../../campaign/act-3-red-dust, ../../../campaign/act-5-the-belt]
updated: 2026-10-27
---

# Warden Tank

## Summary

A human-built tracked tank: the hull drives its own heading along roads and the Tharsis foundry's
conveyors while a separate turret tracks the ship and fires bursts, even back up the screen after
it has passed. In Act 3 it is **unmarked grey with no trim** (a hint of human involvement, Level
19); from Act 5 it appears in Ascendancy black and gold, first on Level 34's derelict hulls (draft, approved in round 60).

## Design

### Stat block

Numbers at **medium**, rendered from [data.yaml](data.yaml) (M7 part F, step F2; see the
[balancing basis](../../README.md#balancing-basis)). HP is in damage units (a Pulse Cannon L1 shot
does 2). Set at its first level from the proposed Act 3 reference DPS (M18 = a, approved in round 36:
L19 = 92): the hull's 64 HP is 0.7 s, the turret 0.2 s. Where it returns (Act 6, L39 and L41) it
gets the act HP factor of a `medium` unit (every part).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Unmarked (machine: the EMP Burst stuns it longest) |
| Layer | `ground` |
| Size tier | `medium` |
| Size | 64×64 px, hitbox 46×56 (the concept; the production sprites 76×76); turret hitbox 22×22 on the hull's centre |
| Parts | hull (`vital`), turret (`destroyable`: silences its gun) |
| Orientation | `16 angles` (the hull, 2 tread frames); the turret `32 angles`, each with its own heading |
| HP | hull 64 · turret 20; 84 (easy 63 / hard 109, from the global multipliers) |
| Armour / shield | the rear hull plate is the weak point: direct shots arriving within ±50° of its rear do ×1.5 to the hull (the Scuttler's arc rule reversed); a tank driving up a belt toward the ship shows it its rear plate |
| Speed | 40 px/s along its path (plus scroll; plus the conveyor's speed when on one) |
| Movement | `walk` (tracked) along an authored road or conveyor path, turning at 60°/s; the turret turns at 120°/s toward the ship independently |
| Attack | a 3-shot `burst` every 2.5 s, the shots 0.15 s apart along the turret's aim once it is within 10° of the ship (the turret still 0.3 s first: the tell), standard 160 px/s, `medium` 6; it fires in any direction, up the screen too |
| Formations | convoy (2–4) (one path, 1.5 s apart) |
| Weak points | the rear hull plate (from behind) (×1.5), the turret (a destroyable part) |
| Effective traits | `anti-ground`, `rear` |
| Credits | 28 (score 280 × chain); whole (hull first) 26; parts: hull 20, turret 8 (28) |
| Death | `medium` mechanical explosion with sparks and smoke; the burnt hull stays as a wreck (grey, no markings) |
| First level / used in | L19; introduced in Level 19 (Foundry of Tharsis); returns L39, L41 (Act 6) in Ascendancy livery |
| Difficulty hooks | easy: 2-shot burst every 3.0 s; hard: 4-shot burst every 2.0 s, turret 160°/s |
<!-- /data -->

### Behaviour

- **Two sprites.** Hull and turret are drawn and turned separately: the hull faces its path, the
  turret its target. The turret's barrel recoils with each shot and a muzzle flash marks the
  burst's start (the tell: the turret stops turning for 0.3 s before the first shot).
- **Firing behind.** Its turret tracks the ship all round, so a tank that has scrolled past keeps
  firing up the screen; this is part of Level 19's "turrets fire from behind as you pass".
- **The rear plate.** On Level 19's belts a tank drives *up* the belt (facing up the screen) while the
  belt and the scroll carry it down past the ship, so its thin rear plate faces the ship **while it
  approaches**: the front guns meet it then (the briefing's `level-19-rear-arcs` circles it so), and
  `rear` guns finish a tank that has passed. A tank driving down a road shows the plate only once past.
- **Conveyors.** On a conveyor path its ground speed adds to the belt's (a `Road` that moves
  relative to the ground, §2.8 of the gap analysis); a convoy rides the belt nose to tail.
- **EMP.** As a machine it is stunned for 3 s by the EMP Burst: tracks and turret stop, no fire.
  (Its unmarked grey makes the point: a Vrell unit would only be stunned 1.5 s.)
- **Hint.** In Act 3 nothing on it identifies a maker: grey hull, no trim, no rim light, no
  insignia. The wreck is the same. Radio may remark on it in Level 19, but the main hint there is
  the Revenant Walker.
- **Code fit.** Hull `walk` and the `EnemyGun` turret turn rate exist separately; the two-sprite
  unit with independent headings is new (§2.8 of the gap analysis).

### Act 5: the return in livery (draft)

**Draft** (Act 5 design phase, step D-enemies-B; approved in round 60). The stat block above
(the approved Act 3 unit, rendered from its data) is unchanged; this section adds its Act 5 use. The build
updates the data's *First level / used in* row (L34 added before Act 6's L39 and L41).

- **J1: the Crawler Tank dropped.** The roster's Act 5 Crawler Tank (L34, a tank with a front and a rear
  turret) is what this tank already does: its turret fires bursts back up the screen after it has passed, and
  this doc scheduled "Ascendancy black and gold from Act 5". L34 *Salvage Run* flies the Warden Tank in livery
  instead (judgement call J1, shown in round 60): one unit, its art and sounds saved; the ground variety keeps
  two Ascendancy movers (the [Strider](../strider/README.md) and this tank).
- **Where.** L34 (the breather, threat rating 2): convoys of 2–3 on roads along the derelict CDF and Helix
  hulls of the ship graveyard, turning at the hull plates' seams; the level doc places them.
- **HP.** The act HP factor of a returning `medium` unit (its first level L19, reference 92; L34's proposed
  reference 140, J2): ×140/92 = ×1.52, the **hull 97 · turret 30** (127 whole), its time to kill unchanged.
  Bounty unchanged (26 whole, 28 in parts, Act 1 terms; the act factor 1.6⁴ scales it).
- **Attack, movement, rear plate, EMP**: as the stat block and *Behaviour* above; no new hook.
- **Livery.** The chosen r05-a concept is the Act 5 look (the r05 black livery re-rendered in the Act 5 gunmetal/charcoal with gold trim, white accent, rim light; red
  running lights per Q11 b, decided in round 60); its wreck is black and gold too. The intel names it Ascendancy
  from Act 5 (Act 3's "unidentified" stays for Levels 19–21). The production frames reuse
  `tools/art/warden_tank.py` with the livery changed (no new model).

### Difficulty and level use

- Burst and interval per the stat block; the global multipliers apply.
- Its burst fires along the turret's facing, so a level may give it the `in-arc` objective hook.

## Concept art

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

Chosen concept from [round 05](../../../concept-rounds/round-05/README.md) (listed in the
[ground](../README.md#concept-art) Concept art table): hull at 16 headings following a road,
turret at 32 headings tracking the player, tread marks. It is chosen **in Ascendancy livery only**
(black and gold, white accent, rim light), as used from Act 5. The **Act 3 unmarked-grey variant**
(the same model, grey, no trim) is proposed in concept round 36 (step A-units; prompts in
[../concept/prompts.md](../concept/prompts.md), generator `tools/concept/enemies_r36.py`), accepted in round 36 (U.6, 2026-10-13): the Act 3 livery is chosen too.

| File | What | Status |
|---|---|---|
| [../concept/warden-tank-r05-a.png](../concept/warden-tank-r05-a.png) | Warden Tank — Ascendancy tank (white accent): hull 16 headings following a road, turret 32 headings tracking the player, tread marks (sheet) | chosen — Ascendancy livery |
| [../concept/warden-tank-r05-a.gif](../concept/warden-tank-r05-a.gif) | Warden Tank — the same in motion | chosen — Ascendancy livery |
| [../concept/warden-tank-r36-a.png](../concept/warden-tank-r36-a.png) | Warden Tank, unmarked grey — the r05-a model with only its livery changed (no gold, no accent), side by side with r05-a (sheet) | chosen (round 36, U.6: the unmarked grey livery) |
| [../concept/warden-tank-r36-a.gif](../concept/warden-tank-r36-a.gif) | Warden Tank, unmarked grey — on a dirt road on Mars (motion) | chosen (round 36, U.6: the unmarked grey livery) |

Production art (Act 3 grey): [warden-tank-final-r42-a.png](../concept/warden-tank-final-r42-a.png)
([GIF](../concept/warden-tank-final-r42-a.gif)), `tools/art/warden_tank.py` (sprite names and frame layout in its
docstring and `briefs/m7f-names.md`); approved as final by the user in round 42 (F.b, 2026-10-20). Prompt notes:
[../concept/prompts.md](../concept/prompts.md#warden-tank-final-r42-a). The Ascendancy livery (Act 5+) is not
produced yet.

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-units; the round's review board follows) — a/b proposals in the belt look of [Q11](../../README.md#open-questions) a (gloss black; the user chose look b, gunmetal/charcoal with gold, in round 60, so the chosen variant is re-rendered in gunmetal in production), each a PNG sheet (the source render, the sprites and states, the readability check over the darkest ground of Level 34 with the measured gold share, key frames) and a GIF in the 480×540 play field; generator `tools/concept/enemies_r60.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/warden-tank-r60-b.png](concept/warden-tank-r60-b.png) | Warden Tank B, heavier gold — gold track guards, a gold engine deck and a gold turret roof on the same model (sheet) | chosen (round 60) |
| [concept/warden-tank-r60-b.gif](concept/warden-tank-r60-b.gif) | Warden Tank B, heavier gold — gold track guards, a gold engine deck and a gold turret roof on the same model (motion) | chosen (round 60) |

## Implementation

- [x] Two-part unit: hull (`16 angles`) and turret (`32 angles`) with independent headings and turn rates (step F3: `EnemySpec.Turret`, `WardenTankTest`)
- [x] Tracked `walk` along roads and conveyors (belt speed added) (step F3: `Belts`, `FoundryTest`)
- [x] Turret burst: 0.3 s still before it fires, 3 shots 0.15 s apart, every 2.5 s, any direction (step F3: `WardenTankTest`)
- [x] Rear plate ×1.5 within ±50° of the rear; the turret's loss silences it (step F3: `EnemySpec.RearPlate`, `WardenTankTest`)
- [ ] EMP stun 3 s (machine)
- [ ] Unmarked grey livery and wreck for Act 3; Ascendancy livery from Act 5
- [ ] Act 5 (when the draft is approved): L34 in the data's *used in* row, the act HP factor at L34 (hull 97, turret 30), the intel label "Ascendancy" from Act 5, the livery's production frames and wreck (`tools/art/warden_tank.py`)
- [x] Data file with the stat block (when Level 19 is built): step F2, the `turret` and `rear_plate`
      keys loaded, checked and mapped (`FoundryRules`)
- [x] Production art (round of part F; approved as final in round 42, F.b)

## Open questions

- J1 (shown in round 60): the Crawler Tank dropped for this tank in livery at L34; the alternative is the
  roster's Crawler Tank as its own unit (a front and a rear turret), one more unit's art and sounds.
- Q11 (decided in round 60, **b**, not the recommendation): the Ascendancy look on the belt is gunmetal/charcoal hulls
  with gold (≥ 20 % gold, rim light, red running lights); a (gloss black) and c (black silhouettes) rejected
  ([enemies](../../README.md#factions-and-visual-language)).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the [ground roster](../README.md) to a draft
  spec with the round-05 concept (Ascendancy livery) as its chosen model; the Act 3 grey variant
  is proposed by step A-units. M18 = a taken as the recommended option, accepted in round 36 (2026-10-13). Our proposals: hull 64 and turret 20 as parts, the ±50° rear plate at ×1.5, the
  3-shot burst every 2.5 s in any direction, bounty 26 whole / 28 in parts, intel label
  "unidentified".
- 2026-10-12 (merge): the round-36 unmarked-grey livery proposal is listed under Concept art.
- 2026-10-13 (user, round 36): approved (D.14). Q.22f yes: the intel lists the Act 3 tanks as "unidentified", not Ascendancy. U.6 accepted: the unmarked grey livery (the r05-a model, no gold, no accent, the red sensors kept) is chosen for Act 3.
- 2026-10-19 (M7 part F, step F2): the stat block moved into [data.yaml](data.yaml) (the `turret` and
  `rear_plate` keys; the turret a destroyable part that silences the gun, its hit box on the hull's
  centre; the hull's `first_bonus` 6 for the whole 26). Fixed, accepted in round 42 (2026-10-20): the
  rear plate faced the ship "after it passes" in the *Effective traits* row; on Level 19's belts a tank
  drives up the belt, so its rear plate faces the ship while it approaches (as Level 19's briefing image
  and text have it).
- 2026-10-20: round 42 closed: the Act 3 grey production art (F.b) approved as final by the user; the build choices (B.c) and the rear-plate fix taken as built.
- 2026-10-27 (Act 5 design phase, step D-enemies-B; approved in round 60): the Act 5 return
  drafted (*Act 5: the return in livery*): L34 in Ascendancy livery replaces the roster's Crawler Tank (J1), hull 97
  and turret 30 by the act HP factor, intel "Ascendancy" from Act 5. The design is `review` until the round.
- 2026-10-27 (user, [round 60](../../../concept-rounds/round-60/README.md) closed): the design approved as drafted (`design: approved`); art A.15 **b**, "Warden Tank B, heavier gold", chosen (`art: chosen`), the other variant in `concept/rejected/`. Q11 **b** (gunmetal/charcoal with gold, not gloss black): the chosen variant is re-rendered in gunmetal/charcoal in this part's production art, keeping the ≥ 20 % gold and the readability check.
