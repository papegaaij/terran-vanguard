---
title: Level 45 – Hive Reef
design: approved
implementation: done
art: chosen
depends-on: [../../../world/vrell-space, ../../../enemies/ground/spawning-organ, ../../../enemies/ground/hive-node, ../../../enemies/ground/threadcrawler, ../../../enemies/air/whirl-seed, ../../../enemies/air/brood-pod, ../../../enemies/air/spore-bomber, ../../../enemies/ground/spine-turret, ../../../enemies/ground/polyp-mortar, ../../../player/generator, ../../../player/specials, ../../../player/wingmen, ../../../systems/retry, ../../../ui/hud, ../../act-2-homefront/level-09-arcology-fall]
updated: 2026-11-06
---

# Level 45 – Hive Reef

## Summary

Past the gate the wing flies low over a **hive reef**, a moon-sized living structure in the spore nebula where the
Vrell breed. A `destroy-targets` level: **eight spawning organs** on the wing's lane must all burn; one that scrolls
past alive fails the mission at once (Levels 09 and 39's rule). The reef breathes: **spawning pits** open on a 2 s
tell and swarms rise out of them, the organs throw [Whirl Seed](../../../enemies/air/whirl-seed/README.md) clusters,
**spore clouds** slow the ship, and three [Threadcrawler](../../../enemies/ground/threadcrawler/README.md)s cross the
plates (the secondary). On the reef's spine the wing cuts loose a **call organ**, the captured Choir tech that becomes
the Vrell Swarm Call. Ground and air, all from the front; 278 s at 100 px/s, ≈ 115 enemies a minute at medium.

**Status: approved** (Act 7 design phase, step `m11-d-levels-2`, 2026-11-04; the user approved it on 2026-11-05, round 84).
Written from the act roster, the gap analysis's recommended answers (**Q9 b**, **Q10 a**) (the user changed Q10 to **b** in round 84; the looks below follow b) and judgement calls (J3, J12,
**J13**, **J16**) and the earlier steps' drafts, which are the source of truth for their numbers:
[Vrell space](../../../world/vrell-space/README.md)'s reef, mood and hazards (*Spore clouds*, *Living terrain*), the
Threadcrawler's and the Whirl Seed's Act 7 sections, the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft), the
[Vrell Swarm Call](../../../player/specials/README.md#act-7-specials-draft),
[Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft) and the HUD's
[Act 7 additions](../../../ui/hud/README.md#act-7-additions-draft). **Built** in M11 part C (step C2): the
[data.yaml](data.yaml) holds the level (its tables below are rendered from it), measured with `levelReport`.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft; D-story may reword them):

<!-- data: briefing -->
> **Commander Okafor:** "Past the gate there is a reef the size of a moon, and it is alive.
> Every Vrell ship we have fought was born somewhere like it. Eight spawning organs grow on your
> lane. Burn every one: one left alive breeds for their whole swarm. Rook flies your wing."
>
> **Dr. Varga:** "The reef breathes: its pits glow before they open, the organs swell before
> they spit seeds. Violet clouds are spores; they slow you, and a blast burns them away. On the
> spine a pale horn calls the broods. Cut its roots and bring it home."
<!-- /data -->

*Images* (production art in M11's build, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-45-hive-reef`: the fleet's scan of the reef from above, a violet and teal disc veined like a leaf;
  the wing's lane a dotted line across it, the eight organs ringed and numbered O1–O8, the spine and the living bridge
  marked, "SPORE DENSITY HIGH" hatched over the clouds.
- Varga's page `level-45-spawning-organ`: her field sketch in grey outline, twice: a spawning organ (a swollen sac on
  the plates, its seed pod drawn swelling, "0.5 s") and the call organ (a pale ribbed horn, its four roots marked
  "CUT HERE", the horn itself marked "DO NOT FIRE").

*Hangar teaser* (shop screen before L45; Rook speaks, not voiced):

<!-- data: teaser -->
> **Rook:** "A reef that breeds. I'm loading the Mortar."
<!-- /data -->

The plan's L45 visit buys the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md) **L5** (the level's
`area`) and fits Rook's owned **Mortar** (the organs' `anti-ground`): 24.5 of 25 MW
([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The intel panel's `SPECIAL LIMITED`
names the Airstrike and the Orbital Lance if fitted (`NO COVER`, the
[Act 7 specials](../../../player/specials/README.md#act-7-specials-draft)); the plan's EMP Burst is unaffected and
stuns the reef's living turrets and crawlers (Vrell units).

## Threat profile

Hand-written (draft); directions are counted by each wave's `from` (a pit's swarm counts as `front`: the pits lie in
the field's top 60 % and rise ahead of the ship).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `ground` (the reef: the spawning organs, the pits, Threadcrawlers, Spine Turrets, Polyp Mortars), `air` (Skitter streams, Mote Swarms, Needlers, Stingers, Brood Pods, Whirl Seed clusters), `low-air` (Spore Bombers) |
| Attack directions | front 100% |
| Density | 5 |
| Recommended traits | `anti-ground`, `area` |
| Hazards | Spore clouds (slow, never hide bullets); spawning pits (2 s tell); the organs' seed clusters |
| Boss / mid-boss | none |
| Special limits | `NO COVER` (the Airstrike, the Orbital Lance; J12), as the act's every level |
| Sensor-suite detail | **none**: Vrell space, the hive reef, `ground` + `air`, front, the objective `ORGANS 0 / 8` · **L1**: + density 5, the hazards "spore clouds" and "spawning pits", the secondary `CRAWLERS 0 / 2`, `SPECIAL LIMITED` (`NO COVER`) · **L2**: + Threadcrawler, Brood Pod, Spore Bomber, Spine Turret, Polyp Mortar, Needler, Stinger, Whirl Seed, Skitter and Mote portraits · **L3**: + the eight organs and the call organ on the threat map, `anti-ground` and `area` highlighted |
<!-- /data -->

*Varga's intel line* per sensor-suite level (must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "A living reef, Lancer. Eight spawning organs to burn." |
| L1 | "Spore clouds slow you. Pits open under the swarms." |
| L2 | "Threadcrawlers, Brood Pods, turrets grown from the reef." |
| L3 | "Anti-ground for the organs. A blast burns a cloud." |

**Trait coverage** (`TraitCoverageTest`, the lesson): `anti-ground` and `area` are covered by one loadout of what is
in the shop at the L45 visit within the plan's 20 MW for the weapons: the
[Hammer Mortar](../../../player/weapons/hammer-mortar/README.md) alone (front, both traits, 3 MW at L1), or any front
with the [Bomb Rack](../../../player/weapons/bomb-rack/README.md) and the Tesla Coil Pod on the wings (1 + 2 MW). The
plan's own fit covers `area` (the Tesla pod) but not `anti-ground`, as Level 39's did: Rook's Mortar (left out of the
test) and the organs' soft hide make up for it; the launch warning names the trait, as built.

## Objective

- **Primary** `destroy-targets` (**J16**): destroy the eight named **spawning organs**, each its own ground-target
  group **O1–O8** (t≈28, 58, 80, 112, 152, 192, 226, 252: one alone in section 1, then two a section). The tracker
  reads **`ORGANS n / 8`**, counting to its target.
  - **Fails at once** (the campaign's [objective failure](../../README.md#pacing-rules), as Levels 05, 09 and 39): the
    first organ that leaves the screen alive fails the mission through the
    [failed primary objective](../../../systems/retry/README.md#on-a-failed-primary-objective) flow (the mission failed
    screen 3 s later; the lost-mission prompt as built), with Okafor's `mission-failed` line naming the organ.
  - **No holds** (unlike Levels 09 and 39): each organ is on the screen ≈ 6.2 s at 100 px/s (540 px of field plus its
    76 px), about six times its time to kill at the reference; never two organs on the screen at once (≥ 20 s apart);
    Rook's Mortar takes them first by his ground priority. See *Open questions*.
  - **Readable on the screen** (the round 80 lesson): an organ is the largest, brightest thing on the reef (a tall
    ribbed sac on nine roots with a ring of six pods round its iris and a pulsing rim, A.5 b, in the one Vrell palette), no spore cloud drifts over one while it is on the screen, Okafor names the
    first as it enters, and the tracker's mark **blinks amber while an organ is in the field's bottom third** (the
    leaving warning L43's draft proposes for its Wardens; one rule for both, D-act's cross-doc point).
- **Secondary** `escapes` (the Threadcrawlers): kill at least **2 of the 3** Threadcrawlers before they cross off the
  reef (`escapes: threadcrawler`, `allowed: 1`; easy 1 of 3, hard 3 of 3), met at the second kill (`DONE`), failed by
  the second one away. The tracker's second line counts to its target, **`CRAWLERS n / 2`**. Pays **100** in Act 1
  terms (**1,678** in Act 7, the act factor 1.6⁶ = 16.777); the typical haul counts 50 %. (A `destroy-targets`
  primary rules out a `groups` secondary: the [level schema](../../../tech/architecture/schemas-levels/README.md).)
- **The call organ** (Q9 b) is a story pickup, not an objective: see *The call organ*.

### The spawning organ

A unit of its own, the [spawning organ](../../../enemies/ground/spawning-organ/README.md) (its stat block and
behaviour live there; step C2 promoted it from "a row in the enemies' data", which cannot load: the primary's groups
and the loader find units only under `design/enemies/`, the Spawning Reef's precedent). It plays by the
[Hive Node](../../../enemies/ground/hive-node/README.md)'s rules (its iris tell, its release, its shut-while-close rule,
here a hold) **but is not hardened** (J16): every weapon that hits `ground` hurts it, `anti-ground` does ×2, `area`
blasts reach it whole. 80×80 px (hitbox 60×60), **183 HP** (easy 137 / hard 238), its first release **1.5 s** after
its centre crosses the top edge, then every **3.0 s**, a Whirl Seed cluster (6; easy 5, hard 8) and a Skitter pair
(hard 3) by turns, held while the ship is within **120 px**; 45 credits; not counted among the level's enemies. Death:
the sac tears, a short flash and a ring of ichor, the torn husk stays on the plates; a dry tearing at the release and
the death (no pop). **Measured** (step C2, the plan's fit): each organ dies 1.2–2.8 s after it enters the screen (about
a third of its 6.2 s), so most never release; none got away in 18 autopilot runs (with Rook and without).

### The call organ

The Q9 b capture (the [Vrell Swarm Call](../../../player/specials/README.md#act-7-specials-draft)'s story): a pale,
ribbed horn about 64×40 px on the reef's spine (section 3, at x≈240, its centre at the top edge at **t≈127**), no
glow but a slow breathing of its ribs, clearly unlike the organs (it is not on the tracker and is no target:
shots pass over it). It is held by **four roots** (`ground` triggers, **15 HP** each, no bounty) around it. With the
four roots cut the horn lifts off the plates and drifts up to the play plane over 2 s (t≈131–135 for a player who
shoots at once), and is **collected by flying through it** (`CALL ORGAN TAKEN` on the HUD's event line). If the
player misses it, the *Steadfast*'s salvage tug behind the wing picks it up (Okafor's event line; Level 34's rule for
the Helix core), so it is never lost: **the Swarm Call is in the shop from the L46 visit by winning the level**
(10 000, the [specials](../../../player/specials/README.md#act-7-specials-draft)). No wave enters from t=126 to t=138;
no spore cloud drifts near it; it pays no credits.

## Layout

Scroll **100 px/s** (script time: every time here is the scroll distance ÷ 100; no holds). Setting:
[Vrell space](../../../world/vrell-space/README.md#sub-locations-per-level)'s **hive reefs in the spore nebula**, the
reef a `ground` layer from t = 0, `vacuum` (J12). **Light** ([mood](../../../world/vrell-space/README.md#light-per-level-q10-b),
Q10 b, the one Vrell palette): teal-black and violet chitin, glowing mint-teal veins, the pits' glow, violet spore clouds; the reef a
step darker than the units on it; veins pulse slowly and are never bullet-sized dots. Motion budget: the opening pits
and the organs' swell are the strong elements; the veins' pulse and the drifting clouds stay slow. Every haze and glow
smooth (≥ 32 steps, no ordered dither, fading to 0 inside its sprite's canvas: the Act 5 lesson).

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Reef Rim | 0–40 | 0–4,000 | 100 | clear | `ground`: chitin plates rising out of the nebula, the first pit (opens t=12), organ **O1** (t≈28). `low-air`: floating spore sacs. `deep`: the nebula, the next reefs far off. | **The pit taught** alone, **the first cloud** (t=18) alone, **the first organ** alone. |
| 2. The Brood Fields | 40–108 | 4,000–10,800 | 100 | clear | `ground`: rows of pits, organs **O2** (t≈58) and **O3** (t≈80), the first Threadcrawler's pass (t≈90), Spine Turrets, a Polyp Mortar pair. | The reef at full breath: pits, Brood Pods, clouds. |
| 3. The Spine | 108–160 | 10,800–16,000 | 100 | clear | `ground`: the reef's ridge, a raised backbone of plates; organ **O4** (t≈112), **the call organ** (t≈127), organ **O5** (t≈152). | **The capture** in a quiet window (t=126–138). |
| 4. The Living Bridge | 160–212 | 16,000–21,200 | 100 | clear | `low-air`: a living bridge of tendon and chitin arching over the field (t≈160–175) to the next reef. `ground`: the first reef's edge, the gap, then the second reef's rim (five pieces); the courier's cyst (t≈176), the second Threadcrawler (t≈176), organ **O6** (t≈192). | Spore Bombers under the bridge; the clouds at their densest. |
| 5. The Brood Heart | 212–268 | 21,200–26,800 | 100 | clear | `ground`: a ring of pits round a deep brood well, organs **O7** (t≈226) and **O8** (t≈252), the third Threadcrawler (t≈236). | The hardest stretch; the last organ. |
| 6. The Far Rim | 268–278 | 26,800–27,800 | 100 | clear | `ground`: the reef's edge falling away to the nebula. `deep`: the first dark hulls of the dead fleet far ahead (Level 46). | ≥ 4 s of calm (the last wave gone by ≈ t=271); level end at t=278. |
<!-- /data -->

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). Built and measured in step C2 (`levelReport`, the plan's fit with Rook).

- **HP** from the proposed Act 7 reference DPS (J11: **180** at L45,
  [Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)): the Threadcrawler at its
  [Act 7 values](../../../enemies/ground/threadcrawler/README.md#act-7-the-hive-reef-draft) (whole 441); the returning
  `medium` units with the act HP factor from their first level: the **Brood Pod** 30 × 180/38 = **142**, the
  **Spore Bomber** 24 × 180/32 = **135**, the spawning organ **183** (above); the `small` units with the
  [beam era](../../../enemies/README.md#balancing-basis)'s ×2 as built (Needler 8, Stinger 12, Spine Turret 16, Polyp
  Mortar 20); the Skitters, Motes and Whirl Seeds `tiny` (HP 1). No unit carries a hex shield (J4).
- **Density and pacing** (the Act 2+ floor): **532 enemies** over 278 s, **≈ 115 a minute** at medium (the act's
  minimum 80; Act 6's band 100–118): 425 in the 66 waves below, 64 from the organs (8 clusters of 6 Whirl Seeds, 8
  Skitter pairs; a fast kill saves the second release, so the build measures it), 18 from the three Brood Pods'
  bursts, 25 on the ground (3 Threadcrawlers, 14 Spine Turrets, 8 Polyp Mortars); the 8 organs are objectives, not
  counted (their unit's `counted: false`; Level 39's towers, by contrast, are counted). Easy ≈ 440, hard ≈ 610 by the formation lever. A wave about every 3 s; the floor comes
  from the Needlers' thorns, the Stingers' fans, the turrets' and mortars' fire, the Threadcrawlers' pores, the Brood
  Pods' bursts and the seed clusters: expected to meet ≤ 3 pauses over 2 s, ≥ 2.5 on the screen and ≥ 10 shots a
  minute (`PacingTest`, `ActFloorTest`), **to be confirmed with `levelReport`**. **Measured** (step C2, pending the
  user's review in round 90): the approved 66 waves alone gave 1.0 on the screen and 2 shots a minute at medium (27
  pauses over 2 s): the plan's Tesla Coil Pod L5 and the beam clear the reef's popcorn at once. So step C2 added the act's
  **denser mixed waves** (the user's answer for Act 7, 2026-11-05: "not just with small enemies"): 61 waves of Chimera
  and Needler wings, Harrow streams, Stinger columns and Spore Bomber lines on the offbeats (never in the quiet
  windows), 664 counted units in the waves at medium (easy 527, hard 801), **≈ 154 a minute**: 3.0 on the screen and
  21 shots a minute at medium (hard 5.0 and 47), the three quiet windows the only pauses over 2 s at medium and hard
  (easy: the same three over 3 s, listed in `PacingTest` pending the review), ≥ 11 s of calm at the end.
- **Directions**: 66 of 66 waves front (100 %); the pit swarms count as front (*Threat profile*).
- **Pit swarms** (the [living terrain](../../../world/vrell-space/README.md#hazards--set-pieces)): rows marked `pit`
  rise from a named pit at their `t`, its 2 s tell from `t − 2`; 0.5 s rising, harmless; a pit whose rise would put a
  unit within 120 px of the ship holds closed until it would not (so a pit wave may come late: the build's autopilot
  checks it). The other pits are scenery that breathes but never opens.
- **Quiet windows**: no wave enters t=8–20 but the first pit's (t=12; the first cloud drifts in at t≈18 alone), t=26–33
  (organ O1, t≈28, alone with its first cluster) or t=126–138 (the call organ).
- **Limits and spacing** (the Act 5 lesson): the Brood Pods are the level's only `medium` air hulls, one at a time
  (t=46, 102, 201: each bursts within 8 s); the Spore Bombers fly `low-air` (below the play plane); never two
  Threadcrawlers on the screen (≥ 60 s apart), their paths clear of the pits and the organs (the crawler's Act 7
  section); at most 4 Spine Turrets and 2 Polyp Mortars firing at once; never two organs on the screen; no seed
  cluster opens within 120 px of the ship.

The wave table (rendered from the data; every wave enters from the front, the pit swarms out of their pits, named in
the notes):

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (left) | The reef rim: popcorn over the plates |
| 6 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front | |
| 7.5 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 12 | 1 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | Out of pit P1. **The first pit, alone**: its 2 s tell from t=10 (the teaching); no wave enters t=8–20 but this one |
| 21 | 1 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | After the first spore cloud (t=18) drifts in alone |
| 22.5 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 24 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (right) | Gone before the first organ (t=28); no wave enters t=26–33 |
| 34 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 35.5 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 37 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front | |
| 38.5 | 1 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 40 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (left) | The brood fields |
| 41.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 43 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | Out of pit P2 |
| 46 | 2 | single | [Brood Pod](../../../enemies/air/brood-pod/README.md) | 1 | front | Bursts into 6 Skitters 8 s after entering, or when killed |
| 48 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 50 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (right) | |
| 51.5 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 53 | 2 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 56 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | Out of pit P3. Before organ O2 (t=58) |
| 57.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 61 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 62.5 | 2 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 64 | 2 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 67 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (left) | |
| 68.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 70 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | Out of pit P4 |
| 73 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front | |
| 74.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 76 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (right) | Before organ O3 (t=80) |
| 78.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 81.5 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 83 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 84.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 86 | 2 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Thin while the first Threadcrawler (t=90) crosses |
| 88.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 90 | 2 | snake | [Threadcrawler](../../../enemies/ground/threadcrawler/README.md) | 1 | front | **T1**, left to right across the open plates (clear of the pits and organs: VrellChecks); its head drops the armour patch |
| 93 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | Out of pit P5 |
| 94.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 96 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (left) | |
| 97.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 99 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front | |
| 100.5 | 2 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 102 | 2 | single | [Brood Pod](../../../enemies/air/brood-pod/README.md) | 1 | front | Alone of its tier on the screen; killed before it bursts, it drops the shield cell |
| 103.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 105 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 107 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 109 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (right) | The spine; organ O4 at t=112 |
| 111 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 114 | 3 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 115.5 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 117 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | Out of pit P6 |
| 118.5 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 120 | 3 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | `low-air`, over the spine |
| 121.5 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 123 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (left) | The last before the call organ: no wave enters t=126–138 |
| 124.5 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 139 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front | After the call organ |
| 140.5 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 142 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | Out of pit P7 |
| 143.5 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 145 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (right) | |
| 146.5 | 3 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 148 | 3 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Before organ O5 (t=152) |
| 150 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 153.5 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 155 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 156.5 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 158 | 3 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | Out of pit P8 |
| 159.5 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 161 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (left) | The living bridge overhead |
| 162.5 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 164 | 4 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 165.5 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 167 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 168.5 | 4 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 170 | 4 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 172 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 174 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 176 | 4 | snake | [Threadcrawler](../../../enemies/ground/threadcrawler/README.md) | 1 | front | **T2**, right to left over the second reef (its lip at row ≈17510) |
| 178 | 4 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | Out of pit P9. After the second Threadcrawler (t=176): the second reef's first pit (step C2: from t=173, its pit on the reef's lip) |
| 180.5 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front | |
| 182.5 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 185 | 4 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | `low-air` |
| 186.5 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 188 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (right) | Before organ O6 (t=192) |
| 190 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 193 | 4 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 195 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | Out of pit P10 |
| 196.5 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 198 | 4 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 199.5 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 201 | 4 | single | [Brood Pod](../../../enemies/air/brood-pod/README.md) | 1 | front | |
| 202.5 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 204 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (left) | |
| 205.5 | 4 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 207 | 4 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 208.5 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 210 | 4 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | Out of pit P11 |
| 211.5 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 213 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | The brood heart |
| 214.5 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 216 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (right) | |
| 217.5 | 5 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 219 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front | |
| 220.5 | 5 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 222 | 5 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | Out of pit P12. Before organ O7 (t=226) |
| 224 | 5 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 227 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 229 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 230.5 | 5 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 232 | 5 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Before the third Threadcrawler (t=236) |
| 233.5 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 236 | 5 | snake | [Threadcrawler](../../../enemies/ground/threadcrawler/README.md) | 1 | front | **T3**, left to right before the brood well |
| 239 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | Out of pit P13 |
| 240.5 | 5 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 242 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front | |
| 243.5 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 245 | 5 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | `low-air` |
| 246.5 | 5 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 248 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front (left) | Before organ O8 (t=252) |
| 250 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 253.5 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 255 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 256.5 | 5 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 258 | 5 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | Out of pit P14. The last pit |
| 259.5 | 5 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 261 | 5 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | |
| 262.5 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step C2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 264 | 5 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | **The last wave**; gone by ≈ t=271, the calm to the end at t=278 |

Totals: Skitter 161 · Needler 111 · Harrow 72 · Stinger 60 · Chimera 64 · Mote Swarm 160 · Spore Bomber 30 · Brood Pod 3 · Threadcrawler 3.
<!-- /data -->

## Ground targets

| Section | Target | Effect |
|---|---|---|
| 1 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) pair on the rim plates (t≈36) | Bounty 12 each; the reef's living turrets, thorn spines grown from the plates |
| 1 | Spawning organ **O1** (t≈28) | Primary; alone, named by Okafor |
| 2 | Spawning organs **O2** (t≈58), **O3** (t≈80) | Primary |
| 2 | Spine Turret pair (t≈62), Spine Turret pair (t≈96) | Bounty 12 each |
| 2 | [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) pair between the pit rows (t≈70) | Bounty 15 each |
| 2 | [Threadcrawler](../../../enemies/ground/threadcrawler/README.md) **T1** (t≈90), one pass left to right across the plates | Secondary; bounty 56; its head drops an **armour patch** |
| 3 | Spawning organs **O4** (t≈112), **O5** (t≈152) | Primary |
| 3 | Spine Turret pair on the spine (t≈118) | Bounty 12 each |
| 3 | The call organ's four roots (15 HP each, t≈127) | The capture (*The call organ*); no bounty |
| 3 | Polyp Mortar pair (t≈140), Spine Turret pair (t≈146) | Bounty 15 / 12 each |
| 4 | The courier's cyst (30 HP, t≈176) | The secret, see below |
| 4 | Threadcrawler **T2** (t≈176), right to left | Secondary; bounty 56 |
| 4 | Spine Turret pair (t≈184), Polyp Mortar pair (t≈198) | Bounty 12 / 15 each |
| 4 | Spawning organ **O6** (t≈192) | Primary |
| 5 | Spawning organs **O7** (t≈226), **O8** (t≈252) | Primary; the last two |
| 5 | Polyp Mortar pair on the well's lip (t≈230), Threadcrawler **T3** (t≈236, left to right), Spine Turret pair (t≈240) | Bounty 15 / 56 / 12 |

Totals: organs 8 · Threadcrawlers 3 · Spine Turrets 14 · Polyp Mortars 8 (the 25 counted ground units).

## Hazards

- **Spore clouds** (**J13**): the [Vrell space](../../../world/vrell-space/README.md#hazards--set-pieces) rule as its
  document defines it (0.7 of the ship's speed while inside, the backdrop dimmed a step, bullets and pickups drawn
  above, a unit inside under at most a 25 % veil, an `area` hit or the Plasma Arc burns it away in 1 s). This level
  sets where and when: **10 clouds**, at t≈18 (the first, alone, x≈300), 50, 72, 96, 117, 145, 166, 170, 222, 246; the
  two of section 4 (t≈166, 170) on opposite halves under the living bridge; at most two on the screen, never
  overlapping, never over an organ or the call organ while it is on the screen, never resting over the ship's band
  longer than 2 s. Rook's Mortar burns them as the player's blasts do. They pay nothing.
- **Spawning pits** (the [living terrain](../../../world/vrell-space/README.md#hazards--set-pieces)): 14 pit swarms
  (the `pit` rows), each from its own pit, the 2 s tell and the 120 px rule.
- **The organs' seed clusters**: the Whirl Seed's ricochets off the side edges; the 120 px hold.
- **Special limits** (J12): `NO COVER` for the Airstrike and the Orbital Lance; the Bomb Rack and the Hammer Mortar
  have their ground (the reef); the EMP Burst stuns every unit here (all Vrell) but never the organs' clocks (objects,
  not units: they hold nothing to stun; see *Open questions*); the Smart Bomb clears the seeds in flight.

## Secrets and pickups

- **The courier's cyst** (the secret, section 4, t≈176 (step C2: from t≈172, which lay in the gap before the second reef), x≈90, on the second reef's rim): a gold Ascendancy courier pod
  the reef has grown over, a lump of chitin with a gold edge showing, no glow. A **30 HP** cyst; broken, it frees a
  hidden crate worth **100** in Act 1 terms (**1,678**). The Salvage scanner's glint marks it. Its event line, Rook:
  "*Gold under the chitin. One of Vorne's. The reef ate it.*"
- **Armour patch**: dropped by the first Threadcrawler's head (t≈90).
- **Shield cell**: dropped by the second Brood Pod (t=102) when killed before it bursts.
- **Overdrive**: dropped by the right Spine Turret of the t≈184 pair.
- No supply drone (L43's was the last past the ring).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), drafted for the **1 s rule** (≈ 12 characters a second, a
1.5 s lead, 0.4 s of silence; event lines drop after 6 s); `RadioTimelineLevel45Test` checks them with Rook and
without. **The radio's noise** (J3): step **3** of the act's rise (L43 → L47), the subtitles clean; the Choir's channel
clean (it is close). **Speakers**: Okafor (from the *Steadfast*), Varga, Rook (barks `requires: escort`), the Choir
once on the enemy channel (its fearful Act 7 voice; the [Silence's rule](../../../story/README.md): it names nothing).

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, Steadfast. A living reef the size of a moon." |
| t=9.6 | Varga | "Pits glow, then open. Don't sit over one." |
| t=17 | Varga | "Violet spores slow you. A blast burns them away." |
| t=24.6 | Okafor | "Spawning organ ahead. Eight on our lane. Burn them all." |
| t=33 | Okafor | "Let none past, Lancer. Each one breeds a fleet." |
| The first organ destroyed (`first-target`, event, bark) | Rook | "One down. It screamed. I didn't like that." |
| t=87 | Varga | "Threadcrawler on the plates. Take it before it crosses the reef." |
| t=121.5 | Varga | "The pale horn on the spine calls their broods. Cut its roots and bring it out. Don't burn it." |
| The call organ collected (event) | Varga | "It's still singing. Quietly. I want to know what it says." |
| The call organ missed (event, it leaves the screen) | Okafor | "The tug has the horn. Keep your eyes on the organs." |
| t=160 | Okafor | "Living bridge overhead. The next reef is just as hungry." |
| t=208 (enemy channel) | The Choir | "…do not burn the brood… it is all we carry…" |
| t=215 | Varga | "Carry? Carry where? Lancer, it's never said a thing like that." |
| t=248 | Okafor | "Last two organs. Then this reef is only meat." |
| Secondary met (`secondary-objective`, the second crawler down) | Varga | "Two crawlers down. The plates are quieter." |
| Secondary failed (the second crawler off the reef) | Varga | "Another crawler got away. It will find a reef of its own." |
| Level end | Okafor | "The reef's gone quiet. A graveyard of their ships ahead. Varga wants a look." |
| An organ leaves the screen alive (`mission-failed`, the failed screen's line) | Okafor | "{group} is still breeding behind you. That brood comes for the fleet. Pull back, Lancer." |
<!-- /data -->

Timeline check (text, by the rule above): the timed lines close at ≈ 9.2, 16.6, 24.2, 32.4, 40.3, 93.8, 130.8, 166.2,
213.1, 220.2 and 249.8; nothing overlaps. Varga's pit line closes as the first pit opens (tell from t=10), her cloud
line as the first cloud enters (t≈18); Okafor names O1 before it enters (t≈28) and states the failure rule while it is
on the screen. The crawler line comes before T1 (t≈90). The horn line closes as the horn's roots are on the screen
(t≈127). The Choir's line and Varga's answer (the Act 7 fear, and the seed of Level 46's "*singing away from
something*") sit in section 4's steady stretch. The event lines wait up to 6 s for a free slot. **Step C2** (the game's radio pages, `RadioTimelineLevel45Test`: 22 characters a line, 3 lines a page, typed at 30
a second, a page held 3 s and the last 5 s): the opening's two-page lines ran up to 8 s late, so the t=1, 9.6, 17, 24.6
and 33 lines are cut to one page each (the table above; pending the user's review in round 90), Varga's answer to the
Choir moves from t=213.5 to t=215 and Okafor's "*Last two organs*" from t=244.5 to t=248 (after the crawlers' met line
when the third crawler's kill meets it); every timed line now starts on time.

## Music & ambience

The Act 7 A theme (**track 16**, new, J8; the cues are D-audio's
[music](../../../audio/music/README.md#cues-per-level-act-7)): its base stem over the rim and the brood fields, the
base at −6 dB under the call organ's capture (t=126–138), the full mix from the living bridge to the brood heart with
the `choir` stem over the brood heart, fading out over the far rim. Ambience: the **`vrell-hive`** bed
([sfx](../../../audio/sfx/README.md#ambience-per-setting)): a slow, deep organic breathing under a faint **dry** creak
of growing tissue (no wet sound). New sounds this level needs, for the act's sfx step: the pit's tell and opening (a
low dry parting), the organ's swell and dry tear at its release and death (no pop), the call organ's roots tearing and
its quiet horn note when taken (a real conch or low horn, pitched down: the Swarm Call's own source), the cloud's burn
(a soft dry crackle); the Threadcrawler, the Whirl Seed, the Brood Pod and the turrets as built. Freesound only for
real things (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(45) = 700 × 1.07⁴⁴ ≈ **13,740** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act 1
terms** (Skitter 5, Mote 2, Needler 12, Stinger 15, Brood Pod 20, Spore Bomber 25, Whirl Seed 3, Threadcrawler 56,
Spine Turret 12, Polyp Mortar 15, the spawning organ 45, the crate 100, the secondary 100), × the act factor
**16.777** (1.6⁶), bounties also × the scale. A **`bounty_scale` of 0.30** (the economy's shares: air kills 60 %,
ground targets 80 %, the secret and the secondary 50 %; a first estimate for the build to confirm with
`TypicalHaulTest` and `levelReport`) puts the typical haul at **13,676 (−0.5 %)**, the perfect run at 21,983 (1.60 ×
budget). No boss or mid-boss. The primary's groups pay no objective credits (the schema's rule); each organ pays its
bounty. The call organ pays nothing (a story pickup).

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 161 × 5 + Needler 111 × 12 + Harrow 72 × 12 + Stinger 60 × 15 + Chimera 64 × 14 + Mote Swarm 160 × 2 + Spore Bomber 30 × 25 + Brood Pod 3 × 20 + Threadcrawler 3 × 60 by parts (48 whole) + released Skitter 18 × 5 + released Whirl Seed 48 × 3 | 18,086 | 10,865 |
| Ground targets: Spawning organ 8 × 45 + Spine Turret 14 × 12 + Polyp Mortar 8 × 15 | 1,844 | 1,475 |
| Secret: courier's cyst (hidden crate, 1% of budget) | 1,678 | 839 |
| Secondary: kill 2 of the 3 Threadcrawlers | 1,678 | 839 |
| **Total** (bounty scale 0.17) | **23,286** | **14,019** |
| Budget(n) = the typical haul's target; typical +2 %, perfect 1.69 × budget | | 13,740 |
<!-- /data -->

**Measured** (`levelReport`, step C2, with the denser mixed waves): the typical haul **14,019 (+2.0 %)** at the scale
**0.17**, the perfect run 23,286 (1.69 × budget); the hand estimate 0.30 below was the approved 66 waves'. The plan's
forward / reference stays 1.10 (`balance.py`).

The hand estimate's scale sat a step above L43's draft 0.27: the reef pays its ground (the organs, a fifth of the haul, are all
destroyed in a won run, so the typical 80 % of them is cautious; counted at 100 % the scale is ≈ 0.29). **Balance
plan**: the L45 visit buys the Tesla Coil Pod L5 (forward 198 against the reference **180**, 1.10, the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)) and fits Rook's Mortar; 24.5 of 25 MW.
The organs fall to the beam and the pods in about a second each, Rook's Mortar and the Tesla's chains help; the Bomb
Rack or the Hammer Mortar (the recommended traits' answer) halves it.

## Difficulty notes

- **Easy**: seed clusters of 5, the organs' easy HP (137), the Skitter pairs of 2, Brood Pod bursts as built (easy),
  the Threadcrawler's easy segments (10) and every other pore; the secondary 1 of 3; the formation lever thins the
  popcorn.
- **Hard**: seed clusters of 8 with each seed's 3-bullet puff (counted in the bullet budget), the organs' Skitter
  triples and hard HP (238), Brood Pod bursts of 8, the Threadcrawler's 16 segments and short pore rest; the secondary
  3 of 3.
- Every difficulty: the 2 s pit tell, the 0.5 s seed swell, the 120 px holds and the clouds' 0.7 slow are fixed (the
  world's rules).

## Concept art

Round 84's proposals (step ART-R-A, M11 design phase, picked 2026-11-05; generator `tools/concept/enemies_r84.py spawning-organ`): the spawning organ. Each sheet holds the source render, the sprites at play size with their states, the readability check over the reef's brightest and darkest band beside the player (a stand-in of the level's light in the one Vrell palette, Q10 b) and key frames; each GIF is the 480×540 play field at the level's 100 px/s. Prompts: [concept/prompts.md](concept/prompts.md).

Round 90 (M11 part C, step ART-C, 2026-11-06; production art, pending the user's review): the spawning organ (A.5 b in
the one Vrell palette), the call organ with its roots and the courier's cyst by `tools/art/act7_reef.py organ call cyst`
(held in `build/art-held/sprites/` until step C2's data claims them), the hive-reef kit by `tools/art/act7_reef.py reef`
(`assets/backdrop/level-45/`, its `backdrop:` block and the pits' places in `concept/backdrop-proposal.yaml` for step
C2; the veins' pulse and the pits' frames by state are step C4's hooks, the yaml names them), the spore cloud's
production art in [Vrell space](../../../world/vrell-space/README.md#concept-art) and the two briefing images in the
[briefing](../../../ui/briefing/README.md#concept-art) (`briefing-images-final-r90-a`). The torn husk is the organ's
`-stump` (the remains' 10 s) and its veins going dark its `-creep`, not a backdrop piece.

| File | What | Status |
|---|---|---|
| [concept/spawning-organ-r84-b.png](concept/spawning-organ-r84-b.png) | Spawning organ b, "reef polyp": a taller ribbed sac on nine roots, the same teal veins, a ring of six seed pods round a crown iris; the same states and check; its colours are to come from the one Vrell palette (Q10 b), not the reef pink of the proposal | chosen (round 84 A.5 b) |
| [concept/spawning-organ-r84-b.gif](concept/spawning-organ-r84-b.gif) | The same scene with b | chosen (round 84 A.5 b) |
| [concept/spawning-organ-final-r90-a.png](concept/spawning-organ-final-r90-a.png) | Production spawning organ (80×80): the iris opening with its glow, the breath, the pods' swell and burst, the death (the sac tearing, the ichor ring), the torn husk as the veins go dark outward; 1× over this level's production reef (sheet) | chosen (round 90 F.1 accept) |
| [concept/spawning-organ-final-r90-a.gif](concept/spawning-organ-final-r90-a.gif) | An organ coming down the brood fields: the swell, the release, the iris, then shot down | chosen (round 90 F.1 accept) |
| [concept/l45-props-final-r90-a.png](concept/l45-props-final-r90-a.png) | The call organ rooted on the spine (ribs breathing), a root knot (whole, hit, cut) and its tearing, the horn lifting over 2 s and drifting loose in its faint glow; the courier's cyst (whole, cracked, broken open) and its break, at 2× (sheet) | chosen (round 90 F.3 accept) |
| [concept/l45-props-final-r90-a.gif](concept/l45-props-final-r90-a.gif) | The four roots cut one by one, the horn lifting and drifting loose | chosen (round 90 F.3 accept) |
| [concept/backdrop-final-r90-a.png](concept/backdrop-final-r90-a.png) | Production backdrop: eight screens at half size with stand-ins (the rim's plates over the nebula, the brood fields' pits, the spine with the call organ, the living bridge over the gap, the second reef, the brood well, the far rim with the dead fleet ahead), the pits' 24 frames, the pieces and the tiles (sheet) | chosen (round 90 F.2 accept) |
| [concept/backdrop-final-r90-a.gif](concept/backdrop-final-r90-a.gif) | The level in a time-lapse, one frame every 4 s, at half size (the veins pulsing) | chosen (round 90 F.2 accept) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (the deep tiles cut at the sections' rows, the four ground tiles, the seam and edge pieces, the second reef's five pieces, the well, the pits, the bridge, the spore sacs, the dead fleet), the sections and the 14 pits' places, for step C2 (not data) | chosen (round 90 F.2 accept) |
| [concept/organ-pit-capture-r90-a.gif](concept/organ-pit-capture-r90-a.gif) | Game capture (step C5, re-captured in step CD5 over the fixed spine): organ O4 releasing beside pit P6 opening, the Mote Swarm rising out of it (t=112–119) | chosen (round 90 L.1 ok) |
| [concept/cloud-burn-capture-r90-a.gif](concept/cloud-burn-capture-r90-a.gif) | Game capture: the first spore cloud burnt away by Rook's Mortar burst (t=18.5–21.5) | chosen (round 90 L.2 ok) |
| [concept/cloud-inside-capture-r90-a.gif](concept/cloud-inside-capture-r90-a.gif) | Game capture: the ship inside the first spore cloud, no Rook (t=19–23) | chosen (round 90 L.2 ok) |
| [concept/horn-taken-capture-r90-a.gif](concept/horn-taken-capture-r90-a.gif) | Game capture: the call organ's roots cut, the horn loose and flown through, `CALL ORGAN TAKEN` (t=127–132.5) | chosen (round 90 L.3 ok) |
| [concept/bullets-veins-capture-r90-a.png](concept/bullets-veins-capture-r90-a.png) | Game capture: Vrell bullets over the reef's brightest veins, three crops at 1:1 | chosen (round 90 L.4 ok) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

The round's game captures (step C5) are the `*-capture-r90-a` rows above; how they were made: [concept/prompts.md](concept/prompts.md#game-captures-r90-a).

## Implementation

- [x] Level data `data.yaml` (M11's build; step C2): sections and speed, the eight organ groups as the `destroy-targets`
      primary, the `escapes: threadcrawler` secondary (`allowed: 1`; easy 2, hard 0), waves with their pits, ground
      targets, the clouds per difficulty, radio (the `mission-failed` cue with `{group}`), budget, threat profile,
      briefing, backdrop, music; `medium: vacuum`, the `beyond-gate` limits (`NO COVER`)
- [x] The spawning organ in the enemies' data (its own unit directory, step C2; a ground object with the Hive Node's spawn rules, not hardened, the
      cluster/pair alternation, the 120 px hold)
- [x] The failed primary: an organ leaving the screen alive fails at once (the built flow); the tracker's amber
      blink in the field's bottom third
- [x] The spawning pits (the 2 s tell, the rise, the 120 px hold) and the spore clouds (the slow, the dim, the veil,
      the burn by `area` and the Plasma Arc, the placement rules) as Vrell space's hazards (steps CD1 and C2:
      `ReefChecks`; the pits' and the clouds' looks are step C4's)
- [x] The call organ: four root triggers, the horn's lift, the fly-through pickup `CALL ORGAN TAKEN`, the tug's
      fallback; the Swarm Call unlocked by the win (steps CD1, C2 and C4)
- [x] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), ≥ 80 a minute (`DensityTest`), the quiet
      windows, ≥ 4 s of calm after the last enemy, one `medium` air hull at a time (step C2, with the denser mixed
      waves; easy's three quiet windows over 3 s pending the user's review in round 90)
- [x] `ORGANS n / 8` and `CRAWLERS n / 2` on the two-line tracker (seen in the game: steps C5 and CD5)
- [x] Radio voiced with the step-3 noise, every timed line at most 1 s late (`RadioTimelineLevel45Test`, with Rook and
      without); the Choir's line on the clean enemy channel
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`);
      `TraitCoverageTest` covering Level 45
- [x] Briefing: both pages, each one screen (`BriefingLayoutTest`), with their images
- [x] Typical haul within ±5 % of budget(45) = 13,740 with the level's `bounty_scale` (`TypicalHaulTest`)
- [x] Music and ambience: track 16 by section, the `vrell-hive` bed, the level's sounds (`Level45SoundsTest`; step C4:
      the bed is round 90's a until the user's pick)
- [x] Easy/hard variations; the autopilot (organs first, round the clouds) flies it on every difficulty with Rook and
      with `--escort none` and wins it (all eight organs; step C2, `Level45Test`: the generic play, 18 of 18 runs won
      with no organ away, the horn taken in each)
- [x] Production art and captures for the build's round (step ART-C; steps C5 and CD5's captures; round 90)

## Decisions

- 2026-11-04 (Act 7 design phase, step `m11-d-levels-2`; decided 2026-11-05, round 84): promoted from the
  act roster to a draft level document, hand tables only, no `data.yaml`. Ground and air, all from the front; six
  sections, 278 s at 100 px/s, 532 enemies (≈ 115 a minute); eight spawning organs as the `destroy-targets` primary
  without holds (one alone, then two a section, never two on the screen), the organ at 183 HP releasing a Whirl Seed
  cluster and a Skitter pair alternately every 3 s from 1.5 s on; 14 pit swarms; 10 spore clouds; three
  Threadcrawlers as the secondary (2 of 3); the reef's living turrets (Spine Turrets, Polyp Mortars) and three Brood
  Pods; the call organ on the spine as the Swarm Call's capture; the courier's cyst as the secret; the Choir's
  "*all we carry*" line; the radio's noise step 3; `bounty_scale` 0.30. No Mantis (its side entries break the
  front-only row).
- 2026-11-04 (step `m11-d-act`, decided 2026-11-05, round 84): the names adopted by the story step (the open question closed); the music and bed as D-audio's cues
  (`vrell-hive`; the tissue's sounds dry).
- 2026-11-05, round 84 (the user's page answers; decided as drafted unless stated, the alternatives rejected). The open items were:
  - **Q9** (round 84, Act 7's arsenal): drafted **b** (the call organ taken here, the Swarm Call from the L46 visit);
    **c** drops the capture (the horn and its window go; the spine keeps an organ); **a** changes nothing here.
  - **J13** (round 84, spore clouds): drafted as slowing and dimming, never hiding bullets; the sketch's "hide bullets"
    is dropped.
  - **J16** (round 84, the organs): drafted as eight, not hardened, Hive Node rules, failing at once; the alternative is
    Level 09's **holds** over each organ (the scroll easing to 40 px/s until it dies), if the build's autopilot or a
    playtest loses organs in the clouds; a softer one is `allowed: 1` (a missed organ breeds a rear Skitter stream).
- 2026-11-05 (user, round 84 closed): **A.5 b "reef polyp"** chosen for the spawning organ (a taller ribbed sac on nine roots, a ring of six pods round the iris); its colours come from the one Vrell palette (Q10 b), not the reef pink of the proposal; a "teal bulb" rejected (moved to `concept/rejected/`); **D.4 approved**; the organs' sounds are S.17 a, S.18 a, S.19 b, the pits' S.45 a and S.46 b, the call organ's S.47 b and the Swarm Call's S.48 b.
- 2026-11-05 (round 84 D.4, closed in step C2): the two open questions decided as drafted: **the EMP and the organs**:
  objects the EMP does not stun (their release clocks run; `emp: false`), not a 1.5 s stun as Vrell units; **the
  secondary**: the Threadcrawlers, 2 of 3 (`CRAWLERS n / 2`), not none.
- 2026-11-06 (M11 part C, step C2; pending the user's review in round 90): built from the hand tables. **The organ is a
  unit directory** ([spawning organ](../../../enemies/ground/spawning-organ/README.md), `counted: false`), its hold a
  wait (never the Hive Node's skip). Placements: the organs at x 240, 330, 150, 310, 170, 300, 150, 320; the pits at
  ART-C's places, but **the t=173 pit wave moves to t=178** (its pit at row 17070 lay in the gap between the reefs; P9
  now opens on the second reef's lip) and the Needlers and Motes after it 1.5 s and 0.5 s later; **the courier's cyst at
  t≈176** (from t≈172, the same gap); the Threadcrawlers cross **diagonally** (at 70 px/s on top of the 100 px/s scroll a
  crawler cannot cross the field level: one pass from a side edge near the top to the other side's lower half, ≈ 10 s on
  the screen); the first cloud at t=18 (the *Waves* table's t≈17 was the radio line's), the radio's "*Pink clouds*" now
  "*Violet clouds*" (Q10 b's one palette, as the briefing says). **The denser mixed waves** (the act's floor, the user's
  2026-11-05 answer): 61 waves of Chimera and Needler wings, Harrow streams, Stinger columns and Spore Bomber lines on
  the offbeats, the quiet windows kept; `bounty_scale` 0.17 (typical +2.0 %). The opening's radio lines cut to one page
each, Varga's t=213.5 line moved to t=215 and Okafor's t=244.5 to t=248 (the 1 s rule on the game's radio pages); Varga's briefing page shortened
to one screen ("And Lancer:" and "I want to hear what it sings." dropped). A pit's Skitter swarm rises as a cloud
  round its pit (the Skitter has no flock: a sim planner for a pit's flockless swarm). Measured gaps (`PENDING_LEAST`,
  `PENDING_PAUSES`): the beam era's small-unit life 0.69× Level 10's (the floor 0.9×), easy's three quiet windows over 3 s.
- 2026-11-06, round 90: F.1–F.3 accepted (the spawning organ's, the reef kit's and the call organ, roots and cyst's production art) and L.1–L.4 ok (the organ and a pit, a spore cloud burning, the horn taken, Vrell bullets over the veins). C.1 (a denser Level 45: 61 added waves, hard's 5.1 enemies on the screen and 46 shots a minute), C.2 (the small units' life 0.69× Level 10's against the rule's 0.9×), C.3 (a pit's units held 120 px from its centre; a pit still held off the bottom edge loses its wave) and C.5 (the Threadcrawlers cross the screen diagonally) accepted as built.
