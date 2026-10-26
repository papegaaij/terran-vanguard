---
title: Level 28 – Abyssal Maw
design: approved
implementation: in-progress
art: final
depends-on: [../../../enemies/bosses/abyssal-maw, ../../../enemies/naval/depth-hunter, ../../../enemies/naval/glassfin, ../../../enemies/naval/barbfin, ../../../enemies/naval/eel-swarm, ../../../enemies/naval/abyss-ray, ../../../enemies/naval/siren, ../../../enemies/naval/spiral-nautilus, ../../../enemies/ground/spine-turret, ../../../enemies/ground/polyp-mortar, ../../../world/europa, ../../../player/wingmen, ../../../story/twist, ../../../story/characters/the-choir, ../../../systems/economy]
updated: 2026-10-26
---

# Level 28 – Abyssal Maw

## Summary

The Act 4 finale and the story's turn. Aegis Wing swims through the hatchery gate into the Vrell
nursery built round Europa's deepest vent: galleries of egg racks, a spawning cavern, an antechamber
where the vent's glow rises and the screen clears while the Choir sings. There the act boss waits, coiled
round the vent: the [Abyssal Maw](../../../enemies/bosses/abyssal-maw/README.md), a serpent leviathan
whose body frames the arena, who dives once into the dark and, in its last phase, bleeds grey
human machinery: Helix implants. About 104 s of approach, the fight on a halted arena (≈ 130 s at
medium), then **the reveal** (Q10 a): the wing climbs the drill shaft, **breaks through the ice** (the
medium switches back to air), and two **Ascendancy cruisers in black and gold** rise through the ice
beside the Vrell fleet and open fire: a ≈ 35 s survive section, then the act end. No Rook (still missing,
radio silent; a Bulwark flies his slot).

**Status: approved in round 48 (2026-10-22); drafted (Act 4 design phase, step D-L27-28, 2026-10-21), decided in round 48 (2026-10-22).**
Written from the act roster, the gap analysis's recommended answers (Q1 b, Q7 a, Q9 b, Q10 a, Q11 a; all
pending in round 48) and the wave 1 documents; the boss spec owns the fight and the cruisers, this
document owns the approach, the timeline, the radio and the act end. **In data since M8 part H (step H3, 2026-10-25)**:
the level's numbers live in [data.yaml](data.yaml) and the briefing, teaser, threat profile, sections, waves, ground
targets, radio and credit budget tables below are rendered from it (`tools/sync_tables.py`), with the act's outro and
`next_act` in the [act's data](../data.yaml); step H3's placements and the gap check's defaults (`m8h-gaps.md` §5) are
accepted in round 58 (2026-10-26).

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image:

<!-- data: briefing -->
> **Commander Okafor:** "Everything the Vrell sent at Europa was grown in one place: a hatchery
> round the deepest vent. End it, Lancer, and the ice is ours again. Rook is still out there.
> We'll find him."
>
> **Dr. Varga:** "A guardian lies coiled round the vent. Shoot its gill sacs, then the lime in
> its throat when the jaw opens. The grey hulls at the gate carry the code from the wreck.
> Helix, Lancer."
<!-- /data -->

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`):

- Okafor's page `level-28-hatchery`: a sonar chart of the hatchery: the gate, the galleries of egg racks,
  the spawning cavern, the antechamber and the caldera round the vent with a ring of faint lights under
  it, Level 23's drill shaft marked as the way out; teal Vrell vectors and grey, unmarked contacts guarding
  the gate.
- Varga's page `level-28-hull-code`: her scan of the Level 27 wreck's hull: the code under the burnt paint
  (`HDX-417`, Level 27's wreck's, accepted in round 57) with the Helix Dynamics emblem beside it and an
  intercepted beacon frequency trace matching it; in a corner the guardian's silhouette coiled round a vent.

*Hangar teaser* (shop screen before L28; Okafor, not voiced):

<!-- data: teaser -->
> **Commander Okafor:** "The hatchery is next, Lancer. Something large guards it. Bring
> everything you have."
<!-- /data -->

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule): the three Hunter
columns (the t=16 guard column too, gap check §5 H3) and the rear ambush enter `from: rear`, the pincers and the side
schools `from: sides`, the Nautilus pairs `circle` from the front as Level 27's (the draft's 68 / 25 / 7 %); in the
fight the coil, the lunge and the shed Eel Swarms come from the whole arena and are not in the numbers.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (the play plane under water: Glassfin schools, Barbfin groups, Depth Hunters, Eel Swarms, Nautilus pairs, Sirens, Abyss Rays, the Maw), `sub` (the Rays' shadows, the Hunters' rise, the Maw's dive), `ground` (egg racks, Spine Turret nests, Polyp Mortars, the vent), `low-air` (the cruisers of the reveal, scenery) |
| Attack directions | front 69% · sides 22% · rear 9% (the approach by its waves; in the fight the coil frames all four edges, the lunge and the shed Eel Swarms come from the whole arena and are not in the numbers) |
| Density | 4 |
| Recommended traits | `anti-sub`, `homing` (`side` guns help too: three traits do not fit the intel panel, as Levels 13 and 14) |
| Hazards | The Maw's coil sweeps, fan walls and lunge (the arena); the cruisers' broadsides and flak (the run); the water's rules ([europa](../../../world/europa/README.md#under-water-rules)) until the breakout |
| Boss / mid-boss | Act boss [Abyssal Maw](../../../enemies/bosses/abyssal-maw/README.md) |
| Sensor-suite detail | **none**: Europa, the hatchery, `air` + `ground`, all directions. **L1**: + the directions, density 4, hazard "coiled guardian", the objective `DESTROY THE ABYSSAL MAW`. **L2**: + Depth Hunter, Eel Swarm, Abyss Ray, Siren, Spiral Nautilus, Barbfin, Glassfin portraits; the boss's name and silhouette (`abyssal-maw`). **L3**: + `anti-sub` and `homing` highlighted; the timeline strip marks the Choir's song (97.5 s) and the boss (104 s); 1 secret. The finale after the boss is **not** on the strip (the reveal is a surprise). |
<!-- /data -->

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "The hatchery, Lancer. Everything on Europa was grown here." |
| L1 | "From every side. Something huge coiled round the vent." |
| L2 | "Rays, hulls, singers. And one huge contact: the guardian." |
| L3 | "Anti-sub for its dive. Homing and side guns for the machinery in its spine." |

## Objective

- **Primary** `reach-end` with the arena as the condition, as Levels 14 and 21: the level ends when the
  Abyssal Maw is destroyed and **the breakout and the run** have been flown. The briefing and the objective
  line read `DESTROY THE ABYSSAL MAW` (the boss's name on the bar). It is not a `boss` primary.
- **Secondary** "Clear the hatchery" (`kill_ratio`, §1.7): destroy at least **65 %** of the level's
  approach enemies (**90 of 137** at medium), judged at the level's end. The Maw, the Eel Swarms it
  sheds, the Hunters of the finale and the cruisers are not counted (`boss_counted: false`). Pays **100**
  credits in Act 1 terms (410 in Act 4); the typical haul counts it at 50 %. It has **no radio line**.

## Layout

Scroll **100–120 px/s** in the hatchery (slow for the water's drag, as Level 27), then the arena
**halts** (the boss spec: nothing slides under the coil), then **150–200 px/s** for the climb and the
run. Setting: [Europa](../../../world/europa/README.md)'s hatchery, medium `underwater` until the breakout
line (t=115), then `air`. **Light** (Q2 a): the hatchery's own bioluminescence (teal and violet racks), the
vent's red-amber rising through the antechamber, the arena lit lime-white from the throat; after the
breakout the night ice under Jupiter. Motion budget: the Eel Swarms and Rays are the strong elements in the
approach; in the arena the coil, the walls of light and the lunge (the boss's, on real seconds); in the
finale the ice and the cruisers. Pixel columns are speed × time (the eases move them by under 100 px).

**The arena and the clock.** The arena section starts at **t=104** with the boss warning (track 22), the
long bar `ABYSSAL MAW`, the Vrell boss theme after it, the arrival and the **boss checkpoint** in one
moment (none on hard; the Choir has sung at t=97.5). The scroll is **0**: a one-second section
`speed: 0, arena: true` as Level 19's yard; the level clock halts at its end while the Maw lives, and the
kill resumes it after the Maw's 4 s death chain and `hold_after_boss: 1` (the flash and the credit shower
over the vent, collectable, and one second more; the hold counts after the death chain). **K** below is the kill: the clock reads **t=100 + K** from the
moment it resumes (K+5 = t=105).

**Rook and the escort.** Rook is missing; the escort slot flies the **Bulwark** (see Level 27). It follows
Rook's boss rules ([the boss spec](../../../enemies/bosses/abyssal-maw/README.md#the-escort-the-bulwark)):
out of a sweep's bight and the lunge's column, against the coil, the sacs first, then an open implant. In
the run it keeps off the broadside bands. With `--escort none` the level is the same without it. **No Rook
lines** (dropped).

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Hatchery Gate | 0–22 | 0–2,640 | 120 | clear | `ground`: the ring of chitin arches (the end of Level 27's view), then a gallery mouth lined with egg racks, glowing lamps of grown flesh. `low-air`: drifting spores and silt. `sub`: the dark below. | Teal bioluminescence. The launch; the Hunters guarding the gate (t=16): the grey hulls beside the Vrell. |
| 2. Brood Galleries | 22–50 | 2,640–5,730 | 110 | clear | `ground`: long galleries of egg racks (scenery, rows of pale pods), Spine Turret nests on the racks' ends (t≈30), the Sirens grown on the gallery walls (t≈36). `air`: the racks' glow. | Teal and violet racks. Nautilus pairs round the racks; the Sirens on the gallery wall; the Ray; a rear Hunter column. |
| 3. Spawning Cavern | 50–80 | 5,730–9,030 | 110 | clear | `ground`: a cavern floor of hatching pools and resin ridges, the **UTC recon sub *Pelagic-3*** wedged under a collapsed rack at the cavern's mouth (t≈58, the secret), a Spine Turret nest on a resin ridge (t≈66), Polyp Mortars on the far ridge (t≈74), Sirens on the cavern's last stack (t≈76). `sub`: the vent's red glow rising from the floor. | Warm amber from below. The Eel Swarms, the second Ray, the last rear ambush; the floor warms up. |
| 4. Antechamber | 80–104 | 9,030–11,440 | 100 | clear | `ground`: the vent's caldera rim, steaming fissures, a last pair of Polyp Mortars (t≈92). No wave after t=93.5: the screen clears while the Choir sings (t=97.5). | Red-amber, the vent's glow. The last Hunter column (t=86), the pre-boss pause (the level's third gap over 2 s); the Choir; the warning at t=104. |
| 5. The Vent | 104–105 | 11,440–11,440 | 0 | clear | `ground`: the hatchery caldera filling the field, the vent's throat at (240, 330) ([the boss's arena](../../../enemies/bosses/abyssal-maw/README.md#arena)). `sub`: the coil winding in round the arena from off the screen. | The vent's lime-white, the bioluminescent frame. The boss fight (≈ 130 s at medium); the scroll halted and the level clock stopped while the Maw lives. |
| 6. The Shaft | 105–115 | 11,440–12,790 | 150 | clear | `ground`: the Vrell drill shaft's walls (Level 23's, run in reverse: they push without damage) closing on both sides, the caldera falling away below. `high-air`: the ice ceiling cracking ahead. | Light growing from above. The climb after the kill (K+5 to K+15); no enemies; the radio's first beat of the finale. |
| 7. The Breakout | 115–118 | 12,790–13,240 | 150 | clear | `ground`: the ice ceiling shatters round the ship as it breaches the shaft mouth's lip (near t=115, K+15; wherever the ship is: high or at a flank earlier, low later), the burst anchored on the ice where it broke through: slabs tumble out of the field, the under-water grade lifts over 1 s; Europa's night ice under Jupiter. `high-air`: the Vrell fleet's carriers rising from other drill holes. | A white burst, then Jupiter-lit ice. Europa's signature set piece; **the medium switches to air**. |
| 8. The Rise | 118–122 | 13,240–13,840 | 150 | clear | `ground`: Europa's night ice (Level 22's backdrop), intact where the cruisers break through: on both flanks the crust lifts in a hull's shape and splits, each hull moving with the ice at first and slowing to the wing's pace in 2 s, its hole left behind in the ice. `low-air`: two **Ascendancy cruisers rise** beside the Vrell carriers, growing from dim to full scale (4 s); not hittable, no contact. | Night ice under Jupiter, amber gun ports. The reveal (K+18); the reveal sting; Varga's and Okafor's lines. |
| 9. The Run | 122–157 | 13,840–20,790 | 200 | clear | `low-air`: the cruisers keep pace on both flanks and fire: broadsides, flak, point defence. The Vrell and the cruisers never fire at each other. | Night ice; amber broadsides. The ≈ 35 s survive section (K+22 to K+57); no fighters. |
| 10. Orbit | 157–162 | 20,790–22,490 | 400 | clear | `low-air`: the cruisers fall off the bottom edge. | Fading to white as the wing makes orbit. The level ends at K+62; the debrief follows. |
<!-- /data -->

The finale's timeline is the boss spec's *The reveal* table (times from the kill K), with one change the
radio forces (see *Radio chatter*): Okafor's "up the shaft" waits behind the Choir's fragment, at ≈ K+8.6 instead of
K+5 (the climb is silent until then). The level ends at **K+62**, five seconds after the run's last shot
(the spec's "≈ 57 s" is where the cruisers fall behind); the section after the kill is the level's
**outro for the pacing checks** (no enemies by design: `PacingTest` and `ActFloorTest` measure the approach,
up to the boss's death; the build must say so).

**The run cannot be lost** (the boss spec's draft rule, pending the user's review): hits cost shield and
armour as usual, but the armour stops at 1 until the level ends; the alternative is a second checkpoint at
the breakout (easy and medium, a new checkpoint kind). **The breakout** needs the level's medium to switch
from water to air at a line: the water factors ease out over 1 s, the grade lifts, the special limits end
(the Airstrike fires again; the Sonar Pulse reads `NO WATER`); the climb uses Level 23's shaft pieces
(`Terrain`) laid in reverse.

## Waves

Formation names from the [formation vocabulary](../../../enemies/README.md#formation-vocabulary). The
**act HP factor** (the returning `medium`+ units, the proposed reference DPS **120** at Level 28 against
the unit's first level): Eel Swarm head 100 → 120 (segments 10 → 12, tail 25 → 30), Abyss Ray 210 → 242, Siren
90 → 100, Spiral Nautilus 100 → 111, Depth Hunter 120 → 124 (its first level is 27: ×120/116); the `small`
units keep the beam era's ×2 (Barbfin 26 → 52 since the underwater weapons rework, step AW2; the draft's 24 was
stale) and the `tiny` Glassfin its 1 HP. The Eel Swarms the **Maw** sheds
carry the same factor. Measured with `levelReport` in step H3 (89 a minute at medium, the Act 2+ floor met).

- **Density and pacing** (§1.7): **137 enemies at medium** over the 87.5 s from t=6 to t=93.5 (**≈ 91 a
  minute**), written to the Act 2+ floor from the start (a wave about every 2.5 s, Barbfin groups for the
  shots, Glassfin for the popcorn). After t=93.5 **no wave enters**: the last Eel Swarm (t=91) is the last
  large unit, so the screen clears while the Choir sings (t=97.5), as Level 21's H.3, and the warning at t=104 shows
  no incoming box. The antechamber's pre-boss pause (t≈94–104) is the level's one stretch over 2 s of nothing
  at the floor (the third allowed gap), as Level 21's; the pacing checks must name it.
- **The hatchery guards** (t=16, 46 and 86, three Hunters, three, four): the grey hulls of Level 27 fly
  beside the Vrell, all three as rear columns with BOTTOM warnings 3 s ahead (the first too: every Hunter column
  rises from the rear, gap check §5 H3; the draft had it at the gate's flanks). No
  hull-code line (hint 3 is paid; Varga's t=13 line is the question). A Hunter column and a Siren are never in the
  same stretch (Q5): the Sirens come at t=36, 76 and the columns at 46 and 86.
- **Directions** ("all", the roster): pincers and school entries from the sides with edge warnings 3 s
  ahead, two rear waves (the t=66 ambush and the Hunter columns) with BOTTOM warnings for their first
  entries; front 69 % (≥ 60 %). The boss never warns (the user's rule of 2026-10-21).
- **The returning units** are the act's cast in a nursery: Eel Swarms (three), Nautilus pairs (two),
  Sirens on the gallery and cavern walls, two Abyss Rays (the act's large set-piece unit has no mid-boss:
  twice here); the Glow Angler is not used (the hatchery is lit).

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | Popcorn through the hatchery gate |
| 8.5 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | |
| 11 | 1 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | A firing pair at the gate's lamps |
| 13.5 | 1 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 16 | 1 | column | [Depth Hunter](../../../enemies/naval/depth-hunter/README.md) | 3 | rear (left) | The Hunters **guard the hatchery** (BOTTOM warning at t=13): the grey hulls of Level 27 beside the Vrell; no hull-code line (hint 3 is paid) |
| 18.5 | 1 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (left) | |
| 21 | 1 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | |
| 23.5 | 2 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 26 | 2 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | Down the left; act HP factor |
| 28.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the left side; edge warning |
| 31 | 2 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | Spirals round the gallery's egg racks (the README's pincer: the pair orbits the ship); act HP factor |
| 33.5 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 36 | 2 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | front | Sirens grown on the gallery walls; act HP factor |
| 38.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | |
| 41 | 2 | carrier + escorts | [Abyss Ray](../../../enemies/naval/abyss-ray/README.md) + [Barbfin](../../../enemies/naval/barbfin/README.md) | 1 + 4 | front | The act's last Ray (rises from below): its shadow lit by the egg racks' glow; four Barbfin in `circle` peel off as it rises; act HP factor; it drops an overdrive |
| 43.5 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 46 | 2 | column | [Depth Hunter](../../../enemies/naval/depth-hunter/README.md) | 3 | rear (right) | A rear column through the gallery (BOTTOM warning at t=43); Flares or the rear guns |
| 48.5 | 2 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (right) | |
| 51 | 3 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | |
| 53.5 | 3 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 56 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | Down the right |
| 58.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the right side; edge warning |
| 61 | 3 | circle | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | front | In the spawning cavern; not on easy |
| 63.5 | 3 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 66 | 3 | rear ambush | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | rear | Rear ambush, BOTTOM warning at t=63 |
| 68.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | |
| 71 | 3 | carrier + escorts | [Abyss Ray](../../../enemies/naval/abyss-ray/README.md) + [Barbfin](../../../enemies/naval/barbfin/README.md) | 1 + 4 | front | A second Ray in the cavern (rises from below; the roster's large set-piece unit, twice here); it drops a shield cell |
| 73.5 | 3 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | |
| 76 | 3 | turret nest | [Siren](../../../enemies/naval/siren/README.md) | 2 | front | On the cavern's last stack; hard's third on its left (gap check §5 H4) |
| 78.5 | 3 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (left) | |
| 81 | 4 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front | |
| 83.5 | 4 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 86 | 4 | column | [Depth Hunter](../../../enemies/naval/depth-hunter/README.md) | 4 | rear | The last grey column before the vent (BOTTOM warning at t=83) |
| 88.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the left side; edge warning |
| 91 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front | The last Eel Swarm, down the left |
| 93.5 | 4 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | The last wave: no wave after it, so the screen clears while the Choir sings (t=97.5) |
| 104 | 5 | boss | [Abyssal Maw](../../../enemies/bosses/abyssal-maw/README.md) | 1 | the arena's edges (the coil winding in from off the screen at the top right) | At the arena's start: the warning, the bar and the checkpoint; the coil, the sacs, the walls, the sweeps, the dive and the implants as in its spec; its shed Eel Swarms; no other waves in the arena |

Totals: Glassfin 74 · Barbfin 40 · Depth Hunter 10 · Eel Swarm 3 · Spiral Nautilus 4 · Siren 4 · Abyss Ray 2 · Abyssal Maw 1.
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 2 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on the racks' ends (t≈30) | Bounty 12 each |
| 3 | The UTC recon sub *Pelagic-3* under a collapsed rack (`ground` trigger, 20 HP, hard 28, t≈58, x 90) | The secret: frees a crate worth 100 (410) |
| 3 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on a resin ridge (t≈66); hard ×5 | Bounty 12 each |
| 3 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) ×2 on the far ridge (t≈74) | Bounty 15 each |
| 4 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) ×2 on the caldera rim (t≈92) | Bounty 15 each |
<!-- /data -->

## Hazards

- **The approach**: no terrain collision (the racks and the ridges are scenery at the ship's height), no
  plumes (the vent's fissures steam as scenery); the water's rules
  ([europa](../../../world/europa/README.md#under-water-rules)).
- **In the arena**: the coil is a wall (stops the ship and the escort without damage); the sweeps, the
  fan walls, the dive and the lunge are the boss's
  ([spec](../../../enemies/bosses/abyssal-maw/README.md#phases)); the Eel Swarms it sheds.
- **In the finale**: the cruisers' **broadside, flak and point defence** (the boss spec's
  [cruiser table](../../../enemies/bosses/abyssal-maw/README.md#the-cruisers-draft)): invulnerable set
  pieces; the shaft and the open ice have no hazards of their own.
- **Special limits**: under water as Level 27 (no Airstrike or Orbital Lance; the Sonar Pulse works),
  lifted at the breakout line.

## Secrets and pickups

- **The UTC recon sub *Pelagic-3*** (the secret, t≈58, x≈90, at the cavern's mouth): a Coalition recon
  sub lost in week one, nose-down under a collapsed egg rack, her hatch sealed by Vrell resin. A `ground`
  trigger of **20 HP** (28 on hard); destroyed, her crate floats up: **100** credits in Act 1 terms (410).
  The Salvage scanner's glint shows it at any range. Its event line, Okafor: "*Pelagic-3. Our recon sub,
  eleven crew, lost in week one. Log it, Doctor. Take what's aboard, Lancer.*"
- **Overdrive**: dropped by the t=41 Abyss Ray at its death.
- **Armour patches** ×2: a Thera Deep service buoy releases one at (240, 380 y-up) at t=88, in the
  antechamber (Level 25-style); and the boss's phase 3 drops one (its spec).
- **Shield cell**: dropped by the second Ray (t=71) at its death; otherwise the normal drop table. There
  are no pickups in the finale (the run is a dodge, the credit shower of the kill is the haul).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), draft text for the **1 s rule** (checked with
Level 15's text model as Level 21: a timed line holds the radio for the longer of its voice and its text
(5 s for the last page plus 3 s for each page before it, typed at 30 characters a second, a page of 3 × 22
characters), then 0.4 s of silence; event lines drop after 6 s). **Speakers: Okafor, Varga and the Choir.** **No Rook lines**: he is missing (dropped, not just
`requires: escort`); the Bulwark has no radio. The arena's clock halts, so the boss lines are events
(`boss-arrival`, `boss-phase`, the new `boss-dive`, queued as `PHASE` lines: they wait for a gap, stale after
20 s, dropped at the kill). **The finale's lines are queued as `CLOSING` lines in order** (Level 21's
rule: they never go stale and the debrief waits for the last); `delay` counts from the kill K, and a line
waits behind the one before it.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Everything the Vrell sent at Europa was grown in this hatchery. End it, Lancer, and we go home." |
| t=13 | Varga | "Grey hulls again, guarding the gate. Helix hulls, Commander. Why is Helix guarding a Vrell nursery?" |
| t=26 | Okafor | "I don't know, Doctor. When this is over I will ask, and I will not accept stand by." |
| t=38 | Varga | "Eggs on every rack. Thousands. This is where the Vrell on Europa came from." |
| t=63 | Varga | "The floor is warm. The vent is under us, and something large is coiled round it." |
| t=76 | Okafor | "Antechamber, then the vent. Shields up, Lancer. Everything you have." |
| t=88 | Varga | "My biosign is off the scale. It knows we're here." |
| t=97.5 | The Choir (distorted) | "[the Choir sings]" |
| Boss arrival (`boss-arrival`, the coil winding in) | Varga | "Gill sacs first, Lancer. Then the lime in its throat when the jaw opens." |
| Phase 2 (`boss-phase` Tightening) | Okafor | "Walls of light. Find the gap and keep moving." |
| The dive (the head below 50 %, `boss-dive`, queued as `PHASE`) | Varga | "It's gone under! Watch the dark. It comes back fast!" |
| Phase 3 (`boss-phase` Implants, at the first spark) | Varga | "Commander, that's not Vrell. That's machinery in its spine. Relay housings… Helix pattern." |
| Phase 3, queued after Varga's line | Okafor | "Then hit the machinery, Lancer." |
| Maw destroyed (`boss-destroyed`, the chain's start), held 3 s (`delay`): the Choir speaks, with its layering on speech | The Choir (distorted) | "…many… we were many here…" |
| After the Choir (`CLOSING`) | Okafor | "Maw's down. Aegis Wing, up the shaft. Now." |
| K+18, the cruisers' rise (`CLOSING`, `delay: 18`) | Varga | "Those aren't Vrell hulls—" |
| Then (`CLOSING`) | Okafor | "Those are not Vrell ships. God help us. Those are ours." |
| K+34, the run (`CLOSING`, `delay: 34`) | Okafor | "All Aegis units, break for orbit. Don't look back." |
| K+42, the run (`CLOSING`, `delay: 42`) | Varga | "That's the code from the wreck, painted on their bows. They aren't hiding it any more." |
<!-- /data -->

Timeline check (text model): t=1 closes ≈ 12.6, t=13 ≈ 24.7, t=26 ≈ 37.2, t=38 ≈ 48.9, t=63 ≈ 74.1, t=76 ≈ 86.7,
t=88 ≈ 95.0, the Choir's song (≈ 3.9 s of sound, subtitle 5.6 s) closes ≈ 103.5 before the warning at t=104; the
secret's event line (*Pelagic-3* in reach at t≈56–62) waits at most 6 s. The closing queue from K, in
order, with the waits: the Choir K+3.0 to ≈ K+8.6, Okafor ≈ K+8.6 to ≈ K+15.4 (the climb's last second is
K+15, the breakout), Varga K+18 to ≈ K+24.3 (the cruisers' rise), Okafor ≈ K+24.3 to ≈ K+31.5, Okafor K+34 to
≈ K+41.1, Varga K+42 to ≈ K+53.3; the level ends at K+62 and the debrief waits for the last line. The
Choir's fragment waits **3 s** (`delay: 3`, after Level 21's 4.5 s: the three implants' cracks come in the
first second and the groan after; decided in round 48 (2026-10-22)).

**Story notes.** Varga's t=13 line and Okafor's reply carry the thread from Level 27 (the hull code, "*stand
by*"): Okafor says she will not accept it, which Act 5's opening pays. The reveal's two lines are the
character documents' ([Okafor](../../../story/characters/okafor/README.md): "*Those are not Vrell ships.
God help us. Those are ours.*"); Okafor's owning her Act 3 mistake is Act 4's outro page and the Level 29
briefing, not here. The Choir's fragment is Acts 3–4's register
([the Choir](../../../story/characters/the-choir/README.md): "…many…", never the door).

## Boss / mid-boss

Act boss: [Abyssal Maw](../../../enemies/bosses/abyssal-maw/README.md) (Q9 b, the coiled guardian). Its
parts, phases, HP, attacks, the dive, the implants, the escort rules and the **finale's staging and the
cruisers** live in the spec. Arena notes for this level:

- The arena starts at t=104 with the boss warning, the bar and the checkpoint; the Choir has sung at t=97.5, so
  the arrival (≈ 6 s: the coil winding in round the arena from off the screen, the head settling, the bar filling,
  invulnerable until the head is up; round 58 F.2)
  follows the spec's intro. The scroll is halted; nothing slides under the coil.
- **Traits** (the question D-boss left open): the fight asks for `anti-sub` and homing/side fire more than
  piercing: phase 1 is the head and the sacs in the front guns' reach, phase 2 the walls and the **dive**
  (only `anti-sub` and the Smart Bomb reach the coil in the dark), phase 3 the three implants at the neck
  and at the left and right coil, where `side` guns and `homing` shots (the Micro-missile pods) do the work
  and the Ion Beam stops on the coil. `piercing` helps only against the shed Eel Swarms. The level therefore
  recommends **`anti-sub`, `homing`** (the roster's `anti-sub`, `piercing` is dropped), with `side` named
  in Varga's L3 line. Pending the user's review.
- The checkpoint is recorded at t=104 on easy and medium; none on hard (the
  [retry](../../../systems/retry/README.md) rules). Whether the breakout gets a second one is the open
  question above.
- The Airstrike does not apply (the arena is under the ice). The Smart Bomb reaches the diving coil.
- **Bounty**: the boss spec assumed a `bounty_scale` of 0.50 and 510 in Act 1 terms (24.0 %); with this
  level's measured-by-hand scale **0.74** the same 24 % is **345 in Act 1 terms** (head 297, gill sacs 12
  each), paid **1,046 = 24.0 %** of budget(28). The spec's number is recomputed with
  the same formula, as it says; it needs updating when the scale is measured.
- **Rook and the autopilot** must leave a sweep's bight, step out of the lunge's column and chain the walls'
  gaps (the spec's last lesson), then cross the run through the broadside bands' gaps, or the tests
  cannot finish the level. The run's target: ≤ ≈ 15 shield and armour lost at medium.

## Music & ambience

[Track 11](../../../audio/music/README.md#track-list), the Act 4 B theme: the hatchery's ambience alone in
section 1, the **B base from section 2**, the **full mix from section 3**, **Vorne's motif** as a faint layer
from the Hunters' first guard (t=16), out in section 4 into the antechamber's near-silence: the vent's
rumble, the Choir's song at t=97.5 (the stage sound), then the boss warning (track 22) at t=104 and the
Vrell boss theme "The Choir Descends" (track 18) through the fight, **dropping away under the dive's darkness**
(the ambience bed alone, the boss spec) and back after the lunge, fading at the kill. The finale: the death's
groan over a silent theme; the shaft's climb under the ambience alone; **the reveal sting** (a new cue, AUD:
cold low brass with Vorne's motif hidden in it) at the cruisers' rise (t=118); **the run under Vorne's motif**
(the faint layer lifted: the motif in the open for the first time, as the music plan's Act 5 opening
begins), out over the orbit. The debrief plays the `mission-complete` jingle as every level does, and the
act-complete fanfare (track 24) plays under the first outro page.

Ambience: Europa's bed under the ice, `europa-under`, to the breakout (one bed per setting, the user 2026-10-26: the
`europa-hatchery` bed and the vent's `caldera-rumble` layer left the game); the breakout's ice tear and water roar;
Europa's night-ice wind above (`europa-ice`, Level 22's bed) from the breakout. New sounds this
level needs (the act's audio step lists them; the boss's are in its
[sounds brief](../../../enemies/bosses/abyssal-maw/README.md#sounds-brief)): the egg racks' creak, the
hatchery's throb, the antechamber's rumble, the shaft's climb, the cruisers' engine thrum and the ice they
shed. Freesound only for real-world sounds (CC0/CC-BY), no bubbly or popping sounds.

## Act end

Level 28 ends Act 4 and follows Level 21's order (Level 14's, the Act 1 order):

1. The Maw's death: the chain (the implants, the unwinding, the vent's flare), the screen flash of an act
   boss, the credit shower over the vent (collectable: the scroll is halted), the Choir's fragment.
2. **The finale** (not a settling-dust section: sections 6–10): the climb, the breakout, the reveal, the run,
   the orbit; no enemies, the closing lines in order, the debrief waiting for the last.
3. The **debrief with the act summary** (`mission-complete` jingle, the tally, the summary
   `ACT IV COMPLETE · DEEP WATER`).
4. The **act outro**, four voiced pages with their images (the data and the texts are the
   [Act 4 README](../README.md)'s; they carry the story beat "Aegis Wing escapes into orbit; the Ascendancy
   has declared itself") with `act-complete` under page 1.
5. **The release's end**, moved from Level 21 to Level 28 (M8 part H): the end card `ACTS I-IV COMPLETE …
   TO BE CONTINUED / ACT V - THE BELT. COMING SOON.`, the name entry and the credits, then the main menu;
   Continue opens the hangar before Level 29, which reads `ACT V - THE BELT: COMING SOON` and cannot
   launch (Level 21's `ACT IV - DEEP WATER: COMING SOON` goes). The next act's name comes from a `next_act`
   key in Act 4's data. `--act-end` works unchanged for this level.

Vorne's broadcast opens Act 5 (the Level 29 briefing, decided 2026-09-30): it is not in the outro or the
release.

## Credit budget

Budget(28) = 700 × 1.07²⁷ ≈ **4,350** ([economy](../../../systems/economy/README.md#per-level-budget)).
Data in **Act 1 terms** (Glassfin 2, Barbfin 12, Depth Hunter 30, Eel Swarm 48 whole, Spiral Nautilus 30, Siren 28,
Abyss Ray 60, Spine Turret 12, Polyp Mortar 15, the boss 345, the crate 100, the secondary 100), × the act factor
**4.096** (1.6³), bounties also × the level's scale. The hand estimate's **`bounty_scale` of 0.74** puts the
typical haul on the budget (step H3's `levelReport`: +0.0 %, the perfect run at 1.53 ×; the economy's shares: 60 % of
the air kills, 80 % of the ground, half the secret and the secondary, the boss at 100 %); `TypicalHaulTest` confirms it
in the build (step H4). The boss keeps the economy's act-boss share of
**24 %** (the 30 % capped by the perfect-run ≥ 1.4 × rule, Level 21's 24.8 %): **345 in Act 1 terms**, not the spec's
510 at an assumed 0.50.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Glassfin 74 × 2 + Barbfin 40 × 12 + Depth Hunter 10 × 30 + Eel Swarm 3 × 60 by parts (48 whole) + Spiral Nautilus 4 × 30 + Siren 4 × 28 + Abyss Ray 2 × 60 | 4,405 | 2,580 |
| Ground targets: Spine Turret 6 × 12 + Polyp Mortar 4 × 15 | 396 | 317 |
| Boss: Abyssal Maw (head 297 + sacs 4 × 12) | 1,044 | 1,044 |
| Secret: Pelagic-3 (hidden crate, 2% of budget) | 410 | 205 |
| Secondary: clear the hatchery | 410 | 205 |
| **Total** (bounty scale 0.74) | **6,665** | **4,351** |
| Budget(n) = the typical haul's target; typical +0 %, perfect 1.53 × budget | | 4,350 |
<!-- /data -->

The scale is above M12's 0.3–0.5 for the same reason as Level 27: 54 % of the enemies are 2-credit
Glassfin. The cruisers pay nothing.

**Balance plan**: the plan's L28 visit (the Mk IV "Tokamak" with the nanites, the armour and shield tiers of
part A; the [Act 4 hand plan](../../../player/generator/README.md#act-4-hand-plan-draft)): single-target
DPS 153 effective, the proposed reference **120** (the boss spec's HP and window use it). The level expects
`anti-sub` and wing or side guns with `homing` for the implants and is flyable with front guns and
`anti-sub` alone, at a longer fight; the fight's measurement uses the plan's Level 28 fit.

## Difficulty notes

- **Easy**: the formation lever thins the schools; the Barbfin shot every 3 s; the Hunters' torpedo every 4.5 s;
  no Nautilus pair at t=61; the boss ×0.75 and its easy hooks (rings of 8, walls of 13 with two gaps, the sweep's
  tell 1.5 s, the lunge's shadow 2.4 s, implants open 5 s, no implant bursts); the cruisers' easy hooks (the tell
  1.5 s, one flak marker, no double bands); the boss checkpoint.
- **Hard**: schools larger and 40 % of a school dives; Barbfin 2-needle bursts every 2.1 s; the Hunters'
  torpedo every 2.8 s at 85°/s; a third Siren on the antechamber's rim; *Pelagic-3*'s trigger 28 HP; the boss ×1.3
  and its hard hooks (rings of 12, walls of 21, the sweep's tell 0.9 s, the lunge's shadow 1.6 s, implants open
  3 s, Eel Swarms of 10); the cruisers' hard hooks (the tell 0.9 s, flak every 3 s, bursts of 4). There is no boss
  checkpoint on hard.

## Concept art

Production art of step ART-H2 (M8 part H, round 58), prompts and briefs in [concept/prompts.md](concept/prompts.md);
the briefing and outro images are listed in the [briefing](../../../ui/briefing/README.md#concept-art) table, the Maw and
the cruisers in the [bosses'](../../../enemies/bosses/README.md#concept-art) (step ART-H1).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r58-a.png](concept/backdrop-final-r58-a.png) | Production backdrop (`tools/art/backdrop_l28.py`, from the act's production backdrops): the gate's ring of arches from inside, the flesh lamps, the gallery's mouth and the egg racks (teal and violet pods; three racks' ends carrying the t≈30 nests), the gallery walls' Siren bulges, the fallen rack over *Pelagic-3*'s scour, the resin ridge, the far ridge and the cavern's last stack, the steaming fissures, the caldera filling the halted arena (the throat's lime-white core at (240, 330)), the shaft (Level 23's walls and floor in reverse), the bore's broken lip on the breakout line, the ceiling's fractures, the night ice under Jupiter with other drill holes, a Vrell carrier and the cruisers' leads; sixteen screens under the hatchery light's stand-in (sheet) | chosen (round 58: accepted as final) |
| [concept/backdrop-final-r58-a.gif](concept/backdrop-final-r58-a.gif) | t 106–124 at half size: the climb out of the caldera, the shaft, the breakout, the rise | chosen (round 58: accepted as final) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block, the sections, the shaft's terrain, the fixed rows, the ground places, the per-section `underwater_look` and the static `lights:` the art is drawn for (adopted into [data.yaml](data.yaml) in step H3) | chosen (round 58: accepted as final) |
| [concept/l28-props-final-r58-a.png](concept/l28-props-final-r58-a.png) | *Pelagic-3* (`tools/art/l28_props.py`: the UTC recon sub under the fallen rack's bones, her hatch sealed in Vrell resin; sealed, hit, opened; her beacon and the resin's teal), the service buoy (Level 25's model in Thera Deep's orange), the breakout's ice slabs (four, 8 tumble frames each) and the ceiling's burst (sheet) | chosen (round 58: accepted as final) |
| [concept/l28-props-final-r58-a.gif](concept/l28-props-final-r58-a.gif) | *Pelagic-3* shot open and her crate rising, the buoy releasing its patch, the breakout's burst and the slabs flying out over the night ice | chosen (round 58: accepted as final) |
| [concept/approach-capture-r58-a.png](concept/approach-capture-r58-a.png) | Game capture (step H7; Xvfb, `--bench`, medium, `--invulnerable`, auto-fire, the ship unsteered, the balance plan's Level 28 fit, the Bulwark): the approach at t≈15 (the BOTTOM warning), 19 (the guard column of Depth Hunters rising beside the gate), 37 and 39 (the Sirens' ring off the gallery walls among the egg racks), the play field at 1:1 | chosen (round 58: accepted as final) |
| [concept/choir-capture-r58-a.png](concept/choir-capture-r58-a.png) | The Choir's song at t≈100 (the screen cleared, "[the Choir sings]" on the radio panel, the HUD at 960×540) and the hangar intel's threat strip with the Choir's lime mark (a playthrough save at Level 28) | chosen (round 58: accepted as final) |
| [concept/breakout-capture-r58-a.png](concept/breakout-capture-r58-a.png) | The breakout at t≈111.7 (the ceiling coming in over the shaft), 114.2, 114.9 (the burst round the ship), 115.7 and 117.2 (the slabs over the night ice), 1:1 | chosen (round 58: accepted as final) |
| [concept/breakout-capture-r58-a.mp4](concept/breakout-capture-r58-a.mp4) | The same run t≈107–140 with the game's sound: the shaft's climb, the breakout, the cruisers' rise with the reveal sting, Varga's and Okafor's lines, the run's first broadsides (the a stand-ins) | chosen (round 58: accepted as final) |
| [concept/orbit-capture-r58-a.png](concept/orbit-capture-r58-a.png) | The run's end and the orbit at t≈155.7, 157.7, 159.7 and 161.7: the cruisers falling off the bottom edge, the fade to white, 1:1 | chosen (round 58: accepted as final) |
| [concept/no-escort-capture-r58-a.png](concept/no-escort-capture-r58-a.png) | A run with `--escort none`: the fight without the Bulwark at its 14.7 and 24.7 s (phase 1's rings and fans), 1:1 | chosen (round 58: accepted as final) |
| [concept/pelagic-capture-r58-a.png](concept/pelagic-capture-r58-a.png) | A run steered under *Pelagic-3* (x 90): the sub hit under the fallen rack as she enters at the top edge (t≈58.2), opened (58.8), her crate rising (60.1), 1:1 | chosen (round 58: accepted as final) |
| [concept/breakout-burst-capture-r59-a.png](concept/breakout-burst-capture-r59-a.png) | Game capture for round 59 R.3 (step I4; Xvfb, `--bench`, medium, `--invulnerable`, auto-fire, the balance plan's Level 28 fit, the Bulwark; four runs, the ship steered by held keys after the kill): the breakout's burst at the ship's breach for a ship at the left, in the middle, at the right and one weaving left-right and up-down, each just before the breach and 0.1, 0.3 and 0.7 s after; the lower field (y 120–540) at 1:1 | chosen (round 59: accepted as final) |
| [concept/breakout-burst-capture-r59-a.mp4](concept/breakout-burst-capture-r59-a.mp4) | The weaving run from the shaft's climb to 8 s after the breach with the game's sound: the burst where the moving ship crossed the lip, anchored on the ice as the ship flies on, the ice slabs, the medium switching to air | chosen (round 59: accepted as final) |

The Maw's and the cruisers' production art (step ART-H1) and their captures are in the
[bosses'](../../../enemies/bosses/README.md#concept-art) table, the Maw's intel silhouette and the intel capture in the
[hangar's](../../../ui/hangar/README.md#concept-art), the briefing capture in the
[briefing's](../../../ui/briefing/README.md#concept-art) and the act's end in the [act's](../README.md#concept-art) (round 58).

## Implementation

- [x] Level data `data.yaml` (M8 part H) with the sections, waves, ground targets, radio, budget, threat
      profile, briefing, backdrop and music; the arena a one-second halted section with `hold_after_boss: 1`
      (after the 4 s death chain: K+5)
- [x] Reference DPS 120 at L28 and the Act 4 balance plan's Level 28 fit (part A) before the data; the act HP
      factor for the returning `medium`+ units listed in *Waves*
- [x] The approach to the Act 2+ rules (`ActFloorTest`, `PacingTest`, the pre-boss pause named as the third
      gap), the edge warnings for side and rear first entries, the Hunters guarding the gate
- [x] The [boss](../../../enemies/bosses/abyssal-maw/README.md) arena and fight per its spec: the checkpoint at
      t=104 (none on hard), the bar, the warning, the Choir's song at t=97.5; Rook and the Bulwark out of the
      bights and the column (`MawArenaTest`, `MawHazardsTest`, `AbyssalMawTest`,
      `Level28Test.theCheckpointTheFloorAndTheKillRatio`, `RadioTimelineLevel28Test`; round 58's captures)
- [x] The finale: the climb through Level 23's shaft pieces in reverse, the medium switch to air at the break
      line (factors out over 1 s, grade, special limits), the ice ceiling's shatter, the cruisers' rise and
      run (invulnerable `low-air` set pieces, the spec's broadsides, flak and point defence), the orbit; the
      armour floor at 1 (or the second checkpoint), the section after the kill outside the pacing checks
      (`Level28Test.theArenaHaltsAndTheBreakoutLeavesTheWater`, `ArmourFloorTest`, `CruiserRunTest`,
      `Level28LooksTest.theBreakoutAndTheOrbit`, `PacingTest`; round 58's captures)
- [x] Primary `reach-end` with the arena and the finale as the condition, `DESTROY THE ABYSSAL MAW`;
      secondary `kill_ratio` 65 % judged at the level's end (the boss, its eels and the cruisers not counted)
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`), without Rook; the boss lines as
      `PHASE` events including the new `boss-dive`, the finale's lines as `CLOSING` with `delay` from the kill,
      the Choir's fragment with its layering
- [x] Threat profile (the finale not on the strip), Varga's four intel lines and Okafor's teaser fitting the
      intel panel (`IntelPanelLayoutTest`); the Maw's intel silhouette; the strip's Choir mark at t=97.5 (step H6:
      `IntelTest.theStripMarksTheChoirsSongOfLevel28`, `IntelPanelLayoutTest`; round 58's intel capture)
- [x] Briefing: both pages, each one screen (`BriefingLayoutTest`), with their images
- [x] Typical haul within ±5 % of budget(28) with the level's `bounty_scale`; the boss's share inside 20–40 %
      (`BalanceTest`), the bounty recomputed with the measured scale
- [x] Ground targets, the *Pelagic-3* secret (20 HP trigger, hard 28, crate 100), the overdrive, the service
      buoy's armour patch and the boss's phase-3 patch (`Level28Test.theLevelHoldsTheReadmesCount`,
      `MawArenaTest.theImplantsSparkOpenInTurnFireTheirBurstsAndCountOnTheHeadsBar`, `ContentLoaderTest`)
- [x] Music: track 11's stems by section, the warning and the boss theme (dropping under the dive), the
      reveal sting, the run under Vorne's motif, the act-complete fanfare under the outro; the ambience beds
- [x] Act end: the debrief with `ACT IV COMPLETE · DEEP WATER`, the four outro pages with `act-complete`, the
      release's end moved here from Level 21 (`ACTS I-IV COMPLETE`, `next_act`), the hangar before L29
      reading `ACT V - THE BELT: COMING SOON` (`--level 28 --act-end`)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with the Bulwark on every
      difficulty (`--escort none` too)
- [x] Production art and the round's captures (steps ART-H1, ART-H2; the captures of step H7, round 58)

## Open questions

All decided in round 48 (2026-10-22):

- **Recommended traits `anti-sub`, `homing`** (with `side` in Varga's line): the roster's `piercing` is
  dropped (it helps only against the shed Eel Swarms; the Ion Beam stops on the coil).
- **The boss's bounty** 345 in Act 1 terms (24 %) at the hand scale 0.74, not the spec's 510 at an assumed 0.50:
  both follow the same formula; the measured scale decides.
- **The run cannot be lost** (the armour floor at 1) or a second checkpoint at the breakout (also the boss
  spec's open question).
- **The Choir's fragment** at the Maw's death ("…many… we were many here…") with a 3 s delay, or the
  Choir only singing (Q8).
- **The hull code's text**: `HDX-417`, Level 27's wreck's (accepted in round 57; the draft was `HLX-DS-0417`); it
  appears in the Level 28 briefing image and on the cruisers' bows.
- **The level's end at K+62**, five seconds after the boss spec's ≈ 57 s (the orbit's acceleration), and
  Okafor's "up the shaft" at ≈ K+8.6 instead of the spec's K+5, behind the Choir.
- **A new `boss-dive` radio event** (the dive is a phase 2 sub-event: the radio needs a hook for it).

## Decisions

- 2026-10-21 (Act 4 design phase, step D-L27-28; decided in round 48 (2026-10-22)): drafted from the
  roster row, the gap analysis (§1.2, §1.7, §2.1, §2.5, §2.8–2.10, Q7 a, Q9 b, Q10 a, Q11 a), the boss's
  README (the source of truth for the fight and the reveal's staging) and the Level 19 and 21 documents as the
  models (every section, the act-end order, hand tables marked draft, no `data.yaml`).
- 2026-10-21: the approach is the act's last stretch before the boss: 137 enemies (≈ 91 a minute) to the Act 2+
  floor, the Hunters guarding the gate beside the Vrell (the alliance foreshadowed, not named), no wave after
  t=93.5, the Choir at t=97.5, the warning at t=104.
- 2026-10-21: the arena is a one-second halted section (Level 19's yard), not a crawling tile: the coil is
  anchored and the scroll halts (the boss spec); the finale is five more sections (climb, breakout, rise,
  run, orbit) on the level clock after `hold_after_boss: 5`.
- 2026-10-21: recommended traits `anti-sub`, `homing` (not the roster's `piercing`): D-boss's note that phase 3
  favours side and homing weapons; `side` in Varga's L3 line.
- 2026-10-21: the secret is the UTC recon sub *Pelagic-3* (Okafor's line; Level 25 has Conamara's survey sub,
  Level 27 the Thera Deep hauler); the secondary is the 65 % kill ratio as Level 21's.
- 2026-10-21: the Act 4 end follows Level 21's order with the finale in place of the settling dust; the
  release's end moves to Level 28 (Act 5's name from `next_act`).
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-25 (M8 part H, step H3): the level in data ([data.yaml](data.yaml)) from the approved hand tables and ART-H2's
  backdrop proposal (its sections, places, lights and the shaft's terrain adopted); the tables above rendered from it.
  The gap check's defaults (`m8h-gaps.md` §5 H2–H13) applied: the t=16 guard column from the rear (BOTTOM warning at
  t=13), hard's third Siren with the t=76 pair, no Hunters in the finale, the shed Eel Swarms out of the kill ratio,
  the arrival line on `boss-arrival`, the shaft's walls pushing without damage, the armour floor at 1 from the kill on
  every difficulty (`armour_floor`), the closing lines' delays from the kill. Step H3's own choices (accepted in round 58 (2026-10-26)):
  the Nautilus pairs `circle` from the front (Level 27's reading of the draft's pincer); the Sirens of t=36 on the
  gallery walls on every difficulty; the music's section 1 the motif layer alone (`stems: {1: motif}`, from t=16: the
  motif needs the theme playing); the cruisers' point-defence turrets at 100, 270 and 440 px below the top edge, their
  flak's scatter 80 px and its direct hit `medium`; *Pelagic-3* a 64 px target (the schema has no separate hit box:
  ART-H2's proposed 44×36 is not used); Varga's cut-off "Those aren't Vrell hulls—" with the em dash (the voice's
  cut-off rule); the ambience Level 27's `europa-vents` as a stand-in until step H6 wires AUD-H's `europa-hatchery`
  bed. The hand `bounty_scale` 0.74 measured on the budget (+0.0 %); the stale Barbfin HP (24 → 52) fixed.
- 2026-10-25 (M8 part H, step H4): the tests (`Level28Test`, `AbyssalMawTest`) and the autopilot: the approach flown as
  Level 27's vent field without the plumes (`VentPlay`; beside a unit rising at it from below rather than on its lane),
  the arena by `MawPlay` (every move planned among the bullets, the bights, the lunge's column and the coil, through the
  fan walls' gaps; the sacs, the throat while the jaw is open, the open implant), the run by `RunPlay` (off the told
  bands and the flak). With the plan's fit (Level 27's and the Shield Mk III) it wins on every difficulty and seed with
  and without the Bulwark, no retry; the Maw 126.5 s at medium (par 127, the HP kept), the run's losses ≈ 8 at medium.
  The finale after the kill is left out of the pacing measure (from the armour floor on), as *Implementation* asks.
  Accepted in round 58 (C.1, C.8): the beam era's small-unit time 1.52× Level 10's (the rule's 1.5; `BeamEraHpTest` allows 1.6 here:
  the Barbfins holding above the Hunter columns and the Rays, served first); Act 4 on hard ends 49 % under the
  expert's bound (Act 3's carried deficit; `ActPlaythroughTest`); the Maw's phase 1 for a random dodger (its spec).
- 2026-10-26 (step H6, accepted in round 58 (2026-10-26)): `hold_after_boss` 5 → 1: the clock adds the hold to the Maw's 4 s death
  chain (Level 19's rule), so 5 held it to K+9 and the cruisers rose at K+22, after Varga's K+18 line; now K+5 =
  t=105 as this document times it. The sounds: round 58's a variants as stand-ins (the beds `europa-hatchery` and
  `caldera-rumble`, the latter an ambience layer from t=80 swelling over 10 s, held under the fight, cut at the kill),
  track 18 as built (Q.1 a) dropping away over the dive's 2 s slide and back over 2 s after the lunge (the game's
  hook on the coil's dive states, no data key), the reveal sting (track 30) once at the cruisers' rise (their
  `CRUISER_RISE`), the run under track 11's motif stem alone from t=126 (in over 3 s under the sting's tail) to the
  level's end (out over the orbit's 5 s): the music's new `motif_after_boss` key. Section 1's motif-alone stems now
  play the motif from its cue (t=16), silent before it. The strip's Choir mark is the threat profile's new `strip:
  [choir-song]` (only this level asks for it). The arrival and the dive's lines queue as `PHASE` lines like the
  phases'.
- 2026-10-26 (step H7): checked and captured for [round 58](../../../concept-rounds/round-58/README.md) (the full `check`,
  `levelReport -Plevel=28` on every difficulty: medium 147 enemies, 89.1 a minute, no gap over 2 s, the haul +0.0 %,
  the Maw 107 s for the autopilot; the captures in *Concept art* and the bosses', hangar's, briefing's and act's
  tables). Level 28's atlases pack into 7 pages, one over the budget: `AtlasBudget` allows it (round 58 C.6). The
  part's small rules, **accepted in round 58 (2026-10-26)** (the round asks only C.1–C.8 and Q.1 of them): the gap
  check's defaults (`m8h-gaps.md` §5 H1–H13: the plan's Shield Mk III with the Depth Charges kept, the Barbfin's 52 HP,
  the guard column from the rear, hard's third Siren at t=76, no Hunters in the finale, the Sonar Pulse opening the
  diving coil, the shed swarms out of the kill ratio, the `boss-arrival` event, the shaft's walls without damage, the
  coil as the arena's own clamp, the presentation-only finale pieces, the floor on every difficulty from the kill, the
  `CLOSING` delays from the kill); ART-H2's (the two invented emblems, the outro's cruisers drawn by the image
  generator, Okafor's built figure, the targets' places and the wall rows, the lime throat, the night ice as Level 22's
  shelf darkened, the buoy as Level 25's model recoloured); ART-H1's (the head's reared, dive and dead poses, the ice
  crust's floes, the effects' and the cruiser's tracer, flak and marker looks, the sacs unchanged in phase 3); AUD-H's
  (the sweep and the implant as two sounds each, the shaft's climb one 10 s sound, the cruisers' sounds unmuffled in
  the air); H3's (the Nautilus pairs' `circle`, the cruisers' turret places and flak numbers, the throat's box 40×30,
  the implants' 1 HP, *Pelagic-3* a 64 px target, Varga's cut-off with the em dash, the outro's first page's voice);
  H4's (the Maw's phase 1, the beam era's 1.52×, Act 4 on hard: C.1 and C.8); H5's (no armour-floor tell on the HUD:
  C.2; the looks' numbers, the orbit's white at 0.85, the sacs hidden in the lunge, the cruisers' shadow offset); H6's
  (`hold_after_boss` 1, the sounds' wiring, the strip's Choir mark).
- 2026-10-26 (user, [round 58](../../../concept-rounds/round-58/README.md) closed): F.1 (the backdrop), F.4 (the breakout) and F.5
  (*Pelagic-3* and the buoy) accepted as final, F.6-F.8 accepted too (intel, briefing images, the act's end; their parts'
  logs); F.2 (the Maw) and F.3 (the cruisers) **redo** in round 59 (the bosses' log). Sounds: the a picks play already;
  the b picks (`implant-zap`, `maw-death`, `cruiser-rise`) go into the game in a later step ([sfx](../../../audio/sfx/README.md#decisions)).
  Q.1 a: the Maw keeps "The Choir Descends" as built. C.1-C.8 accepted (C.1 keep phase 1, C.2 no floor tell, C.3 the
  broadside's whole band, C.4 the hull code as it is, C.5 Act V's name on the end card, C.6 seven atlas pages, C.7 Level
  26's last line, C.8 the balance exceptions), and the step logs' small rules (decided in round 58) with them.
  **Fix for the next code step (F.4, the user's note, verbatim):** "The effect is good, but the timing is off. It needs to be timed exactly when this ship breaches the surface. This is not fixed timing, because the ship can be anywhere on the screen and even move at that moment."
  The ceiling's burst must be placed and started at the moment the ship's position crosses the surface, wherever the ship
  is on the screen and even while it moves, not at a fixed time and place.
- 2026-10-26 (step I2; accepted in round 59): **F.4 fixed:** the burst (`BreakoutLooks`) and the
  breakout's sound (`MawCues`) start on the step the shaft mouth's lip reaches the ship's nose at its x (`BreachLine`:
  the bore's circle of the backdrop's `shaft_mouth`, r 160 round x 240, its apex on the breakout line), at the lip where
  the ship crossed, and the burst stays anchored there on the ice, scrolling with the ground, while the ship flies on;
  a ship high on the screen or at a flank breaches earlier than one low in the middle (up to ≈ 3 s before t=115). The
  medium still switches at t=115 for the whole field. Tested left, middle, right and moving
  (`Level28LooksTest.theBurstStartsAsTheShipBreachesTheIce`, `Level28SoundsTest`). **F.3 redone:** the cruisers rise
  through the intact night ice where they are: the backdrop's two open leads (`ice-split-left/-right`, open water before
  the hulls came up) are gone; each hull starts on the ice with its crust (the night ice itself, magnified so that at the
  rise's 0.6 it is the ground's own ice) and moves with the ground, slowing evenly to the wing's pace by half the rise
  (2 s), while the crust splits and sheets off; its hole (`ascendancy-cruiser-hole`: the footprint at 0.6, black water,
  plates, a white rim, cracks running out) stays on the ice where it broke through. The run's lanes are unchanged (the
  hulls in their running place from t=120; the rise is presentation only, `CruiserLooks`;
  `Level28LooksTest.theCruisersBreakThroughTheIceWhereTheyAre`). **Ambience:** one bed per setting (the user,
  2026-10-26): `europa-under` to the breakout, `europa-ice` above (the hatchery bed and the caldera's layer left the
  game).
- 2026-10-26 (step I4; [round 59](../../../concept-rounds/round-59/README.md) opened): the round 58 fixes captured
  (R.1 the Maw's motion, R.2 the cruisers' rise, R.3 the burst for a ship at the left, the middle, the right and
  moving) and asked with V.1 (the Act 4 listen-through), Q.1 (the beds' volume) and seven calls: the Maw's par 138
  (C.1), easy's shorter fight (C.2), the head under the play plane on its way back (C.3), the hull riding the ice 2 s
  (C.4), the breach at the ship's nose with the medium switching at t=115 for the whole field (C.5), hard's credits
  in Acts 3 and 4 (C.6), the homing diamond to Act 5 (C.7). The steps' small rules, accepted in round
  59: step I1's (the coil's 90 px arcs and the 55 px neck, the 5 s wind-in, the head's swim set at 16 headings, the
  dive's 3 s way back; the bight's tight bend kept, its segments up to ≈ 40° from their neighbours for the 0.8 s
  hold: a smaller swing or a longer bight would change the approved sweep; the tail's last three segments kept off the
  top edge at the top-right corner, where the coil reads as running off the screen in the captures); step I2's (the
  breach line the bore's circle r 160 round x 240, the burst anchored on the ice, the hole sprite at the rise's 0.6
  footprint, the leads removed from the backdrop; the Europa beds one per setting); step I3's (the weapons' stale "`sub` at 50 %" notes
  in their `layers_hit` text corrected to the under-water rules: the play plane at the water's factor, the deep `sub`
  layer only in the Sonar Pulse's window; Level 28's beam-era time 1.52×, C.8 of round 58,
  unchanged with the real income).
- 2026-10-26 (user, [round 59](../../../concept-rounds/round-59/README.md) closed): R.3 the breakout's burst **accepted**
  (it starts as the ship's nose crosses the shaft mouth's lip, wherever the ship is; the round 58 F.4 fix is closed), with
  R.1 the Maw's motion and R.2 the cruisers' rise (the Maw's and the cruisers' art `final`, the captures `chosen`). C.1
  keep (the par 138), C.2-C.4 accepted, C.5 accepted (one medium switch at t=115 for the whole field, the burst at the
  breach). The steps' small rules ("pending round 59") are accepted.
