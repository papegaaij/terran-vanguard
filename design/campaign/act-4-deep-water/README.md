---
title: Act 4 – Deep Water
design: approved
implementation: in-progress
art: proposed
depends-on: [../act-3-red-dust, ../../world/europa, ../../story/twist, ../../enemies/naval, ../../enemies/bosses/abyssal-maw, ../../player/systems]
updated: 2026-10-24
---

# Act 4 – Deep Water

## Summary

Levels 22–28. The Vrell are boring through Europa's ice to reach the ocean beneath, and Aegis Wing
follows them down in hydro-kit Stormhawks: across the ice shelf, down a drill shaft past Thera Deep's
farms, over the abyssal plain, through a singing trench, into black water, across a vent field and into
the Vrell hatchery. This is the underwater act: the water slows every bullet and every ship, weapons
without `anti-sub` lose a third of their damage, and the evidence against Helix Dynamics piles up:
human-built relay beacons, a grey submarine drone with a Helix hull code, Rook shot down in the dark.
The act ends against the Abyssal Maw and with the reveal: Ascendancy cruisers in black and gold rise
through the ice beside the Vrell fleet and open fire.

**Status: approved in round 48 (2026-10-22); drafted (Act 4 design phase, 2026-10-21); everything below is pending the user's review in
concept round 48.** The seven level documents, the units, the boss and the systems were drafted in the
design phase's first two waves from the gap analysis's recommended answers (Q1–Q12; a local working note,
`m8-gaps.md`); this README pulls them together (the act intro and outro, the units and their HP factors,
the density and haul table, the threat profile, the music and the voice) as
[Act 3's](../act-3-red-dust/README.md) did. There is **no `data.yaml`** under the campaign or the enemies
yet: the loader would fly it, so every number here is a hand table marked draft, and the build (M8,
[roadmap](../../tech/roadmap/README.md#m8-parts)) turns them into data and measures them.

## Contents

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [level-22-ice-shelf](level-22-ice-shelf/README.md) | Act 4's opener and breather: four Vrell drill rigs on Conamara's ice shelf, ice geysers, the run-in to the station | approved | in-progress | final |
| [level-23-through-the-ice](level-23-through-the-ice/README.md) | The descent: a drill shaft, the waterline where the hydro-kit switches the ship to the water rules, Thera Deep's kelp farms; Glassfin schools and the Eel Swarm | approved | in-progress | final |
| [level-24-abyssal-plain](level-24-abyssal-plain/README.md) | The ocean floor: Vrell spawning reefs, the Barbfin, three Abyss Rays rising from below, the first red-gold relay beacons | approved | in-progress | final |
| [level-25-siren-trench](level-25-siren-trench/README.md) | A narrow trench with side caves: Sirens whose rings slow the ship, the Spiral Nautilus, wall collision; the act's hardest stretch before the Maw | approved | in-progress | final |
| [level-26-blackwater](level-26-blackwater/README.md) | Darkness: headlight and glow only, the Glow Angler's lure, the Choir's first hint of the door; Rook is shot down | approved | in-progress | proposed |
| [level-27-hydrothermal-vents](level-27-hydrothermal-vents/README.md) | The vent field: heat plumes that burn and bend shots, grey Depth Hunters from below (hint 3: a Helix hull code), a Bulwark in Rook's slot | approved | in-progress | proposed |
| [level-28-abyssal-maw](level-28-abyssal-maw/README.md) | The hatchery and the act boss, the Abyssal Maw; then the climb, the ice breakout and the reveal: two Ascendancy cruisers; the act end | approved | not-started | none |

## Design

### Synopsis

Europa's research town **Conamara Station** has reported tremors for a week. The Vrell fleet that left
Mars has come to Europa, and its drill rigs are boring through the ice shelf. Aegis Wing strikes the rigs
on the surface, then dives through the Vrell's own shaft with hydro-kits fitted to the Stormhawks. Below the
ice lies **Thera Deep**, a city of forty thousand people living off kelp farms the Vrell are draining; below
the farms, the abyssal plain, where the Vrell grow spawning reefs, and a line of human-built relay beacons
pulsing red-gold. The wing fights through a trench of singing Sirens and into black water where only the
ship's headlight and the Vrell's glow show anything; in the dark Rook is shot down by torpedoes from unseen
grey hulls. In the vent field those hulls, the Depth Hunters, show themselves, and a scorched wreck shows its
Helix hull code. The Vrell hatchery lies round Europa's deepest vent, guarded by the Abyssal Maw, which dies
with Helix machinery in its spine. As the wing climbs the shaft and breaks through the ice, two cruisers in
black and gold rise through it beside the Vrell fleet and open fire on the Coalition.

### Story beats

- L22: Europa is a scientific colony. Dr. Ilse Brandt, the station's director and Varga's former colleague
  from the Helix years, is the new voice on the radio; Varga's one personal line ("*Ilse? It's Elena. Hold on,
  we are coming.*") is her beat of the act. Rook: "*Last open sky for a while, Lancer.*"
- L23: the hydro-kit is a hasty field refit; Rook jokes about "flying a submarine". Thera Deep Control (a calm
  harbour controller, the second new voice) shows the farms the Vrell are draining.
- L24–L26: human-built relay beacons pulse **red-gold** along the Vrell routes (L24's first line, L25's walls, the
  L27 ridge), the first thing the act asks the player to wonder about; Varga decodes Helix frequencies on them.
- L26: in the dark the Choir speaks, in Acts 3–4's register: "*…small ones… in the dark… your kin… came before
  you…*" (Q8 a: no mention of a door yet). Varga: "*Your kin. It said your kin, Commander.*" Okafor: "*Noted,
  Doctor. Eyes front.*" At the level's end Rook is shot down on the screen by torpedoes from unseen Depth
  Hunters, a cut line and a lamp going dark (Q7 a). He is absent (radio silent, a Bulwark heavy drone in his
  slot) in L27–L28 and L29; see [Rook](../../story/characters/rook/README.md).
- L27: **hint 3.** A Depth Hunter wreck scorched by a plume shows its Helix hull code under the paint. Varga
  reads it, Okafor sends it up to High Command (radio and a placed prop, no wreck crawl: L19 did one).
- L28: **the reveal.** The Maw dies bleeding grey Helix machinery. The wing climbs the drill shaft and breaks
  through the ice (the water rules end at the break line); two Ascendancy cruisers in black and gold rise through
  the ice beside the Vrell carriers and open fire. Okafor: "*Those are not Vrell ships. God help us. Those are
  ours.*" The Vrell and the cruisers never fire at each other: the alliance is shown, not told. A 35 s run to
  orbit, then the act outro. Chairman Silas Vorne's system-wide broadcast, declaring the Jovian Ascendancy's
  independence and its partnership with the Vrell, opens Act 5 (the L29 briefing, decided 2026-09-30); it is
  not in this act.

### Act intro and outro

Format: the [briefing screen](../../ui/briefing/README.md), as Acts 1–3. Order: the L21 debrief with the
act summary → Act 3's outro (which ends on "*Aegis Wing leaves Mars in four days*") → this act's title card →
the act intro below → the L22 mission briefing → the hangar visit before L22 (autosave) → L22. (Since M8 part B
Act 3 names no next act and its outro leads here; until part H moves the release's end to Level 28, the hangar
before L23 cannot launch and reads `MORE: COMING SOON`.) The intro continues from Act 3's outro,
which already told the Colossus's death, the Vrell leaving Mars and Varga's plot to Europa, so it opens on the
arrival at Jupiter and does not repeat them. After the Maw falls the act ends in Level 21's order: the
finale (the climb, the breakout, the reveal, the run, the orbit), the debrief with the act summary
`ACT IV COMPLETE · DEEP WATER`, the four outro pages with track 24 *act complete* under page 1, then the
release's end, which moves from Level 21 to Level 28 and autosaves the hangar before L29: `ACT V - THE BELT:
COMING SOON` (see [Level 28](level-28-abyssal-maw/README.md#act-end)).

**Timeline** (draft): Aegis Wing leaves Mars four days after the Colossus (Act 3's outro); Jupiter is
twelve days at full burn from Mars; the wing arrives in **July 2185**. Conamara's tremors are a week old;
the Vrell fleet got there first by a few days.

**Title card** (held 3.5 s over the act's still, `ui/act-4-deep-water-still.png`, rendered by
`tools/art/act_stills.py act-4` from Level 22's production backdrop as Acts 2–3's were: Europa's ice shelf
from the entry corridor, the Conamara Chaos ice field's broken plates and fissures, Jupiter filling the
sky, a thin plume of steam from the first rig on the horizon; its chrome lettering
`ui/act-4-deep-water-title.png` from `tools/art/act_titles.py`). Text (approved in round 48):

<!-- data: act-title-card -->
> ACT IV
> DEEP WATER
> Europa · July 2185
<!-- /data -->

**Act intro** (four pages, each voiced and with its own image in `assets/ui/briefing/`, as Act 3's). Texts
(approved in round 48 (2026-10-22)); three by Commander Okafor, one by Dr. Varga. The title card and the pages live
in the act's [data.yaml](data.yaml) (`levels`, `title_card`, `briefing`; M8 part B, step B2) and are rendered here.
Images are production art straight away (the build's art step makes them with `tools/art/briefing_images.py`):

| Page | Speaker | Image |
|---|---|---|
| 1 | Commander Okafor (grim) | `act-4-arrival`: Jupiter's banded face filling the transport's forward view, Europa a pale crescent against it, a CDF overlay plotting the Vrell fleet's courses converging on the moon |
| 2 | Dr. Varga | `act-4-europa-cutaway`: a CDF cutaway of Europa: the ice shell kilometres thick with Conamara Station on top, four red Vrell shafts boring down, Thera Deep's domes and farms in the ocean below, the vents and the trench further down |
| 3 | Commander Okafor | `act-4-hydro-kit`: a Stormhawk in the transport's refit bay in a hydro-kit (sealed gun ports, intake fairings, a torpedo rack), ground crews on gantries, Rook's Ember craft beside it |
| 4 | Commander Okafor (fierce) | `act-4-drop`: the transport's drop bay open over Europa, Aegis Wing's Stormhawks on their launch rails, the cracked white shelf far below under Jupiter |

<!-- data: act-briefing -->
> **Commander Okafor:** "Twelve days at full burn, and the Vrell beat us to Europa by a week.
> Their fleet is in orbit and their drill rigs are on the ice. Conamara Station has been feeling
> the tremors since before we left Mars."
>
> **Dr. Varga:** "Europa is a shell of ice kilometres thick over an ocean. Forty thousand people
> live under it, in Thera Deep, and eat from its kelp farms. The Vrell are drilling for that
> ocean. They are not here for the ice."
>
> **Commander Okafor:** "The Stormhawks are refitted for the water: sealed intakes, a torpedo
> rack, a pressure-proof hull. Everything slows down there, theirs and yours. Guns lose a third
> of their bite, beams and lasers nearly all. Save your credits for torpedoes and charges."
>
> **Commander Okafor:** "We hit the rigs on the surface first, then we follow the hole down.
> Drop in ninety minutes. Mission briefing follows. Aegis Actual out."
<!-- /data -->

**Act outro** (four pages after the L28 debrief, each voiced and with its own image; track 24 under
page 1). It carries the beat the finale hands over: the wing escapes into orbit, and the Ascendancy has
declared itself. The finale's closing radio already has the reveal's two lines, so the outro does not repeat
them; Vorne's broadcast is **not** here (it opens Act 5). Texts (draft; decided in round 48 (2026-10-22)):

| Page | Speaker | Image |
|---|---|---|
| 1 | Commander Okafor (grim) | `act-4-outro-orbit`: Europa from orbit, the ice shelf broken open where the shafts came up, two black-and-gold cruisers small against the limb, the Vrell carriers rising round them |
| 2 | Dr. Varga (grim) | `act-4-outro-hullcode`: Varga's screen: the hull code from the Depth Hunter's wreck beside the same code painted on a cruiser's bow, the relay beacons' frequencies in a strip below |
| 3 | Commander Okafor | `act-4-outro-ready-room`: Okafor alone in the transport's ready room under the lamps, Act 3's walker plate on a screen, her hands on the table |
| 4 | Commander Okafor (fierce) | `act-4-outro-aegis`: Aegis Wing's Stormhawks on the transport's flight deck, one rail left empty with Rook's name stencilled on it |

> **Commander Okafor:** "The Maw is dead and Europa's ocean is clear of the Vrell, for what that is
> worth. We are in orbit. The cruisers did not follow. We lost Aegis Two in the dark, and we do not
> know where he is."
>
> **Dr. Varga:** "I matched the code. It is the one from the Depth Hunter's wreck, and it is painted
> on their bows, in the open now. The beacons, the grey hulls, the drones on Mars: they all say the
> same name. The Vrell did not find us alone."
>
> **Commander Okafor:** "On Mars I told Dr. Varga to keep her eyes on the canyon. I silenced her, and
> I was wrong. That does not happen again. What she sees, she says, on every channel."
>
> **Commander Okafor:** "Whatever comes over the radio next, we hear it together. Rook is out there. We
> ship out, and we find him. Aegis Actual out."

The outro sets up [Act 5](../act-5-the-belt/README.md) (Vorne's broadcast, Rook at Ceres Hub) without
pre-empting its intro. It pays Okafor's Act 3 silencing ("*Not on this channel, Doctor.*") as her character
sheet asks; page 3 is that beat, the L29 briefing continues it.

### Settings

[Europa](../../world/europa/README.md), one sub-location per level (its *Sub-locations per level* table): the
ice surface and Conamara Station (L22); the descent shaft and Thera Deep's farms (L23); the ocean floor and
the first beacon line (L24); the trench (L25); the deep ocean and the fault scarp (L26); the vent field and the
beacon ridge (L27); the Vrell hatchery, then the shaft and the ice surface again (L28).

### New mechanics

Each is designed in the level that introduces it; the systems are M8's part A (the
[roadmap](../../tech/roadmap/README.md#m8-parts)).

| Level | Mechanic | Where it is designed |
|---|---|---|
| L22 | **Ice geysers** (cracks that glow, then burst; shard rings from the rigs); the hardened **Drill Rig** ground structures; the surface is *dry*: the Harpoon and the Sonar Pulse read `NO WATER` | [Level 22](level-22-ice-shelf/README.md), [Drill Rig](../../enemies/ground/drill-rig/README.md) |
| L23 | **Under-water mode** (round 48 Q1 b, the softened rules): a level or section `medium: underwater` (the play plane stays `air`; `sub` is the deep below, `ground` the sea floor, `high-air` the ice ceiling); enemy bullets × 0.7, drag on movement (the pressure hull removes it), weapons without `anti-sub` × 0.7, beam reach × 0.75, no Airstrike or Orbital Lance; the **waterline** switch; the descent shaft as `Terrain` | [Level 23](level-23-through-the-ice/README.md), [europa: Under water rules](../../world/europa/README.md#under-water-rules), [systems](../../player/systems/README.md) |
| L24 | The `sub` → `air` rise: a **shadow** that tells the Abyss Ray's rise from below; **spawning reefs** (ground structures that breed fry); the Glassfin and Barbfin as the act's popcorn (Q3 a) | [Level 24](level-24-abyssal-plain/README.md), [Abyss Ray](../../enemies/naval/abyss-ray/README.md) |
| L25 | **Terrain collision under water** (trench walls and a rock pillar, as L16) with **side caves** enemies swim out of; the **sonar slow** (the Siren's visible ring: 60 % speed for 2 s, 3 s immunity, no damage; Q5 a) | [Level 25](level-25-siren-trench/README.md), [Siren](../../enemies/naval/siren/README.md) |
| L26 | **Darkness for every unit** (Q4 a: a look only: bullets always show, everything is hittable; the headlight cone and the Vrell's glow; the Sonar Pulse lights the field for 6 s); the Glow Angler's pickup-like lure; Rook's scripted shoot-down (Q7 a) | [Level 26](level-26-blackwater/README.md), [Glow Angler](../../enemies/naval/glow-angler/README.md) |
| L27 | **Heat plumes** (cycles that burn and bend every shot crossing them); the Depth Hunter's **shootable homing torpedoes** from below (the first homing enemy projectile; Decoy Flares retarget them); Rook's absence (the Bulwark) | [Level 27](level-27-hydrothermal-vents/README.md), [Depth Hunter](../../enemies/naval/depth-hunter/README.md) |
| L28 | An act boss that **frames the arena** (a coil that is a wall), **dives** below the play plane (only `anti-sub` and the Smart Bomb reach it) and bleeds Helix implants; the **breakout** (the medium switches back to air at the break line) and the **reveal run** under two invulnerable cruisers; the new `boss-dive` radio event | [Level 28](level-28-abyssal-maw/README.md), [Abyssal Maw](../../enemies/bosses/abyssal-maw/README.md) |

**Equipment new in the act** (designed in [weapons](../../player/weapons/README.md),
[specials](../../player/specials/README.md), [systems](../../player/systems/README.md),
[generator](../../player/generator/README.md) and [wingmen](../../player/wingmen/README.md); all drafts): from the
L22 visit the Harpoon Torpedoes (front, `anti-sub`, `forward`), the Depth Charges (rear, `anti-sub`, `rear`), the
Sonar Pulse special (one free charge), the pressure hull (removes the water's drag) and the Bulwark heavy drone
(an escort alternative; loaned free while Rook is missing, L27–L29); from L24 the Auto-repair nanites; the Mk IV
"Tokamak" generator in the act's shop; from L27 the Decoy Flares special. The hydro-kit itself is automatic and
free (the act's old open question: kept, Q1). The trait pacing table makes `anti-sub` required from L23 (L26 asks
for `homing`, `spread` and `area` instead: the dark's level).

### Factions present

Vrell, plus the **grey, unmarked human-built machines**: the Depth Hunter (L27, with its Helix hull code under the
paint, the act's hint 3) and, openly, the **Ascendancy cruisers** in black and gold (L28's reveal; scenery and a
set piece, invulnerable, paying nothing). Helix Dynamics appears as relay beacons (L24–L27) pulsing red-gold and a
hull code. The Vrell and the Ascendancy never fire at each other.

### Boss

Act boss [Abyssal Maw](../../enemies/bosses/abyssal-maw/README.md) (L28; Q9 b, the coiled guardian: head 6,700 HP
and four gill sacs from the proposed reference DPS 120, a bounty of 345 in Act 1 terms, 24 % of budget(28)).
**No mid-boss** in Act 4 (the campaign's pacing rule lists Acts 1, 2, 3, 6 and 7); the Abyss Ray, three times in
L24, is the act's large set-piece unit.

### Units in the act

New: the Drill Rig (L22, a ground structure), Glassfin and Eel Swarm (L23; the Glassfin is the act's `tiny`
popcorn, Q3 a), Abyss Ray and Barbfin (L24; the Barbfin is the small firing swimmer), Siren and Spiral Nautilus
(L25), Glow Angler (L26), Depth Hunter (L27) and the Abyssal Maw (L28), plus the Ascendancy cruisers of the
reveal. Returning from Acts 1–3: Skitter, Needler and Stinger (L22–L23), Mote Swarm (L22–L23), Skimmer and Mantis
(L22), Spine Turret and Polyp Mortar (L22 and the L27–L28 vent and hatchery nests), Driftjelly, Lamprey and Reef
Spitter (L23–L26; the Reef Spitter sits on the sea floor, the Lamprey needs no art change), Scuttler (L24, on the
sea floor). The [naval roster](../../enemies/naval/README.md) says what changes under water.

Only the returning `medium` and larger units get the act HP factor from the
[balancing basis](../../enemies/README.md#balancing-basis) (it needs the `reference_dps` 22–28 entries in
`enemies/data.yaml` before any Act 4 level data), against the proposed reference DPS **96 · 100 · 104 · 108 ·
112 · 116 · 120** for L22–L28 (the Act 4 hand plan in [generator](../../player/generator/README.md#act-4-hand-plan-draft):
about 96 at L22, then +4 a level, in the water rules' effective terms; **part A of M8 sets them with
`levelReport`**). The levels' drafts use these values (HP at medium; a new unit's first level takes no factor):

| Level | Units with the act HP factor (HP at medium) |
|---|---|
| L22 | Mantis 26 → 48 (L06, ×96/52) |
| L23 | none (the Eel Swarm's head 100, the siphon pump 40 and the Glassfin 1 are at their first level) |
| L24 | Scuttler 28 → 77 (L04, ×104/38), Eel Swarm head 100 → 104 (segment 10, tail 26; whole about 255) |
| L25 | none (the Siren 90 and the Spiral Nautilus 100 are at their first level) |
| L26 | Eel Swarm head → 112 (segment 11, tail 28), Siren → 93, Spiral Nautilus → 104 |
| L27 | Eel Swarm head → 116 (segment 12, tail 29), Abyss Ray 210 → 234, Siren → 97, Spiral Nautilus → 107 (the Depth Hunter's 120 is its first level) |
| L28 | Eel Swarm head → 120 (segment 12, tail 30), Abyss Ray → 242, Siren → 100, Spiral Nautilus → 111, Depth Hunter → 124 |

The `tiny` and `small` units (Glassfin, Skitter, Mote Swarm, Barbfin, Glow Angler, Driftjelly, Lamprey, Reef
Spitter, Skimmer, Spine Turret, Polyp Mortar) keep their HP, apart from the two rules already in the basis:
the Needler and the Stinger take `small_act_hp` capped at ×3 in L22 and L23 (12 and 18), and from Level 18
every `small` unit takes the **beam-era ×2** by level number (Barbfin 24, Glow Angler 28, Spine Turret 16,
Polyp Mortar 20, Skimmer 10, Driftjelly 8, Reef Spitter 14, Lamprey 6). Under water the beam reaches only 75 % as
far and every non-`anti-sub` weapon does 70 %: **`BeamEraHpTest` is re-measured in part A** (a small unit's time
on the screen against Level 10's 0.9–1.5×) before these HP are final.

### Density and threat profile

The act's minimum is **60 enemies a minute** at medium (`DensityTest`; the campaign's Act 4 rule; a breather
may dip 20 % to 48). Every draft was written to the **Act 2+ floor** (`ActFloorTest`, `PacingTest`) from the
start, after Act 3's lesson (its drafts at 50–90 a minute were all reworked to 77–119): a wave about every 2.5–3.5 s,
at most three stretches over 2 s with nothing on the screen, ≥ 2.5 enemies on the screen and ≥ 10 enemy shots
a minute, with **firing groups** (the Barbfin's needles, the Eel Swarm's head, the Driftjelly's and Angler's
rings) doing the shot work. The water's bullets × 0.7 stay on the screen longer, so the fire rates are set per
level with `levelReport` in the build, not in the drafts. **The band: 74–128 a minute at medium** (≈ 74 for the
breather L26, ≈ 86 for the opener L22), not a target but what each level's cadence gives.

| Level | Rating | Draft | Notes | `bounty_scale` (hand estimate) |
|---|---|---|---|---|
| [L22](level-22-ice-shelf/README.md) | 3 | 214 in 150 s, **≈ 86** | The breather by threat; 30 are ground targets | **0.38** |
| [L23](level-23-through-the-ice/README.md) | 3 | 352 in 165 s, **≈ 128** | Inflated by the Glassfin schools (about 70 %, 1 HP each, 2 credits); the firing groups carry the floor. Above the usual 75: recorded in its Decisions | **0.45** |
| [L24](level-24-abyssal-plain/README.md) | 4 | 265 in 172 s, **≈ 92** | The reefs' fry add more, not counted | **0.40** |
| [L25](level-25-siren-trench/README.md) | 4 | 280 in 173 s, **≈ 97** | A wave every 2.5 s; easy ≈ 224, hard ≈ 308 by the formation lever | **0.48** |
| [L26](level-26-blackwater/README.md) | 3 | 223 in 182 s, **≈ 74** | The breather by threat, not by count: lighter groups, the dark reads slowly | **0.41** |
| [L27](level-27-hydrothermal-vents/README.md) | 4 | 219 in 135 s (t=6–141), **≈ 97** | Barbfin groups for the shots; never a Siren with a Hunter column | **0.78** |
| [L28](level-28-abyssal-maw/README.md) | 4 | 137 in the approach, **≈ 91** | The approach only: the boss fight and the finale come on top, no wave after t=93.5 | **0.74** |

**The economy.** Budget(n) = 700 × 1.07^(n−1): **2,898 · 3,101 · 3,318 · 3,551 · 3,799 · 4,065 · 4,350** for L22–L28.
Data is in Act 1 terms, the code pays each × the act factor **4.096** (1.6³), bounties also × the level's scale;
with the hand scales above the typical hauls come to **2,899 (+0.0 %) · 3,087 (−0.5 %) · 3,319 (+0.0 %) · 3,527 (−0.7 %) ·
3,788 (−0.3 %) · 4,048 (−0.4 %) · 4,365 (+0.4 %)**, every one within ±5 %, and the perfect runs at 1.51–1.66 × the
budget (≥ 1.4 ×). `TypicalHaulTest` fits the final values in each part. The scales run **0.38–0.78**, inside Act 3's
0.26–0.95: L27 and L28 are above M12's 0.3–0.5 because 54–60 % of their enemies are 2-credit Glassfin (they
pay 16 % of the air bounty). The act boss is 24 % of L28's budget (the perfect-run cap, L21's lesson), **345 in
Act 1 terms** at the level's scale 0.74.

**Threat profile across the act** (the levels' hand tables):

| Level | Directions (by enemy) | Traits | Hazards | Boss |
|---|---|---|---|---|
| L22 | front 97 % · sides 3 % | `anti-ground`, `forward` | ice geysers, the rigs' shard rings | none |
| L23 | front 100 % | `anti-sub`, `forward` | shaft walls (terrain), the water | none |
| L24 | front 100 % (the Ray rises from below the lane) | `anti-sub`, `spread` | the Ray's fans, the reefs' fry, the water | none |
| L25 | front 76 % · sides 24 % (cave entries and wall nests) | `anti-sub`, `spread`, `homing` | trench walls, the Sirens' slow rings | none |
| L26 | front 97 % · sides 3 % | `homing`, `spread`, `area` | darkness, Glow Angler lunges, Driftjelly and Siren rings | none |
| L27 | front 68 % · sides 24 % · rear 8 % | `anti-sub`, `rear` | heat plumes (burn, bend shots), the water | none |
| L28 | front 68 % · sides 25 % · rear 7 % | `anti-sub`, `homing` (`side` in Varga's L3 line) | the Maw's coil sweeps, fan walls and lunge; the cruisers' broadsides | act boss Abyssal Maw |

Every level keeps at least 60 % of its waves from the front (L27–L28 at 68 %), and every side or rear entry has its edge
warning 3 s ahead (the Nautilus pairs, the cave entries, the Hunter columns' rear ambushes). L25 lists three traits
though the intel panel shows two (as Levels 13 and 14 did): a pending fix for the build. **Act 4's breathers are
breathers by threat, not by count**: L22 and L26 are the easy levels by what shoots at you, at 86 and 74 a minute.

### Music

Per level, from the [track list](../../audio/music/README.md#track-list): track 10, the Act 4 A theme **"Thera Deep"**
(chosen in round 02; the production stems are part A's), plays in L22 (the ice ambience alone, the base stem from
the first rig, the full mix for the run-in), L23 (the under-water bed switching at the waterline) and L24 (the beacons'
pulse as a faint sonar ping on the beat). **L26 plays the darkness variant** of the A theme (the lamps go out and it strips
back to sonar pings and a deep drone; out at t=160 for Rook's loss). Track 11, the **Act 4 B theme** (muffled
ambient-dnb; in the track list with **no chosen file yet**: the audio step of round 48 proposes it), plays in L25, L27
and L28. **Vorne's motif** (the cold four-note arpeggio, a faint layer in Acts 3–4) returns in L27 from the Hunters'
first sight (t=56) and plays **alone over the ambience while Varga reads the hull code** (t=68–82, as L19's wreck beat
did), and in L28 from the Hunters' first guard (t=16); in the finale it is lifted into the open for the first time
under the run, as Act 5's opening begins. L28 uses the boss warning (track 22) and the Vrell boss theme "The Choir
Descends" (track 18), dropping away under the dive's darkness; **a new reveal sting** (cold low brass with the motif
hidden in it; not in the track list yet) marks the cruisers' rise at K+18; the act-complete fanfare (track 24) plays
under the first outro page. **No bubble layer** in any theme or ambience (round 48 Q12 a; the user's "no
bubbly/popping sounds"). The cues per section are in each level's *Music & ambience*; the ambiences (the ice surface,
the shaft, `europa-trench`, `europa-black`, `europa-vents`, `europa-hatchery`) and the act's new sounds (underwater
explosions, ice cracking, drill rigs, the Maw's roar, the breakout, the cruisers' broadsides) are listed by the
[sfx](../../audio/sfx/README.md) README; real-thing sounds come from Freesound only (CC0/CC-BY, heavy and big).

### Voice

Every briefing page and radio line is voiced, as in Acts 1–3 ([voice](../../audio/voice/README.md)); the hangar
teaser stays text only. **Two new colony voices** (cast in the audition step, pending):

- **Dr. Ilse Brandt**, Conamara Station's director and Varga's former colleague from the Helix years (L22): dry,
  tired, precise, a woman's voice; her radio portrait the station's sodium-lit dome control room.
- **Thera Deep Control** (L23, L27): the city's harbour controller, a calm man's voice, older than Rook's.

Rook flies L22–L26 from the escort slot (his lines `requires: escort`, one dry joke a level), and in **L26 his
lines are scripted** (the warm one at t=150, the cut line at t=161.2, cut by the hits, "*Lancer, something's below u—*"); from L27
he is missing, so he has **no lines** in L27–L28 (dropped, not just `requires: escort`) and the Bulwark has no
radio. **The Choir** sings in L28 before the boss (t=97.5) and **speaks** twice: L26's "*…small ones… in the dark…
your kin… came before you…*" and L28's "*…many… we were many here…*" as the Maw dies, delayed 3 s after the chain's
start; both in the Acts 3–4 register (never the door, Q8 a), with the speech treatment of Act 3's L21 layering.

| Level | Speakers | New speaker |
|---|---|---|
| L22 | Okafor, Varga, Rook, Dr. Brandt | Dr. Ilse Brandt (Conamara) |
| L23 | Okafor, Varga, Rook, Thera Deep Control | Thera Deep Control |
| L24 | Okafor, Varga, Rook | — |
| L25 | Okafor, Varga, Rook | — |
| L26 | Okafor, Varga, Rook (scripted), the Choir (speaks) | — |
| L27 | Okafor, Varga, Thera Deep Control | — (Rook missing) |
| L28 | Okafor, Varga, the Choir (sings, then speaks) | — (Rook missing) |

Okafor is a woman (she / her) in every line. **Pronunciation** for the voice step (the table already has Europa,
Conamara and Thera): add Thera Deep, Conamara Chaos, Brandt, Marlin (L27's hauler) and Pelagic-3 (L28's recon sub);
seeds or prompts are pinned per line, as in Act 3. The intro's four pages and the outro's four are voiced with
the act's data (parts B and H).

## Concept art

The title card's still and the act intro's images are production art straight away (M8 part B, step ART-B, as Act 3's);
the intro's four images are on the [briefing review sheet](../../ui/briefing/README.md#concept-art). The act's concept
files are the Europa mood stills and the under-water grade in [europa](../../world/europa/README.md#concept-art), the units'
proposals in [naval](../../enemies/naval/README.md#concept-art) and the boss's in
[bosses](../../enemies/bosses/README.md#concept-art), all in round 48. Prompts in [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/act-4-still-final-r51-a.png](concept/act-4-still-final-r51-a.png) | Review sheet, M8 part B (round 51): the title card's 960×540 still (Europa's ice shelf from the entry corridor, the Conamara Chaos to a sharp horizon, Jupiter huge over it, a steam thread from the first rig; from Level 22's production backdrop) and the title card over it | chosen (round 52, T.1 = a: regenerated by step D4 with the geyser a; the title card in the game) |
| [concept/act-4-intro-capture-r51-a.png](concept/act-4-intro-capture-r51-a.png) | Game capture, M8 part B (step B5): a save before Level 22 (`:content:playthroughSave -Plevel=22`) continued from the main menu: the ACT IV DEEP WATER title card, the four voiced intro pages, then the Level 22 hangar | chosen (round 51 F.6 / round 52 T.1: the intro accepted; captured with the r51 still, the game now shows a) |
| [concept/act-4-intro-capture-r51-a.mp4](concept/act-4-intro-capture-r51-a.mp4) | The same run as a short video with the game's sound: Continue, the title card and page 1 (28 s) | chosen (round 51 F.6 / round 52 T.1: the intro accepted; captured with the r51 still, the game now shows a) |
| [concept/act-4-still-r52-a.png](concept/act-4-still-r52-a.png) | Round 52 (F.6 redo), variant a: the 960×540 title-card still with one huge water-vapour plume rising from behind the horizon left of Jupiter (~240 px tall: a thin base, a column of fanning jets, a rounded crown, a faint veil falling back; sunlit from the upper left, glittering ice grains); the rest as r51 (ice shelf, Conamara Chaos, Jupiter, the rig's steam thread) | chosen (round 52, T.1 = a) |
| [concept/rejected/act-4-still-r52-b.png](concept/rejected/act-4-still-r52-b.png) | Round 52 (F.6 redo), variant b: a nearer plume (~380 px tall) from a fissure on the ice below the lettering's left end, with a curtain of three smaller plumes along the fissure receding to the horizon on the left; the rest as r51 | rejected (round 52, T.1: a picked) |
| [concept/act-4-still-r52-sheet.png](concept/act-4-still-r52-sheet.png) | Round 52 review sheet: both stills at 1× and the title card over each (darkened to 55 %, letterbox, the chrome lettering) | chosen (round 52, T.1 = a; the sheet shows both) |

## Implementation

- [ ] All 7 levels promoted to level documents (drafts written 2026-10-21, pending round 48) and implemented
      (M8 parts B–H).
- [ ] Act data file: `levels: [22, 28]`, the title card, the four intro pages and the four outro pages with their
      images and expressions, rendered into *Act intro and outro* as marked tables (part B for the intro, part H for
      the outro with `next_act` for the release's end, ACT V · THE BELT). The intro is built (part B: `ActFourDataTest`,
      `BriefingsTest`, `OutroOnwardTest.afterAct3sOutroComeAct4sTitleCardAndTheBriefingOfLevel22`); the outro waits for part H.
- [ ] The title card's still (`tools/art/act_stills.py act-4` from Level 22's backdrop) and lettering; the eight
      intro and outro briefing images plus the fourteen level briefing images (`tools/art/briefing_images.py`). Built in
      part B: the still and lettering, the four intro images and Level 22's two (`BriefingLayoutTest`; round 51).
- [ ] The act's systems (M8 part A and the levels' parts): the under-water mode (medium field, trait factor,
      bullet speed, drag, beam reach, special setting restrictions), the descent shaft and trench on `Terrain`,
      the Siren's slow, darkness, heat plumes, homing torpedoes and Decoy Flares, the breakout and the reveal run,
      the `boss-dive` radio event.
- [x] `reference_dps` 22–28 and the Act 4 balance plan (`balance-plan.yaml` visits 22–28) before any Act 4 level
      data (M8 part A): `enemies/data.yaml`, `ActHpFactorTest`, `BalanceTest` (step A2: the reference DPS 96 → 120,
      the plan's visits with a temporary budget(n) income and the water's ×0.7 in `BalanceTest` and
      `tools/balance.py`; `ActHpFactorTest`'s Act 4 cases in step A4; `ActFourDataTest.theReferenceDpsRunsToLevel28`, `BalanceTest`, `tools/balance.py` exit 0).
- [ ] Every level within the act's density band (`DensityTest`, `PacingTest`, `ActFloorTest`), the beam era's HP
      re-measured under water (`BeamEraHpTest`) and its typical haul within ±5 % of its budget (`TypicalHaulTest`).
- [ ] The act-end order after L28 (Level 21's) and the release's end moved from L21 to L28; the hangar before L29
      reads `ACT V - THE BELT: COMING SOON`.
- [x] A save at the L22 hangar from the `v0.1` release plays the Act 4 intro **once** on Continue (the `act-intro:N`
      flag, as Act 3's), not repeating the title card of an act already seen (step B2: `CampaignRouteTest`).
- [ ] The act's voices (Dr. Brandt, Thera Deep Control, the Choir's speech) cast and rendered; the act's sounds
      (Freesound only) and the Act 4 B theme and the reveal sting made.
- [ ] `ActPlaythroughTest` and `BalanceTest` through Act 4 with Rook (L22–L26) and the Bulwark (L27–L28; `--escort none` works).
- [ ] The user's playthrough of Act 4.

## Open questions

- All of round 48's questions (the draft answers are in each document's *Decisions*): the softened water
  rules (Q1 b), the light of Europa (Q2 a), the act's own swimmers (Q3 a), darkness as a look (Q4 a), the Siren's
  slow (Q5 a), the Bulwark (Q6), Rook's on-screen shoot-down (Q7 a), the Choir's words (Q8 a), the Maw as a coiled
  guardian (Q9 b), the reveal as a breakout and run (Q10 a), the hint-3 hull code (Q11 a), no bubbles (Q12 a).
- The Act 4 timeline (July 2185, Jupiter twelve days from Mars), the Mk IV's 20 MW and the intro and outro texts.

## Decisions

- 2026-09-30: Hydro-kit drafted as an automatic free refit; underwater rules owned by
  [europa](../../world/europa/README.md#under-water-rules).
- 2026-09-30: Rook goes missing at the end of L26 and returns in Act 5 (Ceres Hub, L29; L30 flying).
- 2026-09-30: The twist is revealed at the end of L28, with Vorne's broadcast in the level-29
  briefing.
- 2026-09-30: Enemy variety pass: new units added to the level rows so the act passes the [variety checklist](../../enemies/README.md#variety-checklist-per-act).
- 2026-10-21 (Act 4 design phase; **everything decided in round 48 (2026-10-22)**): the sketch's roster replaced
  by seven draft level documents (the Contents table); the Maw moved from "the trench" to the **hatchery** (Europa's
  setting table says so now); the act README rewritten for the rules Acts 1–3 established: the intro (title card
  ACT IV · DEEP WATER · Europa · July 2185, four pages continuing Act 3's outro) and the outro (four pages: the
  escape, the hull code, Okafor owning Act 3's silencing, Rook), the units in the act with the HP factors from
  the proposed reference DPS 96–120, the density band (≈ 74–128 a minute at the Act 2+ floor) and the hand-estimated
  haul table (every level within ±5 %), the threat profile, music (track 10 in L22–L24, the darkness variant in L26,
  track 11 in L25 and L27–L28, Vorne's motif in L27–L28, the reveal sting) and voice (Dr. Brandt and Thera Deep
  Control new, Rook's scripted L26 beat, the Choir speaking twice); Level 26's HP line aligned with the act factor
  (Nautilus 104, Siren 93).
- 2026-10-21 (same step): **no mid-boss** in Act 4 (the pacing rule); the Abyss Ray is the large set-piece unit.
  The heavy drone is renamed **Bulwark** (it clashed with Act 3's Warden Tank; the campaign README follows).
  Beacons are **red-gold** (the story README had "black and gold", which belongs to the Ascendancy cruisers of the
  reveal; fixed). The Siren's ring is the Siren page's own spec (120 px/s to 230 px; the europa text now says
  so). The Abyssal Maw's bounty is 345 in Act 1 terms (not 510), its Choir fragment waits 3 s, the level ends at
  K+62 and the traits are `anti-sub`, `homing`. The Mk IV "Tokamak" recommended at 20 MW (data unchanged at 18
  until the user decides). The weapons README's two roster rows (Harpoon Torpedoes, Depth Charges) became Contents rows.
  The M8 row and parts A–I are proposed in the [roadmap](../../tech/roadmap/README.md#m8-parts).
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-22 (M8 part B, steps B2, ART-B and B4; accepted in round 51 (2026-10-22)): the act's `data.yaml` with the title card and the four intro pages
  (marked tables); Act 3's `next_act` removed, so between parts B and C the hangar before Level 23 reads "MORE: COMING
  SOON" and cannot launch; the outro stays out of the data until part H (its images and voices are H's), which adds
  `next_act` (ACT V / THE BELT). The title card's still and lettering from Level 22's production backdrop
  (`act-4-still-final-r51-a`); after Act 3's outro come Act 4's title card and Level 22's briefing
  (`OutroOnwardTest`); the intro captured from a save before Level 22 (`act-4-intro-capture-r51-a`).
- 2026-10-22 (user, round 51, closed): the briefing images (F.7) accepted. **F.6 redo**: the title card stays `proposed`; the user: "Maybe we can add a geyser to the title card somewhere. Europa is known for its giant geysers shooting up up to 200km." The redo (a giant geyser rising over the horizon of the still) follows in round 52; its intro captures (the title card in them) are redone with it. The part's calls C.1–C.7 accepted. The Act 4 intro's accepted-in-round-51 rules are those the steps logged above.
- 2026-10-22 (M8 part C, step ART-T; accepted in round 52 (2026-10-22)): the title card's redo (round 51 F.6) with a giant
  water-vapour plume: **a** one huge plume rising behind the horizon left of Jupiter, clear of the lettering; **b** a
  nearer plume on a fissure with a curtain of three smaller ones, its column behind the left end of DEEP
  (`act-4-still-r52-a`/`-b`, the sheet with the lettering composited); the r51 still stays in `assets/ui/` until the pick.
- 2026-10-22 (user, round 52, closed; T.1 = a): the Act 4 title card carries the giant geyser **a** (one huge plume
  rising from behind the horizon left of Jupiter, its crown left of ACT IV): `tools/art/act_stills.py` builds it with
  `A4_GEYSER = "a"` into `assets/ui/act-4-deep-water-still.png` and its review sheet `act-4-still-final-r51-a.png`
  (step D4); b (the nearer fissure plume) rejected, its still moved to `concept/rejected/`. The round 51 intro captures
  show the old still (not re-captured). C.1–C.7 of round 52 accepted with Level 23.
- 2026-10-24 (M8 AW, step AW2; the user's decision of 2026-10-23, the wording accepted in round 56 (2026-10-24)):
  Okafor's intro page 3 warns that beams and lasers lose nearly all their bite under water and to save credits for
  torpedoes and depth charges ("... a pressure-proof hull. Everything slows down there, theirs and yours. Guns lose a
  third of their bite, beams and lasers nearly all. Save your credits for torpedoes and charges."; the plan's wording,
  shortened to the page's four lines: `BriefingLayoutTest`); its voice take is re-rendered by step VOX-AW.
- 2026-10-24 (user, round 56 closed, C.7): the four warning texts accepted as built. Round 55 closed the same day
  (Level 26; Rook's loss retimed after the playtest: [Level 26](level-26-blackwater/README.md#decisions)).
