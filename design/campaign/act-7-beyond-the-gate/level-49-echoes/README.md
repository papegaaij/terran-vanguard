---
title: Level 49 – Echoes
design: approved
implementation: in-progress
art: proposed
depends-on: [../../../enemies/bosses, ../../../enemies/bosses/brood-carrier, ../../../enemies/bosses/dust-colossus, ../../../enemies/bosses/iron-sovereign, ../../../world/vrell-space, ../../../player/generator, ../../../player/wingmen, ../../../player/specials, ../../../ui/hud, ../../../story/characters/the-choir, ../level-48-vornes-end]
updated: 2026-11-07
---

# Level 49 – Echoes

## Summary

Past Vorne's citadel the wing flies into **the Choir's memory**: the hive mind, having taken Vorne in, tries to break
the wing's will by showing it what it has lost. A short approach (≈ 90 s) through three remembered battlefields
(Luna, Mars, Ceres orbit) and their remembered waves, all in the **spectral tint**, then the **boss rush** (Q6 a):
the echoes of the [Brood Carrier](../../../enemies/bosses/brood-carrier/README.md#echo-level-49-draft), the
[Dust Colossus](../../../enemies/bosses/dust-colossus/README.md#echo-level-49-draft) and the
[Iron Sovereign](../../../enemies/bosses/iron-sovereign/README.md#echo-level-49-draft), one phase each, each told by
its own boss warning, with the armour, the shield and the special's charges refilled and a checkpoint between them.
The memory then melts into the hive's tissue, and the core lies ahead. ≈ 288 s at medium; ≈ 107 enemies a minute
over the approach.

**Status: approved** (Act 7 design phase, step `m11-d-levels-4`, 2026-11-04; the user approved it on 2026-11-05, round 84).
Written from the act roster, the gap analysis's recommended answers (**Q6 a**, **Q10 a**) (the user changed Q10 to **b** in round 84; the looks below follow b) and judgement calls (J3,
**J8**, **J9**, J11, J12) and the earlier steps' drafts, which are the source of truth for their numbers: the
[bosses](../../../enemies/bosses/README.md#boss-rush-l49-echoes)' *Boss rush* rules (the tint, no weak-point
multipliers, the warning, the refill, the checkpoint per echo, the dissolve, the bounties, the par) and the three
*Echo* sections, [Vrell space](../../../world/vrell-space/README.md#sub-locations-per-level)'s memory and its
[mood](../../../world/vrell-space/README.md#light-per-level-q10-b), the HUD's
[echo counter](../../../ui/hud/README.md#act-7-additions-draft), the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft) and
[Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft). **Built** (M11 part G, step G2): the level's
[`data.yaml`](data.yaml) holds the approved tables, measured and tuned (the density waves, the echoes' HP, the scale;
pending the user's review in round 94); the tables marked as data are rendered from it.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft; D-story may reword them):

<!-- data: briefing -->
> **Commander Okafor:** "Past Vorne's citadel our scans show places we know. Luna. Mars. Ceres.
> All in one spot, all at once. They are not there, Lancer. Fly through whatever it shows you:
> the hive's core lies on the other side."
>
> **Dr. Varga:** "The Choir remembers everything it has lost, and it will show you what you took
> from it. None of it is real, but it can still hurt you. Each time a memory breaks your
> readings snap back, armour and all. As if it remembers you whole, too."
<!-- /data -->

*Images* (production art in M11's build, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-49-memory-scan`: the fleet's long-range scan with three terrain signatures stacked on one point
  (Luna's grey, Mars's rust, the belt's rock), "LOCATION: INVALID" stamped across it; the wing's lane a dotted line
  into the stack.
- Varga's page `level-49-echo-trace`: Level 07's record of the Brood Carrier in grey outline beside a new trace of the
  same shape in violet, "MATCH 98 %" in her hand.

*Hangar teaser* (shop screen before L49; Rook speaks, not voiced):

<!-- data: teaser -->
> **Rook:** "Varga says we're seeing old friends today. I never liked the old friends."
<!-- /data -->

The plan's L49 visit fits the **[Bastion Shield](../../../player/shields/README.md)** in place of the Mk III (three
fights with refills between them: a deep buffer the refills top up) and gives Rook his Autocannon; 26 of 27 MW
([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The intel panel's `SPECIAL LIMITED`
names the Airstrike and the Orbital Lance if fitted (`NO COVER`, J12).

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (`space` is `air` in vacuum, J12: every remembered wave, the Brood Carrier's echo); `ground` in the arenas only (the Dust Colossus's remembered basin floor, the Iron Sovereign's station layer) |
| Attack directions | front 74% · sides 12% · rear 13% by the waves' data; the Colossus's rear breach |
| Density | 5 |
| Recommended traits | `forward`, `rear`, `shield-breaker` (**J9**: the roster's "any balanced loadout" is not a trait) |
| Hazards | The three echoes back to back (the bosses' documents); the remembered waves from every side |
| Boss / mid-boss | **boss rush**: the echoes of the Brood Carrier, the Dust Colossus and the Iron Sovereign (Q6 a) |
| Special limits | `NO COVER` (the Airstrike, the Orbital Lance; J12) |
| Sensor-suite detail | **none**: Vrell space, the Choir's memory, `air`, all directions · **L1**: + density 5, the hazard "three echoes in a row", the secondary `KILLS 0 / 157`, `SPECIAL LIMITED` (`NO COVER`) · **L2**: + the three echoes' portraits (tinted), Skitter, Needler, Stinger, Spore Bomber, Dust Kite, Ghost Drone, Mote Swarm, Choir Herald, Dart, Talon, Gunship and Hornet portraits · **L3**: + `forward`, `rear` and `shield-breaker` highlighted, the wave strip marking the three echoes |
<!-- /data -->

*Varga's intel line* per sensor-suite level (must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "The Choir's memory, Lancer. Luna, Mars, Ceres." |
| L1 | "Waves from every side, then three echoes in a row." |
| L2 | "Carrier, Colossus, Sovereign: as they were, in light." |
| L3 | "Guns ahead and behind; a breaker for the shields." |

**Trait coverage** (`TraitCoverageTest`, the lesson): any front weapon covers `forward`, a rear weapon `rear`, and
the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md) (wing, in the shop since L29) `shield-breaker`;
one loadout of what is in the shop at the L49 visit covers all three. **The plan's own fit covers them**: the
[Ion Beam](../../../player/weapons/ion-beam/README.md) L5 (front), the
[Swarm Tail](../../../player/weapons/swarm-tail/README.md) L5 (rear) and the Tesla Coil Pod L5, with the
[Micro-missile Pod](../../../player/weapons/micro-missile-pod/README.md) L5: no launch warning.

## Objective

- **Primary** `reach-end` with the rush as the condition (Levels 21, 28, 35 and 42's form): the level ends when the
  third echo is dead and the closing lines have played. The briefing and the objective line read **`BREAK THE
  ECHOES`**; from the first warning the tracker's well reads **`ECHO n / 3`** with its pips (the
  [HUD](../../../ui/hud/README.md#act-7-additions-draft)), so the player always sees how many fights are left. A
  ship lost in an echo restarts at that echo's checkpoint (every difficulty, the rush's rule); a hard game over is a
  game over.
- **Secondary** "Clear the memory" (`kill_ratio`, the 65 % rule): destroy at least **65 %** of the approach's
  enemies, **157 of 241** at medium as built (the hand table's 104 of 160 before step G2's density waves; easy and
  hard by the formation lever, the same 65 %), the echoes and what they launch not counted (`boss_counted: false`).
  The tracker counts to its target, **`KILLS n / 157`**, until the first warning (`until: warning`); it is **met** at
  the target's kill (`DONE`) and **failed** once the approach's remaining enemies can no longer
  reach it, at the latest at the first warning (t=90), so it resolves before the rush and the well can hand over to
  the echo counter. Pays **100** in Act 1 terms (**1,678** in Act 7); the typical haul counts 50 %.

## Layout

Scroll **100 px/s** through the approach (script time; the memory has no ground there, so the speed moves the `deep`
and `low-air` remembered scenery); in the rush each arena's own (the bosses' documents). Setting:
[Vrell space](../../../world/vrell-space/README.md#sub-locations-per-level)'s **Choir memory**, `vacuum` (J12).
**Light** ([mood](../../../world/vrell-space/README.md#light-per-level-q10-b), Q10 b): the earlier acts' worlds
(Luna, Mars and Ceres keep their own colours: not Vrell) under the **echo tint of A.8** (the spectral tint: desaturated, a cold violet-cyan), their
edges dissolving into the palette's dark tissue; the remembered units and the echoes a step brighter than their tinted worlds, **their
bullets as built** (the tint never reaches a bullet). Each memory **forms** at a stretch's start (the scenery
brightening out of the tissue over 2 s, from the top edge down) and **dissolves** at its end (the edges melting into
tissue, then the next world forming): smooth fades, no flash, no colour inversion. Every haze and glow smooth (≥ 32
steps, no ordered dither, fading to 0 inside its sprite's canvas).

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Luna remembered | 0–31 | 0–3,100 | 100 | clear | `deep`: Earth's crescent over the grey limb, dissolving into the tissue. `low-air`: Luna's regolith and the L1 point's wreck lights, remembered; the supply pod (secret, t≈24). | Act 1's Vrell as the Choir remembers them: Skitters, Needlers, Stingers, Spore Bombers. |
| 2. Mars remembered | 31–61 | 3,100–6,100 | 100 | clear | `deep`: Mars's rust disc. `low-air`: Valles walls far below, dust veils (scenery, never over a bullet). | Act 3: Dust Kites, Ghost Drones, Mote Swarms, Choir Heralds. |
| 3. Ceres remembered | 61–90 | 6,100–9,000 | 100 | clear | `deep`: The belt's dark rock, Ceres far off. `low-air`: Tumbling rocks and Ceres's station lights far off. | Act 5, **Vorne's memory**: Darts, Talons, shielded Gunships and Hornets (`shield-breaker`); the last wave at t=84, gone by the first warning (t=90). |
| 4. Echo of the Carrier | 90–98 | 9,000–9,240 | 20 | clear | `deep`: Luna's L1 point: Earth, the Moon, the picket's wreck lights. | The first warning at t=90 (the approach's last wave gone); the Brood Carrier's echo forms at its station (t=95-98). |
| 5. First gap | 98–101 | 9,240–9,300 | 20 | clear | `deep`: The memory melting into the tissue. | The dissolve (≤ 2 s chain, then the motes); no wave; the next warning at t=101. |
| 6. Echo of the Colossus | 101–106 | 9,300–9,400 | 20 | clear | `ground`: The remembered Hellas basin floor (this fight only; its breaches need it). | The second warning at t=101 (no line); the Dust Colossus's echo at its bar (t=106), its first tell 2 s after. |
| 7. Second gap | 106–109 | 9,400–9,460 | 20 | clear | `deep`: The memory melting into the tissue. | The dissolve; no wave; the third warning at t=109. |
| 8. Echo of the Sovereign | 109–117 | 9,460–9,460 | 0 | clear | `deep`: Ceres orbit: Ceres's curve and the station's lights; the scroll halted (the Sovereign is its own anchored station). | The third warning at t=109; the Iron Sovereign's echo forms on its station (t=114-117), the core already open. |
| 9. The unmaking | 117–135 | 9,460–10,140 | 40 | clear | `deep`: The last memory melts into the hive's living tissue; far ahead, the core's red-violet glow. | ≥ 4 s of calm after the third dissolve; the closing lines; the level ends at t=135 (≈ 288 s real). |
<!-- /data -->

**The rush** (paper times at medium; the fights' numbers are the [bosses](../../../enemies/bosses/README.md#boss-rush-l49-echoes)'):

| Echo | The warning (track 22) while the memory forms | Forms (invulnerable) | Bar | Kill (paper) | Then |
|---|---|---|---|---|---|
| [Brood Carrier](../../../enemies/bosses/brood-carrier/README.md#echo-level-49-draft) | **t=90–95**: Luna's L1 point, the slow scroll (20 px/s); the checkpoint at the warning | t=95–98, already broadside at its station | t=98 | ≈ t=151 (≈ 53 s) | the gap t=151–159 |
| [Dust Colossus](../../../enemies/bosses/dust-colossus/README.md#echo-level-49-draft) | **t=154–159**: the **remembered Hellas basin floor** forms as a `ground` layer for this fight only (its breaches need it; crawl 20 px/s); the checkpoint at the warning | its first tell 2 s after the bar | t=159 | ≈ t=209 (≈ 50 s) | the gap t=209–217 |
| [Iron Sovereign](../../../enemies/bosses/iron-sovereign/README.md#echo-level-49-draft) | **t=212–217**: Ceres orbit, the scroll halted, the station's `ground` layer; the checkpoint at the warning | t=217–220, the core already open | t=220 | ≈ t=270 (≈ 50 s) | the unmaking |

**Each gap** (≈ 8 s, the bosses' rule): the dead echo's chain (≤ 2 s) and its dissolve into violet motes; the
**refill** at the dissolve's end (the armour and the shield to full, the special's charges back to what the player
brought into the level, a cooldown special `READY`; the HUD's 1 s fill and chime); the memory melting into tissue
and the next world forming under the **next warning** (5 s), with the checkpoint taken at its start. No wave enters a
gap. The bar to the last kill is **≈ 172 s** on this timeline (the bosses' paper par ≈ 178 s counts the first echo's
arrival); the par counts from the first **bar** (the built rush's rule) and is the build's measured mean: **176 s**
(step G2: the plan's fit and Rook, five seeds at medium; each echo 40–60 s, the act boss's 90–180 s window does not
apply).

**As built** (step G2, pending the user's review in round 94): the level clock stands at each gate's end while its
echo lives, so the data's times after t=90 are the clock's: the warnings at t=90, 101 and 109 (5 s, the Colossus's
4.9 s, before each arrival), the bars at t=98, 106 and 117 (each gate's halt: the checkpoint and the refill are taken
there, the built rush engine's rule, not at the warning), the gaps t=98–101 and 106–109 (3 s of clock after each kill:
with the chain and the dissolve ≈ 8 s from a kill to the next bar), the unmaking t=117–135; ≈ 288 s real at medium.

**The echo's launched units** (the Carrier's Skitters and Needlers, the Sovereign's Darts and station missiles)
**dissolve with their echo** at its death, unpaid (they are its memory): each gap starts clean and the refill is
never under fire. Proposed here for the bosses' *Boss rush* rules (their *Death* row), pending the user's review.

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). **Measured** (step G2, `levelReport`): the hand table's 30 waves plus 22
density waves (the final-act rule: Stingers, Needlers, Ghost Drones, Dust Kites, Talons and Darts, each in its act's
stretch, all from the front, marked "Step G2: density"): **241 enemies** at medium to the first warning (170 a minute;
easy 183, hard 299), 3.57 on the screen and 70 shots a minute (the floor's 2.5 and 10), one pause over 2 s (t≈88–90,
the approach's last wave gone before the first warning), the end's calm 18 s; the hand list below is the draft's.

- **The remembered waves** are the units as built, drawn in the spectral tint (the echoes' tint rule), never a new
  unit: the Choir's memory of each act's battles, in the campaign's order. The Act 5 stretch's Ascendancy units
  (Darts, Talons, Gunships, Hornets) are **Vorne's** memories, taken in with him at Level 48, as the Iron Sovereign's
  echo is.
- **HP** from the proposed Act 7 reference DPS (J11: **196** at L49,
  [Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)): the returning `medium` units with the
  act HP factor from their first level: the **Spore Bomber** 24 × 196/32 = **147**, the **Choir Herald** 60 × 196/84 =
  **140**, the **Gilded Gunship** 88 × 196/124 = **139** (its hex shield scaled the same, the
  [shields' rule](../../../enemies/README.md#balancing-basis)), the **Hornet** 96 × 196/128 = **147** (likewise); the
  `small` units with the beam era's ×2 as built (Needler 8, Stinger 12, Ghost Drone 28, Talon 32); the Skitters, Dust
  Kites, Motes and Darts `tiny`. The echoes' HP are their *Echo* sections'.
- **Density and pacing**: **160 enemies** in 30 waves over the approach's 90 s, **≈ 107 a minute** at medium (easy
  ≈ 135, hard ≈ 185 by the formation lever), counted **up to the first warning** (Level 42's rule for a level whose
  boss fills most of it); the echoes and their launched units are not counted. A wave about every 3 s. The floor comes
  from the Needlers' thorns, the Stingers' missiles, the Spore Bombers' spores, the Heralds' rings, the Ghost Drones'
  shots, the Talons' pairs and the shielded hulls' fire. Expected over the approach: ≤ 3 pauses over 2 s, ≥ 2.5 on the
  screen, ≥ 10 shots a minute; the rush is boss time (the bosses' never-quiet rule, 1.0 s), its two 8 s gaps outside
  `PacingTest`'s measure as every boss's arena; **to be confirmed with `levelReport`**.
- **Directions** (≥ 60 % front by wave count): front 21, sides 5 (the Stinger pincers, the Dust Kite swarms from a
  side, the Hornet pair), rear 4 (the remembered rear Skitters, a Mote Swarm and two Dart swarms from the bottom
  edge) of 30: **70 % front**. Every side or rear entry gets the built edge warning.
- **Limits and spacing** (the Act 5 lesson): never two `medium` or larger hulls overlapping: the Spore Bomber pair ≥
  120 px apart, the two Heralds' rings never side by side (the second waits while the first lives within 120 px of its
  lane), the Gunships and the Hornets on separate lanes ≥ 120 px apart.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 2 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 3.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step G2: density, the final-act rule (varied mixed waves) |
| 5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 8 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 9.5 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Step G2: density, the final-act rule (varied mixed waves) |
| 11 | 1 | pincer | [Stinger](../../../enemies/air/stinger/README.md) | 4 | sides | 2 a side |
| 14 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 15.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (right) | Step G2: density, the final-act rule (varied mixed waves) |
| 17 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 20 | 1 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | ≥ 120 px apart; the remembered Level 03 bombers |
| 21.5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 3 | front (left) | Step G2: density, the final-act rule (varied mixed waves) |
| 23 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | rear | Level 05's first rear wave, remembered |
| 24.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (right) | Step G2: density, the final-act rule (varied mixed waves) |
| 26 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | The supply pod (secret, t≈24) |
| 27 | 1 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Step G2: density, the final-act rule (varied mixed waves) |
| 28 | 1 | pincer | [Stinger](../../../enemies/air/stinger/README.md) | 4 | sides | |
| 30 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 33 | 2 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 10 | front | The memory turns to Mars (t=31) |
| 34.5 | 2 | stream | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | Step G2: density, the final-act rule (varied mixed waves) |
| 36 | 2 | stream | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 4 | front | |
| 37.5 | 2 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | Step G2: density, the final-act rule (varied mixed waves) |
| 39 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 41 | 2 | stream | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | Step G2: density, the final-act rule (varied mixed waves) |
| 42 | 2 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | Its aura ring as built |
| 44 | 2 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 10 | left side | |
| 45.5 | 2 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | Step G2: density, the final-act rule (varied mixed waves) |
| 47 | 2 | stream | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | |
| 48.5 | 2 | stream | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | Step G2: density, the final-act rule (varied mixed waves) |
| 50 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | rear | From the bottom edge |
| 51.5 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step G2: density, the final-act rule (varied mixed waves) |
| 53 | 2 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | ≥ 120 px from the first Herald if it lives; drops the Shield cell |
| 55 | 2 | snake | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 10 | right side | |
| 56.5 | 2 | V-wing | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 4 | front | Step G2: density, the final-act rule (varied mixed waves) |
| 58 | 2 | stream | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | |
| 59.5 | 2 | stream | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front | Step G2: density, the final-act rule (varied mixed waves) |
| 62 | 3 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 8 | front | The memory turns to Ceres orbit (t=61) |
| 63.5 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 4 | front (left) | Step G2: density, the final-act rule (varied mixed waves) |
| 65 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 66.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step G2: density, the final-act rule (varied mixed waves) |
| 68 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | Its hex shield (`shield-breaker`) |
| 69.5 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 4 | front (right) | Step G2: density, the final-act rule (varied mixed waves) |
| 71 | 3 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 8 | rear | Act 5's Darts from behind |
| 72.5 | 3 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | Step G2: density, the final-act rule (varied mixed waves) |
| 74 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Its tip drops the Overdrive |
| 75.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Step G2: density, the final-act rule (varied mixed waves) |
| 77 | 3 | pincer | [Hornet](../../../enemies/air/hornet/README.md) | 2 | sides | One a side, shielded; never beside the Gunship |
| 78.5 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 4 | front (left) | Step G2: density, the final-act rule (varied mixed waves) |
| 80 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | |
| 81 | 3 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 5 | front | Step G2: density, the final-act rule (varied mixed waves) |
| 82 | 3 | stream | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 8 | rear | |
| 84 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The last wave; gone by ≈ t=89 |
| 95 | 4 | boss rush (echo) | [Brood Carrier](../../../enemies/bosses/brood-carrier/README.md) | 1 | front | its warning at t=90 |
| 105.9 | 6 | boss rush (echo) | [Dust Colossus](../../../enemies/bosses/dust-colossus/README.md) | 1 | front | its warning at t=101 |
| 114 | 8 | boss rush (echo) | [Iron Sovereign](../../../enemies/bosses/iron-sovereign/README.md) | 1 | front | its warning at t=109 |

Totals: Skitter 40 · Stinger 24 · Needler 23 · Spore Bomber 2 · Dust Kite 42 · Ghost Drone 22 · Mote Swarm 16 · Choir Herald 2 · Dart Drone 34 · Talon 32 · Gilded Gunship 2 · Hornet 2.
<!-- /data -->

## Hazards

- **The echoes** ([bosses](../../../enemies/bosses/README.md#boss-rush-l49-echoes)): three fights in a row, each
  with its playbook and its never-quiet floor; the warnings and the refills between them (see *Layout*).
- **The memory**: scenery on `deep` and `low-air`, no hit boxes; the dust veils of section 2 never cover a bullet.
- **Special limits** (J12): `NO COVER` for the Airstrike and the Orbital Lance. The plan's
  [Swarm Call](../../../player/specials/README.md#act-7-specials-draft) is a cooldown special: the refill makes it
  `READY` for every echo (its cap of 400 a call per echo, the bosses' rule); a charged special (the EMP Burst, the
  Smart Bomb) gets back the charges the player brought.

## Secrets and pickups

- **The supply pod** (secret, section 1, t≈24, x≈380): a remembered Coalition supply pod (Act 1's convoys), tumbling
  slowly in the tint, half dissolved; a **20 HP** trigger (28 on hard), broken it frees a hidden crate worth **100**
  in Act 1 terms (**1,678**). The Salvage scanner's glint marks it. Its event line, Varga: "*That pod is ours. It
  remembers our things too.*" Name a placeholder for D-story.
- **Shield cell**: dropped by the second Choir Herald (t=53). **Overdrive**: dropped by the tip of the t=74 Talon
  V-wing.
- No armour patch: the refills between the echoes are the level's repair.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), drafted for the **1 s rule** (≈ 12 characters a second, a
1.5 s lead, 0.4 s of silence; event lines drop after 6 s); `RadioTimelineLevel49Test` checks them with Rook and
without. **The radio's noise** (J3): step **5** (held from L47), the subtitles clean; the Choir's channel clean.
**Speakers**: Okafor, Varga, Rook (barks `requires: escort`), the Choir twice on the enemy channel. The
[Silence's rule](../../../story/README.md): the Choir's lines here are about what it lost, never what it fears.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, your telemetry says you're over Luna. You're not. Stay with it." |
| t=12 | Varga | "It isn't Luna, Lancer. It's the Choir remembering Luna. Shoot what it shows you." |
| t=23.5 (bark, `requires: escort`) | Rook | "Skitters. Like the first day. I hated the first day." |
| t=33 | Varga | "Mars now. The dust, the kites. It is going through the war in order." |
| t=62 | Varga | "Darts. That is not a Vrell memory, Lancer. That one is Vorne's." |
| t=70 | Okafor | "Shielded hulls in the memory. Arc them open." |
| t=84 | Varga | "Something big is forming ahead. I know that shape." |
| t=91.5 (The first warning (t=90; enemy channel)) | The Choir | "…you broke these… we remember… we remember everything we lose…" |
| t=97.5 (bark, `requires: escort`: as the echo forms (real t≈98)) | Rook | "The Brood Carrier. I watched you kill that thing over Luna." |
| Secondary met (`secondary-objective`, the 104th kill) | Okafor | "The memory is thinning. Good shooting, Aegis." |
| Secondary failed (by the first warning at the latest) | Varga | "Too many slipped past. Never mind: they were never there." |
| The first echo dead (`boss-destroyed`) | Varga | "It's coming apart. Your armour reads full again. It remembers you whole." |
| t=105.9 (as the Colossus shows (real t≈159)) | Varga | "The Colossus. The Hellas basin. It still bites, Lancer. Watch the ground." |
| The second echo dead (`boss-destroyed`; bark, `requires: escort`) | Rook | "Two down. Who's next?" |
| t=110.5 (The third warning (t=109; enemy channel)) | The Choir | "…he remembered this one… he is ours now… so it is ours…" |
| t=116.5 (as the station forms (real t≈218)) | Okafor | "That's Vorne's station. They pulled it out of his head." |
| t=119.5 (2.5 s of clock after the third echo's death (real t≈272)) | Varga | "That was all of it. There is nothing left for it to show us." |
| t=126.5 (bark, `requires: escort`) | Rook | "Luna, Mars, Ceres. All of it again in five minutes. Not bad." |
| Level end | Okafor | "Form up, Aegis. The core is ahead. One more push, and we all go home." |
<!-- /data -->

Timeline check (text, by the rule above): the timed lines close at ≈ 8.3, 17.2, 25.8, 40.2, 68.8, 75.2, 89.7, 97.5,
106.4, 168.6, 218.9, 229.1, 280.5 and 287.5; nothing overlaps. Varga's warning line closes before the first warning;
the Choir's lines ride the first and the third warnings (their sound under the line, as Vorne's line 1 over Level 48's
sting); no line starts during an echo's told move but the event lines, which wait for a free slot and drop rather
than talk over a tell. The second warning (t=154) has no line: Varga names the Colossus once it shows. The level end
comes at t=288, ≥ 4 s after the third echo's dissolve (≈ t=272) and after Rook's last bark; Okafor's line plays over
the debrief's wait, as every level's last line.

## Boss / mid-boss

The boss rush (Q6 a): the [bosses](../../../enemies/bosses/README.md#boss-rush-l49-echoes)' *Boss rush* rules and the
three *Echo* sections hold the fights, their HP, playbooks, tells, bounties and the par. Arena notes for this level:

- **The warning before each echo shows** (the Act 5 lesson): each echo forms only after its 5 s warning (see
  *Layout*); the first warning sounds with the approach's last wave gone.
- **Checkpoints**: one at each echo's warning, on **every** difficulty (the rush's exception to hard's no boss
  checkpoint); a checkpoint restores the refill's state (armour, shield and charges as at that warning).
- **Traits**: `forward` (the Carrier's broadside and the Sovereign's open core face the ship), `rear` (the Colossus's
  rear breach at least every third draw, the approach's rear waves), `shield-breaker` (the Sovereign's core hex
  shield, the approach's Gunships and Hornets).
- **The plan's fit** (the [Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)'s L49 row: the
  Ion Beam L5, the Tesla Coil Pod L5, a Micro-missile Pod L5, the Swarm Tail L5, the Bastion Shield on the Mk VI, 26
  of 27 MW; the Swarm Call; Rook's Autocannon): the rush's measurement uses this fit (M11's Level 49 part), five
  seeds, every echo inside 40–60 s.
- **Rook**: his rules per echo are each original's; his armour refills with the ship's
  ([Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft)).
- **Bounty**: the three echoes' **682** in Act 1 terms (the bosses' table) × 16.777 × this level's scale **0.51** (step
  G2's fit; the hand 0.66): **≈ 5,836 = 32.4 % of budget(49)** (see *Credit budget*); the boss rush's whole bounty
  counts in the typical haul.

## Music & ambience

**J8** (the cues are D-audio's [music](../../../audio/music/README.md#cues-per-level-act-7)): the approach plays
**each remembered act's own A theme as its memory copy** (an offline render through the memory filter: the earlier
tracks heard far off, hollow, slowed a little): [track 4](../../../audio/music/README.md#track-list) over Luna, track
8 over Mars, track 12 over Ceres, crossfading over 2 s as each memory forms (t=31, t=61); out under Varga's t=84 line.
Each echo: the boss warning (track 22, unfiltered), then its boss track's memory copy (track 18 for the Carrier and
the Colossus, track 19 for the Sovereign), falling away (2 s) at its death; the gaps under the bed alone. After the
third echo the bed alone to the level end. The alternative is the Act 7 B theme (track 17) through the filter for the
whole approach. Ambience ([sfx](../../../audio/sfx/README.md#ambience-per-setting)): the **memory copies** of the
built beds, `luna-memory`, `mars-dust-memory` and `belt-open-memory`, crossfading as each memory forms, then
**`vrell-hive`** for the unmaking. New sounds this level needs, for the act's sfx step: the memory forming and
dissolving (a soft rising and falling shimmer, no chime), the echoes' dissolve into motes, the refill (the
shield-restore chime, built); the echoes' own sounds as built, unfiltered. Freesound only for real things (CC0/CC-BY),
no bubbly or popping sounds.

## Credit budget

Budget(49) = 700 × 1.07⁴⁸ ≈ **18,010** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act 1
terms** (Skitter 5, Needler 12, Stinger 15, Spore Bomber 25, Dust Kite 4, Ghost Drone 14, Mote Swarm 2, Choir Herald
30, Dart Drone 2, Talon 12, Gilded Gunship 28, Hornet 26, the echoes 682, the crate 100, the secondary 100), × the act
factor **16.777** (1.6⁶), bounties also × the scale (the crate and the secondary are not scaled). The economy's shares:
60 % of the kills and the launched units, the echoes at 100 % (every won run kills all three), half the secret and the
secondary. The hand plan's **`bounty_scale` of 0.66** put the typical haul at 18,039 (+0.2 %) on the 30 waves; with
step G2's density waves the levelReport's fit is **0.51**: typical **17,981 (−0.2 %)**, perfect 26,637 (**1.48 ×**
budget, the ≥ 1.4 rule).

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 40 × 5 + Stinger 24 × 15 + Needler 23 × 12 + Spore Bomber 2 × 25 + Dust Kite 42 × 4 + Ghost Drone 22 × 14 + Mote Swarm 16 × 2 + Choir Herald 2 × 30 + Dart Drone 34 × 2 + Talon 32 × 12 + Gilded Gunship 2 × 28 + Hornet 2 × 26 + launched Dart Drone 12 × 2 | 17,445 | 10,467 |
| Boss rush: Brood Carrier echo 225 + Dust Colossus echo 240 + Iron Sovereign echo 217 | 5,836 | 5,836 |
| Secret: supply pod (hidden crate, 1% of budget) | 1,678 | 839 |
| Secondary: clear the memory | 1,678 | 839 |
| **Total** (bounty scale 0.51) | **26,637** | **17,981** |
| Budget(n) = the typical haul's target; typical -0 %, perfect 1.48 × budget | | 18,010 |
<!-- /data -->

The hand scale was the act's highest, above the bosses' hand **0.57** (step `m11-d-boss`), as Level 42's 0.63 was Act
6's: a 90 s approach earns little and the rush fills most of the level. The final act's density waves (step G2) made
the approach richer, so the measured scale is 0.51 and the echoes pay **≈ 32 %** of the budget (D-boss's estimate at
0.57: 36 %), still more than an act boss's 24 % for three fights (pending the user's review in round 94). **Balance plan**: the L49 visit fits
the Bastion Shield (forward 198 against the reference **196**, 1.01, the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)); 26 of 27 MW; Rook's Autocannon.

## Difficulty notes

- **Easy**: the formation lever thins the approach (≈ 135), the echoes' easy hooks (their originals' rests ×1.3, the
  global HP lever), the secondary's 65 % of the easy count.
- **Hard**: ≈ 185 in the approach, the echoes' hard hooks (rests ×0.8, the HP lever), the pod's 28 HP; the checkpoint
  per echo stays (the rush's exception).
- Every difficulty: the refills and the 8 s gaps are the same (a rule of the rush, not a lever).

## Concept art

The level's needs (round 84's proposals, then the M11 part's production art):

- the memory backdrop kit in Q10 b's palette and light: Luna's limb with Earth's crescent, Mars's disc and Valles walls, the
  belt's rock with Ceres's lights, each in the spectral tint and dissolving into dark tissue at the edges (soft, ≥ 32
  steps); the forming and dissolving transitions; the remembered Hellas basin floor as a `ground` strip;
- **one echo in the spectral tint** (ART-R-B's proposal, the bosses' tint rule: the original's colours desaturated
  toward violet-white, translucent edges fading to 0 inside the canvas), and the remembered units' tint on one sheet;
- the supply pod (whole, broken); the two briefing images;
- the round's captures: a remembered wave over Luna, the first echo's warning and forming, a refill (`ECHO 2 / 3`, the
  armour filling), readability crops of the tinted units' bullets against the tinted worlds at 1:1.

Round 94 (M11 part G, step ART-G, 2026-11-07; production art, pending the user's review): the memory backdrop kit,
the remembered Hellas basin floor (a ground tile set of 1,344 rows that wraps, so a crawl of any length is covered),
the supply pod and the tint's reference sheet by `tools/art/act7_memory.py`; the backdrop held in
`build/art-held/backdrop/level-49/` and the pod's sprites (`debris-supply-pod`, `-break`) in
`build/art-held/sprites/` until step G2's data claims them (its `backdrop` block from `concept/backdrop-proposal.yaml`,
the pod as a `debris` chunk `supply-pod`); the two briefing images in the
[briefing](../../../ui/briefing/README.md#concept-art) (`briefing-images-final-r94-a`). Each memory is drawn over the
tissue under one mask (`memory-mask.png`: forming from the top edge down, melting sides first; step G4's
`MemoryLooks`); the tint on the units, the echoes and the pod is the game's shader (G4), its constants on the
reference sheet. Every tile and piece passed a row/column straight-line scan (no run of 60 px in a tile or 120 px in a
piece; the longest 77 px, Luna's limb). Pending the user's review: the memories' big pictures as deep pieces over one
tissue tile (no seams between worlds), Valles's walls along both sides of the Mars stretch, gate 3 without a ground
tile (the Sovereign is its own anchored station). Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r94-a.png](concept/backdrop-final-r94-a.png) | Production backdrop: ten screens with tinted stand-ins and untinted bullets (Luna, Earth over the limb, Mars, Ceres, gate 1 forming and the L1 point, the Hellas floor forming and in the fight, gate 3's Ceres orbit, the unmaking), the six tile sets over the tissue, the pieces and the mask (sheet) | proposed |
| [concept/backdrop-final-r94-a.gif](concept/backdrop-final-r94-a.gif) | Luna melting into the tissue sides first, then Mars forming from the top edge down (t=28.5-34.5) | proposed |
| [concept/echo-tint-final-r94-a.png](concept/echo-tint-final-r94-a.png) | The spectral tint's reference for the game's shader: the twelve remembered units original and tinted over their worlds at 1:1 with untinted bullets and the measured luminance, the three echoes, the shimmer, the constants (sheet) | proposed |
| [concept/supply-pod-final-r94-a.png](concept/supply-pod-final-r94-a.png) | The remembered Coalition supply pod: 16 tumble frames in its own colours and in the tint, its break, in place at 1:1 (sheet) | proposed |
| [concept/supply-pod-final-r94-a.gif](concept/supply-pod-final-r94-a.gif) | The pod tumbling in the tint over Luna remembered, then broken | proposed |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (the tissue, the five memory tile sets, the six pieces at their stretches' middles, the mask's use) and a section proposal, for step G2 (not data) | proposed |

## Implementation

- [x] Level data `data.yaml` (M11's build): sections and speed, `reach-end` with the rush as the condition, the
      `kill_ratio` secondary (65 %, `boss_counted: false`, resolved by the first warning), waves, radio, budget,
      threat profile, briefing, the memory backdrops per section and per arena, music; `medium: vacuum`, the
      `beyond-gate` limits (`NO COVER`)
- [x] **Three boss fights in one level** (the gap analysis's large risk): the three echoes in order, each its arena
      (the Carrier's crawl, the Colossus's remembered basin floor as a `ground` layer for its fight only, the
      Sovereign's halt), the warning before each, the dissolve, the gaps, the refill (armour, shield, the special's
      charges as brought), a checkpoint at each warning on every difficulty; the echoes' launched units dissolving
      with them (step G2: the rush's gates hold the clock, the checkpoint and the refill at each gate's halt)
- [x] The spectral tint on the echoes and the remembered units (never on a bullet); the memory forming and
      dissolving without a flash (step G4: `EchoTint`, `MemoryLooks`, `Level49LooksTest`)
- [ ] `BREAK THE ECHOES`, `KILLS n / 157` then `ECHO n / 3` with its pips, each echo's tinted bar
      ([HUD](../../../ui/hud/README.md#act-7-additions-draft))
- [x] Wave script to the Act 2+ rules over the approach (`ActFloorTest`, `PacingTest`), ≥ 80 a minute (`DensityTest`,
      counted to the first warning), ≥ 60 % front, the medium-hull spacing, ≥ 4 s of calm after the third echo
- [ ] Radio voiced with the step-5 noise, every timed line at most 1 s late (`RadioTimelineLevel49Test`, with Rook and
      without); the Choir's lines on the clean enemy channel
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`);
      `TraitCoverageTest` covering Level 49
- [x] Briefing: both pages, each one screen (`BriefingLayoutTest`), with their images
- [x] Typical haul within ±5 % of budget(49) = 18,010 with the level's `bounty_scale` (`TypicalHaulTest`); one par for
      the rush, the Boss rush bonus once
- [ ] The rush measured with the plan's fit and Rook over five seeds at medium and hard (`levelReport`'s boss part):
      every echo 40–60 s, never quiet over 1.0 s, a still ship losing armour (step G2, `EchoFightTest`: the Carrier
      and the Sovereign 46–55 s on every seed, the Colossus's mean 57 s but its seeds 32–78 s; the longest quiet up
      to 1.5 s, the Colossus's ≈ 2–4 s with its vents shot; hard 63–89 s: open, round 94)
- [x] Music and ambience: the remembered themes and the boss tracks as memory copies, the beds' memory copies and the
      `vrell-hive` bed, the level's sounds (`Level49SoundsTest`; step G4: a track's memory copy is its number + 100)
- [x] Easy/hard variations; the autopilot flies it on every difficulty with Rook and with `--escort none`
- [ ] Production art and captures for the build's round

## Decisions

- 2026-11-04 (Act 7 design phase, step `m11-d-levels-4`; decided 2026-11-05, round 84): promoted from the
  act roster to a draft level document, hand tables only, no `data.yaml`. A 90 s approach through the Choir's
  memories of Luna, Mars and Ceres (160 enemies, ≈ 107 a minute to the first warning, 70 % front), then the boss rush
  (Q6 a) on the bosses' rules: warnings at t=90, 154 and 212, bars at t=98, 159 and 220, the last kill ≈ t=270,
  8 s gaps with the refill and a checkpoint per echo on every difficulty; the echoes' launched units dissolve with
  them; ≈ 288 s. Traits `forward`, `rear`, `shield-breaker` (J9), covered by the plan's own fit. The secondary
  `KILLS n / 104` (65 %), resolved before the rush; `BREAK THE ECHOES` then `ECHO n / 3`. `bounty_scale` 0.66 (the
  echoes ≈ 42 % of budget(49)); the radio's noise step 5; the Choir twice.
- 2026-11-04 (step `m11-d-act`, decided 2026-11-05, round 84): the bosses' *Boss rush* rules take the dissolve (unpaid) and the scale 0.66; the story step adopted
  the names and lines; the music as memory copies and the beds' memory copies (D-audio's cues); the open questions closed.
- 2026-11-05, round 84 (the user's page answers; decided as drafted unless stated, the alternatives rejected). The open items were:
  - **Q6** (round 84, the boss rush): drafted **a** (the Brood Carrier, the Dust Colossus and the Iron Sovereign, one
    phase each, refills and a checkpoint per echo, the Sovereign as Vorne's memory); **b** the Choir's own losses only
    (the Carrier, the Colossus and a dry Abyssal Maw: its water rules and dive); **c** two echoes (the Carrier and the
    Sovereign; no Colossus, no basin floor). The [bosses](../../../enemies/bosses/README.md#boss-rush-l49-echoes) hold
    the fights.
  - **J8** (round 84, the music): the remembered acts' themes through the memory filter over the approach (this draft)
    or track 17 through it.
- 2026-11-07 (M11 part G, step G2, pending the user's review in round 94; m11g-gaps.md §4, §6): the two open
  questions, closed by round 84 (Q6 a, J8), moved here from the *Open questions*: **the approach** as three remembered acts in order (≈ 90 s,
  counted for density), not the 10–15 s bridges between the echoes; **the secondary** as `kill_ratio` 65 % of the
  approach, resolved before the rush, not none. Built as trivia: each echo is a variant of its original's data (a
  `boss.echo` overlay in the original's `data.yaml`, read into a stat block of its own under the original's slug; the
  originals and their golden flights unchanged); the forming is a 3 s `rise` from the Carrier's station itself and the
  Sovereign's `form` (untouchable and silent from its arrival; the simulation's `formSeconds`); the dissolve fades every
  enemy projectile (the simulation's 0.5 s fade) and takes the units an echo launched, unpaid; the vents stay up through
  the dive and the buried stretch (a shot vent stays dead); the Sovereign's cover is an aimed pair from the core's
  centre; the crawling gates ease in over the gate (2 s) to 20 px/s; the checkpoint and the refill at each gate's halt
  (the bar), the built engine's rule; the second warning without a line; the secondary judged by the first warning
  (`until: warning`). Measured and tuned on the autopilot (the plan's fit and Rook, five seeds at medium): the echoes'
  HP to their 40–60 s (the Carrier 5,500, the Colossus 2,300, the Sovereign 7,400; the paper's 10,320 / 6,300 / 8,500
  assumed the full reference DPS on every open target), the Colossus's one phase firing its spiral at every open mouth
  (the simulation's rule: the eruption's spiral among them), the Carrier's brood never its first move (no pair is open
  yet), the rush's par 176 s; 22 density waves (the final-act rule) and the scale 0.51. Open: with the Colossus's three
  vents shot its dive and buried stretch are quiet again (up to ≈ 5 s); the Carrier's and the Sovereign's longest quiet
  up to ≈ 1.5 s (a cover pair's last bullet passing as the next tell starts, the Sovereign's first draw after its forming); the Colossus's seeded draw spreads its fight
  32–78 s (its mean ≈ 54 s).
