---
title: Level 14 – Siege Spire
design: approved
implementation: done
art: final
depends-on: [../../../enemies/bosses/siege-spire]
updated: 2026-10-31
---

# Level 14 – Siege Spire

## Summary

The Act 2 finale. A three-kilometre Vrell citadel, the **Siege Spire**, has grown through the
heart of **Geneva Concord**, the UTC capital, and its canopy shadows the whole old town. The
Coalition throws everything it has left at it. Lancer and Rook go in first, along the lakefront
and through the old town, where the Spire's roots burst out of the streets and grow guns, to the
Concord rotunda where the trunk stands. There they fight the act boss, the
[Siege Spire](../../../enemies/bosses/siege-spire/README.md). The boss sums up the act: ground
targets first, then attacks from the rear, then dodging in the open. 108 seconds of approach at
140 px/s, then the boss fight (tuned to about 135 s, measured in the content step; round 40: about
103 s with the lighter roots), then a 12 s
daylight section, a 25 px/s drift.

**Status (M5 part H, step H4).** The level's [data.yaml](data.yaml) exists and the marked tables
below are rendered from it (step H4: the loader, the Spire's data, the measured fight); the backdrop
in the data is a placeholder until step H5. The design was decided on 2026-10-11 for M5 part H
(decisions H1–H10 and the stated defaults of the part's gap analysis, see *Decisions*). **The user is
on vacation: every decision here was taken as the recommendation and is accepted in round 34 (2026-10-12).** The most important one changes an approved
spec: **H1 = a drops the approved 15 px/s creep of boss phase 1** (see *Layout* and *Decisions*); the
user must confirm that. Step H4 added two more for review: **the root field scrolls at 80 px/s** (not
140) so the root-burst growths stand on the screen once they fire, and **the Spire's paid bounty is
26.8 % of the budget**, not 30 % (see *Credit budget*).

## Briefing

Two pages, as Levels 08–11, each **one screen** (`BriefingLayoutTest`): Okafor's mission (the
Spire and the way in), then Varga's page on the roots and what to bring. The text of the approved
single page is split between the two, with the unchanged sentences kept. Draft texts, for review
in concept round 36:

<!-- data: briefing -->
> **Commander Okafor:** "Lancer, Aegis Actual. Something grew out of Geneva Concord two nights
> ago: the Siege Spire, three kilometres tall and still growing. Your flight goes in first,
> along the lakefront and through the old town to the rotunda. Destroy it. Aegis Actual out."
>
> **Dr. Varga:** "The Vrell fight as one, and I believe the Spire binds them. Its roots run
> under the old town and grow guns where they break the surface. Missiles find the root guns
> while you dodge; bombs crack them. When the crown opens, look behind you."
<!-- /data -->

*Images* (production art straight away, H8 = a; [briefing images](../../../ui/briefing/README.md)):
Okafor's page `level-14-spire-over-geneva` (the Spire over Geneva seen from the lake, the canopy
over the old town), Varga's `level-14-root-scan` (her scan of the root grid under the old town,
the burst points marked); the level's data names them as each page's `image` once step H5 has
rendered them.

*Hangar teaser* (shop screen before L14; Rook, `LT. K. TANAKA / AEGIS TWO`, text only, not
voiced, as Levels 10 and 11; the approved unattributed text is given to him, shortened in step H4
to fit the intel panel, `IntelPanelLayoutTest`, as Varga's L2 and L3 lines below):

<!-- data: teaser -->
> **Rook:** "Roots first, flyers from behind, then the Spire. I'll take the six o'clock."
<!-- /data -->

*Varga's intel line* per sensor-suite level (the data's `varga`; draft texts for round 36; fitting
the intel panel is checked by `IntelPanelLayoutTest`, which may force shorter lines as it did for
Level 11; the approved single line is the L3 line):

| Sensor suite | Line |
|---|---|
| none | "Geneva Concord, Lancer. Something has been growing here for two nights, on the ground and in the air." |
| L1 | "Mostly from ahead. Roots break the streets: watch for the dust." |
| L2 | "Root guns, hive nodes, cloaked Wraiths. And the Spire." |
| L3 | "Bring anti-ground. When the crown opens, look behind you." |

## Threat profile

Rendered from the level data (the intel's rule counts each wave's `from`); the draft's "front 71 /
rear 29" counted a different wave list. The maw's Wraiths loop to the rear in the fight but are not
edge waves, so they are not in the numbers.

<!-- data: threat-profile -->
| Field | Value |
|---|---|
| Dominant layers | `ground` (the roots, the root-burst growths, the hive nodes, Creepers, Ravagers), `air`, `high-air` (cloaked Wraiths under the canopy) |
| Attack directions | front 88% · sides 10% · rear 2% (the approach by its waves; in the boss fight the maw's Wraiths loop round to the rear) |
| Density | 4 |
| Recommended traits | `homing`, `anti-ground`; `rear` helps against the Wraiths (three traits do not fit the intel panel) |
| Hazards | Root bursts (telegraphed by dust, ground layer, no collision); low light under the canopy |
| Boss / mid-boss | Act boss [Siege Spire](../../../enemies/bosses/siege-spire/README.md) |
| Sensor-suite detail | **none**: Geneva Concord, `ground` + `air` + `high-air`, front. **L1**: + the directions, density 4, hazard "root bursts", the objective `DESTROY THE SIEGE SPIRE`. **L2**: + Creeper, Ravager, Hive Node, Wraith, Needler, Skitter, Spine Turret, Polyp Mortar portraits; the boss's name and silhouette (`siege-spire`). **L3**: + `homing` and `anti-ground` highlighted; the timeline strip marks the root field (60–95 s) and the boss (108 s); 1 secret. |
<!-- /data -->

## Objective

- **Primary** `reach-end` with the arena as the condition, as Level 07: the scroll halts at the
  arena and the level ends when the Siege Spire is destroyed and the daylight section has run.
  The briefing and the objective line read `DESTROY THE SIEGE SPIRE` (the boss's name on the
  bar). It is not a `boss` primary.
- **Secondary** "Roots" (`groups`): destroy all **8 root-burst growths** (4 Spine Turrets and 4
  Polyp Mortars; **easy 4**, Spine Turrets only) before they leave the screen. A growth counts
  from its placement; "leaving the screen" is the bottom edge, as for any ground unit. The
  tracker reads `ROOTS 3/8` (easy `n/4`). Pays **63** credits in Act 1 terms (101 in Act 2, the
  credit factor 1.6); the typical haul counts it at the economy's secondary share. Okafor's
  line plays when it is met.

## Layout

Approach at **140 px/s** (the chosen Geneva scene), **108 s** of it, with **the root field at
80 px/s** (step H4, accepted in round 34 (2026-10-12): a root-burst growth appears 1 s after its dust and
fired 1.5 s later (0.75 s since step H7b), and at 140 px/s it would by then have scrolled 350 px from its burst, out of the
player's reach before the bottom edge; at 80 px/s it stands about 3 s on the screen once it fires).
The **boss arrives at t=108**: the arena section starts then, with speed 0, and the boss warning, the
boss's arrival (its bar, the sting, track 22) and the boss checkpoint are one moment. **Round 41** (the
user's playtest 2026-10-19: "put a small pause in between the last enemies and the fly towards it; show the
warning box before you reach it"; accepted in round 34 (2026-10-12)): the warning (box, klaxon, track 22) opens
**7.5 s before the arrival**, at t=100.5 (`boss.incoming`), and closes before the Spire's roots enter the top
edge (t≈105.5); the last wave enters at t=97.5 (two waves dropped, four moved earlier, see *Waves*), so the
autopilot's screen is empty from about t=101.5 (medium) or 102.5 (hard) until the roots appear, and the Spire
holds its fire 1.5 s after the halt (its Roots phase's `delay`); the bar and the checkpoint stay at the
arrival. A retry from the boss checkpoint warns at the arrival as before. **The 15 px/s
creep of boss phase 1 in the approved spec is dropped** (H1 = a, accepted in round 34 (2026-10-12)): the
built anchored arena needs speed 0, and all six roots stand on the screen from the start. "In turn"
comes from the staggered fire (pods 0.4 s apart, each every 1.6 s, the mortar roots every 8 s since
round 34's P2) and from the player's choice of root: the inner pods' guns turn toward the ship and the
outer pods fire straight down their columns, so the player's x decides the exposure. The
arena runs from t=108 to t=250; the level clock halts at the arena's end while the Spire lives, and a
kill jumps the clock to it (Level 07's `arenaJump`), the scroll ramping up over 1 s into the 12 s
daylight section, which **drifts at 25 px/s** (steps H7b and H9, accepted in round 34 (2026-10-12): at 140 px/s the
stump scrolled off about 4 s after the kill, before the light fell on it, at 40 px/s about 9 s after it,
10 s before the debrief; now it stays on the screen through the daylight's beams until past the section's
end, the scroll running on to the debrief). Total 262 s on the level clock and 13,320 px of ground (the scroll column
below). Motion budget: the strong elements are the street traffic and lamps' flicker in sections 1–2
(abandoned cars standing still, only a few moving) and the root bursts and dust in section 3. The
canopy shadow drifts slowly. In the arena everything moving drives off real seconds, not the halted
scroll.

<!-- data: level-sections -->
| Section | t (s) | Scroll (px) | Speed (px/s) | Atmosphere | Layers and content | Purpose |
|---|---|---|---|---|---|---|
| 1. Lakefront | 0–20 | 0–2,800 | 140 | light | `deep`: Lake Geneva at dusk, dark water. `ground`: the lakefront quays, the Jet d'Eau plume (leaning away from the screen centre, perspective rule), the Rhône bridges, a CDF armoured car half under creep on the Pont du Mont-Blanc (the secret). `low-air`: Hammer flight bombers passing below (scenery). `high-air`: the canopy's edge as a soft shadow (≤ 40 %). | Set-up; light popcorn from t=7. |
| 2. Old Town | 20–60 | 2,800–8,400 | 140 | light | `ground`: perimeter blocks of row houses with ridged roofs and courtyards, the cathedral towers (leaning, perspective rule), sodium-lit streets, parks and tree rows, creep veins along the gutters, the Place du Molard with two hive nodes. `high-air`: canopy shadow. | Creepers in the streets, a turret nest on the cathedral square, the hive-node pair. |
| 3. Root Field | 60–95 | 8,400–11,200 | 80 | medium | `ground`: streets broken by roots as thick as trams; four root bursts split the paving (1 s dust telegraph each) and grow a Spine Turret and a Polyp Mortar; creep over the roofs. `low-air`: spore-haze banks lit violet from below. | The signature set piece: roots growing guns. Ravagers out of a crater; the first Wraiths from behind. |
| 4. Concord Approach | 95–108 | 11,200–13,020 | 140 | medium | `ground`: avenues converging on the Concord rotunda's roundabout; the Spire's trunk rising out of it, leaning away from the centre; roots radiating along the avenues. `high-air`: the canopy directly overhead. | Last Wraiths; the Choir's song; the trunk grows on the horizon. |
| 5. Siege Spire | 108–250 | 13,020–13,020 | 0 | clear | `ground`: the arena around the rotunda, the trunk at the top, four turret-pod roots and two mortar roots spread along the avenues, all on the screen. Phase 3 the stump and the ground debris after the tear-free shockwave. `high-air`: canopy shadow only. | The boss fight (≈ 135 s at medium); the warning, the arrival and the checkpoint at t=108. |
| 6. Daylight | 250–262 | 13,020–13,320 | 25 | clear | `ground`: no enemies, no boss. The canopy withers and tears; low sunlight falls across the old town, the lake and the stump. | Release; act complete. |
<!-- /data -->

## Waves

Enemy definitions are linked per row; formation names come from the
[formation vocabulary](../../../enemies/README.md#formation-vocabulary). Returning units use the
act HP factor from the [balancing basis](../../../enemies/README.md#balancing-basis) (the Creeper
40; Hive Node, Ravager, Wraith, Mantis and Spore Bomber at their L14 values; none for the Siege Spire,
whose first level this is; of the small units only the Needler and the Stinger, capped at ×3, since
step WB: `small_act_hp`); HP values live in the enemy specs only.

**Density and pacing** (step WB, 2026-10-19, the user's P.e "yes, all" of round 40: the L10-style
waves steps CA and WA gave Levels 08–09 and 12–13). A wave about every **2.5 s** from t=5.5 to the
arena, the next entering while the last is still on the screen: Needler groups of 4–5 (12 HP: they live
to fire) and Stinger columns of 3 between Skitter streams and snakes, and returning medium units that
stay and fire: four Creeper convoys of 5 up the streets (the draft's two of 3 enlarged, two new in the
root field and on the avenue), three Spore Bomber lines and two Mantis pincers. Kept: the Ravager pack
out of the crater (t=72), the two rear Wraith ambushes, the ground targets and the root bursts. The
introduction of the root bursts keeps a quiet window: no wave enters from t=59 (a Spore Bomber line
that lingers to t≈62) until t=66 (the first burst's dust at 63; 67 before, when the screen stood empty
for 2 s once the first growths died). At medium **181 units in 43 waves** (4.2 a wave) before the arena,
**≈ 106 a minute** (`DensityTest`, at least 50). The Act 2 rules (`PacingTest`, `ActFloorTest`, flown
with the plan's fit; the floor leaves out the Spire's fight, which has its own tests): at medium **2.76
enemies on the screen, 23.3 enemy shots a minute, 1 pause over 2 s** (the daylight), at hard 3.31, 39.3
and 1 (before: medium 0.53, 3.1 and 12; hard 0.94, 8.3 and 4). Hard is harder by its levers (HP, fire
rate, formation sizes, the growths' earlier first shot, the extra turret nest) with the Needler and
Stinger groups one smaller and the late Spore Bomber lines of 2 (`hard: {count}`). The t=82 Wraiths
enter from the rear on medium and hard; on easy a Needler V-wing from the front replaces them (a Wraith
flies only the rear ambush).

<!-- data: waves -->
| t (s) | Section | Formation | Enemies (link) | Count | Enter from | Notes |
|---|---|---|---|---|---|---|
| 5.5 | 1 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | Popcorn over the lake as the launch ends |
| 8 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 10.5 | 1 | line abreast | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front | |
| 13 | 1 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | Over the bridges |
| 15.5 | 1 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 18 | 1 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | Over the rooftops |
| 20.5 | 2 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | Returning: one from each side, edge warnings; sweeps for 6 s |
| 23 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 25.5 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 28 | 2 | convoy | [Creeper](../../../enemies/ground/creeper/README.md) | 5 | front | Along the Rue du Rhône |
| 28.5 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 31 | 2 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (alternating edges) | |
| 33.5 | 2 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | |
| 36 | 2 | pincer | [Needler](../../../enemies/air/needler/README.md) | 4 | sides | 2 per side, hold 4 s |
| 38.5 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | Weaves between the cathedral towers |
| 40.5 | 2 | V-wing | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | Over the cathedral square's turret nest |
| 43 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 45.5 | 2 | pincer | [Mantis](../../../enemies/air/mantis/README.md) | 2 | sides | |
| 47.5 | 2 | convoy | [Creeper](../../../enemies/ground/creeper/README.md) | 5 | front | Down from the cathedral hill |
| 48 | 2 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 50.5 | 2 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 53 | 2 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 55.5 | 2 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 4 | front | Over the Place du Molard |
| 58.5 | 2 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | The last before the first root burst; no wave enters from t=59 until t=66 |
| 66 | 3 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | front (left) | After the first burst's quiet window |
| 68.5 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 72 | 3 | pack | [Ravager](../../../enemies/ground/ravager/README.md) | 4 | front | Out of a root-burst crater; the leader drops an overdrive |
| 74.5 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 77 | 3 | convoy | [Creeper](../../../enemies/ground/creeper/README.md) | 5 | front | Up a street broken by the roots |
| 77 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 4 | front | |
| 79.5 | 3 | line abreast | [Spore Bomber](../../../enemies/air/spore-bomber/README.md) | 3 | front | |
| 82 | 3 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Cloaked on `high-air` under the canopy; no edge warning or rear bark; easy: a Needler V-wing from the front instead |
| 82 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 3 | front | Easy only: instead of the rear Wraiths |
| 84.5 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (right) | |
| 87 | 3 | pincer | [Needler](../../../enemies/air/needler/README.md) | 4 | sides | |
| 89 | 3 | convoy | [Creeper](../../../enemies/ground/creeper/README.md) | 5 | front | Up an avenue toward the rotunda (round 41: from t=97, its last Creeper off the screen before the quiet approach) |
| 89.5 | 3 | column | [Stinger](../../../enemies/air/stinger/README.md) | 3 | front | |
| 92 | 3 | snake | [Skitter](../../../enemies/air/skitter/README.md) | 6 | front (left) | |
| 94.5 | 3 | V-wing | [Needler](../../../enemies/air/needler/README.md) | 5 | front | |
| 95 | 4 | rear ambush | [Wraith](../../../enemies/air/wraith/README.md) | 2 | rear | Round 41: from t=104, so that they are back out of the bottom edge before the quiet approach |
| 95.5 | 4 | stream | [Skitter](../../../enemies/air/skitter/README.md) | 5 | sides (alternating edges) | Popcorn from the sides on the avenues (round 41: from t=97) |
| 97.5 | 4 | line abreast | [Needler](../../../enemies/air/needler/README.md) | 5 | front | The last wave before the Spire (round 41: from t=99.5; the t=102 Stinger V-wing and the t=105.5 Needler line made way for the quiet approach: the screen quiet from about t=103 while the warning shows, until the Spire's roots enter at t≈105.5) |
| 108 | 5 | boss | [Siege Spire](../../../enemies/bosses/siege-spire/README.md) | 1 | front (anchored at the rotunda, scrolls in with the ground) | At the halt (the warning, the bar and the boss checkpoint); phase 2: the maw launches a Wraith every 8 s (hard 2) as ambushes that loop to the rear, at most 2 alive |

Totals: Skitter 57 · Needler 58 · Stinger 18 · Mantis 4 · Creeper 20 · Spore Bomber 9 · Ravager 4 · Wraith 4 · Siege Spire 1.
<!-- /data -->

The maw's launches in the fight (a Wraith every 8 s, hard 2, at most 2 alive, round 40; 2 and 3
before; three launches in the measured medium fight) are ambush plans, not edge waves: no bottom-edge warning and no rear bark since 2026-10-21 (a unit's own
re-entry; Rook's Trail formation still applies).

## Ground targets

<!-- data: ground-targets -->
| Section | Target | Effect |
|---|---|---|
| 1 | A CDF armoured car half under creep on the Pont du Mont-Blanc (trigger, t≈15) | 6 hits clear the creep; the cargo hatch pops open as the hidden crate |
| 2 | [Spine Turret](../../../enemies/ground/spine-turret/README.md) turret nest ×3 on the cathedral square (t≈38) | Bounty |
| 2 | [Hive Node](../../../enemies/ground/hive-node/README.md) ×2 in the Place du Molard, hardened (t≈55) | Optional: each releases 2 Skitters while alive (the budget counts one release each) |
| 2 | Spine Turret nest ×3 beside the hive nodes (t≈55), **hard only**; hard ×3 | Bounty |
| 3 | Root bursts ×4 (t≈63, 74, 84, 92), each growing [Spine Turret](../../../enemies/ground/spine-turret/README.md) + [Polyp Mortar](../../../enemies/ground/polyp-mortar/README.md) after a 1 s dust telegraph; easy Spine Turret | Invulnerable and silent for 0.75 s (hard 0.5 s); the secondary's group `roots` |
<!-- /data -->

## Hazards

- **Root bursts** (H2 = a, data-driven `root_bursts`: a time, a position, the growths per
  difficulty, telegraph 1.0 s, first-shot delay 0.75 s, hard 0.5 s (step H7b); the bursts at t=63 (x 150, 120 px
  below the top edge), 74 (x 330, 110 px), 84 (x 170, 140 px) and 92 (x 320, 100 px), each growth
  26 px either side of its burst's point, the Spine Turret on the left): the paving cracks and dust
  rises for 1 s, then a root breaks out and grows a Spine Turret and a Polyp Mortar on the ground
  layer. No collision. A growth takes **no damage and fires nothing while it grows**, then fires
  its first shot **0.75 s after it has appeared** (hard 0.5 s; step H7b, accepted in round 34 (2026-10-12): at
1.5 s the growths fired from about 220 px above the bottom edge, on top of the ship; now about
280 px). From then on it scrolls with the
  ground. A burst lies **100–400 px below the top edge** and never within **60 px of the ship's
  x** at the telegraph. The units count among the level's enemies from the burst, and in the
  secondary's group `roots` from their placement. The crater at the Ravager pack's entry (t=72) is
  a burst without growths (a backdrop and dust effect at the first burst's x; the pack itself
  enters at the top as usual). Easy: the growths are Spine Turrets only.
- **Low light**: the canopy darkens the old town (backgrounds at the dark end of the recede
  range). Enemy accents and bullets keep their full brightness.

## Secrets and pickups

- **Armoured car** (t≈18): six hits clear the creep off the car on the bridge, and its cargo
  hatch pops open with a CDF supply crate of **88** credits in Act 1 terms (**141** in Act 2; the
  draft's "140" was the Act 2 amount written into the data).
- **Overdrive** ×2: from the t=72 Ravager leader, and from the maw when it dies (phase 2 → 3).
- **Armour patches** ×2: a CDF supply drone (Level 13's, [allies](../../../allies/README.md)) flies
  in from the bottom edge at t≈96, releases its patch at t=100 at x 240, 380 px above the bottom
  edge (the data's `drops`, as Level 13's) and leaves the same way (step H9, accepted in round 34 (2026-10-12);
  it was a placed pickup that simply entered at the top edge), and the tear-free shockwave throws one
  up from the debris at the start of phase 3.

## Radio chatter

Spoken radio ([voice](../../../audio/voice/README.md)), as Levels 08–11, with Rook's lines
`requires: escort`. The draft's script is **retimed to the HUD's 1 s rule** (`RadioTimelineTest`,
with the voiced lengths of step H6): the draft's clash at t=106/107/108 on top of the boss
warning is gone, with the Choir at t=96 and Varga at t=102; **Rook's t=108 line "Here comes the
big one" is the boss-warning bark** (his AI says it at the warning, so the scripted copy is
dropped), and so is the draft's t=80 "Six o'clock, Lancer!" (his rear bark covers the t=82 and
t=95 ambushes; t=104 until round 41). Okafor's opening line holds the radio for about 12 s, so Hammer Lead speaks at
t=13 (his flight passes at t=16), Rook at t=21 and Varga's canopy line at t=31.5. The arena's lines are **events** (the
level clock is halted): `boss-phase` (phases 2 and 3), `boss-destroyed` and
`secondary-objective`; a phase's lines queue as `PHASE` lines (step H7b): they wait for a gap like
event lines but go stale only after 20 s, so Rook's phase-3 line, queued after Okafor's, plays (at
the 6 s of an event line it went stale behind it); a phase line still waiting at the kill is dropped.
The secondary's line, a closing line, **goes ahead of the Choir** when the roots die before it
(about t=93.5 in a normal run; step H9, accepted in round 34 (2026-10-12)): queued while the radio is free, it
opens at once and the Choir's and Varga's timed lines wait for it (about 8 s later, Varga's then over
the Spire's arrival). When the roots die late, while a line plays, it waits for a gap as before and
**once the boss warning starts it is dropped** (or cut if it opened less than 1.5 s before), since the HUD
shows the objective done, and Rook's boss-warning bark plays instead of going stale behind it (step
H7b, accepted in round 34 (2026-10-12)). The closing line is queued as `CLOSING` so that the daylight section
and the debrief wait for it.
The Choir's text line is voiced as in the earlier levels (the portrait frame full of static).

<!-- data: radio -->
| Trigger | Speaker | Line |
|---|---|---|
| t=1 | Okafor | "All units, Aegis Actual. Everything the Coalition has left is in the air over Geneva tonight. Make it count." |
| t=13 | Generic CDF (Hammer Lead) | "Hammer flight on station. Call your targets, Aegis." |
| t=21 | Rook | "Lancer, whatever happens in there, I'm on your wing. As usual." |
| t=31.5 | Varga | "The canopy is one organism. The roots feed the trunk. Kill the roots and the trunk starves." |
| t=54 | Varga | "Hive nodes in the Molard. Hardened, like Lagos." |
| t=62 | Okafor | "Roots surfacing in the streets! They're growing guns." |
| t=96 | The Choir (distorted) | "[the Choir sings]" |
| t=102 | Varga | "It's singing to the whole city. Lancer, that's the Spire." |
| t=110 (At the halt (round 40, 4c: the weapon hint)) | Rook | "Missiles on the roots, I'll keep the flyers off you." |
| Phase 2 (`boss-phase` Maw) | Varga | "The crown is opening. It's launching flyers, and they'll loop behind you." |
| Phase 3 (`boss-phase` Free spire) | Okafor | "It's tearing loose! Stay on it, Lancer!" |
| Phase 3, queued after Okafor's line | Rook | "Roots and all. Somebody call a gardener." |
| Spire destroyed (`boss-destroyed`) | Varga | "The canopy's dying. All of it, all at once." |
| Secondary objective met | Okafor | "Root grid in the old town destroyed. The engineers will thank you." |
| Level end (the closing line) | Okafor | "Every Vrell on Earth just lost its voice. They're scattering. We take the fight to them now." |
<!-- /data -->

Rook's phase-3 line is a second `boss-phase` cue for the same phase: it is queued after Okafor's
(no event delay exists; step H4, pending review; it waits as a `PHASE` line, step H7b). The car's
line plays when its secret is found.

## Boss / mid-boss

Act boss: [Siege Spire](../../../enemies/bosses/siege-spire/README.md). Its phases, HP and
attacks live in the spec. Arena notes for this level:

- The arena starts at t=108 with the boss bar and the checkpoint; the warning (track 22) opens
  7.5 s ahead, at t=100.5 (round 41; on arrival until then and on a retry from the checkpoint), and
  the boss theme starts after it. The
  trunk stands in the Concord rotunda at the top of the arena; the four turret-pod roots and
  two mortar roots break out along the avenues radiating from it, **all six on the screen from
  the start** (no creep, H1 = a).
- Phase 2: the maw's Wraiths come at the player from the bottom edge as ambushes (H3 = a): the
  sim adds no edge warning before a re-entry (rule of 2026-10-21; the 3 s warning of H3 = a is gone), Rook switches to
  his Trail formation, at most 2 alive at once (round 40: one a launch, 4 alive
  before).
- **Round 40** (proposals 4a–4c, step CB): the pods fire down their columns with safe lanes between
  and take turns to aim (a 0.6 s glow, one pod every 2.0 s); the maw's fan 7 orbs every 3.0 s; the
  roots 460 / 530 HP. The weapon hint is homing: the traits `homing`, `anti-ground`, Varga's briefing
  ("Missiles find the root guns while you dodge; bombs crack them."), the intel's L3 ("Homing
  missiles reach every root.") and Rook at the halt ("Missiles on the roots, I'll keep the flyers off
  you.", t=110). Measured: the plan's fit 103.4 s with 2.5 / 0 / 0 hits a minute by phase, the
  Pulse Cannon L4 alone 148–152 s, the user's playtest fit about 120 s
  ([Siege Spire](../../../enemies/bosses/siege-spire/README.md#stat-block)); the par 103 s.
- Phase 3: the freed spire lifts off its anchor over ≈ 3 s, rises to `air` and drifts across the
  upper half (H4 = a); the withered stump and the shockwave debris stay on the ground layer as
  scenery, with no collision (the stump is drawn from the boss's state, not as a backdrop
  piece).
- The Airstrike can hit the roots and the maw (ground targets), capped at 150 per part per
  strike by the [specials](../../../player/specials/README.md) rules.
- The boss checkpoint is recorded at the arrival, t=108, on easy and medium, per the
  [retry](../../../systems/retry/README.md) rules; there is none on hard.
- Bounty: **492 as paid** (29.2 % of the budget at step WB's scale of 0.40; 482 and 28.6 % at round 40's
  0.81, 452 and 26.8 % before; 768 in Act 1 terms since step WB, 372 before: pods and mortar roots 64
  each, maw 128, core 256 at the spec's ratios 60:60:120:240), see *Credit budget*. The maw's Wraiths pay
  their own bounty.
- The fight's length is **measured** (H10 = a; the [Siege Spire](../../../enemies/bosses/siege-spire/README.md#stat-block)):
  the autopilot's fights on the plan's fit over five seeds, the HP ×1.8 the spec's, a mean of 129.5 s (round 40: 103.4 s, the par 103 s)
  at medium (every seed 126.1–131.7 s; remeasured in step H6 after pod 1's offset fix), the par 130 s. Flown in this level (seed 2185) the fight
  lasted 97 s on easy, 136 s on medium and 204 s on hard.

## Music & ambience

The Act 2 B theme "Firestorm" (track 7 in the [track list](../../../audio/music/README.md#track-list))
for the approach: the base stem from section 2, the full mix from section 4. At t=100.5, 7.5 s before
the arena start (round 41; at the arena start, t=108, until then), the boss warning (track 22) plays and the Vrell boss theme "The Choir Descends" (track 18)
starts after it and runs through the fight, fading at the kill. The daylight section has **no
music**, and there is no mid-level jingle. After the kill the debrief plays the `mission-complete`
jingle as every level does, and the act complete fanfare (track 24) plays under the first outro
page (H6 = a, the Act 1 order), **not over the daylight section**. Ambience: the
[megacity ambience](../../../audio/sfx/README.md#ambience-per-setting) with the sirens replaced by
a low organic hum from the canopy, which cuts out when the Spire dies.

## Act end

Level 14 ends Act 2 and follows the order of Act 1's end (M4, H6 = a): the Spire's death →
the **daylight section** (12 s: the canopy withers and tears on a real-seconds timeline from the
boss's death, low sunlight, the closing radio line; no enemies, no music, the ambience without
the hum) → the **debrief with the act summary** (`mission-complete` jingle, the tally, the summary
`ACT II COMPLETE · HOMEFRONT`) → the **act outro**, four voiced pages (H7 = a; the data is
in the [Act 2 README](../README.md)) with `act-complete` under page 1 → the release's end. There
is no Level 15 yet: M6 part B (Q1 = a, accepted in round 35) follows the outro with the end card, the
name entry and the credits, then the main menu; Continue opens the hangar before L15, which reads
`ACT III - RED DUST: COMING SOON` and cannot launch (H9 = a had it read `BEFORE L15 - NOT BUILT
YET`). `--act-end` works unchanged for this level.

## Credit budget

Budget(14) = 700 × 1.07¹³ ≈ **1,687**, the typical haul's target
([economy](../../../systems/economy/README.md#per-level-budget)), not a perfect collection. The
draft's 2,410 was the old perfect-run curve with Act-2-scaled amounts. **The data keeps Act 1 terms**
(Skitter 5, Needler 12, Creeper 22, Ravager 18, Wraith 30, Hive Node 45, Spine Turret 12, Polyp
Mortar 15, the car's crate 88, the secondary 63); the code pays each × the act factor 1.6, and
bounties also × the level's **`bounty_scale` 0.40** (0.76 until round 40, 0.81 until step WB), which
puts the typical haul on the budget within ±5 % (`TypicalHaulTest`; step WB: 1,744, +3.4 %; the perfect
run 2,458, 1.46 ×; round 41, without the two dropped approach waves: 1,707, +1.2 %; the perfect run 2,396,
1.42 ×). The maw's Wraiths are budgeted at the measured fight's count (three launches
of 1 since round 40, of 2 before; the boss's `spawns`), the node-spawned Skitters at one release per node.

**The Spire's share (step H4, accepted in round 40 (2026-10-19)): 452 paid = 26.8 % of the budget**, not the
planned ≈ 506 (30 %). The typical haul sits on the budget and a perfect run must earn at least 1.4 ×
it (`TypicalHaulTest`): the boss pays the same in both, so every credit it takes off the scaled kills
narrows the gap between them; at 30 % the perfect run falls short (≈ 1.39 ×), at 26.8 % it reaches
1.42 ×. The act boss's share stays inside its tolerance (20–40 %, `BalanceTest`). **Round 40:** with
one Wraith a launch the perfect run fell to 2,289 (1.36 ×) at the scale of 0.76; the scale went to
**0.81** (the typical haul 1,722, +2.1 %; the perfect run 2,400, 1.42 ×), so the Spire paid **482
= 28.6 %** of the budget (accepted in round 34 (2026-10-12)). **Step WB (2026-10-19, accepted in round 34 (2026-10-12)):** the L10-style waves carry about twice the bounties, so the scale fell to **0.40**; at the
old 372 the Spire would have paid 238 (14 %, under the act boss's 20 % floor), so its bounty doubled to
**768** (pods and mortar roots 64, maw 128, core 256): every part pays what it did, **492 = 29.2 %**.

<!-- data: credit-budget -->
| Source | Perfect run | Typical haul |
|---|---|---|
| Kills: Skitter 57 × 5 + Needler 58 × 12 + Stinger 18 × 15 + Mantis 4 × 30 + Creeper 20 × 22 + Spore Bomber 9 × 25 + Ravager 4 × 18 + Wraith 4 × 30 + released Skitter 4 × 5 | 1,451 | 936 |
| Ground targets: Spine Turret 7 × 12 + Hive Node 2 × 45 + Polyp Mortar 4 × 15 | 154 | 123 |
| Boss: Siege Spire (pods 4 × 64 + mortars 2 × 64 + maw 128 + core 256) | 492 | 492 |
| Boss spawns: Wraith 3 × 30 (a typical fight) | 57 | 34 |
| Secret: armoured car (hidden crate, 5% of budget) | 141 | 70 |
| Secondary: destroy every root-burst growth | 101 | 50 |
| **Total** (bounty scale 0.4) | **2,396** | **1,707** |
| Budget(n) = the typical haul's target; typical +1 %, perfect 1.42 × budget | | 1,687 |
<!-- /data -->

## Difficulty notes

- **Easy**: the root-burst growths are Spine Turrets only (the secondary counts 4); a Needler V-wing
  from the front replaces the t=82 Wraiths (a Wraith flies only the rear ambush); the boss ×0.75.
- **Hard**: the Needler and Stinger groups one smaller and the Spore Bomber lines at t=58.5 and 79.5
  of 2 (`hard: {count}`, step WB); a second Spine Turret nest beside the hive nodes; root bursts grow 1.0 s after the
  telegraph instead of 1.5 s; the boss ×1.3 and its hard-mode hooks from the spec (3 Wraiths per
  launch, a 180° sweep, a ring of 12 for the lob). There is no boss checkpoint on hard.

## Concept art

Rejected proposals (2 rows) are listed in [concept/rejected/README.md](concept/rejected/README.md).

The look comes from the chosen Geneva scene in [art direction](../../../art-direction/README.md):
[scene-geneva-r10-a.png](../../../art-direction/concept/scene-geneva-r10-a.png) and its
[scroll loop](../../../art-direction/concept/scene-geneva-r10-a.gif); the boss follows its chosen
[Siege Spire](../../../enemies/bosses/siege-spire/README.md) concept. Concept round 36 (part H, H8 = a,
accepted in round 36 (2026-10-13)) auditions what has no concept: the **root burst** and the **daylight** a/b
here, the **tear-free roar** and the **canopy withering** a/b in the [sfx](../../../audio/sfx/README.md)
round; the armoured car, the outro images, the burst's sounds and the hum get one version; the Geneva
backdrop and the Spire go straight to production. Generator notes: [concept/prompts.md](concept/prompts.md).
Closed in the combined round 34 (user, 2026-10-12): the root burst **a**, the daylight **a**; everything
else approved as final and accepted, the Spire's death after a redo.

| File | What | Status |
|---|---|---|
| [concept/root-burst-r36-a.png](concept/root-burst-r36-a.png), [.gif](concept/root-burst-r36-a.gif) | Root burst, look a "breaking paving": cracks with a violet glow seeping through, dust, a slate root arching out with two knots, the growths swelling out of them (`tools/art/root_burst.py`; in the game until the pick: `--assets` wrote `assets/sprites/root-burst-decal`, `-puff`, `-root`, step H5) | chosen |
| [concept/daylight-r36-a.png](concept/daylight-r36-a.png) | Daylight section, look a "golden hour": warm grade, low-sun beams through the tearing canopy (`tools/art/daylight_l14.py`; still and timeline) | chosen |
| [concept/backdrop-final-r36-a.png](concept/backdrop-final-r36-a.png), [.gif](concept/backdrop-final-r36-a.gif) | Production backdrop straight from scene-geneva-r10-a (`tools/art/backdrop_l14.py`, notes in [concept/prompts.md](concept/prompts.md#backdrop-final-r36-a); its block for the level data, for the main agent to merge, is [concept/backdrop-proposal.yaml](concept/backdrop-proposal.yaml)): the city rebuilt from plans (perimeter blocks, tile and slate roofs, sodium lamps, the canopy's mottled shadow baked in): the lakefront with the lake, the old town, the root field with roots as thick as trams, cracked paving and collapsed houses, the daylight tiles; sections 4–6 one unique strip (the converging avenues, the Concord plaza under the Spire's anchor with six avenues along its roots, the daylight graded by its distance past the arena frame); the Rhone with the Pont du Mont-Blanc, Saint-Pierre's square and the Place du Molard cut from the tiles under them, the cathedral towers in perspective, the Jet d'Eau, the Ravagers' crater, boulevards over the tile seams, Hammer flight and the few moving cars; the spore haze and the canopy's veins; the game's daylight looks (the canopy tearing, the beams, look a); composites with stand-ins for the units and the Spire; the GIF the ease into the halt and the daylight after a kill | final (round 44, X.i) |
| [concept/l14-props-final-r36-a.png](concept/l14-props-final-r36-a.png), [.gif](concept/l14-props-final-r36-a.gif) | Production art (`tools/art/l14_props.py`): the CDF armoured car under creep, cleared hit by hit, hatch open with the supply crate | final (round 44, X.i) |
| [concept/act-2-outro-final-r36-a.png](concept/act-2-outro-final-r36-a.png) | Production art (`tools/art/outro_l14_images.py`): the Act 2 outro's four briefing images, the stump in Geneva at sunrise, the memorial wall of names at dawn, Ares Landing's priority signal from Mars, Aegis Wing and the Mars transport on the apron at dusk | final (round 44, X.i) |
| [concept/level-14-capture-final-r36-a.png](concept/level-14-capture-final-r36-a.png), [.mp4](concept/level-14-capture-final-r36-a.mp4) | Capture of the game (medium, a strong fit with Rook, **`--invulnerable`**: the dead-reckoning driver cannot survive the Spire on the debug start's cores): 115 / 117 kills, ROOTS 8 / 8, secret 1 / 1, the Spire in 1:26 (par 2:10), A+; nineteen frames (the briefing pages and the hangar from a won Level 13, one phase-1 frame from a run without `--invulnerable`) and the whole run with sound through the act end ([recipe](concept/prompts.md#level-14-capture-final-r36-a)) | final (round 44, X.i) |
| [concept/level-14-capture-fixes-r36-a.png](concept/level-14-capture-fixes-r36-a.png), [.mp4](concept/level-14-capture-fixes-r36-a.mp4) | Supplementary capture of the step H7b fixes (step H8; the level time burnt in): Hammer flight at x 130 clear of the car, the growths' first shot about 290 px above the bottom edge, the radio at the roots' end and the boss arrival (Rook's warning bark, his phase-3 line), the free spire over the ash stump, the withering 3 s after the kill, the daylight's 40 px/s drift; the bottom row a run **without `--invulnerable`** on the balance plan's L14 fit (cores included) with Rook, lost five times in the Spire fight ([recipe](concept/prompts.md#level-14-capture-fixes-r36-a)) | final (round 44, X.i) |
| [concept/level-14-capture-daylight-r36-a.png](concept/level-14-capture-daylight-r36-a.png), [.mp4](concept/level-14-capture-daylight-r36-a.mp4) | Supplementary clip of the step H9 changes (step CF; 47 s with sound, the level time burnt in, and a sheet; `--invulnerable`): Okafor's root-grid line at the roots' end (≈ 95) ahead of the Choir (≈ 105) and Varga (≈ 111), the CDF supply drone carrying the armour patch in from the bottom edge (t 96.5–103), the kill (≈ 194.5) and the daylight at 25 px/s: the stump on the screen through the beams, its last root tips leaving at ≈ 215.3 (≈ 21 s after the kill), the debrief at ≈ 217.5 ([recipe](concept/prompts.md#level-14-capture-daylight-r36-a)) | final (round 44, X.i) |
| [concept/act-2-outro-capture-r36-a.png](concept/act-2-outro-capture-r36-a.png) | Capture of the act end from the same run (a real kill): the daylight and the closing line, the debrief, the act summary, the four voiced outro pages, the hangar before L15 ([recipe](concept/prompts.md#act-2-outro-capture-r36-a)) | final (round 44, X.i) |
| [concept/level-14-fixes-r42-a.mp4](concept/level-14-fixes-r42-a.mp4) | Check capture of the playtest fixes (round 42, medium, the plan's L14 fit, the test autopilot): the last waves, the quiet gap, the Siege Spire warning box before it arrives, its sweeps, its death and the scroll resuming (144 s with sound) | final (round 42, K.b: accepted) |
| [concept/level-14-fixes-r42-a.png](concept/level-14-fixes-r42-a.png) | 20-frame sheet of that excerpt | final (round 42, K.b: accepted) |
| [concept/level-14-briefing-r44-a.mp4](concept/level-14-briefing-r44-a.mp4) | Varga's new briefing page 2 (the briefing shown before a debug level start by a snapshot hook, paged on by itself once the voice ends): the root-scan image, her text typed and voiced, the hand-over to the level. 17.5 s | final (round 44, X.c: accepted) |
| [concept/level-14-briefing-r44-a.png](concept/level-14-briefing-r44-a.png) | Briefing page 2 at 1:1 (960×540), fully typed | final (round 44, X.c: accepted) |
| [concept/level-14-fight-r44-a.mp4](concept/level-14-fight-r44-a.mp4) | The kinder Spire fight in the live game (plan fit: Pulse L4, Micro-Missile Pods L2, Tail Gun L2, Mk II, Composite II, Airstrike ×4, Rook's Autocannon; level t ≈ mp4 time + 90): the last waves, Okafor's root-grid line, "SIEGE SPIRE APPROACHING", the Choir, Varga, then **Rook's halt line "Missiles on the roots, I'll keep the flyers off you."** voiced at 0:28–0:34 (level t ≈ 118: it queues behind Okafor, the Choir and Varga), the roots' fire, the sweeps, the crown opening, the kill (≈ 2:05), the daylight, the debrief (A+, boss time 1:44 against a par of 1:43, ROOTS 8 / 8). 152 s | final (round 44, X.d: accepted) |
| [concept/level-14-fight-r44-a.png](concept/level-14-fight-r44-a.png) | 20-tile sheet of that clip + the radio panel on Rook's line, the Spire and the open crown at 1:1 | final (round 44, X.d: accepted) |

## Implementation

- [x] README reworked to the decided design: arena start = warning = arrival = checkpoint at
      t=108, no creep, density, budget(14), the act-end order (M5 part H, step H1a)
- [x] Level data `data.yaml` with every block below and this README's marked tables rendered
      from it; `PartHLevelKeysTest`, `Level14Test` (M5 part H, step H4)
- [x] Scroll timeline, sections, atmosphere intensity and parallax content per layer as in
      *Layout*; the arena section with `speed: 0` halting the scroll at t=108, the 12 s daylight
      section after it, primary `reach-end` (M5 part H, step H4)
- [x] Production backdrop `tools/art/backdrop_l14.py`: six sections, the canopy shadow and spore
      haze, the rotunda arena, the daylight grade; moving scenery on real seconds in the arena
      (M5 part H, step H5)
- [x] Root bursts (H2 = a): `root_bursts` with the placed ground unit, the 1 s telegraph, the
      invulnerable silent growth, the first shot after 0.75 s (hard 0.5 s; step H7b), the position rule,
      counted in the density and the secondary (M5 part H, steps H3 and H4)
- [x] Root-burst look: the cracking and dust, the root breaking out, the grow-in, the crater;
      look a, the user's pick in round 34 (M5 part H, steps H5 and H6:
      `RootBurstLooks`, `Level14LooksTest`)
- [x] Wave script matches the *Waves* table: at least 50 enemies a minute at medium from t=8 to
      the arena (`DensityTest`), no empty screen over 3 s on every difficulty (`PacingTest`)
      (M5 part H, step H4)
- [x] L10-style waves (round 40 P.e): at medium and hard at most 3 empty stretches over 2 s
      (`PacingTest`'s Act 2 rule), at medium ≥ 2.5 enemies on the screen and ≥ 10 enemy shots a
      minute outside the boss fight (`ActFloorTest`), the typical haul on the budget and the Spire's
      share about 30 % (step WB, 2026-10-19)
- [x] Boss arena per the arena notes: all six roots from the start, the maw's ambush launches
      with the dynamic bottom-edge warning and Rook's Trail, the lift-off, the stump, the
      checkpoint at t=108 on easy and medium and none on hard (`Level14Test`) (M5 part H, steps H3
      and H4)
- [x] The fight measured on the plan's fit (five seeds, every seed 90–180 s, the mean 129.5 s, the par 130 s),
      the HP tuned, the par and the bounty set (`SiegeSpireTest`, `BalanceTest`) (M5 part H,
      step H4)
- [x] Armoured-car secret (6 hits, crate 88 in Act 1 terms; its creep cleared blob by blob, a
      frame a hit, then the hatch open), overdrives, armour patches (the drone's patch as a placed
      pickup without a drone sprite; since step H9 carried in by Level 13's supply drone from the bottom
      edge, `drops`) (M5 part H, steps H4, H5, H6 and H9)
- [x] Radio chatter cues incl. the boss phase events, voiced, every timed line at most 1 s late
      (`RadioTimelineTest`, with Rook and without him); the closing line queued `CLOSING` (M5
      part H, steps H4 and H6)
- [x] Secondary objective `ROOTS n/8` (easy `n/4`) tracked and rewarded 63 in Act 1 terms, the
      debrief's row `ROOTS n / 8` (M5 part H, steps H3, H4 and H6; `PartHHudTest`,
      `LevelScreenPartHTest`)
- [x] Threat profile from the data, Varga's four intel lines and Rook's teaser fitting the intel
      panel (`IntelPanelLayoutTest`), the Spire's intel silhouette and the L3 strip's marks (M5
      part H, steps H4 and H5; `boss-siege-spire`, the root field's mark, `PartHHudTest`); the boss's
      mark at 108 s, a red tick over the waves, added in step K10 (`Intel.marks`,
      `IntelPanelLayoutTest.level14sStripMarksTheBossAtItsArrival`)
- [x] Briefing: Okafor's and Varga's pages, each one screen (`BriefingLayoutTest`), with their
      images (M5 part H, steps H4 and H5; checked in step H6)
- [x] Daylight section: no enemies and no music, the canopy withering and tearing from the boss's
      death, the sunlit grade; daylight look a, the user's pick in round 34 (M5
      part H, steps H5 and H6: `SpireLooks`, `Level14LooksTest`)
- [x] Act end: the debrief with the act summary, then the four outro pages with `act-complete`,
      then the hangar (`--level 14 --act-end`; `OutroOnwardTest`, `DebriefActEndTest`,
      `CampaignRouteTest`) (M5 part H, steps H4 and H6)
- [x] Typical haul at medium within ±5 % of budget(14) = 1,687 with the level's `bounty_scale`
      (`TypicalHaulTest`); the Spire's paid share ≈ 30 % (`BalanceTest`) (M5 part H, step H4)
- [x] Music: "Firestorm" base from section 2, the full mix from section 4, the warning at the
      arena start, the boss theme after it, none in the daylight; the canopy hum and its cut
      (M5 part H, step H6; `Level14SoundsTest`)
- [x] Easy/hard variations as in *Difficulty notes* (M5 part H, step H4)
- [x] Captures for concept round 36: the approach, a burst, the Wraiths' rear entries, the three
      phases, the lift-off, the death and the daylight, the act end (M5 part H, step H7)
- [x] Round 40 (4a–4c, step CB): the kinder Spire's data (its README), the traits `homing`,
      `anti-ground`, Varga's briefing page and intel L3 with the homing hint, Rook's line at the halt,
      the maw's Wraiths budgeted at 3 (`Level14Test`, `SiegeSpireTest`, `TypicalHaulTest`)
- [x] Round 41 (the user's playtest 2026-10-19; steps FX-S2, FX-G and FX-C, accepted in round 41 (2026-10-19)): the
      warning 7.5 s before the arrival (`boss.incoming`), the quiet approach (the t=102 and t=105.5 waves
      dropped; the convoy, the side stream, the Wraiths and the Needler line earlier), the Roots phase's 1.5 s
      delay, the leading sweep (the Spire's README) (`Level14Test`, `SiegeSpireTest`, `PacingTest`,
      `TypicalHaulTest`; the game's `BossWarningBoxTest`)
- [x] Round 40: Varga's new briefing page and Rook's halt line voiced (step VOX2, 2026-10-19; `VoiceFilesTest`), a
      capture of the kinder fight for the user's review (round 44, X.c and X.d)

## Open questions

- None open. All of H1-H10 were decided as recommended while the user was on vacation and accepted in round 34
  (2026-10-12); round 44 (X.h, 2026-10-20) closed the list (see *Decisions*): above all that the approved 15 px/s creep
  is dropped (H1) and that all six roots are in range at once.

## Decisions

Entries dated before 2026-10-21 (M8's start): [decisions-archive.md](decisions-archive.md).

- 2026-10-21: Edge-warning rule change (user, built by step EW): the maw's Wraith launches and the rear ambushes are a unit's
  own re-entries and are no longer edge-warned (H3 = a's 3 s warning and Rook's launch bark are gone); the Spire's own
  arrival keeps its boss-warning box (round 41), which is not an edge warning.
- 2026-10-30, the user: dither audit, ≥ 25 % smoothed (step DITH8): the daylight's `daylight-beams_0..1` after the Spire's death (frame 0 was one dither step over the field) in smooth steps, not the 4×4 Bayer checker ([audit](../../../art-direction/production/README.md#dither-audit-2026-10-30)).
