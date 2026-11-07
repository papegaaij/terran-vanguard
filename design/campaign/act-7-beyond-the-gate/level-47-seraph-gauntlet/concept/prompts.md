# Level 47 – Seraph Gauntlet: concept prompts

## backdrop-final-r92-a

**The inner-sphere backdrop, production** (round 92, M11 part E, step ART-E, 2026-11-06; production art, pending the
user's review). Files: `backdrop-final-r92-a.png` (sheet: eight screens at half size with stand-ins, the pieces) and
`.gif` (the level in a time-lapse, one frame every 3 s); the `backdrop:` block in `backdrop-proposal.yaml`. Generator:
`python3 tools/art/act7_sphere.py sphere` (`assets/backdrop/level-47/`).

- **Prompt:** late-1990s pre-rendered CGI game backdrop, straight top-down, lit gold-white (#F2DC9A) from the core ahead
  (the top of the screen) and a little from the left. Deep: the curved inside of a shell of Vrell stone far below, in
  the one Vrell palette's violets (#200f30 to #705468, a teal-black #0e282e in the grooves): broad rounded ribs arching
  across the field, unevenly spaced, their bows and tilts rocking slowly along the shell, their upper flanks catching
  the gold-white light; a fine broken grain on the stone; in the Choir hall the ribs bending into the vault's warped
  rings round a domed boss with six curved spokes; the light richer section by section, warmed toward the core. Before
  the shell (section 1): near-black violet space, faint dust, sparse dim stars and the dead fleet's last hulls, small
  and dark, then the shell's ragged rim. Far: the great ribs, arched bands of the same stone crossing the screen with a
  soft shadow under them, kept a step under the Seraph's ivory (the gold only a thin edge); the dead fleet's last hulls
  (Level 46's, small and warmed). Low-air: the Heralds' rings of light as still, tilted soft gold rings; faint warm
  motes. At the end the core's light (a smooth gold-white glow) fills the top half and Level 48's citadel stands as a
  dark teal-black mass of knotted towers, rimmed gold, at its far side.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, text,
  watermark, motion blur, dithered gradients, lens flare, pale bone, straight lines, evenly spaced stripes, bubbles
- **Animation brief:** low motion: the shell passes slowly (0.15), the great ribs at half speed, the rings and motes
  drifting a few px/s, nothing pulsing; the core's light grows over the level by its curve (E4, screen space). Every
  glow a smooth gradient of at least 32 steps that fades to 0 inside its canvas, no ordered dither; no straight seam or
  ruler-straight line in any tile or piece (the generator's row/column scan).

## l47-props-final-r92-a

**The memory pearl, production** (round 92, step ART-E). Files: `l47-props-final-r92-a.png` (sheet) and `.gif` (the
pearl drifting down the right third, flickering, shot apart). Generator: `python3 tools/art/act7_sphere.py pearl`
(`assets/sprites/debris-memory-pearl*`, step E2's debris chunk `memory-pearl`).

- **Prompt:** late-1990s pre-rendered CGI game sprite, straight top-down, a 36 px shard of the shell's violet stone,
  ragged and ribbed, its upper edges lit gold-white, a dark outline round it; in a socket at its centre a milky pearl
  (#e2dcec) with the Moon faint inside it (a pale disc lit from the right, two grey maria).
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, text, watermark, dithered
  gradients, bubbles
- **Animation brief:** the shard tumbles slowly (16 frames a turn, the light fixed); the remembered image flickers
  faintly (8 additive frames, a 6 fps loop, pale silver-violet, never in a bullet's violet); shot, it breaks into nacre
  and stone chips and its light lets go in a pale ring widening and fading (8 frames each). Every glow a smooth
  gradient of at least 32 steps that fades to 0 inside its canvas.

## game-captures-r92-a

Concept round 92, captures of the game, not generated art (M11 part E, step EF5, 2026-11-07): the game built by
`:desktop:installDist` (branch `m11-act-7`, parts E and F in the tree, the full `check` green), on a private Xvfb
display, `--bench`, a scratch `--settings`, nothing played aloud (the audio environment stripped; OpenAL Soft's `wave`
driver writing the mix to a file, checked in `/proc/<pid>/environ`).

1. **The real-time run** (pair kill, core light, the 1:1 crops): `tools/concept/replay/record_replay.sh 47 medium l47.rec
   --loadout front=ion-beam:5,left=tesla-coil-pod:5,right=micro-missile-pod:5,rear=swarm-tail:4,generator=6,shield=2,armor=5 --special vrell-swarm-call --escort rook:autocannon:1` (the balance plan's L47 fit as the game's level-start options: the Mk VI, Shield Mk III, Composite
   V, the Swarm Call, Rook's Autocannon L1): 15,960 steps (266.0 s), won with 160 armour, 757 kills, hash
   `58da70ddf805e876`; then the start script with the same options plus `--level 47 --difficulty medium --replay
   l47.rec` on a 960×540 Xvfb display, a 960×540 window at 0,0, `ffmpeg -f x11grab -framerate 60` (near-lossless); the
   game's state hash at the recording's end equal to the recorder's. Level t = 0 at the log's `[replay] the level's
   first step` line; the sound from the wav file's appearance. Clips cut as H.264 CRF 21 at 60 fps with AAC 160 kb/s
   at −3 dB, the play field (x 240–720) cut out. In this run the autopilot (the beam held on the centre line) kills
   every Seraph and each Herald before its pair lands (`SERAPHS DONE` at ≈ t=220), so no Seraph leaves and no pair
   flies under a living Herald.
2. **The Herald pair** (`--shots`, one simulation step a frame, a screen every 3 ticks = 20 a second, the clip at real
   speed, silent): the same fit without Rook (`--escort none`) and with auto-fire off and no fire at all, `--invulnerable`,
   ticks 8070–9180: the pair under the t=134 Herald holds its whole four cycles and leaves.
3. **The leaving tell** (`--shots` as 2, auto-fire on): the Ion Beam L5 alone, no Rook, `--invulnerable`, the ship held
   at the left edge by a key press (XTest), so the first Seraph (t=40) lands mirrored on the far right and leaves alive
   (the secondary still open): ticks 2940–3540, the whole screen with the HUD.
Laid out by scratch scripts (not kept).

## pair-kill-capture-r92-a

Concept round 92, a capture of the game (step EF5, [how](#game-captures-r92-a), the real-time run, with sound): the first
pair (t=60.5–67): the two violet rings, the pair landing on the ship's far side and mirroring it, the ship crossing the
middle and the beam killing the lower Seraph, then the other.

## herald-pair-capture-r92-a

Concept round 92, a capture of the game (step EF5, [how](#game-captures-r92-a), the Herald run, silent): t=136.5–152: a
Herald at its station over the pair, its ring on the upper Seraph (and on the small units that pass under it), four
cycles, the pair's leaving tell and their jump out.

## leaving-capture-r92-a

Concept round 92, a capture of the game (step EF5, [how](#game-captures-r92-a), the leaving run, silent, the whole
screen): t=51–57: the first Seraph's last fold, its wings flaring white as it rises 40 px, the jump out; the
secondary's well (`SERAPHS 0 / 14`, bottom left) blinks while the tell shows.

## core-light-capture-r92-a

Concept round 92, a capture of the game (step EF5, [how](#game-captures-r92-a), the real-time run, with sound): the
level's end (t=248–265.5): the ribs lit, the core's light in the upper field, the citadel's dark mass coming in over it.

## seraph-vs-ribs-capture-r92-a

Concept round 92, a capture of the game (step EF5, [how](#game-captures-r92-a), the real-time run): three 240 px crops at
1:1: the first pair over a rib (t=63.4), a pair over the vault's lit edge (t=189.3), the last pair over the brightest
ribs (t=239.4), their violet shots and the beam beside them.
