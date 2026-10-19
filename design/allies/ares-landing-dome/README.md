---
title: Ares Landing dome
design: review
implementation: done
art: final
depends-on: [../../enemies, ../../enemies/air/spore-bomber, ../../enemies/ground/shellback, ../../ui/hud, ../../world/mars, ../../campaign/act-3-red-dust/level-17-dome-siege]
updated: 2026-10-19
---

# Ares Landing dome

## Summary

A greenhouse dome of Ares Landing, the oldest colony on Mars: a friendly `ground` structure that
scrolls through with the ground and counts **hits** instead of HP. Only the attacks the
[target-the-objective hook](../../enemies/README.md#target-the-objective-hook) sends at it hurt it;
it never fails the mission. Five named domes stand in [Level 17](../../campaign/act-3-red-dust/level-17-dome-siege/README.md),
whose secondary objective is that none of them is hit (M16 = a).

## Design

The spec, approved with Level 17 in concept round 36 (M16 = a, Q.17a, MA.3 = a), moved here as its own
document in M7 part D (Q.25b). Its numbers live in the allies' [data.yaml](../data.yaml) under the slug
`ares-landing-dome` (one loader for every ally; the defaults of the gap check below pending the user's
review in round 40); the table is hand-written. Level 17 places the domes (the level's `allies` block,
[schemas](../../tech/architecture/README.md#data-file-schemas)).

| Property | Value |
|---|---|
| Size / layer | footprint **120×120 px** on `ground` (`size`): a geodesic glass dome glowing green inside, on a concrete ring with airlocks; **hit box 88×88**, the footprint less 16 px each side. Eden, the largest, is **144×144** (the level's `size` override; its hit box keeps the 16 px margin: 112×112) |
| Follows | `follows: ground`: it enters at the top edge at its time (its bottom edge there) and scrolls through with the ground; the scroll never halts for it (a timed halt needs a `defend` primary) |
| Hits | breaks on the **3rd** hit at medium (`hits: 3`; the level's easy 4, hard 2); no repair |
| Damaged by | only hooked attacks, one hit each (`damaged_by`): a hooked bomber over it as its footprint passes beneath (`bombs: 1`, once per bomber, its load spent), a hooked mortar's direct hit landing on it (`lobs: true`; the ring of 8 hurts only the ship), and a roll crossing its footprint (`rolls: 1`, once per roll; the [Shellback](../../enemies/ground/shellback/README.md)'s, hooked or not). Shots aimed at the ship that cross it do nothing |
| Feedback | a white hit flash; a crack pattern spreading per hit (`-crack-1..3`); at one hit left the inner green light flickers (`-flicker`); broken, the glass caves in, the light goes out and a dust plume rises (`-broken`; no debris on the player's plane) |
| Ship and fire | no contact; the player's and Rook's fire and the specials pass over it |
| Mission | never fails it: a broken dome loses its crops, not lives (the colonists are below ground) |
| HUD | an objective tracker `DOMES` with one pip per dome (green; amber after its first hit; a red flash then dark when broken; the `dome-pip`), turning `DONE` or `FAILED` with the level's `untouched` secondary; the debrief's row `DOMES UNTOUCHED n / 5` |
| Radio | `{ally}` is the dome's name (the level's `ally-hit` at a dome's first hit, `ally-lost` when it breaks) |
| Sprites | `ares-dome` (Eden `ares-dome-eden`, the level's `sprite` override): intact, `-crack-1`, `-crack-2`, `-crack-3`, `-flicker`, `-broken` (step ART-D) |
| Levels | [L17 Dome Siege](../../campaign/act-3-red-dust/level-17-dome-siege/README.md): Bradbury, Lowell, Schiaparelli, Burroughs and Eden |

**The hook at the domes** (the level's `targeted_by`, per unit type): a hooked bomber (`always`)
holds the level's `bomb_row` (Level 17: 160 px below the top edge) and steers at its own speed to the
x of the next dome coming down, a wave's bombers on x slots `bomb_slot` apart (40 px) round the dome's
x; when the dome's footprint passes beneath it, it drops its load (one hit) and flies on, laying its
normal spore mines on the ship's plane. A hooked mortar (`alternate`) lobs every second shell at the
nearest dome while that dome is closer than the ship, aimed where the dome will be when the shell
lands (the marker on the ground, 1.2 s ahead). A roll crosses a dome below the roller's path whether
the roller is hooked or not.

## Concept art

The look is the chosen geodesic glass dome of concept round 36 (MA.3 = a), in
[Mars](../../world/mars/README.md)'s concept art: [dome-r36-a.png](../../world/mars/concept/dome-r36-a.png)
(the rejected b, a ribbed composite pressure dome, in Mars's `concept/rejected/`). Production art
(the sprites above and the HUD pip) came with M7 part D's round 40 (step ART-D).
Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/ares-dome-final-r40-a.png](concept/ares-dome-final-r40-a.png) | Ares Landing dome final sprites (`tools/art/siege_units.py`, from `mars_r36.dome_a`, MA.3 = a): 120 px and Eden 144 px, breathing loop, cracks 1-3, flicker, white flash, cave-in, dust plume, HUD pip (sheet) | chosen (round 40, F.a: approved as final) |
| [concept/ares-dome-final-r40-a.gif](concept/ares-dome-final-r40-a.gif) | Ares Landing dome final: Bradbury taking three hits and caving in beside an untouched Eden, 10 fps | chosen (round 40, F.a: approved as final) |

## Implementation

- [x] Spec loaded from data (`hits`, `damaged_by` `bombs`, `lobs`, `rolls`, `follows: ground`, `sprite`) and placed by a level's `allies` block (units, `targeted_by`, `bomb_row`, `bomb_slot`, the easy/hard `hits`; Eden's `size` and `sprite` overrides); checked by content's `SiegeChecks` (M7 part D, step D2)
- [x] Scrolls through with the ground; hits counted per dome; breaks at its hits; never fails the mission (step D1, `GroundAlliesTest`)
- [x] Hurt only by hooked bomb runs, hooked lobs' direct hits and rolls, one hit each (steps D1, D3; `BombRunTest`, `RollCrushTest`, `Level17Test`)
- [x] Looks: hit flash, cracks per hit, the flicker at one hit left, the cave-in with its dust plume (step G1, `DomeLooksTest`; cracks and a break in the round 40 capture)
- [x] HUD tracker `DOMES` and the debrief row (step G1, `LevelScreenSiegeTest`)
- [x] Production art: the dome's states, Eden's larger variant, the HUD pip (step ART-D, proposed in round 40)

## Open questions

- Pending the user's review in round 40 (defaults of the gap check, built in M7 part D): the dome as its
  own directory with its numbers in the allies' data under its slug; Eden as a `size` override (144 px,
  the hit box keeping the 16 px margin) with its own sprite rather than a second slug; the bombers' row
  and x slots; the hook per unit type.

## Decisions

- 2026-10-12 (overnight design phase): added to the allies roster, drafted in Level 17 (five named domes
  to hold).
- 2026-10-13 (user, round 36): approved with Level 17 (M16 accepted; Q.17a: the hooked Shellbacks hurt
  it too); the look is the geodesic glass dome on a concrete ring (MA.3 = a); it becomes its own allies
  document in part D (Q.25b).
- 2026-10-18 (M7 part D, step D2): this document written from Level 17's approved spec, its numbers in
  [data.yaml](../data.yaml) (`ares-landing-dome`: `hits: 3`, `damaged_by` `bombs: 1`, `lobs: true`,
  `rolls: 1`); the roster row replaced by the Contents row; the defaults above pending round 40.
- 2026-10-19 (user, round 40, F.a): the final art approved (`art: final`); the dome's defaults (round 40 B.b) accepted.
