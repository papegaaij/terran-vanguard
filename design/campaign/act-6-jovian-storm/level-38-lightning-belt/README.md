---
title: Level 38 – Lightning Belt
design: approved
implementation: done
art: chosen
depends-on: [../../../enemies/air/harrow, ../../../enemies/air/buzzsaw-drone, ../../../enemies/air/chimera, ../../../enemies/air/talon, ../../../enemies/air/hornet, ../../../enemies/air/wraith, ../../../enemies/air/lamprey, ../../../world/jovian, ../../../player/specials, ../../../player/wingmen, ../../../player/generator, ../../../ui/hud, ../../act-2-homefront/level-12-storm-front]
updated: 2026-11-03
---

# Level 38 – Lightning Belt

## Summary

The run through Jupiter's storm layer, under the cloud cities and round the Great Red Spot's rim: the only route to
Io that the Callisto batteries cannot track. **Lightning strikes** come down in told columns and hit everything on
`air`, the enemies too; the new [Harrow](../../../enemies/air/harrow/README.md)s ram from every edge and burst into
rings however they die; the [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md)s return. The act's
all-directions level and its only one without the Ascendancy's ground: air only (`NO GROUND`). Density 5, ≈ 115
enemies a minute at medium over 196 s.

**Status: approved 2026-10-31 (round 72, D.4)** (drafted in the Act 6 design phase, step `m10-d-levels-2`, 2026-10-31).
Written from the act roster, the gap analysis's recommended answers (Q3 a with c's backdrop, Q7 b, Q10 a), its
judgement calls (J1 reference DPS, J2 media, J3 lightning, J5 no new tiny unit) and the wave 1 documents (the
[Harrow](../../../enemies/air/harrow/README.md), the Buzzsaw's
[Act 6 return](../../../enemies/air/buzzsaw-drone/README.md#act-6-the-return-in-the-storm), the
[Jovian hazards](../../../world/jovian/README.md#hazards--set-pieces), the
[Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft)); every number is a first draft for the
M10 part of this level to measure with `levelReport`. M10 part D (step D2, 2026-11-01): the hand tables are the level's
[data.yaml](data.yaml), rendered here, and measured with `levelReport` (*Waves*, *Credit budget*).

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Rendered from the level's
[data.yaml](data.yaml) (M10 part D, step D2: the approved texts as they are, four lines a page on the screen):

<!-- data: briefing -->
> **Commander Okafor:** "Aurelia is gone, and Vorne's fleet still drinks from Io. The high road
> to Io runs under Callisto's guns; the storm layer below the cloud cities doesn't. Their
> trackers go blind in it. So do we. Fly the Lightning Belt and come out over Io."
>
> **Dr. Varga:** "The storm throws columns of lightning: a thin line first, then the strike, the
> whole way down. It hits whatever flies in it, theirs too. And Aurelia's labs bred hybrids for
> this storm. Harrows. Whatever kills one, it bursts into a ring. Kill them far away."
<!-- /data -->

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-38-lightning-belt`: the fleet's chart of the storm layer: Aurelia's wreck at the top, the
  route under the cloud decks round the Red Spot's rim (a rust spiral at the chart's edge), the climb out toward
  Io's yellow disc; lightning cells marked as hatched areas.
- Varga's page `level-38-harrow`: her sketch of a Harrow from above (the gunmetal dart, the teal sac), its burst ring
  of 8 with the gap toward the ship; beside it a lightning column's thin telegraph line and the strike's width.

*Hangar teaser* (shop screen before L38; Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "Storm layer next. Weather report: lightning, then more lightning."
<!-- /data -->

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule): about 80 % of the
enemies (76 % of the waves) enter from the top, the rest from the side edges and the bottom, every side or rear entry edge-warned 3 s
ahead (the Wraiths' rear ambush excepted: their own flight).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (Harrows, Buzzsaw Drones, Chimeras, Talons, Hornets, Dart and Skitter streams, motes, Lampreys, Stingers, Needlers); `high-air` (the Wraiths, cloaked); **no ground** |
| Attack directions | front 80% · sides 14% · rear 6% (counted by enemy: every side or rear entry edge-warned 3 s ahead, the Wraiths' rear ambush excepted: their own flight) |
| Density | 5 |
| Recommended traits | `homing`, `beam` |
| Hazards | Lightning strikes (told columns, they hit the ship and the enemies); wind gusts; burst rings (the Harrows) |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: Jupiter, storm layer, `air`, all directions, the objective `REACH IO` · **L1**: + density 5, the hazards "lightning", "gusts" and "burst rings", the secondary `HARROWS 0 / 23`, the row `NO GROUND` · **L2**: + Harrow (new), Buzzsaw Drone, Chimera, Talon, Hornet, Wraith, Lamprey, Stinger, Needler, Dart Drone, Skitter and Mote portraits · **L3**: + `homing` and `beam` highlighted, the wave strip marking the side and rear entries |
<!-- /data -->

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Under the cloud cities: the storm layer. Lightning." |
| L1 | "From every side. No ground: the bombs stay idle." |
| L2 | "New: Harrows that burst. Buzzsaws back, Chimeras." |
| L3 | "Homing for the flanks, a beam for the healers." |

## Objective

- **Primary** `reach-end`: cross the storm layer and climb out toward Io.
- **Secondary** `kill_ratio` on the Harrows (`HARROWS`, the gap analysis's 65 % rule): kill at least **23 of the 35**
  [Harrow](../../../enemies/air/harrow/README.md)s (65 %, rounded up), **judged at the level's end** (the campaign's
  [kill-ratio rule](../../README.md#pacing-rules): its credits and its line at completion). The tracker counts to
  its target, **`HARROWS n / 23`** (the Act 5 lesson that a count shows its target), and reads `MET` only at the end.
  A Harrow killed by a lightning strike counts (J3: the player lured it); one that rams the ship counts as a kill as
  its page says; one that flies out unhurt does not. Pays **100** credits in Act 1 terms (**1,049** in Act 6, the act
  factor 1.6⁵ = 10.486); the typical haul counts 50 %.
- **No ground** (`ground: none`, J2): the Bomb Rack and every other bomb delivery read `NO GROUND` and stay idle while
  drawing their power (Level 30's rule); the validator rejects ground targets and terrain here.

## Layout

Scroll **140 px/s** (the backdrop's; nothing stands on a ground layer). Setting: [the Jovian
system](../../../world/jovian/README.md#sub-locations-per-level)'s **storm layer**, Jupiter's **air** (the
[media rules](../../../world/jovian/README.md#media-per-level): weather, smoke and fire rise as on Earth, no rain).
**Light** ([mood](../../../world/jovian/README.md#moods-per-level-q10), Q10 a): storm-dark blue-grey and slate,
purple-grey cloud walls, the lightning's blue-white; the Red Spot's rim a far rust glow in section 4; the breakout
lifts to black sky with Io's yellow disc. Motion budget: the lightning and the gusts' streaks are the strong elements;
the cloud walls roll slowly on `deep` and `high-air`; no foreground banks over `air` in this level (they are Level
36's; the storm's dark is this level's read). Every haze, glow and cloud layer is smooth (≥ 32 steps, no ordered
dither, drawn once: the Act 5 lesson).

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Under Aurelia | 0–22 | 0–3,080 | 140 | medium | `deep`: the storm's top, Aurelia's wreck sinking far above and behind (lights going out). `low-air`: torn balloon fabric and deck debris falling past (scenery, no hit box). | The drop into the storm; popcorn and the first Chimeras. |
| 2. The Storm Wall | 22–62 | 3,080–8,680 | 140 | heavy | `deep`: cloud walls lit from inside. `high-air`: storm scud passing over. | **The lightning taught** (the first strike alone at t=26), **the Harrow introduced** (t=30.5) and the first lure (t=43.5). |
| 3. The Cells | 62–109 | 8,680–15,260 | 140 | heavy | `deep`: storm cells as towers of cloud (backdrop only: no hit box, the Buzzsaws do not bounce off them); gust streaks. | All directions: the Buzzsaws' return, side and rear entries, the first three-way hunt, the first gust spell. |
| 4. The Red Spot's Rim | 109–150 | 15,260–21,000 | 140 | heavy | `deep`: the Red Spot's rim as a rust glow and a vast curving wall far off; the densest lightning. | The peak: Hornets hunted by Stingers, Harrows from every edge, the second gust spell. |
| 5. The Breakout | 150–196 | 21,000–27,440 | 140 | medium, light peak 172–196 | `deep`: the cloud tops thinning, stars, Io's yellow disc rising; the last strike at t=163. | The climb out; the last waves; the calm before the end. |
<!-- /data -->

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). All numbers are **draft**: data in **Act 1
terms** with a `bounty_scale` (see *Credit budget*).

- **HP**: the units' own pages at the proposed L38 reference DPS **152** (J1): the Harrow, the Buzzsaw's Act 6 return
  and the Chimera as their drafts say (`small`: the beam era's ×2, no act factor); the Talon and the Hornet with the
  **Act 6 shields** ([enemy shields](../../../enemies/README.md#enemy-shields): its L38 row is the Hornet's); the
  Wraith with the act HP factor of a returning `medium` unit; the Skitter, the Lamprey, the Stinger, the Needler,
  the Dart and the motes as built.
- **Density and pacing** (the Act 2+ rules, `PacingTest` and `ActFloorTest`, the act's 75 a minute): **375 enemies**
  in 68 waves over 196 s, **≈ 115 a minute** at medium (≈ 121 counted to the last wave's entry; easy ≈ 310, hard ≈ 420
  by the formation lever), a wave about every 2.5 s, no stretch over 2 s with nothing on the screen. The shots come
  from the Talon and Chimera pairs, the Hornets' salvos, the Wraiths' bursts, the Stingers' fans and the Harrows'
  rings; the build sets the fire rates with `levelReport`.
- **Tiny units** (J5, the [variety checklist](../../../enemies/README.md#variety-checklist-per-act)): the Dart Drone
  streams and the Mote Swarms (35 Darts, 120 motes) carry the act's `tiny` tier here.
- **Quiet windows**: no wave enters from t=20.5 to t=28 (the first strike at t=26 plays alone, as Level 27's first
  plume) or from t=30.5 to t=35 (the first Harrows alone).
- **Limits** (the units' pages): at most **6 Harrows** on the screen and 2 in their tell or ram; at most **6 Buzzsaw
  Drones**, 2 dashing; **never two Buzzsaw dashes and two Harrow rams at once**: a Buzzsaw wave and a Harrow wave are
  at least 7 s apart; no other `death-burst` unit in the level (no Spore Bombers); at most 3 Hornets and 16 homing
  missiles; **no two `medium` hulls overlapping** (the Act 5 lesson): the Hornet pairs keep ≥ 80 px between hull edges,
  and the Wraiths of t=142 are gone before the Hornets of t=154.5 take their stations (the build checks the overlap with `levelReport`).
- **Edge warnings**: every side and rear entry is warned 3 s ahead on its edge (the cross at t=137 on all four).
- **The three-way war** ([Q3 a](../../../enemies/README.md#the-three-way-war), decided 2026-10-31 (round 72)): two hunts. The
  t=74 Lampreys latch the t=72.5 Talon line; the t=113 Stingers dive on the t=111 Hornet pair. A prey unit the Vrell
  kill pays the player nothing; the hybrids (Chimeras, Harrows) are never prey. The backdrop adds Vrell and
  Ascendancy craft fighting far off on `deep` in sections 3–4 (no hit boxes).

Rendered from the data (M10 part D, step D2; "left"/"right" is the side edge a wave enters by):

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 5.5 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 8 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 10.5 | 1 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | |
| 13 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 15.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 18 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 20.5 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | The hybrids of Aurelia's labs; the last wave before the first strike |
| 28 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | After the first strike (telegraph t=26, alone) |
| 30.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 3 | front | **The Harrow's introduction**: a short stream alone, no other wave until t=35 |
| 35 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 37.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 40 | 2 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | |
| 42.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front | The first lure: a column's telegraph at t=43.5 across their chase |
| 47 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 49.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 52 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | left side | The first side entry, edge-warned 3 s ahead |
| 54.5 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 57 | 2 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | |
| 59.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 62 | 3 | circle | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 4 | front | **The Buzzsaw's return**: a circle breaking off one by one; no Harrow until t=70 |
| 65 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | left side | Edge-warned |
| 67.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 70 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | right side | From the right edge, edge-warned; the strike at t=71.5 lures them |
| 72.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | **Prey** of the t=74 Lampreys (three-way war) |
| 74 | 3 | swarm | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | left side | **Hunters** of the t=72.5 Talons: they latch them (Level 33's rule); edge-warned |
| 77 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 79.5 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | The first rear entry (its own flight, no edge warning); cloaked on `high-air`, `homing` finds it |
| 82 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 84.5 | 3 | stream | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 4 | left side | Edge-warned; no Harrow until t=94 |
| 87 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 89.5 | 3 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | |
| 94 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | rear | From the bottom edge, edge-warned; the strike at t=95.5 lures them |
| 96.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 99 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The CDF supply canister drifts down the middle from t=100 |
| 101.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | right side | Edge-warned |
| 104 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 106.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 109 | 4 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 111 | 4 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | **Prey** of the t=113 Stingers; ≥ 80 px between the hulls; the second drops a shield cell |
| 113 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front | **Hunters** of the t=111 Hornets: they dive on them |
| 115.5 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 118 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Aurelia's cargo pod (the secret) tumbles down the left edge from t≈118 |
| 121 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | sides (alternating edges) | Alternating side edges, both edge-warned; the strike at t=122.5 lures them |
| 123.5 | 4 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | right side | Edge-warned |
| 126 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Its last Chimera drops an overdrive |
| 128.5 | 4 | circle | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 5 | front | No Harrow until t=137 |
| 131 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 133.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 137 | 4 | cross | [Harrow](../../../enemies/air/harrow/README.md) | 4 | all edges | One from each edge, every edge warned; the strike at t=138 lures them |
| 139.5 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 142 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 3 | rear | The last rear ambush; gone before the Hornets of t=154.5 take their stations |
| 144.5 | 4 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | |
| 147 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 149.5 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | left side | Edge-warned |
| 152 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 154.5 | 5 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 3 | front | ≥ 80 px between the hulls |
| 157 | 5 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front | The strike at t=158.5 lures them |
| 159.5 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | rear | From the bottom edge on its path, edge-warned |
| 162 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 165 | 5 | stream | [Buzzsaw Drone](../../../enemies/air/buzzsaw-drone/README.md) | 4 | right side | Edge-warned; no Harrow until t=175 |
| 167.5 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 170 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 172.5 | 5 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | |
| 175 | 5 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | rear | From the bottom edge, edge-warned; out of the lightning (the last strike was t=163) |
| 177.5 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 180 | 5 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | A leader at the tip |
| 182.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | **The last wave**; the calm from ≈ t=189 to the end at t=196 |

Totals: Skitter 66 · Talon 49 · Mote Swarm 120 · Dart Drone 35 · Chimera 30 · Harrow 35 · Buzzsaw Drone 17 · Lamprey 4 · Wraith 6 · Needler 3 · Hornet 5 · Stinger 5.
<!-- /data -->

Harrows: 35 in 8 waves (3, 4, 4, 5, 6, 4, 5, 4; easy 31, every wave at most 4; hard pinned at medium's 35). Buzzsaw
Drones: 17 in 4 waves. Non-front waves: 16 of 68.

**Measured** (`levelReport -Plevel=38`, step D2, before any tuning; the plan's fit with Rook, three seeds): medium
**375 enemies, 117.8 a minute** (the act's minimum 75), 2.78 on the screen, 56.5 shots a minute, two pauses over 2 s
(the first strike's quiet window t=23.5–28.2 and t=92.0–94.3) and 9.5 s of calm at the end; easy 298 (93.6 a minute),
hard 448 (140.7 a minute, 3.64 on the screen, 94 shots a minute; the formation lever's +20 %). No big-hull overlap.
The caps in flight: at most 4 Harrows on the screen, 1–2 in their tell or ram, at most 6 Buzzsaws, none dashing (the
autopilot takes them in their chase), never two dashes and two rams at once; about 35 rings a run, 86 ring bullets a
minute of flight (easy 72); no strike over a Buzzsaw's tell or a Hornet's hatch tell. The script flown unharmed (nothing
dies, the worst case, report only) holds up to 3 Harrows in their tell or ram and 5 Buzzsaws dashing: a player who kills
nothing meets more than the caps. The Harrows live 1.3 s on the screen (the beam era's small units 0.99 × Level 10's). The t=154.5 Hornet trio flies
the built line abreast, 120 px apart: 68 px between hull edges, not the drafted 80 (their drift keeps them apart;
no overlap), decided 2026-11-01 (round 76 C.4).

### Lightning

The [lightning strike](../../../world/jovian/README.md#hazards--set-pieces) (J3, decided 2026-10-31 (round 72)) as the Jovian
document defines it: the column, its telegraph, the strike's damage to the ship, to Rook and to the enemies on `air`,
the flash and its option. This level sets only **when and where**:

- **Medium, 26 strikes** (sections 2–5, about one every 5.3 s): t = **26** (alone, its column far from the ship), 33,
  39, **43.5**, 51, 56, 61, 66, **71.5**, 78, 83, 91, **95.5**, 102.5, 108, 114, 119, **122.5**, 127.5, 134.5, **138**,
  143.5, 150.5, 155.5, **158.5**, 163 (the telegraph's start; the strike follows 1.0 s later; the level's
  [data.yaml](data.yaml) holds each column's x). None in section 1 or after t=163 (the breakout climbs out of the
  storm). Step D2 moved the drafted times (50, 60, 67, 72, 84, 90, 96, 102, 123.5, 128, 134, 139, 144, 149, 155,
  159.5) by at most 1 s: the world's rule allows no tell within 1 s of any wave's entry.
- **The lures** (bold above): six strikes are timed 1–1.5 s after a Harrow wave's entry, their column across the
  wave's chase lane (a front stream's left or right lane, a side stream's edge, the bottom stream's left lane, the
  cross's middle), so a player who drags the Harrows through a told column gets the kill, the bounty and the
  `HARROWS` count, and their rings open away from the ship.
- **Placement rules** (the Jovian document's): never two strikes within 1.5 s; no telegraph starts on the ship's x
  within 1 s of a wave's entry; and this level's own: **no strike over a Buzzsaw's tell** (the strikes before t=62,
  t=84.5, t=128.5 and t=165 end before the waves' first tells; none after t=163) and never a column through a Hornet
  wave's station row while its salvo tells.
- **Easy: 13 strikes** (t = 26, 39, 51, 61, 71.5, 83, 95.5, 108, 119, 127.5, 138, 150.5, 158.5), never within 60 px of
  the ship's x at the telegraph's start. **Hard: all 26**, and every third from t=51 (51, 66, 83, 102.5, 119, 134.5,
  150.5, 163) is a **tracking** column that locks on the ship's x at its telegraph's start.
- **Columns moved by the build** (decided 2026-11-01, round 76 C.5, C.6): the t=155.5 column at x=180, not 60 (step
  D2: `levelReport` flew the drafted one over a Hornet's hatch tell); the t=119 column at x=375, not 420 (step D3: the
  t=111 pair's right Hornet drifts under the drafted column while it keeps apart from its partner).
- **Rook** keeps out of a told column ([Rook in Act 6](../../../player/wingmen/README.md#rook-in-act-6-draft)).

### Wind

[Level 12](../../act-2-homefront/level-12-storm-front/README.md#weather)'s gusts as built (the Jovian
[wind shear](../../../world/jovian/README.md#hazards--set-pieces)), blowing **from the right** (the zonal wind of
this band), in **two spells**: t=80 and t=86 (section 3) and t=140 and t=146 (section 4); hard adds t=111 (step D2:
not t=113, two seconds from the t=114 strike's tell). Level 12's
push, ramps and telegraph unchanged (easy pushes less); they move the ship, Rook and the Lampreys, never bullets,
Harrows' rams or the rings. No gust within 2 s of a lightning telegraph.

## Hazards

- **Lightning strikes**: above (*Lightning*).
- **Wind gusts**: above (*Wind*).
- **Burst rings**: the [Harrow](../../../enemies/air/harrow/README.md#behaviour)'s death-burst on every death on the
  screen, its ram included: the level's main bullet load at close range.
- **Special limits** (J2, [Act 6 specials](../../../player/specials/README.md#act-6-specials-draft)): `NO GROUND` for
  the Bomb Rack and the ground-only shells; the Airstrike, the EMP Burst (it stuns the hybrids as machines), the
  Smart Bomb (it clears the rings, never a column), the Decoy Flares (the Hornets' missiles; a Harrow ignores them)
  and the Shield Overcharge work; the Sonar Pulse reads `NO WATER`.

## Secrets and pickups

- **Aurelia's cargo pod** (the secret, section 4, t≈118): a gold cargo pod torn loose from the falling city, tumbling
  slowly down the left edge's storm haze on `air` with its lights dead (dark against the dark grade; the Salvage
  scanner's glint shows it at any range, a lightning flash outlines it for 0.5 s). **20 HP**; destroyed, it frees a
  crate worth **100** in Act 1 terms (1,049). A strike that crosses it opens it too. Its event line, Varga: "*Aurelia
  freight. Someone tried to save it.*"
- **Shield cell**: dropped by the second Hornet of the t=111 pair (if the player kills it).
- **Armour patch**: a CDF supply canister dropped by the fleet through the storm at t≈100 on its parachute, drifting
  down the middle (no friendly brackets since round 79: every shot passes it, [HUD](../../../ui/hud/README.md#act-6-additions-draft)). **It has no
  latch** (decided 2026-11-01, round 76 L.1 b): it opens by itself over the middle and releases the armour patch (Level
  14's supply drone). It opens 270 px from the top (`opens_at: 270`: the
  middle, about 4.75 s after it enters on easy and medium, t≈104.75), lets the patch fall and is gone; until then
  every shot, bullet and dash passes it, so the patch falls on every run. (First built with a 10 HP latch that only the player's shot broke,
  step D1; step D3 found the plan's fit broke it in 17 of 18 runs only with stray missiles or Rook's fire, the beam
  passing debris and the Tesla pod seeking units: hence L.1 b.) A Harrow's ram still ignores the canister.
- **Overdrive**: dropped by the last Chimera of the t=126 wing.

## Radio chatter

Spoken radio, rendered from the data, text for the **1 s rule** (text at about 12 characters a second plus a 1.5 s lead for the length
check; 0.4 s of silence; no timed line more than 1 s late); event lines drop after 6 s. Rook's lines carry `requires:
escort`. The Choir is **quiet** in this level (the gap analysis): no Vrell voice, no Vorne.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "The storm layer. Their trackers can't see through it, and neither can we." |
| t=23 | Varga | "Lightning. A thin line, then the strike, top to bottom. It hits them too." |
| t=34 | Varga | "Harrows: hybrid rammers. However they die, they burst into a ring. Kill them far off." |
| The first enemy killed by a strike (event, bark) | Rook | "Did the sky just take my kill?" |
| t=52 | Okafor | "Contacts from the flanks. The storm turns everything round down here." |
| t=62.5 | Varga | "Buzzsaws. Out here they bounce off nothing but the edges." |
| t=73 | Varga | "Lampreys on that Talon wing. The Vrell hunt them everywhere now." |
| t=109.5 | Okafor | "The Red Spot's rim. Every storm on this planet feeds it." |
| t=134.5 | Varga | "Harrows on every side. Find the gaps in the rings." |
| t=150.5 | Okafor | "Climbing out. Io's ahead, and every refinery he has left." |
| t=186 | Rook | "Lightning, kamikazes, buzzsaws. Next time I pick the scenic route." |
| Secondary met (at the end: 23 of the 35 Harrows down) | Varga | "Most of their Harrows down. Aurelia's labs will notice." |
| Level end | Okafor | "Out of the storm. Io, Lancer." |
<!-- /data -->

The secret's line is the secret's own (its `radio`, played when the cargo pod's crate is freed): Varga, "*Aurelia
freight. Someone tried to save it.*" Fourteen lines in all.

Timeline check (text, by the rule above): the t=1 line closes at about t=8.6; Varga's lightning line (t=23) at about
t=30.6, after the first strike's telegraph (t=26) has shown what it describes and before the Harrow line (t=34; step
D4: on the game's typed pages the lightning line runs to about t=33.9, so the Harrow line moved from t=32.5, decided
2026-11-01, round 76 C.7), which closes at about t=45.7 there, as the first lure (t=43.5) strikes. The flank line (t=52) closes at about t=59.2, the
Buzzsaw line at about t=68.8, the Lamprey line (t=73, the hunt's entry at t=74) at about t=79.8. The cross warning
line (t=134.5, the edge warnings from t=134) closes at about t=140.2; the breakout line at about t=156.8. Rook's last
line (t=186) closes at about t=193.0, inside the calm. Without Rook his two lines drop.

## Music & ambience

[Track 15](../../../audio/music/README.md#track-list) "Storm Wall", the Act 6 B theme (heavy, stormy, choir and
distorted bass; round 72 M.1 a, built in part C): its intro over the drop under Aurelia, the base through the storm
wall, the full mix from the cells (the data's `full_section: 3`), a lift at the breakout; the music plan keeps track 15 here, so the storm
and the war sound alike ([cues](../../../audio/music/README.md#cues-per-level-act-6)). Ambience: the `jovian-sky` bed
([sfx](../../../audio/sfx/README.md#ambience-per-setting): a wide wind roar, rain-less, thunder rolling far off),
**+3 dB in the storm sections** (Level 13's whiteout rule); each strike's thunder from Level 12's set keyed to the strike with a **close crack** (the
Jovian document's brief); the gusts' rush from Level 12. The bed's rise is the data's `ambience_db` by section
(sections 2–4). The Harrow's sounds ([sfx](../../../audio/sfx/README.md), Freesound CC0/CC-BY): the lock tell and the
burst are round 72's picks (S.5 b, S.6 a); its hunting whistle (a 2 s loop for the nearest hunting Harrow, its pitch
rising over the chase, stopped at the lock tell) and its ram whoosh play their a variants until round 76's picks
(step AUD-D, built in D4).

## Credit budget

Budget(38) = 700 × 1.07³⁷ ≈ **8,557** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act 1
terms** (Skitter 5, Mote 2, Talon 12, its leader 25, Dart 2, Chimera 14, Harrow 12, Buzzsaw 12, Lamprey 12, Wraith
30, Hornet 26, Stinger 15, Needler 12, the secret's crate 100, the secondary 100; the units' pages), × the act factor
**10.486** (1.6⁵), bounties × the scale. A **`bounty_scale` of 0.43** (the gap analysis's 0.40–0.56 band) puts the
typical haul **+1.0 %** over the budget by hand (−1.0 % measured, below) (the economy's shares: air kills 60 %, the secret and the secondary 50 %). No
ground targets, no boss.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 66 × 5 + Talon 49 × 12 + Mote Swarm 120 × 2 + Dart Drone 35 × 2 + Chimera 30 × 14 + Harrow 35 × 12 + Buzzsaw Drone 17 × 12 + Lamprey 4 × 12 + Wraith 6 × 30 + Needler 3 × 12 + Hornet 5 × 26 + Stinger 5 × 15 | 12,370 | 7,422 |
| Squad leaders: 5 × 13 extra (perfect run only) | 295 | 0 |
| Secret: Aurelia cargo pod (hidden crate, 1% of budget) | 1,049 | 524 |
| Secondary: down 23 of the 35 Harrows | 1,049 | 524 |
| **Total** (bounty scale 0.43) | **14,763** | **8,471** |
| Budget(n) = the typical haul's target; typical -1 %, perfect 1.73 × budget | | 8,557 |
<!-- /data -->

**Measured** (`levelReport`, step D2): the typical haul **8,471 (−1.0 %)** at the README's scale **0.43** (kept;
0.44 would hit the budget), the perfect run 14,763 (1.73 × budget; `TypicalHaulTest`); easy 7,047, hard 9,765. The
autopilot on the plan's fit wins 3/3 on every difficulty and meets `HARROWS` in every run (35 of 35 down at medium
and hard, 31 of 31 on easy, against targets of 23, 23 and 21). Step D3's `Level38Test` (the autopilot's storm
play: out of told columns, the Harrows at range, a sidestep in a ram's last 0.3 s): 3/3 on every difficulty with Rook
and without, `HARROWS` met in all 18 runs, the caps held in flight. In `ActPlaythroughTest` Rook ends the level at 0
of his 80 armour on medium and hard (11 on easy): a note for the act's escort load (round 76).

Prey kills by the Vrell (two hunts, 6 units) pay nothing; they come out of the 40 % of the air enemies the typical
player misses, so the typical haul stands. Lightning kills pay as the player's.

**Balance plan** (the [Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft), its L38 row): the
visit buys nothing (Composite V is in the shop from this visit; the plan buys it at the L41 visit, J11 of round 72); the loadout is
the L37 one (the beam, the Tesla Coil Pod on the left wing, a Micro-missile Pod on the right, the Swarm Tail), Rook
with his Autocannon. The level asks `homing` (the side and rear entries, the cloaked Wraiths) and `beam` (it denies
the Chimeras' [regeneration](../../../enemies/README.md#regeneration) and burns down a Harrow stream on its line); the
plan's pods and the Swarm Tail give the homing and the rear, the beam the rest. The plan flies at its power cap here
(no room for a utility until the L40 visit): the Act 5 lesson is met only from L40, the hand plan's choice (see *Open
questions*).

## Difficulty notes

- **Easy**: 13 strikes, none near the ship; gusts at Level 12's easy push; the Harrows' slower tell and ram and at
  most 4 on the screen (their page); the Buzzsaws' easy hooks; the formation lever thins the popcorn; the secondary's
  target stays 65 % of the Harrows flown (easy flies fewer: the target follows the count).
- **Hard**: all 26 strikes with eight tracking columns; a third gust at t=111 (step D2; drafted t=113); the Harrows' faster tell and ram and
  faster rings; the Buzzsaws' third dash; the Hornets' and Wraiths' hard hooks.

## Concept art

Round 76 (M10 part D, step ART-D; production art, proposed): the storm-layer kit by `tools/art/backdrop_l38.py` (the
shared Jovian pieces in `tools/art/jovkit.py`), in round 72's L38 grade (storm-dark slate, one purple-grey for every
cloud, mean about 75, the darkest windows about 50): the deep layer one continuous field cut into the five sections'
tile sets (the storm's top under Aurelia's wreck, the heavy floor, the floor under the cells, the Red Spot's curving
rim with its rust glow, the breakout thinning to black sky, stars and Io's disc), two cloud walls and two storm cells
as deep pieces lit from inside by lightning (additive `-glow` pieces on the real clock, frame 0 the brightest: the
pulse the game may draw on each told strike), Level 37's sinking city as Aurelia's wreck and its fall debris on
`low-air` in the storm's grade (ART-C2's pieces), the heavy atmosphere's scud on `high-air`; at most two animated
pieces on the screen at once. Aurelia's cargo pod and the CDF canister by `tools/art/storm_layer_units.py`
(the canister's latch a separate overlay, so the no-latch answer (round 76 L.1 b) needs no new art). The lightning column is round 73's
([Jovian system](../../../world/jovian/README.md#concept-art)); the Harrow's frames are in its
[document](../../../enemies/air/harrow/README.md#concept-art), the briefing images in the
[briefing](../../../ui/briefing/README.md#concept-art) (`briefing-images-final-r76-a`). The proposal
(`concept/backdrop-proposal.yaml`, step D2's input, not data) holds the tile sets, pieces, placements, atmospheres,
the sections and the pod's and the canister's `debris`. Prompts and briefs: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r76-a.png](concept/backdrop-final-r76-a.png) | Production backdrop: twelve screens at half size with stand-ins (the wreck and the debris, the storm's top, the first strike's tell alone and the strike, the first Harrows, a wall lit from inside, a Buzzsaw circle over the cells, the canister, the Red Spot's rim with the pod, a Harrow cross by the rim, the breakout, Io's disc), the readability crops at 1× (each section's darkest and brightest window with a Harrow hunting and one in its tell, a Buzzsaw, needles, an orb, the ship; the measured means), the deep pieces with their brightest flash, the debris, the tile sets (sheet) | chosen (round 76 F.2 accept) |
| [concept/backdrop-final-r76-a.gif](concept/backdrop-final-r76-a.gif) | t 24–34 at half size (10 frames a second): the first strike's tell and strike alone, then the first Harrows | chosen (round 76 F.2 accept) |
| [concept/cargo-pod-final-r76-a.png](concept/cargo-pod-final-r76-a.png) | Aurelia's cargo pod: its 16 tumble frames at 2× over the left edge at t=118, lamps dead, the lightning flash's outline, the break in air (sheet) | chosen (round 76 F.4 accept) |
| [concept/cargo-pod-final-r76-a.gif](concept/cargo-pod-final-r76-a.gif) | The pod tumbling down the left edge, outlined by a flash, then broken open | chosen (round 76 F.4 accept) |
| [concept/canister-final-r76-a.png](concept/canister-final-r76-a.png) | The CDF supply canister on its parachute: the sway with the latch overlay (QD-4 a) and without it (QD-4 b), the release's ten frames (sheet) | chosen (round 76 F.5 accept; L.1 b: the sheet's sway without the latch is the canister's look) |
| [concept/canister-final-r76-a.gif](concept/canister-final-r76-a.gif) | The canister drifting down the middle, its latch shot, the release | chosen (round 76 F.5 accept; L.1 b: the latch is dropped, a later code step) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (tile sets, pieces, placements, the atmospheres), the sections and the pod's and canister's `debris` for step D2 (not data) | chosen (round 76; step D2's input, built) |
| [concept/walls-capture-r76-a.png](concept/walls-capture-r76-a.png) | In the game (step D5, the parked medium run; [recipe](concept/prompts.md#level-38-capture-r76-a)): the first strike alone at t=26, the storm wall's inner glow pulsing on its tell, the strike | chosen (round 76 F.3 accept) |
| [concept/walls-capture-r76-a.gif](concept/walls-capture-r76-a.gif) | The same, t 25.5–28.3 | chosen (round 76 F.3 accept) |
| [concept/cross-capture-r76-a.png](concept/cross-capture-r76-a.png) | In the game: the t=137 cross, all four edges warned, one Harrow from each, the t=138 lure's strike and the rings over the Red Spot's rim | chosen (round 76 F.7 accept) |
| [concept/cross-capture-r76-a.gif](concept/cross-capture-r76-a.gif) | The same, t 136–141 | chosen (round 76 F.7 accept) |
| [concept/pod-capture-r76-a.png](concept/pod-capture-r76-a.png) | In the game: Aurelia's cargo pod tumbling down the left edge, a flash's outline, hit, broken open, its crate collected (+1,049) | chosen (round 76 F.4 accept) |
| [concept/pod-capture-r76-a.gif](concept/pod-capture-r76-a.gif) | The same, t 118–124.7 (the field's left 180 px) | chosen (round 76 F.4 accept) |
| [concept/canister-capture-r76-a.png](concept/canister-capture-r76-a.png) | In the game: the canister under its brackets, its latch shot by a Scatter Vulcan, the release and the armour patch; with the plan's beam (parked) it drifts out unshot | chosen (round 76 F.5 accept; L.1 b: it shows the latch the build still has, to be removed by a later code step) |
| [concept/canister-capture-r76-a.gif](concept/canister-capture-r76-a.gif) | The canister run, t 100–105 | chosen (round 76 F.5 accept; L.1 b: it shows the latch the build still has, to be removed by a later code step) |
| [concept/readability-capture-r76-a.png](concept/readability-capture-r76-a.png) | 1:1 crops from the game: a Harrow and ring bullets over the Red Spot's dark brown, a ring over the storm floor, a lured ring and a Harrow's tell over the walls | chosen (round 76 F.8 accept) |
| [concept/tracking-capture-r76-a.png](concept/tracking-capture-r76-a.png) | In the game, hard: the t=66 tracking column told on the parked ship's x, locked and striking (the ship invulnerable) | chosen (round 76 F.9 accept) |
| [concept/tracking-capture-r76-a.gif](concept/tracking-capture-r76-a.gif) | The same, t 65.5–67.5 | chosen (round 76 F.9 accept) |

## Implementation

- [x] Level data `data.yaml` (when built): sections and speed, `ground: none`, waves with their `from` and edge
      warnings, the two hunter/prey links, the lightning schedule per difficulty (times, x, tracking), the gusts,
      radio, budget, threat profile, briefing, backdrop, music
- [x] Lightning strikes as the Jovian document's rule: the told column, the strike on the ship, Rook and `air`
      enemies (paid, counted for the kill ratio), the placement rules validated (1.5 s apart, no telegraph on the
      ship's x within 1 s of an entry, none over a Buzzsaw tell), the flash option, the thunder event (part A's
      `Lightning`; `LightningBeltDataTest`, `Level38Test`, the storm-layer checks of step D2)
- [x] The Harrow and the Buzzsaw's Act 6 use as their pages; the caps (6 Harrows, 2 rams; 6 Buzzsaws, 2 dashes;
      never two dashes and two rams at once) checked over the wave script (`levelReport`; held in every flown run,
      exceeded only by the script flown unharmed: round 76 L.2)
- [x] The two hunts (Q3 a) as the enemies' three-way rule; prey killed by the Vrell unpaid (`LightningBeltDataTest`)
- [x] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), the act's 75 a minute (`DensityTest`), ≥ 60 %
      from the front, the quiet windows, ≥ 4 s of calm after the last enemy
- [x] `HARROWS n / 23` kill-ratio secondary, judged at the level end, lightning kills counted: a `kill_ratio`
      scoped to one enemy (a new `enemy: harrow` key; the schema's ratio counts every enemy)
- [x] `NO GROUND` for the ground-only weapons (the hangar intel and the briefing's threat line show it)
- [x] Radio voiced, every timed line at most 1 s late, with Rook and without (`requires: escort`;
      `RadioTimelineLevel38Test`)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel
- [x] Briefing: both pages, each one screen, with their images
- [x] Typical haul within ±5 % of budget(38) = 8,557
- [x] Music (track 15) and the `jovian-sky` bed (+3 dB in the storm sections); the strike's close crack
      (`Level38SoundsTest`)
- [x] Easy/hard variations; the autopilot flies it on every difficulty, with and without Rook (`Level38Test`: 3/3
      each)
- [x] Production art and captures for the round (round 76: step ART-D's art in the game since D4, step D5's
      captures)
- [x] Round 76 L.1 b: the CDF canister has **no latch**: it opens by itself over the middle and releases the armour
      patch (as Level 14's supply drone); every shot, bullet and dash passes it (decided
      2026-11-01; the simulation's opening chunk, step DE6s; the data's `opens_at: 270`, loaded and checked, the patch
      released on every flown run: `LightningBeltDataTest`, `Level38Test`, step DE6c)
- [x] Round 76 L.1 b in the game: no latch overlay drawn on a chunk that opens by itself (the art needs nothing new;
      step F4: `PropPasses` skips the latch, the release plays on `DEBRIS_RELEASED`)
- [x] Round 76 L.3 a: Rook's armour in Levels 38 and 39 settled in M10 part I's Act 6 balance (decided 2026-11-01;
      step I1: measured on Levels 38–42, plain aimed fire took it, not the storm; his Act 6 weave, `RookWeave`, see
      [wingmen](../../../player/wingmen/README.md#rook-in-act-6-draft): on medium he ends Level 38 at 36 and Level 39
      at 72 of 80, was 0)

## Open questions

Decided in round 72 (2026-10-31); left to the build:

- **The secondary**: drafted as 65 % of the Harrows judged at the end (the gap analysis; it needs a kill ratio scoped
  to one enemy, a new schema key); the alternatives are the built `escapes: harrow` with `allowed: 12` (the same
  count, decided mid-level: failed by the 13th Harrow out unhurt, met by the 23rd kill), a plain kill ratio of every
  enemy (65 %), or no secondary.
- **Names** ("the Lightning Belt", "Under Aurelia", "the Red Spot's Rim"): placeholders for the story step.

## Decisions

- 2026-10-31 (Act 6 design phase, step `m10-d-levels-2`): drafted from the roster row, the gap analysis (Q3 a + c,
  Q7 b, Q10 a, J1–J3, J5 and its decided L38 items: the 65 % Harrow secondary, the Buzzsaws' ricochet off the edges
  only, the Choir quiet) and the wave 1 documents; decided 2026-10-31 (round 72).
- 2026-10-31 (decided by me, trivia): 140 px/s and 196 s; 26 strikes at medium (13 easy, 26 with 8 tracking on hard),
  six of them lures timed on Harrow waves; the gusts from the right in two spells; no foreground banks here (Level
  36's); a Buzzsaw wave and a Harrow wave at least 7 s apart; the level's tiny tier from the Dart streams and the
  motes (J5); the secondary's target shown as `n / 23` from the start; the secret a cargo pod from Aurelia (a strike
  opens it too).
- 2026-10-31, design phase (step m10-d-act; decided 2026-10-31 (round 72)): the ambience is D-audio's `jovian-sky` bed, +3 dB in the
  storm sections (no own `jovian-storm` bed); the music plan keeps track 15 (the open question is closed).
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): J3 (the lightning: told columns
  that hit the ship and the `air` enemies and pay their kills), Q3 a (two hunts; c rejected), Q8 a with J11 (the
  Crucible at the Level 40 visit; no utility in the fit at this visit) and Q7 b as drafted. The document approved as
  drafted (D.4; `design: approved`).
- 2026-11-01 (M10 part D, step D2; decided as trivia, decided 2026-11-01, round 76 C.2, C.3, L.2): the hand tables became
  the level's [data.yaml](data.yaml) on ART-D's backdrop proposal (its sections at the README's times, so no tile set
  moved). Sixteen strike tells moved by at most 1 s (the world's rule: no tell within 1 s of any wave's entry, which
  the drafted times broke) and the lures to 1.0–1.5 s after their Harrow waves, each column on its wave's chase lane;
  the t=155.5 column between the Hornets' stations (`levelReport` flew one over a Hornet's hatch tell at x=60). Hard's
  third gust at t=111, not t=113 (two seconds clear of the t=114 tell). The t=159.5 Skitters fly a rear snake on its
  path (only chasers and dashers stream in from the rear) and the t=74 Lampreys the built side swarm. The Harrow
  waves are pinned at most 4 on easy and at medium's counts on hard (the caps), the Hornet waves likewise (at most 3).
  The secondary is the Implementation's `kill_ratio` with `enemy: harrow` (m10d-gaps.md §4), the music's bed rise a
  new `ambience_db` by section, the canister built as drafted (the friendly brackets and a 10 HP latch; round 76 L.1 b then dropped the latch).
  `bounty_scale` 0.43 kept (typical −1.0 %).
- 2026-11-01 (M10 part D, steps D3 and D4; decided as trivia, decided 2026-11-01, round 76 C.6, C.7, C.8): the t=119 column
  at x=375, not 420 (D3: the t=111 pair's right Hornet drifts under the drafted one); Varga's Harrow line at t=34, not
  t=32.5 (D4: her t=23 line runs to t≈33.9 on the game's typed pages); the threat profile's setting "Jupiter, storm
  layer" (the drafted wording ran out of the intel panel); the strike's pulse on the walls and cells only.
- 2026-11-01 (M10 part D, step D5): implementation done (the full `check` green but for Level 39's three known reds,
  step E4's); the captures from the game and [round 76](../../../concept-rounds/round-76/README.md) opened: part D's art
  (F.1–F.11), the Harrow's sounds, the voices, the Buzzsaw's Act 6 return (QD-1), QD-4 the canister's latch, the caps
  with nothing shot, Rook's armour and the steps' calls.
- 2026-11-01 (user, round 76 closed; page answers, no notes): **every row as recommended except L.1 b, which is the
  recommendation too**: art F.1–F.11 accepted; sounds S.1 a (the whistle: the jet's thin approach) and S.2 a (the ram:
  the heavy spear, CC-BY), the b files moved to `concept/rejected/`; voices V.1 accepted; **D.1 approved** (the Buzzsaw
  Drone's Act 6 return: its README `design: approved`); **L.1 b**: the CDF canister has no latch (the build follows in a
  code step: open Implementation item); L.2 accepted (the caps stay authored; an unharmed script may exceed them); **L.3 a**
  (Rook's armour settled in part I's Act 6 balance); C.1–C.8 accepted (every "pending the user's review in round 76"
  note is now "decided 2026-11-01 (round 76 <id>)"). `art: chosen`; `implementation: in-progress` until the latch is out.
- 2026-11-02 (M10 part I, step I1; round 76 L.3 a): measured per level 38–42 (medium and hard, 3 seeds) what takes
  Rook's armour: the Talons', Harrows' and Chimeras' aimed pairs (here 64–230 damage a level when he flies the whole
  level, medium and hard), lightning 10–20, a ram now and then; his Act 2 dodge saw most of those bullets late or never. His Act 6 weave
  (wingmen) leaves him 36 of 80 at the end on medium (0 before); no keep-out added.
- 2026-11-03 (M10 part I, step I4): `implementation: done`: the canister's latch is out (round 76 L.1 b: it opens by itself at `opens_at`, step DE6c) and the Implementation list is ticked; the full `check` is green.
