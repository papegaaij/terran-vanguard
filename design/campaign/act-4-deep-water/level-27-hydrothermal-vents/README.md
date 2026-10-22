---
title: Level 27 – Hydrothermal Vents
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/naval/depth-hunter, ../../../enemies/naval/glassfin, ../../../enemies/naval/barbfin, ../../../enemies/naval/eel-swarm, ../../../enemies/naval/abyss-ray, ../../../enemies/naval/siren, ../../../enemies/naval/spiral-nautilus, ../../../enemies/naval/glow-angler, ../../../enemies/ground/spine-turret, ../../../enemies/ground/polyp-mortar, ../../../world/europa, ../../../player/wingmen, ../../../player/specials, ../../../story/twist]
updated: 2026-10-22
---

# Level 27 – Hydrothermal Vents

## Summary

The vent field before the hatchery: black smokers, amber bacterial mats and **heat plumes** that erupt
in cycles, burn anything above them and **bend every shot** that crosses them. Aegis Wing flies it
without Rook (Aegis Two went down in Level 26; the escort slot holds a
[Bulwark](../../../player/wingmen/README.md#heavy-drone-bulwark-escort-alternative-l22) drone) and
meets, for the first time on the screen, the grey
[Depth Hunters](../../../enemies/naval/depth-hunter/README.md): human-built submarine drones that rise
in columns from below and fire slow, shootable homing torpedoes up the screen, so the level asks for
`rear` guns and `anti-sub` fire. **Hint 3** of the [twist](../../../story/twist/README.md): a Hunter
wreck scorched by a plume shows its Helix hull code under the paint; Varga reads it, Okafor sends it
up to High Command (radio and a placed prop, no wreck crawl). The secondary silences the relay beacons
on the ridge. No boss (no mid-boss in Act 4); density 4 at the Act 2+ floor (≈ 97 enemies a
minute at medium). 150 s: the field's edge, the black smokers, the chimney garden, the beacon ridge and
the hatchery gate.

**Status: approved in round 48 (2026-10-22); drafted (Act 4 design phase, step D-L27-28, 2026-10-21), decided in round 48 (2026-10-22).**
Written from the act roster, the gap analysis's recommended answers (Q1 b, Q5 a, Q7 a, Q11 a; all
pending in round 48) and the wave 1 documents; every number is a first draft for part A. There is no
`data.yaml` yet (a `data.yaml` under `design/campaign/` would be loaded and flown by `check`): the tables
below are hand-written drafts.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

> **Commander Okafor:** "Aegis Two is missing. Rook's beacon went dark in the black water, and a
> Bulwark drone flies his slot until we find him. The vents are the last stretch before the hatchery,
> Lancer."
>
> **Dr. Varga:** "Vent plumes erupt in cycles and bend every shot that crosses them. Something grey
> rises from below and fires up: stay high and shoot its torpedoes down. Rear guns, and anti-sub."

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`):

- Okafor's page `level-27-vent-field`: a sonar chart of the vent field: the route from the plain's edge
  over the black smokers and the chimney garden to the beacon ridge and the hatchery gate's glow, the
  vents ringed amber, the four beacons as red-gold dots, Aegis Two's last position marked with an
  empty circle.
- Varga's page `level-27-plume-cycle`: her diagram of a plume's cycle (the glowing mouth, the lifted
  silt, the eruption column, the three timings 1.4 s / 1.6 s / 1.5 s) with a shot bending off it, and in
  the corner a grey hull under the ship's lane with a torpedo rising.

*Hangar teaser* (shop screen before L27; Okafor, not voiced; Rook cannot be the voice of this one):

> **Commander Okafor:** "Vent field ahead, and a drone in Rook's slot. Bring flares and a rear gun, Lancer."

## Threat profile

Draft; directions are counted by each wave's `from` (the intel's rule): 68 % front,
24 % sides, 8 % rear (the Hunter columns and the rear ambushes).

| Field | Value |
|---|---|
| Dominant layers | `air` (the play plane under water: Glassfin schools, Barbfin groups, Depth Hunters, Eel Swarms, Nautilus pairs, Sirens, an Abyss Ray), `ground` (smoker chimneys with Spine Turret nests and Polyp Mortars, the relay beacons), `sub` (the Ray's shadow and the Hunters' rise from below) |
| Attack directions | front 68% · sides 24% · rear 8% |
| Density | 4 |
| Recommended traits | `anti-sub`, `rear` |
| Hazards | Heat plumes (burn, and bend shots that cross them); the water's rules ([europa](../../../world/europa/README.md#under-water-rules)) |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: Europa, the vent field, `air` + `ground`, front and rear · **L1**: + directions (front, sides, rear), density 4, hazard "heat plumes", the objective `REACH THE HATCHERY GATE` and the secondary `BEACONS 0/4` · **L2**: + Depth Hunter (new, shown as "unmarked grey, human-built"), Eel Swarm, Abyss Ray, Siren, Spiral Nautilus, Glow Angler, Barbfin and Glassfin portraits; the plumes' rings on the map · **L3**: + `anti-sub` and `rear` highlighted; the timeline strip marks the three Hunter columns (56, 96 and 126 s) and the beacon ridge (94–124 s); 1 secret |

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "The vent field. Hot water, and Rook still missing." |
| L1 | "Plumes bend your shots. Contacts from below and behind." |
| L2 | "New: grey hulls, not Vrell. They fire up from below." |
| L3 | "Rear guns for the grey hulls. Anti-sub, and flares for the torpedoes." |

## Objective

- **Primary** `reach-end`: cross the vent field to the hatchery gate. The level ends at the gate's
  glow after the last Hunter column; no arena, no boss.
- **Secondary** `destroy-targets` (counted): destroy at least **3 of the 4** relay beacons on the Beacon
  Ridge before the ridge scrolls off (the HUD counter `BEACONS`, shown from the first mast; met as soon
  as it can no longer fail: the third beacon down), judged at the level's end. Pays **100** credits in
  Act 1 terms (410 in Act 4, the act factor 1.6³ = 4.096); the typical haul counts 50 %. It has **no
  radio line**: Varga's t=94.5 line is the instruction. The beacons are scenery in Levels 24–26 (red-gold
  pulses on the walls and the ridge's lip) and shootable here: the same human-built relays that guide the
  Vrell ships; destroying them has no effect on the waves (flavour; the build may dim the Vrell's wave
  lights, not drafted).

## Layout

Scroll **110–130 px/s** (the water's drag makes the ship sluggish and bullets ×0.7 stay longer on the
screen: slower than Act 3's 150; the data eases each change over 2 s). Setting: [Europa](../../../world/europa/README.md)'s
vent field, medium `underwater` for the whole level. **Light** (Q2 a): lit by the vents themselves:
the amber glow of the mats and plumes, with the dim blue ambient of Levels 25–26 in the open stretches
and the hatchery's teal at the end (Level 28 is the hatchery). Motion budget: the plumes' eruptions and
the Hunters' torpedoes are the strong elements; the drifting silt is the second; everything else stays
quiet. Pixel columns are speed × time (the eases move them by under 100 px).

**Escort.** Rook is missing (shot down at the end of Level 26): the escort slot flies the **Bulwark**
(the loaner, or a bought one): a drone with an `anti-sub` cannon that draws 30 % of aimed fire. It has
no radio. Its AI keeps out of a plume's tell and eruption disc like a telegraphed lane, stays out of a
Hunter torpedo's line, and shoots the nearest Hunter. With `--escort none` the level is the same
without it; Rook's lines are **dropped**, not just silenced (the debrief and briefing name him). A
`--level 27` debug start treats him as MIA unless `--escort rook:` is given
([wingmen](../../../player/wingmen/README.md#rooks-loss-and-absence-l26l29)).

| Section | t (s) | Scroll (px) | Speed (px/s) | Light | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Vent Field Edge | 0–24 | 0–2,880 | 120 | dim blue, the vent glow ahead | `ground`: the plain's last ridge, sea-floor silt, scattered tube worms and the first black smokers' stacks (scenery). `low-air`: drifting silt. `sub`: the abyss below with a warm red haze. The beacons of Levels 24–26 stand on the ridge's lip (scenery). | The launch (5 s); the Glassfin and Barbfin popcorn; the water's lesson from Level 26 refreshed in the light. |
| 2. Black Smokers | 24–56 | 2,880–6,400 | 110 | vent glow (amber) | `ground`: fields of black smokers, bacterial mats glowing amber round the vents, the Thera Deep hauler *Marlin* nose-down by a stack (t≈40, the secret), the first Spine Turret nest on a chimney (t≈34). **Heat plumes** from t=26 (see *Hazards*). | The plumes taught alone first (t=26), then with waves; Eel Swarm and Glow Anglers in the glow. |
| 3. Chimney Garden | 56–90 | 6,400–10,140 | 110 | vent glow, brightest | `ground`: a forest of chimneys 60–120 px wide, plume vents between them, the **wreck of a grey Depth Hunter** scorched by a plume (t≈66–74), Polyp Mortars on a mound (t≈80), a Siren nest on a stack (t≈81). `sub`: the Hunters' columns rise from here. | The first Depth Hunter column (t=56), hint 3 at the wreck (t=70), Sirens and Nautilus pairs between the plumes. |
| 4. Beacon Ridge | 90–124 | 10,140–14,220 | 120 | dim blue, red-gold pulses | `ground`: a long ridge with four relay beacons on masts (t≈94, 101, 108, 116: **the secondary's targets**), a Spine Turret nest on each shoulder, the supply buoy (t≈118). `sub`: the Abyss Ray's shadow (t≈106). | The beacons shot down under fire; the second Hunter column; the Ray with its Barbfin. |
| 5. The Gate | 124–150 | 14,220–17,600 | 130 | teal, the hatchery's glow | `ground`: the plain falling away into a vast bowl, the **hatchery gate** ahead: a ring of grown chitin arches lit teal; the last plumes at t≈126 and 133. `sub`: the abyss opening. | The third Hunter column and the last Eel Swarm; the gate's glow clears the vents; level end. |

## Waves

Formation names from the [formation vocabulary](../../../enemies/README.md#formation-vocabulary). The
**act HP factor** (the returning `medium`+ units, the proposed reference DPS 116 at Level 27 against the
unit's first level): Eel Swarm head 100 → 116 (segments 10 → 12, tail 25 → 29), Abyss Ray 210 → 234,
Siren 90 → 97, Spiral Nautilus 100 → 107; the [Depth Hunter](../../../enemies/naval/depth-hunter/README.md)
is at its first level (120, no factor); the `small` units keep the beam era's ×2 they got by level number
(Barbfin 24, Glow Angler 28) and the `tiny` Glassfin its 1 HP. Every number is a hand estimate for part A's
`levelReport`.

- **Density and pacing** (§1.7): **219 enemies at medium** over the 135 s from t=6 to t=141 (**≈ 97 a
  minute**; the Act 3 band was 69–119), written to the Act 2+ floor from the start: a wave about every
  **2.5 s**, Barbfin firing groups for the 10 enemy shots a minute (a group of 3 fires about 11 needles
  in the 9 s it holds), Glassfin schools for the popcorn, a rear ambush or a Hunter column
  every ≈ 10–15 s so the **rear** is a real direction. Bullets ×0.7 stay longer on the screen: the
  build sets the fire rates with `levelReport` (the floor of 2.5 on the screen, ≤ 3 stretches over 2 s).
  Paper counts here; the measure is part A's.
- **The Hunter columns** (t=56, 96 and 126, 3 + 4 + 3): each rises from the bottom edge under a **BOTTOM
  warning 3 s ahead**, holds in the lower band 10 s and leaves downward, so there is never more than one
  column on the screen and the first column is on the screen alone in its stretch. The three columns are
  the level's answer to the front-guns-only loadout: the Depth Charges (`rear`, `anti-sub`) are made for
  them. A column and a Siren are never in the same stretch (Q5: a slowed ship under torpedoes is the act's
  one unfair moment): the Sirens come at t=81, between the first column (leaves t≈69) and the second.
- **Directions** ("front, rear", the roster): pincers and school entries from the sides with edge warnings
  3 s ahead (LEFT/RIGHT), four rear Barbfin ambushes and three Hunter columns with BOTTOM warnings for their
  first entries; front 68 %.
- **The Ray** (t=106) rises from below with a 1.5 s shadow and four Barbfin in `circle`, once, over the
  beacon ridge; the Spiral Nautilus pair (t=76) and the Sirens (t=81) are the Level 25 skills in the plumes'
  gaps.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | Popcorn over the vent field's edge; the first school of the act's second half |
| 8.5 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front |  |
| 11 | 1 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | A firing pair; the first plume row is still two seconds away |
| 13.5 | 1 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 16 | 1 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (alternating edges) |  |
| 18.5 | 1 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front |  |
| 21 | 1 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 23.5 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the sides; edge warnings |
| 26 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | Into the first plume row (t=26: no wave reaches the vents before them) |
| 28.5 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 31 | 2 | rear ambush | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | rear | Rear ambush, BOTTOM warning at t=28; the Depth Charges' target |
| 33.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front |  |
| 36 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 38.5 | 2 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (alternating edges) |  |
| 41 | 2 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front (left) | 12 segments; its stripe lit by the vent glow; act HP factor |
| 43.5 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front |  |
| 46 | 2 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 3 | front | Hanging in the vent glow, lures lit by the plumes; lighter than Level 26 (the glow shows them) |
| 48.5 | 2 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 51 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the sides; edge warnings |
| 53.5 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 56 | 3 | column | [Depth Hunter](../../../enemies/naval/depth-hunter/README.md) | 3 | rear (BOTTOM warning at t=53) | **First sight of the grey hulls**: hold in the lower band and fire up; Varga names nothing |
| 58.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front |  |
| 61 | 3 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 63.5 | 3 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (alternating edges) |  |
| 66 | 3 | rear ambush | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | rear | Rear ambush, BOTTOM warning at t=63 |
| 68.5 | 3 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front |  |
| 71 | 3 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 73.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the sides; edge warnings |
| 76 | 3 | pincer | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | sides | 1 per side; edge warnings; spirals through the Chimney Garden's plume gaps; act HP factor |
| 78.5 | 3 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 81 | 3 | line abreast | [Siren](../../../enemies/naval/siren/README.md) | 2 | front | Sirens grown on a chimney stack at t=81; act HP factor; never together with a Hunter column (Q5) |
| 83.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front |  |
| 86 | 3 | rear ambush | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | rear | Rear ambush |
| 88.5 | 3 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 91 | 4 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (alternating edges) |  |
| 93.5 | 4 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front |  |
| 96 | 4 | column | [Depth Hunter](../../../enemies/naval/depth-hunter/README.md) | 4 | rear (BOTTOM warning at t=93) | Second column over the Beacon Ridge, while the first beacons burn |
| 98.5 | 4 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 101 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the sides; edge warnings |
| 103.5 | 4 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 106 | 4 | circle | [Abyss Ray](../../../enemies/naval/abyss-ray/README.md) + [Barbfin](../../../enemies/naval/barbfin/README.md) | 1 + 4 | front (rises from below) | The shadow's 1.5 s tell; four Barbfin in `circle` peel off as it rises; act HP factor |
| 108.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front |  |
| 111 | 4 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 113.5 | 4 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (alternating edges) |  |
| 116 | 4 | rear ambush | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | rear | Rear ambush, BOTTOM warning at t=113 |
| 118.5 | 4 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front |  |
| 121 | 4 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 123.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the sides; edge warnings |
| 126 | 5 | column | [Depth Hunter](../../../enemies/naval/depth-hunter/README.md) | 3 | rear (BOTTOM warning at t=123) | The last column, in the gate's glow; Decoy Flares or shooting the torpedoes |
| 128.5 | 5 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 131 | 5 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front |  |
| 133.5 | 5 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 136 | 5 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front (right) | 12 segments, the last chain before the gate |
| 138.5 | 5 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (alternating edges) |  |
| 141 | 5 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front |  |

Totals: Glassfin 132 · Barbfin 67 · Eel Swarm 2 · Abyss Ray 1 · Siren 2 · Spiral Nautilus 2 · Glow Angler 3 · Depth Hunter 10.

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 2 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on a smoker chimney (t≈34) | Bounty 12 each |
| 2 | The hauler *Marlin* nose-down by a stack (`ground` trigger, 20 HP, t≈40, x 90) | The secret: frees a crate worth 100 (410) |
| 3 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) ×2 on a mound (t≈80) | Bounty 15 each; the mortars lob over the plumes' gaps |
| 4 | **Relay beacons** ×4 on masts (t≈94 at x 80, 101 at x 400, 108 at x 240, 116 at x 90; `ground`, not hardened, draft 60 HP, a red-gold pulse every 4 s) | The secondary (3 of 4); bounty 15 each; a destroyed beacon goes dark |
| 4 | Spine Turret nest ×3 on the ridge shoulders (t≈100); hard ×5 | Bounty 12 each |

## Hazards

- **Heat plumes** (sections 2–5, groups of 2–3 vents on the sea floor at t≈26, 33, 40, 47, 52, 58, 65, 72,
  79, 85, 94, 101, 108, 115, 126 and 133, x positions chosen so two neighbouring discs always leave a gap of
  at least 100 px; the data's `plumes`, Level 19's furnace vents are the model): a vent's mouth is a 64 px
  disc. **Tell 1.4 s**: the mouth glows amber, silt lifts off it, a low rising rumble. Then the plume
  **erupts for 1.6 s**: a rising column seen from above (rings of shimmering water, silt streaming
  outward). A plume
  - hits the ship and the escort for `heavy` **10**, once per burst, whatever their layer;
  - hits every enemy above it for **40**, once per burst (a Glassfin dies, a Barbfin survives a first
    burst only at hard HP): kills by it **pay the bounty** and count for the secondary and the kill ratio
    (the player steered them there; decided in round 48 (2026-10-22));
  - **bends bullets**: any bullet, the player's or the enemy's, that crosses an erupting disc is turned
    **25° away from its centre**, once per bullet; a homing torpedo keeps its turn rate and re-targets
    afterwards. Beams and lasers are not bent. A plume stops nothing.
  A group fires as a ripple (0.5 s from one vent to the next, alternating ends), each vent every **4.5 s**
  (a vent crosses the screen in about 4.9 s, so it erupts at least once); the first group (t≈26)
  erupts alone before any wave reaches it. Easy: tell 1.8 s, a cycle of 6 s; hard: tell 1.0 s, a cycle of
  4 s. Code fit: the furnace vents' row and the `Sled`'s bullet check are the nearest models; the bullet
  bend is new ([gap note §2.5](../README.md)).
- **The water** (Q1 b, [under water rules](../../../world/europa/README.md#under-water-rules)): bullets
  ×0.7, acceleration ×0.6, torpedo-type range +50 %, non-`anti-sub` damage ×0.7, beam reach ×0.75; the
  pressure hull removes the drag.
- **Special limits**: no Airstrike or Orbital Lance; the Sonar Pulse works (full damage for 6 s); the
  Decoy Flares (unlocked this visit) retarget a Hunter torpedo for 4 s.
- No terrain collision (the chimneys are scenery at the ship's height).

## Secrets and pickups

- **The hauler *Marlin*** (the secret, t≈40, x≈90): a Thera Deep pressure hauler nose-down against a
  smoker's stack, her hold sealed under a skin of Vrell resin. A `ground` trigger of **20 HP**
  (28 on hard); destroyed, her crate floats up: **100** credits in Act 1 terms (410). The Salvage
  scanner's glint shows it at any range. Its event line, Thera Deep Control: "*That's the Marlin. Whatever is
  in her hold is yours, Aegis.*"
- **The hull-code wreck** (hint 3, t≈66–74, x≈175): a grey Depth Hunter on the sea floor between two
  plumes, its hull split, **the paint blistered off by the heat** so a hull code shows under it. It is a
  **placed prop**, not the first Hunter you kill (the build's wreck on a kill can still leave one): the
  hint cannot be missed and needs no crawl (Level 19's wreck crawl is not repeated). Varga reads it at
  t=70 (below).
- **Overdrive**: dropped by the t=106 Abyss Ray at its death.
- **Armour patch**: a Thera Deep service buoy releases one at (240, 380 y-up) at t=118, at the ridge's
  end (a Level 25-style buoy).
- **Shield cell**: dropped by the second Hunter column's last unit; otherwise the normal drop table.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), draft text for the **1 s rule** (checked with
Level 15's text model, as Level 21: a timed line holds the radio for the longer of its voice and its text
(5 s for the last page plus 3 s for each page before it, typed at 30 characters a second, a page of 3 × 22
characters), then 0.4 s of silence; no timed line starts late; event lines drop after 6 s). **Speakers:
Okafor, Varga and Thera Deep Control** (the colony's voice from Level 23, cast in the act's voice step). **No
Rook lines** in this level (dropped, not just `requires: escort`: he is missing); the Bulwark has no
radio.

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis Two is dark, and a Bulwark drone has his slot. Vent field ahead, Lancer. Eyes up and hands steady." |
| t=12 | Varga | "The vents erupt in cycles. A plume burns whatever it touches and bends every shot that crosses it." |
| t=24 | Generic colonial (Thera Deep Control) | "Thera Deep Control, Aegis. Our hauler Marlin went down by the smokers in week one. If you see her, say so." |
| Marlin destroyed (event) | Generic colonial (Thera Deep Control) | "That's the Marlin. Whatever is in her hold is yours, Aegis." |
| t=52 | Varga | "Contacts rising from below. Grey hulls, not Vrell. Stay high and shoot down their torpedoes." |
| t=70 | Varga | "That wreck on the floor, its paint burnt off by a plume. There's a hull code under it. It's Helix Dynamics, Commander." |
| t=82.5 | Okafor | "I'm sending it to UTC High Command now. Keep scanning, Doctor. I want every frequency." |
| t=94.5 | Varga | "The ridge beacons carry the same Helix handshake as that hull. Take them down, Lancer." |
| Abyss Ray's shadow (event, t=106) | Varga | "Large contact below you! It's rising!" |
| t=117 | Okafor | "High Command answers: the drones were stolen, stand by. I'm done standing by, Aegis." |
| t=129 | Varga | "Third column below you, and the gate's lights ahead. Flares, Lancer." |
| Level end (the closing line, `CLOSING`) | Okafor | "The hatchery gate. Whatever Helix is hiding, it's down there. Aegis, we go in." |

Timeline check (text model): t=1 closes ≈ 12.9, t=12 ≈ 23.7 (before the first plume at t=26 and Thera Deep
at t=24), t=24 ≈ 35.9, the secret's event line (the *Marlin* in reach t≈38–46) waits at most 6 s, t=52
≈ 63.5 (the BOTTOM warning for the first column is at t=53, the arrow, not the radio), t=70 ≈ 82.3,
t=82.5 ≈ 93.8, t=94.5 ≈ 105.8 (the first beacon at t≈94), the Ray's shadow line (t=106) starts after it,
t=117 ≈ 128.2, t=129 ≈ 139.7. The two hint-3 lines (Varga's code, Okafor's sending it up) carry the
beat; Okafor's t=117 reply is the first sign that High Command will not act, which Level 28's Varga
lines carry on ("*stand by*").

## Boss / mid-boss

None: Act 4 has no mid-boss (the campaign's pacing rule lists Acts 1, 2, 3, 6 and 7); the act's large
set-piece unit is the [Abyss Ray](../../../enemies/naval/abyss-ray/README.md), once here (t=106). The
level ends at the hatchery gate; [Level 28](../level-28-abyssal-maw/README.md) opens inside it.

## Music & ambience

[Track 11](../../../audio/music/README.md#track-list), the Act 4 B theme (muffled ambient-dnb; in the
track list without a chosen file): the A theme's sonar-and-pad intro in section 1, the **B base from
section 2** (the first plumes), the **full mix from section 3**. **Vorne's motif** (the cold four-note
arpeggio, a faint layer in Acts 3–4) comes in with the Hunters' first sight (t=56), plays **alone over the
ambience while Varga reads the hull code (t=68–82)** as Level 19's wreck beat did, and then stays as the faint
layer to the end; the base stem from section 5 (the gate), out over the last 6 s. No bubble layer (round 48
Q12 a). No boss, no sting. Ambience: a new `europa-vents` bed (a deep rumble of hot water, a distant
black-smoker roar, a slow groan of chimneys, no bubbles) throughout. New sounds this level needs: the plume's
tell (a low rising rumble) and eruption (a heavy roar, no pop), the bullet bend (a short swish of
water), the Hunter's torpedo launch and thruster, the torpedo's impact, the beacons' pulse (a low hum), the
hull-code scan's single ping, the *Marlin*'s resin crack, the Glassfin shatter; listed in
[sfx](../../../audio/sfx/README.md) by the act's audio step (Freesound for real-world sounds only,
CC0/CC-BY, no bubbly or popping sounds).

## Credit budget

Budget(27) = 700 × 1.07²⁶ ≈ **4,065** ([economy](../../../systems/economy/README.md#per-level-budget)).
Data in **Act 1 terms** (Glassfin 2, Barbfin 12, Depth Hunter 30, Glow Angler 14, Eel Swarm 48 whole,
Spiral Nautilus 30, Siren 28, Abyss Ray 60, Spine Turret 12, Polyp Mortar 15, relay beacon 15, the crate 100,
the secondary 100), × the act factor **4.096** (1.6³), bounties also × the level's scale. At ≈ 97 a minute a
**`bounty_scale` of about 0.78** puts the typical haul on the budget (-0.4 %), the perfect run at
1.65 × (estimated by hand with the Act 3 shares: 60 % of the air kills, 80 % of the ground,
half the secret and the secondary); `TypicalHaulTest` confirms it in the build. The scale is above M12's
0.3–0.5 because the popcorn is cheap Glassfin (2 each): 60 % of the enemies pay 16 % of the air bounty.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Glassfin 132 × 2 + Barbfin 67 × 12 + Eel Swarm 2 × 48 + Abyss Ray 1 × 60 + Siren 2 × 28 + Spiral Nautilus 2 × 30 + Glow Angler 3 × 14 + Depth Hunter 10 × 30 | 5,374 | 3,224 |
| Ground targets: Spine Turret 6 × 12 + Polyp Mortar 2 × 15 + relay beacon 4 × 15 | 518 | 414 |
| Secret: the hauler *Marlin* (hidden crate) | 410 | 205 |
| Secondary: silence the beacons (3 of 4) | 410 | 205 |
| **Total** (bounty scale 0.78) | **6,711** | **4,048** |
| Budget(n) = the typical haul's target; typical -0.4 %, perfect 1.65 × budget | | 4,065 |

**Balance plan**: the plan's L27 visit (Harpoon L4, Depth Charges L3, both pods' Micro-missiles L3, Mk IV
"Tokamak"; the [Act 4 hand plan](../../../player/generator/README.md#act-4-hand-plan-draft)): effective
single-target DPS 153, the proposed reference 116. The level expects `anti-sub` and `rear` (the Depth
Charges) and is flyable without the rear at the ×0.7 damage; Decoy Flares are an option, not a need.

## Difficulty notes

- **Easy**: the plumes' tell 1.8 s and a 6 s cycle; the Hunters' torpedo every 4.5 s at 55°/s; Barbfin shots
  every 3 s; the Hunter columns of 3 only (the formation lever); no pincer in sections 2–3; the secondary
  stays 3 of 4.
- **Hard**: the plumes' tell 1.0 s, a 4 s cycle; the Hunters' torpedo every 2.8 s at 85°/s; Barbfin 2-needle
  bursts every 2.1 s; the schools larger and 40 % of a school dives; the third column 4 hunters; the *Marlin*'s
  trigger 28 HP; the beacons' HP 90.

## Concept art

None yet. The level needs (Act 4's production-art round; the mood from the Europa mood stills):

- the vent field's backdrop (five sections: the edge, the black smokers, the chimney garden, the beacon
  ridge, the gate), the smoker chimneys and the amber mats, the plumes' mouth, tell and eruption frames,
  the hatchery gate's arches;
- the props: the hauler *Marlin*, the Hunter wreck with its blistered hull code, the relay beacon masts
  (lit and dark), the service buoy;
- the two briefing images; the intel portraits (Depth Hunter as "unmarked grey");
- the round's captures: the level at medium with the plan's fit and the Bulwark, the first plume group,
  the first Hunter column, the hull-code beat, a plume bending a torpedo, the beacons, at 1:1 crops.

## Implementation

- [ ] Level data `data.yaml` (M8 part A/D): sections and speeds with eases, waves, ground targets, `plumes`,
      radio, budget, threat profile, briefing, backdrop, music
- [ ] Under-water medium for the whole level (the factors of [europa](../../../world/europa/README.md#under-water-rules))
- [ ] Heat plumes: the tell, eruption, `heavy` 10 on the ship and escort, 40 on enemies (kills pay), the 25° bullet bend
      (once per bullet; beams unbent), the rippling groups with a gap of at least 100 px, easy/hard timings
- [ ] Depth Hunter columns from the bottom edge with the BOTTOM warning, never two columns on the screen;
      the Siren and Hunter stretches apart
- [ ] The hull-code wreck as a placed prop between two plumes; Varga's and Okafor's lines timed to it
- [ ] Relay beacons as destroyable `ground` targets, the `BEACONS` HUD counter, the secondary at 3 of 4
- [ ] The Bulwark as the escort (loaner at the visit before L27), its AI out of plume discs and torpedo lines;
      Rook's lines dropped; `--escort none` flies the level on every difficulty
- [ ] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`) with the beam era's HP, the act HP factor
      (reference DPS 116 at L27), edge warnings for side and rear first entries
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`), the event lines dropped after 6 s
- [ ] Threat profile, Varga's four intel lines and the teaser fitting the intel panel (`IntelPanelLayoutTest`)
- [ ] Briefing: both pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(27) with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music: track 11's stems by section, the motif layer and its solo over the wreck, the `europa-vents` bed (no bubbles)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with the Bulwark on every difficulty
- [ ] Production art and the round's captures

## Open questions

All decided in round 48 (2026-10-22):

- **The hull-code wreck is a placed prop** (the hint cannot be missed) rather than the first Hunter killed
  (the Hunter's own spec leaves a wreck there; both are possible, the placed one is what the level counts on).
- **The beacons are shootable** here (the secondary, 3 of 4) though they are scenery in Levels 24–26; if
  Levels 24–26 want a shootable beacon too, the target type is shared.
- **Plume kills pay** and count for the secondary and the kill ratio; the alternative is a plume that
  only hurts the ship and bends shots.
- **The plume's bullet bend** (25°, once per bullet) is a new rule beyond the roster's "deflect bullets";
  a stronger option stops enemy bullets at the plume (rejected: it would make plumes a shield).
- **Thera Deep Control** speaks twice (the hauler); the speaker is the act's colony voice, cast in the voice step.

## Decisions

- 2026-10-21 (Act 4 design phase, step D-L27-28; decided in round 48 (2026-10-22)): drafted from the
  roster row, the gap analysis (§1.2 hint 3 as radio plus a placed wreck and a briefing image, no wreck crawl;
  §2.5 plumes; §2.10 Rook's absence; Q5 a, Q7 a, Q11 a grey Hunters until Level 28) and the wave 1 documents.
- 2026-10-21: the level is written to the Act 2+ floor from the start (≈ 97 a minute, a wave every
  2.5 s); the Hunter columns three, four and three, one at a time; the Sirens kept away from them (Q5).
- 2026-10-21: the plumes are discs, not rows (a plume is a vent on a sea floor seen from above); they bend
  bullets by 25° instead of stopping them; their kills pay.
- 2026-10-21: the secret is the Thera Deep hauler *Marlin* (Level 25 has Conamara's survey sub, Level 28 a
  UTC recon sub); the secondary is the four relay beacons (the story's human-built relays).
- 2026-10-21: no Rook lines (dropped), the Bulwark loaner in his slot, Okafor's hangar teaser instead of
  Rook's.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
