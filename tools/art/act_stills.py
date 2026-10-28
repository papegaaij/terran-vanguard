#!/usr/bin/env python3
"""Production art: the still behind an act's title card (design/campaign/act-1-first-contact, Act
intro and outro: "over a still of the Gagarin shipyards at dawn, Earth's terminator behind";
design/ui/briefing, the act title card act-title-r08-a; M4 part H batch, concept round 26).

Outputs (assets/ui/, a texture of its own like the title scene, not packed):
  act-1-first-contact-still.png   960x540: the Gagarin yards from above at dawn, as Level 01 shows
                                  them, over Earth's day side with the sunrise terminator running
                                  across behind them (the night side and its city lights bottom
                                  left, the dawn band warm along the line); the south launch rail
                                  with Lancer's Stormhawk on it on the left, the north arm with
                                  Aegis Two launching beyond it, a bridge crane over the top, open
                                  dock frames and a burning platform along the bottom, the cruiser
                                  hull on the right; the middle kept open for the chrome lettering
  design/campaign/act-1-first-contact/concept/act-1-still-final-r26-a.png   review sheet: the still,
                                  and the title card as the game draws it over it (no GIF: a still)
  act-2-homefront-still.png       960x540 (M5 part B batch, concept round 30): Nova Lagos at night
                                  from above, under the landing, as Level 08 shows it: its avenues
                                  and rooftops, towers with their walls projected from the still's
                                  middle, the harbour stepping down to the Gulf of Guinea bottom left
                                  (the quays, piers, a container ship, cranes, container yards), fires
                                  on the yards and two burnt blocks with their smoke columns leaning
                                  north east, the fog banks and mist; a cloud deck over the top and the
                                  right, lit from below; eight landers (small Vrell seed pods, their
                                  heat shields glowing) coming down through it from the top right on
                                  their smoke trails, kept off the lettering
  design/campaign/act-2-homefront/concept/act-2-still-final-r30-a.png   its review sheet, as Act 1's
  act-3-red-dust-still.png        960x540 (M7 part B batch, concept round 38): Mars from the entry corridor,
                                  Level 15's deep layer in perspective from high up: the Tharsis plain
                                  (`mars-high`, broad light and dark regions over its tiling) running to the
                                  curved horizon, Olympus Mons (`olympus`, 2.2x) far below in the lower middle
                                  under the lettering, the cloud deck's banks (`deck-banks`) on a nearer plane in
                                  the middle distance with their shadows on the plain, the thin cirrus (`cirrus`,
                                  additive at 40 %) nearest; the haze thickening to the butterscotch sky, the limb
                                  line, the sky dark above; a dust-storm front rolling along the northern horizon
                                  on the right (drawn here, no piece)
  design/campaign/act-3-red-dust/concept/act-3-still-final-r38-a.png   its review sheet, as Act 1's
  act-4-deep-water-still.png      960x540 (M8 part B batch, concept round 51): Europa's ice shelf from the entry
                                  corridor, Level 22's ground tile sets in perspective from high up (the Act 3
                                  still's planes): the shelf's double ridges (`ice-shelf`) near, the Conamara Chaos's
                                  broken plates (`rig-field`) beyond, running to the sharp curved horizon (no air: no haze, a black sky with a few
                                  stars); Jupiter huge over the horizon on the right, banded, lit from the upper
                                  left with its night side to the right, the Great Red Spot; a thin steam thread
                                  rising from the first rig far out on the plates, lit on its sunward side
  design/campaign/act-4-deep-water/concept/act-4-still-final-r51-a.png   its review sheet, as Act 1's
  design/campaign/act-4-deep-water/concept/act-4-still-r52-{a,b}.png   round 52 (F.6 redo) proposals, `act-4-r52`
                                  only: the Act 4 still with a giant water-vapour plume (ice grains thrown on
                                  ballistic arcs from a vent, sunlit from the upper left, glittering, falling back as
                                  a veil); a: one huge plume on the horizon left of Jupiter, b: a nearer plume on a
                                  fissure below the lettering with a curtain of smaller ones receding along it;
                                  act-4-still-r52-sheet.png: both stills and the title card over each. The user
                                  picked a (round 52, T.1): A4_GEYSER = "a", assets/ui's still carries its plume

The look is Level 01's final backdrop (tools/art/backdrop_l01.py, approved in round 12): Earth and
the terminator are its placeholder generator's functions (tools/concept/backdrop_l01.py, frozen,
imported by path) rendered at 960x540 and posterized with 4x4 ordered dither on the wide
gradients; the structures are its production pieces from assets/backdrop/level-01/ and the far
Stormhawks its 16-heading set, placed as the level places them (the far layer hazed and smaller);
Lancer's Stormhawk is tools/art/stormhawk.py's level frame. Nothing lit is mirrored: the mirrored
placements use the level's own -mirrored pieces. The game draws the still darkened behind the
lettering (BriefingScreen.drawTitleCard).

Act 2's still is built from Level 08's production backdrop (tools/art/backdrop_l08.py, imported by
path; its images from assets/backdrop/level-08/): the gulf glow under the haze, the avenues and
rooftops tile sets side by side, the harbour tile in three steps (the open water its first columns
mirrored), the ship, cranes, burnt blocks, fires and smoke columns, the fog and mist tile sets, and
its tower roofs with their walls drawn by its own projection (TowerProjection's), centred on the
still's middle and no roof leaning out more than 84 px. The cloud deck (r03's bank shading), the
trails and the landers are drawn here. The composite under the light is posterized to one
96-colour palette with ordered dither, the additive light (fires, lamps, the landers' shields and
trails) to 32 of its own.

Act 3's still is built from Level 15's production backdrop (tools/art/backdrop_l15.py's images in
assets/backdrop/level-15/): each layer is a plane under the camera sampled per pixel with mip levels
(the ground, the deck at 0.55 of its distance, the cirrus at 0.2), hazed by distance; one 96-colour
dithered palette.

Act 4's still is built from Level 22's production backdrop (tools/art/backdrop_l22.py's images in
assets/backdrop/level-22/) the same way, without haze (Europa has no air); Jupiter, the stars and the steam
thread are drawn here (Jupiter's bands with tools/concept/europa_r48.py's ramp JUP).

  act-5-the-belt-still.png        960x540 (M9 part D batch, concept round 64; round 63 T.1 c "the gold wing", from
                                  tools/concept/act5_still_r63.py's idea c, whose render kit it imports): the belt's
                                  rocks close up, the sun low on the left beside a huge rim-lit foreground rock, a big
                                  sunlit rock top right, a drift of small rocks hazed in the sunlit dust band; a wing of
                                  four Talons in echelon below the lettering in the production Talon's livery (gunmetal,
                                  gold outer panels, the leader's gold spine), each banked its own way, their gold a
                                  polished metal with a sheen band (round 63's flat chrome read as yellow), gold glints,
                                  the engines' short trails; the sun's glare soft (a 1 / (1 + d^2) halo rolled off to
                                  white above a knee, a stronger dither there) instead of round 63's clipped blob
  design/campaign/act-5-the-belt/concept/act-5-still-final-r64-a.png   its review sheet, as Act 1's, with 2x crops
  design/campaign/act-5-the-belt/concept/rejected/act-5-still-final-r62-a.png   ``act-5-r62`` only: round 62's rejected
                                  still (Ceres Hub from the approach, Level 29's laid ground in perspective;
                                  ``ceres_hub_still``) and its title card, as it was reviewed

Run: python3 tools/art/act_stills.py [act-1] [act-2] [act-3] [act-4] [act-5] [--review] | act-4-r52 | act-5-r62 | --check
(~1.5 min for all, act-5 ~1 min on 16 cores; after backdrop_l01.py, stormhawk.py, backdrop_l08.py, backdrop_l15.py,
backdrop_l22.py and backdrop_l29.py; --check lists every still with its size and fails on a missing Source chunk)
"""
import importlib.util
import sys
from pathlib import Path

import numpy as np
from PIL import Image

import artkit
from artkit import DESIGN, ROOT, raster, sprite

from parallax_r02 import haze  # noqa: E402  (concept script, imported unchanged)
from render import sdf  # noqa: E402

_spec = importlib.util.spec_from_file_location(
    "concept_backdrop_l01", Path(__file__).resolve().parents[1] / "concept" / "backdrop_l01.py")
l01 = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(l01)

SCRIPT = "act_stills.py"
SOURCE = artkit.source_note(SCRIPT, "M4 part H batch")
ROUND = "r26"
OUT = ROOT / "assets" / "ui"
PIECES = ROOT / "assets" / "backdrop" / "level-01"
CONCEPT = DESIGN / "campaign" / "act-1-first-contact" / "concept"
W, H = 960, 540
SEA = l01.SEA
DECK_COLOURS = 32
BRIGHTNESS = 0.55                                # BriefingScreen.STILL_BRIGHTNESS, for the review
BAR = 58                                         # the title card's letterbox bars


def piece(name):
    return Image.open(PIECES / f"{name}.png").convert("RGBA")


def earth():
    """Earth far below: the level's day side and its dawn overlay, laid out for the wide still:
    the terminator a gentle diagonal behind the lettering, night bottom left."""
    day = np.array(l01.earth_tile(W, H)).astype(np.float64)
    arr = np.zeros((H, W, 4))
    arr[..., :3] = day[..., :3]
    arr[..., 3] = 1
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float64)
    line = (yy - 300) * 0.9 - (xx - 430) * 0.42          # the terminator, day above and right
    inside = np.ones((H, W), bool)
    night = sdf.smoothstep(-50, 70, line) * inside
    l01.over(arr, np.array(SEA[0], float) + 6, night * 0.84)
    rng = np.random.default_rng(2681)
    towns = raster.fbm(W, H, 32, 2683, octaves=3, period=False)
    lights = (towns > 0.6) & (rng.random((H, W)) < 0.05) & (night > 0.5)
    l01.over(arr, np.array([210, 180, 120], float), lights * 0.9)
    band = np.exp(-(line / 60.0) ** 2)                    # dawn light along the terminator
    l01.over(arr, np.array([200, 150, 100], float), band * 0.28)
    core = np.exp(-((line - 8) / 18.0) ** 2)              # its brightest strip, day side
    l01.over(arr, np.array([226, 178, 124], float), core * 0.14)
    img = l01.finish_dithered(arr, colors=DECK_COLOURS)
    a = np.array(img).astype(np.float64)
    a[..., :3] *= 0.8                                    # the deep layer recedes, as in the level
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def far_layer():
    """The north arm and Aegis Two launching from it, at the far layer's haze."""
    layer = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    arm = piece("north-arm")
    layer.alpha_composite(arm.crop((0, 40, 180, 40 + H)), (560, 0))
    for heading, (x, y) in ((1, (636, 190)), (2, (688, 140))):   # banking away to the right
        sprite.paste_center(layer, piece(f"stormhawk-far_{heading}"), x, y)
    return layer


def ground_layer():
    """The yard's structures at the ground layer, framing the open middle."""
    layer = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    rail = piece("launch-rail")
    layer.alpha_composite(rail.crop((0, 120, 200, 120 + H)), (40, 0))
    layer.alpha_composite(piece("cruiser-hull"), (W - 236, 40))
    dock = piece("dock-frame")
    dock_m = piece("dock-frame-mirrored")
    layer.alpha_composite(dock, (250, 360))
    layer.alpha_composite(dock_m, (500, 380))
    layer.alpha_composite(piece("platform-burning_1"), (380, 430))
    layer.alpha_composite(piece("crossbeam"), (0, 476))
    layer.alpha_composite(piece("crossbeam"), (480, 476))
    crane = piece("bridge-crane")
    layer.alpha_composite(crane, (230, 64))
    return layer


def low_layer():
    """Low-air pieces over the ground: a crane jib and a lattice beam, larger than the ground."""
    layer = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    layer.alpha_composite(piece("crane-jib-mirrored"), (640, 330))
    return layer


def still():
    img = earth()
    far = far_layer()
    img.alpha_composite(haze(far, SEA[2], 0.35))
    ground = ground_layer()
    shadow = sprite.shadow_of(ground, opacity=0.45, blur=1.2)   # the art direction's drop shadow
    img.alpha_composite(shadow, (6, 8))
    img.alpha_composite(ground)
    ship = artkit.load_frames("ship")[2]
    sprite.paste_center(img, sprite.shadow_of(ship, opacity=0.45, blur=1.0), 140 + 8, 392 + 10)
    sprite.paste_center(img, ship, 140, 392)                    # Lancer on the south rail
    flame = artkit.load_frames("engine-flame")
    if flame:
        f = flame[4]
        for dx in (-5, 5):
            img = artkit.add_light(img, f, (int(140 + dx - f.width / 2), int(392 + 18)))
    low = low_layer()
    img.alpha_composite(sprite.shadow_of(low, opacity=0.4, blur=1.5), (10, 14))
    img.alpha_composite(low)
    return img.convert("RGB").convert("RGBA")


def build():
    OUT.mkdir(parents=True, exist_ok=True)
    img = still()
    artkit.save_png(img, OUT / "act-1-first-contact-still.png", SOURCE)
    print(f"act-1-first-contact-still.png: {img.width}x{img.height}, {artkit.colour_count([img])} colours")


def title_card(img, act="act-1-first-contact"):
    """The title card as BriefingScreen draws it: the still darkened, the letterbox bars, the
    chrome lettering (without the settings line and the missions, which the game sets in its font)."""
    card = Image.new("RGBA", (W, H), (0, 0, 0, 255))
    dark = Image.blend(Image.new("RGBA", (W, H), (0, 0, 0, 255)), img, BRIGHTNESS)
    card.alpha_composite(dark)
    card.paste((0, 0, 0, 255), (0, 0, W, BAR))
    card.paste((0, 0, 0, 255), (0, H - BAR, W, H))
    letters = Image.open(OUT / f"{act}-title.png").convert("RGBA")
    card.alpha_composite(letters, ((W - letters.width) // 2, 138))
    return card


def review():
    img = Image.open(OUT / "act-1-first-contact-still.png").convert("RGBA")
    sheet = raster.sheet(1000, 40 + 2 * (H + 30) + 10, "ACT 1 TITLE-CARD STILL (FINAL R26): THE GAGARIN YARDS AT DAWN",
                         f"PRODUCTION ART, M4 PART H BATCH - {ROUND.upper()}")
    raster.draw_text(sheet, 16, 38, f"THE STILL, 960X540 AT 1X ({artkit.colour_count([img])} COLOURS)", raster.LABEL)
    sheet.alpha_composite(img, (16, 52))
    raster.draw_text(sheet, 16, 52 + H + 14, f"THE TITLE CARD OVER IT: DARKENED TO {int(BRIGHTNESS * 100)} %, LETTERBOX, "
                                             "CHROME LETTERING (THE GAME ADDS THE SETTINGS LINE AND THE MISSIONS)",
                     raster.LABEL)
    sheet.alpha_composite(title_card(img), (16, 52 + H + 28))
    CONCEPT.mkdir(parents=True, exist_ok=True)
    path = CONCEPT / f"act-1-still-final-{ROUND}-a.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")


# =========================================================================== Act 2: Nova Lagos

_spec8 = importlib.util.spec_from_file_location("backdrop_l08", Path(__file__).resolve().parent / "backdrop_l08.py")
l08 = importlib.util.module_from_spec(_spec8)
_spec8.loader.exec_module(l08)
l08.W = W               # its projection and blits draw on the still's 960 px (only in this process)

A2_SOURCE = artkit.source_note(SCRIPT, "M5 part B batch")
A2_ROUND = "r30"
A2_CONCEPT = DESIGN / "campaign" / "act-2-homefront" / "concept"
A2_NAME = "act-2-homefront"
STILL_COLOURS = 96       # the composite under the light, one dithered palette
CLOUD_COLOURS = 16
LIGHT_COLOURS = 32
HAZE = (np.array([0x2c, 0x24, 0x34], float), 0.06)   # Level 08's haze colour at its `light` look
# the city: Level 08's ground tile sets side by side (no repeat), the tile row at the still's top
# chosen so their cross streets lie at y = 12 + 96 k (300, 396 and 492 are the harbour's steps)
CITY = (("avenues", 0, 83), ("rooftops", 480, 371))
# the harbour, stepping down to the south east: (top y, right x, harbour tile row at y 0): from
# its top the band takes the harbour tile (water, quay, quay road, container yards) with the yards'
# right edge on a city street, and the Gulf (the water extended) left of it
HARBOUR = ((300, 474, 467), (396, 650, 371), (492, 866, 275))
WATER_EDGE, YARDS_END = 196, 480      # the harbour tile's quay edge and its right end
QUAY = np.array([24, 34, 52.0])       # its quay edge
PERIOD = l08.PERIOD                   # the city's cross streets
L08_DIR = l08.OUT
CX, CY = 480.0, 280.0                 # the towers' projection centre (y down): the still's middle
FOG = 0.6                             # the fog banks' share (the deck is over them)
LEAN = 84                             # most px a roof may lean out from its foot
# the landers' heads, kept off the lettering (x 100-860, y 138-345 with the settings line)
TRAIL_DIR = np.array([-0.45, 0.89]) / np.hypot(-0.45, 0.89)   # the landers come in from the top right
LANDERS = [((215, 418), 0.0, 1.0), ((392, 458), 2.0, 0.9), ((642, 394), -2.5, 1.0),
           ((792, 448), 1.5, 0.85), ((906, 262), -1.0, 0.8), ((62, 214), 3.0, 0.7),
           ((704, 104), -1.5, 0.6), ((296, 92), 1.0, 0.55)]
# the fires: on the yards along the quays, then two burnt blocks in the city (x, y, size)
SHORE_FIRES = [(300, 352, 0.9), (414, 330, 0.7), (470, 448, 1.0), (592, 424, 0.6), (700, 518, 0.8),
               (812, 508, 0.6)]
BURNT = [("burnt-block-c", 771, 403), ("burnt-block-a", 880, 307)]     # top left on their lots
CITY_FIRES = [(818, 444, 0.8), (916, 346, 0.6)]
SHIP = (86, 352)                                    # the container ship's top left, at the first quay
CRANES = [(82, 326), (258, 426)]                    # the quay cranes' top left, booms over the water
_yy, _xx = np.mgrid[0:H, 0:W].astype(np.float64)


def fires():
    return SHORE_FIRES + CITY_FIRES


def piece8(name):
    return np.array(l08.load(name)).astype(np.float64)


def over_arr(dst, src, alpha=1.0):
    a = src[..., 3:4] / 255 * alpha
    dst[..., :3] = src[..., :3] * a + dst[..., :3] * (1 - a)


def put(frame, img, x, y, additive=None):
    """A float RGBA image with its top left at (x, y) on the still (y down), clipped."""
    l08.blit(frame, img, int(x), int(H - y - img.shape[0]), additive)


def harbour_columns(right):
    """The harbour tile's column for each x left of ``right``: the tile ends at ``right``; left of the
    tile the open water, its first 36 columns mirrored back and forth (no pier crosses them)."""
    tx = np.arange(right) - (right - YARDS_END)
    k = -tx - 1
    m = k % 72
    return np.where(tx >= 0, tx, np.where(m < 36, m, 71 - m))


def ground():
    """Nova Lagos from above as Level 08's ground draws it: the Gulf's glow (deep, under the haze),
    the avenues and rooftops tile sets, the harbour in three steps, the ship and the cranes. Returns
    the frame (float RGBA) and the water's mask."""
    gulf = piece8("gulf")
    sea = gulf[np.arange(H) % gulf.shape[0]][:, np.arange(W) % gulf.shape[1]].copy()
    sea[..., :3] = sea[..., :3] * (1 - HAZE[1]) + HAZE[0] * HAZE[1]
    frame = sea.copy()
    for name, x0, row in CITY:
        tile = piece8(name)
        frame[:, x0:x0 + 480] = tile[(row + np.arange(H)) % tile.shape[0]]
    harbour = piece8("harbour")
    water = np.zeros((H, W), bool)
    left = 0
    for top, right, row in HARBOUR:
        band = harbour[(row + np.arange(top, H)) % harbour.shape[0]][:, harbour_columns(right)]
        frame[top:, :right] = sea[top:, :right]
        over_arr(frame[top:, :right], band)
        water[top:, :right] = band[..., 3] < 250
        edge = right - YARDS_END + WATER_EDGE
        frame[top:top + 2, left:edge, :3] = QUAY                 # the quay's lip where the band steps out
        left = edge
    put(frame, piece8("container-ship"), *SHIP)
    for x, y in CRANES:
        put(frame, piece8("quay-crane"), x, y)
    for name, x, y in BURNT:
        put(frame, piece8(name), x, y)
    return frame, water


def harbour_lamps():
    """Sodium lamps along the quays' lips (the tile's own lamps are in it)."""
    out, left = [], 0
    for top, right, _ in HARBOUR:
        edge = right - YARDS_END + WATER_EDGE
        out += [(x, top + 3) for x in range(left + 24, edge - 8, 48)]
        left = edge
    return out


def lots():
    """The city's lots (x0, y0, x1, y1) on the still that the harbour and the burnt blocks leave free."""
    out = []
    for name, x0, _ in CITY:
        for b0, b1 in l08.PLANS[name]["blocks"]:
            for s in range(12 - PERIOD, H, PERIOD):              # the cross streets' centres
                y0, y1 = s + 6, s + PERIOD - 6
                if any(y1 > top and x0 + b0 < right for top, right, _ in HARBOUR):
                    continue
                if any(abs(x0 + b0 - x) < 8 and abs(y0 - y) < 8 for _, x, y in BURNT):
                    continue
                out.append((x0 + b0, y0, x0 + b1, y1))
    return out



def towers():
    """Towers on the free lots, Level 08's roof pieces with their walls projected from the still's
    middle (backdrop_l08.py's TowerProjection draw): taller towards the middle, so no roof leans more
    than LEAN px; a few neon towers to the east, under the deck."""
    rng = np.random.default_rng(3088)
    out = []
    for x0, y0, x1, y1 in lots():
        if rng.random() > 0.62:
            continue
        fits = [f for f in l08.FOOTPRINTS if f[0] <= x1 - x0 - 6 and f[1] <= y1 - y0 - 8]
        if not fits:
            continue
        foot = fits[-1] if rng.random() < 0.7 or len(fits) == 1 else fits[-2]
        cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
        dist = max(np.hypot(cx - CX, cy - CY), 1)
        style = ("neon" if rng.random() < 0.5 else "neon-b") if cx > 560 and rng.random() < 0.4 else \
            ("a" if rng.random() < 0.55 else "b")
        heights = [h for h in l08.STYLES[style][0] if (l08.tower_scale(h) - 1) * dist <= LEAN
                   and (L08_DIR / f"{l08.tower_id(foot, h, style)}.png").exists()]
        if not heights:
            continue
        h = heights[min(len(heights) - 1, int(rng.uniform(0.35, 1.0) ** 1.1 * len(heights)))]
        out.append((l08.tower_id(foot, h, style), foot, h, style, cx, cy))
    return out



def draw_towers(frame):
    """As backdrop_l08.draw_towers, on the still: the walls the camera sees, then the roof."""
    cy_up = H - CY
    for name, (fw, fh), h, style, cx, cy in sorted(towers(), key=lambda t: t[2]):
        k = l08.tower_scale(h)
        roof = piece8(name)
        rw, rh = roof.shape[1], roof.shape[0]
        fx, fy = l08.java_round(cx - fw / 2), l08.java_round(H - cy - fh / 2)      # bottom left, y up
        rx = l08.java_round(CX + (fx + fw / 2 - CX) * k - rw / 2)
        ry = l08.java_round(cy_up + (fy + fh / 2 - cy_up) * k - rh / 2)
        wall = l08.wall_for(style, h)
        tex = np.array(l08.load(wall)).astype(np.float64)
        shade = l08.STYLES[style][2] or 0.5
        fx1, fy1, rx1, ry1 = fx + fw, fy + fh, rx + rw, ry + rh
        if ry > fy:
            l08.wall_quad(frame, tex, shade, ((fx, fy), (fx1, fy)), ((rx, ry), (rx1, ry)), h)
        if ry1 < fy1:
            l08.wall_quad(frame, tex, 1.0, ((fx1, fy1), (fx, fy1)), ((rx1, ry1), (rx, ry1)), h)
        if rx < fx:
            l08.wall_quad(frame, tex, 1.0, ((fx, fy1), (fx, fy)), ((rx, ry1), (rx, ry)), h)
        if rx1 > fx1:
            l08.wall_quad(frame, tex, shade, ((fx1, fy), (fx1, fy1)), ((rx1, ry), (rx1, ry1)), h)
        l08.blit(frame, roof, rx, ry)


def air(frame):
    """Level 08's low and high air over the city: its amber-lit fog banks (two windows of the tile
    set), the fires on the yards and the burnt blocks with their smoke columns, its thin mist."""
    fog = piece8("fog")
    for x0, row in ((0, 120), (480, 560)):
        over_arr(frame[:, x0:x0 + 480], fog[(row + np.arange(H)) % fog.shape[0]], FOG)
    for i, (x, y, s) in enumerate(fires()):
        f = piece8(f"fire_{i % 4}")
        put(frame, f, x - f.shape[1] / 2, y - f.shape[0] / 2)
    for i, (x, y, s) in enumerate(fires()):
        col = piece8(f"smoke-column-{'abc'[i % 3]}")
        put(frame, col, x - col.shape[1] * 0.32, y - col.shape[0] * 0.72)
    mist = piece8("mist")
    for x0, row in ((0, 300), (480, 0)):
        part = mist[(row + np.arange(H)) % mist.shape[0]]
        l08.add(frame[:, x0:x0 + 480], part, 0.4)


def city():
    frame, water = ground()
    draw_towers(frame)
    air(frame)
    return Image.fromarray(np.clip(frame, 0, 255).astype(np.uint8), "RGBA"), water


def ui_dither(rgb, mask, colours):
    """An RGB layer posterized with 4x4 ordered dither on its mask (the backdrops' rule for wide
    gradients), as tools/art/ui_scenes.dithered."""
    h, w = rgb.shape[:2]
    b = np.tile(artkit.BAYER4, (h // 4 + 1, w // 4 + 1))[:h, :w]
    img = raster.to_rgba_image(rgb + ((b - 0.5) * 10.0)[..., None], mask * 255)
    return artkit.quantize_set([img], colours)[0]


def lander_paths():
    """Each lander's trail: head, tail (off the frame behind it), its brightness (its nearness)."""
    out = []
    for (hx, hy), turn, bright in LANDERS:
        a = np.radians(turn)
        d = np.array([TRAIL_DIR[0] * np.cos(a) - TRAIL_DIR[1] * np.sin(a),
                      TRAIL_DIR[0] * np.sin(a) + TRAIL_DIR[1] * np.cos(a)])
        head = np.array([hx, hy], float)
        out.append((head, head - d * 900, bright))
    return out


def along(head, tail):
    """Per pixel: t along the trail (0 at the tail, 1 at the head) and the distance across it."""
    seg = head - tail
    length = np.hypot(*seg)
    t = ((_xx - tail[0]) * seg[0] + (_yy - tail[1]) * seg[1]) / length ** 2
    tc = np.clip(t, 0, 1)
    d = np.hypot(_xx - (tail[0] + seg[0] * tc), _yy - (tail[1] + seg[1] * tc))
    return t, d, length


def lander_size(bright):
    """A lander's length and width in px: the nearer (brighter) the larger."""
    return 16 + 16 * bright, 9 + 8 * bright


def lander_shape(head, tail, bright, sx, sy):
    """A lander's body at the points (sx, sy): u along it (-1 tail .. 1 nose, the nose at the trail's
    head), b across it, the half width there and the body mask: a seed pod, blunt and round at the
    nose (its heat shield), tapering to the tail."""
    d = (head - tail) / np.hypot(*(head - tail))
    n = np.array([-d[1], d[0]])
    length, width = lander_size(bright)
    a = (sx - head[0]) * d[0] + (sy - head[1]) * d[1] + length * 0.5
    b = (sx - head[0]) * n[0] + (sy - head[1]) * n[1]
    u = a / (length * 0.5)
    half = width * 0.5 * np.where(u > 0, np.sqrt(np.clip(1 - u ** 2, 0, 1)), np.clip(1 + u * 0.85, 0, 1))
    return u, b, half, (np.abs(b) < half) & (u > -1) & (u < 1), d, n


def cloud_deck():
    """The cloud deck the landing comes down through: billowy banks over the top and the right,
    open over the harbour and the city's middle, lit from below by the city, the fires and the
    trails (r03's bank shading)."""
    import parallax_r03 as p3   # noqa: E402
    n = raster.fbm(W // 2, H, 54, 3201, octaves=5, period=False)
    n = np.array(Image.fromarray((n * 255).astype(np.uint8)).resize((W, H), Image.BICUBIC),
                 dtype=np.float64) / 255
    top = np.clip(1 - (_yy - 40) / 260, 0, 1)
    right = np.clip((_xx - 560) / 360, 0, 1) * np.clip(1 - (_yy - 300) / 260, 0, 1)
    shore = np.clip(1 - np.hypot(_xx - 300, _yy - 430) / 280, 0, 1)
    n = n * 0.8 + 0.42 * np.maximum(top, right) - 0.35 * shore
    glow = np.zeros((H, W))
    for x, y, s in fires():
        glow += np.exp(-((_xx - x) ** 2 + (_yy - y) ** 2) / (90 * s) ** 2) * s
    for head, tail, bright in lander_paths():
        t, d, _ = along(head, tail)
        glow += np.exp(-(d / 30) ** 2) * np.clip(t, 0, 1) ** 2 * bright * (t < 1.05) * 0.7
    pal = [(10, 12, 26), (22, 26, 50), (36, 40, 70), (54, 58, 92)]
    deck = p3.bank(n, 0.58, 0.16, pal, max_alpha=220, light=3.0, glow=(np.array([150, 70, 26]), np.clip(glow, 0, 1.4)))
    return p3.step_alpha(artkit.quantize_set([deck], CLOUD_COLOURS)[0], 5)


def trail_smoke():
    """The trails behind the landers: grey smoke bands widening and thinning behind each craft."""
    a = np.zeros((H, W))
    n = raster.fbm(W, H, 16, 3301, octaves=3, period=False)
    for head, tail, bright in lander_paths():
        t, d, length = along(head, tail)
        behind = (1 - np.clip(t, 0, 1)) * length - lander_size(bright)[0] * 0.6   # px behind the craft
        width = 2.5 + np.clip(behind, 0, None) * 0.035
        a = np.maximum(a, np.exp(-(d / width) ** 2) * (t <= 1) * (behind > 0)
                       * np.exp(-np.clip(behind, 0, None) / 480) * 0.8 * (0.6 + 0.6 * n))
    rgb = np.broadcast_to(np.array([72, 66, 76.0]), (H, W, 3)) * (0.75 + 0.4 * n[..., None])
    a = np.clip(a, 0, 1)
    img = raster.to_rgba_image(rgb, np.floor(a * 6 + 0.5) / 6 * 255)
    return artkit.quantize_set([img], 12)[0]


def landers():
    """The landers themselves: small Vrell seed pods (dark ribbed chitin, three short spines trailing
    from the tail), nose first along their trails, lit from the top left; rendered at 4x and
    box-reduced."""
    f = 4
    out = np.zeros((H, W, 4))
    key = np.array([-0.55, -0.6]) / np.hypot(0.55, 0.6)                 # the light, top left (y down)
    for head, tail, bright in lander_paths():
        length, width = lander_size(bright)
        x0, y0 = max(0, int(head[0] - 40)), max(0, int(head[1] - 40))
        x1, y1 = min(W, int(head[0] + 40)), min(H, int(head[1] + 40))
        sy, sx = np.mgrid[y0 * f:y1 * f, x0 * f:x1 * f].astype(np.float64) / f + 0.5 / f
        u, b, half, body, d, n = lander_shape(head, tail, bright, sx, sy)
        a = u * length * 0.5
        spines = np.zeros_like(body)
        for side in (-1, 0, 1):
            sa = a + length * 0.42                                          # px behind the tail's root
            off = side * (width * 0.12 + np.clip(-sa, 0, None) * 0.45)
            spines |= (sa < 0) & (sa > -length * (0.28 if side else 0.2)) & (np.abs(b - off) < 0.6)
        nb = np.clip(b / np.maximum(half, 1e-3), -1, 1)
        normal = n[None, None, :] * nb[..., None] + d[None, None, :] * np.clip(u, 0, 1)[..., None] * 0.6
        lit = np.clip(0.3 + 0.8 * -(normal @ key), 0, 1)
        rib = (np.floor((a + 100) / 2.4) % 2) * 0.14 * (u < 0.5)            # chitin plates behind the shield
        rgb = l08.CHITIN[0] * (1 - lit[..., None]) + l08.CHITIN[3] * 0.8 * lit[..., None]
        rgb = rgb * (1 - rib[..., None])
        rgb = np.where(spines[..., None] & ~body[..., None], l08.CHITIN[1] * 0.8, rgb)
        tile = np.zeros(sx.shape + (4,))
        tile[..., :3] = rgb / 255
        tile[..., 3] = body | spines
        small = l08.reduce(tile, f)
        sub = out[y0:y1, x0:x1]
        sa_ = small[..., 3:4]
        sub[..., :3] = small[..., :3] * 255 * sa_ + sub[..., :3] * (1 - sa_)
        sub[..., 3] = np.maximum(sub[..., 3], sa_[..., 0] * 255)
    img = raster.to_rgba_image(out[..., :3], (out[..., 3] >= 128) * 255)
    return artkit.quantize_set([img], 16)[0]


def lights(water):
    """The additive light: the fires (a warm glow, flame flecks, broken reflections on the water),
    the quays' lamps, and the landers' entry glow (a hot shield at the nose, a violet halo: Vrell
    craft) with a short ember trail behind them."""
    light = np.zeros((H, W, 3))
    rng = np.random.default_rng(3401)
    ripple = raster.fbm(W // 8, H, 4, 3411, octaves=2, period=False)
    ripple = np.array(Image.fromarray((ripple * 255).astype(np.uint8)).resize((W, H), Image.NEAREST),
                      dtype=np.float64) / 255
    wet = (water * np.clip(ripple * 2 - 0.6, 0, 1))[..., None]
    for x, y, s in fires():
        r2 = (_xx - x) ** 2 + (_yy - y) ** 2
        light += np.exp(-r2 / (30 * s) ** 2)[..., None] * np.array([150, 58, 12]) * s
        light += np.exp(-r2 / (9 * s) ** 2)[..., None] * np.array([130, 70, 20]) * s
        refl = np.exp(-((_xx - x) / (6 + 6 * s)) ** 2 - ((_yy - y - 30) / (34 * s)) ** 2)
        light += refl[..., None] * wet * np.array([150, 70, 20]) * s
        for _ in range(int(30 * s)):                          # the flames: hot flecks
            fx, fy = x + rng.normal(0, 6 * s), y + rng.normal(0, 4 * s)
            ix, iy = int(fx), int(fy)
            if 0 <= ix < W - 1 and 1 <= iy < H:
                hot = rng.random()
                light[iy - 1:iy + 1, ix] += (255, 120 + 120 * hot, 30 + 80 * hot)
    for x, y in harbour_lamps():
        r2 = (_xx - x) ** 2 + (_yy - y) ** 2
        light += np.exp(-r2 / 20.0)[..., None] * l08.SODIUM * 1.25
    for head, tail, bright in lander_paths():
        t, d, length = along(head, tail)
        size, width = lander_size(bright)
        behind = (1 - np.clip(t, 0, 1)) * length - size * 0.9          # px behind the tail
        on = (t <= 1.0) & (t >= 0) & (behind > 0)
        fade = np.exp(-np.clip(behind, 0, None) / 120)
        core = np.exp(-(d / (0.8 + behind * 0.004)) ** 2) * fade * on
        soft = np.exp(-(d / (3 + behind * 0.02)) ** 2) * fade * on
        light += np.array([220, 96, 36.0]) * (core * 0.45 + soft * 0.2)[..., None] * bright
        # the entry glow: the heat shield on the nose white to orange, a warm glow ahead of it and a
        # violet halo round the craft (Vrell)
        u, b, half, body, dd, _ = lander_shape(head, tail, bright, _xx + 0.5, _yy + 0.5)
        shield = body * np.clip((u - 0.55) / 0.45, 0, 1)
        light += shield[..., None] * np.array([230, 150, 90]) * bright
        r2 = (_xx - head[0] - dd[0] * 2) ** 2 + (_yy - head[1] - dd[1] * 2) ** 2
        light += np.exp(-r2 / (width * 0.45) ** 2)[..., None] * np.array([90, 40, 16]) * bright
        light += np.exp(-r2 / 300.0)[..., None] * np.array([80, 30, 120]) * bright
    return light


def add_lit(img, light):
    """``img`` with the additive light on it; the pixels it touches get a palette of their own,
    ordered-dithered so the wide glows do not band."""
    lit = light.max(axis=-1) >= 3
    b = np.tile(artkit.BAYER4, (H // 4 + 1, W // 4 + 1))[:H, :W]
    rgb = np.clip(np.array(img).astype(np.float64)[..., :3] + light + ((b - 0.5) * 10.0)[..., None], 0, 255)
    out = img.copy()
    out.alpha_composite(artkit.quantize_set([raster.to_rgba_image(rgb, lit * 255)], LIGHT_COLOURS)[0])
    return out


def still2():
    img, water = city()
    img.alpha_composite(cloud_deck())
    img.alpha_composite(trail_smoke())
    img.alpha_composite(landers())
    img = ui_dither(np.array(img).astype(np.float64)[..., :3], np.ones((H, W)), STILL_COLOURS)
    img = add_lit(img, lights(water))
    return img.convert("RGB").convert("RGBA")


def build2():
    OUT.mkdir(parents=True, exist_ok=True)
    img = still2()
    artkit.save_png(img, OUT / f"{A2_NAME}-still.png", A2_SOURCE)
    print(f"{A2_NAME}-still.png: {img.width}x{img.height}, {artkit.colour_count([img])} colours")


def review2():
    img = Image.open(OUT / f"{A2_NAME}-still.png").convert("RGBA")
    sheet = raster.sheet(1000, 40 + 2 * (H + 30) + 10,
                         "ACT 2 TITLE-CARD STILL (FINAL R30): NOVA LAGOS, LANDFALL",
                         f"PRODUCTION ART, M5 PART B BATCH - {A2_ROUND.upper()}")
    raster.draw_text(sheet, 16, 38, f"THE STILL, 960X540 AT 1X ({artkit.colour_count([img])} COLOURS)", raster.LABEL)
    sheet.alpha_composite(img, (16, 52))
    raster.draw_text(sheet, 16, 52 + H + 14, f"THE TITLE CARD OVER IT: DARKENED TO {int(BRIGHTNESS * 100)} %, LETTERBOX, "
                                             "CHROME LETTERING (THE GAME ADDS THE SETTINGS LINE AND THE MISSIONS)",
                     raster.LABEL)
    sheet.alpha_composite(title_card(img, A2_NAME), (16, 52 + H + 28))
    A2_CONCEPT.mkdir(parents=True, exist_ok=True)
    path = A2_CONCEPT / f"act-2-still-final-{A2_ROUND}-a.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")


# =========================================================================== Act 3: Olympus from the entry corridor

A3_SOURCE = artkit.source_note(SCRIPT, "M7 part B batch")
A3_ROUND = "r38"
A3_CONCEPT = DESIGN / "campaign" / "act-3-red-dust" / "concept"
A3_NAME = "act-3-red-dust"
L15_DIR = ROOT / "assets" / "backdrop" / "level-15"
HORIZON, CURVE = 104.0, 6e-5            # the horizon's middle and the planet's curve down to the sides
FOCAL = 480.0
GROUND_A = 366_000.0                    # the ground plane: its distance ahead = GROUND_A / rows below the horizon
DECK_SHARE, CIRRUS_SHARE = 0.55, 0.2    # the cloud deck's and the cirrus' planes, nearer (their A a share of it)
DECK_TEX = 0.4                          # the deck's texels per plane unit: its banks drawn 2.5x the level's size
OLY_UV, OLY_SCALE = (-60.0, 1300.0), 2.2   # the volcano (the `olympus` piece) on the ground plane, its scale
HAZE_FAR = 9000.0                       # the haze's distance scale (tile px)
SKY = np.array([0xd6, 0xa6, 0x70], float)   # backdrop_l15.HAZE_COLOUR: the butterscotch sky
SKY_TOP = np.array([20, 12, 22.0])      # the sky from high up
STORM = (500, 960)                      # the dust-storm front's span on the horizon (x)


def l15(name):
    """A Level 15 backdrop image (tools/art/backdrop_l15.py) as a premultiplied float RGBA array."""
    a = np.array(Image.open(L15_DIR / f"{name}.png").convert("RGBA")).astype(np.float64)
    a[..., :3] *= a[..., 3:4] / 255
    return a


def mip_chain(a, levels=7):
    out = [a]
    for _ in range(levels):
        q = out[-1]
        q = q[:q.shape[0] // 2 * 2, :q.shape[1] // 2 * 2]
        out.append((q[0::2, 0::2] + q[1::2, 0::2] + q[0::2, 1::2] + q[1::2, 1::2]) / 4)
    return out


def bilinear(a, x, y, periodic):
    """``a`` at pixel coordinates (x, y), wrapping or transparent outside."""
    h, w = a.shape[:2]
    x, y = x - 0.5, y - 0.5
    x0, y0 = np.floor(x).astype(int), np.floor(y).astype(int)
    fx, fy = (x - x0)[..., None], (y - y0)[..., None]

    def at(xi, yi):
        if periodic:
            return a[yi % h, xi % w]
        inside = ((xi >= 0) & (xi < w) & (yi >= 0) & (yi < h))[..., None]
        return a[np.clip(yi, 0, h - 1), np.clip(xi, 0, w - 1)] * inside
    top = at(x0, y0) * (1 - fx) + at(x0 + 1, y0) * fx
    bot = at(x0, y0 + 1) * (1 - fx) + at(x0 + 1, y0 + 1) * fx
    return top * (1 - fy) + bot * fy


def plane_sample(chain, x, y, footprint, periodic):
    """A texture on a plane: (x, y) its texel coordinates, ``footprint`` texels per screen pixel,
    filtered by blending the two nearest mip levels."""
    lod = np.clip(np.log2(np.maximum(footprint, 1.0)), 0, len(chain) - 1.001)
    out = np.zeros(x.shape + (4,))
    for level in range(len(chain)):
        wgt = np.clip(1 - np.abs(lod - level), 0, 1)
        if wgt.max() == 0:
            continue
        s = 2.0 ** level
        out += bilinear(chain[level], x / s, y / s, periodic) * wgt[..., None]
    return out


def horizon_y():
    return HORIZON + CURVE * (_xx - W / 2) ** 2


def olympus_still():
    """Act 3's still: Mars from the entry corridor, the deep layer of Level 15 seen in perspective from
    high up: the Tharsis plain (`mars-high`) running to the curved horizon, Olympus Mons (`olympus`)
    far below in the lower middle, the cloud deck's banks (`deck-banks`) on a nearer plane, the thin
    cirrus (`cirrus`) nearer still, additive at 40 % as the game draws it; the haze thickening with
    distance to the butterscotch sky; a dust-storm front rolling along the northern horizon."""
    hor = horizon_y()
    dy = np.maximum(_yy - hor, 0.5)
    ground = _yy > hor
    z = GROUND_A / dy                                            # tile px ahead
    u = (_xx - W / 2) * z / FOCAL
    foot = z / FOCAL
    plain = plane_sample(mip_chain(l15("mars-high")), u + 140, -z, foot, True)[..., :3]
    oly = l15("olympus")
    ou = (u - OLY_UV[0]) / OLY_SCALE + oly.shape[1] / 2
    ov = -(z - OLY_UV[1]) / OLY_SCALE + oly.shape[0] / 2
    vol = plane_sample(mip_chain(oly), ou, ov, foot / OLY_SCALE, False)
    v = z + 700
    albedo = (1 + 0.16 * np.sin(u / 610 + 1.3) * np.sin(v / 830) + 0.12 * np.sin(u / 1700 - v / 2300)
              + 0.08 * np.sin(u / 290 + v / 410))                  # broad light and dark regions over the tiling
    plain = plain * albedo[..., None]
    rgb = vol[..., :3] + plain * (1 - vol[..., 3:4] / 255)
    haze = (1 - np.exp(-z / HAZE_FAR))[..., None]
    rgb = rgb * (1 - (0.12 + 0.8 * haze)) + SKY * 0.9 * (0.12 + 0.8 * haze)
    # the cloud deck on its plane, kept off the volcano
    zd = GROUND_A * DECK_SHARE / dy
    deck_chain = mip_chain(l15("deck-banks"))
    deck = plane_sample(deck_chain, ((_xx - W / 2) * zd / FOCAL + 300) * DECK_TEX, (-zd - 400) * DECK_TEX,
                        zd / FOCAL * DECK_TEX, True)
    clear = np.clip(np.hypot((_xx - 430) / 320, (_yy - 440) / 130) - 0.7, 0, 1)
    band = np.clip((zd - 1100) / 900, 0, 1) * np.clip((16000 - zd) / 6000, 0, 1)      # banks in the middle distance
    da = deck[..., 3:4] / 255 * (clear * band)[..., None] * 0.9
    shadow = plane_sample(deck_chain, (u * DECK_SHARE + 340) * DECK_TEX, (-z * DECK_SHARE - 340) * DECK_TEX,
                          foot * DECK_SHARE * DECK_TEX, True)
    rgb = rgb * (1 - 0.3 * (shadow[..., 3:4] / 255) * (clear * band)[..., None])          # their shadows on the plain
    dcol = np.where(deck[..., 3:4] > 0, deck[..., :3] / np.maximum(deck[..., 3:4] / 255, 1e-6), 0)
    dhaze = (1 - np.exp(-zd / HAZE_FAR))[..., None]
    dcol = dcol * (1 - 0.7 * dhaze) + SKY * 0.7 * dhaze
    rgb = rgb * (1 - da) + dcol * da
    # the storm front on the northern horizon: billowing dust rising off the plain
    n1 = raster.fbm(W, 1, 40, 3871, octaves=4, period=False)[0]
    span = np.clip((_xx[0] - STORM[0]) / 160, 0, 1) * np.clip((STORM[1] + 40 - _xx[0]) / 60, 0, 1)
    top = hor[0] - span * (20 + 42 * n1)
    bill = raster.fbm(W, H, 10, 3872, octaves=4, period=False)
    storm = np.clip((_yy - top[None, :] + 4 * bill) / 6, 0, 1) * (_yy < hor + 26 * span[None, :]) * (span[None, :] > 0)
    storm *= np.clip((hor + 26 * span[None, :] - _yy) / 18, 0, 1)
    shade = np.clip(0.6 + 0.9 * (bill - 0.5) - 0.01 * (_yy - top[None, :]), 0.15, 1)
    scol = raster.ramp([(0.0, (84, 46, 30)), (0.5, (160, 102, 62)), (1.0, (226, 176, 120))], shade)
    scol = scol * 0.75 + SKY * 0.25                                        # far off: hazed
    sky_t = np.clip((hor - _yy) / hor, 0, 1) ** 0.6
    sky = SKY[None, None, :] * (1 - sky_t[..., None]) + SKY_TOP * sky_t[..., None]
    sky += np.exp(-((_yy - hor) / 2.5) ** 2)[..., None] * np.array([60, 44, 30.0])     # the limb's bright line
    rgb = np.where(ground[..., None], rgb, sky)
    a = (storm * span[None, :] ** 0.5)[..., None]
    rgb = rgb * (1 - a) + scol * a
    # the cirrus, nearest: thin water ice, additive at 40 %
    zc = GROUND_A * CIRRUS_SHARE / dy
    ci = plane_sample(mip_chain(l15("cirrus")), (_xx - W / 2) * zc / FOCAL, -zc - 200, zc / FOCAL, True)
    rgb = rgb + ci[..., :3] * 0.4 * ground[..., None]
    return np.clip(rgb, 0, 255)


def still3():
    img = ui_dither(olympus_still(), np.ones((H, W)), STILL_COLOURS)
    return img.convert("RGB").convert("RGBA")


def build3():
    OUT.mkdir(parents=True, exist_ok=True)
    img = still3()
    artkit.save_png(img, OUT / f"{A3_NAME}-still.png", A3_SOURCE)
    print(f"{A3_NAME}-still.png: {img.width}x{img.height}, {artkit.colour_count([img])} colours")


def review3():
    img = Image.open(OUT / f"{A3_NAME}-still.png").convert("RGBA")
    sheet = raster.sheet(1000, 40 + 2 * (H + 30) + 10,
                         "ACT 3 TITLE-CARD STILL (FINAL R38): OLYMPUS MONS",
                         f"PRODUCTION ART, M7 PART B BATCH - {A3_ROUND.upper()}")
    raster.draw_text(sheet, 16, 38, f"THE STILL, 960X540 AT 1X ({artkit.colour_count([img])} COLOURS)", raster.LABEL)
    sheet.alpha_composite(img, (16, 52))
    raster.draw_text(sheet, 16, 52 + H + 14, f"THE TITLE CARD OVER IT: DARKENED TO {int(BRIGHTNESS * 100)} %, LETTERBOX, "
                                             "CHROME LETTERING (THE GAME ADDS THE SETTINGS LINE AND THE MISSIONS)",
                     raster.LABEL)
    sheet.alpha_composite(title_card(img, A3_NAME), (16, 52 + H + 28))
    A3_CONCEPT.mkdir(parents=True, exist_ok=True)
    path = A3_CONCEPT / f"act-3-still-final-{A3_ROUND}-a.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")


# =========================================================================== Act 4: Europa from the entry corridor

A4_SOURCE = artkit.source_note(SCRIPT, "M8 part B batch")
A4_ROUND = "r51"
A4_CONCEPT = DESIGN / "campaign" / "act-4-deep-water" / "concept"
A4_NAME = "act-4-deep-water"
L22_DIR = ROOT / "assets" / "backdrop" / "level-22"
HORIZON4, CURVE4 = 286.0, 1.0e-4       # the sharp horizon's middle and its curve down to the sides
GROUND4 = 150_000.0                    # the ground plane: distance ahead = GROUND4 / rows below the horizon
CHAOS_Z = (2600.0, 5200.0)             # the shelf gives way to the chaos's plates over this distance
JUP_C, JUP_R = (676.0, 196.0), 286.0   # Jupiter's disc on the screen
SUN = np.array([-0.62, -0.5, 0.6])     # the sun from the upper left (screen x right, y down, z toward the camera)
STEAM_AT = (318.0, 22.0)               # the first rig far out: x on the screen, rows below the horizon


def l22(name):
    a = np.array(Image.open(L22_DIR / f"{name}.png").convert("RGBA")).astype(np.float64)
    a[..., :3] *= a[..., 3:4] / 255
    return a


def jupiter():
    """Jupiter's disc: latitude bands with turbulence (europa_r48's JUP ramp), the Great Red Spot, lit from the upper
    left with a soft terminator; RGB and coverage."""
    from europa_r48 import JUP  # noqa: E402  (concept script, unchanged)
    cx, cy = JUP_C
    nx, ny = (_xx - cx) / JUP_R, (_yy - cy) / JUP_R
    r2 = nx ** 2 + ny ** 2
    inside = r2 < 1
    nz = np.sqrt(np.clip(1 - r2, 0, 1))
    lat = np.arcsin(np.clip(ny, -1, 1))
    turb = raster.fbm(W, H, 40, 4401, octaves=5, period=False)
    belts = np.sin(lat * 9.0 + 0.6 * turb) * 0.6 + np.sin(lat * 23.0 + 1.6 * turb + 0.4 * np.sin(nx * 7 + lat * 5)) * 0.4
    v = 0.5 + 0.3 * belts
    col = raster.ramp(JUP, v)
    spot = np.exp(-(((nx + 0.18) / 0.13) ** 2 + ((ny - 0.36) / 0.065) ** 2))
    col = col * (1 - 0.55 * spot[..., None]) + np.array((196, 84, 52.0)) * 0.55 * spot[..., None]
    light = np.clip(nx * SUN[0] + ny * SUN[1] + nz * SUN[2], 0, 1) / np.linalg.norm(SUN)
    light = np.clip(light * 1.25, 0, 1) ** 0.8
    limb = 0.75 + 0.25 * nz
    rgb = col * (0.06 + 0.94 * light)[..., None] * limb[..., None]
    edge = np.clip((1 - np.sqrt(r2)) * JUP_R, 0, 1)
    return rgb, (inside * edge)


# round 52 (F.6 redo, the user: "Europa is known for its giant geysers shooting up to 200 km"): a giant water-vapour
# plume over the accepted composition, two proposals; None draws the round-51 still unchanged. The user's pick (round
# 52, T.1 = a, 2026-10-22): the one huge plume on the horizon, left of Jupiter
A4_GEYSER = "a"
A4_R52 = "r52"
# a vent: (screen x, screen y of the vent, apex height px, jet spread, lean, particles, seed, hidden below the horizon)
GEYSERS = {
    "a": [(196.0, 296.0, 240.0, 0.085, 0.0, 26000, 5201, True)],         # one huge plume on the horizon, left of Jupiter
    "b": [(156.0, 444.0, 384.0, 0.06, -0.01, 26000, 5211, False),      # a nearer one on a fissure, below the lettering
          (104.0, 392.0, 150.0, 0.09, -0.01, 9000, 5212, False),         # and a curtain of smaller ones along it,
          (58.0, 356.0, 92.0, 0.1, 0.0, 6000, 5213, False),              # receding to the horizon on the left
          (22.0, 330.0, 56.0, 0.1, 0.0, 4000, 5214, False)],
}
FISSURE = [(186.0, 468.0), (156.0, 444.0), (104.0, 392.0), (58.0, 356.0), (22.0, 330.0), (-10.0, 312.0)]


def blur(a, s):
    """A separable Gaussian blur (sigma ``s`` px), zero outside."""
    r = int(3 * s) + 1
    k = np.exp(-0.5 * (np.arange(-r, r + 1) / s) ** 2)
    k /= k.sum()
    for axis in (0, 1):
        pad = [(0, 0), (0, 0)]
        pad[axis] = (r, r)
        p = np.pad(a, pad)
        n = a.shape[axis]
        a = sum(k[i] * (p[i:i + n] if axis == 0 else p[:, i:i + n]) for i in range(2 * r + 1))
    return a


def geyser_density(x0, y0, apex, spread, lean, n, seed, behind_horizon):
    """A plume of ice grains thrown ballistically from a vent: each grain flies a parabola (a few jets fanning out at
    their own angles), its time in flight sampled evenly, so the field is thin and dense at the vent, a tall column
    widening upward, a crown where the grains turn, and a faint veil where they fall back, wider still."""
    rng = np.random.default_rng(seed)
    jets, tops = rng.normal(0, spread, 9), rng.uniform(0.55, 1.0, 9)       # each jet its own angle and height
    which = rng.integers(0, 9, n)
    ratio = jets[which] + rng.normal(0, spread * 0.3, n) + lean
    vy = np.sqrt(2 * apex * tops[which] * rng.uniform(0.15, 1, n) ** 0.6)  # most grains high, fewer lower: a round crown
    depth = rng.normal(0, spread, n) * vy * 0.3                             # their drift toward or away from the camera
    tmax = 2 * vy * 1.05                                                    # up and back down to the vent
    steps = 90
    t = rng.uniform(0, 1, (n, steps)) * tmax[:, None]
    x = x0 + (ratio * vy)[:, None] * t
    y = y0 - (vy[:, None] * t - 0.5 * t ** 2)
    w = np.exp(-t / (tmax[:, None] * 0.7))                                  # grains thin out as they fly: a faint veil
    xi, yi = np.round(x).astype(int), np.round(y).astype(int)
    ok = (xi >= 0) & (xi < W) & (yi >= 0) & (yi < H) & (y <= y0 + depth[:, None] * t)   # landed on the ice
    d = np.bincount(yi[ok] * W + xi[ok], weights=w[ok], minlength=W * H).reshape(H, W)
    d = blur(d, 1.2) * 0.6 + blur(d, 3.5) * 0.4
    d = np.maximum(d * 500.0 / n * (apex / 240.0) ** 1.6 - 0.02, 0)   # the same look for any grain count and size;
    if behind_horizon:                                                 # no faint haze at the fringe
        d = np.where(_yy > HORIZON4 + CURVE4 * (_xx - W / 2) ** 2, 0.0, d)
    return d


def draw_geysers(rgb, variant):
    """The giant plumes of a round-52 variant over the still: lit by the sun on their upper left (their own grains
    shadow the far side), a little of Jupiter's warm light on the right, ice crystals glittering in the sun."""
    dens = np.zeros((H, W))
    for g in GEYSERS[variant]:
        dens += geyser_density(*g)
    if variant == "b":                                                      # the fissure on the ice, dark with a lit lip
        for (ax, ay), (bx, by) in zip(FISSURE, FISSURE[1:]):
            seg = np.array([bx - ax, by - ay])
            t = np.clip(((_xx - ax) * seg[0] + (_yy - ay) * seg[1]) / (seg @ seg), 0, 1)
            dist = np.hypot(_xx - ax - t * seg[0], _yy - ay - t * seg[1])
            wdt = 0.6 + 2.2 * np.clip((_yy - 300) / 160, 0, 1)
            rgb = rgb * (1 - 0.75 * np.exp(-(dist / wdt) ** 2))[..., None]
            lip = np.exp(-((dist - wdt * 1.2) / (wdt * 0.6)) ** 2) * (_yy < ay + (_xx - ax) * seg[1] / seg[0])
            rgb = rgb + (lip * 40)[..., None]
    n = raster.fbm(W, H, 16, 5220, octaves=4, period=False)
    dens = dens * (0.65 + 0.7 * n)
    alpha = 1 - np.exp(-dens * 1.6)
    tau = np.zeros((H, W))                                                  # the grains between a point and the sun
    for step in range(3, 40, 3):                                            # (upper left), over a short way
        dx, dy = round(step * 0.78), round(step * 0.63)
        tau[dy:, dx:] += dens[:H - dy, :W - dx] * 3
    lit = np.exp(-tau * 0.035)
    col = (np.array((242, 248, 255.0)) * lit[..., None] + np.array((76, 88, 114.0)) * (1 - lit[..., None])
           + np.array((18, 12, 6.0)) * np.clip((_xx - 140) / 400, 0, 1)[..., None] * (1 - lit[..., None]))
    rgb = rgb * (1 - alpha[..., None]) + col * alpha[..., None]
    rng = np.random.default_rng(5230)
    glint = rng.uniform(0, 1, (H, W)) < np.clip(alpha - 0.1, 0, 0.7) * lit * 0.03
    return np.where(glint[..., None], np.array((255, 255, 255.0)), rgb)


def europa_still(geyser=None):
    hor = HORIZON4 + CURVE4 * (_xx - W / 2) ** 2
    dy = np.maximum(_yy - hor, 0.5)
    ground = _yy > hor
    z = GROUND4 / dy
    u = (_xx - W / 2) * z / FOCAL
    foot = z / FOCAL
    shelf = plane_sample(mip_chain(l22("ice-shelf")), u + 240, -z, foot, True)[..., :3]
    chaos = plane_sample(mip_chain(l22("rig-field")), u * 0.8 + 100, -z * 0.8, foot * 0.8, True)[..., :3]
    k = np.clip((z - CHAOS_Z[0]) / (CHAOS_Z[1] - CHAOS_Z[0]), 0, 1)[..., None]
    rgb = shelf * (1 - k) + chaos * k
    far = (1 - np.exp(-z / 30000.0))[..., None]                 # no air: only the texture's own falloff, a hint of
    rgb = rgb * (1 - 0.35 * far) + np.array((150, 162, 182.0)) * 0.35 * far * 0.6     # the far ice's grey
    shine = np.clip((_xx - 300) / 660, 0, 1)[..., None] * 0.12   # Jupiter's warm light on the ice to the right
    rgb = rgb * (1 - shine) + np.array((255, 190, 140.0)) * shine
    # the sky: black, a few stars, Jupiter
    sky = np.zeros((H, W, 3)) + np.array((2, 3, 8.0))
    rng = np.random.default_rng(4402)
    for _ in range(120):
        x, y = rng.integers(0, W), rng.integers(0, int(HORIZON4))
        b = rng.uniform(60, 200)
        sky[y, x] = (b, b, b * 1.05)
    jrgb, ja = jupiter()
    sky = sky * (1 - ja[..., None]) + jrgb * ja[..., None]
    rgb = np.where(ground[..., None], rgb, sky)
    rgb += np.exp(-((_yy - hor) / 1.2) ** 2)[..., None] * np.array((30, 34, 40.0)) * ground[..., None]   # the sunlit rim
    if geyser:
        rgb = draw_geysers(rgb, geyser)
    # the steam thread from the first rig, far out on the plates
    sx = STEAM_AT[0]
    base_y = HORIZON4 + CURVE4 * (sx - W / 2) ** 2 + STEAM_AT[1]
    t = np.clip((base_y - _yy) / 150, 0, 1)
    cxs = sx + 26 * t ** 1.6 + 3 * np.sin(t * 9)
    width = 1.6 + 9 * t ** 1.2
    n = raster.fbm(W, H, 8, 4403, octaves=3, period=False)
    plume = np.exp(-((_xx - cxs) / width) ** 2) * (_yy <= base_y) * (1 - t) ** 0.7 * (0.65 + 0.5 * n)
    sunward = np.clip(0.75 - (_xx - cxs) / np.maximum(width, 1) * 0.35, 0.45, 1.05)
    pcol = np.array((236, 242, 250.0)) * sunward[..., None]
    a = np.clip(plume, 0, 0.9)[..., None]
    rgb = rgb * (1 - a) + pcol * a
    d = np.hypot(_xx - sx, (_yy - base_y) * 2)
    rgb = np.where((d < 2.2)[..., None], np.array((40, 26, 22.0)), rgb)          # the rig, a dark speck
    rgb += (np.exp(-(d / 1.4) ** 2) * 0.9)[..., None] * np.array((168, 255, 42.0)) * 0.5   # its lime bore
    return np.clip(rgb, 0, 255)


def still4(geyser=A4_GEYSER):
    img = ui_dither(europa_still(geyser), np.ones((H, W)), STILL_COLOURS)
    return img.convert("RGB").convert("RGBA")


def build4():
    OUT.mkdir(parents=True, exist_ok=True)
    img = still4()
    artkit.save_png(img, OUT / f"{A4_NAME}-still.png", A4_SOURCE)
    print(f"{A4_NAME}-still.png: {img.width}x{img.height}, {artkit.colour_count([img])} colours")


def review4():
    img = Image.open(OUT / f"{A4_NAME}-still.png").convert("RGBA")
    sheet = raster.sheet(1000, 40 + 2 * (H + 30) + 10,
                         "ACT 4 TITLE-CARD STILL (FINAL R51): EUROPA UNDER JUPITER",
                         f"PRODUCTION ART, M8 PART B BATCH - {A4_ROUND.upper()}")
    raster.draw_text(sheet, 16, 38, f"THE STILL, 960X540 AT 1X ({artkit.colour_count([img])} COLOURS)", raster.LABEL)
    sheet.alpha_composite(img, (16, 52))
    raster.draw_text(sheet, 16, 52 + H + 14, f"THE TITLE CARD OVER IT: DARKENED TO {int(BRIGHTNESS * 100)} %, LETTERBOX, "
                                             "CHROME LETTERING (THE GAME ADDS THE SETTINGS LINE AND THE MISSIONS)",
                     raster.LABEL)
    sheet.alpha_composite(title_card(img, A4_NAME), (16, 52 + H + 28))
    A4_CONCEPT.mkdir(parents=True, exist_ok=True)
    path = A4_CONCEPT / f"act-4-still-final-{A4_ROUND}-a.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")


def round52():
    """Round 52's proposals: the still with a giant geyser, variants a and b, and their sheet (each still and the title
    card over it)."""
    A4_CONCEPT.mkdir(parents=True, exist_ok=True)
    stills = {}
    for v in GEYSERS:
        stills[v] = still4(v)
        path = A4_CONCEPT / f"act-4-still-{A4_R52}-{v}.png"
        stills[v].convert("RGB").save(path, optimize=True)
        print(f"{path.relative_to(ROOT)}: {artkit.colour_count([stills[v]])} colours")
    what = {"a": "A: ONE HUGE PLUME ON THE HORIZON, LEFT OF JUPITER",
            "b": "B: A NEARER PLUME ON A FISSURE, A CURTAIN OF SMALLER ONES RECEDING ALONG IT"}
    sheet = raster.sheet(1000, 40 + len(stills) * 2 * (H + 28) + 20,
                         "ACT 4 TITLE-CARD STILL (R52): A GIANT EUROPA GEYSER", "ROUND 52 F.6 REDO - PROPOSALS A/B")
    y = 38
    for v, img in stills.items():
        raster.draw_text(sheet, 16, y, f"{what[v]} - THE STILL, 960X540 AT 1X", raster.LABEL)
        sheet.alpha_composite(img, (16, y + 14))
        y += H + 28
        raster.draw_text(sheet, 16, y, f"{v.upper()}: THE TITLE CARD OVER IT (DARKENED TO {int(BRIGHTNESS * 100)} %, "
                                       "LETTERBOX, CHROME LETTERING)", raster.LABEL)
        sheet.alpha_composite(title_card(img, A4_NAME), (16, y + 14))
        y += H + 28
    path = A4_CONCEPT / f"act-4-still-{A4_R52}-sheet.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"sheet: {path.relative_to(ROOT)}")


# --------------------------------------------------------------------------- Act 5 (M9 part D batch, round 64)

A5_CONCEPT = DESIGN / "campaign" / "act-5-the-belt" / "concept"
A5_NAME = "act-5-the-belt"
A5_ROUND = "r64"
A5_SOURCE = artkit.source_note(SCRIPT, "M9 part D batch")
A5_R62 = "r62"                         # round 62's rejected still (``ceres_hub_still``): its sheet, ``act-5-r62``
HORIZON5, CURVE5 = 236.0, 1.6e-4       # Ceres's horizon: a dwarf planet's stronger curve
GROUND5 = 96_000.0                     # the crust's plane: distance ahead = GROUND5 / rows below the horizon
NEAR5 = 300.0                          # texel rows of the strip before the screen's bottom row


def ceres_hub_still():
    """Act 5's still: Ceres Hub from the approach, Level 29's ground layer (tools/art/backdrop_l29.py ``ground_strip``:
    the crust's ragged edge with the canyon's mouth, the dock canyon with its white lamps, the Helix block in charcoal
    and gold among the grey Coalition blocks, Block C across it) in perspective from high up, running to Ceres's
    curved horizon; space below the crust's edge in the foreground and over the horizon: the belt's sunlit dust band,
    stars, the sun's glare at the top left."""
    import backdrop_l29 as l29  # noqa: E402  (tools/art: the level's layout and laid ground)
    r0 = l29.CANYON_SEAM - 1000
    n = 4096
    strip = l29.ground_strip(r0, n).astype(np.float64)
    strip[..., :3] *= strip[..., 3:4] / 255
    tile = np.array(Image.open(l29.OUT / "dock-canyon.png").convert("RGBA")).astype(np.float64)
    hor = HORIZON5 + CURVE5 * (_xx - W / 2) ** 2
    dy = np.maximum(_yy - hor, 0.5)
    ground = _yy > hor
    z = GROUND5 / dy
    v = z - (GROUND5 / (H - 1 - HORIZON5) - NEAR5)            # texel rows above the strip's bottom
    u = (_xx - W / 2) * z / FOCAL + 240
    rim = np.where(u < 0, np.mod(u, 64), np.where(u >= 480, 416 + np.mod(u, 64), u))
    foot = z / FOCAL
    near = plane_sample(mip_chain(strip[::-1].copy()), rim, v, foot, False)
    far = plane_sample(mip_chain(tile[::-1].copy()), rim, v + r0, foot, True)
    k = (v >= n - 2)[..., None]
    rgba = near * (1 - k) + far * k
    a = np.clip(rgba[..., 3:4] / 255, 0, 1)
    fall = (1 - np.exp(-z / 40000.0))[..., None]
    rgb = rgba[..., :3] / np.maximum(a, 1e-6) * (1 - 0.3 * fall) + np.array((120, 110, 98.0)) * 0.3 * fall
    sky = np.zeros((H, W, 3)) + np.array((3, 3, 7.0))
    band = np.exp(-((_yy - 70 - (_xx - 480) * 0.22) / 60) ** 2) * (0.6 + 0.4 * raster.fbm(W, H, 32, 5501, octaves=3,
                                                                                         period=False))
    sky += band[..., None] * np.array((96, 76, 54.0))
    rng = np.random.default_rng(5502)
    for _ in range(160):
        x, y = rng.integers(0, W), rng.integers(0, H)
        bv = rng.uniform(60, 220)
        sky[y, x] += (bv, bv * 0.97, bv * 0.9)
    sky += (np.exp(-(np.hypot(_xx + 40, _yy + 60) / 160) ** 2) * 1.2)[..., None] * np.array((255, 236, 200.0))
    out = np.where(ground[..., None], rgb * a + sky * (1 - a), sky)
    out += (np.exp(-((_yy - hor) / 1.3) ** 2) * ground)[..., None] * np.array((60, 54, 44.0))   # the sunlit limb
    return np.clip(out, 0, 255)


# The gold wing (round 63 T.1 c, tools/concept/act5_still_r63.py's idea c, its render kit imported unchanged)
A5_SUN = (150, 404)                    # the low sun, just clear of the foreground rock's edge
A5_FRONT = (-10, 566)                  # the foreground rock's centre (radius 240 px)
A5_BIG = (858, 118)                    # the big sunlit rock top right (radius 150 px)
A5_DRIFT = (("f1", 880, 300, 30, 513, 0.5), ("f2", 380, 104, 18, 514, 0.62), ("f3", 560, 250, 12, 515, 0.66),
            ("f4", 250, 276, 22, 516, 0.55), ("f5", 724, 214, 16, 517, 0.6), ("f6", 640, 84, 10, 518, 0.68),
            ("f7", 120, 160, 14, 519, 0.6))        # the hazed small rocks: key, x, y, radius px, seed, haze share
# the wing in echelon, receding to the left, the leader nearest: x, y, span px, leader, yaw, bank, pitch (degrees;
# round 63 flew all four in one pose, round 64 turns each its own way: banking into the turn, the second levelling)
A5_TALONS = [(320, 372, 84, False, -129, 29, -38), (454, 400, 110, False, -111, 13, -31),
             (620, 402, 142, False, -121, 29, -37), (812, 402, 174, True, -108, 20, -33)]
A5_KEY = (-2.4, 0.2, 1.1)              # the sun as the renderer's point key: low at the left, close (a falloff on the gold)
A5_STRIPS = {"big": 4, "front": 8}
A5_GLARE = np.array((255, 236, 205.0))
A5_KNEE = 176.0                        # above it the still rolls off to white instead of clipping (the sun's glare)


def _a5_sheen(p, n):
    """The gold panels' albedo: a broad polished band across the wings (model space), darker toward root and tip."""
    u = np.abs(p[:, 0]) * 0.8 + p[:, 1] * 0.6
    return 0.62 + 0.61 * np.clip(1.2 - np.abs(u - 0.45) / 0.45, 0, 1) ** 1.5


def _a5_rot(yaw, bank, pitch):
    import act5_still_r63 as r63  # noqa: E402  (concept script, imported unchanged)
    return r63.rx(pitch * r63.DEG) @ r63.rz(yaw * r63.DEG) @ r63.ry(bank * r63.DEG)


def a5_talon_job(size, leader, yaw, bank, pitch):
    """A Talon for the still: round 60's b model in the production Talon's livery (tools/art/belt_units.py
    ``gunmetal``), its gold a polished metal (less mirror, a sheen band, lit by the close low key) instead of round
    63's flat chrome, which read as yellow paint."""
    import act5_still_r63 as r63  # noqa: E402
    import belt_units as bu  # noqa: E402
    import enemies_r60 as e60  # noqa: E402
    from dataclasses import replace
    from render.enemy_models import A_GOLD, hx  # noqa: E402
    scene, mats = e60.talon_model("b", leader)
    mats = bu.gunmetal(mats)
    mats[A_GOLD] = replace(mats[A_GOLD], albedo=hx("d8902a"), metal=0.45, shininess=50, spec=1.5, pattern=_a5_sheen)
    sc, mm = r63.posed(scene, mats, _a5_rot(yaw, bank, pitch), size / 2.1 / r63.PPU)
    return sc, mm, (size + 8, size + 8, 4, 80), dict(key_pos=A5_KEY, exposure=0.92)


def a5_parts():
    """The models, rendered in parallel (the big rocks in row strips): the rocks as round 63's, the Talons here."""
    import os
    import act5_still_r63 as r63  # noqa: E402
    import belt_units as bu  # noqa: E402
    from concurrent.futures import ProcessPoolExecutor
    jobs = [("big", r63.rock_job, (150, 511, (-3.4, -1.2, 0.9), 0.6, 1.25)),
            ("front", r63.rock_job, (240, 512, (1.8, 1.2, -2.2), 0.2, 1.6))]
    for i, (key, _, _, r, seed, _) in enumerate(A5_DRIFT):
        jobs.append((key, r63.rock_job, (r, seed, (-3.4, -0.4, 1.0), 0.3 * i)))
    for i, (_, _, size, leader, yaw, bank, pitch) in enumerate(A5_TALONS):
        jobs.append((f"t{i}", a5_talon_job, (size, leader, yaw, bank, pitch)))
    work = [(name, fn, args, i, A5_STRIPS.get(name, 1)) for name, fn, args in jobs for i in range(A5_STRIPS.get(name, 1))]
    got = {}
    with ProcessPoolExecutor(max_workers=min(16, os.cpu_count() or 4)) as ex:
        for name, i, img in ex.map(r63._run, work):
            got.setdefault(name, {})[i] = img
    parts = {name: Image.fromarray(np.concatenate([np.array(rows[i]) for i in sorted(rows)], axis=0), "RGBA")
             for name, rows in got.items()}
    for i in range(len(A5_TALONS)):
        parts[f"t{i}"] = bu.rim(parts[f"t{i}"])
    return parts


def a5_finish(rgb, soft):
    """One 96-colour palette with 4x4 ordered dither (round 63's ``finish``): the median cut weighs the saturated
    accents and the glare's gradient, and the dither is stronger in the glare (``soft`` 0..1), so it fades smoothly
    instead of stepping into posterized rings."""
    b = np.tile(artkit.BAYER4, (H // 4 + 1, W // 4 + 1))[:H, :W]
    px = np.clip(rgb + ((b - 0.5) * (10.0 + 12.0 * soft))[..., None], 0, 255).astype(np.uint8)
    flat = px.reshape(-1, 3).astype(int)
    accent = flat[(flat.max(1) - flat.min(1) > 80) & (flat.max(1) > 110)]
    glare = flat[soft.reshape(-1) > 0.15]
    sample = np.concatenate([flat, np.repeat(accent, 24, axis=0), np.repeat(glare, 6, axis=0)]).astype(np.uint8)
    palette = Image.fromarray(sample.reshape(-1, 1, 3), "RGB").quantize(
        colors=STILL_COLOURS, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE)
    out = Image.fromarray(px, "RGB").quantize(palette=palette, dither=Image.Dither.NONE)
    return out.convert("RGB").convert("RGBA")


def gold_wing_still(parts):
    """Act 5's still, the gold wing: the belt's rocks close up, the sun low on the left beside a huge rim-lit foreground
    rock, a big sunlit rock top right, a drift of small rocks hazed in the dust band; a wing of four Talons (the
    production livery: gunmetal, gold outer panels, the leader's gold spine) in echelon below the lettering, each
    banked its own way, gold glints and their engines' short trails. The sun's glare is a soft one: a small white
    core, a halo falling off as 1 / (1 + d^2) and a wide warm wash, rolled off to white above a knee."""
    import act5_still_r63 as r63  # noqa: E402
    import belt_units as bu  # noqa: E402
    sx, sy = A5_SUN
    rgb = r63.space(6331, (-9999.0, -9999.0), 330, -0.24, band_k=1.25, stars=120)     # the sky without its glare
    d = np.hypot(_xx - sx, _yy - sy)
    rgb += (np.exp(-(d / 520) ** 2) * 0.18)[..., None] * np.array((170, 130, 90.0))
    haze = np.array((120, 98, 74.0))
    for key, x, y, _, _, k in A5_DRIFT:
        im = np.array(parts[key]).astype(np.float64)
        im[..., :3] = im[..., :3] * (1 - k) + haze * k
        rgb = r63.paste(rgb, Image.fromarray(im.astype(np.uint8), "RGBA"), x, y)
    rgb = r63.paste(rgb, parts["big"], *A5_BIG)
    for i, (x, y, size, _, yaw, bank, pitch) in enumerate(A5_TALONS):
        rot = _a5_rot(yaw, bank, pitch)
        nose = rot @ np.array((0, 1.0, 0))
        back = -np.array((nose[0], -nose[1])) / np.hypot(nose[0], nose[1])
        for ex in (-0.12, 0.12):                                                   # the twin engines' short trails
            e = rot @ np.array((ex, -0.74, 0)) * size / 2.1
            px, py = x + e[0], y - e[1]
            for t in np.linspace(0, 1, 12):
                rgb = r63.glow(rgb, px + back[0] * 0.5 * size * t, py + back[1] * 0.5 * size * t, 1.6 + 2.5 * t,
                               (255, 110, 50), 0.45 * (1 - t))
        rgb = r63.paste(rgb, parts[f"t{i}"], x, y)
        for ex in (-0.12, 0.12):
            e = rot @ np.array((ex, -0.74, 0)) * size / 2.1
            rgb = r63.glow(rgb, x + e[0], y - e[1], 2.5 + size / 80, (255, 170, 100), 1.2)
    rgb = r63.paste(rgb, parts["front"], *A5_FRONT)
    m = r63.mask_of(parts["front"], *A5_FRONT)                     # the glare spilling over the rock's edge
    pad = np.pad(m, 2)
    edge = m & ~(pad[:-4, 2:-2] & pad[2:-2, 4:] & pad[4:, 2:-2] & pad[2:-2, :-4])
    rgb += (edge * np.exp(-(d / 110) ** 2) * 1.0)[..., None] * np.array((255, 226, 180.0))
    glare = np.exp(-(d / 11) ** 2) * 3.0 + 1.1 / (1 + (d / 34) ** 2) + 0.32 / (1 + (d / 150) ** 2)
    rgb += (glare * ~m + glare * m * 0.25)[..., None] * A5_GLARE              # the rock in front dims it
    over = np.maximum(rgb - A5_KNEE, 0)
    rgb = np.where(rgb < A5_KNEE, rgb, A5_KNEE + (255 - A5_KNEE) * (1 - np.exp(-over / (255 - A5_KNEE))))
    img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8)).convert("RGBA")
    for i, (x, y, size, *_rest) in enumerate(A5_TALONS):                    # gold glints where the sun catches a panel
        t = parts[f"t{i}"]
        gx, gy = r63.gold_glint(t)
        w = 11 + size // 14 // 2 * 2
        img = artkit.add_light(img, bu.star(w, 1.0, (255, 214, 120)),
                               (int(x - t.width / 2 + gx - w / 2), int(y - t.height / 2 + gy - w / 2)))
    soft = np.clip(1.6 / (1 + (d / 90) ** 2), 0, 1)
    return a5_finish(np.array(img)[..., :3].astype(np.float64), soft)


def build5():
    OUT.mkdir(parents=True, exist_ok=True)
    img = gold_wing_still(a5_parts())
    artkit.save_png(img, OUT / f"{A5_NAME}-still.png", A5_SOURCE)
    print(f"{A5_NAME}-still.png: {img.width}x{img.height}, {artkit.colour_count([img])} colours")


def review5(img=None, rnd=A5_ROUND):
    """The review sheet: the still at 1x, the title card over it, and (round 64) 2x crops of the wing and the glare.
    ``img`` + ``rnd`` = A5_R62: round 62's rejected still's sheet, into concept/rejected/."""
    final = img is None
    img = Image.open(OUT / f"{A5_NAME}-still.png").convert("RGBA") if final else img
    crops = [(712, 310, 912, 490), (36, 314, 276, 494)] if final else []        # the leader; the sun by the rock
    title = ("ACT 5 TITLE-CARD STILL (FINAL R64): THE GOLD WING" if final
             else "ACT 5 TITLE-CARD STILL (FINAL R62): CERES HUB")
    batch = "M9 PART D BATCH" if final else "M9 PART B BATCH"
    sheet = raster.sheet(1000, 40 + 2 * (H + 30) + (400 if crops else 0) + 10, title,
                         f"PRODUCTION ART, {batch} - {rnd.upper()}")
    raster.draw_text(sheet, 16, 38, f"THE STILL, 960X540 AT 1X ({artkit.colour_count([img])} COLOURS)", raster.LABEL)
    sheet.alpha_composite(img, (16, 52))
    raster.draw_text(sheet, 16, 52 + H + 14, f"THE TITLE CARD OVER IT: DARKENED TO {int(BRIGHTNESS * 100)} %, LETTERBOX, "
                                             "CHROME LETTERING (THE GAME ADDS THE SETTINGS LINE AND THE MISSIONS)",
                     raster.LABEL)
    sheet.alpha_composite(title_card(img, A5_NAME), (16, 52 + H + 28))
    if crops:
        y = 52 + 2 * H + 28 + 16
        raster.draw_text(sheet, 16, y, "2X DETAILS: THE WING'S GOLD AND BANKS; THE SUN'S SOFT GLARE BY THE FOREGROUND ROCK",
                         raster.LABEL)
        x = 16
        for box in crops:
            part = img.crop(box)
            sheet.alpha_composite(part.resize((part.width * 2, part.height * 2), Image.NEAREST), (x, y + 14))
            x += part.width * 2 + 16
    folder = A5_CONCEPT if final else A5_CONCEPT / "rejected"
    folder.mkdir(parents=True, exist_ok=True)
    path = folder / f"act-5-still-final-{rnd}-a.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")


if __name__ == "__main__":
    if "--check" in sys.argv[1:]:
        bad = 0
        for name, src in (("act-1-first-contact", SOURCE), (A2_NAME, A2_SOURCE), ("act-3-red-dust", A3_SOURCE),
                          (A4_NAME, A4_SOURCE), (A5_NAME, A5_SOURCE)):
            path = OUT / f"{name}-still.png"
            img = Image.open(path) if path.exists() else None
            note = img.text.get(artkit.SOURCE_KEY, "") if img else ""
            ok = img is not None and img.size == (W, H) and note.startswith(f"tools/art/{SCRIPT}") and (
                src is None or note == src)
            print(f"{'ok ' if ok else 'BAD'} {path.relative_to(ROOT)}: {img.size if img else None} {note!r}")
            bad += not ok
        sys.exit(1 if bad else 0)
    if "act-4-r52" in sys.argv[1:]:
        round52()
        sys.exit(0)
    if "act-5-r62" in sys.argv[1:]:
        review5(ui_dither(ceres_hub_still(), np.ones((H, W)), STILL_COLOURS).convert("RGB").convert("RGBA"), A5_R62)
        sys.exit(0)
    acts = [a for a in sys.argv[1:] if a in ("act-1", "act-2", "act-3", "act-4", "act-5")] or [
        "act-1", "act-2", "act-3", "act-4", "act-5"]
    if "act-1" in acts:
        if "--review" not in sys.argv[1:]:
            build()
        review()
    if "act-2" in acts:
        if "--review" not in sys.argv[1:]:
            build2()
        review2()
    if "act-3" in acts:
        if "--review" not in sys.argv[1:]:
            build3()
        review3()
    if "act-4" in acts:
        if "--review" not in sys.argv[1:]:
            build4()
        review4()
    if "act-5" in acts:
        if "--review" not in sys.argv[1:]:
            build5()
        review5()
