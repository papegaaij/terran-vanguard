---
title: Level 42 – Ascendant
design: approved
implementation: in-progress
art: proposed
depends-on: [../../../enemies/bosses/ascendant, ../../../enemies/bosses/abyssal-maw, ../../../enemies/air/talon, ../../../enemies/air/chimera, ../../../enemies/air/mirror-interceptor, ../../../enemies/air/harrow, ../../../enemies/air/gilded-gunship, ../../../enemies/air/hornet, ../../../enemies/air/dart-drone, ../../../enemies/air/skitter, ../../../enemies/air/mote-swarm, ../../../world/jovian, ../../../player/wingmen, ../../../story/characters/vorne, ../../../ui/hud, ../../../systems/economy]
updated: 2026-11-02
---

# Level 42 – Ascendant

## Summary

The Act 6 finale and, for now, the release's end. Aegis Wing climbs into Callisto orbit after the Ascendant, through
what is left of Vorne's home fleet: Talon wings and Chimera swarms, two gloss-black cruisers firing broadsides along
the flanks (Level 28's Regent class), Gunships, Mirror Interceptors and Harrows from every side. Then the scroll halts
and the [Ascendant](../../../enemies/bosses/ascendant/README.md) comes about, bow-on: the gold warship first, then the
Vrell half erupting through its hull while Vorne shouts for the only time, then the hull breaks and his Vrell
lifeboat runs for the gate. It always escapes; damage on it pays. Okafor closes the act: "*Then we follow him.*"
About 92 s of approach, the fight on a halted arena (≈ 150 s at medium with the escape), the closing lines, and the
act's end naming Act VII. Air only (`NO GROUND`). Density 5.

**Status: approved 2026-10-31 (round 72, D.8)** (drafted in the Act 6 design phase, step `m10-d-levels-3`, 2026-10-31). Written from the act roster, the gap analysis's recommended answers (**Q6 a** the bow-on fight and the
escape, **Q1 c** the mirror panels, **Q3 a** the three-way war, **Q9 a** the elite livery, **Q10 a** the Level 42 mood,
**Q12 a** the briefing's page from the files; judgement calls **J1** the reference DPS 168, **J2** the medium, **J5** no
new tiny unit, **J6** Vorne's only shout, **J9** the music, **J10** Okafor on the carrier, all decided 2026-10-31 (round 72)) and
the wave 1 and 2 documents. The boss document owns the fight (phases, parts, playbooks, tells, HP, the lifeboat, the
bounty's split, Vorne's lines and their order); this document owns the approach, the timeline, the radio's placing and
delays, the economy and the act end. Every number is a hand-written first draft at medium for M10's Level 42 part to
measure with `levelReport`; there is no `data.yaml` yet.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Rendered from the level's
[data.yaml](data.yaml) (M10 part H, step H2: the approved texts as they are):

<!-- data: briefing -->
> **Commander Okafor:** "Vorne is aboard the Ascendant, in Callisto orbit, behind what is left
> of his home fleet. Break the screen, then break his ship. He does not leave orbit, Lancer."
>
> **Dr. Varga:** "From his own files: the Ascendant is half warship, half Vrell. Generators feed
> the bow's shield, mirrors cover the batteries. If the Vrell half wakes, its sacs heal: burn
> them steadily."
<!-- /data -->

Varga's page is the files' page (Q12 a: the Level 41 files used in the next briefing).

*Images* (production art in M10's Level 42 part; [briefing images](../../../ui/briefing/README.md)):

- Okafor's page `level-42-callisto-orbit`: a tactical chart of Callisto orbit: the CDF fleet's arc from the citadel,
  the home fleet's last ships as gold contacts, two cruiser silhouettes on the flanks, the Ascendant's gold mark at
  the top with its heading line toward the Tether Gate's bearing.
- Varga's page `level-42-ascendant-files`: a page of Vorne's files as Varga decoded them: the Ascendant's bow-on
  schematic in Helix's blue lines (the generators, the mirror batteries, the bow cannon), a Vrell cross-section
  overlaid in teal where the plates should be, a lifeboat bay marked with a question mark.

*Hangar teaser* (shop screen before L42; Okafor, not voiced):

<!-- data: teaser -->
> **Commander Okafor:** "This is where Vorne makes his stand. Fit for one big target and
> everything it launches."
<!-- /data -->

## Threat profile

Rendered from the data. Directions are counted by each wave's `from` (the intel's rule), by enemy (the README's
draft counted waves: 25 of 31 from the front); the cruisers' broadsides cross from the flanks; in the fight the boss
fires from the top and is not in the numbers.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (`space` is `air` in vacuum: the screen, the Ascendant and what it launches); `low-air` (the cruisers, invulnerable set pieces). No ground layer: the Bomb Rack and Rook's Mortar read **`NO GROUND`** (J2: the approach keeps the fleet's hulls on `low-air`) |
| Attack directions | front 83% · sides 11% · rear 6% (counted by enemy; the cruisers' broadsides cross from the flanks; in the fight the boss fires from the top and is not in the numbers) |
| Density | 5 |
| Recommended traits | `piercing` (the bow's stacked parts), `homing` (the broods, the lifeboat's weave, the Harrows); `side` opens the mirror panels from the far side |
| Hazards | The cruisers' broadsides and point defence (t≈24–58); Harrow death-burst rings; the Mirrors' bounced shots; the Ascendant's spinal lance, fans, salvos, spore spiral, bile lobs and the lifeboat's spore wake |
| Boss / mid-boss | Act boss [Ascendant](../../../enemies/bosses/ascendant/README.md) |
| Sensor-suite detail | **none**: Callisto orbit, `air` only, all directions. **L1**: + the directions, density 5, hazards "broadsides" and "death-burst rings", the objective `STOP THE ASCENDANT`. **L2**: + Talon, Chimera, Gilded Gunship, Hornet, Mirror Interceptor, Harrow, Dart Drone, Skitter, Mote Swarm portraits; the boss's name and silhouette (`ascendant`). **L3**: + `piercing` and `homing` highlighted; the timeline strip marks the cruisers (24 s), the Harrow cross (67 s) and the boss (92 s); 1 secret. |
<!-- /data -->

*Varga's intel line* per sensor-suite level (to fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Callisto orbit. Both fleets, no ground at all." |
| L1 | "A screen from every side. Then his flagship." |
| L2 | "Talons, Chimeras, Mirrors, Harrows. The Ascendant." |
| L3 | "Piercing for the bow, homing for the broods." |

## Objective

- **Primary** `reach-end` with the arena as the condition, as Levels 21, 28 and 35: the level ends when the Ascendant's
  fight is over (the hull broken and the lifeboat `ESCAPED`) and the closing lines have played. The briefing and the
  objective line read `STOP THE ASCENDANT` (the boss's name on the bar, then the lifeboat's escape bar). It is not a
  `boss` primary, and it is won by the escape: the lifeboat cannot die ([the lifeboat](../../../enemies/bosses/ascendant/README.md#the-lifeboat)).
- **Secondary** "Clear the orbit" (`kill_ratio`, the 65 % rule): destroy at least **65 %** of the approach's enemies
  (**104 of 159** at medium: the waves' planned units, the Gunships' Darts counting as kills but not in the
  requirement, step H2 as Level 40's rule; the draft's "112 of 171" counted them in), judged at the level's end. The Ascendant and what it launches
  are not counted (`boss_counted: false`), nor Talons the Vrell kill (prey). Pays **100** in Act 1 terms (1,049 in
  Act 6); the typical haul counts it at 50 %. No radio line. (The gap analysis drafted none; Levels 21, 28 and 35 each
  have one; decided 2026-10-31 (round 72).)

## Layout

Scroll **160 px/s** in open orbit, slowing to a **halt** under the Ascendant (eased over the last 6 s, so nothing pops
in), then easing to 60 px/s over the empty orbit after the escape. Setting: the
[Jovian system](../../../world/jovian/README.md#sub-locations-per-level)'s Callisto orbit, vacuum
([vacuum rules](../../../world/belt/README.md#vacuum-rules)). **Light** ([mood](../../../world/jovian/README.md#moods-per-level-q10),
Q10 a): black space, Callisto's curve below with the citadel's lights on its night side, Jupiter's cream limb at the
right edge, both fleets' running lights; the Ascendant's gold under the strong top-left rim light, then the Vrell teal
in its phase 2. Motion budget: the waves and the cruisers' broadsides in the approach; in the arena the bow's parts
and the eruption; after the escape the wreck's halves drifting apart.

**The arena and the clock.** The last wave enters at **t=83**; no wave after it (the pre-boss pause, t≈86–92). The
**boss warning** (track 22) sounds at **t=88** with the scroll's last slowing, and the **boss checkpoint** is taken
there (the spec's; none on hard). The arena section starts at **t=92** (a one-second section `speed: 0, arena: true`,
Levels 28 and 35's), as the bow shows at the top edge: the hull slides in over 6 s, invulnerable until in place, with
the bar `ASCENDANT` ([intro](../../../enemies/bosses/ascendant/README.md#arena)). The level clock **halts at t=93 while
the Ascendant lives, the escape included**: the hull's break at **K** (the heart at 0: the chain, the act boss's
screen flash at K+2.5 s, the credit shower over the wreck, collectable: the scroll is halted), the lifeboat's breakout
at K+3 s and its 14 s climb to the top edge; **E** (`ESCAPED`, ≈ K+17) ends the fight and the clock resumes
(`hold_after_boss: 0`): it reads **t=93 at E**. The section after the escape is the level's **outro for the pacing
checks** (no enemies by design: `PacingTest` and `ActFloorTest` measure the approach up to the halt, as Levels 28 and 35).

**Rook and the escort.** Rook flies his slot with his Autocannon (the plan's L42 fit) and follows the boss's
[escort rules](../../../enemies/bosses/ascendant/README.md#the-escort-rook) (the generators first, the far-side flank, the
weakest sac, the lifeboat in phase 3). With `--escort none` the level is the same without him and his lines.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Callisto Orbit | 0–22 | 0–3,520 | 160 | clear | `deep`: Callisto's curve below with the citadel's lights on its night side, Jupiter's cream limb at the right edge, the CDF fleet closing from below. | Talons, Chimeras, popcorn; the Vrell hunting Talons (t=14). |
| 2. The Home Fleet | 22–54 | 3,520–8,640 | 160 | clear | `deep`: The home fleet's lights far ahead. `low-air`: **cruiser A** on the left flank (t≈24–42), **cruiser B** on the right (t≈40–58), each overtaking from the bottom edge and falling back off it; a CDF frigate pair trading fire with cruiser A from the right flank (friendly marker); a dead home-fleet tender drifting (t≈42, the secret). | The broadsides; popcorn and small waves only (no medium hull beside a cruiser). |
| 3. The Screen | 54–84 | 8,640–13,440 | 160 | clear | `deep`: The home fleet's burning hulks falling toward Callisto; the Ascendant's gold glint high ahead. | Gunships and Darts, the Mirror pincer and rear ambush, Hornets, the Harrow cross (t=67) and stream. |
| 4. The Berth Line | 84–86 | 13,440–13,760 | 160 | clear | `deep`: The Ascendant's drive glow growing over its glint. | The last wave has entered (t=83); a CDF supply drone's armour patch (t=85). |
| 5. The Berth Line, braking | 86–92 | 13,760–14,243 | 1 | clear | `deep`: The drive glow swinging away as the Ascendant comes about. | The pre-boss pause (t≈86–92); the boss warning at t=88 (the checkpoint there). |
| 6. The Ascendant | 92–93 | 14,243–14,243 | 0 | clear | `air`: the hull filling the top of the field, bow-on ([arena](../../../enemies/bosses/ascendant/README.md#arena)). `deep`: Callisto's curve, the fleets. | The boss fight (≈ 150 s at medium with the escape); the scroll halted and the clock stopped until `ESCAPED`. |
| 7. The Empty Orbit | 93–113 | 14,243–15,383 | 60 | clear | `deep`: The CDF fleet's lights closing in; the gate's bearing marked by the lifeboat's last flash. `air`: The wreck's two halves drifting out of the field's sides, burning at the breaches. No enemies. | The closing lines (E+2 to ≈ E+18.7); the level ends at t=113 (E+20). |
<!-- /data -->

**The home fleet's cruisers** (Level 28's built set piece, [the cruisers](../../../enemies/bosses/abyssal-maw/README.md#the-cruisers-draft),
reused with their final art: the Regent class in gloss black and gold): invulnerable `low-air` hulls on one flank at a
time, no HUD name or bar, paying nothing. Each overtakes the wing from the bottom edge over 4 s, keeps pace for ≈ 10 s
and falls back off the bottom edge. Its **broadside** (the 1.2 s tell: the gun ports glowing hot amber, amber tracer
lines across a 70 px band; then 7 shells across the band from its side, `medium` 6, 260 px/s) fires every **3.5 s**,
its band on the ship's y, then a random band, in turn; **never two bands at once** (one cruiser at a time: Level 28's
from-20-s double band is not used); its **point defence** fires an aimed `burst` of 3 every 2.5 s. Section 2's waves
are popcorn and small units only, so nothing medium crowds a band. The CDF frigates on the other flank carry the friendly marker (round
70's; Level 29's rule), so a gold-and-black hull is always the enemy's and a white one ours.

## Waves

Formation names from the [formation vocabulary](../../../enemies/README.md#formation-vocabulary). The **act HP
factor** for the returning `medium`+ units (the proposed reference **168** at Level 42 against the unit's first level,
J1): Gilded Gunship ×168/124 (88 → 119; its [Act 6 shield](../../../enemies/README.md#enemy-shields) 24 → 33), Hornet
×168/128 (96 → 126, shield 24 → 32), Mirror Interceptor ×168/148 (120 → 136; its mirror's heat by the same factor).
The `small` units (Talon with its Act 6 shield, Chimera, Harrow) keep their pages' HP with the beam era's ×2, the
`tiny` ones (Darts, Skitters, Motes) 1 HP. Every number is a hand estimate for `levelReport`.

- **Density and pacing** (the Act 2+ floor): **171 enemies at medium** in the 92 s approach (the waves' 159 and the
  Gunships' **12 Darts**), **≈ 112 a minute** (the act's band 90–120): a wave every 2.5 s in sections 1 and 3, every
  3 s beside the cruisers (section 2, where the broadsides carry the threat). After t=83 **no wave enters**: the
  pre-boss pause (t≈86–92) is the level's one stretch over 2 s with nothing on the screen at the floor (as Level 35's);
  the pacing checks must name it.
- **The Ascendant's launches** (its phase 1 Dart streams and phase 2 Chimera broods, at most every third draw) count
  into the level's enemies, not its density (the boss rule); the typical haul budgets them at the expected count
  (**32 Darts, 16 Chimeras**).
- **Tiny units** (J5): the Gunships' Darts, the Skitters and the Mote Swarms (and the boss's Darts).
- **The three-way war** (Q3 a): the Skitter swarm of t=14 **hunts** the Talons of t=11.5 (prey; ram); a Talon the Vrell
  kill pays nothing ([the rule](../../../enemies/README.md#the-three-way-war)). Never the Ascendant or its launches
  (a boss and hybrids are never prey).
- **Hulls apart**: no `medium` air hull beside a cruiser (section 2 has none); the Mirror pincer and rear ambush keep
  ≥ 80 px between hulls, the rear ambush entering after the pincer has left.
- **Directions**: 25 of 31 waves from the front (81 %); the side pincers and stream have edge warnings, the rear V-wing
  and the Mirror rear ambush a BOTTOM warning, the Harrow cross every edge, 3 s ahead.
- **Totals**: Talon 43 (5 leaders) · Mote Swarm 42 · Chimera 34 · Skitter 18 · Harrow 13 · Mirror Interceptor 5 ·
  Gilded Gunship 2 (+ 12 Darts) · Hornet 2.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 4 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Callisto's curve below |
| 6.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 9 | 1 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 11.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front (right) | **Prey** of the Skitters of t=14 |
| 14 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Vrell **hunters** (ram) of the t=11.5 Talons |
| 16.5 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | No leader (a hybrid wing) |
| 19 | 1 | pincer | [Talon](../../../enemies/air/talon/README.md) | 6 | sides | Edge warnings at t=16 |
| 21.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 24 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | Cruiser A overtakes on the left flank |
| 27 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | The leader drops the Overdrive |
| 30 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | The README's Skitter swarm: a stream (Skitters fly no flock) |
| 33 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | |
| 36 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 39 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | Cruiser A falls back; cruiser B overtakes on the right (t≈40) |
| 42 | 2 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | The dead tender drifts in (the secret) |
| 45 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 48 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 51 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front | Cruiser B falls back from t=54; the README's Skitter swarm: a stream |
| 54.5 | 3 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 2 | front | ≈ 12 Darts (3 every 5 s, at most 6 alive); the last Gunship drops the Shield cell |
| 57 | 3 | pincer | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 2 | sides | Edge warnings at t=54; 1 + 1 |
| 59.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 62 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | rear | From behind: it rises from the bottom edge (BOTTOM warning at t=59) |
| 64.5 | 3 | line abreast | [Hornet](../../../enemies/air/hornet/README.md) | 2 | front | ≥ 80 px between the hulls |
| 67 | 3 | cross | [Harrow](../../../enemies/air/harrow/README.md) | 8 | all edges | 2 per edge, warned on every edge at t=64 (hard 2 per edge: the README's 3 is beyond the Harrow's cross of 4–8) |
| 69.5 | 3 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 72 | 3 | rear ambush | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 3 | rear | BOTTOM warning at t=69; each in its lane, ≥ 80 px apart; the pincer of t=57 has left (it holds 7 s) |
| 74.5 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 6 | front | |
| 77 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | |
| 79.5 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | sides (alternating edges) | From alternating sides, edge warnings at t=76.5 |
| 82 | 3 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 83 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | The last wave: the pre-boss pause t≈86–92 |
| 92 | 6 | boss | [Ascendant](../../../enemies/bosses/ascendant/README.md) | 1 | top (the bow slides in from the top edge; it hangs there, bow-on) | At the arena's start: the bar and the checkpoint; the Darts and Chimeras it launches as in its spec; no other waves in the arena |

Totals: Talon 43 · Mote Swarm 42 · Chimera 34 · Skitter 18 · Gilded Gunship 2 · Mirror Interceptor 5 · Hornet 2 · Harrow 13 · Ascendant 1.
<!-- /data -->

## Ground targets

None: the level has no ground layer (J2, decided here). The citadel is `deep` scenery; the cruisers are `low-air` set
pieces; the Ascendant is an `air` boss.

## Hazards

- **The approach**: the cruisers' broadsides and point defence (*Layout*), the Harrows' death-burst rings (thin them far
  out), the Mirror Interceptors' bounced shots (the player's own fire, magenta, slow), the Gunships' rings and Darts,
  the Hornets' salvos. No terrain, no weather (vacuum).
- **In the arena**: the bow is a body (contact `huge` 25, the ship pushed out); the spinal lance (one told line at a
  time), the batteries' bursts behind their mirrors, the gold fans, missile salvos and Dart streams; the eruption; the
  Vrell playbook (broods, spore spiral, bile lobs with their marks, grafted guns); the lifeboat's spore wake
  ([phases](../../../enemies/bosses/ascendant/README.md#phases), [tells](../../../enemies/bosses/ascendant/README.md#tells)).
- **Specials** ([Jovian media](../../../world/jovian/README.md#media-per-level)): the Airstrike flies and the Orbital
  Lance has cover; their boss rules cap them per part ([duration and HP](../../../enemies/bosses/ascendant/README.md#duration-and-hp));
  the EMP Burst empties the cannon's shield and shatters all four mirrors, stuns the launched Darts and Chimeras, never
  the sacs' regeneration; the Bomb Rack reads `NO GROUND`; the Sonar Pulse reads `NO WATER`. The IFF spoofer fools
  nothing here (the hand plan stows it at this visit).

## Secrets and pickups

- **The dead tender** (the secret, t≈42, x≈330, drifting down the field's right half on `low-air`): a home-fleet supply
  tender the Vrell boarded and picked clean, its strongbox still sealed on its spine. A small breakable object on the
  play plane (Level 30's buoy rule: the strongbox's housing takes **4 hits**, a beam's touch one hit per 0.25 s);
  broken, it frees a hidden crate worth **100** in Act 1 terms (1,049). The Salvage scanner's glint shows it at any
  range. Its event line, Okafor: "*A home-fleet tender. The Vrell took its crew, not its strongbox. Take it.*"
- **Overdrive**: dropped by the leader of the t=27 V-wing.
- **Shield cell**: dropped by the second Gunship of the t=54.5 column.
- **Armour patches** ×2: a CDF supply drone (white, on `low-air`, marked friendly) releases one at (240, 380 y-up) at
  t=85, in the pre-boss pause; the Ascendant's eruption drops one (its spec).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), draft text for the **1 s rule** (12 characters a second plus a
1.5 s lead; 0.4 s of silence); event lines drop after 6 s. Rook's lines carry `requires: escort`. **Speakers:
Okafor** (from the carrier, J10), **Varga**, **Rook** on the friendly channel, **Vorne** on the enemy channel (the
amber plate, the steady interference over his portrait) and **the Choir** (its own channel's dress) at the eruption
([characters](../../../story/characters/README.md)). The arena's clock halts, so the boss lines are events
(`boss-arrival`, `boss-phase`), queued as `PHASE` lines (they wait for a gap, stale after 20 s, dropped at the
escape). **Vorne's lines and the Choir's are the boss document's** ([Vorne](../../../enemies/bosses/ascendant/README.md#vorne),
lines 1–7, wording decided 2026-10-31 (round 72)): this table places them, adds the friendly lines between them and sets the
delays. **The closing lines are queued as `CLOSING` lines in order** (Level 21's rule: they never go stale and the
debrief waits for the last); `delay` counts from **E** (`ESCAPED`).

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Callisto orbit. Everything Vorne has left is between us and the Ascendant. Break through, Lancer." |
| t=12 | Varga | "Hybrids and Talons in his screen, and the Vrell are hunting both. Let them." |
| t=23.5 | Okafor | "Home-fleet cruisers on your flanks. Watch the gun ports: broadsides." |
| t=31 | Rook | "Seen these at Europa. They still can't hit a moving target." |
| t=54 | Varga | "Mirror interceptors. Flank them, burn the mirror, or break it with the Tesla." |
| t=64.5 | Okafor | "Bursters from every side! Thin them far out." |
| t=71 | Rook | "Vorne's flagship is breaking orbit. If we're doing this, it's now." |
| t=82 | Okafor | "The Ascendant is coming about. All Aegis units: everything you have." |
| The hull in place (`boss-arrival`), the enemy channel: the boss document's line 1 | Vorne | "The Ascendant, Lancer. Helix built her, the Vrell grew her, and I command her. You should have named your price." |
| After it (`PHASE`) | Varga | "Generators feed the bow shield. The batteries hide behind mirrors: hit them from the far side." |
| The first generator destroyed, or 25 s into phase 1 (`boss-phase`): line 2 | Vorne | "Okafor sends you in alone again. She spent Aurelia's people the same way, once." |
| After it (`PHASE`) | Okafor | "Aurelia asked us for help, Vorne. We came. You never did." |
| The bow cannon below half (`boss-phase`): line 3 | Vorne | "You fight well for a hired gun. It changes nothing. The door is open, and I hold the key." |
| After it (`PHASE`) | Rook | "He talks a lot for a man in a gold box." |
| The eruption, its first second (`boss-phase`): line 4, the Choir's own channel | The Choir (distorted) | "…the one who opened… is inside us now… he was always inside us…" |
| Right after it: line 5, **shouted** (the voice check's one exception) | Vorne | "No! We had an agreement!" |
| After it (`PHASE`) | Varga | "The Vrell half is waking! The sacs heal: hold your fire on one until it bursts." |
| The heart opens, two sacs dead (`boss-phase`) | Varga | "Two sacs down. The heart is open!" |
| The hull breaks, K (`boss-phase`, `delay: 0.5`) | Okafor | "She's breaking up!" |
| K + 4 s, the lifeboat away (`boss-phase` at the breakout K+3, `delay: 1`): line 6, cold again | Vorne | "A misunderstanding. They still need me; someone must open the way. The gate, then. Goodbye, Lancer." |
| `ESCAPED` (E, `CLOSING`, `delay: 2`) | Varga | "He's past our reach, heading for the Tether Gate. The Choir won't keep his deal." |
| Then (`CLOSING`, with Rook) | Rook | "He ran. After all that, he just ran." |
| Then (`CLOSING`), ≥ 4 s after E: line 7, the level's and the act's last voice | Okafor | "Then we follow him." |
<!-- /data -->

Timeline check (text model): t=1 closes ≈ 10.7, t=12 ≈ 19.8, t=21.5 ≈ 28.7, t=31 ≈ 37.4; the tender (in reach
t≈42–48) is spoken by ≈ 56 at the latest; Varga's t=55 then starts up to ≈ 1 s late (the part checks it, or the
tender's line drops after its 6 s) and closes ≈ 62.9; t=63.5 ≈ 68.7 (the Harrow cross at 67, warned at 64), t=73
≈ 80.0, t=80.5 ≈ 87.7, before the warning at t=88. **Step H4b** (`RadioTimelineLevel42Test` on the game's queue, its
lines held as typed and voiced): Okafor's t=21.5 started ≈ 2 s late, t=63.5 ≈ 2.5 s and t=80.5 ≈ 3 s, so the approach
is retimed: Okafor's t=21.5 to 23.5 (before cruiser A rises at 24), Varga's t=55 to 54 (the interceptors' edge
warnings), Okafor's t=63.5 to 64.5 (the Harrow cross's warning), Rook's t=73 to 71, Okafor's t=80.5 to 82 (it runs
under the warning at 88); every timed line now starts at most 1 s late. The tender's line plays when the strongbox
opens by ≈ t=43 (later it waits for a gap before Varga's t=54 and drops after its 6 s). In the arena: Vorne's arrival line (≈ 11.3 s of subtitle) then
Varga's (≈ 9.3 s) fill the intro and the first seconds of phase 1; each of Vorne's lines 2 and 3 is followed by one
friendly answer; at the eruption the Choir's line, Vorne's shout and Varga's run back to back (≈ 14 s, inside the 4 s
eruption and the first seconds of phase 2). From K: Okafor K+0.5 to ≈ K+3.5, Vorne's line 6 K+4 to ≈ K+15.3, inside the
escape (E ≈ K+17). The closing queue from E, in order: Varga E+2.0 to ≈ E+10.3, Rook ≈ E+10.7 to ≈ E+15.2, Okafor
≈ E+15.6 to ≈ E+18.7 (without Rook: ≈ E+10.7 to ≈ E+13.8, still ≥ 4 s after E); the level ends at **E+20** (t=113) and
the debrief waits for the last line.

**Story notes.** Vorne's seven lines are the boss document's, in its order: the arrival, two taunts (the second at
Okafor and Aurelia: her one answer points to Level 37, where Aurelia asked the CDF for help and got it, and does not
repeat the intro's "*I was at Aurelia. I remember.*" or her Level 37 memory of 2166), the Choir's claim on him ("*…is
inside us now…*", not Level 37's line), **his only shout** (J6; the voice check's one exception), and his cold goodbye, still believing
the Choir needs him. He says nothing of the Silence (Level 41 named it). Varga's closing line says what he will not:
the Choir will not keep the deal (Act 7's seed). Okafor's "*Then we follow him.*" is the radio's and the act's last
voice, so the act outro does not repeat it (the gap analysis's "the radio or the outro, not both").

## Boss / mid-boss

Act boss: [Ascendant](../../../enemies/bosses/ascendant/README.md) (Q6 a, bow-on over a halted arena). Its phases,
parts, shields and mirrors, the regeneration, the playbooks, tells, HP, the lifeboat and its cap, the escort's rules,
the hull's break and Vorne's lines live in the spec. Arena notes for this level:

- The warning sounds at t=88 with the scroll's last slowing, before the hull shows (the Act 5 lesson); the checkpoint
  is taken there on easy and medium (the spec), none on hard; the arena starts at t=92 as the bow enters the top edge.
- **Traits**: `piercing` (the cannon and the batteries stacked on the bow) and `homing` (the broods, the lifeboat's
  weave); the roster's pair, kept. `side`: the far third of the field opens a face's mirrors.
- **The plan's fit** (the [Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft)'s L42 row: the Ion
  Beam L5, the Tesla Coil Pod L3, a Micro-missile Pod L5, the Swarm Tail L3 on the Mk V+, 23 of 25 MW; the spoofer
  stowed; Rook's Autocannon): the Tesla pod breaks a mirror and empties the cannon's shield on a hit, the beam holds a
  sac's regeneration. The fight's measurement uses this fit (M10's Level 42 part).
- **Bounty**: the spec's formula (24 % of budget(42) ÷ (10.486 × `bounty_scale`)) with this level's scale gives the
  boss's bounty in Act 1 terms, split as the spec's 45 / 40 / 15: at the hand scale 0.64, **401** (phase 1 180, phase 2
  161, the lifeboat 60); at step H2's measured **0.63**, **407**: phase 1 **183** (the bow cannon 113, each generator
  15, each battery 10), phase 2 **163** (the heart 115, each sac 16), the lifeboat **61** at the cap; paid **2,689 =
  24.0 %** of budget(42) (the [Ascendant's data](../../../enemies/bosses/ascendant/data.yaml)).
- **The lifeboat's share in the typical haul**: the hull's 85 % at 100 % (the boss dies in every won run) and the
  lifeboat's 15 % at **60 %** of its cap (the middle of the spec's autopilot band, 40–80 %): a choice of this draft,
  as the economy's shares have no row for a boss part that pays pro rata.
- **The Darts and Chimeras it launches** pay their own bounty (Dart 2, Chimera 14, outside the 24 %) and count into the
  level's enemies, not its density; the kill-ratio secondary leaves them out (`boss_counted: false`).

## Music & ambience

Draft for the audio step (D-audio owns the [Act 6 music plan](../../../audio/music/README.md#track-list), J9):
[track 15](../../../audio/music/README.md#track-list), the Act 6 B theme: the **base stem** in section 1, the **full mix
from the home fleet** (t=22), Vorne's motif **on**, **out at the halt** (fading t=86–88); the boss warning (track 22)
at t=88; [track 19](../../../audio/music/README.md#track-list) "Iron Sovereign" (the Ascendancy boss theme) from the
warning's end through phase 1; at the eruption **track 18** "The Choir Descends" takes over (the ship turns Vrell under
him); the **escape sting** (track 33, the audio step's) at `ESCAPED` cuts track 18. Under the closing lines the
`belt-open` bed alone (Vorne's motif gone with him). The debrief plays the `mission-complete` jingle (23), and the
act-complete fanfare (24) plays under the first outro page. Ambience: the `belt-open` bed (open space heard through the
hull, Act 5's; [sfx](../../../audio/sfx/README.md#ambience-per-setting)) throughout; the cruisers' sounds (Level 28's: the broadside's tell and volley, the
point defence) in section 2; the boss's ([brief](../../../enemies/bosses/ascendant/README.md#sounds-brief)) in the arena.
New sounds this level needs: the tender's strongbox cracking (a steel lockbox's hinges shearing), the boss's (its
brief). Freesound only for real-world sounds (CC0/CC-BY), no bubbly or popping sounds.

## Act end

Level 42 ends Act 6 and follows Level 35's order, with the escape in place of the kill:

1. **The hull breaks** (K): the chain along the keel, the act boss's screen flash at K+2.5 s, the credit shower over
   the wreck (collectable: the scroll is halted). Then **the escape** (K+3 to E ≈ K+17): the lifeboat climbs, the
   escape bar drains, `ESCAPED`, the escape sting (track 33).
2. **The empty orbit** (section 6): no enemies, the closing lines in order (Varga, Rook, Okafor's "*Then we follow
   him.*"), the debrief waiting for the last.
3. The **debrief with the act summary** (`mission-complete` jingle, the tally, the summary `ACT VI COMPLETE · JOVIAN
   STORM`).
4. The **act outro**, four voiced pages with their images (the texts and the data are the
   [Act 6 README](../README.md)'s, step D-act: the Ascendant broken, Vorne gone toward the gate, the Silence, Rook's
   line; Okafor's "*Then we follow him.*" is the radio's and is **not** repeated there), with `act-complete` under page 1.
5. **The release's end**, moved from Level 35 to Level 42 (M10's close-out): the end card `ACTS I-VI COMPLETE … TO BE
   CONTINUED / ACT VII - BEYOND THE GATE. THE TETHER GATE. COMING SOON.`, the name entry and the credits, then the main menu; Continue
   opens the hangar before Level 43, which reads `ACT VII - BEYOND THE GATE: COMING SOON` and cannot launch (Level 35's
   `ACT VI - JOVIAN STORM: COMING SOON` goes). The next act's name comes from the `next_act` key in Act 6's data.
   `--act-end` works unchanged for this level.

## Credit budget

Budget(42) = 700 × 1.07⁴¹ ≈ **11,216** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act 1
terms** (Talon 12, leader 25, Mote Swarm 2 a mote, Chimera 14, Skitter 5, Gilded Gunship 28, Dart Drone 2, Mirror
Interceptor 30, Hornet 26, Harrow 12, the boss 407, the crate 100, the secondary 100), × the act factor **10.486**
(1.6⁵), bounties also × the level's scale. At ≈ 112 a minute a **`bounty_scale` of 0.64** put the typical haul on the
budget by hand (**11,197, −0.2 %**), the perfect run at 1.60 × (the economy's shares: 60 % of the air kills and
launched units, half the secret and the secondary, the hull at 100 % and the lifeboat at 60 % of its cap). Step H2's
`levelReport` measured +1.2 % at 0.64 and fitted **0.63**; the boss's bounty recomputed at it is **407** (the table
below, rendered from the data: **11,202, −0.1 %**, the perfect run 1.57 ×; the haul counts the whole boss, the
lifeboat's share at its cap: step H3 measures the share); `TypicalHaulTest` confirms it in the build. The boss keeps the act-boss share of **24 %** (the perfect-run ≥ 1.4 ×
rule; Levels 21, 28 and 35). The scale is above the gap analysis's band (0.40–0.56): a 92 s approach with no ground
earns less than Level 35's (which had 15 hull guns), so its bounties pay more each.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Talon 43 × 12 + Mote Swarm 42 × 2 + Chimera 34 × 14 + Skitter 18 × 5 + Gilded Gunship 2 × 28 + Mirror Interceptor 5 × 30 + Hornet 2 × 26 + Harrow 13 × 12 + launched Dart Drone 12 × 2 | 10,552 | 6,331 |
| Squad leaders: 5 × 13 extra (perfect run only) | 430 | 0 |
| Boss: Ascendant (cannon 113 + gen-l 15 + gen-r 15 + bat-1 10 + bat-2 10 + bat-3 10 + bat-4 10 + sac-1 16 + sac-2 16 + sac-3 16 + heart 115 + lifeboat 61) | 2,689 | 2,689 |
| Boss spawns: Dart Drone 32 × 2 + Chimera 16 × 14 (a typical fight) | 1,888 | 1,133 |
| Secret: The home-fleet tender's strongbox (hidden crate, 1% of budget) | 1,049 | 524 |
| Secondary: clear the orbit | 1,049 | 524 |
| **Total** (bounty scale 0.63) | **17,657** | **11,202** |
| Budget(n) = the typical haul's target; typical -0 %, perfect 1.57 × budget | | 11,216 |
<!-- /data -->

**Balance plan**: the plan's L42 visit raises the right Micro-missile Pod to L5 and the Tesla Coil Pod to L3 and stows
the IFF spoofer ([the hand plan](../../../player/generator/README.md#act-6-hand-plan-draft)): forward DPS 181 against the
proposed reference **168** (1.08), 23 of 25 MW, two spare (one utility or the Reflex Shield's room). The level expects
`piercing` and `homing`; the plan's pods are its homing, the beam its focus on the sacs.

## Difficulty notes

- **Easy**: the formation lever thins the wings and the popcorn; one Gunship in the column (fewer Darts); the Mirror
  pincer 1 + 1 and the rear ambush of 2; the Harrow cross 1 per edge, the stream of 4; the cruisers' broadsides every
  4.5 s with a 1.5 s tell; the boss ×0.75 with its easy hooks (rests ×1.3, the lance's line 1.5 s, the spiral 2 arms,
  the brood 3, the lifeboat's cap 1,050); the checkpoint at the warning.
- **Hard**: the wings larger; a third Gunship in the column; the rear ambush of 4; the Harrow cross 3 per edge; the
  broadsides every 3.0 s; the boss ×1.3 with its hard hooks (the mirror arc 140°, bursts of 4, the spiral 4 arms, the
  escape 12 s); no boss checkpoint.

## Concept art

Round 80 (M10 part H, step ART-H2; production art, proposed). The Ascendant's hull, parts, eruption, wreck halves,
lifeboat and intel silhouette are the [Ascendant](../../../enemies/bosses/ascendant/README.md#concept-art)'s (step
ART-H1); the cruisers are Level 28's final art (reused); the CDF supply drone is Levels 13/18's `supply-drone` sprite
(reused); the two briefing images and the act outro's four are the [briefing's](../../../ui/briefing/README.md#concept-art)
(`briefing-images-final-r80-a`). The round's captures follow with step H5.

**Callisto orbit** (`tools/art/backdrop_l42.py`, on the shared Jovian pieces of `tools/art/jovkit.py`), in round 72's
L42 grade (black space, Jupiter's limb and Callisto's curve, both fleets' running lights; the boss's gold and teal over
black): one continuous `deep` field (stars, Jupiter's cream limb as a thin lit crescent along the right edge the whole
level) cut into the sections' tile sets, scrolling at **0.06** (Level 40's), and the pieces over it - **Callisto's
curve below** the whole level (its night side: dark blue-grey cratered ice in Jupiter's warm shine from the right, a
thin cool rim on the limb, the black citadel's gold windows and lamps steady on it), held on the screen by a `path`
(the limb at y≈360 at the start, 400 by the halt, then still through the fight, sinking 72 px over the empty orbit);
the CDF carrier group closing from below in section 1 and again after the escape; the home fleet's gold lamps far ahead
in sections 1–2; **two burning hulks** (a Regent cruiser's bow half and stern half, Level 28's model cut along a jagged
break, fire venting at the breaches, sparks trailing up) falling toward Callisto in section 3 and passing behind its
limb; the **Ascendant's gold glint** high ahead from t≈53, its **drive glow** growing over it as the scroll slows
(t=83–88.5) and both swinging away out of the top over t=89.5–91.5 (it comes about) before the bow slides in; nothing
animated behind the fight; after `ESCAPED` the lifeboat's last flash toward the gate at the top (white-teal) and the
fleet's lights closing in. At most two animated pieces on the screen at once; every glow additive and smooth (≥ 32
levels, checked), no ordered dither; no red in the backdrop (the spinal lance's line and the batteries' rims are red).
Pending the user's review in round 80: Callisto's curve held by its path instead of scrolling with the layer (a 0.06
layer carries it off the bottom by t≈30); Jupiter's limb a near-straight edge (a disc filling the right edge for the
whole level must be huge); the drive glow leaving upward before the hull's slide-in; section 4 split at t=86 with the
6 s ease to 1 px/s (Level 35's halt: the simulation halts at speed 0 only in the arena). **The CDF frigates and the
dead tender** (`tools/art/l42_props.py`): the frigate in the fleet's white with blue bands, two twin turrets on its
spine turned to port toward cruiser A, grey radiator vanes, three engines, white strobes (never red); its pale
blue-white muzzle flash and tracer dash (presentation only: they hit nothing); the friendly marker is the game's
brackets. The tender: an Ascendancy supply hull in gloss black and gold, two cargo pods torn off and one split open,
dull teal-black Vrell resin where they boarded, the strongbox's gold cradle on its spine, drifting dead at 25°; the
strongbox a gunmetal case with gold bands and the Ascendancy seal, its lamp breathing gold; its break in vacuum (a
short flash, the shell chips in a ring, no smoke). The proposal (`concept/backdrop-proposal.yaml`, step H2's input, not
data) holds the backdrop block, the sections, the tender's `tows` row and a commented `frigates` row. Atlas: the
backdrop 0.46 of a 2048 page before packing, the frigates and the tender 3 %. Prompts and briefs:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r80-a.png](concept/backdrop-final-r80-a.png) | Production backdrop: twelve screens at half size with stand-ins (Callisto's curve, Jupiter's limb and the fleet rising; Chimeras under the home fleet's lights; cruiser A and the marked CDF frigates; the dead tender and a rear wing; cruiser B; a hulk falling and a Gunship; the Harrow cross and the glint; the pause with the drive glow; the Ascendant coming about; the held arena under the bow's outline; the lifeboat's last flash; the empty orbit), the readability crops at 1× (each section's darkest and brightest window with a Talon, a Chimera, a Harrow, amber needles, an orb, the ship; the means), the pieces, the tile sets (sheet) | proposed (round 80) |
| [concept/backdrop-final-r80-a.gif](concept/backdrop-final-r80-a.gif) | t 82–92 at half size (the glint, the drive glow growing and swinging away, the halt), the arena with the bow's outline sliding in, then t 93–98 after `ESCAPED` (the lifeboat's last flash, the fleet closing) | proposed (round 80) |
| [concept/l42-props-final-r80-a.png](concept/l42-props-final-r80-a.png) | The CDF frigate (2×, 1×, its glow frames, the muzzle 4×, the tracer 4×), the dead tender with its strongbox (2×), the seal lamp's frames (3×), the strongbox's break (8 frames, 2×), both at 1× on the backdrop (sheet) | proposed (round 80) |
| [concept/l42-props-final-r80-a.gif](concept/l42-props-final-r80-a.gif) | The tender drifting down the right half from t=42, the seal lamp breathing, the strongbox shot open after 4.6 s, the hull drifting on; the frigate pair firing tracer bursts to port | proposed (round 80) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (tile sets, pieces, placements with their paths, the atmosphere), the sections, the tender's `tows` row and a commented `frigates` row for step H2 (not data) | proposed (round 80; step H2's input) |

## Implementation

- [x] Level data `data.yaml` (M10's Level 42 part) with the sections, waves, radio, budget, threat profile, briefing,
      backdrop and music; the arena a one-second halted section, the clock halted until `ESCAPED`, no hold after it
      (step H2; the tables above rendered from it)
- [ ] Reference DPS 168 at L42 and the Act 6 hand plan's Level 42 fit (M10 part A) before the data; the act HP factor
      for the returning `medium`+ units listed in *Waves*
- [ ] The approach to the Act 2+ rules (`ActFloorTest`, `PacingTest`, the pre-boss pause named as the gap), the
      Gunships' Darts counted, edge and BOTTOM warnings for side and rear first entries, the Harrow cross warned on
      every edge; the three-way war's prey link (the Skitters of t=14 on the Talons of t=11.5)
- [ ] The home fleet's cruisers on Level 28's built code: one flank at a time, broadsides every 3.5 s (never two bands),
      point defence; the CDF frigates with the friendly marker
- [ ] `NO GROUND` on the hangar and in the weapons rows (J2 extended to Level 42: the HUD's and the world's lists name
      it)
- [ ] The [boss](../../../enemies/bosses/ascendant/README.md) arena and fight per its spec: the warning and the checkpoint
      at t=88 (none on hard), the halt at t=92, the bar, the escape bar, `ESCAPED`; Rook and the autopilot per the spec
- [ ] Primary `reach-end` with the arena as its condition, `STOP THE ASCENDANT`; secondary `kill_ratio` 65 % of the
      approach (the boss and its launches not counted)
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineTest`), with Rook and without; Vorne on the enemy
      channel (the amber plate, the interference, the portrait's mood per line; line 5 `shout: true`, the voice
      check's one exception); the Choir's line; the boss lines as `PHASE` events, the closing lines as `CLOSING` with
      `delay` from `ESCAPED`, Okafor's line last
- [ ] Threat profile (the empty orbit not on the strip), Varga's four intel lines and Okafor's teaser fitting the intel
      panel; the Ascendant's intel silhouette
- [ ] Briefing: both pages, each one screen, with their images (Varga's from the files)
- [ ] Typical haul within ±5 % of budget(42) with the level's `bounty_scale`; the boss's share inside 20–40 %
      (`BalanceTest`), its bounty recomputed with the measured scale, the lifeboat's typical share measured
- [ ] The tender secret (4 hits, crate 100), the overdrive, the shield cell, the supply drone's and the eruption's
      armour patches
- [ ] Music: track 15's stems by section, out at the halt, the warning, track 19, track 18 from the eruption, the
      escape sting (33), the act-complete fanfare under the outro; the `belt-open` ambience
- [ ] Act end: the debrief with `ACT VI COMPLETE · JOVIAN STORM`, the four outro pages with `act-complete` (without
      Okafor's last line), the release's end moved here from Level 35 (`ACTS I-VI COMPLETE`, `next_act`: `ACT VII -
      BEYOND THE GATE`), the hangar before L43 reading `ACT VII - BEYOND THE GATE: COMING SOON` (`--level 42 --act-end`)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot flies the level with Rook on every difficulty
      (`--escort none` too)
- [ ] Production art and the round's captures

## Open questions

Left to the build (the document is approved as drafted):

- **The cruisers** (Level 28's, one flank at a time): reused as drafted, or no cruisers (a plain screen, ≈ 10 more
  enemies).
- **The lifeboat's typical share** counted at 60 % of the cap (drafted) or at 100 % (the economy's boss row; the scale
  would fall to ≈ 0.63).

## Decisions

- 2026-10-31 (Act 6 design phase, step `m10-d-levels-3`; decided 2026-10-31 (round 72)): drafted from the
  roster row, the gap analysis (§0, §1.1, §2.4, §2.5, §3: Q1 c, Q3 a, Q6 a, Q9 a, Q10 a, Q12 a; J1, J2, J5, J6, J9,
  J10), the Ascendant's README (the source of truth for the fight, the lifeboat and Vorne's lines) and Level 35's
  draft as the model (every section, the act-end order, hand tables marked draft, no `data.yaml`).
- 2026-10-31: the approach is 92 s, ≈ 112 a minute (171, the Gunships' Darts counted), no wave after t=83, the warning
  and the checkpoint at t=88, the halt at t=92; the clock halted until `ESCAPED`, the closing lines from it.
- 2026-10-31: **`NO GROUND` in Level 42** (the gap analysis left it to this step): the home fleet's cruisers are Level
  28's invulnerable `low-air` set pieces (one flank at a time, never two bands), so the hand plan's stowed spoofer and
  the boss's `air` layer agree; the HUD's and the world's `NO GROUND` lists gain Level 42 (D-act's cross-doc fix).
- 2026-10-31: Level 42's hand `bounty_scale` is **0.64**, so the boss's bounty is **401** in Act 1 terms (24 % of
  budget(42)), recomputed from the spec's 435 at 0.59; the lifeboat's share counts at 60 % in the typical haul.
- 2026-10-31: the closing order is Varga (the gate, the Choir's broken deal), Rook (without him, nothing), then Okafor's
  "*Then we follow him.*" as the act's last voice; the act outro does not repeat it.
- 2026-10-31: the act end follows Level 35's order with the escape in place of the kill; the release's end moves to
  Level 42 and names Act VII (`next_act`).
- 2026-10-31 (decided here, trivia): a `kill_ratio` secondary where the gap analysis drafted none; the secret is a dead
  home-fleet tender (Level 30's breakable-object rule); the Vrell hunt one Talon wave; Okafor answers Vorne's Aurelia
  taunt once.
- 2026-10-31, design phase (step m10-d-act; decided 2026-10-31 (round 72)): the Choir's line 4 is its own ("*…the one who opened…
  is inside us now… he was always inside us…*", the boss document follows); Okafor's answer to Vorne's line 2 is
  "*Aurelia asked us for help, Vorne. We came. You never did.*" (her "I remember" is the intro's and her 2166 memory
  Level 37's: neither repeats here); the ambience is D-audio's `belt-open` bed; the release's end names the next act's
  setting from Act 6's `next_act` (`ACT VII - BEYOND THE GATE. THE TETHER GATE. COMING SOON.`, as Act 5's named
  Callisto).
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): **Q6 a** (bow-on over a halted
  arena, the lifeboat escaping; b a flank pass and c two bosses rejected), J2 (`NO GROUND` in Level 42; the cruisers'
  decks as a `ground` stretch rejected), **J13 kept** (the secondary "Clear the orbit"), J15 (the scale 0.64 outside
  the band, the build measures it), the friendly answers between Vorne's lines (wording as drafted), A.10 a (the
  gilded wedge) and **A.11 change** (the hull breaks along a jagged fracture; the
  [Ascendant](../../../enemies/bosses/ascendant/README.md) owns it). The document approved as drafted (D.8; `design:
  approved`).
- 2026-11-02 (M10 part H, step H2; trivia pending the user's review in round 80): the level's `data.yaml` from this
  document and ART-H2's backdrop proposal: ART-H2's seven sections (section 4 split at t=86 where Level 35's 6 s ease to
  the halt at 1 px/s starts; the one-second arena at 0 from t=92); the warning at t=88 as the boss's `incoming` (4 s),
  the checkpoint at the arena's start as Level 35's (a retry warns again); cruiser A runs 28–38 on the left (rising
  from 24, the CDF frigates with it), B 44–54 on the right (rising from 40, gone by 58); easy's broadside every 4.5 s
  with a 1.5 s tell, hard's every 3.0 s; no flak, never two bands. The secondary is **104 of 159** (the waves' planned
  units; the draft's "112 of 171" counted the launched Darts in; Level 40's rule). The Skitter swarms of t=30 and t=51
  are streams (the hunters of t=14 a snake), hard's Harrow cross 2 per edge (the cross's 4–8). ART-H2's five exits off
  the screen slowed to 2 px a frame before their re-park (the backdrop check), and a path that holds a piece still on
  the screen (Callisto, the Ascendant's glint) no longer counts as a strongly animated element (the check's reading).
  `levelReport`'s haul fit gives **`bounty_scale` 0.63** (+1.2 % at 0.64): the boss's bounty recomputed at it, **407**
  (183 / 163 / 61). Before tuning (`levelReport`, the plan's fit with Rook, three seeds): the approach is flown without
  a loss on every difficulty (medium 3.93 on the screen, 128 shots a minute, the small units 1.45× Level 10's, the gaps
  only the cruiser hand-over and the pre-boss pause, the secondary's kills 131–139 of 104), but the generic autopilot
  never ends phase 1 (it holds the field's edges, 1–6 panel downs a fight, the cannon untouched): the fight's measures
  wait for step H3's own play (`PacingTest.PENDING_AUTOPILOT` until then).
- 2026-11-02 (step H4b, pending the user's review in round 80): the game's music hooks 3 and 4 (track 19 → 18 by the
  2 s crossfade at the eruption's `BOSS_PHASE`; track 33 cuts the boss music at `ESCAPED`, the bed alone after it), the
  boss's round 80 a sounds on their events (the lance's charge from the cannon's tell, timed to end at the shot; the
  hull break's file from K with the `huge` rung at the flash; a panel's mirror breaking and regrowing on the Mirror
  Interceptor's sounds, lower), the tender's strongbox on its own crack; the radio's `CLOSING` lines from `ESCAPED`
  and the fight's `PHASE` lines dropped there; the approach retimed (*Radio chatter*: five timed lines by 1–2 s).

