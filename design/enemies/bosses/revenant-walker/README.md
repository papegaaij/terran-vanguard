---
title: Revenant Walker
design: approved
implementation: not-started
art: chosen
depends-on: [../../../systems/difficulty, ../../../systems/economy, ../../../campaign/act-3-red-dust, ../../../story/twist]
updated: 2026-10-13
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

Draft numbers at **medium**; HP in damage units (a Pulse Cannon L1 shot does 2), bullet classes and
contact damage from the [balancing basis](../../README.md#balancing-basis). The reference DPS used is
the **proposed** Level 19 value of **92** (M18 = a: ≈ 76 at Level 15 rising by about 4 a level to 100
at Level 21; accepted in round 36, confirmed by the Act 3 balance plan in part A).

| Field | Value |
|---|---|
| Faction | Unmarked (Ascendancy; nothing in the game identifies it yet) |
| Layer | `ground` (it walks on the yard: no contact with the ship or Rook; all weapons hit it, `anti-ground` ×2) |
| Size tier | `huge` |
| Size | drawn ≈ 170×190 px (the hull ≈ 110×120 on two reverse-knee legs, a biped: B.1 = a); hitboxes: hull 110×120, front plate 110×30 on the heading side, each pod 36×36 on a shoulder, the vents 50×24 on the back |
| Parts | hull (`vital`; the bar); front plate (`armoured`: shots glance, no HP, not in the bar, covers the hull from the heading side); left and right missile pod (`destroyable`, on the shoulders, outside the plate's cover); the reactor vents (a weak spot on the hull's back, ×2 while open); the rear flamer (a hardpoint under the vents, not destroyable); the legs (drawn, no hitbox) |
| Orientation | 8 headings (45° steps: the passes walk the 4 cardinal ones, the turns use the diagonals) × an 8-frame walk cycle; pods snap to the ship's bearing (16 headings) |
| HP | hull 1,600; pods 300 each; total **2,200** (easy 1,650 / hard 2,860, from the global multipliers) |
| Armour / shield | the front plate turns every shot that meets it from ahead; the back and the pods take full damage; the vents ×2 while open (2 s after each burn, always in phase 3) |
| Speed | 45 px/s (phase 1), 55 px/s (phase 2), 60 px/s (phase 3); turns 90° in 1.5 s |
| Movement | `walk` in **passes** across the halted casting yard (see *Passes*); off the screen 3 s between passes, under an edge warning |
| Attack | homing salvos from the pods, a chin `burst`, the rear **flame cone** (see *Phases* and *Attacks*) |
| Formations | solo set piece |
| Weak points | the reactor vents (exposed machinery: they glow heat orange when open) |
| Effective traits | `rear`, `anti-ground`, `homing`, `side` |
| Credits | **262** in Act 1 terms (hull 210, pods 26 each); × 2.56 (Act 3 factor 1.6²) × Level 19's `bounty_scale` of 0.53 it pays **355 = 15.0 % of budget(19) = 2,366** (see *Bounty*) |
| Death | `huge` machine variant: the legs buckle, it topples forward along its heading over 2 s with chained explosions, the hull lands as a smoking **wreck** that stays (scenery) for the hint-2 beat; credit shower; no screen flash (act bosses only) |
| First level / used in | L19; mid-boss of L19 only |
| Difficulty hooks | hard: salvos of 3 per pod from phase 1, missiles turn 120°/s, the flame cone 170 px, the phase-2 swivel ±35°; easy: the flame's tell 0.8 s, salvos of 1 in phase 1 |

### Attacks

- **Homing missiles** (`homing`, new for enemies: no enemy fires them yet). Each pod fires a salvo,
  one missile 0.2 s after the other: 110 px/s, turn rate 90°/s, 4 s life then they fizzle, `medium` = 6
  on a hit, **shootable** (2 HP; homing shots, bolts and Rook all hit them), a grey smoke trail. At most
  6 alive. A pod's hatch flips open 0.4 s before its salvo (the tell).
- **Chin cannon** (`burst`): 3 shots 0.15 s apart, 150 px/s, `small` = 4, every 2.5 s. Phase 1 fires
  **along its heading** (not aimed; the lesson of the Siege Spire's round-34 P2: a kinder first phase
  for a player who does not dodge well); from phase 2 on it is `aimed`.
- **Rear flame cone** (new attack shape, built on the `laser-sweep`'s beam test with a cone instead of
  a line): a cone **150 px** long, ±22°, from the flamer on its back, pointing **against its heading**.
  It fires when the ship (or Rook) is inside the cone's reach + 40 px behind it, at most once every 3 s:
  a **0.6 s tell** (the pilot light flares blue-white, heat shimmer along the cone's outline, a hiss),
  then a **1.5 s burn**; a ship touched by the burn takes `heavy` = 10, **once per burn** (the laser's
  once-per-sweep rule). After each burn the reactor **vents open for 2 s** (the weak spot, ×2): the
  reward for staying behind it, just outside the cone. The flame is drawn above the ground layer, bright
  and opaque enough to read over the foundry's orange glow (art step).

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
| 1 — Stride | both pods destroyed, or the hull below 60 % | Passes at 45 px/s in the lane beside the ship. The pods fire **salvos of 2, one pod at a time**, alternating, every 3 s (so a salvo every 3 s in all, not two); the chin `burst` along its heading; the flamer behind it. |
| 2 — Hunt | the hull below 30 % | Passes at 55 px/s in the ship's lane. Each living pod fires salvos of 3 every 4 s, staggered by 2 s; the chin `burst` is `aimed`; the flamer **swivels ±25°** toward the ship during its tell. A destroyed pod stays dead (its shoulder sparks). |
| 3 — Overheat | the hull at 0 | The reactor overheats: the vents **stay open** (×2 for the rest of the fight) and glow white-orange. It stops leaving the yard: at the end of a pass it **turns round on the spot** (1.5 s) and walks back in the ship's lane at 60 px/s; **the flamer burns through the whole turn**, sweeping its cone through 180° behind it (the tell 0.6 s before the turn starts). Living pods keep their phase-2 salvos. A **shield cell** drops as the phase begins. |

### Duration and HP

Target: the mid-boss window **45–75 s** at medium, aimed at ≈ 60 s; the **par** is the measured mean
(draft **60 s**).

Paper estimate (the window-aware method of the Kraken's `BalanceTest` check): the effective boss DPS
is 0.6 × 92 = **55.2**. The walker is on the screen ≈ 80 % of the time (3 s off per ≈ 15 s pass), and
about half of that the ship faces its plate, where only the pods (phase 1) are open to the front guns,
the rest its back (×1, the vents ×2 for 2 s per burn): an overall efficiency of ≈ 0.68, ≈ 37.5 DPS.
2,200 HP ÷ 37.5 ≈ 59 s, plus the intro pass's first 4 s (invulnerable until the hull is fully on the
screen) ≈ **63 s**.

Lessons from Act 2 that the build must apply (M7 part F):
- **Measure, then tune the HP, not the attacks** (Harbour Kraken E6, Siege Spire H10): the autopilot
  with the balance plan's Level 19 fit over five seeds; the Spire's measured fight ran at ≈ 0.6 of its
  paper length (the plan's `anti-ground` from Rook and the ×2 parts), so expect an HP scale of up to
  ×1.5–1.8 here, the walker being a `ground` boss too.
- **A kinder phase 1** (Siege Spire, round 34 P2): measure with the calibration harness (armour lost per
  10 s, eight seeds); the random-dodge driver should lose about what it loses to the Kraken (≈ 10) or
  less. Phase 1's levers, in order: the lane beside the ship, the burst along the heading, one pod at a
  time.

### Bounty

A mid-boss pays ≈ **15 %** of its level's budget (band 10–20 %, `BalanceTest`). Data is in Act 1
terms: **bounty = share × budget(19) ÷ (2.56 × `bounty_scale`)**. With
[Level 19](../../../campaign/act-3-red-dust/level-19-foundry-of-tharsis/README.md)'s `bounty_scale` of
**0.53** that is 0.15 × 2,366 ÷ 1.357 ≈ **262** (hull 210, pods 26 each: the Kraken's arms-to-head
ratio, rounded); paid 262 × 2.56 × 0.53 = **355 = 15.0 %**. (The draft's first figure, 345 at an
assumed scale of 0.40, is dropped.) If Level 19's scale changes, the bounty is recomputed with the
same formula.

### Presentation

- **Look**: an unmarked human war machine in **grey with no trim** (the [Act 3 rule for unmarked
  Ascendancy machines](../../README.md)): a **biped** on two reverse-knee legs (the chosen concept
  a, "Strider heavy", round 36 B.1), a squat armoured hull, the twin missile pods (boxes) on the
  shoulders, the chin cannon, the reactor vents and the flamer nozzle on its back; the vents are
  drawn as a **hot-amber reactor grille** (the Ascendancy rule for exposed cores, Q.23i). Bare-metal scars where the plates and stencils were ground off. No insignia anywhere.
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
frame layout in its docstring and `briefs/m7f-names.md`); `art: chosen` stays until the user reviews it
(round 42). Prompt notes: [../concept/prompts.md](../concept/prompts.md#revenant-walker-final-r42-a).

## Implementation

- [ ] Stat block and boss script in a `data.yaml` (parts, the front plate as an `armoured` part, the
      vents as a weak spot with its window, the passes, phases, par), loaded; global difficulty
      multipliers applied (M7 part F)
- [ ] Walking passes: lanes, the lane choice per phase, the entry-edge cycle, 3 s off the screen with
      the dynamic edge warning, a bottom entry as a rear wave for Rook; the phase-3 turn on the spot
- [ ] Enemy `homing` missiles: salvos, turn rate, life, shootable, at most 6 alive; the pods' hatch tell
- [ ] The rear flame cone: trigger by the ship behind it, tell, burn, once per burn, cooldown; the
      phase-2 swivel and the phase-3 turning burn; the vents opening after each burn
- [ ] Rook and the autopilot keep out of the cone and shoot the pods first in phase 1
- [ ] The fight measured over five seeds (the balance plan's Level 19 fit), the HP tuned to ≈ 60 s at
      medium (every seed in 45–75 s), the par set to the measured mean; phase 1 checked with the
      calibration harness; the bounty at ≈ 15 % of budget(19) after Level 19's `bounty_scale`
- [ ] Death: the topple, the chained explosions, the credit shower, the wreck left as scenery with its
      filed plate; the hint-2 crawl and its radio
- [ ] Production art (≤ one atlas page): 8 headings × the walk cycle, pods, the flame cone and its
      tell, the vents' glow, the topple and the wreck, the intel silhouette

## Open questions

- None open: round 36 answered them (see *Decisions*); the reference DPS and `bounty_scale` are
  confirmed by the Act 3 balance plan in part A (M18, M12).

## Decisions

- 2026-09-30: Drafted in the bosses roster as the Level 19 mid-boss and hint 2.
- 2026-10-12 (overnight design phase): promoted from the roster to a draft spec (all pending the
  user's review in round 36): passes from every edge across a halted casting yard with edge warnings;
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
  pending the user's review.
