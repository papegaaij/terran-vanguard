#!/usr/bin/env python3
"""Production art: the Act 3 outro's four briefing images (design/campaign/act-3-red-dust, Act outro; M7 part H,
step ART-H3), one per outro page, in the chosen briefing-r08-a look of tools/art/briefing_images.py (its Board,
palettes, colours and labels) and tools/art/outro_l14_images.py's OutroBoard (Mars, big labels, the top layer),
both imported unchanged. Commander Okafor speaks pages 1, 2 and 4, Dr. Varga page 3.

Outputs (672x240, the briefing screen's image size):
  act-3-outro-hellas  p1: the Hellas basin at dawn from low over the floor (a heightfield of dunes, hillshaded with
                      cast shadows from the low sun in the east, set in perspective with the dust haze): the
                      Colossus's collapsed crater with its slumped terraces, the lime ichor pooled in its throat and
                      the bone mandibles' tips sunk in the sand, the ridge's sunken furrow leading off; the last spore
                      trails climbing out of the frame from the far basin; lift-off from every site, the shelters open
  act-3-outro-domes   p2: Ares Landing's domes at night, every pane lit (the geodesic lattice of
                      briefing_images.colonists, dome a of design/world/mars), the colonists on the ring road in front
                      of them as silhouettes looking up, some waving; the stars, Phobos
  act-3-outro-europa  p3: Varga's tracking plot: the Vrell fleet's courses off Mars bending toward Jupiter and
                      converging on Europa; in the corner a minimised window with the walker's filed plate (a crop of
                      assets/ui/briefing/level-20-filed-plate.png), unlabelled beyond its file name
  act-3-outro-aegis   p4: Aegis Wing on Ares Landing's spaceport apron at dusk, seen from the tower: the production
                      Stormhawk and Ember sprites (assets/) foreshortened on their pads, ground crews and their work
                      lights refitting them, the domes on the horizon, Jupiter a bright point in the dusk sky
  design/ui/briefing/concept/act3-outro-final-r44-a.png
                      the review sheet: the four images at 1x, labelled

Composed as briefing_images composes its pages: the display layer posterized to 24 colours with ordered dither, the
lines and labels to 16, each sprite on a palette of its own.

Run: python3 tools/art/outro_l21_images.py [hellas|domes|europa|aegis ...] [--assets] [--review]
(~10 s; after tools/art/stormhawk.py, rook.py and briefing_images.py level-20-filed-plate, whose files it reads).
By default all four are rendered into the review sheet only; --assets also writes the named ones (all four if none
is named) to assets/ui/briefing/act-3-outro-<name>.png, Source-tagged; --review rebuilds the sheet from the files in
assets/, rendering only those still missing there.
"""
import math
import sys

import numpy as np
from PIL import Image

import artkit
import briefing_images as bi
import outro_l14_images as o14
from artkit import ROOT
from briefing_images import AMBER, CYAN, CYAN_DIM, GREEN, GREY, H, RED, VIOLET, W, WHITE

from render import raster  # noqa: E402

SCRIPT = "outro_l21_images.py"
SOURCE = artkit.source_note(SCRIPT, "M7 part H batch")
ROUND = "r44"
OUT = bi.OUT
CONCEPT = bi.CONCEPT
PREFIX = "act-3-outro-"
SAND = [(0.0, (40, 18, 12)), (0.3, (98, 48, 28)), (0.6, (168, 98, 58)), (0.85, (214, 156, 104)),
        (1.0, (240, 200, 150))]
DAWN = (255, 214, 168)          # the low sun
PANE = (255, 206, 128)          # a lit dome pane


def palettised(img, colours=o14.SPRITE_COLOURS):
    return artkit.quantize_set([img], colours)[0]


def frame_box(b, x0, y0, x1, y1):
    b.draw.rectangle([x0, y0, x1, y1], outline=CYAN_DIM + (200,))


# --------------------------------------------------------------------------- p1: the crater at dawn

GW, GD = 1300, 700              # the ground grid: across, deep (one cell = one unit; row 0 nearest)
CRATER = (600.0, 153.0, 92.0)   # its centre (x, row) and radius on the grid
CAM_F, CAM_H, NEAR = 300.0, 65.0, 120.0   # the camera: focal length (px), height, the grid's first row's depth


def basin_floor():
    """The floor round the crater as a heightfield on the ground grid and its albedo: dunes, the crater's funnel with
    slumped terraces and a raised rim, the ridge's sunken furrow, the lime pool in the throat, the mandibles' tips."""
    zz, xx = np.mgrid[0:GD, 0:GW].astype(np.float64)
    f1 = raster.fbm(GW, GD, 64, 7101, octaves=4, period=False)
    f2 = raster.fbm(GW, GD, 12, 7103, octaves=3, period=False)
    dunes = 3.2 * np.sin((xx * 0.8 + zz * 0.5 + 30 * f1) / 26.0) + 1.2 * np.sin((xx - zz) / 9.0 + 6 * f1)
    h = dunes + 14 * (f1 - 0.5) + 2 * (f2 - 0.5)
    cx, cz, R = CRATER
    r = np.hypot(xx - cx, (zz - cz) * 1.0)
    u = r / R
    funnel = -38 * np.clip(1 - u, 0, 1) ** 1.4
    slump = -3.5 * np.clip(1 - u, 0, 1) * (np.abs(np.sin(u * 18 + 3 * f1)) > 0.6)    # the slumped terraces
    rim = 7 * np.exp(-((u - 1.04) / 0.1) ** 2) * (0.7 + 0.6 * f1)
    h = np.where(u < 1.3, h * np.clip((u - 0.6) / 0.7, 0, 1), h) + funnel + slump + rim
    # the furrow: the ridge's trench, sunk, from the crater off to the far left
    t = np.clip((cx - xx) / 420, 0, 1)
    path_z = cz + 160 * t ** 1.3 + 30 * np.sin(t * 5)
    dist = np.abs(zz - path_z)
    h -= 9 * np.exp(-(dist / 16) ** 2) * (xx < cx - R * 0.8) * np.clip(1 - t * 0.6, 0, 1)
    alb = raster.ramp(SAND, np.clip(0.55 + 0.25 * (f2 - 0.5) + 0.25 * (f1 - 0.5), 0, 1))
    # dark collapsed sand inside, lime ichor pooled in the throat, lime streaks down the funnel
    alb = np.where((u < 0.95)[..., None], alb * 0.82, alb)
    streak = (np.abs(np.sin(np.arctan2(zz - cz, xx - cx) * 11 + 4 * f2)) < 0.12) & (u < 0.6)
    alb = np.where(streak[..., None], np.array([120, 150, 60.0]), alb)
    pool = u < 0.2 + 0.05 * (f2 - 0.5)
    alb = np.where(pool[..., None], np.array([150, 200, 70.0]) * (0.7 + 0.3 * f2)[..., None], alb)
    h = np.where(pool, -38 * (1 - 0.2) ** 1.4, h)
    # the mandibles' tips: four bone hooks half sunk on the funnel's sides
    for k in range(4):
        a = math.radians(30 + 90 * k + 12 * math.sin(k * 2.7))
        for s in np.linspace(0, 1, 18):
            rr = R * (0.32 + 0.3 * s)
            px, pz = cx + rr * math.cos(a + 0.5 * s), cz + rr * math.sin(a + 0.5 * s)
            w = 5.5 * (1 - 0.7 * s)
            m = np.hypot(xx - px, zz - pz) < w
            alb = np.where(m[..., None], np.array([222, 210, 186.0]), alb)
            h = np.where(m, np.maximum(h, funnel + 6 * (1 - s) + 2), h)
    return h, alb


def cast_shadow(h, tan_elev, step=1.0):
    """Shadows from a sun on the +x side (east) at an elevation with tangent ``tan_elev``: a cell is lit when no cell
    east of it rises above the sun's line."""
    lit = np.ones_like(h)
    horizon = np.full(h.shape[0], -1e9)
    for x in range(h.shape[1] - 1, -1, -1):
        horizon -= tan_elev * step
        lit[:, x] = h[:, x] >= horizon
        horizon = np.maximum(horizon, h[:, x])
    return lit


def hellas():
    """p1: the Hellas basin at dawn, the Colossus's crater, the last spore trails."""
    b = o14.OutroBoard()
    sx0, sx1, sy0, sy1 = 14, 470, 20, 228
    hz = 78                                                     # the far horizon in the frame
    # the sky: dawn on Mars, the sun just up in the east (right), its blue halo, butterscotch overhead
    t = np.clip((b.yy - sy0) / (hz - sy0), 0, 1)
    sky = raster.ramp([(0.0, (70, 48, 52)), (0.6, (168, 112, 80)), (1.0, (222, 168, 118))], t)
    sun_x, sun_y = 452.0, hz - 6
    d = np.hypot(b.xx - sun_x, (b.yy - sun_y) * 1.3)
    sky += (np.exp(-(d / 34) ** 2) * 0.9)[..., None] * np.array([70, 110, 150.0])
    sky += (np.exp(-(d / 5) ** 2))[..., None] * np.array([120, 120, 110.0])
    # the ground in perspective: screen row y at distance zf / (y - hz)
    h, alb = basin_floor()
    gy, gx = np.gradient(h)
    nx, nz = -gx, -gy
    norm = np.sqrt(nx ** 2 + nz ** 2 + 1)
    sun = np.array([0.92, -0.12, 0.36])                        # (x east, z away, up)
    diffuse = np.clip((nx * sun[0] + nz * sun[1] + sun[2]) / norm, 0, 1)
    lit = cast_shadow(h, sun[2] / sun[0])
    shade = 0.28 + 0.95 * diffuse * (0.25 + 0.75 * lit)
    col = alb * shade[..., None] * (np.array(DAWN) / 255)[None, None]
    col += (1 - lit)[..., None] * np.array([6, 10, 22.0])      # the cool sky fill in the shadows
    ys = np.arange(H, dtype=np.float64)
    depth = np.where(ys > hz, CAM_H * CAM_F / np.maximum(ys - hz, 1e-3), 1e9)
    zi = depth - NEAR                                                   # the grid row seen on screen row y
    xi = (b.xx - (sx0 + sx1) / 2) * (depth[:, None] / CAM_F) + GW / 2
    inside = (zi[:, None] >= 0) & (zi[:, None] < GD - 0.5) & (xi >= 0) & (xi < GW - 1) & (b.yy > hz)
    zi2 = np.clip(np.round(zi), 0, GD - 1).astype(int)[:, None].repeat(W, 1)
    xi2 = np.clip(np.round(xi), 0, GW - 1).astype(int)
    ground = col[zi2, xi2]
    haze = np.clip(1 - (b.yy - hz) / 70, 0, 1) ** 1.6              # the dust haze toward the horizon
    ground = ground * (1 - haze[..., None] * 0.8) + haze[..., None] * np.array([200, 150, 108.0]) * 0.8
    far = (b.yy > hz) & ~inside
    ground = np.where(far[..., None], np.array([186, 136, 98.0]), ground)
    scene = np.where((b.yy <= hz)[..., None], sky, ground)
    box = (b.xx >= sx0) & (b.xx <= sx1) & (b.yy >= sy0) & (b.yy <= sy1)
    b.base = np.where(box[..., None], scene, b.base)
    # the far rim of the basin on the horizon, low and blue-grey in the dawn
    rim = hz - 3 - 3 * np.sin(b.xx / 31.0) - 2 * np.sin(b.xx / 9.0 + 1) - 4 * np.exp(-((b.xx - 120) / 60) ** 2)
    b.base = np.where((box & (b.yy > rim) & (b.yy <= hz + 1))[..., None], np.array([138, 104, 90.0]), b.base)
    # the last spore trails: from the far basin up out of the frame, drifting west
    rng = np.random.default_rng(7111)
    for k, x in enumerate((60, 150, 228, 300, 360, 410)):
        top_y = sy0 - 8
        n = 60
        sway = rng.uniform(-60, -10)
        pts = [(x + sway * (s ** 1.6) + 4 * math.sin(s * 9 + k), hz - 2 + (top_y - hz + 2) * s) for s in np.linspace(0, 1, n)]
        for i, (px, py) in enumerate(pts):
            if py >= sy0 and sx0 <= px <= sx1:
                b.glow(px, py, 1.0 + 1.2 * i / n, (54, 30, 74), 0.5 * (1 - 0.5 * i / n))
        hx, hy = pts[int(n * (0.55 + 0.08 * k % 0.4))]
        if sy0 + 4 < hy:
            b.glow(hx, hy, 3, (120, 70, 160), 0.8)
            b.marker(hx, hy, "dot", (40, 24, 40), 1)
    b.draw.rectangle([sx0, sy0, sx1, sy1], outline=CYAN_DIM + (200,))
    # the callouts: the crater on screen (its centre row: depth of the crater's grid row)
    cdepth = CRATER[1] + NEAR
    crow = hz + CAM_H * CAM_F / cdepth
    ccx = (sx0 + sx1) / 2 + (CRATER[0] - GW / 2) * CAM_F / cdepth
    b.glow(ccx, crow + 4, 12, (70, 110, 24), 0.8)                     # the lime ichor pooled in the throat
    b.bracket(ccx - 74, crow - 22, ccx + 74, crow + 26, AMBER, 6)
    bi.tag(b, ccx + 82, crow - 8, "THE COLOSSUS - DEAD", AMBER)
    bi.tag(b, ccx + 82, crow + 3, "ITS CRATER, COLLAPSED", WHITE)
    bi.tag(b, sx0 + 8, sy0 + 34, "SPORE TRAILS - CLIMBING OUT", VIOLET)
    bi.tag(b, sx0 + 8, hz + 30, "THE RIDGE - SUNK", CYAN_DIM)
    b.title("HELLAS BASIN - 06:10, DAWN")
    b.label(W - 10, 9, "THE COLOSSUS IS DEAD", AMBER, right=True)
    px = 482
    b.line([(px - 6, 26), (px - 6, H - 12)], CYAN_DIM, 1, 90)
    b.label(px, 30, "WITHIN A DAY", WHITE)
    b.label(px, 40, "VRELL LIFT-OFF", RED)
    for i, name in enumerate(("THE BASIN", "THE PLAINS", "THE FOUNDRY")):
        b.label(px, 56 + 10 * i, "- " + name, CYAN)
    b.label(px, 90, "EVERY SITE ON MARS", RED)
    b.line([(px, 106), (W - 12, 106)], CYAN_DIM, 1, 120)
    b.label(px, 114, "ARES LANDING", WHITE)
    b.label(px, 124, "SHELTERS OPEN", GREEN)
    b.label(px, 140, "FIRST TIME IN", AMBER)
    b.big(px, 152, "6 WEEKS", AMBER)
    b.label(W - 10, H - 16, "THIS MORNING", CYAN_DIM, right=True)
    return b


# --------------------------------------------------------------------------- p2: every dome lit

def dome(b, cx, base, R, seed, sp=0.16, near=1.0):
    """A geodesic dome (dome a of design/world/mars: struts in a triangle lattice) seen from its ring road at night,
    every pane lit warm with its own brightness; returns the dome's mask."""
    nx, ny = (b.xx - cx) / R, (b.yy - base) / (R * 0.82)
    rr = nx ** 2 + ny ** 2
    m = (rr < 1) & (b.yy < base)
    nz = np.sqrt(np.clip(1 - rr, 0, 1))
    az = np.arctan2(nx, nz)
    el = np.arcsin(np.clip(-ny, -1, 1))
    u, v, w_ = el / sp, (az + el * 0.577) / sp, (az - el * 0.577) / sp
    width = 0.07 / near
    strut = (np.abs(u - np.round(u)) < width / np.maximum(nz, 0.3)) | \
            (np.abs(v - np.round(v)) < width * 0.9 / np.maximum(nz, 0.3)) | \
            (np.abs(w_ - np.round(w_)) < width * 0.9 / np.maximum(nz, 0.3))
    cell = np.floor(u) * 7919 + np.floor(v) * 104729 + np.floor(w_) * 1299709 + seed * 31
    hsh = (np.sin(cell * 12.9898) * 43758.5453) % 1.0
    glass = np.array(PANE, float) * (0.55 + 0.45 * hsh)[..., None] * (0.75 + 0.35 * nz)[..., None]
    glass = np.where((hsh > 0.86)[..., None], np.array([255, 236, 190.0]), glass)      # the brightest panes
    rows = (np.sin(b.yy / 2.4) > 0.3) & (b.yy > base - R * 0.3)                         # crops and floors inside
    glass = np.where(rows[..., None], glass * 0.82, glass)
    glass = np.where(strut[..., None], np.array([46, 34, 30.0]) * (0.8 + 0.4 * nz)[..., None], glass)
    b.base = np.where(m[..., None], glass, b.base)
    b.glow(cx, base - R * 0.4, R * 1.3, (70, 46, 22), 0.55)
    return m


def onlooker(b, x, foot, tall, wave=0, seed=0):
    """A small figure on the ring road, dark against the domes' light, its head tilted back to look up; ``wave``
    raises one arm (1) or both (2)."""
    dark = (12, 10, 18, 255)
    rim = PANE + (170,)
    d = b.draw
    w = max(3.0, tall * 0.3)
    r = max(1.6, tall * 0.11)
    hip, neck = foot - tall * 0.42, foot - tall * 0.8
    d.line([(x - w * 0.25, foot), (x - w * 0.2, hip)], fill=dark, width=2)
    d.line([(x + w * 0.25, foot), (x + w * 0.2, hip)], fill=dark, width=2)
    d.polygon([(x - w / 2, hip), (x + w / 2, hip), (x + w / 2 - 0.5, neck), (x - w / 2 + 0.5, neck)], fill=dark)
    hx, hy = x + (0.6 if seed % 2 else -0.6), neck - r * 1.3                    # tilted back: the face up
    d.ellipse([hx - r, hy - r, hx + r, hy + r], fill=dark)
    if wave >= 1:
        d.line([(x + w / 2, neck + 1), (x + w * 0.9, neck - tall * 0.42)], fill=dark, width=2)
    if wave >= 2:
        d.line([(x - w / 2, neck + 1), (x - w * 0.9, neck - tall * 0.42)], fill=dark, width=2)
    d.line([(x - w / 2, neck + 1), (x - w / 2, hip)], fill=rim, width=1)                 # the rim light behind them
    d.ellipse([hx - r - 0.5, hy - r - 0.5, hx - r + 1.2, hy + r - 1], fill=rim)


def domes():
    """p2: Ares Landing at night, every dome burning its lights, the colonists on the ring road looking up."""
    b = o14.OutroBoard()
    hz = 182
    t = np.clip(b.yy / hz, 0, 1)
    b.base = raster.ramp([(0.0, (4, 4, 14)), (0.7, (12, 10, 26)), (1.0, (34, 22, 30))], t)
    rng = np.random.default_rng(7201)
    for _ in range(140):
        x, y = rng.uniform(0, W), rng.uniform(0, hz - 20)
        b.base[int(y), int(x)] += rng.uniform(40, 150)
    b.glow(560, 44, 4, (120, 110, 100), 1.0)                                         # Phobos
    ground = b.yy > hz
    b.base = np.where(ground[..., None], np.array([30, 20, 18.0]) + 6 * np.sin(b.xx / 7.0)[..., None], b.base)
    # the domes, far to near; every pane lit
    for cx, base, R, seed in ((614, hz, 40, 5), (40, hz, 52, 4), (520, hz, 70, 3), (126, hz, 96, 2)):
        dome(b, cx, base, R, seed, sp=0.2 if R < 60 else 0.16, near=R / 100 + 0.5)
    dome(b, 330, hz + 6, 132, 1, sp=0.14, near=1.4)
    # the ring road in front, the light thrown on it
    road = b.yy >= 204
    b.base = np.where(road[..., None], np.array([44, 34, 32.0]) + 10 * ((b.xx % 56) > 1)[..., None]
                      - 20 * (b.yy < 206)[..., None], b.base)
    b.base += (np.exp(-((b.yy - 196) / 14) ** 2) * (b.yy > hz))[..., None] * np.array([60, 40, 18.0])
    b.title("ARES LANDING - LAST NIGHT, 23:40")
    b.label(W - 10, 9, "TAKE THAT WITH YOU", AMBER, right=True)
    # the colonists, looking up
    xs = list(range(40, 640, 23))
    for i, x in enumerate(xs):
        x += int(rng.integers(-6, 7))
        tall = float(rng.uniform(15, 19))
        wave = 2 if i % 7 == 3 else 1 if i % 4 == 1 else 0
        onlooker(b, x, 226 + int(rng.integers(-2, 3)), tall, wave, seed=i)
    for name, x, y in (("BRADBURY", 296, 30), ("LOWELL", 98, 70), ("EDEN", 500, 98)):
        bi.tag(b, x, y, name, CYAN_DIM)
    bi.tag(b, 14, 26, "EVERY DOME - EVERY PANE", AMBER)
    bi.tag(b, 14, 37, "LIT FOR AEGIS WING", WHITE)
    bi.tag(b, W - 10 - raster.text_width("NOT FORGIVEN - NOT FORGOTTEN"), 26, "NOT FORGIVEN - NOT FORGOTTEN", GREY)
    bi.tag(b, 14, 190, "THE RING ROAD", CYAN_DIM)
    return b


# --------------------------------------------------------------------------- p3: the plot to Europa

def plate_window(w=118, h=62):
    """The minimised window in the plot's corner: the filed plate's photo (a crop of Level 20's briefing image)
    under a title bar with only its file name."""
    src = Image.open(OUT / "level-20-filed-plate.png").convert("RGB")
    crop = src.crop((96, 52, 336, 178)).resize((w - 4, h - 14), Image.BOX)
    win = Image.new("RGBA", (w, h), (8, 12, 26, 255))
    win.paste(crop, (2, 12))
    raster.draw_text(win, 4, 3, "WRECK 19-A.IMG", (150, 170, 190, 255))
    raster.draw_text(win, w - 10, 3, "_", (150, 170, 190, 255))
    a = np.array(win)
    a[0, :], a[-1, :], a[:, 0], a[:, -1] = (40, 130, 180, 255), (40, 130, 180, 255), (40, 130, 180, 255), (40, 130, 180, 255)
    a[10, :] = (40, 130, 180, 255)
    return palettised(Image.fromarray(a, "RGBA"), 32)


def jupiter(b, cx, cy, r):
    """Jupiter as the display draws a planet (Board.planet's banded giant) in its own cream and tan ramp, so it
    does not take Mars's colours from the display's palette."""
    d = np.hypot(b.xx - cx, b.yy - cy)
    nx, ny = (b.xx - cx) / r, (b.yy - cy) / r
    lum = 0.6 + 0.25 * np.sin(ny * 9 + 0.6 * np.sin(nx * 3)) + 0.08 * np.sin(ny * 23)
    spot = np.hypot((nx - 0.25) / 0.2, (ny - 0.35) / 0.09) < 1                       # the Great Red Spot
    light = np.clip(0.35 - 0.55 * nx - 0.55 * ny + 0.5 * np.sqrt(np.clip(1 - nx ** 2 - ny ** 2, 0, 1)), 0.08, 1)
    body = raster.ramp([(0.0, (16, 10, 8)), (0.5, (110, 80, 56)), (1.0, (226, 200, 160))], np.clip(lum * light, 0, 1))
    body = np.where(spot[..., None], np.array([150, 70, 46.0]) * light[..., None], body)
    b.base = np.where((d < r)[..., None], body, b.base)
    b.base += (np.exp(-((d - r) / 3) ** 2) * 0.8)[..., None] * np.array([150, 120, 80])


def europa():
    """p3: Varga's plot: every course off Mars bends toward Jupiter and ends at Europa."""
    b = o14.OutroBoard()
    mx, my, mr = 70, 132, 40
    b.glow(mx, my, 70, (60, 18, 10), 0.7)
    b.mars(mx, my, mr)
    jx, jy, jr = 590, 118, 58
    b.glow(jx, jy, 110, (40, 30, 20), 0.6)
    jupiter(b, jx, jy, jr)
    ex, ey = 506, 82                                                       # Europa
    b.planet(ex, ey, 6, "moon", seed=21)
    b.ring(jx, jy, math.hypot(ex - jx, ey - jy), colour=CYAN_DIM, alpha=70)
    b.title("TRACKING - VRELL FLEET OFF MARS")
    b.label(W - 10, 9, "NOW WE KNOW WHERE", AMBER, right=True)
    # the courses: off Mars in every direction, each bending round toward Jupiter, all ending at Europa
    rng = np.random.default_rng(7301)
    for k, a in enumerate(np.radians(np.linspace(-70, 70, 7))):
        p0 = np.array([mx + (mr + 6) * math.cos(a), my + (mr + 6) * math.sin(a)])
        c1 = p0 + np.array([math.cos(a), math.sin(a)]) * rng.uniform(90, 150)
        c2 = np.array([ex - 120.0, ey + (k - 3) * 22])
        p3 = np.array([ex - 7.0, ey + (k - 3) * 0.8])
        pts = [tuple((1 - s) ** 3 * p0 + 3 * (1 - s) ** 2 * s * c1 + 3 * (1 - s) * s ** 2 * c2 + s ** 3 * p3)
               for s in np.linspace(0, 1, 26)]
        b.dashed(pts[:-1], RED, 1, 4, 3)
        b.arrow(*pts[-3], *pts[-2], RED, size=2, dashed=False)
        for s in (0.3, 0.55):
            q = pts[int(25 * s)]
            b.marker(q[0], q[1] - 2, "chevron", RED, 2)
    b.ring(ex, ey, 11, colour=AMBER, alpha=230)
    b.ring(ex, ey, 17, colour=AMBER, alpha=140)
    bi.tag(b, ex - 46, ey - 30, "EUROPA", AMBER)
    bi.tag(b, jx - 16, jy + jr + 6, "JUPITER", CYAN_DIM)
    bi.tag(b, mx - 14, my + mr + 8, "MARS", CYAN_DIM)
    bi.tag(b, 136, 30, "COURSES PLOTTED: 7", WHITE)
    bi.tag(b, 136, 41, "ALL BEND TOWARD JUPITER", RED)
    bi.tag(b, 136, 52, "ALL END AT ONE MOON", AMBER)
    bi.tag(b, 250, 196, "NOT GOING HOME", RED)
    bi.tag(b, 250, 207, '"WE ARE GOING"', VIOLET)
    # the minimised window in the corner: her own file, left open
    win = plate_window()
    b.sprites.append((win, 14, H - 12 - win.height))
    b.label(W - 10, H - 16, "DR. VARGA", CYAN_DIM, right=True)
    return b


# --------------------------------------------------------------------------- p4: refit at dusk

def foreshortened(img, k, squash=0.58, colours=40):
    """A top-down sprite seen from the tower: scaled by ``k``, its depth squashed, alpha stepped, palettised."""
    w, h = max(1, round(img.width * k)), max(1, round(img.height * k * squash))
    a = np.array(img.resize((w, h), Image.BOX)).astype(np.float64)
    a[..., 3] = np.where(a[..., 3] >= 128, 255, 0)
    return palettised(Image.fromarray(a.astype(np.uint8), "RGBA"), colours)


def crew(b, x, foot, tall, kneel=False):
    """A ground crew figure in its work light."""
    dark = (16, 16, 24, 255)
    hi = (230, 160, 70, 220)
    d = b.draw
    if kneel:
        tall *= 0.65
    w = max(2.0, tall * 0.32)
    d.rectangle([x - w / 2, foot - tall * 0.75, x + w / 2, foot], fill=dark)
    d.ellipse([x - 1.6, foot - tall - 1, x + 1.6, foot - tall * 0.72], fill=dark)
    d.line([(x + w / 2, foot - tall * 0.7), (x + w / 2, foot - 1)], fill=hi, width=1)


def aegis():
    """p4: Aegis Wing refitting on Ares Landing's spaceport apron at dusk, Jupiter a bright point in the sky."""
    b = o14.OutroBoard()
    hz = 96
    t = np.clip(b.yy / hz, 0, 1)
    b.base = raster.ramp([(0.0, (10, 12, 34)), (0.55, (52, 40, 62)), (0.85, (150, 100, 80)), (1.0, bi.BUTTERSCOTCH)], t)
    sx, sy = 88.0, hz - 2
    b.glow(sx, sy, 60, (40, 70, 120), 0.9)                                   # the Martian dusk's blue sun halo
    b.glow(sx, sy, 10, (90, 130, 170), 0.9)
    rng = np.random.default_rng(7401)
    for _ in range(40):
        x, y = rng.uniform(0, W), rng.uniform(0, hz * 0.45)
        b.base[int(y), int(x)] += rng.uniform(30, 80)
    jx, jy = 470, 30                                                         # Jupiter, a bright point
    b.glow(jx, jy, 5, (120, 110, 90), 1.0)
    b.glow(jx, jy, 1.4, (200, 190, 160), 1.0)
    # the horizon: the mesas and the domes' lit silhouettes
    mesa = hz - 2 - 3 * np.sin(b.xx / 29.0) - 2 * np.sin(b.xx / 7.0) - np.where((b.xx > 540) & (b.xx < 620), 7, 0)
    b.base = np.where((b.yy > mesa)[..., None], np.array([40, 26, 24.0]), b.base)
    for cx, R in ((210, 24), (262, 15), (330, 34), (392, 12)):
        m = (np.hypot((b.xx - cx) / R, (b.yy - hz) / (R * 0.8)) < 1) & (b.yy < hz)
        lit = (np.sin(b.xx * 1.3) > 0.2) & (np.sin(b.yy * 1.7 + b.xx * 0.3) > -0.4)
        b.base = np.where(m[..., None], np.where(lit[..., None], np.array([210, 160, 90.0]), np.array([60, 44, 36.0])),
                          b.base)
        b.glow(cx, hz - R * 0.4, R * 1.6, (60, 40, 20), 0.5)
    # the apron in perspective: concrete, its joints converging, the pads' circles
    ground = b.yy >= hz
    dz = np.maximum(b.yy - hz, 1e-3)
    gz = 900.0 / dz
    gxw = (b.xx - W / 2) * gz / 40.0
    joints = (np.abs(((gxw + 1000) % 30) - 15) > 14.2) | (np.abs((gz % 24) - 12) > 11.0 + np.clip(gz / 400, 0, 0.7))
    apron = np.array([64, 54, 56.0]) * (0.7 + 0.3 * np.clip(dz / 140, 0, 1))[..., None]
    apron = np.where(joints[..., None], apron * 0.72, apron)
    b.base = np.where(ground[..., None], apron, b.base)
    b.base += (np.exp(-(dz / 10) ** 2) * ground)[..., None] * np.array([60, 36, 20.0])  # the dusk on the far apron
    # the wing on its pads, near and far, the crews and their work lights
    pads = [("AEGIS 3", "ship_2", 140, 126, 0.75), ("AEGIS 4", "ship_2", 470, 126, 0.75),
            ("ROOK", "rook", 110, 186, 1.15), ("LANCER", "ship_2", 304, 190, 1.2), ("AEGIS TWO", "ship_2", 500, 188, 1.15)]
    for i, (name, kind, x, y, k) in enumerate(pads):
        b.ring(x, y + 4, 40 * k, 14 * k, colour=AMBER, alpha=110)
        for lx, ly in ((x - 26 * k, y + 12 * k), (x + 30 * k, y - 4 * k)):
            b.glow(lx, ly, 18 * k, (140, 96, 40), 0.65)
        img = bi.rook_craft() if kind == "rook" else bi.asset("ship_2")
        spr = foreshortened(img, k)
        bi.shadow(b, spr, x, y, 4, 3, 0.55)
        b.sprite(spr, x, y)
        crew(b, x - 30 * k, y + 14 * k, 14 * k)
        crew(b, x + 26 * k, y + 4 * k, 13 * k, kneel=i % 2 == 0)
        b.glow(x + 22 * k, y + 2 * k, 2.5, (255, 220, 140), 1.0)             # a welder's spark at a mount
        tag_y = y + 20 * k
        bi.tag(b, x - 20, tag_y, name, CYAN if name == "LANCER" else AMBER if name == "ROOK" else GREY)
    # a crane over Lancer's mount, a weapon pod on its hook
    b.line([(256, 200), (256, 136), (322, 136)], GREY, 2, 230)
    b.line([(314, 136), (314, 162)], GREY, 1, 230)
    b.draw.rectangle([310, 162, 318, 168], fill=(110, 116, 128, 255))
    bi.tag(b, jx + 8, jy - 4, "JUPITER", WHITE)
    b.line([(jx + 3, jy), (jx + 6, jy)], WHITE, 1, 200)
    b.title("ARES LANDING SPACEPORT - DUSK")
    b.label(W - 10, 9, "WE FOLLOW THEM", AMBER, right=True)
    px = 574
    b.label(px, 112, "REFIT", WHITE)
    b.label(px, 122, "AEGIS WING", CYAN)
    b.label(px, 132, "LEAVES MARS", CYAN)
    b.big(px, 146, "4 DAYS", AMBER)
    b.label(px, 172, "NEXT: EUROPA", RED)
    b.label(W - 10, H - 16, "AEGIS ACTUAL OUT", CYAN_DIM, right=True)
    return b


# --------------------------------------------------------------------------- out

IMAGES = {"hellas": hellas, "domes": domes, "europa": europa, "aegis": aegis}


def render(name):
    return IMAGES[name]().image()


def review(images):
    cols = 2
    rows = -(-len(images) // cols)
    sheet = raster.sheet(16 + cols * (W + 16), 56 + rows * (H + 30),
                         f"ACT 3 OUTRO IMAGES (FINAL {ROUND.upper()}): ONE PER PAGE",
                         f"PRODUCTION ART, M7 PART H - {ROUND.upper()}")
    raster.draw_text(sheet, 16, 38, f"{len(images)} IMAGES {W}X{H} AT 1X, AS THE BRIEFING SCREEN DRAWS THEM ABOVE THE TEXT",
                     raster.LABEL)
    for i, (name, img) in enumerate(images.items()):
        img = img.convert("RGBA")
        x, y = 16 + (i % cols) * (W + 16), 54 + (i // cols) * (H + 30)
        sheet.alpha_composite(img, (x, y))
        raster.draw_text(sheet, x, y + H + 6, f"{PREFIX}{name} ({artkit.colour_count([img])} COLOURS)".upper(),
                         raster.LABEL_DIM)
    CONCEPT.mkdir(parents=True, exist_ok=True)
    path = CONCEPT / f"act3-outro-final-{ROUND}-a.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")


def main(args):
    flags = {a for a in args if a.startswith("--")}
    picked = [a.removeprefix(PREFIX) for a in args if not a.startswith("--")] or list(IMAGES)
    wrong = [n for n in picked if n not in IMAGES] + sorted(flags - {"--assets", "--review"})
    if wrong:
        sys.exit(f"unknown: {' '.join(wrong)} (names: {' '.join(IMAGES)}; flags: --assets --review)")
    images = {}
    if "--review" in flags:                                   # the sheet from the files in assets/
        for name in IMAGES:
            path = OUT / f"{PREFIX}{name}.png"
            if path.exists():
                images[name] = Image.open(path).convert("RGB")
    for name in IMAGES:
        if name not in images or ("--assets" in flags and name in picked):
            images[name] = render(name)
        if "--assets" in flags and name in picked:
            OUT.mkdir(parents=True, exist_ok=True)
            artkit.save_png(images[name], OUT / f"{PREFIX}{name}.png", SOURCE)
            print(f"wrote {(OUT / f'{PREFIX}{name}.png').relative_to(ROOT)}")
    review(images)


if __name__ == "__main__":
    main(sys.argv[1:])
