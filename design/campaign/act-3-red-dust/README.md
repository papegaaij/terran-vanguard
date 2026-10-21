---
title: Act 3 – Red Dust
design: approved
implementation: in-progress
art: final
depends-on: [../act-2-homefront, ../../world/mars, ../../story/twist]
updated: 2026-10-21
---

# Act 3 – Red Dust

## Summary

Levels 15–21. The Mars colonies are under siege. Aegis Wing descends past Olympus Mons, runs the
Valles Marineris to Ares Landing and holds its domes, sweeps a dust storm, retakes the Tharsis
Foundry, is driven back out of the Hellas basin and returns to kill the Dust Colossus. The first
hints of human involvement turn up here: unmarked grey drones flying with the Vrell, and a war
walker built with human engineering, while the Vrell leave every Helix site untouched. The act
introduces burrowing enemies, terrain collision with canyon walls, support and barrier units, a
visibility radius, reverse scroll and the first boss that breaches from any side.

**Status: approved in concept round 36 (user, 2026-10-13).** The seven level documents and this
README (the act intro and outro texts, the units, music, voice, the density across the act) were
written in the overnight design phase (2026-10-12) from the gap analysis's recommended options
(M1–M19), and the round approved them with every recommendation kept or accepted. The act's [data.yaml](data.yaml)
holds the levels, the title card and the intro pages since M7 part B (step B2), rendered below as
marked tables; the outro joins it in part H.

## Contents

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [level-15-olympus-descent](level-15-olympus-descent/README.md) | Act 3's opener: the descent through the cloud deck past Olympus Mons to the colony rail line; introduces the Burrower and the Dust Kite | approved | done | final |
| [level-16-valles-canyon-run](level-16-valles-canyon-run/README.md) | The canyon run east to Ares Landing: terrain collision, a fork, tendril barriers and the Threadcrawler; the Helix station untouched on the rim | approved | done | final |
| [level-17-dome-siege](level-17-dome-siege/README.md) | Hold Ares Landing's five domes against bombers and Shellbacks; Choir Herald and Shellback introduced; the Helix compound untouched | approved | done | final |
| [level-18-dust-storm](level-18-dust-storm/README.md) | Sweep a dust storm with visibility cut to a radius; Dust Devils; the Ghost Drone's first sight (hint 1); the act's breather | approved | in-progress | final |
| [level-19-foundry-of-tharsis](level-19-foundry-of-tharsis/README.md) | Tharsis foundry: grey Warden Tanks on conveyors, furnace vents, turrets that fire after you pass; mid-boss Revenant Walker and the hint-2 wreck crawl | approved | in-progress | final |
| [level-20-retreat-from-hellas](level-20-retreat-from-hellas/README.md) | The Hellas strike turns into a retreat: reverse scroll, a Mote Swarm pursuing from the top, ambushes from the bottom; Rook's quiet beat | approved | done | final |
| [level-21-dust-colossus](level-21-dust-colossus/README.md) | Act finale in the Hellas basin: rim, inner wall, dune sea and lime hollow (≈ 66 a minute, all directions, the ridge pass), then the Dust Colossus on a slow-crawling arena; the Choir's first words; Level 14's act-end order | approved | done | final |

## Design

### Synopsis

Mars has held alone for five weeks when Aegis Wing arrives. The Vrell came down in the Hellas
basin in the south and spread north; they own the sky over **Ares Landing**, the oldest colony,
on the edge of the northern plains. The wing drops in from the west past Olympus Mons, runs the
Valles Marineris east (the one road the Vrell do not hold from the air) and holds Ares Landing's
five greenhouse domes while the colonists shelter below ground. Twice on the way it passes a Helix
Dynamics site the Vrell have left untouched. A storm front carries Vrell and grey, unmarked drones
across the plains; in the **Tharsis Foundry**, a colony built with Helix money, a grey war walker
with its serial plates filed off guards the casting yard. A strike on the Vrell staging grounds in
the Hellas basin turns into a retreat to the rim when something kilometres long moves under the
sand. Aegis Wing goes back down, kills the Dust Colossus in the basin's lime hollow, and the Choir
speaks for the first time as the Vrell leave Mars.

### Story beats

- L15: Mars asks for help. The colonists are proud and resent Earth arriving late (Ares Control's
  first lines); the resentment thaws over the act and is paid off by his dinner line in L21.
- L16: the Helix research station stands untouched on the Valles rim. Varga observes it on the
  radio; Okafor deflects ("*Eyes on the canyon, Doctor.*").
- L17: the Helix compound at Ares Landing is the set piece: a Vrell swarm bends round its fence.
  Varga says it aloud; Okafor cuts her off on the radio ("*Not on this channel, Doctor.*") and,
  in the L18 briefing, tells the room it stays there (M13 = a: the private side lives in the
  briefings and the hangar intel, never the open radio).
- L18: **hint 1**. Varga's first sight of the grey Ghost Drones: "*That's not Vrell. That
  signature is... ours?*"
- L19: **hint 2**. The Revenant Walker's wreck: serial plates filed off by hand, the alloy Helix
  foundry stock. The scroll crawls past it (M15 = a); in the L20 briefing Varga is told to leave it.
- L20: a genuine retreat. The war is not going well. Rook's one quiet line in the lull
  foreshadows Act 4, where he is shot down (L26).
- L21: the Colossus falls and Mars is saved. The Choir's first words, "*…many… we are many… we
  are going…*" (M14 = a), as it dies. The Vrell pull back toward Jupiter's moons, and Varga
  tracks their fleet to Europa (the act outro).

### Act intro and outro

Format: the [briefing screen](../../ui/briefing/README.md), as Acts 1–2. Order: the L14 debrief
with the act summary → Act 2's outro (which ends on "Aegis Wing ships out for Mars in seventy-two
hours") → this act's title card → the act intro below → the L15 mission briefing → the hangar
visit before L15 (autosave) → L15. The intro continues from Act 2's outro, which already told the
Spire's death, the losses and Ares Landing's signal, so it opens on the arrival over Mars and does
not repeat them. After the Colossus falls, the act ends in Level 14's order (H6 = a): the
settling-dust section, the debrief with the act summary `ACT III COMPLETE · RED DUST`, the four
outro pages with track 24 *act complete* under page 1, then the release's end (moved from Level 14
to Level 21 in M7 part H), which autosaves the hangar before L22: `ACT IV - DEEP WATER: COMING
SOON` (see [Level 21](level-21-dust-colossus/README.md#act-end)).

**Timeline** (round 36, Q.24b): Act 2 plays in May 2185; Ares Landing's signal said three weeks; three days
to ship out and nine days at full burn put the arrival at **five weeks** of siege, in June. Level
15's summary, briefing and Ares Control's t=12 line say "five weeks" (changed from the draft's
"three weeks" when the levels were harmonised).

**Title card** (held 3.5 s over the act's still, `ui/act-3-red-dust-still.png`, rendered by
`tools/art/act_stills.py act-3` from Level 15's production backdrop as Act 2's was from Level 08's:
Mars from the entry corridor, Olympus Mons' caldera and flank far below through thin cirrus, the
cloud deck, the Tharsis plain beyond, a dust-storm front on the northern horizon; its chrome
lettering `ui/act-3-red-dust-title.png` from `tools/art/act_titles.py`). Text:

<!-- data: act-title-card -->
> ACT III
> RED DUST
> Mars · June 2185
<!-- /data -->

**Act intro** (four pages, each voiced and with its own image in `assets/ui/briefing/`, as Act 2's).
Texts (approved in round 36); three by Commander Okafor, one by Dr. Varga, as Act 1's outro mixed them.
The title card and the pages live in the act's [data.yaml](data.yaml) (`levels`, `title_card`,
`briefing`; M7 part B) and are rendered here:

| Page | Speaker | Image |
|---|---|---|
| 1 | Commander Okafor (grim) | `act-3-arrival`: Mars filling the transport's forward view, a CDF overlay with the colonies' beacons, most of them dark; Ares Landing's still lit |
| 2 | Dr. Varga | `act-3-mars-front`: a CDF globe display of Mars: the Hellas basin in the south glowing Vrell red, the spread north in tendrils, the colonies marked (Ares Landing on the edge of the northern plains, Tharsis Foundry on the plateau), the Valles Marineris between them |
| 3 | Commander Okafor | `act-3-colonists`: a militia gun pit on a dome's ring at dusk under a butterscotch sky, a colonist gunner looking up, patched glass panels behind |
| 4 | Commander Okafor (fierce) | `act-3-drop`: the transport's drop bay open over Mars, Aegis Wing's Stormhawks and Rook's Ember craft on their launch rails, the cloud deck far below |

<!-- data: act-briefing -->
> **Commander Okafor:** "Nine days at full burn. Ares Landing has now held alone for five weeks:
> no fleet, no supply drops, only militia guns and the air its domes still make. Every colony on
> Mars is cut off from every other."
>
> **Dr. Varga:** "The Vrell came down in the Hellas basin, in the south, and spread north from
> there. They have changed for Mars: they swim through the sand and ride the dust storms. They
> are learning this planet faster than we did."
>
> **Commander Okafor:** "The colonists asked Earth for help long before the Gate opened, and got
> committees. They will not thank us for coming late. They don't have to. We fly for them
> anyway."
>
> **Commander Okafor:** "We go in from the west, past Olympus Mons, and take the canyons back
> one at a time. Drop in ninety minutes. Mission briefing follows. Aegis Actual out."
<!-- /data -->

**Act outro** (four pages after the L21 debrief, each voiced and with its own image; track 24 under
page 1). It carries the beat the L21 draft hands over: the Vrell leave Mars, and Varga tracks their
fleet to Europa. Level 21's closing radio already has the Choir's words, Varga's "*that was not
singing*" and Okafor's "*Log everything*", so the outro does not repeat them; page 3 only answers
the Choir's "*we are going*". Texts (approved in round 36); the pages live in the act's [data.yaml](data.yaml)
(`outro`, M7 part H, step PH2) and are rendered here:

<!-- data: act-outro-pages -->
| Page | Speaker | Image |
|---|---|---|
| 1 | Commander Okafor (grim) | `act-3-outro-hellas`: the Hellas basin at dawn, the Colossus's collapsed crater, the last spore trails climbing out of the frame |
| 2 | Commander Okafor | `act-3-outro-domes`: Ares Landing's domes at night with every pane lit, colonists on the ring road looking up |
| 3 | Dr. Varga (grim) | `act-3-outro-europa`: Varga's tracking plot: the Vrell fleet's courses off Mars bending toward Jupiter and converging on Europa; in a corner of her screen a minimised window with the walker's filed plate (the quiet thread, unexplained) |
| 4 | Commander Okafor (fierce) | `act-3-outro-aegis`: Aegis Wing on Ares Landing's spaceport apron, ground crews refitting the Stormhawks, Jupiter a bright point in the dusk sky |
<!-- /data -->

<!-- data: act-outro -->
> **Commander Okafor:** "The Colossus is dead. Within a day the Vrell lifted off from every site
> on Mars: the basin, the plains, the foundry. Ares Landing opened its shelters this morning,
> for the first time in six weeks."
>
> **Commander Okafor:** "The colonists have not forgiven Earth for coming late, and they are
> right not to. But last night every dome in Ares Landing burned its lights for Aegis Wing. Take
> that with you."
>
> **Dr. Varga:** "I tracked their fleet off Mars. It is not going home. Every course bends
> toward Jupiter and ends at one moon: Europa. It told us '*we are going*'. Now we know where."
>
> **Commander Okafor:** "Europa means Thera Deep and Conamara Station, and Conamara reports
> tremors under the ice. We refit, and we follow them. Aegis Wing leaves Mars in four days.
> Aegis Actual out."
<!-- /data -->

The outro sets up [Act 4](../act-4-deep-water/README.md) (Conamara Station's tremors, L22) without
pre-empting its intro. It keeps the Helix thread where Act 3 leaves it: Okafor says nothing about
it, the image only shows that Varga has not let it go.

### Settings

[Mars](../../world/mars/README.md), one sub-location per level (its *Sub-locations per level*
table): orbit, the cloud deck, Olympus Mons' slopes, Lycus Survey Station and the Tharsis dunes
(L15); the Valles Marineris from the Noctis edge to the Eos outflow (L16); Ares Landing (L17); the
northern plains in a dust storm (L18); the Tharsis Foundry (L19); the Hellas basin's north side,
from the floor to the rim (L20); the Hellas basin from the rim to the lime hollow (L21).

### New mechanics

Each is designed in the level that introduces it; the systems are M7's parts
(the [gap analysis](../../../m7-gaps.md)'s roadmap row).

| Level | Mechanic | Where it is designed |
|---|---|---|
| L15 | The **`buried` layer** (M7 = a): nothing hits it, not even the Smart Bomb; Burrowers hittable only while surfaced, after a bulge tell; the descent through two cloud decks (`high-air` ≤ 40 %, `low-air`) | [Level 15](level-15-olympus-descent/README.md), [Burrower](../../enemies/ground/burrower/README.md) |
| L16 | **Terrain collision**: canyon walls as polylines and islands in the level data (M2 = a), contact 10 (easy 6) with a push-back and a 0.5 s cooldown (M1 = a), the walls rising toward the camera with the collision at their foot (MA.1 = b), walls stop shots, the Ion Beam, missiles and enemy bullets (not the Orbital Lance's column: round 39 C1); a visual fork round a mesa (M3 = a); the fast scroll (230 px/s in the rapids); **tendril barriers** (`link`): the tendril hit only by a `beam` (M9 = a), the anchors by everything; the secondary kind `links` | [Level 16](level-16-valles-canyon-run/README.md), [Tendril Anchor](../../enemies/ground/tendril-anchor/README.md) |
| L17 | **Support units**: the Choir Herald's `aura` rings absorb about its escorts' HP (M8 = a, the EMP strips them); the **dome ally** (M16 = a) hurt only by hooked attackers, and the secondary kind `untouched` | [Level 17](level-17-dome-siege/README.md), [Choir Herald](../../enemies/air/choir-herald/README.md), [allies](../../allies/README.md) |
| L18 | **Visibility radius** in the dust storm (M6 = a): a look only, ground and `low-air` fade outside ~170 px through a soft ragged falloff (MA.2 = b), air as glowing silhouettes, bullets always visible, everything hittable; Dust Devils that pull the ship and the pickups | [Level 18](level-18-dust-storm/README.md), [Dust Devil](../../enemies/ground/dust-devil/README.md) |
| L19 | **Conveyors** that carry ground units, **furnace vents**, turrets that fire up the screen after the ship has passed (`arc: up`); a mid-boss with a wreck crawl after it (M15 = a) | [Level 19](level-19-foundry-of-tharsis/README.md), [Revenant Walker](../../enemies/bosses/revenant-walker/README.md) |
| L20 | **Reverse scroll** (M4 = a, M5 = a): signed scroll speeds, entries by travel direction, a TOP edge warning; the `pursuit` formation (the Mote Swarm pressing from the top) | [Level 20](level-20-retreat-from-hellas/README.md) |
| L21 | An act boss **breaching from any edge and under the ship**, with suction, on a slowly crawling arena | [Level 21](level-21-dust-colossus/README.md), [Dust Colossus](../../enemies/bosses/dust-colossus/README.md) |

**Equipment new in the act** (designed in [weapons](../../player/weapons/README.md),
[specials](../../player/specials/README.md), [systems](../../player/systems/README.md) and
[wingmen](../../player/wingmen/README.md)): the Ion Beam (front, `beam`), the Light Drone Bay and
the EMP Burst from the L15 visit; the third utility bay and the Evasive thrusters in the act's
shop; the Rear Lance and the Orbital Lance from L17; the Deflector Pod from L18; the Shield
Overcharge, the first cooldown special with its HUD ring, from L19; the Rear-guard Drone Bay from
L20. The trait pacing table makes `beam` available at L15 and first needed at L16, where every
tendril gate also falls to an anchor kill, so the beam stays optional.

### Factions present

Vrell, plus **unmarked** grey machines: the Ghost Drone (L18–L19), the Warden Tank (L19) and the
Revenant Walker (L19), grey with no trim ([enemies](../../enemies/README.md): unmarked Ascendancy
machines in Acts 3–4). Nothing identifies them as Ascendancy yet. Helix Dynamics appears only as
untouched scenery: the research station on the Valles rim (L16) and the compound at Ares Landing
(L17), drawn alike.

### Boss

Act boss [Dust Colossus](../../enemies/bosses/dust-colossus/README.md) (L21); mid-boss
[Revenant Walker](../../enemies/bosses/revenant-walker/README.md) (L19).

### Units in the act

New: Burrower and Dust Kite (L15; the Dust Kite is the act's popcorn in every level, M10 = b),
Tendril Anchor and Threadcrawler (L16), Choir Herald and Shellback (L17; the Ares Landing domes as
a new ally), Ghost Drone and Dust Devil (L18), Warden Tank and the Revenant Walker (L19), the Dust
Colossus (L21). Returning from Acts 1–2: Skitter, Needler, Stinger (every level), Spine Turret and
Polyp Mortar (every level's nests), Spore Bomber (L15, L17, L21), Ravager and Creeper (L15, L19),
Mantis (L16, L17, L21), Wraith (L18–L21), Mote Swarm (L17, L18, L20 as the pursuit swarm, L21).

Only the returning `medium` and larger units get the act HP factor from the
[balancing basis](../../enemies/README.md#balancing-basis) (D5 = c of M5 part A), against the
reference DPS 76 · 80 · 84 · 88 · 92 · 96 · 100 for L15–L21 (M18 = a, accepted in round 36; it has to
be in `enemies/data.yaml` before any Act 3 level data). The levels' drafts use these values:

| Level | Units with the act HP factor (HP at medium) |
|---|---|
| L15 | Spore Bomber 24 → 57, Ravager 16 → 19, Creeper 30 → 38 |
| L16 | Mantis 26 → 40, Burrower 44 → 46 |
| L17 | Spore Bomber → 63, Mantis → 42 |
| L18 | Wraith 16 → 21 |
| L19 | Creeper → 46, Ravager → 23, Wraith → 22, Choir Herald 60 → 66, Shellback body 120 → 131 and vent 24 → 26 |
| L20 | Burrower → 56, Dust Devil 52 → 57, Wraith → 23 |
| L21 | Burrower → 58, Dust Devil → 59, Shellback body → 143 and vent → 29, Choir Herald → 71, Mantis → 50, Wraith → 24, Spore Bomber → 75 |

The `tiny` and `small` units (Dust Kite, Skitter, Needler, Stinger, Mote Swarm, Ghost Drone,
Tendril Anchor, Spine Turret, Polyp Mortar) keep their HP. The level documents list the waves.
Since 2026-10-19 two exceptions: Level 18 joins the basis's `small_act_hp` (Needler 12, Stinger 18), and from
Level 18, where the plan fits the Ion Beam, every `small` unit's HP is doubled on top (**beam-era HP**, the user's
"tougher, not more"; Needler 24, Stinger 36, Ghost Drone 28, Spine Turret 16, Polyp Mortar 20 at L18; see the
[balancing basis](../../enemies/README.md#balancing-basis)). Levels 15–17 fly the Pulse Cannon and keep theirs
until their own rework.

### Density and threat profile

The act's minimum is **55 enemies a minute** at medium (`DensityTest`; a breather may dip 20 % to
44). The drafts were written in parallel and spread widely: L15 85, L16 90, L17 89, L18 50, L19 58,
L20 64, L21 66 a minute. Act 2 as built ran L08 58, L09 84, L10 98, L11 98, L12 70, L13 54, L14 68:
its 84–98 were not targets but the fix-ups of drafts at 27–62 a minute that left the screen empty
between holds, filled afterwards with Skitter streams so `PacingTest` passed.

**The band (round 36, Q.24a): 60–72 a minute for the act's levels, 48–55 for
the breather (L18).** The reasons:

- **The 3 s rule is met by cadence, not by wave size.** A wave every 3–3.5 s is about 18 waves a
  minute; at three or four small units each that is 55–75 a minute. Every Act 3 draft already
  keeps that cadence, so smaller popcorn waves lower the count without opening gaps (the autopilot
  clears a three-unit wave in 1–2 s, the next arrives before the screen has been empty for 3 s).
- **The act's mechanics carry the threat.** Walls narrowing to 180 px, the dust radius, the hooked
  waves on the domes and the reverse scroll are the new pressure; 85–90 a minute of popcorn on top
  crowds a 180 px canyon and buries the domes' hooked bombers in noise. The campaign's rule is many
  small, easy units *at the same momentary threat*.
- **The economy.** At 85–90 a minute most of the budget sits in popcorn, and Level 15's
  `bounty_scale` falls to 0.36, below Act 2's lowest (0.40): the act's new units pay little. At
  60–72 the first three levels land at about 0.40–0.60.
- **Margin.** 60 leaves 9 % over the minimum for the data step, which recounts (released spawns,
  the arena cut) and re-times the waves; L19's draft at 58 has almost none.
- **The sawtooth.** The roster's density ratings 3 · 4 · 4 · 3 · 4 · 4 · 4 read as an opener at
  ≈ 64, the 4s at 62–70, the breather at ≈ 50.

If `PacingTest` fails in a level's data step, the fix is one more small wave (cadence), and a
level that ends above 75 a minute records why in its Decisions, as Act 2's did.

| Level | Rating | Draft | Recommended | Change made to the level document | `bounty_scale` draft → final |
|---|---|---|---|---|---|
| [L15](level-15-olympus-descent/README.md) | 3 | 262 in 185 s, **85** | ≈ 197, **≈ 64** | Changed 2026-10-19 (round 40 P.e, the Act 2+ floor, step PE15): 306 in 185 s, **≈ 99**; 33 firing Needler/Stinger groups (12/18 HP by `small_act_hp`), Kite V-wings → looping swarms of 6; the Burrower fields, set pieces and radio unchanged (was 200, ≈ 65) | 0.36 → 0.42 → **0.26** |
| [L16](level-16-valles-canyon-run/README.md) | 4 | 279 in 185 s, **90** | ≈ 215, **≈ 70** | Changed 2026-10-19 (round 40 P.e, the Act 2+ floor, step PE16): 367 in 185 s, **≈ 119**; Needler circles and pairs, Stinger streams down the fork, Kite swarms of 9, Spine ledges and two Burrower fields (12/18 HP by `small_act_hp`); the walls, gates, crawlers, narrows pairs and radio unchanged (was 215, ≈ 70) | 0.45 → 0.54 → **0.27** |
| [L17](level-17-dome-siege/README.md) | 4 | 247 in 167 s, **89** | ≈ 195, **≈ 70** | Changed 2026-10-19 (round 40 P.e, the Act 2+ floor, step PE17): 213 in 167 s, **≈ 77**; six Burrower fields (kept off the domes), nine Stinger streams for Skitter/Kite waves (12/18 HP by `small_act_hp`); the domes, hooked bombers, Shellbacks, Heralds, Helix beat and radio unchanged (was 196, ≈ 70) | 0.55 → 0.60 → **0.40** |
| [L18](level-18-dust-storm/README.md) | 3 | 149 in 178 s, **50** | keep, **≈ 50** | Changed 2026-10-19 (the user: the Act 2+ floor, "tougher, not more"): 301 in 178 s, **≈ 101**, with the beam era's HP; the breather by threat, not by count | 0.92 → **0.37** |
| [L19](level-19-foundry-of-tharsis/README.md) | 4 | 159 in 166 s, **58** | ≈ 172, **≈ 62** | Changed 2026-10-19 (M7 part F, the Act 2+ floor and the beam era, steps F2/F4): 226 at medium (193 in waves + 33 ground), **≈ 107**; firing Needler/Stinger groups of 4, nests under the gantries, mortar pair in the tanks' quiet window; the walker 3,740 HP (par 53), bounty 495 (15.0 %) (was 172, ≈ 62; walker 262); pending round 42 | 0.53 → 0.52 → **0.28** |
| [L20](level-20-retreat-from-hellas/README.md) | 4 | 178 in 167 s, **64** | keep, **≈ 64** | Changed 2026-10-20 (M7 part G step G2, the Act 2+ floor and the beam era, count-neutral as the user's Q.4): 192 at medium (170 in waves incl. 53 motes + 22 ground), **≈ 69** on paper; eight popcorn threes as firing Needler/Stinger threes, three spine nests added, the lull's Dust Kite flocks at the edges. Step G4 (measured and tuned, two passes): **197** (175 + 22), **≈ 71** measured; three strike waves, two Needler pincers through the turnabout, the lull as six kite flocks of 3, the firing threes as pairs, three Burrower fields as pairs; pending round 43 | 0.95 → 0.77 (paper) → **0.83** (step G4, measured) |
| [L21](level-21-dust-colossus/README.md) | 4 | 107 in 97 s, **66** | keep, **≈ 66** | None | 0.80 (the boss's share is open in L21) |

The scales keep each level's typical haul on its budget (1,805 · 1,931 · 2,067 · 2,211 · 2,366 ·
2,532 · 2,709) with the popcorn's bounty removed at the air share (60 %); `TypicalHaulTest` fits the
final values in each part. The act's scales then run about 0.40–0.95, wider than Act 2's
0.40–0.76: L18 and L20 are cheap mixes against high budgets. Accepted in round 36 (M12; a scale is a
per-level knob; it only changes what one kill pays) rather than adding pricier units, which would
need about four times the Mantis and Spore Bombers L20's draft offers to reach 0.75.

**Threat profile across the act** (the levels' hand tables):

| Level | Directions (by enemy) | Traits | Hazards | Boss |
|---|---|---|---|---|
| L15 | front 96 % · sides 4 % | `anti-ground`, `forward` | none | none |
| L16 | front 100 % | `forward`, `piercing` | canyon walls, tendril barriers | none |
| L17 | front 95 % · sides 5 % | `spread`, `anti-ground` | attacks on the domes, Shellback mortars | none |
| L18 | front 88 % · sides 6 % · rear 6 % | `homing`, `spread` | the dust storm's radius, Dust Devils' pull | none |
| L19 | front 90 % · rear 10 % | `anti-ground`, `rear` | furnace vents, conveyors, turrets firing after the ship passes | mid-boss Revenant Walker |
| L20 | ahead 57 % · behind 42 % · sides 1 % (by travel; the Stingers enter at the top since 2026-10-21) | `rear`, `homing` | reverse scroll, the pursuing swarm | none |
| L21 | front 75 % · sides 12 % · rear 12 % | `anti-ground`, `rear` | suction and debris in the arena | act boss Dust Colossus |

Every level keeps at least 60 % of its waves from the front (L20 counts by travel), and every side
or rear entry has its edge warning 3 s ahead (L20's TOP warning is new).

### Music

Per level, from the [track list](../../audio/music/README.md#track-list): track 8, the Act 3 A theme
"Red Dust Run" (chosen in round 02; its full length and stems are part B's), plays in L15, L16, L17
and L20; track 9, the Act 3 B theme ("Grey Signal", D minor dub techno, round 36's S.1 = b; built in part E), plays
in L18, L19 and the L21 approach. **Vorne's motif** is a faint layer of track 9 only where the grey
machines are: in L18 from the Ghost Drones' first sight (t=69.5, off in the eye, back in the back
wall) and in L19 under the walker fight and alone over the wreck crawl (hint 2 in the music),
heard as an intercepted radio whistle. It is
**muted in L21** (the Colossus is pure Vrell) and returns in Act 4 (L27–L28). L19's walker gets the
mini-boss sting; the Dust Colossus gets the boss warning (track 22) and the Vrell boss theme "The
Choir Descends" (track 18); L21's settling dust has no music, and the act-complete fanfare (track
24) plays under the first outro page. L20 uses track 8 (its breakbeat suits the run; the roadmap's
part G row names the B theme, which moves to part E, Level 18, where it is first needed). The cues
per section are in each level's *Music & ambience*; the ambiences (`mars-dust`, `mars-colony`,
`mars-storm`, `mars-foundry`, the basin rumble) and the act's new sounds are listed by the
[sfx](../../audio/sfx/README.md) README.

### Voice

Every briefing page and radio line is voiced, as in Acts 1–2 ([voice](../../audio/voice/README.md));
the hangar teaser stays text only. Rook flies all seven levels from the escort slot (M17 = a), his
lines with `requires: escort`, one dry joke a level, none in L20, where he has the act's one quiet
beat instead. The Choir sings in L15, L16 and L21 before the boss, and **speaks** once, as the
Colossus dies (its first words; the voice needs the Choir's layering on speech, not its sung stage
sound).

**One colony voice: Ares Control** (round 36, Q.17d; cast S.6 = b, reader Thomas A; his radio
portrait the dust-suit controller on the teal channel, C.1 = b). The drafts have two Mars
speakers with the same arc: *Ares Control* (L15, L16, L19, L20, L21), Ares Landing's flight
controller, and *Ares Warden* (L17, one line in L21), a militia officer who speaks for the colony;
both proud, dry, resentful that Earth came late. They are **one character**, Ares Control: the
colonist who runs Ares Landing's flight control and, in the siege, speaks for the colony. One
voice makes the thaw land (the L15 jab, L17's "*Earth can stay for dinner*", L21's "*Dinner's on
Ares Landing*") and costs one audition instead of two. A man's voice (L21's draft says "his"),
older than Rook's, contrasting with Okafor's and Varga's. Done in the level documents: L17's nine
Ares Warden lines and L21's one went to Ares Control, and L17's *New speaker* paragraph points to
Level 15's.

| Level | Speakers | New speaker |
|---|---|---|
| L15 | Okafor, Varga, Rook, Ares Control, the Choir (sings) | Ares Control (cast in part B's round) |
| L16 | Okafor, Varga, Rook, Ares Control, the Choir (sings) | — |
| L17 | Okafor, Varga, Rook, Ares Control (the draft's Ares Warden) | — |
| L18 | Okafor, Varga, Rook | — |
| L19 | Okafor, Varga, Rook, Ares Control | — |
| L20 | Okafor, Varga, Rook (his quiet beat), Ares Control | — |
| L21 | Okafor, Varga, Rook, Ares Control, the Choir (sings, then speaks) | the Choir's speech (a new treatment, not a new cast) |

Okafor is a woman (she / her) in every line and note. **Pronunciation** for the voice step (the
voice tests already read "Ares" as "air is" without a prompt): Ares, Schiaparelli, Olympus Mons,
Valles Marineris, Tharsis, Hellas, Lycus, Coprates, Candor, Noctis, Eos; seeds or prompts are
pinned per line, as in Act 2. The act intro's four pages and the outro's four are voiced with the
act's data (parts B and H).

**Okafor's cut-off line** "*Not on this channel, Doctor.*" (M13 = a) is in three drafts: L17
(t=88.5), L18 (t=78) and L19 (t=148.5, the walker spec's). Decided (round 36, Q.24c): a refrain bookending the
Helix beat in L17 and L19, with L18 varied so it does not wear thin: "*Later, Doctor. Eyes on the
storm.*" And Varga's approved Act 3 line from her [character sheet](../../story/characters/varga/README.md),
"*Commander, they have not touched a single Helix facility. Not one.*", is in no draft: it
replaces her L17 line at t=80.5 ("*Helix compound: not a scratch…*"), after she has seen two
Helix sites.

## Concept art

The title card's still and the act intro's images are production art straight away (M7 part B, step
ART-B, as Act 2's); the intro's four images are on the
[briefing review sheet](../../ui/briefing/README.md#concept-art). Prompts in
[concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/act-3-still-final-r38-a.png](concept/act-3-still-final-r38-a.png) | Review sheet, M7 part B (round 38): the title card's 960×540 still (Mars from the entry corridor: Olympus Mons far below, the cloud deck's banks, the cirrus, the Tharsis plain to the horizon, a dust-storm front on the northern horizon, from Level 15's production backdrop) and the title card over it | chosen (round 38, F.7: approved as final) |
| [concept/act-3-intro-capture-r38-a.png](concept/act-3-intro-capture-r38-a.png) | Game capture, M7 part B (round 38): a version 3 save at the Level 15 hangar (the M6 release's) continued from the main menu: the title card, the four voiced intro pages, Level 15's two briefing pages, then the hangar (L.6) | chosen (round 38, K.2: accepted) |
| [concept/act-3-intro-capture-r38-a.mp4](concept/act-3-intro-capture-r38-a.mp4) | The same run as a short video with the game's sound: Continue, the title card and page 1; the end of the briefing into the hangar (42 s) | chosen (round 38, K.2: accepted) |

## Implementation

- [ ] All 7 levels promoted to level documents (done 2026-10-12, approved in round 36) and
      implemented (M7 parts B–H).
- [x] Act data file: `levels: [15, 21]`, the title card, the four intro pages and the four outro
      pages with their images and expressions, rendered into *Act intro and outro* as marked
      tables (part B for the intro: done in step B2; part H for the outro: done in step PH2, with `next_act` for the release's end).
- [x] The title card's still (`tools/art/act_stills.py act-3` from Level 15's backdrop) and lettering;
      the eight briefing images (`tools/art/briefing_images.py`). Part B: the still, the lettering and the
      intro's four images (step ART-B, round 38, accepted in round 43 (2026-10-20)); the outro's four in part H
      (step ART-H3, `tools/art/outro_l21_images.py`, in `assets/ui/briefing/`, approved in round 44 F.a–F.f); the
      lettering from `tools/art/act_titles.py` (step ART-K).
- [x] The act's systems: the `buried` layer (L15), terrain collision and the canyon walls (L16,
      rules in the [art-direction](../../art-direction/README.md) layer model), `link` and `aura`
      (L16–L17), the visibility radius (L18), conveyors, vents and up-facing turrets (L19), reverse
      scroll (L20), the breaching boss (L21).
- [x] `reference_dps` 15–21 and the Act 3 balance plan (part A) before any Act 3 level data (M7
      part A: `enemies/data.yaml`, the plan's rows 15–21, `ActHpFactorTest`, `BalanceTest`).
- [x] Every level within the act's density band (`DensityTest`, `PacingTest`) and its typical haul
      within ±5 % of its budget (`TypicalHaulTest`; through Level 21, M7 part I).
- [x] The act-end order after L21 (L14's) and the release's end moved from L14 to L21; the hangar
      before L22 reads `ACT IV - DEEP WATER: COMING SOON`.
- [x] A save at the L15 hangar from the M6 release, written before Act 3 existed, plays the Act 3
      intro **once** on Continue (user decision L.6 of round 36): the title card, the four intro
      pages and the Level 15 briefing, then the hangar; a flag in the save (intro seen) keeps it
      from playing again (part B: steps B2 and B4, the `act-intro:N` flags, `CampaignRouteTest`,
      `SaveFormatTest`; captured in round 38, `act-3-intro-capture-r38-a`, accepted in round 43 (2026-10-20)).
- [x] Ares Control voiced (cast in round 36, S.6 = b; his lines rendered in part B, his mid-line
      pauses checked); the Choir's speech treatment (part H). Part B's half done in steps AUD-B and VOX-B
      (the listen-through of every voiced Act 3 line: all fine, round 45 V.1, 2026-10-21).
- [x] `ActPlaythroughTest` and `BalanceTest` through Act 3 with Rook (M7 part I, step I1, 2026-10-20: Levels 01–21 on every difficulty).
- [ ] The user's playthrough of Act 3 (the same one as Act 2's, round 34's P9 and M7's: part I's save at the Level 15 hangar, medium; round 44 X.m, I.3), then the `v0.1` release (Acts 1-3, I.2).

## Open questions

- None open: round 36 answered every question (see *Decisions*).

## Decisions

- 2026-09-30: The twist hints are the Ghost Drone (L18) and the Revenant Walker (L19).
- 2026-09-30: Enemy variety pass: new units added to the level rows so the act passes the [variety checklist](../../enemies/README.md#variety-checklist-per-act).
- 2026-10-01: L17 introduces the Shellback, an animal-like Vrell armoured beast that threatens the domes (round 05 follow-up).
- 2026-10-12 (overnight design phase): the seven roster rows promoted to draft level documents
  (the Contents table replaces the roster); the canyon-wall question moved to Level 16 and answered
  there (M1 = a, pending). This README rewritten for the rules Acts 1–2 established, all accepted in round 36 (2026-10-13): the act intro (title card ACT III · RED DUST · Mars · June
  2185, four pages continuing Act 2's outro) and the act outro (four pages, the Vrell leave Mars
  and Varga tracks their fleet to Europa) as draft texts; the timeline of nine days' crossing and
  five weeks of siege; the units in the act with the act HP factor from the proposed reference DPS
  76–100 (M18 = a); the new-mechanics table; a density band of 60–72 a minute (the breather 48–55)
  instead of the drafts' 50–90, with the pending changes to Levels 15, 16, 17 and 19 (smaller
  popcorn waves at the same cadence) and the scales of L18 and L20 accepted; music per level (track
  8 in L15–17 and L20, track 9 in L18, L19 and L21's approach; Vorne's motif in L18–L19 only, muted
  in L21); the two Mars speakers merged into one, Ares Control; Okafor's cut-off line as a refrain
  in L17 and L19 with L18 varied, and Varga's approved Act 3 line placed in L17.
- 2026-10-12 (overnight design phase, harmonised): Levels 15, 16, 17 and 19 brought into the band
  (L15 200 ≈ 65 a minute, scale 0.42; L16 215 ≈ 70, 0.54; L17 196 ≈ 70, 0.60; L19 172 ≈ 62,
  0.52); their threat profiles (L15 front 96 % · sides 4 %, L17 95 · 5, L19 front 90 · rear 10),
  "five weeks" in Level 15, Ares Warden's lines moved to Ares Control. Accepted in round 36 (2026-10-13).
- 2026-10-13 (user, round 36): the act approved (D.1) with its intro and outro texts as drafted.
  M1–M19 kept or accepted. Q.24 all yes: the density band 60–72 a minute (breather 48–55), the
  timeline (nine days' crossing, five weeks of siege, June 2185), Okafor's refrain in L17 and L19
  varied in L18 with Varga's approved line in L17, Vorne's motif as a third lockstep stream, track
  9's key and tempo after the picked sketch (S.1 = b "Grey Signal", 125 BPM D minor), the Mars
  names respelled for the voice, "Lance Control", the geography. Ares Control cast (S.6 = b) with the
  dust-suit portrait (C.1 = b); the canyon walls rise toward the camera (MA.1 = b, Level 16 adapted).
  **L.6**: a release save at the L15 hangar plays the Act 3 intro once on Continue (Implementation).
  The levels stay `implementation: not-started` until M7 builds them (parts A–I on the
  [roadmap](../../tech/roadmap/README.md)).
- 2026-10-14 (M7 part B, step ART-B): the title card's still and the intro's four images made straight to production (round 38), as Act 2's; the still sees Level 15's deep layer in perspective (Olympus far below, the cloud deck, the plain beyond, the storm front on the horizon). Accepted in round 43 (2026-10-20): the flat cloud banks, the faint `mars-high` tiling near the bottom, RED DUST's lettering taller than HOMEFRONT's.
- 2026-10-14 (user, round 38): closed: the title card `act-3-still-final-r38-a` approved as final (F.7); the
  intro capture accepted (K.2), the intro pages under Level 15's header and objectives kept as built (R.3); the
  four voiced intro pages accepted (V.4); B.2 (e), the `act-intro:N` story flag with the version 3 save's
  migration, and B.3 (f), Continue going through the intro check from the main menu and the save slots, kept.
  `art` moves to `chosen`: the act's art so far is final, its outro and later levels' art are still to come.
- 2026-10-18 (user, round 39): Level 16's round closed: C1 (the Orbital Lance is not stopped by rock, the specials
  spec wins), C2 (a fork wave flies the widest channel) and C3 (flyer shadows on the walls) accepted; R.4: the
  level's hint changed, not the economy: no beam promised at Level 16, its traits `forward`, `piercing` (the
  balance plan still buys the Ion Beam at Level 18). The walls' look is being redone (R.1, for round 40).
- 2026-10-20 (step PH2, M7 part H): the outro's four pages in the act's data (rendered above, the approved text
  unchanged) and `next_act: {act: ACT IV, name: DEEP WATER, setting: EUROPA}` for the release's end (H.7d: "ACTS I-III
  COMPLETE", "ACT IV - DEEP WATER. EUROPA. COMING SOON."; the hangar before L22 "ACT IV - DEEP WATER: COMING SOON").
- 2026-10-20 (user, round 44, A.b (a): production assets (a `Source` chunk in `assets/`, every Concept art row decided)): **`art: final`** for this part. A later addition (a new sprite, a new concept round) is its own row and does not reopen the rest.
- 2026-10-20 (user, round 44, closed): Level 21 and the act end built and reviewed (rows F, V, K, B, Q; two captures K.b and K.c asked for changes, see the [Level 21 decisions](level-21-dust-colossus/README.md#decisions)); the art audit (A.a, A.b: `art: final` for this README and the part READMEs whose assets are production); the stale "pending the user's review" notes of the Act 3 tree and units rewritten as "accepted in round N (date)" (I.7); an Act 3 level is `implementation: done` when its round has closed and its checklist is ticked (I.8; Level 15 is `done`); the playthrough stays an act-level item (one for M5 and M7, I.3). The release `v0.1` is Acts 1-3 (I.2); the Act 4 design phase follows part I and the release (J.a).
- 2026-10-21 (user, round 45): the Act 3 voices accepted after the listen-through (V.1); hard's Act 3 economy accepted for v0.1, revisited after the user's playthrough (C.1).
