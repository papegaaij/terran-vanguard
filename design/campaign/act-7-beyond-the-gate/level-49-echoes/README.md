---
title: Level 49 – Echoes
design: approved
implementation: not-started
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
[Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft). There is **no `data.yaml`**: the build turns
the hand tables into one; no number is measured.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft; D-story may reword them):

> **Commander Okafor:** "Past Vorne's citadel our scans show places we know. Luna. Mars. Ceres. All in one spot, all
> at once. They are not there, Lancer. Fly through whatever it shows you: the hive's core lies on the other side."
>
> **Dr. Varga:** "The Choir remembers everything it has lost, and it will show you what you took from it. None of it is
> real, but it can still hurt you. Each time a memory breaks your readings snap back, armour and all. As if it
> remembers you whole, too."

*Images* (production art in M11's build, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-49-memory-scan`: the fleet's long-range scan with three terrain signatures stacked on one point
  (Luna's grey, Mars's rust, the belt's rock), "LOCATION: INVALID" stamped across it; the wing's lane a dotted line
  into the stack.
- Varga's page `level-49-echo-trace`: Level 07's record of the Brood Carrier in grey outline beside a new trace of the
  same shape in violet, "MATCH 98 %" in her hand.

*Hangar teaser* (shop screen before L49; Rook speaks, not voiced):

> **Rook:** "Varga says we're seeing old friends today. I never liked the old friends."

The plan's L49 visit fits the **[Bastion Shield](../../../player/shields/README.md)** in place of the Mk III (three
fights with refills between them: a deep buffer the refills top up) and gives Rook his Autocannon; 26 of 27 MW
([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The intel panel's `SPECIAL LIMITED`
names the Airstrike and the Orbital Lance if fitted (`NO COVER`, J12).

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `air` (`space` is `air` in vacuum, J12: every remembered wave, the Brood Carrier's echo); `ground` in the arenas only (the Dust Colossus's remembered basin floor, the Iron Sovereign's station layer) |
| Attack directions | front 70 % · sides 17 % · rear 13 % (21 / 5 / 4 of 30 approach waves; the act's ≥ 60 % front holds); the Colossus's rear breach |
| Density | 5 |
| Recommended traits | `forward`, `rear`, `shield-breaker` (**J9**: the roster's "any balanced loadout" is not a trait) |
| Hazards | The three echoes back to back (the bosses' documents); the remembered waves from every side |
| Boss / mid-boss | **boss rush**: the echoes of the Brood Carrier, the Dust Colossus and the Iron Sovereign (Q6 a) |
| Special limits | `NO COVER` (the Airstrike, the Orbital Lance; J12) |
| Sensor-suite detail | **none**: Vrell space, the Choir's memory, `air`, all directions · **L1**: + density 5, the hazard "three echoes in a row", the secondary `KILLS 0 / 104`, `SPECIAL LIMITED` (`NO COVER`) · **L2**: + the three echoes' portraits (tinted), Skitter, Needler, Dust Kite, Ghost Drone, Choir Herald, Dart, Talon, Gunship and Hornet portraits · **L3**: + `forward`, `rear` and `shield-breaker` highlighted, the wave strip marking the three echoes |

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
  enemies, **104 of 160** at medium (easy and hard by the formation lever, the same 65 %), the echoes and what they
  launch not counted (`boss_counted: false`). The tracker counts to its target, **`KILLS n / 104`**, until the first
  warning; it is **met** at the 104th kill (`DONE`) and **failed** once the approach's remaining enemies can no longer
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

| Section | t (s) | Scroll (px) | Layers and content | Purpose |
|---|---|---|---|---|
| 1. Luna remembered | 0–31 | 0–3,100 | `deep`: Earth's crescent over the grey limb; `low-air`: Luna's regolith and the L1 point's wreck lights, remembered, the edges dissolving into tissue; the supply pod (secret, t≈24) | Act 1's Vrell as the Choir remembers them: Skitters, Needlers, Stingers, Spore Bombers |
| 2. Mars remembered | 31–61 | 3,100–6,100 | `deep`: Mars's rust disc; `low-air`: Valles walls far below, dust veils (scenery, never over a bullet) | Act 3: Dust Kites, Ghost Drones, Mote Swarms, Choir Heralds |
| 3. Ceres remembered | 61–90 | 6,100–9,000 | `deep`: the belt's dark rock; `low-air`: tumbling rocks and Ceres's station lights far off | Act 5, **Vorne's memory**: Darts, Talons, shielded Gunships and Hornets (`shield-breaker`) |
| 4. The echoes | 90–≈270 | each arena's crawl or halt | the three arenas in turn (the table below) | **The boss rush** |
| 5. The unmaking | ≈270–288 | — | the last memory melts into the hive's living tissue; far ahead, the core's red-violet glow | ≥ 4 s of calm; the closing lines; level end ≈ t=288 |

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
arrival); the par is the build's measured mean.

**The echo's launched units** (the Carrier's Skitters and Needlers, the Sovereign's Darts and station missiles)
**dissolve with their echo** at its death, unpaid (they are its memory): each gap starts clean and the refill is
never under fire. Proposed here for the bosses' *Boss rush* rules (their *Death* row), pending the user's review.

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

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

Draft wave table:

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 2 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front |  |
| 8 | 1 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 11 | 1 | pincer | [Stinger](../../../enemies/air/stinger/README.md) | 4 | left, right | 2 a side |
| 14 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front |  |
| 17 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 20 | 1 | pair | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | ≥ 120 px apart; the remembered Level 03 bombers |
| 23 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | rear | Level 05's first rear wave, remembered |
| 26 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | The supply pod (secret, t≈24) |
| 28 | 1 | pincer | [Stinger](../../../enemies/air/stinger/README.md) | 4 | left, right |  |
| 30 | 1 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 33 | 2 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 10 | front | The memory turns to Mars (t=31) |
| 36 | 2 | stream | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 4 | front |  |
| 39 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 42 | 2 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | Its `aura` ring as built |
| 44 | 2 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 10 | left |  |
| 47 | 2 | stream | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front |  |
| 50 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | rear | From the bottom edge |
| 53 | 2 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | ≥ 120 px from the first Herald if it lives |
| 55 | 2 | swarm | [Dust Kite](../../../enemies/air/dust-kite/README.md) | 10 | right |  |
| 58 | 2 | stream | [Ghost Drone](../../../enemies/air/ghost-drone/README.md) | 3 | front |  |
| 62 | 3 | swarm | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 8 | front | The memory turns to Ceres orbit (t=61) |
| 65 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 68 | 3 | single | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | Its hex shield (`shield-breaker`) |
| 71 | 3 | swarm | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 8 | rear | Act 5's Darts from behind |
| 74 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 77 | 3 | pair | [Hornet](../../../enemies/air/hornet/README.md) | 2 | left, right | One a side, shielded; never beside the Gunship |
| 80 | 3 | single | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front |  |
| 82 | 3 | swarm | [Dart Drone](../../../enemies/air/dart-drone/README.md) | 8 | rear |  |
| 84 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The last wave; gone by ≈ t=89 |

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

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, your telemetry says you're over Luna. You're not. Stay with it." |
| t=9 | Varga | "It isn't Luna, Lancer. It's the Choir remembering Luna. Shoot what it shows you." |
| t=20 (bark, `requires: escort`) | Rook | "Skitters. Like the first day. I hated the first day." |
| The supply pod opened (event) | Varga | "That pod is ours. It remembers our things too." |
| t=33 | Varga | "Mars now. The dust, the kites. It is going through the war in order." |
| t=62 | Varga | "Darts. That is not a Vrell memory, Lancer. That one is Vorne's." |
| t=70 | Okafor | "Shielded hulls in the memory. Arc them open." |
| t=84 | Varga | "Something big is forming ahead. I know that shape." |
| t=91 (the first warning; enemy channel) | the Choir | "*…you broke these… we remember… we remember everything we lose…*" |
| t=100 (bark, `requires: escort`) | Rook | "The Brood Carrier. I watched you kill that thing over Luna." |
| Secondary met (`secondary-objective`, the 104th kill) | Okafor | "The memory is thinning. Good shooting, Aegis." |
| Secondary failed | Varga | "Too many slipped past. Never mind: they were never there." |
| The first echo dead (event) | Varga | "It's coming apart. Your armour reads full again. It remembers you whole." |
| t=161 | Varga | "The Colossus. The Hellas basin. It still bites, Lancer. Watch the ground." |
| The second echo dead (event, bark, `requires: escort`) | Rook | "Two down. Who's next?" |
| t=213 (the third warning; enemy channel) | the Choir | "*…he remembered this one… he is ours now… so it is ours…*" |
| t=223 | Okafor | "That's Vorne's station. They pulled it out of his head." |
| t=274 | Varga | "That was all of it. There is nothing left for it to show us." |
| t=281 (bark, `requires: escort`) | Rook | "Luna, Mars, Ceres. All of it again in five minutes. Not bad." |
| Level end | Okafor | "Form up, Aegis. The core is ahead. One more push, and we all go home." |

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
- **Bounty**: the three echoes' **682** in Act 1 terms (the bosses' table) × 16.777 × this level's scale **0.66**:
  **≈ 7,552 = 41.9 % of budget(49)** (see *Credit budget*); the boss rush's whole bounty counts in the typical haul.

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
secondary. A **`bounty_scale` of 0.66** puts the typical haul at **18,039 (+0.2 %)**, the perfect run at 25,590
(**1.42 ×** budget, the ≥ 1.4 rule).

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 40 × 5 + Needler 12 × 12 + Stinger 8 × 15 + Spore Bomber 2 × 25 + Dust Kite 30 × 4 + Ghost Drone 10 × 14 + Mote Swarm 16 × 2 + Choir Herald 2 × 30 + Dart Drone 24 × 2 + Talon 12 × 12 + Gilded Gunship 2 × 28 + Hornet 2 × 26 (1,166) | 12,911 | 7,747 |
| Boss rush: Brood Carrier 225 (8 sacs × 12 + the core 129) + Dust Colossus 240 (the mouth 222 + 3 vents × 6) + Iron Sovereign 217 (the core 181 + 6 SAM batteries × 6) (682) | 7,552 | 7,552 |
| Echo spawns: Skitter 8 × 5 + Needler 4 × 12 + Dart Drone 36 × 2 (a typical rush, killed before they dissolve) (160) | 1,772 | 1,063 |
| Secret: the supply pod (hidden crate) | 1,678 | 839 |
| Secondary: clear the memory (65 %) | 1,678 | 839 |
| **Total** (bounty scale 0.66) | **25,590** | **18,039** |
| Budget(n) = the typical haul's target; typical +0.2 %, perfect 1.42 × budget | | 18,010 |

The scale is the act's highest, above the bosses' hand **0.57** (step `m11-d-boss`), as Level 42's 0.63 was Act 6's:
a 90 s approach earns little and the rush fills most of the level, so its bounties pay more each. At 0.66 the echoes
pay **≈ 42 %** of the budget (D-boss's estimate at 0.57: 36 %), more than an act boss's 24 % for three fights; the
alternative (the echoes held at 36 % with a richer approach) is an open question. **Balance plan**: the L49 visit fits
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

- [ ] Level data `data.yaml` (M11's build): sections and speed, `reach-end` with the rush as the condition, the
      `kill_ratio` secondary (65 %, `boss_counted: false`, resolved by the first warning), waves, radio, budget,
      threat profile, briefing, the memory backdrops per section and per arena, music; `medium: vacuum`, the
      `beyond-gate` limits (`NO COVER`)
- [ ] **Three boss fights in one level** (the gap analysis's large risk): the three echoes in order, each its arena
      (the Carrier's crawl, the Colossus's remembered basin floor as a `ground` layer for its fight only, the
      Sovereign's halt), the warning before each, the dissolve, the gaps, the refill (armour, shield, the special's
      charges as brought), a checkpoint at each warning on every difficulty; the echoes' launched units dissolving
      with them
- [ ] The spectral tint on the echoes and the remembered units (never on a bullet); the memory forming and
      dissolving without a flash
- [ ] `BREAK THE ECHOES`, `KILLS n / 104` then `ECHO n / 3` with its pips, each echo's tinted bar
      ([HUD](../../../ui/hud/README.md#act-7-additions-draft))
- [ ] Wave script to the Act 2+ rules over the approach (`ActFloorTest`, `PacingTest`), ≥ 80 a minute (`DensityTest`,
      counted to the first warning), ≥ 60 % front, the medium-hull spacing, ≥ 4 s of calm after the third echo
- [ ] Radio voiced with the step-5 noise, every timed line at most 1 s late (`RadioTimelineLevel49Test`, with Rook and
      without); the Choir's lines on the clean enemy channel
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`);
      `TraitCoverageTest` covering Level 49
- [ ] Briefing: both pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(49) = 18,010 with the level's `bounty_scale` (`TypicalHaulTest`); one par for
      the rush, the Boss rush bonus once
- [ ] The rush measured with the plan's fit and Rook over five seeds at medium and hard (`levelReport`'s boss part):
      every echo 40–60 s, never quiet over 1.0 s, a still ship losing armour
- [ ] Music and ambience: the remembered themes and the boss tracks as memory copies, the beds' memory copies and the
      `vrell-hive` bed, the level's sounds (`Level49SoundsTest`)
- [ ] Easy/hard variations; the autopilot flies it on every difficulty with Rook and with `--escort none`
- [ ] Production art and captures for the build's round

## Open questions

- **The approach**: drafted as three remembered acts in order (≈ 90 s, counted for density); the alternative is the
  gap analysis's 10–15 s bridges of small waves between the echoes (against the bosses' clean 8 s refill gaps).
- **The secondary**: drafted as `kill_ratio` 65 % of the approach (Level 42's form), resolved before the rush; the
  alternative is none (the rush is the level).

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
