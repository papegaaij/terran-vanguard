---
title: Level 19 – Foundry of Tharsis
design: approved
implementation: in-progress
art: final
depends-on: [../../../enemies/bosses/revenant-walker, ../../../enemies/ground/warden-tank, ../../../enemies/ground/spine-turret, ../../../enemies/air/dust-kite, ../../../enemies/air/ghost-drone, ../../../story/twist, ../../../player/specials, ../../../world/mars]
updated: 2026-10-20
---

# Level 19 – Foundry of Tharsis

## Summary

The Tharsis Foundry, the colony Helix Dynamics money built on the Tharsis plateau, has gone quiet:
the Vrell hold its lower works together with grey, unmarked machines, and two hundred crew are
sealed in the smelter tower. Aegis Wing clears the works from the slag fields to the casting yard:
[Warden Tanks](../../../enemies/ground/warden-tank/README.md) ride the conveyors, furnace vents flare
from the floor and gantry turrets fire **after the ship has passed**, so the level asks for `rear`
and `anti-ground`. In the casting yard the scroll halts for the mid-boss, the
[Revenant Walker](../../../enemies/bosses/revenant-walker/README.md); its wreck, serial plates filed
off, is **hint 2** of the [twist](../../../story/twist/README.md), shown in a slow crawl past it
(M15 = a). About 172 s of level clock plus a ≈ 60 s fight; ≈ 88 enemies a minute at the Act 2+ floor,
tougher rather than more (the beam era's HP).

**Status: approved in concept round 36 (user, 2026-10-13).** Written in the overnight design
phase (2026-10-12) from the act roster and the gap analysis's recommended answers, all kept or
accepted in the round. M7 part F (step F2, 2026-10-19) moved the numbers into the level's
[data.yaml](data.yaml): the tables below are rendered from it, the waves re-planned to the Act 2+ floor
(the gap check's default, see *Waves*), pending the user's review in round 42.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (approved in round 36):

<!-- data: briefing -->
> **Commander Okafor:** "Tharsis Foundry went quiet two days ago. The Vrell hold the lower
> works, and those grey machines are with them. Two hundred crew are sealed in the smelter
> tower. Get them out."
>
> **Dr. Varga:** "Grey tanks ride the conveyors, and the gantry turrets face up the line: they
> fire after you pass. Rear guns, Lancer. And something large is in the casting yard."
<!-- /data -->

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`):

- Okafor's page `level-19-tharsis-works`: a CDF site plan of the foundry: the slag fields, the
  two conveyor halls, the furnace row, the casting yard and the south gate; the smelter tower
  marked with the crew count; red Vrell vectors and grey, unlabelled contacts on the conveyor lines.
- Varga's page `level-19-rear-arcs`: her diagram of a gantry turret's firing arc pointing up the
  line (the ship passing under it, then caught from behind) and a Warden Tank's turret tracking
  backwards, its thin rear plate circled; a large blurred thermal blob in the yard in the corner.

*Hangar teaser* (shop screen before L19; Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "A foundry full of grey tanks. Bring something that shoots backwards."
<!-- /data -->

## Threat profile

Rendered from the data; directions are counted by each wave's `from` (the intel's rule).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `ground` (Warden Tanks, Creepers, Ravagers, Shellbacks, turret and mortar nests, the walker), `air` (Needler and Stinger groups, popcorn, Ghost Drones, Wraiths, a Choir Herald) |
| Attack directions | front 94% · rear 6% |
| Density | 4 |
| Recommended traits | `anti-ground`, `rear` |
| Hazards | Furnace vents flaring from the floor; conveyors carrying ground units; turrets that fire after the ship passes |
| Boss / mid-boss | Mid-boss: Revenant Walker (unnamed before the fight) |
| Sensor-suite detail | none: Tharsis industrial zone, `ground` + `air`, front · L1: + directions (front, rear), density 4, hazards "furnace vents" and "rear-firing turrets", the objective `CLEAR THE FOUNDRY` and the secondary `GREY TANKS 12/14` · L2: + Warden Tank (new), Dust Kite, Skitter, Needler, Stinger, Ghost Drone, Wraith, Creeper, Ravager, Shellback, Choir Herald portraits, and a silhouette "unclassified, huge, `ground`" (the walker) · L3: + `anti-ground` and `rear` highlighted, the wave strip marking the casting-yard halt, 1 secret |
<!-- /data -->

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Tharsis Foundry. Vrell and grey machines together." |
| L1 | "Front and rear. Turrets that fire after you pass." |
| L2 | "New: grey tanks. And one huge contact, unknown." |
| L3 | "Rear guns for the turrets. Anti-ground for tanks." |

## Objective

- **Primary** `reach-end`: clear the works to the south gate. The walker blocks the casting yard:
  the scroll stays halted until it dies (as Level 11's Kraken).
- **Secondary** `destroy-targets` (counted): destroy at least **12 of the 14** grey Warden Tanks
  before they leave the screen (the data's `escapes: warden-tank` with `allowed: 2`, the HUD counter
  `GREY TANKS`), met as soon as it can no longer fail (the twelfth tank destroyed; the HUD counter turns `DONE` then; round 42, pending the user's review) or at the level's end, failed at
  once by the third away. Pays
  **100** credits in Act 1 terms (256 in Act 3); the typical haul counts 50 %. The tanks ride the
  conveyors past the ship, so the secondary rewards a `rear` gun or `anti-ground` fire before they
  pass. The counter shows on the HUD from the first tank (as Level 09's targets).

## Layout

Scroll **150 px/s** on the open slag fields and the run-out, **130 px/s** in the halls and the
furnace row, **120 px/s** on the yard approach (the tanks and the turrets need time to pass). The
setting is [Mars](../../../world/mars/README.md)'s Tharsis Foundry: a rust plateau under a
butterscotch sky, black slag heaps, roofless steel halls, smelter stacks with orange glow, the
smelter tower at the left edge (drawn from above: the ground is opaque, pending round 42). Motion budget:
the conveyors' moving plates and the furnace glow are the two strong elements; smoke stays at half
particle speed. The data eases each speed change over 2 s (the sections' `ramp`); the scroll column
includes the eases.

**Timeline.** The launch takes the first 5 s. Section 5 is the **arena** (`arena: true`,
`speed: 0`), t = 132–133: the scroll eases to a halt over its first second in the casting yard, and
the level clock halts at its end until the walker dies. Section 6 is the **wreck crawl** (M15 = a,
the walker's spec): the arena's `hold_after_boss: 1` keeps the clock halted 1 s after the 2 s death
chain, then it resumes at t = 133 and the scroll eases up to 20 px/s over 2 s (the crawl section's
`ramp`), crawls 10 s and eases up to 150 px/s from t = 145 (section 7's ramp). About **232 s** with a
60 s fight.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Slag Fields | 0–24 | 0–3,600 | 150 | light | `ground`: slag heaps, the smelter tower at the left edge (from above), an ore gantry across the field (t≈18) with a turret nest under it. `low-air`: stack smoke (~15 %). | Popcorn and light groups from t=6; the first up-facing turret nest taught. |
| 2. Conveyor Halls | 24–64 | 3,600–8,820 | 130 | light | `ground`: two roofless halls, conveyor belts at x 140 and 340 running up the screen, gantries over them; the shift office at the left wall (the secret, t≈52). | Warden Tanks alone first (t=30.5), then on both belts. |
| 3. Furnace Row | 64–104 | 8,820–14,020 | 130 | medium | `ground`: rows of furnace vent grates across the floor between smelter stacks, one belt at x 240 between the rows, slag pools glowing orange. `low-air`: heat shimmer above the grates. | The vents introduced alone (t≈64.5), then with waves; the Choir Herald; Shellbacks on the furnace road. |
| 4. Yard Approach | 104–132 | 14,020–17,390 | 120 | light | `ground`: the casting yard's two feed belts (x 100 and 380), two vent rows, the yard gate with a turret gantry (t≈115); the yard's slab floor enters at the top at the end. | Mixed waves; the last tanks before the yard. |
| 5. Casting Yard | 132–133 | 17,390–17,390 | 0 | light | `ground`: the casting yard filling the field: cast slabs, cooling moulds, molten channels (scenery, no damage), the vents cold. | The mid-boss (≈ 60 s at medium); the scroll halted while it lives. |
| 6. The Wreck | 133–145 | 17,390–17,610 | 20 | light | `ground`: as section 5; the walker's **wreck** smoking where it fell, the filed plate turned up. | Hint 2: the crawl past the wreck with Varga's line and Okafor's cut. |
| 7. South Gate | 145–172 | 17,610–21,530 | 150 | clear | `ground`: the outbound belt, ingot stacks, the south gate and the plateau beyond; the crews' rovers leaving the tower (t≈160, scenery). | Release; level end. |
<!-- /data -->

### Conveyors

- Belts on the `ground` layer, 48 px wide, drawn with moving plates and rollers; they run **up the
  screen** at **40 px/s** relative to the ground (the foundry's feed lines run to the casting yard).
  The data's `belts` give each run's x and the level times its bottom end enters at the top edge and
  its top end leaves at the bottom edge, where the backdrop's belt pieces lie (ART-F2's proposal): the
  halls at x 140 and 340 (t 26.8–67.9), the furnace row at x 240 (66.8–108.1), the yard's feed belts at
  x 100 and 380 (107.7–147.7, under the halt) and the outbound belt at x 240 (147.5–173.5).
- Ground units on a belt are carried by it: a Warden Tank driving up a belt at its own 40 px/s moves
  down the screen at 130 − 40 − 40 = **50 px/s** in section 2, so it stays on the screen ≈ 12 s and
  its turret keeps firing back up at the ship after it has passed. Driving up the belt it faces up the
  screen, so its thin **rear plate faces the ship while it approaches** (the front guns meet it), and
  its turret tracks back; the briefing's `level-19-rear-arcs` shows it so. Wrecks and husks on a belt
  ride it off the screen. No nest stands on a belt (`FoundryChecks`).
- Belts do not touch the ship, Rook, pickups or bullets (they fly above).

### Up-facing turret nests ("turrets fire from behind as you pass")

The roster's rear-firing turrets are [Spine Turrets](../../../enemies/ground/spine-turret/README.md)
placed with their silence arc turned **180°** (a ground target's `arc: up`, new in part F; the
art is unchanged, the barrel already turns 32 angles): a nest is **silent while it is above the
ship** and opens fire up the screen **once it has scrolled past** (the ship more than 100° off its
facing until then). Rear guns kill them after the pass; `anti-ground` fire (bombs, mortars) before
it. Each nest hangs under a gantry so the arc reads as "pointing back up the line".

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*).

- **Act HP factor** from the reference DPS 92 at L19 (M18 = a, round 36):
  **Creeper** (L08, 60) ×1.53 → 46 HP, **Ravager** (L09, 63) ×1.46 → 23 HP, **Wraith** (L10, 66)
  ×1.39 → 22 HP, **Choir Herald** (L17, 84) ×1.10 → 66 HP, **Shellback** (L17, 84) ×1.10 → body 99,
  vent 18 (round 40 K.a's 90 / 16; the whole bounty 48 kept). Level 19 joins the basis's `small_act_hp`
  (the Needler 12, the Stinger 18; the gap check's default, pending round 42).
- **Beam-era HP** (the user's "tougher, not more" of 2026-10-19; `beam_era_hp` ×2 on `small` units from
  Level 18): the **Needler 24**, the **Stinger 36**, the **Ghost Drone 28**, the **Spine Turret 16** and the
  **Polyp Mortar 20**; the Dust Kite and the Skitter (`tiny`) keep 1 HP; the Warden Tank (new here,
  `medium`) keeps 64 + 20.
- **Density and pacing** (the Act 2+ rules: `PacingTest`'s Act 2 rule and `ActFloorTest`'s 2.5 enemies on
  the screen and 10 enemy shots a minute with the plan's fit at medium; the gap check's default, met by
  density rather than by an exception, pending round 42): **297 enemies** at medium (waves 260 + ground
  targets 37; step F2 built 243, measured by step F4 at 1.67 on the screen and 7.3 shots a minute) over the
  166 s of scroll from the launch, the arena excluded: **≈ 107 a minute** (Level 18's 101). The approved table's anchor
  waves keep their times (the Warden Tanks at 30.5/41/76/107.5 and the south gate's pair at 149, the
  Creepers at 55/125, the Herald at 69, the Shellbacks at 83, the Ravagers at 93.5, the Ghost Drones at
  58.5/90/159.5, the rear entries at 23.5/48/79.5/100.5/118/156) and the radio beats; between them a wave
  about every 2–3.5 s of light firing groups (Needler line abreasts and Stinger V-wings and streams of 4,
  as Level 18's; 3 where the furnace row and the yard approach crowd) where the approved table flew short
  popcorn streams, Dust Kite swarms of 4 that stay on the screen,
  and more up-facing spine nests under the gantries (t≈36, 70, 88, 110, 152). Hard: the firing groups one
  unit smaller (as Levels 17–18), its extra nest and Wraith pair; easy thinned by the formation lever.
  **Measured (step F4, three passes, pending round 42)**: the groups of 3 became 4 (hard 3), five popcorn
  streams became firing groups (t=11, 51.5 kept; 84.5, 104, 109 reverted in pass 2, the furnace row and the
  yard approach reached 9–10 on the screen), firing groups fill the gaps at t=44, 78, 99, 123.5, 147.5, 158 and
  the run-out at 167.5 and 169.5, and a Polyp Mortar pair by the hall walls (t=31.5) holds the tanks' quiet
  window (the plan's fit kills a tank in ≈ 1.5 s). With the plan's fit at medium: **2.83 on the screen,
  27.8 shots a minute**, one pause over 2 s (the walker's death chain and hold, 3.8 s real); hard 3.31, one
  pause; easy 1.28, one pause over 3 s; a killed small unit lives 1.24× Level 10's (`BeamEraHpTest`).
- **Quiet windows**: no other wave for 3.5 s after the first Warden Tanks (t=30.5) and after the first
  vent row (t≈64.5, no wave on it; the mortar pair by the stacks holds the screen); the crawl carries only
  Dust Kite swarms of 4 at the field's edges (one about every 2.5 s), so the wreck in the middle reads.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (left) | |
| 8.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 11 | 1 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (right) | |
| 13.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Varga's line on the gantry nests at t=14 |
| 16 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Over the ore gantry's up-facing nest (t≈18) |
| 18.5 | 1 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | |
| 21 | 1 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (alternating edges) | |
| 23.5 | 1 | line abreast | [Skitter](../../../enemies/air/skitter/README.md) | 3 | rear | The first rear entry, warned 3 s ahead |
| 26 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 28 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Gone before the tanks' quiet window |
| 30.5 | 2 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front | **New.** Alone until t=34 on the belt at x 140; Varga's line at t=30 |
| 34 | 2 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | |
| 36 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Over the first conveyor gantry's nest (t≈36) |
| 39.5 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 41 | 2 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front | 2 on the belt at x 140, then 1 on x 340 1.5 s later |
| 42.5 | 2 | single | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 1 | front | |
| 44 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Step F4: the floor (fills 42.5-46) |
| 46 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Over the slag pit's mortars |
| 48 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Cloaked; warned 3 s ahead (Rook's bark at t=45.5); act HP factor |
| 51.5 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Past the shift office (the secret) |
| 53 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 55 | 2 | convoy | [Creeper](../../../enemies/ground/creeper/README.md) | 3 | front | On the hall floor road between the belts; act HP factor |
| 56.5 | 2 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | |
| 58.5 | 2 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | Over the second conveyor gantry's nest (t≈60); Ares Control's line at t=57.5 |
| 62 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | Gone before the vents' quiet window |
| 65.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | After the first vent row's flare (t≈64.5) |
| 69 | 3 | carrier + escorts | [Choir Herald](../../../enemies/air/choir-herald/README.md) + [Needler](../../../enemies/air/needler/README.md) | 1 + 4 | front | The escorts in `orbit`, ringed; act HP factor; Varga's line at t=66 |
| 71.5 | 3 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | |
| 73.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 76 | 3 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 3 | front | Between the vent rows |
| 78 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step F4: the floor |
| 79.5 | 3 | line abreast | [Skitter](../../../enemies/air/skitter/README.md) | 3 | rear | Warned 3 s ahead (Rook's bark at t=77) |
| 81.5 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (alternating edges) | |
| 83 | 3 | convoy | [Shellback](../../../enemies/ground/shellback/README.md) | 2 | front | On the furnace road; act HP factor |
| 84.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Over the Shellbacks |
| 88.5 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Over the furnace gantry's nest (t≈88); Okafor's line at t=88 |
| 90 | 3 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 4 | front | |
| 92 | 3 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | |
| 93.5 | 3 | pack | [Ravager](../../../enemies/ground/ravager/README.md) | 4 | front | Round the slag pool's mortars; act HP factor |
| 97 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | The leader drops an overdrive; Rook's bark at t=97.5 |
| 99 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Step F4: the floor |
| 100.5 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Warned 3 s ahead |
| 104 | 4 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | Ares Control's line at t=105 |
| 105.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 107.5 | 4 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front | 2 + 2 on the feed belts |
| 107.5 | 4 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front | |
| 109 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (left) | Beside the feed belts |
| 111.5 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 114.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Over the yard gate's nest (t≈115) |
| 118 | 4 | line abreast | [Skitter](../../../enemies/air/skitter/README.md) | 3 | rear | Warned 3 s ahead |
| 120 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 121.5 | 4 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | The supply drone's armour patch at t=122 |
| 123.5 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step F4: the floor |
| 125 | 4 | convoy | [Creeper](../../../enemies/ground/creeper/README.md) | 3 | front | Act HP factor |
| 126.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 128.5 | 4 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | Gone before the halt |
| 130 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Hard only: an extra Wraith pair before the yard, warned |
| 132 | 5 | mid-boss | [Revenant Walker](../../../enemies/bosses/revenant-walker/README.md) | 1 | top (its intro pass) | At the halt (the bar, the mini-boss sting, the boss checkpoint); its passes from every edge per its spec, with their edge warnings; no other waves in the arena |
| 133.5 | 6 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | The crawl: kite swarms along the edges only |
| 136 | 6 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | |
| 138.5 | 6 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | Varga's hint line at t=137 |
| 141 | 6 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | |
| 143.5 | 6 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | The scroll picks up at t=145 |
| 145.5 | 7 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 147.5 | 7 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step F4: the floor |
| 149 | 7 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front | The last two tanks |
| 151 | 7 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 153.5 | 7 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 156 | 7 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Warned 3 s ahead |
| 158 | 7 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Step F4: the floor |
| 159.5 | 7 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | |
| 161.5 | 7 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | |
| 163 | 7 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Okafor's line at t=163 |
| 166.5 | 7 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | The last wave |
| 167.5 | 7 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Step F4: the run-out |
| 169.5 | 7 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step F4: the run-out |

Totals: Dust Kite 61 · Needler 73 · Stinger 59 · Skitter 23 · Warden Tank 14 · Wraith 6 · Creeper 6 · Ghost Drone 10 · Choir Herald 1 · Shellback 2 · Ravager 4 · Revenant Walker 1.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 1 | Up-facing [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 under the ore gantry (t≈18) | Bounty 12 each; fire only after the ship has passed |
| 2 | Up-facing Spine Turret nest ×2 under the first conveyor gantry (t≈36) | Bounty |
| 2 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) pair by the hall walls (t≈31.5) | Bounty 15 each |
| 2 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) pair in the slag pits (t≈46) | Bounty 15 each |
| 2 | Foundry payroll safe in the shift office (`ground` trigger, 20 HP, t≈52, x 60) | The secret: frees a crate worth 100 (256) |
| 2 | Up-facing Spine Turret nest ×3 under a conveyor gantry (t≈60) | Bounty |
| 2 | Polyp Mortar pair by the first smelter stacks (t≈63), on the screen through the first vent row's window | Bounty |
| 3 | Up-facing Spine Turret nest ×2 under a furnace gantry (t≈70) | Bounty |
| 3 | Up-facing Spine Turret nest ×4 on the smelter stacks, 2 + 2 either side of the row (t≈80) | Bounty |
| 3 | Up-facing Spine Turret nest ×3 under a furnace gantry (t≈88) | Bounty |
| 3 | Polyp Mortar floe ×3 by the slag pool (t≈95) | Bounty |
| 3 | Up-facing Spine Turret nest ×2 at the furnace row's end (t≈100; hard +3); hard ×5 | Bounty |
| 4 | Up-facing Spine Turret nest ×2 under the feed belts' gantry (t≈110) | Bounty |
| 4 | Up-facing Spine Turret nest ×3 under the yard gate (t≈115) | Bounty |
| 7 | Up-facing Spine Turret nest ×2 by the outbound belt (t≈152) | Bounty |
| 7 | Polyp Mortar pair at the south gate (t≈155) | Bounty |
<!-- /data -->

The Warden Tanks, Creepers, Ravagers and Shellbacks are waves (they move); they are counted with
the ground units in the budget. Every Spine Turret nest of the level is up-facing (`arc: up`).

## Hazards

- **Furnace vents** (sections 3–4, seven rows of 3–4 grates at t≈64.5, 72, 80.5, 87, 96, 112.5 and
  122.5, the data's `vents`; Level 05's sleds are the model): a grate glows orange to white for a
  **1.2 s tell** (heat shimmer above it, a rising roar), then a flame column **44 px** across stands for
  **1.0 s**. It hits the ship and Rook whatever their layer for `heavy` **10**, once per burst; enemies
  are unharmed and bullets pass. A row fires as a ripple, one grate after the other 0.4 s apart from
  one end (alternating ends row by row), every **3.5 s**, so a gap is always open and a row fires at
  least once while it is on the screen. The first row (t≈64.5) flares alone before any wave reaches it.
  The vents are cold in the casting yard.
- **Conveyors**: move ground units; no contact (above).
- **The walker's flame cone** and homing missiles: its [spec](../../../enemies/bosses/revenant-walker/README.md).
- No terrain collision.

## Secrets and pickups

- **Foundry payroll safe** (the secret, t≈52, x 60, the left wall of the first hall): a squat steel
  safe in the shift office, its door lamp blinking green. A `ground` trigger of **20 HP**; destroyed,
  it pops a hidden crate worth **100** in Act 1 terms (256). The Salvage scanner's glint shows it.
  Its line, Ares Control: "*That's the foundry payroll. The crews say keep it.*" (an event line).
- **Overdrive**: dropped by the t=97 Stinger leader.
- **Armour patch**: an Ares militia supply drone (the [CDF supply drone](../../../allies/README.md#cdf-supply-drone)'s
  presentation) releases one at (240, 380 y-up) at t=122, before the yard.
- **Shield cell**: the walker drops one as its phase 3 begins (its spec).

## Radio chatter

Spoken radio, retimed to the **1 s rule** (as Levels 17–18: 5 s for the last page plus 3 s for each
page before it, a page of 3 × 22 characters, text at 30 characters a second, 0.4 s of silence; no
timed line more than 1 s late). Event lines drop after 6 s. Rook's lines carry `requires: escort`;
his one dry joke is at t=38. The colony's voice is **Ares Control** (Level 15's speaker; one colony
voice across the act, as Level 17 proposes), relaying the sealed crews; no new speaker.

The arena's lines are **events** (the level clock is halted): `first-telegraph` (the walker's first
flame tell), `boss-part-destroyed` (the first missile pod shot off) and `boss-destroyed`. The crawl's
lines are timed from t=133, when the clock resumes: Rook's `boss-destroyed` line, started at the
death, closes before t=137 (the death chain's 2 s plus 1 s, then 3.7 s of the line).

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Tharsis Foundry, Lancer. The Vrell hold the lower works, and the grey machines are with them. Clear it to the casting yard." |
| t=14 | Varga | "Gantry turrets ahead face up the line. They fire once you're past." |
| t=22.5 | Generic colonial (Ares Control) | "Two hundred foundry crew are sealed in the smelter tower." |
| t=30 | Varga | "Grey tanks on the conveyors. Their back plates are thin." |
| t=38 | Rook | "Tanks on a conveyor belt. Someone hates walking." |
| t=45.5 | Rook | "Six o'clock! Cloakers!" |
| t=57.5 | Generic colonial (Ares Control) | "Furnace row is next. The vents still fire. Watch for the glow." |
| t=66 | Varga | "A singer with escorts. Kill it and their shields go with it." |
| t=77 | Rook | "Coming up behind, Lancer!" |
| t=88 | Okafor | "Grey drones again, same as the storm. Keep your spacing." |
| t=97.5 | Rook | "Cloakers on six again!" |
| t=105 | Generic colonial (Ares Control) | "Something big walked into the casting yard an hour ago. It isn't one of ours." |
| t=116 | Rook | "Behind us!" |
| t=124.5 | Okafor | "Casting yard ahead. Whatever is in there, Lancer, put it down." |
| First flame tell (`first-telegraph`) | Varga | "It burns what's behind it. Stay out of the cone, then hit the vents." |
| First missile pod destroyed (`boss-part-destroyed`) | Rook | "Pod's gone. One more." |
| Walker destroyed (`boss-destroyed`) | Rook | "Walker's down. No colours, no numbers." |
| t=137 | Varga | "Lancer, the serial plates are filed off. By hand. And that alloy is Helix foundry stock." |
| t=148.5 | Okafor | "Not on this channel, Doctor." |
| t=155.5 | Generic colonial (Ares Control) | "Tower to Aegis: the yard is clear. We're coming out." |
| t=163 | Okafor | "South gate ahead. Last of them, Lancer." |
| Secondary met (at the end) | Okafor | "Not one grey tank left the foundry. Good." |
| Level end | Okafor | "Tharsis is breathing again. Come home, Aegis." |
<!-- /data -->

Timeline check (text): the longest timed lines close at ≈ 13.5 (t=1), ≈ 116.0 (t=105) and ≈ 148.3
(Varga's hint line, t=137); every timed line starts on time. Rook's four rear barks fall on the
rear waves' warnings (t=45.5, 77, 97.5, 116); the t=23.5 and t=156 rear waves are warned without a
bark (the radio is busy). **Hint 2** follows M13 = a and M15 = a: in the level only Varga's
observation and Okafor's cut, which lands as the scroll picks up again (t≈145–147). Varga's line is
the spec's text shortened to two pages (the spec's "slow over the wreck" is the crawl itself).
Rook's death line is not his dry joke; he does not take up the question.

## Boss / mid-boss

Mid-boss: [Revenant Walker](../../../enemies/bosses/revenant-walker/README.md). Its passes, phases,
HP, attacks and wreck beat live in the spec. Arena notes for this level:

- The scroll eases to a halt with the casting yard filling the field (the yard's slab floor from the
  top edge to below the bottom; the molten channels run between the walker's lanes, never under the
  lane lines x 120 / 240 / 360 and y 200 / 330, so the walk reads on the slabs). The vents are cold.
- The walker's first pass is its intro from the top; the short mid-boss bar `REVENANT WALKER`, the
  **mini-boss sting** and the boss checkpoint (none on hard) at the halt, as the Kraken's. **No Choir
  static** (it is not Vrell).
- No other waves in the arena: the passes and their edge warnings are the fight (measured by step F4:
  52.7 s in the fixture, 55–57 s in the level at medium, the par 53; the walker's HP 3,740; round 43's rework,
  far more fire and a bigger flame: 57.1 s in the fixture, ≈ 61 s in the level, the par 57, HP 3,170; round 44's
  armoured pods, rail lance and stomp: 50.8 s in the fixture, 50–59 s in the level, the par 51, HP 1,350).
- **Bounty**: recomputed with Level 19's `bounty_scale` of 0.35 (below): **395** in Act 1 terms
  (hull 315, pods 40 each), paid 395 × 2.56 × 0.35 = **354 = 15.0 %** of budget(19); step F4 sets the
  final value from the measured scale (pending round 42).
- The boss warning opens 3 s before the halt (`incoming: 3`, round 41's key); the walker's first pass
  from the top needs no edge warning.
- **Hint 2** (M15 = a): the wreck stays on the `ground` layer where it falls, the filed plate turned
  up; section 6 crawls past it at 20 px/s for 10 s (the walker never stands lower than y ≈ 470, so
  the wreck stays on the screen for at least 8 s of it). The wreck's flank plate is the same render
  as Level 20's briefing image.

## Music & ambience

The Act 3 B theme (track 9 in the [track list](../../../audio/music/README.md#track-list), the
"Grey Signal", D minor dub techno, round 36's S.1 = b) with **Vorne's motif as a faint layer**, as the
[music](../../../audio/music/README.md) plan places it under the L19 walker fight: the foundry
ambience alone in section 1, the **base stem from section 2**, the **full mix from section 3**, the
**mini-boss sting** at the halt, then the full mix **with the motif layer** under the fight. Over
the crawl the music drops to the **motif layer alone** over the ambience (hint 2 in the music), and
the base stem returns in section 7 to the end. Ambience: a new `mars-foundry` bed (conveyor rumble,
a far furnace roar, a slow hammer, the plateau wind; no bubbly or popping sounds). New sounds this
level needs: the furnace vent's tell and burst (a roar, no pop), the belts' rumble, the Warden
Tank's tracks, turret whine, burst and death, the walker's sounds (its spec); listed in
[sfx](../../../audio/sfx/README.md) by the act's audio step.

## Credit budget

Budget(19) = 700 × 1.07¹⁸ ≈ **2,366** ([economy](../../../systems/economy/README.md#per-level-budget)).
Data in **Act 1 terms** (Dust Kite 4, Skitter 5, Needler 12, Stinger 15, Ghost Drone 14, Wraith 30,
Choir Herald 30, Warden Tank 26 whole, Creeper 22, Ravager 18, Shellback 48 whole, Spine Turret 12,
Polyp Mortar 15, the walker 495, the crate 100, the secondary 100), × the act factor **2.56**,
bounties × the scale (the beam era raises HP, not bounties). With the waves at the measured Act 2+ floor
(≈ 107 a minute) a **`bounty_scale` of 0.28** (step F4; round 36's 0.52 for ≈ 62 a minute, the gap
check's paper 0.35 for ≈ 88) puts the typical haul on budget (`TypicalHaulTest`: +1.4 %; the table's model
+0 %), just under M12's 0.3–0.5 expectation (pending round 42).

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Dust Kite 61 × 4 + Needler 73 × 12 + Stinger 59 × 15 + Skitter 23 × 5 + Warden Tank 14 × 28 by parts (26 whole) + Wraith 6 × 30 + Creeper 6 × 22 + Ghost Drone 10 × 14 + Choir Herald 1 × 30 + Shellback 2 × 52 by parts (48 whole) + Ravager 4 × 18 | 2,339 | 1,477 |
| Ground targets: Spine Turret 26 × 12 + Polyp Mortar 11 × 15 | 355 | 284 |
| Mid-boss: Revenant Walker (hull 395 + left pod 50 + right pod 50) | 355 | 355 |
| Secret: payroll safe (hidden crate, 4% of budget) | 256 | 128 |
| Secondary: stop 12 of the 14 grey tanks | 256 | 128 |
| **Total** (bounty scale 0.28) | **3,561** | **2,372** |
| Budget(n) = the typical haul's target; typical +0 %, perfect 1.51 × budget | | 2,366 |
<!-- /data -->

Whole-unit bounties (Warden Tank 26, Shellback 48) are used; part-first kills pay a little more.
**Balance plan**: the plan's L19 visit comes from part A's Act 3 balance plan (M18; the
[Ion Beam](../../../player/weapons/ion-beam/README.md)'s hand plan reaches forward DPS 100 here). The
level expects `rear` (a Tail Gun or the [Rear Lance](../../../player/weapons/rear-lance/README.md))
and `anti-ground`, and is flyable without either; the Shield Overcharge (unlocked at this visit) and
the EMP Burst against the machines are options, not needs.

## Difficulty notes

- **Easy**: vents every 5 s with a 1.6 s tell; the Warden Tanks' 2-shot bursts and the walker's easy
  hooks (their specs); the secondary stays 12 of 14 (the tanks have less HP); the formation lever.
- **Hard**: vents every 3 s with a 0.9 s tell and a 0.3 s ripple (0.4 s would not fit a row of four
  into the 3 s period; step F2's default, pending round 42); the Warden Tanks' 4-shot bursts and faster turrets;
  an extra up-facing nest of 3 at t≈100; an extra Wraith rear pair at t≈130 before the yard (warned);
  the walker's hard hooks and no boss checkpoint.

## Concept art

Production art of M7 part F's round 42 (step ART-F2), pending the user's review. Prompts:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r42-a.png](concept/backdrop-final-r42-a.png) | Level 19 backdrop final (`tools/art/backdrop_l19.py`): the play field at twelve times (slag fields, halls, furnace row, yard approach and gate, casting yard, the wreck's crawl, south gate), no units or props; the belt's four frames and the pieces (sheet) | chosen (round 42: approved as final) |
| [concept/backdrop-final-r42-a.gif](concept/backdrop-final-r42-a.gif) | Level 19 backdrop final: the whole level flown at 0.5x with the belts running, 10 fps | chosen (round 42: approved as final) |
| [concept/l19-props-final-r42-a.png](concept/l19-props-final-r42-a.png) | Level 19 props final (`tools/art/l19_props.py`, step ART-F4): the furnace vent grate (shut, the tell's three steps) with its tell glow, flame column and smoke; the gantry and the up-facing nest's cradle (chevrons pointing up the line); the payroll safe (intact, hit, opened) with its lamp and the shift office; the ingot stacks and pallet; the crews' rover in 16 headings with its lights; frames from the GIF (sheet) | chosen (round 42: approved as final) |
| [concept/l19-props-final-r42-a.gif](concept/l19-props-final-r42-a.gif) | Level 19 props final over the backdrop: the ore gantry's nests passing the ship and firing back up, the safe shot open, a vent row's ripple, the south gate's ingots and the crews' rovers, 10 fps | chosen (round 42: approved as final) |
| [concept/level-19-capture-r42-a.mp4](concept/level-19-capture-r42-a.mp4) | Capture of the whole level with sound (round 42, medium, the plan's fit with Rook's Mortar, the test autopilot): belts and Warden Tanks, gantry nests, furnace vents, the walker fight, the wreck crawl, the south gate, the debrief (261 s) | proposed (round 42) |
| [concept/level-19-capture-r42-a.png](concept/level-19-capture-r42-a.png) | 20-frame sheet of that run and a row of 1:1 crops (the walker front and back, a Warden Tank, a vent row) | proposed (round 42) |
| [concept/level-19-walker-r43-a.mp4](concept/level-19-walker-r43-a.mp4) | Check capture of the reworked walker fight with sound (round 43, step WR: medium, the plan's fit with Rook's Mortar, the test autopilot): the bar at the halt, the chin's fans and the flank guns, the salvos, the 240 px cone's tell and burn, phase 3 and the topple (83 s) | proposed (round 43) |
| [concept/level-19-walker-r43-a.png](concept/level-19-walker-r43-a.png) | 12-frame sheet of that fight (the play field, times in s of the run) | proposed (round 43) |
| [concept/level-19-walker-r44-a.mp4](concept/level-19-walker-r44-a.mp4) | Check capture of the walker fight after round 44 (step WR2: medium, the plan's fit with Rook's Mortar, the test autopilot, sound; 94 s, the fight 72 s): the pods' hatches and salvos, the stomp's brace ring, the cone, the red rail lance's charge line and shot once both pods are gone and in phase 3, the topple and the wreck; the lance's and the stomp's sounds are placeholders | proposed (round 44) |
| [concept/level-19-walker-r44-a.png](concept/level-19-walker-r44-a.png) | 12-frame sheet of that fight (the play field, times in s of the clip) | proposed (round 44) |
| [concept/payroll-safe-glow-r44-a.mp4](concept/payroll-safe-glow-r44-a.mp4) | The payroll safe's strobe in the live game (plan fit, the autopilot does not shoot it): the shift office scrolling down the left wall at about 130 px/s (0:02–0:06), the safe's door lamp blinking green about twice a second with a small green halo spilling just past the safe's box when lit. It reads as a blinking green lamp; the halo is small (≈ 12 px) and easy to miss at play speed among the belts. With sound, 9 s | proposed (round 44) |
| [concept/payroll-safe-glow-r44-a.png](concept/payroll-safe-glow-r44-a.png) | Six field frames + seven 1:1 crops of the safe at 2×, lit and dark | proposed (round 44) |

The level will need (part F's production-art round; the mood comes from the Tharsis
foundry mood still in [mars](../../../world/mars/README.md) and the walker's chosen concept a, the
"Strider heavy" biped, round 36 MA.4 and B.1):

- the production backdrop (seven sections: the slag fields with the tower on the horizon, the
  conveyor halls, the furnace row, the yard approach and gate, the casting yard as the arena, the
  south gate and the plateau) with the animated belt plates;
- the furnace vent grate (idle, tell, burst frames), the gantries over the up-facing nests (a
  readable "points back" cue), the payroll safe, the supply drone's pass;
- the walker's wreck with the filed plate lying in the yard (shared with Level 20's briefing image);
- the two briefing images; the intel portrait of the Warden Tank (Act 3 grey) and the "unclassified,
  huge" silhouette for L2;
- the round's captures: the level at medium with the plan's fit, a tank convoy on a belt firing back
  up, a vent ripple, the walker's passes and the hint-2 crawl with its radio.

## Implementation

- [x] Level data `data.yaml` (when the level is built, part F): sections and speeds with eases, the
      arena and the crawl section, waves, ground targets, radio, budget, threat profile, briefing,
      backdrop, music (step F2; the marked tables render from it)
- [x] The loader's keys checked (`FoundryChecks`) and mapped for the simulation (`FoundryRules`: belts,
      vents, `arc: up`, `hold_after_boss` and the crawl's ease, `allowed`; the units' turret, rear plate,
      stride, plate, cone, homing) (step F2, `FoundryKeysTest`)
- [x] Conveyor belts: a `Road` moving relative to the ground at 40 px/s, carrying ground units and
      wrecks; the Warden Tank's convoy paths on the belts (steps F3, G2: `Belts`, `FoundryTest`, `BeltWrecks`)
- [x] Up-facing Spine Turret nests (`arc: up`): silent above the ship, firing after it passes (step F3: `EnemyGun.arcFacing`, `FoundryTest`)
- [x] Furnace vents: rows, tell, burst, ripple order, period by difficulty, `heavy` 10 once per
      burst on the ship and Rook whatever their layer; cold in the yard (steps F3, G2: `FurnaceVents`, `FoundryTest`, `FoundryLooksTest`)
- [x] The casting-yard arena with the Revenant Walker (its spec), the mini-boss sting and the boss
      checkpoint; the bounty recomputed to ≈ 15 % with the level's `bounty_scale` (accepted as built in
      round 42, B.b; the walker reworked in round 43 for the user's K.a)
- [x] Hint 2: the wreck left as scenery, the 20 px/s crawl, Varga's line and Okafor's cut, the
      motif layer alone over the crawl (seen and accepted in the round-42 capture, N.c)
- [x] Wave script: the Act 2+ rules (`PacingTest`'s Act 2 rule, `ActFloorTest`'s floor; ≈ 88 a
      minute at medium, `DensityTest`), ≥ 60 % of waves from the front, edge warnings for every rear
      wave, the act HP factor for the Creeper, Ravager, Wraith, Choir Herald and Shellback, the beam
      era's HP (`BeamEraHpTest`), measured in step F4 (the autopilot's walker play and vent dodging;
      `Level19Test`, `RevenantWalkerTest`) (steps F2, F4: `Level19Test`)
- [x] Secondary `destroy-targets`: 12 of the 14 Warden Tanks, the HUD counter, judged at the end (steps F2, G2: `Level19HudTest`)
- [ ] Ground targets, the payroll-safe secret (20 HP trigger, crate 100), the supply drone's armour
      patch at t=122, the overdrive
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`, with Rook and without),
      Rook's lines `requires: escort`; the arena's event lines (steps VOX-F, G1: `RadioTimelineLevel19Test`)
- [ ] Threat profile with the "unclassified, huge" L2 silhouette, Varga's four intel lines and Rook's
      teaser fitting the intel panel (`IntelPanelLayoutTest`)
- [ ] Briefing: Okafor's page and Varga's, each one screen (`BriefingLayoutTest`), with their images
- [x] Typical haul within ±5 % of budget(19) = 2,366 with the level's `bounty_scale` (`TypicalHaulTest`;
      step F4: 2,399 at 0.28, +1.4 %; perfect 3,561)
- [ ] Music: track 9's stems, the mini-boss sting, the motif layer under the fight and alone over the
      crawl; the `mars-foundry` ambience
- [ ] Easy/hard variations; the autopilot flies the level and the fight with Rook on every difficulty
      (`--escort none` too)
- [ ] Production art and captures for part F's round

## Open questions

- None open: round 36 answered every question (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the Act 3 roster to a draft level document.
  Taken as the recommended options, pending the user's review in round 36: M10 = b (the Dust Kite as
  the act's popcorn), M12 = a, M13 = a (in the level only Varga's observation and Okafor's "Not on
  this channel, Doctor."), M15 = a (the scroll crawls past the wreck; Level 20's briefing shows the
  filed plate), M17 = a, M18 = a (reference DPS 92), M19 (hand tables). Our proposals: seven sections
  with the casting yard as the arena and a crawl section after it; belts running up the screen at
  40 px/s so the tanks linger and fire back; the rear-firing turrets as up-facing Spine Turrets;
  furnace vent rows as ripples with an open gap; the secondary 12 of 14 tanks; the payroll-safe
  secret; Ares Control as the colony voice (no new speaker); track 9 with the motif layer under the
  fight and alone over the crawl; 159 enemies (≈ 58 a minute, the minimum plus the 3 s rule's needs,
  not Levels 15–17's 85–90) and `bounty_scale` 0.53 for a typical 2,356; the walker's bounty
  recomputed to 262 (15.0 %).
- 2026-10-12 (overnight, harmonised with the act README): density raised into the act's band of
  60–72 a minute (pending the user's review in round 36) by four cadence-fill waves of small units in
  sections 2–4 (a Kite stream at t=56.5, a Skitter stream at t=84.5, a Kite V-wing at t=95, a
  Skitter snake at t=109; 13 enemies), none over the wreck crawl: 172 enemies (≈ 62 a minute), front
  90 % · rear 10 %; `bounty_scale` 0.53 → 0.52 (typical 2,364, −0.1 %), the walker's 262 kept
  (paid 349, 14.8 %). Okafor's t=148.5 "Not on this channel, Doctor." kept as the refrain
  bookending the Helix beat with Level 17 (Level 18's varied); the music already matches the act
  (track 9, Vorne's motif under the walker fight and alone over the crawl).
- 2026-10-13 (user, round 36): approved (D.6), M13 and M15 kept or accepted. Q.19 all yes: the
  rear-firing turrets are up-facing Spine Turrets (`arc: up`), the secondary "12 of 14 grey tanks",
  the furnace vents every 3.5 s for `heavy` 10 once per burst, the kite pairs at the edges during the
  wreck crawl, `bounty_scale` 0.52 accepted. The walker is the "Strider heavy" biped (B.1 = a); track
  9 is "Grey Signal" with the motif as an intercepted radio whistle (S.1 = b).
- 2026-10-19 (M7 part F, step F2): the numbers moved into [data.yaml](data.yaml), the tables marked. The gap
  check's defaults, pending the user's review in round 42: the Act 2+ floor met by density (243 enemies,
  ≈ 88 a minute: light firing groups in place of the approved popcorn streams, more up-facing nests under
  the gantries at t≈36, 70, 88, 110 and 152, the crawl's kites as Dust Kite swarms of 4 at the edges; the
  anchors and radio beats at their times), not by an `ActFloorTest` exception; Level 19 joins
  `small_act_hp`; `bounty_scale` 0.52 → 0.35 (paper typical 2,341, −1.1 %; step F4 sets it from the
  measured run) and the walker's bounty 262 → 395 (hull 315, pods 40 each; 354 paid, 15.0 %); Rook flies
  his owned Mortar at the plan's L19 visit (`fit: [[escort, mortar]]`, the level asks `anti-ground`); the
  Shellback's act-HP figures fixed to round 40 K.a's (99 / 18, was 131 / 26). The data's layout:
  section 6 (the crawl) runs 133–145 (the arena's `hold_after_boss: 1`, its 2 s ease up to 20 px/s,
  10 s at the crawl), section 7 eases back to 150 px/s from t=145; the boss warning 3 s before the halt
  (`incoming: 3`); the outbound belt at x 240 where ART-F2 drew it (the table had 300) and its tank pair
  at t=149 on it; the vents' seven rows of 3–4 grates alternating their ripple's end, hard's ripple
  0.3 s (0.4 s does not fit four grates into hard's 3 s period); the secondary's counter `GREY TANKS`
  and its name "Stop 12 of the 14 grey tanks" (the table's "No grey tank leaves the foundry" read as
  all 14). The Warden Tank's rear plate, as the briefing image shows it: a tank drives *up* its belt,
  so its rear plate faces the ship while it approaches (the tank spec said "after it passes"; fixed
  there). Okafor's approved secondary line "Not one grey tank left the foundry" is kept although up to
  two may get away (pending round 42).
- 2026-10-19 (M7 part F, step F4): measured with the autopilot (its new walker play and vent dodging) and the
  plan's fit (the Ion Beam L3, Rook's Mortar). The floor: step F2's waves gave 1.67 on the screen and 7.3 shots a
  minute at medium (the plan's fit kills everything in ≈ 1.5 s); three passes (*Waves*) give 2.83 and 27.8 (hard
  3.31, under the ≈ 3.5 cap), 297 enemies at medium. `bounty_scale` 0.35 → **0.28** (typical 2,399, +1.4 %; perfect
  3,561), the walker's bounty 395 → 495 (paid 355 = 15.0 %); the walker's HP 2,200 → 3,740 and par 60 → 53 (its
  spec). Pending the user's review in round 42: "tougher, not more" read with the floor as the binding rule (the
  level is denser than step F2's plan, as dense as Level 18), and the scale just under M12's 0.3.
- 2026-10-20 (user, round 42, closed): the backdrop (F.c) and the props (F.d) approved as final (their concept rows
  `chosen`, `art: final`); the level's data and the walker as built (B.a–B.d) and the round's other rows accepted;
  the walker's death and the crawl accepted (N.c). K.a (the capture) **change**: "The walker looks like a real easy
  target. It needs to do a lot more shooting and the flame needs to be bigger." The round-42 capture stays
  `proposed` until it is re-recorded; the sounds (S rows) are round 43's.
- 2026-10-20 (round 43, step WR): the walker reworked for K.a (its spec: far more fire in every phase, the flank
  guns, the 240 px cone that tracks the ship; HP 3,170, par 57); the level re-measured: the fight ≈ 61 s at medium,
  no retries, the haul unchanged (+1.4 %). A check capture of the fight is in *Concept art* (pending the user's
  review in round 43).
- 2026-10-20 (round 44, step WR2; the user in round 43, W.a/W.e): the walker's pods armoured until open, its rail lance
  once both pods are gone and in phase 3, its stomp shockwave (its spec; HP 1,350, par 51); the level re-measured:
  the fight 50–59 s at medium on the level test's seeds, no retries at medium, the floor and the haul unchanged
  (+1.4 %); hard is won with 2 retries over 3 runs (round 43: 1; hard keeps medium's lance, stomp and phase-1
  salvos, or the walker drained the ship before the level's end; pending the user's review). A check capture of the fight is in *Concept art* (pending the user's review in round 44).
