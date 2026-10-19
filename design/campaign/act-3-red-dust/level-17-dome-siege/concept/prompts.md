# Level 17 – concept and production notes

Round 40 (M7 part D, step ART-D: production straight away, as Act 2's and Levels 15-16's; the user is on
vacation, so all of it is pending the user's review in round 40). None of it is prompted: every file is
rendered by a generator in `tools/art/`.

## backdrop-final-r40-a

Production art, M7 part D (round 40). Not a mockup: the sheet and GIF of Level 17's backdrop in
`assets/backdrop/level-17/`, rendered by [tools/art/backdrop_l17.py](../../../../../tools/art/backdrop_l17.py)
(`python3 tools/art/backdrop_l17.py`: every image, the proposed `backdrop:` block (written to
`briefs/m7d-backdrop.yaml` for the merge into the level's data) and this review; `--review` only the review).
From the Ares Landing mood still `mars-ares-landing-r36-a` and the chosen dome a
([Mars](../../../../world/mars/README.md)): the rust plain and the rail line from the north with a stranded
freight train, the outer domes' aprons and annexes with Vrell growth creeping toward them, the burnt outer
greenhouses under their smoke, the Helix compound (`mars_r36.helix_site` as Level 16's station: black and
gold, lit, a clean fenced yard, unlabelled), the rail yard, the inner ring road with its gun pits, the
spaceport's apron, pads and hangars. The domes on the sheet and in the GIF are the ally's sprites at their
data placements. The GIF flies the whole level at 0.5x.

## l17-props-final-r40-a

Production art, M7 part D (round 40), straight to production (no concept): the secret's collapsed greenhouse
frame over the seed-vault capsule (whole, hit, badly hit, lifted aside), the freed capsule (amber bands, a
rule-7 light rim) and its beacon glow, rendered by [tools/art/siege_units.py](../../../../../tools/art/siege_units.py).
The GIF plays the frame being shot apart on the burnt fields and the capsule popping free.

## level-17-capture-r40-a

Round 40, a capture of the game, not generated art (`.png` sheet, `.mp4` with the game's sound), taken on
2026-10-19 (step D5) from a snapshot of the uncommitted working tree, built by `:desktop:installDist` into an
isolated build directory. Run 1: `terran-vanguard --bench 225 --settings <file> --start level --level 17
--difficulty medium --loadout front=pulse-cannon:4,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:2,generator=2,shield=1,armor=2
--special airstrike:2 --escort rook:autocannon:1` (the balance plan's L17 ship: Level 16's, the plan buys nothing
at L17; the Airstrike as fitted since Level 15, no EMP charge fitted, so no ring strip), **no `--invulnerable`**,
on a private Xvfb display (960×540; settings: a 960×540 window at 0,0, `controls.auto-fire=true`). Run 2, for
the domes cracking and breaking: the same ship without Rook, `--difficulty hard --invulnerable --escort none
--bench 80`, auto-fire off, the ship parked at the bottom centre. Video: `ffmpeg -f x11grab -draw_mouse 0
-framerate 30 -copyts`; sound: OpenAL Soft's `wave` driver (`ALSOFT_DRIVERS=wave`) writing the game's mix to a
file, nothing played aloud, aligned at the game's `device reopen` log line. **t** is seconds since the game's
`[display] window` log line. Run 1's ship is flown by XTest key events from a small Python driver at y 96 by
dead reckoning (270 px/s): random sweeps between x 60 and 420, a wall reset every 40 s, parked under the seed
vault's frame (x 70) from t 72 to 80; the Airstrike called at t 41 and 141; after the debrief is seen, Enter.
The radio in the capture plays the times before step D5's retime (t=45, 55.5, 71.5, 80.5; now 47, 57.5, 71, 78).

Run 1: **190 / 196 kills (97 %)**, armour damage 0, secret 1 / 1 (the seed vault), every dome hit once and none
lost, **DOMES UNTOUCHED 0 / 5** (the tracker's label red from Bradbury's first hit at t ≈ 44, then FAILED once
Eden has passed, t ≈ 138), the debrief at t ≈ 185. Seen: the hooked Spore Bombers hanging over Bradbury and
Lowell and over Eden (t 127–129) and their bomb runs; the first Shellback walking in alone (t ≈ 31); the t=46.5
Shellback walking down across Lowell's glass with a hit flash and its impact marker (t 48–50.5), a dark ball
on the dome for an instant (t ≈ 51, perhaps its curl) and its death flash over Lowell (t ≈ 51.5); the first Herald shot within
about 2 s of entering (t ≈ 57.5), its rings barely seen; the burnt fields and the Helix compound (t ≈ 82), the
rail yard, the inner ring road; Shellbacks walking over Schiaparelli and Burroughs (t 113–118); the second Herald
with ringed bombers (t ≈ 125.5); the spaceport apron and two small flak puffs (t ≈ 160.5). Run 2: Bradbury's
cracks growing hit by hit (t 41–44), Lowell broken under a Shellback (t ≈ 50.5), the Herald's rings, radius
circle and threads on its escorts (t 56–70). Not seen: a Shellback's roll and its belly, a vent shot (the vent cannot be hit from below), the EMP strip (no charge fitted). The
video joins run 1, t −1 to 200, and run 2, t 38 to 72 (H.264 CRF 29, AAC 128 kb/s, the sound 3 dB down, a
caption and the time burnt in, 16.5 MB). The sheet: twenty frames with the left HUD panel, four of them run 2's,
the last the debrief.
