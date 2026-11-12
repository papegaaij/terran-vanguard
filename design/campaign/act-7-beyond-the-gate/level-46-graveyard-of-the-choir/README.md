---
title: Level 46 – Graveyard of the Choir
design: approved
implementation: done
art: chosen
depends-on: [../../../world/vrell-space, ../../../enemies/space/husk, ../../../enemies/space/leviathan, ../../../enemies/air/wraith, ../../../enemies/air/lamprey, ../../../enemies/air/mantis, ../../../enemies/air/chimera, ../../../enemies/air/harrow, ../../../player/generator, ../../../player/specials, ../../../player/wingmen, ../../../story, ../../act-5-the-belt/level-34-salvage-run]
updated: 2026-11-12
---

# Level 46 – Graveyard of the Choir

## Summary

The act's **breather**: beyond the reefs the wing crosses a field of dead Vrell ships drifting before a withered
world, not shot apart but **hollowed from the inside**, pale crystal along their cracks. Nobody knows what did it.
The dead hulls that still drift on the play plane are [Husk](../../../enemies/space/husk/README.md)s, harmless until
hit; the Vrell's remnants come from every side. In the middle of the level comes **the quiet** (Q4 a): about 24 s in
which nothing attacks, the music is out, a **dying [Leviathan](../../../enemies/space/leviathan/README.md#act-7-the-dying-leviathan-level-46-draft)**
drifts overhead, and a **starless patch** that never moves sits on the sky; then the Husks crack. Salvage everywhere:
seed caskets (the secondary), the Leviathan's parts, two secrets, and a **living Vrell core** cut from the one warm
hull (Q9 b: the Mk VI "Choir Core"). Space only, all directions; 268 s, ≈ 78 enemies a minute at medium.

**Status: approved** (Act 7 design phase, step `m11-d-levels-2`, 2026-11-04; the user approved it on 2026-11-05, round 84).
Written from the act roster, the gap analysis's recommended answers (**Q4 a**, **Q9 b**, **Q10 a**) (the user changed Q10 to **b** in round 84; the looks below follow b) and judgement calls
(J3, **J6**, J8, J12, J15) and the earlier steps' drafts, which are the source of truth for their numbers:
[Vrell space](../../../world/vrell-space/README.md)'s dead fleet, mood and hazards (*The quiet*, *The dying
Leviathan*, *Hollowed hulls*), the [Husk](../../../enemies/space/husk/README.md), the Leviathan's
[Act 7 section](../../../enemies/space/leviathan/README.md#act-7-the-dying-leviathan-level-46-draft), the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft),
[Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft) and the
[Act 7 specials](../../../player/specials/README.md#act-7-specials-draft). **Built** in M11 part D (step D2): the
[data.yaml](data.yaml) holds the level (its tables below are rendered from it), measured with `levelReport`.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft; D-story may reword them):

<!-- data: briefing -->
> **Commander Okafor:** "Beyond the reefs our scans show a fleet. Hundreds of Vrell hulls, and
> none of them moving. Cross it and tell me what killed them. Take what you can on the way: we
> are a long way from Callisto, and nothing out here sells us fuel."
>
> **Dr. Varga:** "The hulls are dead, but some still hold a charge. Hit one and it may answer.
> Look for one that is still warm, Lancer. A living core would give the fleet a generator like
> nothing we can build."
<!-- /data -->

*Images* ([briefing images](../../../ui/briefing/README.md); production art by step ART-D, round 91):

- Okafor's page `level-46-dead-fleet`: the fleet's long-range scan, hundreds of hull echoes in a loose cloud before a
  grey world's disc, "NO DRIVE SIGNATURES" stamped across it; the wing's lane a dotted line through it; one patch of
  the starfield in the corner left blank on the scan, unlabelled.
- Varga's page `level-46-hollow-hull`: her thermal scan of one dead hull in grey outline, the plates bent outward round
  the holes, "INTERIOR: EMPTY" in her hand; beside it one hull with a single warm spot at its heart, ringed.

*Hangar teaser* (shop screen before L46; Rook speaks, not voiced):

<!-- data: teaser -->
> **Rook:** "A graveyard. Varga says it's quiet. I'm bringing the autocannon anyway."
<!-- /data -->

The plan's L46 visit buys the **[Vrell Swarm Call](../../../player/specials/README.md#act-7-specials-draft)** (10 000,
new in the shop: the call organ taken in Level 45) and fits it in place of the EMP Burst, and gives Rook his Autocannon
again; 24.5 of 25 MW ([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The intel panel's
`SPECIAL LIMITED` names the Airstrike and the Orbital Lance if fitted (`NO COVER`) and the Bomb Rack (`NO GROUND`).

## Threat profile

Rendered from the data; directions are counted by each wave's `from` (the caskets' too: objects drifting in; the
Husks' and caskets' drifts from the front count as `front`).

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `space` (the Husks, Chimera and Needler wings, Harrow and Skitter streams, Stinger lines and columns, Mote Swarms, Lampreys, Mantis pincers), `high-air` (Wraiths; the dying Leviathan, scenery); **no ground** (`NO GROUND`: the dead fleet's hulks are `deep` and `low-air` scenery) |
| Attack directions | front 86% · sides 9% · rear 5% by the waves' data (the seed caskets drift in too, objects, not counted) |
| Density | 3 (the breather) |
| Recommended traits | `homing`, `piercing` |
| Hazards | Derelicts that answer fire (the Husks' crack fans and death rings); the quiet (scripted, harmless) |
| Boss / mid-boss | none |
| Special limits | `NO COVER` (the Airstrike, the Orbital Lance; J12); `NO GROUND` for the Bomb Rack and every bomb delivery |
| Sensor-suite detail | **none**: Vrell space, the dead fleet, `space`, all directions · **L1**: + density 3, the hazard "derelicts that answer fire", the secondary `CASKETS 0 / 5`, `SPECIAL LIMITED` (`NO COVER`, `NO GROUND`) · **L2**: + Husk (new), Wraith, Lamprey, Mantis, Chimera, Harrow, Needler, Stinger, Skitter and Mote portraits · **L3**: + `homing` and `piercing` highlighted, the wave strip marking the quiet |
<!-- /data -->

*Varga's intel line* per sensor-suite level (must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "A dead fleet, Lancer. Hundreds of hulls, none moving." |
| L1 | "Sparse, but from every side. Salvage everywhere." |
| L2 | "New: Husks, dead hulls that answer fire. Wraiths too." |
| L3 | "Homing for the Wraiths; piercing for drifting hulls." |

**Trait coverage** (`TraitCoverageTest`, the lesson): `homing` and `piercing` are covered by one loadout of what is in
the shop at the L46 visit within the plan's 20 MW for the weapons: the
[Lance Laser](../../../player/weapons/lance-laser/README.md) (front, `piercing`, 4 MW at L1) with the
[Micro-missile Pod](../../../player/weapons/micro-missile-pod/README.md) (wing, `homing`, 1 MW), or with the
[Swarm Tail](../../../player/weapons/swarm-tail/README.md) in the rear. The plan's own fit covers `homing` (the pod and
the Swarm Tail) but not `piercing` (the Ion Beam is `beam`): the launch warning names it, as for Level 39's
`anti-ground`; the breather's slow Husks forgive it.

## Objective

- **Primary** `reach-end`: cross the dead fleet (`CROSS THE DEAD FLEET`).
- **Secondary** `escapes` (the seed caskets): break open at least **5 of the 6** seed caskets before they drift off
  the screen (`escapes: seed-casket`, label `CASKETS`, `allowed: 1`; easy 4 of 6, hard 6 of 6), met at the fifth
  (`DONE`), failed by the second casket away; the tracker counts to its target, **`CASKETS n / 5`** (Level 34's
  "strip the graveyard", the built form). Pays **100** in Act 1 terms (**1,678** in Act 7); the typical haul counts
  50 %.
- **The Vrell core** (Q9 b) is a story pickup, not an objective: see *The warm hull*.

### The seed casket

A play-plane object of this level (a row in the enemies' data in the build, no unit page; Level 43's Helix pod's
kind): a Vrell seed casket, a chitin capsule ≈ 40×28 px drifting slowly (15–25 px/s) out of a breached hull, dull
grey-brown like the dead hulls but with a faint teal seam (the one thing in the wreckage worth taking). **20 HP**
(easy 15 / hard 26); no contact damage; homing shots and Rook never pick it (Level 34's containers' rule). Broken, it
frees a **medium salvage** (50 in Act 1 terms) and counts for the secondary. Six: **C1** t≈30, **C2** t≈66,
**C3** t≈100, **C4** t≈160, **C5** t≈205, **C6** t≈238, each on the screen ≥ 8 s, none in the quiet.

### The warm hull

The Q9 b salvage (the [Mk VI "Choir Core"](../../../player/generator/README.md#act-7-hand-plan-draft)'s story): at
**t≈184** a dead Vrell capital hull drifts in on `low-air` (scenery, ≈ 300 px long, its prow from the top-left), the
one hull **not hollowed**: it is scarred by weapons and died of its wounds like the Leviathan, and in its open rib
cage a **living core** (≈ 48 px) still glows faint violet-gold, the only warm light in the level. Four **sinews** hold
it (`low-air` triggers, **15 HP** each, no bounty; every weapon hits `low-air`). With the four cut the core rises to
the play plane over 2 s (t≈188–191 for a player who shoots at once) and is **collected by flying through it**
(`VRELL CORE SALVAGED` on the HUD's event line). Missed, the *Steadfast*'s salvage tug picks it up (Okafor's event
line; Level 34's rule), so it is never lost: **the Mk VI "Choir Core" is in the shop from the L47 visit by winning the
level** (part A writes `available: L47`). No wave enters from t=182 to t=192; it pays no credits.

## Layout

Scroll **90 px/s** (script time; no ground, so the speed only moves the `deep` and `low-air` scenery and the warm
hull's and the Helix wreck's pinned objects; the caskets and the Husks drift on the play plane at their own speeds). Setting: [Vrell space](../../../world/vrell-space/README.md#sub-locations-per-level)'s **dead fleet before a
dead world**, `vacuum` (J12), **no ground layer**. **Light** ([mood](../../../world/vrell-space/README.md#light-per-level-q10-b),
Q10 b): **one cold white-violet light from the top-left** on the dead fleet's hulls (not Vrell: their own colours, lit by the Vrell light) and a withered world in the palette's darkest violet; pale crystal at the hulls' cracks; the
Husks a step darker and higher in contrast than the paler, flatter scenery hulks, so the living threat reads. Motion
budget: very low: the hulks turn slowly, no glow pulses but the warm core's; the strongest motion is the Husks' crack
and the Leviathan's slow pass. Every haze and glow smooth (≥ 32 steps, no ordered dither, fading to 0 inside its
sprite's canvas).

**The starless patch** (Q4 a, the [world's rule](../../../world/vrell-space/README.md#hazards--set-pieces)): from t=0
to the end, a soft-edged region about 120 px across (a quarter of the field's width) centred at **x≈360, 150 px below
the top edge**, on `deep`, where no star and no nebula shows; **the only thing in the level that does not scroll**: the
stars drift into it and go out. It never moves, grows or shows anything; nothing in the level points at it, and no
line names it (the [Silence's rule](../../../story/README.md): hinted, never shown).

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Outer Wrecks | 0–48 | 0–4,320 | 90 | clear | `low-air`: scattered dead hulls, hollowed, crystal at the cracks; the first Husk (t=14), casket C1. `deep`: the withered world small and far, the starless patch. | The Husk taught alone; the patch in place from the start. |
| 2. The Graveyard Lanes | 48–118 | 4,320–10,620 | 90 | clear | `low-air`: hulks in loose lanes, some broken in two; the Helix wreck (secret, t≈88); caskets C2, C3. | Lampreys from the sides, Wraiths from behind; the breather's steady stretch. |
| 3. The Quiet | 118–144 | 10,620–12,960 | 90 | clear | `low-air`: the densest hulks, every one torn open from inside. `high-air`: **the dying Leviathan** (t=122–142); five Husks drifting. | **The quiet** (Q4 a): nothing attacks; the music out; Varga's one line. |
| 4. The Remnants | 144–214 | 12,960–19,260 | 90 | clear | `low-air`: the lanes break up; **the warm hull** (t≈184); caskets C4, C5. | The Husks crack; the remnants from every side; **the core** in a quiet window (t=182–192). |
| 5. The Dead World | 214–268 | 19,260–24,120 | 90 | clear | `deep`: the withered grey world fills the lower sky, its surface cracked and pale. `low-air`: the last hulks thin out; Vorne's courier (secret, t≈228); casket C6. | The last waves; ≥ 4 s of calm (the last wave gone by ≈ t=262); level end at t=268. |
<!-- /data -->

**The quiet** (t=120–144; Q4 a; the [Husk](../../../enemies/space/husk/README.md#behaviour)'s *The quiet*):

- The last wave before it (t=109) is gone by ≈ t=118; from t=120 to t=144 no wave enters and nothing fires unless the
  player fires first. Five Husks drift in late (three from the left at t=132, two from the right at t=132.5, 0.5 s apart, so all five
  are on the screen at the cue at any drift speed: round 91 C.7, step D6; hulls
  ≥ 80 px apart on lines that never meet); a Husk the player shoots cracks and answers as always. Rook's pick skips
  them until the player hits one ([wingmen](../../../player/wingmen/README.md#rook-in-act-7-draft)).
- The music drops out at t=120 for the `dead-fleet` bed alone; the sounds play as everywhere.
- **The dying Leviathan** (**J6**; its [Act 7 section](../../../enemies/space/leviathan/README.md#act-7-the-dying-leviathan-level-46-draft)):
  it enters the top-left corner at **t=122** on its first pass's diagonal, on `high-air` at 18 px/s, and leaves the
  bottom-right by **≈ t=142**; no attacks, no contact, scenery for the counts. Its parts pay salvage to the beam and
  homing shots (the level's `homing` trait reaches it); a beam that crosses a Husk on its way hurts the Husk first, so
  taking the Leviathan's salvage can wake the quiet: the player's choice.
- Varga's one line at t=126 (the hollowed hulls); nobody else speaks.
- **The end of the quiet** at t=144: every Husk still on the screen cracks **on cue** in turn, 0.6 s apart, the
  farthest from the ship first (the Husk's level cue, its 0.5 s tell each), so no two fans fly side by side (the
  Husk's 120 px rule); Okafor's line; the remnants' first wave at t=147.

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). Built and measured in step D2 (`levelReport`, the plan's fit with Rook).

- **HP** from the proposed Act 7 reference DPS (J11: **184** at L46,
  [Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)): the Husk new here (**150**,
  [its stat block](../../../enemies/space/husk/README.md#stat-block)); the returning `medium` units with the act HP
  factor from their first level: the **Mantis** 26 × 184/52 = **92**, the **Wraith** 16 × 184/66 = **45**; the
  `small` units with the [beam era](../../../enemies/README.md#balancing-basis)'s ×2 as built (Chimera 40, Harrow 16,
  Lamprey 6); the Skitters and Motes `tiny`. No unit carries a hex shield (J4). The Leviathan's parts are its Act 7
  section's (scenery).
- **The Vrell's remnants**: the dead fleet's survivors are the Vrell's own and their hybrids (Chimeras, Harrows), as
  at the gate; no Ascendancy unit flies (Vorne's courier is a wreck).
- **Density and pacing** (the breather: the act's minimum 80 a minute may dip 20 % to **64**): **347 enemies** in 64
  waves over 268 s, **≈ 78 a minute** at medium (easy ≈ 290, hard ≈ 400 by the formation lever); the caskets are
  objects and the Leviathan scenery, neither counted. A wave about every 3.5 s outside the quiet. The floor comes from
  the Chimeras' aimed pairs, the Harrows' rings, the Wraiths' bursts, the Mantises' sweeps and the Husks' fans and
  rings; during the quiet the five drifting Husks keep the screen from being empty (`PacingTest` counts them).
  Expected over the level: ≤ 3 pauses over 2 s, ≥ 2.5 on the screen, ≥ 10 shots a minute (the quiet's 24 s without a
  shot is inside a level-wide measure, so **no shots-floor exception is needed**; if the build measures under, the
  breather's exception is the fallback), **to be confirmed with `levelReport`**. **Measured** (step D2, pending the
  user's review in round 91): the approved waves alone gave 1.8 on the screen at medium and 14 pauses over 2 s, so step
  D2 added the act's denser mixed waves (Needler and Chimera wings, Stinger lines and columns, Harrow streams; Mote
  Swarms of 10, Skitter streams of 8): **546 counted units at medium** (easy 431, hard 661), ≈ 122 a minute, **2.64 on
  the screen and 29 shots a minute** (hard 3.7 and 44), the three quiet windows the only pauses over 2 s; no
  shots-floor exception (see *Decisions*).
- **Directions** (≥ 60 % front by wave count): front 43, sides 14 (Lamprey and Mantis pincers, Husks drifting in from
  a side), rear 7 (Wraith rear ambushes, Mote swarms from the bottom) of 64: **67 % front**. Husks enter unwarned from
  any edge (their document: scenery until hit); every other side or rear entry gets the built edge warning.
- **Quiet windows**: no wave enters t=12–19 but the first Husk (t=14, alone), t=110–147 but the quiet's Husks (t=132,
  132.5), or t=182–192 (the warm hull).
- **Limits and spacing** (the Act 5 lesson): at most 6 Husks on the screen (a Husk wave waits while 4 drift); Husks on
  drift lines ≥ 80 px apart that never meet, never over another `medium` hull; the Mantis pincers (t=167, 219) hover in
  their 40 px edge bands while that stretch's Husks drift through the field's middle third; the Wraiths' rear lanes
  avoid the Husks' drift lines; never a Husk's crack beside another's fan in flight (cracked hulls ≥ 120 px apart, or
  the quiet's cue in turn).

The wave table (rendered from the data; the Husks and the caskets drift on their `lines`, unwarned):

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 6 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front (alternating edges) | |
| 9 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 11.5 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 14 | 1 | single | [Husk](../../../enemies/space/husk/README.md) | 1 | right side | **The first Husk**, alone, drifting across: harmless until shot; no wave enters t=12–19 |
| 20 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 23 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 25 | 1 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 27 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 28.5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 30 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front (alternating edges) | |
| 30 | 1 | single | [Seed casket](../../../enemies/space/seed-casket/README.md) | 1 | front | Casket **C1**, out of a breached hull down to the right edge |
| 32 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 34 | 1 | swarm | [Husk](../../../enemies/space/husk/README.md) | 3 | left side | Crossing drifts, hulls ≥ 80 px apart, below the first Husk's line (step D2) |
| 36 | 1 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 38 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 40 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 42 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 45 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 47 | 1 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 49 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | left side | The lanes: the pincer's left half (3 a side) |
| 49 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | right side | The pincer's right half |
| 51 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 53 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 56 | 2 | swarm | [Husk](../../../enemies/space/husk/README.md) | 4 | front | Two pairs diverging from the top to the side edges above the Wraiths' hover band (step D2) |
| 60 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front (alternating edges) | |
| 63 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 66 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 66 | 2 | single | [Seed casket](../../../enemies/space/seed-casket/README.md) | 1 | front | Casket **C2**, down to the left edge |
| 68.5 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 70 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | From behind: `homing` or the beam reach them on `high-air` |
| 72 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 73.5 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 74 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 75.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 77 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 78.5 | 2 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 80 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front (alternating edges) | |
| 82 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 84 | 2 | swarm | [Husk](../../../enemies/space/husk/README.md) | 3 | right side | |
| 86 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 88 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | The Helix wreck (secret, t≈88) |
| 91 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | left side | |
| 91 | 2 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | right side | |
| 93 | 2 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 95 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 96.5 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 98 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 99.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 100 | 2 | single | [Seed casket](../../../enemies/space/seed-casket/README.md) | 1 | front | Casket **C3**, gone before the quiet |
| 102 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | |
| 104 | 2 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 106 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 107.5 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 109 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front (alternating edges) | **The last wave before the quiet**: gone by ≈ t=118 |
| 122–142 | 3 | solo set piece | [Leviathan](../../../enemies/space/leviathan/README.md) (the dying pass) | 1 | front (top-left, diagonal) | `high-air`, scenery: no attacks, no bounty; salvage per part |
| 132 | 3 | swarm | [Husk](../../../enemies/space/husk/README.md) | 3 | left side | **The quiet** (t=120–144): drifting, harmless unless shot; step D6 (round 91 C.7): in at t=132–133, 11–12 s before the crack cue, so at any drift (20–40 px/s) every one is wholly on the screen at t=144 |
| 132.5 | 3 | swarm | [Husk](../../../enemies/space/husk/README.md) | 2 | right side | The quiet; the Leviathan above (t=122–142); on the t=132 Husks' slope, 82 px between; step D6: in at t=132.5–133, on the screen at the cue |
| 145.5 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | rear | Step D2: t=145.5, not 148: the remnants from behind under Okafor's "Contacts on every side!" (t=144.5), so the cracked Husks' deaths leave no pause |
| 147 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | left side | **The remnants**: the quiet's Husks cracked in turn at t=144–146.4 |
| 147 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | right side | |
| 149.5 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 151 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 152 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Step D2: t=152, not 157: out up their side lanes before the t=167 Mantises hover in their edge bands |
| 154 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 158.5 | 4 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 160 | 4 | swarm | [Husk](../../../enemies/space/husk/README.md) | 3 | front | Down the field's middle third (the Mantises' edge bands clear) |
| 160 | 4 | single | [Seed casket](../../../enemies/space/seed-casket/README.md) | 1 | front | Casket **C4** |
| 162 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 164 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front (alternating edges) | |
| 167 | 4 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | One a side, hovering in its edge band; no Husk on its half |
| 169.5 | 4 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 171 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 173 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 174 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 176 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 177 | 4 | swarm | [Husk](../../../enemies/space/husk/README.md) | 3 | left side | |
| 180 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | The last before the warm hull: no wave enters t=182–192 |
| 193 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | After the core |
| 195 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 196 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | left side | |
| 196 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | right side | |
| 199 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 200.5 | 4 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 202 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | |
| 203.5 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 205 | 4 | single | [Seed casket](../../../enemies/space/seed-casket/README.md) | 1 | front | Casket **C5** |
| 206 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front (alternating edges) | |
| 209 | 4 | swarm | [Husk](../../../enemies/space/husk/README.md) | 3 | front | Step D2: from the front down the middle third, not from the right (the README's): across the field they met the t=202 Wraiths' side lane and the t=219 Mantises' edge bands |
| 211 | 4 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 212 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 216 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | The dead world |
| 219 | 5 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | |
| 221 | 5 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 223 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | front | |
| 224 | 5 | column | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 226 | 5 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front (alternating edges) | Vorne's courier (secret, t≈228) |
| 228.5 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 230 | 5 | swarm | [Husk](../../../enemies/space/husk/README.md) | 4 | front | |
| 232 | 5 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 234 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 236 | 5 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 236 | 5 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Step D2: t=236, not 247: up their side lanes before the t=253 Husks drift out to the sides |
| 237 | 5 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | left side | |
| 237 | 5 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 3 | right side | |
| 238 | 5 | single | [Seed casket](../../../enemies/space/seed-casket/README.md) | 1 | front | Casket **C6** |
| 240 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 241 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 244 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 10 | rear | |
| 245.5 | 5 | line abreast | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 247 | 5 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 249 | 5 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Step D2: the denser mixed waves (the act's floor; the user's 2026-11-05 answer for Act 7) |
| 250 | 5 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front (alternating edges) | |
| 250 | 5 | swarm | [Husk](../../../enemies/space/husk/README.md) | 3 | front | Step D2: t=250, not 253, diverging to the side edges in the upper half: gone by ≈ t=262 (the end calm) |
| 256 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | **The last wave**; gone by ≈ t=262, the calm to the end at t=268 |

Totals: Skitter 96 · Harrow 61 · Mote Swarm 130 · Husk 32 · Chimera 74 · Stinger 60 · Needler 49 · Lamprey 30 · Wraith 10 · Mantis 4 · Leviathan 1; the Seed caskets (6) are not enemies.
<!-- /data -->

## Hazards

- **The Husks' answer** ([its behaviour](../../../enemies/space/husk/README.md#behaviour)): a damaged Husk's crack fan
  and its death ring; the quiet's cue at t=144.
- **The quiet** (Q4 a): scripted and harmless (see *Layout*).
- **Hollowed hulls** (the [world's](../../../world/vrell-space/README.md#hazards--set-pieces)): scenery on `deep` and
  `low-air`, no hit boxes; shots pass over them.
- **Special limits** (J12): `NO COVER` for the Airstrike and the Orbital Lance; `NO GROUND` for the Bomb Rack and every
  bomb delivery (no ground layer: the hulks are scenery); the EMP Burst stuns the Husks 1.5 s (their crack clock
  stops) and every Vrell unit; the Smart Bomb's flash cracks a Husk like any hit; the new **Swarm Call**'s drones hunt
  the remnants (never the Leviathan: it is on `high-air`).

## Secrets and pickups

The salvage level of the act: the caskets (the secondary), the Leviathan's parts and two hidden crates.

- **The Helix wreck** (secret 1, section 2, t≈88, x≈110): the last of the 2179 Helix survey that went missing at the
  gate (Level 43's pod was its beacon), a small human hull wedged in a Vrell hulk's breach, intact, its lights long
  dead. Its hatch is a **20 HP** trigger (28 on hard); broken, it frees a hidden crate worth **100** in Act 1 terms
  (**1,678**). The Salvage scanner's glint marks it. Its event line, Varga: "*Helix markings. The rest of the 2179
  survey. They made it through.*"
- **Vorne's courier** (secret 2, section 5, t≈228, x≈380): a gold Ascendancy courier drone tumbling slowly, its drive
  burnt out, on the course the lifeboat flew; a **20 HP** trigger (28 on hard), a hidden crate of **100** (1,678). Its
  event line, Rook: "*Gold paint. One of Vorne's couriers. He came this way.*"
- **The dying Leviathan's salvage**: each wrecked part drops a small salvage (10), its death a medium (50): **120** in
  Act 1 terms taken whole (its [Act 7 section](../../../enemies/space/leviathan/README.md#act-7-the-dying-leviathan-level-46-draft)).
- **The seed caskets**: six medium salvage (50 each).
- **Shield cell**: dropped by the left Mantis of the t=167 pincer. **Overdrive**: dropped by the tip of the t=212
  V-wing. **Armour patch**: dropped by the first Husk (t=14) when destroyed (the lesson's reward for meeting the new
  unit).

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), drafted for the **1 s rule** (≈ 12 characters a second, a
1.5 s lead, 0.4 s of silence; event lines drop after 6 s); `RadioTimelineLevel46Test` checks them with Rook and
without. **The radio's noise** (J3): step **4** of the act's rise (L43 → L47), the subtitles clean; the Choir's channel
clean (it is close). **Speakers**: Okafor, Varga, Rook (barks `requires: escort`), the Choir once on the enemy channel.
The [Silence's rule](../../../story/README.md): no one names it; only Varga comes near it, and her lines describe what
the wing sees, never what did it. Nobody speaks during the quiet but Varga, once.

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Dead ships, Aegis. Hundreds of them. Stay sharp anyway." |
| t=11 | Varga | "That hull is drifting dead. Leave it, or shoot it and see if it answers." |
| The first Husk cracks (`first-crack`, event, bark) | Rook | "It answered. Noted." |
| t=24 | Varga | "Seed caskets in the wreckage. Crack them as they pass. Five, and the fleet eats for weeks." |
| t=47 | Okafor | "Lampreys in the lanes, both sides. Keep moving." |
| t=68 | Varga | "Wraiths behind you. Your missiles find them where the guns can't." |
| t=126 (t=126 (the quiet)) | Varga | "Lancer, look at the hulls. They were opened from the inside. Nothing burned them. Something emptied them." |
| t=144.5 | Okafor | "Contacts on every side! The dead ones are waking." |
| t=152 (t=152 (enemy channel)) | The Choir | "…these were ours… we left them… we ran…" |
| t=158 | Varga | "They are not singing to us. They are singing away from something." |
| t=181.5 | Varga | "One hull's still warm. Its core is alive. Cut the sinews and fly through it. I want that core." |
| The core collected (event) | Varga | "A living core. Give me a week and it will power anything we own." |
| The core missed (event, it leaves the screen) | Okafor | "The tug has the core. Stay on the lane, Lancer." |
| t=219 | Okafor | "The dead world. Past it the Choir's song gets louder. That's our heading." |
| Secondary met (`secondary-objective`, the fifth casket) | Rook | "Five caskets. The quartermaster owes you a drink." |
| Secondary failed (the second casket away) | Varga | "Two caskets lost to the drift. A pity." |
| Level end | Okafor | "Out of the graveyard. Form up, Aegis. Nobody talks until Varga's done." |
<!-- /data -->

Timeline check (text, by the rule above): the timed lines close at ≈ 10.6, 18.5, 33.0, 52.4, 74.9, 136.2, 150.1,
156.8, 164.9, 190.8 and 221.6; nothing overlaps. Varga's Husk line closes as the first Husk drifts in (t=14); the
casket line comes before C1 (t≈30); the Wraith line before their first rear ambush (t=70). In the quiet only Varga's
line plays (t=126–136.2, while the Leviathan is overhead). Okafor's "*waking*" line rides the quiet's cue (t=144); the
Choir's line, the act's fourth Choir line of fear (after Levels 43–45), and Varga's answer (her Act 7 understanding, the
[story](../../../story/README.md)'s line) follow before the core. The event lines wait up to 6 s for a free slot.
D-story may move "*singing away from something*" to another level; this draft places it where the hint is.

## Music & ambience

**J8** (L46 drops the music; the cues are D-audio's [music](../../../audio/music/README.md#cues-per-level-act-7)): the
Act 7 B theme's (**track 17**, new) base stem alone at −6 dB in sections 1–2, **out at t=120 for the quiet** (a 1 s
fade) and through the Husks' cue; its pulse returns with the remnants (t=147) and thins away over the dead world, so
that the level ends in the bed alone; the alternative is no music at all. Ambience: the **`dead-fleet`** bed
([sfx](../../../audio/sfx/README.md#ambience-per-setting)), alone through the quiet: a low, empty space drone with the
slow creak and tick of cold hulls, very sparse. New sounds this level needs, for the act's sfx step: the Husk's (its
document's brief: the creak, the ice-fracture crack, the glassy shards, the hollow collapse), the dying Leviathan's
slower, lower cry if it dies (its section's), the casket's dry crack, the warm hull's sinews tearing and the core's
soft hum when taken; the rest as built. Freesound only for real things (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(46) = 700 × 1.07⁴⁵ ≈ **14,702** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act 1
terms** (Skitter 5, Mote 2, Harrow 12, Chimera 14, Lamprey 12, Wraith 30, Mantis 30, Husk 25, a small salvage 10, a
medium 50, a crate 100, the secondary 100), × the act factor **16.777** (1.6⁶), bounties also × the scale (salvage,
crates and the secondary are not scaled). The salvage level earns much of its haul from the wrecks: a **`bounty_scale`
of 0.19** (the economy's shares: air kills 60 %, salvage 80 %, the secrets and the secondary 50 %; a first estimate
for `TypicalHaulTest` and `levelReport`) puts the typical haul at **14,794 (+0.6 %)**, the perfect run at 23,146
(1.57 × budget). The Vrell core pays nothing (a story pickup).

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 96 × 5 + Harrow 61 × 12 + Mote Swarm 130 × 2 + Husk 32 × 25 + Chimera 74 × 14 + Stinger 60 × 15 + Needler 49 × 12 + Lamprey 30 × 12 + Wraith 10 × 30 + Mantis 4 × 30 | 12,670 | 7,602 |
| Salvage: seed caskets 6 × medium salvage (50) | 5,034 | 3,222 |
| Salvage: the scenery Leviathan, taken whole (8 × small salvage + its death's medium salvage: 130) | 2,183 | 1,397 |
| Secret: Helix wreck (hidden crate, 1% of budget) | 1,678 | 839 |
| Secret: Vorne's courier (hidden crate, 1% of budget) | 1,678 | 839 |
| Secondary: break 5 of the 6 seed caskets | 1,678 | 839 |
| **Total** (bounty scale 0.135) | **24,921** | **14,738** |
| Budget(n) = the typical haul's target; typical +0 %, perfect 1.70 × budget | | 14,702 |
<!-- /data -->

**Measured** (`levelReport`, step D2, with the denser mixed waves): the typical haul on budget at the scale **0.135**
(the denser waves' kills; the hand estimate 0.19 below was the approved 64 waves'), the seed caskets' and the dying
Leviathan's salvage counted at the ground targets' share × the pickups' (as Level 34's containers), the Leviathan's
eight parts each with its small salvage (130 whole in Act 1 terms).

The scale is the act's lowest (L43's draft 0.27, L45's 0.30), as Level 34's built 0.27 was Act 5's: the breather's
kills are few and its salvage (≈ 38 % of the typical haul; the Leviathan ≈ 14 % of the budget, its set piece's share)
carries it. The Salvage scanner's +10 / +20 % on the salvage and the crates comes on top, outside the budget.
**Balance plan**: the L46 visit buys the Swarm Call (forward 198 against the reference **184**, 1.08, the
[Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)); 24.5 of 25 MW; Rook's Autocannon.

## Difficulty notes

- **Easy**: the Husks' easy hooks (fan of 3, ring of 6), the casket secondary 4 of 6, the Wraiths' easy bursts, the
  formation lever thins the popcorn; the quiet and the Leviathan unchanged.
- **Hard**: the Husks' fan of 7 and ring of 10 with a 0.4 s crack tell (the quiet's cue then 0.5 s apart), the casket
  secondary 6 of 6, the Wraiths' 7-shot bursts, the caskets' and secrets' 26/28 HP.
- Every difficulty: the quiet's 24 s and its silence are the same (a scene, not a difficulty lever).

## Concept art

Round 91 (M11 part D, step ART-D, 2026-11-06; production art, pending the user's review): the dead-fleet kit, the
warm hull and its core, the seed casket, the Helix wreck and Vorne's courier by `tools/art/act7_deadfleet.py`
(`assets/backdrop/level-46/`; the sprites in `assets/` since step D2's data claimed them, the dying Leviathan's held for step D4; the
`backdrop:` block, the sections and the warm hull's, sinews' and hatch's places in `concept/backdrop-proposal.yaml`
for step D2). The Husk's production art is in [its document](../../../enemies/space/husk/README.md#concept-art), the
dying Leviathan's grade in [the Leviathan's](../../../enemies/space/leviathan/README.md#concept-art) and the two
briefing images in the [briefing](../../../ui/briefing/README.md#concept-art) (`briefing-images-final-r91-a`). Pending
the user's review: Vorne's courier is drawn as a tumbling debris chunk (Level 43's Helix pod's kind, frame 0 serving a
trigger's look); the starless patch is the game's code (D4), no art. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r91-a.png](concept/backdrop-final-r91-a.png) | Production backdrop: eight screens at half size with stand-ins (the outer wrecks and the world small and far, the graveyard lanes, the Helix wreck, the quiet's densest torn hulks, the warm hull with its core, the world filling the lower sky), the pieces (four hollowed hulks, the Helix wreck, the warm hull with its warm light, the world far and near) | chosen (round 91 F.2 accept) |
| [concept/backdrop-final-r91-a.gif](concept/backdrop-final-r91-a.gif) | The level in a time-lapse, one frame every 4 s, at half size (the hulks rocking slowly) | chosen (round 91 F.2 accept) |
| [concept/l46-props-final-r91-a.png](concept/l46-props-final-r91-a.png) | The seed casket (whole, broken, its break), the core in its four sinews breathing, its rise and drift in its violet-gold halo, the sinew knot (whole, hit, cut) and its tear, the warm hull and the Helix wreck at 1×, the Helix hatch (shut, dented, blown) and its break, Vorne's courier tumbling and its break (sheet) | chosen (round 91 F.3 accept) |
| [concept/l46-props-final-r91-a.gif](concept/l46-props-final-r91-a.gif) | The warm hull coming in on the ground's scroll, its four sinews cut one by one, the core lifting out and drifting loose | chosen (round 91 F.3 accept) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (the five deep tiles cut at the sections' rows, the world far and near, the four hulks, the Helix wreck, the warm hull and its glow with their paths), the sections, the warm hull's core and sinews' and the Helix hatch's places, for step D2 (not data) | chosen (round 91 F.2 accept) |
| [concept/quiet-capture-r91-a.gif](concept/quiet-capture-r91-a.gif) | Game capture (step CD5): the quiet, the dying Leviathan overhead, the Husks drifting, the starless patch (t=127–135) | chosen (round 91 L.1 ok) |
| [concept/cracks-capture-r91-a.gif](concept/cracks-capture-r91-a.gif) | Game capture: the cue at t=144 cracking the Husks still on the screen in turn (t=143.5–149) | chosen (round 91 L.2 ok) |
| [concept/core-taken-capture-r91-a.gif](concept/core-taken-capture-r91-a.gif) | Game capture: the sinews cut, the core freed and flown through, `VRELL CORE SALVAGED` (t=185.5–191) | chosen (round 91 L.3 ok) |
| [concept/husk-vs-hulk-capture-r91-a.png](concept/husk-vs-hulk-capture-r91-a.png) | Game capture: Husks against the scenery hulks and their bullets over the brightest hulls, three crops at 1:1 | chosen (round 91 L.4 ok) |
| [concept/cracks-capture-r92-a.gif](concept/cracks-capture-r92-a.gif) | Game capture (step EF5, round 92 L.6, round 91 C.7's fix): the crack cue at t=144 with all five quiet Husks on the screen, cracking in turn | chosen |

The round's game captures (step CD5) are the `*-capture-r91-a` rows above; how they were made: [concept/prompts.md](concept/prompts.md#game-captures-r91-a).

## Implementation

- [x] Level data `data.yaml` (M11's build, step D2): sections and speed, `reach-end`, the `escapes: seed-casket`
      secondary (`allowed: 1`; easy 2, hard 0), waves, the quiet (no waves t=120–144, the Husks' cue at t=144), radio,
      budget, threat profile, briefing, music (the stem's drop and return as `stem_times`); `medium: vacuum`, the
      `beyond-gate` limits (`NO COVER`), no ground (`NO GROUND`); the backdrop block (the starless patch is the game's
      code: step D4)
- [x] The seed casket as a unit of its own ([seed-casket](../../../enemies/space/seed-casket/README.md): a play-plane
      object, 20 HP, a medium salvage, not picked by homing or Rook)
- [x] The dying Leviathan as its Act 7 section: the timed pass t=122–142 on `high-air`, no attacks, scenery for the
      counts, salvage per part (its dying grade in the game: step D4)
- [x] The warm hull: four sinew objects pinned on `low-air` (15 HP), the core's 2 s rise, the fly-through story pickup
      `vrell-core`, the tug's fallback; the Mk VI unlocked by the win (`available: L47`); the hull's look and the
      `VRELL CORE SALVAGED` line in the game: step D4
- [x] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), ≥ 64 a minute (`DensityTest`, the breather's
      dip), ≥ 60 % front, the quiet windows, ≥ 4 s of calm after the last enemy, the Husk and medium-hull spacing
      (`HullStackingTest`, `VrellChecks`' drift lines)
- [x] `CASKETS n / 5` on the tracker (the built `escapes` label; seen in the game: step CD5's captures)
- [x] Radio voiced with the step-4 noise, every timed line at most 1 s late (`RadioTimelineLevel46Test`, with Rook and
      without); the Choir's line on the clean enemy channel
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`); the
      Husk's portrait (`intel/husk`, step ART-D); `TraitCoverageTest` covering Level 46
- [x] Briefing: both pages, each one screen (`BriefingLayoutTest`), with their images
- [x] Typical haul within ±5 % of budget(46) = 14,702 with the level's `bounty_scale` (`TypicalHaulTest`)
- [x] Music and ambience: track 17's base stem (the sparse stem until 2026-11-12) and its drop, the `dead-fleet` bed, the level's sounds
      (`Level46SoundsTest`; step D4: the bed is round 91's a until the user's pick)
- [x] Easy/hard variations; the autopilot (holding fire in the quiet) flies it on every difficulty with Rook and with
      `--escort none` (`Level46Test`)
- [x] Production art and captures for the build's round (step ART-D; step CD5's captures; round 91)

## Open questions

None open: round 84's items were decided 2026-11-05 (see *Decisions*); step D2's own choices wait for round 91.

## Decisions

- 2026-11-04 (Act 7 design phase, step `m11-d-levels-2`; decided 2026-11-05, round 84): promoted from the
  act roster to a draft level document, hand tables only, no `data.yaml`. Space only, all directions (67 % front);
  five sections, 268 s at 90 px/s, 347 enemies (≈ 78 a minute, inside the breather's 64); the quiet t=120–144 with
  five drifting Husks, the dying Leviathan t=122–142 on `high-air`, the screen-fixed starless patch at x≈360,
  y≈150, Varga's one line, the Husks cracking on cue in turn at t=144; the seed caskets as the secondary (5 of 6);
  the warm hull's living core as the Mk VI's salvage (t≈184); the Helix wreck and Vorne's courier as the secrets; the
  Choir's "*we ran*" line and Varga's "*singing away from something*"; the radio's noise step 4; `bounty_scale`
  0.19.
- 2026-11-04 (step `m11-d-act`, decided 2026-11-05, round 84): the story step kept the Choir's line and Varga's "*singing away*" here and adopted the names (the
  open questions closed); the Choir's line is the act's fourth of fear; the bed `dead-fleet` (D-audio's cues).
- 2026-11-05, round 84 (the user's page answers; decided as drafted unless stated, the alternatives rejected). The open items were:
  - **Q4** (round 84, the quiet and the Silence's hint): drafted **a** (≈ 24 s, the music out, harmless drifting Husks,
    a screen-fixed starless patch that never moves, Varga's one line, then the cracks); **b** the same, and at the level's
    end the patch moves (something occludes the stars: it breaks "never shown"); **c** radio only, no patch (a weaker
    hint). The [world's open questions](../../../world/vrell-space/README.md#open-questions) hold it.
  - **Q9** (round 84): drafted **b** (the Vrell core salvaged here, the Mk VI from the L47 visit); **c** keeps the core
    (the Mk VI without the capture story is the same set piece); **a** changes nothing here.
  - **J6** (round 84, the dying Leviathan): drafted as its section's set piece; the alternative is no Leviathan (the
    quiet's Husks alone).
  - **J8** (round 84, the music): drafted as a sparse track-17 stem that drops out for the quiet; the alternative is no
    music in the whole level.
  - **The secondary** (round 84, D.10 and the level's approval): drafted as the seed caskets (5 of 6, Level 34's form);
    the alternative, none (a breather), rejected.
- 2026-11-06 (M11 part D, step D2; pending the user's review in round 91): built. **Measured** (`levelReport`, the
  plan's fit with Rook, before tuning): the approved 64 waves gave 1.82 on the screen at medium and 14 pauses over 2 s
  (the plan's beam and coil pod clear the remnants at once), the haul −32 % at 0.19. So step D2 added the act's
  **denser mixed waves** (the user's answer for Act 7, 2026-11-05: "not just with small enemies"): Needler wings and
  columns, Stinger lines and columns, Chimera wings and Harrow streams on the offbeats (never in the quiet windows),
  Mote Swarms of 10 and Skitter streams of 8, 546 counted units at medium (easy 431, hard 661): **2.64 on the screen**
  and **29 shots a minute** at medium (hard 3.7 and 44), the three approved quiet windows the only pauses over 2 s
  (the first Husk taught alone, the quiet's approach t≈113–118, the warm hull; easy the same three over 3 s, listed
  in `PacingTest`), ≥ 6 s of calm at the end, no big-hull overlap; the Lamprey pincers fly as two edge-warned streams
  of 3 (the Lamprey has no pincer); the Husks' lines step D2's own (each wave's hulls ≥ 80 px apart; the front waves
  diverge to the side edges above the Wraiths' hover band, the t=160 and t=209 waves down the middle third, the t=209
  one from the front, not the right, the last Husks at t=250, not 253, for the end calm); the Wraiths at t=152, 236
  (not 157, 247) clear of the Mantises' and Husks' paths; the remnants' rear Mote Swarm at t=145.5 (not 148) under
  Okafor's "Contacts on every side!"; the quiet's hold (Rook's and the autopilot's) from t=118 (from 120 Rook woke the
  then t=118 Husks in their first 2 s; since step D6 they enter at t=132); the beam era's small units live 0.65× Level 10's time (listed in
  `BeamEraHpTest`, as Level 45's 0.69×). The secret Helix wreck rides the ground's scroll (a path as the warm hull's)
  so its hatch is pinned on it; Vorne's courier is ART-D's tumbling debris chunk (20 HP on every difficulty: a chunk
  has no hard HP); one still hulk of the quiet left out and four moved 0.5–2 s later (the motion budget's two rocking
  hulks). The dying Leviathan drops a small salvage for each of its 8 parts, the blowhole's too (step CD1's rule: 130 in
  Act 1 terms taken whole, not the section's 120).
- 2026-11-06 (step D4, the game; pending the user's review in round 91): each Husk keeps one of the three hull shapes
  by its serial and turns through its 32 frames by its spin; the starless patch is drawn by the game for this level
  (no data key: x 360, 150 px below the top, opaque to 36 px, a smoothstep to nothing at 84 px, about 120 px across at
  half); the warm hull's sinews, its core and the Helix wreck's hatch are drawn over the low-air hulls that carry them;
  the level's name is too long for the hangar's top bar with `BEFORE` (it reads `L46 - GRAVEYARD OF THE CHOIR`) and for
  the HUD's body font (it is set in the small one); the seed casket has an intel portrait (the intel lists it with the
  wave units).
- 2026-11-06, round 91: F.2 and F.3 accepted (the dead-fleet kit; the warm hull, casket and wrecks) and L.1–L.4 ok (the quiet, the cue's cracks, the core taken, the Husks against the scenery hulks). C.1 (a denser Level 46: 47 added waves), C.2 (the rear Mote swarm at t=145.5), C.4 (the small units' life 0.65× Level 10's against 0.9×), C.5 (the quiet's fire hold from t=118) and C.6 (the Lamprey pincers as two warned streams) accepted as built. **C.7 accepted** (the crack cue): the fix, the quiet's Husks slowed and entered later so all five are on the screen at t=144, is accepted; done in step D6. The `cracks-capture-r91-a` row stays chosen as the record of the call (it showed the fault).
- 2026-11-07 (user, [round 92](../../../concept-rounds/round-92/README.md) closed): **L.6 ok** (round 91's crack cue, fixed: the quiet's Husks come in later and slower, all five on the screen when the cue cracks them in turn); `cracks-capture-r92-a.gif` chosen.
- 2026-11-12 (the user's Act 7 playthrough: "Level 46 has no music"): not a bug: track 17's `sparse` stem played as
  `stem_times` asked (the stream and its changes checked: the stem is sample-aligned with the base and the full mix and
  loaded as the level's extra), but it was inaudible. The stem is the heartbeat and the sub alone, nearly all of it
  below ~120 Hz: −19.8 LUFS as a file, −25.8 at the level's −6 dB (the base stem −18.3, the full mix −14.3); on the
  phone/laptop-speaker model (a 300 Hz high-pass, 24 dB/octave) −54.6 LUFS at −6 dB, under the `dead-fleet` bed's
  −39.8 as a file. Decided by Claude, pending the user's review: the level plays **track 17's base stem** at the same
  times and levels (−6 dB from the start, out for the quiet at t=120, back at t=147, −12 dB from t=214, gone by t=262):
  −24.3 LUFS at −6 dB, −33.6 on the phone model (the *Music & ambience* text above already described the base stem).
  The sparse stem's heartbeat is a synthesised soft kick (two sine thumps sweeping 75/66 → 50 Hz on the drum bus,
  `tools/concept/audio/music_r03.py` `heartbeat`), not the recorded heart: the drum kind of heartbeat the user turned
  down for the Heart in rounds 95/97; the stem itself is unchanged.
