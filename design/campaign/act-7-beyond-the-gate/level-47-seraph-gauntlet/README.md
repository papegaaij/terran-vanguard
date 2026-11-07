---
title: Level 47 – Seraph Gauntlet
design: approved
implementation: in-progress
art: chosen
depends-on: [../../../enemies/air/choir-seraph, ../../../enemies/air/choir-herald, ../../../enemies/air/wraith, ../../../enemies/air/mantis, ../../../enemies/air/lamprey, ../../../enemies/README.md, ../../../world/vrell-space, ../../../player/generator, ../../../player/wingmen, ../../../player/specials, ../../../ui/hud]
updated: 2026-11-07
---

# Level 47 – Seraph Gauntlet

## Summary

Past the dead fleet the wing flies into **the Choir's inner sphere**: the curved inside of a shell of Vrell stone, lit
gold-white by the core ahead. Here the Choir keeps its elites, the [Choir Seraph](../../../enemies/air/choir-seraph/README.md)s:
they jump to a told point and then **mirror** the ship across the field's centre while they fire a spiral, in pairs,
three times under a [Choir Herald](../../../enemies/air/choir-herald/README.md)'s ring. Around them fly the Choir's
regulars from every edge: Wraiths from behind, Mantises from the sides, Lampreys, Harrows and the swarms. The
secondary is to let **at most three Seraphs get away**. The act's hardest regular waves; air only, all directions.
266 s, ≈ 106 enemies a minute at medium.

**Status: approved** (Act 7 design phase, step `m11-d-levels-3`, 2026-11-04; the user approved it on 2026-11-05, round 84).
Written from the act roster, the gap analysis's recommended answers (**Q3 a**; J3, J9, J11, J12) and the earlier
steps' drafts, which are the source of truth for their numbers: the
[Choir Seraph](../../../enemies/air/choir-seraph/README.md) and the shared
[`teleport` and `mirror`](../../../enemies/README.md#movement-pattern-vocabulary) rules,
[Vrell space](../../../world/vrell-space/README.md)'s inner sphere and its mood, the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft) (the Mk VI "Choir Core" in the shop from
this level's visit), [Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft), the
[Act 7 specials](../../../player/specials/README.md#act-7-specials-draft) (`NO COVER`, the Swarm Call) and the HUD's
[Act 7 additions](../../../ui/hud/README.md#act-7-additions-draft). There is **no `data.yaml`**: the build turns the
hand tables into one; no number is measured.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft):

<!-- data: briefing -->
> **Commander Okafor:** "Dr. Varga's core is in the reactor bay, and it's waking up. Good:
> you'll need the power. Past the dead fleet lies the Choir's inner shell. It keeps its best in
> there. Get through it, Lancer. Vorne's trail runs straight across it."
>
> **Dr. Varga:** "The Choir's elites: we call them Seraphs. They fold away, land elsewhere and
> copy you from the far side. Cross the middle to hit one; missiles find them anywhere. And
> listen to the Choir in there. It has stopped singing at us."
<!-- /data -->

*Images* (production art in M11's build, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-47-the-shell`: a CDF chart of the inner sphere as a cut-away ring of stone round a gold-white
  core, the dead fleet a scatter of grey marks outside it, the wing's lane a dotted line through the shell's wall,
  the *Steadfast* group's marker behind, "CORE 0.02 AU".
- Varga's page `level-47-choir-seraph`: her scan of a Seraph in grey outline, the field's centre line dashed, the ship
  on one side and the Seraph's mirrored position on the other joined by an arrow; a warp-in ring and "≥ 140 px".

*Hangar teaser* (shop screen before L47; Rook speaks, not voiced):

<!-- data: teaser -->
> **Rook:** "Varga's new core purrs. I don't trust anything that purrs."
<!-- /data -->

The hangar before L47 adds the **Generator Mk VI "Choir Core"** to the shop (the core salvaged in Level 46, the
[generator](../../../player/generator/README.md#act-7-hand-plan-draft)); the plan buys it and flies 24.5 of 27 MW,
room for one 2-MW utility. The intel panel's `SPECIAL LIMITED` row stays (`NO COVER`); the plan's Swarm Call is
unaffected. `NO GROUND` warns a fitted Bomb Rack.

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` only (the Seraphs, Heralds, Wraiths, Mantises, Lampreys and the swarms); no ground layer |
| Attack directions | front 93% · sides 5% · rear 2% by the waves' data |
| Density | 5 |
| Recommended traits | `beam`, `homing` |
| Hazards | The Seraphs' spirals while they mirror; the Mantises' sweeps; the Lampreys' latch; the Harrows' burst rings |
| Boss / mid-boss | none |
| Special limits | `NO COVER` (the Airstrike, the Orbital Lance; J12); `NO GROUND` for the Bomb Rack and every bomb delivery |
| Sensor-suite detail | **none**: the Choir's inner sphere, `air`, all directions · **L1**: + density 5, the hazards "teleports, mirrors, spirals", the objective `CROSS THE SPHERE`, the secondary `SERAPHS 0 / 14`, the rows `NO GROUND` and `SPECIAL LIMITED` (`NO COVER`) · **L2**: + Choir Seraph (new), Choir Herald, Wraith, Mantis, Lamprey, Chimera, Harrow, Needler, Stinger, Skitter and Mote portraits · **L3**: + `beam` and `homing` highlighted, the wave strip marking the nine Seraph arrivals |
<!-- /data -->

*Varga's intel line* per sensor-suite level (must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "The Choir's inner shell, Lancer. Its elites live here." |
| L1 | "Attacks from every side. No ground. Seraphs jump and mirror." |
| L2 | "New: Choir Seraphs. Heralds, Wraiths, Mantises, Lampreys." |
| L3 | "Cross the middle to hit a Seraph. Missiles find them." |

**Trait coverage** (`TraitCoverageTest`, the lesson): `beam` and `homing` are both covered by the plan's own fit, the
Ion Beam L5 on the front and the Micro-missile Pod L5 on the right wing (and the Swarm Tail's homing behind), within
the Mk VI's 27 MW. The beam held on the centre line burns a Seraph lined up there and the Herald above it; the
homing missiles pick a Seraph's new arrival wherever it lands (the teleport rule).

## Objective

- **Primary** `reach-end`: cross the inner sphere (`CROSS THE SPHERE`).
- **Secondary** `escapes` (the Seraphs): **17** Seraphs fly the level (one alone, then eight pairs, t=40 to 238).
  Each flies its four cycles ([its stat block](../../../enemies/air/choir-seraph/README.md#stat-block)) and then jumps
  out for good, back to the Heart. The secondary is met when **14** are destroyed (`escapes: choir-seraph`,
  `allowed: 3`) and fails at once at the fourth escape. The HUD counts to its target (the Act 5 lesson):
  `SERAPHS n / 14`, n the Seraphs destroyed, `DONE` in green at 14, `FAILED` in red at the fourth escape.
  **Readable on screen** (the round 80 lesson): a Seraph's **last** fold is its **leaving tell**: 0.8 s instead of
  0.3 s, its wings flaring white as it rises 40 px, a rising glass chime, and the tracker's well blinks amber while
  one shows it (proposed for its document, below); the player can still finish it in the tell. Decided by ≈ t=253 at
  the latest (the last pair's hold). Pays **100** credits in Act 1 terms (**1,678** in Act 7, the act factor
  16.777 = 1.6⁶); the typical haul counts 50 %. Easy: 11 of 15 (`allowed: 4`); hard: 16 of 18 (`allowed: 2`)
  (*Difficulty notes*).
- **No cover**, **no ground** ([media](../../../world/vrell-space/README.md#media-per-level)): the Airstrike and the
  Orbital Lance read `NO COVER`, the Bomb Rack `NO GROUND`; the Swarm Call works (its drones hunt the nearest
  enemies, a Seraph's arrival included).

## Layout

Scroll **130 px/s** at the shell's edge, **120 px/s** among the ribs, **110 px/s** in the Choir hall (the pairs and
their Herald stay readable), **140 px/s** into the core's light; each change eased over 2 s. The setting is
[Vrell space](../../../world/vrell-space/README.md#sub-locations-per-level)'s inner sphere in Q10 b's **L47 light**
([moods](../../../world/vrell-space/README.md#light-per-level-q10-b)): the shell's Vrell-stone ribs in the palette lit gold-white, the act's richest
light, warm from the core ahead; the shell kept a step darker than the Seraphs, their warp-in rings violet
over the stone, never gold. Medium: **vacuum** (short flashes and debris rings, no smoke). **No ground layer**
(`NO GROUND`): the shell is `deep` scenery far below. Motion budget: the Seraphs' rings, wings and spirals are the
strong elements; the shell's ribs pass slowly and the Heralds' light rings on `low-air` drift without pulsing in a
bullet's colour.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Shell's Edge | 0–48 | 0–6,240 | 130 | clear | `deep`: the dead fleet's last hulls falling behind, then the stone shell's wall curving away below. `low-air`: drifting motes of light. | The regulars from the front, the first Wraiths and Lampreys; the Seraph's intro alone at t=40 (quiet window t=37–45). |
| 2. The Ribs | 48–120 | 6,240–14,890 | 120 | clear | `deep`: the shell's great ribs crossing far below, lit gold-white along their edges. `low-air`: thin light rings. | Three Seraph pairs; a Mantis, Lampreys, a Harrow stream, Wraiths behind. |
| 3. The Choir Hall | 120–200 | 14,890–23,700 | 110 | clear | `deep`: the ribs meeting in a vault, the core a gold-white glow at the top edge. `low-air`: the Heralds' rings of light (scenery). | Three pairs, each under a Herald; the Harrow cross (t=142); the Choir speaks (t=144); the memory pearl (the secret, t≈170). |
| 4. The Core Light | 200–266 | 23,700–32,910 | 140 | clear | `deep`: the core's light filling the top third; at its far side, by t≈255, a dark mass grown over gold (Level 48's citadel). | The last two pairs; the last wave at t=250; ≥ 4 s of calm; the level ends at t=266 as the light fills the screen's top half. |
<!-- /data -->

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

- **HP** from the proposed Act 7 reference DPS (J11: **188** at L47,
  [Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The Choir Seraph is new here
  (**90**, [its stat block](../../../enemies/air/choir-seraph/README.md#stat-block)). The **act HP factor** for the
  returning `medium` units ([balancing basis](../../../enemies/README.md#balancing-basis)): Choir Herald ×188/84
  (60 → **134**), Mantis ×188/52 (26 → **94**), Wraith ×188/66 (16 → **46**). The `small` units fly the beam era's
  ×2 as built (Needler 8, Stinger 12, Lamprey 6, Chimera 40, Harrow 16); the Skitters and Motes are `tiny`. No hex
  shields (J4); the Herald's aura ring is its own rule.
- **Density and pacing** (the Act 2+ floor from the start): **468 enemies** over 266 s, **≈ 106 a minute** at medium
  (the act's minimum 80; Act 6's band 100–118). A wave every 3 s from t=3 to the last at t=250, gaps of 4–7 s only
  before the Seraphs' arrivals. Waves by direction: front 66 of 81 (81 %), sides 9, rear 5, every edge 1: the ≥ 60 %
  front rule holds with the level `all` directions. The Seraphs' spirals, the Needlers' thorns, the Stingers' fans,
  the Mantises' sweeps and the Harrows' rings make the floor: expected to meet ≤ 3 pauses over 2 s, ≥ 2.5 enemies on
  the screen and ≥ 10 shots a minute (`PacingTest`, `ActFloorTest`), **to be confirmed with `levelReport`** in the
  build; **hard's** pacing is measured too (the gap analysis: the act's hardest regular waves, not raised past the
  floor rules). **Measured** (step E2, `levelReport`, the plan's fit with Rook; accepted by the user in round 92, C.1 and C.2):
  the approved 81 waves alone gave **1.2 on the screen** at medium and 22 pauses over 2 s (the plan's Ion Beam L5 and
  Tesla Coil Pod L5 erase the popcorn), so step E2 added **69 density waves** on the offbeats (the final-act rule:
  Stinger wings and columns, Needler columns and wings, Chimera wings, Harrow streams, Rift Skater streams, two Skitter
  streams), every one inside the Seraph rules below: **779 enemies at medium** (≈ 179 a minute; easy 602, hard 963),
  **3.0 on the screen and 48 shots a minute** (easy 1.7 and 26, hard 4.6 and 100), the only pauses over 2 s the quiet
  window round the first Seraph (t≈35–46) and the end's calm; the `small` units' time on the screen 0.66× Level 10's
  (pending, as Levels 45–46). The hard Seraphs' longer holds cover almost every second from t=40, so the density
  waves are all small units off the centre line.
- **The Seraphs** (its document's *Difficulty and level use*): one alone first (its first ring 1.0 s), then pairs,
  then pairs under a Herald; **at most one pair on the screen** (each pair's hold, four cycles ≈ 15 s, ends before
  the next arrives: pairs 24–26 s apart); a pair's arrival never on the same 0.5 s as an edge warning on the ship's
  side. While a pair holds, the popcorn is thin (snakes along the side edges, swarms kept off the centre line) so the
  middle the player must cross stays flyable.
- **The Heralds** (t=134, 160, 186): each hovers at the top centre (its wave's station `at: [240, 70]`, its sine
  drift off) for its pair's hold (`hold` 14.5 s, easy 9.5, hard 20.5), then leaves upward as its pair's leaving tells
  begin; the pair's **upper** row sits **80 px** under the Herald's centre (its wave's `rows: [150, 245]`; the hulls'
  sprites 6 px apart), so its aura (90 px, measured centre to centre) rings that Seraph **only while it crosses under
  the Herald** (within ±41 px of the centre), at the centre where the guns line it up; easy's 75 px aura never reaches
  it, hard's 105 px rings it within ±68 px. Kill the singer first: the beam from below meets the Seraph first, the
  homing missiles and the Swarm Call reach the Herald.
- **Spacing** (the Act 5 lesson): no two `medium` hulls overlap. The Mantises (t=78, 128, 178) and the Wraith
  ambushes (t=104, 152, 206, 230) enter **only after a pair has gone** and leave before the next arrives; the
  Wraiths' first ambush (t=15) comes before any Seraph. At most two Chimera groups on the screen
  ([the Chimera](../../../enemies/air/chimera/README.md)'s rule), at most 6 Harrows (the cross is told on every edge).
- **Calm at the end** (the Act 5 lesson): the last wave (a Skitter snake) enters at t=250 and is gone by ≈ t=257; the
  last pair is gone by ≈ t=253; **≥ 9 s** of calm before the level end.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | Into the inner sphere: the stone shell far below, the core's light ahead |
| 4.5 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 6 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 7.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 9 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 10.5 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 12 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 13.5 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step E2: density, the final-act rule (varied mixed waves) |
| 15 | 1 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | The first rear ambush (the Wraith's own flight, no edge warning) |
| 16.5 | 1 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 18 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 19.5 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step E2: density, the final-act rule (varied mixed waves) |
| 21 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 22.5 | 1 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 24 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 25.5 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 27 | 1 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | sides (alternating edges) | From alternating side edges, edge-warned 3 s |
| 28.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 30 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 31.5 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 33 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | The last wave before the quiet window |
| 40 | 1 | single | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 1 | front | **The Choir Seraph's first appearance**: alone, its first ring 1.0 s, quiet window t=37–45 (Varga's line ahead of it); 4 cycles, gone by ≈ t=55; drops the Shield cell |
| 40 | 1 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | Hard: the first Seraph flies as a pair |
| 46 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | Along the side edges, clear of the Seraph's row |
| 47.5 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step E2: density, the final-act rule (varied mixed waves) |
| 49 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 50.5 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 52 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front (right) | |
| 53.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step E2: density, the final-act rule (varied mixed waves) |
| 55 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 56.5 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 58 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front (left) | |
| 60 | 2 | column | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 62 | 2 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | The first pair: rows ≥ 90 px apart, the spirals interleaved; gone by ≈ t=77 |
| 62 | 2 | single | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 1 | front | Easy: a single Seraph before pairs |
| 63.5 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 65 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 66.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step E2: density, the final-act rule (varied mixed waves) |
| 68 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | sides (alternating edges) | |
| 69.5 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 71 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 72.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step E2: density, the final-act rule (varied mixed waves) |
| 74 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (left) | |
| 76 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 78 | 2 | single | [Mantis](../../../enemies/air/mantis/README.md) | 1 | left side | From the left edge, edge-warned, after the pair has gone; gone by ≈ t=87 |
| 79 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | left side | Hard: the Mantis's slot as a Harrow stream from its edge, 1 s later: its warning clear of the hard pair's landing |
| 81 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 82.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 84 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 86 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 88 | 2 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | Gone by ≈ t=103 |
| 88 | 2 | single | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 1 | front | Easy: a single Seraph before pairs |
| 89.5 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 91 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front (right) | |
| 92.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 94 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 96.5 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 97 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front (alternating edges) | From the top edge, alternating its halves 0.6 s apart |
| 98.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 100 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 102 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 104 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | After the pair has gone |
| 105 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear | Hard: the Wraiths' slot as a Harrow stream from the bottom edge (t=105: its warning clear of the hard t=88 pair's landing at t=101) |
| 106 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 107.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 109 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 110.5 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 112 | 2 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | Gone by ≈ t=127 |
| 113.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step E2: density, the final-act rule (varied mixed waves) |
| 115 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | sides (alternating edges) | |
| 116.5 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 118 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 119.5 | 2 | column | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 121 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 122.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 124 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front (left) | |
| 126 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 128 | 3 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | One from each side edge, edge-warned, after the pair; gone by ≈ t=134, before the Herald |
| 129 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | sides (alternating edges) | Hard: the Mantises' slot as a Harrow stream from the side edges, 1 s later: its warning clear of the hard pair's landing |
| 130 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 132 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 134 | 3 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | Hovers at its station (at [240, 70], its sine drift off) for its hold (14.5 s, easy 9.5, hard 20.5), then leaves upward as its pair's leaving tells begin; its aura (90 px) rings the t=136 pair's upper Seraph (80 px under it) only while it crosses under it; drops the Overdrive |
| 135.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 136 | 3 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | **The first pair with a Herald**: the upper row 80 px under the Herald's centre (step E2: not 90, the aura's centre-to-centre reach), the lower 95 px under it; gone by ≈ t=151 |
| 137.5 | 3 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 139 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 140.5 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 142 | 3 | cross | [Harrow](../../../enemies/air/harrow/README.md) | 8 | all edges | 2 per edge, warned on every edge |
| 143.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 145 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 146.5 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 148 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (left) | |
| 150 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 152 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 4 | rear | |
| 153 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear | Hard: the Wraiths' slot as a Harrow stream from the bottom edge, 1 s later: its warning clear of the hard pair's landing |
| 155 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 156.5 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 158 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 160 | 3 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | As at t=134 (its station and hold) |
| 162 | 3 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | Gone by ≈ t=177 |
| 163.5 | 3 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 165 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | sides (alternating edges) | |
| 166.5 | 3 | column | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 168 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 169.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 171 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front (right) | |
| 172.5 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 174 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 176 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 178 | 3 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | After the pair; at the side edges, clear of the t=186 Herald at the top centre; gone by ≈ t=187 |
| 179 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | sides (alternating edges) | Hard: the Mantises' slot as a Harrow stream from the side edges, 1 s later: its warning clear of the hard pair's landing |
| 180 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 181.5 | 3 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 183 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 184.5 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 186 | 3 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | As at t=134 (its station and hold); drops the Armour patch |
| 188 | 3 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | Gone by ≈ t=203 |
| 189.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 191 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 192.5 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 194 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 195.5 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step E2: density, the final-act rule (varied mixed waves) |
| 197 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | sides (alternating edges) | |
| 198.5 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 200 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 10 | front (left) | Into the core's light |
| 201.5 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 203 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 204.5 | 4 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 206 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 4 | rear | |
| 206 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear | Hard: the Wraiths' slot as a Harrow stream from the bottom edge, 1 s later: its warning clear of the hard pair's landing |
| 207.5 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 209 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 210.5 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 212 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 214 | 4 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | Gone by ≈ t=229 |
| 215.5 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 217 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 218.5 | 4 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 220 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | front (alternating edges) | |
| 221.5 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 223 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 224.5 | 4 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 226 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 7 | front (right) | |
| 228 | 4 | column | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 230 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | After the pair; gone by ≈ t=238, before the last pair |
| 231 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear | Hard: the Wraiths' slot as a Harrow stream from the bottom edge, 1 s later: its warning clear of the hard pair's landing |
| 232 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 233.5 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 235 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | sides (alternating edges) | |
| 236.5 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 238 | 4 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | The last pair; the secondary is decided when the second is gone (≈ t=253 at the latest) |
| 239.5 | 4 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 241 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 242.5 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step E2: density, the final-act rule (varied mixed waves) |
| 244 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 245 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 246.5 | 4 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 4 | front (right) | Step E2: density, the final-act rule (varied mixed waves) |
| 247 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front (left) | |
| 248.5 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front (left) | Step E2: density, the final-act rule (varied mixed waves) |
| 250 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 10 | front (right) | The last wave; gone by ≈ t=257, calm to the level end |

Totals: Skitter 168 · Chimera 99 · Mote Swarm 144 · Stinger 118 · Needler 82 · Harrow 55 · Wraith 17 · Rift Skater 44 · Lamprey 27 · Choir Seraph 17 · Mantis 5 · Choir Herald 3.
<!-- /data -->

## Hazards

- **The Seraphs' spirals** (the unit's): two arms, slow, only while mirroring; a pair's arms interleave. The puzzle is
  the mirror: the forward guns line a Seraph up only at the centre, where the spiral is densest; wing weapons,
  homing shots and the Swarm Call reach it anywhere.
- **The warp-in rings** (the [`teleport`](../../../enemies/README.md#movement-pattern-vocabulary) rule): never within
  140 px of the ship nor in its bottom 80 px band; a ring the ship flies at slides away.
- **The Mantises' sweeps**, **the Lampreys' latch** (shake it off left–right) and **the Harrows' rings**: the units'
  own, as built.

## Secrets and pickups

- **The memory pearl** (t≈170, x 360, drifting down the right third in a shard of the shell's stone): a pearl of stone the shell grew
  round a remembered image, flickering faintly with the Moon as the Vrell saw it in Act 1 (the Choir's memory, which
  Level 49 flies through); a small breakable object on the play plane (20 HP, like a small rock); broken, it frees a
  hidden crate worth **100** in Act 1 terms (1,678 in Act 7). The Salvage scanner's glint marks it. Varga's event
  line.
- **Shield cell**: dropped by the t=40 Seraph.
- **Overdrive**: dropped by the t=134 Herald.
- **Armour patch**: dropped by the t=186 Herald (no supply drone reaches past the gate).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), drafted for the **1 s rule** (≈ 12 characters a second, a
1.5 s lead, 0.4 s of silence; event lines drop after 6 s); `RadioTimelineLevel47Test` checks them with Rook and
without. **The radio's noise** (J3): step **5**, the top of the act's rise (L43 → L47; it holds to L50), the subtitles
clean; the Choir's channel clean. **Speakers**: Okafor (from the *Steadfast*, behind), Varga, Rook, and the Choir twice
on the enemy channel (its fearful Act 7 voice, [the Silence's rule](../../../story/README.md): it names nothing it
describes). Rook's barks carry `requires: escort`; his t=12 and t=250 lines do not. **Vorne is silent.**

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, Steadfast. That shell is the Choir's own. Its best wait in there." |
| t=12 | Rook | "Bone and gold light. Like flying through a cathedral. An angry one." |
| t=23 | Varga | "Their elites are Seraphs. They vanish, land somewhere else, and copy you from the far side." |
| t=35 | Varga | "Cross the middle to line one up. Missiles find them wherever they land." |
| t=56 | Okafor | "Every Seraph that gets away carries word to the Heart. Let none of them home." |
| The first Seraph killed (event, bark, `requires: escort`) | Rook | "Got one mid-jump. They don't like surprises either." |
| t=67 (bark, `requires: escort`) | Rook | "Wraiths come up under you in here. Keep your tail gun warm." |
| t=120 | Varga | "Heralds ahead. They sing a shield onto whatever sits under them. Kill the singer first." |
| t=144 (enemy channel) | The Choir | "…why do you follow… we are running… we are running…" |
| t=152 | Varga | "Running. Lancer, that was not a threat. I think it is afraid." |
| t=200 | Okafor | "That light is the Choir's core. Push through it, Aegis." |
| t=230 | Varga | "Past the core I read gold hulls under the growth. Ascendancy gold. Vorne is there." |
| Secondary met (`secondary-objective`, the 14th Seraph down) | Okafor | "No Seraph is getting home. Good flying, Lancer." |
| Secondary failed (the fourth Seraph jumps out) | Varga | "Seraphs got back to the core. The Heart will know our faces now." |
| t=243 (enemy channel) | The Choir | "…the silence comes… it is behind us…" |
| t=250 | Rook | "Behind them? We're behind them. Aren't we?" |
<!-- /data -->

Timeline check (`RadioTimelineLevel47Test`, step E2, the text timeline until step VOX-EF voices the lines): no
timed line starts more than 1 s late with Rook or without, on every difficulty, with the secondary met at its target
(easy's 11th Seraph, t≈190; medium's 14th and hard's 16th in the t=214 pair, t≈216–222) or failed. Step E2 moved
Rook's opening line to t=12, Varga's two Seraph lines to t=23 and t=35, Okafor's to t=56, Rook's Wraith bark to t=67
and Varga's gold-hulls line to t=230 (after the secondary's line), and shortened Okafor's first line ("Its best wait in
there"): at the README's times the text timeline ran up to 5 s late. Varga's lines still come ahead of the first
Seraph's first landing or with it (t=41).

## Music & ambience

The Act 7 B theme (**track 17**, new, J8; the cues are D-audio's
[music](../../../audio/music/README.md#cues-per-level-act-7)): the base stem over section 1, the full mix from the
first pair (t=62), the `choir` stem over it in the Choir hall; it thins to the base under the Choir's t=243 line and
holds to the debrief. Ambience: the **`vrell-inner-sphere`** bed
([sfx](../../../audio/sfx/README.md#ambience-per-setting)): a warm, low resonance of a vast hollow space with a faint
far hum of air in pipes, never a melody. New sounds this level needs, for the act's sfx step: the Seraph's (its
document's brief, plus the leaving tell's rising chime); the memory pearl's crack (a stone or thick glass bead
cracking, a short high ring); Freesound only for real things (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(47) = 700 × 1.07⁴⁶ ≈ **15,731** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in
**Act 1 terms** (Skitter 5, Mote 2, Needler 12, Stinger 15, Chimera 14, Lamprey 12, Harrow 12, Wraith 30, Mantis 30,
Choir Herald 30, Choir Seraph 35, the crate 100, the secondary 100), × the act factor **16.777** (1.6⁶), bounties
also × the scale. A **`bounty_scale` of 0.37** (air kills 60 %, the secret and the secondary 50 %; a first estimate
for the build to confirm with `TypicalHaulTest` and `levelReport`) puts the typical haul at **15,682 (−0.3 %)**, the
perfect run at 26,345 (1.67 × budget). No ground targets, no boss.
**Measured** (step E2, `levelReport`): with the density waves the typical haul is on budget at the scale **0.17**
(15,740, +0.1 %; perfect 26,792, 1.70 × budget); the hand estimate 0.37 above was the approved 81 waves'.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 168 × 5 + Chimera 99 × 14 + Mote Swarm 144 × 2 + Stinger 118 × 15 + Needler 82 × 12 + Harrow 55 × 12 + Wraith 17 × 30 + Rift Skater 44 × 14 + Lamprey 27 × 12 + Choir Seraph 17 × 35 + Mantis 5 × 30 + Choir Herald 3 × 30 | 23,436 | 14,062 |
| Secret: memory pearl (hidden crate, 1% of budget) | 1,678 | 839 |
| Secondary: let at most three Seraphs get away | 1,678 | 839 |
| **Total** (bounty scale 0.17) | **26,792** | **15,740** |
| Budget(n) = the typical haul's target; typical +0 %, perfect 1.70 × budget | | 15,731 |
<!-- /data -->

The scale sits above Level 43's 0.27 and Level 44's 0.31: fewer, heavier units (the elites, the returning mediums)
earn more of the haul. **Balance plan**: the L47 visit buys the **Generator Mk VI "Choir Core"** (32,000), leaving
the fit as at L45 (forward 198 against the proposed reference **188**, 1.05; rear 42 from the Swarm Tail L4), 24.5 of
27 MW ([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)); Rook's Autocannon. The level
expects `beam` (a Seraph lined up at the centre, the Herald above it) and `homing` (the arrivals anywhere).

## Difficulty notes

- **Easy**: the Seraph's easy hooks (mirror 2.0 s, 3 cycles, 4 shots/s, **single Seraphs before pairs**: the t=62
  and t=88 pairs fly as single Seraphs, **15 Seraphs**, the secondary `SERAPHS n / 11`, `allowed: 4`); the Herald's
  easy aura (75 px) never reaches the upper row (80 px under it), so the easy Seraphs fly unringed and the Heralds ring only
  the regulars; the other units' easy hooks; the formation lever's smaller waves.
- **Hard**: the Seraph's hard hooks (mirror 3.0 s, **5 cycles**, 8 shots/s: a pair holds ≈ 21 s, still inside the
  24–26 s between arrivals); the t=40 Seraph flies as a pair, **18 Seraphs**, the secondary `SERAPHS n / 16`,
  `allowed: 2`. The longer holds leave no gap between pairs for another `medium` hull, so on hard the seven Mantis and
  Wraith waves between pairs (t=78, 104, 128, 152, 178, 206, 230) fly as **Harrow streams of 5** from the same edges
  (the bottom edge for the Wraiths', the side edges for the Mantises'); the t=15 Wraiths stay. The other waves by the
  formation lever, trimmed by hand so never more than one pair, one Herald and 6 Harrows are on the screen.

## Concept art

Round 92 (M11 part E, step ART-E, 2026-11-06; production art, accepted by the user in round 92 F.2–F.4): the inner-sphere kit and the
memory pearl by `tools/art/act7_sphere.py` (`assets/backdrop/level-47/`; the pearl's sprites `debris-memory-pearl*` in
`assets/` since step E2's data names the chunk; the `backdrop:` block, the sections and the core light's curve in
`concept/backdrop-proposal.yaml` for step E4). The Seraph's production art is in [its document](../../../enemies/air/choir-seraph/README.md#concept-art)
and the two briefing images in the [briefing](../../../ui/briefing/README.md#concept-art) (`briefing-images-final-r92-a`).
Pending the user's review: the shell is the palette's violet stone lit gold-white along its ribs' upper edges (Q10 b:
no pale bone), its ribs at three scales (main ribs and their grain in the deep field, the great ribs on `far`) and the
vault's rings and spokes in the deep field (a far vault piece was tried and dropped: it read as an object); the core
light is one additive disc the game scales and weights by a curve (E4); the pearl is step E2's tumbling debris chunk
(Level 46's courier's kind), its flicker and its released light additive sets for E4. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r92-a.png](concept/backdrop-final-r92-a.png) | Production backdrop: eight screens at half size with stand-ins (a Seraph pair, its shots, the ship): the dead fleet's last hulls, the shell's rim, the ribs, the Choir hall's vault, the core light growing, the citadel's dark mass at the end; the pieces (three great ribs, the citadel, the core light, the far hulls, the Heralds' rings) | chosen |
| [concept/backdrop-final-r92-a.gif](concept/backdrop-final-r92-a.gif) | The level in a time-lapse, one frame every 3 s, at half size | chosen |
| [concept/l47-props-final-r92-a.png](concept/l47-props-final-r92-a.png) | The memory pearl in its shard of shell stone (36×36, 16 tumble frames), its flicker (the remembered Moon), its break and the release of its light (sheet) | chosen |
| [concept/l47-props-final-r92-a.gif](concept/l47-props-final-r92-a.gif) | The pearl drifting down the right third, tumbling and flickering, shot apart | chosen |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (the four deep tiles cut at the sections' rows, the great ribs and far hulks on `far`, the Heralds' rings and the motes on `low-air`, the citadel, the core light and its curve), the sections, for step E4 (not data) | proposed |
| [concept/pair-kill-capture-r92-a.mp4](concept/pair-kill-capture-r92-a.mp4) | Game capture (step EF5, real time, with sound): the first pair arrives, mirrors the ship, the ship crosses the middle and kills both | chosen |
| [concept/herald-pair-capture-r92-a.mp4](concept/herald-pair-capture-r92-a.mp4) | Game capture (step EF5, no fire, silent): a pair under the t=134 Herald, its ring on the upper Seraph, four cycles and the leaving tell | chosen |
| [concept/leaving-capture-r92-a.mp4](concept/leaving-capture-r92-a.mp4) | Game capture (step EF5, the whole screen, silent): a Seraph's leaving tell and the secondary well's blink | chosen |
| [concept/core-light-capture-r92-a.mp4](concept/core-light-capture-r92-a.mp4) | Game capture (step EF5, real time, with sound): the level's end, the core's light and the citadel's dark mass | chosen |
| [concept/seraph-vs-ribs-capture-r92-a.png](concept/seraph-vs-ribs-capture-r92-a.png) | Game capture (step EF5): Seraphs and their shots over the lit ribs, three crops at 1:1 | chosen |

## Implementation

- [x] Level data `data.yaml` (M11's build): sections and speeds with 2 s eases, waves, radio, budget, threat profile,
      briefing, backdrop, music; `medium: vacuum`, the `beyond-gate` limits (`NO COVER`), no ground layer (`NO GROUND`)
      (step E2; ART-E's backdrop block as it is)
- [x] The Seraph secondary (`escapes: choir-seraph`, `allowed: 3`, `SERAPHS n / 14` counting to its target, the
      leaving tell on a Seraph's last fold and the well's amber blink while one shows it; easy 11 of 15, hard 16 of 18)
      (steps E1, E2: `Level47Test`; the autopilot met it in every run on every difficulty)
- [x] The Heralds' stations over their pairs (the upper row 80 px under the Herald's centre, the hulls' sprites 6 px
      apart), the aura ringing a Seraph only under it (steps E1, E2: `HeraldStationTest`, `Level47Test`)
- [x] Wave script: ≥ 80 enemies a minute at medium (`DensityTest`), the Act 2+ floor (`PacingTest`, `ActFloorTest`,
      hard measured too), the quiet window t=37–45, ≥ 4 s calm after the last enemy, ≥ 60 % front, no two `medium`
      hulls overlapping (one pair on the screen; the Mantises and Wraiths between pairs; hard's Harrow streams;
      `SeraphChecks`, `HullStackingTest`, no tolerance since step F1c: the hard overlap was the measure counting a
      gone Seraph, *Decisions*)
- [x] Radio voiced with the step-5 noise, every timed line at most 1 s late (`RadioTimelineLevel47Test`, with Rook and
      without); Rook's barks `requires: escort`; the Choir's lines on the clean enemy channel (the data and the text
      timeline in step E2; the voices are step VOX-EF's)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`); the
      Choir Seraph's portrait; `TraitCoverageTest` covering Level 47
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images; the Mk VI in
      the shop from this visit
- [x] Typical haul within ±5 % of budget(47) = 15,731 with the level's `bounty_scale` (`TypicalHaulTest`)
- [x] Music and ambience: track 17 by section, the `vrell-inner-sphere` bed, the level's sounds (`Level47SoundsTest`)
      (steps MUS-EF, E4; the round 92 take a as the stand-in until the bed is swapped, round 92's S.1: *Open questions*)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot (crossing the centre for a mirrored Seraph, the
      missiles on an arrival, a Herald first) flies the level with Rook on every difficulty and with `--escort none`
      (`Level47Test`, `SeraphPlay`)
- [x] Production art and captures for the build's round (steps ART-E, E4, EF5:
      [round 92](../../../concept-rounds/round-92/README.md), closed 2026-11-07)
- [ ] The bed swapped for an existing chosen bed (round 92 S.1, pending the user's pick: *Open questions*)

## Open questions

- **The bed** (round 92, S.1: the user rejected both proposals, "The first one has voices, the second machinery. Don't we already have something that is good enough?"): which **existing chosen bed** Level 47 plays under its music instead of `vrell-inner-sphere`. The main agent's recommendation: `vrell-gate-r89-a` (Act 7's open Vrell-space bed). **Pending the user's pick; not started.** Until then the game plays `vrell-inner-sphere-r92-a` as the stand-in (it moves to `concept/rejected/` of the [sfx](../../../audio/sfx/README.md) when the bed is swapped).

## Decisions

- 2026-11-04 (Act 7 design phase, step `m11-d-levels-3`; decided 2026-11-05, round 84): promoted from the
  act roster to a draft level document, hand tables only, no `data.yaml`. Air only, all directions (81 % front);
  four sections, 266 s, 468 enemies (≈ 106 a minute); 17 Choir Seraphs (one, then eight pairs, three under a
  Herald), the secondary 14 of them (`SERAPHS n / 14`) with a leaving tell proposed for the unit; the Heralds over
  the pairs' upper rows; the Mantises and Wraiths only between pairs (hard: Harrow streams instead); traits `beam`
  and `homing` (the plan's own fit); the memory pearl as the secret (Level 49's memory foreshadowed); the Choir's two fearful
  lines; the radio's noise step 5; `bounty_scale` 0.37; Vorne silent.
- 2026-11-04 (step `m11-d-act`, decided 2026-11-05, round 84): the music and bed as D-audio's cues (track 17 with its `choir`
  stem in the Choir hall, `vrell-inner-sphere`); the Seraph's leaving tell and the HUD's amber blink now in their
  documents.
- 2026-11-05, round 84 (the user's page answers; decided as drafted unless stated, the alternatives rejected). The open items were:
  - **Q3** (round 84, the Choir Seraph): drafted **a** (teleport to a told point never inside 140 px, then mirror the
    ship's x for 2–3 s firing a `spiral`, in pairs); **b** teleport only, no mirror; **c** mirror only, no teleport.
    The [Seraph's open questions](../../../enemies/air/choir-seraph/README.md#open-questions) hold it; with **b** or
    **c** this level keeps its pairs and its secondary.
- 2026-11-07 (M11 part E, step E2): the open questions decided as drafted by round 84 (D.11, the level's approval),
  moved here: **the leaving tell** is in the [Seraph's document](../../../enemies/air/choir-seraph/README.md#behaviour)
  (step m11-d-act); **the secondary's strictness** 14 of 17 (`allowed: 3`; all of them as at Level 43 rejected); **the
  Seraph's HP** 90 (≈ 180 rejected); **the length** 266 s (no section stretched: the density waves meet the floor).
- 2026-11-07 (step E2, built from the data, accepted by the user in round 92, C.1–C.3):
  - **The upper row 80 px** under a Herald's centre, not 90 (the aura measures centre to centre, so at 90 px it rang
    only at dx = 0): it rings within ±41 px of the centre, the doc's intent ("only while it crosses under").
  - **The Herald leaves as its pair's leaving tells begin** (E1's open point 1): its hold 14.5 s (easy 9.5, hard 20.5)
    instead of the pair's whole 16/11/22, so the tell's 40 px rise never lifts the upper Seraph into the Herald's hull
    or aura (`levelReport` measured the hulls overlapping 0.4 s at every Herald pair before); the leaving Seraph is never
    ringed in its tell.
  - **A jump ends the ring** (E1's open point 2; the Herald's rule "a jump out of the radius ends the ring"): a gone
    unit is in no aura, but the built 1.0 s keep lets a ring outlast the 0.7 s fade and ring and fade 0.3 s after a
    landing away from the centre. The doc's intent is the ring dropped as the unit goes: a one-line simulation change
    (a gone unit's ring collapses at once), reported to the main agent for the next simulation step.
  - **A pair's rows stay ≥ 90 px apart** (E1's open point 3): a lower row drawn deeper than the ship's row rule allows
    (140 px above the ship) is lifted, so the rows could close under 90 px (`levelReport` measured a 0.3 s Seraph
    overlap on hard). The intent (never overlapping) wants the rule to lift both rows together: a simulation change in
    `PairRows`/`Teleporting`, reported with the one above.
  - **Easy's singles and hard's t=40 pair** as `skip` twins (a wave per difficulty, the built rule), as hard's Harrow
    streams; those streams 1 s after their slots (t=79, 105, 129, 153, 179, 206, 231) so their warnings clear the hard
    pairs' landings (`SeraphChecks`' 0.5 s rule).
  - **The popcorn off the centre while a pair holds**: every front wave of `tiny`/`small` units in a hold names its
    `edge` (or flies swarm paths kept 60 px off the centre); `SeraphChecks` checks it on every difficulty.
  - **The density waves** (the user's final-act rule: Act 7 may be harder; thin levels get more waves of varied types): the approved
    81 waves gave 1.2 on the screen at medium and 22 pauses over 2 s; 69 offbeat waves were added (Stinger wings and
    columns, Needler columns and wings, Chimera wings, Harrow streams, Rift Skater streams on the side columns, two
    Skitter streams) with the Seraph rules kept: see *Waves*.
  - **Varga's briefing page shortened** to one screen (`BriefingLayoutTest`: the approved text took 5 lines of 4):
    "land elsewhere and copy you from the far side … missiles find them anywhere", the meaning kept.
  - **The memory pearl** a tumbling debris chunk on the play plane (Level 46's courier: 20 HP, `reveals` its crate),
    not a pinned hatch (the README's "on the play plane").
  - **The music**: track 17's Choir statement as its third (motif) layer over the Choir hall (`motif: {from: 120, to:
    200}`, MUS-EF's `hollow-light-motif.ogg`), the full mix from t=62 and the base from t=243 as timed stems.
- 2026-11-07 (steps G1 and F1c, the simulation follow-ups of E2's calls): a jumping Seraph's ring drops at once, and a
  pair's two rows are lifted together; the 0.3 s hard "overlap" at t=101 was the measure counting the upper Seraph while
  it was gone (no contact), so `HullStackingTest` holds Level 47 without a tolerance.
- 2026-11-07 (step EF5): the full `check` green with parts E and F; round 92 opened (the bed, track 17 split, the art,
  the level in play from a real-time capture, three calls).
- 2026-11-07 (user, [round 92](../../../concept-rounds/round-92/README.md) closed): **S.1 neither** (both `vrell-inner-sphere` proposals rejected: a reads as voices, b as machinery; the user asks to reuse an existing chosen bed: **open, pending the user's pick**, recommended `vrell-gate-r89-a`; `-r92-a` stays as the game's stand-in until then, b moved to `rejected/`); **M.1 ok** (track 17's statement in the Choir hall); **F.2, F.3, F.4 accept** (the inner-sphere kit, the memory pearl, the briefing images: art rows chosen); **L.1–L.6 ok** (the capture rows chosen, the core's light, the leaving tell and the Seraphs over the ribs included); **C.1** (a denser Level 47, 69 mixed waves), **C.2** (small enemies at two thirds of Level 10's life) and **C.3** (the Herald's ring also on the small enemies passing under it) accepted as built.
