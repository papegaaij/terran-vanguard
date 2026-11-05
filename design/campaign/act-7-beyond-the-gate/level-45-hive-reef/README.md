---
title: Level 45 – Hive Reef
design: approved
implementation: not-started
art: chosen
depends-on: [../../../world/vrell-space, ../../../enemies/ground/hive-node, ../../../enemies/ground/threadcrawler, ../../../enemies/air/whirl-seed, ../../../enemies/air/brood-pod, ../../../enemies/air/spore-bomber, ../../../enemies/ground/spine-turret, ../../../enemies/ground/polyp-mortar, ../../../player/generator, ../../../player/specials, ../../../player/wingmen, ../../../systems/retry, ../../../ui/hud, ../../act-2-homefront/level-09-arcology-fall]
updated: 2026-11-05
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
[Act 7 additions](../../../ui/hud/README.md#act-7-additions-draft). There is **no `data.yaml`**: the build turns the
hand tables into one; no number is measured.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft; D-story may reword them):

> **Commander Okafor:** "Past the gate there is a reef the size of a moon, and it is alive. Every Vrell ship we have
> fought was born somewhere like it. Eight spawning organs grow on your lane. Burn every one: one left alive breeds
> for their whole swarm. Rook flies your wing."
>
> **Dr. Varga:** "The reef breathes. Its pits glow before they open; the organs swell before they spit seeds. The violet
> clouds are spores: they slow you, and a blast burns them away. And Lancer: on the reef's spine grows a pale horn that
> calls the broods. Cut its roots and bring it home. I want to hear what it sings."

*Images* (production art in M11's build, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-45-hive-reef`: the fleet's scan of the reef from above, a violet and teal disc veined like a leaf;
  the wing's lane a dotted line across it, the eight organs ringed and numbered O1–O8, the spine and the living bridge
  marked, "SPORE DENSITY HIGH" hatched over the clouds.
- Varga's page `level-45-spawning-organ`: her field sketch in grey outline, twice: a spawning organ (a swollen sac on
  the plates, its seed pod drawn swelling, "0.5 s") and the call organ (a pale ribbed horn, its four roots marked
  "CUT HERE", the horn itself marked "DO NOT FIRE").

*Hangar teaser* (shop screen before L45; Rook speaks, not voiced):

> **Rook:** "A reef that breeds. I'm loading the Mortar."

The plan's L45 visit buys the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md) **L5** (the level's
`area`) and fits Rook's owned **Mortar** (the organs' `anti-ground`): 24.5 of 25 MW
([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The intel panel's `SPECIAL LIMITED`
names the Airstrike and the Orbital Lance if fitted (`NO COVER`, the
[Act 7 specials](../../../player/specials/README.md#act-7-specials-draft)); the plan's EMP Burst is unaffected and
stuns the reef's living turrets and crawlers (Vrell units).

## Threat profile

Hand-written (draft); directions are counted by each wave's `from` (a pit's swarm counts as `front`: the pits lie in
the field's top 60 % and rise ahead of the ship).

| Field | Value |
|---|---|
| Dominant layers | `ground` (the reef: the spawning organs, the pits, Threadcrawlers, Spine Turrets, Polyp Mortars), `air` (Skitter streams, Mote Swarms, Needlers, Stingers, Brood Pods, Whirl Seed clusters), `low-air` (Spore Bombers) |
| Attack directions | front 100 % |
| Density | 5 |
| Recommended traits | `anti-ground`, `area` (the roster's) |
| Hazards | Spore clouds (slow, never hide bullets); spawning pits (2 s tell); the organs' seed clusters |
| Boss / mid-boss | none |
| Special limits | `NO COVER` (the Airstrike, the Orbital Lance; J12), as the act's every level |
| Sensor-suite detail | **none**: Vrell space, the hive reef, `ground` + `air`, front, the objective `ORGANS 0 / 8` · **L1**: + density 5, the hazards "spore clouds" and "spawning pits", the secondary `CRAWLERS 0 / 2`, `SPECIAL LIMITED` (`NO COVER`) · **L2**: + Threadcrawler, Brood Pod, Spore Bomber, Spine Turret, Polyp Mortar, Needler, Stinger, Whirl Seed, Skitter and Mote portraits · **L3**: + the eight organs and the call organ on the threat map, `anti-ground` and `area` highlighted |

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

A ground object of this level (a row in the enemies' data in the build, no unit page: a single-level object, as
Level 39's cracking towers). It plays by the [Hive Node](../../../enemies/ground/hive-node/README.md)'s rules (its
iris tell, its release, its shut-while-close rule) **but is not hardened** (J16): every weapon that hits `ground`
hurts it, `anti-ground` does ×2, `area` blasts reach it whole.

| Field | Value |
|---|---|
| Layer, tier | `ground`, `medium` |
| Size | 80×80 px, hitbox 60×60 (a swollen sac rooted in the plates, veins running into it, a seed pod on its crown) |
| HP | **183** (easy 137 / hard 238 by the global multipliers): the Hive Node's 64 × the act HP factor of a returning `medium` unit from its first level (L09, reference 63) at L45 (reference 180): ×2.857; ≈ 1.0 s at the reference |
| Release | while on the screen: its first release **1.5 s** after its centre crosses the top edge, then every **3.0 s**, alternating a [Whirl Seed](../../../enemies/air/whirl-seed/README.md#act-7-the-hive-reefs-organs-draft) **cluster** (6; easy 5, hard 8; the seed pod's 0.5 s swell, the Whirl Seed's Act 7 section) and a **Skitter pair** (the Hive Node's spawn: 2, hard 3, its 0.5 s iris); so at most two releases before it leaves; it **holds** a release while the ship is within **120 px** (the reef's rule) |
| Credits (Act 1 terms) | 45 (the Hive Node's) |
| Death | Vrell space's vacuum ([media](../../../world/vrell-space/README.md#media-per-level)): the sac tears and collapses, a short flash and a ring of ichor that spreads and fades; the torn husk stays on the plates, its veins going dark outward |
| Sound | a dry tearing at the release and the death (no pop; D-audio) |

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

| Section | t (s) | Scroll (px) | Layers and content | Purpose |
|---|---|---|---|---|
| 1. The Reef Rim | 0–40 | 0–4,000 | `ground`: chitin plates rising out of the nebula, the first pit (opens t=12), organ **O1** (t≈28); `low-air`: floating spore sacs; `deep`: the nebula, the next reefs far off | **The pit taught** alone, **the first cloud** (t≈18) alone, **the first organ** alone |
| 2. The Brood Fields | 40–108 | 4,000–10,800 | `ground`: rows of pits, organs **O2** (t≈58) and **O3** (t≈80), the first Threadcrawler's pass (t≈90), Spine Turrets, a Polyp Mortar pair | The reef at full breath: pits, Brood Pods, clouds |
| 3. The Spine | 108–160 | 10,800–16,000 | `ground`: the reef's ridge, a raised backbone of plates; organ **O4** (t≈112), **the call organ** (t≈127), organ **O5** (t≈152) | **The capture** in a quiet window (t=126–138) |
| 4. The Living Bridge | 160–212 | 16,000–21,200 | `low-air`: a living bridge of tendon and chitin arching over the field (t≈160–175) to the next reef; `ground`: the second reef's rim, organ **O6** (t≈192), the second Threadcrawler (t≈176), the courier's cyst (t≈172) | Spore Bombers under the bridge; the clouds at their densest |
| 5. The Brood Heart | 212–268 | 21,200–26,800 | `ground`: a ring of pits round a deep brood well, organs **O7** (t≈226) and **O8** (t≈252), the third Threadcrawler (t≈236) | The hardest stretch; the last organ |
| 6. The Far Rim | 268–278 | 26,800–27,800 | `ground`: the reef's edge falling away to the nebula; `deep`: the first dark hulls of the dead fleet far ahead (Level 46) | ≥ 4 s of calm (the last wave gone by ≈ t=271); level end at t=278 |

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

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
  counted (Level 39's rule). Easy ≈ 440, hard ≈ 610 by the formation lever. A wave about every 3 s; the floor comes
  from the Needlers' thorns, the Stingers' fans, the turrets' and mortars' fire, the Threadcrawlers' pores, the Brood
  Pods' bursts and the seed clusters: expected to meet ≤ 3 pauses over 2 s, ≥ 2.5 on the screen and ≥ 10 shots a
  minute (`PacingTest`, `ActFloorTest`), **to be confirmed with `levelReport`**.
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

Draft wave table (every wave `front` or `pit`):

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 6 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front |  |
| 12 | 1 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | pit | **The first pit, alone**: its 2 s tell from t=10 (the teaching); no wave enters t=8–20 but this one |
| 21 | 1 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | After the first spore cloud (t≈17) drifts in alone |
| 24 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | Gone before the first organ (t=28); no wave enters t=26–33 |
| 34 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 37 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front |  |
| 40 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | The brood fields |
| 43 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | pit |  |
| 46 | 2 | single | [Brood Pod](../../../enemies/air/brood-pod/README.md) | 1 | front | Bursts into 6 Skitters 8 s after entering, or when killed |
| 50 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 53 | 2 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 56 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | pit | Before organ O2 (t=58) |
| 61 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 64 | 2 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 67 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 70 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | pit |  |
| 73 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front |  |
| 76 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | Before organ O3 (t=80) |
| 83 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 86 | 2 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Thin while the first Threadcrawler (t=90) crosses |
| 93 | 2 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | pit |  |
| 96 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 99 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front |  |
| 102 | 2 | single | [Brood Pod](../../../enemies/air/brood-pod/README.md) | 1 | front | Alone of its tier on the screen |
| 105 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 109 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | The spine; organ O4 at t=112 |
| 114 | 3 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 117 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | pit |  |
| 120 | 3 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | `low-air`, over the spine |
| 123 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | The last before the call organ: no wave enters t=126–138 |
| 139 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front | After the call organ |
| 142 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | pit |  |
| 145 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 148 | 3 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Before organ O5 (t=152) |
| 155 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 158 | 3 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | pit |  |
| 161 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | The living bridge overhead |
| 164 | 4 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 167 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 170 | 4 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 173 | 4 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | pit | Before the second Threadcrawler (t=176) |
| 179 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front |  |
| 182 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 185 | 4 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | `low-air` |
| 188 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | Before organ O6 (t=192) |
| 195 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | pit |  |
| 198 | 4 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 201 | 4 | single | [Brood Pod](../../../enemies/air/brood-pod/README.md) | 1 | front |  |
| 204 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 207 | 4 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 210 | 4 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | pit |  |
| 213 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | The brood heart |
| 216 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 219 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front |  |
| 222 | 5 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | pit | Before organ O7 (t=226) |
| 229 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 232 | 5 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Before the third Threadcrawler (t=236) |
| 239 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | pit |  |
| 242 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 6 | front |  |
| 245 | 5 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 2 | front | `low-air` |
| 248 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | Before organ O8 (t=252) |
| 255 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front |  |
| 258 | 5 | swarm | [Skitter](../../../enemies/air/skitter/README.md) | 7 | pit | The last pit |
| 261 | 5 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front |  |
| 264 | 5 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | **The last wave**; gone by ≈ t=271, the calm to the end at t=278 |

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
| 4 | The courier's cyst (30 HP, t≈172) | The secret, see below |
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

- **The courier's cyst** (the secret, section 4, t≈172, x≈90, on the second reef's rim): a gold Ascendancy courier pod
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

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, Steadfast. A living reef the size of a moon. It breeds everything they fly." |
| t=9.6 | Varga | "The pits are its mouths. They glow, then open. Don't sit over one." |
| t=17 | Varga | "Pink clouds are spores. They slow you down. A blast burns them away." |
| t=24.6 | Okafor | "Swollen sac ahead: a spawning organ. Eight on our lane. Burn them all." |
| t=33 | Okafor | "One that gets behind you breeds for the fleet. Let none past, Lancer." |
| The first organ destroyed (event, bark, `requires: escort`) | Rook | "One down. It screamed. I didn't like that." |
| t=87 | Varga | "Threadcrawler on the plates. Take it before it crosses the reef." |
| t=121.5 | Varga | "The pale horn on the spine calls their broods. Cut its roots and bring it out. Don't burn it." |
| The call organ collected (event) | Varga | "It's still singing. Quietly. I want to know what it says." |
| The call organ missed (event, it leaves the screen) | Okafor | "The tug has the horn. Keep your eyes on the organs." |
| The secret found (event, bark, `requires: escort`) | Rook | "Gold under the chitin. One of Vorne's. The reef ate it." |
| t=160 | Okafor | "Living bridge overhead. The next reef is just as hungry." |
| t=208 (enemy channel) | the Choir | "*…do not burn the brood… it is all we carry…*" |
| t=213.5 | Varga | "Carry? Carry where? Lancer, it's never said a thing like that." |
| t=244.5 | Okafor | "Last two organs. Then this reef is only meat." |
| Secondary met (`secondary-objective`, the second crawler down) | Varga | "Two crawlers down. The plates are quieter." |
| Secondary failed (the second crawler off the reef) | Varga | "Another crawler got away. It will find a reef of its own." |
| Level end | Okafor | "The reef's gone quiet. A graveyard of their ships ahead. Varga wants a look." |
| `mission-failed` | Okafor | "{group} is still breeding behind you. That brood comes for the fleet. Pull back, Lancer." |

Timeline check (text, by the rule above): the timed lines close at ≈ 9.2, 16.6, 24.2, 32.4, 40.3, 93.8, 130.8, 166.2,
213.1, 220.2 and 249.8; nothing overlaps. Varga's pit line closes as the first pit opens (tell from t=10), her cloud
line as the first cloud enters (t≈18); Okafor names O1 before it enters (t≈28) and states the failure rule while it is
on the screen. The crawler line comes before T1 (t≈90). The horn line closes as the horn's roots are on the screen
(t≈127). The Choir's line and Varga's answer (the Act 7 fear, and the seed of Level 46's "*singing away from
something*") sit in section 4's steady stretch. The event lines wait up to 6 s for a free slot.

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

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 195 × 5 (161 in waves, 16 from the organs, 18 from the Brood Pods) + Mote Swarm 160 × 2 + Needler 63 × 12 + Stinger 32 × 15 + Brood Pod 3 × 20 + Spore Bomber 6 × 25 + Whirl Seed 48 × 3 (2,885) | 14,520 | 8,712 |
| Ground targets: spawning organ 8 × 45 + Threadcrawler 3 × 56 + Spine Turret 14 × 12 + Polyp Mortar 8 × 15 (816) | 4,107 | 3,286 |
| Secret: the courier's cyst (hidden crate) | 1,678 | 839 |
| Secondary: `CRAWLERS` 2 of 3 | 1,678 | 839 |
| **Total** (bounty scale 0.30) | **21,983** | **13,676** |
| Budget(n) = the typical haul's target; typical −0.5 %, perfect 1.60 × budget | | 13,740 |

The scale sits a step above L43's draft 0.27: the reef pays its ground (the organs, a fifth of the haul, are all
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

| File | What | Status |
|---|---|---|
| [concept/spawning-organ-r84-b.png](concept/spawning-organ-r84-b.png) | Spawning organ b, "reef polyp": a taller ribbed sac on nine roots, the same teal veins, a ring of six seed pods round a crown iris; the same states and check; its colours are to come from the one Vrell palette (Q10 b), not the reef pink of the proposal | chosen (round 84 A.5 b) |
| [concept/spawning-organ-r84-b.gif](concept/spawning-organ-r84-b.gif) | The same scene with b | chosen (round 84 A.5 b) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

Still needed (round 84's other proposals, then the M11 part's production art):

- the hive-reef backdrop kit in Q10 b's palette and light: chitin plates, glowing veins, the spine, the brood well, the reef's rim
  falling to the nebula, the living bridge on `low-air`, floating spore sacs; the pits (closed, the tell, open,
  closing);
- the **call organ** (rooted, the roots cut, lifting free), the courier's cyst;
- the spore cloud (drifting, the slow's tint, burning away: smooth, ≥ 32 steps, fading to 0 inside its canvas);
- the two briefing images;
- the round's captures: an organ releasing a cluster beside an opening pit, a cloud burning under a Mortar burst, the
  call organ taken (`CALL ORGAN TAKEN`), readability crops of the Vrell bullets over the reef's brightest veins at 1:1.

## Implementation

- [ ] Level data `data.yaml` (M11's build): sections and speed, the eight organ groups as the `destroy-targets`
      primary, the `escapes: threadcrawler` secondary (`allowed: 1`; easy 2, hard 0), waves with their pits, ground
      targets, the clouds per difficulty, radio (the `mission-failed` cue with `{group}`), budget, threat profile,
      briefing, backdrop, music; `medium: vacuum`, the `beyond-gate` limits (`NO COVER`)
- [ ] The spawning organ in the enemies' data (a ground object with the Hive Node's spawn rules, not hardened, the
      cluster/pair alternation, the 120 px hold)
- [ ] The failed primary: an organ leaving the screen alive fails at once (the built flow); the tracker's amber
      blink in the field's bottom third
- [ ] The spawning pits (the 2 s tell, the rise, the 120 px hold) and the spore clouds (the slow, the dim, the veil,
      the burn by `area` and the Plasma Arc, the placement rules) as Vrell space's hazards
- [ ] The call organ: four root triggers, the horn's lift, the fly-through pickup `CALL ORGAN TAKEN`, the tug's
      fallback; the Swarm Call unlocked by the win
- [ ] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), ≥ 80 a minute (`DensityTest`), the quiet
      windows, ≥ 4 s of calm after the last enemy, one `medium` air hull at a time
- [ ] `ORGANS n / 8` and `CRAWLERS n / 2` on the two-line tracker
- [ ] Radio voiced with the step-3 noise, every timed line at most 1 s late (`RadioTimelineLevel45Test`, with Rook and
      without); the Choir's line on the clean enemy channel
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`);
      `TraitCoverageTest` covering Level 45
- [ ] Briefing: both pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(45) = 13,740 with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music and ambience: track 16 by section, the `vrell-hive` bed, the level's sounds (`Level45SoundsTest`)
- [ ] Easy/hard variations; the autopilot (organs first, round the clouds) flies it on every difficulty with Rook and
      with `--escort none` and wins it (all eight organs)
- [ ] Production art and captures for the build's round

## Open questions

- **The EMP and the organs**: drafted as objects the EMP does not stun (their release clocks run); the alternative is
  to stun them 1.5 s as Vrell units (cheaper in play, one more rule).
- **The secondary**: drafted as the Threadcrawlers (2 of 3); the alternative is none (the organs are the count).

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
