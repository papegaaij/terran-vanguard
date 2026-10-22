---
title: Sound effects
design: approved
implementation: in-progress
art: final
depends-on: [../../player, ../../enemies, ../../ui]
updated: 2026-10-22
---

# Sound effects

## Summary

The full list of sound effects, grouped by category, each with a priority for the first
playable build (P1), the first act (P2), or later (P3). Mixing rules keep the constant player
fire from drowning out everything else.

## Design

### Sourcing

Player shots and explosions use **recorded third-party sounds** instead of synthesis (round 01
decision). Only CC0 or CC-BY sources are used (the repository is public); each file is listed
in [CREDITS.md](../../../CREDITS.md) and CC-BY authors appear on the in-game credits screen.
Imports are reproducible via `tools/concept/audio/import_sfx.py` (concept: Freesound HQ
previews; production: rebuild from the original files). Pickups and UI sounds stay synthesized.
Round 08 follows the same split: recorded hits, player damage, enemy, special, radio-squelch,
klaxon and ambience sounds (`import_sfx.py`); synthesized pickups by type and UI blips in the
family of the chosen round-01 pickups (`tools/concept/audio/sfx_r08.py`). Recorded one-shots are
levelled on the 200 Hz–5 kHz band so sub-heavy sources stay audible on small speakers;
where a source needs EQ (sub-sonic drift, a harsh top end) the high- or low-pass is set and
commented on its `import_sfx.py` entry. A source that is almost all sub-bass is replaced, not
boosted.

### Weapon sound families

Every weapon and weapon-like special plays the sound of its **family**, so a new weapon only
needs a family, and levels of the same weapon differ by pitch and layering, not by new files.
Round 03 proposals; the reused round 02 shots are marked (r02). Files are in `concept/`.

| Family | Weapons (slot) | Specials | Concept files | Character |
|---|---|---|---|---|
| `pulse` | Pulse Cannon (front), Tail Gun, Fan Blaster (rear), Side Splitter (rear), Light Drone Bay shots (wing) | — | [a](concept/player-shot-r02-a.ogg) (r02), [b, heavy](concept/player-shot-r02-b.ogg) (r02) | Short energy projectile; the most-heard sound, so quiet and pitch-varied (±5%). Rear guns play it ~10% lower. |
| `vulcan` | Scatter Vulcan (front) | — | [a](concept/player-shot-r02-e.ogg) (r02), [b](concept/shot-vulcan-r03-b.ogg) | Heavy rotary/autocannon chatter; one sound per volley, not per bullet of the fan. |
| `ballistic` | Autocannon Pod, Swivel Gun (wing) | — | [a](concept/player-shot-r02-d.ogg) (r02) | Recorded single gunshot, dry and short. |
| `laser` | Lance Laser (front), Rear Lance (rear) | — | [a](concept/player-shot-r02-c.ogg) (r02), [b](concept/shot-laser-r03-b.ogg) | Descending energy sweep; piercing weapons sound "longer" than pulse. |
| `beam` | Ion Beam (front) | Orbital Lance | loops: [a](concept/shot-beam-r04-a.ogg) (Ion Beam), [b](concept/shot-beam-r04-b.ogg) (alternative), [c](concept/shot-beam-r04-c.ogg) (Orbital Lance); [start](concept/shot-beam-start-r04-a.ogg), [stop](concept/shot-beam-stop-r04-a.ogg) (round 04) | **Seamless loops** played while firing: start → loop → stop. a = smooth mid-range laser hum (~850 Hz), b = gritty pulsing energy loop, c = piercing ~1.65 kHz ray. The round 03 loops were ~100% below 200 Hz (inaudible on small speakers), so beams now keep ≥ 70% of their energy in 200 Hz–5 kHz and are normalised on that band's RMS (−30 dB) to match the shots' loudness. |
| `missile` | Hornet Launcher (front) | — | [a](concept/shot-missile-r03-a.ogg) | Rocket ignition + whoosh. |
| `micromissile` | Micro-missile Pod (wing), Swarm Tail (rear) | Decoy Flares (pitched up) | [a](concept/shot-micromissile-r03-a.ogg) | Small, quick rocket launch; frequent, so short. |
| `mortar` | Hammer Mortar (front) | — | [a](concept/shot-mortar-r03-a.ogg) | Hollow tube thump for lobbed shells. |
| `bomb` | Bomb Rack (wing) | Airstrike (full-length whistle) | [a](concept/shot-bomb-r03-a.ogg) | Falling-bomb whistle, cut short for the rack; the Airstrike uses the source's full whistle. |
| `torpedo` | Torpedo Pod (wing), Harpoon Torpedoes (front) | Sonar Pulse (see Specials) | [a](concept/shot-torpedo-r03-a.ogg) | Muffled underwater launch with bubbles; above water the same file is played drier/brighter. |
| `mine` | Proximity Mines, Depth Charges (rear) | — | [a](concept/shot-mine-r03-a.ogg) | Metallic drop-and-bounce clunk; mines add an arming beep (UI synth), depth charges a splash. |
| `tesla` | Tesla Coil Pod (wing), Plasma Arc (front) | EMP Burst (layered, longer) | [a](concept/shot-tesla-r03-a.ogg) | Electric zap/crackle; Plasma Arc chains replay it per jump at rising pitch. |
| `resonator` | Choir Resonator (front, captured Vrell tech) | Vrell Swarm Call | [a](concept/shot-resonator-r03-a.ogg) | Big alien energy cannon; later layered with a Choir chord from the music. |
| — | Deflector Pod (wing, defensive) | Smart Bomb, Shield Overcharge, Time Dilation | — | Not shots: use shield/impact and special sounds (see below); Smart Bomb uses the `huge` explosion rung. |

### Explosion ladder

Explosions are chosen by **enemy size**, with a random variant per kill (never the same file
twice in a row) plus ±4% pitch, so repeated kills don't sound identical. Round 03 proposals;
reused round 02 files marked (r02).

| Rung | Length | Files | Enemies (examples from the [roster](../../enemies/README.md)) |
|---|---|---|---|
| `tiny` | ≤ 0.6 s | [a](concept/explosion-tiny-r03-a.ogg), [b](concept/explosion-tiny-r03-b.ogg) | Skitter, Asteroid Mite, spores, shootable missiles and mines, Shard Drone links |
| `small` | 0.7–1.1 s | [a](concept/explosion-r02-a.ogg) (r02), [b](concept/explosion-r02-b.ogg) (r02), [c](concept/explosion-small-r03-a.ogg) | Needler, Stinger, Talon, Ghost Drone, Harrow, Skimmer, Spine Turret, Driftjelly, Ravager |
| `medium` | 1.6–2 s | [a](concept/explosion-r02-c.ogg) (r02), [b](concept/explosion-medium-r03-b.ogg) | Gilded Gunship, Hornet, Chimera, Mantis, Creeper, SAM Nest, Crawler Tank, Reef Spitter, Minelayer, destroyed buildings |
| `large` | 2.8–3.1 s | [a](concept/explosion-r02-d.ogg) (r02), [b](concept/explosion-r02-e.ogg) (r02), [c](concept/explosion-large-r03-a.ogg) | Hive Node, Sentinel Tower, Rail Bunker, Abyss Ray, Choir Seraph, mid-boss parts, boss phase ends |
| `huge` | 5–6 s | [a](concept/explosion-huge-r03-b.ogg) (sub-heavy boom), [b](concept/explosion-huge-r04-a.ogg) (recorded 4 kg TNT blast with debris, round 04) | Act-boss deaths, capital ships, Smart Bomb, Iron Sovereign core |
| `underwater` | 3–3.5 s | [a, recorded](concept/explosion-underwater-r03-a.ogg), [b](concept/explosion-underwater-r04-a.ogg) (round 04) | Everything below the surface in Act 4 (Europa). The derived low-pass variant was rejected, so under-water sounds need recorded sources |
| `water` | 1.9 s | [a](concept/explosion-water-r03-a.ogg) | Surface naval kills (Act 2 ocean, Europa ice floes), depth-charge hits. In a level with `water: true` (M5 part E, Level 11) every kill on the play field plays it over its size rung, a torpedo's impact plays it, and a landing blast (bomb, mortar shell, the Airstrike) plays it instead of its ground blast |

### Player weapons

| Sound | Notes | Priority |
|---|---|---|
| Pulse Cannon shot | Short, bright "pew"; very frequent, so quiet and varied (3 pitch variants) | P1 |
| Scatter Vulcan shot | Rapid chatter | P1 |
| Hornet / micro-missile launch | Whoosh with a small ignition pop; the Hornet Launcher (L10) plays its `missile` family since M5 part A | P1 |
| Hammer Mortar / bomb drop | Hollow thunk, whistle down | P2 |
| Lance Laser | Sustained zap with tail | P2 |
| Ion Beam loop | Humming loop with start/stop | P2 |
| Harpoon torpedo / depth charge | Muffled launch, bubbles; underwater boom; the Torpedo Pod (L11) — M5 part E (the chosen `torpedo` launch, drier above water; its impact the `water` rung), Harpoon Torpedoes and Depth Charges Act 4 | P2 |
| Plasma Arc | Crackle | P3 |
| Tail Gun / Fan Blaster | Like the front guns, a little lower (rear guns play their family 10 % lower, built with the Side Splitter); Tail Gun (L08) and Fan Blaster (L10), built in M5 part A | P1 |
| Proximity mine drop / arm | Click + beep; Proximity Mines (L12): the drop plays the `mine` family at its own pitch (M5 part A); the arming beep as a mine arms: chosen [a](concept/weapon-mine-arm-r28-a.ogg) "armed chirp" in [round 28](../../concept-rounds/round-28/README.md) (b "sensor ping" rejected) | P2 |
| Choir Resonator | Alien chord shot | P3 |
| Overdrive start / end | Power-up surge / power-down | P1 |

### Impacts and explosions

| Sound | Notes | Priority |
|---|---|---|
| Hit: metal | Tick/clank for Ascendancy and machines — [a](concept/hit-metal-r08-a.ogg), [b](concept/hit-metal-r08-b.ogg) | P1 |
| Hit: organic | Wet, chitinous crunch for the Vrell — [a](concept/hit-organic-r08-a.ogg), [b](concept/hit-organic-r08-b.ogg) | P1 |
| Hit: enemy shield | Glassy ping | P2 |
| Explosion small | Popcorn enemies; 3 variants | P1 |
| Explosion medium | 3 variants | P1 |
| Explosion large | Heavy enemies, buildings; with debris tail | P1 |
| Explosion boss | Long, multi-stage, with sub-bass | P2 |
| Explosion underwater | Muffled variants for the Europa act | P2 |
| Ground target destroyed | Crumbling structure — [a](concept/hit-crumble-r08-a.ogg), [b](concept/hit-crumble-r08-b.ogg) | P2 |

### Player ship

| Sound | Notes | Priority |
|---|---|---|
| Shield hit | Electric fizz — [a](concept/player-shield-hit-r08-a.ogg) | P1 |
| Shield break | Descending zap + alarm blip — [a](concept/player-shield-break-r08-a.ogg) | P1 |
| Shield restored | Rising chime — [a](concept/player-shield-restore-r08-a.ogg) | P2 |
| Armour hit | Metallic crunch — [a](concept/player-armour-hit-r08-a.ogg) | P1 |
| Low armour warning | Beeps (slow at 30 %, fast at 15 %) — [a](concept/player-low-armour-r08-a.ogg) | P1 |
| Ship destroyed | Big explosion, then the music sting — [a](concept/player-destroyed-r08-a.ogg) | P1 |
| Dash | Thruster burst | P3 |
| Engine hum | Subtle loop (optional) | P3 |
| Launch rail | The catapult run at a level start (Level 01 section 1): a real carrier launch under an afterburner to the stop at 3.55 s (a coupler's steel clank over a rail car's slam) and a steam release, 4.95 s — chosen [b](concept/launch-rail-r47-b.ogg) ([round 47](../../concept-rounds/round-47/README.md), L.5; in the game from the Freesound preview cut until its originals are fetched); round 11's synthesized [b](concept/launch-rail-r11-b.ogg) before it | P2 |

### Pickups

| Sound | Priority |
|---|---|
| Salvage small / medium / large (rising pitch) — small [a](concept/pickup-salvage-small-r08-a.ogg), medium [pickup-r01-c](concept/pickup-r01-c.ogg), large [a](concept/pickup-salvage-large-r08-a.ogg) | P1 |
| Shield cell — [a](concept/pickup-shield-cell-r08-a.ogg) | P1 |
| Armour patch — [a](concept/pickup-armour-patch-r08-a.ogg) | P2 |
| Special charge — [a](concept/pickup-special-charge-r08-a.ogg) | P2 |
| Overdrive pickup — [pickup-r01-a](concept/pickup-r01-a.ogg); start/end cues [a](concept/overdrive-start-r08-a.ogg), [a](concept/overdrive-end-r08-a.ogg) | P1 |
| Data core (distinct, rewarding) — [pickup-r01-b](concept/pickup-r01-b.ogg) | P2 |
| Weather sonde collected (Level 12, planned, M5 part F): a short bright chime as the ship flies through a sonde, on `SONDE_COLLECTED`; one proposal in round 34 | P1 |

### Specials

| Sound | Priority |
|---|---|
| Airstrike: radio call (radio squelch + text), jets flyby [a](concept/special-airstrike-jets-r08-a.ogg), bomb carpet [a](concept/special-airstrike-bombs-r08-a.ogg) | P1 |
| Smart bomb: charge-up + white-out boom — [a](concept/special-smartbomb-r08-a.ogg) | P1 |
| EMP: electric thump + power-down whine | P2 |
| Decoy flares — [a](concept/special-flares-r08-a.ogg) | P2 |
| Orbital lance: charge + sustained beam | P3 |
| Sonar pulse ping — [a](concept/special-sonar-r04-a.ogg) (clean single ping); round 04 | P2 |
| Shield overcharge | P3 |
| Time dilation: slow-down / speed-up sweeps | P3 |
| Special unavailable (denied buzz) — [a](concept/special-denied-r08-a.ogg) | P1 |

### Enemies

| Sound | Priority |
|---|---|
| Enemy shot: small [a](concept/enemy-shot-small-r08-a.ogg), [b](concept/enemy-shot-small-r08-b.ogg) / heavy [a](concept/enemy-shot-heavy-r08-a.ogg), [b](concept/enemy-shot-heavy-r08-b.ogg) / laser charge warning [a](concept/enemy-laser-warning-r08-a.ogg) | P1 |
| Missile launch (enemy) — [a](concept/enemy-missile-r08-a.ogg) | P1 |
| Vrell screech, 2 variants: [c](concept/enemy-screech-r08-c.ogg), [d](concept/enemy-screech-r08-d.ogg), in turn — a large Vrell unit entering the screen (its hit box first over the play field): the Mantis, the Coilwyrm's head (not its body or a regrown head), the Spore Bomber, the Scuttler and, from Level 08, the [Creeper](../../enemies/ground/creeper/README.md) (M5 part B); once per unit, at most one screech every 3 s (a unit entering inside them stays silent), at the Vrell spawns' level (user decision 2026-10-06) | P2 |
| Turret rotate / lock-on beep — [a](concept/enemy-lock-r08-a.ogg) | P2 |
| Portal / warp-in; Vrell spawn (Brood Pod bursting, Hive Node and Brood Carrier spawns) — [a](concept/enemy-spawn-r08-a.ogg) (wet creature swell), [b](concept/enemy-spawn-r08-b.ogg) (fleshy burst) | P2 |
| Carrier launching drones — the Brood Carrier's units leaving its sacs: chosen [b](concept/enemy-carrier-launch-r25-b.ogg) (slime lunge; a creature spit was rejected), [round 25](../../concept-rounds/round-25/README.md) | P2 |
| Boss roars and phase-change cues (per boss) — the Brood Carrier (Level 07): roar as it arrives and, lower, as it turns broadside, chosen [b](concept/enemy-carrier-roar-r25-b.ogg) (bear and didgeridoo; a deep roar with echo was rejected); a bay sac opening, chosen [b](concept/enemy-carrier-sac-open-r25-b.ogg), and closing, chosen [b](concept/enemy-carrier-sac-close-r25-b.ogg) (a tear out of sucking mud and its reverse; flesh pulled apart was rejected); a sac bursting, chosen [a](concept/enemy-carrier-sac-burst-r25-a.ogg) (very wet, fleshy explosion, CC-BY; a visceral tear was rejected); the plate iris opening, chosen [b](concept/enemy-carrier-iris-r25-b.ogg) (organic morph; an alien hatch was rejected), [round 25](../../concept-rounds/round-25/README.md); other bosses P3 | P2 |
| Lifeboat tow (Level 07 secret): the amber cable snapping on its third hit — chosen [a](concept/secret-cable-snap-r25-a.ogg) (chain snap, CC-BY; a slowed string twang was rejected), [round 25](../../concept-rounds/round-25/README.md); the hits play the metal hit, the crate the large salvage | P2 |
| Hive Node (Level 09, M5 part C): the iris opening reuses the Brood Carrier's chosen iris [b](concept/enemy-carrier-iris-r25-b.ogg) (organic morph), the release the Vrell spawn [a](concept/enemy-spawn-r08-a.ogg), its death the `large` rung; no screech (a static mound: its iris is its cue) | P2 |
| Ravager pounce (Level 09, M5 part C): a short snarl and lunge as it takes off — chosen [a](concept/ravager-pounce-r31-a.ogg) and [b](concept/ravager-pounce-r31-b.ogg) (both kept: the game picks one at random per pounce), [round 31](../../concept-rounds/round-31/README.md); its death the `small` rung; no screech (a pack would screech on top of itself; the pounce is its cue) | P2 |
| Wraith decloak (Level 10, M5 part D): a short violet shimmer-crack as its 0.4 s flash starts — chosen [b](concept/wraith-decloak-r32-b.ogg) (metallic sci-fi rip over a membrane swish, CC-BY; a glassy ice crack over a synthesized shimmer was rejected), [round 32](../../concept-rounds/round-32/README.md); its death the `medium` rung; no screech (the decloak is its cue) | P2 |
| Mote Swarm whoosh (Level 10, M5 part D): a rushing flutter of many small wings as a swarm enters and again as it loops back — chosen [b](concept/mote-swarm-r32-b.ogg) (insect chitter over a whoosh; a birdlike rush of pigeon wings was rejected), [round 32](../../concept-rounds/round-32/README.md); a mote's death the `tiny` rung | P2 |
| Lance strike (Level 10's scripted loss, M5 part D): a thin descending whine into a sharp impact as the lance hits Lifeline Three — chosen [a](concept/lance-r32-a.ogg) (alien falling whine into a thunder crack and its roll, the impact 1.2 s in, so it starts at t 116.8; a whistle into a piercing stab was rejected), [round 32](../../concept-rounds/round-32/README.md); the shuttle's loss reuses the ally loss sound | P2 |
| Harbour Kraken (Level 11, M5 part E), one proposal each in [round 33](../../concept-rounds/round-33/README.md), all accepted (chosen; recordings with synthesized layers, the churn's waves CC-BY, byjoshberry, on the credits roll): the lane telegraph's churn [a](concept/kraken-churn-r33-a.ogg) (a rushing surge of waves over a rumble swelling over the 1.0 s telegraph, reworked without bubbles; hard overruns its 0.8 s telegraph by 0.2 s), the slam [a](concept/kraken-slam-r33-a.ogg) (the arm's rush, then a heavy wet impact and big splash at 0.5 s into the file), the head's surfacing swell [a](concept/kraken-surface-r33-a.ogg) (a deep swell, water streaming off as the crown breaks, with each `SURFACE`), its death [a](concept/kraken-death-r33-a.ogg) (a long wet groan over churning water, reworked without bubbles, under the `huge` water burst) | P2 |
| Convoy ships (Level 11, M5 part E), chosen in [round 33](../../concept-rounds/round-33/README.md) as the Kraken's: a ship hit [a](concept/ship-hit-r33-a.ogg) (metal struck, water rushing in; on `ALLY_HIT`), a ship sinking [a](concept/ship-sink-r33-a.ogg) (a steel groan, a muffled boom, waves washing over; 3.8 s, reworked without bubbles), the frigate's distant flak [a](concept/frigate-flak-r33-a.ogg) (two dull pops far away, raised twice to −16 dB after listening, one file per `ALLY_FLAK`) | P2 |
| Driftjelly (Level 11, M5 part E): its ring's pulse chosen in [round 33](../../concept-rounds/round-33/README.md), [a](concept/driftjelly-pulse-r33-a.ogg) (a soft synthesized underwater bloop and ripple, no source; kept over reusing the enemy shot's small sound); its death the `small` rung with the `water` explosion | P3 |
| Lamprey (Level 12, planned, M5 part F): the **latch**, a wet suck-and-bite as it latches (`LAMPREY_LATCHED`); the **drain**, a quiet looping hum-and-crackle while any Lamprey drains (stopping at the break or the shake-off); the **shake-off**, a slap and a squeal as they are thrown off (`LAMPREY_SHAKEN`); its death the `small` rung (a wet pop). Latch and drain a/b in round 34 (user decision F8 = a), the shake-off one proposal; the user picked a for both (2026-10-12) | P1 |
| Weather (Level 12, planned, M5 part F): **thunder** one-shots, 2–3 variants, 0.3–1.5 s after each lightning flash (`LIGHTNING`; the delay and variant fixed by the flash's index), F6 = a; a **gust** whoosh rising with each gust's telegraph. Thunder and gust a/b in round 34 (F8 = a); the user picked **b** for both (2026-10-12: the close strikes, the recorded gust); CC0 or CC-BY recordings or synthesis | P2 |
| Nansen Relay (Level 13, M5 part G; G9 = a; the user's picks of 2026-10-12: grid online **b**, every other **a**, `Sfx.RELAY_HIT`, `INTEGRITY_ALARM`, `RELAY_DESTROYED`, `DRONE_LAUNCH`, `GRID_ONLINE`): the relay's **hit** (a short metallic thud with a zap, on the relay-aimed hit event; relay hit [a](concept/relay-hit-r35-a.ogg) / [b](concept/rejected/relay-hit-r35-b.ogg)), the **integrity alarm** (a two-tone alarm once at 50 % and once at 25 %, on `ALLY_BELOW`; [a](concept/integrity-alarm-r35-a.ogg) / [b](concept/rejected/integrity-alarm-r35-b.ogg)), **relay destroyed** (a power-down and rumble, [a](concept/relay-destroyed-r35-a.ogg), one version), the **drone launch** (rotors spinning up, [a](concept/drone-launch-r35-a.ogg), one version) and **grid online** (the lights coming on in sequence at t=218, a power-up chain; [a](concept/rejected/grid-online-r35-a.ogg) / [b](concept/grid-online-r35-b.ogg)); the Skimmer's flip-and-sink reuses the water explosion | P2 |
| Siege Spire (Level 14, M5 part H; H8 = a; the user's picks of 2026-10-12: the tear-free roar and the canopy withering **b**, the root burst **a**): the **canopy hum**, a seamless loop [a](concept/canopy-hum-r36-a.ogg) while the canopy lives (cut at the Spire's death, no hum in the daylight), the **root burst** crack as the paving breaks (the telegraph's end; [a](concept/root-burst-r36-a.ogg) / [b](concept/rejected/root-burst-r36-b.ogg)), the **growth** swell [a](concept/root-growth-r36-a.ogg) under a growing turret or mortar (one version), the **tear-free roar** (phase 3's lift-off; [a](concept/rejected/tear-free-roar-r36-a.ogg) / [b](concept/tear-free-roar-r36-b.ogg)) and the **canopy withering** after the Spire's death ([a](concept/rejected/canopy-withering-r36-a.ogg) / [b](concept/canopy-withering-r36-b.ogg)); the sac, the lob and the sweep reuse the existing sounds | P2 |
| Arcology collapse (Level 09, M5 part C): a low rumble under the 1.5 s lean and the 1.5 s drop swelling into a long crash at the impact (about 7 s) — chosen [a](concept/collapse-r31-a.ogg) (stony rumble swelling into a tumbling masonry crash, CC-BY; a steel-frame cave-in was rejected; the production file crashes at 3.0 s, the concept file at 1.5 s), [round 31](../../concept-rounds/round-31/README.md) | P2 |
| Leviathan whale-song cry (its death, under the large burst) — chosen [b](concept/enemy-leviathan-cry-r47-b.ogg) (a real humpback's moan slowed to 0.8, 4 s; round 47, L.2; in the game from the Freesound preview cut until its originals are fetched); round 16's synthesized [a](concept/enemy-leviathan-cry-r16-a.ogg) before it | P2 |
| Polyp Mortar lob — chosen [a](concept/enemy-mortar-lob-r21-a.ogg) (mortar thump; an organic spit was rejected); impact — chosen [a](concept/enemy-mortar-impact-r21-a.ogg) (wet splat; an acid sizzle was rejected), [round 21](../../concept-rounds/round-21/README.md) | P2 |
| Mass-driver sled (Level 05 hazard): whine while the rail lights chase — chosen [a](concept/hazard-sled-whine-r21-a.ogg) (charge hum loop, played twice over the 1.5 s chase; a rising charge was rejected); pass — chosen [b](concept/hazard-sled-pass-r21-b.ogg) (rushing flyby; a rail-gun crack was rejected), [round 21](../../concept-rounds/round-21/README.md) | P2 |
| Mantis (Level 06): telegraph, the 0.6 s arc — chosen [a](concept/enemy-mantis-telegraph-r23-a.ogg) (laser charge-up; a second charge was rejected); beam sweep, 1.3 s — chosen [b](concept/enemy-mantis-sweep-r23-b.ogg) (death ray with crackle, CC-BY; a game-style beam was rejected), [round 23](../../concept-rounds/round-23/README.md) | P2 |
| Coilwyrm chain-cut tear (Level 06): the body torn in two as a cut segment's rear part starts to regrow, over the segment's burst — chosen [a](concept/enemy-coilwyrm-cut-r27-a.ogg) (a run of wet flesh rips, 15 % slower; one juicy limb-tearing rip with a low body was rejected), [round 27](../../concept-rounds/round-27/README.md) | P2 |
| Coilwyrm head regrowth (Level 06), 0.6 s — chosen [b](concept/enemy-coilwyrm-regrow-r23-b.ogg) (insect growl and chitter; a wet slime was rejected), [round 23](../../concept-rounds/round-23/README.md) | P2 |
| Coilwyrm death (Level 06): the head's deeper burst — chosen [c](concept/enemy-coilwyrm-head-burst-r24-c.ogg) (messy splatter, slowed over a sub thump, CC-BY), then a segment's or the tail's wet burst, 13 times down the chain 0.25 s apart — chosen [b](concept/enemy-coilwyrm-burst-r24-b.ogg) (fleshy burst; a wet gib crack, the messy splatter and a synthesized pop were rejected); [preview](concept/enemy-coilwyrm-death-final-r24-a.ogg), [round 24](../../concept-rounds/round-24/README.md) | P2 |
| Perimeter beacon flares (Level 06 hazard): launch — chosen [a](concept/hazard-flare-launch-r23-a.ogg) (flare-gun shot, CC-BY; a firework ignition was rejected); burn loop while it falls — chosen [a](concept/hazard-flare-burn-r23-a.ogg) (road flare, the 3 s loop played back to back while a flare burns; a second road flare was rejected), [round 23](../../concept-rounds/round-23/README.md) | P2 |

### UI and radio

| Sound | Priority |
|---|---|
| Menu move / confirm / back — [a](concept/ui-menu-move-r08-a.ogg) / [a](concept/ui-menu-confirm-r08-a.ogg) / [a](concept/ui-menu-back-r08-a.ogg) | P1 |
| Buy / sell / equip / upgrade / can't afford / won't fit (power) — buy [a](concept/ui-shop-buy-r08-a.ogg), sell [a](concept/ui-shop-sell-r08-a.ogg), can't afford / won't fit [a](concept/ui-shop-denied-r08-a.ogg), equip [a](concept/ui-equip-r08-a.ogg), upgrade [a](concept/ui-upgrade-r08-a.ogg) | P1 |
| Save done: the hangar's autosave as it opens and a save to a slot (a failed save plays the back blip) — chosen [b](concept/ui-save-r27-b.ogg) (a latch click, two warm bells over a pad; data ticks landing on a bright fifth and a bell were rejected), [round 27](../../concept-rounds/round-27/README.md) | P2 |
| Typewriter blip (briefing text) — [a](concept/ui-typewriter-r08-a.ogg) | P1 |
| Radio squelch open / close — [a](concept/ui-radio-open-r08-a.ogg) / [a](concept/ui-radio-close-r08-a.ogg) | P1 |
| Warning klaxon (boss, rear attack) — [a](concept/ui-klaxon-r08-a.ogg) (seamless loop; a single blast can be cut from it) | P1 |
| Edge warning tone (side or rear wave, with the edge arrows) — chosen [b](concept/ui-edge-warning-r11-b.ogg) (contact ping; a triple chirp was rejected), [round 11](../../concept-rounds/round-11/README.md) | P1 |
| Debrief tally tick / grade stamp — tick [a](concept/ui-tally-tick-r08-a.ogg), total [a](concept/ui-tally-total-r08-a.ogg), grade stamp [a](concept/ui-grade-stamp-r08-a.ogg) | P2 |

### Ambience (per setting)

Space hum, orbital station creaks, city wind and sirens, Martian dust wind, underwater drone and
whale-like calls under Europa's ice, asteroid rumble, Jovian storm, alien pulsing beyond the
gate. P2–P3, one loop per setting in the [world](../../world/README.md).

| Setting | Key (a level's `music.ambience`) | Loop (round 08) |
|---|---|---|
| Earth orbit | `earth-orbit` | [a](concept/ambience-orbit-r08-a.ogg) — space drone, 16 s |
| Luna | `luna` | [a](concept/ambience-luna-r08-a.ogg) — desolate space-wind drone, 16 s |
| Earth megacity | `earth-megacity` (Level 08, M5 part B) | [a](concept/ambience-city-r08-a.ogg) — night city with distant sirens, 20 s; played as it is (M5 part B default) |
| Earth ocean | `earth-ocean` (Level 10 from section 4, crossfading in over 4 s from the megacity, M5 part D; Level 11 from t=0 throughout, M5 part E) | [a](concept/ambience-ocean-r08-a.ogg) — waves at speed, 16 s |
| Earth ocean storm | `earth-storm` (M5 part F: Level 12 in sections 1–4 and 6, crossfading to `earth-ocean` in the eye and back over 4 s; user decision F6 = a: the game plays round 34's **rain-only bed** [ambience-storm-rain-r34-a](concept/ambience-storm-rain-r34-a.ogg), 10 s, cut from the same CC0 source, until the round closes; the chosen a kept) | [a](concept/ambience-storm-r08-a.ogg) — rain and thunder, 24 s |
| Earth arctic | `earth-arctic` (M5 part G: `Sfx.AMBIENCE_ARCTIC` under all of Level 13, +3 dB in the whiteout peak, ramping with it) | [a](concept/ambience-arctic-r08-a.ogg) — cold wind, 16 s |
| Earth megacity under the canopy | `earth-canopy` (M5 part H: `Sfx.AMBIENCE_CANOPY` under Level 14 instead of the megacity's sirens; at the Siege Spire's death it fades out over 2 s while `earth-megacity` rises over 7 s under the canopy's withering, pending the user's review) | [a](concept/canopy-hum-r36-a.ogg) — the canopy's hum, round 36's one proposal, a seamless 8 s loop |
| Mars entry | `mars-entry` (Act 3: Level 15 in sections 1–2, the descent through the thin air; crossfading to `mars-dust` over 2 s at section 3; M7 part B: `Sfx.AMBIENCE_MARS_ENTRY`) | the [entry roar](#act-3-sounds-approved-round-36) of round 38: the chosen [a](concept/entry-roar-r38-a.ogg) "recorded roar", a seamless 12 s loop (S.3 = a, 2026-10-14) |
| Mars dust wind | `mars-dust` (Act 3: Level 15 from section 3, Levels 20 and 21; M7 part B: `Sfx.AMBIENCE_MARS_DUST`) | the chosen [a](concept/mars-dust-r38-a.ogg) "recorded dust wind" of round 38, a seamless 16 s loop (S.4 = a, 2026-10-14) |
| Mars canyon | `mars-canyon` (Act 3: Level 16, the dust wind with a canyon howl; M7 part C) | the chosen [a](concept/mars-canyon-r39-a.ogg) of round 39, a synthesized dust wind with a resonant canyon howl, a seamless 16 s loop (S.5, 2026-10-18) |
| Mars colony | `mars-colony` (Act 3: Level 17, the plains wind, the domes' air-plant hum, a distant rail clank) | round 40, accepted: [a](concept/mars-colony-r40-a.ogg) — synthesized plains wind, air-plant hum (60 Hz partials) and a distant rail clank, seamless 16 s loop (accepted in round 40, S.l) |
| Mars dust storm | `mars-storm` (Act 3: Level 18 from t=25, swelling in the storm walls, +3 dB as Level 13's whiteout; in the eye a crossfade to `mars-dust` and back over 4 s, as Level 12's eye) | [a](concept/dust-storm-bed-r36-a.ogg) — "recorded front", a sandstorm over a furious low howl, seamless 16 s loop (round 36, S.5 = a) |
| Mars foundry | `mars-foundry` (Act 3: Level 19, conveyor rumble, a far furnace roar, a slow hammer, the plateau wind) | round 42 (M7 part F): [a](concept/mars-foundry-r42-a.ogg) — synthesized conveyor rumble, far furnace roar, a slow distant hammer every 4 s with a hall echo and the plateau wind, seamless 16 s loop, pending the user's review |
| Europa ice | `europa-ice` (Act 4, draft: Level 22, Level 23's shaft mouth, Level 28 after the breakout) | round 48: not made yet (Freesound quota): a cold wind over open ice with deep ice groans rolling under it |
| Europa under the ice | `europa-under` (Act 4, draft: Level 23 from the waterline, Level 24) | round 48: not made yet: muffled deep water, a heavy pressure rumble, hull creaks, far whale-like calls; no bubbles |
| Europa trench | `europa-trench` (Act 4, draft: Level 25, Level 26's scarp) | round 48: not made yet: the under-water bed lower and closer, far rock grinding |
| Europa black water | `europa-black` (Act 4, draft: Level 26) | round 48: not made yet: a heavy pressure drone in total dark, very sparse far calls |
| Europa vents | `europa-vents` (Act 4, draft: Level 27; a caldera variant for Level 28's arena) | round 48: not made yet: a deep roaring vent rumble with slow surges and a far venting hiss |

### Act 3 sounds (approved, round 36)

**Status: approved in concept round 36 (user, 2026-10-13, D.29).** The new sounds Levels 15–21 and the Act 3 arsenal need, from the level, unit and
equipment documents (which link here for them). The rules are the act's: CC0 or CC-BY recordings
with synthesized layers where a recording lacks something, levelled on the 200 Hz–5 kHz band, and
**no bubbly, popping or squelching sounds** (user rule): sand, rock, wind, metal, fire and glass,
not liquid. Round 36 picked four of them (the burrow eruption a, the canyon wall scrape b, the Ion
Beam loop a and the dust-storm bed a); every other one is proposed a/b in the round of the part
that builds it (M7 parts A–H), as Act 2 did. A sound that reuses a chosen file says so.

**Terrain, hazards and set pieces**

| Sound | Trigger and use | Brief | Proposed in | Priority |
|---|---|---|---|---|
| Canyon wall scrape | The ship (or Rook) touches a canyon wall or rock island (L16 on; M1 = a: damage, push-back, 0.5 s cooldown): once per contact | A **metal screech** with a spark hiss (the chosen b, [canyon-scrape-r36-b](concept/canyon-scrape-r36-b.ogg), 0.6 s; S.3 = b over a's rock grind): a hard onset and a rough tail; reads as damage (played with the armour or shield hit, a little under it), panned to the touching side | round 36: b | P1 |
| Shots on rock | A bolt or bullet stopped by a wall | Reuse: a quiet dull rock tick, the ground hit at −6 dB; the beam's rock scorch is the Ion Beam's impact below | — | P2 |
| Entry roar | Level 15 sections 1–2, the descent through the thin air (a loop) | A high thin wind roar over the hull, rising as the air thickens, crossfading into `mars-dust` at section 3 | round 38: a, chosen ([entry-roar-r38-a](concept/entry-roar-r38-a.ogg), a seamless 12 s loop; the rise is the game's level ramp) | P2 |
| Relay hut door | Level 16's secret: the hut opened | Reuse the crate's metal burst; no new file unless the round asks | — | P3 |
| Dome hit | A dome of Level 17 takes a hit (`ALLY_HIT`) | A hard hammer strike on thick glass and the big dome answering: a long low glassy ring in a cathedral-sized space; no shatter; 1.2 s | round 49 (M.1): [b](concept/dome-hit-r49-b.ogg), the hammer strike over the glass ring in the cathedral reverb, 1.2 s (in the game from the Freesound preview cut until its originals are fetched; round 40's [a](concept/dome-hit-r40-a.ogg) before it) | P1 |
| Dome break | A dome breaks (`ALLY_LOST`) | A cave-in: a low rumble under the structure coming down, the panes smashing in it; about 2.5 s, under the ally-loss sound | round 41 (S.h): **c**, chosen ([dome-break-r40-c](concept/dome-break-r40-c.ogg): a low-passed building collapse over a deep crust rumble with smashing glass, 2.4 s, CC0 Freesound originals); rejected: round 40's synthesized a (user: "doesn't sound as a cave in, needs a rumble") and the redo b (a structure collapse with a sub thump, shorter under the glass) | P1 |
| Distant flak | Level 17's militia guns (t≈160) and Level 20's rim guns | Reuse the frigate's chosen distant flak ([a](concept/frigate-flak-r33-a.ogg)) | — | P2 |
| Conveyor belts | Level 19, the belts under the tanks (a loop while belts are on screen) | Folded into the `mars-foundry` bed rather than a sound per belt | round 42 (M7 part F): a belt loop all the same, for the game step to use or drop: [a](concept/belt-rumble-r42-a.ogg) (a roller rumble, plate seams knocking 6 a second and a motor hum, seamless 2.4 s loop) | P3 |
| Furnace vent | Level 19's vents: the tell (the glow) and the burst | Tell: a rising gas roar, 1.0 s; burst: a fire blast roaring out and dying, 1.5 s; no pop at the start | round 42 (M7 part F): tell [a](concept/rejected/vent-tell-r42-a.ogg) (a rising gas roar over a deepening rumble, 1.05 s), burst [a](concept/rejected/vent-burst-r42-a.ogg) (a fire blast with a 40 ms attack, roaring out and dying, 1.5 s), both redone from the Freesound database in round 43 (the user: "Same thing", too small) and replaced by tell [a](concept/vent-tell-r43-a.ogg) (a balloon burner's roar swelling in, 1.05 s) and burst [a](concept/vent-burst-r43-a.ogg) (a deep flame burst over a gas burner's burn, 1.6 s), chosen | P2 |
| Basin rumble | Level 20 from t=12 (the ridge), Level 21 from the ridge pass (t≈64) into the fight | A deep earth groan with grinding under it, a loop that can swell; Level 21's cuts at the kill | part G | P2 |
| Ridge pass | Level 21, the Colossus's back moving under the sand past the ship (t≈64–72) | A travelling rumble panned across the field with sand hissing off the ridge, 6–8 s | round 44 (M7 part H, from the Freesound database): [a](concept/colossus-ridge-r44-a.ogg) (a subway train passing under a bridge, sand flowing, panned left to right, 7.0 s), [b](concept/rejected/colossus-ridge-r44-b.ogg) (a large low rumble passing with a stone grind and sand pouring, panned right to left, 7.0 s), **chosen: a** (round 44, S.f: the user's pick, played with its channels swapped for Level 21's right-to-left ridge); a one-shot over the `basin-rumble` bed, which stays | P2 |

**Enemies** (each unit's death plays its size rung; the Vrell screech rule covers a new large Vrell
unit as it enters: the Shellback; the human-built units never screech)

| Sound | Trigger and use | Brief | Proposed in | Priority |
|---|---|---|---|---|
| Burrower bulge | The 1.0 s bulge tell (`burrow`) | A low rumble and sand cracking, swelling to the surfacing; the cue the player hunts by ear | round 38: b, chosen: [a crack layer](concept/burrow-bulge-r38-b.ogg) played over the eruption a, both from the bulge's start ([as mixed](concept/burrow-bulge-mix-r38-b.ogg); M7 part B: `Sfx.BURROW_BULGE`) | P1 |
| Burrower eruption | It surfaces | Sand and grit bursting upward, a short heavy whoomph with a rock-crack; dry, no plop. The chosen a "sand burst" ([burrow-eruption-r36-a](concept/burrow-eruption-r36-a.ogg), 2.1 s: the tell's rumble swelling over 1.0 s, then the impact and sand raining down; S.2 = a); M7 part B: `Sfx.BURROW_ERUPTION`, played at the bulge's start | round 36: a | P1 |
| Burrower dive | It dives (0.4 s) | A falling sand hiss, short (M7 part B: `Sfx.BURROW_DIVE`) | round 38: b "grain slide", chosen ([burrow-dive-r38-b](concept/burrow-dive-r38-b.ogg)) | P2 |
| Dust Kite | — | None of its own (popcorn): the `tiny` rung; a quiet membrane flutter only if the round finds them silent | — | P3 |
| Tendril hum | A tendril barrier live (a loop; the nearest one plays) | A taut electric-organic drone, rising with the 1.0 s dormant tell into a flare; no wobble that could read as bubbling | round 39 (M7 part C): a, chosen ([tendril-hum-r39-a](concept/tendril-hum-r39-a.ogg), a steady 120 Hz harmonic drone, seamless 6 s loop; S.1) | P1 |
| Tendril burn and break | A `beam` on a tendril (a loop while it burns); the tendril or an anchor dies | Burn: a searing crackle over the beam; break: a whip-crack snap and a falling whine as the line goes slack | round 39, chosen (S.2, S.3): burn [a](concept/tendril-burn-r39-a.ogg), a crackle of noise grains over a sizzle, seamless 2 s loop; break [a](concept/tendril-break-r39-a.ogg), a snap and a falling harmonic whine, 0.9 s | P2 |
| Threadcrawler | Its walk (a loop while on screen) and the pore wave | Walk: a dry many-legged skitter on rock; the pores fire as the enemy small shot, head to tail; the chain's death as the Coilwyrm's bursts, smaller | round 39, chosen (S.4): walk [a](concept/crawler-walk-r39-a.ogg), twelve legs of noise ticks over a rock rustle, seamless 3 s loop (the pores and the death reuse existing sounds) | P2 |
| Choir Herald | Its song (with its `ring`), the aura (a loop while any ring is up), a ring absorbing a hit, the rings collapsing at its death | Song: a short sung phrase in the Choir's colour (not words); aura: a soft high shimmer; absorb: a glassy ping (the enemy-shield hit); collapse: a falling chord, as its death row says | song: round 47's [a](concept/herald-song-r47-a.ogg) (a choir's swelling chord slowed to 0.7 over throat singing, 2.4 s; in the game from the Freesound preview cut until its originals are fetched; round 40's [a](concept/herald-song-r40-a.ogg) before it); round 40: aura [a](concept/herald-aura-r40-a.ogg) (steady high partials, seamless 4 s loop), absorb [a](concept/aura-absorb-r40-a.ogg) (a glassy ping, 0.4 s), collapse [a](concept/aura-collapse-r40-a.ogg) (a falling chord, 2.0 s) | P1 |
| Shellback | Walk, mortar lob, curl, roll, uncurl | Walk: heavy padded footfalls with shell plates clacking; lob: the Polyp Mortar's chosen lob, lower; curl: plates grinding shut; roll: a rumbling grind that crushes (a loop, 2.5 s); impact: the mortar's chosen impact | round 40: walk [a](concept/shellback-walk-r40-a.ogg) (padded footfalls and clacking plates, seamless 3.2 s loop), lob [a](concept/shellback-lob-r40-a.ogg) (a dull cough and a pressure hiss, 0.6 s), curl [a](concept/shellback-curl-r40-a.ogg) (plates grinding shut, 0.8 s), roll [a](concept/shellback-roll-r40-a.ogg) (a rumbling rock-crushing grind, seamless 2.5 s loop), uncurl [a](concept/shellback-uncurl-r40-a.ogg) (0.7 s); its impact is the mortar's chosen impact | P2 |
| Dust Devil | Spin (a loop, the nearest one), the 0.4 s rise and opening, the grit spiral | Spin: a whirling wind roar with grit; opening: the vanes parting with a gust and a low hollow tone; the spiral as the enemy small shot with a grit hiss | round 41 (M7 part E): spin [a](concept/devil-spin-r41-a.ogg) (a whirling roar over a rumble and grit, seamless 3.0 s loop), opening [a](concept/devil-open-r41-a.ogg) (a gust, vane scrapes and a hollow tone, 0.7 s), spiral [a](concept/devil-spiral-r41-a.ogg) (a turning grit hiss, 0.45 s) | P1 |
| Ghost Drone | Its drop into the lane, the 0.3 s eye glint, its burst, its death | Drop: a clean turbine whine descending; glint: a short electronic lock tick; burst: three dry precise shots (human guns, not Vrell); death: a mechanical break-up, sparks and metal, on the `small` rung. It must sound built, not grown | round 41 (M7 part E): drop [a](concept/ghost-drone-drop-r41-a.ogg) (a clean turbine whine, 0.9 s), glint [a](concept/ghost-drone-glint-r41-a.ogg) (a lock tick, 0.15 s), burst [a](concept/ghost-drone-burst-r41-a.ogg) (three dry shots, 0.35 s), death [a](concept/ghost-drone-death-r41-a.ogg) (a dry bang, flung metal and sparks, 0.8 s; the `small` rung's Ghost Drone entry) | P1 |
| Warden Tank | Tracks (a loop while on screen, the nearest), turret slew, the 0.3 s hold and burst, death | Tracks: grinding treads and a diesel-turbine hum; slew: a servo whine; burst: a heavy autocannon; death: a hull explosion with metal debris | round 42 (M7 part F): tracks [a](concept/rejected/tank-tracks-r42-a.ogg) (grinding treads with track-link ticks, a diesel hum and a faint turbine whine, seamless 2.0 s loop), slew [a](concept/rejected/tank-turret-r42-a.ogg) (a servo whine with gear ticks, 0.6 s), burst [a](concept/rejected/tank-burst-r42-a.ogg) (a heavy autocannon, four rounds, 0.75 s), death [a](concept/tank-death-r42-a.ogg) (a hull explosion, a metal clang, debris and sparks, 1.8 s); round 43 redid the tracks, slew and burst from the Freesound database and the user chose tracks [a](concept/tank-tracks-r43-a.ogg) (a T-26 tank driving, seamless 4.0 s loop), slew [a](concept/tank-turret-r43-a.ogg) (a real turret re-aligning, 0.8 s) and burst [a](concept/tank-burst-r43-a.ogg) (a tank cannon, three shots, 1.18 s); the r42 tracks, slew and burst are rejected | P2 |
| Mote Swarm (pursuit) | Level 20's pursuing band (a loop) and each lunge | Band: the chosen swarm whoosh's insect chitter stretched into a hum ([b](concept/mote-swarm-r32-b.ogg), Level 10); lunge: a rising rush, 0.6 s | part G | P2 |
| Rear-guard and light drones | A drone breaking, a drone rebuilt | Break: a small metal crunch and spark (the metal hit and a `tiny` blast); rebuilt: a soft rising synth tone | part A | P2 |

**Bosses**

| Sound | Trigger and use | Brief | Proposed in | Priority |
|---|---|---|---|---|
| Revenant Walker | Its stride (each footfall), the pods' launch, the flamer's 0.6 s tell and cone, a vent hit, its death and the wreck's settle | Stride: a dull heavy stomp with a hydraulic hiss (no pop); pods: the chosen enemy missile launch ([a](concept/enemy-missile-r08-a.ogg), its first enemy use); tell: a gas hiss rising; flamer: a roaring flame loop; death: the `huge` rung with metal collapse; the wreck: metal settling and ticking as it cools, under the crawl | round 42 (M7 part F): stomp [a](concept/walker-stomp-r42-a.ogg) (a dull heavy stomp, a leg clank and a hydraulic hiss, 1.0 s), hatch tell [a](concept/rejected/walker-hatch-r42-a.ogg) (a latch tick, a pneumatic hiss and a hinge clack, 0.45 s), pod missile [a](concept/rejected/walker-missile-r42-a.ogg) (an ignition crack and a rocket whoosh sliding away, 0.8 s; an alternative to the planned enemy missile launch r08 a), flamer tell [a](concept/rejected/walker-flame-tell-r42-a.ogg) (a gas hiss rising, 0.7 s), flame [a](concept/rejected/walker-flame-r42-a.ogg) (a roaring, flickering flame, seamless 2.0 s loop); the vent hit, the death and the wreck's settle are not in this round; round 43 (Freesound database, the user's picks of 2026-10-20) replaced the hatch tell, flamer tell and flame and added the rest, all chosen: hatch [b](concept/walker-hatch-r43-b.ogg) (a submarine hatch's clank over a steam release, 1.01 s), flamer tell [a](concept/walker-flame-tell-r43-a.ogg) (a huge gas vent opening, 0.87 s), flame [b](concept/walker-flame-r43-b.ogg) (a flamethrower's deep roar, seamless 4.0 s loop), vent hit [a](concept/walker-vent-hit-r43-a.ogg) (a big metal hit on a lift door, 0.55 s), death [a](concept/walker-death-r43-a.ogg) (a large explosion with debris and a metal crash, 5.6 s, the `huge` rung) and wreck settle [a](concept/walker-wreck-settle-r43-a.ogg) (a bunker door's low groan over an engine cooling, 5.0 s); the r42 hatch, flamer tell, flame and the pod missile (the round 08 variant a is used) are rejected; round 44 (step WR2's rail lance and stomp shockwave, from the Freesound database): lance charge [a](concept/rejected/walker-lance-charge-r44-a.ogg)/[b](concept/walker-lance-charge-r44-b.ogg), lance shot [a](concept/walker-lance-shot-r44-a.ogg)/[b](concept/rejected/walker-lance-shot-r44-b.ogg), shockwave [a](concept/walker-shockwave-r44-a.ogg)/[b](concept/rejected/walker-shockwave-r44-b.ogg), **chosen** (round 44: lance charge **b**, S.g; lance shot **a**, S.h; shockwave **a**, S.i) | P1 |
| Dust Colossus | Each breach's 2.0 s bulge and its eruption, the suction (a loop while it pulls), the bite, a mouth's death, the death chain | Bulge: the Burrower's rumble, far bigger and longer; breach: a mountain of sand erupting with a roar; suction: a deep inhaling roar with sand streaming; bite: a heavy jaw snap with grinding chitin; death: a collapse into the sand, a long subsiding roar under the `huge` rung; dry throughout | round 44 (M7 part H, from the Freesound database): bulge [a](concept/colossus-bulge-r44-a.ogg)/[b](concept/rejected/colossus-bulge-r44-b.ogg), eruption [a](concept/colossus-eruption-r44-a.ogg)/[b](concept/rejected/colossus-eruption-r44-b.ogg), suction loop [a](concept/colossus-suction-r44-a.ogg)/[b](concept/rejected/colossus-suction-r44-b.ogg), bite [a](concept/colossus-bite-r44-a.ogg)/[b](concept/rejected/colossus-bite-r44-b.ogg), death [a](concept/colossus-death-r44-a.ogg)/[b](concept/rejected/colossus-death-r44-b.ogg), **chosen: a** for all six (round 44, S.a-S.e, S.f); no mouth's death sound (S.j: none needed) | P1 |

**Player equipment**

| Sound | Trigger and use | Brief | Proposed in | Priority |
|---|---|---|---|---|
| Ion Beam loop | The `beam` family (start → loop → stop), the Ion Beam | The round-04 loop's source made heavier: [ion-beam-loop-r36-a](concept/ion-beam-loop-r36-a.ogg) (2.6 s loop, the round-04 loop's source with an ion body; S.4 = a), which does not betray its length over a long hold, in place of the round-04 loop [a](concept/shot-beam-r04-a.ogg) | round 36: a | P1 |
| Beam impact | The beam on a target, a wall or a tendril (a layer while it touches) | A searing contact sizzle (the scorch), dry; on rock with a grit crackle | part A | P2 |
| Deflector Pod | A bullet caught; a charge returned | Tick: the shield hit pitched up and cut short; return: a soft rising tone, as its document says | part A | P2 |
| Light drone fire | — | The `pulse` family pitched up and quieter (its document) | — | P2 |
| EMP Burst | Its ring; a stunned machine | The chosen tesla shot ([a](concept/shot-tesla-r03-a.ogg)) layered and longer with an electric thump; a machine's power-down whine as its lights go out | part A | P2 |
| Orbital Lance | The call, the 0.6 s targeting line, the 3 s column | The call is voiced ([voice](../voice/README.md)); the targeting line plays the chosen lock beep ([a](concept/enemy-lock-r08-a.ogg)); the column the chosen loop [c](concept/shot-beam-r04-c.ogg) with a deep rumble under it | part A | P2 |
| Shield Overcharge | Activation; the end of its 8 s | Activation: a rising charge-up whoosh in the shield-restore chime's family; end: a soft falling shimmer | part A | P2 |
| Cooldown ready | A cooldown special's ring full (HUD) | A short soft ready chime, UI synth (the specials' rule) | part A | P2 |
| Dash | The Evasive thrusters' dash | The *Dash* row of *Player ship*: a short thruster burst in the engine's family | part A | P2 |

**Mixing** (proposal): a unit's loop (the tendril hum, the Dust Devil's spin, the Warden Tank's
tracks, the Shellback's roll) plays **once per kind**, for the nearest such unit and panned to it,
at the enemy-fire level; it starts and stops with a 0.2 s fade, is never stolen while its unit
lives (as an ambience) and counts as one voice. The dust storm changes nothing in the mix: the
bullets and their sounds stay as they are (M6 = a).

### Act 4 sounds (draft, round 48)

**Status: draft, decided in round 48 (2026-10-22)** (M8 design phase, step AUD48). The new sounds Levels
22–28 and the Act 4 arsenal need, from the [level](../../campaign/act-4-deep-water/README.md), unit, boss and
equipment documents (the level documents list them in their *Music & ambience*; Levels 27–28 are still being
drafted). The rules are Act 3's and the user's: real recordings from Freesound only (CC0 or CC-BY, in CREDITS.md),
**heavy and big**, levelled on the 200 Hz–5 kHz band like the accepted Act 3 sounds, and **no bubbly, popping or
squelching sounds**, a hard constraint in an under-water act. Round 48's **Q12** (proposed **a**): **no low-pass on
the sfx bus** (it would make every Act 3-style heavy sound small for six levels); instead heavy real under-water
recordings for the explosions and the creatures, the muffled ambience beds above (*Ambience (per setting)*), and no
bubble pop anywhere (the chosen torpedo launch's bubbles cut, the A theme's bubble layer dropped). Alternative b: the
bus low-pass as first drafted.

**Not made yet**: Freesound refused the first original download of round 48 on 2026-10-21 (HTTP 429, its daily
request limit of 500), so every sound here is a brief for the round; the a/b proposals follow after the quota resets
(the round page's sound rows name the candidate originals). A sound that reuses a chosen file says so.

**Explosions under water** (Q12 a): the explosion ladder's rungs (`tiny` to `huge`) get under-water variants for every
kill below the waterline (Levels 23–28): deep, heavy pressure booms with a muffled body and a long low tail, no splash
and no bubble rattle; the chosen [explosion-underwater-r04-a](concept/explosion-underwater-r04-a.ogg) is one candidate
for a rung. P1.

**Terrain, hazards and set pieces**

| Sound | Trigger and use | Brief | Proposed in | Priority |
|---|---|---|---|---|
| Ice crack | Level 22, a geyser's tell | A deep ice crack running and splitting, 0.8–1.0 s | round 48 (not made yet) | P1 |
| Geyser burst | Level 22, a geyser erupting | A roaring column of steam and slush tearing upward, 1.5 s; no hiss-pop at the start | round 48 (not made yet) | P1 |
| Conamara station hum | Level 22's end, the station | A big building's ventilation hum and a low transformer drone under the wind | round 48 (not made yet) | P3 |
| Waterline crossing | Level 23, the ship crossing the waterline | A heavy submersion swell: the air's roar cut into a deep muffled whoomph; no splash-plop | round 48 (not made yet) | P1 |
| Hydro-kit refit | Level 23, the kit locking for water | Heavy clamps and a hydraulic seal, 0.6 s | round 48 (not made yet) | P2 |
| Wall scrape under water | Levels 23 and 25, the ship on the shaft or trench wall (the canyon scrape's rule) | Metal grinding on ice or rock, muffled, a hard onset | round 48 (not made yet); else the chosen canyon scrape, muffled | P2 |
| Siphon pump | Level 23, the farm racks' pumps: a hum while one lives, its break | A deep pump throb; a heavy mechanical failure with a pressure release | round 48 (not made yet) | P2 |
| Reef iris and collapse | Level 24, a reef opening and dying | A slow grinding of chitin plates; a heavy crumbling of coral-like rock | round 48 (not made yet) | P2 |
| Beacon pulse | Level 24, the beacon line | A deep, far, clean sonar ping under the music | round 48 (not made yet); else the chosen Sonar Pulse ping, lower | P3 |
| Headlight click | Level 26, the lamps on at the start and out at t=8–18 | A heavy relay clunk | round 48 (not made yet) | P2 |
| Rook's lamp flicker and spin-down | Level 26, t≈160, Rook's loss | An electrical flicker crackle, then his engine winding down into silence | round 48 (not made yet) | P1 |
| Heat plume / vent eruption | Level 27 (its document in progress): the tell and the eruption | A deep rumble rising from below; a roaring column of superheated water, heavy | round 48 (not made yet) | P1 |
| Breakout | Level 28, the ice ceiling shattering after the kill | Ice cracking and tearing (calving), a huge crash, a roar of water and air into the open | round 48 (not made yet) | P1 |

**Enemies** (each unit's death plays its size rung, under water the variants above; the Vrell screech rule covers a new
large Vrell unit as it enters: the Abyss Ray; the human-built Depth Hunter never screeches)

| Sound | Trigger and use | Brief | Proposed in | Priority |
|---|---|---|---|---|
| [Drill Rig](../../enemies/ground/drill-rig/README.md) | Level 22: the collar boring (a loop), its core's death | A low heavy drill whine with a grinding bite; a structure failing, metal and ice | round 48 (not made yet) | P1 |
| [Eel Swarm](../../enemies/naval/eel-swarm/README.md) | The head's pulse shot; a segment dying | A dull pressure thump, short; a wet tearing slide, short; no squelch | round 48 (not made yet) | P2 |
| [Glassfin](../../enemies/naval/glassfin/README.md) | Its death (`tiny`) | A thin glass shatter, small and dry | round 48 (not made yet) | P2 |
| [Abyss Ray](../../enemies/naval/abyss-ray/README.md) | Its rising horn (the shadow's tell); its fan | A low, heavy, swelling horn-like call; the fan as the enemy medium shot | round 48 (not made yet) | P1 |
| [Barbfin](../../enemies/naval/barbfin/README.md) | Its needles | A short sharp spit (the Needler's role) | round 48 (not made yet); else the Needler's shot | P3 |
| [Siren](../../enemies/naval/siren/README.md) | The sac's 0.8 s tell and the ring; the slow on the ship | A soft, heavy, low sweep spreading out, sung rather than pinged; a falling tone with a hull shimmer | round 48 (not made yet) | P1 |
| [Spiral Nautilus](../../enemies/naval/spiral-nautilus/README.md) | The shell spin (a loop, the nearest), the uncoil, the spiral | A heavy rolling grind; a shell scraping open; the spiral as the enemy small shot | round 48 (not made yet) | P2 |
| [Glow Angler](../../enemies/naval/glow-angler/README.md) | The lure (a loop, the nearest), the lunge, its ring | A faint electric hum; a rushing surge and a heavy jaw snap (no click); the ring as the enemy small shot | round 48 (not made yet) | P1 |
| Driftjelly | Level 26 | Reuse its pulse of round 46 (the user's pick) | — | P3 |
| [Depth Hunter](../../enemies/naval/depth-hunter/README.md) | Its rise, the torpedo launch, the torpedo's run (Level 26's far trails too), its death | A ballast blow and a hull groan; a heavy pneumatic launch thump; a long distant rising hiss; a mechanical break-up, metal and sparks. It must sound built, not grown | round 48 (not made yet) | P1 |
| Scuttler on silt | Level 24 (returning unit) | Its walk, muffled; reuse with a low-pass unless the round asks for more | — | P3 |

**Bosses**: the [Abyssal Maw](../../enemies/bosses/abyssal-maw/README.md#sounds-brief) (arrival roar, gill breath,
throat charge, coil sweep, eel shed, dive, lunge, implant spark and burst, death) and the reveal's set piece (the
breakout above, the cruisers' rise, their broadside, flak and point defence: big naval gun volleys, airbursts, rapid
autocannon) follow the boss document's *Sounds brief*, one row each in round 48, not made yet. P1.

**Player equipment**

| Sound | Trigger and use | Brief | Proposed in | Priority |
|---|---|---|---|---|
| [Harpoon Torpedoes](../../player/weapons/harpoon-torpedoes/README.md) | Launch; the hit | The `torpedo` family made heavier and without the chosen launch's bubbles: a pneumatic tube thump and a motor running off; the hit on the under-water explosion set | round 48 (not made yet) | P1 |
| [Depth Charges](../../player/weapons/depth-charges/README.md) | Roll-out; the burst | A heavy barrel clank; a heavy, low under-water thud | round 48 (not made yet) | P1 |
| Sonar Pulse | The special (L22) | Reuse the chosen [special-sonar-r04-a](concept/special-sonar-r04-a.ogg) | — | P2 |
| Decoy Flares | The special (L27) | Reuse the chosen [special-flares-r08-a](concept/special-flares-r08-a.ogg) | — | P2 |
| Bulwark | The heavy drone (L22 on): a hit as it draws fire, its break and rebuild | Reuse the light drone's break and rebuild (round 46); a heavier hit only if the round asks | — | P3 |

**Voice-adjacent**: the Choir's whisper (Level 26) and Rook's cut-off are the voice pipeline's
([voice](../voice/README.md#act-4-voices-draft-round-48)); no sound file.

### Chosen sounds the game does not play

Every other chosen sound in the Concept art tables below is played by the game (an entry of
`vanguard.game.audio.Sfx`; `SfxFilesTest` checks both lists against this README). Besides them the
game may play only `proposed` sounds of an open concept round, provisionally until the user's choice
(round 34's and round 35's a, until those rounds close).

| File | Why not |
|---|---|
| [player-shot-r02-b.ogg](concept/player-shot-r02-b.ogg) | `pulse` family b ("heavy"): the family plays a, levels differ by pitch; kept for layering a heavy level |
| [player-shot-r02-c.ogg](concept/player-shot-r02-c.ogg) | `laser` family a: the Lance Laser plays b (r03) |
| [player-shot-r02-e.ogg](concept/player-shot-r02-e.ogg) | `vulcan` family a: the Scatter Vulcan plays b (r03) |
| [shot-tesla-r03-a.ogg](concept/shot-tesla-r03-a.ogg) | Tesla Coil Pod, Plasma Arc: later acts; the EMP Burst: Act 3 (M7 part A, draft) |
| [shot-resonator-r03-a.ogg](concept/shot-resonator-r03-a.ogg) | Choir Resonator: a later act |
| [shot-beam-r04-a.ogg](concept/shot-beam-r04-a.ogg) | Ion Beam loop: replaced by round 36's heavier loop a (S.4); kept as the source of that loop |
| [shot-beam-r04-b.ogg](concept/shot-beam-r04-b.ogg) | Alternative beam loop: a later act |
| [shot-beam-start-r04-a.ogg](concept/shot-beam-start-r04-a.ogg) | Beam start: round 37's shorter start a (its source at 1.8× speed; S.1) plays in its place (M7 part A); kept as that start's source |
| [shot-beam-stop-r04-a.ogg](concept/shot-beam-stop-r04-a.ogg) | Beam stop: round 37's stop b ("ion collapse", S.2) plays in its place (M7 part A); kept as the source of the rejected stop a |
| [burrow-bulge-mix-r38-b.ogg](concept/burrow-bulge-mix-r38-b.ogg) | Listening aid for round 38's S.1 = b: the eruption a and the bulge's crack layer b summed; the game plays the two files itself (`Sfx.BURROW_ERUPTION`, `Sfx.BURROW_BULGE`) |
| [special-sonar-r04-a.ogg](concept/special-sonar-r04-a.ogg) | Sonar Pulse (L22): Act 4 |
| [special-flares-r08-a.ogg](concept/special-flares-r08-a.ogg) | Decoy Flares (L27): Act 4 |
| [explosion-underwater-r04-a.ogg](concept/explosion-underwater-r04-a.ogg) | Under-water kills: Act 4 |
| [ambience-storm-r08-a.ogg](concept/ambience-storm-r08-a.ogg) | Ocean storm setting: its baked-in thunder cannot follow Level 12's flashes, so the game plays round 34's rain-only bed from the same source (F6 = a); kept for the round's review |
| [enemy-laser-warning-r08-a.ogg](concept/enemy-laser-warning-r08-a.ogg) | Act 1's only laser, the Mantis, has its own telegraph (round 23) |
| [enemy-coilwyrm-death-final-r24-a.ogg](concept/enemy-coilwyrm-death-final-r24-a.ogg) | A review preview of the Coilwyrm's chained death; the game plays its two bursts |

### Mixing rules

- **Voice limit**: 32 simultaneous voices. Per-sound instance limits: player fire 2, small
  explosions 6, hits 4. Beyond the limit, the oldest instance is stolen.
- **Priority** when stealing voices: warnings and player damage > boss sounds > explosions > enemy
  fire > player fire > pickups > ambience. A sound past the 32 voices steals the oldest voice of
  the lowest priority at or below its own, and is dropped when every voice ranks above it. The
  groups not named above: the interface (menus, radio blips, the hangar, the debrief, the music's
  cues) ranks with the warnings, the specials' sounds with the boss sounds, enemy hazards (mortar,
  sled, flares, the Mantis's beam) and the Vrell screech with enemy fire, hits with the player fire that lands them. A
  running loop (an ambience) is never stolen, since it would not come back, and always gets a
  voice.
- **Levels** (relative, first draft): player fire −12 dB, enemy fire −9 dB, explosions 0 dB,
  player damage and warnings +2 dB, pickups −6 dB, enemy hits −6 dB (8 dB below player damage).
- **Variation**: every frequent sound gets ±5 % random pitch and 2–3 variants.
- **Stereo**: pan by horizontal play-field position, subtle (max ±40 %).
- **Underwater** (round 48 Q12, proposed **a**, pending the user's review: see *Act 4 sounds*): **no** low-pass on
  the sfx bus; heavy real under-water recordings for the explosions and the creatures instead. The first draft was:
  a low-pass filter on the sfx bus in `sub` settings, and muffled variants for
  explosions.

## Concept art

Concept round 01 — see [round 01](../../concept-rounds/round-01/README.md). Briefs and
AI-generator prompts: [concept/prompts.md](concept/prompts.md). Generated by
`tools/concept/audio/sfx.py`. Levels: shots peak at −10 dBFS, pickups −4 to −7, explosions −1.5.

| File | What | Status |
|---|---|---|
| [concept/rejected/player-shot-r01-a.ogg](concept/rejected/player-shot-r01-a.ogg) | Classic laser "pew", descending square, 0.14 s | rejected — synthesized shots not good enough; source real recordings |
| [concept/rejected/player-shot-r01-b.ogg](concept/rejected/player-shot-r01-b.ogg) | Pulse cannon: low thump with a crisp snap, 0.12 s | rejected — synthesized shots not good enough; source real recordings |
| [concept/rejected/player-shot-r01-c.ogg](concept/rejected/player-shot-r01-c.ogg) | Plasma bolt "zwip", rise-fall with small stereo echo, 0.23 s | rejected — synthesized shots not good enough; source real recordings |
| [concept/rejected/explosion-r01-a.ogg](concept/rejected/explosion-r01-a.ogg) | Small crunchy 8-bit pop, abrupt cut, 0.62 s | rejected — must sound more realistic; source real recordings |
| [concept/rejected/explosion-r01-b.ogg](concept/rejected/explosion-r01-b.ogg) | Big boom with sub drop and debris tail, 1.45 s | rejected — must sound more realistic; source real recordings |
| [concept/rejected/explosion-r01-c.ogg](concept/rejected/explosion-r01-c.ogg) | Sci-fi plasma "whoom" with swirling stereo (Vrell flavour), 1.25 s | rejected — must sound more realistic; source real recordings |
| [concept/pickup-r01-a.ogg](concept/pickup-r01-a.ogg) | Classic rising 4-note power-up arpeggio, 0.57 s | chosen — standard power-ups |
| [concept/pickup-r01-b.ogg](concept/pickup-r01-b.ogg) | Two-bell chime with echo (rare upgrades), 0.75 s | chosen — rare upgrades |
| [concept/pickup-r01-c.ogg](concept/pickup-r01-c.ogg) | Rising synth sweep with sparkles (credits), 0.50 s | chosen — credits |

Concept round 02 — recorded sounds from Freesound, imported by
`tools/concept/audio/import_sfx.py`; sources and licences in [CREDITS.md](../../../CREDITS.md).

| File | What | Status |
|---|---|---|
| [concept/player-shot-r02-a.ogg](concept/player-shot-r02-a.ogg) | "Projectile Shoot" by unfa (CC0 1.0) — Pulse Cannon (starter front gun) | chosen — starting set, weapon mapping as described |
| [concept/player-shot-r02-b.ogg](concept/player-shot-r02-b.ogg) | "Sci Fi Gun Shot" by Bird_man (CC0 1.0) — heavy front gun / Hammer Mortar | chosen — starting set, weapon mapping as described |
| [concept/player-shot-r02-c.ogg](concept/player-shot-r02-c.ogg) | "laser3" by nsstudios (CC-BY 4.0) — Lance Laser / light laser weapons | chosen — starting set, weapon mapping as described |
| [concept/player-shot-r02-d.ogg](concept/player-shot-r02-d.ogg) | "Machine Gun 001 - single shot" by pgi (CC0 1.0) — Autocannon Pod / ballistic guns | chosen — starting set, weapon mapping as described |
| [concept/player-shot-r02-e.ogg](concept/player-shot-r02-e.ogg) | "Autocannon Three Shot Burst" by qubodup (CC0 1.0) — Scatter Vulcan / heavy ballistic front gun | chosen — starting set, weapon mapping as described |
| [concept/explosion-r02-a.ogg](concept/explosion-r02-a.ogg) | "small explosion" by bevibeldesign (CC0 1.0) — small enemy destroyed (fighters, drones) | chosen — starting set for the size ladder |
| [concept/explosion-r02-b.ogg](concept/explosion-r02-b.ogg) | "Explosion 1" by magnuswaker (CC0 1.0) — small-to-medium enemy (heavy fighters, turrets) | chosen — starting set for the size ladder |
| [concept/explosion-r02-c.ogg](concept/explosion-r02-c.ogg) | "Explosion" by qubodup (CC0 1.0) — medium enemy (gunships, ground vehicles, buildings) | chosen — starting set for the size ladder |
| [concept/explosion-r02-d.ogg](concept/explosion-r02-d.ogg) | "Nearby explosion with debris" by juskiddink (CC-BY 4.0) — large enemy / mid-boss / building collapse | chosen — starting set for the size ladder |
| [concept/explosion-r02-e.ogg](concept/explosion-r02-e.ogg) | "explosion_big_01" by derplayer (CC0 1.0) — boss destroyed / capital ship | chosen — starting set for the size ladder |

Concept round 03 — per-weapon shot families and the explosion ladder (see Design above),
recorded sounds from Freesound imported by `tools/concept/audio/import_sfx.py`; sources in
[CREDITS.md](../../../CREDITS.md), briefs in [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/shot-vulcan-r03-b.ogg](concept/shot-vulcan-r03-b.ogg) | "minigun.wav" by pgi (CC0 1.0) — `vulcan` b, rotary chatter | chosen |
| [concept/shot-laser-r03-b.ogg](concept/shot-laser-r03-b.ogg) | "Laser shot.wav" by michael_grinnell (CC0 1.0) — `laser` b | chosen |
| [concept/rejected/shot-beam-r03-a.ogg](concept/rejected/shot-beam-r03-a.ogg) | "heavy beam weapon" by a deleted Freesound user (CC0 1.0) — `beam` a, 1.4 s seamless loop (Ion Beam) | rejected — not good |
| [concept/rejected/shot-beam-r03-b.ogg](concept/rejected/shot-beam-r03-b.ogg) | "SFX Oscilating Laser Beam" by bolkmar (CC-BY 4.0) — `beam` b, 2.62 s seamless loop (Orbital Lance) | rejected — barely audible |
| [concept/shot-missile-r03-a.ogg](concept/shot-missile-r03-a.ogg) | "Rocket Launch" by Jarusca (CC0 1.0) — `missile` (Hornet Launcher) | chosen |
| [concept/shot-micromissile-r03-a.ogg](concept/shot-micromissile-r03-a.ogg) | "Rocket Shots" by Audionautics (CC-BY 3.0) — `micromissile`, first shot | chosen |
| [concept/shot-mortar-r03-a.ogg](concept/shot-mortar-r03-a.ogg) | "Mortar Shots.flac" by qubodup (CC0 1.0) — `mortar`, first shot | chosen |
| [concept/shot-bomb-r03-a.ogg](concept/shot-bomb-r03-a.ogg) | "Falling Bomb.wav" by Daleonfire (CC0 1.0) — `bomb`, whistle cut to 1.2 s | chosen |
| [concept/shot-torpedo-r03-a.ogg](concept/shot-torpedo-r03-a.ogg) | "Torpedo launch underwater.wav" by jobro (CC-BY 3.0) — `torpedo` | chosen |
| [concept/shot-mine-r03-a.ogg](concept/shot-mine-r03-a.ogg) | "small metal object fall" by nicktermer (CC0 1.0) — `mine` drop-and-bounce | chosen |
| [concept/shot-tesla-r03-a.ogg](concept/shot-tesla-r03-a.ogg) | "Electric zap.wav" by michael_grinnell (CC0 1.0) — `tesla` | chosen |
| [concept/shot-resonator-r03-a.ogg](concept/shot-resonator-r03-a.ogg) | "sci-fi cannon" by humanoide9000 (CC-BY 4.0) — `resonator` (Choir Resonator) | chosen |
| [concept/explosion-tiny-r03-a.ogg](concept/explosion-tiny-r03-a.ogg) | "Small Explosion" by Cyberios (CC0 1.0) — `tiny` a | chosen |
| [concept/explosion-tiny-r03-b.ogg](concept/explosion-tiny-r03-b.ogg) | "Small explosion" by dinodilopho (CC0 1.0) — `tiny` b | chosen |
| [concept/rejected/explosion-tiny-r03-c.ogg](concept/rejected/explosion-tiny-r03-c.ogg) | "Firecracker Explosion" by unfa (CC0 1.0) — `tiny` c, sharp crack | rejected — not good |
| [concept/explosion-small-r03-a.ogg](concept/explosion-small-r03-a.ogg) | "Small Explosion" by lorenzgillner (CC0 1.0) — `small` c | chosen |
| [concept/rejected/explosion-medium-r03-a.ogg](concept/rejected/explosion-medium-r03-a.ogg) | "Air Explosion.wav" by 1histori (CC0 1.0) — `medium` b | rejected — not good |
| [concept/explosion-medium-r03-b.ogg](concept/explosion-medium-r03-b.ogg) | "Explode001" by mitchelk (CC0 1.0) — `medium` c | chosen |
| [concept/explosion-large-r03-a.ogg](concept/explosion-large-r03-a.ogg) | "explosion_big_02" by derplayer (CC0 1.0) — `large` c | chosen |
| [concept/rejected/explosion-huge-r03-a.ogg](concept/rejected/explosion-huge-r03-a.ogg) | "Explosion_01.wav" by tommccann (CC0 1.0) — `huge` a, 5 s | rejected — not good |
| [concept/explosion-huge-r03-b.ogg](concept/explosion-huge-r03-b.ogg) | "Big Boom" by unfa (CC0 1.0) — `huge` b, 6 s, deep sub-bass | chosen |
| [concept/explosion-underwater-r03-a.ogg](concept/explosion-underwater-r03-a.ogg) | "underwater explosion.wav" by cubix (CC0 1.0) — `underwater` a, recorded | chosen |
| [concept/rejected/explosion-underwater-r03-b.ogg](concept/rejected/explosion-underwater-r03-b.ogg) | "Explosion" by qubodup (CC0 1.0) through a 4-pole 500 Hz low-pass — `underwater` b, derived | rejected — not good (derived low-pass) |
| [concept/explosion-water-r03-a.ogg](concept/explosion-water-r03-a.ogg) | "Water Explosion" by Sheyvan (CC0 1.0) — `water`, surface naval kills | chosen |

Concept round 04 — audible beam loops with start/stop, Sonar Pulse, extra `huge` and
`underwater` explosions; imported by `tools/concept/audio/import_sfx.py`, sources in
[CREDITS.md](../../../CREDITS.md), briefs in [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/shot-beam-r04-a.ogg](concept/shot-beam-r04-a.ogg) | "laser beam" by peepholecircus (CC0 1.0) — `beam` loop a, 2.6 s, smooth ~850 Hz hum (Ion Beam) | chosen |
| [concept/shot-beam-r04-b.ogg](concept/shot-beam-r04-b.ogg) | "Weapons Beam Loop" by unfa (CC0 1.0) — `beam` loop b, 1.9 s, gritty pulsing texture | chosen |
| [concept/shot-beam-r04-c.ogg](concept/shot-beam-r04-c.ogg) | "SonicDeathRay_1.2KHzNoCrackle" by zimbot (CC-BY 4.0) — `beam` loop c, 2.0 s, piercing ray (Orbital Lance) | chosen |
| [concept/shot-beam-start-r04-a.ogg](concept/shot-beam-start-r04-a.ogg) | "Machine Charge" by Glitchedtones (CC0 1.0) — `beam` start, 0.9 s rising charge | chosen |
| [concept/shot-beam-stop-r04-a.ogg](concept/shot-beam-stop-r04-a.ogg) | "Power Down" by noirenex (CC0 1.0) — `beam` stop, 1.3 s power-down | chosen |
| [concept/special-sonar-r04-a.ogg](concept/special-sonar-r04-a.ogg) | "Sonar Ping" by SamsterBirdies (CC0 1.0) — Sonar Pulse, clean single ping | chosen |
| [concept/rejected/special-sonar-r04-b.ogg](concept/rejected/special-sonar-r04-b.ogg) | "Ping!" by unfa (CC0 1.0) — Sonar Pulse, long ringing ping | rejected — A is enough |
| [concept/explosion-huge-r04-a.ogg](concept/explosion-huge-r04-a.ogg) | "Explosion with debris - authentic. 4kg TNT" by sidohzen (CC0 1.0) — `huge` b, 6 s real blast | chosen |
| [concept/explosion-underwater-r04-a.ogg](concept/explosion-underwater-r04-a.ogg) | "underwater explosion" by mokasza (CC-BY 4.0) — `underwater` b | chosen |

Concept [round 08](../../concept-rounds/round-08/README.md) — the remaining Acts 1–2 sounds: recorded hits, player damage, enemy, special, radio, klaxon and ambience sounds (`tools/concept/audio/import_sfx.py`, sources in [CREDITS.md](../../../CREDITS.md)) and synthesized pickups and UI blips (`tools/concept/audio/sfx_r08.py`). Briefs and AI prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/hit-metal-r08-a.ogg](concept/hit-metal-r08-a.ogg) | "HeavyBulletPing.mp3" by wilhellboy (CC0 1.0) — Hit: metal — bullet ping on Ascendancy hulls and machines | chosen |
| [concept/hit-metal-r08-b.ogg](concept/hit-metal-r08-b.ogg) | "Bullet Hit Metal" by coolguy244e (CC0 1.0) — Hit: metal, variant | chosen |
| [concept/hit-organic-r08-a.ogg](concept/hit-organic-r08-a.ogg) | "splat.ogg" by gprosser (CC0 1.0) — Hit: organic — wet hit on Vrell chitin | chosen |
| [concept/hit-organic-r08-b.ogg](concept/hit-organic-r08-b.ogg) | "cracking.wav" by smidoid (CC-BY 4.0) — Hit: organic, chitin crunch | chosen |
| [concept/hit-crumble-r08-a.ogg](concept/hit-crumble-r08-a.ogg) | "building_collapse02_close.wav" by onteca (CC-BY 3.0) — Ground target destroyed — crumbling structure | chosen |
| [concept/hit-crumble-r08-b.ogg](concept/hit-crumble-r08-b.ogg) | "Bricks/Stones/Rocks/Gravel Falling" by iwanPlays (CC0 1.0) — Ground target destroyed, small (rubble burst); replaces "Rock Smash" by NeoSpica, which was almost all sub-bass | chosen |
| [concept/player-shield-hit-r08-a.ogg](concept/player-shield-hit-r08-a.ogg) | "ELECTRIC_ZAP_001.wav" by JoelAudio (CC0 1.0) — Shield hit — electric fizz, low-passed at 7 kHz | chosen |
| [concept/rejected/player-shield-hit-r08-b.ogg](concept/rejected/player-shield-hit-r08-b.ogg) | "Sci-Fi Force Field Impact 15.wav" by StormwaveAudio (CC-BY 4.0) — Shield hit, force-field variant | rejected (user, round 08) |
| [concept/player-shield-break-r08-a.ogg](concept/player-shield-break-r08-a.ogg) | "Synthesized_Pitch-Down_Zap" by joe_bou_khalil (CC-BY 4.0) — Shield break — descending zap | chosen |
| [concept/player-shield-restore-r08-a.ogg](concept/player-shield-restore-r08-a.ogg) | "Power Up Charge [Remix of LegoLunatic's Charged laser 151243]" by qubodup (CC0 1.0) — Shield restored — rising charge | chosen |
| [concept/rejected/player-shield-restore-r08-b.ogg](concept/rejected/player-shield-restore-r08-b.ogg) | "Shield recharging" by Bychop (CC0 1.0) — Shield restored, longer recharge | rejected (user, round 08) |
| [concept/player-armour-hit-r08-a.ogg](concept/player-armour-hit-r08-a.ogg) | "Impact on metal" by JoMungus (CC0 1.0) — Armour hit — metallic crunch | chosen |
| [concept/player-low-armour-r08-a.ogg](concept/player-low-armour-r08-a.ogg) | "Bleeper 1" by magnuswaker (CC0 1.0) — Low armour warning — one beep | chosen |
| [concept/player-destroyed-r08-a.ogg](concept/player-destroyed-r08-a.ogg) | "spaceship explosion9.WAV" by phantastonia (CC-BY 4.0) — Ship destroyed | chosen |
| [concept/overdrive-start-r08-a.ogg](concept/overdrive-start-r08-a.ogg) | "Spacey 1up/Power up" by GameAudio (CC0 1.0) — Overdrive start | chosen |
| [concept/overdrive-end-r08-a.ogg](concept/overdrive-end-r08-a.ogg) | "Teleport Error" by Jerimee (CC0 1.0) — Overdrive end | chosen |
| [concept/enemy-shot-small-r08-a.ogg](concept/enemy-shot-small-r08-a.ogg) | "Sci-fi gun shot x6" by humanoide9000 (CC0 1.0) — Enemy shot, small | chosen |
| [concept/enemy-shot-small-r08-b.ogg](concept/enemy-shot-small-r08-b.ogg) | "retro shot blaster" by JavierZumer (CC-BY 4.0) — Enemy shot, small (retro blaster) | chosen |
| [concept/enemy-shot-heavy-r08-a.ogg](concept/enemy-shot-heavy-r08-a.ogg) | "ScifiHeavyBlasterShot.wav" by SuperPhat (CC0 1.0) — Enemy shot, heavy | chosen |
| [concept/enemy-shot-heavy-r08-b.ogg](concept/enemy-shot-heavy-r08-b.ogg) | "Heavy blaster shot 05" by xkeril (CC0 1.0) — Enemy shot, heavy b — bassy blaster with a falling sweep | chosen |
| [concept/enemy-laser-warning-r08-a.ogg](concept/enemy-laser-warning-r08-a.ogg) | "Laser Charging" by plasterbrain (CC0 1.0) — Enemy laser charge warning | chosen |
| [concept/enemy-missile-r08-a.ogg](concept/enemy-missile-r08-a.ogg) | "Missile firing fl.mp3" by NHMWretched (CC0 1.0) — Enemy missile launch | chosen |
| [concept/rejected/enemy-screech-r08-a.ogg](concept/rejected/enemy-screech-r08-a.ogg) | "Monster screech" by Khrinx (CC0 1.0) — Vrell screech (spawn/attack cue) a | rejected (user, round 08) |
| [concept/rejected/enemy-screech-r08-b.ogg](concept/rejected/enemy-screech-r08-b.ogg) | "inhuman screech.wav" by Wolfsinger (CC-BY 4.0) — Vrell screech b | rejected (user, round 08) |
| [concept/enemy-screech-r08-c.ogg](concept/enemy-screech-r08-c.ogg) | "alien4.wav" by AlienXXX (CC-BY 4.0) — Vrell screech c | chosen |
| [concept/enemy-screech-r08-d.ogg](concept/enemy-screech-r08-d.ogg) | "alien screech.wav" by jvmyka@gmail.com (CC0 1.0) — Vrell screech d — reversed, processed horse sounds | chosen |
| [concept/enemy-spawn-r08-a.ogg](concept/enemy-spawn-r08-a.ogg) | "DCA Alien Spawning Birth.aif" by darcyadam (CC0 1.0) — Vrell spawn a — wet creature swell (Hive Node / Brood Carrier spawns) | chosen |
| [concept/enemy-spawn-r08-b.ogg](concept/enemy-spawn-r08-b.ogg) | "goreSplat.wav" by ThefitzyG (CC0 1.0) — Vrell spawn b — fleshy burst (Brood Pod bursting) | chosen |
| [concept/enemy-lock-r08-a.ogg](concept/enemy-lock-r08-a.ogg) | "lock on" by SamsterBirdies (CC0 1.0) — Turret lock-on beep | chosen |
| [concept/special-airstrike-jets-r08-a.ogg](concept/special-airstrike-jets-r08-a.ogg) | "Jet Plane Flyby.flac" by qubodup (CC0 1.0) — Airstrike: jets flyby | chosen |
| [concept/special-airstrike-bombs-r08-a.ogg](concept/special-airstrike-bombs-r08-a.ogg) | "R11-55-Large Blasts.wav" by craigsmith (CC0 1.0) — Airstrike: bomb carpet | chosen |
| [concept/special-smartbomb-r08-a.ogg](concept/special-smartbomb-r08-a.ogg) | "Energy Blast" by Kinoton (CC0 1.0) — Smart bomb: charge-up + white-out boom | chosen |
| [concept/special-flares-r08-a.ogg](concept/special-flares-r08-a.ogg) | "Guns & Explosions Album - Flare gun 5-2.wav" by OGsoundFX (CC-BY 4.0) — Decoy flares | chosen |
| [concept/special-denied-r08-a.ogg](concept/special-denied-r08-a.ogg) | "acess denied buzz" by Jacco18 (CC0 1.0) — Special unavailable (denied buzz) | chosen |
| [concept/ui-radio-open-r08-a.ogg](concept/ui-radio-open-r08-a.ogg) | "Power On.wav" by JustinBW (CC-BY 4.0) — Radio squelch open | chosen |
| [concept/ui-radio-close-r08-a.ogg](concept/ui-radio-close-r08-a.ogg) | "Radio Sign Off / Squelch" by JovianSounds (CC0 1.0) — Radio squelch close | chosen |
| [concept/ui-klaxon-r08-a.ogg](concept/ui-klaxon-r08-a.ogg) | "Sci-Fi Alarm" by noirenex (CC0 1.0) — Warning klaxon (boss, rear attack) — loop | chosen |
| [concept/rejected/ui-klaxon-r08-b.ogg](concept/rejected/ui-klaxon-r08-b.ogg) | "RedAlert_Klaxon_STTOS_recreated.wav" by zimbot (CC-BY 4.0) — Warning klaxon, single blast | rejected (user, round 08) |
| [concept/ambience-orbit-r08-a.ogg](concept/ambience-orbit-r08-a.ogg) | "spacedrone3.wav" by Elektrocell (CC0 1.0) — Ambience: Earth orbit (space hum) | chosen |
| [concept/ambience-luna-r08-a.ogg](concept/ambience-luna-r08-a.ogg) | "drone Space wind scifi.wav" by ztitchez (CC-BY 4.0) — Ambience: Luna | chosen |
| [concept/ambience-city-r08-a.ogg](concept/ambience-city-r08-a.ogg) | "201110 Distant sirens, urban, night, quiet, roof 11pm.flac" by TRP (CC0 1.0) — Ambience: megacity | chosen |
| [concept/ambience-ocean-r08-a.ogg](concept/ambience-ocean-r08-a.ogg) | "Ocean waves hitting bow of moving boat." by byjoshberry (CC-BY 4.0) — Ambience: ocean | chosen |
| [concept/ambience-storm-r08-a.ogg](concept/ambience-storm-r08-a.ogg) | "Rain and Thunder 4" by FlatHill (CC0 1.0) — Ambience: ocean storm (rain + thunder) | chosen |
| [concept/ambience-arctic-r08-a.ogg](concept/ambience-arctic-r08-a.ogg) | "Wind__Artic__Cold.wav" by cobratronik (CC0 1.0) — Ambience: arctic wind | chosen |
| [concept/pickup-salvage-small-r08-a.ogg](concept/pickup-salvage-small-r08-a.ogg) | Synthesized — Salvage small — short, high PWM blip with one sparkle (pickup-r01-c family, which itself is salvage medium) | chosen |
| [concept/pickup-salvage-large-r08-a.ogg](concept/pickup-salvage-large-r08-a.ogg) | Synthesized — Salvage large — long PWM sweep two octaves up with an octave layer and a sparkle shower | chosen |
| [concept/pickup-shield-cell-r08-a.ogg](concept/pickup-shield-cell-r08-a.ogg) | Synthesized — Shield cell — cool rising triangle arpeggio (G major) with a chorus shimmer | chosen |
| [concept/pickup-armour-patch-r08-a.ogg](concept/pickup-armour-patch-r08-a.ogg) | Synthesized — Armour patch — low square 'clunk' plus a metallic ding: a plate bolted on | chosen |
| [concept/pickup-special-charge-r08-a.ogg](concept/pickup-special-charge-r08-a.ogg) | Synthesized — Special charge — three rising square notes ending in a bell | chosen |
| [concept/ui-menu-move-r08-a.ogg](concept/ui-menu-move-r08-a.ogg) | Synthesized — Menu move — soft 50 ms triangle blip | chosen |
| [concept/ui-menu-confirm-r08-a.ogg](concept/ui-menu-confirm-r08-a.ogg) | Synthesized — Menu confirm — two rising square notes (E6–B6) | chosen |
| [concept/ui-menu-back-r08-a.ogg](concept/ui-menu-back-r08-a.ogg) | Synthesized — Menu back — two falling square notes (B5–E5) | chosen |
| [concept/ui-shop-buy-r08-a.ogg](concept/ui-shop-buy-r08-a.ogg) | Synthesized — Shop buy — blip plus coin sparkle ('ka-ching') | chosen |
| [concept/ui-shop-sell-r08-a.ogg](concept/ui-shop-sell-r08-a.ogg) | Synthesized — Shop sell — three descending coin blips | chosen |
| [concept/ui-shop-denied-r08-a.ogg](concept/ui-shop-denied-r08-a.ogg) | Synthesized — Shop denied (can't afford / won't fit) — low beating square buzz | chosen |
| [concept/ui-typewriter-r08-a.ogg](concept/ui-typewriter-r08-a.ogg) | Synthesized — Typewriter blip — 30 ms click-blip, played per character of briefing text | chosen |
| [concept/ui-tally-tick-r08-a.ogg](concept/ui-tally-tick-r08-a.ogg) | Synthesized — Debrief tally tick — 30 ms high sine tick | chosen |
| [concept/ui-tally-total-r08-a.ogg](concept/ui-tally-total-r08-a.ogg) | Synthesized — Debrief total — C-major bell chord stinger | chosen |
| [concept/ui-equip-r08-a.ogg](concept/ui-equip-r08-a.ogg) | Synthesized — Hangar equip — square clunk, latch click and a quiet rising two-note blip | chosen |
| [concept/ui-upgrade-r08-a.ogg](concept/ui-upgrade-r08-a.ogg) | Synthesized — Hangar upgrade — short PWM rise landing on a G6 bell | chosen |
| [concept/ui-grade-stamp-r08-a.ogg](concept/ui-grade-stamp-r08-a.ogg) | Synthesized — Debrief grade stamp — punchy thump with a paper slap and a low bell | chosen |

Concept [round 11](../../concept-rounds/round-11/README.md) — synthesized by
`tools/concept/audio/sfx_r11.py`, levelled on the 200 Hz–5 kHz band like the recorded sounds
(launch rail −27 dB with a −4 dBFS ceiling, the UI/klaxon level, so it sits under the briefing's
last radio line; warning tones −24 dB / −2 dBFS, the player-damage level).

| File | What | Status |
|---|---|---|
| [concept/rejected/launch-rail-r11-a.ogg](concept/rejected/launch-rail-r11-a.ogg) | Synthesized — Launch rail "mag-lev": linear-motor hum and whine rising with the speed, coil ticks passing faster, release clunk and latch at 3.6 s, engine whoosh into open space (4.9 s) | rejected — b chosen (round 11) |
| [concept/launch-rail-r11-b.ogg](concept/launch-rail-r11-b.ogg) | Synthesized — Launch rail "catapult": pressure hiss building, shuttle rumble over the rail joints with a rattle, heavy two-stage buffer clunk at 3.6 s, steam vent dying away (4.9 s) | | rejected — round 47, L.5: replaced by [launch-rail-r47-b.ogg](concept/launch-rail-r47-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/rejected/ui-edge-warning-r11-a.ogg](concept/rejected/ui-edge-warning-r11-a.ogg) | Synthesized — Edge warning tone "triple chirp": three soft square blips (A5, A5, E6) on the warning's flash cycle (0.267 s) | rejected — b chosen (round 11) |
| [concept/ui-edge-warning-r11-b.ogg](concept/ui-edge-warning-r11-b.ogg) | Synthesized — Edge warning tone "contact ping": an upward chirp into a ringing 1.6 kHz ping, quieter repeat one flash cycle later | chosen |

Concept round 16 (M4 part C, the Level 03 batch) — synthesized by
`tools/concept/audio/sfx_r16.py`, levelled on the 200 Hz–5 kHz band like the recorded enemy
sounds (−30 dB, the Vrell screech and spawn level; ceiling −6 dBFS), so it sits under the large
burst it plays with. Brief: [concept/prompts.md](concept/prompts.md#enemy-leviathan-cry-r16-a-synthesized).

| File | What | Status |
|---|---|---|
| [concept/enemy-leviathan-cry-r16-a.ogg](concept/enemy-leviathan-cry-r16-a.ogg) | Synthesized — the Leviathan's whale-song cry: a deep, mournful alien call (a throat-like pulse voice through moving "oo–aah–oo" formants, its fundamental rising 92 → 128 Hz and falling to 52 Hz with a yodel break, a second voice a tritone above beating against it, a breathy blowhole rasp at the start, a long dark reverb), 3.0 s with the tail; −22.5 LUFS, peak −11.3 dBFS | rejected — round 47, L.2: replaced by [enemy-leviathan-cry-r47-b.ogg](concept/enemy-leviathan-cry-r47-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |

Concept round 21 (M4 part E, Level 05; user decision D8) — recorded, imported by
`tools/concept/audio/import_sfx.py`, an a/b pair for each of the sled's whine and pass and the
Polyp Mortar's lob and impact. Briefs and sources: [concept/prompts.md](concept/prompts.md#round-21--level-05s-sled-and-polyp-mortar-m4-part-e-user-decision-d8), [CREDITS.md](../../../CREDITS.md).

| File | What | Status |
|---|---|---|
| [concept/hazard-sled-whine-r21-a.ogg](concept/hazard-sled-whine-r21-a.ogg) | "Railgun_ChargeLoop" by BaggoNotes (CC0 1.0) — sled whine a, a loop played while the rail lights chase (1.5 s) | chosen |
| [concept/rejected/hazard-sled-whine-r21-b.ogg](concept/rejected/hazard-sled-whine-r21-b.ogg) | "charging power" by JavierZumer (CC-BY 4.0) — sled whine b, played once over the 1.5 s chase | rejected |
| [concept/rejected/hazard-sled-pass-r21-a.ogg](concept/rejected/hazard-sled-pass-r21-a.ogg) | "shipboard_railgun.mp3" by deleted_user_1941307 (CC0 1.0) — sled pass a, the 0.4 s run | rejected |
| [concept/hazard-sled-pass-r21-b.ogg](concept/hazard-sled-pass-r21-b.ogg) | "Planetary Flyby faster.aif" by mattpavone (CC0 1.0) — sled pass b, the 0.4 s run | chosen |
| [concept/enemy-mortar-lob-r21-a.ogg](concept/enemy-mortar-lob-r21-a.ogg) | "Mortar" by Mozfoo (CC0 1.0) — Polyp Mortar lob a | chosen |
| [concept/rejected/enemy-mortar-lob-r21-b.ogg](concept/rejected/enemy-mortar-lob-r21-b.ogg) | "NPX Throat Gathering Spit 3.wav" by noahpardo (CC0 1.0) — Polyp Mortar lob b | rejected |
| [concept/enemy-mortar-impact-r21-a.ogg](concept/enemy-mortar-impact-r21-a.ogg) | "Wet Splat" by JustInvoke (CC-BY 4.0) — Polyp Mortar impact a, the blob landing | chosen |
| [concept/rejected/enemy-mortar-impact-r21-b.ogg](concept/rejected/enemy-mortar-impact-r21-b.ogg) | "Acid Bubbling.wav" by spookymodem (CC0 1.0) — Polyp Mortar impact b, the blob landing | rejected |

Concept round 23 (M4 part F, Level 06; user decision D8) — recorded, imported by
`tools/concept/audio/import_sfx.py`, an a/b pair for each of the Mantis's telegraph and beam sweep,
the perimeter beacon's flare launch and the falling flare's burn loop, and the Coilwyrm's head
regrowth. Briefs and sources: [concept/prompts.md](concept/prompts.md#round-23--level-06s-mantis-flares-and-coilwyrm-m4-part-f-user-decision-d8), [CREDITS.md](../../../CREDITS.md).

| File | What | Status |
|---|---|---|
| [concept/enemy-mantis-telegraph-r23-a.ogg](concept/enemy-mantis-telegraph-r23-a.ogg) | "Laser Charge Up" by magnuswaker (CC0 1.0) — Mantis telegraph a, the 0.6 s crimson arc before a sweep | chosen |
| [concept/rejected/enemy-mantis-telegraph-r23-b.ogg](concept/rejected/enemy-mantis-telegraph-r23-b.ogg) | "laser-charge.wav" by StavSounds (CC0 1.0) — Mantis telegraph b, the 0.6 s crimson arc before a sweep | rejected |
| [concept/rejected/enemy-mantis-sweep-r23-a.ogg](concept/rejected/enemy-mantis-sweep-r23-a.ogg) | "Game - Style Laser Beam" by Jofae (CC0 1.0) — Mantis sweep a, the 1.2 s beam | rejected |
| [concept/enemy-mantis-sweep-r23-b.ogg](concept/enemy-mantis-sweep-r23-b.ogg) | "SonicDeathRay_1.2KHzWithCrackle.wav" by zimbot (CC-BY 4.0) — Mantis sweep b, the 1.2 s beam | chosen |
| [concept/hazard-flare-launch-r23-a.ogg](concept/hazard-flare-launch-r23-a.ogg) | "FlareGun_Shot01.wav" by marb7e (CC-BY 4.0) — flare launch a, the perimeter beacon firing a flare shell (t=70, 112, 150) | chosen |
| [concept/rejected/hazard-flare-launch-r23-b.ogg](concept/rejected/hazard-flare-launch-r23-b.ogg) | "firework_rocket_ignition.ogg" by derplayer (CC0 1.0) — flare launch b, the perimeter beacon firing a flare shell (t=70, 112, 150) | rejected |
| [concept/hazard-flare-burn-r23-a.ogg](concept/hazard-flare-burn-r23-a.ogg) | "ROAD FLARE.wav" by frankelmedico (CC0 1.0) — flare burn a, a quiet loop while a flare falls (8 s, 12 s on easy) | chosen |
| [concept/rejected/hazard-flare-burn-r23-b.ogg](concept/rejected/hazard-flare-burn-r23-b.ogg) | "Road Flare1" by theshaggyfreak (CC-BY 4.0) — flare burn b, a quiet loop while a flare falls (8 s, 12 s on easy) | rejected |
| [concept/rejected/enemy-coilwyrm-regrow-r23-a.ogg](concept/rejected/enemy-coilwyrm-regrow-r23-a.ogg) | "Slime 3.wav" by Archos (CC0 1.0) — Coilwyrm regrowth a, the 0.6 s a cut chain's new head grows | rejected |
| [concept/enemy-coilwyrm-regrow-r23-b.ogg](concept/enemy-coilwyrm-regrow-r23-b.ogg) | "Insect Animal Growl Chitter" by SecureSubset (CC0 1.0) — Coilwyrm regrowth b, the 0.6 s a cut chain's new head grows | chosen |

Concept round 24 (the Coilwyrm's death, user request of 2026-10-05) — four options, each a
segment burst, the head's deeper burst and a preview of the whole chained death at the round's
0.05 s rhythm (`tools/concept/audio/sfx_r24.py`; a–c recorded, cut by `import_sfx.py`'s functions,
d synthesized). Closed 2026-10-05: segment burst **b**, head burst **c**, the ripple slowed to
0.25 s; the production files are rebuilt from the Freesound originals by
`tools/art/sfx_originals.py` (with sfx_r24.py's treatment). Briefs and sources: [concept/prompts.md](concept/prompts.md#round-24--the-coilwyrms-death-bursts), [CREDITS.md](../../../CREDITS.md).

| File | What | Status |
|---|---|---|
| [concept/rejected/enemy-coilwyrm-burst-r24-a.ogg](concept/rejected/enemy-coilwyrm-burst-r24-a.ogg) | "Gib sound" by RoozyDB (CC0 1.0) — Coilwyrm segment burst a, a sharp wet crack with gooey bits (0.30 s) | rejected |
| [concept/rejected/enemy-coilwyrm-head-burst-r24-a.ogg](concept/rejected/enemy-coilwyrm-head-burst-r24-a.ogg) | Coilwyrm head burst a: the same source, a longer cut 18 % slower over a sub thump (0.57 s) | rejected |
| [concept/rejected/enemy-coilwyrm-death-r24-a.ogg](concept/rejected/enemy-coilwyrm-death-r24-a.ogg) | Preview a (review only): the head's burst, then 13 segment bursts 0.05 s apart | rejected |
| [concept/enemy-coilwyrm-burst-r24-b.ogg](concept/enemy-coilwyrm-burst-r24-b.ogg) | "Burst Flesh" by magnuswaker (CC0 1.0) — Coilwyrm segment burst b, a dense fleshy burst with a low body (0.26 s): the segments' and the tail's burst | chosen |
| [concept/rejected/enemy-coilwyrm-head-burst-r24-b.ogg](concept/rejected/enemy-coilwyrm-head-burst-r24-b.ogg) | Coilwyrm head burst b: the same source, a longer cut 18 % slower over a sub thump (0.75 s) | rejected |
| [concept/rejected/enemy-coilwyrm-death-r24-b.ogg](concept/rejected/enemy-coilwyrm-death-r24-b.ogg) | Preview b (review only): the whole chained death at 0.05 s | rejected |
| [concept/rejected/enemy-coilwyrm-burst-r24-c.ogg](concept/rejected/enemy-coilwyrm-burst-r24-c.ogg) | "Messy Splat 3" by FoolBoyMedia (CC-BY 4.0) — Coilwyrm segment burst c, a bright, messy splatter (0.22 s) | rejected |
| [concept/enemy-coilwyrm-head-burst-r24-c.ogg](concept/enemy-coilwyrm-head-burst-r24-c.ogg) | "Messy Splat 3" by FoolBoyMedia (CC-BY 4.0) — Coilwyrm head burst c: a longer cut 18 % slower over a sub thump (0.75 s): the heads' burst (original and regrown) | chosen |
| [concept/rejected/enemy-coilwyrm-death-r24-c.ogg](concept/rejected/enemy-coilwyrm-death-r24-c.ogg) | Preview c (review only): the whole chained death at 0.05 s | rejected |
| [concept/rejected/enemy-coilwyrm-burst-r24-d.ogg](concept/rejected/enemy-coilwyrm-burst-r24-d.ogg) | Synthesized — Coilwyrm segment burst d, a pressurised pop: membrane snap, chitin crackle, a falling gas bloop, wet bubble chirps and a splash hiss (0.28 s) | rejected |
| [concept/rejected/enemy-coilwyrm-head-burst-r24-d.ogg](concept/rejected/enemy-coilwyrm-head-burst-r24-d.ogg) | Coilwyrm head burst d: a bigger synthesized pop (lower bloop, more drops), 18 % slower over a sub thump (0.73 s) | rejected |
| [concept/rejected/enemy-coilwyrm-death-r24-d.ogg](concept/rejected/enemy-coilwyrm-death-r24-d.ogg) | Preview d (review only): the whole chained death at 0.05 s | rejected |
| [concept/enemy-coilwyrm-death-final-r24-a.ogg](concept/enemy-coilwyrm-death-final-r24-a.ogg) | Preview of the chosen pair at the game's rhythm (review only, from the Freesound originals): the head burst c, then 13 bursts b 0.25 s apart, each at the explosion level, pitched up as the members narrow down the taper | chosen |

Concept round 25 (M4 part G, Level 07) — recorded (CC0 / CC-BY), an a/b pair for each of the
Brood Carrier's roar, a bay sac opening, closing and bursting, a unit launched from a sac, the
plate iris opening, and the lifeboat tow's cable snap (`tools/concept/audio/sfx_r25.py`, cut from
the cached Freesound **originals**, levelled on the loudest 100 ms of the 200 Hz–5 kHz band).
Closed 2026-10-05: roar **b**, sac opening **b**, closing **b**, launch **b**, sac burst **a**, iris
**b**, cable snap **a**; the production files in `assets/sfx/` are written by
`tools/art/sfx_originals.py` with sfx_r25.py's treatment (`PRODUCTION`). The klaxon keeps its chosen
`ui-klaxon-r08-a` (already rebuilt from its original), so it is not in the round. Briefs and
sources: [concept/prompts.md](concept/prompts.md#round-25--level-07s-brood-carrier-and-lifeboat-tow-m4-part-g), [CREDITS.md](../../../CREDITS.md).

| File | What | Status |
|---|---|---|
| [concept/rejected/enemy-carrier-roar-r25-a.ogg](concept/rejected/enemy-carrier-roar-r25-a.ogg) | "Deep Roar Echo 2.wav" by noahpardo (CC0 1.0) — carrier roar a: a 0.6 s swell, 2.5 s of deep roar, its echo (4.0 s) | rejected |
| [concept/enemy-carrier-roar-r25-b.ogg](concept/enemy-carrier-roar-r25-b.ogg) | "Didgeridoo Monster Roar" by Noxdl (CC0 1.0) — carrier roar b: a bear's roar over a didgeridoo drone, a buzzing, whale-like body (3.0 s) | chosen |
| [concept/rejected/enemy-carrier-sac-open-r25-a.ogg](concept/rejected/enemy-carrier-sac-open-r25-a.ogg) | "GOREFlsh_Flesh Manipulation 01_KVV AUDIO_FREE" by KVV_Audio (CC-BY 4.0) — sac opening a: flesh pulled apart close up (0.85 s) | rejected |
| [concept/enemy-carrier-sac-open-r25-b.ogg](concept/enemy-carrier-sac-open-r25-b.ogg) | "Squelch.mp3" by LucasDuff (CC0 1.0) — sac opening b: something torn out of a sucking mud (0.9 s) | chosen |
| [concept/rejected/enemy-carrier-sac-close-r25-a.ogg](concept/rejected/enemy-carrier-sac-close-r25-a.ogg) | "GOREFlsh_Flesh Manipulation 01_KVV AUDIO_FREE" by KVV_Audio (CC-BY 4.0) — sac closing a: the same recording's third gesture, quieter (0.6 s) | rejected |
| [concept/enemy-carrier-sac-close-r25-b.ogg](concept/enemy-carrier-sac-close-r25-b.ogg) | "Squelch.mp3" by LucasDuff (CC0 1.0) — sac closing b: opening b's tear reversed, a wet suck that ends shut (0.6 s) | chosen |
| [concept/rejected/enemy-carrier-launch-r25-a.ogg](concept/rejected/enemy-carrier-launch-r25-a.ogg) | "Spit 1 - The Ridge - Spanker" by bananplyte (CC0 1.0) — launch a: a creature's throaty spit (0.5 s) | rejected |
| [concept/enemy-carrier-launch-r25-b.ogg](concept/enemy-carrier-launch-r25-b.ogg) | "Slime Attack 1" by qubodup (CC0 1.0) — launch b: a slime monster's wet lunge, brighter (0.4 s) | chosen |
| [concept/enemy-carrier-sac-burst-r25-a.ogg](concept/enemy-carrier-sac-burst-r25-a.ogg) | "Headshot 2" by SilverIllusionist (CC-BY 4.0) — sac burst a: a very wet, fleshy explosion, 10 % slower (1.56 s) | chosen |
| [concept/rejected/enemy-carrier-sac-burst-r25-b.ogg](concept/rejected/enemy-carrier-sac-burst-r25-b.ogg) | "Gutsy Spillage 1" by magnuswaker (CC0 1.0) — sac burst b: a visceral tear with a wet tail, 10 % slower (1.22 s) | rejected |
| [concept/rejected/enemy-carrier-iris-r25-a.ogg](concept/rejected/enemy-carrier-iris-r25-a.ogg) | "SFX Door Open.wav" by Paul368 (CC0 1.0) — iris a: an alien hatch morphing open, a pressure release up front (1.6 s) | rejected |
| [concept/enemy-carrier-iris-r25-b.ogg](concept/enemy-carrier-iris-r25-b.ogg) | "Simple Mutate (Monster)" by Division4884 (CC0 1.0) — iris b: a grinding, organic morph (1.3 s) | chosen |
| [concept/secret-cable-snap-r25-a.ogg](concept/secret-cable-snap-r25-a.ogg) | "snapping-chain" by CosmicEmbers (CC-BY 3.0) — cable snap a: a sharp crack, the loose chain rattling after it (1.2 s) | chosen |
| [concept/rejected/secret-cable-snap-r25-b.ogg](concept/rejected/secret-cable-snap-r25-b.ogg) | "Guitar string snaps.wav" by juskiddink (CC-BY 4.0) — cable snap b: a string snapping, 25 % slower, a heavy cable's twang and ring (1.6 s) | rejected |

Concept round 27 (user decision of 2026-10-06 on round 26's open questions) — an a/b pair for the
save-done sound, synthesized in the round-08 UI family (peak −8 dBFS like the other UI blips), and
for the Coilwyrm's chain-cut tear, recorded (CC0 / CC-BY, cut from the cached Freesound
**originals**, levelled on the loudest 100 ms of the 200 Hz–5 kHz band to −20 dB, between the
segment burst's −17.3 and the regrowth's −27.4), all by `tools/concept/audio/sfx_r27.py`. Not
listened to by Claude. Closed 2026-10-06: save done **b**, chain-cut tear **a**; the save sound is
copied into `assets/sfx/` by `copyPlaceholderSounds`, the tear's production file is written by
`tools/art/sfx_originals.py` with sfx_r27.py's treatment (`PRODUCTION`). Briefs and sources:
[concept/prompts.md](concept/prompts.md#round-27--the-save-done-sound-and-the-coilwyrms-chain-cut-tear), [CREDITS.md](../../../CREDITS.md).

| File | What | Status |
|---|---|---|
| [concept/rejected/ui-save-r27-a.ogg](concept/rejected/ui-save-r27-a.ogg) | Synthesized — save done a, "write and seal": four quick rising pulse ticks (E6 G♯6 B6 E7), then a bright triangle fifth (E6 + B6) over a bell an octave up, a short echo (0.70 s) | rejected |
| [concept/ui-save-r27-b.ogg](concept/ui-save-r27-b.ogg) | Synthesized — save done b, "calm chime": a soft latch click, two warm bells a fourth apart (A5, D6 80 ms later) over a swelling triangle pad (0.92 s) | chosen |
| [concept/enemy-coilwyrm-cut-r27-a.ogg](concept/enemy-coilwyrm-cut-r27-a.ogg) | "rip_tear FLESH!!!!.wav" by aust_paul (CC0 1.0) — chain-cut tear a: a monster tearing at flesh, a run of short wet rips, 15 % slower for a bigger body (1.06 s) | chosen |
| [concept/rejected/enemy-coilwyrm-cut-r27-b.ogg](concept/rejected/enemy-coilwyrm-cut-r27-b.ogg) | "Tearing Flesh" by dereklieu (CC-BY 3.0) — chain-cut tear b: one juicy limb-tearing rip (layered bread breaks) with a low body (0.72 s) | rejected |

Concept round 28 (M5 part A) — an a/b pair for the proximity mine's arming beep, synthesized in the
round-08 UI family by `tools/concept/audio/sfx_r28.py`: short, dry, high and quiet (peak −12 dBFS,
the tick level), so mines arming together do not clutter. Not listened to by Claude. Closed: **a**
chosen (user), b in `concept/rejected/`; the game plays a as a concept copy (`Sfx.MINE_ARM`,
`copyPlaceholderSounds`).
Briefs: [concept/prompts.md](concept/prompts.md#round-28--the-proximity-mines-arming-beep).

| File | What | Status |
|---|---|---|
| [concept/weapon-mine-arm-r28-a.ogg](concept/weapon-mine-arm-r28-a.ogg) | Synthesized — mine arming a, "armed chirp": two soft rising pulse blips a fifth apart (A6, E7 45 ms later), low-passed (0.10 s) | chosen |
| [concept/rejected/weapon-mine-arm-r28-b.ogg](concept/rejected/weapon-mine-arm-r28-b.ogg) | Synthesized — mine arming b, "sensor ping": one sine ping gliding up a fifth in 15 ms, ringing out on a faint metallic overtone over a tiny latch click (0.15 s) | rejected |

Concept round 31 (M5 part C) — a/b pairs for the arcology's collapse (a rumble swelling over the
1.5 s warning into a long crash) and the Ravager's pounce (a short snarl over a whoosh), cut by
`tools/concept/audio/sfx_r31.py` from cached Freesound originals (CC0 / CC-BY); the Hive Node reuses
the carrier iris (`enemy-carrier-iris-r25-b`) and the spawn (`enemy-spawn-r08-a`). Not listened to by
Claude. Outcome (user, 2026-10-07): collapse **a**, both pounces kept (picked at random per
pounce); collapse b moved to `concept/rejected/`. Briefs and sources:
[concept/prompts.md](concept/prompts.md#round-31--the-arcologys-collapse-and-the-ravagers-pounce),
[CREDITS.md](../../../CREDITS.md).

| File | What | Status |
|---|---|---|
| [concept/collapse-r31-a.ogg](concept/collapse-r31-a.ogg) | "Cracking Earthquake" by uagadugu (CC0) + "Big crash, a house tumbling down" by YleArkisto (CC-BY 4.0) — collapse a: stony rumble swelling 1.5 s into a tumbling masonry crash (5.5 s) | chosen |
| [concept/rejected/collapse-r31-b.ogg](concept/rejected/collapse-r31-b.ogg) | "Radiator Metal Rumbling" by RutgerMuller (CC0) + "Explosion or Collapse.wav" by tec_studio (CC0) — collapse b: steel-frame rattle swelling into a deep rolling cave-in with a long dust tail (5.7 s) | rejected |
| [concept/ravager-pounce-r31-a.ogg](concept/ravager-pounce-r31-a.ogg) | "Goblin Snarl" by qubodup (CC0) — pounce a: rasping dog-like attack snarl over a quick swish (0.7 s) | chosen |
| [concept/ravager-pounce-r31-b.ogg](concept/ravager-pounce-r31-b.ogg) | "Dragon: Snarl, Roar + Attack" by Breviceps (CC0) — pounce b: snapping attack roar, sped up to hound size, over a heavier whoosh (0.7 s) | chosen |

Concept round 32 (M5 part D) — a/b pairs for the Wraith's decloak, the Mote Swarm (a one-shot at
its entry and again at its loop-back) and the lance of Level 10's scripted loss (its impact 1.2 s into
the file, so the game starts it 1.2 s before the hit), cut by `tools/concept/audio/sfx_r32.py` from
cached Freesound originals (CC0 / CC-BY) with synthesized layers. Not listened to by Claude.
Outcome (user, 2026-10-08): decloak **b** (CC-BY), swarm **b**, lance **a**; the other three moved
to `concept/rejected/`. Briefs and sources:
[concept/prompts.md](concept/prompts.md#round-32--the-wraiths-decloak-the-mote-swarms-whoosh-and-the-lance),
[CREDITS.md](../../../CREDITS.md).

| File | What | Status |
|---|---|---|
| [concept/rejected/wraith-decloak-r32-a.ogg](concept/rejected/wraith-decloak-r32-a.ogg) | "ICEBrk_Break04" by InMotionAudio (CC0) + synthesized shimmer — decloak a: violet shimmer breaking into a glassy ice crack (0.7 s) | rejected |
| [concept/wraith-decloak-r32-b.ogg](concept/wraith-decloak-r32-b.ogg) | "Spacey Teleport Rip" by GameAudio (CC0) + "Swirling Velvet Cloak 2" by F.M.Audio (CC-BY 4.0) — decloak b: metallic sci-fi rip over a membrane swish (0.6 s) | chosen |
| [concept/rejected/mote-swarm-r32-a.ogg](concept/rejected/mote-swarm-r32-a.ogg) | "Pigeon flock fly away" by TRP (CC0) + synthesized whoosh — swarm a: birdlike rush of small wings (1.7 s) | rejected |
| [concept/mote-swarm-r32-b.ogg](concept/mote-swarm-r32-b.ogg) | "Insect Superfast Wing Flap" by kalhan (CC0) ×5 + synthesized whoosh — swarm b: insect chitter (1.7 s) | chosen |
| [concept/lance-r32-a.ogg](concept/lance-r32-a.ogg) | synthesized whine + "Closeup Thunder Strike 01" by loganzsound (CC0) — lance a: alien falling whine into a thunder crack, impact at 1.2 s (3.8 s) | chosen |
| [concept/rejected/lance-r32-b.ogg](concept/rejected/lance-r32-b.ogg) | "Whistling Firework" by magnuswaker (CC0) + "Piercing impact / Stabbing" by Breviceps (CC0) + synthesized thump — lance b: whistle into a piercing stab, impact at 1.2 s (1.8 s) | rejected |

Concept [round 33](../../concept-rounds/round-33/README.md) (M5 part E, Level 11) — one proposal each (no a/b) for the Harbour Kraken's lane
churn, slam, surfacing swell and death, the convoy ships' hit and sinking, the frigate's distant flak
and the Driftjelly's pulse, built by `tools/concept/audio/sfx_r33.py` from cached Freesound originals
(CC0, but for the byjoshberry waves in the churn and the sinking, CC-BY and already on the credits roll) with synthesized layers; the torpedo launch, the water
explosion and the ocean loop are the existing finals. Levels as the loudest 100 ms of the 200 Hz–5 kHz
band against the existing set (see the script's header). Closed 2026-10-09 (user): all eight
accepted, the churn, death, sinking and flak after a rework (no bubbles; the flak raised twice);
seven rebuilt from their originals into `assets/sfx/` by `tools/art/sfx_originals.py` (`sfx_r33.py`'s
`PRODUCTION`), the synthesized pulse copied by `:pipeline:copyPlaceholderSounds`. Briefs and sources:
[concept/prompts.md](concept/prompts.md#round-33--level-11s-kraken-convoy-ships-flak-and-jelly),
[CREDITS.md](../../../CREDITS.md). The offset is the event's moment inside the file, so the game
starts the file that long before the event.

| File | What | Status |
|---|---|---|
| [concept/kraken-slam-r33-a.ogg](concept/kraken-slam-r33-a.ogg) | "Big robot footstep 002" by AudioPapkin (CC0) + "Big Splash" by Bird_man (CC0) + synthesized rush, thump and spray — slam: the arm's rush, impact and splash at 0.5 s (2.2 s, −15 dB) | chosen |
| [concept/kraken-churn-r33-a.ogg](concept/kraken-churn-r33-a.ogg) | "Ocean waves hitting bow of moving boat" by byjoshberry (CC-BY) + synthesized rumble — lane churn: a rushing surge of waves swelling for 1.0 s (−24 dB) | chosen |
| [concept/kraken-surface-r33-a.ogg](concept/kraken-surface-r33-a.ogg) | "Coming up from underwater" by adviseme333 (CC0) + synthesized rumble and whoosh — surfacing: a deep swell peaking as the crown breaks (1.1 s), water streaming off (2.75 s, −21 dB) | chosen |
| [concept/ship-hit-r33-a.ogg](concept/ship-hit-r33-a.ogg) | "Metal Shovel Impact" by Sadiquecat + "slightmetallicthud" by profoundsounds + "Splash_002" by jamesabels (all CC0) + synthesized thump — ship hit: metal struck, water rushing in, impact at 0 s (0.94 s, −18 dB) | chosen |
| [concept/ship-sink-r33-a.ogg](concept/ship-sink-r33-a.ogg) | "…ship hull list2" by kyles (CC0) + "Ocean waves hitting bow of moving boat" by byjoshberry (CC-BY) + synthesized boom and rumble — ship sinking: steel groan, muffled boom, wave wash (3.8 s, −20 dB) | chosen |
| [concept/frigate-flak-r33-a.ogg](concept/frigate-flak-r33-a.ogg) | "Distant Shot 4" and "Distant Shot 2" by HenKonen (CC0) — distant flak: two dull far pops at 0 and 0.55 s, raised twice after listening (1.6 s, −16 dB) | chosen |
| [concept/driftjelly-pulse-r33-a.ogg](concept/driftjelly-pulse-r33-a.ogg) | synthesized (no source) — jelly pulse: a soft underwater bloop, a lower echo and a ripple (0.9 s, −24 dB) | rejected — round 46, K.6: replaced by [driftjelly-pulse-r46-b.ogg](concept/driftjelly-pulse-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/kraken-death-r33-a.ogg](concept/kraken-death-r33-a.ogg) | "Sea Creature Roar" by Bikkit99 + "CO2 Water Jet" by unfa + synthesized rumble (all CC0) — death: a slowed, long wet groan over churning water, under the `huge` burst (5.2 s, −17 dB) | chosen |

Concept round 34 (M5 part F, Level 12; the round's README is written in step 8) — user decisions
F6 = a and F8 = a: a/b for the thunder (three variants per proposal, played 0.3–1.5 s after each
lightning flash), the gust, the Lamprey's latch and its drain loop; one proposal for the shake-off
and the sonde chime; and the rain-only storm bed cut from the chosen bed's source (the chosen
`ambience-storm-r08-a` kept). Built by `tools/concept/audio/sfx_r34.py` from cached Freesound originals
(all already credited in earlier rounds; the CC-BY ones — Wet Splat, inhuman screech, the sonic death
ray — already on the credits roll) with synthesized layers; levels as the loudest 100 ms of the
200 Hz–5 kHz band against the existing set (the script's header). Briefs and sources:
[concept/prompts.md](concept/prompts.md#round-34--level-12s-storm-and-the-lamprey),
[CREDITS.md](../../../CREDITS.md).

| File | What | Status |
|---|---|---|
| [concept/rejected/thunder-1-r34-a.ogg](concept/rejected/thunder-1-r34-a.ogg) | "Rain and Thunder 4" by FlatHill (CC0) + synthesized sub rumble — thunder a 1 (the storm's own thunder, the bed's source): its big clap and roll, low-passed at 1.8 kHz to drop the rain (6.5 s, −14 dB, −14.4 LUFS) | rejected |
| [concept/rejected/thunder-2-r34-a.ogg](concept/rejected/thunder-2-r34-a.ogg) | "Rain and Thunder 4" by FlatHill (CC0) + synthesized sub rumble — thunder a 2: a shorter distant roll, panned right (3.6 s, −16 dB, −20.4 LUFS) | rejected |
| [concept/rejected/thunder-3-r34-a.ogg](concept/rejected/thunder-3-r34-a.ogg) | "Rain and Thunder 4" by FlatHill (CC0) + synthesized sub rumble — thunder a 3: a deep low roll, nearly all below 200 Hz (3.6 s, −19 dB, −16.3 LUFS) | rejected |
| [concept/thunder-1-r34-b.ogg](concept/thunder-1-r34-b.ogg) | "Closeup Thunder Strike 01" by loganzsound (CC0) + synthesized sub rumble — thunder b 1 (close strikes): a sharp crack, then the roll (7.0 s, −14 dB, −22.1 LUFS) | chosen |
| [concept/thunder-2-r34-b.ogg](concept/thunder-2-r34-b.ogg) | "Closeup Thunder Strike 01" by loganzsound (CC0) + synthesized sub rumble — thunder b 2: the strike 14 % slower, farther, panned right (6.0 s, −16 dB, −22.7 LUFS) | chosen |
| [concept/thunder-3-r34-b.ogg](concept/thunder-3-r34-b.ogg) | "Closeup Thunder Strike 01" by loganzsound (CC0) + synthesized sub rumble — thunder b 3: the strike 26 % slower, the farthest (5.5 s, −17 dB, −22.5 LUFS) | chosen |
| [concept/rejected/gust-r34-a.ogg](concept/rejected/gust-r34-a.ogg) | synthesized (no source) — gust a: band-passed wind rising over the 1.0 s telegraph with a hollow howl, holding through the gust with a flutter, falling; from the left to the right (3.6 s, −19 dB) | rejected |
| [concept/gust-r34-b.ogg](concept/gust-r34-b.ogg) | "Wind__Artic__Cold.wav" by cobratronik (CC0) + synthesized rumble — gust b: a recorded gust in the same envelope and pan (3.6 s, −19 dB) | chosen |
| [concept/lamprey-latch-r34-a.ogg](concept/lamprey-latch-r34-a.ogg) | "Slime Attack 1" by qubodup + "rip_tear FLESH!!!!.wav" by aust_paul (both CC0) + synthesized suction and thump — latch a, suck and bite: an inhaling rush, the bite at 0.16 s, teeth grinding in (0.67 s, −16.7 dB) | chosen |
| [concept/rejected/lamprey-latch-r34-b.ogg](concept/rejected/lamprey-latch-r34-b.ogg) | "Wet Splat" by JustInvoke (CC-BY) + "slightmetallicthud.mp3" by profoundsounds + "rip_tear FLESH!!!!.wav" by aust_paul (CC0) + synthesized thump — latch b, clamp on the hull: a wet splat on a metallic thud at 0 s, teeth gnawing in (0.68 s, −17.5 dB) | rejected |
| [concept/lamprey-drain-r34-a.ogg](concept/lamprey-drain-r34-a.ogg) | synthesized (no source) — drain a, seamless 2.4 s loop: a low electric hum throbbing in 0.4 s gulps (latch look a's swallow), sparse crackle, a faint whine (−22 dB) | rejected — round 46, K.7: replaced by [lamprey-drain-r46-b.ogg](concept/lamprey-drain-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/rejected/lamprey-drain-r34-b.ogg](concept/rejected/lamprey-drain-r34-b.ogg) | "SonicDeathRay_1.2KHzWithCrackle.wav" by zimbot (CC-BY, slowed to 0.45) + synthesized sub hum — drain b, seamless 2.4 s loop: a siphoning whine with crackle over a sub swelling twice a loop (−22 dB) | rejected |
| [concept/lamprey-shake-r34-a.ogg](concept/lamprey-shake-r34-a.ogg) | "Wet Splat" by JustInvoke + "inhuman screech.wav" by Wolfsinger (both CC-BY) + synthesized whoosh — shake-off: a wet slap and a short squeal whooshing away (0.82 s, −13.5 dB; −17 dB until step G8) | chosen |
| [concept/sonde-chime-r34-a.ogg](concept/sonde-chime-r34-a.ogg) | synthesized (no source) — sonde collected: a three-blip telemetry chirp into two bright bell tones, E6 and B6 (0.95 s, −16 dB) | rejected — round 46, K.8: replaced by [sonde-chime-r46-a.ogg](concept/sonde-chime-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/ambience-storm-rain-r34-a.ogg](concept/ambience-storm-rain-r34-a.ogg) | "Rain and Thunder 4" by FlatHill (CC0) — the rain-only storm bed (F6 = a): the source's stretch after its thunder, 90 Hz high-pass, seamless 10.0 s loop, at the ambiences' level (band RMS −38.8 dB under the −10 dBFS ceiling; the same rain is ~14 dB quieter inside the chosen bed) | chosen |
| [concept/relay-hit-r35-a.ogg](concept/relay-hit-r35-a.ogg) | "Impact on metal" by JoMungus + "ELECTRIC_ZAP_001" by JoelAudio (CC0) + synthesized thump — relay hit a: struck plating and a spark (0.57 s, −19 dB) | chosen |
| [concept/rejected/relay-hit-r35-b.ogg](concept/rejected/relay-hit-r35-b.ogg) | "slightmetallicthud" by profoundsounds (CC0) + synthesized glitch and thump — relay hit b: a dull thud and the link glitching (0.45 s, −19 dB) | rejected |
| [concept/integrity-alarm-r35-a.ogg](concept/integrity-alarm-r35-a.ogg) | synthesized (no source) — integrity alarm a: hi-lo two-tone (988/740 Hz) ×3 (1.5 s, −17 dB) | chosen (kept in round 47, L.3: "The old one is fine for that.") |
| [concept/rejected/integrity-alarm-r35-b.ogg](concept/rejected/integrity-alarm-r35-b.ogg) | synthesized (no source) — integrity alarm b: three rising whoops 420→1050 Hz (1.5 s, −17 dB) | rejected |
| [concept/rejected/grid-online-r35-a.ogg](concept/rejected/grid-online-r35-a.ogg) | synthesized (no source) — grid online a: five breakers closing, a mains hum swelling, an A-major bloom (2.6 s, −17 dB) | rejected |
| [concept/grid-online-r35-b.ogg](concept/grid-online-r35-b.ogg) | "Machine Charge" by Glitchedtones (CC0) + synthesized clunk, whine and chord — grid online b: spin-up into a contact clunk (2.6 s, −17 dB) | chosen |
| [concept/drone-launch-r35-a.ogg](concept/drone-launch-r35-a.ogg) | synthesized (no source) — drone launch: four rotors spinning up, climbing away with a pan (2.2 s, −20 dB) | rejected — round 46, K.10: replaced by [drone-launch-r46-a.ogg](concept/drone-launch-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/relay-destroyed-r35-a.ogg](concept/relay-destroyed-r35-a.ogg) | "Big Boom" by unfa + "Sounds For Earthquakes - Radiator Metal Rumbling" by RutgerMuller (CC0) + synthesized power-down and thump — relay destroyed (3.7 s, −15 dB) | chosen |
| [concept/root-burst-r36-a.ogg](concept/root-burst-r36-a.ogg) | "Cracking Earthquake" by uagadugu + "Gravel Falling" by iwanPlays (CC0) + "cracking.wav" by smidoid + "building_collapse02_close" by onteca (CC-BY) + synthesized cracks and thump — root burst a (round 36): paving cracking under a swelling rumble for 1 s, a masonry crunch and falling stones at 1.0 s (2.1 s, −17 dB) | chosen |
| [concept/rejected/root-burst-r36-b.ogg](concept/rejected/root-burst-r36-b.ogg) | "Squelch" by LucasDuff + "…ship hull list2" by kyles + "rip_tear FLESH!!!!" by aust_paul (CC0) + "Tearing Flesh" by dereklieu (CC-BY) + synthesized slosh, creak and thump — root burst b (round 36): a wet creep surge and a root creaking for 1 s, a wet tear at 1.0 s (2.0 s, −17 dB) | rejected |
| [concept/root-growth-r36-a.ogg](concept/root-growth-r36-a.ogg) | "…ship hull list2" by kyles (CC0) + synthesized swell, creak and knock — root growth (round 36): stretching creak and a swelling over 1.5 s, a knock as it sets (1.75 s, −20 dB) | chosen |
| [concept/rejected/tear-free-roar-r36-a.ogg](concept/rejected/tear-free-roar-r36-a.ogg) | "Cracking Earthquake" by uagadugu + "ICEBrk_Break04" by InMotionAudio + "Big Boom" by unfa + "Deep Roar Echo 2" by noahpardo + "Gravel Falling" by iwanPlays (CC0) + "building_collapse02_close" by onteca + "cracking.wav" by smidoid (CC-BY) + synthesized thump and lift — tear-free a (round 36): a tectonic rip, root snaps, a shockwave boom at 0.35 s, a deep roar under the lift (3.4 s, −15 dB) | rejected |
| [concept/tear-free-roar-r36-b.ogg](concept/tear-free-roar-r36-b.ogg) | "Sea Creature Roar" by Bikkit99 + "Dragon: Snarl, Roar + Attack" by Breviceps + "rip_tear FLESH!!!!" by aust_paul (CC0) + "Tearing Flesh" by dereklieu (CC-BY) + synthesized shockwave and thump — tear-free b (round 36): a creature bellow rising in pitch with the lift, wet tearing roots, an air shockwave (3.4 s, −14 dB) | chosen |
| [concept/rejected/canopy-withering-r36-a.ogg](concept/rejected/canopy-withering-r36-a.ogg) | "rip_tear FLESH!!!!" by aust_paul + "Wind__Artic__Cold" by cobratronik (CC0) + "Tearing Flesh" by dereklieu (CC-BY) + synthesized crackle — withering a (round 36): a dry crackling shrivel and a slow far tear settling into wind (7.0 s, −19 dB) | rejected |
| [concept/canopy-withering-r36-b.ogg](concept/canopy-withering-r36-b.ogg) | "Simple Mutate (Monster)" by Division4884 + "Wind__Artic__Cold" by cobratronik (CC0) + synthesized sigh and groan — withering b (round 36): a dying exhale, a deep groan and sigh descending into wind (7.5 s, −19 dB) | chosen |
| [concept/canopy-hum-r36-a.ogg](concept/canopy-hum-r36-a.ogg) | synthesized (no source) — canopy hum (round 36), seamless 8.0 s loop: two slowly beating harmonic voices on 46 Hz, a 0.75 Hz throb and a 4 s breath (band RMS −39 dB, 3 dB under the ambiences) | rejected — round 46, K.1: replaced by [canopy-hum-r46-a.ogg](concept/canopy-hum-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |

Concept [round 36](../../concept-rounds/round-36/README.md) (Act 3 "Red Dust", overnight design
phase 2026-10-12, step A-audio) — a/b for four Act 3 sounds from cached Freesound originals (CC0, two
CC-BY, all in [CREDITS.md](../../../CREDITS.md)) plus synthesized layers; generator
`tools/concept/audio/sfx_r36b.py` (the `b` suffix because Level 14's sounds already use `r36`
names). One-shots levelled like the recorded enemy sounds, loops by their band RMS (beam −30 dB,
storm bed −36 dB); no bubble, popping or squelch layers. For the Ion Beam a third option is to keep
the chosen round-04 loop [shot-beam-r04-a](concept/shot-beam-r04-a.ogg) unchanged.

| File | What | Status |
|---|---|---|
| [concept/burrow-eruption-r36-a.ogg](concept/burrow-eruption-r36-a.ogg) | "Cracking Earthquake" by uagadugu + "Hard Impact on Sand" by Elements-Library + "SAND POUR.wav" by nicoproson (CC0) + synthesized pressure tone and sand hiss — burrow eruption a "sand burst": the 1.0 s tell's rumble swelling, then the break-out impact and sand raining down (2.1 s, −19 dB) | chosen (round 36, S.2) |
| [concept/rejected/burrow-eruption-r36-b.ogg](concept/rejected/burrow-eruption-r36-b.ogg) | "Deep Roar Echo 2" by noahpardo + "Gravel Impacts and Falls.wav" by lolamadeus (CC0) + synthesized churn, spray and thump — b "the swimmer breaks out": sand churning under the ground, a muffled growl, gravel thrown up (2.1 s, −19 dB) | rejected — round 36: a (sand burst) picked |
| [concept/rejected/canyon-scrape-r36-a.ogg](concept/rejected/canyon-scrape-r36-a.ogg) | "Metal pipe along concrete 2.wav" (deleted user) + "Hard Impact on Sand" by Elements-Library + "Gravel Impacts and Falls.wav" by lolamadeus (CC0) — canyon wall scrape a "rock grind": the hull dragged along stone, a dull hit, grit falling (0.65 s, −18 dB) | rejected — round 36: b (metal screech) picked |
| [concept/canyon-scrape-r36-b.ogg](concept/canyon-scrape-r36-b.ogg) | "big metal side impact 3 scraping.wav" by Halleck (CC-BY) + synthesized spark hiss and thump — b "metal screech" (0.6 s, −18 dB) | chosen (round 36, S.3) |
| [concept/ion-beam-loop-r36-a.ogg](concept/ion-beam-loop-r36-a.ogg) | "laser beam" by peepholecircus (CC0, the round-04 loop's source and window) + a synthesized ion body and circular hiss — Ion Beam loop a "the chosen hum, heavier", seamless 2.60 s loop (−30 dB) | chosen (round 36, S.4) |
| [concept/rejected/ion-beam-loop-r36-b.ogg](concept/rejected/ion-beam-loop-r36-b.ogg) | synthesized (no source) — b "ion stream": a buzzing core, a smooth shimmer flutter at 1.8 kHz, a circular hiss and a slow swell, seamless 2.00 s loop (−30 dB) | rejected — round 36: a (the chosen hum, heavier) picked |
| [concept/dust-storm-bed-r36-a.ogg](concept/dust-storm-bed-r36-a.ogg) | "G56-22-Sandstorm.wav" by craigsmith (CC0) + "20101121.wind.howl.furious.01.wav" by dobroide (CC-BY) — dust-storm bed a "recorded front" (`mars-storm`, Level 18): a steady sandstorm over a low furious howl, seamless 16.0 s loop (−36 dB) | chosen (round 36, S.5) |
| [concept/rejected/dust-storm-bed-r36-b.ogg](concept/rejected/dust-storm-bed-r36-b.ogg) | synthesized (no source) — b "synthesized front": a low roar, grit hiss and a wandering howl band swelling with gusts, seamless 16.0 s loop (−36 dB) | rejected — round 36: a (recorded front) picked |

Concept round 37 (M7 part A, step AUD-A) — a/b for the Act 3 equipment sounds from Freesound originals
(CC0, in [CREDITS.md](../../../CREDITS.md); new: jokallset, Maxx222, JSK_Productions) plus synthesized
layers; generator `tools/concept/audio/sfx_r37.py` (briefs and measurements in
[concept/prompts.md](concept/prompts.md), section *Round 37*). Closed 2026-10-13 (user): S.2, S.4, S.6–S.10
**b**, the rest **a**; the unchosen variants in `concept/rejected/` (the generator writes them there).
One-shots levelled like the recorded sounds, loops by band RMS (the impact sizzle −38 dB, about 1–2.5 LU
under the beam loop; the lance rumble −36 dB); no bubble, popping or squelch layers, every file scanned
for isolated peaks. The chosen Ion Beam loop [ion-beam-loop-r36-a](concept/ion-beam-loop-r36-a.ogg) stays
as it is (`--check-loop`: 2.600 s, band RMS −30.0 dB, seam +1.8 dB); the start plays **with** the loop,
the stop at the release.

| File | What | Status |
|---|---|---|
| [concept/ion-beam-start-r37-a.ogg](concept/ion-beam-start-r37-a.ogg) | "Machine Charge" by Glitchedtones (CC0) + synthesized ignition and ion body — Ion Beam start a "charge snap": the round-04 start's rise at 1.8× speed, a hiss and the loop's 110 Hz body rising onto its pitch (0.40 s, −24 dB) | chosen (round 37, S.1) |
| [concept/rejected/ion-beam-start-r37-b.ogg](concept/rejected/ion-beam-start-r37-b.ogg) | synthesized (no source) — start b "ion strike": an arc crack and the ion body settling 220 → 110 Hz (0.45 s, −24 dB) | rejected — round 37: a (charge snap) picked |
| [concept/rejected/ion-beam-stop-r37-a.ogg](concept/rejected/ion-beam-stop-r37-a.ogg) | "Power Down" by noirenex (CC0) + synthesized ion body — stop a "power down, short": the round-04 stop's first fall over the body sliding 110 → 45 Hz (0.55 s, −25 dB) | rejected — round 37: b (ion collapse) picked |
| [concept/ion-beam-stop-r37-b.ogg](concept/ion-beam-stop-r37-b.ogg) | synthesized (no source) — stop b "ion collapse": the body sliding 110 → 35 Hz, its hiss falling (0.62 s, −25 dB) | rejected — round 46, K.17: replaced by [ion-beam-stop-r46-a.ogg](concept/ion-beam-stop-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/beam-impact-r37-a.ogg](concept/beam-impact-r37-a.ogg) | synthesized (no source) — beam impact a "arc scorch": a fluttering dry 2–7 kHz sizzle over a low burning body, seamless 1.50 s loop (−38 dB, −27.7 LUFS) | rejected — round 46, K.18: replaced by [beam-impact-r46-a.ogg](concept/beam-impact-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/rejected/beam-impact-r37-b.ogg](concept/rejected/beam-impact-r37-b.ogg) | "Plasma cutter gas.wav" by jokallset (CC0) + synthesized sizzle — impact b "cutting torch", seamless 1.50 s loop (−38 dB, −28.8 LUFS) | rejected — round 37: a (arc scorch) picked |
| [concept/rejected/beam-impact-rock-r37-a.ogg](concept/rejected/beam-impact-rock-r37-a.ogg) | synthesized (no source) — impact a on rock: with a grit crackle and a stone roar, seamless 1.50 s loop (−38 dB) | rejected — round 37: b (cutting torch on rock) picked |
| [concept/beam-impact-rock-r37-b.ogg](concept/beam-impact-rock-r37-b.ogg) | "Plasma cutter gas.wav" by jokallset (CC0) + synthesized sizzle, grit and stone — impact b on rock, seamless 1.50 s loop (−38 dB) | chosen (round 37, S.4) |
| [concept/deflector-tick-r37-a.ogg](concept/deflector-tick-r37-a.ogg) | "ELECTRIC_ZAP_001" by JoelAudio (CC0) + synthesized ping — Deflector tick a: the shield hit at 1.6× speed, cut short, a glassy tick (0.12 s, −25.5 dB) | chosen (round 37, S.5) |
| [concept/rejected/deflector-tick-r37-b.ogg](concept/rejected/deflector-tick-r37-b.ogg) | synthesized (no source) — tick b "dry deflect": a high snap and three inharmonic metal partials ringing 35 ms (0.12 s, −24.5 dB) | rejected — round 37: a (the shield hit, up) picked |
| [concept/rejected/deflector-return-r37-a.ogg](concept/rejected/deflector-return-r37-a.ogg) | synthesized (no source) — charge return a "soft rise": two soft voices easing up a fifth, 520 → 780 Hz (0.45 s, −27 dB) | rejected — round 37: b (charge settle) picked |
| [concept/deflector-return-r37-b.ogg](concept/deflector-return-r37-b.ogg) | synthesized (no source) — return b "charge settle": an E5–B5 fifth fading in over a rising air whoosh, no glide (0.45 s, −27 dB) | chosen (round 37, S.6) |
| [concept/rejected/emp-ring-r37-a.ogg](concept/rejected/emp-ring-r37-a.ogg) | "Electric zap.wav" by michael_grinnell (CC0) + synthesized thump, arcs and buzz — EMP ring a "the tesla, layered": four ever slower copies spreading out, crackling arcs, a thump (1.35 s, −18.6 dB, −14.1 LUFS) | rejected — round 37: b (arc ring) picked |
| [concept/emp-ring-r37-b.ogg](concept/emp-ring-r37-b.ogg) | synthesized (no source) — ring b "arc ring": a dying 60 → 45 Hz electric buzz, a discharge crack, arcs and a thump (1.34 s, −18 dB, −19.2 LUFS) | rejected — round 46, K.15: replaced by [emp-ring-r46-b.ogg](concept/emp-ring-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/rejected/emp-powerdown-r37-a.ogg](concept/rejected/emp-powerdown-r37-a.ogg) | synthesized (no source) — machine power-down a "spin-down whine": a relay clunk and a whine falling 1400 → 180 Hz (0.92 s, −24 dB) | rejected — round 37: b (buzz dies) picked |
| [concept/emp-powerdown-r37-b.ogg](concept/emp-powerdown-r37-b.ogg) | synthesized (no source) — power-down b "buzz dies": a flickering 100 Hz mains buzz sagging to 70 Hz (0.9 s, −24 dB) | rejected — round 46, K.16: replaced by [emp-powerdown-r46-a.ogg](concept/emp-powerdown-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/rejected/lance-rumble-r37-a.ogg](concept/rejected/lance-rumble-r37-a.ogg) | synthesized (no source) — Orbital Lance rumble a "orbital roar" under loop c: a deep roar over low voices on 41 Hz, seamless 3.00 s loop (−36 dB, −24.9 LUFS) | rejected — round 37: b (the ground answers) picked |
| [concept/lance-rumble-r37-b.ogg](concept/lance-rumble-r37-b.ogg) | "Cracking Earthquake" by uagadugu (CC0) + synthesized roar — rumble b "the ground answers", seamless 3.00 s loop (−36 dB, −23.1 LUFS) | chosen (round 37, S.9) |
| [concept/rejected/overcharge-whoosh-r37-a.ogg](concept/rejected/overcharge-whoosh-r37-a.ogg) | "Power Up Charge" by qubodup (CC0) + synthesized whoosh and shimmer — Shield Overcharge activation a "the restore, charged" (1.0 s, −19 dB) | rejected — round 37: b (double shell) picked |
| [concept/overcharge-whoosh-r37-b.ogg](concept/overcharge-whoosh-r37-b.ogg) | synthesized (no source) — activation b "double shell": a pad rising an octave as its filter opens, a whoosh and a white bloom (1.0 s, −19 dB) | rejected — round 46, K.13: replaced by [overcharge-whoosh-r46-b.ogg](concept/overcharge-whoosh-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/overcharge-end-r37-a.ogg](concept/overcharge-end-r37-a.ogg) | synthesized (no source) — overcharge end a "shimmer fades": fluttering high partials gliding down (0.9 s, −24 dB) | rejected — round 46, K.14: replaced by [overcharge-end-r46-b.ogg](concept/overcharge-end-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/rejected/overcharge-end-r37-b.ogg](concept/rejected/overcharge-end-r37-b.ogg) | "Power Up Charge" by qubodup (CC0) + synthesized shimmer — end b "the charge drains": the restore's source backwards, fading, under a falling shimmer (0.9 s, −24 dB) | rejected — round 37: a (shimmer fades) picked |
| [concept/cooldown-ready-r37-a.ogg](concept/cooldown-ready-r37-a.ogg) | synthesized (no source) — cooldown ready a "two soft bells": A5 then E6 (0.5 s, −22 dB) | chosen (round 37, S.12) |
| [concept/rejected/cooldown-ready-r37-b.ogg](concept/rejected/cooldown-ready-r37-b.ogg) | synthesized (no source) — ready b "glass fifth": one soft C6–G6 chord (0.5 s, −22 dB) | rejected — round 37: a (two soft bells) picked |
| [concept/dash-r37-a.ogg](concept/dash-r37-a.ogg) | synthesized (no source) — dash a "thruster puff": a jet noise burst sweeping 600 → 2500 → 900 Hz over a short low roar (0.35 s, −20.6 dB) | rejected — round 46, K.12: replaced by [dash-r46-b.ogg](concept/dash-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/rejected/dash-r37-b.ogg](concept/rejected/dash-r37-b.ogg) | "RocketThrustMaxx.wav" by Maxx222 + "Afterburner (Close, Screamer, Quick Onset)" by JSK_Productions (CC0) — dash b "rocket kick" (0.35 s, −20 dB) | rejected — round 37: a (thruster puff) picked |
| [concept/drone-break-r37-a.ogg](concept/drone-break-r37-a.ogg) | "HeavyBulletPing" by wilhellboy + "Small Explosion" by Cyberios (CC0) + synthesized sparks — light drone break a: the metal hit and a `tiny` blast with sparks (0.49 s, −20 dB) | chosen (round 37, S.14) |
| [concept/rejected/drone-break-r37-b.ogg](concept/rejected/drone-break-r37-b.ogg) | "Impact on metal" by JoMungus + "Small explosion" by dinodilopho (CC0) + synthesized sparks — break b "crunch" (0.63 s, −20 dB) | rejected — round 37: a (ping and blast) picked |
| [concept/drone-rebuild-r37-a.ogg](concept/drone-rebuild-r37-a.ogg) | synthesized (no source) — drone rebuilt a "materialise": a soft saw pad easing up a fourth as its filter opens (0.6 s, −25 dB) | rejected — round 46, K.19: replaced by [drone-rebuild-r46-a.ogg](concept/drone-rebuild-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/rejected/drone-rebuild-r37-b.ogg](concept/rejected/drone-rebuild-r37-b.ogg) | synthesized (no source) — rebuilt b "three bells": E5, A5, E6 over a faint sparkle (0.57 s, −25 dB) | rejected — round 37: a (materialise) picked |

Concept round 38 (M7 part B, Level 15, step AUD-B; closed 2026-10-14: bulge **b**, dive **b**, entry roar **a**,
`mars-dust` **a**, the unchosen files in `concept/rejected/`) — a/b for the Burrower's
bulge and dive, Level 15's entry roar and the Mars dust wind `mars-dust`, from cached Freesound originals
already credited (CC0; the dobroide howl and smidoid's cracking CC-BY; all in
[CREDITS.md](../../../CREDITS.md)) plus synthesized layers; generator `tools/concept/audio/sfx_r38.py`
(briefs and measurements in [concept/prompts.md](concept/prompts.md), section *Round 38*). One-shots levelled
like the recorded enemy sounds, loops by band RMS (−36 dB, the ambiences'); no bubble, popping or
squelch layers. The chosen eruption a already opens with the 1.0 s bulge tell (its rumble swells for
1.0 s, the impact lands at 1.0 s), so it is played **at the bulge's start**, and the bulge's a is that
alone (no file); b adds a crack layer over it. The game ships the eruption and the four chosen files
(`:pipeline:copyPlaceholderSounds`).

| File | What | Status |
|---|---|---|
| [concept/burrow-bulge-r38-b.ogg](concept/burrow-bulge-r38-b.ogg) | "cracking.wav" by smidoid (CC-BY) + synthesized cracks and a gritty trickle — bulge b "crack layer": sand and crust cracking faster towards the break, no rumble of its own, played with the eruption a from the bulge's start (1.27 s, −25 dB; a is the eruption alone) | chosen (round 38, S.1) |
| [concept/burrow-bulge-mix-r38-b.ogg](concept/burrow-bulge-mix-r38-b.ogg) | Listening aid: the eruption a and the bulge b started together, as the game mixes b (2.1 s; not loaded by the game) | chosen (round 38, S.1; listening aid) |
| [concept/rejected/burrow-dive-r38-a.ogg](concept/rejected/burrow-dive-r38-a.ogg) | "SAND POUR.wav" by nicoproson + "Bricks/Stones/Rocks/Gravel Falling" by iwanPlays (CC0) + a synthesized sand slump — dive a "sand pour": a sand hiss under a low-pass falling 9 → 1.6 kHz, small stones dropping in (0.62 s, −22 dB, −22.0 LUFS) | rejected — round 38: b (grain slide) picked |
| [concept/burrow-dive-r38-b.ogg](concept/burrow-dive-r38-b.ogg) | synthesized (no source) — dive b "grain slide": a dense rain of noise grains whose band falls 5 → 1 kHz as it thins, over the slump (0.55 s, −26 dB, −21.4 LUFS) | rejected — round 46, J.17: replaced by [burrow-dive-r46-a.ogg](concept/burrow-dive-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/entry-roar-r38-a.ogg](concept/entry-roar-r38-a.ogg) | "20101121.wind.howl.furious.01.wav" by dobroide (CC-BY) + a synthesized hull buffet — entry roar a "recorded roar": the howl high-passed at 650 Hz (its body gone, the thin roar left) over a low buffet 10 dB under, seamless 12.0 s loop (−36 dB) | chosen (round 38, S.3) |
| [concept/rejected/entry-roar-r38-b.ogg](concept/rejected/entry-roar-r38-b.ogg) | synthesized (no source) — entry roar b "synthesized roar": a thin air roar (0.7–6 kHz), a resonant whistle wandering 1.6–2.6 kHz and the buffet, seamless 12.0 s loop (−36 dB) | rejected — round 38: a (recorded roar) picked |
| [concept/mars-dust-r38-a.ogg](concept/mars-dust-r38-a.ogg) | "G56-22-Sandstorm.wav" by craigsmith + "Wind__Artic__Cold.wav" by cobratronik (CC0) — `mars-dust` a "recorded dust wind": the sandstorm (another stretch than the storm bed's, 0.3–6 kHz) riding slow gusts over a cold wind's low moan, thinner and calmer than `mars-storm`, seamless 16.0 s loop (−36 dB) | chosen (round 38, S.4) |
| [concept/rejected/mars-dust-r38-b.ogg](concept/rejected/mars-dust-r38-b.ogg) | synthesized (no source) — `mars-dust` b "synthesized dust wind": a thin pinkish wind, a faint whistle wandering 0.9–1.4 kHz and a grit hiss rising with the gusts, seamless 16.0 s loop (−36 dB) | rejected — round 38: a (recorded dust wind) picked |
| [concept/tendril-hum-r39-a.ogg](concept/tendril-hum-r39-a.ogg) | synthesized (no source) — tendril hum a: 120 Hz and its harmonics up to 5 kHz (odd ones stronger) over a steady fizz, no vibrato, beating or modulation (no wobble), seamless 6.0 s loop (−38 dB) | chosen (round 39, S.1) |
| [concept/tendril-burn-r39-a.ogg](concept/tendril-burn-r39-a.ogg) | synthesized (no source) — tendril burn a: 3–9 ms noise-grain crackle (2–8 kHz, ~220 a second) over a dry sizzle bed, seamless 2.0 s loop (−34 dB) | rejected — round 46, J.15: replaced by [tendril-burn-r46-a.ogg](concept/tendril-burn-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/tendril-break-r39-a.ogg](concept/tendril-break-r39-a.ogg) | synthesized (no source) — tendril break a: a whip-crack and a falling harmonic whine (2.4 → 0.55 kHz, fading out), 0.9 s (−17 dB) | rejected — round 46, J.16: replaced by [tendril-break-r46-b.ogg](concept/tendril-break-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/crawler-walk-r39-a.ogg](concept/crawler-walk-r39-a.ogg) | synthesized (no source) — Threadcrawler walk a: twelve legs in two rows, 2–6 ms noise ticks at 8.4 steps a second over a rock rustle, seamless 3.0 s loop (−36 dB) | rejected — round 46, J.13: replaced by [crawler-walk-r46-a.ogg](concept/crawler-walk-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/mars-canyon-r39-a.ogg](concept/mars-canyon-r39-a.ogg) | synthesized (no source) — `mars-canyon` a: a dust wind with a resonant canyon howl (260–420 Hz plus an octave voice) breathing with the gusts, seamless 16.0 s loop (−36 dB) | rejected — round 46, K.2: replaced by [mars-canyon-r46-a.ogg](concept/mars-canyon-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/dome-hit-r40-a.ogg](concept/dome-hit-r40-a.ogg) | synthesized (no source) — `dome-hit` a: a 70–190 Hz knock and a glass tick, a creak (resonance 0.7 → 1.4 kHz) panned round the field, 60 stress-line grains; no shatter, 1.2 s (−18 dB) | rejected — round 49, M.1: replaced by [dome-hit-r49-b.ogg](concept/dome-hit-r49-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/rejected/dome-break-r40-a.ogg](concept/rejected/dome-break-r40-a.ogg) | synthesized (no source) — `dome-break` a: a crack, ~400 glass grains thinning out, a hiss falling 6 → 1.2 kHz and a sawtooth frame groan sinking 150 → 85 Hz, 3.6 s (−16 dB) | rejected |
| [concept/rejected/dome-break-r40-b.ogg](concept/rejected/dome-break-r40-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `dome-break` b: collapse.wav (metalfortress) with Glass Shatter 2 (Greg_Surr) and a synthesized 55 → 38 Hz sub thump, 2.4 s (−16 dB) | rejected |
| [concept/dome-break-r40-c.ogg](concept/dome-break-r40-c.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `dome-break` c: Building Collapse (craigsmith, low-passed) over Earth's Crust Depressing (bevibeldesign, 180 Hz low-pass) with Glass Smash (dwswords), 2.4 s (−16 dB) | chosen |
| [concept/herald-song-r40-a.ogg](concept/herald-song-r40-a.ogg) | synthesized (no source) — `herald-song` a: three voices (a fifth apart) singing an 'ah' (formants 800/1150/2900 Hz), F3 → A3 → E3 with a slow vibrato, 1.9 s (−20 dB) | rejected — round 47, L.1: replaced by [herald-song-r47-a.ogg](concept/herald-song-r47-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/herald-aura-r40-a.ogg](concept/herald-aura-r40-a.ogg) | synthesized (no source) — `herald-aura` a: six steady partials at 1.9–4.6 kHz with slow swells over a faint 5–9 kHz air, seamless 4.0 s loop (−40 dB) | rejected — round 46, J.19: replaced by [herald-aura-r46-b.ogg](concept/herald-aura-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/aura-absorb-r40-a.ogg](concept/aura-absorb-r40-a.ogg) | synthesized (no source) — `aura-absorb` a: a glassy ping of four inharmonic partials (2.1–6.1 kHz), 0.4 s (−22 dB) | rejected — round 46, J.20: replaced by [aura-absorb-r46-b.ogg](concept/aura-absorb-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/aura-collapse-r40-a.ogg](concept/aura-collapse-r40-a.ogg) | synthesized (no source) — `aura-collapse` a: five sustained voices sliding a fourth down over 1.6 s with a fading sheet of ring-glass pings, 2.0 s (−18 dB) | chosen (round 40, S.f: accepted) |
| [concept/shellback-walk-r40-a.ogg](concept/shellback-walk-r40-a.ogg) | synthesized (no source) — `shellback-walk` a: four padded footfalls (60–160 Hz thud, 300–900 Hz pad) and clacking plates, seamless 3.2 s loop (−34 dB) | rejected — round 46, J.12: replaced by [shellback-walk-r46-b.ogg](concept/shellback-walk-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/shellback-lob-r40-a.ogg](concept/shellback-lob-r40-a.ogg) | synthesized (no source) — `shellback-lob` a: an 80–260 Hz cough thump and a short pressure hiss (300–1800 Hz), 0.6 s (−17 dB) | rejected — round 46, J.11: replaced by [shellback-lob-r46-b.ogg](concept/shellback-lob-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/shellback-curl-r40-a.ogg](concept/shellback-curl-r40-a.ogg) | synthesized (no source) — `shellback-curl` a: eleven staggered plate scrapes (band noise 0.6–2.5 kHz) ending in a heavy clack, 0.8 s (−20 dB) | rejected — round 46, J.8: replaced by [shellback-curl-r46-b.ogg](concept/shellback-curl-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/shellback-roll-r40-a.ogg](concept/shellback-roll-r40-a.ogg) | synthesized (no source) — `shellback-roll` a: a 40–140 Hz rumble and a gritty rock-crush noise on 2.5 turns a loop, seamless 2.5 s loop (−30 dB) | rejected — round 46, J.10: replaced by [shellback-roll-r46-b.ogg](concept/shellback-roll-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/shellback-uncurl-r40-a.ogg](concept/shellback-uncurl-r40-a.ogg) | synthesized (no source) — `shellback-uncurl` a: the plate scrapes in reverse order, a looser clack and a short exhale of grit, 0.7 s (−22 dB) | rejected — round 46, J.9: replaced by [shellback-uncurl-r46-b.ogg](concept/shellback-uncurl-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/mars-colony-r40-a.ogg](concept/mars-colony-r40-a.ogg) | synthesized (no source) — `mars-colony` a: a thin plains wind, the domes' air-plant hum (60 Hz and partials 2–6, steady) and a distant rail clank, seamless 16.0 s loop (−36 dB) | rejected — round 46, K.3: replaced by [mars-colony-r46-b.ogg](concept/mars-colony-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/devil-spin-r41-a.ogg](concept/devil-spin-r41-a.ogg) | synthesized (no source) — `devil-spin` a: a whirling wind roar circling (resonance turning 3 times a loop, channels half a turn apart) over a 50–130 Hz rumble and a 3–8 kHz grit hiss, seamless 3.0 s loop (−32 dB) | rejected — round 46, J.5: replaced by [devil-spin-r46-a.ogg](concept/devil-spin-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/devil-open-r41-a.ogg](concept/devil-open-r41-a.ogg) | synthesized (no source) — `devil-open` a: a gust swelling 0.4 → 1.8 kHz, five vane scrapes and a low hollow tone (110/165/220 Hz), 0.7 s (−20 dB) | rejected — round 46, J.6: replaced by [devil-open-r46-a.ogg](concept/devil-open-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/devil-spiral-r41-a.ogg](concept/devil-spiral-r41-a.ogg) | synthesized (no source) — `devil-spiral` a: a turning grit hiss (resonance 3.5 → 1.8 kHz) with sand ticks, 0.45 s (−22 dB) | rejected — round 46, J.7: replaced by [devil-spiral-r46-a.ogg](concept/devil-spiral-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/ghost-drone-drop-r41-a.ogg](concept/ghost-drone-drop-r41-a.ogg) | synthesized (no source) — `ghost-drone-drop` a: a clean turbine whine descending (harmonic set, 1100 → 520 Hz) with a faint air hiss, 0.9 s (−24 dB) | rejected — round 46, J.1: replaced by [ghost-drone-drop-r46-b.ogg](concept/ghost-drone-drop-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/ghost-drone-glint-r41-a.ogg](concept/ghost-drone-glint-r41-a.ogg) | synthesized (no source) — `ghost-drone-glint` a: an electronic lock tick: two inharmonic pings (2.6 and 3.9 kHz, 60 ms decay), 0.15 s (−24 dB) | rejected — round 46, J.2: replaced by [ghost-drone-glint-r46-b.ogg](concept/ghost-drone-glint-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/ghost-drone-burst-r41-a.ogg](concept/ghost-drone-burst-r41-a.ogg) | synthesized (no source) — `ghost-drone-burst` a: three dry precise shots 65 ms apart (a snap, a 1.5–4 kHz crack, a 150–400 Hz body each), 0.35 s (−21 dB: the −4 dBFS ceiling limits it) | rejected — round 46, J.3: replaced by [ghost-drone-burst-r46-a.ogg](concept/ghost-drone-burst-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/ghost-drone-death-r41-a.ogg](concept/ghost-drone-death-r41-a.ogg) | synthesized (no source) — `ghost-drone-death` a: a dry bang, nine pieces of ringing metal flung out and a spatter of 70 spark grains, no fireball rumble, 0.8 s (−19 dB) | rejected — round 46, J.4: replaced by [ghost-drone-death-r46-b.ogg](concept/ghost-drone-death-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/walker-stomp-r42-a.ogg](concept/walker-stomp-r42-a.ogg) | synthesized (no source) — `walker-stomp` a: a 30–120 Hz thud and a 120–400 Hz body, a low leg clank (inharmonic partials round 260 Hz) and a hydraulic hiss (2–6 kHz, 25 ms attack), 1.0 s (−19 dB: the −4 dBFS ceiling limits it) | rejected — round 46, J.21: replaced by [walker-stomp-r46-a.ogg](concept/walker-stomp-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/rejected/walker-hatch-r42-a.ogg](concept/rejected/walker-hatch-r42-a.ogg) | synthesized (no source) — `walker-hatch` a: a latch tick, a short pneumatic hiss and a hinge clack (inharmonic partials round 900 Hz), 0.45 s (−22 dB) | rejected (round 43: replaced by the Freesound redo) |
| [concept/rejected/walker-missile-r42-a.ogg](concept/rejected/walker-missile-r42-a.ogg) | synthesized (no source) — `walker-missile` a: a dry ignition crack and a rocket whoosh rising 0.5 → 2.6 kHz (20 ms attack) sliding off to one side with a thin exhaust hiss, 0.8 s (−19 dB) | rejected (round 42: the walker missile takes round 08 a) |
| [concept/rejected/walker-flame-tell-r42-a.ogg](concept/rejected/walker-flame-tell-r42-a.ogg) | synthesized (no source) — `walker-flame-tell` a: a gas hiss rising 0.8 → 3.2 kHz and swelling over a low gas rush, 0.7 s (−22 dB) | rejected (round 43: replaced by the Freesound redo) |
| [concept/rejected/walker-flame-r42-a.ogg](concept/rejected/walker-flame-r42-a.ogg) | synthesized (no source) — `walker-flame` a: an 80–1400 Hz flame roar flickering at 3, 7 and 11 cycles a loop over a 40–120 Hz body and a 3–7 kHz gas hiss, seamless 2.0 s loop (−28 dB) | rejected (round 43: replaced by the Freesound redo) |
| [concept/rejected/vent-tell-r42-a.ogg](concept/rejected/vent-tell-r42-a.ogg) | synthesized (no source) — `vent-tell` a: a rising gas roar (resonance 250 → 1400 Hz, swelling) over a deepening 40–160 Hz rumble, 1.05 s (−22 dB) | rejected (round 43: replaced by the Freesound redo) |
| [concept/rejected/vent-burst-r42-a.ogg](concept/rejected/vent-burst-r42-a.ogg) | synthesized (no source) — `vent-burst` a: a fire blast (40 ms attack, no pop; 60–3000 Hz, resonance falling 1.4 → 0.3 kHz) roaring out and dying, 1.5 s (−16 dB) | rejected (round 43: replaced by the Freesound redo) |
| [concept/rejected/tank-tracks-r42-a.ogg](concept/rejected/tank-tracks-r42-a.ogg) | synthesized (no source) — `tank-tracks` a: a 400–1600 Hz tread grind with track-link ticks 14 a second, a 41 Hz diesel harmonic set and a faint 1.9 kHz turbine whine, seamless 2.0 s loop (−32 dB) | rejected (round 43: replaced by the Freesound redo) |
| [concept/rejected/tank-turret-r42-a.ogg](concept/rejected/tank-turret-r42-a.ogg) | synthesized (no source) — `tank-turret` a: a servo whine (harmonic set gliding 420 → 640 Hz, smooth attack and release) with faint gear ticks, 0.6 s (−24 dB) | rejected (round 43: replaced by the Freesound redo) |
| [concept/rejected/tank-burst-r42-a.ogg](concept/rejected/tank-burst-r42-a.ogg) | synthesized (no source) — `tank-burst` a: a heavy autocannon, four rounds 0.12 s apart (a snap, a 0.8–3 kHz crack and a 50–250 Hz body each, a short low echo), 0.75 s (−18 dB: the −4 dBFS ceiling limits it) | rejected (round 43: replaced by the Freesound redo) |
| [concept/tank-death-r42-a.ogg](concept/tank-death-r42-a.ogg) | synthesized (no source) — `tank-death` a: a deep 30–200 Hz boom, a fire roar falling 1.5 → 0.3 kHz, a heavy metal clang (round 420 Hz), eight pieces of debris clattering and 50 spark grains, 1.8 s (−17 dB) | rejected — round 46, J.22: replaced by [tank-death-r46-a.ogg](concept/tank-death-r46-a.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/belt-rumble-r42-a.ogg](concept/belt-rumble-r42-a.ogg) | synthesized (no source) — `belt-rumble` a: a 40–160 Hz roller rumble, a 200–1200 Hz belt grind, plate seams knocking (250–900 Hz with a 1.2–2.5 kHz rattle) 6 a second and a 50 Hz motor hum, seamless 2.4 s loop (−34 dB) | chosen (round 42, accepted; round 43 closed) |
| [concept/mars-foundry-r42-a.ogg](concept/mars-foundry-r42-a.ogg) | synthesized (no source) — `mars-foundry` a: a 35–180 Hz conveyor rumble, a far furnace roar (80–900 Hz, peaking at 240 Hz), a slow distant hammer every 4.0 s (a low-passed thud and ring with a hall echo) and the plateau wind, seamless 16.0 s loop (−36 dB) | rejected — round 46, K.4: replaced by [mars-foundry-r46-b.ogg](concept/mars-foundry-r46-b.ogg) (the file stays in concept/: earlier rounds' boards link it) |
| [concept/rejected/walker-hatch-r43-a.ogg](concept/rejected/walker-hatch-r43-a.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `walker-hatch` a: air brakes releasing and a large metal door slamming; AIRBrst-Samsung Galaxy Smartphone, CU_Truck, Air Brakes, Released_Nicholas Judy_TDC (designerschoice) + Closing large metal door-001 (AudioPapkin), 1.05 s (−20 dB) | rejected (round 43: not picked) |
| [concept/walker-hatch-r43-b.ogg](concept/walker-hatch-r43-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-hatch` b: a submarine hatch's metal clank and creak over a steam pressure release, a deep impact under the clank; Air (or steam) pressure release (brunoboselli) + G45-30-Submarine Hatch Opening.wav (craigsmith) + Big Structure Collision Heard from Inside (Osvoldon), 1.01 s (−20 dB) | chosen (round 43, S.a b: the user's pick) |
| [concept/walker-flame-tell-r43-a.ogg](concept/walker-flame-tell-r43-a.ogg) | Freesound (CC-BY 3.0, [CREDITS.md](../../../CREDITS.md)) — `walker-flame-tell` a: a huge gas vent opening; Venting.wav (Sea Fury), 0.87 s (−21 dB) | chosen (round 43, S.b a: the user's pick) |
| [concept/rejected/walker-flame-tell-r43-b.ogg](concept/rejected/walker-flame-tell-r43-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-flame-tell` b: a 1000 hp boiler's safety valve opening, slowed to 0.85; steam_safety_valve.wav (Engineer_815), 0.84 s (−20 dB) | rejected (round 43: not picked) |
| [concept/rejected/walker-flame-r43-a.ogg](concept/rejected/walker-flame-r43-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-flame` a: a flamethrower's long burst, slowed to 0.85; Flamethrower_Weapon_Long_Bursts.wav (AslakHostaker), seamless 4.0 s loop (−28 dB) | rejected (round 43: not picked) |
| [concept/walker-flame-r43-b.ogg](concept/walker-flame-r43-b.ogg) | Freesound (CC-BY 3.0, [CREDITS.md](../../../CREDITS.md)) — `walker-flame` b: a flamethrower's deep roar; Flamethrower (pugaeme), seamless 4.0 s loop (−28 dB) | chosen (round 43, S.c b: the user's pick) |
| [concept/vent-tell-r43-a.ogg](concept/vent-tell-r43-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `vent-tell` a: a balloon burner's roar swelling in; gas vuur dakwerken gasbrander luchtballon.wav (NickPeeters), 1.05 s (−20 dB) | chosen (round 43, S.d a: the user's pick) |
| [concept/rejected/vent-tell-r43-b.ogg](concept/rejected/vent-tell-r43-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `vent-tell` b: a brighter balloon burner's roar swelling in; Hot air balloon burner (YannSauvin), 1.05 s (−20 dB) | rejected (round 43: not picked) |
| [concept/vent-burst-r43-a.ogg](concept/vent-burst-r43-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `vent-burst` a: a deep flame burst with the end of a gas burner's burn, dying away; Flame Burst (magnuswaker) + gas vuur dakwerken gasbrander luchtballon.wav (NickPeeters), 1.60 s (−19 dB) | chosen (round 43, S.e a: the user's pick) |
| [concept/rejected/vent-burst-r43-b.ogg](concept/rejected/vent-burst-r43-b.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `vent-burst` b: a fire burst roaring out; Fire - Effects - Swooshes - Bursts, evolving (GregorQuendel), 1.60 s (−20 dB) | rejected (round 43: not picked) |
| [concept/tank-tracks-r43-a.ogg](concept/tank-tracks-r43-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `tank-tracks` a: a T-26 tank driving, tracks and engine; t26_moving_01.wav (greatmganga), seamless 4.0 s loop (−32 dB) | chosen (round 43, S.f a: the user's pick) |
| [concept/rejected/tank-tracks-r43-b.ogg](concept/rejected/tank-tracks-r43-b.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `tank-tracks` b: a tracked bulldozer driving; tank bulldozer tracked vehicle sound effect atmo (Garuda1982), seamless 4.0 s loop (−32 dB) | rejected (round 43: not picked) |
| [concept/tank-turret-r43-a.ogg](concept/tank-turret-r43-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `tank-turret` a: a real artillery turret re-aligning; Tank or Tower Turret Moving and Turning (qubodup), 0.80 s (−23 dB) | chosen (round 43, S.g a: the user's pick) |
| [concept/rejected/tank-turret-r43-b.ogg](concept/rejected/tank-turret-r43-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `tank-turret` b: a factory overhead crane rolling; Factory_Crane_Roll.wav (mickyman5000), 0.90 s (−23 dB) | rejected (round 43: not picked) |
| [concept/tank-burst-r43-a.ogg](concept/tank-burst-r43-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `tank-burst` a: a tank cannon firing three shots 0.15 s apart; Tank Firing (qubodup), 1.18 s (−17 dB) | chosen (round 43, S.h a: the user's pick) |
| [concept/rejected/tank-burst-r43-b.ogg](concept/rejected/tank-burst-r43-b.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `tank-burst` b: a heavy artillery shot, three shots 0.15 s apart; Guns & Explosions Album - Autocannon - Shot 1.wav (OGsoundFX), 1.38 s (−18 dB) | rejected (round 43: not picked) |
| [concept/walker-vent-hit-r43-a.ogg](concept/walker-vent-hit-r43-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-vent-hit` a: a big hit on a lift door; Big stereo metal hit (LPA134), 0.55 s (−20 dB) | chosen (round 43, S.i a: the user's pick) |
| [concept/rejected/walker-vent-hit-r43-b.ogg](concept/rejected/walker-vent-hit-r43-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-vent-hit` b: a hollow metal impact with a burst of steam; Old_Truck_Metal_Squeaks-Creaks-Rattle-Impact_Hollow_Impact_Soft_Squeak (dilsun) + steam_burst.wav (pengo_au), 0.52 s (−19 dB) | rejected (round 43: not picked) |
| [concept/walker-death-r43-a.ogg](concept/walker-death-r43-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-death` a: a large explosion with debris, the hull landing with a massive impact and a metal crash; S17-28 Large explosion with debris.wav (craigsmith) + S10-13 Large hard metal crash; optical.wav (craigsmith) + Big Structure Collision Heard from Inside (Osvoldon), 5.60 s (−14 dB) | chosen (round 43, S.j a: the user's pick) |
| [concept/rejected/walker-death-r43-b.ogg](concept/rejected/walker-death-r43-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-death` b: two chained blasts, the final boom with a car crash's metal and a radiator rumbling; Huge Explosion (florianreichelt) + massive explosion (Hugofski) + Car Crash (with Glass) (magnuswaker) + Sounds For Earthquakes - Radiator Metal Rumbling.wav (RutgerMuller), 5.60 s (−15 dB) | rejected (round 43: not picked) |
| [concept/walker-wreck-settle-r43-a.ogg](concept/walker-wreck-settle-r43-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-wreck-settle` a: a bunker door's low metallic groan, slowed to 0.85, over an engine cooling; Metallic Groan (hinchinbrook) + Clicking Engine Cooldown (CmdRobot), 5.00 s (−24 dB) | chosen (round 43, S.k a: the user's pick) |
| [concept/rejected/walker-wreck-settle-r43-b.ogg](concept/rejected/walker-wreck-settle-r43-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-wreck-settle` b: a giant metal structure creaking, over an engine cooling; Creaking Metal - Slow (EagleStealthTeam) + Clicking Engine Cooldown (CmdRobot), 5.00 s (−24 dB) | rejected (round 43: not picked) |
| [concept/basin-rumble-r43-a.ogg](concept/basin-rumble-r43-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `basin-rumble` a: an earthquake's earth rumble, deep, with rock grinding in it; earth rumble.wav (Reitanna), 16.0 s loop (−28 LUFS) | chosen (round 43, S.l a: the user's pick) |
| [concept/rejected/basin-rumble-r43-b.ogg](concept/rejected/basin-rumble-r43-b.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `basin-rumble` b: an earthquake's rumble with creaks and groans, over a deep distant rumble; Earthquake_Rumble_Creaks_Groans_Dust.wav (Cell31_Sound_Productions) + EARTHQUAKE OR DISTANT SPACE SHUTTLE RUMBLE.WAV (metrostock99), 12.0 s loop (−28 LUFS) | rejected (round 43: not picked) |
| [concept/swarm-hum-r43-a.ogg](concept/swarm-hum-r43-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `swarm-hum` a: a designed swarm drone slowed to 0.85, over a bee box's hive hum; Swarm Drone.wav (deerlord) + In the bee box2- swarm of bees_stereo (Nordliecht), 6.0 s loop (−32 dB) | chosen (round 43, S.m a: the user's pick) |
| [concept/rejected/swarm-hum-r43-b.ogg](concept/rejected/swarm-hum-r43-b.ogg) | Freesound (CC-BY 3.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `swarm-hum` b: a large swarm of buzzing flies slowed to 0.8, over blowflies slowed to 0.9; Large Swarm of Buzzing Flies.wav (jakobthiesen) + Blowflies! (kangaroovindaloo), 6.0 s loop (−32 dB) | rejected (round 43: not picked) |
| [concept/swarm-lunge-r43-a.ogg](concept/swarm-lunge-r43-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `swarm-lunge` a: the swarm drone of hum a swelling in and gliding up, a powerful whoosh rushing in at its end; Swarm Drone.wav (deerlord) + Powerfull Whooshes 2.wav (Nic3_one), 0.80 s (−20 dB) | chosen (round 43, S.n a: the user's pick) |
| [concept/rejected/swarm-lunge-r43-b.ogg](concept/rejected/swarm-lunge-r43-b.ogg) | Freesound (CC-BY 3.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `swarm-lunge` b: the fly swarm of hum b swelling in and gliding up, a big swoosh rushing in at its end; Large Swarm of Buzzing Flies.wav (jakobthiesen) + Big Swoosh (Electroviolence), 0.80 s (−20 dB) | rejected (round 43: not picked) |
| [concept/colossus-bulge-r44-a.ogg](concept/colossus-bulge-r44-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `colossus-bulge` a: an earthquake's cracking soil and stone swelling in, sand pouring off the bulge; Cracking Earthquake (cracking soil, cracking stone) (uagadugu) + sand pour on sand FF662.aif (martinimeniscus), 2.10 s (−24 dB) | chosen (round 44, S.a a: the user's pick) |
| [concept/rejected/colossus-bulge-r44-b.ogg](concept/rejected/colossus-bulge-r44-b.ogg) | Freesound (CC-BY 4.0, CC-BY 3.0, [CREDITS.md](../../../CREDITS.md)) — `colossus-bulge` b: a large low rumble's crescendo with a heavy stone grind and sand flowing; Large, Low Rumble.wav (duckduckpony) + Guterton - Heavy Stone Grind (guterton.wav) + Flowing Sand (SteveMeli), 2.10 s (−22 dB) | rejected — round 44, S.a: a picked |
| [concept/colossus-eruption-r44-a.ogg](concept/colossus-eruption-r44-a.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `colossus-eruption` a: an earth impact's boom, a deep creature roar rising out of it, rock and sand pouring back; superhero_earth_impact_hit_07062025 (Artninja) + Deep Roar Echo 2.wav (noahpardo) + avl04.wav (blaukreuz), 2.60 s (−17 dB) | chosen (round 44, S.b a: the user's pick) |
| [concept/rejected/colossus-eruption-r44-b.ogg](concept/rejected/colossus-eruption-r44-b.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `colossus-eruption` b: a massive explosion's boom, a deep monster roar and a landslide's roar, sand pouring back; massive explosion (Hugofski) + Deep Monster Roar 2.wav (MisterSegev) + Landslide.wav (bikesnbassboi) + avl02.wav (blaukreuz) + sand pour on sand FF662.aif (martinimeniscus), 2.60 s (−20 dB) | rejected — round 44, S.b: a picked |
| [concept/colossus-suction-r44-a.ogg](concept/colossus-suction-r44-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `colossus-suction` a: air rushing into a chimney (a deep steady draw) under a sandstorm; fireplace air rush into chimney steady wind current hum.flac (kyles) + G56-22-Sandstorm.wav (craigsmith), seamless 4.0 s loop (−28 dB) | chosen (round 44, S.c a: the user's pick) |
| [concept/rejected/colossus-suction-r44-b.ogg](concept/rejected/colossus-suction-r44-b.ogg) | Freesound (CC0, CC-BY 3.0, [CREDITS.md](../../../CREDITS.md)) — `colossus-suction` b: a large industrial turbine's fan slowed to 0.8, a monster's inhale under it, sand flowing; motor industrial turbine large fan blades spin sorta.flac (kyles) + Monster Inhale.wav (ecfike) + Flowing Sand (SteveMeli), seamless 4.0 s loop (−28 dB) | rejected — round 44, S.c: a picked |
| [concept/colossus-bite-r44-a.ogg](concept/colossus-bite-r44-a.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `colossus-bite` a: a massive structure's deep impact with bones crunching, slowed to 0.6, and a large stone scraping; Big Structure Collision Heard from Inside (Osvoldon) + Horror / Zombie - Biting on bones (Breviceps) + Scraping and Clunking Large Stone 1.wav (F.M.Audio), 0.90 s (−19 dB) | chosen (round 44, S.d a: the user's pick) |
| [concept/rejected/colossus-bite-r44-b.ogg](concept/rejected/colossus-bite-r44-b.ogg) | Freesound (CC-BY 4.0, CC-BY 3.0, [CREDITS.md](../../../CREDITS.md)) — `colossus-bite` b: an earth impact's boom with a bone crack slowed to 0.55 and a heavy stone grind after it; superhero_earth_impact_hit_07062025 (Artninja) + Bone Crunch/Crack 2 (Mixed) (EvanBoyerman) + Guterton - Heavy Stone Grind (guterton.wav), 1.00 s (−20 dB) | rejected — round 44, S.d: a picked |
| [concept/colossus-death-r44-a.ogg](concept/colossus-death-r44-a.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `colossus-death` a: a deep roar shuddering, chained eruptions, then a rock avalanche's rumble subsiding; NPX Deep Roar Echo 1.wav (noahpardo) + S17-28 Large explosion with debris.wav (craigsmith) + superhero_earth_impact_hit_07062025 (Artninja) + massive explosion (Hugofski) + rock_avalanche_w_rumbling_sound_01092026 (Artninja), 6.10 s (−13 dB) | chosen (round 44, S.e a: the user's pick) |
| [concept/rejected/colossus-death-r44-b.ogg](concept/rejected/colossus-death-r44-b.ogg) | Freesound (CC-BY 4.0, CC0, CC-BY 3.0, [CREDITS.md](../../../CREDITS.md)) — `colossus-death` b: a deep monster roar, a building collapsing and two deep booms over a landslide, rock pouring in; Deep Monster Roar 2.wav (MisterSegev) + Deep Boom (Za-Games) + building_collapse04_enclosed_space.wav (onteca) + Landslide.wav (bikesnbassboi) + avl02.wav (blaukreuz), 6.00 s (−16 dB) | rejected — round 44, S.e: a picked |
| [concept/colossus-ridge-r44-a.ogg](concept/colossus-ridge-r44-a.ogg) | Freesound (CC0, CC-BY 3.0, [CREDITS.md](../../../CREDITS.md)) — `colossus-ridge` a: a subway train passing under a bridge, sand flowing off the ridge, panned left to right; Train, Subway, Pass By, Under Bridge Perspective (Kinoton) + Flowing Sand (SteveMeli), 7.00 s (−20 LUFS) | chosen (round 44, S.f a: the user's pick; the game plays a render with its channels swapped, right to left, for Level 21's track: `tools/concept/audio/sfx_r44.py --mirror colossus-ridge-r44-a`) |
| [concept/rejected/colossus-ridge-r44-b.ogg](concept/rejected/colossus-ridge-r44-b.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `colossus-ridge` b: a large low rumble passing, a heavy stone grind and sand pouring, panned right to left; Large, Low Rumble.wav (duckduckpony) + Guterton - Heavy Stone Grind (guterton.wav) + sand pour on sand FF662.aif (martinimeniscus), 7.00 s (−20 LUFS) | rejected — round 44, S.f: a picked |
| [concept/rejected/walker-lance-charge-r44-a.ogg](concept/rejected/walker-lance-charge-r44-a.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-lance-charge` a: a power charge rising, its pitch gliding up, a Tesla coil's sparking swelling under it; charging power (JavierZumer) + S16-18 Tesla coil sparking and humming.wav (craigsmith), 1.05 s (−20 dB) | rejected — round 44, S.g: b picked |
| [concept/walker-lance-charge-r44-b.ogg](concept/walker-lance-charge-r44-b.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `walker-lance-charge` b: a high-voltage discharge's buzz swelling and gliding up, a Tesla coil's sparks on top, a low rumble rising; high-voltage-discharge-2.wav (follytowers) + S16-18 Tesla coil sparking and humming.wav (craigsmith) + Large, Low Rumble.wav (duckduckpony), 1.05 s (−23 dB) | chosen (round 44, S.g b: the user's pick) |
| [concept/walker-lance-shot-r44-a.ogg](concept/walker-lance-shot-r44-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-lance-shot` a: a real railgun (an extremely loud cannon); Railgun - Extremely LOUD Cannon (qubodup), 1.60 s (−16 dB) | chosen (round 44, S.h a: the user's pick) |
| [concept/rejected/walker-lance-shot-r44-b.ogg](concept/rejected/walker-lance-shot-r44-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-lance-shot` b: a shipboard railgun's heavy boom with a high-voltage discharge's crack on its attack; shipboard_railgun.mp3 (deleted_user_1941307) + Taser/High Voltage discharge (The_Chemical_Workshop), 1.60 s (−18 dB) | rejected — round 44, S.h: a picked |
| [concept/walker-shockwave-r44-a.ogg](concept/walker-shockwave-r44-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-shockwave` a: a giant's footfall and an explosion's shockwave rolling out of it; Giant Walking.wav (facklere) + Explosion and Shockwave 1 (qubodup), 1.60 s (−19 dB) | chosen (round 44, S.i a: the user's pick) |
| [concept/rejected/walker-shockwave-r44-b.ogg](concept/rejected/walker-shockwave-r44-b.ogg) | Freesound (CC0, CC-BY 4.0, CC-BY 3.0, [CREDITS.md](../../../CREDITS.md)) — `walker-shockwave` b: a giant's step, a loud thump and a massive structure's deep impact rolling out, rubble shaken loose; Giant step (DasWaff) + loudthump.wav (777hr1) + Big Structure Collision Heard from Inside (Osvoldon) + building_collapse04_enclosed_space.wav (onteca), 1.48 s (−21 dB) | rejected — round 44, S.i: a picked |
| [concept/rejected/ghost-drone-drop-r46-a.ogg](concept/rejected/ghost-drone-drop-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `ghost-drone-drop` a: a jet's turbine whine passing, its speed gliding down from 1.3 to 0.8; Jet Flyby 02 (mrspivey), 0.92 s (−24 dB) | rejected — round 46, J.1: b picked |
| [concept/ghost-drone-drop-r46-b.ogg](concept/ghost-drone-drop-r46-b.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `ghost-drone-drop` b: a Eurofighter's close flyby, its speed gliding down from 1.25 to 0.8; Eurofighter Typhoon Flyby 005 – Close Proximity (TimoSchmied), 0.90 s (−24 dB) | chosen (round 46, J.1 b: the user's pick) |
| [concept/rejected/ghost-drone-glint-r46-a.ogg](concept/rejected/ghost-drone-glint-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `ghost-drone-glint` a: an electric relay latching (two dry clicks); Relay 2 (JoontheFloof), 0.23 s (−32 dB) | rejected — round 46, J.2: b picked |
| [concept/ghost-drone-glint-r46-b.ogg](concept/ghost-drone-glint-r46-b.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `ghost-drone-glint` b: a camera shutter's two clicks with a rifle's bolt release under them; Canon 550D Shutter (iainmccurdy) + M&P15 Bolt Release [No Ring] (smill.and.welson), 0.30 s (−26 dB) | chosen (round 46, J.2 b: the user's pick) |
| [concept/ghost-drone-burst-r46-a.ogg](concept/ghost-drone-burst-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `ghost-drone-burst` a: an M16's three-round burst (shots 0.14 s apart); M16 gun 3 round burst (lensflare8642), 0.60 s (−21 dB) | chosen (round 46, J.3 a: the user's pick) |
| [concept/rejected/ghost-drone-burst-r46-b.ogg](concept/rejected/ghost-drone-burst-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `ghost-drone-burst` b: an automatic rifle's three-shot burst (shots 0.1 s apart); Rifle Burst (qubodup), 0.29 s (−24 dB) | rejected — round 46, J.3: a picked |
| [concept/rejected/ghost-drone-death-r46-a.ogg](concept/rejected/ghost-drone-death-r46-a.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `ghost-drone-death` a: a dry bang and a heavy metal crunch, a burst of sparks and parts falling; Explosion 1 (Burning Car rec by Ñado) (qubodup) + Metal_heavy_punch_with_crunch_and_glass_debris_sounds_08242024 (Artninja) + Electric Sparks (mikiko850) + Parts falling (tlwmdbt), 0.90 s (−19 dB) | rejected — round 46, J.4: b picked |
| [concept/ghost-drone-death-r46-b.ogg](concept/ghost-drone-death-r46-b.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `ghost-drone-death` b: a metal crash, a real quadcopter hitting a wall and falling, smashed debris and sparks; Metal Crash Collision (qubodup) + Quadcopter Drone Collision Hitting Wall and Falling (qubodup) + QUICK_SMASH_DEBRIS_001.wav (JoelAudio) + Electric Sparks (mikiko850), 0.87 s (−19 dB) | chosen (round 46, J.4 b: the user's pick) |
| [concept/devil-spin-r46-a.ogg](concept/devil-spin-r46-a.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `devil-spin` a: inside a tornado (a steady whirling roar) with a sandstorm's grit; Inside a Tornado.wav (RogerBoyX69) + G56-22-Sandstorm.wav (craigsmith), 3.00 s, seamless 3 s loop (−32 dB) | chosen (round 46, J.5 a: the user's pick) |
| [concept/rejected/devil-spin-r46-b.ogg](concept/rejected/devil-spin-r46-b.ogg) | Freesound (CC0, CC-BY 3.0, [CREDITS.md](../../../CREDITS.md)) — `devil-spin` b: a tornado through a squeaking metal structure, sand flowing; JM_NATURAL ELEMENTS_Wind 01 - Tornado - Inside a metalic squeaking structure (Julien_Matthey) + Flowing Sand (SteveMeli), 3.00 s, seamless 3 s loop (−32 dB) | rejected — round 46, J.5: a picked |
| [concept/devil-open-r46-a.ogg](concept/devil-open-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `devil-open` a: a gust swelling in over a heavy stone door grinding open (its hollow resonance); Instant Wind (Raclure) + Heavy stone door opens (PostProdDog), 0.70 s (−21 dB) | chosen (round 46, J.6 a: the user's pick) |
| [concept/rejected/devil-open-r46-b.ogg](concept/rejected/devil-open-r46-b.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `devil-open` b: a powerful whoosh with stone dragging on stone; Powerfull Whooshes 2.wav (Nic3_one) + stone_on_stone.aif (thanvannispen), 0.70 s (−20 dB) | rejected — round 46, J.6: a picked |
| [concept/devil-spiral-r46-a.ogg](concept/devil-spiral-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `devil-spiral` a: a throw of dust with sand sliding off a shovel; OsVaDuThDustThrowThud.wav (The_Sean_) + Sand slidding out of shovel slowly.wav (XfiXy8), 0.47 s (−22 dB) | chosen (round 46, J.7 a: the user's pick) |
| [concept/rejected/devil-spiral-r46-b.ogg](concept/rejected/devil-spiral-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `devil-spiral` b: a big swoosh with sand pouring; Big Swoosh (Electroviolence) + sand pour on sand FF662.aif (martinimeniscus), 0.45 s (−22 dB) | rejected — round 46, J.7: a picked |
| [concept/rejected/shellback-walk-r46-a.ogg](concept/rejected/shellback-walk-r46-a.ogg) | Freesound (CC-BY 3.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `shellback-walk` a: a big creature's footsteps (two to a loop) with stones clacking; Dinosaur Footsteps-01.wav (jamesrodavidson) + stonehit1.wav (aerror), 3.26 s, seamless 3.26 s loop (−43 dB) | rejected — round 46, J.12: b picked |
| [concept/shellback-walk-r46-b.ogg](concept/shellback-walk-r46-b.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `shellback-walk` b: a monster's heavy stomps (two to a loop) with stone knocking on stone; Monster Stomp Footsteps Sequence (Vilkas_Sound) + stone_on_stone.aif (thanvannispen), 4.16 s, seamless 4.16 s loop (−34 dB) | chosen (round 46, J.12 b: the user's pick) |
| [concept/rejected/shellback-lob-r46-a.ogg](concept/rejected/shellback-lob-r46-a.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `shellback-lob` a: a real M224 mortar firing, slowed to 0.85, with a pressure hiss; M224 Mortar Firing Close 02 (Nanashi) + Pressure Hiss (magnuswaker), 0.62 s (−17 dB) | rejected — round 46, J.11: b picked |
| [concept/shellback-lob-r46-b.ogg](concept/shellback-lob-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `shellback-lob` b: a 120 mm mortar shot, slowed to 0.8, with a pressure hiss; 120mm Mortar Shell Shot 2 (qubodup) + Pressure Hiss (magnuswaker), 0.64 s (−21 dB) | chosen (round 46, J.11 b: the user's pick) |
| [concept/rejected/shellback-curl-r46-a.ogg](concept/rejected/shellback-curl-r46-a.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `shellback-curl` a: stone dragging on stone, closing on a heavy stone clack; stone_on_stone_dragging3.aif (thanvannispen) + stonehit1.wav (aerror), 0.80 s (−20 dB) | rejected — round 46, J.8: b picked |
| [concept/shellback-curl-r46-b.ogg](concept/shellback-curl-r46-b.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `shellback-curl` b: a heavy stone grind, closing on a stone-on-stone knock; Guterton - Heavy Stone Grind (guterton.wav) + stone_on_stone.aif (thanvannispen), 0.85 s (−20 dB) | chosen (round 46, J.8 b: the user's pick) |
| [concept/rejected/shellback-roll-r46-a.ogg](concept/rejected/shellback-roll-r46-a.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `shellback-roll` a: a boulder rolling with gravel falling; Rolling boulder (gootie3333333) + Gravel Impacts and Falls.wav (lolamadeus), 2.50 s, seamless 2.5 s loop (−30 dB) | rejected — round 46, J.10: b picked |
| [concept/shellback-roll-r46-b.ogg](concept/shellback-roll-r46-b.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `shellback-roll` b: a large low rumble with stone dragging on stone; Large, Low Rumble.wav (duckduckpony) + stone_on_stone_dragging3.aif (thanvannispen), 2.50 s, seamless 2.5 s loop (−31 dB) | chosen (round 46, J.10 b: the user's pick) |
| [concept/rejected/shellback-uncurl-r46-a.ogg](concept/rejected/shellback-uncurl-r46-a.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `shellback-uncurl` a: stone dragging open, a loose stone clack and sand pouring off; stone_on_stone_dragging3.aif (thanvannispen) + stonehit1.wav (aerror) + sand pour on sand FF662.aif (martinimeniscus), 0.70 s (−22 dB) | rejected — round 46, J.9: b picked |
| [concept/shellback-uncurl-r46-b.ogg](concept/shellback-uncurl-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `shellback-uncurl` b: a heavy stone door grinding open, a stone clack and sand sliding off; Heavy stone door opens (PostProdDog) + stonehit1.wav (aerror) + Sand slidding out of shovel slowly.wav (XfiXy8), 0.70 s (−24 dB) | chosen (round 46, J.9 b: the user's pick) |
| [concept/crawler-walk-r46-a.ogg](concept/crawler-walk-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `crawler-walk` a: fingers tapping like bug legs (a dense skitter) over gravel; bug legs tapping (c64cosmin) + Gravel Impacts and Falls.wav (lolamadeus), 3.00 s, seamless 3 s loop (−36 dB) | chosen (round 46, J.13 a: the user's pick) |
| [concept/rejected/crawler-walk-r46-b.ogg](concept/rejected/crawler-walk-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `crawler-walk` b: a crab walking, slowed to 0.85, over the tapping legs; crab walking (stuniverso) + bug legs tapping (c64cosmin), 3.00 s, seamless 3 s loop (−36 dB) | rejected — round 46, J.13: a picked |
| [concept/rejected/tendril-hum-r46-a.ogg](concept/rejected/tendril-hum-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `tendril-hum` a: an electric transformer's hum in a large room with high-voltage lines buzzing; hum electric transformer in large room1.flac (kyles) + 765 kilo-volt transmission lines buzzing and crackling (johnaudiotech), 6.00 s, seamless 6 s loop (−38 dB) | rejected — round 46, J.14: redo, the user: "Keep the old one, it's fine" (the old tendril hum stays; both r46 variants rejected) |
| [concept/rejected/tendril-hum-r46-b.ogg](concept/rejected/tendril-hum-r46-b.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `tendril-hum` b: a power pylon's buzz with high-voltage lines crackling; Electric_Pylon_Power Line-Buzz 01.wav (omnisounddesign) + 765 kilo-volt transmission lines buzzing and crackling (johnaudiotech), 6.00 s, seamless 6 s loop (−38 dB) | rejected — round 46, J.14: redo, the user: "Keep the old one, it's fine" (the old tendril hum stays; both r46 variants rejected) |
| [concept/tendril-burn-r46-a.ogg](concept/tendril-burn-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `tendril-burn` a: an electric welder's crackle; Welding_2_long_take.wav (ivolipa), 2.00 s, seamless 2 s loop (−34 dB) | chosen (round 46, J.15 a: the user's pick) |
| [concept/rejected/tendril-burn-r46-b.ogg](concept/rejected/tendril-burn-r46-b.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `tendril-burn` b: a TIG welder's hiss with electric sparks; Electric welding with tig - Full cycle (ldezem) + Electric Sparks (mikiko850), 2.00 s, seamless 2 s loop (−34 dB) | rejected — round 46, J.15: a picked |
| [concept/rejected/tendril-break-r46-a.ogg](concept/rejected/tendril-break-r46-a.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `tendril-break` a: a whip crack and a motor winding down, its speed gliding from 1.4 to 0.6; Whip Crack 01.wav (CGEffex) + Wind-down 1.aif (timgormly), 0.90 s (−19 dB) | rejected — round 46, J.16: b picked |
| [concept/tendril-break-r46-b.ogg](concept/tendril-break-r46-b.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `tendril-break` b: a cable snapping with a whip crack, a fan slowing to a stop, its speed gliding from 2 to 0.8; 24_SeRompeCable.wav (Lextao) + Whip Crack.m4a (SciFiSounds) + FAN 3 (Slow Stop).wav (temawas), 0.92 s (−18 dB) | chosen (round 46, J.16 b: the user's pick) |
| [concept/burrow-dive-r46-a.ogg](concept/burrow-dive-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `burrow-dive` a: sand sliding off a shovel, its speed gliding down from 1 to 0.75; Sand slidding out of shovel slowly.wav (XfiXy8), 0.55 s (−26 dB) | chosen (round 46, J.17 a: the user's pick) |
| [concept/rejected/burrow-dive-r46-b.ogg](concept/rejected/burrow-dive-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `burrow-dive` b: sand sliding on metal, gliding down, with sand pouring; sand slide on metal FF678.aif (martinimeniscus) + sand pour on sand FF662.aif (martinimeniscus), 0.55 s (−26 dB) | rejected — round 46, J.17: a picked |
| [concept/rejected/herald-song-r46-a.ogg](concept/rejected/herald-song-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `herald-song` a: a short choir's 'aah', slowed to 0.85 (darker); Short Choir (Breviceps), 1.90 s (−20 dB) | rejected — round 46, J.18: redo, the user: "Also explain what this is supposed to sound like" |
| [concept/rejected/herald-song-r46-b.ogg](concept/rejected/herald-song-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `herald-song` b: a choir's chord changing, a low throat chant under it; Choir.aif (DiArchangeli) + Kargyraa Throat Chant/Singing (Lower Register Vocal Folds) (Hjaarlmarch), 1.90 s (−20 dB) | rejected — round 46, J.18: redo, the user: "Also explain what this is supposed to sound like" |
| [concept/rejected/herald-aura-r46-a.ogg](concept/rejected/herald-aura-r46-a.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `herald-aura` a: an ethereal choir drone, high-passed at 1.5 kHz; 26-07-12 - Ethereal Choir Drone - ambient pad (Valerie-Vivegnis), 4.00 s, seamless 4 s loop (−40 dB) | rejected — round 46, J.19: b picked |
| [concept/herald-aura-r46-b.ogg](concept/herald-aura-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `herald-aura` b: a choir's held chord, high-passed at 1.5 kHz; Choir.aif (DiArchangeli), 2.60 s, seamless 2.6 s loop (−40 dB) | chosen (round 46, J.19 b: the user's pick) |
| [concept/rejected/aura-absorb-r46-a.ogg](concept/rejected/aura-absorb-r46-a.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `aura-absorb` a: a burst of electric sparks over a muffled metal punch; Electric Sparks (mikiko850) + Metal_heavy_punch_with_crunch_and_glass_debris_sounds_08242024 (Artninja), 0.35 s (−22 dB) | rejected — round 46, J.20: b picked |
| [concept/aura-absorb-r46-b.ogg](concept/aura-absorb-r46-b.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `aura-absorb` b: a force field's hit with a crackle of sparks over a muffled punch; Force Field 01.wav (LilMati) + Electric Sparks (mikiko850) + Metal_heavy_punch_with_crunch_and_glass_debris_sounds_08242024 (Artninja), 0.40 s (−22 dB) | chosen (round 46, J.20 b: the user's pick) |
| [concept/walker-stomp-r46-a.ogg](concept/walker-stomp-r46-a.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `walker-stomp` a: a big robot's footstep with a bus door's hydraulic hiss; Big robot footstep 002 (AudioPapkin) + Hydraulic Hiss, TTC bus door (OttisG), 1.00 s (−20 dB) | chosen (round 46, J.21 a: the user's pick) |
| [concept/rejected/walker-stomp-r46-b.ogg](concept/rejected/walker-stomp-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `walker-stomp` b: a mech's step and its servo, a pressure hiss; mech_step_002.ogg (pgi) + Pressure Hiss (magnuswaker), 1.00 s (−22 dB) | rejected — round 46, J.21: a picked |
| [concept/tank-death-r46-a.ogg](concept/tank-death-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `tank-death` a: a real military explosive, a large metal vehicle crashing and a flame burst; Explosive 1 v2 [DOD 130303].flac (qubodup) + R29-28-Large Metal Vehicle Crashes.wav (craigsmith) + Flame Burst (magnuswaker), 1.80 s (−17 dB) | chosen (round 46, J.22 a: the user's pick) |
| [concept/rejected/tank-death-r46-b.ogg](concept/rejected/tank-death-r46-b.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `tank-death` b: a large explosion with debris, a metal crash, fire roaring out, metal wreckage; S17-28 Large explosion with debris.wav (craigsmith) + Metal Crash Collision (qubodup) + Fire - Effects - Swooshes - Bursts, evolving (GregorQuendel) + R29-28-Large Metal Vehicle Crashes.wav (craigsmith), 1.80 s (−17 dB) | rejected — round 46, J.22: a picked |
| [concept/canopy-hum-r46-a.ogg](concept/canopy-hum-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `canopy-hum` a: a deep time-stretched voice drone (the living canopy), slowed to 0.85, over a large basement's deep furnace hum; Dark Voice Drone 02 - Loop (kkenny101) + room tone large room church basement heavy deep ventilation or furnace hum quiet and clicking that picks up in middle.flac (kyles), seamless 16.0 s loop (−48 dB band RMS, −31 LUFS) | chosen (round 46, K.1 a: the user's pick) |
| [concept/rejected/canopy-hum-r46-b.ogg](concept/rejected/canopy-hum-r46-b.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `canopy-hum` b: wind humming and howling in power lines, slowed to 0.7, over a transformer's hum in a large room; Tuuli sähkölangoissa / Wind humming and howling in the electric wires, power line (YleArkisto) + hum electric transformer in large room1.flac (kyles), seamless 16.0 s loop (−39 dB band RMS, −31 LUFS) | rejected — round 46, K.1: a picked |
| [concept/mars-canyon-r46-a.ogg](concept/mars-canyon-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `mars-canyon` a: a low wind in a desert canyon with a moaning whistling wind howling through it; Low wind in a desert canyon 2 (fran_marenco) + G55-12-Moaning Whistle Wind.wav (craigsmith), seamless 16.0 s loop (−36 dB band RMS, −29 LUFS) | chosen (round 46, K.2 a: the user's pick) |
| [concept/rejected/mars-canyon-r46-b.ogg](concept/rejected/mars-canyon-r46-b.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `mars-canyon` b: a howling wind over a sandstorm's grit; howling wind.WAV (inchadney) + G56-22-Sandstorm.wav (craigsmith), seamless 16.0 s loop (−36 dB band RMS, −31 LUFS) | rejected — round 46, K.2: a picked |
| [concept/rejected/mars-colony-r46-a.ogg](concept/rejected/mars-colony-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `mars-colony` a: an open plain's wind, an underground ventilation plant's hum and a distant freight train clicking by; Pink Lakes Wind Atmos #2 (kangaroovindaloo) + Arena Underground Ventilation Hum and Rumble (jamesdrake89) + freight train distant short and clicky.flac (kyles), seamless 16.0 s loop (−36 dB band RMS, −32 LUFS) | rejected — round 46, K.3: b picked |
| [concept/mars-colony-r46-b.ogg](concept/mars-colony-r46-b.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `mars-colony` b: a thin sand wind, an industrial ventilation hum and a distant freight train's steel clanking; G56-22-Sandstorm.wav (craigsmith) + Ambient Industrial Ventilation Soundscape (amenhotepIV) + Shunting Atmos (kangaroovindaloo), seamless 16.0 s loop (−36 dB band RMS, −32 LUFS) | chosen (round 46, K.3 b: the user's pick) |
| [concept/rejected/mars-foundry-r46-a.ogg](concept/rejected/mars-foundry-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `mars-foundry` a: a foundry furnace's roar, a factory's overhead crane rumbling (the conveyors), a far heavy hammer every 4 s and a low wind; Foundry Furnace.wav (Walter_Odington) + G16-09-Factory-Overhead Crane.wav (craigsmith) + Low wind in a desert canyon 2 (fran_marenco) + Big robot footstep 002 (AudioPapkin), seamless 16.0 s loop (−38 dB band RMS, −28 LUFS) | rejected — round 46, K.4: b picked |
| [concept/mars-foundry-r46-b.ogg](concept/mars-foundry-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `mars-foundry` b: a foundry's furnace, a big industrial fan (the blowers), a steel mill's deep drone, a low wind and a far giant hammer every 8 s; Metalurgists At The Foundary.wav (Walter_Odington) + MACHFan_big_industrial_fan_white_noise_Olympus_LS-P4.wav (DBS_Sounds) + Steel mill low frequency drone (sarana) + Low wind in a desert canyon 2 (fran_marenco) + Giant step (DasWaff), seamless 16.0 s loop (−40 dB band RMS, −28 LUFS) | chosen (round 46, K.4 b: the user's pick) |
| [concept/rejected/enemy-leviathan-cry-r46-a.ogg](concept/rejected/enemy-leviathan-cry-r46-a.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `enemy-leviathan-cry` a: a blue whale's B call (a deep falling moan), slowed to 0.9, over an underwater rumble; Blue Whale B call @ 5X (MBARI_MARS) + Underwater Rumble (EminYILDIRIM), 3.40 s (−24 dB) | rejected — round 46, K.5: redo, the user: "Neither options sound like a whale cry. Probably too low quality" |
| [concept/rejected/enemy-leviathan-cry-r46-b.ogg](concept/rejected/enemy-leviathan-cry-r46-b.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `enemy-leviathan-cry` b: two humpback whale calls, slowed to 0.5 (deep and mournful); Underwater recording of Humpback whales and their calves (alanmcki), 3.40 s (−24 dB) | rejected — round 46, K.5: redo, the user: "Neither options sound like a whale cry. Probably too low quality" |
| [concept/rejected/driftjelly-pulse-r46-a.ogg](concept/rejected/driftjelly-pulse-r46-a.ogg) | Freesound (CC-BY 3.0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `driftjelly-pulse` a: a muffled underwater impact over an underwater rumble; Underwater Impact (deleted_user_7709760) + Underwater Rumble (EminYILDIRIM), 0.90 s (−27 dB) | rejected — round 46, K.6: b picked |
| [concept/driftjelly-pulse-r46-b.ogg](concept/driftjelly-pulse-r46-b.ogg) | Freesound (CC-BY 3.0, [CREDITS.md](../../../CREDITS.md)) — `driftjelly-pulse` b: a massive structure bending (a deep groan), slowed to 0.8, with the underwater impact's tail; Massive Structure Bend (Kostas17) + Underwater Impact (deleted_user_7709760), 0.90 s (−30 dB) | chosen (round 46, K.6 b: the user's pick) |
| [concept/rejected/lamprey-drain-r46-a.ogg](concept/rejected/lamprey-drain-r46-a.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `lamprey-drain` a: a Jacob's ladder's buzzing arc slowed to 0.6, over a transformer's hum; JacobsLadderLong1.flac (Halleck) + hum electric transformer in large room1.flac (kyles), seamless 2.40 s loop (−24 dB) | rejected — round 46, K.7: b picked |
| [concept/lamprey-drain-r46-b.ogg](concept/lamprey-drain-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `lamprey-drain` b: a Tesla coil sparking and humming, slowed to 0.7, over a loose light fixture's hum and crackle; S16-18 Tesla coil sparking and humming.wav (craigsmith) + ELECMisc-INT_NOISES-Electrical Hums And Crackles Of A Light Fixture With A Loose Light Bulb_YMLT_NONE_08982261 (YouMightLikeThis), seamless 2.40 s loop (−24 dB) | chosen (round 46, K.7 b: the user's pick) |
| [concept/sonde-chime-r46-a.ogg](concept/sonde-chime-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `sonde-chime` a: a heavy mechanical clamp slamming shut with a radio squelch (the telemetry handed over); no bell; MechanicalClamp.wav (Skullsmasha) + Radio Sign Off / Squelch (JovianSounds), 1.00 s (−17 dB) | chosen (round 46, K.8 a: the user's pick) |
| [concept/rejected/sonde-chime-r46-b.ogg](concept/rejected/sonde-chime-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `sonde-chime` b: a heavy dungeon door's latch clanking shut with a radio squelch; no bell; metal heavy dungeon prison door unlatch open close thud rattle and hatch open close slide squeak various int perspective on offmic.wav (kyles) + Squelch.mp3 (LucasDuff), 0.90 s (−16 dB) | rejected — round 46, K.8: a picked |
| [concept/rejected/integrity-alarm-r46-a.ogg](concept/rejected/integrity-alarm-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `integrity-alarm` a: a navy ship's danger whistle siren (a real one); S29-08 Navy ship classic danger whistle siren.wav (craigsmith), 1.70 s (−17 dB) | rejected — round 46, K.9: redo, the user: "Tell what this is needed for." |
| [concept/rejected/integrity-alarm-r46-b.ogg](concept/rejected/integrity-alarm-r46-b.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `integrity-alarm` b: a submarine's dive horn, two blasts (a real one); Submarie dive horn_better.wav (CGEffex), 1.85 s (−17 dB) | rejected — round 46, K.9: redo, the user: "Tell what this is needed for." |
| [concept/drone-launch-r46-a.ogg](concept/drone-launch-r46-a.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `drone-launch` a: a drone taking off, slowed to 0.75 (bigger rotors); Drone take off (GeorgeHopkins), 2.20 s (−21 dB) | chosen (round 46, K.10 a: the user's pick) |
| [concept/rejected/drone-launch-r46-b.ogg](concept/rejected/drone-launch-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `drone-launch` b: a quadcopter's rotors starting and taking off, slowed to 0.75; Dji Mavic Mini 2 take-off UP-DOWN XY mic placement Zoom H5.wav (Sadiquecat), 2.20 s (−21 dB) | rejected — round 46, K.10: a picked |
| [concept/rejected/dome-hit-r46-a.ogg](concept/rejected/dome-hit-r46-a.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `dome-hit` a: a bang on thick glass with a heavy thud behind it, a glass door's creak and ice cracking (the stress lines); no shatter; BANGING ON THICK GLASS.wav (trexlasso) + Thuds on Window (zerolagtime) + Door Close Heavy Metal Glass Slow Creak Seal Theatre (LampEight) + Ice Crack 3 (j_p_higgins), 1.20 s (−26 dB) | rejected — round 46, K.11: redo, the user: "Neither sound like glass being hit. Glass being hit is a sharp sound, not a thud." |
| [concept/rejected/dome-hit-r46-b.ogg](concept/rejected/dome-hit-r46-b.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `dome-hit` b: a heavy thud on a window, a heavy glass door's creak and a frozen lake's fine cracking; no shatter; Thuds on Window (zerolagtime) + Door Close Heavy Metal Glass Medium Creak Seal Theatre (LampEight) + Ice Crack 4 (j_p_higgins) + BANGING ON THICK GLASS.wav (trexlasso), 1.20 s (−27 dB) | rejected — round 46, K.11: redo, the user: "Neither sound like glass being hit. Glass being hit is a sharp sound, not a thud." |
| [concept/rejected/dash-r46-a.ogg](concept/rejected/dash-r46-a.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `dash` a: a compressed-air blast with a jet thruster's bassy roar; Tire Bead Seater Air Blast (j_soundeffects) + jet thruster bassy potential.aiff (kyles), 0.40 s (−21 dB) | rejected — round 46, K.12: b picked |
| [concept/dash-r46-b.ogg](concept/dash-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `dash` b: an afterburner's roar, sped to 1.2, with a gas release's hiss; Afterburner (Close, Screamer, Quick Onset) (JSK_Productions) + Gas Release (adgawrhbshbffsfgvsrf), 0.40 s (−21 dB) | chosen (round 46, K.12 b: the user's pick) |
| [concept/rejected/overcharge-whoosh-r46-a.ogg](concept/rejected/overcharge-whoosh-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `overcharge-whoosh` a: a camera flash charging, its whine gliding up, a powerful whoosh at its end; Power Charge (crashoverride6) + Powerfull Whooshes 2.wav (Nic3_one), 1.00 s (−20 dB) | rejected — round 46, K.13: b picked |
| [concept/overcharge-whoosh-r46-b.ogg](concept/overcharge-whoosh-r46-b.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `overcharge-whoosh` b: a phasing power-up gliding up with a Tesla coil's sparks swelling under it; Electronic_Powerup.wav (StephenSaldanha) + S16-18 Tesla coil sparking and humming.wav (craigsmith), 1.00 s (−20 dB) | chosen (round 46, K.13 b: the user's pick) |
| [concept/rejected/overcharge-end-r46-a.ogg](concept/rejected/overcharge-end-r46-a.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `overcharge-end` a: the camera flash's charge whine gliding down and fading; Power Charge (crashoverride6), 0.90 s (−24 dB) | rejected — round 46, K.14: b picked |
| [concept/overcharge-end-r46-b.ogg](concept/overcharge-end-r46-b.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `overcharge-end` b: the phasing power-up gliding down with the Tesla coil's sparks dying; Electronic_Powerup.wav (StephenSaldanha) + S16-18 Tesla coil sparking and humming.wav (craigsmith), 0.90 s (−25 dB) | chosen (round 46, K.14 b: the user's pick) |
| [concept/rejected/emp-ring-r46-a.ogg](concept/rejected/emp-ring-r46-a.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `emp-ring` a: a Jacob's ladder's arc buzzing out, a Tesla coil's sparks and a massive structure's deep impact; JacobsLadderLong1.flac (Halleck) + S16-18 Tesla coil sparking and humming.wav (craigsmith) + Big Structure Collision Heard from Inside (Osvoldon), 1.35 s (−20 dB) | rejected — round 46, K.15: b picked |
| [concept/emp-ring-r46-b.ogg](concept/emp-ring-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `emp-ring` b: a high-voltage discharge with a taser's crack on its attack over a deep boom; high-voltage-discharge-2.wav (follytowers) + Taser/High Voltage discharge (The_Chemical_Workshop) + Deep Boom (Za-Games), 1.35 s (−25 dB) | chosen (round 46, K.15 b: the user's pick) |
| [concept/emp-powerdown-r46-a.ogg](concept/emp-powerdown-r46-a.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `emp-powerdown` a: a large machine powering down, played fast and gliding down from 3x to 1x; Large machine running and powering down (ryks88), 0.90 s (−24 dB) | chosen (round 46, K.16 a: the user's pick) |
| [concept/rejected/emp-powerdown-r46-b.ogg](concept/rejected/emp-powerdown-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `emp-powerdown` b: a transformer's hum sagging (gliding 1.0 to 0.6) with a loose light fixture's flickering crackle; hum electric transformer in large room1.flac (kyles) + ELECMisc-INT_NOISES-Electrical Hums And Crackles Of A Light Fixture With A Loose Light Bulb_YMLT_NONE_08982261 (YouMightLikeThis), 0.90 s (−24 dB) | rejected — round 46, K.16: a picked |
| [concept/ion-beam-stop-r46-a.ogg](concept/ion-beam-stop-r46-a.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `ion-beam-stop` a: a Tesla coil's hum and a large machine gliding down together (1.0 to 0.35); S16-18 Tesla coil sparking and humming.wav (craigsmith) + Large machine running and powering down (ryks88), 0.64 s (−26 dB) | chosen (round 46, K.17 a: the user's pick) |
| [concept/rejected/ion-beam-stop-r46-b.ogg](concept/rejected/ion-beam-stop-r46-b.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `ion-beam-stop` b: a Jacob's ladder's arc buzz and a transformer's hum gliding down to nothing; JacobsLadderLong1.flac (Halleck) + hum electric transformer in large room1.flac (kyles), 0.62 s (−26 dB) | rejected — round 46, K.17: a picked |
| [concept/beam-impact-r46-a.ogg](concept/beam-impact-r46-a.ogg) | Freesound (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `beam-impact` a: arc welding: a dry crackling sizzle over its 120 Hz buzz; Arc Welding.wav (kev_durr), seamless 1.50 s loop (−40 dB) | chosen (round 46, K.18 a: the user's pick) |
| [concept/rejected/beam-impact-r46-b.ogg](concept/rejected/beam-impact-r46-b.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `beam-impact` b: MIG welding over a Jacob's ladder's arc buzz slowed to 0.6; MIG Welding (unfa) + JacobsLadderLong1.flac (Halleck), seamless 1.50 s loop (−38 dB) | rejected — round 46, K.18: a picked |
| [concept/drone-rebuild-r46-a.ogg](concept/drone-rebuild-r46-a.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `drone-rebuild` a: an electric turret motor whirring up (gliding 0.8 to 1.2) into a rifle bolt's metal clack; Rotating tank turret planetary electric motor (lorefold) + M&P15 Bolt Release [No Ring] (smill.and.welson), 0.60 s (−25 dB) | chosen (round 46, K.19 a: the user's pick) |
| [concept/rejected/drone-rebuild-r46-b.ogg](concept/rejected/drone-rebuild-r46-b.ogg) | Freesound (CC0, [CREDITS.md](../../../CREDITS.md)) — `drone-rebuild` b: a sci-fi mechanical door's servo and clunk; Sci-Fi Mechanical Door (CheatinSloth), 0.60 s (−25 dB) | rejected — round 46, K.19: a picked |
| [concept/rejected/launch-rail-r46-a.ogg](concept/rejected/launch-rail-r46-a.ogg) | Freesound (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `launch-rail` a: a pressure hiss building and a fighter jet's close pass (the shuttle's run) into a large metal door slamming (the buffer) and a steam safety valve venting; Air (or steam) pressure release (brunoboselli) + Eurofighter Typhoon Flyby 005 – Close Proximity (TimoSchmied) + Closing large metal door-001 (AudioPapkin) + steam_safety_valve.wav (Engineer_815), 4.90 s (−21 dB) | rejected — round 46, K.20: redo, the user: "Neither sounds like a launch. Maybe look for aircraft carrier launch sounds." |
| [concept/rejected/launch-rail-r46-b.ogg](concept/rejected/launch-rail-r46-b.ogg) | Freesound (CC-BY 4.0, CC0, [CREDITS.md](../../../CREDITS.md)) — `launch-rail` b: a hydraulic hiss, a subway train rushing past under a bridge (the shuttle's run), a bunker's heavy metal gate slamming (the buffer) and gas and steam escaping; Hydraulic Hiss, TTC bus door (OttisG) + Train, Subway, Pass By, Under Bridge Perspective (Kinoton) + Heavy Gate Metal 01 (underground bunker of civil defense, Vsetin City, Club Vesmir) (ALEFF-ATMOS) + MrM_Gas_Steam_Escape.wav (mrmccormack), 4.90 s (−21 dB) | rejected — round 46, K.20: redo, the user: "Neither sounds like a launch. Maybe look for aircraft carrier launch sounds." |
| [concept/herald-song-r47-a.ogg](concept/herald-song-r47-a.ogg) | Freesound HQ preview (CC0, [CREDITS.md](../../../CREDITS.md)) — `herald-song` a: plays at the Choir Herald's song (`Sfx.HERALD_SONG`), a one-shot with each 10-bullet ring the Herald fires (every 4 s, 3.2 s on hard) while it lives, Levels 17, 19 and 21; should sound like the Vrell Choir's voice: eerie and inhuman, several voices in a slow drifting chord. This one: a concert-hall choir's chord swelling in a crescendo while its voices slide upward, slowed to 0.7 (deeper and slower, the slide drags), with throat singing in a cave under it slowed to 0.85: its whistling overtones drift above the drone, which no human choir does; Vocal_crescendo.wav (cellokratzer) + Throat Singing in a Cave (ItsThatRoboGuy), 2.40 s (−20 dB) | chosen (round 47, L.1 a: the user's pick; still the preview cut, see the note under Open questions) |
| [concept/rejected/herald-song-r47-b.ogg](concept/rejected/herald-song-r47-b.ogg) | Freesound HQ preview (CC-BY 3.0, [CREDITS.md](../../../CREDITS.md)) — `herald-song` b: plays at the Choir Herald's song (`Sfx.HERALD_SONG`), a one-shot with each 10-bullet ring the Herald fires (every 4 s, 3.2 s on hard) while it lives, Levels 17, 19 and 21; should sound like the Vrell Choir's voice: eerie and inhuman, several voices in a slow drifting chord. This one: a chamber choir's held 'oh' chord played twice at once, slowed to about 0.65 and a semitone apart, both sagging slowly in pitch: two choirs out of tune with each other, beating and smeared, a chord that slides downward and never settles; chamber_choir_chord_o.wav (uair01), 2.40 s (−20 dB) | rejected — round 47, L.1: a picked |
| [concept/rejected/enemy-leviathan-cry-r47-a.ogg](concept/rejected/enemy-leviathan-cry-r47-a.ogg) | Freesound HQ preview (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `enemy-leviathan-cry` a: plays at the Leviathan's death cry (Level 03), under its large burst when it dies; should sound like a whale's cry: deep, long and mournful. This one: a humpback whale's moan from a clean 3D hydrophone recording: a cry that falls and settles on a long low tone with its overtones, slowed to 0.85 (a bigger animal); humpback_whales_and_orcas_ambient_underwater_recording (raiden04), 4.00 s (−24 dB) | rejected — round 47, L.2: b picked |
| [concept/enemy-leviathan-cry-r47-b.ogg](concept/enemy-leviathan-cry-r47-b.ogg) | Freesound HQ preview (CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `enemy-leviathan-cry` b: plays at the Leviathan's death cry (Level 03), under its large burst when it dies; should sound like a whale's cry: deep, long and mournful. This one: another humpback moan from the same recording: it starts high, sinks into a long deep groan and lifts a little at its end, slowed to 0.8 (deeper); humpback_whales_and_orcas_ambient_underwater_recording (raiden04), 4.00 s (−27 dB) | chosen (round 47, L.2 b: the user's pick; still the preview cut, see the note under Open questions) |
| [concept/rejected/integrity-alarm-r47-a.ogg](concept/rejected/integrity-alarm-r47-a.ogg) | Freesound HQ preview (CC0, [CREDITS.md](../../../CREDITS.md)) — `integrity-alarm` a: plays at an ally or an object the player protects (Level 13's relay, Level 17's domes) dropping below 50 % and again below 25 % of its health: the cockpit's warning that it is in danger; should sound like a short urgent alarm, two tones, about 1 s, repeating. This one: a real submarine's submersion alarm, a harsh buzzing horn, cut into a high-low call (the low a third under, by slowing it to 0.84), twice: 'BAAP-baap, BAAP-baap', 2 s; Submarine submersion alarm_mono_wave.wav (Guialgarve), 1.95 s (−17 dB) | rejected — round 47, L.3: neither; the old one stays (the user: "The old one is fine for that.") |
| [concept/rejected/integrity-alarm-r47-b.ogg](concept/rejected/integrity-alarm-r47-b.ogg) | Freesound HQ preview (CC0, [CREDITS.md](../../../CREDITS.md)) — `integrity-alarm` b: plays at an ally or an object the player protects (Level 13's relay, Level 17's domes) dropping below 50 % and again below 25 % of its health: the cockpit's warning that it is in danger; should sound like a short urgent alarm, two tones, about 1 s, repeating. This one: a real mains-driven klaxon hooter (the old 'ah-oo-gah' horn's steady blare), cut into a high-low call (the low slowed to 0.8), twice: rougher and more nasal than a, 2 s; Klaxon on axis Long.wav (jameswrowles), 1.95 s (−17 dB) | rejected — round 47, L.3: neither; the old one stays (the user: "The old one is fine for that.") |
| [concept/rejected/dome-hit-r47-a.ogg](concept/rejected/dome-hit-r47-a.ogg) | Freesound HQ preview (CC0, [CREDITS.md](../../../CREDITS.md)) — `dome-hit` a: plays at an enemy shot hitting a Level 17 colony dome (`ALLY_HIT`), a greenhouse of thick glass; should sound like glass being hit: sharp, a hard knock with a bright crack, maybe a faint stress creak; not a thud. This one: a hard glass impact (a metal object on glass, with cracks forming), the ring of a window pane under it and a sharp glass crack 0.1 s later: bright and hard, 0.9 s; Hard Glass Impact (deleted_user_3656686) + hit - glass - window 01.wav (Anthousai) + Crack_glass.wav (13FPanska_Cerny_Jan), 0.90 s (−22 dB) | rejected — round 47, L.4: redo, the user: "These are too small. a is a bottle cracking, b is nothing like glass breaking. A dome is a very large structure." |
| [concept/rejected/dome-hit-r47-b.ogg](concept/rejected/dome-hit-r47-b.ogg) | Freesound HQ preview (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `dome-hit` b: plays at an enemy shot hitting a Level 17 colony dome (`ALLY_HIT`), a greenhouse of thick glass; should sound like glass being hit: sharp, a hard knock with a bright crack, maybe a faint stress creak; not a thud. This one: a window struck hard: the knock and the pane ringing, a second hard tap's ring on it, and a stress crack in the glass right after (a cooling glass cracking): a glassy 'tock' with a ring, 1 s; hit - glass - window 01.wav (Anthousai) + Tapping on window - 3 types.WAV (bpwebster) + glass_kappa_03.wav (mugwood), 1.00 s (−21 dB) | rejected — round 47, L.4: redo, the user: "These are too small. a is a bottle cracking, b is nothing like glass breaking. A dome is a very large structure." |
| [concept/rejected/launch-rail-r47-a.ogg](concept/rejected/launch-rail-r47-a.ogg) | Freesound HQ preview (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `launch-rail` a: plays at every level start (4.9 s): the ship is catapulted off the carrier; the shuttle stops at 3.6 s; should sound like an aircraft-carrier catapult launch: the jet at full power, the run, the hard stop, steam. This one: a real carrier launch (USS George H. W. Bush) with a real F-4's afterburner at full power under its start, then the catapult fires at 1.2 s and the jet's roar tears past; at 3.55 s a rail car's hard steel slam (the shuttle hitting its stop) and a steam release hissing as the roar fades; Jet Fighters and Helicopter Launches and Recoveries (Landings) on Aircraft Carrier.flac (qubodup) + Afterburner sound.wav (StoneyJ) + Rail Cars Slam Hard and Couple Together Air Released (WeenyBeany) + Air (or steam) pressure release (brunoboselli), 4.90 s (−21 dB) | rejected — round 47, L.5: b picked |
| [concept/launch-rail-r47-b.ogg](concept/launch-rail-r47-b.ogg) | Freesound HQ preview (CC0, CC-BY 3.0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `launch-rail` b: plays at every level start (4.9 s): the ship is catapulted off the carrier; the shuttle stops at 3.6 s; should sound like an aircraft-carrier catapult launch: the jet at full power, the run, the hard stop, steam. This one: another real launch from the same carrier: an afterburner at full power, the catapult firing at 1.2 s and the jet's roar rising more slowly; at 3.55 s a train coupler's sharp steel clank over a rail car slam's low body (a brighter, more metallic stop than a), then a close steam release; Jet Fighters and Helicopter Launches and Recoveries (Landings) on Aircraft Carrier.flac (qubodup) + Afterburner sound.wav (StoneyJ) + Train car coupling sounds (toam) + Rail Cars Slam Hard and Couple Together Air Released (WeenyBeany) + AIRHiss-Blue Snowball Microphone, CU_Steam Release, Acme Windmaster_Nicholas Judy_TDC (designerschoice), 4.95 s (−21 dB) | chosen (round 47, L.5 b: the user's pick; still the preview cut, see the note under Open questions) |
| [concept/rejected/dome-hit-r49-a.ogg](concept/rejected/dome-hit-r49-a.ogg) | Freesound HQ preview (CC0, [CREDITS.md](../../../CREDITS.md)) — `dome-hit` a: plays at every enemy shot that hits one of Level 17's colony greenhouse domes (`Sfx.DOME_HIT`, `AllyCues`): often, several in a row; should sound like a glass dome tens of metres across, the size of a stadium roof, being hit: both sharp and huge, a hard bright strike and then the big structure answering (a long low glassy resonance or boom, cracks running away across a large surface, like a frozen lake's ice), heard from a distance; at most 1.2 s, no shatter, not a thud, not a bottle. This one: a steel hammer striking a big glass pane on a wall (loud, bright, not breaking) and, right under it, a frozen lake's ice cracking: one deep crack and then a run of smaller cracks racing away across the sheet, each with the ice's falling 'zing', slowed to 0.8 so the sheet sounds bigger: sharp, glassy and wide, 1.2 s; IMPACT_GLASS_PANE_HIT_NO_SMASH_METAL_RING_LOUD_NTG3_H4N_20120513.wav (samueljustice00) + Ice - Lake fractures (Vrymaa), 1.22 s (−20 dB) | rejected — round 49, M.1: the user picked b |
| [concept/dome-hit-r49-b.ogg](concept/dome-hit-r49-b.ogg) | Freesound HQ preview (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `dome-hit` b: plays at every enemy shot that hits one of Level 17's colony greenhouse domes (`Sfx.DOME_HIT`, `AllyCues`): often, several in a row; should sound like a glass dome tens of metres across, the size of a stadium roof, being hit: both sharp and huge, a hard bright strike and then the big structure answering (a long low glassy resonance or boom, cracks running away across a large surface, like a frozen lake's ice), heard from a distance; at most 1.2 s, no shatter, not a thud, not a bottle. This one: the hammer on the glass pane slowed to 0.85 (heavier), over a glass table struck and ringing (a contact-mic recording) slowed to 0.5, an octave down, so a large glass body hums on after the hit; both played into a real cathedral (convolved with its recorded impulse response), so it rings out in a huge space as if heard from far below: a bright 'clank' with a long low glassy 'hmmm', 1.2 s; IMPACT_GLASS_PANE_HIT_NO_SMASH_METAL_RING_LOUD_NTG3_H4N_20120513.wav (samueljustice00) + R4_00328-8_GlassTable.wav (kevp888) + IR_Cathedral_5m_Stereo.wav (Nox_Sound), 1.20 s (−20 dB) | chosen (round 49, M.1 b: the user's pick; still the preview cut, see the note under Open questions) |
| [concept/rejected/dome-hit-r49-c.ogg](concept/rejected/dome-hit-r49-c.ogg) | Freesound HQ preview (CC0, CC-BY 4.0, [CREDITS.md](../../../CREDITS.md)) — `dome-hit` c: plays at every enemy shot that hits one of Level 17's colony greenhouse domes (`Sfx.DOME_HIT`, `AllyCues`): often, several in a row; should sound like a glass dome tens of metres across, the size of a stadium roof, being hit: both sharp and huge, a hard bright strike and then the big structure answering (a long low glassy resonance or boom, cracks running away across a large surface, like a frozen lake's ice), heard from a distance; at most 1.2 s, no shatter, not a thud, not a bottle. This one: a rock slammed onto a frozen lake (a hard icy crack with the ice's 'zing'), on a deep crack and boom of the lake's thick ice slowed to 0.85, and from 0.25 s a tall steel door's low groan under it, quiet, like the dome's frame taking the blow: harder and darker than a, no glass pane, 1.2 s; rock hitting icy lake (ultraaxvii) + Ice - Lake fractures (Vrymaa) + Door_Metal_Groans_Ext.wav (Cell31_Sound_Productions), 1.23 s (−25 dB) | rejected — round 49, M.1: the user picked b |

## Implementation

- [x] SFX playback with instance limits, stealing by priority, pitch variation: the 32-voice
  limit with priority stealing (`VoiceLimit`, `Sfx.Priority`), the per-sound instance limits, ±pitch
- [x] Stereo panning by play-field X
- [ ] Underwater low-pass on the sfx bus — **later: Act 4** (the Europa levels); round 48 Q12 a proposes dropping it
  for the under-water recordings of *Act 4 sounds* (decided in round 48 (2026-10-22))
- [x] The Act 1 sounds: every chosen sound an Act 1 event uses is played (the list of the ones
  that are not, with the reason, is above); the explosion ladder by size, the Smart Bomb, the
  shield's restore chime, the low-armour beeps, the pickups by type, the ground targets' crumble
- [x] Heavy enemy shots: `Sfx.ENEMY_HEAVY_SHOT_A`/`_B` play when an `ENEMY_FIRED` event's value
  (the gun's damage, sent by the simulation) is at least the medium bullet class's damage
  (`FlightSounds.heavyShot`, `BulletLooks.MEDIUM_DAMAGE`): the Leviathan's and Scuttler's `medium` shots
- [x] The hangar's shop sounds (`Sfx.SHOP_BUY`, `SHOP_SELL`, `SHOP_DENIED`, `EQUIP`, `UPGRADE`,
  chosen by `Sfx.shop`) played by the hangar screen instead of the menu blips: a shop action its
  own, a paid repair the purchase, an undo and a confirmed sale the refund, a refusal the denial
  (`HangarScreen.doneSound`; `HangarSoundsTest`)
- [x] The Vrell screech (user decision 2026-10-06): `Sfx.ENEMY_SCREECH_C`/`_D` in turn as a Mantis,
  a Coilwyrm's head, a Spore Bomber or a Scuttler first comes onto the screen, once per unit (by its
  serial), at most one every 3 s, at the explosion gain like the Vrell spawns, enemy-fire priority
  (`ScreechCue`, followed in `FlightSounds.watch`; `ScreechCueTest`, with Level 06 flown)
- [x] Save done (P2) and the Coilwyrm's chain-cut tear, round 27's choices: `Sfx.SAVE_DONE`
  (save b, a concept copy by `copyPlaceholderSounds`) when the hangar's autosave or a save to a slot
  is written (`HangarScreen`, `SlotsScreen`), `Sfx.COILWYRM_CUT` (tear a, rebuilt from its original
  by `tools/art/sfx_originals.py` with sfx_r27.py's `PRODUCTION`) on `CHAIN_CUT` (before: a lower
  Brood Pod burst)
- [x] The Act 2 weapon sounds built in M5 part A: the Tail Gun and Fan Blaster play the `pulse`
  family 10 % lower as rear guns, the Hornet Launcher `Sfx.MISSILE_SHOT` (shot-missile-r03-a), the
  Proximity Mines' drop `Sfx.MINE_DROP` (shot-mine-r03-a, not lowered: a drop, not a gun), the
  Swivel Gun the `ballistic` family; a mine's blast plays the small explosion like a bomb's
  (`FlightSounds`)
- [x] The proximity mine's arming beep: `Sfx.MINE_ARM` on the simulation's `PROXIMITY_MINE_ARMED`
  (the step a mine arms), at −10 dB in the flight mix, ±3 % pitch, panned, two instances at most
  (`FlightSounds`); round 28's chosen a, the armed chirp (M5 part A)
- [x] The megacity ambience (`earth-megacity`: `Sfx.AMBIENCE_CITY`, `Sfx.ambience` maps a level's
  key to its loop for the level screen) under Level 08's section 1 (M5 part B)
- [x] The Creeper's screech as it comes onto the screen, like the Scuttler's (`ScreechCue`) (M5
  part B)
- [x] Level 09's sounds (M5 part C): the Hive Node's iris and release (reused), the Ravager's pounce
  and the arcology's collapse (round 31's picks), played from the simulation's events:
  `Sfx.HIVE_IRIS` (carrier iris b) on `SPAWN_TELEGRAPH`, `Sfx.HIVE_SPAWN` (Vrell spawn a) on
  `SPAWN_RELEASED`, the node's death on the `large` rung and the Ravager's on the `small` one; the
  pounce (`POUNCE`) plays `Sfx.RAVAGER_POUNCE_A` or `_B` at random (`FlightSounds`' own SplitMix64),
  the collapse `Sfx.ARCOLOGY_COLLAPSE` (collapse a) from the warning's start (`COLLAPSE_AT_WARNING`),
  the crash at the impact, 3.0 s in (`COLLAPSE_CRASH_SECONDS`, round 31's look c: the 1.5 s lean
  and the 1.5 s drop)
- [x] Level 10's sounds wired (M5 part D), with placeholders until round 32's picks: the Wraith's
  decloak (`Sfx.WRAITH_DECLOAK` on `DECLOAK`, for now the Mantis's telegraph a), the Mote Swarm's
  whoosh (`Sfx.MOTE_SWARM`, for now the sled's pass b: once as a swarm's first member comes onto
  the screen, `SwarmCue`, and on each `LOOP_BACK`) and the lance's strike (`Sfx.LANCE_STRIKE`, for
  now the large crumble a, whose crash also sits 1.2 s in: started on the level clock
  `FlightSounds.LANCE_IMPACT_SECONDS` = 1.2 s before the scripted loss's hit, 116.8 s); the ocean
  ambience (`Sfx.AMBIENCE_OCEAN`, `earth-ocean`, brought forward from part E) crossfading in over
  4 s from the megacity at Level 10's section 4 (`LevelMusic.ambienceChanges`, back to the megacity
  on a restart); the shuttles' hits and losses on the ally sounds (`Level10SoundsTest`)
- [x] Round 32's picks for the decloak (b), the swarm (b) and the lance (a) in `assets/sfx/` and in
  their `Sfx` entries, replacing the placeholders (the lance's impact stays 1.2 s into its file:
  `Level10SoundsTest` checks the loudest moment against `LANCE_IMPACT_SECONDS`)
- [x] Torpedo launch and the water explosion — M5 part E (step E3d: `Sfx.SHOT_TORPEDO`,
  `EXPLOSION_WATER` and `EXPLOSION_UNDERWATER` from the chosen `shot-torpedo-r03-a`,
  `explosion-water-r03-a` and `explosion-underwater-r03-a`: the Torpedo Pod's launch plays the
  `torpedo` family file, and in a water level a kill, a torpedo's impact and a landing blast play
  the water explosion over their size rung, a submerged kill or impact the under-water one,
  `LevelScreen.waterBurst`/`waterDeath`/`waterImpact`, `FlightSounds.waterExplosion`); the other
  Act 2 ambiences with their levels (the storm in Level 12's sounds, part F; the arctic in Level 13's,
  part G: their items below)
- [x] Level 11's sounds (M5 part E, step E3d, [round 33](../../concept-rounds/round-33/README.md)'s sounds, all accepted, played from `assets/sfx/`:
  seven rebuilt from their originals by `tools/art/sfx_originals.py`, the synthesized pulse copied by
  `:pipeline:copyPlaceholderSounds`): the Kraken's churn (each `TELEGRAPH`, from the
  telegraph's start; no seek, so on hard the 1.0 s file overruns the 0.8 s telegraph by 0.2 s), slam
  (started `telegraph + rise − 0.5 s` after each lane's `TELEGRAPH` so its impact lands on the slam,
  the second lane 0.5 s later), surfacing (`SURFACE`) and death (the set piece's cry, under the
  `huge` burst); the ships' hit (`ALLY_HIT`) and sinking (`ALLY_LOST`), the frigate's distant flak
  (`ALLY_FLAK`), the Driftjelly's ring (the `ENEMY_FIRED` of a unit with a ring); the ocean ambience
  from t=0, the base stem from section 2, the full mix from 4 and the mini-boss sting at the halt
  (`Level11SoundsTest`)
- [x] Level 12's sounds (M5 part F, steps 2 and 6): the rain-only storm bed (`earth-storm`) with
  the ocean in the eye, thunder after the flashes, the gust, the Lamprey's latch, drain loop and
  shake-off, the sonde chime; auditioned in round 34, then played from `assets/sfx/`
  (`Level12SoundsTest`) — built with round 34's variant a copied as placeholders
  (`copyPlaceholderSounds`; `Sfx.AMBIENCE_STORM`, `THUNDER_1`–`3`, `GUST`, `LAMPREY_LATCH`,
  `LAMPREY_DRAIN`, `LAMPREY_SHAKE`, `SONDE_CHIME`; `FlightSounds`), swapped for the user's picks
  (step K1: the thunder and the gust b)
- [x] The Proximity Mines' drop and arm heard in Level 12's capture (M5 part F, step 7): both heard in the capture and
      accepted in round 34; the drop was raised to −10 dB afterwards and the re-captured clip (round 44, X.e) was
      accepted by the user (2026-10-20)
- [x] Level 13's sounds (M5 part G, step G6): `AMBIENCE_ARCTIC` (key `earth-arctic`) with its whiteout
  peak (the section peak's `ambience_db`, ramping with the atmosphere; `LevelMusic.ambienceLevels`), the
  relay hit, the integrity alarm, relay destroyed, the drone launch and grid online as round 35's **a**
  placeholders (`copyPlaceholderSounds`; `Sfx.RELAY_HIT`, `INTEGRITY_ALARM`, `RELAY_DESTROYED`,
  `DRONE_LAUNCH`, `GRID_ONLINE`; `FlightSounds`), swapped for the user's picks (step K1: grid online b); the
  Skimmer's sink on the water explosion (`Level13SoundsTest`)
- [x] Level 14's sounds (M5 part H, step H6; the user's picks since step K1: the tear-free roar and the
  canopy withering b, the rest a): the canopy hum as the ambience `earth-canopy` (`Sfx.AMBIENCE_CANOPY`; at the Spire's
  death it fades out over 2 s while the megacity's ambience rises over 7 s, `LevelMusic`), the root burst at
  each burst's telegraph (`ROOT_BURST`), the growth as its growths appear (`ROOT_GROWTH`), the tear-free
  roar at the lift (`TEAR_FREE_ROAR`), the canopy withering 3 s after the Spire's death (step H7b)
  (`CANOPY_WITHERING`); the maw opening reuses the carrier's sac open, the launches its launch, the lobs
  the Polyp Mortar's, the sweep the Mantis's telegraph and beam (`FlightSounds`, `Level14SoundsTest`)
- [x] Recorded sounds rebuilt from the Freesound originals: every chosen recorded concept sound (75
  from rounds 02–08, round 21's four, round 23's five, round 24's two, round 25's seven, round 27's one, round 31's three, round 32's three and round 33's seven) in `assets/sfx/` under its concept name, with a `SOURCE` comment, by
  [`tools/art/sfx_originals.py`](../../../tools/art/README.md); `importPlaceholders` keeps them
  (only the synthesized sounds are still copied). `art: final` waits for the user's round-12 review.
- [ ] Act 3's sounds (approved in round 36; M7 parts A–H): the sounds of
  *Act 3 sounds* in `assets/sfx/` and `Sfx` as each part builds its levels, units and equipment; the
  five Mars ambiences (`mars-dust`, `mars-canyon`, `mars-colony`, `mars-storm`, `mars-foundry`) and the
  basin rumble; a unit's loop once per kind for the nearest unit (the mixing proposal). M7 part A
  (step A6): the Act 3 arsenal's sounds play — the Ion Beam's start with its loop (round 36 a) and
  stop, the sizzle while it burns (rock on a ground object), the deflector tick and return, the
  drone break and rebuild (an absorbed hit pings the metal hit softly), the EMP ring and one
  power-down a step, the Orbital Lance's lock beep and its loop c with the rumble while it burns, the
  overcharge whoosh and end, the cooldown chime and the dash burst — as round 37's picks (user,
  2026-10-13: the beam's stop, the rock sizzle, the deflector return, the EMP ring and power-down, the
  lance rumble and the overcharge whoosh b, the rest a; step Q2) (`FlightSounds`, `Sfx`). M7 part B
  (step B4): Level 15's — the Burrower's eruption a from its bulge's start with round 38's crack layer b
  over it (`Sfx.BURROW_BULGE`, step R38C) and its dive b, the entry roar `mars-entry` a in sections 1–2
  crossfading over 2 s to `mars-dust` a at section 3 (round 38's picks, user 2026-10-14; `Level15SoundsTest`). M7 part C
  (steps AUD-C, G1, G2): Level 16's — the canyon scrape b panned at the contact, the tendril hum (0.4 while
  dormant) and burn loops and the break, the Threadcrawler's walk loop (nearest unit, panned) and its cut as the
  Coilwyrm's rip at pitch 0.8, −3 dB, the `mars-canyon` ambience (`Level16SoundsTest`; round 39's proposals,
  pending the user's review)

## Open questions

Rounds 47 and 49 closed (2026-10-22): the dome hit **b** (round 49); herald song **a**, Leviathan cry **b**, launch rail **b**; the integrity alarm keeps the old file. Open:

- **Originals of the picks:** the four picks are still cut from Freesound's free previews. Their originals (Freesound 502946, 816640 for the herald song; 797762 for the Leviathan cry; 162449, 104883, 198605, 750751, 803061 for the launch rail; 155619, 636555 and the cathedral impulse response 648945 for the dome hit, round 49) wait for the download limit; then the picks are rebuilt from them (`tools/concept/audio/sfx_r47l.py`, `sfx_r49m.py`) and the rebuilt files replace the preview cuts in `assets/` (step SND-M, 2026-10-22: the game already plays the four picks from the preview cuts).
- **`dome-hit`** (round 49, M.1): **b** picked (the hammer strike over the glass ring in the cathedral reverb); a and c rejected, round 47's two and round 46's two before them too. It replaced r40-a in the game from the preview cut (step SND-M); the originals' rebuild follows.

## Decisions

- 2026-09-30: Priorities P1–P3 guide production order; player fire is deliberately quiet.
- 2026-09-30: Concept round 01: all three pickups chosen, one per item type — A standard power-ups, B rare upgrades, C credits.
- 2026-09-30: Concept round 01: all synthesized player shots and explosions rejected. Explosions must be somewhat realistic; source recorded sound effects from free online libraries instead.
- 2026-09-30: Concept round 02: recorded shots and explosions are much better; all ten kept as a starting set. Needed next: a distinct shot sound per weapon type (see [weapons](../../player/weapons/README.md)) and more explosions covering the full range from small pops to large booms.
- 2026-09-30: Concept round 03: rejected huge-a, medium-a, tiny-c, underwater-b and both beam loops (B barely audible); all other r03 shots and explosions chosen. The `beam` family needs new sources (round 04).
- 2026-09-30: Concept round 04: all new sounds chosen (beam loops, start/stop, huge and under-water explosions, sonar A); sonar B rejected — A is enough.
- 2026-10-01: Concept round 08: remaining Acts 1–2 sounds proposed — 40 recorded (CC0/CC-BY, band-levelled; five sub-bass-only candidates rejected) and 14 synthesized pickups and UI blips. Overdrive pickup maps to pickup-r01-a, data core to pickup-r01-b, salvage medium to pickup-r01-c. Still open: equip/upgrade and grade-stamp UI sounds, a fourth Vrell screech, an Airstrike radio call (text + squelch for now, no voice).
- 2026-10-01: Confirmed by the user: pickups and UI sounds stay synthesized; everything else (shots, explosions, hits, enemies, specials, radio, klaxon, ambience) uses recorded CC0/CC-BY sounds.
- 2026-10-01: Round 08 verified (peak, 200 Hz–5 kHz band level, DC, edges, loop seams, spectrograms) and fixed: bell and shimmer tails in four synthesized sounds (armour patch, special charge, shield cell, debrief total) ended in a click — the bell now has a release and the armour patch is longer; ship destroyed is high-passed at 20 Hz (sub-sonic drift left a DC offset); shield hit a is low-passed at 7 kHz (83 % of its energy was above 5 kHz); crumble b got a new source (the first was almost all sub-bass and stayed ~8 dB quieter than a on its audible band, even high-passed). Added the missing Acts 1–2 sounds: heavy enemy shot b, Vrell screech d, two Vrell spawn sounds, and synthesized equip, upgrade and grade-stamp UI sounds. Rejected on licence grounds: Artninja's "morphing burst" (built from Warner Bros and Zapsplat library sounds). `sfx.py` now writes the rejected round-01 shots and explosions to `concept/rejected/`.
- 2026-10-01: Concept round 08: all round-08 sounds chosen except screech a and b, shield hit b, shield restore b and klaxon b (rejected).
- 2026-10-01: Approved by the user in the [Acts 1–2 design review](../../reviews/acts-1-2/README.md).
- 2026-10-01: M1: the spike's `SfxBank` is carried over (64 OpenAL sources, played only from the render thread because libGDX's source pool is not thread-safe) and now enforces the per-sound instance limits by stopping the oldest instance. Levels relative to player damage: explosions −2 dB, hits −8 dB (the mixing rules give no level for hits), player fire −14 dB; ±5 % pitch (±4 % explosions), two-variant sounds alternate, pan up to ±40 % by play-field x. Voice stealing by priority is not done yet. Copies of the used files are in `assets/sfx/` (rows in CREDITS.md).
- 2026-10-01: Enemy-hit level added to the mixing rules (user decision): −6 dB on this scale, 8 dB below player damage, as the M1 SFX player uses.
- 2026-10-02: M2 imports the Level 01 sounds into `assets/sfx` (`:pipeline:importPlaceholders`,
  CREDITS.md rows for the third-party ones): small enemy shots, the small explosion family for
  Needlers and containers, metal hits for ground objects, the salvage, shield cell and armour patch
  pickups (salvage large for the hidden crate), radio squelch and typing, the debrief tick, total
  and stamp, and the Earth-orbit ambience loop.
- 2026-10-02: The launch rail sound goes into the next concept round (user decision); listed as a
  pending item in the Player ship table, no file yet.
- 2026-10-02: Concept round 11 proposed: two synthesized launch-rail sounds and two edge-warning
  tones (`tools/concept/audio/sfx_r11.py`). The edge warning tone is a new P1 row: the user
  found the side and rear warnings easy to miss after playing Level 01.
- 2026-10-02: Concept round 11 (user choices): launch rail **b** "catapult" and edge-warning tone **b** "contact ping" chosen; a "mag-lev" and a "triple chirp" moved to `concept/rejected/`. The launch-rail item is no longer pending.
- 2026-10-02: M3 part B1: the launch rail (b) plays at the start of every attempt's 5 s launch in
  Level 01, so its buffer clunk lands at 3.6 s; the edge-warning tone (b) plays when an edge warning
  starts, at the player-damage level, panned towards the warned edge, with an instance limit of 1
  so overlapping warnings never stack. Both are synthesized (no CREDITS.md rows) and imported by
  `:pipeline:importPlaceholders`. The launch rail plays at full level (the file is levelled like the
  interface sounds); no level is given for it beyond that.
- 2026-10-02: Production audio (Level 01 batch, part P2): the 75 chosen recorded sounds are rebuilt
  from the Freesound originals (cached by `tools/concept/audio/freesound_fetch.py`) with their
  unchanged `import_sfx.py` settings by `tools/art/sfx_originals.py` and written to `assets/sfx/`
  with a `SOURCE` comment; the concept files stay as the record of the choice. One step is added
  before the treatment: 13 originals decode above full scale (lossy originals and float WAVs, up
  to +18.7 dBFS for "Machine Gun 001") and are clipped at full scale first, as 16-bit playback
  clips them; levelled on the unclipped peak, the Autocannon Pod shot came out 11 dB quieter
  than the chosen sound and "explosion_big_02" 1.7 dB. Checked against the chosen files: same
  lengths, and peak and 200 Hz–5 kHz band RMS within 1 dB, except the peak of
  `enemy-shot-small-r08-b`, 1.7 dB lower at the same band RMS (it is levelled on its band, so only
  the crest of its attack differs from the preview's; the audible level is the same).
- 2026-10-02: Concept round 12 closed (user decision): the 75 recorded sounds rebuilt from the Freesound originals approved as **final** (the 1.7 dB lower peak of `enemy-shot-small-r08-b` accepted); this doc's `art` stays `chosen`, since the synthesized sounds were not part of the round.
- 2026-10-02: Concept round 16: the Leviathan's whale-song cry proposed as a synthesized sound
  (`tools/concept/audio/sfx_r16.py`, written to `concept/` like the other synthesized sounds,
  which `importPlaceholders` copies). The 2026-10-01 decision has enemy sounds recorded; this one
  is synthesized for the M4 part C batch, so a deep alien call with its own glide and formants
  exists without a source search. The user decides in round 16 whether it stays or a recorded
  CC0/CC-BY call replaces it.
- 2026-10-03: Concept round 16 closed (user decision): the synthesized Leviathan whale-song cry (`enemy-leviathan-cry-r16-a`) approved as **final**. The user accepts this synthesized cry, so its placeholder note against the 2026-10-01 rule (enemy sounds are recorded CC0/CC-BY) is resolved: no recorded replacement is sought. This doc's `art` stays `chosen`.
- 2026-10-04: Round 21 opened with the sounds of Level 05's props (user decision D8 of M4 part E): recorded a/b pairs for the sled's whine and pass and the Polyp Mortar's lob and impact.
- 2026-10-04: Round 21 closed for the sounds (user): mortar lob a, mortar impact a, sled whine a
  (its 0.78 s loop played twice over the 1.5 s chase) and sled pass b chosen and played by the game
  (`Sfx.MORTAR_LOB`, `MORTAR_IMPACT`, `SLED_WHINE`, `SLED_PASS`, in the `:pipeline` copy list),
  replacing the placeholders; the four others moved to `concept/rejected/`, their CREDITS.md rows
  kept with the new paths, as for the earlier rounds' rejected recordings.
- 2026-10-04: Round 23 opened with Level 06's new sounds (user decision D8 of M4 part F): recorded
  a/b pairs for the Mantis's telegraph and beam sweep, the flare launch and burn loop and the
  Coilwyrm's head regrowth. The data core keeps its round-08 mapping to the chosen `pickup-r01-b`
  and the Smart Bomb its chosen `special-smartbomb-r08-a`, so neither is in the round.
- 2026-10-05: Round 23 decided (user): Mantis telegraph **a** and sweep **b** (CC-BY), flare
  launch **a** (CC-BY) and burn **a**, Coilwyrm regrowth **b** chosen; the other five moved to
  `concept/rejected/` (their CREDITS.md rows follow). Wired into the game as round 21's sounds
  were: copied into `assets/sfx/` under their concept names by `:pipeline:importPlaceholders`
  (`copyPlaceholderSounds`), played by `FlightSounds` through new `Sfx` entries: the telegraph at
  the warnings' level, the sweep at the hits' level (a little above enemy fire), the launch with
  the burn's 3 s loop played back to back while the flare burns (8 s, 12 s on easy; the last play
  at half level as the pool fades), the regrowth at the explosions' level. The two CC-BY picks
  reach the credits screen through their CREDITS.md rows (the screen's list is generated from
  them, skipping rejected files). Rebuilding them (and round 21's) from the Freesound originals
  with `tools/art/sfx_originals.py` needs `freesound_fetch.py --download` first: the originals
  are not cached.
- 2026-10-05: Production audio for rounds 21 and 23, now that the originals are cached: the nine
  chosen recorded sounds (sled whine a, sled pass b, mortar lob a and impact a; Mantis telegraph a
  and sweep b, flare launch a and burn a, Coilwyrm regrowth b) rebuilt from the Freesound
  originals with their unchanged `import_sfx.py` settings by `tools/art/sfx_originals.py` into
  `assets/sfx/` under their concept names, with a `SOURCE` comment and CREDITS.md rows for the
  `assets/` files; `copyPlaceholderSounds` keeps them, as round 12's. The script builds every
  sound whose Concept art row is `chosen` and stops at the first one whose original is not cached,
  which was the stop above; the rows stay `chosen` (the concept files record the choice, the
  `SOURCE` comment marks the final file). Only the sled pass's original decodes above full scale
  (82 samples, +0.01 dBFS) and is clipped first. Checked against the chosen files: same lengths
  (the whine's 0.78 s and the burn's 3.00 s loops still seamless), peaks within 0.3 dB, 200 Hz–5
  kHz band RMS within 0.5 dB and 50 ms envelopes within 0.7 dB, except the regrowth: held at its
  −6 dBFS ceiling by the original's 0.6 dB higher crest, it is 0.8 dB lower on its band and 0.9
  dB on average over its envelope (within the 1 dB tolerance). This doc's `art` stays `chosen` (the synthesized sounds are not final).
- 2026-10-05: Round 24 closed (user): the Coilwyrm's head burst **c** (CC-BY) and segment burst
  **b**; a and d and the round's previews moved to `concept/rejected/` (their CREDITS.md rows
  follow). Rebuilt from the Freesound originals by `tools/art/sfx_originals.py`, which now also
  takes sounds with a treatment of their own (`DERIVED`: `sfx_r24.py`'s `PRODUCTION`, the cut,
  the loudest-100-ms levelling and the head's slowed thump), into `assets/sfx/` under their
  concept names; within 0.4 dB of the concept files on peak and band. The chained death plays one
  burst every 0.25 s at the explosion level (they no longer overlap), each pitched by the member's
  width down the taper; `enemy-coilwyrm-death-final-r24-a` previews it.
- 2026-10-05: Round 25 opened (M4 part G, Level 07): recorded a/b pairs for the Brood Carrier's
  roar (its arrival and its turn), a bay sac opening, closing and bursting, a unit launched from a
  sac, the plate iris opening and the lifeboat tow's cable snap (`tools/concept/audio/sfx_r25.py`,
  the first round cut from the Freesound originals, so the chosen files need no rebuild). Option a
  plays in Level 07 for now, through new `Sfx` entries and `FlightSounds`: the roar 0.75 s after
  the klaxon starts and again at 0.85 pitch as the turn begins; one sac-opening sound for the sacs
  that open together, a quieter closing as they shut; up to three launch spits 0.1 s apart after
  an opening (an opening's units all leave in one step); a sac shot off bursts with the generic
  part explosion under it, and the death chain bursts every sac with it in its tail-to-head order;
  the iris as the core is exposed; the cable snap as the pod falls free (the cable's hits keep the
  metal hit). The carrier plays no generic phase blast. All at the explosions' level. The klaxon
  keeps the chosen, already final `ui-klaxon-r08-a`. Not listened to: picked by description,
  rating, envelope, band balance and level.
- 2026-10-05: Round 25 closed (user): carrier roar **b**, sac opening **b**, sac closing **b**,
  launch **b**, sac burst **a** (CC-BY 4.0, SilverIllusionist), iris **b** and cable snap **a**
  (CC-BY 3.0, CosmicEmbers); the other variants moved to `concept/rejected/` (their CREDITS.md
  rows follow). The chosen files were already cut from the Freesound originals; the production
  files are written by `tools/art/sfx_originals.py`, which takes round 25's chosen treatment as
  `DERIVED` (`sfx_r25.py`'s `PRODUCTION`: the cut, the slowdown or reversal, the loudest-100-ms
  levelling), into `assets/sfx/` under their concept names with a `SOURCE` comment; `Sfx` and
  `copyPlaceholderSounds` play the chosen ones, the provisional option-a files that are no longer
  used were removed from `assets/sfx/`. The two CC-BY sounds join the credits screen's list.
- 2026-10-05: M4 part H, the SFX pass. **Voice limit**: at most 32 effects at once (`VoiceLimit`
  in `SfxBank`); a play past an effect's own instance limit stops its oldest instance, a play past
  32 steals the oldest voice of the lowest priority at or below its own or is dropped. Libgdx does
  not report a sound's end, so a voice counts for its file's length (read from the Ogg file's last
  page, divided by the pitch). Priorities as in the mixing rules; the groups they do not name were
  placed by the main-agent brief and this pass: the interface with the warnings, the specials with
  the boss sounds, hazards with enemy fire, hits with player fire; loops are never stolen.
  **Wired** (chosen sounds already in `assets/sfx/`): enemies explode on their size rung (`medium`
  for the Brood Pod, Mantis, Scuttler and Spore Bomber; two files a rung, alternated); a set
  piece's part blows with the `medium` rung, a boss's phase end with the `large` one; a set piece's
  death with the `huge` rung (an act boss, and a set piece that is no boss, the Leviathan) or the
  `large` one (a mid-boss, the Gorgon Frigate); an act boss's tail-to-head chain starts on the
  `large` rung, bursts on the `medium` one and ends on the `huge` blast with the `large` rung's boss
  blast (r02-e) 0.05 s later; the Leviathan's break-up layers the `large` and `medium` rungs instead
  of slowed small explosions. A destroyed ground target bursts with the small ladder's r02-b or a and
  crumbles under it: collapse a for a target of at least 1 000 px², rubble b below. The Smart Bomb
  plays its chosen energy blast with the `huge` rung's sub-heavy boom under it at −3 dB (the blast
  swells over 0.5 s; the boom gives the instant flash its punch). The shield's restore chime plays
  once the shield is full again after a break (not after every hit's regeneration). The low-armour
  beep repeats every 1.2 s at or below 30 % armour and every 0.6 s at or below 15 %, while the ship
  flies, 6 dB under the warnings' level, since armour does not regenerate and the beeping can last
  the rest of the level. Pickups by type: salvage medium r01-c, the special charge's own chime, the
  data core r01-b, the overdrive pickup r01-a with the overdrive's start cue. Heavy enemy shots and
  the hangar's shop sounds have their `Sfx` entries but are not played yet (see Implementation).
  The nine synthesized sounds are copied by `copyPlaceholderSounds`. Not played, with the reason:
  the table *Chosen sounds the game does not play*. Gaps for concept round 26: a save-done sound, the
  Coilwyrm's chain-cut tear, the Vrell screech's cue. Not listened to: levels set by rule.
- 2026-10-05: Concept round 26 closed (user: the round accepted as proposed): the SFX pass accepted
  as built: the 32-voice limit with priority stealing and the newly wired chosen sounds (the shop,
  the pickups by type, the explosions by size, the ground targets' crumble, the Smart Bomb, the
  shield's restore chime, the low-armour beeps, the heavy enemy shot), at the levels set by rule.
  The row's two questions had no proposal and were not answered: the Vrell screech's cue and
  whether to make concepts for a save-done sound and the Coilwyrm's chain-cut tear stay open
  questions, tagged `later: M5`. Every item is ticked or tagged later, so the document is `done`;
  `art` stays `chosen`, since the synthesized sounds are still concept copies and the save-done and
  chain-cut sounds have none.
- 2026-10-06: Round 26's open questions decided (user): the **Vrell screech** (c and d, in turn)
  plays as a large Vrell unit enters the screen, the Mantis, the Coilwyrm's head, the Spore Bomber
  and the Scuttler, throttled to one every **3 s** so a wave does not stack them; no simulation event
  marks an enemy's entry, so the game watches each step for a unit of those kinds whose hit box
  first overlaps the play field (by its serial, so it screeches once). It ranks with enemy fire.
  Concepts for the **save-done sound** and the **Coilwyrm's chain-cut tear** go into [concept
  round 27](../../concept-rounds/round-27/README.md) (two synthesized, two recorded options; a of
  each wired until the choice: the save at the hangar's autosave and a slot save, the tear at the
  cut).
- 2026-10-06: Concept round 27 closed (user): save done **b**, the calm chime (latch click, two warm
  bells over a pad; a, the rising data ticks on a bright fifth, rejected); the Coilwyrm's chain-cut
  tear **a**, "rip_tear FLESH!!!!.wav" by aust_paul (CC0; b, "Tearing Flesh" by dereklieu, CC-BY,
  rejected, so it leaves the credits roll); the Vrell screech approved as built. The save sound
  ships as a concept copy like the other synthesized UI blips, the tear rebuilt from its Freesound
  original by `tools/art/sfx_originals.py`. The game now plays no proposed sound.
- 2026-10-06: M5 plan (user decision D1 of part A): the Act 2 weapon sounds are retagged by part:
  the Hornet, the rear guns and the mines' drop and arm with part A, the torpedo with part E
  (Level 11).
- 2026-10-06: M5 part A built the Act 2 weapon sounds from the chosen families: the Hornet's
  `missile` (shot-missile-r03-a) and the mines' drop (shot-mine-r03-a) are played now, so they
  leave the not-played list; rear guns keep the 10 % lower pitch, the mine's drop does not (it is
  not a gun). The arming beep the `mine` family names has no chosen sound: a gap for round 28.
- 2026-10-06: M5 part A, concept round 28: the proximity mine's arming beep gets two synthesized
  proposals (`tools/concept/audio/sfx_r28.py`, round-08 UI family, peak −12 dBFS): a, a rising
  two-blip "armed chirp", and b, a single gliding "sensor ping". Main-agent brief: short (≤ 0.15 s),
  dry and quiet, since a mount drops up to two mines a second and keeps up to six. The simulation
  now marks the step a mine arms (`PROXIMITY_MINE_ARMED`, at the mine; it allocates nothing and the
  replay hashes are unchanged, events are not hashed); option a plays there provisionally
  (`Sfx.MINE_ARM`, two instances, −10 dB, between the hits' −8 and the shots' −14), so the game plays
  one proposed sound until the round closes.
- 2026-10-06: Concept round 28 closed (user): the mine's arming beep **a**, the armed chirp (two
  rising pulse blips a fifth apart); b, the gliding sensor ping, rejected and moved to
  `concept/rejected/` (`sfx_r28.py`'s `CHOSEN` writes it there). The game already played a, as a
  concept copy like the other synthesized sounds (`copyPlaceholderSounds`, `Sfx.MINE_ARM`); it
  again plays no proposed sound. This doc's `art` stays `chosen` (the synthesized sounds are not
  final).
- 2026-10-06: M5 part B (gap defaults with the user's decisions D1–D6): the **megacity ambience**
  (round 08's a, used as it is) plays under Level 08 by the key `earth-megacity`, a new column of
  the *Ambience* table that names each setting's key for a level's `music.ambience`; the
  **Creeper** screeches on entry like the Scuttler; it needs no other new sound (the enemy fan
  shot, the `medium` explosion). The billboard's topple plays the destructible break or the
  `medium` explosion unless its concept pair brings a sound. The document goes back to
  `in-progress` for part B.
- 2026-10-06: M5 part B: the game plays the megacity ambience by the key `earth-megacity`
  (`Sfx.AMBIENCE_CITY`; its row left the *Chosen sounds the game does not play* table) and the
  Creeper screeches on entry (`ScreechCue`).
- 2026-10-07: M5 part C (user decision D9 = a and the stated defaults): Level 09 needs two new
  sounds, the **Ravager's pounce** and the **arcology's collapse** (a rumble under the shadow
  swelling into a long crash), each an a/b in concept round 31 (recorded CC0 / CC-BY sources, each
  in CREDITS.md); the **Hive Node** reuses the Brood Carrier's iris b and the Vrell spawn a, and
  dies on the `large` rung; the Ravager dies on the `small` rung. Our reading, for review: neither
  unit screeches on entry (the node's iris and the pounce are their cues; a pack would screech over
  itself).
- 2026-10-07: M5 part C, game side: the Hive Node plays the Brood Carrier's iris b as its iris opens
  and the Vrell spawn a (so it leaves the "not played" list) as it releases; the Ravager's take-off
  and the arcology's collapse are wired to their own slots with placeholders (carrier launch b, a
  slime lunge; hit-crumble a, "building collapse", from the fall) until round 31's picks are in
  `assets/sfx/`, when the collapse sound moves to the shadow's start
  (`FlightSounds.COLLAPSE_AT_WARNING`).
- 2026-10-07: Round 31 closed for the sounds: collapse a (CC-BY 4.0, YleArkisto, on the credits
  roll), both pounces kept and picked at random per pounce; rebuilt from the originals into
  `assets/sfx/` by `tools/art/sfx_originals.py` with `sfx_r31.py`'s `PRODUCTION`; the collapse
  plays from the shadow (crash at 1.5 s; to move to the impact when the fall is reworked). Collapse
  b (a steel-frame cave-in) moved to `concept/rejected/`. The Hive Node's reuse of the carrier iris
  b and the Vrell spawn a, and no screech for either unit, confirmed (round 31 row 8).
- 2026-10-07: The collapse's crash moved to the impact (round 31's look c built: the 1.5 s lean,
  then the 1.5 s drop): `sfx_r31.py`'s `PRODUCTION` now mixes the crash at its `IMPACT` (3.0 s)
  over a rumble cut 1.5 s longer and swelling for those 3.0 s, rebuilt by
  `tools/art/sfx_originals.py` into `assets/sfx/collapse-r31-a.ogg` (7.0 s; the same two
  originals, so the credits are unchanged; its check flags the length against the 5.5 s concept
  file, which stays as the record of the choice). `FlightSounds.COLLAPSE_CRASH_SECONDS` is 3.0.
- 2026-10-08: M5 part D (stated defaults with the user's decisions of 2026-10-08): three new sounds
  for Level 10, each an a/b in round 32 (CC0 or CC-BY only, recorded in CREDITS.md): the Wraith's
  decloak, the Mote Swarm's whoosh (on its entry and its loop-back) and the lance's strike; the
  shuttles reuse the ally hit and loss sounds. Round 08's ocean loop is brought forward from part E
  as `earth-ocean`, crossfading in over 4 s from the megacity at Level 10's section 4.
- 2026-10-08: M5 part D, game side: Level 10's three new sounds play from the simulation with
  existing chosen sounds as placeholders until round 32's picks reach `assets/sfx/` (as part C did):
  the decloak the Mantis's telegraph a (a short laser charge-up), the swarm the sled's pass b (a
  rushing flyby), the lance the large crumble a, chosen because its crash lies 1.2 s in, where round
  32's lance files put their impact, so the timing is already the final one. Our readings, for
  review: the decloak and the swarm at the Vrell spawns' level, the lance at the player-damage
  level; a swarm is heard once as its first member comes onto the screen, its later members silent.
  The ocean loop is played (it leaves the "not played" list).
- 2026-10-08: Round 32 closed for the sounds (user picks): the Wraith's decloak **b** (a metallic
  sci-fi rip over a membrane swish; its cloak layer CC-BY 4.0, F.M.Audio, on the credits roll), the
  Mote Swarm **b** (an insect chitter over a whoosh) and the lance **a** (an alien falling whine into
  a thunder crack, its impact 1.2 s in); rebuilt from the originals into `assets/sfx/` by
  `tools/art/sfx_originals.py` with `sfx_r32.py`'s `PRODUCTION` and played by `Sfx.WRAITH_DECLOAK`,
  `MOTE_SWARM` and `LANCE_STRIKE` in place of the placeholders (the lance still started at t 116.8;
  the swarm still heard once at its entry by `SwarmCue` and on each loop-back). Decloak a, swarm a
  and lance b moved to `concept/rejected/`.
- 2026-10-08: M5 part E (user decisions of 2026-10-08; sounds are not an E-question, so one
  proposal each unless the main agent asks for an a/b): Level 11 plays the chosen torpedo launch and
  water explosion (round 03), the ocean ambience from t=0, and new sounds for the Kraken (churn, slam,
  surfacing, death), the ships (hit, sinking) and the frigate's distant flak, CC0 or CC-BY only. In a
  water level every kill on the play field plays the `water` explosion over its size rung and
  landing blasts play it instead of their ground blast (a stated default: their impacts splash).
- 2026-10-08: concept [round 33](../../concept-rounds/round-33/README.md) proposes the Level 11 sounds (one each, all CC0 recordings plus synthesis):
  the Kraken's churn, slam (impact 0.5 s in), surfacing swell and death, the ships' hit and
  sinking, the frigate's distant flak and the Driftjelly's pulse
  (`tools/concept/audio/sfx_r33.py`); awaiting the user's pick, then `tools/art/sfx_originals.py`
  gets a `PRODUCTION` for the chosen ones and the game wires them.
- 2026-10-09: Round 33 closed for the sounds (user): all eight accepted, the slam, surfacing, ship
  hit and jelly pulse as first made; the churn (now a surge of waves over a rumble), the death and the
  sinking reworked without bubbles, the flak raised twice (to −16 dB, low-passed at 4 kHz) after
  listening, then accepted. Seven rebuilt from the originals into `assets/sfx/` by
  `tools/art/sfx_originals.py` with `sfx_r33.py`'s `PRODUCTION` (identical to the concept files by its
  check), under the concept names, so `Sfx` keeps its paths; their `copyPlaceholderSounds` entries
  dropped; the Driftjelly's pulse, fully synthesized, stays copied from its concept file.
- 2026-10-09: M5 part F (user decisions F6 = a and F8 = a of 2026-10-09): Level 12's storm bed is a
  new rain-only cut of the chosen round-08 source ("Rain and Thunder 4", FlatHill, CC0), the chosen
  file kept, and thunder plays as one-shots after each lightning flash (rejected: b, thunder on top
  of the bed's own, muddy; c, flashes timed to the bed's thunder, coupling the schedule to an audio
  loop). New sounds for round 34: thunder, gust, the Lamprey's latch and drain loop a/b, the
  shake-off and the sonde chime one each; CC0 or CC-BY only.
- 2026-10-11: M5 part F step 6 (pending the user's review in the combined round 34): the game plays
  round 34's **a** sounds as placeholders until the round closes. The thunder: the three variants in
  turn, each 0.3–1.5 s after its flash (spread by the golden ratio over the flash index, so a replay
  sounds alike), panned 0.2 to the bolt's side, at the explosions' level; the gust as its telegraph
  starts, panned 0.3 to the upwind side (the upwind cue) at −4 dB; the latch's bite at the player
  damage level; the drain loop at −6 dB while a Lamprey drains in flight (stopped with the latch, the
  ship's loss, the level's end and the screen); the shake-off at −2 dB; the sonde chime at −4 dB. The
  rain-only bed is `earth-storm`; the ocean ambience crossfades in for the eye and back (the level's
  `ambience_changes`).
- 2026-10-11: M5 parts G and H (the user is on vacation; the main agent took the gap analyses' recommendations, **pending the user's review** in the combined round): Level 13's sounds are the relay hit, the integrity alarm, relay destroyed, the drone launch and grid online, with the arctic ambience (`AMBIENCE_ARCTIC`, +3 dB in the whiteout peak); Level 14's are the canopy hum, the root burst, the growth, the tear-free roar and the canopy withering; the rest reuse existing sounds. Auditions in rounds 35 and 36 (a/b where the rows say so).
- 2026-10-11: M5 part F, after the round 34 capture (the user is on vacation; **pending the user's
  review in the combined round**): the latch, the shake-off and the gust could not be heard in the
  storm's mix, though their files are as loud as the shield hit's (about −15 LUFS). The gust (from
  −4 dB) and the shake-off (from −2 dB) now play at the warnings' level with the latch, the edge
  warning and the shield hit; a latch and a shake-off also duck the theme by 6 dB for 1 s (as Level
  10's scripted loss does), since the latch was at that level already. The drain loop (−6 dB) and
  the chime (−4 dB) stay.
- 2026-10-12: M5 part G, step G6 (**pending the user's review in the combined round**): Level 13's sounds
  wired as round 35's a until the round closes. Levels over the files' own (sfx_r35.py levelled them
  against the set): the relay hit with the explosions (−2 dB; it plays often), the integrity alarm,
  grid online and relay destroyed at the warnings' level (0 dB), the drone launch with the explosions
  (−2 dB, not a quiet pickup cue: the round 34 capture could not hear the quiet one-shots). The alarm
  plays once on each `ALLY_BELOW` (50 % and 25 %), grid online on `DEFEND_COMPLETE` (t=218), the drone
  launch 4 s before each drop as its arc starts (only while the relay stands), relay destroyed on its
  `ALLY_LOST`. The arctic wind rises 3 dB through the whiteout by the peak's `ambience_db`. On the ice
  only a burst on a naval unit (the Skimmer) plays the water explosion (G5 = a); every other burst on
  Level 13 plays the ground's.
- 2026-10-12: M5 part H, step H6 (the user is on vacation; **pending the user's review** in round 36):
  Level 14 plays round 36's variant a (the root burst, the tear-free roar, the canopy withering) and the
  one proposals (the root growth, the canopy hum) from `assets/sfx/` under their concept names, at the
  files' own levels; the canopy hum replaces the megacity's sirens as the level's ambience and hands
  back to them at the Spire's death (out over 2 s, the city in over 7 s); the withering starts 0.5 s
  after the death, with the canopy's first tear frame. The maw opening reuses the Brood Carrier's sac
  open (the user's round 25 pick, played at 0.85 pitch) as the spec says; no bubbly or popping layer
  was added.
- 2026-10-12: M5 part G, step G8 (**pending the user's review in the combined round**): the Lamprey's
  shake-off (round 34's placeholder) measured about 6 dB under Level 12's busy mix and was marginal at one
  of its two shake-offs, though it already plays at the warnings' level (0 dB, the game's ceiling). The
  file itself is now 3.5 dB louder (`sfx_r34.py`: −13.5 dB instead of −17, the same −2 dBFS ceiling;
  −12.1 LUFS instead of −16.1); the latch, the gust and the theme's 6 dB duck stay as they were.
- 2026-10-12: M5 part H, step H7b (**pending the user's review**): the canopy withering starts 3 s
  after the Siege Spire's death, at the end of its death chain (`FlightSounds.WITHERING_DELAY_SECONDS`
  0.5 → 3.0): at 0.5 s, with the tear's first frame, the chain's bursts masked it in the H7 capture.
- 2026-10-12: M5 part I, after step CF (**pending the user's review**): a Proximity Mine's drop plays at
  the arming beep's −10 dB instead of the shots' −14 dB (`FlightSounds.MINE_DROPPING`): in the CF capture
  the front gun firing on the same tick masked the drop completely (10–20 dB under the mix); the arming
  beep was heard. Not re-captured.
- 2026-10-12 user, round 34 (Levels 12–14's sounds, rounds 34–36's proposals): the thunder **b** (three
  close strikes, loganzsound CC0), the gust **b** (the recorded gust, cobratronik CC0), grid online **b**
  (the spin-up into a contact clunk, Glitchedtones CC0), the tear-free roar **b** (the creature's bellow
  with the lift) and the canopy withering **b** (the dying exhale, groan and sigh); every other sound of
  the three rounds **a** or accepted as its one proposal. Rejected: thunder, gust, grid online, roar and
  withering a; the latch, drain, relay hit, integrity alarm and root burst b (their files move to
  `concept/rejected/` when the round closes). Step K1 wired the b files (`Sfx`, `copyPlaceholderSounds`;
  the a copies left `assets/sfx/`); measured as G8 did (the loudest 100 ms of the 200 Hz–5 kHz band),
  each b is within 1 dB of the a it replaces (the third thunder 2 dB over), so the levels stay; grid
  online's b is 2.6 s like a, so Level 13's jingle fade stays 2.6 s. None has a bubble, squelch or water
  layer (the roar's wet tearing is the user's pick). The picked sounds are the concept files, already
  built from the Freesound originals or synthesized, without a `SOURCE` mark (`PlaceholderAuditTest`
  lists them).
- 2026-10-12 (user, [round 34](../../concept-rounds/round-34/README.md)): the round closed. The one-proposal
  sounds accepted (12.4e the shake-off, the sonde chime and the rain-only bed; 13.4d the drone launch and
  relay destroyed; 14.4d the root growth and the canopy hum); the picks as in the line above. The
  rejected files (thunder 1–3, gust, grid online, roar and withering a; the latch, drain, relay hit,
  integrity alarm and root burst b) moved to `concept/rejected/`, their CREDITS.md rows with them.
  Pending: the picked sounds still lack a `SOURCE` mark (`PlaceholderAuditTest` lists them).
- 2026-10-12 (overnight design phase, **pending the user's review in round 36**): Act 3's sounds
  listed with briefs (*Act 3 sounds*): terrain and set pieces (the canyon wall scrape, the dome's hit
  and break, the furnace vents, the basin rumble), the new units and the two bosses, the Act 3
  equipment (the Ion Beam with its impact, the EMP Burst, the Orbital Lance, the Shield Overcharge,
  the Deflector Pod, the drones, the dash), and five Mars ambiences; reuses named where a chosen file
  fits (the frigate's flak, the swarm's whoosh, the enemy missile launch for the walker's pods, the
  lock beep for the lance's targeting line, the tesla shot for the EMP). Round 36 proposes a/b for the
  burrow eruption, the canyon wall scrape, the Ion Beam loop and the dust-storm bed; the rest go to
  the round of the part that builds them. No bubbly, popping or squelching sounds (user rule). A
  unit's loop plays once per kind, for the nearest unit (proposal). The unplayed-sound reasons of the
  beam, tesla, missile and lock files name their Act 3 use.
- 2026-10-13 (user, round 36): the *Act 3 sounds* section approved (D.29). Picks: burrow eruption
  **a** "sand burst", canyon wall scrape **b** "metal screech" (with Halleck's CC-BY scrape: on the
  credits roll), Ion Beam loop **a** "the chosen hum, heavier", dust-storm bed **a** "recorded front"
  (`mars-storm`; dobroide's CC-BY howl on the credits roll). The rejected files (burrow eruption b,
  scrape a, ion loop b, storm bed b) moved to `concept/rejected/`, CREDITS.md updated.
- 2026-10-13: M7 part A, step A6: the Act 3 arsenal's sounds wired in flight with round 37's a as
  placeholders (copied into `assets/sfx/` by `importPlaceholders`) until the round closes; the Ion
  Beam loop a, the lock beep and loop c left the *Chosen sounds the game does not play* table. Our
  reading, pending the user's review: the beam's start plays with its loop (not before it), the
  sizzle's −38 dB file plays as it is under the loop, the EMP's power-down plays once a step for what
  the ring reaches (a machine's at the explosions' level, a Vrell unit's 6 dB lower), a drone's
  absorbed hit plays the metal hit 12 dB under the hits.
- 2026-10-13: step Q2: round 37's sound picks in the game (user: S.2, S.4, S.6–S.10 **b**, the rest
  **a**): the seven b files replace their a copies in `assets/sfx/` (`Sfx`, the `importPlaceholders`
  list, `PlaceholderAuditTest`, `ArsenalSoundsTest`), played at the a's levels. Measured: no clicks or
  loop-seam jumps; the EMP ring b sits about 5 LU and the overcharge whoosh b about 5.5 LU under their
  a (the ring has no headroom at −2.7 dBFS peak, the whoosh 4 dB at −7 dBFS), so both are quieter than
  the Smart Bomb and the shield restore in flight — pending the user's review.
- 2026-10-13 (user, round 37): round 37 closed (step Q3): the picks S.1–S.15 as built in step Q2
  (S.2, S.4, S.6–S.10 **b**, the rest **a**); the fifteen unchosen files moved to `concept/rejected/`
  (`sfx_r37.py` routes them by `CHOSEN`, CREDITS.md paths updated). B.6 (a)–(d) yes: the beam's start
  plays with its loop, the sizzle at its file's −38 dB, the EMP power-down once a step (a Vrell unit
  6 dB lower), a drone's absorbed hit 12 dB under the hits.
- 2026-10-14 (M7 part B, step AUD-B, **pending the user's review in round 38**): round 38's sounds
  proposed (`sfx_r38.py`): the bulge a (the eruption a alone, started at the bulge: its built-in swell is
  the tell, so a separate rumble would double it) / b (a crack layer over it), the dive, the entry roar
  and `mars-dust` a/b. The chosen eruption a and each a are copied into `assets/sfx/` until the pick
  (`PlaceholderAuditTest` lists them); the game plays the eruption at the bulge's start, not at the
  surfacing.
- 2026-10-14: M7 part C (step AUD-C): the chosen canyon wall scrape b ([canyon-scrape-r36-b](concept/canyon-scrape-r36-b.ogg),
  Halleck, CC-BY, already in CREDITS.md and now on the credits roll) is copied into `assets/sfx/`; round 39's
  proposals (`sfx_r39.py`, all synthesized, option a each): the tendril hum, burn loop and break, the Threadcrawler's
  walk and the `mars-canyon` ambience, copied into `assets/sfx/` until the pick, pending the user's review.
- 2026-10-14 (user, round 38): closed (step R38C): S.1 = **b**, the crack layer `burrow-bulge-r38-b` played over
  the eruption a, both from the bulge's start at the same pitch (`Sfx.BURROW_BULGE`, `FlightSounds`); S.2 =
  **b**, the grain slide; S.3 = **a**, the recorded entry roar; S.4 = **a**, the recorded dust wind. The game
  ships the four chosen files (`copyPlaceholderSounds`, `PlaceholderAuditTest`); the dive a, the entry roar b
  and `mars-dust` b moved to `concept/rejected/` (`sfx_r38.py` routes them by `CHOSEN`, CREDITS.md paths
  updated). B.3 (d)–(e) yes: `mars-entry` crossfading over 2 s to `mars-dust` at section 3, the eruption from
  the bulge's start.
- 2026-10-18: M7 part D (step AUD-D): round 40's proposals (`sfx_r40.py`, all synthesized, option a each, no
  third-party layers and none bubbly or popping): the dome hit and break, the Choir Herald's song, aura loop,
  absorb ping and collapse chord, the Shellback's walk, lob, curl, roll loop and uncurl, and the `mars-colony`
  ambience; copied into `assets/sfx/` until the pick (`PlaceholderAuditTest` lists them), pending the user's review.
- 2026-10-18 (user, round 39): closed: the tendril hum (S.1), burn (S.2) and break (S.3), the Threadcrawler's walk
  (S.4) and the `mars-canyon` ambience (S.5) accepted as proposed (one variant each, all synthesized), so all five
  `chosen`; the game already ships them. B.4 (b), (d), (e) kept: the scrape at −2 dB panned at the contact, the
  unit loops and the tendril hum's 0.4 while dormant with 0.2 s fades, the crawler's cut as the Coilwyrm's rip at
  pitch 0.8, −3 dB.
- 2026-10-19 (user, round 40): the dome hit, the Herald's song, aura, absorb and collapse, the Shellback's walk, lob, curl, roll and uncurl and the `mars-colony` ambience accepted (S.a, S.c-S.l, `chosen`); the dome break (S.b) is a redo: "It needs a rumble. Look in the online database for cave in or collapse sounds." (step AUD-S).
- 2026-10-19: M7 part E (step AUD-E): round 41's proposals (`sfx_r41.py`, all synthesized, option a each, nothing for
  CREDITS.md, none bubbly or popping): the Dust Devil's spin loop, opening and grit spiral and the Ghost Drone's drop,
  glint, burst and death; not in `assets/sfx/` until E4/EG1 imports them, pending the user's review.
- 2026-10-19 (user, round 41 S.h): the dome break is **c**, the low-passed building collapse over the crust rumble with
  smashing glass (chosen; the recommendation: its longer rumble under the glass is the cave-in asked for); b (the shorter
  sub thump) and round 40's synthesized a moved to `concept/rejected/`. The game plays `dome-break-r40-c.ogg` (step FXS).
- 2026-10-19: M7 part F (step AUD-F): round 42's proposals (`sfx_r42.py`, all synthesized, option a each, nothing for
  CREDITS.md, none bubbly or popping, every blast with a ramped attack): the Revenant Walker's stomp, pod hatch, pod
  missile (an alternative to the planned enemy missile launch r08 a), flamer tell and flame loop, the furnace vents' tell
  and burst, the Warden Tank's tracks loop, turret slew, burst and death, a conveyor belt loop (the plan folds the belts
  into the bed; offered all the same) and the `mars-foundry` ambience; not in `assets/sfx/` until the game step's import
  copies them, pending the user's review.
- 2026-10-20 (user, round 43, S.a-S.n): the Freesound redo and additions, picks: hatch **b**, flamer tell **a**, flame
  **b**, vent tell **a**, vent burst **a**, tank tracks **a**, tank turret **a**, tank burst **a**, walker vent hit
  **a**, walker death **a**, wreck settle **a**, `basin-rumble` **a** (Level 20's second ambience layer), `swarm-hum`
  **a**, `swarm-lunge` **a** (all `chosen`). The other variants (hatch a, flamer tell b, flame a, vent tell b, vent
  burst b, tracks b, turret b, burst b, vent hit b, death b, settle b, rumble b, hum b, lunge b) and round 42's
  synthesized hatch, flamer tell, flame, vent tell, vent burst, tracks, turret and burst, plus its pod missile (round
  08 a is used), moved to `concept/rejected/`; the r42 stomp, tank death, belt loop and `mars-foundry` stay chosen.
  CREDITS.md paths updated.
- 2026-10-20: M7 part H (step AUD-H, round 44's S rows): eighteen proposals from the Freesound database (`sfx_r44.py`, CREDITS.md +54 lines, never synthesized): the Dust Colossus's bulge, eruption, suction loop, bite, death and ridge pass and the Revenant Walker's lance charge, lance shot and stomp shockwave, a and b each. **Provisional picks until the user listens** (step HB; nothing is approved): the game plays **a** for all of them except the ridge pass, which plays **b** (a pans left to right, the ridge travels right to left); the walker's brace tell stays a placeholder. No separate sound for a mouth's death (the death's own sound covers it; round 44, S.j asks). Review points: the bites are slowed bone crunches (crackle check), the loudness against the bed, the 3 s death chain under the 6.1 s sound.
- 2026-10-20 (user, round 44, closed; S.a-S.j): **Colossus** bulge **a**, eruption **a**, suction loop **a**, bite **a**, death **a**, ridge pass **a** (a subway train passing under a bridge, sand flowing off the ridge; the concept file keeps its left-to-right pan and the game plays a render with the channels swapped, right to left, for Level 21's ridge, written by `tools/concept/audio/sfx_r44.py --mirror colossus-ridge-r44-a`); **walker** lance charge **b** (a high-voltage discharge's buzz with Tesla sparks), lance shot **a** (a real railgun), shockwave **a**; S.j: **no mouth's death sound** (the death's own sound covers it). The picks are `chosen`; the nine other r44 files moved to `concept/rejected/` (bulge b, eruption b, suction b, bite b, death b, ridge b, lance charge a, lance shot b, shockwave b; CREDITS.md rows point there). The CC-BY items among the picks go on the in-game credits screen. All part-H sounds are decided, so `art` is `final` for this part.
- 2026-10-21 (user, round 46, closed; J.1-J.22, K.1-K.20): picks (chosen, in `concept/`): ghost-drone-drop b, glint b, burst a, death b; devil spin a, open a, spiral a; shellback curl b, uncurl b, roll b, lob b, walk b; crawler walk a; tendril burn a, break b; burrow dive a; herald aura b; aura absorb b; walker stomp a; tank death a; canopy hum a; mars canyon a, colony b, foundry b; driftjelly pulse b; lamprey drain b; sonde chime a; drone launch a; dash b; overcharge whoosh b, end b; EMP ring b, powerdown a; ion beam stop a; beam impact a; drone rebuild a. **Redo** (both variants moved to `concept/rejected/`, the old sound stays in the game until a better one is picked): tendril hum (the user: "Keep the old one, it's fine", so no redo needed), herald song, leviathan cry, integrity alarm, dome hit, launch rail. The 36 other variants moved to `concept/rejected/` too (CREDITS.md rows point there). The picks are not yet in `assets/` (the import step follows; list in `briefs/r46-picks.md`). Also: the user's general note on sound rows: "You should give a lot more context when asking to judge a sound. I can't judge a sound if I don't know what it's used for and what it's supposed to sound like." Every sound row from now on states where the sound plays, what it is for and what it is supposed to sound like, before the variants. The CC-BY items among the picks go on the in-game credits screen.
- 2026-10-21 (step SNDL): round 46's 36 picks in the game: copied into `assets/sfx/` under their concept names
  (`Sfx`, the `importPlaceholders` list, `PlaceholderAuditTest`, the level sound tests), the credits roll
  regenerated; the 36 files they replace left `assets/sfx/` and are marked rejected here (their files stay in
  `concept/`, since the earlier rounds' boards link them). The tendril hum stays round 39's a; the herald song,
  Leviathan cry, integrity alarm, dome hit and launch rail keep their old files until their redo.
- 2026-10-22 (user, round 47, closed; L.1-L.5): herald song **a** (a crescendoing choir chord with throat singing), Leviathan cry **b** (the humpback moan slowed to 0.8), launch rail **b** (carrier launch with train-coupling clank and steam); integrity alarm: neither, "The old one is fine for that." (both rejected, the old r35 a stays); dome hit: redo, "These are too small. a is a bottle cracking, b is nothing like glass breaking. A dome is a very large structure." (both rejected, a new redo follows in round 49). The picks are still the free-preview cuts: the originals wait for the Freesound download limit, then they are rebuilt and imported (not in `assets/` yet). Rejected files moved to `concept/rejected/`.
- 2026-10-22 (user, round 49, closed; M.1): dome hit **b** (the hammer strike on a glass pane, slowed to 0.85, over a struck glass table's ring an octave down, both played into a real cathedral); a (hammer plus a frozen lake's fractures) and c (a rock on icy lake ice plus a groaning metal door) rejected and moved to `concept/rejected/`; round 40's synthesized r40-a is replaced. The pick is still cut from the free previews: its originals (Freesound 155619, 636555 and the impulse response 648945) wait for the download limit, then it is rebuilt with `tools/concept/audio/sfx_r49m.py` and imported into `assets/`.
- 2026-10-22 (step SND-M; user: the picks go in now, from the preview cuts): rounds 47 and 49's four picks in the
  game, copied into `assets/sfx/` under their concept names from the Freesound preview cuts (`Sfx`, the
  `importPlaceholders` list, `PlaceholderAuditTest`, `Level17SoundsTest`), the credits roll regenerated; herald-song
  r40-a, enemy-leviathan-cry r16-a, launch-rail r11-b and dome-hit r40-a left `assets/sfx/`. Levels as round 46's
  import: each cut is levelled at the replaced file's band level (the loudest 100 ms of the 200 Hz–5 kHz band) under
  its LUFS cap, so they play at the same game levels. The launch rail's stop at 3.55 s (r11-b: 3.6 s) and its
  4.95 s fit the 5 s launch. They still wait for their originals (the Freesound download limit); the rebuilt files
  will replace the preview cuts.
- 2026-10-22 (step AUD48, M8 design phase, decided in round 48 (2026-10-22)): *Act 4 sounds* drafted from the
  level, unit, boss and equipment documents, with five Europa ambience keys; Q12 a proposed (no sfx-bus low-pass,
  heavy under-water recordings, no bubbles). No file made: Freesound answered HTTP 429 (its 500-a-day request limit)
  at the first original download on 2026-10-21, so the a/b proposals follow when the quota resets.
