#!/usr/bin/env python3
"""Production art: the Level 36 backdrop, Cloud Divers (design/campaign/act-6-jovian-storm/level-36-cloud-divers; M10
part B batch, concept round 74, step ART-B): the act's first Jovian kit.

Straight to production from round 72's chosen looks: Q10 a's **L36 grade** (tools/concept/act6_r72.py ``sky_l36``:
high sunlit bands in cream, ochre and rust under a pale haze, the sun from the top left; the ``JOV_SUN`` ramp) and
A.12 a's **cream veil** for the foreground banks (``make_banks`` a: the bank in the band's own lit cream at 65 % over
the bodies). The user, round 72: "clouds at different altitudes do not have different colors", so the deep layer's
cloud heads, the low-air tops and wisps and the banks share one cream ramp (``CREAM``). Every band, haze, cloud and
glow is a float field written unquantised (far more than 32 steps, no ordered dither: the 2026-10-30 dither audit,
``DitherGuardTest``); every glow is additive and drawn once.

The layers (``scroll_factors``): ``deep`` 0.15 (Jupiter's bands far below: the slow crawl), ``low-air`` 1.3 (the
cloud tops and wisps below the play plane; the harvesters fly here), ``high-air`` 1.7 (the dive's thin ice haze and
the foreground banks: at 120 px/s a bank crosses the screen in about 3.4 s, the README's "3-4 s").

The deep layer is **one continuous field** down the whole level (``deep_field``: the layer rows from the level start
on), cut into the four sections' tile sets at the rows the game shows them (``LevelData.seam``: a section's tile set
begins at the row entering the top edge as it starts), so the section changes have no seam on the screen; each tile's
rows past its section run a crossfade back to its own first rows, so it also wraps (BackdropSeamsTest). The bands
change by the layer row: the dive's bands far below under the high haze, Jupiter's limb and black space at the
bottom of the first screen; cream and ochre bands; a broad rust band; brown-grey toward the storm, then paler and
warmer at the end as the bands thin over Aurelia's sky. **Re-run after the data moves a section** (the tiles are cut
at the sections' rows).

Tile sets (480 wide; ``deep`` opaque, the others transparent; the deep heights follow the sections' spans):
  dive-bands      deep 480x1344: section 1: Jupiter's limb and space at the first screen's bottom, the bands far below
                  under the high haze, the cloud decks' small heads
  cream-bands     deep 480x1024: section 2: the cream and ochre storm bands, festoons, cloud heads
  rust-band       deep 480x992: section 3: a broad rust band between cream edges
  storm-edge      deep 480x1440: section 4: the band browning to grey (the storm wall's side), then paler and warmer
  cloud-tops      low-air 480x960: section 1: cream cloud tops rushing past (about a quarter of the area)
  cloud-wisps     low-air 480x960: sections 2-4: thin cream wisps along the wind
  ice-haze        high-air 480x960: section 1: faint white ice streaks over everything
  (cloud-tops and ice-haze are clear in the rows where section 2's tiles take over, so no cloud is cut at the seam)
Pieces (the layer named; ``-glow`` = additive, drawn by the game at its piece's placements on the piece's frame clock,
Level 24's convention, here on ``deep``: step B4 draws deep glows):
  carrier-group        deep 128x160: the CDF carrier group far below, nose up the screen: the carrier (the CDF heavy
                       transport's hull, round 72's stand-in for the carrier group, as the act still) and two escorts,
                       hazed toward the bands
  carrier-group-glow   deep 128x160, 8 frames at 4 fps: their running lights (white strobes: one flash in 2 s, red and
                       green nav lights, the blue engines)
  storm-wall           deep 320x480: the storm wall of Level 38 far ahead on the right: a towering brown-grey cloud mass
                       (soft edges all round)
  storm-wall-glow      deep 320x480, 12 frames at 6 fps: lightning inside it (soft blue-white flashes behind the cloud,
                       a bolt fragment; three flashes in the 2 s loop), placed so it shows from t~130
  bank-a .. bank-f     high-air, 142x86, 194x86, 224x122, 268x130, 300x160, 320x162: the foreground banks, the cream veil (alpha at most 0.65: a body
                       under a bank shows at about 35 %), soft edges; **each image is its bank's rectangle**: the
                       proposal's ``banks: [{t, x, w, h, piece}]`` carry the image's size, so the checks' rectangles and
                       the art agree exactly (``piece`` is new beside A2b's schema: B2 adds the key or matches by size)

Review: design/campaign/act-6-jovian-storm/level-36-cloud-divers/concept/backdrop-final-r74-a.png (eight screens with
stand-ins, the banks over bodies with the cap and band numbers, the pieces, the tile sets, the explosion read) and .gif
(t 100-110 at half size: the harvester fields under the banks). ``--proposal`` writes the ``backdrop:`` block, the
sections, the banks and the pathfinder pod's ``debris`` to the level's concept/backdrop-proposal.yaml (never into
data.yaml).

Reads the level's data.yaml when it exists (sections), otherwise the README's Layout table as sections.
Run: python3 tools/art/backdrop_l36.py [--proposal | --review | --check]   (all: about 1.5 min)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image

import artkit
from artkit import DESIGN, ROOT, TAU

import l23kit  # noqa: E402  (held sprites: load from assets/ or build/art-held/)
from render import raster  # noqa: E402
from render.sdf import rotate_z  # noqa: E402

SCRIPT = "backdrop_l36.py"
BATCH = "M10 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r74"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-6-jovian-storm" / "level-36-cloud-divers"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-36"
W, SCREEN = 480, 540
FACTORS = {"deep": 0.15, "low-air": 1.3, "high-air": 1.7}
LOW_H = HIGH_H = 960

# The one cream ramp of every cloud (A.12 a): shade 0 (the underside) .. 1 (the lit top) - round 72's bank tones a.
CREAM = [(0.0, (196, 160, 120)), (0.5, (236, 214, 178)), (1.0, (255, 248, 230))]
JOV_SUN = [(0.0, (110, 50, 28)), (0.25, (168, 92, 50)), (0.5, (206, 146, 86)), (0.75, (236, 204, 150)),
           (1.0, (252, 240, 214))]                                         # act6_r72.JOV_SUN (the L36 grade)
RUST = [(0.0, (78, 32, 20)), (0.3, (128, 54, 30)), (0.55, (166, 80, 44)), (0.8, (206, 140, 88)),
        (1.0, (236, 204, 156))]
STORM = [(0.0, (46, 38, 36)), (0.35, (82, 70, 62)), (0.65, (120, 106, 94)), (1.0, (164, 150, 132))]
HAZE = np.array((236, 224, 204), float)                                    # the pale haze (sky_l36's)
HIGH_HAZE = np.array((226, 224, 222), float)                               # the dive's high haze, a touch cooler
BANK_ALPHA = 0.65                                                          # the veil: a body under it at ~35 %

# =========================================================================== the clock (content LevelScroll, ported)

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
DEEP, LOW, HIGH = "dive-bands cream-bands rust-band storm-edge".split(), ("cloud-tops", "cloud-wisps"), "ice-haze"
PROPOSED_SECTIONS = [
    {"name": "The Dive", "end": 24, "speed": 160, "atmosphere": "clear", "tiles": ["dive-bands", "cloud-tops",
                                                                                  "ice-haze"]},
    {"name": "Into the Bands", "end": 70, "speed": 120, "ramp": 2, "atmosphere": "clear",
     "tiles": ["cream-bands", "cloud-wisps"]},
    {"name": "Harvester Fields", "end": 125, "speed": 100, "ramp": 2, "atmosphere": "clear",
     "tiles": ["rust-band", "cloud-wisps"]},
    {"name": "Storm Edge", "end": 192, "speed": 120, "ramp": 2, "atmosphere": "clear",
     "tiles": ["storm-edge", "cloud-wisps"]},
]
SECTIONS = DATA["sections"] if DATA else PROPOSED_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 120)) if DATA else 120.0
END = float(SECTIONS[-1]["end"])
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]


def _speed(i):
    s = SECTIONS[i]
    return float(s["speed"]) if s.get("speed") is not None else BASE_SPEED


def _u(t, start, ramp):
    return min(max((t - start) / ramp, 0.0), 1.0) if ramp > 0 else (1.0 if t >= start else 0.0)


def _ease_out(u):
    return u - u ** 3 + u ** 4 / 2


def _travelled(i, a, b):
    """backdrop_l30.py's port of LevelScroll for a level without halts: a speed change eased over the ramp."""
    speed, start, end = _speed(i), STARTS[i], float(SECTIONS[i]["end"])
    px = (b - a) * speed
    ramp = float(SECTIONS[i].get("ramp", 0) or 0)
    if i > 0 and ramp > 0:
        ramp = min(ramp, end - start)
        px -= (speed - _speed(i - 1)) * ramp * (_ease_out(_u(b, start, ramp)) - _ease_out(_u(a, start, ramp)))
    return px


def scroll(t):
    """LevelData.scrollAt: the ground scroll at level time t, px (before the start at the first speed)."""
    s = 0.0
    for i, sec in enumerate(SECTIONS):
        if t < float(sec["end"]) or i == len(SECTIONS) - 1:
            return s + _travelled(i, STARTS[i], t)
        s += _travelled(i, STARTS[i], float(sec["end"]))
    raise AssertionError


def time_at(s):
    lo, hi = -120.0, END + 60
    for _ in range(80):
        mid = (lo + hi) / 2
        if scroll(mid) < s:
            lo = mid
        else:
            hi = mid
    return (lo + hi) / 2


def seam(layer, i):
    """LevelData.seam, rounded as BackdropTiles does: the layer row where section i's tile set begins."""
    return int(round(scroll(STARTS[i]) * FACTORS[layer] + SCREEN))


def t_for_layer_row(row, factor):
    """The placement time putting a piece's centre on layer row ``row`` (centre = scroll(t) x factor + 270)."""
    return round(time_at((row - SCREEN / 2) / factor), 3)


def t_centre_at(t, y_down, factor):
    """The placement time of a piece whose centre is at screen y (down) at level time t."""
    return t_for_layer_row(scroll(t) * factor + (SCREEN - y_down), factor)


# =========================================================================== shared fields

def smoothstep(a, b, x):
    t = np.clip((x - a) / (b - a), 0, 1)
    return t * t * (3 - 2 * t)


def blur(a, sigma, wrap_rows=False):
    """A separable Gaussian blur in float (no 8-bit steps); rows wrap for a tile."""
    if sigma <= 0:
        return a
    r = int(np.ceil(3 * sigma))
    k = np.exp(-(np.arange(-r, r + 1) ** 2) / (2 * sigma * sigma))
    k /= k.sum()
    p = np.pad(a, ((r, r), (0, 0)), mode="wrap" if wrap_rows else "edge")
    a = sum(k[i] * p[i:i + a.shape[0], :] for i in range(2 * r + 1))
    p = np.pad(a, ((0, 0), (r, r)), mode="edge")
    return sum(k[i] * p[:, i:i + a.shape[1]] for i in range(2 * r + 1))


def streaks(h, w, seed, cell=8, squeeze=6):
    """Fine horizontal streaks: noise drawn narrow and stretched along x (act6_r72.bands' streak)."""
    s = raster.fbm(max(8, w // squeeze), h, cell, seed, octaves=3, period=False)
    img = Image.fromarray(np.clip(s * 255, 0, 255).astype(np.uint8)).resize((w, h), Image.BICUBIC)
    return np.array(img, np.float64) / 255


def billow_height(h, w, blobs, wrap=False):
    """Towering cloud heads as a smooth height field from blobs (x, y_down, radius); rows wrap when ``wrap``."""
    hgt = np.zeros((h, w))
    for bx, by, rr in blobs:
        for dy in ((-h, 0, h) if wrap else (0,)):
            cy = by + dy
            x0, x1 = int(max(0, bx - 2 * rr)), int(min(w, bx + 2 * rr + 1))
            y0, y1 = int(max(0, cy - 2 * rr)), int(min(h, cy + 2 * rr + 1))
            if x0 >= x1 or y0 >= y1:
                continue
            yy, xx = np.mgrid[y0:y1, x0:x1].astype(np.float64)
            d2 = ((xx - bx) ** 2 + (yy - cy) ** 2) / rr ** 2
            hgt[y0:y1, x0:x1] = np.maximum(hgt[y0:y1, x0:x1], rr * np.sqrt(np.clip(1 - d2 / 2.2, 0, 1)))
    return blur(hgt, 2.0, wrap)


def billow_shade(hgt, wrap=False, edge=None):
    """(shade 0..1 lit from the top left with soft self-shadowing and dark creases between the heads, coverage
    0..1) of a height field (act6_r72.billows' lighting, its creases deepened); ``edge`` (0..1 noise) frays the
    outline."""
    alpha = smoothstep(3, 10, hgt + (0 if edge is None else 9 * (edge - 0.5)))
    if wrap:
        p = np.pad(hgt, ((1, 1), (0, 0)), mode="wrap")
        gy, gx = np.gradient(p)
        gy, gx = gy[1:-1], gx[1:-1]
    else:
        gy, gx = np.gradient(hgt)
    shade = np.clip(0.5 - (gx + gy) * 0.42 + hgt / 80, 0, 1)
    sh = np.roll(np.roll(hgt, 6, 0), 6, 1)
    shade *= 1 - 0.38 * smoothstep(0, 10, sh - hgt)
    crease = np.clip(blur(hgt, 5.0, wrap) - hgt, 0, None)
    shade *= 1 - 0.5 * smoothstep(0, 6, crease)
    return blur(shade, 0.6, wrap), alpha


def cloud_mass(h, w, blobs, seed, wrap=False, detail=0.4):
    """Cumulus heads: the blobs' height field broken up by noise (cauliflower edges, no clean spheres), lit from
    the top left -> (shade 0..1, coverage 0..1)."""
    hgt = billow_height(h, w, blobs, wrap)
    n1 = raster.fbm(w, h, 16, seed, octaves=2, period=wrap)
    n2 = raster.fbm(w, h, 36, seed + 1, octaves=2, period=wrap)
    hgt = hgt * (1 - detail / 2 + detail * (0.4 * n1 + 0.6 * n2))
    return billow_shade(blur(hgt, 0.7, wrap), wrap, n1)


def mass_blobs(rng, cx, cy, rx, ry, n, size):
    """Blobs in an ellipse round (cx, cy), bigger toward its middle (a cloud's towering core)."""
    out = []
    for _ in range(n):
        a, u = rng.uniform(0, TAU), np.sqrt(rng.uniform(0, 1))
        k = 1 - 0.55 * u
        out.append((cx + np.cos(a) * u * rx, cy + np.sin(a) * u * ry, rng.uniform(*size) * k))
    return out


# =========================================================================== the deep field (rows = layer rows)

def deep_span():
    """Layer rows the level shows on the deep layer: from the start's bottom edge to the end's top (+4 s)."""
    return -40, int(np.ceil(scroll(END + 4) * FACTORS["deep"] + SCREEN))


def deep_field(seed=3601):
    """The whole deep layer as one float RGB field, row 0 = layer row ``r0`` (y-up: the array's row i is layer row
    r0 + i, so it is drawn flipped). Returns (rgb, r0)."""
    lo, hi = deep_span()
    r0, r1 = lo - 1600, hi + 1600
    n = r1 - r0
    r = (np.arange(n) + r0).astype(np.float64)[:, None] * np.ones((1, W))
    x = np.ones((n, 1)) * np.arange(W, dtype=np.float64)[None, :]
    seams = [seam("deep", i) for i in range(1, len(SECTIONS))]
    s1, s2, s3 = (seams + [hi, hi, hi])[:3]

    # the weight of each section's look along the rows (the changes crawl in over ~140 rows round each seam)
    def w_after(s, width=140):
        return smoothstep(s - width / 2, s + width / 2, r)
    wb, wr, ws = w_after(s1), w_after(s2), w_after(s3)
    k_dive, k_band, k_rust, k_storm = 1 - wb, wb * (1 - wr), wr * (1 - ws), ws
    # the band phase: its period by section (far bands fine, the rust band broad), integrated so it never jumps
    period = 64 * k_dive + 150 * k_band + 300 * k_rust + 190 * k_storm
    phase = np.cumsum(TAU / period[:, 0])[:, None]
    warp = raster.fbm(W, n, 96, seed, octaves=3, period=False)
    fest = raster.fbm(W, n, 40, seed + 1, octaves=3, period=False)
    st = streaks(n, W, seed + 2)
    v = 0.5 + 0.5 * np.sin(phase + (warp - 0.5) * 2.6 + 0.35 * np.sin(x / 90 + r / 410) + (fest - 0.5) * 0.9)
    v = np.clip(0.62 * v + 0.38 * st, 0, 1)
    # the rust band: one broad dark-red band with cream edges across section 3's middle
    mid3 = (s2 + s3) / 2
    rust_core = np.exp(-((r - mid3 + 30 * (warp - 0.5)) / ((s3 - s2) * 0.36)) ** 2)
    sun = raster.ramp(JOV_SUN, 0.18 + 0.72 * v)
    far = raster.ramp(JOV_SUN, 0.3 + 0.55 * v)
    rust = raster.ramp(RUST, np.clip(0.12 + 0.7 * v - 0.25 * rust_core, 0, 1))
    # section 4: browning toward grey over its first half, the end paler and warmer (Aurelia's sky ahead)
    span4 = max(1.0, hi - s3)
    grey = smoothstep(s3, s3 + 0.3 * span4, r) * (1 - smoothstep(s3 + 0.55 * span4, s3 + 0.85 * span4, r))
    storm = raster.ramp(STORM, 0.15 + 0.75 * v) * grey[..., None] + sun * (1 - grey[..., None])
    rgb = (far * k_dive[..., None] + sun * k_band[..., None] + rust * k_rust[..., None] + storm * k_storm[..., None])
    # the deep layer's cloud heads (the decks far below): the one cream, many small ones in the dive
    rng = np.random.default_rng(seed + 7)
    blobs = []
    for _ in range(int(n / 60)):
        cy = rng.uniform(0, n)
        dive = float(r0 + cy) < s1
        if not dive and rng.uniform() < 0.4:
            continue
        k = 0.55 if dive else 1.0
        blobs += mass_blobs(rng, rng.uniform(-30, W + 30), cy, 70 * k, 22 * k, 26, (4 * k + 2, 11 * k + 2))
    shade, cov = cloud_mass(n, W, blobs, seed + 8)
    light = 1 - 0.42 * grey                                         # the storm side darkens every cloud the same
    clouds = raster.ramp(CREAM, shade) * light[..., None]
    cov = cov * (0.5 + 0.25 * (1 - k_dive))                          # far below: thinned by the haze
    rgb = rgb * (1 - cov[..., None]) + clouds * cov[..., None]
    # the haze: heavy and cool in the dive, the pale L36 haze after; the sun's warmth from the top left
    haze_k = 0.42 * k_dive + 0.16 * (1 - k_dive) * (1 - 0.4 * grey)
    tint = HIGH_HAZE * k_dive[..., None] + HAZE * (1 - k_dive[..., None])
    rgb = rgb * (1 - haze_k[..., None]) + tint * haze_k[..., None]
    rgb *= (1.0 + 0.08 * (1 - x / W))[..., None]
    # Jupiter's limb and space at the first screen's bottom: the planet's edge bulging toward us in the middle
    limb = 70 + (620 - np.sqrt(np.clip(620 ** 2 - (x - 240) ** 2, 0, None)))
    above = r - limb
    space = np.zeros_like(rgb) + np.array((4, 5, 11.0))
    srng = np.random.default_rng(seed + 9)
    for _ in range(90):
        i, j = srng.integers(0, int(-r0 + 140)), srng.integers(0, W)
        b = srng.uniform(50, 200)
        space[i, j] += (b, b * 0.97, b * 0.92)
    darken = 0.45 + 0.55 * smoothstep(0, 90, above)
    rgb = rgb * darken[..., None]
    rim = np.exp(-(above / 3.5) ** 2) * 0.7 + np.exp(-(np.clip(-above, 0, None) / 9.0)) * 0.35 * (above < 0)
    on_planet = smoothstep(-1.0, 1.0, above)[..., None]
    rgb = rgb * on_planet + space * (1 - on_planet)
    rgb += rim[..., None] * np.array((150, 176, 214.0))
    return np.clip(rgb, 0, 255), r0


def cut_tile(field, r0, a, b):
    """A tile set of the rows [a, b) of the field (y-up), its height the span + 160 rounded up to 32; the rows past b
    crossfade back into the rows below a, so the tile wraps. Returns the image (top row = its highest)."""
    span = b - a
    h = int(np.ceil((span + 160) / 32) * 32)
    out = np.zeros((h, W, 3))
    for j in range(h):
        r = a + ((j - a) % h)
        if r < b:
            val = field[r - r0]
        else:
            w = smoothstep(b, a + h, r)
            val = (1 - w) * field[r - r0] + w * field[r - h - r0]
        out[h - 1 - j] = val
    return Image.fromarray(np.clip(np.round(out), 0, 255).astype(np.uint8), "RGB").convert("RGBA")


def deep_tiles():
    field, r0 = deep_field()
    lo, hi = deep_span()
    bounds = [lo] + [seam("deep", i) for i in range(1, len(SECTIONS))] + [hi]
    return {name: cut_tile(field, r0, bounds[i], bounds[i + 1]) for i, name in enumerate(DEEP)}


# =========================================================================== low-air and high-air tiles

def clear_rows(h, rows, width=110):
    """A tile's alpha multiplier: 0 over the tile rows of the layer rows ``rows`` (where another section's tile set
    meets this one), easing back over ``width`` rows (the image is top row = highest)."""
    m = np.ones(h)
    for lr in rows:
        j = np.arange(h)
        d = np.abs(((j - lr % h) + h / 2) % h - h / 2)
        m = np.minimum(m, smoothstep(width * 0.35, width, d))
    return m[::-1][:, None]


TOPS_ROLL = 40


def cloud_tops(seed=3621):
    h = LOW_H
    rng = np.random.default_rng(seed)
    blobs = []
    for _ in range(8):
        blobs += mass_blobs(rng, rng.uniform(-20, W + 20), rng.uniform(0, h), 100, 40, 110, (8, 22))
    shade, cov = cloud_mass(h, W, blobs, seed + 1, wrap=True)
    # step B4: the field rolled 40 rows (the same clouds) so the tile's wrap falls between two heads, not along an
    # edge (BackdropSeamsSpaceTest's wrap score 1.58 -> 0.39)
    shade, cov = np.roll(shade, TOPS_ROLL, 0), np.roll(cov, TOPS_ROLL, 0)
    s = seam("low-air", 1)
    alpha = cov * 0.92 * clear_rows(h, [s - 40])
    return rgba(raster.ramp(CREAM, shade), alpha)


def cloud_wisps(seed=3622):
    h = LOW_H
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:W].astype(np.float64)
    alpha = np.zeros((h, W))
    for _ in range(14):
        cx, cy = rng.uniform(-60, W + 60), rng.uniform(0, h)
        length, thick = rng.uniform(140, 300), rng.uniform(5, 13)
        tilt = rng.uniform(-0.12, 0.05)
        for dy in (-h, 0, h):
            u = (xx - cx) / (length / 2)
            v = (yy - cy - dy - (xx - cx) * tilt) / thick
            alpha = np.maximum(alpha, np.exp(-v ** 2) * np.clip(1 - u ** 2, 0, 1) ** 1.5)
    tex = raster.fbm(W, h, 24, seed + 1, octaves=4, period=True)
    alpha = np.clip(alpha * (0.45 + 0.9 * tex), 0, 1) * 0.55
    shade = np.clip(0.55 + 0.6 * (tex - 0.5) + 0.25 * alpha, 0, 1)
    s = seam("low-air", 1)
    alpha *= clear_rows(h, [s + 40])
    return rgba(raster.ramp(CREAM, shade), blur(alpha, 1.0, True))


def ice_haze(seed=3623):
    h = HIGH_H
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:W].astype(np.float64)
    alpha = np.zeros((h, W))
    for _ in range(22):
        cx, cy = rng.uniform(-40, W + 40), rng.uniform(0, h)
        length, thick = rng.uniform(60, 200), rng.uniform(1.2, 3.5)
        for dy in (-h, 0, h):
            u = (xx - cx) / (length / 2)
            v = (yy - cy - dy) / thick
            alpha = np.maximum(alpha, np.exp(-v ** 2) * np.clip(1 - u ** 2, 0, 1) ** 2 * rng.uniform(0.5, 1))
    alpha = blur(alpha, 0.8, True) * 0.17 * clear_rows(h, [seam("high-air", 1) - 40])
    return rgba(np.zeros((h, W, 3)) + np.array((232, 236, 244.0)), alpha)


def rgba(rgb, alpha):
    a = np.clip(np.round(np.asarray(alpha) * 255), 0, 255)
    rgb = np.where(a[..., None] > 0, np.clip(np.round(rgb), 0, 255), 0)
    return Image.fromarray(np.dstack([rgb, a]).astype(np.uint8), "RGBA")


# =========================================================================== pieces

def soft_box(w, h, edge):
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    d = np.minimum.reduce([xx + 0.5, w - 0.5 - xx, yy + 0.5, h - 0.5 - yy])
    return smoothstep(1.0, edge, d)


BANKS = {"bank-a": (160, 96, 3631), "bank-b": (200, 112, 3633), "bank-c": (240, 128, 3637),
         "bank-d": (280, 144, 3641), "bank-e": (320, 160, 3643), "bank-f": (352, 176, 3647)}   # the canvas aimed at


def bank(w, h, seed):
    """A foreground bank: a cloud mass (heads towering in its middle, lumpy all round) in the cream veil, at most
    BANK_ALPHA; cropped to its own bounds, so the image is its bank's rectangle."""
    rng = np.random.default_rng(seed)
    blobs = mass_blobs(rng, w / 2, h / 2, w * 0.4, h * 0.28, int(w * h / 420), (h * 0.11, h * 0.24))
    shade, cov = cloud_mass(h, w, blobs, seed + 1)
    yy = np.mgrid[0:h, 0:w][0].astype(np.float64)
    shade = np.clip(shade - 0.18 * smoothstep(h * 0.35, h * 0.9, yy), 0, 1)      # the underside a step darker
    a = np.clip(cov, 0, 1) * soft_box(w, h, 4) * BANK_ALPHA
    img = rgba(raster.ramp(CREAM, shade), a)
    x0, y0, x1, y1 = img.getbbox()
    img = img.crop((x0 - 1, y0 - 1, x1 + 1, y1 + 1))
    return img.crop((0, 0, img.width + img.width % 2, img.height + img.height % 2))


def carrier_job(job):
    """One render of a carrier-group hull: the CDF heavy transport (tools/art/outro_l14_images.py, round 72's stand-in
    for the carrier group's hulls) nose up the screen, ``extent`` model units across the ``size`` canvas."""
    import outro_l14_images as o14  # noqa: E402
    size, extent = job
    scene, mats = o14.transport_scene()
    hi, f = artkit.render_hi(lambda p: scene(rotate_z(p, np.pi / 2)), mats, size, extent, z_top=40.0)
    return artkit.native(hi, f, 60)


CARRIER = (128, 160)
CARRIER_PX = 0.36                                                     # px per transport unit (90 px long)
GROUP = [((64, 76), 1.0), ((22, 118), 0.42), ((106, 112), 0.42)]       # (centre, scale): the carrier, two escorts
HULL_CANVAS = {1.0: (108, 124), 0.42: (48, 56)}
LIGHTS = [(-79, -102, "red"), (79, -102, "green"), (-8, -126, "engine"), (8, -126, "engine"), (-42, -121, "engine"),
          (42, -121, "engine"), (-79, -98, "strobe"), (79, -98, "strobe"), (0, 128, "strobe")]   # transport units


def carrier_group():
    """The carrier and its two escorts, hazed toward the bands, and their light frames (8 at 4 fps: the white
    strobes flash on frame 0 and fade on frame 1; the nav lights and the engines steady), smooth additive glows."""
    jobs = [(HULL_CANVAS[1.0], HULL_CANVAS[1.0][0] / CARRIER_PX), (HULL_CANVAS[0.42], HULL_CANVAS[0.42][0] / (CARRIER_PX * 0.42))]
    with ProcessPoolExecutor() as pool:
        big, small = list(pool.map(carrier_job, jobs))
    img = Image.new("RGBA", CARRIER, (0, 0, 0, 0))
    for (cx, cy), s in GROUP:
        sp = big if s == 1.0 else small
        img.alpha_composite(sp, (int(round(cx - sp.width / 2)), int(round(cy - sp.height / 2))))
    a = np.array(img).astype(np.float64)
    a[..., :3] = a[..., :3] * 0.5 + np.array((214, 204, 188.0)) * 0.5    # hazed: far below the play plane
    body = artkit.quantize_set([Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")], 40)[0]
    pts = []
    for (cx, cy), s in GROUP:
        k = s * CARRIER_PX
        hw, hh = HULL_CANVAS[s]
        cx0, cy0 = int(round(cx - hw / 2)) + hw / 2, int(round(cy - hh / 2)) + hh / 2
        pts += [(cx0 + mx * k, cy0 - my * k, kind, s) for mx, my, kind in LIGHTS]
    frames = []
    for f in range(8):
        light = np.zeros((CARRIER[1], CARRIER[0], 3))
        for x, y, kind, s in pts:
            if kind == "strobe":
                k = {0: 1.0, 1: 0.35}.get(f, 0.0)
                if k:
                    glow(light, x, y, 1.4 * (0.7 + 0.3 * s), (255, 255, 255), 2.2 * k)
                    glow(light, x, y, 4.5 * (0.7 + 0.3 * s), (200, 220, 255), 0.35 * k)
            elif kind == "engine":
                glow(light, x, y + 2 * s, 2.0 * (0.6 + 0.4 * s), (110, 170, 255), 1.4)
            else:
                glow(light, x, y, 1.2, (255, 60, 50) if kind == "red" else (60, 255, 110), 1.6)
        frames.append(additive_rgb(light))
    return body, frames


def glow(light, x, y, r, col, k):
    h, w = light.shape[:2]
    rr = int(np.ceil(r * 4))
    x0, x1, y0, y1 = max(0, int(x - rr)), min(w, int(x + rr) + 1), max(0, int(y - rr)), min(h, int(y + rr) + 1)
    if x0 >= x1 or y0 >= y1:
        return
    yy, xx = np.mgrid[y0:y1, x0:x1].astype(np.float64) + 0.5
    f = np.exp(-((xx - x) ** 2 + (yy - y) ** 2) / (r * r)) * k
    light[y0:y1, x0:x1] += f[..., None] * np.array(col, float)


def additive_rgb(light):
    """A float light field (0..255+) -> premultiplied on black, alpha 1 wherever it adds light (unquantised)."""
    rgb = np.clip(np.round(light), 0, 255)
    a = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, a]).astype(np.uint8), "RGBA")


STORM_WALL = (320, 480)
STORM_SPOTS = [(150, 250, 46), (210, 330, 38), (120, 360, 30)]      # flash centres inside the mass (x, y, radius)
STORM_FLASH = {0: (0, 1.0), 1: (0, 0.45), 4: (1, 0.8), 5: (1, 0.3), 8: (2, 1.0), 9: (0, 0.6), 10: (2, 0.25)}


def storm_wall(seed=3651):
    """The storm wall far ahead: a towering brown-grey cloud mass, lit from the top left, its core dark; soft
    edges all round (a deep piece over the deep tile)."""
    w, h = STORM_WALL
    rng = np.random.default_rng(seed)
    blobs = []
    for k in range(9):
        blobs += mass_blobs(rng, rng.uniform(130, 220), 50 + k * 46, 90, 34, 40, (14, 34))
    hgt = billow_height(h, w, blobs)
    shade, cov = cloud_mass(h, w, blobs, seed + 1)
    rgb = raster.ramp(STORM, np.clip(0.1 + 0.85 * shade, 0, 1)) * 0.92
    a = cov * soft_box(w, h, 24) * 0.96
    return rgba(rgb, a), hgt


def storm_glow(hgt):
    """12 frames: soft blue-white light inside the cloud (strongest where the cloud is thick), a short bolt fragment
    on the brightest flashes; frames without a flash are dark (alpha 0)."""
    w, h = STORM_WALL
    thick = smoothstep(4, 30, hgt)
    rng = np.random.default_rng(3652)
    frames = []
    for f in range(12):
        light = np.zeros((h, w, 3))
        if f in STORM_FLASH:
            spot, k = STORM_FLASH[f]
            sx, sy, sr = STORM_SPOTS[spot]
            yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
            field = np.exp(-((xx - sx) ** 2 + (yy - sy) ** 2) / (sr * sr * 2.2)) * thick
            light += field[..., None] * np.array((150, 170, 230.0)) * k
            if k >= 0.8:
                x, y = sx + rng.uniform(-8, 8), sy - sr * 0.6
                for _ in range(10):
                    nx, ny = x + rng.uniform(-6, 6), y + rng.uniform(5, 10)
                    for u in np.linspace(0, 1, 8):
                        glow(light, x + (nx - x) * u, y + (ny - y) * u, 0.9, (230, 240, 255), 1.1 * k)
                    x, y = nx, ny
        frames.append(additive_rgb(light))
    return frames


# =========================================================================== build

def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    for name, img in deep_tiles().items():
        save(name, img)
        print(f"{name}: {img.width}x{img.height}")
    save("cloud-tops", cloud_tops())
    save("cloud-wisps", cloud_wisps())
    save("ice-haze", ice_haze())
    for name, (w, h, seed) in BANKS.items():
        save(name, bank(w, h, seed))
    body, lights = carrier_group()
    save("carrier-group", body)
    for i, f in enumerate(lights):
        save(f"carrier-group-glow_{i}", f)
    wall, hgt = storm_wall()
    save("storm-wall", wall)
    for i, f in enumerate(storm_glow(hgt)):
        save(f"storm-wall-glow_{i}", f)
    return pieces_spec(), placements(), bank_rows()


def tile_heights():
    return {name: Image.open(OUT / f"{name}.png").height for name in list(DEEP) + list(LOW) + [HIGH]}


def pieces_spec():
    spec = {"carrier-group": {"layer": "deep", "size": list(CARRIER)},
            "carrier-group-glow": {"layer": "deep", "size": list(CARRIER), "frames": 8, "fps": 4},
            "storm-wall": {"layer": "deep", "size": list(STORM_WALL)},
            "storm-wall-glow": {"layer": "deep", "size": list(STORM_WALL), "frames": 12, "fps": 6}}
    for name in BANKS:
        spec[name] = {"layer": "high-air", "size": list(bank_size(name))}
    return spec


def bank_size(name):
    return Image.open(OUT / f"{name}.png").size


# =========================================================================== the places

def placements():
    """The deep pieces: the carrier group in the dive (centred mid-screen at t=11, right of centre; on the screen
    from the start to ~t=26), the storm wall entering the top on the right at ~t=126 (its lightning shows from
    ~t=130) and passing by the end."""
    f = FACTORS["deep"]
    wall_t = t_centre_at(126.0, -STORM_WALL[1] / 2 + 24, f)
    return [{"name": "carrier-group", "t": 11.0, "x": 300, "note": "the CDF carrier group far below (section 1)"},
            {"name": "storm-wall", "t": round(wall_t, 3), "x": 380,
             "note": "the storm wall far ahead on the right, its top entering at t~126; storm-wall-glow (lightning "
                     "inside) is drawn at this place"}]


# Foreground banks (t = when the bank's centre passes the screen's middle, x its centre; the piece's size is its
# rectangle). README: from t~28 (small), never over the t=34 Chimera pair (nothing on the screen t=31-42), the t=45
# wing entering under a bank's edge, densest in section 3 (a third of the field), the t=103 Lampreys and the t=120
# Chimera swarm entering under banks, the pod drifting under one (t~146, x 400), thinning in section 4; skip: [easy]
# keeps easy under a quarter.
BANK_PLAN = [
    (28.6, 360, "bank-a", None), (46.3, 150, "bank-b", None), (51.8, 370, "bank-a", None), (56.6, 120, "bank-c", None),
    (61.6, 350, "bank-b", None), (66.2, 150, "bank-c", None), (70.6, 360, "bank-c", None),
    (73.6, 130, "bank-e", "easy"), (76.0, 360, "bank-c", None), (79.0, 140, "bank-d", None),
    (81.6, 340, "bank-e", "easy"), (84.6, 140, "bank-c", None), (87.2, 350, "bank-d", None),
    (90.2, 130, "bank-e", "easy"), (93.0, 350, "bank-c", None), (95.8, 140, "bank-d", None),
    (98.6, 360, "bank-e", "easy"), (101.6, 130, "bank-c", None), (104.6, 260, "bank-e", None),
    (107.8, 120, "bank-c", "easy"), (110.4, 350, "bank-d", None), (113.4, 130, "bank-e", "easy"),
    (116.2, 360, "bank-c", None), (121.6, 240, "bank-f", None),
    (128.4, 120, "bank-c", None), (134.0, 360, "bank-b", None), (141.0, 140, "bank-c", None),
    (148.6, 390, "bank-d", None), (157.0, 130, "bank-b", None), (165.0, 350, "bank-a", None),
    (174.0, 150, "bank-a", None),
]


def bank_rows():
    return [{"t": t, "x": x, "w": bank_size(p)[0], "h": bank_size(p)[1], "piece": p, "skip": [s] if s else None}
            for t, x, p, s in BANK_PLAN]


def bank_box(b, at):
    """JovianHazardChecks.bankAt: [left, bottom, right, top], y-up from the play field's bottom edge."""
    centre = SCREEN / 2 + FACTORS["high-air"] * (scroll(b["t"]) - scroll(at))
    return b["x"] - b["w"] / 2, centre - b["h"] / 2, b["x"] + b["w"] / 2, centre + b["h"] / 2


def bank_measures(banks, difficulty="medium", dt=0.05):
    """(the most of the field under banks at once and when, the longest stay over the ship's band and which bank,
    the windows a bank is on the screen [(t0, t1, i)]) - the checks' samples."""
    on = [b for b in banks if not (b.get("skip") and difficulty in b["skip"])]
    worst, worst_t = 0.0, 0.0
    band = {}
    windows = {}
    cell = 4
    for step in range(int((END + 2) / dt)):
        at = step * dt
        mask = np.zeros((SCREEN // cell, W // cell), bool)
        for b in on:
            x0, y0, x1, y1 = bank_box(b, at)
            if y1 <= 0 or y0 >= SCREEN or x1 <= 0 or x0 >= W:
                continue
            windows.setdefault(id(b), [at, at, banks.index(b)])[1] = at
            c0, c1 = int(max(0, x0) // cell), int(np.ceil(min(W, x1) / cell))
            r0, r1 = int(max(0, y0) // cell), int(np.ceil(min(SCREEN, y1) / cell))
            mask[r0:r1, c0:c1] = True
            if y0 < 140:
                band[id(b)] = band.get(id(b), 0) + dt
        share = mask.mean()
        if share > worst:
            worst, worst_t = share, at
    longest = max(band.values()) if band else 0.0
    return worst, worst_t, longest, sorted(windows.values())


# =========================================================================== proposal

def proposal_text(pieces, placed, banks):
    src = "the level's data.yaml" if DATA else "the README's Layout table (no data.yaml yet: re-run after B2)"
    h = tile_heights()
    lines = ["backdrop:",
             f"  # Presentation only: Level 36's dive over Jupiter's bands (tools/art/backdrop_l36.py, step ART-B, from {src}).",
             "  # No ground (`ground: none`): the deep layer is the base. Its four tile sets are cut from one continuous",
             "  # field at the rows each section's tiles begin (LevelData.seam): re-run the generator after moving a",
             "  # section. Animated: carrier-group-glow (section 1) and storm-wall-glow (section 4), never together.",
             "  # The -glow pieces are additive at their piece's placements (Level 24's ground rule; B4: on deep too).",
             "  scroll_factors: {deep: 0.15, far: 0.5, ground: 1, low-air: 1.3, high-air: 1.7}   # far, ground unused",
             "  ramp: 2",
             "  haze_colour: ece0cc",
             "  atmosphere:",
             "    clear: {haze: 0.0}   # the haze is in the tiles",
             "  tile_sets:",
             f"    dive-bands: {{layer: deep, height: {h['dive-bands']}}}   # section 1: the limb, the bands far below",
             f"    cream-bands: {{layer: deep, height: {h['cream-bands']}}}   # section 2: cream and ochre bands",
             f"    rust-band: {{layer: deep, height: {h['rust-band']}}}   # section 3: the broad rust band",
             f"    storm-edge: {{layer: deep, height: {h['storm-edge']}}}   # section 4: browning to grey, then paler",
             f"    cloud-tops: {{layer: low-air, height: {h['cloud-tops']}}}   # section 1: cream tops rushing past",
             f"    cloud-wisps: {{layer: low-air, height: {h['cloud-wisps']}}}   # sections 2-4: cream wisps",
             f"    ice-haze: {{layer: high-air, height: {h['ice-haze']}}}   # section 1: faint ice streaks",
             "  pieces:"]
    for name, spec in pieces.items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for p in placed:
        if p["note"]:
            lines.append(f"    # {p['note']}")
        lines.append(f"    - {{piece: {p['name']}, t: {p['t']}, x: {p['x']}}}")
    worst, wt, longest, _ = bank_measures(banks)
    eworst, ewt, _, _ = bank_measures(banks, "easy")
    lines += ["  banks:",
              "    # The foreground banks (A2b's {t, x, w, h, skip}; `piece` names the image whose size is the rectangle:",
              "    # B2 adds the key to the schema or draws the bank-* piece of that size). Measured here as",
              f"    # JovianHazardChecks samples them: at most {worst * 100:.1f} % of the field under banks at t={wt:.2f}"
              f" (easy {eworst * 100:.1f} % at t={ewt:.2f}),",
              f"    # the longest stay over the ship's band {longest:.2f} s; never on the screen t=31-42 (the Chimera pair)."]
    for b in banks:
        skip = f", skip: [{b['skip'][0]}]" if b.get("skip") else ""
        lines.append(f"    - {{t: {b['t']}, x: {b['x']}, w: {b['w']}, h: {b['h']}, piece: {b['piece']}{skip}}}")
    lines.append("sections:   # ART-B's reading of the README's Layout (speeds, 2 s eases); B2 writes the real ones")
    for s in SECTIONS:
        lines.append("  - " + "{" + ", ".join(f"{k}: {v}" for k, v in s.items() if k != "notes") + "}")
    lines.append(f"# rows: the end's bottom edge {int(round(scroll(END)))} (ground px); deep "
                 f"{int(round(scroll(END) * FACTORS['deep']))}, high-air {int(round(scroll(END) * FACTORS['high-air']))}")
    lines += ["debris:",
              "  # Proposal (ART-B): the pathfinder pod (the secret), a small breakable on the play plane like a small rock",
              "  # (20 HP); `secret: true` (B1's flag) frees the hidden crate on its break. Sprites debris-pathfinder-pod_0..15",
              "  # (32x32, its slow tumble) and debris-pathfinder-pod-break_0..7 (tools/art/jovian_units.py). It enters the",
              "  # top at t=146 and drifts down under bank-d (t 148.6, x 390): under the bank about t=147.2-148.8.",
              "  chunks:",
              "    pathfinder-pod: {size: [28, 28], hp: 20}",
              "  placed:",
              "    - {t: 146, x: 400, chunk: pathfinder-pod, drift: [-6, -90], secret: true}"]
    return "\n".join(lines) + "\n"


def write_proposal(pieces, placed, banks):
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces, placed, banks), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    data = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))
    return data["backdrop"]["pieces"], data["backdrop"]["placed"], data["backdrop"]["banks"], data["debris"]


# =========================================================================== composite, review, check

_CACHE = {}


def images(name, spec):
    if name not in _CACHE:
        n = spec.get("frames")
        _CACHE[name] = ([Image.open(OUT / f"{name}_{k}.png").convert("RGBA") for k in range(n)] if n
                        else [Image.open(OUT / f"{name}.png").convert("RGBA")])
    return _CACHE[name]


def tile_rows(name, offset):
    if name not in _CACHE:
        _CACHE[name] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
    arr = _CACHE[name]
    height = len(arr)
    rows = int(round(offset)) + SCREEN - 1 - np.arange(SCREEN)
    return arr[height - 1 - (rows % height)], rows


def section_at_row(layer, row):
    i = 0
    for k in range(1, len(SECTIONS)):
        if row >= seam(layer, k):
            i = k
    return i


def layer_tiles(layer, t):
    """The screen's rows of a layer at t, each from its section's tile set (the game's seams)."""
    off = scroll(t) * FACTORS[layer]
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for i, sec in enumerate(SECTIONS):
        names = [n for n in sec["tiles"] if (n in DEEP and layer == "deep") or (n in LOW and layer == "low-air")
                 or (n == HIGH and layer == "high-air")]
        if not names:
            continue
        arr, rows = tile_rows(names[0], off)
        sel = np.array([section_at_row(layer, r) == i for r in rows])
        out[sel] = arr[sel]
    return Image.fromarray(out, "RGBA")


def blit(img, p, pieces, t, layer, glow=False):
    spec = pieces[p["piece"] + ("-glow" if glow else "")] if glow else pieces[p["piece"]]
    if spec["layer"] != layer:
        return img
    name = p["piece"] + ("-glow" if glow else "")
    ims = images(name, spec)
    frame = ims[int(t * spec.get("fps", 0)) % len(ims)] if spec.get("frames") else ims[0]
    centre = scroll(p["t"]) * FACTORS[layer] + SCREEN / 2
    y_down = SCREEN - (centre - scroll(t) * FACTORS[layer])
    if y_down - frame.height / 2 > SCREEN or y_down + frame.height / 2 < 0:
        return img
    return l23kit.add_c(img, frame, p["x"], y_down) if glow else l23kit.paste_c(img, frame, p["x"], y_down)


def load_held(name):
    try:
        return l23kit.load(name)
    except FileNotFoundError:
        return None


def compose(t, pieces, placed, banks, debris=None, units=True, difficulty="medium"):
    """The screen at t (480x540): deep tiles and pieces (glows added), low-air tiles and the harvesters, the play
    plane's stand-ins, the banks over the bodies, then the bullets and the ship above, the high-air ice haze."""
    img = layer_tiles("deep", t)
    for p in placed:
        img = blit(img, p, pieces, t, "deep")
        if p["piece"] + "-glow" in pieces:
            img = blit(img, p, pieces, t, "deep", glow=True)
    img.alpha_composite(layer_tiles("low-air", t))
    if units:
        img = low_air_stand_ins(img, t)
        img = air_stand_ins(img, t, debris)
    for b in banks:
        if b.get("skip") and difficulty in b["skip"]:
            continue
        x0, y0, x1, y1 = bank_box(b, t)
        if y1 <= 0 or y0 >= SCREEN:
            continue
        sp = images(b["piece"], pieces[b["piece"]])[0]
        img = l23kit.paste_c(img, sp, b["x"], SCREEN - (y0 + y1) / 2)
    if units:
        img = above_banks(img, t)
    img.alpha_composite(layer_tiles("high-air", t))
    return img


HARVESTER_PAIRS = (72.0, 86.0, 100.0, 114.0)


def low_air_stand_ins(img, t):
    """The harvester pairs (tools/art/jovian_units.py's held frames): from the top 160 px apart, down at 50 px/s,
    30 px/s sideways mirrored, their plumes under them."""
    hv, hl = load_held("harvester"), load_held("harvester-left")
    pv, pl = load_held("harvester-plume"), load_held("harvester-left-plume")
    if not hv:
        return img
    for t0 in HARVESTER_PAIRS:
        u = t - t0
        if u < 0 or u > 13:
            continue
        y = -30 + 50 * u
        for side, (body, plume) in ((-1, (hl, pl)), (1, (hv, pv))):
            x = 240 + side * 80 + side * 30 * u * 0.5
            if plume:
                img = l23kit.paste_c(img, plume[int(u * 6) % len(plume)], x, y)
            img = l23kit.paste_c(img, body[0], x, y)
    return img


def air_stand_ins(img, t, debris):
    """Bodies on the play plane: a Talon line or a Chimera wing by the wave table's times, the pod."""
    talon = load_held("talon")
    chim = load_held("chimera") or talon
    if talon:
        for t0, kind, n in ((6, "talon", 4), (28, "talon", 4), (34, "chimera", 2), (45, "chimera", 5),
                            (57, "talon", 4), (63, "chimera", 6), (81, "chimera", 8), (98, "chimera", 6),
                            (117, "talon", 5), (120, "chimera", 8), (135, "chimera", 6), (149, "talon", 5),
                            (169, "chimera", 7)):
            u = t - t0
            if not 0 <= u <= 6:
                continue
            frames = chim if kind == "chimera" else talon
            for k in range(n):
                x = 240 + (k - (n - 1) / 2) * 52
                y = -20 + 150 * u - abs(k - (n - 1) / 2) * 26 - 60 * max(0, u - 2.4) ** 2
                if -30 < y < SCREEN + 30:
                    img = l23kit.paste_c(img, frames[0], x, y)
    if debris:
        pod = load_held("debris-pathfinder-pod")
        for row in debris["placed"]:
            u = t - row["t"]
            if pod and 0 <= u < 8:
                x = row["x"] + row["drift"][0] * u
                y = -20 - row["drift"][1] * u
                img = l23kit.paste_c(img, pod[int(u * 4) % len(pod)], x, y)
    return img


def above_banks(img, t):
    """Above the banks: a few enemy bullets (dark outline) and the ship."""
    ship = artkit.load_frames("ship")
    needle, orb = load_held("needle"), load_held("orb")
    if 40 < t < 190 and needle and orb:
        for k in range(4):
            img = l23kit.paste_c(img, needle[0], 196 + 22 * k, 250 + 34 * k)
        img = l23kit.paste_c(img, orb[0], 330, 300)
    if ship:
        img = l23kit.paste_c(img, ship[2], 240, 470)
    return img


SHOTS = [(0.5, "THE DIVE: JUPITER'S LIMB, THE CARRIER GROUP"), (14.0, "THE DIVE: THE BANDS FAR BELOW"),
         (29.0, "INTO THE BANDS: THE FIRST BANK"), (46.0, "A CHIMERA WING UNDER A BANK'S EDGE"),
         (104.0, "HARVESTER FIELDS: LAMPREYS OUT OF A BANK"), (121.5, "THE CHIMERA SWARM UNDER THE BANKS"),
         (148.0, "STORM EDGE: THE POD UNDER A BANK"), (END - 0.5, "THE END: THE BANDS THIN")]


def explosion_panel(pieces):
    """The explosion read (brief: report only): explosion-small (additive, the Earth-air ladder) at 1x and 2x over
    each section's band at its brightest window and over a bank on it; the measured lift (the frame's mean luminance
    over its ground) printed under each."""
    try:
        boom = artkit.load_frames("explosion-small")
    except Exception:
        return None, []
    grounds, darks = [], []
    for name in DEEP:
        tl = np.array(Image.open(OUT / f"{name}.png").convert("RGB")).astype(np.float64)
        lum = tl @ np.array((0.299, 0.587, 0.114))
        win = np.array([lum[y:y + 64, 200:280].mean() for y in range(0, len(lum) - 64, 8)])
        y, yd = int(np.argmax(win) * 8), int(np.argmin(win) * 8)
        grounds.append((name.upper(), Image.fromarray(tl[y:y + 64, 200:280].astype(np.uint8)).convert("RGBA")))
        darks.append((name.upper() + " DARKEST", Image.fromarray(tl[yd:yd + 64, 200:280].astype(np.uint8)).convert("RGBA")))
    bank_img = images("bank-c", pieces["bank-c"])[0]
    with_bank = []
    for lab, g in grounds:
        gb = g.copy()
        gb.alpha_composite(bank_img.crop((80, 32, 160, 96)))
        with_bank.append((lab + " + BANK", gb))
    cells, reads = [], []
    for lab, g in grounds + with_bank:
        row = []
        for f in (2, 4, 7):
            c = g.copy()
            a = np.array(c).astype(np.float64)
            e = np.array(boom[f]).astype(np.float64)
            ox, oy = (80 - e.shape[1]) // 2, (64 - e.shape[0]) // 2
            a[oy:oy + e.shape[0], ox:ox + e.shape[1], :3] += e[..., :3]
            c = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
            row.append(c)
        base = np.array(g)[..., :3].astype(np.float64) @ np.array((0.299, 0.587, 0.114))
        peak = np.array(row[1])[..., :3].astype(np.float64) @ np.array((0.299, 0.587, 0.114))
        core = (slice(22, 42), slice(30, 50))
        lift = peak[core].mean() - base[core].mean()
        clip = float((np.array(row[1])[..., :3][core].min(axis=-1) >= 250).mean())
        reads.append((lab, base.mean(), lift, clip))
        cells.append((lab, row))
    for lab, g in darks:                                         # numbers only: the same frame over the darkest window
        a = np.array(g).astype(np.float64)
        e = np.array(boom[4]).astype(np.float64)
        ox, oy = (80 - e.shape[1]) // 2, (64 - e.shape[0]) // 2
        base = a[..., :3] @ np.array((0.299, 0.587, 0.114))
        a[oy:oy + e.shape[0], ox:ox + e.shape[1], :3] += e[..., :3]
        peak = np.clip(a[..., :3], 0, 255) @ np.array((0.299, 0.587, 0.114))
        core = (slice(22, 42), slice(30, 50))
        reads.append((lab, base.mean(), peak[core].mean() - base[core].mean(),
                      float((np.clip(a[..., :3], 0, 255)[core].min(axis=-1) >= 250).mean())))
    return cells, reads


def review():
    pieces, placed, banks, debris = load_proposal()
    shots = [(compose(t, pieces, placed, banks, debris), lab) for t, lab in SHOTS]
    cols = 4
    sw, sh = W // 2, SCREEN // 2
    rows_n = -(-len(shots) // cols)
    cells, reads = explosion_panel(pieces)
    expl_h = 0 if not cells else 24 + 4 * (64 + 14) + 60
    pieces_h = 230
    tile_h = 1440 // 6 + 40
    sheet = raster.sheet(16 + cols * (sw + 12), 70 + rows_n * (sh + 26) + 410 + pieces_h + tile_h + expl_h + 40,
                         "LEVEL 36 BACKDROP (FINAL R74): CLOUD DIVERS",
                         f"PRODUCTION ART, {BATCH.upper()} - R74")
    raster.draw_text(sheet, 16, 38, "EIGHT SCREENS AT HALF SIZE WITH STAND-INS (TALONS, CHIMERAS, THE HARVESTER PAIRS "
                                    "AND THE POD FROM THEIR HELD FRAMES; BULLETS AND THE SHIP ABOVE THE BANKS)",
                     raster.LABEL)
    for i, (im, lab) in enumerate(shots):
        x, y = 16 + (i % cols) * (sw + 12), 56 + (i // cols) * (sh + 26)
        sheet.alpha_composite(im.resize((sw, sh), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + sh + 4, lab, raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 26) + 6
    worst, wt, longest, _ = bank_measures(banks)
    eworst, ewt, _, _ = bank_measures(banks, "easy")
    raster.draw_text(sheet, 16, y, f"THE BANKS (CREAM VEIL, ALPHA AT MOST {BANK_ALPHA:.2f}: A BODY UNDER ONE AT ~35 %) - "
                                   f"MEASURED: AT MOST {worst * 100:.1f} % OF THE FIELD (T={wt:.1f}; EASY {eworst * 100:.1f} %), "
                                   f"LONGEST OVER THE SHIP'S BAND {longest:.2f} S", raster.LABEL)
    plate = layer_tiles("deep", 104.0)
    for k, name in enumerate(BANKS):
        bi = images(name, pieces[name])[0]
        c = plate.crop((0, 0, bi.width + 8, bi.height + 8)).copy()
        c.alpha_composite(bi, (4, 4))
        x0, y0 = 16 + (k % 3) * 340, y + 16 + (k // 3) * 196
        sheet.alpha_composite(c, (x0, y0))
        raster.draw_text(sheet, x0, y0 + c.height + 2, f"{name.upper()} {bi.width}X{bi.height}", raster.LABEL_DIM)
    y += 410
    y += 300
    raster.draw_text(sheet, 16, y, "THE DEEP PIECES AT 1X: THE CARRIER GROUP AND ITS LIGHTS (STROBE FRAME 0), THE STORM "
                                   "WALL AND A LIGHTNING FRAME (ADDED)", raster.LABEL)
    x = 16
    for name, glow_frame in (("carrier-group", 0), ("storm-wall", 0), ("storm-wall", 8)):
        body = images(name, pieces[name])[0]
        glow_img = images(name + "-glow", pieces[name + "-glow"])[glow_frame]
        c = layer_tiles("deep", 140.0).crop((0, 0, body.width, min(body.height, 200)))
        c.alpha_composite(body.crop((0, 0, body.width, c.height)))
        c = l23kit.add_c(c, glow_img.crop((0, 0, body.width, c.height)), body.width / 2, c.height / 2)
        sheet.alpha_composite(c, (x, y + 16))
        x += c.width + 14
    y += pieces_h
    raster.draw_text(sheet, 16, y, "THE TILE SETS (SIXTH SIZE): DIVE-BANDS, CREAM-BANDS, RUST-BAND, STORM-EDGE; "
                                   "CLOUD-TOPS, CLOUD-WISPS, ICE-HAZE (ON A GREY PLATE)", raster.LABEL)
    x = 16
    for name in list(DEEP) + list(LOW) + [HIGH]:
        tl = Image.open(OUT / f"{name}.png").convert("RGBA")
        small = tl.resize((tl.width // 6, tl.height // 6), Image.BOX)
        pl = Image.new("RGBA", small.size, (60, 64, 76, 255))
        pl.alpha_composite(small)
        sheet.alpha_composite(pl, (x, y + 16))
        x += small.width + 10
    y += tile_h
    if cells:
        raster.draw_text(sheet, 16, y, "THE EXPLOSION READ (REPORT ONLY): EXPLOSION-SMALL (ADDITIVE) FRAMES 2/4/7 AT 1X "
                                       "OVER EACH SECTION'S BRIGHTEST WINDOW, AND OVER A BANK ON IT", raster.LABEL)
        for k, (lab, row) in enumerate(cells):
            cx = 16 + (k // 4) * 500
            cy = y + 18 + (k % 4) * (64 + 14)
            for j, c in enumerate(row):
                sheet.alpha_composite(c, (cx + j * 84, cy))
            b = reads[k]
            raster.draw_text(sheet, cx + 256, cy + 20, lab, raster.LABEL_DIM)
            raster.draw_text(sheet, cx + 256, cy + 34, f"GROUND {b[1]:.0f} LIFT {b[2]:+.0f}", raster.LABEL_DIM)
            raster.draw_text(sheet, cx + 256, cy + 46, f"CORE WHITE {b[3] * 100:.0f} %", raster.LABEL_DIM)
    png, gif = artkit.review_paths(CONCEPT, "backdrop")
    sheet.convert("RGB").save(png, optimize=True)
    frames = [compose(100.0 + i / 10, pieces, placed, banks, debris).resize((sw, sh), Image.BOX) for i in range(100)]
    import enemies_r60 as e60  # noqa: E402
    size = e60.rig.write_gif(frames, gif, fps=10, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")
    for lab, base, lift, clip in reads:
        print(f"explosion over {lab}: ground {base:.0f}, peak lift {lift:+.0f}, core clipped {clip * 100:.0f} %")


def wrap_score(a):
    lum = a[..., :3].astype(float) @ np.array((0.299, 0.587, 0.114))
    h = len(lum)

    def change(i, j):
        d = np.sort(np.abs(lum[i % h] - lum[j % h]))
        return d[:int(len(d) * 0.9)].mean()
    ref = sorted(change(k, k + 1) for k in range(h - 40, h + 40) if k < h - 4 or k > h + 2)
    return max(change(h - 2, h - 1), change(h - 1, 0), change(0, 1)) / max(ref[len(ref) // 2], 0.5)


def wrap_score_alpha(a):
    """A transparent tile's wrap: the premultiplied change across the wrap vs. the mean change between rows."""
    pm = (a[..., :3].astype(float) @ np.array((0.299, 0.587, 0.114))) * a[..., 3] / 255
    rows = np.abs(np.diff(pm, axis=0)).mean(axis=1)
    return np.abs(pm[-1] - pm[0]).mean() / max(np.percentile(rows, 95), 0.05)


def checker_share(img):
    """DitherGuardTest.checkerCells: the share of visible pixels that are a 1 px checker cell."""
    a = np.array(img.convert("RGBA")).astype(np.int64)
    c = (a[..., 0] << 24) | (a[..., 1] << 16) | (a[..., 2] << 8) | a[..., 3]
    vis = a[1:-1, 1:-1, 3] > 0
    mid = c[1:-1, 1:-1]
    left, right, up, down = c[1:-1, :-2], c[1:-1, 2:], c[:-2, 1:-1], c[2:, 1:-1]
    chk = vis & (left == right) & (left != mid) & (up == down) & (up != mid)
    return int(vis.sum()), int(chk.sum())


def seam_jump(layer, i):
    """The mean luminance change across section i's seam on the screen vs. the rows round it."""
    t = STARTS[i] + 0.4
    arr = np.array(layer_tiles(layer, t)).astype(np.float64)
    lum = arr[..., :3] @ np.array((0.299, 0.587, 0.114))
    off = scroll(t) * FACTORS[layer]
    y = int(round(SCREEN - (seam(layer, i) - off))) - 1
    if not 2 <= y < SCREEN - 3:
        return 0.0
    d = lambda a, b: np.abs(lum[a] - lum[b]).mean()  # noqa: E731
    ref = np.median([d(k, k + 1) for k in range(max(0, y - 30), min(SCREEN - 1, y + 30))])
    return d(y, y + 1) / max(ref, 0.5)


def check():
    """The deep tiles opaque and wrapping (BackdropSeamsTest's measure), the section seams invisible, sizes as the
    proposal, Source chunks, no checker (DitherGuardTest's measure under 5 %), the deep pieces' edges never on the
    screen opaque, at most two animated pieces at once, the banks: the cap (a third, easy a quarter), the ship's band
    (2 s), never two banks overlapping (drawn once), none on the screen t=31-42, banks at the top at t=45, 103, 120."""
    problems = []
    pieces, placed, banks, _ = load_proposal()
    for name in DEEP:
        a = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
        if wrap_score(a) > 1.5:
            problems.append(f"{name}: wrap score {wrap_score(a):.2f}")
        if (a[..., 3] < 255).any():
            problems.append(f"{name}: not opaque")
    for name in list(LOW) + [HIGH]:
        a = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
        if wrap_score_alpha(a) > 1.5:
            problems.append(f"{name}: wrap score {wrap_score_alpha(a):.2f}")
    for layer, i in [("deep", k) for k in range(1, len(SECTIONS))] + [("low-air", 1), ("high-air", 1)]:
        j = seam_jump(layer, i)
        if j > 2.0:
            problems.append(f"{layer} seam {i}: a visible jump ({j:.2f}x the rows round it)")
    for name, spec in pieces.items():
        n = spec.get("frames")
        files = [OUT / f"{name}_{k}.png" for k in range(n)] if n else [OUT / f"{name}.png"]
        for f in files:
            im = Image.open(f)
            if list(im.size) != list(spec["size"]):
                problems.append(f"{f.name}: {im.size} vs {spec['size']}")
    vis = chk = 0
    for f in sorted(OUT.glob("*.png")):
        im = Image.open(f)
        if im.text.get(artkit.SOURCE_KEY) != SOURCE:
            problems.append(f"{f.name}: no Source chunk")
        v, c = checker_share(im)
        if v and c / v >= 0.05:
            problems.append(f"{f.name}: {c / v * 100:.1f} % checker")
        vis, chk = vis + v, chk + c
    for name in BANKS:
        a = np.array(Image.open(OUT / f"{name}.png"))[..., 3]
        if a[0].any() or a[-1].any() or a[:, 0].any() or a[:, -1].any():
            problems.append(f"{name}: alpha on its edge (the cloud spills past its rectangle)")
        ys, xs = np.nonzero(a)
        if xs.min() > 12 or xs.max() < a.shape[1] - 13 or ys.min() > 16 or ys.max() < a.shape[0] - 17:
            problems.append(f"{name}: the cloud does not fill its rectangle ({xs.min()}-{xs.max()}, {ys.min()}-{ys.max()})")
        if a.max() > round(BANK_ALPHA * 255) + 1:
            problems.append(f"{name}: alpha {a.max()} over the veil's {BANK_ALPHA}")
    for step in range(int((END + 4) * 10)):
        t = step / 10
        animated = set()
        for p in placed:
            for name in (p["piece"], p["piece"] + "-glow"):
                spec = pieces.get(name)
                if not spec:
                    continue
                f = FACTORS[spec["layer"]]
                y = SCREEN - (scroll(p["t"]) * f + SCREEN / 2 - scroll(t) * f)
                if y + spec["size"][1] / 2 > 0 and y - spec["size"][1] / 2 < SCREEN and spec.get("frames"):
                    animated.add(name)
        if len(animated) > 2:
            problems.append(f"t={t}: {sorted(animated)} animated at once")
            break
    for p in placed:
        spec = pieces[p["piece"]]
        im = np.array(images(p["piece"], spec)[0])[..., 3]
        edges = {"top": im[0].any(), "bottom": im[-1].any(), "left": im[:, 0].any(), "right": im[:, -1].any()}
        for step in range(0, int(END * 10)):
            t = step / 10
            f = FACTORS[spec["layer"]]
            yc = SCREEN - (scroll(p["t"]) * f + SCREEN / 2 - scroll(t) * f)
            top, bottom = yc - spec["size"][1] / 2, yc + spec["size"][1] / 2
            left, right = p["x"] - spec["size"][0] / 2, p["x"] + spec["size"][0] / 2
            on = bottom > 0 and top < SCREEN
            if on and ((edges["top"] and 0 <= top < SCREEN) or (edges["bottom"] and 0 < bottom <= SCREEN)
                       or (edges["left"] and 0 <= left < W) or (edges["right"] and 0 < right <= W)):
                problems.append(f"{p['piece']}: an opaque edge on the screen at t={t}")
                break
    worst, wt, longest, windows = bank_measures(banks)
    eworst, ewt, _, _ = bank_measures(banks, "easy")
    if worst > 1 / 3:
        problems.append(f"banks: {worst * 100:.1f} % of the field at t={wt:.2f} (a third at most)")
    if eworst > 1 / 4:
        problems.append(f"banks, easy: {eworst * 100:.1f} % at t={ewt:.2f} (a quarter at most)")
    if longest > 2.0:
        problems.append(f"banks: {longest:.2f} s over the ship's band (2 s at most)")
    for t0, t1, i in windows:
        if t1 > 31 and t0 < 42:
            problems.append(f"banks[{i}] on the screen t={t0:.1f}-{t1:.1f}: over the Chimera pair's window (31-42)")
    for i, a in enumerate(banks):
        for b in banks[i + 1:]:
            ya = scroll(a["t"]) * FACTORS["high-air"]
            yb = scroll(b["t"]) * FACTORS["high-air"]
            if abs(ya - yb) < (a["h"] + b["h"]) / 2 and abs(a["x"] - b["x"]) < (a["w"] + b["w"]) / 2:
                problems.append(f"banks at t={a['t']} and t={b['t']} overlap (a veil drawn twice)")
    for at, what in ((45.2, "the t=45 Chimera wing"), (103.4, "the t=103 Lampreys"), (120.4, "the t=120 swarm")):
        if not any(bank_box(b, at)[3] > SCREEN - 20 and bank_box(b, at)[1] < SCREEN - 40 for b in banks):
            problems.append(f"no bank at the top edge at t={at} ({what} enter under a bank)")
    print(f"banks: at most {worst * 100:.1f} % of the field (t={wt:.2f}), easy {eworst * 100:.1f} % (t={ewt:.2f}), "
          f"longest over the ship's band {longest:.2f} s; checker {chk / max(vis, 1) * 100:.2f} % of {vis} px")
    for p in problems:
        print("PROBLEM", p)
    print("check:", "ok" if not problems else f"{len(problems)} problems")
    return not problems


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--review" in args:
        review()
        sys.exit(0)
    built = build_all()
    write_proposal(*built)
    if "--proposal" not in args:
        review()
