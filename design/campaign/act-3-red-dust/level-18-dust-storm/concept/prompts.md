# Level 18 – concept and production notes

Round 41 (M7 part E, step ART-E: production straight away, as Levels 15-17's; the user is on vacation, so all of
it is pending the user's review in round 41). None of it is prompted: every file is rendered by a generator in
`tools/art/`.

## backdrop-final-r41-a

Production art, M7 part E (round 41). Not a mockup: the sheet and GIF of Level 18's backdrop and the storm
look's textures in `assets/backdrop/level-18/`, rendered by
[tools/art/backdrop_l18.py](../../../../../tools/art/backdrop_l18.py) (`python3 tools/art/backdrop_l18.py`: every
image, the proposed `backdrop:` block (written to `briefs/m7e-backdrop.yaml` for the merge into the level's data)
and this review; `--review` only the review). From the storm-plains mood still `mars-storm-plains-r36-a` and the
chosen radius look b ([Mars](../../../../world/mars/README.md), MA.2): the northern plain's rust flats with dry
channels and the half-buried pipeline, the storm plain under drifted dust sheets, the eye's cracked dry lake
with the storm's walls round it, the pipeline ridges under the nests, Ares Landing's outskirts ahead at the end;
the dust banks and streaks, the front and the back wall passing over on high-air; the veil (two fields) and the
two tongue fields that break the falloff. The sheet and the GIF draw the look as the game will (the radius at
medium per the README's Layout, air units outside it as dark silhouettes keeping their glow, bullets always
drawn, a few stand-in units). The GIF flies the whole level at 0.5x.

Rendered again on 2026-10-19 (step FXS, round 41 F.c): `storm-front` and `storm-back-wall` thin out to nothing over
their top third (they ended at their top row at up to 20 % opacity, the straight line at 5 s in the loop),
`python3 tools/art/backdrop_l18.py storm-front storm-back-wall`, which renders the review again.

## l18-props-final-r41-a

Production art, M7 part E (round 41), straight to production (no concept): the secret's militia rover wreck, a
six-wheeled rover in the militia's off-white with its amber cargo pod, half buried in a rust drift (intact, hit,
destroyed with the pod blown open), its red distress beacon (additive, aligned with the rover) and the pod
popping, rendered by [tools/art/storm_units.py](../../../../../tools/art/storm_units.py) (`python3
tools/art/storm_units.py rover`). The sheet shows the beacon seen inside, at the edge of and outside the radius
(gone with the ground); the GIF plays the rover scrolling into the radius at the left edge, shot open, the crate
popping free.

## level-18-capture-r41-a

Round 41, a capture of the game, not generated art (`.png` sheet, `.mp4` with the game's sound), taken on
2026-10-19 (step E6) from a snapshot of the uncommitted working tree after step E5 (beam-era HP, 301 enemies),
built by `:desktop:installDist` into an isolated build directory. Run 1: `terran-vanguard --bench 214 --settings
<file> --start level --level 18 --difficulty medium --loadout front=ion-beam:2,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:2,generator=2,shield=1,armor=2
--special airstrike:2 --escort rook:autocannon:1 --invulnerable` (the balance plan's L18 ship: the Ion Beam
bought at L2, the Pulse Cannon sold), on a private Xvfb display (960×540; settings: a 960×540 window at 0,0,
`controls.auto-fire=true`). **`--invulnerable`**: two attempts without it, flown by the same blind driver, lost
the armour at t ≈ 80 and t ≈ 95 (Level 17's run survived the same driver). Run 2, for the pull: the same ship
without Rook, `--escort none --invulnerable --bench 62`, auto-fire off, the ship moved left of centre (x ≈ 165)
at t 30 and left there. Video: `ffmpeg -f x11grab -draw_mouse 0 -framerate 30`; sound: OpenAL Soft's `wave`
driver (`ALSOFT_DRIVERS=wave`, `ALSOFT_CONF` naming the file) writing the game's mix to a file, nothing played
aloud, aligned at the game's `device reopen` log line. **t** is seconds since the game's `[display] window` log
line. Run 1's ship is flown by XTest key events from a small Python driver (ctypes on libXtst) by dead reckoning
at 270 px/s: random sweeps between x 120 and 360, a wall reset every 30–40 s, held near the centre through the
Dust Devils (t 41.5) and hint 1 (t 69.5), moved to the left edge (x ≈ 64) for the rover wreck from t 80 to 97,
the Airstrike called at t 148; no key after the level end.

Run 1: **230 / 301 kills (76 %)**, armour damage 0 (invulnerable), secret 1 / 1 (the rover wreck), the kill-ratio
secondary MET (the HUD's KILLS n / 196 is its required count, 65 % of 301), the debrief at t ≈ 199. Seen: the
plains and the front's haze (t 26–33), the radius closing in (t 33–36), the two Dust Devils closed and opening
with their grit spiral (t 42–46), hint 1's three Ghost Drones (t 69.5) with a red eye glint before a burst
(t ≈ 71), the eye's clear air over the dry lake (t 112–128) with its Dust Devil holding the supply drone's armour
patch and the pickups released at its death (t ≈ 127.5), the back wall (t 131), the back-wall devils with the
open core glowing through the veil outside the radius (t 142), rear and side warnings, the clearing and Ares
Landing's domes ahead (t 176–183), the debrief. Run 2: the ship parked at x ≈ 165 pulled about 60 px right
into the gap between the two devils (t 42–45), the open cores and the spiral at 1:1. Not seen or read badly
(round 41's capture row): the rover was shot by the beam at the top of the screen, outside the radius, so its
beacon never showed (the crate popped at t ≈ 90.5); the t=67.5 Needler line was still on the screen through
hint 1 (t 69.5–72.5), so the Ghost Drones' first sight is not alone; the storm front at t ≈ 28 reads as a soft
haze, not a wall; outside the radius the air units keep most of their detail (Needlers' white legs, Stingers'
red), so the silhouette pass is weak against the light veil. The video joins run 1, t −1 to 214, and run 2, t 39
to 49 (H.264 CRF 29, AAC 128 kb/s, the sound 3 dB down, a caption and the time burnt in, 16.5 MB). The sheet:
twenty frames with the left HUD panel, three of them run 2's, the last the debrief.

## level-18-crops-r41-a

Round 41, readability crops from the capture above (`level-18-capture-r41-a`), cut from the full 960×540 frames:
the top row at 1:1, the bottom row at 2× (nearest neighbour): inside the radius (run 2, t=44: an open Dust Devil,
its spiral and the ship), outside it (run 1, t=71: Ghost Drones, one with its eye glint, and the Needlers left
from t=67.5) and the back wall at radius 150 (run 1, t=142: a devil beside the ship, another's core glowing
through the veil at the right edge).

## level-18-fixes-r41-b

Round 41 follow-up, a capture of the game, not generated art (`.png` sheet of eight play-field frames at 0.75x,
`.mp4` without sound), taken on 2026-10-19 (step FXS) after the fixes F.c (the storm front's top edge), K.a (the
Dust Devils under the veil in every phase) and R.b (the air silhouettes in the dust's own colour), built by
`:desktop:installDist` into an isolated build directory: `terran-vanguard --bench 100 --settings <file> --level 18
--difficulty medium --loadout front=ion-beam:2,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:2,generator=2,shield=1,armor=2
--special airstrike:2 --escort rook:autocannon:1 --invulnerable` on a private Xvfb display (960×540; settings: a
960×540 window at 0,0, `controls.auto-fire=true`), the ship left where it starts (no driver). Video: `ffmpeg -f
x11grab -draw_mouse 0 -framerate 30`; **t** is seconds since the game's `[display] window` log line. The video joins
t 27–34 (the front passing) and t 75–91 (the Dust Devils at t 77 opening out of the dust at the edge of the radius,
open inside it; Needlers, Stingers and Ghost Drones outside the radius), H.264 CRF 27, the time burnt in. The sheet:
t 29, 31, 78.5, 79, 79.5, 80.5, 83 and 88.
