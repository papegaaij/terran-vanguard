---
title: Level 18 – Dust Storm
design: approved
implementation: in-progress
art: chosen
depends-on: [../../../enemies/air/ghost-drone, ../../../enemies/ground/dust-devil, ../../../enemies/air/dust-kite, ../../../story/twist, ../../../player/systems, ../../../player/wingmen, ../../../world/mars]
updated: 2026-10-19
---

# Level 18 – Dust Storm

## Summary

A storm front rolls across the northern plains of Mars with Vrell inside it, and Aegis Wing sweeps
it before it reaches Ares Landing. Inside the storm the view shrinks to a radius round the ship
(M6 = a: a look only, bullets always visible, everything hittable). The level introduces the roaming
[Dust Devil](../../../enemies/ground/dust-devil/README.md) vortices, which pull the ship and the
pickups, and the grey, unmarked [Ghost Drone](../../../enemies/air/ghost-drone/README.md): Varga's
first sight of it is **hint 1** of the [twist](../../../story/twist/README.md) ("*That's not Vrell.
That signature is... ours?*"). The act's breather by threat, not by count (the user, 2026-10-19): no
boss, light firing groups and the calm eye, at the Act 2+ floor's density (≈ 100 enemies a minute, tougher rather than more: the beam era's HP). 183 s: clear plains, the storm front, the deep storm, a calm eye, the back wall and the clearing.

**Status: approved in concept round 36 (user, 2026-10-13).** Written in the overnight design
phase (2026-10-12) from the act roster and the gap analysis's recommended answers, all kept or
accepted in the round. M7 part E (step E2, 2026-10-19) moved the numbers into the level's
[data.yaml](data.yaml): the tables below are rendered from it, the waves re-planned to the Act 2+ floor
(the user's decision of 2026-10-19; see *Waves*), pending the user's review in round 41.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Okafor's page also
carries the private side of the Helix beat (M13 = a: the briefings and the hangar intel carry it,
the radio does not). Texts (approved in round 36):

<!-- data: briefing -->
> **Commander Okafor:** "A storm front is crossing the northern plains with Vrell inside it.
> Sweep it before it reaches Ares Landing. And Lancer: what Doctor Varga said about the Helix
> compound stays in this room."
>
> **Dr. Varga:** "In the dust you'll see glow, not shapes, past a few ship-lengths. The Vrell
> glow; bullets always show. Homing guns find what your eyes can't."
<!-- /data -->

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-18-storm-front`: an orbital weather view of the plains: the storm front as a
  rust wall with the calm eye, Ares Landing's domes at its southern edge, the sweep route through
  it.
- Varga's page `level-18-visibility`: her diagram of the storm's look: the ship in its clear
  radius, the ground fading into dust outside it, Vrell as glowing silhouettes, bullets crisp at
  any range.

*Hangar teaser* (shop screen before L18; Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "Sandstorm on the plains. Bring homing, or bring luck. I'm bringing both."
<!-- /data -->

## Threat profile

Rendered from the data; directions are counted by each wave's `from` (the intel's rule).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (popcorn, Needler and Stinger groups, Ghost Drones, Wraiths), `ground` and `low-air` (Dust Devils, which rise to `low-air` when open; mortar and turret nests) |
| Attack directions | front 93% · sides 4% · rear 3% |
| Density | 3 |
| Recommended traits | `homing`, `spread` |
| Hazards | Dust storm: visibility drops to a radius round the ship; Dust Devils pull the ship and the pickups |
| Boss / mid-boss | none |
| Sensor-suite detail | none: storm plains, `air` + `ground`, front · L1: + directions (all), density 3, hazard "dust storm, half blind", the objective `SWEEP THE STORM` and the secondary `KILL RATIO 65 %` · L2: + Dust Devil (new), Dust Kite, Skitter, Needler, Stinger, Mote Swarm, Wraith portraits, and a silhouette "unclassified, small, `air`" (the Ghost Drone stays unnamed before its first sight) · L3: + `homing` and `spread` highlighted, the wave strip marking the eye, 1 secret |
<!-- /data -->

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "A dust storm on the plains. You'll fly half blind." |
| L1 | "From every side, rear too. Ground vortices that pull." |
| L2 | "New: Dust Devils. And contacts I can't classify." |
| L3 | "Homing sees through dust. Spread for the kites." |

## Objective

- **Primary** `reach-end`: sweep through the storm to the clearing.
- **Secondary**: destroy at least **65 %** of all enemies (§1.7's kill-ratio rule), judged at the
  level's end. Pays **100** credits in Act 1 terms (256 in Act 3); the typical haul counts 50 %.
  The storm makes it a real stretch: the units outside the radius are hard to read, and `homing`
  closes the gap.

## Layout

Scroll **150 px/s** in the clear, **140 px/s** in the front, **120 px/s** in the storm (enemies are
seen late, so the scroll gives more time). Setting: [Mars](../../../world/mars/README.md)'s plains,
butterscotch sky, rust flats with dry channels, terraforming pipelines half buried, a militia rover
wreck. Motion budget: the dust (the veil and the streaks) is the one strong element; the Dust Devils'
swirls are the second; everything else stays quiet. The data eases each speed change over 2 s (the
sections' `ramp`); the scroll column includes the eases.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Visibility (medium) | Layers and content | Purpose |
|---|---|---|---|---|---|---|---|
| 1. Open Plains | 0–25 | 0–3,750 | 150 | clear, medium peak 20–25 | full | `deep`: rust plain to the horizon, the storm wall standing on it. `ground`: dry channels, a buried pipeline, wind ripples. `high-air`: the front's leading streaks from t=20. | Popcorn from t=6; the storm seen coming. |
| 2. Storm Front | 25–55 | 3,750–7,960 | 140 | medium | full → 170 px over t=28–36 | `high-air`: the dust wall passing over the screen from the top edge (t=28), streaks ≤ 40 %. `ground`: the plain dimming into dust outside the radius. | The radius ramps in; the Dust Devils alone (t=41.5). |
| 3. Deep Storm | 55–110 | 7,960–14,580 | 120 | heavy | 170 px | `ground`: as section 2 at full strength; a militia rover wreck half buried at x 60 (t≈90, the secret), its red beacon visible only inside the radius; the pipeline's spine-turret nests. | Ghost Drones (hint 1, t=69.5); the first rear entries. |
| 4. The Eye | 110–130 | 14,580–16,980 | 120 | light | 170 → full over t=110–114; full; full → 150 over t=130–134 | `deep`: the sky clear overhead, the storm's walls round the field. `ground`: a dry lake bed, a Polyp Mortar floe (t≈125). | A breather in clear air; the Dust Devil's core window learnt in plain sight. |
| 5. Back Wall | 130–170 | 16,980–21,780 | 120 | heavy | 150 px | `high-air`: the storm's back wall, denser than the front, streaks at 40 %. `ground`: the plain gone outside the radius. | Ghost Drone pincers and lines with Dust Devils; the hardest stretch. |
| 6. Clearing | 170–183 | 21,780–23,700 | 150 | light | 150 → full over t=170–176 | `ground`: the dust thinning, the plain and the domes of Ares Landing on the horizon. | Release; level end. |
<!-- /data -->

### Visibility in the storm (M6 = a)

A **look only**: the simulation does not know about the dust, so nothing changes what can hit
what (rejected b, the Darkness rule, and c, hiding bullets; the layer rule of
[art direction](../../../art-direction/README.md) never hides bullets). The values (approved in round 36; the look is MA.2's b):

| Element | Inside the radius | Outside the radius |
|---|---|---|
| `ground` and `low-air` (Dust Devils, nests, the rover, the plain) | drawn | **gone into the dust** (a rust veil); a Dust Devil's open core glows through faintly; a Dust Devil is under the veil in every phase (closed, rising and sinking on the ground, open on low-air), so it never shows over the dust while closed and vanishes as it opens (round 41 K.a) |
| `air` enemies | drawn | **fading gradually to silhouettes in the dust's own colour** through the falloff (round 41 R.b: seen through the dust they share its colour): a soft shape a few per cent denser than the veil around it, a trace of its shading, no detail; only its lights poke through: the Vrell's saturated teal, lime and crimson glow, the Ghost Drone's red sensor eye and white running lights (its glow frames) |
| Enemy bullets, mortar markers and every telegraph | drawn | **always drawn at full contrast** |
| Pickups | drawn | drawn with a faint glint (they are the player's to chase) |
| The ship, Rook, the player's shots | drawn | Rook (if he strays out) as such a silhouette with his red and green running lights; the player's shots drawn |
| HUD, edge warnings, the sensor suite's threat arrows (L2+) | above the dust | above the dust |

- **Radius**: centred on the ship, **170 px** in the deep storm, **150 px** in the back wall (easy
  200 / 180, hard 150 / 130); it follows the ship. **The edge** (round 36, MA.2 = b): a soft,
  ragged falloff over about 120 px around the radius, broken by two drifting noise fields (dust
  tongues), so the view fades into the storm rather than ending at a line. It eases over the times in
  *Layout* (8 s into the front, 4 s into and out of the eye, 6 s out over the clearing). The data's
  `visibility` block holds the radii, the ramps by level time, the falloff and the easy/hard radii
  (presentation only, as `flak`; `LevelData.Visibility.radiusAt`).
- The Dust Devil's pull (130 px) always lies inside the radius, so what pulls the ship is always
  seen.
- **Everything is hittable** wherever it is: homing's edge is that it locks onto what the player
  cannot read well, not that it alone can hit. A kill outside the radius shows its burst through
  the dust.
- **Code fit**: Level 06's darkness and Level 13's whiteout are the patterns
  (`HazardSpec.Darkness`, the whiteout's `AtmosphereRamp`): a screen-space mask in presentation,
  after the `low-air` layer and before `air`, with the air units' silhouette pass.
- **Rook** flies as anywhere else (his AI reads the simulation, not the look).

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*).

- **Act HP factor** from the reference DPS 88 at L18 (M18 = a, round 36): **Wraith**
  (L10, 66) ×1.33 → 21 HP; the Needler 12 and the Stinger 18 (Level 18 joins the basis's
  `small_act_hp`, capped at ×3: the user's decision of 2026-10-19, round 40 P.e).
- **Beam-era HP** (the user's "tougher, not more" of 2026-10-19; the
  [balancing basis](../../../enemies/README.md#balancing-basis), `beam_era_hp`): from Level 18, the plan's first
  Ion Beam, every `small` unit's HP ×2: the **Needler 24**, the **Stinger 36**, the **Ghost Drone 28**, the
  **Spine Turret 16** and the **Polyp Mortar 20**, so they live long enough to shoot back (a killed small unit
  lives 2.05 s on the screen with the plan's fit at medium, Level 10 1.60 s; `BeamEraHpTest`). The Dust Kite,
  Skitter and Mote Swarm (`tiny`) keep 1 HP; the Dust Devil (new here, `medium`) keeps 52.
- **Density and pacing** (the user's decision of 2026-10-19: the Act 2+ rules, `PacingTest`'s Act 2
  rule and `ActFloorTest`'s 2.5 enemies on the screen and 10 enemy shots a minute with the plan's fit
  at medium; and "tougher, not more": the beam era's HP instead of step E4's 605 enemies): **301 enemies**
  (waves 277 + ground targets 24) over the 178 s from the launch, **≈ 101 a minute** at medium (easy 231,
  hard 294). The approved table's anchor waves keep their times (the Dust Devils at
  41.5/77/116.5/138.5, the Ghost Drones at 69.5/87.5/131.5/145.5/166.5, the rear entries at
  52/94.5/152.5, the Needler pincers at 80.5/159.5) and the radio beats; between them E2's wave about
  every 2.5 s (the eye included) of firing, light groups: Needler line abreasts of 4–5, Stinger V-wings
  of 4–5, Stinger V-wings and streams of 3 where E2 flew short Dust Kite V-wings and streams, Dust Kite
  swarms of 6–7 and snakes, Skitter streams and snakes, four more light groups (a Stinger V or a Needler
  line of 3) in the gaps over 2.5 s, and the spine-turret and mortar nests along the pipelines (the hint
  nest 5 turrets). The level stays the act's breather by threat (no boss, popcorn-weight groups, the
  eye in clear air), not by count. Hard: the firing groups one unit smaller (as Act 2's reworks); easy
  thinned by the formation lever. Every side and rear wave warned 3 s ahead at its edge (the warnings
  draw above the dust). With the plan's fit at medium: **2.77 enemies on the screen and 46.5 enemy shots a
  minute**, two pauses over 2 s (t=25.8–28.7 before the front, the clearing); hard 2.93 and 77.5, one pause;
  easy 1.26 and 15.8 (Act 1's rule: one pause over 3 s); pending the user's review in round 41.
- **Quiet windows**: no other wave for 3.5 s after the first Dust Devils (t=41.5) and after the first
  Ghost Drones (t=69.5). The Ghost Drones' first sight keeps Vrell air off the screen (their spec):
  the grey machines read as a different force; a spine-turret nest on the ground (t≈69) keeps the
  screen from emptying if the three drones die fast.
- **Hint 1** is the t=69.5 line abreast: three drones drop into their lanes 120–220 px below the top
  edge, inside or near the radius, strafe and burst. Varga's line starts as they bank (t=71).

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (left) | Clear sky; the storm wall on the horizon |
| 8.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 11 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | front (right) | |
| 13.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 16 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 18.5 | 1 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 6 | front | |
| 21 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 23.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 26 | 2 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | The storm front reaches the top edge at t=28 |
| 28.5 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | In the ramp |
| 31 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 33 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Teal glow in the thickening dust |
| 35 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 37.5 | 2 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | Gone before the Dust Devils' quiet window |
| 39.5 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 41.5 | 2 | pack | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 2 | front | **New.** A pair, alone until t=45 (ground, cycles 0.8 s apart); Varga's line at t=41 |
| 45 | 2 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 6 | front | |
| 47.5 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 50 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 52 | 2 | line abreast | [Skitter](../../../enemies/air/skitter/README.md) | 3 | rear | The first rear entry, warned 3 s ahead; Rook's bark at t=49 |
| 54.5 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 57 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (alternating edges) | Radius 170 from here |
| 59.5 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 62 | 3 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | |
| 64 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (left) | |
| 65.5 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (alternating edges) | Gone before the Ghost Drones' quiet window |
| 67.5 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 69.5 | 3 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | **New, hint 1.** Alone until t=73.5, no Vrell air on the screen (a spine-turret nest on the ground); Varga's line at t=71, Okafor's at t=78 |
| 73.5 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (alternating edges) | |
| 75 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 77 | 3 | pack | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 2 | front | Pull with the kites crossing |
| 78.75 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 80.5 | 3 | pincer | [Needler](../../../enemies/air/needler/README.md) | 4 | sides | 2 per edge; warned 3 s ahead |
| 83 | 3 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 7 | front | |
| 85.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 87.5 | 3 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 4 | front | Rook's line at t=86 |
| 90 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Over the buried rover (the secret) |
| 92.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 94.5 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Cloaked: `homing` hits them before the decloak; warned 3 s ahead |
| 96.5 | 3 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (right) | |
| 99 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 101.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front | The leader drops an overdrive |
| 104 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 106.5 | 3 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 7 | front | |
| 109 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 111.5 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | **The Eye**: radius opens over 4 s; a breather |
| 114 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 116.5 | 4 | single | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 1 | front | In clear air: the core window learnt |
| 119 | 4 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (alternating edges) | The supply drone's armour patch at t=118 |
| 121.5 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 124 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 126.5 | 4 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 6 | front | |
| 128.5 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (right) | The back wall reaches the top edge at t=130 |
| 130 | 5 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 131.5 | 5 | pincer | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 4 | sides | 2 + 2 at their lanes; warned 3 s ahead; radius 150 |
| 133.5 | 5 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | |
| 136 | 5 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 138.5 | 5 | pack | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 3 | front | Three roaming, cycles offset 0.8 s; the last drops a shield cell |
| 141 | 5 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (alternating edges) | |
| 143 | 5 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 145.5 | 5 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 4 | front | Splits half left, half right; Rook's line at t=146 |
| 148 | 5 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 6 | front | |
| 150 | 5 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Hard only: an extra Wraith pair from the rear, warned |
| 150.5 | 5 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 152.5 | 5 | line abreast | [Skitter](../../../enemies/air/skitter/README.md) | 3 | rear | Warned 3 s ahead |
| 155 | 5 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 157.5 | 5 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 159.5 | 5 | pincer | [Needler](../../../enemies/air/needler/README.md) | 4 | sides | 2 per edge; warned 3 s ahead |
| 162 | 5 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | |
| 164 | 5 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 166.5 | 5 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | |
| 169 | 5 | stream | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front (alternating edges) | The dust thins over t=170–176 |
| 171.5 | 6 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 173.5 | 6 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 176 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | The last wave |

Totals: Stinger 77 · Needler 72 · Skitter 34 · Dust Kite 54 · Mote Swarm 12 · Dust Devil 8 · Ghost Drone 18 · Wraith 2.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 1 | Spine Turret pair on the half-buried pipeline (t≈8) | Bounty 12 each |
| 2 | Spine Turret pair on the pipeline in the front (t≈32) | Bounty 12 each; their shots always visible |
| 2 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) pair in a dry channel (t≈47), in the ramp | Bounty 15 each; their markers always visible |
| 3 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×5 on the pipeline (t≈69), on the screen through the hint-1 window | Bounty; the screen is never empty while the Ghost Drones fly alone |
| 3 | Spine Turret nest ×3 on a pipeline ridge (t≈88), seen only inside the radius | Bounty 12 each; their shots always visible |
| 3 | Militia rover wreck (`ground` trigger, 20 HP, t≈90, x 60) | The secret: frees a crate worth 100 (256) |
| 3 | Polyp Mortar pair in the deep storm (t≈103) | Bounty; their markers always visible |
| 4 | Polyp Mortar floe ×3 on the dry lake bed (t≈125), in the eye | Bounty |
| 5 | Spine Turret pair on the pipeline in the back wall (t≈149) | Bounty |
| 5 | Spine Turret nest ×3 (t≈160), in the back wall | Bounty |
<!-- /data -->

The Dust Devils are waves (they roam: a pair or a trio a `pack`, each wandering round its own path,
the path the centre line); the budget counts them with the kills.

## Hazards

- **The dust storm** (above): the radius; the streaks on `high-air` (≤ 40 %, never over the bullets).
- **Dust Devil pull**: within 130 px a drift toward the centre (35 px/s, easy 25, hard 45), never a
  trap against the ship's speed; pickups within 170 px are drawn in and held until the devil dies.
- No terrain collision; no wind push in this level (the Dust Devil is the only pull, and Level 12's
  gusts stay in Act 2).

## Secrets and pickups

- **Militia rover wreck** (the secret, t≈90, x 60, the left edge of the deep storm): a buried
  six-wheeled rover whose red distress beacon flashes only inside the radius, so a player must fly
  to the edge to see it (the Salvage scanner's glint shows it at any range, through the dust). A
  `ground` trigger of **20 HP**; destroyed, its cargo pod pops as a hidden crate worth **100** in
  Act 1 terms (256). Its line, Varga: "*Militia supply pod. Someone was out here before the storm.*"
  (an event line).
- **Overdrive**: dropped by the t=101.5 Stinger leader.
- **Armour patch**: an Ares militia supply drone (the [CDF supply drone](../../../allies/README.md#cdf-supply-drone)'s
  presentation) releases one at (240, 380 y-up) at t=118, in the eye.
- **Shield cell**: dropped by the last Dust Devil of the t=138.5 swarm at its death, with whatever
  pickups the swarm held.

## Radio chatter

Spoken radio, retimed to the **1 s rule** (as Level 17: 5 s for the last page plus 3 s for each page
before it, text at 30 characters a second, 0.4 s of silence; no timed line more than 1 s late). The
times start every line after the one before has closed (as text). Event lines drop after 6 s. Rook's
lines carry `requires: escort`; his one dry joke is at t=14.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Storm front on the northern plains, Lancer. Vrell are moving inside it. Sweep it before it reaches Ares Landing." |
| t=14 | Rook | "Can't see a thing in there. Sounds like home." |
| t=22 | Varga | "The front hits in a few seconds. Past your wingtips you'll see glow, not shapes. Bullets always show." |
| t=41 | Varga | "Vortex organisms. They pull. Shoot the core when the spin opens." |
| t=49 | Rook | "Six o'clock, Lancer! Coming up behind!" |
| t=56 | Varga | "We're in the thick of it. Trust the glow and the warnings." |
| t=71 | Varga | "That's not Vrell. That signature is... ours?" |
| t=78 | Okafor | "Later, Doctor. Eyes on the storm." |
| t=86 | Rook | "Grey ones fire straight. Watch for the red eye." |
| t=93 | Varga | "Cloakers at your back. Homing finds them before they show." |
| t=111.5 | Varga | "We're in the eye. Clear sky for a minute. Enjoy it." |
| t=127.5 | Okafor | "Back wall of the storm ahead. It's thicker than the first." |
| t=146 | Rook | "Grey drones on both flanks. Same red eyes." |
| t=168 | Okafor | "Clear air ahead, Lancer. Bring it home." |
| Secondary met (at the end) | Okafor | "Clean sweep. Nothing in that storm will reach the domes." |
| Level end | Okafor | "The front is breaking up over the plains. Come home, Aegis." |
<!-- /data -->

Timeline check (text): the longest lines close at ≈ 13.1 (t=1) and ≈ 33.8 (t=22); Varga's hint
line closes at ≈ 77.9 and Okafor's at ≈ 84.5; nothing overlaps. **Hint 1** follows M13 = a: in the
level only Varga's line and Okafor's cut; Rook's t=86 bark is tactical (he does not take up the
question). The roster's exact line is kept. Okafor's cut is varied here ("Later, Doctor. Eyes on
the storm.") so that her "Not on this channel, Doctor." bookends the Helix beat in Levels 17 and 19
without wearing thin ([Act 3 voice](../README.md#voice)).

## Music & ambience

The Act 3 B theme (track 9 in the [track list](../../../audio/music/README.md#track-list), the
"Grey Signal", D minor dub techno, round 36's S.1 = b), with **Vorne's motif as a faint layer**: the
plains wind alone in section 1, the **base stem from section 2** (the front), the **motif layer from
the Ghost Drones' first sight** (t=69.5, the first hint in the music as in the
[music](../../../audio/music/README.md) plan), the base stem without the motif in the eye, the **full
mix with the motif in section 5**, out over the clearing. Ambience: the `mars-storm` bed (the
dust-storm bed chosen in round 36, S.5 = a "recorded front": a sandstorm over a furious low howl) from
t=25, swelling in the walls and dropping to a light wind in the eye. The data: `start_section: 2`,
`full_section: 5`, the base stem in sections 4 and 6 (`stems`), the motif layer `motif: {from: 69.5,
sections: {4: false}}` (off in the eye), the ambience `mars-dust` (the plains wind) in section 1 and the
eye and the clearing, `mars-storm` crossfading in at sections 2 and 5 (`ambience_changes`, 3–4 s); the
walls' swell (+3 dB) is the game's (step G1). New sounds this level needs: the
Dust Devil's spin, its opening and its grit spiral; the Ghost Drone's descent whine, burst and
mechanical death; listed in [sfx](../../../audio/sfx/README.md) by the act's audio step.

## Credit budget

Budget(18) = 700 × 1.07¹⁷ ≈ **2,211** ([economy](../../../systems/economy/README.md#per-level-budget)).
Data in **Act 1 terms** (Dust Kite 4, Skitter 5, Needler 12, Stinger 15, Mote 2, Ghost Drone 14,
Wraith 30, Dust Devil 26, Spine Turret 12, Polyp Mortar 15, the crate 100, the secondary 100), × the
act factor **2.56**, bounties × the scale (the beam era raises HP, not bounties). With the waves at the
Act 2+ floor (≈ 101 a minute with the beam era's HP; the user's decisions of 2026-10-19) a **`bounty_scale`
of 0.37** (from round 36's 0.92 for ≈ 50 a minute, E2's 0.51 for ≈ 89 and E4's 0.24 for ≈ 204) puts the
typical haul at **2,186 (−1.1 %)**, inside M12's 0.3–0.5 expectation (0.38 steps to +4.3 % as the rounded
bounties change); `TypicalHaulTest`, pending the user's review in round 41.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Stinger 77 × 15 + Needler 72 × 12 + Skitter 34 × 5 + Dust Kite 54 × 4 + Mote Swarm 12 × 2 + Dust Devil 8 × 26 + Ghost Drone 18 × 14 + Wraith 2 × 30 | 2,770 | 1,702 |
| Ground targets: Spine Turret 17 × 12 + Polyp Mortar 7 × 15 | 285 | 228 |
| Secret: rover wreck (hidden crate, 5% of budget) | 256 | 128 |
| Secondary: clean sweep | 256 | 128 |
| **Total** (bounty scale 0.37) | **3,567** | **2,186** |
| Budget(n) = the typical haul's target; typical -1 %, perfect 1.61 × budget | | 2,211 |
<!-- /data -->

**Balance plan**: the plan's L18 visit comes from part A's Act 3 balance plan (M18); the level
expects `homing` (the Hornet Launcher or the Micro-Missile Pod) and is flyable without it.

## Difficulty notes

- **Easy**: radius 200 px in the deep storm, 180 in the back wall; the Ghost Drones' 2-shot burst
  and the Dust Devils' longer window and weaker pull (their specs); the formation lever.
- **Hard**: radius 150 / 130; the Ghost Drones' 4-shot burst and the second pass of surviving
  line-abreast drones; the Dust Devils' 3-arm spiral and 45 px/s pull; an extra Wraith rear pair at
  t≈150 (warned); the Wraiths' hard hooks from their spec.

## Concept art

Production art of M7 part E's round 41 (step ART-E), pending the user's review. Prompts:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r41-a.png](concept/backdrop-final-r41-a.png) | Level 18 backdrop final (`tools/art/backdrop_l18.py`): the play field at twelve times with the storm look b at the README's radius, stand-in units; the main pieces and the storm textures (sheet) | chosen (round 41); fixed by step FXS (2026-10-19), pending the user's look (F.c: a horizontal line where the dust ends at about 5 s; step FXS) |
| [concept/backdrop-final-r41-a.gif](concept/backdrop-final-r41-a.gif) | Level 18 backdrop final: the whole level flown at 0.5x with the storm look, 10 fps | chosen (round 41); fixed by step FXS (2026-10-19), pending the user's look (F.c: a horizontal line where the dust ends at about 5 s; step FXS) |
| [concept/l18-props-final-r41-a.png](concept/l18-props-final-r41-a.png) | The rover-wreck secret (`tools/art/storm_units.py`): the half-buried militia rover (intact, hit, destroyed), its red beacon, seen in / at the edge of / outside the radius, the pod popping (sheet) | chosen (round 41: approved as final) |
| [concept/l18-props-final-r41-a.gif](concept/l18-props-final-r41-a.gif) | The rover scrolling into the radius, shot open, the crate popping free, 20 fps | chosen (round 41: approved as final) |
| [concept/level-18-capture-r41-a.png](concept/level-18-capture-r41-a.png) | Game capture (step E6): twenty frames of a medium run with the plan's L18 ship and Rook (`--invulnerable`) and a no-fire run showing the Dust Devils' pull, the debrief last | proposed (round 41); K.a fixed by step FXS (2026-10-19), pending the user's look (the Dust Devil vanishes for a second at 24 s; step FXS) |
| [concept/level-18-capture-r41-a.mp4](concept/level-18-capture-r41-a.mp4) | Game capture (step E6): the whole medium run with sound (t −1 to 214), then the pull run (t 39–49) | proposed (round 41); K.a fixed by step FXS (2026-10-19), pending the user's look (the Dust Devil vanishes for a second at 24 s; step FXS) |
| [concept/level-18-fixes-r41-b.png](concept/level-18-fixes-r41-b.png) | Game capture (step FXS) after round 41's fixes, eight frames: the front passing with its top faded out (F.c), the Dust Devils opening out of the dust at the radius's edge and open inside it (K.a), Needlers and Ghost Drones outside the radius as dust-coloured silhouettes with their lights (R.b) | proposed (round 41 follow-up) |
| [concept/level-18-fixes-r41-b.mp4](concept/level-18-fixes-r41-b.mp4) | Game capture (step FXS): the front passing (t 27–34), the Dust Devils and the silhouettes (t 75–91), no sound | proposed (round 41 follow-up) |
| [concept/level-18-crops-r41-a.png](concept/level-18-crops-r41-a.png) | Readability crops from the capture at 1:1 and 2×: inside the radius, outside it, the back wall at 150 px | proposed (round 41) |

The level needs (part E's production-art round; the mood comes from the storm-plains
mood still and the chosen radius look **b**, the soft ragged falloff, in
[mars](../../../world/mars/README.md), round 36 MA.2 and MA.4):

- the production backdrop (six sections: the plains and the storm wall, the front, the deep storm,
  the eye's dry lake, the back wall, the clearing with the domes on the horizon) and the dust veil
  and streak layers;
- the visibility look in the game: the radius mask, the air units' silhouette pass with their
  glows, the Ghost Drone's eye and running lights at range;
- the rover wreck prop with its beacon; the two briefing images; the intel portraits of the Dust
  Devil and the Ghost Drone, and the "unclassified" silhouette for L2;
- the round's captures: the level at medium with the plan's fit, the hint-1 moment with its radio,
  a Dust Devil's pull and window in the dust, readability crops inside and outside the radius at
  1:1 and 2×.

## Implementation

- [x] Level data `data.yaml` (when the level is built, part E): sections and speeds with eases,
      the visibility ramp per section, waves, ground targets, radio, budget, threat profile,
      briefing, backdrop, music (step E2: all in; step E4 merged ART-E's backdrop block; step E5 the waves
      rebuilt for the beam-era HP)
- [x] Dust-storm visibility (M6 = a): the radius mask (170 / 150 px, easy 200 / 180, hard 150 / 130,
      the soft ragged ~120 px falloff with drifting dust tongues, MA.2 = b) following the ship, `ground` and `low-air` hidden outside it, `air` units as
      silhouettes with their glow, bullets, markers, telegraphs, pickups, Rook, the HUD and the
      warnings always drawn; the ramps; nothing in the simulation changes (step EG1: `StormLooks`,
      `StormLooksTest`, `PassOrderTest`; in the capture, where the air units outside the radius keep most of
      their detail against the light veil: round 41)
- [x] Hint 1: the t=69.5 Ghost Drone line abreast alone with no Vrell air, Varga's line and Okafor's
      cut; the Vorne motif layer from that moment (steps E2-E5, EG1: `Level18Test`'s quiet windows by wave
      times, `Level18MusicTest`, `RadioTimelineLevel18Test`; the capture shows the t=67.5 Needler line still on
      the screen through the window: round 41)
- [x] Wave script to the Act 2+ rules (the user, 2026-10-19): `ActFloorTest` (2.5 on the screen, 10
      shots a minute with the plan's fit at medium) and `PacingTest`'s Act 2 rule (no level left in
      `PENDING_ACT_TWO_RULE`), the quiet windows, edge warnings for every side and rear wave
- [x] Ground targets, the rover secret (20 HP trigger, the beacon only inside the radius, crate 100),
      the supply drone's armour patch at t=118, the overdrive, the Dust Devil's held pickups and the
      shield cell (steps E1, E2, E4: `Level18Test`, `PullTest`; the held patch released in the capture)
- [x] Kill-ratio secondary at 65 %, judged at the level end (§1.7)
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel18Test`, with Rook and without:
      the rover wreck early, late and between Rook's and Varga's lines, the secondary at the end), Rook's
      lines `requires: escort`
- [x] Beam-era HP (step E5, the user's "tougher, not more"): `small` units ×2 from Level 18
      (`beam_era_hp`), the waves back to ≈ 101 a minute at the floor (`BeamEraHpTest`, `ActFloorTest`,
      `PacingTest`, `ActHpFactorTest`)
- [x] Threat profile with the "unclassified" L2 silhouette, Varga's four intel lines and Rook's
      teaser fitting the intel panel (`IntelPanelLayoutTest`; step EG2's unclassified intel)
- [x] Briefing: Okafor's page (with the Helix line) and Varga's, each one screen
      (`BriefingLayoutTest`), with their images (ART-E's `level-18-storm-front`, `level-18-visibility`)
- [x] Typical haul within ±5 % of budget(18) = 2,211 with the level's `bounty_scale` (`TypicalHaulTest`)
- [x] Music: track 9's stems and the motif layer by section; the `mars-storm` ambience and its swell
      (steps MUS-E, EG1, EG2: `Level18MusicTest`, `Level18SoundsTest`)
- [ ] Easy/hard variations; the autopilot flies the level with Rook on every difficulty
      (`--escort none` too) (the variations are in; `Level18Test`'s autopilot flies every difficulty with Rook,
      without a retry; without him it is not flown)
- [x] Production art and captures for part E's round (steps ART-E and E6; round 41)

## Open questions

- None open: round 36 answered every question (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the Act 3 roster to a draft level document.
  Taken as the recommended options, pending the user's review in round 36: M6 = a (the storm is a
  look: ground and `low-air` gone and air units dimmed outside a radius, bullets always visible,
  everything hittable), M10 = b (the Dust Kite as the act's popcorn), M12 = a, M13 = a (Varga's line,
  Okafor's "Not on this channel, Doctor.", the private part on her briefing page), M17 = a, M18 = a
  (reference DPS 88), M19 (hand tables). Our proposals: the six sections with the eye as a breather
  in clear air; the radius values and ramps; pickups drawn with a glint outside the radius; the
  t=69.5 first sight kept apart from Vrell air; the Vorne motif layer from hint 1; the kill-ratio
  secondary; the rover-wreck secret seen only inside the radius; 149 enemies (≈ 50 a minute, the
  breather) and `bounty_scale` 0.92 for a typical 2,201.
- 2026-10-12 (overnight, harmonised with the act README): Okafor's t=78 cut varied to "Later,
  Doctor. Eyes on the storm." (closes ≈ 84.5, before Rook at t=86), so "Not on this channel,
  Doctor." stays a refrain for Levels 17 and 19; the breather's ≈ 50 a minute and its scale kept
  (the act's breather band 48–55); the music already matches the act (track 9, Vorne's motif from
  t=69.5, off in the eye, back in the back wall).
- 2026-10-13 (user, round 36): approved (D.5), M6 = a kept (a look only). **MA.2 = b**: the radius
  has a soft, ragged falloff over about 120 px around its value, broken by drifting dust tongues,
  units fading gradually to silhouettes (in place of the 40 px soft edge); a (a hard 10 px edge)
  rejected. Q.18 all yes: the radius 170 / 150 px (easy 200 / 180, hard 150 / 130) checked in part
  E's captures, pickups outside it drawn with a glint, `bounty_scale` 0.92 kept, one Helix sentence
  on Okafor's briefing page. The `mars-storm` bed is the recorded front (S.5 = a); track 9 is "Grey
  Signal" (S.1 = b); the Ghost Drone's eye and running lights are added to its sprite (U.5).
- 2026-10-19 (M7 part E, step E2): the level's `data.yaml` written and the tables marked. The user's
  decision of 2026-10-19: Level 18 meets the Act 2+ pacing and floor rules and joins `small_act_hp`
  (Needler 12, Stinger 18), staying the breather by threat, not by count. Built as the gap check's
  defaults, pending the user's review in round 41: the anchor waves kept and filled to a wave about every
  2.5 s with firing groups, kite swarms and nests (264 enemies, ≈ 89 a minute; `bounty_scale` 0.51 for a
  typical 2,228); a spine-turret nest on the ground in the hint-1 window; the Needler pincers 2 per edge;
  the Skitters' rear entries line abreasts from the rear; the Dust Devils' pairs and trio `pack`s on
  their own paths; hard's firing groups one smaller; the eye's motif off and the base stem over the eye
  and the clearing; `mars-dust` in section 1, the eye and the clearing; the radius ramp out over the
  clearing 6 s (t=170–176). The backdrop is a placeholder (Level 17's images) until step E4 merges
  ART-E's.
- 2026-10-19 (M7 part E, step E4): ART-E's backdrop block merged (tile sets `north-plain`, `storm-plain`,
  `dry-lake`, the seams, pipelines and ridges under the nests, the eye's walls, the front and the back wall
  passing over); the high-air factor 2.0 as ART-E rendered it (E2's placeholder had 2.2), pending the user's
  review in round 41.
- 2026-10-19 (M7 part E, step E4): the Dust Devil and the Ghost Drone mapped (`StormRules`; the grit spiral
  turns clockwise on the screen with the vanes' spin) and the level flown (`Level18Test`). E2's waves flew
  1.05 enemies on the screen with the plan's fit; three tuning passes (the fill groups one or two larger, a
  light firing group or kite V midway between every two waves at least 2 s apart, the quiet windows kept, the
  hint nest 5 turrets) reach 3.27 on the screen and 22.9 shots a minute at medium: 605 enemies (≈ 204 a minute)
  and `bounty_scale` 0.24 (typical 2,136, −3.4 %). The intel's hazard reads "dust storm, half blind" and the
  setting "Mars, northern plains" ("low visibility" overflowed the panel). All pending the user's review in
  round 41.
- 2026-10-19 (user): the beam era's density is "tougher, not more": raise the enemies' HP for the beam era so
  units live long enough to shoot back, bring Level 18 back to about 90–110 enemies a minute still meeting the
  floor, and let later levels follow the same rule.
- 2026-10-19 (M7 part E, step E5): built as the basis's **beam-era HP** (`beam_era_hp`: ×2 on `small` units
  from Level 18, the plan's first Ion Beam), set so a killed small unit lives as long on the screen as on
  Level 10 (E2's waves: 0.9 s → 1.5 s, Level 10 1.6 s; `BeamEraHpTest`). The waves rebuilt from E2's plan
  (E4's fill waves dropped): Needler lines and Stinger V-wings one larger, E2's short kite V-wings and
  streams Stinger groups of 3, four light groups in the widest gaps; 301 enemies (≈ 101 a minute), 2.77 on
  the screen and 46.5 shots a minute at medium (hard 2.93 and 77.5), `bounty_scale` 0.37 (typical 2,186,
  −1.1 %). The anchors, quiet windows, the eye, the radio and the hint nest kept; the autopilot flies every
  difficulty without a retry. Pending the user's review in round 41 (the shots a minute are about twice
  Level 10's 22: the price of units that shoot back).
- 2026-10-19 (user, round 41; step FXS): **F.c** the straight line at 5 s in the backdrop loop was the backdrop's own: the
  high-air `storm-front` piece (and the `storm-back-wall`) ended at its top row at up to 20 % opacity, a hard edge passing
  down the screen in the game too (t ≈ 31, fainter under the game's veil); `front()` in `tools/art/backdrop_l18.py` now
  thins the dust out to nothing over the piece's top third, broken by the noise, and both pieces and the review were
  rendered again. **K.a** the Dust Devil at 24 s into part 2 (t ≈ 79) was drawn with the flyers while closed (its
  vortex phase is not the ground phase, so the renderer took its `ground` layer for `air`): over the veil, as a dark
  silhouette, then gone into the dust as it opened on `low-air`; a closed, rising or sinking devil is now drawn with the
  ground units, under the veil like its open self (`VortexLooksTest`). **R.b** the air silhouettes take the veil's own
  colour at their place (the veil and its drift layer), 0.82 of it with 0.12 of the unit's luminance as a trace of its
  shading, the shade fading into the plain dust over 2 texels round the edge, covering the unit whole; only saturated
  bright pixels (the Vrell's light) poke through, a pale or white part (a Needler's legs) goes into the dust, the
  Ghost Drone's eye and tips come back with its glow frames (`StormLooksTest`). Pending the user's review
  (`concept/level-18-fixes-r41-b`).
