---
title: Revenant Walker
design: approved
implementation: in-progress
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../campaign/act-3-red-dust, ../../../story/twist]
updated: 2026-10-20
---

# Revenant Walker

## Summary

The Act 3 mid-boss (Level 19, Foundry of Tharsis): a grey, unmarked, human-built war walker that
strides through the foundry's casting yard in passes from every edge. Homing missile pods on its
shoulders cover the ground ahead of it; a rear-facing flamer burns whoever slips past it. Its wreck,
serial plates filed off, is the twist's **hint 2**.

**Status: approved in concept round 36 (user, 2026-10-13).** Numbers are hand-written tables at
medium (no `data.yaml` until M7 part F builds it); every choice below follows the gap analysis's
recommended options; the look is variant **a**, the "Strider heavy" biped (B.1).

## Design

### Stat block

Numbers at **medium**, rendered from [data.yaml](data.yaml) (M7 part F, step F2); HP in damage units (a Pulse Cannon L1 shot does 2), bullet classes and
contact damage from the [balancing basis](../../README.md#balancing-basis). The reference DPS used is
the **proposed** Level 19 value of **92** (M18 = a: ≈ 76 at Level 15 rising by about 4 a level to 100
at Level 21; accepted in round 36, confirmed by the Act 3 balance plan in part A).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Unmarked |
| Layer | `ground` (it walks on the yard: no contact with the ship or Rook; all weapons hit it, `anti-ground` ×2) |
| Size tier | `huge` |
| Size | 170×190 px, hitbox 110×120 (the hull ≈ 110×120 on two reverse-knee legs, a biped: B.1 = a); hitboxes: hull 110×120, front plate 110×30 on the heading side, each pod 36×36 on a shoulder (±53 px, the art's mounts), the vents 50×24 on the back |
| Parts | hull (`vital`; the bar); front plate (shots glance, no HP, not in the bar, covers the hull from the heading side); left and right missile pod (`destroyable`, on the shoulders, outside the plate's cover; round 44: armoured while its hatch is shut, until the overheat); the back-mounted rail lance (not destroyable); the reactor vents (a weak spot on the hull's back, ×2 while open); the rear flamer (a hardpoint under the vents, not destroyable); the legs (drawn, no hitbox) |
| Orientation | `8 angles` (45° steps; the passes walk the 4 cardinal ones, the turns use the diagonals) × an 8-frame walk cycle; pods snap to the ship's bearing (16 headings) |
| HP | hull 1,190; pods 80 each; total 1350 (easy 1012 / hard 1755, from the global multipliers) |
| Armour / shield | the front plate turns every shot that meets it from ahead (it stops the Ion Beam too); a pod turns every shot while its hatch is shut (round 44: only its 0.4 s hatch tell and its salvo open it; from phase 3 the hatches jam open); the back takes full damage; the vents ×2 while open (2 s after each burn, always in phase 3) |
| Speed | 45 px/s (phase 1), 55 px/s (phase 2), 60 px/s (phase 3); turns 90° in 1.5 s |
| Movement | `walk` in **passes** across the halted casting yard (see *Passes*); off the screen 3 s between passes, under an edge warning |
| Attack | the rear **flame cone** (240 px, ±32°, a 0.6 s tell, a 1.5 s burn, `heavy` = 10 once per burn, at most every 3 s; from phase 2 its burn follows the ship at 40°/s; see *Attacks*); the back-mounted **rail lance** once both pods are gone and in phase 3 (round 44: a 1 s charge, a glowing line from its back through where the ship was, then a 0.3 s shot along it, `heavy` = 10 once a shot, every 4 s, 3 s in phase 3); the **stomp** (round 44: the ship or Rook within 90 px of its centre: a 0.5 s brace, then a ring out to 110 px over 0.3 s, `medium` = 6 once a stomp, at most every 4 s); phase 1: the pods take turns firing `homing` missile salvos of 3, one 0.2 s after the other, every 3.5 s (110 px/s, turning 90°/s, 4 s life, 2 HP each, at most 8 alive, `medium` = 6; the hatch opens 0.4 s before: the tell); from phase 2 each living pod fires salvos of 3 every 4 s, staggered; the chin cannon's `fan` of 3 over 30° in a `burst` of 2 (0.2 s apart, 150 px/s, `small` = 4) every 2 s, along its heading in phase 1; phase 2: `aimed` at the ship (3 over 24°, 160 px/s); phase 3: 5 over 40° every 2.4 s (170 px/s); the hull's **flank guns** (round 43): a `burst` of 3 out of both flanks, square to its heading (0.25 s apart, 120 px/s, `small` = 4) every 2.2 s in phases 1 and 2: a dashed line across the lanes beside its own; 4 every 2.2 s in phase 3 (130 px/s) |
| Formations | solo set piece |
| Weak points | hot-amber reactor vents (while open) (×2) (exposed machinery: they glow heat orange when open) |
| Effective traits | `rear`, `anti-ground`, `homing`, `side` |
| Credits | 495 (score 4950 × chain) in Act 1 terms (hull 395, pods 50 each); × 2.56 (Act 3 factor 1.6²) × Level 19's `bounty_scale` of 0.28 it pays **355 = 15.0 % of budget(19) = 2,366** (see *Bounty*) |
| Death | `huge` machine variant: the legs buckle, it topples forward along its heading over 2 s with chained explosions, the hull lands as a smoking **wreck** that stays (scenery) for the hint-2 beat; credit shower; no screen flash (act bosses only) |
| First level / used in | L19; mid-boss of L19 only |
| Difficulty hooks | hard: missiles turn 120°/s, the phase-2 chin fan of 5, flank bursts of 4 (6 in phase 3), the flame cone 270 px, the swivel ±45° (round 44: its salvos of 4 in phase 1 dropped, the armoured pods fire longer); easy: salvos of 2, single chin fans in phase 1, flank bursts of 2 (3 in phase 3), the flame cone 210 px, its tell 0.8 s, the lance's charge 1.3 s every 5 s (3.8 s), the stomp's brace 0.7 s |
<!-- /data -->

### Attacks

Round 43 (step WR, the user in round 42, K.a: "The walker looks like a real easy target. It needs to do a lot
more shooting and the flame needs to be bigger."): every phase now fires its pods, its chin and its new flank
guns, and the cone is half as long again and wider. All numbers at medium; the stat block above is the
data's.

- **Homing missiles** (`homing`, new for enemies: no enemy fires them yet). Each pod fires a salvo,
  one missile 0.2 s after the other: 110 px/s, turn rate 90°/s, 4 s life then they fizzle, `medium` = 6
  on a hit, **shootable** (2 HP; homing shots, bolts and Rook all hit them), a grey smoke trail. At most
  8 alive. A pod's hatch flips open 0.4 s before its salvo (the tell). Phase 1: salvos of **3**, the pods
  taking turns, one every 3.5 s; from phase 2 each living pod every 4 s, staggered (round 43: 3 s; round 44:
  4 s, the armoured pods live longer). **Round 44: a pod is armoured while its hatch is shut**: shots glance off
  it and it stops the Ion Beam, as the front plate does; only its 0.4 s tell and its salvo (≈ 0.8 s at medium)
  open it to fire. From phase 3 the hatches jam open as the reactor overheats. A pod has 80 HP: the plan's fit
  lined up on an open hatch deals ≈ 50–85 in a window, a typical (partly lined-up) window less, so a pod falls in
  ≈ 2–3 open windows.
- **Chin cannon** (`fan` in a `burst`): phase 1 a fan of 3 over 30° twice, 0.2 s apart, every 2 s,
  **along its heading** (not aimed: it sweeps the lane it walks, so the first phase stays readable); phase 2
  a fan of 3 over 24° three times, **aimed**, every 2 s; phase 3 a fan of **5 over 40°** three times every
  2.4 s, faster (170 px/s). `small` = 4.
- **Flank guns** (new in round 43; a `fan` of 2 over 180° along its heading: one bullet out of each
  flank, square to its walk): a burst of 3, 0.25 s apart, every 2.2 s in phases 1 and 2, of 4 in phase 3,
  120–130 px/s, `small` = 4. As it walks they leave a dashed line across the lanes beside its own: in
  phase 1, where the ship is (the lane beside the ship's), the ship slips between the dashes or keeps ahead
  of or behind them.
- **Rear flame cone** (new attack shape, built on the `laser-sweep`'s beam test with a cone instead of
  a line): a cone **240 px** long, **±32°**, from the flamer on its back, pointing **against its heading**
  (round 43: was 150 px, ±22°). It fires when the ship (or Rook) is inside the cone's reach + 40 px behind
  it, at most once every 3 s: a **0.6 s tell** (the pilot light flares blue-white, heat shimmer along the
  cone's outline, a hiss), then a **1.5 s burn**; a ship touched by the burn takes `heavy` = 10, **once per
  burn** (the laser's once-per-sweep rule). From phase 2 the tell swivels up to **±35°** toward the ship and
  the burn keeps **following the ship at 40°/s** within that swivel (`track`). After each burn the reactor
  **vents open for 2 s** (the weak spot, ×2): the reward for staying behind it, just outside the cone. The
  flame is drawn above the ground layer, bright and opaque enough to read over the foundry's orange glow;
  its puffs grow with its length and their number with its area (`WalkerLooks`).
- **Rail lance** (round 44, the user in round 43: "When both missile hatches are destroyed, the walker is
  mostly toothless … something else for the longer range"): a lance on its back wakes **once both pods are
  gone, and in phase 3** (pods or not). A second after it wakes, and then every **4 s** (**3 s** in phase 3),
  while it walks on the screen, it **charges for 1 s**: a red line glows from its back through the
  point where the ship was as the charge began, out to the field's edge, pulsing brighter as the charge runs
  out; then it **fires for 0.3 s** along that line: a bright beam 14 px wide, `heavy` = 10 once a shot to the
  ship and to Rook where it touches them (a white-hot core in a red glow; red to keep it apart from the Ion Beam's
  blue and the flame's orange), a small white-hot flash where it hits (never the screen). The line
  pivots on its point as the walker walks on: a ship that stays put is hit at any range, one that keeps moving
  is not. Rook steps off the line while it charges or fires.
- **Stomp shockwave** (round 44, the user in round 43, W.e: "stomp shockwave"): the ship or Rook within
  **90 px** of its centre starts a **0.5 s brace** (the legs brace, a dusty ring marks the ground at the
  stomp's reach, its rim brightening), then it stomps: a ring of dust expands from where it stands to
  **110 px** over 0.3 s, `medium` = 6 once a stomp to whoever it reaches; at most one every **4 s**. No more
  safe spot over or against it (its guns do not fire within 72 px of it). Rook keeps out of the ring.

### Passes

The scroll eases to a halt in the foundry's casting yard (the arena; the level document places it).
The walker never stands still: it crosses the yard in **passes**, each from one edge to the opposite
one, then steps off the screen.

- **Lanes**: vertical passes walk on x = 120, 240 or 360; horizontal passes on y = 200 or 330.
- **Lane choice**: in **phase 1** the lane *beside* the ship's (the lane nearest the ship's x, moved
  one toward the field centre; from the centre lane left and right alternately), so it strides past the
  ship instead of over it; from **phase 2** on the ship's own lane (nearest the ship at the pass's
  start).
- **Entry edges** cycle **top → bottom → left → top → bottom → right** (then repeat). The first pass is
  its intro from the top. Between passes it is off the screen for **3 s**, under the **edge warning**
  for its next entry (`WarningEdge` BOTTOM, LEFT or RIGHT, 3 s ahead, computed from the pass schedule
  so it is deterministic; a top entry needs none). A bottom entry counts as a rear wave for Rook (his
  Trail formation and rear bark), as the Siege Spire's Wraiths do.
- **Why passes**: the ship's front guns fire up and its rear guns down. A walker striding down the
  screen shows its plated front to the ship's front guns (only the pods are open to them) until it has
  passed; then its back, vents and flamer face the ship from below, in the **rear** guns' field. On an
  up-pass the roles swap. Wing mounts firing sideways reach it on horizontal passes. The fight thus
  tests rear and side weapons (Level 19's traits) and teaches "get behind it, but not in the cone".
- A vertical pass is ≈ 730 px (field + body) ≈ 16 s at 45 px/s; a horizontal pass ≈ 650 px ≈ 14 s.
- **Rook** keeps out of the cone's reach during a tell and a burn (his slot moves aside, as from a
  telegraphed lane at the Harbour Kraken), shoots the pods first in phase 1, and is burned like the ship.
  The test autopilot does the same, so the balance and playthrough tests can finish the fight.

### Phases

| Phase | Ends at | Behaviour |
|---|---|---|
| 1 — Stride | both pods destroyed, or the hull below 60 % | Passes at 45 px/s in the lane beside the ship. The pods fire **salvos of 3, one pod at a time**, alternating, every 3.5 s; the chin's fans along its heading; the flank guns; the flamer behind it. |
| 2 — Hunt | the hull below 30 % | Passes at 55 px/s in the ship's lane. Each living pod fires salvos of 3 every 4 s, staggered (still armoured while shut); with both pods gone the **rail lance** charges and fires every 4 s; the chin's fans are `aimed`; the flank guns; the flamer **swivels ±35°** toward the ship during its tell and its burn follows the ship. A destroyed pod stays dead (its shoulder sparks). |
| 3 — Overheat | the hull at 0 | The reactor overheats: the vents **stay open** (×2 for the rest of the fight) and glow white-orange. It stops leaving the yard: at the end of a pass it **turns round on the spot** (1.5 s) and walks back in the ship's lane at 60 px/s; **the flamer burns through the whole turn**, sweeping its cone through 180° behind it (the tell 0.6 s before the turn starts). Fiercer guns: the chin's fans of 5 over 40°, the flank guns' bursts of 4; living pods keep their phase-2 salvos, their hatches jammed open (no armour). The **rail lance** wakes whatever the pods, every 3 s. A **shield cell** drops as the phase begins. |

### Duration and HP

Target: the mid-boss window **45–75 s** at medium, aimed at ≈ 60 s; the **par** is the measured mean:
**51 s** (round 44, below; round 43's rework: 57 s; step F4: 53 s).

Paper estimate (the window-aware method of the Kraken's `BalanceTest` check): the effective boss DPS
is 0.6 × 92 = **55.2**. The walker is on the screen ≈ 80 % of the time (3 s off per ≈ 15 s pass), and
about half of that the ship faces its plate, where only the pods (phase 1) are open to the front guns,
the rest its back (×1, the vents ×2 for 2 s per burn): an overall efficiency of ≈ 0.68, ≈ 37.5 DPS.
2,200 HP ÷ 37.5 ≈ 59 s, plus the intro pass's first 4 s (invulnerable until the hull is fully on the
screen) ≈ **63 s**.

**Measured (step F4, accepted in round 42 (2026-10-20).** The autopilot with the plan's fit (the Ion Beam
L3, two Micro-missile Pods, the Tail Gun, Rook's Mortar) plays "get behind it, but not in the cone": the pods
first in phase 1 (from below on a down-pass, lined up on their outer edge, clear of the plate; the pods sit at the
art's mounts, ±53 px), then the hull's back from just outside the cone's length (inside its trigger, so a burn
that misses opens the vents), the flank from below on a horizontal pass, out of the cone at once while it tells,
burns or sweeps. At 2,200 HP the fight lasted 32 s (the back takes the whole fit, the vents ×2); the length steps
with the passes (a kill slips into the next pass), so the HP were set inside a plateau: **3,740** (hull 2,720,
pods 510; ×1.70) gives **52.7 s** on all five seeds (in-level 55–57 s), the par **53**; easy 38 s, hard 79 s, all
won, no burn reaches the autopilot. The paper's efficiency 0.68 becomes the measured **1.39** in `BalanceTest` and
`tools/balance.py` (4 s + 3,740 ÷ (55.2 × 1.39) = 52.7 s). The calibration harness (`RevenantWalkerTest`: a random dodge, a new way
every 0.4 s, eight seeds, shield and armour lost per 10 s of phase 1): **12.3**, the Kraken's phase 1 with the
same driver 11.4: about as much (the test allows 1.2×), not less; the attacks are not tuned (pending review).

**Re-measured after the rework (round 43, step WR, accepted in round 43 (2026-10-20).** Far more fire and the
bigger cone slow the autopilot (more dodging; it stands off behind the 240 px cone): at 3,740 HP the fight ran
80–110 s at medium, its phase 3 alone up to 54 s. The HP were cut to **3,170** (hull 2,310, pods 430; ×0.85), the
fight **56.1–60.2 s** on all five seeds (mean 57.1 s; in-level 56.8–68.2 s, mean 61.0), the par **57**; easy 33–39
s, hard 98–106 s (in-level 111–174 s, one retry), all won, at most one burn reaching the autopilot at medium, no retries at
medium. The fight's shots a minute (one per gun shot, a fan counting once, plus one per missile; the level report's
measure): **≈ 100** (phase 1 ≈ 125, phase 2 ≈ 120) against **41** before (60 / 43); phase 3 fires 5-way fans and
flank bursts, but the autopilot's phase 3 lasts ≈ 10 s and much of it hugs the walker, inside the 72 px within
which no gun fires (hard: ≈ 185, phase 3 ≈ 240). The random dodger now loses **21.0** per 10 s of phase 1 against
the Kraken's 11.4 (×1.84; the test asks for 1.3–2×). `WALKER_EFFICIENCY` 1.39 → **1.08** (57.1 s at 3,170 HP).

**Re-measured after round 44 (step WR2, accepted in round 44 (2026-10-20).** The armoured pods (hit only in
their windows), the rail lance (a dodge every 4 s, every 3 s in phase 3) and the stomp (no more hugging it) cut the
autopilot's damage hard: at hull 2,310 with pods of 100 the fight ran ≈ 96 s. The autopilot learnt the new rules:
in phase 1 it shoots a pod whose hatch is open (else it lines up on the nearer one), on a horizontal pass it lines
up on the back half past the lower pod (shut, it stops the shots from below) and takes the open lower pod; off the
line of a charging lance and out of a bracing stomp's ring at once; from phase 2 it waits for a bottom entry between
two lanes. The HP were then set inside a plateau (the length steps with the passes): **1,350** (hull 1,190, pods 80),
the fixture **46–59 s** on five seeds (mean 50.8 s), the par **51**; in the level 50–59 s at medium on the level
test's three seeds (one of five seeds at 80 s: a down pass at the ship and a horizontal pass with a pod alive), no
retries at medium; easy 32–33 s; hard ≈ 104 s in the fixture on average, all won (in the level 95–131 s, 2 retries
over 3 runs, round 43: 1; with hard's own faster lance and stomp and its salvos of 4 the ship arrived at the walker
with half its armour and died in the fight: 5 retries, and the pacing test's hard run did not finish, so hard keeps
medium's lance and stomp and its phase-1 salvos of 3). Hull 1,180 gives a 48.6 s mean, 1,200 puts one fixture seed at
79 s. Phase 2's salvos every 4 s per pod (3 s) and the lance's 4 s / 3 s and the stomp's 4 s cooldown are the tuned
levers (first tries: 3.5 / 2.5 / 3 s). The random dodger now loses **23.7** per 10 s of phase 1 (the stomp catches
it straying over the walker; the Kraken 11.4, ×2.1; the test now asks for 1.3–2.2×). `WALKER_EFFICIENCY` 1.08 →
**0.52** (50.8 s at 1,350 HP).

Lessons from Act 2 that the build must apply (M7 part F):
- **Measure, then tune the HP, not the attacks** (Harbour Kraken E6, Siege Spire H10): the autopilot
  with the balance plan's Level 19 fit over five seeds; the Spire's measured fight ran at ≈ 0.6 of its
  paper length (the plan's `anti-ground` from Rook and the ×2 parts), so expect an HP scale of up to
  ×1.5–1.8 here, the walker being a `ground` boss too.
- **A kinder phase 1** (Siege Spire, round 34 P2; round 43 reverses it at the user's request: phase 1 is now fierce but fair, ≈ 1.8× the Kraken's): measure with the calibration harness (armour lost per
  10 s, eight seeds); the random-dodge driver should lose about what it loses to the Kraken (≈ 10) or
  less. Phase 1's levers, in order: the lane beside the ship, the burst along the heading, one pod at a
  time.

### Bounty

A mid-boss pays ≈ **15 %** of its level's budget (band 10–20 %, `BalanceTest`). Data is in Act 1
terms: **bounty = share × budget(19) ÷ (2.56 × `bounty_scale`)**. With
[Level 19](../../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md)'s `bounty_scale` of
**0.28** (step F4, from the measured waves; step F2's paper 0.35, round 36's 0.53, then 0.52) that is
0.15 × 2,366 ÷ 0.717 ≈ **495** (hull 395, pods 50 each: the Kraken's arms-to-head ratio, rounded); paid
495 × 2.56 × 0.28 = **355 = 15.0 %**. (The earlier figures, 345 at an assumed 0.40, 262 at 0.53 and 395 at
0.35, are dropped.) If Level 19's scale changes, the bounty is recomputed with the same formula.

### Presentation

- **Look**: an unmarked human war machine in **grey with no trim** (the [Act 3 rule for unmarked
  Ascendancy machines](../../README.md)): a **biped** on two reverse-knee legs (the chosen concept
  a, "Strider heavy", round 36 B.1), a squat armoured hull, the twin missile pods (boxes) on the
  shoulders, the chin cannon, the reactor vents and the flamer nozzle on its back; the vents are
  drawn as a **hot-amber reactor grille** (the Ascendancy rule for exposed cores, Q.23i). Bare-metal scars where the plates and stencils were ground off. No insignia anywhere.
- **Readability (round 42, step GFX, accepted in round 42 (2026-10-20))**: under the Ion Beam the **heat builds
  over 0.5 s and cools over 1.0 s as a local hot spot**: the hull stays grey, the pods tint by their heat.
  Hit flashes follow the Kraken's rule (at most one every 0.25 s, a 0.3 blend towards white, none on a
  beam-hot part). The **flame cone** is drawn as a **tell outline over a faint fill**, then a filled **flame
  sector** (yellow-white at the nozzle to deep orange) over a dark underlay, with flame puffs, so it reads over
  the foundry's glow (round 43: drawn at the cone's new 240 px and ±32°, its puffs up to 1.6× larger and 2.6× as
  many, the fill finer). The **homing missiles** are drawn 1.5x with an exhaust glow and **0.9 s smoke trails**.
- **Intro**: it walks in from the top edge as the scroll eases to a halt; the short mid-boss bar
  `REVENANT WALKER`, the mini-boss sting and the boss checkpoint (none on hard), as at the Kraken. **No
  Choir static** in the HUD portrait frame: Choir transmissions precede Vrell bosses only, and this one
  is not Vrell (a quiet clue for the attentive player). Under the fight, Vorne's motif as a faint layer
  ([audio](../../../audio/music/README.md)).
- **Walk**: footfalls shake small dust and slag puffs from the yard; each footfall a dull stomp (no
  sound that pops).
- **EMP Burst** ([specials](../../../player/specials/README.md)): the walker is a machine, but the
  specials document's rule holds (approved in round 36): bosses and mid-bosses are **never
  stunned**, so the burst does not pause it (the 1 s stun once proposed here is dropped).

### Hint 2: the wreck beat

M15 = a (accepted in round 36): after the death the **scroll eases past the wreck**.

1. The walker dies where it is: it topples forward along its heading and the **wreck stays** on the
   ground layer, smoking, one flank turned up showing the **serial plate filed to bare metal**
   (a bright rectangle with file scratches, readable at 1×).
2. 1 s after the death chain ends, the scroll restarts at a **crawl of 20 px/s** (eased over 2 s) for
   10 s, then eases up to the level's speed. The walker never stands lower than y ≈ 470, so its wreck
   stays on the screen for at least 8 s of the crawl.
3. The radio exchange over the crawl (voiced, the 1 s rule, each line one subtitle page; the level
   document owns the timeline and may reword):
   - **Rook**: "Walker's down. No colours, no numbers. Who builds a thing like that and doesn't sign it?"
   - **Dr. Varga**: "Lancer, slow over the wreck. The serial plates are filed off. By hand. That alloy
     reads as Helix foundry stock."
   - **Okafor**: "Not on this channel, Doctor."  (M13 = a: in the level only Varga's observation and
     this cut-off; the rest in the hangar intel and briefings.)
4. Level 20's briefing image shows the **filed plate close up** (the briefing images' generator; the
   level documents and the art step own it).

## Concept art

Proposals: concept round 36 (step A-bosses, listed in the [bosses' Concept art
table](../README.md#concept-art); prompts in [../concept/prompts.md](../concept/prompts.md#revenant-walker-r36-a),
generator `tools/concept/bosses_r36.py`); round 36 (2026-10-13) chose **a** (B.1), b is rejected:
[a "Strider heavy"](../concept/revenant-walker-r36-a.png) ([GIF](../concept/revenant-walker-r36-a.gif)),
a reverse-knee biped with two shoulder missile boxes, a tail flamer between fuel tanks and a
hot-amber reactor grille on its back, its wreck's plate ground blank; and
[b "Siege crawler"](../concept/rejected/revenant-walker-r36-b.png) ([GIF](../concept/rejected/revenant-walker-r36-b.gif)),
a low four-legged hull with a dozer prow, two vertical-launch blocks and a twin-nozzle flamer
barbette, its wreck's plate cut off with a torch.

**The GIFs against this spec.** Both show one downward pass: the front guns kill the launchers while
it approaches, the rear guns its reactor once it is below the ship. That is the spec's split (see
*Why passes*: the plated front leaves only the pods to the front guns, the back and its vents face
the rear guns). The GIFs condense the fight, and these points differ (the spec stands for the production art):
- the reactor glows and takes hits for the whole pass; in the spec the vents open (×2, glowing) only
  for 2 s after each burn and in phase 3, and the hull, not a separate reactor part, is the bar;
- the flamer turret swings up to ±60° toward the ship from the first pass; in the spec it points
  against the heading in phase 1, swivels ±25° in phase 2 and sweeps 180° in phase 3's turns;
- the sprites are ≈ 230 px (a) and ≈ 260 px (b) across, the spec draws the walker ≈ 170×190 px;
- the legs: two (a, chosen), as the *Look* now reads.

Production art: [revenant-walker-final-r42-a.png](../concept/revenant-walker-final-r42-a.png)
([GIF](../concept/revenant-walker-final-r42-a.gif)), `tools/art/revenant_walker.py` (sprite names and
frame layout in its docstring and `briefs/m7f-names.md`): **approved as final** by the user in round 42 (F.a),
`art: final` (round 43's rework changed no sprite: the bigger cone is drawn by code). Prompt notes:
[../concept/prompts.md](../concept/prompts.md#revenant-walker-final-r42-a).

## Implementation

- [x] Stat block and boss script in a `data.yaml` (parts, the front plate, the vents as a weak spot with
      its window, the passes, phases, par), loaded; global difficulty multipliers applied (M7 part F,
      step F2: `movement.stride`, `boss.plate`, `boss.cone`, the pods' `homing`, an attack's `parts` and
      `aim: facing`, `until.share`, mapped by `FoundryRules`)
- [x] Walking passes: lanes, the lane choice per phase, the entry-edge cycle, 3 s off the screen with
      the dynamic edge warning, a bottom entry as a rear wave for Rook; the phase-3 turn on the spot (step F1: `StrideArena`, `RevenantWalkerSimTest`)
- [x] Enemy `homing` missiles: salvos, turn rate, life, shootable, at most 6 alive; the pods' hatch tell (step F1: `Missiles`, `RevenantWalkerSimTest`; the hatch tell's art in step ART-F3)
- [x] The rear flame cone: trigger by the ship behind it, tell, burn, once per burn, cooldown; the
      phase-2 swivel and the phase-3 turning burn; the vents opening after each burn (steps F1, F3: `RevenantWalkerSimTest`, `RookHazards`)
- [x] Rook and the autopilot keep out of the cone and shoot the pods first in phase 1 (step F4: the autopilot's
      walker play, `RevenantWalkerTest`)
- [x] The fight measured over five seeds (the balance plan's Level 19 fit), the HP tuned to ≈ 60 s at
      medium (every seed in 45–75 s), the par set to the measured mean; phase 1 checked with the
      calibration harness; the bounty at ≈ 15 % of budget(19) after Level 19's `bounty_scale`
- [x] Round 42 readability (step GFX, accepted in round 42 (2026-10-20)): the beam's local heat, the hit-flash rule,
      the flame cone's tell and sector, the larger missiles with glow and trails (see *Presentation*)
- [x] Round 43 rework (step WR, K.a of round 42; accepted in round 43 (2026-10-20): the chin's fans in
      bursts in every phase, the flank guns, bigger salvos, a fiercer phase 3, the cone 240 px ±32° with the
      tracking burn, the drawing scaled to it; the HP re-measured (3,170), the par 57 (`RevenantWalkerSimTest`,
      `FoundryKeysTest`, `RevenantWalkerTest`)
- [x] Round 44 (step WR2, the user in round 43, W.a/W.e; accepted in round 44 (2026-10-20): the pods armoured
      while their hatch is shut (`boss.hatch_armour`, until phase 3), the back-mounted rail lance once both pods are
      gone and in phase 3 (`boss.lance`: the charge's line, the shot, its flash), the stomp shockwave (`boss.stomp`:
      the brace's ring, the expanding ring); Rook and the autopilot keep clear of both; the HP re-measured (1,350),
      the par 51 (`StrideStrikes`, `RevenantWalkerSimTest`, `FoundryKeysTest`, `RevenantWalkerTest`, `StrikeLooks`);
      their own sounds still to come (placeholders: the Mantis's charge-up and sweep, the hatch and footfall pitched
      down)
- [x] Round 43's sound picks played (step WR2 for SNDI): the hatch, the missile, the flame's tell and loop, the vent hit
      on the open vents, its own death, the wreck's groan as the topple lands
- [x] Death: the topple, the chained explosions, the credit shower, the wreck left as scenery with its
      filed plate; the hint-2 crawl and its radio (the user accepted them in the round-42 capture, N.c)
- [x] Production art (≤ one atlas page): 8 headings × the walk cycle, pods, the flame cone and its
      tell, the vents' glow, the topple and the wreck, the intel silhouette (approved as final in round 42, F.a)

## Open questions

- None open: round 36 answered them (see *Decisions*); the reference DPS and `bounty_scale` are
  confirmed by the Act 3 balance plan in part A (M18, M12).

## Decisions

- 2026-09-30: Drafted in the bosses roster as the Level 19 mid-boss and hint 2.
- 2026-10-12 (overnight design phase): promoted from the roster to a draft spec (all accepted in round 36 (2026-10-13): passes from every edge across a halted casting yard with edge warnings;
  three phases (Stride, Hunt, Overheat); a front plate, two pods and the reactor vents as the parts;
  enemy `homing` missiles and a rear flame cone as new attacks; a kinder phase 1 (the lane beside the
  ship, the burst along the heading, one pod at a time); HP 2,200 from the proposed reference DPS 92
  (paper ≈ 63 s, par 60 s until measured); bounty 345 in Act 1 terms (≈ 15 % at an assumed
  `bounty_scale` of 0.40); M15 = a, the wreck beat with the scroll's 20 px/s crawl and the
  Rook–Varga–Okafor exchange; no Choir static at its intro.
- 2026-10-12 (merge): the bounty follows Level 19's `bounty_scale` of 0.53: 262 in Act 1 terms (hull
  210, pods 26 each), paid 355 = 15.0 %; replaces 345 (14.9 % at an assumed 0.40). The round-36 a/b
  proposals are listed under Concept art (`art: proposed`), with the points where their GIFs differ
  from this spec; the *Look*'s legs now follow the chosen variant.
- 2026-10-13 (user, round 36): approved (D.18), M15 = a accepted. Q.23 yes: it leaves the screen
  between passes (3 s under an edge warning), the flame deals `heavy` 10 once per burn, the weak
  point is drawn as a hot-amber reactor on its back. **B.1 = a**: the "Strider heavy" biped chosen;
  the *Size* and *Look* rows now describe two reverse-knee legs (Q.25f); the "Siege crawler" (b,
  four legs) rejected and moved to `../concept/rejected/`. The reference DPS 92 and the bounty 262
  (Level 19's scale) stand until the Act 3 balance plan (part A). The 1 s EMP stun proposed here is
  dropped for the specials document's rule (bosses never stunned), an alignment made at the close,
  accepted in round 37 (2026-10-13).
- 2026-10-19 (M7 part F, step F2): the stat block and script moved into [data.yaml](data.yaml), the table
  marked. The front plate is the boss's `plate` (a box ahead of the hull, not an `armoured` part: it has no
  hit box of its own to be shot); the walker's 8 body headings are a new orientation `8 angles`; the
  horizontal lanes y 200 / 330 are px below the top edge (the data's convention); phase 1 ends on both
  pods gone or the hull below 60 % (`until.share`); the chin's phase-1 burst fires along its heading
  (`aim: facing`); hard's salvos of 3 from phase 1, missiles 120°/s, the cone 170 px with a 35° swivel,
  easy's 0.8 s tell and phase-1 salvos of 1 are its hooks. The bounty recomputed for Level 19's
  `bounty_scale` of 0.35: 395 (hull 315, pods 40 each), paid 354 = 15.0 %; HP 2,200 and par 60 until
  step F4 measures the fight (accepted in round 42 (2026-10-20).
- 2026-10-19 (M7 part F, step F4): the fight measured with the autopilot's walker play and the plan's Level 19 fit
  (see *Duration and HP*): HP 2,200 → **3,740** (hull 2,720, pods 510), par 60 → **53** (the measured mean,
  52.7 s); the pods moved to the art's mounts (±53 px, was ±73: G1's request, so the missiles leave the drawn
  pods), which put them at the plate's ends (the autopilot lines up on their outer edge); `intel:
  {unclassified_until_flown: true}` (unnamed before the fight; the check now counts a level's boss as flying it);
  the bounty 395 → **495** (hull 395, pods 50) for Level 19's measured `bounty_scale` of 0.28, paid 355 = 15.0 %.
  The random dodger loses 12.3 per 10 s of phase 1 against the Kraken's 11.4 (about as much, not less). Accepted in round 42 (2026-10-20).
- 2026-10-20 (round 42, step GFX): readability fixes after the round 42 captures: the Ion Beam's heat as a
  local hot spot (builds 0.5 s, cools 1.0 s; the hull stays grey, the pods tint), the Kraken's hit-flash rule
  (at most one per 0.25 s, a 0.3 blend, none on a beam-hot part), the flame cone's tell outline and filled
  flame sector over a dark underlay with puffs, homing missiles 1.5x with an exhaust glow and 0.9 s smoke
  trails. Accepted in round 42 (2026-10-20).
- 2026-10-20 (user, round 42, closed): the production art approved as final (F.a; `art: final`); the walker in
  the simulation and Level 19's data as built (B.a–B.d), the cone and pods (R.d) and the rest of the round's rows
  accepted; the death and the crawl seen and accepted (N.c). K.a **change**: "The walker looks like a real easy
  target. It needs to do a lot more shooting and the flame needs to be bigger." (round 43, below). Its sounds
  (S rows) are round 43's.
- 2026-10-20 (round 43, step WR, the K.a change; accepted in round 43 (2026-10-20): **much more shooting**:
  the chin fires fans in bursts in every phase (along its heading in phase 1, aimed from phase 2, 5-way in phase
  3), new **flank guns** (a fan of 2 over 180° along its heading, bursts out of both flanks), salvos of 3 in phase
  1 and every 3 s per pod from phase 2, at most 8 missiles; a **bigger flame**: 240 px ±32° (was 150, ±22°), the
  swivel ±35° (25°), and from phase 2 the burn follows the ship at 40°/s within it (`track`, new); easy gentler
  (single chin fans, short flank bursts, salvos of 2, a 210 px cone, the 0.8 s tell), hard fiercer (salvos of 4,
  5-way chin fans, longer flank bursts, 270 px, ±45°). The fight re-measured: HP 3,740 → **3,170** (hull 2,310,
  pods 430), par 53 → **57**; the random dodger's phase 1 21.0 per 10 s (Kraken 11.4); the shots a minute ≈ 100
  against 41. Rejected: a mortar on its back (the shared lob draws Level 14's lime acid, wrong for a foundry
  machine); a forward flame wash in phase 3 (the 180° turning burn already sweeps a half disc of 240 px).
- 2026-10-20 (user, round 43, W.a and W.e; built in step WR2, accepted in round 44 (2026-10-20): "The first
  missile hatch is destroyed before the walker has a chance of ever using it. That's too easy. When both missile
  hatches are destroyed, the walker is mostly toothless. Can't you add something else that keeps the final part
  interesting?" and "stomp shockwave … And as noted above, something else for the longer range." Decided with the
  user: **the pods armoured until open** (only the hatch's tell and salvo take damage; shots glance, the beam stops;
  the step adds: from phase 3 the hatches jam open, so the overheat never stalls on a pod), pod HP 430 → **80**
  (≈ 2–3 open windows); **a back-mounted rail lance** once both pods are gone and in phase 3 (a 1 s charge line
  through where the ship was, a 0.3 s `heavy` shot along it, every 4 s, 3 s in phase 3; first tried at 3.5 / 2.5 s);
  **the stomp shockwave** within 90 px (a 0.5 s brace with a ground ring, a ring out to 110 px, `medium`, at most
  every 4 s). Re-tuned: hull 2,310 → **1,190**, par 57 → **51**, phase 2's salvos every 4 s per pod (3 s); hard keeps
  medium's lance and stomp and drops its phase-1 salvos of 4 (the walker otherwise drained the ship before the level's
  end), easy's lance charge 1.3 s every 5 / 3.8 s and stomp brace 0.7 s.
  The sounds of the lance and the stomp are placeholders (nothing synthesized) until the next sound round.
- 2026-10-20 (user, round 44, closed): the second rework accepted as built: **W.a** the capture (the pods armoured until their hatch opens, the stomp's ring, the rail lance on the back) accepted, **W.b** HP 1,350 (pods 80 each, hull 1,190) and par 51 s, **W.c** hard keeps medium's lance, stomp and phase 1, **W.d** the hatches jam open in phase 3, **W.e** phase 2's salvos every 4 s per pod, the lance every 4 s / 3 s and the stomp at most every 4 s, **W.f** phase 1's loss at most 2.2x the Kraken's, **W.g** the narrow HP plateau accepted as a known risk (a later change to the attacks or speed needs a re-measure and an HP re-tune) — all as recommended. **W.h** the stomp's ring is too faint against the foundry floor: a **brighter, thicker ring** (a look change only, no number moves), planned, not started. **Sounds**: S.g the lance charge **b**, S.h the lance shot **a**, S.i the shockwave **a** (see the [sfx decisions](../../../audio/sfx/README.md#decisions)).
