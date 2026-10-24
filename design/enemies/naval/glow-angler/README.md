---
title: Glow Angler
design: approved
implementation: in-progress
art: final
depends-on: [../../../systems/difficulty, ../../../systems/economy]
updated: 2026-10-24
---

# Glow Angler

## Summary

A small Vrell ambusher of the black water: it hangs a teal lure on a stalk that glows like something worth picking up and, when the ship comes close or touches the lure, lunges and bursts a `ring` of shots.

## Design

### Stat block

Approved in round 48 (2026-10-22). Numbers at **medium** (see the [balancing basis](../../README.md#balancing-basis)), from its [data.yaml](data.yaml) (M8 part F, step F2; the table is rendered from it); HP is in damage units (a Pulse Cannon L1 shot does 2), set at the unit's first level from the Act 4 reference DPS (L26 112): the beam-era factor × 2 applies by level number, 28 at L26 (0.25 s). Bullet speeds are base values: the level's water factor (bullets × 0.7, both sides, see the [under water rules](../../../world/europa/README.md#under-water-rules)) applies on top. The play plane under water is the `air` layer. The data: the `lunge` movement (flown by the simulation's `Lunging`, step F1) beside its `drift` with the water, and its one `ring` attack, the lunge's, not a gun (its `interval` the re-arm after the ring; gap check `m8f-gaps.md` §5 F2).

<!-- data: stat-block -->
| Field | Value |
|---|---|
| Faction | Vrell |
| Layer | `air` (the play plane under water) |
| Size tier | `small` |
| Size | 40×32 px, hitbox 30×24 (the body; a stalk of 36 px and a 12 px lure) |
| Parts | single (the lure is drawn, not a part) |
| Orientation | `16 angles` (toward the ship during a lunge) |
| HP | 14 (easy 10 / hard 18, from the global multipliers) |
| Armour / shield | none |
| Speed | 20 px/s (its drift); lunge 220 px/s |
| Movement | `drift` hanging in the dark with the water; when the ship is within 130 px (the lure lies inside that, so touching it counts), it `dive`s at where the ship was for at most 0.6 s, stopping 60 px short, fires and retreats at 100 px/s for 1.5 s |
| Attack | one `ring` of 8 (`small` = 4, 100 px/s base) at the end of the lunge; it re-arms 5 s after the ring; the lure itself does no damage |
| Formations | field (4–8) (scattered over a dark area; a few sparse fields of singletons) |
| Weak points | the lure and the glowing eye (drawn only) |
| Effective traits | `spread`, `homing` |
| Credits | 14 (score 140 × chain) |
| Death | `small` flash that blows out the lure; the stalk stays lit for 1 s then goes dark |
| First level / used in | L26; L27 (the vent glow makes them a lighter threat) |
| Difficulty hooks | easy: trigger 100 px, ring of 6; hard: trigger 160 px, ring of 10 |
<!-- /data -->

### Behaviour

- **The lure** (Q6 a): a near-miss pickup look: a pulsing teal bead on a visible stalk (a pickup's own colour is never used), drawn in the black as its only visible part; Varga warns at the first sighting ("*that is not a pickup*"); touching the lure triggers the lunge but does no damage.
- **Lunge**: the dive ends 60 px short of the ship's position when it fires, so the ring spreads from a point the ship can leave; contact `small` = 10 only if the ship flies into the body.
- **In the dark** (L26): outside the headlight cone units show only their glow: the lure and the eye are lit; shots are always bright; everything is hittable (round 48 Q4 a). The Sonar Pulse lights every unit and lure for 6 s.
- **Under water**: the ring's bullets carry the x 0.7.

### Difficulty and level use

- L26 is the Angler's level, a field scattered over the black; L27's vent glow makes them a lighter threat.
- Beam-era HP by level number (`small`).

## Concept art

Concept round 48 proposals (a/b, decided in round 48 (2026-10-22)): the Angler with its lure in the dark. Files `glow-angler-r48-a/b` in [naval/concept](../concept/), listed in the [naval](../README.md#concept-art) Concept art table. Notes: b looks rocket-like.

## Implementation

- [x] Stat block loaded from a `data.yaml` (step F2); global difficulty multipliers applied; HP re-measured with `levelReport` (the beam's halved reach and the 0.7 non-`anti-sub` damage change its time on the screen: Level 26's small units 2.20 s on the screen at medium with Level 26's own plan fit (step F3), 1.37× Level 10's, inside 0.9–1.5)
- [x] Behaviour, attack and tells per this spec, deterministic and allocation-free (step F1, `Lunging`; `GlowAnglerTest`, `Level26Test.theGlowAnglerLungesFromItsLure`)
- [x] Production sprites straight to production as a/b in the part's round (`tools/art/`), the in-water grade and the intel portrait (`tools/art/glow_angler.py`, `tools/art/intel.py`; `Level26LooksTest.theArtIsWhatTheLooksDraw`, `theAnglersStepsAndStalk`; round 55)
- [x] Death effect, bounty and sounds (Freesound, no bubble pops) per this spec (`AnglerLooks`' blown-out lure and the stalk lit 1 s, `Level26LooksTest.anAnglerLungesAndRings`; `Level26Test.aPerfectRunAtMediumEarnsTheReadmesTotal`; `Level26SoundsTest.theLureHumRisesAsTheShipComesClose`, `theLuresHumTheCrownsSingAndRooksLossIsHeard`; round 55's a/b)
- [x] The lure sprite, the near-miss trigger (130 px or touch) and the dark's glow-only render (`GlowAnglerTest.itHangsDriftingWithTheWaterUntilTheShipIsWithin130`, `easyAndHardChangeItsTriggerAndItsRing`; `Level26LooksTest.theUnitsRimGlowAndSonarPools`)

## Decisions

- - 2026-10-21 (step m8-denemies): drafted for the Act 4 design phase from the roster row and the gap analysis; every number is a first draft decided in round 48 (2026-10-22).
- - 2026-10-21 (recommended option, decided in round 48 (2026-10-22), Q6 a): a near-miss lure, teal with a visible stalk; rejected: b (looks exactly like a pickup) and c (no mimicry).
- 2026-10-22 (user, round 48 D1–D13: "approve"; confirmed: "Yes, set them approved"): design approved.
- 2026-10-23 (step F2, gap check `m8f-gaps.md` §5 F2, accepted in round 55 (2026-10-24)): the stat block as data; the
  trigger is one distance test from the body (130 px, easy 100, hard 160): the lure, at most 48 px from it, lies inside
  it, so touching the lure needs no test of its own; the dive aims at the ship's position at the trigger and stops 60 px
  short or after 0.6 s; the retreat lasts 1.5 s; the ring re-arms 5 s after it (the fire-rate lever applies, as to every
  attack's interval). The open question closes: the first sight stays a timed line (Level 26's Varga at t=24.5), no event
  of its own.
- 2026-10-23 (step F3): flown in Level 26 on every difficulty (`Level26Test`: it lunges and rings in a flight, never
  ringing more often than it lunged; its re-arm the 5 s by the fire-rate lever: easy 7.1 s, hard 3.8 s); the test
  autopilot keeps out of a drifting Angler's trigger where it can and clear of a diving one's ring (`DarkPlay`).
- 2026-10-23 (steps F1, F4 and ART-F, accepted in round 55 (2026-10-24)): the ring waits for room while the ship is
  within 72 px of the Angler (no ring point-blank), and one that never finds room by the retreat's end is dropped; the
  lunge ignores the scroll (it dives in the water, not along the ground); the sprite is an 80 px frame with the body
  centred (the sprite's centre is the hitbox's), drawn graded (the 15 % rim lights it in the dark) with the bead, eyes
  and stalk over the grade; the stalk lights up within 160 px of the ship on every difficulty, and in a light pool, the
  cone or the Sonar Pulse.
- 2026-10-24 (user, round 55 closed, F.3): the Glow Angler's final art accepted (`glow-angler-final-r55-a`); its lure
  hum **a** and lunge **a** picked.
