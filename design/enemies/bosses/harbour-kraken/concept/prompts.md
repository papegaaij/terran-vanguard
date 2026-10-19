# Harbour Kraken – generator notes and prompts

The chosen concept is [harbour-kraken-r07-a](../../concept/prompts.md#harbour-kraken-r07-a) (round 07,
the redo from the depths, in the bosses' `concept/`); its prompt stays valid for the look.

## harbour-kraken-final-r33-a

Round 33, production art (M5 part E batch, user decisions E9 = a: straight to production from the
chosen concept; E7 = a: the slam arms as runtime chains, the gripping arms as an overlay on the
platform, the idle arms as `sub` shadows, the head pre-rendered; E4 = c: water as the hybrid).
Review sheet (`.png`) and loop (`.gif`) made from the files in `assets/` by
`python3 tools/art/harbour_kraken.py --review`; the frames are rendered by
`python3 tools/art/harbour_kraken.py` (see `tools/art/README.md`). The review's sea, its Platform
Tiamat (the concept's platform model) and the game's `sub` pass are stand-ins; the fight follows
the concept's choreography (`tools/concept/kraken_r07.py`: slam, surfacing, eyes, fans, dive).
Registration: every position in `assets/pivots/harbour-kraken.json` is relative to the platform's
centre (the deck 208 × 152 px as in round 07); the head's centre is 152 px below it.

- `harbour-kraken-sub` (160×276): the plain head and mantle, eyes shut, from the round-06 model
  (`tools/concept/render/boss_models.kraken_mantle`, turned arms-down, 88 px per unit as round 07);
  drawn through the `sub` pass whenever any of it is under water.
- `harbour-kraken_0..4` (160×276, 48 colours shared with the whole unit): the surfaced head above
  the water — eyes shut, half open (a lid over the open eye), open, open with the beak's crimson
  glow at half and full (the 0.5 s before a fan).
- `harbour-kraken-collar_0..3` (160×276, stepped translucency): the broken foam collar round the
  surfaced head, a loop.
- `harbour-kraken-surface_0..11` (160×276, 10 steps a frame = 2 s): the surfacing's surface layer
  along the concept's height-map cut — a lit swell with the first foam, the crown breaking first,
  then the eyes and the rest, the collar growing, a wet sheen and runnels on what just came up;
  played backward to dive.
- `harbour-kraken-arm_0..223` and `harbour-kraken-tip_0..63`: the slam arms' segments
  (`boss_models.kraken_arm`, 7 tapers 42–14 px) and tip curls (`kraken_tip`, 34 and 22 px) at 32
  headings, for the runtime chains.
- `harbour-kraken-foam_0..15` (24×24): foam clumps for where an arm breaks the surface or lies awash.
- `harbour-kraken-grip_0..3` (256×156) and `harbour-kraken-grip-sub_0..3` (344×304): the four
  gripping arms on the deck and over its west, east and south edges (foam at the crossings, their
  shadows on the deck), and their under-water stretches to the head's roots for the `sub` pass;
  composited from the segment frames along round 07's arm paths, a 4-frame sway.
- `harbour-kraken-idle-left/-right_0..3` (100×120, drawn at 2×) and `harbour-kraken-shadow_0..1`
  (102×235, drawn at 2×): deep, unlit shadows — the idle arms squirming toward the lower corners, and
  the whole animal swimming crown first for the level's foreshadowing.
- `harbour-kraken-sink_0..7` (12 steps a frame), `harbour-kraken-grip-slide_0..4` (12 steps) and
  `harbour-kraken-death_0..9` (160×160, additive): the death — the head going down dead-eyed in a
  churning foam ring, the grip sliding off the deck, a lime and crimson burst with spray glints.
- `harbour-kraken-arm-death_0..7` (48×48, additive): a severed slam arm's segment pop.

## harbour-kraken-r40-a

Round 40, proposal a for the Level 11 rework (playtest 2026-10-18, `pt2-b.md` proposal 3a: four
one-lane arms rooted in sockets the player can cut, the whole animal moved up the screen). Frames by
`python3 tools/art/harbour_kraken.py --r40`, the review sheet and loop by `--r40 --review` (the
chosen round-33 set is not touched: the new sprites carry `-r40-a` in their names). The round-33
registration is kept: the head stays 216 px below the platform's centre, so the head, grip,
surfacing, death and segment frames are reused unchanged, and the platform moves up instead (its
centre at the halt y −46 instead of 78; `tools/art/backdrop_l11.py`): the head's centre is at y
170, the eyes at 222, the beak at 268. Registration in `assets/pivots/harbour-kraken.json` under
`r40_a`. The review's sea, platform, `sub` pass, ships and lane marks are stand-ins.

- `harbour-kraken-root-r40-a_0..15` (72×96, indexed (lane − 1) × 4 + frame): a root socket where an
  arm breaks the surface, centred on the 40×40 root target (screen x 60 / 154 / 326 / 420, y 210;
  lanes 2 and 3 sit 26 px outward of their lane centres to clear the mantle and the eyes): a dark
  webbed collar ringed with pale suckers, the mottled arm rising out of it and running on down its
  lane, suckers along its flanks and the crimson dorsal seam on its back; a 4-frame breathing loop.
  The same SDF materials as the arms (`boss_models.mats("harbour-kraken")`, mottled), under a mild
  perspective so the rearing reads.
- `harbour-kraken-root-hit-r40-a_0..3`: frame 0 at the hit flash (0.3 toward the pale flesh tint).
- `harbour-kraken-root-raised-r40-a_0..23` (6 per lane): the tell — the arm rearing out of its
  collar (frames 0–3, once), then holding (4–5, a loop), the seam an emitter growing to full
  crimson, a soft shadow on the water; with `harbour-kraken-root-glow-r40-a_0..3` (64×64, additive,
  a crimson halo pulsing on the socket).
- `harbour-kraken-root-cut-r40-a_0..23` (6 per lane): the severed stump recoiling (0–1, once, with
  the round-33 segment pops along the lost arm), then a 4-frame loop of a dark crimson ichor slick
  spreading from it, glints and beading drops.
- `harbour-kraken-root-sink-r40-a_0..19` (5 per lane): the death, the knot going under in churning
  foam.
- `harbour-kraken-reach-r40-a_0..3` (388×132, plain, for the `sub` pass): the four arms' under-water
  stretches from the mantle's root ring to their sockets (the round-33 segment frames along the
  paths), a 4-frame sway.

Brief (for an artist): the Harbour Kraken's four lane arms, each anchored in a fleshy socket at the
surface — a ring of dark webbing studded with pale suckers, the thick rust-mottled arm rising out of
it with a crimson seam along its back; when it is about to strike it rears up and the seam blazes
crimson; once cut, a short stump weeps a dark crimson slick. Pre-rendered 90s CGI, top-down, key
light from the top left, the round-33 Kraken's palette.
