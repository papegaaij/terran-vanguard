---
title: Level 50 – Choir Heart
design: approved
implementation: done
art: chosen
depends-on: [../../../enemies/bosses/choir-heart, ../../../enemies/air/choir-seraph, ../../../enemies/space/gate-warden, ../../../enemies/space/rift-skater, ../../../world/vrell-space, ../../../player/generator, ../../../player/wingmen, ../../../player/specials, ../../../ui/hud, ../../../ui/debrief, ../../../story/characters/the-choir, ../level-43-the-tether-gate, ../level-49-echoes]
updated: 2026-11-13
---

# Level 50 – Choir Heart

## Summary

The campaign's last level. The wing dives into the **hive core**: three minutes of tissue chambers in which every
Vrell family comes at once in short waves, then the final boss, the
**[Choir Heart](../../../enemies/bosses/choir-heart/README.md)** (Q7 a: the membranes, the arena, the core). When it
dies the Choir speaks its last line and **the gate begins to close** (Q8 a: the Choir held it open): a scene, not a
fight, in which Lancer and Rook race back through the closing event plane; on the near side the ring goes dark. The
campaign's end follows ([debrief](../../../ui/debrief/README.md#the-campaign-end-act-7-draft)). ≈ 407 s at medium
(≈ 182 s of approach, ≈ 166 s of the Heart (the measured par 165 from the bar to the kill), ≈ 58 s of the ending scene); ≈ 111 enemies a minute over the approach.

**Status: approved** (Act 7 design phase, step `m11-d-levels-4`, 2026-11-04; the user approved it on 2026-11-05, round 84).
Written from the act roster, the gap analysis's recommended answers (**Q7 a**, **Q8 a**, Q10 a, Q11 a, Q12 a) (the user changed Q10 to **b** in round 84; the looks below follow b) and
judgement calls (J3, **J8**, **J9**, J11, J12) and the earlier steps' drafts, which are the source of truth for their
numbers: the [Choir Heart](../../../enemies/bosses/choir-heart/README.md) (its HP, phases, playbooks, arena, lines and
death), [Vrell space](../../../world/vrell-space/README.md#sub-locations-per-level)'s hive core and its
[mood](../../../world/vrell-space/README.md#light-per-level-q10-b), the gate transit's
[rules](../../../world/vrell-space/README.md#hazards--set-pieces) (Level 43's, reversed here), the HUD's
[Heart's bar](../../../ui/hud/README.md#act-7-additions-draft), the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft),
[Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft) and the
[campaign end](../../../ui/debrief/README.md#the-campaign-end-act-7-draft). It is **built** (M11 part H, step H2): the level's
[data.yaml](data.yaml) renders the briefing, the threat profile, the sections, the waves, the radio and the credit
budget below; the Heart's fight is measured (its [Duration and HP](../../../enemies/bosses/choir-heart/README.md#duration-and-hp)).

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft; D-story may reword them):

<!-- data: briefing -->
> **Commander Okafor:** "This is the hive core, Aegis. Every Vrell signal out here runs to one
> point, and that point is the Choir. Silence it and the war is over. Then come home. All of
> you."
>
> **Dr. Varga:** "It will fight with every song we have heard since Luna. Watch for the organs
> that open when it sings: that is when they can be hurt. And Lancer, it is afraid. I still
> don't know of what."
<!-- /data -->

*Images* (production art in M11's build, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-50-hive-core`: the fleet's scan of the hive, its veins converging on one hot point at the
  centre, "CHOIR - PRIMARY" on it; behind, small at the scan's edge, the gate's ring marked "TETHER GATE - OPEN".
- Varga's page `level-50-heart-organ`: her sketch of the Heart from the scans, a sealed organ with six membranes on its
  rim, one drawn open with a note "SINGING = OPEN".

*Hangar teaser* (shop screen before L50; Rook speaks, not voiced):

<!-- data: teaser -->
> **Rook:** "Last one, Lancer. After this the drinks are on me. All of them."
<!-- /data -->

The plan's L50 visit buys nothing (the act's slack, ≈ 43 000 left); the L49 fit flies on, 26 of 30 MW
([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The intel panel's `SPECIAL LIMITED`
names the Airstrike and the Orbital Lance if fitted (`NO COVER`, J12).

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `air` (`space` is `air` in vacuum, J12: every Vrell family, the Heart) and `ground` (the Brood Halls' tissue floor: Spine Turrets, Polyp Mortars, Threadcrawlers); the Heart's walls are terrain |
| Attack directions | front 87% · sides 8% · rear 6% by the waves' data; in the Heart's phase 2 the drift's reversal |
| Density | 5 |
| Recommended traits | `beam`, `homing`, `area` (**J9**: the roster's "all" is not a trait, and it would include `anti-sub`) |
| Hazards | Every Vrell family at once; the Heart's walls, darkness and drift (its document) |
| Boss / mid-boss | final boss [Choir Heart](../../../enemies/bosses/choir-heart/README.md) (Q7 a) |
| Special limits | `NO COVER` (the Airstrike, the Orbital Lance; J12) |
| Sensor-suite detail | **none**: Vrell space, the hive core, `air` + `ground`, all directions · **L1**: + density 5, the hazard "the Heart reshapes its chamber", the secondary `KILLS 0 / 340`, `SPECIAL LIMITED` (`NO COVER`) · **L2**: + the Choir Heart's portrait, Choir Seraph, Gate Warden, Rift Skater, Choir Herald, Chimera, Harrow, Wraith, Coilwyrm and Threadcrawler portraits · **L3**: + `beam`, `homing` and `area` highlighted, the wave strip marking the Heart |
<!-- /data -->

*Varga's intel line* per sensor-suite level (must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "The hive core. The Choir's Heart is at its centre." |
| L1 | "Every Vrell family at once. Then the Heart." |
| L2 | "Nothing new, Lancer. Everything old, all together." |
| L3 | "A beam, homing and area. It will use them all on you." |

**Trait coverage** (`TraitCoverageTest`, the lesson): `beam` from the [Ion Beam](../../../player/weapons/ion-beam/README.md)
(front), `homing` from the [Micro-missile Pod](../../../player/weapons/micro-missile-pod/README.md) (wing) or the
[Swarm Tail](../../../player/weapons/swarm-tail/README.md) (rear), `area` from the
[Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md) (wing): one loadout of what is in the shop at the
L50 visit covers all three, and **the plan's own fit covers them** (the Ion Beam L5, the Tesla Coil Pod L5, the pod
L5, the Swarm Tail L5): no launch warning. The Tesla pod's `shield-breaker` also opens the Heart's membranes and the
approach's Gate Wardens.

## Objective

- **Primary** `reach-end` with the Heart as the condition (the act bosses' form): the level ends when the Heart is
  dead, the closing gate has played and the closing lines are done. The briefing and the objective line read
  **`SILENCE THE CHOIR`** (the boss's name on its three-segment bar, gone at its death). The scene after the kill has
  no objective: it cannot fail.
- **Secondary** "Break the swarm" (`kill_ratio`, the 65 % rule): destroy at least **65 %** of the approach's enemies,
  **340 of 523** at medium as built (step PT2's thinned Last Chamber; step H2's 382 of 587 with all its density waves, the hand table's 219 of 336 before them; easy and hard by the formation lever, the same 65 %), the Heart and what it releases not
  counted (`boss_counted: false`). The tracker counts to its target, **`KILLS n / 340`**; it is **met** at the 340th
  kill (`DONE`) and **failed** once the approach's remaining enemies can no longer reach it, at the latest at the
  warning (t=182), so it resolves before the Heart's bar takes the screen (Level 49's rule). Pays **100** in Act 1
  terms (**1,678** in Act 7); the typical haul counts 50 %.

## Layout

Scroll **100 px/s** through the approach (script time), slowing from t=176 to a halt as the Heart comes into place
(t≈193); halted through the fight; the ending scene's own speeds. Setting:
[Vrell space](../../../world/vrell-space/README.md#sub-locations-per-level)'s **hive core**, `vacuum` (J12), its
tissue floor a `ground` layer in section 3. **Light** ([mood](../../../world/vrell-space/README.md#light-per-level-q10-b),
Q10 b): the palette's near-black violet tissue lit by the Heart's slow red-violet pulse (≤ 1 Hz, smooth, never a flash), the pulse
reaching further into each chamber as the wing nears the centre (section 1 lit only by the units and the veins'
faint glow, section 4 by the pulse); the pulse's brightest stays below the bullets' cores, the units a step brighter
than the walls. Every haze and glow smooth (≥ 32 steps, no ordered dither, fading to 0 inside its sprite's canvas).

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Throat | 0–45 | 0–4,500 | 100 | clear | `low-air`: ribbed tissue tubes, slow-pulsing veins. `deep`: the hive's far walls. | Act 1's Vrell popcorn, all at once: Skitters, Needlers, Stingers, Motes, Whirl Seeds. |
| 2. The Choir Chambers | 45–97 | 4,500–9,700 | 100 | clear | `low-air`: vaulted chambers with crystalline singing ribs (the Heralds' ring markings grown into the walls). | The singers: Heralds, Seraph pairs, Rift Skaters, Lampreys, Wraiths. |
| 3. The Brood Halls | 97–135 | 9,700–13,500 | 100 | clear | `ground`: the living tissue floor (its spawning pits sealed, no pit hazard), the turret and mortar nests, the crawlers; the seed vault (the secret, t≈118). | The brood: Chimeras and Harrows over the floor's guns. |
| 4. The Last Chamber | 135–182 | 13,500–18,200 | 100 | clear | `low-air`: the veins converging ahead; the Heart's pulse lighting the chamber's far end. | Every family in short waves; Gate Wardens (the window shield once more); the Coilwyrm; the last wave at t=165.5, gone by ≈ t=170; then the breather (shield cells and armour patches drifting in) to the warning. |
| 5. The Last Chamber, braking | 182–187 | 18,200–18,500 | 20 | clear | `deep`: the Heart's chamber at the top, its pulse reaching the bottom edge. | The boss warning at t=182, before the Heart shows (the Act 5 lesson); the Choir's first line. |
| 6. The Heart | 187–188 | 18,500–18,520 | 20 | clear | `deep`: the Heart's arena ([its document](../../../enemies/bosses/choir-heart/README.md#arena)); after K the hive greying outward (K+10.5). | The final boss (the bar to the kill ≈ 165 s at medium); the clock halted while it lives; then the hive goes still. |
| 7. The race | 188–206.5 | 18,520–23,650 | 300 | clear | `deep`: the grey chambers in reverse order (the Last Chamber, the Brood Halls, the Choir Chambers, the Throat), still and dead; the folds closing from the bottom edge; ahead the event plane contracting inside the ring. | The closing gate's race (a scene: no input, no hits, no fail); the warp home at K+29. |
| 8. The near side | 206.5–238 | 23,650–25,780 | 60 | clear | `deep`: the ordinary starfield, Neptune small and blue far off, the Steadfast's group ahead; behind, the ring's upper arc, its event plane shrinking to a point and going out (K+31), its glyphs going dark one by one (K+35–38). | The near side: the radio's noise gone; the closing lines; level end at K+58. |
<!-- /data -->

**The Heart** (paper times at medium, the [Heart](../../../enemies/bosses/choir-heart/README.md#duration-and-hp)'s
numbers): **the boss warning at t=182, before the Heart shows** (track 22, the scroll slowing through the last
chamber; the Choir's first line); the Heart comes into view with the last of the scroll t=187–193, the bar
`CHOIR HEART` filling at t=187, invulnerable until in place; phase 1 (the membranes) t=193–253; the closing (4 s,
the walls, the dark, the drift) t=253–257; phase 2 (the arena) t=257–307; the opening (4 s) t=307–311; phase 3 (the
core) t=311–**K≈349**. The bar to the kill ≈ 162 s on paper; built, the autopilot's fight lasts 156–173 s at medium
(the par **165**: phase 1 ≈ 90 s, phase 2 ≈ 30 s, phase 3 ≈ 37 s, the HP tuned by step H2). Armour
patches drop at the closing and the opening (the Heart's).

**The closing gate** (Q8 a; a **scene**: no input, no hits, no fail, no bar, no target, as Level 43's transit and
Level 42's escape; times from the kill **K**):

| When | What the player sees | Notes |
|---|---|---|
| K – K+4 | The Heart's death ([its document](../../../enemies/bosses/choir-heart/README.md#death)): the heartbeat stops, its bullets fade (0.5 s), the chain, the act boss's flash at K+3.5 (under the flash-reduction option), the credit shower | The last screen flash of the game; the shower is collected at once by the Pickup magnet's rule for a boss's shower (no credit is lost to the scene) |
| K+5 – K+10.5 | The Choir's last line; its portrait forms something face-like, then scatters | The Heart's line 5 (fixed) |
| K+10.5 | **The gate begins to close**: the hive goes still, its tissue greying outward from the dead Heart over 3 s; a deep groan through the chamber; far ahead (the top edge) the tissue parts on the event plane's violet light | The HUD's event line `THE GATE IS CLOSING` |
| K+12 | **Control taken**: the ship eases to x 240, y 420 over 1.5 s and Rook into formation beside it ([Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft)); any Swarm Call drones fade out; the scroll runs up from 0 to **300 px/s** over 3 s | The HUD stays drawn; no shot fires |
| K+13 – K+29 | **The race**: the grey chambers stream past in reverse order (the Last Chamber, the Brood Halls, the Choir Chambers, the Throat), still and dead; behind, the tissue folds closed from the bottom edge, never nearer than 160 px to the ship; ahead, the *Steadfast*'s group's markers on `deep` pass into the event plane (≈ K+20); the event plane grows as the wing nears it, while its rim **contracts** inside the ring (the gate closing) | No collision, nothing to dodge: the tension is in the picture and the radio |
| K+29 – K+30.5 | **The warp**: Level 43's transit reversed: the image bends toward the event plane's centre and grades from violet back to the starfield's colours in one smooth step | The world's photosensitivity rule: the brightness changes in one direction only, **no flashing and no colour inversion** |
| K+30.5 | **The near side**: the ordinary starfield, Neptune small and blue far off (`deep`), the *Steadfast*'s group ahead; the scroll eases to 60 px/s over 2 s; **the radio's noise is gone** (step 0) | The portrait's static overlay off: the first clean radio since Level 42 |
| K+31 – K+35 | Behind the wing the ring's upper arc fills the lower field (`deep`, its stone and glyphs, Level 43's kit); the violet event plane inside it **shrinks to a point and goes out** (a fade, never a flash) | |
| K+35 – K+38 | The ring's glyphs **go dark one by one** round the arc; the ring is plain stone | The gate is dark |
| K+38 – K+62 | The wing flies on toward the *Steadfast*; the closing lines | Level end at **K+62 (≈ t=411)**; the debrief takes over (step H2: K+58 drafted; the closing lines spaced by the 1 s rule) |

Every value is a draft for the build's measure: the race's length (16 s) and speed are picked so that the radio's two
lines fit before the warp; the scene is the same on every difficulty.

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). Measured by step H2 (`levelReport -Plevel=50`): 339 enemies to the warning at
medium (111.8 a minute), 6.7 on the screen, 175 shots a minute, no gap over 2 s. Measured by step PT2 after the
user's playthrough (2026-11-12; the plan's fit, five seeds): 523 at medium (509 in waves, 14 ground; 172 a minute),
2.60 on the screen, 40 shots a minute, no gap over 2 s before the breather (169.7–187.0).

- **Every Vrell family at once**: the Vrell's own (the popcorn, the singers, the elites, the brood, the Wardens) and
  their hybrids (Chimeras, Harrows); no Ascendancy unit (Vorne is gone) and no Husk (the Silence-touched dead are the
  graveyard's, not the living hive's). No unit carries a hex shield (J4); the Gate Wardens' window shields are theirs.
- **HP** from the proposed Act 7 reference DPS (J11: **200** at L50,
  [Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)): the returning `medium` and larger
  units with the act HP factor from their first level: the **Gate Warden** 360 × 200/172 = **419**, the **Choir
  Seraph** 90 × 200/188 = **96**, the **Choir Herald** 60 × 200/84 = **143**, the **Spore Bomber** 24 × 200/32 =
  **150**, the **Mantis** 26 × 200/52 = **100**, the **Wraith** 16 × 200/66 = **48**, the **Coilwyrm** ≈ **377** (×
  200/52 on each part), the **Threadcrawler** ≈ **490** (× 200/80 on each part); the `small` units with the beam
  era's ×2 as built (Needler 8, Stinger 12, Rift Skater 32, Chimera 40, Harrow 16, Lamprey 6, Spine Turret 16, Polyp
  Mortar 20); the Skitters, Motes and Whirl Seeds `tiny`. The Heart's HP are its document's.
- **Density and pacing**: **336 enemies** in 69 waves over the approach's 182 s, **≈ 111 a minute** at medium (easy
  ≈ 280, hard ≈ 390 by the formation lever), counted **up to the warning** (Level 42's rule); the Heart and what it
  releases are not counted. Short waves about every 2.6 s. The floor comes from every family's fire (the Needlers'
  thorns, the Heralds' and Harrows' rings, the Seraphs' and Wraiths' bursts, the Chimeras' aimed pairs, the Wardens'
  windows, the floor's turrets and mortars). Expected over the approach: ≤ 3 pauses over 2 s, ≥ 2.5 on the screen, ≥
  10 shots a minute; the Heart's fight is boss time (its never-quiet rule, 1.0 s) and the scene after it has no
  enemy, both outside `PacingTest`'s measure as every boss's arena; **to be confirmed with `levelReport`**.
- **Directions** (≥ 60 % front by wave count): front 55 (the floor's 8 nests and crawlers among them), sides 8 (the
  Stinger, Lamprey, Harrow and Mantis pincers), rear 6 (the rear Skitter streams, the Wraith ambushes, the rear Harrow
  streams) of 69: **80 % front**. Every side or rear entry gets the built edge warning.
- **The Last Chamber thinned, then a breather** (step PT2, the user's playthrough, 2026-11-12: "the final waves with
  these shielded enemies with rays are a bit too much … there's no break"; accepted 2026-11-13, the user: "act 7 is good"): the shielded
  ray units are the [Gate Wardens](../../../enemies/space/gate-warden/README.md) (window shield, a `heavy` laser line
  every 4 s); their second line (t=160) and 15 more of the section's 35 waves are gone (mostly step H2's density
  waves; every family still flies: the Wardens at 136, a Herald, the Coilwyrm, the Mantises, the popcorn), the last
  six moved up so the last wave (t=165.5) is gone by **≈ t=170**. Then **≈ 12 s of calm** to the warning and 17 s to
  the Heart's first shot, music thinning to the bed (t=160, 171) and Varga's t=172 line in it. The plan's
  [Bastion Shield](../../../player/shields/README.md) refills slowly (120 points, 2 a second + 10 % for the Mk VI's
  spare megawatt, after 1 s broken and a 3 s delay: ≈ 59 s from empty), so the calm brings the refill with it:
  **three shield cells** (30 each) and **two armour patches** (10 each) drift in from the top edge at t=167–175
  (placed, whatever was shot): from empty the three cells (90) and the regen from ≈ t=174 (≈ 29 points) bring it to
  ≈ 119 of 120 as the Heart shows (t=187); the autopilot reached the warning at full shield on every seed (47–120
  before).
- **Limits and spacing** (the Act 5 lesson): never two `medium` or larger hulls overlapping: a Seraph pair's jumps
  never land within 120 px of a Herald (the Seraph's teleport rule) or of the other Seraph, the Wardens' line abreast
  ≥ 120 px apart and clear of the Mantises' edge bands, the Coilwyrm's chain alone in its stretch (no medium hull
  enters t=147–152); the Rift Skaters' told rings never on a Herald's lane; the crawlers' passes clear of the nests.

Draft wave table:

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 2 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (alternating edges) | |
| 3.5 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step H2: density, the final-act rule (varied mixed waves) |
| 5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 6.5 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 8 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 9.5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 11 | 1 | pincer | [Stinger](../../../enemies/air/stinger/README.md) | 4 | sides | 2 a side |
| 12.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 14 | 1 | whirl cluster | [Whirl Seed](../../../enemies/air/whirl-seed/README.md) | 6 | front | |
| 15.5 | 1 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 17 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 18.5 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 20 | 1 | single | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 1 | front | |
| 21.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 23 | 1 | line abreast | [Skitter](../../../enemies/air/skitter/README.md) | 6 | rear | |
| 24.5 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 26 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 27 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step H2: density, the final-act rule (varied mixed waves) |
| 29 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 30.5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 32 | 1 | pincer | [Stinger](../../../enemies/air/stinger/README.md) | 4 | sides | |
| 33.5 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step H2: density, the final-act rule (varied mixed waves) |
| 35 | 1 | whirl cluster | [Whirl Seed](../../../enemies/air/whirl-seed/README.md) | 6 | front | |
| 36.5 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 38 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 39.5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 41 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 42.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 44 | 1 | line abreast | [Skitter](../../../enemies/air/skitter/README.md) | 6 | rear | |
| 45 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 47 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (alternating edges) | |
| 48 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 49 | 2 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | |
| 50.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 52 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 5 | front | Warp-ins on their told rings, clear of the Herald's lane |
| 53.5 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | |
| 55 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 56 | 2 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | `teleport` + `mirror` (Level 47's pairs) |
| 57 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step H2: density, the final-act rule (varied mixed waves) |
| 58 | 2 | pincer | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | sides | 3 a side |
| 60 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 62 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 66 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step H2: density, the final-act rule (varied mixed waves) |
| 69 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front (right) | |
| 72 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 75 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Shield cell (the swarm's leader); down the left third, off the pair's centre line |
| 76.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 78 | 2 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | |
| 79.5 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | |
| 81 | 2 | pincer | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | sides | |
| 82.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 84 | 2 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | |
| 85 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 86 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (right) | |
| 87 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 88 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 5 | front (left) | |
| 89.5 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 91 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front (left) | |
| 92.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 94 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (alternating edges) | |
| 95 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step H2: density, the final-act rule (varied mixed waves) |
| 96 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front (right) | |
| 97 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 99 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front (left) | The floor rises (t=97) |
| 101 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step H2: density, the final-act rule (varied mixed waves) |
| 103 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | front (alternating edges) | |
| 105 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 107 | 3 | single | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 1 | front | |
| 108 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 109 | 3 | snake | [Threadcrawler](../../../enemies/ground/threadcrawler/README.md) | 1 | front | One pass across the floor from the left, clear of the nests |
| 110 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 111 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 112 | 3 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 113 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | rear | |
| 115 | 3 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front (left) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 117 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 10 | front (alternating edges) | |
| 118 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 119 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 121 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 123 | 3 | pincer | [Harrow](../../../enemies/air/harrow/README.md) | 6 | sides | |
| 124 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step H2: density, the final-act rule (varied mixed waves) |
| 125 | 3 | single | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 1 | front | |
| 126 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 127 | 3 | snake | [Threadcrawler](../../../enemies/ground/threadcrawler/README.md) | 1 | front | From the right; clear of the nests |
| 128 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step H2: density, the final-act rule (varied mixed waves) |
| 129 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 10 | front (alternating edges) | |
| 131 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step H2: density, the final-act rule (varied mixed waves); not on hard (its denser formations) |
| 133 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Overdrive (the tip) |
| 135 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 136 | 4 | line abreast | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 2 | front | The window shield once more before the membranes; the first drops the Armour patch |
| 139 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (alternating edges) | |
| 140 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 141 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 142 | 4 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | |
| 144 | 4 | pincer | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | sides | |
| 145.5 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 147 | 4 | snake | [Coilwyrm](../../../enemies/air/coilwyrm/README.md) | 1 | front | Swirling down the chamber's lower half; no medium hull enters t=147–152 |
| 150 | 4 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | front | |
| 151.5 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step H2: density, the final-act rule (varied mixed waves) |
| 153 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | rear | |
| 155 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 157 | 4 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | In their edge bands |
| 159 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step H2: density, the final-act rule (varied mixed waves) |
| 160.5 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (alternating edges) | |
| 162 | 4 | whirl cluster | [Whirl Seed](../../../enemies/air/whirl-seed/README.md) | 6 | front | |
| 163 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 164 | 4 | pincer | [Stinger](../../../enemies/air/stinger/README.md) | 6 | sides | |
| 165.5 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (alternating edges) | The last wave; gone by ≈ t=170 (step PT2: the breather to the warning) |
| 187 | 6 | boss | [Choir Heart](../../../enemies/bosses/choir-heart/README.md) | 1 | top (its descent into view with the last of the scroll, 6 s) | At the arena's start: the bar and the checkpoint (again at each phase's start on easy and medium); nothing enters in the fight but what it releases |

Totals: Skitter 155 · Chimera 72 · Needler 71 · Harrow 64 · Mote Swarm 24 · Stinger 30 · Whirl Seed 18 · Lamprey 38 · Spore Bomber 3 · Choir Herald 3 · Rift Skater 16 · Wraith 4 · Choir Seraph 4 · Threadcrawler 2 · Gate Warden 2 · Coilwyrm 1 · Mantis 2 · Choir Heart 1.
<!-- /data -->

## Ground targets

The Brood Halls' **living tissue floor** (section 3, t=97–135), a `ground` layer of this level only:

- **Spine Turret** nests at t=101 and t=121 (4 each, two a side at x≈100 and x≈380), **Polyp Mortar** nests at
  t=115 and t=131 (3 each, their marked impacts as built), the two **Threadcrawlers** (t=109 from the left, t=127 from
  the right, one pass across the floor each, clear of the nests);
- the **seed vault** (the secret, see *Secrets and pickups*).

The floor's spawning pits are sealed (the hive spends everything on the Heart): no pit hazard, no breeding structure.
The Bomb Rack and every bomb delivery reach the floor; the `area` trait's Tesla sparks clear the nests.

## Hazards

- **The Heart's arena** ([its document](../../../enemies/bosses/choir-heart/README.md#arena)): the membranes' window
  shields (phase 1), the walls as terrain under Act 3's canyon-wall rule, the darkness (a look only, bullets always
  bright) and the [distorted space](../../../enemies/README.md#distorted-space)'s drift with one reversal (phase 2;
  the HUD's reversal chevron and Rook's call), the storm (phase 3).
- **Special limits** (J12): `NO COVER` for the Airstrike and the Orbital Lance. The plan's
  [Swarm Call](../../../player/specials/README.md#act-7-specials-draft) hunts the approach's units, and on the Heart
  an open membrane, a node or the core (the Heart's specials rule); the EMP Burst (if fitted) opens every membrane for
  3 s.

## Secrets and pickups

- **The seed vault** (secret, section 3, t≈118, x≈240, sunk in the floor between the t=115 mortar nest's guns): a
  sealed Vrell organ, a translucent chitin dome packed with seeds; a `ground` trigger of **30 HP** (40 on hard),
  broken it frees a hidden crate worth **100** in Act 1 terms (**1,678**). The Salvage scanner's glint marks it. Its
  event line, Varga: "*Seed stock. Thousands, packed to travel. They were leaving.*" (The Choir's flight, never what
  it fled: [the Silence's rule](../../../story/README.md).) Name a placeholder for D-story.
- **Shield cell**: dropped by the leader of the t=75 Mote Swarm. **Overdrive**: dropped by the tip of the t=133
  Chimera V-wing. **Armour patch**: dropped by the first Gate Warden of the t=136 line (the last stretch's reward), and
  the Heart's two (at the closing and the opening).
- **The breather's pickups** (step PT2): shield cells at t=167 (x 160), 171 (x 240) and 175 (x 330), armour patches
  at t=169 (x 320) and 173 (x 150), entering at the top edge and drifting down (see *Waves*).
- **The Heart's shield cells** (step PT2: "a shield power-up dropped every now and then"): dropped where the part
  broke by the first and the last membrane shot off and by the second and the last node cut (the last at the
  opening, beside its armour patch): four in the fight on easy and medium, about one every 40 s; **two on hard** (the
  last membrane's and the last node's).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), drafted for the **1 s rule** (≈ 12 characters a second, a
1.5 s lead, 0.4 s of silence; event lines drop after 6 s); `RadioTimelineLevel50Test` checks them with Rook and
without. **The radio's noise** (J3): step **5** (held from L47) through the fight and the race, **off on the near
side** (K+30.5: the gate is behind them); the subtitles clean; the Choir's channel clean. **Speakers**: Okafor,
Varga, Rook (barks `requires: escort`), the Choir (its five fight lines, the
[Heart's](../../../enemies/bosses/choir-heart/README.md#the-choir) order and wording). The
[Silence's rule](../../../story/README.md): the Choir fears it; nobody names it.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, Steadfast. The hive core. Every signal out here ends in one place. Find it." |
| t=12.5 | Varga | "Skitters, Needlers, seeds. Everything from the first day, all at once." |
| t=28 (bark, `requires: escort`) | Rook | "Getting crowded, Lancer. Good. Harder to miss." |
| t=46 | Varga | "Heralds and Seraphs. This is where the singing comes from." |
| t=63 | Okafor | "Wraiths on your six. Let the tail find them." |
| t=98 | Varga | "Brood halls. Turrets in the floor. Burn it clean." |
| t=134 | Okafor | "Wardens ahead. Wait for them to open, then hit them." |
| t=150 (bark, `requires: escort`) | Rook | "Everything they've got, Lancer. Means they're out of ideas." |
| t=172 | Varga | "There. At the centre. It's a heart, Lancer, and it's beating faster." |
| Secondary met (`secondary-objective`, the 340th kill at medium) | Okafor | "Their swarm is breaking. Keep going." |
| Secondary failed (by the warning at the latest) | Varga | "Too many got past. They are all going to the Heart." |
| t=183 (The warning (t=182, before it shows; enemy channel): the Heart's line 1) | The Choir | "…small bright one… you have come into the heart of us… turn back… turn back…" |
| In place (`boss-arrival`, real t≈193) + 1 s | Okafor | "That's the Choir. Aegis, end this." |
| `boss-arrival` + 7 s | Varga | "The organs open when they sing. Shoot them then." |
| The closing (phase 2's start; enemy channel): the Heart's line 2 | The Choir | "…we sang so it would not hear us… you are so loud… so loud…" |
| The closing + 5 s (bark, `requires: escort`) | Rook | "Walls! And the lights just went out." |
| The first node cut (`boss-phase` `first-node`) | Varga | "The walls hang on those knots. Cut them all." |
| The reversal's tell (`boss-phase` `reversal`: the second node's death or 23 s into phase 2; bark, `requires: escort`) | Rook | "It's flipping! Hold the middle!" |
| The opening (phase 3's start; enemy channel): the Heart's line 3 | The Choir | "…you opened us… now it can hear… it is listening…" |
| The opening + 5 s | Okafor | "It's open! Everything you have, Lancer!" |
| The core below half its HP (`boss-phase` `core-half`; enemy channel): the Heart's line 4 | The Choir | "…let us through… we only wanted to live… let us through…" |
| K + 5 s (enemy channel): the Heart's line 5 (fixed, the last; the portrait's face-like frame) | The Choir | "…it is behind us… it has always been behind us…" |
| K + 11 s | Okafor | "The gate is closing! The Choir was holding it open. Steadfast is going now. Aegis, get out of there!" |
| t=199.5 (K + 23.5 s (bark, `requires: escort`)) | Rook | "On your wing, Lancer. Don't you dare slow down." |
| t=208 (K + 32 s (with Rook)) | Okafor | "Aegis, Steadfast. I have you. I have both of you." |
| t=208 (K + 32 s (without Rook)) | Okafor | "Aegis, Steadfast. I have you." |
| t=215.5 (K + 39.5 s (the README's K + 38: behind Okafor's K + 32 line, the 1 s rule)) | Varga | "The ring is dark. No readings from the far side at all. It's just stone again." |
| t=227 (K + 51 s (bark, `requires: escort`)) | Rook | "Told you I was coming through that gate. Never said I'd stay." |
| t=234 (K + 58 s) | Okafor | "It's over, Aegis. Stand down." |
<!-- /data -->

Timeline check (text, by the rule above): the approach's timed lines close at ≈ 9.3, 18.3, 33.3, 52.3, 68.2, 103.6,
139.8, 156.4 and 179.2; nothing overlaps, and Varga's last approach line closes before the warning. In the fight
Okafor's t=194 line closes at ≈ 198.3 and Varga's t=200 line at ≈ 205.5, before the first membranes' third song;
the Choir's lines ride the Heart's triggers (never during a told line's tell, the Heart's rule), and the friendly
event lines wait for a free slot and drop rather than talk over the Choir. After the kill the lines close at
≈ K+10.3 (the Choir), K+20.7, K+28.9, K+37.6, K+46.0, K+53.1 and K+57.9; the race's two lines are done before the
warp (K+29), and the level end at K+62 follows Okafor's last (built: Varga at K+39.5, Rook at K+51, Okafor at
K+58; Varga's first line at t=12.5, each behind the line before it).

## Boss / mid-boss

Final boss: [Choir Heart](../../../enemies/bosses/choir-heart/README.md) (Q7 a). Its phases, parts, window-shield
membranes, walls, darkness and drift, the playbooks, tells, HP, the escort's rules, the Choir's lines and its death
live in the spec. Arena notes for this level:

- **The warning before it shows** (the Act 5 lesson): t=182, with the scroll's last slowing through the Last
  Chamber; the Heart comes into view only at t=187.
- **Checkpoints**: on easy and medium at the warning, at the closing (phase 2's start) and at the opening (phase 3's
  start): the last fight is long (the Heart's draft; its open question); **none on hard** (the Heart's hard hook).
- **Traits**: `beam` (the Heart answers in kind with its line; the beam holds an open membrane through its song),
  `homing` (the released Motes, Seeds and Skitters; the nodes on the walls' faces), `area` (the releases and the
  storm's crowd); `shield-breaker` opens a membrane for 3 s.
- **The plan's fit** (the [Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)'s L50 row: the
  Ion Beam L5, the Tesla Coil Pod L5, a Micro-missile Pod L5, the Swarm Tail L5, the Bastion Shield on the Mk VI, 26
  of 30 MW; the Swarm Call; Rook's Autocannon): forward 198 against the reference **200** (0.99, the act's lowest);
  the fight's measurement uses this fit (M11's Level 50 part), five seeds inside 150–180 s.
- **Breaks** (step PT2, the user's playthrough: "the boss needs about 20 % less firing … keep the intensity but add
  breaks"): the Heart's [lulls](../../../enemies/bosses/choir-heart/README.md#phases), 3.5 s every 13 s in every phase
  (only the heartbeat beats on, the membranes open), ≈ 24 % less fire; its HP retuned to 15,300 for the same par.
- **Bounty**: the Heart's formula (24 % of budget(50) ÷ (16.777 × `bounty_scale`)) with this level's measured scale **0.22**
  gives **1,253**, as parts **1,255** in Act 1 terms, split as the spec's 30 / 30 / 40: each membrane **63** (378), each node **94** (376),
  the core **501**; paid **≈ 4,635 = 24.1 %** of budget(50) (step H2's 1,377 at 0.20, the hand 0.34's 811: the same paid share). The
  whole bounty counts in the typical haul.
- **The units it releases** (Motes, Whirl Seeds, Skitters) pay their own bounty and count into the level's enemies,
  not its density; the secondary leaves them out.

## Music & ambience

**J8** (the cues are D-audio's [music](../../../audio/music/README.md#cues-per-level-act-7)): the Act 7 B theme
(**track 17**) full mix with its `choir` stem over the approach, thinning to the base in the Last Chamber and out
under Varga's t=172 line; the **boss warning** (track 22) at t=182; **track 20** "Final boss" (full length, J8) from
the warning's end through the three phases; at **K** its `choir` stem alone under the last line, out at the gate's
groan. **The race**: **track 20's coda** (a one-shot, the main motif alone, rising) from K+12 into the warp's held
chord, fading over the near side's first 4 s; no music under the closing lines; then the campaign end's music (the
debrief's jingle, track 27 *Ending* under the ending pages, track 28 under the credits;
[debrief](../../../ui/debrief/README.md#the-campaign-end-act-7-draft)). Ambience: the **`vrell-hive`** bed with the
**heartbeat layer** over the approach ([sfx](../../../audio/sfx/README.md#ambience-per-setting)): a slow, deep organic
breathing under a faint **dry** creak of growing tissue, the heartbeat rising chamber by chamber and out at the
warning (the Heart's own takes over); after K the bed thins and goes out at the warp; on the near side Act 5's
**`belt-open`**. New sounds this level needs, for the act's sfx step: the gate's closing groan (a vast stone-and-metal
stress, real recordings slowed), the event plane contracting (a low falling rush), the glyphs going dark (soft, low,
one each), the seed vault breaking (a dry chitin crack), the Heart's (its
[brief](../../../enemies/bosses/choir-heart/README.md#sounds-brief)). Freesound only for real things (CC0/CC-BY), no
bubbly or popping sounds.

## The campaign end

Level 50 ends Act 7 and the campaign. Act 7 has no act outro and no `next_act`: the order after the level's end is the
[debrief](../../../ui/debrief/README.md#the-campaign-end-act-7-draft)'s *The campaign end* (step `m11-d-end`), the
source of truth:

1. **The level's end** (this document): the Heart's death, the Choir's last line, the closing gate, the near side,
   the closing lines; the debrief takes over after Okafor's "*It's over, Aegis. Stand down.*" (the 15 s rule waiting
   for the last line).
2. **The final debrief**: Level 50's tally (the `BOSS TIME` row for the Heart), its grade, then the act summary `ACT
   VII COMPLETE · BEYOND THE GATE`.
3. **The ending pages** in the act outro's place (track 27; their texts and images are the
   [act](../README.md)'s, step D-act: Okafor, Varga, Rook, Okafor's last order and the final image of the dark ring;
   the radio's closing lines above are **not** repeated there).
4. The end card `CAMPAIGN COMPLETE` (the completed save written there), the name entry, the credits roll (track 28),
   the main menu; Continue opens the mission select with every level open.

A **replay** of Level 50 ends at its debrief, as every replay (the debrief's rule); a debug run plays the whole order
and writes nothing; `--act-end` changes nothing here.

## Credit budget

Budget(50) = 700 × 1.07⁴⁹ ≈ **19,271** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act 1
terms** (Skitter 5, Needler 12, Stinger 15, Mote Swarm 2, Whirl Seed 3, Spore Bomber 25, Choir Herald 30, Rift Skater
14, Lamprey 12, Choir Seraph 35, Wraith 30, Chimera 14, Harrow 12, Spine Turret 12, Polyp Mortar 15, Threadcrawler 56,
Gate Warden 40, Coilwyrm 86, Mantis 30, the Heart 1,255, the crate 100, the secondary 100), × the act factor **16.777**
(1.6⁶), bounties also × the scale (the crate and the secondary are not scaled). The economy's shares: 60 % of the
kills and the released units, the Heart at 100 %, half the secret and the secondary. A **`bounty_scale` of 0.22** (step PT2's
`levelReport` fit after thinning the Last Chamber; step H2's 0.20 with all the density waves, the hand table's 0.34
before them) puts the typical haul at **19,096 (−0.9 %)**, the perfect run at 29,031 (**1.51 ×** budget).

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 155 × 5 + Chimera 72 × 14 + Needler 71 × 12 + Harrow 64 × 12 + Mote Swarm 24 × 2 + Stinger 30 × 15 + Whirl Seed 18 × 3 + Lamprey 38 × 12 + Spore Bomber 3 × 25 + Choir Herald 3 × 30 + Rift Skater 16 × 14 + Wraith 4 × 30 + Choir Seraph 4 × 35 + Threadcrawler 2 × 60 by parts (48 whole) + Gate Warden 2 × 40 + Coilwyrm 1 × 86 + Mantis 2 × 30 | 19,832 | 11,922 |
| Ground targets: Spine Turret 8 × 12 + Polyp Mortar 6 × 15 | 682 | 546 |
| Boss: Choir Heart (core 501 + m1 63 + m2 63 + m3 63 + m4 63 + m5 63 + m6 63 + n-lu 94 + n-ll 94 + n-ru 94 + n-rl 94) | 4,635 | 4,635 |
| Boss spawns: Whirl Seed 18 × 3 + Mote Swarm 16 × 2 + Skitter 12 × 5 (a typical fight) | 526 | 316 |
| Secret: The seed vault (hidden crate, 1% of budget) | 1,678 | 839 |
| Secondary: break the swarm | 1,678 | 839 |
| **Total** (bounty scale 0.22) | **29,031** | **19,096** |
| Budget(n) = the typical haul's target; typical -1 %, perfect 1.51 × budget | | 19,271 |
<!-- /data -->

The scale sits with the act's others (L45's to L48's 0.135–0.17 after their density waves): the three-minute approach
at ≈ 190 a minute earns most of the haul. The Heart's bounty is recomputed from its formula at this scale (**1,255**, from step H2's 1,377 at
0.20 and 811 at the hand 0.34), so it keeps the act boss's **24 %**; the spec's shares stay. **Balance plan**: the L50 visit buys
nothing (≈ 43 000 unspent: the campaign's last cushion, the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)); 26 of 30 MW; Rook's Autocannon.

## Difficulty notes

- **Easy**: the formation lever thins the approach (≈ 280), the Heart's easy hooks (rests ×1.3, one membrane at a
  time, the walls to 360 px, no reversal, the dark's ambient 20 %, one storm attack at a time; HP 11,475, the medium 15,300 × 0.75), the
  checkpoints at the warning and each phase, the secondary's 65 % of the easy count.
- **Hard**: ≈ 390 in the approach, the Heart's hard hooks (rests ×0.8, the heartbeat ring of 16, the walls to 300 px,
  the line 1.0 s, HP ×1.05), **no boss checkpoint**, the vault's 40 HP; the Heart's two shield cells (not four).
- Every difficulty: the ending scene is the same (no fail, no input).

## Concept art

The level needs (round 84's proposals, then the M11 part's production art):

- the hive-core backdrop kit in Q10 b's palette and light: the Throat's ribbed tubes, the Choir Chambers' singing ribs, the Brood
  Halls' tissue floor as a `ground` strip with its sealed pits and the seed vault, the Last Chamber's converging veins
  lit by the pulse (a smooth light layer, ≥ 32 steps, never a flash);
- the Heart (ART-R-B's proposals per phase, its document);
- **the closing gate's storyboard** (ART-R-B: the hive greying, the race through the dead chambers, the event plane
  contracting in its ring, the warp reversed, the near side with Neptune and the *Steadfast*'s group, the ring's
  event plane going out and its glyphs going dark one by one), with the photosensitivity check (no flash, no
  inversion);
- the seed vault (whole, broken); the two briefing images;
- the round's captures: the approach's busiest stretch, the Heart's three phases (a full real-time recording for the
  boss fight, round 76's rule), the scene end to end.

Round 95 (M11 part H, step ART-H2, 2026-11-07; production art, pending the user's review): the hive-core backdrop kit,
the seed vault and the closing gate's scene kit by `tools/art/act7_core.py`, in Q10 b's one Vrell palette with the
level's light. The approach: one continuous deep field cut at the sections' seams (the Throat's rib rings, the Choir
Chambers' vaults with the Heralds' ring markings, the Brood Halls' folds, the Last Chamber's veins converging on the
Heart, the Heart's chamber), a low-air set a section for the near walls (the Throat's ribbed tube, the crystalline
singing ribs, the vein cables), the Brood Halls' living floor as a `ground` tile set with sealed pits and its ragged
lead and end pieces, each set's veins pulsing in a separate layer, and the pulse as one smooth screen-space light
reaching further each chamber (none in section 1). The scene's kit (drawn by step H4's `ClosingGateLooks`, not by the
section tiles): the arena in grey with the greying mask from the dead Heart outward, the race's one grey strip of the
four chambers in reverse order, the folds, the event plane's disc and rim, the near side's starfield, Neptune, the
*Steadfast*'s group, the ring's arc with its glyph light and the glyphs' eight groups for the data's `glyph-dark`
beats. The backdrop is held in `build/art-held/backdrop/level-50/` while the data's `backdrop` block is the stand-in
(Level 47's images; step H4 wires this kit's block from `concept/backdrop-proposal.yaml`, which also holds the pulse
per section, the sections with the kit's tile sets and the scene's numbers); the vault's sprites (`seed-vault`,
`-break`) are in `assets/sprites/` (the data names them). The Heart's own review is now drawn over this arena
(`choir_heart.py`'s review only). The two briefing images are in the [briefing](../../../ui/briefing/README.md#concept-art)
(`briefing-images-final-r95-a`). Every tile and piece passed the row/column straight-line scan and the alpha-silhouette
scan (the longest straight run 65 px, the event plane's rim, limit 120; the longest silhouette run 38 px, limit 40);
the scene's mean luminance moves one way through the warp (no flash, no inversion). Pending the user's review: the
pulse's reach per chamber, the race as one grey strip passing each chamber once, the folds kept to the bottom 40 px
(the scene's "never nearer than 160 px to the ship" at y 420 would keep them below the screen). Prompts:
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r95-a.png](concept/backdrop-final-r95-a.png) | Production backdrop: ten screens with stand-ins and bullets at the pulse's peak (the Throat, its ribbed walls, the Choir Chambers, the singing ribs, the floor's lip, the Brood Halls with the seed vault, the floor's end, the Last Chamber, the veins converging, the Heart's arena), the tile sets, the pulse light, the floor's lead and end, the vault (sheet) | chosen |
| [concept/backdrop-final-r95-a.gif](concept/backdrop-final-r95-a.gif) | The approach and the arena in a time-lapse (2 s a frame) | chosen |
| [concept/closing-gate-final-r95-a.png](concept/closing-gate-final-r95-a.png) | The closing gate from K+10.5 to K+39 from the kit: the hive greying, the race through the grey chambers, the plane near, the warp reversed, the near side, the plane shrinking to a point, the glyphs going dark, the dark ring; the mean luminance per frame (storyboard) | chosen |
| [concept/closing-gate-final-r95-a.gif](concept/closing-gate-final-r95-a.gif) | The scene at 1×, K+10 to K+40 | chosen |
| [concept/seed-vault-final-r95-a.png](concept/seed-vault-final-r95-a.png) | The seed vault whole, cracked and broken open, its break, in place on the Brood Halls' floor between the mortar nest's guns (sheet) | chosen |
| [concept/seed-vault-final-r95-a.gif](concept/seed-vault-final-r95-a.gif) | The vault cracked and broken on the floor | chosen |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (the deep tiles, the low-air and floor sets, the floor's pieces at their exact rows, the pulse per section), a section proposal and the closing gate's numbers for step H4 (not data) | proposed |
| [concept/throat-capture-r95-a.mp4](concept/throat-capture-r95-a.mp4) | Game capture (step GH5): the Throat, the first popcorn waves between the ribbed tubes (t=3–13) | chosen |
| [concept/brood-halls-capture-r95-a.mp4](concept/brood-halls-capture-r95-a.mp4) | Game capture (step GH5): the Brood Halls' tissue floor with its nests, the mortars round the seed vault and the vault found (t=111–124) | chosen |
| [concept/closing-gate-capture-r95-a.mp4](concept/closing-gate-capture-r95-a.mp4) | Game capture (step GH5, re-shot by step FIXGH with Rook flying home and the *Steadfast*'s group in view), the whole screen with sound: the closing gate scene from K to the level's end: the death, the last line, the greying, the race, the warp home, the near side (63 s) | chosen |
| [concept/campaign-end-capture-r95-a.mp4](concept/campaign-end-capture-r95-a.mp4) | Game capture (step GH5, re-shot by step FIXGH at normal speed), the whole screen with sound, from a real save: the debrief, the act summary, the four ending pages, the campaign end card, the high score entry, the credits' start and end (242 s, cut) | chosen |

## Implementation

- [x] Level data `data.yaml` (M11's build): sections and speed, `reach-end` with the Heart as the condition, the
      `kill_ratio` secondary (65 %, `boss_counted: false`, resolved by the warning), waves, ground targets, radio,
      budget, threat profile, briefing, backdrop (the hive core, the tissue floor), music; `medium: vacuum`, the
      `beyond-gate` limits (`NO COVER`); no `next_act` (the campaign's last level)
- [x] The Choir Heart as its spec (M11 part H), its warning at t=182 before it shows, the checkpoints (easy and
      medium: the warning and each phase's start)
- [x] **The closing gate scene** after the kill: the shower collected, control taken, the formation, the race (the
      scroll to 300 px/s, the chambers in reverse, the folds behind, the *Steadfast*'s markers), the reversed warp
      without a flash or an inversion, the near side, the event plane going out and the glyphs going dark; the
      radio's noise off on the near side
- [x] The campaign end after the level ([debrief](../../../ui/debrief/README.md#the-campaign-end-act-7-draft)):
      the final debrief and act summary, the ending pages, the end card, the completed save
- [x] `SILENCE THE CHOIR`, `KILLS n / 340`, the Heart's three-segment bar
      ([HUD](../../../ui/hud/README.md#act-7-additions-draft)), `THE GATE IS CLOSING` on the event line
- [x] Wave script to the Act 2+ rules over the approach (`ActFloorTest`, `PacingTest`), ≥ 80 a minute (`DensityTest`,
      counted to the warning), ≥ 60 % front, the medium-hull spacing; ≥ 4 s of calm after the Heart (the scene)
- [x] Radio voiced with the step-5 noise and its end on the near side, every timed line at most 1 s late
      (`RadioTimelineLevel50Test`, with Rook and without); the Choir's lines on the clean enemy channel
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`);
      `TraitCoverageTest` covering Level 50
- [x] Briefing: both pages, each one screen (`BriefingLayoutTest`), with their images
- [x] Typical haul within ±5 % of budget(50) = 19,271 with the level's `bounty_scale` (`TypicalHaulTest`); the Heart's
      bounty from its formula at that scale
- [x] Music and ambience: track 17, the warning, track 20, its `choir` stem and its coda for the race, the
      `vrell-hive` bed with the heartbeat layer, `belt-open` on the near side, the level's sounds (`Level50SoundsTest`)
- [x] Easy/hard variations; the autopilot flies it on every difficulty with Rook and with `--escort none`, through
      the scene to the campaign end
- [x] Step PT2 (the user's playthrough, 2026-11-12): the Last Chamber thinned (16 of 35 waves, the second Warden
      line among them), the last wave gone by ≈ t=170, the breather's three shield cells and two armour patches
      (`Level50Test`), the Heart's lulls and shield cells ([its document](../../../enemies/bosses/choir-heart/README.md)),
      the scale refit to 0.22 (`TypicalHaulTest`)
- [x] Production art and captures for the build's round (round 95: ART-H's and ART-H2's finals; step GH5's captures, 2026-11-08)

## Open questions

- Answered in [round 98](../../../concept-rounds/round-98/README.md) (S.1 c, closed 2026-11-09): the real recorded heartbeats `heart-beat-r98-c` and `vrell-hive-heartbeat-r98-c` are imported (2026-11-10, step CIFIX).

## Decisions

- 2026-11-12 (the user's Act 7 playthrough, six tries at Level 50, all lost: "the final waves with these shielded
  enemies with rays are a bit too much … There's no break … add breaks … a shield power up dropped every now and
  then"; step PT2, accepted 2026-11-13, the user: "act 7 is good"): the Last Chamber thinned by 16 of its 35 waves (the second Gate Warden
  line at t=160, the Wardens being the shielded ray units, and mostly step H2's density waves; the last six moved up to
  t=159–165.5), its last wave gone by ≈ t=170 (measured 169.7), a calm of ≈ 12 s to the warning with three shield
  cells and two armour patches drifting in (the Bastion's own regen would need ≈ 59 s from empty; the warning and the
  arena stay where they are, so the backdrop's cut stays); the Heart's lulls (3.5 s every 13 s, ≈ 24 % less fire), its
  HP 15,300 for the same par and four shield cells from its parts (two on hard); 523 enemies (172 a minute), the
  secondary `KILLS n / 340`, `bounty_scale` 0.22 (typical −0.9 %), the Heart's bounty 1,255 by its formula. Measured
  at medium with the plan's fit (five seeds): the Last Chamber's shield and armour lost 56 → 42 a run, the shield at
  the warning 47–120 → 120 on every seed; the fight 270 → 205 shots a minute, the bar to K 164.2 → 164.1 s mean.

- 2026-11-08 (M11 part H, step H4; accepted by the user in round 95): the game. The Heart drawn by its own looks
  (crown, membranes by their windows with their singers, nodes, core, a smooth pulse in time with both beats under
  the bullets, port flares from living parts only, the chain from K, the act's flash at K + 3.5 s), the walls along
  the simulation's faces with a lit inner edge and their veins' tell, the walls' phase dark at 8 % (easy 20 %) with a
  quarter back per node cut, the hive's veins and pulse light beating with the Heart; the closing gate scene from the
  proposal's `closing_gate` numbers in the game (a look: no data key), the warp home as the scene's own (the near
  side unbending under the race), the neutral `THE GATE IS CLOSING` banner, the Choir's face-like frame on line 5,
  track 20's stem alone at K (1 s crossfade, sample-aligned), out under the groan, the coda from K+12; the hive's
  heartbeat layer rising from the Choir Chambers to the warning (`ambience_layers`); the bed −12 dB after K in the
  game (`ambience_db` takes rises only). The atlas: 9 pages trimmed to 7 (the tile sets' pulsing light and the race's
  strip at half size, drawn at 2x), the level's budget 7 like Levels 28 and 49; `deep-8` re-cut to 448 rows for the
  near side's end at 238.
- 2026-11-08 (M11 part H, step H2; accepted by the user in round 95): built from the approved tables. Placed by the
  step: the two Seraph pairs at t=56 and 84 (the third pair and two Heralds moved: the pairs' rules forbid a medium
  hull entering while a pair holds and popcorn on the centre line; the Heralds at 49, 78, 142, 166), the t=75 Mote
  Swarm down the left third; the floor's nests as ground targets (turrets x 100/380, mortars round the vault), the seed
  vault a 30 HP (hard 40) `ground` destructible at t≈118 (the draft wave table's "memory organ" is the seed vault: the
  gap check's §6 rename), the crawlers' passes over the living
  floor (no terrain or reef); the Coilwyrm in from the right edge under the t=136 Wardens (its top path crossed them);
  59 density waves of the act's mix (the plan's fit left 1.5 on the screen on the table's 69: 193 a minute and 3.2 on
  the screen at medium after them, the final-act rule; every second one not on hard, whose formations are denser); `bounty_scale` 0.20 (typical +1.4 %, the Heart's bounty 1,377 by its formula); the closing
  gate on the clock after the arena (`hold_after_boss: 12`, the race at 300 px/s to K+30.5, the near side at 60 px/s to
  K+62), its beats from K, eight glyphs 0.4 s apart; the radio's noise 0 on the near side (`radio_noise_changes`); the
  backdrop ART-H2's hive-core proposal. The open questions closed: **the race** a scene (E.3 a), **the checkpoints** per
  phase on easy and medium (round 84).

- 2026-11-04 (Act 7 design phase, step `m11-d-levels-4`; decided 2026-11-05, round 84): promoted from the
  act roster to a draft level document, hand tables only, no `data.yaml`. A 182 s approach through the hive core
  (the Throat, the Choir Chambers, the Brood Halls with a tissue floor, the Last Chamber; 336 enemies, ≈ 111 a minute
  to the warning, 80 % front), the Heart's warning at t=182 and its fight t=187–≈349 (Q7 a, the spec's numbers), the
  closing gate as a 58 s scene after the kill (Q8 a: the race back, the reversed warp, the near side, the ring going
  dark, the radio's noise off), then the campaign end (Q12 a, the debrief's order). Traits `beam`, `homing`, `area`
  (J9), covered by the plan's own fit. The secondary `KILLS n / 219` (65 %), resolved before the Heart;
  `SILENCE THE CHOIR`. `bounty_scale` 0.34, the Heart's bounty 811 in Act 1 terms (24 % of budget(50)); checkpoints
  per phase on easy and medium.
- 2026-11-04 (step `m11-d-act`, decided 2026-11-05, round 84): the Heart's lines 1 and 3 are the story step's new wording (Levels
  43 and 47 hold the fixed lines; only line 5 is fixed here); K+11 "Steadfast is going now."; the names and lines
  adopted from the story step (the open questions closed); the music and beds as D-audio's cues (track 20's coda for
  the race, `vrell-hive` with the heartbeat layer, `belt-open` on the near side; the tissue's sounds dry).
- 2026-11-05, round 84 (the user's page answers; decided as drafted unless stated, the alternatives rejected). The open items were:
  - **Q7** (round 84, the final fight): drafted **a** (three phases, the Heart's document); **b** a five-part medley;
    **c** two phases and a scripted escape. The [Heart](../../../enemies/bosses/choir-heart/README.md#open-questions)
    holds the fight.
  - **Q8** (round 84, the ending): drafted **a** (the Choir's last words, the gate closing because the Choir held it
    open, the race back as a scene, the ring dark on the near side, everyone of the cast alive; the ending pages are the
    act's); **b** someone stays to close the gate from the far side (Rook or the *Steadfast*: tragic); **c** the gate
    stays open and the fleet holds the far side (a cliffhanger, no closure).
  - **Q12** (round 84, the campaign end): drafted **a** as the [debrief](../../../ui/debrief/README.md#the-campaign-end-act-7-draft)'s.
- 2026-11-08 (user, [round 95](../../../concept-rounds/round-95/README.md) closed): **F.2–F.4, F.6 accept** (the hive-core backdrop, the closing-gate kit, the seed vault and the briefing and ending images: the `*-final-r95-a` art rows chosen, `art: chosen`), **S.2 ok but for the hive heartbeat** and **M.1 ok** (track 20), **V.1–V.3 accept**, **L.1–L.6 ok** (the Throat, the Brood Halls, the whole Heart fight, the arena in the dark, the gate scene, the campaign's end screens: capture rows chosen), **B.1–B.3 ok** (**B.3 a**, QH-2's gentle drift), **C.1–C.4 accept** (the `THE GATE IS CLOSING` banner, the denser level, the end at K+62, the seven picture sheets). **S.2 redo, only the hive heartbeat**: the user's note "Heartbeat is again hard to hear on a phone"; `vrell-hive-heartbeat-r84-b` is redone in round 97 (the energy sits below what a phone speaker plays) and stays the stand-in until then; the gate groan, greying, glyphs, warp home and seed vault are accepted as in place. See *Open questions*.
- 2026-11-09 (user, [round 98](../../../concept-rounds/round-98/README.md) closed): **S.1 c and S.2 c**, no notes: the Heart's heartbeat and the approach's hive layer are the `-r98-c` files; a and b rejected. Import into the game pending; the `-r84-b` files stand in until then ([sfx README](../../../audio/sfx/README.md)).
