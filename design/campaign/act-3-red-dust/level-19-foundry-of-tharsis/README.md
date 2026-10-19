---
title: Level 19 – Foundry of Tharsis
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/bosses/revenant-walker, ../../../enemies/ground/warden-tank, ../../../enemies/ground/spine-turret, ../../../enemies/air/dust-kite, ../../../enemies/air/ghost-drone, ../../../story/twist, ../../../player/specials, ../../../world/mars]
updated: 2026-10-13
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
(M15 = a). About 172 s of level clock plus a ≈ 60 s fight; ≈ 62 enemies a minute.

**Status: approved in concept round 36 (user, 2026-10-13).** Written in the overnight design
phase (2026-10-12) from the act roster and the gap analysis's recommended answers, all kept or
accepted in the round. The numbers are still hand-written tables (M19: no `data.yaml` yet); M7
part F moves them into the level's data and turns these tables into marked ones.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (approved in round 36):

> **Commander Okafor:** "Tharsis Foundry went quiet two days ago. The Vrell hold the lower works,
> and those grey machines are with them. Two hundred crew are sealed in the smelter tower. Get them
> out."
>
> **Dr. Varga:** "Grey tanks ride the conveyors, and the gantry turrets face up the line: they fire
> after you pass. Rear guns, Lancer. And something large is in the casting yard."

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`):

- Okafor's page `level-19-tharsis-works`: a CDF site plan of the foundry: the slag fields, the
  two conveyor halls, the furnace row, the casting yard and the south gate; the smelter tower
  marked with the crew count; red Vrell vectors and grey, unlabelled contacts on the conveyor lines.
- Varga's page `level-19-rear-arcs`: her diagram of a gantry turret's firing arc pointing up the
  line (the ship passing under it, then caught from behind) and a Warden Tank's turret tracking
  backwards, its thin rear plate circled; a large blurred thermal blob in the yard in the corner.

*Hangar teaser* (shop screen before L19; Rook, not voiced):

> **Rook:** "A foundry full of grey tanks. Bring something that shoots backwards."

## Threat profile

Hand-written (M19); directions by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `ground` (Warden Tanks, Creepers, Ravagers, Shellbacks, turret and mortar nests, the walker), `air` (popcorn, Ghost Drones, Wraiths, a Choir Herald) |
| Attack directions | front 90% · rear 10% |
| Density | 4 |
| Recommended traits | `anti-ground`, `rear` |
| Hazards | Furnace vents flaring from the floor; conveyors carrying ground units; turrets that fire after the ship passes |
| Boss / mid-boss | Mid-boss: Revenant Walker (unnamed before the fight) |
| Sensor-suite detail | none: Tharsis industrial zone, `ground` + `air`, front · L1: + directions (front, rear), density 4, hazards "furnace vents" and "rear-firing turrets", the objective `CLEAR THE FOUNDRY` and the secondary `STOP THE GREY TANKS 12/14` · L2: + Warden Tank (new), Dust Kite, Skitter, Needler, Stinger, Ghost Drone, Wraith, Creeper, Ravager, Shellback, Choir Herald portraits, and a silhouette "unclassified, huge, `ground`" (the walker) · L3: + `anti-ground` and `rear` highlighted, the wave strip marking the casting-yard halt, 1 secret |

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
  before they leave the screen ("No grey tank leaves the foundry"), judged at the level's end. Pays
  **100** credits in Act 1 terms (256 in Act 3); the typical haul counts 50 %. The tanks ride the
  conveyors past the ship, so the secondary rewards a `rear` gun or `anti-ground` fire before they
  pass. The counter shows on the HUD from the first tank (as Level 09's targets).

## Layout

Scroll **150 px/s** on the open slag fields and the run-out, **130 px/s** in the halls and the
furnace row, **120 px/s** on the yard approach (the tanks and the turrets need time to pass). The
setting is [Mars](../../../world/mars/README.md)'s Tharsis Foundry: a rust plateau under a
butterscotch sky, black slag heaps, roofless steel halls, smelter stacks with orange glow, the
smelter tower standing on the left horizon. Motion budget: the conveyors' moving plates and the
furnace glow are the two strong elements; smoke stays at half particle speed. The data step adds
2 s eases between the speeds.

**Timeline.** The launch takes the first 5 s. Section 5 is the **arena** (`arena: true`,
`speed: 0`), t = 132–133: the scroll eases to a halt over its first second in the casting yard, and
the level clock halts at its end until the walker dies. Section 6 is the **wreck crawl** (M15 = a,
the walker's spec): 1 s after the death chain ends the clock resumes at t = 133 and the scroll
eases to 20 px/s, crawls for 10 s, then eases up to 150 px/s. About **232 s** with a 60 s fight.

| Section | t (s) | Scroll (px) | Speed (px/s) | Layers and content | Purpose |
|---|---|---|---|---|---|
| 1. Slag Fields | 0–24 | 0–3,600 | 150 | `deep`: the plateau, the foundry's stacks and the smelter tower on the horizon. `ground`: slag heaps, a rail spur, an ore gantry across the field (t≈18) with a turret nest under it. `low-air`: stack smoke (~15 %). | Popcorn from t=6; the first up-facing turret nest taught. |
| 2. Conveyor Halls | 24–64 | 3,600–8,800 | 130 | `ground`: two roofless halls, conveyor belts at x 140 and 340 running up the screen, gantries over them; the shift office at the left wall (the secret, t≈52). | Warden Tanks alone first (t=30.5), then on both belts. |
| 3. Furnace Row | 64–104 | 8,800–14,000 | 130 | `ground`: rows of furnace vent grates across the floor between smelter stacks, one belt at x 240 between the rows, slag pools glowing orange. `low-air`: heat shimmer above the grates. | The vents introduced alone (t≈64.5), then with waves; the Choir Herald; Shellbacks on the furnace road. |
| 4. Yard Approach | 104–132 | 14,000–17,360 | 120 | `ground`: the casting yard's two feed belts (x 100 and 380), two vent rows, the yard gate with a turret gantry (t≈115); the yard's slab floor enters at the top at the end. | Mixed waves; the last tanks before the yard. |
| 5. Casting Yard | 132–133 | 17,360–17,420 | 0 | `ground`: the casting yard filling the field: cast slabs, cooling moulds, molten channels (scenery, no damage), the vents cold. | The mid-boss (≈ 60 s at medium); the scroll halted while it lives. |
| 6. The Wreck | 133–147 | 17,420–17,810 | 0 → 20 → 150 | As section 5; the walker's **wreck** smoking where it fell, the filed plate turned up. | Hint 2: the crawl past the wreck with Varga's line and Okafor's cut. |
| 7. South Gate | 147–172 | 17,810–21,560 | 150 | `ground`: the outbound belt, ingot stacks, the south gate and the plateau beyond; the crews' rovers leaving the tower (t≈160, scenery). | Release; level end. |

### Conveyors

- Belts on the `ground` layer, 48 px wide, drawn with moving plates and rollers; they run **up the
  screen** at **40 px/s** relative to the ground (the foundry's feed lines run to the casting yard).
  A belt is a `Road` that moves relative to the ground (the [Warden Tank](../../../enemies/ground/warden-tank/README.md)'s
  conveyor rule, new in part F).
- Ground units on a belt are carried by it: a Warden Tank driving up a belt at its own 40 px/s moves
  down the screen at 130 − 40 − 40 = **50 px/s** in section 2, so it stays on the screen ≈ 12 s and
  its turret keeps firing back up at the ship after it has passed. Wrecks and husks on a belt ride
  it off the screen.
- Belts do not touch the ship, Rook, pickups or bullets (they fly above).

### Up-facing turret nests ("turrets fire from behind as you pass")

The roster's rear-firing turrets are [Spine Turrets](../../../enemies/ground/spine-turret/README.md)
placed with their silence arc turned **180°** (a placement parameter, `arc: up`, new in part F; the
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
  ×1.39 → 22 HP, **Choir Herald** (L17, 84) ×1.10 → 66 HP, **Shellback** (L17, 84) ×1.10 → body 131,
  vent 26. The Dust Kite, Skitter, Needler, Stinger, Ghost Drone, Spine Turret and Polyp Mortar keep
  their HP; the Warden Tank is new here (84).
- **Density and pacing** (§1.7): **172 enemies** (waves 152 + ground targets 20) over the 166 s of
  scroll from the launch, the arena excluded: **≈ 62 a minute** at medium (minimum 55). A wave
  every 3.5 s, so the screen is never empty over 3 s (`PacingTest`); the halt is the arena. Front
  90 % by count, every rear wave warned 3 s ahead at the bottom edge.
- **Chosen density.** The act's band is 60–72 a minute ([Act 3](../README.md#density-and-threat-profile)); this level sits low in it, with small popcorn waves
  (streams and V-wings of 3) and the weight in the ground units, so the tanks and the turrets, not
  the popcorn, fill the screen. Four **cadence-fill** waves of small units (t=56.5, 84.5, 95, 109,
  13 enemies) enter 1.5 s after a heavy ground wave in sections 2–4 and give the data step's recount
  its margin over the minimum; none over the wreck crawl.
- **Quiet windows**: no other wave for 3.5 s after the first Warden Tanks (t=30.5) and after the
  first vent row (t≈64.5, no wave on it); the crawl carries only kite pairs at the field's edges, so
  the wreck in the middle reads.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (left) | |
| 9.5 | 1 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | |
| 13 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 16.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Over the ore gantry's up-facing nest (t≈18) |
| 20 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | |
| 23.5 | 1 | rear ambush | [Skitter](../../../enemies/air/skitter/README.md) | 3 | rear | The first rear entry, warned 3 s ahead |
| 27 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 30.5 | 2 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front (belt x 140) | **New.** Alone until t=34; Varga's line at t=30 |
| 34 | 2 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | |
| 37.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 41 | 2 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 3 | front (belts x 140 and 340) | 2 + 1, 1.5 s apart |
| 44.5 | 2 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | |
| 48 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Cloaked; warned 3 s ahead; act HP factor (22 HP) |
| 51.5 | 2 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (right) | Past the shift office (the secret) |
| 55 | 2 | convoy | [Creeper](../../../enemies/ground/creeper/README.md) | 3 | front | On the hall floor road; act HP factor (46 HP) |
| 56.5 | 2 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | Cadence fill over the Creeper convoy |
| 58.5 | 2 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | Over the second gantry nest (t≈60) |
| 62 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | Gone before the vents' quiet window |
| 65.5 | 3 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | After the first vent row's flare (t≈64.5) |
| 69 | 3 | carrier + escorts | [Choir Herald](../../../enemies/air/choir-herald/README.md) + [Needler](../../../enemies/air/needler/README.md) | 1 + 4 | front | The escorts in `orbit`, ringed; act HP factor (66 HP) |
| 72.5 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | |
| 76 | 3 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 3 | front (belt x 240) | Between the vent rows |
| 79.5 | 3 | rear ambush | [Skitter](../../../enemies/air/skitter/README.md) | 3 | rear | Warned 3 s ahead |
| 83 | 3 | pair | [Shellback](../../../enemies/ground/shellback/README.md) | 2 | front | On the furnace road; act HP factor (131 / 26) |
| 84.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Cadence fill over the Shellbacks |
| 86.5 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (left) | |
| 90 | 3 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 4 | front | |
| 93.5 | 3 | pack | [Ravager](../../../enemies/ground/ravager/README.md) | 4 | front | Round the slag pool's mortars; act HP factor (23 HP) |
| 95 | 3 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | Cadence fill over the Ravager pack |
| 97 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | The leader drops an overdrive |
| 100.5 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Warned 3 s ahead |
| 104 | 4 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | |
| 107.5 | 4 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 4 | front (belts x 100 and 380) | 2 + 2, the feed belts |
| 109 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | Cadence fill beside the feed belts |
| 111 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 114.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Over the yard gate's nest (t≈115) |
| 118 | 4 | rear ambush | [Skitter](../../../enemies/air/skitter/README.md) | 3 | rear | Warned 3 s ahead |
| 121.5 | 4 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | The supply drone's armour patch at t=122 |
| 125 | 4 | convoy | [Creeper](../../../enemies/ground/creeper/README.md) | 3 | front | |
| 128.5 | 4 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | Gone before the halt |
| — | 5 | solo | [Revenant Walker](../../../enemies/bosses/revenant-walker/README.md) | 1 | top (its intro pass) | **Mid-boss.** Its passes from every edge per its spec, with their edge warnings; no other waves in the arena |
| 135 | 6 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (left edge) | The crawl: kites along the edges only |
| 138.5 | 6 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (right edge) | |
| 142 | 6 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (left edge) | |
| 145.5 | 6 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | The scroll picks up |
| 149 | 7 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front (outbound belt x 300) | The last two tanks |
| 152.5 | 7 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 156 | 7 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Warned 3 s ahead |
| 159.5 | 7 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | |
| 163 | 7 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | |
| 166.5 | 7 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | The last wave |

Totals: Dust Kite 59 · Skitter 34 · Warden Tank 14 · Ghost Drone 10 · Needler 9 · Stinger 7 ·
Creeper 6 · Wraith 6 · Ravager 4 · Shellback 2 · Choir Herald 1 (152), plus the walker.

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 1 | Up-facing [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 under the ore gantry (t≈18) | Bounty 12 each; fire only after the ship has passed |
| 2 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) pair in a slag pit (t≈46) | Bounty 15 each |
| 2 | Foundry payroll safe in the shift office (`ground` trigger, 20 HP, t≈52, x 60) | The secret: frees a crate worth 100 (256) |
| 2 | Up-facing Spine Turret nest ×3 under a conveyor gantry (t≈60) | Bounty |
| 3 | Up-facing Spine Turret nest ×4 on the smelter stacks, 2 + 2 either side of the row (t≈80) | Bounty |
| 3 | Polyp Mortar floe ×3 by the slag pool (t≈95) | Bounty |
| 4 | Up-facing Spine Turret nest ×3 under the yard gate (t≈115) | Bounty |
| 7 | Polyp Mortar pair at the south gate (t≈155) | Bounty |

The Warden Tanks, Creepers, Ravagers and Shellbacks are waves (they move); they are counted with
the ground units in the budget.

## Hazards

- **Furnace vents** (sections 3–4, seven rows of 3–4 grates; Level 05's sleds are the model): a
  grate glows orange to white for a **1.2 s tell** (heat shimmer above it, a rising roar), then a
  flame column **44 px** across stands for **1.0 s**. It hits the ship and Rook whatever their layer
  for `heavy` **10**, once per burst; enemies are unharmed and bullets pass. A row fires as a ripple,
  one grate after the other 0.4 s apart from one end, every **3.5 s**, so a gap is always open and a
  row fires at least once while it is on the screen. The first row (t≈64.5) flares alone before any
  wave reaches it. The vents are cold in the casting yard.
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

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Tharsis Foundry, Lancer. The Vrell hold the lower works, and the grey machines are with them. Clear it to the casting yard." |
| t=14 | Varga | "Gantry turrets ahead face up the line. They fire once you're past." |
| t=22.5 | Ares Control | "Two hundred foundry crew are sealed in the smelter tower." |
| t=30 | Varga | "Grey tanks on the conveyors. Their back plates are thin." |
| t=38 | Rook | "Tanks on a conveyor belt. Someone hates walking." |
| t=45.5 | Rook | "Six o'clock! Cloakers!" |
| t=57.5 | Ares Control | "Furnace row is next. The vents still fire. Watch for the glow." |
| t=66 | Varga | "A singer with escorts. Kill it and their shields go with it." |
| t=77 | Rook | "Coming up behind, Lancer!" |
| t=88 | Okafor | "Grey drones again, same as the storm. Keep your spacing." |
| t=97.5 | Rook | "Cloakers on six again!" |
| t=105 | Ares Control | "Something big walked into the casting yard an hour ago. It isn't one of ours." |
| t=116 | Rook | "Behind us!" |
| t=124.5 | Okafor | "Casting yard ahead. Whatever is in there, Lancer, put it down." |
| First flame tell (`first-telegraph`) | Varga | "It burns what's behind it. Stay out of the cone, then hit the vents." |
| First missile pod destroyed (`boss-part-destroyed`) | Rook | "Pod's gone. One more." |
| Walker destroyed (`boss-destroyed`) | Rook | "Walker's down. No colours, no numbers." |
| t=137 | Varga | "Lancer, the serial plates are filed off. By hand. And that alloy is Helix foundry stock." |
| t=148.5 | Okafor | "Not on this channel, Doctor." |
| t=155.5 | Ares Control | "Tower to Aegis: the yard is clear. We're coming out." |
| t=163 | Okafor | "South gate ahead. Last of them, Lancer." |
| The payroll safe's crate freed (secret) | Ares Control | "That's the foundry payroll. The crews say keep it." |
| Secondary objective met (at the end) | Okafor | "Not one grey tank left the foundry. Good." |
| Level end | Okafor | "Tharsis is breathing again. Come home, Aegis." |

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
- No other waves in the arena: the passes and their edge warnings are the fight (≈ 60 s at medium,
  tuned by measurement as the spec says).
- **Bounty**: recomputed with Level 19's `bounty_scale` of 0.52 (below): **262** in Act 1 terms
  (hull 210, pods 26 each), paid 262 × 2.56 × 0.52 = **349 = 14.8 %** of budget(19).
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
Polyp Mortar 15, the walker 262, the crate 100, the secondary 100), × the act factor **2.56**,
bounties × the scale. A **`bounty_scale` of 0.52** puts the typical haul at **2,364 (−0.1 %)**,
just above M12's 0.3–0.5 (the ground units' share is high and the popcorn thin).

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills (air): Dust Kite 59 × 4 + Skitter 34 × 5 + Ghost Drone 10 × 14 + Needler 9 × 12 + Stinger 7 × 15 + Wraith 6 × 30 + Choir Herald 1 × 30 = 969 | 1,290 | 774 |
| Ground: Warden Tank 14 × 26 + Spine Turret 13 × 12 + Polyp Mortar 7 × 15 + Creeper 6 × 22 + Ravager 4 × 18 + Shellback 2 × 48 = 925 | 1,231 | 985 |
| Mid-boss: Revenant Walker (hull 210 + pods 26 + 26) | 349 | 349 |
| Secret: payroll safe (hidden crate 100) | 256 | 128 |
| Secondary: 12 of 14 grey tanks destroyed | 256 | 128 |
| **Total** (bounty scale 0.52, act factor 2.56) | **3,382** | **2,364** |
| Budget(19) = the typical haul's target; typical −0.1 %, perfect 1.43 × budget | | 2,366 |

Whole-unit bounties (Warden Tank 26, Shellback 48) are used; part-first kills pay a little more.
**Balance plan**: the plan's L19 visit comes from part A's Act 3 balance plan (M18; the
[Ion Beam](../../../player/weapons/ion-beam/README.md)'s hand plan reaches forward DPS 100 here). The
level expects `rear` (a Tail Gun or the [Rear Lance](../../../player/weapons/rear-lance/README.md))
and `anti-ground`, and is flyable without either; the Shield Overcharge (unlocked at this visit) and
the EMP Burst against the machines are options, not needs.

## Difficulty notes

- **Easy**: vents every 5 s with a 1.6 s tell; the Warden Tanks' 2-shot bursts and the walker's easy
  hooks (their specs); the secondary stays 12 of 14 (the tanks have less HP); the formation lever.
- **Hard**: vents every 3 s with a 0.9 s tell; the Warden Tanks' 4-shot bursts and faster turrets;
  an extra up-facing nest of 3 at t≈100; an extra Wraith rear pair at t≈130 before the yard (warned);
  the walker's hard hooks and no boss checkpoint.

## Concept art

Production art of M7 part F's round 42 (step ART-F2), pending the user's review. Prompts:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r42-a.png](concept/backdrop-final-r42-a.png) | Level 19 backdrop final (`tools/art/backdrop_l19.py`): the play field at twelve times (slag fields, halls, furnace row, yard approach and gate, casting yard, the wreck's crawl, south gate), no units or props; the belt's four frames and the pieces (sheet) | proposed (round 42) |
| [concept/backdrop-final-r42-a.gif](concept/backdrop-final-r42-a.gif) | Level 19 backdrop final: the whole level flown at 0.5x with the belts running, 10 fps | proposed (round 42) |

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

- [ ] Level data `data.yaml` (when the level is built, part F): sections and speeds with eases, the
      arena and the crawl section, waves, ground targets, radio, budget, threat profile, briefing,
      backdrop, music
- [ ] Conveyor belts: a `Road` moving relative to the ground at 40 px/s, carrying ground units and
      wrecks; the Warden Tank's convoy paths on the belts
- [ ] Up-facing Spine Turret nests (`arc: up`): silent above the ship, firing after it passes
- [ ] Furnace vents: rows, tell, burst, ripple order, period by difficulty, `heavy` 10 once per
      burst on the ship and Rook whatever their layer; cold in the yard
- [ ] The casting-yard arena with the Revenant Walker (its spec), the mini-boss sting and the boss
      checkpoint; the bounty recomputed to ≈ 15 % with the level's `bounty_scale`
- [ ] Hint 2: the wreck left as scenery, the 20 px/s crawl, Varga's line and Okafor's cut, the
      motif layer alone over the crawl
- [ ] Wave script: ≥ 55 enemies a minute at medium (`DensityTest`, ≈ 62), no empty screen over 3 s
      (`PacingTest`), ≥ 60 % of waves from the front, edge warnings for every rear wave, the act HP
      factor for the Creeper, Ravager, Wraith, Choir Herald and Shellback
- [ ] Secondary `destroy-targets`: 12 of the 14 Warden Tanks, the HUD counter, judged at the end
- [ ] Ground targets, the payroll-safe secret (20 HP trigger, crate 100), the supply drone's armour
      patch at t=122, the overdrive
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`, with Rook and without),
      Rook's lines `requires: escort`; the arena's event lines
- [ ] Threat profile with the "unclassified, huge" L2 silhouette, Varga's four intel lines and Rook's
      teaser fitting the intel panel (`IntelPanelLayoutTest`)
- [ ] Briefing: Okafor's page and Varga's, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(19) = 2,366 with the level's `bounty_scale` (`TypicalHaulTest`)
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
