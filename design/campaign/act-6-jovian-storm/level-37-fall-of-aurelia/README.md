---
title: Level 37 – Fall of Aurelia
design: approved
implementation: not-started
art: none
depends-on: [../../../enemies/ground/graft-turret, ../../../enemies/air/mirror-interceptor, ../../../enemies/air/chimera, ../../../enemies/air/talon, ../../../enemies/air/gilded-gunship, ../../../enemies/air/hornet, ../../../enemies/ground/sam-nest, ../../../enemies/README.md, ../../../allies, ../../../world/jovian, ../../../player/generator, ../../../player/wingmen, ../../../ui/hud, ../../../story/characters]
updated: 2026-10-31
---

# Level 37 – Fall of Aurelia

## Summary

**Aurelia**, the Ascendancy's gilded capital, hanging from balloons and lift rings in Jupiter's cream cloud. Its
traffic control breaks with Vorne and asks the CDF to get its civilians out: three pairs of evacuation shuttles lift
off the decks (marked friendly) while Aegis Wing clears their lanes of Talons, the act's first
[Mirror Interceptors](../../../enemies/air/mirror-interceptor/README.md) and the overgrown
[Graft Turrets](../../../enemies/ground/graft-turret/README.md) on the decks. At **t=100 the Vrell turn**: the Choir
says the door is theirs, Vrell growth bursts over the gold, and from then on chosen Vrell waves hunt the
Ascendancy's craft (the three-way war). In the last 40 s **the city falls**: the balloons tear, the deck breaks along a jagged
fault and the halves sink, and the wing flies down beside the spires into open cloud while Okafor remembers 2166. The act's
`shield-breaker` level. 200 s, ≈ 114 enemies a minute at medium (ground units included).

**Status: approved 2026-10-31 (round 72, D.3)** (drafted in the Act 6 design phase, step `m10-d-levels-1`, 2026-10-31). Written from the act roster, the gap analysis's recommended answers (Q1 c, Q3 a with c's
backdrop, Q4 a, Q7 b, Q9 a, Q10 a; J1, J2, J5, J6, J10) and the wave 1 documents (the units above,
[reflect](../../../enemies/README.md#reflect), [regeneration](../../../enemies/README.md#regeneration),
[the three-way war](../../../enemies/README.md#the-three-way-war), the Act 6
[enemy shields](../../../enemies/README.md#enemy-shields), the [Jovian system](../../../world/jovian/README.md)'s
falling city and moods, the [Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft),
[Rook in Act 6](../../../player/wingmen/README.md#rook-in-act-6-draft), the [allies](../../../allies/README.md#evacuation-shuttle)'
shuttle). There is **no `data.yaml`**: the build (M10 part C) turns the hand tables into one; no number is measured.

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Texts (draft):

> **Commander Okafor:** "Aurelia. The Ascendancy's capital, floating on balloons and lift rings. Its traffic
> control has broken with Vorne and asked us to get the civilians out: shuttles lift from the pads in three
> waves. Clear their lanes and keep them flying. I have been to Aurelia before, Lancer. This time we are there
> for its people."
>
> **Dr. Varga:** "The deck turrets are overgrown. Kill one and a root shows under it: burn the root inside five
> seconds, or the turret grows back. And a new interceptor carries a mirror on its nose. Shots bounce off it, a
> beam heats it until it cracks, the Tesla pod shatters it outright. Its flanks are open."

*Images* (production art in part C, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-37-aurelia`: a CDF chart of Aurelia from above, the ring of gilded decks round the Spire
  Plaza, the balloon decks and lift rings at its far edge, the three shuttle pad pairs numbered with their lanes out
  in green, the wing's route across the city in white.
- Varga's page `level-37-mirror`: her scan of a Mirror Interceptor from above, its 120° arc drawn as a silver fan and
  the open flanks marked; an inset of a Graft Turret's wreck with the root exposed and a "5 s" timer ring.

*Hangar teaser* (shop screen before L37; Rook speaks, not voiced):

> **Rook:** "Mirrors on their noses now. The Tesla pod cracks glass, Lancer. Just saying."

The hangar before L37 adds the [Reflex Shield](../../../player/shields/README.md#reflex-shield-act-6) to the shop
(Q7 b); the plan fits the **Tesla Coil Pod** at L2 on the left wing, the left Micro-missile Pod to the inventory, the
act's `shield-breaker` moment ([Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft): 22 of
22 MW, forward / reference 1.14). Rook keeps his Autocannon.

## Threat profile

Hand-written (draft); directions are counted by each wave's `from`.

| Field | Value |
|---|---|
| Dominant layers | `ground` (Aurelia's decks: Graft Turret nests, SAM Nests), `air` (Talon wings, Mirror Interceptors, Chimeras, a Gunship and a Hornet at a time, Mote Swarms, Skitter snakes; Lampreys after the turn) |
| Attack directions | front 89 % · sides 9 % · rear 2 % |
| Density | 4 |
| Recommended traits | `anti-ground`, `spread`, `shield-breaker` |
| Hazards | Mirrors (bounced shots); regrowing turrets; the Vrell turn (t≈100); the falling city (the last 40 s) |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: Jupiter, Aurelia, `air` + `ground`, front · **L1**: + directions (sides, rear), density 4, hazards "mirrors" and "regrowth", the objective `CROSS AURELIA` and the secondary `SHUTTLES 0 / 5` · **L2**: + Mirror Interceptor (new), Graft Turret (new), Talon, Chimera, Gilded Gunship, Hornet, SAM Nest, Skitter and Mote portraits · **L3**: + `anti-ground`, `spread` and `shield-breaker` highlighted, the wave strip marking the shuttle pairs and the mirrors' entries (the turn is not on it: the sensors do not see it coming) |

*Varga's intel line* per sensor-suite level (must fit `IntelPanelLayoutTest`):

| Sensor suite | Line |
|---|---|
| none | "Aurelia, Lancer. The Ascendancy's capital, on balloons." |
| L1 | "Ahead and both sides. Civilians lifting off: keep them safe." |
| L2 | "New: grown deck turrets, mirror-nosed interceptors." |
| L3 | "Spread and bombs for the decks. Tesla breaks the mirrors." |

## Objective

- **Primary** `reach-end`: cross Aurelia to open cloud beyond it. The city's fall (the last 40 s) cannot fail it.
- **Secondary** (the shuttles): Aurelia's six evacuation shuttles, **Halcyon One to Six** (names of this draft),
  lift off in three pairs (t=44, 72 and 127). The secondary is met when **5 of the 6** have climbed out off the top
  edge; it fails at once when a second shuttle is lost. The HUD counts to its target (the Act 5 lesson): the one-line
  tracker `SHUTTLES n / 5`, n the shuttles out, with six small pips (white waiting, green out, red lost), `DONE` in
  green at 5, `FAILED` in red at the second loss, the box flashing. It can only be met with the third pair (≈ t=143),
  after the turn. Pays **100** credits in Act 1 terms (**1,049** in Act 6, the act factor 10.486); the typical haul
  counts 50 %. Easy: 4 of 6 (`n / 4`); hard: all 6 (`n / 6`).

### The shuttles

Six [evacuation shuttles](../../../allies/README.md#evacuation-shuttle) (Level 10's, final art) in Aurelia's civil
livery: white with a gold band and the city's crest in place of the CDF evac stripe (a palette variant, part C's
production art). The allies' common rules hold: player fire never hurts them, no contact damage to the ship.

- **Flagged `neutral`**: the HUD's [friendly marker](../../../ui/hud/README.md#in-the-play-field) opens its
  "FRIENDLIES INBOUND — HOLD FIRE" banner 2 s before each pair lifts and brackets each shuttle while it is on the
  screen (the Act 5 lesson: neutral craft clearly marked).
- **Liftoff** (5 s, Level 10's): each pair stands on its pads in the lower half of the screen (x 190 and 290) as the
  deck scrolls in and climbs to its station, drawn from the ground layer's scale up to the air scale; **untouchable**
  throughout.
- **Station** (8 s): the pair holds one behind the other in the middle of the field, at (240, 170) and (240, 240)
  px below the top edge, drifting on Level 10's lane sway (±16 px), so the hulls stay inside x 192–288, clear of
  the Mirror Interceptors' side stations (their hulls within 166 px of an edge); then it **climbs out** off the top
  edge over ≈ 3 s, accelerating. About 16 s on the screen a pair.
- **What hurts them**: every enemy bullet that touches a hit box (by its class), an `air` enemy's body (by its tier,
  a `tiny` or `small` one destroyed), as in Level 10. Three waves aim at them through the
  [target-the-objective hook](../../../enemies/README.md#target-the-objective-hook) (t=46, 74 and 130: Talon pairs
  aimed at the nearest shuttle of the pair on its station); the rest aim at the ship, and the Skitter snakes fly
  round the band. **Never prey**: the Vrell never hunt allies (the [three-way rule](../../../enemies/README.md#the-three-way-war)).
- **Armour** 120 (easy 180, hard 90); smoke and an amber pip below 50 %. **Lost**: it loses power and glides down
  into `far` over 3 s (Level 10's presentation), no debris on the play plane.
- **Tuning**: armour and the aimed waves are tuned until the autopilot, which does not guard them, gets 5 of 6 out
  in most medium runs (Level 10's rule); a per-shuttle `damage_factor` is the fallback.

## Layout

Scroll **120 px/s** over the outer rim, **100 px/s** over the gilded decks, **80 px/s** in the Spire Plaza (the turn
stays on the screen), **100 px/s** after the turn and through the fall; each change eased over 2 s. The setting is
the [Jovian system](../../../world/jovian/README.md#sub-locations-per-level)'s Aurelia in Q10 a's **L37 grade**
([moods](../../../world/jovian/README.md#moods-per-level-q10)): gold and white over warm cream cloud, the decks a step
paler and less saturated than the units' gold (gold on gold); after the turn the sky a step darker, fire-orange and
teal, the Vrell bullets the larger, brighter ones over the teal growth. Media: Jupiter's **air** (J2). Motion budget:
the shuttles, the mirrors' specular sweep and, after the turn, the spreading growth are the strong elements; the
balloons sway slowly.

| Section | t (s) | Scroll (px, approx.) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Aurelia's Rim | 0–30 | 0–3,600 | 120 | clear, warm | `ground`: the outer decks, gilded rails, a deck edge with the first Graft Turret nest (t≈14), a SAM platform (t≈24); `low-air`: balloon cables, the first anti-grav ring; `deep`: the city's far side and its balloons. | Popcorn and Talons; the regrowth lesson on one nest. |
| 2. The Gilded Decks | 30–70 | 3,600–7,600 | 100 | clear | `ground`: domes and promenades, the shuttle pads (pair 1, t=44), a dome rim nest (t≈48), two deck edges either side of the pads (t≈62), the 2166 CDF locker under an awning (t≈60, the secret); `low-air`: balloons. | The first mirrors (t=38, quiet window t=36–42); shuttle pair 1. |
| 3. The Spire Plaza | 70–100 | 7,600–10,000 | 80 | clear | `ground`: the plaza round the great spire, its terrace nest (t≈79), pads (pair 2, t=72), SAM Nests under the spire (t≈95); teal growth thickening on the dome seams. | Shuttle pair 2, the 2 + 2 mirrors, Varga's codes; the window before the turn. |
| 4. The Turn | 100–158 | 10,000–15,800 | 100 | darkening, smoke | `ground`: burning decks overgrown with teal growth, a nest on a burning deck (t≈128), the last SAM platform (t≈138), the balloon deck's edge (t≈144), pads in the growth (pair 3, t=127); `low-air`/`deep`: Vrell and gold craft fighting, hulks falling. | The prey links; shuttle pair 3; the rear ambush. |
| 5. The Fall | 158–200 | 15,800–20,000 | 100 | smoke, then open cloud | `ground`: the two falling halves of the deck (t=160–180: each slows, darkens, shrinks at its own rate), then none; `low-air`: spires rising past, debris, a torn balloon; `deep`: the sinking city, the bands. Level end at t=200 over open cloud. | The set piece; the last waves out of the debris; calm to the end. |

### The Vrell turn (t=100)

The moment the act turns ([the three-way war](../../../enemies/README.md#the-three-way-war), Q3 **a** with **c**'s
backdrop). **Set at t=100** (the gap analysis's ≈ t=100, the working note's open item): the second shuttle pair is out
by ≈ t=88, the Spire Plaza (section 3) is on the screen at 80 px/s, and 100 s leave the turn 58 s of hunting before
the fall.

- **Before it** (t=0–100): the Vrell (Motes, Skitters) and the Ascendancy (Talons, Mirror Interceptors, the
  Gunship, the Hornet) and its hybrids (Chimeras, Graft Turrets) fly side by side; no unit hurts another. The Vrell
  growth already creeps over the decks (the [Graft Turrets](../../../enemies/ground/graft-turret/README.md), teal
  veins on the gold), which Okafor notices at t=88.
- **The window** (t=97–103): no new wave; the t=91 Talons, the t=94 Skitters and the t=95 SAM Nests are still on the
  screen (the floor holds). At **t=100** the Choir speaks (two-second static lead, then its line); the Vrell growth
  **bursts over the decks** (teal tendrils racing along the seams of the plaza from the dome rims inward over 4 s,
  plates buckling), fires break out on the decks and the sky darkens a step over 4 s (Q10 a's L37 grade: after the
  turn fire-orange and teal); the music changes (below).
- **From it** (t=103 on): four **prey links**, each a Vrell hunter wave entering a second or two after its prey
  (Level 33's pattern): the t=104 Lampreys latch on the t=103 Talon wing; the t=118 Skitters ram the t=116 Gunship;
  the t=134 Motes lunge at the t=133 Hornet; the t=170 Skitters ram the t=169 Talon wing. A hunter keeps to its
  prey while any of it is on the screen, then flies at the ship. A hunter's hit on a prey flashes teal; a prey the
  Vrell kill pays the player nothing; hunters and prey stay hostile and unmarked.
- **Never prey**: the hybrids (the t=113, t=147, t=165 and t=178 Chimeras, the Graft Turrets) and the Mirror
  Interceptors of this draft (they keep their mirrors on the ship); the Darts a prey Gunship launched are not prey.
- **The backdrop** (c on top): on `deep` and `low-air`, Vrell swarms and gold Ascendancy craft fight beyond the play
  plane (no hit boxes), burning hulks fall past; the growth keeps spreading over the decks section by section.
- **Rook**: his rules for the war, the mirrors and the fall are [Rook in Act 6](../../../player/wingmen/README.md#rook-in-act-6-draft)'s.

### The falling city (t=160–200)

The Jovian world's signature set piece ([hazards](../../../world/jovian/README.md#hazards--set-pieces), Q4 **a**),
placed here:

- **t=150**: Aurelia Traffic's last call (the lift rings failing); the last ground nest (t=144) has scrolled off by
  ≈ t=151, so **no ground unit is on the screen when the fall begins** (the world's rule: units on falling decks
  would stop firing and leave unpaid; this draft places none there).
- **t=160**: the balloons tear and collapse (fabric tearing, no pop), the main deck **breaks along a jagged fault line**
  (A.13 b: an authored, ragged cut through torn plating, never a straight edge), the two halves drift apart and drop at their
  own rates and the spires topple. Over **20 s** (t=160–180) the two city layers each slow, darken and shrink toward the field's centre while
  the ship and Rook fly down beside the spires (tall gold spires rising past on `low-air`, scenery without collision),
  with `low-air` debris (deck plates, cables, a torn balloon) and Vrell in the air around them.
- **t=180–200**: open cloud below the play plane; the city a dark, burning shape sinking into the bands on `deep`,
  smoke rising from it. The waves of section 5 come out of the debris; the last enters at t=186.
- **Calm** (the Act 5 lesson): the last wave is gone by ≈ t=193, **≥ 4 s** before the level end at t=200; Varga's
  t=176 line and the fall's sound carry the end.
- Okafor's Aurelia line plays over the drop (t=161). A renderer set piece (two scaled ground layers and the authored cut line) and a production backdrop part (the gap
  analysis §2.10: large, now two layers), M10 part C; the storyboard is chosen in round 72 (A.13 b, [the world](../../../world/jovian/README.md#concept-art)).

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). **Draft: unmeasured.**

- **HP** from the proposed Act 6 reference DPS (J1: **148** at L37,
  [balancing basis](../../../enemies/README.md#balancing-basis)): the Mirror Interceptor and the Graft Turret are new
  here (their documents: 120 HP and the mirror's heat; 12 + 8 flown at 24 + 16); the Chimera keeps its 40 (`small`,
  no act factor); the Talon its Act 6 shield of 24 (leader 48) over 32; the returning `medium` units take the act HP
  factor and the doubled [shield](../../../enemies/README.md#enemy-shields): the Gilded Gunship ×148/124 (hull ≈ 105,
  shield ≈ 29), the Hornet ×148/128 (hull ≈ 111, shield ≈ 28); the SAM Nest and the Lamprey are `small` (beam era ×2:
  20 and 6); Motes, Skitters and Darts are `tiny`.
- **`shield-breaker` first strongly rewarded** (the campaign's loadout-pressure table): the plan's Tesla Coil Pod
  empties a Talon's 24, a Gunship's 29 or a Hornet's 28 in one arc and **shatters a mirror** at once
  ([reflect](../../../enemies/README.md#reflect)); without it the beam burns a mirror in about a second and the wing
  mounts work the flanks.
- **Tiny units** (J5): the Vrell's [Mote Swarms](../../../enemies/air/mote-swarm/README.md) (112) and
  [Skitters](../../../enemies/air/skitter/README.md) (88) and the Gunship's Darts (≈ 12) carry the count; after the
  turn the same Motes and Skitters are the hunters.
- **Density and pacing** (the Act 2+ floor from the start): **381 enemies** (air waves 334 + an estimated 12 launched
  Darts + 35 ground units in nine nests) over 200 s, **≈ 114 a minute** at medium (air alone ≈ 104; the act's minimum
  75, the band 90–120); a regrown Graft Turret never counts again. A wave every 3–4 s from t=3 to the last at t=186.
  Waves by direction: front 50 of 56 (89 %), sides 5 (the three mirror pincers and the two Lamprey hunter
  streams, 9 %), rear 1 (the mirrors' ambush, 2 %): **non-front 11 %**, every side and rear
  entry **edge-warned 3 s**. The floor comes from the Talons' pairs, the mirrors' bursts and bounced shots, the
  turrets' pairs and the SAM Nests' diamonds: expected to meet ≤ 3 pauses over 2 s, ≥ 2.5 enemies on the screen and
  ≥ 10 shots a minute (`PacingTest`, `ActFloorTest`), **to be confirmed with `levelReport`** in part C. Quiet windows:
  t=36–42 (the first mirrors alone with the t=31 Skitters leaving) and **t=97–103** (the turn).
- **Spacing** (the Act 5 lesson): at most 4 Mirror Interceptors on the screen and 2 on one side; a pincer's hulls
  ≥ 80 px apart; no mirror station over the Hornet's hover (it holds above 140 px, the stations from 160 px) or the
  Gunship; the Gunship and the Hornet never on the screen together. No popcorn routed through a mirror's arc in front
  of it (the [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md#difficulty-and-level-use)'s rule).
- **Calm at the end**: the last wave enters at t=186 and is gone by ≈ t=193; ≥ 4 s of calm before t=200.

| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Vrell and gold side by side before the turn |
| 6 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 9 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) |  |
| 12 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Squad leader at the tip |
| 16 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The first Graft Turret nest (t≈14) on the deck edge below them |
| 19 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) |  |
| 22 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 25 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Two SAM Nests on the rim (t≈24) |
| 28 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | The hybrids of Level 36 over Aurelia |
| 31 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | The last wave before the quiet window |
| 38 | 2 | pincer | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 2 | sides | **The Mirror Interceptor's first appearance**: 1 + 1, quiet window t=36–42 (no popcorn in front of the mirrors); edge-warned 3 s ahead; Varga's line at t=34 |
| 43 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Routed down the middle third, clear of the mirrors' arcs; shuttle pair 1 lifts at t=44 |
| 46 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | **Aims at the shuttles** (the target-the-objective hook): pair 1 |
| 49 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | Routed round the shuttles' band (Level 10's rule) |
| 52 | 2 | single | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | About 6 Darts; drops the Shield cell |
| 55 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Squad leader; drops the Overdrive |
| 58 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The CDF locker (the secret) on a deck awning at t≈60 |
| 61 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front |  |
| 64 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) |  |
| 67 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 70 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Shuttle pair 2 lifts at t=72 |
| 74 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | **Aims at the shuttles**: pair 2; squad leader |
| 77 | 3 | single | [Hornet](../../../enemies/air/hornet/README.md) | 1 | front | Hovers above 140 px, clear of the shuttles' band and the mirrors' stations |
| 80 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) |  |
| 84 | 3 | pincer | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 4 | sides | 2 + 2, ≥ 80 px between hulls; edge-warned 3 s ahead |
| 88 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Routed down the middle third, clear of the mirrors' arcs |
| 91 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 94 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | The last wave before the turn: quiet window t=97–103 |
| 103 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 6 | front | **Prey** (squad leader): the t=104 Lampreys hunt it |
| 104 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | side (left) | **Hunters** of the t=103 wing: they latch on the Talons (edge-warned 3 s ahead) |
| 110 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | At the ship (the Vrell keep their own fire at the player) |
| 113 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Hybrids: never prey, they fight for the Ascendancy |
| 116 | 4 | single | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | **Prey**: the t=118 Skitters ram it; about 6 Darts (the Darts are not prey) |
| 118 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | **Hunters** of the t=116 Gunship: they ram it |
| 121 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 124 | 4 | rear ambush | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 2 | rear | Two lanes ≥ 80 px apart; edge-warned 3 s ahead at the bottom edge |
| 127 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Shuttle pair 3 lifts at t=127 |
| 130 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | **Aims at the shuttles**: pair 3; squad leader |
| 133 | 4 | single | [Hornet](../../../enemies/air/hornet/README.md) | 1 | front | **Prey**: the t=134 Motes lunge at it |
| 134 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | **Hunters** of the t=133 Hornet |
| 137 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) |  |
| 140 | 4 | pincer | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 2 | sides | 1 + 1; edge-warned 3 s ahead |
| 143 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 6 | front | **Prey** (squad leader): the t=144 Lampreys hunt it |
| 144 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | side (right) | **Hunters** of the t=143 wing (edge-warned 3 s ahead) |
| 147 | 4 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front |  |
| 150 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) |  |
| 153 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 156 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The last wave before the fall |
| 162 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The fall begins (t=160): Vrell out of the torn balloons |
| 165 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 7 | front |  |
| 169 | 5 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | **Prey** (squad leader): the t=170 Skitters ram them |
| 170 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | **Hunters** of the t=169 wing |
| 174 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Among the spires' debris on `low-air` |
| 178 | 5 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front |  |
| 182 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front |  |
| 186 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The last wave; gone by ≈ t=193, calm to the end at t=200 |

Totals: Mote Swarm 112 · Skitter 88 · Talon 69 · Chimera 41 · Dart Drone ≈ 12 (launched) · Mirror Interceptor 10 · Lamprey
10 · Gilded Gunship 2 · Hornet 2; the prey waves: 17 Talons, 1 Gunship, 1 Hornet; the hunters: 10 Lampreys, 16 Skitters,
8 Motes.

## Ground targets

Aurelia's decks (`ground`), placed by the time their nest's middle reaches the top edge. The
[Graft Turret](../../../enemies/ground/graft-turret/README.md)'s nests sit on deck edges and dome rims, where `area`
and `anti-ground` sweep them and the beam burns the roots; never more than 6 Graft Turrets on the screen (its rule).

| t (s) | Section | Formation | Target (link) | Count | Where |
|---|---|---|---|---|---|
| 14 | 1 | turret nest | [Graft Turret](../../../enemies/ground/graft-turret/README.md) | 3 | A deck edge on the outer rim: the first nest, alone (the regrowth lesson, Varga's t=11 line) |
| 24 | 1 | turret nest | [SAM Nest](../../../enemies/ground/sam-nest/README.md) | 2 | The rim's air-defence platform |
| 48 | 2 | turret nest | [Graft Turret](../../../enemies/ground/graft-turret/README.md) | 4 | A dome rim on the gilded decks |
| 62 | 2 | turret nest | [Graft Turret](../../../enemies/ground/graft-turret/README.md) | 5 | Two deck edges either side of the pads (pair 1 is out by then) |
| 79 | 3 | turret nest | [Graft Turret](../../../enemies/ground/graft-turret/README.md) | 6 | The Spire Plaza's terrace: a full nest |
| 95 | 3 | turret nest | [SAM Nest](../../../enemies/ground/sam-nest/README.md) | 3 | The plaza's far side under the spire, firing through the turn's window |
| 128 | 4 | turret nest | [Graft Turret](../../../enemies/ground/graft-turret/README.md) | 5 | A burning deck, the growth over its plates |
| 138 | 4 | turret nest | [SAM Nest](../../../enemies/ground/sam-nest/README.md) | 3 | The last platform before the balloon decks |
| 144 | 4 | turret nest | [Graft Turret](../../../enemies/ground/graft-turret/README.md) | 4 | The balloon deck's edge: the last ground units, off the screen by ≈ t=151 |

Totals: Graft Turret 27 (each with its root), SAM Nest 8. A Graft Turret pays once (15), its root a bonus (5); a
regrown turret pays nothing and never counts again.

## Hazards

- **Mirrors** (Q1 c, the [reflect](../../../enemies/README.md#reflect) rule): non-beam shots into a Mirror
  Interceptor's arc come back as slow magenta `small` bullets; the beam heats the mirror, the Tesla Coil Pod or the
  EMP shatters it. The level's spacing keeps popcorn out of the arcs, so the player can stop firing into a mirror
  without being overrun.
- **Regrowing turrets**: the [Graft Turret](../../../enemies/ground/graft-turret/README.md#regrowth)'s root window
  (5 s); a nest left half-done fires again.
- **The Vrell turn** and **the falling city**: above. Neither damages by itself; the spires and debris of the fall are
  scenery without collision.

## Secrets and pickups

- **The CDF locker** (t≈60, x 90, under a deck awning on the left of the pads): a sealed CDF supply locker left on
  Aurelia's decks in 2166, a small `ground` object (20 HP); broken, it frees a hidden crate worth **100** in Act 1
  terms (1,049 in Act 6). The Salvage scanner's glint marks it. Okafor's event line (her history, dropped after 6 s
  if the radio is busy).
- **Overdrive**: dropped by the t=55 squad leader.
- **Shield cell**: dropped by the t=52 Gilded Gunship.
- **Armour patch**: a [CDF supply drone](../../../allies/README.md#cdf-supply-drone) from the carrier releases one at
  (240, 380 y-up) at t=152, before the fall.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), drafted for the **1 s rule** (≈ 12 characters a second, a
1.5 s lead, 0.4 s of silence; event lines drop after 6 s); `RadioTimelineLevel37Test` checks them with Rook and
without. Rook's barks carry `requires: escort`; his scripted line (t=50, his pulp line of the level) does not.

**Speakers** (the Act 6 speakers are D-story's table): Okafor, Varga, Rook; **Aurelia Traffic** (J4: the act's
friendly generic voice, Aurelia's civil traffic controller on the **friendly** channel, steady at first and frayed by
the end; a voice and portrait for the round 72 auditions); **the Choir** (distorted, Level 33's dress, a 2 s static
lead; the music ducks under it); **Vorne** on the **enemy channel** (Act 5's intercepted dress: amber, the steady
interference, `INTERCEPT`), **cold, never shouted** (J6: his only shout is Level 42's).

| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Aurelia Traffic | "CDF flight, this is Aurelia Traffic. We have civilians on the pads. Please. Help us get them out." |
| t=11 | Varga | "Deck turrets ahead, grown over. Kill one and a root shows. Burn the root, or it grows back." |
| t=21 | Okafor | "Aurelia Traffic, Aegis copies. The shuttles fly, Lancer. Keep their lanes clear." |
| t=31 | Varga | "Interceptors, both sides. That nose is a mirror: shoot it and your shots come back. Hit the flanks." |
| The first bounced shot (event, bark) | Rook | "It's throwing your own shots back, Lancer. Go round it." |
| t=42 | Aurelia Traffic | "Halcyon One and Two lifting. CDF flight, they're yours." |
| t=50 | Rook | "Gold decks, gold domes, gold guns. Lancer, these people have a problem." |
| The CDF locker broken (event) | Okafor | "A CDF locker, from sixty-six. Take it. We left a lot behind on these decks." |
| t=66 | Aurelia Traffic | "Halcyon Three and Four next. They're boarding faster than we can launch." |
| A shuttle lost (event, the first time) | Aurelia Traffic | "We lost a shuttle! Aegis, keep them off the lanes!" |
| t=78 | Varga | "I'm inside Aurelia's traffic net. Their identification codes... Commander, I am keeping these." |
| t=88 | Okafor | "Take everything, Doctor. Lancer, the growth on those decks is moving." |
| t=100 (2 s static lead) | The Choir (distorted) | "…the one who opened… is no longer needed… the door is ours…" |
| t=110 | Vorne (enemy channel) | "All Ascendancy wings, this is Vorne. The Vrell are hostile. Burn them off the decks." |
| t=119 | Varga | "They're eating the city. They were never his partners, Lancer. Only a door." |
| A prey unit killed by a hunter (event, the first time, bark) | Rook | "They're eating their own side. Not complaining." |
| t=128 | Aurelia Traffic | "Halcyon Five and Six, launching through the growth. Aegis, please!" |
| t=136 | Okafor | "The Vrell are hunting the gold ones. Let them. Keep your guns on whatever fires at you." |
| Secondary met (`secondary-objective`, the fifth shuttle out) | Aurelia Traffic | "That's everyone we could load. Aurelia thanks you, Aegis." |
| Secondary failed (the second shuttle lost) | Okafor | "Two shuttles down. Keep the rest flying, Lancer." |
| t=150 | Aurelia Traffic | "The lift rings are failing. Aurelia is falling. Aurelia Traffic, signing off." |
| t=161 | Okafor | "I stood on those decks in sixty-six, when we fired on them. Follow her down, Lancer. Then get out." |
| t=176 | Varga | "Aurelia Traffic is off the air. I'm sorry, Commander." |
| Level end | Okafor | "Aurelia is gone. The people on those shuttles are not. Come home, Aegis." |

Timeline check (text): the timed lines close at ≈ 10.6, 20.1, 29.2, 40.8, 48.1, 57.4, 73.5, 87.3, 95.2, 109.1 (the
Choir with its 2 s lead), 118.5, 126.8, 135.0, 144.8, 157.9, 170.7 and 181.9; nothing overlaps. The secondary resolves
at ≈ t=143 (pair 3 out), its line waiting for Okafor's t=136 line to close. Okafor's Aurelia line (the gap analysis's
"Okafor's Aurelia line", Q4) is her memory of 2166 over the drop, not the act intro's "*I was at Aurelia. I remember.
So will you.*" (Q11 a, page 4), so the two never repeat each other. Vorne's line is his cold reaction after the Choir's
(J6); "only a door" is the story's fixed line, given to Varga. Varga's t=78 line is the [IFF spoofer](../../../player/systems/README.md#act-6-the-iff-spoofer-and-the-core-tiers-draft)'s
story beat (Aurelia's codes, in the shop from the L39 visit).

## Music & ambience

A proposal for D-audio's per-level table ([music](../../../audio/music/README.md)): the Act 6 A theme **"Eye of the
Storm"** ([track 14](../../../audio/music/README.md#track-list)) over the rim and the gilded decks (its full mix:
Aurelia's grandeur), the **Choir's line ducks it** (−6 dB, Level 26's duck) at t=100, then the Act 6 **B theme**
(track 15: heavy, choir and distorted bass; its sketch is round 72's) from t=103 to the fall, and over the fall its
breakdown (its `fall` stem: the choir alone over low strings) to the level end. **Vorne's motif** plays in the open
(track 14's motif stem) up to the turn and is **cut** at t=100: track 15 plays without its motif stem from t=103 to
the end, and his t=110 order comes over no motif (the Choir has spoken; the music plan's
[cues](../../../audio/music/README.md#cues-per-level-act-6)). Ambience: the `aurelia` bed
([sfx](../../../audio/sfx/README.md#ambience-per-setting): wind in the balloon cables, the city's hum, a calm
public-address chime) with its burning layer from t=100 to fire, alarms and groaning metal, and over
the fall the deck's deep structural groan and the balloons' fabric tearing (no pops). New sounds this level needs, for
the act's sfx step ([sfx](../../../audio/sfx/README.md)): the evacuation PA chime, the shuttles' liftoff (Level 10's),
the growth's burst over the decks (a fibrous tearing rush, never wet or bubbly: the regrowth rule), the balloons
tearing, the deck's groan, the burning decks; the Mirror Interceptor's and the Graft Turret's sounds (their documents'
briefs). Freesound only for real things (CC0/CC-BY), no bubbly or popping sounds.

## Credit budget

Budget(37) = 700 × 1.07³⁶ ≈ **7,997** ([economy](../../../systems/economy/README.md#per-level-budget)). Data in **Act
1 terms** (Mote 2, Skitter 5, Talon 12 (a leader 25, counted at 12 here), Chimera 14, Dart Drone 2, Mirror
Interceptor 30, Lamprey 12, Gilded Gunship 28, Hornet 26, Graft Turret 15 and its root 5, SAM Nest 15, the crate 100,
the secondary 100), × the act factor **10.486**, bounties also × the scale. A **`bounty_scale` of 0.30** (the earlier
acts' model: typical = 0.663 of the kills, 0.8 of the ground targets, half of the secret and the secondary; the prey
waves' bounty (258) counted at two thirds in the typical haul, a third eaten by the Vrell and paying nothing; a first
estimate for part C to confirm with `TypicalHaulTest` and `levelReport`) puts the typical haul at **7,990 (−0.1 %)**,
the perfect run at 12,410. No boss or mid-boss.

| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Mote Swarm 112 × 2 + Skitter 88 × 5 + Talon 69 × 12 + Chimera 41 × 14 + Dart Drone 12 × 2 + Mirror Interceptor 10 × 30 + Lamprey 10 × 12 + Gilded Gunship 2 × 28 + Hornet 2 × 26 (2,618) | 8,236 | 5,281 |
| Ground targets: Graft Turret 27 × (15 + 5) + SAM Nest 8 × 15 (660) | 2,076 | 1,661 |
| Secret: the CDF locker (hidden crate) | 1,049 | 524 |
| Secondary: the shuttles | 1,049 | 524 |
| **Total** (bounty scale 0.30) | **12,410** | **7,990** |
| Budget(n) = the typical haul's target; typical −0.1 %, perfect 1.55 × budget | | 7,997 |

**Balance plan**: the plan's L37 visit fits the Tesla Coil Pod L2 on the left wing (forward / reference **1.14**,
[Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft)), 22 of 22 MW: no room for a utility until
the L40 visit (Q8 a, J11; the Level 30 lesson is met at L40, not here). The level expects `anti-ground` (the nests),
`spread` (the popcorn and the hunters) and `shield-breaker` (the mirrors and the doubled shields), and is flyable
without the Tesla pod (the beam burns a mirror in about a second, the flanks are open).

## Difficulty notes

- **Easy**: the shuttles' armour 180 and 4 of 6 (`SHUTTLES n / 4`); the Graft Turret's easy hooks (the window 6 s);
  the mirrors' easy hooks (arc 100°, turn 45°/s); the t=84 pincer 1 + 1 and the rear ambush one unit; the prey links
  keep their hunters (the Vrell do more of the work).
- **Hard**: the shuttles' armour 90 and all 6 (`n / 6`); the Graft Turret's hard hooks (the window 4 s, regrowth
  0.6 s); the mirrors' hard hooks; the t=140 pincer 2 + 2; the three aimed waves one Talon more each; the other waves
  by the formation lever, trimmed by hand so never more than 4 mirrors, two Chimera groups and 6 Graft Turrets are
  on the screen.

## Concept art

No art yet. Round 72 proposals (wave 5): Q10's mood sheets for Aurelia before and after the turn and **Q4's
falling-city storyboard** (ART-R-B: the [Jovian system](../../../world/jovian/README.md#concept-art) and the
[act](../README.md)'s `concept/`), the Mirror Interceptor and Graft Turret sheets in their documents (ART-R-A), Aurelia
Traffic's radio portrait (ART-R-B, the story's characters). Production backdrop (Aurelia's decks, domes, spires and
balloons in two states, the growth's spread, the fall's scaled and darkened deck), the shuttles' Aurelia livery, the
locker and the briefing images come in part C (its round 75).

## Implementation

- [ ] Level data `data.yaml` (M10 part C): sections and speeds with 2 s eases, waves with the prey links, ground
      targets, the shuttles, radio, budget, threat profile, briefing, backdrop, music
- [ ] The shuttle secondary: six Level 10 shuttles in Aurelia's livery flagged `neutral` (the friendly marker's banner
      and brackets), liftoff untouchable, station and climb-out, the three aimed waves; `SHUTTLES n / 5` counting to
      its target with six pips, met at the fifth out, failed at the second loss (easy 4, hard 6)
- [ ] The turn at t=100: the Choir's line with the music's duck, the growth's burst and the darker grade over 4 s, the
      prey links from t=103 ([three-way rule](../../../enemies/README.md#the-three-way-war): latch, ram, lunge; prey
      killed by the Vrell pay nothing; hunters keep to their prey), the backdrop war on `deep`/`low-air`
- [ ] The falling city (Q4 a, A.13 b): the balloons tear at t=160, the deck breaks along an authored jagged cut into two ground layers that slow, darken and shrink over 20 s at their own rates, the spires topple,
      the spires and debris on `low-air`, open cloud from t=180; no ground unit on the screen at t=160
- [ ] Mirror Interceptors and Graft Turrets placed as above (their documents' rules); the spacing rules
- [ ] Wave script: ≥ 75 enemies a minute at medium with the ground units and launched Darts (`DensityTest`), the Act
      2+ floor (`PacingTest`, `ActFloorTest`), side and rear entries edge-warned 3 s, the quiet windows t=36–42 and
      t=97–103, ≥ 4 s calm after the last enemy, no two medium hulls overlapping
- [ ] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel37Test`, with Rook and without); Aurelia
      Traffic cast; the Choir distorted; Vorne on the enemy channel, not shouted
- [ ] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`); the
      Mirror Interceptor's and Graft Turret's portraits
- [ ] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images
- [ ] Typical haul within ±5 % of budget(37) = 7 997 with the level's `bounty_scale` (`TypicalHaulTest`)
- [ ] Music and ambience: track 14, the duck, track 15 from the turn, the city bed and its turn, the fall's sounds
      (`Level37SoundsTest`)
- [ ] Easy/hard variations as in *Difficulty notes*; the autopilot (flanking a mirror, closing a nest, not guarding
      the shuttles) flies the level with Rook on every difficulty and with `--escort none`
- [ ] Production art and captures for part C's round

## Open questions

Left to the build (the document is approved as drafted):

- **The turn's moment**: set at **t=100** (the working note's item): earlier would leave the shuttle pairs no calm half,
  later would crowd the turn into the fall. The window t=97–103 holds no new wave, as Level 33's Choir window.
- **The shuttle secondary's target**: 5 of 6 at medium (a loss allowed); our reading of "a count of N with the HUD
  counting". 4 of 6 would let it be met before the turn.
- **Mirror Interceptors never prey** (a call of this draft): the Vrell hunt Talons, the Gunship and the Hornet; a
  mirrored unit as prey would turn its mirror away from the ship and muddle Q1's lesson.

## Decisions

- 2026-10-31 (Act 6 design phase, step `m10-d-levels-1`; **decided 2026-10-31 (round 72)**): promoted from
  the act roster to a draft level document, hand tables only, no `data.yaml`. The shuttle secondary (six Level 10
  shuttles in Aurelia's livery, flagged `neutral`, three pairs at t=44/72/127, 5 of 6 out, `SHUTTLES n / 5`); the
  first mirrors at t=38 in a quiet window; nine ground nests (27 Graft Turrets, 8 SAM Nests), none on the screen when
  the city falls; **the turn at t=100** (the Choir, the growth's burst, four prey links from t=103: latch, ram, lunge,
  ram again); the fall t=160–200 with Okafor's 2166 line; Vorne cold on the enemy channel after the Choir (J6); Varga
  takes Aurelia's codes (the IFF spoofer's story beat); ≈ 114 enemies a minute with the ground units, Motes and
  Skitters as the tiny tier (J5); `bounty_scale` 0.30.
- 2026-10-31, design phase (step m10-d-act; decided 2026-10-31 (round 72)): **Vorne's motif is cut at the turn** (t=100; track 15
  without its motif stem to the end), the music plan's cue; the ambience is D-audio's `aurelia` bed with its burning
  layer.
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): **J12 rename**: the level is
  **"Fall of Aurelia"** (the folder `level-37-gilded-cage` moved to `level-37-fall-of-aurelia`; "Gilded Cage" stays
  Act 5's track 13). Q1 c (the mirror: bounce, beam heat, breaker shatter, open flanks; a and b rejected), Q3 a with
  c's backdrop (four prey links from t=103; b and c rejected), Q4 a (the falling city in the last 40 s; b the act
  outro or briefing image only and c moved to a later level rejected), Q7 b, Q9 a, Q10 a, J1, J2, J5, J6, J10 and J4
  (Halcyon One–Six; Aurelia Traffic is Lizzie Driver, V.3 b) as drafted. **A.13 b** (not the recommendation): the city
  **breaks up** along an authored jagged cut line into two city layers that sink at their own rates and the spires
  topple (the last 40 s above are rewritten). The document approved as drafted (D.3; `design: approved`).
