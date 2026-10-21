---
title: Level 20 – Retreat from Hellas
design: approved
implementation: done
art: final
depends-on: [../../../enemies/air/mote-swarm, ../../../enemies/air/dust-kite, ../../../enemies/ground/burrower, ../../../enemies/ground/dust-devil, ../../../enemies/bosses/dust-colossus, ../../../player/wingmen, ../../../player/weapons/rear-lance, ../../../story/characters/rook, ../../../world/mars]
updated: 2026-10-21
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
quiet line in the lull foreshadows Act 4. About 172 s; ≈ 192 enemies (≈ 69 a minute), 53 of them
the pursuit swarms' motes.

**Status: approved in concept round 36 (user, 2026-10-13).** Written in the overnight design
phase (2026-10-12) from the act roster and the gap analysis's recommended answers, all kept or
accepted in the round. M7 part G (step G2, 2026-10-20) moved the numbers into the level's
[data.yaml](data.yaml): the tables below are rendered from it, the waves re-planned to the Act 2+ floor
("tougher, not more", count-neutral; see *Waves*), accepted in round 43 (2026-10-20).

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Okafor's page pre-briefs
the fallback, so the reversal is no surprise to the player; Varga's carries the private side of
hint 2 (M13 = a: the briefings and the hangar intel carry it, the radio does not). Texts (approved in round 36):

<!-- data: briefing -->
> **Commander Okafor:** "We hit the Vrell staging grounds on the Hellas floor. If it turns, you
> fall back to the rim: nose to the enemy, rear guns clearing the road. Doctor Varga's walker
> report comes to me alone."
>
> **Dr. Varga:** "That walker's plate, Lancer. Filed by hand, not worn. I'm told to leave it. On
> the basin floor: Burrowers, Dust Devils, and swarms. A lot of swarms."
<!-- /data -->

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`):

- Okafor's page `level-20-hellas-fallback`: a CDF terrain plot of the Hellas basin's north side:
  the staging grounds on the floor, the strike route in (solid), the fallback route back up the
  terraces and the rim road (dashed), the militia guns at the rim's lip.
- Varga's page `level-20-filed-plate`: **the Revenant Walker's flank plate close up** (M15 = a): the
  serial field ground to bright bare metal, the file strokes catching the light, an assay strip
  clipped beside it with one line circled in her hand. The render is the wreck's from Level 19.

*Hangar teaser* (shop screen before L20; Rook, not voiced):

<!-- data: teaser -->
> **Rook:** "Hellas. Big hole, lots of bugs. Pack a tail gun, just in case."
<!-- /data -->

## Threat profile

Rendered from the data. Directions are counted by each wave's `from` **by travel** (M5 = a): after the
reversal "ahead" (`front`) is the screen's bottom and "behind" (`rear`) its top; in section 1 ahead is
the top.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (popcorn, Needler and Stinger groups, the pursuit swarm, Wraiths), `ground` (Burrowers, Dust Devils, turret and mortar nests) |
| Attack directions | front 57% · sides 3% · rear 40% (by travel: front = ahead, the bottom edge once reversed; rear = behind, the top) |
| Density | 4 |
| Recommended traits | `rear`, `homing` |
| Hazards | Reverse scroll from t≈22: the ground moves up the screen; a pursuing swarm lunging from the top |
| Boss / mid-boss | none |
| Sensor-suite detail | none: Hellas basin, `air` + `ground`, front · L1: + directions (all, "reverse scroll"), density 4, hazard "fallback under pursuit", the objective `FALL BACK TO THE RIM` and the secondary `KILL RATIO 65 %` · L2: + Mote Swarm, Dust Kite, Skitter, Needler, Stinger, Wraith, Burrower, Dust Devil portraits · L3: + `rear` and `homing` highlighted, the wave strip marking the ridge pass (12–20 s), the turn and the lull, 1 secret |
<!-- /data -->

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

The backdrop is authored as one strip; the level starts 15,360 px up it, flies 2,800 px further up
in the strike and ≈ 180 px more into the turnabout's ease (its top ≈ 2,983 at the zero, t ≈ 22.1;
2,840 again at t=24), then runs it back down past its start (the scroll column is the distance from the
level's start, negative once the ship has retreated past it).

The data eases each speed change (the sections' `ramp`: the turnabout's 4 s through zero at
t ≈ 22.1, the retreat's 2 s, the rim's 8 s); the scroll column includes the eases. The ground's tiles
come from the backdrop's **strip** (`backdrop.strip`, keyed by strip row = 15,360 + the scroll), not
from the sections (step ART-G1's zones: the staging grounds' flats, the dune sea, the salt flats, the
terraces, the upper terrace, the rim slope with its road, the plain beyond the rim).

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Strike | 0–20 | 0 → 2,800 | +140 | light | `ground`: dunes, salt flats, the first Vrell growth; from t≈12 a **travelling ridge** (≈ 900 px, the [Dust Colossus](../../../enemies/bosses/dust-colossus/README.md)'s body, scenery) crosses the floor near the top edge, dunes heaving. | A short normal-scroll opener; the ridge foreshadows Level 21. |
| 2. Turnabout | 20–26 | 2,800 → 2,600 | −120 | medium | `ground`: as section 1; the ridge's wake boiling with Vrell at the top. | The reversal; the pursuit swarm enters from the top (t=24). |
| 3. Basin Floor | 26–70 | 2,600 → −3,110 | −130 | light | `ground`: the floor seen again, then new dunes; a dune ridge with a turret nest (t≈42), mortars in the salt flats (t≈60), a nest on the floor (t≈66). | The first retreat: the swarm above, ambushes from below. |
| 4. The Terraces | 70–108 | −3,110 → −7,680 | −120 | light | `ground`: the basin's terrace steps rising from the bottom edge, sand on each step (Burrowers), turret nests on the steps (t≈85, 96), the outpost's emergency cache on a ledge (t≈92), mortars (t≈100). | Mixed; the swarm drops back at the end. |
| 5. The Lull | 108–124 | −7,680 → −9,300 | −100 | clear | `ground`: a wide upper terrace, wind-carved pillars. | A breather: Dust Kite flocks at the edges only; Rook's quiet line; the armour patch. |
| 6. Rim Road | 124–160 | −9,300 → −14,300 | −140 | light | `ground`: the rim road's switchbacks coming up from the bottom, abandoned militia crawlers, turret nests on the road (t≈140, 155). | The second pursuit, the last ambushes. |
| 7. Over the Rim | 160–172 | −14,300 → −15,340 | −60 | clear | `ground`: the rim's lip rising from the bottom, the militia guns firing up past the ship (flak bursts in the top band, scenery), the plain beyond. | Release; the swarm breaks off; level end. |
<!-- /data -->

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
  (the light stays top-left); units with 16 or 32 angles face their travel. **Not flipped** (they fly by
  the screen): armoured walkers, segment chains, the pursuits and **divers** (the Stinger, 2026-10-21): a
  Stinger enters from the top and dives down at the ship as in a forward level, into the front guns; a
  front one is unwarned, as going forward ("behind" by travel in the shares).
- **Fixed-facing ground units** turn their arcs with the travel (a Spine Turret's silence arc points
  down by travel, so it fires up at the ship while it rises toward it and falls silent once past:
  the same `arc` placement parameter as Level 19's up-facing nests).
- **Front/rear rules read travel**: the 60 %-front rule, the intel's directions and the threat profile
  above.
- **Edge warnings**: a new **TOP** edge (`WarningEdge` TOP, 3 s ahead) for a wave's first entry from the top in
  a reversed section (the pursuit swarms; the Wraiths' pop-ups are re-entries and never warn, 2026-10-21); LEFT/RIGHT as usual. Bottom entries are the
  default edge and unwarned, except the **first three after the turn** (t=27.5, 31.5, 35.5), which
  show the BOTTOM warning as a lesson.
- **Pickups** drift with the scroll's sign (up the screen in a reversed section). Bullets and shots are
  screen-space and unchanged.
- **Backdrop and parallax** run with the sign (new tiles from the bottom; `high-air` dust drifts up);
  the deep-coverage rule holds at the bottom edge.
- **Rook** keeps his slot beside the ship and shoots up into the swarm with his gun (his Autocannon
  at this visit, the balance plan's fit: the user's Q.2 of 2026-10-20). His rear bark is
  keyed to the **top** in a reversed section (three "Up top!" variants, the rear-wave bark's
  `top_lines` in the [wingmen data](../../../player/wingmen/README.md#radio-barks)) and is silent for
  bottom entries; during a swarm lunge's tell his slot moves out of the ship's lane. His AI must fly
  the turnabout and the reversed sections (§1.7.9).

### The pursuit swarm

A [Mote Swarm](../../../enemies/air/mote-swarm/README.md) in a new **`pursuit`** formation (§2.5,
part G); its numbers are first values for review. The defaults are the Mote Swarm's own `pursuit`
block (with its easy and hard hooks); each pursuit wave's `pursuit` block gives its break-off time
(`break_at`) and its refills:

- **Band**: its leader holds a band **50–150 px below the top edge** (behind the ship by travel),
  following the ship's x with a lag (**30 px/s** sideways; round 43 Q.1, was 90). The flock keeps the spec's boids (separation
  18 px).
- **Lunge**: every **4 s** (round 43 Q.1, was 6; a tell due while the flock is out of the band waits) a 0.8 s tell (the flock contracts into a tight ball ≈ 60 px across,
  brightens, a rising hum), then the leader dives straight down the ship's lane at **240 px/s** to
  140 px below where the ship was at the tell's start, and climbs back to the band at 200 px/s, the
  members re-forming round it. The dive does not home: a side step dodges it.
- **Size**: **10 motes** on entry (easy 8, hard 12, the formation lever), plus **refills of 3** (easy
  2, hard 4, the same lever) that join from the top edge every **10 s** (no warning: the swarm is on the
  screen), whether or not it was thinned. Deterministic and counted in the density: the first pursuit
  (t=24–108) gets 8 refills (34 motes), the second (t=124–163) 3 (19 motes). The flock holds at most 24
  motes: a refill only fills its free room, and the motes with no room are never spawned and count
  nowhere (not in the density, not in the kill ratio; the gap check's default, pending round 43).
- **Damage**: contact `tiny` 6 per mote (the mote dies); no shots.
- **Breaks off** at its wave's `break_at`: the leader climbs off the top at 200 px/s and the survivors
  follow (at t=108 into the lull, at t=163 under the rim guns' flak). They are not killed (they count
  as not killed in the kill ratio).
- Each pursuit enters under the **TOP** warning (3 s ahead; easy 4 s). The swarm fills the screen
  between the ambush waves, so the pacing rule holds while it is up.
- **If the beam melts the swarm** (1-HP motes in the Ion Beam's column die in a tick; m7g risk 3): the
  levers are the lunge's cadence and the band's sideways lag (data), not the motes' HP or more refills
  (the user's Q.3 of 2026-10-20). **Measured in step G4: it does.** With the plan's fit the swarm holds
  0.4 motes on average at medium (0.2 easy, 0.8 hard) over its 112 s, and a whole run sees 1–4 lunges:
  the beam takes the entry flock within a second of its entry and the homing pods and Rook take each
  refill as it joins. Neither lever moves it (the band's lag at 15 or 30 px/s instead of 90: the same
  0.35 motes, 2–3 lunges), so the approved numbers stay; accepted in round 43 (2026-10-20) with the
  captures (the levers left are outside Q.3's: the motes' HP, their refills, or homing that skips them).
- **Homing ignores the motes** (the user's choice of 2026-10-20): no homing shot, turret or Rook picks a
  mote as a target ([Mote Swarm](../../../enemies/air/mote-swarm/README.md), `seekers_ignore`); the swarm
  is fought with direct fire, and a homing shot that flies into a mote still hits it. Since the swarm
  sits on the ship's nose, that direct fire is mostly the front guns (the plan's Ion Beam) and Rook's
  Autocannon. Measured in step SW (`levelReport`, `pursuit` part, the plan's fit, 3 seeds): motes on
  the screen over the pursuits' ≈ 112 s at medium 0.45 → **0.77** (easy 0.21 → 0.35, hard 0.72 →
  1.03), and lunges with motes in the flock 1–2 → **2–4** of 18 a run (easy 1 → 1 of 13, hard 4–7 → 6–7
  of 23). The leader point lunges on with an empty flock, so most of the 18 lunges are empty. The beam
  still melts the entry flock and most refills. The approved pursuit numbers stay. The data levers
  measured, not applied (accepted in round 43 (2026-10-20): the band's lag at 30 px/s gives 0.87
  at medium (3–6 lunges with motes), a lunge every 4 s gives 0.94 (6–7 of 27).
- **Both levers applied** (the user's round 43 Q.1; step LQ): the lag 30 px/s and a lunge every 4 s (easy 5.5,
  hard 3), measured together (`levelReport`, the plan's fit, 3 seeds): motes on the screen at medium 0.77 →
  **1.03** (easy 0.35 → 0.44, hard 1.03 → 1.68), lunges with motes 2–4 of 18 → **8–9 of 27–28** a run (easy 1 of
  13 → 3–5 of 19–20, hard 6–7 of 23–24 → 16–21 of 33–35). The floor at medium 3.38 → 3.67 on the screen and 34.5 →
  35.6 shots a minute; gaps over 2 s 2 → 0 (easy 1, hard 0); the haul unchanged (typical 2,525, `bounty_scale`
  0.83); the autopilot wins every seed on every difficulty with the secondary met. A small unit lives 2.53× Level
  10's time (was 2.48×; the limit raised to 2.6×, accepted in round 43 (2026-10-20): see *Decisions*).

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary) plus the new `pursuit`. Data in
**Act 1 terms** with a `bounty_scale` (see *Credit budget*). "Bottom (ahead)" and "top (behind)" are
the screen edges with their meaning by travel.

- **Act HP factor** from the reference DPS 96 at L20 (M18 = a, round 36):
  **Burrower** (L15, 76) ×1.26 → 56 HP, **Dust Devil** (L18, 88) ×1.09 → 57 HP, **Wraith** (L10, 66)
  ×1.45 → 23 HP (medium). Level 20 joins the basis's `small_act_hp` (the Needler and the Stinger),
  and the **beam era's HP** (`beam_era_hp` ×2 on `small` units from Level 18, the user's "tougher, not
  more") applies: **Needler 24**, **Stinger 36**, **Spine Turret 16**, **Polyp Mortar 20**. The Dust
  Kite, the Skitter and the Mote Swarm (`tiny`) keep 1 HP. (The approved text's "keep their HP" for
  the Needler, Stinger, Spine Turret and Polyp Mortar predates those two rules.)
- **What changes the arithmetic** (m7g-gaps §1.5): after the turn the ahead waves come up from the
  bottom into the **rear** guns (the plan's fit: a Tail Gun L3 and two Micro-missile Pods L2 against the
  Ion Beam's 80 up front), so the bottom units live 2–4× longer on the screen than in Levels 18–19, and a
  rising nest is on the screen ≈ 4 s; the swarm, in the beam's column, holds little screen time.
- **Density and pacing** (the Act 2+ rules: `PacingTest`'s Act 2 rule, ≤ 3 gaps over 2 s on medium and
  hard, and `ActFloorTest`'s 2.5 enemies on the screen and 10 enemy shots a minute with the plan's fit;
  step G2's plan, the gap check's default and the user's Q.4 of 2026-10-20, pending round 43):
  "tougher, not more", **count-neutral**: **197 enemies** at medium after step G4's tuning (waves 175,
  53 of them motes, + ground targets 22) over the 167 s from the launch, **≈ 71 a minute** (Levels 18–19
  ran 101–107 because their waves carried the screen time; here the rear-gun waves do; easy 157, hard
  239). The approved table's
  anchors keep their times (the pursuits with their refills and break-offs, the BOTTOM lessons, the
  Burrower fields, the Dust Devil pairs, the Wraith pairs, the Stinger V at 71, the Needler lines at
  48.5/79/141.5, the nests at 42/85/140, the mortars at 60/100, the cache, the patch and every radio
  beat). **Eight bottom popcorn threes became firing threes** (Needler line abreasts at t=40, 63, 94.5,
  137.5; Stinger V-wings at 56.5, 75, 129.5, 154): at 24/36 HP against the rear guns they stay 2–4 s and
  fire (the Stingers since 2026-10-21 from the top, into the front guns: divers are not flipped). **Three spine nests ×3 were added** (t≈66 on the basin floor, ≈96 on a terrace step, ≈155 on the
  rim road), so ground rises during each pursuit. Hard: the extra Wraith pair; easy thinned by the
  formation lever.
- **Step G4's tuning** (`levelReport -Plevel=20`, the plan's fit, the autopilot's retreat play; two
  passes, accepted in round 43 (2026-10-20). Step G2's waves measured 8 gaps over 2 s at medium (the
  beam killed each strike wave and the swarm on entry; the lull's three flocks fell to the homing pods
  in about a second each) and a small unit lived 2.5× Level 10's time. Added: three strike waves (t=11.5
  kite stream, 15 kite V, 18.5 kite stream), **Needler pincers from the sides at t=22.5 and 25.5**
  (through the turnabout and beside the swarm's entry) and the lull as **six kite flocks of 3, 2.5 s
  apart** (t=110.5–123, left and right in turn). Count back: the eight firing threes are **firing pairs**
  on every difficulty (the rear guns serve one unit at a time), the Stinger V at 71 a three, the Burrower
  fields at 52.5, 87 and 150 pairs (the lesson at 35.5 stays a three). Measured after: medium 2 gaps over
  2 s (the lull's start 2.0 s, the run-out 2.1 s), hard none, easy none over 3 s; 3.15 enemies on the
  screen and 33 shots a minute; a small unit lives 3.7 s, 2.3× Level 10's (`BeamEraHpTest` holds a
  reversed level to 2.5×: its small units face the rear guns, as planned); the autopilot wins every
  seed on every difficulty without a retry.
- **The lull** (t=108–124; the user's Q.1 of 2026-10-20): **Dust Kite flocks at the screen's edges**
  (step G4: six flocks of 3 at t=110.5–123, in place of the approved pairs and three) never fire (Level
  19's crawl trick), so the Act 2 rule holds without an exception and Rook's line still plays over a
  nearly empty middle.
- Ahead (by travel) ≈ 74 % of the waves (69 % of those after the turn) and ≈ 57 % of the enemies (the
  threat profile's split counts the enemies); the Stingers after the turn come in at the top since
  2026-10-21 and count as behind (before: 84 %, 81 % and 63 %).
- The t=20.5 vanguard Skitters stream in 0.6 s apart, so all three enter before the turnabout's zero
  (≈ 22.1): a unit that spawns once the travel is reversed enters from the bottom.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | top (ahead, alternating edges) | Normal scroll |
| 9.5 | 1 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | top (ahead) | |
| 11.5 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | top (ahead, alternating edges) | Step G4: the strike's screen held (Act 2+ rule) |
| 13 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 4 | top (ahead, left) | As the ridge starts to cross |
| 15 | 1 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | top (ahead) | Step G4: the strike's screen held |
| 16.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | top (ahead) | |
| 18.5 | 1 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | top (ahead, alternating edges) | Step G4: the strike's screen held |
| 20.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | top (ahead, alternating edges) | The swarm's vanguard out of the ridge's wake, all in before the turn; Okafor's order at t=20 |
| 22.5 | 2 | pincer | [Needler](../../../enemies/air/needler/README.md) | 2 | sides | Step G4: through the turnabout, 1 per edge; warned 3 s ahead |
| 24 | 2 | pursuit | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 + 8 × 3 | top (behind) | **Pursuit 1**: TOP warning from t=21; refills at t=34, 44 … 104; drops back at t=108 |
| 25.5 | 2 | pincer | [Needler](../../../enemies/air/needler/README.md) | 2 | sides | Step G4: beside the swarm's entry, 1 per edge; warned 3 s ahead |
| 27.5 | 3 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead, alternating edges) | The first bottom entry, BOTTOM-warned (lesson) |
| 31.5 | 3 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | BOTTOM-warned (lesson) |
| 35.5 | 3 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 3 | bottom (ahead) | Buried, below the ship; BOTTOM-warned (lesson); act HP factor |
| 40 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | bottom (ahead) | Over the dune ridge's nest (t≈42); a firing pair in place of the approved Skitter snake |
| 44 | 3 | pack | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 2 | bottom (ahead) | Their pull with the swarm above; act HP factor |
| 48.5 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | bottom (ahead) | |
| 52.5 | 3 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 2 | bottom (ahead) | |
| 56.5 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | top (a diver, not flipped) | A firing pair in place of the approved kite stream |
| 59 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | top (behind) | Cloaked inside the swarm; no edge warning (a Wraith's re-entry; Rook's line at t=56); act HP factor |
| 63 | 3 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | bottom (ahead) | A firing pair in place of the approved kite snake |
| 67 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | bottom (ahead, alternating edges) | Over the floor nest (t≈66) |
| 71 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | top (a diver, not flipped) | Up the first terrace; the leader drops an overdrive |
| 75 | 4 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | top (a diver, not flipped) | A firing pair in place of the approved kite stream |
| 79 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | bottom (ahead) | |
| 80 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | top (behind) | Hard only: an extra Wraith pair, no edge warning (a Wraith's re-entry) |
| 83 | 4 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead, right) | Over the terrace nest (t≈85) |
| 87 | 4 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 2 | bottom (ahead) | In the terrace sand |
| 90.5 | 4 | pincer | [Needler](../../../enemies/air/needler/README.md) | 2 | sides | 1 per edge; warned 3 s ahead |
| 94.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | bottom (ahead) | Past the emergency cache (the secret, t≈92); a firing pair in place of the approved kite V-wing |
| 101 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | top (behind) | No edge warning (a Wraith's re-entry; Rook's line at t=98) |
| 104.5 | 4 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead, alternating edges) | |
| 108 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 3 | bottom (ahead, left) | The swarm drops back off the top |
| 110.5 | 5 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | **The lull**: kite flocks at the edges only (they never fire); step G4: six flocks 2.5 s apart (Act 2+ rule) |
| 113 | 5 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | Rook's quiet line from t=113 |
| 115.5 | 5 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | |
| 118 | 5 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | The armour patch at t=117 |
| 120.5 | 5 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | |
| 123 | 5 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | |
| 124 | 6 | pursuit | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 + 3 × 3 | top (behind) | **Pursuit 2**: TOP warning from t=121; refills at t=134, 144, 154; breaks off at t=163 |
| 125.5 | 6 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead, alternating edges) | |
| 129.5 | 6 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | top (a diver, not flipped) | A firing pair in place of the approved Skitter snake |
| 133.5 | 6 | pack | [Dust Devil](../../../enemies/ground/dust-devil/README.md) | 2 | bottom (ahead) | On the road's switchback; the last one drops a shield cell |
| 137.5 | 6 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | bottom (ahead) | Over the road nest (t≈140); a firing pair in place of the approved kite V-wing |
| 141.5 | 6 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 2 | bottom (ahead) | |
| 146.5 | 6 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | top (behind) | No edge warning (a Wraith's re-entry; Rook's line at t=143.5) |
| 150 | 6 | field | [Burrower](../../../enemies/ground/burrower/README.md) | 2 | bottom (ahead) | In the road's drifts |
| 154 | 6 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 2 | top (a diver, not flipped) | Over the last road nest (t≈155); a firing pair in place of the approved kite snake |
| 157.5 | 6 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | bottom (ahead, alternating edges) | |
| 161 | 7 | stream | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead, alternating edges) | The rim guns open fire at t=160 |
| 164.5 | 7 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 3 | bottom (ahead) | The last wave |

Totals: Dust Kite 54 · Skitter 16 · Needler 22 · Mote Swarm 53 · Burrower 9 · Dust Devil 4 · Stinger 11 · Wraith 6.
<!-- /data -->

## Ground targets

All rise from the bottom with the terrain (the reversed sections' rule): a target's `at` time is
when it enters at its edge by travel. The nests carry no `arc` key: the travel frame turns their arcs
(a Spine Turret's silence arc points down by travel). Step G2 added three nests ×3 (t≈66, 96, 155) to
the approved table (see *Waves*); their names match step ART-G1's props (the dune ridge's piece sits on
its nest's row).

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 3 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on a dune ridge (t≈42), arcs by travel | Bounty 12 each; fire up at the ship while rising toward it |
| 3 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) pair in the salt flats (t≈60) | Bounty 15 each; their markers on the ground ahead |
| 3 | Spine Turret nest ×3 on the basin floor (t≈66) | Bounty |
| 4 | Spine Turret nest ×3 on a terrace step (t≈85) | Bounty |
| 4 | Hellas Outpost emergency cache (`ground` trigger, 20 HP, t≈92, x 430) | The secret: frees a crate worth 100 (256) |
| 4 | Spine Turret nest ×3 on a higher terrace step (t≈96) | Bounty |
| 4 | Polyp Mortar pair on a terrace (t≈100) | Bounty |
| 6 | Spine Turret nest ×3 on the rim road (t≈140) | Bounty |
| 6 | Spine Turret nest ×3 on the rim road's last bend (t≈155) | Bounty |
<!-- /data -->

The Burrowers and Dust Devils are waves; they are counted with the ground units in the budget.

## Hazards

- **The reverse scroll** and **the pursuit swarm** (above): the level's two mechanics.
- **Dust Devil pull** (its spec): within 130 px, toward the centre; with the swarm above, the pull
  is never a trap against the ship's speed.
- No terrain collision (the terraces and the rim road are the `ground` layer's look, not walls); no
  wind.
- **The travelling ridge** (scenery, nothing hits it): the data's `ridge` key places its pass (t=12, 8 s across
  from the left edge, 70 px below the top edge; round 43 Q.4).

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

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Hellas basin, Aegis. The Vrell are massing on the floor. Hit them before they move." |
| t=12.5 | Varga | "The basin floor is moving. Something huge, under the sand." |
| t=20 | Okafor | "All Aegis units, break off! Fall back to the rim!" |
| t=27.5 | Varga | "Front guns on the swarm. Rear guns on what's ahead." |
| t=35 | Generic colonial (Ares Control) | "The rim road is open, Aegis. Our guns wait at the top." |
| t=47 | Varga | "More Burrowers below you. Watch for the bulge." |
| t=56 | Rook | "Up top! Cloakers in the swarm!" |
| t=64 | Rook | "That swarm is not getting smaller, Lancer!" |
| t=75 | Okafor | "Keep moving. Don't let them pin you on the terraces." |
| t=98 | Rook | "Cloakers up top again!" |
| t=106 | Varga | "The swarm is dropping back. They're regrouping." |
| t=113 (the quiet beat) | Rook | "Lancer... if I don't come back from one of these, call my mum yourself. No form letters." |
| t=124.5 | Varga | "They've found us again. Here they come." |
| t=131 | Generic colonial (Ares Control) | "Militia on the rim, Aegis. We can see your dust. Keep coming." |
| t=143.5 | Rook | "Up top! More cloakers!" |
| t=152 | Okafor | "The rim is right ahead, Lancer. Get over the lip." |
| t=160 | Generic colonial (Ares Control) | "Rim guns, open fire! Clear their tail!" |
| Secondary met (at the end) | Okafor | "You thinned them on the way out. They'll remember that." |
| Level end | Okafor | "We gave up the basin today. We will take it back. Come home, Aegis." |
<!-- /data -->

Timeline check (text): the two-page lines close at ≈ 12.2 (t=1) and ≈ 124.3 (Rook's quiet line,
t=113); Ares Control's t=131 line starts 0.2 s late (Varga's closes at ≈ 131.2), within the rule;
every other timed line starts on time. Rook's top lines come before the Wraith pairs (t=56, 98,
143.5), which carry no TOP warning since 2026-10-21; his scripted lines stay; the pursuits'
TOP entries (t=24, 124) get his "Up top!" bark (the rear-wave bark's `top_lines`). **The quiet beat**: Rook's line plays with the screen nearly empty and the music down to its
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
ridge; a low earth groan, no bubbly or popping sounds) fading by t=40 (the music's `ambience_layers`:
`basin-rumble` from t=12, in over 2 s, out over its last 12 s to t=40; step AUD-G's sound). The rim
guns' flak is the data's `flak` with `band: top` (bursts in the top band from t=160, every 1.5 s,
hitting nothing). New sounds this level needs:
the pursuit swarm's band hum and the lunge's rising hum, the TOP warning (the edge warning's sound),
the rim guns' flak (a distant flak sound, as Level 17's militia); listed in
[sfx](../../../audio/sfx/README.md) by the act's audio step.

## Credit budget

Budget(20) = 700 × 1.07¹⁹ ≈ **2,532** ([economy](../../../systems/economy/README.md#per-level-budget)).
Data in **Act 1 terms** (Dust Kite 4, Mote 2, Skitter 5, Needler 12, Stinger 15, Wraith 30,
Burrower 24, Dust Devil 26, Spine Turret 12, Polyp Mortar 15, the crate 100, the secondary 100),
× the act factor **2.56**, bounties × the scale (the beam era raises HP, not bounties). Round 36 kept
a **`bounty_scale` of 0.95** for the approved 178 enemies (typical 2,525, −0.3 %); with the waves
re-planned to the Act 2+ floor (192 enemies, the firing groups and the three nests pay more) the gap
check's paper value was 0.77. Step G4 set **0.83** from the tuned waves (197 enemies, fewer Burrowers
and firing units: the scale whose typical haul lands nearest the budget, `TypicalHaulTest`, which
counts the pursuits' refills as the table does), within §1.5's band 0.70–0.85; pending round 43.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Dust Kite 54 × 4 + Skitter 16 × 5 + Needler 22 × 12 + Mote Swarm 53 × 2 + Burrower 9 × 24 + Dust Devil 4 × 26 + Stinger 11 × 15 + Wraith 6 × 30 | 2,785 | 1,807 |
| Ground targets: Spine Turret 18 × 12 + Polyp Mortar 4 × 15 | 578 | 462 |
| Secret: emergency cache (hidden crate, 4% of budget) | 256 | 128 |
| Secondary: thin them out | 256 | 128 |
| **Total** (bounty scale 0.83) | **3,875** | **2,525** |
| Budget(n) = the typical haul's target; typical -0 %, perfect 1.53 × budget | | 2,532 |
<!-- /data -->

**Balance plan**: the plan's L20 visit comes from part A's Act 3 balance plan (M18; the
[Ion Beam](../../../player/weapons/ion-beam/README.md)'s hand plan buys the Tail Gun L3 here); Rook
flies his Autocannon at this visit (the plan's `fit` at "20", the user's Q.2 of 2026-10-20: the level is
air-heavy). The level expects `rear` (Tail Gun, Rear Lance, or the
[Rear-guard Drone Bay](../../../player/weapons/rear-guard-drone-bay/README.md) unlocked at this visit) and
`homing`, and is flyable with the front guns alone, slowly (a rear-less ship turns the bottom waves
into contact threats it must dodge and shoot once they pass).

## Difficulty notes

- **Easy**: the swarm 8 on entry, refills of 2, a lunge every 5.5 s with a 1.2 s tell; the TOP warning
  4 s ahead; the Burrowers' ring of 8 and the Dust Devils' weaker pull (their specs).
- **Hard**: the swarm 12 on entry, refills of 4, a lunge every 3 s; the Burrowers' second ring and
  the Dust Devils' 3-arm spiral (their specs); an extra Wraith pair from the top at t≈80 (unwarned);
  the Wraiths' hard hooks; the firing groups (Needler lines, Stinger V-wings) one unit smaller.

## Concept art

Production art of M7 part G's round 43 (step ART-G1; the mood from the Hellas mood still in
[mars](../../../world/mars/README.md), round 36 MA.4), accepted in round 43 (2026-10-20). Prompts:
[concept/prompts.md](concept/prompts.md). Still to come in part G: the pursuit swarm's band and lunge tell, the
TOP edge warning, the rear-guard drone (ART-G2) and the round's captures.

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r43-a.png](concept/backdrop-final-r43-a.png) | Level 20 backdrop final (`tools/art/backdrop_l20.py`): one strip keyed by strip row, run both ways: the basin floor's salt flats and dune sea with the Vrell staging grounds and the t≈42 dune ridge, the terraces, the upper terrace's pillars, the rim road's switchbacks with abandoned militia crawlers, the rim's lip with the militia's flak guns and the rust plain beyond; the play field at twelve times (no units or props), the strip at 1/40, the pieces and seams (sheet) | chosen (round 43: approved as final) |
| [concept/backdrop-final-r43-a.gif](concept/backdrop-final-r43-a.gif) | Level 20 backdrop final: the whole level flown at 0.5x, the ground moving up the screen after the turn, 10 fps | chosen (round 43: approved as final) |
| [concept/l20-props-final-r43-a.png](concept/l20-props-final-r43-a.png) | Level 20 props final (`tools/art/l20_props.py`): the travelling ridge (lengths, head, tail in 16 headings, the crest glow, sand slides; shared with Level 21's Dust Colossus) crossing the floor at t=12-20; the emergency cache (intact, hit, opened) and its strobe; Okafor's briefing image `level-20-hellas-fallback` (sheet) | chosen (round 43: approved as final) |
| [concept/l20-props-final-r43-a.gif](concept/l20-props-final-r43-a.gif) | Level 20 props final: the ridge crossing near the top edge over the backdrop, its cracks pulsing, 10 fps | chosen (round 43: approved as final) |
| [concept/level-20-capture-r43-a.mp4](concept/level-20-capture-r43-a.mp4) | Level 20 whole run with sound, the plan's fit, test autopilot (level t ≈ mp4 time + 1 s): the strike and the Dust Colossus ridge crossing, the turnabout (the scroll easing to zero ≈ 0:21 and reversing, LEFT/RIGHT, TOP "! REAR" and bottom "! AHEAD" warnings), the pursuit swarm entering at 0:23 (pale-rimmed motes, readable on the sand) and melting under the beam within a second or two (each refill too), the warning box handing over without an empty frame, Rook's "Up top!" lines (0:56, 1:38, 2:23), the Wraiths from the rear, the dune-ridge, terrace and rim-road nests and mortars, the emergency cache passing on its ledge at 1:32–1:36 (not opened: SECRETS 0 / 1; no strobe halo is drawn, see the cache-strobe rows), the swarm dropping back, the lull with the supply drone and armour patch, the second pursuit, the rim guns and their flak (from 2:39 a bright orange-cored pop every 1.5 s in the top band, ~0.1 s, then a small dark smudge), the debrief (A, 167 / 197, Thin them out met). 205 s | chosen (round 43: accepted) |
| [concept/level-20-capture-r43-a.png](concept/level-20-capture-r43-a.png) | 20-tile sheet of that run (HUD left panel + field) + a row of 1:1 crops: the swarm's tell ring and its lunge (from the swarm run), the rear-guard drone and its amber shot (from the drone run), a flak burst, the emergency cache, the Colossus ridge, the rim guns | chosen (round 43: accepted) |
| [concept/level-20-swarm-r43-a.mp4](concept/level-20-swarm-r43-a.mp4) | Demonstration of the pursuit swarm only: the same fit but the autopilot's fire held (snapshot hook) and no Rook, so the motes live: the entry under the TOP warning, the band at the top following the ship, tells (the flock bunches, glows white-hot and a pale ring flashes round it, readable at play size: 0:16.5, 0:22.5, 0:28.5), lunges down the lane (pale-rimmed motes, no dark shadow flock) and climbs back, refills joining, the break-off climbing off the top at 1:33, the lull's start. Not a normal run: the ship never fires. 105 s | chosen (round 43: accepted) |
| [concept/level-20-swarm-r43-a.png](concept/level-20-swarm-r43-a.png) | 12-tile sheet of that run (field only) + 1:1 crops of two tells and a lunge | chosen (round 43: accepted) |
| [concept/level-20-drone-r43-a.mp4](concept/level-20-drone-r43-a.mp4) | Level 20 t≈15–63 with the Rear-guard Drone Bay L1 in place of the left Micro-Missile Pod (the plan does not fit it): the drone trailing below the ship through the turnabout and the first pursuit, swinging out in turns, firing straight back with the Tail Gun: the drone now a small pale-rimmed dot, its shot amber against the Tail Gun's twin cyan-white bolts. 48 s | chosen (round 43: accepted) |
| [concept/level-20-drone-r43-a.png](concept/level-20-drone-r43-a.png) | 6-tile sheet of that excerpt + four 1:1 crops of the drone | chosen (round 43: accepted) |
| [concept/level-20-cache-strobe-r43-a.mp4](concept/level-20-cache-strobe-r43-a.mp4) | **Snapshot-patched build, not the live game:** the emergency cache passing (t≈92–98) with GFX2's strobe halo drawn: the live game never loads the cache's `-glow` frames, because it is a destructible (`hp: 20`, no `hits`) and `GroundPasses` loads glow frames only for triggers, so `StrobeHalo` is never reached (a bug for the next game step). With the glow frames loaded the yellow halo pulses twice a second and reads at play scale. With sound, 6 s | chosen (round 43: accepted) |
| [concept/level-20-cache-strobe-r43-a.png](concept/level-20-cache-strobe-r43-a.png) | Two field frames of that clip (lamp lit, dark) + five 1:1 crops of the cache, lit and dark | chosen (round 43: accepted) |

Varga's page image `level-20-filed-plate` was produced early with Level 19's batch (round 42, the walker wreck's
plate): [briefing-images-final-r42-a.png](../../../ui/briefing/concept/briefing-images-final-r42-a.png); accepted in round 43 (2026-10-20).

## Implementation

- [x] Level data `data.yaml` (M7 part G step G2): sections with **signed** speeds and eases (the
      turnabout's 4 s ease through zero), waves with travel-relative `from`, the pursuits' blocks, the
      BOTTOM lessons' `warning`, ground targets, radio, budget, threat profile, briefing, the strip
      backdrop (step ART-G1's), music with the basin rumble's `ambience_layers`, the rim flak's
      `band: top`, the travelling ridge's `ridge` key (round 43 Q.4); loaded and checked by content's `LevelData` and `RetreatChecks` (test
      `RetreatKeysTest`); mapped for the simulation in step G4 (`RetreatRules`: the pursuit's block from
      the Mote Swarm's defaults, the wave and the hooks, its refills by the formation lever)
- [x] Reverse scroll (M5 = a): `LevelData.Section` accepts negative speeds; entry edges and routes
      follow the sign; ground objects, roads, backdrop tiling and parallax run backwards; front/rear,
      the 60 % rule and the intel's directions read travel; arcs of fixed-facing units by travel;
      pickups drift with the sign
- [x] `WarningEdge` TOP; the BOTTOM lesson warnings on the first three waves after the turn
- [x] The `pursuit` formation: the band, the lag, the lunge with its tell, the refills, the break-off;
      its motes counted in the density and the kill ratio
- [x] Rook in reverse scroll: his slot, his top barks (the three "Up top!" variants are in the data:
      the rear-wave bark's `top_lines`, step G2), out of the lane during a lunge's tell; the quiet beat
      line without `requires: escort` (decided in round 36, Q.20d; in the data)
- [x] Wave script: ≥ 55 enemies a minute at medium (`DensityTest`, 70.8 measured), the Act 2+ rules
      (`PacingTest`'s Act 2 rule, `ActFloorTest`; `BeamEraHpTest` with a reversed level's 2.6×), ≥ 60 % of
      waves ahead by travel (`Level20Test`), the act HP factor for the Burrower, Dust Devil and Wraith,
      `small_act_hp` (Level 20 in its levels since step G2) — the data written in step G2, measured and
      tuned in step G4 (two passes, pending round 43)
- [x] Ground targets, the emergency-cache secret (20 HP trigger, crate 100), the supply drone's armour
      patch at t=117, the overdrive, the Dust Devil's shield cell
- [x] Kill-ratio secondary at 65 %, judged at the level end (§1.7); reachable without a mote
      (`Level20Test`), met by the autopilot on every seed and difficulty
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`, with Rook and without)
- [x] Threat profile by travel, Varga's four intel lines and Rook's teaser fitting the intel panel
      (`IntelPanelLayoutTest`)
- [x] Briefing: Okafor's page (the fallback, the walker report) and Varga's (the filed plate), each
      one screen (`BriefingLayoutTest`), with their images
- [x] Typical haul within ±5 % of budget(20) = 2,532 with the level's `bounty_scale` (`TypicalHaulTest`:
      0.83, −0.3 %)
- [x] Music: track 8's stems by section, the lull's drop; the basin rumble
- [x] Easy/hard variations; the autopilot flies the turnabout and the reversed sections with Rook on
      every difficulty (`--escort none` too); covered by `Level20Test`'s flights, no golden recording beyond Level 01 — the autopilot's retreat play
      (`RetreatPlay`, step G4: the middle band, the unit it kills soonest with either gun, out of a lunge's
      path, a look-ahead over the bullets from both edges) wins every seed on every difficulty with Rook
      and without him (`Level20Test`); the golden replay is not applicable (only Level 01 has one; `Level20Test`'s same-seed test covers determinism)
- [x] Production art and captures for part G's round

## Open questions

- None open: round 36 answered every question (see *Decisions*).

## Decisions

- 2026-10-12 (overnight design phase): promoted from the Act 3 roster to a draft level document.
  Taken as the recommended options, accepted in round 36 (2026-10-13): M4 = a (the terrain moves
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
- 2026-10-20 (user, the gap check's questions): the lull held by non-firing Dust Kite flocks of 4 at the
  edges, no `PacingTest` exception (Q.1); Rook flies his Autocannon at this visit (Q.2); if the beam
  melts the swarm, the lever is the lunge's cadence and the band's lag, not the motes' HP or more
  refills (Q.3); the density count-neutral, ≈ 190–200 enemies and ≈ 70 a minute, popcorn turned into
  firing groups plus three nests, more only if the measured floor fails (Q.4).
- 2026-10-20 (M7 part G, step G2): the numbers moved into [data.yaml](data.yaml), the tables rendered
  from it; the waves re-planned to the Act 2+ floor as the user's Q.4 (192 enemies, ≈ 69 a minute;
  eight popcorn threes as firing threes, three nests added, the lull's kite flocks); the beam era's HP
  and `small_act_hp` stated (the approved "keep their HP" was stale); the paper `bounty_scale` 0.77.
  Choices of the step, accepted in round 43 (2026-10-20): each pursuit's break-off is its wave's
  `break_at` (t=108 and t=163; the second breaks off inside section 7, so not "its section's end"); the
  BOTTOM lessons use the existing wave key `warning` on a front wave in a reversed section (no new
  `warn` key); the t=20.5 vanguard Skitters stream 0.6 s apart (all in before the turnabout's zero); the
  pursuits' TOP warnings 4 s on easy (`easy: {warning: 4}`; the Wraith waves' copies, moot since 2026-10-21, removed by step CI1); the cache's sprite named
  `emergency-cache`; the basin rumble as a second ambience loop (`ambience_layers`) and the rim flak as
  `flak` with `band: top`; the threat profile's hazards `reverse scroll`, `fallback under pursuit`.
- 2026-10-20 (M7 part G, step G4): mapped (`RetreatRules`) and flown by the autopilot's retreat play;
  measured with `levelReport` and tuned in two passes (accepted in round 43 (2026-10-20): three
  strike waves, Needler pincers at t=22.5 and 25.5, the lull as six kite flocks of 3, the firing threes as
  pairs on every difficulty, three Burrower fields as pairs (197 enemies, ≈ 71 a minute; medium 2 gaps
  over 2 s, hard none); `bounty_scale` 0.83 from the measured haul; `BeamEraHpTest` holds a reversed
  level to 2.5× Level 10's small-unit time (measured 2.3×: the rear guns, not the beam, face them); the
  swarm melts under the plan's fit (0.4 motes on average, 1–4 lunges a run) and the band's lag does not
  change it, so the approved pursuit numbers stay. ART-G1's placement notes: the rim road nests stand on
  the road (t≈140: x 124/72/120 on the left hairpin; t≈155: x 164/199/240 on the leg) and the first
  terrace mortar enters at t=100.5 (on the tread, not the riser's face).
- 2026-10-20 (user, after step G4's measure): **"Homing ignores motes"**: the seekers (the player's
  homing weapons and turrets, Rook's target, any target-picking homing) never lock onto a mote; the
  swarm is fought with direct fire. Step SW made it the Mote Swarm's `seekers_ignore: true` and
  re-measured the level: medium 0.77 motes on the screen (was 0.45), 2–4 lunges with motes a run (was
  1–2); the floor 3.38 on the screen and 34.5 shots a minute (was 3.15 and 33.1); the small units'
  time 2.48× Level 10's (was 2.30×, limit 2.5); the autopilot wins 3/3 on every difficulty, the
  secondary met 3/3; the haul unchanged (typical 2,525, `bounty_scale` 0.83). No data tuned: the lag
  and cadence levers are in *The pursuit swarm*, accepted in round 43 (2026-10-20).
- 2026-10-20 (M7 part G, step R43): the code and tests of part G are in (reverse scroll, TOP warnings, the pursuit, Rook's retreat play, the kill ratio, the voiced radio: those Implementation items ticked); the art, the voices, the captures, the build choices and five questions (the swarm's levers, the beam-era limit, turrets as seekers, the ridge's data key, the hangar's and briefing's art status) are in [round 43](../../../concept-rounds/round-43/README.md), accepted in round 43 (2026-10-20).
- 2026-10-20 (user, round 43, closed): the backdrop (F.a), the props (F.b) and Okafor's briefing image
  `level-20-hellas-fallback` (F.e) approved as final (`art: final`, their concept rows `chosen`); the voices (V.a-V.g),
  the captures (K.a-K.d) and the build choices (B.a-B.g, every sub-choice) accepted as built. Q.1 **both levers** (the
  mote swarm's band lag 90 to 30 px/s and a lunge every 4 s, data only; to be applied and re-measured), Q.2 the beam-era
  limit for a reversed level accepted, Q.3 yes (turrets count as seekers), Q.4 the travelling ridge's pass gets a data
  key (to build), Q.5 final for the approved items only. The sounds: `basin-rumble` **a**, `swarm-hum` **a**,
  `swarm-lunge` **a** chosen (S.l-S.n). See [round 43](../../../concept-rounds/round-43/README.md).
- 2026-10-20 (round 43 Q.1, step LQ): **both levers applied** in the Mote Swarm's `pursuit` block (the lag 30 px/s, a
  lunge every 4 s; the easy and hard hooks 5.5 s and 3 s, keeping the medium cadence's ratio: our reading, accepted in round 43 (2026-10-20)) and re-measured together: motes on the screen at medium 0.77 → 1.03, lunges with motes 2–4 → 8–9 a
  run, the floor 3.67 and 35.6 shots a minute, no gap over 2 s at medium, the haul unchanged, the autopilot wins
  everywhere (see *The pursuit swarm*). The motes now held in the beam's column take some of its time, so a small unit
  lives **2.53×** Level 10's time (was 2.48×, over Q.2's 2.5): `BeamEraHpTest`'s limit for a reversed level raised to
  **2.6×**, accepted in round 43 (2026-10-20) (the other way out: lower the Needler's and Stinger's HP on this level).
- 2026-10-20 (round 43 Q.4, step LQ): the travelling ridge's pass is the level key **`ridge`** `{t: 12, seconds: 8,
  from: left, to: right, depth: 70}` (the [schemas](../../../tech/architecture/README.md#data-file-schemas)), read by
  the game's `RidgeLooks` instead of the `basin-rumble` layer's start; the approved track moved into it unchanged
  (the head off the left edge 70 px below the top at t=12, off the right edge 8 s later, the tail gone at t=27).
  Level 21's ridge pass uses the same key, from the right edge.
- 2026-10-20 (user, round 44, closed): **L.a** the swarm's two levers (the band's lag 90 to 30 px/s, a lunge every 4 s at medium) accepted as built, **L.b** easy 5.5 s and hard 3 s, **L.c** the reversed-level limit of `BeamEraHpTest` raised to 2.6x (the swarm lives longer; the alternative, lowering Level 20's small HP, is not taken), **L.d** the `ridge {t, seconds, from, to, depth}` level key (Level 20's track in its data; Level 21 uses the same key) — all as recommended.
- 2026-10-21: Edge-warning rule change (user, built by step EW): the Wraith pop-ups (t=59, 80, 101, 146.5) are no longer TOP-warned
  (a unit's own re-entry); the pursuits' first entry from the top keeps its TOP warning; Rook's scripted lines stay.
- 2026-10-21 (step CI1): the moot `easy: {warning: 4}` keys of the three rear-ambush Wraith pairs (t=59, 101, 146.5)
  removed and their notes say "no edge warning" (a rear ambush's pop-up never warns: `WaveSchedule`); the pursuits'
  `easy: {warning: 4}` stays (it drives their TOP warning). The L3 wave strip marks the ridge pass (12–20 s), the
  `ridge` key's `t` to `t + seconds` (round 44 Q.8 d, `Intel`'s `RIDGE` mark; drawn by the game's intel panel).
- 2026-10-21 (step SF, the user's Act 3 playthrough): "stingers flying backwards look strange. I think when flying
  backwards, the stingers should still enter from the top." A diver (an enemy with a `movement.dive`: only the
  Stinger) is no longer flipped in a reversed scroll (`WaveSchedule.flips`): the V-wings at t=56.5, 71, 75, 129.5
  and 154 enter from the top and dive down at the ship as in a forward level, their warning a forward level's (a
  front wave: none). The wave table reads "top (a diver, not flipped)" and the shares count them behind by travel
  (enemies ahead 63 → 57 %, waves after the turn 81 → 69 %, still over the 60 % rule). Measured after
  (`levelReport -Plevel=20`, the test fit): medium 197 enemies, 3.43 on the screen, 33.8 shots a minute, no gap
  over 2 s, typical haul 2,525 vs 2,532 (−0.3 %), a small unit 3.57 s on the screen = 2.23× Level 10's (≤ 2.6);
  easy one gap of 2.2 s (rule: none over 3 s), hard none; the autopilot wins 3/3 on every difficulty, no retry.
