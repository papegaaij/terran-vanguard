---
title: Art direction
design: approved
implementation: n/a
art: chosen
updated: 2026-10-27
---

# Art direction

## Summary

The game looks like a late-90s shooter built from **pre-rendered CGI sprites**: 3D models
rendered to crisp, palette-limited 2D sprites with metallic highlights, as in Raptor: Call of the
Shadows and Tyrian 2000. This document fixes the screen geometry, sprite sizes, animation rules,
palette approach, the **parallax layer model** and the readability rules that every art asset
and every level must follow.

## Contents

| Part | Summary | Design | Impl | Art |
|---|---|---|---|---|
| [production](production/README.md) | Production art plan: what final means, pipeline to `assets/`, order of work, budgets | draft | in-progress | n/a |

## Design

### The pre-rendered look

- **Modelling.** Hard-surface models for the UTC and the Ascendancy, organic sculpted models for
  the Vrell. Silhouettes first: every sprite must be recognisable as a solid shape at native
  size. Detail comes from panel lines, greebles and material contrast, not from texture noise.
- **Camera.** Orthographic, looking straight down (top-down). No perspective on sprites. Tall
  ground structures are the exception, see *Perspective on the ground layer* below.
- **Lighting.** One warm **point key light at the top-left, above the model** (not a sun: a point
  light gives the characteristic gradient across flat hull plates), a weak cool fill from the
  bottom-right, soft ambient with ambient occlusion. Shadows fall **down-right**.
- **Materials.** Metals get a glossy Blinn-Phong highlight and a classic 90s chrome
  environment reflection (bright sky, dark horizon band, warm ground). Glass is dark and very
  glossy. Engines, cockpit lights and Vrell organs are emissive.
- **Render pipeline** (production and concept tools follow the same steps):
  1. Render at 4–8× native size with alpha.
  2. Box-filter down to native size (colour weighted by coverage).
  3. Hard 1-bit alpha at 50 % coverage (sprite colour key look, no soft edges).
  4. Mild unsharp mask for crispness.
  5. Median-cut palette: 24 colours for small sprites, 32–48 for large ones; no dithering on
     sprites (ordered dithering allowed on large gradients in backgrounds).
  6. Shadows are generated at runtime from the sprite alpha, never baked into the sprite.

  The concept generators in [tools/concept/](../../tools/concept/README.md) implement this with
  an SDF ray-marcher instead of polygon models.

### Resolution and screen geometry

| Item | Value |
|---|---|
| Internal resolution | **960×540** (16:9). Integer scaling with nearest-neighbour where it fits: 2× = 1080p, 4× = 4K. 1440p (2.67×) and 720p (1.33×) default to **letterboxed integer scaling** (crisp, black borders); **sharp-bilinear** (integer pre-scale, then bilinear to fit) is an option; letterbox for non-16:9 displays |
| Play field | **480×540** at x = 240…719 (portrait, 8:9) |
| Side panels | 240×540 each: left x = 0…239, right x = 720…959 (HUD, see `design/ui`) |
| Look-ahead | the player sits in the lower third; enemies entering from the top get about 375 px of warning |

The concept generators take these values from one place: `tools/concept/render/config.py`.

### Sprite sizes (native pixels)

| Class | Layer | Size |
|---|---|---|
| Player ship (AF-12 Stormhawk) | air | 48×48 |
| Wingman (Rook's craft) | air | 40×40 (smaller than the player, who stays dominant) |
| Drones | air | 30–36 |
| Tiny enemy (swarmers, seeds, motes) | air / low-air | 16–24 |
| Small enemy (darts, drones) | air / low-air | 28–48 |
| Medium enemy (gunships, walkers) | air / ground | 56–110 |
| Large enemy (carriers, frigates, mechs, segment chains) | air / ground / sub | 120–250 (a chain's total length; its segments are 24–48) |
| Huge enemy (leviathans, rotating platforms) | high-air / air / space | ≥ 1/3 of the play field (≥ 160 wide or ≥ 180 tall), may exceed it |
| Boss | any | 240–480 wide, multi-part, may exceed the play field |
| Ground turret / emplacement | ground | 24–36 |
| Vehicles, naval craft | ground / sub | 24–72 |
| Station / structure parts | ground / far | trusses 26–34 wide, modules 40–120, solar arrays up to 150 long |
| Enemy bullets | air | 8–13 (never smaller than 8) |
| Player shots | air | 5×16 typical, beams full height |
| Pickups | air | 18–24 |
| Explosions | air / ground | 24–144, additive |

All sizes are 1.5× the round 01 values (640×360). The enemy rows match the **size tiers** in
[enemies](../enemies/README.md#size-tiers).

### Animation rules

- **Banking.** The player ship and the wingman have 5 frames: hard left, left, centre, right,
  hard right (about ±14° and ±28° roll). The concept sheets show 3 (±28°). Frames change over
  about 6 game frames when steering, and return to centre when released.
- **Rotation.** Enemies that turn to face their movement are **pre-rendered at 16 angles**
  (22.5° steps); large or slow enemies and turret barrels use **32 angles** (11.25°). The game
  shows the frame nearest to the current heading; a turn plays through the in-between frames,
  never snapping more than one step per game frame. All rotations are separate renders from the
  3D model with the key light fixed at the top-left, so highlights and shadows stay put while the
  body turns; sprites are never rotated at runtime. Designs for turning enemies avoid a
  lighting-dependent "top": they must read from every angle.
- **Smooth slow rotation** (user feedback on round 06: the Halo Platform's rotation was "very
  jagged"). 16/32 angles only work for small sprites or quick turns. The angle step must move
  the sprite's outer edge by at most ~2 px per frame change: for a part at radius *r* px that is
  roughly 2π·r / 2 frames — a 150 px ring needs about **256 angles** (or a radially symmetric
  design so one symmetry step covers it: a 6-fold ring then needs ~43 unique frames). Large
  slowly rotating structures therefore get dense angle sets or a symmetric design; their
  rotation speed is chosen so a frame change never jumps more than one step.
- **Independent parts.** Turrets, heads and weapon arms on vehicles and walkers are separate
  sprites with their own angle set (e.g. a tank hull at 16 angles, its turret at 32), drawn on a
  pivot point defined per frame of the body.
- **Segment chains** (serpents, centipedes, drone trains): rendered **per segment type, not per
  pose** — head, body segment (one or two variants), tail, each at 16 angles (32 for large
  chains). At runtime every segment picks its angle from the path it is following, so any curve
  or loop comes for free. Segments overlap by 20–30 % of their length; a joint piece is only
  needed when segments would gap in tight turns. Body segments may carry a 2–4 frame ripple
  (legs, fins, glow pulse) offset per segment so the wave travels down the chain.
- **Articulated parts** (a leviathan's tail and fins, claws, ring sections): each part is its own
  sprite with a small angle range (typically ±30° in 8–12 steps) around a pivot, animated
  procedurally (sine sweeps, lagged follow-through). Destroyable parts also have a damaged frame
  and a wreck/stump frame.
- **Walk cycles.** Walkers use 8 frames per cycle for 2 legs (Strider) and 6–8 for 4–6 legs
  (Scuttler, Creeper), rendered at each of their 16 facing angles; centipede legs ripple as a
  2–4 frame segment animation. Feet plant on the ground layer (their speed matches the scroll
  plus their own speed), and heavy walkers leave footprints or dust on the ground layer.
- **Spinners.** Radially symmetric designs (4-, 5-, 6- or 8-fold) need only as many frames as
  one symmetry step: a 6-fold spinner at 32 steps per turn needs 6 unique frames. Spin speed is
  shown by frame rate (4–30 fps), with an optional motion-blur frame set above ~20 fps. The key
  light is fixed, so a spinner's highlight stays top-left while its body turns under it. Spinners
  with a single weak point off-axis use the full angle set instead.
- **Organic motion.** Vrell wing beats and pulsing glows at 8–12 fps; engine flicker 15–20 fps.
- **Water** (user feedback on round 06: the Kraken's and Driftjelly's water rings read as
  plain drawn circles, and the Kraken "just appears"). Wherever something meets the water
  surface:
  - **Waterline:** an irregular, animated foam collar where a body or limb breaks the surface —
    broken, uneven white/cyan foam that wobbles and sheds bits, never a clean outline.
  - **Ripples and wakes:** expanding ripple *trains* (several uneven rings that distort,
    break up and fade, with light/dark wave bands), splashes with spray particles, V-wakes
    behind moving parts. No single-pixel geometric circles.
  - **Below the surface:** submerged parts stay visible through the water — tinted towards the
    water colour, darker, blurred and slightly wavy — and connect continuously to the parts
    above it (an arm that dives in can be followed under water to the body).
  - **Surfacing / diving:** the highest parts break the surface first, then the rest follows
    from top to bottom (and the reverse when diving), with a swell and foam ring growing as it
    rises and water streaming off afterwards. Nothing pops in.
  - **How it is drawn** (user decision E4 = c of M5 part E, the hybrid): everything on the `sub`
    layer is drawn by one **generic pass** into an off-screen buffer and composited over the sea
    with a tint toward the water colour, darker, blurred and slightly wavy (Level 10's
    `FarsideLooks` light map is the precedent for the buffer), so every submerged unit, an arm's
    under-water stretch and the torpedoes get the same look and Levels 12–13 and Act 4 reuse it;
    the **foam collars, ripple trains and surfacing/diving steps** are pre-rendered per unit (in its
    frames, as the round 07 concepts were). Rejected: a (all at run time, collars generated from the
    sprite's alpha at the waterline: the most code and GL risk) and b (all baked: every unit's frame
    count ×2–3 and separate under-water segment sets for the Kraken's arms).
  - **On a water level** (a level's `water: true`, M5 part E): a kill on the play field splashes
    with a ripple train and sinks, leaving no crater or wreck; landing blasts (bombs, mortar shells,
    the Airstrike) splash instead of a ground blast; flyers cast their shadows onto the sea (the
    swell marks the shadow stencil, as the ground does).
  - **Under water** scenes (Europa) must read as under water at a glance: swaying vegetation
    (kelp, sea grass, anemones), rising air-bubble streams, drifting particles, caustic light
    patterns and light shafts, and fish.

- **Explosions.** Pre-rendered volumetric fireball sequences of 12–16 frames (additive), plus
  debris chunks and a shockwave ring for large kills. A 1–2 frame white **hit flash** on every
  damaged enemy (a large boss part flashes at most once each 0.25 s, see Decisions 2026-10-09).
- **Destruction states.** Ground structures and bosses get a damaged and a wrecked frame; wrecks
  stay on the ground layer.

### Palette approach

- Colours are organised as **6-step ramps** (dark background → highlight) per faction, per
  setting, for the UI and for bullets. The reference palette is **B "90s Neon CGI"**
  ([concept/palette-r01-b.png](concept/palette-r01-b.png)): saturated, glossy, violet-shadowed
  chrome. Its hex ramps also live in `tools/concept/render/palette.py`, which all generators use.
  Key ramps: UTC hull `121632 2A3068 4E5AA0 8A96D0 C8D0F4 FFFFFF`, UTC accents
  `0050FF 00A8FF 7FF0FF FF2A6A FF7A2A FFE04A`, enemy shots `300030 C000C0 FF40FF FFC0FF FFFFFF
  FFFF40`, player shots `002060 0060FF 00C0FF A0FFFF FFFFFF 40FF80`.
- Sprites are limited to 24–48 colours each, but there is no global 256-colour limit; the
  "limited" feel comes from the ramps.
- Because palette B is saturated everywhere, backgrounds must work harder to recede: they use
  the darker half of their setting ramp, are **posterized to 12–32 colours** (like palette-limited
  90s tile art; it also keeps them calm) and are hazed towards the setting colour with depth.
- **Faction visual language**
  - **UTC / CDF** (humanity): clean grey/white hard-surface hulls, blue and orange accents,
    panel lines, blue-white engines. Structures on the ground layer mute the orange (readability
    rule 7).
  - **Vrell** (aliens): grown chitin in violets and teals, no straight lines, wet specular,
    bioluminescent glows (teal and pink) that mark weak points.
  - **Jovian Ascendancy** (traitors): angular faceted hulls in black and gold, red sensor lights.
- **Settings** each get a ramp and a mood: Earth orbit (deep blues), Earth surface (greens,
  city nights in sodium orange), Mars (rust and ochre), Europa under the ice (teal-black
  abyss with caustics), asteroid belt (neutral rock greys), Jovian (banded ochres and storm
  browns), alien space beyond the gate (violets, magenta nebulae). The settings themselves are
  described in [design/world](../world/README.md).

### Parallax layer model

The play field is a stack of layers, drawn back to front. Scroll factors are relative to the
**ground layer**, whose speed is the level's scroll speed. Revised after round 01 (orbit "too
empty, ground scrolls too slowly"; city "too crowded"): a new `far` layer fills the gap between
deep and ground, the near layers are faster so speed is felt, and the ground itself scrolls
faster.

| Layer | Contents | Scroll factor | Enemies | Depth cues |
|---|---|---|---|---|
| `deep` | sky, planet surface far below, star fields, nebulae (may have sub-layers, e.g. Earth 0.12 with haze wisps 0.2) | 0.05–0.2 (default 0.12) | none | strongest haze towards the setting's atmosphere colour, desaturated, darker, low contrast, may be slightly blurred |
| `far` | distant structure seen through gaps in the ground: a sister station, a canyon floor, lower city levels, cloud decks | 0.4–0.7 (default 0.5) | none (background set pieces only) | 40–60 % haze towards the setting colour, smaller scale, posterized |
| `ground` | terrain, sea surface, city, station hulls, asteroid surfaces, capital-ship hulls | **1.0** | stationary: turrets, bunkers, tanks, surface ships, growths | full detail; catches shadows of everything above it |
| `sub` | under water: sea floor and submerged craft seen through the surface | 0.8–0.9 for sea-floor scenery; submerged units scroll with the sea (1.0) | submarines, mines, sea creatures (surface to attack) | blue-green tint, caustics, reduced contrast (the generic `sub` pass, see Water) |
| `low-air` | low flyers, traffic, drifting wreckage, dust plumes, low clouds and smoke | 1.3–1.5 (default 1.35) | low flyers | slightly larger than ground scale; shadow offset (9, 13) |
| `air` | the **play plane**: player, wingman, most enemies, all bullets, pickups | screen space | most enemies | shadow offset (21, 30) onto the ground layer |
| `high-air` | clouds, smoke, debris and ice streaks in front of the player; rare huge overhead passes (Leviathan, Brood Carrier) | 2.0–2.5 (default 2.2) | rare huge set-piece enemies only | weather and decoration: larger, blurred or drawn as motion streaks, **at most ~40 % opacity** over the play plane, never hides bullets; enemies: 75 % opacity (so the ship shows under them), scaled per the perspective rule, cast shadows |

**Deep layer coverage**: `deep` is the back of the stack and covers the whole screen from the
level start until the debrief (the outro after the level end included): every section has a tile
set on it, and wherever a set piece's edge crosses the screen the piece is transparent along
that edge (it fades out, or the edge stays off the screen); otherwise the tile set shows through
beyond the edge as a gap.

**Ground scroll speed** (960×540): calm levels 120–140 px/s (about a quarter screen per second),
normal 150–170 px/s, fast or chase levels 190–240 px/s. The fastest visible layer then moves
2–2.5× that, which is what sells the speed. Round 02 uses 190 (orbit), 140 (city) and
160 px/s (canyon).

**Density** (round 02: all three approaches liked): density follows the level's pace — calm
levels like scene B, normal levels like scene C, fast or chase levels like scene A. Always: one
dominant ground feature that defines clear lanes (a canyon, a station spine, avenues); two or
three mid-size set pieces per screen; background detail kept calm by posterized textures and
sparse light points; in a typical frame outside boss fights about 4–6 enemies and at most ~15
enemy bullets.

**Decoration** (user feedback on round 02: "the levels could use a bit more decoration"):
every scene gets a clearly visible **atmosphere layer** and, where the setting allows,
**organic detail**.
- Atmosphere: heavier cloud banks, mist or dust streaks. Dense, opaque banks go on `low-air`
  (below the play plane, so they never hide bullets); thinner wisps and streaks on `high-air`
  keep the ~40 % opacity cap. Per setting: clouds and haze (Earth, Earth orbit, Jupiter), dust
  storms and streaks (Mars, Luna regolith plumes), mist and fog (megacity, arctic), silt and
  bubbles (Europa), nebula wisps (space, Vrell space).
- Organic detail: vegetation on ground layers — parks, tree lines, fields and jungle on Earth,
  greenhouse domes and lichen fields around Mars colonies, kelp and coral-like growth under
  Europa's ice, Vrell biomass (creep, spore fields) on infested areas.
- Decoration never competes with gameplay: it stays lower in contrast and saturation than
  enemies and bullets.
- **Motion budget** (user feedback on round 09: the storm and, less so, the ocean were "heavy on
  the eyes — too much is moving at the same time"). At most **two** strongly animated
  background elements at once (e.g. waves and rain — not waves, rain, scud and background all at
  full strength); everything else moves slowly or not at all. Repetitive fine motion (rain,
  wave crests, wind streaks) stays low in contrast and amplitude, and motion must be smooth —
  no element may jump by more than ~2 px between frames. Intensity peaks (storm, lightning)
  are short, as with the atmosphere-intensity rule.
- **Atmosphere intensity varies through a level** (user feedback on round 03): the round-03
  scenes show the *heavy* end and must not be used like that for a whole level. Each level
  section gets an intensity — `clear` (no banks, a few wisps), `light` (~10–15 % bank
  coverage), `medium` (~20–25 %) or `heavy` (25–40 %, the round-03 look). Most of a level is
  clear to medium; heavy is a short peak (a storm front, a fog bank before an ambush), and
  transitions ramp over several seconds rather than switching. Vegetation and other organic
  detail follow the terrain, not this curve.
- Rules learned in round 03:
  - **Coverage**: low-air banks cover roughly 25–40 % of the screen and leave the lanes of the
    level (avenues, canyon, spine gaps) readable most of the time.
  - **Tones**: mix the setting ramp towards a neutral (grey for clouds and fog, ochre for Mars
    dust) and keep the brightest bank tone clearly below the player's hull in value. Light
    tints must not land in a reserved hue: sodium orange over violet fog turns pink, so fog
    uses a greyed base with an amber glow.
  - **Shape**: clouds are billowy (lighting from a blurred density, lit towards the top-left,
    shadowed bottom-right); fog and dust banks are stretched 2–4× along the wind so they read
    as rolling banks rather than blobs.
  - **Depth**: a bank shadows the ground below it (offset (10, 14), 25–40 %), and flyers cast a
    second shadow onto bank tops at a smaller offset (12, 17) as well as onto the ground.
  - **Limited colour**: banks are posterized to about 12 colours and their alpha stepped to about
    6 levels, like 90s translucency tables; thin veils and wisps need finer steps (about 10) or
    they break into hard-edged patches. This also keeps scrolling layers cheap.
  - **Vegetation**: low-value greens (night parks and trees at about 55–60 % brightness;
    Mars lichen as a dark olive with ragged, speckled edges); trees are small pre-rendered
    canopy sprites; tree rows stop at crossings.
  - When a scene is reduced to a small palette (e.g. a GIF preview), reserve the bullet, shot
    and Vrell glow colours and give key sprites a share of the palette.

- In **space** levels there is no terrain: stations, asteroids and capital ships take the ground
  role at 1.0, and the deep layer becomes several star-field and nebula layers.
- In **under-water** levels (Europa) the sea floor is the ground layer and submerged enemies live
  in `sub`; what can hit them is a gameplay rule (see Open questions).
- **Shadows**: every flyer casts its silhouette down-right onto the ground layer (opacity
  45–55 %, 1–1.5 px blur, 85 % scale); in space only onto structures. Tall parts of the ground
  layer (modules on a station) also cast a short shadow (6, 8) onto the parts below them.
- **Perspective between layers**: anything that connects two layers is drawn in true
  perspective around the screen centre, where a layer with scroll factor *k* is also drawn at
  scale *k*. Tall structures (towers, spires, masts) push their roofs away from the centre
  (camera height model, see parallax B). Cliffs seen from above (a plateau edge, a crater rim)
  run from their top at scale 1.0 down to the floor at the `far` scale (0.6 in parallax C).
  **Canyons the ship flies through** are the exception (user decision MA.1 of round 36,
  2026-10-13, amending this rule for canyons): the canyon floor is the ground layer at 1.0, and the
  walls rise from it toward the camera to the plateau at about 1.25, leaning outward from the centre
  like the perspective towers below; the **foot** of a wall is the collision line, and by the tower
  rule nothing gameplay touches stands on a leaning face or on the plateau (Level 16, see
  [Mars](../world/mars/README.md#parallax-layers)). As they scroll, walls turn, which gives real
  depth. Everything else is orthographic.
- **Perspective towers are scenery** (user decision D1 of M5 part B, 2026-10-06): the towers of a
  city (Level 08's Nova Lagos first, then Levels 09 and 14) are drawn in true perspective at run
  time, a pre-rendered roof and textured walls per tower, projected each frame round the play
  field's (240, 297) with the camera model of parallax B (a roof at height *h* drawn at
  *k* = 6 ÷ (6 − *h*), *h* up to about 1.35, so a roof moves up to 1.29× the ground's scroll and
  leans up to about 70 px outward at the field's edge). Nothing gameplay touches stands on a
  tower: turrets, walkers, ground targets, triggers and crates sit at **street level** or on
  **low structures drawn without lean** (parking decks, landing pads, low roofs and podiums), and
  walkers keep to streets, ramps and low roofs (no wall crawling). Ground units, ground targets
  and bullets are drawn over the towers, so a leaning wall never hides one; levels keep walker
  paths and targets clear of where the walls lean, so this is seldom seen. Flyers' shadows fall
  on walls and roofs at the ground offset. The data and renderer are in
  [architecture](../tech/architecture/schemas-backdrops/README.md) (tower pieces).
- **Kit-bashed structures**: stations, colonies and similar structures are assembled from a kit
  of pre-rendered parts (trusses, modules, arrays, domes) rather than rendered as one image, as
  90s tile sets were. The concept tools do this with `tools/concept/render/station.py`.

### Readability rules

1. **Enemy bullets pop on every background**: bright white core, saturated ring, 1 px dark rim.
   Their hues (magenta/pink, orange) are reserved: no background uses them at that saturation.
   What sets them apart is the **look, not only the hue**: a bullet is saturated, glows and has a
   bright core, while the warm markings of loot targets (rule 7) are dull matte amber with black
   hazard stripes and never glow. Minimum 8 px. Each bullet type has its own shape (orb, needle, ring, beam). In palette B
   the orbs are magenta `FF40FF` and the needles yellow `FFFF40`, both on a `300030` rim.
2. **Player shots** are blue / white / cyan and may be semi-transparent; they never share a hue
   with enemy bullets.
3. **Backgrounds recede**: they are darker (about 70–80 % brightness) and less saturated than
   the sprites above them. The concept tools do this with `terrain.recede()`.
4. **Enemies carry a bright accent** (Vrell glow, UTC stripe, Ascendancy red light) so that a
   dark enemy over a dark background is still found at a glance.
5. **High-air weather and decoration** stay below ~40 % opacity where they overlap the play
   plane. Enemies on `high-air` (the Leviathan's overhead pass, the Brood Carrier's pass) are
   the exception: drawn at 75 % opacity, reaching full opacity as they descend to the play
   plane, scaled per the perspective rule, and they cast shadows.
6. **Pickups** pulse and have a light outline. The player's hit box follows the hull's
   silhouette, so what visibly touches the ship hits it (a gameplay rule in the
   [ship](../player/ship/README.md) doc); bullets must therefore read clearly against the hull.
7. **Loot targets read as targets.** Destructible targets that pay loot (cargo containers,
   crates) and secrets (e.g. Level 01's crane beacon) never blend into the structures they sit
   on: **warm cargo colours** (amber/orange hazard markings) against the cool blue-grey
   structures, a **1 px light rim**, a short **glint about every 2 s**, a white **hit flash** and
   a visible **damage state** from the first hit, and a clear **break-apart** when destroyed.
   Secrets additionally **blink** and are at least as large as a container's marking.
   - **Warm amber on the ground layer is reserved for loot targets.** Structures and scenery on
     it use muted accents instead: rust, beige or desaturated ochre, well below the loot amber in
     saturation and brightness. The UTC kit's orange accent (palette B `ff7a2a`) becomes beige
     ochre `a48366` there (palette B's JOVIAN `ffb840` mixed 40/60 with ASTEROID BELT `686080`);
     small lit lamps and red warning lights stay.
   - **Loot markings are matte, bullets glow** (see rule 1): the markings are dull amber
     (`faa824` in the placeholders) under black hazard stripes, lit like the rest of the target;
     only the glint shines. They never get a bright core or a glow.

## Concept art

Rejected proposals (14 rows) are listed in [concept/rejected/README.md](concept/rejected/README.md).

Concept round 01 (prompts: [concept/prompts.md](concept/prompts.md)):

| File | What | Status |
|---|---|---|
| [concept/palette-r01-b.png](concept/palette-r01-b.png) | Palette B "90s Neon CGI" | chosen |

Round 01 also covered the [player ship](../player/ship/README.md), the
[HUD](../ui/hud/README.md) and the [title logo](../ui/main-menu/README.md); outcomes are
recorded in those documents.

Concept round 02, at 960×540 in palette B with ship A (prompts:
[concept/prompts.md](concept/prompts.md); generator `tools/concept/parallax_r02.py`). Each GIF
is a seamless 4 s loop (80 frames at 20 fps) at native 480×540:

| File | What | Status |
|---|---|---|
| [concept/parallax-r02-a.png](concept/parallax-r02-a.png) | Parallax A "Earth orbit, fuller and faster": play field at 1× plus a breakdown of 6 layers (Earth, sister station, main station kit-bashed from parts, wreckage, play plane, streaks); ground 190 px/s | chosen — approach; needs more decoration (round 03) |
| [concept/parallax-r02-a.gif](concept/parallax-r02-a.gif) | Parallax A: scroll loop | chosen — approach; needs more decoration (round 03) |
| [concept/parallax-r02-b.png](concept/parallax-r02-b.png) | Parallax B "Night megacity, calm": lower-contrast towers, two avenues as lanes, parks, sparse lights, 3 darts and one turret; ground 140 px/s | chosen — approach; needs more decoration (round 03) |
| [concept/parallax-r02-b.gif](concept/parallax-r02-b.gif) | Parallax B: scroll loop | chosen — approach; needs more decoration (round 03) |
| [concept/parallax-r02-c.png](concept/parallax-r02-c.png) | Parallax C "Mars canyon, balanced": canyon floor as a far layer with perspective strata walls, plateau with a colony outpost and Vrell pods, dust plumes and streaks; ground 160 px/s | chosen — approach; needs more decoration (round 03) |
| [concept/parallax-r02-c.gif](concept/parallax-r02-c.gif) | Parallax C: scroll loop | chosen — approach; needs more decoration (round 03) |

Concept round 03, decoration pass on the round 02 scenes (prompts:
[concept/prompts.md](concept/prompts.md); generator `tools/concept/parallax_r03.py`, which
subclasses the round 02 scenes). The GIF loops use reduced palettes (72–128 colours) to stay
under ~8 MB; the PNG sheets show full colour:

| File | What | Status |
|---|---|---|
| [concept/parallax-r03-a.png](concept/parallax-r03-a.png) | Parallax A + decoration: a cyclone and cloud fronts on the deep Earth, pale cloud decks on low-air drifting between the station and the play plane (shadowing the station, catching flyer shadows), haze wisps and ice streaks on high-air | chosen — shows the heavy end of the atmosphere range |
| [concept/parallax-r03-a.gif](concept/parallax-r03-a.gif) | Parallax A + decoration: scroll loop | chosen — shows the heavy end of the atmosphere range |
| [concept/parallax-r03-b.png](concept/parallax-r03-b.png) | Parallax B + decoration: tree-lined avenues, more and richer parks (paths, ponds), rooftop gardens, rolling fog banks on low-air lit amber by the avenue lamps; still calm | chosen — shows the heavy end of the atmosphere range |
| [concept/parallax-r03-b.gif](concept/parallax-r03-b.gif) | Parallax B + decoration: scroll loop | chosen — shows the heavy end of the atmosphere range |
| [concept/parallax-r03-c.png](concept/parallax-r03-c.png) | Parallax C + decoration: ochre dust-storm banks on low-air, dust veil and heavier streaks on high-air, greenhouse tunnels, algae ponds and lichen fields around the colony | chosen — shows the heavy end of the atmosphere range |
| [concept/parallax-r03-c.gif](concept/parallax-r03-c.gif) | Parallax C + decoration: scroll loop | chosen — shows the heavy end of the atmosphere range |

Concept [round 06](../concept-rounds/round-06/README.md) — five more settings at `medium` atmosphere intensity; generator `tools/concept/scenes_r06.py`. The GIFs posterize the base terrain to stay under 8 MB; the PNG sheets show full colour.

| File | What | Status |
|---|---|---|
| [concept/scene-luna-r06-a.png](concept/scene-luna-r06-a.png) | Luna — regolith with earthshine-tinted crater shadows, Tranquility Base, mass-driver rail, Vrell nest crater, regolith plumes (sheet + layer breakdown) | chosen |
| [concept/scene-luna-r06-a.gif](concept/scene-luna-r06-a.gif) | Luna: seamless scroll loop | chosen |
| [concept/scene-belt-r06-a.png](concept/scene-belt-r06-a.png) | Asteroid belt — refinery pit and conveyors on a large rock, Helix block with Rail Bunker, tumbling rocks and haulers, ricocheting Buzzsaw (sheet + layer breakdown) | chosen |
| [concept/scene-belt-r06-a.gif](concept/scene-belt-r06-a.gif) | Asteroid belt: seamless scroll loop | chosen |
| [concept/scene-jovian-r06-a.png](concept/scene-jovian-r06-a.png) | Jupiter — Aurelia's Art Deco decks over cloud bands and a storm vortex, anti-grav ring and balloons, lightning flash (sheet + layer breakdown) | chosen |
| [concept/scene-jovian-r06-a.gif](concept/scene-jovian-r06-a.gif) | Jupiter: seamless scroll loop | chosen |
| [concept/scene-vrell-space-r06-a.png](concept/scene-vrell-space-r06-a.png) | Vrell space — violet/teal nebula, hive surface with glowing veins and spawning pits, spore sacs and tendrils, Coilwyrm on a figure-8 (sheet + layer breakdown) | chosen |
| [concept/scene-vrell-space-r06-a.gif](concept/scene-vrell-space-r06-a.gif) | Vrell space: seamless scroll loop | chosen |

Concept [round 07](../concept-rounds/round-07/README.md) — Europa redone to read as under water (new Water rules); generator `tools/concept/scenes_r07.py`.

| File | What | Status |
|---|---|---|
| [concept/scene-europa-r07-a.png](concept/scene-europa-r07-a.png) | Europa A "kelp forest" — swaying kelp stands and sea grass, anemones and fan corals, caustic light on the floor, rising bubble streams, fish schools, marine snow, light shafts (sheet + layer breakdown) | chosen |
| [concept/scene-europa-r07-a.gif](concept/scene-europa-r07-a.gif) | Europa A: seamless scroll loop | chosen |

Concept [round 08](../concept-rounds/round-08/README.md) — Earth open ocean (L11) under the Water rules; generator `tools/concept/scenes_r08.py` (storm, arctic, Geneva and Luna far side are implemented there but not rendered yet).

| File | What | Status |
|---|---|---|
| [concept/scene-ocean-r08-a.png](concept/scene-ocean-r08-a.png) | Open ocean (L11) — convoy holding station on an overcast sea, wakes and foam in the surface, surfaced and submerged Driftjellies, Reef Spitter rafts, a dark shape below, sea mist (sheet + layer breakdown) | superseded by r09 |
| [concept/scene-ocean-r08-a.gif](concept/scene-ocean-r08-a.gif) | Open ocean: seamless scroll loop | superseded by r09 |

Concept [round 09](../concept-rounds/round-09/README.md) — Acts 1–2 scenes at medium atmosphere (storm peaks heavy), generator `tools/concept/scenes_r09.py`; explosions, generator `tools/concept/vfx_r09.py`.

| File | What | Status |
|---|---|---|
| [concept/scene-ocean-r09-a.png](concept/scene-ocean-r09-a.png) | Open ocean (L11), finished: broken-up wakes, streaky prop-wash, swell-colour depth, darker mist, larger shape under the waves (sheet) | superseded by r10 |
| [concept/scene-ocean-r09-a.gif](concept/scene-ocean-r09-a.gif) | Open ocean (L11), finished: broken-up wakes, streaky prop-wash, swell-colour depth, darker mist, larger shape under the waves (motion) | superseded by r10 |
| [concept/scene-storm-r09-a.png](concept/scene-storm-r09-a.png) | Ocean storm (L12), heavy peak: dark swell, torn whitecaps, wind streaks, rain, scud, a lightning flash (sheet) | superseded by r10 |
| [concept/scene-storm-r09-a.gif](concept/scene-storm-r09-a.gif) | Ocean storm (L12), heavy peak: dark swell, torn whitecaps, wind streaks, rain, scud, a lightning flash (motion) | superseded by r10 |
| [concept/scene-arctic-r09-a.png](concept/scene-arctic-r09-a.png) | Arctic floes (L13): open leads, teal submerged ice, waterline foam, Skimmers with V-wakes, the relay, thin fog (sheet) | chosen |
| [concept/scene-arctic-r09-a.gif](concept/scene-arctic-r09-a.gif) | Arctic floes (L13): open leads, teal submerged ice, waterline foam, Skimmers with V-wakes, the relay, thin fog (motion) | chosen |
| [concept/scene-geneva-r09-a.png](concept/scene-geneva-r09-a.png) | Geneva under the Vrell canopy (L14): old-town blocks, sodium lamps, lake, rotunda, roots and creep, canopy shadow, Hive Node (sheet) | superseded by r10 |
| [concept/scene-geneva-r09-a.gif](concept/scene-geneva-r09-a.gif) | Geneva under the Vrell canopy (L14): old-town blocks, sodium lamps, lake, rotunda, roots and creep, canopy shadow, Hive Node (motion) | superseded by r10 |
| [concept/scene-luna-farside-r09-a.png](concept/scene-luna-farside-r09-a.png) | Luna far side (L06): dark regolith lit by flares, dome lights, Vrell glow and the headlight; Mantis and Coilwyrm (sheet) | chosen |
| [concept/scene-luna-farside-r09-a.gif](concept/scene-luna-farside-r09-a.gif) | Luna far side (L06): dark regolith lit by flares, dome lights, Vrell glow and the headlight; Mantis and Coilwyrm (motion) | chosen |
| [concept/explosions-r09-a.png](concept/explosions-r09-a.png) | Explosions: size ladder 24–144 px, Vrell organic vs Ascendancy metal, water-surface and under-water, hit flash, shield hit and break (sheet) | chosen |
| [concept/explosions-r09-a.gif](concept/explosions-r09-a.gif) | Explosions: size ladder 24–144 px, Vrell organic vs Ascendancy metal, water-surface and under-water, hit flash, shield hit and break (motion) | chosen |

Production art for concept round 12 (the Level 01 batch; part P2 opens the round), review files built from the final frames in `assets/` by `tools/art/explosions.py` (`--review` rebuilds only them); prompts: [concept/prompts.md](concept/prompts.md).

| File | What | Status |
|---|---|---|
| [concept/explosions-final-r12-a.png](concept/explosions-final-r12-a.png) | Final fireball frames of the ladder up to large: tiny 24 px / 12, small 40 / 12, medium 64 / 14, large 96 / 14 frames, additive | chosen |
| [concept/explosions-final-r12-a.gif](concept/explosions-final-r12-a.gif) | The four rungs side by side | chosen |

Concept [round 10](../concept-rounds/round-10/README.md) — revisions under the motion-budget rule; generator `tools/concept/scenes_r10.py`.

| File | What | Status |
|---|---|---|
| [concept/scene-geneva-r10-a.png](concept/scene-geneva-r10-a.png) | Geneva (L14) rebuilt as a city from above: street network with lamps and traffic, perimeter blocks of row houses with ridged roofs, lake with the Jet d'Eau, Rhône bridges, Saint-Pierre, the rotunda, parks; Vrell overgrowth layered on top (sheet) | chosen |
| [concept/scene-geneva-r10-a.gif](concept/scene-geneva-r10-a.gif) | Geneva: seamless loop | chosen |
| [concept/scene-storm-r10-a.png](concept/scene-storm-r10-a.png) | Storm (L12) within the motion budget: sea and rain as the only strong motion, slower scroll and swell, faint motion-blurred rain, swaying scud, one lightning flash; 25 fps (sheet) | chosen |
| [concept/scene-storm-r10-a.gif](concept/scene-storm-r10-a.gif) | Storm: seamless loop | chosen |
| [concept/scene-ocean-r10-a.png](concept/scene-ocean-r10-a.png) | Ocean (L11) within the motion budget: sea and wakes as the strong motion, slower swell, lower contrast, fainter whitecaps and wisps; 25 fps (sheet) | chosen |
| [concept/scene-ocean-r10-a.gif](concept/scene-ocean-r10-a.gif) | Ocean: seamless loop | chosen |

Concept round 30 (M5 part B), the perspective towers' renderer test: a capture of the game, not
production art; placeholder kit by `tools/concept/towers_r30.py` (prompts:
[concept/prompts.md](concept/prompts.md#towers-capture-r30-a)).

| File | What | Status |
|---|---|---|
| [concept/towers-capture-r30-a.png](concept/towers-capture-r30-a.png) | The runtime tower projection in a scratch copy of Level 04's first section (placeholder streets, roofs and window walls in palette B; 114 towers, *h* 0.3–1.35): roofs leaning out from (240, 297), the walls that face the centre with true-perspective window rows, shaded right and down; the convoy, the ship and a flyer with its shadow drawn over the tower district | chosen |

Concept round 33 (M5 part E, Level 11), production art straight from the chosen concepts and the
Harbour Kraken's lane telegraph and slam look as an a/b (user decision E9 = a); generators
`tools/art/water_fx.py` and `tools/art/l11_props.py` (prompts and briefs:
[concept/prompts.md](concept/prompts.md#water-fx-final-r33-a)). The convoy's ship pair is in
[allies](../allies/README.md#concept-art). Closed 2026-10-09 (user): the water and torpedo effects and
the Level 11 props approved as final; the lane telegraph and slam **a** "edge dashes + spray sheets"
(already in the game) approved as final, b in `concept/rejected/`.

| File | What | Status |
|---|---|---|
| [concept/water-fx-final-r33-a.png](concept/water-fx-final-r33-a.png) | Water and torpedo effects, final frames: the water-surface and under-water explosion ladders (24–96 px), splash, ripple train, foam collars, foam strip, small craft wake, the torpedo at 32 headings with its bubble puff and drop splash (sheet) | chosen |
| [concept/water-fx-final-r33-a.gif](concept/water-fx-final-r33-a.gif) | The same on the ocean stand-in: kills on and under the water, a collar, a wake, torpedoes hitting a submerged dummy (motion) | chosen |
| [concept/underwater-burst-final-r52-a.png](concept/underwater-burst-final-r52-a.png) | Under-water kill bursts, proposal **a** "pressure flash" (`tools/art/explosions.py --deep a`): the four rungs' light (a white-teal flash, the pressure ring) and the dark cavity under it; written to `assets/` until the pick (sheet) | chosen (round 52, F.7 = a: the "pressure flash", accepted as final) |
| [concept/underwater-burst-final-r52-a.gif](concept/underwater-burst-final-r52-a.gif) | a's four rungs side by side over the farm floor, 3x | chosen (round 52, F.7 = a: the "pressure flash", accepted as final) |
| [concept/slam-r33-a.png](concept/slam-r33-a.png) | Kraken lane telegraph and slam a, "edge dashes and spray sheets": a boiling band on the lane's centre line, red dashes down both edges, spray sheets thrown to both sides (sheet; in the game until the pick) | chosen |
| [concept/slam-r33-a.gif](concept/slam-r33-a.gif) | The same in two arena lanes with a cargo ship and a stand-in arm: telegraph, rise, impact, awash, sink (motion) | chosen |
| [concept/l11-props-final-r33-a.png](concept/l11-props-final-r33-a.png) | Level 11's props, final frames: floating container with collar and its sinking, the sunken supply pod on its reef root (snagged, hit, freed) and its rise, and the backdrop pieces for step E3b: the burning freighter, three reef growths and a reef root (sheet) | final (round 44, X.i) |
| [concept/l11-props-final-r33-a.gif](concept/l11-props-final-r33-a.gif) | The same on the ocean stand-in: the reef line, the pod freed by torpedoes and rising to the crate, containers drifting and one sinking, the freighter passing (motion) | final (round 44, X.i) |

## Implementation

- [x] Renderer draws the screen at 960×540 and scales by integer factors with letterboxing
      (sharp-bilinear for 1440p and 720p).
- [x] Layer stack (deep, far, ground, low-air, air, high-air) with per-layer scroll factors as in
      the table and a ground scroll speed, configurable per level (each level's `backdrop`:
      `scroll_factors`, `BackdropLayer`, `BackdropCheck`).
- [ ] The `sub` layer (M5 part E, Level 11; E4 = c): drawn deep → `sub` → ground, everything on it
      through the generic tint, blur and wave pass; checked in the xvfb capture and CI's smoke test
      on three OSes (step E3c; see [enemies](../enemies/README.md#implementation)). Built in step
      E3c (the pass and its fallback, see *Decisions*); it ran under xvfb and in the round-33 captures
      (accepted 2026-10-09), the three-OS check waits for CI's smoke test.
- [x] The water rules on a water level (M5 part E, Level 11): animated foam collars at every
      surface contact (jellies, rafts, reefs, the Kraken's parts, ships, containers), ripple trains,
      splashes with spray, V-wakes and prop-wash on the ships; surfacing and diving top-down from the
      units' pre-rendered steps; kills on water sinking without a crater or wreck; landing blasts
      splashing; flyers' shadows on the sea (steps E1b, E1c and E3c; the reefs' collars are the
      backdrop pieces' own, the shadows fall on the sea's chop, which marks the shadow stencil
      everywhere).
- [x] Perspective geometry between layers for canyon walls and cliffs (M7 part C; the
      canyon walls of L16 rising toward the camera, collision at their foot: MA.1 = b of round 36;
      M7 part C).
- [x] Runtime drop shadows from sprite alpha, offset per layer, masked to shadow-catching layers
      (`Shadows`: the flyers, the Coilwyrm's segments and the ship; the ground layer's tiles, road
      and pieces mark the stencil buffer). The flyers' second shadow on low-air bank tops (12, 17)
      and the 1–1.5 px blur are not drawn.
- [x] Perspective towers as scenery (D1 of M5 part B): tower pieces with a roof, textured walls
      and a height, projected each frame round (240, 297) at *k* = 6 ÷ (6 − *h*), drawn under the
      ground units, marking the shadow stencil, their lean checked against the motion budget
      (`BackdropData.Tower`, `BackdropCheck`, `TowerProjection`, the test capture
      `towers-capture-r30-a`); Level 08's tower district in production (`tools/art/backdrop_l08.py`,
      approved as final in concept round 30), reused by Levels 09 and 14.
- [x] High-air weather and decoration opacity capped where it overlaps the play plane; high-air
  enemies drawn at 75 % opacity, perspective-scaled, with shadows (the weather at 40 %; the
  Leviathan from M4 part C, the Brood Carrier with its shadow in part G, approved in round 25).
- [x] Bullet sprites follow the readability rules (core, ring, dark rim, reserved hues): the
      `small` orb (final in round 12) and the player
      shots (round 14); the fast class's needle is final too, ready for the first fast shot.
- [x] The large pulsing orb for `medium` bullets (`BulletLooks`, frames by
      `tools/art/bullet_medium.py`, approved as final in round 26): the Leviathan's and the Scuttler's aimed
      shots; the Spore Bomber's `medium` is its spore, drawn as the mine.
- [ ] The homing diamond — **later: Act 5** (Act 4's homing projectile, the Depth Hunter's torpedo at L27, is drawn as
      its own torpedo sprite with its wake, round 48 b, accepted in round 57; the diamond waits for the first homing
      enemy *bullet*). Draft (gap analysis 2026-10-27, pending the user's review in round 60): the Ascendancy's shootable
      homing missiles of Act 5 (the [Hornet](../enemies/air/hornet/README.md)'s salvos, the SAM Nest's, the Strider's pod,
      the Iron Sovereign's SAM batteries) are drawn as the bullet rules' **diamond** in red-gold with a short thin exhaust,
      its hit box the diamond's core; proposed in round 60, built in M9 part A. The Revenant Walker's missiles and the
      Depth Hunter's torpedo keep their own final sprites.
- [x] Hit flash and explosion sequences as described under Animation rules (`FlashShader`'s white
      flash; `explosion-tiny` to `-large`, the debris chunks and each unit's own death).
- [x] Loot targets follow readability rule 7 (Level 01's cargo containers and beacon, placeholder
      sprites from `tools/concept/ground_targets.py`).
- [x] Production sprite pipeline (render → downsample → 1-bit alpha → sharpen → palette) is
      scripted so every asset is reproducible, like the concept tools
      ([tools/art/](../../tools/art/README.md)).

## Open questions

- None open.

## Decisions

Entries dated before 2026-10-21 (M8's start): [decisions-archive.md](decisions-archive.md).

- 2026-10-22 (M8 part C, gap check D8, steps ART-C and C4; accepted in round 52 (2026-10-22)): kills below the waterline
  take their own bursts, no smoke: **a** "pressure flash" (a white-teal flash, the pressure ring, a dark cavity under it)
  or **b** "quenched fire" (a short orange fireball choked into a teal glow, slow embers, a silt puff); a plays in the
  game (`tools/art/explosions.py --deep a` in `assets/`) until round 52's pick.
- 2026-10-22 (user, round 52, closed; F.7 = a): the under-water kill bursts are **a**, the "pressure flash" (a white-teal flash, the pressure ring, the dark cavity; already in the game); b, the "quenched fire", rejected (its sheet and GIF moved to `concept/rejected/`). The `--deep b` frames are not in `assets/`.
- 2026-10-26 (user, [round 59](../concept-rounds/round-59/README.md) closed, C.7): the homing diamond moves to Act 5, to be
  drawn with the first homing enemy bullet; Act 4's only homing projectile keeps its own torpedo sprite.
- 2026-10-27 (step m9-d-enemies-a, Act 5 design phase; pending the user's review in round 60): the homing diamond is the
  look of every Ascendancy homing missile from Act 5 (red-gold, a short thin exhaust); its proposal goes to round 60's
  unit sheets and its build to M9 part A, which ticks the item.
