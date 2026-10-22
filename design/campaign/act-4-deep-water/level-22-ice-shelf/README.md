---
title: Level 22 – Ice Shelf
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/ground/drill-rig, ../../../enemies/naval/skimmer, ../../../enemies/air/mantis, ../../../player/specials, ../../../player/wingmen, ../../../world/europa]
updated: 2026-10-22
---

# Level 22 – Ice Shelf

## Summary

Act 4 opens on Europa's ice. Conamara Station, a research town of forty scientists on the Conamara
Chaos ice field, has reported tremors for a week; the cause is four Vrell
[Drill Rigs](../../../enemies/ground/drill-rig/README.md) boring through the shelf toward the ocean
below. Aegis Wing flies the surface one last time before the dive: hardened rigs to bomb, a ring of
Spine Turrets round each, the Vrell flyers over the ice, and the new **ice geysers**, cracks that glow
and burst as the rigs shake the shelf. Jupiter fills the deep layer. It is the act's opener and its
breather: no new fighter, one new ground structure, the same surface rules as Act 3 (the hydro-kit
and the water come in Level 23). 150 s: a shelf approach, a slow rig field (60 px/s) and the run-in to
Conamara.

**Status: approved in round 48 (2026-10-22); drafted (Act 4 design phase, 2026-10-21; step `m8-dl2224`); everything below is pending the
user's review in round 48.** Written from the act roster, the gap analysis's recommended answers and
the wave 1 documents ([Drill Rig](../../../enemies/ground/drill-rig/README.md), the
[specials](../../../player/specials/README.md), the [wingmen](../../../player/wingmen/README.md)).
There is **no `data.yaml`**: this is a design phase, the build (M8 part B) turns the hand tables into
one. The tables are marked draft; no number is measured.

## Briefing

Two pages, as Levels 08–14, each one screen (`BriefingLayoutTest`) with room for its image. Texts
(draft):

> **Commander Okafor:** "Conamara Station has felt the ice shaking for a week. Now we know why: the
> Vrell have set four drill rigs on the shelf and they are boring for the ocean. Forty scientists
> are in that dome. Break the rigs before they go through, then we follow the hole down."
>
> **Dr. Varga:** "A rig is a collar and four pumps in a hardened shell. Shots glance off; bombs,
> mortars and missiles get through. Aim for the bore core. And mind the cracks: when the ice
> glows, the steam is next."

*Images* (production art straight away, as Act 2; [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`), briefs only tonight:

- Okafor's page `level-22-conamara-map`: a top-down CDF map of the Conamara Chaos ice field: the
  station dome at the south, the shelf's fissure lines, four red Vrell rig symbols on the way, Jupiter
  as an inset on the corner.
- Varga's page `level-22-drill-rig`: her scan of a Drill Rig (the collar, the four pumps, the bore core
  marked) with an inset of a crack glowing before a geyser.

*Hangar teaser* (shop screen before L22; Rook, not voiced):

> **Rook:** "Last open sky for a while, Lancer. Bombs for the rigs, I'd say."

The hangar before L22 sells the act's first gear: the Harpoon Torpedoes, the Sonar Pulse (one free
charge) and the Bulwark heavy drone (see the [specials](../../../player/specials/README.md#sonar-pulse-l22),
[weapons](../../../player/weapons/harpoon-torpedoes/README.md) and
[wingmen](../../../player/wingmen/README.md#heavy-drone-bulwark-escort-alternative-l22)). All three are idle on
this dry level: the Harpoon and the Sonar Pulse read `NO WATER`; Level 22 is the one to fly with the old
loadout plus an `anti-ground` gun.

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `air` (Stinger, Needler, Skimmer, Skitter and Mote streams, Mantis pincers), `ground` (Drill Rigs, Spine Turret nests, Polyp Mortar floes) |
| Attack directions | front 97 % · sides 3 % |
| Density | 3 |
| Recommended traits | `anti-ground`, `forward` |
| Hazards | Ice geysers (cracks that glow, then burst); the rigs' ice-shard rings |
| Boss / mid-boss | none |
| Sensor-suite detail | none: Europa, ice shelf, `ground` + `air`, front · L1: + directions (sides), density 3, hazard "ice geysers", the objective `REACH CONAMARA` and the secondary `RIGS DOWN` · L2: + Drill Rig (new, hardened), Skimmer, Stinger, Needler, Mantis, Skitter and Mote portraits · L3: + `anti-ground` and `forward` highlighted, the wave strip marking the four rig sites |

*Varga's intel line* per sensor-suite level (they must fit `IntelPanelLayoutTest`, two lines beside the rows):

| Sensor suite | Line |
|---|---|
| none | "Europa's ice shelf, Lancer. Four rigs are boring for the ocean." |
| L1 | "Mostly ahead. Cracks glow before the steam bursts." |
| L2 | "New: a hardened drill rig. Aim for the glowing core." |
| L3 | "Bombs and missiles for the rigs, forward guns for the rest." |

## Objective

- **Primary** `reach-end`: reach Conamara Station. The rigs never fail the mission: a rig that is
  left standing keeps boring and the next level starts at its shaft (the story's consequence, not a
  fail state).
- **Secondary** `destroy-targets` (counted, as Level 19's grey tanks): destroy at least **3 of the 4
  rig cores** before the rig leaves the screen (the data's `escapes: drill-rig` with `allowed: 1`, the HUD
  counter `RIGS`), met as soon as the third core falls (the counter turns `DONE`) or at the level's
  end, failed at once by the second rig away. Pays **100** credits in Act 1 terms (410 in Act 4, the act
  factor 4.096, 1.6³); the typical haul counts 50 %. It rewards an `anti-ground` gun: a loadout without
  one can still win the level (the rigs only glance its shots) but cannot meet the secondary.
  The cores are the whole objective: the pumps are optional bounty and slow the collar 0.5 s each.
- *Pending the user's review*: the roster calls the rigs "the objective". We made them the secondary
  so the opener cannot be failed by a missing bomb rack, as Act 3's openers could not.

## Layout

Scroll **120 px/s** on the shelf approach and the Conamara run-in (110 px/s there), **60 px/s** in the rig field (a rig is on the screen about 11 s, a
hardened core needs time under an `anti-ground` gun). The setting is [Europa](../../../world/europa/README.md)'s
ice surface: a white-blue plain of ridges, fissures and open leads (cracks of black water, scenery;
the Sonar Pulse stays `NO WATER`), Jupiter huge in the deep layer, hard sunlight from the top left,
Vrell teal growth round the rigs. Motion budget: the rigs' spinning collars and the geysers' steam are the strong elements; the ice stays still. The
data eases each speed change over 2 s (the sections' `ramp`).

| Section | t (s) | Scroll (px, approx.) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Shelf | 0–36 | 0–4,320 | 120 | clear | `deep`: Jupiter and the shelf far below. `ground`: fissured ice, ridges, an open lead; a Spine Turret nest on a ridge (t≈20); a first geyser row at t≈12 that flares alone. `high-air`: ice crystals. | Popcorn from t=5; the first geysers; no rig yet. |
| 2. The Rig Field | 36–104 | 4,320–8,400 | 60 | clear | `ground`: four **Drill Rigs** (t=40, 56, 72, 88, x 130, 340, 150, 330), each with its nest of 4–5 Spine Turrets and, round the last, a Polyp Mortar floe (t≈100); steaming cracks where the collars bore; the secret's crevasse at t≈70. | The rigs' quiet windows, the hardened target, the geysers between rigs. |
| 3. Conamara Run-in | 104–150 | 8,400–13,460 | 110 | clear | `ground`: the ice-breaker road to **Conamara Station** (the research dome, a landing pad, sodium lamps; scenery), a cordon of Spine Turrets (t≈125); the geysers tremble faster as the dome nears. | The run-in, the faster geysers; level end as the dome reaches the bottom edge. |

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with
a `bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

- **Act HP factor** ([balancing basis](../../../enemies/README.md#balancing-basis)) from the proposed
  reference DPS 96 at L22 (the Act 4 hand plan, [generator](../../../player/generator/README.md#act-4-hand-plan-draft)):
  **Mantis** (L06, 52) ×1.85 → 48 HP; the **Needler** and **Stinger** take the factor capped at ×3
  (`small_act_hp`): 12 and 18 HP; the other `small` units take the beam-era ×2 only: **Skimmer** 10,
  **Spine Turret** 16, **Polyp Mortar** 20; the Skitter and the Mote are `tiny` and keep 1 HP. The
  **Drill Rig** is new here (core 240, pump 24, 2.5 s of the reference).
- **Density and pacing** (§1.7: drafted to the Act 2+ floor from the start): **214 enemies**
  (waves 184 + ground targets 30) over the 150 s from the launch, **≈ 86 a minute** at medium
  (the breather's band is ≥ 48; the Act 3 levels ran 69–119); a wave about every 3 s from t=5, in
  small waves so the rigs read against less noise; the Mantis pincers at t=61, 82 and 121, warned 3 s
  at their edge (`WarningEdge`); front 97 % by count. The floor comes from firing groups on the screen:
  Stinger streams, Needler lines and Skimmer streams plus the **rigs' rings** (a ring of 10 every 3.5 s
  a rig) and the turret nests; expected to meet ≤ 3 pauses over 2 s (`PacingTest`), ≥ 2.5 enemies on
  the screen and ≥ 10 shots a minute (`ActFloorTest`), **to be confirmed with `levelReport`** in part B.
  The new rig gets a **quiet window**:
  no other wave enters t=39.5–43.5 (the rig and its nest only).
- **Rig sites** are ground targets (*Ground targets*); none of the waves hooks them.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 5 | 1 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) |  |
| 8 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 11 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) |  |
| 14 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |
| 17 | 1 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) |  |
| 20 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | The leader drops an overdrive |
| 23 | 1 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) | Out of the first open lead on the shelf |
| 26 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 29 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) |  |
| 32 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |
| 35 | 1 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) |  |
| 38 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 43.5 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | Over the first rig; the rig's quiet window (t=39.5-43.5) is over |
| 44 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) |  |
| 47 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 50 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) |  |
| 53 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |
| 56 | 2 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) |  |
| 59 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 61 | 2 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | One per edge, hover 6 s; warned 3 s ahead |
| 65 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 68 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) |  |
| 71 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |
| 74 | 2 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) |  |
| 77 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 80 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) |  |
| 82 | 2 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | One per edge, hover 6 s; warned 3 s ahead; Rook's flank bark |
| 86 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) |  |
| 89 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |
| 92 | 2 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) |  |
| 95 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 98 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) |  |
| 101 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 104 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) |  |
| 107 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |
| 109 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The run-in to Conamara; the geysers at their fastest |
| 110 | 3 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) |  |
| 113 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 116 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) |  |
| 119 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 121 | 3 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | One per edge, hover 6 s; warned 3 s ahead |
| 125 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |
| 128 | 3 | stream | [Skimmer](../../../enemies/naval/skimmer/README.md) | 3 | front (alternating edges) |  |
| 131 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 134 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) |  |
| 137 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 140 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) |  |
| 143 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front |  |

Totals: Mote Swarm 56 · Stinger 49 · Skitter 28 · Skimmer 24 · Needler 21 · Mantis 6.

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 1 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on a ridge (t≈20) | Bounty 12 each |
| 2 | **[Drill Rig](../../../enemies/ground/drill-rig/README.md)** ×4 (t=40, 56, 72, 88; x 130, 340, 150, 330): core 240 HP, 4 pumps of 24, hardened; each with a nest of 4 Spine Turrets (the last 5, one more on the ridge) | The secondary counts the cores; core 40, pumps 4 each; spine turrets bounty 12 |
| 2 | Polyp Mortar floe ×3 beside the last rig (t≈100) | Bounty 15 each |
| 2 | Core-sample cache under a crevasse lip (`ground` trigger, 20 HP, t≈70, x 440) | The secret: a crate worth 100 (410) |
| 3 | Spine Turret cordon ×3 outside Conamara (t≈125) | Bounty 12 each |

A destroyed rig leaves a steaming crack and a stilled collar (the Drill Rig's death). The mix has 23
Spine Turrets (3 + 4 + 4 + 4 + 5 + 3; the data sets the exact nests), 3 Polyp Mortars and the 4 rigs: 30 ground enemies in all.

## Hazards

- **Ice geysers** (the data's `vents`; Level 19's furnace vents are the model, `FurnaceVents` on the ground): a crack
  darkens and glows lime-white for a **1.2 s tell** (a hiss and a rising crack sound), then a steam
  column **40 px** across stands for **0.8 s**. It hits the ship and Rook whatever their layer for
  `heavy` **10**, once per burst; enemies are unharmed and bullets pass. Seven rows of 3 cracks at
  t≈12 (alone), 30, 52, 66, 84, 107, 118 and 132; a row bursts as a ripple (0.4 s a crack, from alternate ends),
  every **4.0 s** in sections 1–2 and **3.2 s** in section 3 (Conamara's tremors: Varga and the station say so). A gap is
  always open.
- **Rig rings**: a ring of 10 ice shards every 3.5 s (`medium` = 6) while the pumps live.
- No terrain collision; the rigs and the station are flown over.

## Secrets and pickups

- **Core-sample cache** (t≈70, x 440, under a crevasse lip beside the third rig): a `ground` trigger
  of **20 HP**; destroyed, the lip falls and the cache pops out as a hidden crate worth **100** in
  Act 1 terms (410 in Act 4). The Salvage scanner's glint marks it. Brandt's line: "*That's our deep
  core. Three years of ice. Keep it, you earned it.*" (an event line, dropped after 6 s if the radio is busy).
- **Overdrive**: dropped by the t=20 Stinger leader.
- **Armour patch**: a Conamara ice-crawler (a tracked survey vehicle, the CDF supply drone's role) releases one at (240, 380 y-up) at t=100.
- **Shield cell**: dropped by the second Mantis pair (t=82).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), retimed to the HUD's **1 s rule** (a timed line
holds the radio for the longer of its voice and its text; no timed line more than 1 s late): the times
below leave at least 6 s between lines and the build's `RadioTimelineLevel22Test` checks them with and
without Rook. Event lines drop after 6 s. Rook's lines carry `requires: escort`; his one dry joke is at t=8.

**The station's voice: Dr. Ilse Brandt**, Conamara Station's director, Varga's former colleague from
the Helix years (the story's L22 beat); dry, tired, precise, a woman's voice, new in Act 4 and kept as the
Conamara voice for L22–L23 ([Act 4 voice](../README.md)). Her radio portrait: the station's
sodium-lit dome control room. Voice cast pending the audition step.

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Generic colonial (Dr. Brandt, Conamara) | "Aegis flight, Conamara Station. Four rigs on the shelf, forty of us in the dome, and the ice will not hold still." |
| t=8 | Rook | "Copy, Conamara. Last open sky for a while, Lancer. Enjoy it." |
| First geyser row flares (`first-geyser`, t≈12) | Varga | "Cracks glow, then steam. Do not sit on them." |
| t=29 | Varga | "Drill rig ahead. Hardened shell: bombs and missiles get through. Break the bore core." |
| First rig core destroyed (`target-destroyed`) | Generic colonial (Dr. Brandt, Conamara) | "That's one collar stopped and the shaking eased. Three to go." |
| t=66 | Varga | "Ilse? It's Elena. Hold on, we are coming." |
| t=82 | Rook | (Rook's flank bark) "Mantis, both flanks!" |
| t=100 | Generic colonial (Dr. Brandt, Conamara) | "Something went through under the north dome. There is water in the shaft at Kilo-Seven. It leads down." |
| Secondary met (`secondary-objective`) | Generic colonial (Dr. Brandt, Conamara) | "Three rigs are down. The shelf is quiet. Thank you, Aegis." |
| t=135 | Okafor | "Conamara is in sight. We refit for the water tonight and we take that shaft in the morning." |
| Level end | Okafor | "Conamara is safe, for now. Refit, Aegis. Tomorrow we go under the ice." |

The secret's line (the cache freed, an event line): Brandt, "That's our deep core. Three years of ice. Keep it, you earned it." Varga's t=66 line is
her personal beat of the act (her former colleague; Level 23 continues it); Okafor does not reply.

## Music & ambience

The Act 4 A theme **"Thera Deep"** (track 10 in the [track list](../../../audio/music/README.md#track-list),
chosen in round 02; its production stems come with part A): the Europa ice-surface ambience alone in
section 1 (wind, a distant ice groan, the station's hum at the end), the **base stem from section 2** (the
first rig), the **full mix from section 3** (the run-in to Conamara). Round 48 Q12 a: no
bubble layer in the full-length theme. New sounds this level needs, listed for the act's sfx step
([sfx](../../../audio/sfx/README.md)): the geyser's crack and burst (heavy, no hiss-pops), the Drill
Rig's collar (a low drill whine, its ring), its core's death, the Conamara station hum and the ice
ambience bed; sounds that imitate real things come from Freesound only (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(22) = 700 × 1.07²¹ ≈ **2,898**, the typical haul's target
([economy](../../../systems/economy/README.md#per-level-budget)). The data keeps **Act 1 terms**
(Skitter 5, Mote 2, Needler 12, Stinger 15, Skimmer 14, Mantis 30, Drill Rig 56 whole, Spine Turret 12,
Polyp Mortar 15, the crate 100, the secondary 100); the code pays each × the act factor **4.096**
(1.6³), bounties also × the scale. A **`bounty_scale` of 0.38** (the model of the earlier Act 3
levels: typical = 0.663 of the kills, 0.8 of the ground targets, half of the secret and secondary; a first
estimate for part B to confirm with `TypicalHaulTest` and `levelReport`) puts the typical haul at
**2,899 (+0.0 %)**, the perfect run at 4,399. No boss or mid-boss.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Mote Swarm 56 × 2 + Stinger 49 × 15 + Skitter 28 × 5 + Skimmer 24 × 14 + Needler 21 × 12 + Mantis 6 × 30 (Eel Swarm whole, 48) | 2,732 | 1,811 |
| Ground targets: Drill Rig 4 × 56 (core 40 + pumps 4 × 4) + Spine Turret 23 × 12 + Polyp Mortar 3 × 15 | 848 | 679 |
| Secret: core-sample cache (hidden crate, 7 % of the typical haul) | 410 | 205 |
| Secondary: drill rigs down (3 of 4 cores) | 410 | 205 |
| **Total** (bounty scale 0.38) | **4,399** | **2,899** |
| Budget(n) = the typical haul's target; typical +0.0 %, perfect 1.52 × budget | | 2,898 |

**Balance plan**: the plan's L22 visit (2 855 cash, Harpoon Torpedoes 2 000 and L2 500) buys gear that is idle here
([generator](../../../player/generator/README.md#act-4-hand-plan-draft)); the level expects `anti-ground`
and `forward` and is flyable without either (without `anti-ground` the secondary fails).

## Difficulty notes

- **Easy**: the rigs' shards every 4.5 s (the Drill Rig's easy hook), the Mantis pincers have 1 each;
  the secondary needs 2 of 4 cores; geysers cycle every 5 s.
- **Hard**: a second `aimed` shard with every ring; the secondary needs all 4 cores; geysers cycle every 3.2 s
  (2.6 s in section 3); the other waves by the formation lever, trimmed by hand on the Stinger streams for
  at most ~3.5 enemies on the screen.

## Concept art

No art yet. Round 48 proposals: the ice surface mood still (step A-europa) and a Drill Rig sheet if the
main agent adds one; production backdrop, rig sprites and briefing images come in part B. The mood still
and the under-water look are in [europa](../../../world/europa/README.md#concept-art).

## Implementation

- [ ] Level data `data.yaml` (M8 part B): sections and speeds with 2 s eases, waves, ground targets,
      geyser rows, radio, budget, threat profile, briefing, backdrop, music
- [ ] The four Drill Rigs as hardened ground targets with a vital core and four pumps
      ([Drill Rig](../../../enemies/ground/drill-rig/README.md)); the secondary `destroy-targets` (3 of 4 cores, `RIGS` counter)
- [ ] The ice geysers (`FurnaceVents` on the ground, 1.2 s tell, `heavy` 10 once per burst, cadence faster in section 3)
- [ ] The secret (core-sample cache, 20 HP trigger, crate 100), the Conamara ice-crawler's armour patch, the overdrive and shield-cell drops
- [ ] Wave script: ≥ 48 enemies a minute at medium (`DensityTest`), no empty screen over 2 s (`PacingTest`), the Act 2+ floor (`ActFloorTest`), the rig's quiet window, edge warnings for the Mantis pincers
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel22Test`, with Rook and without), Dr. Brandt cast; Rook's lines `requires: escort`
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`); the Drill Rig's portrait
- [ ] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(22) = 2 898 with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music and ambience: the Act 4 A theme's stems by section; the ice-surface bed; the geyser and rig sounds (`Level22SoundsTest`)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on every difficulty (`--escort none` too)
- [ ] The hangar before L22 sells the Harpoon Torpedoes, the Sonar Pulse (1 free charge) and the Bulwark, each reading `NO WATER` / idle here
- [ ] Production art and captures for part B's round

## Open questions

- Is the secondary "3 of 4 rig cores" right, or should the cores be the primary, as the roster says?
  (Our reading: secondary, so an `anti-ground`-less loadout can still finish the opener.)
- Are the open leads ever water for the Sonar Pulse? (Our reading: no, scenery only, so the Pulse reads `NO WATER` until L23's waterline.)

## Decisions

- 2026-10-21 (Act 4 design phase, step `m8-dl2224`; **all decided in round 48 (2026-10-22)**): promoted from the act roster to a
  draft level document, hand tables only, no `data.yaml`. The rigs are the secondary (3 of 4 cores), the geysers are
  `FurnaceVents` on the ground, the level is dry (the hydro-kit and the water start in L23), Dr. Ilse Brandt is the
  new Conamara voice (Varga's former colleague), numbers from the proposed reference DPS 96.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
