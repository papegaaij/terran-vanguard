# Level 50 – Choir Heart: concept prompts

## backdrop-final-r95-a

**The hive core, production** (round 95, M11 part H, step ART-H2, 2026-11-07; production art, pending the user's
review). Files: `backdrop-final-r95-a.png` (sheet: ten screens with stand-ins and bullets at the pulse's peak, the tile
sets, the pulse light, the floor's lead and end, the seed vault) and `.gif` (the approach and the arena in a
time-lapse); the `backdrop:` block, the pulse per section, a section proposal and the closing gate's numbers in
`backdrop-proposal.yaml`. Generator: `nice -n 19 python3 tools/art/act7_core.py backdrop` (held in
`build/art-held/backdrop/level-50/` until the level's data has a `backdrop` block, then `assets/backdrop/level-50/`).

- **Prompt:** late-1990s pre-rendered CGI game backdrop, straight top-down, the Vrell hive's core in one palette
  (near-black violet tissue #0c0612 to #321840, violet veins #54287c, red-violet veins #7c225c, pale violet crystal
  #64608c), lit only by the Heart's slow red-violet pulse (#6e2152 at its peak, smooth, reaching further toward the
  core, never a flash). The Throat: a ribbed tissue tube, rib rings bowing toward the core ahead, the tube's walls
  ribbed along both sides with slow-pulsing violet veins, the middle open. The Choir Chambers: crowded vaults with
  curved walls, pale crystalline ribs sweeping in from the sides, the Heralds' ring markings grown into the walls and
  floors, softly singing. The Brood Halls: a living tissue floor in rounded lobes, dim red-violet veins in its
  creases, sealed spawning pits puckered shut, the hall's walls darker at the sides; the floor begins and ends along a
  ragged lip. The Last Chamber: the veins converging on one point ahead, the chamber's rib rings round it, the tissue a
  step lighter toward it, vein cables along both sides bending inward. The units a step brighter than the walls; the
  pulse's brightest below a bullet's core.
- **No straight lines:** every wall, rib, vault, lobe and lip curved or ragged (a row/column scan finds no run of 60
  px in a tile or 120 px in a piece, and no piece's alpha silhouette runs straight for 40 px).
- **Negative prompt:** photograph, painterly brush strokes, perspective camera, side view, ruler-straight lines,
  polygonal cells, saturated pink, a flash, ordered dither, clipped glow, text, watermark.

## closing-gate-final-r95-a

**The closing gate, production kit** (round 95, step ART-H2; pending the user's review). Files:
`closing-gate-final-r95-a.png` (the scene from K+10.5 to K+39 drawn from the kit, with the mean luminance per frame)
and `.gif` (K+10 to K+40 at 1×). Generator: `nice -n 19 python3 tools/art/act7_core.py backdrop` (the review emulates
step H4's `ClosingGateLooks` and the reversed `TransitWarp`).

- **Prompt:** the same hive gone still: its tissue greying from the dead Heart outward over 3 s (#1e1d21 to #52505a,
  the veins dark creases); the race back through the grey chambers in reverse order (the Last Chamber, the Brood
  Halls, the Choir Chambers, the Throat) at speed, the tissue folding closed behind from the bottom edge; ahead, the
  event plane's pale violet-white disc with dark violet streaks growing as the wing nears it while its rim contracts
  (the gate closing); the warp reversed back to the starfield in one smooth step; the near side: an ordinary starfield,
  Neptune small and blue, the *Steadfast*'s group ahead; behind, the ring's dark stone arc with cold teal-white glyphs,
  the plane in its mouth shrinking to a point and fading, then the glyphs going dark one by one round the arc.
- **Photosensitivity:** the luminance moves one way through the warp, no flash, no inversion.
- **Negative prompt:** photograph, a white flash, colour inversion, ruler-straight lines, text other than the event
  line, watermark.

## seed-vault-final-r95-a

**The seed vault** (round 95, step ART-H2; pending the user's review). Files: `seed-vault-final-r95-a.png` (sheet:
whole, cracked, broken open at 4×, the break, in place on the floor at 1×) and `.gif`. Generator: `nice -n 19
python3 tools/art/act7_core.py vault` (`seed-vault_0..2` 40×40, `seed-vault-break_0..7` 64×64, held in
`build/art-held/sprites/` until the level's data names the vault).

- **Prompt:** late-1990s pre-rendered CGI sprite, top-down, a sealed Vrell organ sunk in a living tissue floor: a low
  chitin dome in pale amber-violet, six ribs from its crown to its rim, six round windows showing pale mint seeds
  packed inside, a darker rim; cracked along two splits; broken open: the dome split away, the hollow empty. Broken:
  chitin chips and seeds flung out and shrinking, no flash.
- **Negative prompt:** photograph, painterly, a glow, a fireball, text, watermark.

## game-captures-r95-a

Captures of the game, not generated art (M11 parts G and H close-out, step GH5, 2026-11-08): the game built by `:desktop:installDist` (branch `m11-act-7`, parts G and H in the tree, the full `check` green), flown by the content autopilot's recording: `tools/concept/replay/record_replay.sh 50 medium l50.rec --loadout front=ion-beam:5,left=tesla-coil-pod:5,right=micro-missile-pod:5,rear=swarm-tail:5,generator=6,shield=6,armor=5 --special vrell-swarm-call --escort rook:autocannon:1` (the balance plan's Level 50 fit, Rook L1), then the start script with the same options plus `--level 50 --difficulty medium --replay l50.rec --bench <s>` on a private 960×540 Xvfb display, a scratch `--settings` (a 960×540 window, auto-fire), nothing played aloud (the audio environment stripped, OpenAL Soft's `wave` driver writing the mix to a file, checked in `/proc/<pid>/environ`); `ffmpeg -f x11grab -framerate 60`. The game's state hash at the recording's end equal to the recorder's (`5049d13ebe9f8ae9`, 25,615 steps, 426.9 s, won with 126 armour). Level t = 0 at the log's `[replay] the level's first step`; the mix aligned on its end. Clips: H.264 at 60 fps, AAC 160 kb/s at −3 dB; the play field (x 240–720) cut out unless the whole screen is named. Cut by step GH5's scratch scripts (not kept).
Files: `throat-capture-r95-a.mp4` (t=3–13), `brood-halls-capture-r95-a.mp4` (t=111–124), `closing-gate-capture-r95-a.mp4`
(t=364–427, the whole screen) and the Heart's whole fight
([heart-fight-r95-a](../../../../enemies/bosses/choir-heart/concept/prompts.md#heart-fight-r95-a)). On the recorder's clock:
the warning t=182, in place 193.0, phase 2 294.0, phase 3 328.6, K (the last words) 364.9, the flash 368.4, `THE GATE IS
CLOSING` 375.4, the transit forming 376.9, the warp home 393.9, the near side 395.4, the plane out 395.9, the end 426.9.

**The closing gate, re-shot** (step FIXGH, 2026-11-08: Rook flying home, the *Steadfast*'s group in view): the same
method with Rook on **Missiles L5** (`--escort rook:missiles:5`; on the plan's Autocannon L1, and on Autocannon L5, Mortar L5,
Scatter L5 and easy, the autopilot's Rook ejects in the Heart fight), hash `39c443825002e5c4` (22,057 steps, 367.6 s, won
with 160 armour); K t=305.6, the gate closing 316.1, the warp home 334.6, the near side 336.1, the end 367.6; the clip
t=304.7–367.7.

**The campaign end** (`campaign-end-capture-r95-a.mp4`, re-shot by step FIXGH): `./gradlew :content:playthroughSave
-Plevel=50 -Pdifficulty=medium -Pout=<dir>` (the campaign flown on the balance plan to Level 50, the autosave and slot 1),
then the start script with `--settings <dir>/settings.properties --start title --invulnerable --bench 1800` on the same
kind of display, at normal speed: Continue, the briefing, the hangar's Launch, Level 50 flown by the standing ship
(invulnerable, auto-fire; the debrief shows the boss time 6:13), then keys (XTest) through the debrief, the act summary,
the four ending pages, the campaign end card, the high score entry (`AEGIS`) and the credits. Level 50 cannot be won from
a real save without a debug option (the autopilot's replay is a level start, and the standing ship dies without
`--invulnerable`), so the card still reads `AUTOSAVE OFF - DEBUG RUN` and the score is not saved; the credits ran at their
own speed. The clip is eight cuts of that session (the waits between the key presses and the credits' middle left out).

## throat-capture-r95-a

A capture (step GH5, [how](#game-captures-r95-a)): the Throat.

## brood-halls-capture-r95-a

A capture (step GH5, [how](#game-captures-r95-a)): the Brood Halls' floor and the seed vault.

## closing-gate-capture-r95-a

A capture (step GH5, [how](#game-captures-r95-a)): the gate scene end to end.

## campaign-end-capture-r95-a

A capture (step GH5, [how](#game-captures-r95-a)): the campaign end screens from a real save.
