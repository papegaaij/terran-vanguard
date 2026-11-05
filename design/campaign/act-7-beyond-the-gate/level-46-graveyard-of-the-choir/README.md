---
title: Level 46 – Graveyard of the Choir
design: approved
implementation: not-started
art: none
depends-on: [../../../world/vrell-space, ../../../enemies/space/husk, ../../../enemies/space/leviathan, ../../../enemies/air/wraith, ../../../enemies/air/lamprey, ../../../enemies/air/mantis, ../../../enemies/air/chimera, ../../../enemies/air/harrow, ../../../player/generator, ../../../player/specials, ../../../player/wingmen, ../../../story, ../../act-5-the-belt/level-34-salvage-run]
updated: 2026-11-05
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
[Act 7 specials](../../../player/specials/README.md#act-7-specials-draft). There is **no `data.yaml`**: the build
turns the hand tables into one; no number is measured.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft; D-story may reword them):

> **Commander Okafor:** "Beyond the reefs our scans show a fleet. Hundreds of Vrell hulls, and none of them moving.
> Cross it and tell me what killed them. Take what you can on the way: we are a long way from Callisto, and nothing
> out here sells us fuel."
>
> **Dr. Varga:** "The hulls are dead, but some still hold a charge. Hit one and it may answer. Look for one that is
> still warm, Lancer. A living core would give the fleet a generator like nothing we can build."

*Images* (production art in M11's build, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-46-dead-fleet`: the fleet's long-range scan, hundreds of hull echoes in a loose cloud before a
  grey world's disc, "NO DRIVE SIGNATURES" stamped across it; the wing's lane a dotted line through it; one patch of
  the starfield in the corner left blank on the scan, unlabelled.
- Varga's page `level-46-hollow-hull`: her thermal scan of one dead hull in grey outline, the plates bent outward round
  the holes, "INTERIOR: EMPTY" in her hand; beside it one hull with a single warm spot at its heart, ringed.

*Hangar teaser* (shop screen before L46; Rook speaks, not voiced):

> **Rook:** "A graveyard. Varga says it's quiet. I'm bringing the autocannon anyway."

The plan's L46 visit buys the **[Vrell Swarm Call](../../../player/specials/README.md#act-7-specials-draft)** (10 000,
new in the shop: the call organ taken in Level 45) and fits it in place of the EMP Burst, and gives Rook his Autocannon
again; 24.5 of 25 MW ([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The intel panel's
`SPECIAL LIMITED` names the Airstrike and the Orbital Lance if fitted (`NO COVER`) and the Bomb Rack (`NO GROUND`).

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `space` (the Husks, Chimera wings, Harrow and Skitter streams, Mote Swarms, Lampreys, Mantis pincers), `high-air` (Wraiths; the dying Leviathan, scenery); **no ground** (`NO GROUND`: the dead fleet's hulks are `deep` and `low-air` scenery) |
| Attack directions | front 67 % · sides 22 % · rear 11 % (43 / 14 / 7 of 64 waves; the act's ≥ 60 % front holds) |
| Density | 3 (the breather) |
| Recommended traits | `homing`, `piercing` (the roster's) |
| Hazards | Derelicts that answer fire (the Husks' crack fans and death rings); the quiet (scripted, harmless) |
| Boss / mid-boss | none |
| Special limits | `NO COVER` (the Airstrike, the Orbital Lance; J12); `NO GROUND` for the Bomb Rack and every bomb delivery |
| Sensor-suite detail | **none**: Vrell space, the dead fleet, `space`, all directions · **L1**: + density 3, the hazard "derelicts that answer fire", the secondary `CASKETS 0 / 5`, `SPECIAL LIMITED` (`NO COVER`, `NO GROUND`) · **L2**: + Husk (new), Wraith, Lamprey, Mantis, Chimera, Harrow, Skitter and Mote portraits · **L3**: + `homing` and `piercing` highlighted, the wave strip marking the quiet |

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

Scroll **90 px/s** (script time; no ground, so the speed only moves the `deep` and `low-air` scenery and the
caskets). Setting: [Vrell space](../../../world/vrell-space/README.md#sub-locations-per-level)'s **dead fleet before a
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

| Section | t (s) | Scroll (px) | Layers and content | Purpose |
|---|---|---|---|---|
| 1. The Outer Wrecks | 0–48 | 0–4,320 | `low-air`: scattered dead hulls, hollowed, crystal at the cracks; `deep`: the withered world small and far, the starless patch; the first Husk (t=14), casket C1 | The Husk taught alone; the patch in place from the start |
| 2. The Graveyard Lanes | 48–118 | 4,320–10,620 | `low-air`: hulks in loose lanes, some broken in two; the Helix wreck (secret, t≈88); caskets C2, C3 | Lampreys from the sides, Wraiths from behind; the breather's steady stretch |
| 3. The Quiet | 118–144 | 10,620–12,960 | `low-air`: the densest hulks, every one torn open from inside; `high-air`: **the dying Leviathan** (t=122–142); five Husks drifting | **The quiet** (Q4 a): nothing attacks; the music out; Varga's one line |
| 4. The Remnants | 144–214 | 12,960–19,260 | `low-air`: the lanes break up; **the warm hull** (t≈184); caskets C4, C5 | The Husks crack; the remnants from every side; **the core** in a quiet window (t=182–192) |
| 5. The Dead World | 214–268 | 19,260–24,120 | `deep`: the withered grey world fills the lower sky, its surface cracked and pale; `low-air`: the last hulks thin out; Vorne's courier (secret, t≈228); casket C6 | The last waves; ≥ 4 s of calm (the last wave gone by ≈ t=262); level end at t=268 |

**The quiet** (t=120–144; Q4 a; the [Husk](../../../enemies/space/husk/README.md#behaviour)'s *The quiet*):

- The last wave before it (t=109) is gone by ≈ t=118; from t=120 to t=144 no wave enters and nothing fires unless the
  player fires first. Five Husks drift through (three from the left at t=118, two from the right at t=128, hulls
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
`bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

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
  breather's exception is the fallback), **to be confirmed with `levelReport`**.
- **Directions** (≥ 60 % front by wave count): front 43, sides 14 (Lamprey and Mantis pincers, Husks drifting in from
  a side), rear 7 (Wraith rear ambushes, Mote swarms from the bottom) of 64: **67 % front**. Husks enter unwarned from
  any edge (their document: scenery until hit); every other side or rear entry gets the built edge warning.
- **Quiet windows**: no wave enters t=12–19 but the first Husk (t=14, alone), t=110–147 but the quiet's Husks (t=118,
  128), or t=182–192 (the warm hull).
- **Limits and spacing** (the Act 5 lesson): at most 6 Husks on the screen (a Husk wave waits while 4 drift); Husks on
  drift lines ≥ 80 px apart that never meet, never over another `medium` hull; the Mantis pincers (t=167, 219) hover in
  their 40 px edge bands while that stretch's Husks drift through the field's middle third; the Wraiths' rear lanes
  avoid the Husks' drift lines; never a Husk's crack beside another's fan in flight (cracked hulls ≥ 120 px apart, or
  the quiet's cue in turn).

Draft wave table:

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 6 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front |  |
| 9 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 14 | 1 | single | [Husk](../../../enemies/space/husk/README.md) | 1 | right | **The first Husk**, alone, drifting across: harmless until shot; no wave enters t=12–19 |
| 20 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 23 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 27 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 30 | 1 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front | Casket C1 drifting (t≈30) |
| 34 | 1 | swarm (scattered) | [Husk](../../../enemies/space/husk/README.md) | 3 | left | Crossing drifts, hulls ≥ 80 px apart |
| 38 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 42 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 45 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 49 | 2 | pincer | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | left, right | The lanes; 3 a side |
| 53 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 56 | 2 | swarm (scattered) | [Husk](../../../enemies/space/husk/README.md) | 4 | front |  |
| 60 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front |  |
| 63 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 66 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Casket C2 (t≈66) |
| 70 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | From behind: `homing` or the beam reach them on `high-air`; no Husk in their lane |
| 74 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 77 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 80 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front |  |
| 84 | 2 | swarm (scattered) | [Husk](../../../enemies/space/husk/README.md) | 3 | right |  |
| 88 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | The Helix wreck (secret, t≈88) |
| 91 | 2 | pincer | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | left, right |  |
| 95 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 98 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Casket C3 (t≈100) |
| 102 | 2 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear |  |
| 106 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 109 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front | **The last wave before the quiet**: gone by ≈ t=118 |
| 118 | 3 | swarm (scattered) | [Husk](../../../enemies/space/husk/README.md) | 3 | left | **The quiet** (t=120–144): drifting, harmless unless shot |
| 128 | 3 | swarm (scattered) | [Husk](../../../enemies/space/husk/README.md) | 2 | right | The quiet; the Leviathan above (t=122–142) |
| 147 | 4 | pincer | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | left, right | **The remnants**: the quiet's Husks cracked in turn at t=144–146.4 |
| 148 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | rear |  |
| 151 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 154 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 157 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear |  |
| 160 | 4 | swarm (scattered) | [Husk](../../../enemies/space/husk/README.md) | 3 | front | Casket C4 (t≈160) |
| 164 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front |  |
| 167 | 4 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | left, right | One a side, hovering; no Husk on its half |
| 171 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 174 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 177 | 4 | swarm (scattered) | [Husk](../../../enemies/space/husk/README.md) | 3 | left |  |
| 180 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | The last before the warm hull: no wave enters t=182–192 |
| 193 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | After the core |
| 196 | 4 | pincer | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | left, right |  |
| 199 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 202 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Casket C5 (t≈205) |
| 206 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front |  |
| 209 | 4 | swarm (scattered) | [Husk](../../../enemies/space/husk/README.md) | 3 | right |  |
| 212 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 216 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front | The dead world |
| 219 | 5 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | left, right |  |
| 223 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 226 | 5 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front | Vorne's courier (secret, t≈228) |
| 230 | 5 | swarm (scattered) | [Husk](../../../enemies/space/husk/README.md) | 4 | front |  |
| 234 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 237 | 5 | pincer | [Lamprey](../../../enemies/air/lamprey/README.md) | 6 | left, right | Casket C6 (t≈238) |
| 241 | 5 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 7 | front |  |
| 244 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | rear |  |
| 247 | 5 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear |  |
| 250 | 5 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front |  |
| 253 | 5 | swarm (scattered) | [Husk](../../../enemies/space/husk/README.md) | 3 | front |  |
| 256 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | **The last wave**; gone by ≈ t=262 (the Husks shot or drifted out), the calm to the end at t=268 |

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

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Dead ships, Aegis. Hundreds of them. The scans say nothing out there is alive. Stay sharp anyway." |
| t=11 | Varga | "That hull is drifting dead. Leave it, or shoot it and see if it answers." |
| The first Husk cracks (event) | Rook | "It answered. Noted." (bark, `requires: escort`) |
| t=24 | Varga | "Seed caskets in the wreckage. Crack them as they pass. Five, and the fleet eats for weeks." |
| t=47 | Okafor | "Lampreys in the lanes, both sides. Keep moving." |
| t=68 | Varga | "Wraiths behind you. Your missiles find them where the guns can't." |
| The Helix wreck opened (event) | Varga | "Helix markings. The rest of the 2179 survey. They made it through." |
| t=126 (the quiet) | Varga | "Lancer, look at the hulls. They were opened from the inside. Nothing burned them. Something emptied them." |
| t=144.5 | Okafor | "Contacts on every side! The dead ones are waking." |
| t=152 (enemy channel) | the Choir | "*…these were ours… we left them… we ran…*" |
| t=158 | Varga | "They are not singing to us. They are singing away from something." |
| t=181.5 | Varga | "One hull's still warm. Its core is alive. Cut the sinews and fly through it. I want that core." |
| The core collected (event) | Varga | "A living core. Give me a week and it will power anything we own." |
| The core missed (event, it leaves the screen) | Okafor | "The tug has the core. Stay on the lane, Lancer." |
| t=214 | Okafor | "The dead world. Past it the Choir's song gets louder. That's our heading." |
| Vorne's courier opened (event, bark, `requires: escort`) | Rook | "Gold paint. One of Vorne's couriers. He came this way." |
| Secondary met (`secondary-objective`, the fifth casket) | Rook | "Five caskets. The quartermaster owes you a drink." |
| Secondary failed (the second casket away) | Varga | "Two caskets lost to the drift. A pity." |
| Level end | Okafor | "Out of the graveyard. Form up, Aegis. Nobody talks about what we saw until Varga's done." |

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

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 84 × 5 + Mote Swarm 88 × 2 + Harrow 45 × 12 + Chimera 54 × 14 + Lamprey 30 × 12 + Wraith 10 × 30 + Mantis 4 × 30 + Husk 32 × 25 (3,472) | 11,067 | 6,640 |
| Salvage: the dying Leviathan, taken whole (120) | 2,013 | 1,610 |
| Salvage: six seed caskets × 50 | 5,033 | 4,027 |
| Secrets: the Helix wreck and Vorne's courier (hidden crates) | 3,355 | 1,678 |
| Secondary: `CASKETS` 5 of 6 | 1,678 | 839 |
| **Total** (bounty scale 0.19) | **23,146** | **14,794** |
| Budget(n) = the typical haul's target; typical +0.6 %, perfect 1.57 × budget | | 14,702 |

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

None yet. The level needs (round 84's proposals, then the M11 part's production art):

- the dead-fleet backdrop kit in Q10 b's palette and light: hollowed Vrell hulks on `low-air` and `deep` (three or four shapes,
  plates bent outward, crystal at the cracks, no light inside), the withered grey world, the **starless patch** (a
  soft edge, ≥ 32 steps, stars fading into it), the cold white key light;
- the **warm hull** with its living core and sinews (whole, cut, the core rising), the seed casket (whole, broken),
  the Helix wreck, Vorne's courier;
- the dying Leviathan's grade (its section's: desaturated, glows dimmed, no new frames; ART-R-B's mood sheet);
- the two briefing images;
- the round's captures: the quiet (the Leviathan overhead, the Husks drifting, the patch), the cue's cracks in turn,
  the core taken (`VRELL CORE SALVAGED`), readability crops of the Husks against the scenery hulks at 1:1.

## Implementation

- [ ] Level data `data.yaml` (M11's build): sections and speed, `reach-end`, the `escapes: seed-casket` secondary
      (`allowed: 1`; easy 2, hard 0), waves, the quiet (no waves t=120–144, the music's drop, the Husks' cue at
      t=144), radio, budget, threat profile, briefing, backdrop with the screen-fixed starless patch, music;
      `medium: vacuum`, the `beyond-gate` limits (`NO COVER`), no ground (`NO GROUND`)
- [ ] The seed casket in the enemies' data (a play-plane object, 20 HP, a medium salvage, not picked by homing or
      Rook)
- [ ] The dying Leviathan as its Act 7 section: the timed pass t=122–142 on `high-air`, no attacks, scenery for the
      counts, salvage per part
- [ ] The warm hull: four sinew triggers on `low-air`, the core's rise, the fly-through pickup `VRELL CORE SALVAGED`,
      the tug's fallback; the Mk VI unlocked by the win (`available: L47`)
- [ ] Wave script to the Act 2+ rules (`ActFloorTest`, `PacingTest`), ≥ 64 a minute (`DensityTest`, the breather's
      dip), ≥ 60 % front, the quiet windows, ≥ 4 s of calm after the last enemy, the Husk and medium-hull spacing
- [ ] `CASKETS n / 5` on the tracker
- [ ] Radio voiced with the step-4 noise, every timed line at most 1 s late (`RadioTimelineLevel46Test`, with Rook and
      without); the Choir's line on the clean enemy channel
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`); the
      Husk's portrait; `TraitCoverageTest` covering Level 46
- [ ] Briefing: both pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(46) = 14,702 with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music and ambience: track 17's sparse stem and its drop, the `dead-fleet` bed, the level's sounds
      (`Level46SoundsTest`)
- [ ] Easy/hard variations; the autopilot (holding fire in the quiet) flies it on every difficulty with Rook and with
      `--escort none`
- [ ] Production art and captures for the build's round

## Open questions

- **The secondary**: drafted as the seed caskets (5 of 6, Level 34's form); the alternative is none (a breather).

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
