---
title: Level 18 – Dust Storm
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/air/ghost-drone, ../../../enemies/ground/dust-devil, ../../../enemies/air/dust-kite, ../../../story/twist, ../../../player/systems, ../../../player/wingmen, ../../../world/mars]
updated: 2026-10-13
---

# Level 18 – Dust Storm

## Summary

A storm front rolls across the northern plains of Mars with Vrell inside it, and Aegis Wing sweeps
it before it reaches Ares Landing. Inside the storm the view shrinks to a radius round the ship
(M6 = a: a look only, bullets always visible, everything hittable). The level introduces the roaming
[Dust Devil](../../../enemies/ground/dust-devil/README.md) vortices, which pull the ship and the
pickups, and the grey, unmarked [Ghost Drone](../../../enemies/air/ghost-drone/README.md): Varga's
first sight of it is **hint 1** of the [twist](../../../story/twist/README.md) ("*That's not Vrell.
That signature is... ours?*"). The act's breather: ≈ 50 enemies a minute (§1.7 allows down to 44).
183 s: clear plains, the storm front, the deep storm, a calm eye, the back wall and the clearing.

**Status: approved in concept round 36 (user, 2026-10-13).** Written in the overnight design
phase (2026-10-12) from the act roster and the gap analysis's recommended answers, all kept or
accepted in the round. The numbers are still hand-written tables (M19: no `data.yaml` yet); M7
part E moves them into the level's data and turns these tables into marked ones.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Okafor's page also
carries the private side of the Helix beat (M13 = a: the briefings and the hangar intel carry it,
the radio does not). Texts (approved in round 36):

> **Commander Okafor:** "A storm front is crossing the northern plains with Vrell inside it. Sweep
> it before it reaches Ares Landing. And Lancer: what Doctor Varga said about the Helix compound
> stays in this room."
>
> **Dr. Varga:** "In the dust you'll see glow, not shapes, past a few ship-lengths. The Vrell glow;
> bullets always show. Homing guns find what your eyes can't."

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-18-storm-front`: an orbital weather view of the plains: the storm front as a
  rust wall with the calm eye, Ares Landing's domes at its southern edge, the sweep route through
  it.
- Varga's page `level-18-visibility`: her diagram of the storm's look: the ship in its clear
  radius, the ground fading into dust outside it, Vrell as glowing silhouettes, bullets crisp at
  any range.

*Hangar teaser* (shop screen before L18; Rook, not voiced):

> **Rook:** "Sandstorm on the plains. Bring homing, or bring luck. I'm bringing both."

## Threat profile

Hand-written (M19); directions by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `air` (popcorn, Ghost Drones, Wraiths), `ground` and `low-air` (Dust Devils, which rise to `low-air` when open; mortar and turret nests) |
| Attack directions | front 88% · sides 6% · rear 6% |
| Density | 3 |
| Recommended traits | `homing`, `spread` |
| Hazards | Dust storm: visibility drops to a radius round the ship; Dust Devils pull the ship and the pickups |
| Boss / mid-boss | none |
| Sensor-suite detail | none: storm plains, `air` + `ground`, front · L1: + directions (all), density 3, hazard "dust storm, low visibility", the objective `SWEEP THE STORM` and the secondary `KILL RATIO 65 %` · L2: + Dust Devil (new), Dust Kite, Skitter, Needler, Stinger, Mote Swarm, Wraith portraits, and a silhouette "unclassified, small, `air`" (the Ghost Drone stays unnamed before its first sight) · L3: + `homing` and `spread` highlighted, the wave strip marking the eye, 1 secret |

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
swirls are the second; everything else stays quiet. The data step adds 2 s eases between the
speeds.

| Section | t (s) | Scroll (px) | Speed (px/s) | Visibility (medium) | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Open Plains | 0–25 | 0–3,750 | 150 | full | `deep`: rust plain to the horizon, the storm wall standing on it. `ground`: dry channels, a buried pipeline, wind ripples. `high-air`: the front's leading streaks from t=20. | Popcorn from t=6; the storm seen coming. |
| 2. Storm Front | 25–55 | 3,750–7,950 | 140 | full → 170 px over t=28–36 | `high-air`: the dust wall passing over the screen from the top edge (t=28), streaks ≤ 40 %. `ground`: the plain dimming into dust outside the radius. | The radius ramps in; the Dust Devils alone (t=41.5). |
| 3. Deep Storm | 55–110 | 7,950–14,550 | 120 | 170 px | As section 2 at full strength; a militia rover wreck half buried at x 60 (t≈90, the secret), its red beacon visible only inside the radius. | Ghost Drones (hint 1, t=69.5); the first rear entries. |
| 4. The Eye | 110–130 | 14,550–16,950 | 120 | 170 → full over t=110–114; full; full → 150 over t=130–134 | `deep`: the sky clear overhead, the storm's walls round the field; `ground`: a dry lake bed, a Polyp Mortar floe (t≈125). | A breather in clear air; the Dust Devil's core window learnt in plain sight. |
| 5. Back Wall | 130–170 | 16,950–21,750 | 120 | 150 px | The storm's back wall, denser than the front: streaks at 40 %, the plain gone outside the radius. | Ghost Drone pincers and lines with Dust Devils; the hardest stretch. |
| 6. Clearing | 170–183 | 21,750–23,700 | 150 | 150 → full over t=170–176 | The dust thinning, the plain and the domes of Ares Landing on the horizon. | Release; level end. |

### Visibility in the storm (M6 = a)

A **look only**: the simulation does not know about the dust, so nothing changes what can hit
what (rejected b, the Darkness rule, and c, hiding bullets; the layer rule of
[art direction](../../../art-direction/README.md) never hides bullets). The values (approved in round 36; the look is MA.2's b):

| Element | Inside the radius | Outside the radius |
|---|---|---|
| `ground` and `low-air` (Dust Devils, nests, the rover, the plain) | drawn | **gone into the dust** (a rust veil); a Dust Devil's open core glows through faintly |
| `air` enemies | drawn | **fading gradually to dark silhouettes** through the falloff, their glow kept: Vrell teal/lime glow, the Ghost Drone only its red sensor eye and white running lights |
| Enemy bullets, mortar markers and every telegraph | drawn | **always drawn at full contrast** |
| Pickups | drawn | drawn with a faint glint (they are the player's to chase) |
| The ship, Rook, the player's shots | drawn | Rook (if he strays out) as a silhouette with his running lights; the player's shots drawn |
| HUD, edge warnings, the sensor suite's threat arrows (L2+) | above the dust | above the dust |

- **Radius**: centred on the ship, **170 px** in the deep storm, **150 px** in the back wall (easy
  200 / 180, hard 150 / 130); it follows the ship. **The edge** (round 36, MA.2 = b): a soft,
  ragged falloff over about 120 px around the radius, broken by two drifting noise fields (dust
  tongues), so the view fades into the storm rather than ending at a line. It eases over the times in
  *Layout* (8 s into the front, 4 s into and out of the eye).
- The Dust Devil's pull (130 px) always lies inside the radius, so what pulls the ship is always
  seen.
- **Everything is hittable** wherever it is: homing's edge is that it locks onto what the player
  cannot read well, not that it alone can hit. A kill outside the radius shows its burst through
  the dust.
- **Code fit**: Level 06's darkness and Level 13's whiteout are the patterns
  (`LevelScript.Darkness`, the whiteout's `AtmosphereRamp`): a screen-space mask in presentation,
  after the `low-air` layer and before `air`, with the air units' silhouette pass.
- **Rook** flies as anywhere else (his AI reads the simulation, not the look).

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*).

- **Act HP factor** from the reference DPS 88 at L18 (M18 = a, round 36): **Wraith**
  (L10, 66) ×1.33 → 21 HP. The Dust Kite, Skitter, Needler, Stinger, Mote Swarm, Spine Turret and
  Polyp Mortar keep their HP; the Ghost Drone and the Dust Devil are new here.
- **Density and pacing** (§1.7, the breather): **149 enemies** (waves 138 + ground targets 11) over
  the 178 s from the launch, **≈ 50 a minute** at medium (the floor for a breather is 44, the act's
  55 less 20 %). Smaller popcorn waves (streams of 2–3, V-wings of 3) still keep a wave every
  3.5–4 s, so the screen is never empty over 3 s (`PacingTest`); the eye's waves come every 4 s.
  Front 88 % by count; every side and rear wave warned 3 s ahead at its edge (the warnings draw above
  the dust).
- **Quiet windows**: no other wave for 3.5 s after the first Dust Devils (t=41.5) and after the first
  Ghost Drones (t=69.5). The Ghost Drones' first sight keeps Vrell air off the screen (their spec):
  the grey machines read as a different force.
- **Hint 1** is the t=69.5 line abreast: three drones drop into their lanes 120–220 px below the top
  edge, inside or near the radius, strafe and burst. Varga's line starts as they bank (t=71).

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (left) | Clear sky; the storm wall on the horizon |
| 9.5 | 1 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | |
| 13 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (right) | |
| 16.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | |
| 20 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (alternating edges) | |
| 23.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 27 | 2 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | The storm front reaches the top edge at t=28 |
| 30.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | In the ramp |
| 34 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Teal glow in the thickening dust |
| 37.5 | 2 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (right) | Gone before the Dust Devils' quiet window |
| 41.5 | 2 | pair | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 2 | front | **New.** Alone until t=45 (ground, cycles 0.8 s apart); Varga's line at t=41 |
| 45 | 2 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | |
| 48.5 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | |
| 52 | 2 | rear ambush | [Skitter](../../../enemies/air/skitter/README.md) | 3 | rear | The first rear entry, warned 3 s ahead |
| 55.5 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (alternating edges) | Radius 170 from here |
| 59 | 3 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | |
| 62.5 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (alternating edges) | |
| 66 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (left) | Gone before the Ghost Drones' quiet window |
| 69.5 | 3 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | **New, hint 1.** Alone until t=73.5, no Vrell air on the screen; Varga's line at t=71, Okafor's at t=78 |
| 73.5 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (alternating edges) | |
| 77 | 3 | pair | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 2 | front | Pull with the kites crossing |
| 80.5 | 3 | pincer | [Needler](../../../enemies/air/needler/README.md) | 2 | sides | 1 per edge; warned 3 s ahead |
| 84 | 3 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | |
| 87.5 | 3 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 4 | front | |
| 91 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Over the buried rover (the secret) |
| 94.5 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Cloaked: `homing` hits them before the decloak; warned 3 s ahead |
| 98 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (right) | |
| 101.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | The leader drops an overdrive |
| 105 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | |
| 108.5 | 3 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | |
| 112.5 | 4 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | **The Eye**: radius opens over 4 s; a breather |
| 116.5 | 4 | single | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 1 | front | In clear air: the core window learnt |
| 120.5 | 4 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (alternating edges) | |
| 124.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | front | |
| 128 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (right) | The back wall reaches the top edge at t=130 |
| 131.5 | 5 | pincer | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 4 | sides | 2 + 2 at their lanes; warned 3 s ahead; radius 150 |
| 135 | 5 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (right) | |
| 138.5 | 5 | swarm | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 3 | front | Three roaming, cycles offset 0.8 s |
| 142 | 5 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (alternating edges) | |
| 145.5 | 5 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 4 | front | Splits half left, half right; Rook's line at t=146 |
| 149 | 5 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (alternating edges) | |
| 152.5 | 5 | rear ambush | [Skitter](../../../enemies/air/skitter/README.md) | 3 | rear | Warned 3 s ahead |
| 156 | 5 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | |
| 159.5 | 5 | pincer | [Needler](../../../enemies/air/needler/README.md) | 2 | sides | Warned 3 s ahead |
| 163 | 5 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front (left) | |
| 166.5 | 5 | line abreast | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | |
| 170 | 6 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | front (alternating edges) | The dust thins over t=170–176 |
| 173.5 | 6 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | front | The last wave |

Totals: Dust Kite 63 · Skitter 18 · Ghost Drone 18 · Mote Swarm 12 · Needler 10 · Dust Devil 8 ·
Stinger 7 · Wraith 2 (138; 130 of them air).

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 2 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) pair in a dry channel (t≈47), in the ramp | Bounty 15 each; their markers always visible |
| 3 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on a pipeline ridge (t≈88), seen only inside the radius | Bounty 12 each; their shots always visible |
| 3 | Militia rover wreck (`ground` trigger, 20 HP, t≈90, x 60) | The secret: frees a crate worth 100 (256) |
| 4 | Polyp Mortar floe ×3 on the dry lake bed (t≈125), in the eye | Bounty |
| 5 | Spine Turret nest ×3 (t≈160), in the back wall | Bounty |

The Dust Devils are waves (they roam); they are counted with the ground units in the budget.

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
| The rover's crate freed (secret) | Varga | "Militia supply pod. Someone was out here before the storm." |
| Secondary objective met (at the end) | Okafor | "Clean sweep. Nothing in that storm will reach the domes." |
| Level end | Okafor | "The front is breaking up over the plains. Come home, Aegis." |

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
t=25, swelling in the walls and dropping to a light wind in the eye. New sounds this level needs: the
Dust Devil's spin, its opening and its grit spiral; the Ghost Drone's descent whine, burst and
mechanical death; listed in [sfx](../../../audio/sfx/README.md) by the act's audio step.

## Credit budget

Budget(18) = 700 × 1.07¹⁷ ≈ **2,211** ([economy](../../../systems/economy/README.md#per-level-budget)).
Data in **Act 1 terms** (Dust Kite 4, Skitter 5, Needler 12, Stinger 15, Mote 2, Ghost Drone 14,
Wraith 30, Dust Devil 26, Spine Turret 12, Polyp Mortar 15, the crate 100, the secondary 100), × the
act factor **2.56**, bounties × the scale. A **`bounty_scale` of 0.92** puts the typical haul at
**2,201 (−0.5 %)**. The breather's lower density pays more per kill: the scale is well above M12's
0.3–0.5 expectation and Level 17's 0.55. For review: keep (a thin level should not pay less), or
raise the density to ≈ 55 a minute (scale ≈ 0.84).

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills (air): Dust Kite 63 × 4 + Skitter 18 × 5 + Ghost Drone 18 × 14 + Mote Swarm 12 × 2 + Needler 10 × 12 + Stinger 7 × 15 + Wraith 2 × 30 = 903 | 2,127 | 1,276 |
| Ground: Dust Devil 8 × 26 + Spine Turret 6 × 12 + Polyp Mortar 5 × 15 = 355 | 836 | 669 |
| Secret: rover wreck (hidden crate 100) | 256 | 128 |
| Secondary: kill ratio ≥ 65 % | 256 | 128 |
| **Total** (bounty scale 0.92, act factor 2.56) | **3,475** | **2,201** |
| Budget(18) = the typical haul's target; typical −0.5 %, perfect 1.57 × budget | | 2,211 |

**Balance plan**: the plan's L18 visit comes from part A's Act 3 balance plan (M18); the level
expects `homing` (the Hornet Launcher or the Micro-Missile Pod) and is flyable without it.

## Difficulty notes

- **Easy**: radius 200 px in the deep storm, 180 in the back wall; the Ghost Drones' 2-shot burst
  and the Dust Devils' longer window and weaker pull (their specs); the formation lever.
- **Hard**: radius 150 / 130; the Ghost Drones' 4-shot burst and the second pass of surviving
  line-abreast drones; the Dust Devils' 3-arm spiral and 45 px/s pull; an extra Wraith rear pair at
  t≈150 (warned); the Wraiths' hard hooks from their spec.

## Concept art

None yet. The level will need (part E's production-art round; the mood comes from the storm-plains
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

- [ ] Level data `data.yaml` (when the level is built, part E): sections and speeds with eases,
      the visibility ramp per section, waves, ground targets, radio, budget, threat profile,
      briefing, backdrop, music
- [ ] Dust-storm visibility (M6 = a): the radius mask (170 / 150 px, easy 200 / 180, hard 150 / 130,
      the soft ragged ~120 px falloff with drifting dust tongues, MA.2 = b) following the ship, `ground` and `low-air` hidden outside it, `air` units as
      silhouettes with their glow, bullets, markers, telegraphs, pickups, Rook, the HUD and the
      warnings always drawn; the ramps; nothing in the simulation changes
- [ ] Hint 1: the t=69.5 Ghost Drone line abreast alone with no Vrell air, Varga's line and Okafor's
      cut; the Vorne motif layer from that moment
- [ ] Wave script: the breather's ≥ 44 a minute (≈ 50) at medium (`DensityTest` with the level's
      breather allowance), no empty screen over 3 s (`PacingTest`), the quiet windows, edge warnings
      for every side and rear wave
- [ ] Ground targets, the rover secret (20 HP trigger, the beacon only inside the radius, crate 100),
      the supply drone's armour patch at t=118, the overdrive, the Dust Devil's held pickups and the
      shield cell
- [ ] Kill-ratio secondary at 65 %, judged at the level end (§1.7)
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`, with Rook and without),
      Rook's lines `requires: escort`
- [ ] Threat profile with the "unclassified" L2 silhouette, Varga's four intel lines and Rook's
      teaser fitting the intel panel (`IntelPanelLayoutTest`)
- [ ] Briefing: Okafor's page (with the Helix line) and Varga's, each one screen
      (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(18) = 2,211 with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music: track 9's stems and the motif layer by section; the `mars-storm` ambience and its swell
- [ ] Easy/hard variations; the autopilot flies the level with Rook on every difficulty
      (`--escort none` too)
- [ ] Production art and captures for part E's round

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
