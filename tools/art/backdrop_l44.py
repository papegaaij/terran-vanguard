#!/usr/bin/env python3
"""Production art: the Level 44 backdrop, Crossing (design/campaign/act-7-beyond-the-gate/level-44-crossing; M11 part B
batch, concept round 89, step ART-B): the event plane inside the gate.

Straight to production from round 84's chosen look: Q10 b's **one Vrell palette** at its lightest, Level 44's light
(design/world/vrell-space, *Light per level*: pale violet-white to mint with dark violet streaks, the act's one light
field; the brightest band below a bullet core's brightness, ``--check`` measures it), round 84's event plane
(tools/concept/enemies_r84.py ``EventPlane``, tools/concept/act7_r84.py ``plane_rgb``: the same ramp and streaks) made
**tileable in x**: the distorted space slides every layer sideways at its parallax factor (BackdropTiles' lateral
shift, ``deep`` least; the tiles repeat across the field), so every tile set here wraps in x as well as in y. No
surface and no stars. Vacuum: no haze. Every field is a float written unquantised (far more than 32 steps, no ordered
dither: DitherGuardTest's measure); the translucent layers fade to 0 at the rows where their section's tiles start and
end (``clear_rows``), so no straight cut shows.

The layers (``scroll_factors``): ``deep`` 0.15 (one continuous field periodic in x, its look changing by section and cut
into the sections' tile sets at the rows the game shows them, ``LevelData.seam``); ``low-air`` 1.3 (section 2's dark
ribbons); ``high-air`` 1.7 (section 1's thin dark streaks, section 4's fast streaks).

Tile sets (480 wide, wrapping in x and y):
  plane-1 .. plane-5   ``deep``, opaque: 1 the Far Mouth, the plane's broad violet folds; 2 the Current, the folds
                       leaning with it (sheared 25 degrees); 3 the Shoal, the streaks running across the screen (sheared
                       -40 degrees); 4 the Narrows, the folds darker at the sides and the plane at its brightest in the
                       middle (a band that slides with the drift); 5 Out of the Gate, Vrell space's violet and teal
                       nebula behind the curtain (the field blends from the plane over 300 rows round the seam)
  ribbons              ``low-air``, 480x1024, section 2: dark violet ribbons crossing the field, at most 45 % opaque
  thin-streaks         ``high-air``, 480x1024, section 1: thin dark streaks along the flow, at most 30 %
  fast-streaks         ``high-air``, 480x1024, section 4: longer, denser dark streaks, at most 35 %
Pieces (``deep``, in draw order):
  beyond               800xH: Vrell space's nebula behind the curtain (the field's section 5 style at the rows it
                       covers), from t=252, 800 wide so a sideways slide of +-160 px at 0.15 stays covered
  curtain-left, curtain-right   368xH: the event plane's two halves cut from the Narrows' field at exactly the rows and
                       columns they cover (closed they are the tile itself, so they appear at t=252 without a pop), each
                       opaque 12 px past the middle with a soft wobbling inner edge; they move with the layer and are
                       drawn apart over t=255-260 (the parting curtain). H = the deep rows from the screen at t=252 to
                       the end (+ margins)
  far-reef             240x120: a hive reef far ahead (Level 45): a dark teal-black chitin mass with faint mint veins,
                       held at the top from t=258
  plane-warp           256x256 (a data image, not drawn): the slow warp's UV map for BackdropAtmosphere: red and green
                       = the x and y displacement (128 = none, +-127 = +-8 px), a smooth field periodic in both
                       directions; sampled with a 0.1 Hz drift it gives the plane's warp (<= 0.5 Hz)

Section times: the level's data.yaml when it has sections, else this script's own proposal's, else the README's Layout
(130 px/s; 110 in section 5, eased over 2 s). ``--proposal`` writes the ``backdrop:`` block and the sections to
concept/backdrop-proposal.yaml (step B2 places them; the drift's profile and the reversals are the level's ``current``,
not the backdrop's). **Re-run after the data moves a section.**

Review: concept/backdrop-final-r89-a.png (the five sections' screens with stand-ins: Rift Skaters and their rings, the
ship, Vrell bullets over the brightest band; the tiles at a slide of 0 and half a width; the pieces) and .gif (a drift
swing: the layers sliding at their factors).
Run: python3 tools/art/backdrop_l44.py [--proposal | --review | --check]   (all: about 1 min)
"""
import sys
from pathlib import Path

import numpy as np
import yaml
from PIL import Image

import artkit
from artkit import DESIGN, ROOT, TAU

import jovkit  # noqa: E402  (the clock, cut_tile, clear_rows, the checks' measures)
import l23kit  # noqa: E402
from jovkit import additive_rgb, checker_share, clear_rows, cut_tile, rgba, smoothstep, wrap_score  # noqa: E402

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "concept"))
import act7_r84 as a84  # noqa: E402  (concept: the palette, imported unchanged)
from render import raster  # noqa: E402

SCRIPT = "backdrop_l44.py"
BATCH = "M11 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r89"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-7-beyond-the-gate" / "level-44-crossing"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-44"
W, SCREEN, SPEED = 480, 540, 130.0
FACTORS = {"deep": 0.15, "low-air": 1.3, "high-air": 1.7}

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
OWN = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8")) if PROPOSAL.exists() else None
PROPOSED_SECTIONS = [
    {"name": "The Far Mouth", "end": 40, "atmosphere": "clear", "tiles": ["plane-1", "thin-streaks"]},
    {"name": "The Current", "end": 106, "atmosphere": "clear", "tiles": ["plane-2", "ribbons"]},
    {"name": "The Shoal", "end": 177, "atmosphere": "clear", "tiles": ["plane-3"]},
    {"name": "The Narrows", "end": 255, "atmosphere": "clear", "tiles": ["plane-4", "fast-streaks"]},
    {"name": "Out of the Gate", "end": 264, "speed": 110, "ramp": 2, "atmosphere": "clear", "tiles": ["plane-5"]},
]
SECTIONS = (DATA["sections"] if DATA and DATA.get("sections") else
            OWN["sections"] if OWN and OWN.get("sections") else PROPOSED_SECTIONS)
BASE_SPEED = float(DATA.get("scroll_speed", SPEED)) if DATA else SPEED
CLOCK = jovkit.Clock(SECTIONS, BASE_SPEED, FACTORS, SCREEN)
END = CLOCK.end


# M11 part B, step B2: the level's two reversals each split a section into several data sections (the reversed run,
# then the ease forward) that keep its tile sets; the art works on the style sections, each begun by the first data
# section that names its deep tile set (its seam is that section's; the scroll runs on all of them).
def _deep_tile(sec):
    return next((n for n in sec.get("tiles") or [] if n.startswith("plane-")), None)


FIRST = [i for i, sec in enumerate(SECTIONS) if i == 0 or _deep_tile(sec) != _deep_tile(SECTIONS[i - 1])]
SECTIONS = [dict(SECTIONS[a], end=SECTIONS[(FIRST + [len(SECTIONS)])[k + 1] - 1]["end"])
            for k, a in enumerate(FIRST)]
STARTS = [CLOCK.starts[i] for i in FIRST]
scroll = CLOCK.scroll


def seam(layer, k):
    """The row where style section k's tile sets begin: its first data section's seam (LevelData.seam)."""
    return CLOCK.seam(layer, FIRST[k])


def section_at_row(layer, row):
    i = 0
    for k in range(1, len(SECTIONS)):
        if row >= seam(layer, k):
            i = k
    return i


DEEP = [f"plane-{i + 1}" for i in range(len(SECTIONS))]
SEED = 4401
# Q10 b at its lightest (the event plane's ramp, round 84) with the palette's mint in the light folds
PLANE = [(0.0, (70, 46, 108)), (0.3, (120, 96, 168)), (0.55, (156, 136, 198)), (0.8, (196, 186, 226)),
         (1.0, (214, 214, 234))]
MINT = np.array((176, 226, 214.0))
STREAK = np.array((44, 24, 74.0))
LIMIT_LUMA = 228.0                                     # the brightest band: below a bullet core (white, 255)


# =========================================================================== periodic noise (x period 480)

def pnoise(w, h, cx, cy, seed):
    """Smooth value noise periodic in x over ``w`` (cx must divide w) and in y over ``h`` (cy must divide h)."""
    rng = np.random.default_rng(seed)
    gw, gh = w // cx, h // cy
    grid = rng.random((gh, gw))
    ys, xs = np.arange(h) / cy, np.arange(w) / cx
    y0, x0 = np.floor(ys).astype(int), np.floor(xs).astype(int)
    fy, fx = ys - y0, xs - x0
    fy, fx = fy * fy * (3 - 2 * fy), fx * fx * (3 - 2 * fx)
    a = grid[y0 % gh][:, x0 % gw]
    b = grid[y0 % gh][:, (x0 + 1) % gw]
    c = grid[(y0 + 1) % gh][:, x0 % gw]
    d = grid[(y0 + 1) % gh][:, (x0 + 1) % gw]
    top = a + (b - a) * fx[None, :]
    bot = c + (d - c) * fx[None, :]
    return top + (bot - top) * fy[:, None]


def pfbm(w, h, cx, cy, seed, octaves=4):
    out, amp, total = np.zeros((h, w)), 1.0, 0.0
    for o in range(octaves):
        out += amp * pnoise(w, h, max(1, cx >> o), max(1, cy >> o), seed + 101 * o)
        total += amp
        amp *= 0.5
    out /= total
    lo, hi = out.min(), out.max()
    return (out - lo) / max(hi - lo, 1e-9)


def shear(a, tan_a):
    """Each row shifted sideways by row x tan_a px (wrapping in x, linear between px): leaning streaks that still
    wrap in x."""
    h, w = a.shape[:2]
    out = np.empty_like(a)
    xs = np.arange(w)
    for j in range(h):
        s = j * tan_a
        i0 = int(np.floor(s))
        f = s - i0
        r0 = np.roll(a[j], i0, axis=0)
        r1 = np.roll(a[j], i0 + 1, axis=0)
        out[j] = r0 * (1 - f) + r1 * f
    del xs
    return out


def streaks(w, h, seed, along=24, across=6, lean=0.0):
    """Streaks: noise stretched ``along``/``across`` (cells: along x 4 rows by across x 2 columns; both must divide
    the size at every octave for a tile that wraps), leaning ``lean`` (tan) by a shear."""
    n = pfbm(w, h, across * 2 if w % (across * 2) == 0 else across, along * 4, seed, 4)
    return shear(n, lean) if lean else n


# =========================================================================== the deep field

def deep_span():
    return -40, int(np.ceil(scroll(END + 4) * FACTORS["deep"] + SCREEN))


def plane_style(n, seed, kind, rows):
    """The plane's light for a section's style (rows y-up, 480 wide, periodic in x)."""
    h = n
    fold = pfbm(W, h, 160, 192, seed, 4)
    soft_ = pfbm(W, h, 96, 96, seed + 1, 3)
    mint = pfbm(W, h, 240, 384, seed + 2, 2)
    if kind == 1:
        st = streaks(W, h, seed + 3, along=40, across=8)
        v = 0.3 + 0.7 * fold
        dark = np.clip((st - 0.56) * 2.2, 0, 1) * 0.45
    elif kind == 2:
        st = streaks(W, h, seed + 3, along=36, across=6, lean=np.tan(np.radians(25)))
        f2 = shear(fold, np.tan(np.radians(25)))
        v = 0.28 + 0.72 * f2
        dark = np.clip((st - 0.52) * 2.4, 0, 1) * 0.55
    elif kind == 3:
        st = streaks(W, h, seed + 3, along=30, across=5, lean=np.tan(np.radians(-40)))
        v = 0.3 + 0.7 * (0.6 * fold + 0.4 * soft_)
        dark = np.clip((st - 0.5) * 2.6, 0, 1) * 0.6
    else:
        st = streaks(W, h, seed + 3, along=34, across=6, lean=np.tan(np.radians(10)))
        x = np.arange(W)[None, :]
        side = 0.5 - 0.5 * np.cos(TAU * x / W)                       # 0 at x 0/480 (the wrap), 1 in the middle
        v = (0.2 + 0.8 * fold) * (0.55 + 0.45 * side) + 0.25 * side
        dark = np.clip((st - 0.52) * 2.4, 0, 1) * (0.35 + 0.35 * (1 - side))
    v = np.clip(v, 0, 1)
    rgb = raster.ramp(PLANE, v)
    m = np.clip((mint - 0.55) * 2.5, 0, 1) * v
    rgb = rgb * (1 - 0.45 * m[..., None]) + MINT * 0.45 * m[..., None]
    return rgb * (1 - dark[..., None]) + STREAK * dark[..., None]


def nebula(n, seed):
    """Vrell space's violet and teal nebula (act7_r84.space_rgb's colours), periodic in x; faint stars."""
    rgb = np.zeros((n, W, 3)) + (6, 5, 12)
    rgb += np.clip((pfbm(W, n, 160, 160, seed + 1, 5) - 0.42) * 1.9, 0, 1)[..., None] * np.array((74, 28, 100))
    rgb += np.clip((pfbm(W, n, 120, 120, seed + 2, 5) - 0.55) * 2.2, 0, 1)[..., None] * np.array((8, 70, 78))
    rng = np.random.default_rng(seed + 3)
    for _ in range(int(n * W * 0.0012)):
        y, x = rng.integers(0, n), rng.integers(0, W)
        rgb[y, x] = np.maximum(rgb[y, x], np.array((235, 235, 255)) * (0.3 + 0.7 * rng.random() ** 2.5))
    return rgb


_FIELD = {}


def deep_styles():
    """The five styles over the whole deep span (rows y-up from r0), cached: the field and the curtain cut from them."""
    if not _FIELD:
        lo, hi = deep_span()
        r0, r1 = lo - 400, hi + 400
        n = (r1 - r0 + 191) // 192 * 192
        _FIELD["styles"] = [plane_style(n, SEED + 10 * k, k + 1, None) for k in range(4)] + [nebula(n, SEED + 50)]
        _FIELD["r0"], _FIELD["n"] = r0, n
    return _FIELD["styles"], _FIELD["r0"], _FIELD["n"]


def deep_field():
    """One continuous field: each section's style, blended over 300 rows round the seams 1-3; at seam 4 (the gate's
    exit) the nebula blends in only above the seam, so the rows below it are the Narrows' plane alone (the curtain's
    texture, cut from it, matches the tile exactly where it appears)."""
    styles, r0, n = deep_styles()
    rows = np.arange(n) + r0
    seams = [seam("deep", i) for i in range(1, len(SECTIONS))]
    weights = []
    for i in range(len(styles)):
        a = -1e9 if i == 0 else seams[i - 1]
        b = 1e9 if i == len(styles) - 1 else seams[i]
        oa = 150 if i < len(styles) - 1 else 0
        ob = 150 if i < len(styles) - 2 else 0
        wa = smoothstep(a - oa, a + 300 - oa, rows) if i else np.ones(n)
        wb = 1 - smoothstep(b - ob, b + 300 - ob, rows) if i < len(styles) - 1 else np.ones(n)
        weights.append(wa * wb)
    tot = np.sum(weights, axis=0)
    rgb = sum(st * (wt / tot)[:, None, None] for st, wt in zip(styles, weights))
    return np.clip(rgb, 0, 255), r0


def deep_tiles():
    field, r0 = deep_field()
    lo, hi = deep_span()
    bounds = [lo] + [seam("deep", i) for i in range(1, len(SECTIONS))] + [hi]
    return {name: cut_tile(field, r0, bounds[i], max(bounds[i + 1], bounds[i] + 1)) for i, name in enumerate(DEEP)}


# =========================================================================== the translucent layers

TILE_H = 1024


def streak_tile(seed, along, across, lean, peak, k, layer, section):
    """A translucent tile of dark violet streaks (alpha <= ``peak``), wrapping in x and y (the shear over the tile's
    height is a whole number of widths), clear at the rows where its section's tiles start and end."""
    st = streaks(W, TILE_H, seed, along=along, across=across, lean=lean)
    a = np.clip((st - k) / (1 - k), 0, 1) ** 1.5 * peak
    rows = [seam(layer, section), seam(layer, section + 1)] if section + 1 < len(SECTIONS) else [seam(layer, section)]
    a = a * clear_rows(TILE_H, rows, width=160)
    return rgba(np.ones((TILE_H, W, 3)) * STREAK, a)


def ribbons():
    lean = 480 * 1 / TILE_H                                           # one width over the tile: 25 degrees
    return streak_tile(SEED + 60, 64, 12, lean, 0.45, 0.58, "low-air", 1)


def thin_streaks():
    return streak_tile(SEED + 70, 32, 4, 0.0, 0.30, 0.66, "high-air", 0)


def fast_streaks():
    return streak_tile(SEED + 80, 64, 4, 0.0, 0.35, 0.6, "high-air", 3)


# =========================================================================== pieces

CURTAIN_W = 368
REEF = (240, 120)
WARP = 256
T_APPEAR = 252.0                     # the curtain halves and the nebula behind them appear (under the Narrows' rows)


def curtain_rows():
    """The deep rows the curtain spans: the screen's at T_APPEAR to the level's end (+16 px margins), height a
    multiple of 16."""
    f = FACTORS["deep"]
    rb = int(np.floor(scroll(T_APPEAR) * f)) - 16
    rt = int(np.ceil(scroll(END + 1) * f)) + SCREEN + 16
    h = (rt - rb + 15) // 16 * 16
    return rb, h


def cut_columns(style, rb, h, x_left, w):
    """The field's rows [rb, rb + h) (image top = highest row) and the columns of screen x ``x_left .. + w`` at a
    slide of 0 (the tiles' columns: x mod 480)."""
    _, r0, _ = deep_styles()
    block = style[rb - r0:rb - r0 + h][::-1]
    cols = (np.arange(w) + x_left) % W
    return block[:, cols]


def curtain(side):
    """One half of the parting plane, cut from the Narrows' field at the rows and columns it covers (closed, the two
    halves are the tile itself: no pop when they appear); its inner edge soft over 12 px with a slow wobble, hidden
    under the other half while closed (each half opaque 12 px past the middle)."""
    styles, _, _ = deep_styles()
    rb, h = curtain_rows()
    x_left = 240 + 24 - CURTAIN_W if side < 0 else 240 - 24
    rgb = cut_columns(styles[3], rb, h, x_left, CURTAIN_W)
    yy, xx = np.mgrid[0:h, 0:CURTAIN_W].astype(np.float64) + 0.5
    sx = xx + x_left                                                  # screen x when closed
    wob = 4 * np.sin(yy / 41.0 + (0.0 if side < 0 else 1.7)) + 2 * np.sin(yy / 13.0)
    edge = (252 + wob - sx) if side < 0 else (sx - 228 - wob)          # > 0 inside the half
    alpha = smoothstep(-12, 0, edge)
    return rgba(rgb, alpha)


def beyond():
    """Vrell space behind the curtain: the nebula (the field's section 5 style), 800 wide so a sideways slide of
    +-160 px at the deep factor stays covered."""
    styles, _, _ = deep_styles()
    rb, h = curtain_rows()
    return rgba(cut_columns(styles[4], rb, h, -160, 800), np.ones((h, 800)))


def far_reef(seed=SEED + 95):
    w, h = REEF
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    n = raster.fbm(w, h, 24, seed, octaves=4, period=False)
    top = h * 0.35 + 18 * np.sin(xx / 31.0) + 14 * (n[0][None, :] - 0.5) * 2
    ridge = smoothstep(-1, 1, yy - top)
    sides = smoothstep(0, 40, xx) * smoothstep(w, w - 40, xx) * smoothstep(h, h - 24, yy)
    shade = 0.2 + 0.25 * n + 0.2 * smoothstep(8, -4, yy - top - 6)
    rgb = raster.ramp([(0, (4, 6, 10)), (0.5, (10, 28, 30)), (1, (30, 70, 70))], np.clip(shade, 0, 1))
    vein = np.exp(-((((xx + 3 * np.sin(yy / 9.0)) % 23.0) - 11.5) / 0.9) ** 2) * smoothstep(top + 6, top + 30, yy)
    rgb += (vein * 0.5)[..., None] * np.array((80, 200, 170.0))
    return rgba(rgb, ridge * sides * 0.92)


def warp_map():
    dx = pfbm(WARP, WARP, 64, 64, SEED + 97, 3) - 0.5
    dy = pfbm(WARP, WARP, 64, 64, SEED + 98, 3) - 0.5
    rgb = np.dstack([128 + dx * 2 * 127, 128 + dy * 2 * 127, np.full((WARP, WARP), 128.0)])
    return Image.fromarray(np.clip(np.round(rgb), 0, 255).astype(np.uint8), "RGB").convert("RGBA")


# =========================================================================== build

def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    for name, img in deep_tiles().items():
        save(name, img)
    save("ribbons", ribbons())
    save("thin-streaks", thin_streaks())
    save("fast-streaks", fast_streaks())
    save("beyond", beyond())
    save("curtain-left", curtain(-1))
    save("curtain-right", curtain(1))
    save("far-reef", far_reef())
    save("plane-warp", warp_map())
    print(f"backdrop: {len(list(OUT.glob('*.png')))} files in {OUT.relative_to(ROOT)}")


TILES = {"ribbons": "low-air", "thin-streaks": "high-air", "fast-streaks": "high-air"}


def tile_heights():
    return {p.stem: Image.open(p).height for p in OUT.glob("*.png") if p.stem in DEEP or p.stem in TILES}


def pieces_spec():
    _, h = curtain_rows()
    return {"beyond": {"layer": "deep", "size": [800, h]}, "far-reef": {"layer": "deep", "size": list(REEF)},
            "curtain-left": {"layer": "deep", "size": [CURTAIN_W, h]},
            "curtain-right": {"layer": "deep", "size": [CURTAIN_W, h]}}


def hold_path(target_x, target_y, times, f):
    """A placement time and a path putting the piece's centre at screen (x0 + dx(t), target_y(t)); x0 = 240."""
    t_ref = times[0]
    pt = CLOCK.t_centre_at(t_ref, target_y(t_ref), f)

    def dy(t):
        return 270.0 + (scroll(t) - scroll(pt)) * f - target_y(t)
    return round(pt, 3), [[round(t, 3), round(target_x(t) - 240, 1), round(dy(t), 1)] for t in times]


def lerp(points):
    ts, ys = zip(*points)
    return lambda t: float(np.interp(t, ts, ys))


def placements():
    """Draw order: the nebula behind, the reef, the two halves. The halves and the nebula move with the deep layer
    (no hold: their texture is the layer's own rows), parked 3000 px to the right until T_APPEAR."""
    f = FACTORS["deep"]
    s5 = STARTS[4]
    rb, h = curtain_rows()
    pt = CLOCK.t_for_layer_row(rb + h / 2, f)
    out = []
    park = [round(T_APPEAR - 0.05, 3), 3000, 0]
    out.append({"piece": "beyond", "t": pt, "x": 240, "path": [park, [T_APPEAR, 0, 0]],
                "note": f"section 5: Vrell space's nebula behind the curtain, from t={T_APPEAR:g} (hidden until it parts)"})
    # M11 part B, step B2: the reef sinks in once the curtain has parted (t=260-263), so at most two elements move at
    # once (the art direction's motion budget, BackdropCheck: the two halves parting, then the reef).
    t, path = hold_path(lambda t: 240.0, lerp([(s5 + 5.0, -70.0), (s5 + 8.0, 90.0), (END, 110.0)]),
                        [round(s5 + 5.0, 3), round(s5 + 8.0, 3), END], f)
    out.append({"piece": "far-reef", "t": t, "x": 240, "path": [[round(s5 + 4.95, 3), 3000, path[0][2]]] + path,
                "note": "section 5: a hive reef far ahead (Level 45), sinking in from the top once the curtain has parted"})
    ease = [0.0, 0.1, 0.25, 0.45, 0.65, 0.82, 0.94, 1.0]
    for side, name in ((-1, "curtain-left"), (1, "curtain-right")):
        x0 = 240 + 24 - CURTAIN_W / 2 if side < 0 else 240 - 24 + CURTAIN_W / 2
        pts = [park, [T_APPEAR, 0, 0], [round(s5, 3), 0, 0]]
        pts += [[round(s5 + 5.0 * k / 7, 3), round(side * 420 * e, 1), 0] for k, e in enumerate(ease) if k]
        out.append({"piece": name, "t": pt, "x": round(x0, 1), "path": pts,
                    "note": f"section 5: the plane's {'left' if side < 0 else 'right'} half, cut from the Narrows' "
                            f"rows (closed it is the tile itself), parting over t={s5:g}-{s5 + 5:g}"})
    return out


def fmt(v):
    if isinstance(v, list):
        return "[" + ", ".join(fmt(x) for x in v) + "]"
    if isinstance(v, str) and ("'" in v or ":" in v or "," in v):
        return '"' + v + '"'
    return str(v)


def proposal_text(pieces, placed):
    src = ("the level's data.yaml" if DATA and DATA.get("sections") else
           "its own proposal's sections" if OWN and OWN.get("sections") else "the README's Layout table")
    h = tile_heights()
    lines = ["backdrop:",
             f"  # Presentation only: Level 44's event plane (tools/art/backdrop_l44.py, step ART-B, sections from {src}).",
             "  # Every tile set wraps in x and y: the level's `current` slides each layer sideways at its factor (deep",
             "  # least; BackdropTiles' lateral shift), eased to a stop and run back in a reversal. The deep field is one",
             "  # continuous field cut at the sections' seams (LevelData.seam): re-run the generator after moving a",
             "  # section. The plane's slow warp: BackdropAtmosphere's smooth UV wobble (<= 0.5 Hz), from plane-warp.png",
             "  # (a data image, not a region to draw; step B4). The level opens in the transit's violet (B4).",
             "  scroll_factors: {deep: 0.15, far: 0.3, ground: 1, low-air: 1.3, high-air: 1.7}   # far, ground unused",
             "  ramp: 2",
             "  haze_colour: 2c1846",
             "  atmosphere:",
             "    clear: {haze: 0.0}   # vacuum: no haze, no wisps",
             "  tile_sets:"]
    for i, name in enumerate(DEEP):
        lines.append(f"    {name}: {{layer: deep, height: {h[name]}}}   # section {i + 1}: {SECTIONS[i]['name']}")
    for name, layer in TILES.items():
        lines.append(f"    {name}: {{layer: {layer}, height: {h[name]}}}")
    lines.append("  pieces:")
    for name, spec in pieces.items():
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]}}")
    lines.append("  placed:")
    for p in placed:
        if p.get("note"):
            lines.append(f"    # {p['note']}")
        lines.append(f"    - {{piece: {p['piece']}, t: {round(p['t'], 3)}, x: {p['x']}, path: {fmt(p['path'])}}}")
    lines.append("sections:   # the style sections (step B2: the level's data splits two of them round its reversals)")
    for sec in SECTIONS:
        lines.append("  - {" + ", ".join(f"{k}: {fmt(v)}" for k, v in sec.items() if k != "notes") + "}")
    lines.append(f"# rows: the end's bottom edge {int(round(scroll(END)))} (ground px)")
    return "\n".join(lines) + "\n"


def write_proposal():
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces_spec(), placements()), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    data = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))
    return data["backdrop"]["pieces"], data["backdrop"]["placed"]


# =========================================================================== composite

_CACHE = {}


def tile_rows(name, offset, lateral=0):
    key = ("tile", name)
    if key not in _CACHE:
        _CACHE[key] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
    arr = _CACHE[key]
    height = len(arr)
    rows = int(round(offset)) + SCREEN - 1 - np.arange(SCREEN)
    out = arr[height - 1 - (rows % height)]
    return np.roll(out, int(round(lateral)), axis=1), rows


def layer_tiles(t, layer, slide=0.0):
    f = FACTORS[layer]
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for i, sec in enumerate(SECTIONS):
        names = [n for n in sec.get("tiles") or [] if (n in DEEP) == (layer == "deep") and
                 (layer == "deep" or TILES.get(n) == layer)]
        if not names:
            continue
        arr, rows = tile_rows(names[0], scroll(t) * f, slide * f)
        sel = np.array([section_at_row(layer, r) == i for r in rows])
        out[sel] = arr[sel]
    return Image.fromarray(out, "RGBA")


def path_offset(p, t):
    pts = p.get("path")
    if not pts:
        return 0.0, 0.0
    if t <= pts[0][0]:
        return pts[0][1], pts[0][2]
    for a, b in zip(pts, pts[1:]):
        if t < b[0]:
            u = (t - a[0]) / (b[0] - a[0])
            return a[1] + (b[1] - a[1]) * u, a[2] + (b[2] - a[2]) * u
    return pts[-1][1], pts[-1][2]


def compose(t, slide=0.0, units=True):
    pieces, placed = load_proposal()
    img = layer_tiles(t, "deep", slide)
    for p in placed:
        for glow_layer in (False, True):
            name = p["piece"] + ("-glow" if glow_layer else "")
            if name not in pieces:
                continue
            key = ("piece", name)
            if key not in _CACHE:
                _CACHE[key] = Image.open(OUT / f"{name}.png").convert("RGBA")
            im = _CACHE[key]
            dx, dy = path_offset(p, t)
            f = FACTORS["deep"]
            x = p["x"] + dx + slide * f
            y = SCREEN - (scroll(p["t"]) * f + SCREEN / 2 + dy - scroll(t) * f)
            if abs(x - 240) > 1200:
                continue
            img = l23kit.add_c(img, im, x, y) if glow_layer else l23kit.paste_c(img, im, x, y)
    img.alpha_composite(layer_tiles(t, "low-air", slide))
    if units:
        img = stand_ins(img, t)
    img.alpha_composite(layer_tiles(t, "high-air", slide))
    return img


def load_held(name):
    try:
        return l23kit.load(name)
    except FileNotFoundError:
        return None


def stand_ins(img, t):
    sk, gl, ring = load_held("rift-skater"), load_held("rift-skater-glow"), load_held("teleport-ring")
    if sk:
        for k in range(4):
            x, y = 120 + k * 70, 120 + k * 40
            img = l23kit.paste_c(img, sk[(2 + k) % 16], x, y)
            if gl:
                img = l23kit.add_c(img, gl[0], x, y)
        if ring:
            img = l23kit.add_c(img, ring[4], 380, 250)
    bul = load_held("enemy-bullet-violet") or load_held("vrell-bullet")
    if bul:
        for k in range(5):
            img = l23kit.paste_c(img, bul[0], 200 + k * 22, 330 - k * 8)
    ship = load_held("ship")
    if ship:
        img = l23kit.paste_c(img, ship[2] if len(ship) > 2 else ship[0], 240, 470)
    return img


SCREENS = (20.0, 75.0, 140.0, 215.0, 254.0, 257.0, 259.0, 263.0)


def section_at_t(t):
    for i, sec in enumerate(SECTIONS):
        if t < float(sec["end"]):
            return i
    return len(SECTIONS) - 1


def review():
    w = 1000
    sheet = raster.sheet(w, 40 + 2 * 290 + 30 + 300, "LEVEL 44 BACKDROP (FINAL R89): CROSSING, THE EVENT PLANE",
                         f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    for i, t in enumerate(SCREENS):
        x, y = 12 + (i % 4) * 246, 40 + (i // 4) * 290
        raster.draw_text(sheet, x, y, f"T={t:g} S ({SECTIONS[section_at_t(t)]['name'].upper()})", raster.LABEL)
        sheet.alpha_composite(compose(t).resize((240, 270), Image.LANCZOS), (x, y + 12))
    y = 40 + 2 * 290 + 10
    raster.draw_text(sheet, 12, y, "THE TILES SIDE BY SIDE TWICE (THE X WRAP), THE CURTAIN, THE REEF, THE WARP MAP",
                     raster.LABEL)
    x = 12
    for name in DEEP[:4]:
        im = Image.open(OUT / f"{name}.png").convert("RGBA").crop((0, 0, 480, 256))
        two = Image.new("RGBA", (960, 256))
        two.alpha_composite(im, (0, 0))
        two.alpha_composite(im, (480, 0))
        sm = two.resize((240, 64), Image.LANCZOS)
        sheet.alpha_composite(sm, (x if x + 240 < w else 12, y + 14 + (0 if x + 240 < w else 70)))
        x += 246
    x, yy = 12, y + 14 + 140
    for name in ("curtain-left", "curtain-right", "beyond", "far-reef", "plane-warp"):
        im = Image.open(OUT / f"{name}.png").convert("RGBA")
        bg = Image.new("RGBA", im.size, (6, 5, 12, 255))
        bg.alpha_composite(im)
        sm = bg.resize((im.width // 4, im.height // 4), Image.LANCZOS)
        sheet.alpha_composite(sm, (x, yy))
        x += sm.width + 8
    frames = [compose(150 + 0.25 * k, slide=40 * k).resize((240, 270), Image.LANCZOS) for k in range(24)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=8)
    print("review: backdrop-final-r89-a")


# =========================================================================== check

def x_wrap(a):
    """The change across the x wrap (column 479 -> 0) over the median change between neighbouring columns."""
    lum = a[..., :3].astype(float) @ np.array((0.299, 0.587, 0.114))
    if a.shape[-1] == 4:
        lum = lum * a[..., 3] / 255.0
    cols = np.abs(np.diff(lum, axis=1)).mean(axis=0)
    return np.abs(lum[:, -1] - lum[:, 0]).mean() / max(np.median(cols), 0.05)


def check():
    bad = 0
    for p in sorted(OUT.glob("*.png")):
        im = Image.open(p)
        arr = np.array(im.convert("RGBA"))
        src = im.text.get(artkit.SOURCE_KEY) == SOURCE
        v, c = checker_share(im)
        share = c / max(v, 1)
        ok, msg = src and share < 0.05, []
        if p.stem in DEEP or p.stem in TILES:
            ys = wrap_score(arr) if p.stem in DEEP else jovkit.wrap_score_alpha(arr)
            xs = x_wrap(arr)
            luma = (arr[..., :3].astype(float) @ np.array((0.299, 0.587, 0.114))).max()
            ok &= ys < (3.0 if p.stem in DEEP else 1.0) and xs < 3.0 and (p.stem not in DEEP or luma <= LIMIT_LUMA)
            msg.append(f"y wrap {ys:.2f}, x wrap {xs:.2f}, brightest {luma:.0f}")
        if p.stem.endswith("-glow"):
            a = arr[..., :3].max(axis=-1)
            lit = max(a[0].max(), a[-1].max(), a[:, 0].max(), a[:, -1].max())
            ok &= lit == 0
            msg.append(f"edge {lit}")
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {p.name}: {im.size}, checker {share * 100:.1f} %, {', '.join(msg)}")
    return bad == 0


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--review" in args:
        review()
        sys.exit(0)
    if "--proposal" in args:
        write_proposal()
        sys.exit(0)
    build_all()
    write_proposal()
    review()
