---
title: Level 47 – Seraph Gauntlet
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/air/choir-seraph, ../../../enemies/air/choir-herald, ../../../enemies/air/wraith, ../../../enemies/air/mantis, ../../../enemies/air/lamprey, ../../../enemies/README.md, ../../../world/vrell-space, ../../../player/generator, ../../../player/wingmen, ../../../player/specials, ../../../ui/hud]
updated: 2026-11-05
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

> **Commander Okafor:** "Dr. Varga's core is in the reactor bay, and it's waking up. Good: you'll need the power.
> Past the dead fleet lies the Choir's inner shell. It keeps its best in there. Get through it, Lancer. Vorne's
> trail runs straight across it."
>
> **Dr. Varga:** "The Choir's elites: we call them Seraphs. They fold away, land somewhere else and copy your
> movement from the far side of the field. Cross the middle to hit one. Missiles find them wherever they land. And
> listen to the Choir in there. It has stopped singing at us."

*Images* (production art in M11's build, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-47-the-shell`: a CDF chart of the inner sphere as a cut-away ring of stone round a gold-white
  core, the dead fleet a scatter of grey marks outside it, the wing's lane a dotted line through the shell's wall,
  the *Steadfast* group's marker behind, "CORE 0.02 AU".
- Varga's page `level-47-choir-seraph`: her scan of a Seraph in grey outline, the field's centre line dashed, the ship
  on one side and the Seraph's mirrored position on the other joined by an arrow; a warp-in ring and "≥ 140 px".

*Hangar teaser* (shop screen before L47; Rook speaks, not voiced):

> **Rook:** "Varga's new core purrs. I don't trust anything that purrs."

The hangar before L47 adds the **Generator Mk VI "Choir Core"** to the shop (the core salvaged in Level 46, the
[generator](../../../player/generator/README.md#act-7-hand-plan-draft)); the plan buys it and flies 24.5 of 27 MW,
room for one 2-MW utility. The intel panel's `SPECIAL LIMITED` row stays (`NO COVER`); the plan's Swarm Call is
unaffected. `NO GROUND` warns a fitted Bomb Rack.

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `air` only (the Seraphs, Heralds, Wraiths, Mantises, Lampreys and the swarms); no ground layer |
| Attack directions | front 81 % · sides 11 % (Lampreys, Mantises) · rear 6 % (Wraiths) · every edge 1 % (the Harrow cross) |
| Density | 5 |
| Recommended traits | `beam`, `homing` |
| Hazards | The Seraphs' spirals while they mirror; the Mantises' sweeps; the Lampreys' latch; the Harrows' burst rings |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: the Choir's inner sphere, `air`, all directions · **L1**: + density 5, the hazards "teleports, mirrors, spirals", the objective `CROSS THE SPHERE`, the secondary `SERAPHS 0 / 14`, the rows `NO GROUND` and `SPECIAL LIMITED` (`NO COVER`) · **L2**: + Choir Seraph (new), Choir Herald, Wraith, Mantis, Lamprey, Chimera, Harrow, Needler, Stinger, Skitter and Mote portraits · **L3**: + `beam` and `homing` highlighted, the wave strip marking the nine Seraph arrivals |

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

| Section | t (s) | Scroll (px, approx.) | Speed (px/s) | Layers and content | Purpose |
|---|---|---|---|---|---|
| 1. The Shell's Edge | 0–48 | 0–6,240 | 130 | `deep`: the dead fleet's last hulls falling behind, then the stone shell's wall curving away below; `low-air`: drifting motes of light | The regulars from the front, the first Wraiths and Lampreys; the Seraph's intro alone at t=40 (quiet window t=37–45) |
| 2. The Ribs | 48–120 | 6,240–14,880 | 120 | `deep`: the shell's great ribs crossing far below, lit gold-white along their edges; `low-air`: thin light rings | Three Seraph pairs; a Mantis, Lampreys, a Harrow stream, Wraiths behind |
| 3. The Choir Hall | 120–200 | 14,880–23,680 | 110 | `deep`: the ribs meeting in a vault, the core a gold-white glow at the top edge; `low-air`: the Heralds' rings of light (scenery) | Three pairs, each under a Herald; the Harrow cross (t=142); the Choir speaks (t=144); the memory pearl (the secret, t≈170) |
| 4. The Core Light | 200–266 | 23,680–32,920 | 140 | `deep`: the core's light filling the top third; at its far side, by t≈255, a dark mass grown over gold (Level 48's citadel) | The last two pairs; the last wave at t=250; ≥ 4 s of calm; the level ends at t=266 as the light fills the screen's top half |

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
  floor rules).
- **The Seraphs** (its document's *Difficulty and level use*): one alone first (its first ring 1.0 s), then pairs,
  then pairs under a Herald; **at most one pair on the screen** (each pair's hold, four cycles ≈ 15 s, ends before
  the next arrives: pairs 24–26 s apart); a pair's arrival never on the same 0.5 s as an edge warning on the ship's
  side. While a pair holds, the popcorn is thin (snakes along the side edges, swarms kept off the centre line) so the
  middle the player must cross stays flyable.
- **The Heralds** (t=134, 160, 186): each hovers at the top centre (y 70) for its pair's hold, then leaves upward;
  the pair's **upper** row sits 90 px under the Herald's centre (the hulls ≥ 16 px apart), so its aura (90 px) rings
  that Seraph **only while it crosses under the Herald**, at the centre where the guns line it up. Kill the singer
  first: the beam from below meets the Seraph first, the homing missiles and the Swarm Call reach the Herald.
- **Spacing** (the Act 5 lesson): no two `medium` hulls overlap. The Mantises (t=78, 128, 178) and the Wraith
  ambushes (t=104, 152, 206, 230) enter **only after a pair has gone** and leave before the next arrives; the
  Wraiths' first ambush (t=15) comes before any Seraph. At most two Chimera groups on the screen
  ([the Chimera](../../../enemies/air/chimera/README.md)'s rule), at most 6 Harrows (the cross is told on every edge).
- **Calm at the end** (the Act 5 lesson): the last wave (a Skitter snake) enters at t=250 and is gone by ≈ t=257; the
  last pair is gone by ≈ t=253; **≥ 9 s** of calm before the level end.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | Into the inner sphere: the stone shell far below, the core's light ahead |
| 6 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 9 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 12 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 15 | 1 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | The first rear ambush (the Wraith's own flight, no edge warning) |
| 18 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 21 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 24 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 27 | 1 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | side | From alternating side edges, edge-warned 3 s |
| 30 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 33 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | The last wave before the quiet window |
| 40 | 1 | single | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 1 | front | **The Choir Seraph's first appearance**: alone, its first ring 1.0 s, quiet window t=37–45 (Varga's line ahead of it); 4 cycles, gone by ≈ t=55; drops the Shield cell |
| 46 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | Along the side edges, clear of the Seraph's row |
| 49 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 52 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 55 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 58 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 62 | 2 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | The first pair: rows 90 px apart, the spirals interleaved; gone by ≈ t=77 |
| 65 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 68 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | side |  |
| 71 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 74 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front |  |
| 78 | 2 | single | [Mantis](../../../enemies/air/mantis/README.md) | 1 | side | From the left edge, edge-warned, after the pair has gone; gone by ≈ t=87 |
| 81 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 84 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 88 | 2 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | Gone by ≈ t=103 |
| 91 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 94 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 97 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front | From the top edge, alternating its halves 0.6 s apart |
| 100 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 104 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | After the pair has gone |
| 106 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 109 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 112 | 2 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | Gone by ≈ t=127 |
| 115 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | side |  |
| 118 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 121 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 124 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 128 | 3 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | side | One from each side edge, edge-warned, after the pair; gone by ≈ t=134, before the Herald |
| 130 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 134 | 3 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | Hovers at the top centre (y 70); its aura (90 px) rings the t=136 pair's upper Seraph only while it crosses under it; drops the Overdrive |
| 136 | 3 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | **The first pair with a Herald**: the upper row 90 px under the Herald's centre (hulls ≥ 16 px apart); gone by ≈ t=151 |
| 139 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 142 | 3 | cross | [Harrow](../../../enemies/air/harrow/README.md) | 8 | every edge | 2 per edge, warned on every edge |
| 145 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 148 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front |  |
| 152 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 4 | rear |  |
| 155 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 158 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 160 | 3 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | As at t=134 |
| 162 | 3 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | Gone by ≈ t=177 |
| 165 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | side |  |
| 168 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 171 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 174 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 178 | 3 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | side | After the pair; at the side edges, clear of the t=186 Herald at the top centre; gone by ≈ t=187 |
| 180 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 183 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 186 | 3 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | As at t=134; drops the Armour patch |
| 188 | 3 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | Gone by ≈ t=203 |
| 191 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 194 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 197 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | side |  |
| 200 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 10 | front | Into the core's light |
| 203 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 206 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 4 | rear |  |
| 209 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 212 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 214 | 4 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | Gone by ≈ t=229 |
| 217 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 220 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | front |  |
| 223 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 226 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 7 | front |  |
| 230 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | After the pair; gone by ≈ t=238, before the last pair |
| 232 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 235 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | side |  |
| 238 | 4 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | The last pair; the secondary is decided when the second is gone (≈ t=253 at the latest) |
| 241 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 244 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 247 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front |  |
| 250 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 10 | front | The last wave; gone by ≈ t=257, calm to the level end |

Totals: Skitter 156 · Mote Swarm 144 · Chimera 31 · Needler 30 · Lamprey 27 · Stinger 19 · Harrow 19 · Wraith 17 ·
Choir Seraph 17 · Mantis 5 · Choir Herald 3 (= 468).

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
describes). Rook's barks carry `requires: escort`; his t=11 and t=250 lines do not. **Vorne is silent.**

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, Steadfast. That shell is the Choir's own. Whatever it has left, it keeps in there." |
| t=11 | Rook | "Bone and gold light. Like flying through a cathedral. An angry one." |
| t=19 | Varga | "Their elites are Seraphs. They vanish, land somewhere else, and copy you from the far side." |
| t=29 | Varga | "Cross the middle to line one up. Missiles find them wherever they land." |
| t=47 | Okafor | "Every Seraph that gets away carries word to the Heart. Let none of them home." |
| The first Seraph killed (event, bark, `requires: escort`) | Rook | "Got one mid-jump. They don't like surprises either." |
| t=58 (bark, `requires: escort`) | Rook | "Wraiths come up under you in here. Keep your tail gun warm." |
| t=120 | Varga | "Heralds ahead. They sing a shield onto whatever sits under them. Kill the singer first." |
| t=144 (enemy channel) | the Choir | "*…why do you follow… we are running… we are running…*" |
| t=152 | Varga | "Running. Lancer, that was not a threat. I think it is afraid." |
| The memory pearl broken (event) | Varga | "There's an image in it. Our Moon. The Choir remembers where it lost." |
| t=200 | Okafor | "That light is the Choir's core. Push through it, Aegis." |
| t=222 | Varga | "Past the core I read gold hulls under the growth. Ascendancy gold. Vorne is there." |
| Secondary met (`secondary-objective`, the 14th Seraph down) | Okafor | "No Seraph is getting home. Good flying, Lancer." |
| Secondary failed (the fourth Seraph jumps out) | Varga | "Seraphs got back to the core. The Heart will know our faces now." |
| t=243 (enemy channel) | the Choir | "*…the silence comes… it is behind us…*" |
| t=250 | Rook | "Behind them? We're behind them. Aren't we?" |

Timeline check (text): the timed lines close at ≈ 9.9, 18.1, 28.1, 36.4, 54.9, (64.4), 128.8, 149.8, 158.6, 206.1,
230.3, 247.5 and 255.0; nothing overlaps, and the last closes 11 s before the level end. Varga's t=19 and t=29 lines
come ahead of the first Seraph (t=40, the quiet window t=37–45). The secondary resolves between ≈ t=200 and t=253, in
the gaps after t=206.1 and t=230.3; a failure earlier waits up to 6 s for a free slot. The Choir's two lines are its
fearful voice of Act 7 (the gap analysis's §0.2: "*…the silence comes… it is behind us…*"); Rook's answer takes it
literally, Varga's reads the fear. Level 50 completes the line (its final words).

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

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 156 × 5 + Mote Swarm 144 × 2 + Chimera 31 × 14 + Needler 30 × 12 + Lamprey 27 × 12 + Stinger 19 × 15 + Harrow 19 × 12 + Wraith 17 × 30 + Choir Seraph 17 × 35 + Mantis 5 × 30 + Choir Herald 3 × 30 (4,044) | 25,103 | 15,062 |
| Secret: the memory pearl (hidden crate) | 621 | 310 |
| Secondary: the Seraphs | 621 | 310 |
| **Total** (bounty scale 0.37) | **26,345** | **15,682** |
| Budget(n) = the typical haul's target; typical −0.3 %, perfect 1.67 × budget | | 15,731 |

The scale sits above Level 43's 0.27 and Level 44's 0.31: fewer, heavier units (the elites, the returning mediums)
earn more of the haul. **Balance plan**: the L47 visit buys the **Generator Mk VI "Choir Core"** (32,000), leaving
the fit as at L45 (forward 198 against the proposed reference **188**, 1.05; rear 42 from the Swarm Tail L4), 24.5 of
27 MW ([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)); Rook's Autocannon. The level
expects `beam` (a Seraph lined up at the centre, the Herald above it) and `homing` (the arrivals anywhere).

## Difficulty notes

- **Easy**: the Seraph's easy hooks (mirror 2.0 s, 3 cycles, 4 shots/s, **single Seraphs before pairs**: the t=62
  and t=88 pairs fly as single Seraphs, **15 Seraphs**, the secondary `SERAPHS n / 11`, `allowed: 4`); the Herald's
  easy aura (75 px) never reaches the pair's rows (90 px), so the easy Seraphs fly unringed and the Heralds ring only
  the regulars; the other units' easy hooks; the formation lever's smaller waves.
- **Hard**: the Seraph's hard hooks (mirror 3.0 s, **5 cycles**, 8 shots/s: a pair holds ≈ 21 s, still inside the
  24–26 s between arrivals); the t=40 Seraph flies as a pair, **18 Seraphs**, the secondary `SERAPHS n / 16`,
  `allowed: 2`. The longer holds leave no gap between pairs for another `medium` hull, so on hard the seven Mantis and
  Wraith waves between pairs (t=78, 104, 128, 152, 178, 206, 230) fly as **Harrow streams of 5** from the same edges
  (the bottom edge for the Wraiths', the side edges for the Mantises'); the t=15 Wraiths stay. The other waves by the
  formation lever, trimmed by hand so never more than one pair, one Herald and 6 Harrows are on the screen.

## Concept art

No art yet. Round 84's picks (decided 2026-11-05): the Choir Seraph's sheets with its teleport ring in its document (ART-R-A, over
this level's brightest and darkest band); Q10 b's one-palette sheet (the light of this level) in [Vrell space](../../../world/vrell-space/README.md)
(ART-R-B). Production backdrop (the stone shell and its ribs on `deep` at three scales, the vault, the core's light,
the Heralds' scenery rings, the citadel's dark mass at the end), the memory pearl and the briefing images come in
M11's build.

## Implementation

- [ ] Level data `data.yaml` (M11's build): sections and speeds with 2 s eases, waves, radio, budget, threat profile,
      briefing, backdrop, music; `medium: vacuum`, the `beyond-gate` limits (`NO COVER`), no ground layer (`NO GROUND`)
- [ ] The Seraph secondary (`escapes: choir-seraph`, `allowed: 3`, `SERAPHS n / 14` counting to its target, the
      leaving tell on a Seraph's last fold and the well's amber blink while one shows it; easy 11 of 15, hard 16 of 18)
- [ ] The Heralds' stations over their pairs (the upper row 90 px under the Herald's centre, the hulls ≥ 16 px apart),
      the aura ringing a Seraph only under it
- [ ] Wave script: ≥ 80 enemies a minute at medium (`DensityTest`), the Act 2+ floor (`PacingTest`, `ActFloorTest`,
      hard measured too), the quiet window t=37–45, ≥ 4 s calm after the last enemy, ≥ 60 % front, no two `medium`
      hulls overlapping (one pair on the screen; the Mantises and Wraiths between pairs; hard's Harrow streams)
- [ ] Radio voiced with the step-5 noise, every timed line at most 1 s late (`RadioTimelineLevel47Test`, with Rook and
      without); Rook's barks `requires: escort`; the Choir's lines on the clean enemy channel
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`); the
      Choir Seraph's portrait; `TraitCoverageTest` covering Level 47
- [ ] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images; the Mk VI in
      the shop from this visit
- [ ] Typical haul within ±5 % of budget(47) = 15,731 with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music and ambience: track 17 by section, the `vrell-inner-sphere` bed, the level's sounds (`Level47SoundsTest`)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot (crossing the centre for a mirrored Seraph, the
      missiles on an arrival, a Herald first) flies the level with Rook on every difficulty and with `--escort none`
- [ ] Production art and captures for the build's round

## Open questions

- **The Seraph's leaving tell** (its last fold 0.8 s, the wings flaring white, a 40 px rise, a rising chime; the
  objective's readability): proposed here for the [Choir Seraph](../../../enemies/air/choir-seraph/README.md)'s
  document, which does not have it yet (D-act's cross-doc fix).
- **The secondary's strictness**: drafted 14 of 17 (`allowed: 3`): a Seraph lined up only at the centre is the
  level's puzzle, and three slips are forgiven; the alternative is all of them as at Level 43.
- **The Seraph's HP**: drafted at its document's 90; with the round's heavier alternative (≈ 180) this level keeps
  its pairs and the secondary drops to 12 of 17.
- **Length**: 266 s ("near 5 minutes" is the act's target): the build's `levelReport` may stretch a section if the
  pacing allows.

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
