---
title: Level 48 – Vorne's End
design: approved
implementation: in-progress
art: chosen
depends-on: [../../../enemies/bosses/vornes-chimera, ../../../enemies/air/chimera, ../../../enemies/air/harrow, ../../../enemies/air/wraith, ../../../enemies/ground/graft-turret, ../../../enemies/README.md, ../../../world/vrell-space, ../../../player/generator, ../../../player/wingmen, ../../../player/specials, ../../../ui/hud, ../../../story/characters/vorne]
updated: 2026-11-07
---

# Level 48 – Vorne's End

## Summary

Beyond the inner sphere the Vrell have grown **a citadel** over the gold wrecks of what the Ascendancy brought through
the gate: teal-black towers and decks over dull gold hulls, the last port of Vorne's lifeboat. Its swarms rise **from
behind** the ship (Chimera wings, Harrow streams, Wraiths) while grown turrets hold the decks and the wrecks' **gold
reactor cores**, still live, feed the growth: the secondary is to burn them. At the citadel's heart the scroll halts
over the spire well and the mid-boss rises out of it: [Vorne's Chimera](../../../enemies/bosses/vornes-chimera/README.md),
Vorne fused with his lifeboat, who talks through the fight and dies mid-sentence. Then the wing flies out through the
spire. Air and ground, front and rear. 248 s of level clock plus the fight (≈ 5.5 min), ≈ 109 enemies a minute at
medium.

**Status: approved** (Act 7 design phase, step `m11-d-levels-3`, 2026-11-04; the user approved it on 2026-11-05, round 84).
Written from the act roster, the gap analysis's recommended answers (**Q5 a**; J3, J7, J8, J9, J11, J12) and the
earlier steps' drafts, which are the source of truth for their numbers: the mid-boss
[Vorne's Chimera](../../../enemies/bosses/vornes-chimera/README.md) (its arena, phases, playbooks, tells, HP, par,
bounty formula and Vorne's lines), [Vrell space](../../../world/vrell-space/README.md)'s citadel and its mood, the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft),
[Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft), the
[Act 7 specials](../../../player/specials/README.md#act-7-specials-draft) and the HUD's
[Act 7 additions](../../../ui/hud/README.md#act-7-additions-draft). There is **no `data.yaml`**: the build turns the
hand tables into one; no number is measured.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft):

<!-- data: briefing -->
> **Commander Okafor:** "Past the inner shell the Vrell grew a citadel over the wrecks Vorne's
> fleet brought through. His lifeboat's trail ends there. The Choir took him in, Lancer.
> Whatever it made of him waits at the citadel's heart. Bring him down, then get out through the
> spire."
>
> **Dr. Varga:** "The citadel feeds on the wrecks' gold reactors. Burn the cores and starve it.
> Its swarms come up from behind: the place was grown to close round whatever lands in it. What
> the Choir does to a mind it takes in, I don't know. Be ready for anything."
<!-- /data -->

*Images* (production art in M11's build, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-48-the-citadel`: a CDF chart of the citadel seen from above, a ring of grown towers round a
  central spire, gold wreck outlines under the teal growth, the lifeboat's trail a dotted gold line ending at the
  spire, "LAST BEACON: VORNE, A." in the corner.
- Varga's page `level-48-reactor-core`: her scan of a gold reactor core in grey outline, teal veins drawn out of it
  toward an arrow marked "SPIRE"; under it the citadel's plan with the decks' cores as gold dots.

*Hangar teaser* (shop screen before L48; Rook speaks, not voiced):

<!-- data: teaser -->
> **Rook:** "Vorne. All the way out here. Some people just can't retire."
<!-- /data -->

The plan buys the **Swarm Tail L5** (the level's `rear`) and fits Rook's owned **Mortar** for the citadel's ground
([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)): 25 of 27 MW. The intel panel's
`SPECIAL LIMITED` row stays (`NO COVER`).

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (Chimeras, Harrows, Wraiths, the swarms; the mid-boss) and `ground` (the grown decks: Graft Turrets, Spine Turrets, Polyp Mortars and the gold reactor cores) |
| Attack directions | front 87% · sides 1% · rear 12% by the waves' data; the mid-boss's rear sweep |
| Density | 5 |
| Recommended traits | `rear`, `homing` |
| Hazards | The rear waves; the Polyp Mortars' marked impacts; the Harrows' burst rings; the mid-boss's lance, lobs and rear sweep (its document) |
| Boss / mid-boss | mid-boss [Vorne's Chimera](../../../enemies/bosses/vornes-chimera/README.md) (Q5 a) |
| Special limits | `NO COVER` (the Airstrike, the Orbital Lance; J12), as the act's every level |
| Sensor-suite detail | **none**: the Vrell-grown citadel, `air` and `ground`, front and rear · **L1**: + density 5, the hazard "rear waves", the objective `STOP VORNE`, the secondary `CORES 0 / 8`, the mid-boss row, `SPECIAL LIMITED` (`NO COVER`) · **L2**: + Chimera, Harrow, Wraith, Graft Turret, Spine Turret, Polyp Mortar, Needler, Stinger, Skitter, Mote and Choir Seraph portraits; the mid-boss's silhouette (named at the sting) · **L3**: + `rear` and `homing` highlighted, the wave strip marking the rear waves and the arena |
<!-- /data -->

*Varga's intel line* per sensor-suite level (must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Vorne's End. A citadel grown over Ascendancy wrecks." |
| L1 | "Ahead and behind. Live gold cores. Something in the spire." |
| L2 | "Chimeras, Harrows, Wraiths. Grown turrets on the decks." |
| L3 | "Guard your rear. Missiles chase what comes up behind." |

**Trait coverage** (`TraitCoverageTest`, the lesson): `rear` and `homing` are both covered by the plan's own fit: the
Swarm Tail L5 (`rear` and `homing`, bought at this visit) and the Micro-missile Pod L5, within the Mk VI's 27 MW. The
sketch's `spread` is left out (the plan's beam does not cover it; the alternatives are in *Open questions*): the
rear waves rise in wings the homing tail takes, and the mid-boss's rear sweep climbs behind the ship, where the tail
and its missiles meet it.

## Objective

- **Primary** `reach-end` with the mid-boss as its condition (Level 19's and Level 40's rule: the level cannot go on
  while it lives): reach the spire's throat. The briefing and the objective line read `STOP VORNE`; the mid-boss bar
  reads `VORNE'S CHIMERA` with its three segments ([HUD](../../../ui/hud/README.md#act-7-additions-draft)).
- **Secondary** `escapes` (the reactor cores): **10** gold reactor cores sit in the decks' wrecks in five pairs
  (t≈50, 70, 92, 126 and 150). The secondary is met when **8** are destroyed before they scroll off
  (`escapes: reactor-core`, `allowed: 2`, as Level 39's pumps), met as soon as it can no longer fail (the eighth down:
  `DONE` in green), failed at once by the third away (`FAILED` in red). The HUD counts to its target (the Act 5
  lesson): **`CORES n / 8`**. **Readable on screen** (the round 80 lesson): each live core sends pulses of teal light
  up its veins toward the spire (what it does, without a word), the veins go dark when it dies, and while a live core
  is in the field's bottom 120 px its glow flickers and the tracker's well blinks amber. Decided by ≈ t=160 at the
  latest. Pays **100** credits in Act 1 terms (**537** at this level's scale); the typical haul counts 50 %. Easy: 6 of
  10 (`allowed: 4`); hard: all 10.
- **No cover** ([specials](../../../player/specials/README.md#act-7-specials-draft)): the Airstrike and the Orbital
  Lance read `NO COVER`; the Swarm Call works (its drones hunt the Chimera's open parts, the boss's rule).

### The reactor core

A level object of this level (the citadel kit), not counted as an enemy: built as a unit page of its own,
[Reactor Core](../../../enemies/ground/reactor-core/README.md) (the secondary's `escapes` counts a unit by its slug, as
Level 39's pump station; uncounted, `counted: false`, as Level 46's seed casket).

| Field | Value |
|---|---|
| Layer, size | `ground` (hit by every weapon that hits `ground`, `anti-ground` ×2; no contact), 40×40 px, hitbox 32×32 |
| HP | **40** (easy 30 / hard 52, the global multipliers); a level object: no act or beam-era factor |
| Placement | in pairs, one in each half of the field, set in a gold wreck's open hull on the decks; each pair beside a turret nest (t=55, 67, 91, 127, 148), never under it |
| Fire | none; never hurts anything |
| Look | a round gold reactor housing, dulled and cracked, teal growth clamped over it; its core glows gold-white, and teal veins run from it up the screen toward the spire, pulsing upward every 1.5 s (smooth, never bullet-sized dots) |
| Credits | **10** each (Act 1 terms), counted with the ground targets' share (80 %) in the typical haul |
| Destroyed | a short gold-white flash and a debris ring (vacuum); the veins from it go dark from the core outward over 1 s |

The cores do not change the fight (drafted; *Open questions*): the secondary is the citadel's story told by its
veins, and the boss's tuning stays the boss document's.

## Layout

Scroll **120 px/s** over the outer growth, **110 px/s** over the gold decks, **100 px/s** on the spire road, eased to
a **halt** over t=180–186 at the citadel's heart (the arena), then up to **140 px/s** over 2 s out through the spire;
each change eased over 2 s. The setting is [Vrell space](../../../world/vrell-space/README.md#sub-locations-per-level)'s
Vrell-grown citadel in Q10 b's **L48 light** ([moods](../../../world/vrell-space/README.md#light-per-level-q10-b)):
teal-black growth (the palette) over dull gold wreckage (not Vrell: its own colour, lit by the Vrell light); the wreckage a step paler and less saturated than the boss's gold
(gold-on-gold, Act 6's rule), the cores' gold-white the brightest gold below the boss. Medium: **vacuum** (short
flashes and debris rings, no smoke; Vrell matter spreads and fades). Motion budget: the rear waves and the turrets are
the strong elements; the towers on `deep` pass slowly, the veins pulse slowly.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Outer Growth | 0–44 | 0–5,280 | 120 | clear | `ground`: the first grown decks, ridged teal-black tissue over buried gold plating, side chasms. `deep`: the citadel's towers rising ahead. | The regulars; the first turret nests; the first rear wave (t=17). |
| 2. The Gold Decks | 44–116 | 5,280–13,210 | 110 | clear | `ground`: decks laid over whole gold hulls, open hulls with the reactor cores (t≈50, 70, 92), veins running up the screen. `deep`: towers of growth and wreckage. | Rear wings and Harrow streams; Graft Turret nests by the cores. |
| 3. The Spire Road | 116–176 | 13,210–19,220 | 100 | clear | `ground`: a broad grown causeway, the last cores (t≈126, 150) and Vorne's archive (the secret, t≈138); every vein converging ahead. `deep`: the spire, a tower of growth round a gold mast. | The Harrow cross (t=133); the last approach wave at t=169. |
| 4. The Spire Well | 176–178 | 19,220–19,420 | 100 | clear | `ground`: the citadel's heart, the spire well at the top centre, its tissue lid veined with gold light. | The pre-arena pause (t≈177–186); Varga's warning at t=178. |
| 5. The Spire Well, braking | 178–186 | 19,420–19,900 | 20 | clear | `ground`: as section 4, the well's lid coming to the top centre. | The scroll eased to a halt; the pause holds. |
| 6. The Heart | 186–187 | 19,900–19,920 | 20 | clear | `ground`: as section 4, held still; the lid splits at the sting. | The sting at t=186, the mid-boss (≈ 67 s at medium); the clock halted while it lives. |
| 7. Through the Spire | 187–248 | 19,920–28,340 | 140 | clear | `ground`: the spire's inner wall, rings of tissue round the dead gold mast, sliding by. `deep`: a pale shimmer at the top (Level 49's memory) by t≈235. | The calm to t=191, then the Choir's answer (rear wings, a Seraph pair, Wraiths); the last wave at t=235; the level ends at t=248 in the spire's throat. |
<!-- /data -->

**As built** (step F2, accepted by the user in round 93; the Chimera's fight itself is to be reworked, *Open questions*): a boss's arena is not a timed halt (the built clock), so
the braking section eases from 100 to **20 px/s** over **t=178–186** (Level 40's 20 px/s duel) and the arena section
(t=186–187) holds 20 px/s until the clock halts at its end: the scroll stands at ART-F's halt row (19,920) as in the
proposal's 100 → 0 ease over t=180–186; the heart piece's time is moved to keep its rows.

**The arena and the clock** (the [boss document's arena](../../../enemies/bosses/vornes-chimera/README.md#arena)): the
last approach wave enters at t=169 and is gone by ≈ t=177; no wave after it until the fight is over, so the field is
clear when the boss's warning starts. **The warning before it shows**: Varga's line at t=178 (energy rising in the
well), then at **t=186** the **mini-boss sting** (track 21) as the scroll halts and the spire well's lid splits with
a gold light under it, Vorne's first line, the bar `VORNE'S CHIMERA` and the **boss checkpoint** (none on hard); the
Chimera rises out of the well over 4 s, invulnerable until in place. The level clock **halts at t=187** while it
lives; `hold_after_boss: 8` (Vorne's last line ≈ 4.5 s, the 2 s chain, the credit shower) keeps it halted after the
kill, then the clock resumes at t=187 and the scroll eases up to 140 px/s over 2 s. The first wave after him enters
at **t=191**: **≥ 4 s of calm** after the hold, ≥ 12 s after the burst. **K** below is the capsule's death.

**Rook and the escort.** Rook flies his slot with the Mortar ([Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft))
and follows the mid-boss's [escort rules](../../../enemies/bosses/vornes-chimera/README.md#the-escort-rook) (the Dart
bay first, Trail during the rear sweep). With `--escort none` the level is the same without him and his lines.

The pre-arena pause, the calm after the fight and the end's calm are the level's three planned empty stretches over
2 s (the pacing rule's limit; see *Waves*).

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). The table is rendered from the level's [data.yaml](data.yaml) (M11 part F, step
F2): the approved 72 rows (the turret nests as ground targets, *Ground targets*) and step F2's **density waves**
(marked "Step F2: density", the final-act rule: varied mixed waves on the offbeats, front only, off the centre line
while the Seraph pair holds). **Measured** (`levelReport`, the plan's fit): medium 549 enemies (517 in waves, 32 in
nests), 2.74 on the screen, 29 shots a minute, three empty stretches over 2 s (the pre-arena pause, the calm after
the fight, the end's ≈ 10 s); easy 1.48 on the screen; hard 4.96.

- **HP** from the proposed Act 7 reference DPS (J11: **192** at L48,
  [Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The **act HP factor** for the
  returning `medium` units ([balancing basis](../../../enemies/README.md#balancing-basis)): Wraith ×192/66 (16 → **47**),
  Choir Seraph ×192/188 (90 → **92**). The `small` units fly the beam era's ×2 as built (Needler 8, Stinger 12,
  Chimera 40, Harrow 16, Graft Turret 24, Spine Turret 16, Polyp Mortar 20); the Skitters and Motes are `tiny`. No hex
  shields (J4). The mid-boss's numbers are its document's.
- **The Vrell's own** (round 72's turn): the Chimeras, Harrows and Graft Turrets are the Vrell's hybrids, grown on the
  Ascendancy's wrecks; **no Ascendancy unit flies** in the waves (the faction is broken): the only gold craft are the
  [Dart Drones](../../../enemies/air/dart-drone/README.md) the Chimera's bay launches, the boss's.
- **Density and pacing** (the Act 2+ floor): **452 enemies** over the 248 s of level clock (the fight excluded),
  **≈ 109 a minute** at medium (the act's minimum 80; Act 6's band 100–118); the boss-launched Darts and Chimeras
  count into the level's enemies, not its density (the boss's rule). A wave every 3 s; gaps of 2–4 s. Waves by
  direction: front 54 of 72 (75 %, the turret nests scroll in from the front), rear 17, every edge 1: the ≥ 60 %
  front rule holds. **Empty stretches over 2 s** (at most three): the pre-arena pause (t≈177–186), the calm after the
  fight (t=187–191 of clock, after the 8 s hold) and the end's calm (t≈241–248). The floor from the Needlers' thorns,
  the Chimeras' aimed pairs, the Stingers' fans, the turrets, the mortars and the Harrows' rings: expected to meet
  ≥ 2.5 enemies on the screen and ≥ 10 shots a minute (`PacingTest`, `ActFloorTest`), **to be confirmed with
  `levelReport`** in the build.
- **Rear waves** (the level's `rear`): every rear wave has the BOTTOM edge warning 3 s ahead (the Wraiths their own
  flight, as built); never two rear waves within 6 s of each other, and none in the 2 s before a turret nest scrolls
  in, so the ship is never pinned between a nest's fire and a wing rising behind it; a rear Chimera wing rises in
  the lane the ship is **not** in (its V's tip ≥ 120 px off the ship's x at the warning).
- **Spacing** (the Act 5 lesson): the level's `medium` air hulls are the Wraiths and the t=210 Seraph pair: never two
  Wraith ambushes on the screen, and no Wraith while the pair holds (the t=229 ambush follows it). At most two Chimera
  groups on the screen ([the Chimera](../../../enemies/air/chimera/README.md)'s rule), at most 6 Harrows. In the
  fight nothing enters but what the boss launches (its fire rules keep its Chimeras ≥ 80 px off its hull).
- **Calm at the end** (the Act 5 lesson): the last wave (a rear Chimera wing) enters at t=235 and is gone by
  ≈ t=241; **≥ 7 s** of calm before the level end.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | Over the citadel's outer growth |
| 4.5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 6 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 7.5 | 1 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 9 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 10.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 14 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 15.5 | 1 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step F2: density, the final-act rule (varied mixed waves) |
| 17 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | rear | The first rear wave: up from the bottom edge in the lane the ship is not in, BOTTOM warning 3 s (Rook's call) |
| 18.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 20 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 21.5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 23 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 24.5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 26 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 27.5 | 1 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 29 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | rear | From the bottom edge, 0.6 s apart |
| 30.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 32 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 33.5 | 1 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step F2: density, the final-act rule (varied mixed waves) |
| 37 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 38.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 40 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 41.5 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 43 | 1 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | |
| 44.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 46 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Over the gold decks; the first reactor cores (t≈50) |
| 47.5 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 49 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 50.5 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 52 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 53.5 | 2 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step F2: density, the final-act rule (varied mixed waves) |
| 58 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | rear | |
| 59.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 61 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 62.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 64 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 65.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 70 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 71.5 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 73 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear | |
| 74.5 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 76 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 77.5 | 2 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step F2: density, the final-act rule (varied mixed waves) |
| 79 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front | |
| 80.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 82 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | Drops the Shield cell |
| 83.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 85 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 86.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 88 | 2 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front | The Chimera swarm; at most two Chimera groups on the screen |
| 89.5 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 94 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 95.5 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 97 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | rear | |
| 98.5 | 2 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step F2: density, the final-act rule (varied mixed waves) |
| 100 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 101.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 103 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 104.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 106 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 107.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 112 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear | |
| 113.5 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 115 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 116.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 118 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The spire road |
| 119.5 | 3 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step F2: density, the final-act rule (varied mixed waves) |
| 121 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 7 | front | |
| 122.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 124 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 4 | rear | |
| 125.5 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 130 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 131.5 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 133 | 3 | cross | [Harrow](../../../enemies/air/harrow/README.md) | 8 | all edges | 2 per edge, warned on every edge |
| 134.5 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 136 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 137.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 139 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front | |
| 140.5 | 3 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step F2: density, the final-act rule (varied mixed waves) |
| 142 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | rear | |
| 143.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 145 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 146.5 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 151 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 152.5 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 154 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 155.5 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 157 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | |
| 158.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 160 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 161.5 | 3 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step F2: density, the final-act rule (varied mixed waves) |
| 163 | 3 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front | The leader drops the Overdrive |
| 164.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 166 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear | |
| 167.5 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 169 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 10 | front | The last wave before the arena; gone by ≈ t=177 |
| 186 | 6 | mid-boss | [Vorne's Chimera](../../../enemies/bosses/vornes-chimera/README.md) | 1 | the spire well at the top centre (its rise, 4 s) | At the arena's start: the sting, the bar and the checkpoint; nothing enters in the fight but what it launches |
| 191 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The first wave after him (the clock resumed at t=187: ≥ 4 s of calm after the death's hold) |
| 192.5 | 7 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 194 | 7 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | rear | |
| 195.5 | 7 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 197 | 7 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 198.5 | 7 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 200 | 7 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear | |
| 201.5 | 7 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step F2: density, the final-act rule (varied mixed waves) |
| 203 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 204.5 | 7 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 206 | 7 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 207.5 | 7 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 210 | 7 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | The Choir's elites come for the wing (L47's); gone by ≈ t=225; no other medium hull on the screen meanwhile |
| 211.5 | 7 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 213 | 7 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | Along the left edge while the pair holds (off the centre line) |
| 214.5 | 7 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 216 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Down the left third while the pair holds |
| 217.5 | 7 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 219 | 7 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front (right) | Along the right edge while the pair holds |
| 220.5 | 7 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 223 | 7 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear | Step F2: t=223, not 222: its warning clear of the hard pair's landing at t=219 |
| 224.5 | 7 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 226 | 7 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 227.5 | 7 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 229 | 7 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | After the pair has gone; hard: a Harrow stream instead (the pair's longer hold) |
| 229 | 7 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear | Hard: the Wraiths' slot as a Harrow stream from the bottom edge (the Seraph pair's ≈ 21 s hold) |
| 230.5 | 7 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 230.5 | 7 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step F2: density, the final-act rule (varied mixed waves) |
| 232 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 233.5 | 7 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step F2: density, the final-act rule (varied mixed waves) |
| 235 | 7 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 7 | rear | The last wave; gone by ≈ t=241, calm to the level end |

Totals: Skitter 185 · Needler 98 · Mote Swarm 120 · Lamprey 44 · Stinger 58 · Spore Bomber 27 · Chimera 87 · Harrow 37 · Wraith 16 · Choir Seraph 2 · Vorne's Chimera 1.
<!-- /data -->

Totals: Skitter 130 · Mote Swarm 120 · Chimera 71 · Harrow 37 · Needler 30 · Graft Turret 18 · Wraith 16 · Stinger 14 ·
Spine Turret 8 · Polyp Mortar 6 · Choir Seraph 2 (= 452); plus the 10 reactor cores (not enemies) and the mid-boss's
launched units (paper ≈ 18 Darts in phase 1, ≈ 8 Chimeras in phase 2).

## Ground targets

The decks' turret nests (in *Waves*: Graft Turrets, Spine Turrets, Polyp Mortars, 32 in all) and the **10 reactor
cores** (*The reactor core* above), not counted as enemies. The Graft Turrets regrow unless their root is burnt (the
unit's rule): Rook's Mortar and the Tesla arc's sparks take the roots.

## Hazards

- **The rear waves** (the level's): Chimera wings and Harrow streams rising from the bottom edge, Wraiths decloaking
  behind; each told (*Waves*).
- **The Polyp Mortars' marked impacts** and **the Harrows' rings**: the units' own, as built.
- **The mid-boss** (its document): the lance's told line, the bile lobs' marks, the rear sweep's bottom-edge glow.

## Boss / mid-boss

Mid-boss: [Vorne's Chimera](../../../enemies/bosses/vornes-chimera/README.md) (Q5 a: an air craft grown from the
lifeboat, three phases). Its parts, playbooks, tells, HP, the escort's rules, the death and the order and wording of
Vorne's lines live in the spec. Arena notes for this level:

- The fight starts at t=186 with the sting, the bar and the checkpoint (none on hard), after the pre-arena pause; no
  wave enters from t=169 on (*Layout*). Nothing slides in during the fight but what the boss launches.
- **Traits**: `rear` (the rear sweep climbs behind and beside the ship: the Swarm Tail L5's moment, 55 rear DPS) and
  `homing` (its Darts and Chimeras, the mirrors' open far sides); the beam and the Tesla pod's arc (the capsule's hex
  shield) carry the rest. The EMP is not in the plan's fit (the Swarm Call is): its drones hunt the open parts.
- **Bounty**: the spec's formula (15 % of budget(48) ÷ (16.777 × `bounty_scale`)) with this level's hand scale
  **0.32** (below) gives **470 in Act 1 terms** (shares 40 / 25 / 35: each battery 44, the rack 44, the bay 56; each
  sac 39; the capsule 165), paid **2,523 = 15.0 %** of budget(48). The spec assumed 0.40 (376); the main agent
  carries the change to the spec. The build recomputes it with the measured scale: **885** at **0.17** (step F2), paid
  2,524 = 15.0 %.
- **Par**: the spec's paper ≈ 67 s (the bar to the capsule's death), the window 45–75 s; measured in step F2: **61 s**
  (five seeds 55–72 s at medium, `ChimeraFightTest`); after the fight's rework (step F5): **59 s** (five seeds
  56–64 s; the level's pacing outside the fight unchanged: medium 2.67 on the screen, 33.7 shots a minute, 3 pauses).
- **Death**: the spec's: the bullets fade at K, Vorne's last line breaks off at the burst, the credit shower; the
  clock's 8 s hold covers it (*Layout*). Track 34 cuts with his line; the level's track resumes after the calm.

## Secrets and pickups

- **Vorne's archive** (t≈138, x 380, in an open gold hull on the spire road): an Ascendancy data vault, gold and
  sealed, half overgrown; a small `ground` object (30 HP, like a small crate), broken it frees a hidden crate worth
  **100** in Act 1 terms (**1,678** in Act 7: a crate is not scaled, as Levels 42 and 46). The Salvage scanner's glint marks it. Varga's event line; what
  the files say is never told (Vorne hints, he never explains: [the Silence's rule](../../../story/README.md)).
- **Shield cell**: dropped by the t=82 Wraiths' last.
- **Overdrive**: dropped by the leader of the t=163 Chimera swarm.
- **Armour patch**: the mid-boss's (dropped at the fusion's pain, its document).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), drafted for the **1 s rule** (≈ 12 characters a second, a
1.5 s lead, 0.4 s of silence; event lines drop after 6 s); `RadioTimelineLevel48Test` checks them with Rook and
without. **The radio's noise** (J3): step **5** (held from L47), the subtitles clean; the Choir's channel clean.
**Speakers**: Okafor (from the *Steadfast*, behind), Varga, Rook, **Vorne** on the enemy channel in his fused voice
(the HUD's amber plate; J7) and the Choir twice. **Vorne's nine lines are the
[spec's](../../../enemies/bosses/vornes-chimera/README.md#vorne)** (order, wording, triggers, the one shout, the last
line cut by the burst): this table places them and adds the friendly lines between them. Rook's barks carry
`requires: escort`. **Clock times after t=187 are the clock's after the fight** (it halts at t=187 while the Chimera
lives, then 8 s more).

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, Steadfast. The citadel grew over the wrecks Vorne brought through. His lifeboat ran here." |
| t=13 (bark, `requires: escort`) | Rook | "Movement under us. Things come up from behind in here." |
| t=21 | Varga | "The gold reactors in those wrecks are still live. The growth is drinking from them." |
| t=33 | Okafor | "Then starve it. Burn every core you pass, Aegis." |
| t=100 | Okafor | "Halfway to the spire. Keep your tail clear, Lancer." |
| t=120 | Varga | "The spire is the citadel's heart. Every vein in here runs into it." |
| t=150 (bark, `requires: escort`) | Rook | "If he's in there, he knows we're coming. Vorne always knew." |
| Secondary met (`secondary-objective`, the eighth core down) | Okafor | "Cores are dark. Whatever they were feeding is starving now." |
| Secondary failed (the third core away) | Varga | "Cores got past us. The growth will feed on them." |
| t=176 | Varga | "Energy rising in the spire well. Something is coming up through it." |
| t=186 (The sting (t=186, before it shows): the boss document's line 1) | Vorne | "Lancer. You followed me all the way in. How loyal. How very Coalition." |
| In place (`boss-arrival`): line 2 | Vorne | "Look what they grew for me. I asked for a ship. They gave me a body." |
| `boss-arrival` + 8 s | Okafor | "That's Vorne in there, Aegis. What's left of him. End it." |
| The first phase-1 part down or 15 s into phase 1 (`boss-phase`): line 3 | Vorne | "I kept my side of every bargain I ever made. They kept theirs. Forever, they said." |
| The fusion's pain, its first second (`boss-phase`): line 4, the Choir | The Choir | "…he is ours… he sings with us… he is afraid with us…" |
| Right after it: line 5, **shouted** (the voice check's one Act 7 exception) | Vorne | "Get out of my head!" |
| The first sac dead or 10 s into phase 2 (`boss-phase`): line 6, strained | Vorne | "You think this war was about Earth? Earth was the price, Lancer. I was buying time." |
| The capsule opens (`boss-phase`): line 7, cold again | Vorne | "One last deal, Lancer. Walk away, and I'll tell you what's out there." |
| The capsule below half its HP (`boss-phase`): line 8 | Vorne | "I built an empire out of three moons and a grudge. I will not end as a song." |
| A rear sweep's tell (`rear-sweep`, the first, third ...: bark, `requires: escort`) | Rook | "He's dropping! Watch the bottom edge!" |
| The second, fourth ... sweep's tell (bark, `requires: escort`) | Rook | "Under you, Lancer! Coming up the far side!" |
| K, the capsule's death (`boss-phase` `last-words`): line 9, cut at the dash by the burst | Vorne | "Do you know what's coming after them, pilot? Do you have any idea what I was trying to—" |
| The burst (`boss-destroyed`, K + 4.5 s) + 5.5 s: t=189 of the clock after the fight (its 8 s hold from K), `requires: escort` | Rook | "He was trying to tell us something. Right to the end." |
| t=198 | Varga | "At the end he was afraid, Rook. Afraid like they are." |
| t=207 | Okafor | "Vorne's war is over. Ours isn't. Out through the spire, Aegis." |
| t=219 (enemy channel) | The Choir | "…he sang with us… now he is quiet…" |
| t=236 | Varga | "Past the spire my readings make no sense. Like flying into a memory." |
<!-- /data -->

**As built** (step F2, `RadioTimelineLevel48Test`, the 1 s rule; accepted by the user in round 93): Rook's bark at
t=13, Varga's at t=21 and Okafor's at t=33 (the drafts' 11, 19, 29: each waited 1.2–1.7 s behind the line before);
Varga's warning at t=176 (not 178: Vorne's line 1 at the sting waited 2.7 s behind it); after the fight Rook's line on
the burst + 5.5 s (t=189 of the clock, now an event: a run without the fight held Vorne's sting line over a timed
t=189), Varga at t=198, Okafor at t=207 and the Choir at t=219 (the drafts' 195, 202, 212).

Timeline check (text): the timed lines close at ≈ 10.5, (17.0), 27.4, 34.5, 105.8, 127.0, (156.4) and 185.1 before
the fight, and 194.9, 200.9, 208.7, 216.3 and 243.2 after it; nothing overlaps, Varga's warning closes before the
sting, and the last line closes 4.8 s before the level end. In the fight the spec's triggers place Vorne's lines;
Okafor's and Rook's lines there are event lines that wait for a free slot (6 s) and drop rather than cut Vorne. The
secondary resolves between ≈ t=126 and t=160, in the free stretch after t=127.0 (a failure earlier waits up to 6 s).
The Choir's t=212 line answers its line 4 in the fight ("*…he is ours…*"); Rook's and Varga's lines after the death
are the act's tragic beat (the act's *Story beats*: "a tragic last line"); Vorne's own hints stay hints.

## Music & ambience

The Act 7 A theme (**track 16**, J8; the cues are D-audio's
[music](../../../audio/music/README.md#cues-per-level-act-7)) over the approach: its base stem in section 1, the full
mix over the gold decks, thinning to the base on the spire road's last stretch and out under Varga's t=178 warning.
The **mini-boss sting** (track 21) at t=186, then **track 34** (Vorne's last stand, J8) from the sting's end to K,
where it cuts with his line (the spec); the bed alone for the calm; track 16's full mix again from t=191 to the level
end. Ambience: the **`vrell-hive`** bed ([sfx](../../../audio/sfx/README.md#ambience-per-setting)), the living
growth's (the reef's and the hive core's too). New sounds this level needs, for the act's sfx step: the reactor core's
death (a short metallic crack and a fading electrical hum), the archive's breaking (a metal case wrenched open), the
mid-boss's (its document's brief); Freesound only for real things (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(48) = 700 × 1.07⁴⁷ ≈ **16,832** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in
**Act 1 terms** (Skitter 5, Mote 2, Needler 12, Stinger 15, Chimera 14, Harrow 12, Wraith 30, Choir Seraph 35, Graft
Turret 15, Spine Turret 12, Polyp Mortar 15, Dart Drone 2, the reactor core 10, the mid-boss 470, the crate 100, the
secondary 100), × the act factor **16.777** (1.6⁶), bounties also × the scale (the crate is not scaled). A **`bounty_scale` of 0.32** (air
kills and the launched units 60 %, ground targets 80 %, the mid-boss 100 %, the secret and the secondary 50 %; a first
estimate for the build to confirm with `TypicalHaulTest` and `levelReport`) puts the typical haul at **17,601
(+4.6 %)**, the perfect run at 27,029 (1.61 × budget). The mid-boss keeps its **15 %** share (the band 10–20 %,
`BalanceTest`).

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 185 × 5 + Needler 98 × 12 + Mote Swarm 120 × 2 + Lamprey 44 × 12 + Stinger 58 × 15 + Spore Bomber 27 × 25 + Chimera 87 × 14 + Harrow 37 × 12 + Wraith 16 × 30 + Choir Seraph 2 × 35 | 18,863 | 11,318 |
| Ground targets: Graft Turret 18 × 15 (+ root 5) + Spine Turret 8 × 12 + Reactor Core 10 × 10 + Polyp Mortar 6 × 15 | 1,846 | 1,477 |
| Mid-boss: Vorne's Chimera (capsule 309 + battery-l 83 + battery-r 83 + rack 83 + bay 105 + sac-l 74 + sac-r 74 + sac-spine 74) | 2,524 | 2,524 |
| Secret: Vorne's archive (hidden crate, 1% of budget) | 1,678 | 839 |
| Secondary: burn 8 of the 10 reactor cores | 1,678 | 839 |
| **Total** (bounty scale 0.17) | **26,589** | **16,997** |
| Budget(n) = the typical haul's target; typical +1 %, perfect 1.58 × budget | | 16,832 |
<!-- /data -->

**Measured** (step F2, `levelReport`): with the density waves (*Waves*) the typical haul is on budget at the scale
**0.17** (16,795, −0.2 %; perfect 1.56 × budget); the hand estimate 0.32 above was the approved 72 waves'. **The
secondary is paid unscaled** (1,678, the built rule, as Levels 43–47: only bounties take the scale; the hand table
scaled it to 537), and the mid-boss's bounty is recomputed by its formula at the measured scale: 2,525 ÷ (16.777 ×
0.17) = **885** in Act 1 terms (the shares 40 / 25 / 35 kept), paid 2,524 = 15.0 %.
**Balance plan**: the L48 visit buys the Swarm Tail L5
(14,000) and fits Rook's Mortar: forward 198 against the proposed reference **192** (1.03), rear **55**, 25 of 27 MW
([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The level expects `rear` (the rear
waves, the rear sweep) and `homing`; the plan's fit covers both.

## Difficulty notes

- **Easy**: the mid-boss's easy hooks (its document: no rear sweep in phase 3, slower rests); the rear waves' warnings
  as ever; the secondary 6 of 10 (`allowed: 4`); the formation lever's smaller waves; at most two Chimera groups as
  ever.
- **Hard**: the mid-boss's hard hooks and no boss checkpoint; the secondary all 10 (`CORES n / 10`); the Seraph pair's
  hard hooks (a ≈ 21 s hold), so the t=229 Wraiths fly as a Harrow stream of 5 from the bottom edge; the other waves by
  the formation lever, trimmed by hand so never more than 6 Harrows and two Chimera groups are on the screen and no two
  rear waves come within 6 s.

## Concept art

Round 84's picks (decided 2026-11-05): Vorne's Chimera per phase in its document and Q10 b's one-palette sheet (the
light of this level) in [Vrell space](../../../world/vrell-space/README.md) (ART-R-B).

Round 93 (M11 part F, step ART-F, 2026-11-06; production art, accepted by the user in round 93 F.2–F.4): the citadel kit, the reactor
cores and their veins, Vorne's archive and the gold hulls by `tools/art/act7_citadel.py` (`assets/backdrop/level-48/`;
the core's and the archive's sprites held in `build/art-held/` until step F2's data claims them; the `backdrop:` block,
the sections (section 4 split at t=180 where the ease to the halt starts, as Level 40's braking section) and the
cores' and the archive's places in `concept/backdrop-proposal.yaml` for step F2). The Chimera's production art is in
[its document](../../../enemies/bosses/vornes-chimera/README.md#concept-art), Vorne's fused portrait in the
[characters](../../../story/characters/README.md#concept-art) and the two briefing images in the
[briefing](../../../ui/briefing/README.md#concept-art) (`briefing-images-final-r93-a`). Every tile and piece passed a
row/column straight-line scan (no run of 120 px; the longest 58 px, a core's vein). Pending the user's review: the wreckage's plating
drawn as buckled plates (seams as bent noise lines, never Voronoi's straight edges); each core's vein a 560 px piece
from the core up toward the spire, the tiles' own veins carrying on beyond it; the deep's towers seen only through
the side chasms (the decks are solid where the turrets stand). Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r93-a.png](concept/backdrop-final-r93-a.png) | Production backdrop: twelve screens at half size with stand-ins (the outer growth, the gold decks with the cores in their hulls and their veins, two cores dead and their veins dark, the spire road and its side chasms, the heart at the halt with the lid splitting, the spire's inner wall round the dead gold mast, the pale shimmer at the end), the four tiles and the heart piece (sheet) | chosen |
| [concept/backdrop-final-r93-a.gif](concept/backdrop-final-r93-a.gif) | The level in a time-lapse, one frame every 3 s, at half size (the cores burnt in passing, the halt, the spire) | chosen |
| [concept/l48-props-final-r93-a.png](concept/l48-props-final-r93-a.png) | The reactor core (whole, cracked, dead) and its breathing glow, its death (flash, debris ring, chips), Vorne's archive (sealed, cracked, open) and its break, the three open gold hulls and the archive's, a core's vein (the pulse up toward the spire, going dark from the core outward), the spire well's twelve frames (sheet) | chosen |
| [concept/l48-props-final-r93-a.gif](concept/l48-props-final-r93-a.gif) | A core in its hull pulsing up its vein, then burnt: the flash, the vein going dark; the archive cracked and broken | chosen |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (the four deep tiles, the four ground tiles, the seams, the heart, the spire well, the veins and the hulls with their places), the sections, the cores' and the archive's places, for step F2 (not data) | proposed |
| [concept/rear-wave-capture-r93-a.mp4](concept/rear-wave-capture-r93-a.mp4) | Game capture (step EF5, real time, with sound): the first rear wave, a Chimera V-wing rising from the bottom in the half the ship is not in | chosen |
| [concept/core-veins-capture-r93-a.mp4](concept/core-veins-capture-r93-a.mp4) | Game capture (step EF5, real time, with sound): a reactor core pulsing up its vein, burnt, its vein going dark | chosen |

## Implementation

- [x] Level data `data.yaml` (M11's build, part F): sections and speeds with 2 s eases and the halt, waves, radio,
      budget, threat profile, briefing, backdrop, music; `medium: vacuum`, the `beyond-gate` limits (`NO COVER`)
- [x] The reactor cores: 10 level-owned `ground` objects in five pairs, paying, not counted as enemies; their veins'
      pulse toward the spire and going dark; the secondary (`escapes: reactor-core`, `allowed: 2`, `CORES n / 8`
      counting to its target, the well's amber blink while a live core is in the bottom 120 px; easy 6 of 10, hard 10)
      (steps F1, F2: `CitadelChecks`, `Level48Test`; the veins step F4)
- [x] The arena: the halt (as built: 100 → 20 px/s over t=178–186, *Layout*), the sting, the bar, the checkpoint (none on hard), the clock halted at
      t=187 while the Chimera lives, `hold_after_boss: 8`, the first wave after at t=191
- [x] Wave script: ≥ 80 enemies a minute at medium (`DensityTest`), the Act 2+ floor (`PacingTest`, `ActFloorTest`)
      with its three named empty stretches, ≥ 4 s calm after the last enemy, ≥ 60 % front, the rear waves' rules, no
      two `medium` hulls overlapping
- [x] Radio voiced with the step-5 noise and Vorne's fused voice (the shout's exception), every timed line at most 1 s
      late (`RadioTimelineLevel48Test`, with Rook and without); Vorne's lines on the spec's triggers; the line cut by
      the burst
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`);
      `TraitCoverageTest` covering Level 48 (step F2; the full `check` green, step EF5)
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images (steps F2,
      ART-F)
- [x] Typical haul within ±5 % of budget(48) = 16,832 with the level's `bounty_scale`, the mid-boss at ≈ 15 %
      (`TypicalHaulTest`, `BalanceTest`)
- [x] Music and ambience: track 16 by section, the sting, track 34 cut at K, the `vrell-hive` bed, the level's
      sounds (`Level48SoundsTest`) (steps MUS-EF, F4)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level and the fight with Rook on every
      difficulty and with `--escort none`
- [x] Production art and captures for the build's round (the fight as one real-time recording, round 81's rule)
      (steps ART-F, F4, EF5: [round 93](../../../concept-rounds/round-93/README.md), closed 2026-11-07)
- [x] The Chimera fight reworked (round 93 L.3 change; step F5: every part its own gun, silent at its death, two told
      moves at once, new attacks, it enters firing; 226–260 shots a minute at medium from 43–59), then the level
      re-measured (the par 59, the pacing unchanged, the bounty kept); to be shown again in the next round

## Open questions

- None: the five drafted questions (traits, bounty scale, cores and the fight, Vorne before the sting, length) were
  decided as drafted by round 84 (*Decisions*).
- **The Chimera fight's rework** (round 93, L.3): the user finds the fight has far too little action (see the [Chimera](../../../enemies/bosses/vornes-chimera/README.md#open-questions)); the rework changes the arena's fight, not the level's waves, and may change the fight's length, the bounty share and the level's measures. **Done** in step F5 (the Chimera's *Duration and HP*; the par 59, the bounty kept), to be seen again in the next round.

## Decisions

- 2026-11-04 (Act 7 design phase, step `m11-d-levels-3`; decided 2026-11-05, round 84): promoted from the
  act roster to a draft level document, hand tables only, no `data.yaml`. Air and ground, front and rear (75 % front);
  six sections, 248 s of clock plus the fight, 452 enemies (≈ 109 a minute); rear Chimera wings, Harrow streams and
  Wraiths; Graft Turret, Spine Turret and Polyp Mortar nests on the grown decks; the 10 gold reactor cores as the
  secondary (`CORES n / 8`) told by their veins; the arena at t=186 with the warning lines, the sting and the clock's
  8 s hold; Vorne's lines placed on the spec's triggers; the Choir's answer after him (a Seraph pair, its t=212 line);
  Vorne's archive as the secret; traits `rear` and `homing` (the plan's own fit; the sketch's `spread` left as the
  alternative); `bounty_scale` **0.32**, so the spec's bounty becomes 470 (15 %) by its formula.
- 2026-11-04 (step `m11-d-act`, decided 2026-11-05, round 84): Vorne's archive's crate is not scaled (1,678, as Levels 42 and 46):
  typical 17,601 (+4.6 %); the secondary's scaling flagged for part F (*Credit budget*).
- 2026-11-04 (step `m11-d-act`, decided 2026-11-05, round 84): the bed is `vrell-hive` (the proposed `vrell-citadel` folded into
  it, D-audio's cues).
- 2026-11-05, round 84 (the user's page answers; decided as drafted unless stated, the alternatives rejected). The open items were:
  - **Q5** (round 84, the mid-boss): drafted **a** (an air craft grown from the lifeboat, three phases; the spec);
    **b** rooted in the citadel (a ground boss, no rear play); **c** two stages (over the mid-boss length). The
    [spec's open questions](../../../enemies/bosses/vornes-chimera/README.md#open-questions) hold it; with **b** the
    level's `rear` rests on its waves alone.
- 2026-11-05, round 84 (step F2 moves them here from *Open questions*, as decided): **the traits** `rear` and `homing`
  (the sketch's `spread` rejected); **the bounty scale** as the formula's (the boss step's 0.40 and thinner waves
  rejected; measured 0.17 in the build, below); **the cores and the fight** without a link (no regeneration per live
  core); **Vorne before the sting** silent until the spire well (no taunt on the spire road); **the length** 248 s of
  clock plus the fight, not trimmed.
- 2026-11-07 (M11 part F, step F2; the build, accepted by the user in round 93, C.1): the level's `data.yaml` from the
  approved tables with the density waves (63, the final-act rule: Needler and Stinger wings, Lamprey streams, Spore
  Bomber lines, Skitter trickles and small Chimera wings on the offbeats, front only, at the edges while the Seraph pair
  holds): medium 549 enemies, 2.74 on the screen, 29 shots a minute, the three planned pauses; `bounty_scale` **0.17**
  (typical 16,795, −0.2 %), the secondary unscaled (1,678, the built rule) and the Chimera's bounty **885** by its
  formula. Decided as trivia: the turret nests as ground targets beside the cores (never on them); the reactor core a
  unit page (`counted: false`); Vorne's archive a `ground` destructible of 30 HP with the secret's crate (Level 45's
  courier cyst); the braking section 100 → 20 px/s over t=178–186 (a boss's arena is no timed halt) with the heart
  piece's time moved to keep ART-F's rows; the t=222 rear Harrow stream at t=223 (its warning clear of the hard pair's
  landing at t=219); the t=124 Wraiths pinned at 4 on hard (a rear ambush has at most 4); the popcorn while the Seraph
  pair holds along the edges; the hard t=229 Wraiths as a Harrow stream (skip twins). Steps F1/F1b's open calls,
  logged here as trivia: the rear wave's `lane: away` draws its x at the warning's start from the ship's x, the far
  half's middle (a V's tip ≥ 120 px off the ship); a wide formation's lane is kept on the field at the edge; a
  swooping rear V (the Chimeras) mirrored about its tip so its curve sweeps toward the middle; a rising rear V-wing is
  planned as the front V in the rising frame; `leaving: {bottom_band: 120}` counts the secondary's live units by their
  centre.
- 2026-11-07 (step F1c): the Chimera's cover fire closes the fight's quiet stretches (its document's *Decisions*); the
  Dart stream ends with its bay.
- 2026-11-07 (step EF5): the full `check` green with parts E and F; round 93 opened (the fight's sounds, track 34,
  Vorne's voice, the art, the level and the whole fight in play from a real-time recording with sound, the calls).
- 2026-11-07 (user, [round 93](../../../concept-rounds/round-93/README.md) closed): **S.1 ok**, **M.1 ok**, **V.1, V.2 accept**, **F.2–F.5 accept** (the citadel kit, the reactor cores and archive, the briefing images and the fused portrait: art rows chosen, `art: chosen`), **L.1, L.2, L.4–L.6 ok** (a rear wave, a core burnt, the sweep, the fusion and capsule, the death), **C.1 accept** (a denser Level 48, 63 mixed waves), **C.2 accept** (the Chimera's strength as built). **L.3, the whole Chimera fight: change**, the user's note: "As with previous boss fights, there's way too little action. The boss fires at such a slow rate that you don't even have to dodge the bullets. The shield generates faster than the boss kills it. From what I can see, even doubling the rate will not be enough. Also, the weapon arsenal of the boss seems very limited. Add some other weapons, increase the rate. See if that makes it better." Outcome: **a Chimera fight rework is needed** (more attacks, a much higher fire rate, pressure that beats the shield's regeneration); **open, not started**, recorded in the [Chimera's](../../../enemies/bosses/vornes-chimera/README.md) Open questions and Decisions. `chimera-fight-r93-a.mp4` stays `chosen` as the record of the call. The `design` status is left unchanged (`approved`: no precedent for lowering it).
