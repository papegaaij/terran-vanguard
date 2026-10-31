# Production art plan – capture notes

## art-batch-capture-r26-a

Round 26 game capture (M4 part H batch), not generated art: frames of `--bench` runs on an own Xvfb display at 960×540 (`--invulnerable`, auto-fire on in a temporary settings file): Level 01 with the Scatter Vulcan (Skitter and Needler kills, zoomed), Level 04 with `--special smart-bomb:3` (two Scuttlers taken by the bomb: burst, pieces, the husks scrolling away with the ground), Level 02 with the Smart Bomb fired (flash, burst at the ship, the ring over the field), and a new game's Act 1 title card over its still. It shows the art of `tools/art/vrell_deaths.py`, `tools/art/smart_bomb.py` and `tools/art/act_stills.py` as the game draws it; the boss bar plate is drawn by the HUD task and captured with it.

## dither-fix-capture-r71-a

Step DITH8 game capture (the user, 2026-10-30: every dithered set at ≥ 25 % checker smoothed), not generated art:
`--shots` frames of the same deterministic runs before and after the re-render, each from a `:desktop:installDist`
build of a snapshot (the assets before, then the smoothed ones), on an own Xvfb display at 1920×1080 full screen with a
temporary settings file (`controls.auto-fire=true`), `--bench`, `--invulnerable`: Level 28 at ticks 60, 1500 and 3700
(the gate ring, the brood gallery's mouth, the collapsed rack), Level 32 at 3400 (the shaft wall's lamps), Level 29 at
2220 (the hub's lamps), Level 14 at 12780 (`--difficulty easy` and a strong `--loadout`: about 4 s after the Spire's
death, the daylight beams' first frame). Crops of 160×120 px of the 960×540 shots, before left and after right, scaled
2× nearest (as drawn at 1080p) and stacked with their labels; the last row is the left HUD panel's face. The
Ascendancy cruiser's shadow (the worst set, 93 %) is not in it: the run past the Maw needs the boss killed.
