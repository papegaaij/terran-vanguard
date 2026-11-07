---
title: Reactor Core
design: draft
implementation: done
art: chosen
depends-on: [../../../campaign/act-7-beyond-the-gate/level-48-vornes-end]
updated: 2026-11-07
---

# Reactor Core

## Summary

A live gold reactor core in a wreck's open hull on the Vrell citadel's decks: Level 48's secondary target, ten of
them in five pairs, eight to burn (`CORES n / 8`). No attack, no contact, 40 HP, not counted as an enemy. The design is
[Level 48's](../../../campaign/act-7-beyond-the-gate/level-48-vornes-end/README.md#the-reactor-core); this part only
holds its numbers, because the secondary's `escapes` counts a unit by its slug and the loader finds units only in a
directory of their own.

## Design

### Stat block

Numbers at **medium** from its [data.yaml](data.yaml) (the table is rendered from it), Level 48's approved table
(round 84).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy |
| Layer | `ground` |
| Size tier | `small` |
| Size | 40×40 px, hitbox 32×32 |
| Parts | single |
| Orientation | `fixed` (seen from above; its core glowing gold-white) |
| HP | 16 (easy 12 / hard 21, from the global multipliers) |
| Armour / shield | none (not hardened). HP 16 base flies at 40 with the beam era's × 2.5 |
| Speed | 0 px/s (scrolls with the ground) |
| Movement | `terrain` (fixed in its wreck's open hull) |
| Attack | `none`; never hurts anything |
| Formations | single (one in each half of the field, a pair beside a turret nest, C1-C10) |
| Weak points | none |
| Effective traits | `anti-ground` |
| Credits | 10 (score 100 × chain); counted with the ground targets' share in the typical haul, not counted as an enemy |
| Death | a short gold-white flash and a debris ring (vacuum); the veins from it go dark from the core outward over 1 s |
| First level / used in | L48; Level 48 (the secondary `CORES`: 8 of the 10 in the decks' wrecks) |
| Difficulty hooks | none (the global HP multipliers; the secondary's allowed escapes: easy 4, medium 2, hard 0) |
<!-- /data -->

### Behaviour

Level 48's [secondary](../../../campaign/act-7-beyond-the-gate/level-48-vornes-end/README.md#objective): `escapes:
reactor-core`, two allowed away (easy four, hard none); met at the eighth down, failed by the third away. Not counted
as an enemy (density, the floor, kill ratios: Level 46's seed casket's rule), its bounty counted with the ground
targets' share in the typical haul. Each core's vein in the backdrop pulses up toward the spire while it lives and
goes dark from the core outward at its death (the look is step F4's); while a live core is within 120 px of the
field's bottom edge the HUD's tracker blinks amber (`leaving: {bottom_band: 120}`).

## Concept art

Production art in Level 48's batch (step ART-F, round 93): `tools/art/act7_citadel.py props` draws the core whole,
cracked and dead, its breathing glow, its death and tatters
([l48-props-final-r93-a.png](../../../campaign/act-7-beyond-the-gate/level-48-vornes-end/concept/l48-props-final-r93-a.png));
see Level 48's *Concept art*. In `assets/sprites/` since step F2's data claims them (`reactor-core*`).

## Implementation

- [x] Stat block in a `data.yaml`, loaded and checked (M11 part F, step F2)
- [x] The ten cores on Level 48's decks counted by the secondary (`escapes: reactor-core`, step F2)
- [x] Production sprite in the game, its glow's flicker in the bottom band, its death and its vein going dark (steps
  ART-F, F4; round 93 F.3, L.2)

## Decisions

- 2026-11-07 (M11 part F, step F2; gap check `m11f-gaps.md` §4, art accepted by the user in round 93 F.3 and L.2): promoted from
  Level 48's level objects to a minimal part (the secondary counts a unit by its slug, as Level 39's pump station), an
  **uncounted** one (`counted: false`, as Level 46's seed casket), so Level 48's "the build adds it to the enemies'
  data, as Level 39's pump station" reads as this page. HP 16 base, the `small` tier: the beam era's × 2.5 flies it at
  the table's 40 (easy 30, hard 52).
- 2026-11-07 (user, [round 93](../../../concept-rounds/round-93/README.md)): **F.3 accept**: the production art (`l48-props-final-r93-a`, rows chosen; `art: chosen`); **L.2 ok** (a core burnt, its vein going dark). The part's `design` stays `draft`.
