# Iron Sovereign — concept prompts

Round 60 (Act 5 "The Belt" world proposals, step ART-P-world): a/b mockups by `tools/concept/belt_r60.py` on the
frozen `render` package (`python3 tools/concept/belt_r60.py sovereign [a|b]`), in the belt look of the gap
analysis's Q11 a and the fight of Q7 a (both pending the user's review in round 60). The rings are built from
identical hull segments (outer 12, middle 8, inner 6) rendered at 1-degree headings under one fixed top-left sun
and placed at their angle each frame, as the game would; the batteries are the SAM Nest's rack, the Rail Bunker and
the Sentinel Tower's emitter from `tools/concept/enemies_r60.py` (look a), each on a black collar whose four lamps
show gold out of the fire arc and red inside it. Each sheet measures the segment sprites and estimates the atlas
pages (the one-page boss budget).

**Shared look:** late-1990s pre-rendered CGI game sprite, straight top-down orthographic, a vast battle station
seen from directly above in orbit, a strong key light from the top-left (the belt's sun), glossy highlights, crisp
silhouette. Jovian Ascendancy hardware: gloss black hex-panelled armour (#1a1622), gold trim (#ffa800) on at least
a fifth of the silhouette (ring rims and teeth, battery collars, the core's petals), a thin bright gold rim on the
edges facing the light and a red rim on the far edges, small red running lights (#ff2a1e). Three concentric rings
round an armoured core of six shutter petals bearing the Ascendancy crest (a rising sun over a ringed planet), the
rings separated by dark gaps with catwalk light strips.

**Shared negative prompt:** photo, painterly, cel shading, perspective or 3/4 view, tilted view, motion blur, text,
logos other than the crest, grey Coalition livery, blue or cyan lights, magenta or yellow glows (reserved for
bullets), a planet in the foreground.

## iron-sovereign-r60-a

**Iron Sovereign A, continuous wheel (Ascendancy, Level 35)**: about 440 px across (core 80 px; ring bands 70–100,
123–157, 180–220 px from the centre). Files: `iron-sovereign-r60-a.png` (sheet) and `iron-sovereign-r60-a.gif` (the
play field, 26 s).

- **Prompt:** The shared look. Each ring a smooth continuous wheel: the segments butt into one ring with their
  seams hidden in the panel joints, a raised gold lip and gold teeth along every ring's outer rim, dark facet ribs
  across the deck with a red running light on each, small vents. The batteries on black collars with gold rings:
  six twin-tube missile racks on the outer ring, four rail-gun turrets on the middle ring, three laser emitters on
  the inner ring. The core: six armoured gloss-black petals with a gold ray and a gold ring arc on each, gold light
  in the slits between them.
- **Negative prompt:** the shared list above.
- **Animation brief:** Intro 2 s: lights come on ring by ring (outer → inner) and the rings spin up from rest.
  Outer 6°/s clockwise, middle 10°/s counter-clockwise, inner 120° in 4 s then a 4 s hold. The fire arc (120° on
  the ship's bearing, clamped to the lower half): a faint red band on each ring's hull, battery lamps gold → red.
  SAM salvos of two homing diamonds, the rail line (the Rail Bunker's red lock line, then the shot), the laser's
  crimson wedge and sweep during the inner ring's hold, the core's 5-bullet fan after its slits glow gold. A lock:
  the ring grinds to a halt over 2 s, its lights go out, its mounts are wrecks. The core opens over 3 s (petals
  slide apart, white-gold reactor), the gold hex shield, a 2-arm spiral, a Dart stream from a rim bay after its
  gold door-light. Death: the batteries cook off ring by ring (0.15 s apart), the rings crack at their segment
  joints and drift apart turning, the core folds inward and blows, a screen flash.
- **Mockup generator:** `python3 tools/concept/belt_r60.py sovereign a`

## iron-sovereign-r60-b

**Iron Sovereign B, armoured links (Ascendancy, Level 35)**: the same station and fight. Files:
`iron-sovereign-r60-b.png` (sheet) and `iron-sovereign-r60-b.gif` (the play field).

- **Prompt:** As A, but every ring a chain of separate armour blocks: each segment a block with chamfered ends and a
  dark gap to the next, a gold-edged cap plate on top and a gold stripe along its outer edge, red lights at the
  block ends; a gold track ring under each ring shows through the gaps. No teeth: the moving gaps read the turning.
- **Negative prompt:** the shared list above.
- **Animation brief:** as A; at the death the blocks separate along the gaps.
- **Mockup generator:** `python3 tools/concept/belt_r60.py sovereign b`

## iron-sovereign-final-r68-a

**Production art, decided in round 68, 2026-10-29 (step ART-H1, 2026-10-29).** The chosen A.17 b "armoured
links" in Q.11 b's gunmetal and charcoal with gold, built for A.23 **top-lit**: the ring segments rendered once over
0-45 degrees with a light straight above the station's centre (outer 88, middle 63, inner 41 frames: the rim moves
<= 2 px a step), every other heading by quarter turns and a mirror (pixel-exact: the review proves the rule against
direct renders), the sun's top-left direction carried by a static sheen and shade over the rings; the base, the
core (shut, the petals sliding open in 12 frames, open: a white-gold reactor under the shared medium hex shield), the
battery mounts (lamps dark / gold / red) and the turrets (the SAM Nest's rack, the Rail Bunker's rails and the
Sentinel Tower's emitter in the station's livery, 64 headings) lit by the belt's sun. A locked ring is its segments
tinted with the lamps out and its wrecked mounts venting; the death's cook-offs, collar debris and the core's
implosion are additive and solid frames; the segments drift apart by the same rule.

- **Brief:** as [iron-sovereign-r60-b](#iron-sovereign-r60-b) (prompt, negative prompt), with Q.11 b's gunmetal
  hulls (#5a606c, facets #464b55) instead of gloss black.
- **Generator:** `python3 tools/art/iron_sovereign.py` (`--review` rebuilds this sheet and GIF from the written
  frames, `--check` lists the sets and the atlas share); the sprite names, the segment rule and the frame layout are
  in the generator's docstring and `pivots/iron-sovereign.json`.

## iron-sovereign-capture-r68-a

Concept round 68, captures of the game, not generated art (M9 part H, step H5, 2026-10-29): the game built by `:desktop:installDist` from branch `m9-act-5` (after step H4's full `check`; the Sovereign's data as built, round 68 C.1 a) on a private 960×540 Xvfb display, `--bench <s> --settings <scratch>/settings.properties --level 35 --difficulty medium`, the plan's Level 35 fit `--loadout front=ion-beam:4,rear=swarm-tail:3,left=micro-missile-pod:4,right=micro-missile-pod:4,generator=4,shield=2,armor=4 --special emp-burst:1 --escort rook:autocannon:1`, `controls.auto-fire=true`. **Run A** `--invulnerable` (the ship kept still in the middle, which the fight would otherwise kill): the whole level, the fight (the halt at t=93; the inner ring locked at +31 s, the middle +40 s, the outer +49 s, the core open +52 s, the kill ≈ +148 s) and the debrief. **Run B** (not invulnerable): the approach, the ship moved under the lifeboat (x≈400) from t=63.3 to t=74 (lost at t≈85 standing still). **Run C** `--invulnerable` with the Ion Beam alone and no Rook (`--loadout front=ion-beam:4,generator=4,shield=2,armor=4 --escort none`): with the plan's pods and Rook the bay Darts die as they leave the bays, so they are seen only here. **Run E** `--invulnerable --debug-speed 4`, Enter every 14 s from the debrief: the act end. Clips: `ALSOFT_DRIVERS=wave` writing the game's own mix to a file, the screen recorded with ffmpeg `x11grab` at 30 fps, cut to one subject each (≤ 10 s, ≤ 1 s lead-in), cropped on it at 1:1 and muxed (H.264 main, AAC); the level time from the relay's flash (t=61), the sound's lag (0.47 s) from its crack; the fight labelled in seconds from the halt (the level clock stops there). Each clip checked on an 8-frame sheet (the subject in most frames). PNGs: frames of the same recordings at 1:1 (scaled where named), labelled. Scripts: step H5's `clips.py` and `stills.py` (scratch).

- **sovereign-spinup-capture-r68-a**: run A halt −0.15 to +6.9 s, 480×460 (the rings lighting outer → inner and spinning up under the `WARNING` banner, the spin-up's grind and the klaxon); the PNG +0.1, +1.4, +2.6, +4.0 s at 0.5.
- **sovereign-sweep-capture-r68-a**: run A +7.5 to +12.5 s, the play field (a laser battery's wedge telegraph and beam in the inner ring's hold, a rail line after it); the PNG +8.2, +8.5, +8.8 s at 0.6.
- **sovereign-rail-capture-r68-a**: run A +17.0 to +20.0 s, the play field (two rail batteries' lines telegraphed and fired at the ship from the turning middle ring); the PNG +17.3, +18.0, +18.8 s at 0.6.
- **sovereign-lock-capture-r68-a**: run A +30.5 to +37.5 s, 480×460 (the inner ring's last battery killed: the ring grinding to a halt, its lights out, tinted dark, the wrecks venting); the PNG +30.8 to +35.8 s at 0.5.
- **sovereign-opening-capture-r68-a**: run A +48.5 to +55.5 s, 480×460 (the outer ring's lock, the klaxon, the core's six petals sliding open over 3 s to the white-gold reactor); the PNG 160×160 on the core at 1.5x (+49.7 to +52.8 s).
- **sovereign-spiral-capture-r68-a**: run A +55.4 to +64.4 s, the play field (the open core's 2-arm spiral, 4 s on and 2 s off, under its hex shield; the shut core's fan of phases 1–3 is small beside the beam column and not picked out in a clip); the PNG +55.8 to +62.3 s at 0.5.
- **sovereign-bays-capture-r68-a**: run C 205.2–212.2 s (the core open at ≈ 203 s), 320×400 on the core's left (a rim bay's stream of four Darts flying out; small and dark on the dark rings); the PNG 205.8–207.0 s at 0.75.
- **sovereign-death-capture-r68-a**: run A +147.7 to +156.2 s, the play field (the kill, the cook-offs ring by ring, the implosion, the white flash, the segments drifting and fading within ≈ 1 s, the base disc; the `huge` rung); the PNG +148.8 to +161.9 s at 0.5.
- **sovereign-rings-capture-r68-a**: run A: the station at 1:1 in phase 1 (+10 s, the arc band lit on the lower rings), crops at 1:1 of the outer and middle rings with a SAM and a rail battery, the inner ring with the shut core, and the arc band under a laser battery's sweep (+24.6 s); no clip.

## iron-sovereign-capture-r69-a

Concept round 69, a capture of the game, not generated art (M9 part I, step I3, 2026-10-29): the game built by `:desktop:installDist` from branch `m9-act-5` (the working tree after steps FIX3, FIX4 and I1, the full `check` green) on a private 960×540 Xvfb display, `--bench <s> --settings <scratch>/settings.properties`, `--level 35 --difficulty medium --invulnerable`, the plan's fit of [iron-sovereign-capture-r68-a](#iron-sovereign-capture-r68-a) with Rook, `controls.auto-fire=true`, the ship still in the middle (**run A**; the level time from the relay's flash at t=61, the fight labelled in seconds from the halt). Clips: `ALSOFT_DRIVERS=wave` writing the game's own mix to a file, the screen recorded with ffmpeg `x11grab` at 30 fps, cut to the subject (≤ 1 s lead-in), cropped and muxed (H.264 main, AAC; the sound 0.47 s behind the recording, checked on the shots' cracks); checked on an eight-frame sheet. PNGs: frames of the same recording, labelled. Script: step I3's `clips69.py` (scratch).

- **sovereign-rail-capture-r69-a**: halt +16.7 to +19.9 s, the play field: a middle-ring rail battery's thin line telegraphed from its collar at halt +17.2 and fired at +17.7, its origin riding with the turning ring, the core rail's lines beside it (round 68 C.9 a as built in FIX3); the PNG 240×240 at 1:1 on the battery at +17.2, +17.5, +17.7, +17.8 s.

## sovereign-core-r70

Concept round 70, row A.2 (step R70, 2026-10-30): the core phase's three variants as storyboards, mockups over a real game
still, not production art. **Generator:** `python3 tools/concept/sovereign_core_r70.py` (its docstring lists the inputs,
the variants' numbers and the outputs; a few seconds). **Base:** `sovereign-core-capture-r70-a.png`, one 960×540 frame of
the game built by `:desktop:installDist` from branch `m9-act-5` (the working tree after step FIX6; the Sovereign's data as
built) on a private Xvfb display: `--bench 300 --settings <scratch>/settings.properties --level 35 --invulnerable
--loadout front=scatter-vulcan:4,rear=swarm-tail:3,left=micro-missile-pod:4,right=micro-missile-pod:4,generator=4,shield=2,armor=4
--escort rook:autocannon:1 --shots 18600` (`controls.auto-fire=true`, medium, the ship still; the scatter fit instead of
the Ion Beam so no beam column covers the arena), the open core at level tick 18600. The new elements are drawn at game
scale with the production sprites (the Dart Drone, the Minelayer's lit mine, the bays' glow, the cook-off debris) and the
game's code-drawn looks (the enemy `small` bullets, the Rail Bunker's red telegraph line); text in the game's fonts.

- **sovereign-core-r70-a** (the playbook): 1 the spiral after its white tell, 2 the rail volley's three lines fanned 22°
  through the ship's spot after three red petals, 3 the Dart surge's four bays lit and four streams of three, 4 the mine
  ring's amber tell and eight mines at 120–220 px, 5 two seeds' first nine draws (never the same twice running, the
  surge at most every third draw).
- **sovereign-core-r70-b** (the wreck throw): 1 a dead battery's wreck glowing gold with sparks, 2 lifted from its socket
  and aimed at the ship, 3 tumbling in flight while the next wreck glows, 4 a hit's debris and the second wreck in flight,
  5 two seeds' first six throws on the station.
- **sovereign-core-r70-c** (the core breaks loose): 1 the clamps' flashes and the gold target ring, 2 the reactor in
  flight over the empty cradle, 3 at a high stop firing its spiral and a bay's Darts, 4 at a low stop with a rail line,
  5 the five stops and two seeds' routes.
- **sovereign-core-r70-abc**: the three side by side at half size (one strip each).

**Round 70 outcome (2026-10-30):** a and b `chosen` (the combination: the playbook with the wreck throw as its fifth attack), c rejected: `sovereign-core-r70-c.png` is in `concept/rejected/`. Do not re-run `sovereign_core_r70.py` for c's file: it still writes to `concept/`.

## sovereign-playbook-capture-r71-a

Captures of the game, not generated art (step BOSS70, 2026-10-30; the main agent decides whether they go in a round): the
game built by `:desktop:installDist` from branch `m9-act-5` (the working tree after step BOSS70's module checks: round
70's HP shift and the core's playbook) on a private 960×540 Xvfb display, `--bench 290 --settings
<scratch>/settings.properties --level 35 --difficulty medium --invulnerable`, the plan's fit of
[iron-sovereign-capture-r68-a](#iron-sovereign-capture-r68-a) with Rook (`--loadout
front=ion-beam:4,rear=swarm-tail:3,left=micro-missile-pod:4,right=micro-missile-pod:4,generator=4,shield=2,armor=4
--special emp-burst:1 --escort rook:autocannon:1`), `controls.auto-fire=true`, the ship still in the middle (the game's
fixed seed: one run plays the draws wreck throw, spiral, wreck throw, spiral, wreck throw, rail volley, spiral, Dart
surge, mine ring, …; the core opens ≈ 102 s after the halt and dies ≈ 48 s later). Clips: `ALSOFT_DRIVERS=wave` writing
the game's own mix to a file, the screen recorded with ffmpeg `x11grab` at 30 fps, the sound 0.98 s behind the
recording (checked on the relay flash's crack), cut to one attack each (its 0.8 s tell, then the attack; ≤ 8 s),
cropped on the station and muxed (H.264 main, AAC); each checked on an eight-frame sheet. PNGs: four frames of the same
recording at 0.5 (labelled with the recording's seconds). Scripts: step BOSS70's `run.sh` and `sheet.py` (scratch).
The tells' sounds are existing ones (the Sentinel's charge, the SAM nest's lock, the Gunship's doors, the line mine's
blink, the walker wreck's groan; the wreck's tear-off the cable snap, its break the large crumble).

- **sovereign-playbook-spiral-capture-r71-a**: 7 s, 480×460: the reactor's white flare, then the 2-arm spiral for 4 s.
- **sovereign-playbook-rail-volley-capture-r71-a**: 4 s, the play field: three red petals at the core's foot pointing
  at the ship, the three red lines (the Rail Bunker's telegraph, 1 s), the three rails live.
- **sovereign-playbook-dart-surge-capture-r71-a**: 3.2 s, 440×400: the four rim bays lit gold, four streams of three
  Darts flying out (the pods and Rook take most of them as they leave).
- **sovereign-playbook-mine-ring-capture-r71-a**: 8 s, 480×460: the core's amber rim lights blinking, eight line mines
  thrown out to 120–220 px; the pods and the beam shoot most of them, which burst into their rings (the Minelayer's
  rule).
- **sovereign-playbook-wreck-throw-capture-r71-a**: 5 s, the play field: a dead battery's wreck glowing gold with
  sparks, torn off and hurled tumbling at the ship's spot, the next one glowing, the sockets left empty.

## sovereign-look-capture-r71-a

A capture of the game, not generated art (step PIX7, 2026-10-30): the game built by `:desktop:installDist` from branch
`m9-act-5` (the working tree after step PIX7: the rim and shade overlays regenerated by `python3 tools/art/iron_sovereign.py
overlays` at 448×448, drawn 1x, 32 undithered steps) on a private 960×540 Xvfb display, `--bench 400 --settings
<scratch>/settings.properties --level 35 --invulnerable --loadout
front=scatter-vulcan:4,rear=swarm-tail:3,left=micro-missile-pod:4,right=micro-missile-pod:4,generator=4,shield=2,armor=4
--escort rook:autocannon:1 --shots 6200,14020` (`controls.auto-fire=true`, medium, the ship still). The same run on
f1dc0f05 (built in a scratch worktree) gives the "before" crop. Top: the station at 1:1 at level tick 6200 (phase 1)
and 14020 (the open core, the rings locked); bottom: the upper-left rings (screen 300,20, 220 px square) at 2x, before
and after, labelled. Script: step PIX7's `still.py` (scratch).
