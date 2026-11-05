---
title: Level 48 – Vorne's End
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/bosses/vornes-chimera, ../../../enemies/air/chimera, ../../../enemies/air/harrow, ../../../enemies/air/wraith, ../../../enemies/ground/graft-turret, ../../../enemies/README.md, ../../../world/vrell-space, ../../../player/generator, ../../../player/wingmen, ../../../player/specials, ../../../ui/hud, ../../../story/characters/vorne]
updated: 2026-11-05
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

> **Commander Okafor:** "Past the inner shell the Vrell grew a citadel over the wrecks Vorne's fleet brought
> through. His lifeboat's trail ends there. The Choir took him in, Lancer. Whatever it made of him waits at the
> citadel's heart. Bring him down, then get out through the spire."
>
> **Dr. Varga:** "The citadel feeds on the wrecks' gold reactors. Burn the cores and starve it. Its swarms come up
> from behind: the place was grown to close round whatever lands in it. What the Choir does to a mind it takes in,
> I don't know. Be ready for anything."

*Images* (production art in M11's build, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-48-the-citadel`: a CDF chart of the citadel seen from above, a ring of grown towers round a
  central spire, gold wreck outlines under the teal growth, the lifeboat's trail a dotted gold line ending at the
  spire, "LAST BEACON: VORNE, A." in the corner.
- Varga's page `level-48-reactor-core`: her scan of a gold reactor core in grey outline, teal veins drawn out of it
  toward an arrow marked "SPIRE"; under it the citadel's plan with the decks' cores as gold dots.

*Hangar teaser* (shop screen before L48; Rook speaks, not voiced):

> **Rook:** "Vorne. All the way out here. Some people just can't retire."

The plan buys the **Swarm Tail L5** (the level's `rear`) and fits Rook's owned **Mortar** for the citadel's ground
([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)): 25 of 27 MW. The intel panel's
`SPECIAL LIMITED` row stays (`NO COVER`).

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `air` (Chimeras, Harrows, Wraiths, the swarms; the mid-boss) and `ground` (the grown decks: Graft Turrets, Spine Turrets, Polyp Mortars and the gold reactor cores) |
| Attack directions | front 75 % · rear 24 % (Chimera wings, Harrow streams and Wraiths from the bottom edge) · every edge 1 % (the Harrow cross); the mid-boss's rear sweep |
| Density | 5 |
| Recommended traits | `rear`, `homing` |
| Hazards | The rear waves; the Polyp Mortars' marked impacts; the Harrows' burst rings; the mid-boss's lance, lobs and rear sweep (its document) |
| Boss / mid-boss | mid-boss [Vorne's Chimera](../../../enemies/bosses/vornes-chimera/README.md) (Q5 a) |
| Sensor-suite detail | **none**: the Vrell-grown citadel, `air` and `ground`, front and rear · **L1**: + density 5, the hazard "rear waves", the objective `STOP VORNE`, the secondary `CORES 0 / 8`, the mid-boss row, `SPECIAL LIMITED` (`NO COVER`) · **L2**: + Chimera, Harrow, Wraith, Graft Turret, Spine Turret, Polyp Mortar, Needler, Stinger, Skitter, Mote and Choir Seraph portraits; the mid-boss's silhouette (named at the sting) · **L3**: + `rear` and `homing` highlighted, the wave strip marking the rear waves and the arena |

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

A level object of this level (the citadel kit; the build adds it to the enemies' data, as Level 39's pump station),
not a roster unit, not counted as an enemy.

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

| Section | t (s) | Scroll (px, approx.) | Speed (px/s) | Layers and content | Purpose |
|---|---|---|---|---|---|
| 1. The Outer Growth | 0–44 | 0–5,280 | 120 | `ground`: the first grown decks, ridged teal-black tissue over buried gold plating; `deep`: the citadel's towers rising ahead | The regulars; the first turret nests; the first rear wave (t=17) |
| 2. The Gold Decks | 44–116 | 5,280–13,200 | 110 | `ground`: decks laid over whole gold hulls, open hulls with the **reactor cores** (t≈50, 70, 92), veins running up the screen; `deep`: towers of growth and wreckage | Rear wings and Harrow streams; Graft Turret nests by the cores |
| 3. The Spire Road | 116–176 | 13,200–19,200 | 100 | `ground`: a broad grown causeway, the last cores (t≈126, 150) and Vorne's archive (the secret, t≈138); every vein converging ahead; `deep`: the spire, a tower of growth round a gold mast | The Harrow cross (t=133); the last approach wave at t=169 |
| 4. The Spire Well | 176–186 | 19,200–19,900 | 100 → 0 | `ground`: the citadel's heart, the spire well at the top centre, its tissue lid veined with gold light | The pre-arena pause (t≈177–186): the warning lines; the scroll eased to a halt over t=180–186 |
| 5. The Heart | 186–187 | 19,900 | 0 (halted) | as section 4, held still | **The sting at t=186**, the mid-boss (≈ 67 s at medium, paper); the clock halted while it lives |
| 6. Through the Spire | 187–248 | 19,900–28,300 | 0 → 140 | `ground`: the spire's inner wall, rings of tissue round the dead gold mast, sliding by; `deep`: a pale shimmer at the top (Level 49's memory) by t≈235 | The calm to t=191, then the Choir's answer: rear wings, a Seraph pair, Wraiths; the last wave at t=235; the level ends at t=248 in the spire's throat |

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
`bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

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

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | Over the citadel's outer growth |
| 6 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 9 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 12 | 1 | turret nest | [Graft Turret](../../../enemies/ground/graft-turret/README.md) | 4 | front | On the first grown deck |
| 14 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 17 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | rear | **The first rear wave**: up from the bottom edge, BOTTOM warning 3 s (Rook's call) |
| 20 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 23 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 26 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 29 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | rear | From the bottom edge, alternating its halves 0.6 s apart |
| 32 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 35 | 1 | turret nest | [Spine Turret](../../../enemies/ground/spine-turret/README.md) | 4 | front |  |
| 37 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 40 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 43 | 1 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear |  |
| 46 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Over the gold decks; the first reactor cores (t≈50) |
| 49 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 52 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 55 | 2 | turret nest | [Graft Turret](../../../enemies/ground/graft-turret/README.md) | 4 | front | Beside the t≈50 cores, never on them |
| 58 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | rear |  |
| 61 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 64 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 67 | 2 | turret nest | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) | 3 | front |  |
| 70 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 73 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear |  |
| 76 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 79 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front |  |
| 82 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | Drops the Shield cell |
| 85 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 88 | 2 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front | The Chimera swarm; at most two Chimera groups on the screen |
| 91 | 2 | turret nest | [Graft Turret](../../../enemies/ground/graft-turret/README.md) | 5 | front |  |
| 94 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 97 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | rear |  |
| 100 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 103 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 106 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 109 | 2 | turret nest | [Spine Turret](../../../enemies/ground/spine-turret/README.md) | 4 | front |  |
| 112 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear |  |
| 115 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 118 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The spire road |
| 121 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 7 | front |  |
| 124 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 4 | rear |  |
| 127 | 3 | turret nest | [Graft Turret](../../../enemies/ground/graft-turret/README.md) | 5 | front |  |
| 130 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 133 | 3 | cross | [Harrow](../../../enemies/air/harrow/README.md) | 8 | every edge | 2 per edge, warned on every edge |
| 136 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 139 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front |  |
| 142 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | rear |  |
| 145 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 148 | 3 | turret nest | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) | 3 | front |  |
| 151 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 154 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 157 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear |  |
| 160 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 163 | 3 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front | The leader drops the Overdrive |
| 166 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear |  |
| 169 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 10 | front | The last wave before the arena; gone by ≈ t=177 |
| 191 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | **The first wave after him** (the clock resumed at t=187: ≥ 4 s of calm after the death's hold) |
| 194 | 6 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | rear |  |
| 197 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 200 | 6 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear |  |
| 203 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 206 | 6 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 210 | 6 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | The Choir's elites come for the wing (L47's); gone by ≈ t=225; no other medium hull on the screen meanwhile |
| 213 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 216 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 219 | 6 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 222 | 6 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear |  |
| 226 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 229 | 6 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | After the pair has gone |
| 232 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 235 | 6 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 7 | rear | The last wave; gone by ≈ t=241, calm to the level end |

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
  carries the change to the spec. The build recomputes it with the measured scale.
- **Par**: the spec's paper ≈ 67 s (the bar to the capsule's death), the window 45–75 s; measured in M11's build.
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

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, Steadfast. The citadel grew over the wrecks Vorne brought through. His lifeboat ran here." |
| t=11 (bark, `requires: escort`) | Rook | "Movement under us. Things come up from behind in here." |
| t=19 | Varga | "The gold reactors in those wrecks are still live. The growth is drinking from them." |
| t=29 | Okafor | "Then starve it. Burn every core you pass, Aegis." |
| t=100 | Okafor | "Halfway to the spire. Keep your tail clear, Lancer." |
| Vorne's archive broken (event) | Varga | "Ascendancy encryption. His private files. Whatever he knew, it's in there." |
| t=120 | Varga | "The spire is the citadel's heart. Every vein in here runs into it." |
| t=150 (bark, `requires: escort`) | Rook | "If he's in there, he knows we're coming. Vorne always knew." |
| Secondary met (the eighth core down) | Okafor | "Cores are dark. Whatever they were feeding is starving now." |
| Secondary failed (the third core away) | Varga | "Cores got past us. The growth will feed on them." |
| t=178 | Varga | "Energy rising in the spire well. Something is coming up through it." |
| t=186 (the sting) | Vorne | the spec's line **1** |
| `boss-arrival` | Vorne | the spec's line **2** |
| `boss-arrival` + 8 s | Okafor | "That's Vorne in there, Aegis. What's left of him. End it." |
| The spec's triggers for lines **3–8** (the first part down, the fusion's pain, the first sac, the capsule opening, the capsule below half) | Vorne, the Choir (line 4), Vorne shouting (line 5, `shout: true`) | the spec's lines **3–8** |
| Each rear sweep's tell (event, bark, `requires: escort`; alternating) | Rook | "He's dropping! Watch the bottom edge!" / "Under you, Lancer! Coming up the far side!" |
| K (the capsule's death) | Vorne | the spec's line **9**, cut by the burst |
| t=189 | Rook | "He was trying to tell us something. Right to the end." |
| t=195 | Varga | "At the end he was afraid, Rook. Afraid like they are." |
| t=202 | Okafor | "Vorne's war is over. Ours isn't. Out through the spire, Aegis." |
| t=212 (enemy channel) | the Choir | "*…he sang with us… now he is quiet…*" |
| t=236 | Varga | "Past the spire my readings make no sense. Like flying into a memory." |

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

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 130 × 5 + Mote Swarm 120 × 2 + Chimera 71 × 14 + Harrow 37 × 12 + Needler 30 × 12 + Wraith 16 × 30 + Stinger 14 × 15 + Choir Seraph 2 × 35 (3,448) | 18,511 | 11,107 |
| Launched by the mid-boss (paper): Dart Drone 18 × 2 + Chimera 8 × 14 (148) | 795 | 477 |
| Ground targets: Graft Turret 18 × 15 + Spine Turret 8 × 12 + Polyp Mortar 6 × 15 (456) | 2,448 | 1,958 |
| Ground targets: 10 reactor cores × 10 | 537 | 429 |
| Mid-boss: Vorne's Chimera (phase 1 188 + phase 2 117 + the capsule 165) | 2,523 | 2,523 |
| Secret: Vorne's archive (hidden crate, not scaled) | 1,678 | 839 |
| Secondary: the reactor cores | 537 | 268 |
| **Total** (bounty scale 0.32) | **27,029** | **17,601** |
| Budget(n) = the typical haul's target; typical +4.6 %, perfect 1.61 × budget | | 16,832 |

**The secondary** is scaled in this table (537) where Levels 43, 46, 47, 49 and 50 pay theirs unscaled (1,678, the
built rule pays only bounties × the scale): unscaled it lifts the typical haul to ≈ 18,170 (+8 %) and the scale to
≈ 0.29 (the Chimera's bounty 519 by its formula); part F's `TypicalHaulTest` settles it (step m11-d-act's note).
**Why 0.32, not the boss step's 0.40**: at 0.40 this level's 452 enemies and 32 turrets put the typical haul at
≈ 20,700 (+23 %); the act's scales run 0.27–0.37 (Levels 43, 44, 47), and the spec's own rule recomputes its bounty
from the level's scale, so the mid-boss still pays 15 %. **Balance plan**: the L48 visit buys the Swarm Tail L5
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

No art yet. Round 84's picks (decided 2026-11-05): Vorne's Chimera per phase in its document and Q10 b's one-palette sheet (the light of this level) in
[Vrell space](../../../world/vrell-space/README.md) (ART-R-B). Production backdrop (the outer growth, the gold decks
with their open hulls, the causeway, the spire well and its lid, the spire's inner wall), the reactor core and its
veins, Vorne's archive and the briefing images come in M11's build (part F, with the mid-boss).

## Implementation

- [ ] Level data `data.yaml` (M11's build, part F): sections and speeds with 2 s eases and the halt, waves, radio,
      budget, threat profile, briefing, backdrop, music; `medium: vacuum`, the `beyond-gate` limits (`NO COVER`)
- [ ] The reactor cores: 10 level-owned `ground` objects in five pairs, paying, not counted as enemies; their veins'
      pulse toward the spire and going dark; the secondary (`escapes: reactor-core`, `allowed: 2`, `CORES n / 8`
      counting to its target, the well's amber blink while a live core is in the bottom 120 px; easy 6 of 10, hard 10)
- [ ] The arena: the halt over t=180–186, the sting, the bar, the checkpoint (none on hard), the clock halted at
      t=187 while the Chimera lives, `hold_after_boss: 8`, the first wave after at t=191
- [ ] Wave script: ≥ 80 enemies a minute at medium (`DensityTest`), the Act 2+ floor (`PacingTest`, `ActFloorTest`)
      with its three named empty stretches, ≥ 4 s calm after the last enemy, ≥ 60 % front, the rear waves' rules, no
      two `medium` hulls overlapping
- [ ] Radio voiced with the step-5 noise and Vorne's fused voice (the shout's exception), every timed line at most 1 s
      late (`RadioTimelineLevel48Test`, with Rook and without); Vorne's lines on the spec's triggers; the line cut by
      the burst
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`);
      `TraitCoverageTest` covering Level 48
- [ ] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(48) = 16,832 with the level's `bounty_scale`, the mid-boss at ≈ 15 %
      (`TypicalHaulTest`, `BalanceTest`)
- [ ] Music and ambience: track 16 by section, the sting, track 34 cut at K, the `vrell-hive` bed, the level's
      sounds (`Level48SoundsTest`)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level and the fight with Rook on every
      difficulty and with `--escort none`
- [ ] Production art and captures for the build's round (the fight as one real-time recording, round 81's rule)

## Open questions

- **The traits**: drafted `rear` and `homing` (the plan's own fit, the lesson); the sketch's `spread` is the
  alternative (`rear`, `spread`: the Fan Blaster alone or the Plasma Arc or Scatter Vulcan with a rear gun covers it,
  but the plan's beam does not, so the hangar would warn the typical player as at Level 44). The
  [hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)'s L48 row still says `spread` (D-act's
  cross-doc fix).
- **The bounty scale**: drafted 0.32 (the spec's bounty 470 by its own formula); the alternative keeps the boss
  step's 0.40 (376) and thins the waves to ≈ 85 a minute with fewer Chimeras and Wraiths.
- **The cores and the fight**: drafted without a link (the secondary is told by the veins); the alternative lets each
  core left alive add regeneration to the phase-2 sacs (+0.5 % a second a core), a reward for the secondary that
  touches the boss's tuning.
- **Vorne before the sting**: drafted silent until the spire well (his first line is the spec's line 1 at the sting);
  the alternative is one taunt on the spire road.
- **Length**: 248 s of clock plus the fight (≈ 5.5 min in all); the build's `levelReport` may trim the post-fight
  section if the act runs long.

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
