---
title: Honour Guard
design: approved
implementation: done
art: chosen
depends-on: [../../air/talon, ../../../world/jovian, ../../../campaign/act-6-jovian-storm, ../../../player/wingmen, ../../../ui/hud, ../../../story/characters, ../../../systems/economy]
updated: 2026-11-03
---

# Honour Guard

## Summary

The Act 6 mid-boss (Level 40, the Callisto approach): Vorne's three aces in custom gold Talons. The leader opens
alone from the front with a sniper's `laser-line`, the dancer joins from a side with `spiral` loops, the feinter
joins from the rear with dives that pull out, and then the three fly one shared playbook. Each ace draws its next
attack from a small pool, and each death changes the survivors' play. One mid-boss bar in three segments with the
aces' call signs, taunts on the enemy channel and Rook's rivalry with them make it a duel.

**Status: draft** (Act 6 design phase, step D-boss, 2026-10-31), **decided 2026-10-31 (round 72)**.
It follows the gap analysis's recommended answers: **Q5 a** (staggered, then together; the alternatives under
*Variants b and c*), **Q9 a** (the elite livery) and judgement calls **J1** (Level 40's reference DPS 160) and
**J4** (three call signs, one of them Harker). Every number is a hand-written **draft** at medium; there is no
`data.yaml` yet (M10 builds it with Level 40 once the design is approved).

## Design

### Why three aces

The roster's idea, kept: a duel with pilots instead of a machine. It is the opposite of the act boss: the
[Ascendant](../ascendant/README.md) is one huge body anchored at the top of a halted arena; the Honour Guard are
three small, fast bodies that use the whole field, from the front, a side and the rear (Level 40's directions), so
the test is **target choice and reading three tells at once**. Level 40's traits are `spread` (three bodies apart)
and `rear` (the feinter lives behind the ship). The playbook follows round 70's lesson (the
[Iron Sovereign](../iron-sovereign/README.md#the-round-70-rework-decided-round-70)'s core): no fixed loop, every attack
drawn from a pool with its own tell.

### Stat block

Numbers at **medium**; HP in damage units (a Pulse Cannon L1 shot does 2); bullet classes (`small` = 4,
`medium` = 6, `heavy` = 10, `laser` = 8) and contact damage from the [balancing basis](../../README.md#balancing-basis).
The reference DPS is the **proposed** Level 40 value of **160** (J1, the
[Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft); decided 2026-10-31 (round 72)). The aces fly the
[Talon](../../air/talon/README.md)'s airframe; this table sets only what differs.

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Jovian Ascendancy (a machine, but a boss: never stunned, its shields emptied by the EMP Burst) |
| Layer | `air` (`space` is `air` in vacuum, the [Jovian media](../../../world/jovian/README.md#media-per-level)) |
| Size tier | `medium` |
| Size | 60×54 px, hitbox 42×36 (each a custom Talon at 1.5× the airframe; the leader's sniper barrel adds 14 px under the nose, no hit box) |
| Parts | three **bodies**, none `vital` (the last death ends the piece): **Harker** (the leader), **Vesper** (the dancer), **Kestrel** (the feinter); one bar in three segments in that order |
| Orientation | `64 angles` (each; they bank and turn through loops and dives; the Talon flies 16) |
| HP | 1900 each (the three tuned together); total 5700 (easy 4275 / hard 7410, from the global multipliers) |
| Armour / shield | a gold hex **shield of 60** on each ace under the [enemy shield rule](../../README.md#enemy-shields) (spent first, refills after 2 s; the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md)'s `shield-breaker` empties it, the EMP Burst strips all three); no mirror (a unit carries one defence) |
| Speed | 320 px/s (Kestrel diving); Harker 140 px/s on his station, 280 px/s sliding; Vesper 200 px/s through her loops; Kestrel 260 px/s raking |
| Movement | per ace (see *The aces*): Harker the top third, Vesper the middle band, Kestrel the bottom band and the sides; hull edges **≥ 80 px apart** (the Act 6 formation rule: no two medium hulls overlap; step R79b2: a goal the aces steer for, briefly nearer in transit, never nearer than 40 px); contact `medium` 15 to the ship once a second, the ship pushed 24 px out, the ace taking 40 and flying on |
| Attack | per ace, drawn from its pool (see *The playbook*): Harker's sniper line (`laser-line`): the barrel's 0.8 s red glint, then the thin red line tracking the ship for 1 s, locked 0.2 s before it fires, live 0.3 s, 6 px wide, `laser` 8 once; its double tap: a second line re-aimed 0.6 s later (0.8 s telegraph; a triple after a death); the Talon's aimed pair four times 0.4 s apart (`small`, 170 px/s); two aimed 5-way fans over 40° 0.5 s apart (`small`, 150 px/s); Vesper's loop (2.5 s) firing a 2-arm `spiral` from her tail (`small`, 130 px/s, the arms turning 90°/s, 4 bullets/s an arm; 3 arms after a death); her barrel roll past the ship's side (≥ 120 px off) with a 5-way aimed fan at its middle; her ribbon: a fast pass at y 235–285 (inside her band) laying 8 `small` bullets falling 80 px/s with two 50 px gaps; Kestrel's feint dive at 320 px/s pulling out at 110 px (85 committed), a burst of 3 aimed `medium` (200 px/s) back at the ship from the pull-out (5 committed); its overhead feint out of the top edge the same; its rear rake: a climb up a side at 260 px/s, aimed pairs every 0.4 s (`small`); the pincer: Vesper and Kestrel cross from both sides at the ship's height − 160 px, their 5-way fans 0.5 s apart, Harker holding fire |
| Formations | solo set piece |
| Weak points | none (beyond the bodies; each ace's engines glow, no weak-point multiplier) |
| Effective traits | `spread`, `rear`, `homing`, `shield-breaker` |
| Credits | 360 (score 3600 × chain) in Act 1 terms, **120 an ace**, each paid as it dies: ≈ **15 % of budget(40)** at Level 40's `bounty_scale` 0.39 (see *Bounty*) |
| Death | per ace a `medium` vacuum flash and its hull tumbling away; the last ace a 2 s `large` chain, the credit shower (no screen flash: a mid-boss) |
| First level / used in | L40; only (the Level 49 boss rush has no echo of it) |
| Difficulty hooks | easy: Vesper joins at Harker's half or after 25 s, Kestrel 3 s after her (medium 4 s); Harker's line single (1.2 s), no double tap; every rest ×1.3; Vesper's spiral 1 arm; Kestrel's dives told 1.0 s, pulling out at 130 px. Hard: Vesper joins at 3/4 or after 15 s, Kestrel 3 s after her; the double tap a triple, the line 0.8 s; rests ×0.8; Vesper's spiral 3 arms; Kestrel pulls out at 90 px; the aces' HP and shields ×0.95 (not the difficulty's ×1.3: the fiercer duel at about medium's length); no boss checkpoint |
<!-- /data -->

### Arena

Level 40 flies the Callisto approach in vacuum ([the Jovian system](../../../world/jovian/README.md#sub-locations-per-level):
black space, Callisto's dark disc below, Jupiter's limb at an edge, the fleet's lights). The level document places
the duel and owns its timeline; the fight needs:

- **The halt of the waves**: the level's waves end before the sting; the backdrop keeps its slow drift (space has
  no ground to halt), and the level clock halts while an ace lives (the boss rule). No popcorn during the duel:
  three aces are enough to read.
- **The warning before any ace shows**: the mini-boss sting ([track 21](../../../audio/music/README.md)) and the bar
  `HONOUR GUARD` filling with its three segments hatched; then Harker's entry tell (below). The boss checkpoint is
  taken at the sting (its built rules; none on hard).
- **The whole field is the arena**: Harker holds the top third, Vesper the middle band, Kestrel the bottom band and
  the sides, in **three separate bands** of 140 px (y 27–167, 205–345 and 380–520 px below the top edge; round 78
  B.2 b, step F6c; sized for wide turns in step R79b2, round 79 H.1). A band holds where its ace holds or plays; in
  transit (a join, a dive's recovery, a lane change) an ace may leave it on a wide arc. The 80 px hull rule and the
  keep-offs steer, never shove: the ace that yields bends its arc or changes its pace, so two hulls come nearer only
  briefly and never nearer than 40 px (no two hulls overlap), and no ace comes inside the ship's ring. The ship's band
  (the bottom 140 px) is Kestrel's: the rear mounts and a turn of the field pay there.

**Entry tells.** Each ace's **first** entry is announced by **its own tell**, 2.5 s ahead: a gold chevron with its
call sign at the edge it will enter by, its engines' glint beyond the edge and its taunt on the enemy channel. It is
not the generic edge warning: bosses never trigger edge warnings (the [bullet rules](../../README.md#bullet-readability-rules),
user, 2026-10-21); the gap analysis's "edge warning before any ace shows" is met by the ace's own tell. Its
segment of the bar lights as it enters ([HUD](../../../ui/hud/README.md#act-6-additions-draft)). Loops and re-entries in
an ace's own pattern never warn.

### The aces

| Ace | Joins | Station and movement | Look (the elite livery) |
|---|---|---|---|
| **Harker** (the leader; the sniper) | first, from the top edge, after the sting | holds the **top third** (y 27–167) on a racetrack orbit, strafing 40–80 px at 140 px/s round ends of 65 px; his nose follows his flight and turns onto the ship only while he aims (a told line, pair or fan: he slows to a creep along it, never out of his band); a **slide** crosses to the other side at 280 px/s | a long sniper barrel under the nose, a red nose band, the Ascendancy crest on his fins; Harker of Level 29's mutineers, now in Vorne's guard (J4) |
| **Vesper** (the dancer; `spiral`) | from a **side** edge (the one away from Harker) when Harker is down to **2/3** of his HP or after **20 s**, whichever first | loops through the **middle band** (y 205–345) in lazy eights (two 65 px loops joined by straights crossing at the centre, on the side away from Harker's station), keeping 120 px off the ship | white ribbon stripes along the wing edges and the tail |
| **Kestrel** (the feinter; dives that pull out) | from the **bottom edge** (the rear) **15 s after Vesper** (step F2, measured: **4 s**; easy and hard 3 s), or at the **first ace's death**, whichever first | holds **beside the ship band** (y 380–520) on an orbit in a lane by the side away from the ship, 130 px in from the edge; it changes lanes in **one committed wide pass** over the ship (outside its keep-off) only when that height is free of the aces above, otherwise it stays in its lane, its orbit rising over a ship that came into its half, and plays from there; out of a pull-out it climbs back to its station | black and gold chevrons, a red tail band, twin swept fins |

### The playbook

Each ace, after each attack and its **rest**, draws its next attack from **its own pool** with the simulation's
seeded random (a replay draws the same), **never the same twice running**, each with a **0.8 s tell** (see *Tells*).
Rests: **1.2 s** while an ace flies alone, **2.0 s** each with two or three (the conductor spaces them).

| Ace | Pool | What it does |
|---|---|---|
| Harker | **Sniper line** | a `laser-line` aimed at the ship's spot: the thin red line for **1.0 s**, locked 0.2 s before it fires, then a 0.3 s `laser` 8 line 6 px wide; a **double tap**: a second line re-aimed 0.6 s later (0.8 s telegraph) |
| Harker | **Pair volley** | the Talon's aimed pair four times, 0.4 s apart (`small`, 170 px/s) |
| Harker | **Fan** | two aimed 5-way fans over 40°, 0.5 s apart (`small`, 150 px/s) |
| Harker | **Slide** | the side-step to the other side of the top third; no fire (drawn only with two or more aces flying: his breath) |
| Vesper | **Spiral loop** | one full loop (2.5 s) firing a 2-arm `spiral` from her tail (`small`, 130 px/s, the arms turning 90°/s, 4 bullets/s an arm) |
| Vesper | **Roll fan** | a barrel roll past the ship's side, ≥ 120 px off, a 5-way aimed fan (`small`) at the roll's middle |
| Vesper | **Ribbon** | a fast pass across the field at y 235–285 (inside her band) leaving a line of 8 slow bullets (`small`, 80 px/s down) with two gaps of 50 px |
| Kestrel | **Feint dive** | turns its nose at the ship, then dives at 320 px/s along a line at the ship's spot and **pulls out at 110 px** (never closer: a feint, never a ram), banking off sideways and firing a `burst` of 3 aimed `medium` shots back at the ship from the pull-out |
| Kestrel | **Rear rake** | climbs from the bottom band up one side at 260 px/s, firing aimed pairs (`small`) across the field every 0.4 s, then loops back down |
| Kestrel | **Overhead feint** | loops out of the top edge (no tell at the edge: its own pattern) and dives from the front as the feint dive |
| all three | **Pincer** (team move) | Vesper and Kestrel cross from both sides at once at the ship's height − 160 px, their fans 0.5 s apart, while Harker holds fire; drawn at most every **fourth** draw, only with all three flying |

**The conductor** (with two or three aces): **at most one telegraphed line or dive at a time** (Harker's line and
Kestrel's dive never overlap; the later waits), **at most two aces firing at once** (the third slides, loops or
climbs back), and no ace starts an attack while another's tell runs. So the field is busy, but every threat is told
on its own.

**Each death changes the survivors** (Q5 a): the survivors' rests **−30 %** and each gets its edge: Harker's double
tap becomes a **triple**; Vesper's spiral gets **3 arms** (she turns aggressive); Kestrel **commits**: it pulls out at
**85 px** (still outside the 72 px no-spawn ring) and its burst is 5. The last ace alone: rests −50 % and it draws from
its pool and the dead aces' one signature move each (a Harker line, a Vesper loop, a Kestrel dive) flown its own way.
A line on the enemy channel names the loss (see *Lines*).

### Tells

| Attack | Tell | Medium (easy / hard) |
|---|---|---|
| An ace's first entry | the gold chevron with its call sign at its edge, the engines' glint, its taunt | 2.5 s |
| Sniper line | the barrel's red glint, then the red line brightening | 0.8 s glint + 1.0 s line (easy 1.2 s / hard 0.8 s line) |
| Pair volley, fan | the twin muzzles' red glint (the Talon's) | 0.8 s |
| Spiral loop | Vesper's ribbons flare white as she pulls into the loop | 0.8 s |
| Roll fan, ribbon | her wingtips trail white as she banks | 0.8 s |
| Feint dive, overhead feint | Kestrel's engines flare white and its nose swings to the ship | 0.8 s (easy 1.0 s) |
| Rear rake | its tail band glows red as it climbs | 0.8 s |
| Pincer | both flankers' engines flare at once; Harker's crest dims (he holds fire) | 0.8 s |

### The escort (Rook)

Rook flies the act in the escort slot ([Rook in Act 6](../../../player/wingmen/README.md#rook-in-act-6-draft)). He follows
his boss rules: he shoots the **nearest ace** (the rivalry is in the lines, not in his pick), steps out of a told
sniper line like the ship, and keeps 40 px from a dive's line. His shots spend an ace's shield like any hit. His state
is in the boss checkpoint as usual. The test autopilot does the same, plus "turn for Kestrel": a told dive makes it
side-step off the line, and it fires the rear mount at Kestrel on its station.

### Duration and HP

Target: the mid-boss window **45–75 s** at medium; the **par** is the measured mean, **66 s** (the paper's 75 s until
the build measured it; *Measured* below).

Paper estimate: the effective boss DPS is 0.6 × 160 = **96**; three moving, shielded bodies that dodge out of the
forward cone take ≈ 0.7 of it: ≈ **67**. 4,800 ÷ 67 ≈ 72 s of fire, plus ≈ 4 s of Harker's entry (invulnerable until
fully on the screen): **≈ 76 s**.

| Stretch | Aces flying | HP spent (paper) | Time |
|---|---|---|---|
| The sting, Harker's entry | — | — | ≈ 4 s |
| Harker alone, until Vesper joins at his 2/3 | 1 | ≈ 530 | ≈ 8 s |
| Harker and Vesper, until Kestrel joins | 2 | ≈ 1,000 | ≈ 15 s |
| All three, then two, then one | 3 → 1 | ≈ 3,270 | ≈ 49 s |
| Total | | 4,800 | **≈ 76 s** |

The measured bosses ran faster than their paper (the Revenant Walker ×1.39 the paper's efficiency at first, the Iron
Sovereign's HP doubled in its build), so the build expects to **raise** the HP. Lessons the build must apply (M10, the
Level 40 part):
- **Measure, then tune the HP, not the attacks**: the autopilot with the hand plan's Level 40 fit and Rook over five
  seeds, every seed inside 45–75 s, the par at the measured mean; tune the three HP together (equal shares).
- **Reading, not dodging**: the random dodger's loss per 10 s against the Kraken's phase 1 (the calibration band of the
  walker's test); the levers are the rests and the conductor's limits.
- **The three together must happen**: on every seed all three aces fly together for ≥ 10 s at medium; if the plan's
  beam kills Harker before Kestrel joins, the joins move earlier, not the HP up.

**Measured** (M10 part F; round 78 B.1 **change**, decided 2026-11-01: the numbers stay as built, the user gave no
verdict on them, so the duel goes to the next round as a real-time recording, `duel-fight-r78-b.mp4`; Level 40's data,
the plan's L40 fit with the Crucible, the spoofer's load and Rook, five seeds; re-measured in step R79b2 below). The
measured fire on an ace is ≈ 120 a
second, not the paper's 67: the draft's 1,600 an ace ran 47 s at medium with **0 s** of all three together (the focus
fell on Vesper, who died before Kestrel's 15 s ran out), so Kestrel joins **4 s** after Vesper crossed (easy and hard
3 s) and the three HP are tuned together, with `GuardPlay` (the autopilot reading the told sniper lines and dives, the
rear mount on Kestrel), to 2,120 an ace (step F6c; **1,900** since step R79b2, below). The aces fly like aircraft (B.1, steps
F6s–F6s4: smooth turns with banking, eased speed, the nose leading, the holds as orbits, avoidance instead of shoves)
in **separate bands** (B.2 b, steps F6c and F6s3: Harker y 60–160, Vesper 190–290, Kestrel 420–520 px below the top
edge; see *Arena*), and the medium duel runs **about 70 s** (the window 45–75 s; step F6s3's five seeds 62.8–72.8 s,
mean 66.6 s; step F6s4's real-time recording, one seed, 63.5 s from Harker's entry to the last kill); the par is **66**
(the measured mean, step F6c). Step F6c
measured the old HP in the same flight: easy 45.9 s, hard 89.2 s (not re-measured at 2,120). The deaths' order varies
by seed (Harker last on all five medium seeds, Vesper first on four of five in step F6c). **Reading, not dodging**: the
random dodger loses 26.4 of shield and armour per 10 s against the Kraken phase 1's 11.4 (**2.31×**; the walker's band
is 2.2×, the test allows up to 3.0×, the Abyssal Maw's; accepted, round 78 B.3). **The hull rule in the separate
bands** (B.2 b): since step F6s3 no ace holding its station leaves its band at medium; the pushes are at most 2.8 px
(Harker) and 7.6 px (Vesper and Kestrel), where they were up to 36 px (the first bands: Harker pushed out 3.8 times a
duel for 6.6 s, up to 376 px on one seed).

**Wider turns** (round 79 H.1, step R79b2: "they take way too many sharp turns ... smoother and wider"): no ace turns
tighter than 60 px nor faster than 2.6 radians/s (a tight turn is a slow one; its nose at most 2.6 radians/s, eased),
its speed eased, the holds wide orbits of 65 px in the 140 px bands; the separation and the keep-offs steer (the
yielding ace bends its arc or slows or hastens along it) instead of the exact solver's shoves, which hold only the
floors (hulls 40 px apart, the ship's ring). Measured on Level 40 at medium (four seeds, the flown places): sharp
turns (45° or more at a radius under 60 px) 3–13 a minute an ace where they were 40–77, turn reversals 6–18 a
minute (22–52), turns tighter than 45 px in 0.5–3.3 % of the turning samples (the floors' rare shoves; radii were
down to 2 px), the nose at most 2.6 radians/s (8 before), Kestrel slow over the ship 4–6 % of its time (20–27 s of a
60 s duel before). The price: two hulls come nearer than 80 px 8–14 % of the duel, about 9 times a minute, never
nearer than 40 px; an ace comes inside its keep-off 13–20 % of the duel, never inside the ship's ring. With the wider
turns the duel ran longer (72.1 s at 2,120), so the HP is **1,900 an ace** (5,700): medium **67.1 s** (59.2–72.9
over five seeds, the par 66 kept), easy 47.3 s (40.6–51.6), hard 64.1 s (52.8–71.5); the random dodger 22.5 per 10 s
against the Kraken phase 1's 11.4 (1.97×). Recorded in real time as `duel-fight-r79-b.mp4` (*Concept art*).

**Specials.** The specials' boss rules hold per ace (each is a boss part): a Smart Bomb up to 150 on each, an
Orbital Lance up to 400 on each it crosses; the EMP empties the three shields (never a stun). The
[Plasma Arc](../../../player/weapons/plasma-arc/README.md)'s chain treats the three aces as three targets (its own rule).
The IFF spoofer fools no ace (they are pilots, not turrets).

### Bounty

A mid-boss pays ≈ **15 %** of its level's budget (the band 10–20 %, `BalanceTest`; the Harbour Kraken's and the
Revenant Walker's 15 %). budget(40) = 700 × 1.07³⁹ ≈ **9,796**; 15 % ≈ **1,469 paid**. Data is in Act 1 terms:
**bounty = 1,469 ÷ (10.486 × `bounty_scale`) ≈ 140 ÷ `bounty_scale`** (10.486 = 1.6⁵, Act 6's factor). Level 40's
scale is the level document's; at its hand **0.39** ([Level 40](../../../campaign/act-6-jovian-storm/level-40-honour-guard/README.md#credit-budget)) that is
**360**, **120 an ace** (≈ 491 paid each, **1,472 = 15.0 %**), each paid as its ace dies (the first draft's 0.59, Level 35's
measured scale, gave 237). When the level's scale is set, the bounty is recomputed with the
same formula; the share stays at 15 %. (The gap analysis drafted 12 % ≈ 1,175 paid; 15 % follows the built
mid-bosses, both inside the band.)

### Death

An ace at 0: its shield shatters, a short vacuum flash at the engines and a ring of debris
([vacuum rules](../../../world/belt/README.md#vacuum-rules): no rising smoke), and its hull **tumbles away** down and out
of the field, burning only at the cockpit (air inside), no hit and no contact. Its segment goes dark and its tag is
struck through. The **last** ace: a 2 s `large` chain round its tumbling hull, then the credit shower where it died;
no screen flash (an act boss's only). The level's waves resume after ≥ 4 s of calm.

### Lines

The aces speak on the **enemy channel** (the HUD's amber plate, Act 5's intercepted dress); Rook answers on the
friendly one. **This document owns the aces' fight lines**; Level 40 places and times them, and the story step owns
the call signs, their voices and Rook's wording (J4). Drafts, decided 2026-10-31 (round 72):

| When | Speaker | Line (draft) |
|---|---|---|
| Harker's entry tell | Harker | "Rook. They let you fly again? Then let's settle Ceres properly." |
| Vesper's entry tell | Vesper | "Leave some for me, Harker. The quiet one's mine." |
| Kestrel's entry tell | Kestrel | "Check your six, Lancer. I'm already there." |
| The first ace's death | a survivor | Harker: "That was one of mine." / Vesper: "You'll pay for that one." / Kestrel: "No more games." (whichever survives speaks; one line) |
| The second ace's death | the last ace | "Just us now. Come on, then." |
| The last ace's death | — | silence on the enemy channel; Rook's last word is the level's |

Rook's rivalry (the story's and the level's): one or two of his lines between the aces' (at Harker's entry, at
Harker's death), never over a tell.

### Variants b and c (for the round's a/b/c)

- **b — Three duels in sequence**: Harker, then Vesper, then Kestrel, each alone with its own bar. Cheaper (one body at
  a time, the bar as built) and easier to read, but predictable: three short mid-bosses, not a guard.
- **c — All three from the start**: the conductor and the shared playbook at once. The same cost as a; harder to read
  in its first seconds and harder to keep the hulls apart; no build-up.

### Presentation

- **Look brief** (Q9 a, the [elite livery](../../README.md#factions-and-visual-language)): the
  [Talon](../../air/talon/README.md)'s airframe re-rendered at 1.5× in **gloss black and heavy gold** (gold ≈ 35–45 %
  of the silhouette), the strong top-left rim light so the black never sits dark on black space, red running lights;
  each ace's mark (Harker's barrel and red nose band, Vesper's white ribbons, Kestrel's chevrons and red tail band)
  readable at play size. Their shields' gold hexes at 40 %.
- **Readability** (one check per sheet): the three aces over Level 40's black space, Callisto's disc and Jupiter's
  cream limb ([moods per level](../../../world/jovian/README.md#moods-per-level-q10)); the three marks told apart at
  play size; the sniper's red line never confused with the fleet's lights.
- **Music**: the mini-boss sting (track 21), then the **duel theme** (track 32, the audio step's) replaces the level
  track for the fight; the level track returns after the last ace.

### Sounds brief

Real recordings from Freesound (CC0 / CC-BY), jet and gun sounds; no bubbly or popping sounds. Sounds play in vacuum as
everywhere (the game's convention).

| Sound | Where / when | Brief |
|---|---|---|
| Ace fly-by | an ace passing near the ship, a dive's pull-out | a real jet fly-by, short, doppler; a rougher one for Kestrel |
| Sniper charge | the sniper line's telegraph | a rising electric whine under the line (the enemy `laser-line` family), sharper |
| Sniper shot | the line firing | a hard crack with a short sizzling tail |
| Kestrel's dive | the feint dive | a jet's dive scream rising, cut by the pull-out's roar |
| Spiral loop | Vesper's loop | a fast rhythmic cannon (light autocannon), panning with the loop |
| Ace death | an ace at 0 | a jet's engine failing and a mid explosion, metal tumbling; the last ace a longer chain |

## Concept art

Concept round 72 (Act 6 design phase, step ART-R-B; **decided 2026-10-31 (round 72)**): the three aces in the
elite livery of Q9 a, a and b, each a PNG sheet (the source renders, 8 of 16 headings at 1×, rest / tell / shield
crops, the measured gold share, the three over Level 40 at 1× and over Jupiter's limb at 2×) and a storyboard of the
joins (eight key frames and the conductor's 60 s timeline, measured: one line or dive at a time, two aces firing at
most). Generator `tools/concept/act6_r72.py guard`; prompts: [concept/prompts.md](concept/prompts.md). Round 78's
production frames (M10 part F, step ART-F1, decided 2026-11-01 (round 78 F.1–F.5 accept)): A.8 a's model unchanged at 64 headings, the tells
as additive layers the game draws at the tell's strength, the wrecks, the entry chevrons and call signs; the shield is
the enemy hex shield's `enemy-shield-medium` and the death's flash and debris the vacuum bursts as built. The crest on
Harker's tail fins (the look brief's, not on round 72's model) is added for the pincer's "crest dims" tell.

| File | What | Status |
|---|---|---|
| [concept/honour-guard-r72-a.png](concept/honour-guard-r72-a.png) | Honour Guard A, marks on one airframe: the Talon's airframe for all three, told apart by paint (Harker's barrel and red nose band, Vesper's white ribbons, Kestrel's chevrons, red tail band and twin fins); gold 38–42 % (sheet) | chosen (round 72 A.8 a) |
| [concept/honour-guard-joins-r72-a.png](concept/honour-guard-joins-r72-a.png) | The joins in look A: the sting, Harker alone, Vesper's tell from the far side, two aces, Kestrel's tell from the rear, three under the conductor, the pincer, the first death; the conductor's timeline (storyboard) | chosen (round 72 A.9 ok, with the aces of A.8 a) |
| [concept/honour-guard-final-r78-a.png](concept/honour-guard-final-r78-a.png) | Final frames (M10 part F, step ART-F1, `tools/art/honour_guard.py aces`; held in `build/art-held/sprites/` until the boss's data.yaml): the three aces at 64 headings (72 px cells, the 60×54 airframe at 1.5× the Talon, one 48-colour palette each, gold 34–46 % by heading); at 2× each at rest, its tell (Harker's barrel glint, Vesper's ribbons, Kestrel's tail band), under the enemy hex shield (`enemy-shield-medium`), its engines flaring, Harker's crest dimmed (the pincer); the read at 1× and 2× over round 72's Level 40 stand-in with the sniper line, the ship and Rook (sheet) | chosen (round 78 F.1 accept) |
| [concept/honour-guard-final-r78-a.gif](concept/honour-guard-final-r78-a.gif) | 4.8 s over the stand-in: Harker strafing the top third with the barrel glint and the sniper line, Vesper's figure-eight with her ribbons and wingtip trail, Kestrel's told dive pulled out 110 px short of the ship and the climb | chosen (round 78 F.1 accept) |
| [concept/honour-guard-fx-final-r78-a.png](concept/honour-guard-fx-final-r78-a.png) | The wrecks (16 frames of the tumble each, gold dulled, the cockpit fire at its pivot), the additive sets (cockpit fire, barrel glint, engine flare, wingtip puffs, edge glint), the entry tells (the four chevrons with their glows, the call signs in the HUD's label font) over the stand-in, and an ace's death and the last ace's 2 s chain in key frames (sheet) | chosen (round 78 F.5 accept) |
| [concept/honour-guard-fx-final-r78-a.gif](concept/honour-guard-fx-final-r78-a.gif) | The three entry tells (2.5 s, the game shows one at a time) and the aces flying in from their edges; then the last ace's death: the shield's break, the vacuum flash and debris ring, the hull tumbling down and out with its cockpit burning, the chain's bursts | chosen (round 78 F.5 accept) |
| [concept/entry-capture-r78-a.png](concept/entry-capture-r78-a.png) | In the game (step F5, Level 40, the plan's fit, the ship parked; [recipe](concept/prompts.md#honour-guard-capture-r78-a)): each ace's entry tell, Harker's chevron and call sign under the bar, Vesper's at the right edge, Kestrel's at the bottom left, each ace flying in under its shield | chosen (round 78 F.2 accept) |
| [concept/entry-capture-r78-a.gif](concept/entry-capture-r78-a.gif) | In the game, motion: the three entries | chosen (round 78 F.2 accept) |
| [concept/together-capture-r78-a.png](concept/together-capture-r78-a.png) | In the game: the three aces together under the conductor, a told line, Vesper's pass as the line locks, her ribbon and its falling bullets | chosen (round 78 F.1 accept) |
| [concept/together-capture-r78-a.gif](concept/together-capture-r78-a.gif) | In the game, motion: 17 s of the three together | chosen (round 78 F.1 accept) |
| [concept/dive-capture-r78-a.png](concept/dive-capture-r78-a.png) | In the game: Kestrel's overhead feint, over the top edge, diving at the ship, the pull-out, banking off with the burst back | chosen (round 78 C.1 accept) |
| [concept/dive-capture-r78-a.gif](concept/dive-capture-r78-a.gif) | In the game, motion: the feint dive and pull-out | chosen (round 78 C.1 accept) |
| [concept/sniper-capture-r78-a.png](concept/sniper-capture-r78-a.png) | In the game, 1:1: Harker's sniper line tracking, locked, live | chosen (round 78 F.4 accept) |
| [concept/deaths-capture-r78-a.png](concept/deaths-capture-r78-a.png) | In the game: the first death (Vesper: her shield breaking, the wreck tumbling down and out), Kestrel committing closer, Harker's death, Kestrel alone, the last ace's death, its chain and the credit shower | chosen (round 78 F.5 accept) |
| [concept/deaths-capture-r78-a.gif](concept/deaths-capture-r78-a.gif) | In the game, motion: the three deaths | chosen (round 78 F.5 accept) |
| [concept/marks-capture-r78-a.png](concept/marks-capture-r78-a.png) | In the game at 2×: Harker with his line over Callisto's lit ice, Vesper, Kestrel over black space, Kestrel's pull-out over the ship | chosen (round 78 F.1 accept) |
| [concept/bar-capture-r78-a.png](concept/bar-capture-r78-a.png) | In the game at 1.5×: the segmented bar from the sting (three dim tags, hatched) through the joins to Vesper's and Harker's struck tags | chosen (round 78 F.3 accept) |
| [concept/duel-fight-r78-b.mp4](concept/duel-fight-r78-b.mp4) | The whole duel in the game, real time with the game's sound and music (step F6r, re-recorded after F6s4's flight fixes; [recipe](concept/prompts.md#duel-fight-r78-b)): Level 40 on medium, the plan's fit, flown by the content autopilot through `--replay`; 82 s from the sting (t 96) through the three joins, the conductor's plays, the sniper lines, the three deaths and the last ace's chain to the credit shower (t 178). Rook is shot down early in this run, so the duel is flown without him | superseded (round 79 H.1 change: the aces' turns too sharp; shown again as r79-b) |
| [concept/duel-fight-r78-b.png](concept/duel-fight-r78-b.png) | The recording's poster frame (t 146): the three aces, Harker's sniper line live past the ship | superseded (round 79 H.1 change: the aces' turns too sharp; shown again as r79-b) |
| [concept/duel-fight-r79-b.mp4](concept/duel-fight-r79-b.mp4) | The whole duel again after round 79 H.1's wider turns (step R79b2; [recipe](concept/prompts.md#duel-fight-r79-b)): Level 40 on medium, the plan's fit with Rook, flown by the content autopilot through `--replay`; 77 s from t 96: the sting, the three joins (t 105.5, 113.0, 119.5), the plays in wide smooth arcs (holds as 65 px orbits, joins and dives turning no tighter than 60 px, the nose at most 2.6 radians/s), Vesper down at 150.1, Kestrel at 162.8, Harker at 165.9 with his chain and the credit shower; real time with the game's sound and music | chosen (round 80 H.1 ok: "much better") |
| [concept/duel-fight-r79-b.png](concept/duel-fight-r79-b.png) | The recording's poster frame (t 126): the three aces in their bands over Callisto, Rook beside the ship | chosen (round 80 H.1 ok: "much better") |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

To be built in M10 with Level 40, once round 72 approves the design.

- [x] Stat block and boss script in a `data.yaml` (the three bodies, their joins, stations, pools, the conductor, the
      death changes, par); global difficulty multipliers applied (step F2: `boss.squadron`, loaded by content's
      `SquadronBlock`/`SquadronRules`, checked by `SquadronChecks`; the HP and joins tuned, the par measured)
- [x] A **three-body mid-boss** (the gap analysis §2.10: large): one bar summing three bodies, segments per body, each
      body its own movement, pool and death; the boss checkpoint keeps every ace's HP, shield, station and draw state,
      and a retry and a replay repeat the fight exactly (step F1a: `Squadron`, `AceFlight`, `SquadronTest`; the retry
      repeats the duel from the sting, the built checkpoint; `Level40Test`'s same-seed state)
- [x] The entry tells (the chevron with the call sign; not the edge warning), the joins by HP share or time, the
      conductor's limits, the seeded draws (steps F1a, F1b, F4: `AcePlaybookTest`, `SquadronTest`, `HonourGuardLooks`;
      every measured duel: at most one told line or dive, two aces firing)
- [x] Rook and the autopilot: the nearest ace, out of told lines and dives, the rear mount on Kestrel (step F1b:
      `SquadronRookTest`; step F3: `GuardPlay`, `Level40Test`)
- [x] The fight measured over five seeds (the window, the HP tuned, the par set; all three together ≥ 10 s), the
      calibration against the Kraken's phase 1, the bounty at ≈ 15 % with Level 40's scale (steps F2, F3:
      `HonourGuardFightTest`, `BalanceTest`; the numbers in *Duration and HP*; round 78 B.1: as built, no verdict)
- [x] The HUD's segmented bar and tags ([Act 6 additions](../../../ui/hud/README.md#act-6-additions-draft)); the lines in
      Level 40's radio timeline (step F4: `BossBar.drawSegments` with the call-sign sprites, `HonourGuardLooksTest`;
      `RadioTimelineLevel40Test`)
- [x] Production art: three aces at 64 headings in the elite livery, their tells, shields, the tumbling wrecks; the
      readability checks over Level 40's backdrop (ART-F1, step F4; step F5's captures over the Callisto backdrop,
      round 78)
- [x] The aces fly like aircraft in separate bands and the duel is recorded in real time (round 78 B.1 and B.2 b; steps
      F6s `1a460fdc`, F6c `a62441a4`, F6s3 `eb7057ef`, F6s4 `7c67bf95`, F6r `79974c83`: 2,120 HP an ace, par 66, the
      medium duel about 70 s; `HonourGuardFightTest`)
- [x] The aces turn smoother and wider and the duel is re-recorded (round 79 H.1, step R79b2: turns no tighter than
      60 px nor faster than 2.6 radians/s, the 140 px bands, the separation and keep-offs steering, 1,900 HP an ace, par
      66; `HonourGuardTurnsTest`, `AceFlightSmoothnessTest`, `AceFlightTest`, `HonourGuardFightTest`;
      `duel-fight-r79-b.mp4`)
- [x] Sounds (Freesound, CC0/CC-BY) per the brief; the duel theme (track 32) after the sting (step F4: round 78's
      picks (S.1–S.11, M.1; F6g `b31fb221` swapped the four b's in), `HonourGuardCues`, the sniper's in `SovereignCues`; `Level40SoundsTest`)

## Open questions

Left to the build (the document is approved as drafted):

- **The bounty share**: 15 % of budget(40) (the built mid-bosses', this document) or the gap analysis's 12 %.
- **Kestrel's feints**: never a ram (this document); a real ram on hard only would make the rear deadly but breaks
  the "pull out" read.

## Decisions

- 2026-11-01 (M10 part F, step F2; decided 2026-11-01, round 78 C.4 accept): the stat block and script as data
  (`boss.squadron`: the aces on the parts, none vital; the stations' bands in px below the top edge; the playbook's
  pools, rests, cuts, pincer and conductor); measured on Level 40 and tuned (the spec's lesson: the joins earlier, the
  three HP together): **2,240 an ace**, Kestrel **4 s** after Vesper (easy and hard 3 s), par **62** (the measured mean);
  the bounty stays 360 (Level 40's measured scale is its hand 0.39). Trivia: the hex shields are the spec's 60 as
  written (the Act 6 `shields.heavy` factor is a level rule for the units' Act 5 numbers); the pair volley and the rake
  the Talon's 2-shot fan; the dive's burst 200 px/s; the sniper line holds no fire near the ship.
- 2026-09-30: Drafted in the bosses roster as the Act 6 mid-boss (L40): three ace pilots in custom gold Talons
  attacking from the front, the sides and the rear in turn, each with its own pattern (sniper `laser-line`, `spiral`
  dancer, `kamikaze` feints), taunting over the radio.
- 2026-10-31 (Act 6 design phase, step m10-d-boss; **decided 2026-10-31 (round 72)**): promoted from the
  roster to a draft spec on the gap analysis's recommendations: Q5 a (staggered joins at Harker's 2/3 or 20 s, and 15 s
  later; then a shared playbook under a conductor; each death changes the survivors), Q9 a (the elite livery), J1
  (Level 40's reference 160), J4 (Harker among the three). HP 1,600 an ace (4,800), hex shields of 60; paper ≈ 76 s,
  par 75 until measured; bounty 237 in Act 1 terms (15 % of budget(40) ≈ 1,469 paid at a hand scale 0.59). Trivia
  decided here: the `kamikaze` feints are dives that pull out at 110 px (85 px committed), never a ram; the aces are
  `medium` custom Talons at 1.5× (a `small` mid-boss reads poorly); each ace's first entry has **its own tell**
  instead of an edge warning (the 2026-10-21 rule that bosses trigger none); no popcorn during the duel; the aces'
  fight lines are this document's, the call signs and voices the story's.
- 2026-10-31, design phase (step m10-d-act; decided 2026-10-31 (round 72)): the bounty follows Level 40's hand scale **0.39**:
  **360** in Act 1 terms, **120 an ace**, 1,472 paid (15.0 % of budget(40)); the build recomputes it with the measured
  scale.
- 2026-10-31 (user, [round 72](../../../concept-rounds/round-72/README.md) closed): **Q5 a** (staggered, then
  together, one segmented bar; b three duels in sequence and c all three from the start rejected), J4 (Harker, Vesper
  and Kestrel, Harker on David Clarke's voice; Vesper is Jenny Bradshaw and Kestrel Expatriate, V.1 and V.2) and the
  document approved as drafted (D.12; `design: approved`); **A.8 a**, "marks on one airframe", and the join storyboard
  (A.9 ok; the board with aces a) chosen (`art: chosen`; the b rows in `concept/rejected/`); the duel theme is "Three
  Aces" (M.2 b).
- 2026-11-01 (M10 part F, step F3; decided 2026-11-01, round 78 C.5 accept): `GuardPlay` (the autopilot reading the told
  lines and dives) and the three HP tuned together to **1,900 an ace** (easy 4,275 / hard 7,410 in all); par 62 (F3's
  mean 62.3).
- 2026-11-01 (M10 part F, step F5; decided 2026-11-01, round 78 B.3 accept): the full check green; the duel measured again
  on the built Level 40 (medium 66.0 s, 5 of 5 in the window; *Duration and HP*); the captures from the game added to
  *Concept art*; every Implementation item ticked (`implementation: done`). Asked in round 78: the duel's numbers, the
  80 px hull rule against the bands, the random dodger at 2.59× the Kraken's phase 1.
- 2026-11-02 (M10 round 78 follow-up, steps F6s and F6c; B.1 and B.2 b): the aces fly like aircraft (smooth turns,
  eased speed, the nose leading; the holds as orbits: Harker's racetrack, Vesper's eights, Kestrel's lane orbit ≈ 90 px
  in from a side; Harker's nose on the ship only while he aims) in **separate bands**: Harker y 60–160 (was 70–170),
  Vesper 190–290 (was 150–330), Kestrel 420–520 (kept); each keeps its role, Vesper's band 130 px over Kestrel's.
  Re-measured: medium 65.5 s, the par **66** (was 62; decided here, the user's rule for such numbers); the stat block's
  difficulty text now says Kestrel joins 3 s after Vesper on easy and hard (it said 20 s and 10 s; the data's 3 s).
- 2026-11-02 (user, [round 78](../../../concept-rounds/round-78/README.md) closed, 2026-11-01; page answers and one
  note): the aces' art accepted as production art (F.1–F.5: models, entry chevrons, bar, sniper line, deaths and the last
  chain; C.9; `art: chosen`); sounds S.1–S.4, S.6 a, **S.5, S.7, S.8 and S.9 b** (not the recommendation; the unchosen
  files in the sfx part's `concept/rejected/`); **B.2 b** (separate bands, built); B.3 (the dodger at 2.31×), B.4 (the
  halting arena), B.5 (the fly-by range 110 px) and C.1–C.3, C.5 accepted; **B.1 change**. The user's note (verbatim):
  "Change the question you mean? This is just total unreadable. It's just random numbers in random order. What do want
  me to say about those? You should ask clear questions written in clear words. Not whole blobs of text with numbers
  intermixed. Keep you questions short and precise. Show in rendering what the differences are. I can't say a thing
  about this whole boss fight. All I can see in the recordings is that the movement of the other crafts is not very
  natural." So the duel's numbers (2,120 HP an ace, par 66, medium about 70 s) stay as built without a verdict; the aces'
  flight was already reworked (F6s–F6s4) and the whole duel recorded in real time with sound (F6r) for the next round's
  boss question, `duel-fight-r78-b.mp4` (`proposed`).
- 2026-11-02 (built since round 78 opened; recorded here): F6g `b31fb221` (the four b sounds in the game; the last ace's
  wreck and its 2 s chain); F6s `1a460fdc`, F6c `a62441a4`, F6s3 `eb7057ef` and F6s4 `7c67bf95` (the aces fly like
  aircraft: smooth turns with banking, eased speed, the nose leading, holds as orbits, separate bands Harker 60–160,
  Vesper 190–290 and Kestrel 420–520 px below the top, avoidance instead of shoves; each ace 2,120 HP, par 66, the
  medium duel about 70 s); F6r `79974c83` (the duel as one real-time recording with sound, flown by the autopilot through
  the debug `--replay` option).
- 2026-11-02 (M10 part I, step I2; decided 2026-11-03 (round 80 H.1)): hard's HP lever on the aces 0.95 (the difficulty hook `hp`,
  in place of the difficulty's `enemy_hp` 1.3): the hard duel 65.5 s (60.5-70.9, five seeds), inside the mid-boss
  window 45-75 (91.1 s before); hard's rests, joins, taps and spiral as they were; medium unchanged.
- 2026-11-02 (round 79 H.1, step R79b2; decided by the main agent, the user judges the recording): the aces turn
  smoother and wider: no turn tighter than 60 px nor faster than 2.6 radians/s, the nose at most 2.6 radians/s; the
  bands 140 px (Harker y 27–167 with his strafe's centre at x 130, Vesper 205–345, Kestrel 380–520), left in transit
  on wide arcs; the 80 px hull rule and the keep-offs steer (the yielding ace's pace and arc), the exact solver only
  holding the floors (40 px, the ship's ring); Kestrel changes lanes only through a free height over the ship, else
  stays in its lane (its orbit rising over the ship) and plays; Vesper's ribbon inside her band (y 235–285). Sharp
  turns 3–13 a minute an ace (40–77 before), reversals 6–18 (22–52); hulls under 80 px 8–14 % of the duel. HP 1,900 an
  ace for the par 66 (medium 67.1 s). Recording `duel-fight-r79-b.mp4`. Rejected: the hard 80 px stop and the band
  clamps (they parked a yielding ace dead and shoved it in jinks); a lane change re-planned each step (Kestrel hovered
  over the ship).
- 2026-11-03 (M10 part I, step I4, art audit): the duel recordings' statuses follow the answers: `duel-fight-r79-b` chosen (round 80 H.1 ok), `duel-fight-r78-b` superseded; every Concept art row is answered, `art: chosen` stays.
