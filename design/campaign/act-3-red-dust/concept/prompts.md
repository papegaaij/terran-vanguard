# Act 3 – Red Dust: concept prompts

Production art for the act's title card; the briefing images of the act intro are in
[design/ui/briefing/concept/prompts.md](../../../ui/briefing/concept/prompts.md#briefing-images-final-r38-a).

## act-3-still-final-r38-a

Production art, M7 part B (round 38; straight to production as Act 2's still, pending the user's review). Not a mockup: the review sheet of `assets/ui/act-3-red-dust-still.png` (960×540, the still behind the Act 3 title card) and of the title card as the game draws it over it (darkened to 55 %, letterbox, the chrome lettering `assets/ui/act-3-red-dust-title.png` from [tools/concept/ui_assets.py](../../../../tools/concept/ui_assets.py), read from the act's `title_card`), rendered by [tools/art/act_stills.py](../../../../tools/art/act_stills.py) (`python3 tools/art/act_stills.py act-3`) from Level 15's production backdrop ([tools/art/backdrop_l15.py](../../../../tools/art/backdrop_l15.py), its images in `assets/backdrop/level-15/`), as Act 2's was from Level 08's. Mars from the entry corridor, the level's deep layer seen in perspective from high up: the Tharsis plain (`mars-high`, with broad light and dark regions over its tiling) running to the curved horizon; Olympus Mons (`olympus`: the caldera's nested pits, the radial flank) far below in the lower middle, under the lettering; the cloud deck's banks (`deck-banks`) on a nearer plane in the middle distance, their shadows on the plain, kept off the volcano; the thin water-ice cirrus (`cirrus`, additive at 40 % as the game draws it) nearest; the haze thickening with distance to the butterscotch sky, a bright limb line, the sky darkening above (the height); a dust-storm front rolling along the northern horizon on the right (billowing ochre dust, hazed; drawn by the script, no backdrop piece). One 96-colour palette with 4×4 ordered dither.

Brief (for an artist): late-90s pre-rendered CGI, a view down from the edge of space over Mars: Olympus Mons' caldera and shield far below, banks of pale water-ice cloud between, the rust plain to a curved horizon, a dust-storm wall on the horizon, the butterscotch haze; the middle kept quiet for the act title.

## act-3-intro-capture-r38-a

Round 38, a capture of the game (`.png` sheet, `.mp4` with the game's sound), taken on 2026-10-14 (step B5) with
the build and recipe of [level-15-capture-r38-a](../level-15-olympus-descent/concept/prompts.md#level-15-capture-r38-a)
(Xvfb, x11grab, the `wave` driver), here `terran-vanguard --start title --bench 120 --settings <file>` with a
version 3 save (`saves/autosave.json` next to the settings file: the M6 release's format, written before Act 3
existed, `nextLevel` 15, no story flags; hand-written in step B4) and no debug option besides `--bench` (a debug
run writes no save, so the `act-intro:3` flag is not stored; `CampaignRouteTest` and `SaveFormatTest` cover it).
Enter at t 6 (PRESS START), 9 (CONTINUE), then after each page's voice: t 32, 45, 59, 72, 91 and 104. The title card
shows from t ≈ 9.5 and gives way by itself at ≈ 13; the four intro pages (voices 17.5, 11.4, 12.6 and 11.7 s) from
t ≈ 13.5, 33, 45.5 and 59.5, Level 15's two briefing pages from ≈ 73 and 91.5, the hangar from ≈ 105. The sheet: ten
frames at 0.5×; the video (H.264 CRF 28, 42 s, 1.5 MB): t 7–37 (Continue, the card, page 1 whole) and 100–112 (the
briefing's end into the hangar).
