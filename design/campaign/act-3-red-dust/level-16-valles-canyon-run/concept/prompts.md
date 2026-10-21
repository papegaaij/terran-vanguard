# Level 16 – concept and production notes

Round 39 (M7 part C, step ART-C: production straight away, as Act 2's and Level 15's; the user is on
vacation, so all of it is accepted in round 39 (2026-10-18). None of it is prompted: every file is
rendered by a generator in `tools/art/`.

## backdrop-final-r39-a

Production art, M7 part C (round 39). Not a mockup: the sheet and GIF of Level 16's backdrop in
`assets/backdrop/level-16/`, rendered by [tools/art/backdrop_l16.py](../../../../../tools/art/backdrop_l16.py)
(`python3 tools/art/backdrop_l16.py`: every image, the proposed `backdrop:` block (written to
`briefs/m7c-backdrop.yaml` for the merge into the level's data) and this review; `--review` only the review).
From the chosen canyon walls b of round 36 (`canyon-walls-r36-b`, MA.1) and the Valles mood still
`mars-valles-r36-a` ([Mars](../../../../world/mars/README.md)). The walls are textures the game projects onto
the level's `terrain` (canyon-wall, wall-foot, the islands' faces and tops); the sheet's composites project
them as WallProjection will. The GIF plays the canyon mouth, the fork's mesa and the rapids' pillar.

## l16-props-final-r39-a

Production art, M7 part C (round 39), straight to production (no concept): the Candor relay hut (sealed,
hit by hit, burst with the crate in the bay; its beacon), the stranded cargo rovers (intact, damaged, wreck)
and the wall-contact spark with its dust, rendered by
[tools/art/canyon_units.py](../../../../../tools/art/canyon_units.py) (`--review` only the review). The GIF
plays the hut's three hits, the rovers being destroyed and the spark at a wall's foot.

## level-16-capture-r39-a

Round 39, a capture of the game, not generated art (`.png` sheet, `.mp4` with the game's sound), taken on
2026-10-14 (step C5) from the uncommitted working tree (nobody else was editing), built by `:desktop:installDist`
into an isolated build directory. `terran-vanguard --bench 240 --settings <file> --start level --level 16
--difficulty medium --loadout front=pulse-cannon:4,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:2,generator=2,shield=1,armor=2
--special orbital-lance:2 --escort rook:autocannon:1` (the balance plan's L16 ship, the Mk III generator included;
the plan has no beam before Level 18, so the Orbital Lance, unlocked at Level 17, stands in for the special to
show a beam burning a tendril), **no `--invulnerable`**, on a private Xvfb display (960×540; settings: a 960×540
window at 0,0, `controls.auto-fire=true`). Video: `ffmpeg -f x11grab -draw_mouse 0 -framerate 30 -copyts`; sound:
OpenAL Soft's `wave` driver (`ALSOFT_DRIVERS=wave`) writing the game's mix to a file, nothing played aloud,
aligned at the game's `device reopen` log line. **t** is seconds since the game's `[display] window` log line.
The ship is flown by XTest key events from a small Python driver at y 96 by dead reckoning (270 px/s): it follows
the canyon's middle computed from `data.yaml` (the sections' scroll and the wall polylines, 40 px off the rock
over a look-ahead), the left channel at the fork (the relay hut), right of the pillar, under an anchor at
gates 1, 3, 4 and 6; three deliberate wall touches (t 50.7 right, 97.7 left, 128.6 right) re-anchor its x;
the Lance called at t 46.75 (gate 2) and 144.75 (gate 5); after the debrief is seen, Enter twice.

**210 / 215 kills (98 %)**, armour damage 0 (the three wall touches and the tendril and pore hits all on the
shield), secret 1 / 1 (the relay hut), **BARRIERS BROKEN 9 / 9** (+205 CR), A+ (rating 99), 3 584 credits. Seen:
the walls begin flat at the field's edges at the mouth (t ≈ 17) and stand at full height by t ≈ 26; gate 1
dormant (a thin line, t 34.1–34.4), its flare and the left anchor's kill at t ≈ 34.6, the tendril withering by
34.9; the Lance column burning gate 2's dormant tendril (t ≈ 47.3) and leaving its black scorch line on the
floor; the fork's mesa from t ≈ 66, the relay hut's door burst in the left channel (t ≈ 72); the first
Threadcrawler down the left wall's foot with its pore waves (t 86–92), the pillar (t ≈ 102), the rovers on the
cliff road (t ≈ 108), the Helix station on the right rim (t ≈ 115.5–118.5), gates 4–6 in the narrows, the second
crawler crossing the outflow's floor (t 163–168), the Burrowers on the sand (t ≈ 173–178), the debrief at
≈ 204. Not seen: a crawler cut (the pulse cannon killed both outright), Rook's wall touches. Radio as text and
blips: Level 16's voices are not rendered yet (step VOX-C). The video is the whole run, t −1 to 230 (H.264
CRF 29, AAC 128 kb/s, the sound 3 dB down, a caption and the level time burnt in, 14.6 MB). The sheet: twenty
frames with the left HUD panel at 0.5×, the last the debrief.

## canyon-redo-r40-a

Round 40 (step CW, round 39 R.1: "Make it look more like a real canyon. Maybe you can take inspiration from
pictures from the Grand Canyon from above."), a capture of the game, not generated art (`.png` sheet of eight
frames at 1:1, `.mp4` cut to 59 s with the game's sound), taken on 2026-10-18 from a snapshot of the
uncommitted working tree built by `:desktop:installDist` into an isolated build directory.
`terran-vanguard --bench 158 --settings <file> --start level --level 16 --difficulty medium --invulnerable
--loadout front=pulse-cannon:4,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:2,generator=2,shield=1,armor=2
--escort rook:autocannon:1`, on a private Xvfb display (960×540, settings as `level-16-capture-r39-a`'s), flown
by the same XTest driver down the canyon's middle (the left channel at the fork, right of the pillar); video
`ffmpeg -f x11grab`, sound OpenAL Soft's `wave` driver to a file; **t** is seconds since the game's
`[display] window` log line. The video joins five level-time cuts (the mouth 15–27, the fork 64–80, the pillar
97–106, the Helix station 112–118, the narrows 136–152), each labelled with the level time burnt in.

What is drawn: the walls and buttes from the canyon model in
[tools/art/backdrop_l16.py](../../../../../tools/art/backdrop_l16.py) (its section "the canyon model") and its
twin in the game, `game/src/main/java/vanguard/game/render/CanyonProfile.java` (projected by `WallProjection`):
each wall descends from the rim in six steps (the dark inner gorge, its talus bench, the red-orange middle
cliffs, the red talus slope, the pale cap cliff, the cream cap slope), each step's edge set back from the
collision line toward the rock by the room to the field's edge and by side canyons of three sizes (the big ones
repeat every 768 rows, the plateau tile's 960 rows at 1.25, so the tile's gullies drain into them); per-step
lighting from the upper left (ambient 0.45, key 0.55), the cliffs' cast shadows on the benches below and the
left wall's on the floor (0.45 px per px of rise). The textures (`canyon-wall` 128×1280, `mesa-face`,
`pillar-face`, `mesa-top_0..2`, `pillar-top`, `plateau`, `cliff-habitat`) are regenerated by
`python3 tools/art/backdrop_l16.py canyon-wall mesa-face pillar-face mesa-top pillar-top plateau cliff-habitat`.
Before the game work, three still mockups from the script's compositor (`composite()`, the model painted by
height at 2× and box-filtered) side by side with round 39's capture crops settled the steps, the light and the
buttes' insets.

## canyon-redo-compare-r40-a

Round 40 (step CW): round 39's capture (`level-16-capture-r39-a.mp4`, frames at level time + 1 s of video)
above `canyon-redo-r40-a`'s frames at the same level times (26, 72, 101.5, 143), the play field cropped at 1:1.
Not generated art.
