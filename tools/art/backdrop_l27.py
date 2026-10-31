#!/usr/bin/env python3
"""Production art: the Level 27 backdrop, Hydrothermal Vents (design/campaign/act-4-deep-water/level-27-hydrothermal-
vents; M8 part G batch, concept round 57, step ART-G).

Straight to production from the round-48 mood still `europa-vents-r48-a` (tools/concept/europa_r48.py ``vents_still``:
basalt silt, black smoker chimneys with hot mouths, amber bacterial mats round them, white tube worms with red plumes,
the grey Hunter wreck with a pale hull code; its colours BASALT, PLAIN and FLESH imported unchanged); structure as
backdrop_l24.py (exact rows, the scroll helpers in tools/art/l27kit.py, its ``lumps`` height fields and the relay
beacon pieces of tools/art/l24_props.py imported unchanged). Posterized per image, wide gradients ordered-dithered.

**The grade is the game's** (``underwater_look`` per section, gap check m8g §5 G10: not dark): the tiles and pieces
carry the unlit floor; the light is the section's ambient and tint (dim blue 0.45 with the headlights in sections 1 and
4, vent-lit amber 0.6 in sections 2-3, the gate's teal 0.6 in section 5), the smokers' amber pools (the static
``lights:`` this script lists) and the plumes' pools by phase (the game's PlumeLooks). The only light that is the art's
own is the pieces' additive ``-glow`` (added over the grade, Backdrop.drawPieceGlows: the smokers' hot mouths, the
beacons on the plain's last ridge, the gate's teal).

Tile sets (480x960, wrapping, ground):
  vent-edge       section 1: the plain's last silt going dark grey toward basalt, tube-worm clumps, pale stones
  smoker-field    section 2: basalt silt, rusty sulphide staining, pale bacterial mats in the hollows, worm clumps
  chimney-garden  section 3: rougher basalt, heavier sulphide crust and mats, more worms
  ridge-basalt    section 4: blue-grey pillow basalt in long ribs along the ridge, bare, scattered stones
  bowl-floor      section 5: the bowl's floor below the rim: dark resin-stained silt, thin chitin root veins
Pieces (ground; exact rows; none within 48 px of a plume disc's centre):
  ridge-lip            480x176 at the start: the plain's last ridge, two of Levels 24-26's beacons on its lip
  relay-beacon         128x192, 16 frames at 4 fps (Level 24's piece, scenery here) with relay-beacon-glow
  smoker-1..18         144/160/176 px (looks a, b, c): black smoker clusters (1-3 dark mineral chimneys 40-70 px across
                       with hot mouths, the mats and worms round their feet), each drawn for its place; each with its
                       -glow (the mouths' amber-white heat, the mats' faint warmth): sections 1-4, densest in the
                       chimney garden
  nest-stack           176x152 at t~34: a broad flat-topped chimney stack, the three Spine Turrets' seats on top
  marlin-stack         176x176 at t~40: the smoker the hauler *Marlin* lies nose-down against, her scour (the hull
                       itself is the trigger's sprite, tools/art/l27_props.py ``marlin-hauler``); with -glow
  hull-wreck           144x128 at t=70, x 175: the grey Depth Hunter on the floor, its hull split, the paint blistered
                       off by a plume and the hull code HDX-417 pale under it (hint 3; readable at 1:1)
  mortar-mound         176x128 at t~80: the Polyp Mortars' mound (lumpy basalt, mats)
  siren-stack          208x160 at t~81: a tall stack with a ledge for the two Sirens grown on it
  ridge-crest-1..4     160x160: the rock knoll and concrete footing under each relay beacon (the beacons are units:
                       tools/art/relay_beacon.py), cable trenches toward the next
  ridge-shoulder-1, -2 the ridge's two shoulders at t~99.5 under the Spine Turret nests
  seam-smoker-field, seam-chimney-garden, seam-ridge-basalt   480x320 on the section seams
  seam-bowl-floor      480x320: the bowl's rim where the plain falls away (section 5's seam)
  hatchery-gate        480x720 at the end: the ring of grown chitin arches over the hatchery's mouth, with
                       hatchery-gate-glow (the teal light under the arches, the nodes at their feet)
Review: design/campaign/act-4-deep-water/level-27-hydrothermal-vents/concept/backdrop-final-r57-a.png/.gif (the vent
light's stand-in, tools/art/l27kit.py; stand-ins for the units: the plume mouths from l27_props.py when written, the
turrets, mortars, Sirens, beacons, the *Marlin*, the ship); `--proposal` writes the `backdrop:` block, the plume rows,
the ground targets' places and the static lights to the level's concept/backdrop-proposal.yaml (never into data.yaml).

Reads the level's data.yaml when it exists (sections, the vents' rows, ground targets), otherwise the README's values
and ART-G's proposal (l27kit). Re-run after the data moves a section, a plume row or a target.
Run: python3 tools/art/backdrop_l27.py [--proposal | --review | --check]   (all: about 2 min)
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor
from functools import lru_cache

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
import l24_props as props
import l27kit as kit
from artkit import DESIGN, ROOT, TAU, raster
from l27kit import (END, OUTRO, PLUME_ROWS, PLUME_WIDTH, SCREEN, SECTIONS, W, exact_t, scroll, seam_row,
                    section_at_row, target_centre, time_at)

import backdrop_l16 as b16  # noqa: E402  (tools/art: periodic noise, ramp, dither_alpha)
import backdrop_l22 as b22  # noqa: E402  (tools/art: posterize)
import backdrop_l24 as b24  # noqa: E402  (tools/art: lumps, wrap_score)
import europa_r48 as eu  # noqa: E402  (concept script, unchanged: BASALT, PLAIN, FLESH, m36.shade_rgb)

SCRIPT = "backdrop_l27.py"
BATCH = "M8 part G batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r57"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = kit.LEVEL_DIR
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = kit.BACKDROP
TILE_H = 960
FEATHER = 32
MAX_PIECE = 2040
BEACON_FPS = 4
PLUME_CLEAR = 48                     # no piece opaque within this of a plume disc's centre
SECTION_TILES = ["vent-edge", "smoker-field", "chimney-garden", "ridge-basalt", "bowl-floor"]
AMBER = np.array((255, 150, 60), float)
HOT = np.array((255, 232, 170), float)
TEAL = np.array((40, 255, 200), float)

pn, ramp, dither_alpha = b16.pn, b16.ramp, b16.dither_alpha
posterize = b22.posterize


def grid(h=TILE_H, w=W):
    return np.mgrid[0:h, 0:w].astype(np.float64)


# --------------------------------------------------------------------------- tile sets

BASALT = [tuple(int(c) for c in s) for s in eu.BASALT]
PLAIN = [tuple(int(c) for c in s) for s in eu.PLAIN]
FLESH = [tuple(int(c) for c in s) for s in eu.FLESH]
BLUE_BASALT = [(12, 14, 20), (28, 32, 40), (48, 54, 64), (72, 78, 88), (100, 106, 114), (130, 136, 142)]
SULPHIDE = np.array((150, 88, 46), float)
MAT = np.array((214, 192, 140), float)


def _mix(a, b, k):
    return [tuple(int(x * (1 - k) + y * k) for x, y in zip(p, q)) for p, q in zip(a, b)]


def vent_floor(seed, gain, stops, relief=0.3, ridge=0.18, ribs=0.0, crust=0.0, mats=0.0, worms=0, stones=40,
               veins=0):
    """A sea-floor tile (wrapping vertically): periodic fbm relief with sharp ridges (and N-S ribs), shaded from the
    upper left through ``stops``; rusty sulphide staining, pale bacterial mats in the hollows; then tube-worm clumps
    (white tubes, red plumes), stones and chitin root veins drawn wrapping."""
    yy, xx = grid()
    n = pn(W, TILE_H, 96, seed, 5)
    rd = 1 - np.abs(2 * pn(W, TILE_H, 48, seed + 1, 2) - 1)
    hgt = 0.42 + relief * n + ridge * rd ** 4
    if ribs:
        warp = 56 * pn(W, TILE_H, 120, seed + 2, 2)
        hgt += ribs * (0.5 + 0.5 * np.sin(TAU * (xx + warp) / 84)) ** 3
        hgt += 0.5 * ribs * pn(W, TILE_H, 12, seed + 4, 3)
    stops = [tuple(min(255, int(c * gain)) for c in s) for s in stops]
    rgb = eu.m36.shade_rgb(hgt, stops, strength=16, shadow=0.004, periodic=True)
    rgb *= (0.92 + 0.16 * pn(W, TILE_H, 8, seed + 5, 2))[..., None]
    if crust:
        c = pn(W, TILE_H, 24, seed + 6, 3)
        stain = np.clip((c - (1 - crust)) / 0.12, 0, 1)
        rgb = rgb * (1 - 0.55 * stain[..., None]) + SULPHIDE * (rgb.mean(-1, keepdims=True) / 90) * 0.55 * stain[..., None]
    if mats:
        m = pn(W, TILE_H, 20, seed + 7, 4)
        low = np.clip((0.55 - n) / 0.3, 0, 1)
        cover = np.clip((m * (0.6 + 0.6 * low) - (1 - mats)) / 0.08, 0, 1)
        q = artkit.ordered_dither(cover * 0.9, 3)
        fuzz = 0.85 + 0.3 * pn(W, TILE_H, 3, seed + 8, 2)
        rgb = rgb * (1 - q[..., None]) + MAT * fuzz[..., None] * q[..., None]
    img = posterize(rgb, 32)
    lay = Image.new("RGBA", (W, TILE_H), (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    rng = np.random.default_rng(seed + 9)

    def wrap(fn):
        for dy in (-TILE_H, 0, TILE_H):
            fn(dy)

    for _ in range(veins):                                          # thin chitin root veins, dark with a pale edge
        x, y, a = rng.uniform(0, W), rng.uniform(0, TILE_H), rng.uniform(0, TAU)
        pts = [(x, y)]
        for _ in range(int(rng.integers(6, 12))):
            a += rng.normal(0, 0.45)
            x, y = x + math.cos(a) * 10, y + math.sin(a) * 10
            pts.append((x, y))
        wrap(lambda dy, pts=pts: (d.line([(px + 1, py + dy + 1) for px, py in pts], fill=(150, 120, 140, 255), width=1),
                                  d.line([(px, py + dy) for px, py in pts], fill=(40, 24, 40, 255), width=2)))
    for _ in range(worms):                                          # tube-worm clumps: white tubes, red plumes
        cx, cy = rng.uniform(10, W - 10), rng.uniform(0, TILE_H)
        for _ in range(int(rng.integers(6, 14))):
            a, rr = rng.uniform(0, TAU), abs(rng.normal(0, 5))
            px, py = cx + math.cos(a) * rr, cy + math.sin(a) * rr
            wrap(lambda dy, px=px, py=py: (
                d.ellipse([px - 1.6, py + dy - 1.6, px + 1.6, py + dy + 1.6], fill=(222, 216, 204, 255),
                          outline=(70, 60, 56, 255)),
                d.point([(px, py + dy)], fill=(214, 44, 40, 255)),
                d.point([(px - 1, py + dy - 1)], fill=(244, 240, 232, 255))))
    for _ in range(stones):                                         # pale stones and shells
        cx, cy, r = rng.uniform(0, W), rng.uniform(0, TILE_H), rng.uniform(1.0, 2.4)
        wrap(lambda dy, cx=cx, cy=cy, r=r: (
            d.ellipse([cx - r, cy + dy - r, cx + r, cy + dy + r], fill=(118, 116, 110, 255)),
            d.point([(cx - r * 0.4, cy + dy - r * 0.4)], fill=(168, 164, 152, 255))))
    img.alpha_composite(lay)
    a = np.array(img)
    a[..., 3] = 255
    return Image.fromarray(a, "RGBA")


TILES = {
    "vent-edge": lambda: vent_floor(2701, 1.45, _mix(PLAIN, BASALT, 0.55), relief=0.3, worms=14, stones=60, crust=0.08),
    "smoker-field": lambda: vent_floor(2711, 1.7, BASALT, relief=0.32, ridge=0.2, crust=0.3, mats=0.16, worms=22,
                                       stones=30),
    "chimney-garden": lambda: vent_floor(2721, 1.65, BASALT, relief=0.36, ridge=0.26, crust=0.42, mats=0.22, worms=30,
                                         stones=24),
    "ridge-basalt": lambda: vent_floor(2731, 1.35, BLUE_BASALT, relief=0.22, ridge=0.1, ribs=0.12, crust=0.04,
                                       worms=4, stones=60),
    "bowl-floor": lambda: vent_floor(2741, 1.25, _mix(BASALT, FLESH, 0.45), relief=0.18, ridge=0.08, stones=14,
                                     veins=40),
}


@lru_cache(maxsize=None)
def tile(name):
    return np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))


def tile_rows(name, bottom, h):
    t = tile(name)
    rows = bottom + h - 1 - np.arange(h)
    return t[(TILE_H - 1 - np.mod(rows, TILE_H)).astype(int)]


def floor_rows(bottom, h):
    out = np.zeros((h, W, 4), np.uint8)
    for k in range(h):
        row = bottom + h - 1 - k
        out[k] = tile(SECTION_TILES[section_at_row(row)])[TILE_H - 1 - int(row) % TILE_H]
    return out


def feather_alpha(h, w):
    k = np.arange(h)[:, None]
    a = np.minimum(np.clip((k + 0.5) / FEATHER, 0, 1), np.clip((h - k - 0.5) / FEATHER, 0, 1))
    return dither_alpha(np.broadcast_to(a, (h, w)).copy(), 2)


# --------------------------------------------------------------------------- the layout

# smokers: (t the centre enters at the top edge, x, variant); clear of the plume discs, the targets and the wreck
SMOKERS = [(9, 400, "c"), (15, 70, "b"), (20, 300, "a"),
           (29.5, 250, "a"), (36.5, 160, "b"), (43.5, 340, "c"), (49.5, 50, "a"), (55, 290, "b"),
           (61.5, 150, "c"), (61.5, 335, "a"), (68.5, 410, "b"), (75.5, 215, "a"), (75.5, 440, "c"),
           (82.5, 245, "b"), (88.5, 150, "c"), (88.5, 345, "a"),
           (112, 400, "b"), (120.5, 300, "a")]
SMOKER = {"a": (144, [(0, 0, 30, 34)], 2751),
          "b": (160, [(-22, 8, 26, 30), (24, -14, 20, 24)], 2761),
          "c": (176, [(-30, 14, 22, 24), (26, 18, 26, 32), (0, -32, 18, 20)], 2771)}
NEST_T, NEST_X = 34.0, 384.0
MARLIN = next(((t, x) for k, t, x in kit.TARGETS if k == "marlin"), (40.0, 90.0))
MOUND = (80.0, 99.0)
SIREN_STACK = (81.0, 395.0)
SHOULDERS = [(99.5, 130.0, 3), (99.5, 330.0, 2)]          # t, x, seats (hard's five: 3 + 2)
RIDGE_LIP_T = 3.0
LIP_BEACONS = [70, 420]


def mouths(kind, t, x):
    """The hot mouths of a smoker placement as (ground row, x)."""
    size, chims, _ = SMOKER[kind]
    row = target_centre(t, 0)
    return [(row - cy, x + cx) for cx, cy, r, z in chims]


def pool_of(kind, t, x):
    """One amber pool per smoker cluster: the mean of its mouths (ground row, x)."""
    m = mouths(kind, t, x)
    return sum(r for r, _ in m) / len(m), sum(c for _, c in m) / len(m)


def plume_centres():
    return [(target_centre(t, PLUME_WIDTH), x) for t, xs in PLUME_ROWS for x in xs]


# --------------------------------------------------------------------------- piece helpers

def relief(bottom, x0, w, h, hgt, mask, albedo, colours=36, shadow=(6, 8), blur=3, extra_cover=None, k=1.6):
    """A shaded relief (lit from the upper left) over the floor rows: opaque where ``mask`` covers, its cast shadow
    toward the lower right on the floor round it."""
    floor = np.zeros((h, w, 3))
    fr = floor_rows(bottom, h)
    lo, hi = max(0, x0), min(W, x0 + w)
    floor[:, lo - x0:hi - x0] = fr[:, lo:hi, :3]
    gy, gx = np.gradient(hgt)
    shade = np.clip(1.0 + (gx + gy) * k, 0.3, 1.6)            # lit from the upper left (height rising away)
    rgb = albedo * shade[..., None]
    m = np.clip(mask, 0, 1)
    sm = np.roll(np.roll(m, shadow[1], 0), shadow[0], 1)
    sm = np.array(Image.fromarray((sm * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(blur))) / 255.0
    under = floor * (1 - 0.5 * sm[..., None])
    if extra_cover is not None:
        under = np.where((extra_cover[1] > 0.5)[..., None], extra_cover[0], under)
    out = np.where((m > 0.5)[..., None], rgb, under)
    a = np.maximum((m > 0.5).astype(float), np.clip(sm * 1.6, 0, 1) * (sm > 0.08))
    if extra_cover is not None:
        a = np.maximum(a, extra_cover[1])
    a = _edges(a, x0, w)
    return posterize(np.clip(out, 0, 255), colours, dither_alpha(a, 2))


def _edges(a, x0, w):
    a = a.copy()
    a[0, :] = a[-1, :] = 0
    xs = np.arange(w) + x0
    a[:, (xs < 0) | (xs >= W)] = 0
    if x0 > 0:
        a[:, 0] = 0
    if x0 + w < W:
        a[:, -1] = 0
    return a


def mat_ring(w, h, cx, cy, r0, r1, seed, density=0.55):
    """Pale bacterial mats round a foot: fuzzy patches between radii r0 and r1, and a worm-clump layer."""
    yy, xx = grid(h, w)
    r = np.hypot(xx - cx, (yy - cy) * 1.05)
    n = raster.fbm(w, h, 7, seed, octaves=3, period=False)
    band = np.clip((r1 - r) / (r1 - r0), 0, 1) * (r > r0 * 0.9)
    return np.clip((n * band * 1.6 - (1 - density)) / 0.15, 0, 1)


def worms_layer(lay, rng, cx, cy, r0, r1, n):
    d = ImageDraw.Draw(lay)
    for _ in range(n):
        a, rr = rng.uniform(0, TAU), rng.uniform(r0, r1)
        px, py = cx + math.cos(a) * rr, cy + math.sin(a) * rr
        for _ in range(int(rng.integers(3, 7))):
            qx, qy = px + rng.normal(0, 2.5), py + rng.normal(0, 2.5)
            d.ellipse([qx - 1.5, qy - 1.5, qx + 1.5, qy + 1.5], fill=(222, 216, 204, 255), outline=(70, 60, 56, 255))
            d.point([(qx, qy)], fill=(214, 44, 40, 255))


def chimney_field(w, h, chims, seed, mouth=True, flat=None):
    """Height and albedo of chimneys: steep crusty cones (cx, cy, base radius, height) with knobs; a black mouth at
    the top of each (``mouth``) or a flat top (``flat``: the plateau's share of the radius)."""
    yy, xx = grid(h, w)
    hgt = np.zeros((h, w))
    mouth_m = np.zeros((h, w))
    rim = np.zeros((h, w))
    rng = np.random.default_rng(seed)
    noise = raster.fbm(w, h, 5, seed + 1, octaves=3, period=False)
    for cx, cy, r, z in chims:
        d = np.hypot(xx - cx, yy - cy) / (r * (0.9 + 0.2 * noise))
        if flat:
            cone = z * np.clip((1 - d) / (1 - flat), 0, 1) ** 0.7
        else:
            cone = z * np.clip(1 - d, 0, 1) ** 1.25
        hgt = np.maximum(hgt, cone)
        for _ in range(int(r * 0.6)):                               # mineral knobs and ledges on the flanks
            a, rr = rng.uniform(0, TAU), rng.uniform(0.35, 0.95) * r
            kx, ky = cx + math.cos(a) * rr, cy + math.sin(a) * rr
            kr = rng.uniform(2.5, 6)
            kz = z * (1 - rr / r) ** 1.25 + rng.uniform(1, 4)
            k = kz * np.sqrt(np.clip(1 - ((xx - kx) ** 2 + (yy - ky) ** 2) / kr ** 2, 0, 1))
            hgt = np.maximum(hgt, k)
        if mouth and not flat:
            dm = np.hypot(xx - cx, yy - cy)
            mr = max(3.5, r * 0.22)
            mouth_m = np.maximum(mouth_m, (dm < mr).astype(float))
            rim = np.maximum(rim, np.clip(1 - np.abs(dm - mr - 1.2) / 1.6, 0, 1))
            hgt = np.where(dm < mr, hgt - z * 0.4, hgt)
    crust = raster.fbm(w, h, 4, seed + 2, octaves=3, period=False)
    alb = ramp([(26, 24, 26), (48, 42, 40), (74, 64, 58), (104, 90, 78), (136, 118, 100)],
               np.clip(0.2 + 0.45 * crust + hgt / 90, 0, 1))
    hi = np.clip(hgt / max(1.0, max(z for _, _, _, z in chims)), 0, 1)
    stain = (raster.fbm(w, h, 9, seed + 3, octaves=2, period=False) > 0.5) & (hi > 0.45)
    alb = np.where(stain[..., None], alb * 0.6 + SULPHIDE * 0.4 * (0.6 + 0.6 * hi[..., None]), alb)
    band = 0.86 + 0.28 * (np.sin(hgt * 0.9 + 2.6 * crust) > 0.2)          # the chimneys' growth bands
    alb = alb * np.where(hgt > 1, band, 1.0)[..., None]
    alb = np.where((rim > 0.3)[..., None], np.array((150, 90, 50.0)) * 0.8, alb)
    alb = np.where((mouth_m > 0.5)[..., None], np.array((10, 7, 7.0)), alb)
    return hgt, alb, mouth_m, rim


def glow_of(w, h, spots, mats=None, k_mat=0.1):
    """Additive: each mouth's hot core and amber halo (cx, cy, r); the mats' faint warmth."""
    yy, xx = grid(h, w)
    f = np.zeros((h, w))
    hot = np.zeros((h, w))
    for cx, cy, r in spots:
        d = np.hypot(xx - cx, yy - cy)
        hot = np.maximum(hot, np.exp(-(d / max(2.0, r * 0.5)) ** 2))
        f = np.maximum(f, np.exp(-(d / (r * 1.8)) ** 2) * 0.55)
    if mats is not None:
        f = np.maximum(f, mats * k_mat)
    v = artkit.smooth_steps(f + hot * 0.6)
    rgb = AMBER[None, None] * v[..., None] + (HOT - AMBER)[None, None] * np.clip(hot - 0.5, 0, 1)[..., None] * 1.6
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


def _place(t, x, w, h, clamp=True):
    row0 = target_centre(t, 0)
    x0 = int(np.clip(round(x - w / 2), 0, W - w)) if clamp else int(round(x - w / 2))
    bottom = int(round(row0 - h / 2))
    return bottom, x0, x - x0, h / 2


# --------------------------------------------------------------------------- pieces

def smoker_piece(kind, t, x):
    size, chims, seed = SMOKER[kind]
    w = h = size
    bottom, x0, cx, cy = _place(t, x, w, h)
    ch = [(cx + dx, cy + dy, r, z) for dx, dy, r, z in chims]
    hgt, alb, mouth, rim = chimney_field(w, h, ch, seed)
    mask = (hgt > 0.8).astype(float)
    mats = np.zeros((h, w))
    for ccx, ccy, r, z in ch:
        mats = np.maximum(mats, mat_ring(w, h, ccx, ccy, r * 0.85, r * 1.9, seed + int(ccx)))
    mats *= (mask < 0.5)
    lay = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    rng = np.random.default_rng(seed + 11)
    for ccx, ccy, r, z in ch:
        worms_layer(lay, rng, ccx, ccy, r * 0.95, r * 1.5, 5)
    floor = np.zeros((h, w, 3))
    fr = floor_rows(bottom, h)
    floor[:, :] = fr[:, x0:x0 + w, :3]
    q = artkit.ordered_dither(mats * 0.95, 3)
    under = floor * (1 - q[..., None]) + MAT * (0.85 + 0.25 * raster.fbm(w, h, 3, seed + 12, 2, period=False))[..., None] \
        * q[..., None]
    wl = np.array(lay).astype(float)
    wa = wl[..., 3:4] / 255
    under = under * (1 - wa) + wl[..., :3] * wa
    cover = np.maximum(q, wa[..., 0])
    img = relief(bottom, x0, w, h, hgt, mask, alb, colours=40, shadow=(9, 12), blur=4, extra_cover=(under, cover),
                 k=0.9)
    spots = [(ccx, ccy, max(3.5, r * 0.22)) for ccx, ccy, r, z in ch]
    g = glow_of(w, h, spots, mats * (mask < 0.5), 0.08)
    ga = np.array(g)
    ga[..., :3] = (ga[..., :3] * _edges(np.ones((h, w)), x0, w)[..., None]).astype(np.uint8)
    ga[ga[..., :3].max(-1) == 0] = 0
    return img, Image.fromarray(ga, "RGBA"), bottom, x0


def stack_piece(t, x, w, h, top_r, z, seats, seed):
    """A broad flat-topped chimney stack: the plateau (its share ``top_r`` of the radius) carries the units' seats."""
    bottom, x0, cx, cy = _place(t, x, w, h)
    rad = min(w, h) * 0.42
    ch = [(cx, cy, rad, z)]
    hgt, alb, _, _ = chimney_field(w, h, ch, seed, mouth=False, flat=top_r)
    yy, xx = grid(h, w)
    for sx in seats:                                               # the seats: shallow scorched rings
        d = np.hypot(xx - (sx - x0), yy - cy)
        ring = np.clip(1 - np.abs(d - 9) / 2.2, 0, 1)
        alb = np.where((ring > 0.4)[..., None] & (hgt > z * 0.8)[..., None], alb * 0.55, alb)
    mask = (hgt > 0.8).astype(float)
    mats = mat_ring(w, h, cx, cy, rad * 0.9, rad * 1.45, seed + 5) * (mask < 0.5)
    lay = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    worms_layer(lay, np.random.default_rng(seed + 6), cx, cy, rad, rad * 1.3, 7)
    floor = floor_rows(bottom, h)[:, x0:x0 + w, :3].astype(float)
    q = artkit.ordered_dither(mats * 0.95, 3)
    under = floor * (1 - q[..., None]) + MAT * q[..., None]
    wl = np.array(lay).astype(float)
    wa = wl[..., 3:4] / 255
    under = under * (1 - wa) + wl[..., :3] * wa
    img = relief(bottom, x0, w, h, hgt, mask, alb, colours=40, shadow=(10, 13), blur=4,
                 extra_cover=(under, np.maximum(q, wa[..., 0])), k=0.9)
    return img, bottom, x0


def mound_piece():
    t, x = MOUND
    w, h = 176, 128
    bottom, x0, cx, cy = _place(t, x, w, h)
    rng = np.random.default_rng(2781)
    blobs = [(cx, cy, 62, 40, 14)]
    for _ in range(18):
        a, r = rng.uniform(0, TAU), rng.uniform(10, 50)
        blobs.append((cx + math.cos(a) * r, cy + math.sin(a) * r * 0.6, rng.uniform(10, 20), rng.uniform(8, 16),
                      rng.uniform(5, 12) * (1 - r / 70)))
    hgt, crust = b24.lumps(w, h, blobs, 2782)
    mask = (hgt > 0.6).astype(float)
    alb = ramp([(30, 28, 30), (56, 50, 48), (84, 74, 66), (112, 100, 88)], np.clip(0.15 + hgt / 24 + 0.3 * crust, 0, 1))
    mats = (raster.fbm(w, h, 6, 2783, octaves=3, period=False) > 0.56) & (hgt > 2) & (hgt < 9)
    alb = np.where(mats[..., None], MAT * 0.9, alb)
    return relief(bottom, x0, w, h, hgt, mask, alb, colours=32), bottom, x0


def marlin_stack():
    """The smoker the Marlin lies nose-down against (up-left of her), her scour and drag marks in the silt."""
    t, x = MARLIN
    w = h = 176
    bottom, x0, cx, cy = _place(t, x, w, h)
    ch = [(cx - 34, cy - 40, 30, 36)]
    hgt, alb, mouth, rim = chimney_field(w, h, ch, 2791)
    yy, xx = grid(h, w)
    ax, ay = math.cos(-0.95), math.sin(-0.95)                      # the hull's axis, nose toward the stack
    u = (xx - cx) * ax + (yy - cy) * ay
    v = -(xx - cx) * ay + (yy - cy) * ax
    hollow = np.clip(1 - (u / 40) ** 2 - (v / 17) ** 2, 0, 1)
    mask = (hgt > 0.8).astype(float)
    floor = floor_rows(bottom, h)[:, x0:x0 + w, :3].astype(float)
    n = raster.fbm(w, h, 8, 2792, octaves=3, period=False)
    scour = floor * (1 - 0.35 * hollow[..., None]) * (0.9 + 0.2 * n[..., None])
    mats = mat_ring(w, h, ch[0][0], ch[0][1], 26, 56, 2793) * (mask < 0.5) * (hollow < 0.2)
    q = artkit.ordered_dither(mats * 0.95, 3)
    under = scour * (1 - q[..., None]) + MAT * q[..., None]
    img = relief(bottom, x0, w, h, hgt, mask, alb, colours=40, shadow=(9, 12), blur=4,
                 extra_cover=(under, np.clip(np.maximum(hollow * 3, q), 0, 1)), k=0.9)
    g = glow_of(w, h, [(ch[0][0], ch[0][1], 6.6)], mats, 0.08)
    return img, g, bottom, x0


def crest_piece(i):
    """The rock knoll and footing under relay beacon i: lumpy blue-grey basalt, a concrete pad at the mast's foot,
    a cable trench toward the next beacon."""
    t, x = kit.BEACONS[i]
    w = h = 160
    import relay_beacon as rb                                       # noqa: E402  (the beacon's foot in its frame)
    centre_row = target_centre(t, rb.SIZE[1])
    foot_row = centre_row - (rb.FOOT[1] - rb.SIZE[1] / 2)
    bottom = int(round(foot_row - h / 2))
    x0 = int(np.clip(round(x - w / 2), 0, W - w))
    cx, cy = x - x0, h / 2
    rng = np.random.default_rng(2800 + i)
    blobs = [(cx, cy + 4, 44, 36, 10)]
    for _ in range(14):
        a, r = rng.uniform(0, TAU), rng.uniform(14, 52)
        blobs.append((cx + math.cos(a) * r, cy + math.sin(a) * r * 0.8, rng.uniform(10, 18), rng.uniform(8, 14),
                      rng.uniform(4, 9) * (1 - r / 70)))
    hgt, crust = b24.lumps(w, h, blobs, 2810 + i)
    yy, xx = grid(h, w)
    pad = (np.abs(xx - cx) < 10) & (np.abs(yy - cy) < 10)
    hgt = np.where(pad, 10.5, hgt)
    mask = (hgt > 0.6).astype(float)
    alb = ramp([(34, 38, 46), (58, 64, 74), (86, 92, 102), (114, 120, 128)], np.clip(0.15 + hgt / 20 + 0.3 * crust, 0, 1))
    conc = np.array((140, 140, 134.0)) * (0.9 + 0.2 * raster.fbm(w, h, 4, 2820 + i, octaves=2, period=False))[..., None]
    alb = np.where(pad[..., None], conc, alb)
    if i + 1 < len(kit.BEACONS):                                    # the cable trench toward the next mast
        tj, xj = kit.BEACONS[i + 1]
        dx, dy = xj - x, -(target_centre(tj, 0) - target_centre(t, 0))
        L = math.hypot(dx, dy)
        ux, uy = dx / L, dy / L
        along = (xx - cx) * ux + (yy - cy) * uy
        across = -(xx - cx) * uy + (yy - cy) * ux
        trench = np.clip(1 - (across / 2.4) ** 2, 0, 1) * (along > 12) * np.clip((78 - along) / 30, 0, 1)
        alb = alb * (1 - 0.45 * trench[..., None])
        mask = np.maximum(mask, (trench > 0.3).astype(float))
    return relief(bottom, x0, w, h, hgt, mask, alb, colours=32), bottom, x0


def shoulder_piece(t, x, seats, seed):
    w, h = 176, 128
    bottom, x0, cx, cy = _place(t, x, w, h)
    rng = np.random.default_rng(seed)
    blobs = [(cx, cy, 70, 34, 12)]
    for _ in range(16):
        a, r = rng.uniform(0, TAU), rng.uniform(10, 60)
        blobs.append((cx + math.cos(a) * r, cy + math.sin(a) * r * 0.5, rng.uniform(12, 20), rng.uniform(8, 14),
                      rng.uniform(5, 11) * (1 - r / 80)))
    hgt, crust = b24.lumps(w, h, blobs, seed + 1)
    yy, xx = grid(h, w)
    alb = ramp([(32, 36, 44), (56, 62, 72), (84, 90, 100), (112, 118, 126)], np.clip(0.15 + hgt / 20 + 0.3 * crust, 0, 1))
    for k in range(seats):                                          # flattened seats along the crest
        sx = cx + (k - (seats - 1) / 2) * 42
        d = np.hypot(xx - sx, yy - cy)
        hgt = np.where(d < 11, np.maximum(hgt, 11.5), hgt)
        alb = np.where((np.abs(d - 10) < 1.4)[..., None], alb * 0.55, alb)
    mask = (hgt > 0.6).astype(float)
    return relief(bottom, x0, w, h, hgt, mask, alb, colours=32), bottom, x0


# 3x5 glyphs for the hull code (pale stencil)
GLYPHS = {"H": ["101", "101", "111", "101", "101"], "D": ["110", "101", "101", "101", "110"],
          "X": ["101", "101", "010", "101", "101"], "-": ["000", "000", "111", "000", "000"],
          "4": ["101", "101", "111", "001", "001"], "1": ["010", "110", "010", "010", "111"],
          "7": ["111", "001", "010", "010", "010"]}
HULL_CODE = "HDX-417"


def hull_wreck():
    """The grey Depth Hunter on the floor (round 48's look b, tools/concept/enemies_r48.py ``depth_hunter``): lying
    across the screen nose to the right, the hull split behind the tubes and the stern knocked askew, the paint
    blistered and burnt off over the forward hull (a plume's heat) so the hull code shows pale; its scour, a scorch
    on the silt and torn plates."""
    import enemies_r36 as e36  # noqa: E402
    import enemies_r48 as e48  # noqa: E402
    from dataclasses import replace
    from render.enemy_rigs import model_rotation, model_space_materials
    from render.sdf import rotate_z
    t, x = kit.WRECK
    w, h = 144, 128
    bottom = int(round(scroll(t) + SCREEN / 2 - h / 2))
    x0 = int(round(x - w / 2))
    base, mats = e48.depth_hunter("b", 0.0)
    scorch = (lambda p, n: np.where(p[:, 1] > -0.05, np.where(
        np.sin(p[:, 0] * 23 + p[:, 1] * 17) * np.cos(p[:, 1] * 29) > 0.35, 1.0, 0.62), 0.85))
    mats = list(mats)
    mats[0] = replace(mats[0], pattern=scorch)
    heading = 12.6                                                  # nose to the right, a little up
    rot = model_rotation(np.pi / 2 + TAU * heading / 16)
    rot2 = model_rotation(np.pi / 2 + TAU * heading / 16 + 0.35)

    def sc(p):
        q = rotate_z(p, rot)
        d1, m1 = base(q)
        d1 = np.maximum(d1, 0.02 - q[:, 1])                        # the forward hull: y > 0.02
        q2 = rotate_z(p - np.array([-0.18, -0.12, 0.0]), rot2)
        d2, m2 = base(q2)
        d2 = np.maximum(d2, q2[:, 1] + 0.12)                       # the stern section: y < -0.12, knocked askew
        return np.where(d2 < d1, d2, d1), np.where(d2 < d1, m2, m1)
    extent = 144 / 33.0
    hi, f = artkit.render_hi(sc, model_space_materials(mats, rot), (w, h), extent)
    body = np.array(artkit.native(hi, f, crisp=60)).astype(np.float64)
    body = np.array(e36.grey_rim(Image.fromarray(body.astype(np.uint8), "RGBA"))).astype(np.float64)
    # the hull code on the forward hull, pale bare metal through the blistered paint
    cw = 4 * len(HULL_CODE) - 1
    best = None
    for oy in range(30, h - 36):                                    # the code on the flattest, lightest plate
        for ox in range(20, w - cw - 20):
            box = body[oy - 1:oy + 6, ox - 1:ox + cw + 1]
            if (box[..., 3] > 0).all():
                score = box[..., :3].mean() - box[..., :3].std() * 2 - 0.3 * abs(ox + cw / 2 - w / 2) - 0.3 * abs(oy - h / 2)
                if best is None or score > best[0]:
                    best = (score, ox, oy)
    _, ox, oy = best
    body[oy - 1:oy + 6, ox - 1:ox + cw + 1, :3] *= 0.55              # the blistered patch round it, burnt dark
    tx = ox
    for ch in HULL_CODE:
        for gy, row in enumerate(GLYPHS[ch]):
            for gx, bit in enumerate(row):
                if bit == "1" and body[oy + gy, tx + gx, 3] > 0:
                    body[oy + gy, tx + gx, :3] = (206, 204, 190)
        tx += 4
    yy, xx = grid(h, w)
    floor = np.zeros((h, w, 3))
    fr = floor_rows(bottom, h)
    lo, hi_ = max(0, x0), min(W, x0 + w)
    floor[:, lo - x0:hi_ - x0] = fr[:, lo:hi_, :3]
    cx, cy = w / 2, h / 2
    r = np.hypot(xx - cx, (yy - cy) * 1.3)
    hollow = np.clip(1 - (r / 52) ** 2, 0, 1)
    sc_ring = np.clip(1 - ((np.hypot(xx - cx + 30, yy - cy + 20) - 4) / 26) ** 2, 0, 1)   # the plume's scorch
    n = raster.fbm(w, h, 7, 2831, octaves=3, period=False)
    under = floor * (1 - 0.3 * hollow[..., None]) * (1 - 0.45 * sc_ring[..., None] * (0.7 + 0.6 * n[..., None]))
    ba = body[..., 3:4] / 255
    sm = np.roll(np.roll(body[..., 3] / 255, 7, 0), 6, 1)
    sm = np.array(Image.fromarray((sm * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(2.5))) / 255.0
    under = under * (1 - 0.45 * sm[..., None])
    rgb = under * (1 - ba) + body[..., :3] * ba
    img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(2833)
    plates = Image.new("L", (w, h), 0)
    dp = ImageDraw.Draw(plates)
    for _ in range(8):                                              # torn plates round the break
        px, py = cx + rng.normal(-6, 22), cy + rng.normal(4, 16)
        s, a = rng.uniform(2, 4), rng.uniform(0, TAU)
        pts = [(px + s * math.cos(a + k * TAU / 4) * (1.6 if k % 2 else 1), py + s * math.sin(a + k * TAU / 4))
               for k in range(4)]
        d.polygon(pts, fill=(118, 122, 130, 255), outline=(36, 38, 42, 255))
        dp.polygon(pts, fill=255)
    cover = np.clip(np.maximum.reduce([hollow * 3, sc_ring * 2, sm * 2, ba[..., 0], np.array(plates) / 255.0]), 0, 1)
    cover = _edges(cover, x0, w)
    rgb = np.array(img)[..., :3].astype(float)
    return posterize(rgb, 48, dither_alpha(cover, 2)), bottom, x0


def seam_piece(index, seed, h=320):
    lower, upper = SECTION_TILES[index - 1], SECTION_TILES[index]
    bottom = int(round(seam_row(index))) - h // 2
    lo = tile_rows(lower, bottom, h)[..., :3].astype(float)
    up = tile_rows(upper, bottom, h)[..., :3].astype(float)
    yy, xx = grid(h, W)
    n = raster.fbm(W, h, 40, seed, octaves=4, period=False)
    line = h / 2 + 34 * np.sin(xx / 83 + seed) + 30 * (n - 0.5) * 2
    rgb = np.where((yy < line)[..., None], up, lo)
    return posterize(rgb, 40, feather_alpha(h, W)), bottom


def bowl_rim(seed=2841, h=320):
    """Section 5's seam: the plain's lip and the slope falling away into the bowl (up the screen): a lit edge, then a
    band of slope darkening with streaks running down it, into the bowl floor."""
    index = 4
    bottom = int(round(seam_row(index))) - h // 2
    lo = tile_rows(SECTION_TILES[3], bottom, h)[..., :3].astype(float)
    up = tile_rows(SECTION_TILES[4], bottom, h)[..., :3].astype(float)
    yy, xx = grid(h, W)
    n = raster.fbm(W, h, 40, seed, octaves=4, period=False)
    lip = h * 0.62 + 18 * np.sin(xx / 70 + 1.3) + 16 * (n - 0.5) * 2
    slope_top = lip - 70 - 20 * raster.fbm(W, h, 60, seed + 1, octaves=2, period=False)
    u = np.clip((lip - yy) / np.maximum(lip - slope_top, 1), 0, 1)       # 0 at the lip, 1 at the slope's top (far)
    streak = 0.85 + 0.3 * pn(W, 64, 8, seed + 2, 2)[np.arange(h) % 64][:, :]
    slope = lo * (0.75 - 0.35 * u)[..., None] * streak[..., None]
    rgb = np.where((yy > lip)[..., None], lo, np.where((yy > slope_top)[..., None], slope, up * 0.9))
    edge = np.abs(yy - lip) < 1.6
    rgb = np.where(edge[..., None], np.minimum(255, lo * 1.35 + 14), rgb)
    rgb = np.where(((yy > lip + 1.6) & (yy < lip + 5))[..., None], lo * 0.7, rgb)
    return posterize(np.clip(rgb, 0, 255), 40, feather_alpha(h, W)), bottom


def ridge_lip():
    """The plain's last ridge at the start: a line of silted basalt humps across, the old beacons on its lip."""
    w, h = W, 176
    row0 = scroll(RIDGE_LIP_T) + SCREEN / 2
    bottom = int(round(row0 - h / 2))
    rng = np.random.default_rng(2851)
    blobs = []
    for k in range(40):
        x = -16 + k * 13 + rng.uniform(-6, 6)
        y = h / 2 + 10 * math.sin(x / 61) + rng.normal(0, 6)
        blobs.append((x, y, rng.uniform(16, 30), rng.uniform(12, 22), rng.uniform(7, 13)))
    hgt, crust = b24.lumps(w, h, blobs, 2852)
    yy, _ = grid(h, w)
    mask = (hgt > 0.6).astype(float) * np.clip(np.minimum(yy - 4, h - 5 - yy) / 4, 0, 1)
    alb = ramp([(40, 42, 46), (66, 68, 70), (92, 94, 92), (116, 118, 112)], np.clip(0.15 + hgt / 22 + 0.3 * crust, 0, 1))
    silt = (hgt > 9) & (crust > 0.55)
    alb = np.where(silt[..., None], alb * 0.7 + np.array((110, 116, 112)) * 0.3, alb)
    return relief(bottom, 0, w, h, hgt, mask, alb), bottom


GATE_H = 720
GATE_R, GATE_ARCHES = 196.0, 10


def gate_fields():
    w, h = W, GATE_H
    yy, xx = grid(h, w)
    cx, cy = w / 2, h / 2
    r = np.hypot(xx - cx, yy - cy)
    th = np.arctan2(yy - cy, xx - cx)
    hgt = np.zeros((h, w))
    span = TAU / GATE_ARCHES
    for k in range(GATE_ARCHES):
        a0 = k * span + 0.15
        dth = (th - a0 + np.pi) % TAU - np.pi
        inside = np.abs(dth) < span * 0.62
        prof = np.cos(np.clip(dth / (span * 0.62), -1, 1) * np.pi / 2)
        rib = np.exp(-((r - GATE_R) / (6 + 9 * prof)) ** 2) * (4 + 36 * prof) * inside
        hgt = np.maximum(hgt, rib)
        fx, fy = cx + math.cos(a0 + span / 2) * GATE_R, cy + math.sin(a0 + span / 2) * GATE_R   # a foot's buttress
        along = (xx - fx) * math.cos(a0 + span / 2) + (yy - fy) * math.sin(a0 + span / 2)
        across = -(xx - fx) * math.sin(a0 + span / 2) + (yy - fy) * math.cos(a0 + span / 2)
        but = np.clip(1 - (across / 9) ** 2, 0, 1) * np.clip(1 - along / 56, 0, 1) * (along > -8) * 16
        hgt = np.maximum(hgt, but)
    n = raster.fbm(w, h, 6, 2861, octaves=3, period=False)
    hgt = hgt + (hgt > 1) * 2.0 * n
    pit = np.clip(1 - r / (GATE_R - 18), 0, 1)
    return hgt, pit, r, th


def hatchery_gate():
    """The gate: a ring of grown chitin arches (bone-violet ribs with buttresses at their feet) over the hatchery's
    mouth, a dark resin pit inside the ring sinking toward its centre; mostly transparent outside."""
    w, h = W, GATE_H
    t = END
    bottom = int(round(scroll(t) + SCREEN / 2 - h / 2))
    hgt, pit, r, th = gate_fields()
    yy, xx = grid(h, w)
    floor = tile_rows("bowl-floor", bottom, h)[..., :3].astype(float)
    crust = raster.fbm(w, h, 5, 2862, octaves=3, period=False)
    alb = ramp([(70, 50, 72), (112, 88, 110), (162, 140, 150), (212, 198, 190)], np.clip(0.15 + hgt / 34 + 0.25 * crust, 0, 1))
    gy, gx = np.gradient(hgt)
    shade = np.clip(1.0 + (gx + gy) * 0.5, 0.35, 1.45)
    ribs = hgt > 1.5
    pit_rgb = floor * (1 - 0.85 * pit ** 0.6)[..., None] + np.array((4, 10, 12.0)) * pit[..., None]
    sm = np.roll(np.roll(ribs.astype(float), 9, 0), 7, 1)
    sm = np.array(Image.fromarray((sm * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(4))) / 255.0
    base = np.where((pit > 0)[..., None], pit_rgb, floor) * (1 - 0.45 * sm[..., None])
    rgb = np.where(ribs[..., None], alb * shade[..., None], base)
    outer = np.clip((GATE_R + 80 - r) / 30, 0, 1)
    cover = np.maximum(ribs.astype(float), np.maximum(outer * np.clip(sm * 2 + pit * 4, 0, 1), (pit > 0) * 1.0))
    cover *= np.clip(np.minimum(yy - 2, h - 3 - yy) / 6, 0, 1)
    return posterize(np.clip(rgb, 0, 255), 48, dither_alpha(np.clip(cover, 0, 1), 2)), bottom


def gate_glow():
    w, h = W, GATE_H
    hgt, pit, r, th = gate_fields()
    yy, xx = grid(h, w)
    cx, cy = w / 2, h / 2
    f = np.exp(-((r - GATE_R + 24) / 16) ** 2) * 0.22 + pit ** 4 * 0.16
    span = TAU / GATE_ARCHES
    for k in range(GATE_ARCHES):
        a = k * span + 0.15 + span / 2
        fx, fy = cx + math.cos(a) * (GATE_R - 6), cy + math.sin(a) * (GATE_R - 6)
        d = np.hypot(xx - fx, yy - fy)
        f = np.maximum(f, np.exp(-(d / 5) ** 2) + np.exp(-(d / 16) ** 2) * 0.4)
    f *= (hgt < 6)                                                  # the ribs' tops stay unlit by it
    f *= np.clip(np.minimum(yy - 2, h - 3 - yy) / 40, 0, 1)
    v = artkit.smooth_steps(f)
    rgb = TEAL[None, None] * v[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


# --------------------------------------------------------------------------- build

def _tile_job(name):
    return TILES[name]()


def _smoker_job(job):
    t, x, kind = job
    return smoker_piece(kind, t, x)


META = None


def build():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in list(OUT.glob("*.png")):
        old.unlink()
    with ProcessPoolExecutor() as ex:
        imgs = list(ex.map(_tile_job, list(TILES)))
    for name, img in zip(TILES, imgs):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)
    tile.cache_clear()
    pieces, placed = {}, []

    def put(name, img, note, t, x, frames=None, fps=None):
        if name not in pieces:
            if frames:
                for k, f in enumerate(frames):
                    artkit.save_png(f, OUT / f"{name}_{k}.png", SOURCE)
                pieces[name] = {"layer": "ground", "size": [frames[0].width, frames[0].height], "frames": len(frames),
                                "fps": fps}
            else:
                artkit.save_png(img, OUT / f"{name}.png", SOURCE)
                pieces[name] = {"layer": "ground", "size": [img.width, img.height]}
        if t is not None:
            placed.append((note, name, t, x))

    img, bottom = ridge_lip()
    put("ridge-lip", img, "the plain's last ridge at the start; exact rows", exact_t(bottom, img.height), 240)
    body = artkit.quantize_set([props.beacon_body(props.PULSE[k]) for k in range(props.PULSE_FRAMES)], 32)
    glow = [props.beacon_glow(k) for k in range(props.PULSE_FRAMES)]
    lip_row = scroll(RIDGE_LIP_T) + SCREEN / 2 + 8
    for i, x in enumerate(LIP_BEACONS):
        bb = int(round(lip_row - (props.BEACON[1] - 1 - props.BEACON_BASE[1])))
        put("relay-beacon", None, f"Levels 24-26's beacon {i + 1} on the ridge's lip (scenery; its light relay-beacon-"
            "glow, added over the grade)", exact_t(bb, props.BEACON[1]), x - props.BEACON_BASE[0] + props.BEACON[0] // 2,
            frames=body, fps=BEACON_FPS)
    put("relay-beacon-glow", None, "", None, None, frames=glow, fps=BEACON_FPS)
    with ProcessPoolExecutor() as ex:
        smokers = list(ex.map(_smoker_job, SMOKERS))
    for i, ((t, x, kind), (img, g, bottom, x0)) in enumerate(zip(SMOKERS, smokers), 1):
        name = f"smoker-{i}"
        put(name, img, f"black smoker cluster {i} (look {kind}) at t~{t:g}, x {x}: its mouths' heat {name}-glow; exact "
            "rows", exact_t(bottom, img.height), x0 + img.width // 2)
        put(f"{name}-glow", g, "", None, None)
    img, bottom, x0 = stack_piece(NEST_T, NEST_X, 176, 152, 0.55, 30, [340, 384, 428], 2871)
    put("nest-stack", img, f"the Spine Turret nest's chimney stack (t={NEST_T:g}: the three turrets on its top at x 340, "
        "384, 428); exact rows", exact_t(bottom, img.height), x0 + img.width // 2)
    img, g, bottom, x0 = marlin_stack()
    put("marlin-stack", img, f"the smoker the Marlin lies against and her scour (the trigger at t={MARLIN[0]:g}, x "
        f"{MARLIN[1]:g}); its mouth marlin-stack-glow", exact_t(bottom, img.height), x0 + img.width // 2)
    put("marlin-stack-glow", g, "", None, None)
    img, bottom, x0 = hull_wreck()
    put("hull-wreck", img, f"hint 3: the Depth Hunter wreck with its blistered hull code (mid-screen at t={kit.WRECK[0]:g}, "
        f"x {kit.WRECK[1]:g}); exact rows", exact_t(bottom, img.height), x0 + img.width // 2)
    img, bottom, x0 = mound_piece()
    put("mortar-mound", img, f"the Polyp Mortars' mound (t={MOUND[0]:g}, x 70 and 128); exact rows",
        exact_t(bottom, img.height), x0 + img.width // 2)
    img, bottom, x0 = stack_piece(SIREN_STACK[0], SIREN_STACK[1], 208, 160, 0.6, 38, [360, 430], 2881)
    put("siren-stack", img, f"the chimney stack the two Sirens are grown on (t={SIREN_STACK[0]:g}, x 360 and 430); exact "
        "rows", exact_t(bottom, img.height), x0 + img.width // 2)
    for i in range(len(kit.BEACONS)):
        img, bottom, x0 = crest_piece(i)
        t, x = kit.BEACONS[i]
        put(f"ridge-crest-{i + 1}", img, f"relay beacon {i + 1}'s knoll and footing (the unit at t={t:g}, x {x:g}; its foot "
            "on the pad); exact rows", exact_t(bottom, img.height), x0 + img.width // 2)
    for i, (t, x, seats) in enumerate(SHOULDERS, 1):
        img, bottom, x0 = shoulder_piece(t, x, seats, 2890 + i)
        put(f"ridge-shoulder-{i}", img, f"the ridge's shoulder {i} under its Spine Turret nest (t={t:g}); exact rows",
            exact_t(bottom, img.height), x0 + img.width // 2)
    for i in (1, 2, 3):
        img, bottom = seam_piece(i, 2900 + i)
        put(f"seam-{SECTION_TILES[i]}", img, f"section {i + 1}'s seam (ground {seam_row(i):.0f}): {SECTION_TILES[i - 1]} "
            f"into {SECTION_TILES[i]}; exact rows", exact_t(bottom, img.height), 240)
    img, bottom = bowl_rim()
    put("seam-bowl-floor", img, f"section 5's seam (ground {seam_row(4):.0f}): the bowl's rim, the plain falling away; "
        "exact rows", exact_t(bottom, img.height), 240)
    img, bottom = hatchery_gate()
    put("hatchery-gate", img, f"the hatchery gate's ring of arches, mid-screen at the level's end (t={END:g}); its teal "
        "light hatchery-gate-glow; exact rows", exact_t(bottom, img.height), 240)
    put("hatchery-gate-glow", gate_glow(), "", None, None)
    global META
    META = {"pieces": pieces, "placed": [list(p) for p in placed]}
    print(f"wrote {len(TILES)} tile sets and {len(pieces)} pieces to {OUT.relative_to(ROOT)}")


# --------------------------------------------------------------------------- proposal

DESC = {"vent-edge": "section 1: the plain's last silt going to basalt, tube-worm clumps",
        "smoker-field": "section 2: basalt silt, sulphide staining, pale bacterial mats, worms",
        "chimney-garden": "section 3: rougher basalt, heavier crust and mats",
        "ridge-basalt": "section 4: blue-grey pillow basalt ribbed along the ridge",
        "bowl-floor": "section 5: the bowl's floor, resin-stained silt and chitin root veins"}


def meta():
    if META is not None:
        return META
    block = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))["backdrop"]
    return {"pieces": block["pieces"], "placed": [["", p["piece"], p["t"], p["x"]] for p in block["placed"]]}


def _light_rows():
    """The static light pools (underwater_look's `lights:`): scroll = the ground row of the pool's centre less 540."""
    rows = []
    for i, (t, x, kind) in enumerate(SMOKERS, 1):
        row, mx = pool_of(kind, t, x)
        rows.append((int(round(row - SCREEN)), int(round(mx)), 100, "ffa040", f"smoker-{i} (look {kind}) at t~{t:g}"))
    t, x = MARLIN
    rows.append((int(round(target_centre(t, 0) + 40 - SCREEN)), int(x - 34), 90, "ffa040", "the Marlin's smoker"))
    for i, x in enumerate(LIP_BEACONS):
        rows.append((int(round(scroll(RIDGE_LIP_T) + SCREEN / 2 + 8 + 62 - SCREEN)), x, 40, "ff6040",
                     f"the old beacon {i + 1} on the ridge's lip (its 4 s pulse is the glow piece's)"))
    rows.append((int(round(scroll(END) + SCREEN / 2 - SCREEN)), 240, 240, "40ffc8", "the hatchery gate's teal"))
    return sorted(rows)


def plumes_block():
    lines = ["# the heat plumes this art is drawn for (G2's `vents:`; gap check §1.2): 16 rows at the README's times,",
             "# 2-3 discs each, the ripple's order alternating ends, neighbouring discs >= 164 px apart centre to centre",
             "# (a 100 px gap between 64 px discs); no backdrop piece within 48 px of a disc's centre",
             "vents:",
             "  look: heat-plume",
             "  shape: disc",
             "  width: 64",
             "  rows:"]
    for t, xs in PLUME_ROWS:
        lines.append(f"    - {{t: {t:g}, xs: [{', '.join(f'{x:g}' for x in xs)}]}}")
    return "\n".join(lines) + "\n"


def targets_block():
    lines = ["# ground places this art is drawn for (G2's `ground_targets:` and the Siren nest's `on_ground` xs; t = the top",
             "# edge, x the unit's centre):",
             f"#   Spine Turret nest x3 on the nest-stack: t {NEST_T:g}, x 340, 384, 428",
             f"#   the Marlin (`sprite: marlin-hauler`, size [64, 64], 20 / hard 28 HP): t {MARLIN[0]:g}, x {MARLIN[1]:g}",
             f"#   Polyp Mortar x2 on the mortar-mound: t {MOUND[0]:g}, x 70, 128",
             f"#   Siren x2 grown on the siren-stack (`on_ground` xs, every difficulty): t {SIREN_STACK[0]:g}, x 360, 430",
             "#   relay beacons (`enemy: relay-beacon`, size [72, 104], hitbox [36, 80]: the mast's foot 22 px below the",
             "#     unit's centre on its ridge-crest pad): " + ", ".join(f"t {t:g} x {x:g}" for t, x in kit.BEACONS),
             "#   Spine Turret x3 on the ridge's shoulders: t 99.5, x 130, 172 (shoulder 1) and 310 (shoulder 2);"
             " hard x5: + x 88 and 352",
             f"#   the hull-code wreck is the hull-wreck piece (mid-screen at t={kit.WRECK[0]:g}, x {kit.WRECK[1]:g}; Varga's "
             "t=70 line)"]
    return "\n".join(lines) + "\n"


def lights_block():
    lines = ["# static lights this art assumes (G2's underwater_look `lights:`; the pools are the game's light map, not",
             "# baked); the plumes' pools follow their phase in the game (PlumeLooks), not listed:",
             "#   lights:"]
    for s, x, r, col, note in _light_rows():
        lines.append(f"#     - {{scroll: {s}, x: {x}, radius: {r}, colour: {col}}}   # {note}")
    return "\n".join(lines) + "\n"


def proposal_block():
    m = meta()
    src = "the level's data.yaml" if kit.DATA else "the README and ART-G's layout (no data.yaml yet: re-run after G2)"
    lines = ["backdrop:",
             f"  # Presentation only: Level 27's vent field (tools/art/backdrop_l27.py, step ART-G, from {src}).",
             "  # Not dark: the grade's per-section underwater_look lights it (ART-G's proposal below), plus the smokers'",
             "  # amber pools (lights:) and the plumes' pools by phase. Every piece's -glow is added over the grade on its",
             "  # frame clock (drawPieceGlows): the smokers' mouths, the old beacons on the ridge's lip, the gate's teal.",
             "  scroll_factors: {far: 0.5, ground: 1, low-air: 1.35, high-air: 2.2}   # far unused, given as the loader asks",
             "  ramp: 2",
             "  haze_colour: 1a1410  # the vent-lit water",
             "  atmosphere:",
             "    clear: {haze: 0.0}",
             "  tile_sets:"]
    for name in TILES:
        lines.append(f"    {name}: {{layer: ground, height: {TILE_H}}}   # {DESC[name]}")
    lines.append("  pieces:")
    for name, spec in m["pieces"].items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']:g}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for note, piece, t, x in m["placed"]:
        if note:
            lines.append(f"    # {note}")
        lines.append(f"    - {{piece: {piece}, t: {t}, x: {x}}}")
    lines.append("# sections' tiles: " + "; ".join(f"{i + 1}: [{t}]" for i, t in enumerate(SECTION_TILES)))
    lines.append("# sections' underwater_look (ART-G's proposal, round 57; the level-wide block = section 1's):")
    for i, look in enumerate(kit.LOOK_YAML):
        lines.append(f"#   {i + 1}: underwater_look: {look}")
    return "\n".join(lines) + "\n"


def write_proposal():
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_block() + "\n" + plumes_block() + "\n" + targets_block() + "\n" + lights_block(),
                        encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# --------------------------------------------------------------------------- composite

def piece_frames(piece):
    spec = meta()["pieces"][piece]
    n = spec.get("frames")
    if n:
        return [np.array(Image.open(OUT / f"{piece}_{k}.png").convert("RGBA")) for k in range(n)]
    return [np.array(Image.open(OUT / f"{piece}.png").convert("RGBA"))]


def placed_pieces():
    out = []
    for note, piece, t, x in meta()["placed"]:
        imgs = piece_frames(piece)
        h, w = imgs[0].shape[:2]
        bottom = int(round(scroll(float(t)) + SCREEN / 2 - h / 2))
        glow = piece_frames(piece + "-glow") if piece + "-glow" in meta()["pieces"] else None
        fps = meta()["pieces"][piece].get("fps") or 0
        out.append((piece, bottom, int(round(float(x) - w / 2)), imgs, glow, fps))
    return out


def _paste_rows(img, arr, x0, y_top):
    h = arr.shape[0]
    if x0 < 0:
        arr, x0 = arr[:, -x0:], 0
    if x0 + arr.shape[1] > W:
        arr = arr[:, :W - x0]
    if -h < y_top < SCREEN:
        img.alpha_composite(Image.fromarray(np.ascontiguousarray(arr), "RGBA"), (x0, y_top))


def ground_view(t, pieces):
    base = scroll(t)
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for y in range(SCREEN):
        row = int(math.floor(base + SCREEN - 1 - y))
        out[y] = tile(SECTION_TILES[section_at_row(row)])[TILE_H - 1 - row % TILE_H]
    img = Image.fromarray(out, "RGBA")
    glows = []
    for name, bottom, x0, arrs, glow, fps in pieces:
        h = arrs[0].shape[0]
        y_top = int(round(base + SCREEN - 1 - (bottom + h - 1)))
        if y_top >= SCREEN or y_top + h <= 0:
            continue
        k = int(t * fps) % len(arrs) if fps else 0
        _paste_rows(img, arrs[k], x0, y_top)
        if glow is not None:
            g = glow[k % len(glow)]
            glows.append((Image.fromarray(g, "RGBA"), x0 + g.shape[1] / 2, y_top + g.shape[0] / 2, 1.0))
    return img, glows


def _held(name):
    try:
        return kit.load(name)
    except FileNotFoundError:
        return []


@lru_cache(maxsize=None)
def stand_ins():
    out = {}
    for kind, name in (("spine-turret", "spine-turret"), ("polyp-mortar", "polyp-mortar"), ("siren", "siren"),
                       ("relay-beacon", "relay-beacon"), ("marlin", "marlin-hauler")):
        f = _held(name)
        out[kind] = f[0] if f else None
    out["plume"] = _held("heat-plume")
    out["plume-glow"] = _held("heat-plume-glow")
    out["plume-column"] = _held("heat-plume-column")
    out["beacon-glow"] = _held("relay-beacon-glow")
    return out


def composite(t, pieces, ship=(240.0, 444.0)):
    img, pglows = ground_view(t, pieces)
    st = stand_ins()
    base = scroll(t)
    solids, glows, over = [], list(pglows), []
    for x, y, ph, u in kit.plumes_on_screen(t):
        if st["plume"]:
            k = 0 if ph == "cold" else (1 + min(2, int(u * 3)) if ph == "tell" else 3)
            solids.append((st["plume"][k], x, y))
            if ph == "tell" and st["plume-glow"]:
                glows.append((st["plume-glow"][min(2, int(u * 3))], x, y, 1.0))
            if ph == "burn" and st["plume-column"]:
                over.append((st["plume-column"][min(len(st["plume-column"]) - 1, int(u * len(st["plume-column"])))], x, y))
    for kind, tt, x in kit.TARGETS:
        im = st.get(kind)
        if im is None:
            continue
        y = base + SCREEN - target_centre(tt, im.height)
        if -80 < y < SCREEN + 80:
            solids.append((im, x, y))
            if kind == "relay-beacon" and st["beacon-glow"]:
                glows.append((st["beacon-glow"][int(t * BEACON_FPS) % 16], x, y, 1.0))
    pools = []
    for (tt, x, kind) in SMOKERS:
        row, mx = pool_of(kind, tt, x)
        y = base + SCREEN - 1 - row
        if -100 < y < SCREEN + 100:
            pools.append((mx, y, 100, 1.0))
    return kit.frame(img, t, solids=solids, glows=glows, ship_xy=ship, pools=pools, over=over)


# --------------------------------------------------------------------------- checks and review

def check():
    """Wraps (BackdropSeamsTest's measure), opaque tiles, sizes, the edge rule of BackdropAssetsTest, nothing opaque
    within 48 px of a plume disc's centre (seams aside), the plume rows' gaps, the targets clear of the plumes."""
    problems = []
    for name in TILES:
        a = tile(name)[..., :3].astype(float)
        jump = np.abs(a[0] - a[-1]).mean()
        inner = np.abs(a[1:] - a[:-1]).mean()
        if jump > 3 * inner + 6:
            problems.append(f"{name}: wrap seam {jump:.1f} vs {inner:.1f}")
        if b24.wrap_score(tile(name)) > 1.5:
            problems.append(f"{name}: wrap score {b24.wrap_score(tile(name)):.2f} (BackdropSeamsTest's limit 1.5)")
        if (tile(name)[..., 3] < 255).any():
            problems.append(f"{name}: a ground tile must be opaque")
    for name, spec in meta()["pieces"].items():
        for arr in piece_frames(name):
            if arr.shape[0] > MAX_PIECE or tuple(spec["size"]) != (arr.shape[1], arr.shape[0]):
                problems.append(f"{name}: size {arr.shape[1]}x{arr.shape[0]} (spec {spec['size']})")
                break
    centres = plume_centres()
    for name, bottom, x0, arrs, _, _ in placed_pieces():
        h, w = arrs[0].shape[:2]
        for arr in arrs:
            a = arr[..., 3] > 0
            if a[-1].any() and bottom > 0:
                problems.append(f"{name} (bottom {bottom}): opaque bottom row")
            if a[0].any() and scroll(END + OUTRO) + SCREEN > bottom + h:
                problems.append(f"{name} (bottom {bottom}): opaque top row comes on screen")
            if a[:, 0].any() and x0 > 0:
                problems.append(f"{name}: opaque left column at x {x0}")
            if a[:, -1].any() and x0 + w < W:
                problems.append(f"{name}: opaque right column at x {x0 + w}")
        if name.startswith("seam-"):
            continue
        for row, x in centres:
            ry = bottom + h - 1 - row
            lo_y, hi_y = int(max(0, ry - PLUME_CLEAR)), int(min(h, ry + PLUME_CLEAR))
            lo_x, hi_x = int(max(0, x - PLUME_CLEAR - x0)), int(min(w, x + PLUME_CLEAR - x0))
            if lo_y < hi_y and lo_x < hi_x:
                yy, xx = np.mgrid[lo_y:hi_y, lo_x:hi_x]
                near = np.hypot(yy - ry, xx + x0 - x) < PLUME_CLEAR
                if (arrs[0][lo_y:hi_y, lo_x:hi_x, 3][near] > 0).any():
                    problems.append(f"{name}: within {PLUME_CLEAR} px of the plume at t={time_at(row - SCREEN - 32):.1f} "
                                    f"x {x:g}")
    for t, xs in PLUME_ROWS:
        s = sorted(xs)
        if any(b - a < PLUME_WIDTH + 100 for a, b in zip(s, s[1:])):
            problems.append(f"plume row t={t:g}: discs closer than 164 px")
        if any(x - PLUME_WIDTH / 2 < 0 or x + PLUME_WIDTH / 2 > W for x in xs):
            problems.append(f"plume row t={t:g}: a disc leaves the field")
    for kind, t, x in kit.TARGETS:
        for row, px in centres:
            if math.hypot(x - px, target_centre(t, 0) - row) < 80:
                problems.append(f"{kind} at t={t:g} x {x:g}: within 80 px of a plume")
    print("check: " + ("ok" if not problems else f"{len(problems)} problems"))
    for p in sorted(set(problems)):
        print("  " + p)
    return problems


TIMES = [(3, "1 THE RIDGE'S LIP, OLD BEACONS"), (17, "1 THE FIRST SMOKERS"), (27.5, "2 THE FIRST PLUMES"),
         (36, "2 THE NEST STACK"), (42, "2 THE MARLIN'S SMOKER"), (63, "3 THE CHIMNEY GARDEN"),
         (70, "3 THE HULL-CODE WRECK"), (82.5, "3 MOUND AND SIREN STACK"), (103, "4 THE BEACON RIDGE"),
         (126, "5 THE BOWL'S RIM"), (138, "5 THE BOWL"), (150, "5 THE GATE")]


def review():
    g = placed_pieces()
    shots = [composite(t, g) for t, _ in TIMES]
    cols = 6
    cw, ch = 160, 180
    sheet = raster.sheet(16 + cols * (cw + 8) + 8, 60 + 2 * (ch + 24) + 40 + 300 + 260,
                         "LEVEL 27 BACKDROP - HYDROTHERMAL VENTS (FINAL)", f"PRODUCTION ART, {BATCH.upper()} - R57")
    raster.draw_text(sheet, 16, 38, "THE SCREEN AT 1/3 UNDER THE VENT LIGHT'S STAND-IN (0.45 BLUE + HEADLIGHTS / 0.6 AMBER / "
                     "0.6 TEAL BY SECTION, THE SMOKERS' AND PLUMES' POOLS)", raster.LABEL)
    for i, ((t, label), img) in enumerate(zip(TIMES, shots)):
        x, y = 16 + (i % cols) * (cw + 8), 56 + (i // cols) * (ch + 24)
        sheet.alpha_composite(img.resize((cw, ch), Image.Resampling.BOX), (x, y))
        raster.draw_text(sheet, x, y + ch + 4, f"T={t:g} {label}"[:27], raster.LABEL_DIM)
    y = 56 + 2 * (ch + 24) + 8
    raster.draw_text(sheet, 16, y, "TILES 1/4, UNLIT", raster.LABEL)
    for k, name in enumerate(TILES):
        im = Image.fromarray(tile(name), "RGBA").resize((120, 240), Image.Resampling.BOX)
        sheet.alpha_composite(im, (16 + k * 128, y + 16))
        raster.draw_text(sheet, 16 + k * 128, y + 260, name.upper()[:18], raster.LABEL_DIM)
    x0 = 16 + 5 * 128 + 8
    for k, name in enumerate(("smoker-1", "smoker-2", "smoker-3")):
        im = Image.fromarray(piece_frames(name)[0], "RGBA").resize((88, 88), Image.Resampling.BOX)
        sheet.alpha_composite(im, (x0 + (k % 2) * 96, y + 16 + (k // 2) * 120))
    raster.draw_text(sheet, x0, y + 260, "SMOKERS 1/2 (UNLIT)", raster.LABEL_DIM)
    y += 290
    raster.draw_text(sheet, 16, y, "AT 1X: THE HULL CODE AT T=70 (BLISTERED PAINT, 'HDX-417'), THE NEST STACK (T=36), "
                     "THE GATE (T=150)", raster.LABEL)
    sheet.alpha_composite(shots[6].crop((60, 160, 300, 400)), (16, y + 16))
    sheet.alpha_composite(shots[3].crop((240, 120, 480, 360)), (16 + 248, y + 16))
    sheet.alpha_composite(shots[11].crop((40, 80, 440, 320)), (16 + 2 * 248, y + 16))
    frames = [composite(t, g).resize((240, 270), Image.Resampling.BOX).convert("RGB")
              for t in np.arange(25, 35, 0.125)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=8)


def main(argv):
    CONCEPT.mkdir(parents=True, exist_ok=True)
    if "--check" in argv:
        sys.exit(1 if check() else 0)
    if "--review" in argv:
        review()
        return
    if "--proposal" in argv and (OUT / "vent-edge.png").exists() and PROPOSAL.exists():
        write_proposal()
        return
    build()
    write_proposal()
    check()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
