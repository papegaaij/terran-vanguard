---
title: Weapons
design: approved
implementation: done
art: final
depends-on: [../generator, ../../systems/economy]
updated: 2026-10-22
---

# Weapons

## Summary

Every weapon belongs to one slot type (front, rear or wing mount) and is described by the
shared **weapon traits** from the [design index](../../README.md#shared-vocabulary). Level
threat profiles recommend traits, and the hangar marks the weapons that have them. That is how
"choose the right loadout for the next level" works.

## Contents

The weapons available by L14 (Acts 1–2) have full designs, plus the Act 3 weapons (Ion Beam, Rear
Lance, Deflector Pod, Light Drone Bay; designed 2026-10-12, approved in concept round 36 on 2026-10-13)
and the Rear-guard Drone Bay (M7 part G, 2026-10-20: its numbers approved with the drones in round 36,
the document in review); later weapons are still roster rows below.

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [pulse-cannon](pulse-cannon/README.md) | Pulse Cannon — Front, forward; DPS 20 → 70; starter; start | approved | done | final |
| [scatter-vulcan](scatter-vulcan/README.md) | Scatter Vulcan — Front, spread; DPS 18 → 65; 1 200; L02 | approved | done | final |
| [lance-laser](lance-laser/README.md) | Lance Laser — Front, piercing, forward; DPS 25 → 95; 2 500; L05 | approved | done | final |
| [hammer-mortar](hammer-mortar/README.md) | Hammer Mortar — Front, anti-ground, area; DPS 25 → 90; 1 500; L07 | approved | done | final |
| [hornet-launcher](hornet-launcher/README.md) | Hornet Launcher — Front, homing; DPS 15 → 60; 2 000; L10 | approved | done | final |
| [ion-beam](ion-beam/README.md) | Ion Beam — Front, beam; DPS 45 → 130; 5 000; L15 | approved | in-progress | final |
| [side-splitter](side-splitter/README.md) | Side Splitter — Rear, side; DPS 12 → 40; 1 400; L05 | approved | done | final |
| [tail-gun](tail-gun/README.md) | Tail Gun — Rear, rear; DPS 10 → 35; 600; L08 | approved | done | final |
| [fan-blaster](fan-blaster/README.md) | Fan Blaster — Rear, rear, spread; DPS 10.2 → 40; 1 200; L10 | approved | done | final |
| [proximity-mines](proximity-mines/README.md) | Proximity Mines — Rear, rear, area; DPS 20 → 70; 1 500; L12 | approved | done | final |
| [rear-lance](rear-lance/README.md) | Rear Lance — Rear, rear, piercing; DPS 18 → 60; 2 800; L17 | approved | in-progress | none |
| [autocannon-pod](autocannon-pod/README.md) | Autocannon Pod — Wing (per pod), forward; DPS 8 → 25; 500; L02 | approved | done | final |
| [bomb-rack](bomb-rack/README.md) | Bomb Rack — Wing (per pod), anti-ground; DPS 12 → 40; 900; L03 | approved | in-progress | final |
| [micro-missile-pod](micro-missile-pod/README.md) | Micro-missile Pod — Wing (per pod), homing; DPS 8 → 28; 800; L06 | approved | done | final |
| [swivel-gun](swivel-gun/README.md) | Swivel Gun — Wing (per pod), side, homing; DPS 8 → 26; 1 500; L09 | approved | done | final |
| [torpedo-pod](torpedo-pod/README.md) | Torpedo Pod — Wing (per pod), anti-sub; DPS 10 → 32; 1 000; L11 | approved | done | final |
| [harpoon-torpedoes](harpoon-torpedoes/README.md) | Harpoon Torpedoes — Front, anti-sub, forward; DPS 30 → 100; 2 000; L22 | approved | in-progress | chosen |
| [depth-charges](depth-charges/README.md) | Depth Charges — Rear, anti-sub, rear; DPS 25 → 80; 1 300; L22 | approved | in-progress | chosen |
| [deflector-pod](deflector-pod/README.md) | Deflector Pod — Wing (per pod), defensive; absorbs 3 → 5 bullets per 2 s; 1 600; L18 | approved | in-progress | final |
| [light-drone-bay](light-drone-bay/README.md) | Light Drone Bay — Wing (per bay), forward (drone); DPS 10 → 30; 2 000; L15 | approved | in-progress | final |
| [rear-guard-drone-bay](rear-guard-drone-bay/README.md) | Rear-guard Drone Bay — Wing (per bay), rear (drone); DPS 9 → 26; 2 400; L20 | approved | in-progress | final |

## Design

### Common rules

- **Upgrade levels 1–5.** Each level raises damage and/or adds projectiles and raises the power
  draw. Overdrive pickups temporarily add a 6th level pattern.
- **Upgrade cost** (first draft): L2 = 0.5 × base price, L3 = 1 ×, L4 = 2 ×, L5 = 4 ×. Taking a
  weapon from L1 to L5 costs 7.5 × its base price on top of the purchase.
- **Draw**: listed as L1 → L5 in MW. In between it rises linearly and is rounded to 0.5. A fitted
  weapon draws at its **power setting** (the [power dial](../README.md#power-budget), 2026-10-21):
  turned down in the hangar for free, it flies and draws as the lower level.
- **Unlock**: `Lnn` = the item is in the shop from the hangar visit before level *nn*. The
  trait pacing (which trait must be buyable by which level) is owned by the
  [campaign](../../campaign/README.md#loadout-pressure-and-shop-unlock-pacing); the unlock
  levels here are set to match it. Some items are unlocked by data cores or story events
  (captured tech).
- **Layers**: which weapons hit which layer is defined once in the enemy
  [layer rules](../../enemies/README.md#layer-rules). In short: all weapons hit `air`,
  `low-air` and `ground`, hardened ground targets need `anti-ground`, `high-air` takes
  only `homing` and `beam`, and the `sub` layer seen from above water takes only `anti-sub` (the
  Torpedo Pod) and the Smart Bomb, nothing else (no `area` blast, no homing shot, no Rook; user
  decision E2 = a of M5 part E). Weapons marked "ground only" (bombs, mortar shells) do not hit
  flying targets; over water they hit surfaced units only. Under
  water the [Europa rules](../../world/europa/README.md#under-water-rules) apply. The layer
  model itself is in [art direction](../../art-direction/README.md).
- DPS figures are first-draft balancing values; see *Data and balancing* below.

### Data and balancing

All numbers of the designed weapons (pattern, rate, damage, speed, draw, prices) live in the
`data.yaml` next to each weapon's README, the single source; the rules they share (upgrade cost
factors, draw rounding, the single-target DPS target) are in [data.yaml](data.yaml) here (schema:
[data file schemas](../../tech/architecture/README.md#data-file-schemas)). Each weapon's property
and per-level tables are rendered from it with `python3 tools/sync_tables.py`;
`python3 tools/balance.py` checks a typical player's purchases
([balance-plan.yaml](../balance-plan.yaml)) for levels 01–21 against the [credit budget](../../systems/economy/README.md#per-level-budget),
the power cap and the DPS available (and time-to-kill once enemy stat data exists, see the
script's docstring). DPS is given two ways: **volley DPS** (all shots) and **single-target DPS**
(shots that hit a 36 px target 100 px away; seeking, lobbed and dropped weapons count every
shot). M7 part A: a `beam`'s damage per second is both (it burns its first target), a drone bay's
shots all count (they aim at the convergence point), a deflector deals none.

### Roster — later weapons

Still at `idea`; they get a directory when they are designed (Act 3 onwards).

| Name | Slot | Traits | DPS L1/L5 | Draw L1→L5 | Base price | Unlock | Design |
|---|---|---|---|---|---|---|---|
| Plasma Arc | front | area, spread | 30 / 120 (short range, chains to 3–6 targets) | 5 → 8 | 6 000 | L31 | idea |
| Choir Resonator | front | spread, piercing | 45 / 160 | 7 → 11 | 12 000 | L44 (captured Vrell tech) | idea |
| Swarm Tail | rear | rear, homing | 15 / 55 (micro-missiles that curve back up) | 3 → 5 | 3 500 | L30 | idea |
| Tesla Coil Pod | wing | area, shield-breaker | 12 / 40 (short-range zap) | 2 → 4 | 3 000 | L29 | idea |

### Coverage check

Every trait is buyable from the level the campaign's pacing table requires. First item per
trait (the campaign table is the reference for the levels):

| Trait | Required from | First item(s) |
|---|---|---|
| forward | start | Pulse Cannon (starter), Autocannon Pod L02 |
| spread | L02 | Scatter Vulcan L02 |
| anti-ground | L03 | Bomb Rack L03; Airstrike special L04; Hammer Mortar L07 |
| piercing | L05 | Lance Laser L05 |
| side | L05 | Side Splitter L05; Swivel Gun L09 |
| homing | L06 | Micro-missile Pod L06; Swivel Gun L09; Hornet Launcher L10 |
| area | L07 | Hammer Mortar L07; Proximity Mines L12 |
| rear | L08 | Tail Gun L08; Fan Blaster L10 |
| anti-sub | L11 | Torpedo Pod L11; Harpoon Torpedoes, Depth Charges and the Sonar Pulse special L22 |
| beam | L15 | Ion Beam L15 |
| shield-breaker | L29 | Tesla Coil Pod L29 (the EMP Burst special strips shields from L15) |

## Concept art

Concept [round 08](../../concept-rounds/round-08/README.md) — player projectile families; generator `tools/concept/vfx_r08.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/projectiles-r08-a.png](concept/projectiles-r08-a.png) | All 13 projectile families: sprite, 3-frame muzzle flash, 4-frame impact, L1/L3/L5 patterns, missile plumes and trails (sheet) | chosen — beam needs an impact effect where it hits (round 09) |
| [concept/projectiles-r08-a.gif](concept/projectiles-r08-a.gif) | The families firing in sequence from the Stormhawk with fitted pods (motion) | chosen — beam needs an impact effect where it hits (round 09) |

Concept [round 09](../../concept-rounds/round-09/README.md) — generator `tools/concept/vfx_r09.py`.

| File | What | Status |
|---|---|---|
| [concept/beam-impact-r09-a.png](concept/beam-impact-r09-a.png) | Beam impact: the beam bores into the silhouette and ends in a contact flare with back-sparks, scorch and heat shimmer; off-screen when nothing is hit (sheet) | chosen |
| [concept/beam-impact-r09-a.gif](concept/beam-impact-r09-a.gif) | Beam impact: the beam bores into the silhouette and ends in a contact flare with back-sparks, scorch and heat shimmer; off-screen when nothing is hit (motion) | chosen |

Concept [round 14](../../concept-rounds/round-14/README.md) — the Act 1 arsenal's effects as final art; generator `tools/art/weapon_fx.py`.

| File | What | Status |
|---|---|---|
| [concept/weapons-final-r14-a.png](concept/weapons-final-r14-a.png) | Final effects: the Scatter Vulcan, Autocannon and Side Splitter shots at every angle their patterns use, the lance per level, the micro-missile at 32 headings, the bomb and the shell, the ballistic and launcher muzzle flashes, the ballistic and explosive impacts (sheet) | chosen |
| [concept/weapons-final-r14-a.gif](concept/weapons-final-r14-a.gif) | The Stormhawk firing the fan, the pods and the side guns, a missile turning through its headings (motion) | chosen |
| [concept/weapons-capture-final-r14-a.png](concept/weapons-capture-final-r14-a.png) | Game captures of Level 01 with `--loadout`: Vulcan, Side Splitter and bombs; the Lance and two missile pods; the Mortar, bombs and the Autocannon | chosen |

Concept round 28 — the Act 2 arsenal's effects (M5 part A batch; each weapon's review sheet and GIF
are in its own `concept/`); generator `tools/art/act2_weapon_fx.py`.

| File | What | Status |
|---|---|---|
| [concept/act2-weapons-capture-r28-a.png](concept/act2-weapons-capture-r28-a.png) | Game captures with `--loadout` on Act 1 levels: the Fan Blaster, Tail Gun, Hornets, Swivel Guns and Proximity Mines in flight with their final effects | chosen |

Concept [round 36](../../concept-rounds/round-36/README.md) (Act 3 "Red Dust", overnight design
phase 2026-10-12) — the Act 3 front beam and the drone bay, a/b each; generator
`tools/concept/gear_r36.py`.

| File | What | Status |
|---|---|---|
| [concept/ion-beam-r36-a.png](concept/ion-beam-r36-a.png) | [Ion Beam](ion-beam/README.md) A — the chosen round-08/09 beam family per level (L1 6 px to L5 14 px, overdrive 18 px): contact on the first target, a tendril burnt through, rock-scorch on a rock island, the glance spark over a hardened ground target (sheet) | chosen (round 36, G.1) |
| [concept/ion-beam-r36-a.gif](concept/ion-beam-r36-a.gif) | Ion Beam A — Valles canyon at L2: two Ghost Drones in turn, a Tendril Anchor link burnt through, a rock island stopping the beam, a Hive Node passed over (motion) | chosen (round 36, G.1) |
| [concept/rejected/ion-beam-r36-b.png](concept/rejected/ion-beam-r36-b.png) | Ion Beam B "ion stream" — a white core in a cyan sheath with two helix strands and racing packets, ring splash at the contact, slag beads on rock (sheet) | rejected — round 36: a (the round-08/09 beam family) picked |
| [concept/rejected/ion-beam-r36-b.gif](concept/rejected/ion-beam-r36-b.gif) | Ion Beam B — the same scene as A (motion) | rejected — round 36: a (the round-08/09 beam family) picked |
| [concept/light-drone-r36-a.png](concept/light-drone-r36-a.png) | [Light Drone Bay](light-drone-bay/README.md) A — 14×14 disc drone and its dock-ring bay pod: headings, block flash, 36 px orbit and 140 px convergence, block / break (8 hits) / rebuild 6 s, the HUD hits-left pips (sheet) | chosen (round 36, G.2) |
| [concept/light-drone-r36-a.gif](concept/light-drone-r36-a.gif) | Light drone A — two drones orbiting the Stormhawk, firing at the convergence point, absorbing Needler bullets, breaking and being rebuilt (motion) | chosen (round 36, G.2) |
| [concept/rejected/light-drone-r36-b.png](concept/rejected/light-drone-r36-b.png) | Light drone B — a dart drone like a miniature Stormhawk and a slim launch-rail bay (sheet) | rejected — round 36: a (disc drone) picked |
| [concept/rejected/light-drone-r36-b.gif](concept/rejected/light-drone-r36-b.gif) | Light drone B — the same scene as A (motion) | rejected — round 36: a (disc drone) picked |
| [concept/arsenal-capture-r37-a.png](concept/arsenal-capture-r37-a.png) | Game capture (round 37, M7 part A) with `--loadout front=ion-beam:2,left=light-drone-bay,right=deflector-pod,utility=evasive-thrusters` on Act 1 levels: the beam's contact flare and heat tint on the Gorgon Frigate's tentacle, hull and core (L05) and under the Brood Carrier (L07); a drone rammed, broken and rebuilt with the HUD hits strip (L05 hard, scratch data: 2 hits); the Deflector Pod catching bullets and its charges returning (sheet) | chosen (round 37, K.1: accepted) |
| [concept/arsenal-capture-r37-a.mp4](concept/arsenal-capture-r37-a.mp4) | The same, 53 s with the game's sound | chosen (round 37, K.1: accepted) |

Concept [round 48](../../concept-rounds/README.md) (Act 4 "Deep Water", design phase 2026-10-21) —
the two `anti-sub` weapons of Act 4, a/b each, under the round-48 under-water grade with stand-in
targets; generator `tools/concept/gear_r48.py`; prompts in [concept/prompts.md](concept/prompts.md#harpoon-torpedoes-r48-a).

| File | What | Status |
|---|---|---|
| [concept/harpoon-torpedoes-r48-a.png](concept/harpoon-torpedoes-r48-a.png) | [Harpoon Torpedoes](harpoon-torpedoes/README.md) A "barbed harpoon" — a gunmetal shaft with a steel spear head and swept barbs, a teal motor ring and the cable-thin line of light behind it; 32 headings, the volley per level, the weak homing (70° cone, 90°/s), the impact (a dark cavity flash, a teal ring to the 20 px blast) (sheet) | chosen — round 48 |
| [concept/harpoon-torpedoes-r48-a.gif](concept/harpoon-torpedoes-r48-a.gif) | Harpoon A — L3 then L5 (two parallel) homing onto stand-in Needlers and Driftjellies on the Level 24 sea floor (motion) | chosen — round 48 |
| [concept/rejected/harpoon-torpedoes-r48-b.png](concept/rejected/harpoon-torpedoes-r48-b.png) | Harpoon Torpedoes B "heavy torpedo" — a white UTC torpedo with a cobalt nose band and a pump-jet shroud, a straight white-teal wake streak; the impact a white flash with a double ring (sheet) | rejected — round 48: a picked |
| [concept/rejected/harpoon-torpedoes-r48-b.gif](concept/rejected/harpoon-torpedoes-r48-b.gif) | Harpoon B — the same scene as A (motion) | rejected — round 48: a picked |
| [concept/depth-charges-r48-a.png](concept/depth-charges-r48-a.png) | [Depth Charges](depth-charges/README.md) A "drum" — a navy drum with yellow bands and a white fuse lamp blinking faster; roll-out, hover, sink (smaller, darker), the burst radius 40 / 52 / 64 px; the burst a pressure dome (flash disc, dark cavity, rim ring) (sheet) | chosen — round 48 |
| [concept/depth-charges-r48-a.gif](concept/depth-charges-r48-a.gif) | Depth Charges A — L3 then L5 (two charges splayed) bursting on stand-in Driftjellies rising from behind (motion) | chosen — round 48 |
| [concept/rejected/depth-charges-r48-b.png](concept/rejected/depth-charges-r48-b.png) | Depth Charges B "finned canister" — a grey canister with a dome and cross fins, a cyan fuse ring that closes; the burst a sharp double shock ring that refracts the floor (sheet) | rejected — round 48: a picked |
| [concept/rejected/depth-charges-r48-b.gif](concept/rejected/depth-charges-r48-b.gif) | Depth Charges B — the same scene as A (motion) | rejected — round 48: a picked |

## Implementation

- [x] Weapon data format: slot, traits, per-level damage/pattern/draw, price, unlock (the
  fields of each weapon's `data.yaml`)
- [x] Projectile patterns with L1–L5 (+ overdrive) variants for the Act 1 arsenal and the Pulse Cannon
- [x] The Act 2 weapons that need no water: Tail Gun, Fan Blaster, Hornet Launcher and Swivel Gun
  with their own effects and sounds, the Proximity Mines with their delivery (M5 part A; the
  Targeting computer's turn bonus and the mine's arming beep are items of their own documents)
- [x] The Torpedo Pod — M5 part E (the `sub` layer, the level's `water` flag and the torpedo
  delivery, Level 11; its own items in [torpedo-pod](torpedo-pod/README.md#implementation))
- [x] Layer hit rules per trait for the Act 1 arsenal: `anti-ground` (hardened targets, ×2 on the ground for bolts), homing reaching `high-air`, ground-only blasts (`area` of the mortar)
- [x] The mines' `area` on `air`, `low-air` and `ground` (M5 part A)
- [x] `anti-sub` on the `sub` layer, and nothing else but the Smart Bomb reaching it (E2 = a;
  `SubLayerTest`) — M5 part E (step E2a)
- [x] `beam` — **later: Act 3** (the Ion Beam, L15) — M7 part A (step A1; walls, tendrils and
  auras stop or feed it with their parts C and D: the [Ion Beam](ion-beam/README.md#implementation))
- [x] Hangar trait markers linked to the level threat profile

## Open questions

- Should the Ion Beam keep `shield-breaker` after all? That would make shield-breaking available
  from L15 instead of L29 and weaken the Act 5 shop moment.
- The roster is large (24 entries). For the first playable build (Acts 1–2) the 14 designed
  weapons are needed; the earlier suggestion to start with Pulse Cannon, Scatter Vulcan, Tail
  Gun, Micro-missile Pod and Bomb Rack still holds as the order of implementation.

## Decisions

- 2026-09-30: Weapons are described by the shared trait vocabulary; every weapon has L1–L5.
- 2026-09-30: Rows stay in the roster until a weapon gets a detailed design (per CLAUDE.md).
- 2026-09-30: Unlocks expressed as levels and aligned with the campaign's trait pacing: Bomb
  Rack to L03, Lance Laser and Side Splitter to L05, Hammer Mortar to L07, Tail Gun to L08,
  Torpedo Pod to L11 (optional early anti-sub). The Ion Beam loses `shield-breaker` (it is the
  `beam` answer from L15); the Tesla Coil Pod brings `shield-breaker` at L29, as the campaign
  paces it.
- 2026-09-30: Layer hit rules are owned by [enemies](../../enemies/README.md#layer-rules).
- 2026-10-01: Layer hit rules settled (see [enemies](../../enemies/README.md#layer-rules)): `anti-ground` does not add hits on `low-air` (every weapon already hits it); hardened ground targets need `anti-ground`.
- 2026-10-01: The 14 weapons available by L14 promoted to their own documents with per-level numbers in `balance-data.json` (single source, tables generated by `tools/balance.py --sync`). Spread fans (Scatter Vulcan L4–L5, Fan Blaster L4–L5) get a dense core so single-target damage rises with every level.
- 2026-10-01: Concept round 08: projectile families chosen ("very nice"); the beam needs an impact effect where it hits — it currently ends abruptly at the sprite edge (round 09).
- 2026-10-01: Concept round 09: beam impact chosen.
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-02: Weapon numbers moved from `balance-data.json` into a `data.yaml` per weapon plus this directory's [data.yaml](data.yaml) for the shared rules (M2 data files); `tools/sync_tables.py` renders each weapon's property and per-level tables, `tools/balance.py --sync` is gone.
- 2026-10-02: M3 part B2: every designed weapon is sold in the hangar from its unlock level with
  its price, upgrade costs and draw; ◆ markers count a weapon's traits that the next level
  recommends, shown from sensor L3 on (the level at which the intel reveals the recommendation).
  Only the Pulse Cannon flies so far; the others fly from M4.
- 2026-10-02: M4 part A built (user decisions at the part's start: a `--loadout` debug option to fly them on Level 01; the micro-missile's `range` is its seek radius and it lives 1.2 s; missiles seek enemies only and the mortar snaps to destructible ground targets, never a secret's beacon; the effects come from a production generator, reviewed in round 14). The simulation flies every weapon whose delivery it knows (bolts, homing, dropped, lobbed): the Act 1 arsenal, and the Act 2 Tail Gun, Fan Blaster, Hornet Launcher and Swivel Gun with borrowed effects until their levels; the mines and the Torpedo Pod do not fly yet.
- 2026-10-02: Concept round 14 closed (user decision): the Act 1 arsenal's effects approved as **final**; the seven weapons have `art: final`. This doc's `art` stays `chosen` while the Act 2 weapons have concept art only.
- 2026-10-06: M5 part A (user decision D1 = a): part A builds the five Act 2 weapons that need no
  water (the Tail Gun, Fan Blaster, Hornet Launcher and Swivel Gun with their own effects, the
  Proximity Mines with their delivery and `area`) on the Act 1 levels with `--loadout`; the
  Torpedo Pod, `anti-sub` and the `sub` layer move to part E with Level 11. Rejected: b (the torpedo
  in part A with a minimal `sub` layer and dummies: the water rules would be redone in E) and c
  (each weapon with its unlock level's part: the Tail Gun would ship in B with borrowed effects).
  The effects go straight to production (the families were chosen in round 08), reviewed in
  concept round 28 like round 14. The Targeting computer's +20 % turn reaches the Micro-missile
  Pod, the Hornet Launcher and the Swivel Gun's slew ([ship systems](../systems/README.md)).
- 2026-10-06: M5 part A built the five Act 2 weapons that need no water. New deliveries in the
  simulation: `TURRET` (the Swivel Gun: aims its pod all round at its slew, straight shots reaching
  every layer a homing missile does) and `MINE` (the Proximity Mines: dropped, holding their screen
  position, arming, set off by `air` and `low-air` enemies, one `area` blast on `air`, `low-air`
  and `ground`); the Hornet accelerates and leaves its ports in turn. New data fields (schema in
  [architecture](../../tech/architecture/README.md#data-file-schemas)): `accelerate`, `ports`,
  `slew`, `drift`, `arm`, `trigger`; `hits: area` marks the mines. A homing missile's turn and a
  turret's slew are the one turn rate the Targeting computer scales. Effects from
  `tools/art/act2_weapon_fx.py`, proposed for round 28; Act 1's replays and hashes are unchanged.
- 2026-10-06: Concept round 28 closed (user: accepted): the five Act 2 weapons' effects
  (`tools/art/act2_weapon_fx.py`) approved as **final**, and the Tail Gun, Fan Blaster, Hornet
  Launcher, Swivel Gun and Proximity Mines have `art: final` (the Swivel's fixed barrel and the
  mines' weak spots accepted as shown); the mine's arming beep is **a**, the armed chirp. This doc's
  `art` stays `chosen` while the Torpedo Pod has concept art only (part E).
- 2026-10-08: M5 part E (user decision E2 = a of 2026-10-08): only `anti-sub` reaches the `sub`
  layer from above water (and the Smart Bomb, a special); `area` does not, so the line above and the
  open item "`anti-sub` and `area` on the `sub` layer" are corrected to the
  [layer rules](../../enemies/README.md#layer-rules), which always said so. Rejected: b (mortar and
  Rook's Mortar blasts landing on water also hitting `sub` units: a blast rule per delivery, the
  torpedo losing its point, the secret reachable by mortar) and c (as b at 50 %: one more multiplier).
  The ground-only blasts over water hit surfaced units only (a default).
- 2026-10-12: M5 part I (close-out): the Torpedo Pod item ticked (built in part E, its own document
  `done`); every other item is ticked or tagged later (`beam`, Act 3), so the document is `done`.
- 2026-10-12 (overnight design phase): the Act 3 weapons Ion Beam (L1 raised to 45 DPS), Rear Lance,
  Deflector Pod and Light Drone Bay promoted from the roster to draft documents, accepted in round 36 (2026-10-13).
- 2026-10-12 (overnight design phase): concept round 36 proposes an a/b of the Ion Beam and of the
  light drone and its bay (`tools/concept/gear_r36.py`); both documents `art: proposed`, pending the
  user's choice.
- 2026-10-13 (user, round 36): the Ion Beam, Rear Lance, Deflector Pod and Light Drone Bay approved
  (D.20–D.23); the Ion Beam's draw 4 → 8 MW (M11). Picks: the Ion Beam **a** (the round-08/09 beam
  family), the light drone **a** (disc drone, dock-ring bay); the rejected variants moved to
  `concept/rejected/`.
- 2026-10-13 (user, round 37): the Act 3 arsenal's capture accepted (K.1); the Ion Beam's and the light
  drone's production art approved as final (F.1, F.2), the Deflector Pod's look **a** chosen (P.1, b
  rejected and moved to its `concept/rejected/`); the simulation's build choices B.1 (a)–(f) all yes; R.3
  (the Deflector Pod's data layout) accepted; R.4 (a)–(g) all fixed, (c) by Q1's `CAST_ARMOUR` rule (a
  set piece's armour stops the beam on every layer).
- 2026-10-20 (user, round 44, A.b (a): production assets (a `Source` chunk in `assets/`, every Concept art row decided)): **`art: final`** for this part. A later addition (a new sprite, a new concept round) is its own row and does not reopen the rest.
- 2026-10-21 (the user, Act 3 playthrough: "My big weapons take too much power and I can't lower them"):
  the [power dial](../README.md#power-budget): every fitted weapon flies and draws at a power setting
  from L1 to its bought level, turned down and up in the hangar for free.
