---
title: Abyssal Maw
design: approved
implementation: not-started
art: chosen
depends-on: [../../../world/europa, ../../../campaign/act-4-deep-water, ../../naval/eel-swarm, ../../../player/wingmen, ../../../story/twist, ../../../story/characters/the-choir, ../../../systems/economy]
updated: 2026-10-22
---

# Abyssal Maw

## Summary

The Act 4 boss (Level 28, the Vrell hatchery under Europa's ice): a serpent leviathan coiled round
the hatchery's thermal vent. Its body lines the arena's edges as a living, glowing frame, its head
rears at the top; it breathes rings from its gill sacs, fires bioluminescent fan walls, sweeps the
arena with its coil, sheds Eel Swarms from its body and dives once into the dark to lunge up through
the field. In its last phase grey human machinery bolted into its body sparks open: Helix implants,
the last clue before the reveal. After its death the wing breaks out through the ice and runs a
gauntlet of Ascendancy cruisers in black and gold (*The reveal*, below).

**Status: approved in round 48 (2026-10-22)** (Act 4 design phase, step D-boss, 2026-10-21; pending the user's review in concept
round 48). It follows the gap analysis's recommended answers, both pending in round 48: **Q9 b**, the
coiled guardian (the drafted fight, Q9 a, is under *Variant a*), and **Q10 a**, the breakout and run.
Every number is a hand-written **draft** at medium; there is no `data.yaml` until M8 part H builds the
boss (a `data.yaml` here would be loaded and flown by `check`).

## Design

### Why a coiled guardian

The roster's Maw rose from below in a jaw-snap on a shadow tell. One act earlier the [Dust
Colossus](../dust-colossus/README.md) already breaches under the ship on a marker (its phase 3), at the
edges and from below. The coiled guardian keeps the roster's ingredients (Eel Swarm trails, fan walls,
the shadow from below, the Helix implants) but turns the fight inside out: the Colossus hides and
appears; the Maw is **always there**, framing the arena, and the danger is the frame moving. The
shadow from below survives as **one** event (the dive and lunge of phase 2), not a cycle.

### Stat block (draft)

Numbers at **medium**; HP in damage units (a Pulse Cannon L1 shot does 2); bullet classes (`small` =
4, `medium` = 6, `heavy` = 10) and contact damage from the [balancing
basis](../../README.md#balancing-basis). The reference DPS used is the **proposed** Level 28 value of
**120**, an effective value under water (Act 4's proposal: 96 at Level 22, +4 a level; part A sets it on
the extended balance plan). Bullet speeds are written **base → in water**: the level's water factor
(bullets ×0.7 on both sides, the [under water rules](../../../world/europa/README.md#under-water-rules))
is applied on top of the base.

| Field | Value (draft) |
|---|---|
| Faction | Vrell, with Helix implants (grey, unmarked human machinery; see *Phase 3*) |
| Layer | the play plane under water (in code `air` with the level's `medium: underwater`): all weapons hit it, those without `anti-sub` at the water's factor (×0.7 under Q1 b; ×0.5 as drafted, Q1 a). During the dive (phase 2) the head and the coil are in the deep water **below** the play plane (`sub`): only `anti-sub` weapons and the Smart Bomb reach them |
| Size tier | `huge` |
| Size | head ≈ 150×130 px (hitbox 110×100); the body a chain of ≈ 31 segments, 64 px wide, ≈ 56 px apart (≈ 1,750 px with the head), half of each segment beyond the field's edge while coiled; gill sacs 36×36; implants 40×40 |
| Parts | the **head** (`vital`; the bar): the skull ×0.5, the lime **throat** a weak spot ×2 while the jaw is open; four **gill sacs** (`destroyable`, outside the bar, phases 1–2); the **coil** (the body chain: armoured, shots glance with a spark and the Ion Beam stops on it, no HP; a wall for the ship); three **Helix implants** (phase 3: weak spots ×2 of the head's HP while sparked open, armoured while shut) |
| Orientation | the head `fixed` (facing down, with jaw, rear-up, dive and lunge frames); the coil's segments `32 angles` along the chain |
| HP | head 6,700; gill sacs 150 each (600); total **7,300** (easy 5,475 / hard 9,490, from the global multipliers ×0.75 / ×1.3) |
| Armour / shield | the coil always (a wall, no damage); the skull ×0.5; the throat ×2 while the jaw is open; an implant ×2 while sparked open, armoured while shut; in the dive only `anti-sub` and the Smart Bomb |
| Speed | the head 0 px/s at rest; a coil sweep swings in at ≈ 220 px/s; the lunge crosses the field in 0.8 s (≈ 680 px/s) |
| Movement | anchored round the vent (see *Arena*); the coil sweeps, the dive and the lunge (see *Phases*) |
| Attack | phase 1: the gill sacs' `ring`s of 10 (`small`, 100 → 70 px/s), one sac every 1.6 s in turn, each after a 0.6 s swell; the throat's aimed `fan` of 5 over 40° (`medium`, 130 → 91 px/s) every 5 s, after a 0.6 s glow, the jaw open 3 s; phase 2: the **fan walls** (a `fan` of 17 over 150°, `small`, 110 → 77 px/s, with one gap) every 4 s, the **coil sweeps** every 7 s, the **dive and lunge** once; phase 3: the open implant's aimed `burst` of 3 (`small`, 140 → 98 px/s) every 1.5 s, sweeps every 6 s from three sides; Eel Swarms shed from the body in every phase |
| Formations | solo set piece (Eel Swarms shed into the arena) |
| Weak points | the lime throat (while the jaw is open) (×2); the hot-amber Helix implant cores (phase 3, while open) (×2) |
| Effective traits | `anti-sub`, `homing`, `side`; `piercing` only helps against the shed Eel Swarms (Level 28 recommends `anti-sub`, `homing` and names `side` in Varga's L3 line, dropping the roster's `piercing`) |
| Credits | **345** (score 3,450 × chain) in Act 1 terms (head 297, gill sacs 12 each); × 4.096 (Act 4's factor 1.6³) × Level 28's `bounty_scale` of about 0.74 it pays **1,046 = 24.0 % of budget(28) = 4,350** (see *Bounty*; recomputed from the draft's 510 at an assumed 0.50 once [Level 28](../../../campaign/act-4-deep-water/level-28-abyssal-maw/README.md#credit-budget) set the scale) |
| Death | `huge` underwater variant (see *Death*): the implants blow out one after another, the coil convulses and unwinds into the dark, the vent flares; chained bursts of silt and lime ichor over 4 s; screen flash at the chain's end (an act boss); credit shower |
| First level / used in | L28; L28 only (the L49 boss rush re-uses the Brood Carrier, Dust Colossus and Iron Sovereign, not the Maw) |
| Difficulty hooks | hard: gill rings of 12, fan walls of 21, the sweep's tell 0.9 s, the lunge's shadow 1.6 s, implants open 3 s, Eel Swarms of 10 segments; easy: rings of 8, fan walls of 13 with two gaps, the sweep's tell 1.5 s, the lunge's shadow 2.4 s, implants open 5 s, no implant `burst` (single `aimed` shots) |

### Arena

The hatchery vent ([Level 28](../../../campaign/act-4-deep-water/README.md); the level document places
the arena and owns its timeline). The scroll **halts** over the vent (no crawl: the coil is anchored to
the floor, so nothing slides under it; the Dust Colossus's lesson, its K.c). Screen space, the field
480×540, y down:

| Element | Where | Notes |
|---|---|---|
| The vent | a caldera filling the arena floor, its glowing throat at (240, 330), ≈ 140 px across | `ground` scenery, no hit; lights the arena from below (Q2 a: L28 is lit by the vent and the hatchery's bioluminescence) |
| The head | centre (240, 95), the throat at (240, 135) | clear of the boss bar's plate (35 px); 320 px above the ship band, inside the Ion Beam's under-water reach (×0.75 of 540 = 405 px) |
| Gill sacs | (178, 80), (302, 80), (190, 128), (290, 128) | two each side of the head |
| The coil | from the head along the top to the top-left corner, down the left edge (inner flank x 50), along the bottom (inner flank y 505), up the right edge (inner flank x 430); the tail tip at the top-right corner | the ship flies in x ≈ 62–418, y ≤ 493 |
| Implants (phase 3) | the neck at (110, 60); the left coil at (40, 300); the right coil at (440, 300) | the neck one in the front guns' reach, the side ones for wing mounts, angled and homing shots |

The coil is a **wall**: it stops the ship and the escort without damage (the trench walls' rule of
Level 25); only a sweep or the lunge hurts. Enemy bullets pass over it. The long bar `ABYSSAL MAW`
appears with the boss warning, the boss checkpoint (none on hard) and the Vrell boss theme; the level
clock halts while it lives.

**Intro** (≈ 6 s): the Choir sings first ("[the Choir sings]", the static in the HUD portrait frame:
Choir transmissions precede a Vrell boss). In the vent's glow a ring of faint lights circles under the
field: the coil, below the play plane. It rises along the edges over 3 s (the segments brighten and grow
as they come up, silt pouring off their plates), the head lifts at the top last, jaw open in a silent
roar, and the bar fills. Invulnerable until the head is up. Nothing pops in: every part is seen rising
from the dark (the water rules' lesson from the Kraken).

### Phases

| Phase | Ends at | Behaviour |
|---|---|---|
| 1 — Coiled | all four gill sacs destroyed, or the head below 70 % | The **gill sacs** breathe in turn: a sac swells 0.6 s (its lime glow brightens; the tell) and exhales a `ring` of 10 (`small`, 100 → 70 px/s); one sac every 1.6 s, so each sac every 6.4 s (a destroyed sac's turn is skipped). The **jaw** opens every 5 s for 3 s: the throat glows 0.6 s, then an aimed `fan` of 5 over 40° (`medium`, 130 → 91 px/s); the throat ×2 while open. One **Eel Swarm** shed every 10 s (at most one alive). The coil lies still. The first phase: a fixed frame, slow rings, the head in the front guns' reach. |
| 2 — Tightening | the head below 30 % | The **fan walls** (see below) every 4 s, the jaw open from the glow to the wall; living gill sacs keep breathing, one every 2.4 s. The **coil sweeps** every 7 s, left and right in turn, between the walls. One Eel Swarm every 12 s. As the head falls below **50 %** the Maw **dives** once (see below). |
| 3 — Implants | the head at 0 | The Maw rears: the jaw clamps shut (the skull ×0.5 for the rest of the fight, no throat window) and the three **Helix implants** tear open through its plates (see below). Sweeps every 6 s from the left, the right and the **bottom** in turn (the bottom sweep a rear threat). Eel Swarms every 8 s (at most two alive). An **armour patch** drops as the phase begins. |

**Fan walls** (phase 2): the throat glows 0.8 s, then a `fan` of **17** over **150°** (`small`, 110 → 77
px/s, bioluminescent teal orbs, drawn bright), aimed down with **one** orb missing: the gap. The gap is
placed 40–90 px to the left or right of the ship's x at the ship band (never on it: the ship must move),
and within **140 px** of the previous wall's gap, so two walls on the screen at once always chain (at
77 px/s a wall takes ≈ 5 s to reach the ship band). The gap at the ship band ≈ 110 px edge to edge.

**Coil sweeps** (phases 2–3): a bight of the coil, five segments (≈ 280 px) centred on the ship's y
(clamped 160–420; the bottom sweep on the ship's x, clamped 140–340), is chosen as the tell starts and
does not follow. **Tell 1.2 s**: silt streams off the bight's plates, its glow line brightens lime, a low
groan. Then it **swings in** 110 px (the bottom one 90 px) in 0.5 s, **holds** 0.8 s and swings back in
0.8 s. A ship or escort the bight reaches during the swing takes `heavy` = 10, once a sweep, and is
pushed out of it (the Kraken slam's impact rule). Bullets are not stopped by it.

**The dive and the lunge** (once, as the head falls below 50 %; ≈ 12 s): the Maw roars and the whole
coil **slides off the edges into the dark** below (2 s; the layer `sub`: only `anti-sub` and the Smart
Bomb reach it, its bar stays), and the vent's glow gutters: for the next ≈ 8 s the arena is near the
[darkness](../../../world/europa/README.md) of Level 26 (the ship's light cone and the glows; a
reminder of Blackwater, not a new rule). The arena is open and quiet: no walls, no sweeps. **4 s** after
the dive a **shadow** runs up a **column** 90 px wide at the ship's x (clamped 95–385, fixed when it
starts) from the bottom edge: **2.0 s** (the tell: the silt in the column lifts, the shadow darkens
upward). Then the head bursts up through the play plane at the bottom edge and races up the column to
the top in **0.8 s**, jaws open: a ship or escort in the column as it passes takes `heavy` = 10, once.
Its body streams up the column behind it and **stays there for 3 s** (a wall splitting the arena; its
throat lime as it passes, ×2: a brief window), then the coil slides back round the edges from the top
(2 s) and the light returns. One dive in the fight, never a cycle (Q9 b).

**Phase 3, the implants**: three grey housings bolted through its plates (the neck, the left coil, the
right coil; see *Arena*) **spark open in turn**, neck → left → right → neck…: a **1.0 s** tell (sparks,
an amber flicker under cracked casing), then the housing's plate blows aside and its **hot-amber core**
is open for **4 s** (the Ascendancy rule for exposed cores): ×2, and the hits count on the head's bar
(the implants are wired into its nerves). An open implant **discharges**: an amber arc crawls over it
0.4 s, then an aimed `burst` of 3 electric bolts (`small`, 140 → 98 px/s) every 1.5 s. Shut, an implant
is armoured. They are not destroyed one by one: they blow out at the death. At the **first spark** the
radio names them (Level 28 owns the wording): **Dr. Varga**: "Commander, that's not Vrell. That's
machinery in its spine. Relay housings… Helix pattern." **Okafor**: "Then hit the machinery, Lancer."

The **dive** has its own radio event, the new **`boss-dive`** (queued as a `PHASE` line, so it waits for a gap and goes stale after 20 s; the head below 50 %): **Dr. Varga**: "It's gone under! Watch the dark. It comes back fast!" (Level 28 owns the wording; a small code addition for the build, part H.)

### Eel Swarms

The [Eel Swarm](../../naval/eel-swarm/README.md) trails of the roster: a coil segment's plate **flares
lime for 1.0 s** (the tell), then an Eel Swarm of **8** segments (hard 10; the boss sets the count)
slips out of the coil there and swims across the field toward the opposite side on the unit's own
`snake` path, leaving at the far edge. Shed points cycle left coil → bottom coil → right coil, the one
nearest the ship skipped. They are the boss's shed units: they count into the level's enemies (as the
Kraken's jellies), not into its density, carry the Eel Swarm's own bounty with the act HP factor
(×120/100 at L28), and get **no edge warning** (a boss and what it sheds never trigger them, the user's
rule of 2026-10-21; the flaring plate is the tell). They give `piercing` and homing loadouts their
targets.

### Tells

| Attack | Tell | Medium (easy / hard) |
|---|---|---|
| Gill ring | the sac swells, its lime glow brightens | 0.6 s |
| Throat fan (phase 1) | the throat glows lime, the jaw opens | 0.6 s |
| Fan wall (phase 2) | the throat glows, teal orbs light along the jaw's rim | 0.8 s |
| Coil sweep | silt streams off the bight, its glow line brightens, a groan | 1.2 s (1.5 / 0.9) |
| Eel Swarm shed | a coil plate flares lime | 1.0 s |
| Dive | a roar, the coil slides into the dark, the vent gutters | 2.0 s |
| Lunge | a shadow runs up the column from the bottom edge, its silt lifts | 2.0 s (2.4 / 1.6) |
| Implant open | sparks, an amber flicker under the casing | 1.0 s |
| Implant burst | an amber arc crawls over the housing | 0.4 s |

### The escort (the Bulwark)

Rook is missing (shot down at the end of Level 26, radio silent in Levels 27–28); the escort slot flies
the **Bulwark** heavy drone ([wingmen](../../../player/wingmen/README.md)). It follows Rook's boss rules:
it keeps out of a telegraphed sweep's bight and out of the lunge's column until they have passed, holds
off the coil, shoots the gill sacs first in phase 1 and an open implant in phase 3, and is hit by sweeps
and the lunge like the ship. With `--escort none` the fight is the same without it. The test autopilot
does the same, so the balance and playthrough tests can finish the fight.

### Duration and HP

Target: the act-boss window **90–180 s** at medium, aimed at ≈ **130 s**; the **par** is the measured
mean (draft **130 s**).

Paper estimate (the window-aware method of the Kraken's `BalanceTest` check): the effective boss DPS is
0.6 × 120 = **72**. Phase 1: the jaw open 3 s of 5 (×1.25, the throat-and-skull mean) and shut 2 s (×0.5):
×0.95 on the bar, with a quarter of the fire spent on the sacs. Phase 2: ×0.85 (the jaw open for each
wall, the skull between), plus the dive's ≈ 12 s with next to no damage. Phase 3: the implants ×2 while
open, one at a time, the side ones reached only by part of the loadout, the skull ×0.5 for the rest:
≈ ×0.9.

| Phase | HP | Time |
|---|---|---|
| Intro, the Choir, the coil rising | — | ≈ 6 s |
| 1 — Coiled (100 → 70 %, + the sacs) | 2,010 + 600 | ≈ 38 s |
| 2 — Tightening (70 → 30 %, the dive at 50 %) | 2,680 | ≈ 44 + 12 s |
| 3 — Implants (30 → 0 %) | 2,010 | ≈ 31 s |
| Total | 6,700 + 600 | **≈ 131 s** |

Lessons from Acts 2–3 the build must apply (M8 part H):
- **Measure, then tune the HP, not the attacks** (Kraken E6, Spire H10, Colossus H4, walker F4): the
  autopilot with the balance plan's Level 28 fit (its under-water loadout, part A) over five seeds,
  every seed inside 90–180 s, the par at the measured mean. The Colossus measured ≈ 0.8 of its paper
  length, the walker anywhere from 0.5 to 1.4: expect the HP to move.
- **Phase 1 against the Kraken's** (H.4's band): the calibration harness's random dodge loses
  0.8–1.2× what it loses to the Kraken's phase 1 (11.4 per 10 s); the levers are the ring's count and
  the sac interval. Bullets ×0.7 stay longer on the screen: count a ring's lifetime, not its rate.
- The autopilot (and the Bulwark) must leave a sweep's bight, step out of the lunge's column and
  chain the walls' gaps, or the tests cannot finish the fight.

### Bounty

An act boss aims at ≈ **25 %** of its level's budget (the economy's 30 %, capped by the perfect-run
≥ 1.4 × budget rule: Level 21 learned the cap sits near 24.8 %). Data is in Act 1 terms: **bounty =
share × budget(28) ÷ (4.096 × `bounty_scale`)**. Level 28's hand-estimated `bounty_scale` is **≈ 0.74** (Act 3 ended
at 0.26–0.83, Level 21 at 0.50; the Act 4 levels' popcorn is cheap Glassfin, so the scale runs high): 0.24 × 4,350 ÷ (4.096 × 0.74) ≈ **345** (head 297, gill
sacs 12 each), paid **1,046 = 24.0 %** of budget(28). (The first draft assumed 0.50 and wrote 510; corrected 2026-10-21 to agree with Level 28.) When Level 28's scale is measured in the build, the bounty is
recomputed with the same formula; the share stays at 24 % unless the level's mix lets the perfect run
carry more. The reveal's cruisers pay nothing.

### Death

The head at 0: the jaw gapes and locks open; the three implants **blow out** one after another
(neck, left, right, 0.4 s apart: amber arcs, a spray of grey casing, a heavy electrical crack each); the
coil **convulses** round the rim, then **unwinds and sinks** into the dark below, segment by segment from
the tail to the head, each with a burst of silt and lime ichor; the head sinks last into the vent, whose
glow **flares** white-lime as it goes. 4 s of chain, then the screen flash (an act boss) and the credit
shower over the vent. No bubbles: silt clouds, ichor and pressure waves (Q12 a).

At the chain's start the HUD portrait frame fills with the Choir's static, and a fragment at the Acts 3–4
stage of [its arc](../../../story/characters/the-choir/README.md) (Q8 a): **"…many… we were many here…"**
(pending the user's review; Level 28 owns the line and its delay: **3 s** after the chain's start, so it runs K+3 to ≈ K+8.6 (Level 21's was 4.5 s); Okafor's "Maw's down" follows at ≈ K+8.6). Then the
breakout begins.

### The reveal: the breakout and the run (Q10 a)

The level document owns the timeline and the radio; this is the set piece's staging and the cruisers'
spec. Times from the kill (the chain's start); draft.

| Time | Beat |
|---|---|
| 0–5 s | The death chain, the flash, the credit shower (collectable: the scroll stays halted). |
| ≈ 8.6 s | **Okafor** (after the Choir's fragment, K+3 to ≈ K+8.6): "Maw's down. Aegis Wing, up the shaft. Now." The scroll climbs: the wing rises out of the caldera into the Vrell drill shaft (Level 23's shaft walls, `Terrain`, run in reverse), the light from above growing, the ice ceiling (`high-air`) cracking ahead. No enemies; a few Glassfin and eels flee upward past the ship, firing nothing. ≈ 10 s. |
| ≈ 15 s | **The breakout** (Europa's signature set piece): the ceiling shatters in a white burst (local, not the act boss's flash), ice slabs tumble out of the field, the under-water grade lifts over 1 s, the sound of ice tearing and a column of water. The level's medium switches back to **air** at the break line: the bullet factor, the drag and the weapon factor end. Above: Europa's night ice under Jupiter (Level 22's backdrop), the Vrell fleet's carriers rising from other drill holes in the `deep` and `high-air` layers. |
| ≈ 18 s | **The reveal**: the ice on both flanks splits and two **Ascendancy cruisers** rise through it beside the Vrell carriers (4 s, growing from dim and small to full scale, ice sliding off their hulls), black and gold; the reveal sting (Vorne's motif hidden in it). **Dr. Varga**: "Those aren't Vrell hulls—" **Okafor**: "Those are not Vrell ships. God help us. Those are ours." Gun ports open in amber along their sides. |
| ≈ 22–57 s | **The run** (≈ 35 s, 30–40): the cruisers fire on the wing (see *The cruisers*); no fighters (Q10 a; c would bring Act 5's units forward). The Vrell and the cruisers never fire at each other: the alliance shown, not told. **Okafor**: "All Aegis units, break for orbit. Don't look back." |
| ≈ 57–62 s | The scroll accelerates, the cruisers fall behind off the bottom edge and the wing makes orbit: a short **orbit section** (Europa's night side below, Jupiter ahead, no enemies) while the last line closes. The level ends at **K+62**: the debrief with the act summary, the [Act 4](../../../campaign/act-4-deep-water/README.md) outro (4 pages), then the release end `ACT V - THE BELT: COMING SOON` until M9. Vorne's broadcast opens Act 5 (the Level 29 briefing). |

**The run can't be lost** (draft, pending the user's review): its hits cost shield and armour as usual
(the repair bill in the hangar), but the armour stops at 1 until the level ends: the story needs the
wing to escape, and a retry from the start of a 130 s boss after a won fight would be harsh. The
alternative is a second checkpoint at the breakout (easy and medium; a new checkpoint kind). The
section after the kill is the level's outro for the pacing checks (no enemies by design; `PacingTest`
and `ActFloorTest` measure up to the boss's death; the build must say so).

#### The cruisers (draft)

The faction's first black-and-gold hulls, and the first insignia the player sees after Act 3's
ground-off plates.

| Field | Value (draft) |
|---|---|
| Name | Ascendancy cruiser (a class name, e.g. "Regent class", pending; the HUD shows no name and no bar) |
| Faction | Jovian Ascendancy (openly, for the first time) |
| Layer | `low-air` (below the play plane: no contact) |
| Size | ≈ 220×640 px each, longer than the field's height; one on each flank, the hull's inner edge at x ≈ 110 (left) and 370 (right), the rest beyond the field's edge |
| HP | none: invulnerable huge set pieces; shots spark off the armour, the Ion Beam stops on it; no bounty |
| Movement | rise through the ice (4 s), then keep pace with the wing for the run; at its end fall behind off the bottom edge |
| **Broadside** | a row of gun ports along the hull glows hot amber and amber tracer lines cross a horizontal band 70 px tall (the tell, **1.2 s**); then a volley of 7 shells crosses the band from the cruiser's side (`medium` = 6 each, 260 px/s; above water, no factor). One every 3.0 s, the flanks in turn, the band on the ship's y, then a random band; from 20 s two bands at once (one from each side) with at least one free band of 100 px between them |
| **Flak** | two markers (45 px, the boss `lob` rule) near the ship every 4 s: 1.0 s, then a `ring` of 8 (`small`, 120 px/s) at each |
| **Point defence** | each cruiser's turret line fires an aimed `burst` of 3 (`small`, 170 px/s) every 2.5 s |
| Difficulty hooks | hard: the broadside's tell 0.9 s, flak every 3 s, bursts of 4; easy: the tell 1.5 s, one flak marker, no double bands |
| Target | the autopilot at medium loses ≤ ≈ 15 shield and armour over the run (measured in part H) |
| Death | none: they stay behind, firing, as the wing makes orbit |

### Variant a (the roster's draft; Q9 a)

The Maw rises from below in a **jaw-snap**: a shadow grows under the ship (≈ 2 s), the jaws burst up
through the play plane and snap (`heavy`), then it sinks, trailing Eel Swarms, and fires bioluminescent
`fan` walls from the deep between snaps; the Helix implants on its body are seen during the snaps.
Rejected as the recommendation because it repeats the Dust Colossus's breach under the ship one act
later (its phase 3 marker), with a creature that is hidden most of the fight. If chosen, the Colossus's
breach machinery (`BreachArena`) carries it with the `sub` layer in place of `buried`.

### Presentation

- **Look brief (b, the coiled guardian)**: a serpent leviathan ≈ 1,750 px long, seen from above,
  coiled round the vent's caldera. Dark abyssal chitin plates over a deep-violet hide, shingled along
  the body like a moray's armour (the Vrell role colours: the base by its role family, the glow by the
  kind of threat); a **bioluminescent teal line** down each flank, so the coil reads in the dark as a
  glowing frame round the arena. The head: a long eel skull with an underslung jaw, a lime throat (the
  weak spot, like every Vrell weak point), two pairs of **gill sacs** behind the jaw, lime-teal and
  translucent, swelling as they breathe. The **Helix implants**: three grey, unmarked housings bolted
  through its plates, cables sunk into the flesh round them, the casings cracked and scorched
  (*wrecked*), serial plates ground off as on the Revenant Walker; shut they read as grey scabs, open
  as a **hot-amber** core. Nothing gold on the Maw (Q11 a: grey until the cruisers).
- **Look brief (a, rising from below)**: a broad jaw rising out of black water on a shadow, eel
  trails peeling off its sides, implants visible on its lower jaw; for the round's a/b.
- **Cruisers**: angular black hulls with **gold trim lines** and a thin gold and red rim light (the
  [Ascendancy colours](../../README.md#role-colours)), rows of hot-amber gun ports, the Ascendancy's
  gold emblem, and the **Helix hull code** of Level 27 stencilled openly in gold on the bow (the hint
  paid off); ice sheeting off them as they rise.
- **Readability**: the fan walls' orbs are bright teal over the dark (the bullet readability rules);
  the coil's glow line is the sweep's tell, so it must stay legible against the vent's light; the
  lunge's shadow is darker than the darkest backdrop.
- **Music**: the Vrell boss theme in its Act 4 setting (muffled, reverberant; [audio](../../../audio/README.md));
  silence under the dive's darkness except the ambience bed; the reveal sting at the cruisers' rise;
  the run under Vorne's motif.

### Sounds brief

Q12 a: heavy, big, real underwater recordings from Freesound (CC0 / CC-BY), **no bubbles and nothing
that pops**; no low-pass bus. Each row for the round's a/b by how it sounds.

| Sound | Where / when | Brief |
|---|---|---|
| Arrival roar | the coil rising, the head lifting | a vast, slow, subsonic groan (whale or ship-hull stress recordings, pitched down), long reverb tail |
| Gill breath | each sac's ring | a deep pressure thump with a soft exhale, like a heartbeat under water; never a bubble |
| Throat charge | the throat's glow before a fan or a wall | a rising low drone with a growl |
| Coil sweep | a sweep's tell, then the swing | a grinding groan of plates (ice or hull creak, slowed), then a heavy swish of displaced water |
| Eel shed | a plate flaring, an Eel Swarm slipping out | a wet tearing slide, short |
| Dive | the coil sliding into the dark | a long falling roar and a pressure swell; the music drops away |
| Lunge | the head racing up the column | a rushing surge and a jaw snap (heavy, no click) |
| Implant spark / burst | an implant's tell, its open core, its bolts | electrical crackle and arcing hum (real arc recordings), a hard zap per bolt; machine, not creature: the clue in the sound |
| Death | the implants' cracks, the unwinding, the vent's flare | three heavy electrical cracks, a long dying groan, a deep detonation-like pressure boom |
| Breakout | the ice ceiling shattering | ice cracking and tearing (glacier calving, lake ice), a huge crash and a water roar into open air |
| Cruiser rise | the cruisers through the ice | deep engine thrum, ice grinding off the hull |
| Broadside / flak / point defence | the run | big naval gun volleys (cannon recordings), airbursts, rapid autocannon |

## Concept art

Concept round 48 proposals (decided in round 48 (2026-10-22)), listed in the [bosses](../README.md#concept-art) Concept art table (generator `tools/concept/bosses_r48.py`): the Maw as a "Rising maw" (a) or the "Coiled guardian" (b, recommended), and the Ascendancy cruiser as a "Regent wedge" (a) or a "Forked monitor" (b). The cruiser mockups show the placeholder hull code `HX-427`; Level 27 sets `HLX-DS-0417`, which the production art uses.

## Implementation

Built in M8 part H with Level 28 (the roadmap's proposed parts); nothing here is built yet.

- [ ] Stat block and boss script in a `data.yaml` (the coil chain round the arena, the sacs, the implants
      as weak spots of the head's HP, the phases, the dive and lunge, the sweeps, the shed points, par),
      loaded; global difficulty multipliers applied
- [ ] The coil as a wall round the arena (the ship and the escort stopped, no damage; Level 25's trench
      walls' collision), and its sweeps (the bight, the tell, the swing, the push)
- [ ] The fan walls with a placed gap that chains with the previous wall's
- [ ] The dive (the layer `sub` below the play plane, the darkness's light drop) and the lunge column
- [ ] Phase 3's implants: weak spots on chain segments that count on another part's HP (new: the
      Colossus's gullet is a weak spot on its own part), their open cycle and bursts
- [ ] Eel Swarms shed from coil segments (the boss's count, counted into the enemies, not the density)
- [ ] The Bulwark and the autopilot: out of a sweep's bight and the lunge's column, through the walls'
      gaps, against the coil
- [ ] The fight measured over five seeds (the window 90–180 s, the HP tuned, the par set), phase 1
      checked with the calibration harness against the Kraken's, the bounty at ≈ 24 % with Level 28's
      measured scale
- [ ] Intro (the Choir sings, the coil rising), death (the implants, the unwinding, flash, credit
      shower), the Choir's fragment
- [ ] The breakout (the shaft climb, the medium switch to air, the ice ceiling) and the run (the
      cruisers as invulnerable `low-air` set pieces, broadsides, flak, point defence, the armour floor),
      then the act end and the release end
- [ ] Production art (≤ one atlas page for the Maw; the cruisers their own): the head's frames, the coil
      segments in 32 angles, the sacs, the implants shut / sparking / open / blown, the death, the intel
      silhouette; the cruisers rising and their gun ports

## Open questions

All decided in round 48 (2026-10-22):
- Q9: the coiled guardian (b, recommended) or the roster's rising maw (a); a/b concept art in round 48.
- Q10: the breakout and run (a, recommended), the outro stills only (b), or (a) with the first
  Ascendancy fighters (c).
- The run can't be lost (the armour floor at 1) or a breakout checkpoint.
- The Choir's fragment at the death ("…many… we were many here…"), or the Choir only sings (Q8).
- The phase 3 implants count on the head's bar (one bar for the fight) rather than being destroyed one by
  one (three small bars would split the HUD's single boss bar).

## Decisions

- 2026-09-30: Drafted in the bosses roster as the Act 4 boss (L28): rises from below in a jaw-snap,
  trails Eel Swarms, bioluminescent `fan` walls, wrecked Helix implants.
- 2026-10-21 (Act 4 design phase, step D-boss; decided in round 48 (2026-10-22)): promoted from the
  roster to a draft spec on the gap analysis's recommendations: Q9 b, the coiled guardian round the
  hatchery vent (phases Coiled, Tightening with one dive and lunge, Implants), the roster's version kept
  as *Variant a*; Q10 a, the breakout through the ice and a ≈ 35 s run under two invulnerable Ascendancy
  cruisers, specced here instead of a separate part (it is a set piece of this level, not a boss). HP
  6,700 + 4 × 150 from the proposed L28 reference DPS 120 (paper ≈ 131 s, par 130 until measured); the
  bounty 510 in Act 1 terms (24.0 % at an assumed `bounty_scale` of 0.50, under the perfect-run cap);
  the scroll halts (no crawl); the shed Eel Swarms and the boss's attacks give no edge warnings.
- 2026-10-21 (step m8-dact, **decided in round 48 (2026-10-22)**): reconciled with [Level 28](../../../campaign/act-4-deep-water/level-28-abyssal-maw/README.md): the bounty is **345** in Act 1 terms (head 297, sacs 12 each; 24 % at the level's hand `bounty_scale` ≈ 0.74), not 510 at an assumed 0.50; the Choir's fragment waits 3 s (K+3 to ≈ K+8.6); the level ends at K+62 with an orbit section; a new radio event `boss-dive`; the level's traits are `anti-sub`, `homing` (`side` named in Varga's L3 line).
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
