#!/usr/bin/env python3
"""Production art: the Level 19 backdrop, Foundry of Tharsis (design/campaign/act-3-red-dust/
level-19-foundry-of-tharsis; M7 part F, step ART-F2, review in round 42).

Straight to production from the approved Tharsis foundry mood still `mars-tharsis-foundry-r36-a`
(tools/concept/mars_r36.py `foundry()`, imported unchanged for its ramps): dark basalt slag with glowing
cracks, roofless halls with low concrete walls, belts of dark plates with rollers and amber arrows, the
furnaces' white-orange glow, the round smelter tower. Lit from the upper left (backdrop_l15.lit /
backdrop_l17.finish_piece), receding, posterized per image. Nothing lit is mirrored.

The ground is opaque everywhere (no `deep` layer, as Levels 17 and 18: the README's "smelter tower on the
left horizon" cannot show under a top-down ground; the tower is drawn from above at the left edge in
sections 1 and 7, pending the user's review in round 42). Layers: ground (x1), low-air (x1.35).

Tile sets (480 wide, 960 tall, wrapping): ground `slag-field` (sections 1, 7: basalt slag, heaps, glowing
cracks, rust), `hall-floor` (2: the two roofless halls' slab floors x 50-230 and 250-430, low walls and
column stubs, slag between), `furnace-floor` (3: riveted iron plates, slag pools glowing at the edges),
`yard-apron` (4: packed slag with two rail pairs), `casting-yard` (5, 6: cast slabs, cooling moulds,
molten channels; scenery); low-air `stack-smoke` (light) and `furnace-smoke` (medium, warmer, denser).

Pieces (ground): seams `seam-halls`, `seam-furnaces`, `seam-apron`, `seam-yard`, `seam-south` (480x320,
exact rows); `belt-plates` (80x480, animated: 4 frames at 10 fps, the plates 16 px apart moving 4 px up a
frame = 40 px/s up the screen relative to the ground, the README's belt speed; full height, copies at exact
rows join; one id, so all belts count as one animated element); `belt-end` (80x48, the drum housing at a
run's ends); `smelter-tower` (200x200); `smelter-stack` (96x96); `slag-heap` (160x128); `yard-gate`
(480x96: the yard wall with its gate and the belt slots); `south-gate` (480x480: the outer wall with the
open gate, the plateau beyond, at the level's end).

Belt runs (x, from the README): section 2 at 140 and 340, 3 at 240, 4 at 100 and 380, 7 the outbound belt
at 240 up to the south gate. The proposal lists each run's times for the data step's `Belt` specs.

Outputs: assets/backdrop/level-19/*.png; the review sheet and GIF
design/campaign/act-3-red-dust/level-19-foundry-of-tharsis/concept/backdrop-final-r42-a.png/.gif;
`--proposal` writes the level's `backdrop:` block to briefs/m7f-backdrop.yaml (for the data step to merge;
never into the level's data.yaml); `--check` checks the tile wraps, sizes, edges, seams and belt joins.

Reads the level's data.yaml when it exists (sections with speed and `ramp`), otherwise the README's values.
Run: python3 tools/art/backdrop_l19.py [--proposal | --review | --check] [id ...] (all: ~1 min)
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
import backdrop_l17 as b17  # noqa: E402  (tools/art: the Mars plain base, piece finishing)
import mars_r36 as m36  # noqa: E402  (concept script, unchanged: the Mars ramps)
from render import raster  # noqa: E402

SCRIPT = "backdrop_l19.py"
BATCH = "M7 part F"
SOURCE = artkit.source_note(SCRIPT, BATCH)
LEVEL_DIR = DESIGN / "campaign" / "act-3-red-dust" / "level-19-foundry-of-tharsis"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = ROOT / "briefs" / "m7f-backdrop.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-19"
REVIEW = CONCEPT / "backdrop-final-r42-a.png"
W, SCREEN = 480, 540
MID = SCREEN / 2
TILE_H = 960
FACTORS = {"ground": 1.0, "low-air": 1.35}
HAZE_COLOUR = "d6a670"                  # mars_r36.SKY: the butterscotch sky
RAMP = 3
ATMOSPHERE = {
    "clear": {"haze": 0.04},
    "light": {"banks": "stack-smoke", "haze": 0.06},
    "medium": {"banks": "furnace-smoke", "haze": 0.1},
}
BASALT, RUST, PLAIN = m36.BASALT, m36.RUST, m36.PLAIN
GLOW = np.array((255, 132, 36), float)
HOT = np.array((255, 226, 150), float)
CONCRETE = [(70, 68, 66), (98, 94, 90), (126, 120, 112), (156, 148, 138), (186, 178, 166)]
STEEL = [(40, 40, 44), (62, 62, 68), (90, 88, 92), (124, 120, 120), (164, 158, 152)]
AMBER = np.array((178, 140, 40), float)
SMOKE = [(30, 28, 28), (58, 54, 52), (92, 86, 82), (124, 116, 108)]
SMOKE_WARM = [(46, 30, 24), (84, 58, 44), (122, 90, 68), (160, 122, 92)]

pn, aniso, smooth, ramp = b15.pn, b15.aniso, b15.smooth, b15.ramp
lit, posterize_rgb, dither_alpha = b15.lit, b15.posterize_rgb, b15.dither_alpha
finish, finish_piece = b17.finish, b17.finish_piece
jr = k8.java_round


# =========================================================================== the level's timeline

DEFAULT_SECTIONS = [   # the README's Layout until the level data exists (the data step adds 2 s eases)
    {"name": "Slag Fields", "end": 24, "speed": 150, "atmosphere": "light"},
    {"name": "Conveyor Halls", "end": 64, "speed": 130, "ramp": 2, "atmosphere": "light"},
    {"name": "Furnace Row", "end": 104, "speed": 130, "atmosphere": "medium"},
    {"name": "Yard Approach", "end": 132, "speed": 120, "ramp": 2, "atmosphere": "light"},
    {"name": "Casting Yard", "end": 133, "speed": 0, "atmosphere": "light"},
    # 0 -> 20 -> 150 in the data (the walker's crawl); 390 px over 14 s as one speed until then
    {"name": "The Wreck", "end": 147, "speed": 27.857, "atmosphere": "light"},
    {"name": "South Gate", "end": 172, "speed": 150, "ramp": 2, "atmosphere": "clear"},
]
SECTION_TILES = [["slag-field"], ["hall-floor"], ["furnace-floor"], ["yard-apron"], ["casting-yard"],
                 ["casting-yard"], ["slag-field"]]


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
    if i == 0 or speed(i) == 0 or speed(i - 1) == 0 or speed(i) == speed(i - 1):
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
    """LevelData.scrollAt with the M7 part B ease (as backdrop_l18.scroll_at)."""
    scroll = 0.0
    for i, s in enumerate(SECTIONS):
        if t < s["end"] or i == len(SECTIONS) - 1:
            return scroll + _travelled(i, STARTS[i], t)
        scroll += _travelled(i, STARTS[i], s["end"])
    raise AssertionError


def t_at(scroll):
    lo, hi = -60.0, END + 80
    for _ in range(80):
        mid = (lo + hi) / 2
        if scroll_at(mid) < scroll:
            lo = mid
        else:
            hi = mid
    return (lo + hi) / 2


def seam(layer, s):
    """Where section s's tile set begins on the layer."""
    return jr(scroll_at(STARTS[s]) * FACTORS[layer] + SCREEN)


def t_for_bottom(layer, bottom, h):
    """A placement time whose piece (height h) has its bottom edge exactly at layer position ``bottom``."""
    f = FACTORS[layer]
    t = round(t_at((bottom + h / 2 - MID) / f), 4)
    for dt in (0, 1e-4, -1e-4, 2e-4, -2e-4, 3e-4, -3e-4, 4e-4, -4e-4):
        if jr(scroll_at(t + dt) * f + MID - h / 2) == bottom:
            return round(t + dt, 4)
    raise AssertionError((layer, bottom, h, t))


def t_centre(pos, layer="ground"):
    return round(t_at((pos - MID) / FACTORS[layer]), 3)


def top_pos(t, layer="ground"):
    return scroll_at(t) * FACTORS[layer] + SCREEN


def piece_bottom(p, h, layer="ground"):
    return jr(scroll_at(p["t"]) * FACTORS[layer] + MID - h / 2)


# =========================================================================== tile sets

def grid():
    return np.mgrid[0:TILE_H, 0:W].astype(np.float64)


def slag_base(seed):
    """Basalt slag: rough clinker, low heaps, rust stains; the height, albedo and the glowing cracks' mask."""
    n = pn(W, TILE_H, 32, seed, 5)
    heap = pn(W, TILE_H, 160, seed + 1, 3)
    grit = np.random.default_rng(seed + 2).random((TILE_H, W))
    hgt = 2.2 * n + 4.5 * smooth(0.55, 0.85, heap) + 0.5 * grit
    tone = np.clip(0.3 + 0.45 * (n - 0.5) + 0.18 * smooth(0.55, 0.85, heap) + 0.08 * grit, 0, 1)
    alb = ramp(BASALT, tone)
    rust = smooth(0.6, 0.78, pn(W, TILE_H, 96, seed + 3, 3))
    alb = alb * (1 - 0.4 * rust[..., None]) + np.array(RUST[2], float) * 0.4 * rust[..., None]
    crack = (np.abs(pn(W, TILE_H, 60, seed + 4, 3) - 0.5) < 0.009) & (pn(W, TILE_H, 120, seed + 5, 2) > 0.56)
    return hgt, alb, crack


def glow_into(rgb, mask, amount=0.75, halo=True):
    """Emissive cracks/pools over the lit ground: a hot core and a soft orange halo (wrapping)."""
    m = mask.astype(np.float64)
    if halo:
        h = m.copy()
        for _ in range(3):
            h = (h + np.roll(h, 1, 0) + np.roll(h, -1, 0) + np.roll(h, 1, 1) + np.roll(h, -1, 1)) / 5
        h = np.clip(h * 2.2, 0, 1) * 0.45
        rgb = rgb * (1 - h[..., None]) + GLOW * h[..., None]
    return rgb * (1 - amount * m[..., None]) + GLOW * amount * m[..., None]


def slag_field():
    """Sections 1 and 7: the slag fields: black basalt clinker in low heaps, rust stains, glowing cracks."""
    hgt, alb, crack = slag_base(1901)
    rgb = glow_into(lit(hgt, alb, ambient=0.45, key=0.65), crack)
    return finish(rgb, 32)


HALLS = ((50, 230), (250, 430))


def hall_floor():
    """Section 2: two roofless halls' slab floors (x 50-230 and 250-430) with low concrete walls and the
    steel column stubs of the gone roof every 120 px, slab joints, oil stains; slag outside and between."""
    yy, xx = grid()
    hgt, alb, crack = slag_base(1911)
    n = pn(W, TILE_H, 40, 1913, 3)
    floor = np.zeros(xx.shape, bool)
    for a, b in HALLS:
        floor |= (xx >= a) & (xx < b)
    joint = ((np.abs(((xx - 50) % 60) - 30) > 29) | (yy % 60 == 30)) & floor     # no joint on the wrap rows
    stain = smooth(0.62, 0.78, pn(W, TILE_H, 48, 1915, 3)) * floor
    falb = ramp(CONCRETE, np.clip(0.48 + 0.3 * (n - 0.5), 0, 1))
    falb = falb * (1 - 0.45 * stain[..., None]) + np.array((40, 34, 30), float) * 0.45 * stain[..., None]
    falb = np.where(joint[..., None], falb * 0.7, falb)
    alb = np.where(floor[..., None], falb, alb)
    hgt = np.where(floor, 0.4 * n - 0.4 * joint, hgt)
    for a, b in HALLS:
        for e in (a, b):
            wall = np.abs(xx - e) < 4
            col = (np.abs(xx - e) < 8) & (np.abs((yy % 120) - 60) < 7)
            hgt = np.where(wall, 3.0, hgt)
            hgt = np.where(col, 6.0, hgt)
            alb = np.where(wall[..., None], np.array(CONCRETE[3], float), alb)
            alb = np.where(col[..., None], np.array(STEEL[2], float), alb)
    rgb = glow_into(lit(hgt, alb, ambient=0.45, key=0.65), crack & ~floor)
    return finish(rgb, 32)


def furnace_floor():
    """Section 3: the furnace row's floor: riveted iron plates 40 px square, heat-darkened, slag pools
    glowing orange near the stacks at the edges (x < 90, x > 390), a slag road down the middle."""
    yy, xx = grid()
    n = pn(W, TILE_H, 32, 1921, 4)
    px, py = xx % 40, (yy + 20) % 40                  # no plate seam on the wrap rows
    seam_ = (px < 1) | (py < 1)
    rivet = (np.hypot(np.abs(px - 20) - 16, np.abs(py - 20) - 16) < 1.3)
    heat = smooth(0.4, 0.8, pn(W, TILE_H, 80, 1923, 3))
    alb = ramp(STEEL, np.clip(0.42 + 0.3 * (n - 0.5) - 0.15 * heat, 0, 1))
    alb = alb * (1 - 0.25 * heat[..., None]) + np.array((92, 52, 34), float) * 0.25 * heat[..., None]
    alb = np.where(seam_[..., None], alb * 0.6, alb)
    hgt = 0.3 * n - 0.5 * seam_ + 0.6 * rivet
    side = np.clip(1 - np.minimum(xx, W - xx) / 110, 0, 1)
    pool = smooth(0.5, 0.56, pn(W, TILE_H, 48, 1925, 3) * 0.6 + side * 0.55)
    sh, salb, crack = slag_base(1927)
    alb = alb * (1 - pool[..., None]) + salb * pool[..., None]
    hgt = hgt * (1 - pool) + (sh * 0.6 - 1.0) * pool
    core = smooth(0.86, 0.9, pn(W, TILE_H, 48, 1925, 3) * 0.6 + side * 0.55)
    rgb = lit(hgt, alb, ambient=0.45, key=0.65)
    rgb = glow_into(rgb, core > 0.5, amount=0.85)
    rgb = glow_into(rgb, crack & (pool > 0.5), halo=False)
    return finish(rgb, 36)


def yard_apron():
    """Section 4: the casting yard's approach: packed grey slag, tyre and track ruts, two rail pairs
    (x 30 and 450 under the edges), oil and rust."""
    yy, xx = grid()
    hgt, alb, crack = slag_base(1931)
    n = pn(W, TILE_H, 24, 1933, 4)
    packed = ramp(CONCRETE[:4], np.clip(0.42 + 0.35 * (n - 0.5), 0, 1)) * 0.82
    k = smooth(0.25, 0.45, pn(W, TILE_H, 120, 1935, 2) + 0.25)
    alb = alb * (1 - k[..., None]) + packed * k[..., None]
    hgt = hgt * (1 - 0.7 * k)
    ruts = sum(np.exp(-((xx - c - 6 * np.sin(2 * np.pi * yy / TILE_H * 2 + c)) / 2.2) ** 2) for c in (176, 196, 284, 304))
    hgt -= 0.8 * ruts
    alb = alb * (1 - 0.2 * np.clip(ruts, 0, 1)[..., None])
    for c in (30, 450):
        for r in (c - 9, c + 9):
            rail = np.abs(xx - r) < 1.5
            hgt = np.where(rail, 1.8, hgt)
            alb = np.where(rail[..., None], np.array(STEEL[3], float), alb)
        tie = (np.abs(xx - c) < 14) & (yy % 14 < 4)
        hgt = np.where(tie & ~(np.abs(np.abs(xx - c) - 9) < 1.5), hgt + 0.6, hgt)
        alb = np.where(tie[..., None] & (np.abs(np.abs(xx - c) - 9) >= 1.5)[..., None], np.array(BASALT[2], float), alb)
    rgb = glow_into(lit(hgt, alb, ambient=0.45, key=0.65), crack & (k < 0.3), amount=0.6)
    return finish(rgb, 32)


def casting_yard():
    """Sections 5 and 6, the arena: the casting yard's floor: rows of cast slabs (still warm at their
    cores), cooling moulds (dark pits, a dull red core), two molten channels crossing the field (scenery)."""
    yy, xx = grid()
    n = pn(W, TILE_H, 32, 1941, 4)
    alb = ramp(CONCRETE, np.clip(0.38 + 0.3 * (n - 0.5), 0, 1)) * 0.85
    hgt = 0.3 * n
    rng = np.random.default_rng(1943)
    emit = np.zeros(xx.shape)
    for row in range(8):
        y0 = row * 120 + 16
        x = rng.uniform(4, 30)
        while x < W - 60:
            w = rng.uniform(56, 96)
            h = rng.uniform(52, 76)
            kind = rng.random()
            box = (xx >= x) & (xx < x + w) & (yy >= y0) & (yy < y0 + h)
            if kind < 0.3:                                    # a cooling mould: a pit with a dull red core
                hgt = np.where(box, -2.5, hgt)
                alb = np.where(box[..., None], np.array(STEEL[0], float), alb)
                core = (xx >= x + 8) & (xx < x + w - 8) & (yy >= y0 + 8) & (yy < y0 + h - 8)
                emit = np.maximum(emit, core * 0.45)
            elif kind < 0.85:                                 # a cast slab, warm towards its centre
                hgt = np.where(box, hgt + 2.0, hgt)
                warm = np.clip(1 - np.hypot((xx - x - w / 2) / (w / 2), (yy - y0 - h / 2) / (h / 2)), 0, 1) * box
                slab = ramp(STEEL[1:], np.clip(0.45 + 0.2 * (n - 0.5), 0, 1))
                alb = np.where(box[..., None], slab * (1 - 0.35 * warm[..., None]) + np.array((150, 70, 40), float) * 0.35 * warm[..., None], alb)
            x += w + rng.uniform(10, 26)
    for c in (300, 780):
        cy = c + 10 * np.sin(2 * np.pi * xx / W * 2 + c)
        d = np.abs(yy - cy)
        lip = d < 9
        hgt = np.where(lip, 1.2, hgt)
        hgt = np.where(d < 5, -1.5, hgt)
        alb = np.where(lip[..., None], np.array(BASALT[2], float), alb)
        emit = np.maximum(emit, (d < 4.5) * 1.0)
    rgb = lit(hgt, alb, ambient=0.45, key=0.65)
    rgb = rgb * (1 - np.clip(emit, 0, 1)[..., None]) + (GLOW * 0.7 + HOT * 0.3 * (emit > 0.9)[..., None]) * np.clip(emit, 0, 1)[..., None]
    rgb = glow_into(rgb, emit > 0.9, amount=0.0)
    return finish(rgb, 36)


def stack_smoke():
    return b15.cloud_bank(W, TILE_H, 1951, 0.7, 0.14, 70, colours=SMOKE)


def furnace_smoke():
    return b15.cloud_bank(W, TILE_H, 1953, 0.6, 0.16, 92, colours=SMOKE_WARM)


# =========================================================================== seams

FEATHER = 32
SEAM_H = 320
SEAMS = {"seam-halls": (1, "slag-field", "hall-floor", 1961), "seam-furnaces": (2, "hall-floor", "furnace-floor", 1963),
         "seam-apron": (3, "furnace-floor", "yard-apron", 1965), "seam-yard": (4, "yard-apron", "casting-yard", 1967),
         "seam-south": (6, "casting-yard", "slag-field", 1969)}


def tile_strip(name, bottom, h):
    img = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    hgt = img.shape[0]
    pos = bottom + h - 1 - np.arange(h)
    return img[[hgt - 1 - (int(p) % hgt) for p in pos]]


def seam_piece(name):
    """backdrop_l18.seam_piece: the lower section's tiles below a ragged boundary at the seam, the upper's
    above, a darker line on the boundary, the ends faded into the tile sets (ordered-dithered 1-bit alpha)."""
    s, lower, upper, seed = SEAMS[name]
    g = seam("ground", s)
    bottom, h = g - SEAM_H // 2, SEAM_H
    up = tile_strip(upper, bottom, h)
    lo = tile_strip(lower, bottom, h)
    rows = bottom + h - 1 - np.arange(h)[:, None]
    xs = np.arange(W)[None, :]
    rng = np.random.default_rng(seed)
    wob = sum(a * np.sin(2 * np.pi * c * xs / W + rng.uniform(0, 6.3)) for c, a in zip((1, 2, 5, 11), (16, 10, 5, 3)))
    edge = g + wob + (np.random.default_rng(seed + 1).random((1, W)) - 0.5) * 3
    out = np.where((rows >= edge)[..., None], up, lo)
    d = rows - edge
    r = (d >= 0) & (d < 2)
    out[..., :3] = np.where(r[..., None], out[..., :3] * 0.75, out[..., :3])
    k = np.arange(h)[:, None]
    a = np.minimum(np.clip((k + 0.5) / FEATHER, 0, 1), np.clip((h - k - 0.5) / FEATHER, 0, 1))
    alpha = dither_alpha(np.broadcast_to(a, (h, W)), 2)
    out[..., 3] = np.where(alpha > 0, 255, 0)
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out.astype(np.uint8), "RGBA")


# =========================================================================== pieces

BELT = (80, 480)
PITCH, STEP, BELT_FRAMES, BELT_FPS = 16, 4, 4, 10      # 4 px a frame at 10 fps: 40 px/s up the screen


def belt_frame(k):
    """One frame of the belt: low concrete walls, roller rails with rollers every 24 px (static), the dark
    plates 16 px apart moving 4k px up, an amber chevron every fifth plate; full height, wrapping."""
    w, h = BELT
    f = 2
    yy, xx = b15.piece_canvas(BELT, f)
    d = np.abs(xx - w / 2)
    wall = (d >= 32) & (d < 38)
    rail = (d >= 25) & (d < 31)
    roller = rail & ((yy % 24) >= 10) & ((yy % 24) < 14)
    bed = d < 24
    py = (yy + STEP * k) % PITCH                      # the plates move up: the pattern shifts with k
    idx = np.floor((yy + STEP * k) / PITCH) % 5
    gap = (py >= 2) & (py < 6)                       # its edges on rows 1 mod 4: no false wrap line
    hgt = np.where(wall, 4.0, 0.0) + np.where(rail, 1.0, 0.0) + np.where(roller, 1.6, 0.0)
    hgt = np.where(bed, 1.2 - 0.8 * gap + 0.25 * np.sin(py / PITCH * np.pi), hgt)
    alb = np.zeros(yy.shape + (3,))
    alb[:] = np.array(CONCRETE[3], float)
    alb = np.where(rail[..., None], np.array(STEEL[1], float), alb)
    alb = np.where(roller[..., None], np.array(STEEL[4], float), alb)
    plate = ramp(STEEL[:3], np.clip(0.4 + 0.15 * np.sin(xx * 0.9) * 0.2, 0, 1))
    alb = np.where(bed[..., None], plate, alb)
    alb = np.where((bed & gap)[..., None], np.array(STEEL[0], float) * 0.7, alb)
    chev = bed & (idx == 0) & (np.abs(py - 8 - (d * 0.45)) < 1.8) & (d < 12)
    alb = np.where(chev[..., None], AMBER, alb)
    cover = (d < 38).astype(np.float64)
    return finish_flat(hgt, alb, cover, f, colours=48)


def finish_flat(hgt, alb, cover, f=2, colours=32):
    """backdrop_l17.finish_piece for a piece that tiles along itself: wrapping light and no bottom-right
    fall-off (its gradient would leave a step where the copies join)."""
    a = k8.relief(hgt, alb, np.zeros_like(alb), np.ones(hgt.shape), f, wrap=True, ambient=0.42, key=0.7, ao=0.11,
                  gradient=0.0)
    c = np.clip(cover, 0, 1)
    c[:, :f], c[:, -f:] = 0, 0
    a[..., 3] = c
    nat = k8.reduce(a, f)
    nat[..., 3] = np.clip((nat[..., 3] - 0.5) * 2.5 + 0.5, 0, 1)
    img = posterize_rgb(b15.recede(nat[..., :3] * 255, 0.18), colours, dither_alpha(nat[..., 3], 2))
    arr = np.array(img)
    arr[arr[..., 3] == 0] = 0
    return Image.fromarray(arr, "RGBA")


def belt_frames():
    return artkit.quantize_set([belt_frame(k) for k in range(BELT_FRAMES)], 32)


def belt_end():
    """The drum housing at a belt run's end: a steel box across the belt, hazard stripes on its lip."""
    w, h = 80, 48
    f = 2
    yy, xx = b15.piece_canvas((w, h), f)
    box = (np.abs(xx - w / 2) < 38) & (np.abs(yy - h / 2) < 16)
    lip = box & (np.abs(np.abs(yy - h / 2) - 13) < 3)
    stripe = lip & (((xx + yy) % 10) < 5)
    hub = (np.abs(xx - w / 2) > 30) & (np.abs(yy - h / 2) < 8) & box
    hgt = np.where(box, 5.0, 0.0) + np.where(hub, 1.5, 0.0) - np.where(lip, 0.6, 0.0)
    alb = np.zeros(yy.shape + (3,))
    alb[:] = np.array(STEEL[2], float)
    alb = np.where(stripe[..., None], AMBER, alb)
    alb = np.where((lip & ~stripe)[..., None], np.array(STEEL[0], float), alb)
    alb = np.where(hub[..., None], np.array(STEEL[4], float), alb)
    return finish_piece(hgt, alb, box * 1.0, f, colours=24)


def round_piece(size, seed, r_out, r_in, wall_h, glow_r, deck=None, ribs=24):
    """A round stack seen from above: the ring wall, radial panel lines, an optional deck ring, the throat
    glowing white-orange in the middle (emission), soft shadow ring on the ground side."""
    w, h = size
    f = 2
    yy, xx = b15.piece_canvas(size, f)
    r = np.hypot(xx - w / 2, yy - h / 2)
    ang = np.arctan2(yy - h / 2, xx - w / 2)
    n = raster.fbm(w * f, h * f, 10, seed, octaves=2, period=False)
    body = r < r_out
    ring = body & (r > r_in)
    hgt = np.where(body, wall_h * 0.85, 0.0) + np.where(ring, wall_h * 0.15, 0.0)
    rib = ring & (np.abs(((ang / (2 * np.pi) * ribs) % 1) - 0.5) > 0.44)
    alb = np.zeros(yy.shape + (3,))
    alb[:] = np.array(STEEL[2], float) * (0.9 + 0.2 * n[..., None])
    alb = np.where(rib[..., None], np.array(STEEL[1], float), alb)
    if deck:
        dk = (r > deck[0]) & (r < deck[1])
        hgt = np.where(dk, wall_h * 0.92, hgt)
        alb = np.where(dk[..., None], np.array(CONCRETE[2], float), alb)
        rail = np.abs(r - deck[1]) < 1.0
        alb = np.where(rail[..., None], np.array(STEEL[4], float), alb)
    throat = r < glow_r
    hgt = np.where(throat, wall_h * 0.3, hgt)
    e = np.clip(1 - r / glow_r, 0, 1)
    emission = np.where(throat[..., None], (GLOW * 0.75 + HOT * 0.25 * e[..., None]) * (0.6 + 0.4 * e[..., None]), 0)
    alb = np.where(throat[..., None], np.array((40, 20, 10), float), alb)
    soot = smooth(0.0, 1.0, np.clip(1 - (r - glow_r) / 10, 0, 1)) * (r >= glow_r) * body
    alb = alb * (1 - 0.6 * soot[..., None])
    return finish_piece(hgt, alb, body * 1.0, f, colours=40, emission=emission)


def smelter_tower():
    """The smelter tower from above (the crew's refuge): a wide round top, a deck ring with its railing, the
    central smelter throat glowing."""
    return round_piece((200, 200), 1971, 92, 70, 22.0, 26, deck=(34, 62), ribs=36)


def smelter_stack():
    return round_piece((96, 96), 1973, 44, 32, 14.0, 22, ribs=20)


def slag_heap():
    """A black slag heap, its crest still glowing in a few veins, its foot thinning into the field."""
    w, h = 160, 128
    f = 2
    yy, xx = b15.piece_canvas((w, h), f)
    n = raster.fbm(w * f, h * f, 18, 1975, octaves=4, period=False)
    m = np.clip(1 - np.hypot((xx - w / 2) / (w * 0.46), (yy - h / 2) / (h * 0.44)) + (n - 0.5) * 0.45, 0, 1)
    hgt = 9 * smooth(0.0, 0.8, m) + 1.2 * n
    alb = ramp(BASALT, np.clip(0.25 + 0.4 * (n - 0.5) + 0.25 * m, 0, 1))
    vein = (np.abs(raster.fbm(w * f, h * f, 14, 1977, octaves=2, period=False) - 0.5) < 0.02) & (m > 0.55)
    emission = np.where(vein[..., None], GLOW * 0.8, 0)
    return finish_piece(hgt, alb, smooth(0.04, 0.2, m), f, colours=28, emission=emission)


def wall_piece(size, wall_y, wall_h, openings, seed, plateau=False):
    """A thick concrete wall across the field at rows wall_y..wall_y+wall_h with the given (x0, x1)
    openings, pylons at each segment's ends, buttresses; ``plateau`` fills everything above the wall with
    the rust plateau (its top rows faded into the tiles below by dithered alpha)."""
    w, h = size
    f = 2
    yy, xx = b15.piece_canvas(size, f)
    n = raster.fbm(w * f, h * f, 20, seed, octaves=3, period=False)
    gap = np.zeros(xx.shape, bool)
    for a, b in openings:
        gap |= (xx >= a) & (xx < b)
    wall = (yy >= wall_y) & (yy < wall_y + wall_h) & ~gap
    pylon = np.zeros(xx.shape, bool)
    for a, b in openings:
        for e, s in ((a, -1), (b, 1)):
            pylon |= (np.abs(xx - (e + s * 9)) < 9) & (np.abs(yy - (wall_y + wall_h / 2)) < wall_h / 2 + 6)
    butt = wall & (np.abs((xx % 48) - 24) < 4)
    hgt = np.where(wall, 8.0, 0.0) + np.where(butt, 1.0, 0.0)
    hgt = np.where(pylon, 12.0, hgt)
    alb = np.zeros(yy.shape + (3,))
    alb[:] = ramp(CONCRETE, np.clip(0.5 + 0.3 * (n - 0.5), 0, 1))
    alb = np.where(pylon[..., None], np.array(STEEL[2], float), alb)
    stripe = wall & (np.abs(yy - wall_y - 2) < 2) & (((xx + yy) % 12) < 6)
    alb = np.where(stripe[..., None], AMBER, alb)
    cover = (wall | pylon) * 1.0
    if plateau:
        hp, ap, py_, px_ = b17.plain_base(1981, w, h)
        top = yy < wall_y
        road = top & (np.abs(xx - w / 2) < 30)
        hgt = np.where(top, hp[(yy.astype(int)) % h, (xx.astype(int)) % w] * 0.8, hgt)
        palb = ap[(yy.astype(int)) % h, (xx.astype(int)) % w]
        palb = np.where(road[..., None], ramp(CONCRETE[:3], np.clip(0.5 + 0.3 * (n - 0.5), 0, 1)), palb)
        alb = np.where(top[..., None], palb, alb)
        fade = np.clip((yy - 4) / 40, 0, 1)
        cover = np.maximum(cover, top * fade)
    return finish_piece(hgt, alb, cover, f, colours=40)


def yard_gate():
    return wall_piece((480, 96), 26, 42, [(58, 142), (204, 276), (338, 422)], 1983)


def south_gate():
    return wall_piece((480, 480), 330, 34, [(196, 284)], 1985, plateau=True)


# =========================================================================== registry

GENERATORS = {
    "slag-field": slag_field, "hall-floor": hall_floor, "furnace-floor": furnace_floor,
    "yard-apron": yard_apron, "casting-yard": casting_yard,
    "stack-smoke": stack_smoke, "furnace-smoke": furnace_smoke,
    "belt-plates": belt_frames, "belt-end": belt_end, "smelter-tower": smelter_tower,
    "smelter-stack": smelter_stack, "slag-heap": slag_heap, "yard-gate": yard_gate, "south-gate": south_gate,
}
SECOND = {name: (lambda n=name: seam_piece(n)) for name in SEAMS}
TILE_SETS = {   # id -> (layer, drift, note)
    "slag-field": ("ground", None, "sections 1, 7: basalt slag, low heaps, rust stains, glowing cracks"),
    "hall-floor": ("ground", None, "section 2: the two roofless halls' slab floors, low walls, column stubs"),
    "furnace-floor": ("ground", None, "section 3: riveted iron plates, slag pools glowing at the edges"),
    "yard-apron": ("ground", None, "section 4: packed slag, ruts, two rail pairs under the edges"),
    "casting-yard": ("ground", None, "sections 5, 6 (the arena): cast slabs, cooling moulds, molten channels"),
    "stack-smoke": ("low-air", 6, "light: thin stack smoke, at most 28 %"),
    "furnace-smoke": ("low-air", 8, "medium (the furnace row): warmer, denser smoke, at most 36 %"),
}
PIECES = {   # id -> (layer, note, extra)
    "seam-halls": ("ground", "section 2's seam: the slag field into the halls (exact rows)", {}),
    "seam-furnaces": ("ground", "section 3's seam: the halls into the furnace row (exact rows)", {}),
    "seam-apron": ("ground", "section 4's seam: the furnace row onto the yard apron (exact rows)", {}),
    "seam-yard": ("ground", "section 5's seam: the apron into the casting yard (exact rows)", {}),
    "seam-south": ("ground", "section 7's seam: the casting yard back onto the slag field (exact rows)", {}),
    "belt-plates": ("ground", "a conveyor's moving plates, rails, rollers and low walls (40 px/s up; exact rows)",
                    {"frames": BELT_FRAMES, "fps": BELT_FPS}),
    "belt-end": ("ground", "the drum housing at a belt run's end", {}),
    "smelter-tower": ("ground", "the smelter tower from above (the crew's refuge), at the left edge", {}),
    "smelter-stack": ("ground", "a furnace stack, its throat glowing", {}),
    "slag-heap": ("ground", "a black slag heap, glowing veins at its crest", {}),
    "yard-gate": ("ground", "the casting yard's wall: the gate and the two belt slots", {}),
    "south-gate": ("ground", "the outer wall with the open south gate, the rust plateau beyond", {}),
}


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
    second = [n for n in SECOND if not wanted or n in wanted or any(w in wanted for w in SECTION_TILES_SET)]
    with ProcessPoolExecutor(max_workers=12) as pool:
        for name, img in pool.map(render_job, first):
            save(name, img)
        for name, img in pool.map(render_job, second):
            save(name, img)


SECTION_TILES_SET = {t for ts in SECTION_TILES for t in ts}


# =========================================================================== placements and the block

def image_of(name, frame=0):
    p = OUT / f"{name}.png"
    return p if p.exists() else OUT / f"{name}_{frame}.png"


def size_of(name):
    return list(Image.open(image_of(name)).size)


def belt_runs():
    """[(section, x, bottom, copies)]: each section's belts as whole 480 px copies ending 40 px below the
    next seam (the run's top), starting at least 60 px above the section's own seam."""
    runs = []
    for s, xs in ((1, (140, 340)), (2, (240,)), (3, (100, 380))):
        lo, hi = seam("ground", s) + 60, seam("ground", s + 1) - 40
        n = (hi - lo) // BELT[1]
        for x in xs:
            runs.append((s, x, hi - n * BELT[1], n))
    lo = seam("ground", 6) + 60
    hi = gate_wall_pos() + 4
    n = (hi - lo) // BELT[1]
    runs.append((6, 240, hi - n * BELT[1], n))
    return runs


def gate_top():
    """The south gate's top edge: 40 px above the view's top at the level's end."""
    return jr(top_pos(END) + 40)


def gate_wall_pos():
    """The south gate's wall foot (layer position) where the outbound belt ends under it."""
    return gate_top() - (330 + 34)


def placements():
    out = []
    for name, (s, _, _, _) in SEAMS.items():
        g = seam("ground", s)
        out.append((f"# the section seam under {name} (exact rows; re-run after the data moves a section's start)"
                    if name == "seam-halls" else "",
                    {"piece": name, "t": t_for_bottom("ground", g - SEAM_H // 2, SEAM_H), "x": 240}))
    out.append(("# 1, 7. the smelter tower at the left edge (the README's horizon tower, from above; pending round 42)",
                {"piece": "smelter-tower", "t": t_centre(top_pos(8) - 100), "x": 30}))
    out.append(("", {"piece": "smelter-tower", "t": t_centre(top_pos(154) - 100), "x": 30}))
    out.append(("# 1, 7. slag heaps", {"piece": "slag-heap", "t": t_centre(top_pos(3) - 64), "x": 390}))
    for t, x in ((14, 250), (20, 420), (149, 380), (158, 160)):
        out.append(("", {"piece": "slag-heap", "t": t_centre(top_pos(t) - 64), "x": x}))
    first = True
    for t in (66, 76, 86, 96):
        for x in (40, 440):
            out.append(("# 3. the furnace row's stacks at the edges" if first else "",
                        {"piece": "smelter-stack", "t": t_centre(top_pos(t + (5 if x > 240 else 0)) - 48), "x": x}))
            first = False
    out.append(("# 4. the yard gate: its wall enters at the top at t=115 (the turret gantry is a prop)",
                {"piece": "yard-gate", "t": t_centre(top_pos(115) - 48), "x": 240}))
    first = True
    for s, x, bottom, n in belt_runs():
        for k in range(n):
            out.append((f"# belts (exact rows, the copies join): section {s + 1} at x {x}, {n} copies" if k == 0 else "",
                        {"piece": "belt-plates", "t": t_for_bottom("ground", bottom + k * BELT[1], BELT[1]), "x": x}))
        out.append(("", {"piece": "belt-end", "t": t_centre(bottom + 12), "x": x}))
        if s != 6:
            out.append(("", {"piece": "belt-end", "t": t_centre(bottom + n * BELT[1] - 12), "x": x}))
        first = False
    out.append(("# 7. the south gate and the plateau beyond (its top 40 px above the view at the level's end)",
                {"piece": "south-gate", "t": t_centre(gate_top() - 240), "x": 240}))
    return out


def belt_times():
    """The belt runs as (x, from t, to t): the run's bottom entering at the top .. its top leaving below."""
    out = []
    for s, x, bottom, n in belt_runs():
        out.append((x, round(t_at(bottom - SCREEN), 2), round(t_at(bottom + n * BELT[1]), 2)))
    return out


def write_proposal():
    places = placements()
    flow = k8.flow
    lines = ["backdrop:",
             "  # Presentation only: Foundry of Tharsis (tools/art/backdrop_l19.py, from mars-tharsis-foundry-r36-a).",
             "  # The ground is opaque; no deep layer (the README's horizon tower is drawn from above at the left edge,",
             "  # pending round 42). Seams and belt copies sit at exact rows: re-run `backdrop_l19.py --proposal`",
             "  # after the data moves a section (the belts' times below then move too).",
             f"  scroll_factors: {flow(FACTORS)}",
             f"  ramp: {RAMP}",
             f"  haze_colour: {HAZE_COLOUR}  # the butterscotch sky (mars_r36.SKY)",
             "  atmosphere:"]
    notes = {"clear": "the south gate's run-out: haze only", "light": "slag fields, halls, approach, yard: thin stack smoke",
             "medium": "the furnace row: warmer, denser smoke"}
    for k, v in ATMOSPHERE.items():
        lines.append(f"    {k}: {flow(v)}   # {notes[k]}")
    lines.append("  tile_sets:")
    for name, (layer, drift, note) in TILE_SETS.items():
        spec = {"layer": layer, "height": TILE_H}
        if drift is not None:
            spec["drift"] = drift
        lines.append(f"    {name}: {flow(spec)}   # {note}")
    lines.append("  pieces:")
    for name, (layer, note, extra) in PIECES.items():
        lines.append(f"    {name}: {flow({'layer': layer, 'size': size_of(name), **extra})}   # {note}")
    lines.append("  placed:")
    for comment, p in places:
        if comment:
            lines.append(f"    {comment}")
        lines.append(f"    - {flow(p)}")
    lines.append("  # Section tiles (`sections[].tiles`): " + "; ".join(
        f"{i + 1}: [{', '.join(t)}]" for i, t in enumerate(SECTION_TILES)))
    lines.append("  # Atmospheres: 1, 2 light; 3 medium; 4, 5, 6 light; 7 clear.")
    lines.append("  # Belt runs drawn (x, the run's bottom enters at the top at t .. its top leaves below at t), for the")
    lines.append("  # data's Belt specs (48 px wide, 40 px/s up): " + "; ".join(f"x {x} t {a}-{b}" for x, a, b in belt_times()))
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# =========================================================================== check

_cache = {}


def cached(name):
    if name not in _cache:
        _cache[name] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    return _cache[name]


def row_change(a, i, j):
    """wrap_score's change between two rows (luminance premultiplied by alpha, the 10 % largest left out)."""
    lum = (a[..., 0] * 0.299 + a[..., 1] * 0.587 + a[..., 2] * 0.114) * a[..., 3] / 255
    d = np.sort(np.abs(lum[i] - lum[j]))
    return d[:int(len(d) * 0.9)].mean()


def check():
    problems = []
    for name in TILE_SETS:
        im = cached(name)
        if im.shape[:2] != (TILE_H, W):
            problems.append(f"{name}: {im.shape[1]}x{im.shape[0]} is not {W}x{TILE_H}")
        score = k8.wrap_score(im)
        print(f"  wrap {name}: rows {score:.2f}")
        if score > 1.5:
            problems.append(f"{name}: a line where it repeats ({score:.2f})")
    for name in PIECES:
        if name == "belt-plates":
            frames = [cached(f"belt-plates_{k}") for k in range(BELT_FRAMES)]
            for k, fr in enumerate(frames):
                # the plates repeat every 240 rows: the join (row 479 to row 0) must change as much as the same
                # rows half the piece away (239 to 240), not more (wrap_score's neighbours are mostly flat plate)
                score = abs(row_change(fr, BELT[1] - 1, 0) - row_change(fr, BELT[1] // 2 - 1, BELT[1] // 2))
                if score > 1.0 or np.abs(fr[:BELT[1] // 2] - fr[BELT[1] // 2:]).mean() > 3:
                    problems.append(f"belt-plates_{k}: its copies do not join ({score:.2f})")
                if (fr[:, 0, 3] > 0).any() or (fr[:, -1, 3] > 0).any():
                    problems.append(f"belt-plates_{k}: a side column is opaque")
            print(f"  join belt-plates: {max(abs(row_change(f, BELT[1] - 1, 0) - row_change(f, BELT[1] // 2 - 1, BELT[1] // 2)) for f in frames):.2f}")
            continue
        a = cached(name)[..., 3] > 0
        full_width = name in SEAMS or name in ("yard-gate", "south-gate")
        if a[0].any() or a[-1].any():
            problems.append(f"{name}: its top or bottom row is opaque")
        if not full_width and (a[:, 0].any() or a[:, -1].any()):
            problems.append(f"{name}: a side column is opaque")
    placed = [p for _, p in placements()]
    for name, (s, _, _, _) in SEAMS.items():
        p = next(p for p in placed if p["piece"] == name)
        if piece_bottom(p, SEAM_H) != seam("ground", s) - SEAM_H // 2:
            problems.append(f"{name} is not centred on its seam")
    for s, x, bottom, n in belt_runs():
        bottoms = sorted(piece_bottom(p, BELT[1]) for p in placed if p["piece"] == "belt-plates" and p["x"] == x
                         and bottom <= piece_bottom(p, BELT[1]) < bottom + n * BELT[1])
        if bottoms != [bottom + k * BELT[1] for k in range(n)]:
            problems.append(f"the belt at x {x} (section {s + 1}): its copies do not join")
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


def frame_at(t, placed):
    view = scroll_at(t)
    frame = np.zeros((SCREEN, W, 3))
    rows = jr(view) + (SCREEN - 1 - np.arange(SCREEN))
    for s in range(len(SECTIONS)):
        lo = -math.inf if s == 0 else seam("ground", s)
        hi = seam("ground", s + 1) if s + 1 < len(SECTIONS) else math.inf
        sel = (rows >= lo) & (rows < hi)
        if sel.any():
            img = cached(SECTION_TILES[s][0])
            frame[sel] = img[[TILE_H - 1 - (int(r) % TILE_H) for r in rows[sel]]][..., :3]
    belt = int(t * BELT_FPS) % BELT_FRAMES
    for p in placed:
        name = p["piece"] if p["piece"] != "belt-plates" else f"belt-plates_{belt}"
        img = cached(name)
        h, w = img.shape[:2]
        bottom = piece_bottom(p, h)
        top_y = SCREEN - (bottom + h - jr(view))
        if -h < top_y < SCREEN:
            over(frame, img, jr(p["x"] - w / 2), top_y)
    atm = ATMOSPHERE[atmosphere_at(t)]
    if atm.get("banks"):
        img = cached(atm["banks"])
        lr = jr(view * FACTORS["low-air"]) + (SCREEN - 1 - np.arange(SCREEN))
        bank = np.roll(img[[TILE_H - 1 - (int(r) % TILE_H) for r in lr]], int(TILE_SETS[atm["banks"]][1] * t) % W, 1)
        over(frame, bank, 0, 0)
    return np.clip(frame, 0, 255).astype(np.uint8)


def review():
    placed = [p for _, p in placements()]
    times = [8, 22, 34, 60, 70, 92, 112, 118, 132.5, 145, 158, 171]
    scale = 0.5
    cw, ch = int(W * scale), int(SCREEN * scale)
    cols = 6
    sheet = raster.sheet(16 + cols * (cw + 10), 60 + 2 * (ch + 24) + 280,
                         "LEVEL 19 FOUNDRY OF THARSIS - BACKDROP (FINAL R42)", "PRODUCTION ART, M7 PART F - R42")
    raster.draw_text(sheet, 16, 38, "THE PLAY FIELD (GROUND AND LOW-AIR SMOKE) AT 0.5X AT THESE TIMES; NO UNITS OR PROPS",
                     raster.LABEL)
    for i, t in enumerate(times):
        fr = Image.fromarray(frame_at(t, placed), "RGB")
        x, y = 16 + (i % cols) * (cw + 10), 54 + (i // cols) * (ch + 24)
        sheet.paste(fr.resize((cw, ch), Image.BOX), (x, y))
        sec = next(s["name"] for s in SECTIONS if t < s["end"]) if t < END else SECTIONS[-1]["name"]
        raster.draw_text(sheet, x, y + ch + 4, f"T={t}  {sec.upper()}", raster.LABEL_DIM)
    y = 54 + 2 * (ch + 24) + 6
    raster.draw_text(sheet, 16, y, "PIECES AT 1X (BELT FRAMES 0-3) AND 0.5X (ON DARK)", raster.LABEL)
    x = 16
    for k in range(BELT_FRAMES):
        img = Image.open(OUT / f"belt-plates_{k}.png").convert("RGBA").crop((0, 0, 80, 240))
        sheet.alpha_composite(img, (x, y + 16))
        x += 86
    raster.draw_text(sheet, 16, y + 16 + 242, "BELT-PLATES 0-3", raster.LABEL_DIM)
    x += 10
    for name in ("belt-end", "smelter-tower", "smelter-stack", "slag-heap", "yard-gate", "south-gate"):
        img = Image.open(OUT / f"{name}.png").convert("RGBA")
        if name not in ("belt-end", "smelter-stack"):
            img = img.resize((max(1, img.width // 2), max(1, img.height // 2)), Image.BOX)
        if x + img.width > sheet.width - 16:
            break
        sheet.alpha_composite(img, (x, y + 16))
        raster.draw_text(sheet, x, y + 16 + img.height + 2, name.upper(), raster.LABEL_DIM)
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
