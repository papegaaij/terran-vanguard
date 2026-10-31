---
title: Level 41 – Callisto Fortress
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/ground/shield-pylon, ../../../enemies/ground/halo-platform, ../../../enemies/ground/warden-tank, ../../../enemies/ground/rail-bunker, ../../../enemies/ground/sentinel-tower, ../../../enemies/ground/sam-nest, ../../../enemies/ground/graft-turret, ../../../enemies/air/talon, ../../../enemies/air/chimera, ../../../enemies/air/gilded-gunship, ../../../enemies/air/hornet, ../../../enemies/air/lamprey, ../../../world/jovian, ../../../player/systems, ../../../ui/hud, ../../../story/characters/varga, ../../../systems/economy]
updated: 2026-10-31
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
gone to orbit. About 146 s of level clock plus four halts (≈ 46 s typical); ≈ 118 enemies a minute. Density 5.

**Status: approved 2026-10-31 (round 72, D.7)** (drafted in the Act 6 design phase, step `m10-d-levels-3`, 2026-10-31). Written from the act roster, the gap analysis's recommended answers (**Q12 a** the files in play, **Q3 a**
the three-way war, **Q9 a** the liveries, **Q10 a** the Level 41 mood, **Q7 b** and **Q8 a** the L41 visit's
generator and IFF spoofer; judgement calls **J1** the reference DPS 164, **J5** no new tiny unit, **J8** the Halo
Platform's r07 design, **J10** Okafor on the carrier, all decided 2026-10-31 (round 72)) and the wave 1 and 2 documents. The unit
documents own the pylons, the domes, the gates' rule and the Halo; this document places them and owns the timeline,
the files, the radio and the economy. Every number is a hand-written first draft at medium for M10's Level 41 part to
measure with `levelReport`; there is no `data.yaml` yet.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. **Draft texts**:

> **Commander Okafor:** "Helix HQ is under Callisto's citadel. Shield pylons hold its gates: break them and the
> gates open. Vorne's flagship is docked inside. Get there before it leaves."
>
> **Dr. Varga:** "I worked in that building. Its archive cores hold Vorne's files: open one and fly over it, and my
> spike does the rest. The Halo at the centre only fires on the side that faces you."

*Images* (production art in M10's Level 41 part; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-41-callisto-citadel`: a tactical chart of the citadel from above: the causeways across the ice,
  three gates as gold rings (the pylons), the Halo at the centre, the dock with the Ascendant's gold silhouette in its
  berth, the wing's route in white.
- Varga's page `level-41-helix-hq`: her old floor plan of Helix HQ (a Helix badge photo clipped to its corner, her
  own, 2179), the three archive cores circled in teal, a hand-drawn arrow to the main core under the Halo.

*Hangar teaser* (shop screen before L41; Okafor, not voiced):

> **Commander Okafor:** "The fortress is next. Anything that hits the ground, fit it. The Crucible is ready."

## Threat profile

Draft; directions are counted by each wave's `from` (the intel's rule): every wave from the front (the roster's one
direction); the ground guns fire in any direction.

| Field | Value |
|---|---|
| Dominant layers | `ground` (the pylons, the Halo Platform, Warden Tanks, Rail Bunkers, Sentinel Towers, SAM Nests, Graft Turrets on the citadel's decks), `air` (Talons, Chimeras, Gilded Gunships and their Darts, Hornets, the Vrell's Lampreys and popcorn) |
| Attack directions | front 100% (the ground's fire from every side as the ship passes) |
| Density | 5 |
| Recommended traits | `anti-ground` (the gates, the hardened Rail Bunkers, the Halo), `area` (the guns under a dropped dome, the Halo's ring); `piercing` helps on the Halo's ring |
| Hazards | Rail lines (the Rail Bunkers and the Halo's rail turrets), Sentinel sweeps, SAM homing diamonds; the domes (no damage: they protect) |
| Boss / mid-boss | none (the Halo Platform is a held set piece, not a boss) |
| Sensor-suite detail | **none**: Callisto, the citadel, `ground` and `air`, front. **L1**: + density 5, hazards "rail lines" and "dome gates", the objective `BREAK THE GATES`, the secondary `FILES`. **L2**: + Shield Pylon, Halo Platform, Warden Tank, Rail Bunker, Sentinel Tower, SAM Nest, Graft Turret, Talon, Chimera, Gilded Gunship, Hornet portraits. **L3**: + `anti-ground` and `area` highlighted; the timeline strip marks the three gates (42, 74, 98 s) and the Halo (112 s); 1 secret. |

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
blue-grey ice, the black citadel, red and gold lights, the Sun low with long shadows, Jupiter's banded disc huge at
the top-left; the citadel's decks a step lighter than the units on them. Motion budget: the domes' slow shimmer and
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
[retry](../../../systems/retry/README.md) rules). Typical halts at medium with the plan's fit: Gate One ≈ 8 s (two
pylons, 480 HP), Gate Two and Gate Three ≈ 11 s (three, 720 HP), the Halo ≈ 16 s (1,560 HP), all at the boss rule's
0.6 × the reference of 164 between the halt cycles' waves: **≈ 46 s** of halts.

**Rook and the escort.** Rook flies his slot with his **Mortar** (the plan's L41 fit; the hardened bunkers and the
pylons are its work); his pick skips covered units while an uncovered target is on the screen (the dome rule) and
takes the pylons first in a halt. With `--escort none` the level is the same without him and his lines.

| Section | t (s) | Scroll (px) | Speed (px/s) | Layers and content | Purpose |
|---|---|---|---|---|---|
| 1. The Ice Field | 0–20 | 0–2,800 | 140 | `deep`: Jupiter's disc, the fleet's lights. `ground`: Callisto's cratered ice, a crater-rim outpost (t≈8), the outer causeway's gatehouse (t≈15). | Talons, Chimeras and popcorn; the first SAM group and a Graft nest. |
| 2. The Outer Causeways | 20–42 | 2,800–5,440 | 120 | `ground`: two causeways across a crevasse field to the citadel's outer wall; a Sentinel Tower on the wall (t≈32). | Warden Tanks on the causeways; the Lampreys on the Gunships; the first gate ahead. |
| 3. Gate One | 42–44 | 5,440–5,560 | 120 → 0 | `ground`: the outer gate: two pylons on its towers, their domes over a Warden pair in the gateway and a SAM group on the right bastion; archive core A in the courtyard at (240, 400). | The first halt (≈ 8 s); the first file. |
| 4. The Hangar Decks | 44–74 | 5,560–9,040 | 0 → 120 | `ground`: hangar roofs and launch decks, open bays with parked Talons (scenery), Rail Bunker pairs (t≈50, 70), a Graft nest (t≈58), a SAM group (t≈64). | Hardened bunkers and regrowing turrets. |
| 5. Gate Two | 74–76 | 9,040–9,160 | 120 → 0 | `ground`: the hangar gate: three pylons, their domes over the Rail Bunker pair, a Warden pair and a Graft nest of 3; archive core B at (120, 390). | The second halt (≈ 11 s); the second file. |
| 6. The Citadel Ring | 76–98 | 9,160–11,680 | 0 → 120 | `ground`: the ring road round the citadel's core: a Sentinel Tower (t≈80), a SAM group (t≈84), a Warden convoy (t≈86), a Warden pair driving to the inner gate (t≈96). | The heaviest ground stretch. |
| 7. Gate Three | 98–100 | 11,680–11,800 | 120 → 0 | `ground`: the inner gate: three pylons, their domes over the Warden pair, a Rail Bunker and a SAM group; archive core C at (360, 390). | The third halt (≈ 11 s); the third file. |
| 8. The Inner Court | 100–112 | 11,800–13,120 | 0 → 120 | `ground`: the court before Helix HQ's tower: a Graft nest (t≈104), Varga's old research wing with its lab vault (t≈104, the secret), a Sentinel Tower (t≈108); the Halo's rim enters at the top. | The approach to the Halo. |
| 9. The Halo | 112–114 | 13,120–13,240 | 120 → 0 | `ground`: the Halo Platform on its mount over the HQ's main core, its core at (240, 190) ([held arena](../../../enemies/ground/halo-platform/README.md#behaviour)); nothing else on the ground. | The held centrepiece (≈ 16 s), the checkpoint; the main core and the Silence. |
| 10. Helix HQ | 114–146 | 13,240–17,580 | 0 → 140 | `ground`: HQ's roofs and comm masts, a Rail Bunker pair (t≈120), a SAM group (t≈126), a Sentinel Tower (t≈132), a Graft nest (t≈138), then the dock: the Ascendant's **empty berth**, its clamps open, its gold drive glow climbing away in the `deep` layer. | The run to the dock; the level ends at t=146 over the empty berth. |

The level ends at t=146 (≥ 4 s of calm after the last wave of t=136 and the last ground target of t≈138). Its empty
stretches over 2 s: the end's calm only; the halts carry their cycles' waves.

## Waves

Formation names from the [formation vocabulary](../../../enemies/README.md#formation-vocabulary). The **act HP
factor** for the returning `medium`+ units (the proposed reference **164** at Level 41 against the unit's first level,
J1): Gilded Gunship ×164/124 (88 → 116; its [Act 6 shield](../../../enemies/README.md#enemy-shields) 24 → 32), Hornet
×164/128 (96 → 123, shield 24 → 31), Warden Tank (its [Act 6 section](../../../enemies/ground/warden-tank/README.md):
hull 114, turret 36), Rail Bunker ×164/136 (100 → 121), Sentinel Tower ×164/132 (120 → 149). The new units fly their
pages' numbers (the Shield Pylon 240, the Halo Platform 1,560). The `small` units (Talon with its Act 6 shield,
Chimera, Lamprey, SAM Nest, Graft Turret) keep their pages' HP with the beam era's ×2, the `tiny` ones (Darts,
Skitters, Motes) 1 HP. Every number is a hand estimate for `levelReport`.

- **Density and pacing** (the Act 2+ floor): **379 enemies at medium** over ≈ 192 s (146 s of clock and the four
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
- **The three-way war** (Q3 a): the Lamprey streams of t=24 and t=85.5 are **hunters** of the Gunship columns of t=21.5
  and t=83 (prey): they latch on the Gunships (Level 33's rule: the shield empties, the guns stop, it drifts down), and
  a Gunship the Vrell kill pays nothing ([the rule](../../../enemies/README.md#the-three-way-war)). The Vrell eat the
  fortress too, in the backdrop: teal growth on the citadel's outer wall where the Vrell landed after Aurelia.
- **Directions**: every wave from the front (the roster); no edge warnings needed.
- **Totals**: Talon 83 (11 leaders) · Mote Swarm 84 · Chimera 46 · Skitter 42 · Gilded Gunship 6 (+ 36 Darts) ·
  Lamprey 8 · Hornet 6; the ground's 68 under *Ground targets*.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 6.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 9 | 1 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Over the crater-rim SAM group |
| 11.5 | 1 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 14 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 16.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 19 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 21.5 | 2 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | ≈ 12 Darts; prey of the Lampreys of t=24 |
| 24 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front | Vrell hunters (latch) of the t=21.5 Gunships; Rook's line at t=25 |
| 26.5 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The first Warden pair on the causeway below |
| 29 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 31.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | |
| 34 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 36.5 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 39 | 2 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | Holding over the gate as the halt begins |
| H1+1 … | 3 | (Gate One's halt cycle) | | 10 typical | front | Mote Swarm 6, Talon line 4 (Chimera swarm 6 at H1+9 if the gate still stands) |
| 46 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 48.5 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 51 | 4 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Over the first Rail Bunker pair |
| 53.5 | 4 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 56 | 4 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 58.5 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 62 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 65 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | |
| 68 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 71 | 4 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | The last before Gate Two |
| H2+1 … | 5 | (Gate Two's halt cycle) | | 17 typical | front | Skitter swarm 6, Talon V-wing 5, Mote Swarm 6 |
| 78 | 6 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 80.5 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 83 | 6 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | ≈ 12 Darts; prey of the Lampreys of t=85.5 |
| 85.5 | 6 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front | Vrell hunters (latch) of the t=83 Gunships |
| 88 | 6 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 91 | 6 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 94 | 6 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | The last before Gate Three |
| H3+1 … | 7 | (Gate Three's halt cycle) | | 17 typical | front | Mote Swarm 6, Talon V-wing 5, Chimera swarm 6 |
| 102.5 | 8 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 105 | 8 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 107.5 | 8 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | |
| 110 | 8 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | The last before the Halo |
| H4+2 … | 9 | (the Halo's halt cycle) | | 16 typical | front | Mote Swarm 6, Talon line 4, Skitter swarm 6 |
| 116 | 10 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 118.5 | 10 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | |
| 121 | 10 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | ≈ 12 Darts, out of the HQ's hangar |
| 124 | 10 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 127 | 10 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 130 | 10 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 133 | 10 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 136 | 10 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The last wave; the end's calm from t≈141 |

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 1 | [SAM Nest](../../../enemies/ground/sam-nest/README.md) group ×3 on the crater-rim outpost (t≈8) | Bounty 15 each; homing diamonds, shootable |
| 1 | [Graft Turret](../../../enemies/ground/graft-turret/README.md) nest ×4 on the gatehouse (t≈15) | Bounty 15 once each, the root 5; a turret regrows 5 s after its death unless its root dies |
| 2 | [Warden Tank](../../../enemies/ground/warden-tank/README.md) pair on the left causeway (t≈24) | Bounty 28 each (in parts); bursts back up the screen as they pass |
| 2 | [Sentinel Tower](../../../enemies/ground/sentinel-tower/README.md) on the outer wall (t≈32) | Bounty 40; its sweeps |
| 2 → 3 | Warden Tank pair driving into the gateway (t≈38), parked under Gate One's dome A from the halt | Covered until pylon A falls |
| 3 | **Gate One**: [Shield Pylon](../../../enemies/ground/shield-pylon/README.md) ×2 on the gate towers (upper corners of the halted screen); dome A over the Warden pair, dome B over a SAM Nest group ×3 on the right bastion | Bounty 45 each; the halt ends with the second |
| 3 | Archive core A under its hatch (30 HP) in the courtyard, (240, 400) | `FILES` 1 of 3 |
| 4 | [Rail Bunker](../../../enemies/ground/rail-bunker/README.md) pair (t≈50) | Bounty 45 each; hardened; never both lines on the ship's row |
| 4 | Graft Turret nest ×4 on the launch-deck edges (t≈58) | As above |
| 4 | SAM Nest group ×3 on a hangar roof (t≈64) | Bounty 15 each |
| 4 → 5 | Rail Bunker pair (t≈70), covered by Gate Two's dome A from the halt | Covered until its pylon falls |
| 5 | **Gate Two**: Shield Pylon ×3 (two on the gate towers, one on the left bastion); dome A over the Rail Bunker pair, dome B over a Warden Tank pair in the gateway, dome C over a Graft Turret nest ×3 | Bounty 45 each; the halt ends with the third |
| 5 | Archive core B (30 HP hatch), (120, 390) | `FILES` 2 of 3 |
| 6 | Sentinel Tower (t≈80) · SAM Nest group ×3 (t≈84) · Warden Tank convoy ×3 on the ring road (t≈86) | Bounties 40, 15 each, 28 each |
| 6 → 7 | Warden Tank pair driving to the inner gate (t≈96), parked under Gate Three's dome A | Covered until its pylon falls |
| 7 | **Gate Three**: Shield Pylon ×3; dome A over the Warden pair, dome B over a Rail Bunker, dome C over a SAM Nest group ×3 | Bounty 45 each; the halt ends with the third |
| 7 | Archive core C (30 HP hatch), (360, 390) | `FILES` 3 of 3 |
| 8 | Graft Turret nest ×4 (t≈104) · Sentinel Tower (t≈108) | As above |
| 8 | Varga's lab vault (`ground` trigger, 20 HP, hard 28; t≈104, x≈410) | The secret |
| 9 | **[Halo Platform](../../../enemies/ground/halo-platform/README.md)**, its core at (240, 190) | Bounty 120 whole (turrets 10 each, the core 60); the halt ends with the core |
| 10 | Rail Bunker pair (t≈120) · SAM Nest group ×3 (t≈126) · Sentinel Tower (t≈132) · Graft Turret nest ×4 (t≈138) | As above |

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

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Callisto's citadel, Lancer. Shield pylons hold its gates. Break them and the gates open." |
| t=11 | Varga | "I worked in this building. Open an archive core and fly over it: my spike does the rest." |
| t=25 | Rook | "Lampreys on the Gunships. The bugs are eating the fortress too." |
| t=33 | Okafor | "Tanks on the causeway, and a gate ahead. Pylons first, then whatever they cover." |
| Gate One's halt (`halt`) | Varga | "Two pylons, two domes. Nothing under a dome can be hurt. The core's in the courtyard." |
| File 1 in (`file`) | Varga | "First file. Shipping lists: Helix biotech going out through the gate. For years." |
| t=48 | Okafor | "Rail bunkers on the hangar decks. Anti-ground if you have it, Rook's mortar if you don't." |
| t=60 | Varga | "Graft turrets on the decks. Kill the root, or they grow back." |
| Gate Two's halt (`halt`) | Okafor | "Three pylons this time. Any order, but fast." |
| File 2 in (`file`) | Varga | "Second file. A contract. Vorne's name, and a Vrell sigil beside it." |
| t=87 | Rook | "Same tanks as Mars. Nicer paint." |
| Gate Three's halt (`halt`) | Okafor | "The inner gate. The Halo is behind it." |
| File 3 in (`file`) | Varga | "Third file. The price was passage. The Vrell wanted through the gate. To us." |
| t=100.5 | Varga | "The Halo. Its guns fire only on the side that faces you. Kill three and the core opens." |
| Lab vault destroyed (event) | Varga | "My old lab. They kept the vault. Open it, Lancer." |
| The Halo's halt (`halt`) | Rook | "A merry-go-round with guns. I hate merry-go-rounds." |
| Halo core destroyed (`halo-destroyed`, `delay: 2`, never stale) | Varga | "The Halo fed the main core. I'm in. They were not invading. They were running. Vorne's files call it the Silence." |
| t=132 | Okafor | "The Ascendant is off its berth. He's running for orbit." |
| t=139 | Rook | "Then we chase him. Like always." |

Timeline check (text model): t=1 closes ≈ 9.8, t=11 ≈ 19.9, t=25 ≈ 31.8, t=33 ≈ 41.2, before Gate One's halt at
t=42. In the halts the clock stands, so the halt and file lines play in real time (a file line waits behind its
halt's line; a file taken after its gate opens is spoken on the move, ≤ 6 s late or dropped). t=48 ≈ 57.0, t=60 ≈ 66.6,
t=87 ≈ 91.3. Varga's t=100.5 closes ≈ 109.4; the vault (in reach t≈104–110) is spoken by ≈ 115 at the latest, before
the Halo's halt line. The Halo's story line (≈ 11.0 s) starts 2 s after the core's death and runs ≈ 11 s into the HQ
run (to clock ≈ 124 with a typical halt); Okafor's t=132 (≈ 138.1) and Rook's t=139 (≈ 143.2)
follow, before the level's end at t=146. M10's Level 41 part checks it with `RadioTimelineTest` (with and without Rook,
with 0–3 files, short and long halts).

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
level's end (his secret is out). The debrief plays the `mission-complete` jingle (23). Ambience: the `callisto-fortress` bed
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

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Talon 83 × 12 + Mote Swarm 84 × 2 + Chimera 46 × 14 + Skitter 42 × 5 + Gilded Gunship 6 × 28 + the Gunships' Darts 36 × 2 + Lamprey 8 × 12 + Hornet 6 × 26 (one typical halt each) | 7,896 | 4,738 |
| Squad leaders: 11 × 13 extra (perfect run only) | 450 | 0 |
| Ground targets: Shield Pylon 8 × 45 + Halo Platform 120 + Warden Tank 11 × 28 + Rail Bunker 7 × 45 + Sentinel Tower 4 × 40 + SAM Nest 18 × 15 + Graft Turret 19 × 15 | 5,719 | 4,575 |
| Graft Turret roots: 19 × 5 (perfect run only) | 299 | 0 |
| Secret: Varga's lab vault (hidden crate) | 1,049 | 524 |
| Secondary: `FILES` 3 / 3 | 1,049 | 524 |
| **Total** (bounty scale 0.30) | **16,462** | **10,361** |
| Budget(n) = the typical haul's target; typical −1.2 %, perfect 1.57 × budget | | 10,482 |

Longer halts add their cycles' waves (the perfect run on a slow halt earns more). **Balance plan**: the plan's L41
visit buys **Composite V** (the Crucible and the **IFF spoofer** came at the L40 visit, J11) and fits Rook's Mortar
([the hand plan](../../../player/generator/README.md#act-6-hand-plan-draft)): forward DPS 168 against the proposed
reference **164** (1.02), 24 of 25 MW, one MW spare. The level expects `anti-ground` and `area`; the plan's fit has
neither on the ship (Rook's Mortar is the anti-ground) and breaks the gates on its beam (the pylons are not hardened:
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

None yet. The level needs (M10's Level 41 part; the units' proposals are in their pages' round-72 concept, the moods
in [the world](../../../world/jovian/README.md)'s):

- the backdrop: Callisto's cratered ice, the causeways and the crevasse field, the citadel's walls, three gates with
  their towers and **blast doors** (closed, opening), the hangar decks, the ring road, the inner court and Varga's
  research wing, the Halo's mount, HQ's roofs and masts, the dock with the **empty berth**; the Vrell growth on the
  outer wall; Jupiter's disc; every glow and haze smooth (≥ 32 steps, no ordered dither, drawn once);
- the archive core (hatch whole, broken; the core glowing, the upload ring filling), the lab vault; the CDF beacon;
- the two briefing images; the intel portraits of the pylon and the Halo;
- the round's captures: the level at medium with the plan's fit and Rook: a gate halt with its domes (covered units'
  sheen), an upload, a gate opening, the Halo's halt and its barrier dropping, the Silence line on the HUD, the empty
  berth; 1:1 crops of a dome's rim over the ice and the livery split (elite pylon beside a regular Warden Tank).

## Implementation

- [ ] Level data `data.yaml` (M10's Level 41 part) with the sections, waves, halt cycles, ground targets, radio,
      budget, threat profile, briefing, backdrop and music; three gate sections `holds: pylons` and the Halo's
      `holds: halo` with its checkpoint (none on hard)
- [ ] Reference DPS 164 at L41 and the Act 6 hand plan's Level 41 fit (the Mk V+ and the IFF spoofer; M10 part A)
      before the data; the act HP factor for the returning `medium`+ units listed in *Waves*
- [ ] The pylon gates per the [Shield Pylon](../../../enemies/ground/shield-pylon/README.md#the-pylon-gates-level-41):
      the halt, the clock standing while a pylon stands, the pylons asleep until the halt, the halt cycles cut at the
      last pylon, the blast doors opening, `PYLONS n / m` and the gate pips
- [ ] The [Halo Platform](../../../enemies/ground/halo-platform/README.md)'s held arena at (240, 190), its checkpoint,
      the armour patch at its death
- [ ] **The uplink** (new, small): the archive core's hatch (a ground trigger), the core's 40 px uplink zone with a
      2.0 s cumulative timer and its ring, Lancer's ship only, a core lost off the bottom edge; `FILES n / 3` on the
      tracker's second line; the secondary judged at the end
- [ ] The three-way war's prey links (the Lampreys of t=24 and t=85.5 latching the Gunships of t=21.5 and t=83); a
      prey kill by the Vrell pays nothing
- [ ] The level to the Act 2+ rules (`ActFloorTest`, `PacingTest`, the halts with their cycles; the end's calm named);
      the Gunships' Darts counted
- [ ] The liveries: the pylons, the Halo and the citadel's structures elite; the returning units regular (no new
      frames); the Graft Turrets hybrid
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`), with Rook and without, with 0–3 files;
      the halt and file lines as events; the Silence line never stale
- [ ] Threat profile, Varga's four intel lines and Okafor's teaser fitting the intel panel
- [ ] Briefing: both pages, each one screen, with their images
- [ ] Typical haul within ±5 % of budget(41) with the level's `bounty_scale`
- [ ] The lab vault secret (20 HP, hard 28, crate 100), the shield cell, the overdrive, the two armour patches
- [ ] Music: track 15's stems by section, the motif stem out after the Silence line; the `callisto-fortress` ambience with the
      fortress machinery; the blast doors', hatch's, uplink's and vault's sounds
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on every difficulty
      (`--escort none` too) and takes the files (it hovers over an open core in a halt)
- [ ] Production art and the round's captures

## Open questions

Left to the build (the document is approved as drafted):

- **Four halts** (three gates and the Halo, ≈ 46 s): the pylon doc's "three gates of 2–3" kept; fewer gates (two) would
  shorten the level by ≈ 11 s and raise its scale.
- **The `bounty_scale` 0.30**, under the gap analysis's band: the fortress's 68 ground targets pay most; fewer ground
  targets (or no Graft nests) would bring it near 0.35.

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
