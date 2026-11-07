# Level 48 – Vorne's End: concept prompts

## backdrop-final-r93-a

**The citadel backdrop, production** (round 93, M11 part F, step ART-F, 2026-11-06; production art, pending the
user's review). Files: `backdrop-final-r93-a.png` (sheet: twelve screens at half size with stand-ins, the tiles and
the heart) and `.gif` (the level in a time-lapse, one frame every 3 s); the `backdrop:` block in
`backdrop-proposal.yaml`. Generator: `python3 tools/art/act7_citadel.py citadel` (`assets/backdrop/level-48/`).

- **Prompt:** late-1990s pre-rendered CGI game backdrop, straight top-down, a soft violet key light from the top left
  (Vrell space: no sun). Ground: a citadel grown by the Vrell over the gold wrecks the Ascendancy brought through the
  gate: ridged teal-black growth (#102c32 lit) in branching ropes over lobed swellings, buried dull gold hull plating
  (#96805a lit, a step paler and less saturated than the boss's gold) showing where the growth thins, its plates
  buckled, their seams bent; thin teal veins (#38a091) meandering up the screen. Section 1 the outer growth (mostly
  tissue, gold in patches, side chasms); section 2 decks laid over whole gold hulls (gold-heavy); section 3 a broad
  causeway (x 75-405) with the sides falling away; the heart: dense growth whose veins converge on the spire well, a
  raised ring round a tissue lid of six lobes veined with gold light that splits from the centre on a gold-lit
  throat; section 7 the spire's inner wall, rings of tissue arching over a dead gold mast down the middle, torn
  windows at the sides. Deep: the void below in the palette's darkest violet with a faint nebula, the citadel's
  towers far below (lobed growth round gold wreckage, a teal rim on the top-left edge), the spire a vast tower round
  a gold mast, a pale violet-white shimmer (Level 49's memory) rising at the top by t≈235.
- **No straight lines:** every seam, edge, rib and ring curved or ragged (the user's rule; a row/column scan finds no
  run of 120 px).
- **Negative prompt:** photograph, painterly brush strokes, soft focus, perspective camera, side view, ruler-straight
  lines, regular grids, text, watermark, ordered dither, clipped glow.

## l48-props-final-r93-a

**The reactor cores, their veins, Vorne's archive, the hulls, the spire well** (round 93, step ART-F; pending the
user's review). Files: `l48-props-final-r93-a.png` (sheet) and `.gif`. Generator: `python3 tools/art/act7_citadel.py
props citadel` (sprites held in `build/art-held/sprites/` until step F2's data claims `reactor-core` and
`vorne-archive`; the pieces in `assets/backdrop/level-48/`).

- **Prompt:** a round gold reactor housing seen from straight above (40 px), dulled and cracked, four teal-black
  tissue clamps grown over its rim, its core glowing gold-white (#ffecb2) in the throat with a soft halo; cracked (the
  rim split, the core dimmer); dead (the core gone, a dark hole, the rim burst open). Destroyed in vacuum: a short
  gold-white flash, a ring of gold debris light spreading and fading, chips of housing and tissue flung out. Its
  vein: a raised channel of growth from the core up the screen toward the spire, meandering, a steady teal glow with
  one brighter pulse running up it every 1.5 s, going dark from the core outward over 1 s when it dies (smooth,
  never bullet-sized dots). Vorne's archive (40 px): an Ascendancy data vault, gold and sealed, two raised bands, half
  overgrown by teal-black growth; cracked; broken open (the lid gone, the hollow dark). The open gold hulls (≈ 110x150):
  a wreck's dull gold hull half buried in the decks, its plating split over an empty round hold, growth lapping over
  its sides.
- **Negative prompt:** photograph, cartoon, flat vector, text, watermark, ordered dither, clipped glow, a hard flash.

## game-captures-r93-a

Concept round 93, captures of the game, not generated art (M11 part F, step EF5, 2026-11-07): the game built by
`:desktop:installDist` (branch `m11-act-7`, parts E and F in the tree, the full `check` green), one real-time run with
sound, made as Level 47's ([how](../../level-47-seraph-gauntlet/concept/prompts.md#game-captures-r92-a), its step 1):
`tools/concept/replay/record_replay.sh 48 medium l48.rec --loadout front=ion-beam:5,left=tesla-coil-pod:5,right=micro-missile-pod:5,rear=swarm-tail:5,generator=6,shield=2,armor=5 --special vrell-swarm-call --escort rook:mortar:1` (the balance plan's L48 fit: the Swarm Tail
L5, Rook's Mortar L1): 19,368 steps (322.8 s), won with 145 armour, 706 kills, hash `5a387d0820d12d60`; the recorder's
fight: the arrival at t=186.0 (real seconds from the level's first step), in place at 190.0, phase 2 at 207.5, phase 3 at
239.7, the burst at 258.3. The game flew it with `--replay l48.rec` on a private 960×540 Xvfb display (`--bench 300`:
the run stops 23 s before the level's end, so the end hash was not printed; the fight's moments fell on the recorder's
times), `ffmpeg -f x11grab -framerate 60`, the mix from OpenAL Soft's `wave` driver (nothing played aloud). Clips cut as
H.264 CRF 21 at 60 fps with AAC 160 kb/s at −3 dB (the mix measured −16.5 LUFS, peak 0.1 dBFS), the play field cut
out (x 240–720). The fight's whole recording and its moments are in [the Chimera's
prompts](../../../../enemies/bosses/vornes-chimera/concept/prompts.md#chimera-fight-r93-a). Laid out by scratch
scripts (not kept).

## rear-wave-capture-r93-a

Concept round 93, a capture of the game (step EF5, [how](#game-captures-r93-a), with sound): the first rear wave
(t=13.5–22): the rear warning and the bottom chevrons, the Chimera V-wing rising from the bottom edge in the half the
ship is not in, curving toward the middle.

## core-veins-capture-r93-a

Concept round 93, a capture of the game (step EF5, [how](#game-captures-r93-a), with sound): the first reactor cores
(t=48.5–54.5): core C1 in its open gold hull, its light pulsing up the teal vein toward the spire, burnt by the beam at
t≈51 (the flash, the hull's socket dark, the vein going dark).
