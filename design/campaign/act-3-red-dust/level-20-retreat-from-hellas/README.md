---
title: Level 20 – Retreat from Hellas
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/air/mote-swarm, ../../../enemies/air/dust-kite, ../../../enemies/ground/burrower, ../../../enemies/ground/dust-devil, ../../../enemies/bosses/dust-colossus, ../../../player/wingmen, ../../../player/weapons/rear-lance, ../../../story/characters/rook, ../../../world/mars]
updated: 2026-10-19
---

# Level 20 – Retreat from Hellas

## Summary

Aegis Wing strikes the Vrell staging grounds on the floor of the Hellas basin, and the strike goes
wrong: something kilometres long moves under the sand, the basin boils with Vrell, and Okafor orders
a retreat to the rim. After 20 s of normal flight the scroll **reverses** (M4 = a, M5 = a): the
ground moves up the screen, the ship keeps facing up and flies backwards toward the bottom, a
pursuing [Mote Swarm](../../../enemies/air/mote-swarm/README.md) presses from the **top** (held by
the front guns), and the Burrowers, Dust Devils and popcorn that have cut the way back come up from
the **bottom** (cleared by the rear guns). A genuine retreat: the war is not going well. Rook's one
quiet line in the lull foreshadows Act 4. About 172 s; ≈ 64 enemies a minute, a third of them the
pursuit swarm.

**Status: approved in concept round 36 (user, 2026-10-13).** Written in the overnight design
phase (2026-10-12) from the act roster and the gap analysis's recommended answers, all kept or
accepted in the round. The numbers are still hand-written tables (M19: no `data.yaml` yet); M7
part G moves them into the level's data and turns these tables into marked ones.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Okafor's page pre-briefs
the fallback, so the reversal is no surprise to the player; Varga's carries the private side of
hint 2 (M13 = a: the briefings and the hangar intel carry it, the radio does not). Texts (approved in round 36):

> **Commander Okafor:** "We hit the Vrell staging grounds on the Hellas floor. If it turns, you
> fall back to the rim: nose to the enemy, rear guns clearing the road. Doctor Varga's walker
> report comes to me alone."
>
> **Dr. Varga:** "That walker's plate, Lancer. Filed by hand, not worn. I'm told to leave it. On
> the basin floor: Burrowers, Dust Devils, and swarms. A lot of swarms."

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`):

- Okafor's page `level-20-hellas-fallback`: a CDF terrain plot of the Hellas basin's north side:
  the staging grounds on the floor, the strike route in (solid), the fallback route back up the
  terraces and the rim road (dashed), the militia guns at the rim's lip.
- Varga's page `level-20-filed-plate`: **the Revenant Walker's flank plate close up** (M15 = a): the
  serial field ground to bright bare metal, the file strokes catching the light, an assay strip
  clipped beside it with one line circled in her hand. The render is the wreck's from Level 19.

*Hangar teaser* (shop screen before L20; Rook, not voiced):

> **Rook:** "Hellas. Big hole, lots of bugs. Pack a tail gun, just in case."

## Threat profile

Hand-written (M19). Directions are counted by each wave's `from` **by travel** (M5 = a): after the
reversal "ahead" is the screen's bottom and "behind" its top; in section 1 ahead is the top.

| Field | Value |
|---|---|
| Dominant layers | `air` (popcorn, the pursuit swarm, Wraiths), `ground` (Burrowers, Dust Devils, turret and mortar nests) |
| Attack directions | ahead 61% · behind 38% · sides 1% (by travel; on the screen after the turn: bottom, top, sides) |
| Density | 4 |
| Recommended traits | `rear`, `homing` |
| Hazards | Reverse scroll from t≈22: the ground moves up the screen; a pursuing swarm lunging from the top |
| Boss / mid-boss | none |
| Sensor-suite detail | none: Hellas basin, `air` + `ground`, front · L1: + directions (all, "reverse scroll"), density 4, hazard "fallback under pursuit", the objective `FALL BACK TO THE RIM` and the secondary `KILL RATIO 65 %` · L2: + Mote Swarm, Dust Kite, Skitter, Needler, Stinger, Wraith, Burrower, Dust Devil portraits · L3: + `rear` and `homing` highlighted, the wave strip marking the turn and the lull, 1 secret |

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Hellas basin. The Vrell are massing on the floor." |
| L1 | "If we fall back, the swarm will be on your nose." |
| L2 | "Swarms, Burrowers, Dust Devils. Cloakers too." |
| L3 | "Rear guns clear the road. Spread holds the swarm." |

## Objective

- **Primary** `reach-end`: fall back over the Hellas rim.
- **Secondary**: destroy at least **65 %** of all enemies (§1.7's kill-ratio rule), judged at the
  level's end; the pursuit swarm's motes count, those that drop back alive count as not killed. Pays
  **100** credits in Act 1 terms (256 in Act 3); the typical haul counts 50 %. Holding the swarm with
  `spread` while the rear guns clear the road is the way to it.

## Layout

Signed scroll speeds (M5 = a): a negative speed moves the ground **up** the screen. **+140 px/s** in
the strike, then **−130 / −120 / −100 / −140 px/s** in the retreat. Setting:
[Mars](../../../world/mars/README.md)'s Hellas basin, the north side: a pale ochre basin floor of
dunes and salt-cracked flats under a hazy butterscotch sky, Vrell growth spreading over the staging
grounds, then the basin's terraces (broad steps of layered rock), the rim road's switchbacks and the
rim's lip with the militia's guns. Motion budget: the ground moving the "wrong" way and the swarm
are the two strong elements; dust stays faint. The data step adds 2 s eases between the speeds
(the turnabout has its own 4 s ease).

The backdrop is authored as one strip; the level starts 15,360 px up it, flies 2,840 px further up
in the strike, then runs it back down past its start (the scroll column is the distance from the
level's start, negative once the ship has retreated past it).

| Section | t (s) | Scroll (px) | Speed (px/s) | Layers and content | Purpose |
|---|---|---|---|---|---|
| 1. The Strike | 0–20 | 0 → 2,800 | +140 | `deep`: the basin floor to the horizon. `ground`: dunes, salt flats, the first Vrell growth; from t≈12 a **travelling ridge** (≈ 900 px, the [Dust Colossus](../../../enemies/bosses/dust-colossus/README.md)'s body, scenery) crosses the floor near the top edge, dunes heaving. | A short normal-scroll opener; the ridge foreshadows Level 21. |
| 2. Turnabout | 20–26 | 2,800 → 2,600 | +140 → −120 over t=20–24 (zero at ≈ 22.2), then −120 | As section 1; the ridge's wake boiling with Vrell at the top. | The reversal; the pursuit swarm enters from the top (t=24). |
| 3. Basin Floor | 26–70 | 2,600 → −3,120 | −130 | `ground`: the floor seen again, then new dunes; a dune ridge with a turret nest (t≈42), mortars in the flats (t≈60). | The first retreat: the swarm above, ambushes from below. |
| 4. The Terraces | 70–108 | −3,120 → −7,680 | −120 | `ground`: the basin's terrace steps rising from the bottom edge, sand on each step (Burrowers), a turret nest on a step (t≈85), the outpost's emergency cache on a ledge (t≈92). | Mixed; the swarm drops back at the end. |
| 5. The Lull | 108–124 | −7,680 → −9,280 | −100 | `ground`: a wide upper terrace, wind-carved pillars. `deep`: the basin's floor far behind (top), the ridge's dust settling. | A breather: light popcorn only; Rook's quiet line; the armour patch. |
| 6. Rim Road | 124–160 | −9,280 → −14,320 | −140 | `ground`: the rim road's switchbacks coming up from the bottom, abandoned militia crawlers, a turret nest on the road (t≈140). | The second pursuit, the last ambushes. |
| 7. Over the Rim | 160–172 | −14,320 → −15,360 | −140 → −60 over t=160–168, then −60 | `ground`: the rim's lip rising from the bottom, the militia guns firing up past the ship (flak bursts in the top band, scenery), the plain beyond. | Release; the swarm breaks off; level end. |

### Reverse scroll (M4 = a, M5 = a)

The rules for a section with a negative speed (new in part G), for review:

- **Direction of travel** is down the screen. The ship keeps facing up, its front guns fire up and
  its rear guns down: "rear" stays the bottom of the screen for every weapon and drone (the
  [Rear Lance](../../../player/weapons/rear-lance/README.md), the [wingmen](../../../player/wingmen/README.md)'s
  rear-guard drone), which is **ahead** in travel here.
- **Entry edges follow the sign**: a wave's default entry ("front" by travel) is the **bottom** edge
  and its route is mirrored vertically: a Dust Kite glides up the screen and swoops across the ship's
  lane from below, a Burrower surfaces a short way below the ship, ground targets and scenery rise
  from the bottom with the terrain. "Rear" by travel is the **top** edge. The sprites are not mirrored
  (the light stays top-left); units with 16 or 32 angles face their travel.
- **Fixed-facing ground units** turn their arcs with the travel (a Spine Turret's silence arc points
  down by travel, so it fires up at the ship while it rises toward it and falls silent once past:
  the same `arc` placement parameter as Level 19's up-facing nests).
- **Front/rear rules read travel**: the 60 %-front rule, the intel's directions and the threat profile
  above.
- **Edge warnings**: a new **TOP** edge (`WarningEdge` TOP, 3 s ahead) for every entry from the top in
  a reversed section (the pursuit swarms, the Wraiths); LEFT/RIGHT as usual. Bottom entries are the
  default edge and unwarned, except the **first three after the turn** (t=27.5, 31.5, 35.5), which
  show the BOTTOM warning as a lesson.
- **Pickups** drift with the scroll's sign (up the screen in a reversed section). Bullets and shots are
  screen-space and unchanged.
- **Backdrop and parallax** run with the sign (new tiles from the bottom; `high-air` dust drifts up);
  the deep-coverage rule holds at the bottom edge.
- **Rook** keeps his slot beside the ship and shoots up into the swarm with his gun. His rear bark is
  keyed to the **top** in a reversed section ("Up top!" variants, written below) and is silent for
  bottom entries; during a swarm lunge's tell his slot moves out of the ship's lane. His AI must fly
  the turnabout and the reversed sections (§1.7.9).

### The pursuit swarm

A [Mote Swarm](../../../enemies/air/mote-swarm/README.md) in a new **`pursuit`** formation (§2.5,
part G); its numbers are first values for review:

- **Band**: its leader holds a band **50–150 px below the top edge** (behind the ship by travel),
  following the ship's x with a lag (90 px/s sideways). The flock keeps the spec's boids (separation
  18 px).
- **Lunge**: every **6 s** a 0.8 s tell (the flock contracts into a tight ball ≈ 60 px across,
  brightens, a rising hum), then the leader dives straight down the ship's lane at **240 px/s** to
  140 px below where the ship was at the tell's start, and climbs back to the band at 200 px/s, the
  members re-forming round it. The dive does not home: a side step dodges it.
- **Size**: **10 motes** on entry (easy 8, hard 12, the formation lever), plus **refills of 3** that
  join from the top edge every **10 s** (no warning: the swarm is on the screen), whether or not it was
  thinned. Deterministic and counted in the density: the first pursuit (t=24–108) gets 8 refills (34
  motes), the second (t=124–160) 3 (19 motes).
- **Damage**: contact `tiny` 6 per mote (the mote dies); no shots.
- **Breaks off** when its section ends: the leader climbs off the top at 200 px/s and the survivors
  follow (at t=108 into the lull, at t=163 under the rim guns' flak). They are not killed.
- Each pursuit enters under the **TOP** warning (3 s ahead). The swarm fills the screen between the
  ambush waves, so the 3 s rule holds while it is up.

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary) plus the new `pursuit`. Data in
**Act 1 terms** with a `bounty_scale` (see *Credit budget*). "Bottom (ahead)" and "top (behind)" are
the screen edges with their meaning by travel.

- **Act HP factor** from the reference DPS 96 at L20 (M18 = a, round 36):
  **Burrower** (L15, 76) ×1.26 → 56 HP, **Dust Devil** (L18, 88) ×1.09 → 57 HP, **Wraith** (L10, 66)
  ×1.45 → 23 HP. The Dust Kite, Mote Swarm, Skitter, Needler, Stinger, Spine Turret and Polyp Mortar
  keep their HP.
- **Density and pacing** (§1.7): **178 enemies** (waves 165 + ground targets 13) over the 167 s from
  the launch, **≈ 64 a minute** at medium (minimum 55). 53 of them are the pursuit swarms' motes;
  outside the pursuits a wave every 3.5–4 s, inside them a bottom wave every ≈ 4 s with the swarm on
  the screen in between, so the screen is never empty over 3 s (`PacingTest`). Ahead (by travel)
  83 % of the waves, 61 % of the enemies.
- **Chosen density.** Act 2 ran 54–70 a minute and Levels 15–17's drafts 85–90; this level aims at
  the minimum plus what the pacing needs. The swarm alone is a constant presence, so the ambush waves
  stay small (threes) and the count lands at ≈ 64 rather than higher.
- **Quiet windows**: none needed for new units (nothing is new but the `pursuit`); the turnabout
  (t=21–23.5) spawns nothing but the top vanguard, and the lull (t=108–124) carries kite pairs only.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | top (ahead) | Normal scroll |
| 9.5 | 1 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | top (ahead) | |
| 13 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | top (ahead) | As the ridge starts to cross |
| 16.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | top (ahead) | |
| 20.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (behind once reversed) | The swarm's vanguard out of the ridge's wake; Okafor's order at t=20 |
| 24 | 2 | pursuit | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 + 8 × 3 | top (behind) | **Pursuit 1**: TOP warning from t=21; refills at t=34, 44 … 104; drops back at t=108 |
| 27.5 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | The first bottom entry, BOTTOM-warned (lesson) |
| 31.5 | 3 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | BOTTOM-warned (lesson) |
| 35.5 | 3 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 3 | bottom (buried, below the ship) | BOTTOM-warned (lesson); act HP factor (56 HP) |
| 40 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 3 | bottom (ahead) | Over the dune ridge's nest (t≈42) |
| 44 | 3 | pair | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 2 | bottom (ahead) | Their pull with the swarm above; act HP factor (57 HP) |
| 48.5 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | bottom (ahead) | |
| 52.5 | 3 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 3 | bottom (buried) | |
| 56.5 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | |
| 59 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | top (behind) | Cloaked inside the swarm; TOP-warned; act HP factor (23 HP) |
| 63 | 3 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | |
| 67 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | bottom (ahead) | |
| 71 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | bottom (ahead) | Up the first terrace; the leader drops an overdrive |
| 75 | 4 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | |
| 79 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | bottom (ahead) | |
| 83 | 4 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | Over the terrace nest (t≈85) |
| 87 | 4 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 3 | bottom (buried) | In the terrace sand |
| 90.5 | 4 | pincer | [Needler](../../../enemies/air/needler/README.md) | 2 | sides | 1 per edge; warned 3 s ahead |
| 94.5 | 4 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | Past the emergency cache (the secret, t≈92) |
| 101 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | top (behind) | TOP-warned |
| 104.5 | 4 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | |
| 108 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 3 | bottom (ahead) | The swarm drops back off the top |
| 112 | 5 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | bottom (ahead) | **The lull** |
| 115.5 | 5 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 2 | bottom (ahead) | Rook's quiet line from t=113 |
| 119 | 5 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | The armour patch at t=117 |
| 124 | 6 | pursuit | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 + 3 × 3 | top (behind) | **Pursuit 2**: TOP warning from t=121; refills at t=134, 144, 154; breaks off at t=163 |
| 125.5 | 6 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | |
| 129.5 | 6 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 3 | bottom (ahead) | |
| 133.5 | 6 | pair | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 2 | bottom (ahead) | On the road's switchback; the last one drops a shield cell |
| 137.5 | 6 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | Over the road nest (t≈140) |
| 141.5 | 6 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 3 | bottom (ahead) | |
| 146.5 | 6 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | top (behind) | TOP-warned |
| 150 | 6 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 3 | bottom (buried) | In the road's drifts |
| 154 | 6 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | |
| 157.5 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | bottom (ahead) | |
| 161 | 7 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | The rim guns open fire at t=160 |
| 164.5 | 7 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | The last wave |

Totals: Mote Swarm 53 · Dust Kite 52 · Skitter 22 · Needler 12 · Burrower 12 · Wraith 6 ·
Dust Devil 4 · Stinger 4 (165; 149 of them air).

## Ground targets

All rise from the bottom with the terrain (the reversed sections' rule).

| Section | Target | Effect |
|---|---|---|
| 3 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on a dune ridge (t≈42), arcs by travel | Bounty 12 each; fire up at the ship while rising toward it |
| 3 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) pair in the salt flats (t≈60) | Bounty 15 each; their markers on the ground ahead |
| 4 | Spine Turret nest ×3 on a terrace step (t≈85) | Bounty |
| 4 | Hellas Outpost emergency cache (`ground` trigger, 20 HP, t≈92, x 430) | The secret: frees a crate worth 100 (256) |
| 4 | Polyp Mortar pair on a terrace (t≈100) | Bounty |
| 6 | Spine Turret nest ×3 on the rim road (t≈140) | Bounty |

The Burrowers and Dust Devils are waves; they are counted with the ground units in the budget.

## Hazards

- **The reverse scroll** and **the pursuit swarm** (above): the level's two mechanics.
- **Dust Devil pull** (its spec): within 130 px, toward the centre; with the swarm above, the pull
  is never a trap against the ship's speed.
- No terrain collision (the terraces and the rim road are the `ground` layer's look, not walls); no
  wind.

## Secrets and pickups

- **Hellas Outpost emergency cache** (the secret, t≈92, x 430, the right edge of a terrace ledge): a
  half-buried militia shelter hatch with a yellow strobe. A `ground` trigger of **20 HP** that rises
  from the bottom; destroyed, it pops a hidden crate worth **100** in Act 1 terms (256). The Salvage
  scanner's glint shows it. Its line, Ares Control: "*Hellas Outpost's emergency cache. Better you
  than the Vrell.*" (an event line).
- **Overdrive**: dropped by the t=71 Stinger leader.
- **Armour patch**: an Ares militia supply drone (the [CDF supply drone](../../../allies/README.md#cdf-supply-drone)'s
  presentation, flying up from the bottom edge) releases one at (240, 300 y-up) at t=117, in the lull.
- **Shield cell**: dropped by the last Dust Devil of the t=133.5 pair, with whatever pickups it held.

## Radio chatter

Spoken radio, retimed to the **1 s rule** (as Levels 17–19: 5 s for the last page plus 3 s for each
page before it, a page of 3 × 22 characters, text at 30 characters a second, 0.4 s of silence; no
timed line more than 1 s late). Event lines drop after 6 s. The colony's voice is **Ares Control**
(no new speaker). Rook's tactical lines carry `requires: escort`; he has **no dry joke** in this
level (the retreat) and instead the act's one **quiet beat** (M17 = a), at t=113 in the lull.

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Hellas basin, Aegis. The Vrell are massing on the floor. Hit them before they move." |
| t=12.5 | Varga | "The basin floor is moving. Something huge, under the sand." |
| t=20 | Okafor | "All Aegis units, break off! Fall back to the rim!" |
| t=27.5 | Varga | "Front guns on the swarm. Rear guns on what's ahead." |
| t=35 | Ares Control | "The rim road is open, Aegis. Our guns wait at the top." |
| t=47 | Varga | "More Burrowers below you. Watch for the bulge." |
| t=56 | Rook | "Up top! Cloakers in the swarm!" |
| t=64 | Rook | "That swarm is not getting smaller, Lancer!" |
| t=75 | Okafor | "Keep moving. Don't let them pin you on the terraces." |
| t=98 | Rook | "Cloakers up top again!" |
| t=106 | Varga | "The swarm is dropping back. They're regrouping." |
| t=113 | Rook (quiet beat) | "Lancer... if I don't come back from one of these, call my mum yourself. No form letters." |
| t=124.5 | Varga | "They've found us again. Here they come." |
| t=131 | Ares Control | "Militia on the rim, Aegis. We can see your dust. Keep coming." |
| t=143.5 | Rook | "Up top! More cloakers!" |
| t=152 | Okafor | "The rim is right ahead, Lancer. Get over the lip." |
| t=160 | Ares Control | "Rim guns, open fire! Clear their tail!" |
| The cache's crate freed (secret) | Ares Control | "Hellas Outpost's emergency cache. Better you than the Vrell." |
| Secondary objective met (at the end) | Okafor | "You thinned them on the way out. They'll remember that." |
| Level end | Okafor | "We gave up the basin today. We will take it back. Come home, Aegis." |

Timeline check (text): the two-page lines close at ≈ 12.2 (t=1) and ≈ 124.3 (Rook's quiet line,
t=113); Ares Control's t=131 line starts 0.2 s late (Varga's closes at ≈ 131.2), within the rule;
every other timed line starts on time. Rook's top barks fall on the Wraiths' TOP warnings (t=56, 98,
143.5). **The quiet beat**: Rook's line plays with the screen nearly empty and the music down to its
base stem; it is not a joke and nobody answers it. It plays **without** `requires: escort`
(he speaks from the formation off-screen when the slot is empty, as his character document allows),
so every player hears it (round 36, Q.20d).

## Music & ambience

The Act 3 A theme **"Red Dust Run"** (track 8 in the [track list](../../../audio/music/README.md#track-list),
chosen in round 02; its stems come with part B): the **base stem** in the strike, the **full mix from
the turnabout** (t=20, Okafor's order), **down to the base stem in the lull** (t=108–124, under Rook's
line), the full mix again from section 6, out over the rim with the mission-complete jingle. No
Vorne motif (the pursuers are Vrell). Decided in round 36 (Q.20e): the A theme's breakbeat suits
the run, and Levels 18–19 already carry the B theme, which part E makes for Level 18. Ambience: the `mars-dust` wind (Level 15's) with a deep **basin rumble** from t=12 (the
ridge; a low earth groan, no bubbly or popping sounds) fading by t=40. New sounds this level needs:
the pursuit swarm's band hum and the lunge's rising hum, the TOP warning (the edge warning's sound),
the rim guns' flak (a distant flak sound, as Level 17's militia); listed in
[sfx](../../../audio/sfx/README.md) by the act's audio step.

## Credit budget

Budget(20) = 700 × 1.07¹⁹ ≈ **2,532** ([economy](../../../systems/economy/README.md#per-level-budget)).
Data in **Act 1 terms** (Dust Kite 4, Mote 2, Skitter 5, Needler 12, Stinger 15, Wraith 30,
Burrower 24, Dust Devil 26, Spine Turret 12, Polyp Mortar 15, the crate 100, the secondary 100),
× the act factor **2.56**, bounties × the scale. A **`bounty_scale` of 0.95** puts the typical haul
at **2,525 (−0.3 %)**. That is well above M12's 0.3–0.5, as Level 18's 0.92: the level has no
mid-boss, its density rests on cheap motes and kites, and its budget is the act's second highest.
Kept in round 36 (Q.20f), rather than adding pricier units (a Mantis pair, Spore Bombers) to bring
the scale toward 0.6.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills (air): Mote Swarm 53 × 2 + Dust Kite 52 × 4 + Skitter 22 × 5 + Needler 12 × 12 + Wraith 6 × 30 + Stinger 4 × 15 = 808 | 1,965 | 1,179 |
| Ground: Burrower 12 × 24 + Spine Turret 9 × 12 + Dust Devil 4 × 26 + Polyp Mortar 4 × 15 = 560 | 1,362 | 1,090 |
| Secret: emergency cache (hidden crate 100) | 256 | 128 |
| Secondary: kill ratio ≥ 65 % | 256 | 128 |
| **Total** (bounty scale 0.95, act factor 2.56) | **3,839** | **2,525** |
| Budget(20) = the typical haul's target; typical −0.3 %, perfect 1.52 × budget | | 2,532 |

**Balance plan**: the plan's L20 visit comes from part A's Act 3 balance plan (M18; the
[Ion Beam](../../../player/weapons/ion-beam/README.md)'s hand plan buys the Tail Gun L3 here). The
level expects `rear` (Tail Gun, Rear Lance, or the rear-guard drone unlocked at this visit) and
`homing`, and is flyable with the front guns alone, slowly (a rear-less ship turns the bottom waves
into contact threats it must dodge and shoot once they pass).

## Difficulty notes

- **Easy**: the swarm 8 on entry, refills of 2, a lunge every 8 s with a 1.2 s tell; the TOP warning
  4 s ahead; the Burrowers' ring of 8 and the Dust Devils' weaker pull (their specs).
- **Hard**: the swarm 12 on entry, refills of 4, a lunge every 4.5 s; the Burrowers' second ring and
  the Dust Devils' 3-arm spiral (their specs); an extra Wraith pair from the top at t≈80 (TOP-warned);
  the Wraiths' hard hooks.

## Concept art

None yet. The level will need (part G's production-art round; the mood comes from the Hellas
mood still in [mars](../../../world/mars/README.md), round 36 MA.4):

- the production backdrop as one strip run both ways (the basin floor and the staging grounds, the
  terraces, the upper terrace's pillars, the rim road's switchbacks, the rim's lip with the militia
  guns and the plain beyond), lit so it reads in either scroll direction;
- the travelling ridge (shared with Level 21's Dust Colossus body), the emergency cache hatch, the
  abandoned militia crawlers, the rim guns' flak;
- the pursuit swarm's look in its band and its lunge tell; the TOP edge warning;
- the two briefing images (the filed plate reuses the walker's wreck render from Level 19);
- the round's captures: the turnabout, a lunge, a bottom ambush under the swarm, Rook's quiet beat in
  the lull, the climb over the rim.

Varga's page image `level-20-filed-plate` was produced early with Level 19's batch (round 42, the walker wreck's
plate): [briefing-images-final-r42-a.png](../../../ui/briefing/concept/briefing-images-final-r42-a.png); pending the
user's review.

## Implementation

- [ ] Level data `data.yaml` (when the level is built, part G): sections with **signed** speeds and
      eases (the turnabout's 4 s ease through zero), waves with travel-relative `from`, ground
      targets, radio, budget, threat profile, briefing, backdrop, music
- [ ] Reverse scroll (M5 = a): `LevelData.Section` accepts negative speeds; entry edges and routes
      follow the sign; ground objects, roads, backdrop tiling and parallax run backwards; front/rear,
      the 60 % rule and the intel's directions read travel; arcs of fixed-facing units by travel;
      pickups drift with the sign
- [ ] `WarningEdge` TOP; the BOTTOM lesson warnings on the first three waves after the turn
- [ ] The `pursuit` formation: the band, the lag, the lunge with its tell, the refills, the break-off;
      its motes counted in the density and the kill ratio
- [ ] Rook in reverse scroll: his slot, his top barks, out of the lane during a lunge's tell; the
      quiet beat line (with or without `requires: escort`, pending)
- [ ] Wave script: ≥ 55 enemies a minute at medium (`DensityTest`, ≈ 64), no empty screen over 3 s
      (`PacingTest`), ≥ 60 % of waves ahead by travel, the act HP factor for the Burrower, Dust Devil
      and Wraith
- [ ] Ground targets, the emergency-cache secret (20 HP trigger, crate 100), the supply drone's armour
      patch at t=117, the overdrive, the Dust Devil's shield cell
- [ ] Kill-ratio secondary at 65 %, judged at the level end (§1.7)
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`, with Rook and without)
- [ ] Threat profile by travel, Varga's four intel lines and Rook's teaser fitting the intel panel
      (`IntelPanelLayoutTest`)
- [ ] Briefing: Okafor's page (the fallback, the walker report) and Varga's (the filed plate), each
      one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(20) = 2,532 with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music: track 8's stems by section, the lull's drop; the basin rumble
- [ ] Easy/hard variations; the autopilot flies the turnabout and the reversed sections with Rook on
      every difficulty (`--escort none` too); a golden replay of the level
- [ ] Production art and captures for part G's round

## Open questions

- None open: round 36 answered every question (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the Act 3 roster to a draft level document.
  Taken as the recommended options, pending the user's review in round 36: M4 = a (the terrain moves
  up the screen; the swarm presses from the top, the ambushes come up from the bottom), M5 = a (a
  signed scroll speed per section, entry edges by the sign, front/rear by travel, a TOP warning edge),
  M10 = b (the Dust Kite as the act's popcorn), M12 = a, M13 = a (the walker report on Okafor's
  page, the plate on Varga's), M15 = a (the filed plate as Varga's briefing image), M17 = a (Rook's one
  quiet beat foreshadowing Act 4, no joke), M18 = a (reference DPS 96), M19 (hand tables). Our
  proposals: a 20 s strike opener that turns into the retreat at Okafor's order, with the Colossus's
  ridge crossing the basin as the cause; the `pursuit` formation (band, lunge, refills, break-off);
  the BOTTOM lesson warnings; a lull with Rook's line; the rim guns' flak as the end; the kill-ratio
  secondary counting the motes; the emergency-cache secret; Ares Control as the colony voice; track 8;
  178 enemies (≈ 64 a minute, the minimum plus the pacing's needs) and `bounty_scale` 0.95 for a
  typical 2,525.
- 2026-10-12 (overnight, harmonised with the act README): checked against the act's band of 60–72
  a minute and its music and voice notes; no change needed: ≈ 64 a minute and `bounty_scale` 0.95
  kept (the act accepts the cheap mix's high scale), track 8
  with no Vorne motif, Ares Control as the colony voice, Rook's quiet beat in place of a joke.
- 2026-10-13 (user, round 36): approved (D.7), M4, M5 and M17 kept or accepted. Q.20 all yes: the
  20 s strike opener, the pursuit's numbers (10 + refills of 3 every 10 s, a lunge every 6 s at
  240 px/s, tuned in part G's captures), bottom entries unwarned after the first three, Rook's quiet
  beat without `requires: escort`, track 8 for the retreat (track 9 moves to part E),
  `bounty_scale` 0.95 kept.
