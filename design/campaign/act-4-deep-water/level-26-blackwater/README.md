---
title: Level 26 – Blackwater
design: approved
implementation: in-progress
art: proposed
depends-on: [../../../enemies/naval/glow-angler, ../../../enemies/naval/depth-hunter, ../../../enemies/naval/siren, ../../../enemies/naval/spiral-nautilus, ../../../enemies/naval/eel-swarm, ../../../enemies/naval/driftjelly, ../../../story/characters/the-choir, ../../../story/characters/rook, ../../../world/europa, ../../../player/wingmen, ../../../player/specials]
updated: 2026-10-25
---

# Level 26 – Blackwater

## Summary

Kilometres of black water with nothing on the charts. Only the ship's headlight cone and the Vrell's own
bioluminescence are lit: enemies are seen by their glow (a look only: bullets always show, everything is
hittable, round 48 Q4 a). The level introduces the
[Glow Angler](../../../enemies/naval/glow-angler/README.md), whose teal lure looks almost like a pickup,
and carries the [Choir](../../../story/characters/the-choir/README.md)'s first hint of the door in the Acts 3–4
register ("…your kin… came before you…", Q8 a). The act's breather by threat, not by count (no boss,
≈ 74 enemies a minute at the Act 2+ floor, lighter firing groups), it ends with its story beat: **Rook is
shot down** by torpedoes from unseen [Depth Hunters](../../../enemies/naval/depth-hunter/README.md), on the
screen, in the black (Q7 a). 188 s: the last lamps, the dark, the fault scarp with its Sirens, the Choir
and the constellation, the deep, and the silence.

**Status: approved in round 48 (2026-10-22); in the build (M8 part F).** Drafted in the Act 4 design phase
(2026-10-21) from the act roster, the gap analysis's recommended answers (Q4 a, Q6 a, Q7 a, Q8 a) and the wave 1
documents. The level's numbers live in its [data.yaml](data.yaml) (step F2) and the briefing, threat profile, layout,
scarp, waves, ground targets, radio and credit tables below are rendered from it; the counts are measured with
`levelReport` (step F2, tests in step F3).

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (approved in round 48):

<!-- data: briefing -->
> **Commander Okafor:** "Past the trench there is nothing on the charts, only black water
> between us and the vents. The Vrell route runs through it. Fly it on your headlight, Lancer,
> and keep Rook close."
>
> **Dr. Varga:** "You'll see only what glows. The Vrell glow teal and violet, and so do their
> lures: a teal bead on a stalk is not a pickup. Bullets always show. The Sonar Pulse lights
> everything for six seconds."
<!-- /data -->

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-26-blackwater`: a chart with the trench's end and the vent field's glow far to the
  south and nothing in between but a dotted route, the CDF markers lost off the edge.
- Varga's page `level-26-lure`: her diagram of the Glow Angler: its stalk and teal bead in the dark beside a warm
  gold pickup, the ship's cone, the lunge radius (130 px).

*Hangar teaser* (shop screen before L26; Rook, not voiced; his last):

<!-- data: teaser -->
> **Rook:** "Black water ahead. Stay where I can see your lights, Lancer. I'm not that good in
> the dark."
<!-- /data -->

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule): the scarp's nests enter
`from: sides` (the simulation brings them in with the ground at the front). At sensor L3 the intel panel marks the
nests on the side arrows (step F5).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (the play plane under water, seen as glows: Glow Anglers, Driftjellies, Nautilus pairs, Eel Swarm snakes), the fault scarp's Siren nests |
| Attack directions | front 97% · sides 3% (the scarp's nests count as sides) |
| Density | 3 |
| Recommended traits | `homing`, `area` |
| Hazards | Darkness: only the headlight cone, light pools and the Vrell's glow; Glow Anglers lunge when touched; Driftjelly rings; Siren rings at the scarp |
| Boss / mid-boss | none |
| Sensor-suite detail | none: Europa, deep water, `air`, front · L1: + directions (all), density 3, hazard "darkness", the objective `CROSS THE BLACKWATER` and the secondary `KILL RATIO 65 %` · L2: + Glow Angler (new), Driftjelly, Eel Swarm, Spiral Nautilus, Siren portraits, the lure marked "not a pickup" · L3: + the scarp's nest positions on the threat arrows |
<!-- /data -->

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Black water. You'll fly by headlight and by their glow." |
| L1 | "Nothing hides from a shot, only from your eyes. Bullets always show." |
| L2 | "New: Glow Angler. Its lure is not a pickup. Sonar lights it." |
| L3 | "Homing and area guns. Pulse the sonar when it gets thick." |

## Objective

- **Primary** `reach-end`: cross the Blackwater to the vents' glow.
- **Secondary**: destroy at least **65 %** of all enemies (§1.7), judged at the level's end. Pays **100**
  credits in Act 1 terms (410 in Act 4); the typical haul counts 50 %. Homing and the Sonar Pulse close the
  gap in the dark.
- The shoot-down is not an objective and cannot fail the level (see *Rook's loss*).

## Layout

Scroll **130 px/s** in the lamp stretch, **120 px/s** in the dark, **110 px/s** in the silence (units are
seen late, so the scroll gives more time; the data eases each change over 2 s). **Ambient** (the data's
`underwater_look`): 20 % in the lamp stretch, ramping to 6 % over t=8–18 (a section's `ramp`, gap check F4), 6 % from
there to the end. Setting:
[Europa](../../../world/europa/README.md)'s deep water, medium `underwater`: the black level of Q2 a. Motion
budget: the glows are the one strong element; the drifting particles (marine snow, lit only by the cone) are the
second; the rest is black.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Last Lamp | 0–20 | 0–2,600 | 130 | clear | `ground`: the last Thera Deep lamp posts along a drowned pipeline, one every 360 px, until t=14; the trench's beacons falling behind. | The lights go out behind the ship; the Nautilus's crown learnt while lamps still burn. |
| 2. The Dark | 20–60 | 2,600–7,410 | 120 | clear | `ground`: Black water; marine snow in the cone; the only light the units' glow and the pickups' warm gold. | The Glow Angler (t=24); the lure learnt. |
| 3. The Scarp | 60–100 | 7,410–12,210 | 120 | clear | `ground`: A single **fault wall on the right** (a scarp, flyable width 400 → 280 px) with three cave mouths; the Sirens' sacs glowing beside them; the farm barge lamp (t≈84, left). The scarp sinks away at t=100. | The Sirens in the dark: their glow is their tell. |
| 4. The Choir | 100–130 | 12,210–15,810 | 120 | clear | `ground`: Open black water; the Choir's static in the HUD portrait from t=97. | The Choir's fragment (t=99); the constellation of Anglers (t=108). |
| 5. The Deep | 130–155 | 15,810–18,810 | 120 | clear | `ground`: Open black water; a faint amber seep glow far below from t=140. | The last fights; Rook's pre-line (t=150). |
| 6. The Silence | 155–188 | 18,810–22,450 | 110 | clear | `ground`: Black water; the vent field's red glow rising on the horizon from t=174; Rook's lamp beside the ship until t≈163.5. | Rook's loss (t=161–166); the aftermath; level end. |
<!-- /data -->

### Darkness (Q4 a: a look only)

The rules are [europa](../../../world/europa/README.md#darkness-level-26)'s: **a look only**, the simulation
does not know the dark. This level's values (draft):

| Element | In the light | In the dark |
|---|---|---|
| Units | drawn whole inside the headlight cone (70° ahead, 260 px) and any light pool | **glow only**: the teal and violet parts and lamps; the body a faint rim (about 15 %) |
| Enemy bullets, markers, telegraphs | drawn | **always drawn bright**, never hidden |
| Pickups | drawn | **glow warm gold** from afar (a pickup is never teal) |
| The ship and the player's shots | drawn | drawn |
| Rook | drawn | a small lamp of about 56 px (a light pool) so the player always sees him |
| The Glow Angler's lure | drawn whole, stalk included | a pulsing **teal bead**; the stalk shows only when it is within ~160 px |
| HUD, edge warnings, the Sensor suite's arrows | above the dark | above the dark |

- **Everything is hittable** wherever it is; homing shots and Rook pick targets as usual (rejected: b, shots hit
  lit units only; c, a sonar-blip display). The ambient is a settings option's lever (*Dark levels assist*: 20 %).
- **The Sonar Pulse** lights and outlines every unit and lure for 6 s and gives every weapon full damage for
  those 6 s; Varga says so at t=46.
- **Light pools**: Driftjelly veins, the nests' sacs, the Anglers' lures and eyes, the Nautilus crown's
  violet, the farm barge's lamp, the beacons; the ship's cone 260 px; Rook's lamp 56 px.
- **Code fit** (gap check F1, corrected in step F2): the dark is the under-water light map of step LX with this level's
  values, the data's `underwater_look`: the twin headlights' hard cone `{strength: 1.0, reach: 260, angle: 70}`, the
  ambient 6 % (from 20 % with section 1's `ramp`), a 15 % `rim` on the scarp's rock, the escort's 56 px lamp
  (`escort_lamp`, Rook's or the Bulwark's) and the static `lights` (the lamp posts, the trench's two last beacons, the
  farm barge's lamp, the survey buoy); the glow-only pass for units outside the light and the settings assist are the
  game's (step F4). Level 06's `hazards.darkness` (`HazardSpec.Darkness`, ground only) is not used; nothing in the
  simulation knows the dark.

### The fault scarp (section 3)

The Level 25 terrain rules (one wall instead of two): a polyline on the right, flyable width at least
**280 px** at its narrowest, three notches (caves) with Siren nests beside them (the Siren is a wall mount, so it needs
a wall: the scarp is its only place in this level). The nests' glow is their tell; the ring slows as in
[Level 25](../level-25-siren-trench/README.md). The left side is open water. No ground units on the scarp.

The scarp's collision line from the data (`terrain`, ART-F's proposal adopted unchanged in step F2), each point's row
with the time it reaches the top edge:

<!-- data: terrain -->
| t at the top edge (scroll px) | Left x | Right x | Flyable width | Notes |
|---|---|---|---|---|
| 0–56 (0–6,930) | — | — | 480 | Open black water, no walls |
| 56 (6,930) | — | 480 | 480 | The scarp rises at the field's right edge; wall height 0 |
| 59 (7,290) | — | 430 | 430 | wall height 0.7 |
| 61 (7,530) | — | 400 | 400 | The flyable width 400 px; the first cave mouth and its nests |
| 66 (8,130) | — | 400 | 400 | |
| 74 (9,090) | — | 330 | 330 | The scarp closing in |
| 80 (9,810) | — | 290 | 290 | |
| 92 (11,250) | — | 280 | 280 | The narrowest: 280 px |
| 97 (11,850) | — | 300 | 300 | |
| 100 (12,210) | — | 360 | 360 | The scarp sinks away; wall height 0.5 |
| 103 (12,570) | — | 480 | 480 | Gone: open black water; wall height 0 |

**Notches** (the side caves, 90 px along the wall, 70 deep; index, wall, t at the top edge of the middle (scroll px)): 0 right 60.8 (7,500); 1 right 70.8 (8,700); 2 right 84.8 (10,380).

**Contact**: 10 per touch (easy 6, hard 10), pushed 28 px along the wall's normal over 0.12 s, then a 0.5 s cooldown in which a touch only clamps.
<!-- /data -->

Its three cave mouths (`terrain.notches`, rows 7,500, 8,700 and 10,380: at the top edge at t≈60.8, 70.8 and 84.7) are
drawn by the game; the seven nests stand on the wall's foot beside them, one at a time (step F2): the Siren's rule of
at most one ring in flight per wall forbids Level 25's 1.5 s stagger on a single wall, so each nest is its own wave,
5.6 s apart (on hard a nest sings twice in its 5 s on the screen, every 3.6 s: two nests' rings keep 1.92 s apart only
that far apart).

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). The numbers are the data's (step F2), in
**Act 1 terms** with a `bounty_scale` (see *Credit budget*).

- **HP** (the units' own pages): the Glow Angler 28 (14 ×2 beam era, L26 reference **112**), the Nautilus 100 → 104 (the act HP factor, reference 112 against its first level's 108),
  the Siren 90 → 93; the Eel Swarm and the Driftjelly returning with the act HP factor of part A (from the reference
  DPS 22–28, the Eel's head at about 100 × 112/100 = 112); the beam-era ×2 on the small ones.
- **Density and pacing** (the Act 2+ rules): **223 enemies** over 188 s, **≈ 74 a minute** at medium (easy ≈ 178,
  hard ≈ 245), the act's breather by threat: lighter groups and the dark's slow reading, not fewer shots. A wave
  about every 3 s; no stretch over 2 s with nothing on the screen (the drifters keep it: the aftermath's jellies
  hang on the screen). The Anglers' rings, the Nautilus's spirals and the Driftjelly's rings are the shot
  source. Measured in step F2 (`levelReport -Plevel=26`, Level 25's plan fit until step F3 gives the level its own):
  223 at medium (184 easy, 262 hard), 75.6 a minute; 5.2 on the screen and 57 shots a minute (the Act 2+ floor 2.5 and
  10); two stretches over 2 s at medium (the loss's window, ≈ 158–160, and the aftermath from ≈ 170), two over 3 s on
  easy; the small units 2.2 s on the screen (1.39× Level 10's). After round 55's retime (2026-10-24, the loss 6 s
  later): 73.1 a minute at medium, 3.85 on the screen and 73.8 shots a minute; the two long stretches are the loss's
  (≈ 157–166) and the aftermath's (≈ 176–188); the typical haul +1.3 %.
- **Quiet windows**: no other wave for 3.5 s after the Anglers' first sight (t=24); the Choir's fragment
  (t=99–105) keeps the field to the snakes and the drifters already out.
- **The constellation** (t=108, 8 Anglers in a field, with a jelly field after): the "big swarm" the Choir's
  static announces; it is the level's hardest stretch and the Sonar Pulse's showcase.
- **Rook's loss** (t=161–166, see below) takes the field: after t=151 only the jelly drifters come (t=166 and
  172): the aftermath is intentionally quiet and counts as the level's one long gap.

The data's waves (step F2; `side` = a Siren nest on the scarp's foot, `wall` in the data). Every other wave enters from
the front. The Driftjelly and Glow Angler fields enter with the water and drift on it; the draft's `scattered` Angler rows
are sparse fields (no `scattered` formation in the vocabulary: a 400 px wide area, 100 px between units); while the
scarp is on the screen (t=62–100) the fields and the snakes keep to its open left side (x 30–270); an eel swims its
route with its sway, a pair of eels is two rows 4 s apart (t=96/100, 141.5/145.5); a Nautilus pair (`circle`) orbits
the ship for its 15 s hold. Step F2's tuning against the draft (`levelReport -Plevel=26`, pending review): the t=26
Angler row at 27.5 (the first sight's quiet window, 24–27.5), the t=65.5 field at 64 (no empty stretch after the first
nest), the nests one at a time (above), the t=102 field in over 5 s (it drifts through the Choir's quiet window), the
t=152 field spread out to the loss's window and the aftermath's jellies in over 5–6 s each. M8 AW (step AW2, pending the
user's review in round 56): the faster harpoon left gaps on easy (three over 3 s) and the small units at 0.89× Level
10's: the t=152 Angler field at t=151 and the t=89 jelly field at t=87.5 (0.92×, easy two over 3 s).

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 5 | 1 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | In the last lamps' reach, veins lit |
| 8 | 1 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front | Scattered |
| 11 | 1 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | The crown glow seen while the lamps still burn |
| 13 | 1 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 15 | 1 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 18 | 1 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The stripe lit; the last lamp goes out behind it |
| 24 | 2 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front | **New.** First sight: Varga's warning; quiet window of 3.5 s after it |
| 27.5 | 2 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front | Scattered (the README's t=26, after the quiet window) |
| 28.5 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 32 | 2 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 3 | front | Scattered |
| 35 | 2 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | |
| 37 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 39 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 42 | 2 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 5 | front | |
| 45.5 | 2 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | |
| 49 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 52 | 2 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front | Scattered |
| 55 | 2 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | |
| 58.5 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 61.2 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 1 | wall (right) | The first Sirens of the dark, beside the first cave: their glow is the tell |
| 64 | 3 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 5 | front | On the open left side (the README's t=65.5: no empty stretch after the first nest) |
| 66.8 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 1 | wall (right) | |
| 69 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | |
| 72.4 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 1 | wall (right) | Beside the second cave |
| 75.5 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 78 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 1 | wall (right) | |
| 79 | 3 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | |
| 81 | 3 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front | Scattered |
| 83 | 3 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front | Scattered |
| 83.6 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 1 | wall (right) | Beside the third cave |
| 87.5 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 89.2 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 1 | wall (right) | |
| 92 | 3 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 6 | front | |
| 94 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 94.8 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 1 | wall (right) | The last nest as the scarp sinks |
| 96 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The first of a pair 4 s apart; its head drops an overdrive |
| 100 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The second of the pair; the scarp sinks away |
| 102 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 5 | front | Drifting through the Choir's fragment, in over 5 s |
| 108 | 4 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 8 | front | The constellation, the big swarm the Choir announced; quiet window of 3.5 s before it; its centre Angler drops a shield cell |
| 110 | 4 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front | Scattered |
| 112.5 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 8 | front | |
| 116 | 4 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | |
| 118 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 120 | 4 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front | Scattered |
| 123.5 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | |
| 127 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 129 | 4 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front | Scattered |
| 131 | 5 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 6 | front | |
| 134.5 | 5 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | |
| 138 | 5 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 141.5 | 5 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The first of a pair 4 s apart |
| 143.5 | 5 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 145.5 | 5 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The second of the pair |
| 145.5 | 5 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 5 | front | Scattered |
| 149 | 5 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | |
| 151 | 5 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front | The last fight before the silence, spread out to the loss's quiet window |
| 166 | 6 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 5 | front | Harmless drifters: their veins are the only light in the aftermath; no other wave after this |
| 172 | 6 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front | Drifting in over 5 s |

Totals: Driftjelly 118 · Glow Angler 78 · Spiral Nautilus 12 · Eel Swarm 8 · Siren 7.
<!-- /data -->

## Rook's loss (Q7 a)

The act's emotional beat, scripted and **harmless to the player** (no hit boxes: drawn trails only), at the
end of the level. Rook's escort flight plays it; without him it is radio only.

| t (s) | What |
|---|---|
| 148 | Rook's last pre-line (his one dry joke is earlier, t=13). The music thins to the pad at 150. |
| 155 | Varga: "Contacts below!" (short): her sonar finds them as they launch. |
| 161.2 | Rook's last line, voiced: "Lancer, something's below u—", opening as Varga's shout closes. |
| 162.5 | While he speaks, three thin **torpedo trails** rise from the bottom edge, one each in the lanes either side of Rook and one on him, white-orange against the black. The Depth Hunters themselves are never drawn: they stay below the field (their first, unexplained appearance; no hit boxes, no damage; the player's own lane is never one of the lanes). |
| 163.5 | The trails converge on Rook's lamp: three flashes that **cut his line mid-word** (his voice in the "u"; the subtitle ends on "…below u—"); tossed up a little by the hits, the lamp flickers and **spins down** on the screen in a long spiral, sinking faster and faster, 2.5 s, going dark as it leaves the bottom edge (at 1.25× the scroll). |
| 166–172 | Silence; the music out; the ship alone in the black with the jellies' glow. |
| 173.5 | Okafor: "Aegis Two, report. Rook? Rook, answer!" (no answer). |
| 180.5 | Varga: "His lamp went down, Commander." |
| 187 | Okafor: "Finish the dive, Lancer. We will find him." (the level-end line). |

- **The state**: it is not a normal "Downed" (the eject and the repair of the next visit): the save records
  `rook_missing: true` **from the win** of this level ([wingmen](../../../player/wingmen/README.md#rooks-loss-and-absence-l26l29)).
  A retry after the loss (armour at zero in the last seconds) flies the level again with Rook; the loss is
  replayed. If he was ejected earlier in the attempt, the beat is radio only.
- **`--escort none`** or a run without Rook: no lamp, no trails; his last pre-line and his cut line are heard as
  radio (the scripted lines are not `requires: escort`), then Okafor and Varga as above.
- **After the win**: the debrief names him; the next hangar shows him MIA with the Bulwark loaner
  ([wingmen](../../../player/wingmen/README.md#heavy-drone-bulwark-escort-alternative-l22)); no Rook lines in
  Levels 27 and 28.
- **Why on the screen**: the act's beat is not told in a briefing; it is the L26 ending the player flies into
  (the unseen torpedoes also set up Level 27's Depth Hunters, first seen there).

## The Choir (Q8 a)

At **t=99**, 2 s after the Choir's static shows in the HUD portrait frame (the portrait's interference warning
for a big swarm), a layered, pitched-down, reversed-whisper line in the Acts 3–4 register, subtitled in
lowercase with ellipses: "*…small ones… in the dark… your kin… came before you…*". It only hints: no
door, no "the one who opened" (Acts 5–6). The music ducks 6 dB under it. Varga answers at t=110.5 ("*Your kin.
It said your kin, Commander.*"); Okafor cuts it at t=118 ("*Noted, Doctor. Eyes front.*"). The drafted
"*Your kin opened the door. Why do you fight?*" is rejected here (it is the Act 5–6 voice; the act README's
line is revised by the act step). The constellation of Anglers follows at t=108.

## Ground targets

The sea floor is out of sight in the black; this level has no ground units. The one ground object is the
secret:

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 3 | Thera Deep farm barge (`ground` trigger, 20 HP, t≈84, x 60) | The secret: frees a crate worth 100 (410) |
<!-- /data -->

## Hazards

- **Darkness** (above): a look only.
- **Glow Angler lunges**: touching the lure or coming within 130 px (easy 100, hard 160) triggers a lunge and
  a ring of 8; the lure does no damage.
- **Driftjelly rings**: an 8-shot ring when the ship is within 96 px.
- **Siren rings** at the scarp: as Level 25; the scarp's wall contact 10 (easy 6).
- **Water** and the special limits as Level 25 (no Airstrike or Orbital Lance).

## Secrets and pickups

- **The farm barge** (the secret, t≈84, x 60, the left side of the dark): a drowned Thera Deep farm barge
  with a single working lamp: the **only warm light** in the Blackwater, visible from the dark's far side
  against the Anglers' teal; flying to it is the secret (a player may fear a lure; Varga's event line tells
  it apart). A `ground` trigger of **20 HP**; destroyed, its cargo pod pops a crate worth **100** (410). The
  Salvage scanner's glint shows it at any range. Its line, Varga: "*A farm barge, lamp still on. Warm light,
  Lancer: that one is ours.*"
- **Overdrive**: dropped by the t=96 Eel Swarm pair's first snake (`dropped_by: {wave: 96, unit: first}`).
- **Shield cell**: dropped by the central Angler of the t=108 constellation: the field's unit nearest its centre where
  the scatter puts it at its entry (`dropped_by: {wave: 108, unit: centre}`, gap check F10).
- **Armour patch**: a drifting Thera Deep survey buoy (warm gold glow) releases one at (240, 380 y-up) at
  t=130, in the open water before the deep.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), checked against the HUD's **1 s rule** (no timed line more than
1 s late, with Rook and without) by the game's radio timeline test (step F2 wrote its runs, `briefs/m8f2-radio.patch`,
for step F5; the lines play as text until step VOX-F voices them). Event lines drop after 6 s. Rook's barks carry
`requires: escort` and end at the loss's trails (t=162.5); his scripted lines at t=13, 148 and 161.2 are not barks (no
`requires`: they play with him, ejected, without him or with the Bulwark). The Choir's line is subtitled in lowercase,
voiced as the Choir; its row's `static_lead: 2` shows the HUD portrait's interference from t=97 and its `duck_db: 6`
ducks the music under it. The secret's line (the farm barge destroyed): Varga, "A farm barge, lamp still on. Warm
light, Lancer: that one is ours."

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Blackwater, Lancer. No lamps, no charts. Your headlight and whatever glows, nothing else." |
| t=13 | Rook | "Dark and wet. At least nobody sees me being scared." |
| t=24.5 | Varga | "That glow is not a pickup. Teal is Vrell. Touch it and it lunges." |
| t=46 | Varga | "Lost them? Pulse the sonar. It lights everything for six seconds." |
| t=63 | Varga | "A fault wall on your right. Sirens in its caves: watch for the glow before the ring." |
| t=99 | The Choir (distorted) | "…small ones… in the dark… your kin… came before you…" |
| t=110.5 | Varga | "Your kin. It said your kin, Commander." |
| t=118 | Okafor | "Noted, Doctor. Eyes front." |
| t=140 | Okafor | "Almost through the deep, Lancer. Vents ahead." |
| t=148 | Rook | "Lancer, if I buy you a drink for this one, we're even." |
| t=155 | Varga | "Contacts below!" |
| t=161.2 | Rook | "Lancer, something's below u—" |
| t=173.5 | Okafor | "Aegis Two, report. Rook? Rook, answer!" |
| t=180.5 | Varga | "His lamp went down, Commander." |
| t=187 (Level end (t=187)) | Okafor | "Finish the dive, Lancer. We will find him." |
| Secondary met (at the end) | Okafor | "Clean crossing. Now find him, Lancer." |
<!-- /data -->

Retimed in step F2 to the HUD's real hold (every line on the panel at least 5 s, then 0.4 s of silence; the radio
timeline test with the lines as text, with and without Rook; pending the user's review): Rook's pre-line from t=150 to
148, so Varga's warning follows it; Varga's "Contacts below!" from t=157 to 155 (her sonar finds the torpedoes as they
launch, 2 s before their trails rise; at 157 it would hold the radio past the cut line); Okafor's call from t=165 to
167.5, Varga's reply from 171 to 174.5 and Okafor's level-end line from 176 to 181, each after the line before it. Rook's
one dry joke (t=13) is the level's; his t=148 line is the warm one the cut line answers.

Retimed again after round 55 (the user's playtest of 2026-10-24: "The line comes several seconds after he's been shot
down"; "Rook can't talk after being shot down"): the cut line comes **before** the hits and the hits cut it. It is due
at t=161.2, as soon as Varga's shout has closed (≈ 161.1 at the default text speed), the trails rise from t=162.5 under
"something's below" and the hits land at t=163.5, 2.3 s into the line, in its last word (the take's "u"); everything
after the loss moved 6 s later (Okafor 173.5, Varga 180.5, Okafor's level-end line 187, the level's end 188).
`RadioTimelineLevel26Test` checks on every difficulty and fit that the line opens before the trails and that the hits
land at least 2 s in and while its voice still speaks.

## Music & ambience

The Act 4 **darkness variant** of the A theme ([track 10](../../../audio/music/README.md#track-list)
"Thera Deep"): in section 1 the full A theme with the lamps, then it **strips back** as the lights go
out (t=8–18) to sonar pings in silence and a deep drone; the scarp adds the Sirens' harmonic layer; the Choir's
fragment ducks it; the pad thins from t=150; **out at t=166** (silence for the aftermath, Rook's loss);
the vent field's warm pad creeps in from t=174. No bubble layer (round 48 Q12 a). Ambience: the `europa-black`
bed (a heavy pressure drone, distant whale calls, no bubbles), the `europa-trench` rumble at the scarp.
New sounds this level needs: the Angler's lure hum and lunge, its ring, the Driftjelly's soft pulse, the
Nautilus crown's violet hum, the torpedo trails (a long, distant rising hiss), Rook's lamp flicker and spin-down,
the Choir's layered whisper, the headlight click on at the level start; listed in
[sfx](../../../audio/sfx/README.md) by the act's audio step (Freesound for real-world sounds only).

The data (step F2; played by step F5): track 10 from section 1 with every stem, then the music's `stem_times`: the dark
stem (`thera-deep-dark`) alone from t=8 over 10 s, −6 dB from t=150, out (`none`) at t=164–166, the warm pad stem
(`thera-deep-warm`, round 55's pick b "a synth pad") from t=174 over 10 s; the ambience `europa-black`, with an `ambience_layers`
`europa-trench` over the scarp (t=56–104).

## Credit budget

Budget(26) = 700 × 1.07²⁵ ≈ **3,799** ([economy](../../../systems/economy/README.md#per-level-budget)). Data
in **Act 1 terms** (Driftjelly 10, Glow Angler 14, Spiral Nautilus 30, Eel Swarm 48 whole as the code counts it (gap
check F11), Siren 28, the crate 100, the secondary 100), × the act factor **4.096**, bounties × the scale. A
**`bounty_scale` of 0.43** (the README's estimate was 0.41) puts the typical haul at 3,850, **+1.3 %** of budget(26)
(`levelReport -Plevel=26`, step F2; within the ±5 % of `TypicalHaulTest`). The table's typical column is the sync
script's estimate (+13 %); the build's measure (`levelReport`, `TypicalHaulTest`) is the one the test holds (Levels 23
and 24's tables differ from it the same way, by less).

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Driftjelly 118 × 10 + Glow Angler 78 × 14 + Spiral Nautilus 12 × 30 + Eel Swarm 8 × 60 by parts (48 whole) + Siren 7 × 28 | 5,949 | 3,865 |
| Secret: farm barge (hidden crate, 3% of budget) | 410 | 205 |
| Secondary: clean crossing | 410 | 205 |
| **Total** (bounty scale 0.43) | **6,769** | **4,275** |
| Budget(n) = the typical haul's target; typical +13 %, perfect 1.78 × budget | | 3,799 |
<!-- /data -->

**Balance plan**: the plan's L26 visit buys the Harpoon Torpedoes L4 (one visit before the
[Act 4 hand plan](../../../player/generator/README.md#act-4-hand-plan-draft)'s L27; the Mk IV follows at L27); the
level expects `homing` and `area` and the Sonar Pulse, and is flyable without them.

## Difficulty notes

- **Easy**: the Angler's trigger 100 px and a ring of 6; the Nautilus with 2 arms; the Siren's slow 40 % for
  1.5 s; the formation lever thins the fields; the ambient assist is the settings' lever, not the difficulty's.
- **Hard**: the Angler's trigger 160 px and a ring of 10; the Nautilus with 4 arms; the Siren ring every 3.6 s;
  the fields larger. The shoot-down is the same on every difficulty.

## Concept art

Production art of M8 part F (round 55, step ART-F) and game captures; the generators and briefs are in
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r55-a.png](concept/backdrop-final-r55-a.png) | Production backdrop (`tools/art/backdrop_l26.py`, from darkness-r48-a): the drowned pipeline and its lamp posts (the last one failing), the trench's last beacons, the black water (near black: the light map shows it), the fault scarp on the right with three caves, two amber seeps far below, the vent field's red glow at the end; ten screens in the dark look's stand-in (ambient 20 -> 6 %, the headlights, Rook's lamp) (sheet) | chosen (round 55, F.1: accepted as final) |
| [concept/backdrop-final-r55-a.gif](concept/backdrop-final-r55-a.gif) | t 10-18 at half size: the last lamps and the light going out | chosen (round 55, F.1: accepted as final) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block, the `terrain:` block (the scarp, three notches) the art is drawn for and the static lights it assumes | chosen (round 55, F.1: accepted as final) |
| [concept/l26-props-final-r55-a.png](concept/l26-props-final-r55-a.png) | The secret (`tools/art/l26_props.py`): the drowned farm barge with its one working sodium lamp (lying, hit, opened) and its glow (the lamp, then the stutter and sparks) (sheet) | chosen (round 55, F.6: accepted as final) |
| [concept/l26-props-final-r55-a.gif](concept/l26-props-final-r55-a.gif) | The barge's warm lamp among the anglers' teal lures, found by the cone, shot open, its crate rising | chosen (round 55, F.6: accepted as final) |
| [concept/rook-loss-final-r55-a.png](concept/rook-loss-final-r55-a.png) | Rook's loss (`tools/art/rook_loss.py`): the hydro craft's tumble and its failing lamp, the torpedo trail strip and motor, the hit's flash under water (sheet) | proposed (round 55, F.5: the spin redone on the screen after the user's playtest, 2026-10-24; shown again in round 57) |
| [concept/rook-loss-final-r55-a.gif](concept/rook-loss-final-r55-a.gif) | The trails rising in their lanes, the three flashes, the spin-down off the bottom edge, the dark | proposed (round 55, F.5: the spin redone on the screen after the user's playtest, 2026-10-24; shown again in round 57) |
| [concept/lamps-capture-r55-a.png](concept/lamps-capture-r55-a.png) | Game capture, M8 part F (step F6; Level 26 on medium, `--invulnerable`, auto-fire, the balance plan's Level 26 fit: Harpoon Torpedoes L4, Depth Charges L3, two Micro-Missile Pods L3, the Pressure hull, the Sonar Pulse pressed at t≈64 and t≈110, Rook along; t = the level time): the lamp stretch into the dark (t=1, 5, 9, 13, 17): the lamp posts' sodium pools and the beacons scrolling with the ground, the ambient ramping 20 → 6 % over t=8–18, the twin headlight cones with their faint haze lighting the floor and the marine snow | chosen (round 55, F.2: accepted) |
| [concept/open-water-capture-r55-a.png](concept/open-water-capture-r55-a.png) | Game capture (step F6, the same run, 1:1): the cones in open water (t=26, 28, 30): the faint beam haze (step F5) and the snow lit only inside the cones, Rook's lamp beside the ship | chosen (round 55, F.2: accepted) |
| [concept/angler-capture-r55-a.png](concept/angler-capture-r55-a.png) | Game capture (step F6, a run with the guns held, 1:1 round the ship, t≈34–36): Glow Anglers in the dark as a teal bead and red eyes on the faint rim, one lunging at the ship and ringing its 8 shots | chosen (round 55, F.3: accepted) |
| [concept/angler-capture-r55-a.mp4](concept/angler-capture-r55-a.mp4) | The same as a video with the game's sound (t≈31–41): the lures' hum, the lunges and their rings | chosen (round 55, F.3: accepted) |
| [concept/scarp-capture-r55-a.png](concept/scarp-capture-r55-a.png) | Game capture (step F6, the main run): the fault scarp on the right (t=64.5, 70, 78, 84, 92): the first Sonar Pulse lighting every unit whole (64.5), the Sirens' sacs glowing at the caves and their rings, a Nautilus's crown, the farm barge's warm lamp (84) | chosen (round 55, F.2: accepted) |
| [concept/sonar-capture-r55-a.png](concept/sonar-capture-r55-a.png) | Game capture (step F6, the main run): the constellation (t=109.5, 111, 113, 116, 118): the Anglers as beads in the black, the second Sonar Pulse (t≈110) lighting them whole for its 6 s, the dark again after it | chosen (round 55, F.2: accepted) |
| [concept/choir-capture-r55-a.png](concept/choir-capture-r55-a.png) | Game capture (step F6, the main run, whole screen): the Choir's line (t=97, 98.5, 99.5, 101): the static in the HUD's portrait frame for the 2 s before it, then the Choir's portrait and its subtitled whisper | chosen (round 55, C.5: accepted) |
| [concept/choir-capture-r55-a.mp4](concept/choir-capture-r55-a.mp4) | The same as a video with the game's sound (t≈94.5–106.5): the static's sound (choir-static a), the whisper, the music ducked 6 dB under it | chosen (round 55, C.5: accepted) |
| [concept/rook-loss-capture-r55-a.png](concept/rook-loss-capture-r55-a.png) | Game capture (step F6, the main run): Rook's loss (t=156.9, 157.2, 157.5, 157.8, 158.2, 159, 162): the three trails rising to his lamp, the flashes, the spin-down off the bottom edge, the dark without his lamp | proposed (round 55, F.5: the spin redone on the screen after the user's playtest, 2026-10-24; shown again in round 57) |
| [concept/rook-loss-capture-r55-a.mp4](concept/rook-loss-capture-r55-a.mp4) | The same as a video with the game's sound (t≈153.5–177.5): his last lines, the trails and the hits, the spin, the silence from t=160 and the vents' warm pad (a) creeping in from t=168 | proposed (round 55, F.5: the spin redone on the screen after the user's playtest, 2026-10-24; shown again in round 57) |
| [concept/debrief-capture-r55-a.png](concept/debrief-capture-r55-a.png) | Game capture (step F6, the main run): the debrief with the red `ROOK  MIA` row under the tally | chosen (round 55: accepted) |
| [concept/rook-loss-capture-r57-a.png](concept/rook-loss-capture-r57-a.png) | Rook's loss after step FX (round 55 F.5's redo; step G5, Xvfb, `--bench`, medium, the plan's fit, `--invulnerable`, the guns held until t=158 so the silence is heard): his lamp beside the ship at t≈162.4, the three trails at 163.2, the hits at 163.6–163.9, his ember sinking at 164.8–165.1 (1:1), the screen at t≈169 (`ROOK  MIA` in the escort row) | proposed (round 57) |
| [concept/rook-loss-capture-r57-a.mp4](concept/rook-loss-capture-r57-a.mp4) | The same run t≈150–180 with the game's sound: his cut line, the trails, the hits cutting it, the spin, the silence, the warm pad b and the last lines | proposed (round 57) |
| [concept/dark-assist-capture-r55-a.png](concept/dark-assist-capture-r55-a.png) | Game capture (step F6): the Dark levels assist (Options) at t=28: off (left, the main run) and on (right, a second run: the ambient held at 20 %) | chosen (round 55, C.6: accepted) |

## Implementation

- [x] Level data `data.yaml` (step F2): sections and speeds with eases, the ambient ramp, the
      scarp terrain, waves, radio, budget, threat profile, briefing, music (`ContentLoaderTest`, `levelReport`)
- [x] Darkness look (Q4 a): the glow-only pass for units outside the light, the cone, light pools, the ambient
      ramp, bullets, pickups, Rook's lamp always visible; nothing in the simulation changes (steps F4, F5: `DarkLight` on
      LX's light map; `Level26LooksTest.theDarkRampsDownAndTheAssistHoldsItsFloor`, `theConeLightsOnlyAheadOfTheLamps`,
      `theStaticLightsLieOnTheirRows`, `theUnitsRimGlowAndSonarPools`, `PassOrderTest`; `Level26Test.theDarkIsTheLooksKeysOnly`;
      F5's faint beam haze and the snow lit in the cones; the Dark levels assist holds the ambient at 20 %)
- [x] The Glow Angler's lure and lunge (130 px or touch), the Driftjelly and Eel Swarm in the dark (`GlowAnglerTest`,
      `Level26Test.theGlowAnglerLungesFromItsLure`, `Level26LooksTest.theAnglersStepsAndStalk`, `anAnglerLungesAndRings`,
      `theArtIsWhatTheLooksDraw`: the jelly's veins and the eels' glow-only frames)
- [x] The fault scarp (one wall, three notches, the Sirens' glow tell) (`Level26Test.theScarpItsNotchesAndItsNests`;
      the Sirens' glow frames from part E over the grade, `Level26LooksTest.theUnitsRimGlowAndSonarPools`)
- [x] The Choir's fragment with the HUD static 2 s before, the music ducking (Q8 a) (`StaticLeadsTest`,
      `Level26SoundsTest.theChoirsLineDucksTheMusicSixDecibels`, `Level26Test.theChoirsLineAndItsKeys`)
- [x] **Rook's loss**: the scripted trails (visual only), the lamp spin-down, the cut line, `rook_missing`
      saved at the win, the replay on a retry, radio only with `--escort none` and when ejected (`RookLossTest`,
      `RookLossLooksTest`, `Level26Test.rooksLossAndItsReplayOnARetry`, `withTheBulwarkOrNoEscortTheLossIsTheRadiosOnly`,
      `ActPlaythroughTest` (the win records him missing), `Level26SoundsTest.theLuresHumTheCrownsSingAndRooksLossIsHeard`;
      the debrief's red `ROOK  MIA` row, seen in round 55's capture)
- [x] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), the quiet windows, edge warnings (none due:
      every wave enters from the front, the nests with the scarp; `Level26Test`)
- [x] Kill-ratio secondary at 65 %, judged at the level end (`Level26Test`)
- [x] Radio voiced, every timed line at most 1 s late (with Rook and without), the Choir's line subtitled (step VOX-F;
      `RadioTimelineLevel26Test`, `VoiceFilesTest`)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`)
- [x] Briefing: both pages, each one screen, with their images (`BriefingLayoutTest`; ART-F's images)
- [x] Typical haul within ±5 % of budget(26) = 3,799 with the level's `bounty_scale` (`TypicalHaulTest`: +1.3 %)
- [x] Music: the darkness variant by section; the `europa-black` ambience (no bubbles) (`Level26SoundsTest.
      theStemsStripBackThinGoSilentAndTheWarmPadComesIn`, `theExtraStemsAreOnTheThemesGrid`,
      `theBedIsTheBlackWaterAndTheTrenchOverTheScarp`; the warm pad a/b in round 55)
- [x] Easy/hard variations; the autopilot flies the level on every difficulty (with and without Rook; `Level26Test`,
      the campaign playthrough to the Level 27 visit, `ActPlaythroughTest`)
- [x] Production art and captures for the round (steps ART-F, F4 and F6; [round 55](../../../concept-rounds/round-55/README.md))
- [x] Round 55's fixes (the user's playtest of 2026-10-24, step FX): Rook's cut line before the hits, the hits cutting
      it (`RadioTimelineLevel26Test.theHitsCutRooksLastLine`), the loss and the aftermath 6 s later (`Level26Test`,
      `Level26SoundsTest`), his spin on the screen (`RookLossTest`, `Level26Test.rooksLossAndItsReplayOnARetry`); the
      round's sound picks in the game (`Level26SoundsTest.everySoundHasItsFileRoundFiftyFivesPick`); the spin's look
      shown again in round 57 (F.5)

## Decisions

- 2026-10-21 (Act 4 design phase, step m8-dl2526): drafted from the roster row, the gap analysis and the wave 1
  documents; **decided in round 48 (2026-10-22)**: Q4 a (a look only), Q6 a (a near-miss lure in teal with
  a stalk), Q7 a (the shoot-down on the screen, harmless trails, the cut line), Q8 a (the Choir's hint, not
  the drafted line); rejected options are those of the gap analysis.
- 2026-10-21: the Act 2+ floor applies to the breather (≈ 74 a minute; the aftermath is the one long gap,
  kept non-empty by drifting jellies); the Glassfin is not used here (the dark's breather has the Anglers).
- 2026-10-21: the secret is the farm barge's warm lamp, the only warm light in the black; the pre-loss line
  and the cut line are scripted, Rook's barks stop after t=157.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-23 (step F2, gap check `m8f-gaps.md` §5, accepted in round 55 (2026-10-24)): the level as data. The open
  questions close: the Sirens stand on the scarp as drafted (F5); the eel pays by the code's count, 48 whole typical and
  60 by parts perfect (F11); a retry after the loss replays it (J3). The dark is the light map with this level's values,
  not `hazards.darkness` (F1, the *Code fit* line corrected); the ambient ramp is a look key of section 1 (F4); the
  central Angler is the field's unit nearest its centre (`unit: centre`, F10). Step F2's own choices: ART-F's scarp and
  notches adopted; the nests one at a time, 5.6 s apart (one ring in flight per wall on one wall, every difficulty); the
  draft's `scattered` rows as sparse fields; the waves' and the loss's radio retimes above; `bounty_scale` 0.43.
- 2026-10-23 (step F3, accepted in round 55 (2026-10-24)): the tests, the plan fit and the autopilot. The balance
  plan's fit (the harpoon L4, both pods L3, the Depth Charges L3, the Sonar Pulse, the Pressure hull, Composite III,
  Rook's Autocannon) flies the level on every difficulty with and without Rook, no retries; hard is won without any
  tuning of the data: the autopilot plays the dark by planning every move (`DarkPlay`: the bullets flown on, the bodies,
  a drifting Angler's trigger, a jelly's reach and a diving Angler's ring kept clear of, the line under the lowest unit
  it can reach from outside its zone). Measured (`levelReport -Plevel=26`, 3 seeds): easy 2.46 on the screen, medium
  4.88 and 60.7 shots a minute (floor ok, two stretches over 2 s: the loss's silence and the aftermath), hard 7.71;
  small units 1.37× Level 10's time on the screen; typical haul +1.3 % (0.43 hits the budget). The kill ratio is met on
  easy and medium; on hard the autopilot kills about 60–65 % (the harpoon's one torpedo a second bounds it; met in the
  campaign playthrough's hard run, 172 of 262), printed, not asserted. The campaign playthrough now flies Act 4 to Level
  26: its win records Rook missing and the Level 27 visit has the loaner Bulwark in his slot.
- 2026-10-23 (steps F1, F4, F5, ART-F and the gap check `m8f-gaps.md` §5, accepted in round 55 (2026-10-24)): the
  rest of the level's small rules. **Rook's loss** (F1, F6–F8): the trails rise in Rook's lane and x ± 48 px (a lane
  within 40 px of the ship's x moves to his far side), visual only with no hit boxes; his barks end at the trails
  (t=157), his scripted lines play in every case; the loss is no eject (no pod, no repair line); he falls at
  max(distance / 2.5 s, 1.25 × the scroll), so he leaves the screen in about 0.7 s, spinning away with a 40 px drift
  (changed after round 55, C.2: see 2026-10-24);
  the Bulwark in the slot (or no escort) hears the loss on the radio only and carries Rook's 56 px lamp; the debrief
  names him with a red `ROOK  MIA` row when the win sets the flag. **The dark** (F3, F4, F5): a 15 % rim pool per unit
  (its body and a faint halo), the kinds' glow frames drawn over the grade; every unit whole for the Sonar Pulse's 6 s,
  the backdrop staying dark; the ambient ramp a look key of section 1; a faint haze (0.07) in the cones and the snow lit
  only there (Levels 23–26 too), so the beam reads in open water; the Dark levels assist (Options, off by default) holds
  the ambient at 20 %; the pickups keep their gold halo ("the only warm light" is the scenery's), the stalk shows within
  160 px on every difficulty (F13). **Pacing and music** (F9, F12): the aftermath's long gap is a quiet window t=156–160,
  the jellies at t=160 and 166 keep the rest non-empty; track 10 only, the Sirens' harmonic layer is their rings' sung
  sweep, the warm pad from t=168 is round 55's a or b. **Art** (ART-F): the scarp's profile (rising t=56–61, 280 px from
  t=80, out by t=103) and its three notches; six lamp posts every 360 px, the sixth failing; two trench beacons at the
  start; two amber seeps (t=143, 149.5); the vent glow as a 2040 px ground piece whose glow rises toward the top edge;
  the farm barge's look (no concept of its own); the Driftjelly's "veined re-tint" as glow-only veins (no re-coloured
  body); the eels' resting stripe at 0.35; Rook's spin as a scorched tumble with his lamp dark (its light is the glow
  set), the trails as a repeated strip.
- 2026-10-24 (M8 AW, step AW2; accepted in round 56 (2026-10-24)): with the reworked harpoon (L4) the t=152 Angler
  field moves to t=151 and the t=89 jelly field to t=87.5 (`PacingTest` easy, `BeamEraHpTest` 0.89× → 0.92×); won 3/3 and
  the secondary 3/3 on every difficulty, the typical haul unchanged (+1.3 %). The Glow Angler's HP 17 (34 here) was tried
  and dropped (0.90×, a medium stretch more).
- 2026-10-24 (user, round 55 closed): F.1–F.4 and F.6–F.8 accepted as final (the backdrop, the dark look, the Glow
  Angler, the glow-only frames, the farm barge, the briefing images and the intel), C.1, C.3 and C.5–C.7 accepted; the
  sounds lure a, lunge a, crown a, trails a, hits a, spin **b**, click **b**, static **b**, bed a and the warm pad **b**
  (in the game since step FX). The steps' logged small rules are accepted. **C.2 and C.4 changed, F.5 redone** after
  the user's playtest of Level 26 ("The timing with Rook's 'Lancer, something's below us' radio call is very bad. The
  line comes several seconds after he's been shot down"; "Rook can't talk after being shot down"), step FX: his cut
  line comes **before** the hits and the hits cut it: due at t=161.2 (as soon as Varga's 5.5 s shout has closed, so it
  opens on time), the trails from t=162.5 under "something's below", the hits at t=163.5, 2.3 s in, in his last word;
  the loss and everything after it 6 s later than before (the music out at t=164–166, the jellies at t=166 and 172, the
  warm pad from t=174, Okafor 173.5, Varga 180.5, Okafor 187, the vent field's piece, the level's end 188; Varga's
  shout stays at t=155, before the trails). His **spin plays on the screen** (C.2): the hits toss him up a little and
  he sinks back faster and faster, crossing the bottom edge as the 2.5 s spin ends at 1.25× the scroll (from high on
  the screen a steady fall at the speed that takes him off in time); the lamp's flicker and going dark are seen whole.
  `levelReport -Plevel=26` after it: medium 73.1 a minute, 3.85 on the screen, 73.8 shots a minute, three stretches
  over 2 s (the loss's ≈ 157–166 and the aftermath's ≈ 176–188 the long ones; the rule's at most 3: ok), the typical
  haul +1.3 %, won and the secondary met 3/3 on every difficulty. F.5 (Rook's loss) is shown again in round 57 with
  part G. The debrief's `ROOK  MIA` row is unchanged.
- 2026-10-25 (step G5): Rook's loss after step FX captured again for [round 57](../../../concept-rounds/round-57/README.md)
  L.1 (`rook-loss-capture-r57-a`, the guns held from t=158 so the silence is heard); after the hits his lamp is out, so
  his spin shows only as a faint ember for about 1.5 s.
