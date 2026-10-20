#!/usr/bin/env python3
"""Production art: the Level 20 backdrop, Retreat from Hellas (design/campaign/act-3-red-dust/
level-20-retreat-from-hellas; M7 part G, step ART-G1, review in round 43).

Straight to production from the approved Hellas mood still `mars-hellas-r36-a` (tools/concept/mars_r36.py
`hellas()`, imported unchanged for its ochre ramp): the pale ochre basin floor of dunes and salt-cracked flats,
the Vrell staging grounds' growth, the basin wall's terraces (broad steps of layered rock, sand on each step),
the wide upper terrace with its wind-carved pillars, the rim road's switchbacks, the rim's lip with the
militia's guns and the rust plain beyond. Lit from the upper left (backdrop_l15.lit / backdrop_l17.sdf_piece),
receding, posterized per image. Nothing lit is mirrored, so it reads the same in either scroll direction.

**A strip, not a forward tiling** (m7g-gaps.md 1.7): Level 20 flies 2,840 px up the basin floor, turns, and
runs back down past its start to the rim, so the ground is one strip keyed by **strip row**, not by section:

* strip row = `origin` + the ground scroll + y, y the screen row counted up from the bottom edge (so
  `origin` + scroll is the row at the screen's bottom edge; origin 15,360: row 0 is the bottom edge at the
  level's end, the strike's top at ~18,740 the top edge at the turn). Rows grow up the strip, toward the
  basin's floor; the rim is at the bottom.
* `strip.zones`: `{from, to, tiles}` give each strip row `[from, to)` its ground tile set; a tile's pixel row
  is `floorMod(strip row, height)` counted from the image's bottom row (as Backdrop.drawTiles from position 0),
  so the tiling is the same in both directions and a zone boundary sits at the same rows both ways.
* pieces on the ground are placed by `row` (the piece's centre's strip row) and `x`, not by `t`; the seams sit
  centred on their zone boundaries at exact rows. The other layers keep their factor of the ground scroll (the
  low-air dust wraps; it drifts with the travel: GA's).

The ground is opaque everywhere (no `deep` layer, as Levels 17-19: the README's "basin floor to the horizon"
and the lull's "basin's floor far behind" cannot show under a top-down ground; pending the user's review in
round 43). Layers: ground (x1), low-air (x1.35).

Tile sets (480 wide, 960 tall, wrapping): ground `basin-flats` (salt-cracked flats under drifted sand: the
staging grounds and the mortars' flats), `basin-dunes` (the dune sea, crests across the screen so the travel
reads), `terraces` (four sinuous steps of layered rock a tile, sand on the treads, risers lit facing the basin),
`upper-terrace` (wind-fluted bedrock pavement, the lull), `rim-slope` (the basin wall's rocky slope with the rim
road's switchbacks, two legs a tile), `rim-plain` (the rust plain beyond the rim); low-air `basin-haze` (light,
faint ochre dust) and `ridge-dust` (medium: the ridge's wake).

Pieces (ground): seams `seam-dunes`, `seam-flats`, `seam-terraces`, `seam-upper`, `seam-road` (480x320, exact
rows) and `rim-lip` (480x320, the rim's crest: the slope into the plain, exact rows); `dune-ridge` (400x136, the
dune ridge the t~42 turret nest stands on); `vrell-growth-a/-b/-c` (the staging grounds); `hellas-pillar-a/-b/-c`
(the upper terrace's wind-carved pillars, cast shadows to the lower right); `militia-crawler-a/-b` (abandoned
militia crawlers parked along the road's two leg directions) and `militia-crawler-wreck`; `rim-gun-a`/`-b`
(64x64, 4 frames at 6 fps: the militia's twin flak gun on the lip firing up the screen, the muzzle flash on
frame 0 of a and frame 2 of b).

Outputs: assets/backdrop/level-20/*.png; the review sheet and GIF
design/campaign/act-3-red-dust/level-20-retreat-from-hellas/concept/backdrop-final-r43-a.png/.gif;
`--proposal` writes the level's `backdrop:` block to briefs/l20-backdrop.yaml (for the data step to merge; never
into the level's data.yaml); `--check` checks the tile wraps, sizes, edges, zones and seams.

Reads the level's data.yaml when it exists (sections with signed speed and `ease`, ground targets), otherwise
the README's values (2 s eases, the turnabout's 4 s, the rim's 8 s).
Run: python3 tools/art/backdrop_l20.py [--proposal | --review | --check] [id ...] (all: ~2 min)
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image

import artkit
from artkit import DESIGN, ROOT, write_gif

import backdrop_l08 as k8  # noqa: E402  (tools/art: relief lighting, YAML flow, wrap score)
import backdrop_l15 as b15  # noqa: E402  (tools/art: periodic noise, lighting, posterizing, banks)
import backdrop_l17 as b17  # noqa: E402  (tools/art: the Mars plain base, piece finishing, Vrell growth, gun pit)
import mars_r36 as m36  # noqa: E402  (concept script, unchanged: the Mars ramps)
from render import raster  # noqa: E402
from render.sdf import Material, rotate_z, sd_box, sd_capsule, sd_cylinder_z, union  # noqa: E402

SCRIPT = "backdrop_l20.py"
BATCH = "M7 part G"
SOURCE = artkit.source_note(SCRIPT, BATCH)
LEVEL_DIR = DESIGN / "campaign" / "act-3-red-dust" / "level-20-retreat-from-hellas"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = ROOT / "briefs" / "l20-backdrop.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-20"
REVIEW = CONCEPT / "backdrop-final-r43-a.png"
W, SCREEN = 480, 540
MID = SCREEN / 2
TILE_H = 960
FACTORS = {"ground": 1.0, "low-air": 1.35}
HAZE_COLOUR = "d6a670"                  # mars_r36.SKY: the butterscotch sky
RAMP = 3
ATMOSPHERE = {
    "clear": {"haze": 0.04},
    "light": {"banks": "basin-haze", "haze": 0.06},
    "medium": {"banks": "ridge-dust", "haze": 0.09},
}
OCHRE = m36.OCHRE
PLAIN = m36.PLAIN
ROCK = [(70, 44, 30), (104, 68, 44), (138, 94, 62), (170, 122, 84), (196, 150, 108), (222, 184, 140)]
STRATA = [(0.0, (120, 78, 50)), (0.18, (176, 128, 86)), (0.34, (146, 98, 64)), (0.5, (196, 152, 106)),
          (0.66, (128, 84, 54)), (0.82, (184, 138, 94)), (1.0, (150, 102, 66))]
TEAL = np.array(m36.TEAL, float)

pn, aniso, smooth, ramp, pdist = b15.pn, b15.aniso, b15.smooth, b15.ramp, b15.pdist
lit, recede, posterize_rgb, dither_alpha = b15.lit, b15.recede, b15.posterize_rgb, b15.dither_alpha
finish, finish_piece, sdf_piece = b17.finish, b17.finish_piece, b17.sdf_piece
jr = k8.java_round


# =========================================================================== the level's timeline

DEFAULT_SECTIONS = [   # the README's Layout until the level data exists (signed speeds; 2 s eases)
    {"name": "The Strike", "end": 20, "speed": 140, "atmosphere": "light"},
    {"name": "Turnabout", "end": 26, "speed": -120, "ease": 4, "atmosphere": "medium"},
    {"name": "Basin Floor", "end": 70, "speed": -130, "ease": 2, "atmosphere": "light"},
    {"name": "The Terraces", "end": 108, "speed": -120, "ease": 2, "atmosphere": "light"},
    {"name": "The Lull", "end": 124, "speed": -100, "ease": 2, "atmosphere": "clear"},
    {"name": "Rim Road", "end": 160, "speed": -140, "ease": 2, "atmosphere": "light"},
    {"name": "Over the Rim", "end": 172, "speed": -60, "ease": 8, "atmosphere": "clear"},
]
OUTRO = 15.0                             # LevelData.OUTRO_SECONDS: the scroll runs on at the last speed


def _load(path):
    return (yaml.safe_load(path.read_text(encoding="utf-8")) or {}) if path.exists() else {}


DATA = _load(LEVEL_DIR / "data.yaml")
SECTIONS = [dict(s) for s in (DATA.get("sections") or DEFAULT_SECTIONS)]
SPEED = float(DATA.get("scroll_speed", 150))
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]
END = float(SECTIONS[-1]["end"])


def speed(i):
    return float(SECTIONS[i].get("speed", SPEED))


def ease(i):
    if i == 0 or speed(i) == speed(i - 1):
        return 0.0
    return float(SECTIONS[i].get("ease", SECTIONS[i].get("ramp", 0)) or 0)


def _travelled(i, a, b):
    v, s = speed(i), STARTS[i]
    px = (b - a) * v
    e = ease(i)
    if e > 0:
        old = speed(i - 1)

        def lost(t):
            u = min(max((t - s) / e, 0.0), 1.0)
            return e * (u - u ** 3 + u ** 4 / 2)
        px -= (v - old) * (lost(b) - lost(a))
    return px


def scroll_at(t):
    """LevelData.scrollAt with the M7 part B ease, signed (as backdrop_l18.scroll_at)."""
    scroll = 0.0
    for i, s in enumerate(SECTIONS):
        if t < s["end"] or i == len(SECTIONS) - 1:
            return scroll + _travelled(i, STARTS[i], t)
        scroll += _travelled(i, STARTS[i], s["end"])
    raise AssertionError


# =========================================================================== the strip

ORIGIN = 15360                          # the strip row at the screen's bottom edge at t=0


def bottom_row(t):
    """The strip row at the screen's bottom edge at time t (Backdrop: origin + round(scroll))."""
    return ORIGIN + jr(scroll_at(t))


def t_at_bottom(row, lo=24.0, hi=None):
    """The time in the retreat (t >= lo) when ``row`` is at the screen's bottom edge."""
    hi = END + OUTRO if hi is None else hi
    a, b = lo, hi
    for _ in range(80):
        m = (a + b) / 2
        if ORIGIN + scroll_at(m) > row:
            a = m
        else:
            b = m
    return round((a + b) / 2, 2)


# Zone boundaries (strip rows; rows grow up the strip toward the basin floor). Exact: the seams sit on them.
LIP = 400                               # the rim's crest: the slope above, the plain below
ROAD = 6080                             # the upper terrace above, the rim slope and its road below
UPPER = 7640                            # the last terrace riser (row 7660) above, the upper terrace below
TERRACES = 12240                        # the flats above, the first terrace riser (row 12220) below
FLATS = 14400                           # the dunes above, the salt flats (the mortars) below
DUNES = 16200                           # the staging grounds' flats above, the dune sea below
STRIP_LO, STRIP_HI = -4800, 20160       # the strip's ends (beyond what any scroll shows)
ZONES = [(STRIP_LO, LIP, "rim-plain"), (LIP, ROAD, "rim-slope"), (ROAD, UPPER, "upper-terrace"),
         (UPPER, TERRACES, "terraces"), (TERRACES, FLATS, "basin-flats"), (FLATS, DUNES, "basin-dunes"),
         (DUNES, STRIP_HI, "basin-flats")]


def zone_of(row):
    for lo, hi, name in ZONES:
        if lo <= row < hi:
            return name
    return ZONES[0][2] if row < STRIP_LO else ZONES[-1][2]


# =========================================================================== tile sets

def grid():
    return np.mgrid[0:TILE_H, 0:W].astype(np.float64)


def from_bottom(yy):
    """An image row's tile row counted from the bottom (= the strip row mod 960)."""
    return TILE_H - 1 - yy


def voronoi(seed, n, yy, xx, warp=6.0):
    """A periodic Voronoi network: (distance to the nearest edge, the cell's index)."""
    rng = np.random.default_rng(seed)
    px, py = rng.uniform(0, W, n), rng.uniform(0, TILE_H, n)
    wx = xx + warp * (pn(W, TILE_H, 48, seed + 1, 3) - 0.5) * 2
    wy = yy + warp * (pn(W, TILE_H, 48, seed + 2, 3) - 0.5) * 2
    d1 = np.full(xx.shape, np.inf)
    d2 = np.full(xx.shape, np.inf)
    idx = np.zeros(xx.shape, int)
    for k, (x0, y0) in enumerate(zip(px, py)):
        d = np.hypot(pdist(wx, x0, W), pdist(wy, y0, TILE_H))
        closer = d < d1
        d2 = np.where(closer, d1, np.minimum(d2, d))
        idx = np.where(closer, k, idx)
        d1 = np.where(closer, d, d1)
    return (d2 - d1) / 2, idx


def sand_ripples(yy, xx, seed, cycles=300):
    """Wind ripples across the screen (crests along x), periodic both ways."""
    warp = pn(W, TILE_H, 96, seed, 3)
    return np.sin(2 * np.pi * (cycles * yy / TILE_H + 2 * xx / W) + 9 * warp)


def basin_flats():
    """The basin floor's salt flats: pale salt polygons with curled rims and dark cracks, half under drifted
    sand with wind ripples (the staging grounds, the mortars' flats)."""
    yy, xx = grid()
    edge, idx = voronoi(2001, 150, yy, xx)
    tone_cell = np.random.default_rng(2002).uniform(-1, 1, 150)[idx]
    n = pn(W, TILE_H, 48, 2003, 4)
    drift = smooth(0.48, 0.66, aniso(W, TILE_H, 4, 1, 40, 2004, 3) * 0.7 + pn(W, TILE_H, 160, 2005, 3) * 0.5)
    crack = smooth(1.8, 0.6, edge)
    rim = smooth(4.5, 1.8, edge) * (1 - crack)
    rip = sand_ripples(yy, xx, 2006)
    hgt = 0.5 * n + 0.9 * rim - 1.1 * crack
    hgt = hgt * (1 - drift) + drift * (1.4 + 0.9 * n + 0.28 * rip)
    salt = ramp(OCHRE[2:], np.clip(0.62 + 0.12 * tone_cell + 0.2 * (n - 0.5) + 0.12 * rim, 0, 1))
    salt = salt * (1 - 0.45 * crack[..., None]) + np.array(OCHRE[1], float) * 0.45 * crack[..., None]
    sand = ramp(OCHRE[1:5], np.clip(0.56 + 0.3 * (n - 0.5) + 0.08 * rip, 0, 1))
    alb = salt * (1 - drift[..., None]) + sand * drift[..., None]
    return finish(lit(hgt, alb, ambient=0.48, key=0.62), 32)


def basin_dunes():
    """The dune sea: transverse dunes with their crests across the screen (sinuous, whole cycles over the
    width), gentle stoss slopes facing the basin and slip faces toward the rim; ripples on the stoss."""
    yy, xx = grid()
    ty, tx = np.mgrid[0:W, 0:TILE_H].astype(np.float64)       # transposed: the dune phase runs along the tile
    prof, u = b15.dune_field(TILE_H, W, 2011, ty, tx)
    prof, u = prof.T, u.T
    rip = sand_ripples(yy, xx, 2012, cycles=280) * (u < 0.74)
    n = pn(W, TILE_H, 48, 2013, 3)
    hgt = 13 * prof + 0.22 * rip + 1.2 * n
    trough = np.clip(1 - prof / 0.25, 0, 1)
    t = np.clip(0.6 + 0.22 * (n - 0.5) + 0.22 * prof - 0.3 * trough, 0, 1)
    alb = ramp(OCHRE[1:], t)
    return finish(lit(hgt, alb, ambient=0.46, key=0.66), 32)


RISERS = (940, 700, 560, 220)           # the terraces' risers, tile rows from the bottom (strip row mod 960): 940
# and 700 meet the seams (rows 7660, 12220), 560 puts the t~96 step nest on a step's edge, the t~92 cache on 940's
RISE, FACE = 30.0, 26.0                 # a step's height and its face's half width (rows)


def riser_offset(xx, k):
    rng = np.random.default_rng(2020 + k)
    return sum(a * np.sin(2 * np.pi * c * xx / W + rng.uniform(0, 6.3)) for c, a in ((1, 14), (2, 9), (5, 4), (9, 2)))


def terraces():
    """The basin wall's terraces: four steps a tile (uneven treads), each riser a band of layered rock lit as it faces the
    basin (up the strip), its top lip bare rock, boulders fallen at its foot, sand drifted on each tread.
    The height is the staircase less its trend, so the tile wraps (the treads lean back a little)."""
    yy, xx = grid()
    rb = from_bottom(yy)
    n = pn(W, TILE_H, 48, 2021, 4)
    hgt = np.zeros(xx.shape)
    face = np.zeros(xx.shape)
    lip = np.zeros(xx.shape)
    foot = np.zeros(xx.shape)
    strata = np.zeros(xx.shape)
    for k, r in enumerate(RISERS):
        u = ((r + riser_offset(xx, k) + 4 * (n - 0.5) - rb + TILE_H / 2) % TILE_H) - TILE_H / 2   # > 0 below it
        s = smooth(-FACE, FACE, u)
        hgt += RISE * (s - (u + TILE_H / 2) / TILE_H)
        f = np.clip(1 - np.abs(u) / FACE, 0, 1)
        face = np.maximum(face, f)
        strata = np.where(f > 0, (u + FACE) / (2 * FACE) + 0.12 * (pn(W, TILE_H, 24, 2025 + k, 2) - 0.5), strata)
        lip = np.maximum(lip, np.exp(-((u - FACE - 3) / 3.5) ** 2))
        foot = np.maximum(foot, np.exp(-((u + FACE + 8) / 7) ** 2))
    rng = np.random.default_rng(2022)
    boulders = (rng.random(xx.shape) > 0.9) * foot
    boulders = np.maximum(boulders, np.roll(boulders, 1, 1) * 0.8)
    boulders = np.maximum(boulders, np.roll(boulders, 1, 0) * 0.6)
    rip = sand_ripples(yy, xx, 2023, cycles=200)
    bare = smooth(0.45, 0.8, 0.9 * lip + 0.25 * pn(W, TILE_H, 24, 2024, 3)) * (1 - face)
    sandy = (1 - bare) * (1 - face)
    hgt = hgt + 0.5 * n + 2.0 * boulders + sandy * 0.1 * rip + 1.2 * lip + 1.5 * bare * pn(W, TILE_H, 16, 2026, 3)
    rock = raster.ramp(STRATA, np.clip(strata + 0.06 * (n - 0.5), 0, 1))
    rockb = ramp(ROCK[1:], np.clip(0.52 + 0.3 * (n - 0.5), 0, 1))
    sand = ramp(OCHRE[1:5], np.clip(0.66 + 0.2 * (n - 0.5) + 0.05 * rip, 0, 1))
    alb = rockb * bare[..., None] + sand * sandy[..., None]
    alb = np.where((face > 0)[..., None], rock, alb)
    alb = alb * (1 - 0.3 * boulders[..., None]) * (1 - 0.22 * foot[..., None] * (1 - face[..., None]))
    return finish(lit(hgt, alb, ambient=0.45, key=0.7), 36)


def upper_terrace():
    """The lull's wide upper terrace: wind-fluted bedrock pavement (grooves along the wind, across the
    screen), cracks, sand streaked in the grooves' lee."""
    yy, xx = grid()
    n = pn(W, TILE_H, 48, 2031, 4)
    flute = aniso(W, TILE_H, 8, 1, 12, 2032, 3)
    groove = np.sin(2 * np.pi * (60 * yy / TILE_H) + 6 * (flute - 0.5) + 3 * pn(W, TILE_H, 160, 2033, 2))
    edge, cell = voronoi(2034, 70, yy, xx, warp=8)
    crack = smooth(1.8, 0.5, edge)
    tone = np.random.default_rng(2036).uniform(-1, 1, 70)[cell]
    streak = smooth(0.58, 0.74, aniso(W, TILE_H, 6, 1, 20, 2035, 3)) * smooth(0.0, 0.6, -groove)
    hgt = 0.35 * groove + 1.2 * n - 1.4 * crack - 0.3 * streak + 0.5 * tone
    rock = ramp(ROCK[1:], np.clip(0.5 + 0.22 * (n - 0.5) + 0.1 * tone + 0.03 * groove, 0, 1))
    sand = ramp(OCHRE[2:5], np.clip(0.55 + 0.2 * (n - 0.5), 0, 1))
    alb = rock * (1 - streak[..., None]) + sand * streak[..., None]
    alb = alb * (1 - 0.35 * crack[..., None])
    return finish(lit(hgt, alb, ambient=0.45, key=0.7), 32)


# the rim road: two legs a tile, hairpins at the edges (tile rows from the bottom; x in px)
ROAD_LEGS = (((112, 40), (368, 440)), ((368, 520), (112, 920)))
ROAD_TURNS = ((368, 480, "right"), (112, 0, "left"))
ROAD_R, ROAD_HALF = 40.0, 20.0


def _seg_dist(px, py, a, b):
    ax, ay = a
    bx, by = b
    dx, dy = bx - ax, by - ay
    h = np.clip(((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy), 0, 1)
    return np.hypot(px - ax - h * dx, py - ay - h * dy)


def road_distance(xx, rb):
    """The distance to the road's centreline (periodic in the tile)."""
    d = np.full(xx.shape, np.inf)
    for shift in (-TILE_H, 0, TILE_H):
        r = rb + shift
        for a, b in ROAD_LEGS:
            d = np.minimum(d, _seg_dist(xx, r, a, b))
        for cx, cy, side in ROAD_TURNS:
            arc = np.abs(np.hypot(xx - cx, r - cy) - ROAD_R)
            on = (xx >= cx) if side == "right" else (xx <= cx)
            d = np.where(on, np.minimum(d, arc), d)
    return d


def road_x(row):
    """The road's centre x on the legs at a strip row (None in a hairpin), and the leg ('a' rises to the right
    toward the rim, 'b' to the left)."""
    r = row % TILE_H
    for leg, ((x0, r0), (x1, r1)) in zip("ab", ROAD_LEGS):
        if min(r0, r1) <= r <= max(r0, r1):
            return round(x0 + (x1 - x0) * (r - r0) / (r1 - r0), 1), leg
    return None, None


def rim_slope():
    """The basin wall below the rim: a rocky slope furrowed by gullies running down it, strata outcrops across,
    scree and sand pockets; the rim road's switchbacks cut into it (a packed-dust bed with two ruts, a berm of
    cleared rocks on its downhill side, a cut bank on its uphill side)."""
    yy, xx = grid()
    rb = from_bottom(yy)
    n = pn(W, TILE_H, 48, 2041, 4)
    gully = aniso(W, TILE_H, 1, 6, 8, 2042, 3)
    band = np.sin(2 * np.pi * (14 * yy / TILE_H) + 4 * pn(W, TILE_H, 160, 2043, 2))
    edge, cell = voronoi(2046, 120, yy, xx, warp=10)
    block = smooth(0.5, 3.0, edge)
    tone = np.random.default_rng(2047).uniform(-1, 1, 120)[cell]
    rng = np.random.default_rng(2044)
    scree = rng.random(xx.shape) > 0.975
    pocket = smooth(0.6, 0.74, pn(W, TILE_H, 48, 2045, 3))
    hgt = 2.6 * gully + 0.8 * n + 0.3 * band + 1.6 * block + 0.6 * tone + 0.9 * scree
    d = road_distance(xx, rb) + 3.0 * (pn(W, TILE_H, 16, 2048, 3) - 0.5)
    bed = smooth(ROAD_HALF + 1.0, ROAD_HALF - 1.0, d)
    stones = (rng.random(xx.shape) > 0.55) * np.exp(-((d - ROAD_HALF - 3) / 2.5) ** 2) * (1 - bed)
    stones = np.maximum(stones, np.roll(stones, 1, 1) * 0.7)
    rut = sum(np.exp(-((d - o) / 1.8) ** 2) for o in (8.0,)) * bed
    flat = hgt * 0.15 + 1.0
    hgt = hgt * (1 - bed) + bed * (flat + 0.25 * n - 0.2 * rut) + 1.6 * stones
    rock = raster.ramp(STRATA, np.clip(0.5 + 0.12 * band + 0.25 * tone * 0.5 + 0.2 * (n - 0.5), 0, 1)) * 0.92
    rock = rock * (1 - 0.2 * scree[..., None]) * (1 - 0.3 * (1 - block[..., None]))
    sand = ramp(OCHRE[1:5], np.clip(0.55 + 0.25 * (n - 0.5), 0, 1))
    alb = rock * (1 - pocket[..., None]) + sand * pocket[..., None]
    dust = ramp([(150, 118, 84), (186, 152, 112), (212, 182, 140)], np.clip(0.55 + 0.25 * (n - 0.5), 0, 1))
    dust = dust * (1 - 0.2 * np.clip(rut, 0, 1)[..., None])
    alb = alb * (1 - bed[..., None]) + dust * bed[..., None]
    alb = alb * (1 - 0.25 * stones[..., None])
    return finish(lit(hgt, alb, ambient=0.45, key=0.7), 36)


def rim_plain():
    """Beyond the rim: the rust plain (backdrop_l17's Mars plain base), a few rocks."""
    hgt, alb, _, _ = b17.plain_base(2051)
    rocks = (np.random.default_rng(2052).random(hgt.shape) > 0.994) * 2.4
    rocks = np.maximum(rocks, np.roll(rocks, 1, 1) * 0.7)
    return finish(lit(hgt + rocks, alb), 32)


def basin_haze():
    return b15.cloud_bank(W, TILE_H, 2061, 0.72, 0.16, 64, colours=OCHRE[1:5])


def ridge_dust():
    return b15.cloud_bank(W, TILE_H, 2063, 0.6, 0.2, 88, colours=OCHRE[1:5])


# =========================================================================== seams

FEATHER = 32
SEAM_H = 320
SEAMS = {   # name -> (boundary row, the tile set below it, the one above it, seed)
    "seam-road": (ROAD, "rim-slope", "upper-terrace", 2071),
    "seam-upper": (UPPER, "upper-terrace", "terraces", 2073),
    "seam-terraces": (TERRACES, "terraces", "basin-flats", 2075),
    "seam-flats": (FLATS, "basin-flats", "basin-dunes", 2077),
    "seam-dunes": (DUNES, "basin-dunes", "basin-flats", 2079),
}


def tile_strip(name, bottom, h):
    """A tile set's rows for strip rows [bottom, bottom + h) as an image array (top row = highest)."""
    img = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    hgt = img.shape[0]
    pos = bottom + h - 1 - np.arange(h)
    return img[[hgt - 1 - (int(p) % hgt) for p in pos]]


def wobble(seed, amps=(16, 10, 5, 3)):
    xs = np.arange(W)[None, :]
    rng = np.random.default_rng(seed)
    w = sum(a * np.sin(2 * np.pi * c * xs / W + rng.uniform(0, 6.3)) for c, a in zip((1, 2, 5, 11), amps))
    return w + (np.random.default_rng(seed + 1).random((1, W)) - 0.5) * 3


def feathered(out, h):
    k = np.arange(h)[:, None]
    a = np.minimum(np.clip((k + 0.5) / FEATHER, 0, 1), np.clip((h - k - 0.5) / FEATHER, 0, 1))
    alpha = dither_alpha(np.broadcast_to(a, (h, W)), 2)
    out[..., 3] = np.where(alpha > 0, 255, 0)
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def seam_piece(name):
    """backdrop_l19.seam_piece by strip row: the lower zone's tiles below a ragged boundary at the zone's row,
    the upper's above, a darker line on the boundary, the ends faded into the tile sets (1-bit dither)."""
    g, lower, upper, seed = SEAMS[name]
    bottom, h = g - SEAM_H // 2, SEAM_H
    up = tile_strip(upper, bottom, h)
    lo = tile_strip(lower, bottom, h)
    rows = bottom + h - 1 - np.arange(h)[:, None]
    amps = (6, 4, 2, 1) if name in ("seam-terraces", "seam-upper") else (16, 10, 5, 3)
    edge = g + wobble(seed, amps)
    out = np.where((rows >= edge)[..., None], up, lo)
    d = rows - edge
    r = (d >= 0) & (d < 2)
    out[..., :3] = np.where(r[..., None], out[..., :3] * 0.78, out[..., :3])
    return feathered(out, h)


LIP_H = 320


def rim_lip():
    """The rim's crest at row LIP: the slope's rock rising to a lit crest band facing the basin, a dark back
    slope falling to the rust plain; the road's track over it at the middle; the plain's tiles below."""
    bottom, h = LIP - LIP_H // 2, LIP_H
    up = tile_strip("rim-slope", bottom, h)
    lo = tile_strip("rim-plain", bottom, h)
    rows = bottom + h - 1 - np.arange(h)[:, None] + np.zeros((1, W))
    xs = np.arange(W)[None, :] + np.zeros((h, 1))
    crest = LIP + wobble(2081, (10, 6, 3, 2))
    u = rows - crest                                   # > 0 above the crest (toward the basin)
    nn = raster.fbm(W, h, 16, 2082, octaves=4, period=False)
    hgt = 14 * np.exp(-(np.maximum(u, 0) / 30) ** 2) * np.exp(-(np.minimum(u, 0) / 16) ** 2) + 2.2 * nn
    track = np.exp(-((xs - 240 - 0.25 * u) / 15) ** 2) * (np.abs(u) < 70)
    hgt = hgt - 3.0 * track * np.exp(-(u / 30) ** 2)
    band = (np.abs(u) < 70).astype(np.float64)
    rock = raster.ramp(STRATA, np.clip(0.4 + 0.004 * u + 0.25 * (nn - 0.5), 0, 1))
    rock = np.where((track > 0.5)[..., None], np.array((176, 140, 102), float), rock)
    rgb = lit(hgt, rock, wrap=False, ambient=0.45, key=0.72)
    rgb = posterize_rgb(recede(rgb, 0.2), 36)
    rgb = np.array(rgb).astype(np.float64)
    base = np.where((u >= 0)[..., None], up, lo)
    a = smooth(70, 40, np.abs(u))
    a = np.clip(a + 0.0 * band, 0, 1)
    out = base.copy()
    out[..., :3] = base[..., :3] * (1 - a[..., None]) + rgb[..., :3] * a[..., None]
    return feathered(out, h)


# =========================================================================== pieces

def dune_ridge():
    """A sharp transverse dune ridge across the field (the t~42 turret nest stands on its crest): a long stoss
    slope facing the basin, the crest, a short slip face toward the rim; the dune sea's colours."""
    size = (400, 136)
    w, h = size
    f = 2
    yy, xx = b15.piece_canvas(size, f)
    crest = h * 0.58 + 9 * np.sin(2 * np.pi * xx / w * 1.3 + 0.7) + 4 * np.sin(2 * np.pi * xx / w * 3.1)
    u = yy - crest                                       # < 0 above (the stoss), > 0 the slip face
    prof = np.where(u < 0, np.exp(-(u / 36) ** 2), np.exp(-(u / 12) ** 2))
    ends = np.clip(np.minimum(xx, w - xx) / 70, 0, 1) ** 0.8
    n = raster.fbm(w * f, h * f, 24, 2091, octaves=3, period=False)
    hgt = 20 * prof * ends + 1.0 * n + 0.25 * np.sin(yy / 1.6 + 3 * n) * (u < -4)
    cover = smooth(0.12, 0.32, prof * ends + 0.12 * (n - 0.5))
    alb = ramp(OCHRE[1:], np.clip(0.5 + 0.12 * prof + 0.15 * (n - 0.5), 0, 1))
    return finish_piece(hgt, alb, cover, f, colours=28)


def vrell_growth_a():
    return b17.vrell_growth((160, 120), 2101, 0.55)


def vrell_growth_b():
    return b17.vrell_growth((120, 96), 2103, 0.5)


def vrell_growth_c():
    return b17.vrell_growth((200, 140), 2105, 0.6)


def pillar_mats():
    bands = lambda p, n: (0.74 + 0.2 * np.sin(p[:, 2] * 0.55) ** 2 + 0.05 * np.sin(p[:, 0] * 1.7 + p[:, 1] * 1.3)  # noqa: E731
                          + 0.25 * (n[:, 2] > 0.9))
    return [Material((0.66, 0.5, 0.34), metal=0.0, shininess=10, spec=0.1, pattern=bands),
            Material((0.74, 0.58, 0.4), metal=0.0, shininess=12, spec=0.12),
            Material((0.6, 0.46, 0.32), metal=0.0, shininess=8, spec=0.08)]


def pillar_scene(parts, seed):
    """A wind-carved pillar: a tapering spire of layered rock (its strata showing on the sloping sides from
    above), its flat top lighter (x, y in px, z up). ``parts``: (x, y, r_base, r_top, height) per spire."""
    rng = np.random.default_rng(seed)
    wob = rng.uniform(0, 6.3, 3)

    def scene(p):
        a = np.arctan2(p[:, 1], p[:, 0])
        rough = 1 + 0.14 * np.sin(3 * a + wob[0]) + 0.08 * np.sin(5 * a + wob[1]) + 0.06 * np.sin(p[:, 2] * 0.5 + wob[2])
        items = []
        for cx, cy, rb, rt, ht in parts:
            q = p - np.array([cx, cy, 0.0])
            r = np.hypot(q[:, 0], q[:, 1]) / rough
            k = np.clip(q[:, 2] / ht, 0, 1)
            rad = rb + (rt - rb) * k ** 0.8
            d = np.maximum(r - rad, np.abs(q[:, 2] - ht / 2) - ht / 2) * 0.7
            items.append((d, 0))
        return union(*items, k=2.0)
    return scene


def hellas_pillar(parts, size, seed):
    ht = max(p[4] for p in parts)
    return sdf_piece(pillar_scene(parts, seed), pillar_mats(), size, z_top=ht + 10, f=4,
                     shadow=(int(ht * 0.3), int(ht * 0.36)), shadow_alpha=0.5, colours=28)


def pillar_a():
    return hellas_pillar([(-14, 14, 30, 13, 70)], (160, 160), 2111)


def pillar_b():
    return hellas_pillar([(-10, 10, 22, 9, 48)], (112, 112), 2113)


def pillar_c():
    return hellas_pillar([(-24, 16, 26, 11, 62), (6, 4, 20, 9, 50)], (168, 160), 2115)


DRAB = (0.56, 0.53, 0.4)                 # Ares militia drab
DRAB_D = (0.34, 0.33, 0.27)


def crawler_mats(wreck=False):
    k = 0.45 if wreck else 1.0
    soot = (lambda p, n: np.where(np.hypot(p[:, 0] - 3, p[:, 1] + 6) < 9, 0.35, 1.0)) if wreck else None
    return [Material(tuple(c * k for c in DRAB), metal=0.3, shininess=40, spec=0.4, pattern=soot),     # 0 hull
            Material((0.12, 0.12, 0.12), metal=0.1, shininess=10, spec=0.1),                          # 1 tyres
            Material(tuple(c * k for c in DRAB_D), metal=0.4, shininess=50, spec=0.5),                 # 2 turret, gun
            Material((0.05, 0.05, 0.06), metal=0.0, shininess=4, spec=0.05),                          # 3 open hatch
            Material((0.62 * k, 0.5 * k, 0.16 * k), metal=0.2, shininess=30, spec=0.3),               # 4 the amber band
            Material((0.7, 0.58, 0.42), metal=0.0, shininess=10, spec=0.1)]                           # 5 drifted sand


def crawler_scene(heading, wreck=False):
    """An abandoned six-wheeled militia crawler (42 x 22 px): a slab-sided hull with an amber band, the cab
    forward, the roof hatch open, a small turret with a stubby gun; sand drifted against its uphill side."""
    def scene(p):
        q = rotate_z(p, -heading)
        items = [(sd_box(q, (0, 0, 7.0), (10.0, 21.0, 4.5), 1.6), 0),
                 (sd_box(q, (0, 15.0, 11.0), (8.0, 5.0, 1.6), 1.2), 0),
                 (sd_box(q, (0, 4.0, 11.8), (10.2, 1.2, 0.8), 0.3), 4),
                 (sd_box(q, (0, -6.0, 11.6), (3.4, 3.4, 0.6), 0.4), 3)]
        for y in (-14.0, 0.0, 14.0):
            for x in (-10.5, 10.5):
                items.append((sd_cylinder_z(rotate_z(q - np.array([x, y, 4.0]), 0.0), (0, 0, 0), 4.2, 2.2) - 0.4, 1))
        if not wreck:
            items.append((sd_cylinder_z(q, (0, -14.0, 12.5), 4.0, 1.4), 2))
            items.append((sd_capsule(q, (0, -14.0, 13.5), (0, -2.0, 14.5), 1.0), 2))
        else:
            items.append((sd_capsule(q, (0, -14.0, 12.0), (5.0, -8.0, 11.0), 1.0), 2))
        items.append((sd_box(q, (12.5, 0, 1.5), (3.0, 20.0, 2.5), 2.5), 5))
        return union(*items)
    return scene


LEG_ANGLE = math.atan2(368 - 112, 440 - 40)  # a leg's slant from straight up the strip


def militia_crawler_a():
    """Parked along leg a (the road climbing to the right toward the rim: nose down-right on the screen)."""
    return sdf_piece(crawler_scene(math.pi - LEG_ANGLE), crawler_mats(), (64, 64), z_top=30, f=4, shadow=(3, 4),
                     colours=28)


def militia_crawler_b():
    """Parked along leg b (nose down-left on the screen), its turret gone dark."""
    return sdf_piece(crawler_scene(math.pi + LEG_ANGLE), crawler_mats(), (64, 64), z_top=30, f=4, shadow=(3, 4),
                     colours=28)


def militia_crawler_wreck():
    return sdf_piece(crawler_scene(math.pi * 0.62, wreck=True), crawler_mats(wreck=True), (64, 64), z_top=30, f=4,
                     shadow=(3, 4), colours=24)


GUN = 64
GUN_FRAMES, GUN_FPS = 4, 6
MUZZLES = ((11.6, -12.6), (8.4, -12.6))          # the twin barrels' tips from the pit's centre (screen px)


def rim_gun_frames(flash_at):
    """backdrop_l17.gun_pit (the militia's twin flak gun, barrels raised up the screen) with its muzzle flash:
    frame ``flash_at`` the burst, the next one its fading flare and smoke, the rest quiet."""
    pit = b17.gun_pit()                               # 56x56; padded to GUN so its shadow clears the edges
    canvas = Image.new("RGBA", (GUN, GUN), (0, 0, 0, 0))
    canvas.alpha_composite(pit.convert("RGBA"), ((GUN - pit.width) // 2, (GUN - pit.height) // 2))
    base = np.array(canvas).astype(np.float64)
    yy, xx = np.mgrid[0:GUN, 0:GUN].astype(np.float64)
    frames = []
    for k in range(GUN_FRAMES):
        img = base.copy()
        age = (k - flash_at) % GUN_FRAMES
        if age < 2:
            for mx, my in MUZZLES:
                cx, cy = GUN / 2 + mx + 2.5, GUN / 2 + my - 2.5
                d = np.hypot(xx - cx, yy - cy)
                r = 5.5 if age == 0 else 3.5
                core = d < r * 0.45
                halo = (d < r) & ~core
                col = np.array((255, 236, 170), float) if age == 0 else np.array((236, 170, 90), float)
                img[core] = np.concatenate([col, [255]])
                img[halo] = np.concatenate([np.array((255, 168, 60), float) * (1.0 if age == 0 else 0.7), [255]])
        frames.append(Image.fromarray(img.astype(np.uint8), "RGBA"))
    return artkit.quantize_set(frames, 32)


def rim_gun_a():
    return rim_gun_frames(0)


def rim_gun_b():
    return rim_gun_frames(2)


# =========================================================================== registry

GENERATORS = {
    "basin-flats": basin_flats, "basin-dunes": basin_dunes, "terraces": terraces, "upper-terrace": upper_terrace,
    "rim-slope": rim_slope, "rim-plain": rim_plain, "basin-haze": basin_haze, "ridge-dust": ridge_dust,
    "dune-ridge": dune_ridge, "vrell-growth-a": vrell_growth_a, "vrell-growth-b": vrell_growth_b,
    "vrell-growth-c": vrell_growth_c, "hellas-pillar-a": pillar_a, "hellas-pillar-b": pillar_b,
    "hellas-pillar-c": pillar_c, "militia-crawler-a": militia_crawler_a, "militia-crawler-b": militia_crawler_b,
    "militia-crawler-wreck": militia_crawler_wreck, "rim-gun-a": rim_gun_a, "rim-gun-b": rim_gun_b,
}
SECOND = {name: (lambda n=name: seam_piece(n)) for name in SEAMS}
SECOND["rim-lip"] = rim_lip
TILE_SETS = {   # id -> (layer, drift, note)
    "basin-flats": ("ground", None, "the basin floor's salt flats under drifted sand (the staging grounds, the mortars' flats)"),
    "basin-dunes": ("ground", None, "the dune sea, crests across the screen"),
    "terraces": ("ground", None, "the basin wall's terraces: four steps of layered rock a tile, sand on the treads"),
    "upper-terrace": ("ground", None, "the lull's wide upper terrace: wind-fluted bedrock pavement"),
    "rim-slope": ("ground", None, "the rocky slope below the rim with the rim road's switchbacks (two legs a tile)"),
    "rim-plain": ("ground", None, "beyond the rim: the rust plain"),
    "basin-haze": ("low-air", 5, "light: faint ochre dust, at most 25 %"),
    "ridge-dust": ("low-air", 8, "medium (the turnabout): the ridge's wake, at most 35 %"),
}
PIECES = {   # id -> (layer, note, extra)
    "seam-road": ("ground", "the upper terrace onto the rim slope and its road (exact rows)", {}),
    "seam-upper": ("ground", "the last terrace riser onto the upper terrace (exact rows)", {}),
    "seam-terraces": ("ground", "the salt flats onto the first terrace riser (exact rows)", {}),
    "seam-flats": ("ground", "the dune sea onto the salt flats (exact rows)", {}),
    "seam-dunes": ("ground", "the staging grounds' flats onto the dune sea (exact rows)", {}),
    "rim-lip": ("ground", "the rim's crest: the slope's lit face, the back slope onto the plain, the road's track (exact rows)", {}),
    "dune-ridge": ("ground", "a sharp dune ridge across the field: the t~42 turret nest on its crest", {}),
    "vrell-growth-a": ("ground", "Vrell growth on the staging grounds", {}),
    "vrell-growth-b": ("ground", "Vrell growth, smaller", {}),
    "vrell-growth-c": ("ground", "Vrell growth, larger", {}),
    "hellas-pillar-a": ("ground", "a wind-carved pillar on the upper terrace (a tapering spire of layered rock, its shadow)", {}),
    "hellas-pillar-b": ("ground", "a smaller pillar", {}),
    "hellas-pillar-c": ("ground", "a twin pillar", {}),
    "militia-crawler-a": ("ground", "an abandoned militia crawler along road leg a (nose down-right)", {}),
    "militia-crawler-b": ("ground", "an abandoned militia crawler along road leg b (nose down-left)", {}),
    "militia-crawler-wreck": ("ground", "a burnt militia crawler off the road", {}),
    "rim-gun-a": ("ground", "the militia's twin flak gun on the rim's lip, firing up (flash on frame 0)",
                  {"frames": GUN_FRAMES, "fps": GUN_FPS}),
    "rim-gun-b": ("ground", "the same gun, its flash on frame 2", {"frames": GUN_FRAMES, "fps": GUN_FPS}),
}
ANIMATED = {"rim-gun-a", "rim-gun-b"}


def render_job(name):
    fn = GENERATORS.get(name) or SECOND[name]
    return name, fn()


def save(name, img):
    OUT.mkdir(parents=True, exist_ok=True)
    if isinstance(img, list):
        for old in OUT.glob(f"{name}_*.png"):
            old.unlink()
        for i, im in enumerate(img):
            artkit.save_png(im, OUT / f"{name}_{i}.png", SOURCE)
        print(f"  {name}: {len(img)} frames {img[0].size[0]}x{img[0].size[1]}")
    else:
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)
        print(f"  {name}: {img.size[0]}x{img.size[1]}")


def write_all(wanted):
    first = [n for n in GENERATORS if not wanted or n in wanted]
    second = [n for n in SECOND if not wanted or n in wanted or any(w in TILE_SETS for w in wanted)]
    with ProcessPoolExecutor(max_workers=12) as pool:
        for name, img in pool.map(render_job, first):
            save(name, img)
        for name, img in pool.map(render_job, second):
            save(name, img)


# =========================================================================== placements and the block

def image_of(name, frame=0):
    p = OUT / f"{name}.png"
    return p if p.exists() else OUT / f"{name}_{frame}.png"


def size_of(name):
    return list(Image.open(image_of(name)).size)


def targets():
    """{name: [(t, x), ...]} of the data's ground targets, else the README's (x where it gives none: ours)."""
    out = {}
    for g in DATA.get("ground_targets") or []:
        out[g.get("name") or g["target"]] = [tuple(map(float, a)) for a in g["at"]]
    return out or {"dune ridge nest": [(42, 200), (42.4, 240), (42.8, 280)],
                   "salt flat mortars": [(60, 130), (60.4, 350)],
                   "terrace nest": [(85, 120), (85.4, 160), (85.8, 200)],
                   "emergency cache": [(92, 430)],
                   "terrace mortars": [(100, 300), (100.4, 360)],
                   "rim road nest": [(140, None)]}


def target_row(t, size=32):
    """A ground object's centre row: in a reversed section it enters at the bottom edge at t (its top edge
    there, y = -h/2: m7g-gaps.md 1.1), in the strike at the top edge."""
    if scroll_at(t + 0.01) < scroll_at(t):
        return bottom_row(t) - size / 2
    return bottom_row(t) + SCREEN + size / 2


def target_centre(name, size=32):
    spots = targets()[name]
    row = sum(target_row(t, size) for t, _ in spots) / len(spots)
    xs = [x for _, x in spots if x is not None]
    return row, (sum(xs) / len(xs) if xs else None)


def growth_spots():
    """The staging grounds: Vrell growth over the flats the strike flies over (rows 16,300-18,700), clear of
    the screen's middle lane at the strike's start; a few patches reach into the dunes."""
    rng = np.random.default_rng(2121)
    out = []
    row = 15500
    names = ["vrell-growth-b", "vrell-growth-a", "vrell-growth-c"]
    while row < 18900:
        k = int(rng.integers(0, 3))
        x = float(rng.choice([rng.uniform(40, 150), rng.uniform(330, 440), rng.uniform(150, 330)], p=[0.4, 0.4, 0.2]))
        if row < DUNES - 100 and 150 < x < 330:
            x = 70.0 if x < 240 else 410.0
        out.append((names[k], int(row), int(round(x))))
        row += int(rng.uniform(150, 260)) if row > DUNES else int(rng.uniform(260, 400))
    return out


PILLARS = [("hellas-pillar-a", 7420, 64), ("hellas-pillar-c", 7180, 412), ("hellas-pillar-b", 6920, 120),
           ("hellas-pillar-a", 6700, 370), ("hellas-pillar-b", 6480, 44), ("hellas-pillar-c", 6300, 430),
           ("hellas-pillar-b", 7040, 296)]


def crawler_spots():
    """Three abandoned crawlers on the rim road (section 6), each on a leg's centreline, parked to one side."""
    out = []
    for row, side in ((5420, -1), (3680, 1), (2300, -1)):
        x, leg = road_x(row)
        out.append((f"militia-crawler-{leg}", row, round(x + side * 6)))
    out.append(("militia-crawler-wreck", 4420, 446))
    return out


RIM_GUNS = [("rim-gun-a", 60), ("rim-gun-b", 168), ("rim-gun-a", 312), ("rim-gun-b", 420)]


def placements():
    out = []
    for name, (g, _, _, _) in SEAMS.items():
        out.append(("# the zone seams, centred on their boundaries (exact rows)" if name == "seam-road" else "",
                    {"piece": name, "row": g, "x": 240}))
    out.append(("# 7. the rim's crest (its boundary row 400: the screen's top band at the level's end)",
                {"piece": "rim-lip", "row": LIP, "x": 240}))
    for k, (name, row, x) in enumerate(growth_spots()):
        out.append(("# 1-3. the Vrell staging grounds on the floor (the strike, seen again after the turn)" if k == 0 else "",
                    {"piece": name, "row": row, "x": x}))
    row, _ = target_centre("dune ridge nest")
    out.append(("# 3. the dune ridge the t~42 turret nest stands on (its centre row: the nest's)",
                {"piece": "dune-ridge", "row": int(round(row)), "x": 240}))
    for k, (name, row, x) in enumerate(PILLARS):
        out.append(("# 5. the lull's pillars, clear of the middle lane" if k == 0 else "", {"piece": name, "row": row, "x": x}))
    for k, (name, row, x) in enumerate(crawler_spots()):
        out.append(("# 6. abandoned militia crawlers on the rim road" if k == 0 else "", {"piece": name, "row": row, "x": x}))
    for k, (name, x) in enumerate(RIM_GUNS):
        out.append(("# 7. the militia's guns on the lip, firing up past the ship" if k == 0 else "",
                    {"piece": name, "row": LIP - 30, "x": x}))
    return out


def write_proposal():
    places = placements()
    flow = k8.flow
    lines = ["backdrop:",
             "  # Presentation only: Retreat from Hellas (tools/art/backdrop_l20.py, from mars-hellas-r36-a).",
             "  # The ground is opaque; no deep layer (pending round 43). M7 part G: the ground is a STRIP keyed by",
             "  # strip row (strip row = strip.origin + the ground scroll + y, y counted up from the screen's bottom",
             "  # edge), not by section: Level 20 flies back over ground it has shown. A tile's pixel row is",
             "  # floorMod(strip row, height) from the image's bottom; ground pieces are placed by `row` (their",
             "  # centre's strip row), not `t`. The rows do not move with the data's timing; re-run",
             "  # `backdrop_l20.py --proposal` only to re-align the dune ridge with its nest (it reads the data).",
             f"  scroll_factors: {flow({'far': 0.5, 'ground': 1, 'low-air': 1.35, 'high-air': 2})}"
             "   # far and high-air unused (no deep layer, no high-air art), given as the loader asks",
             f"  ramp: {RAMP}",
             f"  haze_colour: {HAZE_COLOUR}  # the butterscotch sky (mars_r36.SKY)",
             "  atmosphere:"]
    notes = {"clear": "the lull and over the rim: haze only", "light": "the floor, the terraces, the rim road: faint dust",
             "medium": "the turnabout: the ridge's wake"}
    for k, v in ATMOSPHERE.items():
        lines.append(f"    {k}: {flow(v)}   # {notes[k]}")
    lines.append("  strip:")
    lines.append(f"    origin: {ORIGIN}   # the strip row at the screen's bottom edge at t=0 (row 0: the bottom edge at the end)")
    lines.append("    zones:   # the ground's tile set by strip row, [from, to); the ends lie beyond any scroll")
    zone_notes = {"rim-plain": "beyond the rim", "rim-slope": "6. the rim road's switchbacks",
                  "upper-terrace": "5. the lull", "terraces": "4. the terraces", "basin-dunes": "3. the dune sea (the t~42 ridge)"}
    for lo, hi, name in ZONES:
        note = zone_notes.get(name) or ("1-3. the staging grounds" if lo == DUNES else "3. the salt flats (the t~60 mortars)")
        lines.append(f"      - {flow({'from': lo, 'to': hi, 'tiles': [name]})}   # {note}")
    lines.append("  tile_sets:")
    for name, (layer, drift, note) in TILE_SETS.items():
        spec = {"layer": layer, "height": TILE_H}
        if drift is not None:
            spec["drift"] = drift
        lines.append(f"    {name}: {flow(spec)}   # {note}")
    lines.append("  pieces:")
    for name, (layer, note, extra) in PIECES.items():
        lines.append(f"    {name}: {flow({'layer': layer, 'size': size_of(name), **extra})}   # {note}")
    lines.append("  placed:   # ground pieces by strip row (their centre)")
    for comment, p in places:
        if comment:
            lines.append(f"    {comment}")
        lines.append(f"    - {flow(p)}")
    lines.append("  # Atmospheres: 1 light; 2 medium; 3, 4 light; 5 clear; 6 light; 7 clear.")
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# =========================================================================== check

_cache = {}


def cached(name):
    if name not in _cache:
        _cache[name] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    return _cache[name]


def visited():
    rows = [bottom_row(t) for t in np.arange(0, END + OUTRO, 0.25)]
    return min(rows), max(rows) + SCREEN


def check():
    problems = []
    for name in TILE_SETS:
        im = cached(name)
        if im.shape[:2] != (TILE_H, W):
            problems.append(f"{name}: {im.shape[1]}x{im.shape[0]} is not {W}x{TILE_H}")
        score = k8.wrap_score(im)
        # the ground tiles never repeat across (the play field is one tile wide); the low-air ones drift across
        cols = k8.wrap_score(im, columns=True) if TILE_SETS[name][0] != "ground" else 0.0
        print(f"  wrap {name}: rows {score:.2f} columns {cols:.2f}")
        if score > 1.5 or cols > 1.5:
            problems.append(f"{name}: a line where it repeats ({score:.2f}, {cols:.2f})")
        if TILE_SETS[name][0] == "ground" and (im[..., 3] < 255).any():
            problems.append(f"{name}: a ground tile with transparent pixels (the ground is opaque)")
    for name in PIECES:
        if name in ANIMATED:
            ims = [np.array(Image.open(OUT / f"{name}_{k}.png").convert("RGBA")) for k in range(GUN_FRAMES)]
        else:
            ims = [cached(name)]
        for a in ims:
            a = a[..., 3] > 0
            full_width = name in SEAMS or name == "rim-lip"
            if a[0].any() or a[-1].any():
                problems.append(f"{name}: its top or bottom row is opaque")
            if not full_width and (a[:, 0].any() or a[:, -1].any()):
                problems.append(f"{name}: a side column is opaque")
    for i in range(1, len(ZONES)):
        if ZONES[i][0] != ZONES[i - 1][1]:
            problems.append(f"zones {ZONES[i - 1][2]} and {ZONES[i][2]} do not meet")
    lo, hi = visited()
    print(f"  rows shown: {lo} .. {hi} (zones {STRIP_LO} .. {STRIP_HI})")
    if lo < STRIP_LO or hi > STRIP_HI:
        problems.append(f"the scroll shows rows {lo}..{hi} beyond the strip")
    bounds = {g for g, _, _, _ in SEAMS.values()} | {LIP}
    if bounds != {z[0] for z in ZONES[1:]}:
        problems.append("a zone boundary without its seam (or a seam off its boundary)")
    for name, (g, lower, upper, _) in SEAMS.items():
        if zone_of(g - 1) != lower or zone_of(g) != upper:
            problems.append(f"{name}: its tile sets are not the zones' at row {g}")
    for _, p in placements():
        if p["piece"] in SEAMS and p["row"] != SEAMS[p["piece"]][0]:
            problems.append(f"{p['piece']} is not centred on its boundary")
    for msg in problems:
        print("PROBLEM", msg)
    print(f"check: {len(problems)} problem(s)")
    return not problems


# =========================================================================== review

def over(dst, src, x, y):
    h, w = src.shape[:2]
    y0, y1 = max(0, y), min(dst.shape[0], y + h)
    x0, x1 = max(0, x), min(dst.shape[1], x + w)
    if y1 <= y0 or x1 <= x0:
        return
    s = src[y0 - y:y1 - y, x0 - x:x1 - x]
    a = s[..., 3:4] / 255
    dst[y0:y1, x0:x1] = s[..., :3] * a + dst[y0:y1, x0:x1] * (1 - a)


def atmosphere_at(t):
    for i, s in enumerate(SECTIONS):
        if t < s["end"] or i == len(SECTIONS) - 1:
            return s.get("atmosphere", "light")


def frame_at(t, placed, frame_no=None):
    view = bottom_row(t)
    frame = np.zeros((SCREEN, W, 3))
    rows = view + (SCREEN - 1 - np.arange(SCREEN))
    for lo, hi, name in ZONES:
        sel = (rows >= lo) & (rows < hi)
        if sel.any():
            img = cached(name)
            frame[sel] = img[[TILE_H - 1 - (int(r) % TILE_H) for r in rows[sel]]][..., :3]
    gun = int(t * GUN_FPS) % GUN_FRAMES if frame_no is None else frame_no
    for p in placed:
        name = p["piece"] if p["piece"] not in ANIMATED else f"{p['piece']}_{gun}"
        img = cached(name)
        h, w = img.shape[:2]
        bottom = jr(p["row"] - h / 2)
        top_y = SCREEN - (bottom + h - view)
        if -h < top_y < SCREEN:
            over(frame, img, jr(p["x"] - w / 2), top_y)
    atm = ATMOSPHERE[atmosphere_at(t)]
    if atm.get("banks"):
        img = cached(atm["banks"])
        lr = jr(scroll_at(t) * FACTORS["low-air"]) + (SCREEN - 1 - np.arange(SCREEN))
        bank = np.roll(img[[TILE_H - 1 - (int(r) % TILE_H) for r in lr]], int(TILE_SETS[atm["banks"]][1] * t) % W, 1)
        over(frame, bank, 0, 0)
    return np.clip(frame, 0, 255).astype(np.uint8)


TIMES = [3, 21.5, 30, 42, 58, 72, 92, 112, 124.5, 138, 165, 171.5]


def review():
    placed = [p for _, p in placements()]
    scale = 0.5
    cw, ch = int(W * scale), int(SCREEN * scale)
    cols = 6
    strip_w = 120
    sheet = raster.sheet(16 + cols * (cw + 10) + strip_w + 20, 60 + 2 * (ch + 24) + 300,
                         "LEVEL 20 RETREAT FROM HELLAS - BACKDROP (FINAL R43)", "PRODUCTION ART, M7 PART G - R43")
    raster.draw_text(sheet, 16, 38, "THE PLAY FIELD (GROUND AND LOW-AIR DUST) AT 0.5X; THE GROUND MOVES UP THE SCREEN FROM T=22."
                     " NO UNITS OR PROPS", raster.LABEL)
    for i, t in enumerate(TIMES):
        fr = Image.fromarray(frame_at(t, placed), "RGB")
        x, y = 16 + (i % cols) * (cw + 10), 54 + (i // cols) * (ch + 24)
        sheet.paste(fr.resize((cw, ch), Image.BOX), (x, y))
        sec = next((s["name"] for s in SECTIONS if t < s["end"]), SECTIONS[-1]["name"])
        raster.draw_text(sheet, x, y + ch + 4, f"T={t} {sec.upper()} R{bottom_row(t)}", raster.LABEL_DIM)
    # the whole strip at 1/40, the zones named
    sx = 16 + cols * (cw + 10) + 10
    lo, hi = -540, 19200
    k = 40
    rows = hi - 1 - np.arange(0, hi - lo, k)
    strip = np.zeros((len(rows), W, 3))
    for i, r in enumerate(rows):
        strip[i] = cached(zone_of(r))[TILE_H - 1 - (int(r) % TILE_H), :, :3]
    simg = Image.fromarray(strip.astype(np.uint8), "RGB").resize((strip_w, strip.shape[0]), Image.BOX)
    sheet.paste(simg, (sx, 54))
    for lo_, hi_, name in ZONES:
        a, b = max(lo_, lo), min(hi_, hi)
        if b > a:
            raster.draw_text(sheet, sx + 2, 54 + (hi - b) // k + 2, name.upper(), raster.LABEL_DIM)
    raster.draw_text(sheet, sx, 40, "THE STRIP AT 1/40", raster.LABEL)
    y = 54 + 2 * (ch + 24) + 6
    raster.draw_text(sheet, 16, y, "PIECES AT 1X (RIM GUN FRAMES 0-3) AND THE SEAMS AT 0.25X", raster.LABEL)
    x = 16
    for name in ("dune-ridge", "vrell-growth-a", "vrell-growth-c", "hellas-pillar-a", "hellas-pillar-b",
                 "hellas-pillar-c", "militia-crawler-a", "militia-crawler-b", "militia-crawler-wreck"):
        img = Image.open(OUT / f"{name}.png").convert("RGBA")
        if name == "dune-ridge":
            img = img.resize((img.width // 2, img.height // 2), Image.BOX)
        sheet.alpha_composite(img, (x, y + 16))
        raster.draw_text(sheet, x, y + 16 + img.height + 2, name.upper().replace("MILITIA-", ""), raster.LABEL_DIM)
        x += img.width + 8
    for k_ in range(GUN_FRAMES):
        img = Image.open(OUT / f"rim-gun-a_{k_}.png").convert("RGBA")
        sheet.alpha_composite(img, (x, y + 16))
        x += img.width + 4
    raster.draw_text(sheet, x - 4 * 60, y + 16 + 58, "RIM-GUN-A 0-3", raster.LABEL_DIM)
    x, y2 = 16, y + 160
    for name in list(SEAMS) + ["rim-lip"]:
        img = Image.open(OUT / f"{name}.png").convert("RGBA")
        img = img.resize((img.width // 4, img.height // 4), Image.BOX)
        sheet.alpha_composite(img, (x, y2 + 16))
        raster.draw_text(sheet, x, y2 + 16 + img.height + 2, name.upper(), raster.LABEL_DIM)
        x += img.width + 10
    CONCEPT.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(REVIEW, optimize=True)
    gif = [Image.fromarray(frame_at(float(t), placed), "RGB").resize((240, 270), Image.BOX)
           for t in np.arange(0.0, END, 0.6)]
    write_gif(gif, REVIEW.with_suffix(".gif"), fps=10, colors=128)
    print(f"review: {REVIEW.relative_to(ROOT)} and .gif")


if __name__ == "__main__":
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    if "--proposal" in sys.argv[1:]:
        write_proposal()
    elif "--review" in sys.argv[1:]:
        review()
    elif "--check" in sys.argv[1:]:
        sys.exit(0 if check() else 1)
    else:
        write_all(args)
        write_proposal()
        ok = check()
        review()
        sys.exit(0 if ok else 1)
