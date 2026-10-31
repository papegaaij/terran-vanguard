---
title: Honour Guard
design: approved
implementation: not-started
art: chosen
depends-on: [../../air/talon, ../../../world/jovian, ../../../campaign/act-6-jovian-storm, ../../../player/wingmen, ../../../ui/hud, ../../../story/characters, ../../../systems/economy]
updated: 2026-10-31
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

### Stat block (draft)

Numbers at **medium**; HP in damage units (a Pulse Cannon L1 shot does 2); bullet classes (`small` = 4,
`medium` = 6, `heavy` = 10, `laser` = 8) and contact damage from the [balancing basis](../../README.md#balancing-basis).
The reference DPS is the **proposed** Level 40 value of **160** (J1, the
[Act 6 hand plan](../../../player/generator/README.md#act-6-hand-plan-draft); decided 2026-10-31 (round 72)). The aces fly the
[Talon](../../air/talon/README.md)'s airframe; this table sets only what differs.

| Field | Value |
|---|---|
| Faction | Jovian Ascendancy, Vorne's guard; three **machines** (but a mid-boss: never stunned; the EMP Burst empties their shields) |
| Layer | `air` (`space` is `air` in vacuum, the [Jovian media](../../../world/jovian/README.md#media-per-level)) |
| Size tier | three `medium` bodies |
| Size | each a custom Talon at 1.5× the airframe: 60×54 px, hitbox 42×36; the leader's sniper barrel adds 14 px under the nose (no hitbox) |
| Parts | three **bodies**, each `vital` for itself: **Harker** (the leader), **Vesper** (the dancer), **Kestrel** (the feinter); call signs are working names, the story step owns them (J4, see *The aces*) |
| Orientation | `64 angles` each (they bank and turn through loops and dives; the Talon flies 16) |
| HP | **1,600 each, 4,800 in all** (easy 3,600 / hard 6,240, from the global multipliers) |
| Armour / shield | a gold hex **shield of 60** on each ace under the [enemy shield rule](../../README.md#enemy-shields) (spent first, refills after 2 s; the [Tesla Coil Pod](../../../player/weapons/tesla-coil-pod/README.md)'s `shield-breaker` empties it, the EMP Burst strips all three); no mirror (a unit carries one defence) |
| Speed | Harker 140 px/s on his station, 280 px/s sliding; Vesper 200 px/s through her loops; Kestrel 320 px/s diving, 260 px/s raking |
| Movement | per ace (see *The aces*); hull edges always **≥ 80 px apart** (the Act 6 formation rule: no two medium hulls overlap) |
| Attack | per ace, drawn from its pool (see *The playbook*): Harker's sniper line (`laser-line`, `laser` 8), pair volley and fan (`small`); Vesper's `spiral` loop, roll fan and ribbon (`small`); Kestrel's feint dives and rear rake (`medium` bursts, `small` pairs) |
| Contact | `medium` 15 to the ship (once), the ship pushed out; the ace takes 40 and flies on |
| Formations | the solo set piece: one, then two, then three aces |
| Weak points | none beyond the bodies (each ace's engines glow; no weak-point multiplier) |
| Effective traits | `spread` (three bodies apart), `rear` (Kestrel's station), `homing` (moving targets), `shield-breaker` (the shields, a bonus only) |
| Credits | ≈ **15 % of budget(40)** in all, a third per ace (see *Bounty*): **360** in Act 1 terms at Level 40's hand `bounty_scale` of 0.39, 120 an ace |
| Death | per ace a `medium` vacuum flash and its hull tumbling away; the last ace a 2 s `large` chain, the credit shower (no screen flash: a mid-boss) |
| First level / used in | L40 only (the Level 49 boss rush has no echo of it) |
| Difficulty hooks | easy: Vesper joins at Harker's half or after 25 s, Kestrel 20 s later; Harker's line single, no double tap; every rest ×1.3; Vesper's spiral 1 arm; Kestrel pulls out at 130 px. Hard: Vesper joins at 3/4 or after 15 s, Kestrel 10 s later; the double tap a triple; rests ×0.8; Vesper's spiral 3 arms; Kestrel pulls out at 90 px |

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
  the sides. The ship's band (the bottom 140 px) is Kestrel's: the rear mounts and a turn of the field pay there.

**Entry tells.** Each ace's **first** entry is announced by **its own tell**, 2.5 s ahead: a gold chevron with its
call sign at the edge it will enter by, its engines' glint beyond the edge and its taunt on the enemy channel. It is
not the generic edge warning: bosses never trigger edge warnings (the [bullet rules](../../README.md#bullet-readability-rules),
user, 2026-10-21); the gap analysis's "edge warning before any ace shows" is met by the ace's own tell. Its
segment of the bar lights as it enters ([HUD](../../../ui/hud/README.md#act-6-additions-draft)). Loops and re-entries in
an ace's own pattern never warn.

### The aces

| Ace | Joins | Station and movement | Look (the elite livery) |
|---|---|---|---|
| **Harker** (the leader; the sniper) | first, from the top edge, after the sting | holds the **top third** (y 70–170), strafing 40–80 px at 140 px/s, his nose on the ship; a **slide** crosses to the other side at 280 px/s | a long sniper barrel under the nose, a red nose band, the Ascendancy crest on his fins; Harker of Level 29's mutineers, now in Vorne's guard (J4) |
| **Vesper** (the dancer; `spiral`) | from a **side** edge (the one away from Harker) when Harker is down to **2/3** of his HP or after **20 s**, whichever first | loops through the **middle band** (y 150–330) in figure-eights 160–220 px across, never within 120 px of the ship | white ribbon stripes along the wing edges and the tail |
| **Kestrel** (the feinter; dives that pull out) | from the **bottom edge** (the rear) **15 s after Vesper**, or at the **first ace's death**, whichever first | holds **below or beside the ship band** (y 420–520, or a side's outer 60 px), then dives; out of a pull-out it climbs back to its station | black and gold chevrons, a red tail band, twin swept fins |

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
| Vesper | **Ribbon** | a fast pass across the field at y 200–260 leaving a line of 8 slow bullets (`small`, 80 px/s down) with two gaps of 50 px |
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

Target: the mid-boss window **45–75 s** at medium; the **par** is the paper's **75 s** until the build measures the
mean (then the mean).

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
most). Generator `tools/concept/act6_r72.py guard`; prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/honour-guard-r72-a.png](concept/honour-guard-r72-a.png) | Honour Guard A, marks on one airframe: the Talon's airframe for all three, told apart by paint (Harker's barrel and red nose band, Vesper's white ribbons, Kestrel's chevrons, red tail band and twin fins); gold 38–42 % (sheet) | chosen (round 72 A.8 a) |
| [concept/honour-guard-joins-r72-a.png](concept/honour-guard-joins-r72-a.png) | The joins in look A: the sting, Harker alone, Vesper's tell from the far side, two aces, Kestrel's tell from the rear, three under the conductor, the pincer, the first death; the conductor's timeline (storyboard) | chosen (round 72 A.9 ok, with the aces of A.8 a) |

Rejected proposals: [concept/rejected/README.md](concept/rejected/README.md).

## Implementation

To be built in M10 with Level 40, once round 72 approves the design.

- [ ] Stat block and boss script in a `data.yaml` (the three bodies, their joins, stations, pools, the conductor, the
      death changes, par); global difficulty multipliers applied
- [ ] A **three-body mid-boss** (the gap analysis §2.10: large): one bar summing three bodies, segments per body, each
      body its own movement, pool and death; the boss checkpoint keeps every ace's HP, shield, station and draw state,
      and a retry and a replay repeat the fight exactly
- [ ] The entry tells (the chevron with the call sign; not the edge warning), the joins by HP share or time, the
      conductor's limits, the seeded draws
- [ ] Rook and the autopilot: the nearest ace, out of told lines and dives, the rear mount on Kestrel
- [ ] The fight measured over five seeds (the window, the HP tuned, the par set; all three together ≥ 10 s), the
      calibration against the Kraken's phase 1, the bounty at ≈ 15 % with Level 40's scale
- [ ] The HUD's segmented bar and tags ([Act 6 additions](../../../ui/hud/README.md#act-6-additions-draft)); the lines in
      Level 40's radio timeline
- [ ] Production art: three aces at 64 headings in the elite livery, their tells, shields, the tumbling wrecks; the
      readability checks over Level 40's backdrop
- [ ] Sounds (Freesound, CC0/CC-BY) per the brief; the duel theme (track 32) after the sting

## Open questions

Left to the build (the document is approved as drafted):

- **The bounty share**: 15 % of budget(40) (the built mid-bosses', this document) or the gap analysis's 12 %.
- **Kestrel's feints**: never a ram (this document); a real ram on hard only would make the rear deadly but breaks
  the "pull out" read.

## Decisions

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
