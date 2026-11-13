---
title: Level 37 – Fall of Aurelia
design: approved
implementation: done
art: chosen
depends-on: [../../../enemies/ground/graft-turret, ../../../enemies/air/mirror-interceptor, ../../../enemies/air/chimera, ../../../enemies/air/talon, ../../../enemies/air/gilded-gunship, ../../../enemies/air/hornet, ../../../enemies/ground/sam-nest, ../../../enemies/README.md, ../../../allies, ../../../world/jovian, ../../../player/generator, ../../../player/wingmen, ../../../ui/hud, ../../../story/characters]
updated: 2026-11-13
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
shuttle). The level's numbers are its [data.yaml](data.yaml) (M10 part C, step C2), rendered here, and measured with
`levelReport` (*Waves*, *Credit budget*).

## Briefing

Two pages, each one screen (`BriefingLayoutTest`) with room for its image. Rendered from the level's [data.yaml](data.yaml) (M10 part C, step C2):

<!-- data: briefing -->
> **Commander Okafor:** "Aurelia, the Ascendancy's capital on its balloons. Its traffic control
> has broken with Vorne: shuttles lift its civilians from the pads in three waves. Keep their
> lanes clear. I have been to Aurelia before, Lancer. This time we are there for its people."
>
> **Dr. Varga:** "The deck turrets are overgrown. Kill one and a root shows: burn it inside five
> seconds, or the turret grows back. A new interceptor carries a mirror on its nose. Shots
> bounce off it, a beam cracks it, the Tesla pod shatters it. Its flanks are open."
<!-- /data -->

*Images* (production art in part C, [briefing images](../../../ui/briefing/README.md)), briefs only now:

- Okafor's page `level-37-aurelia`: a CDF chart of Aurelia from above, the ring of gilded decks round the Spire
  Plaza, the balloon decks and lift rings at its far edge, the three shuttle pad pairs numbered with their lanes out
  in green, the wing's route across the city in white.
- Varga's page `level-37-mirror`: her scan of a Mirror Interceptor from above, its 120° arc drawn as a silver fan and
  the open flanks marked; an inset of a Graft Turret's wreck with the root exposed and a "5 s" timer ring.

*Hangar teaser* (shop screen before L37; Rook speaks, not voiced):

<!-- data: teaser -->
> **Rook:** "Mirrors on their noses now. The Tesla pod cracks glass, Lancer. Just saying."
<!-- /data -->

The hangar before L37 adds the [Reflex Shield](../../../player/shields/README.md#reflex-shield-act-6) to the shop
(Q7 b). The plan already fitted the **Tesla Coil Pod** at L2 on the left wing at the L36 visit (the hand plan's own
alternative, round 73 C.12; the left Micro-missile Pod to the inventory) and now raises the **Ion Beam to L5**, the act's
`shield-breaker` moment ([Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft): 22 of 23 MW,
forward / reference 1.14). Rook keeps his Autocannon.

## Threat profile

Rendered from the data. Directions by each wave's `from`, counted by enemy (the intel's rule): the mirror pincers and the
Lamprey hunter streams from the sides, the mirrors' rear ambush from behind.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `ground` (Aurelia's decks: Graft Turret nests, SAM Nests), `air` (Talon wings, Mirror Interceptors, Chimeras, a Gunship and a Hornet at a time, Mote Swarms, Skitter snakes; Lampreys after the turn) |
| Attack directions | front 94% · sides 5% · rear 1% |
| Density | 4 |
| Recommended traits | `anti-ground`, `spread`, `shield-breaker` |
| Hazards | Mirrors (bounced shots); regrowing turrets; the Vrell turn (t≈100); the falling city (the last 40 s) |
| Boss / mid-boss | none |
| Sensor-suite detail | **none**: Jupiter, Aurelia, `air` + `ground`, front · **L1**: + directions (sides, rear), density 4, hazards "mirrors" and "regrowth", the objective `CROSS AURELIA` and the secondary `SHUTTLES 0 / 5` · **L2**: + Mirror Interceptor (new), Graft Turret (new), Talon, Chimera, Gilded Gunship, Hornet, SAM Nest, Skitter and Mote portraits · **L3**: + `anti-ground`, `spread` and `shield-breaker` highlighted, the wave strip marking the shuttle pairs and the mirrors' entries (the turn is not on it: the sensors do not see it coming) |
<!-- /data -->

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
production art). No contact damage to the ship. Unlike the allies' common rule, **the player's fire hits them** as it
hits Act 5's marked neutral craft (round 75 F.3, the user's decision 2026-11-01, built in step C6a): shots, beams,
blasts and the specials' damage hurt a shuttle the guns reach (on its station or sliding aside), and one shot down is
a lost shuttle, paying nothing; Rook's fire passes and the homing weapons never pick one.

- **Marked friendly**: the HUD's [friendly marker](../../../ui/hud/README.md#in-the-play-field) opens its
  "FRIENDLIES INBOUND — HOLD FIRE" banner 2 s before each pair lifts and brackets each shuttle while it is on the
  screen (the Act 5 lesson: neutral craft clearly marked). They are built as Level 10's air allies (the level's
  `air_allies`: enemy bullets and bodies hurt them), not as craft with the `neutral` flag (whose craft enemy fire
  passes); the marker's brackets and banner are extended to them (m10c-gaps §4, step C2).
- **Liftoff** (5 s, Level 10's): each pair stands on its pads in the lower half of the screen (x 190 and 290) as the
  deck scrolls in and climbs to its station, drawn from the ground layer's scale up to the air scale; **untouchable**
  throughout.
- **Station** (3 s): the pair holds one behind the other in the middle of the field, at (240, 170) and (240, 240)
  px below the top edge, drifting on Level 10's lane sway (±16 px), so the hulls stay inside x 192–288, clear of
  the Mirror Interceptors' side stations (their hulls within 166 px of an edge).
- **Aside and out** (round 75 F.3, step C6a): each shuttle then **slides aside** over 1.5 s toward its own side edge
  and a little down (the left pad's to x 90, the right one's to x 390, 40 px lower; touchable, the sway stops), and
  **climbs out** straight up along that edge over 3 s, accelerating (untouchable), away from the centre lane the ship
  fires up. Pair 1 holds t=49–52, slides to 53.5 and is out by 56.5; pair 2 77–80, 81.5, 84.5; pair 3 132–135, 136.5,
  139.5. About 12.5 s on the screen a pair.
- **Never behind a shuttle** (round 75 F.3): while a shuttle is on its station, sliding or climbing out, no enemy
  hovers or flies in behind it in the player's line of fire (above it with more than half its width inside the
  shuttle's column for over 1 s; `ShuttleLaneTest`): the t=77 Hornet holds down the right third, the t=80 snake comes
  down the middle after pair 2 has slid aside; around pair 3 a Talon pair comes down the left third at t=132 (was
  t=124), the Hornet enters at t=135 as the pair slides aside (was t=133), the rear ambush rises at t=136.5 as it
  climbs out untouchable (was t=131: its mirrors bounced shots up into the shuttles), the Motes follow at t=137 and the
  left-edge snake at t=140 (were t=134 and 137), the last pincer at t=149 (was t=145, clear of the Hornet's exit); the
  nests stand off the stations, the slides and the edge lanes (the dome rim's on its right half, the terrace's left
  two further back).
- **What hurts them**: every enemy bullet that touches a hit box (by its class), an `air` enemy's body (by its tier,
  a `tiny` or `small` one destroyed), as in Level 10. Three waves aim at them through the
  [target-the-objective hook](../../../enemies/README.md#target-the-objective-hook) (t=46, 74 and 130: Talon pairs
  aimed at the nearest shuttle of the pair on its station); the rest aim at the ship, and the Skitter snakes fly
  round the band. **Never prey**: the Vrell never hunt allies (the [three-way rule](../../../enemies/README.md#the-three-way-war)).
- **Armour** 120 (easy 180, hard 90); smoke and an amber pip below 50 %. **Lost**: it loses power and glides down
  into `far` over 3 s (Level 10's presentation), no debris on the play plane.
- **Tuning**: armour and the aimed waves are tuned until the autopilot, which does not guard them and never shoots
  one (it holds its fire with a shuttle in its guns' band or its seekers' line), gets 5 of 6 out in most medium runs
  (Level 10's rule); a per-shuttle `damage_factor` is the fallback. Measured in step C6a: every easy and medium run 6
  of 6; hard (all 6 must come home) 10 of 16 runs over 8 seeds with and without Rook, 5 of the test's 6 (round 75
  L.2 a: the data moved, the armour kept; step C3 had 6 of 16, step C5 none of 6).

## Layout

Scroll **120 px/s** over the outer rim, **100 px/s** over the gilded decks, **80 px/s** in the Spire Plaza (the turn
stays on the screen), **100 px/s** after the turn and through the fall; each change eased over 2 s. The setting is
the [Jovian system](../../../world/jovian/README.md#sub-locations-per-level)'s Aurelia in Q10 a's **L37 grade**
([moods](../../../world/jovian/README.md#moods-per-level-q10)): gold and white over warm cream cloud, the decks a step
paler and less saturated than the units' gold (gold on gold); after the turn the sky a step darker, fire-orange and
teal, the Vrell bullets the larger, brighter ones over the teal growth. Media: Jupiter's **air** (J2). Motion budget:
the shuttles, the mirrors' specular sweep and, after the turn, the spreading growth are the strong elements; the
balloons sway slowly.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Aurelia's Rim | 0–30 | 0–3,600 | 120 | clear | `ground`: the outer decks, gilded rails, a deck edge with the first Graft Turret nest (t≈14), a SAM platform (t≈24). `low-air`: balloon cables, the first anti-grav ring. `deep`: the city's far side and its balloons. | Popcorn and Talons; the regrowth lesson on one nest. |
| 2. The Gilded Decks | 30–70 | 3,600–7,620 | 100 | clear | `ground`: domes and promenades, the shuttle pads (pair 1, t=44), a dome rim nest (t≈48), two deck edges either side of the pads (t≈62), the 2166 CDF locker under an awning (t≈60, the secret). `low-air`: balloons. | The first mirrors (t=38, quiet window t=36–42); shuttle pair 1. |
| 3. The Spire Plaza | 70–100 | 7,620–10,040 | 80 | clear | `ground`: the plaza round the great spire, its terrace nest (t≈79), pads (pair 2, t=72), SAM Nests under the spire (t≈95); teal growth thickening on the dome seams. | Shuttle pair 2, the 2 + 2 mirrors, Varga's codes; the window before the turn. |
| 4. The Turn | 100–158 | 10,040–15,820 | 100 | darkening, smoke | `ground`: burning decks overgrown with teal growth, a nest on a burning deck (t≈128), the last SAM platform (t≈138), the balloon deck's edge (t≈144), pads in the growth (pair 3, t=127). `low-air/deep`: Vrell and gold craft fighting, hulks falling. | The prey links; shuttle pair 3; the rear ambush. |
| 5. The Fall | 158–200 | 15,820–20,020 | 100 | smoke, then open cloud | `ground`: the two falling halves of the deck (t=160–196: each slows, darkens, shrinks at its own rate and fades from t≈161.5 as the cloud tops close over it, t=170–197.5; round 75 F.6 redo), then none. `low-air`: spires rising past, debris, a torn balloon. `deep`: the bands. Level end at t=200 over open cloud. | The set piece; the last waves out of the debris; calm to the end. |
<!-- /data -->

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
  the t=137 Motes lunge at the t=135 Hornet (step C6a: were t=134 and 133, round 75 F.3 and L.2 a); the t=170 Skitters
  ram the t=169 Talon wing. A hunter keeps to its
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
- **t=180–200**: open cloud below the play plane; the two halves, darkened and shrunk, **fade slowly** (from t≈161.5 to
  nothing at t≈194) as two cloud tops slide in from the sides and close over them (t=170–197.5), so the platform
  **disappears in the clouds completely** (round 75 F.6 redo: the user, 2026-11-01; there is no separate sinking city
  and nothing slides out through the screen's bottom edge). The waves of section 5 come out of the debris; the last
  enters at t=186.
- **Calm** (the Act 5 lesson): the last wave is gone by ≈ t=193, **≥ 4 s** before the level end at t=200; Varga's
  t=176 line and the fall's sound carry the end.
- Okafor's Aurelia line plays over the drop (t=161). A renderer set piece (two scaled ground layers and the authored cut line) and a production backdrop part (the gap
  analysis §2.10: large, now two layers), M10 part C; the storyboard is chosen in round 72 (A.13 b, [the world](../../../world/jovian/README.md#concept-art)).

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Data in **Act 1 terms** with a
`bounty_scale` (see *Credit budget*). Rendered from the data (M10 part C, step C2) and measured with `levelReport`
(*Built* below the table); the bullets are the hand draft's plan.

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
  Waves by direction (built: 60 waves with step C2's six offbeat Talon pairs): front 54 of 60 (90 %), sides 5 (the
  three mirror pincers and the two Lamprey hunter streams, 8 %), rear 1 (the mirrors' ambush, 2 %), every side and rear
  entry **edge-warned 3 s**. The floor comes from the Talons' pairs, the mirrors' bursts and bounced shots, the
  turrets' pairs and the SAM Nests' diamonds: expected to meet ≤ 3 pauses over 2 s, ≥ 2.5 enemies on the screen and
  ≥ 10 shots a minute (`PacingTest`, `ActFloorTest`), confirmed with `levelReport` in step C2 (*Built*). Quiet windows:
  t=36–42 (the first mirrors alone with the t=31 Skitters leaving) and **t=97–103** (the turn).
- **Spacing** (the Act 5 lesson): at most 4 Mirror Interceptors on the screen and 2 on one side; a pincer's hulls
  ≥ 80 px apart; no mirror station over the Hornet's hover (it holds above 140 px, the stations from 160 px) or the
  Gunship; the Gunship and the Hornet never on the screen together. No popcorn routed through a mirror's arc in front
  of it (the [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md#difficulty-and-level-use)'s rule).
- **Calm at the end**: the last wave enters at t=186 and is gone by ≈ t=193; ≥ 4 s of calm before t=200.

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 3 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Vrell and gold side by side before the turn |
| 6 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 9 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 12 | 1 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Squad leader at the tip |
| 16 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The first Graft Turret nest (t≈14) on the deck edge below them |
| 17.5 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the floor's pause after the beam erases the swarm) |
| 19 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 22 | 1 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 25 | 1 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Two SAM Nests on the rim (t≈24) |
| 28 | 1 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | The hybrids of Level 36 over Aurelia |
| 31 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | The last wave before the quiet window |
| 38 | 2 | pincer | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 2 | sides | **The Mirror Interceptor's first appearance**: 1 + 1, quiet window t=36–42 (no popcorn in front of the mirrors); edge-warned 3 s ahead; Varga's line at t=31 |
| 43 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Routed down the middle third, clear of the mirrors' arcs; shuttle pair 1 lifts at t=44 |
| 46 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | **Aims at the shuttles** (the target-the-objective hook): pair 1 |
| 49 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | Routed round the shuttles' band (Level 10's rule): down the right edge |
| 52 | 2 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | About 6 Darts; drops the Shield cell |
| 55 | 2 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | Squad leader; drops the Overdrive |
| 58 | 2 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The CDF locker (the secret) on a deck awning at t≈60 |
| 61 | 2 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | |
| 64 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | |
| 67 | 2 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 70 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Shuttle pair 2 lifts at t=72 |
| 72 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the floor's pause after the beam erases the swarm) |
| 74 | 3 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | **Aims at the shuttles**: pair 2; squad leader |
| 77 | 3 | single | [Hornet](../../../enemies/air/hornet/README.md) | 1 | front (right) | Hovers above 140 px down the right third (step C6a, round 75 F.3: never behind a shuttle in the line of fire); gone (≈ t=88) before the t=89 mirrors take their stations |
| 80 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front | Down the middle (step C6a, round 75 F.3: pair 2 has slid aside to the edges and climbs out along them) |
| 84 | 3 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Routed down the middle third (step C2: t=84, the mirror pincer's t=89: the Hornet's 9 s hover is over before the mirrors come) |
| 86.5 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the floor's pause after the beam erases the swarm) |
| 89 | 3 | pincer | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 4 | sides | 2 + 2, ≥ 80 px between hulls; edge-warned 3 s ahead (step C2: t=89, not 84: the t=77 Hornet holds above their stations until ≈ t=88; pair 2 is out) |
| 91 | 3 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 94 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | The last wave before the turn: quiet window t=97–103 |
| 103 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 6 | front | **Prey** (squad leader): the t=104 Lampreys hunt it |
| 104 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | left side | **Hunters** of the t=103 wing: they latch on the Talons (edge-warned 3 s ahead) |
| 107.5 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the floor's pause after the beam erases the swarm) |
| 110 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | At the ship (the Vrell keep their own fire at the player) |
| 113 | 4 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 6 | front | Hybrids: never prey, they fight for the Ascendancy |
| 116 | 4 | column | [Gilded Gunship](../../../enemies/air/gilded-gunship/README.md) | 1 | front | **Prey**: the t=118 Skitters ram it; about 6 Darts (the Darts are not prey) |
| 118 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | **Hunters** of the t=116 Gunship: they ram it |
| 121 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 127 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Shuttle pair 3 lifts at t=127 |
| 130 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | **Aims at the shuttles**: pair 3; squad leader |
| 132 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front (left) | Step C2: a firing Talon pair on the offbeat (step C6a: t=132, not 124, down the left third, past pair 3's stations before they slide aside: the floor's pause the rear ambush left at t=131) |
| 135 | 4 | single | [Hornet](../../../enemies/air/hornet/README.md) | 1 | front | **Prey**: the t=137 Motes lunge at it; down the middle as pair 3 slides aside (step C6a, round 75 F.3: t=135, not 133, never behind a shuttle in the line of fire) |
| 136.5 | 4 | rear ambush | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 2 | rear | Two lanes ≥ 80 px apart; edge-warned 3 s ahead at the bottom edge (step C6a: t=136.5, not 131: rising as pair 3 climbs out untouchable, so the shots their mirrors bounce up no longer hit it on its stations and slide; step C2 had moved it from t=124 for the t=116 Gunship) |
| 137 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | **Hunters** of the t=135 Hornet: down the middle third (step C6a, round 75 L.2 a: t=137, not 134, clear of pair 3's stations and edge lanes) |
| 140 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | Step C6a: t=140, not 137 (pair 3 climbs out along the left edge until t=139.5) |
| 143 | 4 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 6 | front | **Prey** (squad leader): the t=144 Lampreys hunt it |
| 144 | 4 | stream | [Lamprey](../../../enemies/air/lamprey/README.md) | 5 | right side | **Hunters** of the t=143 wing (edge-warned 3 s ahead) |
| 147 | 4 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front | |
| 149 | 4 | pincer | [Mirror Interceptor](../../../enemies/air/mirror-interceptor/README.md) | 2 | sides | 1 + 1 (hard 2 + 2); edge-warned 3 s ahead (step C2: t=145, not 140: the third pair's Hornet holds above their stations; step C6a: t=149, its hover now ends ≈ t=146) |
| 150 | 4 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (right) | |
| 153 | 4 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 156 | 4 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The last wave before the fall |
| 159 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the floor's pause after the beam erases the swarm) |
| 162 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The fall begins (t=160): Vrell out of the torn balloons |
| 165 | 5 | V-wing | [Chimera](../../../enemies/air/chimera/README.md) | 7 | front | |
| 169 | 5 | V-wing | [Talon](../../../enemies/air/talon/README.md) | 5 | front | **Prey** (squad leader): the t=170 Skitters ram them |
| 170 | 5 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 8 | front (left) | **Hunters** of the t=169 wing |
| 174 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | Among the spires' debris on `low-air` |
| 176 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 2 | front | Step C2: a firing Talon pair on the offbeat (the floor's pause after the beam erases the swarm) |
| 178 | 5 | swarm | [Chimera](../../../enemies/air/chimera/README.md) | 8 | front | |
| 182 | 5 | line abreast | [Talon](../../../enemies/air/talon/README.md) | 4 | front | |
| 186 | 5 | swarm | [Mote Swarm](../../../enemies/air/mote-swarm/README.md) | 8 | front | The last wave; gone by ≈ t=193, calm to the end at t=200 |

Totals: Mote Swarm 112 · Talon 83 · Skitter 88 · Chimera 41 · Mirror Interceptor 10 · Gilded Gunship 2 · Hornet 2 · Lamprey 10.
<!-- /data -->

**Built** (step C2, `levelReport` at the plan's fit with Rook): **60 waves**, 348 air units + 35 ground units at
medium, **117.8 a minute** (easy 93.8, hard 141.5); **4.22 on the screen and 109.8 shots a minute** at medium (the
floor's 2.5 and 10), two stretches over 2 s at medium and hard (the quiet window before the first mirrors and the
turn's window; easy one over 3 s), the end calm 12.2 s. Retimed from the hand table (pending the user's review in
round 75): the **t=84 mirror pincer at t=89** (the t=77 Hornet's 9 s hover, at most 150 px below the top edge, overlaps
the stations' 160 px with their hulls until ≈ t=88; the t=88 Mote swarm takes t=84), the **rear ambush at t=131**
(the t=116 Gunship drifts through its bottom stations until ≈ t=129; step C6a: **t=136.5**, its bounced shots clear
of pair 3) and the **t=140 pincer at t=145** (the t=133 Hornet's hover; hard's 2 + 2; step C6a: **t=149**, the t=135
Hornet's exit); **six firing Talon pairs** on the offbeats (t=17.5, 72, 86.5, 107.5, 124 and 159; step C6a: the t=124 pair at t=132;
Level 36's lever) fill the floor's pauses after the beam erases a swarm. No two `medium` hulls overlap on any
difficulty; at most 4 Mirror Interceptors on the screen and 2 on one side, at most 6 Graft Turrets (`levelReport`'s
`mirrors` line).

## Ground targets

Aurelia's decks (`ground`), placed by the time their nest's middle reaches the top edge. The
[Graft Turret](../../../enemies/ground/graft-turret/README.md)'s nests sit on deck edges and dome rims, where `area`
and `anti-ground` sweep them and the beam burns the roots; never more than 6 Graft Turrets on the screen (its rule).

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 1 | [Graft Turret](../../../enemies/ground/graft-turret/README.md) ×3 on a deck edge of the outer rim (t≈14): the first nest, alone (the regrowth lesson, Varga's t=11 line) | Bounty 15 each, the root 5 |
| 1 | [SAM Nest](../../../enemies/ground/sam-nest/README.md) ×2 on the rim's air-defence platform (t≈24) | Bounty 15 each |
| 2 | Graft Turret ×4 round the right half of a dome rim on the gilded decks (t≈48), off the shuttles' stations | Bounty 15 each, the root 5 |
| 2 | Graft Turret ×5 on the two deck edges either side of the pads (t≈62; pair 1 is out by then) | Bounty 15 each, the root 5 |
| 2 | The 2166 CDF supply locker under a deck awning (a `ground` trigger, 20 HP, t≈60, x 90) | The secret: frees a crate worth 100 (1,049) |
| 3 | Graft Turret ×6 on the Spire Plaza's terrace (t≈79–80): a full nest, off the shuttles' slide and edge lanes | Bounty 15 each, the root 5 |
| 3 | SAM Nest ×3 on the plaza's far side under the great spire (t≈95), firing through the turn's window | Bounty 15 each |
| 4 | Graft Turret ×5 on a burning deck, the growth over its plates (t≈128) | Bounty 15 each, the root 5 |
| 4 | SAM Nest ×3 on the last platform before the balloon decks (t≈138) | Bounty 15 each |
| 4 | Graft Turret ×4 on the balloon deck's edge (t≈144): the last ground units, off the screen by ≈ t=151 | Bounty 15 each, the root 5 |
<!-- /data -->

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
without. Rook's barks carry `requires: escort`; his scripted line (t=52.5, his pulp line of the level) does not.

**Speakers** (the Act 6 speakers are D-story's table): Okafor, Varga, Rook; **Aurelia Traffic** (J4: the act's
friendly generic voice, Aurelia's civil traffic controller on the **friendly** channel, steady at first and frayed by
the end; a voice and portrait for the round 72 auditions); **the Choir** (distorted, Level 33's dress, a 2 s static
lead; the music ducks under it); **Vorne** on the **enemy channel** (Act 5's intercepted dress: amber, the steady
interference, `INTERCEPT`), **cold, never shouted** (J6: his only shout is Level 42's).

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Aurelia Traffic | "CDF flight, civilians on our pads. Please. Help us get them out." |
| t=9 | Okafor | "Aegis copies. Lancer, the shuttles fly. Keep their lanes clear." |
| t=16.5 | Varga | "Grown turrets. Kill one, burn its root, or the turret grows back." |
| t=24.5 | Varga | "Mirror noses, both sides: shots bounce back. Hit the flanks." |
| The first bounced shot (`first-bounce`, event, bark) | Rook | "It's throwing your own shots back, Lancer. Go round it." |
| t=32 | Aurelia Traffic | "Halcyon One and Two boarding. CDF flight, they're yours." |
| t=52.5 | Rook | "Gold decks, domes and guns. Lancer, these people have a problem." |
| t=70.5 | Aurelia Traffic | "Halcyon Three and Four next. Boarding faster than we can launch." |
| A shuttle lost (`first-ally-lost`, event, the first time) | Aurelia Traffic | "We lost a shuttle! Aegis, keep them off the lanes!" |
| t=78.5 | Varga | "Their traffic net is mine. Aurelia's codes. I'm keeping these." |
| t=86.5 | Okafor | "Take everything, Doctor. Lancer, the growth is moving." |
| t=100 (t=100 (its 2 s static from t=98)) | The Choir (distorted) | "…the one who opened… is no longer needed… the door is ours…" |
| t=115.5 (t=115.5, the enemy channel (cold, never shouted)) | Vorne | "All wings, this is Vorne. The Vrell are hostile. Burn them." |
| t=123 | Aurelia Traffic | "Halcyon Five and Six, launching through the growth. Aegis, please!" |
| t=131 | Varga | "They're eating the city. Never his partners. Only a door." |
| A prey unit killed by a hunter (`first-prey-kill`, event, the first time, bark) | Rook | "They're eating their own side. Not complaining." |
| Secondary met (`secondary-objective`, the fifth shuttle out; easy the fourth, hard the sixth) | Aurelia Traffic | "That's everyone we could load. Aurelia thanks you, Aegis." |
| Secondary failed (`secondary-failed`, the second shuttle lost; easy the third, hard the first) | Okafor | "Two shuttles down. Keep the rest flying, Lancer." |
| t=153.5 | Okafor | "Vrell hunting the gold ones. Let them. Shoot whatever fires at you." |
| t=163 | Aurelia Traffic | "The lift rings are failing. Aurelia is falling. Aurelia Traffic, signing off." |
| t=174.5 | Okafor | "I stood on those decks in sixty-six, when we fired on them. Follow her down, Lancer. Then get out." |
| t=186.5 | Varga | "Aurelia Traffic is off the air. I'm sorry, Commander." |
| Level end | Okafor | "Aurelia is gone. The people on those shuttles are not. Come home, Aegis." |
<!-- /data -->

Timeline check (round 75 L.3 a and L.4 a, step C6a: the timed lines up to t=140 and Okafor's t=153.5 line cut to one
typed page each, ≈ 7 s, the same facts, new takes to come; modelled on the game's radio queue with the runs' event
times): the timed lines open on time at t=1, 9, 16.5 (Varga's turret warning back by the first nest, t≈14, where the
draft had it), 24.5, 32 (pair 1 boarding), 52.5, 70.5, 78.5, 86.5 (Okafor's reply ends ≈ 93.3, so the Choir's static
from t=98 is heard alone), 100, 115.5, 123, 131, 153.5, 163, 174.5 and 186.5, and leave gaps for the event lines: the
first bounce (t≈39-45) in t≈39.3-52.5, the locker (t≈60-63) in t≈60.1-70.5, the first prey kill in t≈107.4-115.5
(the Lampreys' kills from t≈105) and t≈138.3-153.5 (the Motes' at t≈134-140; a kill at t≈118-119 goes stale behind
Vorne's and Aurelia Traffic's lines), the first shuttle lost in t≈138.3-153.5 for the third pair (one of the first
two pairs, rare since the shuttles leave the centre lane, goes stale: the gaps are the bounce's and the locker's); an
outcome decided early (easy met with pair 2 at t≈84.5, a failure at the second pair) opens at t≈107.4 after the Choir,
the third pair's at t≈139.5 at once. Okafor's Aurelia line (the gap analysis's
"Okafor's Aurelia line", Q4) is her memory of 2166 over the drop, not the act intro's "*I was at Aurelia. I remember.
So will you.*" (Q11 a, page 4), so the two never repeat each other. Vorne's line is his cold reaction after the Choir's
(J6); "only a door" is the story's fixed line, given to Varga. Varga's t=78.5 line is the [IFF spoofer](../../../player/systems/README.md#act-6-the-iff-spoofer-and-the-core-tiers-draft)'s
story beat (Aurelia's codes, in the shop from the L39 visit).

## Music & ambience

A proposal for D-audio's per-level table ([music](../../../audio/music/README.md)): the Act 6 A theme **"Eye of the
Storm"** ([track 14](../../../audio/music/README.md#track-list)) over the rim and the gilded decks (its full mix:
Aurelia's grandeur), the **Choir's line ducks it** (−6 dB, Level 26's duck) at t=100, then the Act 6 **B theme**
(track 15: heavy, choir and distorted bass; its sketch is round 72's) from t=103 to the fall, and over the fall its
breakdown (its `fall` stem: the choir alone over low strings) to the level end. **Vorne's motif** plays in the open
(track 14's motif stem) up to the turn and is **cut** at t=100: track 15 plays without its motif stem from t=103 to
the end, and his t=115.5 order comes over no motif (the Choir has spoken; the music plan's
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

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Mote Swarm 112 × 2 + Talon 83 × 12 + Skitter 88 × 5 + Chimera 41 × 14 + Mirror Interceptor 10 × 30 + Gilded Gunship 2 × 28 + Hornet 2 × 26 + Lamprey 10 × 12 + launched Dart Drone 12 × 2 | 9,383 | 5,630 |
| Squad leaders: 7 × 13 extra (perfect run only) | 308 | 0 |
| Ground targets: Graft Turret 27 × 15 (+ root 5) + SAM Nest 8 × 15 | 2,209 | 1,767 |
| Secret: CDF locker (hidden crate, 1% of budget) | 1,049 | 524 |
| Secondary: get 5 of the 6 shuttles out | 1,049 | 524 |
| **Total** (bounty scale 0.32) | **13,998** | **8,446** |
| Budget(n) = the typical haul's target; typical +6 %, perfect 1.75 × budget | | 7,997 |
<!-- /data -->

**Measured** (`levelReport`, step C2): the README's scale 0.30 flew a typical haul of 7,490 at medium (−6.3 %, the
waves before the offbeat pairs); the level's **`bounty_scale` is 0.32**, the typical haul **8,079 (+1.0 %)** with the
pairs, the perfect run 13,539 (1.69 × budget); easy 6,905, hard 9,192. The flown haul counts the Graft Turrets' roots
(5 each), the prey the Vrell kill (paying nothing) and the shuttles' secondary as flown (met in 3 of 3 medium runs).

**Balance plan**: the plan fitted the Tesla Coil Pod L2 on the left wing at its L36 visit (round 73 C.12) and buys the
**Ion Beam L5** at this one (forward / reference **1.14**,
[Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft)), 22 of 23 MW: no room for a 2-MW utility until
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

Round 72 (wave 5): Q10's mood sheets for Aurelia before and after the turn and **Q4's falling-city storyboard**
(ART-R-B: the [Jovian system](../../../world/jovian/README.md#concept-art) and the [act](../README.md)'s `concept/`),
the Mirror Interceptor and Graft Turret sheets in their documents (ART-R-A), Aurelia Traffic's radio portrait (ART-R-B,
the story's characters).

Round 75 (M10 part C, step ART-C2; production art, proposed): the Aurelia kit, the turn's growth burst, the backdrop war
and the falling city by `tools/art/backdrop_l37.py` (the shared Jovian pieces in `tools/art/jovkit.py`), in round 72's
L37 grade (the decks a step paler and less saturated than the units' gold; after the turn fire-orange and teal) and
A.13 b with round 73 R.2's storyboard (the jagged break: torn plate, flaps bent outward, a charred, hot-glowing rim,
shards in the gap, smoke and fire from both edges). The city is one continuous field laid out on the level's clock
(every landmark by its time: the pad decks at x 190 / 290, the nests' deck edges, rims and terraces, the locker's awning
at x 90, the Spire Plaza and its great spire), cut into back-to-back ground pieces; from the turn every row entering is
burnt and overgrown, and the screen at the turn is a growth burst revealed from the dome rims along the seams over 4 s;
at the fall the main deck's two halves (the city's own pixels: the handover is exact) slow, darken and shrink toward
the field's centre on their curves and fade out under the cloud veils (the F.6 redo below; the first version's sinking
city on `deep` is gone from this level, its files stay as Level 38's source). The proposal
(`concept/backdrop-proposal.yaml`, step C2's input, not data) holds the tile sets, pieces, placements, the `growth` and
`fall` blocks with the halves' curves, the sections and the ground targets' and pads' places. The briefing images are
in the [briefing](../../../ui/briefing/README.md#concept-art) (`briefing-images-final-r75-a`). Prompts and briefs:
[concept/prompts.md](concept/prompts.md).

Round 75 F.6 redo (step ART-C3, proposed; the user: the platform should disappear in the clouds completely, fade slowly
during the fall, and parts were cut off at the bottom in the last frame): the cut was the sinking city's own canvas
(256×208, smaller than the halves at their curves' end: the east half's lower ~50 rows and the west half's top were
cropped, a straight edge) and its sliding out through the screen's bottom edge on `deep`. Now the two halves carry the
whole fall (t 160–196): they slow, shrink, turn and darken as before, keep their gap, are whole on the screen from
t≈172.5, sink slowly and fade from t 161.5 to nothing at t 194, while two cloud tops of the deck's own heads slide in
from the sides and close over them (t 170–197.5), then fade into the deck; no sinking city in this level any more (its
files stay as Level 38's source).

| File | What | Status |
|---|---|---|
| [concept/backdrop-final-r75-a.png](concept/backdrop-final-r75-a.png) | Production backdrop: eighteen screens at third size with stand-ins (the rim, the SAM platform, pair 1's pads, the locker's awning, the plaza's terrace, the great spire, the turn at +0.6/+2/+4.5 s, a burning deck with the war below, the main deck, the fall at +0/+2/+6/+12/+21 s, the open cloud, the end), the readability crops (Talon, Gunship, Chimera, needles and an orb over the pre-turn deck, the plaza, a burnt deck and the main deck), the halves with their torn rims and glow, the growth burst and its reveal map, the spires' topple, the balloon tearing, hulks, a deck fire, the war, debris, the sinking city, the tile sets and the city's ground pieces (sheet) | chosen (round 75 F.5 accept; its fall part rejected: F.6 redo, see `fall-final-r75-b`) |
| [concept/backdrop-final-r75-a.gif](concept/backdrop-final-r75-a.gif) | The fall, t 157–182 at half size (4 frames a second): the main deck whole, the jagged break, the halves slowing, darkening and shrinking, the spires toppling, the sinking city | rejected (round 75 F.6 redo: the fall's end was not as it should be; replaced by `fall-final-r75-b`) |
| [concept/growth-final-r75-a.gif](concept/growth-final-r75-a.gif) | The turn, t 99.5–105.5 at half size (10 frames a second): the growth bursting over the plaza from the dome rims along the seams, the fires, the deck a step darker | chosen (round 75 F.5 accept) |
| [concept/cdf-locker-final-r75-a.png](concept/cdf-locker-final-r75-a.png) | The CDF locker, the secret (`tools/art/aurelia_units.py locker`, step ART-C1; 32×28, the ground target's `cdf-locker` frames 0 sealed, 1 hit, 2 broken open) at 4× and at 1× beside the ship and a Graft Turret with the game's crate (sheet) | chosen (round 75 F.4 accept) |
| [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml) | The generator's `backdrop:` block (tile sets, pieces, the city's ground pieces on exact rows, placements, the `growth` and `fall` blocks with the halves' curves), the sections, the ground targets' places, the pads and the locker for step C2 (not data) | chosen (round 75; step C2's input; its `fall` block is replaced by the F.6 redo's in the data) |
| [concept/turn-capture-r75-a.png](concept/turn-capture-r75-a.png), [.gif](concept/turn-capture-r75-a.gif) | In the game (step C5, the [recipe](concept/prompts.md#level-37-capture-r75-a)): the turn at t=98 (the Choir's static), t=102 (the growth's burst over the decks) and t=110 (Vorne's order, the darker grade, the fires); the loop t 97–112 | chosen (round 75 F.5 accept) |
| [concept/prey-capture-r75-a.png](concept/prey-capture-r75-a.png), [.gif](concept/prey-capture-r75-a.gif) | In the game: the t=118 Skitter snake ramming the t=116 Gilded Gunship (the ship steered off the middle), t 119–123 | chosen (round 75 F.7 accept) |
| [concept/fall-capture-r75-a.png](concept/fall-capture-r75-a.png), [.gif](concept/fall-capture-r75-a.gif) | In the game: the fall at t=160 (the break), t=170 (the halves drifting, a spire toppling) and t=185 (the sinking city); the loop t 159–186 | rejected (round 75 F.6 redo: the fall's end; replaced by the redo) |
| [concept/fall-final-r75-b.png](concept/fall-final-r75-b.png) | The F.6 redo (step ART-C3): the whole fall t 158–200 at half size with stand-ins (the break, the halves slowing, shrinking, darkening and fading, the cloud tops closing over them, gone by t 194, open cloud to the end), 1:1 crops of the screen's lower half at t 176/186/192/196 (nothing cut), the two cloud tops, the halves' alpha, shade and scale by second (sheet) | chosen (round 79 H.2 ok; the F.6 redo of round 75) |
| [concept/fall-final-r75-b.gif](concept/fall-final-r75-b.gif) | The F.6 redo: the whole fall, t 158–200 at play size (4 frames a second), with stand-ins | chosen (round 79 H.2 ok; the F.6 redo of round 75) |
| [concept/readability-capture-r75-a.png](concept/readability-capture-r75-a.png) | In the game, 1:1 crops: gold on gold (Talons, the shuttles and a nest over the decks before the turn) and teal on teal (the Chimera swarm and its fire over the growth after it) | chosen (round 75 F.8 accept) |
| [concept/locker-capture-r75-a.png](concept/locker-capture-r75-a.png) | In the game, 1:1: the CDF locker under its awning at t≈61 | chosen (round 75 F.4 accept) |

## Implementation

- [x] Level data `data.yaml` (M10 part C): sections and speeds with 2 s eases, waves with the prey links, ground
      targets, the shuttles, radio, budget, threat profile, briefing, backdrop, music (step C2, 2026-11-01: on ART-C2's
      backdrop proposal; the content's checks: no ground unit on the screen at the fall, no mirrored, hybrid, ally or
      boss wave hunted, the theme change after Vorne's motif)
- [x] The shuttle secondary: six Level 10 shuttles in Aurelia's livery marked friendly (the friendly marker's banner
      and brackets), liftoff untouchable, station and climb-out, the three aimed waves; `SHUTTLES n / 5` counting to
      its target with six pips, met at the fifth out, failed at the second loss (easy 4, hard 6) (steps C1, C2: air
      allies outside the primary in lift groups, the `saved` secondary, `AirAlliesTest`, `SavedSecondaryTest`; step
      C4b: the livery, the brackets, the banner per lift and the tracker, `FriendlyMarkerTest`, `MissionPanelTest`)
- [x] The turn at t=100: the Choir's line with the music's duck, the growth's burst and the darker grade over 4 s, the
      prey links from t=103 ([three-way rule](../../../enemies/README.md#the-three-way-war): latch, ram, lunge; prey
      killed by the Vrell pay nothing; hunters keep to their prey), the backdrop war on `deep`/`low-air` (step C1: the
      hunting snakes, `PreyWarTest`; step C4a: the growth overlay, the grade's 4 s ramp, the war and fires; step C4b:
      the Choir's static and duck)
- [x] The falling city (Q4 a, A.13 b): the balloons tear at t=160, the deck breaks along an authored jagged cut into two ground layers that slow, darken and shrink over 20 s at their own rates, the spires topple,
      the spires and debris on `low-air`, open cloud from t=180; no ground unit on the screen at t=160 (step C4a:
      the falling-city pass on ART-C2's halves, `FallingCityTest`; the content's check: no ground unit at the fall)
- [x] Mirror Interceptors and Graft Turrets placed as above (their documents' rules); the spacing rules (step C2: at
      most 4 mirrors on the screen, 2 a side, 6 Graft Turrets, no `medium` hulls overlapping on any difficulty)
- [x] Wave script: ≥ 75 enemies a minute at medium with the ground units and launched Darts (`DensityTest`), the Act
      2+ floor (`PacingTest`, `ActFloorTest`), side and rear entries edge-warned 3 s, the quiet windows t=36–42 and
      t=97–103, ≥ 4 s calm after the last enemy, no two medium hulls overlapping (step C2 with the generic autopilot
      and the plan's fit; the beam era's small units 1.48× Level 10's, `BeamEraHpTest`; 1.50×, the band's top, with step C3's autopilot
      and the Fall section back at 100 px/s in step C5's full check: round 75 C.9)
- [x] Radio voiced, every timed line at most 1 s late (`RadioTimelineLevel37Test`, with Rook and without); Aurelia
      Traffic cast; the Choir distorted; Vorne on the enemy channel, not shouted (step VOX-C: 26 lines; step C4b: the
      timed lines retimed to the typed pages, opening at most 0.8 s late; the event barks, the first shuttle lost and
      the locker's line go stale in the runs and an early outcome waits to t≈140.5: round 75 L.3; step C6a: one page
      each, retimed, see *Radio chatter*)
- [x] Threat profile, Varga's four intel lines and Rook's teaser fitting the intel panel (`IntelPanelLayoutTest`); the
      Mirror Interceptor's and Graft Turret's portraits (step ART-C1: the portraits; step C4b: `IntelPanelLayoutTest`)
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their images (step C2: the
      pages shortened to 4 lines, round 75 L.1; step VOX-C: voiced; step C4b: the images re-run from the production
      frames)
- [x] Typical haul within ±5 % of budget(37) = 7 997 with the level's `bounty_scale` (`TypicalHaulTest`; step C2:
      +1.0 % at 0.32)
- [x] Music and ambience: track 14, the duck, track 15 from the turn, the city bed and its turn, the fall's sounds
      (`Level37SoundsTest`) (step AUD-C: track 15 and its stems, the r75 a variants; step C4b: the theme change in
      `LevelMusic`, `AureliaCues`)
- [x] Easy/hard variations as in *Difficulty notes*; the autopilot (flanking a mirror, closing a nest, not guarding
      the shuttles) flies the level with Rook on every difficulty and with `--escort none` (step C3, `Level37Test`: 3/3
      with no retry on every difficulty, with Rook and without; the secondary met in every easy and medium run, on hard
      in none of step C5's six runs (losses at t≈57 and t≈136-139): round 75 L.2)
- [x] Round 75's follow-up (F.3, L.2 a, L.3 a, L.4 a; step C6a): the shuttles take the player's fire (`player_hits`)
      and hold 3 s, slide aside to the side edges and climb out along them (`hold`, `aside`; step C6s's simulation);
      no enemy hovers or flies in behind a shuttle in the line of fire (`ShuttleLaneTest`); the t=134 Motes, the
      nests, the Hornets and the rear ambush off the stations, slides and edge lanes; the timed radio lines to t=140
      one typed page each and retimed; the autopilot never shoots a shuttle (`AureliaPlay`); hard 6 of 6 in most runs
      (`Level37Test`) (10 of 16 over 8 seeds); `levelReport` at
      medium: 117.8 a minute, 4.19 on the screen, the gaps 4.6 s, 3.6 s and the end's 10.2 s, no hulls stacked, the
      haul +1.0 %, the beam era's small units 1.45× Level 10's (was 1.50×); the content's check green but
      `VoiceFilesTest` on the 13 changed lines (VOX-C2)
- [x] Round 75's redo and picks (2026-11-01): the fall redone (F.6 redo; step ART-C3 `2d056270`: the halves fade from t≈161.5
      to nothing at t≈194 under two cloud veils, no sinking city, the cut was the sinking city's own canvas; step C6b
      `770065c7`: the fall in the game, no separate sinking city), the shuttles take the player's fire and slide aside
      (C6s `16ffc420`: the aside path; C6a `12899be1`: the shuttles' keys, waves and nests off the lanes with the flown
      `ShuttleLaneTest`, the t=137 Motes rerouted, one-page radio lines; a spark on a shuttle hit, `770065c7`), the 13
      lines re-voiced and the intro's page 4 by a per-line shift (VOX-C2 `5b346fec`), the picks S.1 b, S.5 b and S.8 b
      in the game (C6b)
- [x] Production art and captures for part C's round (steps ART-C1, ART-C2, C4b; step C5: the captures,
      [round 75](../../../concept-rounds/round-75/README.md))

## Open questions

Left to the build (the document is approved as drafted):

- **The turn's moment**: set at **t=100** (the working note's item): earlier would leave the shuttle pairs no calm half,
  later would crowd the turn into the fall. The window t=97–103 holds no new wave, as Level 33's Choir window.
- **The shuttle secondary's target**: 5 of 6 at medium (a loss allowed); our reading of "a count of N with the HUD
  counting". 4 of 6 would let it be met before the turn.
- **Mirror Interceptors never prey** (a call of this draft): the Vrell hunt Talons, the Gunship and the Hornet; a
  mirrored unit as prey would turn its mirror away from the ship and muddle Q1's lesson.
- **Late and stale event lines** (open after round 75, noted for the user): with the shorter timed lines, easy's
  secondary-met line (t≈84.5) and an early failed line (t≈81) still wait until t≈107.4, after the Choir (23-26 s late); a
  shuttle lost while pair 1 or 2 is up, and the Gunship prey kill at t≈118.5-119, still go stale.

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
- 2026-10-31 (step ART-C2, decided by me as trivia, decided 2026-11-01 (round 75 F.5, F.6 and C.8 accepted; the fall redone, below)): the city's ground is one
  continuous field cut into back-to-back ground pieces (no repeating ground tile set: its landmarks stand where the
  waves need them); the deep layer a cream cloud deck (`cloud-deck`) opening on the bands in section 5
  (`cloud-bands`); the turn's darker grade the `smoke` atmosphere (haze 0.35 toward `5a443c` over the deep layer, the
  `smoke-wisps` on `high-air`) with the burnt, overgrown city baked into every row entering after t=100; the halves'
  curves (west to 0.38 of its size and 64 % dark, east to 0.46 and 56 %, drifting 30 / 26 px apart and turning −6° /
  +5°, the ground's speed easing out over about a second) and the sinking city crossfading in at t=179–181 (replaced by the F.6 redo: the halves fade under cloud veils).
- 2026-11-01 (step C2, decided as trivia, decided 2026-11-01 (round 75 C.5 accept for the bounty scale 0.32, C.6 the retimed mirror waves, C.7 the six Talon pairs, C.8 the smoke atmosphere)): the level's data on ART-C2's
  proposal. **Retimes**: the t=84 mirror pincer at **t=89** (the t=77 Hornet's hover overlaps the stations until
  ≈ t=88; the t=88 Mote swarm takes t=84), the rear ambush at **t=131** (the t=116 Gunship drifts through its bottom
  stations until ≈ t=129), the t=140 pincer at **t=145** (the t=133 Hornet's hover; hard's 2 + 2): no two `medium`
  hulls overlap; **six firing Talon pairs** on the offbeats (t=17.5, 72, 86.5, 107.5, 124, 159; Level 36's lever) for the
  floor's pauses; the level's **`bounty_scale` 0.32** (0.30 flew −6.3 %). The Choir's line at t=100 with its static
  from t=98 (Level 33's convention); the old theme out over 1 s at t=100 (a stem time `none`), track 15 from t=103. ART-C2's
  `smoke` atmosphere is the game's `medium` intensity (its look unchanged). Doc fixes (the gap check's §6): the hangar
  paragraph and the balance plan follow the built plan (the Tesla Coil Pod at the L36 visit, the Ion Beam L5 at this
  one); "flagged `neutral`" reads "marked friendly" (Level 10's air allies, not the `neutral` flag). The two briefing
  pages shortened to 4 lines each (round 74 L.2 a's rule for Level 36; `BriefingLayoutTest`: the drafts ran 5 lines).
- 2026-11-01 (step C5): part C closed out: the full `./gradlew check` green (4,961 tests; `Level37Test` re-flown with the
  Fall section back at 100 px/s: 3/3 with no retry on every difficulty, with Rook and without; the secondary met in
  every easy and medium run and in none of the six hard runs; the beam era's small units 1.50× Level 10's); the
  captures for round 75 (the mirrors, a nest's roots, the shuttles, the turn, the hunt, the fall, the readability crops,
  the locker); the radio prose to step C4b's retiming. Round 75 asks the briefing pages (L.1), the hard shuttles (L.2),
  the crowded radio (L.3) and the Choir's static (L.4).
- 2026-11-01 (round 75 follow-up, step C6a; the user's answers F.3, L.2 a, L.3 a, L.4 a): the shuttles take the
  player's fire (`player_hits`, as Act 5's marked craft) and leave the centre lane: 3 s on station, 1.5 s sliding aside
  to x 90 / 390 and 40 px lower, then 3 s up along that edge (C6s's simulation). No enemy hovers or flies in behind a
  shuttle in the line of fire (`ShuttleLaneTest`, flown as `HullStackingTest`: a static check cannot see the formations'
  paths): the t=77 Hornet to the right third, the t=80 snake down the middle, the third pair's waves retimed (Talon pair
  t=124 → 132 left, Hornet 133 → 135, rear ambush 131 → 136.5, Motes 134 → 137 down the middle, snake 137 → 140, pincer
  145 → 149); the dome rim and terrace nests moved on their decks off the stations, slides and edge lanes (the backdrop's
  generator still lists their old places: for the art step). The timed radio lines to t=140 and Okafor's "hunting" line
  cut to one typed page each and retimed (Varga's turret warning back to the first nest, t=16.5): the bounce bark, the
  locker's line, most first prey kills and the third pair's first loss find their gaps; a loss in the first two pairs
  still goes stale (the gaps went to the bounce and the locker). The autopilot never shoots a shuttle. Hard 10 of 16
  runs all six home (was 0 of 6), the armour kept; the beam era's small units 1.45×.
- 2026-11-01, round 75 (user, closed; page answers): **every row as recommended except S.1 b, S.5 b, S.8 b, V.4 b and
  F.6 redo**. Art F.1, F.2, F.4, F.5, F.7-F.11 accepted; **F.3 accept a** (the livery) **with a note** on the live capture:
  with friendlies on the screen the gunship cannot fly in behind them, and the capture fired straight through them. The
  user's decision after it: the shuttles are like Act 5's marked craft, **the player's fire hits them** (a shot-down
  shuttle is a lost shuttle), and after about 3 s on station each slides aside and a little down to its own side edge
  and climbs out there (*The shuttles*; the allies' rule in [allies](../../../allies/README.md#common-rules), built in
  C6s `16ffc420` and C6a `12899be1`). **F.6 redo** (the user: "The end is not as it should be. The platform should
  disappear in the clouds completely. During the fall it should fade slowly. Also, in the last frame some parts are cut
  off at the bottom."): built in ART-C3 `2d056270` (the halves fade, cloud veils; the cut was the sinking city's own
  bounds) and C6b `770065c7`; the new review `fall-final-r75-b` stays `proposed` (the user sees it in a later round),
  `backdrop-final-r75-a.gif` and `fall-capture-r75-a` are rejected (redo). Sounds: S.2-S.4, S.6, S.7, S.9-S.11 a; **S.1 b**
  (pa-chime), **S.5 b** (aurelia-burning), **S.8 b** (the Graft Turret's own shot, not the Talon's file); the other
  variants in the sfx part's `concept/rejected/`; M.1 ok (track 15); voices V.1, V.3 accepted, V.2 a, **V.4 b** (round 74's
  take of the intro's page 4 exactly, VOX-C2 `5b346fec`); L.1-L.4 a (the shortened briefing pages, the hard shuttles' data
  moved with the armour kept, shorter timed radio lines, Okafor's t=89.5 line one page); C.1-C.9 accepted (every "pending
  the user's review in round 75" note is now "decided 2026-11-01 (round 75 <id>)"). `art` stays `proposed` until the
  redo's review (`fall-final-r75-b`).
  **Open for the user (not decided; measured after the shorter lines):** easy's secondary-met line (t≈84.5) and an early
  failed line (t≈81) still wait until t≈107.4, after the Choir, 23-26 s late; a shuttle lost while pair 1 or 2 is up, and
  the Gunship prey kill at t≈118.5-119, still go stale.
- 2026-11-02 (user, round 79 H.2 closed, ok, no note): the redone fall (`fall-final-r75-b`: the halves fading slowly and
  gone in the clouds, nothing cut off at the bottom) accepted; art `chosen`.
- 2026-11-03 (user, Act 6 playthrough; step PT2): "a flame burning in open air, about halfway, close to the first
  damaged platform". Cause: `backdrop_l37.py`'s deck search gave up within 120 px and kept the wanted x, so the t=133
  deck fire stood wholly over the open cloud and the t=118 one hung over a deck's edge. The search now needs the fire's
  whole footprint on the city (an open deck first, any deck surface next, t moved by up to 4 s) and fails rather than
  falls back: the fires at t=118.25 x 120 and t=132.5 x 357; the t=104 and t=148 fires were on their decks. Held by
  `--check` and `Level37FiresTest`.
- 2026-11-03 (user, on PT2's capture; step PT3): the t=118.25 fire still hung half off its deck's lower-left corner.
  Cause: PT2's rule held only a 3x4 grid of points within 12 px of the centre, the flames reach 17 px aside and 31 px
  down. Now the fire's whole drawn footprint (the flame sprite's alpha >= 128 extent over its frames, plus 6 px: 47x47
  px from 23 px left to 23 right, 9 up to 37 down of the centre) must lie on a deck's outline (octagons' cut corners
  off it): an open deck surface first, over every t step, a dome or pad on a deck only as the fallback. New places:
  t=100.5 x 287, t=118.5 x 111, t=132.75 x 328, t=148 x 160. Held by `--check` and `Level37FiresTest` (every pixel
  of the box, the box read from the sprite).
- 2026-11-03 (user, on PT3's capture; step PT4): every deck fire ended in a flat horizontal cut: the `deck-fire`
  flames ran solid to the sprite's bottom edge. The sprite is now 64x96 (8 px more left and right, 16 px more top and
  bottom, its centre on the same point of the fire) and fades out inside it: the flame body rounds off under its base
  into a soft orange glow on the deck with a few embers, every canvas edge at alpha <= 8 (held by `--check` and
  `Level37FiresTest`). The footprint grew by 1 px down (to 38 px), which moved the t=100.5 fire to x 286.
