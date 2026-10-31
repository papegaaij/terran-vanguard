---
title: Level 27 – Hydrothermal Vents
design: approved
implementation: done
art: final
depends-on: [../../../enemies/naval/depth-hunter, ../../../enemies/naval/glassfin, ../../../enemies/naval/barbfin, ../../../enemies/naval/eel-swarm, ../../../enemies/naval/abyss-ray, ../../../enemies/naval/siren, ../../../enemies/naval/spiral-nautilus, ../../../enemies/naval/glow-angler, ../../../enemies/ground/spine-turret, ../../../enemies/ground/polyp-mortar, ../../../enemies/ground/relay-beacon, ../../../world/europa, ../../../player/wingmen, ../../../player/specials, ../../../story/twist]
updated: 2026-10-31
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
Written from the act roster, the gap analysis's recommended answers (Q1 b, Q5 a, Q7 a, Q11 a) and the wave 1
documents. **In data since M8 part G (step G2, 2026-10-24)**: the level's numbers live in [data.yaml](data.yaml) and
the briefing, teaser, threat profile, sections, waves, ground targets, radio and credit budget tables below are
rendered from it (`tools/sync_tables.py`); step G2's placements and the gap check's defaults (`m8g-gaps.md` §5) are
accepted in round 57 (2026-10-25).

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image:

<!-- data: briefing -->
> **Commander Okafor:** "Aegis Two is missing. Rook's beacon went dark in the black water, and a
> Bulwark drone flies his slot until we find him. The vents are the last stretch before the
> hatchery, Lancer."
>
> **Dr. Varga:** "Vent plumes erupt in cycles and bend every shot that crosses them. Something
> grey rises from below and fires up: stay high and shoot its torpedoes down. Rear guns, and
> anti-sub."
<!-- /data -->

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

<!-- data: teaser -->
> **Commander Okafor:** "Vent field ahead, and a drone in Rook's slot. Bring flares and a rear
> gun, Lancer."
<!-- /data -->

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule): the Hunter columns and the
rear Barbfin pairs enter `from: rear`, the pincers and the side schools `from: sides` (the draft's 68 / 24 / 8 %).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (the play plane under water: Glassfin schools, Barbfin groups, Depth Hunters, Eel Swarms, Nautilus pairs, Sirens, an Abyss Ray), `ground` (smoker chimneys with Spine Turret nests and Polyp Mortars, the relay beacons), `sub` (the Ray's shadow and the Hunters' rise from below) |
| Attack directions | front 69% · sides 23% · rear 8% (the Hunter columns and the rear ambushes from below) |
| Density | 4 |
| Recommended traits | `anti-sub`, `rear` |
| Hazards | Heat plumes (burn, and bend shots that cross them); the water's rules ([europa](../../../world/europa/README.md#under-water-rules)) |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: Europa, the vent field, `air` + `ground`, front and rear · **L1**: + directions (front, sides, rear), density 4, hazard "heat plumes", the objective `REACH THE HATCHERY GATE` and the secondary `BEACONS 0/4` · **L2**: + Depth Hunter (new, shown as "unmarked grey, human-built"), Eel Swarm, Abyss Ray, Siren, Spiral Nautilus, Glow Angler, Barbfin and Glassfin portraits; the plumes' rings on the map · **L3**: + `anti-sub` and `rear` highlighted; the timeline strip marks the three Hunter columns (56, 96 and 126 s) and the beacon ridge (94–124 s); 1 secret |
<!-- /data -->

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

**Light** per section (the data's `underwater_look`; gap check §5 G10, ART-G's proposal, accepted in round 57 (2026-10-25)): 1 dim blue
(ambient 0.45, L25's headlights 70° × 260 px at 0.8), the vent glow ahead; 2 vent-lit warm grey (0.60, no headlights),
amber pools round the smokers; 3 the same, brightest (0.62); 4 dim blue again (as 1), the beacons' red-gold pulses; 5 the
hatchery's teal (0.60). The amber is in the light pools (one 100 px pool per smoker, the data's `lights`) and the
plumes' own pools by phase (step G4), not in the tint.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Vent Field Edge | 0–24 | 0–2,880 | 120 | clear | `ground`: the plain's last ridge, sea-floor silt, scattered tube worms and the first black smokers' stacks (scenery); the beacons of Levels 24–26 on the ridge's lip (scenery). `low-air`: drifting silt. `sub`: the abyss below with a warm red haze. | Dim blue, the vent glow ahead. The launch (5 s); the Glassfin and Barbfin popcorn; the water's lesson from Level 26 refreshed in the light. |
| 2. Black Smokers | 24–56 | 2,880–6,410 | 110 | clear | `ground`: fields of black smokers, bacterial mats glowing amber round the vents, the Thera Deep hauler *Marlin* nose-down by a stack (t≈40, the secret), the first Spine Turret nest on a chimney (t≈34). **Heat plumes** from t=26. | Vent glow (amber). The plumes taught alone first (t=26), then with waves; Eel Swarm and Glow Anglers in the glow. |
| 3. Chimney Garden | 56–90 | 6,410–10,150 | 110 | clear | `ground`: a forest of chimneys 60–120 px wide, plume vents between them, the **wreck of a grey Depth Hunter** scorched by a plume (t≈66–74), Polyp Mortars on a mound (t≈80), a Siren nest on a stack (t≈81). `sub`: the Hunters' columns rise from here. | Vent glow, brightest. The first Depth Hunter column (t=56), hint 3 at the wreck (t=70), Sirens and Nautilus pairs between the plumes. |
| 4. Beacon Ridge | 90–124 | 10,150–14,220 | 120 | clear | `ground`: a long ridge with four relay beacons on masts (t≈94, 101, 108, 116: **the secondary's targets**), a Spine Turret nest on each shoulder, the supply buoy (t≈118). `sub`: the Abyss Ray's shadow (t≈106). | Dim blue, red-gold pulses. The beacons shot down under fire; the second Hunter column; the Ray with its Barbfin. |
| 5. The Gate | 124–150 | 14,220–17,590 | 130 | clear | `ground`: the plain falling away into a vast bowl, the **hatchery gate** ahead: a ring of grown chitin arches lit teal; the last plumes at t≈126 and 133. `sub`: the abyss opening. | Teal, the hatchery's glow. The third Hunter column and the last Eel Swarm; the gate's glow clears the vents; level end. |
<!-- /data -->

## Waves

Formation names from the [formation vocabulary](../../../enemies/README.md#formation-vocabulary). The
**act HP factor** (the returning `medium`+ units, the proposed reference DPS 116 at Level 27 against the
unit's first level): Eel Swarm head 100 → 116 (segments 10 → 12, tail 25 → 29), Abyss Ray 210 → 234,
Siren 90 → 97, Spiral Nautilus 100 → 107; the [Depth Hunter](../../../enemies/naval/depth-hunter/README.md)
is at its first level (120, no factor); the `small` units keep the beam era's ×2 they got by level number
(Barbfin 52: M8 AW's 26 × 2; Glow Angler 28) and the `tiny` Glassfin its 1 HP. Measured with `levelReport -Plevel=27`
in step G2: 219 enemies of the waves and 12 ground targets at medium, **95.6 a minute**, 11.3 on the screen and 159
shots a minute with the plan's fit (the floor 2.5 and 10), no stretch over 2 s on any difficulty.

- **Density and pacing** (§1.7): **219 enemies at medium** over the 135 s from t=6 to t=141 (**≈ 97 a
  minute**; the Act 3 band was 69–119), written to the Act 2+ floor from the start: a wave about every
  **2.5 s**, Barbfin firing groups for the 10 enemy shots a minute (a group of 3 fires about 11 needles
  in the 9 s it holds), Glassfin schools for the popcorn, a rear ambush or a Hunter column
  every ≈ 10–15 s so the **rear** is a real direction. Bullets ×0.7 stay longer on the screen: the
  build sets the fire rates with `levelReport` (the floor of 2.5 on the screen, ≤ 3 stretches over 2 s).
  The schools' S-routes, the columns' sides (x 144 / 336) and the Hunter columns' xs (144, 336, 240) are step G2's.
- **The Hunter columns** (t=56, 96 and 126, 3 + 4 + 3): each rises from the bottom edge under a **BOTTOM
  warning 3 s ahead**, holds in the lower band 10 s and leaves downward, so there is never more than one
  column on the screen and the first column is on the screen alone in its stretch. The three columns are
  the level's answer to the front-guns-only loadout: a `rear` gun and `anti-sub` fire answer them (the
  balance plan refits the Tail Gun on the rear at this visit, the user's Q.1 = a, 2026-10-24; the Depth Charges are
  thrown forward since M8 AW). A column and a Siren are never in the same stretch (Q5: a slowed ship under torpedoes is the act's
  one unfair moment): the Sirens come at t=81, between the first column (leaves t≈69) and the second.
- **Directions** ("front, rear", the roster): pincers and school entries from the sides with edge warnings
  3 s ahead (LEFT/RIGHT), four rear Barbfin ambushes and three Hunter columns with BOTTOM warnings for their
  first entries; front 68 %.
- **Step G2's data** (accepted in round 57 (2026-10-25)): the Nautilus pair is the unit's `circle` (its only formation: the pair
  orbits the ship for 15 s), not the draft's `pincer`; the rear Barbfin pairs are `rear ambush` waves (up the ambush
  lanes from the bottom edge, holding above the ship); easy skips the Barbfin pincers of sections 2–3 (`skip`); the Hunter
  columns 3 / 3 / 3 on easy and 3 / 4 / 4 on hard (`easy` / `hard` counts against the formation lever).
- **The Ray** (t=106) rises from below with a 1.5 s shadow and four Barbfin in `circle`, once, over the
  beacon ridge; the Spiral Nautilus pair (t=76) and the Sirens (t=81) are the Level 25 skills in the plumes'
  gaps.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | Popcorn over the vent field's edge; the first school of the act's second half |
| 8.5 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | |
| 11 | 1 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | A firing pair |
| 13.5 | 1 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 16 | 1 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (left) | |
| 18.5 | 1 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | |
| 21 | 1 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 23.5 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the left side; edge warning |
| 26 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | Into the first plume row (t=26: no wave reaches the vents before them) |
| 28.5 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 31 | 2 | rear ambush | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | rear | Rear ambush, BOTTOM warning at t=28: up from below, the rear gun's target |
| 33.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | |
| 36 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 38.5 | 2 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (right) | |
| 41 | 2 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | Its stripe lit by the vent glow; act HP factor |
| 43.5 | 2 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | |
| 46 | 2 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 3 | front | Hanging in the vent glow, lures lit by the plumes; lighter than Level 26 (the glow shows them) |
| 48.5 | 2 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings; not on easy |
| 51 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the right side; edge warning |
| 53.5 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 56 | 3 | column | [Depth Hunter](../../../enemies/naval/depth-hunter/README.md) | 3 | rear (left) | **First sight of the grey hulls** (BOTTOM warning at t=53): hold in the lower band and fire up; Varga names nothing |
| 58.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | |
| 61 | 3 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 63.5 | 3 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (left) | |
| 66 | 3 | rear ambush | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | rear | Rear ambush, BOTTOM warning at t=63 |
| 68.5 | 3 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | |
| 71 | 3 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings; not on easy |
| 73.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the left side; edge warning |
| 76 | 3 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | Spirals through the Chimney Garden's plume gaps (the README's pincer: the pair orbits the ship); act HP factor |
| 78.5 | 3 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 81 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | front | Sirens grown on a chimney stack; act HP factor; never together with a Hunter column (Q5) |
| 83.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | |
| 86 | 3 | rear ambush | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | rear | Rear ambush |
| 88.5 | 3 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 91 | 4 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (right) | |
| 93.5 | 4 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | |
| 96 | 4 | column | [Depth Hunter](../../../enemies/naval/depth-hunter/README.md) | 4 | rear (right) | Second column over the Beacon Ridge (BOTTOM warning at t=93), while the first beacons burn; its last drops a shield cell |
| 98.5 | 4 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 101 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the right side; edge warning |
| 103.5 | 4 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 106 | 4 | carrier + escorts | [Abyss Ray](../../../enemies/naval/abyss-ray/README.md) + [Barbfin](../../../enemies/naval/barbfin/README.md) | 1 + 4 | front | The shadow's 1.5 s tell; four Barbfin in `circle` peel off as it rises; act HP factor; the Ray drops an overdrive |
| 108.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | |
| 111 | 4 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 113.5 | 4 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (left) | |
| 116 | 4 | rear ambush | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | rear | Rear ambush, BOTTOM warning at t=113 |
| 118.5 | 4 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | |
| 121 | 4 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 123.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the left side; edge warning |
| 126 | 5 | column | [Depth Hunter](../../../enemies/naval/depth-hunter/README.md) | 3 | rear | The last column (BOTTOM warning at t=123), in the gate's glow; Decoy Flares or shooting the torpedoes |
| 128.5 | 5 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 131 | 5 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | |
| 133.5 | 5 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 136 | 5 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The last chain before the gate |
| 138.5 | 5 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (right) | |
| 141 | 5 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | The last wave |

Totals: Glassfin 132 · Barbfin 67 · Eel Swarm 2 · Glow Angler 3 · Depth Hunter 10 · Spiral Nautilus 2 · Siren 2 · Abyss Ray 1.
<!-- /data -->

Totals: Glassfin 132 · Barbfin 67 · Eel Swarm 2 · Abyss Ray 1 · Siren 2 · Spiral Nautilus 2 · Glow Angler 3 · Depth Hunter 10.

## Ground targets

The four relay beacons are a part of their own ([relay-beacon](../../../enemies/ground/relay-beacon/README.md), step
G2: the secondary's `escapes` counts a unit by its slug). ART-G's places (accepted in round 57 (2026-10-25)): the first nest on the
nest stack (x 340/384/428), the mortars on their mound (x 70/128), the beacons each on a ridge crest, the shoulder nests
at x 130/172 and 310 (hard + 88 and 352).

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 2 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on a smoker chimney (t≈34) | Bounty 12 each |
| 2 | The hauler *Marlin* nose-down by a stack (`ground` trigger, 20 HP, hard 28, t≈40, x 90) | The secret: frees a crate worth 100 (410) |
| 3 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) ×2 on a mound (t≈80) | Bounty 15 each; the mortars lob over the plumes' gaps |
| 4 | **[Relay beacons](../../../enemies/ground/relay-beacon/README.md)** ×4 on masts (t≈94 at x 80, 101 at x 400, 108 at x 240, 116 at x 90; `ground`, not hardened, 60 HP, a red-gold pulse every 4 s) | The secondary (3 of 4); bounty 15 each; a destroyed beacon goes dark |
| 4 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on the ridge shoulders (t≈100); hard ×5; hard ×5 | Bounty 12 each |
<!-- /data -->

## Hazards

- **Heat plumes** (sections 2–5, groups of 2–3 vents on the sea floor at t≈26, 33, 40, 47, 52, 58, 65, 72,
  79, 85, 94, 101, 108, 115, 126 and 133, x positions chosen so two neighbouring discs always leave a gap of
  at least 100 px; the data's `vents` with `look: heat-plume` and `shape: disc`, Level 19's furnace vents are the
  model; ART-G's xs keep neighbours at least 164 px apart centre to centre): a vent's mouth is a 64 px
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
  4 s. Flown since step G1 (`HeatPlumes`; gap check §5 G5, G6: bent are the player's shots, enemy bullets and
  torpedoes; not beams, lances, depth charges, mines or lobs).
- **The water** (Q1 b, [under water rules](../../../world/europa/README.md#under-water-rules)): bullets
  ×0.7, acceleration ×0.6, torpedo-type range +50 %, non-`anti-sub` damage ×0.7; energy weapons (beam and lasers)
  damage ×0.2 and reach ×0.4 of the field's height (M8 AW); the pressure hull removes the drag.
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

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis Two is dark, and a Bulwark drone has his slot. Vent field ahead, Lancer. Eyes up and hands steady." |
| t=12 | Varga | "The vents erupt in cycles. A plume burns whatever it touches and bends every shot that crosses it." |
| t=24 | Thera Deep (Thera Deep Control) | "Thera Deep Control, Aegis. Our hauler Marlin went down by the smokers in week one. If you see her, say so." |
| t=52 | Varga | "Contacts rising from below. Grey hulls, not Vrell. Stay high and shoot down their torpedoes." |
| t=70 | Varga | "That wreck on the floor, its paint burnt off by a plume. There's a hull code under it. It's Helix Dynamics, Commander." |
| t=82.5 | Okafor | "I'm sending it to UTC High Command now. Keep scanning, Doctor. I want every frequency." |
| t=94.5 | Varga | "The ridge beacons carry the same Helix handshake as that hull. Take them down, Lancer." |
| The Abyss Ray's shadow (`first-ray`, t=106) | Varga | "Large contact below you! It's rising!" |
| t=117 | Okafor | "High Command answers: the drones were stolen, stand by. I'm done standing by, Aegis." |
| t=129 | Varga | "Third column below you, and the gate's lights ahead. Flares, Lancer." |
| Level end (the closing line) | Okafor | "The hatchery gate. Whatever Helix is hiding, it's down there. Aegis, we go in." |
<!-- /data -->

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
Q12 a). No boss, no sting. Ambience: Europa's bed under the ice, `europa-under`, throughout (one bed per
setting, the user 2026-10-26; the level's own `europa-vents` bed left the game). New sounds this level needs: the plume's
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
**`bounty_scale` of 0.79** (the draft's hand estimate 0.78) puts the typical haul on the budget: `levelReport
-Plevel=27` (step G2) measured -1.5 % at 0.78 and named 0.79, the perfect run at 1.65 ×; `TypicalHaulTest` holds it
(step G3). The table is the sync script's estimate. The scale is above M12's
0.3–0.5 because the popcorn is cheap Glassfin (2 each): 60 % of the enemies pay 16 % of the air bounty.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Glassfin 132 × 2 + Barbfin 67 × 12 + Eel Swarm 2 × 60 by parts (48 whole) + Glow Angler 3 × 14 + Depth Hunter 10 × 30 + Spiral Nautilus 2 × 30 + Siren 2 × 28 + Abyss Ray 1 × 60 | 5,456 | 3,234 |
| Ground targets: Spine Turret 6 × 12 + Polyp Mortar 2 × 15 + Relay Beacon 4 × 15 | 528 | 422 |
| Secret: Marlin (hidden crate, 2% of budget) | 410 | 205 |
| Secondary: silence 3 of the 4 beacons | 410 | 205 |
| **Total** (bounty scale 0.79) | **6,804** | **4,066** |
| Budget(n) = the typical haul's target; typical +0 %, perfect 1.67 × budget | | 4,065 |
<!-- /data -->

**Balance plan**: the plan's L27 visit (Harpoon L4, Depth Charges L3, both pods' Micro-missiles L3, Mk IV
"Tokamak"; the [Act 4 hand plan](../../../player/generator/README.md#act-4-hand-plan-draft)): effective
single-target DPS 153, the proposed reference 116. Since M8 AW the Depth Charges are thrown forward, so the plan has no
fire below the ship: the user's **Q.1 = a** (2026-10-24): the plan **refits the Tail Gun** (owned since L23) on the
rear at this visit, beside the Mk IV (step G3), the charges back when part H decides Level 28's fit. The level expects
`anti-sub` and `rear` (the Tail Gun against the Hunters and their torpedoes); Decoy Flares are an option, not a need.

## Difficulty notes

- **Easy**: the plumes' tell 1.8 s and a 6 s cycle; the Hunters' torpedo every 4.5 s at 55°/s; Barbfin shots
  every 3 s; the Hunter columns of 3 only; no Barbfin pincer in sections 2–3; the secondary stays 3 of 4.
- **Hard**: the plumes' tell 1.0 s, a 4 s cycle; the Hunters' torpedo every 2.8 s at 85°/s; Barbfin 2-needle
  bursts every 2.6 s (M8 AW's stat block); the schools larger (the formation lever); the second and third columns 4
  hunters; the *Marlin*'s trigger 28 HP; the beacons' HP 78 (the global multiplier: the draft's 90 has no stat-block
  key, accepted in round 57 (2026-10-25)); five Spine Turrets on the shoulders.

## Concept art

ART-G's production art for round 57, accepted as final (prompts and briefs: [concept/prompts.md](concept/prompts.md)); the light on the
sheets is the review's stand-in, not the game's grade (step G5's captures replace it).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r57-a.png](concept/backdrop-final-r57-a.png) | Production backdrop (`tools/art/backdrop_l27.py`, from europa-vents-r48-a): the plain's last ridge with Levels 24-26's beacons, the black smokers (18 chimney clusters with hot mouths, amber mats, tube worms), the nest and Siren stacks, the *Marlin*'s smoker, the hull-code wreck (`HDX-417` under blistered paint), the beacon ridge's knolls and shoulders, the bowl's rim and the hatchery gate's ring of chitin arches; twelve screens under the vent light's stand-in (sheet) | chosen (round 57: accepted as final) |
| [concept/backdrop-final-r57-a.gif](concept/backdrop-final-r57-a.gif) | t 25-35 at half size: the first plume rows' tells and eruptions among the smokers | chosen (round 57: accepted as final) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block, the plume rows, the ground places, the per-section `underwater_look` and the static `lights:` the art is drawn for (adopted in the data by step G2) | chosen (round 57: accepted as final) |
| [concept/l27-props-final-r57-a.png](concept/l27-props-final-r57-a.png) | The *Marlin* (`tools/art/l27_props.py`: sealed under Vrell resin, hit, opened, her lamp and the resin's teal) and the heat plume (mouth and tell steps, the tell's light and lifting silt, the eruption from above with its heat, settling, the bend's ripple) (sheet) | chosen (round 57: accepted as final) |
| [concept/l27-props-final-r57-a.gif](concept/l27-props-final-r57-a.gif) | The first plume row's tell and eruption, shots bent 25 degrees off a burning disc; the *Marlin* by her stack shot open, her crate rising | chosen (round 57: accepted as final) |
| [concept/plumes-capture-r57-a.png](concept/plumes-capture-r57-a.png) | Game capture (step G5; Xvfb, `--bench`, medium, the plan's fit, the Bulwark, `--invulnerable`, auto-fire, the ship unsteered): the first plume row (x 120 and 360) at t≈26.8 (the mouths glowing, the silt lifting), 27.5 (the tell's end) and 28.2 (the eruption), the play field at 1:1 | chosen (round 57: accepted as final) |
| [concept/plumes-capture-r57-a.mp4](concept/plumes-capture-r57-a.mp4) | The same run t≈22–34 with the game's sound: the tells, the eruptions, the bend's swish and the `europa-vents` bed (the a stand-ins) | chosen (round 57: accepted as final) |
| [concept/hunter-capture-r57-a.png](concept/hunter-capture-r57-a.png) | The first Depth Hunter column (x 144) rising at t≈56.6–61.2 and holding beside the ship, its torpedoes leaving the tubes, the Decoy Flares fired at t≈59 (a run with `--special decoy-flares:6`), 1:1 | chosen (round 57: accepted as final) |
| [concept/hunter-capture-r57-a.mp4](concept/hunter-capture-r57-a.mp4) | The same run t≈53–68 with sound: the BOTTOM warning, the rise, the launches, the torpedoes' run, the flares, torpedoes shot down | chosen (round 57: accepted as final) |
| [concept/bend-capture-r57-a.png](concept/bend-capture-r57-a.png) | A burning disc bending shots: the harpoons' trails kinking 25° in the plume at t≈103.0–103.5 (1:1) and the level's last plume row at t≈128 over the ship; no Hunter torpedo was caught crossing a disc in the unsteered runs | chosen (round 57: accepted as final) |
| [concept/wreck-capture-r57-a.png](concept/wreck-capture-r57-a.png) | The hull-code beat, the whole screen at t≈70 (the wreck mid-screen under the side warning) and t≈72.5 (Varga's line on the radio panel) | chosen (round 57: accepted as final) |
| [concept/wreck-capture-r57-a.mp4](concept/wreck-capture-r57-a.mp4) | t≈64–84 with sound: the scan ping, Varga's and Okafor's lines, Vorne's motif alone over the bed from t=68 to 82 | chosen (round 57: accepted as final) |
| [concept/marlin-capture-r57-a.png](concept/marlin-capture-r57-a.png) | The *Marlin* by her smoker at t≈41.0–43.4, shot open (a run steered under her at t≈35), her crate rising, 1:1 | chosen (round 57: accepted as final) |
| [concept/marlin-capture-r57-a.mp4](concept/marlin-capture-r57-a.mp4) | The same run t≈38–46 with sound: the resin's crack | chosen (round 57: accepted as final) |
| [concept/beacons-capture-r57-a.png](concept/beacons-capture-r57-a.png) | The relay beacons: a mast's red lamp and its gold pulse (t≈102.3, 104.0, main run), a beacon shot down (t≈94.7–95.6: the lamp, the break, the dark mast; a run steered under each beacon) and the screen at t≈110 with `BEACONS DONE` | chosen (round 57: accepted as final) |
| [concept/beacons-capture-r57-a.mp4](concept/beacons-capture-r57-a.mp4) | The steered run t≈92–112 with sound: the beacons' hum and breaks, the second Hunter column | chosen (round 57: accepted as final) |
| [concept/gate-capture-r57-a.png](concept/gate-capture-r57-a.png) | The hatchery gate's arches passing over the ship at t≈147, 149 and 150.5 | chosen (round 57: accepted as final) |
| [concept/gate-capture-r57-a.mp4](concept/gate-capture-r57-a.mp4) | t≈138–152 with sound: the last stretch, the gate, Okafor's last lines, the theme going out | chosen (round 57: accepted as final) |
| [concept/no-escort-capture-r57-a.png](concept/no-escort-capture-r57-a.png) | `--escort none`: the whole screen at t≈60.5, the first Hunter column with no escort (the ESCORT row empty) | chosen (round 57: accepted as final) |
| [concept/crops-capture-r57-a.png](concept/crops-capture-r57-a.png) | 1:1 crops from the game: the Hunter column (t≈58.4, the Bulwark beside it), the wreck's `HDX-417`, a plume's tell (t≈27) and an eruption (t≈103.2) | chosen (round 57: accepted as final) |

The captures' frame rate: a full medium run on the software renderer (Xvfb, llvmpipe, no recording) averaged **459 fps**;
the longest frame (51 ms) is the level's first, no other frame of 50 ms or more.

## Implementation

- [x] Level data `data.yaml` (M8 part G, step G2): sections and speeds with eases, waves, ground targets, `plumes`,
      radio, budget, threat profile, briefing, backdrop, music
- [x] Under-water medium for the whole level (the factors of [europa](../../../world/europa/README.md#under-water-rules))
- [x] Heat plumes: the tell, eruption, `heavy` 10 on the ship and escort, 40 on enemies (kills pay), the 25° bullet bend
      (once per bullet; beams unbent), the rippling groups with a gap of at least 100 px, easy/hard timings (step G1's
      simulation, `HeatPlumesTest`; step G4's looks and sounds: the mouth, the tell's silt and glow, the eruption's
      column and heat, the settling silt, the bend's ripple and swish, the amber pool by phase; `Level27LooksTest`)
- [x] Depth Hunter columns from the bottom edge with the BOTTOM warning, never two columns on the screen;
      the Siren and Hunter stretches apart
- [x] The hull-code wreck as a placed prop between two plumes; Varga's and Okafor's lines timed to it (the backdrop's
      `hull-wreck` at t=70; Varga's scan ping 2 s before it passes mid-screen, `Level27SoundsTest`)
- [x] Relay beacons as destroyable `ground` targets, the `BEACONS` HUD counter, the secondary at 3 of 4 (their 4 s
      pulse and light, the lamp's break and the dark mast, the hum and break; `Level27Test`, `Level27LooksTest`)
- [x] The Bulwark as the escort (loaner at the visit before L27), its AI out of plume discs and torpedo lines;
      Rook's lines dropped; `--escort none` flies the level on every difficulty
- [x] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`) with the beam era's HP, the act HP factor
      (reference DPS 116 at L27), edge warnings for side and rear first entries
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`), the event lines dropped after 6 s
      (`RadioTimelineLevel27Test`, `VoiceFilesTest`; Thera Deep Control with his `thera-deep` portrait)
- [x] Threat profile, Varga's four intel lines and the teaser fitting the intel panel (`IntelPanelLayoutTest`; from L2
      the Depth Hunter tagged `UNMARKED GREY`, at L3 the strip's hull-grey ticks for the three columns and the
      beacons' red marks)
- [x] Briefing: both pages, each one screen (`BriefingLayoutTest`), with their images
- [x] Typical haul within ±5 % of budget(27) with the level's `bounty_scale` (`TypicalHaulTest`)
- [x] Music: track 11's stems by section, the motif layer and its solo over the wreck, the `europa-under` bed (no bubbles; `europa-vents` until step I2)
      (`Level27SoundsTest`; round 57's picks: the a variants play, the b picks (S.2, S.6, S.7, S.9, S.12) follow in step H5)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with the Bulwark on every difficulty
- [x] Production art and the round's captures (steps ART-G and G5; [round 57](../../../concept-rounds/round-57/README.md))

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
- 2026-10-24 (M8 part G, step G2; accepted in round 57 (2026-10-25)): the level in `data.yaml` from the approved
  tables and ART-G's proposal (plume xs, target places, per-section light, static pools, backdrop adopted unchanged);
  the relay beacons a part of their own (`relay-beacon`, the secondary's `escapes`); the Nautilus pair a `circle`; the
  Hunter columns at x 144 / 336 / 240, 3 / 3 / 3 on easy and 3 / 4 / 4 on hard; the beacons' hard HP 78 (no key for the
  draft's 90); `bounty_scale` 0.79 (`levelReport`: -1.5 % at 0.78); the stale lines since M8 AW fixed (the water's
  energy-weapon factors, Barbfin 52 HP, the hard Barbfin burst every 2.6 s, the rear answer the Tail Gun per Q.1 = a).
- 2026-10-24 (M8 part G, step G3; accepted in round 57 (2026-10-25)): the balance plan refits the Tail Gun L3 on the
  rear at this visit (Q.1 = a; 15.5 of the Mk IV's 20 MW); the test autopilot plans every move here (`VentPlay`: the hot
  discs, the torpedoes flown on, the bullets and bodies; the beacons first, under them with the harpoon or over them with
  the rear gun; under the Hunter column's band while one is on the screen; Decoy Flares if fitted). Measured
  (`levelReport`, 5 seeds): won every run on every difficulty with no retry, 3 of 4 beacons on every run, a small unit
  1.38× Level 10's time on the screen; without an escort every difficulty won with the beacons. No wave retuned: the
  level was not at fault (the 0/3 and 7.1× came from flying the starter fit and the old plays).
- 2026-10-25 (M8 part G, step G4; accepted in round 57 (2026-10-25)): the level drawn and played. The motif's solo
  over the wreck is the stems out with the motif layer on (`stem_times` t=68 `stems: none`; `stems: motif` there would
  have loaded the motif file a second time as an extra stem), and the theme with the motif goes out over the last 6 s
  (t=144 `db: -40`). The vent field has static lights but is lit (45–62 %), so it is not *black water*: its units keep
  their plain looks and the dark's sounds (the crown hum, the headlights' clicks) stay off (`DarkLight.blackWater`,
  ambient below 30 %). The Depth Hunters fade in from 35 % over their first 1.2 s on the field (the rise out of the
  deep), their nozzles flicker at 8 fps moving and 4 holding; the torpedoes are drawn with their own sprite, glow,
  wake and a launch puff at the tubes, and burst on the under-water ladder. "The plumes' rings on the map" (sensor L2)
  are the briefing's sonar chart (`level-27-vent-field`, every plume vent ringed amber): the hangar has no per-level
  map. Thera Deep Control's lines carry his `thera-deep` portrait (as at Level 23).
- 2026-10-25 (M8 part G, step G5; accepted in round 57 (2026-10-25)): part G built and flown; the captures above.
  The small rules the steps decided (log `m8-gaps.md` 2026-10-24/25, gap check `m8g-gaps.md` §5), not asked in the
  round unless named there: a column's Hunters hold evenly spread over their band (G1); a shot hitting a disc's centre
  line is bent clockwise (G1); at most 16 units burnt a burst (G1); a plume kill is the player's kill (bounty × scale,
  chain, kill ratio), plumes burn swimmers only (not ground units, the Ray's shadow or torpedoes, which are bent); bent
  are the player's shots, enemy bullets and torpedoes, not beams, lances, depth charges, mines or mortar lobs, once a
  bullet, 25° away from the disc's centre (§5 G5, G6); torpedoes at most 8 in flight, bursting on the ship, the Bulwark
  and Rook, never drawn by the Bulwark (§5 G4); the Bulwark keeps out of hot discs and every homing torpedo's line and
  aims at the nearest Hunter, and only plumes hit it (G1, §5 G7; round 57 C.1); the two Sirens are ground nests on a
  chimney stack reusing Level 25's wall art (§5 G8, G4; C.2); the rear Barbfin pairs rise straight from the bottom edge
  and hover in their band, BOTTOM warning 3 s ahead (G1, §5 G9); the level is lit, not black water (ambient 45–62 %,
  §5 G10); track 11, the motif alone at 68–82 (§5 G11); a `--level 27` debug start flies the Bulwark loaner (§5 G12);
  Levels 24–26's beacons stay scenery (§5 G13); ART-G's plume and target xs, 18 smoker clusters, the hull code
  `HDX-417`, the *Marlin* as a Thera Deep hauler sealed in Vrell resin (her crate the simulation's spawn, G4), the
  Hunter's own torpedo sprite, the gate as ten chitin arches, the warm-grey tint of sections 2–3 (the amber in the
  pools); the intel's `UNMARKED GREY` tag (G4). The Hunter's rise has no sound of its own (not in the gap check's
  sound list; AUD-G). Captured: the first Hunter column holds beside the unsteered ship (x 144) and the third rises
  under it (x 240, the ship's own column), so in the captures the hulls overlap the ship; no torpedo was caught being
  bent; the beacons are dark masts in the dim blue, readable by their lamps.
- 2026-10-25 (user, [round 57](../../../concept-rounds/round-57/README.md) closed): F.1–F.8 accepted as final (the backdrop, the *Marlin* and the plume, the Depth
  Hunter, the relay beacon, the briefing images, the intel portraits, the captures); the part is `art: final`. Sounds: the
  vent field's bed **a**, the plume's tell **b**, its eruption **a**, the bend's swish **a**, the torpedo launch **a**,
  the torpedo's run **b**, its impact **b**, the beacon's hum **a**, its break **b**, the *Marlin*'s crack **a**, the
  Hunter's hull break **a**, the hull-code scan **b** (the b picks go into the game in step H5; the rejected files
  are in the sound effects' `concept/rejected/`). C.1–C.7 accepted (the Bulwark's dodging, the Sirens on the chimneys, the
  beacons' 60 HP and bounty 15, the Hunter columns, the vent field's light, the Nautilus pair's circle, no sound for the
  Hunter's rise), and every small rule logged above as pending the round is accepted.
- 2026-10-30, the user: dither audit, ≥ 25 % smoothed (step DITH8): `hatchery-gate-glow`, `smoker-1..18-glow`, `marlin-stack-glow`, `relay-beacon-glow`, `marlin-hauler-glow`, the heat plume's glow, heat and silt puffs in smooth steps, not the 4×4 Bayer checker ([audit](../../../art-direction/production/README.md#dither-audit-2026-10-30)).
