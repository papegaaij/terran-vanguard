---
title: Level 50 – Choir Heart
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/bosses/choir-heart, ../../../enemies/air/choir-seraph, ../../../enemies/space/gate-warden, ../../../enemies/space/rift-skater, ../../../world/vrell-space, ../../../player/generator, ../../../player/wingmen, ../../../player/specials, ../../../ui/hud, ../../../ui/debrief, ../../../story/characters/the-choir, ../level-43-the-tether-gate, ../level-49-echoes]
updated: 2026-11-05
---

# Level 50 – Choir Heart

## Summary

The campaign's last level. The wing dives into the **hive core**: three minutes of tissue chambers in which every
Vrell family comes at once in short waves, then the final boss, the
**[Choir Heart](../../../enemies/bosses/choir-heart/README.md)** (Q7 a: the membranes, the arena, the core). When it
dies the Choir speaks its last line and **the gate begins to close** (Q8 a: the Choir held it open): a scene, not a
fight, in which Lancer and Rook race back through the closing event plane; on the near side the ring goes dark. The
campaign's end follows ([debrief](../../../ui/debrief/README.md#the-campaign-end-act-7-draft)). ≈ 407 s at medium
(≈ 182 s of approach, ≈ 167 s of the Heart, ≈ 58 s of the ending scene); ≈ 111 enemies a minute over the approach.

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
[campaign end](../../../ui/debrief/README.md#the-campaign-end-act-7-draft). There is **no `data.yaml`**: the build
turns the hand tables into one; no number is measured.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft; D-story may reword them):

> **Commander Okafor:** "This is the hive core, Aegis. Every Vrell signal out here runs to one point, and that point
> is the Choir. Silence it and the war is over. Then come home. All of you."
>
> **Dr. Varga:** "It will fight with every song we have heard since Luna. Watch for the organs that open when it
> sings: that is when they can be hurt. And Lancer, it is afraid. I still don't know of what."

*Images* (production art in M11's build, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-50-hive-core`: the fleet's scan of the hive, its veins converging on one hot point at the
  centre, "CHOIR - PRIMARY" on it; behind, small at the scan's edge, the gate's ring marked "TETHER GATE - OPEN".
- Varga's page `level-50-heart-organ`: her sketch of the Heart from the scans, a sealed organ with six membranes on its
  rim, one drawn open with a note "SINGING = OPEN".

*Hangar teaser* (shop screen before L50; Rook speaks, not voiced):

> **Rook:** "Last one, Lancer. After this the drinks are on me. All of them."

The plan's L50 visit buys nothing (the act's slack, ≈ 43 000 left); the L49 fit flies on, 26 of 27 MW
([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The intel panel's `SPECIAL LIMITED`
names the Airstrike and the Orbital Lance if fitted (`NO COVER`, J12).

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `air` (`space` is `air` in vacuum, J12: every Vrell family, the Heart) and `ground` (the Brood Halls' tissue floor: Spine Turrets, Polyp Mortars, Threadcrawlers); the Heart's walls are terrain |
| Attack directions | front 80 % · sides 12 % · rear 9 % (55 / 8 / 6 of 69 waves; the floor's nests and crawlers count as front; the act's ≥ 60 % front holds); in the Heart's phase 2 the drift's reversal |
| Density | 5 |
| Recommended traits | `beam`, `homing`, `area` (**J9**: the roster's "all" is not a trait, and it would include `anti-sub`) |
| Hazards | Every Vrell family at once; the Heart's walls, darkness and drift (its document) |
| Boss / mid-boss | final boss [Choir Heart](../../../enemies/bosses/choir-heart/README.md) (Q7 a) |
| Special limits | `NO COVER` (the Airstrike, the Orbital Lance; J12) |
| Sensor-suite detail | **none**: Vrell space, the hive core, `air` + `ground`, all directions · **L1**: + density 5, the hazard "the Heart reshapes its chamber", the secondary `KILLS 0 / 219`, `SPECIAL LIMITED` (`NO COVER`) · **L2**: + the Choir Heart's portrait, Choir Seraph, Gate Warden, Rift Skater, Choir Herald, Chimera, Harrow, Wraith, Coilwyrm and Threadcrawler portraits · **L3**: + `beam`, `homing` and `area` highlighted, the wave strip marking the Heart |

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
  **219 of 336** at medium (easy and hard by the formation lever, the same 65 %), the Heart and what it releases not
  counted (`boss_counted: false`). The tracker counts to its target, **`KILLS n / 219`**; it is **met** at the 219th
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

| Section | t (s) | Scroll (px) | Layers and content | Purpose |
|---|---|---|---|---|
| 1. The Throat | 0–45 | 0–4,500 | `low-air`: ribbed tissue tubes, slow-pulsing veins; `deep`: the hive's far walls | Act 1's Vrell popcorn, all at once: Skitters, Needlers, Stingers, Motes, Whirl Seeds |
| 2. The Choir Chambers | 45–97 | 4,500–9,700 | `low-air`: vaulted chambers with crystalline singing ribs (the Heralds' ring markings grown into the walls) | The singers: Heralds, Seraph pairs, Rift Skaters, Lampreys, Wraiths |
| 3. The Brood Halls | 97–135 | 9,700–13,500 | `ground`: the living tissue floor (its spawning pits sealed: no pit hazard), the turret and mortar nests, the crawlers; the seed vault (secret, t≈118) | The brood: Chimeras and Harrows over the floor's guns |
| 4. The Last Chamber | 135–182 | 13,500–≈18,000 | `low-air`: the veins converging ahead; the Heart's pulse lighting the chamber's far end | Every family in short waves; Gate Wardens (the window shield once more); the Coilwyrm |
| 5. The Heart | 182–≈349 | halted from t≈193 | the Heart's arena ([its document](../../../enemies/bosses/choir-heart/README.md#arena)) | The final boss |
| 6. The closing gate | ≈349–≈407 | 0 → 300 → 60 px/s | the hive going still; the race; the warp; the near side and the dark ring | The ending scene; the closing lines; level end |

**The Heart** (paper times at medium, the [Heart](../../../enemies/bosses/choir-heart/README.md#duration-and-hp)'s
numbers): **the boss warning at t=182, before the Heart shows** (track 22, the scroll slowing through the last
chamber; the Choir's first line); the Heart comes into view with the last of the scroll t=187–193, the bar
`CHOIR HEART` filling at t=187, invulnerable until in place; phase 1 (the membranes) t=193–253; the closing (4 s,
the walls, the dark, the drift) t=253–257; phase 2 (the arena) t=257–307; the opening (4 s) t=307–311; phase 3 (the
core) t=311–**K≈349**. The bar to the kill ≈ 162 s (the Heart's paper; the par is the build's measured mean). Armour
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
| K+38 – K+58 | The wing flies on toward the *Steadfast*; the closing lines | Level end at **K+58 (≈ t=407)**; the debrief takes over |

Every value is a draft for the build's measure: the race's length (16 s) and speed are picked so that the radio's two
lines fit before the warp; the scene is the same on every difficulty.

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

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
- **Limits and spacing** (the Act 5 lesson): never two `medium` or larger hulls overlapping: a Seraph pair's jumps
  never land within 120 px of a Herald (the Seraph's teleport rule) or of the other Seraph, the Wardens' line abreast
  ≥ 120 px apart and clear of the Mantises' edge bands, the Coilwyrm's chain alone in its stretch (no medium hull
  enters t=147–152); the Rift Skaters' told rings never on a Herald's lane; the crawlers' passes clear of the nests.

Draft wave table:

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 2 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front |  |
| 8 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 11 | 1 | pincer | [Stinger](../../../enemies/air/stinger/README.md) | 4 | left, right | 2 a side |
| 14 | 1 | whirl cluster | [Whirl Seed](../../../enemies/air/whirl-seed/README.md) | 6 | front |  |
| 17 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 20 | 1 | single | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 1 | front |  |
| 23 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | rear |  |
| 26 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front |  |
| 29 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 32 | 1 | pincer | [Stinger](../../../enemies/air/stinger/README.md) | 4 | left, right |  |
| 35 | 1 | whirl cluster | [Whirl Seed](../../../enemies/air/whirl-seed/README.md) | 6 | front |  |
| 38 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 41 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front |  |
| 44 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | rear |  |
| 47 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 50 | 2 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front |  |
| 53 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 5 | front | Warp-ins on their told rings |
| 56 | 2 | pincer | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | left, right | 3 a side |
| 59 | 2 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front | `teleport` + `mirror` (Level 47's pairs) |
| 62 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 64 | 2 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front | Clear of the Seraphs' lanes |
| 66 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear |  |
| 69 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 72 | 2 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front |  |
| 75 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Shield cell (the swarm's leader) |
| 78 | 2 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front |  |
| 80 | 2 | pincer | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | left, right |  |
| 83 | 2 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 5 | front |  |
| 86 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 88 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear |  |
| 90 | 2 | pair | [Choir Seraph](../../../enemies/air/choir-seraph/README.md) | 2 | front |  |
| 92 | 2 | single | [Choir Herald](../../../enemies/air/choir-herald/README.md) | 1 | front |  |
| 94 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 96 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 99 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | The floor rises (t=97) |
| 101 | 3 | turret nest | [Spine Turret](../../../enemies/ground/spine-turret/README.md) | 4 | floor (scroll) | x 100 and x 380, 2 each |
| 103 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | front |  |
| 107 | 3 | single | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 1 | front |  |
| 109 | 3 | single | [Threadcrawler](../../../enemies/ground/threadcrawler/README.md) | 1 | floor (scroll) | One pass across the floor from the left |
| 111 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 113 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | rear |  |
| 115 | 3 | turret nest | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) | 3 | floor (scroll) | The memory organ (secret, t≈118) |
| 117 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 10 | front |  |
| 119 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 121 | 3 | turret nest | [Spine Turret](../../../enemies/ground/spine-turret/README.md) | 4 | floor (scroll) |  |
| 123 | 3 | pincer | [Harrow](../../../enemies/air/harrow/README.md) | 6 | left, right |  |
| 125 | 3 | single | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 1 | front |  |
| 127 | 3 | single | [Threadcrawler](../../../enemies/ground/threadcrawler/README.md) | 1 | floor (scroll) | From the right; clear of the nests |
| 129 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 10 | front |  |
| 131 | 3 | turret nest | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) | 3 | floor (scroll) |  |
| 133 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Overdrive (the tip) |
| 136 | 4 | line abreast | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 2 | front | The window shield once more before the membranes |
| 139 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 141 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 144 | 4 | pincer | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | left, right |  |
| 147 | 4 | chain | [Coilwyrm](../../../enemies/air/coilwyrm/README.md) | 1 | front | Swirling down the chamber |
| 150 | 4 | stream | [Rift Skater](../../../enemies/space/rift-skater/README.md) | 6 | front |  |
| 153 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | rear |  |
| 155 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front |  |
| 157 | 4 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | left, right | In their edge bands |
| 160 | 4 | line abreast | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 2 | front | ≥ 120 px from the Mantises |
| 163 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 165 | 4 | whirl cluster | [Whirl Seed](../../../enemies/air/whirl-seed/README.md) | 6 | front |  |
| 167 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 169 | 4 | pincer | [Stinger](../../../enemies/air/stinger/README.md) | 6 | left, right |  |
| 171 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | front |  |
| 173 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front |  |
| 175 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | The last wave; gone by ≈ t=181 |

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

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), drafted for the **1 s rule** (≈ 12 characters a second, a
1.5 s lead, 0.4 s of silence; event lines drop after 6 s); `RadioTimelineLevel50Test` checks them with Rook and
without. **The radio's noise** (J3): step **5** (held from L47) through the fight and the race, **off on the near
side** (K+30.5: the gate is behind them); the subtitles clean; the Choir's channel clean. **Speakers**: Okafor,
Varga, Rook (barks `requires: escort`), the Choir (its five fight lines, the
[Heart's](../../../enemies/bosses/choir-heart/README.md#the-choir) order and wording). The
[Silence's rule](../../../story/README.md): the Choir fears it; nobody names it.

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, Steadfast. The hive core. Every signal out here ends in one place. Find it." |
| t=11 | Varga | "Skitters, Needlers, seeds. Everything from the first day, all at once." |
| t=28 (bark, `requires: escort`) | Rook | "Getting crowded, Lancer. Good. Harder to miss." |
| t=46 | Varga | "Heralds and Seraphs. This is where the singing comes from." |
| t=63 | Okafor | "Wraiths on your six. Let the tail find them." |
| t=98 | Varga | "Brood halls. Turrets in the floor. Burn it clean." |
| The seed vault opened (event) | Varga | "Seed stock. Thousands, packed to travel. They were leaving." |
| t=134 | Okafor | "Wardens ahead. Wait for them to open, then hit them." |
| t=150 (bark, `requires: escort`) | Rook | "Everything they've got, Lancer. Means they're out of ideas." |
| t=172 | Varga | "There. At the centre. It's a heart, Lancer, and it's beating faster." |
| Secondary met (`secondary-objective`, the 219th kill) | Okafor | "Their swarm is breaking. Keep going." |
| Secondary failed | Varga | "Too many got past. They are all going to the Heart." |
| t=183 (the warning; enemy channel) | the Choir | the Heart's line **1** |
| t=194 | Okafor | "That's the Choir. Aegis, end this." |
| t=200 | Varga | "The organs open when they sing. Shoot them then." |
| The closing (phase 2's start; enemy channel) | the Choir | the Heart's line **2** |
| The closing + 5 s (bark, `requires: escort`) | Rook | "Walls! And the lights just went out." |
| The first node cut (event) | Varga | "The walls hang on those knots. Cut them all." |
| The reversal's tell (event, bark, `requires: escort`) | Rook | his reversal call ([Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft)) |
| The opening (phase 3's start; enemy channel) | the Choir | the Heart's line **3** |
| The opening + 5 s | Okafor | "It's open! Everything you have, Lancer!" |
| The core below half (enemy channel) | the Choir | the Heart's line **4** |
| K+5 (enemy channel) | the Choir | the Heart's line **5** (fixed, the last) |
| K+11 | Okafor | "The gate is closing! The Choir was holding it open. Steadfast is going now. Aegis, get out of there!" |
| K+23.5 (bark, `requires: escort`) | Rook | "On your wing, Lancer. Don't you dare slow down." |
| K+32 | Okafor | "Aegis, Steadfast. I have you. I have both of you." (without Rook: "Aegis, Steadfast. I have you.") |
| K+38 | Varga | "The ring is dark. No readings from the far side at all. It's just stone again." |
| K+46.5 (bark, `requires: escort`) | Rook | "Told you I was coming through that gate. Never said I'd stay." |
| K+54 | Okafor | "It's over, Aegis. Stand down." |

Timeline check (text, by the rule above): the approach's timed lines close at ≈ 9.3, 18.3, 33.3, 52.3, 68.2, 103.6,
139.8, 156.4 and 179.2; nothing overlaps, and Varga's last approach line closes before the warning. In the fight
Okafor's t=194 line closes at ≈ 198.3 and Varga's t=200 line at ≈ 205.5, before the first membranes' third song;
the Choir's lines ride the Heart's triggers (never during a told line's tell, the Heart's rule), and the friendly
event lines wait for a free slot and drop rather than talk over the Choir. After the kill the lines close at
≈ K+10.3 (the Choir), K+20.7, K+28.9, K+37.6, K+46.0, K+53.1 and K+57.9; the race's two lines are done before the
warp (K+29), and the level end at K+58 follows Okafor's last.

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
  of 27 MW; the Swarm Call; Rook's Autocannon): forward 198 against the reference **200** (0.99, the act's lowest);
  the fight's measurement uses this fit (M11's Level 50 part), five seeds inside 150–180 s.
- **Bounty**: the Heart's formula (24 % of budget(50) ÷ (16.777 × `bounty_scale`)) with this level's scale **0.34**
  gives **811** in Act 1 terms, split as the spec's 30 / 30 / 40: each membrane **41** (246), each node **61** (244),
  the core **321**; paid **≈ 4,626 = 24.0 %** of budget(50) (the spec's hand 0.60 gave 460, the same paid share). The
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
Gate Warden 40, Coilwyrm 86, Mantis 30, the Heart 811, the crate 100, the secondary 100), × the act factor **16.777**
(1.6⁶), bounties also × the scale (the crate and the secondary are not scaled). The economy's shares: 60 % of the
kills and the released units, the Heart at 100 %, half the secret and the secondary. A **`bounty_scale` of 0.34** puts
the typical haul at **19,313 (+0.2 %)**, the perfect run at 29,663 (**1.54 ×** budget).

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 110 × 5 + Needler 30 × 12 + Mote Swarm 24 × 2 + Stinger 14 × 15 + Whirl Seed 18 × 3 + Spore Bomber 3 × 25 + Choir Herald 4 × 30 + Rift Skater 16 × 14 + Lamprey 18 × 12 + Choir Seraph 6 × 35 + Wraith 4 × 30 + Chimera 36 × 14 + Spine Turret 8 × 12 + Harrow 30 × 12 + Threadcrawler 2 × 56 + Polyp Mortar 6 × 15 + Gate Warden 4 × 40 + Coilwyrm 1 × 86 + Mantis 2 × 30 (3,655) | 20,849 | 12,509 |
| Boss: Choir Heart (6 membranes × 41 + 4 nodes × 61 + the core 321) (811) | 4,626 | 4,626 |
| Boss releases: Mote Swarm 16 × 2 + Whirl Seed 18 × 3 + Skitter 12 × 5 (a typical fight) (146) | 833 | 500 |
| Secret: the seed vault (hidden crate) | 1,678 | 839 |
| Secondary: break the swarm (65 %) | 1,678 | 839 |
| **Total** (bounty scale 0.34) | **29,663** | **19,313** |
| Budget(n) = the typical haul's target; typical +0.2 %, perfect 1.54 × budget | | 19,271 |

The scale sits with the act's others (L43's draft 0.27, L45's 0.30): the three-minute approach at ≈ 111 a minute
earns most of the haul. The Heart's bounty is recomputed from its formula at this scale (**811**, from the spec's
hand 460 at 0.60), so it keeps the act boss's **24 %**; the spec's shares stay. **Balance plan**: the L50 visit buys
nothing (≈ 43 000 unspent: the campaign's last cushion, the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)); 26 of 27 MW; Rook's Autocannon.

## Difficulty notes

- **Easy**: the formation lever thins the approach (≈ 280), the Heart's easy hooks (rests ×1.3, one membrane at a
  time, the walls to 360 px, no reversal, the dark's ambient 20 %, one storm attack at a time; HP 14,925), the
  checkpoints at the warning and each phase, the secondary's 65 % of the easy count.
- **Hard**: ≈ 390 in the approach, the Heart's hard hooks (rests ×0.8, the heartbeat ring of 16, the walls to 300 px,
  the line 1.0 s, HP ×1.05), **no boss checkpoint**, the vault's 40 HP.
- Every difficulty: the ending scene is the same (no fail, no input).

## Concept art

None yet. The level needs (round 84's proposals, then the M11 part's production art):

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

## Implementation

- [ ] Level data `data.yaml` (M11's build): sections and speed, `reach-end` with the Heart as the condition, the
      `kill_ratio` secondary (65 %, `boss_counted: false`, resolved by the warning), waves, ground targets, radio,
      budget, threat profile, briefing, backdrop (the hive core, the tissue floor), music; `medium: vacuum`, the
      `beyond-gate` limits (`NO COVER`); no `next_act` (the campaign's last level)
- [ ] The Choir Heart as its spec (M11 part H), its warning at t=182 before it shows, the checkpoints (easy and
      medium: the warning and each phase's start)
- [ ] **The closing gate scene** after the kill: the shower collected, control taken, the formation, the race (the
      scroll to 300 px/s, the chambers in reverse, the folds behind, the *Steadfast*'s markers), the reversed warp
      without a flash or an inversion, the near side, the event plane going out and the glyphs going dark; the
      radio's noise off on the near side
- [ ] The campaign end after the level ([debrief](../../../ui/debrief/README.md#the-campaign-end-act-7-draft)):
      the final debrief and act summary, the ending pages, the end card, the completed save
- [ ] `SILENCE THE CHOIR`, `KILLS n / 219`, the Heart's three-segment bar
      ([HUD](../../../ui/hud/README.md#act-7-additions-draft)), `THE GATE IS CLOSING` on the event line
- [ ] Wave script to the Act 2+ rules over the approach (`ActFloorTest`, `PacingTest`), ≥ 80 a minute (`DensityTest`,
      counted to the warning), ≥ 60 % front, the medium-hull spacing; ≥ 4 s of calm after the Heart (the scene)
- [ ] Radio voiced with the step-5 noise and its end on the near side, every timed line at most 1 s late
      (`RadioTimelineLevel50Test`, with Rook and without); the Choir's lines on the clean enemy channel
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`);
      `TraitCoverageTest` covering Level 50
- [ ] Briefing: both pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(50) = 19,271 with the level's `bounty_scale` (`TypicalHaulTest`); the Heart's
      bounty from its formula at that scale
- [ ] Music and ambience: track 17, the warning, track 20, its `choir` stem and its coda for the race, the
      `vrell-hive` bed with the heartbeat layer, `belt-open` on the near side, the level's sounds (`Level50SoundsTest`)
- [ ] Easy/hard variations; the autopilot flies it on every difficulty with Rook and with `--escort none`, through
      the scene to the campaign end
- [ ] Production art and captures for the build's round

## Open questions

- **The race**: a scene (this draft, round 80's lesson: an unclear chase reads worse than a scene); the alternative is
  a short flown escape with tissue folds to dodge and no fail (Level 42's escape was a scene too).
- **The checkpoints**: per phase on easy and medium (this draft) or the warning's only.

## Decisions

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
