# Level 15 – concept and production notes

Round 38 (M7 part B, step ART-B: production straight away, as Act 2's levels; the user is on vacation, so
all of it is pending the user's review in round 38). None of it is prompted: every file is rendered by a
generator in `tools/art/`.

## backdrop-final-r38-a

Production art, M7 part B (round 38). Not a mockup: the sheet and GIF of Level 15's backdrop in
`assets/backdrop/level-15/`, rendered by [tools/art/backdrop_l15.py](../../../../../tools/art/backdrop_l15.py)
(`python3 tools/art/backdrop_l15.py`: the proposal, every image, the checks and this review; `--review`
only the review; `--check` the checks; `--proposal` only [backdrop-proposal.yaml](backdrop-proposal.yaml), the
level's block for the level-data step to merge). Direction: the Olympus mood still
[mars-olympus-r36-a](../../../../world/mars/concept/mars-olympus-r36-a.png) (`tools/concept/mars_r36.py`: its
Mars ramps, the caldera's nested pits and radial channels, the water-ice cloud tones, imported unchanged) and
the art direction's Mars approach, [parallax r02-c](../../../../art-direction/concept/parallax-r02-c.png).
Brief (the README's *Layout*): a descent without a zoom. Sections 1–2 have no ground: Mars far below on
`deep` (x0.15, hazed toward the butterscotch sky), the Tharsis plain small with Olympus Mons passing under the
entry corridor, then the flank closer with its leveed lava channels; thin water-ice cirrus on `high-air`
(≤ 40 %), the entry's plasma streaks fading out round t=8 (the `clear` look's wisps, the entry-glow peak);
the thick deck as low-air banks (~30 %, opaque cores, peach tops, mauve undersides). At t≈62–64 the opaque
`deck-close` covers the whole field for 2 s while the ground's opaque edge (`slopes-lead`) crosses the screen
under its core, then parts over the slopes. The ground: the lower flank (stepped lava terraces, basalt knobs,
regolith fields with burrow furrows), the foot of the Lycus escarpment (scree fans and boulders, the great
cliff along the right edge as perspective towers with ravines: nothing gameplay touches), Lycus Survey Station
overrun (two pairs of pads, the domed hab drawn flat, the road with Vrell growths), the drop pod's ledge, the
Tharsis dune sea (crests across a wind from the left, dark sand in the troughs), a crashed colony lander on a
crest and the colony rail line with three pylons. Dust: thin veils (light), crest streaks (section 5), the
dust peak's rolling banks (170–180 s, translucent so the Burrowers' bulges read). The transport passes over
the launch in the first 2 s with its drop bay open through the hull, drawn as a huge overhead pass (75 %,
normal blending: a data key for the game, see the proposal). The sheet: every tile set and piece, then 14
composites as the game draws them (the turrets, mortars and props as stand-ins); the GIF: the launch and the
close-over.

## l15-props-final-r38-a

Production art, M7 part B (round 38): Level 15's props, rendered by
[tools/art/l15_props.py](../../../../../tools/art/l15_props.py) (`python3 tools/art/l15_props.py`; `--review`
only the review). No concept: designed to the README's *Ground targets* and *Secrets and pickups* and
readability rule 7. Brief: Lycus Survey Station's supply containers are Level 01's cargo container
(`tools/art/loot_targets.py`'s model, damage and break-apart, imported unchanged) in the colonies' dusty
off-white with the matte amber hazard ends: intact, damaged, wrecked (burnt out, the roof gone) and the
eight-frame break-apart. The secret: a colonial supply drop pod (an off-white capsule with amber hazard
bands, a fin and a beacon lens) half buried under angular basalt rubble and dirt on the ledge at the cliff's
foot; four hits (hard 6) knock a boulder off each (scorched), then the pod lies uncovered among the
scattered rocks and its hatch springs open on the amber crate; rule 7's light rim on every frame and the
beacon's blinking glow (additive, a slow blink until the first hit, a hurried flicker after it). The sheet:
the frames enlarged, the rubble on the ledge and the containers on the escarpment's regolith; the GIF: the
rubble cleared hit by hit with the beacon blinking, then a container breaking apart.

## level-15-capture-r38-a

Round 38, a capture of the game, not generated art (`.png` sheet, `.mp4` with the game's sound), taken on
2026-10-14 (step B5) from the uncommitted working tree (13:50; nobody else was editing), built by
`:desktop:installDist` into an isolated build directory. `terran-vanguard --bench 245 --settings <file> --level 15
--difficulty medium --loadout front=pulse-cannon:4,left=micro-missile-pod:2,right=micro-missile-pod:2,rear=tail-gun:2,generator=1,shield=1,armor=2
--special airstrike:2 --escort rook:autocannon:1` (the balance plan's L15 ship, cores included; the Airstrike's
charges a guess, the plan does not track them), **no `--invulnerable`**, on a private Xvfb display (960×540;
settings: a 960×540 window at 0,0, `controls.auto-fire=true`). Video: `ffmpeg -f x11grab -draw_mouse 0 -framerate
30 -copyts`; sound: OpenAL Soft's `wave` driver (`ALSOFT_DRIVERS=wave`) writing the game's mix to a file, nothing
played aloud, aligned at the game's `device reopen` log line. **t** is seconds since the game's `[display] window`
log line. The ship is flown by XTest key events from a small Python driver (dead reckoning at 270 px/s): random
sweeps along the bottom (y 96, x 60–420) with a wall reset every 40 s, parked under the drop pod's rubble (x 400)
from t 124 to 131.5; Airstrikes at t 100, 150 and 173; after the debrief is seen, Enter twice into the hangar.

**198 / 200 kills (99 %)**, armour damage 8 (100 → 93), secret 1 / 1 (its crate and the four containers' salvage not
picked up: the driver stays at the bottom; SALVAGE and SECRETS credit 0), secondary met, A+ (rating 97), 3 297
credits. Seen: the transport passes in the first ≈ 1.5 s while the ship and Rook drop into formation; the deck
closes from the top from t ≈ 60, covers the field t ≈ 61.5–64.5 and has parted over the slopes by t ≈ 65.5; the
first field's first bulge at t ≈ 71.5 and surfacing ≈ 72.5 (rings from ≈ 73.5, the craters kept); the pincer's
edge warnings at t ≈ 142; the rail line crosses at t ≈ 186–188, the last field still surfacing to t ≈ 194, the level
end lines from ≈ 196, the debrief at ≈ 205. The video is the whole run, t −1 to 232 (H.264 CRF 29, AAC 128 kb/s, the
sound 3 dB down, a caption and the level time burnt in, 18.5 MB). The sheet: twenty frames with the left HUD panel
at 0.5×, the last the debrief.
