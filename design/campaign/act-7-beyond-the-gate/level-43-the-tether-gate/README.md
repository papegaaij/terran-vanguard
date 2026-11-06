---
title: Level 43 – The Tether Gate
design: approved
implementation: done
art: proposed
depends-on: [../../../enemies/space/gate-warden, ../../../enemies/air/chimera, ../../../enemies/air/harrow, ../../../enemies/README.md, ../../../world/vrell-space, ../../../player/generator, ../../../player/wingmen, ../../../player/specials, ../../../ui/hud]
updated: 2026-11-05
---

# Level 43 – The Tether Gate

## Summary

Act 7 opens at **the gate's mouth**: the fleet has burned from Callisto to the Tether Gate beyond Neptune, and Aegis
Wing flies ahead of the carrier *Steadfast*'s group to clear the moon-sized ring's approach. Its guards are the
[Gate Warden](../../../enemies/space/gate-warden/README.md)s, sentinels of the ring's own stone sealed in a shell that
opens only while they fire; the Vrell's swarms and their hybrids (Chimeras, Harrows) fly between them. The secondary
is to leave **no Warden alive**. In the last minute the wing flies along the ring's surface, and the level ends in the
**gate transit**: a 1.5 s warp into the event plane. Space only, all from the front. 268 s, ≈ 111 enemies a minute at
medium.

**Status: approved** (Act 7 design phase, step `m11-d-levels-1`, 2026-11-04; the user approved it on 2026-11-05, round 84).
Written from the act roster, the gap analysis's recommended answers (**Q2 a**, **Q11 a**; J1, J3, J10, J12) and the
earlier steps' drafts, which are the source of truth for their numbers: the
[Gate Warden](../../../enemies/space/gate-warden/README.md) and the
[window shields](../../../enemies/README.md#window-shields), [Vrell space](../../../world/vrell-space/README.md)'s
setting, mood and gate transit, the [Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft),
[Rook in Act 7](../../../player/wingmen/README.md#rook-in-act-7-draft), the
[Act 7 specials](../../../player/specials/README.md#act-7-specials-draft) (`NO COVER`) and the HUD's
[Act 7 additions](../../../ui/hud/README.md#act-7-additions-draft). There is **no `data.yaml`**: the build turns the
hand tables into one; no number is measured.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. The [act](../README.md)'s title card
(J1: *The Tether Gate · November 2185*) and intro (J2: Okafor, page 3 the Choir intercepted) come before it; the
texts are the act document's. This briefing follows them, in **November 2185** (J10). A `v0.4` save at the L43
hangar plays the title card and intro once on Continue before this briefing (`act-intro:7`, the
[completed campaign's save rules](../../../systems/saves/README.md#the-completed-campaign-act-7-draft)). Texts (draft):

<!-- data: briefing -->
> **Commander Okafor:** "The Tether Gate. Vorne went through it, and the Vrell came out of it.
> We go after him. Things grown from the ring's stone guard its mouth: clear them, then lead us
> through. No bombers or lance past Neptune. Rook flies your wing."
>
> **Dr. Varga:** "The wardens sit in a sealed shell; gunfire only sparks off it. They open for a
> breath to fire their line, and the coil's arc cracks the shell for longer. Something woke this
> dead ring. Past it, space bends. Fly your ship, not the stars."
<!-- /data -->

*Images* (production art in M11's build, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-43-the-mouth`: a CDF chart of the ring seen from the fleet's approach, nearly edge-on; the
  Warden stations at its mouth as crimson dots in arcs, the *Steadfast* group at the bottom, the wing's lane a dotted
  line into the ring's centre, "NEPTUNE +0.4 AU" in the corner.
- Varga's page `level-43-gate-warden`: her scan of a Warden in grey outline, twice: shell closed (a pale gold ovoid)
  and open (six petals folded back, the eye crimson); under it a timeline bar, "1.8 s OPEN / 4.0 s".

*Hangar teaser* (shop screen before L43, the act's first; Rook speaks, not voiced):

<!-- data: teaser -->
> **Rook:** "One door, no help on the far side. Pack everything, Lancer."
<!-- /data -->

The hangar before L43 adds the **Bastion Shield** to the shop ([shields](../../../player/shields/README.md)); the plan
buys the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md) **L4** (the level's `shield-breaker`, the
arc's reach 170 px) and flies 23.5 of 25 MW
([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The intel panel's `SPECIAL LIMITED`
warning names the Airstrike and the Orbital Lance if fitted (`NO COVER` from this level on, the
[Act 7 specials](../../../player/specials/README.md#act-7-specials-draft)); the plan's EMP Burst is unaffected.

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `space` (the Gate Wardens at their stations, the Vrell swarms, Chimera and Needler wings, Harrow streams, Stinger columns); `low-air`: Spore Bomber lines between the Warden groups; `ground` in section 4 only: the ring's surface and its harmless **glyph stones** |
| Attack directions | front 100% |
| Density | 4 |
| Recommended traits | `forward`, `shield-breaker` |
| Hazards | The Wardens' `laser-line`s (straight down and aimed); the Harrows' burst rings; the Spore Bombers' rising spores; the gate transit (scripted, harmless) |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: the Tether Gate, `space`, front · **L1**: + density 4, the hazard "laser lines", the objective `CLEAR THE GATE`, the secondary `WARDENS 0 / 16`, `SPECIAL LIMITED` (`NO COVER`) · **L2**: + Gate Warden (new), Chimera, Harrow, Spore Bomber, Needler, Stinger, Skitter and Mote portraits · **L3**: + `forward` and `shield-breaker` highlighted, the wave strip marking the seven Warden groups |
<!-- /data -->

*Varga's intel line* per sensor-suite level (must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "The Tether Gate, Lancer. A ring of stone the size of a moon." |
| L1 | "Straight ahead. No air cover past the ring. Laser lines." |
| L2 | "New: Gate Wardens, sealed stone. Chimeras, Harrows, swarms." |
| L3 | "Wardens open only to fire. The coil's arc cracks them." |

**Trait coverage** (`TraitCoverageTest`, the lesson): `forward` and `shield-breaker` are covered by the plan's own fit
(the Ion Beam on the front, the Tesla Coil Pod on the left wing, in the shop since L29), with 20 MW for the weapons on
the Mk V+ after the Mk III and the magnet.

## Objective

- **Primary** `reach-end`: clear the gate's mouth (`CLEAR THE GATE`); the level ends in the gate transit.
- **Secondary** `escapes` (the Wardens): **16** Gate Wardens hold the mouth in seven groups (t=36 to 218). Each holds
  its station 14 s ([its document](../../../enemies/space/gate-warden/README.md#stat-block)), then rises out through
  the top edge: a Warden that leaves alive has gone back to the ring. The secondary is met when **all 16** are
  destroyed (`escapes: gate-warden`, `allowed: 0`; the gap analysis's "no Gate Warden survives") and fails at once
  when one rises out. The HUD counts to its target (the Act 5 lesson): `WARDENS n / 16`, n the Wardens destroyed,
  `DONE` in green at 16, `FAILED` in red at the first escape. **Readable on screen** (the round 80 lesson): for the
  last **2 s** at its station a Warden's glyphs dim and flicker and it tilts its top toward the edge (the leaving tell,
  proposed for its document, below), and the tracker's well blinks amber while one is in it. Decided by ≈ t=236 at the
  latest (the t=218 pair's hold). Pays **100** credits in Act 1 terms (**1,678** in Act 7, the act factor 16.777 =
  1.6⁶); the typical haul counts 50 %. Easy: 14 of 16 (`n / 14`, `allowed: 2`); hard: all 18 (*Difficulty notes*).
- **No cover** (J12): the level is beyond the fleet's net, so the Airstrike and the Orbital Lance read `NO COVER`;
  the EMP Burst works (it opens every window shield on the screen for 3 s, the
  [window shields](../../../enemies/README.md#window-shields)).

### The glyph stones

The ring's surface (section 4, the `ground` strip of [Vrell space](../../../world/vrell-space/README.md#sub-locations-per-level))
carries the gate's glyphs cut in raised stones. Eight of them are level-owned paying destructibles (like Level 34's
salvage, not a roster unit), so the strip is worth a bomb: the level has ground and the Bomb Rack does not read
`NO GROUND`.

| Field | Value |
|---|---|
| Layer, size | `ground` (hit by all weapons, `anti-ground` ×2; no contact), 48×48 px |
| HP | **60** (easy 45 / hard 78, the global multipliers); a level object: no act or beam-era factor |
| Placement | two clusters of four on the ring's surface, at t≈204 and t≈228, spread across the field; never under a Warden's station |
| Fire | none; never hurts anything |
| Look | a squat block of the gate's dark basalt with one crimson glyph cut in its face, dimmer than a Warden's glyphs and never pulsing |
| Credits | **15** each (Act 1 terms), counted with the ground targets' share (80 %) in the typical haul |
| Destroyed | the block splits along the glyph, which goes dark; stone chips in a ring (vacuum: no smoke) |

## Layout

Scroll **120 px/s** at the mouth, **110 px/s** among the pickets, **100 px/s** in the ring's shadow (the lines of
three stay readable), **140 px/s** along the ring; each change eased over 2 s. The setting is
[Vrell space](../../../world/vrell-space/README.md#sub-locations-per-level)'s gate approach in Q10 b's **L43 light** (the one Vrell palette)
([moods](../../../world/vrell-space/README.md#light-per-level-q10-b)): the gate's dark stone and cold teal-white glyphs
(Vrell stone and glyphs, in the palette) under the violet and teal nebula, the fleet's white running lights behind; the ring a step darker and less saturated
than the Wardens, its glyphs never pulsing in the colour or rhythm of a Warden's telegraph. Medium: **vacuum**
([media](../../../world/vrell-space/README.md#media-per-level)): short flashes and debris rings, no smoke. Motion
budget: the Wardens' shells and lines are the strong elements; the ring grows slowly on `deep`.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. The Mouth | 0–48 | 0–5,760 | 120 | clear | `deep`: the ring small ahead, the nebula; the *Steadfast* group's running lights behind (bottom of the screen at the start, scrolling off by t≈20). `low-air`: drifting spore sacs. | Vrell popcorn; the Warden's intro alone at t=36 (quiet window t=33–40). |
| 2. The Pickets | 48–124 | 5,760–14,130 | 110 | clear | `deep`: the ring grows to a third of the screen, its glyphs readable. `low-air`: dead tendrils. | Warden pairs (straight, then aimed); Chimeras and Harrows; the supply drone (t=118). |
| 3. The Ring's Shadow | 124–200 | 14,130–21,740 | 100 | clear | `deep`: the ring fills the top of the screen, the nebula behind it; the light dims a step. | Three Warden lines of three; the Helix pod (the secret, t≈176). |
| 4. Along the Ring | 200–256 | 21,740–29,540 | 140 | clear | `ground`: **the ring's surface** (basalt and glyph channels) with the glyph stones (t≈207, t≈227). `deep`: the ring's inner curve rising like a horizon; the Choir's line (t=208). | The last Warden pair (t=218); the run to the ring's centre; the last wave at t=246. |
| 5. The Transit | 256–268 | 29,540–31,220 | 140 | clear | `deep`: the event plane fills the ring's centre ahead, pale violet; the scripted formation from t=262; the **warp** t=266.5–268. | ≥ 4 s of calm (the last wave gone by ≈ t=253), then the gate transit; level end at t=268 inside the warp. |
<!-- /data -->

**The gate transit** (Q11 a, [Vrell space](../../../world/vrell-space/README.md#hazards--set-pieces)'s rules): from
t=262 the ship and Rook fly in formation toward the ring's centre, scripted (no input, no hits, Rook's
[Act 7 rule](../../../player/wingmen/README.md#rook-in-act-7-draft)); at **t=266.5** the 1.5 s warp bends the whole
image toward the centre while its colours shift smoothly to the event plane's violet, brightness changing in one
direction only (no flashing, no inversion); the level ends at **t=268** inside it and the debrief opens over the
violet. Level 44 opens in the same violet. The HUD stays drawn over the effect.

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

- **HP** from the proposed Act 7 reference DPS (J11: **172** at L43,
  [Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)): the Gate Warden is new here (**360**,
  [its stat block](../../../enemies/space/gate-warden/README.md#stat-block)); the `small` units fly the
  [beam era](../../../enemies/README.md#balancing-basis)'s factor, **×2.5 from Level 43** (step B3's draft, pending the
  user's review in round 89: the Chimera 50, the Harrow 20; ×2 before, 40 and 16); the Skitters and
  Motes are `tiny`. The returning `medium` [Spore Bomber](../../../enemies/air/spore-bomber/README.md) (step B3b) takes
  the act HP factor (24 → **129**, 0.75 s of the reference DPS).
- **The Vrell's own** (round 72's turn): the Chimeras and Harrows are the Vrell's hybrids now; no Ascendancy unit
  flies (the faction is broken). No unit carries a hex shield (J4).
- **Density and pacing** (the Act 2+ floor from the start): **496 enemies** over 268 s, **≈ 111 a minute** at medium
  (the act's minimum 80; Act 6's band 100–118). A wave every 3 s from t=3 to the last at t=246, gaps of 5–6 s only
  around the Warden entries. Waves by direction: front 76 of 76 (100 %). The floor comes from the Needlers' thorns,
  the Chimeras' aimed pairs, the Stingers' fans, the Harrows' rings and the Wardens' lines: expected to meet ≤ 3
  pauses over 2 s, ≥ 2.5 enemies on the screen and ≥ 10 shots a minute (`PacingTest`, `ActFloorTest`), **to be
  confirmed with `levelReport`** in the build. **Built** (step B3b, the user's answer "Increase the density, but not
  just with small enemies. Add different types.", 2026-11-05): **623 enemies** in 113 waves at medium (≈ 139 a
  minute; easy 474, hard 774), all from the front; the floor met (see *Measured with the denser mixed waves*).
- **The Wardens' windows** (its document's *Difficulty and level use*): the first one alone, then pairs, then lines
  of three; at most 4 on the screen and 2 lines telegraphed at once; a straight-down line never on a column a
  neighbour's line holds. While a group holds its stations the popcorn is thin (swarms of 8 kept off the Warden
  columns, small snakes) so nothing pins the ship under a straight-down line; no Harrow stream enters within 3 s of a
  Warden group's entry. Step B3b's added waves enter between the groups, the Spore Bomber lines once a group has risen
  out; the one inside a hold (t=104.5, under the aimed pair) flies at x 80/240/400, off the pair's columns (x 160/320).
- **Spacing** (the Act 5 lesson): the Wardens are the level's only `medium` hulls; in a group they keep ≥ 80 px between
  hull edges, and no two groups share the screen (each group is gone before the next enters: 14 s holds, ≥ 26 s
  apart). At most two Chimera groups on the screen ([the Chimera](../../../enemies/air/chimera/README.md)'s rule), at
  most 6 Harrows. **A line of three stands 160 px apart** (x 80, 240, 400, the hulls 84 px apart: the wave's
  `spacing: 160`, step B3b); the pairs stay at x 160 and 320. The returning Spore Bombers (`medium`, `low-air`, step
  B3b) fly lines of three between the groups and never overlap an air hull (`HullStackingTest`, the script unharmed).
- **Calm at the end** (the Act 5 lesson): the last wave (a Skitter snake) enters at t=246 and is gone by ≈ t=253;
  **≥ 9 s** of calm before the formation at t=262, ≥ 13 s before the level end.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | Out of the ring's shadow; the Steadfast group's running lights behind on `deep` |
| 6 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 7.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 9 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 12 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 13.5 | 1 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step B3b: a Spore Bomber line, the heavier mix (the user, 2026-11-05) |
| 15 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 16.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 18 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front (left) | |
| 21 | 1 | line abreast | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | |
| 24 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 25.5 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step B3b: a Needler wing on the offbeat |
| 27 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 30 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | The last wave before the quiet window |
| 36 | 1 | single | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 1 | front | **The Gate Warden's first appearance**: alone, straight-down line, quiet window t=33–40 (Varga's line ahead of it); drops the Shield cell |
| 41 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Kept off the Warden's column (the left lane) |
| 43 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 44 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 44.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 46 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 48 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front (right) | The Vrell's hybrids, home with them |
| 51 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 52.5 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step B3b: a second Chimera wing |
| 54 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 55.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step B3b: a Harrow stream before the pair |
| 57 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 59 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 62 | 2 | line abreast | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 2 | front | Straight-down lines, windows 1.3 s apart (left first); hard: a line of three |
| 65 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Between the pair's columns |
| 68 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | From the top edge, alternating its halves 0.6 s apart |
| 71 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 74 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 77 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 78 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step B3b: a Chimera wing as the pair rises out |
| 79 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 80 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 81 | 2 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step B3b: Spore Bombers once the pair has risen out |
| 83 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 84.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step B3b: a Harrow stream |
| 86 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 89 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 90.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step B3b: a Needler wing before the aimed pair |
| 94 | 2 | line abreast | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 2 | front | **Aimed** lines: stand off the line (easy: straight down); hard: a line of three |
| 97 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 100 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front (alternating edges) | |
| 103 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 104.5 | 2 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step B3b: Spore Bombers under the aimed pair, off its columns |
| 106 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 109 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 110.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step B3b: a Needler wing |
| 112 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 113.5 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step B3b: a Harrow stream between the Warden groups |
| 115 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front | |
| 118 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | The Steadfast's supply drone at t=118 (the Armour patch) |
| 121 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 123 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 124 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 125 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 126 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 128 | 3 | line abreast | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 3 | front | The first line of three: straight down, opening left to right; the middle one drops the Overdrive |
| 131 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The left edge, off the line's columns |
| 134 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | |
| 137 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 138.5 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step B3b: a Stinger column |
| 140 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 143 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 143.5 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 146 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 147 | 3 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step B3b: Spore Bombers once the first line has risen out |
| 149 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 150.5 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (left) | Step B3b: a Chimera wing between the Warden lines |
| 152 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | |
| 155 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 156 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 158 | 3 | line abreast | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 3 | front | Aimed lines, opening from the middle out (easy: straight down) |
| 161 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 164 | 3 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front | The Chimera swarm, along the left edge; at most two Chimera groups on the screen |
| 167 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 168.5 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step B3b: a Needler wing |
| 170 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | front (alternating edges) | |
| 173 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 174 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 176 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | The Helix pod drifts down at x 120 (the secret) |
| 177 | 3 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step B3b: Spore Bombers once the aimed line has risen out |
| 179 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 180 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front (alternating edges) | Step B3b: a Harrow stream |
| 182 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 184 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front | Step B3b: a Stinger column before the third line |
| 188 | 3 | line abreast | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 3 | front | Straight down, opening right to left |
| 191 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 194 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 197 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 198.5 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (right) | Step B3b: a Needler wing into the ring |
| 200 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Over the ring's surface (the `ground` strip) |
| 203 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front (alternating edges) | |
| 206 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 207 | 4 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step B3b: Spore Bombers over the ring, once the third line has risen out |
| 209 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 212 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 213 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 4 | front (right) | Step B3b: a Chimera wing before the last pair |
| 215 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 3 | front (alternating edges) | Step B2: a Skitter trickle on the offbeat (the Act 2+ floor) |
| 218 | 4 | line abreast | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 2 | front | The last Wardens, aimed (easy: straight down); the secondary is decided when the second is gone (≈ t=236 at the latest) |
| 221 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 224 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 227 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 228.5 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front (left) | Step B3b: a Needler wing |
| 230 | 4 | column | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front | |
| 233 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 236 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | |
| 237.5 | 4 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | Step B3b: the last Spore Bombers, once the last pair has risen out |
| 239 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | front (alternating edges) | |
| 242 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 7 | front | |
| 246 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 10 | front (left) | The last wave; gone by ≈ t=253, calm to the transit |

Totals: Skitter 212 · Mote Swarm 168 · Needler 64 · Spore Bomber 21 · Stinger 30 · Gate Warden 16 · Chimera 66 · Harrow 46.
<!-- /data -->

The README's hand table had 496 (Skitter 170); step B2 added the 20 offbeat Skitter trickles (60 Skitters, = 556); plus
the 8 glyph stones (not enemies).

**Measured** (`levelReport -Plevel=43`, step B2, the plan's fit: Level 42's with the Tesla Coil Pod L4 and Rook's
Autocannon, the stand-in until step B3's `Level43Test`): the README's 76 waves flew **1.91 on the screen**, 14.6 shots
a minute and **19 pauses over 2 s** at medium (every wave cleared within about a second), so step B2 added a **Skitter
trickle of 3** (a stream from alternating edges, 1 s apart) on twenty offbeats and put the Needlers and Stingers into
the basis's `small_act_hp` (24 and 36 HP with the beam era). Now **556 enemies, 127 a minute** at medium (easy 424,
hard 690), **2.16 on the screen**, 15 shots a minute, **3 pauses over 2 s** (the quiet window t=31.8–36.1, t=183–186,
t=213–215), 20 s of calm at the end. **The floor's 2.5 on the screen is not met** (pending the user's review in round
89): the plan's forward DPS (189) clears the popcorn as it enters, and a killed small unit lives **0.77×** Level 10's
time on the screen (the beam era's band 0.9–1.5). The autopilot wins 3 of 3 on every difficulty; the Wardens'
secondary is met in **3 of 3** at medium and easy, **0 of 3** on hard (18 Wardens), so the Warden keeps its 360 HP
(QB-3 a not needed). The Warden's time to kill: **1.9 s** of the plan's whole fit with the arc holding the shell open
(the Tesla L4's 31 DPS in 189), about **2.3 s of open time** without the pod (158 DPS: two windows, ≈ 4.5 s on the
clock).

**Measured with the draft answer** (step B3, pending the user's review in round 89: the beam era's **×2.5 from Level
43** and the Warden autopilot, `Level43Test`'s `WardenPlay`: the arc held on a Warden from about 100 px below it, a
window timed, off a running line): **1.96 on the screen**, 15.3 shots a minute, **5 pauses over 2 s** at medium and at
hard (the Wardens now die in one or two windows, 4-11 s after they enter), a killed small unit **0.79×** Level 10's time
(×2 with the generic play: 2.16, 0.77×). The autopilot wins 3 of 3 on every difficulty with Rook and with `--escort
none`, the secondary met on every easy and medium run and on 1 of 3 hard runs (all 18: one Warden of an aimed line
of three gets away); no line struck the ship on easy or medium. The haul is unchanged (bounties do not scale with the
beam era): 12,100 at 0.25. **Still missing the floor**: 1.96 against 2.5 on the screen, the small units' 0.79× against
0.9, and 5 pauses over 2 s against 3 (measured, printed, not asserted: `PacingTest.PENDING_WAVES`,
`BeamEraHpTest.PENDING_LEAST`).

**Measured with the denser mixed waves** (step B3b, 2026-11-05, the user's answer above; the draft answer kept: the
beam era's ×2.5 and `WardenPlay`): **23 waves**, 17 new and 6 in place of B2's Skitter trickles (t=13.5, 25.5, 91.5,
113.5, 150.5, 185.5; 14 trickles remain): **7 Spore Bomber lines of three** (t=13.5, 81, 104.5, 147, 177, 207, 237.5:
once a Warden group has risen out, but t=104.5 off the aimed pair's columns), **6 Needler wings** of 4, **4 Chimera
wings** of 4, **4 Harrow streams** of 4 and **2 Stinger columns** of 4: medium 556 → **623** (easy 424 → 474, hard
690 → 774). At medium **3.01
on the screen** (1.96), **22.8 shots a minute** (15.3), **1 pause over 2 s** (5; only the Warden's quiet window
t=31–36), a killed small unit **0.97×** Level 10's time (0.79×), 19.7 s of calm at the end; easy 1.70 on the screen,
13.2 shots, hard 5.59 and 46.5. No big-hull overlap on any difficulty. The autopilot wins 3 of 3 on every difficulty
with Rook and with `--escort none`, no line striking the ship; the secondary met on every easy and medium run, on hard
1 of 3 without Rook and 0 of 3 with him (15-17 of 18: hard's lines of three now have company; B3's 1 of 3, the
level test's 6 of 9 per fit holds). The pacing and the floor are asserted again (out of `PacingTest.PENDING_WAVES`
and `BeamEraHpTest.PENDING_LEAST`).

## Ground targets

The 8 **glyph stones** on the ring's surface in section 4 (*The glyph stones* above): harmless paying destructibles,
not counted as enemies.

## Hazards

- **The Wardens' lines** (the unit's): straight down from the eye or aimed once through the ship's position, a 0.8 s
  crimson telegraph then a 1.0 s live band. The lesson is the window: shoot during the telegraph and slip aside, or
  break the shell with the Tesla arc.
- **The Harrows' rings** (the unit's, as built): however a Harrow dies its sac bursts into a slow ring of 8; kill it
  at range.
- **The gate transit** (Q11 a): a scripted set piece at the end, harmless (*Layout*). Photosensitivity: no flash, no
  inversion, brightness in one direction (the world's rule).

## Secrets and pickups

- **The Helix pod** (t≈176, x 120, drifting down out of the ring's shadow): the beacon pod of the Helix survey that
  went missing at the gate in 2179 (the [story](../../../story/README.md)'s lore), a small breakable object on the
  play plane (20 HP, like a small rock); broken, it frees a hidden crate worth **100** in Act 1 terms (1,678 in
  Act 7). The Salvage scanner's glint marks it. Varga's event line (dropped after 6 s if the radio is busy).
- **Shield cell**: dropped by the t=36 Warden.
- **Overdrive**: dropped by the middle Warden of the t=128 line.
- **Armour patch**: the *Steadfast*'s [CDF supply drone](../../../allies/README.md#cdf-supply-drone) releases one at
  (240, 380 y-up) at t=118, the last the fleet can send before the ring.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), drafted for the **1 s rule** (≈ 12 characters a second, a
1.5 s lead, 0.4 s of silence; event lines drop after 6 s); `RadioTimelineLevel43Test` checks them with Rook and
without. **The radio's noise** (J3): step **1** of the act's rise (L43 → L47), the subtitles clean; the Choir's channel
clean. **Speakers**: Okafor (from the *Steadfast*), Varga (on the *Steadfast*), Rook, and the Choir once on the enemy
channel (Act 5's intercepted dress; its fearful Act 7 voice, the door line). Rook's barks carry `requires: escort`;
his scripted lines (t=13, t=257) do not. **Vorne is silent** (he went through ahead of the fleet).

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, Steadfast. The gate's mouth is yours. Clear its wardens, then lead us through." |
| t=13 | Rook | "Dead for forty-four years, and the day Vorne needs a door, it wakes up. Lovely." |
| t=25 | Varga | "The stone things on the ring are new. Sealed shells. They only open when they fire." |
| t=37 | Varga | "Shoot when the red line shows. Or break the shell with the coil's arc." |
| t=48 | Okafor | "Every warden you leave stands between the fleet and the gate. Leave none." |
| The first Warden killed by Rook (event, bark, `requires: escort`) | Rook | "Cracked one. They don't like the coil." |
| t=78 | Varga | "Chimeras and Harrows. The Vrell brought Vorne's hybrids home with them." |
| t=90 | Okafor | "Aimed lines now. Don't wait in them. Shoot from where the beam isn't." |
| t=118 | Okafor | "Supply drone inbound from Steadfast. The last one you'll get past the ring." |
| t=130 (bark, `requires: escort`) | Rook | "Three at once. Fine. I'll take the left one, you take the other two." |
| t=196 | Okafor | "You're over the ring itself. Our signal gets worse past here. Stay together." |
| t=208 (enemy channel) | The Choir | "…small bright one… why do you stand in the door…" |
| t=215 | Varga | "That was the Choir. It has never asked us anything before." |
| Secondary met (`secondary-objective`, the 16th Warden down) | Okafor | "No wardens left at the mouth. The fleet can pass." |
| Secondary failed (a Warden rises out) | Varga | "One went back into the ring. The fleet will meet it at the mouth." |
| t=245 | Okafor | "The ring is lighting, Aegis. Form up on the centre. We're right behind you." |
| t=257 | Rook | "Here we go. Stay close, Lancer." |
| t=263 | Okafor | "Steadfast, follow Aegis through." |
<!-- /data -->

The Helix pod broken (the secret's event line, in the data's `secrets`): Varga, "Helix survey markings. 2179. So this is where they went."

Timeline check (text, step B5): the timed lines close at ≈ 11.9, 23.7, 35.8, 47.4, 58.5, 88.4, 100.3, 128.5, (140.3),
206.6, 214.6, 222.0, 255.5, 263.1 and 269.6; each opens at most 0.5 s late (the 1 s rule). Okafor's last line opens in
the formation (t=263.5) and runs on under the warp. The secondary
resolves between ≈ t=221 and t=236 (the last pair), in the gap between t=222 and t=245; a failure earlier waits
up to 6 s for a free slot. Varga's t=25 line comes ahead of the first Warden and her t=37 line in its quiet window (t=33–40). The Choir's line is
the act's first in play (the intro's page 3 is heard first; the [Silence's rule](../../../story/README.md): fearful,
it names nothing); "the door" is the gate.

## Music & ambience

The Act 7 A theme (**track 16**, new, J8; the cues are D-audio's
[music](../../../audio/music/README.md#cues-per-level-act-7)): its intro over section 1, the base stem from the
pickets (section 2), the full mix from the ring's shadow (section 3), the `choir` stem over it from the Choir's door
line; at the transit's start the music **fades out over 1.5 s under the transit's sound** (the warp's roar carries the
moment), then the debrief. Ambience: the **`vrell-gate`** bed
([sfx](../../../audio/sfx/README.md#ambience-per-setting)): open space through the hull with a slow stone resonance
from the ring. New sounds this level needs, for the act's sfx step: the Warden's (its document's brief), the glyph
stones' crack, the supply drone's pass (built), the transit's warp (a deep rushing pull rising into a held pressure,
from a real low roar pitched down, no flash of noise); Freesound only for real things (CC0/CC-BY), no bubbly or
popping sounds.

## Credit budget

Budget(43) = 700 × 1.07⁴² ≈ **12,001** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in
**Act 1 terms** (Skitter 5, Mote 2, Needler 12, Stinger 15, Chimera 14, Harrow 12, Spore Bomber 25, Gate Warden 40, the glyph stone
15, the crate 100, the secondary 100), × the act factor **16.777** (1.6⁶), bounties (the stones too) also × the
scale. A **`bounty_scale` of 0.27** (the economy's shares: air kills 60 %, ground targets 80 %, the secret and the
secondary 50 %; a first estimate for the build to confirm with `TypicalHaulTest` and `levelReport`) puts the typical
haul at **12,158 (+1.3 %)**, the perfect run at 20,641 (1.72 × budget). No boss or mid-boss.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 212 × 5 + Mote Swarm 168 × 2 + Needler 64 × 12 + Spore Bomber 21 × 25 + Stinger 30 × 15 + Gate Warden 16 × 40 + Chimera 66 × 14 + Harrow 46 × 12 | 16,718 | 10,031 |
| Ground targets: glyph stones 8 × 15 | 384 | 307 |
| Secret: helix pod (hidden crate, 1% of budget) | 1,678 | 839 |
| Secondary: leave no Gate Warden alive | 1,678 | 839 |
| **Total** (bounty scale 0.19) | **20,458** | **12,016** |
| Budget(n) = the typical haul's target; typical +0 %, perfect 1.70 × budget | | 12,001 |
<!-- /data -->

**Measured** (`levelReport`, step B2, after the offbeat trickles): the typical haul **12,100 (+0.8 %)** at the scale **0.25** (the hand estimate 0.27 before the 60 added Skitters), the perfect run 20,558 (1.71 × budget).
Step B3b (the denser mixed waves, 623 units): the typical haul **12,016 (+0.1 %)** at the scale **0.19**, the perfect
run 20,458 (1.70 × budget); the plan's forward / reference stays 1.10 (`balance.py`).

The scale sits under Act 6's built 0.31–0.63: the act factor rose by 1.6 again while the opener is popcorn-heavy
(338 Skitters and Motes); the build's measure moves it. **Balance plan**: the L43 visit buys the Tesla Coil Pod L4
(forward / reference **1.10**, the [Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)); 23.5
of 25 MW. The level expects `shield-breaker` (the arc keeps a Warden open: about two seconds of the whole fit kill
it) and `forward` (the beam into the eye during a window); it is flyable without the pod by timing the windows.

## Difficulty notes

- **Easy**: the Warden's easy hooks (a window every 4.5 s, only straight-down lines: the t=94, t=158 and t=218
  groups fire straight down); the secondary 14 of 16 (`allowed: 2`); the formation lever's smaller waves; at most two
  Chimera groups as ever.
- **Hard**: the Warden's hard hooks (a window every 3.5 s, the telegraph still 0.8 s; lines of three with a 1.0 s
  stagger): the t=62 and t=94 pairs fly as lines of three, **18 Wardens** (`WARDENS n / 18`, all of them); the
  other waves by the formation lever, trimmed by hand so never more than 4 Wardens and 6 Harrows are on the screen.

## Concept art

Round 84's picks (decided 2026-11-05): the Gate Warden's sheets in its document (ART-R-A, over this level's brightest and
darkest band); Q10 b's one-palette sheet (the light of this level) and Q11's transit storyboard a/b in
[Vrell space](../../../world/vrell-space/README.md) and the act's title-card still (A.9 b, "the mouth") from this level's backdrop
(J1) in the [act](../README.md) (ART-R-B).

Round 89 (M11 part B, step ART-B; production art, pending the user's review): the gate kit by `tools/art/backdrop_l43.py`
(round 84's ring painter and nebula, `tools/concept/act7_r84.py`, imported unchanged), the glyph stones and the Helix pod
by `tools/art/gate_units.py props` (held in `build/art-held/sprites/` until step B2's data claims them), the Gate Warden's
frames in [its document](../../../enemies/space/gate-warden/README.md#concept-art), the briefing images in the
[briefing](../../../ui/briefing/README.md#concept-art) (`briefing-images-final-r89-a`), the act still in the
[act](../README.md#concept-art). The ring's three scales are five held pieces swapped at the section changes (m11b-gaps §4);
**the 2 s cross-fade at each swap is step B4's**. The transit's warp is the game's post-effect (B4), not art. Prompts and
briefs: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r89-a.png](concept/backdrop-final-r89-a.png) | Production backdrop: eight screens at half size with stand-ins (the ring small ahead over the nebula with the *Steadfast*'s group behind, a third of the screen, its near arc filling the top, the ring's surface strip with the glyph stones and its inner rim, the event plane filling the mouth), the pieces and the surface tile (sheet) | proposed (round 89) |
| [concept/backdrop-final-r89-a.gif](concept/backdrop-final-r89-a.gif) | The level in a time-lapse, one frame every 4 s, at half size | proposed (round 89) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (the deep tile sets cut at the sections' rows, the ring-surface tile, the pieces and their held paths, the spore sacs and tendrils) and the sections, for step B2 (not data) | proposed (round 89; step B2's input) |
| [concept/l43-props-final-r89-a.png](concept/l43-props-final-r89-a.png) | The glyph stone (intact, cracked, split) and its destruction; the Helix pod tumbling and shot open, at 2× (sheet) | proposed (round 89) |
| [concept/l43-props-final-r89-a.gif](concept/l43-props-final-r89-a.gif) | The pod tumbling beside a stone breaking | proposed (round 89) |
| [concept/glyph-stones-capture-r89-a.png](concept/glyph-stones-capture-r89-a.png) | Game capture (M11 part B, step B5): the two clusters of glyph stones on the ring's surface, section 4 | proposed (round 89) |
| [concept/transit-capture-r89-a.png](concept/transit-capture-r89-a.png) | Game capture: the gate transit from t=260 to the level's end at t=268 (the formation, the warp), with the play field's mean luminance every 0.1 s (one way up, no flash) | proposed (round 89) |
| [concept/transit-capture-r89-a.gif](concept/transit-capture-r89-a.gif) | The same, t=258–268 at the game's speed | proposed (round 89) |

## Implementation

- [x] Level data `data.yaml` (M11's build): sections and speeds with 2 s eases, waves, radio, budget, threat profile,
      briefing, backdrop, music; `medium: vacuum`, the `beyond-gate` limits (`NO COVER`) (step B2, 2026-11-05)
- [x] The Gate Warden secondary (`escapes: gate-warden`, `allowed: 0`, `WARDENS n / 16` counting to its target, the
      well's amber blink while a Warden shows its leaving tell; easy 14 of 16, hard 18 of 18) (steps A3a, B2, B4,
      2026-11-05)
- [x] The glyph stones: 8 level-owned `ground` destructibles in section 4, paying, not counted as enemies (steps B2,
      ART-B, 2026-11-05: `Level43Test`)
- [x] The gate transit: the scripted formation from t=262 (no input, no hits, Rook beside), the 1.5 s warp post-effect
      (radial pull, smooth grade to violet, no flash or inversion), the level ending inside it and the debrief over
      the violet; Level 44 opening in it (steps B1, B4, 2026-11-05: `GateTransit`, `TransitWarp`, the luminance one
      way)
- [x] Wave script: ≥ 80 enemies a minute at medium (`DensityTest`), the Act 2+ floor (`PacingTest`, `ActFloorTest`),
      the quiet window t=33–40, ≥ 4 s calm after the last enemy, no two `medium` hulls overlapping, the Wardens'
      limits (4 on the screen, 2 lines telegraphed) (step B3b, 2026-11-05: the denser mixed waves, 3.01 on the screen,
      22.8 shots a minute, the lines of three 160 px apart)
- [x] Radio voiced with the step-1 noise, every timed line at most 1 s late (`RadioTimelineLevel43Test`, with Rook and
      without); Rook's barks `requires: escort`; the Choir's line on the clean enemy channel (steps VOX-B, B4, B5,
      2026-11-05: seven lines retimed by B5)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`); the
      Gate Warden's portrait; `TraitCoverageTest` covering Level 43 (steps B2, ART-B, 2026-11-05)
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images; the act's title
      card and intro before it once (`act-intro:7`) (steps B2–B4; both pages shortened and re-voiced: step B5,
      2026-11-05)
- [x] Typical haul within ±5 % of budget(43) = 12 001 with the level's `bounty_scale` (`TypicalHaulTest`) (step B2:
      12,100 at 0.25; step B3b: 12,016 at 0.19)
- [x] Music and ambience: track 16 by section, the `vrell-gate` bed, the level's sounds (`Level43SoundsTest`) (steps
      MUS-B, AUD-B, B4, 2026-11-05; the bed's a/b in round 89, a playing meanwhile)
- [x] Easy/hard variations as in *Difficulty notes* (steps B2, B3b, 2026-11-05)
- [x] The autopilot (timing a window, the arc on a Warden) flies the level with Rook on every difficulty and with
      `--escort none` (`Level43Test`, `WardenPlay`; step B3, 2026-11-05: 3 of 3 everywhere, the secondary 7 of 9)
- [x] Production art and captures for the build's round (ART-B; captures by step B5, 2026-11-05;
      [round 89](../../../concept-rounds/round-89/README.md))

## Decisions

- 2026-11-05: denser with mixed types, the user's answer (step B3b, M11 part B; round 89's floor question closed): "This
  is the final act. It can be a bit harder. Increase the density, but not just with small enemies. Add different
  types." Spore Bomber lines (the returning `medium`), Chimera and Needler wings, Stinger columns and Harrow streams
  between the Warden groups, six of B2's Skitter trickles replaced (623 units at medium, the floor met; see *Measured
  with the denser mixed waves*); the beam era's ×2.5 and the reversal rule kept (neither hurts: the small units' life
  0.97×); the Warden lines of three 160 px apart (x 80/240/400, the sim's `spacing` on a hovering line abreast);
  `bounty_scale` 0.19.
- 2026-11-05 (step B3, M11 part B, pending the user's review in round 89): the floor's draft answer built as one
  number and one rule: the beam era's ×2.5 from Level 43 (`beam_era_hp.later`) and a reversal of a current a scripted
  pause (Level 44's; `LevelMetrics.REVERSAL_SCRIPTED`); measured with the Warden autopilot (`WardenPlay`), the floor
  still missed (see *Measured with the draft answer*). The three Wardens abreast keep x 120/240/360: no data key
  spreads a hovering line (the sim's `spacing` for it named in *Open questions*).
- 2026-11-05 (step B2, M11 part B): the round-84 open questions decided as drafted (round 84 closed, the alternatives
  rejected): the secondary all 16 (14 of 16 only on easy); the glyph stones as the ring strip's paying ground (the
  Bomb Rack not `NO GROUND`); the length 268 s. The Warden's leaving tell is its own document's (round 84's cross-doc
  fix).
- 2026-11-05 (step B2, M11 part B; pending the user's review in round 89): the [data.yaml](data.yaml) from the hand
  tables: the pairs fly as a line abreast of 2 (x 160, 320); the swarms beside a Warden hold in the side or middle
  lanes; the t=188 line opens right to left (`line_order: right-left`); the glyph stones' clusters at t≈207 (after the
  t=188 line has risen) and t≈227 in the columns the t=218 pair leaves free; the Helix pod at t=176, x 120; the Choir's
  line on the enemy channel without `distorted` (clean); the music's `choir` stem as track 16's third layer from
  t=208 to t=262; twenty offbeat Skitter trickles and `small_act_hp` for the Needlers and Stingers (the floor);
  `bounty_scale` 0.25 (typical 12,100, +0.8 %).

- 2026-11-04 (Act 7 design phase, step `m11-d-levels-1`; decided 2026-11-05, round 84): promoted from the
  act roster to a draft level document, hand tables only, no `data.yaml`. Space, all from the front; five sections,
  268 s, 496 enemies (≈ 111 a minute); 16 Gate Wardens in seven groups (one, pairs, lines of three; straight and
  aimed), the secondary all of them (`WARDENS n / 16`); the glyph stones as the ring strip's paying ground; the Helix
  pod as the secret (the 2179 survey's lore); the Steadfast's last supply drone; the Choir's first Act 7 line (the
  door); the radio's noise step 1; the transit by Q11 a ending the level at t=268; `bounty_scale` 0.27; Vorne
  silent.
- 2026-11-04 (step `m11-d-act`, decided 2026-11-05, round 84): the story step's wording: the briefing's no-cover sentence names
  the fleet split (the bombers hold the near side with the line; the Lance can't reach past Neptune), t=262
  "Steadfast, follow Aegis through.", the Choir's door line the act's first in play (the intro's page 3 is heard
  first).
- 2026-11-04 (step `m11-d-act`, decided 2026-11-05, round 84): the music fades out over 1.5 s under the transit's sound (no held
  chord cut by the warp), the `choir` stem from the door line, the `vrell-gate` bed (D-audio's cues).
- 2026-11-05, round 84 (the user's page answers; decided as drafted unless stated, the alternatives rejected). The open items were:
  - **Q2** (round 84, the Gate Warden's shell): drafted **a** (invulnerable while closed, open while it fires,
    `shield-breaker` or the EMP open it 3 s); **b** an Act 5 hex pool every weapon wears down; **c** Act 6's mirror
    while closed. The [enemies' open questions](../../../enemies/README.md#open-questions) hold it.
  - **Q11** (round 84, the gate transit): drafted **a** (a 1.5 s smooth warp, no flash or inversion, the level ending
    inside it); **b** a 0.5 s full colour inversion as the world's sketch said (a photosensitivity risk); **c** a cut to
    a title-card style still.
- 2026-11-05 (step B5, the part's close-out; pending the user's review in round 89): the radio retimed to the 1 s rule
  (Rook t=10 → 13, Varga t=19 → 25 and t=29 → 37, Okafor t=46 → 48, t=248 → 245 and t=262 → 263; each line's trigger
  and meaning kept, Varga's shell line now in the first Warden's quiet window); both briefing pages shortened to one
  screen (`BriefingLayoutTest`: 4 lines; the same facts, plainer; "no bombers or lance past Neptune" for the no-cover
  sentence) and re-voiced on the keys' seeds (Whisper read back word for word); the backdrop's ring pieces kept from
  showing a cut edge (`BackdropAssetsTest`): the near arc (`ring-top`) and the inner rim held 2 px above the screen's top
  edge, the arc's band ending 4 px inside its canvas, and the mouth's canvas 560 px tall so its whole ring fits
  (`tools/art/backdrop_l43.py`).
