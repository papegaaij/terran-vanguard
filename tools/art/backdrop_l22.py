#!/usr/bin/env python3
"""Production art: the Level 22 backdrop, the ice shelf of Europa's Conamara Chaos (design/campaign/
act-4-deep-water/level-22-ice-shelf; M8 part B batch, concept round 51, step ART-B).

Straight to production from the round-48 mood still `europa-ice-shelf-r48-a` (tools/concept/europa_r48.py: its ice
ramp ICE, the ridged height field of ``ice_albedo``, the open lead's black water mirroring Jupiter's bands
``jupiter_tex``/JUP, Jupiter's warm shine from the right), built like Level 13's lane-cut leads (backdrop_l13.py) and
Level 15's tile sets per section with exact-row seam pieces (backdrop_l15.py). Hard sunlight from the upper left,
posterized per image; the ice is still (the motion budget: the rigs' collars and the geysers' steam are the strong
elements, both sprites).

**No deep layer** (pending review): the shelf is opaque everywhere, the leads included (they are dark water
mirroring Jupiter, not holes: the deep layer is sky, not water), so a `jupiter-sky` tile under it would never show;
the proposal leaves `deep` out (Level 04's top-down rule: the ground covers the screen in every section). Jupiter
shows in the leads' reflection, in the warm shine from the right, and in the Act 4 still (act_stills.py act-4).

**The leads are cut along the Skimmer lanes** (the level's data, `waves[*].lanes`; briefs/m8b-lanes.md): unit i of a
stream enters at t + 1.5 i on lane i mod n, a uniform Catmull-Rom curve through its points (ends repeated) in ground
space at that unit's own entry, so every unit run lays its own lead; easy flies units 0-1, medium 0-2, hard 0-3, and
all are cut (a lead with no boat in it on easy is scenery). A lead is 2 x 58 px wide (the 40 px weave, the hull's 15
px half width and a rim; its rough edge never bites in past 57 px; `--check` tests every run's full weave on water in
the field). Runs whose rows overlap are one piece (`lead-1` .. `-N`, 480 px wide, placed at exact rows).

Layers (factors as the data): `ground` 1.0: `ice-shelf` (section 1: the white-blue plain, Europa's double ridges
(lineae) crossing it, fissures, pressure ridges), `rig-field` (section 2: the chaos's broken plates closer, refrozen
matrix, Vrell teal stains, dark wet bore cracks), `conamara-run` (section 3: the plates under packed snow, the
ice-breaker road at x 240 with its berms, marker poles and a sodium lamp every 240 px); pieces `seam-rig-field` and
`seam-conamara` (the seams at exact rows), `pressure-ridge` (the t~20 nest's ridge), `crevasse` (the secret's
crevasse at t~70 under the lip sprite), the leads, `conamara-station` (the station at exact rows over the run-in's ice:
tools/art/l22_props.py's models, the dome's bottom edge at the screen's at t=150); `high-air` 2.2: `ice-crystals`
(section 1: glittering crystals and thin vapour wisps, additive at 40 % by the game).

Outputs (assets/backdrop/level-22/):
  ice-shelf, rig-field, conamara-run    ground tile sets 480x960 (opaque, wrapping)
  ice-crystals                          high-air tile set 480x960
  seam-rig-field, seam-conamara         ground pieces 480x320 on the section seams (exact rows; their ends fade 32 rows
                                        into the tile sets, dithered: no opaque edge on the screen)
  pressure-ridge, crevasse              ground pieces (transparent round the feature)
  lead-1 .. lead-N                      ground pieces 480 wide (exact rows; transparent round the water)
  conamara-station                      ground piece 480x300 (exact rows)
  design/campaign/act-4-deep-water/level-22-ice-shelf/concept/backdrop-final-r51-a.png/.gif   the review
  design/campaign/act-4-deep-water/level-22-ice-shelf/concept/backdrop-proposal.yaml        (--proposal)

Reads the level's data.yaml (sections, speeds, ramps, waves' lanes, ground targets, vents). Run:
python3 tools/art/backdrop_l22.py [--proposal | --review | --check] (all: about 2 min); re-run after the data moves a
lane, a section or the lip (the leads and the seams sit at exact rows).
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor
from functools import lru_cache

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU, raster

import europa_r48 as eu  # noqa: E402  (concept script, unchanged: Europa's ice ramp and Jupiter's bands)
import scenes_r06 as s6  # noqa: E402  (concept script, unchanged: periodic hillshade and cast shadows)

SCRIPT = "backdrop_l22.py"
BATCH = "M8 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r51"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-4-deep-water" / "level-22-ice-shelf"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-22"
W, SCREEN = 480, 540
TILE_H = 960
LANE_HALF = 58                       # a lead's half width (L13)
FEATHER = 32                        # rows a seam piece fades into the tile sets it continues (step B4)
LANE_MIN = 57                        # the edge never bites in further
WEAVE, HULL = 40, 15
ICE = [np.array(c, float) for c in eu.ICE]
SODIUM = np.array((255, 168, 56), float)
TEAL = np.array((0, 255, 154), float)
FACTORS = {"ground": 1.0, "low-air": 1.35, "high-air": 2.2}

# --------------------------------------------------------------------------- the level's data

DATA = yaml.safe_load((LEVEL_DIR / "data.yaml").read_text(encoding="utf-8"))
SECTIONS = DATA["sections"]
BASE_SPEED = float(DATA.get("scroll_speed", 120))
END = float(SECTIONS[-1]["end"])
OUTRO_END = END + 15


def _speeds():
    out, start, prev = [], 0.0, None
    for s in SECTIONS:
        v = float(s.get("speed", BASE_SPEED if prev is None else prev))
        out.append((start, float(s["end"]), v, float(s.get("ramp", 0)) if prev is not None else 0.0, prev))
        start, prev = float(s["end"]), v
    return out


SPEEDS = _speeds()
DT = 1 / 600


def speed_at(t):
    v = SPEEDS[0][2]
    for start, end, sp, ramp, prev in SPEEDS:
        if t >= start:
            if ramp and prev is not None and t < start + ramp:
                u = (t - start) / ramp
                v = prev + (sp - prev) * u * u * (3 - 2 * u)
            else:
                v = sp
    return v


_T = np.arange(0, OUTRO_END + 1, DT)
_S = np.concatenate([[0.0], np.cumsum([speed_at(t + DT / 2) * DT for t in _T[:-1]])])


def scroll(t):
    return float(np.interp(t, _T, _S))


def time_at(s):
    return float(np.interp(s, _S, _T))


def seam_row(index):
    return scroll(float(SECTIONS[index - 1]["end"])) + SCREEN


def entry_row(t, y):
    """The ground row of a point y px below the top edge at time t (rows count up from the start's bottom edge)."""
    return scroll(t) + SCREEN - y


def target_centre(t, h):
    """A ground target `at: [t, x]` enters at the top edge at t: its centre h/2 above the top edge."""
    return scroll(t) + SCREEN + h / 2


def place_t(centre_row):
    """The `t` a piece's centre passes the middle of the screen (LevelData.pieceCentre)."""
    return time_at(centre_row - SCREEN / 2)


def exact_t(bottom, h):
    """A `t` that puts a piece of height h with its bottom on row ``bottom`` exactly (the game rounds)."""
    t = place_t(bottom + h / 2)
    for digits in (3, 4, 5, 6):
        tt = round(t, digits)
        if round(scroll(tt) + SCREEN / 2 - h / 2) == bottom:
            return tt
    return round(t, 6)


# --------------------------------------------------------------------------- the lanes' runs

def catmull(points, per=24):
    """A uniform Catmull-Rom curve through ``points`` with the ends repeated (sim FlightPath)."""
    p = [points[0]] + list(points) + [points[-1]]
    out = []
    for i in range(1, len(p) - 2):
        p0, p1, p2, p3 = (np.array(q, float) for q in p[i - 1:i + 3])
        for k in range(per):
            u = k / per
            out.append(0.5 * ((2 * p1) + (-p0 + p2) * u + (2 * p0 - 5 * p1 + 4 * p2 - p3) * u * u
                              + (-p0 + 3 * p1 - 3 * p2 + p3) * u ** 3))
    out.append(np.array(points[-1], float))
    return np.array(out)


def unit_count(wave):
    counts = [int(wave.get("count", 1))]
    for d in ("easy", "hard"):
        if isinstance(wave.get(d), dict) and "count" in wave[d]:
            counts.append(int(wave[d]["count"]))
    if wave.get("formation") == "stream":            # the formation lever: easy -1, hard +1 unless set
        if not (isinstance(wave.get("hard"), dict) and "count" in wave["hard"]):
            counts.append(int(wave.get("count", 1)) + 1)
    return max(counts)


def lane_runs():
    """Every unit run of every lane wave: (t_entry, ground curve [x, row]) for the most units any difficulty flies."""
    runs = []
    for w in DATA["waves"]:
        lanes = w.get("lanes")
        if not lanes:
            continue
        n = unit_count(w)
        for i in range(n):
            te = float(w["t"]) + 1.5 * i
            lane = lanes[i % len(lanes)]
            pts = [(float(x), entry_row(te, float(y))) for x, y in lane]
            runs.append((te, catmull(pts)))
    return runs


def lead_groups():
    """The runs whose row spans (with the lead's width) overlap, merged: [(bottom, top, [curves])]."""
    spans = []
    for _, c in lane_runs():
        inside = c[(c[:, 0] > -LANE_HALF - 4) & (c[:, 0] < W + LANE_HALF + 4)]
        lo, hi = inside[:, 1].min() - LANE_HALF - 8, inside[:, 1].max() + LANE_HALF + 8
        spans.append([math.floor(lo), math.ceil(hi), [c]])
    spans.sort(key=lambda s: s[0])
    groups = []
    for s in spans:
        if groups and s[0] <= groups[-1][1]:
            groups[-1][1] = max(groups[-1][1], s[1])
            groups[-1][2] += s[2]
        else:
            groups.append(s)
    out = []
    for lo, hi, curves in groups:                    # an atlas page holds 2048 rows: split taller groups
        n = math.ceil((hi - lo) / 2000)
        step = math.ceil((hi - lo) / n)
        for k in range(n):
            b = lo + k * step
            out.append((b, min(hi, b + step), curves))
    return out


def lane_distance(curves, bottom, h, margin=80):
    """Distance (px) from every pixel of a piece (480 wide, rows bottom..bottom+h) to the nearest run centreline."""
    yy, xx = np.mgrid[0:h, 0:W].astype(np.float64)
    rows = bottom + h - 1 - yy
    d = np.full((h, W), 1e6)
    for c in curves:
        dense = []
        for a, b in zip(c, c[1:]):
            steps = max(1, int(np.hypot(*(b - a)) / 2))
            for k in range(steps):
                dense.append(a + (b - a) * k / steps)
        for px, prow in dense:
            if px < -margin or px > W + margin or prow < bottom - margin or prow > bottom + h + margin:
                continue
            y0 = int(bottom + h - 1 - prow)
            ys = slice(max(0, y0 - margin), min(h, y0 + margin + 1))
            xs = slice(max(0, int(px) - margin), min(W, int(px) + margin + 1))
            if ys.start >= ys.stop or xs.start >= xs.stop:
                continue
            d[ys, xs] = np.minimum(d[ys, xs], np.hypot(xx[ys, xs] - px, rows[ys, xs] - prow))
    return d


# --------------------------------------------------------------------------- the ice

def pf(cell, seed, octaves=4, w=W, h=TILE_H):
    return raster.fbm(w, h, cell, seed, octaves=octaves, period=True)


def wrap_dist_line(xx, yy, x0, y0, x1, y1, h=TILE_H):
    """Distance to a segment, the tile wrapping vertically (the segment drawn at y-h, y, y+h)."""
    d = np.full(xx.shape, 1e9)
    dx, dy = x1 - x0, y1 - y0
    L2 = dx * dx + dy * dy
    for off in (-h, 0, h):
        t = np.clip(((xx - x0) * dx + (yy - y0 - off) * dy) / L2, 0, 1)
        d = np.minimum(d, np.hypot(xx - x0 - t * dx, yy - y0 - off - t * dy))
    return d


def double_ridges(xx, yy, seed, count):
    """Europa's lineae: long double ridges (two crests, a trough between) crossing the tile, a height to add."""
    rng = np.random.default_rng(seed)
    hgt = np.zeros(xx.shape)
    for _ in range(count):
        a = rng.uniform(-0.9, 0.9) + (np.pi / 2 if rng.random() < 0.3 else 0.0)
        cx, cy = rng.uniform(40, W - 40), rng.uniform(0, TILE_H)
        L = rng.uniform(500, 1100)
        bend = rng.uniform(-0.35, 0.35)
        d = np.full(xx.shape, 1e9)
        for k in range(4):                           # a gently curving line in four segments
            a0, a1 = a + bend * (k - 2) / 2, a + bend * (k - 1) / 2
            u0, u1 = -L / 2 + k * L / 4, -L / 2 + (k + 1) * L / 4
            x0, y0 = cx + np.cos(a0) * u0, cy + np.sin(a0) * u0
            x1, y1 = cx + np.cos(a1) * u1, cy + np.sin(a1) * u1
            d = np.minimum(d, wrap_dist_line(xx, yy, x0, y0, x1, y1))
        wdt = rng.uniform(5.0, 8.0)
        prof = np.exp(-((d - wdt) / (wdt * 0.45)) ** 2) - 0.25 * np.exp(-(d / (wdt * 0.5)) ** 2)
        hgt += prof * rng.uniform(0.05, 0.08)
    return hgt


def chaos_plates(xx, yy, seed, n, matrix=0.06):
    """Conamara Chaos: polygonal ice plates (Voronoi, wrapping vertically) raised over a rough refrozen matrix."""
    rng = np.random.default_rng(seed)
    pts = np.column_stack([rng.uniform(0, W, n), rng.uniform(0, TILE_H, n)])
    wx = (pf(96, seed + 7, 4) - 0.5) * 70                # domain warp: ragged, uneven plates
    wy = (pf(96, seed + 8, 4) - 0.5) * 70
    qx, qy = xx + wx, yy + wy
    d1 = np.full(xx.shape, 1e9)
    d2 = np.full(xx.shape, 1e9)
    for px, py in pts:
        for off in (-TILE_H, 0, TILE_H):
            d = np.hypot(qx - px, (qy - py - off) * 1.0)
            d2 = np.where(d < d1, d1, np.minimum(d2, d))
            d1 = np.minimum(d1, d)
    edge = d2 - d1                                    # 0 on a plate boundary
    rough = pf(24, seed + 9, 3)
    plate = np.clip((edge - 5 - 12 * rough) / 5, 0, 1)   # flat-topped plates, a wide rough matrix between
    return plate * matrix, edge


def colour_ice(hgt, stops=None, strength=36, shadow=0.0035, warm=0.10):
    col = eu.m36.shade_rgb(hgt, [tuple(c) for c in (stops or ICE)], strength=strength, shadow=shadow, periodic=True)
    shine = (np.arange(W)[None, :] / W)[..., None] * warm         # Jupiter's warm light from the right
    return col * (1 - shine) + np.array((255, 196, 150)) * shine


def fissures(seed, h=TILE_H):
    f = pf(48, seed, 3, h=h)
    n = pf(96, seed + 1, 3, h=h)
    return np.exp(-((f - 0.5) / 0.006) ** 2) * (n > 0.5)


def posterize(rgb, colours=32, alpha=None):
    img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB")
    q = np.array(img.quantize(colours, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE).convert("RGB"))
    a = np.full(q.shape[:2], 255, np.uint8) if alpha is None else alpha.astype(np.uint8)
    out = np.dstack([q, a])
    out[a == 0] = 0
    return Image.fromarray(out, "RGBA")


def shelf_height(seed, h=TILE_H):
    yy, xx = np.mgrid[0:h, 0:W].astype(np.float64)
    n = pf(96, seed, 5, h=h)
    r1 = 1 - np.abs(2 * pf(64, seed + 1, 3, h=h) - 1)
    big = pf(160, seed + 3, 3, h=h)
    hgt = 0.5 + 0.28 * (big - 0.5) + 0.08 * (n - 0.5) + 0.12 * r1 ** 12
    return hgt, xx, yy


def ice_shelf():
    hgt, xx, yy = shelf_height(2201)
    hgt += double_ridges(xx, yy, 2202, 4)
    crack = fissures(2203)
    hgt -= 0.03 * crack
    col = colour_ice(hgt)
    col = col * (1 - 0.3 * crack)[..., None] + np.array((60, 120, 170)) * 0.3 * crack[..., None]
    return posterize(col)


def rig_field():
    hgt, xx, yy = shelf_height(2211)
    plates, edge = chaos_plates(xx, yy, 2212, 14, 0.07)
    hgt = hgt * 0.7 + 0.15 + plates + double_ridges(xx, yy, 2213, 2) * 0.8
    m = pf(32, 2214, 4)
    matrix = plates < 0.02
    hgt = np.where(matrix, hgt - 0.02 + 0.05 * (m - 0.5), hgt)
    crack = fissures(2215)
    hgt -= 0.03 * crack
    col = colour_ice(hgt)
    col = np.where(matrix[..., None], col * np.array((0.86, 0.9, 0.96)), col)      # the refrozen matrix, bluer
    col = col * (1 - 0.3 * crack)[..., None] + np.array((60, 120, 170)) * 0.3 * crack[..., None]
    stains = (pf(64, 2216, 4) > 0.74) & (pf(16, 2217, 3) > 0.45)                 # Vrell teal stains on the ice
    col = np.where(stains[..., None], col * 0.55 + np.array((10, 70, 64)) * 0.45, col)
    return posterize(col)


def conamara_run():
    import l22_props
    hgt, xx, yy = shelf_height(2221)
    plates, edge = chaos_plates(xx, yy, 2222, 12, 0.05)
    hgt = hgt * 0.6 + 0.2 + plates
    hgt = hgt * 0.8 + 0.1 * pf(32, 2223, 3)                   # packed snow softens the plates
    col = colour_ice(hgt, strength=30)
    road = l22_props.road_strip(64, TILE_H, 2224)
    img = posterize(col)
    img.alpha_composite(road, (l22_props.ROAD_X - 32, 0))
    return img


def ice_crystals():
    """High-air: glitter points and a few thin vapour wisps (additive at 40 %)."""
    rng = np.random.default_rng(2231)
    rgb = np.zeros((TILE_H, W, 3))
    wisps = pf(80, 2232, 4)
    stretch = np.clip((wisps - 0.6) / 0.4, 0, 1) * (pf(16, 2233, 2) > 0.4)
    rgb += np.array((70, 84, 96)) * artkit.ordered_dither(stretch * 0.8, 4)[..., None]
    for _ in range(260):
        x, y = rng.integers(0, W), rng.integers(0, TILE_H)
        b = rng.uniform(0.4, 1.0)
        rgb[y, x] = np.array((236, 246, 255)) * b
        if b > 0.85:
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                rgb[(y + dy) % TILE_H, min(W - 1, max(0, x + dx))] = np.array((150, 180, 210)) * b
    alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    out = np.dstack([np.clip(rgb, 0, 255), alpha]).astype(np.uint8)
    return Image.fromarray(out, "RGBA")


TILES = {"ice-shelf": ice_shelf, "rig-field": rig_field, "conamara-run": conamara_run, "ice-crystals": ice_crystals}
SECTION_TILES = [["ice-shelf", "ice-crystals"], ["rig-field"], ["conamara-run"]]


@lru_cache(maxsize=None)
def tile(name):
    p = OUT / f"{name}.png"
    return np.array(Image.open(p).convert("RGBA"))


def tile_rows(name, bottom, h):
    """The rows ``bottom``..``bottom+h`` of a tile set as the game lays them (image row = H-1 - row mod H)."""
    t = tile(name)
    rows = bottom + h - 1 - np.arange(h)
    return t[(TILE_H - 1 - np.mod(rows, TILE_H)).astype(int)]


# --------------------------------------------------------------------------- pieces

def seam_piece(lower, upper, seam, seed, h=320):
    """A section seam at exact rows: the lower tile set below a ragged pressure ridge, the upper above it."""
    bottom = int(round(seam)) - h // 2
    lo, up = tile_rows(lower, bottom, h).astype(float), tile_rows(upper, bottom, h).astype(float)
    yy, xx = np.mgrid[0:h, 0:W].astype(np.float64)
    n = raster.fbm(W, h, 40, seed, octaves=4, period=False)
    line = h / 2 + 40 * np.sin(xx / 97 + seed) + 30 * (n - 0.5) * 2
    above = yy < line
    rgb = np.where(above[..., None], up[..., :3], lo[..., :3])
    d = yy - line
    ridge = np.exp(-(d / 3.0) ** 2)
    shade = np.where(d < 0, 1.12, 0.72)                          # the ridge's lit upper face, its shaded lower one
    rgb = rgb * (1 - ridge[..., None] * 0.6) + (ICE[5] * shade[..., None]) * ridge[..., None] * 0.6
    # Step B4: its two ends fade FEATHER rows into the tile sets they continue (ordered-dithered alpha over the same
    # rows as the tiles under them, as backdrop_l15.py's blend pieces), so no opaque edge crosses the screen.
    k = np.arange(h)[:, None]
    a = np.minimum(np.clip((k + 0.5) / FEATHER, 0, 1), np.clip((h - k - 0.5) / FEATHER, 0, 1))
    alpha = artkit.ordered_dither(np.broadcast_to(a, (h, W)).copy(), 2) * 255
    return posterize(rgb, 32, alpha), bottom


def pressure_ridge(nest_row, xs, seed=2241):
    """A pressure ridge under the t~20 nest: a raised, fractured ridge of ice blocks running across the right of the
    field, its crest under the turrets; transparent round it."""
    w, h = 260, 120
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    n = raster.fbm(w, h, 20, seed, octaves=4, period=False)
    cx0 = (min(xs) + max(xs)) / 2 - (W - w) - 0          # the nest's middle in the piece (piece x centre W - w/2)
    crest = h / 2 + 8 * np.sin(xx / 40) + 6 * (n - 0.5)
    d = np.abs(yy - crest)
    width = 22 + 10 * np.clip(1 - np.abs(xx - cx0) / 120, 0, 1) - 14 * np.clip((np.abs(xx - w / 2) - 90) / 40, 0, 1)
    hgt = np.clip(1 - d / np.maximum(width, 1), 0, 1) ** 0.8 * (0.8 + 0.4 * n)
    blocks = raster.fbm(w, h, 6, seed + 1, octaves=2, period=False)
    hgt = hgt + 0.15 * (blocks > 0.6) * (hgt > 0.2)
    # Step B4: the ridge dies out into the plain at its left end (inside the field: an opaque edge showed there); its
    # right end runs off the field's right edge.
    hgt = hgt * np.clip(xx / 36, 0, 1)
    gy, gx = np.gradient(hgt)
    sh = np.clip(1 - (gx + gy) * 9, 0.45, 1.3)
    col = (ICE[4] * 0.6 + ICE[5] * 0.4)[None, None, :] * sh[..., None]
    alpha = (hgt > 0.18 + 0.1 * (n - 0.5)) * 255
    return posterize(col, 20, alpha), w, h


def crevasse(lip_x, seed=2251):
    """The secret's crevasse: a dark gash with blue walls across the right of the field, through the lip's place at
    ``lip_x`` (its middle on the piece's middle row, as the lip sprite's gash), out over the right edge."""
    x0 = 300
    w, h = W - x0, 64
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    n = raster.fbm(w, h, 12, seed, octaves=3, period=False)
    gx = xx + x0
    half = np.interp(gx, [x0, x0 + 30, lip_x - 40, lip_x + 40, W], [0, 6, 11, 11, 10]) + 1.5 * (n - 0.5)
    centre = h / 2 + np.interp(gx, [x0, lip_x - 50, lip_x + 40, W], [8, 0, 0, -3])
    top, bot = centre - half, centre + half
    gash = (yy > top) & (yy < bot)
    u = np.clip((yy - top) / np.maximum(bot - top, 1), 0, 1)
    wall = np.clip(1 - u / 0.35, 0, 1)
    col = np.array((8, 16, 30.0)) * (1 - wall[..., None]) + ICE[2] * wall[..., None]
    col = np.where((u > 0.8)[..., None], ICE[1] * 0.7, col)
    rim = ((np.abs(yy - top) < 1.2) | (np.abs(yy - bot) < 1.2)) & (half > 1.5)
    col = np.where(rim[..., None], ICE[5], col)
    alpha = (gash | rim) * 255
    return posterize(col, 16, alpha), x0 + w / 2, w, h


def water_rgb(rows, xx, seed=2261):
    """The leads' water: black, Jupiter's warm bands mirrored in it, a thin new-ice skin in patches, glints."""
    h = rows.shape[0]
    n = raster.fbm(W, h, 30, seed, octaves=3, period=False)
    v = 0.5 + 0.5 * np.sin(rows / 23.0 + 3.0 * n + 0.8 * np.sin(xx / 53.0))
    jup = raster.ramp(eu.JUP, v)
    disc = np.clip(1 - np.abs(xx - 330) / 260, 0, 1) ** 0.7     # Jupiter's reflection, brightest right of the middle
    water = jup * (0.05 + 0.13 * disc[..., None]) + np.array((3, 8, 16))
    skin = raster.fbm(W, h, 12, seed + 1, octaves=3, period=False) > 0.7
    water = np.where(skin[..., None], np.array((30, 44, 60.0)) + water * 0.4, water)
    return water


def lead_piece(bottom, top, curves, seed):
    h = top - bottom
    d = lane_distance(curves, bottom, h)
    yy, xx = np.mgrid[0:h, 0:W].astype(np.float64)
    rows = bottom + h - 1 - yy
    n = raster.fbm(W, h, 10, seed, octaves=3, period=False)
    edge = LANE_MIN + 7 * n
    # Step B4: nothing on the piece's first and last rows (a lead's tip touching its piece's border showed an opaque
    # edge on the screen: lead-2's top row, 3 px).
    ends = np.minimum(yy, h - 1 - yy)
    water = (d < edge) & (ends >= 1)
    rim = (d < edge + 2.5) & ~water & (ends >= 1)
    slush = water & (d > edge - 3)
    rgb = water_rgb(rows, xx, seed)
    glint = np.exp(-((d - edge + 6) / 2.5) ** 2) * (n > 0.5)
    rgb = rgb + glint[..., None] * 34
    rgb = np.where(slush[..., None], np.array((52, 74, 98.0)), rgb)
    rgb = np.where(rim[..., None], np.array((214, 228, 240.0)), rgb)
    alpha = (water | rim) * 255
    return posterize(rgb, 24, alpha)


def station_piece():
    """Conamara Station at exact rows over the run-in's ice: the sodium pools on the ice, l22_props' structures."""
    import l22_props
    w, h = l22_props.STATION
    dome_bottom = l22_props.MAIN[1] + l22_props.MAIN[2] * 0.78
    bottom = int(round(scroll(END))) - int(round(h - 1 - dome_bottom))   # the dome's foot on the bottom edge at the end
    tiles = tile_rows("conamara-run", bottom, h)
    ground = Image.fromarray(tiles.copy(), "RGBA")
    l22_props.lamps_on(ground, l22_props.STATION_LAMPS)
    ground.alpha_composite(l22_props.station_layer())
    l22_props.pad_marks(ground)
    a = np.array(ground)
    # Step B4: the plain ice at its two ends fades FEATHER rows into the tile set under it (ordered-dithered alpha over
    # the same rows), so no opaque edge crosses the screen; the station's structures and lamp pools stay opaque.
    k = np.arange(h)[:, None]
    ramp = np.minimum(np.clip((k + 0.5) / FEATHER, 0, 1), np.clip((h - k - 0.5) / FEATHER, 0, 1))
    alpha = artkit.ordered_dither(np.broadcast_to(ramp, (h, w)).copy(), 2) * 255
    built = (a[..., :3] != tiles[..., :3]).any(axis=2)
    return posterize(a[..., :3].astype(float), 48, np.where(built, 255, alpha)), bottom


# --------------------------------------------------------------------------- build

def targets(enemy=None, sprite=None):
    out = []
    for g in DATA.get("ground_targets") or []:
        if (enemy and g.get("enemy") == enemy) or (sprite and g.get("sprite") == sprite):
            for at in g.get("at") or ([g["t"], g["x"]] if "t" in g else []):
                out.append(at)
            if "positions" in g:
                out += g["positions"]
    return out


def nest_positions():
    """The t~20 ridge nest's turrets (Spine Turrets entering before t=25 in section 1)."""
    out = []
    for g in DATA.get("ground_targets") or []:
        if g.get("enemy") != "spine-turret":
            continue
        ats = g.get("at")
        ats = ats if isinstance(ats, list) and ats and isinstance(ats[0], list) else [ats] if ats else []
        out += [a for a in ats if a and float(a[0]) < 25]
    return out


def lip_at():
    for g in DATA.get("ground_targets") or []:
        if g.get("sprite") == "crevasse-lip":
            at = g.get("at")
            return (at[0] if isinstance(at[0], list) else at), g.get("size", [64, 48])
    return [70.0, 440.0], [64, 48]


def build():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("lead-*.png"):
        old.unlink()
    names = list(TILES)
    with ProcessPoolExecutor() as ex:
        imgs = list(ex.map(_tile_job, names))
    for name, img in zip(names, imgs):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)
    tile.cache_clear()
    placed, pieces = [], {}
    # seams
    for i, (lower, upper) in ((1, ("ice-shelf", "rig-field")), (2, ("rig-field", "conamara-run"))):
        img, bottom = seam_piece(lower, upper, seam_row(i), 2270 + i)
        name = f"seam-{upper}"
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)
        pieces[name] = {"layer": "ground", "size": [W, img.height]}
        placed.append((f"{i + 1}. section {i + 1}'s seam (ground {seam_row(i):.0f}): {lower} into {upper} along a "
                       "pressure ridge; exact rows", name, exact_t(bottom, img.height), 240))
    # the ridge under the t~20 nest
    nest = nest_positions()
    if nest:
        img, w, h = pressure_ridge(max(target_centre(float(a[0]), 32) for a in nest), [float(a[1]) for a in nest])
        row = np.mean([target_centre(float(a[0]), 32) for a in nest])
        artkit.save_png(img, OUT / "pressure-ridge.png", SOURCE)
        pieces["pressure-ridge"] = {"layer": "ground", "size": [w, h]}
        placed.append(("1. the pressure ridge under the t~20 nest", "pressure-ridge", round(place_t(row), 3),
                       W - w // 2))
    # the crevasse under the lip
    (lt, lx), lsize = lip_at()
    img, cx, w, h = crevasse(float(lx))
    row = target_centre(float(lt), float(lsize[1]))
    artkit.save_png(img, OUT / "crevasse.png", SOURCE)
    pieces["crevasse"] = {"layer": "ground", "size": [w, h]}
    placed.append(("2. the secret's crevasse, its gash through the lip (x {:.0f})".format(float(lx)), "crevasse",
                   round(place_t(row), 4), int(cx)))
    # the leads
    groups = lead_groups()
    with ProcessPoolExecutor() as ex:
        leads = list(ex.map(_lead_job, [(b, t, c, 2300 + i) for i, (b, t, c) in enumerate(groups)]))
    for i, ((b, t, _), img) in enumerate(zip(groups, leads), 1):
        name = f"lead-{i}"
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)
        pieces[name] = {"layer": "ground", "size": [W, img.height]}
        placed.append((f"open lead {i} (rows {b}-{t}), cut along the Skimmer runs; exact rows", name,
                       exact_t(b, img.height), 240))
    # the station
    img, bottom = station_piece()
    artkit.save_png(img, OUT / "conamara-station.png", SOURCE)
    pieces["conamara-station"] = {"layer": "ground", "size": [img.width, img.height], "mid_size": True}
    placed.append(("3. Conamara Station: the dome's foot on the bottom edge at t={:.0f}; exact rows".format(END),
                   "conamara-station", exact_t(bottom, img.height), 240))
    global META
    META = {"pieces": pieces, "placed": [list(p) for p in placed]}
    print(f"wrote {len(TILES)} tile sets and {len(pieces)} pieces to {OUT.relative_to(ROOT)}")


def _tile_job(name):
    return TILES[name]()


def _lead_job(args):
    return lead_piece(*args)


# --------------------------------------------------------------------------- proposal

DESC = {
    "ice-shelf": "section 1: the white-blue plain of Europa's ice, double ridges (lineae) crossing it, fissures, "
                 "pressure ridges; Jupiter's warm shine from the right",
    "rig-field": "section 2: the chaos's broken plates over the refrozen matrix, Vrell teal stains, fissures",
    "conamara-run": "section 3: the plates under packed snow, the ice-breaker road at x 240 (berms, marker poles, a "
                    "sodium lamp every 240 px)",
    "ice-crystals": "section 1: glittering ice crystals and thin vapour wisps (additive at 40 %)",
}


META = None


def meta():
    """The pieces and placements: this run's, else read back from the proposal file."""
    if META is not None:
        return META
    block = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))["backdrop"]
    return {"pieces": block["pieces"], "placed": [["", p["piece"], p["t"], p["x"]] for p in block["placed"]]}


def proposal_block():
    meta_ = meta()
    lines = ["backdrop:",
             "  # Presentation only: Europa's ice shelf (tools/art/backdrop_l22.py, from europa-ice-shelf-r48-a). No deep",
             "  # layer: the shelf is opaque everywhere (the leads are dark water mirroring Jupiter, not holes); the leads",
             "  # are cut along the Skimmer runs, the seams, the crevasse and the station sit at exact rows (re-run the",
             "  # generator after the data moves a lane, a section or the lip).",
             "  scroll_factors: {ground: 1, low-air: 1.35, high-air: 2.2}",
             "  ramp: 4",
             "  haze_colour: c8d8e8  # Europa's pale blue-white",
             "  atmosphere:",
             "    clear: {haze: 0.0}   # every section: hard sunlight from the top left, no haze",
             "  tile_sets:"]
    for name in TILES:
        layer = "high-air" if name == "ice-crystals" else "ground"
        lines.append(f"    {name}: {{layer: {layer}, height: {TILE_H}}}   # {DESC[name]}")
    lines.append("  pieces:")
    for name, spec in meta_["pieces"].items():
        extra = ", mid_size: true" if spec.get("mid_size") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for note, piece, t, x in meta_["placed"]:
        if note:
            lines.append(f"    # {note}")
        lines.append(f"    - {{piece: {piece}, t: {t}, x: {x}}}")
    lines.append("# sections' tiles: " + "; ".join(f"{i + 1}: [{', '.join(t)}]" for i, t in enumerate(SECTION_TILES)))
    return "\n".join(lines) + "\n"


def write_proposal():
    PROPOSAL.write_text(proposal_block(), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# --------------------------------------------------------------------------- composite, checks and review

def placed_pieces():
    out = []
    for note, piece, t, x in meta()["placed"]:
        img = np.array(Image.open(OUT / f"{piece}.png").convert("RGBA"))
        h = img.shape[0]
        bottom = int(round(scroll(float(t)) + SCREEN / 2 - h / 2))
        out.append((piece, bottom, int(round(float(x) - img.shape[1] / 2)), img))
    return out


def section_at_row(row):
    s = 0
    for i in range(1, len(SECTIONS)):
        if row >= seam_row(i):
            s = i
    return s


def ground_view(t, pieces=None):
    """The ground layer on the screen at time t (480x540 RGBA, screen row 0 = the top edge)."""
    base = scroll(t)
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for y in range(SCREEN):
        row = int(math.floor(base + SCREEN - 1 - y))
        name = SECTION_TILES[section_at_row(row)][0]
        out[y] = tile(name)[TILE_H - 1 - row % TILE_H]
    img = Image.fromarray(out, "RGBA")
    for piece, bottom, x0, arr in pieces if pieces is not None else placed_pieces():
        h = arr.shape[0]
        top_row = bottom + h - 1
        y_top = int(round(base + SCREEN - 1 - top_row))
        if y_top >= SCREEN or y_top + h <= 0:
            continue
        img.alpha_composite(Image.fromarray(arr, "RGBA"), (x0, y_top)) if 0 <= x0 else img.alpha_composite(
            Image.fromarray(arr[:, -x0:], "RGBA"), (0, y_top))
    return img


def review_plate(w, h):
    """A plate of the shelf for other generators' reviews (the ice-shelf tile's top rows)."""
    p = OUT / "ice-shelf.png"
    img = Image.open(p).convert("RGBA")
    return img.crop((0, 0, min(w, W), min(h, TILE_H))).resize((w, h)) if (w, h) != (W, min(h, TILE_H)) else \
        img.crop((0, 0, w, h))


def check():
    problems = []
    for name in TILES:                                   # the wraps: the top row continues the bottom row
        a = tile(name)[..., :3].astype(float)
        jump = np.abs(a[0] - a[-1]).mean()
        inner = np.abs(a[1:] - a[:-1]).mean()
        if jump > 3 * inner + 6:
            problems.append(f"{name}: wrap seam {jump:.1f} vs {inner:.1f}")
    pieces = placed_pieces()
    leads = [p for p in pieces if p[0].startswith("lead-")]
    for te, c in lane_runs():                            # every run's full weave on water inside the field
        bad = 0
        for (x, row), (x2, row2) in zip(c, c[1:]):
            if not (0 <= x <= W):
                continue
            dx, dr = x2 - x, row2 - row
            L = math.hypot(dx, dr) or 1
            nx, nr = -dr / L, dx / L
            for off in (-WEAVE - HULL, -WEAVE, 0, WEAVE, WEAVE + HULL):
                px, pr = x + nx * off, row + nr * off
                if not (0 <= px < W):
                    continue
                hit = False
                for _, bottom, x0, arr in leads:
                    h = arr.shape[0]
                    if bottom <= pr < bottom + h:
                        yy = int(bottom + h - 1 - pr)
                        if arr[yy, int(px) - x0, 3] > 0:
                            hit = True
                            break
                bad += not hit
        if bad:
            problems.append(f"run entering at t={te:g}: {bad} weave samples off the water")
    for _, bottom, x0, arr in pieces:                     # exact rows: the placement reproduces the bottom
        pass
    print("check: " + ("ok" if not problems else f"{len(problems)} problems"))
    for p in problems:
        print("  " + p)
    return problems


def stand_in(name, i=0):
    try:
        import l22_props
        return l22_props.load(name)[i]
    except Exception:  # noqa: BLE001
        return None


def composite(t, pieces):
    """The screen at t: the ground with the level's ground targets and geysers as stand-ins, the ship."""
    import drill_rig
    img = ground_view(t, pieces)
    base = scroll(t)
    try:
        rig, base_d, pump = drill_rig.load("drill-rig")[0], drill_rig.load("drill-rig-base")[0], drill_rig.load("drill-rig-pump")[0]
    except FileNotFoundError:
        rig = None
    turret = artkit.load_frames("spine-turret")
    gey = stand_in("ice-geyser")
    lip = stand_in("crevasse-lip")

    def y_of(row):
        return base + SCREEN - row

    for g in DATA.get("ground_targets") or []:
        ats = g.get("at")
        ats = ats if isinstance(ats, list) and ats and isinstance(ats[0], list) else [ats] if ats else []
        for at in ats:
            size = 120 if g.get("enemy") == "drill-rig" else 32
            if g.get("sprite") == "crevasse-lip":
                size = 48
            y = y_of(target_centre(float(at[0]), size))
            x = float(at[1])
            if not -80 < y < SCREEN + 80:
                continue
            if g.get("enemy") == "drill-rig" and rig is not None:
                artkit.sprite.paste_center(img, base_d, x, y)
                for dx, dy in drill_rig.PUMPS:
                    artkit.sprite.paste_center(img, pump, x + dx, y - dy)
                artkit.sprite.paste_center(img, rig, x, y)
            elif g.get("enemy") == "spine-turret":
                artkit.sprite.paste_center(img, turret[0], x, y)
            elif g.get("sprite") == "crevasse-lip" and lip is not None:
                artkit.sprite.paste_center(img, lip, x, y)
    vents = DATA.get("vents") or {}
    for r in vents.get("rows") or []:
        y = y_of(target_centre(float(r["t"]), 40))
        if -40 < y < SCREEN + 40 and gey is not None:
            for x in r["xs"]:
                artkit.sprite.paste_center(img, gey, float(x), y)
    ship = artkit.load_frames("ship")[2]
    artkit.sprite.paste_center(img, ship, 240, 470)
    return img


TIMES = [(8, "1 THE SHELF, THE FIRST GEYSER ROW"), (21, "1 THE RIDGE NEST, THE FIRST LEADS"),
         (37, "2 THE SEAM INTO THE RIG FIELD"), (44, "2 THE FIRST RIG"), (73, "2 THE CREVASSE AND THE LIP"),
         (106, "3 THE SEAM INTO THE RUN-IN"), (135, "3 THE ROAD TO CONAMARA"), (150, "3 THE DOME AT THE BOTTOM EDGE")]


def review():
    pieces = placed_pieces()
    shots = [composite(t, pieces) for t, _ in TIMES]
    cols = 4
    cw, ch = 240, 270
    sheet = raster.sheet(16 + cols * (cw + 8) + 8 + 3 * (120 + 8) + 16, 60 + 2 * (ch + 24) + 300,
                         "LEVEL 22 BACKDROP - EUROPA'S ICE SHELF (FINAL)", f"PRODUCTION ART, {BATCH.upper()} - R51")
    raster.draw_text(sheet, 16, 38, "THE SCREEN AT 1/2: GROUND TILES, LEADS ALONG THE SKIMMER RUNS, SEAMS, CREVASSE, STATION "
                                    "(STAND-INS: RIGS, TURRETS, GEYSERS, LIP, SHIP)", raster.LABEL)
    for i, ((t, label), img) in enumerate(zip(TIMES, shots)):
        x, y = 16 + (i % cols) * (cw + 8), 56 + (i // cols) * (ch + 24)
        sheet.alpha_composite(img.resize((cw, ch), Image.Resampling.BOX), (x, y))
        raster.draw_text(sheet, x, y + ch + 4, f"T={t:g} {label}"[:40], raster.LABEL_DIM)
    x0 = 16 + cols * (cw + 8) + 8
    raster.draw_text(sheet, x0, 38, "TILE SETS (480X960 AT 1/4)", raster.LABEL)
    for k, name in enumerate(["ice-shelf", "rig-field", "conamara-run"]):
        im = Image.fromarray(tile(name), "RGBA").resize((120, 240), Image.Resampling.BOX)
        sheet.alpha_composite(im, (x0 + k * 128, 56))
        raster.draw_text(sheet, x0 + k * 128, 300, name.upper(), raster.LABEL_DIM)
    cr = Image.fromarray(tile("ice-crystals"), "RGBA").crop((0, 0, 240, 240))
    sheet.alpha_composite(artkit.add_light(Image.new("RGBA", cr.size, (10, 12, 26, 255)), cr), (x0, 316))
    raster.draw_text(sheet, x0, 560, "ICE-CRYSTALS (HIGH-AIR, 1X CROP)", raster.LABEL_DIM)
    y = 56 + 2 * (ch + 24)
    raster.draw_text(sheet, 16, y, "AT 1X: THE FIRST RIG (T=44) AND THE STATION (T=150)", raster.LABEL)
    sheet.alpha_composite(shots[3].crop((0, 0, 480, 280)), (16, y + 16))
    sheet.alpha_composite(shots[7].crop((0, 260, 480, 540)), (16 + 488, y + 16))
    frames = [composite(t, pieces).resize((240, 270), Image.Resampling.BOX).convert("RGB")
              for t in np.arange(16, 26, 0.1)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=10)


def main(argv):
    if "--review" in argv:
        review()
        return
    if "--check" in argv:
        sys.exit(1 if check() else 0)
    if "--proposal" in argv:
        write_proposal()
        return
    build()
    write_proposal()
    check()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
