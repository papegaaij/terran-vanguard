---
title: Level 28 – Abyssal Maw
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/bosses/abyssal-maw, ../../../enemies/naval/depth-hunter, ../../../enemies/naval/glassfin, ../../../enemies/naval/barbfin, ../../../enemies/naval/eel-swarm, ../../../enemies/naval/abyss-ray, ../../../enemies/naval/siren, ../../../enemies/naval/spiral-nautilus, ../../../enemies/ground/spine-turret, ../../../enemies/ground/polyp-mortar, ../../../world/europa, ../../../player/wingmen, ../../../story/twist, ../../../story/characters/the-choir, ../../../systems/economy]
updated: 2026-10-22
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
document owns the approach, the timeline, the radio and the act end. There is no `data.yaml` yet (it would
be loaded and flown by `check`): the tables below are hand-written drafts.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`). **Draft texts**:

> **Commander Okafor:** "Everything the Vrell sent at Europa was grown in one place: a hatchery round
> the deepest vent. End it, Lancer, and the ice is ours again. Rook is still out there. We'll find him."
>
> **Dr. Varga:** "A guardian lies coiled round the vent. Shoot its gill sacs, then the lime in its
> throat when the jaw opens. The grey hulls at the gate carry the code from the wreck. Helix, Lancer."

*Images* (production art straight away; [briefing images](../../../ui/briefing/README.md),
`tools/art/briefing_images.py`):

- Okafor's page `level-28-hatchery`: a sonar chart of the hatchery: the gate, the galleries of egg racks,
  the spawning cavern, the antechamber and the caldera round the vent with a ring of faint lights under
  it, Level 23's drill shaft marked as the way out; teal Vrell vectors and grey, unmarked contacts guarding
  the gate.
- Varga's page `level-28-hull-code`: her scan of the Level 27 wreck's hull: the code under the burnt paint
  (draft `HLX-DS-0417`, pending the user's review) with the Helix Dynamics emblem beside it and an
  intercepted beacon frequency trace matching it; in a corner the guardian's silhouette coiled round a vent.

*Hangar teaser* (shop screen before L28; Okafor, not voiced):

> **Commander Okafor:** "The hatchery is next, Lancer. Something large guards it. Bring everything you have."

## Threat profile

Draft; directions are counted by each wave's `from` (the intel's rule): 68 % front,
25 % sides, 7 % rear; in the fight the coil, the lunge and the shed Eel
Swarms come from the whole arena and are not in the numbers.

| Field | Value |
|---|---|
| Dominant layers | `air` (the play plane under water: Glassfin schools, Barbfin groups, Depth Hunters, Eel Swarms, Nautilus pairs, Sirens, Abyss Rays, the Maw), `sub` (the Rays' shadows, the Maw's dive), `ground` (egg racks, Spine Turret nests, Polyp Mortars, the vent), `low-air` (the cruisers of the reveal, scenery) |
| Attack directions | front 68% · sides 25% · rear 7% (the approach by its waves; in the fight the coil frames all four edges) |
| Density | 4 |
| Recommended traits | `anti-sub`, `homing` (`side` guns help too: three traits do not fit the intel panel, as Levels 13 and 14) |
| Hazards | The Maw's coil sweeps, fan walls and lunge (the arena); the cruisers' broadsides and flak (the run) |
| Boss / mid-boss | Act boss [Abyssal Maw](../../../enemies/bosses/abyssal-maw/README.md) |
| Sensor-suite detail | **none**: Europa, the hatchery, `air` + `ground`, all directions. **L1**: + the directions, density 4, hazard "coiled guardian", the objective `DESTROY THE ABYSSAL MAW`. **L2**: + Depth Hunter, Eel Swarm, Abyss Ray, Siren, Spiral Nautilus, Barbfin, Glassfin portraits; the boss's name and silhouette (`abyssal-maw`). **L3**: + `anti-sub` and `homing` highlighted; the timeline strip marks the Choir's song (97.5 s) and the boss (104 s); 1 secret. The finale after the boss is **not** on the strip (the reveal is a surprise). |

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
kill resumes it after `hold_after_boss: 5` (the 4 s death chain, the flash and the credit shower over the
vent, collectable, and one second more). **K** below is the kill: the clock reads **t=100 + K** from the
moment it resumes (K+5 = t=105).

**Rook and the escort.** Rook is missing; the escort slot flies the **Bulwark** (see Level 27). It follows
Rook's boss rules ([the boss spec](../../../enemies/bosses/abyssal-maw/README.md#the-escort-the-bulwark)):
out of a sweep's bight and the lunge's column, against the coil, the sacs first, then an open implant. In
the run it keeps off the broadside bands. With `--escort none` the level is the same without it. **No Rook
lines** (dropped).

| Section | t (s) | Scroll (px) | Speed (px/s) | Light | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Hatchery Gate | 0–22 | 0–2,640 | 120 | teal bioluminescence | `ground`: the ring of chitin arches (the end of Level 27's view), then a gallery mouth lined with egg racks, glowing lamps of grown flesh. `low-air`: drifting spores and silt. `sub`: the dark below. | The launch; the Hunters guarding the gate (t=16): the grey hulls beside the Vrell. |
| 2. Brood Galleries | 22–50 | 2,640–5,720 | 110 | teal and violet racks | `ground`: long galleries of egg racks (scenery, rows of pale pods), Spine Turret nests on the racks' ends (t≈30). `air`: the racks' glow. | Pincers and Nautilus pairs round the racks; the Sirens on the gallery wall; the Ray. |
| 3. Spawning Cavern | 50–80 | 5,720–9,020 | 110 | warm amber from below | `ground`: a cavern floor of hatching pools and resin ridges, the **UTC recon sub *Pelagic-3*** wedged under a collapsed rack at the cavern's mouth (t≈58, the secret), a Spine Turret nest on a resin ridge (t≈66), Polyp Mortars on the far ridge (t≈74). `sub`: the vent's red glow rising from the floor. | The Eel Swarms, the second Ray, the last rear ambush; the floor warms up. |
| 4. Antechamber | 80–104 | 9,020–11,420 | 100 | red-amber, the vent's glow | `ground`: the vent's caldera rim, steaming fissures, a last pair of Polyp Mortars (t≈92). No wave after t=93.5: the screen clears while the Choir sings (t=97.5). | The last Hunter column (t=86), the pre-boss pause; the Choir; the warning at t=104. |
| 5. The Vent (arena) | 104–105 | 11,420–11,420 | 0 | the vent's lime-white, bioluminescent frame | `ground`: the hatchery caldera filling the field, the vent's throat at (240, 330) ([the boss's arena](../../../enemies/bosses/abyssal-maw/README.md#arena)). `sub`: the coil rising from the dark. | The boss fight (≈ 130 s at medium); the scroll halted and the level clock stopped while the Maw lives. |
| 6. The Shaft | 105–115 | 11,420–12,920 | 150 | light growing from above | `ground`: the Vrell drill shaft's walls (Level 23's, run in reverse) closing on both sides, the caldera falling away below; the ice ceiling (`high-air`) cracking ahead. No enemies; a few Glassfin and eels flee upward past the ship, firing nothing. | The climb after the kill (K+5 to K+15); the radio's first beat of the finale. |
| 7. The Breakout | 115–118 | 12,920–13,370 | 150 | white burst, then Jupiter-lit ice | The ice ceiling shatters at t=115 (K+15): slabs tumble out of the field, the under-water grade lifts over 1 s, **the medium switches to air**; the Vrell fleet's carriers rise from other drill holes in `deep` and `high-air`. | Europa's signature set piece. |
| 8. The Rise | 118–122 | 13,370–13,970 | 150 | night ice under Jupiter, amber gun ports | Europa's night ice (Level 22's backdrop); the ice on both flanks splits and **two Ascendancy cruisers rise** beside the Vrell carriers, growing from dim to full scale (4 s). Not hittable, no contact (`low-air`). | The reveal (K+18); the reveal sting; Varga's and Okafor's lines. |
| 9. The Run | 122–157 | 13,970–20,970 | 200 | night ice; amber broadsides | The cruisers keep pace on both flanks and fire: broadsides, flak, point defence (see the [cruisers](../../../enemies/bosses/abyssal-maw/README.md#the-cruisers-draft)). The Vrell and the cruisers never fire at each other. No fighters (Q10 a). | The ≈ 35 s survive section (K+22 to K+57). |
| 10. Orbit | 157–162 | 20,970–21,970 | 200 | fading to white | The scroll accelerates from 200 to 400 px/s over 3 s (the px column counts 200), the cruisers fall off the bottom edge still firing, the screen fades as the wing makes orbit. | The level ends at K+62; the debrief follows. |

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
units keep the beam era's ×2 (Barbfin 24) and the `tiny` Glassfin its 1 HP. The Eel Swarms the **Maw** sheds
carry the same factor. Every number is a hand estimate for part A's `levelReport`.

- **Density and pacing** (§1.7): **137 enemies at medium** over the 87.5 s from t=6 to t=93.5 (**≈ 91 a
  minute**), written to the Act 2+ floor from the start (a wave about every 2.5 s, Barbfin groups for the
  shots, Glassfin for the popcorn). After t=93.5 **no wave enters**: the last Eel Swarm (t=91) is the last
  large unit, so the screen clears while the Choir sings (t=97.5), as Level 21's H.3, and the warning at t=104 shows
  no incoming box. The antechamber's pre-boss pause (t≈94–104) is the level's one stretch over 2 s of nothing
  at the floor (the third allowed gap), as Level 21's; the pacing checks must name it.
- **The hatchery guards** (t=16, 46 and 86, three Hunters, three, four): the grey hulls of Level 27 fly
  beside the Vrell: the first at the gate from the flanks, the others as rear columns with BOTTOM warnings. No
  hull-code line (hint 3 is paid; Varga's t=13 line is the question). A Hunter column and a Siren are never in the
  same stretch (Q5): the Sirens come at t=36, 76 and the columns at 46 and 86.
- **Directions** ("all", the roster): pincers and school entries from the sides with edge warnings 3 s
  ahead, two rear waves (the t=66 ambush and the Hunter columns) with BOTTOM warnings for their first
  entries; front 68 % (≥ 60 %). The boss never warns (the user's rule of 2026-10-21).
- **The returning units** are the act's cast in a nursery: Eel Swarms (three), Nautilus pairs (two),
  Sirens on the gallery and cavern walls, two Abyss Rays (the act's large set-piece unit has no mid-boss:
  twice here); the Glow Angler is not used (the hatchery is lit).

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 6 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front | Popcorn through the hatchery gate |
| 8.5 | 1 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front |  |
| 11 | 1 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front | A firing pair at the gate's lamps |
| 13.5 | 1 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 16 | 1 | column | [Depth Hunter](../../../enemies/naval/depth-hunter/README.md) | 3 | front (from the gate's flanks) | The Hunters **guard the hatchery**: the same grey hulls as Level 27 now fly beside the Vrell (their weapons are the Vrell's targets' own); no hull-code line (hint 3 is paid) |
| 18.5 | 1 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (alternating edges) |  |
| 21 | 1 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front |  |
| 23.5 | 2 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 26 | 2 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front (left) | 12 segments; act HP factor 1.20 |
| 28.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the sides; edge warnings |
| 31 | 2 | pincer | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | sides | 1 per side; edge warnings; spirals round the gallery's egg racks; act HP factor |
| 33.5 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 36 | 2 | line abreast | [Siren](../../../enemies/naval/siren/README.md) | 2 | front | Sirens on the gallery wall (terrain nests); act HP factor |
| 38.5 | 2 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front |  |
| 41 | 2 | circle | [Abyss Ray](../../../enemies/naval/abyss-ray/README.md) + [Barbfin](../../../enemies/naval/barbfin/README.md) | 1 + 4 | front (rises from below) | The act's last Ray: its shadow lit by the egg racks' glow; act HP factor |
| 43.5 | 2 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 46 | 2 | column | [Depth Hunter](../../../enemies/naval/depth-hunter/README.md) | 3 | rear (BOTTOM warning at t=43) | A rear column through the gallery; Flares or the rear guns |
| 48.5 | 2 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (alternating edges) |  |
| 51 | 3 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front |  |
| 53.5 | 3 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 56 | 3 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front (right) | 12 segments |
| 58.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the sides; edge warnings |
| 61 | 3 | pincer | [Spiral Nautilus](../../../enemies/naval/spiral-nautilus/README.md) | 2 | sides | In the spawning cavern; the Sirens' rings are on the walls |
| 63.5 | 3 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 66 | 3 | rear ambush | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | rear | BOTTOM warning at t=63 |
| 68.5 | 3 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | front |  |
| 71 | 3 | circle | [Abyss Ray](../../../enemies/naval/abyss-ray/README.md) + [Barbfin](../../../enemies/naval/barbfin/README.md) | 1 + 4 | front (rises from below) | A second Ray in the cavern (the roster's large set-piece unit, twice here) |
| 73.5 | 3 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |
| 76 | 3 | line abreast | [Siren](../../../enemies/naval/siren/README.md) | 2 | front | On the cavern's last stack |
| 78.5 | 3 | column | [Glassfin](../../../enemies/naval/glassfin/README.md) | 6 | front (alternating edges) |  |
| 81 | 4 | column | [Barbfin](../../../enemies/naval/barbfin/README.md) | 3 | front |  |
| 83.5 | 4 | pincer | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | sides | 1 per side; edge warnings |
| 86 | 4 | column | [Depth Hunter](../../../enemies/naval/depth-hunter/README.md) | 4 | rear (BOTTOM warning at t=83) | The last grey column before the vent; the boss's Barbfin and Eel Swarms are the Maw's own |
| 88.5 | 4 | swarm | [Glassfin](../../../enemies/naval/glassfin/README.md) | 8 | sides | Popcorn from the sides; edge warnings |
| 91 | 4 | snake | [Eel Swarm](../../../enemies/naval/eel-swarm/README.md) | 1 | front (left) | The last Eel Swarm; no wave after t=93.5, so the screen clears while the Choir sings (t=97.5) |
| 93.5 | 4 | line abreast | [Barbfin](../../../enemies/naval/barbfin/README.md) | 2 | front |  |

Totals: Glassfin 74 · Barbfin 40 · Eel Swarm 3 · Abyss Ray 2 · Siren 4 · Spiral Nautilus 4 · Depth Hunter 10.

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 2 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) nest ×3 on the racks' ends (t≈30) | Bounty 12 each |
| 3 | The UTC recon sub *Pelagic-3* under a collapsed rack (`ground` trigger, 20 HP, t≈58, x 90) | The secret: frees a crate worth 100 (410) |
| 3 | Spine Turret nest ×3 on a resin ridge (t≈66); hard ×5 | Bounty 12 each |
| 3 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) ×2 on the far ridge (t≈74) | Bounty 15 each |
| 4 | Polyp Mortar ×2 on the caldera rim (t≈92) | Bounty 15 each |

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

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Everything the Vrell sent at Europa was grown in this hatchery. End it, Lancer, and we go home." |
| t=13 | Varga | "Grey hulls again, guarding the gate. Helix hulls, Commander. Why is Helix guarding a Vrell nursery?" |
| t=26 | Okafor | "I don't know, Doctor. When this is over I will ask, and I will not accept stand by." |
| t=38 | Varga | "Eggs on every rack. Thousands. This is where the Vrell on Europa came from." |
| Pelagic-3 destroyed (event) | Okafor | "Pelagic-3. Our recon sub, eleven crew, lost in week one. Log it, Doctor. Take what's aboard, Lancer." |
| t=63 | Varga | "The floor is warm. The vent is under us, and something large is coiled round it." |
| t=76 | Okafor | "Antechamber, then the vent. Shields up, Lancer. Everything you have." |
| t=88 | Varga | "My biosign is off the scale. It knows we're here." |
| t=97.5 | The Choir (distorted) | "[the Choir sings]" |
| Boss arrival (`boss-arrival`, the coil rising) | Varga | "Gill sacs first, Lancer. Then the lime in its throat when the jaw opens." |
| Phase 2 (`boss-phase` Tightening) | Okafor | "Walls of light. Find the gap and keep moving." |
| The dive (the head below 50 %, `boss-dive`, queued as `PHASE`) | Varga | "It's gone under! Watch the dark. It comes back fast!" |
| Phase 3 (`boss-phase` Implants, at the first spark) | Varga | "Commander, that's not Vrell. That's machinery in its spine. Relay housings... Helix pattern." |
| Phase 3, queued after Varga's line | Okafor | "Then hit the machinery, Lancer." |
| Maw destroyed (`boss-destroyed`, the chain's start), `delay: 3`: the Choir speaks, with its layering on speech | The Choir (distorted) | "...many... we were many here..." |
| After the Choir (`CLOSING`) | Okafor | "Maw's down. Aegis Wing, up the shaft. Now." |
| K+18, the cruisers' rise (`CLOSING`, `delay: 18`) | Varga | "Those aren't Vrell hulls--" |
| Then (`CLOSING`) | Okafor | "Those are not Vrell ships. God help us. Those are ours." |
| K+34, the run (`CLOSING`, `delay: 34`) | Okafor | "All Aegis units, break for orbit. Don't look back." |
| K+42, the run (`CLOSING`, `delay: 42`) | Varga | "That's the code from the wreck, painted on their bows. They aren't hiding it any more." |

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
  the arrival (≈ 6 s: the coil rising, the head lifting, the bar filling, invulnerable until the head is up)
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

Ambience: a new `europa-hatchery` bed (a slow warm throb, a distant Choir hum, creaking egg racks, no bubbles)
through the approach, the vent's rumble swelling through the antechamber and cutting at the kill; the breakout's
ice tear and water roar; Europa's night-ice wind above (Level 22's bed) from the breakout. New sounds this
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
**4.096** (1.6³), bounties also × the level's scale. At ≈ 91 a minute a **`bounty_scale` of about 0.74**
puts the typical haul on the budget (+0.4 %), the perfect run at 1.51 × (estimated by
hand with the Act 3 shares: 60 % of the air kills, 80 % of the ground, half the secret and the secondary, the boss
at 100 %); `TypicalHaulTest` confirms it in the build. The boss keeps the economy's act-boss share of
**24 %** (the 30 % capped by the perfect-run ≥ 1.4 × rule, Level 21's 24.8 %): **345 in Act 1 terms**, not the spec's
510 at an assumed 0.50.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Glassfin 74 × 2 + Barbfin 40 × 12 + Eel Swarm 3 × 48 + Abyss Ray 2 × 60 + Siren 4 × 28 + Spiral Nautilus 4 × 30 + Depth Hunter 10 × 30 | 4,316 | 2,590 |
| Ground targets: Spine Turret 6 × 12 + Polyp Mortar 4 × 15 | 400 | 320 |
| Boss: Abyssal Maw (head 297 + gill sacs 4 × 12) | 1,046 | 1,046 |
| Secret: *Pelagic-3* (hidden crate) | 410 | 205 |
| Secondary: clear the hatchery | 410 | 205 |
| **Total** (bounty scale 0.74) | **6,581** | **4,365** |
| Budget(n) = the typical haul's target; typical +0.4 %, perfect 1.51 × budget | | 4,350 |

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

None yet. The level needs (Act 4's production-art round; the mood from the Europa mood stills; the boss and
the cruisers are in the [bosses'](../../../enemies/bosses/README.md#concept-art) round-48 proposals):

- the hatchery backdrop (the gate, the galleries of egg racks, the spawning cavern, the antechamber, the
  caldera arena as one halted tile), the props (the egg racks, *Pelagic-3*, the service buoy), the shaft pieces
  in reverse, the breakout's ice (the ceiling, the slabs, the night ice under Jupiter) and the cruisers'
  rise;
- the two briefing images (`level-28-hatchery`, `level-28-hull-code`); the intel silhouette of the Maw;
  the act's outro images (the Act 4 README's);
- the round's captures: the level at medium with the plan's fit and the Bulwark: the approach, the Choir,
  the fight's phases and the dive, the death, the breakout, the reveal and the run, the debrief and the
  act end; 1:1 crops of the coil, the walls of light and a broadside.

## Implementation

- [ ] Level data `data.yaml` (M8 part H) with the sections, waves, ground targets, radio, budget, threat
      profile, briefing, backdrop and music; the arena a one-second halted section with `hold_after_boss: 5`
- [ ] Reference DPS 120 at L28 and the Act 4 balance plan's Level 28 fit (part A) before the data; the act HP
      factor for the returning `medium`+ units listed in *Waves*
- [ ] The approach to the Act 2+ rules (`ActFloorTest`, `PacingTest`, the pre-boss pause named as the third
      gap), the edge warnings for side and rear first entries, the Hunters guarding the gate
- [ ] The [boss](../../../enemies/bosses/abyssal-maw/README.md) arena and fight per its spec: the checkpoint at
      t=104 (none on hard), the bar, the warning, the Choir's song at t=97.5; Rook and the Bulwark out of the
      bights and the column
- [ ] The finale: the climb through Level 23's shaft pieces in reverse, the medium switch to air at the break
      line (factors out over 1 s, grade, special limits), the ice ceiling's shatter, the cruisers' rise and
      run (invulnerable `low-air` set pieces, the spec's broadsides, flak and point defence), the orbit; the
      armour floor at 1 (or the second checkpoint), the section after the kill outside the pacing checks
- [ ] Primary `reach-end` with the arena and the finale as the condition, `DESTROY THE ABYSSAL MAW`;
      secondary `kill_ratio` 65 % judged at the level's end (the boss, its eels and the cruisers not counted)
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`), without Rook; the boss lines as
      `PHASE` events including the new `boss-dive`, the finale's lines as `CLOSING` with `delay` from the kill,
      the Choir's fragment with its layering
- [ ] Threat profile (the finale not on the strip), Varga's four intel lines and Okafor's teaser fitting the
      intel panel (`IntelPanelLayoutTest`); the Maw's intel silhouette
- [ ] Briefing: both pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(28) with the level's `bounty_scale`; the boss's share inside 20–40 %
      (`BalanceTest`), the bounty recomputed with the measured scale
- [ ] Ground targets, the *Pelagic-3* secret (20 HP trigger, hard 28, crate 100), the overdrive, the service
      buoy's armour patch and the boss's phase-3 patch
- [ ] Music: track 11's stems by section, the warning and the boss theme (dropping under the dive), the
      reveal sting, the run under Vorne's motif, the act-complete fanfare under the outro; the ambience beds
- [ ] Act end: the debrief with `ACT IV COMPLETE · DEEP WATER`, the four outro pages with `act-complete`, the
      release's end moved here from Level 21 (`ACTS I-IV COMPLETE`, `next_act`), the hangar before L29
      reading `ACT V - THE BELT: COMING SOON` (`--level 28 --act-end`)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with the Bulwark on every
      difficulty (`--escort none` too)
- [ ] Production art and the round's captures

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
- **The hull code's text** `HLX-DS-0417` (a draft; it appears in the Level 28 briefing image and on the
  cruisers' bows).
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
