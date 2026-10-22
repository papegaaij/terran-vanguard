---
title: Level 26 – Blackwater
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/naval/glow-angler, ../../../enemies/naval/depth-hunter, ../../../enemies/naval/siren, ../../../enemies/naval/spiral-nautilus, ../../../enemies/naval/eel-swarm, ../../../enemies/naval/driftjelly, ../../../story/characters/the-choir, ../../../story/characters/rook, ../../../world/europa, ../../../player/wingmen, ../../../player/specials]
updated: 2026-10-22
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
screen, in the black (Q7 a). 182 s: the last lamps, the dark, the fault scarp with its Sirens, the Choir
and the constellation, the deep, and the silence.

**Status: approved in round 48 (2026-10-22); drafted (Act 4 design phase, 2026-10-21), decided in round 48 (2026-10-22).** Written from
the act roster, the gap analysis's recommended answers (Q4 a, Q6 a, Q7 a, Q8 a) and the wave 1
documents; every number is a first draft for part A.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`). **Draft texts**:

> **Commander Okafor:** "Past the trench there is nothing on the charts, only black water between us and
> the vents. The Vrell route runs through it. Fly it on your headlight, Lancer, and keep Rook close."
>
> **Dr. Varga:** "You'll see only what glows. The Vrell glow teal and violet, and so do their lures:
> a teal bead on a stalk is not a pickup. Bullets always show. The Sonar Pulse lights everything for six seconds."

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-26-blackwater`: a chart with the trench's end and the vent field's glow far to the
  south and nothing in between but a dotted route, the CDF markers lost off the edge.
- Varga's page `level-26-lure`: her diagram of the Glow Angler: its stalk and teal bead in the dark beside a warm
  gold pickup, the ship's cone, the lunge radius (130 px).

*Hangar teaser* (shop screen before L26; Rook, not voiced; his last):

> **Rook:** "Black water ahead. Stay where I can see your lights, Lancer. I'm not that good in the dark."

## Threat profile

Draft; directions counted by each wave's `from` (the intel's rule).

| Field | Value |
|---|---|
| Dominant layers | `air` (the play plane under water, seen as glows: Glow Anglers, Driftjellies, Nautilus pairs, Eel Swarm snakes), the fault scarp's Siren nests |
| Attack directions | front 97% · sides 3% · rear 0% |
| Density | 3 |
| Recommended traits | `homing`, `spread`, `area` |
| Hazards | Darkness: only the headlight cone, light pools and the Vrell's glow; Glow Anglers lunge when touched; Driftjelly rings; Siren rings at the scarp |
| Boss / mid-boss | none |
| Sensor-suite detail | none: Europa, deep water, `air`, front · L1: + directions (all), density 3, hazard "darkness", the objective `CROSS THE BLACKWATER` and the secondary `KILL RATIO 65 %` · L2: + Glow Angler (new), Driftjelly, Eel Swarm, Spiral Nautilus, Siren portraits, the lure marked "not a pickup" · L3: + the scarp's nest positions on the threat arrows |

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
seen late, so the scroll gives more time; the data eases each change over 2 s). Setting:
[Europa](../../../world/europa/README.md)'s deep water, medium `underwater`: the black level of Q2 a. Motion
budget: the glows are the one strong element; the drifting particles (marine snow, lit only by the cone) are the
second; the rest is black.

| Section | t (s) | Scroll (px) | Speed (px/s) | Ambient | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Last Lamp | 0–20 | 0–2,600 | 130 | 20 % → 6 % over t=8–18 | `ground`: the last Thera Deep lamp posts along a drowned pipeline, one every 360 px, until t=14; the trench's beacons falling behind. | The lights go out behind the ship; the Nautilus's crown learnt while lamps still burn. |
| 2. The Dark | 20–60 | 2,600–7,400 | 120 | 6 % | Black water; marine snow in the cone; the only light the units' glow and the pickups' warm gold. | The Glow Angler (t=24); the lure learnt. |
| 3. The Scarp | 60–100 | 7,400–12,200 | 120 | 6 % | A single **fault wall on the right** (a scarp, flyable width 400 → 280 px) with three cave mouths; the Sirens' sacs glowing in them; the farm barge lamp (t≈84, left). The scarp sinks away at t=100. | The Sirens in the dark: their glow is their tell. |
| 4. The Choir | 100–130 | 12,200–15,800 | 120 | 6 % | Open black water; the Choir's static in the HUD portrait from t=97. | The Choir's fragment (t=99); the constellation of Anglers (t=108). |
| 5. The Deep | 130–155 | 15,800–18,800 | 120 | 6 % | Open black water; a faint amber seep glow far below from t=140. | The last fights; Rook's pre-line (t=150). |
| 6. The Silence | 155–182 | 18,800–21,770 | 110 | 6 % | Black water; the vent field's red glow rising on the horizon from t=168; Rook's lamp beside the ship until t≈160. | Rook's loss (t=157.5–163); the aftermath; level end. |

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
- **Code fit**: `HazardSpec.Darkness` (cone, flares, light pools, ambient) is the generic base; this level needs
  a glow-only pass for units outside the light and the settings assist; nothing in the simulation changes.

### The fault scarp (section 3)

The Level 25 terrain rules (one wall instead of two): a polyline on the right, flyable width at least
**280 px** at its narrowest, three notches (caves) with a Siren nest each (the Siren is a wall mount, so it needs
a wall: the scarp is its only place in this level). The nests' glow is their tell; the ring slows as in
[Level 25](../level-25-siren-trench/README.md). The left side is open water. No ground units on the scarp.

## Waves

Enemy definitions are linked per row; formation names from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). All numbers are **draft**, data in
**Act 1 terms** with a `bounty_scale` (see *Credit budget*).

- **HP** (the units' own pages): the Glow Angler 28 (14 ×2 beam era, L26 reference **112**), the Nautilus 100 → 104 (the act HP factor, reference 112 against its first level's 108),
  the Siren 90 → 93; the Eel Swarm and the Driftjelly returning with the act HP factor of part A (from the reference
  DPS 22–28, the Eel's head at about 100 × 112/100 = 112); the beam-era ×2 on the small ones.
- **Density and pacing** (the Act 2+ rules): **223 enemies** over 182 s, **≈ 74 a minute** at medium (easy ≈ 178,
  hard ≈ 245), the act's breather by threat: lighter groups and the dark's slow reading, not fewer shots. A wave
  about every 3 s; no stretch over 2 s with nothing on the screen (the drifters keep it: the aftermath's jellies
  hang on the screen). The Anglers' rings, the Nautilus's spirals and the Driftjelly's rings are the shot
  source; part A tunes with `levelReport`.
- **Quiet windows**: no other wave for 3.5 s after the Anglers' first sight (t=24); the Choir's fragment
  (t=99–105) keeps the field to the snakes and the drifters already out.
- **The constellation** (t=108, 8 Anglers in a field, with a jelly field after): the "big swarm" the Choir's
  static announces; it is the level's hardest stretch and the Sonar Pulse's showcase.
- **Rook's loss** (t=157–163, see below) takes the field: after t=152 only the jelly drifters come (t=160 and
  166): the aftermath is intentionally quiet and counts as the level's one long gap.

Draft wave table (`from`: `wall` = a Siren nest on the scarp):

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 5 | 1 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front | In the last lamps' reach, veins lit |
| 8 | 1 | scattered | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front |  |
| 11 | 1 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | The crown glow seen while the lamps still burn |
| 13 | 1 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 15 | 1 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 18 | 1 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The stripe lit; the last lamp goes out behind it |
| 24 | 2 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front | First sight: Varga's warning; quiet window of 3.5 s after it |
| 26 | 2 | scattered | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front |  |
| 28.5 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 32 | 2 | scattered | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 3 | front |  |
| 35 | 2 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front |  |
| 37 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 39 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 42 | 2 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 5 | front |  |
| 45.5 | 2 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front |  |
| 49 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 52 | 2 | scattered | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front |  |
| 55 | 2 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front |  |
| 58.5 | 2 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 62 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (right, the scarp) | The first Sirens of the dark: their glow is the tell |
| 65.5 | 3 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 5 | front (left, the open side) |  |
| 69 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front |  |
| 72 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 3 | wall (right, the scarp) |  |
| 75.5 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 79 | 3 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front |  |
| 81 | 3 | scattered | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front |  |
| 83 | 3 | scattered | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front |  |
| 86 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | wall (right, the scarp) |  |
| 89 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 92 | 3 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 6 | front |  |
| 94 | 3 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 96 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 2 | front | Two staggered 4 s apart; the scarp sinks away at t=100 |
| 102 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 5 | front | Drifting through the Choir's fragment |
| 108 | 4 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 8 | front | The constellation, the big swarm the Choir announced; quiet window of 3.5 s before it (the Choir) |
| 110 | 4 | scattered | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front |  |
| 112.5 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 8 | front |  |
| 116 | 4 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front |  |
| 118 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 120 | 4 | scattered | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front |  |
| 123.5 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front |  |
| 127 | 4 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 129 | 4 | scattered | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front |  |
| 131 | 5 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 6 | front |  |
| 134.5 | 5 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front |  |
| 138 | 5 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 141.5 | 5 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 2 | front |  |
| 143.5 | 5 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 145.5 | 5 | scattered | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 5 | front |  |
| 149 | 5 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 6 | front |  |
| 152 | 5 | field | [Glow Angler](../../../enemies/naval/glow-angler/README.md) | 4 | front | The last fight before the silence |
| 160 | 6 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 5 | front | Harmless drifters: their veins are the only light in the aftermath; no other wave after this |
| 166 | 6 | field | [Driftjelly](../../../enemies/naval/driftjelly/README.md) | 4 | front |  |

## Rook's loss (Q7 a)

The act's emotional beat, scripted and **harmless to the player** (no hit boxes: drawn trails only), at the
end of the level. Rook's escort flight plays it; without him it is radio only.

| t (s) | What |
|---|---|
| 150 | Rook's last pre-line (his one dry joke is earlier, t=13). The music thins to the pad. |
| 157 | Varga: "Contacts below!" (event line, short). Three thin **torpedo trails** rise from the bottom edge, one each in the lanes either side of Rook and one on him, white-orange against the black. The Depth Hunters themselves are never drawn: they stay below the field (their first, unexplained appearance; no hit boxes, no damage; the player's own lane is never one of the lanes). |
| 157.5 | The trails converge on Rook's lamp: three flashes; the lamp flickers and **spins down** off the bottom edge in a long spiral, scrolling faster than the field, 2.5 s, going dark. |
| 160.3 | Rook's last line, voiced and **cut mid-word**: "Lancer, something's below u—"; the subtitle ends on "…below u—". |
| 160–165 | Silence; the music out; the ship alone in the black with the jellies' glow. |
| 165 | Okafor: "Aegis Two, report. Rook? Rook, answer!" (no answer). |
| 171 | Varga: "His lamp went down, Commander." |
| 176 | Okafor: "Finish the dive, Lancer. We will find him." (the level-end line). |

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

| Section | Target | Effect |
|---|---|---|
| 3 | Thera Deep farm barge (`ground` trigger, 20 HP, t≈84, x 60) | The secret: frees a crate worth 100 (410) |

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
- **Overdrive**: dropped by the head of the t=96 Eel Swarm pair's first snake.
- **Shield cell**: dropped by the central Angler of the t=108 constellation.
- **Armour patch**: a drifting Thera Deep survey buoy (warm gold glow) releases one at (240, 380 y-up) at
  t=130, in the open water before the deep.

## Radio chatter

Spoken radio, draft for the **1 s rule** (as Level 25: ≈ 12 characters a second, a 1.5 s lead, 0.4 s of
silence; event lines drop after 6 s). Rook's barks carry `requires: escort` and stop after t=157; his scripted
lines at t=13, 150 and 160.3 are not barks. The Choir's line is subtitled, voiced as the Choir.

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Blackwater, Lancer. No lamps, no charts. Your headlight and whatever glows, nothing else." |
| t=13 | Rook | "Dark and wet. At least nobody sees me being scared." |
| t=24.5 | Varga | "That glow is not a pickup. Teal is Vrell. Touch it and it lunges." |
| t=46 | Varga | "Lost them? Pulse the sonar. It lights everything for six seconds." |
| t=63 | Varga | "A fault wall on your right. Sirens in its caves: watch for the glow before the ring." |
| barge destroyed (event) | Varga | "A farm barge, lamp still on. Warm light, Lancer: that one is ours." |
| t=99 | The Choir | "…small ones… in the dark… your kin… came before you…" |
| t=110.5 | Varga | "Your kin. It said your kin, Commander." |
| t=118 | Okafor | "Noted, Doctor. Eyes front." |
| t=140 | Okafor | "Almost through the deep, Lancer. Vents ahead." |
| t=150 | Rook | "Lancer, if I buy you a drink for this one, we're even." |
| t=157 | Varga | "Contacts below!" |
| t=160.3 | Rook | "Lancer, something's below u—" (cut) |
| t=165 | Okafor | "Aegis Two, report. Rook? Rook, answer!" |
| t=171 | Varga | "His lamp went down, Commander." |
| Level end (t=176) | Okafor | "Finish the dive, Lancer. We will find him." |
| Secondary met (at the end) | Okafor | "Clean crossing. Now find him, Lancer." |

Timeline check (text): the longest lines close at ≈ 9.9 (t=1) and ≈ 91.5 (the barge event line if played);
the Choir's line closes ≈ 104.8, Varga's reply opens at 110.5; the cut at t=160.3 follows Varga's
"Contacts below!" (closes ≈ 159.8); Okafor's t=165 follows it; nothing overlaps. The barge's event line is
dropped if it would start after 6 s past the destroying shot. Rook's one dry joke (t=13) is the level's;
his t=150 line is the warm one the cut line answers.

## Music & ambience

The Act 4 **darkness variant** of the A theme ([track 10](../../../audio/music/README.md#track-list)
"Thera Deep"): in section 1 the full A theme with the lamps, then it **strips back** as the lights go
out (t=8–18) to sonar pings in silence and a deep drone; the scarp adds the Sirens' harmonic layer; the Choir's
fragment ducks it; the pad thins from t=150; **out at t=160** (silence for the aftermath, Rook's loss);
the vent field's warm pad creeps in from t=168. No bubble layer (round 48 Q12 a). Ambience: the `europa-black`
bed (a heavy pressure drone, distant whale calls, no bubbles), the `europa-trench` rumble at the scarp.
New sounds this level needs: the Angler's lure hum and lunge, its ring, the Driftjelly's soft pulse, the
Nautilus crown's violet hum, the torpedo trails (a long, distant rising hiss), Rook's lamp flicker and spin-down,
the Choir's layered whisper, the headlight click on at the level start; listed in
[sfx](../../../audio/sfx/README.md) by the act's audio step (Freesound for real-world sounds only).

## Credit budget

Budget(26) = 700 × 1.07²⁵ ≈ **3,799** ([economy](../../../systems/economy/README.md#per-level-budget)). Data
in **Act 1 terms** (Driftjelly 10, Glow Angler 14, Spiral Nautilus 30, Eel Swarm 56 whole, Siren 28, the crate
100, the secondary 100), × the act factor **4.096**, bounties × the scale. At ≈ 74 enemies a minute a
**`bounty_scale` of about 0.41** puts the typical haul at **3,788 (−0.3 %)** (estimated by hand as in
Level 25); `TypicalHaulTest` confirms it in the build.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Driftjelly 118 × 10 + Glow Angler 78 × 14 + Spiral Nautilus 12 × 30 + Eel Swarm 8 × 56 + Siren 7 × 28 | 5,502 | 3,378 |
| Ground targets | 0 | 0 |
| Secret: farm barge (hidden crate) | 410 | 205 |
| Secondary: clean crossing | 410 | 205 |
| **Total** (bounty scale 0.41) | **6,322** | **3,788** |
| Budget(n) = the typical haul's target; typical −0.3 %, perfect 1.66 × budget | | 3,799 |

**Balance plan**: the plan's L26 visit saves for the Mk IV (see the
[Act 4 hand plan](../../../player/generator/README.md#act-4-hand-plan-draft)); the level expects `homing` and
`area` and the Sonar Pulse, and is flyable without them.

## Difficulty notes

- **Easy**: the Angler's trigger 100 px and a ring of 6; the Nautilus with 2 arms; the Siren's slow 40 % for
  1.5 s; the formation lever thins the fields; the ambient assist is the settings' lever, not the difficulty's.
- **Hard**: the Angler's trigger 160 px and a ring of 10; the Nautilus with 4 arms; the Siren ring every 3.6 s;
  the fields larger. The shoot-down is the same on every difficulty.

## Concept art

None yet. The level needs (Act 4's production-art round; the darkness look's a/b stills come from the Europa
step's round 48 proposals):

- the Blackwater look in the game (the glow-only pass, the headlight cone, marine snow, Rook's lamp);
- the Glow Angler with its stalk and lure, the scarp's cave mouths with their sacs, the farm barge with its
  lamp, the survey buoy; the Driftjelly's veined under-water look (a re-tint);
- Rook's loss: the torpedo trails, the lamp's spin-down, the vent glow rising;
- the two briefing images; the intel portraits;
- the round's captures: the level at medium, the lure moment, the constellation with and without the Sonar
  Pulse, the whole shoot-down, readability crops at 1:1.

## Implementation

- [ ] Level data `data.yaml` (when built in part A): sections and speeds with eases, the ambient ramp, the
      scarp terrain, waves, radio, budget, threat profile, briefing, music
- [ ] Darkness look (Q4 a): the glow-only pass for units outside the light, the cone, light pools, the ambient
      ramp, bullets, pickups, Rook's lamp always visible; nothing in the simulation changes
- [ ] The Glow Angler's lure and lunge (130 px or touch), the Driftjelly and Eel Swarm in the dark
- [ ] The fault scarp (one wall, three notches, the Sirens' glow tell)
- [ ] The Choir's fragment with the HUD static 2 s before, the music ducking (Q8 a)
- [ ] **Rook's loss**: the scripted trails (visual only), the lamp spin-down, the cut line, `rook_missing`
      saved at the win, the replay on a retry, radio only with `--escort none` and when ejected
- [ ] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), the quiet windows, edge warnings
- [ ] Kill-ratio secondary at 65 %, judged at the level end
- [ ] Radio voiced, every timed line at most 1 s late (with Rook and without), the Choir's line subtitled
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel
- [ ] Briefing: both pages, each one screen, with their images
- [ ] Typical haul within ±5 % of budget(26) = 3,799 with the level's `bounty_scale`
- [ ] Music: the darkness variant by section; the `europa-black` ambience (no bubbles)
- [ ] Easy/hard variations; the autopilot flies the level on every difficulty (with and without Rook)
- [ ] Production art and captures for the round

## Open questions

- **A Siren needs a wall**: the Siren page limits its nests to trench walls; the scarp is this level's way
  to use it in the dark (drafted); the alternative is no Sirens in Level 26.
- **Is the Eel Swarm's whole kill worth 56** here, or do the dark's snakes pay by head only? Part A's measure.
- **Is a retry after the loss shown again** (drafted: yes, the full beat each attempt)?

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
