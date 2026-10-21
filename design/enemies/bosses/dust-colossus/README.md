---
title: Dust Colossus
design: approved
implementation: in-progress
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../campaign/act-3-red-dust, ../../../story/characters/the-choir]
updated: 2026-10-21
---

# Dust Colossus

## Summary

The Act 3 boss (Level 21, Hellas basin): a colony-sized sand organism that burrows under a slowly
scrolling arena and breaches at the edges of the play field, front, sides and rear in turn, then
anywhere under the ship. Each breach raises a mouth (the weak point) that fires rings, sucks the
ship and loose rock toward it and bites. As it dies, the Choir speaks its first words.

**Status: approved in concept round 36 (user, 2026-10-13).** The numbers live in [data.yaml](data.yaml)
since M7 part H (step PH2); every choice below follows the gap analysis's
recommended options; the look is variant **b**, the "Sinkhole colony" (B.2).

## Design

### Stat block

Numbers at **medium**, rendered from [data.yaml](data.yaml) (M7 part H, step PH2); HP in damage units (a Pulse
Cannon L1 shot does 2), bullet classes and contact damage from the [balancing basis](../../README.md#balancing-basis). The reference DPS used is
the **proposed** Level 21 value of **100** (M18 = a: ≈ 76 at Level 15 rising to ≈ 100 at Level 21;
accepted in round 36, confirmed by the Act 3 balance plan in part A).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `buried` (the body: nothing hits it, not even the Smart Bomb; no contact); a mouth is `buried` until the middle of its rise, then `ground` (all weapons, `anti-ground` ×2) until the middle of its dive; the phase-2 spine vents `ground` while up |
| Size tier | `huge` |
| Size | 228×228 px, hitbox 120×120 (the mouth ≈ 150 px across, a 120 px circle; at an edge about two thirds of it on the screen; the body ≈ 900 px long under the sand, seen only as a travelling ridge; each spine vent 40×40) |
| Parts | the mouth (`vital`; the bar): the lips ×0.5, the lime **gullet** a weak spot ×2 while open; three spine vents (`destroyable`, phase 2, outside the bar); the body (`buried`, drawn as the ridge) |
| Orientation | `fixed` (the mouth breaching from the left, right, top and bottom edges, leaning into the field, and straight up in the middle in phase 3, each with its rise, open, bite and dive frames) |
| HP | mouth 5,600; spine vents 280 each (840); total 6440 (easy 4830 / hard 8372, from the global multipliers) |
| Armour / shield | `buried` between breaches and during the first half of a rise and the second half of a dive; the lips ×0.5; the gullet ×2 while the mouth is open |
| Speed | 0 px/s (the mouths do not move once risen; the arena crawls at 20 px/s) |
| Movement | `burrow` with fixed **breach points** around the field (see *Breaches*); between breaches its ridge runs under the sand from the last breach point toward the next |
| Attack | the **suction** (45 px/s within 120 px, falling off to 0 at 360 px); the **bite** (a 0.8 s tell, then `heavy` = 10 within 70 px, once); debris in phases 2-3; phase 3's **eruption** (a 45 px marker on the ship, `heavy` = 10 after 1.5 s; see *Phases*); phase 1: the gullet fires a ring of 12 (`small` = 4, 100 px/s) every 2.5 s while open, each after a 0.5 s lime glow; phase 2: rings of 14 (110 px/s) every 2.5 s; with a 5-way `fan` (spread 50°, 130 px/s, `small`) at the ship between the rings, in phase 1 too (round 45); phase 3: the mouth standing in the field fires a 2-arm `spiral` (4 bullets a second per arm, 120 px/s, `small`) while it stands open; phase 2: each living spine vent vents an aimed 3-round `burst` (130 px/s, `small`) every 3 s while the mouth is open |
| Formations | solo set piece |
| Weak points | lime gullet (while open) (×2) |
| Effective traits | `anti-ground`, `side`, `rear`, `homing` |
| Credits | 480 (score 4800 × chain) in Act 1 terms (mouth 444, spine vents 12 each); × 2.56 × Level 21's `bounty_scale` of 0.50 it pays **614 = 22.7 % of budget(21) = 2,709** (see *Bounty*) |
| Death | `huge`: the last mouth rears up and shudders, the sand splits along the whole ridge, chained eruptions of dust and lime ichor over 3 s, the mouth sinks into a collapsing crater; screen flash at the chain's end (an act boss); credit shower; the Choir speaks (see *The Choir's first words*) |
| First level / used in | L21; L21 only (an echo version in the L49 boss rush) |
| Difficulty hooks | hard: rings of 16, the bite's tell 0.6 s, the pull 55 px/s, phase 3's marker 1.2 s, the spiral's three arms; easy: the pull 30 px/s, the bite's tell 1.0 s, phase 3's marker 1.8 s |
<!-- /data -->

### Breaches

A breach is one cycle of a mouth at a **breach point**:

1. **Tell** (2.0 s): the sand **bulges** and cracks at the breach point, a dust ring spreads, a low
   rumble (no popping). No breach gets an **edge warning** (2026-10-21, user: a boss never triggers
   them); a side or rear point is still fixed **3 s ahead** of the rise (1 s before the bulge), when the
   ridge of its back starts to run under the sand toward it: the bulge and the ridge are its tells.
2. **Rise** (1.5 s): the mouth breaches, leaning into the field; `buried` → `ground` at the middle.
3. **Open** (7 s): the jaws open, the gullet glows lime (the weak spot). The gullet fires (see
   *Phases*) and the **suction** runs.
4. **Bite** (tell 0.8 s, then the snap): the teeth flash and the jaws start to close; at the snap a ship
   or Rook inside the **bite circle** (70 px from the mouth's centre) takes `heavy` = 10, once (the
   Kraken slam's impact rule).
5. **Dive** (1.5 s), then **buried** (2 s, overlapping the next tell): a cycle is ≈ 12 s.

**Breach points** (screen space; the ground scrolls under them and the crater scrolls on with it):

| Point | Mouth centre | Leans | Tell |
|---|---|---|---|
| Front | x 240 (phase 2: the ship's x, clamped 100–380), y 95 (round 44 Q.11: the mouth's 120 px circle clears the boss bar, whose plate ends 35 px below the top edge; was 30) | down | bulge, ridge |
| Left | x 30, y 260 (phase 2: the ship's y, clamped 160–420) | right | bulge, ridge |
| Right | x 450, y as left | left | bulge, ridge |
| Rear | x 240 (phase 2: the ship's x, clamped 100–380), y 515 | up | bulge, ridge |
| Under the ship (phase 3) | the ship's position when the marker is placed, clamped 80 px inside the edges | up (faces the camera) | the marker |

A side or rear mouth faces the ship's sides and tail, where the front guns cannot easily reach it:
the fight tests the **wing mounts**, the **rear** guns and homing (Level 21's traits). The bottom
breach counts as a rear wave for Rook (his Trail formation and rear bark).

**No safe spot on the erupted mouth** (round 44 Q.4, step L21R): a ship parked on the erupted mouth's centre (inside
the no-fire distance) draws no spiral (0 shots a minute measured), but the debris rocks pulled into the mouth hit it:
**9.9** shield and armour lost per 10 s parked (`DustColossusTest`, five seeds at medium), against about 1 per 10 s for
the autopilot's stand-off in phase 3. So the eruption needs no ring; SIM-K's eruption-ring hook stays unused (no data
key).

### Suction and debris

- **Suction** (a radial pull built like Level 12's `Wind` push, but toward a point): while a mouth is
  open, the ship, Rook and the debris drift toward its centre at **45 px/s** within 120 px, falling
  off linearly to 0 at 360 px (easy 30, hard 55; for scale, Level 12's gusts push 40 px/s). Any engine
  outruns it (Mk I 270 px/s): it is a precision hazard that drags a careless ship into the bite, not a
  trap. Bullets are not pulled.
- **Debris** (rock hazards, Q.23d): in phases 2 and 3, rocks are torn loose at the field's far half
  (one every 1.2 s in phase 2, every 0.8 s in phase 3, at random points along the edges away from the
  mouth) and pulled toward the mouth along the suction's lines, accelerating from 60 px/s; the mouth
  swallows them. They are **hazards**, not enemies: `air`, 20–28 px, 2 HP and shootable, contact
  `tiny` = 6, no bounty, not counted in the level's density. (b) would make one rock in four an ore
  chunk, a credit pickup the mouth swallows unless the ship flies into the pull to catch it; rejected
  for now because it moves the level's pickup share.

### Phases

| Phase | Ends at | Behaviour |
|---|---|---|
| 1 — Circling | the mouth below 70 % (round 44 Q.9; was 60 %) | Breaches in a fixed order **front → left → right → rear**, repeating, at the fixed points (the front first, to teach the cycle where the front guns reach it). The gullet fires one **ring of 12** (`small` = 4, 100 px/s) every 2.5 s, each after a 0.5 s lime glow, with phase 2's 5-way `fan` at the ship half an interval after each ring (round 45 B.b; round 44 Q.2's rings every 2.0 s went back to 2.5 s with it). No debris. The first phase: slow rings, fixed points, one mouth at a time. |
| 2 — Hunting | the mouth below 45 % (round 44 Q.9; was 25 %) | The order **rear → left → rear → right → front**, the points following the ship (see *Breaches*). Rings of 14 (110 px/s) every 2.5 s with a 5-way `fan` (spread 50°, 130 px/s, `small`) at the ship in between. Each breach also raises **three spine vents** along the ridge from the mouth toward the field's centre (120, 200 and 280 px from the mouth), `ground`, each venting an aimed 3-round `burst` every 3 s while the mouth is open; they sink with the mouth, and a destroyed vent stays dead. Debris every 1.2 s. |
| 3 — Breaching anywhere | the mouth at 0 | It breaches **under the ship**: a marker (the bulge and a crack ring, 90 px across) is placed on the ship's position and erupts **1.5 s** later (the Polyp Mortar marker's rule: placed at the start, it does not follow); a ship or Rook inside takes `heavy` = 10. The mouth stands open in the field for **5 s** firing a two-arm `spiral` (4 bullets a second per arm, 120 px/s, `small`), the suction at full strength, then bites and dives; 1.5 s buried, then the next marker (a cycle ≈ 10 s). Debris every 0.8 s. An **armour patch** drops as the phase begins. |

### Arena

The Hellas basin floor ([Level 21](../../../campaign/act-3-red-dust/README.md); the level document
places the arena). The scroll eases down to **20 px/s** at the arena's start and keeps crawling
(the gap analysis's "slowly scrolling arena"): the basin's dunes and cracks keep moving under the
breaches, so the backdrop needs ≈ 3,000 px of basin floor (or a seamless tile). The long bar `DUST
COLOSSUS` appears at the arena's start with the warning, the boss checkpoint (none on hard) and the
Vrell boss theme; the level clock halts at the arena section's end while it lives, a kill jumps it
there (the Siege Spire's rule). **The intro**: the Choir sings first ("[the Choir sings]", the static in
the HUD portrait frame: the rule that Choir transmissions precede a Vrell boss), then the first tell.

### Duration and HP

Target: the act-boss window **90–180 s** at medium, aimed at ≈ 130 s; the **par** is the measured mean
(draft **130 s**).

**Measured (step H4, `DustColossusTest`):** the autopilot with the balance plan's Level 21 fit (the Ion Beam L3, two
Micro-missile Pods L2, the Tail Gun L3, Composite III, Rook's Autocannon) in Level 21's crawling arena, five seeds:

| Difficulty | Fight (bar → kill) | Phases 1 + 2 + 3 | Breaches | Shield + armour lost | Won |
|---|---|---|---|---|---|
| Easy | 86.8 s (80.0–89.7) | 17 + 50 + 14 s | 6–7 | ≤ 8 | 5 / 5 |
| Medium | **101.7 s** (100.9–103.1) | 22 + 61 + 13 s | 8 | ≤ 4 | 5 / 5 |
| Hard | 142.7 s (135.5–148.1) | 34 + 80 + 22 s | 11–12 | ≤ 12 | 5 / 5 |

Every medium seed lies inside the window at the spec's HP (mouth 5,600, vents 280), so the HP stays and the **par was
102 s**. The real level gives the same fight (the level report: 101.7 s at medium, 89.3 s easy, 141.7 s hard; no bite
and no eruption reached the ship). Phase 2 is the long one: its rear breaches leave only the Tail Gun and the missiles
on the gullet, the vents and the pulled rocks take shots. Phase 1 with the calibration harness's random dodge: **6.3**
shield and armour lost per 10 s against the Kraken's 11.4 = **0.55×**, below H.4's band of 0.8–1.2× (see *Open
questions*). The ring, the fan and the spiral name the mouth as their firing part (`parts: [mouth]`): its phases end on
HP shares, which name no part, and without it the gullet never fired (found in step H4).

**Re-measured (step L21R, round 44):** phase 3 starts at 45 % of the mouth (Q.9: it lasted ≈ 12 s with one eruption)
and, so that the fight stays in the window with the HP kept (Q.3), phase 1 ends at 70 % (from 60 %: phase 2, the slow
one, keeps its 35 % share); phase 1's rings every 2.0 s (Q.2); the front point 95 px down (Q.11).

| Difficulty | Fight (bar → kill) | Phases 1 + 2 + 3 | Breaches (by phase) | Won |
|---|---|---|---|---|
| Easy | 76.0 s | 10 + 40 + 21 s | 6 (1 / 3.4 / 1.6) | 5 / 5 |
| Medium | **104.3 s** (`DustColossusTest`; the level report 106.6 s, 102.2–109.0) | 17 + 55 + 28 s | 8.8 (2 / 4.2 / 2.6) | 5 / 5 |
| Hard | 131.7 s | 29 + 65 + 32 s | 11 (3 / 5 / 3) | 5 / 5 |

Phase 3 now gets **about 3 eruptions** at medium (2.6 over five seeds; 3.0 in the level's three), and the **par is
104 s**. Phase 1 with the random dodge: **8.0** per 10 s against the Kraken's 11.4 = **0.70×**, still under H.4's 0.8
(the test's lower bound 0.65, pending the user's review; the fan from phase 1 is the next lever).

**Re-measured (step C45, round 45 B.b):** phase 2's 5-way fan in phase 1 too, half an interval after each ring. With
the rings every 2.0 s the random dodge lost **14.9** per 10 s = **1.30×** the Kraken's, over H.4's 1.2; with the rings
back to every 2.5 s (the phase's interval, the fan's too) **13.4 = 1.17×**, inside the band, and the test's lower bound
is H.4's 0.8 again. The fight (`DustColossusTest`, five seeds): medium **106.4 s** (104.6–107.2; phases 17 + 55 + 28 s,
8–9 breaches), easy 73.0 s, hard 137.7 s (132.0–151.8), all won; the level report 104.7 s at medium (100.0–107.1), 3/3
in the window. The **par is 106 s**; the haul +1.1 % at the `bounty_scale` 0.50, so the bounty 480 keeps its 22.7 %.

Paper estimate (window-aware, as the Kraken's `BalanceTest` check): the effective boss DPS is 0.6 × 100
= **60**. While a mouth is open, hits fall on the lips (×0.5) and the gullet (×2), a mean of ×1.25 (the
Kraken's eyes-and-mantle convention); half of the rise and of the dive at ×0.5; nothing while buried.
Phases 1–2: (7 × 1.25 + 1.5 × 0.5) ÷ 12 s ≈ 0.79 → 47.5 DPS; phase 3: (5 × 1.25 + 1 × 0.5) ÷ 10 s ≈
0.68 → 40.5 DPS.

| Phase | HP | Time |
|---|---|---|
| Arrival, the Choir and the first tell | — | ≈ 6 s |
| 1 — Circling (100 → 60 %) | 2,240 | ≈ 47 s |
| 2 — Hunting (60 → 25 %; the vents not counted) | 1,960 | ≈ 41 s |
| 3 — Breaching anywhere (25 → 0 %) | 1,400 | ≈ 35 s |
| Total | 5,600 | **≈ 129 s** |

Lessons from Act 2 that the build must apply (M7 part H):
- **Measure, then tune the HP, not the attacks** (Kraken E6, Spire H10): the autopilot with the balance
  plan's Level 21 fit over five seeds, every seed inside 90–180 s, the par at the measured mean. The
  paper ignores how hard the edge mouths are to reach with front guns (longer) and the plan's
  `anti-ground` and ×2 hits (shorter: the Spire ran at ≈ 0.6 of its paper length).
- **A kinder phase 1** (Spire round 34 P2): the calibration harness's random-dodge driver should lose
  about what it loses to the Kraken (≈ 10 armour per 10 s) or less; the levers are the ring's speed
  and count and the pull.
- The autopilot (and Rook) must leave the bite circle before the snap, step out of phase 3's marker
  and work against the pull, or the tests cannot finish the fight.

### Bounty

An act boss aims at ≈ **30 %** of its level's budget (band 20–40 %). Data is in Act 1 terms: **bounty =
share × budget(21) ÷ (2.56 × `bounty_scale`)**. [Level 21](../../../campaign/act-3-red-dust/level-21-dust-colossus/README.md)
sets its `bounty_scale` at **0.50** (step L21R's measured fit after round 44's wave rework; step H4's 0.74, the paper
value 0.75), and the boss there is **480** (mouth 444, vents 12 each), paid 568 + 3 × 15 = **614 = 22.7 %** of
budget(21) = 2,709 (H.5); the perfect run is 1.48 × the budget.

**Why 22.7 %, not 30 %** (accepted in round 36, Q.21a), as the [Siege Spire](../siege-spire/README.md)'s
26.8 %: a perfect run must earn at least **1.4 × the budget** (`TypicalHaulTest`), and the boss pays
the same in the typical and the perfect run, so every credit it takes off the kills narrows the gap
between them. With Level 21's mix the bound holds up to a share of about **24.8 %**; at 30 % (scale
≈ 0.71, the boss ≈ 445 in Act 1 terms) the perfect run reaches only ≈ 1.38 ×. 22.7 % is inside the
act boss's band (`BalanceTest`). A larger share needs a different mix in the level (a larger secret
or more air bounty), not more density. The draft's first figure, 790 at an assumed scale of 0.40
(29.9 %), is dropped.

### The Choir's first words

M14 = a (kept in round 36): the Choir first **speaks** as the Colossus dies; in Acts 1–2 it
only sang.

- At the death (the chain's start), the HUD portrait frame fills with the [Choir](../../../story/characters/the-choir/README.md)'s
  glyph and static, and the subtitle reads, as the Choir's own words for the first time:
  **"…many… we are many… we are going…"** (its Act 3 sample line). Voiced with the Choir's layering on
  speech, not the sung stage sound ([voice](../../../audio/voice/README.md); the audio step briefs it).
- After the chain and the flash: **Dr. Varga**: "Commander… that was not singing. Those were words."
  **Okafor**: "I heard it, Doctor. Log everything." Then the level's closing lines and the act-end
  order (the debrief with the act summary, the outro, the hangar), owned by the Level 21 document.
- "We are going" pays off the story beat: the Vrell pull back from Mars toward Jupiter's moons.

### Presentation

- **Look** (the chosen concept b, "Sinkhole colony", round 36 B.2): a Vrell colony grown from sand
  and chitin, the colour of Hellas dust. At a breach the ground **collapses into a funnel**, and the
  mouth is **four bone mandibles** unfolding round the lime throat (the gullet; the mandibles are its
  jaws and its lips), seen from above; **violet-lit vein ridges** run under the sand round the
  funnel and along the body's ridge. The weak points glow lime (the gullet, the spine vents), as
  every Vrell weak point.
- **The ridge**: between breaches the body's back moves under the sand as a travelling dune ridge with
  a dust haze (drawn by the game), so the player can read where it goes.
- **Rise and dive**: the sand slumps into the funnel as the mandibles unfold, a dust ring and
  falling grains; at the dive they fold and the sand pours back; the crater stays and scrolls on
  with the ground.
- Every mouth stays visibly connected to the body under the sand (the water rules' lesson from the
  Kraken: it never pops in).

## Concept art

Proposals: concept round 36 (step A-bosses, listed in the [bosses' Concept art
table](../README.md#concept-art); prompts in [../concept/prompts.md](../concept/prompts.md#dust-colossus-r36-a),
generator `tools/concept/bosses_r36.py`); round 36 (2026-10-13) chose **b** (B.2), a is rejected:
[a "Rearing maw"](../concept/rejected/dust-colossus-r36-a.png) ([GIF](../concept/rejected/dust-colossus-r36-a.gif)),
a segmented slate worm arching out of a cracking sand dome, three jaw plates around rings of teeth
and a lime throat; and [b "Sinkhole colony"](../concept/dust-colossus-r36-b.png)
([GIF](../concept/dust-colossus-r36-b.gif)), the ground collapsing into a funnel, four bone mandibles
unfolding around a lime throat, violet-lit vein ridges under the sand.

Production art: [dust-colossus-final-r44-a.png](../concept/dust-colossus-final-r44-a.png) ([GIF](../concept/dust-colossus-final-r44-a.gif)), `tools/art/dust_colossus.py` (sprite names and frame layout in its docstring; held in `build/art-held/` until this part's `data.yaml` claims the root), prompt in [../concept/prompts.md](../concept/prompts.md#dust-colossus-final-r44-a); accepted in round 44 (2026-10-20).

Round 45 (round 44 K.b, step ART-K): [dust-colossus-final-r45-a.png](../concept/dust-colossus-final-r45-a.png) ([GIF](../concept/dust-colossus-final-r45-a.gif)): the spine behind an edge mouth with the vents as organs on its segments, and the death throe (rearing, the geysers along the ridge, the fissure ring, debris, the collapse, the dust plume); prompt in [../concept/prompts.md](../concept/prompts.md#dust-colossus-final-r45-a); frame rules for the game in the step's art-names note; round 45 (2026-10-21): the death throe and the debris rocks accepted; the spine is to be re-rendered as one continuous ridged back (step ART-S, planned, a new r46 row), until then the r45 spine stands.

Round 46 (round 45 A.a, step ART-S): [dust-colossus-final-r46-a.png](../concept/dust-colossus-final-r46-a.png) ([GIF](../concept/dust-colossus-final-r46-a.gif)): the spine as one continuous ridged back (dark shingled plates over the violet hide, sand berms on the flanks; same frames and names, no game change); prompt in [../concept/prompts.md](../concept/prompts.md#dust-colossus-final-r46-a); proposed, pending the user's review.

Round 46 b (the user on r46: the spine "just appears one segment at a time, like it's warped in"; step GSR): [dust-colossus-final-r46-b.png](../concept/dust-colossus-final-r46-b.png) ([GIF](../concept/dust-colossus-final-r46-b.gif)) and the in-game capture [level-21-spine-r46-b](../../../campaign/act-3-red-dust/level-21-dust-colossus/concept/level-21-spine-r46-b.mp4): the back rises out of the ridge's silhouette head to tail and sinks back tail to head (a game change, no new frames); prompt in [../concept/prompts.md](../concept/prompts.md#dust-colossus-final-r46-b); proposed, pending the user's review.

## Implementation

- [x] The `buried` layer (M7 = a; appended after `sub` so the ordinals and replay hashes stay): nothing
      hits it, not even the Smart Bomb; no contact (the layer from part B's Burrower; the Colossus's buried mouth is shielded, step PH1: `BreachArenaTest`)
- [x] Stat block and boss script in a `data.yaml` (breach points, the cycle, phases, par), loaded;
      global difficulty multipliers applied (M7 part H, step PH2: `movement.breach`, `boss.suction`,
      `vents`, `eruption`, `debris` and the hooks, checked by `ColossusChecks`, mapped by `ColossusRules`)
- [x] Breaches: the tell, the point fixed 3 s ahead (no edge warning since 2026-10-21), rise, open, bite, dive; the points per
      phase; the layer flips at the middles; the bottom breach as a rear wave for Rook (step PH1: `BreachArena`, `BreachArenaTest`; the bite lands by the centre within 70 px, a cycle is 12.8 s)
- [x] The suction (a radial pull on the ship, Rook and debris) and the debris rocks as hazards (steps PH1/PH3: `BreachArenaTest`, `BreachHazardsTest`; debris stops the player's shots only)
- [x] Phase 2's spine vents; phase 3's marker under the ship and the eruption; the drops (step PH3: `BreachHazardsTest`; step L21R: the erupted mouth is no safe spot, round 44 Q.4, `DustColossusTest`)
- [x] The slow arena (20 px/s), the level clock halting at the section's end while it lives (steps PH1/HA: `CrawlingArenaTest`)
- [x] Rook and the autopilot: out of the bite circle and the marker, against the pull (sim step PH3: Rook;
      step H4: `ColossusPlay`, the suction and the pulled rocks in the planner's look-ahead)
- [x] The fight measured over five seeds (101.7 s at medium, every seed 90–180 s), the HP kept, the par set
      (102); phase 1 checked with the calibration harness (0.55× the Kraken's; round 44 Q.2); the bounty
      at 22.7 % with the final scale 0.74 (`DustColossusTest`, step H4; step L21R after round 44's Q.2, Q.9 and Q.11:
      104.3 s, par 104, phase 3 ≈ 3 eruptions, phase 1 0.70×, the bounty 480 at the scale 0.50; step C45 after
      round 45 B.b, the fan in phase 1 and its rings every 2.5 s: 106.4 s, par 106, phase 1 1.17×, the test's band
      0.8–1.2× again)
- [x] Intro (the Choir sings), death (chain, flash, credit shower), the Choir's first words and the
      lines after them (step HB: the death's own sound, the local flash and the credit shower; step VOX-H: the Choir's words as one take, round 44 Q.7)
- [x] Production art (≤ one atlas page): the mouth in its 5 poses with rise, open, bite and dive frames,
      the spine vents, the bulge and the crater, the ridge, the death, the intel silhouette (steps ART-H2, HA: `tools/art/dust_colossus.py`, drawn by `ColossusLooks`; `art` stays `chosen` until the user approves it in round 44, F.c)

## Open questions

- Pending the user's review (step C45, 2026-10-21): with phase 2's fan in phase 1 (round 45 B.b) and the rings every
  2.0 s phase 1 measured 1.30× the Kraken's, over H.4's 1.2, so its rings (and the fan between them) went back to
  every 2.5 s: **1.17×**, inside the band (round 44 Q.2's 2.0 s undone by this step's choice). **Phase 1 ends at 70 %** (from 60 %) so that phase 3's 45 % share (Q.9) keeps
  the fight in the window with the HP kept; with phase 1 at 60 % the fight fell to ≈ 92 s with 83–97 s seeds.
- Built (steps ART-K, GK, SIM-K, round 44 K.b/K.c): the spine with the vents as organs on its segments, the 4.5 s death throe, the ground holding still while the Colossus is out and the brighter bite circle (Q.10). **Pending the user's review (round 45):** at 1x the spine's humps read as a chain of pale bubbles (one continuous ridged back is proposed), the debris rocks read dark, and phase 1's 0.70x pressure (a 5-way fan in phase 1 is proposed; built in step C45: 1.17x).

## Decisions

- 2026-09-30: Drafted in the bosses roster as the Act 3 boss (L21).
- 2026-10-12 (overnight design phase): promoted from the roster to a draft spec (all accepted in round 36 (2026-10-13)): the body `buried` (M7 = a) with breach points at the four edges and,
  in phase 3, under the ship; a breach cycle of tell (bulge, edge warning 3 s ahead for the sides and
  rear), rise, open, bite and dive; three phases (Circling, Hunting, Breaching anywhere); a radial
  suction at 45 px/s on the ship, Rook and debris; debris as rock hazards (a; b, ore pickups, rejected
  for the pickup share); phase 2's spine vents as `anti-ground` targets; HP 5,600 + 840 from the
  proposed reference DPS 100 (paper ≈ 129 s, par 130 s until measured); bounty 790 in Act 1 terms
  (≈ 30 % at an assumed `bounty_scale` of 0.40); the arena crawling at 20 px/s; M14 = a, the Choir's
  first words at its death after singing at its intro.
- 2026-10-12 (merge): the bounty follows Level 21's numbers: 300 in Act 1 terms (mouth 264, vents 12
  each) at its `bounty_scale` of 0.80, paid 614 = 22.7 %, capped by the perfect-run ≥ 1.4 × bound
  (≈ 24.8 % at most with the level's mix), as the Siege Spire's 26.8 %; replaces 790 (29.9 % at an
  assumed 0.40). The round-36 a/b proposals are listed under Concept art (`art: proposed`).
- 2026-10-13 (user, round 36): approved (D.19), M14 = a kept. Q.23 yes: the suction pulls the ship
  gently (any engine outruns it), the debris is rock hazards, phase 3 breaches under the ship; Q.21a:
  the bounty share 22.7 % (300 in Act 1 terms at Level 21's scale 0.80). **B.2 = b**: the "Sinkhole
  colony" chosen (a funnel collapsing in the sand, four bone mandibles round a lime throat, violet
  vein ridges); the *Look* follows it. The "Rearing maw" worm (a) rejected and moved to
  `../concept/rejected/`.
- 2026-10-20 (user, round 43, part H's answers that touch the boss): phase 1's calibration band is **0.8-1.2x the Kraken's 11.4 per 10 s** with the random-dodge harness (H.4); the boss keeps its **22.7 % bounty share** while the perfect run stays at least 1.4x (H.5); the arena tile repeats at 512 px, section 6's tiles start at the next seam and the collapsed crater and the furrow are props placed at the kill (H.6); the suction pulls no pickups, debris stops the player's shots but not the enemy's bullets and a phase-2 point is fixed when its edge warning starts, 3 s before the rise, the front one at its tell (H.7 b, c, h). The level's own answers are in [Level 21](../../../campaign/act-3-red-dust/level-21-dust-colossus/README.md#decisions).
- 2026-10-20: production art made (step ART-H2): 72 px/unit (the mandibles' open reach trimmed to 1.5 units for the one-page budget), five poses (edge breaches leaning 30° into the field on the body's neck), the bulge cracks violet as concept b; accepted in round 44 (2026-10-20).
- 2026-10-20 (step PH2, M7 part H; accepted in round 44 (2026-10-20)): the `data.yaml`: the spec's paper HP (5,600 +
  3 × 280) and par 130 for step H4 to measure; the bounty 320 (mouth 284) for the 22.7 % share at Level 21's paper scale
  0.75; the points in screen space, turned into the simulation's frame by `ColossusRules`; defaults: the gullet's box
  70×70, the vents' aimed bursts `small` at 130 px/s, the spiral's arms turning 90°/s, the debris accelerating at
  60 px/s² (sim step H3's suggestion), the hard rings of 16 in both phases.
- 2026-10-20 (step H4, M7 part H; accepted in round 44 (2026-10-20)): the fight flown and measured
  (`DustColossusTest`, `Level21Test`, the level report's `colossus` line). The ring, the fan and the spiral name the
  mouth as their firing part (without it the gullet never fired: its phases end on HP shares). At the spec's HP the
  medium fight lasts 101.7 s on five seeds (100.9–103.1, inside 90–180 s): the HP stays, the par is the measured
  102 (was the draft 130). Level 21's measured `bounty_scale` 0.74 makes the bounty 325 (mouth 289) for H.5's 22.7 %.
  Phase 1's calibration measures 0.55× the Kraken's, under H.4's band: an open question, not tuned (the attacks are
  the user's).
- 2026-10-20 (steps HA and HB, M7 part H; accepted in round 44 (2026-10-20)): the game draws the Colossus (`ColossusLooks`: the poses, the funnel, the bulge, the marker, the gullet's glow, the craters, the vents, the rocks and the death frames), plays the nine round-44 sounds as provisional picks (bulge, eruption, bite and suction loop panned to the mouth; the death's own sound; the ridge pass *b*) and flashes locally at the death with the credit shower; no separate sound for a mouth's death (round 44, S.j). Asked in round 44: the safe spot on the erupted mouth (Q.4).
- 2026-10-20 (user, round 44, A.b (b): its last `proposed` rows are decided (round 44 and the closed rounds 41-43)): **`art: final`** for this part. A later addition (a new sprite, a new concept round) is its own row and does not reopen the rest.
- 2026-10-20 (user, round 44, the boss's rows): **K.b change**: "What are those turrets popping up out of the ground? I think they should be part of the Colossus, but then they should be placed on its spine, not like this in the sand. They have the same moving issue as the creature itself (see below).  I think the creature also needs something extras. The few small blasts don't do such a large creature justice. Think of something to add." **K.c change**: "The ground moves underneath the Colossus. When the Colossus is out of the ground, the background cannot move, or the Colossus must move together with the ground." Planned (steps SIM-K, ART-K, GK, L21R; nothing ticked until built): the crawl **halts while the Colossus is out of the ground** (the floor and the Colossus no longer slide against each other), the spine **vents become organs on its spine** (they move with it and are part of the boss; the current sand turrets go) and a **death throe** is added (a longer, heavier death than the few small blasts). **F.c** the production art approved as final (`art: final`; the new organs and the throe are their own art rows). **B.a-B.d** (the breach machinery, the vents and debris, the data and the measured fight, every sub-choice) stand as built. **Q.2** the rings every 2.0 s (from 2.5), then measured; **Q.3** keep the par 102 s and 5,600 HP; **Q.4** re-measure the safe spot with a ship parked on the mouth, then a short ring (about 100 px, `medium`, a tell as the walker's footfall) if it is still safe; **Q.9** raise phase 3's share of the mouth's HP so that it gets about 3 eruptions, measured (the HP total unchanged); **Q.10** a faint 70 px bite circle on the sand during the tell; **Q.11** the front breach point moves down to clear the boss bar. The sounds (S.a-S.f): all **a**, the ridge pass mirrored for the right-to-left track; S.j no sound for a mouth's death. The Choir's death line accepted as one take (V.a, Q.7).
- 2026-10-20 (step L21R, round 44's Q.2, Q.3, Q.4, Q.9 and Q.11 built; pending the user's review): phase 1's rings
  every 2.0 s (Q.2): the random dodge loses 8.0 per 10 s, 0.70× the Kraken's (from 0.55×), still under 0.8, reported,
  the test's bound 0.65. Phase 3 from 45 % of the mouth (Q.9; from 25 %) and phase 1 to 70 % (from 60 %, our choice:
  at 60 % phase 2 shrank to 10 % and the fight to ≈ 92 s, under the window on some seeds): phase 3 gets 2.6–3 eruptions
  at medium. The HP kept (Q.3): the fight 104.3 s, the par 104. The front point 95 px below the top edge (Q.11; from
  30): the mouth's 120 px circle clears the boss bar (its plate's bottom 35 px down, `BossBar`). The erupted mouth
  re-measured with a ship parked on it (Q.4): no spiral (inside the no-fire distance), but 9.9 lost per 10 s to the
  pulled rocks, so no safe spot and no ring (SIM-K's hook unused). The bounty 480 (mouth 444) for H.5's 22.7 % at
  Level 21's new scale 0.50.
- 2026-10-20 (steps SIM-K, ART-K, GK): the crawl halts while the Colossus is out (K.c); the spine is 4 segments x 3 states x 16 headings with the vents on it (K.b), with a soft spine shadow; the death throe lasts 4.5 s (rear, collapse, geysers tail to head, or a geyser ring round the mouth when the ridge is short, fissure ring, 18 rocks, plume, shockwave 2.9 s, shake up to 7 px, flash 4.2, credits 4.5); the bite circle, the stomp and the brace rings are brighter. Proposed in round 45: the spine as one continuous ridged back, lighter debris.
- 2026-10-21 (user, round 45): A.b the death throe approved, A.d the debris rocks accepted as they are; A.a the spine reads as bubbles: re-render as one continuous ridged back (step ART-S, planned); B.a the Choir's words at the death get a 4.5 s delay (the roar drowns them; C45 + G45, planned); B.b the 5-way fan of phase 2 is added to phase 1 (C45, planned).
- 2026-10-21 (step C45, round 45 B.b): phase 1 fires phase 2's 5-way fan half an interval after each ring. With the
  rings every 2.0 s the random dodge lost 1.30× the Kraken's (over H.4's 1.2), so phase 1's rings went back to every
  2.5 s (our choice, pending the user's review): 1.17×, the test's lower bound 0.8 again. The fight 106.4 s at medium,
  par 106 (from 104); the haul +1.1 % at the scale 0.50, the bounty 480 (22.7 %) kept. B.a's Choir delay is Level 21's
  (its radio line's `delay: 4.5`).
- 2026-10-21 (user, after playthroughs): "The walker also seem to show warnings. This takes away the
  surprise effect that should go with a boss fight. Bosses must never trigger these warnings." Its
  side and rear breaches no longer show an edge warning or a wave banner (nor Rook's rear bark); the
  bulge, the ridge and the boss warning banner at its arrival stay, as does Rook's Trail on a rear
  breach (`BreachArenaTest`). The Wraith pairs of Level 21 lose their re-entry warning too (their
  own flight pattern).
