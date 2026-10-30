---
title: Special abilities
design: approved
implementation: in-progress
art: final
depends-on: [../../campaign, ../../world]
updated: 2026-10-29
---

# Special abilities

## Summary

The special slot holds one ability, fired with its own button. **Charge-based** specials use up
charges that are bought in the hangar and occasionally found in levels. **Cooldown-based**
specials are expensive to buy but recharge on their own. Some specials depend on the setting:
no airstrikes under the Europa ice. This gives the hangar intel another thing to warn about.

## Design

| Special | Type | Effect | Cost | Unlock | Setting limits | Design |
|---|---|---|---|---|---|---|
| Airstrike | charges, max 4 (1 free at unlock) | Two CDF bombers sweep up the screen: heavy damage (300) to `ground` and `low-air`, 60 to `air` | 300 / charge | L04 (CDF bomber support assigned after L03) | Not under ice/water; not beyond the gate | draft |
| Smart Bomb | max 3 charges (1 free at unlock) | Flash: clears all enemy bullets, 120 damage to everything on screen | 400 / charge | L06 | none | draft |
| Decoy Flares | max 6 charges | Homing missiles and seekers retarget to flares for 4 s | 150 / charge | L27 (with the first homing projectiles) | none | draft |
| EMP Burst | max 3 charges (1 free at unlock) | Stuns machines 3 s and strips enemy shields and aura rings; Vrell (biomechanical) stunned 1.5 s; bosses never stunned (see *Act 3 specials in detail*) | 350 / charge | L15 | none | draft |
| Sonar Pulse | max 4 charges | Pings the water: reveals the `sub` layer and makes it hittable by all weapons for 6 s; under water it also lights every unit and gives every weapon full damage for 6 s (see *Act 4 specials in detail*) | 250 / charge | L22 | Water only (under the Europa ice, from L23); idle with `NO WATER` elsewhere | draft |
| Orbital Lance | max 2 charges | 3 s vertical beam, 40 px wide, that follows the ship's X, 400 DPS on all layers except `sub` (boss parts at most 400 per lance); a `beam` for tendrils | 600 / charge | L17 | Needs satellite cover: not under ice, not beyond the gate | draft |
| Shield Overcharge | cooldown 45 s | Fills the shield to twice its capacity and keeps the capacity doubled for 8 s | 5 000 once | L19 | none | draft |
| Time Dilation | cooldown 60 s | Everything except the player runs at 50 % speed for 5 s | 8 000 once | Act 6 (was L31; Q12 = c of round 60, decided) | none | idea |
| Vrell Swarm Call | cooldown 75 s | Captured Choir tech: summons 6 friendly Vrell drones for 10 s | 10 000 once | L45 | none | idea |

Rules:
- Only one special type can be equipped. Unused charges of other types stay in the inventory.
- Charges persist between levels. Charges used in a failed attempt are restored on retry.
- The special button does nothing if the special is unavailable in this setting. The HUD icon
  is greyed out and the hangar intel warns in advance.

### Acts 1–2 specials in detail

First-draft balancing values. Damage of the Airstrike already includes the ×2 `anti-ground`
bonus. "Boss part" = any hittable part of a mid-boss or act boss.

#### Airstrike (L04)

The numbers the game reads are in [data.yaml](data.yaml) (`airstrike` block); the table below
describes them.

| Property | Value |
|---|---|
| Charges | Bought at 300 cr each, at most 4 carried; one call uses one charge. **1 free charge** when the Airstrike unlocks (hangar visit before L04), so the player can try it without paying |
| Call | Press special → radio line ("Hammer flight, inbound!", Hammer Lead in the generic CDF portrait, text + blip). After **0.6 s** two CDF bombers (56×64 px) enter at the bottom edge at the player's x at the call − 64 px and + 64 px (clamped to the play field) and fly straight up at 600 px/s (0.9 s across the screen) |
| Bombs | Each bomber drops a bomb every 36 px of travel, the first at 18 px (15 each); a bomb bursts 0.25 s after release on the ground point under its release, which the scroll carries down meanwhile (ground coordinates); blast radius 32 px. The strike covers a corridor about 190 px wide |
| Damage | Per blast: 100 to `ground` and `low-air` targets (hardened included), 20 to `air` targets. One strike deals at most **300** to a ground/low-air target and **60** to an air target. `high-air`, `sub` and allies are not hit. Blasts come from above: they ignore frontal armour (the Scuttler's) |
| Rewards | Kills pay their normal bounty and keep the chain multiplier going |
| Bosses | At most 150 per boss part per strike (weak-point multipliers do not apply) |
| Defence | No invulnerability; enemy bullets are not cleared |
| Repeat | A new strike can be called once the bombers have left the screen (≈ 1.6 s after the call) |
| Limits | Not under the ice or under water, not beyond the gate (see the table above) |
| Audio / VFX | `bomb` family with the full whistle plus a jet flyby (SFX list); bombers, bomb carpet and blasts on the round-09 specials sheet |

#### Smart Bomb (L06)

| Property | Value |
|---|---|
| Charges | 400 cr each, at most 3. **1 free charge** when the Smart Bomb unlocks (hangar visit before L06), fitted only into an **empty** special slot (a fitted Airstrike stays) with a hangar notice, like the Airstrike's |
| Effect | Instant. A white flash (0.1 s at 80 % opacity, fading over 0.25 s; reduced by the flash-reduction option) and a shockwave ring that expands from the ship to cover the whole play field in **0.35 s** |
| Bullets | All enemy bullets on screen are removed at once; bullets spawned while the ring expands are removed as it passes them; spore mines pop as the ring passes them, as if shot |
| Damage | **120** once to every enemy on screen, on every layer (`air`, `low-air`, `ground` incl. hardened, `high-air`, `sub`) |
| Bosses | 60 to each boss part |
| Defence | Player invulnerable for **1.0 s** from activation (ship blinks) |
| Repeat | 1.5 s between bombs |
| Rewards | Kills pay their normal bounty and keep the chain multiplier going |
| Audio / VFX | `huge` explosion rung layered with a whoosh; flash and ring on the round-09 specials sheet |

#### Decoy Flares (L27)

Specified for Acts 1–2, but no enemy of Acts 1–3 fires a homing projectile: the first are the
Depth Hunter's torpedoes ([naval](../../enemies/naval/README.md), L27) and the SAM Nest's missiles
([ground](../../enemies/ground/README.md), L29). The unlock therefore moved from L07 to L27, the
hangar visit before the first of them (user decision D4 of M4 part G), so the shop never sells a
special with nothing to decoy.

| Property | Value |
|---|---|
| Charges | 150 cr each, at most 6 |
| Launch | 4 flares from the rear muzzle in a 120° fan backwards, starting at 300 px/s and slowing to a stop over 0.6 s; each burns for **4 s** |
| Effect | While any flare burns, every enemy homing projectile and seeker behaviour targets the nearest flare instead of the player, including homing shots launched during those 4 s. A homing projectile that reaches a flare detonates on it (the flare keeps burning) |
| Not affected | Aimed and straight bullets, lasers, contact attacks |
| Bosses | Same rules for boss homing attacks |
| Damage / defence | None |
| Repeat | 1.0 s between launches |
| Audio / VFX | `micromissile` family pitched up; flares on the round-09 specials sheet |

#### Common input rules

- The special button is buffered for 0.1 s: a press while the strike still flies calls the next one
  if the bombers leave within 0.1 s. With no charge left, no special fitted, a press that the buffer
  could not place (or in a setting where the special is unavailable) it plays the "special denied"
  sound and flashes the HUD special row.
- Charges are counted in flight: a special charge pickup adds one up to the most carried (it is only
  dropped when a special is fitted). The level reports the charges used and found, and the campaign
  applies them only for a won level.
- Charges used in a failed attempt are restored on retry (see above).

### Act 3 specials in detail

Approved in concept round 36 (user, 2026-10-13, D.24; the looks EMP Burst a and Shield
Overcharge b). The tables are hand-written; the numbers the game reads are in [data.yaml](data.yaml)
(M7 part A: the `specials` rows, the `emp`, `orbital_lance` and `overcharge` blocks and
`cooldown_pickup`; keep both in step).

#### EMP Burst (L15)

| Property | Value |
|---|---|
| Charges | 350 cr each, at most 3. **1 free charge** when it unlocks (hangar visit before L15), into the inventory; fitted only into an **empty** special slot, with the hangar notice (the Smart Bomb's rule) |
| Effect | Instant. A blue-white electric ring expands from the ship to cover the whole play field in **0.4 s**; each unit is affected when the ring reaches its centre (the Smart Bomb's timing) |
| Machines | Units whose stat block marks them as **machines** are **stunned 3 s**. In Act 3 these are the unmarked grey units: the Ghost Drone ([air](../../enemies/air/README.md)) and the Warden Tank ([ground](../../enemies/ground/README.md)) |
| Vrell | Every other unit is Vrell (biomechanical) and is **stunned 1.5 s** |
| Stun | A stunned unit's own clock stops: it does not steer, turn, fire or change state, and its timers (fire, windows, surfacing, auras, links) resume where they stopped. An air unit keeps its momentum and drifts on along its line; a ground unit stops where it is on the ground (a chain stops whole). It stays hittable and keeps its contact damage. A new EMP restarts the stun at its full length; stuns do not add up |
| Shields and auras | Strips enemy shields: every **aura ring** a Choir Herald has given (M8 = a) is emptied at once, and a stunned Herald refills nothing during its stun. Any other enemy shield of a later act (a part's shield HP) is emptied the same way |
| Bosses | Boss, mid-boss and set-piece parts are **never stunned**; their shields and aura rings are stripped. The Revenant Walker (a machine mid-boss) may answer the EMP in its own document; by default it does not |
| Not affected | Enemy bullets and mines in flight; units in the `sub` or buried layer (a surfaced Burrower is stunned, and stays up for the stun); tendrils (an anchor is stunned as a Vrell, its link holds: only `beam` breaks a link, M9 = a); allies (domes, Rook, the player's drones) |
| Damage | None. Kills made during a stun pay as usual |
| Defence | No invulnerability, bullets are not cleared |
| Repeat | 1.0 s between bursts |
| Look and sound | The ring with crackling arcs along it; a stunned unit gets a 4-frame electric crackle overlay, its glow dims and a machine's running lights go out; the `tesla` family layered and longer ([sfx](../../audio/sfx/README.md#weapon-sound-families)) |

The machine flag is new: the enemy documents of Act 3 mark the Ghost Drone, the Warden Tank and
the Revenant Walker as machines (D-enemies and D-bosses steps), every Vrell unit is biomechanical
by default.

#### Orbital Lance (L17)

| Property | Value |
|---|---|
| Charges | 600 cr each, at most 2 |
| Call | Press special → an event line on the radio ("Lance uplink locked. Firing!", a CDF orbital officer in the generic CDF portrait, text and blip, like Hammer Lead's call). A thin red targeting line runs down the play field at the ship's x for **0.6 s**, then the lance fires |
| Lance | A vertical column **40 px wide** over the whole play field's height for **3 s**, then a 0.3 s fade. Its centre follows the ship's x at up to **240 px/s**, so it lags a fast dodge and can be swept across a wave |
| Damage | **400 per second** (applied every simulation step) to every enemy whose hit box overlaps the column, on `air`, `low-air`, `ground` (hardened targets included: it comes from above, like the Airstrike, and ignores frontal armour) and `high-air`; not the `sub` or buried layer. It is a `beam` for the tendrils: a link in the column takes its damage (M9 = a) |
| Bosses | 200 per second on each boss part, at most **400** per boss part per lance; weak-point multipliers do not apply |
| Walls | Not stopped by canyon walls or rock islands (it comes from orbit); units on a wall are hit |
| Not affected | Allies (domes, Rook, drones), pickups; enemy bullets are not cleared |
| Defence | No invulnerability |
| Repeat | A new lance can be called 1.0 s after the last one has faded |
| Limits | Needs satellite cover: not under the ice or under water, not beyond the gate (no Act 3 level restricts it) |
| Look and sound | The `beam` family's loop c (`shot-beam-r04-c`, the piercing ray), a scorch line on the ground under the column, a white-hot core with a blue halo |

At 600 a charge (22–33 % of an Act 3 level's budget) it is the strongest single call in the shop:
a `medium` unit inside the column dies in well under a second, and 400 is about 4–7 % of an act
boss's HP at Act 3's reference DPS.

#### Shield Overcharge (L19, the first cooldown special)

| Property | Value |
|---|---|
| Price | **5 000 once**, no charges; sold back at 60 % like other gear |
| Cooldown | **45 s**, counted from each activation |
| Effect | Instant. The shield fills to **twice its capacity** (Mk III: 90 of 45), a broken shield included (its break and regen delay are cancelled). For **8 s** the capacity is doubled: hits drain the doubled shield as usual, and its regen, after the normal delay, refills it towards the doubled capacity. After 8 s the capacity is normal again and any excess bleeds away over 0.5 s |
| Not affected | Armour (no repair), the mercy time, the power load (a special draws no power); the spare-power regen bonus applies as ever |
| Defence | No invulnerability: it is a buffer, not a shield against everything |
| Look and sound | **White** (round 36, G.4 = b; gold is the Ascendancy's colour): at activation the shield ring flares white-hot into a **double shell**, a second ring just outside the first, that stays while overcharged; the HUD's shield bar keeps its 0–45 scale and shows the second layer as **white cores** inside the filled cells; a rising charge-up whoosh (the shield-restore chime's family, not bubbly) |

#### Cooldown specials and the HUD ring

The rules every cooldown special follows (the Shield Overcharge first; Time Dilation and the Vrell
Swarm Call later):

- **Ready at the start** of every attempt: the level start, a retry and a boss checkpoint. Nothing
  carries from one level to the next, and nothing is restored on a retry because nothing is spent.
- **The cooldown runs in level time** from the activation: it stops while the game is paused and
  runs faster with the debug speed.
- **A press while it cools** plays the denied sound and flashes the HUD row (the common input
  rules); the 0.1 s buffer also holds a press made up to 0.1 s before it is ready.
- **A special charge pickup** dropped while a cooldown special is fitted takes **10 s** off the
  running cooldown (nothing when it is ready); the pickup is dropped as for the charge specials.
- **HUD ring** (the special row of the [HUD](../../ui/hud/README.md)): the special's 16 px icon sits
  in a thin ring. While cooling the ring fills clockwise from 12 o'clock in a dim cyan and the row
  shows the whole seconds left where a charge special shows `×` charges (`SHIELD OVERCHARGE 27s`);
  when it is full the ring flashes white once, a short soft ready chime plays (UI synth, never
  bubbly) and the row reads `READY`. While the effect lasts (the Overcharge's 8 s) a white arc
  drains anticlockwise over the ring to show the time left.

### Act 4 specials in detail

*Draft, decided in round 48 (2026-10-22) (Q4 = a, Q1 = b). Step `m8-dsystems`; the numbers are in
[data.yaml](data.yaml) since M8 part A (step A2: the Sonar Pulse's row and `sonar` block, the `flares` block and the
`limits`).*

#### Setting restrictions and the greyed state

A level (or a section of one: L23's descent, L28's breakout) lists the specials it forbids, and the
rule table above is read from that list instead of from the special's name:

| Setting | Forbidden | HUD reason word |
|---|---|---|
| Under the ice, in water (L23 from the waterline to L28's breakout) | Airstrike, Orbital Lance | `UNDER ICE` |
| Above water or dry | Sonar Pulse | `NO WATER` |
| The ice surface (L22), the descent shaft above the waterline | none forbidden (the Airstrike works on the surface; the Sonar Pulse still reads `NO WATER` until the water starts) | — |

- The HUD's special row greys the icon and writes the reason word where the charge count stands; a
  press plays the "special denied" sound and flashes the row (the common input rules). The
  restriction is shown **in advance** in the hangar intel (a `SPECIAL LIMITED` warning on the next
  level's panel, from the Sensor suite's first level; without one, Varga's line says so) and in the
  briefing's threat line, as the table above already promises.
- A charge is never spent on a refused press. When the ship crosses a waterline the row
  changes state at once (a short chime, the row flashes once).
- Level data: the settings' lists are the `limits` of [data.yaml](data.yaml) (`underwater`: Airstrike and Orbital
  Lance, `UNDER ICE`; `dry`, a level or section without water: the Sonar Pulse, `NO WATER`), read from a level's or
  section's `medium` and the level's `water`; a level or section may replace its list with `special_limits:
  [airstrike, orbital-lance]` (the specials' slugs; each must be in a setting's list, which gives its reason word).

#### Sonar Pulse (L22)

| Property | Value |
|---|---|
| Charges | 250 cr each, at most 4; **1 free charge** when it unlocks (the hangar visit before L22), fitted only into an empty special slot like the Smart Bomb's |
| Setting | Water only; in a dry level the row reads `NO WATER`. L22's ice shelf is dry unless its level draft adds open leads |
| Call | Instant. A teal ring expands from the ship to cover the field in 0.5 s (a soft, heavy, low ping: no bubbles, no pops), then every unit stays outlined for the duration |
| Duration | **6 s** from the call; a second call while it lasts restarts the 6 s (no stacking) |
| Above water | Reveals the `sub` layer (the outline) and makes it hittable by **all weapons** for the 6 s, as first drafted |
| Under water | Everything on the screen is lit and drawn whole (outlined in teal), including the lures and the glow-only units of L26 (the Glow Angler's lure is then plain to see), and **every weapon does full damage** (the water's × 0.7, and the energy weapons' × 0.2, are off) for the 6 s; the `sub` layer (a unit rising from below) is hittable by all weapons too. A unit that is only lit by it is not slowed, revealed to the radar or marked for homing: it is a lamp, not a lock |
| Not affected | Enemy bullets and speeds, the Siren's slow, the ceiling (`high-air`) rules |
| Repeat | 1.5 s between pulses |
| Rewards | Kills pay their normal bounty |
| Audio / VFX | A new `sonar` sound family (heavy, no bubbles); the ring and the outlines on the round-48 specials sheet |

Why it exists: above water it was "reveal the sub layer"; under the ice the play plane is already
the water, so the pulse becomes the answer to the dark and to the **×0.7**: a charge buys six
seconds of the surface's firepower for a loadout that still carries non-`anti-sub` guns (the
Act 3 loadout's beam, × 0.2 under water since M8 AW, gains fivefold: 16 → 80 DPS, its reach still 0.4), the lamp for the black level, and the only way
for such a gun to reach a rising `sub` unit.

#### Decoy Flares (L27) under water

Unchanged (see above): under water the flares drift and burn for 4 s as they do in air, the
launch fan stays 120° backwards, the flare's speed × 0.7 like every projectile. Unlocked at the
visit before L27 with the Depth Hunter's torpedoes ([naval](../../enemies/naval/README.md)).

### Act 5 specials (draft)

*Draft, step `m9-d-systems` (2026-10-27), **approved in round 60** (Q12 = c). Act 5
brings no new special; the belt only changes where the old ones work.*

| Setting | Forbidden | HUD reason word |
|---|---|---|
| Inside the hollowed asteroid (L32 *Deep Core*, its tunnels and the refinery: rock overhead, as under the ice) | Airstrike, Orbital Lance | `IN ROCK` |
| The rest of the belt (L29–L31, L33–L35: open space, Ceres Hub's dock canyon, the station hulls) | none (the Airstrike's CDF bombers and the Lance's satellites still cover the belt; at Ceres they are the loyalists') | — |
| Every Act 5 level (vacuum, no water) | Sonar Pulse | `NO WATER` (the existing dry rule) |

- The L32 limit is a new setting list (`underground`, part A, in [data.yaml](data.yaml)'s `limits`) with
  its own reason word, shown in advance in the hangar intel and the briefing's threat line as the
  Act 4 limits are; `IN ROCK` (7 characters) fits the compact row where `UNDER ICE` does.
- **EMP Burst** in Act 5: the Ascendancy's units are machines (stunned 3 s) and wear hex shields,
  which the burst strips (the [enemy shield rule](../../enemies/README.md#enemy-shields)); the hand
  plan fits it from L29 ([generator](../generator/README.md#act-5-hand-plan-draft)). Its numbers are
  unchanged.
- **Decoy Flares** in Act 5: the Ascendancy's homing missiles (the red-gold **homing diamonds** of the
  SAM Nest, the Hornet, the Strider and the Sovereign's batteries) are enemy `Missile`s, so the flares
  decoy them as they decoy the Depth Hunter's torpedoes; unchanged rules.
- **Time Dilation** moves to Act 6 (Q12 = c): a global time scale is a large engine change for one
  shop item in an act whose income (≈ 41 000) does not stretch to it. Under Q12 = b it would stay at L31.

### One special at a time

The special slot holds **one** special; there is no swap button. Specials are changed in the
hangar.

## Concept art

Concept [round 09](../../concept-rounds/round-09/README.md) — generator `tools/concept/vfx_r09.py`. Prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/specials-r09-a.png](concept/specials-r09-a.png) | Airstrike (CDF bombers, bomb carpet), Smart Bomb (flash, ring, bullets popping), Decoy Flares (homing shots retarget) (sheet) | chosen |
| [concept/specials-r09-a.gif](concept/specials-r09-a.gif) | Airstrike (CDF bombers, bomb carpet), Smart Bomb (flash, ring, bullets popping), Decoy Flares (homing shots retarget) (motion) | chosen |
| [concept/airstrike-bomber-final-r17-a.png](concept/airstrike-bomber-final-r17-a.png) | Production art, round 17 (`tools/art/airstrike_bomber.py`): the Airstrike's CDF bomber, 56×64 facing up, 4 engine-flame frames, and its soft ground shadow (sheet) | chosen |
| [concept/airstrike-bomber-final-r17-a.gif](concept/airstrike-bomber-final-r17-a.gif) | Production art, round 17: three bombers crossing a Luna road with their shadows (motion) | chosen |
| [concept/smart-bomb-final-r26-a.png](concept/smart-bomb-final-r26-a.png) | Production art, round 26 (`tools/art/smart_bomb.py`): the Smart Bomb's ring profile (`smart-bomb-ring`, 72×8, additive, drawn as a band of quads round the circle) and the burst at the bomb point (`smart-bomb-burst_0..11`, 128×128, additive, 30 fps), and the whole effect over a dimmed Level 01 field every 4th step (sheet) | chosen |
| [concept/smart-bomb-final-r26-a.gif](concept/smart-bomb-final-r26-a.gif) | Production art, round 26: the flash, burst and ring clearing a field of Needlers and bullets (motion) | chosen |

Concept [round 36](../../concept-rounds/round-36/README.md) (Act 3 "Red Dust", overnight design
phase 2026-10-12) — the EMP Burst and the Shield Overcharge with its cooldown ring, a/b each;
generator `tools/concept/gear_r36.py`.

| File | What | Status |
|---|---|---|
| [concept/emp-burst-r36-a.png](concept/emp-burst-r36-a.png) | EMP Burst A — a ring with crackling arcs at +0.05 / 0.15 / 0.25 / 0.35 s; the stun overlay: machines (Ghost Drone, grey Warden Tank) lights out for 3 s, Vrell (Needler) glow dimmed for 1.5 s (sheet) | chosen (round 36, G.3) |
| [concept/emp-burst-r36-a.gif](concept/emp-burst-r36-a.gif) | EMP Burst A — the burst covers the field in 0.4 s, the machines and Needlers stop while the bullets in flight fly on (motion) | chosen (round 36, G.3) |
| [concept/rejected/emp-burst-r36-b.png](concept/rejected/emp-burst-r36-b.png) | EMP Burst B "arc front" — a dashed double ring that forks into each unit it reaches, flicker and a spark cage as the stun overlay (sheet) | rejected — round 36: a (ring with crackling arcs) picked |
| [concept/rejected/emp-burst-r36-b.gif](concept/rejected/emp-burst-r36-b.gif) | EMP Burst B — the same scene as A (motion) | rejected — round 36: a (ring with crackling arcs) picked |
| [concept/rejected/shield-overcharge-r36-a.png](concept/rejected/shield-overcharge-r36-a.png) | Shield Overcharge A — gold flare and shimmer; the shield bar 0–90 with a gold upper half, the 24×24 cooldown ring and the special row in five states (sheet) | rejected — round 36: b (white double shell) picked; a's gold is the Ascendancy colour |
| [concept/rejected/shield-overcharge-r36-a.gif](concept/rejected/shield-overcharge-r36-a.gif) | Shield Overcharge A — field and right HUD panel: shield 45 → 21, overcharged to 90 for 8 s, the excess bleeds away, a press while cooling denied, the ring filling over 45 s (motion) | rejected — round 36: b (white double shell) picked; a's gold is the Ascendancy colour |
| [concept/shield-overcharge-r36-b.png](concept/shield-overcharge-r36-b.png) | Shield Overcharge B — a white-hot double shell; the bar 0–45 with white second-layer cores (sheet) | chosen (round 36, G.4) |
| [concept/shield-overcharge-r36-b.gif](concept/shield-overcharge-r36-b.gif) | Shield Overcharge B — the same scene as A (motion) | chosen (round 36, G.4) |

Production art for concept round 37 (M7 part A step ART-A, `tools/art/act3_specials.py`; prompts in
[concept/prompts.md](concept/prompts.md#emp-burst-final-r37-a)): the EMP Burst (G.3 = a) and the Shield
Overcharge (G.4 = b) in the chosen looks, and the Orbital Lance, which round 36 showed no look for, as
an a/b proposal (the user picked b, "descending rings", 2026-10-13: b is written to `assets/`, step Q2;
a in `concept/rejected/`). Round 37 closed 2026-10-13: the EMP Burst's and the Shield Overcharge's art
approved as final (F.3, F.4).

| File | What | Status |
|---|---|---|
| [concept/emp-burst-final-r37-a.png](concept/emp-burst-final-r37-a.png) | EMP Burst final effects: the ring as a strip mapped round the circle (`emp-burst-ring`, 4 crackle frames) and the stun crackle overlay in three sizes, on the field (sheet) | chosen (round 37, F.3: approved as final) |
| [concept/emp-burst-final-r37-a.gif](concept/emp-burst-final-r37-a.gif) | The ring covering the field in 0.4 s, Ghost Drones dark for 3 s and Needlers dimmed for 1.5 s under the crackle (motion) | chosen (round 37, F.3: approved as final) |
| [concept/rejected/orbital-lance-r37-a.png](concept/rejected/orbital-lance-r37-a.png) | Orbital Lance A "piercing ray": a solid red targeting line, a column with a wide white-hot core and a blue halo with striations racing down, the scorch tile and embers (sheet) | rejected — round 37: b (descending rings) picked |
| [concept/rejected/orbital-lance-r37-a.gif](concept/rejected/orbital-lance-r37-a.gif) | A: the lock line for 0.6 s, the 40 px column following the ship for 3 s over Needlers and a Hive Node, the scorch left on the ground, the 0.3 s fade (motion) | rejected — round 37: b (descending rings) picked |
| [concept/orbital-lance-r37-b.png](concept/orbital-lance-r37-b.png) | Orbital Lance B "descending rings": a dashed double red line with chevrons running down, a column with a narrower core and bright rings travelling down it (sheet) | chosen (round 37, P.2) |
| [concept/orbital-lance-r37-b.gif](concept/orbital-lance-r37-b.gif) | B in the same scene (motion) | chosen (round 37, P.2) |
| [concept/shield-overcharge-final-r37-a.png](concept/shield-overcharge-final-r37-a.png) | Shield Overcharge final effects: the white double shell loop and the activation flare (sheet) | chosen (round 37, F.4: approved as final) |
| [concept/shield-overcharge-final-r37-a.gif](concept/shield-overcharge-final-r37-a.gif) | The flare, the double shell round the ship for a few seconds, its end (motion) | chosen (round 37, F.4: approved as final) |
| [concept/specials-capture-r37-a.png](concept/specials-capture-r37-a.png) | Game capture (round 37, M7 part A): the EMP Burst stunning Needlers made machines in scratch data (3 s) and Skitters (1.5 s), L05; the Orbital Lance's lock line, column, scorch and Lance Control's line, L07; the Shield Overcharge's double shell and white cores, its cooldown ring (a charge pickup taking 10 s off) through to READY, L09; the Smart Bomb, L14 (sheet) | chosen (round 37, K.2: accepted) |
| [concept/specials-capture-r37-a.mp4](concept/specials-capture-r37-a.mp4) | The same, 46 s with the game's sound | chosen (round 37, K.2: accepted) |

Concept [round 48](../../concept-rounds/README.md) (Act 4 "Deep Water", design phase 2026-10-21) — the
Sonar Pulse under water in Level 26's black, a/b; generator `tools/concept/gear_r48.py`; prompts in
[concept/prompts.md](concept/prompts.md#sonar-pulse-r48-a).

| File | What | Status |
|---|---|---|
| [concept/rejected/sonar-pulse-r48-a.png](concept/rejected/sonar-pulse-r48-a.png) | Sonar Pulse A "flood" — a soft teal band with two echoes that lights the water it passes; every unit drawn whole with a teal outline for 6 s, blinking in the last second; the above-water `sub` reveal; the HUD special row (ready, active, last second, `NO WATER`) (sheet) | rejected — round 48: b picked |
| [concept/rejected/sonar-pulse-r48-a.gif](concept/rejected/sonar-pulse-r48-a.gif) | Sonar Pulse A — the headlight only, the call, the ring covering the field in 0.5 s, 6 s lit, the dark returning (motion) | rejected — round 48: b picked |
| [concept/sonar-pulse-r48-b.png](concept/sonar-pulse-r48-b.png) | Sonar Pulse B "echo marks" — a crisp dashed pale-cyan double ring; the water stays black, units drawn whole inside sonar contact brackets that ping as the ring reaches them (sheet) | chosen — round 48 |
| [concept/sonar-pulse-r48-b.gif](concept/sonar-pulse-r48-b.gif) | Sonar Pulse B — the same scene as A (motion) | chosen — round 48 |
| [concept/sonar-pulse-final-r50-a.png](concept/sonar-pulse-final-r50-a.png) | Sonar Pulse final frames (`tools/art/act4_gear.py sonar`, round 48 G3 = b): the sweep's dash and the contact bracket's corner (ping, steady); the dashed double ring sweeping the black and the brackets snapping in (sheet) | chosen (round 50, F.3: accepted as final) |
| [concept/sonar-pulse-final-r50-a.gif](concept/sonar-pulse-final-r50-a.gif) | The pulse from the ship in the black, stand-in units drawn whole inside their brackets as the ring reaches them, 20 fps at 2x | chosen (round 50, F.3: accepted as final) |
| [concept/sonar-pulse-capture-r50-a.png](concept/sonar-pulse-capture-r50-a.png) | Game capture, Level 11 (`water: true`): the Sonar Pulse's ring sweeping the field and the contact brackets on the units it reached (a frame) | chosen (round 50, F.3: accepted) |
| [concept/sonar-pulse-capture-r50-a.mp4](concept/sonar-pulse-capture-r50-a.mp4) | Game capture with the game's sound: two pulses on Level 11 with their pings, the ring and the brackets | chosen (round 50, F.3: accepted) |
| [concept/decoy-flares-final-r50-a.png](concept/decoy-flares-final-r50-a.png) | Decoy Flares final frames (`tools/art/act4_gear.py flares`, round 09's a): the white-hot flare's 4-frame flicker and its warm halo; four flares fanned out behind the ship under water (sheet) | chosen (round 50, F.4: accepted as final) |
| [concept/decoy-flares-final-r50-a.gif](concept/decoy-flares-final-r50-a.gif) | Four flares launched in the 120° fan behind the ship, slowing to a stop and burning, 20 fps at 2x | chosen (round 50, F.4: accepted as final) |
| [concept/decoy-flares-capture-r50-a.png](concept/decoy-flares-capture-r50-a.png) | Game capture, Level 19 under water: four flares burning behind the ship and a Revenant Walker missile turning for them, the Bulwark beside the ship (a frame) | chosen (round 50, F.4: accepted) |
| [concept/decoy-flares-capture-r50-a.mp4](concept/decoy-flares-capture-r50-a.mp4) | Game capture with the game's sound: the flares launched as the Revenant Walker's pods fire, its missiles bursting on them | chosen (round 50, F.4: accepted) |

## Implementation

- [x] Special slot and charge counting: the special button (`Command.SPECIAL`) with its 0.1 s
  buffer and the denied sound, charges used and found reported by the level and applied for a won
  level only (a retry restores them), the special charge pickup (`--special airstrike:2` fits it
  for testing)
- [x] Cooldown timers — **later: Act 3** (the Shield Overcharge at L19, the first cooldown special) — rules under *Cooldown specials and the HUD ring*
  (M7 part A, step A2: the data, `price` and `cooldown` instead of charges, `cooldown_pickup`; the
  shop's buy-once and sell-back rows in `Hangar` with the gear holding 1 for an owned one, stated
  default accepted in round 37, B.2 d; the timers in the simulation: step A3; the HUD ring and the
  ready chime: step A6)
- [x] EMP Burst as specified in *Act 3 specials in detail* (the stun, the machine flag, stripping aura rings; its free charge at the L15 unlock) — **later: Act 3** (M7 part A; step A2: its row with the free charge and the `emp` block, the stat blocks' `machine` key; the game, step A6: the ring with its arcs, the crackle on stunned units, a stunned Vrell unit's glow dimmed and a machine's lights out, the ring and power-down sounds; the simulation, step A3: `EmpSpec`, the stun stopping the unit's clock, `Act3SpecialsTest`; the aura and shield strip is a hook until the Choir Herald's `aura`, M7 part D)
- [x] Orbital Lance as specified in *Act 3 specials in detail* (the steered column, the boss cap, tendrils as a `beam`, its radio call) — **later: Act 3** (M7 part A; step A2: its row and the `orbital_lance` block; it counts as `anti-ground` for a level's `required` traits, `Hangar.HARDENED_SPECIALS`; the game, step A6: the red targeting line, the column and its scorch line with embers, the lock beep, loop c and the rumble, Lance Control's line on the radio (text and blip until step Q2 cast and rendered her voice, round 37 b); the simulation, step A3: `LanceSpec`, the boss-part ledger, `Act3SpecialsTest`; tendrils as a `beam` with the Tendril Anchor, M7 part C)
- [x] Shield Overcharge as specified in *Act 3 specials in detail* (the doubled capacity, the white double shell and the shield bar's white second-layer cores) — **later: Act 3** (M7 part A; step A2: its row, 5 000 once with a 45 s cooldown, and the `overcharge` block; the game, step A6: the white double shell and its activation flare, the shield bar's white cores, the whoosh and the end shimmer)
- [x] Airstrike as specified in *Acts 1–2 specials in detail* (M4 part D: the production bomber
  sprite with its engine flicker and ground shadow, the Bomb Rack's bomb, the blasts
  `explosion-medium`; the boss-part cap applies once bosses exist)
- [x] Smart Bomb as specified in *Acts 1–2 specials in detail* (M4 part F: its numbers in the
  `smart_bomb` block of data.yaml; `--special smart-bomb:2` fits it for testing; M4 part H: the
  production ring and burst of `tools/art/smart_bomb.py` over the spec's white field flash, approved
  as final in round 26; its chosen sound by the SFX pass)
- [x] One free Smart Bomb charge granted once, at the unlock before L06: fitted only into an empty
  special slot, with the hangar notice (`free_charges: 1` in data.yaml)
- [x] Decoy Flares — **later: Act 4** (unlock at L27 with the first homing projectiles; user decision D4 of M4 part G) (M8 part A, step A2: the `flares` block in [data.yaml](data.yaml), `SpecialsData.Flares`; the simulation in step A3: `Act4SpecialsTest.theFlaresFanOutBackwardsSlowToAStopAndBurnFourSeconds`, `aHomingMissileGoesForTheNearestBurningFlareAndBurstsOnIt`; flown in step A4, `Act4ArsenalFlightTest.theSonarPulseAndTheDecoyFlaresFlyWithTheirSettings`; the look and the chosen r08-a sound in step A6)
- [x] One free Airstrike charge granted once, at the unlock before L04
- [x] Act 5's limit inside the hollowed asteroid (L32): Airstrike and Orbital Lance forbidden, `IN ROCK`; the
  Sonar Pulse `NO WATER` all act — M9 part A (draft: *Act 5 specials*) (step A1: the simulation's
  `SpecialLimits.Reason.IN_ROCK`; step A2: the `underground` limit in [data.yaml](data.yaml), a level's or section's
  `underground: true`, `medium: vacuum` dry, `LevelData.forbiddenReason`, `BeltKeysTest`; step A3: the HUD word fits
  the compact row with the Lance's whole name, `DeepWaterLooksTest`; the intel's `SPECIAL LIMITED` names the setting's
  own word, `IntelPanel.limited`)
- [x] Setting restrictions read from the level data — **later: Act 4** (draft: *Act 4 specials in detail*) (the first restricted setting is under the Europa ice from L22; no Acts 1–3 level restricts a special) (M8 part A, step A2: the `limits` and the level's and section's `medium` and `special_limits`, `SpecialsData.forbidden`/`reason`, `LevelData.forbiddenSpecials`, checked by `ActFourChecks`; `ActFourDataTest`; the simulation's forbidden state in step A3, `Act4SpecialsTest.theAirstrikeReadsUnderIceUnderWaterAndWorksAboveIt`, `aWaterlineChangesTheStateOnceWithItsEvent`; mapped in step A4, `Act4ArsenalFlightTest.aForbiddenSpecialIsRefusedWithItsReasonAndNoChargeSpent`; the greyed row with its word and the limit chime in step A6, `DeepWaterLooksTest.theReasonWordFitsTheSpecialRowWithTheNameCut`)
- [x] HUD special row: icon, name, charges; greyed while the strike flies or with no charge; flashes
  when denied
- [x] HUD: cooldown ring (M7 part A step A6: `ShipPanel`; the row goes compact, the icon in its
  ring where the word `SPECIAL` stands, and reads `OVERCHARGE 27s` / `READY`; the white flash and the
  chime as it is ready; the overcharge's white arc draining anticlockwise)
- [x] HUD: the unavailable-in-this-setting state (greyed icon, the reason word) — **later: Act 4** (with the setting restrictions) (M8 part A, step A6: the greyed row with its word and the limit chime, `DeepWaterLooksTest.theReasonWordFitsTheSpecialRowWithTheNameCut`)
- [x] Sonar Pulse as under *Sonar Pulse (L22)*: the pulse, the lighting, the full-damage window, the 1 free charge — **later: Act 4** (M8 part A; step A2: its row (250 a charge, at most 4, L22, the free charge given at the L22 visit into an empty slot only) and the `sonar` block; the shop row waits for its icon in `Catalogue.AWAITING_ICONS`; the icon from ART-A, the row live in step A5; the simulation in step A3, `Act4SpecialsTest.thePulseOpensSixSecondsOfFullDamageUnderWaterAndACallRestartsThem`, `abovetheWaterThePulseOpensTheSubLayerToEveryWeaponAndItsRingFindsTheContacts`; `ActFourEscortTest.theSonarPulsesFreeChargeComesAtLevel22AndIsFittedOnlyIntoAnEmptySlot`; the ring, the brackets and the ping in step A6, `DeepWaterLooksTest.aContactBracketSnapsInOverAThirdOfASecond`, `ActFourSoundsTest.theSonarPulsePings`)

## Decisions

- 2026-09-30: Two special types (charge-based and cooldown-based); setting limits are a
  deliberate part of level preparation.
- 2026-09-30: Unlocks expressed as levels (`Lnn` = in the shop from the hangar visit before
  level *nn*). The Airstrike arrives at L04, matching the campaign's first-special anchor.
  Underwater limits follow [europa](../../world/europa/README.md#under-water-rules).
- 2026-10-01: One special equipped at a time (user accepted the recommendation).
- 2026-10-01: Airstrike, Smart Bomb and Decoy Flares specified in full for Acts 1–2 (timing, area, damage caps, boss rules, invulnerability, repeat delay).
- 2026-10-01: Concept round 09: specials chosen.
- 2026-10-01: Free first Airstrike charge (user decision): 1 charge is granted when the Airstrike unlocks before L04. The balance plan in `balance-plan.yaml` now buys 1 charge at L04 instead of 2 (same 2 charges carried).
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-02: The charge price, charge limit and unlock of the three Acts 1–2 specials moved into [data.yaml](data.yaml) (M2 data files), where `tools/balance.py` reads them; the specials table stays hand-written for now (mostly prose).
- 2026-10-02: The "2 charges max 4" in the roster against the 1 free charge is left as it is for
  now (user decision during M2); it is an open question for M4, when the Airstrike is built.
- 2026-10-03: Airstrike charges (user decision): 1 free Airstrike charge, given once at the hangar
  visit before Level 04 (a save flag); if the special slot is empty the Airstrike is fitted into it,
  with a hangar notice. The roster cell is now "charges, max 4 (1 free at unlock)", which closes
  the open question.
- 2026-10-03: Bombs, shells and the Airstrike ignore the Scuttler's frontal armour, because they
  come from above (user decision). Their damage carries a from-above flag that the armour check
  reads once the Scuttler is built.
- 2026-10-03: M4 part D conventions (main-agent choice): `Command.SPECIAL` is the last command bit,
  so existing recordings keep their bits; the existing bindings (X, left Ctrl, gamepad B); a 0.1 s
  input buffer; with no charge the denied sound and a flashing HUD row; a new call only once the
  bombers have left the screen; the level reports the charges used and found and the campaign
  applies them only for a won level; the blasts land in ground coordinates (the release point
  carried down by the scroll during the 0.25 s fall); kills pay their bounty and keep the chain;
  a strike hits `ground` and `low-air` (100 per blast, at most 300 per strike) and `air` (20 per
  blast, at most 60), not `high-air`, `sub` or allies; the HUD special box is a right-panel row in
  the weapon rows' style, `SPECIAL  AIRSTRIKE ×2` with the 16 px hangar icon.
- 2026-10-03: The "Hammer flight, inbound!" call queues as an event line (main-agent choice): the
  radio has no interrupting priority yet (the HUD's *priority interrupts* item), so it plays as
  soon as the radio is free and is dropped if it would come later than the stale limit, rather
  than as a timed line that would play however late.
- 2026-10-03: The Airstrike's numbers moved into [data.yaml](data.yaml) (`airstrike` block, with
  the bomber's 56×64 size and the call's radio line), the free charge as `free_charges` and the
  input buffer as `input_buffer`. The Audio / VFX rows cited the round-08 specials sheet; the
  concept is round 09.
- 2026-10-03: M4 part D built the special slot and the Airstrike (see Implementation).
- 2026-10-03: Concept round 17 (user decision): the Airstrike's CDF bomber production art approved as **final** ([round 17](../../concept-rounds/round-17/README.md)).
- 2026-10-04: The Smart Bomb gets **one free charge** at its unlock (hangar visit before L06),
  fitted only into an empty special slot, with a hangar notice, like the Airstrike (user decision
  D5 of M4 part F), so the special is tried at all. The balance plan and the tests keep the
  Airstrike. Rejected: no free charge (at 400 cr, 41 % of budget(6), it would likely never be
  bought in Act 1). It sets the precedent for the Decoy Flares at L07.
- 2026-10-04: M4 part F built the Smart Bomb (main-agent choices): its ring grows from where the
  ship was when it went off; every enemy bullet and mortar blob goes at once, bullets the ring
  passes later go too; each enemy and boss part takes its damage once, when the ring reaches its
  centre; the 1.0 s invulnerability is the ship's mercy time (it blinks); it has no radio call;
  `free_charges: 1` moved into data.yaml.
- 2026-10-04: M4 part F step 3 (main-agent fix): the ring pops the spore mines it passes as if
  they were shot (their burst, their credits), like the bullets it clears.
- 2026-10-05: Decoy Flares (user decision D4 of M4 part G): the unlock moves from L07 to the act
  of the first homing enemy, so they leave the L07 intel and part G. Main-agent reading: the first
  homing projectiles are the Depth Hunter's (L27, Act 4), so the unlock is L27, the level itself
  rather than the act's opener, since the flares are useless before it; whether the Lamprey's
  chase at L12 counts was left open. Rejected: building them at L07 (a special that does
  nothing for 20 levels) and giving the carrier homing seekers to decoy (changes an approved boss).
- 2026-10-05: The user confirmed the Decoy Flares unlock at L27 (the first homing projectile);
  the Lamprey's chase at L12 does not count, so the flares stay out of M5.
- 2026-10-05: M4 part H docs reconciliation: the cooldown timers and ring are tagged for Act 3
  (Shield Overcharge, L19), the setting restrictions and the greyed state for Act 4 (Europa, from
  L22); every other item is ticked, so the document is `done`.
- 2026-10-05: M4 part H (round 26 batch): the Smart Bomb's production art from the chosen round-09
  sheet, `tools/art/smart_bomb.py`: the ring's cross-section (`smart-bomb-ring`, the sheet's halo,
  white core and trailing cyan band) drawn by `FarsideLooks.drawSmartBomb` as 72 quads round the
  circle with the profile across the band, so it stays crisp at every radius and dims over the last
  30 % of its run; a burst at the bomb point (`smart-bomb-burst`, 12 frames at 30 fps); the white
  field flash stays the spec's fill. The strokes remain the fallback without the files. Review
  files proposed for round 26; not drawn yet (kept for later): the bullets popping with a sparkle
  as the ring passes them, which the round-09 sheet shows.
- 2026-10-05: Concept round 26 closed (user: the round accepted as proposed): the Smart Bomb's production ring and burst approved as **final**, with its chosen sound as wired; the bullets' sparkle as the ring passes them stays for later. The part's `art` stays `chosen`: the Decoy Flares (Act 4) have no production art yet.
- 2026-10-12 (overnight design phase): the Act 3 specials specified as draft rows with their
  details: the EMP Burst (machines 3 s, Vrell 1.5 s, a stunned unit's clock stops, aura rings and
  shields stripped, bosses never stunned, one free charge at L15; a new `machine` flag in the enemy
  stat blocks), the Orbital Lance (0.6 s targeting line, a 40 px column for 3 s following the ship
  at 240 px/s, 400 per second, boss parts at most 400 per lance, a `beam` for tendrils, not stopped
  by walls) and the Shield Overcharge (twice the capacity for 8 s); the cooldown rules (ready at
  every attempt's start, level time, a charge pickup takes 10 s off) and the HUD ring. Accepted in round 36 (2026-10-13); the Acts 1–2 specials are unchanged.
- 2026-10-12 (overnight design phase): concept round 36 proposes an a/b of the EMP Burst and of the
  Shield Overcharge with its HUD ring (`tools/concept/gear_r36.py`), pending the user's choice.
- 2026-10-13 (user, round 36): the *Act 3 specials in detail* approved (D.24): the EMP Burst, the
  Orbital Lance (its caller "Lance Control", Q.24g), the Shield Overcharge, the cooldown rules and
  the HUD ring. G.3 = a: the EMP Burst's ring with crackling arcs chosen, the "arc front" (b)
  rejected. **G.4 = b**: the Shield Overcharge is **white** (a white-hot double shell, the shield
  bar's 0–45 scale kept with white second-layer cores, a white arc on the HUD ring); the gold look
  (a, the Ascendancy's colour) rejected. The rejected files moved to `concept/rejected/`. The special
  row's width for "OVERCHARGE" is checked with the HUD in part A.
- 2026-10-13: M7 part A, step A6, the game's side (our reading, **accepted in round 37 (2026-10-13)**: the EMP ring is the 60×128 strip as a band round the circle, fading to 65 %; the
  crackle by the unit's size (under 32 px, under 64 px, above), a stunned Vrell unit's glow at 35 %
  and a machine's out; the lance's scorch columns fade over 4 s, their embers over 1.2 s; the
  overcharge's shell fades to half over the excess's bleed. The Orbital Lance's call is Lance
  Control's line, urgent like Hammer Lead's (the level screen now picks the call by the special's
  kind: the Airstrike's and the lance's; step A3's finding), text and blip until the voice is cast.
- 2026-10-13 (user, round 37): the EMP Burst's and the Shield Overcharge's production art approved as
  final (F.3, F.4); the Orbital Lance's look **b** "descending rings" chosen (P.2; a "piercing ray"
  rejected and moved to `concept/rejected/`, `tools/art/act3_specials.py` routes it there); the
  specials capture accepted (K.2); the compact special row for a cooldown special accepted as a change
  to the approved HUD text (C2); the build choices all kept (B.2 a–e: the stun stops the unit's clock,
  a chain stops whole, no stacking, bosses and `sub` units never stunned; a cooldown special ready at
  every attempt, a pickup takes 10 s off; the Overcharge ends at an attempt's start; B.3 b and d; B.5
  d–g); sounds S.7 EMP ring b, S.8 power-down b, S.9 lance rumble b, S.10 activation b, S.11 end a,
  S.12 ready chime a; Lance Control is Celine Major (S.16 = b, step Q2). R.4 (d), the READY flash
  counted in game time, fixed (step A8).
- 2026-10-18 (user, round 39): C1: this spec wins over Level 16's first table: the Orbital Lance's column is not
  stopped by rock (Level 16's *What walls stop* now lists it apart from the Ion Beam). R.3: the Lance's scorch line
  on the canyon's pale sand kept as it is.
- 2026-10-20 (user, round 44, A.b (a): production assets (a `Source` chunk in `assets/`, every Concept art row decided)): **`art: final`** for this part. A later addition (a new sprite, a new concept round) is its own row and does not reopen the rest.
- 2026-10-21 (Act 4 design phase, step m8-dsystems; **decided in round 48 (2026-10-22)**): setting restrictions specified (a per-level/section list, the HUD reason words `UNDER ICE` / `NO WATER`, shown in advance in the intel), the Sonar Pulse specified for water (it lights the field and gives full damage for 6 s: Q4 = a; above water as drafted; 1 free charge at L22); the Airstrike stays allowed on L22's ice surface. Alternatives from Q4: (b) shots hit only lit units, (c) sonar blips only.
- 2026-10-22 (M8 part A, step A2; accepted in round 50 (2026-10-22)): the Sonar Pulse, the Decoy Flares' numbers and
  the setting restrictions in [data.yaml](data.yaml); a level's `special_limits` name the specials by slug (the
  proposal's form) and replace the setting's list; the reason word is the one of the setting that lists the special;
  the Sonar Pulse works wherever there is water (`medium: underwater` or a `water: true` level, D4 of the gap check).
- 2026-10-22 (M8 part A, steps A3–A6; accepted in round 50 (2026-10-22)): the Decoy Flares decoy enemy `Missile`s only (D5; a seeker flag comes with the first unit that needs one, part G); the flares start 20 px behind the ship and a missile bursts on a flare inside a 10 px box; a level's or section's list may allow the Airstrike under water (it replaces the medium's rule; the Sonar needs water always); the Sonar Pulse and the Flares get no radio call; ART-A's flare glow is dithered and leaves no smoke under water.
- 2026-10-22 (user, [round 50](../../concept-rounds/round-50/README.md) closed): F.3 (the Sonar Pulse) and F.4 (the Decoy Flares): the final art and the game captures accepted, the concept rows `chosen`. B.2 accepted: the Sonar Pulse works in any water (under water and on the `water: true` Levels 11–13), `NO WATER` on dry levels and L22's ice. The Sonar Pulse keeps its chosen ping r04-a (S.7 a).
- 2026-10-27 (Act 5 design phase, step m9-d-systems; **approved in round 60**): Act 5's limits:
  the Airstrike and the Orbital Lance forbidden only inside the hollowed asteroid of L32 (`IN ROCK`), allowed
  elsewhere in the belt; the Sonar Pulse reads `NO WATER` all act; no new special in Act 5, Time Dilation to Act 6
  (Q12 = c; alternatives: (b) at L31 as first drafted).
