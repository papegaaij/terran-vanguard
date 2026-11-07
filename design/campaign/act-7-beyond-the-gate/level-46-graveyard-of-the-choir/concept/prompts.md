# Level 46 – Graveyard of the Choir: concept prompts

## backdrop-final-r91-a

**The dead-fleet backdrop, production** (round 91, M11 part D, step ART-D, 2026-11-06; production art, pending the
user's review). Files: `backdrop-final-r91-a.png` (sheet: eight screens at half size with stand-ins, the pieces) and
`.gif` (the level in a time-lapse, one frame every 4 s); the `backdrop:` block in `backdrop-proposal.yaml`. Generator:
`python3 tools/art/act7_deadfleet.py fleet` (`assets/backdrop/level-46/`).

- **Prompt:** late-1990s pre-rendered CGI game backdrop, straight top-down, one cold white-violet key light from the
  top left. Deep: near-black violet space (#06050a), the faintest dust in the palette's darkest violet (#181122),
  sparse white-violet stars, small dark far hulls with a cold rim on their top-left edges; a withered grey-violet
  world (#463e54 lit), small and far in section 1, filling the lower sky in section 5, its pale cracked surface, no
  atmosphere. Low-air: hollowed Vrell hulks in dull grey-brown plates (#8f8273), punched open from the inside, the
  plates round each hole bent outward, pale crystal (#b8c8d4) faint along the cracks, no light inside; a long
  spindle, a broad hull, a hull broken in two (fore and aft halves with ragged torn ends); paler and flatter than the
  Husks. The Helix survey craft (grey-white hull, a gold band) wedged in a hulk's breach, its lights dead.
- **The warm hull:** a dead Vrell capital hull (≈ 300 px), not hollowed but scarred by weapons (scorched craters),
  its rib cage open amidships, its prow to the top left; in the cage a living core (48 px, a violet membrane over
  glowing gold veins) held by four sinews to the ribs; the core's warm violet-gold light on the ribs, pulsing slowly:
  the only warm light in the level.
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, text,
  watermark, motion blur, dithered gradients, lens flare, bubbles
- **Animation brief:** very low motion: the hulks rock slowly (±7°, 6.4 s forth and back); no glow pulses but the warm
  core's (4 s); the starless patch is drawn by the game (a soft disc where no star shows, never moving). Every glow a
  smooth gradient of at least 32 steps that fades to 0 inside its canvas, no ordered dither.

## l46-props-final-r91-a

**The seed casket, the warm core and its sinews, the Helix hatch and Vorne's courier, production** (round 91, step
ART-D). Files: `l46-props-final-r91-a.png` (sheet) and `.gif` (the warm hull coming in, its four sinews cut one by
one, the core lifting out and drifting loose). Generator: `python3 tools/art/act7_deadfleet.py casket warm wrecks`
(held in `build/art-held/sprites/` until step D2's data claims them).

- **Seed casket** (40×28): a chitin capsule in the dead hulls' dull grey-brown plates, end caps, its long seam a faint
  teal line (#46aaa0); broken: split open along the seam, the hollow dark and empty; its break: the halves drifting
  apart and shrinking, chips flung out, a faint teal breath out of the seam.
- **Living core** (48 px; 128×104 with its sinews): a violet membrane (#4c2666) over glowing gold veins (#ffc460),
  four dark red-violet sinews to knots on the ribs, breathing once in 4 s; cut, it lifts out over 2 s, growing to 1.2,
  the cut sinew ends hanging, in a violet-gold halo; loose, it drifts breathing once a second.
- **Sinew knot** (28×28): a fleshy knot gripping a grey rib: whole; hit (a gash, gold ichor); cut (a severed stump);
  torn through: tissue shreds and gold flecks flung out.
- **Helix hatch** (24×24): the survey craft's grey hatch with the gold Helix hex: shut, dented, blown open; blown out:
  panel chips and gold flakes, a brief hot core.
- **Vorne's courier** (40×40): a small gold Ascendancy courier drone, gloss gold with black panel lines and short
  fins, its drive nozzle scorched black and dead, tumbling slowly (16 turns, rocking); shot apart: gold fragments, a
  brief hot core, no smoke (vacuum).
- **Negative prompt:** as above.

## game-captures-r91-a

Concept round 91, captures of the game, not generated art (M11 part D, step CD5, 2026-11-06): the game built by
`:desktop:installDist` (branch `m11-act-7`, parts C and D in the tree) on a private 1920×1080 Xvfb display, sound off
(the audio environment stripped, OpenAL's null driver), `--bench`, a scratch `--settings`, `--level 46 --invulnerable`
and `--shots <tick>,…` writing the 960×540 screens every 3 ticks (20 a second); the GIFs take every second one (10 a
second) and cut out the play field (x 240–720), the HUD's warnings inside it. Fit: the balance plan's late-act weapons
(Ion Beam L5 front, Tesla Coil Pod L5 left, Micro-missile Pod L5 right, Swarm Tail L4 rear) and Rook with the Autocannon
L5 (the plan's L46 pick). The quiet and the cue fly with auto-fire off and no fire at all, so nothing wakes a Husk
before the cue; the core clip holds fire (XTest key presses timed on the written shots) from the start until the four
sinews are cut, steering the ship under the left pair, then the right pair, then under the core, and up into it. The
1:1 crops are cut from the same flights by eye. Laid out by a scratch script (not kept).

## quiet-capture-r91-a

Concept round 91, a capture of the game (step CD5): the quiet (t=127–135), the dying Leviathan crossing overhead, the
quiet's Husks drifting harmless among the torn hulks, the starless patch (x≈360, 150 px down) in the deep layer. The
[CD5 captures](#game-captures-r91-a).

## cracks-capture-r91-a

Concept round 91, a capture of the game (step CD5): the end of the quiet (t=143.5–149): the cue at t=144 cracks the
Husks still on the screen in turn, each fanning its shards, under the remnants' edge warnings. The
[CD5 captures](#game-captures-r91-a).

## core-taken-capture-r91-a

Concept round 91, a capture of the game (step CD5): the warm hull (t=185.5–191): its four sinews cut by the beam, the
core freed and flown through, `VRELL CORE SALVAGED` on the event line. The [CD5 captures](#game-captures-r91-a).

## husk-vs-hulk-capture-r91-a

Concept round 91, a capture of the game (step CD5): three 240 px crops at 1:1: Husks beside a long scenery hulk in the
quiet (t=120.5), a cracked Husk's shards over the pale scenery hulk (t=145.5), a Husk's death-ring orbs and a cracked
Husk over the warm hull (t=186.6). The [CD5 captures](#game-captures-r91-a).

## cracks-capture-r92-a

Concept round 92 (round 91's C.7 follow-up), a capture of the game (step EF5, 2026-11-07), made exactly as the
[CD5 captures](#game-captures-r91-a) (`--shots` every 3 ticks, auto-fire off and no fire, `--invulnerable`, the same fit
with Rook's Autocannon L5): the end of the quiet (t=143–149) after step D6's retime (the quiet's Husks enter at t=132 and
drift slower): the cue at t=144 cracks all five Husks still on the screen in turn, each fanning its shards.
