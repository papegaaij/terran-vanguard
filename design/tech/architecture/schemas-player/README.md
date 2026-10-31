---
title: Data file schemas — player and economy
design: approved
implementation: in-progress
art: n/a
updated: 2026-10-31
---

# Data file schemas — player and economy

## Summary

The schemas of the player's data files (weapons, the ship and its core parts, specials and utility modules, pickups, the wingmen) and of the systems' files (economy, difficulty, scoring, retry). Moved unchanged from the [architecture](../README.md#data-file-schemas)'s Data file schemas, which keep the shared conventions (keys, units, sizes) and the index of every schema.

## Design

- **Weapon** (`player/weapons/<slug>/data.yaml`): `name`, `slot` (`front`/`rear`/`wing`),
  `traits`, `price` (0 = starter), `upgrade_base` (default: the price), `unlock` (level),
  `draw` (`[L1, L5]`), `sfx`, `vfx`, `hits`, `speed` (px/s or `[start, end]`; none when lobbed
  or dropped), `size`, `range` (px, `screen` or `drop`) or `lifetime` (a homing weapon has both:
  its `range` is the seek radius), the behaviour numbers `converge` (° a pod turns in towards the
  centre line), `fall` (s a dropped bomb falls), `flight` (s a lobbed shell flies), `snap` (px a
  shell's auto-aim reaches), `cone` (° ahead a homing shot picks its target in; a turret's forward
  cone), `accelerate` (s an accelerating shot takes from its `speed` start to its end), `ports` (px
  either side of the muzzle the shots of a volley leave from in turn, left first), `slew` (°/s: a
  turret, which aims its straight shots at the nearest enemy all round), the mines' `drift` (s their
  drop's drift decays over), `arm` (s before they arm) and `trigger` (px an `air` or `low-air` enemy
  sets one off at; mines have `hits: area`, a `lifetime` and per level a `blast` and `max_live`),
  flags `mirrored` (fires the same pattern to the left too; numbers per side), `pod` (numbers per pod) and `seek`
  (homing, lobbed or dropped: every projectile counts as a hit on a single target), then
  `levels` (five) and `overdrive`, each with `pattern`, `rate` (volleys/s), `damage` per
  projectile and optional `pierce`, `blast`, `turn` (°/s), `max_live`. A pattern lists one
  `[x offset, angle]` per projectile; angle 0 = straight up the screen, 90 = right,
  180 = straight back. The damage of ground-only `anti-ground` weapons already includes the ×2
  anti-ground bonus. `notes.layers_hit` is the README's *Layers hit* cell. Shared rules
  (`player/weapons/data.yaml`): `upgrade_cost_factors` (L2–L5), `draw_round`,
  `single_target` (`distance`, `width`).
  M5 part E (the [Torpedo Pod](../../../player/weapons/torpedo-pod/README.md); user decisions E1 and E2
  of 2026-10-08; built in step E2a): `hits: anti-sub` is the **torpedo** delivery
  (`WeaponSpec.Delivery.TORPEDO`): dropped from its pod, it runs on the `sub` layer from its `speed`
  start to its end over `accelerate` s, steers at most the level's `turn` (not scaled by the
  Targeting computer) toward the nearest `sub` or `ground` unit within its `range` in its `cone`
  (picked as fired and again when the target dies or leaves its reach; without one it runs
  straight) and lives for its `range`; it strikes the first `sub` or `ground` hit box it touches
  (units, set-piece parts, destructibles, a `sub` trigger) at full damage, never a flyer, and passes
  under crane arms, sleds, tow cables and debris; only in a level with `water: true`, elsewhere the
  pod fires nothing (`Sortie.mountIdle`) but still draws its power; pooled, no allocation per step.
  `content` requires a speed, a range in px, a `cone`, `accelerate` when the speed rises, a `turn`
  on every level and no `lifetime`.
  M7 part A (Act 3's arsenal; built in step A2, the simulation's specs in steps A1 and A4):
  `hits: beam` (the [Ion Beam](../../../player/weapons/ion-beam/README.md)) has no `speed`, `size`,
  `range` or `lifetime`, and every level and the overdrive is `{width, damage}`: the beam's hit width
  in px and its damage **per second** (no `pattern` or `rate`; volley and single-target DPS are that
  damage). `hits: deflector` (the [Deflector Pod](../../../player/weapons/deflector-pod/README.md), a
  `pod`) has no projectile either, and every level and the overdrive is a field `{radius, charges,
  recharge}` (px round the ship's centre on the pod's side, the bullets it catches, s until one charge
  returns) with no `pattern`, `rate` or `damage`. A **`drone`** block (the [Light Drone
  Bay](../../../player/weapons/light-drone-bay/README.md), `hits: standard`, a `pod`) holds the drone's
  `size` and `hitbox`, `orbit` (px), `period` (s a turn), `converge` (px ahead of the ship's centre its
  shots aim at), `fade_in` (s) and `contact` (`hits`, `small_hits`, `laser_hits`, `damage`); every
  level and the overdrive then adds `drone_hits` (hits before it breaks) and `rebuild` (s). A drone
  bay's shots count as single-target hits; a deflector's DPS is 0. `SimSpecs.flies` is false for the
  three until their specs are mapped (step A4); the Rear Lance is a plain `standard` rear weapon.
  M7 part G (the [Rear-guard Drone Bay](../../../player/weapons/rear-guard-drone-bay/README.md); loaded and checked
  in step G2 by content's `WeaponData` and `RetreatChecks`, flown from step G3, mapped in step G4): a drone's
  **`station`** (`orbit`, the default, or `trail`); a trailing drone has `behind` (px behind the ship's centre),
  `side` (px to its bay's side), `lag` (s it follows the ship with) and `fire: back` (straight back) instead of
  `orbit`, `period` and `converge`; its bay is a wing `pod` with the `rear` trait. Step G4 maps the trail station
  (`RetreatRules.trail` → `DroneSpec.trail`), so `SimSpecs.flies` is true for it.
  M8 part A (Act 4's anti-sub pair, round 48; loaded and checked in step A2 by content's `WeaponData`, the
  simulation's specs in step A1, mapped in step A4): the trait **`anti-sub`** (`WeaponData.antiSub`) keeps a weapon's
  full damage under water (see *Europa*). The [Harpoon Torpedoes](../../../player/weapons/harpoon-torpedoes/README.md)
  are a front `hits: anti-sub` torpedo (fired from the nose) with **`splash`** (a share: a hit blasts its level's
  `blast` radius for that share of its damage on the others there, not on the struck target; only a torpedo's, every
  level and the overdrive with a `blast`). **`hits: depth-charge`** (the [Depth
  Charges](../../../player/weapons/depth-charges/README.md), `WeaponData.DEPTH_CHARGE`) has one `speed` (px/s backwards
  at the roll-out), a `size` and a `drift` (s the roll-out decays to a hover over) and no `range` or `lifetime`; every
  level and the overdrive add a **`fuse`** (s from the launch to the burst; only a depth charge's) and a `blast` (the
  burst radius); its tables are rendered by `tools/sync_tables.py`'s depth-charge rows. `notes.vfx` overrides the
  README's *Projectile family* cell (the round-48 sheets).
  M9 part A (Act 5's pair, round 60; loaded and checked in step A2 by content's `WeaponData`, the simulation's specs
  in step A1): **`hits: arc`** (the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md),
  `WeaponData.ARC`, the simulation's `Delivery.ARC`) is a wing `pod` without `speed`, `size`, `range` or `lifetime`: a
  `cone` (° of the forward arc it picks its target in), a `splash` (the sparks' share of the arc's damage on the other
  units within its level's `blast` radius) and per level and the overdrive one shot in its `pattern`, a `rate`, a
  `damage`, a **`reach`** (px from the pod; only an arc's) and a `blast` (the sparks' radius); its tables are rendered
  by `tools/sync_tables.py`'s arc rows. The trait **`shield-breaker`** (`WeaponData.shieldBreaker`) empties an enemy
  shield at once ([enemy shields](../../../enemies/README.md#enemy-shields)). **`pick`** (`nearest` when absent,
  `rear-first`: the [Swarm Tail](../../../player/weapons/swarm-tail/README.md)'s nearest enemy behind the ship's nose
  line within its `range` first, else the nearest anywhere in it) is a homing missile's (`hits: homing`, no `slew`).
  M10 part A (Act 6's front gun, round 72; loaded and checked in step A2a by content's `WeaponData`, the simulation's
  `CHAIN` delivery steps A1b and A2b's): **`hits: chain`** (the [Plasma Arc](../../../player/weapons/plasma-arc/README.md),
  `WeaponData.CHAIN`) is a front gun without `pod`, `speed`, `size`, `range`, `lifetime` or `splash`: a `cone` (° of the
  forward cone its first strike picks its target in), a **`falloff`** (each jump's share of the damage of the strike
  before it; only a chain's) and per level and the overdrive one shot in its `pattern`, a `rate`, a `damage`, a `reach`
  (px from the nose), a **`jump`** (px from each struck unit to the next) and a **`chain`** (the most units a discharge
  strikes, its first strike included); single-target DPS is the first strike, the volley a full chain
  (`tools/design_data.py`), its tables rendered by `tools/sync_tables.py`'s chain rows.
- **Ship and core parts**: ship (`acceleration_seconds`, `stop_seconds`, `precision_factor`,
  `size`, `edge_gap`, `hull` (hit boxes `[x, y, width, height]` from the sprite's top left), `collection_radius`, `mercy_seconds`, `bank_change_steps`,
  `mounts`: `front`, `wings`, `roots` (the side guns' muzzles), `rear`, `engines`; its speed is the fitted engine's); shields (`break_seconds`, `models` with
  `capacity`, `regen`, `delay`, `draw`; M10 part A: a variant shield's **`reflect`**: `window`, the share of the capacity
  it holds at least when a bullet hits, `share` of the bullet's damage it takes then, the bolt's `speed` px/s and
  `factor` × the bullet class's damage: the [Reflex Shield](../../../player/shields/README.md#reflex-shield-act-6));
  armour (`plating` with `max`, and the low-armour `radio`
  line: `speaker`, optional `expression` and `distorted`, `line`); generator
  (`spare_power`, `models` with `output`; M9 part A: `drain` with `per` (MW each latched drainer takes off the
  effective output), `max` (MW all of them at most) and `floor` (the weapons' lowest fire-rate factor), the
  [enemy drain effects](../../../player/generator/README.md#enemy-drain-effects)); systems (`engines` with `speed`, `draw`; `bays` with `start` (the starting utility bays) and
  `extra` (each bought bay's `price` and `available`; their count and `start` add up to the loadout's
  utility slots); `utility`
  with `draw`, one price per level, `design` status, the Pickup magnet's `magnet` (`radius`, `pull`, one
  per level) and an optional `for_sale` (false keeps an unlocked module out of the shop); the
  Targeting computer's `targeting` (`turn_bonus`, 0.2: the share added to the
  Stormhawk's homing turn rates and the Swivel's slew; `bar_seconds`, 1.5: how long an HP bar stays
  after the last hit; `bar_fade`, 0.3 s) and the Salvage scanner's `salvage` (`bonus`, one share
  per level: `[0.1, 0.2]`) and (M7 part A) the Evasive thrusters' `dash` (`distance` px, `seconds`,
  `invulnerable` s and `cooldown` s from its start, `double_tap` s between the first press's release
  and the second press); M8 part A: the Auto-repair nanites' `repair` (`interval`, s per armour point, one per level;
  `cap`, the share of the plating's maximum it repairs up to; `pause`, s a hit pauses it) and the Pressure hull's
  `hull` (`drag: false`: no under-water drag); M10 part A: the Ascendancy IFF spoofer's **`spoof`** (`delay`, s a
  `spoofable` unit's aimed volley or told attack comes late)); an item's `available` is an availability key (`act 6`)
  or a level (`L39`: in the shop from that level's visit), each core list's in its order (`CampaignChecks`); specials (`input_buffer` (s a press waits
  while the special is busy), `cooldown_pickup` (M7 part A: s a special charge pickup takes off a
  cooldown special's running cooldown), `specials` with `name`, `unlock` and either `charge_price`,
  `max_charges` and optional `free_charges` (given once at the unlock) or (M7 part A, a cooldown
  special bought once) `price` and `cooldown` (s), and the `airstrike` block: `delay`, `offset`,
  `speed`, `bomber_size`, `bomb_spacing`, `fall`, `blast_radius`, `damage` (`ground`, `air` per
  blast), `cap` (`ground`, `air`, `boss_part` per strike) and the call's `radio` (`speaker`,
  `portrait`, `line`), and the `smart_bomb` block: `flash` (`seconds` held at `opacity`, then
  `fade` s), `ring` (s to cover the play field), `damage` (`all`, `boss_part`), `invulnerable` and
  `repeat` (s); M7 part A: the `emp` block (`ring` s, `stun` s for a `machine` and a `vrell` unit,
  `repeat` s), the `orbital_lance` block (`lock` s, `width` px, `seconds`, `fade` s, `follow` px/s,
  `dps`, `boss_part` `dps` and `cap`, `repeat` s, the call's `radio`) and the `overcharge` block
  (`factor` on the shield's capacity, `seconds`, `bleed` s); M8 part A: the `sonar` block (`ring` s, `seconds` a
  pulse lasts, `repeat` s), the `flares` block (`count`, `fan` °, `speed` px/s, `drift` s, `burn` s, `repeat` s) and
  the **`limits`** (the setting restrictions: per `setting`, `underwater` or `dry` (neither under water nor a `water:
  true` level), the specials it `forbids` by name and the HUD's `reason` word; a special is in at most one setting,
  `SpecialsData.forbidden`/`reason`)). Each model also has `name`, `price` and
  `available`.
- **Player** (`player/data.yaml`): `availability`, `pickup_seconds`, `pickup_drift_speed`, `pickups` (salvage
  credits, overdrive `levels` and `seconds`, shield cell `shield_percent`, armour patch
  `armour`, special charge `charges`, data core). Levels name pickups as `small salvage`,
  `armour patch`, ….
- **Systems**: economy (`starting_credits`, `budget` `base` and `growth` (the typical haul),
  `act_factor`, `sell_back`, `typical_player` shares `air_kills`, `ground_targets`, `secrets`,
  `pickups`, `primary`, `secondary`); difficulty (one `{easy, medium, hard}` entry per lever: factors such as
  `enemy_hp`, changes such as `formation_size` (−0.2 = −20 %), `aimed_spread_degrees`, `bullet_budget`,
  `repair_cost`, `retries`, `boss_checkpoint`, `sensor_bonus`); scoring (`kill_score`,
  `pickup_score`, `chain`, `rating` weights and `full_chain`, `bonuses`, `grades`); retry (`armour_floor`, the
  share of the maximum armour a retry starts with at least; `game_over`, Okafor's last transmission on
  the game over screen: `speaker`, `expression`, `line`, spoken through the radio filter).
- **Wingmen** (`player/wingmen/data.yaml`, see
  [wingmen](../../../player/wingmen/README.md#data)): `rook` (`joins` (the first
  level), `side` (a new campaign's), `armour`, `size`, `hitbox`, `muzzle` (his one gun's muzzle,
  px from the sprite's top left), `speed`, `acceleration_seconds`,
  `min_distance`, `edge_gap`, `ram_damage`, `launch_warning` (share of his armour), `glide_seconds`,
  `swap_seconds`, M8 part A: `missing` (`after`, the level he is shot down at the end of, the story flag
  `rook-missing` from its win; `back`, the level he flies again from), `formations` (`wing`, `wide`, `trail`: `[x, y]` offsets from the player's centre,
  y down, x mirrored on the left side), `flank_distance`, `reaction_seconds`, `dodge` (`interval`,
  `look_ahead`, `clearance` (also his gap to the air enemies' bodies), `step`, `reacts` (the share
  of the predicted bullets he reacts to, each decided once by his own seeded generator)), `cone` (° full width), `range`, `recent_hit_seconds`, `eject`
  (`explosion`, `pod_speed`)); `guns` (`price_factor` of the base weapon's price and upgrade base,
  and the `list`, the first the free starter: `id`, `name`, `base` (a weapon slug), `scale` (on each
  projectile's damage), `available`); `barks` (`speaker`, `spacing` s, and `triggers` in priority
  order: `trigger` (`boss-warning`, `rear-wave` with `ahead` s, `sides-wave`, `player-armour` and
  `rook-armour` with `below` (share), `rook-ejects`, `kill-streak` with `kills` and `seconds`,
  `overdrive`), the portrait's `expression`, optional `shout`, and the variant `lines`; M7 part G: the rear wave's
  optional `top_lines`, its variants in a reversed section, where behind is the top of the screen). His repair
  cost is the difficulty's `repair_cost`. M8 part A: **`bulwark`**, the Bulwark heavy drone (`armour`, `size`,
  `hitbox`, `speed`, `slot` (`[x, y]`, its fixed wing slot on the escort's side), `gun` (`rate`, `damage`,
  `anti_sub`), `draw` (`radius` px, `share` of the enemy's aimed bullets it draws), `price`, `available`, `repair` cr
  per point on every difficulty).
