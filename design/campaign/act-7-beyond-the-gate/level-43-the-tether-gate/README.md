---
title: Level 43 – The Tether Gate
design: approved
implementation: not-started
art: none
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

> **Commander Okafor:** "The Tether Gate. Vorne went through it, and the Vrell came out of it. The fleet goes after
> him. Things grown out of the ring's own stone hold its mouth: Aegis Wing clears them, then leads us through. The
> bombers hold this side with the line, and the lance can't reach past Neptune. Rook flies your wing."
>
> **Dr. Varga:** "The wardens are sealed in a shell. Gunfire sparks off it and does nothing. They open for a breath
> when they fire their line, and the coil's arc cracks the shell for longer. This ring was dead for forty-four years.
> Something on the far side woke it. When you cross, space itself will bend. Fly your ship, not the stars."

*Images* (production art in M11's build, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-43-the-mouth`: a CDF chart of the ring seen from the fleet's approach, nearly edge-on; the
  Warden stations at its mouth as crimson dots in arcs, the *Steadfast* group at the bottom, the wing's lane a dotted
  line into the ring's centre, "NEPTUNE +0.4 AU" in the corner.
- Varga's page `level-43-gate-warden`: her scan of a Warden in grey outline, twice: shell closed (a pale gold ovoid)
  and open (six petals folded back, the eye crimson); under it a timeline bar, "1.8 s OPEN / 4.0 s".

*Hangar teaser* (shop screen before L43, the act's first; Rook speaks, not voiced):

> **Rook:** "One door, no help on the far side. Pack everything, Lancer."

The hangar before L43 adds the **Bastion Shield** to the shop ([shields](../../../player/shields/README.md)); the plan
buys the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md) **L4** (the level's `shield-breaker`, the
arc's reach 170 px) and flies 23.5 of 25 MW
([Act 7 hand plan](../../../player/generator/README.md#act-7-hand-plan-draft)). The intel panel's `SPECIAL LIMITED`
warning names the Airstrike and the Orbital Lance if fitted (`NO COVER` from this level on, the
[Act 7 specials](../../../player/specials/README.md#act-7-specials-draft)); the plan's EMP Burst is unaffected.

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `space` (the Gate Wardens at their stations, the Vrell swarms, Chimera wings, Harrow streams); `ground` in section 4 only: the ring's surface and its harmless **glyph stones** |
| Attack directions | front 100 % |
| Density | 4 |
| Recommended traits | `forward`, `shield-breaker` |
| Hazards | The Wardens' `laser-line`s (straight down and aimed); the Harrows' burst rings; the gate transit (scripted, harmless) |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: the Tether Gate, `space`, front · **L1**: + density 4, the hazard "laser lines", the objective `CLEAR THE GATE`, the secondary `WARDENS 0 / 16`, `SPECIAL LIMITED` (`NO COVER`) · **L2**: + Gate Warden (new), Chimera, Harrow, Needler, Stinger, Skitter and Mote portraits · **L3**: + `forward` and `shield-breaker` highlighted, the wave strip marking the seven Warden groups |

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

| Section | t (s) | Scroll (px, approx.) | Speed (px/s) | Layers and content | Purpose |
|---|---|---|---|---|---|
| 1. The Mouth | 0–48 | 0–5,760 | 120 | `deep`: the ring small ahead, the nebula; the *Steadfast* group's running lights behind (bottom of the screen at the start, scrolling off by t≈20); `low-air`: drifting spore sacs | Vrell popcorn; the Warden's intro alone at t=36 (quiet window t=33–40) |
| 2. The Pickets | 48–124 | 5,760–14,120 | 110 | `deep`: the ring grows to a third of the screen, its glyphs readable; `low-air`: dead tendrils | Warden pairs (straight, then aimed); Chimeras and Harrows; the supply drone (t=118) |
| 3. The Ring's Shadow | 124–200 | 14,120–21,720 | 100 | `deep`: the ring fills the top of the screen, the nebula behind it; the light dims a step | Three Warden lines of three; the Helix pod (the secret, t≈176) |
| 4. Along the Ring | 200–256 | 21,720–29,560 | 140 | `ground`: **the ring's surface** (basalt and glyph channels) with the glyph stones (t≈204, t≈228); `deep`: the ring's inner curve rising like a horizon; the Choir's line (t=208) | The last Warden pair (t=218); the run to the ring's centre; the last wave at t=246 |
| 5. The Transit | 256–268 | 29,560–31,240 | 140 | `deep`: the event plane fills the ring's centre ahead, pale violet; the scripted formation from t=262; the **warp** t=266.5–268 | ≥ 4 s of calm (the last wave gone by ≈ t=253), then the gate transit; level end at t=268 inside the warp |

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
  [beam era](../../../enemies/README.md#balancing-basis)'s ×2 as built (the Chimera 40, the Harrow 16); the Skitters and
  Motes are `tiny`. No returning `medium` unit flies here, so no act HP factor applies.
- **The Vrell's own** (round 72's turn): the Chimeras and Harrows are the Vrell's hybrids now; no Ascendancy unit
  flies (the faction is broken). No unit carries a hex shield (J4).
- **Density and pacing** (the Act 2+ floor from the start): **496 enemies** over 268 s, **≈ 111 a minute** at medium
  (the act's minimum 80; Act 6's band 100–118). A wave every 3 s from t=3 to the last at t=246, gaps of 5–6 s only
  around the Warden entries. Waves by direction: front 76 of 76 (100 %). The floor comes from the Needlers' thorns,
  the Chimeras' aimed pairs, the Stingers' fans, the Harrows' rings and the Wardens' lines: expected to meet ≤ 3
  pauses over 2 s, ≥ 2.5 enemies on the screen and ≥ 10 shots a minute (`PacingTest`, `ActFloorTest`), **to be
  confirmed with `levelReport`** in the build.
- **The Wardens' windows** (its document's *Difficulty and level use*): the first one alone, then pairs, then lines
  of three; at most 4 on the screen and 2 lines telegraphed at once; a straight-down line never on a column a
  neighbour's line holds. While a group holds its stations the popcorn is thin (swarms of 8 kept off the Warden
  columns, small snakes) so nothing pins the ship under a straight-down line; no Harrow stream enters within 3 s of a
  Warden group's entry.
- **Spacing** (the Act 5 lesson): the Wardens are the level's only `medium` hulls; in a group they keep ≥ 80 px between
  hull edges, and no two groups share the screen (each group is gone before the next enters: 14 s holds, ≥ 26 s
  apart). At most two Chimera groups on the screen ([the Chimera](../../../enemies/air/chimera/README.md)'s rule), at
  most 6 Harrows.
- **Calm at the end** (the Act 5 lesson): the last wave (a Skitter snake) enters at t=246 and is gone by ≈ t=253;
  **≥ 9 s** of calm before the formation at t=262, ≥ 13 s before the level end.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | Out of the ring's shadow; the Steadfast group's running lights behind on `deep` |
| 6 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 9 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 12 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 15 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 18 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 21 | 1 | line abreast | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 24 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 27 | 1 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 30 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | The last wave before the quiet window |
| 36 | 1 | single | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 1 | front | **The Gate Warden's first appearance**: alone, straight-down line, quiet window t=33–40 (Varga's line ahead of it); drops the Shield cell |
| 41 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Kept off the Warden's column |
| 44 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 48 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 5 | front | The Vrell's hybrids, home with them |
| 51 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 54 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 57 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 62 | 2 | pair | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 2 | front | Straight-down lines, windows 1.3 s apart (left first) |
| 65 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 68 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front | From the top edge, alternating its halves 0.6 s apart |
| 71 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 74 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 77 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 80 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 83 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 86 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 89 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 94 | 2 | pair | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 2 | front | **Aimed** lines: stand off the line |
| 97 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 100 | 2 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front |  |
| 103 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 106 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 109 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 112 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 115 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front |  |
| 118 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | The Steadfast's supply drone at t=118 (the Armour patch) |
| 121 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 125 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 128 | 3 | line abreast | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 3 | front | The first line of three: straight down, opening left to right; the middle one drops the Overdrive |
| 131 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 134 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 4 | front |  |
| 137 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 140 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 143 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 146 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 149 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 152 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 4 | front |  |
| 155 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 158 | 3 | line abreast | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 3 | front | Aimed lines, opening from the middle out |
| 161 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 164 | 3 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front | The Chimera swarm; at most two Chimera groups on the screen |
| 167 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 170 | 3 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | front |  |
| 173 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 176 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 179 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 182 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 188 | 3 | line abreast | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 3 | front | Straight down, opening right to left |
| 191 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 194 | 3 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 197 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 200 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Over the ring's surface (the `ground` strip) |
| 203 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 5 | front |  |
| 206 | 4 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front |  |
| 209 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 212 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 218 | 4 | pair | [Gate Warden](../../../enemies/space/gate-warden/README.md) | 2 | front | The last Wardens, aimed; the secondary is decided when the second is gone (≈ t=236 at the latest) |
| 221 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 224 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 227 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 230 | 4 | column | [Stinger](../../../enemies/air/stinger/README.md) | 5 | front |  |
| 233 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front |  |
| 236 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front |  |
| 239 | 4 | stream | [Harrow](../../../enemies/air/harrow/README.md) | 6 | front |  |
| 242 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 7 | front |  |
| 246 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 10 | front | The last wave; gone by ≈ t=253, calm to the transit |

Totals: Skitter 170 · Mote Swarm 168 · Chimera 50 · Needler 40 · Harrow 30 · Stinger 22 · Gate Warden 16 (= 496);
plus the 8 glyph stones (not enemies).

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
his scripted lines (t=10, t=257) do not. **Vorne is silent** (he went through ahead of the fleet).

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "Aegis, Steadfast. The gate's mouth is yours. Clear its wardens, then lead us through." |
| t=10 | Rook | "Dead for forty-four years, and the day Vorne needs a door, it wakes up. Lovely." |
| t=19 | Varga | "The stone things on the ring are new. Sealed shells. They only open when they fire." |
| t=29 | Varga | "Shoot when the red line shows. Or break the shell with the coil's arc." |
| t=46 | Okafor | "Every warden you leave stands between the fleet and the gate. Leave none." |
| The first Warden killed by Rook (event, bark, `requires: escort`) | Rook | "Cracked one. They don't like the coil." |
| t=78 | Varga | "Chimeras and Harrows. The Vrell brought Vorne's hybrids home with them." |
| t=90 | Okafor | "Aimed lines now. Don't wait in them. Shoot from where the beam isn't." |
| t=118 | Okafor | "Supply drone inbound from Steadfast. The last one you'll get past the ring." |
| t=130 (bark, `requires: escort`) | Rook | "Three at once. Fine. I'll take the left one, you take the other two." |
| The Helix pod broken (event) | Varga | "Helix survey markings. 2179. So this is where they went." |
| t=196 | Okafor | "You're over the ring itself. Our signal gets worse past here. Stay together." |
| t=208 (enemy channel) | the Choir | "*…small bright one… why do you stand in the door…*" |
| t=215 | Varga | "That was the Choir. It has never asked us anything before." |
| Secondary met (`secondary-objective`, the 16th Warden down) | Okafor | "No wardens left at the mouth. The fleet can pass." |
| Secondary failed (a Warden rises out) | Varga | "One went back into the ring. The fleet will meet it at the mouth." |
| t=248 | Okafor | "The ring is lighting, Aegis. Form up on the centre. We're right behind you." |
| t=257 | Rook | "Here we go. Stay close, Lancer." |
| t=262 | Okafor | "Steadfast, follow Aegis through." |

Timeline check (text): the timed lines close at ≈ 9.6, 18.1, 27.4, 36.3, 53.6, 85.4, 97.2, 125.8, (137.2), 203.8,
213.3, 221.3, 255.8, 261.1 and 266.2; nothing overlaps, and the last closes before the warp (t=266.5). The secondary
resolves between ≈ t=221 and t=236 (the last pair), in the gap between t=221.3 and t=248; a failure earlier waits
up to 6 s for a free slot. Varga's t=19 and t=29 lines come ahead of the first Warden (t=36). The Choir's line is
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
**Act 1 terms** (Skitter 5, Mote 2, Needler 12, Stinger 15, Chimera 14, Harrow 12, Gate Warden 40, the glyph stone
15, the crate 100, the secondary 100), × the act factor **16.777** (1.6⁶), bounties (the stones too) also × the
scale. A **`bounty_scale` of 0.27** (the economy's shares: air kills 60 %, ground targets 80 %, the secret and the
secondary 50 %; a first estimate for the build to confirm with `TypicalHaulTest` and `levelReport`) puts the typical
haul at **12,158 (+1.3 %)**, the perfect run at 20,641 (1.72 × budget). No boss or mid-boss.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 170 × 5 + Mote Swarm 168 × 2 + Chimera 50 × 14 + Needler 40 × 12 + Harrow 30 × 12 + Stinger 22 × 15 + Gate Warden 16 × 40 (3,696) | 16,742 | 10,045 |
| Ground targets: 8 glyph stones × 15 | 544 | 435 |
| Secret: the Helix pod (hidden crate) | 1,678 | 839 |
| Secondary: the Wardens | 1,678 | 839 |
| **Total** (bounty scale 0.27) | **20,641** | **12,158** |
| Budget(n) = the typical haul's target; typical +1.3 %, perfect 1.72 × budget | | 12,001 |

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

No art yet. Round 84's picks (decided 2026-11-05): the Gate Warden's sheets in its document (ART-R-A, over this level's brightest and
darkest band); Q10 b's one-palette sheet (the light of this level) and Q11's transit storyboard a/b in
[Vrell space](../../../world/vrell-space/README.md) and the act's title-card still (A.9 b, "the mouth") from this level's backdrop
(J1) in the [act](../README.md) (ART-R-B). Production backdrop (the ring on `deep` at three scales, the fleet's
lights, the ring's surface strip and the glyph stones, the Helix pod), the briefing images and the warp effect come in
M11's build.

## Implementation

- [ ] Level data `data.yaml` (M11's build): sections and speeds with 2 s eases, waves, radio, budget, threat profile,
      briefing, backdrop, music; `medium: vacuum`, the `beyond-gate` limits (`NO COVER`)
- [ ] The Gate Warden secondary (`escapes: gate-warden`, `allowed: 0`, `WARDENS n / 16` counting to its target, the
      well's amber blink while a Warden shows its leaving tell; easy 14 of 16, hard 18 of 18)
- [ ] The glyph stones: 8 level-owned `ground` destructibles in section 4, paying, not counted as enemies
- [ ] The gate transit: the scripted formation from t=262 (no input, no hits, Rook beside), the 1.5 s warp post-effect
      (radial pull, smooth grade to violet, no flash or inversion), the level ending inside it and the debrief over
      the violet; Level 44 opening in it
- [ ] Wave script: ≥ 80 enemies a minute at medium (`DensityTest`), the Act 2+ floor (`PacingTest`, `ActFloorTest`),
      the quiet window t=33–40, ≥ 4 s calm after the last enemy, no two `medium` hulls overlapping, the Wardens'
      limits (4 on the screen, 2 lines telegraphed)
- [ ] Radio voiced with the step-1 noise, every timed line at most 1 s late (`RadioTimelineLevel43Test`, with Rook and
      without); Rook's barks `requires: escort`; the Choir's line on the clean enemy channel
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`); the
      Gate Warden's portrait; `TraitCoverageTest` covering Level 43
- [ ] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images; the act's title
      card and intro before it once (`act-intro:7`)
- [ ] Typical haul within ±5 % of budget(43) = 12 001 with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music and ambience: track 16 by section, the `vrell-gate` bed, the level's sounds (`Level43SoundsTest`)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot (timing a window, the arc on a Warden) flies the
      level with Rook on every difficulty and with `--escort none`
- [ ] Production art and captures for the build's round

## Open questions

- **The secondary's strictness**: drafted all 16 at medium (the gap analysis's "no Gate Warden survives"); the
  alternative is 14 of 16 as on easy, if the build's autopilot finds the last line of three too tight with the Mk III.
- **The Warden's leaving tell** (2 s, glyphs dimming, the top tilting; the objective's readability): proposed here for
  the [Gate Warden](../../../enemies/space/gate-warden/README.md)'s document, which does not have it yet (D-act's
  cross-doc fix).
- **The glyph stones**: added by this draft so the ring's `ground` strip (the world's) is worth something and the
  Bomb Rack is not `NO GROUND`; the alternative is the strip as scenery and the level `NO GROUND`.
- **Length**: 268 s ("near 5 minutes" is the act's target; Act 6's levels ran 190–220 s): the build's `levelReport`
  may stretch a section if the pacing allows.

## Decisions

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
