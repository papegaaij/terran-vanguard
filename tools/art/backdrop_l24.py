#!/usr/bin/env python3
"""Production art: the Level 24 backdrop, Abyssal Plain (design/campaign/act-4-deep-water/level-24-abyssal-plain;
M8 part D batch, concept round 53, step ART-D).

Straight to production from the round-48 mood still `europa-abyssal-plain-r48-a` (tools/concept/europa_r48.py
``plain_still``: the silt plain at the lamps' edge, warm behind and dark ahead, kelp, the grey beacon mast head with its
red-gold lamp), imported unchanged for its colours (PLAIN, WARM); structure as backdrop_l23.py (exact rows, the scroll
helpers; its ice ceiling imported unchanged, so the two levels' ceilings match), the beacon and pylon renders from
tools/art/l24_props.py. Posterized per image, wide gradients ordered-dithered.

**The grade is the game's** (C4's ``UnderwaterGrade`` with a per-section ``underwater_look``): the tiles carry the unlit
floor (ambient 1); the plain darkens section by section by the grade alone, "warm behind, dark ahead" (round 48 Q2 a).
ART-D's proposal for the ladder, shown in round 53 (the proposal file's comments carry the full blocks):
  section 1 The Plain's Edge   ambient 0.50   section 2 The Reef Field   0.32
  section 3 The Beacon Line    0.18           section 4 The Ray Crossing 0.14
The lamp pools of Thera Deep's last grow-lamp pylons (t~0 behind the ship, t~4, 12, 22) are baked into their pieces as
gain over the floor (floor x (1 + 1.4 g WARM) + 70 g WARM, Level 23's formula) and assume section 1's ambient 0.50 (at
0.50 x the grade they read as warm pools on a dim plain; Level 23 baked its at 0.58). The beacons' pulse is NOT baked:
the beacon pieces carry the mast (graded) and ``relay-beacon-glow`` is its additive light, drawn ungraded after the
grade (step D4) at the same placements and frame clock as ``relay-beacon``.

Tile sets (480x960, wrapping; per section):
  lamp-edge     ground, section 1: the silt plain at Thera Deep's edge, kelp stands in clumps, pale stones (Level 23's
                plain-edge continued)
  plain-reefs   ground, section 2: darker silt, stronger ridges, Vrell coral crust in small plum and bone tufts, thin
                violet root veins, sparse kelp (the spawning reefs themselves are units, nothing is baked under them)
  beacon-line   ground, section 3: smooth dark silt, scattered stones, almost no kelp
  deep-silt     ground, section 4: long low dunes of fine silt, empty
  ice-ceiling   high-air (x2.2), all sections: backdrop_l23.py's ice ceiling (faint cracks of light, additive at 40 %)
Pieces (ground; exact rows; nothing under the four spawning reefs, nothing within 100 px of a beacon's foot but its own
footing):
  thera-lamps         480x540 at t=0: two of Thera Deep's last pylons at the bottom edge with their pools (behind)
  lamp-pylon-1..3     208x208: the last grow-lamp pylons (t=4, 12, 22) with their pools baked
  floor-ridge         480 wide: a low rock ridge across the plain at t~18 (the Reef Spitter nest sits on it)
  seam-plain-reefs, seam-beacon-line, seam-deep-silt   480x320 on the section seams
  coral-stack-1..4    112x112: dead coral and rock stacks of the reef field (scenery: no iris, no teal; away from the
                      reefs)
  wreck-scar          176x176 at the secret (t~105, x 70): the scour and drag furrow the *Meridian* left in the silt,
                      debris plates, a snapped tow cable (the wreck itself is the ground target's sprite)
  beacon-footing-1..3 176x176: each beacon's concrete footing, its cable trenches toward the next beacon (buried, fading),
                      the mast's soft shadow on the silt
  relay-beacon        128x192, animated (16 frames at 8 fps: the 2 s pulse), the mast and its lamp lens; placed on each
                      footing (t=100 x 60, t=112 x 420, t=126 x 100: the mast's foot enters at the top edge then)
  relay-beacon-glow   128x192, animated, additive, NOT placed: the beacon's light for the game to add over the grade at
                      relay-beacon's placements (lamp core, halo, its light on the floor; gold flash, dim red at rest)
  trench-mouth        480 wide at the end: the plain's lip at the bottom edge at the level's end, the trench beyond it
                      falling into the black (covers the screen through the outro)
Review: design/campaign/act-4-deep-water/level-24-abyssal-plain/concept/backdrop-final-r53-a.png/.gif (stand-ins for the
units: reefs, nests, the wreck, the ship; the grade the review's stand-in, tools/art/l24kit.py); `--proposal` writes the
`backdrop:` block to the level's concept/backdrop-proposal.yaml (never into data.yaml).

Reads the level's data.yaml when it exists (sections, speeds, ramps, ground targets: the reefs, the wreck, nests),
otherwise the README's values. Re-run after the data moves a section or a target.
Run: python3 tools/art/backdrop_l24.py [--proposal | --review | --check]   (all: about 2 min)
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
import l24kit as kit
from artkit import DESIGN, ROOT, TAU, raster

import backdrop_l16 as b16  # noqa: E402  (tools/art: finish, dither_alpha)
import backdrop_l22 as b22  # noqa: E402  (tools/art: posterize)
import backdrop_l23 as b23  # noqa: E402  (tools/art: the ice ceiling, unchanged)
import europa_r48 as eu  # noqa: E402  (concept script, unchanged: Europa's colours, PLAIN, WARM)

SCRIPT = "backdrop_l24.py"
BATCH = "M8 part D batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r53"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-4-deep-water" / "level-24-abyssal-plain"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-24"
W, SCREEN = 480, 540
TILE_H = 960
FEATHER = 32
SHIP_Y = 96
OUTRO = 15                                   # LevelData.OUTRO_SECONDS: the scroll runs on through the outro
FACTORS = {"ground": 1.0, "low-air": 1.35, "high-air": 2.2}
WARM = np.array(eu.WARM, float)
LAMP = (255, 220, 160)
MAX_PIECE = 2040                             # AtlasPacker's 2048 page, less padding

# --------------------------------------------------------------------------- the level's data (or the README)

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
FALLBACK_SECTIONS = [{"end": 30, "speed": 100}, {"end": 95, "speed": 80, "ramp": 2},
                     {"end": 135, "speed": 100, "ramp": 2}, {"end": 172, "speed": 90, "ramp": 2}]
SECTIONS = DATA["sections"] if DATA else FALLBACK_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 100)) if DATA else 100.0
END = float(SECTIONS[-1]["end"])
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]
SECTION_TILES = [["lamp-edge", "ice-ceiling"], ["plain-reefs", "ice-ceiling"], ["beacon-line", "ice-ceiling"],
                 ["deep-silt", "ice-ceiling"]]
LOOKS = [  # ART-D's per-section underwater_look proposal (round 53): the ladder darkens, caustics thin with the light
    "{ambient: 0.5, tint: bfe6ff, caustics: 0.1, snow: 80, headlights: 0.6}",
    "{ambient: 0.32, tint: b4dcf5, caustics: 0.05, snow: 90, headlights: 0.75}",
    "{ambient: 0.18, tint: a8d0ee, caustics: 0.0, snow: 100, headlights: 0.85}",
    "{ambient: 0.14, tint: a0c8e8, caustics: 0.0, snow: 100, headlights: 0.9}"]


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


_T = np.arange(0, END + OUTRO + 16, DT)
_S = np.concatenate([[0.0], np.cumsum([speed_at(t + DT / 2) * DT for t in _T[:-1]])])


def scroll(t):
    return float(np.interp(t, _T, _S))


def time_at(s):
    return float(np.interp(s, _S, _T))


def seam_row(index):
    return scroll(STARTS[index]) + SCREEN


def target_centre(t, h):
    return scroll(t) + SCREEN + h / 2


def place_t(centre_row, factor=1.0):
    return time_at((centre_row - SCREEN / 2) / factor)


def exact_t(bottom, h):
    t = place_t(bottom + h / 2)
    for digits in (3, 4, 5, 6):
        tt = round(t, digits)
        if round(scroll(tt) + SCREEN / 2 - h / 2) == bottom:
            return tt
    return round(t, 6)


def section_at_row(row):
    s = 0
    for i in range(1, len(SECTIONS)):
        if row >= seam_row(i):
            s = i
    return s


def _ats(g):
    a = g.get("at")
    if a is None:
        return []
    return a if isinstance(a, list) and a and isinstance(a[0], list) else [a]


def _targets():
    """(kind, t, x) of the ground targets: the data's, else the README's."""
    if DATA:
        out = []
        for g in DATA.get("ground_targets") or []:
            kind = g.get("enemy") or ("wreck" if g.get("trigger") or g.get("reveals") else g.get("sprite", "?"))
            for at in _ats(g):
                out.append((str(kind), float(at[0]), float(at[1])))
        return out
    return ([("reef-spitter", 18, 150), ("reef-spitter", 18.6, 300)]
            + [("spawning-reef", t, x) for t, x in ((36, 120), (48, 350), (64, 130), (80, 340))]
            + [("reef-spitter", 50, 250), ("reef-spitter", 66, 220), ("wreck", 105, 70),
               ("reef-spitter", 114, 380), ("reef-spitter", 114.6, 300)])


TARGETS = _targets()
REEFS = [(t, x) for k, t, x in TARGETS if k == "spawning-reef"]
WRECK_AT = next(((t, x) for k, t, x in TARGETS if k == "wreck"), (105, 70))
BEACONS = [(100, 60), (112, 420), (126, 100)]                 # the README: t, x of each mast's foot
PYLONS = [(4, 384), (12, 92), (22, 396)]
STACKS = [(42, 410), (56, 64), (72, 410), (89, 230)]          # dead coral stacks in the reef field, clear of the reefs
RIDGE_T = 18

# --------------------------------------------------------------------------- helpers

pn, ramp, finish, dither_alpha = b16.pn, b16.ramp, b16.finish, b16.dither_alpha
posterize = b22.posterize


def grid(h=TILE_H, w=W):
    return np.mgrid[0:h, 0:w].astype(np.float64)


@lru_cache(maxsize=None)
def tile(name):
    return np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))


def tile_rows(name, bottom, h):
    t = tile(name)
    rows = bottom + h - 1 - np.arange(h)
    return t[(TILE_H - 1 - np.mod(rows, TILE_H)).astype(int)]


def floor_rows(bottom, h):
    """Ground rows bottom..bottom+h as the game lays them, each from its section's tile set."""
    out = np.zeros((h, W, 4), np.uint8)
    for k in range(h):
        row = bottom + h - 1 - k
        out[k] = tile(SECTION_TILES[section_at_row(row)][0])[TILE_H - 1 - int(row) % TILE_H]
    return out


def feather_alpha(h, w):
    k = np.arange(h)[:, None]
    a = np.minimum(np.clip((k + 0.5) / FEATHER, 0, 1), np.clip((h - k - 0.5) / FEATHER, 0, 1))
    return dither_alpha(np.broadcast_to(a, (h, w)).copy(), 2)


# --------------------------------------------------------------------------- tile sets

PLAIN_STOPS = [tuple(int(c) for c in s) for s in eu.PLAIN]


def silt_floor(seed, gain, relief=0.3, ridge=0.18, ripples=0.006, rfreq=80, kelp=0, clumps=False, stones=40, coral=0,
               veins=0):
    """The plain's silt (Level 23's plain-edge recipe): periodic fbm dunes, sharp ridges, ripples, shaded from the
    upper left; then kelp, stones, coral crust and root veins drawn wrapping."""
    yy, xx = grid()
    n = pn(W, TILE_H, 96, seed, 5)
    rd = 1 - np.abs(2 * pn(W, TILE_H, 48, seed + 1, 2) - 1)
    hgt = 0.42 + relief * n + ridge * rd ** 4
    hgt += ripples * np.sin(TAU * (yy + 0.35 * xx + 60 * pn(W, TILE_H, 96, seed + 2, 2)) * rfreq / TILE_H)
    stops = [tuple(min(255, int(c * gain)) for c in s) for s in PLAIN_STOPS]
    rgb = eu.m36.shade_rgb(hgt, stops, strength=16, shadow=0.004, periodic=True)
    rgb *= (0.92 + 0.16 * pn(W, TILE_H, 8, seed + 5, 2))[..., None]
    img = posterize(rgb, 32)
    lay = Image.new("RGBA", (W, TILE_H), (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    rng = np.random.default_rng(seed + 9)

    def wrap(fn):
        for dy in (-TILE_H, 0, TILE_H):
            fn(dy)

    for _ in range(veins):                                          # thin Vrell root veins, violet-brown
        x, y, a = rng.uniform(0, W), rng.uniform(0, TILE_H), rng.uniform(0, TAU)
        pts = [(x, y)]
        for _ in range(int(rng.integers(5, 10))):
            a += rng.normal(0, 0.5)
            x, y = x + math.cos(a) * 9, y + math.sin(a) * 9
            pts.append((x, y))
        wrap(lambda dy: d.line([(px, py + dy) for px, py in pts], fill=(74, 44, 70, 255), width=1))
    for _ in range(kelp):                                           # kelp: tufts, or stands of tufts
        cx, cy = rng.uniform(10, W - 10), rng.uniform(0, TILE_H)
        for _ in range(int(rng.integers(3, 6)) if clumps else 1):
            tx, ty = cx + rng.normal(0, 9 if clumps else 0), cy + rng.normal(0, 7 if clumps else 0)
            col = (30, 84, 62) if rng.random() < 0.6 else (24, 66, 52)
            for _ in range(int(rng.integers(4, 8))):
                a, ln = rng.uniform(0, TAU), rng.uniform(6, 16)
                c = tuple(int(v * rng.uniform(0.75, 1.2)) for v in col) + (255,)
                tip = tuple(min(255, int(v * 1.6)) for v in col) + (255,)
                ex, ey = tx + math.cos(a) * ln, ty + math.sin(a) * ln
                wrap(lambda dy, tx=tx, ty=ty, ex=ex, ey=ey, c=c, tip=tip: (
                    d.line([tx, ty + dy, ex, ey + dy], fill=c, width=2), d.point([(ex, ey + dy)], fill=tip)))
    for _ in range(coral):                                          # coral crust: small plum and bone tufts
        cx, cy = rng.uniform(8, W - 8), rng.uniform(0, TILE_H)
        hue = (104, 52, 112) if rng.random() < 0.6 else (168, 154, 134)
        for _ in range(int(rng.integers(4, 9))):
            a, rr = rng.uniform(0, TAU), rng.uniform(0, 6)
            x, y, s = cx + math.cos(a) * rr, cy + math.sin(a) * rr, rng.uniform(1.0, 2.2)
            hi = tuple(min(255, c + 50) for c in hue) + (255,)
            wrap(lambda dy, x=x, y=y, s=s, hi=hi: (
                d.ellipse([x - s, y + dy - s, x + s, y + dy + s], fill=hue + (255,), outline=(36, 20, 40, 255)),
                d.point([(x - s * 0.4, y + dy - s * 0.4)], fill=hi)))
    for _ in range(stones):                                         # pale stones and shells
        cx, cy, r = rng.uniform(0, W), rng.uniform(0, TILE_H), rng.uniform(1.0, 2.4)
        wrap(lambda dy, cx=cx, cy=cy, r=r: (
            d.ellipse([cx - r, cy + dy - r, cx + r, cy + dy + r], fill=(120, 130, 118, 255)),
            d.point([(cx - r * 0.4, cy + dy - r * 0.4)], fill=(170, 176, 160, 255))))
    img.alpha_composite(lay)
    return img


TILES = {"lamp-edge": lambda: silt_floor(2401, 1.45, kelp=34, clumps=True, stones=60),
         "plain-reefs": lambda: silt_floor(2411, 1.32, relief=0.34, ridge=0.28, kelp=10, stones=40, coral=46,
                                           veins=26),
         "beacon-line": lambda: silt_floor(2421, 1.25, relief=0.26, ridge=0.14, kelp=4, stones=80),
         "deep-silt": lambda: silt_floor(2431, 1.2, relief=0.2, ridge=0.08, ripples=0.016, rfreq=40, stones=24),
         "ice-ceiling": b23.ice_ceiling}

# --------------------------------------------------------------------------- pieces

def lamp_gain(lamps, h, w, x0, bottom):
    yy, xx = grid(h, w)
    rows = bottom + h - 1 - yy
    g = np.zeros((h, w))
    for lx, lr, r, k in lamps:
        g += k * np.exp(-(((xx + x0) - lx) ** 2 + (rows - lr) ** 2) / (2 * r * r))
    return np.clip(g, 0, 1.3)


def lit_piece(bottom, x0, w, h, draw, lamps, colours=40, edge_fade=48):
    """The floor rows under a piece, the drawing over them, lit by the lamps (Level 23's formula); opaque where lit or
    drawn, dithered out where the pool fades (those pixels equal the floor under them anyway)."""
    floor = floor_rows(bottom, h)[:, x0:x0 + w, :3].astype(float)
    ss = 2
    big = Image.fromarray(floor.astype(np.uint8), "RGB").convert("RGBA").resize((w * ss, h * ss), Image.NEAREST)
    lay = Image.new("RGBA", big.size, (0, 0, 0, 0))
    draw(lay, ss)
    big.alpha_composite(lay)
    rgb = np.array(big.resize((w, h), Image.BOX).convert("RGB")).astype(float)
    cover = np.array(lay.resize((w, h), Image.BOX))[..., 3] / 255.0
    g = lamp_gain(lamps, h, w, x0, bottom)
    yy, xx = grid(h, w)
    dl = xx if x0 > 0 else np.full_like(xx, 1e9)
    dr = (w - 1 - xx) if x0 + w < W else np.full_like(xx, 1e9)
    edge = np.minimum(np.minimum(dl, dr), np.minimum(yy, h - 1 - yy))
    u = np.clip(edge / edge_fade, 0, 1)
    g = g * u * u * (3 - 2 * u)
    rgb = rgb * (1 + 1.4 * g[..., None] * WARM) + 70 * g[..., None] * WARM
    a = np.clip((g - 0.03) / 0.10, 0, 1)
    a = np.maximum(a, (cover > 0.5).astype(float))
    a[0, :] = 0
    a[-1, :] = 0
    if x0 > 0:
        a[:, 0] = 0
    if x0 + w < W:
        a[:, -1] = 0
    return posterize(np.clip(rgb, 0, 255), colours, dither_alpha(a, 2))


def _paste_scaled(lay, img, cx, cy, ss):
    big = img.resize((img.width * ss, img.height * ss), Image.NEAREST)
    lay.alpha_composite(big, (int(round(cx * ss - big.width / 2)), int(round(cy * ss - big.height / 2))))


def pylon_draw(spots):
    """A draw function laying pylon renders (their foot at each (x, y) of the piece) with their soft shadows."""
    head = props.pylon_head()
    sh = artkit.sprite.shadow_of(head, opacity=0.5, blur=2.0)
    ox = head.width / 2 - props.PYLON_BASE[0]
    oy = head.height / 2 - props.PYLON_BASE[1]

    def draw(lay, ss):
        for x, y in spots:
            _paste_scaled(lay, sh, x + ox + 5, y + oy + 7, ss)
            _paste_scaled(lay, head, x + ox, y + oy, ss)
        return True
    return draw


def pylon_piece(t, x):
    w = h = 208
    row0 = target_centre(t, 0)
    x0 = int(np.clip(x - w / 2, 0, W - w))
    bottom = int(round(row0 - h / 2))
    cx, cy = x - x0, h / 2
    lamps = [(x, row0, 58, 0.5), (x, row0 + 10, 22, 0.2)]
    return lit_piece(bottom, x0, w, h, pylon_draw([(cx, cy)]), lamps, colours=40), bottom, x0


def thera_lamps():
    """t=0: two of Thera Deep's last pylons at the bottom edge, their pools warming the screen's lower half."""
    h, bottom, x0 = SCREEN, 0, 0
    spots = [(118, 64), (372, 22)]                      # (x, ground row)
    lamps = [(x, r, 92, 0.6) for x, r in spots] + [(x, r + 8, 30, 0.25) for x, r in spots]

    def draw(lay, ss):
        return pylon_draw([(x, h - 1 - r) for x, r in spots])(lay, ss)
    img = lit_piece(bottom, x0, W, h, draw, lamps, colours=48, edge_fade=1)
    a = np.array(img)
    a[-1, :, :] = a[-2, :, :]                        # the bottom row may stay lit: it sits on the edge at t=0
    k = np.arange(h)[:, None]
    fade = np.clip((k + 0.5) / FEATHER, 0, 1)        # only the top fades
    keep = dither_alpha(np.broadcast_to(fade, (h, W)).copy(), 2) > 0
    a[..., 3] = np.where(keep, a[..., 3], 0)
    a[a[..., 3] == 0] = 0
    return Image.fromarray(a, "RGBA"), bottom


def seam_piece(index, seed, band, h=320):
    lower, upper = SECTION_TILES[index - 1][0], SECTION_TILES[index][0]
    seam = seam_row(index)
    bottom = int(round(seam)) - h // 2
    lo = tile_rows(lower, bottom, h)[..., :3].astype(float)
    up = tile_rows(upper, bottom, h)[..., :3].astype(float)
    yy, xx = grid(h)
    n = raster.fbm(W, h, 40, seed, octaves=4, period=False)
    line = h / 2 + 34 * np.sin(xx / 83 + seed) + 30 * (n - 0.5) * 2
    rgb = np.where((yy < line)[..., None], up, lo)
    img = posterize(rgb, 40, feather_alpha(h, W))
    rng = np.random.default_rng(seed)
    lay = Image.new("RGBA", (W, h), (0, 0, 0, 0))
    dr = ImageDraw.Draw(lay)
    for _ in range(46):
        x = rng.uniform(0, W)
        yl = h / 2 + 34 * math.sin(x / 83 + seed) + 30 * (n[int(h / 2), int(min(W - 1, x))] - 0.5) * 2
        y = yl + rng.normal(0, 12)
        if band == "reef":
            hue = (104, 52, 112) if rng.random() < 0.6 else (168, 154, 134)
            for _ in range(5):
                a, rr = rng.uniform(0, TAU), rng.uniform(0, 5)
                s = rng.uniform(1.0, 2.0)
                px, py = x + math.cos(a) * rr, y + math.sin(a) * rr
                dr.ellipse([px - s, py - s, px + s, py + s], fill=hue + (255,), outline=(36, 20, 40, 255))
        else:
            for _ in range(3):
                a, ln = rng.uniform(0, TAU), rng.uniform(4, 10)
                dr.line([x, y, x + math.cos(a) * ln, y + math.sin(a) * ln], fill=(28, 70, 54, 255), width=2)
    la = np.array(lay)
    la[..., 3] = np.where(np.array(img)[..., 3] > 0, la[..., 3], 0)
    img.alpha_composite(Image.fromarray(la, "RGBA"))
    return img, bottom


def relief_piece(bottom, x0, w, h, hgt, mask, albedo, colours=36, shadow=(5, 7)):
    """A shaded relief (lit from the upper left) over the floor rows: opaque where ``mask`` (0..1) covers, with a soft
    cast shadow toward the lower right on the floor round it."""
    floor = floor_rows(bottom, h)[:, x0:x0 + w, :3].astype(float)
    gy, gx = np.gradient(hgt)
    shade = np.clip(1.0 - (gx + gy) * 1.6, 0.35, 1.5)
    rgb = albedo * shade[..., None]
    m = np.clip(mask, 0, 1)
    sm = np.roll(np.roll(m, shadow[1], 0), shadow[0], 1)
    sm = np.array(Image.fromarray((sm * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(3))) / 255.0
    under = floor * (1 - 0.45 * sm[..., None])
    out = np.where((m > 0.5)[..., None], rgb, under)
    a = np.maximum((m > 0.5).astype(float), np.clip(sm * 1.6, 0, 1) * (sm > 0.08))
    a[0, :] = a[-1, :] = 0
    if x0 > 0:
        a[:, 0] = 0
    if x0 + w < W:
        a[:, -1] = 0
    return posterize(np.clip(out, 0, 255), colours, dither_alpha(a, 2))


def lumps(w, h, blobs, seed):
    """A height field of rounded lumps (cx, cy, rx, ry, height) with a crust noise."""
    yy, xx = grid(h, w)
    hgt = np.zeros((h, w))
    for cx, cy, rx, ry, z in blobs:
        d = ((xx - cx) / rx) ** 2 + ((yy - cy) / ry) ** 2
        hgt = np.maximum(hgt, z * np.sqrt(np.clip(1 - d, 0, 1)))
    crust = raster.fbm(w, h, 6, seed, octaves=3, period=False)
    return hgt + (hgt > 0) * 2.2 * crust, crust


def ridge_piece():
    """A low rock ridge across the plain at t~18: a line of silted rock humps running from lower left to upper right."""
    w, h = W, 176
    row0 = target_centre(RIDGE_T, 0)
    bottom = int(round(row0 - h / 2))
    rng = np.random.default_rng(2461)
    blobs = []
    for k in range(40):
        x = -16 + k * 13 + rng.uniform(-6, 6)
        y = h / 2 + 34 - x * 0.14 + 14 * math.sin(x / 47) + rng.normal(0, 7)
        blobs.append((x, y, rng.uniform(16, 30), rng.uniform(12, 22), rng.uniform(7, 13)))
    hgt, crust = lumps(w, h, blobs, 2462)
    mask = (hgt > 0.6).astype(float)
    yy, _ = grid(h, w)
    mask *= np.clip(np.minimum(yy - 4, h - 5 - yy) / 4, 0, 1)
    alb = ramp([(40, 48, 46), (66, 74, 68), (92, 100, 90), (116, 122, 108)], 0.15 + hgt / 22 + 0.3 * crust)
    silt = (hgt > 9) & (crust > 0.55)                            # silt lying on the crests
    alb = np.where(silt[..., None], alb * 0.7 + np.array((110, 126, 112)) * 0.3, alb)
    return relief_piece(bottom, 0, w, h, hgt, mask, alb), bottom, 0


def stack_piece(i, t, x):
    """A dead coral and rock stack in the reef field: lumpy, plum-grey and bone, no iris, no light (scenery)."""
    w = h = 112
    row0 = target_centre(t, 0)
    x0 = int(np.clip(x - w / 2, 0, W - w))
    bottom = int(round(row0 - h / 2))
    rng = np.random.default_rng(2470 + i)
    cx, cy = x - x0, h / 2
    blobs = [(cx, cy, 26, 23, 18)]
    for _ in range(16):
        a, r = rng.uniform(0, TAU), rng.uniform(6, 32)
        blobs.append((cx + math.cos(a) * r, cy + math.sin(a) * r * 0.85, rng.uniform(7, 14), rng.uniform(7, 13),
                      rng.uniform(5, 15) * (1 - r / 46)))
    hgt, crust = lumps(w, h, blobs, 2475 + i)
    mask = (hgt > 0.6).astype(float)
    plum = np.array((74, 52, 74.0))
    bone = np.array((150, 140, 124.0))
    k = np.clip(0.3 + 0.7 * crust + hgt / 40, 0, 1)[..., None]
    alb = plum * (1 - k) + bone * k
    pits = (raster.fbm(w, h, 4, 2480 + i, octaves=2, period=False) > 0.66) & (hgt > 3)
    alb = np.where(pits[..., None], alb * 0.55, alb)
    return relief_piece(bottom, x0, w, h, hgt, mask, alb, colours=32), bottom, x0


def wreck_scar():
    """The scour the Meridian dug: a long hollow in the silt (the hull lies in it), a drag furrow from the upper right,
    debris plates and a snapped tow cable; the hollow's centre at the wreck's centre."""
    w = h = 176
    t, x = WRECK_AT
    row0 = target_centre(t, 48)
    x0 = int(np.clip(x - w / 2, 0, W - w))
    bottom = int(round(row0 - h / 2))
    cx, cy = x - x0, h / 2
    yy, xx = grid(h, w)
    ax, ay = math.cos(0.42), -math.sin(0.42)                     # the hull's axis in image coords (nose up-right)
    u = (xx - cx) * ax + (yy - cy) * ay
    v = -(xx - cx) * ay + (yy - cy) * ax
    hollow = np.clip(1 - (u / 48) ** 2 - (v / 20) ** 2, 0, 1)
    furrow = np.clip(1 - (v / 9) ** 2, 0, 1) * np.clip((u - 28) / 10, 0, 1) * np.clip((100 - u) / 30, 0, 1)
    berm = np.clip(1 - ((np.sqrt((u / 48) ** 2 + (v / 20) ** 2) - 1.15) / 0.22) ** 2, 0, 1)
    berm += np.clip(1 - ((np.abs(v) - 11) / 4) ** 2, 0, 1) * (furrow > 0.01) * np.clip((96 - u) / 30, 0, 1)
    n = raster.fbm(w, h, 8, 2491, octaves=3, period=False)
    hgt = 6 * berm - 7 * hollow - 4 * furrow + 1.5 * n
    floor = floor_rows(bottom, h)[:, x0:x0 + w, :3].astype(float)
    gy, gx = np.gradient(hgt)
    shade = np.clip(1.0 - (gx + gy) * 0.5, 0.45, 1.4)
    rgb = floor * shade[..., None] * (1 - 0.25 * hollow)[..., None]
    cover = np.clip(np.maximum.reduce([hollow * 4, furrow * 3, berm * 2]), 0, 1)
    img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(2492)
    plates = Image.new("L", (w, h), 0)
    dp = ImageDraw.Draw(plates)
    for _ in range(9):                                            # debris plates along the furrow and round the hull
        uu, vv = rng.uniform(-30, 90), rng.normal(0, 16)
        px, py = cx + uu * ax - vv * ay, cy + uu * ay + vv * ax
        s, a = rng.uniform(2.5, 5), rng.uniform(0, TAU)
        pts = [(px + s * math.cos(a + k * TAU / 4) * (1.5 if k % 2 else 1), py + s * math.sin(a + k * TAU / 4))
               for k in range(4)]
        col = (150, 80, 36) if rng.random() < 0.6 else (150, 152, 148)
        d.polygon(pts, fill=col + (255,), outline=(40, 34, 30, 255))
        dp.polygon(pts, fill=255)
    cab = [(cx - 30 * ax, cy - 30 * ay)]                         # the snapped tow cable, trailing from the stern
    a = math.atan2(-ay, -ax) + 0.5
    for _ in range(9):
        a += rng.normal(0, 0.35)
        cab.append((cab[-1][0] + math.cos(a) * 6, cab[-1][1] + math.sin(a) * 6))
    d.line(cab, fill=(26, 26, 28, 255), width=2)
    d.line(cab, fill=(200, 160, 40, 255), width=1)
    dp.line(cab, fill=255, width=2)
    rgb = np.array(img)[..., :3].astype(float)
    cover = np.maximum(cover, np.array(plates) / 255.0)
    cover *= np.clip(np.minimum.reduce([yy - 2, h - 3 - yy, xx - 2 if x0 > 0 else np.full_like(xx, 9),
                                        (w - 3 - xx) if x0 + w < W else np.full_like(xx, 9)]) / 6, 0, 1)
    return posterize(rgb, 36, dither_alpha(cover, 2)), bottom, x0


def footing_piece(i):
    """A beacon's footing: a square concrete pad, cable trenches toward the neighbouring beacons (buried after a short
    run, fading out), a ring of scoured silt and the mast's soft shadow toward the lower right."""
    w = h = 176
    t, x = BEACONS[i]
    row0 = target_centre(t, 0)
    x0 = int(round(x - w / 2))
    bottom = int(round(row0 - h / 2))
    cx, cy = x - x0, h / 2
    xs = np.arange(w) + x0
    floor = np.zeros((h, w, 3))
    fr = floor_rows(bottom, h)
    lo, hi = max(0, x0), min(W, x0 + w)
    floor[:, lo - x0:hi - x0] = fr[:, lo:hi, :3]
    yy, xx = grid(h, w)
    dirs = []
    for j in (i - 1, i + 1):
        if 0 <= j < len(BEACONS):
            tj, xj = BEACONS[j]
            dx, dy = xj - x, -(target_centre(tj, 0) - row0)
            L = math.hypot(dx, dy)
            dirs.append((dx / L, dy / L))
    trench = np.zeros((h, w))
    for ux, uy in dirs:
        along = (xx - cx) * ux + (yy - cy) * uy
        across = -(xx - cx) * uy + (yy - cy) * ux
        run = np.clip((86 - along) / 40, 0, 1) * (along > 8)
        trench = np.maximum(trench, np.clip(1 - (across / 2.6) ** 2, 0, 1) * run)
        trench = np.maximum(trench, 0.45 * np.clip(1 - ((np.abs(across) - 4) / 2) ** 2, 0, 1) * run)
    r = np.hypot(xx - cx, yy - cy)
    scour = np.clip(1 - ((r - 20) / 8) ** 2, 0, 1)
    pad = (np.abs(xx - cx) < 11) & (np.abs(yy - cy) < 11)
    # the mast's shadow: from the foot toward the lower right (the key light is upper left)
    su = (xx - cx) * 0.62 + (yy - cy) * 0.78
    sv = -(xx - cx) * 0.78 + (yy - cy) * 0.62
    shadow = np.clip(1 - (sv / 3.5) ** 2, 0, 1) * np.clip(su / 6, 0, 1) * np.clip((60 - su) / 30, 0, 1)
    shadow = np.array(Image.fromarray((shadow * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(1.5))) / 255.0
    rgb = floor * (1 - 0.35 * trench - 0.2 * scour + 0.06 * (scour > 0.6))[..., None]
    rgb *= (1 - 0.4 * shadow)[..., None]
    conc = np.array((132, 134, 128.0)) * (0.9 + 0.2 * raster.fbm(w, h, 4, 2500 + i, octaves=2,
                                                                 period=False))[..., None]
    conc = np.where(((np.abs(xx - cx) > 9) | (np.abs(yy - cy) > 9))[..., None] & pad[..., None], conc * 0.75, conc)
    conc = np.where(((xx - cx < -9) | (yy - cy < -9))[..., None] & pad[..., None], conc * 1.25, conc)
    rgb = np.where(pad[..., None], conc, rgb)
    cover = np.clip(np.maximum.reduce([trench * 3, scour * 3, shadow * 3, pad.astype(float)]), 0, 1)
    cover[:, xs < 0] = 0
    cover[:, xs >= W] = 0
    cover[0, :] = cover[-1, :] = 0
    return posterize(np.clip(rgb, 0, 255), 32, dither_alpha(cover, 2)), bottom, x0


def trench_mouth():
    """The level's end: the plain's ragged lip at the bottom edge when the level ends, the trench beyond it falling away
    into the black, its walls faint at the sides; tall enough to cover the screen through the outro."""
    lip = scroll(END)
    bottom = int(math.floor(lip - 96))
    top = int(math.ceil(scroll(END + OUTRO) + SCREEN + 8))
    h = top - bottom
    assert h <= MAX_PIECE, f"trench-mouth is {h} rows"
    yy, xx = grid(h)
    rows = bottom + h - 1 - yy
    floor = floor_rows(bottom, h)[..., :3].astype(float)
    n = raster.fbm(W, h, 32, 2511, octaves=4, period=False)
    edge = lip + 14 * np.sin(xx / 37 + 0.6) + 8 * np.sin(xx / 11) + 20 * (n - 0.5)
    d = rows - edge                                               # px beyond the lip
    fall = np.clip(d / 260, 0, 1)
    wall = raster.fbm(W, h, 16, 2512, octaves=3, period=False)
    strata = 0.5 + 0.5 * np.sin((rows * 0.05 + 3 * wall))
    deep = np.array((8, 20, 28.0)) * (1 - fall[..., None]) + np.array((2, 6, 10.0)) * fall[..., None]
    rock = np.array((40, 56, 60.0)) * (0.6 + 0.5 * strata * (1 - fall))[..., None]
    rgb = np.where((d > 0)[..., None], deep + rock * np.clip(1 - d / 140, 0, 1)[..., None] * 0.9, floor)
    side = np.minimum(xx, W - 1 - xx)                             # the trench's walls at the field's sides, faint
    wall_w = 70 + 16 * np.sin(rows / 70.0 + 1) + 7 * np.sin(rows / 23.0)
    on_wall = (d > 60) & (side < wall_w)
    wcol = np.array((30, 42, 46.0)) * (0.55 + 0.45 * wall)[..., None] * (0.6 + 0.4 * (side / wall_w))[..., None]
    rgb = np.where(on_wall[..., None], wcol, rgb)
    near = (d <= 0) & (d > -60)                                   # the slope down to the lip darkens
    rgb = np.where(near[..., None], rgb * (0.65 + 0.35 * np.clip(-d / 60, 0, 1))[..., None], rgb)
    blocks = (raster.fbm(W, h, 6, 2513, octaves=2, period=False) > 0.5) & (d > -8) & (d < 3)
    rgb = np.where(blocks[..., None], np.array((96, 108, 100.0)) * (0.8 + 0.4 * n)[..., None], rgb)
    rgb = np.where(((d > 0) & (d < 2))[..., None], np.array((126, 136, 124.0)), rgb)   # the lit lip
    alpha = np.full((h, W), 255.0)
    k = np.arange(h)[:, None]
    fade = np.clip((h - k - 0.5) / FEATHER, 0, 1)                # only the bottom fades into the silt
    alpha = np.where(dither_alpha(np.broadcast_to(fade, (h, W)).copy(), 2) > 0, 255, 0)
    img = posterize(rgb, 40, alpha)
    a = np.array(img)
    a[0, :, 3] = 0                                                # the top row (off screen through the outro)
    a[a[..., 3] == 0] = 0
    return Image.fromarray(a, "RGBA"), bottom


# --------------------------------------------------------------------------- build

def _tile_job(name):
    return TILES[name]()


def _beacon_frames():
    body = [props.beacon_body(props.PULSE[k]) for k in range(props.PULSE_FRAMES)]
    body = artkit.quantize_set(body, 32)
    glow = [props.beacon_glow(k) for k in range(props.PULSE_FRAMES)]
    return body, glow


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

    def put(name, img, layer, note, t, x, frames=None):
        if name not in pieces:
            if frames:
                for k, f in enumerate(frames):
                    artkit.save_png(f, OUT / f"{name}_{k}.png", SOURCE)
                pieces[name] = {"layer": layer, "size": [frames[0].width, frames[0].height], "frames": len(frames),
                                "fps": props.PULSE_FPS}
            else:
                artkit.save_png(img, OUT / f"{name}.png", SOURCE)
                pieces[name] = {"layer": layer, "size": [img.width, img.height]}
        if t is not None:
            placed.append((note, name, t, x))

    img, bottom = thera_lamps()
    put("thera-lamps", img, "ground", "1. Thera Deep's last pylons behind the ship, their pools baked; exact rows",
        exact_t(bottom, img.height), 240)
    for i, (t, x) in enumerate(PYLONS, 1):
        img, bottom, x0 = pylon_piece(t, x)
        put(f"lamp-pylon-{i}", img, "ground", f"the last grow-lamp pylon {i} (t~{t:g}), its pool baked; exact rows",
            exact_t(bottom, img.height), x0 + img.width // 2)
    img, bottom, x0 = ridge_piece()
    put("floor-ridge", img, "ground", f"a low rock ridge across the plain (t~{RIDGE_T}: the Reef Spitter nest on it); "
        "exact rows", exact_t(bottom, img.height), 240)
    for i, band in ((1, "reef"), (2, "silt"), (3, "silt")):
        if i >= len(SECTIONS):
            continue
        img, bottom = seam_piece(i, 2440 + i, band)
        name = f"seam-{SECTION_TILES[i][0]}"
        put(name, img, "ground", f"section {i + 1}'s seam (ground {seam_row(i):.0f}): {SECTION_TILES[i - 1][0]} into "
            f"{SECTION_TILES[i][0]}; exact rows", exact_t(bottom, img.height), 240)
    for i, (t, x) in enumerate(STACKS, 1):
        img, bottom, x0 = stack_piece(i, t, x)
        put(f"coral-stack-{i}", img, "ground", f"a dead coral stack of the reef field (t~{t:g}; scenery); exact rows",
            exact_t(bottom, img.height), x0 + img.width // 2)
    img, bottom, x0 = wreck_scar()
    put("wreck-scar", img, "ground", f"the Meridian's scour under the wreck (t~{WRECK_AT[0]:g} x {WRECK_AT[1]:g}); "
        "exact rows", exact_t(bottom, img.height), x0 + img.width // 2)
    body, glow = _beacon_frames()
    for i, (t, x) in enumerate(BEACONS):
        img, bottom, x0 = footing_piece(i)
        put(f"beacon-footing-{i + 1}", img, "ground", f"relay beacon {i + 1}'s footing and cable trenches (its foot "
            f"enters at the top edge at t={t:g}, x {x}); exact rows", exact_t(bottom, img.height), x0 + img.width // 2)
        foot = target_centre(t, 0)
        bb = int(round(foot - (props.BEACON[1] - 1 - props.BEACON_BASE[1])))   # the bottom that puts the foot on it
        put("relay-beacon", None, "ground", f"relay beacon {i + 1} on its footing (the 2 s pulse; its light is "
            "relay-beacon-glow, added over the grade at this placement)", exact_t(bb, props.BEACON[1]),
            x - props.BEACON_BASE[0] + props.BEACON[0] // 2, frames=body)
    put("relay-beacon-glow", None, "ground", "", None, None, frames=glow)
    img, bottom = trench_mouth()
    put("trench-mouth", img, "ground", f"the trench's mouth: its lip at the bottom edge at the level's end (t={END:g}); "
        "covers the screen through the outro; exact rows", exact_t(bottom, img.height), 240)
    global META
    META = {"pieces": pieces, "placed": [list(p) for p in placed]}
    print(f"wrote {len(TILES)} tile sets and {len(pieces)} pieces to {OUT.relative_to(ROOT)}")


# --------------------------------------------------------------------------- proposal

DESC = {
    "lamp-edge": "section 1: the silt plain at Thera Deep's edge, kelp stands, pale stones",
    "plain-reefs": "section 2: darker silt, ridges, Vrell coral crust and root veins (the reefs are units)",
    "beacon-line": "section 3: smooth dark silt, stones, almost no kelp",
    "deep-silt": "section 4: long low dunes of fine silt, empty",
    "ice-ceiling": "all sections: the ice ceiling's cracks of light (Level 23's; additive at 40 %)",
}
META = None


def meta():
    if META is not None:
        return META
    block = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))["backdrop"]
    return {"pieces": block["pieces"], "placed": [["", p["piece"], p["t"], p["x"]] for p in block["placed"]]}


def proposal_block():
    m = meta()
    src = "the level's data.yaml" if DATA else "the README (no data.yaml yet: re-run after D2)"
    lines = ["backdrop:",
             f"  # Presentation only: Level 24's abyssal plain (tools/art/backdrop_l24.py, step ART-D, from {src}).",
             "  # No deep layer (the ground is opaque). The plain darkens by the grade alone (underwater_look per section,",
             "  # ART-D's proposal below); the pylons' pools are baked assuming section 1's ambient 0.5. relay-beacon-glow is",
             "  # not placed: the game adds it over the grade at relay-beacon's placements on the same frame clock (D4).",
             "  scroll_factors: {far: 0.5, ground: 1, low-air: 1.35, high-air: 2.2}   # far unused, given as the loader asks",
             "  ramp: 2",
             "  haze_colour: 04222e  # the plain's blue-black water",
             "  atmosphere:",
             "    clear: {haze: 0.0}",
             "  tile_sets:"]
    for name in TILES:
        layer = "high-air" if name == "ice-ceiling" else "ground"
        lines.append(f"    {name}: {{layer: {layer}, height: {TILE_H}}}   # {DESC[name]}")
    lines.append("  pieces:")
    for name, spec in m["pieces"].items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']:g}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for note, piece, t, x in m["placed"]:
        if note:
            lines.append(f"    # {note}")
        lines.append(f"    - {{piece: {piece}, t: {t}, x: {x}}}")
    lines.append("# sections' tiles: " + "; ".join(f"{i + 1}: [{', '.join(t)}]" for i, t in enumerate(SECTION_TILES)))
    lines.append("# sections' underwater_look (ART-D's grade ladder proposal, round 53; the level-wide block = section 1's):")
    for i, look in enumerate(LOOKS):
        lines.append(f"#   {i + 1}: underwater_look: {look}")
    return "\n".join(lines) + "\n"


def write_proposal():
    PROPOSAL.write_text(proposal_block(), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# --------------------------------------------------------------------------- composite, checks and review

def piece_frames(piece):
    spec = meta()["pieces"][piece]
    n = spec.get("frames")
    if n:
        return [np.array(Image.open(OUT / f"{piece}_{k}.png").convert("RGBA")) for k in range(n)]
    return [np.array(Image.open(OUT / f"{piece}.png").convert("RGBA"))]


def placed_pieces(layer="ground"):
    out = []
    for note, piece, t, x in meta()["placed"]:
        if meta()["pieces"][piece]["layer"] != layer:
            continue
        imgs = piece_frames(piece)
        h, w = imgs[0].shape[:2]
        bottom = int(round(scroll(float(t)) * FACTORS[layer] + SCREEN / 2 - h / 2))
        out.append((piece, bottom, int(round(float(x) - w / 2)), imgs))
    return out


def _paste_rows(img, arr, x0, y_top):
    h = arr.shape[0]
    if x0 < 0:
        arr, x0 = arr[:, -x0:], 0
    if x0 + arr.shape[1] > W:
        arr = arr[:, :W - x0]
    if -h < y_top < SCREEN:
        img.alpha_composite(Image.fromarray(np.ascontiguousarray(arr), "RGBA"), (x0, y_top))


def ground_view(t, pieces, frame=0, glows=None):
    base = scroll(t)
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for y in range(SCREEN):
        row = int(math.floor(base + SCREEN - 1 - y))
        out[y] = tile(SECTION_TILES[section_at_row(row)][0])[TILE_H - 1 - row % TILE_H]
    img = Image.fromarray(out, "RGBA")
    for name, bottom, x0, arrs in pieces:
        h = arrs[0].shape[0]
        y_top = int(round(base + SCREEN - 1 - (bottom + h - 1)))
        if y_top >= SCREEN or y_top + h <= 0:
            continue
        _paste_rows(img, arrs[frame % len(arrs)], x0, y_top)
        if glows is not None and name == "relay-beacon":
            glows.append((x0, y_top))
    return img


def held_frames(name):
    try:
        return kit.load(name)
    except FileNotFoundError:
        return []


@lru_cache(maxsize=None)
def stand_ins():
    reef = held_frames("spawning-reef")
    if not reef:                                 # the round-48 still's reef, drawn as eu.reef draws it
        im = Image.new("RGBA", (72, 72), (0, 0, 0, 0))
        d = ImageDraw.Draw(im)
        rng = np.random.default_rng(1)
        for _ in range(26):
            a, rr = rng.uniform(0, TAU), rng.uniform(0, 26)
            px, py, s = 36 + math.cos(a) * rr, 36 + math.sin(a) * rr, rng.uniform(5, 11)
            c = (96, 52, 104) if rng.uniform() < 0.6 else (176, 160, 140)
            d.ellipse([px - s, py - s, px + s, py + s], fill=c + (255,), outline=(30, 16, 34, 255))
        d.ellipse([28, 28, 44, 44], fill=(30, 10, 40, 255))
        d.ellipse([32, 32, 40, 40], fill=(0, 255, 154, 255))
        reef = [im]
    floor = held_frames("reef-spitter-floor")
    gun = artkit.load_frames("reef-spitter")
    nest = None
    if floor and gun:
        nest = floor[0].copy()
        nest.alpha_composite(gun[0], (28 - gun[0].width // 2, 27 - gun[0].height // 2))
    return {"spawning-reef": reef[0], "reef-spitter": nest, "wreck": (held_frames("meridian-wreck") or [None])[0]}


def composite(t, gpieces, frame=None):
    k = int(t * props.PULSE_FPS) % props.PULSE_FRAMES if frame is None else frame
    spots = []
    img = ground_view(t, gpieces, k, spots)
    base = scroll(t)
    st = stand_ins()
    for kind, tt, x in TARGETS:
        im = st.get(kind)
        if im is None:
            continue
        y = base + SCREEN - target_centre(tt, im.height)
        if -60 < y < SCREEN + 60:
            artkit.sprite.paste_center(img, im, x, y)
    artkit.sprite.paste_center(img, kit.ship(2), 240, SCREEN - SHIP_Y)
    sec = section_at_row(base + SCREEN / 2) + 1
    img = kit.grade(img, sec)
    glow = piece_frames("relay-beacon-glow")[k]
    for x0, y_top in spots:
        img = kit.add_c(img, Image.fromarray(glow, "RGBA"), x0 + glow.shape[1] / 2, y_top + glow.shape[0] / 2)
    rows = base * FACTORS["high-air"] + SCREEN - 1 - np.arange(SCREEN)
    c = tile("ice-ceiling")
    hi = c[(TILE_H - 1 - np.mod(rows.astype(int), TILE_H))]
    a = np.array(img).astype(np.float64)
    a[..., :3] += hi[..., :3] * 0.4 * kit.SECTION_AMBIENT[sec]
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def wrap_score(a):
    lum = a[..., :3].astype(float) @ np.array((0.299, 0.587, 0.114))
    h = len(lum)

    def change(i, j):
        d = np.sort(np.abs(lum[i % h] - lum[j % h]))
        return d[:int(len(d) * 0.9)].mean()
    ref = sorted(change(k, k + 1) for k in range(h - 40, h + 40) if k < h - 4 or k > h + 2)
    return max(change(h - 2, h - 1), change(h - 1, 0), change(0, 1)) / max(ref[len(ref) // 2], 0.5)


def check():
    """Wraps (BackdropSeamsTest's measure), sizes, the edge rule of BackdropAssetsTest (a piece's opaque edge row or
    column never on the screen, unless it lies on the field's edge), nothing over a spawning reef, the beacons' clear
    100 px."""
    problems = []
    for name in TILES:
        a = tile(name)[..., :3].astype(float)
        jump = np.abs(a[0] - a[-1]).mean()
        inner = np.abs(a[1:] - a[:-1]).mean()
        if jump > 3 * inner + 6:
            problems.append(f"{name}: wrap seam {jump:.1f} vs {inner:.1f}")
        if name != "ice-ceiling" and wrap_score(tile(name)) > 1.5:
            problems.append(f"{name}: wrap score {wrap_score(tile(name)):.2f} (BackdropSeamsTest's limit 1.5)")
        if name != "ice-ceiling" and (tile(name)[..., 3] < 255).any():
            problems.append(f"{name}: a ground tile must be opaque")
    for name, spec in meta()["pieces"].items():
        for arr in piece_frames(name):
            if arr.shape[0] > MAX_PIECE or tuple(spec["size"]) != (arr.shape[1], arr.shape[0]):
                problems.append(f"{name}: size {arr.shape[1]}x{arr.shape[0]} (spec {spec['size']})")
                break
    for name, bottom, x0, arrs in placed_pieces():
        h, w = arrs[0].shape[:2]
        y_start = bottom                                   # the bottom edge's screen y at t=0 (y up)
        for arr in arrs:
            a = arr[..., 3] > 0
            if a[-1].any() and y_start > 0:
                problems.append(f"{name} (bottom {bottom}): opaque bottom row")
            if a[0].any() and scroll(END + OUTRO) + SCREEN > bottom + h:
                problems.append(f"{name} (bottom {bottom}): opaque top row comes on screen")
            if a[:, 0].any() and x0 > 0:
                problems.append(f"{name}: opaque left column at x {x0}")
            if a[:, -1].any() and x0 + w < W:
                problems.append(f"{name}: opaque right column at x {x0 + w}")
        for t, x in REEFS:                                 # nothing opaque within 40 px of a reef's centre
            row = target_centre(t, 72)
            ry = bottom + h - 1 - row
            lo_y, hi_y = int(max(0, ry - 40)), int(min(h, ry + 40))
            lo_x, hi_x = int(max(0, x - 40 - x0)), int(min(w, x + 40 - x0))
            if lo_y < hi_y and lo_x < hi_x and (arrs[0][lo_y:hi_y, lo_x:hi_x, 3] > 0).any() and \
                    not name.startswith("seam-"):
                problems.append(f"{name}: over the spawning reef at t={t:g} x {x:g}")
    for kind, t, x in TARGETS:                             # D7: no ground target within 100 px of a beacon's foot
        for bt, bx in BEACONS:
            if math.hypot(x - bx, target_centre(t, 0) - target_centre(bt, 0)) < 100:
                problems.append(f"{kind} at t={t:g} x {x:g}: within 100 px of the beacon at t={bt} x {bx}")
    print("check: " + ("ok" if not problems else f"{len(problems)} problems"))
    for p in sorted(set(problems)):
        print("  " + p)
    return problems


TIMES = [(1, "1 THERA DEEP'S LAMPS BEHIND"), (5, "1 PYLON 1, KELP STANDS"), (21, "1 THE RIDGE, NESTS"),
         (36, "2 THE REEF FIELD, REEF 1"), (50, "2 REEF 2, A STACK"), (66, "2 REEF 3, DARKER"),
         (101, "3 BEACON 1, THE WRECK AHEAD"), (113, "3 BEACON 2"), (150, "4 THE RAY CROSSING"),
         (171, "4 THE TRENCH'S MOUTH")]


def review():
    g = placed_pieces("ground")
    shots = [composite(t, g, frame=0 if i % 2 == 0 else 6) for i, (t, _) in enumerate(TIMES)]
    cols = 5
    cw, ch = 240, 270
    right = 3 * (100 + 8) + 16
    sheet = raster.sheet(16 + cols * (cw + 8) + 8 + right, 60 + 2 * (ch + 24) + 330,
                         "LEVEL 24 BACKDROP - ABYSSAL PLAIN (FINAL)", f"PRODUCTION ART, {BATCH.upper()} - R53")
    raster.draw_text(sheet, 16, 38, "THE SCREEN AT 1/2 UNDER THE REVIEW GRADE (AMBIENT 0.50 / 0.32 / 0.18 / 0.14 BY "
                     "SECTION); STAND-INS: REEFS, NESTS, WRECK, SHIP", raster.LABEL)
    for i, ((t, label), img) in enumerate(zip(TIMES, shots)):
        x, y = 16 + (i % cols) * (cw + 8), 56 + (i // cols) * (ch + 24)
        sheet.alpha_composite(img.resize((cw, ch), Image.Resampling.BOX), (x, y))
        raster.draw_text(sheet, x, y + ch + 4, f"T={t:g} {label}"[:40], raster.LABEL_DIM)
    x0 = 16 + cols * (cw + 8) + 8
    raster.draw_text(sheet, x0, 38, "TILES (1/4, UNGRADED)", raster.LABEL)
    for k, name in enumerate(["lamp-edge", "plain-reefs", "beacon-line", "deep-silt"]):
        im = Image.fromarray(tile(name), "RGBA").resize((100, 200), Image.Resampling.BOX)
        sheet.alpha_composite(im, (x0 + (k % 3) * 108, 56 + (k // 3) * 226))
        raster.draw_text(sheet, x0 + (k % 3) * 108, 56 + (k // 3) * 226 + 204, name.upper()[:16], raster.LABEL_DIM)
    b = Image.fromarray(piece_frames("relay-beacon")[0], "RGBA").resize((64, 96), Image.Resampling.BOX)
    sheet.alpha_composite(b, (x0 + 108 + 20, 282))
    raster.draw_text(sheet, x0 + 108, 486, "RELAY BEACON 1/2", raster.LABEL_DIM)
    y = 56 + 2 * (ch + 24)
    raster.draw_text(sheet, 16, y, "AT 1X: THE LAMPS BEHIND (T=1), THE FIRST BEACON'S FLASH (T=101), THE TRENCH (T=171)",
                     raster.LABEL)
    sheet.alpha_composite(shots[0].crop((0, 270, 480, 540)), (16, y + 16))
    sheet.alpha_composite(composite(101, g, frame=0).crop((0, 0, 480, 270)), (16 + 488, y + 16))
    sheet.alpha_composite(shots[9].crop((90, 270, 390, 540)), (16 + 2 * 488, y + 16))
    frames = [composite(t, g).resize((240, 270), Image.Resampling.BOX).convert("RGB")
              for t in np.arange(97, 107, 0.125)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=8)


def main(argv):
    CONCEPT.mkdir(parents=True, exist_ok=True)
    if "--check" in argv:
        sys.exit(1 if check() else 0)
    if "--review" in argv:
        review()
        return
    if "--proposal" in argv and (OUT / "lamp-edge.png").exists() and PROPOSAL.exists():
        write_proposal()
        return
    build()
    write_proposal()
    check()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
