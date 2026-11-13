---
title: Level 41 – Callisto Fortress
design: approved
implementation: done
art: chosen
depends-on: [../../../enemies/ground/shield-pylon, ../../../enemies/ground/halo-platform, ../../../enemies/ground/warden-tank, ../../../enemies/ground/rail-bunker, ../../../enemies/ground/sentinel-tower, ../../../enemies/ground/sam-nest, ../../../enemies/ground/graft-turret, ../../../enemies/air/talon, ../../../enemies/air/chimera, ../../../enemies/air/gilded-gunship, ../../../enemies/air/hornet, ../../../enemies/air/lamprey, ../../../world/jovian, ../../../player/systems, ../../../ui/hud, ../../../story/characters/varga, ../../../systems/economy]
updated: 2026-11-13
---

# Level 41 – Callisto Fortress

## Summary

Helix HQ. Aegis Wing crosses Callisto's ice to the black citadel and breaks in gate by gate: each gate is held by
[Shield Pylons](../../../enemies/ground/shield-pylon/README.md) whose gold domes make the guns under them
invulnerable, and the scroll halts until the gate's pylons fall. Warden Tanks drive the causeways, Rail Bunkers and
Sentinel Towers hold the hangar decks, grafted turrets regrow on the walls. At the citadel's centre the scroll halts
over the [Halo Platform](../../../enemies/ground/halo-platform/README.md), its gun ring turning round a shielded core.
On the way, Varga's uplink spike pulls Vorne's files out of the archive cores; when the Halo falls she reaches the main
core and reads the name that changes the war: **the Silence**. The level ends at the empty berth: the Ascendant has
gone to orbit. About 158 s of level clock (the last 13 s held over the empty berth: the closing lines, step R79c) plus four halts (≈ 42 s measured at medium; about 204 s in all with the eases); ≈ 118 enemies a minute. Density 5.

**Status: approved 2026-10-31 (round 72, D.7)** (drafted in the Act 6 design phase, step `m10-d-levels-3`, 2026-10-31). Written from the act roster, the gap analysis's recommended answers (**Q12 a** the files in play, **Q3 a**
the three-way war, **Q9 a** the liveries, **Q10 a** the Level 41 mood, **Q7 b** and **Q8 a** the L41 visit's
generator and IFF spoofer; judgement calls **J1** the reference DPS 164, **J5** no new tiny unit, **J8** the Halo
Platform's r07 design, **J10** Okafor on the carrier, all decided 2026-10-31 (round 72)) and the wave 1 and 2 documents. The unit
documents own the pylons, the domes, the gates' rule and the Halo; this document places them and owns the timeline,
the files, the radio and the economy. Every number is a hand-written first draft at medium for M10's Level 41 part to
measure with `levelReport`. Built from M10 part G, step G2: the [data.yaml](data.yaml) holds them (the tables below are rendered from it).

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

<!-- data: briefing -->
> **Commander Okafor:** "Helix HQ is under Callisto's citadel. Shield pylons hold its gates:
> break them and the gates open. Vorne's flagship is docked inside. Get there before it leaves."
>
> **Dr. Varga:** "I worked in that building. Its archive cores hold Vorne's files: open one and
> fly over it, and my spike does the rest. The Halo at the centre only fires on the side that
> faces you."
<!-- /data -->

*Images* (production art in M10's Level 41 part; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-41-callisto-citadel`: a tactical chart of the citadel from above: the causeways across the ice,
  three gates as gold rings (the pylons), the Halo at the centre, the dock with the Ascendant's gold silhouette in its
  berth, the wing's route in white.
- Varga's page `level-41-helix-hq`: her old floor plan of Helix HQ (a Helix badge photo clipped to its corner, her
  own, 2179), the three archive cores circled in teal, a hand-drawn arrow to the main core under the Halo.

*Hangar teaser* (shop screen before L41; Okafor, not voiced):

<!-- data: teaser -->
> **Commander Okafor:** "The fortress is next. Anything that hits the ground, fit it. The
> Crucible is ready."
<!-- /data -->

## Threat profile

Draft; directions are counted by each wave's `from` (the intel's rule): every wave from the front (the roster's one
direction); the ground guns fire in any direction.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `ground` (the pylons, the Halo Platform, Warden Tanks, Rail Bunkers, Sentinel Towers, SAM Nests, Graft Turrets on the citadel's decks), `air` (Talons, Chimeras, Gilded Gunships and their Darts, Hornets, the Vrell's Lampreys and popcorn) |
| Attack directions | front 100% (the ground's fire from every side as the ship passes) |
| Density | 5 |
| Recommended traits | `anti-ground` (the gates, the hardened Rail Bunkers, the Halo), `area` (the guns under a dropped dome, the Halo's ring); `piercing` helps on the Halo's ring |
| Hazards | Rail lines (the Rail Bunkers and the Halo's rail turrets), Sentinel sweeps, SAM homing diamonds; the domes (no damage, they protect) |
| Boss / mid-boss | none (the Halo Platform is a held set piece, not a boss) |
| Sensor-suite detail | **none**: Callisto, the citadel, `ground` and `air`, front. **L1**: + density 5, hazards "rail lines" and "dome gates", the objective `BREAK THE GATES`, the secondary `FILES`. **L2**: + Shield Pylon, Halo Platform, Warden Tank, Rail Bunker, Sentinel Tower, SAM Nest, Graft Turret, Talon, Chimera, Gilded Gunship, Hornet portraits. **L3**: + `anti-ground` and `area` highlighted; the timeline strip marks the three gates (42, 74, 98 s) and the Halo (112 s); 1 secret. |
<!-- /data -->

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Callisto's citadel. Ground and air, all front." |
| L1 | "Three gates held by pylons. Then the Halo." |
| L2 | "Pylons, the Halo, Wardens, Rail Bunkers, Towers." |
| L3 | "Anti-ground and area for the gates and the Halo." |

## Objective

- **Primary** `reach-end` through the **pylon gates**: reach the Ascendant's dock. The objective line reads `BREAK THE
  GATES`. Each gate is a held section ([the pylon gates](../../../enemies/ground/shield-pylon/README.md#the-pylon-gates-level-41):
  Level 29's rescue halt with the gate's pylons as its holders): the scroll eases to a halt as the gate reaches the
  field's upper half, **the level clock stands while a pylon of the gate stands**, the gate's own **halt cycle** of
  waves runs (below), and the last pylon's fall opens the blast doors ahead and eases the scroll back up over 2 s. The
  gates cannot fail. The HUD's objective well counts **`PYLONS n / m`** to the current gate's total, with **three gate
  pips** ([HUD](../../../ui/hud/README.md#act-6-additions-draft)). The **Halo Platform** is a fourth held section
  (its own rule: the clock stands while its core lives; no pylon count, the pips only).
- **Secondary `FILES`** (Q12 a): Varga's uplink spike on Helix HQ's **three archive cores**, one on each gate's
  halted screen. A core sits under an armoured hatch (a `ground` trigger of **30 HP**, hard 40; not hardened, never
  under a dome); broken, the core glows teal and Varga's spike is live: the **ship's centre within 40 px of it for 2.0 s
  in total** (not Rook's: the spike rides Lancer's ship) uploads its file, a teal ring filling round the core as the
  timer. A core that leaves the bottom edge before its file is in is lost. The two-line tracker reads `FILES n / 3` on
  line two, counting to its target; the secondary is met with **3 / 3**, judged at the level's end. Pays **100** in
  Act 1 terms (1,049 in Act 6); the typical haul counts it at 50 %. Each file gets a line from Varga (*Radio chatter*).
  The Silence is named whatever the count: Varga reads it from the **main core** when the Halo falls (the story beat
  does not depend on the secondary).

## Layout

Scroll **140 px/s** over the open ice and the HQ run, **120 px/s** in the fortress (the ground needs time), 0 in the
four halts; each change eased over 2 s. Setting: the [Jovian system](../../../world/jovian/README.md#sub-locations-per-level)'s
Callisto HQ, vacuum ([vacuum rules](../../../world/belt/README.md#vacuum-rules): explosions flash short and throw debris
in a ring, wrecks do not burn). **Light** ([mood](../../../world/jovian/README.md#moods-per-level-q10), Q10 a): dark
blue-grey ice, the black citadel, red and gold lights, the Sun low with long shadows; **no Jupiter** (a top-down ground
level: the ice covers the screen from t = 0; Jupiter only in the briefing images and the space levels, the user's call
of 2026-11-01); the citadel's decks a step lighter than the units on them. Motion budget: the domes' slow shimmer and
the Halo's ring are the strong elements; the blast doors move only when a gate opens.

**Liveries** (Q9 a, the [enemies](../../../enemies/README.md#factions-and-visual-language)' rule; the gap note asked this
level to state it): the fortress's **own emplacements are elite**, **gloss black and heavy gold**: the Shield Pylons,
the Halo Platform (its r07 design, J8) and the citadel's structures (gates, blast doors, the dock). The **returning
units are regular**, the Act 5 gunmetal with gold trim, no re-render: the Warden Tanks (the roster's "black and gold"
is met by their Act 5 livery, black hull and gold trim, as [their page](../../../enemies/ground/warden-tank/README.md)
says), Rail Bunkers, Sentinel Towers, SAM Nests, Talons, Gunships, Hornets. The **Graft Turrets** and Chimeras are
**hybrids** (gunmetal and teal tissue).

**The halts and the clock.** A gate's section is two seconds long (`speed: 0`, `holds: pylons`): the scroll eases
down across it from 120 px/s, the clock stands at its end while a pylon stands, and the next section's 2 s ramp eases
it up again. The pylons sleep until the halt (their domes already lit, the emitters dark, not shootable) and wake as
the scroll stands, so no gate falls on the way in (Level 29's clamp-gun rule). The Halo's section is the same with
`holds: halo` (its core), and a **checkpoint** at its halt (as a boss's; none on hard; the
[retry](../../../systems/retry/README.md) rules). Typical halts at medium with the plan's fit, on paper: Gate One ≈ 8 s (two
pylons, 480 HP), Gate Two and Gate Three ≈ 11 s (three, 720 HP), the Halo ≈ 16 s (1,560 HP), all at the boss rule's
0.6 × the reference of 164 between the halt cycles' waves: ≈ 46 s of halts (the data's `halt_cycle.typical`).
**Measured** (step G5, `levelReport -Pparts=gates`, three seeds at medium; the HP stay the approved ones and the doc
follows the measure): Gate One 8.8–11.3 s, Gate Two 9.9–10.7 s, Gate Three 7.7–10.5 s, the **Halo ≈ 13 s** (12.9–13.5 s;
easy ≈ 8 s, hard ≈ 15 s): **≈ 42 s** of halts, the level about 192 s in all.

**Rook and the escort.** Rook flies his slot with his **Autocannon** (the plan's L41 fit since M10 part I, step I2: on
his Mortar, the plan's fit until then, the fortress's Talons flew past and Act 6 ended under the expert's bound on
medium; with the Autocannon the autopilot earns about 1,100 more here and the small units die sooner, `BeamEraHp`
1.42× → 1.33×); his pick skips covered units while an uncovered target is on the screen (the dome rule) and
takes the pylons first in a halt. With `--escort none` the level is the same without him and his lines.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Ice Field | 0–20 | 0–2,800 | 140 | clear | `ground`: Callisto's cratered ice from t = 0, a crater-rim outpost (t≈8), the outer causeway's gatehouse (t≈15). | Talons, Chimeras and popcorn; the first SAM group and a Graft nest. |
| 2. The Outer Causeways | 20–42 | 2,800–5,460 | 120 | clear | `ground`: two causeways across a crevasse field to the citadel's outer wall; a Sentinel Tower on the wall (t≈32). | Warden Tanks on the causeways; the Lampreys on the Gunships; the first gate ahead. |
| 3. Gate One | 42–44 | 5,460–5,580 | 0 | clear | `ground`: the outer gate: two pylons on its towers, their domes over a Warden pair parked on the apron before the gateway and a SAM group on the right bastion; archive core A in the courtyard at (240, 400). | The first halt (≈ 10 s); the first file. |
| 4. The Hangar Decks | 44–74 | 5,580–9,060 | 120 | clear | `ground`: hangar roofs and launch decks, open bays with parked Talons (scenery), Rail Bunker pairs (t≈50, 70), a Graft nest (t≈58), a SAM group (t≈64). | Hardened bunkers and regrowing turrets. |
| 5. Gate Two | 74–76 | 9,060–9,180 | 0 | clear | `ground`: the hangar gate: three pylons, their domes over the Rail Bunker pair, a Warden pair and a Graft nest of 3; archive core B at (120, 390). | The second halt (≈ 10 s); the second file. |
| 6. The Citadel Ring | 76–98 | 9,180–11,700 | 120 | clear | `ground`: the ring road round the citadel's core: a Sentinel Tower (t≈80), a SAM group (t≈84), a Warden convoy (t≈86), a Warden pair driving to the inner gate (t≈95). | The heaviest ground stretch. |
| 7. Gate Three | 98–100 | 11,700–11,820 | 0 | clear | `ground`: the inner gate: three pylons, their domes over the Warden pair, a Rail Bunker and a SAM group; archive core C at (360, 390). | The third halt (≈ 9 s); the third file. |
| 8. The Inner Court | 100–112 | 11,820–13,140 | 120 | clear | `ground`: the court before Helix HQ's tower: a Graft nest (t≈104), Varga's old research wing with its lab vault (t≈104, the secret), a Sentinel Tower (t≈108); the Halo's rim enters at the top. | The approach to the Halo. |
| 9. The Halo | 112–114 | 13,140–13,260 | 0 | clear | `ground`: the Halo Platform on its mount over the HQ's main core, its core at (240, 190); nothing else on the ground. | The held centrepiece (≈ 13 s), the checkpoint; the main core and the Silence. |
| 10. Helix HQ | 114–145 | 13,260–17,460 | 140 | clear | `ground`: HQ's roofs and comm masts, a Rail Bunker pair (t≈120), a SAM group (t≈126), a Sentinel Tower (t≈132), a Graft nest (t≈138), then the dock: the Ascendant's **empty berth**, its clamps open, its gold drive glow climbing away in the `deep` layer. | The run to the dock. |
| 11. The Empty Berth | 145–158 | 17,460–17,612 | 1 | clear | `ground`: The dock round the **empty berth** held on the screen (as at t=146 before step R79c): no enemies. | The calm run-out (≈ 17 s from the last enemy gone ≈ t=141, the music out): Varga's archive line and Okafor's closing line; the level ends at t=158. |
<!-- /data -->

**The ending** (step R79c, round 79: "the ending is quite abrupt with no radio"): the last wave (t=136) is gone by
t≈141 and the theme fades out over t=141–145 (`stem_times`: the ambience alone); the scroll eases from 140 to 1 px/s
over t=145–147, so the empty berth stays on the screen as ART-G2 drew it (the ground's top row 18,152 at the end, the
art's last 18,163: no rerun of the backdrop), and Varga's archive line (t=142.5) and Okafor's closing line (t=150.1)
play over it; the level ends at t=158, ≈ 17 s of calm (`PacingTest`'s end calm ≈ 16 s). Its empty stretches over 2 s:
the end's calm only; the halts carry their cycles' waves.

## Waves

Formation names from the [formation vocabulary](../../../enemies/README.md#formation-vocabulary). The **act HP
factor** for the returning `medium`+ units (the proposed reference **164** at Level 41 against the unit's first level,
J1): Gilded Gunship ×164/124 (88 → 116; its [Act 6 shield](../../../enemies/README.md#enemy-shields) 24 → 32), Hornet
×164/128 (96 → 123, shield 24 → 31), Warden Tank (its [Act 6 section](../../../enemies/ground/warden-tank/README.md):
hull 114, turret 36), Rail Bunker ×164/136 (100 → 121), Sentinel Tower ×164/132 (120 → 149). The new units fly their
pages' numbers (the Shield Pylon 240, the Halo Platform 1,560). The `small` units (Talon with its Act 6 shield,
Chimera, Lamprey, SAM Nest, Graft Turret) keep their pages' HP with the beam era's ×2, the `tiny` ones (Darts,
Skitters, Motes) 1 HP. Every number is a hand estimate for `levelReport`.

- **Density and pacing** (the Act 2+ floor): **379 enemies at medium** over ≈ 192 s (146 s of clock to the berth and the four
  typical halts, ≈ 46 s): the moving waves' 215, the Gunships' **36 Darts**, the halt cycles' 60 (one typical halt
  each) and the **68 ground targets** (with the 8 pylons and the Halo), **≈ 118 a minute** (the act's band 90–120;
  the fortress's ground makes it the act's busiest screen). A wave every 2.5–3 s while the scroll moves (wider than
  Level 40's: the ground fills the gaps), the Talons' and Chimeras' pairs, the Gunships' rings and the ground guns for
  the shots.
- **Halt cycles** (times from each halt's start H, clock stopped; a cycle repeats while the halt lasts and is cut the
  moment the last holder falls; a wave already in flies on): **Gate One** H+1 Mote Swarm 6, H+5 Talon line 4, H+9
  Chimera swarm 6, from H+13 again; **Gate Two** H+1 Skitter swarm 6, H+5 Talon V-wing 5, H+9 Mote Swarm 6, from H+13;
  **Gate Three** H+1 Mote Swarm 6, H+5 Talon V-wing 5, H+9 Chimera swarm 6, from H+13; **the Halo** H+2 Mote Swarm 6,
  H+7 Talon line 4, H+12 Skitter swarm 6, from H+17 (the platform is the threat: a lighter cycle). All from the front.
  The density counts one typical halt: Gate One's first two waves, the others' first three.
- **Tiny units** (J5): the Gunships' Darts, the Skitters and Mote Swarms.
- **The Warden Tanks on the ground** (step R79c, round 79: "the tanks are on top of each other [...] and one drives
  over the gate. Isn't the gate a raised structure?"): the causeway pair and the ring-road convoy enter **1 s apart**
  (≈ 80 px on their road, the tank 64 px) and walk their roads off the screen; each gate's pair drives up its lanes or
  the launch deck or the ring road (one behind the other or on separate lanes) and **parks** (`park: true`: it stops at
  its path's end and stands, scrolling with the ground) on the apron before the gateway, ≈ 72–78 px apart, its body
  clear of the wall's lower face (the wall is a raised structure, 36 px thick at y 110, 116 and 110 on the held
  screens): Gate One's at (263, 187) and (189, 205), Gate Two's at (257, 210) and (184, 190), Gate Three's at
  (300, 202) and (223, 207), each under its dome (within easy's 95 px). They no longer walk on through the halt and
  over the wall; when the gate opens they scroll away with the apron. `Level41GroundTest` flies the level and checks
  both (no two walkers closer than 64 px, none over a wall's face in a halt).
- **The three-way war** (Q3 a): the Lamprey streams of t=24 and t=85.5 are **hunters** of the Gunship columns of t=21.5
  and t=83 (prey): they latch on the Gunships (Level 33's rule: the shield empties, the guns stop, it drifts down), and
  a Gunship the Vrell kill pays nothing ([the rule](../../../enemies/README.md#the-three-way-war)). The Vrell eat the
  fortress too, in the backdrop: teal growth on the citadel's outer wall where the Vrell landed after Aurelia.
- **Directions**: every wave from the front (the roster); no edge warnings needed.
- **Totals**: Talon 83 (11 leaders) · Mote Swarm 84 · Chimera 46 · Skitter 42 · Gilded Gunship 6 (+ 36 Darts) ·
  Lamprey 8 · Hornet 6; the ground's 68 under *Ground targets*.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 6.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 9 | 1 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Over the crater-rim SAM group |
| 11.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 14 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 16.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 19 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 21.5 | 2 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | 2 s apart, ≈ 12 Darts; **prey** of the t=24 Lampreys (three-way war) |
| 24 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front | **Hunters** of the t=21.5 Gunships: they latch them (Level 33's rule); Rook's line at t=25 |
| 24.6 | 2 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front | The first Warden pair on the left causeway |
| 26.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 29 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 31.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | |
| 34 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 36.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 39 | 2 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | Holding over the gate as the halt begins |
| 40.27 | 2 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front | The Warden pair driving up to the gateway, parked on its apron under Gate One's dome A |
| H1+1 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Halt cycle; every 13 s |
| H1+5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Halt cycle; not on easy |
| H1+9 | 3 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Halt cycle (the typical halt ends ≈ H+8); every 13 s |
| H1+11 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Halt cycle, hard only |
| 46 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 48.5 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 51 | 4 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Over the first Rail Bunker pair |
| 53.5 | 4 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 56 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 58.5 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Its leader drops a shield cell |
| 62 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 65 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | |
| 68 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 71 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | The last before Gate Two |
| 71 | 4 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front | A Warden pair driving up to the hangar gate, parked on its apron under Gate Two's dome B |
| H2+1 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | Halt cycle; every 13 s |
| H2+5 | 5 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Halt cycle; not on easy |
| H2+9 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Halt cycle (the typical halt ends ≈ H+11); every 13 s |
| H2+11 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Halt cycle, hard only |
| 78 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 80.5 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 83 | 6 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | 2 s apart, ≈ 12 Darts; **prey** of the t=85.5 Lampreys; the last drops the overdrive |
| 85.5 | 6 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front | **Hunters** of the t=83 Gunships: they latch them |
| 86 | 6 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 3 | front | The Warden convoy on the ring road |
| 88 | 6 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 91 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 94 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | The last before Gate Three |
| 95 | 6 | convoy | [Warden Tank](../../../enemies/ground/warden-tank/README.md) | 2 | front | The Warden pair driving up to the inner gate, parked on its apron under Gate Three's dome A |
| H3+1 | 7 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Halt cycle; every 13 s |
| H3+5 | 7 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Halt cycle; not on easy |
| H3+9 | 7 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Halt cycle (the typical halt ends ≈ H+11); every 13 s |
| H3+11 | 7 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Halt cycle, hard only |
| 102.5 | 8 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 105 | 8 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 107.5 | 8 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 110 | 8 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | The last before the Halo |
| 113.98 | 9 | held | [Halo Platform](../../../enemies/ground/halo-platform/README.md) | 1 | the ground (it scrolls in with the inner court and halts with its core at (240, 190)) | Its halt cycle (Mote Swarm, Talon line, Skitter swarm every 17 s); no pylon near it |
| H4+2 | 9 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Halt cycle; every 17 s |
| H4+7 | 9 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Halt cycle; not on easy |
| H4+11 | 9 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Halt cycle, hard only |
| H4+12 | 9 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | Halt cycle (the typical halt ends ≈ H+16); every 17 s |
| 116 | 10 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 118.5 | 10 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 121 | 10 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | 2 s apart, ≈ 12 Darts, out of the HQ's hangar |
| 124 | 10 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 127 | 10 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 130 | 10 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 133 | 10 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 136 | 10 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | **The last wave**; the end's calm from t≈141 |

Totals: Talon 83 · Mote Swarm 84 · Chimera 46 · Skitter 42 · Gilded Gunship 6 · Lamprey 8 · Warden Tank 11 · Hornet 6 · Halo Platform 1 (with each gate's cycle waves of its typical halt).
<!-- /data -->

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 1 | [SAM Nest](../../../enemies/ground/sam-nest/README.md) group ×3 on the crater-rim outpost (t≈8) | Bounty 15 each; homing diamonds, shootable |
| 1 | [Graft Turret](../../../enemies/ground/graft-turret/README.md) nest ×4 on the gatehouse roof, the Vrell growth round it (t≈15) | Bounty 15 once each, the root 5; a turret regrows 5 s after its death unless its root dies |
| 2 | [Sentinel Tower](../../../enemies/ground/sentinel-tower/README.md) on the outer wall's central tower (t≈32) | Bounty 40; its sweeps |
| 2 | **Gate One**: [Shield Pylon](../../../enemies/ground/shield-pylon/README.md) **A** on the left gate tower (80, 96), its dome over the Warden pair parked on the apron before the gateway | Bounty 45; asleep until the halt |
| 2 | Shield Pylon **B** on the right gate tower (400, 96), its dome over the SAM group on the right bastion | Bounty 45; the halt ends with the gate's last pylon |
| 2 | SAM Nest group ×3 on the right bastion, under Gate One's dome B | Bounty 15 each; covered until pylon B falls |
| 2 | Archive core **A** under its hatch (30 HP, easy 20, hard 40) in the courtyard, (240, 400) | `FILES` 1 of 3: the ship's centre within 40 px of the broken hatch for 2.0 s in total |
| 4 | [Rail Bunker](../../../enemies/ground/rail-bunker/README.md) pair on the hangar decks' pads (t≈50) | Bounty 45 each; hardened; never both lines on the ship's row |
| 4 | Graft Turret nest ×4 on the launch deck's edges (t≈58) | Bounty 15 once each, the root 5 |
| 4 | SAM Nest group ×3 on a hangar roof (t≈64) | Bounty 15 each |
| 4 | Rail Bunker pair on their deck pads before the hangar gate (t≈72), covered by Gate Two's dome A from the halt | Bounty 45 each; covered until pylon A falls |
| 4 | **Gate Two**: Shield Pylon **A** on the right gate tower (430, 100), its dome over the Rail Bunker pair | Bounty 45; asleep until the halt |
| 4 | Shield Pylon **B** on the left gate tower (60, 100), its dome over the Warden pair parked on the apron before the gateway | Bounty 45 |
| 4 | Shield Pylon **C** on the left bastion (100, 260), its dome over a Graft nest of 3; not on easy; easy ×0 | Bounty 45; the halt ends with the gate's last pylon |
| 4 | Graft Turret nest ×3 on the apron beside the left bastion, under Gate Two's dome C | Bounty 15 once each, the root 5; covered until pylon C falls |
| 4 | Archive core **B** under its hatch (30 HP), (120, 390) | `FILES` 2 of 3 |
| 6 | Sentinel Tower on the citadel core's wall (t≈80) | Bounty 40 |
| 6 | SAM Nest group ×3 on the core's terrace (t≈84) | Bounty 15 each |
| 6 | Rail Bunker on its pad by the inner gate, under Gate Three's dome B | Bounty 45; covered until pylon B falls |
| 6 | SAM Nest group ×3 on the apron, under Gate Three's dome C | Bounty 15 each; covered until pylon C falls |
| 6 | Archive core **C** under its hatch (30 HP), (360, 390) | `FILES` 3 of 3 |
| 6 | Shield Pylon **B** on the left tower (50, 132), its dome over the Rail Bunker | Bounty 45 |
| 6 | **Gate Three**: Shield Pylon **A** on the right gate tower (420, 100), its dome over the Warden pair parked on the apron | Bounty 45; asleep until the halt |
| 6 | Shield Pylon **C** on the right bastion (410, 262), its dome over the SAM group; not on easy; easy ×0 | Bounty 45; the halt ends with the gate's last pylon |
| 8 | Graft Turret nest ×4 in the inner court (t≈104) | Bounty 15 once each, the root 5 |
| 8 | Varga's lab vault (20 HP, hard 28; t≈104, x≈410) | The secret: frees a crate worth 100 (1,049) |
| 8 | Sentinel Tower in the inner court (t≈108) | Bounty 40 |
| 10 | Rail Bunker pair on HQ's roofs (t≈120) | Bounty 45 each; hardened |
| 10 | SAM Nest group ×3 on HQ's roofs (t≈126) | Bounty 15 each |
| 10 | Sentinel Tower on HQ's roofs (t≈132) | Bounty 40 |
| 10 | Graft Turret nest ×4 on HQ's last roof (t≈137) | Bounty 15 once each, the root 5 |
<!-- /data -->

**Totals** (68): Shield Pylon 8, Halo Platform 1, Warden Tank 11, Rail Bunker 7, Sentinel Tower 4, SAM Nest 18, Graft
Turret 19. **Gate layouts** follow the pylon's [placement rule](../../../enemies/ground/shield-pylon/README.md#behaviour):
the pylons stand in the halted screen's upper half, outside every dome, the domes' centres at y ≤ 360, so the covered
guns never sit over the ship's band and the archive cores (y 390–400) lie outside every dome. A gate is a choice of
order (the pylons, then what they covered; or the core's hatch first), never a wall of invulnerable fire: the EMP
Burst drops every dome for its 3 s and a `shield-breaker` hit drops one for 2 s (the pylon's openers). The Halo
stands alone: no pylon near it (its barrier is the gate there).

**The IFF spoofer** (the plan fits it from the L40 visit, [the module](../../../player/systems/README.md#act-6-the-iff-spoofer-and-the-core-tiers-draft)):
it delays the SAM Nests', Sentinel Towers', Rail Bunkers', Warden Tanks' and the Halo's aimed volleys by 0.5 s (their
muzzles stutter amber); the Graft Turrets (hybrids) and the aircraft are not fooled. It is the level the module was
bought for.

## Hazards

- **The fortress guns**: the Rail Bunkers' and the Halo's rail lines (at most **two** telegraphed lines on the screen at
  once, never both on the ship's row; the Halo's one line at a time is its own), the Sentinel Towers' sweeps, the SAM
  Nests' homing diamonds (the Decoy Flares retarget them), the Warden Tanks' bursts back up the screen, the Graft
  Turrets' pairs. No terrain collision (ground never collides).
- **The domes** do no damage: shots on a covered unit are spent with a gold spark ([the dome](../../../enemies/ground/shield-pylon/README.md#the-dome)).
- **No eruptions, no weather**: Callisto is dead ice in vacuum.
- **Specials** ([Jovian media](../../../world/jovian/README.md#media-per-level)): the Airstrike flies (CDF bombers from
  Okafor's carrier; it hits covered units no more than shots do: the dome stops it), the Orbital Lance has cover; the
  EMP Burst drops every dome for the stun, empties the shields, stuns the machines and hybrids; the Sonar Pulse reads
  `NO WATER`.

## Secrets and pickups

- **Varga's lab vault** (the secret, t≈104, x≈410, in the inner court's research wing): the gate-research lab she left
  in 2179, its roof cracked, a Helix vault still sealed in the floor. A `ground` trigger of **20 HP** (28 on hard);
  destroyed, its contents float up as a crate worth **100** in Act 1 terms (1,049). The Salvage scanner's glint shows
  it at any range. Its event line, Varga: "*My old lab. They kept the vault. Open it, Lancer.*"
- **Shield cell**: dropped by the leader of the t=58.5 V-wing.
- **Overdrive**: dropped by the second Gunship of the t=83 column when it dies, whoever kills it (the Lampreys' prey).
- **Armour patches** ×2: a CDF beacon dropped by the fleet's bombers on the ice releases one at (240, 380 y-up) at
  t=20; the Halo's core drops one at its death (before the run to the dock).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), draft text for the **1 s rule** (12 characters a second plus a
1.5 s lead; 0.4 s of silence); event lines drop after 6 s. Rook's lines carry `requires: escort`. **Speakers: Okafor**
(from the carrier, J10), **Varga**, **Rook** on the friendly channel ([characters](../../../story/characters/README.md)).
Vorne and the Choir are silent (the story's [Vorne](../../../story/characters/vorne/README.md#act-6-draft): his files
speak for him, and nobody talks over Varga's reading of them). The halts stop the clock, so their lines are events
(`halt` at a halt's start, `file` at each upload, `halo-destroyed`). **The Silence line** never goes stale (Level 21's
`CLOSING` rule applied mid-level): the story beat plays whatever the radio is doing. Its wording is Q12 a's sample
line.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Callisto's citadel, Lancer. Shield pylons hold its gates. Break them and the gates open." |
| t=12.5 | Varga | "I worked in this building. Open an archive core and fly over it: my spike does the rest." |
| t=25 | Rook | "Lampreys on the Gunships. The bugs are eating the fortress too." |
| t=35 | Okafor | "Tanks on the causeway, and a gate ahead. Pylons first, then whatever they cover." |
| Gate One's halt (`halt`, event) | Varga | "Two pylons, two domes. Nothing under a dome can be hurt. The core's in the courtyard." |
| File 1 in (`file`, event) | Varga | "First file. Shipping lists: Helix biotech going out through the gate. For years." |
| t=51 | Okafor | "Rail bunkers on the hangar decks. Anti-ground if you have it, Rook's mortar if you don't." |
| t=62 | Varga | "Graft turrets on the decks. Kill the root, or they grow back." |
| Gate Two's halt (`halt`, event) | Okafor | "Three pylons this time. Any order, but fast." |
| File 2 in (`file`, event) | Varga | "Second file. A contract. Vorne's name, and a Vrell sigil beside it." |
| t=87 | Rook | "Same tanks as Mars. Nicer paint." |
| Gate Three's halt (`halt`, event) | Okafor | "The inner gate. The Halo is behind it." |
| File 3 in (`file`, event) | Varga | "Third file. The price was passage. The Vrell wanted through the gate. To us." |
| t=105 | Varga | "The Halo. Its guns fire only on the side that faces you. Kill three and the core opens." |
| The Halo's halt (`halt`, event) | Rook | "A merry-go-round with guns. I hate merry-go-rounds." |
| Halo core destroyed (`halo-destroyed`, `delay: 2`, never stale; the music ducks 6 dB under it) | Varga | "The Halo fed the main core. I'm in. They were not invading. They were running. Vorne's files call it the Silence." |
| t=129 | Okafor | "The Ascendant is off its berth. He's running for orbit." |
| t=136 | Rook | "Then we chase him. Like always." |
| t=142.5 (over the empty berth, after Rook's line with him flying: step R79c's closing lines) | Varga | "Main archive copied. All of it is on its way to the carrier." |
| t=150.1 (the level's last voice, ending ≈ t=157 (its text); the level ends at t=158) | Okafor | "Good work, Lancer. All wings, back to the carrier." |
<!-- /data -->

Timeline check (step G4, `RadioTimelineLevel41Test` on the game's queue: with and without Rook, 0–3 files, short and
long halts, each file 3–4 s into its halt as G3's autopilot takes them): in the halts the clock stands, so the halt and
file lines play in real time; as a gate's scroll eases out the next timed line is reckoned at the full rate, so a file
line does not take the gap it would overrun. Retimed in step G4 so every timed line starts at most 1 s late in text:
Varga's t=11 to 12.5, Okafor's t=33 to 35 and t=48 to 50, Varga's t=60 to 62 and her Halo line t=100.5 to 105 (it came
right after Gate Three's halt, behind the third file's line). Step H4b: Okafor's t=50 to 51 (voiced it started ≈ 1.3 s
late behind Gate One's halt line in a short halt), and **a file uploaded as its gate's halt line opens takes that line's
place** (the halt line dropped while it waits or within 2 s of opening, its voice cut): Gate One's halt line and the
first file line no longer both fit the ≈ 8 s halt before t=51, and the first file line went stale in every run; now
the halt line gives way when the file comes 3–4 s into the halt (G3's autopilot), and only a file uploaded after the
halt line has played on (≈ 9 s in) still loses its line; the second and third file lines play. The Silence line never goes stale (`stale:
never`: it waits for the radio and plays, 2 s after the core's death, the music ducked 6 dB under it; voiced it runs
**7.5 s**, Varga's grim row); Okafor's t=129
and Rook's t=136 follow (step R79c: from 132 and 139, so the closing lines fit), then over the empty berth Varga's
archive line (t=142.5, right behind Rook's in text) and Okafor's closing line (t=150.1, the level's last voice, its
text on the radio until ≈ t=157) before the level's end at t=158; every line starts at most 1 s late, with or without
Rook, voiced or not.

**Story notes.** Varga worked at Helix on the gate research until 2179 ([Varga](../../../story/characters/varga/README.md));
this is her old building, so the files are hers to read and the vault is her lab. The three files build the deal
(biotech out, a contract, passage in) and the main core names why: the Vrell were fleeing **the Silence** (its first
mention, Q12 a: named once, sharply, never shown). Vorne does not answer: his next words are Level 42's.
Okafor's "running for orbit" hands over to Level 42.

## Music & ambience

Draft for the audio step (D-audio owns the [Act 6 music plan](../../../audio/music/README.md#track-list)):
[track 15](../../../audio/music/README.md#track-list), the Act 6 B theme (heavy, stormy, choir and distorted bass): the
**base stem** on the ice field, the **full mix from the causeways** (t=20), held through the halts, Vorne's motif
**on** (his headquarters); at the Silence line the mix ducks under the radio and the **motif stem drops out** until the
level's end (his secret is out); the theme fades out over t=141–145 as the last enemies go (step R79c), and the
closing lines over the empty berth play over the ambience alone. The debrief plays the `mission-complete` jingle (23). Ambience: the `callisto-fortress` bed
([sfx](../../../audio/sfx/README.md#ambience-per-setting): the belt's convention, heard through the hull) with the
fortress's machinery (deep hums, door servos, distant klaxons) in sections 2–10. New sounds this level needs: the pylons' dome hum, hit and collapse, the Halo's
ring grind and barrier collapse (their pages), the **blast doors opening** at each gate (a huge steel door's rumble,
hydraulic clunks, a final heavy thud), the **hatch break** (a heavy armour plate cracking off), the **uplink** (a
short rising electronic tone as the spike seats, a steady data chatter while it uploads, a clean confirmation chirp
with the file; electronic, never bubbly), the vault's crack (a steel door's bolts shearing). Freesound only for
real-world sounds (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(41) = 700 × 1.07⁴⁰ ≈ **10,482** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act 1
terms** (Talon 12, leader 25, Mote Swarm 2 a mote, Chimera 14, Skitter 5, Gilded Gunship 28, Dart Drone 2, Lamprey 12,
Hornet 26, SAM Nest 15, Graft Turret 15 and its root 5, Warden Tank 28, Rail Bunker 45, Sentinel Tower 40, Shield
Pylon 45, Halo Platform 120, the crate 100, the secondary 100), × the act factor **10.486** (1.6⁵), bounties also × the
level's scale. At ≈ 118 a minute with 68 ground targets a **`bounty_scale` of 0.30** puts the typical haul on the
budget (**10,361, −1.2 %**), the perfect run at 1.57 × (estimated by hand with the economy's shares: 60 % of the air
kills and launched Darts, 80 % of the ground, half the secret and the secondary; the roots and the leaders' extra in
the perfect run only); `TypicalHaulTest` confirms it in the build. The scale is under the gap analysis's band
(0.40–0.56): the fortress's ground pays 1,818 in Act 1 terms (Level 29's 0.35, the last ground-heavy opener, is the
nearest). No boss or mid-boss.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Talon 83 × 12 + Mote Swarm 84 × 2 + Chimera 46 × 14 + Skitter 42 × 5 + Gilded Gunship 6 × 28 + Lamprey 8 × 12 + Warden Tank 11 × 28 by parts (26 whole) + Hornet 6 × 26 + launched Dart Drone 36 × 2 | 8,862 | 5,458 |
| Squad leaders: 9 × 13 extra (perfect run only) | 369 | 0 |
| Ground targets: SAM Nest 18 × 15 + Graft Turret 19 × 15 (+ root 5) + Sentinel Tower 4 × 40 + Shield Pylon 8 × 45 + Rail Bunker 7 × 45 | 4,677 | 3,742 |
| Held: Halo Platform (core 60 + 6 batteries × 10) | 375 | 375 |
| Secret: lab vault (hidden crate, 1% of budget) | 1,049 | 524 |
| Secondary: upload Vorne's 3 files | 1,049 | 524 |
| **Total** (bounty scale 0.3) | **16,381** | **10,624** |
| Budget(n) = the typical haul's target; typical +1 %, perfect 1.56 × budget | | 10,482 |
<!-- /data -->

Longer halts add their cycles' waves (the perfect run on a slow halt earns more). **Balance plan**: the plan's L41
visit buys **Composite V** (the Crucible and the **IFF spoofer** came at the L40 visit, J11) and keeps Rook's
Autocannon (M10 part I; [the hand plan](../../../player/generator/README.md#act-6-hand-plan-draft)): forward DPS 168 against the proposed
reference **164** (1.02), 24 of 27 MW, three MW spare. The level expects `anti-ground` and `area`; the plan's fit has
neither (the hardened bunkers are left; Rook's Mortar, the plan's until M10 part I, was the anti-ground) and breaks the gates on its beam (the pylons are not hardened:
every weapon breaks a gate, the pylon's decision). The plan's fit measures the halts (M10's Level 41 part).

## Difficulty notes

- **Easy**: the formation lever thins the wings and the popcorn; one Gunship in each column; the halt cycles without
  their Talon waves; the pylons' easy hooks (dome radius 95 px, **2 pylons a gate**: Gate Two and Gate Three lose
  their bastion pylon and its dome); the Halo's easy hooks (slower ring, bursts of 2, no open-core ring); the archive
  hatches 20 HP and the upload 1.5 s; the Halo's checkpoint.
- **Hard**: the wings larger; a third Gunship in the t=83 column; the halt cycles add a Talon line of 4 at H+11 (a halt
  cycle's waves come from the front); the pylons' hard hooks (dome radius 125 px, back 1.0 s
  sooner after an opener); the Halo's hard hooks; the hatches 40 HP and the upload 2.5 s; the vault 28 HP; no
  checkpoint.

## Concept art

**Production backdrop (M10 part G, round 79, step ART-G2;
[`tools/art/backdrop_l41.py`](../../../../tools/art/backdrop_l41.py)).** Callisto's cratered ice from t = 0 (no
Jupiter), the Sun low in the top left with every structure's shadow 2.6 times its height, the black-and-gold citadel on
decks a step lighter than its units: the crater-rim outpost, the Vrell-grafted gatehouse, the two causeways over the
crevasse field, the outer wall with its Sentinel tower and the Vrell growth, the three gates (walls, towers under the
pylons, bastions, the cores' wells on the aprons), the hangar decks with parked Talons in their bays, the citadel ring,
the inner court with Varga's cracked-open research wing, the Halo's mount, Helix HQ's roofs and masts and the dock's
empty berth with the drive glow climbing away in its well (`deep`). One continuous fortress cut into nine ground pieces
on exact rows (no tile set; about 2.3 atlas pages with the doors, the depth tile and the glow). Each gate's **blast
doors** are a 12-frame piece (closed, parting along the scroll's 2 s ease), drawn on frame 0 until the gate's
`GATE_OPENED` (step G4's hook). The proposal (`concept/backdrop-proposal.yaml`, step G2's input, not data) holds the
backdrop block, the sections, the gate layouts (pylons, facings, domes, covered units, cores, doors), the other ground
placements and the roads' paths. Prompts and briefs: [concept/prompts.md](concept/prompts.md). The briefing images are
on [the briefing screen's](../../../ui/briefing/README.md#concept-art) sheet.

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r79-a.png](concept/backdrop-final-r79-a.png) | Production backdrop: sixteen screens at half size with stand-ins (the ice at t=0, the outpost, the gatehouse, the causeways, the outer wall and the growth, the three gate halts with their pylons, domes, covered units and cores, the hangar decks, the citadel ring, the research wing, the Halo on its mount, HQ's roofs and masts, the dock, the empty berth), the readability crops at 1× (each stretch's darkest and brightest window with a Talon, a Chimera, a Warden, a SAM, amber needles, an orb, the ship), the doors' frames, the whole fortress at eighth size | chosen (round 79 A.6 ok) |
| [concept/backdrop-final-r79-a.gif](concept/backdrop-final-r79-a.gif) | Gate One's halt at half size: the domes up, the second pylon falls, the blast doors part as the scroll eases back up | chosen (round 79 A.6 ok) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (the fortress pieces, the `doors:` rows, the drive glow's path), the sections, the gate layouts, the Halo's place, the other ground placements, the vault and the beacon, the roads' paths for step G2 (not data) | chosen (round 79 A.6 ok; step G2's input) |

**Production ground objects (M10 part G, round 79, step ART-G1;
[`tools/art/l41_props.py`](../../../../tools/art/l41_props.py)).** The archive cores (`archive-core`, 40×40: sealed,
hit, open; `-break`, `-glow`, the uplink's `-ring_0..31` at the 40 px zone's edge filling clockwise, `-done`), Varga's
lab vault (`lab-vault`, 36×36: sealed, hit, blown open) and the CDF beacon (`cdf-beacon`, 48×48, with `-lights`,
`-open`, `-fall`, `-retro`, `-land`); held in `build/art-held/sprites/` until this level's data names them as `sprite`s
(step G2). The uplink's teal is a cool cyan-teal (70, 235, 220), kept off the Vrell's green-teal (pending the user's
review). The pylon's and the Halo's frames are on their pages' round-79 sheets; their intel portraits on
[the hangar's](../../../ui/hangar/README.md#concept-art) `intel-final-r79-a`. Prompts:
[concept/prompts.md](concept/prompts.md#l41-objects-final-r79-a).

| File | What | Status |
|---|---|---|
| [concept/l41-objects-final-r79-a.png](concept/l41-objects-final-r79-a.png) | The archive core's hatch (sealed, hit, open with its teal glass stack), its break, glow, the uplink ring (the track, then filling) and the file-in flare; the lab vault (sealed, hit, blown open); the CDF beacon planted, opening, falling with its retro burn, its lights and the landing's dust ring; the readability over Level 41's darkest and brightest deck and ice; key frames of an upload and the beacon's drop (sheet) | chosen (round 79 A.3-A.5 ok) |
| [concept/l41-objects-final-r79-a.gif](concept/l41-objects-final-r79-a.gif) | At 1×: an upload (the hatch shot open, the ring filling while the ship hovers, paused as it drifts out, the file in) beside the beacon's drop (the retro burn, the landing, the strobe, the cap opening) | chosen (round 79 A.3-A.5 ok) |

The level needs besides (M10's Level 41 part; the units' proposals are in their pages' round-72 concept, the moods
in [the world](../../../world/jovian/README.md)'s):

- the round's captures (step G5, below; the 1:1 crops of a dome's rim and the livery split are on the
  [Shield Pylon](../../../enemies/ground/shield-pylon/README.md#concept-art)'s page, the Halo's halt on the
  [Halo Platform](../../../enemies/ground/halo-platform/README.md#concept-art)'s).

**Game captures (M10 part G, round 79, step G5)**, from the game itself at `9ce1ebb6` (the detached capture worktree),
Level 41 at medium with the balance plan's L41 fit and Rook's Mortar, flown by the content autopilot through the debug
`--replay` option, on a private Xvfb display; the recipe in [concept/prompts.md](concept/prompts.md#level-fight-r79-a).

| File | What | Status |
|---|---|---|
| [concept/level-fight-r79-a.mp4](concept/level-fight-r79-a.mp4) | The whole level in real time with the game's sound and music, from the level's start to the debrief (200 s, 44 MB): the ice field, the causeways, three gate halts with their domes, uploads and blast doors, the inner court's vault, the Halo's halt and its barrier dropping, the Silence line, Helix HQ and the empty berth | superseded (round 79 L.1 change: the tanks, the squares riding with the ship and the abrupt ending fixed in R79a/R79c; the fixes accepted in round 80 H.2) |
| [concept/level-fight-r79-a.png](concept/level-fight-r79-a.png) | Its poster: Gate Two's halt (the whole screen with the HUD) | superseded (round 79 L.1 change: the tanks, the squares riding with the ship and the abrupt ending fixed in R79a/R79c; the fixes accepted in round 80 H.2) |
| [concept/gate-capture-r79-a.png](concept/gate-capture-r79-a.png) | Gate Two's halt (2× of the play field): three pylons on the wall feeding their domes over the covered units, an archive core in its uplink ring | chosen (round 79 A.2: the gate, pylons and domes right; the tanks' parking fixed in R79c) |
| [concept/doors-capture-r79-a.png](concept/doors-capture-r79-a.png) | Gate One's blast doors parting after its last pylon falls, four frames 0.3 s apart (2×) | chosen (round 79 A.6 ok) |
| [concept/core-capture-r79-a.png](concept/core-capture-r79-a.png) | Gate One's archive core (2×): open in its ring, the ship hovering while the file uploads, the file in (the gold ring) | chosen (round 79 A.3 ok; the cores scroll with the ground since R79a `0685e467`) |
| [concept/vault-capture-r79-a.png](concept/vault-capture-r79-a.png) | Varga's lab vault in the cracked-open research wing (2×), sealed and blown open with its pickup | chosen (round 79 A.4 ok) |
| [concept/beacon-capture-r79-a.png](concept/beacon-capture-r79-a.png) | The CDF beacon's drop on the causeways (2×): falling on its retro burn, then the armour patch it releases | chosen (round 79 A.5 ok) |
| [concept/backdrop-capture-r79-a.png](concept/backdrop-capture-r79-a.png) | The backdrop in the game (1×): the ice field, the causeways, the citadel's inner decks, Helix HQ | chosen (round 79 A.6 ok) |
| [concept/berth-capture-r79-a.png](concept/berth-capture-r79-a.png) | The empty berth at the level's end (2×), the Ascendant's drive glow climbing away in its well | chosen (round 79 A.6 ok) |
| [concept/gate-halt-capture-r80-a.mp4](concept/gate-halt-capture-r80-a.mp4) | Round 80, a fresh real-time run with sound after round 79's fixes: Gate One's halt (20 s), the Warden Tanks parked in pairs on the apron below the gateway ([recipe](concept/prompts.md#level-clips-r80-a)) | chosen (round 80 H.2 ok) |
| [concept/ending-capture-r80-a.mp4](concept/ending-capture-r80-a.mp4) | Round 80, the same run: the level's last 25 s with sound, "The Empty Berth" and Varga's and Okafor's closing lines | chosen (round 80 H.2 ok) |

## Implementation

- [x] Level data `data.yaml` (M10's Level 41 part) with the sections, waves, halt cycles, ground targets, radio,
      budget, threat profile, briefing, backdrop and music; three gate sections `holds: pylons` and the Halo's
      `holds: halo` with its checkpoint (none on hard) (step G2, 2026-11-02: `holds: gate-1` … `gate-3` and
      `holds: halo-platform`, each gate's `halt_cycle`; loaded and checked by content's `FortressChecks`)
- [x] Reference DPS 164 at L41 and the Act 6 hand plan's Level 41 fit (the Mk V+ and the IFF spoofer; M10 part A)
      before the data; the act HP factor for the returning `medium`+ units listed in *Waves*
- [x] The pylon gates per the [Shield Pylon](../../../enemies/ground/shield-pylon/README.md#the-pylon-gates-level-41):
      the halt, the clock standing while a pylon stands, the pylons asleep until the halt, the halt cycles cut at the
      last pylon, the blast doors opening, `PYLONS n / m` and the gate pips
- [x] The [Halo Platform](../../../enemies/ground/halo-platform/README.md)'s held arena at (240, 190), its checkpoint,
      the armour patch at its death
- [x] **The uplink** (new, small): the archive core's hatch (a ground trigger), the core's 40 px uplink zone with a
      2.0 s cumulative timer and its ring, Lancer's ship only, a core lost off the bottom edge; `FILES n / 3` on the
      tracker's second line; the secondary judged at the end
- [x] The three-way war's prey links (the Lampreys of t=24 and t=85.5 latching the Gunships of t=21.5 and t=83); a
      prey kill by the Vrell pays nothing
- [x] The level to the Act 2+ rules (`ActFloorTest`, `PacingTest`, the halts with their cycles; the end's calm named);
      the Gunships' Darts counted
- [x] The liveries: the pylons, the Halo and the citadel's structures elite; the returning units regular (no new
      frames); the Graft Turrets hybrid
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`), with Rook and without, with 0–3 files;
      the halt and file lines as events; the Silence line never stale (steps VOX-G and G4: `RadioTimelineLevel41Test`;
      left for step H4b's radio work: Varga's first file line, stale behind Gate One's halt line, takes that line's
      place; voiced, Okafor's t=50 can start ≈ 1.3 s late)
- [x] Threat profile, Varga's four intel lines and Okafor's teaser fitting the intel panel
- [x] Briefing: both pages, each one screen, with their images
- [x] Typical haul within ±5 % of budget(41) with the level's `bounty_scale`
- [x] The lab vault secret (20 HP, hard 28, crate 100), the shield cell, the overdrive, the two armour patches
- [x] Music: track 15's stems by section, the motif stem out after the Silence line; the `callisto-fortress` ambience with the
      fortress machinery; the blast doors', hatch's, uplink's and vault's sounds
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on every difficulty
      (`--escort none` too) and takes the files (it hovers over an open core in a halt)
- [x] Production art and the round's captures
- [x] Round 79 (step R79c): no two Warden Tanks overlap (1 s apart on the causeway and the ring road) and the gates'
      pairs park on the apron before the gateway, never on or across the wall (`park: true`, the simulation's
      `WalkPath.parked`; `WalkerParkTest`, `Level41GroundTest`)
- [x] Round 79 (step R79c): the ending: the theme out from t=141, the berth held 13 s (`The Empty Berth`, 1 px/s),
      Varga's archive line and Okafor's closing line (voiced, `tools/art/voice.py`), the level's end at t=158
      (`RadioTimelineLevel41Test`, `PacingTest`)

## Open questions

- Closed at the build: Act 6 is built and shipped in `v0.4`; the choices left to the build are settled in this document's Decisions and the built data, and the design stands as approved in [round 72](../../../concept-rounds/round-72/README.md).

## Decisions

- 2026-10-31 (Act 6 design phase, step `m10-d-levels-3`; decided 2026-10-31 (round 72)): drafted from the
  roster row, the gap analysis (§0, §1.1, §2.4, §3: Q3 a, Q7 b, Q8 a, Q9 a, Q10 a, Q12 a; J1, J5, J8, J10), the Shield
  Pylon's, Halo Platform's and Warden Tank's documents (the source of truth for the units and the gate rule) and Level
  29's rescue halt and Level 35's draft as the models.
- 2026-10-31: three pylon gates (2, 3, 3 pylons; easy 2 each) and the Halo as a fourth held section with its own
  checkpoint; the Halo comes last, so the three files (the deal's three parts) come before the Silence and the main
  core under the Halo names it.
- 2026-10-31: the liveries stated (the gap note's open item): the pylons, the Halo and the citadel's structures elite
  (gloss black and heavy gold); the Warden Tanks, Rail Bunkers, Sentinel Towers, SAM Nests and the air units regular
  (Act 5's gunmetal, no re-render); the Graft Turrets hybrid.
- 2026-10-31: the Silence is named whatever the `FILES` count (the story does not hang on a secondary); the files'
  uplink is a new small mechanic (hover 2.0 s within 40 px); the cores never sit under a dome.
- 2026-10-31: ≈ 118 enemies a minute (379 with one typical halt each, 68 of them on the ground); every wave from the
  front (the roster); the Vrell's Lampreys hunt two Gunship columns (Q3 a); the tiny units are the Darts, Skitters and
  Motes (J5).
- 2026-10-31: Level 41's hand `bounty_scale` is **0.30**; the IFF spoofer (fitted at this visit by the hand plan) fools
  the SAM Nests, Sentinel Towers, Rail Bunkers, Warden Tanks and the Halo, not the Graft Turrets.
- 2026-10-31 (decided here, trivia): the secret is Varga's old lab vault; Okafor's "running for orbit" at the empty
  berth hands over to Level 42; the music is track 15 with the motif stem out after the Silence (the audio step
  confirms).
- 2026-10-31, design phase (step m10-d-act; decided 2026-10-31 (round 72)): **Vorne is silent in this level** (the story step's
  Vorne: L40–L41 silent, the files speak for him); his line after the Silence is dropped and the Silence line plays
  alone. The ambience is D-audio's `callisto-fortress` bed.
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): **Q12 a** (the Silence named once
  in play, by Varga, over the `FILES` secondary; b only the Level 42 briefing and c only the act outro rejected),
  **J16 kept** (the uplink: the ship's centre within 40 px of an opened archive core for 2.0 s in total; shooting the
  hatch only rejected), **J8 kept** (the Halo's checkpoint at its halt, none on hard) and Q9 a (the fortress's
  emplacements elite, the returning units regular). The document approved as drafted (D.7; `design: approved`).
- 2026-11-01 (user, round 77 F.1 redo of Level 39's opening; step ART-E2): **no Jupiter on top-down ground levels**:
  the level is over Callisto's ice from t = 0, Jupiter's disc out of the backdrop plan (section 1's `deep` and the
  Concept art list); the fleet's lights in section 1 go with it (a `deep` layer the ground covers). Jupiter stays in
  the briefing images and the space levels (36–38, 40, 42).
- 2026-11-02 (M10 part G, step G2; trivia, decided 2026-11-02 (round 79 closed)): the data from the approved tables and
  ART-G2's proposal: its sections as they are (a moving section's 2 s ease included: Gate One halts at scroll 5,460,
  the Layout table's 5,440 leaves the ease out; the backdrop needs no rerun); the pylons asleep until a step before
  their gate's end and the Halo arriving there (Level 29's clamp-gun rule); each gate's typical halt in its
  `halt_cycle.typical` (8, 11, 11, 16 s: the density and the typical haul count the cycle waves before it); the Warden
  pairs entering about 3 s before their gate and parking in the gateway by its halt (walker waves on ART-G2's roads);
  easy's dropped pylon the bastion pylon C of Gates Two and Three; the Skitter swarms flown as streams (a Skitter has no
  flock) and the Chimera swarms on a route; the leaders on the nine moving V-wings (a halt cycle's waves have none);
  the HQ nest 0.6 s earlier (t 137.4) for the end's 4 s of calm; the CDF beacon without the friendly marker; the
  vault 36×36 (ART-G1's frame). `bounty_scale` 0.30 kept: the typical haul −0.5 % (`levelReport`, before tuning).
- 2026-11-02 (M10 part G, step G4; trivia, decided 2026-11-02 (round 79 closed)): the game. Five timed lines retimed
  for the 1 s rule on the gates' clock (*Radio chatter*: t=11 → 12.5, 33 → 35, 48 → 50, 60 → 62, 100.5 → 105); a gate's
  ease out counts at the full rate when the radio weighs an event line, and a timed line due in the very step counts
  (no event line takes that gap). The Halo held: no bar, no warning, klaxon, banner or boss theme, no screen flash at its
  death; its death the eye's blowout, the standing turrets cooking off 0.15 s apart (a medium blast each) and the large
  vacuum blast one gap after the last; the grind bed pitched up 5 % a turret lost. The gate pips the three pylon gates
  (the Halo's halt shows them green, no count). The blast doors only on the pylon gates; after the Halo's checkpoint
  the three stand open. The dome's hum while a gate's halt holds a standing dome; the uplink's chatter while a file
  uploads. The CDF beacon falls over 1.2 s from 2x on its retro burn, no drone and no marker.
- 2026-11-02 (M10 part G, step G5): the level built (steps G1a, G1b, G2, G3, ART-G1, ART-G2, AUD-G, VOX-G, G4) and
  captured in the game for round 79: the whole level in real time with sound (`concept/level-fight-r79-a.mp4`) and
  stills (*Concept art*); every Implementation item ticked (`implementation: done`). The doc follows the measures: the
  halts ≈ 10, 10, 9 and **13 s** (the Halo; the paper's 16 s from its HP) at medium, the Silence line 7.5 s voiced (no
  pause added before "the Silence"). Decided here, no round row (trivia; the steps' items then marked for round 79): the
  `FILES` secondary judged only at the end (no early fail) and a retry from the Halo's checkpoint on an empty field
  (G1a); Gate One at scroll 5,460, the typical halts in the data, easy dropping pylon C at Gates Two and Three, the
  Skitter swarms as streams, the CDF beacon unmarked (a ground beacon, no craft), the Halo listed as a set piece in the
  databank (G2); the drive glow through the berth's well and the parked Talons dimmed (ART-G2); the uplink's teal
  (70, 235, 220) and the beacon's retro burn with no parachute in vacuum (ART-G1); the five retimed lines and the
  never-stale Silence line as built (G4). Left: Varga's first file line (step H4b), "Vrell" heard close to "rail" in
  Varga's second file line (a voice retake when the cast is next rendered), the Halo's turrets small at 1:1 (the art row
  in round 79 asks).
- 2026-11-02 (step H4b, decided 2026-11-03 (round 80 H.2)): Varga's first file line fixed by a game rule, not a
  retime: a file uploaded as its gate's halt line opens (within 2 s) takes that line's place (Gate One's ≈ 8 s halt has
  no room for both before the next timed line); Okafor's t=50 to 51 (≈ 1.3 s late voiced in a short halt).
- 2026-11-02 (M10 part I, step I2; decided 2026-11-03 (round 80 H.2)): the plan's fit keeps Rook's Autocannon (not the Mortar):
  the autopilot earns about 1,100 more at medium, `BeamEraHp` 1.42× → 1.33×, the gates' halts as before (medium
  9.7-14.2 s); the autopilot takes the lab vault's crate (SecretPlay: it lines up under the vault and climbs to the
  crate; before, the crate fell past it).
- 2026-11-02 (round 79, the user on the recording: the tanks on top of each other at 0:27 and 0:44, one driving over
  the gate, "the tanks are on top of the gate"; step R79c): the causeway pair and the ring-road convoy 1 s apart (were
  0.6 s); the gates' pairs re-pathed and parked on the apron before each gateway (a new wave key `park: true`: a walker
  stops at its path's end; before, past its last point it walked straight on through the halt and over the wall),
  ≥ 72 px apart and clear of the wall, still under their domes (Gate One's pair keeps G2's entry, t=40.27: entered
  earlier, the campaign playthrough's medium act end drifted past its −15 % bound, −15.1 %; now −14.9 %, HEAD −14.2 %). The art's own park places (the proposal yaml's
  `covered`, inside the gateway) are left as ART-G2 wrote them; the data's are the game's.
- 2026-11-02 (round 79, the user: "the ending is quite abrupt with no radio"; step R79c): a closing run-out over the
  empty berth: the theme out at t=141, a new last section `The Empty Berth` (t=145–158, eased to 1 px/s so the art's
  berth stays whole on the screen), Okafor's orbit line and Rook's answer 3 s earlier (129, 136) and two new lines,
  Varga's "Main archive copied. All of it is on its way to the carrier." (t=142.5, her reaching the main archive) and
  Okafor's "Good work, Lancer. All wings, back to the carrier." (t=150.1), voiced (read-back clean; Whisper hears
  "Lancer" as "Glancer", as elsewhere); pending the user's ear.
- 2026-11-02 (user, round 79 closed; page answers and notes): **L.1 change**, **A.2 change**, every other art row **ok**
  (A.1, A.3-A.6), every sound as recommended except **S.8 b** and **S.10 b** (S.1-S.7, S.9, S.11 a; the eleven unchosen
  variants moved to the sfx part's `concept/rejected/`, CREDITS.md paths updated; the picks play since R79a `0685e467`).
  L.1 (verbatim): "at 0:27 in part 1, the tanks are on top of each other and again at 0:44 and one drives over the gate.
  Isn't the gate a raised structure? What is the square at the bottom that scrolls with the ship in part 2? A second one
  joins after gate 2 and a third at gate 3. Are those the files? It looks very strange and they even pass underneath
  turrets. The ending is quite abrupt with no radio." A.2: "The pylons and the domes look right, but the tanks are on top
  of the gate. That doesn't look right." A.3: "Yes, but they float with the ship, that looks very odd. Why aren't these
  pickups?" (the cores' ground scroll: the simulation scrolled only open cores, so a core floated with the ship until it
  opened: a bug). H.1's note (Level 40's opening supply drone "marked friendly again") brought the user's rule below. Built since the round opened: **R79a
  `0685e467`** (the archive cores scroll with the ground from the start; S.8 b and S.10 b in the game); **R79c
  `f5ff579c`** (the gate pairs park on the apron below each gateway, spaced; the convoys apart; the ending: the theme out,
  "The Empty Berth", Varga's and Okafor's closing lines; the friendly-brackets rule applied). **The user's rule
  (2026-11-02): "Only ships that can be hit by the player and must survive get the friendly brackets."** Kept on Level
  29's Stormhawks, Level 36's drop craft and Level 37's shuttles; removed from the supply drones, Level 38's canister and
  Level 42's frigates (the rule in the [HUD](../../../ui/hud/README.md#act-6-additions-draft)). The art (objects A.3-A.5, backdrop A.6, the pylons and Halo of A.1/A.2)
  is `chosen`; the level's own recording (`level-fight-r79-a.mp4`) stays `proposed`: it shows the old tanks and ending, to
  be shown again in round 80, so `art` stays `proposed` until then.
- 2026-11-03: round 80 H.2 **ok** (the user): the tanks parked in pairs and the ending ("The Empty Berth", the closing lines) look right; the round 80 recordings `chosen`, round 79's level recording superseded by them, `art: chosen`.
