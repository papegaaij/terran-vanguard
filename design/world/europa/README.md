---
title: Europa
design: approved
implementation: n/a
art: chosen
updated: 2026-10-22
---

# Europa

## Summary

Jupiter's ice moon: a cracked white surface, and beneath it a lightless ocean where the city of
Thera Deep farms the sea. The Stormhawk goes under water. Setting for Act 4, ending with the
Abyssal Maw — and Ascendancy ships opening fire.

## Design

### Look & feel

Cold, then dark, then alien. The surface is a white-blue plain of ridges and cracks with
Jupiter filling the sky. Under the ice: black water lit only by headlights, city lights and
bioluminescence; kelp-farm grids, pressure domes, hydrothermal vents. The Vrell are farming
something down here. Touchstones: *The Abyss* (1989), *Sphere*, Sub Culture (1997).

### Sub-locations

| Sub-location | Description |
|---|---|
| Ice surface | Ridges, cracks, drilling rigs, ice-breaker vehicles, Jupiter overhead. |
| Conamara Station | Research town on the Conamara Chaos ice field, above Thera Deep; the first to report the Vrell drilling. |
| The descent | Flying down a drilled shaft through the ice (transition level). |
| Thera Deep | The under-ice city: pressure domes, tube corridors, kelp farms. |
| Vent fields | Hydrothermal vents, black smokers, Vrell growth farms. |
| The hatchery | The Vrell nursery round the deepest vent: egg-rack galleries, a spawning cavern, the antechamber where the Abyssal Maw is coiled (Level 28). |
| The trench | Deep, pitch-black trench; human-built beacons. (The Abyssal Maw waits in the Vrell hatchery round the deepest vent, Level 28.) |

### Sub-locations per level

Act 4's seven levels in order ([the act](../../campaign/act-4-deep-water/README.md)); the level documents own the sections and the timings.

| Level | Sub-location | What the level shows of it |
|---|---|---|
| [22 Ice Shelf](../../campaign/act-4-deep-water/level-22-ice-shelf/README.md) | Ice surface, Conamara Station | The shelf's ridges and cracks (ice geysers), four Vrell drill rigs, Jupiter in the deep layer, the station's dome at the run-in |
| [23 Through the Ice](../../campaign/act-4-deep-water/level-23-through-the-ice/README.md) | The descent, Thera Deep | A dry shaft mouth, the waterline (the hydro-kit switches the medium), a wet shaft, the lamp-lit kelp farms |
| [24 Abyssal Plain](../../campaign/act-4-deep-water/level-24-abyssal-plain/README.md) | Ocean floor | Thera Deep's last lamps, Vrell spawning reefs, the first line of red-gold relay beacons |
| [25 Siren Trench](../../campaign/act-4-deep-water/level-25-siren-trench/README.md) | The trench | Narrow walls with side caves and singing nests, a rock pillar, beacons on the walls |
| [26 Blackwater](../../campaign/act-4-deep-water/level-26-blackwater/README.md) | Deep ocean | The lights go out; headlight and glow only; the fault scarp with Sirens; Rook's loss |
| [27 Hydrothermal Vents](../../campaign/act-4-deep-water/level-27-hydrothermal-vents/README.md) | Vent fields | Black smokers, heat plumes, the beacon ridge, a scorched Depth Hunter wreck with its hull code |
| [28 Abyssal Maw](../../campaign/act-4-deep-water/level-28-abyssal-maw/README.md) | The Vrell hatchery, the descent shaft, the ice surface | Egg-rack galleries, the spawning cavern, the antechamber round the deepest vent; then the climb and the ice breakout |

### Parallax layers

| Layer | Contents |
|---|---|
| deep | Ice surface far below / ocean floor with vents and ridges. |
| ground | Domes, rigs, farms, turrets, Vrell growths on the sea floor. |
| sub | (Under water) the deep water *below* the play plane: drifting particles, schools of fish, light shafts, and the submerged enemies that rise from below (an Abyss Ray, the Maw). Not the play plane: see [the play plane](#the-play-plane-under-water). |
| low-air / air | Surface: normal. Under water the player's plane is still the `air` layer, now in water; every swimmer that fights the player is an `air` unit. |
| high-air | Surface: ice crystals and vapour plumes. Under water: the underside of the ice sheet with light through cracks (the ice ceiling). |

### Palette & lighting

- Surface: white-blue ice, orange-brown Jupiter looming, hard sunlight from top-left.
- Under water: near-black blue; light comes from the player's headlights (a soft cone ahead),
  city lights (warm) and bioluminescence (teal/violet — so Vrell bullets must be brighter and
  larger here to stay readable).
- Human beacons pulse red-gold — the visual hint of the Ascendancy.

### Hazards & set pieces

- **Under water rules**: see [below](#under-water-rules). This is the setting where `anti-sub` weapons
  matter.
- **Currents**: zones that push the player and bullets sideways, shown by drifting particles.
- **Darkness** (L26): see [Darkness](#darkness-level-26) below.
- **Ice ceiling**: breaking through the ice from below at the end of a level. (Signature set
  piece: the descent and the breakout.)
- **Vent eruptions**: vents burst periodically, damaging anything above them.

### Under water rules

*Draft, decided in round 48 (2026-10-22) (Q1 = b, the recommended softened rules; the
as-drafted values of 2026-09-30 are noted in the last section). Step `m8-dsystems`.*

This is the single definition of underwater play (levels 23–28). Other documents link here
instead of repeating the numbers. Values are first-draft balancing values; the measuring is part A
of M8 (reference DPS and a hand plan in [generator](../../player/generator/README.md#act-4-hand-plan-draft)).

#### The play plane under water

In the game's layer model the player's plane stays `air` under water; a level (or a section of
one) carries `medium: underwater` and the rules below apply while it does. The older wording "the
`sub` layer is the play plane" is withdrawn.

| Layer | Under water |
|---|---|
| `air` | The play plane: the ship, Rook, every swimming enemy and every bullet. Hit by all weapons (with the damage rule below). |
| `low-air` | The same layer as above water, drawn smaller and lower (fry, floating debris); hit by all weapons. |
| `ground` | The sea floor: reefs, farms, domes, turrets, growths. Hit by all weapons; `anti-ground` still does ×2, hardened targets still take only `anti-ground`, and the Smart Bomb. The water damage rule applies on top, after the ×2. |
| `sub` | The deep water *below* the plane: a unit rising from the abyss (the Abyss Ray's shadow, the Maw's dive). It is reached exactly as above water: only `anti-sub` weapons and the Smart Bomb, and the Sonar Pulse for 6 s. When it rises it becomes an `air` unit. |
| `high-air` | The ice ceiling: `homing` and `beam` weapons only, as always. |

A section of a level may switch medium (the waterline of the descent in L23, the breakout of
L28): the factors below ease in or out over 1 s as the ship crosses it, and the section's own
render grade, sound and special limits switch with it.

#### The rules

| Rule | Value (draft, Q1 b) |
|---|---|
| Hydro-kit | Every Stormhawk (and Rook's variant-C Stormhawk) gets a free, automatic field refit before L23, no slot, no power; it is the look of the ship under water, the factors come from the level's medium. Kept automatic (the act's open question), see [ship systems](../../player/systems/README.md#hydro-kit-automatic). |
| Projectile speed | All bullets, the player's and the enemies', move at × 0.7 (the enemy's difficulty scale is multiplied in; a shot stays on the screen longer, so fire rates are tuned per level). |
| Handling | **Drag:** acceleration × 0.6 (more inertia), top speed unchanged. The ship's `precision` (focus) mode still applies on top. |
| Shield | Regeneration unchanged (the as-drafted −25 % is dropped). |
| Beams and lasers | Reach × 0.75 of the play field's height (the Ion Beam, Lance Laser, Rear Lance; a pierce count is unchanged). |
| Torpedo-type weapons | Range +50 % (the Torpedo Pod 750 px, the [Harpoon Torpedoes](../../player/weapons/harpoon-torpedoes/README.md), the [Depth Charges](../../player/weapons/depth-charges/README.md)); they hit every target in the water (see the plane table) at full damage. |
| Damage | A weapon without the `anti-sub` trait does **× 0.7** of its damage under water (the as-drafted value was × 0.5); `anti-sub` weapons do full damage. It applies to the ship's weapons and to Rook's guns, after `anti-ground`'s ×2; it does not apply to specials (their damage is as in their tables) or to the damage the enemy deals. |
| Specials | No Airstrike or Orbital Lance under the ice; the Sonar Pulse works only in water and, under water, lights the field and gives every weapon full damage for 6 s (see [specials](../../player/specials/README.md#sonar-pulse-l22) and the *special limits* there). |
| Pressure hull | Optional utility module that **removes the drag** (acceleration back to normal; the speed and shield penalties of the draft are gone); see [ship systems](../../player/systems/README.md#pressure-hull-l22). |
| Rook | Flies with the same hydro-kit: the same drag (his formation slot lags a little more), his guns × 0.7 (none of his guns is `anti-sub`); the [Bulwark](../../player/wingmen/README.md#heavy-drone-bulwark-escort-alternative-l22)'s cannon is `anti-sub`. |

The numbers the game reads (M8 part A) are in [data.yaml](data.yaml), with the weapons each reach and
range rule names:

<!-- data: underwater -->
| Rule | Number |
|---|---|
| Projectile speed | × 0.7: every bullet, the player's and the enemies', at its spawn |
| Handling (drag) | acceleration and braking × 0.6, top speed unchanged (the pressure hull removes it) |
| Damage | × 0.7 for a weapon without the `anti-sub` trait (the ship's and Rook's guns), after `anti-ground`'s ×2 |
| Beam and laser reach | × 0.75 of the play field's height: [Ion Beam](../../player/weapons/ion-beam/README.md), [Lance Laser](../../player/weapons/lance-laser/README.md), [Rear Lance](../../player/weapons/rear-lance/README.md) |
| Torpedo-type range | × 1.5: [Torpedo Pod](../../player/weapons/torpedo-pod/README.md), [Harpoon Torpedoes](../../player/weapons/harpoon-torpedoes/README.md), [Depth Charges](../../player/weapons/depth-charges/README.md) |
| Waterline ease | 1 s, for new projectiles |
| Hydro-kit refit | at the hangar visit before L23 (a one-off notice) |

Generated from [data.yaml](data.yaml) by `tools/sync_tables.py`.
<!-- /data -->

Which layers can be hit above water (the `sub` layer seen from the surface in Act 2) is part of
the layer rules in [enemies](../../enemies/README.md#layer-rules).

#### Darkness (level 26)

*Draft, decided in round 48 (2026-10-22) (Q4 = a, a look only).* L26 is the black level:
outside the ship's headlight cone and the light pools (lamps, flares, a unit's own glow) the
field is nearly black (ambient about 6 %). The rules are about what the player *sees*, not about
what a shot can do:

- **Units** outside the light show only their glow: the teal and violet bioluminescent parts and
  any lamp; the body is a faint rim (about 15 % brightness) so a placement is never a surprise.
  Inside the cone (70° ahead, 260 px) the whole unit is drawn.
- **Bullets** are always bright (a bullet is never hidden), and the readability rules for bullets
  (larger, brighter Vrell bullets under water) hold. Warnings 3 s before an edge entry are
  unchanged, the Sensor suite's off-screen arrows too.
- **Everything is hittable**: no weapon needs the target to be lit (the light rule of L06's dark
  triggers is not extended); homing shots and Rook pick targets as usual.
- **Pickups** glow warm gold from afar; the Glow Angler's lure glows Vrell teal and has a visible
  stalk when it comes close (see [naval enemies](../../enemies/naval/README.md)).
- **Rook** carries a small lamp (a light pool of about 56 px) so the player always sees him.
- **Sonar Pulse**: every unit and lure on the screen is drawn whole for 6 s (and every weapon does full
  damage, see [specials](../../player/specials/README.md#sonar-pulse-l22)); L26 is the level where it pays.
- Option (planned for the settings): *Dark levels assist* raises the ambient to 20 %.
- Alternatives (not recommended): (b) shots hit only lit units, the L06 rule extended: it punishes
  non-homing loadouts in the act's breather; (c) a sonar-blip display only: a new display mode,
  hard to read.

#### Sonar slow (the Siren, level 25)

*Draft, decided in round 48 (2026-10-22) (Q5 = a).* The Siren's ring is a **non-damaging
wave**: an expanding ring (120 px/s to a radius of 230 px, 10 px thick; the [Siren](../../enemies/naval/siren/README.md) page owns these numbers) that is visible
and can be dodged like a bullet ring; a Dash (the Evasive thrusters) passes through it safely on
its invulnerability frames.

- **Effect:** the ship that the ring touches (tested once per ring) is **slowed to 60 % speed
  and acceleration for 2 s**, with a ship tell (a teal sonar shimmer round the hull and a short
  falling tone), then has **3 s immunity** from the hit, so slows never chain. No damage.
- **Rook** is slowed the same way (his own 3 s immunity). The pressure hull and shield do nothing
  against it.
- The ring respects the trench walls of L25: it is stopped by wall polylines and enters the side
  caves through their mouths only.
- Alternatives (not recommended): (b) as first drafted, 2 s and no immunity, chains of slows with
  the drag feel unfair; (c) the ring jams weapons for 1 s, a stronger penalty without counterplay.

#### As first drafted (alternative a of Q1, for the user's review)

Acceleration × 0.6 and **top speed −15 %**; shield regeneration **−25 %**; beams and lasers range
**halved**; damage of weapons without `anti-sub` **× 0.5**; the pressure hull removing the
top-speed and shield penalties (the drag stays). Read literally it halves the typical Act 3
loadout's damage at L23 (the Ion Beam twice: damage and reach) and needs three new weapons, about
5 300 cr plus upgrades and their power, against a visit of about 3 000. Alternative (c): no
penalty and ×1.5 for `anti-sub`: makes `anti-sub` optional, against the campaign's pacing table.

### Natives

- Vrell: swimmers, jellyfish-like floating mines, sub-layer ambushers, sea-floor growth farms.
- Ascendancy (late act): human beacons, then the first Ascendancy ships.
- Coalition: Thera Deep defences, submarines.

### Music mood

Slow, deep and eerie. Submerged pads, sonar pings, heartbeat bass; pressure building to the
reveal, which gets a sharp, shocking sting.

### Chosen scene

The look of this setting is set by the chosen parallax scene in art direction: Europa under water, kelp forest (round 07) —
[sheet](../../art-direction/concept/scene-europa-r07-a.png), [scroll loop](../../art-direction/concept/scene-europa-r07-a.gif).
The concept files live in [art-direction](../../art-direction/README.md).

## Concept art

The chosen scene (round 07, in [art direction](../../art-direction/README.md#concept-art)) sets the
under-water look; see *Chosen scene* above.

Concept round 48 (Act 4 "Deep Water", design phase
2026-10-21; all pending the user's review) — one mood still per level (direction, not production
backdrops) on the Q2 a light ladder, a/b for the under-water grade (Q2), the Level 26 darkness look
and the hydro-kit, and the breakout set piece; generator `tools/concept/europa_r48.py`, prompts in
[concept/prompts.md](concept/prompts.md). Units on the stills are production sprites of returning
units and the chosen round-06 Nautilus, for scale; the Glow Angler, the bore site and the carriers
are stand-ins (their own sheets are in [naval](../../enemies/naval/README.md#concept-art),
[ground](../../enemies/ground/README.md) and [bosses](../../enemies/bosses/README.md#concept-art)).

| File | What | Status |
|---|---|---|
| [concept/europa-ice-shelf-r48-a.png](concept/europa-ice-shelf-r48-a.png) | Ice shelf (L22): ridged white-blue ice in hard sunlight, an open lead mirroring Jupiter, a Vrell bore site with steaming cracks and turrets, an ice geyser; still and layer breakdown | chosen — round 48 |
| [concept/europa-descent-r48-a.png](concept/europa-descent-r48-a.png) | The descent (L23 sections 1–2): the drilled shaft receding below, the waterline crossing — dry ice behind the ship, the water grade ahead | chosen — round 48 |
| [concept/europa-thera-deep-r48-a.png](concept/europa-thera-deep-r48-a.png) | Thera Deep (L23 section 3, Q2 a): the chosen r07 farms lit only by grow-lamp pools, two Vrell siphon pumps | chosen — round 48 |
| [concept/europa-abyssal-plain-r48-a.png](concept/europa-abyssal-plain-r48-a.png) | Abyssal plain (L24): the lamps' edge (warm behind, dark ahead), spawning reefs, a relay beacon, an Abyss Ray's shadow below the plane | chosen — round 48 |
| [concept/europa-siren-trench-r48-a.png](concept/europa-siren-trench-r48-a.png) | Siren trench (L25): walls with nests and a cave notch, red-gold beacons, a sonar ring, about 14 % ambient | chosen — round 48 |
| [concept/europa-vents-r48-a.png](concept/europa-vents-r48-a.png) | Hydrothermal vents (L27): black smokers, amber mats, a heat plume erupting, the scorched grey Hunter wreck | chosen — round 48 |
| [concept/europa-hatchery-r48-a.png](concept/europa-hatchery-r48-a.png) | The hatchery (L28): brood galleries of egg racks, the vent's red-amber glow ahead, spores | chosen — round 48 |
| [concept/europa-breakout-r48-a.png](concept/europa-breakout-r48-a.png) | The breakout (L28 sections 6–8): the climb under a cracking ice ceiling, the burst with tumbling slabs, the night ice under Jupiter with Vrell carriers rising (three frames at 1×) | chosen — round 48 |
| [concept/underwater-grade-r48-a.png](concept/underwater-grade-r48-a.png) | Under-water grade A, lamp-lit to black (Q2 a, recommended): L24 at 1× and the L23–L28 ladder (sheet) | chosen — round 48 |
| [concept/underwater-grade-r48-a.gif](concept/underwater-grade-r48-a.gif) | Under-water grade A: L24 in motion | chosen — round 48 |
| [concept/rejected/underwater-grade-r48-b.png](concept/rejected/underwater-grade-r48-b.png) | Under-water grade B, dark everywhere from L23 (Q2 b): the same frame and ladder (sheet) | rejected — round 48: a picked |
| [concept/rejected/underwater-grade-r48-b.gif](concept/rejected/underwater-grade-r48-b.gif) | Under-water grade B: L24 in motion | rejected — round 48: a picked |
| [concept/darkness-r48-a.png](concept/darkness-r48-a.png) | Darkness A (L26): the spec's hard 70°/260 px cone, glow-only units with a 15 % rim, the lure beside a gold pickup, Rook's lamp, the Sonar Pulse on (sheet) | chosen — round 48 |
| [concept/darkness-r48-a.gif](concept/darkness-r48-a.gif) | Darkness A in motion | chosen — round 48 |
| [concept/rejected/darkness-r48-b.png](concept/rejected/darkness-r48-b.png) | Darkness B (L26): a soft lamp pool round the ship and a long faint beam with back-scatter, units fading in near the ship (sheet) | rejected — round 48: a picked |
| [concept/rejected/darkness-r48-b.gif](concept/rejected/darkness-r48-b.gif) | Darkness B in motion | rejected — round 48: a picked |
| [concept/hydro-kit-r48-a.png](concept/hydro-kit-r48-a.png) | Hydro-kit A, sealed refit: pump-jet shrouds, ballast saddles, twin headlights, nav lights; banking frames vs stock, in L24 and L26, the pump wake | chosen — round 48 |
| [concept/rejected/hydro-kit-r48-b.png](concept/rejected/hydro-kit-r48-b.png) | Hydro-kit B, pontoon kit: wing pontoons with lamps, a sonar blister, ducted engines; the same views (the pontoons sit where the weapon pods mount) | rejected — round 48: a picked |
| [concept/hydro-kit-final-r50-a.png](concept/hydro-kit-final-r50-a.png) | Hydro-kit final frames (`tools/art/act4_gear.py hydro`, round 48 E10 = a): the Stormhawk's five banking frames stock and with the sealed refit, the kit with the micro-missile and torpedo pods at their pivots, Rook's craft stock and with the same kit parts (sheet) | chosen (round 50, F.6: accepted as final) |
| [concept/hydro-kit-final-r50-a.gif](concept/hydro-kit-final-r50-a.gif) | The kitted Stormhawk and Rook banking under water (no grade, no wake: part C), the torpedo pods fitted halfway, 10 fps at 2x | chosen (round 50, F.6: accepted as final) |
| [concept/underwater-capture-r50-a.png](concept/underwater-capture-r50-a.png) | Game capture, Level 20 with `--medium underwater@10`: dry (left: the Ion Beam's full reach, the Torpedo Pods `NO WATER`) and under water (right: the beam at three quarters of the field, the pods firing, the Airstrike greyed `UNDER ICE`, the ship and Rook with the hydro-kit) | chosen (round 50, F.6: accepted) |
| [concept/underwater-capture-r50-a.mp4](concept/underwater-capture-r50-a.mp4) | Game capture with the game's sound: the same run across the waterline at 10 s (the special row's flash and limit chime, the factors easing in) | chosen (round 50, F.6: accepted) |

## Decisions

- 2026-09-30: Europa as Act 4 with under-water rules; signature: the descent and ice breakout.
- 2026-09-30: Conamara Station added as a surface sub-location (Act 4 opening).
- 2026-09-30: Underwater rules consolidated here as the single owner: free automatic hydro-kit, 50 % damage for non-`anti-sub` weapons, optional pressure hull removes the speed/shield penalties.
- 2026-09-30: Rook's craft renamed from the placeholder "Kestrel" to his variant-C Stormhawk (see [wingmen](../../player/wingmen/README.md)).
- 2026-10-01: Linked the chosen scene from art direction; `art: chosen`.
- 2026-10-21 (Act 4 design phase, step m8-dsystems; **all decided in round 48 (2026-10-22)**): the *Under water rules* are rewritten to Q1 = b (the softened rules: damage × 0.7, no top-speed or shield-regen penalty, beam reach × 0.75, the pressure hull removes the drag), the as-drafted values kept as alternative a; the play plane under water is `air` in water (`sub` = the deep below, `ground` = the sea floor, `high-air` = the ice ceiling), the parallax-layer table re-worded; Darkness (Q4 = a: a look only, the Sonar Pulse lights the field) and the Siren's sonar slow (Q5 = a: a visible ring, 60 % for 2 s, 3 s immunity) written as system text.
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-22 (M8 part A, step A2): the under-water rules' numbers in [data.yaml](data.yaml) (`underwater`, with the
  weapons the reach and range rules name and the hydro-kit's level 23), rendered into the *Under water rules* table
  (D2 of the part's gap check, accepted in round 50 (2026-10-22)).
- 2026-10-22 (M8 part A, steps A1–A6; small rules decided by the part's gap check and its steps, accepted in round 50 (2026-10-22)): part A is flown on built levels with the debug option `--medium underwater[@<s>]` and on simulation fixtures, no test level in the design tree (D1); the water's factors apply when a shot or a bullet spawns, so a shot keeps the factor it was fired with, and the drag works on braking too (D3); the 1 s ease at a waterline is a smoothstep and touches new projectiles only, while a beam's reach and the layers switch at once; lobbed shells (mortars) and spore mines are not slowed (not bullets); a mine's roll-out is ×0.7.
- 2026-10-22 (M8 part A, ART-A and step A6; accepted in round 50 (2026-10-22)): the hydro-kit's parts on Rook's craft placed by hand; the engine flames still show under water (the under-water look is part C's); over water (Levels 11–13) the harpoons' and the charges' cables, lamps and bursts are drawn in the `sub` pass, so they read dim under the sea's surface.
- 2026-10-22 (user, [round 50](../../concept-rounds/round-50/README.md) closed): F.6: the hydro-kit's final art and the under-water game capture accepted, the concept rows `chosen` (the part stays `art: chosen`: Europa's backdrops and the under-water grade are part C's on). B.4 accepted: lobbed shells and spore mines keep their speed under water.
