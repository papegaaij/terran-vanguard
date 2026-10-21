---
title: Level 09 – Arcology Fall
design: approved
implementation: done
art: final
depends-on: [../../../enemies/ground/hive-node, ../../../enemies/ground/ravager, ../../../enemies/ground/creeper, ../../../player/wingmen]
updated: 2026-10-18
---

# Level 09 – Arcology Fall

## Summary

The first `destroy-targets` mission of Act 2. In the last hour of the night the Vrell have grown
six **hive nodes** in three clusters across the arcology district of Nova Lagos, and every node
breeds Skitters until it dies. The nodes are hardened, so the level is the first that *needs* an
`anti-ground` source. Over each cluster the scroll eases down (a **hold zone**) until both nodes
are dead. Ravager packs, new animal-like pack hunters, gallop through the ruined streets and pounce
at the player; on the Okonjo Bridge they go for a CDF truck convoy. When the last cluster dies, the
creep-hollowed Ndidi Arcology leans, then collapses straight down into a dust blast. About 3 minutes 20 seconds with typical
holds (186 s of script time); no boss.

## Briefing

Two pages, as Level 08's (user decision D3 of M5 part B), each **one screen** (the rule kept in
round 30: from Act 2 on, an act page and a level page each fit one screen; `BriefingLayoutTest`):
Okafor's mission, then Varga's scan of the nodes and the new hunter. Level 08 ended with "Get some
rest. The arcologies are next."; this level flies before dawn of the same night.

<!-- data: briefing -->
> **Commander Okafor:** "Overnight the Vrell grew six hive nodes in the arcology district,
> Lancer: three clusters, A, B and C. Each one breeds swarmers, and their creep eats the towers.
> Destroy all six. Leave one alive and we lose the district."
>
> **Dr. Varga:** "The nodes are hardened: only bombs, mortars or the Airstrike crack them,
> Rook's mortar too. And something new hunts the streets in packs. Fast, low, and it leaps at
> you. Shoot it before it jumps."
<!-- /data -->

*Images* (production art straight away, user decision D9 = a of M5 part C;
[briefing images](../../../ui/briefing/README.md)): Okafor's page `level-09-arcology-district` (the
district from above before dawn: the elevated highway in, Unity Plaza, the boulevard, the lagoon
and the Okonjo Bridge, the Ndidi Arcology; the three clusters marked A, B, C), Varga's
`level-09-node-scan` (a Hive Node's scan with its open iris and the hardened marker, the
`anti-ground` sources, and a Ravager's pounce arc with its air window); the level's data names
them as each page's `image`.

*Hangar teaser* (shop screen before L09; Rook, `LT. K. TANAKA / AEGIS TWO`, not voiced, as round
30's):

<!-- data: teaser -->
> **Rook:** "Hardened nodes ahead, Lancer. Fit something that hits the ground, or fit me the
> mortar."
<!-- /data -->

## Threat profile

Generated from the level data's `threat_profile`.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `ground` (hive nodes, Ravager packs, Creepers, turrets, mortars), `air`; `low-air` carries smoke and dust (scenery) |
| Attack directions | front 97% · sides 3% by enemy count (two Needler pincers and the Mantis pincers) |
| Density | 3 |
| Recommended traits | `anti-ground` (**required**, user decision D7 = a), `area` |
| Hazards | the arcology collapse (scripted, on the ground layer, harmless to the player) |
| Boss / mid-boss | none (finale: the third hold and the collapse) |
| Sensor-suite detail | none: Earth megacity before dawn, `ground` + `air`, front · L1: + directions, density 3, the hazard "structural collapse", the objective `DESTROY 6 HIVE NODES` · L2: + Hive Node (new), Ravager (new), Skitter, Needler, Stinger, Spore Bomber, Mantis, Creeper, Spine Turret and Polyp Mortar portraits; no boss · L3: + `anti-ground` and `area` highlighted, the wave strip marking the three node clusters (37, 119 and 163 s), 1 secret |
<!-- /data -->

**Launch warning** (user decision D7 = a): the threat profile marks `anti-ground` as `required`
(schema built in M5 part C), so the hangar warns at **every** sensor level, not only from L3,
when the loadout has no source that damages hardened targets: no `anti-ground` weapon, no Rook's
Mortar (fitted while he is not grounded) and no Airstrike or Smart Bomb charge
([hangar](../../../ui/hangar/README.md)).

*Varga's intel line* per sensor-suite level (the data's `varga`):

| Sensor suite | Line |
|---|---|
| none | "The arcology district before dawn, Lancer. Things on the ground that must die, and flyers over them." |
| L1 | "Mostly from ahead, two pincers from the flanks. Six targets to destroy, and a tower may fall. It won't touch you." |
| L2 | "New: the Hive Node, a hardened spawner, and the Ravager, a pack hunter that pounces. Creepers and turrets too." |
| L3 | "Anti-ground is a must: nothing else cracks a node. One cache: a creep cocoon on a boulevard roof. Bomb it open." |

## Objective

- **Primary** `destroy-targets`: destroy the six named [hive nodes](../../../enemies/ground/hive-node/README.md),
  each its own ground-target group: **Node A1, A2** (Unity Plaza), **Node B1, B2** (the Okonjo
  Bridge's south-bank approach) and **Node C1, C2** (the Ndidi Arcology's lobby plaza).
  - **Fails at once** (user decision D1 = a, as Level 05): the first node that leaves the screen
    alive fails the mission through the
    [failed primary objective](../../../systems/retry/README.md#on-a-failed-primary-objective)
    flow (the mission failed screen 3 s later), with Okafor's line on that screen naming the node.
    There is no rear Skitter stream from a missed node, no flown-on lost mission and no timed
    collapse.
  - **Hold zones** (D3 = a): see *Layout*. A hold has no timeout: it ends when both nodes of its
    cluster are dead, or when one leaves the screen alive, which fails the mission.
- **Secondary** *Hold the bridge*: destroy every Ravager of the two bridge-assault packs before one
  reaches the Kilo truck convoy (user decision D6 = a): an `escapes` objective scoped to the two
  waves tagged `bridge`; "reaches the convoy" is leaving the screen alive (the trucks are scenery).
  It counts 8 at medium (easy 6, hard 10); the tracker's second line reads `RAVAGERS n / 8`. Pays
  +101 credits (63 in Act 1 terms × the act factor 1.6) and triggers the Kilo Lead's thanks.

## Layout

Ground scroll speed **150 px/s** at the 960×540 baseline (play field 480×540), the launch the
first 5 s. Total **27,900 px = 186 s of script time** (D2 = a): every time in this document and the
level's data is **script time**, the scroll distance ÷ 150 px/s. In a hold the level clock slows
with the scroll (rate = current speed ÷ 150, 0.2 at 30 px/s), so waves, ground targets, radio
lines, the backdrop's placements and the progress bar wait with the scroll; enemies, bullets, the
nodes' spawn cycle, Rook and the backdrop's animation run on real time. A typical hold costs about
6–8 s of real time for 1–2 s of script time, so the level flies about 3 min 20 s. Motion budget:
the burning towers' smoke and, in section 5, the collapse dust are the strong elements; the Kilo
trucks on the bridge are the moving scenery of section 4 (on the real clock, so they do not crawl
in a hold); the creep on the towers pulses slowly.

**Hold zones** (D3 = a): when the first node of a cluster reaches **y = 200 px below the top
edge**, the scroll eases over 1 s to the hold speed, **30 px/s** (easy 20, hard 40), and stays
there until both nodes of the cluster are dead (hold C until the collapse's dust has settled,
9.0 s after its warning starts, so the falling tower and the rubble heap it leaves stay on the
screen however late cluster C dies); then it eases back to 150 px/s over 1 s. A node
that is not killed leaves the bottom edge about **10.6 s** after the hold starts at medium (15.6 s
on easy, 8.3 s on hard): that is the window, there is no timer. No HUD element shows a hold beyond
Okafor's line at the first one.

**Towers and roofs** (as Level 08, D1 = a of M5 part B): the arcology towers are drawn in true
perspective as **scenery only**; the nodes, turrets, mortars, walkers, the cocoon and its crate sit
at street level or on low structures drawn without lean. Nothing is on `far`. The Ndidi Arcology is
Level 08's perspective ground tower (height 1.5) at the lobby plaza. No skybridges. **Time of day**
(D9 = a): the last hour of the night with a grey glow in the east, so "first light" stays Level
10's.

**Kilo trucks**: the CDF convoy crossing the Okonjo Bridge in section 4 is scenery (shots pass
through, as Level 08's traffic); the bridge Ravagers' goal is the bottom edge.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Arcology Approach | 0–20 | 0–3,000 | 150 | light | `deep`: the sprawl's glow under the smoke haze, a grey glow in the east. `ground`: a ruined elevated highway, burnt-out traffic, a CDF barricade; the first arcology towers in perspective with violet creep veins on their upper floors. `low-air`: smoke wisps. | Launch and set-up; the first creep seen on the towers. |
| 2. Unity Plaza | 20–58 | 3,000–8,700 | 150 | clear | `ground`: a wide plaza with a dry fountain, the creep spreading from nodes A1 and A2 across the paving, park trees at the edges, the turret pair; towers around it. `low-air`: none (the plaza must read clearly). | First hold zone (30 px/s in hold A): learn that nodes need anti-ground and that the scroll waits for you. |
| 3. Boulevard Run | 58–96 | 8,700–14,400 | 150 | light | `ground`: a long boulevard with a burnt-out tram, abandoned cars and tree rows; low roofs either side (the mortars, a turret nest, the cocoon); towers in perspective. `low-air`: dust drifting off the street. | Ravager introduction: one pack alone, then a larger pack around the tram; the cocoon secret. |
| 4. Okonjo Bridge | 96–138 | 14,400–20,700 | 150 | medium | `ground`: the lagoon channel, the Okonjo Bridge running up the screen with the Kilo truck convoy crossing it, the far bank's quay with two mortars, the bridgehead turrets, nodes B1 and B2 on the south-bank approach. `low-air`: smoke banks over the water. | The bridge assault (the secondary), then the second hold (30 px/s in hold B). |
| 5. Arcology Fall | 138–186 | 20,700–27,900 | 150 | medium | `ground`: the Ndidi Arcology's lobby plaza with nodes C1 and C2, the arcology tower hollowed by creep; after the collapse its rubble heap under settling dust. `low-air`: smoke, then the collapse's `heavy` dust banks, ramping out over 6 s. | Third hold (30 px/s in hold C), then the collapse set piece and a quiet end over the rubble. |
<!-- /data -->

**Collapse set piece** (user decision D5 = a; round 31's look c, approved 2026-10-07): when C1 and
C2 are both destroyed (the radio event `collapse`), the arcology **leans slowly to the right for
1.5 s** (the warning: up to 4°, slow at first, bending from its foot, a barely visible shudder,
debris and dust wisps trickling off its walls, its cast shadow on the ground under it), then
**drops straight down into itself in one go in 1.5 s** (the roof sinking and greying with dust,
the top storeys crushed, chunks spat out, the shadow shrinking toward the foot). Dust boils out of
its base 0.35 s before the **impact, 3.0 s after the warning starts**; at the impact a radial dust
blast rolls out in every direction and a billow rises over the foot, and the kills **spread outward
from the tower's foot over 1 s** on the blast's front (a ring growing from 100 to 390 px round the
footprint's centre, eased out): every ground unit in the band (the tower's footprint along the
scroll, the whole width) is destroyed as the ring reaches it and pays its bounty and score like an
Airstrike kill. Air units and the player are untouched. The `heavy` dust peak comes in with the
base dust, is full just after the impact and ramps out over 6 s (an event-triggered peak); the dust
settles onto a rubble heap where the tower stood. The look was an a/b in round 31 (D9 = a): **a**
toppled across, **b** pancaked into a dust wave; the user rejected both and approved the rework,
**c**, with a shorter shadow and a lean in one direction (2026-10-07). A
missed C node fails at once (D1 = a), so the tower never falls on a timer.

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary) (`pack` is new in M5 part
C). Returning `medium` and larger units get the act HP factor from the
[balancing basis](../../../enemies/README.md#balancing-basis): the Creeper (L08) has 32 HP here, the
Spore Bomber 47 and the Mantis 31, and since the user's Act 2 playtest (2026-10-18) the Needler (12)
and the Stinger (15), capped at ×3; the Skitters, turrets and mortars keep theirs.

The table is generated from the level data (M5 part C, step C3); times are script time.

**Waves rewritten after the user's Act 2 playtest** (2026-10-18, PT2-B 1b and 2b; Level 10 the
reference): a wave about every **2.5 s** from the launch's end (t=5.5) to t=182.5, 72 waves of **4.4
units** on average (two rows at one time are one mixed wave; the holds stay the quiet moments to aim
the bombs), the next entering while the last is still on the screen. **320 enemies in the waves** at
medium (337 with the ground units, 349 with the node Skitters, 116 a minute; `DensityTest`): Skitter
166 (52 %, was 65 %), Needler 77 (was 38), Stinger 36 (was 9), Spore Bomber 6, Mantis 2, Creeper 11
(the t=84 convoy of 5; those at t=26 and t=158 kept at 3 ahead of holds A and C), Ravager 22. As in
Level 08 the brief's 6–7 units a wave with at most 40 % Skitters (378 units) was cut back: it put 5
enemies on the screen and 37 shots a minute with the plan's fit, and the plan's fit died in every hard
attempt and let a hold C node escape (accepted in round 31 (2026-10-08)).

**The Ravagers** (PT2-B 1b, "Do not mix the ravagers with the nodes and slow them down a little
bit"): the stat block's gallop is 120 px/s (was 160), a pounce every 4 s (was 3) within 170 px (was
200). No pack runs in a hold: the bridge packs enter at t=100 and t=106 (were 106 and 114), off the
deck about 10 s before hold B; hold C's two packs moved to the gap after hold B, t=141 (3) and t=147
(4, splitting round the podiums). The two packs that cross the street (t=62 and t=141) first gallop
down it at the ship, where a forward gun tracks them, then turn across it. `Level09Test` checks that
no Ravager is on the screen in a hold and that the last one leaves at least 3 s before each hold.

**What the player sees** (the autopilot, seed 2185, the completing attempt; `ActFloorTest` with the
plan's fit, `PacingTest` with the user's fit; the before values on the old waves): with the plan's
fit **3.5 enemies on the screen and 22 enemy shots a minute** (was 1.3 and 4.2; the floor is 2.5 and
10; Level 10 4.2 and 22); with the user's fit 1.9 on the screen, 6.4 shots a minute and 2 stretches over
2 s with nothing on the screen at medium, both in hold C round the collapse (was 0.9, 1.2 and 14; at
most 3).

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 5.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | Over the ruined highway as the launch ends; the first targets for Rook |
| 8 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Over the CDF barricade |
| 10.5 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Curls over the fallen highway span |
| 13 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 15.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 18 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (alternating edges) | |
| 20.5 | 2 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Returning: into the plaza district, laying spores |
| 20.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 23 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 26 | 2 | convoy | [Creeper](../../../enemies/ground/creeper/README.md) | 3 | front | Crawls up the avenue toward the plaza's nodes; kept at 3 ahead of hold A |
| 28.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 31 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | Ahead of the turret pair; the last one drops the Airstrike charge (only with a special fitted) |
| 33.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (alternating edges) | Over the plaza's turret pair |
| 36 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Ahead of hold A |
| 40 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | During hold A, where the nodes' Skitters join them |
| 42 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Over the plaza, during or after hold A |
| 44.5 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 46.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 49 | 2 | pincer | [Needler](../../../enemies/air/needler/README.md) | 4 | sides | 2 per side, hold 4 s; Rook's flank bark |
| 49 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Easy only: the pincer from the front |
| 51.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 54 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 56.5 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | After hold A |
| 59 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | Down the empty boulevard |
| 62 | 3 | pack | [Ravager](../../../enemies/ground/ravager/README.md) | 3 | front | **New.** Gallops down the empty boulevard at the ship, then turns across it; pounces within 170 px; Varga's line at t=58, Rook's on the first pounce |
| 65 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) | After the first pack |
| 67.5 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 70 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | Over the boulevard mortars |
| 72.5 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 74.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | Ahead of the tram pack |
| 76 | 3 | pack | [Ravager](../../../enemies/ground/ravager/README.md) | 4 | front | Splits around the burnt-out tram; the last one drops an overdrive; easy 3 |
| 78.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Over the cocoon's roof |
| 81 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 84 | 3 | convoy | [Creeper](../../../enemies/ground/creeper/README.md) | 5 | front | Along the boulevard past the cocoon's roof; easy and hard 4 |
| 86.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 89 | 3 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | Returning: one from each side, edge warnings; sweeps for 6 s |
| 89 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (alternating edges) | |
| 91.5 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 94 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (alternating edges) | |
| 96.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 100 | 4 | pack | [Ravager](../../../enemies/ground/ravager/README.md) | 4 | front | Bridge assault, part 1 (tag `bridge`): down the bridge toward the convoy; easy and hard 3 |
| 102.5 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Over the far bank; not on hard |
| 104 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Between the bridge packs; not on hard |
| 106 | 4 | pack | [Ravager](../../../enemies/ground/ravager/README.md) | 4 | front | Bridge assault, part 2 (tag `bridge`), off the deck well before hold B; easy and hard 3 |
| 108.5 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Over the quay mortars |
| 111 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 113.5 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 116 | 4 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | Over the bridgehead turrets; on easy the last one drops a second Airstrike charge (only with a special fitted) |
| 118.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Ahead of hold B |
| 120.5 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | During hold B, away from the nodes |
| 124.5 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | After hold B |
| 126.5 | 4 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 128 | 4 | pincer | [Needler](../../../enemies/air/needler/README.md) | 4 | sides | After hold B; 2 per side; Rook's flank bark |
| 128 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Easy only: the pincer from the front |
| 130.5 | 4 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | |
| 130.5 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (alternating edges) | |
| 133 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 135.5 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 138 | 5 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | Into the lobby district |
| 141 | 5 | pack | [Ravager](../../../enemies/ground/ravager/README.md) | 3 | front | Charges down the avenue at the ship, then turns across it; after hold B, clear of the nodes |
| 143.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) | Toward the arcology, between the packs |
| 147 | 5 | pack | [Ravager](../../../enemies/ground/ravager/README.md) | 4 | front | Splits round the podiums, half to each side; gone well before hold C; easy 3 |
| 149.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (alternating edges) | |
| 152 | 5 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Under the Choir's song |
| 154.5 | 5 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 156.5 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 158 | 5 | convoy | [Creeper](../../../enemies/ground/creeper/README.md) | 3 | front | Out of the lobby plaza toward the nodes; kept at 3 ahead of hold C |
| 160.5 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Not on hard, where hold C's nodes need every gun |
| 163 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | As hold C starts; not on hard |
| 167.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | Over the falling tower |
| 168.5 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | Into the dust |
| 170 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (alternating edges) | Over the dust after the collapse |
| 172.5 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Over the dust |
| 175 | 5 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 177.5 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Over the rubble |
| 180 | 5 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 182.5 | 5 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | The last wave; then a quiet end over the rubble |

Totals: Skitter 166 · Needler 77 · Stinger 36 · Spore Bomber 6 · Creeper 11 · Ravager 22 · Mantis 2.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 2 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) ×2 guarding the plaza (t≈35) | Bounty |
| 2 | [Hive Node](../../../enemies/ground/hive-node/README.md) **Node A1**, hardened (t≈37) | Primary target; hold A |
| 2 | Hive Node **Node A2**, hardened (t≈38) | Primary target; hold A |
| 3 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) ×2 on low roofs by the boulevard (t≈68) | Bounty |
| 3 | Spine Turret turret nest ×3 on a low roof (t≈80) | Bounty |
| 3 | Creep cocoon on a low roof by the boulevard (**hardened**, 30 HP; t≈86) | Only `anti-ground` weapons, the Airstrike or the Smart Bomb open it; releases the hidden crate (secret, see below) and drops an armour patch |
| 4 | Polyp Mortar ×2 on the far bank's quay (t≈108) | Bounty |
| 4 | Spine Turret ×2 at the bridgehead (t≈116) | Bounty |
| 4 | Hive Node **Node B1**, hardened (t≈119) | Primary target; hold B |
| 4 | Hive Node **Node B2**, hardened (t≈120) | Primary target; hold B |
| 5 | Spine Turrets guarding cluster C (t≈162), none on easy and medium; hard ×2 | Bounty |
| 5 | Hive Node **Node C1**, hardened (t≈164) | Primary target; hold C; both C nodes dead start the collapse |
| 5 | Hive Node **Node C2**, hardened (t≈164) | Primary target; hold C; both C nodes dead start the collapse |
<!-- /data -->

Totals: Hive Node 6 · Spine Turret 7 (hard +2) · Polyp Mortar 4. Each node releases 2 Skitters
(hard 3) every 4 s while it lives; the density and budget count one opening per node (12
Skitters), as the Hive Node's spec says. The primary's groups pay their bounty like every kill.

## Hazards

- **Arcology collapse** (section 5): ground layer only; it destroys ground enemies, never the
  player.
- No terrain collision; smoke and dust banks sit on `low-air`, below the play plane, and never hide
  bullets.

## Secrets and pickups

- **Rooftop cocoon** (t ≈ 86): a creep cocoon on a **low** roof by the boulevard, a hardened
  destructible of about 30 HP (`anti-ground`, the Airstrike or the Smart Bomb only, as Level 04's
  hardened dugout). Breaking it reveals a CDF supply crate worth **160** credits (100 in Act 1 terms
  × 1.6) and drops an armour patch (its `drop`). Its line, Varga: "*That cocoon was sitting on a
  CDF supply crate. Take it.*"
- **Airstrike charge**: dropped by the last Stinger of the t ≈ 31 column, only with a special
  fitted (as Level 04's supply drop; the draft's shield-cell fallback is struck). Easy adds a second
  one from the last Stinger of the t ≈ 116 column. (A destroyed group drops its pickup only when it
  is an objective group, so the turret pairs the draft named cannot.)
- **Overdrive**: dropped by the last Ravager of the t ≈ 76 pack.
- Shield cells follow the normal drop table.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), as Level 08. Times are script time,
retimed to the HUD's 1 s rule (`RadioTimelineTest`): a timed line holds the radio for the longer of
its voice and its text (5 s for the last page plus 3 s for each page before it, typed at 30
characters a second), then 0.4 s of silence, and starts after the one before it has closed. A
timed line inside a hold stretches in real time with the clock; the radio test flies the holds.
The draft's clashes are retimed: t=2 and 4 to t=1 and 8.5; the CDF officer and Okafor (t=104, 106)
to t=95 (two pages) and t=107 (the first bridge pack enters at t=100 since 2026-10-18; the line stays
behind the Kilo Lead's two pages); Varga and the Choir (t=150, 158) to t=140 (two pages) and t=152.5,
the Choir's 3.9 s stage sound and 5.6 s subtitle closing by ≈ 158.

**New speaker**: the **Kilo Lead**, the CDF officer of the Kilo truck convoy (two lines, the
generic CDF radio portrait), auditioned a/b in round 31 (user decision D11 = a) and cast there:
Aaron Bennett (a); his two lines are voiced. **Rook's lines** carry `requires: escort` (new, built in M5 part C), so they never play
when he is grounded or ejected; his generic barks (the pincers' flank bark among them) follow
[his bark table](../../../player/wingmen/README.md#radio-barks). New events (built in M5 part C):
`hold-start` (the first hold), `first-pounce` (the first Ravager leap) and `collapse`.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Six hive nodes in the arcology district. All six die, Lancer." |
| t=8.5 | Rook | "Six bugs, two pilots. I like those odds." |
| t=24 | Varga | "The nodes are hardened. Normal rounds just spark off them. Bombs, mortars or the Airstrike." |
| First hold starts (`hold-start`) | Okafor | "Slowing you down over the plaza. Make it count." |
| First Hive Node destroyed | Rook | "One down. That's a horrible noise they make." |
| t=58 | Varga | "Fast movers on the ground, Lancer. Low, four legs. Hunting." |
| First pounce (`first-pounce`) | Rook | "It jumped at you! Since when do they jump?" |
| t=95 | Generic CDF (Kilo Lead) | "Aegis, Kilo convoy on the Okonjo Bridge. Sixty civilians in the trucks, and things are coming over the far bank." |
| t=107 | Okafor | "Lancer, keep them off that bridge." |
| t=140 | Varga | "The arcology's frame is hollow with creep. Kill those last nodes and it may come down." |
| t=152.5 | The Choir (distorted) | "[the Choir sings]" |
| Collapse starts (`collapse`) | Rook | "Timber. Somebody had to say it." |
| Level end | Okafor | "All six nodes dead. The outer districts evacuate at first light, and you're flying cover." |
| Secondary met | Generic CDF (Kilo Lead) | "Kilo convoy is across. Thank you, Aegis." |
| A node leaves the screen alive (the failed screen's line) | Okafor | "{group} got past you, Lancer. If it lives, the district is lost." |
<!-- /data -->

The missed-node line is not played on the radio: it is the mission failed screen's line, with
`{group}` the node's name ("Node A1"), voiced once per node (six takes), as Level 05's battery line.
Rook's pounce line now says "at **you**": a Ravager leaps at the player's ship, not at him.

## Boss / mid-boss

None. The third hold zone and the collapse are the finale.

## Music & ambience

The Act 2 B theme **"Firestorm"** (track 7 in the [track list](../../../audio/music/README.md#track-list),
final and with a base stem in M5 part C): the **base stem from the launch**, the **full mix during
each hold** (fading in over 1 s as the hold starts, out over 4 s after it ends) and **from the
collapse** to the end (the music data's `full_on`, a run-time stem hook, built in M5 part C). The
draft's "intensity stem during each hold" had no hook: the stems switched only by section, and an
intensity rising with the density was never built. Ambience: the
[megacity ambience](../../../audio/sfx/README.md#ambience-per-setting) (`earth-megacity`, distant
sirens). The collapse is a low rumble swelling to a long crash and the Ravager's pounce has its own
sound: both new SFX from round 31, the collapse **a** (a stony rumble into a tumbling masonry
crash, CC-BY) and the pounce **a** and **b** both (one picked at random per pounce); the nodes reuse the Vrell spawn and Level 07's iris
([sfx](../../../audio/sfx/README.md#enemies)).

## Credit budget

Budget(9) = 700 × 1.07⁸ ≈ **1,203**, the typical haul's target
([economy](../../../systems/economy/README.md#per-level-budget)), not a perfect collection: the
typical player takes 60 % of the air kills, 80 % of the ground units (the nodes, Ravagers and
Creepers among them) and half the secrets and secondary objectives. The level's `bounty_scale` puts
the typical haul on it within ±5 % (`TypicalHaulTest`): **0.29** since the L10-style waves of
2026-10-18 (0.4 before), the typical haul 1,218 (+1.3 %), a perfect run 1,913 (1.59 × budget). The
table lists the stat-block bounties (Act 1 terms); each is paid × the act factor 1.6 × 0.29, rounded
per kill: Skitter 2, Needler 6, Stinger 7, Spore Bomber 12, Mantis 14, Creeper 10, Ravager 8, Hive Node
21, Spine Turret 6, Polyp Mortar 7. The data keeps Act 1 terms for the rest: the crate 100 (pays 160), the secondary 63
(pays 101). Rook's kills and the collapse's kills pay like the player's. The draft's 1,718 was the
old perfect-run curve with Act-2-scaled bounties.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 166 × 5 + Needler 77 × 12 + Stinger 36 × 15 + Spore Bomber 6 × 25 + Creeper 11 × 22 + Ravager 22 × 18 + Mantis 2 × 30 + released Skitter 12 × 5 | 1,456 | 931 |
| Ground targets: Spine Turret 7 × 12 + Hive Node 6 × 45 + Polyp Mortar 4 × 15 | 196 | 157 |
| Secret: cocoon cache (hidden crate, 8% of budget) | 160 | 80 |
| Secondary: hold the bridge | 101 | 50 |
| **Total** (bounty scale 0.29) | **1,913** | **1,218** |
| Budget(n) = the typical haul's target; typical +1 %, perfect 1.59 × budget | | 1,203 |
<!-- /data -->

**Anti-ground in the balance plan** (user decision D8 = c): the plan's L09 visit swaps the left
Autocannon for a **Bomb Rack** and buys **Rook's Mortar**; the Tail Gun moves to the L10 visit
(1,575 cr with repairs, 58 cr left; forward DPS 64 as the sheet counts the Bomb Rack, 1.02 of the
reference 63; the L10–L14 visits re-planned so none overspends: the Tail Gun at L10, the wing
upgrades and the L10 Airstrike charge later, the Tail Gun's L2 at L14). Anti-ground DPS 12 + 15 =
27 against a cluster's 128 HP (hard 166): 4.7 s (hard 6.1 s) of the 10.3 s window, counted to the
first node's hit box leaving (hard 8.3 s). `BalanceTest` gets a **hardened check**: a cluster's HP ÷ the plan's anti-ground DPS
at most 0.6 × the hold window at medium. The plan lives in `design/player/balance-plan.yaml`.

## Difficulty notes

- **Easy**: holds ease to **20 px/s** (a ≈ 15.6 s window); the bridge packs have 3 Ravagers each
  (the secondary counts 6); both Needler pincers enter from the front as a line abreast; the
  last Stinger of the t ≈ 116 column drops a second Airstrike charge (only with a special fitted).
- **Hard**: holds ease to **40 px/s** (≈ 8.3 s); the Ravager packs as on medium and the bridge packs as on
  easy (3 each, the secondary counts 6; one more each than medium until 2026-10-18, when the slower Ravagers with the hard
  levers wore the plan's fit down in every attempt) and they pounce every 4 s as on medium (2 s until 2026-10-18); the
  Needler and Stinger groups one smaller than on medium, no Spore Bomber lines, no Mantis pincer, the
  t=84 convoy of 4 and none of the rows between the bridge packs or just ahead of hold C (so the plan's fit kills cluster C in hold C's shorter window); the nodes release 3 Skitters per
  opening; cluster C is guarded by a Spine Turret pair; the Creepers' hard fan (their stat block).

## Concept art

The look comes from the chosen megacity scene in [art direction](../../../art-direction/README.md):
[parallax-r03-b.png](../../../art-direction/concept/parallax-r03-b.png), built for this level on Level
08's production kit in the last hour of the night by `tools/art/backdrop_l09.py`. Part C (user
decision D9 = a) takes the backdrop, the Kilo trucks, the cocoon and the two briefing images
straight to production and proposes a/b only for the collapse's look (round 31: both rejected,
the rework c approved and built in the game, its puffs and heap by `tools/art/collapse_l09.py`); the creep and the
nodes follow the chosen [Hive Node](../../../enemies/ground/hive-node/README.md) concept. The
generator's backdrop block, merged into the level data, is kept as
[concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml). Prompts and generator notes:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r31-a.png](concept/backdrop-final-r31-a.png), [.gif](concept/backdrop-final-r31-a.gif) | Production backdrop, pre-dawn, on Level 08's kit: Unity Plaza with its creep, the boulevard, the lagoon and the Okonjo Bridge with the Kilo trucks, the arcology's lobby plaza; the cocoon on its low roof | final (round 44, X.i) |
| [concept/rejected/collapse-r31-a.png](concept/rejected/collapse-r31-a.png), [.gif](concept/rejected/collapse-r31-a.gif) | Collapse a: after the 1.5 s shadow warning the arcology topples across the band (the tower projection tilted about its foot), a dust burst at impact, the crushed body left lying | rejected |
| [concept/rejected/collapse-r31-b.png](concept/rejected/collapse-r31-b.png), [.gif](concept/rejected/collapse-r31-b.gif) | Collapse b: after the same warning the arcology pancakes into itself and a dust wave rolls along the band, leaving a heap and debris | rejected |
| [concept/collapse-r31-c.png](concept/collapse-r31-c.png), [.gif](concept/collapse-r31-c.gif) | Collapse c, the rework after both a and b were rejected: during the 1.5 s warning the arcology shudders and leans a few degrees to the right in one go (it bends, debris and dust trickle off it), its own cast shadow (halved) moving with it (re-rendered with the user's two tweaks); then it collapses straight down in 1.5 s, the shadow shortening right to left; dust billows from its base 0.35 s before the impact, a large dust blast rolls out in every direction at the impact (the kills spread outward from the foot over 1 s, shown as a ring on the sheet only) and settles over 6 s onto a rubble heap | chosen |
| [concept/collapse-capture-final-r31-a.png](concept/collapse-capture-final-r31-a.png), [.mp4](concept/collapse-capture-final-r31-a.mp4) | The collapse, look c as built, in the game (medium, the capture run's key plan): the 1.5 s lean with its cast shadow, the 1.5 s drop, the base dust, the blast at the impact (t 190.7, the sound's crash on it), the settling dust; 10 play-field panels and 15.5 s with sound. The late C kills put the impact near the bottom edge, so the heap scrolls away under the dust once hold C ends (it then ended with the blast; since then hold C lasts until the dust has settled; details in [prompts.md](concept/prompts.md#collapse-capture-final-r31-a)) | final (round 44, X.i) |
| [concept/level-09-capture-final-r31-a.png](concept/level-09-capture-final-r31-a.png), [.mp4](concept/level-09-capture-final-r31-a.mp4) | The level in the game at medium with Rook (Mortar L2), a Bomb Rack on each wing, `--invulnerable`, the ship steered over the cocoon and nodes C1 and C2 by a key script: the holds, the nodes' iris and releases, a Ravager pounce, Rook's mortar on the nodes, the tracker, the bridge with the Kilo trucks, the cocoon, the collapse (the neutral fall until round 31's pick) and the debrief; 12 panels and the whole run with sound (details in [prompts.md](concept/prompts.md#level-09-capture-final-r31-a)). Made before the dust fade-out fix (panel 11) | final (round 44, X.i) |
| [concept/level-09-readability-r31-a.png](concept/level-09-readability-r31-a.png) | Readability strip from the capture: the node, the Ravager running and leaping, the cocoon and the trucks over the night city at 1:1 and 2× | final (round 44, X.i) |

## Implementation

- [x] Scroll timeline, sections, atmosphere and layer content as in *Layout*: Level 09's own
      backdrop before dawn on Level 08's kit, the towers in perspective as scenery, nothing on
      `far`, the Ndidi Arcology as a ground tower; the Kilo trucks as scenery
- [x] Hold zones (D3 = a): per cluster, the ease over 1 s to 30 px/s (easy 20, hard 40) when its
      first node reaches 200 px below the top edge, back to 150 px/s when both nodes are dead, no
      timeout; the level clock slows with the scroll (D2 = a, script time), the backdrop's
      animation on the real clock; the hold in the state hash
- [x] Named targets Node A1–C2 in the two-line tracker (`NODES A1 A2 B1 B2 C1 C2`, then
      `RAVAGERS n / 8`), with two-character marks
- [x] A node that leaves the screen alive fails the mission at once (D1 = a), Okafor's `{group}`
      line on the mission failed screen
- [x] Wave script matches the *Waves* table (final times and counts by the data step); at least
      50 enemies per minute of script time at medium (`DensityTest`, one opening per node)
- [x] The user's Act 2 playtest (2026-10-18): L10-style waves (a wave about every 2.5 s, Spore Bomber
      lines, Mantis pincers, a Creeper convoy of 5); at medium and hard at
      most 3 stretches over 2 s of real time with nothing on the screen with the user's fit
      (`PacingTest`, the holds flown), at medium with the plan's fit at least 2.5 enemies on the
      screen and 10 enemy shots a minute (`ActFloorTest`); the Ravager slowed (120 px/s, 4 s, 170 px)
      and no pack in a hold (`Level09Test`); the Needler and the Stinger with the capped act HP factor
- [x] The arcology collapse (D5 = a, round 31's look c): the 1.5 s lean (the warning), the 1.5 s
      drop, the impact at 3.0 s, the kills outward from the tower's foot over 1 s (radius 100 →
      390 px) in the band, paid as Airstrike kills, hold C until the dust has settled (9.0 s after
      the warning starts); drawn as approved:
      the lean and the drop by the tower projection, the cast shadow, the dust (puff sprites), the
      rubble heap, the `heavy` dust peak ramping out over 6 s; the crash of its sound at the impact
- [x] Ground targets, the cocoon secret and the pickups (the Airstrike charges only with a
      special)
- [x] Secondary objective (D6 = a): the Ravagers of the two `bridge` waves, tracked and rewarded
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`, flown with the holds,
      with Rook and without him); `hold-start`, `first-pounce` and `collapse`; Rook's lines
      `requires: escort`; the Kilo Lead cast in round 31 (D11 = a; a, Aaron Bennett) and voiced
- [x] Threat profile with `required: [anti-ground]` and its launch warning at every sensor level
      (D7 = a), Varga's four intel lines and Rook's teaser fitting the intel panel
      (`IntelPanelLayoutTest`), the Hive Node's and Ravager's intel portraits
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their
      images
- [x] Typical haul at medium within ±5 % of budget(9) = 1,203 with the level's `bounty_scale`
      (`TypicalHaulTest`)
- [x] Balance plan per D8 = c and `BalanceTest`'s hardened check; the autopilot bombs the nodes
      and flies the level to its end with Rook on every difficulty
- [x] Music: the "Firestorm" base stem from the launch, the full mix in each hold and from the
      collapse (`full_on`)
- [x] Easy/hard variations as in *Difficulty notes*

## Open questions

- None open.

## Decisions

- 2026-10-01: Promoted from the Act 2 roster to a draft level document.
- 2026-10-01: Primary `destroy-targets`: a node alive at the end of the scroll fails the mission;
  failing the secondary objective only loses its bonus (user decision). *Superseded on 2026-10-07
  by D1 = a: a node fails the mission as soon as it leaves the screen alive.*
- 2026-10-01: L09 introduces the Ravager (round 05 follow-up).
- 2026-10-01: The "no anti-ground source" launch warning is covered by the generic hangar rule (confirmation when a recommended trait is missing; `anti-ground` is recommended here) — see [hangar](../../../ui/hangar/README.md). *Superseded on 2026-10-07 by D7 = a (a `required` trait).*
- 2026-10-01: A missed node triggers the immediate lost-mission prompt (campaign rule), instead of failing only at the end of the scroll. *Settled on 2026-10-07 by D1 = a: the built failed-primary flow, no flown-on option.*
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../../reviews/acts-1-2/README.md).
- 2026-10-07: M5 part C (user decisions of 2026-10-07): **D1 = a** a missed node fails the
  mission at once, as Level 05 (the failed primary flow, the mission failed screen 3 s later);
  the rear Skitter stream, the end-of-scroll fail and the timed collapse are struck, and Okafor's
  missed-node line becomes the failed screen's line with `{group}`; rejected: b (the campaign's
  lost-mission prompt with "fly on", new UI) and c (as written: minutes of a lost mission).
  **D2 = a** in a hold the level clock slows with the scroll, so the level's times are script time
  (distance ÷ 150) and the density and haul count no hold time; rejected: b (a second,
  distance key for the waves after a hold) and c (halting the scroll). **D3 = a** no hold timeout:
  a hold ends when its cluster dies or a node leaves (a fail); difficulty by hold speed, 20 / 30 /
  40 px/s (windows ≈ 15.6 / 10.6 / 8.3 s from the trigger at 200 px below the top edge); the
  draft's 15 s and easy's 25 s never applied, as the node left first. **D4 = a** the Ravager is
  `air` for the middle 0.3 s of its pounce ([ravager](../../../enemies/ground/ravager/README.md)).
  **D5 = a** the collapse is a simulated sweep that kills the ground units under it and pays
  like Airstrike kills. **D6 = a** the secondary counts the Ravagers of the two bridge waves only
  (a wave tag), 6 / 8 / 10 by difficulty, the trucks scenery. **D7 = a** `anti-ground` is
  `required`: the launch warning shows at every sensor level and counts every source that damages
  hardened targets (an anti-ground weapon, Rook's Mortar while he flies, an Airstrike or Smart
  Bomb charge). **D8 = c** the balance plan's L09 visit swaps the left Autocannon for a Bomb Rack
  and buys Rook's Mortar, the Tail Gun moving to L10, with a hardened check in `BalanceTest`.
  **D9 = a** art straight to production in the last hour of the night (a grey glow in the east,
  "first light" stays L10's), a/b only for the collapse's look, the Kilo Lead's voice and the two
  new sounds. **D10 = a** units used by several levels go into each level's unit atlas
  ([production](../../../art-direction/production/README.md)). **D11 = a** the CDF officer of the
  Kilo convoy (speaker `Kilo Lead`) is auditioned a/b in round 31; his two lines play as text
  until then.
- 2026-10-07: M5 part C stated defaults (accepted with the decisions): the Hive Node's ×2 iris and
  the Ravager's ×1.5 maw struck (weak points drawn only); `armour: hardened` flown for the nodes;
  the node's spawn every 4 s with a 0.5 s telegraph, 2 Skitters (hard 3), shut within 96 px of the
  ship, one opening per node in the density and haul; six groups `Node A1` … `Node C2`, each hold
  naming its two; `pack` in the vocabulary; the data in Act 1 terms (crate 100, secondary 63) and
  a `bounty_scale` (≈ 0.47 by the estimate); the Creeper with the act HP factor (32 HP); density
  from the level's own roster with Polyp Mortars, wave times in script time; the radio retimed to
  the 1 s rule, Rook's "at me" now "at you", new events `hold-start`, `first-pounce` and
  `collapse` ("Cluster C destroyed" as the collapse's event), a cocoon line, Rook's lines
  `requires: escort`; two one-screen briefing pages with images, Rook's teaser (not voiced),
  Varga's four lines, the texts to `review` for round 31; the Airstrike charge only with a special
  and from the plaza pair's **last** unit (the draft's "first" turret and its shield-cell fallback
  struck); the cocoon on a low roof, a hardened destructible of about 30 HP with an armour patch;
  the Kilo trucks as scenery, no skybridges, nothing on `far`, the Ndidi Arcology as Level 08's
  ground tower; the collapse's 1.5 s shadow, 3 s fall, event-triggered `heavy` dust over 6 s and
  rubble; the base stem from the launch and the full mix in the holds and from the collapse (the
  "intensity stem in each hold" had no hook); moving scenery on the real clock; no HUD indicator
  for a hold. The layout's layer notes follow Level 08's D1 (towers on the ground, nothing on
  `far`); the briefing's single 560-character page, the teaser without a speaker, the single Varga
  line, the old budget of 1,718 and the "+100" secondary are replaced. `design` goes to `review`
  for the new texts (round 31); `implementation` is `in-progress`.
- 2026-10-07: M5 part C built. The level data (241 enemies at medium plus the nodes' releases,
  83.9 a minute of script time: the pacing rule, measured in real seconds, needed more Skitters
  after the holds than the design's ≈ 62; `bounty_scale` 0.4, typical haul 1,179 against 1,203,
  perfect run 1,789); the autopilot grades A / A / B (hard misses the bridge secondary).
- 2026-10-07: Hold C stays until the collapse's fall ends (main-agent default): otherwise the scroll
  eases back to 150 px/s and the arcology scrolls about 675 px away during the warning and the fall.
  The collapse's band is the `arcology` tower's footprint on the ground (`collapse.tower`), so it
  follows the tower whenever the nodes die.
- 2026-10-07: Rook aims his lobbed gun (user): with the Mortar his shells land on his ground target
  within 200 px (he picks ground targets anywhere ahead within that range, not only in his 30°
  cone), so the plan's anti-ground (Bomb Rack + his Mortar) works; the autopilot then wins medium
  without an Airstrike charge.
- 2026-10-07: The backdrop, the Kilo trucks, the cocoon and the briefing images go straight to
  production on Level 08's kit in the last hour of the night (D9 = a); the tower creep is violet (as
  Level 08's arcology), the ground creep the Hive Node's teal-black; no bank drifts, so the trucks
  and the fires are the only motion. The secondary is named "Hold the bridge".
- 2026-10-07: [Concept round 31](../../../concept-rounds/round-31/README.md) closed but for row 4
  (user, 2026-10-07): the backdrop with the Kilo trucks and the cocoon approved as **final** (the
  subtle east glow, the two creep hues, the dark trucks and bridgehead ramp as they are), as are the
  two briefing images; the capture and the readability strip accepted; the 20 voiced lines accepted
  as rendered; the texts **approved** (the briefing pages, the teaser, Varga's four lines, the radio
  script with the Kilo Lead's lines, the secondary's name "Hold the bridge"), so the document leaves
  `review` for `approved`; the part C numbers (the density of 83.9 a minute, hard's anti-ground at
  0.73 of its window, the autopilot's grades), the D1–D11 checklist and the build choices (a)–(f)
  accepted. The collapse sound is **a**, both Ravager pounces are kept (one picked at random per
  pounce), the Kilo Lead is **a** (Aaron Bennett). The collapse's look: **a** (topples across) and
  **b** (pancakes into a dust wave) both **rejected**, moved to `concept/rejected/`: the user saw a
  separate shadow sweeping right, a sharp dust line, and a building falling sideways as a whole;
  wanted: a cast shadow that moves with the building (shortening right to left as it drops), a
  little sideways sway and then a collapse straight down (a and b combined), dust in every
  direction starting just before it hits the ground and a large dust explosion at the impact. A
  reworked variant c is in progress (row 4 stays open); `art` stays `chosen` until it is approved.
- 2026-10-07: The collapse's kills spread **outward from the tower's foot at the impact** over about
  1 s, with the dust blast (user; replaces the sweep from left to right, the same band and pay).
  **Planned**: the simulation and the game change after variant c is approved.
- 2026-10-07: Collapse look c built in the game (user approval of c with the tweaks: half the
  shadow, a lean to the right in one go, no sway): the warning is the 1.5 s lean, the drop takes
  1.5 s, the impact is at 3.0 s and the kills ride the blast's front outward from the foot over 1 s
  (the data's `drop` and `blast` replace `sweep`; hold C ends with the blast). The tower is the
  backdrop's own, bent and sunk by the tower projection; the cast shadow, the dust (eight puff
  sprites as particles) and the rubble heap (`arcology-heap`, replacing the provisional
  `arcology-rubble`) follow the concept's numbers; the collapse sound was re-cut so its crash lands
  at the impact. `art` is `final`.
- 2026-10-07: Hold C lasts until the collapse's dust has settled (user): it no longer ends with the
  blast (4.0 s after the warning starts) but once the `heavy` dust has ramped out, the impact + the
  collapse's `dust` 6 s (9.0 s after the warning), then eases back to 150 px/s as before. With
  cluster C killed late (round 31's capture: Node C2 dead 7.6 s into hold C, the foot about 60 px
  above the bottom edge at the impact) the heap now slides down under the settling dust at 30 px/s
  instead of scrolling away at 150 px/s right after the blast. The level flies about 4 s longer in
  real time (the script clock runs at 0.2 for 5 s more); the grades and the radio's timing are
  unchanged.
- 2026-10-07: Concept round 31 closed for the collapse (row 4, user): **c** approved with two
  tweaks (the shadow halved; a single lean to the right instead of a sway), built in the game
  ([collapse-capture-final-r31-a](concept/collapse-capture-final-r31-a.png)); hold C lasts until the
  dust settles. Part C's implementation is done.
- 2026-10-18: The user's playtest: "same issue with the buildings" as Level 08. The same cause (the
  towers drawn lowest first, so an inner roof passed under a taller tower beyond it; here the 1.10 creep
  tower at x 328 next to the 1.35 one at x 429, t=170.04) and the same fix: the towers on screen are
  ordered each frame outermost first ([architecture](../../../tech/architecture/README.md)).
- 2026-10-18: The user's Act 2 playtest ("The ravagers are too hard, especially when trying to kill
  the nodes … Do not mix the ravagers with the nodes and slow them down a little bit"; "the waves are
  still very small and silent"; PT2-B 1b, 2a, 2b, 2c chosen): the Ravager slowed in its stat block;
  the bridge packs at t=100/106 (were 106/114), hold C's packs moved to t=141/147 after hold B, the two
  crossing packs (t=62, t=141) head-on before they turn (the bridge deck and the avenue checked against
  the backdrop: the lagoon tiles carry the bridge from t=96, the lobby district's avenue from t=138);
  Okafor's bridge line kept at t=107 (moved to 101 first: the radio test found it 6 s late on easy
  behind the Kilo Lead's two pages); the waves rewritten L10-style (320 units in 72 waves, Skitters 52 %,
  Spore Bomber lines and Mantis pincers added, the t=84 convoy of 5; cut back from the brief's 6–7 a wave
  once the plan's fit died on hard), the first wave at t=5.5 as the launch ends; `bounty_scale` 0.4 →
  0.29 for the same haul; the hard bridge packs of 3 (the secondary 6) so the hard campaign stays within
  the expert's bound (`ActPlaythroughTest`, −13.9 %); the hard pounce interval 4 s as on medium (was 2), the hard packs as medium, the
  returning Spore Bombers and Mantises and the larger convoys accepted in round 39 (2026-10-18).
