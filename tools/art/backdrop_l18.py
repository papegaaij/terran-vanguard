#!/usr/bin/env python3
"""Production art: the Level 18 backdrop, Dust Storm (design/campaign/act-3-red-dust/level-18-dust-storm;
M7 part E batch, concept round 41, step ART-E).

Straight to production from the storm-plains mood still `mars-storm-plains-r36-a` and the chosen radius look
b (MA.2, `storm-radius-r36-b`): tools/concept/mars_r36.py imported unchanged (the plain's ramps, the dust
ramp, the veil colours of `Storm.veil` and the falloff of `Storm.vis`). Lit from the upper left, receding
(rule 3), posterized per image, wide gradients ordered-dithered. Nothing lit is mirrored.

The ground is opaque everywhere (no `deep` layer: the README's horizon, the storm wall standing on the plain
and the domes on the horizon cannot show under a top-down ground; the wall is drawn as the high-air front
passing over the screen and the domes as Ares Landing's outskirts ahead at the level's end: pending the
user's review in round 41). Layers: ground (x1), low-air (x1.35), high-air (x2.0).

Tile sets (480 wide, 960 tall, wrapping): ground `north-plain` (sections 1, 2, 6: the rust flats, dry
channels, wind ripples, small craters), `storm-plain` (sections 3, 5: the plain under drifted dust sheets,
lit flat by the storm's diffuse light), `dry-lake` (section 4, the eye: a pale cracked lake bed with salt
crusts); low-air `dust-wisps` (light, at most 30 %) and `dust-banks` (heavy, at most 38 %); high-air
`dust-streaks` (the atmosphere's `wisps`: thin streaks driven across the screen, at most 40 %).

Pieces (ground unless noted): `seam-storm`, `seam-lake-in`, `seam-lake-out`, `seam-clearing` (480x320,
exact rows: the section seams); `pipeline-a`, `pipeline-b` (480x200: the half-buried terraforming pipeline
crossing the field on its supports, dust drifted over it in places, a junction box); `pipeline-ridge`
(280x240: the pipeline along a low ridge, where the three-turret nests stand, t~69, t~88 and t~160; the
turret pairs at t~8, t~32 and t~149 stand on `pipeline-a`, fitted so the pipe runs under both);
`dry-channel` (140x360: a wider dry channel, the Polyp Mortar pair's at t~47); `vrell-growth` (160x144,
under the deep storm's mortar pair and the eye's mortar floe); `ares-outskirts` (480x480: Ares Landing's edge ahead at the level's end: the
plain road and the rail line running north into it, greenhouse tubes and three small outer domes); low-air
`eye-wall-left`, `eye-wall-right` (120x960, tiling along themselves: the storm's walls standing round the
eye's clear field); high-air `storm-front` (480x420: the front's billowing edge passing over the screen
from t~28, at most 50 %) and `storm-back-wall` (480x480: the denser back wall at t~130, at most 60 %).

StormLooks textures (same folder; the game's visibility pass reads them, not the backdrop block):
`storm-veil` (480x960, opaque, wrapping both ways: the rust veil, field a of look b through the veil ramp,
banded every 6 levels like a 90s translucency table), `storm-veil-drift` (480x960, wrapping: field b as a
lighter layer, at most 45 %, 4 steps; drifts sideways over the veil), `storm-tongues-a`, `storm-tongues-b`
(256x256, wrapping both ways: white, the noise value 0..1 in the alpha, the two drifting fields that break
the falloff: visibility = smoothstep(R + 55, R - 65, d + 70 (a - 0.5) + 40 (b - 0.5)), d the distance to the
ship, R the radius).

Outputs: assets/backdrop/level-18/*.png; the review sheet and GIF
design/campaign/act-3-red-dust/level-18-dust-storm/concept/backdrop-final-r41-a.png/.gif (the play field
with the storm look b drawn over it at the README's radius per time, a few stand-in units from
tools/art/storm_units.py's sprites); `--proposal` writes the level's `backdrop:` block to
briefs/m7e-backdrop.yaml (for the main agent to merge; never into the level's data.yaml).

Reads the level's data.yaml when it exists (sections with speed and `ramp`, the ground targets), otherwise
the README's values. Run: python3 tools/art/backdrop_l18.py [--proposal | --review] [id ...] (all: ~2 min;
after storm_units.py, whose sprites the review shows)
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image, ImageDraw

import artkit
from artkit import DESIGN, ROOT, write_gif

import backdrop_l08 as k8  # noqa: E402  (tools/art: relief lighting, reduce, quantize, YAML flow)
import backdrop_l15 as b15  # noqa: E402  (tools/art: periodic noise, lighting, posterizing, banks)
import backdrop_l17 as b17  # noqa: E402  (tools/art: the Mars plain base, piece finishing, Vrell growth)
import mars_r36 as m36  # noqa: E402  (concept script, unchanged: the Mars ramps, the storm look b)
from render import raster, sdf  # noqa: E402

SCRIPT = "backdrop_l18.py"
BATCH = "M7 part E batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
LEVEL_DIR = DESIGN / "campaign" / "act-3-red-dust" / "level-18-dust-storm"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = ROOT / "briefs" / "m7e-backdrop.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-18"
REVIEW = CONCEPT / "backdrop-final-r41-a.png"
W, SCREEN = 480, 540
MID = SCREEN / 2
TILE_H = 960
FACTORS = {"ground": 1.0, "low-air": 1.35, "high-air": 2.0}
HAZE_COLOUR = "d6a670"                  # mars_r36.SKY: the butterscotch sky
RAMP = 3
ATMOSPHERE = {
    "clear": {"haze": 0.04},
    "light": {"banks": "dust-wisps", "haze": 0.08},
    "medium": {"banks": "dust-wisps", "wisps": "dust-streaks", "haze": 0.12},
    "heavy": {"banks": "dust-banks", "wisps": "dust-streaks", "haze": 0.16},
}
DUST = m36.DUST
TEAL = np.array((40, 200, 150), float)
VEIL_RAMP = [(0, (138, 84, 50)), (0.5, (176, 118, 74)), (1, (214, 162, 110))]   # mars_r36.Storm.veil
TONGUE = 256

pn, aniso, sample, smooth, ramp = b15.pn, b15.aniso, b15.sample, b15.smooth, b15.ramp
lit, recede, posterize_rgb, dither_alpha = b15.lit, b15.recede, b15.posterize_rgb, b15.dither_alpha
finish, finish_piece = b17.finish, b17.finish_piece
jr = k8.java_round


# =========================================================================== the level's timeline

DEFAULT_SECTIONS = [   # the README's Layout until the level data exists (2 s eases)
    {"name": "Open Plains", "end": 25, "speed": 150, "atmosphere": "clear",
     "peak": {"atmosphere": "medium", "from": 20, "to": 25}},
    {"name": "Storm Front", "end": 55, "speed": 140, "ramp": 2, "atmosphere": "medium"},
    {"name": "Deep Storm", "end": 110, "speed": 120, "ramp": 2, "atmosphere": "heavy"},
    {"name": "The Eye", "end": 130, "speed": 120, "atmosphere": "light"},
    {"name": "Back Wall", "end": 170, "speed": 120, "atmosphere": "heavy"},
    {"name": "Clearing", "end": 183, "speed": 150, "ramp": 2, "atmosphere": "light"},
]
SECTION_TILES = [["north-plain"], ["north-plain"], ["storm-plain"], ["dry-lake"], ["storm-plain"], ["north-plain"]]
# the README's visibility (medium): (t0, t1, r0, r1) eased, the radius in px, None = full
VISIBILITY = [(0, 28, None, None), (28, 36, None, 170), (36, 110, 170, 170), (110, 114, 170, None),
              (114, 130, None, None), (130, 134, None, 150), (134, 170, 150, 150), (170, 176, 150, None),
              (176, 999, None, None)]
FULL = 700.0


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
    """LevelData.scrollAt with the M7 part B ease (as backdrop_l17.scroll_at)."""
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
    """The placement time of a piece centred on layer position ``pos``."""
    return round(t_at((pos - MID) / FACTORS[layer]), 3)


def top_pos(t, layer="ground"):
    """The layer position at the screen's top edge at time t."""
    return scroll_at(t) * FACTORS[layer] + SCREEN


def targets():
    """{name: [(t, x), ...]} of the data's ground targets (the target's top edge at the top edge at t), else the
    README's ground targets."""
    out = {}
    for g in DATA.get("ground_targets") or []:
        out[g.get("name") or g["target"]] = [tuple(map(float, a)) for a in g["at"]]
    return out or {"channel mortars": [(47, 120), (47.4, 160)], "ridge nest": [(88, 300), (88.4, 340), (88.8, 380)],
                   "rover wreck": [(90, 60)], "lake floe": [(125, 200), (125.4, 250), (125.8, 225)],
                   "wall nest": [(160, 140), (160.4, 180), (160.8, 220)]}


TURRET = 40                             # a nest unit's footprint (Spine Turret, Polyp Mortar)


def target_centre(name, size=32):
    spots = targets()[name]
    pos = sum(top_pos(t) - size / 2 for t, _ in spots) / len(spots)
    x = sum(x for _, x in spots) / len(spots)
    return pos, x


def radius_at(t):
    """The README's visibility radius at medium (px; FULL when the view is clear), eased over the ramps."""
    for t0, t1, r0, r1 in VISIBILITY:
        if t0 <= t < t1:
            a = FULL if r0 is None else r0
            b = FULL if r1 is None else r1
            u = smooth(0.0, 1.0, (t - t0) / max(t1 - t0, 1e-6)) if a != b else 0.0
            return a + (b - a) * float(u)
    return FULL


# =========================================================================== tile sets

def channels(hgt, alb, yy, xx, seed, n=3, depth=1.6, width=(5, 9)):
    """Dry channels: periodic meanders cut into the plain, a pale silt floor, the banks' shadow by the relief."""
    rng = np.random.default_rng(seed)
    for path in b15.periodic_paths(TILE_H, n, seed, 40, 440, 90, cycles=(1, 2, 3)):
        wd = rng.uniform(*width)
        d = np.abs(xx - path[yy.astype(int) % TILE_H])
        cut = np.clip(1 - d / wd, 0, 1) ** 0.6
        hgt -= depth * 2.2 * cut
        floor = np.clip(1 - d / (wd * 0.55), 0, 1)
        alb = alb * (1 - 0.35 * floor[..., None]) + np.array(m36.OCHRE[3], float) * 0.35 * floor[..., None]
    return hgt, alb


def north_plain():
    """Sections 1, 2 and 6: the northern plain's rust flats: wind ripples, dust drifts, small craters, dry
    channels meandering across, scattered rocks."""
    hgt, alb, yy, xx = b17.plain_base(1801)
    hgt, alb = channels(hgt, alb, yy, xx, 1803)
    rocks = (np.random.default_rng(1802).random(hgt.shape) > 0.995) * 2.2
    rocks = np.maximum(rocks, np.roll(rocks, 1, 1) * 0.7)
    return finish(lit(hgt + rocks, alb), 32)


def storm_plain():
    """Sections 3 and 5: the same plain under the storm: broad drifted dust sheets streaked along the wind
    (pale ochre over the rust), the channels half filled, lit flat by the storm's diffuse light."""
    hgt, alb, yy, xx = b17.plain_base(1811)
    hgt, alb = channels(hgt, alb, yy, xx, 1813, n=2, depth=0.9)
    sheet = smooth(0.52, 0.74, aniso(W, TILE_H, 1, 4, 24, 1815, 4))
    streak = aniso(W, TILE_H, 4, 1, 12, 1817, 3)
    hgt = hgt * (1 - 0.5 * sheet) + 1.2 * sheet + 0.5 * streak * sheet
    dust = np.array(m36.DUST[2], float)
    alb = alb * (1 - 0.35 * sheet[..., None]) + dust * 0.35 * sheet[..., None]
    rgb = lit(hgt, alb, ambient=0.48, key=0.5) * 0.92
    # Rolled (still wrapping) so a calm band of the sheets meets at the repeat: the busier band left there scored
    # 1.62 in BackdropSeamsTest's wrap check (limit 1.5), a false seam (step E4).
    return finish(np.roll(rgb, 672, 0), 32)


def dry_lake():
    """Section 4, the eye: a dry lake bed, pale ochre clay cracked into polygons (periodic Voronoi), white
    salt crusts, a few drift ridges, flat under the clear sky."""
    rng = np.random.default_rng(1821)
    yy, xx = np.mgrid[0:TILE_H, 0:W].astype(np.float64)
    pts = np.stack([rng.uniform(0, W, 520), rng.uniform(0, TILE_H, 520)], -1)
    d1 = np.full(xx.shape, 1e9)
    d2 = np.full(xx.shape, 1e9)
    for px, py in pts:
        dx = np.abs(xx - px)
        dx = np.minimum(dx, W - dx)
        dy = np.abs(yy - py)
        dy = np.minimum(dy, TILE_H - dy)
        d = np.hypot(dx, dy * 1.15)
        d2 = np.where(d < d1, d1, np.minimum(d2, d))
        d1 = np.minimum(d1, d)
    crack = np.clip(1 - (d2 - d1) / 1.6, 0, 1)
    n = pn(W, TILE_H, 120, 1823, 3)
    salt = smooth(0.62, 0.72, pn(W, TILE_H, 80, 1825, 3))
    tone = np.clip(0.55 + 0.3 * (n - 0.5) + 0.12 * np.clip(d1 / 14, 0, 1), 0, 1)
    alb = ramp([(112, 74, 46), (156, 112, 74), (190, 146, 104), (214, 178, 136)], tone)
    alb = alb * (1 - 0.45 * salt[..., None]) + np.array((226, 214, 196), float) * 0.45 * salt[..., None]
    alb = alb * (1 - 0.38 * crack[..., None]) * 0.88
    hgt = 0.6 * n - 1.1 * crack + 0.25 * np.clip(d1 / 10, 0, 1)
    return finish(lit(hgt, alb, ambient=0.5, key=0.6), 32, amount=0.16)


def dust_wisps():
    return b15.cloud_bank(W, TILE_H, 1831, 0.68, 0.14, 76, colours=DUST[1:])


def dust_banks():
    return b15.cloud_bank(W, TILE_H, 1833, 0.58, 0.2, 96, colours=DUST[1:])


def dust_streaks():
    """Thin streaks of driven dust, slanted with the wind (from the upper left), wrapping both ways; the
    brightest at 40 % (alpha 102), most far fainter."""
    S = 2
    img = Image.new("RGBA", (W * S, TILE_H * S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(1841)
    for _ in range(150):
        x, y, b = rng.uniform(0, W), rng.uniform(0, TILE_H), rng.uniform(0.25, 1.0)
        length = 18 + 30 * b
        for ox in (-W, 0, W):
            for oy in (-TILE_H, 0, TILE_H):
                x0, y0 = (x + ox) * S, (y + oy) * S
                d.line([x0, y0, x0 + 0.38 * length * S, y0 + length * S],
                       fill=tuple(DUST[4]) + (int(30 + 72 * b),), width=S)
    a = np.array(img.resize((W, TILE_H), Image.BOX)).astype(np.float64)
    alpha = a[..., 3] / 255
    q = np.where(alpha < 0.06, 0, np.ceil(alpha * 6 - 0.5) / 6)
    q = np.minimum(q, 0.4)
    out = np.dstack([np.full((TILE_H, W, 3), DUST[4], float), q * 255]).astype(np.uint8)
    out[q == 0] = 0
    return Image.fromarray(out, "RGBA")


# =========================================================================== the storm look's textures

def veil_field(seed, h=TILE_H):
    return pn(W, h, 60, seed, 3)


def storm_veil():
    """Field a through the veil ramp, banded every 6 levels (mars_r36.Storm.veil), opaque, wrapping."""
    col = raster.ramp(VEIL_RAMP, veil_field(1851))
    return raster.to_rgba_image(np.round(col / 6) * 6)


def storm_veil_drift():
    """Field b as a lighter layer over the veil (at most 45 %, 4 steps), drifting sideways."""
    n = veil_field(1853)
    alpha = np.clip((n - 0.45) / 0.4, 0, 1) * 0.45
    q = np.where(alpha < 0.06, 0, np.ceil(alpha / 0.45 * 4 - 0.5) / 4 * 0.45)
    rgb = np.broadcast_to(np.array(VEIL_RAMP[2][1], float), (TILE_H, W, 3))
    out = np.dstack([rgb, q * 255]).astype(np.uint8)
    out[q == 0] = 0
    return Image.fromarray(out, "RGBA")


def tongues(seed):
    """A wrapping noise field for the falloff's dust tongues: white, the value in the alpha (0..255)."""
    n = raster.fbm(TONGUE, TONGUE, 64, seed, octaves=3, period=True)
    n = (n - n.min()) / max(n.max() - n.min(), 1e-6)
    out = np.dstack([np.full((TONGUE, TONGUE, 3), 255.0), n * 255]).astype(np.uint8)
    return Image.fromarray(out, "RGBA")


# =========================================================================== seams

FEATHER = 32
SEAM_H = 320
SEAMS = {"seam-storm": (2, "north-plain", "storm-plain", 1861), "seam-lake-in": (3, "storm-plain", "dry-lake", 1863),
         "seam-lake-out": (4, "dry-lake", "storm-plain", 1865), "seam-clearing": (5, "storm-plain", "north-plain", 1867)}


def tile_strip(name, bottom, h):
    img = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    hgt = img.shape[0]
    pos = bottom + h - 1 - np.arange(h)
    return img[[hgt - 1 - (int(p) % hgt) for p in pos]]


def seam_piece(name):
    """backdrop_l17.seam_piece on this level's tile sets: the lower section's tiles below a ragged boundary at
    the seam, the upper's above, the ends faded into the tile sets (ordered-dithered 1-bit alpha)."""
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
    out[..., :3] = np.where(r[..., None], out[..., :3] * 0.8, out[..., :3])
    k = np.arange(h)[:, None]
    a = np.minimum(np.clip((k + 0.5) / FEATHER, 0, 1), np.clip((h - k - 0.5) / FEATHER, 0, 1))
    alpha = dither_alpha(np.broadcast_to(a, (h, W)), 2)
    out[..., 3] = np.where(alpha > 0, 255, 0)
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out.astype(np.uint8), "RGBA")


# =========================================================================== pieces

PIPE_STEEL = np.array((150, 140, 132), float)


def pipe_field(yy, xx, line, radius=3.6, support_every=38):
    """A half-buried pipe along ``line`` (x(t), y(t) samples): its height profile, its cover and the
    supports' mask; drifts bury it where the noise says."""
    pts = np.array(line, float)
    d = np.full(xx.shape, 1e9)
    along = np.zeros(xx.shape)
    total = 0.0
    for (x0, y0), (x1, y1) in zip(pts[:-1], pts[1:]):
        bx, by = x1 - x0, y1 - y0
        L2 = bx * bx + by * by
        h = np.clip(((xx - x0) * bx + (yy - y0) * by) / L2, 0, 1)
        dd = np.hypot(xx - x0 - h * bx, yy - y0 - h * by)
        along = np.where(dd < d, total + h * math.sqrt(L2), along)
        d = np.minimum(d, dd)
        total += math.sqrt(L2)
    tube = np.sqrt(np.clip(radius ** 2 - d ** 2, 0, None))
    support = (d < radius + 2.2) & ((along % support_every) < 3)
    return tube, d, support


PIPE = (480, 200)


def pipe_line(direction, w=PIPE[0]):
    """The pipeline piece's centre line (piece px, y down): rising to the right (a) or falling (b)."""
    y0, y1 = (150, 50) if direction > 0 else (55, 150)
    return [(-10, y0 + 5), (w * 0.35, y0 + (y1 - y0) * 0.33), (w * 0.7, y0 + (y1 - y0) * 0.72), (w + 10, y1 - 4)]


def pipe_y(direction, x):
    pts = np.array(pipe_line(direction), float)
    return float(np.interp(x, pts[:, 0], pts[:, 1]))


def pipeline(direction=1, seed=1871, size=PIPE):
    """A half-buried terraforming pipeline crossing the field on low supports, a junction box, dust drifted
    over it in places (cover fades into the plain at the drifts and the piece's ends)."""
    w, h = size
    f = 2
    yy, xx = b15.piece_canvas(size, f)
    y0, y1 = (150, 50) if direction > 0 else (55, 150)
    line = pipe_line(direction, w)
    tube, d, support = pipe_field(yy, xx, line)
    n = raster.fbm(w * f, h * f, 24, seed, octaves=3, period=False)
    buried = smooth(0.52, 0.62, n)
    hgt = tube * 0.8 * (1 - buried) + 1.5 * support + 1.2 * buried * np.exp(-(d / 8) ** 2)
    jx, jy = w * 0.55, y0 + (y1 - y0) * 0.55
    box = (np.abs(xx - jx) < 9) & (np.abs(yy - jy) < 7)
    hgt = np.where(box, 5.0, hgt)
    bands = ((np.abs(xx * 0.7 + yy * 0.3) % 22) < 2) & (tube > 0)
    alb = np.broadcast_to(PIPE_STEEL, yy.shape + (3,)).copy()
    alb = np.where(bands[..., None], PIPE_STEEL * 0.7, alb)
    rust = smooth(0.55, 0.75, raster.fbm(w * f, h * f, 6, seed + 1, octaves=2, period=False))
    alb = alb * (1 - 0.4 * rust[..., None]) + np.array((120, 60, 36), float) * 0.4 * rust[..., None]
    alb = np.where(support[..., None], np.array((96, 88, 82), float), alb)
    alb = np.where(box[..., None], np.array((118, 112, 104), float), alb)
    dust = np.array(m36.PLAIN[3], float)
    mound = buried * np.exp(-(d / 8) ** 2)
    alb = alb * (1 - mound[..., None]) + dust * mound[..., None]
    shown = buried < 0.5
    cover = np.maximum.reduce([((tube > 0.4) & shown) * 1.0, (support & shown) * 1.0, box * 1.0,
                               smooth(0.3, 0.6, mound)])
    return finish_piece(hgt, alb, cover, f, colours=24)


def pipeline_ridge():
    """The pipeline along a low ridge (the spine-turret nests' ground): a long rust mound, its crest
    flattened, the pipe on it, rock showing on the lit flank."""
    w, h = 280, 240
    f = 2
    yy, xx = b15.piece_canvas((w, h), f)
    n = raster.fbm(w * f, h * f, 30, 1881, octaves=4, period=False)
    ridge = np.clip(1 - np.hypot((xx - w / 2) / (w * 0.46), (yy - h / 2) / (h * 0.42)) + (n - 0.5) * 0.5, 0, 1)
    tube, d, support = pipe_field(yy, xx, [(-10, h * 0.52), (w * 0.5, h * 0.45), (w + 10, h * 0.5)], radius=3.4)
    hgt = 7 * smooth(0.0, 0.7, ridge) + tube * 0.9 + 1.4 * support * (ridge > 0.2)
    rock = smooth(0.6, 0.7, raster.fbm(w * f, h * f, 8, 1883, octaves=2, period=False)) * (ridge > 0.3)
    alb = ramp(m36.PLAIN, np.clip(0.45 + 0.3 * (n - 0.5) + 0.2 * ridge, 0, 1))
    alb = alb * (1 - 0.5 * rock[..., None]) + np.array(m36.BASALT[3], float) * 0.5 * rock[..., None]
    alb = np.where((tube > 0.3)[..., None], PIPE_STEEL, alb)
    alb = np.where(support[..., None], np.array((96, 88, 82), float), alb)
    cover = smooth(0.02, 0.2, ridge)
    return finish_piece(hgt, alb, cover, f, colours=28)


def dry_channel():
    """A wider dry channel meandering down the field: steep banks, a sandy silt floor with ripples, pebbles."""
    w, h = 140, 360
    f = 2
    yy, xx = b15.piece_canvas((w, h), f)
    cx = w / 2 + 22 * np.sin(yy / h * np.pi * 1.6 + 0.4)
    d = np.abs(xx - cx)
    bed = np.clip(1 - d / 26, 0, 1)
    bank = np.clip(1 - np.abs(d - 26) / 6, 0, 1)
    rip = np.sin((yy + 0.3 * xx) / 3.2) * 0.25 * bed
    hgt = -5 * smooth(0.0, 0.5, bed) + 1.2 * bank + rip
    alb = ramp(m36.PLAIN, np.clip(0.55 - 0.15 * bank + 0.0 * bed, 0, 1))
    alb = alb * (1 - 0.5 * bed[..., None]) + np.array(m36.OCHRE[3], float) * 0.5 * bed[..., None]
    fade = np.minimum(np.clip(yy / 30, 0, 1), np.clip((h - yy) / 30, 0, 1))
    cover = np.clip(np.maximum(bed, bank * 0.9) * 1.6, 0, 1) * fade
    return finish_piece(hgt, alb, cover, f, colours=24)


def ares_outskirts():
    """Ares Landing's edge ahead at the level's end: the plain road and the rail line running north into it,
    three greenhouse tubes, and three small outer domes (mars_r36.dome_sprite a, the chosen dome look, at
    72 px without annexes) on their pads at the top: the colony on the horizon."""
    w, h = 480, 480
    hgt, alb, yy, xx = b17.plain_base(1891, w=w, h=h)
    road = np.abs(xx - (180 + 0.12 * (h - yy))) < 6
    rail = (np.abs(xx - 330) < 1.2) | (np.abs(xx - 338) < 1.2)
    bed = np.abs(xx - 334) < 9
    sleepers = bed & ((yy % 7) < 2) & (np.abs(xx - 334) < 7)
    hgt = np.where(road, hgt * 0.2 + 0.4, hgt)
    hgt = np.where(bed, hgt * 0.3 + 0.8, hgt) + 0.5 * rail
    alb = np.where(road[..., None], np.array((150, 128, 104), float), alb)
    alb = np.where(bed[..., None], np.array((104, 92, 84), float), alb)
    alb = np.where(sleepers[..., None], np.array((80, 64, 54), float), alb)
    alb = np.where(rail[..., None], np.array((176, 170, 164), float), alb)
    pads = [(110, 70), (250, 56), (410, 84)]
    for px, py in pads:
        r = np.hypot(xx - px, yy - py)
        alb = np.where((r < 46)[..., None], np.array((150, 140, 126), float), alb)
        hgt = np.where(r < 46, 0.6, hgt)
    img = finish(lit(hgt, alb, wrap=False), 32)
    fade = np.minimum(np.clip(yy / 40, 0, 1), np.clip((h - yy) / 40, 0, 1))
    a = np.array(img)
    a[..., 3] = np.where(dither_alpha(fade, 2) > 0, 255, 0)
    img = Image.fromarray(a, "RGBA")
    for gx, gy in ((40, 190), (64, 186), (88, 192)):
        img.alpha_composite(m36.greenhouse(56, 8).rotate(90, expand=True), (gx - 9, gy - 28))
    for (px, py) in pads:
        dome = m36.dome_sprite("a", 72, annex=False)
        img.alpha_composite(dome, (int(px - dome.width / 2), int(py - dome.height / 2)))
    out = np.array(img)
    out[out[..., 3] == 0] = 0
    return k8.quantize_lit([Image.fromarray(out, "RGBA")], 40)[0]


EYE_W = 120


def eye_wall(side):
    """The storm's wall standing round the eye's clear field, on low-air: billowing dust dense at the screen
    edge (at most 70 %), thinning inward over 120 px, lit from the upper left; wraps along itself (960)."""
    h = TILE_H
    n = pn(EYE_W * 4, h, 60, 1895 + (side == "right"), 3)[:, :EYE_W]
    fine = pn(EYE_W * 4, h, 12, 1897 + (side == "right"), 3)[:, :EYE_W]
    x = np.arange(EYE_W)[None, :] / EYE_W
    depth = (1 - x) if side == "left" else x
    body = np.clip(depth * 1.5 - 0.35 + (n - 0.5) * 0.9, 0, 1)
    light = np.clip(0.5 + 6 * (body - np.roll(np.roll(body, 2, 0), 2, 1)) + 0.3 * (fine - 0.5), 0, 1)
    shades = np.array([DUST[1], DUST[2], DUST[3], DUST[4]], float)
    rgb = shades[np.clip(light * 3.99, 0, 3).astype(int)]
    alpha = np.where(body < 0.08, 0, np.ceil(np.clip(body, 0, 1) * 0.7 * 5 - 0.5) / 5)
    out = np.dstack([rgb, alpha * 255]).astype(np.uint8)
    out[alpha == 0] = 0
    return artkit.quantize_set([Image.fromarray(out, "RGBA")], 12)[0]


def front(size, seed, peak, dark=0.0):
    """A dust front passing over the screen (high-air): its leading (bottom) edge billowing and lit, the dust
    thickest a third of the way up, thinning out above to nothing at its top row; ``peak`` the most opaque
    (stepped)."""
    w, h = size
    n = raster.fbm(w, h, 48, seed, octaves=4, period=False)
    fine = raster.fbm(w, h, 10, seed + 1, octaves=2, period=False)
    yy = np.arange(h)[:, None] / h
    edge = 0.86 + 0.08 * np.sin(np.arange(w)[None, :] / w * TAU_ * 2.3 + 1.1) + (n - 0.5) * 0.18
    body = np.clip((edge - yy) / 0.12, 0, 1) * np.clip(yy / 0.6 + 0.15 + (n - 0.5) * 0.4, 0, 1)
    # Round 41 F.c: the trailing (top) edge thins out to nothing, broken by the noise, so the piece's top row
    # does not end the dust in a straight line as it passes over the screen.
    top = np.clip((yy - 0.04 + (n - 0.5) * 0.1) / 0.3, 0, 1)
    body = body * top * top * (3 - 2 * top)
    light = np.clip(0.45 + 5 * (body - np.roll(np.roll(body, 3, 0), 3, 1)) + 0.25 * (fine - 0.5), 0, 1)
    shades = np.array([DUST[2], DUST[3], DUST[4], (238, 200, 150)], float) * (1 - dark)
    rgb = shades[np.clip(light * 3.99, 0, 3).astype(int)]
    a = body * peak
    alpha = np.where(a < 0.05, 0, np.ceil(a / peak * 5 - 0.5) / 5 * peak)
    out = np.dstack([rgb, alpha * 255]).astype(np.uint8)
    out[alpha == 0] = 0
    return artkit.quantize_set([Image.fromarray(out, "RGBA")], 12)[0]


TAU_ = 2 * np.pi


# =========================================================================== registry

GENERATORS = {
    "north-plain": north_plain, "storm-plain": storm_plain, "dry-lake": dry_lake,
    "dust-wisps": dust_wisps, "dust-banks": dust_banks, "dust-streaks": dust_streaks,
    "pipeline-a": lambda: pipeline(1, 1871), "pipeline-b": lambda: pipeline(-1, 1873),
    "pipeline-ridge": pipeline_ridge, "dry-channel": dry_channel,
    "vrell-growth": lambda: b17.vrell_growth((160, 144), 1885, 0.5),
    "ares-outskirts": ares_outskirts,
    "eye-wall-left": lambda: eye_wall("left"), "eye-wall-right": lambda: eye_wall("right"),
    "storm-front": lambda: front((480, 420), 1901, 0.5),
    "storm-back-wall": lambda: front((480, 480), 1903, 0.6, dark=0.12),
    "storm-veil": storm_veil, "storm-veil-drift": storm_veil_drift,
    "storm-tongues-a": lambda: tongues(1905), "storm-tongues-b": lambda: tongues(1907),
}
SECOND = {name: (lambda n=name: seam_piece(n)) for name in SEAMS}
TILE_SETS = {   # id -> (layer, drift, note)
    "north-plain": ("ground", None, "sections 1, 2, 6: the northern plain's rust flats, dry channels, wind ripples"),
    "storm-plain": ("ground", None, "sections 3, 5: the plain under drifted dust sheets, lit flat by the storm"),
    "dry-lake": ("ground", None, "section 4, the eye: a pale cracked lake bed, salt crusts"),
    "dust-wisps": ("low-air", 6, "light and medium: thin dust wisps, at most 30 %"),
    "dust-banks": ("low-air", 10, "heavy: dust banks, at most 38 %"),
    "dust-streaks": ("high-air", 24, "medium and heavy (the atmosphere's wisps): driven dust streaks, at most 40 %"),
}
PIECES = {   # id -> (layer, note)
    "seam-storm": ("ground", "section 3's seam: the plain into the storm plain (exact rows)"),
    "seam-lake-in": ("ground", "section 4's seam: the storm plain onto the dry lake (exact rows)"),
    "seam-lake-out": ("ground", "section 5's seam: the dry lake back into the storm plain (exact rows)"),
    "seam-clearing": ("ground", "section 6's seam: the storm plain into the clear plain (exact rows)"),
    "pipeline-a": ("ground", "the half-buried terraforming pipeline crossing the field, rising to the right"),
    "pipeline-b": ("ground", "the same pipeline falling to the right (its own lighting)"),
    "pipeline-ridge": ("ground", "the pipeline along a low ridge: the spine-turret nests stand on it"),
    "dry-channel": ("ground", "a wider dry channel: the Polyp Mortar pair's"),
    "vrell-growth": ("ground", "Vrell nest growth, violet with teal veins (under the eye's mortar floe)"),
    "ares-outskirts": ("ground", "Ares Landing's edge ahead: road, rail line, greenhouses, three outer domes"),
    "eye-wall-left": ("low-air", "the storm's wall round the eye, left edge (tiles along itself, exact rows)"),
    "eye-wall-right": ("low-air", "the storm's wall round the eye, right edge (tiles along itself, exact rows)"),
    "storm-front": ("high-air", "the front's billowing edge passing over the screen, at most 50 %"),
    "storm-back-wall": ("high-air", "the back wall, denser and darker, at most 60 %"),
}
TEXTURES = ("storm-veil", "storm-veil-drift", "storm-tongues-a", "storm-tongues-b")


def render_job(name):
    fn = GENERATORS.get(name) or SECOND[name]
    return name, fn()


def save(name, img):
    OUT.mkdir(parents=True, exist_ok=True)
    artkit.save_png(img, OUT / f"{name}.png", SOURCE)
    print(f"  {name}: {img.size[0]}x{img.size[1]}")


def write_all(wanted):
    first = [n for n in GENERATORS if not wanted or n in wanted]
    second = [n for n in SECOND if not wanted or n in wanted
              or any(w in wanted for w in ("north-plain", "storm-plain", "dry-lake"))]
    with ProcessPoolExecutor(max_workers=12) as pool:
        for name, img in pool.map(render_job, first):
            save(name, img)
        for name, img in pool.map(render_job, second):
            save(name, img)


# =========================================================================== placements and the block

def size_of(name):
    return list(Image.open(OUT / f"{name}.png").size)


def placements():
    out = []
    for name, (s, _, _, _) in SEAMS.items():
        g = seam("ground", s)
        out.append((f"# the section seam under {name} (exact rows; re-run after the data moves a section's start)",
                    {"piece": name, "t": t_for_bottom("ground", g - SEAM_H // 2, SEAM_H), "x": 240}))
    tg = targets()
    first = True
    for name in ("pipeline nest", "front nest", "back-wall pipeline"):
        if name not in tg:
            continue
        # the pipe under a turret pair: the piece's centre position fitted so the pipe passes under both
        spots = [(top_pos(t) - TURRET / 2, x) for t, x in tg[name]]
        c = sum(p - PIPE[1] / 2 + pipe_y(1, x) for p, x in spots) / len(spots)
        out.append(("# 1, 2, 5. the half-buried pipeline under the spine-turret pairs (fitted so the pipe runs under both)"
                    if first else "", {"piece": "pipeline-a", "t": t_centre(c), "x": 240}))
        first = False
    out.append(("# 1, 3. the pipeline elsewhere on the plain (clear of the nests and of the rover at x 60)",
                {"piece": "pipeline-b", "t": t_centre(top_pos(18) - 100), "x": 240}))
    out.append(("", {"piece": "pipeline-b", "t": t_centre(top_pos(78) - 100), "x": 240}))
    if "channel mortars" in tg:
        mp, mx = target_centre("channel mortars")
        out.append(("# 2. the dry channel of the Polyp Mortar pair", {"piece": "dry-channel", "t": t_centre(mp), "x": round(mx)}))
    first = True
    for name in ("hint nest", "ridge nest", "wall nest"):
        if name not in tg:
            continue
        rp, rx = target_centre(name, TURRET)
        out.append(("# 3, 5. the pipeline ridges under the spine-turret nests (centred on each nest)" if first else "",
                    {"piece": "pipeline-ridge", "t": t_centre(rp), "x": round(min(max(rx, 140), 340))}))
        first = False
    first = True
    for name in ("storm mortars", "lake floe"):
        for t, x in tg.get(name, []) if name == "storm mortars" else []:
            out.append(("# 3, 4. Vrell nest growth under the mortars" if first else "",
                        {"piece": "vrell-growth", "t": t_centre(top_pos(t) - TURRET / 2), "x": round(x)}))
            first = False
        if name == "lake floe" and name in tg:
            lp, lx = target_centre(name, TURRET)
            out.append(("# 3, 4. Vrell nest growth under the mortars" if first else "",
                        {"piece": "vrell-growth", "t": t_centre(lp), "x": round(lx)}))
            first = False
    out.append(("# 6. Ares Landing's outskirts ahead at the level's end (its domes on screen from t~180)",
                {"piece": "ares-outskirts", "t": t_centre(scroll_at(END) + SCREEN - 290), "x": 240}))
    # the eye's walls on low-air: exact rows so the copies join, covering the eye and its ramps
    lo = jr(top_pos(STARTS[3] - 4, "low-air") - SCREEN - 200)
    hi = jr(top_pos(STARTS[4] + 2, "low-air"))
    first = True
    for bottom in range(lo, hi, TILE_H):
        for side, x in (("left", EYE_W // 2), ("right", W - EYE_W // 2)):
            out.append(("# 4. the storm's walls round the eye (low-air, exact rows: the copies join)" if first else "",
                        {"piece": f"eye-wall-{side}", "t": t_for_bottom("low-air", bottom, TILE_H), "x": x}))
            first = False
    out.append(("# 2. the front passing over the screen: its leading edge enters at the top at t=28 (high-air)",
                {"piece": "storm-front", "t": round(t_at((top_pos(28, "high-air") + 420 / 2 - MID) / FACTORS["high-air"]), 3),
                 "x": 240}))
    out.append(("# 5. the back wall passing over at t=130 (high-air)",
                {"piece": "storm-back-wall", "t": round(t_at((top_pos(130, "high-air") + 480 / 2 - MID) / FACTORS["high-air"]), 3),
                 "x": 240}))
    return out


def write_proposal():
    places = placements()
    flow = k8.flow
    lines = ["backdrop:",
             "  # Presentation only: Dust Storm (tools/art/backdrop_l18.py, from mars-storm-plains-r36-a and the radius look b).",
             "  # The ground is opaque; no deep layer (the README's horizon and the storm wall standing on it cannot show",
             "  # under a top-down ground: the wall is the high-air front passing over, the domes Ares Landing's outskirts",
             "  # ahead at the end; pending round 41). The storm's veil and tongues (storm-veil, storm-veil-drift,",
             "  # storm-tongues-a/-b in the same folder) are StormLooks' textures, not backdrop pieces.",
             "  # Seams and the eye's walls sit at exact rows: re-run `backdrop_l18.py --proposal` after the data moves a section.",
             f"  scroll_factors: {flow(FACTORS)}",
             f"  ramp: {RAMP}",
             f"  haze_colour: {HAZE_COLOUR}  # the butterscotch sky (mars_r36.SKY)",
             "  atmosphere:"]
    notes = {"clear": "section 1 before the front: haze only", "light": "the eye and the clearing: light wind, thin wisps",
             "medium": "the front's streaks (section 1's peak 20-25) and section 2",
             "heavy": "the deep storm and the back wall: dust banks and streaks"}
    for k, v in ATMOSPHERE.items():
        lines.append(f"    {k}: {flow(v)}   # {notes[k]}")
    lines.append("  tile_sets:")
    for name, (layer, drift, note) in TILE_SETS.items():
        spec = {"layer": layer, "height": TILE_H}
        if drift is not None:
            spec["drift"] = drift
        lines.append(f"    {name}: {flow(spec)}   # {note}")
    lines.append("  pieces:")
    for name, (layer, note) in PIECES.items():
        lines.append(f"    {name}: {flow({'layer': layer, 'size': size_of(name)})}   # {note}")
    lines.append("  placed:")
    for comment, p in places:
        if comment:
            lines.append(f"    {comment}")
        lines.append(f"    - {flow(p)}")
    lines.append("  # Section tiles (`sections[].tiles`): " + "; ".join(
        f"{i + 1}: [{', '.join(t)}]" for i, t in enumerate(SECTION_TILES)))
    lines.append("  # Atmospheres: 1 clear with the medium peak 20-25 (the front's streaks); 2 medium; 3 heavy; 4 light; 5 heavy; 6 light.")
    lines.append("  # Ground-target positions used (the level's data, else the README): " + "; ".join(
        f"{k} {v}" for k, v in targets().items()))
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# =========================================================================== the storm look (review)

class StormLook:
    """The chosen radius look b as the game will draw it from this level's textures: outside the radius the
    ground and low-air go into the veil (two drifting fields), the falloff broken by the two tongue fields;
    air units fade to dark silhouettes keeping their glow; bullets, the ship and its shots stay drawn."""

    DARK = np.array((46, 24, 16), float)        # mars_r36.silhouette's dark

    def __init__(self):
        self.veil = np.array(Image.open(OUT / "storm-veil.png").convert("RGB")).astype(np.float64)
        drift = np.array(Image.open(OUT / "storm-veil-drift.png").convert("RGBA")).astype(np.float64)
        self.drift_rgb, self.drift_a = drift[..., :3], drift[..., 3] / 255
        self.ta = np.array(Image.open(OUT / "storm-tongues-a.png").convert("RGBA"))[..., 3] / 255
        self.tb = np.array(Image.open(OUT / "storm-tongues-b.png").convert("RGBA"))[..., 3] / 255
        yy, xx = np.mgrid[0:SCREEN, 0:W]
        self.yy, self.xx = yy, xx

    def tile(self, a, dx, dy):
        h, w = a.shape[:2]
        return a[(self.yy + int(dy)) % h, (self.xx + int(dx)) % w]

    def vis(self, t, px, py, r):
        if r >= FULL - 1:
            return np.ones((SCREEN, W))
        d = np.hypot(self.xx - px, self.yy - py)
        a = self.tile(self.ta, 9 * t, -40 * t)
        b = self.tile(self.tb, -14 * t, -70 * t)
        return sdf.smoothstep(r + 55, r - 65, d + 70 * (a - 0.5) + 40 * (b - 0.5))

    def veil_at(self, t):
        v = self.tile(self.veil, 0, -60 * t)
        da = self.tile(self.drift_a, -25 * t, -110 * t)[..., None]
        return v * (1 - da) + self.tile(self.drift_rgb, -25 * t, -110 * t) * da

    def ground(self, frame, t, px, py, r):
        """frame: float RGB of ground + low-air; returns it with the veil outside the radius and the mask."""
        v = self.vis(t, px, py, r)
        if r >= FULL - 1:
            return frame, v
        out = frame * 0.85 + np.array((176, 118, 74)) * 0.15
        return out * v[..., None] + self.veil_at(t) * (1 - v[..., None]), v

    def unit(self, img, sp, glow, x, y, v):
        """An air unit (PIL RGBA sprite, optional additive glow) on img (PIL RGBA) at (x, y) with mask v there."""
        a = np.array(sp).astype(np.float64)
        a[..., :3] = a[..., :3] * v + self.DARK * (1 - v)
        dark = Image.fromarray(a.astype(np.uint8), "RGBA")
        img.alpha_composite(dark, (int(round(x - sp.width / 2)), int(round(y - sp.height / 2))))
        if glow is not None:
            img = artkit.add_light(img, glow, (int(round(x - glow.width / 2)), int(round(y - glow.height / 2))))
        return img


# =========================================================================== review

_cache = {}


def cached(name):
    if name not in _cache:
        _cache[name] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    return _cache[name]


def over(dst, src, x, y):
    """src (float RGBA) with its top-left at (x, y) on dst (float RGB, top-down)."""
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
            pk = s.get("peak")
            if pk and pk["from"] <= t < pk["to"]:
                return pk["atmosphere"]
            return s.get("atmosphere", "light")


def tiled_rows(name, view, f, drift=0.0, t=0.0):
    img = cached(name)
    rows = jr(view * f) + (SCREEN - 1 - np.arange(SCREEN))
    out = img[[TILE_H - 1 - (int(r) % TILE_H) for r in rows]]
    if drift:
        out = np.roll(out, int(drift * t) % W, axis=1)
    return out


def layer_pieces(frame, layer, placed, view):
    f = FACTORS[layer]
    for p in placed:
        if PIECES[p["piece"]][0] != layer:
            continue
        img = cached(p["piece"])
        h, w = img.shape[:2]
        bottom = jr(scroll_at(p["t"]) * f + MID - h / 2)
        top_y = SCREEN - (bottom + h - jr(view * f))
        over(frame, img, jr(p["x"] - w / 2), top_y)


def ground_frame(t, placed):
    """Ground and low-air of the play field at t (float RGB)."""
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
    layer_pieces(frame, "ground", placed, view)
    atm = ATMOSPHERE[atmosphere_at(t)]
    if atm.get("banks"):
        over(frame, tiled_rows(atm["banks"], view, FACTORS["low-air"], TILE_SETS[atm["banks"]][1], t), 0, 0)
    layer_pieces(frame, "low-air", placed, view)
    return frame


def high_frame(frame, t, placed):
    view = scroll_at(t)
    atm = ATMOSPHERE[atmosphere_at(t)]
    if atm.get("wisps"):
        over(frame, tiled_rows(atm["wisps"], view, FACTORS["high-air"], TILE_SETS[atm["wisps"]][1], t), 0, 0)
    layer_pieces(frame, "high-air", placed, view)
    return frame


def stand_ins():
    import storm_units as su
    return {"drone": su.load("ghost-drone"), "glow": su.load("ghost-drone-glow"), "kite": artkit.load_frames("dust-kite"),
            "ship": artkit.load_frames("ship"), "orb": artkit.load_frames("orb")}


def play_frame(t, placed, look, units, ship_x=None):
    """The play field at t with the storm look at the README's radius and a few stand-in units: a Dust Kite
    pair (teal glow) and a Ghost Drone (red eye, white tips) crossing at the top, the ship below."""
    px = 240 + 60 * math.sin(t * 0.7) if ship_x is None else ship_x
    py = 430
    g, v = look.ground(ground_frame(t, placed), t, px, py, radius_at(t))
    img = Image.fromarray(np.clip(g, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    if units.get("drone"):
        u = (t * 0.23) % 1.0
        x, y = 520 - 600 * u, 150
        k = 4                                                  # strafing left: heading 4 faces left
        img = look.unit(img, units["drone"][k], units["glow"][k], x, y, v[int(np.clip(y, 0, SCREEN - 1)), int(np.clip(x, 0, W - 1))])
    if units.get("kite"):
        for i in range(2):
            u = (t * 0.17 + 0.5 * i) % 1.0
            x, y = 90 + 300 * i, -30 + 600 * u
            k = 0
            fr = units["kite"][k * 4 + int(t * 10) % 4]
            vv = v[int(np.clip(y, 0, SCREEN - 1)), int(np.clip(x, 0, W - 1))]
            img = look.unit(img, fr, None, x, y, vv)
    if units.get("orb"):
        o = units["orb"][0]
        for k in range(3):
            img.alpha_composite(o, (int(300 - 40 * k - 7), int(120 + 90 * k + (t * 40) % 60 - 7)))
    if units.get("ship"):
        sp = units["ship"][2]
        img.alpha_composite(sp, (int(px - sp.width / 2), int(py - sp.height / 2)))
    out = high_frame(np.array(img.convert("RGB")).astype(np.float64), t, placed)
    return np.clip(out, 0, 255).astype(np.uint8)


def review():
    placed = [p for _, p in placements()]
    look = StormLook()
    units = stand_ins()
    times = [10, 22, 29, 33, 45, 70, 90, 112, 122, 131, 150, 181]
    frames = [Image.fromarray(play_frame(t, placed, look, units), "RGB") for t in times]
    scale = 0.5
    cw, ch = int(W * scale), int(SCREEN * scale)
    cols = 6
    sheet = raster.sheet(16 + cols * (cw + 10), 60 + 2 * (ch + 24) + 300,
                         "LEVEL 18 DUST STORM - BACKDROP AND STORM LOOK (FINAL R41)", "PRODUCTION ART, M7 PART E BATCH - R41")
    raster.draw_text(sheet, 16, 38, "THE PLAY FIELD AT 0.5X AT THESE TIMES, THE RADIUS LOOK B AT MEDIUM (170 PX DEEP STORM, 150 BACK WALL), "
                                    "STAND-IN UNITS", raster.LABEL)
    for i, (t, fr) in enumerate(zip(times, frames)):
        x, y = 16 + (i % cols) * (cw + 10), 54 + (i // cols) * (ch + 24)
        sheet.paste(fr.resize((cw, ch), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + ch + 4, f"T={t}  R={'FULL' if radius_at(t) >= FULL - 1 else int(radius_at(t))}",
                         raster.LABEL_DIM)
    y = 54 + 2 * (ch + 24) + 6
    raster.draw_text(sheet, 16, y, "PIECES AND TEXTURES AT 0.5X (ON DARK)", raster.LABEL)
    x = 16
    for name in ("pipeline-a", "pipeline-ridge", "dry-channel", "vrell-growth", "ares-outskirts", "eye-wall-left",
                 "storm-front", "storm-veil", "storm-tongues-a"):
        img = Image.open(OUT / f"{name}.png").convert("RGBA")
        img = img.resize((max(1, img.width // 2), max(1, img.height // 2)), Image.BOX)
        if img.height > 240:
            img = img.crop((0, 0, img.width, 240))
        if x + img.width > sheet.width - 16:
            break
        sheet.alpha_composite(img, (x, y + 16))
        raster.draw_text(sheet, x, y + 16 + img.height + 2, name.upper(), raster.LABEL_DIM)
        x += img.width + 10
    sheet.convert("RGB").save(REVIEW, optimize=True)
    gif = [Image.fromarray(play_frame(t, placed, look, units), "RGB").resize((240, 270), Image.BOX)
           for t in np.arange(0.0, END, 0.6)]
    write_gif(gif, REVIEW.with_suffix(".gif"), fps=10, colors=128)
    print(f"review: {REVIEW.relative_to(ROOT)} and .gif")


if __name__ == "__main__":
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    if "--proposal" in sys.argv[1:]:
        write_proposal()
    elif "--review" in sys.argv[1:]:
        review()
    else:
        write_all(args)
        write_proposal()
        review()
