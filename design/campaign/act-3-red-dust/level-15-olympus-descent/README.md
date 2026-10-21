---
title: Level 15 – Olympus Descent
design: approved
implementation: done
art: final
depends-on: [../../../enemies/ground/burrower, ../../../enemies/air/dust-kite, ../../../world/mars, ../../../player/wingmen, ../../../systems/economy]
updated: 2026-10-20
---

# Level 15 – Olympus Descent

## Summary

Act 3's opener. Aegis Wing drops from the Mars transport into a contested entry corridor, falls
through the cloud deck past the flank of Olympus Mons and crosses the Tharsis dunes to the rail
line of the colonies, which have held alone for five weeks. The level introduces the
[Burrower](../../../enemies/ground/burrower/README.md), the act's first unit that can be hit only
while it is surfaced (the new `buried` layer), and the Martian popcorn, the
[Dust Kite](../../../enemies/air/dust-kite/README.md). 190 s of scroll in five sections, no boss.

**Status: approved in concept round 36 (user, 2026-10-13).** Written in the overnight design
phase (2026-10-12) from the act roster and the gap analysis's recommended answers, all kept or
accepted in the round. The numbers live in the level's [data.yaml](data.yaml) since M7 part B (step
B2); the tables below are rendered from it. The simulation flies its new keys (the Burrower's burrow, the Dust
Kite's swoop, the moving sections' eases, the drop launch, the no-surface zones) once step B3 maps them.

## Briefing

Two pages, as every level since Level 08, each one screen with its image (`BriefingLayoutTest`),
at most four lines of text so the image fits. They follow the Act 3 title card and the act's four
intro pages ([Act 3](../README.md)), which carry the arrival at Mars; the briefing carries only the
mission. Texts (approved in round 36):

<!-- data: briefing -->
> **Commander Okafor:** "The Vrell own the sky over Ares Landing, so we come in from the west:
> down the flank of Olympus Mons and across the dunes to the colony rail line. They have held
> five weeks alone. Don't keep them waiting."
>
> **Dr. Varga:** "The colonists call them Burrowers. They swim under the sand, surface to fire,
> then dive. Nothing touches them underground. The sand bulges just before one comes up: shoot
> there."
<!-- /data -->

*Images* (production art straight away, as in Act 2; [briefing images](../../../ui/briefing/README.md)):
Okafor's page `level-15-descent-route` (the entry corridor on a CDF plot: the transport in orbit,
the cloud deck, the flank of Olympus Mons, Lycus Survey Station at the foot of the escarpment, the
dunes and the rail line east toward Ares Landing), Varga's `level-15-burrower-scan` (a cut through
the regolith: a Burrower swimming under the sand, the bulge above it, the surfaced maw firing its
ring, the `anti-ground` ×2 marker).

*Hangar teaser* (shop screen before L15, Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "Mars. Red sand, red sky, and bugs that swim in the dirt. There's a beam gun in the
> shop, if you're feeling rich."
<!-- /data -->

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `ground` (Burrowers when surfaced, Ravager packs, a Creeper convoy, turret nests and mortars), `buried` (Burrowers under the sand: nothing hits them), `air`; `high-air` and `low-air` carry the cloud decks (scenery) |
| Attack directions | front 97% · sides 3% (two Needler pincers) |
| Density | 3 |
| Recommended traits | `anti-ground`, `forward` |
| Hazards | none (no terrain collision; the Burrowers' rings are their attack) |
| Boss / mid-boss | none (finale the dune field of six Burrowers under a Kite V-wing) |
| Sensor-suite detail | none: Mars, entry and the slopes of Olympus Mons, `ground` + `air`, front · L1: + directions, density 3, no hazards, the objective `REACH THE RAIL LINE` · L2: + Burrower (new), Dust Kite (new), Skitter, Needler, Stinger, Spore Bomber, Ravager and Creeper portraits; no boss · L3: + `anti-ground` highlighted, the wave strip marking the Burrower fields at 68, 80, 98, 125, 153 and 172 s, 1 secret |
<!-- /data -->

*Varga's intel line* per sensor-suite level (fitting the panel is `IntelPanelLayoutTest`'s):

| Sensor suite | Line |
|---|---|
| none | "Mars, Lancer. A hot descent, then the slopes of Olympus Mons. Something lives in the sand." |
| L1 | "Nearly all from ahead, two pincers from the flanks. Nothing behind you." |
| L2 | "New: Burrowers under the sand, and dust kites in the clouds." |
| L3 | "Anti-ground for the Burrowers. One cache on the escarpment." |

## Objective

- **Primary** `reach-end`: reach the colony rail line at the end of the scroll.
- **Secondary** "Clear the slopes" (`kill_ratio`, the campaign's rule of thumb): destroy at least
  **65 %** of the level's enemies, judged at the level's end. Burrowers that dive away after their
  third surfacing count against it, so the secondary rewards hitting them in their windows. Pays
  **80** credits in Act 1 terms (205 in Act 3, the act factor 1.6² = 2.56); the typical haul
  counts it at the secondary's 50 %.

## Layout

The act opener's pace, normal to calm: **190 px/s** for the entry, **170** through the cloud
deck, **150** on the slopes and the escarpment, **160** over the dunes (the art direction's
normal 150–170 px/s; each change eased over 1 s). The launch takes the first 5 s: the ship and
Rook drop from the transport's bay in formation (the transport's underside passes on `high-air`
for the first 3 s). About **190 s**, 30,800 px. Rook flies from the escort slot from the start
(M17 = a); `--escort none` flies the level without him. The data says `launch: drop` (the ship and Rook
drop in from above the top edge to the start spot over the 5 s; accepted in round 38, B.2 (a)) and eases each speed change over 1 s (the moving sections' `ramp`), so the scroll column below
runs a few px past the hand table's round numbers.

**The descent** (the act roster's "clouds scroll past in the foreground"): in sections 1–2 the
ground is far below. The `deep` layer carries the Martian surface at a small scale (Olympus Mons'
caldera and flank, the Tharsis plain beyond), the entry streaks fade on `high-air` in the first
8 s, and the cloud decks pass on two layers: thin water-ice cirrus on `high-air` at most 40 %
opacity (never hiding a bullet, the [layer rule](../../../art-direction/README.md)) and the thick
deck on `low-air` below the play plane. At the end of section 2 the low-air deck closes over the
whole field for about 3 s (t≈61.5–64.5: closing from ≈ 60, parted by ≈ 65.5, as built and accepted in
round 38) while the `ground` layer takes over from `deep` underneath it, then
parts over the slopes: the descent reads as a fall without a zoom. No ground targets before
section 3.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Entry Corridor | 0–30 | 0–5,700 | 190 | light, clear peak 0–8 | `deep`: Mars from high up, Olympus Mons' caldera rim and flank, the Tharsis plain beyond. `high-air`: the transport's underside (0–3 s), fading entry streaks (0–8 s, the entry glow), thin cirrus (≤ 40 %). | The launch; the Dust Kite's first sight at t=5; Needler lines and Stinger streams round the popcorn. |
| 2. Cloud Deck | 30–65 | 5,700–11,660 | 170 | medium | `deep`: the flank closer, lava channels. `low-air`: the thick water-ice deck in banks (~30 %), covering the field at t≈61.5–64.5 while `ground` takes over. `high-air`: cirrus (≤ 40 %). | Mixed air waves through the cloud banks; the first pincer. |
| 3. Olympus Slopes | 65–115 | 11,660–19,170 | 150 | light | `ground`: the volcano's lower flank, lava terraces, basalt outcrops, dusty regolith fields with burrow furrows. `low-air`: the last cloud wisps, thin dust. | The Burrower alone at t=68, then fields of 4 with popcorn; the first turret nest and mortars. |
| 4. Lycus Escarpment | 115–150 | 19,170–24,420 | 150 | light | `ground`: the foot of the escarpment (the great cliff, drawn as perspective scenery, nothing gameplay touches on its face); **Lycus Survey Station**, overrun: pads, a domed hab, a road with Vrell growths; the crashed drop pod on a ledge at its foot. `low-air`: thin dust. | The station's growths, the Creeper convoy on its road, the secret. |
| 5. Tharsis Dunes | 150–190 | 24,420–30,815 | 160 | light, heavy peak 170–180 | `ground`: the dune sea, a crashed colony lander on a crest, then the colony rail line and its pylons across the field at the end. `low-air`: dust streaks off the crests. | The finale field of six Burrowers; the level ends over the rail line. |
<!-- /data -->

The backdrop's look comes from the chosen Mars scenes of round 02/03
([art direction](../../../art-direction/README.md); parallax r02-c the approach, r03-c the canyon
for Level 16), with the sub-locations of [Mars](../../../world/mars/README.md) (Olympus Mons,
Tharsis dunes).

## Waves

Formation names from the [formation vocabulary](../../../enemies/README.md). The returning
`medium` units get the act HP factor (reference DPS 76 at L15, M18 = a: Ravager 16 → 19, Creeper
30 → 38, Spore Bomber 24 → 57); the Needler and the Stinger get it capped at ×3 (`small_act_hp`, step PE15:
12 and 18 HP, as in Act 2's reworked levels and Level 18); the other `tiny` and `small` ones keep their HP.

- **Density and pacing** (step PE15, the user's round 40 P.e "all later levels" and "tougher, not more"):
  **306 enemies at medium** (the waves' 292 and the 14 turrets and mortars) over the 185 s after the
  launch, **≈ 99 a minute** (the act's minimum is 55); a wave about every 2 s from t=5. The Act 2+ rules
  hold: at medium and hard at most 3 pauses over 2 s (`PacingTest`: medium 1, the Burrower's first window
  65.5–68; hard none) and, with the plan's fit at medium, **2.68 enemies on the screen and 17.2 enemy shots
  a minute** (`ActFloorTest`: 2.5 and 10; before: 200 enemies, 1.42 and 10.7, 17 pauses). The floor comes from
  long-lived, firing, low-threat groups, not more popcorn: **Needler lines of 3** (they hover and fire),
  **Stinger streams of 3**, the Dust Kite V-wings turned into **swarms of 6** that loop on the screen, round
  the Act 3 popcorn (Dust Kite and Skitter streams of 3, snakes of 4). A submerged Burrower counts as on the
  screen: its ripple is visible (`PacingTest`).
- **The Burrower's first sight** (t=68): one field of three alone; no other wave enters until
  t=73 and the Kites before it leave the screen first, as Level 10's "safe first sight". Its fields
  are `field` waves (scattered, entering with the ground; the unit spec's "swarm (scattered)",
  proposal: `field`), staggered by the spec (0.8 s apart, at most two up at once), and only over
  sand, ash or regolith.
- **Front 96 %**: two Needler pincers (t=49, t=142.5) enter from the sides with the LEFT/RIGHT
  edge warnings 3 s ahead; no rear waves.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 5 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | **New.** The first targets, shimmering out of the haze |
| 7.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 10.5 | 1 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | |
| 12.5 | 1 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 14 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (left) | |
| 17.5 | 1 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | |
| 19.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 22.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 24.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 26.5 | 1 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | |
| 28 | 1 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 6 | front | Over the first cloud bank |
| 30.5 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 31.5 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 33.5 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 35 | 2 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (right) | Out of a cloud bank |
| 36.5 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 38.5 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | The leader drops an overdrive |
| 42 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 43.5 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 45.5 | 2 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 6 | front | |
| 47 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 49 | 2 | pincer | [Needler](../../../enemies/air/needler/README.md) | 4 | sides | 2 per side, hold 4 s; edge warnings; Rook's flank bark; easy: a line abreast from the front |
| 49 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Easy only: instead of the pincer, its first rank |
| 51 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | Easy only: the pincer's second rank, 2 s later (step B3: no empty screen over 3 s before the t=52.5 snake) |
| 52.5 | 2 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | |
| 54.5 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 56 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 57.5 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | |
| 59.5 | 2 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | Act HP factor (57 HP) |
| 62.5 | 2 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | As the deck closes; gone before the first Burrower |
| 68 | 3 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 3 | front | **New.** Alone: no other wave until t=73 (buried, ahead of the ship) |
| 73 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | |
| 74.5 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 76.5 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 78 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 80 | 3 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 4 | front | With the lava-terrace mortars |
| 82 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 84 | 3 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 6 | front | |
| 89 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | |
| 91 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | Over the basalt turret nest |
| 94.5 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (right) | |
| 96.5 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 98 | 3 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 4 | front | |
| 101.5 | 3 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | Over the Burrower field |
| 103 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | |
| 105 | 3 | pack | [Ravager](../../../enemies/ground/ravager/README.md) | 4 | front | Down the regolith fields; act HP factor (19 HP) |
| 108.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 110 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 112 | 3 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 6 | front | |
| 114 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 115.5 | 4 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | |
| 118 | 4 | convoy | [Creeper](../../../enemies/ground/creeper/README.md) | 3 | front | On the survey station's road; act HP factor (38 HP); hard 4 |
| 120 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 121.5 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (left) | |
| 123 | 4 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | |
| 125 | 4 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 5 | front | Among the station's pads (none surfaces on them) |
| 128.5 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | The last one drops an armour patch |
| 130.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 132 | 4 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | |
| 134 | 4 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | |
| 135.5 | 4 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | |
| 137.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 139 | 4 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | |
| 141 | 4 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 142.5 | 4 | pincer | [Needler](../../../enemies/air/needler/README.md) | 4 | sides | 2 per side, hold 4 s; edge warnings; easy: a line abreast from the front |
| 142.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Easy only: instead of the pincer |
| 146 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 147.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 149.5 | 4 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 6 | front | Over the first dunes |
| 151.5 | 5 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 153 | 5 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 5 | front | |
| 158 | 5 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 160 | 5 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | Over the crashed lander's nest |
| 162 | 5 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | |
| 163.5 | 5 | pack | [Ravager](../../../enemies/ground/ravager/README.md) | 4 | front | Over the dune crests |
| 167 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 170.5 | 5 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front (alternating edges) | |
| 172 | 5 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 6 | front | The finale: the dune field heaves (Okafor's warning at t=168) |
| 174 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 176 | 5 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 177.5 | 5 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 6 | front | |
| 179.5 | 5 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | |
| 181 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 183 | 5 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | The last waves, over the rail line |
| 185.5 | 5 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (alternating edges) | |

Totals: Dust Kite 84 · Needler 73 · Stinger 56 · Skitter 37 · Spore Bomber 4 · Burrower 27 · Ravager 8 · Creeper 3.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 3 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) ×2 on a lava terrace (t≈80) | With the t=80 Burrower field: the first mixed ground read |
| 3 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on a basalt outcrop (t≈92) | Bounty |
| 4 | Lycus Survey Station's growths: Spine Turret ×4 on its pads (t≈118–125) | Bounty; the station is Coalition scenery (no damage, shots pass over it) |
| 4 | Polyp Mortar ×2 on the hab's roof (t≈121) | Bounty |
| 4 | Station supply containers ×4 (`ground`, 3 HP, t≈122–128) | Each drops a small salvage pickup (10); easy, the last one an armour patch too |
| 4 | Rubble over the crashed drop pod on the escarpment's ledge (trigger, t≈128, x≈400) | 4 hits (hard 6) knock it away and uncover the hidden crate |
| 5 | Spine Turret nest ×3 at a crashed colony lander on a dune crest (t≈160) | Bounty |
| 5 | Spine Turret pair at the rail line (t≈185), **hard only**; hard ×2 | Bounty |
<!-- /data -->

Burrowers never surface on the station's pads, the hab or the rail line (the spec's "only over
sand, dust or regolith"): the level's data marks those areas as no-surface zones (the
spec's spot rule then picks the nearest allowed spot).

## Hazards

None. The cloud decks sit on `low-air` (thick) and `high-air` (thin, ≤ 40 %) and never hide
bullets; the escarpment is scenery, with no terrain collision (that starts in Level 16).

## Secrets and pickups

- **Crashed drop pod** (t≈128, x≈400, on the escarpment's ledge): a colonial supply pod half
  buried under rubble. The rubble is a trigger that counts hits: **4 hits** (hard 6) knock it
  away and uncover a crate worth **100** credits in Act 1 terms (256 in Act 3). The Salvage
  scanner's glint shows on the rubble. Its line, Ares Control: "*Our supply drop from last week.
  It's yours, Aegis.*" (draft, one page so it fits the gap before the Choir).
- **Overdrive**: dropped by the t=38.5 Stinger leader.
- **Armour patch**: dropped by the last Stinger of the t=128.5 V-wing.
- **Salvage**: the four station containers, 10 each (Act 1 terms).
- Shield cells follow the normal drop table.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), with the HUD's **1 s rule**
(`RadioTimelineTest`): a timed line holds the radio for the longer of its voice and its text (5 s
for the last page plus 3 s for each page before it, typed at 30 characters a second), then 0.4 s
of silence, and starts after the one before it has closed. The times below were checked with
that model (a two-page line holds ≈ 11 s, a one-page line ≈ 7.5 s, a voice at ≈ 2.6 words a
second): no timed line starts late; the voiced check comes with the data. Event lines: `first-surfacing` (new: the attempt's first Burrower
breaking the surface; once per attempt), the secret's, `secondary-objective` and the level end.

**New speaker: Ares Control**, the flight controller of Ares Landing: a colonist, proud and
tired, who resents Earth arriving after five weeks alone (the act's L15 beat) and thaws over the act.
Cast in round 36 (S.6 = b: reader Thomas A; his lines rendered in M7 part B); the radio portrait
the new colonial generic `radio-generic-colonial` (C.1 = b, the dust-suit controller, on the teal
friendly channel). **Rook's
lines** carry `requires: escort`; his `{side}` line is Level 08's mechanism; his one dry joke of
the level is at t=100.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, you're clear of the transport. The corridor is hot. Weapons free." |
| t=12 | Generic colonial (Ares Control) | "Aegis flight, Ares Landing. Five weeks we held alone. Nice of Earth to drop by." |
| t=23.5 | Rook | "Copy, Lancer. I'm on your {side}. Watch the clouds, things are hiding in them." |
| t=34.5 | Varga | "Those kites ride the dust. You barely see them until they turn. Shoot early." |
| t=45.5 | Okafor | "Clouds are breaking up. Olympus Mons dead ahead, all twenty-two kilometres of it." |
| t=57 | Varga | "Ground movement on the slopes. Something is swimming under the sand." |
| First Burrower surfaces (`first-surfacing`) | Varga | "There! The sand bulges before it comes up. Hit it while it's out." |
| t=84 | Generic colonial (Ares Control) | "Those things took our survey posts. Lycus Station went dark a week ago." |
| t=100 | Rook | "Bugs that swim in the dirt. Back home we just had moles." |
| t=110 | Okafor | "Lycus Station ahead. The growths on its pads are Vrell. Burn them off." |
| t=142 | The Choir (distorted) | "[the Choir sings]" |
| t=149.5 | Varga | "That song again. Lancer, it's coming from under the ground." |
| t=158.5 | Generic colonial (Ares Control) | "Rail line in sight. Half the colony is watching you come in." |
| t=168 | Okafor | "Big contact under the dunes. Lots of them. Keep moving." |
| Secondary met (at the level end) | Generic colonial (Ares Control) | "The slopes are quiet. Maybe Earth can fly after all." |
| Level end | Okafor | "You're down, Aegis. Refuel at the rail head. The canyon run starts at dawn." |
<!-- /data -->

The `first-surfacing` line falls in the t=68–84 gap (the first field surfaces from t≈72.5, the
bulge 1 s before at ≈ 71.5, as built and accepted in round 38), after Varga's t=57 line has closed (≈ t=67.7). The secret's line has the gap
from Okafor's close (≈ t=120.7) to the Choir at t=142: the pod is in reach at t≈128–131. The
Choir's stage sound (≈ 3.9 s, subtitle 5.6 s) closes before Varga at t=149.5.

## Boss / mid-boss

None. The finale is the t=172 field of six Burrowers under the Kite V-wing and the Skitter snake
over the rail line.

## Music & ambience

The Act 3 A theme **"Red Dust Run"** (track 8 in the
[track list](../../../audio/music/README.md#track-list); chosen in round 02 as a 53.8 s concept, the
full length and its stems are part B's): section 1 with the entry wind roar only, the **base stem
from section 2** and the **full mix from section 3** (the slopes, the Burrower's first sight); the
mission-complete jingle at the end. Ambience: the Martian dust wind
([ambience per setting](../../../audio/sfx/README.md#ambience-per-setting), `mars-dust`, new) from
section 3, a high thin entry roar in sections 1–2. The Burrower's rumble and eruption are the
unit's sounds (step A-audio proposes the eruption).

## Credit budget

Budget(15) = 700 × 1.07¹⁴ ≈ **1,805**, the typical haul's target
([economy](../../../systems/economy/README.md#per-level-budget)): 60 % of the air kills, 80 % of
the ground units and targets, 80 % of the salvage, half the secret and the secondary. Amounts in
**Act 1 terms** (the stat-block bounties: Dust Kite 4, Skitter 5, Needler 12, Stinger 15, Spore
Bomber 25, Burrower 24, Ravager 18, Creeper 22, Spine Turret 12, Polyp Mortar 15; the crate 100,
the secondary 80, the salvage 10), each paid × the act factor **2.56** (bounties also × the
level's scale, rounded per kill). A **`bounty_scale` of 0.26** (step PE15, from 0.42 with the busier
waves; M12 = a accepts the act step's low scale) puts the typical haul on the budget (`TypicalHaulTest`:
1,796, −0.5 %): paid per kill Dust Kite 3, Skitter 3, Needler 8, Stinger 10, Spore Bomber 17, Burrower 16,
Ravager 12, Creeper 15, Spine Turret 8, Polyp Mortar 10.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Dust Kite 84 × 4 + Needler 73 × 12 + Stinger 56 × 15 + Skitter 37 × 5 + Spore Bomber 4 × 25 + Burrower 27 × 24 + Ravager 8 × 18 + Creeper 3 × 22 | 2,148 | 1,403 |
| Ground targets: Polyp Mortar 4 × 15 + Spine Turret 10 × 12 + station containers 4 × small salvage 10 | 224 | 163 |
| Secret: drop pod (hidden crate, 6% of budget) | 256 | 128 |
| Secondary: clear the slopes | 205 | 102 |
| **Total** (bounty scale 0.26) | **2,833** | **1,796** |
| Budget(n) = the typical haul's target; typical -0 %, perfect 1.57 × budget | | 1,805 |
<!-- /data -->

Burrowers count at the ground rate (80 %) though a missed window lets one dive away: the
autopilot's real share is measured in part B (as Level 11's submerged jellies were); if it falls
well short, the Burrower's windows are retuned before the scale is raised. The crate is 14 % of
the budget at ×2.56 (Act 2's were 7–9 %): the act step's jump makes a 100 crate large; decided
in round 36 (Q.15c): keep 100 (Act 1 terms stay the same everywhere) and let the scale carry the rest.

**Balance plan** (to be extended to Act 3 before part B, M18 = a): the typical player arrives
with a Pulse Cannon L4, two Micro-missile Pods L2, a Tail Gun L2 and Rook's Autocannon
(`balance-plan.yaml`, L14); the L15 visit buys one EMP charge and no beam yet (the balance plan has it
at L2 at the L18 visit, on budget(n) income: the user's decision of 2026-10-14). Forward DPS 77 against the reference 76: the Burrower's 44 HP dies in 0.6 s of its
2 s window.

## Difficulty notes

- **Easy**: every group a fifth smaller (formation lever: Needler lines and Stinger streams of 2,
  227 enemies, ≈ 74 a minute, Act 1's pacing rule: one pause over 3 s), Burrower fields one smaller and the spec's easy hooks (ring of 8,
  2.4 s surfaced); both Needler pincers enter from the front as a line abreast, the first in two
  ranks of 2 at t=49 and 51 (step B3, accepted in round 38: the autopilot clears one line in
  under a second, leaving the screen empty over 3 s before the t=52.5 snake); an extra armour
  patch from the station's last container.
- **Hard**: every group a fifth larger (formation lever: Needler lines and Stinger streams of 4, 388
  enemies; plan fit 4.79 on the screen, 43.5 shots a minute, no pause over 2 s); the spec's hard hooks (ring of 12 and a second ring of 8, 1.8 s surfaced); the Creeper
  convoy has 4; a Spine Turret pair at the rail line (t≈185, beside it as it crosses the screen); the drop pod's rubble takes 6 hits.

## Concept art

Production straight away, as Act 2's levels did (M7 part B, step ART-B), for round 38; prompts and
briefs in [concept/prompts.md](concept/prompts.md), generators `tools/art/backdrop_l15.py` (from the
chosen [Olympus still](../../../world/mars/concept/mars-olympus-r36-a.png) and parallax r02-c) and
`tools/art/l15_props.py`. `concept/backdrop-proposal.yaml` is the generator's `backdrop` block for the
level's data. The briefing images are on the
[briefing review sheet](../../../ui/briefing/README.md#concept-art), the Ares Control portrait on the
[characters'](../../../story/characters/README.md#concept-art), the Burrower and Dust Kite on their units'.

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r38-a.png](concept/backdrop-final-r38-a.png) | Production backdrop: the tile sets (Mars from high up, the flank, the slopes, the escarpment, the dunes, cirrus, entry streaks, the deck banks and dust), the pieces (the transport's underside, the close-over bank, Lycus Survey Station, the escarpment cliff and rail pylons as towers, the drop pod's ledge, the crashed lander, the rail line) and 14 composites (sheet) | chosen (round 38, F.1: approved as final) |
| [concept/backdrop-final-r38-a.gif](concept/backdrop-final-r38-a.gif) | The launch under the transport and the deck's close-over while the ground takes over from the deep layer (motion) | chosen (round 38, F.1: approved as final) |
| [concept/l15-props-final-r38-a.png](concept/l15-props-final-r38-a.png) | Props: the station's supply containers (intact, damaged, wreck, break), the drop pod's rubble hit by hit and its beacon glow (sheet) | chosen (round 38, F.2: approved as final) |
| [concept/l15-props-final-r38-a.gif](concept/l15-props-final-r38-a.gif) | The same props in motion | chosen (round 38, F.2: approved as final) |
| [concept/level-15-capture-r38-a.png](concept/level-15-capture-r38-a.png) | Game capture, M7 part B (round 38): twenty frames of a whole medium run with the plan's L15 fit and Rook, no `--invulnerable` (the drop under the transport, the cloud deck's close-over, the first Burrower's bulge, ring and dive, a field under Dust Kites, Lycus Survey Station, the drop pod's rubble, the pincer, the dune field, the rail line, the debrief: 198 / 200, secret 1 / 1, A+) | chosen (round 38, K.1: accepted) |
| [concept/level-15-capture-r38-a.mp4](concept/level-15-capture-r38-a.mp4) | The same run as a video with the game's sound, t −1 to 232 (the level through the debrief into the hangar) | chosen (round 38, K.1: accepted) |
| [concept/level-15-end-r42-a.mp4](concept/level-15-end-r42-a.mp4) | Check capture of the level's end (round 42, medium, the test autopilot): the last 25 s and the hand-over to the debrief with the closing lines heard in full (39 s with sound) | chosen (round 42, K.d: accepted) |
| [concept/level-15-end-r42-a.png](concept/level-15-end-r42-a.png) | 20-frame sheet of that excerpt | chosen (round 42, K.d: accepted) |

## Implementation

- [x] Level data `data.yaml` (M7 part B, step B2) with the sections, waves, ground targets, radio, budget,
      threat profile, briefing and music; this README's tables become marked tables
- [x] Reference DPS 15–21 in `enemies/data.yaml` and the Act 3 balance plan (M18) before the data
      (M7 part A)
- [x] The descent: `deep` surface, entry streaks, the two cloud decks, the close-over while
      `ground` takes over at t≈61.5–64.5; the launch from the transport — steps ART-B (`backdrop_l15.py`), B2, B3
      (`launch: drop`, the eased sections) and B4; seen in round 38's capture (the deck covers the field
      t≈61.5–64.5, kept: round 38 R.3)
- [x] Burrower fields with the `buried` layer, the spot rule, the no-surface zones; the first
      field alone (t=68–73) — simulation steps B1 and B3 (`Level15Test`; the looks are step B4's)
- [x] Wave script: ≥ 55 enemies a minute at medium (`DensityTest`: 64.9), no empty screen over 3 s
      (`PacingTest`, a Burrower counting as on the screen in every phase), the pincers' edge warnings,
      the act HP factor for Ravager, Creeper and Spore Bomber — step B3
- [x] Waves to the Act 2+ rules (round 40 P.e): at most 3 pauses over 2 s at medium and hard (`PacingTest`),
      ≥ 2.5 enemies on the screen and ≥ 10 shots a minute with the plan's fit at medium (`ActFloorTest`: 2.68 and
      17.2), Needler and Stinger in `small_act_hp` (`ActHpFactorTest`) — step PE15
- [x] Ground targets: the nests, the mortars, Lycus Survey Station's growths and containers, the
      crashed lander's nest — steps B2, B3 (`Level15Test`: 10 turrets and 4 mortars at medium, 12 turrets on
      hard), ART-B (`l15_props.py`); seen in round 38's capture
- [x] Secret: the rubble trigger (4 hits, hard 6), the crate (100), the scanner's glint, its line — steps
      B2, B3 (`Level15Test`), ART-B, VOX-B (Ares Control's line); found in round 38's capture
- [x] Secondary `kill_ratio` 65 %, judged at the end, 80 in Act 1 terms — step B3 (`Level15Test`: the
      autopilot meets it on every difficulty)
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`, with and without
      Rook); the `first-surfacing` event; Ares Control cast and voiced; Rook's lines
      `requires: escort` — step VOX-B (Level 15's runs in `RadioTimelineTest`; Varga's t=33.5 line
      moved to 34.5)
- [x] Threat profile, Varga's four lines and Rook's teaser fitting the intel panel
      (`IntelPanelLayoutTest`); the Burrower's and Dust Kite's intel portraits — steps B2, ART-B
      (`intel.py`), B4 (the `BURROW_FIELD` mark)
- [x] Briefing: two one-screen pages with their images (`BriefingLayoutTest`) — steps B2, ART-B, VOX-B;
      seen in round 38's intro capture
- [x] Typical haul within ±5 % of 1,805 (`TypicalHaulTest`: 1,796 at `bounty_scale` 0.26, −0.5 %) — steps B3, PE15
- [x] Music: "Red Dust Run" base stem from section 2, full mix from section 3; the Mars ambience — step B4
      (track 8 in the game's `Tracks`; `mars-entry` = round 38's chosen entry roar a in sections 1–2, crossfading over
      2 s to the chosen `mars-dust` a at section 3; the transport drawn as a 75 % normal-blend overhead pass by its
      piece name, no data key)
- [x] Easy/hard variations as in *Difficulty notes* — steps B2, B3 (`Level15Test`: the rubble's hits, the
      hard turret pair, the autopilot on every difficulty)
- [x] Production art and captures for part B's round — step ART-B, the captures in step B5
      (`level-15-capture-r38-a`), in [round 38](../../../concept-rounds/round-38/README.md) (closed 2026-10-14:
      the art approved as final, the capture accepted)

## Open questions

- None open: round 36 answered every question (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the [Act 3](../README.md) roster to a draft
  level document. Taken as the recommended options, accepted in round 36 (2026-10-13): M7 = a
  (the `buried` layer), M10 = b (the Dust Kite as popcorn), M12 = a (a low `bounty_scale`), M17 = a
  (Rook flies the level), M18 = a (reference DPS 76), M19 (hand tables, no data). Our proposals:
  the five sections and speeds, the descent through two cloud decks with the close-over, 262
  enemies (≈ 85 a minute), the Burrower alone at t=68, Lycus Survey Station, the drop-pod secret,
  the 65 % kill-ratio secondary (80), Ares Control as a new speaker, the radio and intel texts,
  `bounty_scale` ≈ 0.36.
- 2026-10-12 (overnight, harmonised with the act README): density brought into the act's band of
  60–72 a minute (accepted in round 36 (2026-10-13) by smaller popcorn waves at the same
  cadence: Dust Kite streams 5 → 3, V-wings 7 → 4, snakes 6 → 4; Skitter streams 4 → 3, snakes
  6 → 4; 200 enemies (≈ 65 a minute, front 96 % · sides 4 %), the Burrower fields and the set pieces unchanged;
  `bounty_scale` 0.36 → 0.42 (typical 1,817, +0.7 %). The siege's length "three weeks" → "five
  weeks" (summary, briefing, Ares Control's t=12 line), the act's timeline of a nine-day crossing.
- 2026-10-13 (user, round 36): approved (D.2) with M7, M10, M12, M17 and M18 kept or accepted. Q.15
  all yes: Ares Control's radio portrait is the new colonial generic (C.1 = b, the dust-suit
  controller, teal channel), a submerged Burrower counts as on the screen for `PacingTest`, the
  drop-pod secret stays 100 in Act 1 terms, the Burrower's formation is `field`. Ares Control is cast
  (S.6 = b, Thomas A).
- 2026-10-14 (M7 part B, step B2; defaults accepted in round 38 (2026-10-14): the hand tables became
  [data.yaml](data.yaml) and marked tables. Choices made there: the backdrop block, the sections' tiles and the
  atmospheres are step ART-B's proposal (`concept/backdrop-proposal.yaml`): the entry glow is a `clear` peak
  at 0–8 s inside section 1's `light` (the plasma streaks as its wisps), the deck's close-over the opaque
  low-air piece `deck-close` round t=63 over the ground's swap line, the dust peak a `heavy` peak; ART-B's
  piece key `overhead_pass` (the transport at 75 % with normal blending) is left out until steps B3/B4 build
  it; the wave strip marks all
  six Burrower fields (68, 80, 98, 125, 153, 172 s; the threat profile's draft named four), derived from the
  waves of a burrowing unit; the no-surface zones over the station's pads, its hab, the rail line and the
  escarpment's cliff face (x ≥ 432 from t=115 to 150); the hard turret pair moved
  from t≈178 (≈ 1,100 px before the rail line) to t≈185, beside it; the
  Ares Control lines (and the secret's) show the colonial generic portrait; the ambience key `mars-entry` for
  the entry roar in sections 1–2, crossfading to `mars-dust` at section 3 over 2 s (the existing
  `ambience_changes`); the easy pincers are line abreasts of their own (`skip`), the fields' easy and hard sizes
  and the convoy's hard 4 come from the formation lever; the secret's line is in the *Secrets* text (the radio
  table lists the radio's cues). `bounty_scale` 0.42 kept until `TypicalHaulTest` fits it (step B3; typical
  1,800, −0.3 %).
- 2026-10-14 (M7 part B, step ART-B): production backdrop and props straight from `mars-olympus-r36-a`, review sheets `backdrop-final-r38-a` and `l15-props-final-r38-a` proposed for round 38. Accepted in round 38 (2026-10-14): the deep scroll factor 0.15, the entry streaks as the `clear` look's wisps (the entry-glow peak fades them), the transport drawn at 75 % with normal blending, the escarpment cliff as towers along the right edge (h 1.2), the road straight at x 300, the colony off-white containers, the drop pod and rubble design with Level 13's amber beacon, the deck fully white from t 62.0 to 64.0.
- 2026-10-14 (M7 part B, step B3; accepted in round 38 (2026-10-14): the simulation flies the level
  (content's `MarsRules`: the burrow and the swoop from the stat blocks, the moving sections' ease, the
  no-surface zones from each zone's `t`, `launch: drop`; a V-wing of Dust Kites flies down the screen).
  `bounty_scale` 0.42 confirmed by `TypicalHaulTest` (typical 1,800, −0.3 %). The autopilot (plan fit
  with Rook) kills every Burrower it sees surface: 27 of 27 on medium, 21 of 21 on easy, 32 of 32 on hard,
  none escaped, above the 80 % the haul assumes, so the windows need no retuning. On easy the t=49 line
  abreast of Needlers became two ranks of 2 at t=49 and 51 (`PacingTest`). The balance plan's L15 visit
  buys one EMP charge; the beam comes at L2 at the L18 visit (budget(n) income, the user's decision of
  2026-10-14).
- 2026-10-14 (M7 part B, step VOX-B): the radio, the drop pod's line and the two briefing pages voiced
  (Okafor, Varga, Rook, Ares Control); Varga's kite warning moved from t=33.5 to t=34.5, since Rook's
  t=23.5 line (escort only) held the radio as text until a second past it (`RadioTimelineTest`, with
  Level 15's runs: no voiced line late). Lycus is spoken as written (pending review in round 38).
- 2026-10-14 (M7 part B, step B5): captured (`level-15-capture-r38-a`: a whole medium run with the plan's fit and
  Rook, no `--invulnerable`, 198 / 200, A+) and the part's implementation items ticked; everything of part B goes
  to concept [round 38](../../../concept-rounds/round-38/README.md), accepted in round 38 (2026-10-14).
- 2026-10-14 (user, round 38): closed. C1 (Varga's kite warning at t=34.5) and C2 (easy's t=49 pincer as two
  ranks of 2 at t=49 and 51) accepted into this spec; the backdrop and props approved as final (F.1, F.2), so
  `art: final`; the capture accepted (K.1); the build choices B.1–B.3 all yes (the drop launch, the
  no-surface zones, the hard turret pair at the rail line, the `BURROW_FIELD` marks, `mars-entry`, the
  V-wing's gliding slots); R.2 (every surfaced Burrower killed by the autopilot, the windows not retuned)
  accepted; R.3 keep all: the deck's ≈ 3 s close-over (t≈61.5–64.5), the first surfacing at ≈ 72.5 and the
  level ending over the dunes rather than the rail line stay as built, the prose above now gives the
  measured values. The Burrower's bulge plays round 38's crack layer b over the eruption, its dive the
  grain slide b; the entry roar a and `mars-dust` a chosen.
- 2026-10-19 (step PE15; the user's round 40 P.e "all later levels" and "tougher, not more"; accepted in round 40 (2026-10-19)): the waves reworked to the Act 2+ rules. The Burrower fields, the set pieces (Spore Bombers, Ravager
  packs, the convoy), the pincers, the pickups' carriers and the radio keep their times; the first Kite stream
  moved from t=7 to t=5 (the launch ends at 5) and the t=21 Needler line to 22.5; between the anchors 33 firing
  groups were added (16 Needler lines of 3 and one of 4 over the rail line at 183, 16 Stinger streams of 3, the
  last at 185.5), six of the seven Kite V-wings (28, 45.5, 84, 112, 149.5, 177.5; 10.5 kept) became swarms of 6
  on authored paths, and two Skitter streams (87.5, 156.5) were dropped. The Needler and the Stinger joined `small_act_hp` (12 and 18 HP). Medium:
  200 → 306 enemies (≈ 65 → 99 a minute), 1.42 → 2.68 on the screen, 10.7 → 17.2 shots a minute, 17 → 1 pauses
  over 2 s; hard 251 → 388, 2.34 → 4.79 on the screen, 21.1 → 43.5 shots, 9 → 0 pauses; easy 152 → 227, 1 pause
  over 3 s. `bounty_scale` 0.42 → 0.26 (typical 1,796, −0.5 %; perfect 2,833).
- 2026-10-20: round 42 closed: the rework to the Act 2+ rules (R.a, kept) and the radio outro fix with its check capture (K.d, T.3) accepted by the user.
- 2026-10-20 (user, round 44, I.8): an Act 3 level is `implementation: done` when its round has closed and its checklist is ticked (Act 2's rule). Round 38 closed and every box is ticked: **`done`**. The act's playthrough stays an act-level item in the [Act 3 README](../README.md).
