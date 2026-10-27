---
title: Mining tug
design: draft
implementation: not-started
art: proposed
depends-on: [../../enemies, ../../ui/hud, ../../world/belt, ../../campaign/act-5-the-belt/level-31-mining-rig-escort]
updated: 2026-10-27
---

# Mining tug

## Summary

The *Bellwether*, a belt mining tug carrying loyalist refugees out of Ceres: a `huge` friendly hull on the
`ground` layer, made of **three sections** (bow, cargo, engines) with their own HP, that holds a station in
the lower half of the play field while the level scrolls. Only attacks the
[target-the-objective hook](../../enemies/README.md#target-the-objective-hook) sends at it hurt it; a lost
section goes dark and the tug flies on; all three lost fails the mission. Escorted in
[Level 31](../../campaign/act-5-the-belt/level-31-mining-rig-escort/README.md).

## Design

**Draft** (Act 5 design phase, step `m9-d-levels-2`, 2026-10-27; the gap analysis's **Q4 a** and the L31
build gap check's **Q-D1 a** and **Q-D2 a**, pending the user's review in round 60). A hand-written table:
its numbers go into the allies' [data.yaml](../data.yaml) under the slug `mining-tug` once approved. The
level places it (station, sway, HP by difficulty, the pay per section, the hook per kind).

| Property | Value |
|---|---|
| Size / layer | footprint **160×260 px** on `ground` (Q-D1 = a; the Q4 draft's 200×300 covered 56 % of the 540 px field's height): a battered belt mining tug seen from above, nose up, **one heading** (it never turns; the sway is a translation) |
| Sections | three, along the hull, each with its own hit box, HP, damage states and smoke points (offsets from the tug's centre, y down): **bow** (bridge, crane and ore grapple) offset (0, −90), hit box 112×68; **cargo** (habitat modules strapped onto the ore frame: the refugees) offset (0, 0), hit box 148×92; **engines** (three drive bells, fuel tanks) offset (0, +90), hit box 136×68 |
| HP | per section; the level sets them by difficulty ([L31](../../campaign/act-5-the-belt/level-31-mining-rig-escort/README.md#the-tug-in-this-level): bow 240, cargo 160, engines 160 at medium); no shield, no regeneration, no repair |
| Damaged by | only **hooked** attacks, each on one section (Q-D2 = a): a hooked **homing missile** bursting in a section's box, **5**; a hooked **diver** crossing a section (the hook's `always`: the first section it crosses), **3** once per dive; a **Shard link** lit across a section, **4** per touch with a **0.5 s cooldown** per link and section (the [air links](../../enemies/README.md#air-links)' escort exception). Every other enemy shot, ring, dash and body passes over it (it lies below the play plane); shots aimed at the ship never hurt it |
| Targeting | a hooked attack picks the **nearest living section** (centre to centre) when it is launched or turns to chase; a missile whose section is lost in flight turns to the nearest living one |
| Behaviour | holds its **station** in screen space while the level scrolls beneath it (it crawls up the belt at the scroll's speed) and drifts round it on a slow **sway** on the level clock (x + 20 sin θ, y + 5 sin 2θ, θ = 2π t ÷ 14 s: a lazy figure-eight like Level 10's shuttles); never reacts to threats or to the player |
| Entry | rises from below the bottom edge into its station over the level's first seconds, **untouchable** (fire and contact pass through) |
| Climb-out | at the level's end its living engines flare and it climbs off the top edge, untouchable; a section alive at the climb-out's start is home |
| Damaged state | a section below 50 % trails smoke from its smoke points (in vacuum a thin venting plume that drifts back with the scroll, no rising column) and its lights flicker |
| Section lost | a short fire flare (the hull holds air), then the section goes **dark** (lights out, a scorched outline) and vents; **presentation only**: the tug keeps its station and its sway (its manoeuvring thrusters carry it on lost engines). Its HUD bar flashes red, then dark |
| Destroyed | when all three sections are lost the hull breaks at the cargo's joints and the pieces drift down and off the bottom edge over 4 s, darkening, without debris on the play plane (a presentation effect: the simulation removes it at once); the level's escort fails |
| Ship and fire | no contact: the ship and Rook fly over it; the player's and Rook's fire, bombs and the specials pass over it ([common rules](../README.md#common-rules)); drifting rocks pass over it |
| HUD | the two-line [objective tracker](../../ui/hud/README.md#act-5-additions-draft): `TUG n / 3` (the sections still alive) over three section bars in hull order, labelled `BOW`, `CARGO`, `ENGINES` where the 208 px well allows (else `B`, `C`, `E`; `MissionLayoutTest` decides); a white flash on a hit, amber below 50 %, a red flash then dark when lost, pale mint under a green glow when home |
| Radio | the level's `first-ally-hit` and `ally-lost` events per section; `{section}` is the section's name (`bow`, `cargo`, `engines`), a new placeholder beside `{ally}` |
| Sounds | a heavy hull impact per hit, a klaxon burst when a section falls below 50 % and when it is lost, the engines' low drive rumble as a bed while they live (the act's audio step; Freesound, CC0/CC-BY, no bubbly or popping sounds) |
| Levels | [L31 Mining Rig Escort](../../campaign/act-5-the-belt/level-31-mining-rig-escort/README.md) |

**Art brief** (for round 60's proposals). A Coalition-era civilian mining tug, older than anything else on
the screen: an ore frame of yellow and grey hazard-striped girders, the **bow** a blunt bridge with a crane
arm and an ore grapple folded back, the **cargo** a cluster of white habitat cans and two ore containers
lashed to the frame with cables (lit windows: people inside), the **engines** three big drive bells with a
blue-white glow and a fuel-tank ring. Belt grime, patched plates, a hand-painted name *BELLWETHER* on the
bow. It must read as friendly at a glance next to the gloss-black Ascendancy units (warm yellow and white,
cool running lights, no red), and the three sections must be visibly separate (seams, colour shifts), so a
dark section reads as lost. Rendered as the pre-rendered CGI look, lit from the top-left like the belt.

**Code fit** (from the L31 build gap check): one ally of N sections, each with an offset, a box, HP and its
own pay, holding one screen station with a sway on the ground layer, with the hook: a new hull escort beside
the existing convoy escort (not a new convoy mode); the HUD's shuttle bars relabelled.

## Concept art

Concept round 60 (Act 5 "The Belt" design phase, step ART-P-world; pending the user's review) — two builds of the
*Bellwether* at 160×260, each a PNG sheet (the tug at 1×, its sections and hit boxes, the damage states per section,
the tug beside Ascendancy units at play size, key frames) and a 9 s GIF in the play field (station and sway over the
rock field, hooked Hornet missiles, the cargo venting below 50 %, the bow lost, the `TUG n / 3` tracker). Generator
`tools/concept/belt_r60.py`; prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/mining-tug-r60-a.png](concept/mining-tug-r60-a.png) | Mining tug A, ore frame — yellow-grey hazard girders, a blunt bridge with a crane and grapple, white habitat cans and ochre ore boxes lashed on, three drive bells in a tank ring (sheet) | proposed |
| [concept/mining-tug-r60-a.gif](concept/mining-tug-r60-a.gif) | Mining tug A, ore frame — on station under hooked fire (motion) | proposed |
| [concept/mining-tug-r60-b.png](concept/mining-tug-r60-b.png) | Mining tug B, pressure hull — one enclosed older hull, the sections told by colour bands and hazard chevrons at the waists, window rows, side tanks, one big and two small bells (sheet) | proposed |
| [concept/mining-tug-r60-b.gif](concept/mining-tug-r60-b.gif) | Mining tug B, pressure hull — on station under hooked fire (motion) | proposed |

## Implementation

- [ ] Spec loaded from the allies' data (`mining-tug`: size, sections with offsets, boxes and smoke points,
      the damage per hooked attack kind, the sway) and placed by a level's escort block (station, period,
      HP by difficulty, pay per section, `targeted_by`)
- [ ] Station and sway on the level clock; entry and climb-out untouchable; one heading
- [ ] Damage per section from hooked missiles, dives and Shard links only, the nearest-section rule, the
      missile's retarget when its section is lost
- [ ] Section states (intact, smoking below 50 %, lost and dark) as presentation; the tug keeps station;
      all three lost fails the escort; the pay per section home
- [ ] HUD tracker `TUG n / 3` with three section bars (`MissionLayoutTest`); radio `{section}`
- [ ] Production art (three sections × intact, damaged, lost; engine glow; lights) and sounds

## Open questions

Pending the user's review in round 60:

- **Q4** (the escort's form): **a** this tug (drafted); **b** three ore haulers as an L04/L11 convoy; **c** the
  tug on the play plane, not to be shot through.
- **Q-D1** (its size): **a** 160×260 centred ≈ 150 px above the bottom (drafted); **b** 200×300; **c** 120×200.
- **Q-D2** (what hurts it): **a** hooked attacks only (drafted); **b** every enemy bullet crossing its box;
  **c** only missiles.
- Its name (*Bellwether*) is a placeholder for the story step.

## Decisions

- 2026-10-27 (Act 5 design phase, step `m9-d-levels-2`): drafted for Level 31 from the gap analysis's Q4 a
  and the L31 build gap check's Q-D1 a and Q-D2 a (pending round 60); a lost section is presentation only
  (the tug keeps station), all three lost fails the mission, each section home pays; the damage per hooked
  kind (missile 5, dive 3, link 4 a touch) is set here, the HP by difficulty in the level.
